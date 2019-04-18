package it.unina.daf.jpad.report.test;

import java.util.List;
import java.io.File;
import java.util.ArrayList;

import com.mathworks.toolbox.javabuilder.MWArray;

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
		//Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		try {
			System.out.println("------------------ JPADReport :: START -----------------");
			// JPADReportUtils.makeReport02a(aircraft, pathToMatlabWorkingDirectory);
			// JPADReportUtils.makeReport03(aircraft, pathToMatlabWorkingDirectory);
			
			MWArray report = JPADReportUtils.openReport(
					"Airbus A320", 
					"pdf", 
					new File("C:/Users/Prince/Desktop/fabrizio/DAF_template"), 
					"en", 
					"WikiPage: Airbus_A320", 
					new File("C:/Users/Prince/Desktop/fabrizio/Airbus_A320_prova.jpg"), 
					"UNINA",
					"Prince94"
					);
			
			MWArray chapter = JPADReportUtils.openChapter("Airbus A320 family");
			
			StringBuilder sb = new StringBuilder();
			sb
			.append("The Airbus A320 family consists of short-to medium-range,")
			.append("narrow body,commercial passenger twin-engine jet airliners")
			.append("manifactured by Airbus.");
			
			JPADReportUtils.addParagraph(chapter, sb.toString());
			
			File imageFile = new File("C:/Users/Prince/Desktop/fabrizio/Airbus_A320_prova.jpg");
			JPADReportUtils.addFigure(chapter, imageFile, "Airbus A320");
			
			JPADReportUtils.addSection(chapter, "Origins");
			
			StringBuilder sb1 = new StringBuilder();
			sb1
			.append("In june 1977 was set up a new Joint European Transport JET programme.")
			.append("It waIs based at the British Aerospace site in Weybridge,Surrey,UK.");
					
			JPADReportUtils.addParagraph(chapter, sb1.toString());
			
		
			List<String> head = new ArrayList<String>();
		
			
			
			List<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
			ArrayList<String> l1 = new ArrayList<String>();
			ArrayList<String> l2 = new ArrayList<String>();
			
			l1.add(new String("wingSpan"));
			l1.add(new String("25"));
			l2.add(new String("wingArea"));
			l2.add(new String("180.0"));
			data.add(l1);
			data.add(l2);
			
			
			
			head.add("Geometrical Parameters");
			head.add("value");
			
			
			JPADReportUtils.addTable(chapter, data, head, "Specifications");
			
			JPADReportUtils.closeChapter(report,chapter);
			
			
			JPADReportUtils.closeReport(report);
			
			System.out.println("------------------ JPADReport :: COMPLETE -----------------");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("\n\tDONE.");

	}

}
