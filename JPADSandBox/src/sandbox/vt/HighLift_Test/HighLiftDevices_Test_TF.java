package sandbox.vt.HighLift_Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.JPADXmlReader;
import standaloneutils.customdata.CenterOfGravity;

public class HighLiftDevices_Test_TF {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	@Option(name = "-i", aliases = { "--input" }, required = false,
			usage = "my input file")
	private File _inputFile;

	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	//------------------------------------------------------------------------------------------
	//BUILDER:
	public HighLiftDevices_Test_TF() {
		theCmdLineParser = new CmdLineParser(this);
	}

	public static void main(String[] args) throws CmdLineException {

		System.out.println("-----------------------------------------------------------");
		System.out.println("HighLiftDevices_Test :: B747-100B");
		System.out.println("-----------------------------------------------------------\n");

		HighLiftDevices_Test_TF main = new HighLiftDevices_Test_TF();

		//----------------------------------------------------------------------------------
		// Default folders creation:
		MyConfiguration.initWorkingDirectoryTree();

		//------------------------------------------------------------------------------------
		// Setup database(s)
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		String highLiftDatabaseFileName = "HighLiftDatabase.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);

		//------------------------------------------------------------------------------------
		// Operating Condition / Aircraft / AnalysisManager (geometry calculations)
		OperatingConditions theCondition = new OperatingConditions();
		theCondition.set_altitude(Amount.valueOf(11000.0, SI.METER));
		theCondition.set_machCurrent(0.84);
		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.B747_100B);

		aircraft.get_theAerodynamics().set_aerodynamicDatabaseReader(aeroDatabaseReader);
		aircraft.get_theAerodynamics().set_highLiftDatabaseReader(highLiftDatabaseReader);
		aircraft.set_name("B747-100B");
		aircraft.get_wing().set_theCurrentAirfoil(
				new MyAirfoil(
						aircraft.get_wing(),
						0.5
						)
				);
		aircraft.get_wing().get_theCurrentAirfoil().set_type(AirfoilTypeEnum.MODERN_SUPERCRITICAL);

		//--------------------------------------------------------------------------------------
		// Aerodynamic analysis
		ACAnalysisManager theAnalysis = new ACAnalysisManager(theCondition);
		theAnalysis.updateGeometry(aircraft);

		// Set the CoG(Bypass the Balance analysis allowing to perform Aerodynamic analysis only)
		CenterOfGravity cgMTOM = new CenterOfGravity();

		// x_cg in body-ref.-frame
		cgMTOM.set_xBRF(Amount.valueOf(23.1, SI.METER));
		cgMTOM.set_yBRF(Amount.valueOf(0.0, SI.METER));
		cgMTOM.set_zBRF(Amount.valueOf(0.0, SI.METER));

		aircraft.get_theBalance().set_cgMTOM(cgMTOM);
		aircraft.get_HTail().calculateArms(aircraft);
		aircraft.get_VTail().calculateArms(aircraft);

		theAnalysis.doAnalysis(aircraft,
				AnalysisTypeEnum.AERODYNAMIC);

		aircraft.get_wing().getAerodynamics().setHighLiftDatabaseReader(highLiftDatabaseReader);

