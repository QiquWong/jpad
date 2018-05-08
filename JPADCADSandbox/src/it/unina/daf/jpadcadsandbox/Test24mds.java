package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.WingAdjustCriteriaEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils.FileExtension;
import standaloneutils.atmosphere.AtmosphereCalc;

public class Test24mds {

	public static void main(String[] args) {

		System.out.println("JPADCADSandbox Test");

		
//		StdAtmos1976 atmo = AtmosphereCalc.getAtmosphere(1000);
		
		Aircraft aircraft = AircraftUtils.importAircraft(args);	
		
		Fuselage fuselage = aircraft.getFuselage();		
		LiftingSurface wing1 = AircraftUtils.importAircraft(args).getWing();		
		LiftingSurface wing2 = AircraftUtils.importAircraft(args).getWing();	
//		LiftingSurface horizontal = aircraft.getHTail();
//		LiftingSurface vertical = aircraft.getVTail();
//		LiftingSurface canard = aircraft.getCanard();
		
		System.out.println("Fuselage Length: " + fuselage.getFuselageLength().doubleValue(SI.METER));
		System.out.println("Wing MAC: " + wing1.getMeanAerodynamicChord());
		System.out.println("Wing MAC LE coordinates: " + wing1.getMeanAerodynamicChordLeadingEdge());
		System.out.println("Wing S planform: " + wing1.getSurfacePlanform());
		System.out.println("Wing Span: " + wing1.getSpan());
		System.out.println("Wing Moment Pole X coordinate: " + 
				(wing1.getXApexConstructionAxes().doubleValue(SI.METER) + 
				 wing1.getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER) + 
				 wing1.getMeanAerodynamicChord().doubleValue(SI.METER)*0.25)
				);
		
		// Modify wing 2
		wing2.adjustDimensions(
				wing1
							.getAspectRatio()*1.3,
				wing1.getEquivalentWing().getPanels().get(0)
							.getChordRoot().doubleValue(SI.METER)*1.1,
				wing1.getEquivalentWing().getPanels().get(0)
							.getChordTip().doubleValue(SI.METER)*0.9,
				wing1.getEquivalentWing().getPanels().get(0)
							.getSweepLeadingEdge().times(0.75),
				wing1.getEquivalentWing().getPanels().get(0)
							.getDihedral().times(0), 
				wing1.getEquivalentWing().getPanels().get(0)
							.getTwistGeometricAtTip(),
				WingAdjustCriteriaEnum.AR_ROOTCHORD_TIPCHORD
				);	
		
		wing2.setAirfoilList(wing1.getAirfoilList());	
		wing2.setXApexConstructionAxes(wing1.getXApexConstructionAxes().minus(Amount.valueOf(6, SI.METER)));
		wing2.setYApexConstructionAxes(wing1.getYApexConstructionAxes());
		wing2.setZApexConstructionAxes(wing1.getZApexConstructionAxes().minus(Amount.valueOf(0.1, SI.METER)));
		
		// Create the aircraft CAD file
		List<OCCShape> allShapes = new ArrayList<>();
		
		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, 7, 7, true, true, false);
		List<OCCShape> wing1Shapes = AircraftUtils.getLiftingSurfaceCAD(wing1, ComponentEnum.WING, 1e-3, false, true, false);
		List<OCCShape> wing2Shapes = AircraftUtils.getLiftingSurfaceCAD(wing2, ComponentEnum.WING, 1e-3, false, true, false);
//		List<OCCShape> horizontalShapes = AircraftUtils.getLiftingSurfaceCAD(horizontal, ComponentEnum.HORIZONTAL_TAIL, 1e-3, false, true, false);
//		List<OCCShape> verticalShapes = AircraftUtils.getLiftingSurfaceCAD(vertical, ComponentEnum.VERTICAL_TAIL, 1e-3, false, true, false);
//		List<OCCShape> canardShapes = AircraftUtils.getLiftingSurfaceCAD(canard, ComponentEnum.CANARD, 1e-3, false, true, false);
		
		allShapes.addAll(fuselageShapes);
		allShapes.addAll(wing1Shapes);
		allShapes.addAll(wing2Shapes);
//		allShapes.addAll(horizontalShapes);
//		allShapes.addAll(verticalShapes);
//		allShapes.addAll(canardShapes);
		
		AircraftUtils.getAircraftSolidFile(allShapes, "IRON-DoubleWing", FileExtension.STEP);
		
//		AircraftUtils.getAircraftSolidFile(fuselage2Shapes, "FUSELAGE_2", FileExtension.STEP);
//		AircraftUtils.getAircraftSolidFile(wing1Shapes, "WING_1", FileExtension.STEP);
//		AircraftUtils.getAircraftSolidFile(wing2Shapes, "WING_2", FileExtension.STEP);
//		AircraftUtils.getAircraftSolidFile(horizontalShapes, "HORIZONTAL", FileExtension.STEP);
//		AircraftUtils.getAircraftSolidFile(verticalShapes, "VERTICAL", FileExtension.STEP);
	}
}
