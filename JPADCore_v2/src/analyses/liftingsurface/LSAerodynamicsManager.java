package analyses.liftingsurface;

import static java.lang.Math.toRadians;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import analyses.OperatingConditions;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.AirfoilCalc;
import calculators.aerodynamics.AnglesCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.MomentCalc;
import calculators.aerodynamics.NasaBlackwell;
import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.HighLiftDeviceEffectEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class LSAerodynamicsManager {

	/*
	 *******************************************************************************
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
	private ConditionEnum _theCondition;
	private int _numberOfPointSemiSpanWise;
	private Amount<Length> _momentumPole;
	private List<Amount<Angle>> _alphaForDistribution;
	private int _numberOfAlphas;
	private int _numberOfAlphasPlot;
	private double _vortexSemiSpanToSemiSpanRatio;
	private List<Amount<Angle>> _alphaArray;
	private List<Amount<Angle>> _alphaArrayClean; 
	private Amount<Length> _currentAltitude;
	private Double _currentMachNumber;
	private Amount<Angle> _currentAlpha;
	private double[] _etaStationDistribution; 
	private List<Amount<Length>> _yStationDistribution;
	private List<Amount<Angle>> _alphaZeroLiftDistribution;
	private List<Double> _clZeroDistribution;
	private List<Amount<Angle>> _twistDistribution;
	private List<Amount<Length>> _chordDistribution;
	private List<Amount<Length>> _ellipticalChordDistribution;
	private List<Amount<Angle>> _dihedralDistribution;
	private List<Amount<?>> _clAlphaDistribution;
	private List<Amount<Length>> _xLEDistribution;
	private List<Amount<Length>> _xACDistribution; // distance from the leading edge of each airfoil AC
	private List<Double> _clMaxDistribution;
	private List<Double> _cmACDistribution;
	private List<Amount<Length>> _airfoilACToWingACDistribution;
	private double[] twistDistributionRadians;
	private double[] alphaZeroLiftDistributionRadians;
	private double[] dihedralDistributionRadians;
	
	private NasaBlackwell theNasaBlackwellCalculator;
	NasaBlackwell theNasaBlackwellCalculatorAlphaZeroLift;
	
	private List<List<Double>> _discretizedAirfoilsCl;
	private List<Double> _clForCdMatrix;
	private List<List<Double>> _discretizedAirfoilsCd;
	private List<Double> _clForCmMatrix;
	private List<List<Double>> _discretizedAirfoilsCm;
	
	// CRITICAL MACH NUMBER
	private Map <MethodEnum, Double> _criticalMachNumber;
	
	// Xac
	/**
	 * MRF = Mean aerodynamic chord Reference Frame
	 * LRF = Local Reference Frame (of the lifting surface)
	 */
	private Map<MethodEnum, Double> _xacMRF; 
	private Map<MethodEnum, Amount<Length>> _xacLRF; 
	
	// LIFT 
	private Map <MethodEnum, Amount<Angle>> _alphaZeroLift;
	private Map <MethodEnum, Amount<Angle>> _alphaStar;
	private Map <MethodEnum, Amount<Angle>> _alphaMaxLinear;
	private Map <MethodEnum, Amount<Angle>> _alphaStall;
	private Map <MethodEnum, Double> _cLZero;
	private Map <MethodEnum, Double> _cLStar;
	private Map <MethodEnum, Double> _cLMax;
	private Map <MethodEnum, Amount<?>> _cLAlpha;
	private Map <MethodEnum, Double> _cLAtAlpha;
	private Map <MethodEnum, Double[]> _liftCoefficient3DCurve;
	private Map <MethodEnum, Double[]> _liftCoefficient3DCurveHighLift;
	private Map <MethodEnum, Double[]> _polar3DCurveHighLift;
	private Map <MethodEnum, Double[]> _momentCoefficient3DCurveHighLift;
	private Map <MethodEnum, double[]> _liftCoefficientDistributionAtCLMax;
	private Map <MethodEnum, Map<Amount<Angle>, List<Double>>> _liftCoefficientDistribution;
	private Map <MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> _liftDistribution;
	private Map <MethodEnum, Map<Amount<Angle>, List<Double>>> _liftCoefficientDistributionBasicLoad;
	private Map <MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> _basicLoadDistribution;
	private Map <MethodEnum, Map<Amount<Angle>, List<Double>>> _liftCoefficientDistributionAdditionalLoad;
	private Map <MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> _additionalLoadDistribution;
	private Map <MethodEnum, Map<Amount<Angle>, List<Amount<Length>>>> _cclDistributionBasicLoad;
	private Map <MethodEnum, Map<Amount<Angle>, List<Amount<Length>>>> _cclDistributionAdditionalLoad;
	private Map <MethodEnum, Map<Amount<Angle>, List<Amount<Length>>>> _cclDistribution;
	private Map <MethodEnum, Map<Amount<Angle>, List<Double>>> _gammaDistributionBasicLoad;
	private Map <MethodEnum, Map<Amount<Angle>, List<Double>>> _gammaDistributionAdditionalLoad;
	private Map <MethodEnum, Map<Amount<Angle>, List<Double>>> _gammaDistribution;
	
	// HIGH LIFT
	private Map <MethodEnum, Double> _cLAtAlphaHighLift;
	private Map <MethodEnum, Double> _cDAtAlphaHighLift;
	private Map <MethodEnum, Double> _cMAtAlphaHighLift;
	private Map <MethodEnum, Amount<Angle>> _alphaZeroLiftHighLift;
	private Map <MethodEnum, Amount<Angle>> _alphaStarHighLift;
	private Map <MethodEnum, Amount<Angle>> _alphaStallHighLift;
	private Map <MethodEnum, Double> _cLZeroHighLift;
	private Map <MethodEnum, Double> _cLStarHighLift;
	private Map <MethodEnum, Double> _cLMaxHighLift;
	private Map <MethodEnum, Amount<?>> _cLAlphaHighLift;
	private Map <MethodEnum, Double> _cD0HighLift;
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
	private Map <MethodEnum, List<Double>> _deltaCD0List;
	private Map <MethodEnum, Double> _deltaCD0;
	private Map <MethodEnum, List<Double>> _deltaCMc4List;
	private Map <MethodEnum, Double> _deltaCMc4;
	
	// DRAG
	private Map <MethodEnum, Double> _cD0;
	private Map <MethodEnum, List<Double>> _cDParasite;
	private Map <MethodEnum, Double> _oswaldFactor;
	private Map <MethodEnum, Double> _cDInduced;
	private Map <MethodEnum, Double> _cDWave;
	private Map <MethodEnum, Double[]> _polar3DCurve;
	private Map <MethodEnum, Map<Amount<Angle>, List<Double>>> _parasiteDragCoefficientDistribution;
	private Map <MethodEnum, Map<Amount<Angle>, List<Double>>> _inducedDragCoefficientDistribution;
	private Map <MethodEnum, Map<Amount<Angle>, List<Double>>> _dragCoefficientDistribution;
	private Map <MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> _dragDistribution;
	private Map <MethodEnum, Double> _cDAtAlpha;

	// PITCHING MOMENT
	private Map <MethodEnum, Double> _cMac;
	private Map <MethodEnum, Amount<?>> _cMAlpha;
	private Map <MethodEnum, Double> _cMAtAlpha;
	private Map <MethodEnum, Double[]> _moment3DCurve;
	private Map <MethodEnum, Map<Amount<Angle>, List<Double>>> _momentCoefficientDistribution;
	private Map <MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> _momentDistribution;
	
	
	//------------------------------------------------------------------------------
	// BUILDER
	//------------------------------------------------------------------------------
	public LSAerodynamicsManager (
			LiftingSurface theLiftingSurface,
			OperatingConditions theOperatingConditions,
			ConditionEnum theCondition,
			int numberOfPointSemiSpanWise,
			List<Amount<Angle>> alphaArray,
			List<Amount<Angle>> alphaForDistribution,
			Amount<Length> momentumPole
			) {
		
		this._theLiftingSurface = theLiftingSurface;
		this._theOperatingConditions = theOperatingConditions;
		this._theCondition = theCondition;
		this._numberOfPointSemiSpanWise = numberOfPointSemiSpanWise;
		this._alphaArray = alphaArray;
		this._alphaForDistribution = alphaForDistribution;
		this._momentumPole = momentumPole;
		
		initializeVariables();
		initializeData(_theCondition);
		
	}
	
	//------------------------------------------------------------------------------
	// METHODS
	//------------------------------------------------------------------------------
	private void initializeData(ConditionEnum theCondition) {
		
		
		switch (theCondition) {
		case TAKE_OFF:
			this._currentMachNumber = this._theOperatingConditions.getMachTakeOff();
			this._currentAltitude = this._theOperatingConditions.getAltitudeTakeOff();
			this._currentAlpha = this._theOperatingConditions.getAlphaCurrentTakeOff();
			break;
		case CLIMB:
			this._currentMachNumber = this._theOperatingConditions.getMachClimb();
			this._currentAltitude = this._theOperatingConditions.getAltitudeClimb();
			this._currentAlpha = this._theOperatingConditions.getAlphaCurrentClimb();
			break;
		case CRUISE:
			this._currentMachNumber = this._theOperatingConditions.getMachCruise();
			this._currentAltitude = this._theOperatingConditions.getAltitudeCruise();
			this._currentAlpha = this._theOperatingConditions.getAlphaCurrentCruise();
			break;
		case LANDING:
			this._currentMachNumber = this._theOperatingConditions.getMachLanding();
			this._currentAltitude = this._theOperatingConditions.getAltitudeLanding();
			this._currentAlpha = this._theOperatingConditions.getAlphaCurrentLanding();
			break;
		default:
			break;
		}
		
		this._numberOfAlphas = _alphaArray.size();
		this._alphaArrayClean = new ArrayList<>();
		this._numberOfAlphasPlot = _alphaArray.size();
		this._vortexSemiSpanToSemiSpanRatio = 1.0/(2*_numberOfPointSemiSpanWise
				);
		
//		this._alphaArrayPlotHighLift = new Double[this._numberOfAlphasPlot];

		//----------------------------------------------------------------------------------------------------------------------
		// Initialize XAC
		if(_xacLRF.get(MethodEnum.DEYOUNG_HARPER) == null) {
			CalcXAC calcXAC = new CalcXAC();
			calcXAC.deYoungHarper();
		}
		
		if(_momentumPole == null)
			_momentumPole = _xacLRF.get(MethodEnum.DEYOUNG_HARPER);
		
		//----------------------------------------------------------------------------------------------------------------------
		// Calculating the mean airfoil
		//......................................................................................................................
		this._meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(_theLiftingSurface.getLiftingSurfaceCreator());
		
		//----------------------------------------------------------------------------------------------------------------------
		// Calculating lifting surface Form Factor
		//......................................................................................................................
		double compressibilityFactor = 1.
				/ Math.sqrt(
						1 - Math.pow(_currentMachNumber, 2)
						* (Math.pow(Math.cos(
								_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord()
									.doubleValue(SI.RADIAN)),2)
								)
						);
		this._theLiftingSurface.getLiftingSurfaceCreator().calculateThicknessMean();
		this._theLiftingSurface.getLiftingSurfaceCreator().calculateFormFactor(compressibilityFactor);
		
		//----------------------------------------------------------------------------------------------------------------------
		// Calculating airfoil parameter distributions
		//......................................................................................................................
		// ETA STATIONS AND Y STATIONS
		this._etaStationDistribution = MyArrayUtils.linspace(0, 1, _numberOfPointSemiSpanWise);
		this._yStationDistribution = new ArrayList<Amount<Length>>();
		double[] yStationDistributionArray = MyArrayUtils.linspace(
				0,
				_theLiftingSurface.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER),
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
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getAlpha0VsY()),
				yStationDistributionArray
				);
		for(int i=0; i<alphaZeroLiftDistributionArray.length; i++)
			_alphaZeroLiftDistribution.add(Amount.valueOf(alphaZeroLiftDistributionArray[i], NonSI.DEGREE_ANGLE));
		//......................................................................................................................
		// CL ZERO
		_clZeroDistribution = new ArrayList<Double>();
		Double[] clZeroDistributionArray = new Double[this._numberOfPointSemiSpanWise];
		clZeroDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertToDoublePrimitive(_theLiftingSurface.getLiftingSurfaceCreator().getCl0VsY()),
				yStationDistributionArray
				);
		for(int i=0; i<clZeroDistributionArray.length; i++)
			_clZeroDistribution.add(clZeroDistributionArray[i]);
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
				MyArrayUtils.convertListOfAmountodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getClAlphaVsY()),
				yStationDistributionArray
				);
		for(int i=0; i<clAlphaDistributionArray.length; i++)
			_clAlphaDistribution.add(Amount.valueOf(clAlphaDistributionArray[i], NonSI.DEGREE_ANGLE.inverse()));
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
		// Xac DISTRIBUTION
		this._xACDistribution = new ArrayList<Amount<Length>>();
		List<Double> xACBreakPoints = 
				_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints().stream()
				.map(y -> _theLiftingSurface.getLiftingSurfaceCreator().getAirfoilList()
							.get(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints().indexOf(y))
								.getXACNormalized()
									*_theLiftingSurface.getLiftingSurfaceCreator().getChordsBreakPoints()
										.get(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints().indexOf(y))
											.doubleValue(SI.METER)
								)
				.collect(Collectors.toList());
				
		Double[] xACDistributionArray = new Double[this._numberOfPointSemiSpanWise];
		xACDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertToDoublePrimitive(xACBreakPoints),
				yStationDistributionArray
				);
		for(int i=0; i<xACDistributionArray.length; i++)
			_xACDistribution.add(Amount.valueOf(xACDistributionArray[i], SI.METER));
		//......................................................................................................................
		// Clmax DISTRIBUTION
		this._clMaxDistribution = new ArrayList<Double>();
		Double[] clMaxDistributionArray = new Double[this._numberOfPointSemiSpanWise];
		clMaxDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertToDoublePrimitive(_theLiftingSurface.getLiftingSurfaceCreator().getClMaxVsY()),
				yStationDistributionArray
				);
		for(int i=0; i<clMaxDistributionArray.length; i++)
			_clMaxDistribution.add(clMaxDistributionArray[i]);
		//......................................................................................................................
		// CmAC DISTRIBUTION
		this._cmACDistribution = new ArrayList<Double>();
		Double[] cmACDistributionArray = new Double[this._numberOfPointSemiSpanWise];
		cmACDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertToDoublePrimitive(_theLiftingSurface.getLiftingSurfaceCreator().getCmACVsY()),
				yStationDistributionArray
				);
		for(int i=0; i<cmACDistributionArray.length; i++)
			_cmACDistribution.add(cmACDistributionArray[i]);
		//......................................................................................................................
		// AIRFOIL AC TO WING AC DISTRIBUTION
		this._airfoilACToWingACDistribution = new ArrayList<Amount<Length>>();
		_airfoilACToWingACDistribution = 
				_xLEDistribution.stream()
					.map(xle -> xle.doubleValue(SI.METER) 
								+ _xACDistribution.get(_xLEDistribution.indexOf(xle)).doubleValue(SI.METER)
								- _xacLRF.get(MethodEnum.DEYOUNG_HARPER).doubleValue(SI.METER)
							)
					.map(ac -> Amount.valueOf(ac, SI.METER))
					.collect(Collectors.toList());
		
		//----------------------------------------------------------------------------------------------------------------------
		// NASA BLACKEWLL CALCULATOR
		//......................................................................................................................
//		twistDistributionRadians = new double[_numberOfPointSemiSpanWise];
//		alphaZeroLiftDistributionRadians = new double[_numberOfPointSemiSpanWise];
		dihedralDistributionRadians = new double[_numberOfPointSemiSpanWise];
