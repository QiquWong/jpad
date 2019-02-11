package it.unina.daf.jpadcad.occfx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADVertex;
import it.unina.daf.jpadcad.occ.OCCDataProvider;
import it.unina.daf.jpadcad.occ.OCCUtils;
import javafx.scene.shape.TriangleMesh;
import opencascade.BRepMesh_IncrementalMesh;
import opencascade.BRepTools;
import opencascade.BRep_Tool;
import opencascade.GeomLProp_SLProps;
import opencascade.Geom_Surface;
import opencascade.Poly_Array1OfTriangle;
import opencascade.Poly_Triangulation;
import opencascade.ShapeAnalysis_FreeBounds;
import opencascade.TopAbs_Orientation;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopLoc_Location;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Vertex;
import opencascade.TopoDS_Wire;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Pnt2d;
import standaloneutils.MyArrayUtils;

public class OCCFXMeshExtractor {

	public static class VertexData extends OCCDataProvider {
		
		private final TopoDS_Vertex vertex;
		
		public VertexData (final TopoDS_Vertex vertex) {
			this.vertex = vertex;
		}
		
		@Override
		public void load() {
			
			if (OCCUtils.theFactory == null) OCCUtils.initCADShapeFactory();
			
			CADVertex cadVertex = (CADVertex) OCCUtils.theFactory.newShape(vertex);
			float[] newNodes = new float[3];
			double[] pnt = cadVertex.pnt();
			newNodes[0] = (float) pnt[0];
			newNodes[1] = (float) pnt[1];
			newNodes[2] = (float) pnt[2];
			setNodes(newNodes);
			vertices = new int[2];
			vertices[0] = 1;
			vertices[1] = 0;
			nbrOfVertices = 1;
		}
	}
	
	public static class EdgeData extends OCCDataProvider {
		
		private final TopoDS_Edge edge;
		
		public EdgeData(final TopoDS_Edge edge) {
			this.edge = edge;
		}
		
		@Override
		public void load() {
			
			if (OCCUtils.theFactory == null) OCCUtils.initCADShapeFactory();
			
			CADEdge cadEdge = (CADEdge)OCCUtils.theFactory.newShape(edge);
			double[] range = cadEdge.range();
			
			CADGeomCurve3D gc = OCCUtils.theFactory.newCurve3D(cadEdge);
			float[] newNodes = null;
			if (gc != null) {
				int npts = gc.nbPoints();
				
				// Allocate one additional point at each end = parametric value 0, 1
				newNodes = new float[(npts + 2) * 3];
				int j = 0;
				double[] values = gc.value(range[0]);
				newNodes[j++] = (float) values[0];
				newNodes[j++] = (float) values[1];
				newNodes[j++] = (float) values[2];
				
				// All intermediary points
				for (int i = 0; i < npts; ++i) {
					values = gc.value(gc.parameter(i + 1));
					newNodes[j++] = (float) values[0];
					newNodes[j++] = (float) values[1];
					newNodes[j++] = (float) values[2];
				}
				
				// Add last point
				values = gc.value(range[1]);
				newNodes[j++] = (float) values[0];
				newNodes[j++] = (float) values[1];
				newNodes[j++] = (float) values[2];
			}
			else if (!cadEdge.isDegenerated()) {
				
				// So, there is no curve, and the edge is not degenerated?
				// => draw lines between the vertices and ignore curvature
				// best approximation we can do
				
				ArrayList<double[]> aa = new ArrayList<double[]>(); // store points here
				for (TopExp_Explorer explorer2 = new TopExp_Explorer(edge,TopAbs_ShapeEnum.TopAbs_VERTEX);
						explorer2.More() == 1; explorer2.Next()) {
					
					TopoDS_Shape sv = explorer2.Current();
					if (!(sv instanceof TopoDS_Vertex))
						continue; // should not happen!
					
					TopoDS_Vertex v = (TopoDS_Vertex) sv;
					aa.add(((CADVertex) OCCUtils.theFactory.newShape(v)).pnt());
				}
				
				newNodes = new float[aa.size() * 3];
				for (int i = 0, k = 0; i < aa.size(); i++) {
					
					double[] f = aa.get(i);
					newNodes[k++] = (float) f[0];
					newNodes[k++] = (float) f[1];
					newNodes[k++] = (float) f[2];
				}
			}
			
			if (newNodes != null) {
				setNodes(newNodes);
			}
			
			if (newNodes == null || newNodes.length == 0) {
				unLoad();
				return;
			}
			
			// Construct the indices
			nbrOfLines = newNodes.length / 3 - 1;
			lines = new int[nbrOfLines * 3];
			int offset = 0;
			for (int i = 0; i < nbrOfLines;) {
				lines[offset++] = 2;
				lines[offset++] = i++;
				lines[offset++] = i;
			}
		}
	}
	
