package sandbox2.mds;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;

class ArgumentsForCADConfigReader {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;
	
	@Option(name = "-db", aliases = { "--dir-database" }, required = true,
			usage = "database directory")
	private File _databaseDirectory;
	
	@Option(name = "-da", aliases = { "--dir-airfoils" }, required = true,
			usage = "airfoil directory path")
	private File _airfoilDirectory;

	@Option(name = "-df", aliases = { "--dir-fuselages" }, required = true,
			usage = "fuselages directory path")
	private File _fuselagesDirectory;
	
	@Option(name = "-dls", aliases = { "--dir-lifting-surfaces" }, required = true,
			usage = "lifting surfaces directory path")
	private File _liftingSurfacesDirectory;
	
	@Option(name = "-de", aliases = { "--dir-engines" }, required = true,
			usage = "engines directory path")
	private File _enginesDirectory;
	
	@Option(name = "-dn", aliases = { "--dir-nacelles" }, required = true,
			usage = "nacelles directory path")
	private File _nacellesDirectory;
	
	@Option(name = "-dlg", aliases = { "--dir-landing-gears" }, required = true,
			usage = "landing gears directory path")
	private File _landingGearsDirectory;
	
	@Option(name = "-dcc", aliases = { "--dir-cabin-configurations" }, required = true,
			usage = "cabin configurations directory path")
	private File _cabinConfigurationsDirectory;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}
	
	public File getDatabaseDirectory() {
		return _databaseDirectory;
	}

	public File getAirfoilDirectory() {
		return _airfoilDirectory;
	}

	public File getFuselagesDirectory() {
		return _fuselagesDirectory;
	}
	
	public File getLiftingSurfacesDirectory() {
		return _liftingSurfacesDirectory;
	}

	public File getEnginesDirectory() {
		return _enginesDirectory;
	}
	
	public File getNacellesDirectory() {
		return _nacellesDirectory;
	}
	
	public File getLandingGearsDirectory() {
		return _landingGearsDirectory;
	}

	public File getCabinConfigurationDirectory() {
		return _cabinConfigurationsDirectory;
	}
}

public class CADConfigReader {
	
	public static CmdLineParser theCmdLineParser;
	public static Aircraft theAircraft;