		//--------------------------------------------------------------------------------------
//		// Define airfoil
//		System.out.println("\n\n-----------------------------------------------------");
//		System.out.println("AIRFOIL");
//		System.out.println("-----------------------------------------------------");
//
//		//AIRFOIL 1
//		double yLocRoot = 0.0;
//		MyAirfoil airfoilRoot = new MyAirfoil(aircraft.get_wing(), yLocRoot, "65-209");
//		airfoilRoot.getGeometry().update(yLocRoot);  // define chord
//		airfoilRoot.getGeometry().set_maximumThicknessOverChord(0.18); //REPORT
//		airfoilRoot.getGeometry().set_deltaYPercent(0.192 *airfoilRoot.getGeometry().get_maximumThicknessOverChord()*100 );
//		System.out.println("\n \n \t ROOT \nAirfoil Type: " + airfoilRoot.get_family());
//		System.out.println("Root Chord " + aircraft.get_wing().get_chordRoot().getEstimatedValue() );
//		System.out.println("Root maximum thickness " + airfoilRoot.getGeometry().get_maximumThicknessOverChord());
//		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
//		System.out.println("LE sharpness parameter Root " + airfoilRoot.getGeometry().get_deltaYPercent());
//
//		airfoilRoot.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-1.2), SI.RADIAN));
//		airfoilRoot.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(10.0),SI.RADIAN));
//		airfoilRoot.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(12.0),SI.RADIAN));
//		airfoilRoot.getAerodynamics().set_clAlpha(6.07);
//		airfoilRoot.getAerodynamics().set_clMax(1.3);
//		airfoilRoot.getAerodynamics().set_clStar(1.06);
//
//		//AIRFOIL 2
//		double yLocKink = aircraft.get_wing().get_spanStationKink() * aircraft.get_wing().get_semispan().getEstimatedValue();
//		MyAirfoil airfoilKink = new MyAirfoil(aircraft.get_wing(), yLocKink, "65-209");
//		airfoilKink.getGeometry().update(yLocKink);   // define chord
//		airfoilKink.getGeometry().set_maximumThicknessOverChord(0.18); //REPORT
//		airfoilKink.getGeometry().set_deltaYPercent(0.192 *airfoilKink.getGeometry().get_maximumThicknessOverChord()*100 );
//		System.out.println("\n \n \t KINK \nAirfoil Type: " + airfoilKink.get_family());
//		System.out.println("Kink Station " + yLocKink);
//		System.out.println("Kink Chord " + aircraft.get_wing().get_chordKink().getEstimatedValue() );
//		System.out.println("Kink maximum thickness " + airfoilKink.getGeometry().get_maximumThicknessOverChord());
//		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
//		System.out.println("LE sharpness parameter Kink " + airfoilKink.getGeometry().get_deltaYPercent());
//
//		airfoilKink.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-1.2), SI.RADIAN));
//		airfoilKink.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(10.0),SI.RADIAN));
//		airfoilKink.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(12.0),SI.RADIAN));
//		airfoilKink.getAerodynamics().set_clAlpha(6.07);
//		airfoilKink.getAerodynamics().set_clMax(1.3);
//		airfoilKink.getAerodynamics().set_clStar(1.06);
//
//		//AIRFOIL 3
//		double yLocTip = aircraft.get_wing().get_semispan().getEstimatedValue();
//		MyAirfoil airfoilTip = new MyAirfoil(aircraft.get_wing(), yLocTip, "65-206");
//		airfoilTip.getGeometry().update(yLocRoot);  // define chord
//		airfoilTip.getGeometry().set_maximumThicknessOverChord(0.1350); //REPORT
//		airfoilTip.getGeometry().set_deltaYPercent(0.192 *airfoilTip.getGeometry().get_maximumThicknessOverChord()*100 );
//		System.out.println("\n \n \t TIP \nAirfoil Type: " + airfoilKink.get_family());
//		System.out.println("tip Chord " +aircraft.get_wing().get_chordTip().getEstimatedValue() );
//		System.out.println("Tip maximum thickness " + airfoilTip.getGeometry().get_maximumThicknessOverChord());
//		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
//		System.out.println("LE sharpness parameter Tip " + airfoilTip.getGeometry().get_deltaYPercent());
//
//		airfoilTip.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-1.6), SI.RADIAN));
//		airfoilTip.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(6.0),SI.RADIAN));
//		airfoilTip.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(12.0),SI.RADIAN));
//		airfoilTip.getAerodynamics().set_clAlpha(6.01);
//		airfoilTip.getAerodynamics().set_clMax(1.0);
//		airfoilTip.getAerodynamics().set_clStar(0.63);
//
//		//--------------------------------------------------------------------------------------
//		// Assign airfoil
//		List<MyAirfoil> myAirfoilList = new ArrayList<MyAirfoil>();
//		myAirfoilList.add(0, airfoilRoot);
//		myAirfoilList.add(1, airfoilKink);
//		myAirfoilList.add(2, airfoilTip);
//		aircraft.get_wing().set_theAirfoilsList(myAirfoilList);
//		aircraft.get_wing().updateAirfoilsGeometry();

		//----------------------------------------------------------------------------------
		// High Lift Devices Test
		List<Double[]> deltaFlap = new ArrayList<Double[]>();
		List<FlapTypeEnum> flapType = new ArrayList<FlapTypeEnum>();
		List<Double> eta_in_flap = new ArrayList<Double>();
		List<Double> eta_out_flap = new ArrayList<Double>();
		List<Double> cf_c = new ArrayList<Double>();
		List<Double> deltaSlat = new ArrayList<Double>();
		List<Double> eta_in_slat = new ArrayList<Double>();
		List<Double> eta_out_slat = new ArrayList<Double>();
		List<Double> cs_c = new ArrayList<Double>();
		List<Double> cExt_c_slat = new ArrayList<Double>();
		List<Double> leRadius_c_slat = new ArrayList<Double>();

