package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.WingAdjustCriteriaEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils.FileExtension;

public class Test33mds {

	public static void main(String[] args) {
		
		System.out.println("---------------------------------------------");
		System.out.println("--------- Adjust Dimension Test -------------");
		System.out.println("---------------------------------------------");
		
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		LiftingSurface canard = aircraft.getCanard();
		
		double origSpan = canard.getEquivalentWing().getPanels().get(0).getSpan().times(2).doubleValue(SI.METRE);
		double origSurface = canard.getEquivalentWing().getPanels().get(0).getSurfacePlanform().times(2).doubleValue(SI.SQUARE_METRE);
		double origTaper = canard.getEquivalentWing().getPanels().get(0).getTaperRatio();
		Amount<Angle> origSweepLE = canard.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge();
		Amount<Angle> origDihedralLE = canard.getEquivalentWing().getPanels().get(0).getDihedral();
		Amount<Angle> origTipTwist = canard.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip();
		
		// Modification 
		LiftingSurface modCanard = AircraftUtils.importAircraft(args).getCanard();
		
		double spanFactor = 0.85;
		
		double modSpan = modCanard.getEquivalentWing().getPanels().get(0).getSpan().times(2).doubleValue(SI.METRE)*spanFactor;
		double modSurface = modCanard.getEquivalentWing().getPanels().get(0).getSurfacePlanform().times(2).doubleValue(SI.SQUARE_METRE);
		double modTaper = modCanard.getEquivalentWing().getPanels().get(0).getTaperRatio();
		Amount<Angle> modSweepLE = modCanard.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().times(1.50);
		Amount<Angle> modDihedralLE = modCanard.getEquivalentWing().getPanels().get(0).getDihedral();
		Amount<Angle> modTipTwist = modCanard.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip();
		
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
		System.out.println("");		
		System.out.println("Modded values:");
		System.out.println("span = " + modSpan);
		System.out.println("surface = " + modSurface);
		System.out.println("taper = " + modTaper);
		System.out.println("sweep LE = " + modSweepLE.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("dihedral = " + modDihedralLE.doubleValue(NonSI.DEGREE_ANGLE));
		System.out.println("tip twist = " + modTipTwist.doubleValue(NonSI.DEGREE_ANGLE));
		
		modCanard.setAirfoilList(canard.getAirfoilList());	
		modCanard.setXApexConstructionAxes(canard.getXApexConstructionAxes().plus(Amount.valueOf(5, SI.METER)));
		
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
