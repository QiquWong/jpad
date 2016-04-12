package sandbox.vt.AGILE_Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
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
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import sandbox.vt.TakeOff_Test.CalcTakeOff;
import standaloneutils.JPADXmlReader;
import standaloneutils.customdata.CenterOfGravity;
import standaloneutils.customdata.MyArray;

public class TakeOff_Test_agile_DC1 {

	private static long _startTimeCalculation, _startTimeGraph, _startTimeBalancedCalculation,
	_startTimeBalancedGraph, _stopTimeBalancedGraph, _stopTimeCalculation,
	_stopTimeGraph, _stopTimeBalancedCalculation, _stopTimeTotal,
	_elapsedTimeTotal, _elapsedTimeCalculation, _elapsedTimeGraph,
	_elapsedTimeBalancedCalculation, _elapsedTimeBalancedGraph;

	//	TODO: example of custom NON_SI unit
	//	private static Unit<? extends Quantity> angularRateUnit = (NonSI.DEGREE_ANGLE).divide((SI.SECOND));

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
	public TakeOff_Test_agile_DC1() {
		theCmdLineParser = new CmdLineParser(this);
	}

	public static void main(String[] args) throws CmdLineException, InstantiationException, IllegalAccessException {

		System.out.println("-----------------------------------------------------------");
		System.out.println("Landing_Test :: agile-dc1");
		System.out.println("-----------------------------------------------------------\n");

		TakeOff_Test_agile_DC1 main = new TakeOff_Test_agile_DC1();

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
		theCondition.set_altitude(Amount.valueOf(0.0, SI.METER));
		theCondition.set_machCurrent(0.15);
		theCondition.calculate();

		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.AGILE_DC1);
		aircraft.set_name("AGILE-DC1");

		aircraft.get_weights().set_MTOM(Amount.valueOf(39750, SI.KILOGRAM)); 

		LiftingSurface theWing = aircraft.get_wing();

		// update of the wing with new model parameters
		theWing.set_surface(Amount.valueOf(82.7, SI.SQUARE_METRE));
		theWing.set_aspectRatio(9.54);
		theWing.set_taperRatioEquivalent(0.217);
		theWing.set_taperRatioInnerPanel(0.425);
		theWing.set_taperRatioOuterPanel(0.387);
		theWing.set_taperRatioCrankedWing(0.1645);
		theWing.set_spanStationKink(0.398);
		theWing.set_extensionLERootChordLinPanel(0.16886);
		theWing.set_extensionTERootChordLinPanel(0.505361);
		theWing.set_iw(Amount.valueOf(Math.toRadians(2.5),SI.RADIAN));
		//TODO: Eliminate
		//				theWing.set_iw(Amount.valueOf(Math.toRadians(0.0),SI.RADIAN));
		//				theWing.set_twistKink(Amount.valueOf(Math.toRadians(0.0),SI.RADIAN));
		//				theWing.set_twistTip(Amount.valueOf(Math.toRadians(0.0),SI.RADIAN));
		//				theWing.set_twistKink(Amount.valueOf(Math.toRadians(-1.592),SI.RADIAN));
		//				theWing.set_twistTip(Amount.valueOf(Math.toRadians(-4),SI.RADIAN));
		//--------------------------------------------------------
		theWing.set_dihedralInnerPanel(Amount.valueOf(Math.toRadians(6.0), SI.RADIAN));
		theWing.set_dihedralOuterPanel(Amount.valueOf(Math.toRadians(6.0), SI.RADIAN));
		MyArray _dihedral = new MyArray(new double[] {theWing.get_dihedralInnerPanel().getEstimatedValue(), theWing.get_dihedralOuterPanel().getEstimatedValue()});
		theWing.set_dihedral(_dihedral);
		theWing.set_chordRoot(Amount.valueOf(6.39, SI.METER));
		theWing.set_chordKink(Amount.valueOf(2.716, SI.METER)); 
		theWing.set_chordTip(Amount.valueOf(1.051, SI.METER)); 
		theWing.set_tc_root(0.161);
		theWing.set_tc_kink(0.149);
		theWing.set_tc_tip(0.119);
		theWing.set_xLERoot(Amount.valueOf(0.0, SI.METER));
		theWing.set_xLEKink(Amount.valueOf(3.707, SI.METER));
		theWing.set_xLETip(Amount.valueOf(8.305, SI.METER));
		theWing.set_sweepQuarterChordEq(Amount.valueOf(26.3, NonSI.DEGREE_ANGLE));
		theWing.set_sweepLEEquivalent(
				theWing.calculateSweep(
						theWing.get_sweepQuarterChordEq().getEstimatedValue(),
						0.0,
						0.25)
				); 


