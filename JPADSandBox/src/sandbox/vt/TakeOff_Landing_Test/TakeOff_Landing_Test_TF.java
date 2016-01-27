package sandbox.vt.TakeOff_Landing_Test;

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
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.JPADXmlReader;
import standaloneutils.customdata.CenterOfGravity;

public class TakeOff_Landing_Test_TF {
	
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
	public TakeOff_Landing_Test_TF() {
		theCmdLineParser = new CmdLineParser(this);
	}

	public static void main(String[] args) throws CmdLineException {
		
		System.out.println("-----------------------------------------------------------");
		System.out.println("TakeOff_Landing_Test :: B747-100B");
		System.out.println("-----------------------------------------------------------\n");
		
		TakeOff_Landing_Test_TF main = new TakeOff_Landing_Test_TF();
		
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

		Aircraft aircraft = Aircraft.createDefaultAircraft("B747-100B");
		aircraft.set_name("B747-100B");
		
		LiftingSurface theWing = aircraft.get_wing();
		
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

		aircraft.get_theAerodynamics().set_aerodynamicDatabaseReader(aeroDatabaseReader);
		theAnalysis.doAnalysis(aircraft, 
				AnalysisTypeEnum.AERODYNAMIC);

		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager(
				theCondition,
				theWing,
				aircraft
				);
		
		theLSAnalysis.set_AerodynamicDatabaseReader(aeroDatabaseReader);
		theLSAnalysis.set_highLiftDatabaseReader(highLiftDatabaseReader);
		theWing.setAerodynamics(theLSAnalysis);
		
		// Define airfoil
		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("AIRFOILS");
		System.out.println("-----------------------------------------------------");

		//AIRFOIL 1
		double yLocRoot = 0.0;
		MyAirfoil airfoilRoot = new MyAirfoil(theWing, yLocRoot, "23-018");
		airfoilRoot.getGeometry().update(yLocRoot);  // define chord
		airfoilRoot.getGeometry().set_maximumThicknessOverChord(0.18); //REPORT
		airfoilRoot.getGeometry().set_deltaYPercent(0.192 *airfoilRoot.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\nRoot Chord " + theWing.get_chordRoot().getEstimatedValue() );
		System.out.println("Root maximum thickness " + airfoilRoot.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());		
		System.out.println("LE sharpness parameter Root " + airfoilRoot.getGeometry().get_deltaYPercent());

		//AIRFOIL 2
		double yLocKink = theWing.get_spanStationKink() * theWing.get_semispan().getEstimatedValue();
		MyAirfoil airfoilKink = new MyAirfoil(theWing, yLocKink, "23-015");
		airfoilKink.getGeometry().update(yLocKink);   // define chord
		airfoilKink.getGeometry().set_maximumThicknessOverChord(0.15); //REPORT
		airfoilKink.getGeometry().set_deltaYPercent(0.192 *airfoilKink.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\nKink Station " + yLocKink);
		System.out.println("Kink Chord " + theWing.get_chordKink().getEstimatedValue() );
		System.out.println("Kink maximum thickness " + airfoilKink.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Kink " + airfoilKink.getGeometry().get_deltaYPercent());

		//AIRFOIL 3
		double yLocTip = theWing.get_semispan().getEstimatedValue();
		MyAirfoil airfoilTip = new MyAirfoil(theWing, yLocTip, "23-012");
		airfoilTip.getGeometry().update(yLocRoot);  // define chord
		airfoilTip.getGeometry().set_maximumThicknessOverChord(0.12); //REPORT
		airfoilTip.getGeometry().set_deltaYPercent(0.192 *airfoilTip.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\ntip Chord " +theWing.get_chordTip().getEstimatedValue() );
		System.out.println("Tip maximum thickness " + airfoilTip.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Tip " + airfoilTip.getGeometry().get_deltaYPercent() + "\n");

		// Assign airfoil
		List<MyAirfoil> myAirfoilList = new ArrayList<MyAirfoil>();
		myAirfoilList.add(0, airfoilRoot);
		myAirfoilList.add(1, airfoilKink);
		myAirfoilList.add(2, airfoilTip);
		theWing.set_theAirfoilsList(myAirfoilList);
		theWing.updateAirfoilsGeometry(); 
			
		//----------------------------------------------------------------------------------
		// High Lift Devices Input
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

		//----------------------------------------------------------------------------------
		// TakeOff - Ground Roll Distance Test
		//----------------------------------------------------------------------------------

		// temporal step
		Amount<Duration> dt = Amount.valueOf(0.5, SI.SECOND);
		Amount<Duration> dtRot = Amount.valueOf(3, SI.SECOND);
		double mu = 0.025;
		double mu_brake = 0.3;
		double k_alpha_dot = 0.07; // [1/deg]
		Amount<Length> wing_to_ground_distance = Amount.valueOf(6.56, SI.METER);
		Amount<Velocity> v_wind = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		Amount<Angle> alpha_ground = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> iw = Amount.valueOf(2.0, NonSI.DEGREE_ANGLE);
		CalcTakeOff_Landing theTakeOffLandingCalculator = new CalcTakeOff_Landing(
				aircraft,
				theCondition,
				highLiftCalculator,
				dt,
				dtRot,
				k_alpha_dot,
				mu,
				mu_brake,
				wing_to_ground_distance,
				v_wind,
				alpha_ground,
				iw
				);

		theTakeOffLandingCalculator.calculateTakeOffDistance();

		// results print
		System.out.println("\n\n\n------------------------------------------------------------");
		System.out.println(" Ground Roll Results : ");
		System.out.println("------------------------------------------------------------");

		System.out.println("\nTime = ");
		for(int i=0; i<theTakeOffLandingCalculator.getTime().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getTime().get(i) + " ");

		System.out.println("\n\nThrust = ");
		for(int i=0; i<theTakeOffLandingCalculator.getThrust().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getThrust().get(i) + " ");

		System.out.println("\n\nThrust Horizontal = ");
		for(int i=0; i<theTakeOffLandingCalculator.getThrust_horizontal().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getThrust_horizontal().get(i) + " ");

		System.out.println("\n\nThrust Vertical = ");
		for(int i=0; i<theTakeOffLandingCalculator.getThrust_vertical().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getThrust_vertical().get(i) + " ");

		System.out.println("\n\nAlpha = ");
		for(int i=0; i<theTakeOffLandingCalculator.getAlpha().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getAlpha().get(i) + " ");
		
		System.out.println("\n\nGamma = ");
		for(int i=0; i<theTakeOffLandingCalculator.getGamma().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getGamma().get(i) + " ");
		
		System.out.println("\n\nTheta = ");
		for(int i=0; i<theTakeOffLandingCalculator.getTheta().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getTheta().get(i) + " ");

		System.out.println("\n\nAlpha_dot = ");
		for(int i=0; i<theTakeOffLandingCalculator.getAlpha_dot().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getAlpha_dot().get(i) + " ");

		System.out.println("\n\ncL = ");
		for(int i=0; i<theTakeOffLandingCalculator.getcL().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getcL().get(i) + " ");

		System.out.println("\n\nLift = ");
		for(int i=0; i<theTakeOffLandingCalculator.getLift().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getLift().get(i) + " ");

		System.out.println("\n\ncD = ");
		for(int i=0; i<theTakeOffLandingCalculator.getcD().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getcD().get(i) + " ");

		System.out.println("\n\nDrag = ");
		for(int i=0; i<theTakeOffLandingCalculator.getDrag().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getDrag().get(i) + " ");

		System.out.println("\n\nFriction = ");
		for(int i=0; i<theTakeOffLandingCalculator.getFriction().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getFriction().get(i) + " ");

		System.out.println("\n\nForce_Total = ");
		for(int i=0; i<theTakeOffLandingCalculator.getTotal_force().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getTotal_force().get(i) + " ");

		System.out.println("\n\nLoad_Factor = ");
		for(int i=0; i<theTakeOffLandingCalculator.getLoadFactor().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getLoadFactor().get(i) + " ");
		
		System.out.println("\n\nAcceleration = ");
		for(int i=0; i<theTakeOffLandingCalculator.getAcceleration().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getAcceleration().get(i) + " ");

		System.out.println("\n\nMean Acceleration = ");
		for(int i=0; i<theTakeOffLandingCalculator.getMean_acceleration().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getMean_acceleration().get(i) + " ");

		System.out.println("\n\nSpeed = ");
		for(int i=0; i<theTakeOffLandingCalculator.getSpeed().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getSpeed().get(i) + " ");

		System.out.println("\n\nMean Speed = ");
		for(int i=0; i<theTakeOffLandingCalculator.getMean_speed().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getMean_speed().get(i) + " ");

		System.out.println("\n\nRate of Climb = ");
		for(int i=0; i<theTakeOffLandingCalculator.getRateOfClimb().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getRateOfClimb().get(i) + " ");
		
		System.out.println("\n\nMean Rate of Climb = ");
		for(int i=0; i<theTakeOffLandingCalculator.getMeanRateOfClimb().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getMeanRateOfClimb().get(i) + " ");
		
		System.out.println("\n\ndelta Ground Distance = ");
		for(int i=0; i<theTakeOffLandingCalculator.getDelta_GroundDistance().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getDelta_GroundDistance().get(i) + " ");

		System.out.println("\n\nGround Distance = ");
		for(int i=0; i<theTakeOffLandingCalculator.getGround_distance().size(); i++)
			System.out.print(theTakeOffLandingCalculator.getGround_distance().get(i) + " ");
	}
	
	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public File get_inputFile() {
		return _inputFile;
	}
}