package sandbox.vt.PayloadRange_Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import aircraft.calculators.ACAerodynamicsManager;
import calculators.geometry.LSGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.AirplaneType;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.FuelFractionDatabaseReader;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.JPADXmlReader;
import standaloneutils.atmosphere.AtmosphereCalc;

public class PayloadRange_Test_TF_SA_MTOM{
	
	//---------------------------------------------------------------------------------
	// VARIABLE DECLARATION: 
	@Option(name = "-i", aliases = { "--input" }, required = false,
			usage = "my input file")
	private File _inputFile;
	
	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;
	
	@Argument
    private List<String> arguments = new ArrayList<String>();
	
	//---------------------------------------------------------------------------------
	//BUILDER:
	public PayloadRange_Test_TF_SA_MTOM(){
		theCmdLineParser = new CmdLineParser(this);
	}
	
	//---------------------------------------------------------------------------------
	// MAIN:
	public static void main(String[] args) throws HDF5LibraryException, NullPointerException, CmdLineException{
		
		//--------------------------------------------------------------------------------------
		// Arguments check and initial activities
		if (args.length == 0){
			System.err.println("NO INPUT FILE GIVEN --> TERMINATING");
			return;
		}
		
		System.out.println("------------------------------------------------------------");
		System.out.println("PayloadRangeCalc_Test :: TURBOFAN");
		System.out.println("------------------------------------------------------------");
		System.out.println(" ");
		
		PayloadRange_Test_TF_SA main = new PayloadRange_Test_TF_SA();
		System.out.println("Input variable usage:");
		main.theCmdLineParser.printUsage(System.out);
		System.out.println("-----------------------------------------------------------");
		main.theCmdLineParser.parseArgument(args); 
		
//		System.out.println("main.get_inputFile: " + main.get_inputFile());
//		System.out.println("Path " + main.get_inputFile().getAbsolutePath());
		String path = main.get_inputFile().getAbsolutePath();
		
		JPADXmlReader reader = new JPADXmlReader(path);
		System.out.println("-----------------------------------------------------------");
		System.out.println("XML File Path : " + path);
		System.out.println("-----------------------------------------------------------");
		System.out.println("Initialize reading \n");
		
		// B747-100B
		Amount<Angle> sweepLEEquivalent = (Amount<Angle>)reader.getXMLAmountWithUnitByPath("//B747/SweepLE");
		Amount<Mass> maxTakeOffMass = (Amount<Mass>)reader.getXMLAmountWithUnitByPath("//B747/Maximum_take_off_mass"); 
		Amount<Mass> operatingEmptyMass = (Amount<Mass>)reader.getXMLAmountWithUnitByPath("//B747/Operating_empty_mass");
		Amount<Mass> maxFuelMass = (Amount<Mass>)reader.getXMLAmountWithUnitByPath("//B747/Maximum_fuel_mass");
		Amount<Area> surface = (Amount<Area>)reader.getXMLAmountWithUnitByPath("//B747/Planform_surface");
		List<String> cd0_property = reader.getXMLPropertiesByPath("//B747/CD0");
		double cd0 = Double.valueOf(cd0_property.get(0));
		List<String> oswald_property = reader.getXMLPropertiesByPath("//B747/OswaldFactor");
		double oswald = Double.valueOf(oswald_property.get(0));
		List<String> ar_property = reader.getXMLPropertiesByPath("//B747/AspectRatio");
		double ar = Double.valueOf(ar_property.get(0));	
		List<String> cl_property = reader.getXMLPropertiesByPath("//B747/Current_lift_coefficient");
		double cl = Double.valueOf(cl_property.get(0));
		List<String> tcMax_property = reader.getXMLPropertiesByPath("//B747/Mean_maximum_thickness");
		double tcMax = Double.valueOf(tcMax_property.get(0));
		List<String> altitude_property = reader.getXMLPropertiesByPath("//B747/Altitude");	
		double altitude = Double.valueOf(altitude_property.get(0));
		List<String> eta_property = reader.getXMLPropertiesByPath("//B747/Propeller_efficiency");
		double eta = Double.valueOf(eta_property.get(0));
		List<String> currentMach_property = reader.getXMLPropertiesByPath("//B747/Mach_number");
		double currentMach = Double.valueOf(currentMach_property.get(0));
		List<String> byPassRatio_property = reader.getXMLPropertiesByPath("//B747/ByPassRatio");
		double byPassRatio = Double.valueOf(byPassRatio_property.get(0));
		List<String> nPassMax_property = reader.getXMLPropertiesByPath("//B747/Maximum_number_of_passengers");
		double nPassMax = Double.valueOf(nPassMax_property.get(0));
		
		List<String> taperRatioEquivalent_property = reader.getXMLPropertiesByPath("//B747/TaperRatio");
		double taperRatioEquivalent = Double.valueOf(taperRatioEquivalent_property.get(0));
		
		Amount<Angle> sweepHalfChordEquivalent = LSGeometryCalc.calculateSweep(
				ar,
				taperRatioEquivalent,
				sweepLEEquivalent.to(SI.RADIAN).getEstimatedValue(),
				0.5,
				0.0
				);
		
		//--------------------------------------------------------------------------------------
		// the next step is necessary to calculate CL and Speed at point E (or A) of the parabolic polar
		ACAerodynamicsManager analysis = new ACAerodynamicsManager();
		analysis.calculateDragPolarPoints(
				ar,
				oswald,
				cd0,
				AtmosphereCalc.getDensity(altitude),
				maxTakeOffMass.getEstimatedValue()*9.81,
				surface.getEstimatedValue()
				);
		
		//--------------------------------------------------------------------------------------
		// Assign all default folders
		MyConfiguration.initWorkingDirectoryTree();

		//--------------------------------------------------------------------------------------
		// Setup database(s)	
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String fuelFractionDatabaseFileName = "FuelFractions.h5";
		FuelFractionDatabaseReader fuelFractionReader = new FuelFractionDatabaseReader(databaseFolderPath, fuelFractionDatabaseFileName);
		
		//--------------------------------------------------------------------------------------
		// Creating calculator object
		
		PayloadRangeCalc test = new PayloadRangeCalc(
				maxTakeOffMass,
				operatingEmptyMass,
				maxFuelMass,
				sweepHalfChordEquivalent,
				nPassMax,
				cl,
				tcMax,
				AirplaneType.TURBOFAN_TRANSPORT_JETS,
				EngineTypeEnum.TURBOFAN,
				AirfoilTypeEnum.MODERN_SUPERCRITICAL,
				analysis,
				fuelFractionReader
				);
		
		// -----------------------CRITICAL MACH NUMBER CHECK----------------------------
		
		boolean check = test.checkCriticalMach(currentMach);
		
		if (check)
			System.out.println("\n\n-----------------------------------------------------------"
					+ "\nCurrent Mach is lower then critical Mach number."
					+ "\nCurrent Mach = " + currentMach
					+ "\nCritical Mach = " + test.getCriticalMach() 
					+ "\n\n\t CHECK PASSED --> PROCEDING TO CALCULATION "
					+ "\n\n"
					+ "-----------------------------------------------------------");
		else{
			System.err.println("\n\n-----------------------------------------------------------"
					+ "\nCurrent Mach is bigger then critical Mach number."
					+ "\nCurrent Mach = " + currentMach
					+ "\nCritical Mach = " + test.getCriticalMach() 
					+ "\n\n\t CHECK NOT PASSED --> WARNING!!! "
					+ "\n\n"
					+ "-----------------------------------------------------------");
		}
		
		// ------------------------MTOM PARAMETERIZATION---------------------------------
		
		test.createPayloadRangeMatrices(
				sweepHalfChordEquivalent,
				surface,
				cd0,
				oswald,
				cl,
				ar,
				tcMax,
				byPassRatio,
				eta,
				altitude,
				currentMach,
				false
				);
		
		// ------------------------------PLOTTING-----------------------------------------------				
		// MTOM parameterization:
		
		test.createPayloadRangeCharts_MaxTakeOffMass(
				test.getRangeMatrix(),
				test.getPayloadMatrix()
				);

	}

	//-----------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public File get_inputFile(){
		return _inputFile;
	}
	
	//------------------------------------------------------------------------------------------
	// END OF THE TEST
}