	public static void main(String[] args) throws InvalidFormatException, HDF5LibraryException {
		
		theAircraft = importAircarftFromXML(args);
		
		File configCADXml = new File("src/sandbox2/mds/config_CAD.xml");
		
		if (configCADXml.exists())
			System.out.println("CAD configuration xml file absolute path: " + configCADXml.getAbsolutePath());
		else
			return;
		
		// Reading the xml file
		JPADXmlReader reader = new JPADXmlReader(configCADXml.getAbsolutePath());
		
		// Detect the parts that need to be rendered
		Boolean generateFuselage;
		Boolean generateWing;
		Boolean generateHorizontal;
		Boolean generateVertical;
		Boolean generateCanard;
		Boolean generateWingFairing;
		Boolean generateCanardFairing;
		
		String generateFuselageString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//fuselage/@generate");
		
		String generateWingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//wing/@generate");
		
		String generateHorizontalString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//horizontal/@generate");
		
		String generateVerticalString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//vertical/@generate");
		
		String generateCanardString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//canard/@generate");
		
		String generateWingFairingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//wing_fairing/@generate");
		
		String generateCanardFairingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//canard_fairing/@generate");
		
		generateFuselage = generateFuselageString.equalsIgnoreCase("TRUE") ? true : false;
		generateWing = generateWingString.equalsIgnoreCase("TRUE") ? true : false;
		generateHorizontal = generateHorizontalString.equalsIgnoreCase("TRUE") ? true : false;
		generateVertical = generateVerticalString.equalsIgnoreCase("TRUE") ? true : false;
		generateCanard = generateCanardString.equalsIgnoreCase("TRUE") ? true : false;
		generateWingFairing = generateWingFairingString.equalsIgnoreCase("TRUE") ? true : false;
		generateCanardFairing = generateCanardFairingString.equalsIgnoreCase("TRUE") ? true : false;
		
		// FUSELAGE	
		
		// Initialize fuselage CAD parameters
		double noseCapSectionFactor1 = 0.15;
		double noseCapSectionFactor2 = 1.0;
		int numberNoseCapSections = 3;
		int numberNoseTrunkSections = 7;
		XSpacingType spacingTypeNoseTrunk = XSpacingType.COSINUS;
		int numberTailTrunkSections = 7;
		XSpacingType spacingTypeTailTrunk = XSpacingType.COSINUS;
		double tailCapSectionFactor1 = 1.0;
		double tailCapSectionFactor2 = 0.15;
		int numberTailCapSections = 3;
		boolean exportFuselageSupportShapes = false;
		
		// Read fuselage CAD parameters from the xml file
		if (generateFuselage && !(theAircraft.getFuselage() == null)) { 
			
			String noseCapSectionFactor1String = reader.getXMLPropertyByPath("//fuselage/noseCapSectionFactor1");
			if (!(noseCapSectionFactor1String == null)) 
				noseCapSectionFactor1 = Double.valueOf(noseCapSectionFactor1String);
			
			String noseCapSectionFactor2String = reader.getXMLPropertyByPath("//fuselage/noseCapSectionFactor2");
			if (!(noseCapSectionFactor2String == null)) 
				noseCapSectionFactor2 = Double.valueOf(noseCapSectionFactor2String);
			
			String numberNoseCapSectionsString = reader.getXMLPropertyByPath("//fuselage/numberNoseCapSections");
			if (!(numberNoseCapSectionsString == null)) 
				numberNoseCapSections = Integer.valueOf(numberNoseCapSectionsString);
			
			String numberNoseTrunkSectionsString = reader.getXMLPropertyByPath("//fuselage/numberNoseTrunkSections");
			if (!(numberNoseTrunkSectionsString == null)) 
				numberNoseTrunkSections = Integer.valueOf(numberNoseTrunkSectionsString);
			
			String spacingTypeNoseTrunkString = reader.getXMLPropertyByPath("//fuselage/spacingTypeNoseTrunk");
			if (!(spacingTypeNoseTrunkString == null)) 
				spacingTypeNoseTrunk = XSpacingType.valueOf(spacingTypeNoseTrunkString);
			
			String numberTailTrunkSectionsString = reader.getXMLPropertyByPath("//fuselage/numberTailTrunkSections");
			if (!(numberTailTrunkSectionsString == null)) 
				numberTailTrunkSections = Integer.valueOf(numberTailTrunkSectionsString);
			
			String spacingTypeTailTrunkString = reader.getXMLPropertyByPath("//fuselage/spacingTypeTailTrunk");
			if (!(spacingTypeTailTrunkString == null)) 
				spacingTypeTailTrunk = XSpacingType.valueOf(spacingTypeTailTrunkString);
			
			String tailCapSectionFactor1String = reader.getXMLPropertyByPath("//fuselage/tailCapSectionFactor1");
			if (!(tailCapSectionFactor1String == null)) 
				tailCapSectionFactor1 = Double.valueOf(tailCapSectionFactor1String);
			
			String tailCapSectionFactor2String = reader.getXMLPropertyByPath("//fuselage/tailCapSectionFactor2");
			if (!(tailCapSectionFactor2String == null)) 
				tailCapSectionFactor2 = Double.valueOf(tailCapSectionFactor2String);
			
			String numberTailCapSectionsString = reader.getXMLPropertyByPath("//fuselage/numberTailCapSections");
			if (!(numberTailCapSectionsString == null)) 
				numberTailCapSections = Integer.valueOf(numberTailCapSectionsString);
			
		}
		
		// WING
		
		// Initialize wing CAD parameters
		double wingTipTolerance = 1e-3;
		boolean exportWingSupportShapes = false;
		
		// Read wing CAD parameters from the xml file
		if (generateWing && !(theAircraft.getWing() == null)) {
			
			String wingTipToleranceString = reader.getXMLPropertyByPath("//wing/tipTolerance");
			if (!(wingTipToleranceString == null))
				wingTipTolerance = Double.valueOf(wingTipToleranceString);
			
			String exportWingSupportShapesString = reader.getXMLPropertyByPath("//wing/exportSupportShapes");
			if (!(exportWingSupportShapesString == null)) 
				exportWingSupportShapes = Boolean.parseBoolean(exportWingSupportShapesString);
			
		}
		
		// HORIZONTAL

		// Initialize horizontal tail CAD parameters
		double horizontalTipTolerance = 1e-3;
		boolean exportHorizontalSupportShapes = false;

		// Read horizontal tail CAD parameters from the xml file
		if (generateHorizontal && !(theAircraft.getHTail() == null)) {
			
			String horizontalTipToleranceString = reader.getXMLPropertyByPath("//horizontal/tipTolerance");
			if (!(horizontalTipToleranceString == null))
				horizontalTipTolerance = Double.valueOf(horizontalTipToleranceString);
			
			String exportHorizontalSupportShapesString = reader.getXMLPropertyByPath("//horizontal/exportSupportShapes");
			if (!(exportHorizontalSupportShapesString == null)) 
				exportHorizontalSupportShapes = Boolean.parseBoolean(exportHorizontalSupportShapesString);

		}
		
		// VERTICAL

		// Initialize vertical tail CAD parameters
		double verticalTipTolerance = 1e-3;
		boolean exportVerticalSupportShapes = false;

		// Read vertical tail CAD parameters from the xml file
		if (generateVertical && !(theAircraft.getVTail() ==  null)) { 

			String verticalTipToleranceString = reader.getXMLPropertyByPath("//vertical/tipTolerance");
			if (!(verticalTipToleranceString == null))
				verticalTipTolerance = Double.valueOf(verticalTipToleranceString);
			
			String exportVerticalSupportShapesString = reader.getXMLPropertyByPath("//vertical/exportSupportShapes");
			if (!(exportVerticalSupportShapesString == null)) 
				exportVerticalSupportShapes = Boolean.parseBoolean(exportVerticalSupportShapesString);
			
		}
		
		// CANARD

		// Initialize canard CAD parameters
		double canardTipTolerance = 1e-3;
		boolean exportCanardSupportShapes = false;

		// Read vertical tail CAD parameters from the xml file
		if (generateCanard && !(theAircraft.getCanard() == null)) { 

			String canardTipToleranceString = reader.getXMLPropertyByPath("//canard/tipTolerance");
			if (!(canardTipToleranceString == null))
				canardTipTolerance = Double.valueOf(canardTipToleranceString);
			
			String exportCanardSupportShapesString = reader.getXMLPropertyByPath("//canard/exportSupportShapes");
			if (!(exportCanardSupportShapesString == null)) 
				exportCanardSupportShapes = Boolean.parseBoolean(exportCanardSupportShapesString);
			
		}
		
		// WING-FUSELAGE FAIRING

		// Initialize wing fairing CAD parameters
		double wingFairingFrontLengthFactor = 1.25;
		double wingFairingBackLengthFactor = 1.25;
		double wingFairingSideSizeFactor = 0.25;
		double wingFairingHeightFactor = 0.25;
		double wingFairingHeightBelowContactFactor = 0.70;
		double wingFairingHeightAboveContactFactor = 0.10;
		double wingFairingFilletRadiusFactor = 0.60;

		// Read wing fairing CAD parameters from the xml file
		if (generateWingFairing && !(theAircraft.getWing() == null) && !(theAircraft.getFuselage() == null)) { 

			String wingFairingFrontLengthFactorString = reader.getXMLPropertyByPath("//wing_fairing/frontLengthFactor");
			if (!(wingFairingFrontLengthFactorString == null))
				wingFairingFrontLengthFactor = Double.valueOf(wingFairingFrontLengthFactorString);
			
			String wingFairingBackLengthFactorString = reader.getXMLPropertyByPath("//wing_fairing/backLengthFactor");
			if (!(wingFairingBackLengthFactorString == null))
				wingFairingBackLengthFactor = Double.valueOf(wingFairingBackLengthFactorString);
			
			String wingFairingSideSizeFactorString = reader.getXMLPropertyByPath("//wing_fairing/sideSizeFactor");
			if (!(wingFairingSideSizeFactorString == null))
				wingFairingSideSizeFactor = Double.valueOf(wingFairingSideSizeFactorString);
			
			String wingFairingHeightFactorString = reader.getXMLPropertyByPath("//wing_fairing/fairingHeightFactor");
			if (!(wingFairingHeightFactorString == null))
				wingFairingHeightFactor = Double.valueOf(wingFairingHeightFactorString);
			
			String wingFairingHeightBelowContactFactorString = reader.getXMLPropertyByPath("//wing_fairing/heightBelowContactFactor");
			if (!(wingFairingHeightBelowContactFactorString == null))
				wingFairingHeightBelowContactFactor = Double.valueOf(wingFairingHeightBelowContactFactorString);
			
			String wingFairingHeightAboveContactFactorString = reader.getXMLPropertyByPath("//wing_fairing/heightAboveContactFactor");
			if (!(wingFairingHeightAboveContactFactorString == null))
				wingFairingHeightAboveContactFactor = Double.valueOf(wingFairingHeightAboveContactFactorString);
			
			String wingFairingFilletRadiusFactorString = reader.getXMLPropertyByPath("//wing_fairing/filletRadiusFactor");
			if (!(wingFairingFilletRadiusFactorString == null))
				wingFairingFilletRadiusFactor = Double.valueOf(wingFairingFilletRadiusFactorString);
			
		}
		
		// CANARD-FUSELAGE FAIRING

		// Initialize canard fairing CAD parameters
		double canardFairingFrontLengthFactor = 1.25;
		double canardFairingBackLengthFactor = 1.25;
		double canardFairingSideSizeFactor = 0.25;
		double canardFairingHeightFactor = 0.25;
		double canardFairingHeightBelowContactFactor = 0.70;
		double canardFairingHeightAboveContactFactor = 0.10;
		double canardFairingFilletRadiusFactor = 0.60;

		// Read canard fairing CAD parameters from the xml file
		if (generateCanardFairing && !(theAircraft.getCanard() == null) && !(theAircraft.getFuselage() == null)) { 

			String canardFairingFrontLengthFactorString = reader.getXMLPropertyByPath("//canard_fairing/frontLengthFactor");
			if (!(canardFairingFrontLengthFactorString == null))
				canardFairingFrontLengthFactor = Double.valueOf(canardFairingFrontLengthFactorString);
			
			String canardFairingBackLengthFactorString = reader.getXMLPropertyByPath("//canard_fairing/backLengthFactor");
			if (!(canardFairingBackLengthFactorString == null))
				canardFairingBackLengthFactor = Double.valueOf(canardFairingBackLengthFactorString);
			
			String canardFairingSideSizeFactorString = reader.getXMLPropertyByPath("//canard_fairing/sideSizeFactor");
			if (!(canardFairingSideSizeFactorString == null))
				canardFairingSideSizeFactor = Double.valueOf(canardFairingSideSizeFactorString);
			
			String canardFairingHeightFactorString = reader.getXMLPropertyByPath("//canard_fairing/fairingHeightFactor");
			if (!(canardFairingHeightFactorString == null))
				canardFairingHeightFactor = Double.valueOf(canardFairingHeightFactorString);
			
			String canardFairingHeightBelowContactFactorString = reader.getXMLPropertyByPath("//canard_fairing/heightBelowContactFactor");
			if (!(canardFairingHeightBelowContactFactorString == null))
				canardFairingHeightBelowContactFactor = Double.valueOf(canardFairingHeightBelowContactFactorString);
			
			String canardFairingHeightAboveContactFactorString = reader.getXMLPropertyByPath("//canard_fairing/heightAboveContactFactor");
			if (!(canardFairingHeightAboveContactFactorString == null))
				canardFairingHeightAboveContactFactor = Double.valueOf(canardFairingHeightAboveContactFactorString);
			
			String canardFairingFilletRadiusFactorString = reader.getXMLPropertyByPath("//canard_fairing/filletRadiusFactor");
			if (!(canardFairingFilletRadiusFactorString == null))
				canardFairingFilletRadiusFactor = Double.valueOf(canardFairingFilletRadiusFactorString);
			
		}
		
		// EXPORT TO FILE OPTIONS
		Boolean exportToFile;
		FileExtension fileExtension;
		
		String exportToFileString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//export_to_file/@value");
		exportToFile = exportToFileString.equalsIgnoreCase("TRUE") ? true : false;
		
		String fileExtensionString = reader.getXMLPropertyByPath("//export_to_file/file_format");
		fileExtension = FileExtension.valueOf(fileExtensionString);
		
		// ***********************************************************************
		// Check what has been read from the XML file, and print it on the console
		// _______________________________________________________________________
		System.out.println("=========================================================");
		System.out.println("=== CAD XML configuration file - Import from XML test ===");
		System.out.println("=========================================================");
		System.out.println();
		
		System.out.println("=== FUSELAGE CAD Parameters");
		System.out.println("--- Generate fuselage: " + generateFuselage);
		if (generateFuselage && !(theAircraft.getFuselage() == null)) {
			
			System.out.println("--- Nose cap section factor #1: " + noseCapSectionFactor1);
			System.out.println("--- Nose cap section factor #2: " + noseCapSectionFactor2);
			System.out.println("--- Number of nose cap sections: " + numberNoseCapSections);
			System.out.println("--- Number of nose trunk sections: " + numberNoseTrunkSections);
			System.out.println("--- Nose trunk spacing type: " + spacingTypeNoseTrunk.toString());
			System.out.println("--- Number of tail trunk sections: " + numberTailTrunkSections);
			System.out.println("--- Tail trunk spacing type: " + spacingTypeTailTrunk.toString());
			System.out.println("--- Tail cap section factor #1: " + tailCapSectionFactor1);
			System.out.println("--- Tail cap section factor #2: " + tailCapSectionFactor2);
			System.out.println("--- Number of tail cap sections: " + numberTailCapSections);
			System.out.println("--- Export fuselage support shapes: " + exportFuselageSupportShapes);
			
		}
		System.out.println();
		
		System.out.println("=== WING CAD Parameters");
		System.out.println("--- Generate wing: " + generateWing);
		if (generateWing && !(theAircraft.getWing() == null)) { 
			
			System.out.println("--- Wing tip tolerance: " + wingTipTolerance);
			System.out.println("--- Export wing support shapes: " + exportWingSupportShapes);
			
		}
		System.out.println();
		
		System.out.println("=== HORIZONTAL TAIL CAD Parameters");
		System.out.println("--- Generate horizontal tail: " + generateHorizontal);
		if (generateHorizontal && !(theAircraft.getHTail() == null)) { 
			
			System.out.println("--- Horizontal tail tip tolerance: " + horizontalTipTolerance);
			System.out.println("--- Export horizontal tail support shapes: " + exportHorizontalSupportShapes);
			
		}
		System.out.println();
		
		System.out.println("=== VERTICAL TAIL CAD Parameters");
		System.out.println("--- Generate vertical tail: " + generateVertical);
		if (generateVertical && !(theAircraft.getVTail() == null)) { 
			
			System.out.println("--- Vertical tail tip tolerance: " + verticalTipTolerance);
			System.out.println("--- Export vertical tail support shapes: " + exportVerticalSupportShapes);
			
		}
		System.out.println();
		
		System.out.println("=== CANARD CAD Parameters");
		System.out.println("--- Generate canard: " + generateCanard);
		if (generateCanard && !(theAircraft.getCanard() == null)) { 
			
			System.out.println("--- Canard tip tolerance: " + canardTipTolerance);
			System.out.println("--- Export canard support shapes: " + exportCanardSupportShapes);
			
		}
		System.out.println();
		
		System.out.println("=== WING/FUSELAGE FAIRING CAD Parameters");
		System.out.println("--- Generate wing/fuselage fairing: " + generateWingFairing);
		if (generateWingFairing && !(theAircraft.getWing() == null) && !(theAircraft.getFuselage() == null)) {
			
			System.out.println("--- Wing/Fuselage fairing front length factor: " + wingFairingFrontLengthFactor);
			System.out.println("--- Wing/Fuselage fairing back length factor: " + wingFairingBackLengthFactor);
			System.out.println("--- Wing/Fuselage fairing side size factor: " + wingFairingSideSizeFactor);
			System.out.println("--- Wing/Fuselage fairing height factor: " + wingFairingHeightFactor);
			System.out.println("--- Wing/Fuselage fairing height below contact factor: " + wingFairingHeightBelowContactFactor);
			System.out.println("--- Wing/Fuselage fairing height above contact factor: " + wingFairingHeightAboveContactFactor);
			System.out.println("--- Wing/Fuselage fairing fillet radius factor: " + wingFairingFilletRadiusFactor);
			
		}
		System.out.println();
		
		System.out.println("=== CANARD/FUSELAGE FAIRING CAD Parameters");
		System.out.println("--- Generate canard/fuselage fairing: " + generateCanardFairing);
		if (generateCanardFairing && !(theAircraft.getCanard() == null) && !(theAircraft.getFuselage() == null)) {
			
			System.out.println("--- Canard/Fuselage fairing front length factor: " + canardFairingFrontLengthFactor);
			System.out.println("--- Canard/Fuselage fairing back length factor: " + canardFairingBackLengthFactor);
			System.out.println("--- Canard/Fuselage fairing side size factor: " + canardFairingSideSizeFactor);
			System.out.println("--- Canard/Fuselage fairing height factor: " + canardFairingHeightFactor);
			System.out.println("--- Canard/Fuselage fairing height below contact factor: " + canardFairingHeightBelowContactFactor);
			System.out.println("--- Canard/Fuselage fairing height above contact factor: " + canardFairingHeightAboveContactFactor);
			System.out.println("--- Canard/Fuselage fairing fillet radius factor: " + canardFairingFilletRadiusFactor);
			
		}
		System.out.println();
		
		System.out.println("=== CAD File options");
		System.out.println("--- Export solids to file: " + exportToFile);
		if (exportToFile) 
			System.out.println("--- File format: " + fileExtension.toString());
		
	}
	
