package it.unina.daf.jpad.report;

import aircraft.Aircraft;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.unit.SI;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWCellArray;
import com.mathworks.toolbox.javabuilder.MWCharArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import com.mathworks.toolbox.javabuilder.remoting.AbstractMWArrayVisitor;
import com.sun.prism.Image;

import MReportUtils.MReportUtils;

//import com.mathworks.engine.*;
//import com.mathworks.matlab.types.*;

public class JPADReportUtils {

	private static MReportUtils reportFactory;
	

	/**
	 * Return factory instance
	 * @return factory instance
	 */
	private static void initializeReportFactory() {
		if (reportFactory == null) {
			try {
				reportFactory = new MReportUtils();
			}
			catch (Exception e) {
				System.err.println("Unable to create a Report Factory object!");
				System.exit(1);
			}
		}
	}
	
	public static MReportUtils getReportFactory() {
		initializeReportFactory();
		return reportFactory;
	}

	public static void setReportFactory (MReportUtils f) {
		if (reportFactory == null)
			reportFactory = f;
		else
			throw new IllegalStateException("Factory already set to "+ reportFactory +".");
	}
	
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
//
//	public static void makeReport03(Aircraft aircraft, String pathToMatlabWorkingDirectory) throws Exception {
//
//		/* Matlab call:
//		 * 
//			rpt = makeReport('Airbus A320', 'pdf', 'DAF_template', 'en', ...
//	                'WikiPage: Airbus_A320 ', '_figures/Airbus_A320_prova.jpg','UNINA', 'Prince94');
//		 */
//
//
//		MReportUtils reportUtilsObj = new MReportUtils();
//
//		List<MWArray> lhsRpt = new ArrayList<>(); // this remains empty, means 0 output
//		List<MWArray> rhsRpt = new ArrayList<>();
//
//		// add "fileName" makeReport02.m
//		rhsRpt.add(new MWCharArray(new String("Airbus A320")));
//		rhsRpt.add(new MWCharArray(new String("pdf")));
//		rhsRpt.add(new MWCharArray(new String("DAF_template")));
//		rhsRpt.add(new MWCharArray(new String("en")));
//		rhsRpt.add(new MWCharArray(new String("WikiPage: Airbus_A320")));
//		rhsRpt.add(new MWCharArray(new String("_figures/Airbus_A320_prova.jpg")));
//		rhsRpt.add(new MWCharArray(new String("UNINA")));
//		rhsRpt.add(new MWCharArray(new String("Prince94")));
//
//		// return object
//		lhsRpt.add(new MWStructArray());
//
//		System.out.println("[makeReport03] writing a report...");
//
//		reportUtilsObj.makeReport(lhsRpt, rhsRpt);
//
//		// ......................................... CREATE A CHAPTER
//
//		/* Matlab call:
//		 * 
//			[chapt] = makeChapter('Airbus A320 Family');
//		 * 
//		 */
//
//		List<MWArray> lhsCh1 = new ArrayList<>(); // this remains empty, means 0 output
//		List<MWArray> rhsCh1 = new ArrayList<>();
//
//		// add chapter title
//		rhsCh1.add(new MWCharArray(new String("Airbus A320 Family")));
//
//		// return object
//		lhsCh1.add(new MWStructArray());
//
//		reportUtilsObj.makeChapter(lhsCh1, rhsCh1);
//
//		// ......................................... CREATE A PARAGRAPH
//		/* Matlab call:
//		 * 
//			makeParagraph(chapt,['The Airbus A320 family consists of short-to medium-range,'...
//			                     'narrow body,commercial passenger twin-engine jet airliners'...
//			                     'manifactured by Airbus.']); %to add a paragraph to the current chapter
//		 */
//		StringBuilder sb = new StringBuilder();
//		sb
//		.append("The Airbus A320 family consists of short-to medium-range,")
//		.append("narrow body,commercial passenger twin-engine jet airliners")
//		.append("manifactured by Airbus.");
//
//		reportUtilsObj.makeParagraph(
//				lhsCh1.get(0), // this chapter owns the paragraphs 
//				sb.toString()
//				);
//		// ......................................... CREATE A SECTION
//		/* Matlab call:
//		 * [chapt]= makeSection(chapt,'Origins');
//		 */
//		List<MWArray> lhsSec1 = new ArrayList<>(); //this remains empty, means 0 output
//		List<MWArray> rhsSec1 = new ArrayList<>();
//
//		// add section title title
//
//		rhsSec1.add(lhsCh1.get(0));
//		rhsSec1.add(new MWCharArray(new String("Origins")));
//
//		reportUtilsObj.makeSection(lhsSec1,rhsSec1);
//
//		//.......................................CREATE A FIGURE 
//		/* Matlab call:
//		 * makeFigure(chapt,'_figures/Airbus_A320_prova.jpg','Airbus A320');
//		 */
//		List<MWArray> lhsFig = new ArrayList<>();
//		List<MWArray> rhsFig = new ArrayList<>();
//
//		File imagefile = new File("C:/Users/Prince/Desktop/fabrizio/Airbus_A320_prova.jpg");
//
//		rhsFig.add(lhsCh1.get(0));
//		rhsFig.add(new MWCharArray(imagefile.getAbsolutePath()));
//		rhsFig.add(new MWCharArray(new String("Airbus A320.jpg")));
//
//
//		reportUtilsObj.makeFigure(lhsFig, rhsFig);
//
//		//.......................................CREATE A TABLE
//
//		/* Matlab call:
//		 * makeTable(ch,C,header,table_title);
//		 */
//		List<MWArray> lhsTb1 = new ArrayList<>(); // this remains empty, means 0 output
//		List<MWArray> rhsTb1 = new ArrayList<>();
//
//		int [] dimT = {3,3}, dimH = {1,3};
//
//		MWCellArray table = new MWCellArray(dimT);
//		MWCellArray Header = new MWCellArray(dimH);
//		int[] d11 = {1,1}, d12 = {1,2}, d13 = {1,3};
//
//		table.set(d11,  new MWCharArray(new String("wing span")));
//		table.set(d12, aircraft.getWing().getSpan().toString());
//		table.set(d13,  new MWCharArray(new String("m")));	
//		Header.set(d11, new MWCharArray(new String("Geometrical Parameters")));
//		Header.set(d12, new MWCharArray(new String("Value")));
//		Header.set(d13, new MWCharArray(new String("Dimensions")));
//
//		rhsTb1.add(lhsCh1.get(0));
//		rhsTb1.add(table);
//		rhsTb1.add(Header);
//		rhsTb1.add(new MWCharArray(new String("Specifications")));
//
//		reportUtilsObj.makeTable(lhsTb1,rhsTb1);
//		
////		List<MWArray> lhsTb1 = new ArrayList<>(); // this remains empty, means 0 output
////		List<MWArray> rhsTb1 = new ArrayList<>();
////
////
////		int[] sdims = {1, 2};
////		String[] sfields = {
////				"wingSpan",			// 1
////				"wingAspectRatio"	// 2
////		};	
////
////		MWStructArray reportDataStructArray = new MWStructArray(sdims, sfields);
////		//int totNumElem = reportDataStructArray.numberOfElements() * reportDataStructArray.numberOfFields();
////
////		reportDataStructArray.set(1, aircraft.getWing().getSpan().toString()); // TODO: pass it as a String containing unit?
////		reportDataStructArray.set(2, aircraft.getWing().getAspectRatio().toString());
////       
////		// set the Header of the Table
////		MWCellArray Header = new MWCellArray(sdims);
////		int[] d11 = {1,1}, d12 = {1,2};
////
////		Header.set(d11, new MWCharArray(new String("Geometrical Parameters")));
////		Header.set(d12, new MWCharArray(new String("Value")));
////
////		rhsTb1.add(lhsCh1.get(0));
////		rhsTb1.add(reportDataStructArray);
////		rhsTb1.add(Header);
////		rhsTb1.add(new MWCharArray(new String("Specifications")));
////
////
//// 
////		reportUtilsObj.makeTab(lhsTb1, rhsTb1);
//		
//		// ......................................... CLOSE THE CHAPTER
//		reportUtilsObj.closeChapter(lhsRpt.get(0), lhsCh1.get(0));
//
//		// ......................................... CLOSE THE REPORT
//		reportUtilsObj.closeReport(lhsRpt.get(0));
//
//		System.out.println("[makeReport03] done.");
//	}

