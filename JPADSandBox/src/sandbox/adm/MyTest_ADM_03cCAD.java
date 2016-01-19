package sandbox.adm;

import java.util.ArrayList;

import org.jcae.opencascade.jni.BRepBndLib;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeEdge;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeFace;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeVertex;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeWire;
import org.jcae.opencascade.jni.BRepGProp;
import org.jcae.opencascade.jni.BRepOffsetAPI_ThruSections;
import org.jcae.opencascade.jni.BRepTools;
import org.jcae.opencascade.jni.BRep_Builder;
import org.jcae.opencascade.jni.BRep_Tool;
import org.jcae.opencascade.jni.Bnd_Box;
import org.jcae.opencascade.jni.GCPnts_UniformDeflection;
import org.jcae.opencascade.jni.GProp_GProps;
import org.jcae.opencascade.jni.GeomAPI_IntSS;
import org.jcae.opencascade.jni.GeomAPI_Interpolate;
import org.jcae.opencascade.jni.GeomAdaptor_Curve;
import org.jcae.opencascade.jni.Geom_BSplineCurve;
import org.jcae.opencascade.jni.Geom_Curve;
import org.jcae.opencascade.jni.Geom_Surface;
import org.jcae.opencascade.jni.TopAbs_ShapeEnum;
import org.jcae.opencascade.jni.TopExp_Explorer;
import org.jcae.opencascade.jni.TopoDS_Compound;
import org.jcae.opencascade.jni.TopoDS_Edge;
import org.jcae.opencascade.jni.TopoDS_Face;
import org.jcae.opencascade.jni.TopoDS_Shape;
import org.jcae.opencascade.jni.TopoDS_Vertex;
import org.jcae.opencascade.jni.TopoDS_Wire;

public class MyTest_ADM_03cCAD {