	public static class FaceData extends OCCDataProvider {
		
		private final TopoDS_Face face;
		private final boolean faceReversed;
		
		private int[] itriangles;
		private int[] ifacesFX;
		TriangleMesh meshFX;
		
		public FaceData(final TopoDS_Face face, boolean faceReversed) {
			this.face = face;
			this.faceReversed = faceReversed;
		}
		
		private boolean checkNormals(double[] normals) {
			
			for (int i = 0; i < normals.length / 3; i++)
				if (normals[3 * i] == 0 & normals[3 * i + 1] == 0 & normals[3 * i + 2] == 0)
					return false;
			return true;
		}
		
		private void transformMesh(TopLoc_Location loc, double[] src, float[] dst) {
			
			/*  Complex transformations can be obtained by combining the
			 *  previous elementary transformations using the method
			 *  Multiply.
			 *  The transformations can be represented as follow :
			 * 
			 *     V1   V2   V3    T       XYZ        XYZ
			 *  | a11  a12  a13   a14 |   | x |      | x'|
			 *  | a21  a22  a23   a24 |   | y |      | y'|
			 *  | a31  a32  a33   a34 |   | z |   =  | z'|
			 *  |  0    0    0     1  |   | 1 |      | 1 |
			 * 
			 *  where {V1, V2, V3} defines the vectorial part of the
			 *  transformation and T defines the translation part of the
			 *  transformation.
			 */			
			
			// get the transformation matrix
			double[] matrix = new double[16];
			int k=0;
			for(int i=1; i<=3; i++)
				for(int j=1; j<=4; j++)
					matrix[k++]=loc.Transformation().Value(i,j);
			matrix[12]=0;
			matrix[13]=0;
			matrix[14]=0;
			matrix[15]=1;
			
			Matrix4d m4d = new Matrix4d(matrix);
			Point3d p3d = new Point3d();
			for (int i = 0; i < src.length; i += 3) {
				
				p3d.x = src[i + 0];
				p3d.y = src[i + 1];
				p3d.z = src[i + 2];
				m4d.transform(p3d);
				dst[i + 0] = (float) p3d.x;
				dst[i + 1] = (float) p3d.y;
				dst[i + 2] = (float) p3d.z;
			}
		}
		
		/**
		 * @param itriangles
		 */
		private static void reverseMesh(int[] itriangles) {
			for (int i = 0; i < itriangles.length; i += 3) {
				int tmp = itriangles[i];
				itriangles[i] = itriangles[i + 1];
				itriangles[i + 1] = tmp;
			}
		}
		
