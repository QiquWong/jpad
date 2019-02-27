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

import MReportUtils.ReportMaker;

//import com.mathworks.engine.*;
//import com.mathworks.matlab.types.*;

public class JPADReportUtils {

	//	public static void makeReport02(Aircraft aircraft, String pathToMatlabWorkingDirectory) throws Exception {
	//
	//		In Java Build Path: MATLAB_ROOT/extern/engines/java/jar/engine.jar
	//
	//		System.out.println("\t>> JPADReportUtils.makeReport02");
	//
	//		//Start MATLAB asynchronously
	//		Future<MatlabEngine> engineFuture = MatlabEngine.startMatlabAsync();
	//
	//		System.out.println("\t>> Starting Matlab engine asynchronously...");
	//
	//		// Get engine instance from the future result
	//		MatlabEngine engine = engineFuture.get();
	//
	//
	////		Map<String, Object> reportDataMap = Stream.of(new Object[][] { 
	////			{ "title", "Title from Java" }, 
	////			{ "subtitle", "Subtitle from Java" }, 
	////			{ "author", "jagodemar" }, 
	////			{ "wingSpan", aircraft.getWing().getSpan().doubleValue(SI.METER) }, 
	////		}).collect(Collectors.toMap(data -> (String) data[0], data -> (Object) data[1]));
	//
	//		Struct reportDataStruct = new Struct(
	//				"title", "Title from Java", 
	//				"subtitle", "Subtitle from Java", 
	//				"author", "jagodemar", 
	//				"wingSpan", aircraft.getWing().getSpan().doubleValue(SI.METER)				
	//				);
	//
	//		// make sure you are in the proper working folder
	//		
	//		engine.eval("cd " + pathToMatlabWorkingDirectory);
	//		engine.putVariableAsync("reportData", reportDataStruct);
	//		
	//		engine.feval(0, "makeReport02", "JPADReport_Test", "docx", reportDataStruct);
	//		
	//		System.out.println("\t>> Disconnecting Matlab engine...");
	//		engine.disconnect();
	//		
	//	}

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
		rhs.add(new MWCharArray(new String("JPADReport_Test")));
		
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

}
