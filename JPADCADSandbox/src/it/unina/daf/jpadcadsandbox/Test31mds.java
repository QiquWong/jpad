package it.unina.daf.jpadcadsandbox;

import java.util.List;

import javax.measure.unit.SI;

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
		
		LiftingSurface wing = aircraft.getWing();
		LiftingSurface hTail = aircraft.getHTail();
		
		System.out.println("----------------------------------------------");
		System.out.println("WING ADJUST DIMENSION TESTING ----------------");
		System.out.println("----------------------------------------------");
		
//		System.out.println("AR = " + hTail.getAspectRatio());
//		System.out.println("Root chord = " + hTail.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER));
//		System.out.println("Tip chord = " + hTail.getEquivalentWing().getPanels().get(0).getChordTip().doubleValue(SI.METER));
//		System.out.println("Area = " + hTail.getSurfacePlanform().doubleValue(SI.SQUARE_METRE));
//		System.out.println("Span = " + hTail.getSpan().doubleValue(SI.METER));
//		
//		hTail.adjustDimensions(
//				hTail.getSpan().doubleValue(SI.METER), 
//				hTail.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER), 
//				hTail.getEquivalentWing().getPanels().get(0).getChordTip().doubleValue(SI.METER), 
//				hTail.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(), 
//				hTail.getEquivalentWing().getPanels().get(0).getDihedral(), 
//				hTail.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(), 
//				WingAdjustCriteriaEnum.SPAN_ROOTCHORD_TIPCHORD
//				);
//		
//		System.out.println("AR = " + hTail.getAspectRatio());
//		System.out.println("Root chord = " + hTail.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER));
//		System.out.println("Tip chord = " + hTail.getEquivalentWing().getPanels().get(0).getChordTip().doubleValue(SI.METER));
//		System.out.println("Area = " + hTail.getSurfacePlanform().doubleValue(SI.SQUARE_METRE));
//		System.out.println("Span = " + hTail.getSpan().doubleValue(SI.METER));
		
		System.out.println("AR = " + wing.getAspectRatio());
		System.out.println("Root chord = " + wing.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER));
		System.out.println("Tip chord = " + wing.getEquivalentWing().getPanels().get(0).getChordTip().doubleValue(SI.METER));
		System.out.println("Area = " + wing.getSurfacePlanform().doubleValue(SI.SQUARE_METRE));
		System.out.println("Span = " + wing.getSpan().doubleValue(SI.METER));
		
		wing.adjustDimensions(
				wing.getAspectRatio(),
				wing.getEquivalentWing().getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)*2, 
				wing.getEquivalentWing().getPanels().get(0).getChordTip().doubleValue(SI.METER),
				wing.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(), 
				wing.getEquivalentWing().getPanels().get(0).getDihedral(), 
				wing.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(), 
				WingAdjustCriteriaEnum.AR_AREA_TIPCHORD
				);
		
		System.out.println("AR = " + wing.getAspectRatio());
		System.out.println("Root chord = " + wing.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER));
		System.out.println("Tip chord = " + wing.getEquivalentWing().getPanels().get(0).getChordTip().doubleValue(SI.METER));
		System.out.println("Area = " + wing.getSurfacePlanform().doubleValue(SI.SQUARE_METRE));
		System.out.println("Span = " + wing.getSpan().doubleValue(SI.METER));
		
		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-3, false, true, false);
		List<OCCShape> hTailShapes = AircraftUtils.getLiftingSurfaceCAD(hTail, ComponentEnum.HORIZONTAL_TAIL, 1e-3, false, true, false);
		
		AircraftUtils.getAircraftSolidFile(hTailShapes, "AdjDim_HTail_Test", FileExtension.STEP);
		AircraftUtils.getAircraftSolidFile(wingShapes, "AdjDim_Wing_Test", FileExtension.STEP);
	}

}