	public MyTest_ADM_03cCAD() {

		GProp_GProps property = new GProp_GProps(); // store measurements
		// If Eps is argument is absent, precision is quite poor
		
		// make a spline 1
		ArrayList<double[]> pointList_1 = new ArrayList<double[]>();

		pointList_1.add(new double[]{  0,   0,  0 });
		pointList_1.add(new double[]{  0,  10,  5 });
		pointList_1.add(new double[]{  0,  20,  0 });
		pointList_1.add(new double[]{  0,  30,  10 });
		pointList_1.add(new double[]{  0,  40,  0 });
		
		double[] points_1 = new double[3*pointList_1.size()];
		int k1 = 0;
		for (int i=0; i < pointList_1.size(); i++)
		{
			points_1[k1] = pointList_1.get(i)[0];
			k1++;
			points_1[k1] = pointList_1.get(i)[1];
			k1++;
			points_1[k1] = pointList_1.get(i)[2];
			k1++;
		}
		
		GeomAPI_Interpolate repSpline_1 = new GeomAPI_Interpolate(
				points_1, // list of points
				false, // periodic or non-periodic 
				1E-7 // tolerance
				);
		repSpline_1.Perform();
		Geom_BSplineCurve s_1 = repSpline_1.Curve();
		TopoDS_Edge spline_1 = (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(s_1).shape();

		// Display various other properties
		BRepGProp.linearProperties(spline_1, property);
		System.out.println("spline 1 length = " + property.mass());

		
		// make a spline 2
		ArrayList<double[]> pointList_2 = new ArrayList<double[]>();

		pointList_2.add(new double[]{  15,   0,  0 });
		pointList_2.add(new double[]{  15,  10,  5 });
		pointList_2.add(new double[]{  15,  20,  0 });
		pointList_2.add(new double[]{  15,  30,  -10 });
		pointList_2.add(new double[]{  15,  40,  0 });
		
		double[] points_2 = new double[3*pointList_2.size()];
		int k2 = 0;
		for (int i=0; i < pointList_2.size(); i++)
		{
			points_2[k2] = pointList_2.get(i)[0];
			k2++;
			points_2[k2] = pointList_2.get(i)[1];
			k2++;
			points_2[k2] = pointList_2.get(i)[2];
			k2++;
		}
		
		GeomAPI_Interpolate repSpline_2 = new GeomAPI_Interpolate(
				points_2, // list of points
				false, // periodic or non-periodic 
				1E-7 // tolerance
				);
		repSpline_2.Perform();
		Geom_BSplineCurve s_2 = repSpline_2.Curve();
		TopoDS_Edge spline_2 = (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(s_2).shape();

		// Display various other properties
		BRepGProp.linearProperties(spline_2, property);
		System.out.println("spline 2 length = " + property.mass());

		
		// Loft
		
		BRepOffsetAPI_ThruSections loft = new BRepOffsetAPI_ThruSections();
		
//		TopoDS_Vertex vertex1 = (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(
//				new double[]{ -10, 15, 0 }
//				).shape();
//		
//		loft.addVertex(vertex1);
		
		// make wires
		BRepBuilderAPI_MakeWire wire_1 = new BRepBuilderAPI_MakeWire();
		wire_1.add(new TopoDS_Shape[]{spline_1});
		BRepBuilderAPI_MakeWire wire_2 = new BRepBuilderAPI_MakeWire();
		wire_2.add(new TopoDS_Shape[]{spline_2});
		
		// add wires to loft structure
		loft.addWire((TopoDS_Wire)wire_1.shape());
		loft.addWire((TopoDS_Wire)wire_2.shape());
		
		loft.build();

		BRepGProp.surfaceProperties(loft.shape(), property);
		System.out.println("Loft surface area = " + property.mass());
		
		
		// The plate
		double[] p1 = new double[]{7, -1, -5};
		double[] p2 = new double[]{12, 50, -5};
		double[] p3 = new double[]{12, 50, 35};
		double[] p4 = new double[]{7, -1, 35};
		TopoDS_Edge edge1 = (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(p1,p2).shape();
		TopoDS_Edge edge2 = (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(p2,p3).shape();
		TopoDS_Edge edge3 = (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(p3,p4).shape();
		TopoDS_Edge edge4 = (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(p4,p1).shape();
		BRepBuilderAPI_MakeWire bbPlate = new BRepBuilderAPI_MakeWire();
		bbPlate.add(new TopoDS_Shape[]{edge1, edge2, edge3, edge4});
		TopoDS_Wire plateWire = (TopoDS_Wire) bbPlate.shape();
		TopoDS_Face planeFace = (TopoDS_Face) new BRepBuilderAPI_MakeFace(plateWire, true).shape();
		
		
		// intersection
		
		TopExp_Explorer faceExplorer = new TopExp_Explorer(loft.shape(), TopAbs_ShapeEnum.FACE);
		TopoDS_Face loftFace = (TopoDS_Face) faceExplorer.current();
		
		Geom_Surface surfLoft = BRep_Tool.surface(loftFace);
		Geom_Surface surfPlane = BRep_Tool.surface(planeFace);
		
		GeomAPI_IntSS intersection = new GeomAPI_IntSS();
		intersection.perform(surfLoft, surfPlane, 0.0001f);
		
    	System.out.println("Intersection done ? " + intersection.isDone());
    	System.out.println("Lines ? " + intersection.nbLines());
		
	    float[] array;
	    GeomAdaptor_Curve adaptator = null;
	    int npts = 0;
	    GCPnts_UniformDeflection deflector = null;
		if ( intersection.nbLines() > 0 )
		{
			Geom_Curve intersectionLine = intersection.line(1);
			
			double[] rangeIntersectionLine = new double[]{0, 1};
			
		    adaptator = new GeomAdaptor_Curve(intersectionLine);
		    deflector = new GCPnts_UniformDeflection();
		    
		    Bnd_Box box = new Bnd_Box(); 
			BRepBndLib.add(spline_1,box);
			double[] bbox = box.get();
	    	System.out.println("bbox length = " + bbox.length);
			double boundingBoxDeflection=0.0005*
				Math.max(Math.max(bbox[3]-bbox[0], bbox[4]-bbox[1]), bbox[5]-bbox[2]);
		    
		    deflector.initialize(adaptator, boundingBoxDeflection, rangeIntersectionLine[0], rangeIntersectionLine[1]);
		    npts = deflector.nbPoints();
	    	System.out.println("deflector npts = " + npts);

		    if(intersectionLine != null)
		    {
////		    	 Allocate one additional point at each end  = parametric value 0, 1
////		    	array = new float[(npts+2)*3];		    
		    	array = new float[(npts+1)*3];		    
		    	int j=0;
		    	double[] values = adaptator.value(rangeIntersectionLine[0]);
		    	array[j++] = (float) values[0];
		    	array[j++] = (float) values[1];
		    	array[j++] = (float) values[2];
		    	// All intermediary points
		    	for (int i=0; i<npts; ++i) {
		    		values = adaptator.value(deflector.parameter(i+1));
		    		array[j++] = (float) values[0];
		    		array[j++] = (float) values[1];
		    		array[j++] = (float) values[2];
			    	System.out.println("(" + values[0] + ", " + values[1] + ", " + values[2] + ")");
		    	}
////		    	// Add last point
////		    	values = adaptator.value(range[1]);
////		    	System.out.println("(" + values[0] + ", " + values[1] + ", " + values[2] + ")");
////		    	array[j++] = (float) values[0];
////		    	array[j++] = (float) values[1];
////		    	array[j++] = (float) values[2];
		    	System.out.println("array length = " + array.length);
		    }
		}
		
		
		
		// prepare a compound/builder pair
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound = new TopoDS_Compound();
		builder.makeCompound(compound);
		
		// add objects to compound

		builder.add(compound, loft.shape());
		builder.add(compound, planeFace);

		for(int i = 0; i < npts; ++i)
		{
			double[] point = adaptator.value(deflector.parameter(i+1));
			
			TopoDS_Vertex vertexConstraint = (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(
					point
					).shape();
			builder.add(compound, vertexConstraint);
		}

		
		// write on file
		BRepTools.write(compound, "test/test03c.brep");
		
		////////////////////////////////////////////////////////////////////////////////////////////

//		TopoDS_Vertex vertex1 = (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(
//		new double[]{ -10, 15, 0 }
//		).shape();

		
	}

}