	public static MWArray openReport (
			String reportName,
			String fileType,
			File templateFile,
			String language,
			String reportTitle,
			File coverImageFile,
			String publisher,
			String authors
			) throws MWException {
		
		initializeReportFactory();
		
		List<MWArray> lhsRpt = new ArrayList<>(); // this remains empty, means 0 output
		List<MWArray> rhsRpt = new ArrayList<>();

		rhsRpt.add(new MWCharArray(reportName));
		rhsRpt.add(new MWCharArray(fileType));
		rhsRpt.add(new MWCharArray(templateFile.getAbsolutePath()));
		rhsRpt.add(new MWCharArray(language));
		rhsRpt.add(new MWCharArray(reportTitle));
		rhsRpt.add(new MWCharArray(coverImageFile.getAbsolutePath()));
		rhsRpt.add(new MWCharArray(publisher));
		rhsRpt.add(new MWCharArray(authors));

		// return object
		lhsRpt.add(new MWStructArray());

		System.out.println("[JPADReport] Opening Report structure ...");

		reportFactory.makeReport(lhsRpt, rhsRpt);

		return lhsRpt.get(0);
		
	}

	public static void closeReport(MWArray report) throws MWException {
		
		initializeReportFactory();
		reportFactory.closeReport(report);
		
	}

