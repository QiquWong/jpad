package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.unit.SI;

import org.apache.log4j.net.SyslogAppender;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.nacelles.Nacelles;
import aircraft.components.powerplant.Engine;
import aircraft.components.powerplant.PowerPlant;
import configuration.enumerations.EngineMountingPositionEnum;
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.enums.WingTipType;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADWire;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCWire;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import opencascade.BRepAlgoAPI_Section;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_Transform;
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

public class Test43mds {

	public static void main(String[] args) {

		System.out.println("----- Nacelle creation -----");
		
		// ----------------------
		// Initialize
		// ----------------------
		if (OCCUtils.theFactory == null)
			OCCUtils.initCADShapeFactory();
		
		List<OCCShape> exportShapes = new ArrayList<>();	
		
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		Nacelles nacelles = aircraft.getNacelles();
		PowerPlant powerPlant = aircraft.getPowerPlant();
		NacelleCreator nacelle = nacelles.getNacellesList().get(0);
		Engine engine = powerPlant.getEngineList().get(0);
				
		// --------------------------
		// Collect geometric data
		// --------------------------
		double bpr = engine.getBPR();
		
		double xApex = nacelle.getXApexConstructionAxes().doubleValue(SI.METER);
		double yApex = nacelle.getYApexConstructionAxes().doubleValue(SI.METRE);
		double zApex = nacelle.getZApexConstructionAxes().doubleValue(SI.METER);	
		double xMaxRadius = nacelle.getXPositionMaximumDiameterLRF().doubleValue(SI.METER);
		
		double lengthCore = nacelle.getLength().doubleValue(SI.METER);
		
		double inletRadius = nacelle.getDiameterInlet().times(0.5).doubleValue(SI.METER);
		double maxRadius = nacelle.getDiameterMax().times(0.5).doubleValue(SI.METRE);
		double byPassOutletRadius = nacelle.getDiameterOutlet().times(0.5).doubleValue(SI.METER);
		
		// -----------------------
		// Fix engine parameters
		// -----------------------
		
		// Design parameters, x axis
		double xPercentMinInlet = 0.05;		 // All percentages referred to length core (NACELLE total length)
		double xPercentInletCone = 0.15;
		double xPercentFanInlet = 0.22;       
		double xPercentFanOutlet = 0.46;
		double xPercentOutletMin = 0.78;
		double xPercentCowlLength = 0.86;
		double xPercentTurbineOutlet = 0.88;
		double xPercentNozzle = 0.17;
		
		// Design parameters, z axis
		double zPercentMinInlet = 0.76;       // All percentages referred to max diameter
		double zPercentFanInletUpp = 0.79;
		double zPercentFanInletLow = 0.23;
		double zPercentFanOutletUpp = 0.80;
		double zPercentFanOutletLow = zPercentFanOutletUpp*getZPercentFanOutletLowDueToBPR(bpr);
		double zPercentOutletMin = zPercentFanOutletLow*getZPercentOutletMinDueToBPR(bpr);
		double zPercentOutletCore = zPercentFanOutletLow*getZPercentOutletCoreDueToBPR(bpr);
		double zPercentTurbineOutletUpp = zPercentOutletCore*getZPercentTurbineOutletUppDueToBPR(bpr);
		double zPercentTurbineOutletLow = zPercentOutletCore*getZPercentTurbineOutletLowDueToBPR(bpr);
		
		// CAD parameters
		double byPassOutletOffset = byPassOutletRadius*(1-zPercentFanOutletUpp)*0.05;
		double coreOutletOffset = byPassOutletOffset;
			
		// ---------------------------------------------------
		// Generate sketching points for the sketching curves
		// ---------------------------------------------------
		double[] ptA = new double[] {xApex, yApex, zApex + inletRadius};
		double[] ptB = new double[] {xApex + xMaxRadius, yApex, zApex + maxRadius};
		double[] ptC = new double[] {xApex + xPercentCowlLength*lengthCore, yApex, zApex + byPassOutletRadius + byPassOutletOffset};
		double[] ptD = new double[] {xApex + xPercentCowlLength*lengthCore, yApex, zApex + byPassOutletRadius - byPassOutletOffset};
		double[] ptE = new double[] {xApex + xPercentFanOutlet*lengthCore, yApex, zApex + zPercentFanOutletUpp*maxRadius};
		double[] ptF = new double[] {xApex + xPercentFanOutlet*lengthCore, yApex, zApex + zPercentFanOutletLow*maxRadius};
		double[] ptG = new double[] {xApex + xPercentOutletMin*lengthCore, yApex, zApex + zPercentOutletMin*maxRadius};
		double[] ptH = new double[] {xApex + lengthCore, yApex, zApex + zPercentOutletCore*maxRadius + coreOutletOffset};
		double[] ptI = new double[] {xApex + lengthCore, yApex, zApex + zPercentOutletCore*maxRadius - coreOutletOffset};
		double[] ptL = new double[] {xApex + xPercentTurbineOutlet*lengthCore, yApex, zApex + zPercentTurbineOutletUpp*maxRadius};
		double[] ptM = new double[] {xApex + xPercentTurbineOutlet*lengthCore, yApex, zApex + zPercentTurbineOutletLow*maxRadius};
		double[] ptN = new double[] {xApex + lengthCore*(1+xPercentNozzle), yApex, zApex};
		double[] ptO = new double[] {xApex + xPercentInletCone*lengthCore, yApex, zApex};
		double[] ptP = new double[] {xApex + xPercentFanInlet*lengthCore, yApex, zApex + zPercentFanInletLow*maxRadius};
		double[] ptQ = new double[] {xApex + xPercentFanInlet*lengthCore, yApex, zApex + zPercentFanInletUpp*maxRadius};
		double[] ptR = new double[] {xApex + xPercentMinInlet*lengthCore, yApex, zApex + zPercentMinInlet*maxRadius};
		
		// -----------------------------------------------------
		// Generate sketching tangents for the sketching curves
		// -----------------------------------------------------
		PVector pvB = new PVector((float) ptB[0], (float) ptB[1], (float) ptB[2]);
		PVector pvC = new PVector((float) ptC[0], (float) ptC[1], (float) ptC[2]);
		PVector pvS = new PVector((float) ptC[0], (float) ptC[1], (float) ptB[2]);
		PVector pvT = PVector.lerp(pvB, pvS, 0.45f);
		
		PVector pvTngC = PVector.sub(pvC, pvT).normalize();
		PVector pvTngD = PVector.mult(pvTngC, -1.0f);
		
		PVector pvG = new PVector((float) ptG[0], (float) ptG[1], (float) ptG[2]);
		PVector pvH = new PVector((float) ptH[0], (float) ptH[1], (float) ptH[2]);
		PVector pvU = new PVector((float) ptH[0], (float) ptH[1], (float) ptG[2]);
		PVector pvV = PVector.lerp(pvG, pvU, 0.25f);
		
		PVector pvTngH = PVector.sub(pvH, pvV).normalize();
		PVector pvTngI = PVector.mult(pvTngH, -1.0f);
		
		PVector pvM = new PVector((float) ptM[0], (float) ptM[1], (float) ptM[2]);
		PVector pvN = new PVector((float) ptN[0], (float) ptN[1], (float) ptN[2]);
		PVector pvZ = new PVector((float) ptN[0], (float) ptN[1], (float) ptM[2]);
		PVector pvAA = PVector.lerp(pvM, pvZ, 0.25f);
		
		PVector pvTngN = PVector.sub(pvN, pvAA).normalize();
		
		PVector pvP = new PVector((float) ptP[0], (float) ptP[1], (float) ptP[2]);
		PVector pvO = new PVector((float) ptO[0], (float) ptO[1], (float) ptO[2]);
		PVector pvAB = new PVector((float) ptO[0], (float) ptO[1], (float) ptP[2]);
		PVector pvAC = PVector.lerp(pvAB, pvP, 0.15f);
		
		PVector pvTngO = PVector.sub(pvAC, pvO).normalize();
		
		PVector pvR = new PVector((float) ptR[0], (float) ptR[1], (float) ptR[2]);
		PVector pvQ = new PVector((float) ptQ[0], (float) ptQ[1], (float) ptQ[2]);
		PVector pvAD = new PVector((float) ptQ[0], (float) ptQ[1], (float) ptR[2]);
		PVector pvAE = PVector.lerp(pvAD, pvR, 0.50f);

		PVector pvTngQ = PVector.sub(pvAE, pvQ).normalize();
		
		double[] tngA = new double[] {0.0, 0.0, 0.5};
		double[] tngB = new double[] {1.0, 0.0, 0.0};
		double[] tngC = new double[] {pvTngC.x, pvTngC.y, pvTngC.z};
		double[] tngD = new double[] {pvTngD.x, pvTngD.y, pvTngD.z};
		double[] tngE = new double[] {-1.0, 0.0, 0.0};
		double[] tngF = new double[] {1.0, 0.0, 0.0};
		double[] tngG = new double[] {1.0, 0.0, 0.0};
		double[] tngH = new double[] {pvTngH.x, pvTngH.y, pvTngH.z};
		double[] tngI = new double[] {pvTngI.x, pvTngI.y, pvTngI.z};
		double[] tngL = new double[] {-1.0, 0.0, 0.0};
		double[] tngM = new double[] {1.0, 0.0, 0.0};
		double[] tngN = new double[] {pvTngN.x, pvTngN.y, pvTngN.z};
		double[] tngO = new double[] {pvTngO.x, pvTngO.y, pvTngO.z};
		double[] tngP = new double[] {1.0, 0.0, 0.0};
		double[] tngQ = new double[] {pvTngQ.x, pvTngQ.y, pvTngQ.z};
		double[] tngR = new double[] {-1.0, 0.0, 0.0};
		
		// -------------------------------------
		// Generate sketching curves
		// -------------------------------------
		List<double[]> ptsCrv1 = new ArrayList<>();
		List<double[]> ptsCrv2 = new ArrayList<>();
		List<double[]> ptsCrv3 = new ArrayList<>();
		List<double[]> ptsCrv4 = new ArrayList<>();
		List<double[]> ptsCrv5 = new ArrayList<>();
		List<double[]> ptsCrv6 = new ArrayList<>();
		List<double[]> ptsCrv7 = new ArrayList<>();
		List<double[]> ptsCrv8 = new ArrayList<>();
		List<double[]> ptsCrv9 = new ArrayList<>();
		List<double[]> ptsCrv10 = new ArrayList<>();
		List<double[]> ptsCrv11 = new ArrayList<>();
		List<double[]> ptsCrv12 = new ArrayList<>();
		List<double[]> ptsCrv13 = new ArrayList<>();
		List<double[]> ptsCrv14 = new ArrayList<>();
		List<double[]> ptsCrv15 = new ArrayList<>();
		List<double[]> ptsCrv16 = new ArrayList<>();
		
		ptsCrv1.add(ptA);	ptsCrv1.add(ptB);		
		ptsCrv2.add(ptB);	ptsCrv2.add(ptC);	
		ptsCrv3.add(ptC);	ptsCrv3.add(ptD);
		ptsCrv4.add(ptD);	ptsCrv4.add(ptE);
		ptsCrv5.add(ptE);	ptsCrv5.add(ptF);
		ptsCrv6.add(ptF);	ptsCrv6.add(ptG);
		ptsCrv7.add(ptG);	ptsCrv7.add(ptH);
		ptsCrv8.add(ptH);	ptsCrv8.add(ptI);
		ptsCrv9.add(ptI);	ptsCrv9.add(ptL);
		ptsCrv10.add(ptL);	ptsCrv10.add(ptM);
		ptsCrv11.add(ptM);	ptsCrv11.add(ptN);
		ptsCrv12.add(ptN);	ptsCrv12.add(ptO);
		ptsCrv13.add(ptO);	ptsCrv13.add(ptP);
		ptsCrv14.add(ptP);	ptsCrv14.add(ptQ);
		ptsCrv15.add(ptQ);	ptsCrv15.add(ptR);
		ptsCrv16.add(ptR);	ptsCrv16.add(ptA);
				
		CADEdge edge1 = OCCUtils.theFactory.newCurve3D(ptsCrv1, false, tngA, tngB, false).edge();
		CADEdge edge2 = OCCUtils.theFactory.newCurve3D(ptsCrv2, false, tngB, tngC, false).edge();
		CADEdge edge3 = OCCUtils.theFactory.newCurve3D(ptsCrv3, false).edge();
		CADEdge edge4 = OCCUtils.theFactory.newCurve3D(ptsCrv4, false, tngD, tngE, false).edge();
		CADEdge edge5 = OCCUtils.theFactory.newCurve3D(ptsCrv5, false).edge();
		CADEdge edge6 = OCCUtils.theFactory.newCurve3D(ptsCrv6, false, tngF, tngG, false).edge();
		CADEdge edge7 = OCCUtils.theFactory.newCurve3D(ptsCrv7, false, tngG, tngH, false).edge();
		CADEdge edge8 = OCCUtils.theFactory.newCurve3D(ptsCrv8, false).edge();
		CADEdge edge9 = OCCUtils.theFactory.newCurve3D(ptsCrv9, false, tngI, tngL, false).edge();
		CADEdge edge10 = OCCUtils.theFactory.newCurve3D(ptsCrv10, false).edge();
		CADEdge edge11 = OCCUtils.theFactory.newCurve3D(ptsCrv11, false, tngM, tngN, false).edge();
		CADEdge edge12 = OCCUtils.theFactory.newCurve3D(ptsCrv12, false).edge();
		CADEdge edge13 = OCCUtils.theFactory.newCurve3D(ptsCrv13, false, tngO, tngP, false).edge();
		CADEdge edge14 = OCCUtils.theFactory.newCurve3D(ptsCrv14, false).edge();
		CADEdge edge15 = OCCUtils.theFactory.newCurve3D(ptsCrv15, false, tngQ, tngR, false).edge();
		CADEdge edge16 = OCCUtils.theFactory.newCurve3D(ptsCrv16, false, tngR, tngA, false).edge();
		
		// -------------------
		// Generate the wire
		// -------------------
		CADWire engineWire = OCCUtils.theFactory.newWireFromAdjacentEdges(
				edge1, edge2, edge3, edge4, edge5, edge6, edge7, edge8, edge9, edge10, edge11, edge12, edge13, edge14, edge15, edge16
				);
		
		// --------------------------------------
		// Generate supporting wires
		// --------------------------------------
		List<CADWire> revolvedWires = new ArrayList<>();
		
		int numSuppWires = 10;
		double[] rotAngles = MyArrayUtils.linspace(0, Math.PI, numSuppWires);
		gp_Trsf rotTrsf = new gp_Trsf();
		gp_Ax1 rotAxis = new gp_Ax1(
				new gp_Pnt(xApex, yApex, zApex), 
				new gp_Dir(1.0, 0.0, 0.0)
				);
		
		revolvedWires.add(engineWire);
		for (int i = 1; i < numSuppWires; i++) {		
			rotTrsf.SetRotation(rotAxis, rotAngles[i]);
			revolvedWires.add(
					(OCCWire) OCCUtils.theFactory.newShape(
							TopoDS.ToWire(
									new BRepBuilderAPI_Transform(
											((OCCShape) engineWire).getShape(), 
											rotTrsf, 
											0).Shape()
									)));			
		}
		
		// ---------------------------------
		// Generate the solid of the engine
		// ---------------------------------
		OCCShape rightHalfEngine = OCCUtils.makePatchThruSections(revolvedWires);
		OCCShape leftHalfEngine = OCCUtils.getShapeMirrored(
				rightHalfEngine, 
				new PVector((float) xApex, (float) yApex, (float) zApex), 
				new PVector(0.0f, 1.0f, 0.0f), 
				new PVector(1.0f, 0.0f, 0.0f)
				);
		
		OCCShape engineSolid = (OCCShape) OCCUtils.theFactory.newSolidFromAdjacentShapes(
				rightHalfEngine, leftHalfEngine
				);
		
		// ---------------------
		// Generate the PYLON
		// ---------------------
		LiftingSurface mountingLS = null;
		double pylonWidthEngineMaxRadiusPercent = 0.25;
		double pylonWidth = pylonWidthEngineMaxRadiusPercent*maxRadius;
		
		switch (engine.getMountingPosition()) {

		case WING:		
			mountingLS = aircraft.getWing();
			
			break;

		case HTAIL:
			mountingLS = aircraft.getHTail();

			break;

		default:

			break;
		}
		
//		exportShapes.addAll(
//				AircraftCADUtils.getLiftingSurfaceCAD(mountingLS, WingTipType.ROUNDED, false, false, true)
//				);
			
		// ----------------------------
		// Generate the PYLON mid wire
		// ----------------------------
		
		// Assign a z offset to the outlet low wire
		double byPassOutletZOffset = byPassOutletOffset;

		CADWire byPassLowOutletWire = OCCUtils.theFactory.newWireFromAdjacentEdges(edge6, edge7);
		double[] byPassLowOutletWireRefVtx = byPassLowOutletWire.vertices().get(0).pnt();
		CADWire byPassLowOutletWireMid = (CADWire) OCCUtils.getShapeTranslated(
				(OCCShape) byPassLowOutletWire, 
				byPassLowOutletWireRefVtx, 
				new double[] {
						byPassLowOutletWireRefVtx[0],
						byPassLowOutletWireRefVtx[1],
						byPassLowOutletWireRefVtx[2] - byPassOutletZOffset
				});
		
		// Generate the mid airfoil camber line
		List<double[]> midAirfoilCamberLinePts = AircraftCADUtils.generateCamberAtY(yApex, mountingLS);
		CADEdge midAirfoilCamberLine = OCCUtils.theFactory.newCurve3D(midAirfoilCamberLinePts, false).edge();
		
		// Generate remaining points
		double[] camberLineFirstPt = midAirfoilCamberLinePts.get(0);
		double[] coreOutletUppPt = byPassLowOutletWireMid.vertices().get(byPassLowOutletWireMid.vertices().size() - 1).pnt();
		
		double[] pt1 = (ptB[0] > (xApex + xPercentFanInlet*lengthCore) && ptB[0] < (xApex + xPercentFanOutlet*lengthCore)) ?
				ptB : new double[] {
						xApex + (xPercentFanOutlet*lengthCore - xPercentFanInlet*lengthCore)/2, 
						yApex, 
						zApex + maxRadius*0.95};
		
		double[] pt2 = midAirfoilCamberLinePts.get(midAirfoilCamberLinePts.size());
		
		double[] pt3 = (camberLineFirstPt[0] > coreOutletUppPt[0]) ? 
				camberLineFirstPt : new double[] {
						camberLineFirstPt[0] + 0.25*(coreOutletUppPt[0] - camberLineFirstPt[0])/2, 
						camberLineFirstPt[1],
						camberLineFirstPt[2]};
		
		double[] pt4 = (camberLineFirstPt[0] > coreOutletUppPt[0]) ? 
				new double[] {
						coreOutletUppPt[0] + 0.25*(camberLineFirstPt[0] - coreOutletUppPt[0]),
						coreOutletUppPt[1],
						coreOutletUppPt[2]
						} : new double[] {
								coreOutletUppPt[0] + 0.25*(coreOutletUppPt[0] - camberLineFirstPt[0]), 
								coreOutletUppPt[1], 
								coreOutletUppPt[2]};
		
		double[] pt5 = coreOutletUppPt;			
					
		double[] pt6 = byPassLowOutletWireMid.vertices().get(0).pnt();
		
		double[] pt7 = new double[] {pt1[0], yApex, byPassLowOutletWireMid.vertices().get(0).pnt()[2]};
		
		// Generate mid wire edges
//		CADEdge edgeA = OCCUtils.theFactory.newCurve3D(pt1, pt2)

//		double byPassLowOutletWireYOffset = pylonWidth/2;
//		double thetaRotLowOutletWire = Math.asin(byPassLowOutletWireYOffset/(ptG[2] - zApex));
//		double[] thetaRotAngles = MyArrayUtils.linspace(0, thetaRotLowOutletWire, nPoints)
				
				
//		double byPassLowOutletWireZOffset = byPassOutletOffset + (ptG[2] - zApex)*(1 - Math.cos(thetaRotLowOutletWire));
//		
//		double[] byPassLowOutletWireRefVtx = byPassLowOutletWireMid.vertices().get(0).pnt();
//		double[] byPassLowOutletWireTrnsLeftVtx = new double[] {
//				byPassLowOutletWireRefVtx[0], 
//				byPassLowOutletWireRefVtx[1] - byPassLowOutletWireYOffset,
//				byPassLowOutletWireRefVtx[2] - byPassLowOutletWireZOffset,
//		};	
//		double[] byPassLowOutletWireTrnsRightVtx = new double[] {
//				byPassLowOutletWireRefVtx[0], 
//				byPassLowOutletWireRefVtx[1] + byPassLowOutletWireYOffset,
//				byPassLowOutletWireRefVtx[2] - byPassLowOutletWireZOffset,
//		};	
//		
//		CADWire byPassLowOutletWireLeft = (CADWire) OCCUtils.getShapeTranslated(
//				(OCCShape) byPassLowOutletWireMid, byPassLowOutletWireRefVtx, byPassLowOutletWireTrnsLeftVtx);
//		CADWire byPassLowOutletWireRight = (CADWire) OCCUtils.getShapeTranslated(
//				(OCCShape) byPassLowOutletWireMid, byPassLowOutletWireRefVtx, byPassLowOutletWireTrnsRightVtx);
//		
//		List<double[]> lsCamberMidSection = AircraftCADUtils.generateCamberAtY(yApex, mountingLS);
//		

		
//		double[] engineRearReferencePnt1 =
		
		// ---------------------
		// Export shapes
		// ---------------------
		exportShapes.add((OCCShape) engineWire);
		exportShapes.addAll(sectionResults);
//		exportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(lsCamberMidSection, false).edge());
//		exportShapes.add(engineSolid);
		
		OCCUtils.write("turbofan_test_01", FileExtension.STEP, exportShapes);
	}
	
