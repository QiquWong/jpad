package sandbox.mr;

import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcHighLiftDevices;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.JPADXmlReader;
import standaloneutils.customdata.CenterOfGravity;

public class Test_MR_Wing_06bis {

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

	//BUILDER:
	public Test_MR_Wing_06bis () {
		theCmdLineParser = new CmdLineParser(this);
	}


	public static void main(String[] args) throws InstantiationException, IllegalAccessException, CmdLineException {

		Test_MR_Wing_06bis main = new Test_MR_Wing_06bis();

		// -----------------------------------------------------------------------
		// Generate default Wing
		// -----------------------------------------------------------------------

		// Wing
		double xAw = 11.0; //meter 
		double yAw = 0.0;
		double zAw = 1.6;
		double iw = 0.0;
		LiftingSurface theWing = new LiftingSurface(
				"Wing", // name
				"Data from AC_ATR_72_REV05.pdf", 
				xAw, yAw, zAw, iw, 
				ComponentEnum.WING
				); 

		theWing.calculateGeometry();
		theWing.getGeometry().calculateAll();

		// Center of Gravity
		double xCgLocal= 1.5; // meter 
		double yCgLocal= 0;
		double zCgLocal= 0;

		CenterOfGravity cg = new CenterOfGravity(
				Amount.valueOf(xCgLocal, SI.METER), // coordinates in LRF
				Amount.valueOf(yCgLocal, SI.METER),
				Amount.valueOf(zCgLocal, SI.METER),
				Amount.valueOf(xAw, SI.METER), // origin of LRF in BRF 
				Amount.valueOf(yAw, SI.METER),
				Amount.valueOf(zAw, SI.METER),
				Amount.valueOf(0.0, SI.METER),// origin of BRF
				Amount.valueOf(0.0, SI.METER),
				Amount.valueOf(0.0, SI.METER)
				);

		cg.calculateCGinBRF();
		theWing.set_cg(cg);
		theWing.set_aspectRatio(6.0);

		// Default operating conditions
		OperatingConditions theOperatingConditions = new OperatingConditions();		
		theOperatingConditions.set_alphaCurrent(Amount.valueOf(2.0, NonSI.DEGREE_ANGLE));

		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("Operating condition");
		System.out.println("-----------------------------------------------------");
		System.out.println("\tMach: " + theOperatingConditions.get_machCurrent());
		System.out.println("\tAltitude: " + theOperatingConditions.get_altitude());
		System.out.println("\tAlpha " + theOperatingConditions.get_alphaCurrent().getEstimatedValue());
		System.out.println("----------------------");


		// allocate manager
		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager ( 
				theOperatingConditions,
				theWing
				);


		//theWing.setAerodynamics(theLSAnalysis);

		// Assign all default folders
		MyConfiguration.initWorkingDirectoryTree();

		// Setup database(s)	
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String databaseFileName = "Aerodynamic_Database_Ultimate.h5";
		String highLiftDatabaseFileName = "HighLiftDatabase.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath, databaseFileName);
		HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);

		theLSAnalysis.set_AerodynamicDatabaseReader(aeroDatabaseReader);
		theLSAnalysis.set_highLiftDatabaseReader(highLiftDatabaseReader);

		// -----------------------------------------------------------------------
		// Define airfoil
		// -----------------------------------------------------------------------

		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("AIRFOIL");
		System.out.println("-----------------------------------------------------");



