package sandbox.mr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcHighLiftDevices;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.MethodEnum;
import functions.Linspace;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.customdata.CenterOfGravity;
import standaloneutils.customdata.MyArray;

public class ACStabilityManager {

	// VARIABLE DECLARATION--------------------------------------

	OperatingConditions theOperatingConditions = new OperatingConditions();
	LSAerodynamicsManager theLSAnalysis;
	LSAerodynamicsManager theLSHTailAnalysis;
	FusAerodynamicsManager theFuselageManager;
	ConditionEnum theCondition;

	MyAirfoil meanAirfoil;
	
	int nValueAlpha = 50;

	CenterOfGravity centerOfGravity = new CenterOfGravity();
	Amount<Length> maxXaftCenterOfGravityBRF;
	Amount<Length> maxXforwCenterOfGravityBRF;
	Amount<Angle> alphaBody = null;
	Amount<Angle> alphaMin;
	Amount<Angle> alphaMax;

	Aircraft aircraft;
	LiftingSurface theWing;
	Fuselage theFuselage;
	LiftingSurface theHTail;
	String subfolderPath;
	String pathXMLTakeOFF = null;
	String pathXMLLanding = null;
	boolean alphaCheck;
	boolean plotCheck = false;


	MyArray alphaStabilityArray = new MyArray();
	MyArray alphaStabilityHLArray = new MyArray();


	//Output Values

	double cLIsolatedWing;
	double cLIsolatedWingTO;
	double cLIsolatedWingLand;
	double cLAlphaWingBody;
	double cLWingBody;

	//Output Arrays
	private double[] cLWingCleanArray;
	private double[] cLWingTOArray;
	private double[] cLWingLandingArray;




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



	// BUILDER--------------------------------------

