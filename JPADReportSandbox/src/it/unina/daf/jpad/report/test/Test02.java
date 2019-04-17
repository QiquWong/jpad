package it.unina.daf.jpad.report.test;

import java.io.File;

import com.mathworks.toolbox.javabuilder.MWArray;

import aircraft.Aircraft;
import it.unina.daf.jpad.report.AircraftUtils;
import it.unina.daf.jpad.report.JPADReportManager;
import it.unina.daf.jpad.report.JPADReportUtils;

public class Test02 {

	public static void main(String[] args) {
		
		System.out.println("-------------------------------------------------------------");
		System.out.println("------------------ JPADReport modeling test -----------------");
		System.out.println("-------------------------------------------------------------");

		// ----------------------
		// Import the aircraft
		// ----------------------
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		System.out.println(aircraft.toString());
		
		try {
			System.out.println("------------------ JPADReport :: START -----------------");

			JPADReportManager.createJPADReport(
					aircraft, 
					aircraft.getId(), 
					"pdf", 
					new File("DAF_template"), 
					"en", 
					aircraft.getId() + " Report", 
					new File("_figures/Airbus_A320_prova.jpg"), 
					"UNINA",
					"Prince94",
					"Aircraft", 
					"Analysis"
					);
			
			System.out.println("------------------ JPADReport :: COMPLETE -----------------");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("\n\tDONE.");
		
	}

}
