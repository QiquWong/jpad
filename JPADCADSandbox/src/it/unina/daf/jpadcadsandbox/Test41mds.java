package it.unina.daf.jpadcadsandbox;

import java.util.List;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.powerplant.Engine;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;

public class Test41mds {

	public static void main(String[] args) {
		System.out.println("-------------------------------------------------------------");
		System.out.println("------------------ CAD engine modeling test -----------------");
		System.out.println("-------------------------------------------------------------");
		
		// ------------------------
		// Initialize the factory
		// ------------------------
		if (OCCUtils.theFactory == null) 
			OCCUtils.initCADShapeFactory();
		
		// ----------------------
		// Import the aircraft
		// ----------------------
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface wing = aircraft.getWing();
		LiftingSurface hTail = aircraft.getHTail();
		LiftingSurface vTail = aircraft.getVTail();
		
		List<NacelleCreator> nacelles = aircraft.getNacelles().getNacellesList();
		List<Engine> engines = aircraft.getPowerPlant().getEngineList();
		
		// -----------------------
		// Generate the 

	}

}
