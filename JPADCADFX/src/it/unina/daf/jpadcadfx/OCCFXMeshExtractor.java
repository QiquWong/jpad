package it.unina.daf.jpadcadfx;

//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.LinkedHashSet;
//
//import javax.vecmath.Matrix4d;
//import javax.vecmath.Point3d;
//
//import org.jcae.opencascade.Utilities;
//import org.jcae.opencascade.jni.BRepBndLib;
//import org.jcae.opencascade.jni.BRepMesh_IncrementalMesh;
//import org.jcae.opencascade.jni.BRepTools;
//import org.jcae.opencascade.jni.BRep_Tool;
//import org.jcae.opencascade.jni.Bnd_Box;
//import org.jcae.opencascade.jni.GCPnts_UniformDeflection;
//import org.jcae.opencascade.jni.GeomAdaptor_Curve;
//import org.jcae.opencascade.jni.GeomLProp_SLProps;
//import org.jcae.opencascade.jni.Geom_Curve;
//import org.jcae.opencascade.jni.Geom_Surface;
//import org.jcae.opencascade.jni.Poly_Triangulation;
//import org.jcae.opencascade.jni.ShapeAnalysis_FreeBounds;
//import org.jcae.opencascade.jni.TopAbs_Orientation;
//import org.jcae.opencascade.jni.TopAbs_ShapeEnum;
//import org.jcae.opencascade.jni.TopExp_Explorer;
//import org.jcae.opencascade.jni.TopLoc_Location;
//import org.jcae.opencascade.jni.TopoDS_Compound;
//import org.jcae.opencascade.jni.TopoDS_Edge;
//import org.jcae.opencascade.jni.TopoDS_Face;
//import org.jcae.opencascade.jni.TopoDS_Shape;
//import org.jcae.opencascade.jni.TopoDS_Vertex;
//import org.jcae.opencascade.jni.TopoDS_Wire;
//
//import javafx.scene.shape.TriangleMesh;
//
///**
// * This class compute different type of meshes (vertices, edges, faces...) from
// * a TopoDS_Shape.
// * @author Agodemar
// */
////TODO try to remove dependency over java3d (javax.vecmath.*)
//public class OCCFXMeshExtractor
//{
//
//	public static class VertexData extends OCCDataProvider
//	{
//		private final TopoDS_Vertex vertex;
//		public VertexData(final TopoDS_Vertex vertex)
//		{
//			this.vertex = vertex;
//		}
//		@Override
//		public void load()
//		{
//			float[] newNodes = new float[3];
//			double[] pnt = BRep_Tool.pnt(vertex);
//			newNodes[0] = (float) pnt[0];
//			newNodes[1] = (float) pnt[1];
//			newNodes[2] = (float) pnt[2];
//			setNodes(newNodes);
//			vertices = new int[2];
//			vertices[0] = 1;
//			vertices[1] = 0;
//			nbrOfVertices = 1;
//		}
//	}
//	public static class EdgeData extends OCCDataProvider
//	{
//		private final TopoDS_Edge edge;
//		public EdgeData(final TopoDS_Edge edge)
//		{
//			this.edge = edge;
//		}
//		@Override
//		public void load()
//		{
//			double[] range = BRep_Tool.range(edge);
//			Geom_Curve gc = BRep_Tool.curve(edge, range);
//			Bnd_Box box = new Bnd_Box();
//			BRepBndLib.add(edge, box);
//			double[] bbox = box.get();
//			double boundingBoxDeflection = 0.0005 *
//					Math.max(Math.max(bbox[3] - bbox[0], bbox[4] - bbox[1]), bbox[5] -
//							bbox[2]);
//			float[] newNodes = null;
//			if (gc != null)
//			{
//				GeomAdaptor_Curve adaptator = new GeomAdaptor_Curve(gc);
//				GCPnts_UniformDeflection deflector =
//						new GCPnts_UniformDeflection();
//				deflector.initialize(adaptator, boundingBoxDeflection, range[0],
//						range[1]);
//				int npts = deflector.nbPoints();
//				// Allocate one additional point at each end = parametric value 0, 1
//				newNodes = new float[(npts + 2) * 3];
//				int j = 0;
//				double[] values = adaptator.value(range[0]);
//				newNodes[j++] = (float) values[0];
//				newNodes[j++] = (float) values[1];
//				newNodes[j++] = (float) values[2];
//				// All intermediary points
//				for (int i = 0; i <
//						npts; ++i)
//				{
//					values = adaptator.value(deflector.parameter(i + 1));
//					newNodes[j++] = (float) values[0];
//					newNodes[j++] = (float) values[1];
//					newNodes[j++] = (float) values[2];
//				}
//				// Add last point
//				values = adaptator.value(range[1]);
//				newNodes[j++] = (float) values[0];
//				newNodes[j++] = (float) values[1];
//				newNodes[j++] = (float) values[2];
//			}
//			else if (!BRep_Tool.degenerated(edge))
//			{
//				// So, there is no curve, and the edge is not degenerated?
//				// => draw lines between the vertices and ignore curvature
//				// best approximation we can do
//				ArrayList<double[]> aa = new ArrayList<double[]>(); // store points here
//				for (TopExp_Explorer explorer2 = new TopExp_Explorer(edge,
//						TopAbs_ShapeEnum.VERTEX);
//						explorer2.more(); explorer2.next())
//				{
//					TopoDS_Shape sv = explorer2.current();
//					if (!(sv instanceof TopoDS_Vertex))
//						continue; // should not happen!
//					TopoDS_Vertex v = (TopoDS_Vertex) sv;
//					aa.add(BRep_Tool.pnt(v));
//				}
//				newNodes = new float[aa.size() * 3];
//				for (int i = 0, j = 0; i <
//						aa.size(); i++)
//				{
//					double[] f = aa.get(i);
//					newNodes[j++] = (float) f[0];
//					newNodes[j++] = (float) f[1];
//					newNodes[j++] = (float) f[2];
//				}
//			}
//			if(newNodes != null)
//			{
//				setNodes(newNodes);
//			}
//			if (newNodes == null || newNodes.length == 0)
//			{
//				unLoad();
//				return;
//			}
//			// Construct the indices
//			nbrOfLines = newNodes.length / 3 - 1;
//			lines = new int[nbrOfLines * 3];
//			int offset = 0;
//			for (int i = 0; i < nbrOfLines;)
//			{
//				lines[offset++] = 2;
//				lines[offset++] = i++;
//				lines[offset++] = i;
//			}
//		}
//	}
//	public static class FaceData extends OCCDataProvider
//	{
//		private final TopoDS_Face face;
//		private final boolean faceReversed;
//		
//		private int[] itriangles;
//		private int[] ifacesFX;
//		TriangleMesh meshFX;
//		
//		public FaceData(final TopoDS_Face face, boolean faceReversed)
//		{
//			this.face = face;
//			this.faceReversed = faceReversed;
//		}
//		private boolean checkNormals(double[] normals)
//		{
//			for (int i = 0; i < normals.length / 3; i++)
//				if (normals[3 * i] == 0 & normals[3 * i + 1] == 0 & normals[3 * i + 2] == 0)
//					return false;
//			return true;
//		}
//		private void transformMesh(TopLoc_Location loc, double[] src, float[] dst)
//		{
//			double[] matrix = new double[16];
//			loc.transformation().getValues(matrix);
//			Matrix4d m4d = new Matrix4d(matrix);
//			Point3d p3d = new Point3d();
//			for (int i = 0; i < src.length; i += 3)
//			{
//				p3d.x = src[i + 0];
//				p3d.y = src[i + 1];
//				p3d.z = src[i + 2];
//				m4d.transform(p3d);
//				dst[i + 0] = (float) p3d.x;
//				dst[i + 1] = (float) p3d.y;
//				dst[i + 2] = (float) p3d.z;
//			}
//		}
//		/**
//		 * @param itriangles
//		 */
//		static private void reverseMesh(int[] itriangles)
//		{
//			for (int i = 0; i < itriangles.length; i += 3)
//			{
//				int tmp = itriangles[i];
//				itriangles[i] = itriangles[i + 1];
//				itriangles[i + 1] = tmp;
//			}
//		}
//		@Override
//		public void load()
//		{
//			TopLoc_Location loc = new TopLoc_Location();
//			Poly_Triangulation pt = BRep_Tool.triangulation(face, loc);
//			float[] newNodes = null;
//			if (pt == null)
//			{
//				System.err.println("Triangulation failed for face " + face +
//						". Trying other mesh parameters.");
//				newNodes = new float[0];
//				polys = new int[0];
//				normals = new float[0];
//				return;
//			}
//			double[] dnodes = pt.nodes();
//			// final int[] itriangles = pt.triangles();
//			itriangles = pt.triangles();
//			if ((face.orientation() == TopAbs_Orientation.REVERSED && !this.faceReversed)
//					|| (face.orientation() != TopAbs_Orientation.REVERSED && this.faceReversed)
//					)
//				reverseMesh(itriangles);
//			// Compute the indices
//			nbrOfPolys = itriangles.length / 3;
//			polys = new int[4 * nbrOfPolys];
//			int offset = 0;
//			// polys are in DataProvider class, itriangles in FaceData class
//			for (int i = 0; i < itriangles.length;)
//			{
//				polys[offset++] = 3;
//				polys[offset++] = itriangles[i++];
//				polys[offset++] = itriangles[i++];
//				polys[offset++] = itriangles[i++];
//			}
//			
//			// define nodes
//			newNodes = new float[dnodes.length];
//			if (loc.isIdentity()) {
//				for (int j = 0; j < dnodes.length; j++)
//					newNodes[j] = (float) dnodes[j];
//			}
//			else {
//				transformMesh(loc, dnodes, newNodes);
//			}
//			setNodes(newNodes);
//			
//			// allocate the JavaFX's TriangleMesh
//			meshFX = new TriangleMesh();
//			// add nodes
//			meshFX.getPoints().addAll(getNodes());
//			
//			//for now we'll just make an empty texCoordinate group
//			meshFX.getTexCoords().addAll(0, 0);
//
//			// prepare face indices: p0,t0, p1,t1, p3,t3 ... with t_i=0
//			ifacesFX = new int[itriangles.length/3 * 6];
//			for (int i = 0; i < itriangles.length; )
//			{
//				// System.out.println("i: " + i);
//				meshFX.getFaces().addAll(
//						itriangles[i++],0,
//						itriangles[i++],0,
//						itriangles[i++],0
//				);
//			}
//			
//			// Add the faces "winding" (see JavaFX TriangleMesh) the points generally counter clock wise
////			for (int i = 0; i < itriangles.length; )
////			{
////				// System.out.println("i: " + i);
////				meshFX.getFaces().addAll(
////						itriangles[i++],0,
////						itriangles[i++],0,
////						itriangles[i++],0
////				);
////			}
//			meshFX.getFaces().addAll(ifacesFX);
//			
//			// Compute the normals
//			Geom_Surface surf = BRep_Tool.surface(face);
//			if (surf != null) {
//				GeomLProp_SLProps geomProp = new GeomLProp_SLProps(1, 0);
//				geomProp.setSurface(surf);
//				if (pt.hasUVNodes()) {
//					double[] n = geomProp.normalArray(pt.uvNodes());
//					//check the normals
//					if (!checkNormals(n)) {
//						// TODO : To be checked
//						normals = new float[n.length];
//						for (int i = 0; i < n.length; i++)
//							normals[i] = (float) 0.;
//						System.err.println("Normal computation failed " + face + "\n");
//					} else {
//						//convert into floats
//						normals = new float[n.length];
//						// Inverse normal if the face is inversed
//						double reverse = 1.;
//						if ((face.orientation() == TopAbs_Orientation.REVERSED && !this.faceReversed)
//								|| (face.orientation() != TopAbs_Orientation.REVERSED && this.faceReversed))
//							reverse = -1.;
//						for (int i = 0; i < n.length; i++)
//							normals[i] = (float) (reverse * n[i]);
//					}
//				} else
//					System.err.println("No UV Nodes to the point triangulation !");
//			} else
//				System.err.println("Can not compute the Geom_Surface of " + face);
//		}
//		public int[] getITriangles() {
//			return itriangles;
//		}
//		public int[] getIFacesFX() {
//			return ifacesFX;
//		}
//		public TriangleMesh getTriangleMesh() {
//			return meshFX;
//		}
//
//	}// end-of-class FaceData
//	
//	private final TopoDS_Shape shape;
//	private boolean meshCreated;
//	/**
//	 * Create a CAOMeshExtractor from a TopoDS_Shape object
//	 *
//	 * @param shape
//	 */
//	public OCCFXMeshExtractor(TopoDS_Shape shape)
//	{
//		this.shape = shape;
//	}
//	/**
//	 * Create a CAOMeshExtractor from a BREP, STEP or IGES file.
//	 * @param fileName
//	 */
//	public OCCFXMeshExtractor(String fileName)
//	{
//		// TODO: check this
//		this(Utilities.readFile(fileName));
//	}
//	public Collection<TopoDS_Vertex> getVertices()
//	{
//		TopExp_Explorer explorer = new TopExp_Explorer();
//		HashSet<TopoDS_Vertex> vertices = new HashSet<TopoDS_Vertex>();
//		for (explorer.init(shape, TopAbs_ShapeEnum.VERTEX); explorer.more(); explorer.next())
//			vertices.add((TopoDS_Vertex) explorer.current());
//		return vertices;
//	}
//	public Collection<TopoDS_Wire> getWires()
//	{
//		TopExp_Explorer explorer = new TopExp_Explorer();
//		HashSet<TopoDS_Wire> wires = new HashSet<TopoDS_Wire>();
//		for (explorer.init(shape, TopAbs_ShapeEnum.WIRE); explorer.more(); explorer.next())
//			wires.add((TopoDS_Wire) explorer.current());
//		return wires;
//	}
//	public Collection<TopoDS_Compound> getCompounds()
//	{
//		TopExp_Explorer explorer = new TopExp_Explorer();
//		HashSet<TopoDS_Compound> compounds = new HashSet<TopoDS_Compound>();
//		for (explorer.init(shape, TopAbs_ShapeEnum.COMPOUND); explorer.more(); explorer.next())
//			compounds.add((TopoDS_Compound) explorer.current());
//		return compounds;
//	}
//	/**
//	 * Create the mesh by calling BRepMesh_IncrementalMesh
//	 * Override to call it with custom parameters
//	 */
//	protected void createMesh()
//	{
//		//Force to recreate the mesh with our parameters
//		BRepTools.clean(shape);
//		new BRepMesh_IncrementalMesh(shape, 7E-3, true);
//	}
//	public Collection<TopoDS_Face> getFaces()
//	{
//		if(!meshCreated)
//		{
//			createMesh();
//			meshCreated = true;
//		}
//		TopExp_Explorer explorer = new TopExp_Explorer();
//		HashSet<TopoDS_Face> faces = new LinkedHashSet<TopoDS_Face>();
//		for (explorer.init(shape, TopAbs_ShapeEnum.FACE); explorer.more(); explorer.next())
//			faces.add((TopoDS_Face) explorer.current());
//		return faces;
//	}
//	/**
//	 * Get only free edges
//	 * @return
//	 */
//	public Collection<TopoDS_Edge> getFreeEdges()
//	{
//		ShapeAnalysis_FreeBounds safb = new ShapeAnalysis_FreeBounds(shape);
//		TopoDS_Compound closedWires = safb.getClosedWires();
//		if(closedWires == null)
//			return Collections.EMPTY_SET;
//		TopExp_Explorer explorer = new TopExp_Explorer();
//		HashSet<TopoDS_Edge> freeEdges = new HashSet<TopoDS_Edge>();
//		for(explorer.init(closedWires, TopAbs_ShapeEnum.EDGE); explorer.more() ; explorer.next())
//			freeEdges.add((TopoDS_Edge)explorer.current());
//		return freeEdges;
//	}
//	/**
//	 * Get all the edges (free edges or not)
//	 * @return
//	 */
//	public Collection<TopoDS_Edge> getEdges()
//	{
//		TopExp_Explorer explorer = new TopExp_Explorer();
//		HashSet<TopoDS_Edge> edges = new HashSet<TopoDS_Edge>();
//		for (explorer.init(shape, TopAbs_ShapeEnum.EDGE); explorer.more(); explorer.next())
//			edges.add((TopoDS_Edge) explorer.current());
//		return edges;
//	}
//	
//	public static void setNodes(float[] nodes)
//	{
//		throw new RuntimeException("DataProvider.EMPTY is immutable");
//	}
//
//	public TopoDS_Shape getShape() {
//		return shape;
//	}
//
//} // end-of-class OCCMeshExtractor