package it.unina.daf.jpad.report;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWException;

import aircraft.Aircraft;

public class JPADReportManager {

	public static void createJPADReport(
			Aircraft aircraft,
			String reportName,
			String fileType,
			File templateFile,
			String language,
			String reportTitle,
			File coverImageFile,
			String publisher,
			String authors,
			String aircraftChapterTitle,
			String analysisChapterTitle
			) throws MWException {
		
		if(aircraft != null) {
			
			MWArray report = JPADReportUtils.openReport(
					reportName, 
					fileType,
					templateFile, 
					language,
					reportTitle, 
					coverImageFile, 
					publisher, 
					authors
					);
			
			createAircraftChapter(aircraft, aircraftChapterTitle, report,templateFile);
			
			if(aircraft.getTheAnalysisManager() != null) {
			
				createAnalysisChapter(aircraft, analysisChapterTitle, report,templateFile);
				
			}
			else {
				System.err.println("WARNING (REPORT): THE ANALYSIS MANAGER OBJECT IS NULL!!");
			}
			
			JPADReportUtils.closeReport(report);
			
		}
		else {
			System.err.println("WARINING (REPORT): THE AIRCRAFT OBJECT IS NULL!!");
		}
		
	}
	
	private static void createAircraftChapter(
			Aircraft aircraft,
			String chapterTitle,
			MWArray report,
			File templateFile
			) throws MWException {
		
		MWArray aircraftChapter = JPADReportUtils.openChapter(chapterTitle,templateFile);
		
		
		// add section "introduction" --> spiegazione velivolo
		createIntroductionSection(aircraft,aircraftChapter,templateFile);
		if(aircraft.getFuselage() != null) {
			createFuselageSection(aircraft, aircraftChapter,templateFile);
		}
		
		// TODO : CONTINUE AS DONE IN FUSEAGE ( REF -> Aircraft.toString() )
		
		JPADReportUtils.closeChapter(report, aircraftChapter);
	}
	
	private static void createAnalysisChapter(
			Aircraft aircraft,
			String chapterTitle,
			MWArray report,
			File templateFile
			) throws MWException {
		
		MWArray analysisChapter = JPADReportUtils.openChapter(chapterTitle,templateFile);
		// TODO: COMPLETE THIS AS IN createAircraftChapter BUT WITH ANALYSES.
		
		JPADReportUtils.closeChapter(report, analysisChapter);
	}
	
