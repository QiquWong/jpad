package sandbox2.vc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.Aircraft;
import aircraft.components.Aircraft.AircraftBuilder;
import analyses.ACAnalysisManager;
import analyses.OperatingConditions;
import analyses.costs.ACCostsManager;
import calculators.costs.CostsCalcUtils;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.JPADXmlReader;


class MyArgumentsCostsTest {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;
	
	@Option(name = "-ia", aliases = { "--input-analyses" }, required = true,
			usage = "analyses input file")
	private File _inputFileAnalyses;

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
	
	@Option(name = "-dc", aliases = { "--dir-costs" }, required = true,
			usage = "costs directory path")
	private File _costsDirectory;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}
	
	public File getInputFileAnalyses() {
		return _inputFileAnalyses;
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
	
	public File getCostsDirectory() {
		return _costsDirectory;
	}


}

public class Test_sandbox2VC_Costs {
	
		// declaration necessary for Concrete Object usage
		public static CmdLineParser theCmdLineParser;
		public static JPADXmlReader reader;

		//-------------------------------------------------------------
		
		public static Aircraft theAircraft; 
	

		public static void main(String[] args) throws CmdLineException {
			
			MyArgumentsCostsTest va = new MyArgumentsCostsTest();
			Test_sandbox2VC_Costs.theCmdLineParser = new CmdLineParser(va);
			
			try{
			theCmdLineParser.parseArgument(args);
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToXML);
			
			String pathToAnalysesXML = va.getInputFileAnalyses().getAbsolutePath();
			System.out.println("ANALYSES INPUT ===> " + pathToAnalysesXML);

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
			
			String dirCosts = va.getCostsDirectory().getCanonicalPath();
			System.out.println("COSTS ===> " + dirCosts);
			
			System.out.println("--------------");
			
			
			// Setup database(s)
			MyConfiguration.initWorkingDirectoryTree();

			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
			AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
			HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
			
			
//			// Initialize Aircraft with default parameters
//			AircraftBuilder theAircraftBuilder = new Aircraft.AircraftBuilder("JPAD Test Aircraft DAF - 2016 - AircraftBuilder call", 
//														 			AircraftEnum.ATR72, 
//														 			aeroDatabaseReader,
//														 			highLiftDatabaseReader); 
//			theAircraft = new Aircraft(theAircraftBuilder);
			
			theAircraft = Aircraft.importFromXML(
					pathToXML,
					dirLiftingSurfaces,
					dirFuselages,
					dirEngines,
					dirNacelles,
					dirLandingGears,
					dirSystems,
					dirCabinConfiguration,
					dirAirfoil,
					aeroDatabaseReader,
					highLiftDatabaseReader);
			
			
			theAircraft.setTheAnalysisManager(ACAnalysisManager.importFromXML(pathToAnalysesXML, theAircraft));
					
			OperatingConditions operatingConditions = new OperatingConditions.OperatingConditionsBuilder("The Operating Conditions").build();
			operatingConditions.setAltitude(Amount.valueOf(11000, SI.METER));
			operatingConditions.setTAS(Amount.valueOf(473, NonSI.KNOT));
//			Amount<Mass> OEM = Amount.valueOf(141056, SI.KILOGRAM);
//			Amount<Mass> MTOM = Amount.valueOf(536164.22, NonSI.POUND);
//			theAircraft.getTheAnalysisManager().getTheWeights().setOperatingEmptyMass(OEM);
//			theAircraft.getTheAnalysisManager().getTheWeights().setMaximumTakeOffMass(MTOM);
//			theAircraft.getTheAnalysisManager().getTheWeights().setManufacturerEmptyMass(OEM);
			
			// Variables setting
			Amount<Mass> _OEM = Amount.valueOf(141056, SI.KILOGRAM);
			Amount<Mass> _MTOM = Amount.valueOf(536164.22, NonSI.POUND);
			Amount<Length> _range = Amount.valueOf(500, NonSI.NAUTICAL_MILE);
			Amount<Velocity> _cruiseSpeed = Amount.valueOf(267.0, SI.METERS_PER_SECOND);
			
//			theAircraft.getTheAnalysisManager().getTheWeights().setOperatingEmptyMass(_OEM);
//			theAircraft.getTheAnalysisManager().getTheWeights().setMaximumTakeOffMass(_MTOM);
//			theAircraft.getTheAnalysisManager().getTheWeights().setManufacturerEmptyMass(_OEM);
			theAircraft.setLifeSpan(16);
//			theAircraft.getTheAnalysisManager().getTheCosts().setAnnualInterestRate(0.054);
//			theAircraft.getTheAnalysisManager().setReferenceRange(_range);
//			theAircraft.getTheAnalysisManager().getThePerformance().setVDesignCruise(_cruiseSpeed);
			
			
			
			CostsCalcUtils.calcAircraftCostSforza(_OEM);
//			theCost.calcAircraftCostSforza();
			
			Amount<Duration> flightTime = Amount.valueOf(15.22, NonSI.HOUR);
			
//			Amount<Velocity> blockSpeed = Amount.valueOf(243.0, SI.METERS_PER_SECOND); // Value according to Sforza
			
//			theAircraft.getTheAnalysisManager().getTheCosts().setFlightTime(flightTime);
			
//			theAircraft.getTheCosts().set_manHourLaborRate(40); // Value according to Sforza
//			theAircraft.getTheCosts().set_blockSpeed(blockSpeed);// Value according to Sforza
//			theAircraft.getTheCosts().calcUtilizationKundu(theCost.get_blockTime().doubleValue(NonSI.HOUR));
			
//			theAircraft.getTheAnalysisManager().getTheCosts().setUtilization(4750);
			
//			theAircraft.getTheCosts().calcTotalInvestments(98400000.0, 9800000.0, 2, 0.1, 0.3);
//			theAircraft.getTheCosts().get_theFixedCharges().set_residualValue(0.2);
			theAircraft.getPowerPlant().setEngineType(EngineTypeEnum.TURBOFAN);
//			Amount<Duration> tb = theCost.calcBlockTime();
//			theCost.set_blockTime(Amount.valueOf(15.94, NonSI.HOUR));;
			
			theAircraft.getTheAnalysisManager().getTheCosts().calculateAll(theAircraft);
			
			Map<MethodEnum, Double> depreciationMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> interestMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> insuranceMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> crewCostsMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> totalFixedChargesMap = 
					new TreeMap<MethodEnum, Double>();

			Map<MethodEnum, Double> landingFeesMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> navigationalChargesMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> groundHandlingChargesMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> maintenanceMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> fuelAndOilMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> totalTripChargesMap = 
					new TreeMap<MethodEnum, Double>();
			
			
			// Start costs estimation
			depreciationMap = theAircraft.getTheAnalysisManager().getTheCosts().getTheFixedCharges().get_calcDepreciation().get_methodsMap();
			interestMap = theAircraft.getTheAnalysisManager().getTheCosts().getTheFixedCharges().get_calcInterest().get_methodsMap();
			insuranceMap = theAircraft.getTheAnalysisManager().getTheCosts().getTheFixedCharges().get_calcInsurance().get_methodsMap();
			crewCostsMap = theAircraft.getTheAnalysisManager().getTheCosts().getTheFixedCharges().get_calcCrewCosts().get_methodsMap();
			//------------------------------------------------------------------------------------------------------------------------
			totalFixedChargesMap = theAircraft.getTheAnalysisManager().getTheCosts().getTheFixedCharges().get_totalFixedChargesMap();
			//------------------------------------------------------------------------------------------------------------------------
			landingFeesMap = theAircraft.getTheAnalysisManager().getTheCosts().getTheTripCharges().get_calcLandingFees().get_methodsMap();
			navigationalChargesMap = theAircraft.getTheAnalysisManager().getTheCosts().getTheTripCharges().get_calcNavigationalCharges().get_methodsMap();
			groundHandlingChargesMap = theAircraft.getTheAnalysisManager().getTheCosts().getTheTripCharges().get_calcGroundHandlingCharges().get_methodsMap();
			maintenanceMap = theAircraft.getTheAnalysisManager().getTheCosts().getTheTripCharges().get_calcMaintenanceCosts().get_methodsMap();
			fuelAndOilMap = theAircraft.getTheAnalysisManager().getTheCosts().getTheTripCharges().get_calcFuelAndOilCharges().get_methodsMap();
			//------------------------------------------------------------------------------------------------------------------------
			totalTripChargesMap = theAircraft.getTheAnalysisManager().getTheCosts().getTheTripCharges().get_totalTripChargesMap();
			//------------------------------------------------------------------------------------------------------------------------
			// DOC = Fixed + Trip Charge
			Map<MethodEnum, Double> DOC = new HashMap<>(totalFixedChargesMap);
			totalTripChargesMap.forEach((k,v) -> DOC.merge(k,v, Double::sum));
			//------------------------------------------------------------------------------------------------------------------------
			
			System.out.println("The aircraft total investment is " +  theAircraft.getTheAnalysisManager().getTheCosts().getTotalInvestments());
//			System.out.println("The aircraft depreciation per block hour is " + depreciation  );
//			System.out.println("The residual value rate is " + theFixedCharges.get_residualValue() );
			System.out.println("The test depreciation methodMap is " + depreciationMap );
			System.out.println("The test interest methodMap is " + interestMap );
			System.out.println("The test insurance methodMap is " + insuranceMap );
			System.out.println("The test crew cost methodMap is " + crewCostsMap );
			System.out.println("The test total fixed charges methodMap is " + totalFixedChargesMap );
			System.out.println();
			
			System.out.println("The test landing fees methodMap is " + landingFeesMap );
			System.out.println("The test navigational charges methodMap is " + navigationalChargesMap );
			System.out.println("The test ground handling charges methodMap is " + groundHandlingChargesMap );
			System.out.println("The test maintenance methodMap is " + maintenanceMap );
			System.out.println("The test fuel and oil methodMap is " + fuelAndOilMap );
			System.out.println("The test total trip charges methodMap is " + totalTripChargesMap );		
			
//			aircraft.getCost().calculateAll();
			System.out.println("\n The test total Fixed Charge methodMap is " + totalFixedChargesMap);
			System.out.println("\n The test total Trip Charges methodMap is " + totalTripChargesMap);
			System.out.println("\n The test DOC methodMap is " + DOC);
			
			}
			catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
			Test_sandbox2VC_Costs.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
			}
		}

	}
