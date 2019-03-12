package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BSplCLib;
import opencascade.GeomAPI_PointsToBSpline;
import opencascade.GeomAbs_Shape;
import opencascade.GeomAdaptor_Curve;
import opencascade.Geom_BSplineCurve;
import opencascade.TColStd_Array1OfReal;
import opencascade.TColgp_Array1OfPnt;
import opencascade.TopoDS_Edge;
import opencascade.gp_Pnt;

public class Test42mds {

	public static void main(String[] args) {
		System.out.println("-----------------------------------------------------");
		System.out.println("--------- OCC NURBS weights control testing ---------");
		System.out.println("-----------------------------------------------------");
		
		// Initialize the shape factory
		if (OCCUtils.theFactory == null)
			OCCUtils.initCADShapeFactory();

		// Generate a simple curve
		double[] pntA = new double[] {0.0, 4.0, 0.0};
		double[] pntB = new double[] {0.0, 0.0, 1.0};
		List<double[]> pnts = new ArrayList<>();
		pnts.add(pntA);
		pnts.add(pntB);
		
		double[] inTan = new double[] {0.0, 0.0, 1.0};
		double[] finTan = new double[] {0.0, -1.0, 0.0};
		
		double[] ctrlPnt = new double[] {0.0, -1.0, 2.0};
		
		OCCGeomCurve3D curve = (OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
				pnts, false, inTan, finTan, false);
		
		GeomAdaptor_Curve adaptorCurve = curve.getAdaptorCurve();
		Geom_BSplineCurve bSplineCurve = adaptorCurve.BSpline();
		
		TColStd_Array1OfReal knotsArray = bSplineCurve.Knots();
		TColgp_Array1OfPnt polesArray = bSplineCurve.Poles();
		TColStd_Array1OfReal weightsArray = new TColStd_Array1OfReal();
		bSplineCurve.Weights(weightsArray);	
		
		System.out.println("\n\tIs weights array empty? " + (weightsArray.Length() == 0));
		
		System.out.println("\n\tNumber of knots: " + knotsArray.Length());	
		List<Double> knots = new ArrayList<>();
		System.out.println("\tKnots:");
		for (int i = 0; i < knotsArray.Length(); i++) {
			knots.add(knotsArray.Value(i+1));
			System.out.print("\t\tknot #" + (i+1) + " " + knots.get(i) + "\n");
		}
		
		System.out.println("\n\tNumber of poles: " + polesArray.Length());	
		List<double[]> poles = new ArrayList<>();
		System.out.println("\tPoles:");
		for (int i = 0; i < polesArray.Length(); i++) {
			poles.add(new double[] {
					polesArray.Value(i+1).X(), 
					polesArray.Value(i+1).Y(), 
					polesArray.Value(i+1).Z()}
			);
			System.out.print("\t\tpole #" + (i+1) + " " + Arrays.toString(poles.get(i)) + "\n");
		}
		
		System.out.println("\n\tNumber of weights: " + weightsArray.Length());	
		List<Double> weights = new ArrayList<>();
		System.out.println("\tWeights:");
		for (int i = 0; i < weightsArray.Length(); i++) {
			weights.add(weightsArray.Value(i+1));
			System.out.print("\t\tweights #" + (i+1) + " " + weights.get(i) + "\n");
		}
		
		// Set the array of weights
		bSplineCurve.SetWeight(1, 1.0);
		bSplineCurve.SetWeight(2, 1.0);
		bSplineCurve.SetWeight(3, 1.0);
		bSplineCurve.SetWeight(4, 1.0);
		
		// Export shapes
		List<OCCShape> exportShapes = new ArrayList<>();
		exportShapes.add((OCCShape) curve.edge());
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(
				new BRepBuilderAPI_MakeEdge(bSplineCurve).Edge()));
		poles.forEach(p -> exportShapes.add((OCCShape) OCCUtils.theFactory.newVertex(p)));
		
		OCCUtils.write("Test42mds", FileExtension.STEP, exportShapes);
	}
}
