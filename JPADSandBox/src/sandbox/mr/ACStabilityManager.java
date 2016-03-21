package sandbox.mr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
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
import configuration.enumerations.AeroConfigurationTypeEnum;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.MethodEnum;
import functions.Linspace;
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


	MyAirfoil meanAirfoil;

	int nValueAlpha = 50;

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


	double [] deltaEArray = MyArrayUtils.linspace(-25, 5 , 7);


	String [] deltaEArrayString = new String [deltaEArray.length];



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

	private double [] parasiteCDWingCleanArray;
	private double [] parasiteCDHTailCleanArray;
	private double [] inducedCDWingArray;
	private double [] inducedCDHTailArray;
	private double [] cDWingCleanArray;
	private double [] cDWingTakeOffArray;
	private double [] cDWingLandingArray;
	private double [] cDHTailCleanArray;
	private double [] cD0WingPolarArray;
	private double [] cDiWingPolarArray;
	private double [] cDWaweWingPolarArray;
	private double [] cDWingPolarArray;




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
	public ACStabilityManager(MyAirfoil meanAirfoil, Aircraft theAircraft, ConditionEnum theCondition,Amount<Angle> alphaMin,
			Amount<Angle> alphaMax, Amount<Angle> alphaBody, boolean plotCheck, String subfolderPath, String pathXMLTakeOFF){

		this.aircraft = theAircraft;
		this.theWing = aircraft.get_wing();
		this.theFuselage = aircraft.get_fuselage();
		this.theHTail = aircraft.get_HTail();
		this.meanAirfoil = meanAirfoil;
		this.subfolderPath = subfolderPath;

		this.theCondition = theCondition;

		this.pathXMLTakeOFF = pathXMLTakeOFF;

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

			centerOfGravity = new CenterOfGravity(x0, y0 , z0 , xL, yL, zL, xB, yB, zB);
			centerOfGravity.calculateCGinBRF();

			break;
		}

		maxXaftCenterOfGravityBRF = Amount.valueOf((centerOfGravity.get_xBRF().getEstimatedValue()*(1+0.1)), centerOfGravity.get_xBRF().getUnit());
		maxXforwCenterOfGravityBRF = Amount.valueOf((centerOfGravity.get_xBRF().getEstimatedValue()*(1-0.1)), centerOfGravity.get_xBRF().getUnit());


	}


	public void CalculateAll() throws InstantiationException, IllegalAccessException{

		// Lift Characteristics
		CalculateLiftCharacteristics();
		CalculateDragCharacteristics();
		CalculateMomentCharacteristics();


		// CL --> need to consider flap contributes

		//CL, CD, CM... 
	}

	public void CalculateLiftCharacteristics() throws InstantiationException, IllegalAccessException {
		System.out.println("\n\n------------------------------------");
		System.out.println("\n LIFT CHARACTERISTICS  ");
		System.out.println("\n------------------------------------");
		CalculateWingLiftCharacteristics();
		CalculateFuselageLiftCharacteristics();
		CalculateHTailLiftCharacteristics();
		if (alphaCheck == true)
			CalculateCompleteAircraftLiftCharacteristics();

	}

	public void CalculateDragCharacteristics(){
		System.out.println("\n\n------------------------------------");
		System.out.println("\n DRAG CHARACTERISTICS  ");
		System.out.println("\n------------------------------------");
		CalculateWingDragCharacteristics();
		CalculateHTailDragCHaracteristics();
	}

	public void CalculateMomentCharacteristics(){

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
		cLWingBodyArray = aircraft.get_theAerodynamics().calculateCLvsAlphaWingBody(alphaMinWing, alphaMaxWing, nValueAlpha, theCondition);


		System.out.println("Cl alpha Wing " + cLAlphaWingActual);
		cLAlphaWingBody = theFuselageManager.calculateCLAlphaFuselage(cLAlphaWingActual);
		System.out.println("Cl alpha Wing Body " + cLAlphaWingBody);

		//CALCULATING CL AT ALPHA FOR WING

		cLWingBody = aircraft.get_theAerodynamics().calculateCLAtAlphaWingBody(alphaBody, meanAirfoil, true, theCondition);
		System.out.println("-------------------------------------");
		System.out.println(" CL of Wing Body at alpha body = " + cLWingBody);

		if(plotCheck == true){
			System.out.println("\n \t \t \tWRITING CL VS ALPHA CHARTS TO FILE");
			aircraft.get_theAerodynamics().PlotCLvsAlphaCurve(meanAirfoil, subfolderPath, theCondition);
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

		System.out.println(" Tau Array " + Arrays.toString(tauIndexArray));


		int nValueHtail = 20;
		double[] cLHTailArrayTemp = new double[nValueHtail];

		StabilityCalculator.CalcCLHTail theCLTauCalculator = theStabilityCalculator.new CalcCLHTail();
		for (int i=0; i<deltaEArray.length; i++){

			cLHTailArrayTemp = theCLTauCalculator.cLHtailWithElevatorDeflection(theHTail, theOperatingConditions, 
					deltaEArray[deltaEArray.length-i-1], tauIndexArray[ tauIndexArray.length-i-1],
					cLHTailCleanArray, alphaActualHTailArray );
			cLHTailMap.put(deltaEArrayString[ tauIndexArray.length-i-1], cLHTailArrayTemp);
			alphaHTailMap.put(deltaEArrayString[ tauIndexArray.length-i-1], theCLTauCalculator.getAlphaArrayWithTau());
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
					null, null, 0.0,null,
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

		// Total drag

		cDWingCleanArray = new double [nValueAlpha];
		for (int i = 0; i<nValueAlpha; i++){
			cDWingCleanArray[i] = parasiteCDWingCleanArray[i]+ inducedCDWingArray[i];
		}

		if (theCondition == ConditionEnum.TAKE_OFF || theCondition == ConditionEnum.LANDING){

			double deltaCD  = highLiftWingCalculator.getDeltaCD();
			cDWingTakeOffArray = new double [cDWingCleanArray.length];
			cDWingLandingArray = new double [cDWingCleanArray.length];
			for (int i = 0; i<nValueAlpha; i++){
		
			if ( theCondition == ConditionEnum.TAKE_OFF ){
				
				cDWingTakeOffArray[i] =  cDWingCleanArray[i] + deltaCD;
			}

			if (theCondition == ConditionEnum.LANDING){
			
				cDWingLandingArray[i] =   cDWingCleanArray[i] + deltaCD;
			}
		}
		}
		//-----------------------------------------------CD0+ CL^"/PI AR e

		// Total Drag with Parabolic interpolation

		if (theCondition == ConditionEnum.CRUISE){
		theWing.calculateFormFactor(theWing.getAerodynamics().calculateCompressibility(theOperatingConditions.get_machCurrent()));
		double cD0WingPolar= theWing.getAerodynamics().calculateCd0Parasite();

		double oswaldFactor = aircraft.get_theAerodynamics().calculateOswald(
				theOperatingConditions.get_machCurrent(), MethodEnum.HOWE);
		System.out.println("oswald factor " + oswaldFactor);

		cD0WingPolarArray = new double [alphaStabilityArray.size()];
		cDiWingPolarArray = new double [alphaStabilityArray.size()];
		cDWaweWingPolarArray = new double [alphaStabilityArray.size()];
		cDWingPolarArray = new double [alphaStabilityArray.size()];

		for (int i=0; i<alphaStabilityArray.size(); i++){

			double cLLocal = theCLWingCalculator.nasaBlackwellAlphaBody(Amount.valueOf(Math.toRadians (alphaStabilityArray.get(i)), SI.RADIAN));	
			cD0WingPolarArray[i] = cD0WingPolar;
			cDiWingPolarArray[i] = (Math.pow(cLLocal, 2))/(Math.PI * theWing.get_aspectRatio() * oswaldFactor);
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
		
		CalcHighLiftDevices theHighLiftTailalculator;
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
					"alpha_h", "CD_induced",
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



}



















