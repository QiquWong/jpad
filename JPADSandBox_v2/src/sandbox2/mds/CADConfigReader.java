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
		
		File configCADXml = new File("src/sandbox2/mds/config_CAD_v2.xml");
		
		if (configCADXml.exists())
			System.out.println("CAD configuration xml file absolute path: " + configCADXml.getAbsolutePath());
		else
			return;
		
		// Reading the XML file
		JPADXmlReader reader = new JPADXmlReader(configCADXml.getAbsolutePath());
		
		// Detect the parts that need to be rendered
		Boolean generateFuselage;
		Boolean generateWing;
		Boolean generateHTail;
		Boolean generateVTail;
		Boolean generateCanard;
		Boolean generateWingFairing;
		Boolean generateCanardFairing;
		
		String generateFuselageString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//fuselage/@generate");
		
		String generateWingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//wing/@generate");
		
		String generateHTailString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//horizontal/@generate");
		
		String generateVTailString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//vertical/@generate");
		
		String generateCanardString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//canard/@generate");
		
		String generateWingFairingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//wing_fairing/@generate");
		
		String generateCanardFairingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//canard_fairing/@generate");
		
		generateFuselage = (generateFuselageString.equalsIgnoreCase("TRUE")) ? true : false;
		generateWing = (generateWingString.equalsIgnoreCase("TRUE")) ? true : false;
		generateHTail = (generateHTailString.equalsIgnoreCase("TRUE")) ? true : false;
		generateVTail = (generateVTailString.equalsIgnoreCase("TRUE")) ? true : false;
		generateCanard = (generateCanardString.equalsIgnoreCase("TRUE")) ? true : false;
		generateWingFairing = (generateWingFairingString.equalsIgnoreCase("TRUE")) ? true : false;
		generateCanardFairing = (generateCanardFairingString.equalsIgnoreCase("TRUE")) ? true : false;
		
		// FUSELAGE	
		
		// Initialize FUSELAGE CAD parameters
		int numberNoseTrunkSections = 7;
		XSpacingType spacingTypeNoseTrunk = XSpacingType.COSINUS;
		int numberTailTrunkSections = 7;
		XSpacingType spacingTypeTailTrunk = XSpacingType.COSINUS;
		
		// Read FUSELAGE CAD parameters from the XML file
		if (generateFuselage && (theAircraft.getFuselage() != null)) { 
			
			String numberNoseTrunkSectionsString = reader.getXMLPropertyByPath("//fuselage/numberNoseTrunkSections");
			if (numberNoseTrunkSectionsString != null) 
				numberNoseTrunkSections = Integer.valueOf(numberNoseTrunkSectionsString);
			
			String spacingTypeNoseTrunkString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//fuselage/numberNoseTrunkSections/@spacing");
			if (spacingTypeNoseTrunkString != null) 
				spacingTypeNoseTrunk = XSpacingType.valueOf(spacingTypeNoseTrunkString);
			
			String numberTailTrunkSectionsString = reader.getXMLPropertyByPath("//fuselage/numberTailTrunkSections");
			if (numberTailTrunkSectionsString != null) 
				numberTailTrunkSections = Integer.valueOf(numberTailTrunkSectionsString);
			
			String spacingTypeTailTrunkString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//fuselage/numberTailTrunkSections/@spacing");
			if (spacingTypeTailTrunkString != null) 
				spacingTypeTailTrunk = XSpacingType.valueOf(spacingTypeTailTrunkString);
			
		} else {
			
			generateFuselage = false;			
		}
		
		// WING
		
		// Initialize WING CAD parameters
		WingTipType wingTipType = WingTipType.CUTOFF;
		
		double wingletYOffsetFactor = 0.50;
		double wingletXOffsetFactor = 0.75;
		double wingletTaperRatio = 0.20;
		
		// Read WING CAD parameters from the XML file
		if (generateWing && (theAircraft.getWing() != null)) {
			
			String wingTipTypeString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//wing/@tipType");
			if (wingTipTypeString != null)
				wingTipType = WingTipType.valueOf(wingTipTypeString);
			
			if (wingTipType.equals(WingTipType.WINGLET)) {
				
				String wingletYOffsetFactorString = reader.getXMLPropertyByPath("//wing/wingletParams/yOffsetFactor");
				if (wingletYOffsetFactorString != null) 
					wingletYOffsetFactor = Double.valueOf(wingletYOffsetFactorString);
					
				String wingletXOffsetFactorString = reader.getXMLPropertyByPath("//wing/wingletParams/xOffsetFactor");
				if (wingletXOffsetFactorString != null) 
					wingletXOffsetFactor = Double.valueOf(wingletXOffsetFactorString);
				
				String wingletTaperRatioString = reader.getXMLPropertyByPath("//wing/wingletParams/taperRatio");
				if (wingletTaperRatioString != null) 
					wingletTaperRatio = Double.valueOf(wingletTaperRatioString);
								
			}		
			
		} else {
			
			generateWing = false;
		}
		
		// HORIZONTAL

		// Initialize HORIZONTAL TAIL CAD parameters
		WingTipType hTailTipType = WingTipType.CUTOFF;

		// Read HORIZONTAL TAIL CAD parameters from the XML file
		if (generateHTail && (theAircraft.getHTail() != null)) {
			
			String hTailTipTypeString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//horizontal/@tipType");
			if (hTailTipTypeString != null)
				hTailTipType = WingTipType.valueOf(hTailTipTypeString);

		} else {
			
			generateHTail = false;
		}
		
		// VERTICAL

		// Initialize VERTICAL TAIL CAD parameters
		WingTipType vTailTipType = WingTipType.CUTOFF;

		// Read VERTICAL TAIL CAD parameters from the XML file
		if (generateVTail && (theAircraft.getVTail() != null)) { 

			String vTailTipTypeString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//vertical/@tipType");
			if (vTailTipTypeString != null)
				vTailTipType = WingTipType.valueOf(vTailTipTypeString);
			
		} else {
			
			generateVTail = false;
		}
		
		// CANARD

		// Initialize CANARD CAD parameters
		WingTipType canardTipType = WingTipType.CUTOFF;
		
		// Read vertical tail CAD parameters from the XML file
		if (generateCanard && (theAircraft.getCanard() != null)) { 

			String canardTipTypeString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//canard/@tipType");
			if (canardTipTypeString != null)
				canardTipType = WingTipType.valueOf(canardTipTypeString);
			
		} else {
			
			generateCanard = false;
		}
		
		// WING-FUSELAGE FAIRING

		// Initialize WING FAIRING CAD parameters
		double wingFairingFrontLengthFactor = 1.00;
		double wingFairingBackLengthFactor = 1.00;
		double wingFairingWidthFactor = 0.75;
		double wingFairingHeightFactor = 0.15;
		double wingFairingHeightBelowReferenceFactor = 0.60;
		double wingFairingHeightAboveReferenceFactor = 0.45;
		double wingFairingFilletRadiusFactor = 0.80;

		// Read WING FAIRING CAD parameters from the XML file
		if (generateWingFairing && (theAircraft.getWing() != null) && (theAircraft.getFuselage() != null)) { 

			String wingFairingFrontLengthFactorString = reader.getXMLPropertyByPath("//wing_fairing/frontLengthFactor");
			if (wingFairingFrontLengthFactorString != null)
				wingFairingFrontLengthFactor = Double.valueOf(wingFairingFrontLengthFactorString);
			
			String wingFairingBackLengthFactorString = reader.getXMLPropertyByPath("//wing_fairing/backLengthFactor");
			if (wingFairingBackLengthFactorString != null)
				wingFairingBackLengthFactor = Double.valueOf(wingFairingBackLengthFactorString);
			
			String wingFairingWidthFactorString = reader.getXMLPropertyByPath("//wing_fairing/widthFactor");
			if (wingFairingWidthFactorString != null)
				wingFairingWidthFactor = Double.valueOf(wingFairingWidthFactorString);
			
			String wingFairingHeightFactorString = reader.getXMLPropertyByPath("//wing_fairing/heightFactor");
			if (wingFairingHeightFactorString != null)
				wingFairingHeightFactor = Double.valueOf(wingFairingHeightFactorString);
			
			String wingFairingHeightBelowReferenceFactorString = reader.getXMLPropertyByPath("//wing_fairing/heightBelowReferenceFactor");
			if (wingFairingHeightBelowReferenceFactorString != null)
				wingFairingHeightBelowReferenceFactor = Double.valueOf(wingFairingHeightBelowReferenceFactorString);
			
			String wingFairingHeightAboveReferenceFactorString = reader.getXMLPropertyByPath("//wing_fairing/heightAboveReferenceFactor");
			if (wingFairingHeightAboveReferenceFactorString != null)
				wingFairingHeightAboveReferenceFactor = Double.valueOf(wingFairingHeightAboveReferenceFactorString);
			
			String wingFairingFilletRadiusFactorString = reader.getXMLPropertyByPath("//wing_fairing/filletRadiusFactor");
			if (wingFairingFilletRadiusFactorString != null)
				wingFairingFilletRadiusFactor = Double.valueOf(wingFairingFilletRadiusFactorString);
			
		} else {
			
			generateWingFairing = false;
		}
		
		// CANARD-FUSELAGE FAIRING

		// Initialize CANARD FAIRING CAD parameters
		double canardFairingFrontLengthFactor = 1.00;
		double canardFairingBackLengthFactor = 1.00;
		double canardFairingWidthFactor = 0.55;
		double canardFairingHeightFactor = 0.15;
		double canardFairingHeightBelowReferenceFactor = 0.70;
		double canardFairingHeightAboveReferenceFactor = 0.45;
		double canardFairingFilletRadiusFactor = 0.80;

		// Read CANARD FAIRING CAD parameters from the XML file
		if (generateCanardFairing && (theAircraft.getCanard() != null) && (theAircraft.getFuselage() != null)) { 

			String canardFairingFrontLengthFactorString = reader.getXMLPropertyByPath("//canard_fairing/frontLengthFactor");
			if (canardFairingFrontLengthFactorString != null)
				canardFairingFrontLengthFactor = Double.valueOf(canardFairingFrontLengthFactorString);
			
			String canardFairingBackLengthFactorString = reader.getXMLPropertyByPath("//canard_fairing/backLengthFactor");
			if (canardFairingBackLengthFactorString != null)
				canardFairingBackLengthFactor = Double.valueOf(canardFairingBackLengthFactorString);
			
			String canardFairingWidthFactorString = reader.getXMLPropertyByPath("//canard_fairing/widthFactor");
			if (canardFairingWidthFactorString != null)
				canardFairingWidthFactor = Double.valueOf(canardFairingWidthFactorString);
			
			String canardFairingHeightFactorString = reader.getXMLPropertyByPath("//canard_fairing/heightFactor");
			if (canardFairingHeightFactorString != null)
				canardFairingHeightFactor = Double.valueOf(canardFairingHeightFactorString);
			
			String canardFairingHeightBelowReferenceFactorString = reader.getXMLPropertyByPath("//canard_fairing/heightBelowReferenceFactor");
			if (canardFairingHeightBelowReferenceFactorString != null)
				canardFairingHeightBelowReferenceFactor = Double.valueOf(canardFairingHeightBelowReferenceFactorString);
			
			String canardFairingHeightAboveReferenceFactorString = reader.getXMLPropertyByPath("//canard_fairing/heightAboveReferenceFactor");
			if (canardFairingHeightAboveReferenceFactorString != null)
				canardFairingHeightAboveReferenceFactor = Double.valueOf(canardFairingHeightAboveReferenceFactorString);
			
			String canardFairingFilletRadiusFactorString = reader.getXMLPropertyByPath("//canard_fairing/filletRadiusFactor");
			if (canardFairingFilletRadiusFactorString != null)
				canardFairingFilletRadiusFactor = Double.valueOf(canardFairingFilletRadiusFactorString);
			
		} else {
			
			generateCanardFairing = false;
		}
		
		// EXPORT TO FILE OPTIONS
		Boolean exportToFile;
		
		FileExtension fileExtension = FileExtension.STEP;
		Boolean exportWireframe = false;

		String exportToFileString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//export_to_file/@value");
		exportToFile = exportToFileString.equalsIgnoreCase("TRUE") ? true : false;

		if (exportToFile) {
			
			String fileExtensionString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//export_to_file/@format");
			fileExtension = FileExtension.valueOf(fileExtensionString);
			
			String exportWireframeString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//export_to_file/@exportWireframe");
			exportWireframe = exportWireframeString.equalsIgnoreCase("TRUE") ? true : false;
			
		}		
		
		// ***********************************************************************
		// Check what has been read from the XML file, and print it on the console
		// _______________________________________________________________________
		StringBuilder stringBuilder = new StringBuilder()
				.append("\n\n\t--------------------------------------\n")
				.append("\tCAD generator input configuration\n")
				.append("\t--------------------------------------\n\n");
		
		stringBuilder.append("\t[Generate Fuselage CAD]: " + generateFuselage + ".\n");
		if (!generateFuselage) 		
			stringBuilder.append("\n");	
		else			
			stringBuilder.append("\tNose trunk spacing: " + spacingTypeNoseTrunk.toString() + ".\n")
						 .append("\tNumber of nose trunk sections: " + numberNoseTrunkSections + ".\n")
						 .append("\tTail trunk spacing: " + spacingTypeTailTrunk.toString() + ".\n")
						 .append("\tNumber of tail trunk sections: " + numberTailTrunkSections + ".\n")					
						 .append("\n");					 
		
		stringBuilder.append("\t[Generate Wing CAD]: " + generateWing + ".\n");	
		if (!generateWing)
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tTip type: " + wingTipType.toString() + "\n");
		if (wingTipType.equals(WingTipType.WINGLET))
			stringBuilder.append("\tWinglet Y offset factor: " + wingletYOffsetFactor + ".\n")
						 .append("\tWinglet X offset factor: " + wingletXOffsetFactor + ".\n")
						 .append("\tWinglet taper ratio: " + wingletTaperRatio + ".\n")
						 .append("\n");
		else 
			stringBuilder.append("\n");
		
		stringBuilder.append("\t[Generate Horizontal Tail CAD]: " + generateHTail + ".\n");	
		if (!generateHTail)
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tTip type: " + hTailTipType.toString() + ".\n")
						 .append("\n");
		
		stringBuilder.append("\t[Generate Vertical Tail CAD]: " + generateVTail + ".\n");	
		if (!generateVTail)
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tTip type: " + vTailTipType.toString() + ".\n")
						 .append("\n");
		
		stringBuilder.append("\t[Generate Canard CAD]: " + generateCanard + ".\n");	
		if (!generateCanard)
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tTip type: " + canardTipType.toString() + ".\n")
						 .append("\n");
		
		stringBuilder.append("\t[Generate Wing-Fuselage Fairing CAD]: " + generateWingFairing + ".\n");
		if (!generateWingFairing) 
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tFront length factor: " + wingFairingFrontLengthFactor + ".\n")
						 .append("\tBack length factor: " + wingFairingBackLengthFactor + ".\n")
						 .append("\tWidth factor: " + wingFairingWidthFactor + ".\n")
						 .append("\tHeight factor: " + wingFairingHeightFactor + ".\n")
						 .append("\tHeight below reference factor: " + wingFairingHeightBelowReferenceFactor + ".\n")
						 .append("\tHeight above reference factor: " + wingFairingHeightAboveReferenceFactor + ".\n")
						 .append("\tFillet radius factor: " + wingFairingFilletRadiusFactor + ".\n")
						 .append("\n");
		
		stringBuilder.append("\t[Generate Canard/Fuselage Fairing CAD]: " + generateCanardFairing + ".\n");
		if (!generateCanardFairing) 
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tFront length factor: " + canardFairingFrontLengthFactor + ".\n")
						 .append("\tRear length factor: " + canardFairingBackLengthFactor + ".\n")
						 .append("\tWidth factor: " + canardFairingWidthFactor + ".\n")
						 .append("\tHeight factor: " + canardFairingHeightFactor + ".\n")
						 .append("\tHeight below reference factor: " + canardFairingHeightBelowReferenceFactor + ".\n")
						 .append("\tHeight above reference factor: " + canardFairingHeightAboveReferenceFactor + ".\n")
						 .append("\tFillet radius factor: " + canardFairingFilletRadiusFactor + ".\n")
						 .append("\n");
				
		stringBuilder.append("\t[Export shapes to file]: " + exportToFile + ".\n");
		if (!exportToFile)
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tFile format: " + fileExtension.toString() + ".\n")
						 .append("\tExport wireframe: " + exportWireframe + ".\n")
			  			 .append("\n");
		
		System.out.println(stringBuilder.toString());
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
	
	public enum WingTipType {
		CUTOFF,
		ROUNDED,
		WINGLET;
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
