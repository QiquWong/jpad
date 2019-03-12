package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.WingAdjustCriteriaEnum;
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.enums.WingTipType;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;

public class Test33mds {

	public static void main(String[] args) {
		
		System.out.println("---------------------------------------------");
		System.out.println("--------- Adjust Dimension Test -------------");
		System.out.println("---------------------------------------------");
		
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		LiftingSurface canard = aircraft.getCanard();

		double origAR = canard.getAspectRatio();
		double origSpan = canard.getEquivalentWing().getPanels().get(0).getSpan().times(2).doubleValue(SI.METRE);
		double origSurface = canard.getEquivalentWing().getPanels().get(0).getSurfacePlanform().times(2).doubleValue(SI.SQUARE_METRE);
		double origRootChord = canard.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double origTaper = canard.getEquivalentWing().getPanels().get(0).getTaperRatio();
		Amount<Angle> origSweepLE = canard.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE);
		Amount<Angle> origDihedralLE = canard.getEquivalentWing().getPanels().get(0).getDihedral().to(NonSI.DEGREE_ANGLE);;
		Amount<Angle> origTipTwist = canard.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip();
		Amount<Angle> origRigging = canard.getRiggingAngle().to(NonSI.DEGREE_ANGLE);;
		
		// Modification 
		LiftingSurface modCanard = AircraftUtils.importAircraft(args).getCanard();
		
		double modAR = 4.5;	
		double modSpan = modCanard.getEquivalentWing().getPanels().get(0).getSpan().times(2).doubleValue(SI.METRE)*1.20;
		double modSurface = modCanard.getEquivalentWing().getPanels().get(0).getSurfacePlanform().times(2).doubleValue(SI.SQUARE_METRE);
		double modRootChord = modCanard.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double modTaper = modCanard.getEquivalentWing().getPanels().get(0).getTaperRatio();
		Amount<Angle> modSweepLE = modCanard.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().minus(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		Amount<Angle> modDihedralLE = modCanard.getEquivalentWing().getPanels().get(0).getDihedral().plus(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		Amount<Angle> modTipTwist = modCanard.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip();
		Amount<Angle> modRigging = modCanard.getRiggingAngle().plus(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		
//		modCanard.adjustDimensions(
//				modAR, 
//				modSurface, 
//				modRootChord, 
//				modSweepLE, 
//				modDihedralLE, 
//				modTipTwist, 
//				WingAdjustCriteriaEnum.AR_AREA_ROOTCHORD
//				);	
//		
//		modCanard.setAirfoilList(canard.getAirfoilList());	
//		modCanard.setXApexConstructionAxes(canard.getXApexConstructionAxes());
//		modCanard.setYApexConstructionAxes(canard.getYApexConstructionAxes());
//		modCanard.setZApexConstructionAxes(canard.getZApexConstructionAxes());
//		modCanard.setRiggingAngle(modRigging);	
		
		canard.setXApexConstructionAxes(canard.getXApexConstructionAxes().minus(Amount.valueOf(5, SI.METER)));
		
		System.out.println("Original values:");
		System.out.println("AR = " + origAR);
		System.out.println("span = " + origSpan);
		System.out.println("surface = " + origSurface);
		System.out.println("root chord = " + origRootChord);
		System.out.println("taper = " + origTaper);
		System.out.println("sweep LE = " + origSweepLE.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("dihedral = " + origDihedralLE.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("tip twist = " + origTipTwist.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("rigging angle = " + origRigging.doubleValue(NonSI.DEGREE_ANGLE));

		System.out.println("");		
		System.out.println("Modded values:");
		System.out.println("AR = " + modCanard.getAspectRatio());
		System.out.println("span = " + modCanard.getEquivalentWing().getPanels().get(0).getSpan().times(2).doubleValue(SI.METRE));
		System.out.println("surface = " + modCanard.getEquivalentWing().getPanels().get(0).getSurfacePlanform().times(2).doubleValue(SI.SQUARE_METRE));
		System.out.println("root chord = " + modCanard.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER));
		System.out.println("taper = " + modCanard.getEquivalentWing().getPanels().get(0).getTaperRatio());
		System.out.println("sweep LE = " + modCanard.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("dihedral = " + modCanard.getEquivalentWing().getPanels().get(0).getDihedral().doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("tip twist = " + modCanard.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip().doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("rigging angle = " + modCanard.getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE));

		// Generate the CAD
		List<OCCShape> canardShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				canard, WingTipType.ROUNDED, false, false, true);
		
		List<OCCShape> modcanardShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				modCanard, WingTipType.ROUNDED, false, false, true);
		
		// Export to file
		List<OCCShape> allShapes = new ArrayList<>();
		allShapes.addAll(canardShapes);
		allShapes.addAll(modcanardShapes);
		
		OCCUtils.write("IRON_canard", FileExtension.STEP, allShapes);				
	}

}
