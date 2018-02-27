package sandbox2.adm;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.SI;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.Aircraft;
import analyses.ACAnalysisManager;
import analyses.OperatingConditions;
import calculators.performance.AircraftPointMassPropagator;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import writers.JPADStaticWriteUtils;

class AircraftPointMassPropagatorArguments {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	@Option(name = "-ia", aliases = { "--input-analyses" }, required = true,
			usage = "analyses input file")
	private File _inputFileAnalyses;
	
	@Option(name = "-ioc", aliases = { "--input-operating-condition" }, required = true,
			usage = "operating conditions input file")
	private File _inputFileOperatingCondition;

	@Option(name = "-ime", aliases = { "--input-mission-events" }, required = true,
			usage = "mission events input file for mission simulation")
	private File _inputFileMissionEvents;
	
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
	
	@Option(name = "-ds", aliases = { "--dir-systems" }, required = true,
			usage = "systems directory path")
	private File _systemsDirectory;
	
	@Option(name = "-dcc", aliases = { "--dir-cabin-configurations" }, required = true,
			usage = "cabin configurations directory path")
	private File _cabinConfigurationsDirectory;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}

	public File getInputFileAnalyses() {
		return _inputFileAnalyses;
	}
	
	public File getInputFileOperatingConditions() {
		return _inputFileOperatingCondition;
	}
	
	public File getInputFileMissionEvents() {
		return _inputFileMissionEvents;
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

	public File getSystemsDirectory() {
		return _systemsDirectory;
	}
	
	public File getCabinConfigurationDirectory() {
		return _cabinConfigurationsDirectory;
	}
}

public class AircraftPointMassPropagatorTest {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static Aircraft theAircraft;

	//-------------------------------------------------------------
	
