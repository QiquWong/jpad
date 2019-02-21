package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.enums.WingTipType;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;

public class Test31mds {

	public static void main(String[] args) {
		
		System.out.println("---------------------------------------------");
		System.out.println("--------- AircraftCADUtils Test -------------");
		System.out.println("---------------------------------------------");
		
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface wing = aircraft.getWing();
		LiftingSurface canard = aircraft.getCanard();
		LiftingSurface horizontal = aircraft.getHTail();
		LiftingSurface vertical = aircraft.getVTail();
		
//		fuselage.setXApexConstructionAxes(Amount.valueOf(-1, SI.METER));
//		fuselage.setYApexConstructionAxes(Amount.valueOf(2, SI.METER));
//		fuselage.setZApexConstructionAxes(Amount.valueOf(1, SI.METER));
//		wing.setYApexConstructionAxes(Amount.valueOf(-3, SI.METER));
//		vertical.setYApexConstructionAxes(Amount.valueOf(-1, SI.METER));
//		vertical.setZApexConstructionAxes(Amount.valueOf(2, SI.METER));
		
//		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, 
//				7, 7, 
//				true, true, false);
//		
//		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, 
////				WingTipType.ROUNDED, 
//				ComponentEnum.WING, 1e-3,
//				true, false, false);
//		
//		List<OCCShape> canardShapes = AircraftUtils.getLiftingSurfaceCAD(canard, 
////				WingTipType.ROUNDED, 
//				ComponentEnum.CANARD, 1e-3,
//				true, false, false);		
//		
//		List<OCCShape> horizontalShapes = AircraftUtils.getLiftingSurfaceCAD(horizontal, 
////				WingTipType.ROUNDED, 
//				ComponentEnum.HORIZONTAL_TAIL, 1e-3,
//				true, false, false);
//		
//		List<OCCShape> verticalShapes = AircraftUtils.getLiftingSurfaceCAD(vertical, 
////				WingTipType.ROUNDED, 
//				ComponentEnum.VERTICAL_TAIL, 1e-3,
//				true, false, false);
		
		List<OCCShape> fuselageShapes = AircraftCADUtils.getFuselageCAD(fuselage, 
				7, 7, 
				true, false, true);
		
		List<OCCShape> wingShapes = AircraftCADUtils.getLiftingSurfaceCAD(wing, 
				WingTipType.ROUNDED, 
//				ComponentEnum.WING, 1e-3,
				true, false, true);
		
//		List<OCCShape> canardShapes = AircraftCADUtils.getLiftingSurfaceCAD(canard, 
//				WingTipType.ROUNDED, 
////				ComponentEnum.CANARD, 1e-3,
//				true, false, false);		
		
		List<OCCShape> horizontalShapes = AircraftCADUtils.getLiftingSurfaceCAD(horizontal, 
				WingTipType.ROUNDED, 
//				ComponentEnum.HORIZONTAL_TAIL, 1e-3,
				true, false, true);
		
		List<OCCShape> verticalShapes = AircraftCADUtils.getLiftingSurfaceCAD(vertical, 
				WingTipType.ROUNDED, 
//				ComponentEnum.VERTICAL_TAIL, 1e-3,
				true, false, true);
		
		List<OCCShape> allShapes = new ArrayList<>();
//		allShapes.addAll(fuselageShapes);
		allShapes.addAll(wingShapes);
//		allShapes.addAll(horizontalShapes);
//		allShapes.addAll(verticalShapes);
//		allShapes.addAll(canardShapes);
		
		String filename = "AircraftCADUtils_Test";
		OCCUtils.write(filename, FileExtension.STEP, allShapes);
		
//		String filename = "AircraftCADUtils_Test.brep";
//		
//		if (OCCUtils.write(filename, fuselageShapes, wingShapes, horizontalShapes, canardShapes))
//			System.out.println("[Test31mds] CAD shapes correctly written to file (" + filename + ")");
	}

}
