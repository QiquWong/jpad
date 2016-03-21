package sandbox.vt.HighLift_Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.Wing;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.JPADXmlReader;
import standaloneutils.customdata.MyArray;

public class AGILE_Test_HighLift {

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
	public AGILE_Test_HighLift() {
		theCmdLineParser = new CmdLineParser(this);
	}

	public static void main(String[] args) throws CmdLineException {

		System.out.println("-----------------------------------------------------------");
		System.out.println("HighLiftDevices_Test :: AGILE DC1");
		System.out.println("-----------------------------------------------------------\n");

		AGILE_Test_HighLift main = new AGILE_Test_HighLift();

		//----------------------------------------------------------------------------------
		// DEFAULT FOLDERS CREATION:
		MyConfiguration.initWorkingDirectoryTree();

		//------------------------------------------------------------------------------------
		// SETTING UP DATABASE:
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		String highLiftDatabaseFileName = "HighLiftDatabase.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);

		//--------------------------------------------------------------------------------------
		// DEFINE OPERATING CONDITIONS, THE WING AND THE WING ANALYZER OBJECTS:
		OperatingConditions theCondition = new OperatingConditions();
		// landing conditions:
		theCondition.set_altitude(Amount.valueOf(0.0, SI.METER));
		theCondition.set_machCurrent(0.15);
		theCondition.calculate();
		
		Wing theWing = new Wing(ComponentEnum.WING);
		theWing.set_aspectRatio(9.57);
		theWing.set_chordRootEquivalentWing(Amount.valueOf(4.92, SI.METER));
		theWing.set_taperRatioEquivalent(0.218);
		theWing.set_sweepQuarterChordEq(Amount.valueOf(26.2, NonSI.DEGREE_ANGLE));
		theWing.set_surface(Amount.valueOf(85.51, SI.SQUARE_METRE));
		theWing.set_span(Amount.valueOf(28.6, SI.METER));
		theWing.set_maxThicknessMean(0.142);
		theWing.set_meanAerodChordEq(Amount.valueOf(3.8, SI.METER));
		// data needed to initialize theLSAnalysis object
		theWing.set_surfaceWetted(Amount.valueOf(0.0, SI.SQUARE_METRE));
		theWing.set_semispan(Amount.valueOf(0.0, SI.METER));
		theWing.set_chordRoot(Amount.valueOf(0.0, SI.METER));
		theWing.set_dihedralMean(Amount.valueOf(0.0, SI.RADIAN));
		theWing.set_sweepHalfChordEq(Amount.valueOf(0.0, SI.RADIAN));
		theWing.set_twistVsY(new MyArray());
		theWing.set_alpha0VsY(new MyArray());
		theWing.set_etaAirfoil(new MyArray());
		
		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager(theCondition, theWing);
		// Assigning database to theLSAnalysis
		theLSAnalysis.setHighLiftDatabaseReader(highLiftDatabaseReader);
		theLSAnalysis.set_AerodynamicDatabaseReader(aeroDatabaseReader);
		
		//--------------------------------------------------------------------------------------
		// DEFINE AIRFOILS (initialize and set data):
		
		//AIRFOIL ROOT
		double yLocRoot = 0.0;		
		MyAirfoil airfoilRoot = new MyAirfoil(theWing, yLocRoot);
		airfoilRoot.getAerodynamics().set_clAlpha(6.8531);
		airfoilRoot.getAerodynamics().set_clStar(1.4473);
		airfoilRoot.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(10.5), SI.RADIAN));
		airfoilRoot.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(24.0), SI.RADIAN));
		airfoilRoot.getAerodynamics().set_clMax(2.293);
		airfoilRoot.getGeometry().set_maximumThicknessOverChord(0.161);
		airfoilRoot.getGeometry().set_radiusLE(0.03892);
		// the followings are not necessaries to the high lift devices effects analysis
		airfoilRoot.getAerodynamics().set_alphaZeroLift(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
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
		airfoilRoot.getGeometry().set_anglePhiTE(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		airfoilRoot.getGeometry().set_thicknessOverChordUnit(0.0);
		airfoilRoot.getGeometry().set_deltaYPercent(0.0);
		airfoilRoot.getGeometry().update(yLocRoot);

		//AIRFOIL KINK
		double yLocKink = 5.148;	
		MyAirfoil airfoilKink = new MyAirfoil(theWing, yLocKink);
		airfoilKink.getAerodynamics().set_clAlpha(6.7786);
		airfoilKink.getAerodynamics().set_clStar(1.375);
		airfoilKink.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(9.25), SI.RADIAN));
		airfoilKink.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(23.0), SI.RADIAN));
		airfoilKink.getAerodynamics().set_clMax(2.187);
		airfoilKink.getGeometry().set_maximumThicknessOverChord(0.149);
		airfoilKink.getGeometry().set_radiusLE(0.04265);
		// the followings are not necessaries to the high lift devices effects analysis
		airfoilKink.getAerodynamics().set_alphaZeroLift(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
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
		airfoilKink.getGeometry().set_deltaYPercent(0.0);
		airfoilKink.getGeometry().update(yLocRoot);
		
		//AIRFOIL TIP
		double yLocTip = 14.3;	
		MyAirfoil airfoilTip = new MyAirfoil(theWing, yLocTip);
		airfoilTip.getAerodynamics().set_clAlpha(6.4061);
		airfoilTip.getAerodynamics().set_clStar(1.1954);
		airfoilTip.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(7.5), NonSI.DEGREE_ANGLE));
		airfoilTip.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(18.5), NonSI.DEGREE_ANGLE));
		airfoilTip.getAerodynamics().set_clMax(1.9419);
		airfoilTip.getGeometry().set_maximumThicknessOverChord(0.119);
		airfoilTip.getGeometry().set_radiusLE(0.01011);
		// the followings are not necessaries to the high lift devices effects analysis
		airfoilTip.getAerodynamics().set_alphaZeroLift(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
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
		airfoilTip.getGeometry().set_deltaYPercent(0.0);
		airfoilTip.getGeometry().update(yLocRoot);
		
		//--------------------------------------------------------------------------------------
		// Assign airfoil
		List<MyAirfoil> myAirfoilList = new ArrayList<MyAirfoil>();
		myAirfoilList.add(0, airfoilRoot);
		myAirfoilList.add(1, airfoilKink);
		myAirfoilList.add(2, airfoilTip);
		theWing.set_theAirfoilsList(myAirfoilList);
		theWing.updateAirfoilsGeometry();

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
		List<String> leRadius_c_slat_property = reader.getXMLPropertiesByPath("//LEradius_c_ratio");
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
		for(int i=0; i<leRadius_c_slat_property.size(); i++)
			leRadius_c_slat.add(Double.valueOf(leRadius_c_slat_property.get(i)));
		for(int i=0; i<eta_in_slat_property.size(); i++)
			eta_in_slat.add(Double.valueOf(eta_in_slat_property.get(i)));
		for(int i=0; i<eta_out_slat_property.size(); i++)
			eta_out_slat.add(Double.valueOf(eta_out_slat_property.get(i)));

		//----------------------------------------------------------------------------------
		// CREATING HIGH LIFT ANALYZER OBJECT:
		LSAerodynamicsManager.CalcHighLiftDevices highLiftCalculator = theLSAnalysis
				.new CalcHighLiftDevices(
						theWing,
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

		//----------------------------------------------------------------------------------
		// ANALYSIS OF HIGH LIFT DEVICES EFFECTS:
		highLiftCalculator.calculateHighLiftDevicesEffects();

		//----------------------------------------------------------------------------------
		// RESULTS OF HIGH LIFT DEVICES ANALYSIS:
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

		System.out.println("\n\ndeltaCD_list = ");
		for(int i=0; i<highLiftCalculator.getDeltaCD_list().size(); i++)
			System.out.print(highLiftCalculator.getDeltaCD_list().get(i) + " ");

		System.out.println("\n\ndeltaCD = \n" + highLiftCalculator.getDeltaCD());

		System.out.println("\n\ndeltaCMc_4_list = ");
		for(int i=0; i<highLiftCalculator.getDeltaCM_c4_list().size(); i++)
			System.out.print(highLiftCalculator.getDeltaCM_c4_list().get(i) + " ");

		System.out.println("\n\ndeltaCMc_4 = \n" + highLiftCalculator.getDeltaCM_c4());
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public File get_inputFile() {
		return _inputFile;
	}
}