	public static void main(String[] args) {
		
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
		    public void write(int b) {
		         // write nothing
		    }
		});
		
		long startTime = System.currentTimeMillis();        
		
		System.out.println("-------------------");
		System.out.println("Aincraft Point-Mass Propagation Test");
		System.out.println("-------------------");
		
		AircraftPointMassPropagatorArguments  va = new AircraftPointMassPropagatorArguments();
		AircraftPointMassPropagatorTest.theCmdLineParser = new CmdLineParser(va);

		try {
			AircraftPointMassPropagatorTest.theCmdLineParser.parseArgument(args);
			
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("AIRCRAFT INPUT ===> " + pathToXML);

			String pathToAnalysesXML = va.getInputFileAnalyses().getAbsolutePath();
			System.out.println("ANALYSES INPUT ===> " + pathToAnalysesXML);
			
			String pathToOperatingConditionsXML = va.getInputFileOperatingConditions().getAbsolutePath();
			System.out.println("OPERATING CONDITIONS INPUT ===> " + pathToOperatingConditionsXML);

			String pathToMissionEventsXML = va.getInputFileMissionEvents().getAbsolutePath();
			System.out.println("MISSION EVENTS INPUT ===> " + pathToMissionEventsXML);
			
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
			
			String dirSystems = va.getSystemsDirectory().getCanonicalPath();
			System.out.println("SYSTEMS ===> " + dirSystems);
			
			String dirCabinConfiguration = va.getCabinConfigurationDirectory().getCanonicalPath();
			System.out.println("CABIN CONFIGURATIONS ===> " + dirCabinConfiguration);
			
			System.out.println("--------------");

			//------------------------------------------------------------------------------------
			// Setup database(s)
			MyConfiguration.initWorkingDirectoryTree();
			
			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
			String fusDesDatabaseFilename = "FusDes_database.h5";
			String vedscDatabaseFilename = "VeDSC_database.h5";
			AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
			HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
			FusDesDatabaseReader fusDesDatabaseReader = new FusDesDatabaseReader(databaseFolderPath, fusDesDatabaseFilename);
			VeDSCDatabaseReader veDSCDatabaseReader = new VeDSCDatabaseReader(databaseFolderPath, vedscDatabaseFilename);

			////////////////////////////////////////////////////////////////////////
			// Aircraft creation
			System.out.println("\n\n\tCreating the Aircraft ... \n\n");
			
			// deactivating system.out
			System.setOut(filterStream);
			
			// default Aircraft ATR-72 ...
//			theAircraft = new Aircraft.AircraftBuilder(
//					"ATR-72",
//					AircraftEnum.ATR72,
//					aeroDatabaseReader,
//					highLiftDatabaseReader
//					)
//					.build();

			// reading aircraft from xml ... 
			theAircraft = Aircraft.importFromXML(
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
			
			// activating system.out
			System.setOut(originalOut);			
			System.out.println(theAircraft.toString());
			System.setOut(filterStream);
			
			////////////////////////////////////////////////////////////////////////
			// Set the folders tree
			MyConfiguration.initWorkingDirectoryTree(
					MyConfiguration.currentDirectoryString,
					MyConfiguration.inputDirectory, 
					MyConfiguration.outputDirectory);
			String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
			String aircraftFolder = JPADStaticWriteUtils.createNewFolder(folderPath + theAircraft.getId() + File.separator);
			String subfolderPath = JPADStaticWriteUtils.createNewFolder(aircraftFolder);

			////////////////////////////////////////////////////////////////////////
			// Defining the operating conditions ...
			System.setOut(originalOut);
			System.out.println("\n\n\tDefining the operating conditions ... \n\n");
			System.setOut(filterStream);
			OperatingConditions theOperatingConditions = OperatingConditions.importFromXML(pathToOperatingConditionsXML);
			System.setOut(originalOut);
			System.out.println(theOperatingConditions.toString());
			System.setOut(filterStream);
			
			////////////////////////////////////////////////////////////////////////
			// Analyzing the aircraft
//			System.setOut(originalOut);
//			System.out.println("\n\n\tRunning requested analyses ... \n\n");
//			System.setOut(filterStream);
//			theAircraft.setTheAnalysisManager(ACAnalysisManager.importFromXML(pathToAnalysesXML, theAircraft));
//			theAircraft.getTheAnalysisManager().doAnalysis(theAircraft, theOperatingConditions, subfolderPath);
//			System.setOut(originalOut);
//			System.out.println("\n\n\tDone!! \n\n");
//			System.setOut(filterStream);
			
			////////////////////////////////////////////////////////////////////////
			// Printing results (activating system.out)
//			System.setOut(originalOut);
//			System.out.println("\n\n\tPrinting results ... \n\n");
//			System.out.println(theAircraft.getTheAnalysisManager().toString());
//			System.out.println("\n\n\tDone!! \n\n");
			
			//======================================================================
			// Propagator test
			
			System.setOut(originalOut);
			AircraftPointMassPropagator propagator = new AircraftPointMassPropagator(theAircraft);
			
			// read initial values of some state variables
			// read the list of events from file
			propagator.readMissionScript(pathToMissionEventsXML);
			
			// initial mass
			double mass0 = 53000.0; // kg
			
			// initial air density (at initial altitude)
			double rho0 = AtmosphereCalc.getDensity(propagator.getAltitude0()); // kg/m^3
			
			// initial lift
			double lift0 = mass0*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND)
					*Math.cos(propagator.getFlightpathAngle0())
					/Math.cos(propagator.getBankAngle0());
			// initial lift coefficient
			double cL0 = lift0
					/(0.5*rho0
							*Math.pow(propagator.getSpeedInertial0(), 2)
							*theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE)
							);
			// initial drag
			double cD0 = 0.025;
			double aspectRatio = theAircraft.getWing().getAspectRatio();
			double oswaldFactor = 0.85;
			double kD = Math.PI * aspectRatio * oswaldFactor;
			double airDensity = AtmosphereCalc.getDensity(propagator.getAltitude0());
			double surfaceWing = theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE);
			double kD0 = 0.5 * airDensity * surfaceWing * cD0;
			double kD1 = 2.0/(airDensity * surfaceWing * kD);
			double drag0 = kD0 * Math.pow(propagator.getSpeedInertial0(), 2) 
					+ kD1 * Math.pow(lift0, 2)/Math.pow(propagator.getSpeedInertial0(), 2);
			
			double thrust0 = drag0  // 200000.0; // N
					+ mass0*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND)*Math.sin(propagator.getFlightpathAngle0());
			
			double xT0 = thrust0/propagator.getkTi().doubleValue(MyUnits.ONE_PER_SECOND_SQUARED);
			double xL0 = lift0/propagator.getkLi().doubleValue(MyUnits.ONE_PER_SECOND_SQUARED);
			
			
			// complete the initial settings
			propagator.setXThrust0(xT0);
			propagator.setThrust0(thrust0);
			propagator.setXLift0(xL0);
			propagator.setLift0(lift0);
			propagator.setMass0(mass0);

			System.out.println("========================================");
			System.out.println("m(0)   = " + mass0);
			System.out.println("rho(0) = " + rho0);			
			System.out.println("xL(0)  = " + xL0);
			System.out.println("L(0)   = " + lift0);
			System.out.println("CL(0)  = " + cL0);
			System.out.println("D(0)   = " + lift0);
			System.out.println("xT(0)  = " + xT0);
			System.out.println("T(0)   = " + thrust0);
			System.out.println("========================================");
			
//			propagator.setInitialConditions(
//					v0, gamma0, psi0,
//					0.0, 0.0, h0, // XI, YI, h
//					0.0, thrust0, // xT, T
//					0.0, lift0, // xL, L
//					phi0, mass0);
			
			// Final time
			propagator.setTimeFinal(180.0); // sec
			
			propagator.enableCharts(true);
			
			// propagate in time
			propagator.propagate();
			
			// Plot
			Path path = Paths.get(pathToMissionEventsXML);
			String missionID = path.getFileName().toString().replace("analysis_mission_simulation_", "").replace(".xml", "");
			String missionOutputDir = JPADStaticWriteUtils.createNewFolder(
					aircraftFolder + "MISSION_SIM" + File.separator + missionID + File.separator);
			if (propagator.chartsEnabled())
				System.out.println("\nPlots saved in folder: " + missionOutputDir);
			
			propagator.setOutputChartDir(missionOutputDir);
			propagator.createOutputCharts(0.5);

			propagator.getTheIntegrator().clearEventHandlers();
			propagator.getTheIntegrator().clearStepHandlers();

			
			long estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println("\n\n\t TIME ESTIMATED = " + (estimatedTime/1000) + " seconds");
			
			System.setOut(filterStream);
			
		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			AircraftPointMassPropagatorTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
		} catch (InstantiationException  | IllegalAccessException e) {
			System.err.println("Error: " + e.getMessage());
			System.err.println("  A problem occurred with the output chart function.");
			e.printStackTrace();
		}	    

	}

}
