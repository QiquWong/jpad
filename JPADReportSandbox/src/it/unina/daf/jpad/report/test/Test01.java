package it.unina.daf.jpad.report.test;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import it.unina.daf.jpad.report.AircraftUtils;
import it.unina.daf.jpad.report.CmdLineUtils;
import it.unina.daf.jpad.report.JPADReportUtils;

public class Test01 {

	public static void main(String[] args) {
		System.out.println("-------------------------------------------------------------");
		System.out.println("------------------ JPADReport modeling test -----------------");
		System.out.println("-------------------------------------------------------------");

		// ----------------------
		// Import the aircraft
		// ----------------------
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface wing = aircraft.getWing();
		LiftingSurface hTail = aircraft.getHTail();
		LiftingSurface vTail = aircraft.getVTail();
		
		String pathToMatlabWorkingDirectory = CmdLineUtils.va.getMatlabWorkingDirectory().getAbsolutePath();
		
		try {
			System.out.println("------------------ JPADReport :: START -----------------");
			//JPADReportUtils.makeReport02a(aircraft, pathToMatlabWorkingDirectory);
			
			JPADReportUtils.makeReport03(aircraft, pathToMatlabWorkingDirectory);
			
			System.out.println("------------------ JPADReport :: COMPLETE -----------------");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("\n\tDONE.");

	}

}