	/**
	 * This class manages the calculation of the longitudinal static stability of an aircraft.
	 * 
	 * @author Manuela Ruocco
	 * @param the aircraft
	 * @param the minimum value of alpha array. It can to be in degree or radian. This value may to be null in order to execute
	 * analysis only for an angle of attack. In this case the values of alpha max and alpha min wil bee alpha bosy +-1 deg
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
		
		

		this.alphaBody = alphaBody;
		if (alphaBody==null){
			alphaCheck = false;}
		else
			if (alphaBody.getUnit() == NonSI.DEGREE_ANGLE){
				alphaBody = alphaBody.to(SI.RADIAN);
			}
		alphaCheck = true;


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


		alphaStabilityArray.linspace(alphaMin.getEstimatedValue(), alphaMax.getEstimatedValue(), nValueAlpha);
		alphaStabilityHLArray.linspace(alphaMin.getEstimatedValue(), alphaMax.getEstimatedValue()-2, nValueAlpha);
		System.out.println(" alpha stability array " + alphaStabilityArray);

		//Set Operating Conditions and CG position 

		switch (theCondition) {
		case TAKE_OFF:
			theOperatingConditions.set_machCurrent(aircraft.get_theAerodynamics().get_machTakeOFF());
			break;

		case LANDING:	
			theOperatingConditions.set_machCurrent(aircraft.get_theAerodynamics().get_machLanding());
			break;

		case CRUISE:	
			theOperatingConditions.set_machCurrent(aircraft.get_theAerodynamics().get_machCruise());
			break;
		}

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

	}

	public void CalculateDragCharacteristics(){

	}

	public void CalculateMomentCharacteristics(){

	}

	public void CalculateWingLiftCharacteristics() throws InstantiationException, IllegalAccessException {


		System.out.println("\n ------------------- ");
		System.out.println("|       WING        |");
		System.out.println(" ------------------- \n\n");

		System.out.println("\t Data: ");

		// DATA
		System.out.println("Angle of incidence of wing (deg) = " + ""
				+  Math.ceil(theWing.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue()));
		if(alphaCheck == true){
			System.out.println("Angle of attack alpha body (deg) = " + "" 
					+ Math.ceil(alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()));}



		if (theCondition == ConditionEnum.TAKE_OFF || theCondition == ConditionEnum.LANDING){
			// READ TAKE OFF DATA
			System.out.println("\n\n------------------------------------");
			System.out.println("\n READING XML " + theCondition + "FILE...  ");
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

			LSAerodynamicsManager.CalcHighLiftDevices highLiftCalculator = theLSAnalysis
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
			highLiftCalculator.calculateHighLiftDevicesEffects();


			// RESULTS

			System.out.println("\n -----------HIGH LIFT " + theCondition + "-------------- ");
			System.out.println("deltaCL0_flap = " + highLiftCalculator.getDeltaCL0_flap());
			System.out.println("deltaCLmax_flap = " + highLiftCalculator.getDeltaCLmax_flap());
			System.out.println("cLalpha_new = " + highLiftCalculator.getcLalpha_new());
			System.out.println("deltaAlphaMax = " + highLiftCalculator.getDeltaAlphaMaxFlap());
			System.out.println("deltaCD = " + highLiftCalculator.getDeltaCD());
			System.out.println("deltaCMc_4 = " + highLiftCalculator.getDeltaCM_c4());
			System.out.println("\n\n");


			//ARRAY FILLING

			if (pathXMLTakeOFF != null){
				cLWingTOArray = highLiftCalculator.calcCLvsAlphaHighLiftDevices(alphaMin, 
						Amount.valueOf((alphaMax.getEstimatedValue()-2), NonSI.DEGREE_ANGLE),
						nValueAlpha);
				System.out.println("CL wing " + theCondition + " Array " + Arrays.toString(cLWingTOArray ) );
			}
			if(pathXMLLanding != null){
				cLWingLandingArray =  highLiftCalculator.calcCLvsAlphaHighLiftDevices(alphaMin,
						Amount.valueOf((alphaMax.getEstimatedValue()-2),NonSI.DEGREE_ANGLE),
						nValueAlpha);
				System.out.println("CL wing " + theCondition + " Array " + Arrays.toString(cLWingLandingArray) );
			}




			// CALCULATING CL AT ALPHA

			if(alphaCheck == true){
				cLIsolatedWingTO = highLiftCalculator.calcCLatAlphaHighLiftDevices(alphaBody);
				System.out.println("\nCL of wing at " + theCondition + " at alpha body = " + cLIsolatedWingTO);
			}

			//PLOT CL VS ALPHA

			//initializing
			List<Double[]> cLHLListPlot = new ArrayList<Double[]>();
			List<Double[]> alphaHLListPlot = new ArrayList<Double[]>();
			List<String> legendCLvsAlphaHighLift  = new ArrayList<>();	

			//clean vector
			LSAerodynamicsManager.CalcCLvsAlphaCurve theCLArrayCalculator = theLSAnalysis.new CalcCLvsAlphaCurve();
			cLWingCleanArray = theCLArrayCalculator.nasaBlackwellCompleteCurve(alphaMin, alphaMax, nValueAlpha, false);


			// filling lists

			Double[] cLWingCleanArrayDouble = new Double[cLWingCleanArray.length];
			Double[] cLWingHLArrayDouble = new Double[cLWingTOArray.length];
			Double [] alphaArrayDouble = new Double[cLWingCleanArray.length];

			for (int i=0; i<cLWingCleanArray.length; i++){
				cLWingCleanArrayDouble[i] = (Double)cLWingCleanArray[i];
				alphaArrayDouble[i] = (Double)alphaStabilityArray.toArray()[i];
			}
			cLHLListPlot.add(0,cLWingCleanArrayDouble);
			alphaHLListPlot.add(0,alphaArrayDouble);
			legendCLvsAlphaHighLift.add("clean");

			if (pathXMLTakeOFF != null){
				for (int i=0; i<cLWingTOArray.length; i++){
					cLWingHLArrayDouble[i] = (Double)cLWingTOArray[i];
					alphaArrayDouble[i] = (Double)alphaStabilityHLArray.toArray()[i];
					}}
			if	(pathXMLLanding != null){
				for (int i=0; i<cLWingTOArray.length; i++){
					cLWingHLArrayDouble[i] = (Double)cLWingLandingArray[i];
					alphaArrayDouble[i] = (Double)alphaStabilityHLArray.toArray()[i];
					}}

			cLHLListPlot.add(1,cLWingHLArrayDouble);
			alphaHLListPlot.add(1,alphaArrayDouble);
			legendCLvsAlphaHighLift.add(theCondition.toString());




			MyChartToFileUtils.plotJFreeChart(alphaHLListPlot,
					cLHLListPlot,
					"CL vs alpha " + theCondition.toString() ,
					"alpha",
					"CL",
					null, null, null,null,
					"deg",
					"",
					true,
					legendCLvsAlphaHighLift,
					subfolderPath,
					"CL vs alpha wing clean and " + theCondition.toString());

			System.out.println("\n\n\t\t\tWRITING CL vs ALPHA CHART TO FILE for wing");

		}

		else{

			//ARRAY FILLING
			LSAerodynamicsManager.CalcCLvsAlphaCurve theCLArrayCalculator = theLSAnalysis.new CalcCLvsAlphaCurve();

			cLWingCleanArray = theCLArrayCalculator.nasaBlackwellCompleteCurve(alphaMin, alphaMax, nValueAlpha, true);
			System.out.println("CL wing Clean Array " + Arrays.toString(cLWingCleanArray) );


			//CALCULATING CL AT ALPHA FOR WING
			if(alphaCheck == true){
				LSAerodynamicsManager.CalcCLAtAlpha theCLWingCalculator = theLSAnalysis.new CalcCLAtAlpha();
				cLIsolatedWing = theCLWingCalculator.nasaBlackwellAlphaBody(alphaBody);
				System.out.println("\nCL of Isolated wing at alpha body = " + cLIsolatedWing);
			}


			//PLOT CL VS ALPHA
			if(plotCheck == true){
				System.out.println("\n-------------------------------------");
				System.out.println("\t \t \tWRITING CL vs ALPHA CHART TO FILE  ");
				MyChartToFileUtils.plotNoLegend(
						alphaStabilityArray.toArray(),cLWingCleanArray, 
						null, null , null , null ,					    // axis with limits
						"alpha_W", "CL", "deg", "",	   				
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

	}


	public void CalculateFuselageLiftCharacteristics(){
		
		System.out.println("\n ------------------- ");
		System.out.println("|     FUSELAGE       |");
		System.out.println(" ------------------- \n\n");
		
		double cLAlphaWing = theLSAnalysis.getcLLinearSlopeNB();
		System.out.println(" cl alpha " + cLAlphaWing);
		cLAlphaWingBody = theFuselageManager.calculateCLAlphaFuselage(cLAlphaWing);

		cLWingBody = aircraft.get_theAerodynamics().calculateCLAtAlphaWingBody(alphaBody, meanAirfoil, true);
		System.out.println("-------------------------------------");
		System.out.println(" CL of Wing Body at alpha body = " + cLWingBody);

		System.out.println("\n \t \t \tWRITING CL VS ALPHA CHARTS TO FILE");
		aircraft.get_theAerodynamics().PlotCLvsAlphaCurve(meanAirfoil, subfolderPath);
		System.out.println("DONE");
		
	}
	public void CalculateWingBodyLiftCharacteristics(){

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



}



