		// XML reading phase:
		// Arguments check
		if (args.length == 0){
			System.err.println("NO INPUT FILE GIVEN --> TERMINATING");
			return;
		}
		main.theCmdLineParser.parseArgument(args);
		String path = main.get_inputFile().getAbsolutePath();
		JPADXmlReader reader = new JPADXmlReader(path);

		System.out.println("-----------------------------------------------------------");
		System.out.println("XML File Path : " + path);
		System.out.println("-----------------------------------------------------------");
		System.out.println("Initialize reading \n");

		List<String> flapNumber_property = reader.getXMLPropertiesByPath("//Flap_Number");
		int flapNumber = Integer.valueOf(flapNumber_property.get(0));
		List<String> flapType_property = reader.getXMLPropertiesByPath("//FlapType");
		List<String> cf_c_property = reader.getXMLPropertiesByPath("//Cf_C");
		List<String> delta_flap1_property = reader.getXMLPropertiesByPath("//Delta_Flap1");
		List<String> delta_flap2_property = reader.getXMLPropertiesByPath("//Delta_Flap2");
		List<String> eta_in_flap_property = reader.getXMLPropertiesByPath("//Flap_inboard");
		List<String> eta_out_flap_property = reader.getXMLPropertiesByPath("//Flap_outboard");
		List<String> delta_slat_property = reader.getXMLPropertiesByPath("//Delta_Slat");
		List<String> cs_c_property = reader.getXMLPropertiesByPath("//Cs_C");
		List<String> cExt_c_slat_property = reader.getXMLPropertiesByPath("//cExt_c");
		List<String> leRadius_c_slat_property = reader.getXMLPropertiesByPath("//LEradius_c_ratio");
		List<String> eta_in_slat_property = reader.getXMLPropertiesByPath("//Slat_inboard");
		List<String> eta_out_slat_property = reader.getXMLPropertiesByPath("//Slat_outboard");


		for(int i=0; i<flapType_property.size(); i++) {
			if(flapType_property.get(i).equals("SINGLE_SLOTTED"))
				flapType.add(FlapTypeEnum.SINGLE_SLOTTED);
			else if(flapType_property.get(i).equals("DOUBLE_SLOTTED"))
				flapType.add(FlapTypeEnum.DOUBLE_SLOTTED);
			else if(flapType_property.get(i).equals("PLAIN"))
				flapType.add(FlapTypeEnum.PLAIN);
			else if(flapType_property.get(i).equals("FOWLER"))
				flapType.add(FlapTypeEnum.FOWLER);
			else if(flapType_property.get(i).equals("TRIPLE_SLOTTED"))
				flapType.add(FlapTypeEnum.TRIPLE_SLOTTED);
			else {
				System.err.println("NO VALID FLAP TYPE!!");
				return;
			}
		}

		Double[] deltaFlap1_array = new Double[delta_flap1_property.size()];
		for(int i=0; i<deltaFlap1_array.length; i++)
			deltaFlap1_array[i] = Double.valueOf(delta_flap1_property.get(i));

		Double[] deltaFlap2_array = new Double[delta_flap2_property.size()];
		for(int i=0; i<deltaFlap1_array.length; i++)
			deltaFlap2_array[i] = Double.valueOf(delta_flap2_property.get(i));

		deltaFlap.add(deltaFlap1_array);
		deltaFlap.add(deltaFlap2_array);

		for(int i=0; i<cf_c_property.size(); i++)
			cf_c.add(Double.valueOf(cf_c_property.get(i)));
		for(int i=0; i<eta_in_flap_property.size(); i++)
			eta_in_flap.add(Double.valueOf(eta_in_flap_property.get(i)));
		for(int i=0; i<eta_out_flap_property.size(); i++)
			eta_out_flap.add(Double.valueOf(eta_out_flap_property.get(i)));
		for(int i=0; i<delta_slat_property.size(); i++)
			deltaSlat.add(Double.valueOf(delta_slat_property.get(i)));
		for(int i=0; i<cs_c_property.size(); i++)
			cs_c.add(Double.valueOf(cs_c_property.get(i)));
		for(int i=0; i<cExt_c_slat_property.size(); i++)
			cExt_c_slat.add(Double.valueOf(cExt_c_slat_property.get(i)));
		for(int i=0; i<leRadius_c_slat_property.size(); i++)
			leRadius_c_slat.add(Double.valueOf(leRadius_c_slat_property.get(i)));
		for(int i=0; i<eta_in_slat_property.size(); i++)
			eta_in_slat.add(Double.valueOf(eta_in_slat_property.get(i)));
		for(int i=0; i<eta_out_slat_property.size(); i++)
			eta_out_slat.add(Double.valueOf(eta_out_slat_property.get(i)));

//		LSAerodynamicsManager.CalcHighLiftDevices highLiftCalculator = aircraft
//				.get_wing()
//				.getAerodynamics()
//				.new CalcHighLiftDevices(
//						aircraft.get_wing(),
//						deltaFlap,
//						flapType,
//						deltaSlat,
//						eta_in_flap,
//						eta_out_flap,
//						eta_in_slat,
//						eta_out_slat,
//						cf_c,
//						cs_c,
//						leRadius_c_slat,
//						cExt_c_slat
//						);

