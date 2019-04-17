package it.unina.daf.jpad.report;

import java.io.File;

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
			
			createAircraftChapter(aircraft, aircraftChapterTitle, report);
			
			if(aircraft.getTheAnalysisManager() != null) {
			
				createAnalysisChapter(aircraft, analysisChapterTitle, report);
				
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
			MWArray report
			) throws MWException {
		
		MWArray aircraftChapter = JPADReportUtils.openChapter(chapterTitle);
		
		// add section "introduction" --> spiegazione velivolo
		
		if(aircraft.getFuselage() != null) {
			createFuselageSection(aircraft, aircraftChapter);
		}
		
		// TODO : CONTINUE AS DONE IN FUSEAGE ( REF -> Aircraft.toString() )
		
		JPADReportUtils.closeChapter(report, aircraftChapter);
	}
	
	private static void createAnalysisChapter(
			Aircraft aircraft,
			String chapterTitle,
			MWArray report
			) throws MWException {
		
		MWArray analysisChapter = JPADReportUtils.openChapter(chapterTitle);
		
		// TODO: COMPLETE THIS AS IN createAircraftChapter BUT WITH ANALYSES.
		
		JPADReportUtils.closeChapter(report, analysisChapter);
	}
	
	private static void createFuselageSection(Aircraft aircraft, MWArray chapter) throws MWException {
		
		JPADReportUtils.addSection(chapter, "Fuselage");
		
		// aggiungi testo, tabelle, immagini ecc... Proponi tu
		
		
	}
	
}
