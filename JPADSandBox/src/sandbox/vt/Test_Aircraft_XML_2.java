package sandbox.vt;

import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import standaloneutils.JPADGlobalData;
import standaloneutils.JPADXmlReader;
import writers.JPADDataWriter;
import writers.JPADWriteUtils;

public class Test_Aircraft_XML_2 {
	
	//---------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	
	// to modify in order to read more aircrafts from an array.
	private final String aircraftName = 
	//							 "ATR72";
								 "F100";
	//							 "A320";
	//							 "B747_100B";
	
	private String importFileName = "REPORT_" + aircraftName;
	private String exportFileName = "REPORT_output_" + aircraftName;
	private String importFilePathNoExt = MyConfiguration.getDir(FoldersEnum.INPUT_DIR) + importFileName;
	private String exportFilePathNoExt = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) + exportFileName;
 
	//----------------------------------------------------------------------------------------
	// MAIN:
	public static void main(String[] args) {
		
		//------------------------------------------------------------------------------------
		// Assign all default folders
		MyConfiguration.initWorkingDirectoryTree();

		Test_Aircraft_XML_2 test = new Test_Aircraft_XML_2();
		
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
		System.out.println("------------- AIRCRAFT " + test.getAircraftName() + " -------------");

		Aircraft aircraft = Aircraft.createDefaultAircraft();
		
		// Associate database(s) to the aircraft object
//		aircraft.get_theAerodynamics().set_aerodynamicDatabaseReader(aeroDatabaseReader);
		
		JPADGlobalData.setTheCurrentAircraftInMemory(aircraft);
		JPADGlobalData.get_theAircraftList().add(aircraft);
		
		//------------------------------------------------------------------------------------
		// Must do the analysis
		ACAnalysisManager theAnalysis = new ACAnalysisManager(operatingConditions);
		theAnalysis.updateGeometry(aircraft);
		
//		theAnalysis.doAnalysis(aircraft, 
//				AnalysisTypeEnum.AERODYNAMIC, 
//				AnalysisTypeEnum.BALANCE,
//				AnalysisTypeEnum.WEIGHTS,
//				AnalysisTypeEnum.PERFORMANCES, 
//				AnalysisTypeEnum.COSTS
//				);

		System.out.println("\n\n\t\tCurrent Mach before importing = " + operatingConditions.get_machCurrent());
		System.out.println("\t\tCurrent Altitude before importing = " + operatingConditions.get_altitude());
		System.out.println("\n\n");
		System.out.println("\t\tCurrent MTOM before importing = " + aircraft.get_weights().get_MTOM());
		System.out.println("\n\n");
		
		
		//-------------------------------------------------------------------
		// Export/Serialize
		
//		JPADDataWriter writer = new JPADDataWriter(
//				JPADGlobalData.getTheCurrentOperatingConditions(),
//				JPADGlobalData.getTheCurrentAircraft(),
//				theAnalysis);
		
		String myImportedFilePathNoExt = 
				MyConfiguration.getDir(FoldersEnum.INPUT_DIR) + "";
		
		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// STATIC FUNCTIONS - TO BE CALLED BEFORE EVERYTHING ELSE
//		JPADWriteUtils.buildXmlTree();
//		writer.exportToXMLfile(myImportedFilePathNoExt + ".xml");
		
		//-------------------------------------------------------------------
		// Re-import serialized file 
		test.importUsingCustomXML(
				JPADGlobalData.getTheCurrentOperatingConditions(), // operatingConditions, 
				JPADGlobalData.getTheCurrentAircraft(), // aircraft, 
				theAnalysis, // null, // theAnalysis,	
				myImportedFilePathNoExt); // +"a" +"aa"
		
		//-------------------------------------------------------------------
		// post re-import checks
		// ...
		
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
	public void importUsingCustomXML(OperatingConditions operatingConditions, Aircraft aircraft, ACAnalysisManager analysis, String inFilePathNoExt) {

		
		System.out.println("\n ----- STARTED IMPORTING DATA FROM THE XML CUSTOM FILE -----\n");

		
		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// STATIC FUNCTIONS - TO BE CALLED BEFORE EVERYTHING ELSE
		JPADWriteUtils.buildXmlTree();

		JPADXmlReader _theReadUtilities = new JPADXmlReader(aircraft, operatingConditions, inFilePathNoExt);
		_theReadUtilities.importAircraftAndOperatingConditions(aircraft, operatingConditions, inFilePathNoExt);
		
		System.out.println("\n\n");
		System.out.println("\t\tCurrent Mach after importing = " + operatingConditions.get_machCurrent());
		System.out.println("\t\tCurrent Altitude after importing = " + operatingConditions.get_altitude());
		System.out.println("\n\n");
		System.out.println("\t\tCurrent MTOM after importing = " + aircraft.get_weights().get_MTOM());
		System.out.println("\n\n");
		
		//-------------------------------------------------------------------
		// Export/Serialize
		
		JPADDataWriter writer = new JPADDataWriter(operatingConditions, aircraft, analysis);
		
		String myExportedFile =  inFilePathNoExt + "b" + ".xml";
		
		writer.exportToXMLfile(myExportedFile);
		
	}
	
	//---------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public String getExportFileName() {
		return exportFileName;
	}

	public void setExportFileName(String exportFileName) {
		this.exportFileName = exportFileName;
	}

	public String getImportFileName() {
		return importFileName;
	}

	public void setImportFileName(String importFileName) {
		this.importFileName = importFileName;
	}

	public String getImportFile() {
		return importFilePathNoExt;
	}

	public void setImportFile(String importFile) {
		this.importFilePathNoExt = importFile;
	}

	public String getExportFile() {
		return exportFilePathNoExt;
	}

	public void setExportFile(String exportFile) {
		this.exportFilePathNoExt = exportFile;
	}

	public String getAircraftName() {
		return aircraftName;
	}
}