		@Override
		public void load() {
			TopLoc_Location loc = new TopLoc_Location();
			Poly_Triangulation pt = BRep_Tool.Triangulation(face, loc);
			int j0 = 0;
			float[] newNodes = null;
			
			if (pt == null) {
				System.err.println("Triangulation failed for face " + face + ". Trying other mesh parameters.");
				newNodes = new float[0];
				polys = new int[0];
				normals = new float[0];
				return;
			}
						
			List<gp_Pnt> gpNodes = new ArrayList<>();
			for(int k = 1; k <= pt.NbNodes(); k++)
				gpNodes.add(pt.Nodes().Value(k));
						
			Poly_Array1OfTriangle pat = pt.Triangles();
			int s = pat.Length()*3;
			itriangles = new int[s];
			int[] n1 = new int[] {0};
			int[] n2 = new int[] {0};
			int[] n3 = new int[] {0};
			for(int i = pat.Lower(); i <= pat.Upper(); i++) {
				
				//Triangles(i).Get(n1,n2,n3);
				pat.Value(i).Get(n1,n2,n3);
				itriangles[j0]   = n1[0] - 1;
				itriangles[j0+1] = n2[0] - 1;
				itriangles[j0+2] = n3[0] - 1;
				j0+=3;
			}
						
			if ((face.Orientation() == TopAbs_Orientation.TopAbs_REVERSED && !this.faceReversed) ||
				(face.Orientation() != TopAbs_Orientation.TopAbs_REVERSED && this.faceReversed))
				reverseMesh(itriangles);
			
			// Compute the indices
			nbrOfPolys = itriangles.length / 3;
			polys = new int[4 * nbrOfPolys];
			int offset = 0;
			
			// polys are in DataProvider class, itriangles in FaceData class
			for (int i = 0; i < itriangles.length; ) {
				
				polys[offset++] = 3;
				polys[offset++] = itriangles[i++];
				polys[offset++] = itriangles[i++];
				polys[offset++] = itriangles[i++];
			}
			
			// define nodes
			newNodes = new float[3*gpNodes.size()]; // see array dd to refactor using Java8+ constructs
			if (loc.IsIdentity() == 1) {
				j0 = 0;
				for (int ii = 0; ii < gpNodes.size(); ii++) {
					newNodes[j0  ] = (float) gpNodes.get(ii).X();
					newNodes[j0+1] = (float) gpNodes.get(ii).Y();
					newNodes[j0+2] = (float) gpNodes.get(ii).Z();
					j0 += 3;
				}
			} else {
				List<Double> dd = gpNodes.stream()
						.map(gp -> Arrays.stream(new Double[]{gp.X(), gp.Y(), gp.Z()}).collect(Collectors.toList()))
						.flatMap(List::stream)
						.collect(Collectors.toList()); 
				transformMesh(
						loc, 
						MyArrayUtils.convertToDoublePrimitive(dd),
						newNodes);
			}
			setNodes(newNodes);
			
			// allocate the JavaFX's TriangleMesh
			meshFX = new TriangleMesh();
			// for now we'll just make an empty texCoordinate group
			meshFX.getTexCoords().addAll(0, 0);
			// add nodes
			meshFX.getPoints().addAll(getNodes());

			// prepare face indices: p0,t0, p1,t1, p3,t3 ... with t_i=0
			ifacesFX = new int[itriangles.length/3 * 6];
			for (int i = 0; i < itriangles.length; ) {
				//System.out.println("i: " + i);
				meshFX.getFaces().addAll(
						itriangles[i++], 0,
						itriangles[i++], 0,
						itriangles[i++], 0
				);
			}
						
			// Compute the normals
			Geom_Surface surf = BRep_Tool.Surface(face);
			if (surf != null) {
				GeomLProp_SLProps geomProp = new GeomLProp_SLProps(1, 0);
				geomProp.SetSurface(surf);
				if (pt.HasUVNodes() == 1) {					
					int numNodes = pt.NbNodes();
					double[] n = new double[3*numNodes];
					List<gp_Pnt2d> uvNodes = new ArrayList<>();
					for(int i = 1; i <= numNodes; i++) {
						uvNodes.add(pt.UVNodes().Value(i));
					}
					
					for(int i = 0; i < numNodes; i++) {
						geomProp.SetParameters(uvNodes.get(i).X(), uvNodes.get(i).Y());
						
						if(geomProp.IsNormalDefined() == 0) {
							n[3*i  ] = 0;
							n[3*i+1] = 0;
							n[3*i+2] = 0;
						}
						else {
							gp_Dir normal = geomProp.Normal();
							n[3*i  ] = normal.X();
							n[3*i+1] = normal.Y();
							n[3*i+2] = normal.Z();
						}
					}
					
					//check the normals
					if (!checkNormals(n)) {
						// TODO : To be checked
						normals = new float[n.length];
						for (int i = 0; i < n.length; i++)
							normals[i] = (float) 0.;
					} else {
						//convert into floats
						normals = new float[n.length];
						// Inverse normal if the face is inverted
						double reverse = 1.;
						if ((face.Orientation() == TopAbs_Orientation.TopAbs_REVERSED && !this.faceReversed)
								|| (face.Orientation() != TopAbs_Orientation.TopAbs_REVERSED && this.faceReversed))
							reverse = -1.;
						for (int i = 0; i < n.length; i++)
							normals[i] = (float) (reverse * n[i]);
					}
				} else
					System.err.println("No UV Nodes to the point triangulation !");
			} else
				System.err.println("Can not compute the Geom_Surface of " + face);
		}
		
