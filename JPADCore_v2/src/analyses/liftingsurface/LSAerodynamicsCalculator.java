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

import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.OperatingConditions;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.AnglesCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.NasaBlackwell;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.ComponentEnum;
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
	private Airfoil _meanAirfoil;
	private OperatingConditions _theOperatingConditions;
	private Map <String, List<MethodEnum>> _taskMap;
	private int _numberOfPointSemiSpanWise;
	private int _numberOfAlphas;
	private int _numberOfAlphasPlot;
	private double _vortexSemiSpanToSemiSpanRatio;
	private List<Amount<Angle>> _alphaArray;
	private Double[] _alphaArrayPlot; 
	private Double[] _alphaArrayPlotHighLift;
	private Double _currentMachNumber;
	private Double _currentLiftCoefficient;
	private double[] _etaStationDistribution; 
	private List<Amount<Length>> _yStationDistribution;
	private List<Amount<Angle>> _alphaZeroLiftDistribution;
	private List<Amount<Angle>> _twistDistribution;
	private List<Amount<Length>> _chordDistribution;
	private List<Amount<Length>> _ellipticalChordDistribution;
	private List<Amount<Angle>> _dihedralDistribution;
	private List<Amount<?>> _clAlphaDistribution;
	private List<Amount<Length>> _xLEDistribution;
	private List<Double> _clMaxDistribution;
	
	// CRITICAL MACH NUMBER
	private Map <MethodEnum, Double> _criticalMachNumber;
	
	// LIFT 
	private Map <MethodEnum, Double> _cLAtAplhaActual;
	private Map <MethodEnum, Amount<Angle>> _alphaZeroLift;
	private Map <MethodEnum, Amount<Angle>> _alphaStar;
	private Map <MethodEnum, Amount<Angle>> _alphaMaxLinear;
	private Map <MethodEnum, Amount<Angle>> _alphaStall;
	private Map <MethodEnum, Double> _cLZero;
	private Map <MethodEnum, Double> _cLStar;
	private Map <MethodEnum, Double> _cLMax;
	private Map <MethodEnum, Amount<?>> _cLAlpha;
	private Map <MethodEnum, Double[]> _liftCoefficient3DCurve;
	private Map <MethodEnum, double[]> _liftCoefficientDistributionAtCLMax;
	private Map <MethodEnum, List<List<Double>>> _liftCoefficientDistribution;
	private Map <MethodEnum, List<List<Amount<Force>>>> _liftDistribution;
	private Map <MethodEnum, List<List<Double>>> _liftCoefficientDistributionBasicLoad;
	private Map <MethodEnum, List<List<Amount<Force>>>> _basicLoadDistribution;
	private Map <MethodEnum, List<List<Double>>> _liftCoefficientDistributionAdditionalLoad;
	private Map <MethodEnum, List<List<Amount<Force>>>> _additionalLoadDistribution;
	private Map <MethodEnum, List<List<Amount<Length>>>> _cclDistributionBasicLoad;
	private Map <MethodEnum, List<List<Amount<Length>>>> _cclDistributionAdditionalLoad;
	private Map <MethodEnum, List<List<Amount<Length>>>> _cclDistribution;
	private Map <MethodEnum, List<List<Double>>> _gammaDistributionBasicLoad;
	private Map <MethodEnum, List<List<Double>>> _gammaDistributionAdditionalLoad;
	private Map <MethodEnum, List<List<Double>>> _gammaDistribution;
	
	// HIGH LIFT
	private Map <MethodEnum, Amount<Angle>> _alphaZeroLiftHighLift;
	private Map <MethodEnum, Amount<Angle>> _alphaStarHighLift;
	private Map <MethodEnum, Amount<Angle>> _alphaStallHighLift;
	private Map <MethodEnum, Double> _cLZeroHighLift;
	private Map <MethodEnum, Double> _cLStarHighLift;
	private Map <MethodEnum, Double> _cLMaxHighLift;
	private Map <MethodEnum, Amount<?>> _cLAlphaHighLift;
	
	//////////////////////////////////////////////
	//											//
	//		TODO: ALL DELTA HAVE TO BE MAPS	    //
	//											//
	//////////////////////////////////////////////
	
	private Map <MethodEnum, List<Double>> _deltaCl0FlapList;
	private Map <MethodEnum, Double> _deltaCl0Flap;
	private Map <MethodEnum, List<Double>> _deltaCL0FlapList;
	private Map <MethodEnum, Double> _deltaCL0Flap;
	private Map <MethodEnum, List<Double>> _deltaClmaxFlapList;
	private Map <MethodEnum, Double> _deltaClmaxFlap;
	private Map <MethodEnum, List<Double>> _deltaCLmaxFlapList;
	private Map <MethodEnum, Double> _deltaCLmaxFlap;
	private Map <MethodEnum, List<Double>> _deltaClmaxSlatList;
	private Map <MethodEnum, Double> _deltaClmaxSlat;
	private Map <MethodEnum, List<Double>> _deltaCLmaxSlatList;
	private Map <MethodEnum, Double> _deltaCLmaxSlat;
	private Map <MethodEnum, List<Double>> _deltaCDList;
	private Map <MethodEnum, Double> _deltaCD;
	private Map <MethodEnum, List<Double>> _deltaCMc4List;
	private Map <MethodEnum, Double> _deltaCMc4;
	
	// DRAG -> TODO: DEFINE VARIABLES
	
	
	// PITCHING MOMENT -> TODO: DEFINE VARIABLES
	
	
	//------------------------------------------------------------------------------
	// CONSTRUCTOR
	//------------------------------------------------------------------------------
	public LSAerodynamicsCalculator (
			LiftingSurface theLiftingSurface,
			OperatingConditions theOperatingConditions,
			Map <String, List<MethodEnum>> taskMap
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
		this._numberOfAlphasPlot = 50;
		this._numberOfPointSemiSpanWise = 50;
		this._vortexSemiSpanToSemiSpanRatio = 0.01;
		this._alphaArrayPlot = new Double[this._numberOfAlphasPlot];
		this._alphaArrayPlotHighLift = new Double[this._numberOfAlphasPlot];
		this._alphaArray = new ArrayList<Amount<Angle>>();
		for(int i=0; i<this._theOperatingConditions.getAlpha().length; i++)
			this._alphaArray.add(Amount.valueOf(
					this._theOperatingConditions.getAlpha()[i],
					NonSI.DEGREE_ANGLE)
					);
		
		//----------------------------------------------------------------------------------------------------------------------
		// Calculating the mena airfoil
		//......................................................................................................................
		this._meanAirfoil = new Airfoil(
				LiftingSurface.calculateMeanAirfoil(_theLiftingSurface),
				this._theLiftingSurface.getAerodynamicDatabaseReader()
				);
		
		//----------------------------------------------------------------------------------------------------------------------
		// Calculating airfoil parameter distributions
		//......................................................................................................................
		// ETA STATIONS E Y STATIONS
		this._etaStationDistribution = MyArrayUtils.linspace(0, 1, _numberOfPointSemiSpanWise);
		this._yStationDistribution = new ArrayList<Amount<Length>>();
		double[] yStationDistributionArray = MyArrayUtils.linspace(
				0,
				_theLiftingSurface.getSemiSpan().doubleValue(SI.METER),
				_numberOfPointSemiSpanWise
				);
		for(int i=0; i<yStationDistributionArray.length; i++)
			_yStationDistribution.add(Amount.valueOf(yStationDistributionArray[i], SI.METER));
		//......................................................................................................................
		// ALPHA ZERO LIFT
		this._alphaZeroLiftDistribution = new ArrayList<Amount<Angle>>();
		Double[] alphaZeroLiftDistributionArray = new Double[this._numberOfPointSemiSpanWise];
		alphaZeroLiftDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getAlpha0VsY()),
				yStationDistributionArray
				);
		for(int i=0; i<alphaZeroLiftDistributionArray.length; i++)
			_alphaZeroLiftDistribution.add(Amount.valueOf(alphaZeroLiftDistributionArray[i], NonSI.DEGREE_ANGLE));
		//......................................................................................................................
		// TWIST 
		this._twistDistribution = new ArrayList<Amount<Angle>>();
		Double[] twistDistributionArray = new Double[this._numberOfPointSemiSpanWise];
		twistDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getTwistsBreakPoints()),
				yStationDistributionArray
				);
		for(int i=0; i<twistDistributionArray.length; i++)
			_twistDistribution.add(Amount.valueOf(twistDistributionArray[i], NonSI.DEGREE_ANGLE));
		//......................................................................................................................
		// CHORDS
		this._chordDistribution = new ArrayList<Amount<Length>>();
		Double[] chordDistributionArray = new Double[this._numberOfPointSemiSpanWise];
		chordDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getChordsBreakPoints()),
				yStationDistributionArray
				);
		for(int i=0; i<chordDistributionArray.length; i++)
			_chordDistribution.add(Amount.valueOf(chordDistributionArray[i], SI.METER));
		//......................................................................................................................
		// DIHEDRAL
		this._dihedralDistribution = new ArrayList<Amount<Angle>>();
		Double[] dihedralDistributionArray = new Double[this._numberOfPointSemiSpanWise];
		dihedralDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getDihedralsBreakPoints()),
				yStationDistributionArray
				);
		for(int i=0; i<dihedralDistributionArray.length; i++)
			_dihedralDistribution.add(Amount.valueOf(dihedralDistributionArray[i], NonSI.DEGREE_ANGLE));
		//......................................................................................................................
		// CL ALPHA
		this._clAlphaDistribution = new ArrayList<Amount<?>>();
		Double[] clAlphaDistributionArray = new Double[this._numberOfPointSemiSpanWise];
		clAlphaDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountodoubleArray(_theLiftingSurface.getClAlphaVsY()),
				yStationDistributionArray
				);
		for(int i=0; i<clAlphaDistributionArray.length; i++)
			_clAlphaDistribution.add(Amount.valueOf(clAlphaDistributionArray[i], NonSI.DEGREE_ANGLE).inverse());
		//......................................................................................................................
		// XLE DISTRIBUTION
		this._xLEDistribution = new ArrayList<Amount<Length>>();
		Double[] xLEDistributionArray = new Double[this._numberOfPointSemiSpanWise];
		xLEDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getXLEBreakPoints()),
				yStationDistributionArray
				);
		for(int i=0; i<xLEDistributionArray.length; i++)
			_xLEDistribution.add(Amount.valueOf(xLEDistributionArray[i], SI.METER));
		//......................................................................................................................
		// Clmax DISTRIBUTION
		this._clMaxDistribution = new ArrayList<Double>();
		Double[] clMaxDistributionArray = new Double[this._numberOfPointSemiSpanWise];
		clMaxDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertToDoublePrimitive(_theLiftingSurface.getClMaxVsY()),
				yStationDistributionArray
				);
		for(int i=0; i<clMaxDistributionArray.length; i++)
			_clMaxDistribution.add(clMaxDistributionArray[i]);

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
		this._liftCoefficient3DCurve = new HashMap<MethodEnum, Double[]>();
		this._liftCoefficientDistribution = new HashMap<MethodEnum, List<List<Double>>>();
		this._liftCoefficientDistributionAtCLMax = new HashMap<MethodEnum, double[]>();
		this._liftDistribution = new HashMap<MethodEnum, List<List<Amount<Force>>>>();
		this._liftCoefficientDistributionBasicLoad = new HashMap<MethodEnum, List<List<Double>>>();
		this._basicLoadDistribution = new HashMap<MethodEnum, List<List<Amount<Force>>>>();
		this._liftCoefficientDistributionAdditionalLoad = new HashMap<MethodEnum, List<List<Double>>>();
		this._additionalLoadDistribution = new HashMap<MethodEnum, List<List<Amount<Force>>>>();
		this._cclDistributionBasicLoad = new HashMap<MethodEnum, List<List<Amount<Length>>>>();
		this._cclDistributionAdditionalLoad = new HashMap<MethodEnum, List<List<Amount<Length>>>>();
		this._cclDistribution = new HashMap<MethodEnum, List<List<Amount<Length>>>>();
		this._gammaDistributionBasicLoad = new HashMap<MethodEnum, List<List<Double>>>();
		this._gammaDistributionAdditionalLoad = new HashMap<MethodEnum, List<List<Double>>>();
		this._gammaDistribution = new HashMap<MethodEnum, List<List<Double>>>();
		
		this._alphaZeroLiftHighLift = new HashMap<MethodEnum, Amount<Angle>>();
		this._alphaStarHighLift = new HashMap<MethodEnum, Amount<Angle>>();
		this._alphaStallHighLift = new HashMap<MethodEnum, Amount<Angle>>();
		this._cLZeroHighLift = new HashMap<MethodEnum, Double>();
		this._cLStarHighLift = new HashMap<MethodEnum, Double>();
		this._cLMaxHighLift = new HashMap<MethodEnum, Double>();
		this._cLAlphaHighLift = new HashMap<MethodEnum, Amount<?>>();
		this._deltaCl0FlapList = new HashMap<MethodEnum, List<Double>>();
		this._deltaCL0FlapList = new HashMap<MethodEnum, List<Double>>();
		this._deltaClmaxFlapList = new HashMap<MethodEnum, List<Double>>();
		this._deltaCLmaxFlapList = new HashMap<MethodEnum, List<Double>>();
		this._deltaClmaxSlatList = new HashMap<MethodEnum, List<Double>>();
		this._deltaCLmaxSlatList = new HashMap<MethodEnum, List<Double>>();
		this._deltaCDList = new HashMap<MethodEnum, List<Double>>();
		this._deltaCMc4List = new HashMap<MethodEnum, List<Double>>();
		this._deltaCl0Flap = new HashMap<MethodEnum, Double>();
		this._deltaCL0Flap = new HashMap<MethodEnum, Double>();
		this._deltaClmaxFlap = new HashMap<MethodEnum, Double>();
		this._deltaCLmaxFlap = new HashMap<MethodEnum, Double>();
		this._deltaClmaxSlat = new HashMap<MethodEnum, Double>();
		this._deltaCLmaxSlat = new HashMap<MethodEnum, Double>();
		this._deltaCD = new HashMap<MethodEnum, Double>();
		this._deltaCMc4 = new HashMap<MethodEnum, Double>();
		
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
		 * and Lift Coefficient Of The Wing � An Empirical Investigation of Parameters and Equations.
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

			AirfoilTypeEnum airfoilType = _theLiftingSurface.getAirfoilList().get(0).getType();
			Amount<Angle> sweepHalfChordEq = _theLiftingSurface.getSweepHalfChordEquivalent(false);
			double maxThicknessMean = _theLiftingSurface.getThicknessMean();
			
			double machCr = AerodynamicCalc.calculateMachCriticalKroo(
					cL,
					sweepHalfChordEq.to(SI.RADIAN),
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

		public double linearDLR(double alpha) {
			// page 3 DLR pdf
			_cLAtAplhaActual.put(
					MethodEnum.LINEAR_DLR,
					LiftCalc.calcCLatAlphaLinearDLR(
							alpha,
							_theLiftingSurface.getAspectRatio()
							)
					);
			return _cLAtAplhaActual.get(MethodEnum.LINEAR_DLR);
		}

		/** 
		 * Evaluate CL at a specific AoA
		 * 
		 * @author Lorenzo Attanasio
		 * @return
		 */
		public double linearAndersonCompressibleSubsonic(Amount<Angle> alpha) {

			if(_cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
				CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
				calcCLAlpha.andersonSweptCompressibleSubsonic();
			}
			if(_cLZero.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
				CalcCL0 calcCLZero = new CalcCL0();
				calcCLZero.andersonSweptCompressibleSubsonic();
			}

			_cLAtAplhaActual.put(
					MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC,
					_cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC).to(NonSI.DEGREE_ANGLE).inverse().getEstimatedValue()
					*alpha.to(NonSI.DEGREE_ANGLE).getEstimatedValue() + 
					_cLZero.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC)
					);
			
			return _cLAtAplhaActual.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC);
		}

		public double nasaBlackwellLinear(Amount<Angle> alpha) {
			
			NasaBlackwell theNasaBlackwellCalculator = new NasaBlackwell(
					_theLiftingSurface.getSemiSpan().doubleValue(SI.METER),
					_theLiftingSurface.getSurface().doubleValue(SI.SQUARE_METRE),
					MyArrayUtils.convertListOfAmountTodoubleArray(_yStationDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_chordDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_xLEDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_dihedralDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_twistDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_alphaZeroLiftDistribution),
					_vortexSemiSpanToSemiSpanRatio,
					alpha.doubleValue(SI.RADIAN),
					_theOperatingConditions.getMachCurrent(),
					_theOperatingConditions.getAltitude().doubleValue(SI.METER)
					);

			theNasaBlackwellCalculator.calculate(alpha.to(SI.RADIAN));

			_cLAtAplhaActual.put(
					MethodEnum.LINEAR_NASA_BLACKWELL,
					theNasaBlackwellCalculator.getCLCurrent()
					);
			
			return _cLAtAplhaActual.get(MethodEnum.LINEAR_NASA_BLACKWELL);
		}

		/**
		 * This method calculates CL at alpha given as input. It interpolates the values of cl and alpha array filled before.
		 * WARNING: it is necessary to call the method CalcCLvsAlphaCurve--> nasaBlackwellCompleteCurve before.
		 * 
		 * @author Manuela Ruocco
		 *
		 */		
		public double nasaBlackwellCompleteCurve(Amount<Angle> alpha){

			double cLActual = 0.0;
			
			if ((_alphaArrayPlot != null) 
					&& (_liftCoefficient3DCurve.get(MethodEnum.NASA_BLACKWELL) != null)) {
				cLActual = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_alphaArrayPlot),
						MyArrayUtils.convertToDoublePrimitive(_liftCoefficient3DCurve.get(MethodEnum.NASA_BLACKWELL)),
						alpha.doubleValue(NonSI.DEGREE_ANGLE)
						);
			}
			else { 
				
				if(_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS) == null) {
					CalcAlphaStar calcAlphaStar = new CalcAlphaStar();
					calcAlphaStar.meanAirfoilWithInfluenceAreas();
				}
				
				if(alpha.doubleValue(NonSI.DEGREE_ANGLE)
						<= _alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS).doubleValue(NonSI.DEGREE_ANGLE)) { // linear trait

					if(_cLAlpha.get(MethodEnum.NASA_BLACKWELL) == null) {
						CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
						calcCLAlpha.nasaBlackwell();
					}
					if(_cLZero.get(MethodEnum.NASA_BLACKWELL) == null) {
						CalcCL0 calcCLZero = new CalcCL0();
						calcCLZero.nasaBlackwell();
					}
					
					cLActual = (_cLAlpha.get(MethodEnum.NASA_BLACKWELL).getEstimatedValue()
							* alpha.doubleValue(NonSI.DEGREE_ANGLE))
							+ _cLZero.get(MethodEnum.NASA_BLACKWELL);
				}
				else { // complete curve 
					
					cLActual = LiftCalc.calculateCLAtAlphaNonLinearTrait(
							alpha,
							_cLAlpha.get(MethodEnum.NASA_BLACKWELL),
							_cLStar.get(MethodEnum.NASA_BLACKWELL),
							_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS),
							_cLMax.get(MethodEnum.NASA_BLACKWELL),
							_alphaStall.get(MethodEnum.NASA_BLACKWELL)
							);
					
				}
				
			}
			
			_cLAtAplhaActual.put(MethodEnum.NASA_BLACKWELL, cLActual);
			
			return _cLAtAplhaActual.get(MethodEnum.NASA_BLACKWELL);
		}

		public void allMethods(Amount<Angle> alpha) {
			// TODO : ADD CHECK ON ALPHA IN ORDER TO EVALUATE IF ALPHA > ALPHA STAR
			linearAndersonCompressibleSubsonic(alpha);
			nasaBlackwellLinear(alpha);
		}

		//////////////////////////////////////////////
		// TODO : IMPLEMENT THESE METHODS LATER !!! //
		//////////////////////////////////////////////
		//						|
		//						|
		//						V