		CalcHighLiftDevices highLiftCalculator = new CalcHighLiftDevices(
				aircraft,
				deltaFlap,
				flapType,
				deltaSlat,
				eta_in_flap,
				eta_out_flap,
				eta_in_slat,
				eta_out_slat,
				cf_c,
				cs_c,
				leRadius_c_slat,
				cExt_c_slat
				);

		highLiftCalculator.calculateHighLiftDevicesEffects();

		//----------------------------------------------------------------------------------
		// Results print
		System.out.println("\ndeltaCl0_flap_list = ");
		for(int i=0; i<highLiftCalculator.getDeltaCl0_flap_list().size(); i++)
			System.out.print(highLiftCalculator.getDeltaCl0_flap_list().get(i) + " ");

		System.out.println("\n\ndeltaCl0_flap = \n" + highLiftCalculator.getDeltaCl0_flap());

		System.out.println("\n\ndeltaCL0_flap_list = ");
		for(int i=0; i<highLiftCalculator.getDeltaCL0_flap_list().size(); i++)
			System.out.print(highLiftCalculator.getDeltaCL0_flap_list().get(i) + " ");

		System.out.println("\n\ndeltaCL0_flap = \n" + highLiftCalculator.getDeltaCL0_flap());

		System.out.println("\n\ndeltaClmax_flap_list = ");
		for(int i=0; i<highLiftCalculator.getDeltaClmax_flap_list().size(); i++)
			System.out.print(highLiftCalculator.getDeltaClmax_flap_list().get(i) + " ");

		System.out.println("\n\ndeltaClmax_flap = \n" + highLiftCalculator.getDeltaClmax_flap());

		System.out.println("\n\ndeltaCLmax_flap_list = ");
		for(int i=0; i<highLiftCalculator.getDeltaCLmax_flap_list().size(); i++)
			System.out.print(highLiftCalculator.getDeltaCLmax_flap_list().get(i) + " ");

		System.out.println("\n\ndeltaCLmax_flap = \n" + highLiftCalculator.getDeltaCLmax_flap());

		System.out.println("\n\ndeltaClmax_slat_list = ");
		for(int i=0; i<highLiftCalculator.getDeltaClmax_slat_list().size(); i++)
			System.out.print(highLiftCalculator.getDeltaClmax_slat_list().get(i) + " ");

		System.out.println("\n\ndeltaClmax_slat = \n" + highLiftCalculator.getDeltaClmax_slat());

		System.out.println("\n\ndeltaCLmax_slat_list = ");
		for(int i=0; i<highLiftCalculator.getDeltaCLmax_slat_list().size(); i++)
			System.out.print(highLiftCalculator.getDeltaCLmax_slat_list().get(i) + " ");

		System.out.println("\n\ndeltaCLmax_slat = \n" + highLiftCalculator.getDeltaCLmax_slat());

		System.out.println("\n\ncLalpha_new_list = ");
		for(int i=0; i<highLiftCalculator.getcLalpha_new_list().size(); i++)
			System.out.print(highLiftCalculator.getcLalpha_new_list().get(i) + " ");

		System.out.println("\n\ncLalpha_new = \n" + highLiftCalculator.getcLalpha_new());

		System.out.println("\n\ndeltaAlphaMax_list = ");
		for(int i=0; i<highLiftCalculator.getDeltaAlphaMax_list().size(); i++)
			System.out.print(highLiftCalculator.getDeltaAlphaMax_list().get(i) + " ");

		System.out.println("\n\ndeltaAlphaMax = \n" + highLiftCalculator.getDeltaAlphaMax());

		System.out.println("\n\ndeltaCD_list = ");
		for(int i=0; i<highLiftCalculator.getDeltaCD_list().size(); i++)
			System.out.print(highLiftCalculator.getDeltaCD_list().get(i) + " ");

		System.out.println("\n\ndeltaCD = \n" + highLiftCalculator.getDeltaCD());
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public File get_inputFile() {
		return _inputFile;
	}
}