	public static Aircraft importAircarftFromXML(String[] args) throws InvalidFormatException, HDF5LibraryException {
		Aircraft aircraft = null;
		
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
		    public void write(int b) {
		         // write nothing
		    }
		});
		
		ArgumentsForCADConfigReader va = new ArgumentsForCADConfigReader();
		CADConfigReader.theCmdLineParser = new CmdLineParser(va);
		
		try {
			CADConfigReader.theCmdLineParser.parseArgument(args);
			
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("AIRCRAFT INPUT ===> " + pathToXML);
			
			String dirAirfoil = va.getAirfoilDirectory().getCanonicalPath();
			System.out.println("AIRFOILS ===> " + dirAirfoil);

			String dirFuselages = va.getFuselagesDirectory().getCanonicalPath();
			System.out.println("FUSELAGES ===> " + dirFuselages);
			
			String dirLiftingSurfaces = va.getLiftingSurfacesDirectory().getCanonicalPath();
			System.out.println("LIFTING SURFACES ===> " + dirLiftingSurfaces);
			
			String dirEngines = va.getEnginesDirectory().getCanonicalPath();
			System.out.println("ENGINES ===> " + dirEngines);
			
			String dirNacelles = va.getNacellesDirectory().getCanonicalPath();
			System.out.println("NACELLES ===> " + dirNacelles);
			
			String dirLandingGears = va.getLandingGearsDirectory().getCanonicalPath();
			System.out.println("LANDING GEARS ===> " + dirLandingGears);
			
			String dirCabinConfiguration = va.getCabinConfigurationDirectory().getCanonicalPath();
			System.out.println("CABIN CONFIGURATIONS ===> " + dirCabinConfiguration);
			
			System.out.println("--------------");
			
			// Setup database(s)
			MyConfiguration.setDir(FoldersEnum.DATABASE_DIR, va.getDatabaseDirectory().getAbsolutePath());

			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
			String fusDesDatabaseFilename = "FusDes_database.h5";
			String vedscDatabaseFilename = "VeDSC_database.h5";

			AerodynamicDatabaseReader aeroDatabaseReader = DatabaseManager.initializeAeroDatabase(
					new AerodynamicDatabaseReader(
							databaseFolderPath,	aerodynamicDatabaseFileName
							),
					databaseFolderPath
					);
			HighLiftDatabaseReader highLiftDatabaseReader = DatabaseManager.initializeHighLiftDatabase(
					new HighLiftDatabaseReader(
							databaseFolderPath,	highLiftDatabaseFileName
							),
					databaseFolderPath
					);
			FusDesDatabaseReader fusDesDatabaseReader = DatabaseManager.initializeFusDes(
					new FusDesDatabaseReader(
							databaseFolderPath,	fusDesDatabaseFilename
							),
					databaseFolderPath
					);
			VeDSCDatabaseReader veDSCDatabaseReader = DatabaseManager.initializeVeDSC(
					new VeDSCDatabaseReader(
							databaseFolderPath,	vedscDatabaseFilename
							),
					databaseFolderPath
					);
			
			// Aircraft creation
			System.out.println("\n\n\tImporting the Aircraft ... \n\n");

			// deactivating system.out
			System.setOut(filterStream);

			// reading aircraft from xml ... 
			aircraft = Aircraft.importFromXML(
					pathToXML,
					dirLiftingSurfaces,
					dirFuselages,
					dirEngines,
					dirNacelles,
					dirLandingGears,
					dirCabinConfiguration,
					dirAirfoil,
					aeroDatabaseReader,
					highLiftDatabaseReader,
					fusDesDatabaseReader,
					veDSCDatabaseReader);
					
		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			CADConfigReader.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("Must launch this app with proper command line arguments.");
			System.exit(1);
		}
			
		System.setOut(originalOut);
		return aircraft;
	}
	
	public enum FileExtension {
		BREP,
		STEP,
		IGES,
		STL;
	}

	public enum XSpacingType {
		UNIFORM {
			@Override
			public Double[] calculateSpacing(double x1, double x2, int n) {
				Double[] xSpacing = MyArrayUtils.linspaceDouble(x1, x2, n);
				return xSpacing;
			}
		},
		COSINUS {
			@Override
			public Double[] calculateSpacing(double x1, double x2, int n) {
				Double[] xSpacing = MyArrayUtils.cosineSpaceDouble(x1, x2, n);
				return xSpacing;
			}
		},
		HALFCOSINUS1 { // finer spacing close to x1
			@Override
			public Double[] calculateSpacing(double x1, double x2, int n) {
				Double[] xSpacing = MyArrayUtils.halfCosine1SpaceDouble(x1, x2, n);
				return xSpacing;
			}
		}, 
		HALFCOSINUS2 { // finer spacing close to x2
			@Override
			public Double[] calculateSpacing(double x1, double x2, int n) {
				Double[] xSpacing = MyArrayUtils.halfCosine2SpaceDouble(x1, x2, n);
				return xSpacing;
			}
		}; 
		
		public abstract Double[] calculateSpacing(double x1, double x2, int n);
	}
}
