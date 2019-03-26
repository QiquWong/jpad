package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.unit.SI;

import org.apache.log4j.net.SyslogAppender;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.nacelles.Nacelles;
import aircraft.components.powerplant.Engine;
import aircraft.components.powerplant.PowerPlant;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.enums.WingTipType;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADFace;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADShapeTypes;
import it.unina.daf.jpadcad.occ.CADWire;
import it.unina.daf.jpadcad.occ.OCCExplorer;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCWire;
import it.unina.daf.jpadcad.occfx.OCCFXMeshExtractor;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import it.unina.daf.jpadcadsandbox.Test23mds.Cam;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import opencascade.BRepAlgoAPI_Section;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepFilletAPI_MakeFillet2d;
import opencascade.BRepOffsetAPI_MakeOffsetShape;
import opencascade.BRepOffset_Mode;
import opencascade.GeomAbs_JoinType;
import opencascade.TopoDS;
import opencascade.gp_Ax1;
import opencascade.gp_Ax2;
import opencascade.gp_Circ;
import opencascade.gp_Dir;
import opencascade.gp_Pln;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;

public class Test43mds extends Application {
	
	public static List<List<TriangleMesh>> mesh;
	
	// mouse positions
	double mousePosX;
	double mousePosY;
	double mouseOldX;
	double mouseOldY;
	double mouseDeltaX;
	double mouseDeltaY;

	// allocating new camera
	final Cam camOffset = new Cam();
	final Cam cam = new Cam();

	public static void main(String[] args) {

		System.out.println("----- TURBOFAN engine creation -----");
			
		List<OCCShape> exportShapes = new ArrayList<>();	
		boolean exportSupportShapes = false;
		boolean exportShells = false;
		boolean exportSolids = true;
		boolean generatePylons = true;
		
		// --------------------
		// Collect components
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		aircraft.getNacelles().getNacellesList().forEach(n -> {
				n.setZApexConstructionAxes(n.getZApexConstructionAxes().minus(Amount.valueOf(0.30, SI.METER)));	
				n.setXApexConstructionAxes(n.getXApexConstructionAxes().plus(Amount.valueOf(0.35, SI.METER)));
		});
		
		List<OCCShape> fuselageShapes = AircraftCADUtils.getFuselageCAD(
				aircraft.getFuselage(), 7, 7, exportSupportShapes, exportShells, exportSolids);

		List<OCCShape> wingShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				aircraft.getWing(), WingTipType.ROUNDED, exportSupportShapes, exportShells, exportSolids);

		List<OCCShape> hTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				aircraft.getHTail(), WingTipType.ROUNDED, exportSupportShapes, exportShells, exportSolids);

		List<OCCShape> vTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				aircraft.getVTail(), WingTipType.ROUNDED, exportSupportShapes, exportShells, exportSolids);	
		
		List<OCCShape> canardShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				aircraft.getCanard(), WingTipType.ROUNDED, exportSupportShapes, exportShells, exportSolids);
		
		List<OCCShape> wingFairingShapes = AircraftCADUtils.getFairingCAD(
				aircraft.getFuselage(), aircraft.getWing(), 
				0.45, 0.45, 1.01, 0.05, 0.65, 0.10, 0.95, 
				exportSupportShapes, exportShells, exportSolids);
		
		List<OCCShape> canardFairingShapes = AircraftCADUtils.getFairingCAD(
				aircraft.getFuselage(), aircraft.getCanard(), 
				0.50, 0.50, 0.50, 0.10, 0.55, 0.85, 0.75, 
				exportSupportShapes, exportShells, exportSolids);
		
		List<OCCShape> engineShapes = AircraftCADUtils.getTurbofanEnginesCAD(
				aircraft, generatePylons, exportSupportShapes, exportShells, exportSolids);
		
		exportShapes.addAll(fuselageShapes);
		exportShapes.addAll(wingShapes);
		exportShapes.addAll(hTailShapes);
		exportShapes.addAll(vTailShapes);
		exportShapes.addAll(canardShapes);
		exportShapes.addAll(wingFairingShapes);
		exportShapes.addAll(canardFairingShapes);
		exportShapes.addAll(engineShapes);
		
		OCCUtils.write("turbofan_test_01", FileExtension.STEP, exportShapes);
		
		// --------------------
		// Extract the meshes
		List<List<TriangleMesh>> triangleMeshes = exportShapes.stream()
				.map(s -> (new OCCFXMeshExtractor(s.getShape())).getFaces().stream()
						.map(f -> {
							OCCFXMeshExtractor.FaceData faceData = new OCCFXMeshExtractor.FaceData(f, true);
							faceData.load();
							return faceData.getTriangleMesh();
						})
						.collect(Collectors.toList())
						)
				.collect(Collectors.toList());
		
		mesh = triangleMeshes;
		
		System.exit(0);
		
//		launch();
	}
	