//		
		for (int i=0; i< _numberOfPointSemiSpanWise; i++) {
//			twistDistributionRadians[i] = _twistDistribution.get(i).doubleValue(SI.RADIAN);
//			alphaZeroLiftDistributionRadians[i] = _alphaZeroLiftDistribution.get(i).doubleValue(SI.RADIAN);
			dihedralDistributionRadians[i] = 0.0;
		}

		theNasaBlackwellCalculator = new NasaBlackwell(
				_theLiftingSurface.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER),
				_theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(_yStationDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_chordDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_xLEDistribution),
				dihedralDistributionRadians,
				_twistDistribution,
				_alphaZeroLiftDistribution,
				_vortexSemiSpanToSemiSpanRatio,
				0.0,
				_currentMachNumber,
				_currentAltitude.doubleValue(SI.METER)
				);
		
		theNasaBlackwellCalculatorAlphaZeroLift = new NasaBlackwell(
				_theLiftingSurface.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER),
				_theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(_yStationDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_chordDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_xLEDistribution),
				dihedralDistributionRadians,
				_twistDistribution,
				_alphaZeroLiftDistribution,
				_vortexSemiSpanToSemiSpanRatio,
				0.0,
				_currentMachNumber,
				_currentAltitude.doubleValue(SI.METER)
				);

		//----------------------------------------------------------------------------------------------------------------------
		// Initialize ALPHA ARRAY CLEAN
		if(_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.WING)) {
			if(theCondition.equals(ConditionEnum.TAKE_OFF) || theCondition.equals(ConditionEnum.LANDING)) {

				if(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
					CalcAlpha0L calcAlphaZeroLift = new CalcAlpha0L();
					calcAlphaZeroLift.integralMeanWithTwist();
				}

				if(_alphaStall.get(MethodEnum.NASA_BLACKWELL) == null) {
					CalcAlphaStall calcAlphaStall = new CalcAlphaStall();
					calcAlphaStall.fromAlphaMaxLinearNasaBlackwell(_currentMachNumber);
				}

				_alphaArrayClean = MyArrayUtils.convertDoubleArrayToListOfAmount(
						MyArrayUtils.linspace(
								_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST).doubleValue(NonSI.DEGREE_ANGLE)-2,
								_alphaStall.get(MethodEnum.NASA_BLACKWELL).doubleValue(NonSI.DEGREE_ANGLE)+2,
								_numberOfAlphas
								), 
						NonSI.DEGREE_ANGLE
						);

			}
			else
				_alphaArrayClean = _alphaArray;
		}
		else if(_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.HORIZONTAL_TAIL) 
				|| _theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)
				|| _theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.CANARD))
			_alphaArrayClean = _alphaArray;


		//----------------------------------------------------------------------------------------------------------------------
		// Initialize LIFTING COEFFICIENT 3D CURVE
		if(_liftCoefficient3DCurve.get(MethodEnum.NASA_BLACKWELL) == null){
			CalcLiftCurve calcLiftCurve = new CalcLiftCurve();
			calcLiftCurve.nasaBlackwell(_currentMachNumber);
		}

		//----------------------------------------------------------------------------------------------------------------------
		// Calculating discretized airfoil parameters arrays (needed only for Wing and HTail)
		//......................................................................................................................
		//		if(!_theLiftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {

		if (_discretizedAirfoilsCl.isEmpty()){
			List<List<Amount<Angle>>> alphaArrayBreakPointsListWing = new ArrayList<>();
			_theLiftingSurface.getLiftingSurfaceCreator().getAirfoilList().stream()
			.map(x -> alphaArrayBreakPointsListWing.add(x.getAlphaForClCurve()))
			.collect(Collectors.toList());

			List<List<Double>> clArrayBreakPointsListWing = new ArrayList<>();
			_theLiftingSurface.getLiftingSurfaceCreator().getAirfoilList().stream()
			.map(x -> clArrayBreakPointsListWing.add(x.getClCurve()))
			.collect(Collectors.toList());

			_discretizedAirfoilsCl = AirfoilCalc.calculateCLMatrixAirfoils(
					_alphaArrayClean, 
					alphaArrayBreakPointsListWing, 
					clArrayBreakPointsListWing,
					_theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints(),
					MyArrayUtils.convertDoubleArrayToListDouble(
							MyArrayUtils.convertFromDoubleToPrimitive(_etaStationDistribution)
							)
					);
		}

		if (_discretizedAirfoilsCd.isEmpty()){

			_clForCdMatrix = MyArrayUtils.convertDoubleArrayToListDouble(
					MyArrayUtils.linspaceDouble(
							-1.0,
							2.0,
							_numberOfAlphas
							)
					);

			List<List<Double>> clForCdArrayBreakPointsListWing = new ArrayList<>();
			_theLiftingSurface.getLiftingSurfaceCreator().getAirfoilList().stream()
			.map(x -> clForCdArrayBreakPointsListWing.add(x.getClForCdCurve()))
			.collect(Collectors.toList());

			List<List<Double>> cdArrayBreakPointsListWing = new ArrayList<>();
			_theLiftingSurface.getLiftingSurfaceCreator().getAirfoilList().stream()
			.map(x -> cdArrayBreakPointsListWing.add(x.getCdCurve()))
			.collect(Collectors.toList());

			_discretizedAirfoilsCd = AirfoilCalc.calculateAerodynamicCoefficientsMatrixAirfoils(
					_clForCdMatrix,	
					clForCdArrayBreakPointsListWing,
					cdArrayBreakPointsListWing,
					_theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints(),
					MyArrayUtils.convertDoubleArrayToListDouble(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_etaStationDistribution
									)
							)
					);

		}

		if (_discretizedAirfoilsCm.isEmpty()){

			_clForCmMatrix = MyArrayUtils.convertDoubleArrayToListDouble(
					MyArrayUtils.linspaceDouble(
							-1.0,
							2.0,
							_numberOfAlphas
							)
					);
			
			List<List<Double>> clForCmArrayBreakPointsListWing = new ArrayList<>();
			_theLiftingSurface.getLiftingSurfaceCreator().getAirfoilList().stream()
			.map(x -> clForCmArrayBreakPointsListWing.add(x.getClForCmCurve()))
			.collect(Collectors.toList());

			List<List<Double>> cmArrayBreakPointsListWing = new ArrayList<>();
			_theLiftingSurface.getLiftingSurfaceCreator().getAirfoilList().stream()
			.map(x -> cmArrayBreakPointsListWing.add(x.getCmCurve()))
			.collect(Collectors.toList());

			_discretizedAirfoilsCm = AirfoilCalc.calculateAerodynamicCoefficientsMatrixAirfoils(
					_clForCmMatrix,	
					clForCmArrayBreakPointsListWing,
					cmArrayBreakPointsListWing,
					_theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints(),
					MyArrayUtils.convertDoubleArrayToListDouble(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_etaStationDistribution
									)
							)
					);
		}
		//		}
	}
	
	private void initializeVariables() {
		
		this._criticalMachNumber = new HashMap<MethodEnum, Double>();
		
		this._xacMRF = new HashMap<MethodEnum, Double>();
		this._xacLRF = new HashMap<MethodEnum, Amount<Length>>();
		
		this._alphaZeroLift = new HashMap<MethodEnum, Amount<Angle>>();
		this._alphaStar = new HashMap<MethodEnum, Amount<Angle>>();
		this._alphaMaxLinear = new HashMap<MethodEnum, Amount<Angle>>();
		this._alphaStall = new HashMap<MethodEnum, Amount<Angle>>();
		this._cLZero = new HashMap<MethodEnum, Double>();
		this._cLStar = new HashMap<MethodEnum, Double>();
		this._cLMax = new HashMap<MethodEnum, Double>();
		this._cLAlpha = new HashMap<MethodEnum, Amount<?>>();
		this._cLAtAlpha = new HashMap<MethodEnum, Double>();
		this._liftCoefficient3DCurve = new HashMap<MethodEnum, Double[]>();
		this._liftCoefficientDistribution = new HashMap<>();
		this._liftCoefficientDistributionAtCLMax = new HashMap<>();
		this._liftDistribution = new HashMap<>();
		this._liftCoefficientDistributionBasicLoad = new HashMap<>();
		this._basicLoadDistribution = new HashMap<>();
		this._liftCoefficientDistributionAdditionalLoad = new HashMap<>();
		this._additionalLoadDistribution = new HashMap<>();
		this._cclDistributionBasicLoad = new HashMap<>();
		this._cclDistributionAdditionalLoad = new HashMap<>();
		this._cclDistribution = new HashMap<>();
		this._gammaDistributionBasicLoad = new HashMap<>();
		this._gammaDistributionAdditionalLoad = new HashMap<>();
		this._gammaDistribution = new HashMap<>();
		
		this._cLAtAlphaHighLift = new HashMap<MethodEnum, Double>();
		this._cDAtAlphaHighLift = new HashMap<MethodEnum, Double>();
		this._cMAtAlphaHighLift = new HashMap<MethodEnum, Double>();
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
		this._deltaCD0List = new HashMap<MethodEnum, List<Double>>();
		this._deltaCMc4List = new HashMap<MethodEnum, List<Double>>();
		this._deltaCl0Flap = new HashMap<MethodEnum, Double>();
		this._deltaCL0Flap = new HashMap<MethodEnum, Double>();
		this._deltaClmaxFlap = new HashMap<MethodEnum, Double>();
		this._deltaCLmaxFlap = new HashMap<MethodEnum, Double>();
		this._deltaClmaxSlat = new HashMap<MethodEnum, Double>();
		this._deltaCLmaxSlat = new HashMap<MethodEnum, Double>();
		this._deltaCD0 = new HashMap<MethodEnum, Double>();
		this._deltaCMc4 = new HashMap<MethodEnum, Double>();
		this._liftCoefficient3DCurveHighLift = new HashMap<MethodEnum, Double[]>();
		this._polar3DCurveHighLift = new HashMap<MethodEnum, Double[]>();
		this._momentCoefficient3DCurveHighLift = new HashMap<MethodEnum, Double[]>();
		
		this._cD0 = new HashMap<MethodEnum, Double>();
		this._cDParasite = new HashMap<MethodEnum, List<Double>>();
		this._oswaldFactor = new HashMap<MethodEnum, Double>();
		this._cDInduced = new HashMap<MethodEnum, Double>();
		this._cDWave = new HashMap<MethodEnum, Double>();
		this._polar3DCurve = new HashMap<MethodEnum, Double[]>();
		this._parasiteDragCoefficientDistribution = new HashMap<>();
		this._inducedDragCoefficientDistribution = new HashMap<>();
		this._dragCoefficientDistribution = new HashMap<>();
		this._dragDistribution = new HashMap<>();
		this._cDAtAlpha = new HashMap<>();
		
		this._discretizedAirfoilsCl = new ArrayList<>();
		this._clForCdMatrix = new ArrayList<>();
		this._discretizedAirfoilsCd = new ArrayList<>();
		this._clForCmMatrix = new ArrayList<>();
		this._discretizedAirfoilsCm = new ArrayList<>();
		
		this._cMac = new HashMap<MethodEnum, Double>();
		this._cMAlpha = new HashMap<MethodEnum, Amount<?>>();
		this._cMAtAlpha = new HashMap<MethodEnum, Double>();
		this._moment3DCurve = new HashMap<MethodEnum, Double[]>();
		this._momentCoefficientDistribution = new HashMap<>();
		this._momentDistribution = new HashMap<>();
		
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

			AirfoilTypeEnum airfoilType = _theLiftingSurface.getLiftingSurfaceCreator().getAirfoilList().get(0).getType();
			Amount<Angle> sweepHalfChordEq = _theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepHalfChord();
			double maxThicknessMean = _theLiftingSurface.getLiftingSurfaceCreator().getThicknessMean();
			
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

			AirfoilTypeEnum airfoilType = _theLiftingSurface.getLiftingSurfaceCreator().getAirfoilList().get(0).getType();
			Amount<Angle> sweepHalfChordEq = _theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepAtTrailingEdge();
			double maxThicknessMean = _theLiftingSurface.getLiftingSurfaceCreator().getThicknessMean();
			
			double machCr = AerodynamicCalc.calculateMachCriticalKroo(
					cL,
					sweepHalfChordEq.to(SI.RADIAN),
					maxThicknessMean,
					airfoilType
					);
			
			_criticalMachNumber.put(MethodEnum.KROO, machCr);
		}

	}
	//............................................................................
	// END OF THE CRITICAL MACH INNER CLASS
	//............................................................................

	//............................................................................
	// CALC XacCL INNER CLASS
	//............................................................................
	/** 
	 * Evaluate the AC x coordinate relative to MAC
	 */
	public class CalcXAC {

		public void atQuarterMAC() {
			_xacMRF.put(
					MethodEnum.QUARTER,
					0.25
					);
			_xacLRF.put(
					MethodEnum.QUARTER, 
					Amount.valueOf(
							_xacMRF.get(MethodEnum.QUARTER)
							*_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER),
							SI.METER)
						.plus(getTheLiftingSurface()
							.getLiftingSurfaceCreator()
								.getMeanAerodynamicChordLeadingEdgeX()
							)
					);
		}

		/**
		 * @see page 555 Sforza
		 */
		public void deYoungHarper() {
			_xacMRF.put(
					MethodEnum.DEYOUNG_HARPER,
							LSGeometryCalc.calcXacFromLEMacDeYoungHarper(
									_theLiftingSurface.getLiftingSurfaceCreator().getAspectRatio(),
									_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER), 
									_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio(),
									_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN)
									)
							/_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER)
					);
			_xacLRF.put(
					MethodEnum.DEYOUNG_HARPER,
					Amount.valueOf(
							_xacMRF.get(MethodEnum.DEYOUNG_HARPER)
							*_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER),
							SI.METER)
						.plus(getTheLiftingSurface()
							.getLiftingSurfaceCreator()
								.getMeanAerodynamicChordLeadingEdgeX()
								)
					);
		}

		/**
		 *  page 53 Napolitano 
		 */
		public void datcomNapolitano() {
			_xacMRF.put(
					MethodEnum.NAPOLITANO_DATCOM,
							LSGeometryCalc.calcXacFromNapolitanoDatcom(
									_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER),
									_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio(),
									_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().doubleValue(NonSI.DEGREE_ANGLE),
									_theLiftingSurface.getLiftingSurfaceCreator().getAspectRatio(),  
									_currentMachNumber,
									_theLiftingSurface.getAeroDatabaseReader()
									)
							/_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER)
					);
			_xacLRF.put(
					MethodEnum.NAPOLITANO_DATCOM,
					Amount.valueOf(
							_xacMRF.get(MethodEnum.NAPOLITANO_DATCOM)
							*_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER),
							SI.METER)
					.plus(getTheLiftingSurface()
							.getLiftingSurfaceCreator()
							.getMeanAerodynamicChordLeadingEdgeX()
							)
					);
		}

	}
	//............................................................................
	// END OF THE CALC XacCL INNER CLASS
	//............................................................................
	
	//............................................................................
	// CL AT APLHA INNER CLASS
	//............................................................................
	public class CalcCLAtAlpha {

		public double linearDLR(Amount<Angle> alpha) {
			// page 3 DLR pdf
			double cLActual = LiftCalc.calcCLatAlphaLinearDLR(
					alpha.doubleValue(SI.RADIAN),
					_theLiftingSurface.getLiftingSurfaceCreator().getAspectRatio()
					); 
			
			_cLAtAlpha.put(MethodEnum.LINEAR_DLR, cLActual);
			
			return cLActual;
		}

		/** 
		 * Evaluate CL at a specific AoA
		 * 
		 * @author Lorenzo Attanasio
		 * @return
		 */
		public double linearAndersonCompressibleSubsonic(Amount<Angle> alpha) {

			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				if(_cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.andersonSweptCompressibleSubsonic();
				}
			}
			else
				if(_cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.helmboldDiederich(_currentMachNumber);
				}
			
			Double cLAlpha = null;
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) 
				cLAlpha = _cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			else
				cLAlpha = _cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			
			if(_cLZero.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
				CalcCL0 calcCLZero = new CalcCL0();
				calcCLZero.andersonSweptCompressibleSubsonic();
			}
			
			double cLActual = cLAlpha*alpha.to(NonSI.DEGREE_ANGLE).getEstimatedValue() 
					+ _cLZero.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC);	
			
			_cLAtAlpha.put(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC, cLActual);
			
			return cLActual;
		}

		public double nasaBlackwellLinear(Amount<Angle> alpha) {

			theNasaBlackwellCalculator.calculate(alpha.to(SI.RADIAN));

			_cLAtAlpha.put(
					MethodEnum.LINEAR_NASA_BLACKWELL,
					theNasaBlackwellCalculator.getCLCurrent()
					);
			
			return theNasaBlackwellCalculator.getCLCurrent();
		}

		/**
		 * This method calculates CL at alpha given as input. It interpolates the values of cl and alpha array filled before.
		 * WARNING: it is necessary to call the method CalcCLvsAlphaCurve--> nasaBlackwellCompleteCurve before.
		 * 
		 * @author Manuela Ruocco
		 *
		 */		
		public double nasaBlackwellCompleteCurve(Amount<Angle> alpha) {

			double cLActual = 0.0;
			
			if ((_alphaArrayClean != null) 
					&& (_liftCoefficient3DCurve.get(MethodEnum.NASA_BLACKWELL) != null)) {
				cLActual = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_alphaArrayClean),
						MyArrayUtils.convertToDoublePrimitive(_liftCoefficient3DCurve.get(MethodEnum.NASA_BLACKWELL)),
						alpha.doubleValue(NonSI.DEGREE_ANGLE)
						);
			}
			else { 
				
				if(_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS) == null) {
					CalcAlphaStar calcAlphaStar = new CalcAlphaStar();
					calcAlphaStar.meanAirfoilWithInfluenceAreas();
				}
				
				if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) {
					if(_cLAlpha.get(MethodEnum.NASA_BLACKWELL) == null) {
						CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
						calcCLAlpha.nasaBlackwell();
					}
				}
				else
					if(_cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH) == null) {
						CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
						calcCLAlpha.helmboldDiederich(_currentMachNumber);
					}
				
				Double cLAlpha = null;
				if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) 
					cLAlpha = _cLAlpha.get(MethodEnum.NASA_BLACKWELL).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
				else
					cLAlpha = _cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
				
				if(alpha.doubleValue(NonSI.DEGREE_ANGLE)
						<= _alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS).doubleValue(NonSI.DEGREE_ANGLE)) { // linear trait

					if(_cLZero.get(MethodEnum.NASA_BLACKWELL) == null) {
						CalcCL0 calcCLZero = new CalcCL0();
						calcCLZero.nasaBlackwell();
					}
					
					cLActual = cLAlpha* alpha.doubleValue(NonSI.DEGREE_ANGLE)
							+ _cLZero.get(MethodEnum.NASA_BLACKWELL);
				}
				else { // complete curve 
					
					cLActual = LiftCalc.calculateCLAtAlphaNonLinearTrait(
							alpha,
							Amount.valueOf(cLAlpha, NonSI.DEGREE_ANGLE.inverse()),
							_cLStar.get(MethodEnum.NASA_BLACKWELL),
							_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS),
							_cLMax.get(MethodEnum.NASA_BLACKWELL),
							_alphaStall.get(MethodEnum.NASA_BLACKWELL)
							);
					
				}
			}
			_cLAtAlpha.put(MethodEnum.NASA_BLACKWELL, cLActual);
			
			return cLActual;
		}

	}
	//............................................................................
	// END OF THE CRITICAL MACH INNER CLASS
	//............................................................................
	
	//............................................................................
	// CL0 INNER CLASS
	//............................................................................
	public class CalcCL0  {

		public void andersonSweptCompressibleSubsonic() {
			
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				if(_cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.andersonSweptCompressibleSubsonic();
				}
			}
			else
				if(_cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.helmboldDiederich(_currentMachNumber);
				}
			
			Double cLAlpha = null;
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) 
				cLAlpha = _cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			else
				cLAlpha = _cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			
			if(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
				CalcAlpha0L calcAlphaZeroLift = new CalcAlpha0L();
				calcAlphaZeroLift.integralMeanWithTwist();
			}
			
			_cLZero.put(
					MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC,
					LiftCalc.calculateLiftCoefficientAtAlpha0(
							_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST).doubleValue(NonSI.DEGREE_ANGLE),
							cLAlpha
							)
					);
		}

		public void nasaBlackwell() {
			
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				if(_cLAlpha.get(MethodEnum.NASA_BLACKWELL) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.nasaBlackwell();
				}
			}
			else
				if(_cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.helmboldDiederich(_currentMachNumber);
				}
			
			Double cLAlpha = null;
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) 
				cLAlpha = _cLAlpha.get(MethodEnum.NASA_BLACKWELL).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			else
				cLAlpha = _cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			
			if(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
				CalcAlpha0L calcAlphaZeroLift = new CalcAlpha0L();
				calcAlphaZeroLift.integralMeanWithTwist();
			}
			
			_cLZero.put(
					MethodEnum.NASA_BLACKWELL,
					LiftCalc.calculateLiftCoefficientAtAlpha0(
							_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST).doubleValue(NonSI.DEGREE_ANGLE),
							cLAlpha
							)
					);
			
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
			Double[] twistDistribution = new Double[_numberOfPointSemiSpanWise];
			
			if ( _theLiftingSurface.getExposedLiftingSurface() != null && _theLiftingSurface.getLiftingSurfaceCreator().getType() == ComponentEnum.WING){
				surface = _theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE);
				semiSpan = _theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METRE);
				yStationDistribution = MyArrayUtils.linspace(
						0,
						_theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER),
						_numberOfPointSemiSpanWise
						);
				alphaZeroLiftDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getAlpha0VsY()),
						yStationDistribution
						);
				chordDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getChordsBreakPoints()),
						yStationDistribution
						);

			}
			else{
				surface = _theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE);
				semiSpan = _theLiftingSurface.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METRE);
				alphaZeroLiftDistribution = MyArrayUtils.convertListOfAmountToDoubleArray(_alphaZeroLiftDistribution);
				yStationDistribution = MyArrayUtils.linspace(
						0,
						_theLiftingSurface.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER),
						_numberOfPointSemiSpanWise
						);
				alphaZeroLiftDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getAlpha0VsY()),
						yStationDistribution
						);
				chordDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getChordsBreakPoints()),
						yStationDistribution
						);
				twistDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getTwistsBreakPoints()),
						yStationDistribution
						);
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
							NonSI.DEGREE_ANGLE)
					);
		}

		public void integralMeanWithTwist() {
			
			double surface;
			double semiSpan;
			double[] yStationDistribution = new double[_numberOfPointSemiSpanWise];
			Double[] chordDistribution = new Double[_numberOfPointSemiSpanWise];
			Double[] alphaZeroLiftDistribution = new Double[_numberOfPointSemiSpanWise];
			Double[] twistDistribution = new Double[_numberOfPointSemiSpanWise];
			
			if ( _theLiftingSurface.getExposedLiftingSurface() != null && _theLiftingSurface.getLiftingSurfaceCreator().getType() == ComponentEnum.WING){
				surface = _theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE);
				semiSpan = _theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METRE);
				yStationDistribution = MyArrayUtils.linspace(
						0,
						_theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER),
						_numberOfPointSemiSpanWise
						);
				alphaZeroLiftDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getAlpha0VsY()),
						yStationDistribution
						);
				chordDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getChordsBreakPoints()),
						yStationDistribution
						);
				twistDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getExposedLiftingSurface().getLiftingSurfaceCreator().getTwistsBreakPoints()),
						yStationDistribution
						);

			}
			else{
				surface = _theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE);
				semiSpan = _theLiftingSurface.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METRE);
				yStationDistribution = MyArrayUtils.linspace(
						0,
						_theLiftingSurface.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER),
						_numberOfPointSemiSpanWise
						);
				alphaZeroLiftDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getAlpha0VsY()),
						yStationDistribution
						);
				chordDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getChordsBreakPoints()),
						yStationDistribution
						);
				twistDistribution = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()),
						MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getTwistsBreakPoints()),
						yStationDistribution
						);
				
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
							NonSI.DEGREE_ANGLE)
					);
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
					_meanAirfoil.getAlphaEndLinearTrait().to(NonSI.DEGREE_ANGLE)
					);
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
							SI.RADIAN.inverse()
							)
					);
		}
		
		public void polhamus() {

			_cLAlpha.put(MethodEnum.POLHAMUS,
					Amount.valueOf(
							LiftCalc.calculateCLalphaPolhamus(
									_theLiftingSurface.getLiftingSurfaceCreator().getAspectRatio(),
									_currentMachNumber, 
									_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
									_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio()
									),
							SI.RADIAN.inverse()
							)
					);
		}

		/**
		 * pag. 49 ADAS
		 */
		public void andersonSweptCompressibleSubsonic() {

			_cLAlpha.put(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC,
					Amount.valueOf(
							LiftCalc.calcCLalphaAndersonSweptCompressibleSubsonic(
									_currentMachNumber,
									_theLiftingSurface.getLiftingSurfaceCreator().getAspectRatio(),
									_theLiftingSurface.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER),
									_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(NonSI.DEGREE_ANGLE), 
									MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedYs()),
									MyArrayUtils.convertListOfAmountodoubleArray(_clAlphaDistribution),
									MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedChords())
									),
							NonSI.DEGREE_ANGLE.inverse()
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
					Amount.valueOf(
							LiftCalc.calcCLalphaIntegralMean2D(
									_theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									_theLiftingSurface.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER),
									MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedYs()), 
									MyArrayUtils.convertListOfAmountodoubleArray(_clAlphaDistribution),
									MyArrayUtils.convertListOfAmountTodoubleArray(_theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedChords())
									),
							NonSI.DEGREE_ANGLE.inverse()
							)
					);
		}

		public void helmboldDiederich(Double mach) {
			_cLAlpha.put(
					MethodEnum.HELMBOLD_DIEDERICH, 
					Amount.valueOf(
							LiftCalc.calculateCLalphaHelmboldDiederich(
									_theLiftingSurface.getLiftingSurfaceCreator().getAspectRatio(),
									_meanAirfoil.getClAlphaLinearTrait().to(SI.RADIAN.inverse()).getEstimatedValue(),
									_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
									mach
									),
							SI.RADIAN.inverse()
							)
					);
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
			
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				if(_cLAlpha.get(MethodEnum.NASA_BLACKWELL) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.nasaBlackwell();
				}
			}
			else
				if(_cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.helmboldDiederich(_currentMachNumber);
				}
			
			Double cLAlpha = null;
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) 
				cLAlpha = _cLAlpha.get(MethodEnum.NASA_BLACKWELL).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			else
				cLAlpha = _cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			
			double result = LiftCalc.calculateCLmaxPhillipsAndAlley( //5.07
					_meanAirfoil.getClMax(),
					cLAlpha, 
					_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio().doubleValue(),
					_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().doubleValue(SI.RADIAN),
					_theLiftingSurface.getLiftingSurfaceCreator().getAspectRatio(),
					_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTwistAerodynamicAtTip().getEstimatedValue(),
					_theLiftingSurface.getAeroDatabaseReader()
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

			double[] alphaArrayNasaBlackwell = MyArrayUtils.linspace(0.0, 40, 41);
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

			if (_theLiftingSurface.getLiftingSurfaceCreator().getType() != ComponentEnum.VERTICAL_TAIL) {
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

		public void roskam() {
			
			double deltaYPercent = _theLiftingSurface.getAeroDatabaseReader()
					.getDeltaYvsThickness(
							_meanAirfoil.getThicknessToChordRatio(),
							_meanAirfoil.getFamily()
							);
			
			_cLMax.put(
					MethodEnum.ROSKAM, 
					_theLiftingSurface.getAeroDatabaseReader().getClmaxCLmaxVsLambdaLEVsDeltaY(
							_theLiftingSurface.getLiftingSurfaceCreator().getPanels().get(0).getSweepLeadingEdge().doubleValue(NonSI.DEGREE_ANGLE), 
							deltaYPercent
							)
					*_meanAirfoil.getClMax()
					);
			
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
					
				if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) {
					if(_cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
						CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
						calcCLAlpha.andersonSweptCompressibleSubsonic();
					}
				}
				else
					if(_cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH) == null) {
						CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
						calcCLAlpha.helmboldDiederich(_currentMachNumber);
					}
				
				Double cLAlpha = null;
				if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) 
					cLAlpha = _cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
				else
					cLAlpha = _cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
				
				if(_cLMax.get(MethodEnum.PHILLIPS_ALLEY) == null) {
					CalcCLmax theCLMaxCalculator = new CalcCLmax();
					theCLMaxCalculator.phillipsAndAlley();
				}
				
				_alphaMaxLinear.put(
						MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC,
						Amount.valueOf(
								(_cLMax.get(MethodEnum.PHILLIPS_ALLEY) - _cLZero.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC))
								/cLAlpha,
								NonSI.DEGREE_ANGLE
								)
						);
			}
			
			double deltaYPercent = _theLiftingSurface.getAeroDatabaseReader()
					.getDeltaYvsThickness(
							_meanAirfoil.getThicknessToChordRatio(),
							_meanAirfoil.getFamily()
							);
			
			Amount<Angle> deltaAlpha = Amount.valueOf(
					_theLiftingSurface.getAeroDatabaseReader()
					.getDAlphaVsLambdaLEVsDy(
							_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().doubleValue(NonSI.DEGREE_ANGLE),
							deltaYPercent
							),
					NonSI.DEGREE_ANGLE);
			
			_alphaStall.put(
					MethodEnum.PHILLIPS_ALLEY,
					_alphaMaxLinear.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC)
					.plus(deltaAlpha)
					);
		}
		
		public void fromAlphaMaxLinearNasaBlackwell(double mach) {
			
			if(_alphaMaxLinear.get(MethodEnum.NASA_BLACKWELL) == null) {
				if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) {
					CalcCLmax theCLMaxCalculator = new CalcCLmax();
					theCLMaxCalculator.nasaBlackwell();
				}
				else {
					CalcCLmax theCLMaxCalculator = new CalcCLmax();
					theCLMaxCalculator.roskam();
					CalcCLAlpha theCLAlphaCalculator = new CalcCLAlpha();
					theCLAlphaCalculator.helmboldDiederich(mach);
					_alphaMaxLinear.put(
							MethodEnum.LINEAR, 
							Amount.valueOf(
									_cLMax.get(MethodEnum.ROSKAM)
									/_cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
									,
									NonSI.DEGREE_ANGLE
									)
							);
				}
			}
			
			double deltaYPercent = _theLiftingSurface.getAeroDatabaseReader()
					.getDeltaYvsThickness(
							_meanAirfoil.getThicknessToChordRatio(),
							_meanAirfoil.getFamily()
							);
			
			Amount<Angle> deltaAlpha = Amount.valueOf(
					_theLiftingSurface.getAeroDatabaseReader()
					.getDAlphaVsLambdaLEVsDy(
							_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().doubleValue(NonSI.DEGREE_ANGLE),
							deltaYPercent
							),
					NonSI.DEGREE_ANGLE);
			
			if(_theLiftingSurface.getLiftingSurfaceCreator().getType() != ComponentEnum.VERTICAL_TAIL)
				_alphaStall.put(
						MethodEnum.NASA_BLACKWELL,
						_alphaMaxLinear.get(MethodEnum.NASA_BLACKWELL)
						.plus(deltaAlpha)
						);
			else
				_alphaStall.put(
						MethodEnum.NASA_BLACKWELL,
						_alphaMaxLinear.get(MethodEnum.LINEAR)
						.plus(deltaAlpha)
						);
		}
		
	}
	//............................................................................
	// END OF THE ALPHA STALL INNER CLASS
	//............................................................................
	
	//............................................................................
	// LIFT CURVE INNER CLASS
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
			
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				if(_cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.andersonSweptCompressibleSubsonic();
				}
			}
			else
				if(_cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.helmboldDiederich(_currentMachNumber);
				}
			
			Double cLAlpha = null;
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) 
				cLAlpha = _cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			else
				cLAlpha = _cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			
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
			
