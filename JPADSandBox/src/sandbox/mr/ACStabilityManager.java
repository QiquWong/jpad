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
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
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
	ConditionEnum theCondition;
	
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
	private double cLIsolatedWing;
	
	MyArray alphaStabilityArray = new MyArray();
	private double[] cLWingArray;
	

	// High Lift Devices Input
	List<Double[]> deltaFlap = new ArrayList<Double[]>();
	List<FlapTypeEnum> flapType = new ArrayList<FlapTypeEnum>();
	List<Double> etaInFlap = new ArrayList<Double>();
	List<Double> etaOutFlap = new ArrayList<Double>();
	List<Double> cfc = new ArrayList<Double>();

			
	// BUILDER--------------------------------------
	
	/**
	 * This class manages the calculation of the longitudinal static stability of an aircraft.
	 * 
	 * @author Manuela Ruocco
	 * @param the aircraft
	 * @param the minimum value of alpha array. It can to be in degree or radian
	 * @param the maximum value of alpha array. It can to be in degree or radian
	 * @param the actual condition (take off, landing, cruise)
	 * @param the angle of attack (alpha body) for the calculation of numerical value of CL, CD, CM. This value may be null. In this case the class calculates
	 * only all stability characteristics at an array of alpha body
	 * @param When this check value is true will be draw all graphs
	 */
	public ACStabilityManager(Aircraft theAircraft, ConditionEnum theCondition,Amount<Angle> alphaMin, Amount<Angle> alphaMax,
			Amount<Angle> alphaBody, boolean plotCheck, String subfolderPath, String pathXMLTakeOFF){

		this.aircraft = theAircraft;
		this.theWing = aircraft.get_wing();
		this.theFuselage = aircraft.get_fuselage();
		this.theHTail = aircraft.get_HTail();
		this.subfolderPath = subfolderPath;
		
		this.theCondition = theCondition;
		
		this.pathXMLTakeOFF = pathXMLTakeOFF;
		
		this.alphaBody = alphaBody;
		if (alphaBody==null){
			alphaCheck = false;}
		else
			alphaCheck = true;
		
		if (alphaMin.getUnit() == SI.RADIAN){
			alphaMin = alphaMin.to(NonSI.DEGREE_ANGLE);
		}
		
		if (alphaMax.getUnit() == SI.RADIAN){
			alphaMax = alphaMax.to(NonSI.DEGREE_ANGLE);
		}
		
		this.alphaMin = alphaMin;
		this.alphaMax = alphaMax;
		
		
		alphaStabilityArray.linspace(alphaMin.getEstimatedValue(), alphaMax.getEstimatedValue(), nValueAlpha);
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
		
		
		// do Analysis
		
		System.out.println("\n\n-----------------------------------");
		System.out.println("\nANALYSIS ");
		System.out.println("\n------------------------------------");
		theAnalysis.doAnalysis(aircraft,
				AnalysisTypeEnum.WEIGHTS,
				AnalysisTypeEnum.BALANCE
				);
		
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
	
	
	public void CalculateAll(){
		
		// Lift Characteristics
		CalculateLiftCharacteristics();
		CalculateDragCharacteristics();
		CalculateMomentCharacteristics();
		
		
		// CL --> need to consider flap contributes
		
		//CL, CD, CM... 
	}
	
	public void CalculateLiftCharacteristics(){
		System.out.println("\n\n------------------------------------");
		System.out.println("\n LIFT CHARACTERISTICS  ");
		System.out.println("\n------------------------------------");
		CalculateWingLiftCharacteristics();
		
	}

	public void CalculateDragCharacteristics(){

	}

	public void CalculateMomentCharacteristics(){

	}

	public void CalculateWingLiftCharacteristics(){


		System.out.println("\n ------------------- ");
		System.out.println("|       WING        |");
		System.out.println(" ------------------- \n\n");

		
		if (theCondition == ConditionEnum.TAKE_OFF){
			// READ TAKE OFF DATA
			
			// Arguments check
			if (pathXMLTakeOFF == null){
				System.err.println("NO " + theCondition + " INPUT FILE GIVEN --> TERMINATING");
				return;
			}

			JPADXmlReader reader = new JPADXmlReader(pathXMLTakeOFF);

			System.out.println("-----------------------------------------------------------");
			System.out.println("XML File Path : " + pathXMLTakeOFF);
			System.out.println("-----------------------------------------------------------");
			System.out.println("Initialize reading \n");

			List<String> flapNumberProperty = reader.getXMLPropertiesByPath("//Flap_Number");
			int flapNumber = Integer.valueOf(flapNumberProperty.get(0));
			List<String> flapTypeProperty = reader.getXMLPropertiesByPath("//FlapType");
			List<String> cfcProperty = reader.getXMLPropertiesByPath("//Cf_c");
			List<String> deltaFlap1Property = reader.getXMLPropertiesByPath("//Delta_Flap1");
			List<String> deltaFlap2Property = reader.getXMLPropertiesByPath("//Delta_Flap2");
			List<String> etaInProperty = reader.getXMLPropertiesByPath("//Flap_inboard");
			List<String> etaOutProperty = reader.getXMLPropertiesByPath("//Flap_outboard");

			for(int i=0; i<flapTypeProperty.size(); i++) {
				if(flapTypeProperty.get(i).equals("SINGLE_SLOTTED"))
					flapType.add(FlapTypeEnum.SINGLE_SLOTTED);
				else if(flapTypeProperty.get(i).equals("DOUBLE_SLOTTED"))
					flapType.add(FlapTypeEnum.DOUBLE_SLOTTED);
				else if(flapTypeProperty.get(i).equals("PLAIN"))
					flapType.add(FlapTypeEnum.PLAIN);
				else if(flapTypeProperty.get(i).equals("FOWLER"))
					flapType.add(FlapTypeEnum.FOWLER);
				else if(flapTypeProperty.get(i).equals("TRIPLE_SLOTTED"))
					flapType.add(FlapTypeEnum.TRIPLE_SLOTTED);
				else {
					System.err.println("NO VALID FLAP TYPE!!");
					return;
				}
			}

			Double[] deltaFlap1Array = new Double[deltaFlap1Property.size()];
			for(int i=0; i<deltaFlap1Array.length; i++)
				deltaFlap1Array[i] = Double.valueOf(deltaFlap1Property.get(i));

			Double[] deltaFlap2Array = new Double[deltaFlap2Property.size()];
			for(int i=0; i<deltaFlap1Array.length; i++)
				deltaFlap2Array[i] = Double.valueOf(deltaFlap2Property.get(i));

			deltaFlap.add(deltaFlap1Array);
			deltaFlap.add(deltaFlap2Array);

			for(int i=0; i<cfcProperty.size(); i++)
				cfc.add(Double.valueOf(cfcProperty.get(i)));
			for(int i=0; i<etaInProperty.size(); i++)
				etaInFlap.add(Double.valueOf(etaInProperty.get(i)));
			for(int i=0; i<etaOutProperty.size(); i++)
				etaOutFlap.add(Double.valueOf(etaOutProperty.get(i)));

			LSAerodynamicsManager.CalcHighLiftDevices highLiftCalculator = theLSAnalysis
					.new CalcHighLiftDevices(theWing,
							theOperatingConditions,
							deltaFlap,
							flapType,
							null,
							etaInFlap, 
							etaOutFlap,
							null,
							null,
							cfc, 
							null,
							null,
							null
							);
			
			
		}

		System.out.println("\t Data: ");

		// DATA
		System.out.println("Angle of incidence of wing (deg) = " + ""
				+  Math.ceil(theWing.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue()));
		if(alphaCheck == true){
			System.out.println("Angle of attack alpha body (deg) = " + "" 
					+ Math.ceil(alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()));}


		//ARRAY FILLING
		LSAerodynamicsManager.CalcCLvsAlphaCurve theCLArrayCalculator = theLSAnalysis.new CalcCLvsAlphaCurve();
		
		cLWingArray = theCLArrayCalculator.nasaBlackwellCompleteCurve(alphaMin, alphaMax, nValueAlpha);
		System.out.println("CL wing Array " + Arrays.toString(cLWingArray) );
		
		
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
					alphaStabilityArray.toArray(),cLWingArray, 
					null, null , null , null ,					    // axis with limits
					"alpha_W", "CL", "deg", "",	   				
					subfolderPath, "CL vs Alpha clean " + theWing);
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


	public void CalculateFuselageLiftCharacteristics(){

	}
	public void CalculateWingBodyLiftCharacteristics(){

	}





	// GETTERS AND SETTERS
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
		return cLWingArray;
	}


	public void setcLWingArray(double[] cLWingArray) {
		this.cLWingArray = cLWingArray;
	}



}



















