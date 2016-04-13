package sandbox.mr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;


import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.fuselage.FusAerodynamicsManager;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.HTail;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCDAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcHighLiftDevices;
import calculators.aerodynamics.MomentCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AeroConfigurationTypeEnum;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import functions.Linspace;
import igeo.io.IObjFileImporter.GeometricVertex;
import jmatrix.Matrix;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.CenterOfGravity;
import standaloneutils.customdata.MyArray;

public class ACStabilityManager {

	// VARIABLE DECLARATION--------------------------------------

	OperatingConditions theOperatingConditions = new OperatingConditions();
	LSAerodynamicsManager theLSAnalysis;
	LSAerodynamicsManager theLSHTailAnalysis;
	FusAerodynamicsManager theFuselageManager;
	StabilityCalculator theStabilityCalculator = new StabilityCalculator();
	ConditionEnum theCondition;
	DownwashCalculator theDownwashCalculator;
	LSAerodynamicsManager.CalcCLAtAlpha theCLWingCalculator;
	LSAerodynamicsManager.CalcHighLiftDevices highLiftWingCalculator ;
	CalcHighLiftDevices theHighLiftTailalculator;
	FusDesDatabaseReader fusDesDatabaseReader;

	double finenessRatio ;
	double noseFinenessRatio ;
	double tailFinenessRatio;
	double upsweepAngle;
	double windshieldAngle;
	double xPositionPole;
	double fusSurfRatio;
	double clAlphaWing;

	MyAirfoil meanAirfoil;

	int nValueAlpha = 61;

	CenterOfGravity centerOfGravity = new CenterOfGravity();
	Amount<Length> maxXaftCenterOfGravityBRF;
	Amount<Length> maxXforwCenterOfGravityBRF;
	Amount<Angle> alphaBody = null;
	Amount<Angle> alphaWing = null;
	Amount<Angle> alphaMin;
	Amount<Angle> alphaMax;
	Amount<Angle> alphaMinWing;
	Amount<Angle> alphaMaxWing;
	Amount<Angle> alphaMinHTail;
	Amount<Angle> alphaMaxHtail;
	Amount<Angle> alphaHorizontalTail ;

	Amount<Force> weight;

	Aircraft aircraft;
	LiftingSurface theWing;
	Fuselage theFuselage;
	LiftingSurface theHTail;
	String subfolderPath;
	String pathXMLTakeOFF = null;
	String pathXMLLanding = null;
	boolean alphaCheck;
	boolean plotCheck = false;


	MyArray alphaStabilityArray = new MyArray(); //alphaBody
	MyArray alphaStabilityHLArray = new MyArray();

	MyArray alphaWingStabilityArray = new MyArray();
	MyArray alphaWingStabilityHLArray = new MyArray();


	Map<String,double[]> cLHTailMap = new HashMap< String, double[]>();
	Map<String,double[]> alphaHTailMap = new HashMap< String, double[]>();

	Map<String,double[]> cDHTailMap = new HashMap< String, double[]>();
	Map<String,double[]> alphacDHTailMap = new HashMap< String, double[]>();

	Map<String,double[]> cMHTailMap = new HashMap< String, double[]>();
	Map<String,double[]> alphacMHTailMap = new HashMap< String, double[]>();

	Map<String,double[]> cMTotalRespectToCGMap = new HashMap< String, double[]>();
	Map<String,double[]> alphacMTotalRespectToCGMap = new HashMap< String, double[]>();

	Map<String,double[]> cMHorizTailRespectToCGMap = new HashMap< String, double[]>();
	
	Map<String,double[]> cLTotMap = new HashMap< String, double[]>();



	//Output Values

	double cLIsolatedWing;
	double cLIsolatedWingTO;
	double cLIsolatedWingLand;
	double cLWingBody;

	double cLMaxWingActual;

	double cLAlphaWingActual;
	double cLAlphaHTailClean; 
	double cLAlphaWingBody;
	double cLAlphaTO;
	double cLAlphaLand;

	double alphaMaxWingActual;
	double alphaMaxTO;
	double alphaMaxLand;
	double alphaStarActual; // deg
	double alphaStarHtail; 


	double cLMaxTO;
	double cLMaxLand;


	double [] cLWingActualArray;


	double deltaMin = -25;
	double deltaMax = 5;
	double [] deltaEArray = MyArrayUtils.linspace(deltaMin, deltaMax , 7);


	String [] deltaEArrayString = new String [deltaEArray.length];

	double xACWingBodyLRF;
	double xACWingBodyBRF;
	double xACHTailBRF;
	double zACHtailBRF;


	//Output Arrays // Correspond to alpha body array
	private double [] cLWingCleanArray;
	private double [] cLWingTOArray;
	private double [] cLWingLandingArray;
	private double [] cLWingBodyArray;
	private double [] cLAlphaArray;
	private double [] cLHTailCleanArray;
	private Double [] downwashAnglesArray;
	private double [] tauIndexArray = new double [deltaEArray.length];
	private double [] cLCompleteAircraftdeltaEArray = new double [deltaEArray.length];
	private double [] cLCompleteAircraft;

	private double [] parasiteCDWingCleanArray;
	private double [] parasiteCDHTailCleanArray;
	private double [] inducedCDWingArray;
	private double [] inducedCDHTailArray;
	private double [] cDWingArray;
	private double [] cDWingCleanArray;
	private double [] cDWingTakeOffArray;
	private double [] cDWingLandingArray;
	private double [] cDHTailCleanArray;
	private double [] cDCompleteAircraft;
	private double [] cD0WingPolarArray;
	private double [] cDiWingPolarArray;
	private double [] cDWaweWingPolarArray;
	private double [] cDWingPolarArray;

	private double [] cmQuarterChordWingArray; 
	private double [] cmACWingArray; 
	private double [] cmQuarterChordHtailArray; 
	private double [] cmACHtailArray; 
	private double [] cmFuselageArray; 
	private double [] cMacWingBody;

	private double [] cNWingBody;
	private double [] cNWing;
	private double [] cCIsolatedWing;

	double [] momentNonAxialThrust;


	double cm0LiftFuselage;
	double aCWing;
	double aCHTail;
	double acWingBody;
	double xACWingBody; //dimens
	double xACWingLRF;
	double xACWingBRF;

	double nonAxialPitchEffectDerivative;
	double [] momentThrust;

	// High Lift Devices Input
	List<Double[]> deltaFlap = new ArrayList<Double[]>();
	List<Double> deltaSlat = new ArrayList<Double>();
	List<FlapTypeEnum> flapType = new ArrayList<FlapTypeEnum>();
	List<Double> etaInFlap = new ArrayList<Double>();
	List<Double> etaOutFlap = new ArrayList<Double>();
	List<Double> etaInSlat = new ArrayList<Double>();
	List<Double> etaOutSlat = new ArrayList<Double>();
	List<Double> cfc = new ArrayList<Double>();
	List<Double> csc = new ArrayList<Double>();
	List<Double> leRadiusCSlat = new ArrayList<Double>();
	List<Double> cExtCSlat = new ArrayList<Double>();
	private double downwashAngleAtAlpha;
	private double[] alphaStabilityHTailArray;
	private double xCGc;



	// BUILDER--------------------------------------