		//AIRFOILS DEFINITION (initialize and set data):
		//AIRFOIL ROOT
		double yLocRoot = 0.0;		
		MyAirfoil airfoilRoot = new MyAirfoil(theWing, yLocRoot);
		airfoilRoot.getAerodynamics().set_clAlpha(7.0336);
		airfoilRoot.getAerodynamics().set_clStar(1.2);
		airfoilRoot.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(9.5), SI.RADIAN));
		airfoilRoot.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(22.5), SI.RADIAN));
		airfoilRoot.getAerodynamics().set_clMax(2.0);
		airfoilRoot.getGeometry().set_maximumThicknessOverChord(0.161);
		airfoilRoot.getGeometry().set_radiusLE(0.03892);
		airfoilRoot.getGeometry().set_deltaYPercent(4.375);
		airfoilRoot.set_chordLocal(6.39);
		airfoilRoot.getGeometry().set_twist(Amount.valueOf(Math.toRadians(0.0), SI.RADIAN));
		// the followings are not necessaries to the high lift devices effects analysis
		airfoilRoot.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-1.9864), SI.RADIAN));
		airfoilRoot.getAerodynamics().set_cdMin(0.0);
		airfoilRoot.getAerodynamics().set_clAtCdMin(0.0);
		airfoilRoot.getAerodynamics().set_kFactorDragPolar(0.0);
		airfoilRoot.getAerodynamics().set_aerodynamicCenterX(0.0);
		airfoilRoot.getAerodynamics().set_cmAC(0.0);
		airfoilRoot.getAerodynamics().set_cmACStall(0.0);
		airfoilRoot.getAerodynamics().set_cmAlphaLE(0.0);
		airfoilRoot.getAerodynamics().set_reynoldsCruise(0.0);
		airfoilRoot.getAerodynamics().set_reynoldsNumberStall(0.0);
		airfoilRoot.getGeometry().set_twist(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		airfoilRoot.getGeometry().set_anglePhiTE(Amount.valueOf(Math.toRadians(0.0), SI.RADIAN));
		airfoilRoot.getGeometry().set_thicknessOverChordUnit(0.0);

		//AIRFOIL KINK
		double yLocKink = 5.5919;	
		MyAirfoil airfoilKink = new MyAirfoil(theWing, yLocKink);
		airfoilKink.getAerodynamics().set_clAlpha(6.9533);
		airfoilKink.getAerodynamics().set_clStar(1.2);
		airfoilKink.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(8.5), SI.RADIAN));
		airfoilKink.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(21.0), SI.RADIAN));
		airfoilKink.getAerodynamics().set_clMax(2.0);
		airfoilKink.getGeometry().set_maximumThicknessOverChord(0.149);
		airfoilKink.getGeometry().set_radiusLE(0.04265);
		airfoilKink.getGeometry().set_deltaYPercent(3.88);
		airfoilKink.set_chordLocal(2.716);
		airfoilKink.getGeometry().set_twist(Amount.valueOf(Math.toRadians(-1.592), SI.RADIAN));
		// the followings are not necessaries to the high lift devices effects analysis
		airfoilKink.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-1.6289), SI.RADIAN));
		airfoilKink.getAerodynamics().set_cdMin(0.0);
		airfoilKink.getAerodynamics().set_clAtCdMin(0.0);
		airfoilKink.getAerodynamics().set_kFactorDragPolar(0.0);
		airfoilKink.getAerodynamics().set_aerodynamicCenterX(0.0);
		airfoilKink.getAerodynamics().set_cmAC(0.0);
		airfoilKink.getAerodynamics().set_cmACStall(0.0);
		airfoilKink.getAerodynamics().set_cmAlphaLE(0.0);
		airfoilKink.getAerodynamics().set_reynoldsCruise(0.0);
		airfoilKink.getAerodynamics().set_reynoldsNumberStall(0.0);
		airfoilKink.getGeometry().set_twist(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		airfoilKink.getGeometry().set_anglePhiTE(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		airfoilKink.getGeometry().set_thicknessOverChordUnit(0.0);

		//AIRFOIL TIP
		double yLocTip = 14.05;	
		MyAirfoil airfoilTip = new MyAirfoil(theWing, yLocTip);
		airfoilTip.getAerodynamics().set_clAlpha(6.6210);
		airfoilTip.getAerodynamics().set_clStar(1.15);
		airfoilTip.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(8.0), NonSI.DEGREE_ANGLE));
		airfoilTip.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(15.5), NonSI.DEGREE_ANGLE));
		airfoilTip.getAerodynamics().set_clMax(1.8);
		airfoilTip.getGeometry().set_maximumThicknessOverChord(0.119);
		airfoilTip.getGeometry().set_radiusLE(0.01011);
		airfoilTip.getGeometry().set_deltaYPercent(2.92);
		airfoilTip.set_chordLocal(1.051);
		airfoilTip.getGeometry().set_twist(Amount.valueOf(Math.toRadians(-4.0), SI.RADIAN));
		// the followings are not necessaries to the high lift devices effects analysis
		airfoilTip.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-3.1795), SI.RADIAN));
		airfoilTip.getAerodynamics().set_cdMin(0.0);
		airfoilTip.getAerodynamics().set_clAtCdMin(0.0);
		airfoilTip.getAerodynamics().set_kFactorDragPolar(0.0);
		airfoilTip.getAerodynamics().set_aerodynamicCenterX(0.0);
		airfoilTip.getAerodynamics().set_cmAC(0.0);
		airfoilTip.getAerodynamics().set_cmACStall(0.0);
		airfoilTip.getAerodynamics().set_cmAlphaLE(0.0);
		airfoilTip.getAerodynamics().set_reynoldsCruise(0.0);
		airfoilTip.getAerodynamics().set_reynoldsNumberStall(0.0);
		airfoilTip.getGeometry().set_twist(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		airfoilTip.getGeometry().set_anglePhiTE(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		airfoilTip.getGeometry().set_thicknessOverChordUnit(0.0);

		// ASSIGN AIRFOILS:
		List<MyAirfoil> myAirfoilList = new ArrayList<MyAirfoil>();
		myAirfoilList.add(0, airfoilRoot);
		myAirfoilList.add(1, airfoilKink);
		myAirfoilList.add(2, airfoilTip);
		theWing.set_theAirfoilsList(myAirfoilList);
		theWing.get_theAirfoilsList().get(1).getGeometry().set_twist(Amount.valueOf(Math.toRadians(-1.0), SI.RADIAN));
		theWing.get_theAirfoilsList().get(2).getGeometry().set_twist(Amount.valueOf(Math.toRadians(-5.0), SI.RADIAN));
		//				theWing.get_theAirfoilsList().get(1).getGeometry().set_twist(Amount.valueOf(Math.toRadians(0.0), SI.RADIAN));
		//				theWing.get_theAirfoilsList().get(2).getGeometry().set_twist(Amount.valueOf(Math.toRadians(0.0), SI.RADIAN));

		//------------------------------------------------------------------------------------
		// UPDATE DATA
		theWing.calculateGeometry();
		theWing.getGeometry().calculateAll();
		theWing.updateAirfoilsGeometry();

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

		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager(
				theCondition,
				theWing,
				aircraft
				);

		theLSAnalysis.set_AerodynamicDatabaseReader(aeroDatabaseReader);
		theAnalysis.doAnalysis(aircraft,AnalysisTypeEnum.AERODYNAMIC);
		theLSAnalysis.setHighLiftDatabaseReader(highLiftDatabaseReader);

		theWing.setAerodynamics(theLSAnalysis);

		// MEAN AIRFOIL:
		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("Starting evaluate the mean airfoil characteristics");
		System.out.println("-----------------------------------------------------");

		MyAirfoil meanAirfoil = theWing
				.getAerodynamics()
				.new MeanAirfoil()
				.calculateMeanAirfoil(
						theWing
						);

		double meanAlphaStar = meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue();
		System.out.println("\nThe mean alpha star is [rad] = " + meanAlphaStar);
		double alphaStarDeg = Math.toDegrees(meanAlphaStar);
		System.out.println("The mean alpha star is [deg] = " + alphaStarDeg);
		double meanLESharpnessParameter = meanAirfoil.getGeometry().get_deltaYPercent();
		Amount<Angle> deltaAlphaMax;

		//----------------------------------------------------------------------------------
		// INITIALIZING HIGH LIFT DEVICES INPUT DATA
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

		//----------------------------------------------------------------------------------
		// XML READING PHASE

		// Arguments check
		if (args.length == 0){
			System.err.println("NO INPUT FILE GIVEN --> TERMINATING");
			return;
		}
		// Input file parsing
		main.theCmdLineParser.parseArgument(args);

		// Creation of the reader object
		String path = main.get_inputFile().getAbsolutePath();
		JPADXmlReader reader = new JPADXmlReader(path);

		System.out.println("-----------------------------------------------------------");
		System.out.println("XML File Path : " + path);
		System.out.println("-----------------------------------------------------------");
		System.out.println("Initialize reading... \n");

		// XML file scan through JPADXmlReader methods
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
		List<String> eta_in_slat_property = reader.getXMLPropertiesByPath("//Slat_inboard");
		List<String> eta_out_slat_property = reader.getXMLPropertiesByPath("//Slat_outboard");

		// Recognizing flap type
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

		// Management of the Lists of String in order to populate the previous Lists
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
		for(int i=0; i<delta_slat_property.size(); i++)
			leRadius_c_slat.add(Double.valueOf(
					(meanAirfoil.getGeometry().get_radiusLE()/theWing.get_meanAerodChordEq().getEstimatedValue())/
					(meanAirfoil.getGeometry().get_maximumThicknessOverChord())
					));
		for(int i=0; i<eta_in_slat_property.size(); i++)
			eta_in_slat.add(Double.valueOf(eta_in_slat_property.get(i)));
		for(int i=0; i<eta_out_slat_property.size(); i++)
			eta_out_slat.add(Double.valueOf(eta_out_slat_property.get(i)));

		LSAerodynamicsManager.CalcHighLiftDevices highLiftCalculator = theLSAnalysis
				.new CalcHighLiftDevices(
						aircraft.get_wing(),
						theCondition,
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

		//		highLiftCalculator.calculateHighLiftDevicesEffects();
		//
		//		//----------------------------------------------------------------------------------
		//		// Results print
		//		System.out.println("\ndeltaCl0_flap_list = ");
		//		for(int i=0; i<highLiftCalculator.getDeltaCl0_flap_list().size(); i++)
		//			System.out.print(highLiftCalculator.getDeltaCl0_flap_list().get(i) + " ");
		//
		//		System.out.println("\n\ndeltaCl0_flap = \n" + highLiftCalculator.getDeltaCl0_flap());
		//
		//		System.out.println("\n\ndeltaCL0_flap_list = ");
		//		for(int i=0; i<highLiftCalculator.getDeltaCL0_flap_list().size(); i++)
		//			System.out.print(highLiftCalculator.getDeltaCL0_flap_list().get(i) + " ");
		//
		//		System.out.println("\n\ndeltaCL0_flap = \n" + highLiftCalculator.getDeltaCL0_flap());
		//
		//		System.out.println("\n\ndeltaClmax_flap_list = ");
		//		for(int i=0; i<highLiftCalculator.getDeltaClmax_flap_list().size(); i++)
		//			System.out.print(highLiftCalculator.getDeltaClmax_flap_list().get(i) + " ");
		//
		//		System.out.println("\n\ndeltaClmax_flap = \n" + highLiftCalculator.getDeltaClmax_flap());
		//
		//		System.out.println("\n\ndeltaCLmax_flap_list = ");
		//		for(int i=0; i<highLiftCalculator.getDeltaCLmax_flap_list().size(); i++)
		//			System.out.print(highLiftCalculator.getDeltaCLmax_flap_list().get(i) + " ");
		//
		//		System.out.println("\n\ndeltaCLmax_flap = \n" + highLiftCalculator.getDeltaCLmax_flap());
		//
		//		System.out.println("\n\ncLalpha_new_list = ");
		//		for(int i=0; i<highLiftCalculator.getcLalpha_new_list().size(); i++)
		//			System.out.print(highLiftCalculator.getcLalpha_new_list().get(i) + " ");
		//
		//		System.out.println("\n\ncLalpha_new = \n" + highLiftCalculator.getcLalpha_new());
		//
		//		System.out.println("\n\ndeltaCD_list = ");
		//		for(int i=0; i<highLiftCalculator.getDeltaCD_list().size(); i++)
		//			System.out.print(highLiftCalculator.getDeltaCD_list().get(i) + " ");
		//
		//		System.out.println("\n\ndeltaCD = \n" + highLiftCalculator.getDeltaCD());
		//
		//		System.out.println("\n\ndeltaCMc_4_list = ");
		//		for(int i=0; i<highLiftCalculator.getDeltaCM_c4_list().size(); i++)
		//			System.out.print(highLiftCalculator.getDeltaCM_c4_list().get(i) + " ");
		//
		//		System.out.println("\n\ndeltaCMc_4 = \n" + highLiftCalculator.getDeltaCM_c4());
		//
		//		System.out.println("--------------CLEAN----------------");
		//		System.out.println(" alpha max " + theWing.getAerodynamics().get_alphaStall().to(NonSI.DEGREE_ANGLE));
		//		System.out.println(" alpha star " + theWing.getAerodynamics().get_alphaStar().to(NonSI.DEGREE_ANGLE));
		//		System.out.println(" cL max" + theWing.getAerodynamics().get_cLMaxClean());
		//		System.out.println(" cL star " + theWing.getAerodynamics().getcLStarWing());
		//		
		//		highLiftCalculator.plotHighLiftCurve();

		//----------------------------------------------------------------------------------
		// TakeOff Distance Test
		//----------------------------------------------------------------------------------
		_startTimeCalculation = System.currentTimeMillis();
		Amount<Duration> dtRot = Amount.valueOf(3, SI.SECOND);
		Amount<Duration> dtHold = Amount.valueOf(0.5, SI.SECOND);
		double mu = 0.03;
		double muBrake = 0.3;
		double kAlphaDot = 0.04; // [1/deg]
		double kcLMax = 0.9;
		double kRot = 1.05;
		double kLO = 1.1;
		double kFailure = 1.1;

//		PARAMETERS USED TO CONSIDER THE PARABOLIC DRAG POLAR CORRECTION AT HIGH CL
		double k1 = 0.0;
		double k2 = 0.0;

		double phi = 1.0;
		double alphaReductionRate = -4; // [deg/s]
		double deltaCD0LandingGear = 0.018; // see chart Nicolai pag.273 pdf
		Amount<Length> wingToGroundDistance = Amount.valueOf(6.56, SI.METER);
		Amount<Length> obstacle = Amount.valueOf(35, NonSI.FOOT).to(SI.METER);
		Amount<Velocity> vWind = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		Amount<Angle> alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> iw = Amount.valueOf(3.0, NonSI.DEGREE_ANGLE);
		CalcTakeOff theTakeOffCalculator = new CalcTakeOff(
				aircraft,
				theCondition,
				highLiftCalculator,
				dtRot,
				dtHold,
				kcLMax,
				kRot,
				kLO,
				kFailure,
				k1,
				k2,
				phi,
				kAlphaDot,
				alphaReductionRate,
				mu,
				muBrake,
				deltaCD0LandingGear,
				wingToGroundDistance,
				obstacle,
				vWind,
				alphaGround,
				iw
				);

		theTakeOffCalculator.calculateTakeOffDistanceODE(null, false);
		Amount<Length> takeOffDistance = theTakeOffCalculator.getGroundDistance().get(theTakeOffCalculator.getGroundDistance().size()-1);
		_stopTimeCalculation = System.currentTimeMillis();
		_startTimeGraph = System.currentTimeMillis();
		theTakeOffCalculator.createTakeOffCharts();
		_stopTimeGraph = System.currentTimeMillis();
		_startTimeBalancedCalculation = System.currentTimeMillis();
		theTakeOffCalculator.calculateBalancedFieldLength();
		_stopTimeBalancedCalculation = System.currentTimeMillis();
		_startTimeBalancedGraph = System.currentTimeMillis();
		theTakeOffCalculator.createBalancedFieldLengthChart();
		_stopTimeBalancedGraph = System.currentTimeMillis();
		_stopTimeTotal = System.currentTimeMillis();

		_elapsedTimeTotal = _stopTimeTotal - _startTimeCalculation;
		_elapsedTimeCalculation = _stopTimeCalculation - _startTimeCalculation;
		_elapsedTimeGraph = _stopTimeGraph - _startTimeGraph;
		_elapsedTimeBalancedCalculation = _stopTimeBalancedCalculation - _startTimeBalancedCalculation;
		_elapsedTimeBalancedGraph = _stopTimeBalancedGraph - _startTimeBalancedGraph;

		System.out.println("\n------------------COMPUTATIONAL TIME-----------------------");
		System.out.println("\nANALYSIS TIME = " + (get_elapsedTime()) + " millisenconds");
		System.out.println("\nCALCULATION TIME = " + (get_elapsedTimeCalculation()) + " millisenconds");
		System.out.println("\nBALANCED FIELD LENGTH CALCULATION TIME = " + (get_elapsedTimeBalanced()) + " millisenconds");
		System.out.println("\nBALANCED FIELD LENGTH GRAPH TIME = " + (get_elapsedTimeBalancedGraph()) + " millisenconds");
		System.out.println("\nGRAPHICS TIME = " + (get_elapsedTimeGraph()) + " millisenconds");
		System.out.println("-----------------------------------------------------------\n");
		System.out.println("\n-----------------------------------------------------------");
		System.out.println("\nTAKE-OFF DISTANCE = " + takeOffDistance);
		System.out.println("\nFAR-25 TAKE-OFF FIELD LENGTH = " + takeOffDistance.times(1.15));
		System.out.println("\nBALANCED FIELD LENGTH = " + theTakeOffCalculator.getBalancedFieldLength());
		System.out.println("\nDecision Speed (V1/VsTO) = " + theTakeOffCalculator.getV1().divide(theTakeOffCalculator.getvSTakeOff()));
		System.out.println("-----------------------------------------------------------\n");
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public File get_inputFile() {
		return _inputFile;
	}

	public static long get_elapsedTime() {
		return _elapsedTimeTotal;
	}

	public static long get_elapsedTimeGraph() {
		return _elapsedTimeGraph;
	}

	public static long get_elapsedTimeCalculation() {
		return _elapsedTimeCalculation;
	}

	public static long get_elapsedTimeBalanced() {
		return _elapsedTimeBalancedCalculation;
	}

	public static long get_elapsedTimeBalancedGraph() {
		return _elapsedTimeBalancedGraph;
	}
}