package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShapeTypes;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.CADWire;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCExplorer;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCSolid;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.WingTipType;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import javaslang.Tuple2;
import javaslang.Tuple3;
import opencascade.BRepFilletAPI_MakeFillet;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Shell;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class Test34mds {

	public static void main(String[] args) {
		System.out.println("--------------------");
		System.out.println("--- Fairing test ---");
		System.out.println("--------------------");
		
		// Import the aircraft
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface wing = aircraft.getWing();
		LiftingSurface canard = aircraft.getCanard();
		LiftingSurface hTail = aircraft.getHTail();
		LiftingSurface vTail = aircraft.getVTail();	
		
		// Generate aircraft CAD shapes
		List<OCCShape> aircraftShapes = new ArrayList<>();
		
//		if (fuselage != null) {
//			fuselage.setXApexConstructionAxes(fuselage.getXApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			fuselage.setYApexConstructionAxes(fuselage.getYApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			fuselage.setZApexConstructionAxes(fuselage.getZApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			
//			aircraftShapes.addAll(AircraftCADUtils.getFuselageCAD(fuselage, 
//					7, 7, 
//					false, false, true));
//		}
//		
//		if (wing != null) {
//			wing.setXApexConstructionAxes(wing.getXApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			wing.setYApexConstructionAxes(wing.getYApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			wing.setZApexConstructionAxes(wing.getZApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			
//			aircraftShapes.addAll(AircraftCADUtils.getLiftingSurfaceCAD(wing, 
//					WingTipType.ROUNDED, 
//					false, false, true));
//		}
//		
//		if (canard != null) {
//			canard.setXApexConstructionAxes(canard.getXApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			canard.setYApexConstructionAxes(canard.getYApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			canard.setZApexConstructionAxes(canard.getZApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			
//			aircraftShapes.addAll(AircraftCADUtils.getLiftingSurfaceCAD(canard, 
//					WingTipType.ROUNDED, 
//					false, false, true));
//		}
//		
//		if (hTail != null) {
//			hTail.setXApexConstructionAxes(hTail.getXApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			hTail.setYApexConstructionAxes(hTail.getYApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			hTail.setZApexConstructionAxes(hTail.getZApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			
//			aircraftShapes.addAll(AircraftCADUtils.getLiftingSurfaceCAD(hTail, 
//					WingTipType.ROUNDED, 
//					false, false, true));
//		}
//		
//		if (vTail != null) {
//			vTail.setXApexConstructionAxes(vTail.getXApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			vTail.setYApexConstructionAxes(vTail.getYApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			vTail.setZApexConstructionAxes(vTail.getZApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
//			
//			aircraftShapes.addAll(AircraftCADUtils.getLiftingSurfaceCAD(vTail, 
//					WingTipType.ROUNDED, 
//					false, false, true));
//		}
		
		// Generate compatible fairings
		List<OCCShape> wingFairingShapes = AircraftCADUtils.getFairingShapes(fuselage, wing, 
				0.95, 0.95, 1.05, 0.25, 
				0.85, 0.45, 
				0.85, 
				false, false, true
				);
		
		aircraftShapes.addAll(wingFairingShapes);
		
		// Write the result to file
		OCCUtils.write("FairingTest_34mds", FileExtension.STEP, aircraftShapes);
		
	}

//	public static List<OCCShape> getFairingShapes(
//			Fuselage fuselage, LiftingSurface liftingSurface,
//			double frontLengthFactor, double backLengthFactor, double widthFactor, double heightFactor,
//			double heightBelowReferenceFactor, double heightAboveReferenceFactor,
//			double filletRadiusFactor,
//			boolean exportSupportShapes, boolean exportShells, boolean exportSolids		
//			) {
//		
//		// ----------------------------------------------------------
//		// Initialize the CAD shapes factory
//		// ----------------------------------------------------------
//		if (OCCUtils.theFactory == null)
//			OCCUtils.initCADShapeFactory();
//		
//		// ----------------------------------------------------------
//		// Initialize shape lists
//		// ----------------------------------------------------------
//		Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> fairingShapes = null;
//		
//		List<OCCShape> requestedShapes = new ArrayList<>();
//		List<OCCShape> supportShapes = new ArrayList<>();
//		List<OCCShape> shellShapes = new ArrayList<>();
//		List<OCCShape> solidShapes = new ArrayList<>();
//		
//		// ----------------------------------------------------------
//		// Geometric data collection
//		// ----------------------------------------------------------
//		FairingDataCollection fairingData = new FairingDataCollection(fuselage, liftingSurface, 
//				frontLengthFactor, backLengthFactor, widthFactor, heightFactor, 
//				heightBelowReferenceFactor, heightAboveReferenceFactor, 
//				filletRadiusFactor);
//		
//		// ----------------------------------------------------------
//		// Invoke a specific fairing generator method
//		// ----------------------------------------------------------
//		FairingPosition fairingPosition = fairingData.getFairingPosition();
//		System.out.println(fairingPosition.toString());
//		
//		switch (fairingPosition) {
//		
//		case DETACHED_UP:
//			fairingShapes = generateDetachedUpFairingShapes(fairingData,
//					exportSupportShapes, exportShells, exportSolids);
//			
//			break;
//			
//		case ATTACHED_UP:
//			fairingShapes = generateAttachedUpFairingShapes(fairingData,
//					exportSupportShapes, exportShells, exportSolids);
//			
//			break;
//			
//		case MIDDLE:
//			fairingShapes = generateMiddleFairingShapes(fairingData,
//					exportSupportShapes, exportShells, exportSolids);
//			
//			break;
//			
//		case ATTACHED_DOWN:
//			fairingShapes = generateAttachedDownFairingShapes(fairingData,
//					exportSupportShapes, exportShells, exportSolids);
//			
//			break;
//			
//		case DETACHED_DOWN:
//			fairingShapes = generateDetachedDownFairingShapes(fairingData,
//					exportSupportShapes, exportShells, exportSolids);
//			
//			break;
//		}
//		
//		supportShapes.addAll(fairingShapes._1());
//		shellShapes.addAll(fairingShapes._2());
//		solidShapes.addAll(fairingShapes._3());
//		
//		requestedShapes.addAll(supportShapes);
//		requestedShapes.addAll(shellShapes);
//		requestedShapes.addAll(solidShapes);
//		
//		return requestedShapes;
//	}
//	
//	public static Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> generateDetachedUpFairingShapes(
//			FairingDataCollection fairingData,
//			boolean exportSupportShapes, boolean exportShells, boolean exportSolids
//			) {
//		
//		Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> retShapes = null;
//		
//		List<OCCShape> supportShapes = new ArrayList<>();
//		List<OCCShape> shellShapes = new ArrayList<>();
//		List<OCCShape> solidShapes = new ArrayList<>();
//		
//		// -------------------------------------
//		// FAIRING sketching points generation
//		// -------------------------------------
//		double[] pntA = new double[] {
//				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ() + 
//					(fairingData.getFuselageMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
//		};
//		
//		double[] pntB = new double[] {
//				fairingData.getRootAirfoilLE()[0],
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingMaximumZ() - (fairingData.getFairingMaximumZ() - fairingData.getRootAirfoilLE()[2])*0.15
//		};
//		
//		double[] pntC = new double[] {
//				fairingData.getRootAirfoilTop()[0],
//				fairingData.getRootAirfoilTop()[1],
//				fairingData.getFairingMaximumZ()
//		};
//		
//		double[] pntD = new double[] {
//				fairingData.getRootAirfoilTE()[0],
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingMaximumZ() - (fairingData.getFairingMaximumZ() - fairingData.getRootAirfoilTE()[2])*0.90
//		};
//		
//		double[] pntE = new double[] {
//				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() + 
//					(fairingData.getFuselageMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
//		};
//		
//		double[] pntF = new double[] {
//				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() - 
//					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
//		};
//		
//		double[] pntG = new double[] {
//				fairingData.getRootAirfoilTE()[0],
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() - 
//					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
//		};
//		
//		double[] pntH = new double[] {
//				fairingData.getRootAirfoilLE()[0],
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ() - 
//					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
//		};
//		
//		double[] pntI = new double[] {
//				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ() - 
//					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
//		};
//		
//		double[] pntJ = new double[] {
//				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ()
//		};
//		
//		double[] pntK = new double[] {
//				fairingData.getRootAirfoilLE()[0],
//				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
//				fairingData.getFairingReferenceZ()
//		};
//		
//		double[] pntL = new double[] {
//				fairingData.getRootAirfoilTE()[0],
//				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
//				fairingData.getFairingReferenceZ()
//		};
//		
//		double[] pntM = new double[] {
//				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() 
//		};
//		
//		// ------------------------------------------------------
//		// FAIRING curves tangent vectors creation
//		// ------------------------------------------------------
//		float width       = (float) Math.abs(pntK[1] - pntJ[1]);
//		float height      = (float) Math.abs(pntC[2] - pntA[2]);
//		float frontLength = (float) Math.abs(pntC[0] - pntA[0]);
//		float backLength  = (float) Math.abs(pntE[0] - pntC[0]);
//			
//		PVector upperCurveInTng = new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]).sub(
//				new PVector((float) pntA[0], (float) pntA[1], (float) pntA[2]));
//		
//		upperCurveInTng.z = upperCurveInTng.z - 1.50f*upperCurveInTng.z;
//		upperCurveInTng.normalize();
//		
//		PVector upperCurveFiTng = new PVector((float) pntE[0], (float) pntE[1], (float) pntE[2]).sub(
//				new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]));
//		
//		upperCurveFiTng.z = upperCurveFiTng.z + 0.10f*upperCurveFiTng.z;
//		upperCurveFiTng.normalize();
//		
//		PVector sideCurveInTng = new PVector(0.20f,  0.98f, 0.00f).normalize();			
//		PVector sideCurveFiTng = new PVector(0.20f, -0.98f, 0.00f).normalize();
//		
//		upperCurveInTng.mult((float) Math.pow(height/frontLength, 0.60));
//		upperCurveFiTng.mult((float) Math.pow(height/backLength, 0.60));
//		sideCurveInTng.mult((float) Math.pow(width/frontLength, 0.30));
//		sideCurveFiTng.mult((float) Math.pow(width/backLength, 0.10));
//			
//		// ------------------------------------------------------
//		// FAIRING supporting curves and right patches creation
//		// ------------------------------------------------------
//		List<double[]> upperCurvePts = new ArrayList<>();
//		List<double[]> sideCurvePts = new ArrayList<>();
//		List<double[]> lowerCurvePts = new ArrayList<>();
//		
//		upperCurvePts.add(pntA);
//		upperCurvePts.add(pntB);
//		upperCurvePts.add(pntC);
//		upperCurvePts.add(pntD);
//		upperCurvePts.add(pntE);
//		
//		sideCurvePts.add(pntJ);
//		sideCurvePts.add(pntK);
//		sideCurvePts.add(pntL);
//		sideCurvePts.add(pntM);
//			
//		lowerCurvePts.add(pntI);
//		lowerCurvePts.add(pntH);
//		lowerCurvePts.add(pntG);
//		lowerCurvePts.add(pntF);
//		
//		Tuple2<List<OCCShape>, List<OCCShape>> fairingShapes = generateFairingShapes(fairingData,
//				upperCurvePts, sideCurvePts, lowerCurvePts, 
//				new PVector[] {upperCurveInTng, upperCurveFiTng}, new PVector[] {sideCurveInTng, sideCurveFiTng}, 
//				exportSupportShapes, exportShells || exportSolids);
//		
//		supportShapes.addAll(fairingShapes._1());
//		
//		if (exportShells) {
//			shellShapes.addAll(fairingShapes._2());
//		}
//		
//		if (exportSolids) {
//			OCCSolid fairingSolid = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells(
//					OCCUtils.theFactory.newShellFromAdjacentShells(
//							(CADShell) fairingShapes._2().get(0), 
//							(CADShell) fairingShapes._2().get(1)
//							));
//			
//			solidShapes.add(fairingSolid);	
//		}
//		
//		retShapes = new Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>>(supportShapes, shellShapes, solidShapes);
//		
//		return retShapes;	
//	}
//	
//	public static Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> generateAttachedUpFairingShapes(
//			FairingDataCollection fairingData,
//			boolean exportSupportShapes, boolean exportShells, boolean exportSolids
//			) {
//		
//		Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> retShapes = null;
//		
//		List<OCCShape> supportShapes = new ArrayList<>();
//		List<OCCShape> shellShapes = new ArrayList<>();
//		List<OCCShape> solidShapes = new ArrayList<>();
//		
//		// -------------------------------------
//		// FAIRING sketching points generation
//		// -------------------------------------
//		double[] pntA = new double[] {
//				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ() + 
//					(fairingData.getFuselageMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
//		};
//		
//		double[] pntB = new double[] {
//				fairingData.getRootAirfoilLE()[0],
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingMaximumZ() - (fairingData.getFairingMaximumZ() - fairingData.getRootAirfoilLE()[2])*0.15
//		};
//		
//		double[] pntC = new double[] {
//				fairingData.getRootAirfoilTop()[0],
//				fairingData.getRootAirfoilTop()[1],
//				fairingData.getFairingMaximumZ()
//		};
//		
//		double[] pntD = new double[] {
//				fairingData.getRootAirfoilTE()[0],
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingMaximumZ() - (fairingData.getFairingMaximumZ() - fairingData.getRootAirfoilTE()[2])*0.15
//		};
//		
//		double[] pntE = new double[] {
//				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() + 
//					(fairingData.getFuselageMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
//		};
//		
//		double[] pntF = new double[] {
//				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() - 
//					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
//		};
//		
//		double[] pntG = new double[] {
//				fairingData.getRootAirfoilTE()[0],
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() - 
//					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
//		};
//		
//		double[] pntH = new double[] {
//				fairingData.getRootAirfoilLE()[0],
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ() - 
//					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
//		};
//		
//		double[] pntI = new double[] {
//				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ() - 
//					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
//		};
//		
//		double[] pntJ = new double[] {
//				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ()
//		};
//		
//		double[] pntK = new double[] {
//				fairingData.getRootAirfoilLE()[0],
//				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
//				fairingData.getFairingReferenceZ()
//		};
//		
//		double[] pntL = new double[] {
//				fairingData.getRootAirfoilTE()[0],
//				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
//				fairingData.getFairingReferenceZ()
//		};
//		
//		double[] pntM = new double[] {
//				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() 
//		};
//		
//		// ------------------------------------------------------
//		// FAIRING curves tangent vectors creation
//		// ------------------------------------------------------
//		float width       = (float) Math.abs(pntK[1] - pntJ[1]);
//		float height      = (float) Math.abs(pntC[2] - pntA[2]);
//		float frontLength = (float) Math.abs(pntC[0] - pntA[0]);
//		float backLength  = (float) Math.abs(pntE[0] - pntC[0]);
//			
//		PVector upperCurveInTng = new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]).sub(
//				new PVector((float) pntA[0], (float) pntA[1], (float) pntA[2]));
//		
//		upperCurveInTng.z = upperCurveInTng.z - 1.50f*upperCurveInTng.z;
//		upperCurveInTng.normalize();
//		
//		PVector upperCurveFiTng = new PVector((float) pntE[0], (float) pntE[1], (float) pntE[2]).sub(
//				new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]));
//		
//		upperCurveFiTng.z = upperCurveFiTng.z + 0.10f*upperCurveFiTng.z;
//		upperCurveFiTng.normalize();
//		
//		PVector sideCurveInTng = new PVector(0.20f,  0.98f, 0.00f).normalize();			
//		PVector sideCurveFiTng = new PVector(0.20f, -0.98f, 0.00f).normalize();
//		
//		upperCurveInTng.mult((float) Math.pow(height/frontLength, 0.60));
//		upperCurveFiTng.mult((float) Math.pow(height/backLength, 0.60));
//		sideCurveInTng.mult((float) Math.pow(width/frontLength, 0.30));
//		sideCurveFiTng.mult((float) Math.pow(width/backLength, 0.10));
//			
//		// ------------------------------------------------------
//		// FAIRING supporting curves and right patches creation
//		// ------------------------------------------------------
//		List<double[]> upperCurvePts = new ArrayList<>();
//		List<double[]> sideCurvePts = new ArrayList<>();
//		List<double[]> lowerCurvePts = new ArrayList<>();
//		
//		upperCurvePts.add(pntA);
//		upperCurvePts.add(pntB);
//		upperCurvePts.add(pntC);
//		upperCurvePts.add(pntD);
//		upperCurvePts.add(pntE);
//		
//		sideCurvePts.add(pntJ);
//		sideCurvePts.add(pntK);
//		sideCurvePts.add(pntL);
//		sideCurvePts.add(pntM);
//			
//		lowerCurvePts.add(pntI);
//		lowerCurvePts.add(pntH);
//		lowerCurvePts.add(pntG);
//		lowerCurvePts.add(pntF);
//		
//		Tuple2<List<OCCShape>, List<OCCShape>> fairingShapes = generateFairingShapes(fairingData,
//				upperCurvePts, sideCurvePts, lowerCurvePts, 
//				new PVector[] {upperCurveInTng, upperCurveFiTng}, new PVector[] {sideCurveInTng, sideCurveFiTng}, 
//				exportSupportShapes, exportShells || exportSolids);
//		
//		supportShapes.addAll(fairingShapes._1());
//		
//		if (exportShells) {
//			shellShapes.addAll(fairingShapes._2());
//		}
//		
//		if (exportSolids) {
//			OCCSolid fairingSolid = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells(
//					OCCUtils.theFactory.newShellFromAdjacentShells(
//							(CADShell) fairingShapes._2().get(0), 
//							(CADShell) fairingShapes._2().get(1)
//							));
//			
//			solidShapes.add(fairingSolid);	
//		}
//		
//		retShapes = new Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>>(supportShapes, shellShapes, solidShapes);
//		
//		return retShapes;		
//	}
//	
//	public static Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> generateMiddleFairingShapes(
//			FairingDataCollection fairingData,
//			boolean exportSupportShapes, boolean exportShells, boolean exportSolids
//			) {
//		
//		Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> retShapes = null;
//		
//		List<OCCShape> supportShapes = new ArrayList<>();
//		List<OCCShape> shellShapes = new ArrayList<>();
//		List<OCCShape> solidShapes = new ArrayList<>();
//		
//		retShapes = new Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>>(supportShapes, shellShapes, solidShapes);
//		
//		return retShapes;
//	}
//	
//	public static Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> generateAttachedDownFairingShapes(
//			FairingDataCollection fairingData,
//			boolean exportSupportShapes, boolean exportShells, boolean exportSolids
//			) {
//		
//		Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> retShapes = null;
//		
//		List<OCCShape> supportShapes = new ArrayList<>();
//		List<OCCShape> shellShapes = new ArrayList<>();
//		List<OCCShape> solidShapes = new ArrayList<>();
//		
//		// -------------------------------------
//		// FAIRING sketching points generation
//		// -------------------------------------
//		double[] pntA = new double[] {
//				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ() - 
//					(fairingData.getFairingReferenceZ() - fairingData.getFuselageMinimumZ())*fairingData.getHeightBelowReferenceFactor()
//		};
//		
//		double[] pntB = new double[] {
//				fairingData.getRootAirfoilLE()[0],
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingMinimumZ() + (fairingData.getFuselageMinimumZ() - fairingData.getFairingMinimumZ())*0.05
//		};
//		
//		double[] pntC = new double[] {
//				fairingData.getRootAirfoilTop()[0],
//				fairingData.getRootAirfoilTop()[1],
//				fairingData.getFairingMinimumZ()
//		};
//		
//		double[] pntD = new double[] {
//				fairingData.getRootAirfoilTE()[0],
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingMinimumZ() + (fairingData.getFuselageMinimumZ() - fairingData.getFairingMinimumZ())*0.05
//		};
//		
//		double[] pntE = new double[] {
//				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() - 
//					(fairingData.getFairingReferenceZ() - fairingData.getFuselageMinimumZ())*fairingData.getHeightBelowReferenceFactor()
//		};
//		
//		double[] pntF = new double[] {
//				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() + 
//					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
//		};
//		
//		double[] pntG = new double[] {
//				fairingData.getRootAirfoilTE()[0],
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() + 
//					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
//		};
//		
//		double[] pntH = new double[] {
//				fairingData.getRootAirfoilLE()[0],
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ() + 
//					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
//		};
//		
//		double[] pntI = new double[] {
//				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ() + 
//					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
//		};
//		
//		double[] pntJ = new double[] {
//				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ()
//		};
//		
//		double[] pntK = new double[] {
//				fairingData.getRootAirfoilLE()[0],
//				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
//				fairingData.getFairingReferenceZ()
//		};
//		
//		double[] pntL = new double[] {
//				fairingData.getRootAirfoilTE()[0],
//				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
//				fairingData.getFairingReferenceZ()
//		};
//		
//		double[] pntM = new double[] {
//				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() 
//		};
//		
//		// ------------------------------------------------------
//		// FAIRING curves tangent vectors creation
//		// ------------------------------------------------------
//		float width       = (float) Math.abs(pntK[1] - pntJ[1]);
//		float height      = (float) Math.abs(pntC[2] - pntA[2]);
//		float frontLength = (float) Math.abs(pntC[0] - pntA[0]);
//		float backLength  = (float) Math.abs(pntE[0] - pntC[0]);
//			
//		PVector lowerCurveInTng = new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]).sub(
//				new PVector((float) pntA[0], (float) pntA[1], (float) pntA[2]));
//		
//		lowerCurveInTng.z = lowerCurveInTng.z + 1.50f*lowerCurveInTng.z;
//		lowerCurveInTng.normalize();
//		
//		PVector lowerCurveFiTng = new PVector((float) pntE[0], (float) pntE[1], (float) pntE[2]).sub(
//				new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]));
//		
//		lowerCurveFiTng.z = lowerCurveFiTng.z - 0.10f*lowerCurveFiTng.z;
//		lowerCurveFiTng.normalize();
//		
//		PVector sideCurveInTng = new PVector(0.20f,  0.98f, 0.00f).normalize();			
//		PVector sideCurveFiTng = new PVector(0.20f, -0.98f, 0.00f).normalize();
//		
//		lowerCurveInTng.mult((float) Math.pow(height/frontLength, 0.60));
//		lowerCurveFiTng.mult((float) Math.pow(height/backLength, 0.60));
//		sideCurveInTng.mult((float) Math.pow(width/frontLength, 0.30));
//		sideCurveFiTng.mult((float) Math.pow(width/backLength, 0.10));
//			
//		// ------------------------------------------------------
//		// FAIRING supporting curves and right patches creation
//		// ------------------------------------------------------
//		List<double[]> lowerCurvePts = new ArrayList<>();
//		List<double[]> sideCurvePts = new ArrayList<>();
//		List<double[]> upperCurvePts = new ArrayList<>();
//		
//		lowerCurvePts.add(pntA);
//		lowerCurvePts.add(pntB);
//		lowerCurvePts.add(pntC);
//		lowerCurvePts.add(pntD);
//		lowerCurvePts.add(pntE);
//		
//		sideCurvePts.add(pntJ);
//		sideCurvePts.add(pntK);
//		sideCurvePts.add(pntL);
//		sideCurvePts.add(pntM);
//			
//		upperCurvePts.add(pntI);
//		upperCurvePts.add(pntH);
//		upperCurvePts.add(pntG);
//		upperCurvePts.add(pntF);
//		
//		Tuple2<List<OCCShape>, List<OCCShape>> fairingShapes = generateFairingShapes(fairingData,
//				lowerCurvePts, sideCurvePts, upperCurvePts, 
//				new PVector[] {lowerCurveInTng, lowerCurveFiTng}, new PVector[] {sideCurveInTng, sideCurveFiTng}, 
//				exportSupportShapes, exportShells || exportSolids);
//		
//		supportShapes.addAll(fairingShapes._1());
//		
//		if (exportShells) {
//			shellShapes.addAll(fairingShapes._2());
//		}
//		
//		if (exportSolids) {
//			OCCSolid fairingSolid = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells(
//					OCCUtils.theFactory.newShellFromAdjacentShells(
//							(CADShell) fairingShapes._2().get(0), 
//							(CADShell) fairingShapes._2().get(1)
//							));
//			
//			solidShapes.add(fairingSolid);	
//		}
//		
//		retShapes = new Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>>(supportShapes, shellShapes, solidShapes);
//		
//		return retShapes;
//	}
//	
//	public static Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> generateDetachedDownFairingShapes(
//			FairingDataCollection fairingData,
//			boolean exportSupportShapes, boolean exportShells, boolean exportSolids
//			) {
//		
//		Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> retShapes = null;
//		
//		List<OCCShape> supportShapes = new ArrayList<>();
//		List<OCCShape> shellShapes = new ArrayList<>();
//		List<OCCShape> solidShapes = new ArrayList<>();
//		
//		// -------------------------------------
//		// FAIRING sketching points generation
//		// -------------------------------------
//		double[] pntA = new double[] {
//				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ() - 
//					(fairingData.getFairingReferenceZ() - fairingData.getFuselageMinimumZ())*fairingData.getHeightBelowReferenceFactor()
//		};
//		
//		double[] pntB = new double[] {
//				fairingData.getRootAirfoilLE()[0],
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingMinimumZ() + (fairingData.getFuselageMinimumZ() - fairingData.getFairingMinimumZ())*0.05
//		};
//		
//		double[] pntC = new double[] {
//				fairingData.getRootAirfoilTop()[0],
//				fairingData.getRootAirfoilTop()[1],
//				fairingData.getFairingMinimumZ()
//		};
//		
//		double[] pntD = new double[] {
//				fairingData.getRootAirfoilTE()[0],
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingMinimumZ() + (fairingData.getFuselageMinimumZ() - fairingData.getFairingMinimumZ())*0.05
//		};
//		
//		double[] pntE = new double[] {
//				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() - 
//					(fairingData.getFairingReferenceZ() - fairingData.getFuselageMinimumZ())*fairingData.getHeightBelowReferenceFactor()
//		};
//		
//		double[] pntF = new double[] {
//				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() + 
//					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
//		};
//		
//		double[] pntG = new double[] {
//				fairingData.getRootAirfoilTE()[0],
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() + 
//					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
//		};
//		
//		double[] pntH = new double[] {
//				fairingData.getRootAirfoilLE()[0],
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ() + 
//					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
//		};
//		
//		double[] pntI = new double[] {
//				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ() + 
//					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
//		};
//		
//		double[] pntJ = new double[] {
//				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
//				fairingData.getRootAirfoilLE()[1],
//				fairingData.getFairingReferenceZ()
//		};
//		
//		double[] pntK = new double[] {
//				fairingData.getRootAirfoilLE()[0],
//				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
//				fairingData.getFairingReferenceZ()
//		};
//		
//		double[] pntL = new double[] {
//				fairingData.getRootAirfoilTE()[0],
//				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
//				fairingData.getFairingReferenceZ()
//		};
//		
//		double[] pntM = new double[] {
//				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
//				fairingData.getRootAirfoilTE()[1],
//				fairingData.getFairingReferenceZ() 
//		};
//		
//		// ------------------------------------------------------
//		// FAIRING curves tangent vectors creation
//		// ------------------------------------------------------
//		float width       = (float) Math.abs(pntK[1] - pntJ[1]);
//		float height      = (float) Math.abs(pntC[2] - pntA[2]);
//		float frontLength = (float) Math.abs(pntC[0] - pntA[0]);
//		float backLength  = (float) Math.abs(pntE[0] - pntC[0]);
//			
//		PVector lowerCurveInTng = new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]).sub(
//				new PVector((float) pntA[0], (float) pntA[1], (float) pntA[2]));
//		
//		lowerCurveInTng.z = lowerCurveInTng.z + 1.50f*lowerCurveInTng.z;
//		lowerCurveInTng.normalize();
//		
//		PVector lowerCurveFiTng = new PVector((float) pntE[0], (float) pntE[1], (float) pntE[2]).sub(
//				new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]));
//		
//		lowerCurveFiTng.z = lowerCurveFiTng.z - 0.10f*lowerCurveFiTng.z;
//		lowerCurveFiTng.normalize();
//		
//		PVector sideCurveInTng = new PVector(0.20f,  0.98f, 0.00f).normalize();			
//		PVector sideCurveFiTng = new PVector(0.20f, -0.98f, 0.00f).normalize();
//		
//		lowerCurveInTng.mult((float) Math.pow(height/frontLength, 0.60));
//		lowerCurveFiTng.mult((float) Math.pow(height/backLength, 0.60));
//		sideCurveInTng.mult((float) Math.pow(width/frontLength, 0.30));
//		sideCurveFiTng.mult((float) Math.pow(width/backLength, 0.10));
//			
//		// ------------------------------------------------------
//		// FAIRING supporting curves and right patches creation
//		// ------------------------------------------------------
//		List<double[]> lowerCurvePts = new ArrayList<>();
//		List<double[]> sideCurvePts = new ArrayList<>();
//		List<double[]> upperCurvePts = new ArrayList<>();
//		
//		lowerCurvePts.add(pntA);
//		lowerCurvePts.add(pntB);
//		lowerCurvePts.add(pntC);
//		lowerCurvePts.add(pntD);
//		lowerCurvePts.add(pntE);
//		
//		sideCurvePts.add(pntJ);
//		sideCurvePts.add(pntK);
//		sideCurvePts.add(pntL);
//		sideCurvePts.add(pntM);
//			
//		upperCurvePts.add(pntI);
//		upperCurvePts.add(pntH);
//		upperCurvePts.add(pntG);
//		upperCurvePts.add(pntF);
//		
//		Tuple2<List<OCCShape>, List<OCCShape>> fairingShapes = generateFairingShapes(fairingData,
//				lowerCurvePts, sideCurvePts, upperCurvePts, 
//				new PVector[] {lowerCurveInTng, lowerCurveFiTng}, new PVector[] {sideCurveInTng, sideCurveFiTng}, 
//				exportSupportShapes, exportShells || exportSolids);
//		
//		supportShapes.addAll(fairingShapes._1());
//		
//		if (exportShells) {
//			shellShapes.addAll(fairingShapes._2());
//		}
//		
//		if (exportSolids) {
//			OCCSolid fairingSolid = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells(
//					OCCUtils.theFactory.newShellFromAdjacentShells(
//							(CADShell) fairingShapes._2().get(0), 
//							(CADShell) fairingShapes._2().get(1)
//							));
//			
//			solidShapes.add(fairingSolid);	
//		}
//		
//		retShapes = new Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>>(supportShapes, shellShapes, solidShapes);
//		
//		return retShapes;
//	}
//	
//	public static Tuple2<List<OCCShape>, List<OCCShape>> generateFairingShapes(FairingDataCollection fairingData,
//			List<double[]> mainCurvePts, List<double[]> sideCurvePts, List<double[]> supSegmPts,
//			PVector[] mainCurveTngs, PVector[] sideCurveTngs,
//			boolean exportSupportShapes, boolean exportShells
//			) {
//		
//		Tuple2<List<OCCShape>, List<OCCShape>> retShapes = null;
//		
//		List<OCCShape> supportShapes = new ArrayList<>();
//		List<OCCShape> fairingShells = new ArrayList<>();
//		
//		double[] pntA = mainCurvePts.get(0);
//		double[] pntE = mainCurvePts.get(mainCurvePts.size() - 1);
//		double[] pntI = supSegmPts.get(0);
//		double[] pntF = supSegmPts.get(supSegmPts.size() - 1);
//		
//		// Generate main supporting curves	
//		CADGeomCurve3D mainCurve = OCCUtils.theFactory.newCurve3D(
//				mainCurvePts, false, 
//				new double[] {mainCurveTngs[0].x, mainCurveTngs[0].y, mainCurveTngs[0].z}, 
//				new double[] {mainCurveTngs[1].x, mainCurveTngs[1].y, mainCurveTngs[1].z}, 
//				false);
//		
//		List<OCCEdge> mainCurves1 = OCCUtils.splitCADCurve(mainCurve, mainCurvePts.get(1));	
//		List<OCCEdge> mainCurves2 = OCCUtils.splitCADCurve(mainCurves1.get(1), mainCurvePts.get(3));
//		
//		CADGeomCurve3D mainCurve1 = OCCUtils.theFactory.newCurve3D(mainCurves1.get(0));
//		CADGeomCurve3D mainCurve2 = OCCUtils.theFactory.newCurve3D(mainCurves2.get(0));
//		CADGeomCurve3D mainCurve3 = OCCUtils.theFactory.newCurve3D(mainCurves2.get(1));
//		
//		CADGeomCurve3D sideCurve1 = OCCUtils.theFactory.newCurve3D(
//				sideCurvePts.stream().limit(2).collect(Collectors.toList()), false, 
//				new double[] {sideCurveTngs[0].x, sideCurveTngs[0].y, sideCurveTngs[0].z}, 
//				new double[] {1.0, 0.0, 0.0}, 
//				false);
//		
//		CADGeomCurve3D sideCurve2 = OCCUtils.theFactory.newCurve3D(
//				sideCurvePts.stream().skip(1).limit(2).collect(Collectors.toList()), false);
//		
//		CADGeomCurve3D sideCurve3 = OCCUtils.theFactory.newCurve3D(
//				sideCurvePts.stream().skip(2).collect(Collectors.toList()), false, 
//				new double[] {1.0, 0.0, 0.0},
//				new double[] {sideCurveTngs[1].x, sideCurveTngs[1].y, sideCurveTngs[1].z}, 			 
//				false);
//		
//		CADGeomCurve3D supSegm1 = OCCUtils.theFactory.newCurve3D(
//				supSegmPts.stream().limit(2).collect(Collectors.toList()), false);
//		
//		CADGeomCurve3D supSegm2 = OCCUtils.theFactory.newCurve3D(
//				supSegmPts.stream().skip(1).limit(2).collect(Collectors.toList()), false);
//		
//		CADGeomCurve3D supSegm3 = OCCUtils.theFactory.newCurve3D(
//				supSegmPts.stream().skip(2).collect(Collectors.toList()), false);
//		
//		// Generate vertical supporting curves
//		int nMain = 10; // number of supporting curves for first and last shell 
//		int nSide = 15; // number of interpolation point on side curves #1 and #3
//		
//		mainCurve1.discretize(nMain);
//		mainCurve2.discretize(nMain);
//		mainCurve3.discretize(nMain);
//		
//		sideCurve1.discretize(nSide);
//		sideCurve3.discretize(nSide);
//		
//		List<double[]> mainCurve1Pts = ((OCCGeomCurve3D) mainCurve1).getDiscretizedCurve().getDoublePoints();
//		List<double[]> mainCurve2Pts = ((OCCGeomCurve3D) mainCurve2).getDiscretizedCurve().getDoublePoints();
//		List<double[]> mainCurve3Pts = ((OCCGeomCurve3D) mainCurve3).getDiscretizedCurve().getDoublePoints();
//		
//		List<Double> sideCurve1XCoords = new ArrayList<>();
//		List<Double> sideCurve1YCoords = new ArrayList<>();
//		List<Double> sideCurve3XCoords = new ArrayList<>();
//		List<Double> sideCurve3YCoords = new ArrayList<>();
//		
//		((OCCGeomCurve3D) sideCurve1).getDiscretizedCurve().getDoublePoints()
//			.forEach(da -> {
//				sideCurve1XCoords.add(da[0]);
//				sideCurve1YCoords.add(da[1]);
//			});
//		
//		double sideCurve2YCoord = sideCurve2.edge().vertices()[1].pnt()[1];
//
//		((OCCGeomCurve3D) sideCurve3).getDiscretizedCurve().getDoublePoints()
//			.forEach(da -> {
//				sideCurve3XCoords.add(da[0]);
//				sideCurve3YCoords.add(da[1]);
//			});
//		
//		List<CADGeomCurve3D> mainSegms = new ArrayList<>();
//		List<CADGeomCurve3D> sideSegms = new ArrayList<>();
//		List<CADGeomCurve3D> subSegms = new ArrayList<>();
//		
//		sideSegms.add(OCCUtils.theFactory.newCurve3D(pntA, pntI));
//		sideSegms.add(OCCUtils.theFactory.newCurve3D(pntE, pntF));
//		
//		for (int i = 1; i < nMain; i++) {
//			
//			double[] mainPnt = mainCurve1Pts.get(i);		
//			double[] subPnt = new double[] {mainPnt[0], pntI[1], pntI[2]};
//			
//			Tuple2<double[], double[]> sidePts = getFairingSidePts(
//					mainPnt, subPnt, sideCurve1XCoords, sideCurve1YCoords);
//			
//			mainSegms.add(OCCUtils.theFactory.newCurve3D(mainPnt, sidePts._1()));				
//			subSegms.add(OCCUtils.theFactory.newCurve3D(subPnt, sidePts._2()));					
//			sideSegms.add(OCCUtils.theFactory.newCurve3D(sidePts._1(), sidePts._2()));		
//		}
//		
//		for (int i = 0; i < nMain; i++) {
//			
//			double[] mainPnt = mainCurve2Pts.get(i);
//			double[] subPnt = new double[] {mainPnt[0], pntI[1], pntI[2]};
//			
//			Tuple2<double[], double[]> sidePts = getFairingSidePts(
//					mainPnt, subPnt, sideCurve2YCoord);
//			
//			mainSegms.add(OCCUtils.theFactory.newCurve3D(mainPnt, sidePts._1()));		
//			subSegms.add(OCCUtils.theFactory.newCurve3D(subPnt, sidePts._2()));		
//			sideSegms.add(OCCUtils.theFactory.newCurve3D(sidePts._1(), sidePts._2()));
//		}
//		
//		for (int i = 0; i < nMain - 1; i++) {
//			
//			double[] mainPnt = mainCurve3Pts.get(i);
//			double[] subPnt = new double[] {mainPnt[0], pntF[1], pntF[2]};
//			
//			Tuple2<double[], double[]> sidePts = getFairingSidePts(
//					mainPnt, subPnt, sideCurve3XCoords, sideCurve3YCoords);
//			
//			mainSegms.add(OCCUtils.theFactory.newCurve3D(mainPnt, sidePts._1()));				
//			subSegms.add(OCCUtils.theFactory.newCurve3D(subPnt, sidePts._2()));					
//			sideSegms.add(OCCUtils.theFactory.newCurve3D(sidePts._1(), sidePts._2()));
//		}
//				
//		if (exportSupportShapes) {
//			supportShapes.add((OCCShape) mainCurve1.edge());
//			supportShapes.add((OCCShape) mainCurve2.edge());
//			supportShapes.add((OCCShape) mainCurve3.edge());
//
//			supportShapes.add((OCCShape) sideCurve1.edge());
//			supportShapes.add((OCCShape) sideCurve2.edge());
//			supportShapes.add((OCCShape) sideCurve3.edge());
//
//			supportShapes.add((OCCShape) supSegm1.edge());
//			supportShapes.add((OCCShape) supSegm2.edge());
//			supportShapes.add((OCCShape) supSegm3.edge());
//			
//			mainSegms.forEach(s -> supportShapes.add((OCCShape) s.edge()));
//			sideSegms.forEach(s -> supportShapes.add((OCCShape) s.edge()));
//			subSegms.forEach(s -> supportShapes.add((OCCShape) s.edge()));
//			
//			List<OCCShape> mirrSupportShapes = new ArrayList<>();
//			supportShapes.forEach(s -> mirrSupportShapes.add(
//					OCCUtils.getShapeMirrored(s, 
//							new PVector((float) pntA[0], (float) pntA[1], (float) pntA[2]), 
//							new PVector(0.0f, 1.0f, 0.0f), 
//							new PVector(1.0f, 0.0f, 0.0f))
//					));
//			
//			supportShapes.addAll(mirrSupportShapes);
//		}
//		
//		if (exportShells) {
//			
//			OCCShape mainPatch = OCCUtils.makePatchThruCurveSections( 
//					OCCUtils.theFactory.newVertex(pntA),
//					mainSegms, 
//					OCCUtils.theFactory.newVertex(pntE));	
//			
//			OCCShape subPatch = OCCUtils.makePatchThruCurveSections(
//					OCCUtils.theFactory.newVertex(pntI), 
//					subSegms, 
//					OCCUtils.theFactory.newVertex(pntF));	
//			
//			List<CADEdge> mainEdges = new ArrayList<>();
//			OCCExplorer expMain = new OCCExplorer();
//			expMain.init(mainPatch, CADShapeTypes.EDGE);
//			while (expMain.more()) {
//				mainEdges.add((CADEdge) expMain.current());
//				expMain.next();
//			}
//			
//			List<CADEdge> subEdges = new ArrayList<>();
//			OCCExplorer expSub = new OCCExplorer();
//			expSub.init(subPatch, CADShapeTypes.EDGE);
//			while (expSub.more()) {
//				subEdges.add((CADEdge) expSub.current());
//				expSub.next();
//			}
//			
//			OCCShape sidePatch = OCCUtils.makePatchThruCurveSections(
//					OCCUtils.theFactory.newCurve3D(mainEdges.get(1)),
//					OCCUtils.theFactory.newCurve3D(subEdges.get(1))
//			);
//			
//			OCCShell rightShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentShapes(
//					mainPatch,
//					sidePatch,
//					subPatch);
//			
//			double filletRadius = Math.abs(
//					mainCurvePts.get(0)[2] - supSegmPts.get(supSegmPts.size() - 1)[2])
//						* 0.45 * fairingData.getFilletRadiusFactor();  
//			
//			int[] edgeIndexes = (fairingData.getWidthFactor() > 1.00) ?
//					new int[] {1, 6}:
//					new int[] {1};
//			
//			OCCShell filletRightShell = OCCUtils.applyFilletOnShell(
//					rightShell, edgeIndexes, filletRadius);
//			
//			OCCShell filletLeftShell = (OCCShell) OCCUtils.getShapeMirrored(
//					filletRightShell, 
//					new PVector((float) pntA[0], (float) pntA[1], (float) pntA[2]), 
//					new PVector(0.0f, 1.0f, 0.0f), 
//					new PVector(1.0f, 0.0f, 0.0f));
//			
//			fairingShells.add(filletRightShell);
//			fairingShells.add(filletLeftShell);
//		}
//		
//		retShapes = new Tuple2<List<OCCShape>, List<OCCShape>>(supportShapes, fairingShells);
//		
//		return retShapes;
//	}
//	
//	private static Tuple2<double[], double[]> getFairingSidePts(
//			double[] mainPnt, double[] subPnt,
//			List<Double> xSide, List<Double> ySide) {
//		
//		double ySideCoord = MyMathUtils.getInterpolatedValue1DLinear(
//				MyArrayUtils.convertToDoublePrimitive(xSide), 
//				MyArrayUtils.convertToDoublePrimitive(ySide), 
//				mainPnt[0]
//				);
//		
//		double[] mainSidePnt = new double[] {
//				mainPnt[0],
//				ySideCoord,
//				mainPnt[2]
//		};
//		
//		double[] subSidePnt = new double[] {
//				mainPnt[0],
//				ySideCoord,
//				subPnt[2]
//		};
//		
//		return new Tuple2<double[], double[]>(mainSidePnt, subSidePnt);
//	}
//	
//	private static Tuple2<double[], double[]> getFairingSidePts(
//			double[] mainPnt, double[] subPnt,
//			double ySideCoord) {
//		
//		double[] mainSidePnt = new double[] {
//				mainPnt[0],
//				ySideCoord,
//				mainPnt[2]
//		};
//		
//		double[] subSidePnt = new double[] {
//				mainPnt[0],
//				ySideCoord,
//				subPnt[2]
//		};
//		
//		return new Tuple2<double[], double[]>(mainSidePnt, subSidePnt);
//	}
//	
//	public static class FairingDataCollection {
//		
//		private Fuselage _fuselage = null;
//		private LiftingSurface _liftingSurface = null;
//		
//		private double _frontLengthFactor = 0.0;
//		private double _backLengthFactor = 0.0;
//		private double _widthFactor = 0.0;
//		private double _heightFactor = 0.0;
//		private double _heightBelowReferenceFactor = 0.0;
//		private double _heightAboveReferenceFactor = 0.0;
//		private double _filletRadiusFactor = 0.0;
//		
//		private double _rootChord = 0.0;
//		private double _rootThickness = 0.0;
//		
//		private List<double[]> _rootAirfoilPts = new ArrayList<>();
//		private List<double[]> _sideAirfoilPts = new ArrayList<>(); // LS airfoil points at FUSELAGE max width
//		private List<double[]> _tipAirfoilPts = new ArrayList<>();  // LS airfoil points at FAIRING max width
//		private double[] _rootAirfoilTop = new double[3];
//		private double[] _rootAirfoilBottom = new double[3];
//		private double[] _rootAirfoilLE = new double[3];
//		private double[] _rootAirfoilTE = new double[3];
//		private double[] _sideAirfoilTop = new double[3];
//		private double[] _sideAirfoilBottom = new double[3];
//		private double[] _tipAirfoilTop = new double[3];
//		private double[] _tipAirfoilBottom = new double[3];
//
//		private PVector _fusDeltaApex = null;
//		private double[] _fuselageSCMiddleTopPnt = new double[3];
//		private double[] _fuselageSCMiddleBottomPnt = new double[3];
//		private double[] _fuselageSCFrontTopPnt = new double[3];
//		private double[] _fuselageSCFrontBottomPnt = new double[3];
//		private double[] _fuselageSCBackTopPnt = new double[3];
//		private double[] _fuselageSCBackBottomPnt = new double[3];
//		private Tuple2<List<Double>, List<Double>> _fuselageSCMiddleUpperYZCoords = null;
//		private Tuple2<List<Double>, List<Double>> _fuselageSCMiddleLowerYZCoords = null;
//		private double _fuselageMinimumZ = 0.0;
//		private double _fuselageMaximumZ = 0.0;
//		
//		private double[] _fusLSContactPnt = new double[3];
//		private double[] _fusFairingUppContactPnt = new double[3];
//		private double[] _fusFairingLowContactPnt = new double[3];
//		
//		private double _fairingMinimumZ = 0.0;
//		private double _fairingMaximumZ = 0.0;
//		private double _fairingReferenceZ = 0.0;	
//			
//		private double _frontLength = 0.0;
//		private double _backLength = 0.0;
//		private double _width = 0.0;
//		
//		private FairingPosition _fairingPosition;	
//		
//		public FairingDataCollection (Fuselage fuselage, LiftingSurface liftingSurface, 
//				double frontLengthFactor, double backLengthFactor, double widthFactor, double heightFactor,
//				double heightBelowReferenceFactor, double heightAboveContactFactor,
//				double filletRadiusFactor
//				) {
//			
//			this._fuselage = fuselage;
//			this._liftingSurface = liftingSurface;
//			
//			// -------------------------------
//			// FAIRING parameters assignment
//			// -------------------------------
//			this._frontLengthFactor = frontLengthFactor;
//			this._backLengthFactor = backLengthFactor;
//			this._widthFactor = widthFactor;
//			this._heightFactor = heightFactor;
//			this._heightBelowReferenceFactor = heightBelowReferenceFactor;
//			this._heightAboveReferenceFactor = heightAboveContactFactor;
//			this._filletRadiusFactor = filletRadiusFactor;
//			
//			// -------------------------------
//			// FAIRING reference lengths
//			// -------------------------------
//			this._rootChord = liftingSurface.getChordsBreakPoints().get(0).doubleValue(SI.METER);
//			this._rootThickness = liftingSurface.getAirfoilList().get(0).getThicknessToChordRatio()*_rootChord;
//			
//			// -------------------------------
//			// FUSELAGE delta position
//			// -------------------------------
//			this._fusDeltaApex = new PVector(
//					(float) fuselage.getXApexConstructionAxes().doubleValue(SI.METER),
//					(float) fuselage.getYApexConstructionAxes().doubleValue(SI.METER),
//					(float) fuselage.getZApexConstructionAxes().doubleValue(SI.METER)
//					);		
//			
//			// ------------------------
//			// Root reference airfoil
//			// ------------------------
//			this._rootAirfoilPts = AircraftCADUtils.generateAirfoilAtY(0, liftingSurface);
//			
//			this._rootAirfoilTop = getAirfoilTop(_rootAirfoilPts);
//			this._rootAirfoilBottom = getAirfoilBottom(_rootAirfoilPts);
//			this._rootAirfoilLE = getAirfoilLE(_rootAirfoilPts);
//			this._rootAirfoilTE = getAirfoilTE(_rootAirfoilPts);
//			
//			// --------------------------------------
//			// FUSELAGE reference points and curves
//			// --------------------------------------
//			double fusWidthAtRootAirfoilTopX = fuselage.getWidthAtX(_rootAirfoilTop[0])*0.5;
//			double fusCamberZAtRootAirfoilTopX = fuselage.getCamberZAtX(_rootAirfoilTop[0] - _fusDeltaApex.x);
//			
//			List<PVector> fuselageSCMiddle = fuselage.getUniqueValuesYZSideRCurve(
//					Amount.valueOf(_rootAirfoilTop[0] - _fusDeltaApex.x, SI.METER));	
//			List<PVector> fuselageSCFront = fuselage.getUniqueValuesYZSideRCurve(
//					Amount.valueOf((_rootAirfoilLE[0] - _frontLength) - _fusDeltaApex.x, SI.METER));
//			List<PVector> fuselageSCBack = fuselage.getUniqueValuesYZSideRCurve(
//					Amount.valueOf((_rootAirfoilTE[0] + _backLength) - _fusDeltaApex.x, SI.METER));
//
//			fuselageSCMiddle.forEach(pv -> pv.add(_fusDeltaApex));
//
//			this._fuselageSCMiddleTopPnt = new double[] {
//					fuselageSCMiddle.get(0).x,
//					fuselageSCMiddle.get(0).y,
//					fuselageSCMiddle.get(0).z
//			};
//			this._fuselageSCMiddleBottomPnt = new double[] {
//					fuselageSCMiddle.get(fuselageSCMiddle.size() - 1).x,
//					fuselageSCMiddle.get(fuselageSCMiddle.size() - 1).y,
//					fuselageSCMiddle.get(fuselageSCMiddle.size() - 1).z
//			};
//			this._fuselageSCFrontTopPnt = new double[] {
//					fuselageSCFront.get(0).x + _fusDeltaApex.x,
//					fuselageSCFront.get(0).y + _fusDeltaApex.y,
//					fuselageSCFront.get(0).z + _fusDeltaApex.z
//			};
//			this._fuselageSCFrontBottomPnt = new double[] {
//					fuselageSCFront.get(fuselageSCFront.size() - 1).x + _fusDeltaApex.x,
//					fuselageSCFront.get(fuselageSCFront.size() - 1).y + _fusDeltaApex.y,
//					fuselageSCFront.get(fuselageSCFront.size() - 1).z + _fusDeltaApex.z
//			};
//			this._fuselageSCBackTopPnt = new double[] {
//					fuselageSCBack.get(0).x + _fusDeltaApex.x,
//					fuselageSCBack.get(0).y + _fusDeltaApex.y,
//					fuselageSCBack.get(0).z + _fusDeltaApex.z
//			};
//			this._fuselageSCBackBottomPnt = new double[] {
//					fuselageSCBack.get(fuselageSCBack.size() - 1).x + _fusDeltaApex.x,
//					fuselageSCBack.get(fuselageSCBack.size() - 1).y + _fusDeltaApex.y,
//					fuselageSCBack.get(fuselageSCBack.size() - 1).z + _fusDeltaApex.z
//			};
//
//			this._fuselageMaximumZ = Math.min(_fuselageSCFrontTopPnt[2], _fuselageSCBackTopPnt[2]);
//			this._fuselageMinimumZ = Math.max(_fuselageSCFrontBottomPnt[2], _fuselageSCBackBottomPnt[2]);
//
//			List<Double> fuselageSCMiddleUpperZCoords = new ArrayList<>();
//			List<Double> fuselageSCMiddleUpperYCoords = new ArrayList<>();
//			List<Double> fuselageSCMiddleLowerZCoords = new ArrayList<>();
//			List<Double> fuselageSCMiddleLowerYCoords = new ArrayList<>();
//
//			fuselageSCMiddleLowerZCoords.add(fusCamberZAtRootAirfoilTopX);
//			fuselageSCMiddleLowerYCoords.add(fusWidthAtRootAirfoilTopX);
//
//			for (int i = 0; i < fuselageSCMiddle.size() - 1; i++) {
//				PVector pv = fuselageSCMiddle.get(i);
//
//				if (pv.z > fusCamberZAtRootAirfoilTopX) {
//					fuselageSCMiddleUpperZCoords.add((double) pv.z);
//					fuselageSCMiddleUpperYCoords.add((double) pv.y);
//				} else if (pv.z < fusCamberZAtRootAirfoilTopX) {
//					fuselageSCMiddleLowerZCoords.add((double) pv.z);
//					fuselageSCMiddleLowerYCoords.add((double) pv.y);
//				}
//			}
//
//			fuselageSCMiddleUpperZCoords.add(fusCamberZAtRootAirfoilTopX);
//			fuselageSCMiddleUpperYCoords.add(fusWidthAtRootAirfoilTopX);
//
//			this._fuselageSCMiddleUpperYZCoords = obtainMonotonicSequence(
//					fuselageSCMiddleUpperYCoords, fuselageSCMiddleUpperZCoords, true);
//
//			this._fuselageSCMiddleLowerYZCoords = obtainMonotonicSequence(
//					fuselageSCMiddleLowerYCoords, fuselageSCMiddleLowerZCoords, false);
//			
//			// ------------------------
//			// Side reference airfoil
//			// ------------------------
//			this._sideAirfoilPts = AircraftCADUtils.generateAirfoilAtY(fusWidthAtRootAirfoilTopX, liftingSurface);
//			
//			this._sideAirfoilTop = getAirfoilTop(_sideAirfoilPts);
//			this._sideAirfoilBottom = getAirfoilBottom(_sideAirfoilPts);
//			
//			// ------------------------------------------------------
//			// Check FAIRING position
//			// ------------------------------------------------------
//			_fairingPosition = checkFairingPosition();
//			
//			// ------------------------------------------------------
//			// LIFTING-SURFACE / FUSELAGE contact point calculation and 
//			// ------------------------------------------------------
//			switch (_fairingPosition) {
//
//			case ATTACHED_UP:
//				_fusLSContactPnt = new double[] {
//						_sideAirfoilBottom[0],
//						MyMathUtils.getInterpolatedValue1DSpline(
//								MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleUpperYZCoords._2())), 
//								MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleUpperYZCoords._1())), 
//								_sideAirfoilBottom[2]),
//						_sideAirfoilBottom[2]
//				};
//
//				break;
//
//			case ATTACHED_DOWN:
//				_fusLSContactPnt = new double[] {
//						_sideAirfoilTop[0],
//						MyMathUtils.getInterpolatedValue1DSpline(
//								MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleLowerYZCoords._2())), 
//								MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleLowerYZCoords._1())), 
//								_sideAirfoilTop[2]),
//						_sideAirfoilTop[2]
//				};
//				
//				break;
//				
//			default:
//
//				break;
//			}
//			
//			// ----------------------------------------
//			// Calculate fairing principal dimensions
//			// ----------------------------------------
//			this._frontLength = _frontLengthFactor*_rootChord;
//			this._backLength = _backLengthFactor*_rootChord;
//			
//			if ((_fairingPosition.equals(FairingPosition.ATTACHED_UP) || _fairingPosition.equals(FairingPosition.ATTACHED_DOWN)) 
//					&& _widthFactor < 1.0) {
//				
//				this._width = widthFactor*(fusWidthAtRootAirfoilTopX - (_fusLSContactPnt[1] - _fusDeltaApex.y)) + 
//						(_fusLSContactPnt[1] - _fusDeltaApex.y);
//				
//			} else {
//				
//				this._width = _widthFactor*fusWidthAtRootAirfoilTopX;
//			}
//			
//			// ------------------------
//			// Tip reference airfoil
//			// ------------------------
//			this._tipAirfoilPts = AircraftCADUtils.generateAirfoilAtY(_width, liftingSurface);				
//			
//			this._tipAirfoilTop = getAirfoilTop(_tipAirfoilPts);
//			this._tipAirfoilBottom = getAirfoilBottom(_tipAirfoilPts);
//	
//			// --------------------------------------------------------------------------------
//			// FUSELAGE / FAIRING contact point and maximum/minimum z coordinates calculation
//			// --------------------------------------------------------------------------------
//			if (_widthFactor < 1.0) {
//				
//				_fusFairingUppContactPnt = new double[] {
//						_tipAirfoilTop[0],
//						_width,
//						MyMathUtils.getInterpolatedValue1DSpline(
//								MyArrayUtils.convertToDoublePrimitive(_fuselageSCMiddleUpperYZCoords._1()), 
//								MyArrayUtils.convertToDoublePrimitive(_fuselageSCMiddleUpperYZCoords._2()), 
//								_width
//								)
//				};
//				
//				_fusFairingLowContactPnt = new double[] {
//						_tipAirfoilTop[0],
//						_width,
//						MyMathUtils.getInterpolatedValue1DSpline(
//								MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleLowerYZCoords._1())), 
//								MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleLowerYZCoords._2())), 
//								_width
//								)
//				};
//				
//				if (_fairingPosition.equals(FairingPosition.DETACHED_UP) || _fairingPosition.equals(FairingPosition.ATTACHED_UP)) {
//					
//					_fairingReferenceZ = _fusFairingUppContactPnt[2];
//					
//					_fairingMinimumZ = MyArrayUtils.getMax(
//							new double[] {
//									_fuselageSCFrontBottomPnt[2],
//									_fusFairingLowContactPnt[2],
//									_fuselageSCBackBottomPnt[2]
//							});
//					
//					_fuselageMaximumZ = Math.min(
//							_fuselageSCFrontTopPnt[2], 
//							_fuselageSCBackTopPnt[2]
//							);
//					
//					if (_fairingPosition.equals(FairingPosition.DETACHED_UP)) {
//						
//						_fairingMaximumZ = Math.max(_rootAirfoilTop[2], _tipAirfoilTop[2]) + _rootThickness*_heightFactor;
//						
//					} else if (_fairingPosition.equals(FairingPosition.ATTACHED_UP)) {
//						
//						_fairingMaximumZ = _rootAirfoilTop[2] + (_fuselageSCMiddleTopPnt[2] - _rootAirfoilTop[2])*_heightFactor;
//						
//					}			
//					
//				} else if (_fairingPosition.equals(FairingPosition.ATTACHED_DOWN) || _fairingPosition.equals(FairingPosition.DETACHED_DOWN)) {
//					
//					_fairingReferenceZ = _fusFairingLowContactPnt[2];
//					
//					_fairingMaximumZ = MyArrayUtils.getMin(
//							new double[] {
//									_fuselageSCFrontTopPnt[2],
//									_fusFairingUppContactPnt[2],
//									_fuselageSCBackTopPnt[2]
//							});
//					
//					_fuselageMinimumZ = Math.max(
//							_fuselageSCFrontBottomPnt[2], 
//							_fuselageSCBackBottomPnt[2]
//							);
//					
//					if (_fairingPosition.equals(FairingPosition.ATTACHED_DOWN)) {
//						
//						_fairingMinimumZ = _fuselageSCMiddleBottomPnt[2] - _rootThickness*_heightFactor;
//						
//					} else if (_fairingPosition.equals(FairingPosition.DETACHED_DOWN)) {
//						
//						_fairingMinimumZ = Math.min(_rootAirfoilBottom[2], _tipAirfoilBottom[2]) - _rootThickness*_heightFactor;
//						
//					}				
//				}
//				
//			} else {
//				
//				if (_fairingPosition.equals(FairingPosition.DETACHED_UP) || _fairingPosition.equals(FairingPosition.ATTACHED_UP)) {
//					
//					_fairingReferenceZ = fusCamberZAtRootAirfoilTopX + 
//							(Math.min(_rootAirfoilBottom[2], _tipAirfoilBottom[2]) - fusCamberZAtRootAirfoilTopX)*0.5;
//					
//					_fairingMinimumZ = MyArrayUtils.getMax(
//							new double[] {
//									_fuselageSCFrontBottomPnt[2],
//									_fuselageSCBackBottomPnt[2]
//							});
//					
//					_fuselageMaximumZ = Math.min(
//							_fuselageSCFrontTopPnt[2], 
//							_fuselageSCBackTopPnt[2]
//							);
//					
//					if (_fairingPosition.equals(FairingPosition.DETACHED_UP)) {
//						
//						_fairingMaximumZ = Math.max(_rootAirfoilTop[2], _tipAirfoilTop[2]) + _rootThickness*_heightFactor;
//						
//					} else if (_fairingPosition.equals(FairingPosition.ATTACHED_UP)) {
//						
//						_fairingMaximumZ = _rootAirfoilTop[2] + (_fuselageSCMiddleTopPnt[2] - _rootAirfoilTop[2])*_heightFactor;
//						
//					}	
//					
//				} else if (_fairingPosition.equals(FairingPosition.ATTACHED_DOWN) || _fairingPosition.equals(FairingPosition.DETACHED_DOWN)) {
//					
//					_fairingReferenceZ = fusCamberZAtRootAirfoilTopX - 
//							(fusCamberZAtRootAirfoilTopX - Math.max(_rootAirfoilTop[2], _tipAirfoilTop[2]))*0.5;
//					
//					_fairingMaximumZ = MyArrayUtils.getMin(
//							new double[] {
//									_fuselageSCFrontTopPnt[2],
//									_fuselageSCBackTopPnt[2]
//							});
//					
//					_fuselageMinimumZ = Math.max(
//							_fuselageSCFrontBottomPnt[2], 
//							_fuselageSCBackBottomPnt[2]
//							);
//					
//					if (_fairingPosition.equals(FairingPosition.ATTACHED_DOWN)) {
//						
//						_fairingMinimumZ = _fuselageSCMiddleBottomPnt[2] - _rootThickness*_heightFactor;
//						
//					} else if (_fairingPosition.equals(FairingPosition.DETACHED_DOWN)) {
//						
//						_fairingMinimumZ = Math.min(_rootAirfoilBottom[2], _tipAirfoilBottom[2]) - _rootThickness*_heightFactor;
//						
//					}				
//				}
//			}		
//		}
//		
//		private FairingPosition checkFairingPosition() {
//
//			List<PVector> fuselageSCAtTopX = _fuselage.getUniqueValuesYZSideRCurve(
//					Amount.valueOf(_sideAirfoilTop[0] - _fusDeltaApex.x, SI.METER));
//			List<PVector> fuselageSCAtBottomX = _fuselage.getUniqueValuesYZSideRCurve(
//					Amount.valueOf(_sideAirfoilBottom[0] - _fusDeltaApex.x, SI.METER));
//
//			double fuselageZTopAtTopX = fuselageSCAtTopX.get(0).add(_fusDeltaApex).z;
//			double fuselageCamberZAtTopX = _fuselage.getCamberZAtX(_sideAirfoilTop[0] - _fusDeltaApex.x) + _fusDeltaApex.z;
//			double fuselageCamberZAtBottomX = _fuselage.getCamberZAtX(_sideAirfoilBottom[0] - _fusDeltaApex.x) + _fusDeltaApex.z;
//			double fuselageZBottomAtBottomX = fuselageSCAtBottomX.get(fuselageSCAtBottomX.size() - 1).add(_fusDeltaApex).z;
//
//			if (_rootAirfoilTop[2] > fuselageZTopAtTopX) {
//				return FairingPosition.DETACHED_UP;
//
//			} else if (_sideAirfoilTop[2] < fuselageZTopAtTopX && _sideAirfoilBottom[2] > fuselageCamberZAtBottomX) {
//				return FairingPosition.ATTACHED_UP;
//			}
//
//			if (_rootAirfoilBottom[2] < fuselageZBottomAtBottomX) {
//				return FairingPosition.DETACHED_DOWN;
//
//			} else if (_sideAirfoilTop[2] < fuselageCamberZAtTopX && _sideAirfoilBottom[2] > fuselageZBottomAtBottomX) {
//				return FairingPosition.ATTACHED_DOWN;
//			}		
//
//			return FairingPosition.MIDDLE;
//		}
//		
//		private double[] getAirfoilTop(List<double[]> airfoilPts) {
//			return airfoilPts.stream().max(Comparator.comparing(pnt -> pnt[2])).get();
//		}
//		
//		private double[] getAirfoilBottom(List<double[]> airfoilPts) {
//			return airfoilPts.stream().min(Comparator.comparing(pnt -> pnt[2])).get();
//		}
//		
//		private double[] getAirfoilLE(List<double[]> airfoilPts) {
//			return airfoilPts.stream().min(Comparator.comparing(pnt -> pnt[0])).get();
//		}
//		
//		private double[] getAirfoilTE(List<double[]> airfoilPts) {
//			return airfoilPts.stream().max(Comparator.comparing(pnt -> pnt[0])).get();
//		}
//		
//		private static <T> List<T> reverseList(List<T> list) {
//			return IntStream.range(0, list.size())
//					.mapToObj(i -> list.get(list.size() - 1 - i))
//					.collect(Collectors.toCollection(ArrayList::new));
//		}
//		
//		private static Tuple2<List<Double>, List<Double>> obtainMonotonicSequence(
//				List<Double> y, List<Double> z, boolean strictlyIncreasing) {		
//			
//			List<Double> ym = new ArrayList<>();
//			List<Double> zm = new ArrayList<>();
//			
//			int n = y.size() - 1;
//			ym.add(y.get(0));
//			zm.add(z.get(0));
//			
//			int j = 0;
//			for (int i = 1; i <= n; i++) {
//				Double yt_p = y.get(i);
//				Double zt_p = z.get(i);
//				Double yt_m = ym.get(j);
//				
//				if (strictlyIncreasing) {
//					if (yt_p > yt_m) {
//						ym.add(yt_p);
//						zm.add(zt_p);
//						j++;
//					}
//				} else {
//					if (yt_p < yt_m) {
//						ym.add(yt_p);
//						zm.add(zt_p);
//						j++;
//					}
//				}				
//			}
//			
//			return new Tuple2<List<Double>, List<Double>>(ym, zm);
//		}
//		
//		public Fuselage getFuselage() {
//			return _fuselage;
//		}
//		
//		public LiftingSurface getLiftingSurface() {
//			return _liftingSurface;
//		}
//		
//		public double getFrontLengthFactor() {
//			return _frontLengthFactor;
//		}
//		
//		public double getBackLengthFactor() {
//			return _backLengthFactor;
//		}
//		
//		public double getWidthFactor() {
//			return _widthFactor;
//		}
//		
//		public double getHeightFactor() {
//			return _heightFactor;
//		}
//		
//		public double getHeightBelowReferenceFactor() {
//			return _heightBelowReferenceFactor;
//		}
//		
//		public double getHeightAboveReferenceFactor() {
//			return _heightAboveReferenceFactor;
//		}
//		
//		public double getFilletRadiusFactor() {
//			return _filletRadiusFactor;
//		}
//		
//		public double getRootChord() {
//			return _rootChord;
//		}
//		
//		public double getRootThickness() {
//			return _rootThickness;
//		}
//		
//		public List<double[]> getRootAirfoilPts() {
//			return _rootAirfoilPts;
//		}
//		
//		public List<double[]> getSideAirfoilPts() {
//			return _sideAirfoilPts;
//		}
//		
//		public List<double[]> getTipAirfoilPts() {
//			return _tipAirfoilPts;
//		}
//		
//		public double[] getRootAirfoilTop() {
//			return _rootAirfoilTop;
//		}
//		
//		public double[] getRootAirfoilBottom() {
//			return _rootAirfoilBottom;
//		}
//		
//		public double[] getRootAirfoilLE() {
//			return _rootAirfoilLE;
//		}
//		
//		public double[] getRootAirfoilTE() {
//			return _rootAirfoilTE;
//		}
//		
//		public double[] getSideAirfoilTop() {
//			return _sideAirfoilTop;
//		}
//		
//		public double[] getSideAirfoilBottom() {
//			return _sideAirfoilBottom;
//		}
//		
//		public double[] getTipAirfoilTop() {
//			return _tipAirfoilTop;
//		}
//		
//		public double[] getTipAirfoilBottom() {
//			return _tipAirfoilBottom;
//		}
//		
//		public PVector getFusDeltaApex() {
//			return _fusDeltaApex;
//		}
//		
//		public double[] getFuselageSCMiddleTop() {
//			return _fuselageSCMiddleTopPnt;
//		}
//		
//		public double[] getFuselageSCMiddleBottom() {
//			return _fuselageSCMiddleBottomPnt;
//		}
//		
//		public double[] getFuselageSCFrontTop() {
//			return _fuselageSCFrontTopPnt;
//		}
//		
//		public double[] getFuselageSCFrontBottom() {
//			return _fuselageSCFrontBottomPnt;
//		}
//		
//		public double[] getFuselageSCBackTop() {
//			return _fuselageSCBackTopPnt;
//		}
//		
//		public double[] getFuselageSCBackBottom() {
//			return _fuselageSCBackBottomPnt;
//		}
//		
//		public Tuple2<List<Double>, List<Double>> getFuselageSCMiddleUpperYZCoords() {
//			return _fuselageSCMiddleUpperYZCoords;
//		}
//		
//		public Tuple2<List<Double>, List<Double>> getFuselageSCMiddleLowerYZCoords() {
//			return _fuselageSCMiddleLowerYZCoords;
//		}
//		
//		public double getFuselageMinimumZ() {
//			return _fuselageMinimumZ;
//		}
//		
//		public double getFuselageMaximumZ() {
//			return _fuselageMaximumZ;
//		}
//
//		public double[] getFusLSContactPnt() {
//			return _fusLSContactPnt;
//		}
//		
//		public double[] getFusFairingUppContactPnt() {
//			return _fusFairingUppContactPnt;
//		}
//		
//		public double[] getFusFairingLowContactPnt() {
//			return _fusFairingLowContactPnt;
//		} 
//		
//		public double getFairingMinimumZ() {
//			return _fairingMinimumZ;
//		}
//		
//		public double getFairingMaximumZ() {
//			return _fairingMaximumZ;
//		}
//		
//		public double getFairingReferenceZ() {
//			return _fairingReferenceZ;
//		}
//		
//		public double getFairingFrontLength() {
//			return _frontLength;
//		}
//		
//		public double getFairingBackLength() {
//			return _backLength;
//		}
//		
//		public double getFairingWidth() {
//			return _width;
//		}
//		
//		public FairingPosition getFairingPosition() {
//			return _fairingPosition;
//		}		
//	}
}
