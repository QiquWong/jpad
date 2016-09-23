package analyses.liftingsurface;

import static java.lang.Math.toRadians;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.OperatingConditions;
import analyses.analysismodel.InnerCalculator;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCLAtAlpha;
import analyses.liftingsurface.LSAerodynamicsManager.CalcHighLiftDevices;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.stability.StabilityCalculators;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class LSAerodynamicsCalculator {

	/*
	 *******************************************************************************
	 * THIS CLASS IS A PROTOTYPE OF THE NEW LSAerodynamicsManager (WORK IN PROGRESS)
	 * 
	 * @author Vittorio Trifari, Manuela Ruocco, Agostino De Marco
	 *******************************************************************************
	 */
	
	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	// INPUT DATA (IMPORTED AND CALCULATED)
	private LiftingSurface _theLiftingSurface;
	private OperatingConditions _theOperatingConditions;
	private Map <String, MethodEnum> _taskMap;
	private int _numberOfPointSemiSpanWise;
	private int _numberOfAlphas;
	private List<Amount<Angle>> _alphaArray;
	private Double _currentMachNumber;
	
	// CRITICAL MACH NUMBER
	private Map <MethodEnum, Double> _criticalMachNumber;
	
	// LIFT 
	private Map <MethodEnum, Amount<Angle>> _alphaZeroLift;
	private Map <MethodEnum, Amount<Angle>> _alphaStar;
	private Map <MethodEnum, Amount<Angle>> _alphaMaxLinear;
	private Map <MethodEnum, Amount<Angle>> _alphaStall;
	private Map <MethodEnum, Double> _cLZero;
	private Map <MethodEnum, Double> _cLStar;
	private Map <MethodEnum, Double> _cLMax;
	private Map <MethodEnum, Amount<?>> _cLAlpha;
	private Map <MethodEnum, List<Double>> _liftCoefficient3DCurve;
	private Map <MethodEnum, List<Double>> _liftCoefficientDistribution;
	private Map <MethodEnum, List<Amount<Force>>> _liftDistribution;
	private Map <MethodEnum, List<Double>> _liftCoefficientDistributionBasicLoad;
	private Map <MethodEnum, List<Amount<Force>>> _basicLoadDistribution;
	private Map <MethodEnum, List<Double>> _liftCoefficientDistributionAdditionalLoad;
	private Map <MethodEnum, List<Amount<Force>>> _additionalLoadDistribution;
	private Map <MethodEnum, List<Amount<Length>>> _cclDistributionBasicLoad;
	private Map <MethodEnum, List<Amount<Length>>> _cclDistributionAdditionalLoad;
	private Map <MethodEnum, List<Amount<Length>>> _cclDistribution;
	private Map <MethodEnum, List<Double>> _gammaDistributionBasicLoad;
	private Map <MethodEnum, List<Double>> _gammaDistributionAdditionalLoad;
	private Map <MethodEnum, List<Double>> _gammaDistribution;
	
	// HIGH LIFT
	private Map <MethodEnum, Amount<Angle>> _alphaZeroLiftHighLift;
	private Map <MethodEnum, Amount<Angle>> _alphaStarHighLift;
	private Map <MethodEnum, Amount<Angle>> _alphaStallHighLift;
	private Map <MethodEnum, Double> _cLZeroHighLift;
	private Map <MethodEnum, Double> _cLStarHighLift;
	private Map <MethodEnum, Double> _cLMaxHighLift;
	private Map <MethodEnum, Amount<?>> _cLAlphaHighLift;
	private List<Double> _deltaCl0FlapList;
	private Double _deltaCl0Flap;
	private List<Double> _deltaCL0FlapList;
	private Double _deltaCL0Flap;
	private List<Double> _deltaClmaxFlapList;
	private Double _deltaClmaxFlap;
	private List<Double> _deltaCLmaxFlapList;
	private Double _deltaCLmaxFlap;
	private List<Double> _deltaClmaxSlatList;
	private Double _deltaClmaxSlat;
	private List<Double> _deltaCLmaxSlatList;
	private Double _deltaCLmaxSlat;
	private List<Double> _deltaCDList;
	private Double _deltaCD;
	private List<Double> _deltaCMc4List;
	private Double _deltaCMc4;
	
	// DRAG -> TODO: DEFINE VARIABLES
	
	
	// PITCHING MOMENT -> TODO: DEFINE VARIABLES
	
	
	//------------------------------------------------------------------------------
	// CONSTRUCTOR
	//------------------------------------------------------------------------------
	public LSAerodynamicsCalculator (
			LiftingSurface theLiftingSurface,
			OperatingConditions theOperatingConditions,
			Map <String, MethodEnum> taskMap
			) {
		
		this._theLiftingSurface = theLiftingSurface;
		this._theOperatingConditions = theOperatingConditions;
		this._taskMap = taskMap;
		
		initializeData();
		initializeVariables();
		// TODO: ADD INITIALIZE CALCULATORS
		
	}
	
	//------------------------------------------------------------------------------
	// METHODS
	//------------------------------------------------------------------------------
	private void initializeData() {
		
		this._currentMachNumber = this._theOperatingConditions.getMachCurrent();
		this._numberOfAlphas = this._theOperatingConditions.getAlpha().length;
		this._alphaArray = new ArrayList<Amount<Angle>>();
		for(int i=0; i<this._theOperatingConditions.getAlpha().length; i++)
			this._alphaArray.add(Amount.valueOf(
					this._theOperatingConditions.getAlpha()[i],
					NonSI.DEGREE_ANGLE)
					);
		
		// TODO : ADD OTHER REQUIRED DATA (if necessary)
		
	}
	
	private void initializeVariables() {
		
		this._criticalMachNumber = new HashMap<MethodEnum, Double>();
		
		this._alphaZeroLift = new HashMap<MethodEnum, Amount<Angle>>();
		this._alphaStar = new HashMap<MethodEnum, Amount<Angle>>();
		this._alphaMaxLinear = new HashMap<MethodEnum, Amount<Angle>>();
		this._alphaStall = new HashMap<MethodEnum, Amount<Angle>>();
		this._cLZero = new HashMap<MethodEnum, Double>();
		this._cLStar = new HashMap<MethodEnum, Double>();
		this._cLMax = new HashMap<MethodEnum, Double>();
		this._cLAlpha = new HashMap<MethodEnum, Amount<?>>();
		this._liftCoefficient3DCurve = new HashMap<MethodEnum, List<Double>>();
		this._liftCoefficientDistribution = new HashMap<MethodEnum, List<Double>>();
		this._liftDistribution = new HashMap<MethodEnum, List<Amount<Force>>>();
		this._liftCoefficientDistributionBasicLoad = new HashMap<MethodEnum, List<Double>>();
		this._basicLoadDistribution = new HashMap<MethodEnum, List<Amount<Force>>>();
		this._liftCoefficientDistributionAdditionalLoad = new HashMap<MethodEnum, List<Double>>();
		this._additionalLoadDistribution = new HashMap<MethodEnum, List<Amount<Force>>>();
		this._cclDistributionBasicLoad = new HashMap<MethodEnum, List<Amount<Length>>>();
		this._cclDistributionAdditionalLoad = new HashMap<MethodEnum, List<Amount<Length>>>();
		this._cclDistribution = new HashMap<MethodEnum, List<Amount<Length>>>();
		this._gammaDistributionBasicLoad = new HashMap<MethodEnum, List<Double>>();
		this._gammaDistributionAdditionalLoad = new HashMap<MethodEnum, List<Double>>();
		this._gammaDistribution = new HashMap<MethodEnum, List<Double>>();
		
		this._alphaZeroLiftHighLift = new HashMap<MethodEnum, Amount<Angle>>();
		this._alphaStarHighLift = new HashMap<MethodEnum, Amount<Angle>>();
		this._alphaStallHighLift = new HashMap<MethodEnum, Amount<Angle>>();
		this._cLZeroHighLift = new HashMap<MethodEnum, Double>();
		this._cLStarHighLift = new HashMap<MethodEnum, Double>();
		this._cLMaxHighLift = new HashMap<MethodEnum, Double>();
		this._cLAlphaHighLift = new HashMap<MethodEnum, Amount<?>>();
		this._deltaCl0FlapList = new ArrayList<>();
		this._deltaCL0FlapList = new ArrayList<>();
		this._deltaClmaxFlapList = new ArrayList<>();
		this._deltaCLmaxFlapList = new ArrayList<>();
		this._deltaClmaxSlatList = new ArrayList<>();
		this._deltaCLmaxSlatList = new ArrayList<>();
		this._deltaCDList = new ArrayList<>();
		this._deltaCMc4List = new ArrayList<>();
		
		// TODO : CONTINUE WITH OTHER MAPS WHEN AVAILABLE
		
	}
	
	//............................................................................
	// CRITICAL MACH INNER CLASS
	//............................................................................
	/** 
	 * Calculate the lifting surface critical Mach number
	 * 
	 * @author Lorenzo Attanasio, Vittorio Trifari
	 */
	public class CalcMachCr {
		/** 
		 * Korn-Mason method for estimating critical mach number
		 *
		 * @author Lorenzo Attanasio
		 * @see Sforza (2014), page 417
		 */
		public void kornMason(double cL) {

			AirfoilTypeEnum airfoilType = _theLiftingSurface.getAirfoilList().get(0).getType();
			Amount<Angle> sweepHalfChordEq = _theLiftingSurface.getSweepHalfChordEquivalent(false);
			double maxThicknessMean = _theLiftingSurface.getThicknessMean();
			
			double machCr = AerodynamicCalc.calculateMachCriticalKornMason(
					cL,
					sweepHalfChordEq,
					maxThicknessMean, 
					airfoilType);

			_criticalMachNumber.put(MethodEnum.KORN_MASON, machCr);
		}

		/**
		 * This method allows users to calculate the crest critical Mach number using the 
		 * Kroo graph which adapts the Shevell graph for swept wing. From this graph the following
		 * equation has been derived (see CIORNEI, Simona: Mach Number, Relative Thickness, Sweep 
		 * and Lift Coefficient Of The Wing – An Empirical Investigation of Parameters and Equations.
		 * Hamburg University of Applied Sciences, Department of Automotive and Aeronautical 
		 * Engineering, Project, 2005). Furthermore a correction for the modern supercritical 
		 * airfoils have been added in order to make results more reliable.
		 * 
		 * @author Vittorio Trifari
		 * @param cL
		 * @param sweepHalfChord
		 * @param tcMax
		 * @param airfoilType
		 * @return m_cr the crest critical Mach number from Kroo equation (2001)
		 */
		public void kroo(double cL) {

			// sweepHalfChord --> radians are required
			AirfoilTypeEnum airfoilType = _theLiftingSurface.getAirfoilList().get(0).getType();
			Amount<Angle> sweepHalfChordEq = _theLiftingSurface.getSweepHalfChordEquivalent(false);
			double maxThicknessMean = _theLiftingSurface.getThicknessMean();
			
			double machCr = AerodynamicCalc.calculateMachCriticalKroo(
					cL,
					sweepHalfChordEq,
					maxThicknessMean,
					airfoilType
					);
			
			_criticalMachNumber.put(MethodEnum.KROO, machCr);
		}

		public void allMethods(double cL) {
			kornMason(cL);
			kroo(cL);
		}
	}
	//............................................................................
	// END OF THE CRITICAL MACH INNER CLASS
	//............................................................................

	//............................................................................
	// CL AT APLHA INNER CLASS
	//............................................................................
	public class CalcCLAtAlpha {

		private double cL;
		private double alphaZeroLiftDeflection;
		private double[] alphaTailArray;
		int nPoints = 20;
		
		double [] alphaArrayHTailPlot = new double [nPoints];
		
		public double[] getAlphaArrayHTailPlot() {
			return alphaArrayHTailPlot;
		}
		public void setAlphaArrayHTailPlot(double[] alphaArrayHTailPlot) {
			this.alphaArrayHTailPlot = alphaArrayHTailPlot;
		}

		public double linearDLR(double alpha) {
			// page 3 DLR pdf
			return LiftCalc.calcCLatAlphaLinearDLR(
					alpha,
					_theLiftingSurface.getAspectRatio()
					);
		}

		/** 
		 * Evaluate CL at a specific AoA
		 * 
		 * @author Lorenzo Attanasio
		 * @return
		 */
		public double linearAndersonCompressibleSubsonic(Amount<Angle> alpha) {

			double cL = calculateCLAlpha.andersonSweptCompressibleSubsonic()
					*alpha.to(SI.RADIAN).getEstimatedValue() + 
					calculateCL0();

			_cLCurrent = cL;

			cLMap.getcXVsAlphaTable().put(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC, alpha, _cLCurrent);

			return cL;

		}

		public double nasaBlackwell(Amount<Angle> alpha) {

			calculateLiftDistribution.getNasaBlackwell().calculate(alpha);

			cLMap.getcXVsAlphaTable().put(MethodEnum.NASA_BLACKWELL, alpha, calculateLiftDistribution.getNasaBlackwell().get_cLCurrent());
			cLMap.getCxyVsAlphaTable().put(MethodEnum.NASA_BLACKWELL, alpha, calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().clone());
			cLMap.getCcxyVsAlphaTable().put(MethodEnum.NASA_BLACKWELL, alpha, calculateLiftDistribution.getNasaBlackwell().get_ccLDistribution().clone());

			cL = calculateLiftDistribution.getNasaBlackwell().get_cLEvaluated();
			_cLCurrent = cL;

			return cL;
		}

		/**
		 * This method calculates CL at alpha given as input. It interpolates the values of cl and alpha array filled before.
		 * WARNING: it is necessary to call the method CalcCLvsAlphaCurve--> nasaBlackwellCompleteCurve before.
		 * 
		 * @author Manuela Ruocco
		 *
		 */		
		public double nasaBlackwellCompleteCurve(Amount<Angle> alpha){
			
			if (alpha.getUnit() == NonSI.DEGREE_ANGLE) 
				alpha = alpha.to(SI.RADIAN);

			double  [] clValue = getcLActualArray();
			double [] alphaArray = getAlphaArrayActual();
			
			double cLAtAlpha = MyMathUtils.getInterpolatedValue1DLinear(alphaArray,clValue, alpha.getEstimatedValue());
			
			return cLAtAlpha;
		}

		public double nasaBlackwellCompleteCurveValue(Amount<Angle> alpha){	
			
			double cLStar, cLTemp, qValue, a ,b ,c ,d;
			Amount<Angle> alphaTemp = Amount.valueOf(0.0, SI.RADIAN);
			
			Airfoil meanAirfoil = new Airfoil(
					getTheLiftingSurface().calculateMeanAirfoil(getTheLiftingSurface()),
					getTheLiftingSurface().getAerodynamicDatabaseReader()
					);
			double alphaStar = meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue();
			set_alphaStar(Amount.valueOf(alphaStar,SI.RADIAN));
			Amount<Angle> alphaStarAmount = Amount.valueOf(alphaStar, SI.RADIAN);

			cLStarWing = nasaBlackwell(alphaStarAmount);
			cLTemp = nasaBlackwell(alphaTemp);
			if (alpha.getEstimatedValue() < alphaStar){    //linear trait
				cLLinearSlope = (cLStarWing - cLTemp)/alphaStar;
				//System.out.println("CL Linear Slope [1/rad] = " + cLLinearSlope);
				qValue = cLStarWing - cLLinearSlope*alphaStar;
				cLAlphaZero = qValue;
				alphaZeroLiftWingClean = -qValue/cLLinearSlope;
				double cLActual = cLLinearSlope * alpha.getEstimatedValue() + qValue;
				//System.out.println(" CL Actual = " + cLActual );
				return cLActual;
			}
			else {  // non linear trait
				calcAlphaAndCLMax(meanAirfoil);
				double cLMax = get_cLMaxClean();
				Amount<Angle> alphaMax = getAlphaMaxClean();	
				double alphaMaxDouble = alphaMax.getEstimatedValue();

				cLLinearSlope = (cLStarWing - cLTemp)/alphaStar;
				//System.out.println("CL Linear Slope [1/rad] = " + cLLinearSlope);
				double[][] matrixData = { {Math.pow(alphaMaxDouble, 3), Math.pow(alphaMaxDouble, 2), alphaMaxDouble,1.0},
						{3* Math.pow(alphaMaxDouble, 2), 2*alphaMaxDouble, 1.0, 0.0},
						{3* Math.pow(alphaStar, 2), 2*alphaStar, 1.0, 0.0},
						{Math.pow(alphaStar, 3), Math.pow(alphaStar, 2),alphaStar,1.0}};
				RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
				double [] vector = {cLMax, 0,cLLinearSlope, cLStarWing};

				double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

				a = solSystem[0];
				b = solSystem[1];
				c = solSystem[2];
				d = solSystem[3];

				double clActual = a * Math.pow(alpha.getEstimatedValue(), 3) + 
						b * Math.pow(alpha.getEstimatedValue(), 2) + 
						c * alpha.getEstimatedValue() + d;

				return clActual;
			}							
		}

		public void allMethods(Amount<Angle> alpha) {
			linearAndersonCompressibleSubsonic(alpha);
			nasaBlackwell(alpha);
		}

		/**
		 * This method calculates CL at alpha given as input. This method calculates both linear trait and 
		 * non linear trait. It use the NasaBlackwell method in order to evaluate the slope of the linear trait
		 * and it builds the non-linear trait using a cubic interpolation. 
		 * 
		 * @author Manuela Ruocco
		 * @param Amount<Angle> alphaBody. It is the angle of attack between the direction of asimptotic 
		 * velocity and the reference line of fuselage.
		 */		
		public double nasaBlackwellAlphaBody(Amount<Angle> alphaBody){
			if (alphaBody.getUnit() == NonSI.DEGREE_ANGLE) 
				alphaBody = alphaBody.to(SI.RADIAN);

			Amount<Angle> angleOfIncidence = _theLiftingSurface.getRiggingAngle();
			Amount<Angle> alphaWing = Amount.valueOf(
					alphaBody.getEstimatedValue() +
					angleOfIncidence.getEstimatedValue(), SI.RADIAN);

			double cLWing = nasaBlackwellCompleteCurve(alphaWing);
			return cLWing;
		}

		/**
		 * This method calculates CL at alpha given as input. This method calculates both linear trait and 
		 * non linear trait. It use the NasaBlackwell method in order to evaluate the slope of the linear trait
		 * and it builds the non-linear trait using a cubic interpolation. This is an overload of previous method
		 * that accepts in input the downwash angle. 
		 * 
		 * @author Manuela Ruocco
		 * @param Amount<Angle> alphaBody. It is the angle of attack between the direction of asimptotic 
		 * velocity and the reference line of fuselage.
		 * @param Amount<Angle> downwash. 
		 */	
		public double nasaBlackwellalphaBody(Amount<Angle> alphaBody,Amount<Angle> downwash ){
			if (alphaBody.getUnit() == NonSI.DEGREE_ANGLE) 
				alphaBody = alphaBody.to(SI.RADIAN);

			if (downwash.getUnit() == NonSI.DEGREE_ANGLE) 
				alphaBody = alphaBody.to(SI.RADIAN);

			Amount<Angle> angleOfIncidence = _theLiftingSurface.getRiggingAngle();
			Amount<Angle> alphaWing = Amount.valueOf(
					alphaBody.getEstimatedValue() +
					angleOfIncidence.getEstimatedValue()- downwash.getEstimatedValue()
					,SI.RADIAN);

			double cLWing = nasaBlackwellCompleteCurveValue(alphaWing);
			return cLWing;
		}

		/**
		 * This method evaluates CL for an alpha array having the elevator deflection as input. 
		 * 
		 * @param angle of deflection of the elevator in deg or radians
		 * @param chord ratio -> cf_c
		 *
		 * @return Cl array for fixed deflection
		 * @author  Manuela Ruocco
		 */

		// The calculation of the lift coefficient with a deflection of the elevator is made by the
		// method calculateCLWithElevatorDeflection. This method fills the array of 20 value of CL in
		// its linear trait. 
		// The procedure used to calculate the CL is the following. It's important to know that 
		// it's necessary to call the method nasaBlackwellCompleteCurve in order to obtain the
		// cl linear slope of the horizontal tail with no elevator deflection.
		//
		//1 . First of all the tau factor it's calculated used the method calculateTauIndex in the .. class
		//2. It's necessary to get the linear slope of the horizontal tail with no elevator defletion.
		// 3. The alphazero lift of the deflected configuration is calculated with this formula
		//   alphaw= tau per delta e
		// 4. At this point it's possible to create an alpha array, starting from the alpha zero lift, until
		//   15 degrees after.
		// 5. the value of cl for each alpha is calculated with the formula :
		//  cl alfa * (alfa + t delta e)

		public double[] calculateCLWithElevatorDeflection (
				List<Double[]> deltaFlap,
				List<FlapTypeEnum> flapType,
				List<Double> deltaSlat,
				List<Double> etaInFlap,
				List<Double> etaOutFlap,
				List<Double> etaInSlat,
				List<Double> etaOutSlat, 
				List<Double> cfc,
				List<Double> csc,
				List<Double> leRadiusSlatRatio,
				List<Double> cExtcSlat
				) {

			// variable declaration
			Amount<Angle> deflection = Amount.valueOf(deltaFlap.get(0)[0], NonSI.DEGREE_ANGLE);
			
			if (deflection.getUnit() == SI.RADIAN){
				deflection = deflection.to(NonSI.DEGREE_ANGLE);
			}
			double deflectionAngleDeg = deflection.getEstimatedValue();

			int nPoints = 20;
			double tauValue, cLLinearSlopeNoDeflection;
			alphaTailArray = new double [nPoints];
			double[] cLArray = new double [nPoints];
			Amount<Angle> alphaActual;
			double alphaStallElevator;
			double deltaCLMaxElevator;
			
			//linear trait
			StabilityCalculators theStablityCalculator = new StabilityCalculators();
			tauValue = theStablityCalculator.calculateTauIndex(cfc.get(0), theAircraft, deflection);
			cLLinearSlopeNoDeflection = getcLLinearSlopeNB()/57.3; //in deg 
			alphaZeroLiftDeflection = - tauValue*deflectionAngleDeg;
			alphaTailArray[0] = alphaZeroLiftDeflection;
			double qValue = - cLLinearSlopeNoDeflection *  alphaZeroLiftDeflection;
			double alphaTemp = 1;
			
			cLArray[0] = 0;
			cLArray[1] = cLLinearSlopeNoDeflection * alphaTemp + qValue;

			double clAlpha = (cLArray[1] - cLArray[0])/Math.abs(alphaTemp-alphaTailArray[0]);
			
			// non linear trait
			CalcHighLiftDevices theHighLiftCalculatorLiftEffects = new CalcHighLiftDevices(
					_theLiftingSurface, 
					_theOperatingConditions,
					deltaFlap, 
					flapType, 
					deltaSlat, 
					etaInFlap,
					etaOutFlap,
					etaInSlat,
					etaOutSlat, 
					cfc, 
					csc, 
					leRadiusSlatRatio, 
					cExtcSlat);
			
			theHighLiftCalculatorLiftEffects.calculateHighLiftDevicesEffects();
			
			deltaCLMaxElevator = theHighLiftCalculatorLiftEffects.getDeltaCLmax_flap()*tauValue;
			double deltaAlphaMaxElevator =-(tauValue * deflection.getEstimatedValue())/2;
			double deltaAlphaMaxElevatordelta = theHighLiftCalculatorLiftEffects.getDeltaAlphaMaxFlap();
			CalcCLAtAlpha theCLCleanCalculator = new CalcCLAtAlpha();
			
			Airfoil meanAirfoil = new Airfoil(
					LiftingSurface.calculateMeanAirfoil(getTheLiftingSurface()),
					getTheLiftingSurface().getAerodynamicDatabaseReader()
					); 
					
			calcAlphaAndCLMax(meanAirfoil);
			Amount<Angle> alphaMax = getAlphaMaxClean().to(NonSI.DEGREE_ANGLE);
			double alphaStarClean = meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue();
			Amount<Angle> alphaStarCleanAmount = Amount.valueOf(alphaStarClean, SI.RADIAN);
			double cLMax = get_cLMaxClean();
			double cLStarClean = theCLCleanCalculator.nasaBlackwellCompleteCurve(alphaStarCleanAmount);
			double cL0Elevator = cLArray[0];
			double alphaStar = (cLStarClean - qValue)/clAlpha;
			alphaStarClean = alphaStarClean*57.3;		
			double cLMaxElevator = cLMax + deltaCLMaxElevator;
			
				alphaStallElevator = alphaMax.getEstimatedValue() + deltaAlphaMaxElevator;
			
			double alphaStarElevator; 

				alphaStarElevator = (alphaStar + alphaStarClean)/2;
			
			cLArray[1] = cLLinearSlopeNoDeflection * (alphaStarElevator + tauValue * deflectionAngleDeg);
			
			double[][] matrixData = { {Math.pow(alphaStallElevator, 3), Math.pow(alphaStallElevator, 2),
				alphaStallElevator,1.0},
					{3* Math.pow(alphaStallElevator, 2), 2*alphaStallElevator, 1.0, 0.0},
					{3* Math.pow(alphaStarElevator, 2), 2*alphaStarElevator, 1.0, 0.0},
					{Math.pow(alphaStarElevator, 3), Math.pow(alphaStarElevator, 2),alphaStarElevator,1.0}};
			RealMatrix m = MatrixUtils.createRealMatrix(matrixData);

			double [] vector = {cLMaxElevator, 0,clAlpha, cLArray[1]};

			double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

			double a = solSystem[0];
			double b = solSystem[1];
			double c = solSystem[2];
			double d = solSystem[3];

			alphaTailArray = MyArrayUtils.linspace(alphaStarElevator,
					alphaStallElevator+ 4,
					nPoints-1);

			double[] cLArrayHighLiftPlot = new double [nPoints];

			for ( int i=0 ; i< alphaTailArray.length ; i++){
				alphaActual = Amount.valueOf((alphaTailArray[i]), NonSI.DEGREE_ANGLE);
				if (alphaActual.getEstimatedValue() <= alphaStarElevator) { 
					cLArray[i+1] = clAlpha * alphaActual.getEstimatedValue() + qValue;}
				else {
					cLArray[i+1] = a * Math.pow(alphaActual.getEstimatedValue(), 3) + 
							b * Math.pow(alphaActual.getEstimatedValue(), 2) + 
							c * alphaActual.getEstimatedValue() + d;
				}

			}
					
			alphaArrayHTailPlot[0] = alphaZeroLiftDeflection;
			for(int i=1; i<nPoints ; i++){
				alphaArrayHTailPlot[i]= alphaTailArray[i-1];
			}
			
			return cLArray;
		}
	
		public Double[] getAlphaTailArrayDouble() {
			Double[]alphaTailArrayDouble = new Double [getAlphaTailArray().length];
			for ( int i=0; i<getAlphaTailArray().length; i++){
				alphaTailArrayDouble [i] = (Double)getAlphaTailArray()[i];
			}
			return alphaTailArrayDouble;
		}

		public double[] getAlphaTailArray() {
			return alphaTailArray;
		}

		public void setAlphaTailArray(double[] alphaTailArray) {
			this.alphaTailArray = alphaTailArray;
		}

		/**
		 * This method calculates CL of an horizontal tail at alpha given as input. This method calculates linear trait 
		 * considering a known elevator deflection. It use the NasaBlackwell method in order to evaluate the slope of the linear trait
		 * This method needs that the field of cl has filled before. --> Need to call calculateCLWithElevatorDeflection
		 * before!
		 * 
		 * @author Manuela Ruocco
		 * @param Amount<Angle> alphaBody. It is the angle of attack between the direction of asimptotic 
		 * velocity and the reference line of fuselage.
		 * @param Amount<Angle> deflection of elevator in degree. 
		 */	

		// In ordet to obtain a value of lift coefficient corresponding at an alpha body with a known
		// elevator deflection it's possible to use the method getCLHTailatAlphaBodyWithElevator.

		public double getCLHTailatAlphaBodyWithElevator (double chordRatio,
				Amount<Angle> alphaBody,
				Amount<Angle> deflection,
				Amount<Angle> downwashAngle,
				List<Double[]> deltaFlap,
				List<FlapTypeEnum> flapType,
				List<Double> deltaSlat,
				List<Double> etaInFlap,
				List<Double> etaOutFlap,
				List<Double> etaInSlat,
				List<Double> etaOutSlat, 
				List<Double> cfc,
				List<Double> csc,
				List<Double> leRadiusSlatRatio,
				List<Double> cExtcSlat
				){

			if (alphaBody.getUnit() == SI.RADIAN)
				alphaBody = alphaBody.to(NonSI.DEGREE_ANGLE);
			double alphaBodyDouble = alphaBody.getEstimatedValue();

			if (downwashAngle.getUnit() == SI.RADIAN)
				downwashAngle = downwashAngle.to(NonSI.DEGREE_ANGLE);
			double downwashAngleDeg = downwashAngle.getEstimatedValue();


			double deflectionAngleDeg = deflection.getEstimatedValue();
			double alphaZeroLift = getAlphaZeroLiftDeflection();

			double[] alphaLocalArray = getAlphaArrayHTailPlot();

			double alphaLocal = alphaBodyDouble 
					- downwashAngleDeg 
					+ theAircraft.getHTail().getRiggingAngle().to(NonSI.DEGREE_ANGLE).getEstimatedValue();

			double[]  clArray = calculateCLWithElevatorDeflection(
					deltaFlap, flapType,deltaSlat,
					etaInFlap, etaOutFlap, etaInSlat, etaOutSlat, 
					 cfc, csc, leRadiusSlatRatio, cExtcSlat
					);
			double clAtAlpha = MyMathUtils.getInterpolatedValue1DLinear(alphaLocalArray, clArray, alphaLocal);

			return clAtAlpha;
		}
	}
	//............................................................................
	// END OF THE CRITICAL MACH INNER CLASS
	//............................................................................
	
	//............................................................................
	// CLalpha INNER CLASS
	//............................................................................
	/** 
	 * Calculate the lift coefficient gradient of the whole lifting surface.
	 * The class hold all available methods to estimate such gradient
	 * (1/rad)
	 * 
	 * @author Lorenzo Attanasio, Manuela Ruocco
	 */
	public class CalcCLAlpha {

		/**
		 * This function determines the linear trait slope of the CL-alpha curve using the NasaBlackwell method.
		 * It evaluate CL wing in correspondence of two alpha and calculates the equation of the line.
		 * 
		 * @author Manuela Ruocco
		 * @param LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha
		 */  

		// TODO : FIX THE CALC CL AT ALPHA INNER CLASS AND CREATE THE OBJECT INSIDE THE METHOD.
		public double nasaBlackwell(LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha){
			
			Amount<Angle> alphaOne = Amount.valueOf(toRadians(0.), SI.RADIAN);
			double clOne = theCLatAlpha.nasaBlackwell(alphaOne);

			Amount<Angle>alphaTwo = Amount.valueOf(toRadians(4.), SI.RADIAN);
			double alphaTwoDouble = 4.0;
			double clTwo = theCLatAlpha.nasaBlackwell(alphaTwo);

			double cLSlope = (clTwo-clOne)/alphaTwo.getEstimatedValue();
			double cLSlopeDeg = Math.toRadians(cLSlope);

			//  TODO : q ??
			double q = clTwo- cLSlopeDeg* alphaTwoDouble;
			return cLSlope;
		}
		
		public void polhamus() {

			// TODO : ??
//			_kPolhamus = LiftCalc.kFactorPolhamus(
//					_theLiftingSurface.getAspectRatio(),
//					_theOperatingConditions.getMachCurrent(), 
//					_theLiftingSurface.getSweepLEEquivalent(false),
//					_theLiftingSurface.getTaperRatioEquivalent(false)
//					);

			_cLAlpha.put(MethodEnum.POLHAMUS,
					Amount.valueOf(
							LiftCalc.calculateCLalphaPolhamus(
									_theLiftingSurface.getAspectRatio(),
									_theOperatingConditions.getMachCurrent(), 
									_theLiftingSurface.getSweepLEEquivalent(false),
									_theLiftingSurface.getTaperRatioEquivalent(false)
									),
							SI.RADIAN
							).inverse()
					);
		}

		/**
		 * pag. 49 ADAS
		 */
		public void andersonSweptCompressibleSubsonic() {

			_cLAlpha.put(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC,
					LiftCalc.calcCLalphaAndersonSweptCompressibleSubsonic(
							_theOperatingConditions.getMachCurrent(),
							_theLiftingSurface.getAspectRatio(),
							_theLiftingSurface.getSemiSpan(),
							_theLiftingSurface.getSweepHalfChordEquivalent(false), 
							_theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedYs(),
							// TODO : ADD THE CALCULATION OF ALL THE AIRFOIL PARAMETERS ON THE DISCRETIZED Y STATIONSS
							_clAlphaVsY.toArray(),
							_theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedChords()
							)
					);
		}

		/** 
		 * This method gets called by andersonSweptCompressibleSubsonic
		 *
		 * @author Lorenzo Attanasio
		 * @return
		 */
		public void integralMean2D() {
			_cLAlpha.put(
					MethodEnum.INTEGRAL_MEAN, 
					LiftCalc.calcCLalphaIntegralMean2D(
							_theLiftingSurface.getSurface(),
							_theLiftingSurface.getSemiSpan(),
							_theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedYs(), 
							// TODO : ADD THE CALCULATION OF ALL THE AIRFOIL PARAMETERS ON THE DISCRETIZED Y STATIONSS
							_clAlphaVsY.toArray(),
							_theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedChords()
							)
					);
		}

		public void allMethods() {
			polhamus();
			andersonSweptCompressibleSubsonic();
			integralMean2D();
//			nasaBlackwell(theCLatAlpha)
		}

	}
	//............................................................................
	// END OF THE CLalpha INNER CLASS
	//............................................................................
	
	//............................................................................
	// HIGH LIFT INNER CLASS
	//............................................................................
	public class CalcHighLift {
		
		// TODO : PUT HERE ALL THE METHODS OF THE CALCHIGHLIFT CLASS 
		
	}	
	//............................................................................
	// END OF THE HIGH LIFT INNER CLASS
	//............................................................................

	//------------------------------------------------------------------------------
	// GETTERS & SETTERS
	//------------------------------------------------------------------------------
	public LiftingSurface getTheLiftingSurface() {
		return _theLiftingSurface;
	}
	public void setTheLiftingSurface(LiftingSurface _theLiftingSurface) {
		this._theLiftingSurface = _theLiftingSurface;
	}
	public OperatingConditions getTheOperatingConditions() {
		return _theOperatingConditions;
	}
	public void setTheOperatingConditions(OperatingConditions _theOperatingConditions) {
		this._theOperatingConditions = _theOperatingConditions;
	}
	public Map <String, MethodEnum> getTaskMap() {
		return _taskMap;
	}
	public void setTaskMap(Map <String, MethodEnum> _taskMap) {
		this._taskMap = _taskMap;
	}
	public int getNumberOfPointSemiSpanWise() {
		return _numberOfPointSemiSpanWise;
	}
	public void setNumberOfPointSemiSpanWise(int _numberOfPointSemiSpanWise) {
		this._numberOfPointSemiSpanWise = _numberOfPointSemiSpanWise;
	}
	public int getNumberOfAlphas() {
		return _numberOfAlphas;
	}
	public void setNumberOfAlphas(int _numberOfAlphas) {
		this._numberOfAlphas = _numberOfAlphas;
	}
	public List<Amount<Angle>> getAlphaArray() {
		return _alphaArray;
	}
	public void setAlphaArray(List<Amount<Angle>> _alphaArray) {
		this._alphaArray = _alphaArray;
	}
	public Double getCurrentMachNumber() {
		return _currentMachNumber;
	}
	public void setCurrentMachNumber(Double _currentMachNumber) {
		this._currentMachNumber = _currentMachNumber;
	}
	public Map<MethodEnum, Double> getCriticalMachNumber() {
		return _criticalMachNumber;
	}
	public void setCriticalMachNumber(Map<MethodEnum, Double> _criticalMachNumber) {
		this._criticalMachNumber = _criticalMachNumber;
	}
	public Map<MethodEnum, Amount<Angle>> getAlphaZeroLift() {
		return _alphaZeroLift;
	}
	public void setAlphaZeroLift(Map<MethodEnum, Amount<Angle>> _alphaZeroLift) {
		this._alphaZeroLift = _alphaZeroLift;
	}
	public Map<MethodEnum, Amount<Angle>> getAlphaStar() {
		return _alphaStar;
	}
	public void setAlphaStar(Map<MethodEnum, Amount<Angle>> _alphaStar) {
		this._alphaStar = _alphaStar;
	}
	public Map<MethodEnum, Amount<Angle>> getAlphaMaxLinear() {
		return _alphaMaxLinear;
	}
	public void setAlphaMaxLinear(Map<MethodEnum, Amount<Angle>> _alphaMaxLinear) {
		this._alphaMaxLinear = _alphaMaxLinear;
	}
	public Map<MethodEnum, Amount<Angle>> getAlphaStall() {
		return _alphaStall;
	}
	public void setAlphaStall(Map<MethodEnum, Amount<Angle>> _alphaStall) {
		this._alphaStall = _alphaStall;
	}
	public Map<MethodEnum, Double> getCLZero() {
		return _cLZero;
	}
	public void setCLZero(Map<MethodEnum, Double> _cLZero) {
		this._cLZero = _cLZero;
	}
	public Map<MethodEnum, Double> getCLStar() {
		return _cLStar;
	}
	public void setCLStar(Map<MethodEnum, Double> _cLStar) {
		this._cLStar = _cLStar;
	}
	public Map<MethodEnum, Double> getCLMax() {
		return _cLMax;
	}
	public void setCLMax(Map<MethodEnum, Double> _cLMax) {
		this._cLMax = _cLMax;
	}
	public Map<MethodEnum, Amount<?>> getCLAlpha() {
		return _cLAlpha;
	}
	public void setCLAlpha(Map<MethodEnum, Amount<?>> _cLAlpha) {
		this._cLAlpha = _cLAlpha;
	}
	public Map<MethodEnum, List<Double>> getLiftCoefficient3DCurve() {
		return _liftCoefficient3DCurve;
	}
	public void setLiftCoefficient3DCurve(Map<MethodEnum, List<Double>> _liftCoefficient3DCurve) {
		this._liftCoefficient3DCurve = _liftCoefficient3DCurve;
	}
	public Map<MethodEnum, List<Double>> getLiftCoefficientDistribution() {
		return _liftCoefficientDistribution;
	}
	public void setLiftCoefficientDistribution(Map<MethodEnum, List<Double>> _liftCoefficientDistribution) {
		this._liftCoefficientDistribution = _liftCoefficientDistribution;
	}
	public Map<MethodEnum, List<Amount<Force>>> getLiftDistribution() {
		return _liftDistribution;
	}
	public void setLiftDistribution(Map<MethodEnum, List<Amount<Force>>> _liftDistribution) {
		this._liftDistribution = _liftDistribution;
	}
	public Map<MethodEnum, List<Double>> getLiftCoefficientDistributionBasicLoad() {
		return _liftCoefficientDistributionBasicLoad;
	}
	public void setLiftCoefficientDistributionBasicLoad(
			Map<MethodEnum, List<Double>> _liftCoefficientDistributionBasicLoad) {
		this._liftCoefficientDistributionBasicLoad = _liftCoefficientDistributionBasicLoad;
	}
	public Map<MethodEnum, List<Amount<Force>>> getBasicLoadDistribution() {
		return _basicLoadDistribution;
	}
	public void setBasicLoadDistribution(Map<MethodEnum, List<Amount<Force>>> _basicLoadDistribution) {
		this._basicLoadDistribution = _basicLoadDistribution;
	}
	public Map<MethodEnum, List<Double>> getLiftCoefficientDistributionAdditionalLoad() {
		return _liftCoefficientDistributionAdditionalLoad;
	}
	public void setLiftCoefficientDistributionAdditionalLoad(
			Map<MethodEnum, List<Double>> _liftCoefficientDistributionAdditionalLoad) {
		this._liftCoefficientDistributionAdditionalLoad = _liftCoefficientDistributionAdditionalLoad;
	}
	public Map<MethodEnum, List<Amount<Force>>> getAdditionalLoadDistribution() {
		return _additionalLoadDistribution;
	}
	public void setAdditionalLoadDistribution(Map<MethodEnum, List<Amount<Force>>> _additionalLoadDistribution) {
		this._additionalLoadDistribution = _additionalLoadDistribution;
	}
	public Map<MethodEnum, List<Amount<Length>>> getCclDistributionBasicLoad() {
		return _cclDistributionBasicLoad;
	}
	public void setCclDistributionBasicLoad(Map<MethodEnum, List<Amount<Length>>> _cclDistributionBasicLoad) {
		this._cclDistributionBasicLoad = _cclDistributionBasicLoad;
	}
	public Map<MethodEnum, List<Amount<Length>>> getCclDistributionAdditionalLoad() {
		return _cclDistributionAdditionalLoad;
	}
	public void setCclDistributionAdditionalLoad(Map<MethodEnum, List<Amount<Length>>> _cclDistributionAdditionalLoad) {
		this._cclDistributionAdditionalLoad = _cclDistributionAdditionalLoad;
	}
	public Map<MethodEnum, List<Amount<Length>>> getCclDistribution() {
		return _cclDistribution;
	}
	public void setCclDistribution(Map<MethodEnum, List<Amount<Length>>> _cclDistribution) {
		this._cclDistribution = _cclDistribution;
	}
	public Map<MethodEnum, List<Double>> getGammaDistributionBasicLoad() {
		return _gammaDistributionBasicLoad;
	}
	public void setGammaDistributionBasicLoad(Map<MethodEnum, List<Double>> _gammaDistributionBasicLoad) {
		this._gammaDistributionBasicLoad = _gammaDistributionBasicLoad;
	}
	public Map<MethodEnum, List<Double>> getGammaDistributionAdditionalLoad() {
		return _gammaDistributionAdditionalLoad;
	}
	public void setGammaDistributionAdditionalLoad(Map<MethodEnum, List<Double>> _gammaDistributionAdditionalLoad) {
		this._gammaDistributionAdditionalLoad = _gammaDistributionAdditionalLoad;
	}
	public Map<MethodEnum, List<Double>> getGammaDistribution() {
		return _gammaDistribution;
	}
	public void setGammaDistribution(Map<MethodEnum, List<Double>> _gammaDistribution) {
		this._gammaDistribution = _gammaDistribution;
	}
	public Map<MethodEnum, Amount<Angle>> getAlphaZeroLiftHighLift() {
		return _alphaZeroLiftHighLift;
	}
	public void setAlphaZeroLiftHighLift(Map<MethodEnum, Amount<Angle>> _alphaZeroLiftHighLift) {
		this._alphaZeroLiftHighLift = _alphaZeroLiftHighLift;
	}
	public Map<MethodEnum, Amount<Angle>> getAlphaStarHighLift() {
		return _alphaStarHighLift;
	}
	public void setAlphaStarHighLift(Map<MethodEnum, Amount<Angle>> _alphaStarHighLift) {
		this._alphaStarHighLift = _alphaStarHighLift;
	}
	public Map<MethodEnum, Amount<Angle>> getAlphaStallHighLift() {
		return _alphaStallHighLift;
	}
	public void setAlphaStallHighLift(Map<MethodEnum, Amount<Angle>> _alphaStallHighLift) {
		this._alphaStallHighLift = _alphaStallHighLift;
	}
	public Map<MethodEnum, Double> getCLZeroHighLift() {
		return _cLZeroHighLift;
	}
	public void setCLZeroHighLift(Map<MethodEnum, Double> _cLZeroHighLift) {
		this._cLZeroHighLift = _cLZeroHighLift;
	}
	public Map<MethodEnum, Double> getCLStarHighLift() {
		return _cLStarHighLift;
	}
	public void setCLStarHighLift(Map<MethodEnum, Double> _cLStarHighLift) {
		this._cLStarHighLift = _cLStarHighLift;
	}
	public Map<MethodEnum, Double> getCLMaxHighLift() {
		return _cLMaxHighLift;
	}
	public void setCLMaxHighLift(Map<MethodEnum, Double> _cLMaxHighLift) {
		this._cLMaxHighLift = _cLMaxHighLift;
	}
	public Map<MethodEnum, Amount<?>> getCLAlphaHighLift() {
		return _cLAlphaHighLift;
	}
	public void setCLAlphaHighLift(Map<MethodEnum, Amount<?>> _cLAlphaHighLift) {
		this._cLAlphaHighLift = _cLAlphaHighLift;
	}
	public List<Double> getDeltaCl0FlapList() {
		return _deltaCl0FlapList;
	}
	public void setDeltaCl0FlapList(List<Double> _deltaCl0FlapList) {
		this._deltaCl0FlapList = _deltaCl0FlapList;
	}
	public Double getDeltaCl0Flap() {
		return _deltaCl0Flap;
	}
	public void setDeltaCl0Flap(Double _deltaCl0Flap) {
		this._deltaCl0Flap = _deltaCl0Flap;
	}
	public List<Double> getDeltaCL0FlapList() {
		return _deltaCL0FlapList;
	}
	public void setDeltaCL0FlapList(List<Double> _deltaCL0FlapList) {
		this._deltaCL0FlapList = _deltaCL0FlapList;
	}
	public Double getDeltaCL0Flap() {
		return _deltaCL0Flap;
	}
	public void setDeltaCL0Flap(Double _deltaCL0Flap) {
		this._deltaCL0Flap = _deltaCL0Flap;
	}
	public List<Double> getDeltaClmaxFlapList() {
		return _deltaClmaxFlapList;
	}
	public void setDeltaClmaxFlapList(List<Double> _deltaClmaxFlapList) {
		this._deltaClmaxFlapList = _deltaClmaxFlapList;
	}
	public Double getDeltaClmaxFlap() {
		return _deltaClmaxFlap;
	}
	public void setDeltaClmaxFlap(Double _deltaClmaxFlap) {
		this._deltaClmaxFlap = _deltaClmaxFlap;
	}
	public List<Double> getDeltaCLmaxFlapList() {
		return _deltaCLmaxFlapList;
	}
	public void setDeltaCLmaxFlapList(List<Double> _deltaCLmaxFlapList) {
		this._deltaCLmaxFlapList = _deltaCLmaxFlapList;
	}
	public Double getDeltaCLmaxFlap() {
		return _deltaCLmaxFlap;
	}
	public void setDeltaCLmaxFlap(Double _deltaCLmaxFlap) {
		this._deltaCLmaxFlap = _deltaCLmaxFlap;
	}
	public List<Double> getDeltaClmaxSlatList() {
		return _deltaClmaxSlatList;
	}
	public void setDeltaClmaxSlatList(List<Double> _deltaClmaxSlatList) {
		this._deltaClmaxSlatList = _deltaClmaxSlatList;
	}
	public Double getDeltaClmaxSlat() {
		return _deltaClmaxSlat;
	}
	public void setDeltaClmaxSlat(Double _deltaClmaxSlat) {
		this._deltaClmaxSlat = _deltaClmaxSlat;
	}
	public List<Double> getDeltaCLmaxSlatList() {
		return _deltaCLmaxSlatList;
	}
	public void setDeltaCLmaxSlatList(List<Double> _deltaCLmaxSlatList) {
		this._deltaCLmaxSlatList = _deltaCLmaxSlatList;
	}
	public Double getDeltaCLmaxSlat() {
		return _deltaCLmaxSlat;
	}
	public void setDeltaCLmaxSlat(Double _deltaCLmaxSlat) {
		this._deltaCLmaxSlat = _deltaCLmaxSlat;
	}
	public List<Double> getDeltaCDList() {
		return _deltaCDList;
	}
	public void setDeltaCDList(List<Double> _deltaCDList) {
		this._deltaCDList = _deltaCDList;
	}
	public Double getDeltaCD() {
		return _deltaCD;
	}
	public void setDeltaCD(Double _deltaCD) {
		this._deltaCD = _deltaCD;
	}
	public List<Double> getDeltaCMc4List() {
		return _deltaCMc4List;
	}
	public void setDeltaCMc4List(List<Double> _deltaCMc4List) {
		this._deltaCMc4List = _deltaCMc4List;
	}
	public Double getDeltaCMc4() {
		return _deltaCMc4;
	}
	public void setDeltaCMc4(Double _deltaCMc4) {
		this._deltaCMc4 = _deltaCMc4;
	}
}