		public int[] getITriangles() {
			return itriangles;
		}
		
		public int[] getIFacesFX() {
			return ifacesFX;
		}
		
		public TriangleMesh getTriangleMesh() {
			return meshFX;
		}

	}// end-of-class FaceData
	
	private /* final */ TopoDS_Shape shape;
	private boolean meshCreated;

	public OCCFXMeshExtractor(TopoDS_Shape shape) {
		this.shape = shape;
	}

	public Collection<TopoDS_Vertex> getVertices() {
		TopExp_Explorer explorer = new TopExp_Explorer();
		HashSet<TopoDS_Vertex> vertices = new HashSet<TopoDS_Vertex>();
		for (explorer.Init(shape, TopAbs_ShapeEnum.TopAbs_VERTEX); explorer.More() == 1; explorer.Next())
			vertices.add(TopoDS.ToVertex(explorer.Current()));
		return vertices;
	}
	
	public Collection<TopoDS_Wire> getWires() {
		TopExp_Explorer explorer = new TopExp_Explorer();
		HashSet<TopoDS_Wire> wires = new HashSet<TopoDS_Wire>();
		for (explorer.Init(shape, TopAbs_ShapeEnum.TopAbs_WIRE); explorer.More() == 1; explorer.Next())
			wires.add(TopoDS.ToWire(explorer.Current()));
		return wires;
	}
	
	public Collection<TopoDS_Compound> getCompounds() {
		TopExp_Explorer explorer = new TopExp_Explorer();
		HashSet<TopoDS_Compound> compounds = new HashSet<TopoDS_Compound>();
		for (explorer.Init(shape, TopAbs_ShapeEnum.TopAbs_COMPOUND); explorer.More() == 1; explorer.Next())
			compounds.add(TopoDS.ToCompound(explorer.Current()));
		return compounds;
	}
	
	/**
	 * Create the mesh by calling BRepMesh_IncrementalMesh
	 * Override to call it with custom parameters
	 */
	protected void createMesh() {
		//Force to recreate the mesh with our parameters
		BRepTools.Clean(shape);
		new BRepMesh_IncrementalMesh(shape, 7E-5);
	}
	
	public Collection<TopoDS_Face> getFaces() {
		if(!meshCreated) {
			createMesh();
			meshCreated = true;
		}
		TopExp_Explorer explorer = new TopExp_Explorer();
		HashSet<TopoDS_Face> faces = new LinkedHashSet<TopoDS_Face>();
		for (explorer.Init(shape, TopAbs_ShapeEnum.TopAbs_FACE); explorer.More() == 1; explorer.Next())
			faces.add(TopoDS.ToFace(explorer.Current()));
		return faces;
	}
	
	/**
	 * Get only free edges
	 * @return
	 */
	public Collection<TopoDS_Edge> getFreeEdges() {
		ShapeAnalysis_FreeBounds safb = new ShapeAnalysis_FreeBounds(shape);
		TopoDS_Compound closedWires = safb.GetClosedWires();
		if(closedWires == null)
			return Collections.emptySet();
		TopExp_Explorer explorer = new TopExp_Explorer();
		HashSet<TopoDS_Edge> freeEdges = new HashSet<TopoDS_Edge>();
		for(explorer.Init(closedWires, TopAbs_ShapeEnum.TopAbs_EDGE); explorer.More() == 1; explorer.Next())
			freeEdges.add(TopoDS.ToEdge(explorer.Current()));
		return freeEdges;
	}
	
	/**
	 * Get all the edges (free edges or not)
	 * @return
	 */
	public Collection<TopoDS_Edge> getEdges() {
		TopExp_Explorer explorer = new TopExp_Explorer();
		HashSet<TopoDS_Edge> edges = new HashSet<TopoDS_Edge>();
		for (explorer.Init(shape, TopAbs_ShapeEnum.TopAbs_EDGE); explorer.More() == 1; explorer.Next())
			edges.add(TopoDS.ToEdge(explorer.Current()));
		return edges;
	}
	
	public static void setNodes(float[] nodes) {
		throw new RuntimeException("DataProvider.EMPTY is immutable");
	}

	public TopoDS_Shape getShape() {
		return shape;
	}
}
