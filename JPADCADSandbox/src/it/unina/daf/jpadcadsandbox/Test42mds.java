package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.nacelles.Nacelles;
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
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
import processing.core.PVector;
import sun.management.GcInfoCompositeData;

public class Test42mds {

	public static void main(String[] args) {
		System.out.println("----------------------------------------------------------");
		System.out.println("--------- Test B-Spline control points potential ---------");
		System.out.println("----------------------------------------------------------");
		
		// Initialize the shape factory
		if (OCCUtils.theFactory == null)
			OCCUtils.initCADShapeFactory();
		
		// Draw a simple straight line
		double[] pnt1 = new double[] {0.0, 0.0, 0.0};
		double[] pnt2 = new double[] {0.0, 1.0, 2.5};
		
		OCCGeomCurve3D straightLine = (OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(pnt1, pnt2);
		Geom_BSplineCurve bSpline = straightLine.getAdaptorCurve().BSpline();
		
		printCurveData(bSpline);
		
		List<Double> knots = getCurveKnots(bSpline);
		
		bSpline.InsertKnot(knots.get(0) + (knots.get(1) - knots.get(0))/2);
		
		printCurveData(bSpline);
		
		bSpline.SetPole(2, new gp_Pnt(0.0, 1.0, 0.0));
		bSpline.RemoveKnot(2, 0, 1.0e-02);
		
		printCurveData(bSpline);
		
		List<double[]> poles = getCurvePoles(bSpline);
		List<Double> weights = getCurveWeights(bSpline);
		
		// Export shapes
		List<OCCShape> exportShapes = new ArrayList<>();
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(
				new BRepBuilderAPI_MakeEdge(bSpline).Edge()));
		poles.forEach(p -> exportShapes.add((OCCShape) OCCUtils.theFactory.newVertex(p)));
		
		OCCUtils.write("Test42mds", FileExtension.STEP, exportShapes);
		
//		// Collect nacelle geometric data
//		Aircraft aircraft = AircraftUtils.importAircraft(args);
//		Nacelles nacelles = aircraft.getNacelles();
//		NacelleCreator nacelle = nacelles.getNacellesList().get(0);
//		
//		double xApex = nacelle.getXApexConstructionAxes().doubleValue(SI.METER);
//		double yApex = nacelle.getYApexConstructionAxes().doubleValue(SI.METRE);
//		double zApex = nacelle.getZApexConstructionAxes().doubleValue(SI.METER);	
//		double xMaxRadius = nacelle.getXPositionMaximumDiameterLRF().doubleValue(SI.METER);
//		double zPosOutlet = nacelle.getZPositionOutletDiameterLRF().doubleValue(SI.METER);
//		
//		double nacelleLength = nacelle.getLength().doubleValue(SI.METER);
//		
//		double inletRadius = nacelle.getDiameterInlet().times(0.5).doubleValue(SI.METER);
//		double maxRadius = nacelle.getDiameterMax().times(0.5).doubleValue(SI.METRE);
//		double outletRadius = nacelle.getDiameterOutlet().times(0.5).doubleValue(SI.METER);
//		
//		// Assign an arbitrary inlet angle
//		double inletAngle = Amount.valueOf(3, NonSI.DEGREE_ANGLE).doubleValue(SI.RADIAN);
//		
//		// ----------------------------------------------------
//		// Draw basic outer shape points and supporting curves
//		// ----------------------------------------------------
//		
//		// Inlet segment
//		PVector apex = new PVector((float) xApex, (float) yApex, (float) zApex);
//		PVector inletUpp = PVector.add(
//				apex, 
//				new PVector(
//						(float) ((-1)*inletRadius*Math.sin(inletAngle)), 
//						0.0f,
//						(float) (inletRadius*Math.cos(inletAngle))
//						)
//				);		
//		PVector inletLow = PVector.add(
//				apex,
//				new PVector(
//						(float) (inletRadius*Math.sin(inletAngle)), 
//						0.0f,
//						(float) ((-1)*inletRadius*Math.cos(inletAngle))
//						)
//				);
//		
//		System.out.println("Inlet upper point: " + inletUpp.toString());
//		System.out.println("Inlet lower point: " + inletLow.toString());
//		
//		List<PVector> inletSegPts = new ArrayList<>();
//		inletSegPts.add(inletLow);
//		inletSegPts.add(inletUpp);
//		CADGeomCurve3D inletSeg = OCCUtils.theFactory.newCurve3DP(inletSegPts, false);
//		
//		// Max diameter segment
//		PVector maxDiamCenter = new PVector(
//				(float) (xApex + xMaxRadius), 
//				(float) yApex,
//				(float) zApex
//				);
//		PVector maxDiamUpp = PVector.add(
//				maxDiamCenter, 
//				new PVector(
//						0.0f,
//						0.0f,
//						(float) maxRadius
//						)
//				);
//		PVector maxDiamLow = PVector.add(
//				maxDiamCenter, 
//				new PVector(
//						0.0f,
//						0.0f,
//						(float) -maxRadius
//						)
//				);
//		
//		System.out.println("Max diameter upper point: " + maxDiamUpp.toString());
//		System.out.println("Max diameter lower point: " + maxDiamLow.toString());
//		
//		List<PVector> maxDiamSegPts = new ArrayList<>();
//		maxDiamSegPts.add(maxDiamLow);
//		maxDiamSegPts.add(maxDiamUpp);
//		CADGeomCurve3D maxDiamSeg = OCCUtils.theFactory.newCurve3DP(maxDiamSegPts, false);
//		
//		PVector maxDiamUppInn = PVector.add(
//				maxDiamCenter, 
//				new PVector(
//						0.0f,
//						0.0f,
//						(float) (inletRadius + 0.15*(maxRadius - inletRadius))
//						)
//				);
//		PVector maxDiamLowInn = PVector.add(
//				maxDiamCenter, 
//				new PVector(
//						0.0f,
//						0.0f,
//						(float) -(inletRadius + 0.15*(maxRadius - inletRadius))
//						)
//				);
//		
//		System.out.println("Max diameter upper inner point: " + maxDiamUppInn.toString());
//		System.out.println("Max diameter lower inner point: " + maxDiamLowInn.toString());
//		
//		// Outlet segment
//		PVector outletDiamCenter = new PVector(
//				(float) (xApex + nacelleLength),
//				(float) yApex,
//				(float) (zApex + zPosOutlet)
//				);
//		PVector outletUpp = PVector.add(
//				outletDiamCenter, 
//				new PVector(
//						0.0f, 
//						0.0f,
//						(float) outletRadius
//						)
//				);		
//		PVector outletLow = PVector.add(
//				outletDiamCenter,
//				new PVector(
//						0.0f, 
//						0.0f,
//						(float) ((-1)*outletRadius)
//						)
//				);
//		
//		System.out.println("Outlet upper point: " + outletUpp.toString());
//		System.out.println("Outlet lower point: " + outletLow.toString());
//		
//		List<PVector> outletSegPts = new ArrayList<>();
//		outletSegPts.add(outletLow);
//		outletSegPts.add(outletUpp);
//		CADGeomCurve3D outletSeg = OCCUtils.theFactory.newCurve3DP(outletSegPts, false);	
//		
//		// Design the outer casing shape
//		List<PVector> outerCasingUpperPts = new ArrayList<>();
//		List<PVector> outerCasingLowerPts = new ArrayList<>();	
//		
//		outerCasingUpperPts.add(outletUpp);
//		outerCasingUpperPts.add(maxDiamUpp);
//		outerCasingUpperPts.add(inletUpp);
//		outerCasingUpperPts.add(maxDiamUppInn);
//		outerCasingUpperPts.add(outletUpp);
//		
//		outerCasingLowerPts.add(outletLow);
//		outerCasingLowerPts.add(maxDiamLow);
//		outerCasingLowerPts.add(inletLow);
//		outerCasingLowerPts.add(maxDiamLowInn);
//		outerCasingLowerPts.add(outletLow);
//		
//		CADGeomCurve3D outerCasingUpper = OCCUtils.theFactory.newCurve3DP(outerCasingUpperPts, false);
//		CADGeomCurve3D outerCasingLower = OCCUtils.theFactory.newCurve3DP(outerCasingLowerPts, false);
//		
//		// Detect control points
//		
//		// Export to file
//		List<OCCShape> exportShapes = new ArrayList<>();
//		exportShapes.add((OCCShape) ((OCCGeomCurve3D) inletSeg).edge());
//		exportShapes.add((OCCShape) ((OCCGeomCurve3D) maxDiamSeg).edge());
//		exportShapes.add((OCCShape) ((OCCGeomCurve3D) outletSeg).edge());
//		exportShapes.add((OCCShape) ((OCCGeomCurve3D) outerCasingUpper).edge());
//		exportShapes.add((OCCShape) ((OCCGeomCurve3D) outerCasingLower).edge());
//		
//		OCCUtils.write("Test42mds", FileExtension.STEP, exportShapes);
		
//		// Generate a simple curve
//		double[] pntA = new double[] {0.0, 4.0, 0.0};
//		double[] pntB = new double[] {0.0, 0.0, 1.0};
//		List<double[]> pnts = new ArrayList<>();
//		pnts.add(pntA);
//		pnts.add(pntB);
//		
//		double[] inTan = new double[] {0.0, 0.0, 1.0};
//		double[] finTan = new double[] {0.0, -1.0, 0.0};
//		
//		double[] ctrlPnt = new double[] {0.0, -1.0, 2.0};
//		
//		OCCGeomCurve3D curve = (OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
//				pnts, false, inTan, finTan, false);
//		
//		GeomAdaptor_Curve adaptorCurve = curve.getAdaptorCurve();
//		Geom_BSplineCurve bSplineCurve = adaptorCurve.BSpline();
//		
//		TColStd_Array1OfReal knotsArray = bSplineCurve.Knots();
//		TColgp_Array1OfPnt polesArray = bSplineCurve.Poles();
//		TColStd_Array1OfReal weightsArray = new TColStd_Array1OfReal();
//		bSplineCurve.Weights(weightsArray);	
//		
//		System.out.println("\n\tIs weights array empty? " + (weightsArray.Length() == 0));
//		
//		System.out.println("\n\tNumber of knots: " + knotsArray.Length());	
//		List<Double> knots = new ArrayList<>();
//		System.out.println("\tKnots:");
//		for (int i = 0; i < knotsArray.Length(); i++) {
//			knots.add(knotsArray.Value(i+1));
//			System.out.print("\t\tknot #" + (i+1) + " " + knots.get(i) + "\n");
//		}
//		
//		System.out.println("\n\tNumber of poles: " + polesArray.Length());	
//		List<double[]> poles = new ArrayList<>();
//		System.out.println("\tPoles:");
//		for (int i = 0; i < polesArray.Length(); i++) {
//			poles.add(new double[] {
//					polesArray.Value(i+1).X(), 
//					polesArray.Value(i+1).Y(), 
//					polesArray.Value(i+1).Z()}
//			);
//			System.out.print("\t\tpole #" + (i+1) + " " + Arrays.toString(poles.get(i)) + "\n");
//		}
//		
//		System.out.println("\n\tNumber of weights: " + weightsArray.Length());	
//		List<Double> weights = new ArrayList<>();
//		System.out.println("\tWeights:");
//		for (int i = 0; i < weightsArray.Length(); i++) {
//			weights.add(weightsArray.Value(i+1));
//			System.out.print("\t\tweights #" + (i+1) + " " + weights.get(i) + "\n");
//		}
//		
//		// Set the array of weights
//		bSplineCurve.SetWeight(1, 1.0);
//		bSplineCurve.SetWeight(2, 1.0);
//		bSplineCurve.SetWeight(3, 1.0);
//		bSplineCurve.SetWeight(4, 1.0);
//		
//		// Export shapes
//		List<OCCShape> exportShapes = new ArrayList<>();
//		exportShapes.add((OCCShape) curve.edge());
//		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(
//				new BRepBuilderAPI_MakeEdge(bSplineCurve).Edge()));
//		poles.forEach(p -> exportShapes.add((OCCShape) OCCUtils.theFactory.newVertex(p)));
//		
//		OCCUtils.write("Test42mds", FileExtension.STEP, exportShapes);
	}
	