	private static List<OCCShape> generatePylonShapes(
			double[] engineUppReferencePnt, 
			double[] engineRearReferencePnt, 
			CADWire byPassLowOutletWire,
			List<double[]> lsReferencePnts
			) {
		
		List<OCCShape> returnedShapes = new ArrayList<>();
		
		return returnedShapes;
	}
	
	private static double getZPercentFanOutletLowDueToBPR(double bpr) {
		
		return (0.6303 - 0.0067*bpr); // BPR 9.0 --> 0.57, BPR 12.0 --> 0.55	
	}
	
	private static double getZPercentOutletMinDueToBPR(double bpr) {
		
		return (1.15 + 0.0067*bpr); // BPR 9.0 --> 1.21, BPR 12.0 --> 1.23	
	}
	
	private static double getZPercentOutletCoreDueToBPR(double bpr) {
		
		return (0.84 + 0.0067*bpr); // BPR 9.0 --> 0.90, BPR 12.0 --> 0.92			
	}
	
	private static double getZPercentTurbineOutletUppDueToBPR(double bpr) {
		
		return (0.92 + 0.013*bpr); // BPR 9.0 --> 1.04, BPR 12.0 --> 1.08	
	}
	
	private static double getZPercentTurbineOutletLowDueToBPR(double bpr) {
		
		return (0.36 + 0.03*bpr); // BPR 9.0 --> 0.63, BPR 12.0 --> 0.72	
	}
}
