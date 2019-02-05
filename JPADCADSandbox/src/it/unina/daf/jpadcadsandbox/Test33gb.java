package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.WingAdjustCriteriaEnum;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.WingTipType;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;

public class Test33gb {

	public static void main(String[] args) {
		
		System.out.println("---------------------------------------------");
		System.out.println("--------- Adjust Dimension Test -------------");
		System.out.println("---------------------------------------------");
		
		if (OCCUtils.theFactory == null)
			OCCUtils.initCADShapeFactory();
		
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
//		LiftingSurface wing = aircraft.getWing();
		
//		double tc1 = wing.getAirfoilList().get(1).getThicknessToChordRatio();
//		List<PVector> noDuplicatesCoords1 = new ArrayList<>();
//		for (int i = 0; i < wing.getAirfoilList().get(1).getXCoords().length; i++) {
//			noDuplicatesCoords1.add(new PVector (
//					(float) wing.getAirfoilList().get(1).getXCoords()[i],
//					0.0f,
//					(float) wing.getAirfoilList().get(1).getZCoords()[i]
//					));
//		}
//		
//		System.out.println(tc1);
//
//		Set<PVector> uniqueEntries1 = new HashSet<>();
//		for (Iterator<PVector> iter = noDuplicatesCoords1.listIterator(1); iter.hasNext(); ) {
//			PVector point = (PVector) iter.next();
//			if (!uniqueEntries1.add(point))
//				iter.remove();
//		}
//		
//		double tc2 = wing.getAirfoilList().get(2).getThicknessToChordRatio();
//		List<PVector> noDuplicatesCoords2 = new ArrayList<>();
//		for (int i = 0; i < wing.getAirfoilList().get(2).getXCoords().length; i++) {
//			noDuplicatesCoords2.add(new PVector (
//					(float) wing.getAirfoilList().get(2).getXCoords()[i],
//					0.0f,
//					(float) wing.getAirfoilList().get(2).getZCoords()[i]
//					));
//		}
//		
//		System.out.println(tc2);
//
//		Set<PVector> uniqueEntries2 = new HashSet<>();
//		for (Iterator<PVector> iter = noDuplicatesCoords2.listIterator(1); iter.hasNext(); ) {
//			PVector point = (PVector) iter.next();
//			if (!uniqueEntries2.add(point))
//				iter.remove();
//		}
//		
//		double thickRatio = tc2/tc1;
//		
//		System.out.println(thickRatio);
//		
//		// Split airfoil points in two separated lists
//		
//		// airfoil1
//		List<Double> xUpper1 = new ArrayList<>();
//		List<Double> zUpper1 = new ArrayList<>();
//		List<Double> xLower1 = new ArrayList<>(); 
//		List<Double> zLower1 = new ArrayList<>();
//		
//		List<Double> x1 = new ArrayList<>();
//		List<Double> z1 = new ArrayList<>();
//		
//		noDuplicatesCoords1.forEach(pv -> {
//			x1.add((double) pv.x);
//			z1.add((double) pv.z);
//		});
//		
//		int nPts1 = x1.size();
//		int iMin1 = MyArrayUtils.getIndexOfMin(MyArrayUtils.convertToDoublePrimitive(x1));
//		
//		IntStream.range(0, iMin1 + 1).forEach(i -> {
//			xUpper1.add(x1.get(i));
//			
//			if (z1.get(i) >= 0)
//				zUpper1.add(z1.get(i)*thickRatio);
//			else
//				zUpper1.add(z1.get(i)*thickRatio);
//			});
//		
//		IntStream.range(iMin1 + 1, nPts1).forEach(i -> {
//			xLower1.add(x1.get(i));
//			
//			if (z1.get(i) <= 0)
//				zLower1.add(z1.get(i)*thickRatio);
//			else
//				zLower1.add(z1.get(i)*thickRatio);
//		});
//
//		List<Double> x = new ArrayList<>();
//		List<Double> z = new ArrayList<>();
//
//		x.addAll(xUpper1);
//		x.addAll(xLower1);
//
//		z.addAll(zUpper1);
//		z.addAll(zLower1);
//		
//		List<double[]> noDuplicates1Scaled = new ArrayList<>();
//		for (int i = 0; i < x.size(); i++) 
//			noDuplicates1Scaled.add(new double[] {x.get(i), 0.0, z.get(i)});
//		
//		CADGeomCurve3D airfoil1 = OCCUtils.theFactory.newCurve3DP(noDuplicatesCoords1, false);
//		CADGeomCurve3D airfoil2 = OCCUtils.theFactory.newCurve3DP(noDuplicatesCoords2, false);
//		CADGeomCurve3D airfoilScaled = OCCUtils.theFactory.newCurve3D(noDuplicates1Scaled, false);
//		
//		List<PVector> scaledNoDuplicates1 = new ArrayList<>();
//		noDuplicatesCoords1.forEach(pv -> scaledNoDuplicates1.add(new PVector(pv.x, pv.y, (float) (pv.z*thickRatio))));
//		
//		CADGeomCurve3D airfoil1 = OCCUtils.theFactory.newCurve3DP(noDuplicatesCoords1, false);
//		CADGeomCurve3D airfoil1Scaled = OCCUtils.theFactory.newCurve3DP(scaledNoDuplicates1, false);
//		CADGeomCurve3D airfoil2 = OCCUtils.theFactory.newCurve3DP(noDuplicatesCoords2, false);
//		
//		List<double[]> newAirfoilPts = AircraftCADUtils.interpolateAirfoils(
//				wing.getAirfoilList().get(1), wing.getAirfoilList().get(2), 0.95);
//		
//		CADGeomCurve3D newAirfoil = OCCUtils.theFactory.newCurve3D(newAirfoilPts, false);
//		
//		List<OCCShape> shapes = new ArrayList<>();
//		shapes.add((OCCEdge) ((OCCGeomCurve3D) airfoil1).edge());
//		shapes.add((OCCEdge) ((OCCGeomCurve3D) airfoil1Scaled).edge());
//		shapes.add((OCCEdge) ((OCCGeomCurve3D) airfoil2).edge());
	
//		System.out.println(newAirfoilPts.size());
//		newAirfoilPts.forEach(pt -> System.out.println(Arrays.toString(pt)));
//		
//		CADGeomCurve3D airfoil1 = OCCUtils.theFactory.newCurve3DP(noDuplicatesCoords1, false);
//		CADGeomCurve3D airfoil2 = OCCUtils.theFactory.newCurve3DP(noDuplicatesCoords2, false);
//		CADGeomCurve3D newAirfoil = OCCUtils.theFactory.newCurve3D(newAirfoilPts, false);
		
//		List<OCCShape> shapes = new ArrayList<>();
//		shapes.add((OCCEdge) ((OCCGeomCurve3D) airfoil1).edge());
////		shapes.add((OCCEdge) ((OCCGeomCurve3D) airfoil2).edge());
//		shapes.add((OCCEdge) ((OCCGeomCurve3D) airfoilScaled).edge());
//		
//		OCCUtils.write("Test33mds", FileExtension.BREP, shapes);
		
		LiftingSurface canard = aircraft.getCanard();
		
		double origSpan = canard.getEquivalentWing().getPanels().get(0).getSpan().times(2).doubleValue(SI.METRE);
		double origSurface = canard.getEquivalentWing().getPanels().get(0).getSurfacePlanform().times(2).doubleValue(SI.SQUARE_METRE);
		double origTaper = canard.getEquivalentWing().getPanels().get(0).getTaperRatio();
		Amount<Angle> origSweepLE = canard.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge();
		Amount<Angle> origDihedralLE = canard.getEquivalentWing().getPanels().get(0).getDihedral();
		Amount<Angle> origTipTwist = canard.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip();
		Amount<Angle> origRiggAngle = canard.getRiggingAngle();
		
		// Modification 
		LiftingSurface modCanard = AircraftUtils.importAircraft(args).getCanard();
		
		double modSpan = modCanard.getEquivalentWing().getPanels().get(0).getSpan().times(2).doubleValue(SI.METRE)*1;
		double modSurface = modCanard.getEquivalentWing().getPanels().get(0).getSurfacePlanform().times(2).doubleValue(SI.SQUARE_METRE);
		double modTaper = modCanard.getEquivalentWing().getPanels().get(0).getTaperRatio();
		Amount<Angle> modSweepLE = modCanard.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().plus(Amount.valueOf(-20.0, NonSI.DEGREE_ANGLE));
		Amount<Angle> modDihedralLE = modCanard.getEquivalentWing().getPanels().get(0).getDihedral().plus(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		Amount<Angle> modTipTwist = modCanard.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip().plus(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		Amount<Angle> modRiggAngle = modCanard.getRiggingAngle().plus(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		
		modCanard.adjustDimensions(
				modSpan, 
				modSurface, 
				modTaper, 
				modSweepLE, 
				modDihedralLE, 
				modTipTwist, 
				WingAdjustCriteriaEnum.SPAN_AREA_TAPER
				);
		
		System.out.println("Original values:");
		System.out.println("span = " + origSpan);
		System.out.println("surface = " + origSurface);
		System.out.println("taper = " + origTaper);
		System.out.println("sweep LE = " + origSweepLE.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("dihedral = " + origDihedralLE.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("tip twist = " + origTipTwist.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("rigging angle = " + origRiggAngle.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("");		
		System.out.println("Modded values:");
		System.out.println("span = " + modSpan);
		System.out.println("surface = " + modSurface);
		System.out.println("taper = " + modTaper);
		System.out.println("sweep LE = " + modSweepLE.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("dihedral = " + modDihedralLE.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("tip twist = " + modTipTwist.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("rigging angle = " + modRiggAngle.doubleValue(NonSI.DEGREE_ANGLE));
		
		modCanard.setAirfoilList(canard.getAirfoilList());	
		modCanard.setXApexConstructionAxes(canard.getXApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
		modCanard.setZApexConstructionAxes(canard.getZApexConstructionAxes().plus(Amount.valueOf(0, SI.METER)));
		modCanard.setRiggingAngle(modRiggAngle);
		
		// Generate the CAD
//		List<OCCShape> canardShapes = AircraftCADUtils.getLiftingSurfaceCAD(
//				canard, WingTipType.ROUNDED, false, false, true);
		
		List<OCCShape> modcanardShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				modCanard, WingTipType.ROUNDED, false, false, true);
		
		// Export to file
		List<OCCShape> allShapes = new ArrayList<>();
		//allShapes.addAll(canardShapes);
		allShapes.addAll(modcanardShapes);
		
		OCCUtils.write("test33mds", FileExtension.STEP, allShapes);
				
	}

}