	private static List<Double> getCurveKnots(Geom_BSplineCurve bSpline) {
		
		TColStd_Array1OfReal knotsArray = bSpline.Knots();

		List<Double> knots = new ArrayList<>();
		for (int i = 0; i < knotsArray.Length(); i++) {
			knots.add(knotsArray.Value(i+1));
		}

		return knots;
	}
	
	private static List<double[]> getCurvePoles(Geom_BSplineCurve bSpline) {
		
		TColgp_Array1OfPnt polesArray = bSpline.Poles();

		List<double[]> poles = new ArrayList<>();
		for (int i = 0; i < polesArray.Length(); i++) {
			poles.add(new double[] {
					polesArray.Value(i+1).X(), 
					polesArray.Value(i+1).Y(), 
					polesArray.Value(i+1).Z()}
					);
		}
		
		return poles;
	}
	
	private static List<Double> getCurveWeights(Geom_BSplineCurve bSpline) {
		
		TColStd_Array1OfReal weightsArray = new TColStd_Array1OfReal();
		bSpline.Weights(weightsArray);	

		List<Double> weights = new ArrayList<>();
		for (int i = 0; i < weightsArray.Length(); i++) {
			weights.add(weightsArray.Value(i+1));
		}
		
		return weights;
	}
	
	private static void printCurveData(Geom_BSplineCurve bSpline) {
		
		TColStd_Array1OfReal knotsArray = bSpline.Knots();
		TColgp_Array1OfPnt polesArray = bSpline.Poles();
		TColStd_Array1OfReal weightsArray = new TColStd_Array1OfReal();
		bSpline.Weights(weightsArray);	

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
	}
}
