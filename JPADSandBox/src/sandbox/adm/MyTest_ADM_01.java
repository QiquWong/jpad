package sandbox.adm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jcae.opencascade.jni.BRepBuilderAPI_MakeEdge;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeVertex;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeWire;
import org.jcae.opencascade.jni.BRepBuilderAPI_NurbsConvert;
import org.jcae.opencascade.jni.BRepMesh_IncrementalMesh;
import org.jcae.opencascade.jni.BRepOffsetAPI_NormalProjection;
import org.jcae.opencascade.jni.BRepPrimAPI_MakeSphere;
import org.jcae.opencascade.jni.BRepTools;
import org.jcae.opencascade.jni.BRep_Tool;
import org.jcae.opencascade.jni.Poly_Triangulation;
import org.jcae.opencascade.jni.TopAbs_ShapeEnum;
import org.jcae.opencascade.jni.TopLoc_Location;
import org.jcae.opencascade.jni.TopoDS_Edge;
import org.jcae.opencascade.jni.TopoDS_Face;
import org.jcae.opencascade.jni.TopoDS_Shape;
import org.jcae.opencascade.jni.TopoDS_Vertex;
import org.jcae.opencascade.jni.TopoDS_Wire;


/**
 * Trim a sphere with square and mesh it.
 * The square TopoDS_Wire is refined into more than 4 TopoDS_Edge.
 * The created geometry is converted to nurbs to mesh both nurbified and
 * original shapes.
 * @see http://www.opencascade.org/org/forum/thread_22070/
 * @see http://github.com/tpaviot/oce/issues/143
 * @author Jerome Robert
 */

// see: TrimmedSphere.java


public class MyTest_ADM_01 {
	
	private static class Shape extends org.jcae.opencascade.Shape<Shape>
	{
		private final static Factory<Shape> FACTORY=new Factory<Shape>()
		{
			@Override
			public Shape create(TopoDS_Shape shape,
				Map<TopoDS_Shape, Shape> map, Shape[] parents)
			{
				return new Shape(shape, map, parents);
			}

			public Shape[] createArray(int length)
			{
				return new Shape[length];
			}

		};
		public Shape(TopoDS_Shape shape)
		{
			this(shape, new HashMap<TopoDS_Shape, Shape>(), new Shape[0]);
		}
		protected Shape(TopoDS_Shape shape, Map<TopoDS_Shape, Shape> map, Shape[] parents)
		{
			super(shape, map, parents);
		}
		@Override
		protected Factory<Shape> getFactory() {
			return FACTORY;
		}

		@Override
		protected Shape getDerived() {
			return this;
		}

		public TopoDS_Shape getImpl() {
			return impl;
		}
	}

