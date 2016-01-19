package sandbox.adm;

import java.util.ArrayList;

import org.jcae.opencascade.jni.BRepBndLib;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeEdge;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeVertex;
import org.jcae.opencascade.jni.BRepGProp;
import org.jcae.opencascade.jni.BRepTools;
import org.jcae.opencascade.jni.BRep_Builder;
import org.jcae.opencascade.jni.BRep_Tool;
import org.jcae.opencascade.jni.Bnd_Box;
import org.jcae.opencascade.jni.GCPnts_UniformDeflection;
import org.jcae.opencascade.jni.GProp_GProps;
import org.jcae.opencascade.jni.GeomAPI_Interpolate;
import org.jcae.opencascade.jni.GeomAdaptor_Curve;
import org.jcae.opencascade.jni.Geom_BSplineCurve;
import org.jcae.opencascade.jni.Geom_Curve;
import org.jcae.opencascade.jni.TopoDS_Compound;
import org.jcae.opencascade.jni.TopoDS_Edge;
import org.jcae.opencascade.jni.TopoDS_Vertex;

public class MyTest_ADM_03bCAD {

	public MyTest_ADM_03bCAD() {

		// make a spline
		ArrayList<double[]> pointList_1 = new ArrayList<double[]>();

		pointList_1.add(new double[]{  15,   0,  0 });
		pointList_1.add(new double[]{  15,  10,  5 });
		pointList_1.add(new double[]{  15,  20,  0 });
		pointList_1.add(new double[]{  15,  30,  10 });
		pointList_1.add(new double[]{  15,  40,  0 });
		
		double[] points_1 = new double[3*pointList_1.size()];
		int k = 0;
		for (int i=0; i < pointList_1.size(); i++)
		{
			points_1[k] = pointList_1.get(i)[0];
			k++;
			points_1[k] = pointList_1.get(i)[1];
			k++;
			points_1[k] = pointList_1.get(i)[2];
			k++;
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
		GProp_GProps property = new GProp_GProps(); // store measurements
		// If Eps is argument is absent, precision is quite poor
		BRepGProp.linearProperties(spline_1, property);
		System.out.println("spline 1 length = " + property.mass());

		
		// try discretizations ...
		// see OCCEdgeDomain.java in jCAE ...
		
		double[] range = BRep_Tool.range(spline_1);
	    Geom_Curve gc = BRep_Tool.curve(spline_1, range);
	    GeomAdaptor_Curve adaptator = new GeomAdaptor_Curve(gc);
	    GCPnts_UniformDeflection deflector = new GCPnts_UniformDeflection();
	    
	    Bnd_Box box = new Bnd_Box(); 
		BRepBndLib.add(spline_1,box);
		double[] bbox = box.get();
    	System.out.println("bbox length = " + bbox.length);
		double boundingBoxDeflection=0.0005*
			Math.max(Math.max(bbox[3]-bbox[0], bbox[4]-bbox[1]), bbox[5]-bbox[2]);
	    
	    deflector.initialize(adaptator, boundingBoxDeflection, range[0], range[1]);
	    int npts = deflector.nbPoints();
    	System.out.println("deflector npts = " + npts);

	    float[] array;
	    if(gc!=null)
	    {
//	    	 Allocate one additional point at each end  = parametric value 0, 1
//	    	array = new float[(npts+2)*3];		    
	    	array = new float[(npts+1)*3];		    
	    	int j=0;
	    	double[] values = adaptator.value(range[0]);
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
//	    	// Add last point
//	    	values = adaptator.value(range[1]);
//	    	System.out.println("(" + values[0] + ", " + values[1] + ", " + values[2] + ")");
//	    	array[j++] = (float) values[0];
//	    	array[j++] = (float) values[1];
//	    	array[j++] = (float) values[2];
	    	System.out.println("array length = " + array.length);
	    }
		

		// prepare a compound/builder pair
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound = new TopoDS_Compound();
		builder.makeCompound(compound);
		
		// add objects to compound
		
//		builder.add(compound, spline_1);
		
		for(int i=0; i<npts; ++i)
		{
			double[] point = adaptator.value(deflector.parameter(i+1));
			
			TopoDS_Vertex vertexConstraint = (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(
					point
					).shape();
			builder.add(compound, vertexConstraint);
		}
		

		// write on file
		BRepTools.write(compound, "test/test03b.brep");
		
	}

}
