package it.unina.daf.jpadcadsandbox.gordonsurfaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADShapeFactory;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShapeFactory;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.utils.BSplineAlgorithms;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.GeomAPI_Interpolate;
import opencascade.Geom_BSplineCurve;
import opencascade.Precision;
import opencascade.TColgp_HArray1OfPnt;
import opencascade.TopoDS_Edge;
import opencascade.gp_Pnt;

public class TestGS01 {

	public static void main(String[] args) {

		System.out.println("-------------------------------------");
		System.out.println("JPADCADSandbox - Gordon Surfaces Test");
		System.out.println("-------------------------------------");
		
		//-----------------------------------------------------------
		// Create a low-level object using OCCT JavaWrapper classes
		// curve #1
		TColgp_HArray1OfPnt points1 = new TColgp_HArray1OfPnt(1, 4);
		points1.SetValue(1, new gp_Pnt( 0,  0, 0));
		points1.SetValue(2, new gp_Pnt( 0, 10, 5));
		points1.SetValue(3, new gp_Pnt( 0, 15,-5));
		points1.SetValue(4, new gp_Pnt( 0, 20, 0));
		
		double[] parameters = BSplineAlgorithms.computeParamsBSplineCurve(points1, 0., 1., 0.5);
		
		System.out.println("BSpline parameters:");
		System.out.println(Arrays.toString(parameters));
		
		CADShapeFactory.setFactory(new OCCShapeFactory());
		CADShapeFactory shapeFactory = CADShapeFactory.getFactory();

		long isPeriodic = 0;
		GeomAPI_Interpolate aNoPeriodInterpolate1 = 
				new GeomAPI_Interpolate(points1, isPeriodic, Precision.Confusion());
		aNoPeriodInterpolate1.Perform();
		Geom_BSplineCurve anInterpolationCurve1 = aNoPeriodInterpolate1.Curve();
		TopoDS_Edge spline1 = new BRepBuilderAPI_MakeEdge(anInterpolationCurve1).Edge();

		//-----------------------------------------------------------
		// Wrap the low-level object into a CADShape
		CADShape cadSpline1 = shapeFactory.newShape(spline1);

		List<OCCShape> outputShapes = new ArrayList<>();
		outputShapes.add((OCCShape)cadSpline1);
		
		// Write to a file
		String fileName = "TestGS01.brep";
		if (OCCUtils.write(fileName, outputShapes))
			System.out.println("========== [main] Output written on file: " + fileName);
		
	}

}
