package it.unina.daf.jpad.report;

import aircraft.Aircraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.unit.SI;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWCharArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;

import MReportUtils.MReportUtils;

//import com.mathworks.engine.*;
//import com.mathworks.matlab.types.*;

public class JPADReportUtils {

/*
	public static void makeReport02a(Aircraft aircraft, String pathToMatlabWorkingDirectory) throws Exception {

//		Map<String, Object> reportDataMap = Stream.of(new Object[][] { 
//			{ "title", "Title from Java" }, 
//			{ "subtitle", "Subtitle from Java" }, 
//			{ "author", "jagodemar" }, 
//			{ "wingSpan", aircraft.getWing().getSpan().doubleValue(SI.METER) }, 
//		}).collect(Collectors.toMap(data -> (String) data[0], data -> (Object) data[1]));

		ReportMaker reportMaker = new ReportMaker();

		List<MWArray> lhs = new ArrayList<>(); // this remains empty, means 0 output
		List<MWArray> rhs = new ArrayList<>();
		
		// add "fileName" makeReport02.m
		rhs.add(new MWCharArray(new String("JPADReport_Test_2")));
		
		// add "type" makeReport02.m
		rhs.add(new MWCharArray(new String("docx")));

		// Make a Struct according to class MWStructArray
		// https://it.mathworks.com/help/javabuilder/MWArrayAPI/com/mathworks/toolbox/javabuilder/MWStructArray.html
		int[] sdims = {1, 2};
		String[] sfields = {
				"title",			// 1 
				"subtitle", 		// 2
				"author",			// 3
				"wingSpan",			// 4
				"wingAspectRatio"	// 5
		};
		
		MWStructArray reportDataStructArray = new MWStructArray(sdims, sfields);
		int totNumElem = reportDataStructArray.numberOfElements() * reportDataStructArray.numberOfFields();

		reportDataStructArray.set(1, "Title from Java");
		reportDataStructArray.set(2, "Subtitle from Java");
		reportDataStructArray.set(3, "jagodemar");
		reportDataStructArray.set(4, aircraft.getWing().getSpan().doubleValue(SI.METER)); // TODO: pass it as a String containing unit?
		reportDataStructArray.set(5, aircraft.getWing().getAspectRatio());
		
		// finally add the Struct "reportData" makeReport02.m
		rhs.add(reportDataStructArray);
		
		reportMaker.makeReport02(lhs, rhs);

	}
*/
	
	public static void makeReport03(Aircraft aircraft, String pathToMatlabWorkingDirectory) throws Exception {
	
		/* Matlab call:
		 * 
			rpt = makeReport('Airbus A320', 'pdf', 'DAF_template', 'en', ...
	                'WikiPage: Airbus_A320 ', '_figures/Airbus_A320_prova.jpg','UNINA', 'Prince94');
		 */
		
		
		MReportUtils reportUtilsObj = new MReportUtils();
		
		List<MWArray> lhs = new ArrayList<>(); // this remains empty, means 0 output
		List<MWArray> rhs = new ArrayList<>();
		
		// add "fileName" makeReport02.m
		rhs.add(new MWCharArray(new String("Airbus A320")));
		rhs.add(new MWCharArray(new String("pdf")));
		rhs.add(new MWCharArray(new String("DAF_template")));
		rhs.add(new MWCharArray(new String("en")));
		rhs.add(new MWCharArray(new String("WikiPage: Airbus_A320")));
		rhs.add(new MWCharArray(new String("_figures/Airbus_A320_prova.jpg")));
		rhs.add(new MWCharArray(new String("UNINA")));
		rhs.add(new MWCharArray(new String("Prince94")));
		
		System.out.println("[makeReport03] writing a report...");
		
		reportUtilsObj.makeReport(lhs, rhs);
		
		System.out.println("[makeReport03] done.");
	}
	
}
