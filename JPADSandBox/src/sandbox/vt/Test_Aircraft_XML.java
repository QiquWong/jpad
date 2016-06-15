package sandbox.vt;

import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import standaloneutils.JPADGlobalData;
import writers.JPADDataWriter;
import writers.JPADWriteUtils;

public class Test_Aircraft_XML {
	
	//---------------------------------------------------------------------------------------
	// VARIABLE DECLARATION: 
	// to modify in order to read more aircrafts from an array.
//	final String aircraftName = 
//	//							 "ATR72";
//								 "F100";
//	//							 "A320";
//	//							 "B747_100B";
//	
	//----------------------------------------------------------------------------------------
	// MAIN:
	public static void main(String[] args) {
		
		//------------------------------------------------------------------------------------
		
		// Assign all default folders
		MyConfiguration.initWorkingDirectoryTree();

		Test_Aircraft_XML test = new Test_Aircraft_XML();
		
//	    String importFileName = "REPORT_" + test.getAircraftName() ;
//		String importFilePathNoExt = MyConfiguration.getDir(FoldersEnum.INPUT_DIR) + importFileName;
//		String exportFileName = "REPORT_output_" + test.getAircraftName();
		//------------------------------------------------------------------------------------
		// Setup database(s)	
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		
		//------------------------------------------------------------------------------------
		// flight conditions
		OperatingConditions operatingConditions = new OperatingConditions();
		JPADGlobalData.set_theCurrentOperatingConditions(operatingConditions);
		
		//------------------------------------------------------------------------------------
		// Initialize Aircraft with default parameters
		Aircraft aircraft = Aircraft.createDefaultAircraft();
		
		// Associate database(s) to the aircraft object
		aircraft.get_theAerodynamics().set_aerodynamicDatabaseReader(aeroDatabaseReader);
		
		JPADGlobalData.setTheCurrentAircraftInMemory(aircraft);
		JPADGlobalData.get_theAircraftList().add(aircraft);
		
		//------------------------------------------------------------------------------------
		// Must do the analysis
		ACAnalysisManager theAnalysis = new ACAnalysisManager(operatingConditions);
		theAnalysis.updateGeometry(aircraft);
		
		theAnalysis.doAnalysis(aircraft, 
				AnalysisTypeEnum.AERODYNAMIC, 
				AnalysisTypeEnum.BALANCE,
				AnalysisTypeEnum.WEIGHTS,
				AnalysisTypeEnum.PERFORMANCE, 
				AnalysisTypeEnum.COSTS
				);

		System.out.println("\n\n\t\tCurrent Mach before importing = " + operatingConditions.get_machCurrent());
		System.out.println("\t\tCurrent Altitude before importing = " + operatingConditions.get_altitude());
		System.out.println("\n\n");
		System.out.println("\t\tCurrent MTOM before importing = " + aircraft.get_weights().get_MTOM());
		System.out.println("\n\n ");
		
		//-------------------------------------------------------------------
		// Export/Serialize
		
		JPADDataWriter writer = new JPADDataWriter(
				JPADGlobalData.getTheCurrentOperatingConditions(),
				JPADGlobalData.getTheCurrentAircraft(),
				theAnalysis);
		
		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// STATIC FUNCTIONS - TO BE CALLED BEFORE EVERYTHING ELSE
		JPADWriteUtils.buildXmlTree();
		
		String defaultAircraftExportPathNoExt = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) + "Default_Aircraft";
		writer.exportToXMLfile(defaultAircraftExportPathNoExt + ".xml");
		
		//-------------------------------------------------------------------
		// Re-import serialized file 
		String exportFilePathNoExt = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) + "Default_Aircraft";
		test.importUsingCustomXML(
				operatingConditions,
				aircraft,
				null,
				defaultAircraftExportPathNoExt,
				exportFilePathNoExt +"_new"); //  +"aa"
		
		//-------------------------------------------------------------------
		// post re-import checks
		// ...
		System.out.println("\n\n");
		System.out.println("\t\tCurrent Mach after importing = " + operatingConditions.get_machCurrent());
		System.out.println("\t\tCurrent Altitude after importing = " + operatingConditions.get_altitude());
		System.out.println("\n\n");
		System.out.println("\t\tCurrent MTOM after importing = " + aircraft.get_weights().get_MTOM());
		System.out.println("\n\n");
		
	}
	//---------------------------------------------------------------------------------------
	// END OF MAIN
	
	//---------------------------------------------------------------------------------------
	// METHODS:
	/****************************************************************************************
	 * This method import an aircraft and it's operating conditions from an .xml file 
	 * overwriting imported data on the default aircraft ones.
	 * 
	 * @param acName aircraft name
	 */
	public void importUsingCustomXML(
			OperatingConditions operatingConditions,
			Aircraft aircraft,
			ACAnalysisManager analysis,
			String inFilePathNoExt,
			String expFilePathNoExt) {

		
		System.out.println("\n ----- STARTED IMPORTING DATA FROM THE XML CUSTOM FILE -----\n");

		// commented because this method only has to write an imported aircraft via .xml
//		JPADXmlReader _theReadUtilities = new JPADXmlReader(aircraft, operatingConditions, inFilePathNoExt);
//		_theReadUtilities.importAircraftAndOperatingConditions(
//				aircraft,
//				operatingConditions,
//				inFilePathNoExt);
		
		//-------------------------------------------------------------------
		// Export/Serialize
		
		JPADDataWriter writer = new JPADDataWriter(operatingConditions, aircraft, analysis);
		writer.exportToXMLfile(expFilePathNoExt + ".xml");
	}
	//-----------------------------------------END------------------------------------------

//	public String getAircraftName() {
//		return aircraftName;
//	}
}