//			_alphaArrayPlot = MyArrayUtils.linspaceDouble(
//					_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST).doubleValue(NonSI.DEGREE_ANGLE)-2,
//					_alphaStall.get(MethodEnum.PHILLIPS_ALLEY).doubleValue(NonSI.DEGREE_ANGLE) + 3,
//					_numberOfAlphasPlot
//					);
			
			_liftCoefficient3DCurve.put(
					MethodEnum.PHILLIPS_ALLEY,
					LiftCalc.calculateCLvsAlphaArray(
							_cLZero.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC),
							_cLMax.get(MethodEnum.PHILLIPS_ALLEY),
							_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS),
							_alphaStall.get(MethodEnum.PHILLIPS_ALLEY),
							Amount.valueOf(cLAlpha, NonSI.DEGREE_ANGLE.inverse()),
							MyArrayUtils.convertListOfAmountToDoubleArray(_alphaArrayClean)
							)
					);
		}
		
		public void nasaBlackwell(double mach) {
			
			if(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
				CalcAlpha0L calcAlphaZeroLift = new CalcAlpha0L();
				calcAlphaZeroLift.integralMeanWithTwist();
			}
			
			if(_cLZero.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCL0 calcCLZero = new CalcCL0();
				calcCLZero.nasaBlackwell();
			}
			

			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				if(_cLAlpha.get(MethodEnum.NASA_BLACKWELL) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.nasaBlackwell();;
				}
			}
			else
				if(_cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.helmboldDiederich(_currentMachNumber);
				}
			
			Double cLAlpha = null;
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) 
				cLAlpha = _cLAlpha.get(MethodEnum.NASA_BLACKWELL).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			else
				cLAlpha = _cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			
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
				calcAlphaStall.fromAlphaMaxLinearNasaBlackwell(mach);
			}
			
			if(_cLMax.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCLmax calcCLmax = new CalcCLmax();
				calcCLmax.nasaBlackwell();
			}
			
			_liftCoefficient3DCurve.put(
					MethodEnum.NASA_BLACKWELL,
					LiftCalc.calculateCLvsAlphaArray(
							_cLZero.get(MethodEnum.NASA_BLACKWELL),
							_cLMax.get(MethodEnum.NASA_BLACKWELL),
							_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS),
							_alphaStall.get(MethodEnum.NASA_BLACKWELL),
							Amount.valueOf(cLAlpha, NonSI.DEGREE_ANGLE.inverse()),
							MyArrayUtils.convertListOfAmountToDoubleArray(_alphaArrayClean)
							)
					);
		}
		
	}		
	//............................................................................
	// END OF THE LIFT CURVE INNER CLASS
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
								((4*_theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE))
										/(Math.PI*_theLiftingSurface.getLiftingSurfaceCreator().getSpan().doubleValue(SI.METER)))
								*Math.sqrt(1-Math.pow(_etaStationDistribution[i],2)),
								SI.METER
								)
						);

			CalcCLAtAlpha theCLAtAlphaCalculator = new CalcCLAtAlpha();

			Map<Amount<Angle>, List<Amount<Length>>> ccLAdditionalSchrenkMap = new HashMap<>();
			Map<Amount<Angle>, List<Amount<Length>>> ccLBasicSchrenkMap = new HashMap<>();
			Map<Amount<Angle>, List<Amount<Length>>> ccLTotalSchrenkMap = new HashMap<>();
			Map<Amount<Angle>, List<Double>> gammaAdditionalSchrenkMap = new HashMap<>();
			Map<Amount<Angle>, List<Double>> gammaBasicSchrenkMap = new HashMap<>();
			Map<Amount<Angle>, List<Double>> gammaTotalSchrenkMap = new HashMap<>();
			Map<Amount<Angle>, List<Double>> liftCoefficientAdditionalSchrenkMap = new HashMap<>();
			Map<Amount<Angle>, List<Double>> liftCoefficientBasicSchrenkMap = new HashMap<>();
			Map<Amount<Angle>, List<Double>> liftCoefficientTotalSchrenkMap = new HashMap<>();
			Map<Amount<Angle>, List<Amount<Force>>> liftAdditionalSchrenkMap = new HashMap<>();
			Map<Amount<Angle>, List<Amount<Force>>> liftBasicSchrenkMap = new HashMap<>();
			Map<Amount<Angle>, List<Amount<Force>>> liftTotalSchrenkMap = new HashMap<>();
			
			for(int i=0; i<_alphaForDistribution.size(); i++) {

				List<Amount<Length>> ccLAdditionalSchrenk = new ArrayList<>();
				List<Amount<Length>> ccLBasicSchrenk = new ArrayList<>();
				List<Amount<Length>> ccLTotalSchrenk = new ArrayList<>();
				List<Double> gammaAdditionalSchrenk = new ArrayList<>();
				List<Double> gammaBasicSchrenk = new ArrayList<>();
				List<Double> gammaTotalSchrenk = new ArrayList<>();
				List<Double> liftCoefficientAdditionalSchrenk = new ArrayList<>();
				List<Double> liftCoefficientBasicSchrenk = new ArrayList<>();
				List<Double> liftCoefficientTotalSchrenk = new ArrayList<>();
				List<Amount<Force>> liftAdditionalSchrenk = new ArrayList<>();
				List<Amount<Force>> liftBasicSchrenk = new ArrayList<>();
				List<Amount<Force>> liftTotalSchrenk = new ArrayList<>();
				
				Amount<Angle> currentAlpha = _alphaForDistribution.get(i);
				
				double cLActual = theCLAtAlphaCalculator.nasaBlackwellCompleteCurve(
						_alphaForDistribution.get(i)
						);

				for(int j=0; j<_numberOfPointSemiSpanWise; j++) {
					ccLAdditionalSchrenk.add(
							_chordDistribution.get(j)
							.plus(_ellipticalChordDistribution.get(j))
							.divide(2)
							.times(cLActual)
							);
					ccLBasicSchrenk.add(
							_chordDistribution.get(j)
							.times(_clAlphaDistribution.get(j)
									.to(NonSI.DEGREE_ANGLE.inverse())
									.getEstimatedValue())
							.times(0.5)
							.times(_twistDistribution.get(j).doubleValue(NonSI.DEGREE_ANGLE)
									-_alphaZeroLiftDistribution.get(j).doubleValue(NonSI.DEGREE_ANGLE)
									)
							);
					ccLTotalSchrenk.add(
							ccLAdditionalSchrenk.get(j)
								.plus(ccLBasicSchrenk.get(j))
							);
					gammaAdditionalSchrenk.add(
							ccLAdditionalSchrenk.get(j)
							.divide(
									_theLiftingSurface.getLiftingSurfaceCreator().getSpan()
									.times(2)
									.doubleValue(SI.METER)
									)
							.getEstimatedValue()
							);
					gammaBasicSchrenk.add(
							ccLBasicSchrenk.get(j)
							.divide(
									_theLiftingSurface.getLiftingSurfaceCreator().getSpan()
									.times(2)
									.doubleValue(SI.METER)
									)
							.getEstimatedValue()
							);
					gammaTotalSchrenk.add(
							gammaAdditionalSchrenk.get(j) + gammaBasicSchrenk.get(j)
							);
					liftCoefficientAdditionalSchrenk.add(
							ccLAdditionalSchrenk.get(j)
							.divide(_chordDistribution.get(j)).getEstimatedValue()
							);
					liftCoefficientBasicSchrenk.add(
							ccLBasicSchrenk.get(j)
							.divide(_chordDistribution.get(j)).getEstimatedValue()
							);
					liftCoefficientTotalSchrenk.add(
							ccLTotalSchrenk.get(j)
							.divide(_chordDistribution.get(j)).getEstimatedValue()
							);
					liftAdditionalSchrenk.add(
							Amount.valueOf(
									ccLAdditionalSchrenk.get(j).doubleValue(SI.METER)
									*_theOperatingConditions.getDynamicPressureCruise().doubleValue(SI.PASCAL),
									SI.NEWTON
									)
							);
					liftBasicSchrenk.add(
							Amount.valueOf(
									ccLBasicSchrenk.get(j).doubleValue(SI.METER)
									*_theOperatingConditions.getDynamicPressureCruise().doubleValue(SI.PASCAL),
									SI.NEWTON
									)
							);
					liftTotalSchrenk.add(
							Amount.valueOf(
									ccLTotalSchrenk.get(j).doubleValue(SI.METER)
									*_theOperatingConditions.getDynamicPressureCruise().doubleValue(SI.PASCAL),
									SI.NEWTON
									)
							);
				}
				
				ccLAdditionalSchrenkMap.put(currentAlpha, ccLAdditionalSchrenk);
				ccLBasicSchrenkMap.put(currentAlpha, ccLBasicSchrenk);
				ccLTotalSchrenkMap.put(currentAlpha, ccLTotalSchrenk);
				gammaAdditionalSchrenkMap.put(currentAlpha, gammaAdditionalSchrenk);
				gammaBasicSchrenkMap.put(currentAlpha, gammaBasicSchrenk);
				gammaTotalSchrenkMap.put(currentAlpha, gammaTotalSchrenk);
				liftCoefficientAdditionalSchrenkMap.put(currentAlpha, liftCoefficientAdditionalSchrenk);
				liftCoefficientBasicSchrenkMap.put(currentAlpha, liftCoefficientBasicSchrenk);
				liftCoefficientTotalSchrenkMap.put(currentAlpha, liftCoefficientTotalSchrenk);
				liftAdditionalSchrenkMap.put(currentAlpha, liftAdditionalSchrenk);
				liftBasicSchrenkMap.put(currentAlpha, liftBasicSchrenk);
				liftTotalSchrenkMap.put(currentAlpha, liftTotalSchrenk);
				
			}
			_cclDistributionAdditionalLoad.put(
					MethodEnum.SCHRENK,
					ccLAdditionalSchrenkMap
					);
			_cclDistributionBasicLoad.put(
					MethodEnum.SCHRENK,
					ccLBasicSchrenkMap
					);
			_cclDistribution.put(
					MethodEnum.SCHRENK,
					ccLTotalSchrenkMap
					);
			_gammaDistributionAdditionalLoad.put(
					MethodEnum.SCHRENK,
					gammaAdditionalSchrenkMap
					);
			_gammaDistributionBasicLoad.put(
					MethodEnum.SCHRENK,
					gammaBasicSchrenkMap
					);
			_gammaDistribution.put(
					MethodEnum.SCHRENK,
					gammaTotalSchrenkMap
					);
			_liftCoefficientDistributionAdditionalLoad.put(
					MethodEnum.SCHRENK,
					liftCoefficientAdditionalSchrenkMap
					);
			_liftCoefficientDistributionBasicLoad.put(
					MethodEnum.SCHRENK,
					liftCoefficientBasicSchrenkMap
					);
			_liftCoefficientDistribution.put(
					MethodEnum.SCHRENK,
					liftCoefficientTotalSchrenkMap
					);
			_additionalLoadDistribution.put(
					MethodEnum.SCHRENK,
					liftAdditionalSchrenkMap
					);
			_basicLoadDistribution.put(
					MethodEnum.SCHRENK,
					liftBasicSchrenkMap
					);
			_liftDistribution.put(
					MethodEnum.SCHRENK,
					liftTotalSchrenkMap
					);
		}
	
		public void nasaBlackwell() {
			
			Map<Amount<Angle>, List<Amount<Length>>> ccLAdditionalMap = new HashMap<>();
			Map<Amount<Angle>, List<Amount<Length>>> ccLBasicMap = new HashMap<>();
			Map<Amount<Angle>, List<Amount<Length>>> ccLTotalMap = new HashMap<>();
			Map<Amount<Angle>, List<Double>> gammaAdditionalMap = new HashMap<>();
			Map<Amount<Angle>, List<Double>> gammaBasicMap = new HashMap<>();
			Map<Amount<Angle>, List<Double>> gammaTotalMap = new HashMap<>();
			Map<Amount<Angle>, List<Double>> liftCoefficientAdditionalMap = new HashMap<>();
			Map<Amount<Angle>, List<Double>> liftCoefficientBasicMap = new HashMap<>();
			Map<Amount<Angle>, List<Double>> liftCoefficientTotalMap = new HashMap<>();
			Map<Amount<Angle>, List<Amount<Force>>> liftAdditionalMap = new HashMap<>();
			Map<Amount<Angle>, List<Amount<Force>>> liftBasicMap = new HashMap<>();
			Map<Amount<Angle>, List<Amount<Force>>> liftTotalMap = new HashMap<>();
			
			
			if(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
				CalcAlpha0L theAlphaZeroLiftCalculator = new CalcAlpha0L();
				theAlphaZeroLiftCalculator.integralMeanWithTwist();
			}
			
			// EVALUATION OF THE BASIC LOAD
			
			theNasaBlackwellCalculatorAlphaZeroLift.calculate(Amount.valueOf(-0.6139, NonSI.DEGREE_ANGLE));
//	     	theNasaBlackwellCalculatorAlphaZeroLift.calculate(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST));
			
			for(int i=0; i<_alphaForDistribution.size(); i++) {
			
				List<Amount<Length>> ccLAdditional = new ArrayList<>();
				List<Amount<Length>> ccLBasic = new ArrayList<>();
				List<Amount<Length>> ccLTotal = new ArrayList<>();
				List<Double> gammaAdditional = new ArrayList<>();
				List<Double> gammaBasic = new ArrayList<>();
				List<Double> gammaTotal = new ArrayList<>();
				List<Double> liftCoefficientAdditional = new ArrayList<>();
				List<Double> liftCoefficientBasic = new ArrayList<>();
				List<Double> liftCoefficientTotal = new ArrayList<>();
				List<Amount<Force>> liftAdditional = new ArrayList<>();
				List<Amount<Force>> liftBasic = new ArrayList<>();
				List<Amount<Force>> liftTotal = new ArrayList<>();
				
				Amount<Angle> currentAlpha = _alphaForDistribution.get(i);
				
				theNasaBlackwellCalculator.calculate(_alphaForDistribution.get(i));
				
				for(int j=0; j<_numberOfPointSemiSpanWise; j++) {
					
					if(Double.isNaN(theNasaBlackwellCalculator.getClTotalDistribution().get(j)))
						theNasaBlackwellCalculator.getClTotalDistribution().toArray()[j] = 0.0;
					
					if(Double.isNaN(theNasaBlackwellCalculatorAlphaZeroLift.getClTotalDistribution().get(j)))
						theNasaBlackwellCalculatorAlphaZeroLift.getClTotalDistribution().toArray()[j] = 0.0;
					
					ccLTotal.add(
							Amount.valueOf(
									theNasaBlackwellCalculator.getClTotalDistribution().get(j)
									*_chordDistribution.get(j).doubleValue(SI.METER),
									SI.METER
									)
							);
					ccLBasic.add(
							Amount.valueOf(
									theNasaBlackwellCalculatorAlphaZeroLift.getClTotalDistribution().get(j)
									*_chordDistribution.get(j).doubleValue(SI.METER),
									SI.METER)
							);
					ccLAdditional.add(
							ccLTotal.get(j).minus(ccLBasic.get(j))
							);
					gammaTotal.add(
							theNasaBlackwellCalculator.getGammaDistribution().get(j)
							);
					gammaBasic.add(
							theNasaBlackwellCalculatorAlphaZeroLift.getGammaDistribution().get(j)
							);
					gammaAdditional.add(
							gammaTotal.get(j) - gammaBasic.get(j)
							);
					liftCoefficientTotal.add(
							theNasaBlackwellCalculator.getClTotalDistribution().get(j)
							);
					liftCoefficientBasic.add(
							theNasaBlackwellCalculatorAlphaZeroLift.getClTotalDistribution().get(j)
							);
					liftCoefficientAdditional.add(
							liftCoefficientTotal.get(j) - liftCoefficientBasic.get(j)
							);
					liftTotal.add(
							Amount.valueOf(
									ccLTotal.get(j).doubleValue(SI.METER)
									*_theOperatingConditions.getDynamicPressureCruise().doubleValue(SI.PASCAL),
									SI.NEWTON
									)
							);
					liftBasic.add(
							Amount.valueOf(
									ccLBasic.get(j).doubleValue(SI.METER)
									*_theOperatingConditions.getDynamicPressureCruise().doubleValue(SI.PASCAL),
									SI.NEWTON
									)
							);
					liftAdditional.add(
							Amount.valueOf(
									ccLAdditional.get(j).doubleValue(SI.METER)
									*_theOperatingConditions.getDynamicPressureCruise().doubleValue(SI.PASCAL),
									SI.NEWTON
									)
							);
				}
				
				ccLAdditionalMap.put(currentAlpha, ccLAdditional);
				ccLBasicMap.put(currentAlpha, ccLBasic);
				ccLTotalMap.put(currentAlpha, ccLTotal);
				gammaAdditionalMap.put(currentAlpha, gammaAdditional);
				gammaBasicMap.put(currentAlpha, gammaBasic);
				gammaTotalMap.put(currentAlpha, gammaTotal);
				liftCoefficientAdditionalMap.put(currentAlpha, liftCoefficientAdditional);
				liftCoefficientBasicMap.put(currentAlpha, liftCoefficientBasic);
				liftCoefficientTotalMap.put(currentAlpha, liftCoefficientTotal);
				liftAdditionalMap.put(currentAlpha, liftAdditional);
				liftBasicMap.put(currentAlpha, liftBasic);
				liftTotalMap.put(currentAlpha, liftTotal);
				
			}
			
			_cclDistributionAdditionalLoad.put(
					MethodEnum.NASA_BLACKWELL,
					ccLAdditionalMap
					);
			_cclDistributionBasicLoad.put(
					MethodEnum.NASA_BLACKWELL,
					ccLBasicMap
					);
			_cclDistribution.put(
					MethodEnum.NASA_BLACKWELL,
					ccLTotalMap
					);
			_gammaDistributionAdditionalLoad.put(
					MethodEnum.NASA_BLACKWELL,
					gammaAdditionalMap
					);
			_gammaDistributionBasicLoad.put(
					MethodEnum.NASA_BLACKWELL,
					gammaBasicMap
					);
			_gammaDistribution.put(
					MethodEnum.NASA_BLACKWELL,
					gammaTotalMap
					);
			_liftCoefficientDistributionAdditionalLoad.put(
					MethodEnum.NASA_BLACKWELL,
					liftCoefficientAdditionalMap
					);
			_liftCoefficientDistributionBasicLoad.put(
					MethodEnum.NASA_BLACKWELL,
					liftCoefficientBasicMap
					);
			_liftCoefficientDistribution.put(
					MethodEnum.NASA_BLACKWELL,
					liftCoefficientTotalMap
					);
			_additionalLoadDistribution.put(
					MethodEnum.NASA_BLACKWELL,
					liftAdditionalMap
					);
			_basicLoadDistribution.put(
					MethodEnum.NASA_BLACKWELL,
					liftBasicMap
					);
			_liftDistribution.put(
					MethodEnum.NASA_BLACKWELL,
					liftTotalMap
					);
		}

	}
	//............................................................................
	// END OF THE CALC LIFT DISTRIBUTIONS INNER CLASS
	//............................................................................
	
	//............................................................................
	// CALC CDParasite INNER CLASS
	//............................................................................
	public class CalcCDParasite {
		
		public void fromAirfoilDistribution(
				Double mach,
				Amount<Length> altitude
				) {

			if(_liftCoefficient3DCurve.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcLiftCurve calcLiftCurve = new CalcLiftCurve();
				calcLiftCurve.nasaBlackwell(mach);
			}
			
			List<Double> cDParasite = new ArrayList<>();

			cDParasite = DragCalc.calculateParasiteDragLiftingSurfaceFromAirfoil(
					_alphaArrayClean,
					theNasaBlackwellCalculator,
					_discretizedAirfoilsCd,
					_clForCdMatrix,
					_chordDistribution, 
					_theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform(), 
					_yStationDistribution
					);

			_cDParasite.put(
					MethodEnum.AIRFOIL_DISTRIBUTION,
					cDParasite);
		}
		
	}
	//............................................................................
	// END OF THE CALC CDParasite INNER CLASS
	//............................................................................
	
	//............................................................................
	// CALC CD0 INNER CLASS
	//............................................................................
	public class CalcCD0 {
		
		public void semiempirical(
				Double mach,
				Amount<Length> altitude
				) {
			
			Double kExcr = _theLiftingSurface.getLiftingSurfaceCreator().getKExcr();
			
			Double cD0Parasite = DragCalc.calculateCD0ParasiteLiftingSurface(
					_theLiftingSurface,
					_theOperatingConditions.getMachTransonicThreshold(),
					mach,
					altitude
					);
			Double cD0Gap = DragCalc.calculateCDGap(_theLiftingSurface);
			
			_cD0.put(
					MethodEnum.SEMIEMPIRICAL,
					cD0Parasite*(1+kExcr)
					+ cD0Gap
					);
		}
		
	}
	//............................................................................
	// END OF THE CALC CD0 INNER CLASS
	//............................................................................
	
	//............................................................................
	// CALC OSWALD FACTOR INNER CLASS
	//............................................................................
	public class CalcOswaldFactor {
		
		public void howe() {
			_oswaldFactor.put(
					MethodEnum.HOWE,
					AerodynamicCalc.calculateOswaldHowe(
							_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio(),
							_theLiftingSurface.getLiftingSurfaceCreator().getAspectRatio(),
							_meanAirfoil.getThicknessToChordRatio(),
							_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN),
							_theLiftingSurface.getNumberOfEngineOverTheWing(),
							_currentMachNumber
							)
					);
		}
		
		public void grosu(Amount<Angle> alpha) {
			
			CalcCLAtAlpha calcCLAtAlpha = new CalcCLAtAlpha();
			calcCLAtAlpha.nasaBlackwellCompleteCurve(alpha);
			
			_oswaldFactor.put(
					MethodEnum.GROSU,
					AerodynamicCalc.calculateOswaldGrosu(
							_meanAirfoil.getThicknessToChordRatio(),
							_theLiftingSurface.getLiftingSurfaceCreator().getAspectRatio(),
							_cLAtAlpha.get(MethodEnum.NASA_BLACKWELL)
							)
					);
		}
		
		public void raymer() {
			_oswaldFactor.put(
					MethodEnum.RAYMER,
					AerodynamicCalc.calculateOswaldRaymer(
							_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().doubleValue(SI.RADIAN),
							_theLiftingSurface.getLiftingSurfaceCreator().getAspectRatio()
							)
					);
		}
		
	}
	//............................................................................
	// END OF THE CALC OSWALD FACTOR INNER CLASS
	//............................................................................
	
	//............................................................................
	// CALC CDi INNER CLASS
	//............................................................................
	public class CalcCDInduced {

		public void howe(Amount<Angle> alpha) {
			
			CalcCLAtAlpha calcCLAtAlpha = new CalcCLAtAlpha();
			calcCLAtAlpha.nasaBlackwellCompleteCurve(alpha);
			
			if(_oswaldFactor.get(MethodEnum.HOWE) == null) {
				CalcOswaldFactor theOswaldCalculator = new CalcOswaldFactor();
				theOswaldCalculator.howe();
			}
			
			_cDInduced.put(
					MethodEnum.HOWE,
					Math.pow(_cLAtAlpha.get(MethodEnum.NASA_BLACKWELL),2)
					/(Math.PI
					*_theLiftingSurface.getLiftingSurfaceCreator().getAspectRatio()
					*_oswaldFactor.get(MethodEnum.HOWE))
					);
		}
		
		public void grosu(Amount<Angle> alpha) {
			
			CalcCLAtAlpha calcCLAtAlpha = new CalcCLAtAlpha();
			calcCLAtAlpha.nasaBlackwellCompleteCurve(alpha);
			
			if(_oswaldFactor.get(MethodEnum.GROSU) == null) {
				CalcOswaldFactor theOswaldCalculator = new CalcOswaldFactor();
				theOswaldCalculator.grosu(alpha);
			}
			
			_cDInduced.put(
					MethodEnum.GROSU,
					Math.pow(_cLAtAlpha.get(MethodEnum.NASA_BLACKWELL),2)
					/(Math.PI
					*_theLiftingSurface.getLiftingSurfaceCreator().getAspectRatio()
					*_oswaldFactor.get(MethodEnum.GROSU))
					);
		}
		
		public void raymer(Amount<Angle> alpha) {
			
			CalcCLAtAlpha calcCLAtAlpha = new CalcCLAtAlpha();
			calcCLAtAlpha.nasaBlackwellCompleteCurve(alpha);
			
			if(_oswaldFactor.get(MethodEnum.RAYMER) == null) {
				CalcOswaldFactor theOswaldCalculator = new CalcOswaldFactor();
				theOswaldCalculator.raymer();
			}
			
			_cDInduced.put(
					MethodEnum.RAYMER,
					Math.pow(_cLAtAlpha.get(MethodEnum.NASA_BLACKWELL),2)
					/(Math.PI
					*_theLiftingSurface.getLiftingSurfaceCreator().getAspectRatio()
					*_oswaldFactor.get(MethodEnum.RAYMER))
					);
		}
		
	}
	//............................................................................
	// END OF THE CALC CDi INNER CLASS
	//............................................................................
	
	//............................................................................
	// CALC CDwave INNER CLASS
	//............................................................................
	public class CalcCDWave {
		
		CalcCLAtAlpha calcCLAtAlpha = new CalcCLAtAlpha();
		
		public void lockKornWithKornMason() {
			_cDWave.put(
					MethodEnum.LOCK_KORN_WITH_KORN_MASON,
					DragCalc.calculateCDWaveLockKorn(
							calcCLAtAlpha.nasaBlackwellCompleteCurve(_currentAlpha),
							_currentMachNumber,
							_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
							_meanAirfoil.getThicknessToChordRatio(),
							_meanAirfoil.getType()
							)
					);
		}
		
		public void lockKornWithKroo() {
			_cDWave.put(
					MethodEnum.LOCK_KORN_WITH_KROO,
					DragCalc.calculateCDWaveLockKornCriticalMachKroo(
							calcCLAtAlpha.nasaBlackwellCompleteCurve(_currentAlpha),
							_currentMachNumber,
							_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
							_meanAirfoil.getThicknessToChordRatio(),
							_meanAirfoil.getType()
							)
					);
		}
		
	}
	//............................................................................
	// END OF THE CALC CDwave INNER CLASS
	//............................................................................
	
	//............................................................................
	// CALC Cd DISTRIBUTION INNER CLASS
	//............................................................................
	public class CalcDragDistributions {
			
		public void nasaBlackwell(double mach) {
			
			if(_liftCoefficient3DCurve.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcLiftCurve calcLiftCurve = new CalcLiftCurve();
				calcLiftCurve.nasaBlackwell(mach);
			}
			
			// PARASITE DRAG COEFFICIENT DISTRIBUTION:
			Map<Amount<Angle>, List<Double>> parasiteDragCoefficientDistributionAlphasMap = new HashMap<>();
			_alphaForDistribution.stream().forEach( a -> 
				parasiteDragCoefficientDistributionAlphasMap.put(
						a,
						DragCalc.calculateParasiteDragDistributionFromAirfoil(
								_numberOfPointSemiSpanWise,
								a,
								theNasaBlackwellCalculator,
								_discretizedAirfoilsCd,
								_clForCdMatrix
								)
						)
					);
			
			_parasiteDragCoefficientDistribution.put(
					MethodEnum.NASA_BLACKWELL, 
					parasiteDragCoefficientDistributionAlphasMap
					);
			
			// INDUCED DRAG COEFFICIENT DISTRIBUTION:
			Map<Amount<Angle>, List<Double>> inducedDragCoefficientDistributionAlphasMap = new HashMap<>();
			_alphaForDistribution.stream().forEach( a -> 
				inducedDragCoefficientDistributionAlphasMap.put(
						a,
						DragCalc.calculateInducedDragDistribution(
								_numberOfPointSemiSpanWise,
								a,
								theNasaBlackwellCalculator,
								_discretizedAirfoilsCl, 
								_alphaArrayClean, 
								_clZeroDistribution, 
								MyArrayUtils.convertDoubleArrayToListDouble(
										MyArrayUtils.convertListOfAmountToDoubleArray(
												_clAlphaDistribution.stream()
												.map(cla -> cla.to(NonSI.DEGREE_ANGLE.inverse()))
												.collect(Collectors.toList())		
												)
										), 
								AerodynamicCalc.calculateInducedAngleOfAttackDistribution(
										a, 
										theNasaBlackwellCalculator, 
										_currentAltitude, 
										_currentMachNumber, 
										_numberOfPointSemiSpanWise)
								)
						));

			_inducedDragCoefficientDistribution.put(
					MethodEnum.NASA_BLACKWELL, 
					inducedDragCoefficientDistributionAlphasMap
					);
			
			// DRAG COEFFICIENT DISTRIBUTION:
			Map<Amount<Angle>, List<Double>> dragCoefficientDistributionAlphasMap = new HashMap<>();
			_alphaForDistribution.stream().forEach( a -> 
				dragCoefficientDistributionAlphasMap.put(
						a,
						DragCalc.calculateTotalDragDistributionFromAirfoil(
								_numberOfPointSemiSpanWise,
								a,
								theNasaBlackwellCalculator,
								_discretizedAirfoilsCl, 
								_alphaArrayClean, 
								_discretizedAirfoilsCd,
								_clForCdMatrix,
								_clZeroDistribution, 
								MyArrayUtils.convertDoubleArrayToListDouble(
										MyArrayUtils.convertListOfAmountToDoubleArray(
												_clAlphaDistribution.stream()
												.map(cla -> cla.to(NonSI.DEGREE_ANGLE.inverse()))
												.collect(Collectors.toList())		
												)
										), 
								AerodynamicCalc.calculateInducedAngleOfAttackDistribution(
										a, 
										theNasaBlackwellCalculator, 
										_currentAltitude, 
										_currentMachNumber, 
										_numberOfPointSemiSpanWise))					
						));

			_dragCoefficientDistribution.put(
					MethodEnum.NASA_BLACKWELL, 
					dragCoefficientDistributionAlphasMap
					);

			// DRAG DISTRIBUTION:
			Map<Amount<Angle>, List<Amount<Force>>> dragDistributionAlphasMap = new HashMap<>();
			_alphaForDistribution.stream().forEach(a -> 
				dragDistributionAlphasMap.put(
						a,
						_dragCoefficientDistribution
						.get(MethodEnum.NASA_BLACKWELL)	
						.get(a).stream()
						.map(cd -> 
						cd
						*_theOperatingConditions.getDynamicPressureCruise().doubleValue(SI.PASCAL)
						*_chordDistribution.get(_dragCoefficientDistribution
								.get(MethodEnum.NASA_BLACKWELL)	
								.get(a)
								.indexOf(cd)).doubleValue(SI.METER)
								)
						.map(d -> Amount.valueOf(d, SI.NEWTON))
						.collect(Collectors.toList())
						)
					);

			_dragDistribution.put(
					MethodEnum.NASA_BLACKWELL, 
					dragDistributionAlphasMap
					);

		}
		
	}
	//............................................................................
	// END OF THE CALC Cd DISTRIBUTION INNER CLASS
	//............................................................................
	
	//............................................................................
	// CALC POLAR INNER CLASS
	//............................................................................
	public class CalcPolar {

		public void semiempirical(
				Double mach,
				Amount<Length> altitude
				) {
			
			if(_liftCoefficient3DCurve.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcLiftCurve calcLiftCurve = new CalcLiftCurve();
				calcLiftCurve.nasaBlackwell(mach);
			}
			
			CalcCDAtAlpha calcCDAtAlpha = new CalcCDAtAlpha();
			
			Double[] cDArray = new Double[_numberOfAlphasPlot];
			for(int i=0; i<_numberOfAlphasPlot; i++) {
				cDArray[i] = calcCDAtAlpha.semiempirical(
						_alphaArrayClean.get(i).to(SI.RADIAN),
						mach,
						altitude
						);
			}
			
			_polar3DCurve.put(MethodEnum.SEMIEMPIRICAL, cDArray);
			
		}
		
		public void fromCdDistribution(
				Double mach,
				Amount<Length> altitude
				) {
			
			if(_liftCoefficient3DCurve.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcLiftCurve calcLiftCurve = new CalcLiftCurve();
				calcLiftCurve.nasaBlackwell(mach);
			}
			
			CalcCDAtAlpha calcCDAtAlpha = new CalcCDAtAlpha();
			
			Double[] cDArray = new Double[_numberOfAlphasPlot];
			for(int i=0; i<_numberOfAlphasPlot; i++) {
				cDArray[i] = calcCDAtAlpha.fromCdDistribution(
						_alphaArrayClean.get(i).to(SI.RADIAN),
						mach,
						altitude
						);
			}
			
			_polar3DCurve.put(MethodEnum.AIRFOIL_DISTRIBUTION, cDArray);
			
		}

	}
	//............................................................................
	// END OF THE CALC POLAR INNER CLASS
	//............................................................................

	//............................................................................
	// CALC CD AT ALPHA INNER CLASS
	//............................................................................
	public class CalcCDAtAlpha {
		
		public double semiempirical(
				Amount<Angle> alpha,
				Double mach,
				Amount<Length> altitude) {
			
			double cDActual = 0.0;
			
			if(_cD0.get(MethodEnum.SEMIEMPIRICAL) == null) {
				CalcCD0 calcCD0 = new CalcCD0(); 
				calcCD0.semiempirical(mach, altitude);
			}
			
			// TODO : CHECK WHICH OSWALD IS BETTER !
			if(_cDInduced.get(MethodEnum.RAYMER) == null) {
				CalcCDInduced calcCDInduced = new CalcCDInduced(); 
				calcCDInduced.raymer(alpha);
			}
			
			if(_cDWave.get(MethodEnum.LOCK_KORN_WITH_KROO) == null) {
				CalcCDWave calcCDWave = new CalcCDWave(); 
				calcCDWave.lockKornWithKroo();
			}
			
			_cDAtAlpha.put(
					MethodEnum.SEMIEMPIRICAL,
					_cD0.get(MethodEnum.SEMIEMPIRICAL)
					+ _cDInduced.get(MethodEnum.RAYMER)
					+ _cDWave.get(MethodEnum.LOCK_KORN_WITH_KROO)
					);
			
			return cDActual;
		}
		
		public double fromCdDistribution(
				Amount<Angle> alpha,
				Double mach,
				Amount<Length> altitude) {

			if(_cDParasite.get(MethodEnum.AIRFOIL_DISTRIBUTION) == null) {
				CalcCDParasite calcCDParasite = new CalcCDParasite(); 
				calcCDParasite.fromAirfoilDistribution(mach, altitude);
			}

			// TODO : CHECK WHICH OSWALD IS BETTER !
				CalcCDInduced calcCDInduced = new CalcCDInduced(); 
				calcCDInduced.raymer(alpha);
			

			if(_cDWave.get(MethodEnum.LOCK_KORN_WITH_KROO) == null) {
				CalcCDWave calcCDWave = new CalcCDWave(); 
				calcCDWave.lockKornWithKroo();
			}

			_cDAtAlpha.put(
					MethodEnum.AIRFOIL_DISTRIBUTION,
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									_alphaArrayClean.stream()
										.map(a -> a.to(NonSI.DEGREE_ANGLE))	
											.collect(Collectors.toList())
											),
							MyArrayUtils.convertToDoublePrimitive(_cDParasite.get(MethodEnum.AIRFOIL_DISTRIBUTION)), 
							alpha.doubleValue(NonSI.DEGREE_ANGLE))
					+ _cDInduced.get(MethodEnum.RAYMER)
					+ _cDWave.get(MethodEnum.LOCK_KORN_WITH_KROO)
					);

			return _cDAtAlpha.get(MethodEnum.AIRFOIL_DISTRIBUTION);

		}

	}
	//............................................................................
	// END OF THE CALC CD AT ALPHA INNER CLASS
	//............................................................................
	
	//............................................................................
	// CALC CMac INNER CLASS
	//............................................................................
	public class CalcCMac {

		public void basicAndAdditionalContribution() {

			//....................................................................
			// ADDITIONAL CONTRIBUTION
			double cMacAdditional = MomentCalc.calculateCMACAdditional(
					_theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform(),
					_theLiftingSurface.getLiftingSurfaceCreator().getSemiSpan(),
					_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord(),
					_yStationDistribution, 
					_chordDistribution,
					_cmACDistribution
					);

			//....................................................................
			// BASIC CONTRIBUTION
			if(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
				CalcAlpha0L theAlphaZeroLiftCalculator = new CalcAlpha0L();
				theAlphaZeroLiftCalculator.integralMeanWithTwist();
			}
			
			double cMacBasic = MomentCalc.calculateCMACBasic(
					_theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform(),
					_theLiftingSurface.getLiftingSurfaceCreator().getSemiSpan(),
					_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord(),
					_yStationDistribution,
					_chordDistribution,
					_xLEDistribution, 
					_dihedralDistribution,
					_twistDistribution, 
					_alphaZeroLiftDistribution,
					_airfoilACToWingACDistribution,
					_vortexSemiSpanToSemiSpanRatio,
					_currentMachNumber,
					_currentAltitude, 
					_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST)
					);
			
			_cMac.put(MethodEnum.BASIC_AND_ADDITIONAL, cMacBasic + cMacAdditional);
			
		}
		
		public void integralMean() {
			
			_cMac.put(
					MethodEnum.INTEGRAL_MEAN,
					MomentCalc.calculateCMACIntegralMean(
							_theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform(),
							_theLiftingSurface.getLiftingSurfaceCreator().getSemiSpan(), 
							_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord(),
							_yStationDistribution,
							_chordDistribution, 
							_cmACDistribution
							)
					);
			
		}

		public void fromAirfoilDistribution() {
			
			if(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
				CalcAlpha0L theAlphaZeroLiftCalculator = new CalcAlpha0L();
				theAlphaZeroLiftCalculator.integralMeanWithTwist();
			}
			
			List<Double> cmACFromDistribution = 
					MomentCalc.calcCmDistributionLiftingSurfaceWithIntegral(
							theNasaBlackwellCalculator, 
							_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST),
							_yStationDistribution,
							_clZeroDistribution,
							_clAlphaDistribution.stream()
							.map(cla -> cla.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue())
							.collect(Collectors.toList()), 
							_cmACDistribution, 
							_chordDistribution,
							_xLEDistribution,
							_discretizedAirfoilsCl, 
							_alphaArrayClean,
							_momentumPole
							);
			
			_cMac.put(
					MethodEnum.AIRFOIL_DISTRIBUTION,
					MomentCalc.calculateCMACIntegralMean(
							_theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform(),
							_theLiftingSurface.getLiftingSurfaceCreator().getSemiSpan(), 
							_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord(),
							_yStationDistribution,
							_chordDistribution, 
							cmACFromDistribution
							)
					);
			
		}
		
	}
	//............................................................................
	// END OF THE CALC CMac INNER CLASS
	//............................................................................
	
	//............................................................................
	// CALC CMAlpha INNER CLASS
	//............................................................................
	public class CalcCMAlpha {

		public void andersonSweptCompressibleSubsonic() {
			
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				if(_cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.andersonSweptCompressibleSubsonic();
				}
			}
			else
				if(_cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.helmboldDiederich(_currentMachNumber);
				}
			
			Double cLAlpha = null;
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) 
				cLAlpha = _cLAlpha.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			else
				cLAlpha = _cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			
			if(_xacMRF.get(MethodEnum.DEYOUNG_HARPER) == null) {
				CalcXAC calcXAC = new CalcXAC();
				calcXAC.deYoungHarper();
			}

			CalcXAC calcXAC = new CalcXAC();
			calcXAC.deYoungHarper();
			_cMAlpha.put(
					MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC,
					Amount.valueOf(
							MomentCalc.calcCMalphaLS(
									cLAlpha,
									_momentumPole.doubleValue(SI.METER), 
									_xacMRF.get(MethodEnum.DEYOUNG_HARPER), 
									_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().getEstimatedValue(), 
									_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue()
									),
							NonSI.DEGREE_ANGLE.inverse()
							)
					);
		}

		public void polhamus() {
	
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				if(_cLAlpha.get(MethodEnum.POLHAMUS) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.polhamus();
				}
			}
			else
				if(_cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.helmboldDiederich(_currentMachNumber);
				}
			
			Double cLAlpha = null;
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) 
				cLAlpha = _cLAlpha.get(MethodEnum.POLHAMUS).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			else
				cLAlpha = _cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			
			if(_xacMRF.get(MethodEnum.DEYOUNG_HARPER) == null) {
				CalcXAC calcXAC = new CalcXAC();
				calcXAC.deYoungHarper();
			}
			
			CalcXAC calcXAC = new CalcXAC();
			calcXAC.deYoungHarper();
			_cMAlpha.put(
					MethodEnum.POLHAMUS,
					Amount.valueOf(
							MomentCalc.calcCMalphaLS(
									cLAlpha,
									_momentumPole.doubleValue(SI.METER), 
									_xacMRF.get(MethodEnum.DEYOUNG_HARPER), 
									_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().getEstimatedValue(), 
									_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue()
									),
							NonSI.DEGREE_ANGLE.inverse()
							)
					);

		}

	}
	//............................................................................
	// END OF THE CALC CMAlpha INNER CLASS
	//............................................................................
	
	//............................................................................
	// CALC CMAtAlpha INNER CLASS
	//............................................................................
	public class CalcCMAtAlpha {
		
		public Double fromAirfoilDistribution(Amount<Angle> alpha) {
			
			if(_moment3DCurve.get(MethodEnum.AIRFOIL_DISTRIBUTION) == null) {
				CalcMomentCurve calcMomentCurve = new CalcMomentCurve();
				calcMomentCurve.fromAirfoilDistribution();
			}
			
			_cMAtAlpha.put(
					MethodEnum.AIRFOIL_DISTRIBUTION,
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									_alphaArrayClean.stream()
										.map(a -> a.to(NonSI.DEGREE_ANGLE))
											.collect(Collectors.toList())
											),
							MyArrayUtils.convertToDoublePrimitive(_moment3DCurve.get(MethodEnum.AIRFOIL_DISTRIBUTION)),
							alpha.doubleValue(NonSI.DEGREE_ANGLE)
							)
					);
			
			return MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_alphaArrayClean.stream()
								.map(a -> a.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									),
					MyArrayUtils.convertToDoublePrimitive(_moment3DCurve.get(MethodEnum.AIRFOIL_DISTRIBUTION)),
					alpha.doubleValue(NonSI.DEGREE_ANGLE)
					);
			
		}
		
	}
	//............................................................................
	// END OF THE CALC CMAtAlpha INNER CLASS
	//............................................................................
	
	//............................................................................
	// CALC CMAtAlpha INNER CLASS
	//............................................................................
	public class CalcMomentCurve {
		
		public void fromAirfoilDistribution() {
			
			_moment3DCurve.put(
					MethodEnum.AIRFOIL_DISTRIBUTION, 
					MyArrayUtils.convertListOfDoubleToDoubleArray(
//							MomentCalc.calcCMLiftingSurfaceWithIntegralACVariable(
//									theNasaBlackwellCalculator,
//									_alphaArrayClean,
//									_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord(),
//									_yStationDistribution,
//									_clZeroDistribution,
//									_clAlphaDistribution.stream()
//										.map(cla -> cla.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue())
//											.collect(Collectors.toList()), 
//									_clForCmMatrix, 
//									_discretizedAirfoilsCm,
//									_chordDistribution,
//									_xLEDistribution,
//									_discretizedAirfoilsCl,
//									_alphaArrayClean, 
//									_theLiftingSurface.getSurface(), 
//									_momentumPole
//									)
							MomentCalc.calcCMLiftingSurfaceWithIntegral(
									theNasaBlackwellCalculator, 
									_alphaArray, 
									_theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord(),
									_yStationDistribution,
									_clZeroDistribution,
									_clAlphaDistribution.stream()
									.map(cla -> cla.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue())
									.collect(Collectors.toList()), 
									_cmACDistribution,
									_chordDistribution,
									_xLEDistribution,
									_discretizedAirfoilsCl,
									_alphaArrayClean, 
									_theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform(), 
									_momentumPole
									)
							)

					);
		}
		
	}
	//............................................................................
	// END OF THE CALC CMAtAlpha INNER CLASS
	//............................................................................
	
	//............................................................................
	// CALC MOMENT DISTRIBUTION INNER CLASS
	//............................................................................
	public class CalcMomentDistribution {
		
		public void fromAirfoilDistribution () {
			
			// CM DISTRIBUTION:
			Map<Amount<Angle>, List<Double>> momentCoefficientDistributionAlphasMap = new HashMap<>();
			
			_alphaForDistribution.stream().forEach( a -> 
				momentCoefficientDistributionAlphasMap.put(
						a,
						MomentCalc.calcCmDistributionLiftingSurfaceWithIntegral(
								theNasaBlackwellCalculator, 
								a,
								_yStationDistribution,
								_clZeroDistribution,
								_clAlphaDistribution.stream()
								.map(cla -> cla.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue())
								.collect(Collectors.toList()), 
								_cmACDistribution, 
								_chordDistribution,
								_xLEDistribution,
								_discretizedAirfoilsCl, 
								_alphaArrayClean,
								_momentumPole
								)
						)
					);
			
			_momentCoefficientDistribution.put(
					MethodEnum.AIRFOIL_DISTRIBUTION, 
					momentCoefficientDistributionAlphasMap
					);
			
			// MOMENT DISTRIBUTION:
			Map<Amount<Angle>, List<Amount<Force>>> momentDistributionAlphasMap = new HashMap<>();
			_alphaForDistribution.stream().forEach(a -> 
			momentDistributionAlphasMap.put(
					a,
					_momentCoefficientDistribution
					.get(MethodEnum.AIRFOIL_DISTRIBUTION)	
					.get(a).stream()
					.map(cm -> 
					cm
					*_theOperatingConditions.getDynamicPressureCruise().doubleValue(SI.PASCAL)
					*Math.pow(
							_chordDistribution.get(
									_momentCoefficientDistribution
									.get(MethodEnum.AIRFOIL_DISTRIBUTION)	
									.get(a)
									.indexOf(cm)).doubleValue(SI.METER),
							2)
							)
					.map(m -> Amount.valueOf(m, SI.NEWTON))
					.collect(Collectors.toList())
					)
					);

			_momentDistribution.put(
					MethodEnum.AIRFOIL_DISTRIBUTION, 
					momentDistributionAlphasMap
					);

		}
		
	}
	//............................................................................
	// END OF THE CALC MOMENT DISTRIBUTION INNER CLASS
	//............................................................................
	
	//............................................................................
	// HIGH LIFT DEVICES EFFECTS INNER CLASS
	//............................................................................
	public class CalcHighLiftDevicesEffects {
		
		@SuppressWarnings("unchecked")
		public void semiempirical(
				List<Amount<Angle>> flapDeflection, 
				List<Amount<Angle>> slatDeflection,
				double mach
				) {
			
			if(_alphaZeroLift.get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
				CalcAlpha0L calcAlphaZeroLift = new CalcAlpha0L();
				calcAlphaZeroLift.integralMeanWithTwist();
			}
			
			if(_cLZero.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCL0 calcCLZero = new CalcCL0();
				calcCLZero.nasaBlackwell();
			}
			
			if(_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS) == null) {
				CalcAlphaStar calcAlphaStar = new CalcAlphaStar();
				calcAlphaStar.meanAirfoilWithInfluenceAreas();
			}
			
			if(_cLStar.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCLStar calcCLStar = new CalcCLStar();
				calcCLStar.nasaBlackwell();
			}
			
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				if(_cLAlpha.get(MethodEnum.NASA_BLACKWELL) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.nasaBlackwell();
				}
			}
			else
				if(_cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH) == null) {
					CalcCLAlpha calcCLAlpha = new CalcCLAlpha();
					calcCLAlpha.helmboldDiederich(_currentMachNumber);
				}
			
			Double cLAlpha = null;
			if(!_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL)) 
				cLAlpha = _cLAlpha.get(MethodEnum.NASA_BLACKWELL).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			else
				cLAlpha = _cLAlpha.get(MethodEnum.HELMBOLD_DIEDERICH).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			
			if(_alphaStall.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcAlphaStall calcAlphaStall = new CalcAlphaStall();
				calcAlphaStall.fromAlphaMaxLinearNasaBlackwell(mach);
			}
			
			if(_cLMax.get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCLmax calcCLmax = new CalcCLmax();
				calcCLmax.nasaBlackwell();
			}
			
			//-----------------------------------------------------
			// EFFECTS:
			Map<HighLiftDeviceEffectEnum, Object> highLiftDevicesEffectsMap = 
					LiftCalc.calculateHighLiftDevicesEffects(
							_theLiftingSurface.getAeroDatabaseReader(),
							_theLiftingSurface.getHighLiftDatabaseReader(),
							_theLiftingSurface.getLiftingSurfaceCreator().getSymmetricFlaps(),
							_theLiftingSurface.getLiftingSurfaceCreator().getSlats(),
							_theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints(),
							_theLiftingSurface.getLiftingSurfaceCreator().getClAlphaVsY(),
							_theLiftingSurface.getLiftingSurfaceCreator().getCl0VsY(),
							_theLiftingSurface.getLiftingSurfaceCreator().getMaxThicknessVsY(),
							_theLiftingSurface.getLiftingSurfaceCreator().getRadiusLEVsY(),
							_theLiftingSurface.getLiftingSurfaceCreator().getChordsBreakPoints(),
							flapDeflection,
							slatDeflection,
							_currentAlpha,
							Amount.valueOf(cLAlpha, NonSI.DEGREE_ANGLE.inverse()),
							_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
							_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio(),
							_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getChordRoot(),
							_theLiftingSurface.getLiftingSurfaceCreator().getAspectRatio(),
							_theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform(),
							_meanAirfoil.getThicknessToChordRatio(),
							_meanAirfoil.getFamily(),
							_cLZero.get(MethodEnum.NASA_BLACKWELL),
							_cLMax.get(MethodEnum.NASA_BLACKWELL),
							_alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS),
							_alphaStall.get(MethodEnum.NASA_BLACKWELL)
							);	
			
			_deltaCl0FlapList.put(
					MethodEnum.SEMIEMPIRICAL, 
					(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl0_FLAP_LIST)
					);
			_deltaCL0FlapList.put(
					MethodEnum.SEMIEMPIRICAL, 
					(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CL0_FLAP_LIST)
					);
			_deltaClmaxFlapList.put(
					MethodEnum.SEMIEMPIRICAL, 
					(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_FLAP_LIST)
					);
			_deltaCLmaxFlapList.put(
					MethodEnum.SEMIEMPIRICAL, 
					(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_FLAP_LIST)
					);
			_deltaClmaxSlatList.put(
					MethodEnum.SEMIEMPIRICAL, 
					(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_SLAT_LIST)
					);
			_deltaCLmaxSlatList.put(
					MethodEnum.SEMIEMPIRICAL, 
					(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_SLAT_LIST)
					);
			_deltaCD0List.put(
					MethodEnum.SEMIEMPIRICAL, 
					(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CD_LIST)
					);
			_deltaCMc4List.put(
					MethodEnum.SEMIEMPIRICAL, 
					(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CM_c4_LIST)
					);
			_deltaCl0Flap.put(
					MethodEnum.SEMIEMPIRICAL, 
					(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl0_FLAP)
					);
			_deltaCL0Flap.put(
					MethodEnum.SEMIEMPIRICAL, 
					(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CL0_FLAP)
					);
			_deltaClmaxFlap.put(
					MethodEnum.SEMIEMPIRICAL, 
					(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_FLAP)
					);
			_deltaCLmaxFlap.put(
					MethodEnum.SEMIEMPIRICAL, 
					(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_FLAP)
					);
			_deltaClmaxSlat.put(
					MethodEnum.SEMIEMPIRICAL, 
					(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_SLAT)
					);
			_deltaCLmaxSlat.put(
					MethodEnum.SEMIEMPIRICAL, 
					(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_SLAT)
					);
			_deltaCD0.put(
					MethodEnum.SEMIEMPIRICAL, 
					(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CD)
					);
			_deltaCMc4.put(
					MethodEnum.SEMIEMPIRICAL, 
					(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CM_c4)
					);
			_cLAlphaHighLift.put(
					MethodEnum.SEMIEMPIRICAL, 
					(Amount<?>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.CL_ALPHA_HIGH_LIFT)
					);
			
			//------------------------------------------------------
			// CL ZERO HIGH LIFT
			_cLZeroHighLift.put(
					MethodEnum.SEMIEMPIRICAL,
					_cLZero.get(MethodEnum.NASA_BLACKWELL)
						+ _deltaCL0Flap.get(MethodEnum.SEMIEMPIRICAL)
					);
			
			//------------------------------------------------------
			// ALPHA ZERO LIFT HIGH LIFT
			_alphaZeroLiftHighLift.put(
					MethodEnum.SEMIEMPIRICAL,
					Amount.valueOf(
							-(_cLZeroHighLift.get(MethodEnum.SEMIEMPIRICAL)
									/_cLAlphaHighLift.get(MethodEnum.SEMIEMPIRICAL)
									.to(NonSI.DEGREE_ANGLE.inverse())
									.getEstimatedValue()
									),
							NonSI.DEGREE_ANGLE)
					);
			
			//------------------------------------------------------
			// CL MAX HIGH LIFT
			if(_deltaCLmaxSlat.get(MethodEnum.SEMIEMPIRICAL) == null)
				_cLMaxHighLift.put(
						MethodEnum.SEMIEMPIRICAL,
						_cLMax.get(MethodEnum.NASA_BLACKWELL)
						+ _deltaCLmaxFlap.get(MethodEnum.SEMIEMPIRICAL)
						);
			else 
				_cLMaxHighLift.put(
						MethodEnum.SEMIEMPIRICAL,
						_cLMax.get(MethodEnum.NASA_BLACKWELL)
						+ _deltaCLmaxFlap.get(MethodEnum.SEMIEMPIRICAL)
						+ _deltaCLmaxSlat.get(MethodEnum.SEMIEMPIRICAL)
						);
			
			//------------------------------------------------------
			// ALPHA STALL HIGH LIFT
			double deltaYPercent = _theLiftingSurface.getAeroDatabaseReader()
					.getDeltaYvsThickness(
							_meanAirfoil.getThicknessToChordRatio(),
							_meanAirfoil.getFamily()
							);
			
			Amount<Angle> deltaAlpha = Amount.valueOf(
					_theLiftingSurface.getAeroDatabaseReader()
					.getDAlphaVsLambdaLEVsDy(
							_theLiftingSurface.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().doubleValue(NonSI.DEGREE_ANGLE),
							deltaYPercent
							),
					NonSI.DEGREE_ANGLE);
			
			_alphaStallHighLift.put(
					MethodEnum.SEMIEMPIRICAL,
					Amount.valueOf(
					((_cLMaxHighLift.get(MethodEnum.SEMIEMPIRICAL)
					- _cLZeroHighLift.get(MethodEnum.SEMIEMPIRICAL))
					/_cLAlphaHighLift.get(MethodEnum.SEMIEMPIRICAL)
						.to(NonSI.DEGREE_ANGLE.inverse())
						.getEstimatedValue()
								)
					+ deltaAlpha.doubleValue(NonSI.DEGREE_ANGLE),
					NonSI.DEGREE_ANGLE)
					);
			
			//------------------------------------------------------
			// ALPHA STAR HIGH LIFT
			_alphaStarHighLift.put(
					MethodEnum.SEMIEMPIRICAL,
					Amount.valueOf(
							_alphaStallHighLift.get(MethodEnum.SEMIEMPIRICAL).doubleValue(NonSI.DEGREE_ANGLE)
							-(_alphaStall.get(MethodEnum.NASA_BLACKWELL).doubleValue(NonSI.DEGREE_ANGLE)
									- _alphaStar.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS).doubleValue(NonSI.DEGREE_ANGLE)),
							NonSI.DEGREE_ANGLE)
					);
			//------------------------------------------------------
			// ALPHA STAR HIGH LIFT
			_cLStarHighLift.put(
					MethodEnum.SEMIEMPIRICAL,
					(_cLAlphaHighLift.get(MethodEnum.SEMIEMPIRICAL)
						.to(NonSI.DEGREE_ANGLE.inverse())
								.getEstimatedValue()
					* _alphaStarHighLift.get(MethodEnum.SEMIEMPIRICAL)
						.doubleValue(NonSI.DEGREE_ANGLE))
					+ _cLZeroHighLift.get(MethodEnum.SEMIEMPIRICAL)
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
		
		public void semiempirical(
				List<Amount<Angle>> flapDeflection,
				List<Amount<Angle>> slatDeflection,
				Double mach
				) {
			
			if((_deltaCL0Flap.get(MethodEnum.SEMIEMPIRICAL) == null) ||
			   (_deltaCLmaxFlap.get(MethodEnum.SEMIEMPIRICAL) == null) ||
			   (_cLAlphaHighLift.get(MethodEnum.SEMIEMPIRICAL) == null)
					) {
				
				CalcHighLiftDevicesEffects theHighLiftEffectsCalculator = new CalcHighLiftDevicesEffects();
				theHighLiftEffectsCalculator.semiempirical(
						flapDeflection,
						slatDeflection,
						mach
						);
				
			}
			
//			_alphaArrayPlotHighLift = MyArrayUtils.linspaceDouble(
//					_alphaZeroLiftHighLift.get(MethodEnum.SEMIEMPIRICAL).doubleValue(NonSI.DEGREE_ANGLE)-2,
//					_alphaStallHighLift.get(MethodEnum.SEMIEMPIRICAL).doubleValue(NonSI.DEGREE_ANGLE) + 3,
//					_numberOfAlphasPlot
//					);
			
			_liftCoefficient3DCurveHighLift.put(
					MethodEnum.SEMIEMPIRICAL,
					LiftCalc.calculateCLvsAlphaArray(
							_cLZeroHighLift.get(MethodEnum.SEMIEMPIRICAL),
							_cLMaxHighLift.get(MethodEnum.SEMIEMPIRICAL),
							_alphaStarHighLift.get(MethodEnum.SEMIEMPIRICAL),
							_alphaStallHighLift.get(MethodEnum.SEMIEMPIRICAL),
							_cLAlphaHighLift.get(MethodEnum.SEMIEMPIRICAL),
							MyArrayUtils.convertListOfAmountToDoubleArray(_alphaArray)
							)
					);			
		}
		
	}	
	//............................................................................
	// END OF THE HIGH LIFT CURVE INNER CLASS
	//............................................................................

	//............................................................................
	// HIGH LIFT POLAR CURVE INNER CLASS
	//............................................................................
	public class CalcHighLiftPolarCurve {
		
		public void semiempirical(
				Double[] cleanPolarCurve,
				List<Amount<Angle>> flapDeflection,
				List<Amount<Angle>> slatDeflection,
				Double mach
				) {
			
			if(_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.WING)) {

				if(_deltaCD0.get(MethodEnum.SEMIEMPIRICAL) == null) {

					CalcHighLiftDevicesEffects theHighLiftEffectsCalculator = new CalcHighLiftDevicesEffects();
					theHighLiftEffectsCalculator.semiempirical(
							flapDeflection,
							slatDeflection,
							mach
							);

				}

				_polar3DCurveHighLift.put(
						MethodEnum.SEMIEMPIRICAL,
						MyArrayUtils.convertListOfDoubleToDoubleArray(
								Arrays.stream(cleanPolarCurve)
								.map(cD -> cD + _deltaCD0.get(MethodEnum.SEMIEMPIRICAL))
								.collect(Collectors.toList())
								)
						);			
			}
			else {
				/*
				 * THIS EQUATION IS AN INTERPOLATING FUNCTION OBTAINED FROM CFD ANALYSES ON THE IRON AIRCRAFT
				 * SINCE THE SEMIEMPIRICAL METHOD ESTIMATES A DELTA_CD0 TOO HIGH FOR HTAIL 
				 */
				double deltaCD0 = 0.0000156*
						Math.pow(flapDeflection.get(0).doubleValue(NonSI.DEGREE_ANGLE),2) + 
						0.000002 * flapDeflection.get(0).doubleValue(NonSI.DEGREE_ANGLE); 
				
				_polar3DCurveHighLift.put(
						MethodEnum.SEMIEMPIRICAL,
						MyArrayUtils.convertListOfDoubleToDoubleArray(
								Arrays.stream(cleanPolarCurve)
								.map(cD -> cD + deltaCD0)
								.collect(Collectors.toList())
								)
						);
				
			}
		}
		
	}	
	//............................................................................
	// END OF THE HIGH LIFT POLAR CURVE INNER CLASS
	//............................................................................
	
	//............................................................................
	// HIGH LIFT MOMENT CURVE INNER CLASS
	//............................................................................
	public class CalcHighLiftMomentCurve {
		
		public void semiempirical(
				Double[] cleanMomentCurve,
				List<Amount<Angle>> flapDeflection,
				List<Amount<Angle>> slatDeflection,
				Double mach
				) {
			
			if(_deltaCMc4.get(MethodEnum.SEMIEMPIRICAL) == null) {
				
				CalcHighLiftDevicesEffects theHighLiftEffectsCalculator = new CalcHighLiftDevicesEffects();
				theHighLiftEffectsCalculator.semiempirical(
						flapDeflection,
						slatDeflection,
						mach
						);
				
			}
			
			_momentCoefficient3DCurveHighLift.put(
					MethodEnum.SEMIEMPIRICAL,
					MyArrayUtils.convertListOfDoubleToDoubleArray(
							Arrays.stream(cleanMomentCurve)
							.map(cM -> cM + _deltaCMc4.get(MethodEnum.SEMIEMPIRICAL))
							.collect(Collectors.toList())
							)
					);			
		}
		
	}	
	//............................................................................
	// END OF THE HIGH LIFT MOMENT CURVE INNER CLASS
	//............................................................................	
	
	//............................................................................
	// CALC CL AT ALPHA HIGH LIFT INNER CLASS
	//............................................................................
	public class CalcCLAtAlphaHighLift {
		
		public double semiempirical(
				Amount<Angle> alpha, 
				List<Amount<Angle>> flapDeflection,
				List<Amount<Angle>> slatDeflection,
				Double mach,
				Amount<Length> altitude
				) {
		
			double cLActual = 0.0;
			
			if ((_alphaArray == null) 
					&& (_liftCoefficient3DCurveHighLift.get(MethodEnum.SEMIEMPIRICAL) == null)) {
				
				CalcHighLiftCurve theHighLiftCurveCalculator = new CalcHighLiftCurve();
				theHighLiftCurveCalculator.semiempirical(flapDeflection, slatDeflection, mach);
				
			}
			cLActual = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(_alphaArray),
					MyArrayUtils.convertToDoublePrimitive(_liftCoefficient3DCurveHighLift
							.get(MethodEnum.SEMIEMPIRICAL)),
					alpha.doubleValue(NonSI.DEGREE_ANGLE)
					);
			
			_cLAtAlphaHighLift.put(MethodEnum.SEMIEMPIRICAL, cLActual);
			
			return cLActual;
		}
		
	}
	//............................................................................
	// END OF THE CALC CL AT ALPHA HIGH LIFT INNER CLASS
	//............................................................................
	
	//............................................................................
	// CALC CD AT ALPHA HIGH LIFT INNER CLASS
	//............................................................................
	public class CalcCDAtAlphaHighLift {
	
		public double semiempirical(
				Amount<Angle> alpha, 
				List<Amount<Angle>> flapDeflection,
				List<Amount<Angle>> slatDeflection,
				Double mach,
				Amount<Length> altitude) {
			
			double cDActual = 0.0;
			
			if(_theLiftingSurface.getLiftingSurfaceCreator().getType().equals(ComponentEnum.WING)) {

				if (_deltaCD0.get(MethodEnum.SEMIEMPIRICAL) == null) {
					CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = new CalcHighLiftDevicesEffects();
					calcHighLiftDevicesEffects.semiempirical(flapDeflection, slatDeflection, mach);
				}

				CalcCDAtAlpha calcCDAtAlpha = new CalcCDAtAlpha();
				double cDActualClean = calcCDAtAlpha.fromCdDistribution(alpha, mach, altitude);

				cDActual = cDActualClean + _deltaCD0.get(MethodEnum.SEMIEMPIRICAL);

				_cDAtAlphaHighLift.put(MethodEnum.SEMIEMPIRICAL, cDActual);
				
			}
			else {
				/*
				 * THIS EQUATION IS AN INTERPOLATING FUNCTION OBTAINED FROM CFD ANALYSES ON THE IRON AIRCRAFT
				 * SINCE THE SEMIEMPIRICAL METHOD ESTIMATES A DELTA_CD0 TOO HIGH FOR HTAIL 
				 */
				double deltaCD0 = 0.0000156*
						Math.pow(flapDeflection.get(0).doubleValue(NonSI.DEGREE_ANGLE),2) + 
						0.000002 * flapDeflection.get(0).doubleValue(NonSI.DEGREE_ANGLE);
				
				CalcCDAtAlpha calcCDAtAlpha = new CalcCDAtAlpha();
				double cDActualClean = calcCDAtAlpha.fromCdDistribution(alpha, mach, altitude);
				
				cDActual = cDActualClean + deltaCD0;
				
				_cDAtAlphaHighLift.put(MethodEnum.SEMIEMPIRICAL, cDActual);
				
			}
			
			return cDActual;
			
		}
		
	}
	//............................................................................
	// END OF THE CALC CD AT ALPHA HIGH LIFT INNER CLASS
	//............................................................................
	
	//............................................................................
	// CALC CM AT ALPHA HIGH LIFT INNER CLASS
	//............................................................................
	public class CalcCMAtAlphaHighLift {

		public Double semiempirical(
				Amount<Angle> alpha, 
				List<Amount<Angle>> flapDeflection,
				List<Amount<Angle>> slatDeflection,
				double mach
				) {

			double cMActual = 0.0;
			
			if (_deltaCMc4.get(MethodEnum.SEMIEMPIRICAL) == null) {
				CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = new CalcHighLiftDevicesEffects();
				calcHighLiftDevicesEffects.semiempirical(flapDeflection, slatDeflection, mach);
			}

			CalcCMAtAlpha calcCMAtAlpha = new CalcCMAtAlpha();
			double cMActualClean = calcCMAtAlpha.fromAirfoilDistribution(alpha);

			cMActual = cMActualClean + _deltaCMc4.get(MethodEnum.SEMIEMPIRICAL);
			
			_cMAtAlphaHighLift.put(MethodEnum.SEMIEMPIRICAL, cMActual);

			return 0.0;
		}
	}
	//............................................................................
	// END OF THE CALC CM AT ALPHA HIGH LIFT INNER CLASS
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
	public Airfoil getMeanAirfoil() {
		return _meanAirfoil;
	}
	public void setMeanAirfoil(Airfoil _meanAirfoil) {
		this._meanAirfoil = _meanAirfoil;
	}
	public OperatingConditions getTheOperatingConditions() {
		return _theOperatingConditions;
	}
	public void setTheOperatingConditions(OperatingConditions _theOperatingConditions) {
		this._theOperatingConditions = _theOperatingConditions;
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
	public int getNumberOfAlphasPlot() {
		return _numberOfAlphasPlot;
	}
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
	public Amount<Length> getCurrentAltitude() {
		return _currentAltitude;
	}

	public void setCurrentAltitude(Amount<Length> _currentAltitude) {
		this._currentAltitude = _currentAltitude;
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
	/**
	 * @return the _xacMRF
	 */
	public Map<MethodEnum, Double> getXacMRF() {
		return _xacMRF;
	}

	/**
	 * @param _xacMRF the _xacMRF to set
	 */
	public void setXacMRF(Map<MethodEnum, Double> _xacMRF) {
		this._xacMRF = _xacMRF;
	}

	/**
	 * @return the _xacLRF
	 */
	public Map<MethodEnum, Amount<Length>> getXacLRF() {
		return _xacLRF;
	}

	/**
	 * @param _xacLRF the _xacLRF to set
	 */
	public void set_xacLRF(Map<MethodEnum, Amount<Length>> _xacLRF) {
		this._xacLRF = _xacLRF;
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
	public Map<MethodEnum, Double[]> getLiftCoefficient3DCurve() {
		return _liftCoefficient3DCurve;
	}
	public void setLiftCoefficient3DCurve(Map<MethodEnum, Double[]> _liftCoefficient3DCurve) {
		this._liftCoefficient3DCurve = _liftCoefficient3DCurve;
	}
	public Map <MethodEnum, Double[]> getLiftCoefficient3DCurveHighLift() {
		return _liftCoefficient3DCurveHighLift;
	}
	public void setLiftCoefficient3DCurveHighLift(Map <MethodEnum, Double[]> _liftCoefficient3DCurveHighLift) {
		this._liftCoefficient3DCurveHighLift = _liftCoefficient3DCurveHighLift;
	}
	public Map<MethodEnum, Map<Amount<Angle>, List<Double>>> getLiftCoefficientDistribution() {
		return _liftCoefficientDistribution;
	}
	public void setLiftCoefficientDistribution(Map<MethodEnum, Map<Amount<Angle>, List<Double>>> _liftCoefficientDistribution) {
		this._liftCoefficientDistribution = _liftCoefficientDistribution;
	}
	public Map<MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> getLiftDistribution() {
		return _liftDistribution;
	}
	public void setLiftDistribution(Map<MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> _liftDistribution) {
		this._liftDistribution = _liftDistribution;
	}
	public Map<MethodEnum, Map<Amount<Angle>, List<Double>>> getLiftCoefficientDistributionBasicLoad() {
		return _liftCoefficientDistributionBasicLoad;
	}
	public void setLiftCoefficientDistributionBasicLoad(
			Map<MethodEnum, Map<Amount<Angle>, List<Double>>> _liftCoefficientDistributionBasicLoad) {
		this._liftCoefficientDistributionBasicLoad = _liftCoefficientDistributionBasicLoad;
	}
	public Map<MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> getBasicLoadDistribution() {
		return _basicLoadDistribution;
	}
	public void setBasicLoadDistribution(Map<MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> _basicLoadDistribution) {
		this._basicLoadDistribution = _basicLoadDistribution;
	}
	
	public Map <MethodEnum, double[]> getLiftCoefficientDistributionAtCLMax() {
		return _liftCoefficientDistributionAtCLMax;
	}

	public void setLiftCoefficientDistributionAtCLMax(Map <MethodEnum, double[]> _liftCoefficientDistributionAtCLMax) {
		this._liftCoefficientDistributionAtCLMax = _liftCoefficientDistributionAtCLMax;
	}

	public Map<MethodEnum, Map<Amount<Angle>, List<Double>>> getLiftCoefficientDistributionAdditionalLoad() {
		return _liftCoefficientDistributionAdditionalLoad;
	}
	public void setLiftCoefficientDistributionAdditionalLoad(
			Map<MethodEnum, Map<Amount<Angle>, List<Double>>> _liftCoefficientDistributionAdditionalLoad) {
		this._liftCoefficientDistributionAdditionalLoad = _liftCoefficientDistributionAdditionalLoad;
	}
	public Map<MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> getAdditionalLoadDistribution() {
		return _additionalLoadDistribution;
	}
	public void setAdditionalLoadDistribution(Map<MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> _additionalLoadDistribution) {
		this._additionalLoadDistribution = _additionalLoadDistribution;
	}
	public Map<MethodEnum, Map<Amount<Angle>, List<Amount<Length>>>> getCclDistributionBasicLoad() {
		return _cclDistributionBasicLoad;
	}
	public void setCclDistributionBasicLoad(Map<MethodEnum, Map<Amount<Angle>, List<Amount<Length>>>> _cclDistributionBasicLoad) {
		this._cclDistributionBasicLoad = _cclDistributionBasicLoad;
	}
	public Map<MethodEnum, Map<Amount<Angle>, List<Amount<Length>>>> getCclDistributionAdditionalLoad() {
		return _cclDistributionAdditionalLoad;
	}
	public void setCclDistributionAdditionalLoad(Map<MethodEnum, Map<Amount<Angle>, List<Amount<Length>>>> _cclDistributionAdditionalLoad) {
		this._cclDistributionAdditionalLoad = _cclDistributionAdditionalLoad;
	}
	public Map<MethodEnum, Map<Amount<Angle>, List<Amount<Length>>>> getCclDistribution() {
		return _cclDistribution;
	}
	public void setCclDistribution(Map<MethodEnum, Map<Amount<Angle>, List<Amount<Length>>>> _cclDistribution) {
		this._cclDistribution = _cclDistribution;
	}
	public Map<MethodEnum, Map<Amount<Angle>, List<Double>>> getGammaDistributionBasicLoad() {
		return _gammaDistributionBasicLoad;
	}
	public void setGammaDistributionBasicLoad(Map<MethodEnum, Map<Amount<Angle>, List<Double>>> _gammaDistributionBasicLoad) {
		this._gammaDistributionBasicLoad = _gammaDistributionBasicLoad;
	}
	public Map<MethodEnum, Map<Amount<Angle>, List<Double>>> getGammaDistributionAdditionalLoad() {
		return _gammaDistributionAdditionalLoad;
	}
	public void setGammaDistributionAdditionalLoad(Map<MethodEnum, Map<Amount<Angle>, List<Double>>> _gammaDistributionAdditionalLoad) {
		this._gammaDistributionAdditionalLoad = _gammaDistributionAdditionalLoad;
	}
	public Map<MethodEnum, Map<Amount<Angle>, List<Double>>> getGammaDistribution() {
		return _gammaDistribution;
	}
	public void setGammaDistribution(Map<MethodEnum, Map<Amount<Angle>, List<Double>>> _gammaDistribution) {
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
	public Map<MethodEnum, List<Double>> getDeltaCD0List() {
		return _deltaCD0List;
	}
	public void setDeltaCDList(Map<MethodEnum, List<Double>> _deltaCDList) {
		this._deltaCD0List = _deltaCDList;
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
	public List<Amount<Angle>> getAlphaArrayClean() {
		return _alphaArrayClean;
	}
	public void setAlphaArrayClean(List<Amount<Angle>> _alphaArrayClean) {
		this._alphaArrayClean = _alphaArrayClean;
	}
//	public Double[] getAlphaArrayPlotHighLift() {
//		return _alphaArrayPlotHighLift;
//	}
//	public void setAlphaArrayPlotHighLift(Double[] _alphaArrayPlotHighLift) {
//		this._alphaArrayPlotHighLift = _alphaArrayPlotHighLift;
//	}
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
	public List<Amount<Length>> getChordDistribution() {
		return _chordDistribution;
	}
	public void setChordDistribution(List<Amount<Length>> _chordDistribution) {
		this._chordDistribution = _chordDistribution;
	}
	public List<Amount<Length>> getEllipticalChordDistribution() {
		return _ellipticalChordDistribution;
	}
	public void setEllipticalChordDistribution(List<Amount<Length>> _ellipticalChordDistribution) {
		this._ellipticalChordDistribution = _ellipticalChordDistribution;
	}
	public List<Amount<Angle>> getDihedralDistribution() {
		return _dihedralDistribution;
	}
	public void setDihedralDistribution(List<Amount<Angle>> _dihedralDistribution) {
		this._dihedralDistribution = _dihedralDistribution;
	}
	public List<Amount<?>> getClAlphaDistribution() {
		return _clAlphaDistribution;
	}
	public void setClAlphaDistribution(List<Amount<?>> _clAlphaDistribution) {
		this._clAlphaDistribution = _clAlphaDistribution;
	}
	public List<Amount<Length>> getXLEDistribution() {
		return _xLEDistribution;
	}
	public void setXLEDistribution(List<Amount<Length>> _xLEDistribution) {
		this._xLEDistribution = _xLEDistribution;
	}
	public List<Double> getClMaxDistribution() {
		return _clMaxDistribution;
	}
	public void setClMaxDistribution(List<Double> _clMaxDistribution) {
		this._clMaxDistribution = _clMaxDistribution;
	}

	/**
	 * @return the _cD0
	 */
	public Map <MethodEnum, List<Double>> getCDParasite() {
		return _cDParasite;
	}

	/**
	 * @param _cD0 the _cD0 to set
	 */
	public void setCDParasite(Map <MethodEnum, List<Double>> _cDParasite) {
		this._cDParasite = _cDParasite;
	}

	/**
	 * @return the _cDInduced
	 */
	public Map <MethodEnum, Double> getCDInduced() {
		return _cDInduced;
	}

	/**
	 * @param _cDInduced the _cDInduced to set
	 */
	public void setCDInduced(Map <MethodEnum, Double> _cDInduced) {
		this._cDInduced = _cDInduced;
	}

	/**
	 * @return the _cDWave
	 */
	public Map <MethodEnum, Double> getCDWave() {
		return _cDWave;
	}

	/**
	 * @param _cDWave the _cDWave to set
	 */
	public void setCDWave(Map <MethodEnum, Double> _cDWave) {
		this._cDWave = _cDWave;
	}

	/**
	 * @return the _polar3DCurve
	 */
	public Map <MethodEnum, Double[]> getPolar3DCurve() {
		return _polar3DCurve;
	}

	/**
	 * @param _polar3DCurve the _polar3DCurve to set
	 */
	public void setPolar3DCurve(Map <MethodEnum, Double[]> _polar3DCurve) {
		this._polar3DCurve = _polar3DCurve;
	}

	/**
	 * @return the _parasiteDragCoefficientDistribution
	 */
	public Map <MethodEnum, Map<Amount<Angle>, List<Double>>> getParasiteDragCoefficientDistribution() {
		return _parasiteDragCoefficientDistribution;
	}

	/**
	 * @param _parasiteDragCoefficientDistribution the _parasiteDragCoefficientDistribution to set
	 */
	public void setParasiteDragCoefficientDistribution(Map <MethodEnum, Map<Amount<Angle>, List<Double>>> _parasiteDragCoefficientDistribution) {
		this._parasiteDragCoefficientDistribution = _parasiteDragCoefficientDistribution;
	}

	/**
	 * @return the _inducedDragCoefficientDistribution
	 */
	public Map <MethodEnum, Map<Amount<Angle>, List<Double>>> getInducedDragCoefficientDistribution() {
		return _inducedDragCoefficientDistribution;
	}

	/**
	 * @param _inducedDragCoefficientDistribution the _inducedDragCoefficientDistribution to set
	 */
	public void setInducedDragCoefficientDistribution(Map <MethodEnum, Map<Amount<Angle>, List<Double>>> _inducedDragCoefficientDistribution) {
		this._inducedDragCoefficientDistribution = _inducedDragCoefficientDistribution;
	}

	/**
	 * @return the _dragCoefficientDistribution
	 */
	public Map <MethodEnum, Map<Amount<Angle>, List<Double>>> getDragCoefficientDistribution() {
		return _dragCoefficientDistribution;
	}

	/**
	 * @param _dragCoefficientDistribution the _dragCoefficientDistribution to set
	 */
	public void setDragCoefficientDistribution(Map <MethodEnum, Map<Amount<Angle>, List<Double>>> _dragCoefficientDistribution) {
		this._dragCoefficientDistribution = _dragCoefficientDistribution;
	}

	/**
	 * @return the _dragDistribution
	 */
	public Map <MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> getDragDistribution() {
		return _dragDistribution;
	}

	/**
	 * @param _dragDistribution the _dragDistribution to set
	 */
	public void setDragDistribution(Map <MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> _dragDistribution) {
		this._dragDistribution = _dragDistribution;
	}

	/**
	 * @return the _oswaldFactor
	 */
	public Map <MethodEnum, Double> getOswaldFactor() {
		return _oswaldFactor;
	}

	/**
	 * @param _oswaldFactor the _oswaldFactor to set
	 */
	public void setOswaldFactor(Map <MethodEnum, Double> _oswaldFactor) {
		this._oswaldFactor = _oswaldFactor;
	}

	/**
	 * @return the _cLAtAlpha
	 */
	public Map <MethodEnum, Double> getCLAtAlpha() {
		return _cLAtAlpha;
	}

	/**
	 * @param _cLAtAlpha the _cLAtAlpha to set
	 */
	public void setCLAtAlpha(Map <MethodEnum, Double> _cLAtAlpha) {
		this._cLAtAlpha = _cLAtAlpha;
	}

	/**
	 * @return the _cLAtAlphaHighLift
	 */
	public Map <MethodEnum, Double> getCLAtAlphaHighLift() {
		return _cLAtAlphaHighLift;
	}

	/**
	 * @param _cLAtAlphaHighLift the _cLAtAlphaHighLift to set
	 */
	public void setCLAtAlphaHighLift(Map <MethodEnum, Double> _cLAtAlphaHighLift) {
		this._cLAtAlphaHighLift = _cLAtAlphaHighLift;
	}

	/**
	 * @return the _cDAtAlpha
	 */
	public Map <MethodEnum, Double> getCDAtAlpha() {
		return _cDAtAlpha;
	}

	/**
	 * @param _cDAtAlpha the _cDAtAlpha to set
	 */
	public void setCDAtAlpha(Map <MethodEnum, Double> _cDAtAlpha) {
		this._cDAtAlpha = _cDAtAlpha;
	}
	
	public Map<MethodEnum, Double> getCD0HighLift() {
		return _cD0HighLift;
	}

	public void setCD0HighLift(Map<MethodEnum, Double> _cD0HighLift) {
		this._cD0HighLift = _cD0HighLift;
	}

	public ConditionEnum getTheCondition() {
		return _theCondition;
	}

	public void setTheCondition(ConditionEnum theCondition) {
		this._theCondition = theCondition;
	}

	public List<Amount<Angle>> getTwistDistribution() {
		return _twistDistribution;
	}

	public void setTwistDistribution(List<Amount<Angle>> _twistDistribution) {
		this._twistDistribution = _twistDistribution;
	}

	public List<Amount<Angle>> getAlphaZeroLiftDistribution() {
		return _alphaZeroLiftDistribution;
	}

	public void setAlphaZeroLiftDistribution(List<Amount<Angle>> _alphaZeroLiftDistribution) {
		this._alphaZeroLiftDistribution = _alphaZeroLiftDistribution;
	}

	public List<Amount<Angle>> getAlphaForDistribution() {
		return _alphaForDistribution;
	}

	public void setAlphaForDistribution(List<Amount<Angle>> _alphaWingForDistribution) {
		this._alphaForDistribution = _alphaWingForDistribution;
	}

	public List<Double> getClZeroDistribution() {
		return _clZeroDistribution;
	}

	public void setClZeroDistribution(List<Double> _clZeroDistribution) {
		this._clZeroDistribution = _clZeroDistribution;
	}

	public List<List<Double>> getDiscretizedAirfoilsCl() {
		return _discretizedAirfoilsCl;
	}

	public void setDiscretizedAirfoilsCl(List<List<Double>> _discretizedAirfoilsCl) {
		this._discretizedAirfoilsCl = _discretizedAirfoilsCl;
	}

	public List<List<Double>> getDiscretizedAirfoilsCd() {
		return _discretizedAirfoilsCd;
	}

	public void setDiscretizedAirfoilsCd(List<List<Double>> _discretizedAirfoilsCd) {
		this._discretizedAirfoilsCd = _discretizedAirfoilsCd;
	}

	public List<List<Double>> getDiscretizedAirfoilsCm() {
		return _discretizedAirfoilsCm;
	}

	public void setDiscretizedAirfoilsCm(List<List<Double>> _discretizedAirfoilsCm) {
		this._discretizedAirfoilsCm = _discretizedAirfoilsCm;
	}

	public Map<MethodEnum, Double> getCD0() {
		return _cD0;
	}

	public void setCD0(Map<MethodEnum, Double> _cD0) {
		this._cD0 = _cD0;
	}

	public Map<MethodEnum, Double> getCDAtAlphaHighLift() {
		return _cDAtAlphaHighLift;
	}

	public void setCDAtAlphaHighLift(Map<MethodEnum, Double> _cDAtAlphaHighLift) {
		this._cDAtAlphaHighLift = _cDAtAlphaHighLift;
	}

	public Map<MethodEnum, Double> getCMAtAlphaHighLift() {
		return _cMAtAlphaHighLift;
	}

	public void setCMAtAlphaHighLift(Map<MethodEnum, Double> _cMAtAlphaHighLift) {
		this._cMAtAlphaHighLift = _cMAtAlphaHighLift;
	}

	public List<Double> getCmACDistribution() {
		return _cmACDistribution;
	}

	public void setCmACDistribution(List<Double> _cmACDistribution) {
		this._cmACDistribution = _cmACDistribution;
	}

	public Amount<Length> getMomentumPole() {
		return _momentumPole;
	}

	public void setMomentumPole(Amount<Length> _momentumPole) {
		this._momentumPole = _momentumPole;
	}

	public Map <MethodEnum, Double> getCMac() {
		return _cMac;
	}

	public void setCMac(Map <MethodEnum, Double> _cMac) {
		this._cMac = _cMac;
	}

	public Map <MethodEnum, Amount<?>> getCMAlpha() {
		return _cMAlpha;
	}

	public void setCMAlpha(Map <MethodEnum, Amount<?>> _cMAlpha) {
		this._cMAlpha = _cMAlpha;
	}

	public Map <MethodEnum, Double> getCMAtAlpha() {
		return _cMAtAlpha;
	}

	public void setCMAtAlpha(Map <MethodEnum, Double> _cMAtAlpha) {
		this._cMAtAlpha = _cMAtAlpha;
	}

	public Map <MethodEnum, Double[]> getMoment3DCurve() {
		return _moment3DCurve;
	}

	public void setMoment3DCurve(Map <MethodEnum, Double[]> _moment3DCurve) {
		this._moment3DCurve = _moment3DCurve;
	}

	public Map<MethodEnum, Map<Amount<Angle>, List<Double>>> getMomentCoefficientDistribution() {
		return _momentCoefficientDistribution;
	}

	public void setMomentCoefficientDistribution(Map<MethodEnum, Map<Amount<Angle>, List<Double>>> _momentCoefficientDistribution) {
		this._momentCoefficientDistribution = _momentCoefficientDistribution;
	}

	public List<Amount<Length>> getAirfoilACToWingACDistribution() {
		return _airfoilACToWingACDistribution;
	}

	public void setAirfoilACToWingACDistribution(List<Amount<Length>> _airfoilACToWingACDistribution) {
		this._airfoilACToWingACDistribution = _airfoilACToWingACDistribution;
	}

	public List<Amount<Length>> getXACDistribution() {
		return _xACDistribution;
	}

	public void setXACDistribution(List<Amount<Length>> _xACDistribution) {
		this._xACDistribution = _xACDistribution;
	}

	public Map<MethodEnum, Double> getDeltaCD0() {
		return _deltaCD0;
	}

	public void setDeltaCD0(Map<MethodEnum, Double> _deltaCD0) {
		this._deltaCD0 = _deltaCD0;
	}

	public Amount<Angle> getCurrentAlpha() {
		return _currentAlpha;
	}

	public void setCurrentAlpha(Amount<Angle> _currentAlpha) {
		this._currentAlpha = _currentAlpha;
	}

	public Map <MethodEnum, Double[]> getPolar3DCurveHighLift() {
		return _polar3DCurveHighLift;
	}

	public void setPolar3DCurveHighLift(Map <MethodEnum, Double[]> _polar3DCurveHighLift) {
		this._polar3DCurveHighLift = _polar3DCurveHighLift;
	}

	public Map <MethodEnum, Double[]> getMomentCoefficient3DCurveHighLift() {
		return _momentCoefficient3DCurveHighLift;
	}

	public void setMomentCoefficient3DCurveHighLift(Map <MethodEnum, Double[]> _momentCoefficient3DCurveHighLift) {
		this._momentCoefficient3DCurveHighLift = _momentCoefficient3DCurveHighLift;
	}

	public Map <MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> getMomentDistribution() {
		return _momentDistribution;
	}

	public void setMomentDistribution(Map <MethodEnum, Map<Amount<Angle>, List<Amount<Force>>>> _momentDistribution) {
		this._momentDistribution = _momentDistribution;
	}

	public NasaBlackwell getTheNasaBlackwellCalculator() {
		return theNasaBlackwellCalculator;
	}

	public void setTheNasaBlackwellCalculator(NasaBlackwell theNasaBlackwellCalculator) {
		this.theNasaBlackwellCalculator = theNasaBlackwellCalculator;
	}
}