	/**
	 * This class manages the calculation of the longitudinal static stability of an aircraft.
	 * 
	 * @author Manuela Ruocco
	 * @param the aircraft
	 * @param the minimum value of alpha array (alpha body array). It can to be in degree or radian.
	 * This is an aLPHA BODY array. This value may to be null in order to execute
	 * analysis only for an angle of attack. In this case the values of alpha max and alpha min wil be alpha body +-1 deg
	 * @param the maximum value of alpha array. It can to be in degree or radian
	 * @param the actual condition (take off, landing, cruise)
	 * @param the angle of attack (alpha body) for the calculation of numerical value of CL, CD, CM. This value may be null. In this case the class calculates
	 * only all stability characteristics at an array of alpha body
	 * @param When this check value is true will be draw all graphs
	 */
	public ACStabilityManager(MyAirfoil meanAirfoil, Aircraft theAircraft, ConditionEnum theCondition, Amount<Angle> alphaMin,
			Amount<Angle> alphaMax, Amount<Angle> alphaBody, boolean plotCheck, String subfolderPath, String pathXMLHighLift){

		this.aircraft = theAircraft;
		this.theWing = aircraft.get_wing();
		this.theFuselage = aircraft.get_fuselage();
		this.theHTail = aircraft.get_HTail();
		this.meanAirfoil = meanAirfoil;
		this.subfolderPath = subfolderPath;

		this.theCondition = theCondition;

		if (theCondition == ConditionEnum.TAKE_OFF)
			this.pathXMLTakeOFF = pathXMLHighLift;

		if(theCondition == ConditionEnum.LANDING)
			this.pathXMLLanding = pathXMLHighLift;


		this.plotCheck = plotCheck;

		theLSAnalysis = theWing.getAerodynamics();
		theLSHTailAnalysis = aircraft.get_HTail().getAerodynamics();

		for (int i =0 ; i<deltaEArrayString.length; i++){
			deltaEArrayString[i] = Double.toString(deltaEArray[i]);
		}


		this.alphaBody = alphaBody;
		if (alphaBody==null){
			alphaCheck = false;}
		else
		{if (alphaBody.getUnit() == NonSI.DEGREE_ANGLE){
			alphaBody = alphaBody.to(SI.RADIAN);
		}
		alphaCheck = true;}


		if (alphaMin==null || alphaMax==null){
			alphaMin = Amount.valueOf((alphaBody.getEstimatedValue() - Math.toRadians(1)),SI.RADIAN);
			alphaMax = Amount.valueOf((alphaBody.getEstimatedValue() + Math.toRadians(1)),SI.RADIAN);
		}

		if (alphaMin.getUnit() == SI.RADIAN){
			alphaMin = alphaMin.to(NonSI.DEGREE_ANGLE);
		}

		if (alphaMax.getUnit() == SI.RADIAN){
			alphaMax = alphaMax.to(NonSI.DEGREE_ANGLE);
		}

		this.alphaMin = alphaMin;
		this.alphaMax = alphaMax;

		this.alphaMinWing =Amount.valueOf((
				alphaMin.getEstimatedValue() + theWing.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue()), NonSI.DEGREE_ANGLE);
		this.alphaMaxWing =Amount.valueOf((
				alphaMax.getEstimatedValue() + theWing.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue()), NonSI.DEGREE_ANGLE);


		alphaStabilityArray.linspace(alphaMin.getEstimatedValue(), alphaMax.getEstimatedValue(), nValueAlpha);
		alphaStabilityHLArray.linspace(alphaMin.getEstimatedValue(), alphaMax.getEstimatedValue()-2, nValueAlpha);

		alphaWingStabilityArray.linspace(alphaMinWing.getEstimatedValue(),alphaMaxWing.getEstimatedValue(), nValueAlpha);
		alphaWingStabilityHLArray.linspace(alphaMinWing.getEstimatedValue(), alphaMaxWing.getEstimatedValue()-2, nValueAlpha);

		alphaStabilityHTailArray = new double [nValueAlpha];

		System.out.println(" alpha stability array " + alphaStabilityArray);

		//Set Operating Conditions and CG position 

		switch (theCondition) {
		case TAKE_OFF:
			theOperatingConditions.set_machCurrent(aircraft.get_theAerodynamics().get_machTakeOFF());
			theOperatingConditions.set_altitude(Amount.valueOf(0.0, SI.METER));
			break;

		case LANDING:	
			theOperatingConditions.set_machCurrent(aircraft.get_theAerodynamics().get_machLanding());
			theOperatingConditions.set_altitude(Amount.valueOf(0.0, SI.METER));
			break;

		case CRUISE:	
			theOperatingConditions.set_machCurrent(aircraft.get_theAerodynamics().get_machCruise());
			theOperatingConditions.set_altitude(aircraft.get_theAerodynamics().getCruiseAltitude());
			break;
		}

		theOperatingConditions.set_alphaCurrent(alphaBody);
		theOperatingConditions.calculate();

		theWing.getAerodynamics().setTheOperatingConditions(theOperatingConditions);
		theWing.getAerodynamics().initializeDataFromOperatingConditions(theOperatingConditions);
		theWing.getAerodynamics().initializeInnerCalculators();

		ACAnalysisManager theAnalysis = new ACAnalysisManager(theOperatingConditions);

		theFuselage.getAerodynamics().set_theOperatingConditions(theOperatingConditions);
		theFuselage.getAerodynamics().initializeDependentData();
		theFuselage.getAerodynamics().initializeInnerCalculators();

		theHTail.getAerodynamics().setTheOperatingConditions(theOperatingConditions);
		theHTail.getAerodynamics().initializeDataFromOperatingConditions(theOperatingConditions);
		theHTail.getAerodynamics().initializeInnerCalculators();

		theLSAnalysis = theWing.getAerodynamics();

		theFuselageManager = new FusAerodynamicsManager(theOperatingConditions, aircraft);

		switch (theCondition) {
		case TAKE_OFF:
			centerOfGravity = theAircraft.get_theBalance().get_cgMTOM();
			break;

		case LANDING:	
			centerOfGravity = theAircraft.get_theBalance().get_cgMZFM();
			break;

		case CRUISE:	
			CenterOfGravity centerOfGravityTempMTOM = theAircraft.get_theBalance().get_cgMTOM();
			CenterOfGravity centerOfGravityTempMZFM =  theAircraft.get_theBalance().get_cgMZFM();

			Amount<Length> x0 = Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_x0().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_x0().getEstimatedValue())/2),centerOfGravityTempMTOM.get_x0().getUnit()) ;
			Amount<Length> y0 = Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_y0().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_y0().getEstimatedValue())/2),centerOfGravityTempMTOM.get_y0().getUnit()) ;
			Amount<Length> z0 = Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_z0().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_z0().getEstimatedValue())/2),centerOfGravityTempMTOM.get_z0().getUnit()) ;

			Amount<Length> xL = Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_xLRF().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_xLRF().getEstimatedValue())/2),centerOfGravityTempMTOM.get_xLRF().getUnit()) ;
			Amount<Length> yL =  Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_yLRF().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_yLRF().getEstimatedValue())/2),centerOfGravityTempMTOM.get_yLRF().getUnit()) ;
			Amount<Length> zL = Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_zLRF().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_zLRF().getEstimatedValue())/2),centerOfGravityTempMTOM.get_zLRF().getUnit()) ;

			Amount<Length> xB  = Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_xBRF().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_xBRF().getEstimatedValue())/2),centerOfGravityTempMTOM.get_xBRF().getUnit()) ;
			Amount<Length> yB =  Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_yBRF().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_yBRF().getEstimatedValue())/2),centerOfGravityTempMTOM.get_yBRF().getUnit()) ;
			Amount<Length> zB = Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_zBRF().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_zBRF().getEstimatedValue())/2),centerOfGravityTempMTOM.get_zBRF().getUnit()) ;
			zB = Amount.valueOf(0.2*aircraft.get_wing().get_meanAerodChordActual().getEstimatedValue(),SI.METER);
			centerOfGravity = new CenterOfGravity(x0, y0 , z0 , xL, yL, zL, xB, yB, zB);
			//centerOfGravity.calculateCGinBRF();

			break;
		}

		switch (theCondition) {
		case TAKE_OFF:
			weight = theAircraft.get_weights().get_MTOW();
			break;

		case LANDING:	
			weight = theAircraft.get_weights().get_MZFW();
			break;

		case CRUISE:
			weight = Amount.valueOf((theAircraft.get_weights().get_MTOW().getEstimatedValue() + 
					theAircraft.get_weights().get_MZFW().getEstimatedValue())/2, SI.NEWTON);
			break;
		}
		maxXaftCenterOfGravityBRF = Amount.valueOf((centerOfGravity.get_xBRF().getEstimatedValue()*(1+0.1)), centerOfGravity.get_xBRF().getUnit());
		maxXforwCenterOfGravityBRF = Amount.valueOf((centerOfGravity.get_xBRF().getEstimatedValue()*(1-0.1)), centerOfGravity.get_xBRF().getUnit());


		// Set database directory	
		String databaseFolderPathfus = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String databaseFileName = "FusDes_database.h5";


		//--------------------------------------------------------------------------------------
		theFuselageManager = new FusAerodynamicsManager(theOperatingConditions, aircraft);


		finenessRatio       = aircraft.get_fuselage().get_lambda_F().doubleValue();
		noseFinenessRatio   = aircraft.get_fuselage().get_lambda_N().doubleValue();
		tailFinenessRatio   = aircraft.get_fuselage().get_lambda_T().doubleValue();
		upsweepAngle 	   = aircraft.get_fuselage().get_upsweepAngle().getEstimatedValue();
		windshieldAngle     = aircraft.get_fuselage().get_windshieldAngle().getEstimatedValue();
		xPositionPole	   = 0.5;
		fusSurfRatio = aircraft.get_fuselage().get_area_C().doubleValue(SI.SQUARE_METRE)/
				aircraft.get_wing().get_surface().doubleValue(SI.SQUARE_METRE);

		fusDesDatabaseReader = new FusDesDatabaseReader(databaseFolderPathfus, databaseFileName);
		fusDesDatabaseReader.runAnalysis(noseFinenessRatio, windshieldAngle, finenessRatio, tailFinenessRatio, upsweepAngle, xPositionPole);

		System.out.println("\n\n------------------------------------");
		System.out.println("\n CONDITION : " + theCondition);
		System.out.println("\n------------------------------------");

	}


	public void calculateAll() throws InstantiationException, IllegalAccessException{

		// Lift Characteristics
		calculateLiftCharacteristics();
		CalculateDragCharacteristics();
		CalculateMomentCharacteristics();


		// CL --> need to consider flap contributes

		//CL, CD, CM... 
	}

	public void calculateLiftCharacteristics() throws InstantiationException, IllegalAccessException {
		System.out.println("\n\n------------------------------------");
		System.out.println("\n LIFT CHARACTERISTICS  ");
		System.out.println("\n------------------------------------");
		CalculateWingLiftCharacteristics();
		CalculateFuselageLiftCharacteristics();
		CalculateHTailLiftCharacteristics();
		CalculateCompleteAircraftLiftCharacteristics();

	}

	public void CalculateDragCharacteristics(){
		System.out.println("\n\n------------------------------------");
		System.out.println("\n DRAG CHARACTERISTICS  ");
		System.out.println("\n------------------------------------");
		CalculateWingDragCharacteristics();
		CalculateHTailDragCHaracteristics();
		calculateCompleteAircraftDragCharacteristics();
	}

	public void CalculateMomentCharacteristics(){
		System.out.println("\n\n------------------------------------");
		System.out.println("\n PITCHING MOMENT  ");
		System.out.println("\n------------------------------------");

		calculateWingMomentCharacteristics();
		calculateHTailMomentCharacteristics();
		calculateFuselageMomentCharacteristics();
		if (aircraft.get_typeVehicle()==AircraftTypeEnum.TURBOPROP){
			calculatePowerEffects();
		}
		calculateMoment();
		calculatedeltaEEquilibrium();
		neutralPointCalculator();
	}

	public void CalculateWingLiftCharacteristics() throws InstantiationException, IllegalAccessException {


		System.out.println("\n ------------------- ");
		System.out.println("|       WING        |");
		System.out.println(" ------------------- \n\n");

		System.out.println("\t Data: ");

		// DATA
		System.out.println("Angle of incidence of wing (deg) = " + theWing.get_iw().getEstimatedValue()*57.3);
		if(alphaCheck == true){
			System.out.println("Angle of attack alpha body (deg) = " + "" 
					+ (alphaBody.getEstimatedValue()*57.3));
			alphaWing = Amount.valueOf(
					(alphaBody.getEstimatedValue() + theWing.get_iw().getEstimatedValue()), SI.RADIAN);
			System.out.println("Angle of attack alpha Wing (deg) = " + Math.toDegrees(alphaWing.getEstimatedValue()));}



		if (theCondition == ConditionEnum.TAKE_OFF || theCondition == ConditionEnum.LANDING){
			// READ TAKE OFF DATA
			System.out.println("\n\n------------------------------------");
			System.out.println("\n READING XML " + theCondition + " FILE...  ");
			System.out.println("\n------------------------------------");


			// Arguments check
			if (pathXMLTakeOFF == null && pathXMLLanding == null){
				System.err.println("NO " + theCondition + " sINPUT FILE GIVEN --> TERMINATING");
				return;
			}

			JPADXmlReader reader = new JPADXmlReader(pathXMLTakeOFF);

			System.out.println("-----------------------------------------------------------");
			System.out.println("XML File Path : " + pathXMLTakeOFF);
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
				cfc.add(Double.valueOf(cf_c_property.get(i)));
			for(int i=0; i<eta_in_property.size(); i++)
				etaInFlap.add(Double.valueOf(eta_in_property.get(i)));
			for(int i=0; i<eta_out_property.size(); i++)
				etaOutFlap.add(Double.valueOf(eta_out_property.get(i)));
			System.out.println("------------------DONE-------------------\n\n");

			if (aircraft.get_typeVehicle() == AircraftTypeEnum.TURBOPROP){
				highLiftWingCalculator = theLSAnalysis
						.new CalcHighLiftDevices(
								theWing,
								theOperatingConditions,
								deltaFlap,
								flapType,
								null,
								etaInFlap,
								etaOutFlap,
								etaInSlat,
								etaOutSlat,
								cfc,
								csc,
								leRadiusCSlat,
								cExtCSlat
								);
			}
			else{
				highLiftWingCalculator = theLSAnalysis
						.new CalcHighLiftDevices(
								theWing,
								theOperatingConditions,
								deltaFlap,
								flapType,
								deltaSlat,
								etaInFlap,
								etaOutFlap,
								etaInSlat,
								etaOutSlat,
								cfc,
								csc,
								leRadiusCSlat,
								cExtCSlat
								);
			}
			highLiftWingCalculator.calculateHighLiftDevicesEffects();
			highLiftWingCalculator.calcCLatAlphaHighLiftDevice(Amount.valueOf(Math.toRadians(2.0), SI.RADIAN));
			alphaStarActual = theWing.getAerodynamics().get_alphaStarHigLift().to(NonSI.DEGREE_ANGLE).getEstimatedValue();

			// RESULTS


			System.out.println(" -----------CLEAN-------------- ");
			System.out.println(" alpha max " + theLSAnalysis.get_alphaMaxClean().to(NonSI.DEGREE_ANGLE));
			System.out.println(" alpha star " + theLSAnalysis.get_alphaStar().to(NonSI.DEGREE_ANGLE));
			System.out.println(" cl max " + theLSAnalysis.get_cLMaxClean());
			System.out.println(" cl star " + theLSAnalysis.getcLStarWing());
			System.out.println(" cl alpha " + theLSAnalysis.getcLLinearSlopeNB() + " (1/rad)");
			System.out.println("\n");


			System.out.println("\n ----------- DELTA HIGH LIFT " + theCondition + "-------------- ");
			System.out.println("deltaCL0_flap = " + highLiftWingCalculator.getDeltaCL0_flap());
			System.out.println("deltaCLmax_flap = " + highLiftWingCalculator.getDeltaCLmax_flap());
			System.out.println("deltaCLmax_slat = " + highLiftWingCalculator.getDeltaCLmax_slat());
			System.out.println("cLalpha_new = (1/rad) " + highLiftWingCalculator.getcLalpha_new()* 57.3);
			System.out.println("deltaAlphaMax = (deg) " + highLiftWingCalculator.getDeltaAlphaMaxFlap());
			System.out.println("deltaCD = " + highLiftWingCalculator.getDeltaCD());
			System.out.println("deltaCMc_4 = " + highLiftWingCalculator.getDeltaCM_c4());
			System.out.println("\n\n");

			alphaMaxWingActual = theLSAnalysis.get_alphaMaxClean().to(NonSI.DEGREE_ANGLE).getEstimatedValue() 
					+ highLiftWingCalculator.getDeltaAlphaMaxFlap();
			cLMaxWingActual = theLSAnalysis.get_cLMaxClean() + highLiftWingCalculator.getDeltaCLmax_flap() + 
					highLiftWingCalculator.getDeltaCLmax_slat();
			cLAlphaWingActual =  highLiftWingCalculator.getcLalpha_new()* 57.3;

			if( theCondition == ConditionEnum.TAKE_OFF ){
				alphaMaxTO = alphaMaxWingActual;
				cLMaxTO = cLMaxWingActual;
				cLAlphaTO = cLAlphaWingActual;
			}

			if( theCondition == ConditionEnum.LANDING ){
				alphaMaxLand = alphaMaxWingActual;
				cLMaxLand = cLMaxWingActual;
				cLAlphaLand = cLAlphaWingActual;
			}


			//			cLAlphaWing = highLiftCalculator.getcLalpha_new();

			//ARRAY FILLING

			if (pathXMLTakeOFF != null){
				cLWingTOArray = highLiftWingCalculator.calcCLvsAlphaBodyHighLiftDevices(alphaMin, 
						Amount.valueOf((alphaMax.getEstimatedValue()), NonSI.DEGREE_ANGLE),
						nValueAlpha);

				cLWingActualArray = cLWingTOArray;

				System.out.println("CL wing " + theCondition + " Array " + Arrays.toString(cLWingTOArray ) );
			}
			if(pathXMLLanding != null){
				cLWingLandingArray =  highLiftWingCalculator.calcCLvsAlphaBodyHighLiftDevices(alphaMin,
						Amount.valueOf((alphaMax.getEstimatedValue()),NonSI.DEGREE_ANGLE),
						nValueAlpha);

				cLWingActualArray = cLWingLandingArray;

				System.out.println("CL wing " + theCondition + " Array " + Arrays.toString(cLWingLandingArray) );
			}




			// CALCULATING CL AT ALPHA

			if(alphaCheck == true){
				cLIsolatedWingTO = highLiftWingCalculator.calcCLatAlphaHighLiftDevices(alphaWing);
				System.out.println("\nCL of wing at " + theCondition + " at alpha body = " + cLIsolatedWingTO + "\n");
			}

			//			//PLOT CL VS ALPHA
			highLiftWingCalculator.plotHighLiftCurve(subfolderPath);
			//
			//			//initializing
			//			List<Double[]> cLHLListPlot = new ArrayList<Double[]>();
			//			List<Double[]> alphaHLListPlot = new ArrayList<Double[]>();
			//			List<String> legendCLvsAlphaHighLift  = new ArrayList<>();	
			//
			//			//clean vector
			//			LSAerodynamicsManager.CalcCLvsAlphaCurve theCLArrayCalculator = theLSAnalysis.new CalcCLvsAlphaCurve();
			//			cLWingCleanArray = theCLArrayCalculator.nasaBlackwellCompleteCurve(alphaMinWing, alphaMaxWing, nValueAlpha, false);
			//
			//
			//			// filling lists
			//
			//			Double[] cLWingCleanArrayDouble = new Double[cLWingCleanArray.length];
			//			Double[] cLWingHLArrayDouble = new Double[cLWingTOArray.length];
			//			Double [] alphaArrayDouble = new Double[cLWingCleanArray.length];
			//
			//			for (int i=0; i<cLWingCleanArray.length; i++){
			//				cLWingCleanArrayDouble[i] = (Double)cLWingCleanArray[i];
			//				alphaArrayDouble[i] = (Double)alphaStabilityArray.toArray()[i];
			//			}
			//			cLHLListPlot.add(0,cLWingCleanArrayDouble);
			//			alphaHLListPlot.add(0,alphaArrayDouble);
			//			legendCLvsAlphaHighLift.add("clean");
			//
			//			if (pathXMLTakeOFF != null){
			//				for (int i=0; i<cLWingTOArray.length; i++){
			//					cLWingHLArrayDouble[i] = (Double)cLWingTOArray[i];
			//					alphaArrayDouble[i] = (Double)alphaStabilityHLArray.toArray()[i];
			//					}}
			//			if	(pathXMLLanding != null){
			//				for (int i=0; i<cLWingTOArray.length; i++){
			//					cLWingHLArrayDouble[i] = (Double)cLWingLandingArray[i];
			//					alphaArrayDouble[i] = (Double)alphaStabilityHLArray.toArray()[i];
			//					}}
			//
			//			cLHLListPlot.add(1,cLWingHLArrayDouble);
			//			alphaHLListPlot.add(1,alphaArrayDouble);
			//			legendCLvsAlphaHighLift.add(theCondition.toString());
			//
			//
			//
			//
			//			MyChartToFileUtils.plotJFreeChart(alphaHLListPlot,
			//					cLHLListPlot,
			//					"CL vs alpha " + theCondition.toString() ,
			//					"alpha_Wing",
			//					"CL",
			//					null, null, null,null,
			//					"deg",
			//					"",
			//					true,
			//					legendCLvsAlphaHighLift,
			//					subfolderPath,
			//					"CL vs alpha wing clean and " + theCondition.toString());

			System.out.println("\n\n\t\t\tWRITING CL vs ALPHA CHART TO FILE for wing");

		}

		else{ // clean

			//ARRAY FILLING
			LSAerodynamicsManager.CalcCLvsAlphaCurve theCLArrayCalculator = theLSAnalysis.new CalcCLvsAlphaCurve();

			cLWingCleanArray = theCLArrayCalculator.nasaBlackwellCompleteCurve(alphaMinWing, alphaMaxWing, nValueAlpha, true);
			cLWingActualArray = cLWingCleanArray;
			System.out.println("CL wing Clean Array " + Arrays.toString(cLWingCleanArray) );
			cLAlphaWingActual = theLSAnalysis.getcLLinearSlopeNB();
			alphaStarActual = theWing.getAerodynamics().get_alphaStar().to(NonSI.DEGREE_ANGLE).getEstimatedValue();


			//CALCULATING CL AT ALPHA FOR WING
			if(alphaCheck == true){
				theCLWingCalculator = theLSAnalysis.new CalcCLAtAlpha();
				cLIsolatedWing = theCLWingCalculator.nasaBlackwellAlphaBody(alphaBody);
				System.out.println("\nCL of Isolated wing at alpha body = " + cLIsolatedWing);
			}



			//PLOT CL VS ALPHA
			if(plotCheck == true){
				System.out.println("\n-------------------------------------");
				System.out.println("\t \t \tWRITING CL vs ALPHA CHART TO FILE  ");
				MyChartToFileUtils.plotNoLegend(
						alphaWingStabilityArray.toArray(),cLWingCleanArray, 
						null, null , null , null ,					    // axis with limits
						"alpha_Wing", "CL", "deg", "",	   				
						subfolderPath, "CL vs Alpha clean Wing" );
				System.out.println("\t \t \tDONE  ");

				//PLOT stall path //TODO delete this

				System.out.println("\n-------------------------------------");
				System.out.println("\t \t \tWRITING STALL PATH CHART TO FILE  ");

				LSAerodynamicsManager.CalcCLMaxClean theCLmaxAnalysis = theLSAnalysis.new CalcCLMaxClean();
				MyArray clMaxAirfoil = theCLmaxAnalysis.getClAirfoils();
				Amount<Angle> alphaWingStall = theLSAnalysis.get_alphaStall();
				double alphaSecond = theLSAnalysis.getAlphaArray().get(3);
				double alphaThird = theLSAnalysis.getAlphaArray().get(6);
				MyArray clAlphaStall = theLSAnalysis.getcLMap()
						.getCxyVsAlphaTable()
						.get(MethodEnum.NASA_BLACKWELL ,alphaWingStall);
				MyArray clSecond = theLSAnalysis.getcLMap()
						.getCxyVsAlphaTable()
						.get(MethodEnum.NASA_BLACKWELL ,Amount.valueOf(alphaSecond, SI.RADIAN));
				MyArray clThird = theLSAnalysis.getcLMap()
						.getCxyVsAlphaTable()
						.get(MethodEnum.NASA_BLACKWELL ,Amount.valueOf(alphaThird, SI.RADIAN));

				double [][] semiSpanAd = {
						theLSAnalysis.get_yStationsND(), theLSAnalysis.get_yStationsND(),
						theLSAnalysis.get_yStationsND(), theLSAnalysis.get_yStationsND(),
						theLSAnalysis.get_yStationsND(), theLSAnalysis.get_yStationsND(),
						theLSAnalysis.get_yStationsND(), theLSAnalysis.get_yStationsND()};

				double [][] clDistribution = {
						clMaxAirfoil.getRealVector().toArray(),
						clSecond.getRealVector().toArray(),
						clThird.getRealVector().toArray(),
						clAlphaStall.getRealVector().toArray()};

				String [] legend = new String [4];
				legend[0] = "CL max airfoil";
				legend[1] = "CL distribution at alpha " 
						+ Math.toDegrees( alphaSecond);
				legend[2] = "CL distribution at alpha " 
						+ Math.toDegrees( alphaThird);
				legend[3] = "CL distribution at alpha " 
						+ Math.toDegrees( alphaWingStall.getEstimatedValue());

				MyChartToFileUtils.plot(
						semiSpanAd,	clDistribution, // array to plot
						0.0, 1.0, 0.0, 2.0,					    // axis with limits
						"eta", "CL", "", "",	    // label with unit
						legend,					// legend
						subfolderPath, "Stall Path of Wing ");			    // output informations

				System.out.println("\t \t \tDONE  ");
				//				double[] AlphaArr = {0,
				//						0.03448276,
				//						0.06896552,
				//						0.10344828,
				//						0.13793104,
				//						0.1724138,
				//						0.20689656,
				//						0.24137932,
				//						0.27586208,
				//						0.31034484,
				//						0.3448276,
				//						0.37931036,
				//						0.41379312,
				//						0.44827588,
				//						0.48275864,
				//						0.5172414,
				//						0.55172416,
				//						0.58620692,
				//						0.62068968,
				//						0.65517244,
				//						0.6896552,
				//						0.72413796,
				//						0.75862072,
				//						0.79310348,
				//						0.82758624,
				//						0.862069,
				//						0.89655176,
				//						0.93103452,
				//						0.96551728,
				//						1.00000004};
				//				
				//
				//				double alphabis = theLSAnalysis.getAlphaArray().get(5);
				//				System.out.println(" alpha IS " + alphabis);
				//				theLSAnalysis.getCalculateLiftDistribution().getNasaBlackwell().calculate(Amount.valueOf(Math.toRadians(10),SI.RADIAN));
				//				double [] clnew = theLSAnalysis.getCalculateLiftDistribution().getNasaBlackwell().get_clTotalDistribution().toArray();
				//				Double [] clInt = MyMathUtils.getInterpolatedValue1DLinear(theLSAnalysis.get_yStationsND(), clnew, AlphaArr);
				//				for(int i = 0; i<clInt.length; i++){
				//					System.out.println(clInt[i]);
				//				}

			}
		}

		cLAlphaArray = new double [alphaWingStabilityArray.size()];
		double alphaStarDeg = theWing.getAerodynamics().get_alphaStar().to(NonSI.DEGREE_ANGLE).getEstimatedValue();
		for (int i=0 ; i<alphaWingStabilityArray.size()-1; i++){
			if(alphaWingStabilityArray.get(i) < alphaStarActual){
				cLAlphaArray[i] = aircraft.get_wing().getAerodynamics().getcLLinearSlopeNB();}
			else{
				cLAlphaArray[i]=Math.toDegrees((((cLWingActualArray[i+1] - cLWingActualArray[i])/(alphaWingStabilityArray.get(i+1) - alphaWingStabilityArray.get(i)))+((cLWingActualArray[i+1] - cLWingActualArray[i])/
						(alphaWingStabilityArray.get(i+1) - alphaWingStabilityArray.get(i))))/2);
				//				cLAlphaArray[i]=Math.toDegrees((cLArray[i+1] - cLArray[i])/(alphaAbsoluteArray[i+1] - alphaAbsoluteArray [i]));
			}
		}
		cLAlphaArray[cLAlphaArray.length-1] = cLAlphaArray[cLAlphaArray.length-2];
	}


	public void CalculateFuselageLiftCharacteristics(){

		System.out.println("\n ------------------- ");
		System.out.println("|     FUSELAGE       |");
		System.out.println(" ------------------- \n\n");


		//ARRAY FILLING
		cLWingBodyArray = aircraft.get_theAerodynamics().calculateCLvsAlphaWingBody(alphaMin, alphaMax, nValueAlpha, theCondition);


		System.out.println("Cl alpha Wing " + cLAlphaWingActual);
		cLAlphaWingBody = theFuselageManager.calculateCLAlphaFuselage(cLAlphaWingActual);
		System.out.println("Cl alpha Wing Body " + cLAlphaWingBody);

		//CALCULATING CL AT ALPHA FOR WING

		cLWingBody = aircraft.get_theAerodynamics().calculateCLAtAlphaWingBody(alphaBody, meanAirfoil, true, theCondition);
		System.out.println("-------------------------------------");
		System.out.println(" CL of Wing Body at alpha body = " + cLWingBody);

		if(plotCheck == true){
			System.out.println("\n \t \t \tWRITING CL VS ALPHA CHARTS TO FILE");

			double [][] alpha = {
					alphaStabilityArray.toArray(), alphaStabilityArray.toArray()};

			double [][] clDistribution = {cLWingActualArray,cLWingBodyArray };

			String [] legend = new String [4];
			legend[0] = " Wing";
			legend[1] = "Wing Body ";


			MyChartToFileUtils.plot(
					alpha, clDistribution, // array to plot
					null,null,null, null,					    // axis with limits
					"alpha", "CL", "", "",	    // label with unit
					legend,					// legend
					subfolderPath, "CL vs Alpha wing and wing body");			

			System.out.println("DONE");
		}

	}

	public void CalculateHTailLiftCharacteristics() throws InstantiationException, IllegalAccessException{

		System.out.println("\n ------------------- ");
		System.out.println("|  HORIZONTAL TAIL   |");
		System.out.println(" ------------------- \n\n");

		// In order to evaluate the angle of attack of the horizzontal tail it's necessary to evaluate the downwash angle
		System.out.println(" DOWNWASH CALCULATION... \n");

		theDownwashCalculator = new DownwashCalculator(aircraft, cLAlphaArray, alphaWingStabilityArray.toArray());
		theDownwashCalculator.calculateDownwashNonLinearDelft();
		downwashAngleAtAlpha = theDownwashCalculator.getDownwashAtAlphaBody(alphaBody);

		downwashAnglesArray = MyMathUtils.getInterpolatedValue1DLinear(theDownwashCalculator.getAlphaBodyArray(), 
				theDownwashCalculator.getDownwashArray(),
				alphaStabilityArray.toArray());


		if (plotCheck == true ){
			theDownwashCalculator.plotDownwashDelftWithPath(subfolderPath);
			theDownwashCalculator.plotDownwashGradientDelftWithPath(subfolderPath);
			theDownwashCalculator.plotZDistanceWithPath(subfolderPath);
			theDownwashCalculator.plotXDistanceWithPath(subfolderPath);}

		System.out.println("\n \n-----------angles-------------- ");
		System.out.println("Angle of attack alpha body (deg) = " + Math.ceil(alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()));
		System.out.println("Angle of incidence of horizontal tail (deg) " + aircraft.get_HTail().get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue());
		System.out.println("Downwash Angle at Alpha Body (deg) " + downwashAngleAtAlpha );

		double angleHorizontalDouble = alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()
				- downwashAngleAtAlpha +  aircraft.get_HTail().get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue();
		alphaHorizontalTail = Amount.valueOf(Math.toRadians(angleHorizontalDouble), SI.RADIAN);
		System.out.println("Angle of Attack of Horizontal Tail (deg) "
				+ angleHorizontalDouble);

		//CLEAN 


		LSAerodynamicsManager.CalcCLvsAlphaCurve theCLHTailArrayCalculator = theLSHTailAnalysis.new CalcCLvsAlphaCurve();
		LSAerodynamicsManager.CalcCLAtAlpha theCLHtailCalculator = theLSHTailAnalysis.new CalcCLAtAlpha();

		Amount<Angle> alphaActualHtail;
		cLHTailCleanArray = new double [alphaStabilityArray.size()];
		double [] alphaActualHTailArray =  new double [alphaStabilityArray.size()];
		for (int i=0; i<alphaStabilityArray.size(); i++){
			alphaStabilityHTailArray [i] = alphaStabilityArray.get(i)-downwashAnglesArray[i] + theHTail.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue();

			alphaActualHtail = Amount.valueOf(Math.toRadians(alphaStabilityArray.get(i)) - 
					Math.toRadians(downwashAnglesArray[i]) 
					+ theHTail.get_iw().getEstimatedValue(), SI.RADIAN);
			alphaActualHTailArray[i] = alphaActualHtail.to(NonSI.DEGREE_ANGLE).getEstimatedValue();
			cLHTailCleanArray[i] =  theCLHtailCalculator.nasaBlackwellCompleteCurveValue(alphaActualHtail);
		}

		System.out.println("CL horizontal tail Clean Array " + Arrays.toString(cLWingCleanArray) );
		cLAlphaHTailClean = theLSHTailAnalysis.getcLLinearSlopeNB();
		alphaStarHtail = theLSHTailAnalysis.get_alphaStar().to(NonSI.DEGREE_ANGLE).getEstimatedValue();


		//TAU (-5 --> 30)

		System.out.println("\n-----START OF TAU CALCULATION-----\n" );
		System.out.println("Delta_e vector --> " + Arrays.toString(deltaEArray));

		Amount<Angle> deflection;
		double chordRatio = theHTail.get_CeCt();
		for(int i=0; i<deltaEArray.length; i++){

			deflection = Amount.valueOf((deltaEArray[i]), NonSI.DEGREE_ANGLE);
			tauIndexArray[i] = theStabilityCalculator.calculateTauIndex(chordRatio, aircraft, deflection);
		}



		int nValueHtail = 20;
		double[] cLHTailArrayTemp = new double[nValueHtail];

		StabilityCalculator.CalcCLHTail theCLTauCalculator = theStabilityCalculator.new CalcCLHTail();
		for (int i=0; i<deltaEArray.length; i++){
			System.out.println(" Tau Value " + deltaEArray[deltaEArray.length-i-1]);

			cLHTailArrayTemp = theCLTauCalculator.cLHtailWithElevatorDeflection(theHTail, theOperatingConditions, 
					deltaEArray[deltaEArray.length-i-1], tauIndexArray[ tauIndexArray.length-i-1],
					cLHTailCleanArray, alphaActualHTailArray );
			cLHTailMap.put(deltaEArrayString[ tauIndexArray.length-i-1], cLHTailArrayTemp);
			alphaHTailMap.put(deltaEArrayString[ tauIndexArray.length-i-1], theCLTauCalculator.getAlphaArrayWithTau());
			System.out.println(" cl h tail " + Arrays.toString(cLHTailArrayTemp));
		}



		//PLOT 
		if (plotCheck ==  true){

			// Stall Path Plot

			LSAerodynamicsManager.CalcCLMaxClean theCLmaxHTailAnalysis = theLSHTailAnalysis.new CalcCLMaxClean(); //is nested
			LSAerodynamicsManager.CalcCLvsAlphaCurve theCLHTailAnalysis = theLSHTailAnalysis.new CalcCLvsAlphaCurve();

			Amount<Angle >alphaHTailAtCLMax = theLSHTailAnalysis.get_alphaStall();

			System.out.println("\n \n \t \t WRITING CHART TO FILE. Stall path. ");
			System.out.println("-----------------------------------------------------");

			// interpolation of CL MAX_airfoil
			MyArray clMaxAirfoilHtail = theCLmaxHTailAnalysis.getClAirfoils();

			double alphaSecond = theLSAnalysis.getAlphaArray().get(3);
			double alphaThird = theLSAnalysis.getAlphaArray().get(6);

			MyArray clAlphaStall = theLSHTailAnalysis.getcLMap()
					.getCxyVsAlphaTable()
					.get(MethodEnum.NASA_BLACKWELL ,alphaHTailAtCLMax);
			MyArray clSecond = theLSHTailAnalysis.getcLMap()
					.getCxyVsAlphaTable()
					.get(MethodEnum.NASA_BLACKWELL ,Amount.valueOf(alphaSecond, SI.RADIAN));
			MyArray clThird = theLSHTailAnalysis.getcLMap()
					.getCxyVsAlphaTable()
					.get(MethodEnum.NASA_BLACKWELL ,Amount.valueOf(alphaThird, SI.RADIAN));

			double [][] semiSpanAdHTail = {
					theLSHTailAnalysis.get_yStationsND(), theLSHTailAnalysis.get_yStationsND(),
					theLSHTailAnalysis.get_yStationsND(), theLSHTailAnalysis.get_yStationsND()};

			double [][] clDistributionHTail = {
					clMaxAirfoilHtail.getRealVector().toArray(),
					clSecond.getRealVector().toArray(),
					clThird.getRealVector().toArray(),
					clAlphaStall.getRealVector().toArray()};

			String [] legendHtail = new String [4];
			legendHtail[0] = "CL max airfoil";
			legendHtail[1] = "CL distribution at alpha " 
					+ Math.toDegrees( alphaSecond);
			legendHtail[2] = "CL distribution at alpha " 
					+ Math.toDegrees( alphaThird);
			legendHtail[3] = "CL distribution at alpha " 
					+ Math.toDegrees( alphaHTailAtCLMax.getEstimatedValue());


			MyChartToFileUtils.plot(
					semiSpanAdHTail, clDistributionHTail, // array to plot
					0.0, 1.0, 0.0, null,					    // axis with limits
					"eta", "CL", "", "",	    // label with unit
					legendHtail,					// legend
					subfolderPath, "Stall Path of Horizontal Tail ");			    // output informations

			System.out.println("-----------------------------------------------------");
			System.out.println("\t \t DONE ");



			//CL VS Alpha clean

			System.out.println("\n-------------------------------------");
			System.out.println("\t \t \tWRITING CL vs ALPHA CHART TO FILE  ");
			MyChartToFileUtils.plotNoLegend(
					alphaActualHTailArray,cLHTailCleanArray, 
					null, null , null , null ,					    // axis with limits
					"alpha_Wing", "CL", "deg", "",	   				
					subfolderPath, "CL vs Alpha Horizontal tail Clean" );
			System.out.println("\t \t \tDONE  ");



			// CL vs alpha with delta e deflection

			List<Double[]> cLListPlot = new ArrayList<Double[]>();
			List<Double[]> alphaListPlot = new ArrayList<Double[]>();
			List<String> legendStall  = new ArrayList<>();

			Double [] clArray;
			Double [] alphaArray;

			for( int j=0; j< deltaEArray.length; j++){
				String key = deltaEArrayString[j];
				clArray = new Double [cLHTailMap.get(key).length];
				alphaArray = new Double [cLHTailMap.get(key).length];
				for(int i=0; i<cLHTailMap.get(key).length; i++){
					clArray[i] = (Double) cLHTailMap.get(key)[i];
					alphaArray[i] = (Double) alphaHTailMap.get(key)[i];	
				}
				cLListPlot.add(clArray);
				alphaListPlot.add(alphaArray);
				legendStall.add(key);
			}


			MyChartToFileUtils.plotJFreeChart(alphaListPlot,
					cLListPlot,
					"CL vs alpha",
					"alpha",
					"CL",
					null, null, 0.0 ,null,
					"deg",
					"",
					true,
					legendStall,
					subfolderPath,
					"CL alpha Horizontal Tail with Elevator");

			System.out.println("\n\n\t\t\tWRITING CL vs ALPHA CHART TO FILE FOR horizontal  tail with elevator deflection");

		}


	}


	public void CalculateCompleteAircraftLiftCharacteristics(){
		System.out.println("\n-----Complete Aircraft-----\n" );

		if (alphaCheck == true){
			double cLWingBodyActual;
			double cLHtailActual;
			System.out.println(" alpha body " + alphaBody.to(NonSI.DEGREE_ANGLE));
			for (int i = 0; i<deltaEArray.length; i++){
				System.out.println(" elevator deflection " + deltaEArray[i]);

				cLWingBodyActual = MyMathUtils.getInterpolatedValue1DLinear(alphaStabilityArray.toArray(),
						cLWingBodyArray, alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue());

				System.out.println(" cL wing body " +  cLWingBodyActual);

				double[] cLHtailarray =  cLHTailMap.get(deltaEArrayString[i]);
				double alphaAngle = alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()- theDownwashCalculator.getDownwashAtAlphaBody(alphaBody)
						+ theHTail.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue();

				cLHtailActual = MyMathUtils.getInterpolatedValue1DLinear(alphaHTailMap.get(deltaEArrayString[i]),
						cLHtailarray,
						alphaAngle);


				System.out.println(" cL horizontal tail  " +  cLHtailActual);


				cLCompleteAircraftdeltaEArray [i] =  cLWingBodyActual + cLHtailActual;

				System.out.println("\n the CL of aircraft at alpha body =(deg)" +
						alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue() +
						" for delta = (deg) "
						+ deltaEArray[i]
								+ " is " + cLCompleteAircraftdeltaEArray [i]);
			}
		}

		cLCompleteAircraft = new double [alphaStabilityArray.size()];
		for (int i=0; i<alphaStabilityArray.size(); i++){

			cLCompleteAircraft[i] = cLWingBodyArray[i] + cLHTailCleanArray[i];
		}
	}




	public void  CalculateWingDragCharacteristics(){
		System.out.println("\n ------------------- ");
		System.out.println("|       WING        |");
		System.out.println(" ------------------- \n\n");


		//-----------------------------------------------CDairfoil+ CL*alpha_i
		// Array

		// parasite Drag

		LSAerodynamicsManager.CalcCdvsAlpha theCDWingArrayCalculator = theLSAnalysis
				.new CalcCdvsAlpha();

		LSAerodynamicsManager.CalcCDAtAlpha theCDWingCalculator = theLSAnalysis
				.new CalcCDAtAlpha();

		parasiteCDWingCleanArray = new double [nValueAlpha];

		Double [] cDWingTemp =  theCDWingArrayCalculator.calculateCDParasiteFromAirfoil(alphaMinWing, alphaMaxWing, nValueAlpha);
		for (int i=0; i< parasiteCDWingCleanArray.length; i++){
			parasiteCDWingCleanArray[i] =(double)cDWingTemp[i];
		}

		// Induced Drag 

		inducedCDWingArray = theCDWingArrayCalculator.calculateCDInduced(alphaMinWing, alphaMaxWing, nValueAlpha);

		//		System.out.println(" cd induced");
		//		for(int i=0 ; i<inducedCDWingArray.length; i++){
		//			System.out.println(inducedCDWingArray[i]);
		//		}

		//		System.out.println(" cl array ");
		//		for(int i =0; i<inducedCDWingArray.length; i++){
		//		System.out.println(cLWingCleanArray[i]);
		//		}

		//		System.out.println(" alphaArray ");
		//		for(int i =0; i<inducedCDWingArray.length; i++){
		//		System.out.println(alphaWingStabilityArray.get(i));
		//		}

		// Total drag

		cDWingArray =  new double [nValueAlpha];
		cDWingCleanArray = new double [nValueAlpha];
		for (int i = 0; i<nValueAlpha; i++){
			cDWingCleanArray[i] = parasiteCDWingCleanArray[i]+ inducedCDWingArray[i];
		}

		cDWingArray = cDWingCleanArray;

		if (theCondition == ConditionEnum.TAKE_OFF || theCondition == ConditionEnum.LANDING){

			double deltaCD  = highLiftWingCalculator.getDeltaCD();
			cDWingTakeOffArray = new double [cDWingCleanArray.length];
			cDWingLandingArray = new double [cDWingCleanArray.length];
			for (int i = 0; i<nValueAlpha; i++){

				if ( theCondition == ConditionEnum.TAKE_OFF ){

					cDWingTakeOffArray[i] =  cDWingCleanArray[i] + deltaCD;
					cDWingArray [i] = cDWingTakeOffArray[i];
				}


				if (theCondition == ConditionEnum.LANDING){

					cDWingLandingArray[i] =   cDWingCleanArray[i] + deltaCD;
					cDWingArray [i] = cDWingLandingArray[i];
				}
			}
		}
		//-----------------------------------------------CD0+ CL^"/PI AR e

		// Total Drag with Parabolic interpolation

		if (theCondition == ConditionEnum.CRUISE){
			theWing.calculateFormFactor(theWing.getAerodynamics().calculateCompressibility(theOperatingConditions.get_machCurrent()));
			double cD0WingPolar= theWing.getAerodynamics().calculateCd0Parasite();




			cD0WingPolarArray = new double [alphaStabilityArray.size()];
			cDiWingPolarArray = new double [alphaStabilityArray.size()];
			cDWaweWingPolarArray = new double [alphaStabilityArray.size()];
			cDWingPolarArray = new double [alphaStabilityArray.size()];

			for (int i=0; i<alphaStabilityArray.size(); i++){

				double cLLocal = theCLWingCalculator.nasaBlackwellAlphaBody(Amount.valueOf(Math.toRadians (alphaStabilityArray.get(i)), SI.RADIAN));	
				cD0WingPolarArray[i] = cD0WingPolar;
				cDiWingPolarArray[i] = (Math.pow(cLLocal, 2))/(Math.PI * theWing.get_aspectRatio())*(1+theWing.getDeltaFactorDrag());
				cDWaweWingPolarArray[i] = theWing.getAerodynamics().getCalculateCdWaveDrag().lockKorn(cLLocal, theOperatingConditions.get_machCurrent());

				cDWingPolarArray[i] = cD0WingPolar + cDiWingPolarArray[i] + cDWaweWingPolarArray[i];
			}}

		// value

		if(alphaCheck ==true){

			double cDIsolatedWing = theCDWingCalculator.integralFromCdAirfoil(
					alphaWing, MethodEnum.NASA_BLACKWELL, theLSAnalysis);
			System.out.println(" CD of Wing at alpha body = "
					+ alphaBody.to(NonSI.DEGREE_ANGLE)
					+ " is " + cDIsolatedWing);

		}

		// plot

		if( plotCheck == true){

			System.out.println("\n-------------------------------------");
			System.out.println("\t \t \tWRITING CD vs ALPHA CHART TO FILE  ");

			if (theCondition == ConditionEnum.TAKE_OFF ){

				double [][] theCDWingMatrix = {cDWingCleanArray,cDWingTakeOffArray };
				double [][] theAlphaCDMatrix = { alphaWingStabilityArray.toArray(), alphaWingStabilityArray.toArray()};
				String [] legend = {"Wing clean Drag coefficient", " Wing Drag coefficient with high lift devices "};

				MyChartToFileUtils.plot(
						theAlphaCDMatrix, theCDWingMatrix, 
						null, null, null, null,
						"alpha_w", "CD",
						"deg", "",
						legend,
						subfolderPath, "Total Drag coefficient vs Alpha Wing for WING ");}

			if (theCondition == ConditionEnum.LANDING){
				double [][] theCDWingMatrix = {cDWingCleanArray,cDWingLandingArray};
				double [][] theAlphaCDMatrix = { alphaWingStabilityArray.toArray(), alphaWingStabilityArray.toArray()};
				String [] legend = {"Wing clean Drag coefficient", " Wing Drag coefficient with high lift devices "};

				MyChartToFileUtils.plot(
						theAlphaCDMatrix, theCDWingMatrix, 
						null, null, null, null,
						"alpha_w", "CD",
						"deg", "",
						legend,
						subfolderPath, "Total Drag coefficient vs Alpha Wing for WING ");
			}

			if (theCondition == ConditionEnum.CRUISE){
				MyChartToFileUtils.plotNoLegend(
						alphaWingStabilityArray.toArray(),parasiteCDWingCleanArray, 
						null, null, null, null,
						"alpha_w", "CD_parasite",
						"deg", "",
						subfolderPath, "Parasite Drag coefficient vs Alpha Wing for WING ");

				System.out.println("\n\n\t\t\tDONE");


				System.out.println("\n-------------------------------------");
				System.out.println("\t \t \tWRITING PARASITE CD vs CL CHART TO FILE  ");

				MyChartToFileUtils.plotNoLegend(
						parasiteCDWingCleanArray, cLWingCleanArray,
						null, null, null, null,
						"CD_parasite", "CL" ,
						"", "",
						subfolderPath, "Parasite Drag coefficient vs CL for WING ");

				System.out.println("\n\n\t\t\tDONE");

				System.out.println("\n-------------------------------------");
				System.out.println("\t \t \tWRITING INDUCED CD vs CL CHART TO FILE  ");

				MyChartToFileUtils.plotNoLegend(
						alphaWingStabilityArray.toArray(),inducedCDWingArray, 
						null, null, null, null,
						"alpha_w", "CD_induced",
						"deg", "",
						subfolderPath, "Induced Drag coefficient vs Alpha Wing for WING ");
				System.out.println("\n\n\t\t\tDONE");

				System.out.println("\n-------------------------------------");
				System.out.println("\t \t \tWRITING TOTAL CD vs CL CHART TO FILE  ");

				double [][] theCDWingMatrix = {parasiteCDWingCleanArray, inducedCDWingArray, cDWingCleanArray};
				double [][] theAlphaCDMatrix = { alphaWingStabilityArray.toArray(), alphaWingStabilityArray.toArray(), alphaWingStabilityArray.toArray()};
				String [] legend = {"parasite Drag Cefficient", "induced Drag Coefficient", "Total Drag Coefficient"};

				MyChartToFileUtils.plot(
						theAlphaCDMatrix, theCDWingMatrix, 
						null, null, null, null,
						"alpha_w", "CD",
						"deg", "",
						legend,
						subfolderPath, "Total Drag coefficient vs Alpha Wing for WING ");

				System.out.println(" \n\n\t\tDONE");


				if (theCondition == ConditionEnum.CRUISE){
					System.out.println("\n-------------------------------------");
					System.out.println("\t \t \tWRITING TOTAL CD POLAR vs CL CHART TO FILE  ");



					double [][] theCDPolarWingMatrix = {cD0WingPolarArray, cDiWingPolarArray, cDWaweWingPolarArray, cDWingPolarArray};
					double [][] theAlphaPolarCDMatrix = { alphaWingStabilityArray.toArray(), 
							alphaWingStabilityArray.toArray(), alphaWingStabilityArray.toArray(), alphaWingStabilityArray.toArray()};

					String [] legendPolar = {"CD0", "CDi", "CDw", "CD"};

					MyChartToFileUtils.plot(
							theAlphaPolarCDMatrix, theCDPolarWingMatrix, 
							null, null, null, null,
							"alpha_w", "CD",
							"deg", "",
							legendPolar,
							subfolderPath, "Drag coefficient contributes vs Alpha Wing for WING ");


					double [][] theCDMatrix = {cDWingCleanArray, cDWingPolarArray};
					double [][] theAlphaMatrix = { alphaWingStabilityArray.toArray(), 
							alphaWingStabilityArray.toArray()};

					String [] legendCD = {"from airfoils", "Polar method"};

					MyChartToFileUtils.plot(
							theAlphaMatrix, theCDMatrix, 
							null, null, null, null,
							"alpha_w", "CD",
							"deg", "",
							legendCD,
							subfolderPath, "Comparison of CD estimation");
				}
			}


		}

	}




	public void  CalculateHTailDragCHaracteristics(){

		System.out.println("\n ------------------- ");
		System.out.println("|  HORIZONTAL TAIL   |");
		System.out.println(" ------------------- \n\n");



		// Array

		LSAerodynamicsManager.CalcCdvsAlpha theCDHtailArrayCalculator = theLSHTailAnalysis
				.new CalcCdvsAlpha();

		LSAerodynamicsManager.CalcCDAtAlpha theCDhTAILCalculator = theLSHTailAnalysis
				.new CalcCDAtAlpha();

		parasiteCDHTailCleanArray= new double [nValueAlpha];

		Double [] cDWingTemp =  theCDHtailArrayCalculator.calculateCDParasiteFromAirfoil(alphaMin,alphaMax, nValueAlpha);
		for (int i=0; i< parasiteCDWingCleanArray.length; i++){
			parasiteCDHTailCleanArray[i] =(double)cDWingTemp[i];
		}

		// Induced Drag 
		inducedCDHTailArray = new double [nValueAlpha];

		inducedCDHTailArray = theCDHtailArrayCalculator.calculateCDInduced(alphaMin,alphaMax, nValueAlpha);

		// Total drag

		cDHTailCleanArray = new double [nValueAlpha];
		for (int i = 0; i<nValueAlpha; i++){
			cDHTailCleanArray[i] = parasiteCDHTailCleanArray[i]+inducedCDHTailArray[i];
		}


		// tau Map


		double [] cDHtailwithTau = new double [alphaStabilityArray.size()];

		List<FlapTypeEnum> flapTypeHtail = new ArrayList<FlapTypeEnum>();
		List<Double> etaInFlapHtail = new ArrayList<Double>();
		List<Double> etaOutFlapHtail = new ArrayList<Double>();

		List<Double> cfcHtail = new ArrayList<Double>();
		flapTypeHtail.add(FlapTypeEnum.PLAIN);
		Double [] deltaArray = new Double [1];
		double [] cdTemp = new double [nValueAlpha];
		etaInFlapHtail.add(
				theHTail.get_etaIn());

		etaOutFlapHtail.add(
				theHTail.get_etaOut());

		cfcHtail.add(theHTail.get_CeCt());

		for (int i = 0; i<tauIndexArray.length; i++){
			List<Double[]> deltaFlapHTail = new ArrayList<Double[]>();

			if ( deltaEArray[i] > 0)
				deltaArray[0] = deltaEArray[i];
			else
				deltaArray[0] = -deltaEArray[i];
			deltaFlapHTail.add(0,deltaArray);

			theHighLiftTailalculator = theLSHTailAnalysis
					.new CalcHighLiftDevices(
							theHTail,
							theOperatingConditions,
							deltaFlapHTail,
							flapTypeHtail,
							null,
							etaInFlapHtail,
							etaOutFlapHtail,
							null,
							null,
							cfcHtail,
							null,
							null,
							null
							);
			theHighLiftTailalculator.calculateHighLiftDevicesEffects();
			double delta = theHighLiftTailalculator.getDeltaCD();
			for (int j=0; j<nValueAlpha; j++){
				cdTemp[j] = cDHTailCleanArray[j] + delta;}
			cDHTailMap.put(deltaEArrayString[i], cdTemp);
			alphacDHTailMap.put(deltaEArrayString[i],alphaStabilityArray.toArray()); // these are alpha wing
			cdTemp =  new double [nValueAlpha];
		}
		// value

		if(alphaCheck == true){
			LSAerodynamicsManager.CalcCDAtAlpha theCDHTailCalculator = theLSHTailAnalysis.new CalcCDAtAlpha();
			double cDHorizontalTail = theCDHTailCalculator.integralFromCdAirfoil(
					alphaHorizontalTail, MethodEnum.NASA_BLACKWELL, theLSAnalysis);
			System.out.println("\n CD of Horizontal Tail with no defection at alpha body = (deg) "
					+ alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()
					+ " is " + cDHorizontalTail
					);
		}

		// plot

		if (plotCheck = true){

			System.out.println("\n-------------------------------------");
			System.out.println("\t \t \tWRITING Total CD vs CL CHART TO FILE  ");


			MyChartToFileUtils.plotNoLegend(
					alphaStabilityArray.toArray(),cDHTailCleanArray, 
					null, null, null, null,
					"alpha_h", "CD",
					"deg", "",
					subfolderPath, "Total Drag coefficient vs Alpha for horizontal TAIL, clean ");


			double [][] alphaMatrix = {alphaStabilityArray.toArray(), alphaStabilityArray.toArray(), 
					alphaStabilityArray.toArray(), alphaStabilityArray.toArray(),
					alphaStabilityArray.toArray(),alphaStabilityArray.toArray(),
					alphaStabilityArray.toArray()};
			double [][] cDTauArray = {cDHTailMap.get(deltaEArrayString[0]),cDHTailMap.get(deltaEArrayString[1]),
					cDHTailMap.get(deltaEArrayString[2]), cDHTailMap.get(deltaEArrayString[3]),
					cDHTailMap.get(deltaEArrayString[4]), cDHTailMap.get(deltaEArrayString[5]),
					cDHTailMap.get(deltaEArrayString[6]) };

			String [] legendCD = new String[deltaArray.length];
			for ( int i=0; i<deltaArray.length; i++){
				legendCD[i] = deltaEArrayString[i];
			}

			MyChartToFileUtils.plot(
					alphaMatrix,cDTauArray, 
					null, null, null, null,
					"alpha_h", "CD",
					"deg", "",
					deltaEArrayString,
					subfolderPath, "Total Drag coefficient vs Alpha for horizontal TAIL with deltae deflection");


			System.out.println("\n\n\t\t\tDONE");
		}


	}

	public void calculateCompleteAircraftDragCharacteristics(){

		double cD0 = aircraft.get_theAerodynamics().get_cD0();
		double oswaldFactor = aircraft.get_theAerodynamics().get_oswald();

		cDCompleteAircraft = new double [alphaStabilityArray.size()];
		for (int i=0; i<alphaStabilityArray.size(); i++){
			cDCompleteAircraft [i]= cD0 + (Math.pow(cLCompleteAircraft[i],2))/(Math.PI* aircraft.get_wing().get_aspectRatio() * oswaldFactor);
		}
	}

	public void calculateWingMomentCharacteristics(){
		System.out.println("\n ------------------- ");
		System.out.println("|       WING        |");
		System.out.println(" ------------------- \n\n");

		System.out.println("\n\tData:");
		System.out.println(" xLE_MAC wing is " + theWing.get_xLEMacActualLRF().getEstimatedValue() + " m" );
		System.out.println(" MAC wing is " +  theWing.get_meanAerodChordActual().getEstimatedValue() + " m ");
		System.out.println(" xAC wing is " + theLSAnalysis.getCalculateXAC().deYoungHarper() + " m ");
		System.out.println(" xAC DeYoung Harper perc. MAC " + theLSAnalysis.getCalculateXAC().deYoungHarper()/
				theWing.get_meanAerodChordActual().getEstimatedValue());


		StabilityCalculator.CalcPitchingMomentAC pitchingMomentCalculatorWing = 
				theStabilityCalculator.new CalcPitchingMomentAC(theWing, theOperatingConditions);


		// Array

		// At Quarter of MAC 

		cmQuarterChordWingArray = new double[nValueAlpha];
		Amount<Angle> alphaWingActual;

		for ( int i=0; i< nValueAlpha; i++){
			alphaWingActual = Amount.valueOf(
					alphaStabilityArray.get(i)+theWing.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue(),
					NonSI.DEGREE_ANGLE);
			cmQuarterChordWingArray[i] = pitchingMomentCalculatorWing.calculateCMQuarterMACIntegral(alphaWingActual);
		}

		if (theCondition == ConditionEnum.TAKE_OFF || theCondition == ConditionEnum.LANDING){
			for(int i=0; i<nValueAlpha; i++){
				cmQuarterChordWingArray [i] = cmQuarterChordWingArray[i] + highLiftWingCalculator.getDeltaCM_c4();
			}
		}


		//aerodynamic center
		aCWing = pitchingMomentCalculatorWing.getACLiftingSurface();
		System.out.println("AC WING percent MAC is " + aCWing);
		//	
		//		aCWing = 0.721;
		cmACWingArray = new double[nValueAlpha];
		for ( int i=0; i< nValueAlpha; i++){
			alphaWingActual = Amount.valueOf(
					alphaStabilityArray.get(i)+theWing.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue(),
					NonSI.DEGREE_ANGLE);
			cmACWingArray[i] = pitchingMomentCalculatorWing.calculateCMIntegral(alphaWingActual, aCWing);
		}

		if (theCondition == ConditionEnum.TAKE_OFF || theCondition == ConditionEnum.LANDING){
			for(int i=0; i<nValueAlpha; i++){
				cmACWingArray [i] = cmACWingArray[i] + highLiftWingCalculator.getDeltaCM_c4();
			}
		}
		xACWingLRF = aCWing * theWing.get_meanAerodChordActual().getEstimatedValue() + theWing.get_xLEMacActualLRF().getEstimatedValue();
		xACWingBRF=aCWing * theWing.get_meanAerodChordActual().getEstimatedValue() + theWing.get_xLEMacActualBRF().getEstimatedValue();

		// Plot

		if (plotCheck == true){
			System.out.println("\n\n\t\t\tWRITING CM vs ALPHA CHART TO FILE FOR wing at c/4");
			MyChartToFileUtils.plotNoLegend(
					alphaStabilityArray.toArray() ,cmQuarterChordWingArray,
					null, null, null, null,
					"alpha_b", "CM_w",
					"deg", "",
					subfolderPath," Moment Coefficient vs alpha for Wing respect to quarter of MAC " );
			System.out.println("\n\n\t\t\tDONE");

			System.out.println("\n\n\t\t\tWRITING CM vs ALPHA CHART TO FILE FOR wing at aerodynamic Center");
			MyChartToFileUtils.plotNoLegend(
					alphaStabilityArray.toArray() ,cmACWingArray,
					null, null, null, null,
					"alpha_b", "CM_w",
					"deg", "",
					subfolderPath," Moment Coefficient vs alpha for Wing respect to A C " );
			System.out.println("\n\n\t\t\tDONE");

		}
	}





	public void calculateHTailMomentCharacteristics(){

		System.out.println("\n ------------------- ");
		System.out.println("|  HORIZONTAL TAIL   |");
		System.out.println(" ------------------- \n\n");

		System.out.println("\n\tData:");
		System.out.println(" xLE_MAC h tail is " + theHTail.get_xLEMacActualLRF().getEstimatedValue() + " m" );
		System.out.println(" MAC h tail is " +   theHTail.get_meanAerodChordActual().getEstimatedValue() + " m ");
		System.out.println(" xAC h tail is " + theLSHTailAnalysis.getCalculateXAC().deYoungHarper() + " m ");
		System.out.println(" xAC DeYoung Harper perc. MAC " + theLSHTailAnalysis.getCalculateXAC().deYoungHarper()/
				theHTail.get_meanAerodChordActual().getEstimatedValue());


		StabilityCalculator.CalcPitchingMomentAC pitchingMomentCalculatorHTail = 
				theStabilityCalculator.new CalcPitchingMomentAC(theHTail, theOperatingConditions);


		// Array

		// At Quarter of MAC 

		cmQuarterChordHtailArray = new double[nValueAlpha];
		Amount<Angle> alphaHTailActual;

		for ( int i=0; i< nValueAlpha; i++){
			alphaHTailActual = Amount.valueOf(
					alphaStabilityArray.get(i)- downwashAnglesArray[i] +theHTail.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue(),
					NonSI.DEGREE_ANGLE);
			cmQuarterChordHtailArray[i] = pitchingMomentCalculatorHTail.calculateCMQuarterMACIntegral(alphaHTailActual);
		}



		//aerodynamic center
		aCHTail = pitchingMomentCalculatorHTail.getACLiftingSurface();
		xACHTailBRF = aCHTail * theHTail.get_meanAerodChordActual().getEstimatedValue() + theHTail.get_xLEMacActualBRF().getEstimatedValue();
		System.out.println("AC H TAIL percent MAC is " + aCHTail);
		System.out.println(" AC H TAIL BRF is "+ xACHTailBRF);
		zACHtailBRF = theHTail.get_zCG().getEstimatedValue();


		cmACHtailArray = new double[nValueAlpha];
		for ( int i=0; i< nValueAlpha; i++){
			alphaHTailActual = Amount.valueOf(
					alphaStabilityArray.get(i)- downwashAnglesArray[i] +theHTail.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue(),
					NonSI.DEGREE_ANGLE);
			cmACHtailArray[i] = pitchingMomentCalculatorHTail.calculateCMIntegral(alphaHTailActual, aCHTail);
		}

		// Elevator Deflection
		// tau Map


		double [] cDHtailwithTau = new double [alphaStabilityArray.size()];

		List<FlapTypeEnum> flapTypeHtail = new ArrayList<FlapTypeEnum>();
		List<Double> etaInFlapHtail = new ArrayList<Double>();
		List<Double> etaOutFlapHtail = new ArrayList<Double>();

		List<Double> cfcHtail = new ArrayList<Double>();
		flapTypeHtail.add(FlapTypeEnum.PLAIN);
		Double [] deltaArray = new Double [1];
		double [] cdTemp = new double [nValueAlpha];
		etaInFlapHtail.add(
				theHTail.get_etaIn());

		etaOutFlapHtail.add(
				theHTail.get_etaOut());

		cfcHtail.add(theHTail.get_CeCt());

		for (int i = 0; i<tauIndexArray.length; i++){
			List<Double[]> deltaFlapHTail = new ArrayList<Double[]>();

			if ( deltaEArray[i] > 0)
				deltaArray[0] = deltaEArray[i];
			else
				deltaArray[0] = -deltaEArray[i];
			deltaFlapHTail.add(0,deltaArray);

			theHighLiftTailalculator = theLSHTailAnalysis
					.new CalcHighLiftDevices(
							theHTail,
							theOperatingConditions,
							deltaFlapHTail,
							flapTypeHtail,
							null,
							etaInFlapHtail,
							etaOutFlapHtail,
							null,
							null,
							cfcHtail,
							null,
							null,
							null
							);
			theHighLiftTailalculator.calculateHighLiftDevicesEffects();

			double [] cmTemp = new double [nValueAlpha];
			double delta = theHighLiftTailalculator.getDeltaCM_c4();
			if(deltaEArray[i] < 0){
				delta= - delta;
			}
			for (int j=0; j<nValueAlpha; j++){
				cmTemp[j] = cmACHtailArray[j] + delta;}
			cMHTailMap.put(deltaEArrayString[i], cmTemp);
			alphacMHTailMap.put(deltaEArrayString[i],alphaStabilityArray.toArray()); // these are alpha wing
			cdTemp =  new double [nValueAlpha];}


		// Plot

		if (plotCheck == true){
			System.out.println("\n\n\t\t\tWRITING CM vs ALPHA CHART TO FILE FOR horizontal tail at c/4");
			MyChartToFileUtils.plotNoLegend(
					alphaStabilityArray.toArray() ,cmQuarterChordHtailArray,
					null, null, null, null,
					"alpha_b", "CM_h",
					"deg", "",
					subfolderPath," Moment Coefficient vs alpha for Horizontal Tail respect to quarter of MAC " );
			System.out.println("\n\n\t\t\tDONE");

			System.out.println("\n\n\t\t\tWRITING CM vs ALPHA CHART TO FILE FOR ht at aerodynamic Center");
			MyChartToFileUtils.plotNoLegend(
					alphaStabilityArray.toArray() ,cmACHtailArray,
					null, null, -0.01, 0.01,
					"alpha_b", "CM_w",
					"deg", "",
					subfolderPath," Moment Coefficient vs alpha for horizontal tail respect to A C " );
			System.out.println("\n\n\t\t\tDONE");


			System.out.println("\n-------------------------------------");
			System.out.println("\t \t \tWRITING CM CHART TO FILE  ");

			double [][] alphaMatrix = {alphaStabilityArray.toArray(), alphaStabilityArray.toArray(), 
					alphaStabilityArray.toArray(), alphaStabilityArray.toArray(),
					alphaStabilityArray.toArray(),alphaStabilityArray.toArray(),
					alphaStabilityArray.toArray()};
			double [][] cMTauArray = {cMHTailMap.get(deltaEArrayString[0]),cMHTailMap.get(deltaEArrayString[1]),
					cMHTailMap.get(deltaEArrayString[2]), cMHTailMap.get(deltaEArrayString[3]),
					cMHTailMap.get(deltaEArrayString[4]), cMHTailMap.get(deltaEArrayString[5]),
					cMHTailMap.get(deltaEArrayString[6]) };

			String [] legendCM = new String[deltaArray.length];
			for ( int i=0; i<deltaArray.length; i++){
				legendCM[i] = deltaEArrayString[i];
			}

			MyChartToFileUtils.plot(
					alphaMatrix,cMTauArray, 
					null, null, null, null,
					"alpha_h", "CM",
					"deg", "",
					deltaEArrayString,
					subfolderPath, "Total Moment coefficient vs Alpha for horizontal TAIL with deltae deflection");


			System.out.println("\n\n\t\t\tDONE");

		}


	}
	public void calculateFuselageMomentCharacteristics(){

		System.out.println("\n ------------------- ");
		System.out.println("|      FUSELAGE      |");
		System.out.println(" ------------------- \n\n");

		//UNINA METHOD

		double cM0Fuselage = -MomentCalc.calcCM0Fuselage(
				fusDesDatabaseReader.getCM0FR(),
				fusDesDatabaseReader.getdCMn(),
				fusDesDatabaseReader.getdCMt())* 
				fusSurfRatio*aircraft.get_fuselage()
				.get__diam_C()
				.doubleValue(SI.METER)/
				aircraft
				.get_wing()
				.get_meanAerodChordActual()
				.doubleValue(SI.METRE);



		double cMaFuselage = MomentCalc.calcCMAlphaFuselage(
				fusDesDatabaseReader.getCMaFR(),
				fusDesDatabaseReader.getdCMan(),
				fusDesDatabaseReader.getdCMat())* 
				fusSurfRatio*aircraft
				.get_fuselage()
				.get__diam_C()
				.doubleValue(SI.METER)/
				aircraft
				.get_wing().get_meanAerodChordActual()
				.doubleValue(SI.METRE);

		//		System.out.println(" CM0 FR = "+ fusDesDatabaseReader.getCM0FR());
		//		System.out.println(" dCMn = "+ fusDesDatabaseReader.getdCMn());
		//		System.out.println(" dCMt = "+ fusDesDatabaseReader.getdCMt());


		// TODO fix the UNINA method!

		cM0Fuselage = -0.0361;
		cMaFuselage = 0.0222;

		System.out.println(" CM0 fuselage = "+ cM0Fuselage);
		System.out.println(" CMalpha fuselage  = (1/deg) "+ cMaFuselage);


		cm0LiftFuselage = cMaFuselage * theWing.getAerodynamics().getAlphaZeroLiftWingClean() + cM0Fuselage;

		System.out.println(" CM0l fuselage = " + cm0LiftFuselage);



		// PLOT

		if( plotCheck == true){
			cmFuselageArray = new double [alphaStabilityArray.size()];

			for (int i=0 ; i<cmFuselageArray.length ; i++)
				cmFuselageArray[i] = cMaFuselage * alphaStabilityArray.get(i) + cM0Fuselage;

			System.out.println("\n\n\t\t\tWRITING CM vs ALPHA CHART TO FILE FOR fuselage");
			MyChartToFileUtils.plotNoLegend(
					alphaStabilityArray.toArray() , cmFuselageArray,
					null, null, null, null,
					"alpha_body", "CM_f",
					"deg", "",
					subfolderPath," Moment Coefficient vs alpha body for fuselage" );
		}

		// delta xac
		clAlphaWing = theLSAnalysis.getcLLinearSlopeNB();


		double deltaXACFuselage = theStabilityCalculator.calcDeltaXACFuselage(cMaFuselage, clAlphaWing);
		System.out.println(" Delta XAC due to fuselage (% chord )= " + deltaXACFuselage);


		xACWingBody = aCWing + deltaXACFuselage;

		xACWingBodyLRF = xACWingBody * theWing.get_meanAerodChordActual().getEstimatedValue() + theWing.get_xLEMacActualLRF().getEstimatedValue();
		xACWingBodyBRF=xACWingBody * theWing.get_meanAerodChordActual().getEstimatedValue() + theWing.get_xLEMacActualBRF().getEstimatedValue();

		double posTemp = (aCWing * theWing.get_meanAerodChordActual().getEstimatedValue()) + theWing.get_xLEMacActualLRF().getEstimatedValue();
		System.out.println(" XAC wing (LRF) = " + posTemp + " m" );
		System.out.println(" XAC wing body (LRF) = " + xACWingBodyLRF + " m" );
		System.out.println(" XAC wing body (BRF) = " + xACWingBodyBRF + " m" );



		cMacWingBody = new double[alphaStabilityArray.size()];
		for (int i=0 ; i< alphaStabilityArray.size(); i++){		
			cMacWingBody[i] = cmACWingArray[i] + cm0LiftFuselage; // use this 
		}

		System.out.println("\n\n cm ac wing-body  = " +  Arrays.toString(cMacWingBody) );
	}
	public void calculatePowerEffects(){

		System.out.println("\n ------------------- ");
		System.out.println("|    POWER EFFECTS    |");
		System.out.println(" ------------------- \n\n");


		StabilityCalculator.CalcPowerPlantPitchingMoment theCMPowerEffectCalculator =
				theStabilityCalculator.new CalcPowerPlantPitchingMoment();

		double thrustPitchEffectDerivative = theCMPowerEffectCalculator.calcPitchingMomentDerThrust(
				aircraft, 
				theOperatingConditions,
				aircraft.get_powerPlant().getEtaEfficiency(),
				cLWingBody
				); // derivative

		System.out.println("Thrust pitching moment derivative " + thrustPitchEffectDerivative);

		// non axial pitching moment

		double clAlphaDeg = clAlphaWing/57.3;
		nonAxialPitchEffectDerivative = theCMPowerEffectCalculator.calcPitchingMomentDerNonAxial(
				aircraft,
				aircraft.get_powerPlant().getnBlade(), 
				aircraft.get_powerPlant().getFanDiameter().getEstimatedValue(),
				clAlphaDeg,centerOfGravity.get_xBRF().getEstimatedValue()
				);

		System.out.println("Non axial pitching moment derivative " + nonAxialPitchEffectDerivative);


		momentThrust =  new double [alphaStabilityArray.size()];
		momentNonAxialThrust =  new double [alphaStabilityArray.size()];

		for ( int i=0 ; i < alphaStabilityArray.size(); i++){

			momentThrust[i] = theCMPowerEffectCalculator.calcPitchingMomentThrust(
					aircraft,weight.getEstimatedValue(), cLCompleteAircraft[i], cDCompleteAircraft[i], centerOfGravity.get_zBRF().getEstimatedValue());

			momentNonAxialThrust [i] = nonAxialPitchEffectDerivative*alphaStabilityArray.get(i)/57.3;
		}

		System.out.println("The pitching moment coefficient due to thrust is " + Arrays.toString(momentThrust));

		if (plotCheck == true){
			MyChartToFileUtils.plotNoLegend(
					alphaStabilityArray.toArray() , momentThrust,
					null, null, null, null,
					"alpha_body", "CM_t",
					"deg", "",
					subfolderPath," Moment Coefficient vs alpha body due to thrust" );

		}
	}
	public void calculateMoment(){

		System.out.println("\n ------------------- ");
		System.out.println("|   PITCHING MOMENTS   |");
		System.out.println(" ------------------- \n\n");
		
		double xCG = centerOfGravity.get_xBRF().getEstimatedValue();
		double zCG = centerOfGravity.get_zBRF().getEstimatedValue();
		double mac = theWing.get_meanAerodChordActual().getEstimatedValue();

		double LRF = (theWing.get_xLEMacActualBRF().getEstimatedValue());
		double ZLRF = theWing.get_zCG().getEstimatedValue();
		xCGc = (xCG-LRF)/theWing.get_meanAerodChordActual().getEstimatedValue();
		System.out.println(" LRF " + LRF);
		System.out.println(" xcg LRF " + xCG);
		System.out.println(" mean aerodynamic chord (m) " + theWing.get_meanAerodChordActual().getEstimatedValue());
		System.out.println(" x cg ad. (MAC) " + (xCG-LRF)/theWing.get_meanAerodChordActual().getEstimatedValue());
		System.out.println(" z cg ad. (MAC) " + (zCG-ZLRF)/theWing.get_meanAerodChordActual().getEstimatedValue());
		
		// WING

		// Normal and tangential components

		cNWingBody = new double [alphaStabilityArray.size()];
		cNWing = new double [alphaStabilityArray.size()];
		cCIsolatedWing = new double [alphaStabilityArray.size()];

		for ( int i=0; i<alphaStabilityArray.size(); i++){

			cNWingBody[i] =   cLWingBodyArray[i]* Math.cos(Math.toRadians(alphaStabilityArray.get(i))) +
					cDWingArray[i] * Math.sin(Math.toRadians(alphaStabilityArray.get(i)));

			cNWing[i] =   cLWingActualArray[i]* Math.cos(Math.toRadians(alphaStabilityArray.get(i))) +
					cDWingArray[i] * Math.sin(Math.toRadians(alphaStabilityArray.get(i)));

			cCIsolatedWing[i] = cDWingArray[i] * Math.cos(Math.toRadians(alphaStabilityArray.get(i))) -
					cLWingActualArray[i] * Math.sin(Math.toRadians(alphaStabilityArray.get(i)));
		}

		// arms

		double xArmWingBody = xCG - xACWingBodyBRF;
		double zArmWingBody = zCG -theWing.get_zCG().getEstimatedValue();

		double xArmWing = xCG - xACWingBRF;
		double zArmWing = zCG -theWing.get_zCG().getEstimatedValue();

		// HORIZONTAL TAIL

		// Normal and tangential components

		double xArmHTail =  xACHTailBRF - xCG ;
		double zArmHTail = Math.abs(zCG-zACHtailBRF);

		if (zCG > 0 && zACHtailBRF > 0  && zACHtailBRF >zCG)
			zArmHTail = zArmHTail;

		if (zCG < 0 && zACHtailBRF < 0  && Math.abs(zACHtailBRF) > Math.abs(zCG))
			zArmHTail = - zArmHTail;

		if (zCG < 0 && zACHtailBRF < 0  && Math.abs(zACHtailBRF) < Math.abs(zCG))
			zArmHTail = zArmHTail;

		if (zCG > 0 && zACHtailBRF > 0  && zACHtailBRF < zCG)
			zArmHTail = - zArmHTail;

		if ( zCG<0 && zACHtailBRF>0)
			zArmHTail = zArmHTail;

		if ( zCG>0 && zACHtailBRF<0)
			zArmHTail = - zArmHTail;

		// Component Equation
		double pressureRatio = theHTail.getAerodynamics().get_dynamicPressureRatio() ;

		double volumetricRatio = (theHTail.get_surface().getEstimatedValue()/theWing.get_surface().getEstimatedValue()) * 
				(xArmHTail/theWing.get_meanAerodChordActual().getEstimatedValue());



		// WING

		double [] cMWingArray = new double [nValueAlpha];
		double [] cMWingNoPendular = new double [nValueAlpha];

		for(int i=0; i<nValueAlpha; i++){
			cMWingArray[i] = cNWing[i] * (xArmWing/mac) + cCIsolatedWing[i] * (zArmWing/mac) + cmACWingArray[i];
			cMWingNoPendular[i] = cNWing[i] * (xArmWing/mac)+ cmACWingArray[i];
		}


		// Horizontal Tail

		double [] cMHorizontalTailCleanArray = new double [nValueAlpha];

		for(int i=0; i<nValueAlpha; i++){
			cMHorizontalTailCleanArray[i] = -cLHTailCleanArray[i] * volumetricRatio * pressureRatio ;
			//					+
			//					cDHTailCleanArray[i] * ((theHTail.get_surface().getEstimatedValue()/theWing.get_surface().getEstimatedValue()))* 
			//					(zArmHTail/mac) * pressureRatio;
		}

		// Horizontal Tail

		double [] cMHorizontalTailArraywithDrag = new double [nValueAlpha];

		for(int i=0; i<nValueAlpha; i++){
			cMHorizontalTailArraywithDrag[i] = -cLHTailCleanArray[i] * volumetricRatio * pressureRatio	+
					cDHTailCleanArray[i] * ((theHTail.get_surface().getEstimatedValue()/theWing.get_surface().getEstimatedValue()))* 
					(zArmHTail/mac) * pressureRatio;
		}

		// Total

		
		double [] cMTotalCleanArray = new double [nValueAlpha];
		if( aircraft.get_typeVehicle()==AircraftTypeEnum.JET){
			
			for(int i=0; i<nValueAlpha; i++){
				cMTotalCleanArray[i] = cMWingArray[i] + cMHorizontalTailCleanArray [i] + cmFuselageArray[i];
			}
		}
		
		else{
		
		for(int i=0; i<nValueAlpha; i++){
			cMTotalCleanArray[i] = cMWingArray[i] + cMHorizontalTailCleanArray [i] + cmFuselageArray[i]
					+ momentThrust[i] + momentNonAxialThrust[i];
		}
		}

		// Equation 



		String tauKey;
		double deltaE;
		double alphaActual;
		double [] cMTemp = new double [nValueAlpha];
		double [] cMHTailTemp = new double [nValueAlpha];
		for (int i = 0; i<tauIndexArray.length; i++){
			tauKey = deltaEArrayString[i];
			deltaE = deltaEArray[i];
			System.out.println(" delta " + tauKey);
			double [] cl = new double [nValueAlpha-1];
			double [] cd = new double [nValueAlpha-1];
			double [] cm = new double [nValueAlpha-1];
			double [] cLTot = new double[nValueAlpha-1];
			
			for (int j=0; j<nValueAlpha-1; j++){
				alphaActual = alphaStabilityArray.get(j)-downwashAnglesArray[j] + theHTail.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue();
				cl[j] = MyMathUtils.getInterpolatedValue1DLinear(alphaHTailMap.get(tauKey), cLHTailMap.get(tauKey), alphaActual);
				cd[j] = MyMathUtils.getInterpolatedValue1DLinear(alphacDHTailMap.get(tauKey), cDHTailMap.get(tauKey), alphaActual);
				cm[j] = MyMathUtils.getInterpolatedValue1DLinear(alphacMHTailMap.get(tauKey), cMHTailMap.get(tauKey), alphaActual);
				cMHTailTemp[j] = -cl[j] * volumetricRatio * pressureRatio 
						+ cd[j] * ((theHTail.get_surface().getEstimatedValue()/theWing.get_surface().getEstimatedValue()))* 
						(zArmHTail/mac) * pressureRatio + cm[j]* pressureRatio *((theHTail.get_surface().getEstimatedValue()/theWing.get_surface().getEstimatedValue()))
						* ( theHTail.get_meanAerodChordActual().getEstimatedValue()/theWing.get_meanAerodChordActual().getEstimatedValue());

				cLTot[j] = cLWingBodyArray[j] + cl[j]*pressureRatio*(theHTail.get_surface().getEstimatedValue()/theWing.get_surface().getEstimatedValue());

				if( aircraft.get_typeVehicle()==AircraftTypeEnum.JET){
					cMTemp[j] = cMWingArray[j]  +  cMHTailTemp[j]+ cmFuselageArray[j];}
				else{
					cMTemp[j] = cMWingArray[j]  +  cMHTailTemp[j]+
							cmFuselageArray[j] +  momentThrust[j] + momentNonAxialThrust[j];
				}
			}
			cMTotalRespectToCGMap.put(tauKey, cMTemp);
			cMHorizTailRespectToCGMap.put(tauKey, cMHTailTemp);
			cLTotMap.put(tauKey, cLTot);
		
			System.out.println("alpha " + Arrays.toString(alphaHTailMap.get(tauKey)));
			System.out.println("cl " + Arrays.toString(cl));
			System.out.println("cd " + Arrays.toString(cd));
			System.out.println("cm " + Arrays.toString(cm));

			
			
			cMHTailTemp =  new double [nValueAlpha];
			cMTemp = new double [nValueAlpha];
			cLTot = new double [nValueAlpha];

		}
		
		
		//cltot
		
		



		// plot 


		double [][] alphaThrust= {alphaStabilityArray.toArray(),alphaStabilityArray.toArray()};

		double [][] cmThrus = {momentThrust,momentNonAxialThrust};

		String [] legendThrust = new String [deltaEArrayString.length];

		legendThrust[0] = "thrust";
		legendThrust[1] = "non axial";


//		System.out.println(" cm " + Arrays.toString(cMTotalRespectToCGMap.get(deltaEArrayString[1])));
		if(aircraft.get_typeVehicle() != AircraftTypeEnum.JET){
		MyChartToFileUtils.plot(
				alphaThrust,	cmThrus, // array to plot
				null, 20.0, null, null,					    // axis with limits
				"alpha", "CM_{tot}", "", "",	    // label with unit
				legendThrust,					// legend
				subfolderPath, "CM thrust contributes");			    // output informations
		}
		System.out.println("\t \t \tDONE  ");


		MyChartToFileUtils.plotNoLegend(
				alphaStabilityArray.toArray() , cMHorizontalTailCleanArray,
				null, null, null, null,
				"alpha_body", "CM",
				"deg", "",
				subfolderPath," HORIZONTAL TAIL Moment Coefficient vs alpha body respect to CG " );

		MyChartToFileUtils.plotNoLegend(
				alphaStabilityArray.toArray() , cMWingArray,
				null, null, null, null,
				"alpha_body", "CM",
				"deg", "",
				subfolderPath," WING Moment Coefficient vs alpha body respect to CG " );

		MyChartToFileUtils.plotNoLegend(
				alphaStabilityArray.toArray() , cMTotalCleanArray,
				null, null, null, null,
				"alpha_body", "CM",
				"deg", "",
				subfolderPath," TOTAL Moment Coefficient vs alpha body respect to CG no deflection" );



		// Wing 


		double [][] alpha= {alphaStabilityArray.toArray(),alphaStabilityArray.toArray(),
				alphaStabilityArray.toArray(),alphaStabilityArray.toArray(), alphaStabilityArray.toArray(),alphaStabilityArray.toArray(),alphaStabilityArray.toArray()};

		double [][] cm = {cMTotalRespectToCGMap.get(deltaEArrayString[0]),cMTotalRespectToCGMap.get(deltaEArrayString[1]),
				cMTotalRespectToCGMap.get(deltaEArrayString[2]), cMTotalRespectToCGMap.get(deltaEArrayString[3]),
				cMTotalRespectToCGMap.get(deltaEArrayString[4]), cMTotalRespectToCGMap.get(deltaEArrayString[5]),
				cMTotalRespectToCGMap.get(deltaEArrayString[6])};

		String [] legend = new String [deltaEArrayString.length];

		for( int i=0; i<deltaEArrayString.length; i++){

			legend[i] = deltaEArrayString[i];
		}

//		System.out.println(" cm " + Arrays.toString(cMTotalRespectToCGMap.get(deltaEArrayString[1])));
		MyChartToFileUtils.plot(
				alpha,	cm, // array to plot
				0.0, 20.0, null, null,					    // axis with limits
				"alpha", "CM tot", "", "",	    // label with unit
				legend,					// legend
				subfolderPath, "CM total ");			    // output informations

		System.out.println("\t \t \tDONE  ");


		double [][] alphatail= {alphaStabilityArray.toArray(),alphaStabilityArray.toArray(),
				alphaStabilityArray.toArray(),alphaStabilityArray.toArray(), alphaStabilityArray.toArray(),alphaStabilityArray.toArray(),alphaStabilityArray.toArray(),};

		double [][] cmHtail = {cMHorizTailRespectToCGMap.get(deltaEArrayString[0]),cMHorizTailRespectToCGMap.get(deltaEArrayString[1]),
				cMHorizTailRespectToCGMap.get(deltaEArrayString[2]),cMHorizTailRespectToCGMap.get(deltaEArrayString[3]),
				cMHorizTailRespectToCGMap.get(deltaEArrayString[4]), cMHorizTailRespectToCGMap.get(deltaEArrayString[5]),
				cMHorizTailRespectToCGMap.get(deltaEArrayString[6])};


		for( int i=0; i<deltaEArrayString.length; i++){

			legend[i] = deltaEArrayString[i];
		}

//		System.out.println(" cm " + Arrays.toString(cMTotalRespectToCGMap.get(deltaEArrayString[1])));
		MyChartToFileUtils.plot(
				alphatail,	cmHtail, // array to plot
				0.0, 20.0, null, null,					    // axis with limits
				"alpha", "CM tot", "", "",	    // label with unit
				legend,					// legend
				subfolderPath, "HORIZONTAL TAIL CM vs cg total ");			    // output informations

		System.out.println("\t \t \tDONE  ");

		
		double [][] cl = {cLTotMap.get(deltaEArrayString[0]),cLTotMap.get(deltaEArrayString[1]),
				cLTotMap.get(deltaEArrayString[2]), cLTotMap.get(deltaEArrayString[3]),
				cLTotMap.get(deltaEArrayString[4]), cLTotMap.get(deltaEArrayString[5]),
				cLTotMap.get(deltaEArrayString[6])};


		double [][] cmcl = {cMTotalRespectToCGMap.get(deltaEArrayString[0]),cMTotalRespectToCGMap.get(deltaEArrayString[1]),
				cMTotalRespectToCGMap.get(deltaEArrayString[2]), cMTotalRespectToCGMap.get(deltaEArrayString[3]),
				cMTotalRespectToCGMap.get(deltaEArrayString[4]), cMTotalRespectToCGMap.get(deltaEArrayString[5]),
				cMTotalRespectToCGMap.get(deltaEArrayString[6])};

//		System.out.println(" cm " + Arrays.toString(cMTotalRespectToCGMap.get(deltaEArrayString[1])));
		MyChartToFileUtils.plot(
				cl,	cmcl, // array to plot
				0.0, null, null, null,					    // axis with limits
				"cl", "CM tot", "", "",	    // label with unit
				legend,					// legend
				subfolderPath, "CM total vs CLTotal ");			    // output informations

		System.out.println("\t \t \tDONE  ");
		
		

		double [][] clTot = {cLTotMap.get(deltaEArrayString[0]),cLTotMap.get(deltaEArrayString[1]),
				cLTotMap.get(deltaEArrayString[2]), cLTotMap.get(deltaEArrayString[3]),
				cLTotMap.get(deltaEArrayString[4]), cLTotMap.get(deltaEArrayString[5]),
				cLTotMap.get(deltaEArrayString[6])};

		double [][] alphaTot = {alphaStabilityArray.toArray(),alphaStabilityArray.toArray(),
				alphaStabilityArray.toArray(),alphaStabilityArray.toArray(), alphaStabilityArray.toArray(),alphaStabilityArray.toArray(),alphaStabilityArray.toArray()};
		
//		System.out.println(" cm " + Arrays.toString(cMTotalRespectToCGMap.get(deltaEArrayString[1])));
		MyChartToFileUtils.plot(
				alphaTot,	clTot, // array to plot
				null, null, null, null,					    // axis with limits
				"alpha", "CL tot", "", "",	    // label with unit
				legend,					// legend
				subfolderPath, "CL tot vs alpha body ");			    // output informations

		System.out.println("\t \t \tDONE  ");

		//		double[] AlphaArr = {0,

		// All

		if(aircraft.get_typeVehicle() != AircraftTypeEnum.JET){
		double [][] alphaArrays = {alphaStabilityArray.toArray(),alphaStabilityArray.toArray(),
				alphaStabilityArray.toArray(),alphaStabilityArray.toArray(), alphaStabilityArray.toArray(),
				alphaStabilityArray.toArray(),alphaStabilityArray.toArray()};

		double [][] cmArrays = {cMWingArray, cMWingNoPendular, cMHorizontalTailCleanArray,
				cmFuselageArray, cMHorizontalTailArraywithDrag, momentThrust,momentNonAxialThrust};

		String [] legendArrays = new String [deltaEArrayString.length];


		legendArrays[0] = "Isolated Wing";
		legendArrays[1] = "Isolated wing zcg =0 ";
		legendArrays[2] = " Horizontal Tail";
		legendArrays[3] = "Fuselage";
		legendArrays[4] = " Horizontal Tail with drag";
		legendArrays[5] = " Thrust";
		legendArrays[6] = " Non Axial, Thrust";


//		System.out.println(" cm " + Arrays.toString(cMTotalRespectToCGMap.get(deltaEArrayString[1])));
		MyChartToFileUtils.plot(
				alphaArrays,cmArrays, // array to plot
				null, 20.0, null, null,					    // axis with limits
				"alpha", "CM", "", "",	    // label with unit
				legendArrays,					// legend
				subfolderPath, "Contributes CM total ");			    // output informations

		System.out.println("\t \t \tDONE  ");
		}
		
		else{
			double [][] alphaArrays = {alphaStabilityArray.toArray(),alphaStabilityArray.toArray(),
					alphaStabilityArray.toArray(),alphaStabilityArray.toArray(), 
					alphaStabilityArray.toArray()};

			double [][] cmArrays = {cMWingArray, cMWingNoPendular, cMHorizontalTailCleanArray,
					cmFuselageArray, cMHorizontalTailArraywithDrag};

			String [] legendArrays = new String [5];


			legendArrays[0] = "Isolated Wing";
			legendArrays[1] = "Isolated wing zcg =0 ";
			legendArrays[2] = " Horizontal Tail";
			legendArrays[3] = "Fuselage";
			legendArrays[4] = " Horizontal Tail with drag";
			


//			System.out.println(" cm " + Arrays.toString(cMTotalRespectToCGMap.get(deltaEArrayString[1])));
			MyChartToFileUtils.plot(
					alphaArrays,cmArrays, // array to plot
					null, 20.0, null, null,					    // axis with limits
					"alpha", "CM", "", "",	    // label with unit
					legendArrays,					// legend
					subfolderPath, "Contributes CM total ");			    // output informations

			System.out.println("\t \t \tDONE  ");
		}

	}


	public void calculatedeltaEEquilibrium(){
	
		Double [] deltaEEquilibrium = new Double [alphaStabilityArray.size()];
		double [] deltaEEquilibriumdouble = new double [alphaStabilityArray.size()];
		double [] deltaEinv = new double [deltaEArray.length];
		Double [] cmFItArray = new Double [160];
		int indexVar;
		double cmTemp;
		double alphaTemp =0;
		double[] alphaFit = MyArrayUtils.linspace(alphaStabilityArray.get(0), alphaStabilityArray.get(alphaStabilityArray.size()-1), 160);
		double [] alphaEquilibrium = new double[deltaEArray.length];
		double [] alphaEquilibriumInv = new double[deltaEArray.length];
		for (int i=0 ; i<deltaEArray.length; i++){
			cmFItArray = MyMathUtils.getInterpolatedValue1DLinear(alphaStabilityArray.toArray(), cMTotalRespectToCGMap.get(deltaEArrayString[i]), alphaFit);
		
			for (int j=0; j<cMTotalRespectToCGMap.get(deltaEArrayString[i]).length-10; j++){
				cmTemp = Math.abs(cMTotalRespectToCGMap.get(deltaEArrayString[i])[j]);
				if(cmTemp< 0.01){
			 indexVar = j;
	
			 alphaTemp = alphaStabilityArray.get(j);}
//				System.out.println(alphaTemp);
				}
		alphaEquilibrium[i] = alphaTemp;
		
		}

		for (int i=0; i<alphaEquilibrium.length; i++){
			alphaEquilibriumInv[i] = alphaEquilibrium[alphaEquilibrium.length-1-i];
			deltaEinv[i] = deltaEArray[deltaEArray.length-1-i];
		}
		
		deltaEEquilibrium = MyMathUtils.getInterpolatedValue1DLinear(alphaEquilibriumInv, deltaEinv, alphaStabilityArray.toArray());
		
		for (int i=0; i<deltaEEquilibrium.length; i++){
			deltaEEquilibriumdouble[i] = (double)deltaEEquilibrium[i];
		}
		if (plotCheck == true){
			MyChartToFileUtils.plotNoLegend(
					alphaStabilityArray.toArray() , deltaEEquilibriumdouble,
					null, null, null, null,
					"alpha_body", "deltaee",
					"deg", "",
					subfolderPath," delta e equilibrium " );
			
		}
	}
	
	
	public void neutralPointCalculator(){
	double [] dCMdCL = new double [nValueAlpha];
	double[] neutralPoint =  new double [nValueAlpha];
	for (int i=1; i<nValueAlpha-2; i++){
		dCMdCL [i] = (((cMTotalRespectToCGMap.get(deltaEArrayString[0])[i]-cMTotalRespectToCGMap.get(deltaEArrayString[0])[i-1])/
				(cLTotMap.get(deltaEArrayString[0])[i]-cLTotMap.get(deltaEArrayString[0])[i-1]))+
				(cMTotalRespectToCGMap.get(deltaEArrayString[0])[i+1]-cMTotalRespectToCGMap.get(deltaEArrayString[0])[i])/
				(cLTotMap.get(deltaEArrayString[0])[i+1]-cLTotMap.get(deltaEArrayString[0])[i]))/2;
	}
		dCMdCL[0] = 	(cMTotalRespectToCGMap.get(deltaEArrayString[0])[1]-cMTotalRespectToCGMap.get(deltaEArrayString[0])[0])/
				(cLTotMap.get(deltaEArrayString[0])[1]-cLTotMap.get(deltaEArrayString[0])[0])/2;
				
//		dCMdCL[nValueAlpha-1] = (cMTotalRespectToCGMap.get(deltaEArrayString[0])[nValueAlpha-1]-cMTotalRespectToCGMap.get(deltaEArrayString[0])[nValueAlpha-2])/
//				(cLTotMap.get(deltaEArrayString[0])[nValueAlpha-1]-cLTotMap.get(deltaEArrayString[0])[nValueAlpha-2])/2;
		
		
		
		for (int j=0; j<dCMdCL.length; j++){
			neutralPoint[j] = xCGc - dCMdCL[j];
		}
		
		if (plotCheck==true){
			MyChartToFileUtils.plotNoLegend(
					alphaStabilityArray.toArray() , neutralPoint,
					null, null, null, null,
					"alpha_body", "N0",
					"deg", "",
					subfolderPath," N0 stick fixed delta e =0 " );
		}
	}

	public void calculateDeltaEArray(){
		this.deltaEArray = MyArrayUtils.linspace(deltaMin, deltaMax , 7);
	}

	// GETTERS AND SETTERS
	//----------------------------------------------------------
	public double getcLIsolatedWing() {
		return cLIsolatedWing;
	}


	public void setcLIsolatedWing(double cLIsolatedWing) {
		this.cLIsolatedWing = cLIsolatedWing;
	}


	public double[] getAlphaStabilityArray() {
		return alphaStabilityArray.toArray();
	}

	public double[] getcLWingArray() {
		return cLWingCleanArray;
	}


	public void setcLWingArray(double[] cLWingArray) {
		this.cLWingCleanArray = cLWingArray;
	}

	public double[] getcLWingTOArray() {
		return cLWingTOArray;
	}


	public double[] getcLWingLandingArray() {
		return cLWingLandingArray;
	}


	public double getcLIsolatedWingTO() {
		return cLIsolatedWingTO;
	}


	public double getcLIsolatedWingLand() {
		return cLIsolatedWingLand;
	}


	public double[] getcLWingCleanArray() {
		return cLWingCleanArray;
	}


	public double getcLAlphaWingBody() {
		return cLAlphaWingBody;
	}


	public void setcLAlphaWingBody(double cLAlphaWingBody) {
		this.cLAlphaWingBody = cLAlphaWingBody;
	}


	public double getcLWingBody() {
		return cLWingBody;
	}


	public void setcLWingBody(double cLWingBody) {
		this.cLWingBody = cLWingBody;
	}


	public double[] getcLWingBodyArray() {
		return cLWingBodyArray;
	}


	public Amount<Angle> getAlphaMin() {
		return alphaMin;
	}


	public Amount<Angle> getAlphaMinWing() {
		return alphaMinWing;
	}


	public Amount<Angle> getAlphaMaxWing() {
		return alphaMaxWing;
	}


	public Amount<Angle> getAlphaMinHTail() {
		return alphaMinHTail;
	}


	public Amount<Angle> getAlphaMaxHtail() {
		return alphaMaxHtail;
	}


	public double getAlphaMaxTO() {
		return alphaMaxTO;
	}


	public double getAlphaMaxLand() {
		return alphaMaxLand;
	}


	public double getcLMaxTO() {
		return cLMaxTO;
	}


	public double getcLMaxLand() {
		return cLMaxLand;
	}


	public double getcLAlphaTO() {
		return cLAlphaTO;
	}


	public double getcLAlphaLand() {
		return cLAlphaLand;
	}


	public double[] getcLHTailCleanArray() {
		return cLHTailCleanArray;
	}


	public double getcLAlphaHTailClean() {
		return cLAlphaHTailClean;
	}


	public void setcLAlphaHTailClean(double cLAlphaHTailClean) {
		this.cLAlphaHTailClean = cLAlphaHTailClean;
	}


	public double[] getDeltaEArray() {
		return deltaEArray;
	}


	public void setDeltaEArray(double[] deltaEArray) {
		this.deltaEArray = deltaEArray;
	}


	public double[] getTauIndexArray() {
		return tauIndexArray;
	}


	public void setTauIndexArray(double[] tauIndexArray) {
		this.tauIndexArray = tauIndexArray;
	}


	public double[] getcLCompleteAircraftdeltaEArray() {
		return cLCompleteAircraftdeltaEArray;
	}


	public double[] getcDWingCleanArray() {
		return parasiteCDWingCleanArray;
	}


	public void setcDWingCleanArray(double[] cDWingCleanArray) {
		this.parasiteCDWingCleanArray = cDWingCleanArray;
	}


	public double[] getParasiteCDWingCleanArray() {
		return parasiteCDWingCleanArray;
	}


	public double[] getParasiteCDHTailCleanArray() {
		return parasiteCDHTailCleanArray;
	}


	public double[] getcDWingInducedArray() {
		return inducedCDWingArray;
	}


	public double[] getcDHTailCleanArray() {
		return cDHTailCleanArray;
	}


	public double getaCWing() {
		return aCWing;
	}


	public void setaCWing(double aCWing) {
		this.aCWing = aCWing;
	}


	public double[] getCmQuarterChordWingArray() {
		return cmQuarterChordWingArray;
	}


	public double[] getCmACWingArray() {
		return cmACWingArray;
	}


	public double[] getCmQuarterChordHtailArray() {
		return cmQuarterChordHtailArray;
	}


	public double[] getCmFuselageArray() {
		return cmFuselageArray;
	}


	public double getCm0LiftFuselage() {
		return cm0LiftFuselage;
	}


	public void setCm0LiftFuselage(double cm0LiftFuselage) {
		this.cm0LiftFuselage = cm0LiftFuselage;
	}


	public double[] getcMacWingBody() {
		return cMacWingBody;
	}


	public void setcMacWingBody(double[] cMacWingBody) {
		this.cMacWingBody = cMacWingBody;
	}



}



