		//AIRFOIL 1
		double yLocRoot = 0.0;
		MyAirfoil airfoilRoot = new MyAirfoil(theWing, yLocRoot, "23-018");
		airfoilRoot.getGeometry().update(yLocRoot);  // define chord
		airfoilRoot.getGeometry().set_maximumThicknessOverChord(0.18); //REPORT
		airfoilRoot.getGeometry().set_deltaYPercent(0.192 *airfoilRoot.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\n \n \t ROOT \nAirfoil Type: " + airfoilRoot.get_family());
		System.out.println("Root Chord " + theWing.get_chordRoot().getEstimatedValue() );
		System.out.println("Root maximum thickness " + airfoilRoot.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());		
		System.out.println("LE sharpness parameter Root " + airfoilRoot.getGeometry().get_deltaYPercent());



		//AIRFOIL 2
		double yLocKink = theWing.get_spanStationKink() * theWing.get_semispan().getEstimatedValue();
		MyAirfoil airfoilKink = new MyAirfoil(theWing, yLocKink, "23-015");
		airfoilKink.getGeometry().update(yLocKink);   // define chord
		airfoilKink.getGeometry().set_maximumThicknessOverChord(0.15); //REPORT
		airfoilKink.getGeometry().set_deltaYPercent(0.192 *airfoilKink.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\n \n \t KINK \nAirfoil Type: " + airfoilKink.get_family());
		System.out.println("Kink Station " + yLocKink);
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
		System.out.println("\n \n \t TIP \nAirfoil Type: " + airfoilKink.get_family());
		System.out.println("tip Chord " +theWing.get_chordTip().getEstimatedValue() );
		System.out.println("Tip maximum thickness " + airfoilTip.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Tip " + airfoilTip.getGeometry().get_deltaYPercent());



		// -----------------------------------------------------------------------
		// Assign airfoil
		// -----------------------------------------------------------------------


		List<MyAirfoil> myAirfoilList = new ArrayList<MyAirfoil>();
		myAirfoilList.add(0, airfoilRoot);
		myAirfoilList.add(1, airfoilKink);
		myAirfoilList.add(2, airfoilTip);
		theWing.set_theAirfoilsList(myAirfoilList);
		theWing.updateAirfoilsGeometry(); 



		// -----------------------------------------------------------------------
		// Calculate CL 
		// -----------------------------------------------------------------------

		System.out.println("---------------------------");
		System.out.println("\nEvaluating CL "); 
		System.out.println("\n---------------------------");

		Amount<Angle> alpha = Amount.valueOf(toRadians(14.), SI.RADIAN);
		LSAerodynamicsManager.CalcCLAtAlpha theCLCalculator = theLSAnalysis.new CalcCLAtAlpha();
		double CL = theCLCalculator.nasaBlackwellCompleteCurve(alpha);

		System.out.println(" At alpha " + alpha.to(NonSI.DEGREE_ANGLE) + " CL = " + CL);

		theLSAnalysis.PlotCLvsAlphaCurve();

		System.out.println("\nDONE "); 

		//----------------------------------------------------------------------------------
		// High Lift Devices Test
		List<Double[]> deltaFlap = new ArrayList<Double[]>();
		List<FlapTypeEnum> flapType = new ArrayList<FlapTypeEnum>();
		List<Double> eta_in_flap = new ArrayList<Double>();
		List<Double> eta_out_flap = new ArrayList<Double>();
		List<Double> cf_c = new ArrayList<Double>();

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
		List<String> cf_c_property = reader.getXMLPropertiesByPath("//Cf_c");
		List<String> delta_flap1_property = reader.getXMLPropertiesByPath("//Delta_Flap1");
		List<String> delta_flap2_property = reader.getXMLPropertiesByPath("//Delta_Flap2");
		List<String> eta_in_property = reader.getXMLPropertiesByPath("//Flap_inboard");
		List<String> eta_out_property = reader.getXMLPropertiesByPath("//Flap_outboard");

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
		for(int i=0; i<eta_in_property.size(); i++)
			eta_in_flap.add(Double.valueOf(eta_in_property.get(i)));
		for(int i=0; i<eta_out_property.size(); i++)
			eta_out_flap.add(Double.valueOf(eta_out_property.get(i)));

		LSAerodynamicsManager.CalcHighLiftDevices highLiftCalculator = theLSAnalysis
				.new CalcHighLiftDevices(
						theWing,
						theOperatingConditions,
						deltaFlap,
						flapType,
						null,
						eta_in_flap,
						eta_out_flap,
						null,
						null,
						cf_c,
						null,
						null,
						null
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

		System.out.println("\n\ndeltaCMc_4_list = ");
		for(int i=0; i<highLiftCalculator.getDeltaCM_c4_list().size(); i++)
			System.out.print(highLiftCalculator.getDeltaCM_c4_list().get(i) + " ");


		System.out.println("\n\ndeltaCMc_4 = \n" + highLiftCalculator.getDeltaCM_c4());		

		//		--------------------------
		//		// New lift curve 

		highLiftCalculator.PlotHighLiftCurve();
		System.out.println("DONE");	

	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public File get_inputFile() {
		return _inputFile;
	}
}
