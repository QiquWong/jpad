package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.WingAdjustCriteriaEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils.FileExtension;

public class Test31mds {

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");
		
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		LiftingSurface originalWing = aircraft.getWing();
		LiftingSurface modWing_0 = AircraftUtils.importAircraft(args).getWing();
		LiftingSurface modWing_1 = AircraftUtils.importAircraft(args).getWing();
		
		modWing_0.getPanels().get(0).setSweepAtLeadingEdge(Amount.valueOf(25, NonSI.DEGREE_ANGLE));
		modWing_0.getPanels().get(1).setSweepAtLeadingEdge(Amount.valueOf(45, NonSI.DEGREE_ANGLE));
		modWing_0.calculateGeometry(originalWing.getType(), originalWing.isMirrored());
		modWing_0.setAirfoilList(originalWing.getAirfoilList());
		
		modWing_1.getPanels().get(0).setSweepAtLeadingEdge(Amount.valueOf(25, NonSI.DEGREE_ANGLE));
		modWing_1.getPanels().get(1).setSweepAtLeadingEdge(Amount.valueOf(45, NonSI.DEGREE_ANGLE));
		modWing_1.calculateGeometry(originalWing.getType(), originalWing.isMirrored());
		modWing_1.setAirfoilList(originalWing.getAirfoilList());
		
		System.out.println("----------------------------------------------");
		System.out.println("WING ADJUST DIMENSION TESTING ----------------");
		System.out.println("----------------------------------------------");
		
		System.out.println("AR = " + modWing_1.getAspectRatio());
		System.out.println("Root chord = " + modWing_1.getPanels().get(0).getChordRoot().doubleValue(SI.METER));
		System.out.println("Kink chord = " + modWing_1.getPanels().get(0).getChordTip().doubleValue(SI.METER));
		System.out.println("Tip chord = " + modWing_1.getPanels().get(1).getChordTip().doubleValue(SI.METER));
		System.out.println("Area = " + modWing_1.getSurfacePlanform().doubleValue(SI.SQUARE_METRE));
		System.out.println("Span = " + modWing_1.getSpan().doubleValue(SI.METER));
		System.out.println("Sweep LE panel 1 = " + modWing_1.getPanels().get(0).getSweepLeadingEdge());
		System.out.println("Sweep LE panel 2 = " + modWing_1.getPanels().get(1).getSweepLeadingEdge());
		
		int i = 0;
		while(i < 10) {
			modWing_1.adjustDimensions(
					modWing_1.getEquivalentWing().getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)*2,
					modWing_1.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER), 
					modWing_1.getEquivalentWing().getPanels().get(0).getChordTip().doubleValue(SI.METER),
					modWing_1.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(), 
					modWing_1.getEquivalentWing().getPanels().get(0).getDihedral(), 
					modWing_1.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(), 
					WingAdjustCriteriaEnum.AREA_ROOTCHORD_TIPCHORD
					);
			
			i++;
		}
		
		modWing_1.setXApexConstructionAxes(modWing_0.getXApexConstructionAxes().plus(Amount.valueOf(10, SI.METER)));
//		modWing_1.setZApexConstructionAxes(modWing_0.getZApexConstructionAxes().plus(Amount.valueOf(3, SI.METER)));
		
		System.out.println("AR = " + modWing_1.getAspectRatio());
		System.out.println("Root chord = " + modWing_1.getPanels().get(0).getChordRoot().doubleValue(SI.METER));
		System.out.println("Kink chord = " + modWing_1.getPanels().get(0).getChordTip().doubleValue(SI.METER));
		System.out.println("Tip chord = " + modWing_1.getPanels().get(1).getChordTip().doubleValue(SI.METER));
		System.out.println("Area = " + modWing_1.getSurfacePlanform().doubleValue(SI.SQUARE_METRE));
		System.out.println("Span = " + modWing_1.getSpan().doubleValue(SI.METER));
		System.out.println("Sweep LE panel 1 = " + modWing_1.getPanels().get(0).getSweepLeadingEdge());
		System.out.println("Sweep LE panel 2 = " + modWing_1.getPanels().get(1).getSweepLeadingEdge());
		
		List<OCCShape> originalWingShapes = AircraftUtils.getLiftingSurfaceCAD(modWing_0, ComponentEnum.WING, 1e-3, false, true, false);
		List<OCCShape> modWingShapes = AircraftUtils.getLiftingSurfaceCAD(modWing_1, ComponentEnum.WING, 1e-3, false, true, false);
		
		List<OCCShape> allShapes = new ArrayList<>();
		allShapes.addAll(originalWingShapes);
		allShapes.addAll(modWingShapes);
		
		AircraftUtils.getAircraftSolidFile(allShapes, "AdjDim_Wing_Test", FileExtension.STEP);
	}

}
