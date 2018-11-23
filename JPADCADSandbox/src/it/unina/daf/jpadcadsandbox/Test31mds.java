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
import configuration.enumerations.WingAdjustCriteriaEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftCADUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftCADUtils.WingTipType;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils.FileExtension;

public class Test31mds {

	public static void main(String[] args) {
		
		System.out.println("---------------------------------------------");
		System.out.println("--------- AircraftCADUtils Test -------------");
		System.out.println("---------------------------------------------");
		
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface wing = aircraft.getWing();
		LiftingSurface horizontal = aircraft.getHTail();
		LiftingSurface vertical = aircraft.getVTail();
		
		fuselage.setYApexConstructionAxes(Amount.valueOf(2, SI.METER));
		wing.setYApexConstructionAxes(Amount.valueOf(-3, SI.METER));
		vertical.setYApexConstructionAxes(Amount.valueOf(-1, SI.METER));
		vertical.setZApexConstructionAxes(Amount.valueOf(2, SI.METER));
		
		List<OCCShape> fuselageShapes = AircraftCADUtils.getFuselageCAD(fuselage, 
				10, 7, 
				true, true, false);
		
		List<OCCShape> wingShapes = AircraftCADUtils.getLiftingSurfaceCAD(wing, 
				WingTipType.ROUNDED, 
//				ComponentEnum.WING, 1e-3,
				true, true, false);
		
		List<OCCShape> horizontalShapes = AircraftCADUtils.getLiftingSurfaceCAD(horizontal, 
				WingTipType.ROUNDED, 
//				ComponentEnum.HORIZONTAL_TAIL, 1e-3,
				true, true, false);
		
		List<OCCShape> verticalShapes = AircraftCADUtils.getLiftingSurfaceCAD(vertical, 
				WingTipType.ROUNDED, 
//				ComponentEnum.VERTICAL_TAIL, 1e-3,
				true, true, false);
		
		String filename = "AircraftCADUtils_Test.brep";
		
		if (OCCUtils.write(filename, fuselageShapes, wingShapes, horizontalShapes, verticalShapes))
			System.out.println("[Test31mds] CAD shapes correctly written to file (" + filename + ")");
	}

}
