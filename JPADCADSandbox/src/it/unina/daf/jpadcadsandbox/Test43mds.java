package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.SI;

import aircraft.Aircraft;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.nacelles.Nacelles;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import processing.core.PVector;

public class Test43mds {

	public static void main(String[] args) {

		System.out.println("----- Nacelle creation -----");
		
		// ----------------------
		// Initialize
		// ----------------------
		if (OCCUtils.theFactory == null)
			OCCUtils.initCADShapeFactory();
		
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		Nacelles nacelles = aircraft.getNacelles();
		NacelleCreator nacelle = nacelles.getNacellesList().get(0);
		
		// --------------------------
		// Collect geometric data
		// --------------------------
		double referenceBPR = 9.0;
		
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
		
		// Design parameters
		double xPercentInletCone = 0.15;
		double xPercentFanInlet = 0.22;       // All percentages referred to length core (== nacelle length)
		double xPercentFanOutlet = 0.46;
		double xPercentOutletMin = 0.78;
		double xPercentCowlLength = 0.86;
		double xPercentTurbineOutlet = 0.88;
		double xPercentNozzle = 0.17;
		
		double zPercentMinInlet = 0.76;       // All percentages referred to max diameter
		double zPercentFanInletUpp = 0.79;
		double zPercentFanInletLow = 0.23;
		double zPercentFanOutletUpp = 0.80;
		double zPercentFanOutletLow = zPercentFanOutletUpp*getZPercentFanOutletLowDueToBPR();
		double zPercentOutletMin = zPercentFanOutletLow*getZPercentOutletMinDueToBPR();
		double zPercentOutletCore = zPercentFanOutletLow*getZPercentOutletCoreDueToBPR();
		double zPercentTurbineOutletUpp = zPercentOutletCore*getZPercentTurbineOutletUppDueToBPR();
		double zPercentTurbineOutletLow = zPercentOutletCore*getZPercentTurbineOutletLowDueToBPR();
		
		// CAD parameters
		double byPassOutletOffset = byPassOutletRadius*(1-zPercentFanOutletUpp)*0.01;
		double coreOutletOffset = byPassOutletOffset;
		
		// ----------------------------------------------
		// Generate the fan cowl profile sketching plane
		// ----------------------------------------------
		PVector pt1 = new PVector(
				(float) xApex, (float) yApex, (float) zApex
				);
		PVector pt2 = new PVector(
				(float) xApex, (float) yApex, (float) (zApex + maxRadius)				
				); 
		PVector pt3 = new PVector(
				(float) (xApex + lengthCore*(1+xPercentNozzle)), (float) yApex, (float) (zApex + maxRadius)
				);
		PVector pt4 = new PVector(
				(float) (xApex + lengthCore*(1+xPercentNozzle)), (float) yApex, (float) zApex
				);
		
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
		double[] ptR = new double[] {xApex + zPercentMinInlet*lengthCore, yApex, zApex + zPercentMinInlet*maxRadius};
		
		// -----------------------------------------------------
		// Generate sketching tangents for the sketching curves
		// -----------------------------------------------------
		
		// Generate supporting points for the tangent vectors
		PVector pvB = new PVector((float) ptB[0], (float) ptB[1], (float) ptB[2]);
		PVector pvC = new PVector((float) ptC[0], (float) ptC[1], (float) ptC[2]);
		PVector pvS = new PVector((float) ptC[0], (float) ptC[1], (float) ptB[2]);
		PVector pvT = PVector.lerp(pvB, pvS, 0.25f);
		
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
		PVector pvAC = PVector.lerp(pvAB, pvP, 0.75f);
		
		PVector pvTngO = PVector.sub(pvAC, pvO).normalize();
		
		PVector pvR = new PVector((float) ptR[0], (float) ptR[1], (float) ptR[2]);
		PVector pvQ = new PVector((float) ptQ[0], (float) ptQ[1], (float) ptQ[2]);
		PVector pvAD = new PVector((float) ptQ[0], (float) ptQ[1], (float) ptR[2]);
		PVector pvAE = PVector.lerp(pvAD, pvR, 0.75f);

		PVector pvTngQ = PVector.sub(pvAE, pvQ).normalize();
		
		double[] tngA = new double[] {0.0, 1.0, 0.0};
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
		
		ptsCrv1.add(ptA);
		ptsCrv1.add(ptB);
		
		ptsCrv2.add(ptB);
		ptsCrv2.add(ptC);
		
		ptsCrv3.add(ptC);
		ptsCrv3.add(ptD);
		
		ptsCrv4.add(ptD);
		ptsCrv4.add(ptE);
		
		ptsCrv5.add(ptE);
		ptsCrv5.add(ptF);
		
		ptsCrv6.add(ptF);
		ptsCrv6.add(ptG);
		
		ptsCrv7.add(ptG);
		ptsCrv7.add(ptH);
		
		ptsCrv8.add(ptH);
		ptsCrv8.add(ptI);
		
		ptsCrv9.add(ptI);
		ptsCrv9.add(ptL);
		
		ptsCrv10.add(ptL);
		ptsCrv10.add(ptM);
		
		ptsCrv11.add(ptM);
		ptsCrv11.add(ptN);
		
		ptsCrv12.add(ptO);
		ptsCrv12.add(ptP);
		
		ptsCrv13.add(ptP);
		ptsCrv13.add(ptQ);
		
		ptsCrv14.add(ptQ);
		ptsCrv14.add(ptR);
		
		ptsCrv15.add(ptR);
		ptsCrv15.add(ptA);
		
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
		CADEdge edge12 = OCCUtils.theFactory.newCurve3D(ptsCrv12, false, tngO, tngP, false).edge();
		CADEdge edge13 = OCCUtils.theFactory.newCurve3D(ptsCrv13, false).edge();
		CADEdge edge14 = OCCUtils.theFactory.newCurve3D(ptsCrv14, false, tngQ, tngR, false).edge();
		CADEdge edge15 = OCCUtils.theFactory.newCurve3D(ptsCrv15, false, tngR, tngA, false).edge();
		
	}

	// TODO: implement this method
	private static double getZPercentFanOutletLowDueToBPR() {
		
		return 0.57;
	}
	
	// TODO: implement this method
	private static double getZPercentOutletMinDueToBPR() {
		
		return 1.21;
	}
	
	// TODO: implement this method
	private static double getZPercentOutletCoreDueToBPR() {
		
		return 0.90;
	}
	
	// TODO: implement this method
	private static double getZPercentTurbineOutletUppDueToBPR() {
		
		return 1.04;
	}
	
	// TODO: implement this method
	private static double getZPercentTurbineOutletLowDueToBPR() {
		
		return 0.63;
	}
}