	public static MWArray openChapter( String chapterTitle) throws MWException {
		
		initializeReportFactory();
		
		List<MWArray> lhsCh = new ArrayList<>(); // this remains empty, means 0 output
	    List<MWArray> rhsCh = new ArrayList<>();
	
	    // add chapter title
	    rhsCh.add(new MWCharArray(chapterTitle));
	 
	    // return object
	    lhsCh.add(new MWStructArray());
	    
	    reportFactory.makeChapter(lhsCh, rhsCh);
	    return lhsCh.get(0);
	    
	}
	
	public static void closeChapter(
			MWArray report,
			MWArray chapter) throws MWException {
		
		initializeReportFactory();
		
		reportFactory.closeChapter(report,chapter);
		
	}
	
	public static void addSection(
			MWArray chapter ,
			String sectionTitle) throws MWException {
		
		initializeReportFactory();
		List<MWArray> lhsSec = new ArrayList<>(); //this remains empty, means 0 output
		List<MWArray> rhsSec = new ArrayList<>();
    	// add section title title
		rhsSec.add(chapter);
	    rhsSec.add(new MWCharArray(sectionTitle));

	    reportFactory.makeSection(lhsSec,rhsSec);
		
	}
	
	public static void addFigure (
			MWArray chapter,
			File imagefile,
			String imageTitle
			) throws MWException {
		
		initializeReportFactory();
		List<MWArray> lhsFig = new ArrayList<>();
		List<MWArray> rhsFig = new ArrayList<>();



		rhsFig.add(chapter);
	    rhsFig.add(new MWCharArray(imagefile.getAbsolutePath()));
		rhsFig.add(new MWCharArray(imageTitle));


		reportFactory.makeFigure(lhsFig, rhsFig);
		
	}
	
	public static void addParagraph (
			MWArray chapter,
			String text) throws MWException {

		initializeReportFactory();
		
	reportFactory.makeParagraph(chapter, // this chapter owns the paragraphs 
			 text);
	}
	
}