//		/**
//		 * This method evaluates CL for an alpha array having the elevator deflection as input. 
//		 * 
//		 * @param angle of deflection of the elevator in deg or radians
//		 * @param chord ratio -> cf_c
//		 *
//		 * @return Cl array for fixed deflection
//		 * @author  Manuela Ruocco
//		 */
//
//		// The calculation of the lift coefficient with a deflection of the elevator is made by the
//		// method calculateCLWithElevatorDeflection. This method fills the array of 20 value of CL in
//		// its linear trait. 
//		// The procedure used to calculate the CL is the following. It's important to know that 
//		// it's necessary to call the method nasaBlackwellCompleteCurve in order to obtain the
//		// cl linear slope of the horizontal tail with no elevator deflection.
//		//
//		// 1 . First of all the tau factor it's calculated used the method calculateTauIndex in the .. class
//		// 2. It's necessary to get the linear slope of the horizontal tail with no elevator defletion.
//		// 3. The alphazero lift of the deflected configuration is calculated with this formula
//		//   alphaw= tau per delta e
//		// 4. At this point it's possible to create an alpha array, starting from the alpha zero lift, until
//		//   15 degrees after.
//		// 5. the value of cl for each alpha is calculated with the formula :
//		//  cl alfa * (alfa + t delta e)
//
//		public double[] calculateCLWithElevatorDeflection (
//				List<Double[]> deltaFlap,
//				List<FlapTypeEnum> flapType,
//				List<Double> deltaSlat,
//				List<Double> etaInFlap,
//				List<Double> etaOutFlap,
//				List<Double> etaInSlat,
//				List<Double> etaOutSlat, 
//				List<Double> cfc,
//				List<Double> csc,
//				List<Double> leRadiusSlatRatio,
//				List<Double> cExtcSlat
//				) {
//
//			// variable declaration
//			Amount<Angle> deflection = Amount.valueOf(deltaFlap.get(0)[0], NonSI.DEGREE_ANGLE);
//			
//			if (deflection.getUnit() == SI.RADIAN){
//				deflection = deflection.to(NonSI.DEGREE_ANGLE);
//			}
//			double deflectionAngleDeg = deflection.getEstimatedValue();
//
//			int nPoints = 20;
//			double tauValue, cLLinearSlopeNoDeflection;
//			alphaTailArray = new double [nPoints];
//			double[] cLArray = new double [nPoints];
//			Amount<Angle> alphaActual;
//			double alphaStallElevator;
//			double deltaCLMaxElevator;
//			
//			//linear trait
//			StabilityCalculators theStablityCalculator = new StabilityCalculators();
//			tauValue = theStablityCalculator.calculateTauIndex(cfc.get(0), theAircraft, deflection);
//			cLLinearSlopeNoDeflection = getcLLinearSlopeNB()/57.3; //in deg 
//			alphaZeroLiftDeflection = - tauValue*deflectionAngleDeg;
//			alphaTailArray[0] = alphaZeroLiftDeflection;
//			double qValue = - cLLinearSlopeNoDeflection *  alphaZeroLiftDeflection;
//			double alphaTemp = 1;
//			
//			cLArray[0] = 0;
//			cLArray[1] = cLLinearSlopeNoDeflection * alphaTemp + qValue;
//
//			double clAlpha = (cLArray[1] - cLArray[0])/Math.abs(alphaTemp-alphaTailArray[0]);
//			
//			// non linear trait
//			CalcHighLiftDevices theHighLiftCalculatorLiftEffects = new CalcHighLiftDevices(
//					_theLiftingSurface, 
//					_theOperatingConditions,
//					deltaFlap, 
//					flapType, 
//					deltaSlat, 
//					etaInFlap,
//					etaOutFlap,
//					etaInSlat,
//					etaOutSlat, 
//					cfc, 
//					csc, 
//					leRadiusSlatRatio, 
//					cExtcSlat);
//			
//			theHighLiftCalculatorLiftEffects.calculateHighLiftDevicesEffects();
//			
//			deltaCLMaxElevator = theHighLiftCalculatorLiftEffects.getDeltaCLmax_flap()*tauValue;
//			double deltaAlphaMaxElevator =-(tauValue * deflection.getEstimatedValue())/2;
//			double deltaAlphaMaxElevatordelta = theHighLiftCalculatorLiftEffects.getDeltaAlphaMaxFlap();
//			CalcCLAtAlpha theCLCleanCalculator = new CalcCLAtAlpha();
//			
//			Airfoil meanAirfoil = new Airfoil(
//					LiftingSurface.calculateMeanAirfoil(getTheLiftingSurface()),
//					getTheLiftingSurface().getAerodynamicDatabaseReader()
//					); 
//					
//			calcAlphaAndCLMax(meanAirfoil);
//			Amount<Angle> alphaMax = getAlphaMaxClean().to(NonSI.DEGREE_ANGLE);
//			double alphaStarClean = meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue();
//			Amount<Angle> alphaStarCleanAmount = Amount.valueOf(alphaStarClean, SI.RADIAN);
//			double cLMax = get_cLMaxClean();
//			double cLStarClean = theCLCleanCalculator.nasaBlackwellCompleteCurve(alphaStarCleanAmount);
//			double cL0Elevator = cLArray[0];
//			double alphaStar = (cLStarClean - qValue)/clAlpha;
//			alphaStarClean = alphaStarClean*57.3;		
//			double cLMaxElevator = cLMax + deltaCLMaxElevator;
//			
//				alphaStallElevator = alphaMax.getEstimatedValue() + deltaAlphaMaxElevator;
//			
//			double alphaStarElevator; 
//
//				alphaStarElevator = (alphaStar + alphaStarClean)/2;
//			
//			cLArray[1] = cLLinearSlopeNoDeflection * (alphaStarElevator + tauValue * deflectionAngleDeg);
//			
//			double[][] matrixData = { {Math.pow(alphaStallElevator, 3), Math.pow(alphaStallElevator, 2),
//				alphaStallElevator,1.0},
//					{3* Math.pow(alphaStallElevator, 2), 2*alphaStallElevator, 1.0, 0.0},
//					{3* Math.pow(alphaStarElevator, 2), 2*alphaStarElevator, 1.0, 0.0},
//					{Math.pow(alphaStarElevator, 3), Math.pow(alphaStarElevator, 2),alphaStarElevator,1.0}};
//			RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
//
//			double [] vector = {cLMaxElevator, 0,clAlpha, cLArray[1]};
//
//			double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);
//
//			double a = solSystem[0];
//			double b = solSystem[1];
//			double c = solSystem[2];
//			double d = solSystem[3];
//
//			alphaTailArray = MyArrayUtils.linspace(alphaStarElevator,
//					alphaStallElevator+ 4,
//					nPoints-1);
//
//			double[] cLArrayHighLiftPlot = new double [nPoints];
//
//			for ( int i=0 ; i< alphaTailArray.length ; i++){
//				alphaActual = Amount.valueOf((alphaTailArray[i]), NonSI.DEGREE_ANGLE);
//				if (alphaActual.getEstimatedValue() <= alphaStarElevator) { 
//					cLArray[i+1] = clAlpha * alphaActual.getEstimatedValue() + qValue;}
//				else {
//					cLArray[i+1] = a * Math.pow(alphaActual.getEstimatedValue(), 3) + 
//							b * Math.pow(alphaActual.getEstimatedValue(), 2) + 
//							c * alphaActual.getEstimatedValue() + d;
//				}
//
//			}
//					
//			alphaArrayHTailPlot[0] = alphaZeroLiftDeflection;
//			for(int i=1; i<nPoints ; i++){
//				alphaArrayHTailPlot[i]= alphaTailArray[i-1];
//			}
//			
//			return cLArray;
//		}
//
//		/**
//		 * This method calculates CL of an horizontal tail at alpha given as input. This method calculates linear trait 
//		 * considering a known elevator deflection. It use the NasaBlackwell method in order to evaluate the slope of the linear trait
//		 * This method needs that the field of cl has filled before. --> Need to call calculateCLWithElevatorDeflection
//		 * before!
//		 * 
//		 * @author Manuela Ruocco
//		 * @param Amount<Angle> alphaBody. It is the angle of attack between the direction of asimptotic 
//		 * velocity and the reference line of fuselage.
//		 * @param Amount<Angle> deflection of elevator in degree. 
//		 */	
//
//		// In ordet to obtain a value of lift coefficient corresponding at an alpha body with a known
//		// elevator deflection it's possible to use the method getCLHTailatAlphaBodyWithElevator.
//
//		public double getCLHTailatAlphaBodyWithElevator (double chordRatio,
//				Amount<Angle> alphaBody,
//				Amount<Angle> deflection,
//				Amount<Angle> downwashAngle,
//				List<Double[]> deltaFlap,
//				List<FlapTypeEnum> flapType,
//				List<Double> deltaSlat,
//				List<Double> etaInFlap,
//				List<Double> etaOutFlap,
//				List<Double> etaInSlat,
//				List<Double> etaOutSlat, 
//				List<Double> cfc,
//				List<Double> csc,
//				List<Double> leRadiusSlatRatio,
//				List<Double> cExtcSlat
//				){
//
//			if (alphaBody.getUnit() == SI.RADIAN)
//				alphaBody = alphaBody.to(NonSI.DEGREE_ANGLE);
//			double alphaBodyDouble = alphaBody.getEstimatedValue();
//
//			if (downwashAngle.getUnit() == SI.RADIAN)
//				downwashAngle = downwashAngle.to(NonSI.DEGREE_ANGLE);
//			double downwashAngleDeg = downwashAngle.getEstimatedValue();
//
//
//			double deflectionAngleDeg = deflection.getEstimatedValue();
//			double alphaZeroLift = getAlphaZeroLiftDeflection();
//
//			double[] alphaLocalArray = getAlphaArrayHTailPlot();
//
//			double alphaLocal = alphaBodyDouble 
//					- downwashAngleDeg 
//					+ theAircraft.getHTail().getRiggingAngle().to(NonSI.DEGREE_ANGLE).getEstimatedValue();
//
//			double[]  clArray = calculateCLWithElevatorDeflection(
//					deltaFlap, flapType,deltaSlat,
//					etaInFlap, etaOutFlap, etaInSlat, etaOutSlat, 
//					 cfc, csc, leRadiusSlatRatio, cExtcSlat
//					);
//			double clAtAlpha = MyMathUtils.getInterpolatedValue1DLinear(alphaLocalArray, clArray, alphaLocal);
//
//			return clAtAlpha;
//		}
	}
	//............................................................................
	// END OF THE CRITICAL MACH INNER CLASS
	//............................................................................
	
	//............................................................................
	// CL0 INNER CLASS
	//............................................................................
	public class CalcCL0  {

		public void andersonSweptCompressibleSubsonic() {
			
			if(_cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
				CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
				calcCLAlpha.andersonSweptCompressibleSubsonic();
			}
			if(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
				CalcAlpha0L calcAlphaZeroLift = new CalcAlpha0L();
				calcAlphaZeroLift.integralMeanWithTwist();
			}
			
			_cLZero.put(
					MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC,
					LiftCalc.calculateLiftCoefficientAtAlpha0(
							_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST).doubleValue(NonSI.DEGREE_ANGLE),
							_cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC).to(NonSI.DEGREE_ANGLE).inverse().getEstimatedValue()
							)
					);
		}

		public void nasaBlackwell() {
			
			if(_cLAlpha.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
				calcCLAlpha.nasaBlackwell();
			}
			if(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
				CalcAlpha0L calcAlphaZeroLift = new CalcAlpha0L();
				calcAlphaZeroLift.integralMeanWithTwist();
			}
			
			_cLZero.put(
					MethodEnum.NASA_BLACKWELL,
					LiftCalc.calculateLiftCoefficientAtAlpha0(
							_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST).doubleValue(NonSI.DEGREE_ANGLE),
							_cLAlpha.get(MethodEnum.NASA_BLACKWELL).to(NonSI.DEGREE_ANGLE).inverse().getEstimatedValue()
							)
					);
			
		}
			
		public void allMethods() {
			andersonSweptCompressibleSubsonic();
			nasaBlackwell();
		}

	}
	//............................................................................
	// END OF THE CL0 INNER CLASS
	//............................................................................
	
	//............................................................................
	// ALPHA ZERO LIFT INNER CLASS
	//............................................................................
	/**
	 * Evaluate alpha zero lift of the entire lifting surface
	 * The alpha0L is considered relative to the root chord
	 * 
	 * @author Lorenzo Attanasio
	 */
	public class CalcAlpha0L{

		public void integralMeanNoTwist() {
			
			double surface;
			double semiSpan;
			double[] yStationDistribution = new double[_numberOfPointSemiSpanWise];
			Double[] chordDistribution = new Double[_numberOfPointSemiSpanWise];
			Double[] alphaZeroLiftDistribution = new Double[_numberOfPointSemiSpanWise];
			
			if ( _theLiftingSurface.getExposedWing() != null && _theLiftingSurface.getType() == ComponentEnum.WING){
				surface = _theLiftingSurface.getExposedWing().getSurface().doubleValue(SI.SQUARE_METRE);
				semiSpan = _theLiftingSurface.getExposedWing().getSemiSpan().doubleValue(SI.METRE);
				yStationDistribution = MyArrayUtils.linspace(
						0,
						_theLiftingSurface.getExposedWing().getSemiSpan().doubleValue(SI.METER),
						_numberOfPointSemiSpanWise
						);
				alphaZeroLiftDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedWing().getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedWing().getAlpha0VsY()),
						yStationDistribution
						);
				chordDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedWing().getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedWing().getLiftingSurfaceCreator().getChordsBreakPoints()),
						yStationDistribution
						);

			}
			else{
				surface = _theLiftingSurface.getSurface().doubleValue(SI.SQUARE_METRE);
				semiSpan = _theLiftingSurface.getSemiSpan().doubleValue(SI.METRE);
				alphaZeroLiftDistribution = MyArrayUtils.convertListOfAmountToDoubleArray(_alphaZeroLiftDistribution);
				System.out.println(" Exposed wing is the wing. There isn't fuselage in the aircraft.");
			}
			
			_alphaZeroLift.put(
					MethodEnum.INTEGRAL_MEAN_NO_TWIST,
					Amount.valueOf(
							AnglesCalc.alpha0LintegralMeanNoTwist(
									surface,
									semiSpan, 
									yStationDistribution,
									MyArrayUtils.convertToDoublePrimitive(chordDistribution),
									MyArrayUtils.convertToDoublePrimitive(alphaZeroLiftDistribution)
									),
							SI.RADIAN)
					);
		}

		public void integralMeanWithTwist() {
			
			double surface;
			double semiSpan;
			double[] yStationDistribution = new double[_numberOfPointSemiSpanWise];
			Double[] chordDistribution = new Double[_numberOfPointSemiSpanWise];
			Double[] alphaZeroLiftDistribution = new Double[_numberOfPointSemiSpanWise];
			Double[] twistDistribution = new Double[_numberOfPointSemiSpanWise];
			
			if ( _theLiftingSurface.getExposedWing() != null && _theLiftingSurface.getType() == ComponentEnum.WING){
				surface = _theLiftingSurface.getExposedWing().getSurface().doubleValue(SI.SQUARE_METRE);
				semiSpan = _theLiftingSurface.getExposedWing().getSemiSpan().doubleValue(SI.METRE);
				yStationDistribution = MyArrayUtils.linspace(
						0,
						_theLiftingSurface.getExposedWing().getSemiSpan().doubleValue(SI.METER),
						_numberOfPointSemiSpanWise
						);
				alphaZeroLiftDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedWing().getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedWing().getAlpha0VsY()),
						yStationDistribution
						);
				chordDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedWing().getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedWing().getLiftingSurfaceCreator().getChordsBreakPoints()),
						yStationDistribution
						);
				twistDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedWing().getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedWing().getLiftingSurfaceCreator().getTwistsBreakPoints()),
						yStationDistribution
						);

			}
			else{
				surface = _theLiftingSurface.getSurface().doubleValue(SI.SQUARE_METRE);
				semiSpan = _theLiftingSurface.getSemiSpan().doubleValue(SI.METRE);
				alphaZeroLiftDistribution = MyArrayUtils.convertListOfAmountToDoubleArray(_alphaZeroLiftDistribution);
				twistDistribution = MyArrayUtils.convertListOfAmountToDoubleArray(_twistDistribution);
				System.out.println(" Exposed wing is the wing. There isn't fuselage in the aircraft.");
			}
			
			_alphaZeroLift.put(
					MethodEnum.INTEGRAL_MEAN_TWIST,
					Amount.valueOf(
							AnglesCalc.alpha0LintegralMeanWithTwist(
									surface,
									semiSpan, 
									yStationDistribution,
									MyArrayUtils.convertToDoublePrimitive(chordDistribution),
									MyArrayUtils.convertToDoublePrimitive(alphaZeroLiftDistribution),
									MyArrayUtils.convertToDoublePrimitive(twistDistribution)
									),
							SI.RADIAN)
					);
		}

		public void allMethods() {
			integralMeanNoTwist();
			integralMeanWithTwist();
		}
	}
	//............................................................................
	// END OF THE ALPHA ZERO LIFT INNER CLASS
	//............................................................................
	
	//............................................................................
	// CL STAR INNER CLASS
	//............................................................................
	public class CalcCLStar {
	
		public void andersonSweptCompressibleSubsonic() {
			CalcCLAtAlpha calcCLAtAlpha = new CalcCLAtAlpha();
			
			if(_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS) != null) {
				CalcAlphaStar calcAlphaStar = new CalcAlphaStar();
				calcAlphaStar.meanAirfoilWithInfluenceAreas();
			}
			
			_cLStar.put(
					MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC,
					calcCLAtAlpha.linearAndersonCompressibleSubsonic(
							_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS)
							)
					);
		}
		
		public void nasaBlackwell() {
			CalcCLAtAlpha calcCLAtAlpha = new CalcCLAtAlpha();
			
			if(_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS) != null) {
				CalcAlphaStar calcAlphaStar = new CalcAlphaStar();
				calcAlphaStar.meanAirfoilWithInfluenceAreas();
			}
			
			_cLStar.put(
					MethodEnum.NASA_BLACKWELL,
					calcCLAtAlpha.nasaBlackwellLinear(
							_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS)
							)
					);
		}

		
		public void allMethod() {
			andersonSweptCompressibleSubsonic();
			nasaBlackwell();
		}
		
	}
	//............................................................................
	// END OF THE CL STAR INNER CLASS
	//............................................................................
	
	//............................................................................
	// ALPHA STAR INNER CLASS
	//............................................................................
	public class CalcAlphaStar {

		public void meanAirfoilWithInfluenceAreas() {
						
			_alphaStar.put(
					MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS,
					_meanAirfoil.getAirfoilCreator().getAlphaEndLinearTrait().to(NonSI.DEGREE_ANGLE)
					);
		}

		public void allMethods() {
			meanAirfoilWithInfluenceAreas();
		}
	}
	
	//............................................................................
	// END OF THE ALPHA ZERO LIFT INNER CLASS
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

		public void nasaBlackwell(){
			
			CalcCLAtAlpha calcCLAtAlpha = new CalcCLAtAlpha();
			
			Amount<Angle> alphaOne = Amount.valueOf(toRadians(0.), SI.RADIAN);
			double clOne = calcCLAtAlpha.nasaBlackwellLinear(alphaOne);

			Amount<Angle>alphaTwo = Amount.valueOf(toRadians(4.), SI.RADIAN);
			double clTwo = calcCLAtAlpha.nasaBlackwellLinear(alphaTwo);

			double cLSlope = (clTwo-clOne)/alphaTwo.getEstimatedValue();

			_cLAlpha.put(MethodEnum.NASA_BLACKWELL,
					Amount.valueOf(
							cLSlope,
							SI.RADIAN
							).inverse()
					);
		}
		
		public void polhamus() {

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
					Amount.valueOf(
							LiftCalc.calcCLalphaAndersonSweptCompressibleSubsonic(
									_theOperatingConditions.getMachCurrent(),
									_theLiftingSurface.getAspectRatio(),
									_theLiftingSurface.getSemiSpan().doubleValue(SI.METER),
									_theLiftingSurface.getSweepHalfChordEquivalent(false).doubleValue(NonSI.DEGREE_ANGLE), 
									MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedYs()),
									MyArrayUtils.convertListOfAmountodoubleArray(_clAlphaDistribution),
									MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedChords())
									),
							NonSI.DEGREE_ANGLE
							).inverse()
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
					Amount.valueOf(
							LiftCalc.calcCLalphaIntegralMean2D(
									_theLiftingSurface.getSurface().doubleValue(SI.SQUARE_METRE),
									_theLiftingSurface.getSemiSpan().doubleValue(SI.METER),
									MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedYs()), 
									MyArrayUtils.convertListOfAmountodoubleArray(_clAlphaDistribution),
									MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedChords())
									),
							NonSI.DEGREE_ANGLE
							).inverse()
					);
		}

		public void allMethods() {
			polhamus();
			andersonSweptCompressibleSubsonic();
			integralMean2D();
			nasaBlackwell();
		}

	}
	//............................................................................
	// END OF THE CLalpha INNER CLASS
	//............................................................................
	
	//............................................................................
	// CL MAX INNER CLASS
	//............................................................................
	public class CalcCLmax {

		public void phillipsAndAlley() {
			
			if(_cLAlpha.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
				calcCLAlpha.nasaBlackwell();
			}
			
			double result = LiftCalc.calculateCLmaxPhillipsAndAlley( //5.07
					_meanAirfoil.getAirfoilCreator().getClMax().doubleValue(),
					_cLAlpha.get(MethodEnum.NASA_BLACKWELL).to(SI.RADIAN).inverse().getEstimatedValue(), 
					_theLiftingSurface.getLiftingSurfaceCreator().getTaperRatioEquivalentWing().doubleValue(),
					_theLiftingSurface.getSweepLEEquivalent(false).doubleValue(SI.RADIAN),
					_theLiftingSurface.getAspectRatio(),
					_theLiftingSurface.getLiftingSurfaceCreator().getTwistAtTipEquivalentWing().getEstimatedValue(),
					_theLiftingSurface.getAerodynamicDatabaseReader()
					);

			_cLMax.put(MethodEnum.PHILLIPS_ALLEY, result);
		}

		/**
		 * Use NASA-Blackwell method for estimating the
		 * lifting surface CLmax
		 * 
		 * @author Lorenzo Attanasio ft Manuela Ruocco
		 */
		public void nasaBlackwell() {

			double[] alphaArrayNasaBlackwell = MyArrayUtils.linspace(0.0, 30, 31);
			double[] clDistributionActualNasaBlackwell = new double[_numberOfPointSemiSpanWise]; 
			boolean firstIntersectionFound = false;
			int indexOfFirstIntersection = 0;
			int indexOfAlphaFirstIntersection = 0;
			double diffCLapp = 0;
			double diffCLappOld = 0;
			double diffCL = 0;
			double accuracy =0.0001;
			double deltaAlpha = 0.0;
			Amount<Angle> alphaNew = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
			
			double[] twistDistributionRadians = new double[_numberOfPointSemiSpanWise];
			double[] alphaZeroLiftDistributionRadians = new double[_numberOfPointSemiSpanWise];
			
			for (int i=0; i< _numberOfPointSemiSpanWise; i++) {
				twistDistributionRadians[i] = _twistDistribution.get(i).doubleValue(SI.RADIAN);
				alphaZeroLiftDistributionRadians[i] = _alphaZeroLiftDistribution.get(i).doubleValue(SI.RADIAN);
			}
			
			NasaBlackwell theNasaBlackwellCalculator = new NasaBlackwell(
					_theLiftingSurface.getSemiSpan().doubleValue(SI.METER),
					_theLiftingSurface.getSurface().doubleValue(SI.SQUARE_METRE),
					MyArrayUtils.convertListOfAmountTodoubleArray(_yStationDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_chordDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_xLEDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_dihedralDistribution),
					twistDistributionRadians,
					alphaZeroLiftDistributionRadians,
					_vortexSemiSpanToSemiSpanRatio,
					0.0,
					_theOperatingConditions.getMachCurrent(),
					_theOperatingConditions.getAltitude().doubleValue(SI.METER)
					);

			// TODO: try to use nasa Blackwell also for vtail
			if (_theLiftingSurface.getType() != ComponentEnum.VERTICAL_TAIL) {
				for (int i=0; i < alphaArrayNasaBlackwell.length; i++) {
					if(firstIntersectionFound == false) {
						theNasaBlackwellCalculator.calculate(
								Amount.valueOf(
										alphaArrayNasaBlackwell[i],
										NonSI.DEGREE_ANGLE).to(SI.RADIAN)
								);
						clDistributionActualNasaBlackwell = 
								theNasaBlackwellCalculator
								.getClTotalDistribution()
								.toArray();

						for(int j =0; j < _numberOfPointSemiSpanWise; j++) {
							if( clDistributionActualNasaBlackwell[j] > _clMaxDistribution.get(j)) {
								firstIntersectionFound = true;
								indexOfFirstIntersection = j;
								break;
							}
						}
					}
					else {
						indexOfAlphaFirstIntersection = i;
						break;
					}
				}
			}
			
			//@author Manuela ruocco
			// After find the first point where CL_wing > Cl_MAX_airfoil, starts an iteration on alpha
			// in order to improve the accuracy.

			for (int k = indexOfFirstIntersection; k< _numberOfPointSemiSpanWise; k++) {
				diffCLapp = ( clDistributionActualNasaBlackwell[k] -  _clMaxDistribution.get(k));
				diffCL = Math.max(diffCLapp, diffCLappOld);
				diffCLappOld = diffCL;
			}
			if( Math.abs(diffCL) < accuracy){
				_cLMax.put(MethodEnum.NASA_BLACKWELL, theNasaBlackwellCalculator.getCLCurrent());
				_alphaMaxLinear.put(
						MethodEnum.NASA_BLACKWELL,
						Amount.valueOf(
								theNasaBlackwellCalculator.getAlphaCurrent(),
								NonSI.DEGREE_ANGLE)
						); 
			}
			else{
				deltaAlpha = alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection] 
							- alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection-1];
				alphaNew = Amount.valueOf(
						(alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection] - (deltaAlpha/2)),
						NonSI.DEGREE_ANGLE
						).to(SI.RADIAN);
				double alphaOld = alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection]; 
				diffCLappOld = 0;
				while ( diffCL > accuracy){
					diffCL = 0;
					theNasaBlackwellCalculator.calculate(alphaNew);
					clDistributionActualNasaBlackwell = theNasaBlackwellCalculator
							.getClTotalDistribution()
								.toArray();
					for (int m =0; m< _numberOfPointSemiSpanWise; m++) {
						diffCLapp = clDistributionActualNasaBlackwell[m] - _clMaxDistribution.get(m);

						if ( diffCLapp > 0 ){
							diffCL = Math.max(diffCLapp,diffCLappOld);
							diffCLappOld = diffCL;
						}

					}
					deltaAlpha = Math.abs(alphaOld - alphaNew.doubleValue(NonSI.DEGREE_ANGLE));
					alphaOld = alphaNew.doubleValue(NonSI.DEGREE_ANGLE);
					if (diffCL == 0){ //this means that diffCL would have been negative
						alphaNew = Amount.valueOf(
								alphaOld + (deltaAlpha/2),
								NonSI.DEGREE_ANGLE
								);
						diffCL = 1; // generic positive value in order to enter again in the while cycle 
						diffCLappOld = 0;
					}
					else { 
						if(deltaAlpha > 0.005){
							alphaNew = Amount.valueOf(
									alphaOld - (deltaAlpha/2),
									NonSI.DEGREE_ANGLE
									);	
							diffCLappOld = 0;
							if ( diffCL < accuracy) break;
						}
						else {
							alphaNew = Amount.valueOf(
									alphaOld - (deltaAlpha),
									NonSI.DEGREE_ANGLE
									);	
							diffCLappOld = 0;
							if ( diffCL < accuracy) 
								break;
						}
					}
				}
				theNasaBlackwellCalculator.calculate(alphaNew.to(SI.RADIAN));
				_liftCoefficientDistributionAtCLMax.put(
						MethodEnum.NASA_BLACKWELL,
						theNasaBlackwellCalculator.getClTotalDistribution().toArray()
						);
				_cLMax.put(MethodEnum.NASA_BLACKWELL, theNasaBlackwellCalculator.getCLCurrent())	;
				_alphaMaxLinear.put(MethodEnum.NASA_BLACKWELL, alphaNew);
			}
		}

		public void allMethods() {
			nasaBlackwell();
			phillipsAndAlley();
		}
	}
	//............................................................................
	// END OF THE CL MAX INNER CLASS
	//............................................................................

	//............................................................................
	// ALPHA STALL INNER CLASS
	//............................................................................
	public class CalcAlphaStall {

		// IN THIS CASE WE USE CLALPHA AND CL0 FROM ANDERSON METHOD IN ORDER TO ESTIMATE DI 
		// ALPHA MAX LINEAR USING THE CLMAX FROM PHILLIPS AND ALLEY.
		public void fromCLmaxPhillipsAndAlley() {
			
			if(_alphaMaxLinear.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
				
				if(_cLZero.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
					CalcCL0 theCL0Calculator = new CalcCL0();
					theCL0Calculator.andersonSweptCompressibleSubsonic();
				}
					
				if(_cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
					CalcCLAlpha theCLAlphaCalculator = new CalcCLAlpha();
					theCLAlphaCalculator.andersonSweptCompressibleSubsonic();
				}
				
				if(_cLMax.get(MethodEnum.PHILLIPS_ALLEY) == null) {
					CalcCLmax theCLMaxCalculator = new CalcCLmax();
					theCLMaxCalculator.phillipsAndAlley();
				}
				
				_alphaMaxLinear.put(
						MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC,
						Amount.valueOf(
								(_cLMax.get(MethodEnum.PHILLIPS_ALLEY) - _cLZero.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC))
								/_cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC).to(NonSI.DEGREE_ANGLE).inverse().getEstimatedValue(),
								NonSI.DEGREE_ANGLE
								)
						);
			}
			
			double deltaYPercent = _theLiftingSurface.getAerodynamicDatabaseReader()
					.getDeltaYvsThickness(
							_meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
							_meanAirfoil.getAirfoilCreator().getFamily()
							);
			
			Amount<Angle> deltaAlpha = Amount.valueOf(
					_theLiftingSurface.getAerodynamicDatabaseReader()
					.getDAlphaVsLambdaLEVsDy(
							_theLiftingSurface.getSweepLEEquivalent(false).doubleValue(NonSI.DEGREE_ANGLE),
							deltaYPercent
							),
					NonSI.DEGREE_ANGLE);
			
			_alphaStall.put(
					MethodEnum.PHILLIPS_ALLEY,
					_alphaMaxLinear.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC)
					.plus(deltaAlpha)
					);
		}
		
		public void fromAlphaMaxLineaNasaBlackwell() {
			
			if(_alphaMaxLinear.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCLmax theCLMaxCalculator = new CalcCLmax();
				theCLMaxCalculator.nasaBlackwell();
			}
			
			double deltaYPercent = _theLiftingSurface.getAerodynamicDatabaseReader()
					.getDeltaYvsThickness(
							_meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
							_meanAirfoil.getAirfoilCreator().getFamily()
							);
			
			Amount<Angle> deltaAlpha = Amount.valueOf(
					_theLiftingSurface.getAerodynamicDatabaseReader()
					.getDAlphaVsLambdaLEVsDy(
							_theLiftingSurface.getSweepLEEquivalent(false).doubleValue(NonSI.DEGREE_ANGLE),
							deltaYPercent
							),
					NonSI.DEGREE_ANGLE);
			
			_alphaStall.put(
					MethodEnum.NASA_BLACKWELL,
					_alphaMaxLinear.get(MethodEnum.NASA_BLACKWELL)
					.plus(deltaAlpha)
					);
		}
		
	}
	//............................................................................
	// END OF THE ALPHA STALL INNER CLASS
	//............................................................................
	
	//............................................................................
	// ALPHA STALL INNER CLASS
	//............................................................................
	public class CalcLiftCurve {
	
		public void fromCLmaxPhillipsAndAlley() {
			
			if(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
				CalcAlpha0L calcAlphaZeroLift = new CalcAlpha0L();
				calcAlphaZeroLift.integralMeanWithTwist();
			}
			
			if(_cLZero.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
				CalcCL0 calcCLZero = new CalcCL0();
				calcCLZero.andersonSweptCompressibleSubsonic();
			}
			
			if(_cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
				CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
				calcCLAlpha.andersonSweptCompressibleSubsonic();
			}
			
			if(_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS) == null) {
				CalcAlphaStar calcAlphaStar = new CalcAlphaStar();
				calcAlphaStar.meanAirfoilWithInfluenceAreas();
			}
			
			if(_cLStar.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
				CalcCLStar calcCLStar = new CalcCLStar();
				calcCLStar.andersonSweptCompressibleSubsonic();
			}
			
			if(_alphaStall.get(MethodEnum.PHILLIPS_ALLEY) == null) {
				CalcAlphaStall calcAlphaStall = new CalcAlphaStall();
				calcAlphaStall.fromCLmaxPhillipsAndAlley();
			}
			
			if(_cLMax.get(MethodEnum.PHILLIPS_ALLEY) == null) {
				CalcCLmax calcCLmax = new CalcCLmax();
				calcCLmax.phillipsAndAlley();
			}
			
			_alphaArrayPlot = MyArrayUtils.linspaceDouble(
					_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST).doubleValue(NonSI.DEGREE_ANGLE)-2,
					_alphaStall.get(MethodEnum.PHILLIPS_ALLEY).doubleValue(NonSI.DEGREE_ANGLE) + 3,
					_numberOfAlphasPlot
					);
			
			_liftCoefficient3DCurve.put(
					MethodEnum.PHILLIPS_ALLEY,
					LiftCalc.calculateCLvsAlphaArray(
							_cLZero.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC),
							_cLStar.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC),
							_cLMax.get(MethodEnum.PHILLIPS_ALLEY),
							_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS),
							_alphaStall.get(MethodEnum.PHILLIPS_ALLEY),
							_cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC),
							_alphaArrayPlot
							)
					);
		}
		
		public void nasaBlackwell() {
			
			if(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
				CalcAlpha0L calcAlphaZeroLift = new CalcAlpha0L();
				calcAlphaZeroLift.integralMeanWithTwist();
			}
			
			if(_cLZero.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCL0 calcCLZero = new CalcCL0();
				calcCLZero.nasaBlackwell();
			}
			
			if(_cLAlpha.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
				calcCLAlpha.nasaBlackwell();
			}
			
			if(_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS) == null) {
				CalcAlphaStar calcAlphaStar = new CalcAlphaStar();
				calcAlphaStar.meanAirfoilWithInfluenceAreas();
			}
			
			if(_cLStar.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCLStar calcCLStar = new CalcCLStar();
				calcCLStar.nasaBlackwell();
			}
			
			if(_alphaStall.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcAlphaStall calcAlphaStall = new CalcAlphaStall();
				calcAlphaStall.fromAlphaMaxLineaNasaBlackwell();
			}
			
			if(_cLMax.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCLmax calcCLmax = new CalcCLmax();
				calcCLmax.nasaBlackwell();
			}
			
			_alphaArrayPlot = MyArrayUtils.linspaceDouble(
					_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST).doubleValue(NonSI.DEGREE_ANGLE)-2,
					_alphaStall.get(MethodEnum.NASA_BLACKWELL).doubleValue(NonSI.DEGREE_ANGLE) + 3,
					_numberOfAlphasPlot
					);
			
			_liftCoefficient3DCurve.put(
					MethodEnum.NASA_BLACKWELL,
					LiftCalc.calculateCLvsAlphaArray(
							_cLZero.get(MethodEnum.NASA_BLACKWELL),
							_cLStar.get(MethodEnum.NASA_BLACKWELL),
							_cLMax.get(MethodEnum.NASA_BLACKWELL),
							_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS),
							_alphaStall.get(MethodEnum.NASA_BLACKWELL),
							_cLAlpha.get(MethodEnum.NASA_BLACKWELL),
							_alphaArrayPlot
							)
					);
		}
	}		
	//............................................................................
	// END OF THE ALPHA STALL INNER CLASS
	//............................................................................
		
	//............................................................................
	// CALC LIFT DISTRIBUTIONS CLASS
	//............................................................................
	public class CalcLiftDistributions {

		public void schrenk() {
			
			_ellipticalChordDistribution = new ArrayList<Amount<Length>>();
			for(int i=0; i<_numberOfPointSemiSpanWise; i++)
				_ellipticalChordDistribution.add(
						Amount.valueOf(
								((4*_theLiftingSurface.getSurface().doubleValue(SI.SQUARE_METRE))
										/(Math.PI*_theLiftingSurface.getSpan().doubleValue(SI.METER)))
								*Math.sqrt(1-Math.pow(_etaStationDistribution[i],2)),
								SI.METER
								)
						);

			CalcCLAtAlpha theCLAtAlphaCalculator = new CalcCLAtAlpha();

			List<List<Amount<Length>>> ccLAdditionalSchrenk = new ArrayList<>();
			List<List<Amount<Length>>> ccLBasicSchrenk = new ArrayList<>();
			List<List<Amount<Length>>> ccLTotalSchrenk = new ArrayList<>();
			List<List<Double>> gammaAdditionalSchrenk = new ArrayList<>();
			List<List<Double>> gammaBasicSchrenk = new ArrayList<>();
			List<List<Double>> gammaTotalSchrenk = new ArrayList<>();
			List<List<Double>> liftCoefficientAdditionalSchrenk = new ArrayList<>();
			List<List<Double>> liftCoefficientBasicSchrenk = new ArrayList<>();
			List<List<Double>> liftCoefficientTotalSchrenk = new ArrayList<>();
			List<List<Amount<Force>>> liftAdditionalSchrenk = new ArrayList<>();
			List<List<Amount<Force>>> liftBasicSchrenk = new ArrayList<>();
			List<List<Amount<Force>>> liftTotalSchrenk = new ArrayList<>();
			
			for(int i=0; i<_numberOfAlphas; i++) {

				double cLActual = theCLAtAlphaCalculator.nasaBlackwellCompleteCurve(
						_alphaArray.get(i)
						);

				for(int j=0; j<_numberOfPointSemiSpanWise; j++) {
					ccLAdditionalSchrenk.get(i).add(
							_chordDistribution.get(j)
							.plus(_ellipticalChordDistribution.get(j))
							.divide(2)
							.times(cLActual)
							);
					ccLBasicSchrenk.get(i).add(
							_chordDistribution.get(j)
							.times(_clAlphaDistribution.get(j)
									.to(NonSI.DEGREE_ANGLE)
									.inverse()
									.getEstimatedValue())
							.times(0.5)
							.times(_twistDistribution.get(j).doubleValue(NonSI.DEGREE_ANGLE)
									-_alphaZeroLiftDistribution.get(j).doubleValue(NonSI.DEGREE_ANGLE)
									)
							);
					ccLTotalSchrenk.get(i).add(
							ccLAdditionalSchrenk.get(i).get(j)
								.plus(ccLBasicSchrenk.get(i).get(j))
							);
					gammaAdditionalSchrenk.get(i).add(
							ccLAdditionalSchrenk.get(i).get(j)
							.divide(
									_theLiftingSurface.getSpan()
									.times(2)
									.doubleValue(SI.METER)
									)
							.getEstimatedValue()
							);
					gammaBasicSchrenk.get(i).add(
							ccLBasicSchrenk.get(i).get(j)
							.divide(
									_theLiftingSurface.getSpan()
									.times(2)
									.doubleValue(SI.METER)
									)
							.getEstimatedValue()
							);
					gammaTotalSchrenk.get(i).add(
							gammaAdditionalSchrenk.get(i).get(j) + gammaBasicSchrenk.get(i).get(j)
							);
					liftCoefficientAdditionalSchrenk.get(i).add(
							ccLAdditionalSchrenk.get(i).get(j)
							.divide(_chordDistribution.get(j)).getEstimatedValue()
							);
					liftCoefficientBasicSchrenk.get(i).add(
							ccLBasicSchrenk.get(i).get(j)
							.divide(_chordDistribution.get(j)).getEstimatedValue()
							);
					liftCoefficientTotalSchrenk.get(i).add(
							ccLTotalSchrenk.get(i).get(j)
							.divide(_chordDistribution.get(j)).getEstimatedValue()
							);
					liftAdditionalSchrenk.get(i).add(
							Amount.valueOf(
									ccLAdditionalSchrenk.get(i).get(j).doubleValue(SI.METER)
									*_theOperatingConditions.getDynamicPressure().doubleValue(SI.PASCAL),
									SI.NEWTON
									)
							);
					liftBasicSchrenk.get(i).add(
							Amount.valueOf(
									ccLBasicSchrenk.get(i).get(j).doubleValue(SI.METER)
									*_theOperatingConditions.getDynamicPressure().doubleValue(SI.PASCAL),
									SI.NEWTON
									)
							);
					liftTotalSchrenk.get(i).add(
							Amount.valueOf(
									ccLTotalSchrenk.get(i).get(j).doubleValue(SI.METER)
									*_theOperatingConditions.getDynamicPressure().doubleValue(SI.PASCAL),
									SI.NEWTON
									)
							);
				}
			}
			_cclDistributionAdditionalLoad.put(
					MethodEnum.SCHRENK,
					ccLAdditionalSchrenk
					);
			_cclDistributionBasicLoad.put(
					MethodEnum.SCHRENK,
					ccLBasicSchrenk
					);
			_cclDistribution.put(
					MethodEnum.SCHRENK,
					ccLTotalSchrenk
					);
			_gammaDistributionAdditionalLoad.put(
					MethodEnum.SCHRENK,
					gammaAdditionalSchrenk
					);
			_gammaDistributionBasicLoad.put(
					MethodEnum.SCHRENK,
					gammaBasicSchrenk
					);
			_gammaDistribution.put(
					MethodEnum.SCHRENK,
					gammaTotalSchrenk
					);
			_liftCoefficientDistributionAdditionalLoad.put(
					MethodEnum.SCHRENK,
					liftCoefficientAdditionalSchrenk
					);
			_liftCoefficientDistributionBasicLoad.put(
					MethodEnum.SCHRENK,
					liftCoefficientBasicSchrenk
					);
			_liftCoefficientDistribution.put(
					MethodEnum.SCHRENK,
					liftCoefficientTotalSchrenk
					);
			_additionalLoadDistribution.put(
					MethodEnum.SCHRENK,
					liftAdditionalSchrenk
					);
			_basicLoadDistribution.put(
					MethodEnum.SCHRENK,
					liftBasicSchrenk
					);
			_liftDistribution.put(
					MethodEnum.SCHRENK,
					liftTotalSchrenk
					);
		}
	
		public void nasaBlackwell() {
			
			List<List<Amount<Length>>> ccLAdditional = new ArrayList<>();
			List<List<Amount<Length>>> ccLBasic = new ArrayList<>();
			List<List<Amount<Length>>> ccLTotal = new ArrayList<>();
			List<List<Double>> gammaAdditional = new ArrayList<>();
			List<List<Double>> gammaBasic = new ArrayList<>();
			List<List<Double>> gammaTotal = new ArrayList<>();
			List<List<Double>> liftCoefficientAdditional = new ArrayList<>();
			List<List<Double>> liftCoefficientBasic = new ArrayList<>();
			List<List<Double>> liftCoefficientTotal = new ArrayList<>();
			List<List<Amount<Force>>> liftAdditional = new ArrayList<>();
			List<List<Amount<Force>>> liftBasic = new ArrayList<>();
			List<List<Amount<Force>>> liftTotal = new ArrayList<>();
			
			NasaBlackwell theNasaBlackwellCalculator = new NasaBlackwell(
					_theLiftingSurface.getSemiSpan().doubleValue(SI.METER),
					_theLiftingSurface.getSurface().doubleValue(SI.SQUARE_METRE),
					MyArrayUtils.convertListOfAmountTodoubleArray(_yStationDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_chordDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_xLEDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_dihedralDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_twistDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_alphaZeroLiftDistribution),
					_vortexSemiSpanToSemiSpanRatio,
					0.0,
					_theOperatingConditions.getMachCurrent(),
					_theOperatingConditions.getAltitude().doubleValue(SI.METER)
					);
			
			NasaBlackwell theNasaBlackwellCalculatorAlphaZeroLift = new NasaBlackwell(
					_theLiftingSurface.getSemiSpan().doubleValue(SI.METER),
					_theLiftingSurface.getSurface().doubleValue(SI.SQUARE_METRE),
					MyArrayUtils.convertListOfAmountTodoubleArray(_yStationDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_chordDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_xLEDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_dihedralDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_twistDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(_alphaZeroLiftDistribution),
					_vortexSemiSpanToSemiSpanRatio,
					0.0,
					_theOperatingConditions.getMachCurrent(),
					_theOperatingConditions.getAltitude().doubleValue(SI.METER)
					);
			
			if(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
				CalcAlpha0L theAlphaZeroLiftCalculator = new CalcAlpha0L();
				theAlphaZeroLiftCalculator.integralMeanWithTwist();
			}
			
			// EVALUATION OF THE BASIC LOAD
			theNasaBlackwellCalculatorAlphaZeroLift.calculate(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST));
			
			for(int i=0; i<_numberOfAlphas; i++) {
			
				theNasaBlackwellCalculator.calculate(_alphaArray.get(i));
				
				for(int j=0; j<_numberOfPointSemiSpanWise; j++) {
					ccLTotal.get(i).add(
							Amount.valueOf(
									theNasaBlackwellCalculator.getClTotalDistribution().get(j)
									*_chordDistribution.get(j).doubleValue(SI.METER),
									SI.METER
									)
							);
					ccLBasic.get(i).add(
							Amount.valueOf(
									theNasaBlackwellCalculatorAlphaZeroLift.getClTotalDistribution().get(j)
									*_chordDistribution.get(j).doubleValue(SI.METER),
									SI.METER)
							);
					ccLAdditional.get(i).add(
							ccLTotal.get(i).get(j).minus(ccLBasic.get(i).get(j))
							);
					gammaTotal.get(i).add(
							theNasaBlackwellCalculator.getGammaDistribution().get(j)
							);
					gammaBasic.get(i).add(
							theNasaBlackwellCalculatorAlphaZeroLift.getGammaDistribution().get(j)
							);
					gammaAdditional.get(i).add(
							gammaTotal.get(i).get(j) - gammaBasic.get(i).get(j)
							);
					liftCoefficientTotal.get(i).add(
							theNasaBlackwellCalculator.getClTotalDistribution().get(j)
							);
					liftCoefficientBasic.get(i).add(
							theNasaBlackwellCalculatorAlphaZeroLift.getClTotalDistribution().get(j)
							);
					liftCoefficientAdditional.get(i).add(
							liftCoefficientTotal.get(i).get(j) - liftCoefficientBasic.get(i).get(j)
							);
					liftTotal.get(i).add(
							Amount.valueOf(
									ccLTotal.get(i).get(j).doubleValue(SI.METER)
									*_theOperatingConditions.getDynamicPressure().doubleValue(SI.PASCAL),
									SI.NEWTON
									)
							);
					liftBasic.get(i).add(
							Amount.valueOf(
									ccLBasic.get(i).get(j).doubleValue(SI.METER)
									*_theOperatingConditions.getDynamicPressure().doubleValue(SI.PASCAL),
									SI.NEWTON
									)
							);
					liftAdditional.get(i).add(
							Amount.valueOf(
									ccLAdditional.get(i).get(j).doubleValue(SI.METER)
									*_theOperatingConditions.getDynamicPressure().doubleValue(SI.PASCAL),
									SI.NEWTON
									)
							);
				}
			}
			
			_cclDistributionAdditionalLoad.put(
					MethodEnum.NASA_BLACKWELL,
					ccLAdditional
					);
			_cclDistributionBasicLoad.put(
					MethodEnum.NASA_BLACKWELL,
					ccLBasic
					);
			_cclDistribution.put(
					MethodEnum.NASA_BLACKWELL,
					ccLTotal
					);
			_gammaDistributionAdditionalLoad.put(
					MethodEnum.NASA_BLACKWELL,
					gammaAdditional
					);
			_gammaDistributionBasicLoad.put(
					MethodEnum.NASA_BLACKWELL,
					gammaBasic
					);
			_gammaDistribution.put(
					MethodEnum.NASA_BLACKWELL,
					gammaTotal
					);
			_liftCoefficientDistributionAdditionalLoad.put(
					MethodEnum.NASA_BLACKWELL,
					liftCoefficientAdditional
					);
			_liftCoefficientDistributionBasicLoad.put(
					MethodEnum.NASA_BLACKWELL,
					liftCoefficientBasic
					);
			_liftCoefficientDistribution.put(
					MethodEnum.NASA_BLACKWELL,
					liftCoefficientTotal
					);
			_additionalLoadDistribution.put(
					MethodEnum.NASA_BLACKWELL,
					liftAdditional
					);
			_basicLoadDistribution.put(
					MethodEnum.NASA_BLACKWELL,
					liftBasic
					);
			_liftDistribution.put(
					MethodEnum.NASA_BLACKWELL,
					liftTotal
					);
		}

//---------------------------------------------------------------------------------------------
//		EVENTUALLY LATER!!
//---------------------------------------------------------------------------------------------		
		/**
		 * This function calculates the lift distribution using the cl of 50 airfoil among the semispan.
		 * This function creates a new airfoil with its own characteristics for each of 50 station and 
		 * evaluates the local cl both in its linear trait and non linear.
		 * 
		 * @author Manuela Ruocco 
		 * @param Amount<Angle> alpha
		 * @param LiftingSurfaceCreator theWing
		 */ 
//		
//		public void airfoilLiftDistribution (Amount<Angle> alpha, LiftingSurface theWing){
//			double [] clLocalAirfoil = new double [_nPointsSemispanWise];
//			Airfoil intermediateAirfoil;
//			double alphaDouble = alpha.getEstimatedValue();
//
//			for (int i=0 ; i< _nPointsSemispanWise ; i++){
//				intermediateAirfoil = new Airfoil(
//						LiftingSurface.calculateAirfoilAtY(theWing, _yStations[i]),
//						theWing.getAerodynamicDatabaseReader()
//						);
//				clLocalAirfoil[i] = intermediateAirfoil.getAerodynamics().calculateClAtAlpha(alphaDouble);
//				clLocalAirfoil[_nPointsSemispanWise-1] = 0;
//
//			}	
//			System.out.println(" cl distribution " + Arrays.toString(clLocalAirfoil));
//		}
//---------------------------------------------------------------------------------------------

		public void allMethods() {
			schrenk();
			nasaBlackwell();
		}
	}
	//............................................................................
	// END OF THE CALC LIFT DISTRIBUTIONS INNER CLASS
	//............................................................................
	
	//............................................................................
	// HIGH LIFT DEVICES EFFECTS INNER CLASS
	//............................................................................
	public class CalcHighLiftDevicesEffects {
		
		public void semiempirical() {
			
			if(_currentLiftCoefficient == null) {
				CalcCLAtAlpha calcCLAtAlphaCalculator = new CalcCLAtAlpha();
				calcCLAtAlphaCalculator.nasaBlackwellCompleteCurve(
						_theOperatingConditions.getAlphaCurrent()
						);
			}
			
			LiftCalc.calculateHighLiftDevicesEffects(
					_theLiftingSurface,
					_theOperatingConditions,
					_theOperatingConditions.getFlapDeflection(),
					_theOperatingConditions.getSlatDeflection(),
					_currentLiftCoefficient
					);	
			
		}
		
	}	
	//............................................................................
	// END OF THE HIGH LIFT DEVICES EFFECTS INNER CLASS
	//............................................................................
	
	//............................................................................
	// HIGH LIFT CURVE INNER CLASS
	//............................................................................
	public class CalcHighLiftCurve {
		
		public void semiempirical() {
			
			if(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
				CalcAlpha0L calcAlphaZeroLift = new CalcAlpha0L();
				calcAlphaZeroLift.integralMeanWithTwist();
			}
			
			if(_cLZero.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCL0 calcCLZero = new CalcCL0();
				calcCLZero.nasaBlackwell();
			}
			
			if(_cLAlpha.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
				calcCLAlpha.nasaBlackwell();
			}
			
			if(_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS) == null) {
				CalcAlphaStar calcAlphaStar = new CalcAlphaStar();
				calcAlphaStar.meanAirfoilWithInfluenceAreas();
			}
			
			if(_cLStar.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCLStar calcCLStar = new CalcCLStar();
				calcCLStar.nasaBlackwell();
			}
			
			if(_alphaStall.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcAlphaStall calcAlphaStall = new CalcAlphaStall();
				calcAlphaStall.fromAlphaMaxLineaNasaBlackwell();
			}
			
			if(_cLMax.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCLmax calcCLmax = new CalcCLmax();
				calcCLmax.nasaBlackwell();
			}
			
			if((_deltaCL0Flap.get(MethodEnum.EMPIRICAL) == null) ||
			   (_deltaCLmaxFlap.get(MethodEnum.EMPIRICAL) == null) ||
			   (_cLAlphaHighLift.get(MethodEnum.EMPIRICAL) == null)
					) {
				
				CalcHighLiftDevicesEffects theHighLiftEffectsCalculator = new CalcHighLiftDevicesEffects();
				theHighLiftEffectsCalculator.semiempirical();
				
			}
			
			CalcLiftCurve theCleanLiftCurveCalculator = new CalcLiftCurve();
			theCleanLiftCurveCalculator.nasaBlackwell();
			
			///////////////////////////////////////
			// TODO: SUM UP THE HIGH LIFT EFFECTS// 
			//       ON THE CLEAN CURVE			 //
			///////////////////////////////////////
			
		}
		
	}	
	//............................................................................
	// END OF THE HIGH LIFT CURVE INNER CLASS
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
	/**
	 * @return the _meanAirfoil
	 */
	public Airfoil getMeanAirfoil() {
		return _meanAirfoil;
	}

	/**
	 * @param _meanAirfoil the _meanAirfoil to set
	 */
	public void setMeanAirfoil(Airfoil _meanAirfoil) {
		this._meanAirfoil = _meanAirfoil;
	}

	public OperatingConditions getTheOperatingConditions() {
		return _theOperatingConditions;
	}
	public void setTheOperatingConditions(OperatingConditions _theOperatingConditions) {
		this._theOperatingConditions = _theOperatingConditions;
	}
	public Map <String, List<MethodEnum>> getTaskMap() {
		return _taskMap;
	}
	public void setTaskMap(Map <String, List<MethodEnum>> _taskMap) {
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
	/**
	 * @return the _numberOfAlphasPlot
	 */
	public int getNumberOfAlphasPlot() {
		return _numberOfAlphasPlot;
	}

	/**
	 * @param _numberOfAlphasPlot the _numberOfAlphasPlot to set
	 */
	public void setNumberOfAlphasPlot(int _numberOfAlphasPlot) {
		this._numberOfAlphasPlot = _numberOfAlphasPlot;
	}

	public double getVortexSemiSpanToSemiSpanRatio() {
		return _vortexSemiSpanToSemiSpanRatio;
	}

	public void setVortexSemiSpanToSemiSpanRatio(double _vortexSemiSpanToSemiSpanRatio) {
		this._vortexSemiSpanToSemiSpanRatio = _vortexSemiSpanToSemiSpanRatio;
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
	/**
	 * @return the _currentLiftCoefficient
	 */
	public Double getCurrentLiftCoefficient() {
		return _currentLiftCoefficient;
	}

	/**
	 * @param _currentLiftCoefficient the _currentLiftCoefficient to set
	 */
	public void setCurrentLiftCoefficient(Double _currentLiftCoefficient) {
		this._currentLiftCoefficient = _currentLiftCoefficient;
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
	/**
	 * @return the _cLAtAplhaActual
	 */
	public Map <MethodEnum, Double> getCLAtAplhaActual() {
		return _cLAtAplhaActual;
	}

	/**
	 * @param _cLAtAplhaActual the _cLAtAplhaActual to set
	 */
	public void setCLAtAplhaActual(Map <MethodEnum, Double> _cLAtAplhaActual) {
		this._cLAtAplhaActual = _cLAtAplhaActual;
	}

	public Map<MethodEnum, Double[]> getLiftCoefficient3DCurve() {
		return _liftCoefficient3DCurve;
	}
	public void setLiftCoefficient3DCurve(Map<MethodEnum, Double[]> _liftCoefficient3DCurve) {
		this._liftCoefficient3DCurve = _liftCoefficient3DCurve;
	}
	public Map<MethodEnum, List<List<Double>>> getLiftCoefficientDistribution() {
		return _liftCoefficientDistribution;
	}
	public void setLiftCoefficientDistribution(Map<MethodEnum, List<List<Double>>> _liftCoefficientDistribution) {
		this._liftCoefficientDistribution = _liftCoefficientDistribution;
	}
	public Map<MethodEnum, List<List<Amount<Force>>>> getLiftDistribution() {
		return _liftDistribution;
	}
	public void setLiftDistribution(Map<MethodEnum, List<List<Amount<Force>>>> _liftDistribution) {
		this._liftDistribution = _liftDistribution;
	}
	public Map<MethodEnum, List<List<Double>>> getLiftCoefficientDistributionBasicLoad() {
		return _liftCoefficientDistributionBasicLoad;
	}
	public void setLiftCoefficientDistributionBasicLoad(
			Map<MethodEnum, List<List<Double>>> _liftCoefficientDistributionBasicLoad) {
		this._liftCoefficientDistributionBasicLoad = _liftCoefficientDistributionBasicLoad;
	}
	public Map<MethodEnum, List<List<Amount<Force>>>> getBasicLoadDistribution() {
		return _basicLoadDistribution;
	}
	public void setBasicLoadDistribution(Map<MethodEnum, List<List<Amount<Force>>>> _basicLoadDistribution) {
		this._basicLoadDistribution = _basicLoadDistribution;
	}
	
	public Map <MethodEnum, double[]> getLiftCoefficientDistributionAtCLMax() {
		return _liftCoefficientDistributionAtCLMax;
	}

	public void setLiftCoefficientDistributionAtCLMax(Map <MethodEnum, double[]> _liftCoefficientDistributionAtCLMax) {
		this._liftCoefficientDistributionAtCLMax = _liftCoefficientDistributionAtCLMax;
	}

	public Map<MethodEnum, List<List<Double>>> getLiftCoefficientDistributionAdditionalLoad() {
		return _liftCoefficientDistributionAdditionalLoad;
	}
	public void setLiftCoefficientDistributionAdditionalLoad(
			Map<MethodEnum, List<List<Double>>> _liftCoefficientDistributionAdditionalLoad) {
		this._liftCoefficientDistributionAdditionalLoad = _liftCoefficientDistributionAdditionalLoad;
	}
	public Map<MethodEnum, List<List<Amount<Force>>>> getAdditionalLoadDistribution() {
		return _additionalLoadDistribution;
	}
	public void setAdditionalLoadDistribution(Map<MethodEnum, List<List<Amount<Force>>>> _additionalLoadDistribution) {
		this._additionalLoadDistribution = _additionalLoadDistribution;
	}
	public Map<MethodEnum, List<List<Amount<Length>>>> getCclDistributionBasicLoad() {
		return _cclDistributionBasicLoad;
	}
	public void setCclDistributionBasicLoad(Map<MethodEnum, List<List<Amount<Length>>>> _cclDistributionBasicLoad) {
		this._cclDistributionBasicLoad = _cclDistributionBasicLoad;
	}
	public Map<MethodEnum, List<List<Amount<Length>>>> getCclDistributionAdditionalLoad() {
		return _cclDistributionAdditionalLoad;
	}
	public void setCclDistributionAdditionalLoad(Map<MethodEnum, List<List<Amount<Length>>>> _cclDistributionAdditionalLoad) {
		this._cclDistributionAdditionalLoad = _cclDistributionAdditionalLoad;
	}
	public Map<MethodEnum, List<List<Amount<Length>>>> getCclDistribution() {
		return _cclDistribution;
	}
	public void setCclDistribution(Map<MethodEnum, List<List<Amount<Length>>>> _cclDistribution) {
		this._cclDistribution = _cclDistribution;
	}
	public Map<MethodEnum, List<List<Double>>> getGammaDistributionBasicLoad() {
		return _gammaDistributionBasicLoad;
	}
	public void setGammaDistributionBasicLoad(Map<MethodEnum, List<List<Double>>> _gammaDistributionBasicLoad) {
		this._gammaDistributionBasicLoad = _gammaDistributionBasicLoad;
	}
	public Map<MethodEnum, List<List<Double>>> getGammaDistributionAdditionalLoad() {
		return _gammaDistributionAdditionalLoad;
	}
	public void setGammaDistributionAdditionalLoad(Map<MethodEnum, List<List<Double>>> _gammaDistributionAdditionalLoad) {
		this._gammaDistributionAdditionalLoad = _gammaDistributionAdditionalLoad;
	}
	public Map<MethodEnum, List<List<Double>>> getGammaDistribution() {
		return _gammaDistribution;
	}
	public void setGammaDistribution(Map<MethodEnum, List<List<Double>>> _gammaDistribution) {
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
	public Map<MethodEnum, List<Double>> getDeltaCl0FlapList() {
		return _deltaCl0FlapList;
	}
	public void setDeltaCl0FlapList(Map<MethodEnum, List<Double>> _deltaCl0FlapList) {
		this._deltaCl0FlapList = _deltaCl0FlapList;
	}
	public Map<MethodEnum, Double> getDeltaCl0Flap() {
		return _deltaCl0Flap;
	}
	public void setDeltaCl0Flap(Map<MethodEnum, Double> _deltaCl0Flap) {
		this._deltaCl0Flap = _deltaCl0Flap;
	}
	public Map<MethodEnum, List<Double>> getDeltaCL0FlapList() {
		return _deltaCL0FlapList;
	}
	public void setDeltaCL0FlapList(Map<MethodEnum, List<Double>> _deltaCL0FlapList) {
		this._deltaCL0FlapList = _deltaCL0FlapList;
	}
	public Map<MethodEnum, Double> getDeltaCL0Flap() {
		return _deltaCL0Flap;
	}
	public void setDeltaCL0Flap(Map<MethodEnum, Double> _deltaCL0Flap) {
		this._deltaCL0Flap = _deltaCL0Flap;
	}
	public Map<MethodEnum, List<Double>> getDeltaClmaxFlapList() {
		return _deltaClmaxFlapList;
	}
	public void setDeltaClmaxFlapList(Map<MethodEnum, List<Double>> _deltaClmaxFlapList) {
		this._deltaClmaxFlapList = _deltaClmaxFlapList;
	}
	public Map<MethodEnum, Double> getDeltaClmaxFlap() {
		return _deltaClmaxFlap;
	}
	public void setDeltaClmaxFlap(Map<MethodEnum, Double> _deltaClmaxFlap) {
		this._deltaClmaxFlap = _deltaClmaxFlap;
	}
	public Map<MethodEnum, List<Double>> getDeltaCLmaxFlapList() {
		return _deltaCLmaxFlapList;
	}
	public void setDeltaCLmaxFlapList(Map<MethodEnum, List<Double>> _deltaCLmaxFlapList) {
		this._deltaCLmaxFlapList = _deltaCLmaxFlapList;
	}
	public Map<MethodEnum, Double> getDeltaCLmaxFlap() {
		return _deltaCLmaxFlap;
	}
	public void setDeltaCLmaxFlap(Map<MethodEnum, Double> _deltaCLmaxFlap) {
		this._deltaCLmaxFlap = _deltaCLmaxFlap;
	}
	public Map<MethodEnum, List<Double>> getDeltaClmaxSlatList() {
		return _deltaClmaxSlatList;
	}
	public void setDeltaClmaxSlatList(Map<MethodEnum, List<Double>> _deltaClmaxSlatList) {
		this._deltaClmaxSlatList = _deltaClmaxSlatList;
	}
	public Map<MethodEnum, Double> getDeltaClmaxSlat() {
		return _deltaClmaxSlat;
	}
	public void setDeltaClmaxSlat(Map<MethodEnum, Double> _deltaClmaxSlat) {
		this._deltaClmaxSlat = _deltaClmaxSlat;
	}
	public Map<MethodEnum, List<Double>> getDeltaCLmaxSlatList() {
		return _deltaCLmaxSlatList;
	}
	public void setDeltaCLmaxSlatList(Map<MethodEnum, List<Double>> _deltaCLmaxSlatList) {
		this._deltaCLmaxSlatList = _deltaCLmaxSlatList;
	}
	public Map<MethodEnum, Double> getDeltaCLmaxSlat() {
		return _deltaCLmaxSlat;
	}
	public void setDeltaCLmaxSlat(Map<MethodEnum, Double> _deltaCLmaxSlat) {
		this._deltaCLmaxSlat = _deltaCLmaxSlat;
	}
	public Map<MethodEnum, List<Double>> getDeltaCDList() {
		return _deltaCDList;
	}
	public void setDeltaCDList(Map<MethodEnum, List<Double>> _deltaCDList) {
		this._deltaCDList = _deltaCDList;
	}
	public Map<MethodEnum, Double> getDeltaCD() {
		return _deltaCD;
	}
	public void setDeltaCD(Map<MethodEnum, Double> _deltaCD) {
		this._deltaCD = _deltaCD;
	}
	public Map<MethodEnum, List<Double>> getDeltaCMc4List() {
		return _deltaCMc4List;
	}
	public void setDeltaCMc4List(Map<MethodEnum, List<Double>> _deltaCMc4List) {
		this._deltaCMc4List = _deltaCMc4List;
	}
	public Map<MethodEnum, Double> getDeltaCMc4() {
		return _deltaCMc4;
	}
	public void setDeltaCMc4(Map<MethodEnum, Double> _deltaCMc4) {
		this._deltaCMc4 = _deltaCMc4;
	}

	/**
	 * @return the _alphaArrayPlot
	 */
	public Double[] getAlphaArrayPlot() {
		return _alphaArrayPlot;
	}

	/**
	 * @param _alphaArrayPlot the _alphaArrayPlot to set
	 */
	public void setAlphaArrayPlot(Double[] _alphaArrayPlot) {
		this._alphaArrayPlot = _alphaArrayPlot;
	}

	/**
	 * @return the _alphaArrayPlotHighLift
	 */
	public Double[] getAlphaArrayPlotHighLift() {
		return _alphaArrayPlotHighLift;
	}

	/**
	 * @param _alphaArrayPlotHighLift the _alphaArrayPlotHighLift to set
	 */
	public void setAlphaArrayPlotHighLift(Double[] _alphaArrayPlotHighLift) {
		this._alphaArrayPlotHighLift = _alphaArrayPlotHighLift;
	}

	public double[] getEtaStationDistribution() {
		return _etaStationDistribution;
	}

	public void setEtaStationDistribution(double[] _etaStationDistribution) {
		this._etaStationDistribution = _etaStationDistribution;
	}

	public List<Amount<Length>> getYStationDistribution() {
		return _yStationDistribution;
	}

	public void setYStationDistribution(List<Amount<Length>> _yStationDistribution) {
		this._yStationDistribution = _yStationDistribution;
	}

	/**
	 * @return the _chordDistribution
	 */
	public List<Amount<Length>> getChordDistribution() {
		return _chordDistribution;
	}

	/**
	 * @param _chordDistribution the _chordDistribution to set
	 */
	public void setChordDistribution(List<Amount<Length>> _chordDistribution) {
		this._chordDistribution = _chordDistribution;
	}

	/**
	 * @return the _ellipticalChordDistribution
	 */
	public List<Amount<Length>> getEllipticalChordDistribution() {
		return _ellipticalChordDistribution;
	}

	/**
	 * @param _ellipticalChordDistribution the _ellipticalChordDistribution to set
	 */
	public void setEllipticalChordDistribution(List<Amount<Length>> _ellipticalChordDistribution) {
		this._ellipticalChordDistribution = _ellipticalChordDistribution;
	}

	/**
	 * @return the _dihedralDistribution
	 */
	public List<Amount<Angle>> getDihedralDistribution() {
		return _dihedralDistribution;
	}

	/**
	 * @param _dihedralDistribution the _dihedralDistribution to set
	 */
	public void setDihedralDistribution(List<Amount<Angle>> _dihedralDistribution) {
		this._dihedralDistribution = _dihedralDistribution;
	}

	/**
	 * @return the _clAlphaDistribution
	 */
	public List<Amount<?>> getClAlphaDistribution() {
		return _clAlphaDistribution;
	}

	/**
	 * @param _clAlphaDistribution the _clAlphaDistribution to set
	 */
	public void setClAlphaDistribution(List<Amount<?>> _clAlphaDistribution) {
		this._clAlphaDistribution = _clAlphaDistribution;
	}

	/**
	 * @return the _xLEDistribution
	 */
	public List<Amount<Length>> getXLEDistribution() {
		return _xLEDistribution;
	}

	/**
	 * @param _xLEDistribution the _xLEDistribution to set
	 */
	public void setXLEDistribution(List<Amount<Length>> _xLEDistribution) {
		this._xLEDistribution = _xLEDistribution;
	}

	/**
	 * @return the _clMaxDistribution
	 */
	public List<Double> getClMaxDistribution() {
		return _clMaxDistribution;
	}

	/**
	 * @param _clMaxDistribution the _clMaxDistribution to set
	 */
	public void setClMaxDistribution(List<Double> _clMaxDistribution) {
		this._clMaxDistribution = _clMaxDistribution;
	}
}