	private static void createFuselageSection(Aircraft aircraft, 
			MWArray chapter,
			File templateFile) throws MWException {
		
		JPADReportUtils.addSection(chapter, "Fuselage",templateFile);
		// aggiungi testo, tabelle, immagini ecc... Proponi tu
		
		StringBuilder sb1 = new StringBuilder();
		sb1.append("\n")
		.append("In this section are presented all the geometrical parameters of the fuselage of ")
		.append("the " + aircraft.getId() +" .\n");
		
		
		if(aircraft.getFuselage().getPressurized()==true) {
			sb1.append("\t The "+aircraft.getId()+ " has a pressurized fuselage. \n");
		}
		else sb1.append("\t The "+aircraft.getId()+ " has a no pressurized fuselage. \n");
		

	
		JPADReportUtils.addParagraph(chapter, sb1.toString());
		
		//create a table with the Fuselage parameters
	
	    List<ArrayList<String>> fuselageTable= new ArrayList<ArrayList<String>>();
	    List<String> fuselageHeader= new ArrayList<String>();
    	ArrayList<String> numberOfDecks = new ArrayList<String>();
 	    ArrayList<String> roughness = new ArrayList<String>();
 	    ArrayList<String> length = new ArrayList<String>();
 	    ArrayList<String> noseLength = new ArrayList<String>();
 	    ArrayList<String> cabinLength = new ArrayList<String>();
 	    ArrayList<String> tailLength = new ArrayList<String>();
 	    ArrayList<String> noseLengthRatio = new ArrayList<String>();
 	    ArrayList<String> cabinLengthRatio = new ArrayList<String>();
 	    ArrayList<String> tailLengthRatio = new ArrayList<String>();
 	    ArrayList<String> cabinWidth = new ArrayList<String>();
 	    ArrayList<String> cabinHeight = new ArrayList<String>();
 	    ArrayList<String> noseFR = new ArrayList<String>();
 	    ArrayList<String> cabinFR = new ArrayList<String>();
 	    ArrayList<String> tailFR = new ArrayList<String>();
 	    ArrayList<String> fuselageFR = new ArrayList<String>();
 	    ArrayList<String> heightFromGround = new ArrayList<String>();
 	  
 	    
 	   numberOfDecks.add("Number of decks ");
 	  numberOfDecks.add(Integer.toString(aircraft.getFuselage().getDeckNumber()));
 	  roughness.add("Roughness ");
 	  roughness.add(aircraft.getFuselage().getRoughness().toString());
 	  length.add("Length ");
 	  length.add(aircraft.getFuselage().getFuselageLength().toString());
 	  noseLength.add("Nose length");
 	  noseLength.add(aircraft.getFuselage().getNoseLength().toString());
 	  cabinLength.add("Cabin length");
 	  cabinLength.add(aircraft.getFuselage().getCylinderLength().toString());
 	  tailLength.add("Tail length" );
 	  tailLength.add(aircraft.getFuselage().getTailLength().toString());
 	  noseLengthRatio.add("Nose length ratio ");
 	  noseLengthRatio.add(aircraft.getFuselage().getNoseLengthRatio().toString());
 	  cabinLengthRatio.add("Cabin length ratio ");
 	  cabinLengthRatio.add(aircraft.getFuselage().getCylinderLengthRatio().toString());
 	  tailLengthRatio.add("Tail length ratio ");
 	  tailLengthRatio.add(aircraft.getFuselage().getTailLengthRatio().toString());
 	  cabinWidth.add("Cabin width");
 	  cabinWidth.add(aircraft.getFuselage().getSectionCylinderWidth().toString());
 	  cabinHeight.add("Cabin height");
 	  cabinHeight.add(aircraft.getFuselage().getSectionCylinderHeight().toString());
 	  noseFR.add("Nose fineness ratio");
 	  noseFR.add(aircraft.getFuselage().getNoseFinenessRatio().toString());
 	  cabinFR.add("Cabin fineness ratio");
 	  cabinFR.add(aircraft.getFuselage().getCylinderFinenessRatio().toString());
 	  tailFR.add("Tail fineness ratio");
 	  tailFR.add(aircraft.getFuselage().getTailFinenessRatio().toString());
 	  fuselageFR.add("Fuselage fineness ratio");
 	  fuselageFR.add(aircraft.getFuselage().getFuselageFinenessRatio().toString());
 	  heightFromGround.add("Height from ground");
 	  heightFromGround.add(aircraft.getFuselage().getHeightFromGround().toString());

 	  
 	  // set the Table 
 	  fuselageTable.add(numberOfDecks);
 	  fuselageTable.add(roughness);
 	  fuselageTable.add(length);
 	  fuselageTable.add(noseLength);
 	  fuselageTable.add(cabinLength);
 	  fuselageTable.add(tailLength);
 	  fuselageTable.add(noseLengthRatio);
 	  fuselageTable.add(cabinLengthRatio);
 	  fuselageTable.add(tailLengthRatio);
 	  fuselageTable.add(cabinWidth);
 	  fuselageTable.add(cabinHeight);
 	  fuselageTable.add(noseFR);
 	  fuselageTable.add(cabinFR);
 	  fuselageTable.add(tailFR);
 	  fuselageTable.add(fuselageFR);
 	  fuselageTable.add(heightFromGround);
 	

 	  //set the header 
 	  fuselageHeader.add("Parameter");
 	  fuselageHeader.add("Value");
 	  
 	  JPADReportUtils.addTable(chapter, fuselageTable, fuselageHeader, "Fuselage Specifications");
 	  
 	  
	}
	private static void createIntroductionSection(Aircraft aircraft,
			MWArray chapter,
			File templateFile) throws MWException {
		JPADReportUtils.addSection(chapter, "Introduction", templateFile);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\t The "+aircraft.getId()+" is a "+aircraft.getPowerPlant().getEngineNumber())
		.append(" engine "+aircraft.getTypeVehicle()+", able to seat ")
		.append(+aircraft.getCabinConfiguration().getDesignPassengerNumber()+" passengers. \n");
		
		
		JPADReportUtils.addParagraph(chapter, sb.toString());
	}
	
}
