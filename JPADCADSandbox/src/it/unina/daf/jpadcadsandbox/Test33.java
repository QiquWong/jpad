package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.WingAdjustCriteriaEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils.FileExtension;

public class Test33 {

	public static void main(String[] args) {
		
		System.out.println("---------------------------------------------");
		System.out.println("--------- Adjust Dimension Test -------------");
		System.out.println("---------------------------------------------");
		
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		LiftingSurface canard = aircraft.getCanard();
		LiftingSurface wing = aircraft.getWing();
		
		
		
		
		double origSpan = canard.getEquivalentWing().getPanels().get(0).getSpan().times(2).doubleValue(SI.METRE);
		double origSurface = canard.getEquivalentWing().getPanels().get(0).getSurfacePlanform().times(2).doubleValue(SI.SQUARE_METRE);
		double origTaper = canard.getEquivalentWing().getPanels().get(0).getTaperRatio();
		Amount<Angle> origSweepLE = canard.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE);
		Amount<Angle> origDihedralLE = canard.getEquivalentWing().getPanels().get(0).getDihedral().to(NonSI.DEGREE_ANGLE);;
		Amount<Angle> origTipTwist = canard.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip();
		Amount<Angle> origRigging = canard.getRiggingAngle().to(NonSI.DEGREE_ANGLE);;
		
		// Modification 
		LiftingSurface modCanard = AircraftUtils.importAircraft(args).getCanard();
		
		double spanFactor = 1.15;
		
		double modSpan = modCanard.getEquivalentWing().getPanels().get(0).getSpan().times(2).doubleValue(SI.METRE)*spanFactor;
		double modSurface = modCanard.getEquivalentWing().getPanels().get(0).getSurfacePlanform().times(2).doubleValue(SI.SQUARE_METRE);
		double modTaper = modCanard.getEquivalentWing().getPanels().get(0).getTaperRatio();
		Amount<Angle> modSweepLE = modCanard.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().minus(Amount.valueOf(10,NonSI.DEGREE_ANGLE));//times(1.50);
		Amount<Angle> modDihedralLE = modCanard.getEquivalentWing().getPanels().get(0).getDihedral().plus(Amount.valueOf(+6,NonSI.DEGREE_ANGLE));;
		Amount<Angle> modTipTwist = modCanard.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip();
		Amount<Angle> modRigging = modCanard.getRiggingAngle().plus(Amount.valueOf(-2, NonSI.DEGREE_ANGLE));
		
		modCanard.adjustDimensions(
				modSpan, 
				modSurface, 
				modTaper, 
				modSweepLE, 
				modDihedralLE, 
				modTipTwist, 
				WingAdjustCriteriaEnum.SPAN_AREA_TAPER
				);
		
		

		modCanard.setAirfoilList(canard.getAirfoilList());	
		modCanard.setXApexConstructionAxes(canard.getXApexConstructionAxes());//.plus(Amount.valueOf(5, SI.METER)));
		Amount<Length> modZPosCanard = modCanard.getZApexConstructionAxes().plus(Amount.valueOf(-3.5, SI.METER));
		modCanard.setRiggingAngle(modRigging);

		
		
		System.out.println("Original values:");
		System.out.println("span = " + origSpan);
		//System.out.println("surface = " + origSurface);
		//System.out.println("taper = " + origTaper);
		System.out.println("sweep LE = " + origSweepLE.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("dihedral = " + origDihedralLE.doubleValue(NonSI.DEGREE_ANGLE));
		//System.out.println("tip twist = " + origTipTwist.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("rigging angle = " + origRigging.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("Z Pos Wing = " + wing.getZApexConstructionAxes().doubleValue(SI.METER));
		System.out.println("Z Pos Canard = " + canard.getZApexConstructionAxes().doubleValue(SI.METER));

		System.out.println("");		
		System.out.println("Modded values:");
		System.out.println("span = " + modSpan);
		//System.out.println("surface = " + modSurface);
		//System.out.println("taper = " + modTaper);
		System.out.println("sweep LE = " + modSweepLE.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("dihedral = " + modDihedralLE.doubleValue(NonSI.DEGREE_ANGLE));
		//System.out.println("tip twist = " + modTipTwist.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("rigging angle = " + modRigging.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("Z Pos Wing = " + wing.getZApexConstructionAxes().doubleValue(SI.METER));
		System.out.println("Z Pos Canard = " + modZPosCanard.doubleValue(SI.METER));
		
		
		
		
		// Generate the CAD
		List<OCCShape> canardShapes = AircraftUtils.getLiftingSurfaceCAD(
				canard, canard.getType(), 1e-3, false, true, false);
		
		List<OCCShape> modcanardShapes = AircraftUtils.getLiftingSurfaceCAD(
				modCanard, modCanard.getType(), 1e-3, false, true, false);
		
		// Export to file
		List<OCCShape> allShapes = new ArrayList<>();
		allShapes.addAll(canardShapes);
		allShapes.addAll(modcanardShapes);
		
		AircraftUtils.getAircraftSolidFile(allShapes, "test33mds", FileExtension.STEP);
				
	}

}