//	private static List<OCCShape> getTurbofanEngineCAD(
//			Aircraft aircraft, NacelleCreator nacelle, Engine engine, boolean generatePylon,
//			boolean exportSupportShapes, boolean exportShells, boolean exportSolids) {
//		
//		List<OCCShape> supportShapes = new ArrayList<>();
//		List<OCCShape> shellShapes = new ArrayList<>();
//		List<OCCShape> solidShapes = new ArrayList<>();
//		List<OCCShape> requestedShapes = new ArrayList<>();
//		
//		// ----------------------------------------------------------
//		// Check whether continuing with the method
//		// ----------------------------------------------------------
//		if (nacelle == null || engine == null) {		
//			System.out.println("========== [AircraftCADUtils::getTurbofanEngineCAD] Nacelle and/or engine object passed to the "
//					+ "getTurbofanEngineCAD method is null! Exiting the method ...");
//			return null;
//		}
//		
//		if (!exportSupportShapes && !exportShells && !exportSolids) {
//			System.out.println("========== [AircraftCADUtils::getTurbofanEngineCAD] No shapes to export! Exiting the method ...");
//			return null;
//		}
//		
//		// -----------------------------------
//		// Initialize lists and shape factory
//		// -----------------------------------
//		if (OCCUtils.theFactory == null)
//			OCCUtils.initCADShapeFactory();
//		
//		// ------------------------------------------
//		// Collect NACELLE and ENGINE geometric data
//		// ------------------------------------------
//		double bpr = engine.getBPR();
//		
//		double xApex = nacelle.getXApexConstructionAxes().doubleValue(SI.METER);
//		double yApex = nacelle.getYApexConstructionAxes().doubleValue(SI.METRE);
//		double zApex = nacelle.getZApexConstructionAxes().doubleValue(SI.METER);	
//		double xMaxRadius = nacelle.getXPositionMaximumDiameterLRF().doubleValue(SI.METER);
//		
//		double lengthCore = nacelle.getLength().doubleValue(SI.METER);
//		
//		double inletRadius = nacelle.getDiameterInlet().times(0.5).doubleValue(SI.METER);
//		double maxRadius = nacelle.getDiameterMax().times(0.5).doubleValue(SI.METRE);
//		double byPassOutletRadius = nacelle.getDiameterOutlet().times(0.5).doubleValue(SI.METER);
//		
//		// -----------------------
//		// Fix engine parameters
//		// -----------------------	
//		
//		// Design parameters, x axis
//		double xPercentMinInlet = 0.05;		 // All percentages referred to length core (NACELLE total length)
//		double xPercentInletCone = 0.15;
//		double xPercentFanInlet = 0.22;       
//		double xPercentFanOutlet = 0.46;
//		double xPercentOutletMin = 0.78;
//		double xPercentCowlLength = 0.86;
//		double xPercentTurbineOutlet = 0.88;
//		double xPercentNozzle = 0.17;
//		
//		// Design parameters, z axis
//		double zPercentMinInlet = 0.76;       // All percentages referred to max diameter
//		double zPercentFanInletUpp = 0.79;
//		double zPercentFanInletLow = 0.23;
//		double zPercentFanOutletUpp = 0.80;
//		double zPercentFanOutletLow = zPercentFanOutletUpp*getZPercentFanOutletLowDueToBPR(bpr);
//		double zPercentOutletMin = zPercentFanOutletLow*getZPercentOutletMinDueToBPR(bpr);
//		double zPercentOutletCore = zPercentFanOutletLow*getZPercentOutletCoreDueToBPR(bpr);
//		double zPercentTurbineOutletUpp = zPercentOutletCore*getZPercentTurbineOutletUppDueToBPR(bpr);
//		double zPercentTurbineOutletLow = zPercentOutletCore*getZPercentTurbineOutletLowDueToBPR(bpr);
//		
//		// CAD parameters
//		double byPassOutletOffset = byPassOutletRadius*(1-zPercentFanOutletUpp)*0.05;
//		double coreOutletOffset = byPassOutletOffset;
//		
//		// ---------------------------------------------------
//		// Generate sketching points for the sketching curves
//		// ---------------------------------------------------
//		double[] ptA = new double[] {xApex, yApex, zApex + inletRadius};
//		double[] ptB = new double[] {xApex + xMaxRadius, yApex, zApex + maxRadius};
//		double[] ptC = new double[] {xApex + xPercentCowlLength*lengthCore, yApex, zApex + byPassOutletRadius + byPassOutletOffset};
//		double[] ptD = new double[] {xApex + xPercentCowlLength*lengthCore, yApex, zApex + byPassOutletRadius - byPassOutletOffset};
//		double[] ptE = new double[] {xApex + xPercentFanOutlet*lengthCore, yApex, zApex + zPercentFanOutletUpp*maxRadius};
//		double[] ptF = new double[] {xApex + xPercentFanOutlet*lengthCore, yApex, zApex + zPercentFanOutletLow*maxRadius};
//		double[] ptG = new double[] {xApex + xPercentOutletMin*lengthCore, yApex, zApex + zPercentOutletMin*maxRadius};
//		double[] ptH = new double[] {xApex + lengthCore, yApex, zApex + zPercentOutletCore*maxRadius + coreOutletOffset};
//		double[] ptI = new double[] {xApex + lengthCore, yApex, zApex + zPercentOutletCore*maxRadius - coreOutletOffset};
//		double[] ptL = new double[] {xApex + xPercentTurbineOutlet*lengthCore, yApex, zApex + zPercentTurbineOutletUpp*maxRadius};
//		double[] ptM = new double[] {xApex + xPercentTurbineOutlet*lengthCore, yApex, zApex + zPercentTurbineOutletLow*maxRadius};
//		double[] ptN = new double[] {xApex + lengthCore*(1+xPercentNozzle), yApex, zApex};
//		double[] ptO = new double[] {xApex + xPercentInletCone*lengthCore, yApex, zApex};
//		double[] ptP = new double[] {xApex + xPercentFanInlet*lengthCore, yApex, zApex + zPercentFanInletLow*maxRadius};
//		double[] ptQ = new double[] {xApex + xPercentFanInlet*lengthCore, yApex, zApex + zPercentFanInletUpp*maxRadius};
//		double[] ptR = new double[] {xApex + xPercentMinInlet*lengthCore, yApex, zApex + zPercentMinInlet*maxRadius};
//		
//		// --------------------------------------------
//		// Generate tangents for the sketching curves
//		// --------------------------------------------
//		PVector pvB = new PVector((float) ptB[0], (float) ptB[1], (float) ptB[2]);
//		PVector pvC = new PVector((float) ptC[0], (float) ptC[1], (float) ptC[2]);
//		PVector pvS = new PVector((float) ptC[0], (float) ptC[1], (float) ptB[2]);
//		PVector pvT = PVector.lerp(pvB, pvS, 0.45f);
//		
//		PVector pvTngC = PVector.sub(pvC, pvT).normalize();
//		PVector pvTngD = PVector.mult(pvTngC, -1.0f);
//		
//		PVector pvG = new PVector((float) ptG[0], (float) ptG[1], (float) ptG[2]);
//		PVector pvH = new PVector((float) ptH[0], (float) ptH[1], (float) ptH[2]);
//		PVector pvU = new PVector((float) ptH[0], (float) ptH[1], (float) ptG[2]);
//		PVector pvV = PVector.lerp(pvG, pvU, 0.25f);
//		
//		PVector pvTngH = PVector.sub(pvH, pvV).normalize();
//		PVector pvTngI = PVector.mult(pvTngH, -1.0f);
//		
//		PVector pvM = new PVector((float) ptM[0], (float) ptM[1], (float) ptM[2]);
//		PVector pvN = new PVector((float) ptN[0], (float) ptN[1], (float) ptN[2]);
//		PVector pvZ = new PVector((float) ptN[0], (float) ptN[1], (float) ptM[2]);
//		PVector pvAA = PVector.lerp(pvM, pvZ, 0.25f);
//		
//		PVector pvTngN = PVector.sub(pvN, pvAA).normalize();
//		
//		PVector pvP = new PVector((float) ptP[0], (float) ptP[1], (float) ptP[2]);
//		PVector pvO = new PVector((float) ptO[0], (float) ptO[1], (float) ptO[2]);
//		PVector pvAB = new PVector((float) ptO[0], (float) ptO[1], (float) ptP[2]);
//		PVector pvAC = PVector.lerp(pvAB, pvP, 0.15f);
//		
//		PVector pvTngO = PVector.sub(pvAC, pvO).normalize();
//		
//		PVector pvR = new PVector((float) ptR[0], (float) ptR[1], (float) ptR[2]);
//		PVector pvQ = new PVector((float) ptQ[0], (float) ptQ[1], (float) ptQ[2]);
//		PVector pvAD = new PVector((float) ptQ[0], (float) ptQ[1], (float) ptR[2]);
//		PVector pvAE = PVector.lerp(pvAD, pvR, 0.50f);
//
//		PVector pvTngQ = PVector.sub(pvAE, pvQ).normalize();
//		
//		double[] tngA = new double[] {0.0, 0.0, 0.5};
//		double[] tngB = new double[] {1.0, 0.0, 0.0};
//		double[] tngC = new double[] {pvTngC.x, pvTngC.y, pvTngC.z};
//		double[] tngD = new double[] {pvTngD.x, pvTngD.y, pvTngD.z};
//		double[] tngE = new double[] {-1.0, 0.0, 0.0};
//		double[] tngF = new double[] {1.0, 0.0, 0.0};
//		double[] tngG = new double[] {1.0, 0.0, 0.0};
//		double[] tngH = new double[] {pvTngH.x, pvTngH.y, pvTngH.z};
//		double[] tngI = new double[] {pvTngI.x, pvTngI.y, pvTngI.z};
//		double[] tngL = new double[] {-1.0, 0.0, 0.0};
//		double[] tngM = new double[] {1.0, 0.0, 0.0};
//		double[] tngN = new double[] {pvTngN.x, pvTngN.y, pvTngN.z};
//		double[] tngO = new double[] {pvTngO.x, pvTngO.y, pvTngO.z};
//		double[] tngP = new double[] {1.0, 0.0, 0.0};
//		double[] tngQ = new double[] {pvTngQ.x, pvTngQ.y, pvTngQ.z};
//		double[] tngR = new double[] {-1.0, 0.0, 0.0};
//		
//		// -------------------------------------
//		// Generate sketching curves
//		// -------------------------------------
//		List<double[]> ptsCrv1 = new ArrayList<>();
//		List<double[]> ptsCrv2 = new ArrayList<>();
//		List<double[]> ptsCrv3 = new ArrayList<>();
//		List<double[]> ptsCrv4 = new ArrayList<>();
//		List<double[]> ptsCrv5 = new ArrayList<>();
//		List<double[]> ptsCrv6 = new ArrayList<>();
//		List<double[]> ptsCrv7 = new ArrayList<>();
//		List<double[]> ptsCrv8 = new ArrayList<>();
//		List<double[]> ptsCrv9 = new ArrayList<>();
//		List<double[]> ptsCrv10 = new ArrayList<>();
//		List<double[]> ptsCrv11 = new ArrayList<>();
//		List<double[]> ptsCrv12 = new ArrayList<>();
//		List<double[]> ptsCrv13 = new ArrayList<>();
//		List<double[]> ptsCrv14 = new ArrayList<>();
//		List<double[]> ptsCrv15 = new ArrayList<>();
//		List<double[]> ptsCrv16 = new ArrayList<>();
//		
//		ptsCrv1.add(ptA);	ptsCrv1.add(ptB);		
//		ptsCrv2.add(ptB);	ptsCrv2.add(ptC);	
//		ptsCrv3.add(ptC);	ptsCrv3.add(ptD);
//		ptsCrv4.add(ptD);	ptsCrv4.add(ptE);
//		ptsCrv5.add(ptE);	ptsCrv5.add(ptF);
//		ptsCrv6.add(ptF);	ptsCrv6.add(ptG);
//		ptsCrv7.add(ptG);	ptsCrv7.add(ptH);
//		ptsCrv8.add(ptH);	ptsCrv8.add(ptI);
//		ptsCrv9.add(ptI);	ptsCrv9.add(ptL);
//		ptsCrv10.add(ptL);	ptsCrv10.add(ptM);
//		ptsCrv11.add(ptM);	ptsCrv11.add(ptN);
//		ptsCrv12.add(ptN);	ptsCrv12.add(ptO);
//		ptsCrv13.add(ptO);	ptsCrv13.add(ptP);
//		ptsCrv14.add(ptP);	ptsCrv14.add(ptQ);
//		ptsCrv15.add(ptQ);	ptsCrv15.add(ptR);
//		ptsCrv16.add(ptR);	ptsCrv16.add(ptA);
//				
//		CADEdge edge1 = OCCUtils.theFactory.newCurve3D(ptsCrv1, false, tngA, tngB, false).edge();
//		CADEdge edge2 = OCCUtils.theFactory.newCurve3D(ptsCrv2, false, tngB, tngC, false).edge();
//		CADEdge edge3 = OCCUtils.theFactory.newCurve3D(ptsCrv3, false).edge();
//		CADEdge edge4 = OCCUtils.theFactory.newCurve3D(ptsCrv4, false, tngD, tngE, false).edge();
//		CADEdge edge5 = OCCUtils.theFactory.newCurve3D(ptsCrv5, false).edge();
//		CADEdge edge6 = OCCUtils.theFactory.newCurve3D(ptsCrv6, false, tngF, tngG, false).edge();
//		CADEdge edge7 = OCCUtils.theFactory.newCurve3D(ptsCrv7, false, tngG, tngH, false).edge();
//		CADEdge edge8 = OCCUtils.theFactory.newCurve3D(ptsCrv8, false).edge();
//		CADEdge edge9 = OCCUtils.theFactory.newCurve3D(ptsCrv9, false, tngI, tngL, false).edge();
//		CADEdge edge10 = OCCUtils.theFactory.newCurve3D(ptsCrv10, false).edge();
//		CADEdge edge11 = OCCUtils.theFactory.newCurve3D(ptsCrv11, false, tngM, tngN, false).edge();
//		CADEdge edge12 = OCCUtils.theFactory.newCurve3D(ptsCrv12, false).edge();
//		CADEdge edge13 = OCCUtils.theFactory.newCurve3D(ptsCrv13, false, tngO, tngP, false).edge();
//		CADEdge edge14 = OCCUtils.theFactory.newCurve3D(ptsCrv14, false).edge();
//		CADEdge edge15 = OCCUtils.theFactory.newCurve3D(ptsCrv15, false, tngQ, tngR, false).edge();
//		CADEdge edge16 = OCCUtils.theFactory.newCurve3D(ptsCrv16, false, tngR, tngA, false).edge();
//		
//		// -------------------
//		// Generate the wire
//		// -------------------
//		CADWire engineWire = OCCUtils.theFactory.newWireFromAdjacentEdges(
//				edge1, edge2, edge3, edge4, edge5, edge6, edge7, edge8, edge9, edge10, edge11, edge12, edge13, edge14, edge15, edge16
//				);
//		
//		// --------------------------------------
//		// Generate supporting wires
//		// --------------------------------------
//		List<CADWire> revolvedWires = new ArrayList<>();
//		
//		int numSuppWires = 20;
//		double[] rotAngles = MyArrayUtils.linspace(0, 2*Math.PI, numSuppWires);
//		gp_Trsf rotTrsf = new gp_Trsf();
//		gp_Ax1 rotAxis = new gp_Ax1(
//				new gp_Pnt(xApex, yApex, zApex), 
//				new gp_Dir(1.0, 0.0, 0.0)
//				);
//		
//		revolvedWires.add(engineWire);
//		for (int i = 1; i < numSuppWires; i++) {		
//			rotTrsf.SetRotation(rotAxis, rotAngles[i]);
//			revolvedWires.add(
//					(OCCWire) OCCUtils.theFactory.newShape(
//							TopoDS.ToWire(
//									new BRepBuilderAPI_Transform(
//											((OCCShape) engineWire).getShape(), 
//											rotTrsf, 
//											0).Shape()
//									)));			
//		}
//		
//		if (exportSupportShapes) {
//			supportShapes.addAll(revolvedWires.stream().map(w -> (OCCShape) w).collect(Collectors.toList()));
//		}
//		
//		if (exportShells || exportSolids) {
//			
//			// ---------------------------------
//			// Generate the solid of the engine
//			// ---------------------------------
//			OCCShell engineShell = (OCCShell) OCCUtils.makePatchThruSections(revolvedWires);	
//			
//			if (exportShells) {
//				shellShapes.add(engineShell);
//			}
//			
//			if (exportSolids) {
//				OCCShape engineSolid = (OCCShape) OCCUtils.theFactory.newSolidFromShell(engineShell);				
//				solidShapes.add(engineSolid);
//			}
//			
//		}
//		
//		// ---------------------
//		// Generate the PYLON
//		// ---------------------		
//		LiftingSurface mountingLS = null;
//		
//		switch (engine.getMountingPosition()) {
//
//		case WING:		
//			mountingLS = aircraft.getWing();
//			
//			break;
//
//		case HTAIL:
//			mountingLS = aircraft.getHTail();
//
//			break;
//
//		default:
//
//			break;
//		}
//		
//		List<double[]> airfoilCamberLinePts = AircraftCADUtils.generateCamberAtY(yApex, mountingLS);
//		
//		if (ptB[2] > airfoilCamberLinePts.get(airfoilCamberLinePts.size() - 1)[2]) {
//			 
//			System.out.println("Pylon shapes builder algorithm interrupted ...\n" + 
//							   "Pylon shapes have not been added to the engine ones!");
//		} else {
//			
//			double pylonWidthEngineMaxRadiusPercent = 0.25;
//			double pylonWidth = pylonWidthEngineMaxRadiusPercent*maxRadius;
//		
//			// ----------------------------
//			// Generate the PYLON mid wire
//			// ----------------------------
//			
//			// Assign a z offset to the outlet low wire
//			double byPassOutletZOffset = byPassOutletOffset;
//
//			CADWire byPassLowOutletWire = OCCUtils.theFactory.newWireFromAdjacentEdges(edge6, edge7);
//			double[] byPassLowOutletWireRefVtx = byPassLowOutletWire.vertices().get(0).pnt();
//			CADWire byPassLowOutletWireMid = (CADWire) OCCUtils.getShapeTranslated(
//					(OCCShape) byPassLowOutletWire, 
//					byPassLowOutletWireRefVtx, 
//					new double[] {
//							byPassLowOutletWireRefVtx[0],
//							byPassLowOutletWireRefVtx[1],
//							byPassLowOutletWireRefVtx[2] - byPassOutletZOffset
//					});		
//			
//			// Generate remaining points
//			OCCGeomCurve3D airfoilCamberLine = (OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(airfoilCamberLinePts, false);
//			airfoilCamberLine.discretize(30);
//			List<double[]> upperCurvePylonMidWirePts = airfoilCamberLine.getDiscretizedCurve().getDoublePoints().stream()
//					.skip(2).limit(26).collect(Collectors.toList());
//			
//			double[] camberLineFirstPt = upperCurvePylonMidWirePts.get(0);
//			double[] camberLineLastPt = upperCurvePylonMidWirePts.get(upperCurvePylonMidWirePts.size() - 1);
//			double[] coreOutletUppPt = byPassLowOutletWireMid.vertices().get(byPassLowOutletWireMid.vertices().size() - 1).pnt();
//			
//			double[] pt1 = upperCurvePylonMidWirePts.get(upperCurvePylonMidWirePts.size() - 1);
//			
//			double[] pt2 = (ptB[0] > (xApex + xPercentFanInlet*lengthCore) && ptB[0] < (xApex + xPercentFanOutlet*lengthCore)) ?
//					ptB : 
//					new double[] {
//							xApex + (xPercentFanOutlet*lengthCore - xPercentFanInlet*lengthCore)/2, 
//							yApex, 
//							zApex + maxRadius*0.95
//							};
//			
//			double[] pt3 = new double[] {pt2[0], yApex, byPassLowOutletWireMid.vertices().get(0).pnt()[2]};
//			
//			double[] pt4 = byPassLowOutletWireMid.vertices().get(0).pnt();
//			
//			double[] pt5 = coreOutletUppPt;		
//			
//			double[] pt6 = new double[] {
//					coreOutletUppPt[0] + 0.20*Math.abs((camberLineFirstPt[0] - camberLineLastPt[0])),
//					coreOutletUppPt[1],
//					coreOutletUppPt[2]
//			};	
//			
//			double[] pt7 = camberLineFirstPt;
//			
//			// Assign tangent vectors
//			PVector pv1 = new PVector((float) pt1[0], (float) pt1[1], (float) pt1[2]);
//			PVector pv2 = new PVector((float) pt2[0], (float) pt2[1], (float) pt2[2]);
//			PVector pv8 = (pv1.x > pv2.x) ? 
//					new PVector(pv2.x, pv1.y, pv1.z) :
//					new PVector(pv1.x, pv2.y, pv2.z);
//			PVector pv9 = (pv1.x > pv2.x) ?
//					PVector.lerp(pv8, pv1, 0.25f) :
//					PVector.lerp(pv8, pv2, 0.25f);
//					
//			PVector pvTng1 = (pv1.x > pv2.x) ?
//					new PVector(-1.0f, 0.0f, 0.0f) :
//					PVector.sub(pv9, pv1).normalize();
//					
//			PVector pvTng2 = (pv1.x > pv2.x) ? 
//					PVector.sub(pv2, pv9).normalize() :
//					PVector.sub(pv9, pv1).normalize();
//					
//			double[] tng1 = new double[] {pvTng1.x, pvTng1.y, pvTng1.z};
//			double[] tng2 = new double[] {pvTng2.x, pvTng2.y, pvTng2.z};
//			
//			// Generate edges
//			List<double[]> ptsCrvA = new ArrayList<>();
//			List<double[]> ptsCrvB = new ArrayList<>();
//			List<double[]> ptsCrvC = new ArrayList<>();
//			List<double[]> ptsCrvF = new ArrayList<>();
//			List<double[]> ptsCrvG = new ArrayList<>();
//			
//			ptsCrvA.add(pt1); 	ptsCrvA.add(pt2);
//			ptsCrvB.add(pt2);	ptsCrvB.add(pt3);
//			ptsCrvC.add(pt3);	ptsCrvC.add(pt4);
//			ptsCrvF.add(pt5);	ptsCrvF.add(pt6);
//			ptsCrvG.add(pt6);	ptsCrvG.add(pt7);
//			
//			CADEdge edgeA = OCCUtils.theFactory.newCurve3D(ptsCrvA, false, tng1, tng2, false).edge();
//			CADEdge edgeB = OCCUtils.theFactory.newCurve3D(ptsCrvB, false).edge();
//			CADEdge edgeC = OCCUtils.theFactory.newCurve3D(ptsCrvC, false).edge();
//			CADEdge edgeD = byPassLowOutletWireMid.edges().get(0);
//			CADEdge edgeE = byPassLowOutletWireMid.edges().get(1);
//			CADEdge edgeF = OCCUtils.theFactory.newCurve3D(ptsCrvF, false).edge();
//			CADEdge edgeG = OCCUtils.theFactory.newCurve3D(ptsCrvG, false).edge();
//			CADEdge edgeH = OCCUtils.theFactory.newCurve3D(upperCurvePylonMidWirePts, false).edge();
//			
//			// Generate the wire
//			CADWire pylonMidWire = OCCUtils.theFactory.newWireFromAdjacentEdges(
//					edgeA, edgeB, edgeC, edgeD, edgeE, edgeF, edgeG, edgeH);
//			
//			if (exportSupportShapes) {
//				supportShapes.add((OCCShape) pylonMidWire);
//			}
//						
//			// ---------------------------------
//			// Generate offset wire
//			// ---------------------------------
//			CADWire pylonRightWire = (CADWire) OCCUtils.getShapeTranslated((OCCShape) pylonMidWire, 
//					pylonMidWire.vertices().get(0).pnt(), 
//					new double[] {
//							pylonMidWire.vertices().get(0).pnt()[0],
//							pylonMidWire.vertices().get(0).pnt()[1] + pylonWidth/2,
//							pylonMidWire.vertices().get(0).pnt()[2]
//							}
//					);
//			
//			if (exportShells || exportSolids) {
//				
//				// -------------------------------
//				// Generate the PYLON right shell
//				// -------------------------------
//				OCCShape pylonRightShell = (OCCShape) OCCUtils.theFactory.newShellFromAdjacentShapes(
//						OCCUtils.makePatchThruSections(pylonMidWire, pylonRightWire),
//						OCCUtils.theFactory.newFacePlanar(pylonRightWire)
//						);
//							
//				OCCShape filletedPylonRightShell = OCCUtils.applyFilletOnShell(
//						(OCCShell) pylonRightShell, 
//						new int[] {2, 6, 10, 14, 18, 22, 26, 30}, 
//						0.75*pylonWidth/2
//						);
//				
//				if (exportShells) {
//					shellShapes.add(filletedPylonRightShell);
//				}
//				
//				// -----------------------------------------------------------
//				// Mirror the right shell and generate the solid of the PYLON
//				// -----------------------------------------------------------
//				OCCShape filletedLeftShell = OCCUtils.getShapeMirrored(
//						filletedPylonRightShell, 
//						new PVector((float) xApex, (float) yApex, (float) zApex), 
//						new PVector(0.0f, 1.0f, 0.0f), 
//						new PVector(1.0f, 0.0f, 0.0f)
//						);
//
//				if (exportShells) {
//					shellShapes.add(filletedLeftShell);
//				}
//
//				if (exportSolids) {
//
//					OCCShape pylonSolid = (OCCShape) OCCUtils.theFactory.newSolidFromAdjacentShapes(
//							filletedPylonRightShell, filletedLeftShell);
//
//					solidShapes.add(pylonSolid);
//				}
//				
//			}
//			
//		}
//		
//		requestedShapes.addAll(supportShapes);
//		requestedShapes.addAll(shellShapes);
//		requestedShapes.addAll(solidShapes);
//		
//		return requestedShapes;
//	}
//	
//	private static double getZPercentFanOutletLowDueToBPR(double bpr) {
//		
//		return (0.6303 - 0.0067*bpr); // BPR 9.0 --> 0.57, BPR 12.0 --> 0.55	
//	}
//	
//	private static double getZPercentOutletMinDueToBPR(double bpr) {
//		
//		return (1.15 + 0.0067*bpr); // BPR 9.0 --> 1.21, BPR 12.0 --> 1.23	
//	}
//	
//	private static double getZPercentOutletCoreDueToBPR(double bpr) {
//		
//		return (0.84 + 0.0067*bpr); // BPR 9.0 --> 0.90, BPR 12.0 --> 0.92			
//	}
//	
//	private static double getZPercentTurbineOutletUppDueToBPR(double bpr) {
//		
//		return (0.92 + 0.013*bpr); // BPR 9.0 --> 1.04, BPR 12.0 --> 1.08	
//	}
//	
//	private static double getZPercentTurbineOutletLowDueToBPR(double bpr) {
//		
//		return (0.36 + 0.03*bpr); // BPR 9.0 --> 0.63, BPR 12.0 --> 0.72	
//	}
	
	@Override 
	public void start(final Stage stage) {	
		camOffset.getChildren().add(cam);
		resetCam();
		
		// creating the parent node and the main scene		
		final Scene scene = new Scene(camOffset, 800, 800, true);
		scene.setFill(new RadialGradient(225, 0.85, 300, 300, 500, false,
                CycleMethod.NO_CYCLE, new Stop[]
                { new Stop(0f, Color.WHITE),
                  new Stop(1f, Color.LIGHTBLUE) }));
		scene.setCamera(new PerspectiveCamera());
		
		Group components = new Group();
		components.setDepthTest(DepthTest.ENABLE);
		
		// creating the mesh view		
		mesh.forEach(mL -> {
			mL.forEach(m -> {
				MeshView face = new MeshView(m);
				face.setDrawMode(DrawMode.FILL);
				components.getChildren().add(face);
			});
		});
		cam.getChildren().add(components);
		
		double halfSceneWidth = scene.getWidth()/2;
		double halfSceneHeigth = scene.getHeight()/2;
		cam.p.setX(halfSceneWidth);
		cam.ip.setX(-halfSceneWidth);
		cam.p.setY(halfSceneHeigth);
		cam.ip.setY(-halfSceneHeigth);
		
		frameCam(stage, scene);
		
		// scale, rotate and translate using the mouse
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
            	
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getX();
                mousePosY = me.getY();
                mouseDeltaX = mousePosX - mouseOldX;
                mouseDeltaY = mousePosY - mouseOldY;
                
                if (me.isAltDown() && me.isShiftDown() && me.isPrimaryButtonDown()) {
                    double rzAngle = cam.rz.getAngle();
                    cam.rz.setAngle(rzAngle - mouseDeltaX);
                }
                else if (me.isAltDown() && me.isPrimaryButtonDown()) {
                    double ryAngle = cam.ry.getAngle();
                    cam.ry.setAngle(ryAngle - mouseDeltaX);
                    double rxAngle = cam.rx.getAngle();
                    cam.rx.setAngle(rxAngle + mouseDeltaY);
                }
                else if (me.isAltDown() && me.isSecondaryButtonDown()) {
                    double scale = cam.s.getX();
                    double newScale = scale + mouseDeltaX*0.1;
                    cam.s.setX(newScale);
                    cam.s.setY(newScale);
                    cam.s.setZ(newScale);
                }
                else if (me.isAltDown() && me.isMiddleButtonDown()) {
                    double tx = cam.t.getX();
                    double ty = cam.t.getY();
                    cam.t.setX(tx + mouseDeltaX);
                    cam.t.setY(ty + mouseDeltaY);
                }                
            }
        });

		// showing the stage
		stage.setScene(scene);
		stage.setTitle("Parametric turbofan engine test");
		stage.show();
	}
	
	private void frameCam(final Stage stage, final Scene scene) {
        setCamOffset(camOffset, scene);
        setCamPivot(cam);
        setCamTranslate(cam);
        setCamScale(cam, scene);
    }
	
	private void setCamOffset(final Cam camOffset, final Scene scene) {
        double width = scene.getWidth();
        double height = scene.getHeight();
        camOffset.t.setX(width/2.0);
        camOffset.t.setY(height/2.0);
    }
	
	private void setCamScale(final Cam cam, final Scene scene) {
        final Bounds bounds = cam.getBoundsInLocal();

        double width = scene.getWidth();
        double height = scene.getHeight();

        double scaleFactor = 1.0;
        double scaleFactorY = 1.0;
        double scaleFactorX = 1.0;
        if (bounds.getWidth() > 0.0001) {
            scaleFactorX = width / bounds.getWidth() / 2.0; // / 2.0;
        }
        if (bounds.getHeight() > 0.0001) {
            scaleFactorY = height / bounds.getHeight() / 2.0; //  / 1.5;
        }
        if (scaleFactorX > scaleFactorY) {
            scaleFactor = scaleFactorY;
        } else {
            scaleFactor = scaleFactorX;
        }
        cam.s.setX(scaleFactor);
        cam.s.setY(scaleFactor);
        cam.s.setZ(scaleFactor);
    }
	
	private void setCamPivot(final Cam cam) {
        final Bounds bounds = cam.getBoundsInLocal();
        final double pivotX = bounds.getMinX() + bounds.getWidth()/2;
        final double pivotY = bounds.getMinY() + bounds.getHeight()/2;
        final double pivotZ = bounds.getMinZ() + bounds.getDepth()/2;
        cam.p.setX(pivotX);
        cam.p.setY(pivotY);
        cam.p.setZ(pivotZ);
        cam.ip.setX(-pivotX);
        cam.ip.setY(-pivotY);
        cam.ip.setZ(-pivotZ);
    }
	
	private void setCamTranslate(final Cam cam) {
        final Bounds bounds = cam.getBoundsInLocal();
        final double pivotX = bounds.getMinX() + bounds.getWidth()/2;
        final double pivotY = bounds.getMinY() + bounds.getHeight()/2;
        cam.t.setX(-pivotX);
        cam.t.setY(-pivotY);
    }
	
	private void resetCam() {
		cam.t.setX(0.0);
		cam.t.setY(0.0);
		cam.t.setZ(0.0);
		
		cam.rx.setAngle(135.0);
		cam.ry.setAngle(-15.0);
		cam.rz.setAngle(10.0);
		
		cam.s.setX(1.25);
		cam.s.setY(1.25);
		cam.s.setZ(1.25);

		cam.p.setX(0.0);
		cam.p.setY(0.0);
		cam.p.setZ(0.0);

		cam.ip.setX(0.0);
		cam.ip.setY(0.0);
		cam.ip.setZ(0.0);

		final Bounds bounds = cam.getBoundsInLocal();
		final double pivotX = bounds.getMinX() + bounds.getWidth() / 2;
		final double pivotY = bounds.getMinY() + bounds.getHeight() / 2;
		final double pivotZ = bounds.getMinZ() + bounds.getDepth() / 2;

		cam.p.setX(pivotX);
		cam.p.setY(pivotY);
		cam.p.setZ(pivotZ);

		cam.ip.setX(-pivotX);
		cam.ip.setY(-pivotY);
		cam.ip.setZ(-pivotZ);
	}
	
	private PhongMaterial setComponentColor(ComponentEnum component) {

		PhongMaterial material = new PhongMaterial();

		switch(component) {

		case FUSELAGE: 
			material.setDiffuseColor(Color.BLUE);
			material.setSpecularColor(Color.LIGHTBLUE);

			break;

		case WING:
			material.setDiffuseColor(Color.RED);
			material.setSpecularColor(Color.MAGENTA);

			break;

		case HORIZONTAL_TAIL:
			material.setDiffuseColor(Color.DARKGREEN);
			material.setSpecularColor(Color.GREEN);

			break;

		case VERTICAL_TAIL:
			material.setDiffuseColor(Color.GOLD);
			material.setSpecularColor(Color.YELLOW);

			break;

		case CANARD:
			material.setDiffuseColor(Color.BLUEVIOLET);
			material.setSpecularColor(Color.VIOLET);

			break;

		default:

			break;
		}
		
		return material;
	}
	
	private class Cam extends Group {
		Translate t  = new Translate();
		Translate p  = new Translate();
		Translate ip = new Translate();
		Rotate rx = new Rotate();
		{rx.setAxis(Rotate.X_AXIS);}
		Rotate ry = new Rotate();
		{ry.setAxis(Rotate.Y_AXIS);}
		Rotate rz = new Rotate();
		{rz.setAxis(Rotate.Z_AXIS);}
		Scale s = new Scale();
		public Cam() {super(); getTransforms().addAll(t, p, ip, rx, ry, rz, s); }
		
	}	
}