	private static List<TopoDS_Vertex> createMeshEdge(double[] p1, double[] p2, int n)
	{
		TopoDS_Vertex[] toReturn = new TopoDS_Vertex[n];
		double[] pp1 = new double[3];
		for(int i = 0; i<n; i++)
		{
			for(int j = 0; j < 3; j++)
				pp1[j] = p1[j] + ((double)i)/n*(p2[j] - p1[j]);
			toReturn[i] = (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(pp1).shape();
		}
		return Arrays.asList(toReturn);
	}

	private static TopoDS_Edge makeEdge(TopoDS_Vertex v1, TopoDS_Vertex v2)
	{
		BRepBuilderAPI_MakeEdge me = new BRepBuilderAPI_MakeEdge(v1, v2);
		TopoDS_Edge toReturn = (TopoDS_Edge)me.shape();
		return toReturn;
	}

	private static TopoDS_Wire createWire(int n, double[] ... points)
	{
		ArrayList<TopoDS_Vertex> vertices = new ArrayList<TopoDS_Vertex>();
		for(int i = 0; i<points.length-1; i++)
			vertices.addAll(createMeshEdge(points[i], points[i+1], n));
		vertices.addAll(createMeshEdge(points[points.length-1], points[0], n));

		BRepBuilderAPI_MakeWire b = new BRepBuilderAPI_MakeWire();
		for(int i = 0; i<vertices.size()-1; i++)
			b.add(makeEdge(vertices.get(i), vertices.get(i+1)));

		b.add(makeEdge(vertices.get(vertices.size()-1), vertices.get(0)));
		return (TopoDS_Wire) b.shape();
	}

	private static TopoDS_Wire createSquare(double s, double y, int n)
	{
		TopoDS_Wire wire = createWire(n,
			new double[]{-s,y,-s},
			new double[]{s,y,-s},
			new double[]{s,y,s},
			new double[]{-s,y,s});
		return wire;
	}

	private static TopoDS_Wire wireFromCompound(TopoDS_Shape comp)
	{
		BRepBuilderAPI_MakeWire wire = new BRepBuilderAPI_MakeWire();
		int i = 0;
		for(Shape e:new Shape(comp).explore(TopAbs_ShapeEnum.EDGE))
		{
			wire.add((TopoDS_Edge)e.getImpl());
			i++;
		}
		TopoDS_Wire toReturn = (TopoDS_Wire) wire.shape();
		return (TopoDS_Wire) toReturn.reversed();
	}
	private static TopoDS_Face createTopoDSShape(boolean nurbs, int n)
	{
		TopoDS_Shape sphere = new BRepPrimAPI_MakeSphere(new double[]{0,0,0}, 3).shape();
		TopoDS_Wire square = createSquare(0.5, 2, n);
		BRepOffsetAPI_NormalProjection projector = new BRepOffsetAPI_NormalProjection(sphere);
		projector.add(square);
		projector.build();
		Shape projection = new Shape(wireFromCompound(projector.projection()));
		Shape ssphere = new Shape(sphere).getShapeFromID(1, TopAbs_ShapeEnum.FACE);
		ssphere.getShapeFromID(1, TopAbs_ShapeEnum.WIRE).remove();
		ssphere.add(projection);
		
		////////////////////////////////////////////////////////////////////
		// Put everything in a compound and write on file
		// Uncomment to test ...
		//
//		BRep_Builder bb = new BRep_Builder();
//		TopoDS_Compound compound = new TopoDS_Compound();
//		bb.makeCompound(compound);
//		bb.add(compound, sphere);
//		bb.add(compound, square);
//		bb.add(compound, projector.projection());
//		
//		TopoDS_Solid mysolid = (TopoDS_Solid) new BRepPrimAPI_MakeBox(
//				new double[]{1.0,0.0,-0.5},
//				new double[]{-1.0,0.5,0.0}).shape();
//		
//		bb.add(compound, mysolid);
//		
//		Uncomment to write on file		
//		//System.out.println("writing brep file ...");
//		//BRepTools.write(compound, "test/test.brep");
		////////////////////////////////////////////////////////////////////
		
		if(nurbs)
			return (TopoDS_Face) new BRepBuilderAPI_NurbsConvert(ssphere.getImpl()).shape();
		else
			return (TopoDS_Face) ssphere.getImpl();
	}

	private static int mesh(TopoDS_Face shape)
	{
		TopLoc_Location loc = new TopLoc_Location();
		BRepTools.clean(shape);
		new BRepMesh_IncrementalMesh(shape, 7E-3, true);
		Poly_Triangulation pt = BRep_Tool.triangulation(shape, loc);
		
		////////////////////////////////////////////////////////////////////
//		System.out.println("N. nodes: " + pt.nbNodes());
//		System.out.println("Nodes: ");
//		for (int i=0; i<pt.nbNodes(); i++)
//			System.out.println(
//					i + ": " + pt.nodes()[i] + " ___ "
//					);
//		System.out.println(
//				"\n......................"
//				);
		////////////////////////////////////////////////////////////////////
		
		return pt.triangles().length / 3;
	}
	

	public MyTest_ADM_01() {
		
		// Try this
		for(int i = 1; i<5; i++)
		{
			System.out.println(i+":");
			//double nn = mesh(createTopoDSShape(true, i));
			double ns = mesh(createTopoDSShape(false, i));
			//System.out.println(i+": "+(nn/ns));
		}
		
	}

}
