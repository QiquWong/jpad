package aircraft.components.liftingSurface;

import static java.lang.Math.cos;
import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.commons.math3.analysis.function.Power;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.poi.util.SystemOutLogger;
import org.jscience.physics.amount.Amount;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.TreeBasedTable;
import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;
import com.sun.org.apache.bcel.internal.generic.NEWARRAY;
import com.sun.org.apache.xml.internal.utils.ThreadControllerWrapper;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAerodynamics;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.componentmodel.InnerCalculator;
import aircraft.componentmodel.componentcalcmanager.AerodynamicsManager;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLMaxClean;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLvsAlphaCurve;
import aircraft.components.liftingSurface.LSAerodynamicsManager.MeanAirfoil;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.AlphaEffective;
import calculators.aerodynamics.AnglesCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.MomentCalc;
import calculators.aerodynamics.NasaBlackwell;
import calculators.geometry.LSGeometryCalc;
import calculators.stability.StabilityCalculators;
import configuration.MyConfiguration;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.DatabaseReaderEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.DatabaseReader;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.AerodynamicsDatabaseManager;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.engine.EngineDatabaseReader;
import database.databasefunctions.engine.TurbofanEngineDatabaseReader;
import database.databasefunctions.engine.TurbopropEngineDatabaseReader;
import javafx.util.Pair;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.CenterOfGravity;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;



/**
 * This class holds all aerodynamic analysis methods 
 * available for a generic lifting surface
 * 
 * @author Lorenzo Attanasio
 */
public class LSAerodynamicsManager extends AerodynamicsManager{

	OperatingConditions theOperatingConditions;
	private LiftingSurface theLiftingSurface;
	Aircraft theAircraft;

	private AerodynamicDatabaseReader _aerodynamicDatabaseReader;
	private HighLiftDatabaseReader _highLiftDatabaseReader;

	private List<DatabaseReader> listDatabaseReaders = new ArrayList<DatabaseReader>();
	// TODO: just an idea
	/*
	 * 	// create an object of this class
	 * 	LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager ( 
	 * 		theOperatingConditions,theWing);
	 * 
	 *	// Initialize database tree
	 *	MyConfiguration.initWorkingDirectoryTree();
	 *
	 *	theLSAnalysis.setDatabaseReaders(
	 *		DatabaseReaderEnum.AERODYNAMIC, "Aerodynamic_Database_Ultimate.h5",
	 *		DatabaseReaderEnum.HIGHLIFT, "HighLiftDatabase.h5",
	 *		);
	 * 
	 */

	Integer _numberOfAlpha;
	private int _nPointsSemispanWise;
	private boolean subfolderPathCheck = true;
	Amount<Angle> alphaStart, alphaEnd, _alphaMaxClean; 

	AirfoilTypeEnum _airfoilType;

	private MyArray alphaArray, cLArray, _alphaDistribution, _twistDistribution, _alpha0lDistribution,
	_twistDistributionExposed, _alpha0lDistributionExposed ;
	private MyArray
	_chordsVsY = new MyArray(SI.METER),
	_chordsVsYExposed =  new MyArray(SI.METER),
	_alpha0lVsY, 
	_clAlphaVsY,
	_ccLAdd,
	_loadAdd,
	_loadBas,
	_gammaBasic,
	_gammaAdd,
	_c_cLAdd,
	_gammaTot;

	Double	
	_cF, _reynolds, 
	_cd0Parasite, _cDw = 0.,
	_cdWFInterf, _cdWNInterf, _cdGap,
	_cL = 0., 
	_compressibilityFactor, 
	_cD0Total, _kPolhamus,
	_cLAlpha, 
	_cLAlphaEstimated, _cLAlphaMean2D,
	_cLMaxClean;

	Amount<Angle> _alpha0L = null;
	Amount<Angle> _alphaZeroLiftInnerPanel = null;
	Amount<Angle> _alphaZeroLiftOuterPanel = null;

	/** AoA of the root chord of the lifting surface */
	Amount<Angle> _alphaRootCurrent = null;
	Amount<Angle> _alphaStar = null;
	Amount<Angle> _alphaStarHigLift = null;
	Amount<Angle> _alphaStall = null;

	Double _machTransonicThreshold = null;
	Double _liftCoefficientGradient = null;
	Double _liftCoefficientGradientInnerPanel = null;
	Double _liftCoefficientGradientOuterPanel = null;
	Double _pitchCoefficientAC = null;
	Double _pitchCoefficientACInnerPanel = null;
	Double _pitchCoefficientACOuterPanel = null;
	Double _liftCoefficientMax = null;
	Double _rollCoefficientGradient = null;
	Double _pitchCoefficientGradient = null;
	Double _yawCoefficientGradient = null;
	Double _gammaTotBasic;
	Double _machNumber3DCritical;

	private Amount<Angle> _alphaCurrent;

	private CoefficientWrapper cLMap = new CoefficientWrapper();
	private CoefficientWrapper cDMap = new CoefficientWrapper();
	private CoefficientWrapper cMMap = new CoefficientWrapper();

	Map <MethodEnum, Amount<?>> _cLAlphaMap = new TreeMap<MethodEnum, Amount<?>>();
	Map <Double, Double> _cLvsAlphaCurve = new TreeMap<Double, Double>();

	// FIXME: Units are wrong for gamma!
	private MyArray _gammaSignedVsY = new MyArray(Unit.ONE);
	private MyArray _gammaVsY = new MyArray(Unit.ONE);
	private MyArray _ccLVsY = new MyArray(Unit.ONE);
	private MyArray _clAdditionalVsY = new MyArray(Unit.ONE);
	private MyArray _clTotalVsY = new MyArray(Unit.ONE);

	private double machCurrent, altitude, alpha, _cLCurrent = 0.45;

	private double surface, surfaceWetted, surfaceReference, 
	semispan, span, chordRoot,
	ar, taperRatioEq, tcRoot,
	dihedralMean, sweepHalfChordEq, sweepQuarterChordEq, 
	maxThicknessMean, deltaAlpha, alphaNew, cLLinearSlope, diffCL =1 ;

	private double _vortexSemiSpan, _vortexSemiSpanToSemiSpanRatio = 0.01;

	private double _cmAlpha, _cL0, _cM0, _dynamicPressureRatio = 1.;

	double _cMacBasic, _cMacTotal, _cMacAdditional, _betaPG;
	double _loadTotIntegral, _gammaTotIntegral;

	private double[] twistVsY, etaAirfoil, _yStations, _yStationsND,
	dihedral, alpha0l, yStationsActual, chordsVsYActual, xLEvsYActual, cdDistributionNasaBlackwell;

	private CalcCLAlpha calculateCLAlpha;
	private CalcAlpha0L calculateAlpha0L;
	private CalcCL0 calculateCL0;
	private CalcCLAtAlpha calculateCLAtAlpha;
	private CalcCLvsAlphaCurve calculateCLvsAlphaCurve;
	private CalcCmAC calculateCmAC;
	private CalcXAC calculateXAC;
	private CalcMachCr calculateMachCr;
	private CalcCdWaveDrag calculateCdWaveDrag;
	private CalcLiftDistribution calculateLiftDistribution;
	private CalcBasicLoad calculateBasicLoadDistribution;
	private CalcCLMaxClean calculateCLMaxClean;
	private CalcCmAlpha calculateCmAlpha;
	private CalcCm0 calculateCm0;
	private ComponentEnum lsType;
	private double[] alphaArrayPlot;
	private double[] cLArrayPlot;
	private String subfolderPathCLAlpha;
	public double cLAlphaZero;
	public double alphaZeroLiftWingClean;


	static double rootChord;
	static double kinkChord;
	static double tipChord;
	static double dimensionalKinkStation;
	static double dimensionalOverKink;
	private static double intermediateClMax;
	private static double intermediateEta;
	private static double intermediateTwist;
	private double intermediateChord;
	private double intermediateDistanceAC;
	private double intermediateXac;
	private static double intermediateAlphaZL;
	private static double intermediateAlphaStar;
	private static double intermediateClStar;
	private static double intermediateClMaxSweep;
	private static double intermediateClatMinCD;
	private static double intermediateCdMin;
	private static double intermediateCm;
	private static double intermediateCmAlphaLE;
	private static double intermediateAerodynamicCentre;
	private static double intermediateMaxThickness;
	private static double intermediateReynolds;
	private static double intermediatekFactorPolar;
	private static double intermediateClAlpha;
	private static double intermediateAlphaStall;
	private double[] _yStationsIntegral;
	public double cLStarWing;
	private double alphaZeroLiftDeflection;
	String subfolderPathCDAlpha;
	public double[] cLActualArray;
	public MyArray alphaArrayActual;
	public MyArray alphaArrayActualHighLift;
	public double[] cLActualArrayHighLift;
	public String subfolderPathHL;
	public LSAerodynamicsManager(OperatingConditions conditions, LiftingSurface liftingSurf, Aircraft ac) {

		theOperatingConditions = conditions;
		setTheLiftingSurface(liftingSurf);
		theAircraft = ac;
		set_AerodynamicDatabaseReader(
				theAircraft
				.get_theAerodynamics()
				.get_aerodynamicDatabaseReader()
				);

		getTheLiftingSurface().setAerodynamics(this);
		initializeDataFromAircraft(ac);
		initializeDataFromLiftingSurface(liftingSurf);
		initializeDataFromOperatingConditions(conditions);
		initializeDependentData();
		initializeInnerCalculators();
	}


	public LSAerodynamicsManager(OperatingConditions conditions, LiftingSurface liftingSurf, Aircraft ac, AerodynamicDatabaseReader adbr) {

		theOperatingConditions = conditions;
		setTheLiftingSurface(liftingSurf);
		theAircraft = ac;
		_aerodynamicDatabaseReader = adbr;

		initializeDataFromAircraft(ac);
		initializeDataFromLiftingSurface(liftingSurf);
		initializeDataFromOperatingConditions(conditions);
		initializeDependentData();
		initializeInnerCalculators();
	}

	public LSAerodynamicsManager(OperatingConditions conditions, LiftingSurface liftingSurf) {

		theOperatingConditions = conditions;
		setTheLiftingSurface(liftingSurf);
		getTheLiftingSurface().setAerodynamics(this);

		initializeDataFromLiftingSurface(liftingSurf);
		initializeDataFromOperatingConditions(conditions);
		initializeDependentData();
		initializeInnerCalculators();
	}

	public LSAerodynamicsManager(LiftingSurface liftingSurf) {

		setTheLiftingSurface(liftingSurf);
		getTheLiftingSurface().setAerodynamics(this);

		initializeDataFromLiftingSurface(liftingSurf);
	}


	public void initializeAircraftData(double surfaceReference) {
		this.surfaceReference = surfaceReference;
	}

	public void initializeDataFromAircraft(Aircraft ac) {
		initializeAircraftData(ac.get_wing().get_surface().doubleValue(SI.SQUARE_METRE));
	}

	public void initializeLiftingSurfaceData(
			ComponentEnum lsType, double surface, double surfaceWetted,
			double semispan, double span, double chordRoot, double tcRoot, double dihedralMean,
			double ar, double taperRatioEq, double sweepHalfChordEq, double sweepQuarterChordEq,
			//double maxThicknessMean, 
			double[] yStationsActual, double[] chordsVsYActual, double[] xLEvsYActual,
			double[] twistVsY, double[] dihedral, double[] alpha0l, double[] etaAirfoil) {
		this.lsType = lsType;
		this.surface = surface;
		this.surfaceWetted = surfaceWetted;
		this.semispan = semispan;
		this.span = span;
		this.chordRoot = chordRoot;
		this.tcRoot = tcRoot;
		this.dihedralMean = dihedralMean;
		this.ar = ar;
		this.taperRatioEq = taperRatioEq;
		this.sweepHalfChordEq = sweepHalfChordEq; 
		this.sweepQuarterChordEq = sweepQuarterChordEq;
		//	this.maxThicknessMean = maxThicknessMean;
		this.yStationsActual = yStationsActual;
		this.chordsVsYActual = chordsVsYActual;
		this.xLEvsYActual = xLEvsYActual;
		this.twistVsY = twistVsY;
		this.dihedral = dihedral;
		this.alpha0l = alpha0l;
		this.etaAirfoil = etaAirfoil;	
	}

	public void initializeDataFromLiftingSurface(LiftingSurface ls) {
		initializeLiftingSurfaceData(
				ls.get_type(), 
				ls.get_surface().doubleValue(SI.SQUARE_METRE), 
				ls.get_surfaceWetted().getEstimatedValue(), 
				ls.get_semispan().doubleValue(SI.METER), 
				ls.get_span().doubleValue(SI.METER), 
				ls.get_chordRoot().getEstimatedValue(), 
				ls.get_tc_root(), 
				ls.get_dihedralMean().doubleValue(SI.RADIAN), 
				ls.get_aspectRatio(), 
				ls.get_taperRatioEquivalent(), 
				ls.get_sweepHalfChordEq().doubleValue(SI.RADIAN), 
				ls.get_sweepQuarterChordEq().doubleValue(SI.RADIAN), 
				//ls.get_maxThicknessMean(),
				ls.get_yStationActual().toArray(),
				ls.get_chordsVsYActual().toArray(),
				ls.get_xLEvsYActual().toArray(),
				ls.get_twistVsY().toArray(), 
				ls.get_dihedral().toArray(),
				ls.get_alpha0VsY().toArray(),
				ls.get_etaAirfoil().toArray());

	} 


	public void initializeOperatingConditions(double altitude, double mach, double alpha) {
		this.machCurrent = mach;
		this.altitude = altitude;
		this.alpha = alpha;
	}

	public void initializeDataFromOperatingConditions(OperatingConditions ops) {
		initializeOperatingConditions(
				ops.get_altitude().doubleValue(SI.METER), 
				ops.get_machCurrent(),
				ops.get_alphaCurrent().doubleValue(SI.RADIAN));
	}

	public void initializeDependentData() {
		_vortexSemiSpan = _vortexSemiSpanToSemiSpanRatio * semispan;
		_nPointsSemispanWise = (int) (1./(2*_vortexSemiSpanToSemiSpanRatio));

//		JPADStaticWriteUtils.logToConsole("\n_numberOfPointsSemispanWise: " + _nPointsSemispanWise 
//				+ "\nVortex semi span length: " + _vortexSemiSpan + "\n");

		//		_yStations = MyMathUtils.linspace(
		//				_vortexSemiSpan,
		//				liftingSurface._semispan. - _vortexSemiSpan,
		//				_nPointsSemispanWise).data;

		_yStations = MyArrayUtils.linspace(0., semispan, _nPointsSemispanWise);
		if (theAircraft != null ){
			if (theAircraft.get_exposedWing() != null && theLiftingSurface.getType()==ComponentEnum.WING){
				double yLocRootExposed = theAircraft.get_exposedWing().get_theAirfoilsList().get(0).getGeometry().get_yStation();
				_yStationsIntegral = MyArrayUtils.linspace(yLocRootExposed, semispan, _nPointsSemispanWise);

				_yStationsND = MyArrayUtils.linspace(0., 1., _nPointsSemispanWise);
				//					
				//					for (int i=0; i<_nPointsSemispanWise; i++){
				//						_chordsVsYExposed.add(getTheLiftingSurface().getChordAtYActual(_yStationsIntegral[i]));
				//		
				//					_alpha0lDistributionExposed = MyArray.createArray(
				//							theAircraft.get_exposedWing().get_alpha0VsYExposed().interpolate(
				//										theAircraft.get_exposedWing().get_etaAirfoilExposed().toArray(),
				//										_yStationsND));
				//					
				//					_twistDistribution = MyArray.createArray(
				//							theAircraft.get_exposedWing().get_twistVsYExposed().interpolate(
				//									theAircraft.get_exposedWing().get_etaAirfoilExposed().toArray(),
				//									_yStationsND));
				//						
				//					}
				_chordsVsYExposed.toArray();

			}
		}
		/** Non dimensional stations (eta) */
		_chordsVsYExposed.toArray();
		_yStationsND = MyArrayUtils.linspace(0., 1., _nPointsSemispanWise);

		_yStationsIntegral = MyArrayUtils.linspace(0.0, semispan, _nPointsSemispanWise);

		for (int i=0; i<_nPointsSemispanWise; i++){
			_chordsVsY.add(getTheLiftingSurface().getChordAtYActual(_yStations[i]));
		}
		//_chordsVsY.toArray();


		_twistDistribution = MyArray.createArray(
				theLiftingSurface._twistVsY.interpolate(
						etaAirfoil,
						_yStationsND));

		_alpha0lDistribution = MyArray.createArray(
				theLiftingSurface.get_alpha0VsY().interpolate(
						etaAirfoil,
						_yStationsND));

		//		_alpha0lVsY = MyArray.createArray(liftingSurface._alpha0l_y.toArray(), SI.RADIAN);
		//		_alpha0lVsY.interpolate(liftingSurface._etaAirfoil.toArray(), liftingSurface._eta.toArray());

		_clAlphaVsY = MyArray.createArray(getTheLiftingSurface()._clAlpha_y.toArray())
				.interpolate(etaAirfoil, _yStationsND);

		alphaArray = new MyArray(SI.RADIAN); 
		cLArray = new MyArray(Unit.ONE);

		alphaStart = Amount.valueOf(toRadians(-2.), SI.RADIAN);
//		alphaEnd = Amount.valueOf(toRadians(12.), SI.RADIAN);
		alphaEnd = Amount.valueOf(toRadians(28.), SI.RADIAN);
		_numberOfAlpha = 15; //8
		alphaArray.setDouble(MyArrayUtils.linspace(
				alphaStart.getEstimatedValue(), 
				alphaEnd.getEstimatedValue(), 
				_numberOfAlpha));
		//		alphaArray.setDouble(new double[]{toRadians(3.), toRadians(5.), toRadians(7.), toRadians(9.)});
	}




	@Override
	public void initializeInnerCalculators() {

		calculateMachCr = new CalcMachCr();
		calculateCdWaveDrag = new CalcCdWaveDrag();

		calculateCLAlpha = new CalcCLAlpha();
		calculateAlpha0L = new CalcAlpha0L();
		calculateCL0 = new CalcCL0();

		calculateLiftDistribution = new CalcLiftDistribution(surface, 
				semispan, _yStations, yStationsActual, chordsVsYActual, xLEvsYActual, 
				dihedral, _twistDistribution.toArray(), _alpha0lDistribution.toArray(),
				_vortexSemiSpanToSemiSpanRatio, 
				machCurrent, altitude, alpha);

		calculateBasicLoadDistribution = new CalcBasicLoad();

		calculateCLAtAlpha = new CalcCLAtAlpha();
		calculateCLvsAlphaCurve = new CalcCLvsAlphaCurve();

		calculateCmAC = new CalcCmAC();
		calculateXAC = new CalcXAC();

		calculateCLMaxClean = new CalcCLMaxClean();
		calculateCmAlpha = new CalcCmAlpha(theAircraft);
		calculateCm0 = new CalcCm0();
	}

	/**
	 * Set AoA of root chord
	 * 
	 * @author Lorenzo Attanasio
	 * @return
	 */
	private void setAlphaRoot(Amount<Angle> alphaRoot) {
		_alphaCurrent = alphaRoot;
		setAlphaRoot(alphaRoot.doubleValue(SI.RADIAN));
	}

	private void setAlphaRoot(double alphaRoot) {
		_alphaDistribution = new MyArray(
				AnglesCalc.getAlphaDistribution(alphaRoot, _twistDistribution.toArray(), _alpha0lDistribution.toArray()));
	}

	/**
	 * Evaluate all aerodynamic quantities.
	 * Each object must be called exactly in the order in 
	 * which it appears in this method.
	 * 
	 * @author Lorenzo Attanasio
	 * @param alphaRoot
	 */
	public void calculateAll(double mach, Amount<Angle> alphaRoot) {

		System.out.println("Running Aerodynamics.calculateAll() for " + lsType.name());
		setAlphaRoot(alphaRoot);

		// TODO: check if compressibility factor has to be evaluated even if 
		// we use the lock - korn method to evaluate wave drag
		getTheLiftingSurface().calculateFormFactor(calculateCompressibility(mach));
		calculateCD0Total();
		calculateCLAlpha.allMethods();
		calculateAlpha0L.allMethods();

		calculateXAC.allMethods();
		calculateAirfoilAerodynamics();

		calculateCmAC.allMethods();
		calculateMachCr.allMethods();
		calculateCdWaveDrag.allMethods();
		//		calculateBasicLoadDistribution.shrenk();
		calculateLiftDistribution.allMethods(alphaRoot);
		calculateCLvsAlphaCurve.allMethods();

		calculateCLMaxClean.allMethods();
		calculateCmAlpha.allMethods();
	}

	/**
	 * Evaluate all aerodynamic quantities with no aircraft.
	 * Each object must be called exactly in the order in 
	 * which it appears in this method.
	 * 
	 * @author Manuela Ruocco
	 * @param alphaRoot
	 */

	public void calculateAllIsolatedWing(double mach, Amount<Angle> alphaRoot) {

		System.out.println("Running Aerodynamics.calculateAll() for " + lsType.name());
		setAlphaRoot(alphaRoot);

		// TODO: check if compressibility factor has to be evaluated even if 
		// we use the lock - korn method to evaluate wave drag
		getTheLiftingSurface().calculateFormFactor(calculateCompressibility(mach));
		//calculateCD0Total();
		calculateCLAlpha.allMethods();
		calculateAlpha0L.allMethods();

		calculateXAC.allMethods();
		calculateAirfoilAerodynamics();

		calculateCmAC.allMethods();
		calculateMachCr.allMethods();
		calculateCdWaveDrag.allMethods();
		//		calculateBasicLoadDistribution.shrenk();
		calculateLiftDistribution.allMethods(alphaRoot);
		calculateCLvsAlphaCurve.allMethods();

		calculateCLMaxClean.allMethods();
		calculateCmAlpha.allMethods();
	}

	public void calculatePrandtlGlauertCorrection() {
		_betaPG = AerodynamicCalc.calculatePrandtlGlauertCorrection(machCurrent);
	}

	public double calculateCL0() {
		return -calculateAlpha0L.integralMeanWithTwist().getEstimatedValue()
				* calculateCLAlpha.andersonSweptCompressibleSubsonic();
	}

	/** 
	 * Check if basic load is actually 0.0
	 * 
	 * @return
	 */
	public double checkBasicLoad() {
		return (2/surface)
				* MyMathUtils.integrate1DSimpsonSpline(
						getTheLiftingSurface()._yStationsAirfoil.toArray(), 
						getTheLiftingSurface()._clBasic_y.times(getTheLiftingSurface()._chordVsYAirfoils).toArray(), 
						0., semispan);
	}

	/**
	 * Calculate lifting surface aerodynamic drag
	 * 
	 * @author Lorenzo Attanasio
	 * @param conditions
	 * @param theAircraft
	 * @param performances
	 * @param weights
	 */
	public double calculateCD0Total() {

		Double kExcr = DragCalc.calculateKExcrescences(theAircraft.get_sWetTotal());

		calculateCd0Parasite();
		calculateCdWingFusInterference();
		calculateCdWingNacelleInterference();
		calculateCdGap();

		_cD0Total = 
				_cd0Parasite*(1 + kExcr) 
				+ _cdGap 
				+ _cdWFInterf
				+ _cdWNInterf;

		return _cD0Total; 
	}


	public double calculateCd0Parasite(){
		_reynolds = theOperatingConditions.calculateRe(
				getTheLiftingSurface().get_meanAerodChordActual().getEstimatedValue(), 
				getTheLiftingSurface().get_roughness().getEstimatedValue());

		if (theOperatingConditions.calculateReCutOff(
				getTheLiftingSurface().get_meanAerodChordActual().getEstimatedValue(), 
				getTheLiftingSurface().get_roughness().getEstimatedValue()) < 
				_reynolds) {

			_reynolds = theOperatingConditions.calculateReCutOff(
					getTheLiftingSurface().get_meanAerodChordActual().getEstimatedValue(), getTheLiftingSurface().get_roughness().getEstimatedValue());

			_cF  = (AerodynamicCalc.calculateCf(
					_reynolds, machCurrent, 
					getTheLiftingSurface().get_xTransitionU()) 
					+ AerodynamicCalc.calculateCf(_reynolds, 
							machCurrent, getTheLiftingSurface().get_xTransitionL()))/2;

		} else // XTRANSITION!!!
		{
			_cF  = (AerodynamicCalc.calculateCf(_reynolds, machCurrent, getTheLiftingSurface().get_xTransitionU()) + 
					AerodynamicCalc.calculateCf(_reynolds, machCurrent, getTheLiftingSurface().get_xTransitionL()))/2; 

		}

		if (getTheLiftingSurface()._type == ComponentEnum.WING) {
			_cd0Parasite = 
					_cF * getTheLiftingSurface()._formFactor 
					* getTheLiftingSurface().get_surfaceWettedExposed().getEstimatedValue()
					/ surface;			

		} else { //TODO NEED TO EVALUATE Exposed Wetted surface also for Vtail and Htail
			_cd0Parasite = 
					_cF * getTheLiftingSurface()._formFactor 
					* surfaceWetted/surfaceReference;
		}

		return _cd0Parasite;
	}


	// Matlab script "Polare_ATR72"
	public double calculateCdWingFusInterference() {
		if (getTheLiftingSurface()._type == ComponentEnum.WING){

			_cdWFInterf = ((0.5*Math.pow(getTheLiftingSurface()._positionRelativeToAttachment,2) 
					+ 1.25*getTheLiftingSurface()._positionRelativeToAttachment + 0.75)
					* 2.16*Math.pow(chordRoot,2)
					* Math.pow((getTheLiftingSurface().get_tc_root()),3))
					/surfaceReference;

		} else {
			_cdWFInterf = 0.0;
		}

		return _cdWFInterf;
	}


	// Matlab script "Polare_ATR72"
	public double calculateCdWingNacelleInterference() {
		if (getTheLiftingSurface()._type == ComponentEnum.WING){
			_cdWNInterf = 0.0033*Math.pow(
					//TODO: change this
					theAircraft.get_theNacelles().get_nacellesList().get(0).get_diameterMean().getEstimatedValue()
					* theAircraft.get_theNacelles().get_nacellesNumber(),2)/
					surface;
		} else {
			_cdWNInterf = 0.0;
		}

		return _cdWNInterf;
	}


	// Matlab script "Polare_ATR72"
	public double calculateCdGap() {
		// Swet_w = Swet_w_exposed??
		_cdGap = 0.0002*(
				Math.pow(Math.cos(sweepQuarterChordEq),2))
				* 0.3 * surfaceWetted/surfaceReference;

		return _cdGap; 
	}


	/**
	 * 	Compressibility factor
	 */
	public double calculateCompressibility(double mach) {

		double k = 1.
				/ Math.sqrt(
						1 - Math.pow(mach, 2)
						* (Math.pow(Math.cos(sweepQuarterChordEq),2))
						);
		_compressibilityFactor = k;
		return k;
	}

	public void calculateAirfoilAerodynamics() {

		// The lifting surface is assumed to have conventional
		// or supercritical airfoils, not both
		_airfoilType =  getTheLiftingSurface()._theAirfoilsList.get(0).get_type();

		for (int i = 0; i < getTheLiftingSurface()._numberOfAirfoils; i++){

			//FIXME: change mach value
			getTheLiftingSurface()._theAirfoilsList.get(i).getAerodynamics().set_mach(machCurrent);
			getTheLiftingSurface()._xAcAirfoil
			.add(getTheLiftingSurface()._theAirfoilsList.get(i).getAerodynamics().get_aerodynamicCenterX()
					* getTheLiftingSurface()._chordsVsYActual.get(i));

			getTheLiftingSurface()._distanceAirfoilACFromWingAC
			.add(getTheLiftingSurface()._xACActualLRF.getEstimatedValue() 
					- getTheLiftingSurface()._xAcAirfoil.get(i) 
					- getTheLiftingSurface().getXLEAtYActual(getTheLiftingSurface()._yStationsAirfoil.get(i)));

			getTheLiftingSurface()._theAirfoilsList.get(i)
			.getAerodynamics().set_alphaRoot(getCalculateAlpha0L().integralMeanWithTwist());

			getTheLiftingSurface()._theAirfoilsList.get(i)
			.getAerodynamics().calculateClAtAlphaEffective();

			getTheLiftingSurface()._theAirfoilsList.get(i)
			.getAerodynamics().getCalculateCdWaveDrag().allMethods();

			getTheLiftingSurface()._clBasic_y
			.add(0.5*getTheLiftingSurface()._theAirfoilsList.get(i).getAerodynamics().get_clCurrent());

		}

		getTheLiftingSurface()._distanceAirfoilACFromWingAC.toArray();
		getTheLiftingSurface()._xAcAirfoil.toArray();
		getTheLiftingSurface()._clBasic_y.toArray();

		//		System.out.println("------" + checkBasicLoad());
	}


	public class CalcBasicLoad extends InnerCalculator {

		private Amount<Angle> alpha0L;
		private MyArray clDistribution;

		public CalcBasicLoad() {
			alpha0L = calculateAlpha0L.integralMeanWithTwist();
		}

		//		public MyArray nasaBlackwell() {
		//			System.out.println("Calling CalculateBasicLoad.nasaBlackwell() ----------------");
		//			if (liftingSurface._type != MyComponent.ComponentEnum.VERTICAL_TAIL) {
		//				calculateLiftDistribution.get_nasaBlackwell().calculate(alpha0L);
		//				clDistribution = calculateLiftDistribution.get_nasaBlackwell().get_clAdditionalDistributionCurrent().clone();
		//			} else {
		//				clDistribution.fillZeros(_nPointsSemispanWise);
		//			}
		//
		//			return clDistribution;
		//		}

		public MyArray shrenk() {
			//			setAlphaRoot(alpha0L.copy());
			//			System.out.println("Calling CalculateBasicLoad.shrenk() ----------------");
			clDistribution = MyArray.createArray(
					_clAlphaVsY.times(_twistDistribution.minus(alpha0L.getEstimatedValue())).times(0.5));
			return clDistribution;
		}

		public MyArray getClDistribution() {
			return clDistribution;
		}

		@Override
		public void allMethods() {
			// TODO Auto-generated method stub

		}

	}


	public class CalcLiftDistribution extends InnerCalculator{

		private double mach, altitude, surface, semispan, vortexSemiSpanToSemiSpanRatio;
		private double _cLCurrent, alpha;

		private double[] yStations, dihedral, twist, alpha0l, yStationsActual, chordsVsYActual, xLEvsYActual;

		private NasaBlackwell nasaBlackwell;

		public CalcLiftDistribution(
				double surface, double semispan,
				double[] yStations,
				double[] yStationsActual, double[] chordsVsYActual,
				double[] xLEvsYActual,
				double[] dihedral, double[] twist, double[] alpha0l,
				double vortexSemiSpanToSemiSpanRatio,
				double mach, double altitude, double alpha) {
			this.mach = mach;
			this.altitude = altitude;
			this.alpha = alpha;
			this.surface = surface;
			this.semispan = semispan;
			this.yStations = yStations;
			this.vortexSemiSpanToSemiSpanRatio = vortexSemiSpanToSemiSpanRatio;
			this.dihedral = dihedral;
			this.twist = twist;
			this.alpha0l = alpha0l;
			this.yStationsActual = yStationsActual;
			this.chordsVsYActual = chordsVsYActual;
			this.xLEvsYActual = xLEvsYActual;


			nasaBlackwell = new NasaBlackwell(semispan, surface, 
					yStationsActual, chordsVsYActual, xLEvsYActual, dihedral, twist, alpha0l, 
					vortexSemiSpanToSemiSpanRatio, alpha, mach, altitude);
		}

		public CalcLiftDistribution() {}

		public CalcLiftDistribution(Amount<Angle> alpha) {
			_alphaCurrent = alpha;
		}

		public double[] schrenk() {

			_cLCurrent = get_cLCurrent();
			MyArray ellChordVsY = new MyArray(
					MyMathUtils.getInterpolatedValue1DLinear(
							yStationsActual, 
							getTheLiftingSurface()._ellChordVsY.toArray(),
							yStations));

			MyArray chordsY = new MyArray(
					MyMathUtils.getInterpolatedValue1DLinear(
							yStationsActual, 
							chordsVsYActual,
							yStations));

			MyArray alpha0lY = new MyArray(
					MyMathUtils.getInterpolatedValue1DLinear(
							_yStationsND, 
							_alpha0lDistribution.toArray(),
							yStations));

			_loadAdd = MyArray.createArray((ellChordVsY.plus(chordsY).divide(2.)));

			_loadBas = new MyArray(Unit.ONE);
			_loadBas.setDouble(chordsY
					.times((_twistDistribution.minus(alpha0lY)))
					.times((0.5*calculateCLAlpha.andersonSweptCompressibleSubsonic())));

			_gammaBasic = MyArray.createArray(
					_loadBas.divide(2*span));

			_gammaAdd = MyArray.createArray(
					_loadAdd.times(_cLCurrent
							/(2*span)));
			System.out.println( " CL CURRENT  " + _cLCurrent);

			_c_cLAdd = MyArray.createArray(_loadAdd.times(_cLCurrent));
			_ccLVsY = _c_cLAdd.plus(_loadBas);

			double[] clLocal = _ccLVsY.divide(chordsY);

			_gammaTot = MyArray.createArray(_ccLVsY.divide(2*span));
			_gammaTotIntegral = ar 
					* MyMathUtils.integrate1DSimpsonSpline(_yStationsND, _gammaTot.toArray());

			_gammaTotBasic = MyMathUtils.integrate1DSimpsonSpline(_yStationsND, _gammaBasic.toArray());

			_loadTotIntegral = (2/surface)
					* MyMathUtils.integrate1DSimpsonSpline(yStations, _ccLVsY.toArray())/2;					

			// TODO: following assignments are not correct! 
			//cLMap.getcXVsAlphaTable().put(MethodEnum.SCHRENK, _alphaCurrent, _cLCurrent);
			//cLMap.getCxyVsAlphaTable().put(MethodEnum.SCHRENK, _alphaCurrent, new MyArray(clLocal));
			//cLMap.getCcxyVsAlphaTable().put(MethodEnum.SCHRENK, _alphaCurrent, _ccLVsY.clone());
			return _ccLVsY.toArray();
		}



		/**
		 * This function calculates the lift distribution using the cl of 50 airfoil among the semispan.
		 * This function creates a new airfoil with its own characteristics for each of 50 station and 
		 * evaluates the local cl both in its linear trait and non linear.
		 * 
		 * @author Manuela Ruocco 
		 * @param Amount<Angle> alpha
		 * @param LiftingSurface theWing
		 */  


		public void airfoilLiftDistribution (Amount<Angle> alpha, LiftingSurface theWing){
			double [] clLocalAirfoil = new double [_nPointsSemispanWise];
			MyAirfoil intermediateAirfoil;
			double alphaDouble = alpha.getEstimatedValue();

			MyAirfoil airfoilRoot = theWing.get_theAirfoilsList().get(0);
			MyAirfoil airfoilKink = theWing.get_theAirfoilsList().get(1);
			MyAirfoil airfoilTip = theWing.get_theAirfoilsList().get(2);

			for (int i=0 ; i< _nPointsSemispanWise ; i++){
				intermediateAirfoil = calculateIntermediateAirfoil(
						theWing, _yStations[i]);
				clLocalAirfoil[i] = intermediateAirfoil.getAerodynamics().calculateClAtAlpha(alphaDouble);
				clLocalAirfoil[_nPointsSemispanWise-1] = 0;

			}	
			System.out.println(" cl distribution " + Arrays.toString(clLocalAirfoil));
		}


		@Override
		public void allMethods() {
			// TODO Auto-generated method stub
		}

		public void allMethods(Amount<Angle> alpha) {
			if (getTheLiftingSurface()._type != ComponentEnum.VERTICAL_TAIL) { 
				System.out.println("alpha " + alpha.getEstimatedValue());
				nasaBlackwell.calculate(alpha);
			}
			schrenk();
		}

		public NasaBlackwell getNasaBlackwell() {
			return nasaBlackwell;
		}

	}


	/** 
	 * Calculate the lifting surface critical Mach number
	 * 
	 * @author Lorenzo Attanasio
	 */
	public class CalcMachCr extends InnerCalculator {

		/** 
		 * Korn-Mason method for estimating critical mach number
		 *
		 * @author Lorenzo Attanasio
		 * @see Sforza (2014), page 417
		 */
		public double kornMason(double cL) {

			double k = 0.95;
			if (_airfoilType == AirfoilTypeEnum.CONVENTIONAL) k = 0.87;

			double machCr = AerodynamicCalc.calculateMachCriticalKornMason(cL,
					sweepHalfChordEq, maxThicknessMean, 
					_airfoilType);

			_methodsMap.put(MethodEnum.KORN_MASON, machCr);
			_machNumber3DCritical = machCr;

			return machCr;
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
		public double kroo(double cL) {

			// sweepHalfChord --> radians are required

			double y = cL/(Math.pow(cos(sweepHalfChordEq),2));
			double x = maxThicknessMean/(Math.cos(sweepHalfChordEq));

			double m_cr = ((2.8355*Math.pow(x, 2)) - (1.9072*x) + 0.9499 - (0.2*y) + (0.4262*x*y)) /
					(Math.cos(sweepHalfChordEq) );

			// this method work for peaky airfoils; for modern supercritical some corrections
			// have to be made.
			if (_airfoilType.equals(AirfoilTypeEnum.SUPERCRITICAL))
				m_cr += 0.035;
			else if (_airfoilType.equals(AirfoilTypeEnum.MODERN_SUPERCRITICAL))
				m_cr += 0.06;

			return m_cr;
		}

		public void allMethods() {
			kornMason(_cLCurrent);
			kroo(_cLCurrent);
		}

	}

	/**
	 * Calculate a lifting surface wave drag
	 * 
	 * @author Lorenzo Attanasio
	 *
	 */
	public class CalcCdWaveDrag extends InnerCalculator {

		/**
		 * Calculate the wave drag
		 * 
		 * @author Lorenzo Attanasio
		 * @see Sforza (2014)
		 * @param cL
		 * @param machCurrent
		 * @param machCr
		 * @return
		 */

		public double lockKorn(double cL, double machCurrent) {

			_cDw = DragCalc.calculateCDWaveLockKorn(cL, machCurrent, calculateMachCr.kornMason(cL));

			_methodsMap.put(MethodEnum.LOCK_KORN, _cDw.doubleValue());

			return _cDw.doubleValue();
		}

		public void allMethods() {
			lockKorn(_cLCurrent, machCurrent);
		}

	}


	/** 
	 * Evaluate the AC x coordinate relative to MAC
	 */
	public class CalcXAC extends InnerCalculator {

		private Map<MethodEnum, Amount<Length>> _methodMapMRF = 
				new TreeMap<MethodEnum, Amount<Length>>();
		private Map<MethodEnum, Amount<Length>> _methodMapLRF = 
				new TreeMap<MethodEnum, Amount<Length>>();

		/**
		 * MRF = Mean aerodynamic chord Reference Frame
		 * LRF = Wing Local Reference Frame
		 */
		public double atQuarterMAC() {
			getTheLiftingSurface()._xACActualMRF = getTheLiftingSurface()._meanAerodChordActual.times(0.25);
			getTheLiftingSurface()._xACActualLRF = getTheLiftingSurface()._xACActualMRF.plus(getTheLiftingSurface()._xLEMacActualLRF);
			_methodMapMRF.put(MethodEnum.QUARTER, getTheLiftingSurface()._xACActualMRF.copy());
			_methodMapLRF.put(MethodEnum.QUARTER, getTheLiftingSurface()._xACActualLRF.copy());
			return getTheLiftingSurface()._xACActualMRF.getEstimatedValue();
		}

		/**
		 * @see page 555 Sforza
		 */
		public double deYoungHarper() {
			getTheLiftingSurface()._xACActualMRF = Amount.valueOf(
					LSGeometryCalc.calcXacFromLEMacDeYoungHarper(ar, getTheLiftingSurface()._meanAerodChordActual.doubleValue(SI.METER), 
							taperRatioEq, sweepQuarterChordEq),
					SI.METER);
			getTheLiftingSurface()._xACActualLRF = getTheLiftingSurface()._xACActualMRF.plus(getTheLiftingSurface()._xLEMacActualLRF);
			_methodMapMRF.put(MethodEnum.DEYOUNG_HARPER, getTheLiftingSurface()._xACActualMRF.copy());
			_methodMapLRF.put(MethodEnum.DEYOUNG_HARPER, getTheLiftingSurface()._xACActualLRF.copy());
			return getTheLiftingSurface()._xACActualMRF.getEstimatedValue();
		}


		/**
		 *  page 53 Napolitano 
		 */
		public double datcomNapolitano() {
			getTheLiftingSurface()._xACActualMRF = Amount.valueOf(
					LSGeometryCalc.calcXacFromNapolitanoDatcom(getTheLiftingSurface()._meanAerodChordActual.doubleValue(SI.METER),
							taperRatioEq ,sweepHalfChordEq, ar,  
							theOperatingConditions.get_machCurrent(),
							//theLiftingSurface.getAerodynamics().get_AerodynamicDatabaseReader() ), SI.METER);
							theAircraft.get_theAerodynamics().get_aerodynamicDatabaseReader() ),SI.METER);
			double xacNapolitano=getTheLiftingSurface()._xACActualMRF.getEstimatedValue();

			//			System.out.println("taper ratio " + taperRatioEq);
			//			System.out.println("sweep angle " + sweepHalfChordEq);
			//			System.out.println("aspect ratio " + ar);
			//			System.out.println("Mach Number" + theOperatingConditions.get_machCurrent());
			return xacNapolitano;

		}



		public void allMethods() {
			atQuarterMAC();
			deYoungHarper(); // Report NACA
			datcomNapolitano();
		}

		public Map<MethodEnum, Amount<Length>> get_methodMapMRF() {
			return _methodMapMRF;
		}

		public Map<MethodEnum, Amount<Length>> get_methodMapLRF() {
			return _methodMapLRF;
		}
	}


	public class CalcCLAtAlpha extends InnerCalculator {

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

		public CalcCLAtAlpha() {

		}

		public void linearDLR(double alpha) {
			// page 3 DLR pdf
			_cL = LiftCalc.calcCLatAlphaLinearDLR(alpha, ar);
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

			//JPADStaticWriteUtils.logToConsole("\nAlpha " + alpha + "\nCL: " + cL + "\n");

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
			MyAirfoil meanAirfoil = new MeanAirfoil().calculateMeanAirfoil(getTheLiftingSurface());
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
				Amount<Angle> alphaMax = get_alphaMaxClean();	
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

		@Override
		public void allMethods() {
			// TODO Auto-generated method stub
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


			Amount<Angle> angleOfIncidence = theLiftingSurface.get_iw();
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


			Amount<Angle> angleOfIncidence = theLiftingSurface.get_iw();
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
					theLiftingSurface, 
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
					leRadiusSlatRatio, 
					cExtcSlat);
			
			theHighLiftCalculatorLiftEffects.calculateHighLiftDevicesEffects();
			
			deltaCLMaxElevator = theHighLiftCalculatorLiftEffects.getDeltaCLmax_flap()*tauValue;
			double deltaAlphaMaxElevator =-(tauValue * deflection.getEstimatedValue())/2;
			double deltaAlphaMaxElevatordelta = theHighLiftCalculatorLiftEffects.getDeltaAlphaMaxFlap();
			CalcCLAtAlpha theCLCleanCalculator = new CalcCLAtAlpha();
			
			
			MyAirfoil meanAirfoil = new MeanAirfoil().calculateMeanAirfoil(getTheLiftingSurface());
			calcAlphaAndCLMax(meanAirfoil);
			Amount<Angle> alphaMax = get_alphaMaxClean().to(NonSI.DEGREE_ANGLE);
			double alphaStarClean = meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue();
			Amount<Angle> alphaStarCleanAmount = Amount.valueOf(alphaStarClean, SI.RADIAN);
			double cLMax=get_cLMaxClean();
			double cLStarClean = theCLCleanCalculator.nasaBlackwellCompleteCurve(alphaStarCleanAmount);
			double cL0Elevator = cLArray[0];
			double alphaStar = (cLStarClean - qValue)/clAlpha;
			alphaStarClean = alphaStarClean*57.3;		
			double cLMaxElevator = cLMax + deltaCLMaxElevator;
			
				alphaStallElevator = alphaMax.getEstimatedValue() + deltaAlphaMaxElevator;
			

			double alphaStarElevator; 

				alphaStarElevator = (alphaStar + alphaStarClean)/2;

			
			cLArray[1] = cLLinearSlopeNoDeflection * (alphaStarElevator + tauValue * deflectionAngleDeg);
			
			double[][] matrixData = { {Math.pow(alphaStallElevator, 3), Math.pow(alphaStallElevator, 2)
				, alphaStallElevator,1.0},
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
					+ theAircraft.get_HTail().get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue();

			double[]  clArray = calculateCLWithElevatorDeflection(
					deltaFlap, flapType,deltaSlat,
					etaInFlap, etaOutFlap, etaInSlat, etaOutSlat, 
					 cfc, csc, leRadiusSlatRatio, cExtcSlat
					);

			double clAtAlpha = MyMathUtils.getInterpolatedValue1DLinear(alphaLocalArray, clArray, alphaLocal);

			return clAtAlpha;

		}


	}

	public double getAlphaZeroLiftDeflection() {
		return alphaZeroLiftDeflection;
	}

	/** 
	 * This function plot CL vs Alpha curve of an isolated wing using 30 value of alpha between alpha=- 10.0 deg and
	 * alphaMax+2. 
	 * 
	 * @author Manuela Ruocco
	 */
	public void plotCLvsAlphaCurve(){
		CalcCLAtAlpha theCLCalculator = new CalcCLAtAlpha();

		double alphaFirst = -10.0;
		Amount<Angle> alphaActual;
		Amount<Angle> alphaTemp = Amount.valueOf(0.0, SI.RADIAN);
		int nPoints = 40;
		MyAirfoil meanAirfoil = new MeanAirfoil().calculateMeanAirfoil(getTheLiftingSurface());
		double alphaStar = meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue();
		Amount<Angle> alphaStarAmount = Amount.valueOf(alphaStar, SI.RADIAN);
		double cLStar = theCLCalculator.nasaBlackwell(alphaStarAmount);
//		double cLTemp = theCLCalculator.nasaBlackwell(alphaTemp);
//		double cLLinearSlope = (cLStar - cLTemp)/alphaStar;
		
		CalcCLAlpha theLineraSlopeCalculator = new CalcCLAlpha();
		double cLLinearSlope = theLineraSlopeCalculator.nasaBlackwell(theCLCalculator);
		
		double qValue = cLStar - cLLinearSlope*alphaStar;

		calcAlphaAndCLMax(meanAirfoil);
		double cLMax = get_cLMaxClean();
		Amount<Angle> alphaMax = get_alphaMaxClean().to(NonSI.DEGREE_ANGLE);
		double alphaMaxDoubleDegree = alphaMax.getEstimatedValue();
		double alphaMaxDouble = alphaMax.to(SI.RADIAN).getEstimatedValue();
		alphaArrayPlot = MyArrayUtils.linspace(alphaFirst,alphaMaxDoubleDegree + 2, nPoints);
		cLArrayPlot = new double [nPoints];

		double[][] matrixData = { {Math.pow(alphaMaxDouble, 3), Math.pow(alphaMaxDouble, 2), alphaMaxDouble,1.0},
				{3* Math.pow(alphaMaxDouble, 2), 2*alphaMaxDouble, 1.0, 0.0},
				{3* Math.pow(alphaStar, 2), 2*alphaStar, 1.0, 0.0},
				{Math.pow(alphaStar, 3), Math.pow(alphaStar, 2),alphaStar,1.0}};
		RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
		double [] vector = {cLMax, 0,cLLinearSlope, cLStar};

		double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

		double a = solSystem[0];
		double b = solSystem[1];
		double c = solSystem[2];
		double d = solSystem[3];


		String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		if(subfolderPathCheck)
			subfolderPathCLAlpha = JPADStaticWriteUtils.createNewFolder(folderPath + "CL alpha Wing Clean" + File.separator);



		for ( int i=0 ; i< alphaArrayPlot.length ; i++){
			alphaActual = Amount.valueOf(toRadians(alphaArrayPlot[i]), SI.RADIAN);
			if (alphaActual.getEstimatedValue() < alphaStar) { 
				cLArrayPlot[i] = cLLinearSlope * alphaActual.getEstimatedValue() + qValue;}
			else {
				cLArrayPlot[i] = a * Math.pow(alphaActual.getEstimatedValue(), 3) + 
						b * Math.pow(alphaActual.getEstimatedValue(), 2) + 
						c * alphaActual.getEstimatedValue() + d;
			}

		}


		MyChartToFileUtils.plotNoLegend(
				alphaArrayPlot,	cLArrayPlot, 
				null, null , null , null ,					    // axis with limits
				"alpha_W", "CL", "deg", "",	   				
				subfolderPathCLAlpha, "CL vs Alpha clean " + theLiftingSurface.getType());
	}

	public void PlotCLvsAlphaCurve(String subfolderPath){
		this.subfolderPathCLAlpha = subfolderPath;
		subfolderPathCheck = false;
		plotCLvsAlphaCurve();
		subfolderPathCheck = true;

	};


	/** 
	 * This function plot CD vs Alpha curve  and CD vs CL curve of a wing, using 30 value of alpha between 
	 * alpha=- 2 deg and alpha = 18 deg
	 * 
	 * @author Manuela Ruocco
	 */
	public void PlotCDvsAlphaCurve(){

		double alphaFirstCD = -2.0;
		double alphaLastCD = 18.0;
		int nPointsCD = 30;
		double [] alphaArrayPlotCD = MyArrayUtils.linspace(alphaFirstCD,alphaLastCD, nPointsCD);
		double [] cDPlotArray = new double [nPointsCD];
		double [] cLPlotArray = new double [nPointsCD];
		Amount<Angle> alphaActual;
		CalcCDAtAlpha theCDCalculator = new CalcCDAtAlpha();
		CalcCLAtAlpha theCLCalculator = new CalcCLAtAlpha();
		String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		
		if(subfolderPathCheck)
			subfolderPathCDAlpha = JPADStaticWriteUtils.createNewFolder(folderPath + "CD wing" + File.separator);

		for (int i=0; i<nPointsCD; i++){
			alphaActual= Amount.valueOf(toRadians(alphaArrayPlotCD[i]), SI.RADIAN);

			cDPlotArray[i] = 
					theCDCalculator
					.integralFromCdAirfoil(
							alphaActual,
							MethodEnum.NASA_BLACKWELL,
							this
							);
			cLPlotArray[i] = theCLCalculator.nasaBlackwell(alphaActual);

		}

		MyChartToFileUtils.plotNoLegend(
				alphaArrayPlotCD,cDPlotArray, 
				null, null , null , null ,					    // axis with limits
				"alpha", "CD", "deg", "",	   				
				subfolderPathCDAlpha, "CD vs Alpha " + theLiftingSurface.get_type());

		MyChartToFileUtils.plotNoLegend(
				cDPlotArray,cLPlotArray, 
				null, null , null , null ,					    // axis with limits
				"CD", "CL", "", "",	   				
				subfolderPathCDAlpha, "CD vs CL " + theLiftingSurface.get_type());

	}
	
	
		public void PlotCDvsAlphaCurve(String subfolderPath){
			this.subfolderPathCDAlpha = subfolderPath;
			subfolderPathCheck = false;
			PlotCDvsAlphaCurve();
			subfolderPathCheck = true;
		
	}
	
	/** 
	 * Evaluate CL vs alpha
	 * 
	 * @author Lorenzo Attanasio
	 */
	public class CalcCLvsAlphaCurve extends InnerCalculator {

		private Map<MethodEnum, Map<Double, Double>> _curveMap = 
				new TreeMap<MethodEnum, Map<Double, Double>>();
		private Map<MethodEnum, MyArray> _cLMap = 
				new TreeMap<MethodEnum, MyArray>();

		public CalcCLvsAlphaCurve() {
			if (calculateCLAtAlpha == null) {
				calculateCLAtAlpha = new CalcCLAtAlpha();
			}
		}

		public void linearDLR() {
			double[] cL = new double[_numberOfAlpha];
			for (int i=0; i< _numberOfAlpha; i++) {
				calculateCLAtAlpha.nasaBlackwell(alphaArray.getAsAmount(i));

				cL[i] = LiftCalc.calcCLatAlphaLinearDLR(alphaArray.get(i), ar);
				_cLvsAlphaCurve.put(alphaArray.get(i), cL[i]);
			}

			_cLMap.put(MethodEnum.DLR_NITA_SCHOLZ, new MyArray(cL));
			_curveMap.put(MethodEnum.DLR_NITA_SCHOLZ, _cLvsAlphaCurve);
		}



		public double[] nasaBlackwell() {

			double[] cL = new double[_numberOfAlpha];

			for (int i=0; i< _numberOfAlpha; i++) {
				calculateCLAtAlpha.nasaBlackwell(alphaArray.getAsAmount(i));

				cL[i] = cLMap.getcXVsAlphaTable().get(MethodEnum.NASA_BLACKWELL, alphaArray.getAsAmount(i));
				_cLvsAlphaCurve.put(alphaArray.get(i), cL[i]);
			}

			_cLMap.put(MethodEnum.NASA_BLACKWELL, new MyArray(cL));
			_curveMap.put(MethodEnum.NASA_BLACKWELL, _cLvsAlphaCurve);

			//			ADOPT_GUI.getLOG().info("\nAlpha " + Arrays.toString(alpha.toArray()) 
			//					+ "\nCLvsAlpha curve: " + Arrays.toString(cL) + "\n");

			return cL;
		}
		
		/** 
		 * Evaluate CL vs alpha array using alpha wing as input
		 * 
		 * @author Manuela Ruocco
		 */

		public double[] nasaBlackwellCompleteCurve(
				Amount<Angle> alphaMin, Amount<Angle> alphaMax, int nValue, boolean printResults ){

			//double [] cLActualArray = new double[nValue];
			alphaArrayActual =new MyArray();
			cLActualArray = new double[nValue];
			
			if (alphaMin.getUnit() == NonSI.DEGREE_ANGLE){
				alphaMin = alphaMin.to(SI.RADIAN);
			}
			
			if (alphaMax.getUnit() == NonSI.DEGREE_ANGLE){
				alphaMax = alphaMax.to(SI.RADIAN);
			}
			
			alphaArrayActual.linspace(alphaMin.getEstimatedValue(), alphaMax.getEstimatedValue(), nValue);
			cLActualArray = LiftCalc.calculateCLvsAlphaArrayNasaBlackwell(
					getTheLiftingSurface(), alphaArrayActual, nValue, printResults);
			return cLActualArray;
		}
			
			public double[] nasaBlackwellCompleteCurveArray(MyArray alphaArrayActual, boolean printResults ){

				int nValue =alphaArrayActual.size();
				cLActualArray = new double[nValue];
				
	
				cLActualArray = LiftCalc.calculateCLvsAlphaArrayNasaBlackwell(
						getTheLiftingSurface(), alphaArrayActual, nValue, printResults);
				return cLActualArray;
			}
			
//			double [] cLActualArray = new double[nValue];
//			CalcCLAtAlpha theClatAlphaCalculator = new CalcCLAtAlpha();
//			double cLStar, cLTemp, qValue, a ,b ,c ,d;
//			Amount<Angle> alphaTemp = Amount.valueOf(0.0, SI.RADIAN);
//			MyAirfoil meanAirfoil = new MeanAirfoil().calculateMeanAirfoil(getTheLiftingSurface());
//			double alphaStar = meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue();
//			Amount<Angle> alphaStarAmount = Amount.valueOf(alphaStar, SI.RADIAN);
//			double alphaActual = 0;
//			
//			for (int i=0; i<nValue; i++ ){
//			alphaActual = alphaArray.get(i);
//			
//			cLStarWing = theClatAlphaCalculator.nasaBlackwell(alphaStarAmount);
//			cLTemp = theClatAlphaCalculator.nasaBlackwell(alphaTemp);
//			if (alphaActual < alphaStar){    //linear trait
//				cLLinearSlope = (cLStarWing - cLTemp)/alphaStar;
//				//System.out.println("CL Linear Slope [1/rad] = " + cLLinearSlope);
//				qValue = cLStarWing - cLLinearSlope*alphaStar;
//				cLAlphaZero = qValue;
//				alphaZeroLiftWingClean = -qValue/cLLinearSlope;
//				cLActualArray[i] = cLLinearSlope * alphaActual+ qValue;
//				//System.out.println(" CL Actual = " + cLActual );
//			}
//
//			else {  // non linear trait
//
//				calcAlphaAndCLMax(meanAirfoil);
//				double cLMax = get_cLMaxClean();
//				alphaMax = get_alphaMaxClean();	
//				double alphaMaxDouble = alphaMax.getEstimatedValue();
//
//				cLLinearSlope = (cLStarWing - cLTemp)/alphaStar;
//				//System.out.println("CL Linear Slope [1/rad] = " + cLLinearSlope);
//				double[][] matrixData = { {Math.pow(alphaMaxDouble, 3), Math.pow(alphaMaxDouble, 2), alphaMaxDouble,1.0},
//						{3* Math.pow(alphaMaxDouble, 2), 2*alphaMaxDouble, 1.0, 0.0},
//						{3* Math.pow(alphaStar, 2), 2*alphaStar, 1.0, 0.0},
//						{Math.pow(alphaStar, 3), Math.pow(alphaStar, 2),alphaStar,1.0}};
//				RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
//				double [] vector = {cLMax, 0,cLLinearSlope, cLStarWing};
//
//				double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);
//
//				a = solSystem[0];
//				b = solSystem[1];
//				c = solSystem[2];
//				d = solSystem[3];
//
//				cLActualArray[i] = a * Math.pow(alphaActual, 3) + 
//						b * Math.pow(alphaActual, 2) + 
//						c * alphaActual + d;
//			}
//
//			}


		/** 
		 * Evaluate linear CL vs alpha curve of the lifting surface
		 * 
		 * @author Lorenzo Attanasio
		 * @return
		 */
		public Map<Double, Double> linearAndersonCompressibleSubsonic() {

			for (int i = 0; i < _numberOfAlpha; i++) {
				cLArray.set(i, calculateCLAtAlpha
						.linearAndersonCompressibleSubsonic(alphaArray.getAsAmount(i)));

				_cLvsAlphaCurve.put(alphaArray.get(i), cLArray.get(i));
			}

			_cLMap.put(MethodEnum.LINEAR, cLArray);
			_curveMap.put(MethodEnum.LINEAR, _cLvsAlphaCurve);

			return _cLvsAlphaCurve;
		}

		@Override
		public void allMethods() {
			linearAndersonCompressibleSubsonic();
			nasaBlackwell();
		}

		public Map<MethodEnum, Map<Double, Double>> get_curveMap() {
			return _curveMap;
		}

		public Map<MethodEnum, MyArray> get_cLMap() {
			return _cLMap;
		}

	}

	public class CalcCLMaxClean extends InnerCalculator {

//		private MyArray clAirfoils = new MyArray(getTheLiftingSurface()._clMaxSweep_y)
//				.interpolate(getTheLiftingSurface()._yStationsAirfoil.toArray(), _yStations);

		private MyArray clAirfoils = new MyArray(getTheLiftingSurface()._clMaxVsY)
				.interpolate(getTheLiftingSurface()._yStationsAirfoil.toArray(), _yStations);
		
		public MyArray getClAirfoils() {
			return clAirfoils;
		}

		private boolean found = false;
		private Amount<Angle> alphaAtCLMax;

		public double phillipsAndAlley() {
			double result;
			EngineTypeEnum engineType;
			if (theAircraft != null) {
				engineType = theAircraft.get_powerPlant().get_engineType();
			} else {
				engineType = EngineTypeEnum.TURBOPROP;
			}
//			result = LiftCalc.calculateCLmaxPhillipsAndAlley(
//					getTheLiftingSurface().get_clMaxVsY().getMean() , calculateCLAlpha.andersonSweptCompressibleSubsonic(), 
//					taperRatioEq, getTheLiftingSurface().get_sweepLEEquivalent().getEstimatedValue(), 
//					ar, getTheLiftingSurface().get_twistTip().getEstimatedValue(),
//					engineType
//					);
			
			result = LiftCalc.calculateCLmaxPhillipsAndAlley(
					getTheLiftingSurface().get_clMaxVsY().getMean() , 5.25, 
					taperRatioEq, getTheLiftingSurface().get_sweepLEEquivalent().getEstimatedValue(), 
					ar, getTheLiftingSurface().get_twistTip().getEstimatedValue(),
					engineType
					);

			cLMap.getcXMaxMap().put(MethodEnum.PHILLIPS_ALLEY, result);
			return result;
		}




		public double phillipsAndAlley(EngineTypeEnum engineType) {
			double result = LiftCalc.calculateCLmaxPhillipsAndAlley( //5.07
					getTheLiftingSurface().get_clMaxVsY().getMean() , calculateCLAlpha.andersonSweptCompressibleSubsonic(), 
					taperRatioEq, getTheLiftingSurface().get_sweepLEEquivalent().getEstimatedValue(), 
					ar, getTheLiftingSurface().get_twistTip().getEstimatedValue(),
					engineType
					);

			cLMap.getcXMaxMap().put(MethodEnum.PHILLIPS_ALLEY, result);
			return result;
		}

		/**
		 * Use NASA-Blackwell method for estimating the
		 * lifting surface CLmax
		 * 
		 * @author Lorenzo Attanasio ft Manuela Ruocco
		 */




		public void nasaBlackwell() {

			// System.out.println("\n\n\t\t\tCalcCLMaxClean : nasaBlackwell\n\n");


			int stepsToStallCounter = 0;
			double accuracy =0.0001;
			double diffCL = 0;
			double diffCLappOld = 0;
			double diffCLapp = 0;
			boolean _findStall = false;
			Amount<javax.measure.quantity.Angle> alphaNewAmount;





			// TODO: try to use nasa Blackwell also for vtail
			if (getTheLiftingSurface()._type != ComponentEnum.VERTICAL_TAIL) {
				if (calculateLiftDistribution.getNasaBlackwell() != null) {
					for (int j=0; j < _numberOfAlpha; j++) {
						if (found == false) {
							for(int i =0; i< _nPointsSemispanWise; i++) {
								if (found == false 
										&& cLMap.getCxyVsAlphaTable()
										.get( MethodEnum.NASA_BLACKWELL,
												alphaArray.getAsAmount(j)).get(i) 
										> clAirfoils.get(i) ) {

//									System.out.println( "distribution " +  cLMap.getCxyVsAlphaTable()
//																		.get( MethodEnum.NASA_BLACKWELL,
//																				alphaArray.getAsAmount(j))  );

									//@author Manuela ruocco
									// After find the first point where CL_wing > Cl_MAX_airfoil, starts an iteration on alpha
									// in order to improve the accuracy.

									for (int k =i; k< _nPointsSemispanWise; k++) {
										diffCLapp = ( cLMap.getCxyVsAlphaTable()
												.get( MethodEnum.NASA_BLACKWELL,
														alphaArray.getAsAmount(j)).get(k) -  clAirfoils.get(k));
										diffCL = Math.max(diffCLapp, diffCLappOld);
										diffCLappOld = diffCL;
									}
									if( Math.abs(diffCL) < accuracy){
										cLMap.getcXMaxMap().put(MethodEnum.NASA_BLACKWELL, (
												cLMap.getcXVsAlphaTable()
												.get(MethodEnum.NASA_BLACKWELL, alphaArray.getAsAmount(j))));
										found = true;
										alphaAtCLMax = alphaArray.getAsAmount(j); 
									}

									else{
										deltaAlpha = alphaArray.getAsAmount(j).getEstimatedValue()
												- alphaArray.getAsAmount(j-1).getEstimatedValue();
										alphaNew = alphaArray.getAsAmount(j).getEstimatedValue() - (deltaAlpha/2);
										double alphaOld = alphaArray.getAsAmount(j).getEstimatedValue(); 
										alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
										diffCLappOld = 0;
										while ( diffCL > accuracy){
											calculateCLAtAlpha.nasaBlackwell(alphaNewAmount);
											cLMap.getCxyVsAlphaTable().put(MethodEnum.NASA_BLACKWELL,
													alphaNewAmount, calculateLiftDistribution.getNasaBlackwell()
													.get_clTotalDistribution());
											diffCL = 0;

											for (int m =0; m< _nPointsSemispanWise; m++) {
												diffCLapp = ( cLMap.getCxyVsAlphaTable()
														.get( MethodEnum.NASA_BLACKWELL,
																alphaNewAmount).get(m) -  clAirfoils.get(m));

												if ( diffCLapp > 0 ){
													diffCL = Math.max(diffCLapp,diffCLappOld);
													diffCLappOld = diffCL;
												}

											}
											deltaAlpha = Math.abs(alphaOld - alphaNew);
											alphaOld = alphaNew;
											if (diffCL == 0 ){
												alphaNew = alphaOld + (deltaAlpha/2);
												alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
												diffCL = 1;
												diffCLappOld = 0;
											}
											else { 
												if(deltaAlpha > 0.005){
													alphaNew = alphaOld - (deltaAlpha/2);	
													alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
													diffCLappOld = 0;
													if ( diffCL < accuracy) break;
												}
												else {
													alphaNew = alphaOld - (deltaAlpha);	
													alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
													diffCLappOld = 0;
													if ( diffCL < accuracy) break;}}

										}
										alphaAtCLMax = Amount.valueOf(alphaNew, SI.RADIAN);
										cLMap.getcXMaxMap().put(MethodEnum.NASA_BLACKWELL, (
												cLMap.getcXVsAlphaTable()
												.get(MethodEnum.NASA_BLACKWELL, alphaAtCLMax)))	;
										found = true;


									}
									set_alphaStall(alphaAtCLMax);
								}
							}
						}

					}

					alphaAtCLMax = alphaEnd.copy(); 

					while (found == false && stepsToStallCounter < 15) {
						double alphaOld = alphaAtCLMax.getEstimatedValue();
						alphaAtCLMax = alphaAtCLMax.plus(Amount.valueOf(toRadians(0.3), SI.RADIAN)).copy();
						calculateCLAtAlpha.nasaBlackwell(alphaAtCLMax);
						stepsToStallCounter++;

						for(int i =0; i < _nPointsSemispanWise; i++) {
							if (cLMap.getCxyVsAlphaTable()
									.get(MethodEnum.NASA_BLACKWELL, alphaAtCLMax)
									.get(i) 
									> clAirfoils.get(i) ) {

								diffCLappOld = 0;
								for (int k =i; k< _nPointsSemispanWise; k++) {
									diffCLapp = (cLMap.getCxyVsAlphaTable()
											.get( MethodEnum.NASA_BLACKWELL,
													alphaAtCLMax).get(k) -  clAirfoils.get(k));
									diffCL = Math.max(diffCLapp, diffCLappOld);
									diffCLappOld = diffCL;
								}
								if( Math.abs(diffCL) < accuracy){
									cLMap.getcXMaxMap().put(MethodEnum.NASA_BLACKWELL, (
											cLMap.getcXVsAlphaTable()
											.get(MethodEnum.NASA_BLACKWELL, alphaAtCLMax)));
									found = true;
								}
								else{
									deltaAlpha = alphaAtCLMax.getEstimatedValue() - alphaAtCLMax.minus(Amount.valueOf(toRadians(0.3), SI.RADIAN)).copy().getEstimatedValue();
									alphaNew = alphaAtCLMax.getEstimatedValue() - (deltaAlpha/2);
									alphaNewAmount = Amount.valueOf(toRadians(alphaNew), SI.RADIAN);
									alphaOld = alphaAtCLMax.getEstimatedValue(); 
									alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
									diffCLappOld = 0;
									while ( diffCL > accuracy){
										calculateCLAtAlpha.nasaBlackwell(alphaNewAmount);
										cLMap.getCxyVsAlphaTable().put(MethodEnum.NASA_BLACKWELL,
												alphaNewAmount, calculateLiftDistribution.getNasaBlackwell()
												.get_clTotalDistribution());
										diffCL = 0;

										for (int m =0; m< _nPointsSemispanWise; m++) {

											diffCLapp = ( cLMap.getCxyVsAlphaTable()
													.get( MethodEnum.NASA_BLACKWELL,
															alphaNewAmount).get(m) -  clAirfoils.get(m));

											if ( diffCLapp > 0 ){
												diffCL = Math.max(diffCLapp,diffCLappOld);
												diffCLappOld = diffCL;
											}

										}
										deltaAlpha = Math.abs(alphaOld - alphaNew);
										alphaOld = alphaNew;
										if (diffCL == 0 ){
											alphaNew = alphaOld + (deltaAlpha/2);
											alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
											diffCL = 1;
											diffCLappOld = 0;
										}
										else { 
											if(deltaAlpha > 0.005){
												alphaNew = alphaOld - (deltaAlpha/2);	
												alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
												diffCLappOld = 0;
												if ( diffCL < accuracy) {
													_findStall =true;
													break;
												}
											}
											else {
												alphaNew = alphaOld - (deltaAlpha);	
												alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
												diffCLappOld = 0;
												if ( diffCL < accuracy) {
													_findStall = true;
													break;
												}}
										}

									}
									alphaAtCLMax = Amount.valueOf(alphaNew, SI.RADIAN);
									cLMap.getcXMaxMap().put(MethodEnum.NASA_BLACKWELL, (
											cLMap.getcXVsAlphaTable()
											.get(MethodEnum.NASA_BLACKWELL, alphaAtCLMax)))	;


								}
							}
							if ( _findStall == true ) break;
						}

						set_alphaStall(alphaAtCLMax);

					}
				}
			}
		}

		public Amount<Angle> getAlphaAtCLMax() {
			return alphaAtCLMax;
		}




		public void setAlphaAtCLMax(Amount<Angle> alphaAtCLMax) {
			this.alphaAtCLMax = alphaAtCLMax;
		}




		public void schrenk() {

		}

		public void allMethods() {
			nasaBlackwell();
			phillipsAndAlley();
			schrenk();
		}

	}

	/**
	 * This function calls two functions of LSAerodynamicManager class that populate the alpha max and cl max.
	 * The value of CL max clean is obtained using Nasa Blackwell method. The NasaBlackwell method evaluates the inviscid 
	 * alpha max. Whit this function a correction in alpha max is introduced.
	 * 
	 * To obtain these values:
	 * CL Max --> object_LSAerodynamicManager.get_cLMaxClean
	 * Alpha Max --> object_LSAerodynamicManager.get_alphaMaxClean
	 * 
	 * 
	 * @author Manuela Ruocco
	 * @param LSAerodynamicsManager
	 * @param Amount<Angle> alphaAtCLMax
	 */  



	public void calcAlphaAndCLMax(MyAirfoil meanAirfoil){
		Amount<Angle> deltaAlphaMax;
		double meanLESharpnessParameter = meanAirfoil.getGeometry().get_deltaYPercent();

		CalcCLvsAlphaCurve theCLAnalysis = new CalcCLvsAlphaCurve();
		CalcCLAtAlpha theCLatAlpha = new CalcCLAtAlpha();
		CalcCLMaxClean theCLmaxAnalysis = new CalcCLMaxClean();

		theCLAnalysis.nasaBlackwell();
		theCLmaxAnalysis.nasaBlackwell();

		Amount<Angle> alphaMaxNasaBlackwell = this.get_alphaStall();
		
//		System.out.println("Alpha max NASA Blackwell : " + alphaMaxNasaBlackwell.to(NonSI.DEGREE_ANGLE));
		
		this.set_cLMaxClean(theCLatAlpha.nasaBlackwell(alphaMaxNasaBlackwell));
		//System.out.println("CL Max " + get_cLMaxClean());
		deltaAlphaMax = Amount.valueOf(
				toRadians (this.get_AerodynamicDatabaseReader().getD_Alpha_Vs_LambdaLE_VsDy(
						getTheLiftingSurface().get_sweepLEEquivalent().to(NonSI.DEGREE_ANGLE).getEstimatedValue() ,
						meanLESharpnessParameter )), SI.RADIAN);
//				System.out.println("Sweep LE Equivalent = " + getTheLiftingSurface().get_sweepLEEquivalent().getEstimatedValue());
//				System.out.println("Delta  alpha max " + deltaAlphaMax.to(NonSI.DEGREE_ANGLE));
		Amount<Angle> alphaMax =  Amount.valueOf((alphaMaxNasaBlackwell.getEstimatedValue() + deltaAlphaMax.getEstimatedValue()), SI.RADIAN);
//				System.out.println( "Alpha max " + alphaMax.to(NonSI.DEGREE_ANGLE) );

		this.set_alphaMaxClean(alphaMax);
	}

	/**
	 * This class calculate high lift devices effects upon a wing in terms of CL and CD. To do
	 * this, the calculation starts, at first, from the airfoil by evaluating DeltaCl0 and DClmax
	 * for flaps and slats; from them, wing high lift devices effects are evaluated by calculating
	 * DeltaCL0, DeltaCLmax for flaps and slats and the new CLalpha.  
	 * Last but not least is the drag coefficient variation due to high lift devices which calculation
	 * is made upon a semi-empirical formula.
	 * 
	 * @author Vittorio Trifari
	 *
	 */
	public class CalcHighLiftDevices {
		//-------------------------------------------------------------------------------------
		// VARIABLE DECLARATION:

		private LiftingSurface theWing;
		private OperatingConditions theConditions;
		private MyAirfoil meanAirfoil;
		private List<Double[]> deltaFlap; 	    
		private List<Double> flapTypeIndex, deltaSlat, etaInFlap, etaOutFlap, 
		etaInSlat, etaOutSlat, cfc, csc, leRadiusSlatRatio, cExtcSlat;
		private final List<Double> deltaFlapRef;
		private List<FlapTypeEnum> flapType; 

		//to evaluate:
		private double deltaCl0Flap = 0,
				deltaCL0Flap = 0,
				deltaClmaxFlap = 0,
				deltaCLmaxFlap = 0,
				deltaClmaxSlat = 0,
				deltaCLmaxSlat = 0,
				cLalphaNew = 0,
				deltaAlphaMaxFlap = 0,
				deltaCD = 0,
				deltaCMC4 = 0,
				cLMaxFlap;

		private ArrayList<Double> deltaCl0FlapList,
		deltaCL0FlapList,
		deltaClmaxFlapList,
		deltaCLmaxFlapList,
		deltaClmaxSlatList,
		deltaCLmaxSlatList,
		cLalphaNewList,
		deltaCDList,
		deltaCMC4List;
		private String subfolderPathHL;


		//-------------------------------------------------------------------------------------
		// BUILDER:

		public CalcHighLiftDevices(
				LiftingSurface theWing,
				OperatingConditions theConditions,
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

			this.theWing = theWing;
			this.theConditions = theConditions;
			this.meanAirfoil = theWing
					.getAerodynamics()
					.new MeanAirfoil()
					.calculateMeanAirfoil(
							theWing
							);
			this.deltaFlap = deltaFlap;
			this.flapType = flapType;
			this.deltaSlat = deltaSlat;
			this.etaInFlap = etaInFlap;
			this.etaOutFlap = etaOutFlap;
			this.etaInSlat = etaInSlat;
			this.etaOutSlat = etaOutSlat;
			this.cfc = cfc;
			this.csc = csc;
			this.leRadiusSlatRatio = leRadiusSlatRatio;
			this.cExtcSlat = cExtcSlat;

			flapTypeIndex = new ArrayList<Double>();
			deltaFlapRef = new ArrayList<Double>();

			for(int i=0; i<flapType.size(); i++) {
				if(flapType.get(i) == FlapTypeEnum.SINGLE_SLOTTED) {
					flapTypeIndex.add(1.0);
					deltaFlapRef.add(45.0);
				}
				else if(flapType.get(i) == FlapTypeEnum.DOUBLE_SLOTTED) {
					flapTypeIndex.add(2.0);
					deltaFlapRef.add(50.0);
				}
				else if(flapType.get(i) == FlapTypeEnum.PLAIN) {
					flapTypeIndex.add(3.0);
					deltaFlapRef.add(60.0);
				}
				else if(flapType.get(i) == FlapTypeEnum.FOWLER) {
					flapTypeIndex.add(4.0);
					deltaFlapRef.add(40.0);
				}
				else if(flapType.get(i) == FlapTypeEnum.TRIPLE_SLOTTED) {
					flapTypeIndex.add(5.0);
					deltaFlapRef.add(50.0);
				}
			}

			deltaAlphaMaxFlap = get_AerodynamicDatabaseReader().getD_Alpha_Vs_LambdaLE_VsDy(
					getTheLiftingSurface()
					.get_sweepLEEquivalent().to(NonSI.DEGREE_ANGLE).getEstimatedValue(),
					meanAirfoil.getGeometry().get_deltaYPercent());
			
			theLiftingSurface.setHigLiftCalculator(this);
			calcAlphaAndCLMax(meanAirfoil);
		}

		//-------------------------------------------------------------------------------------
		// METHODS:

		/**
		 * This method calculate high lift devices effects on lift coefficient curve of the 
		 * airfoil and wing throughout semi-empirical formulas; in particular DeltaCl0, DeltaCL0
		 * DeltaCLmax and DeltaClmax are calculated for flaps when only DeltaClmax and DeltaCLmax
		 * are calculated for slats. Moreover an evaluation of new CLapha slope and CD are performed
		 * for the wing. 
		 * 
		 * @author Vittorio Trifari
		 */
		public void calculateHighLiftDevicesEffects() {

			//--------------------------------------------
			// initialization of flap type map

			//---------------------------------------------
			// deltaCl0 (flap)
			List<Double> thetaF = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++) 
				thetaF.add(Math.acos((2*cfc.get(i))-1));

			List<Double> alphaDelta = new ArrayList<Double>();
			for(int i=0; i<thetaF.size(); i++)
				alphaDelta.add(1-((thetaF.get(i)-Math.sin(thetaF.get(i)))/Math.PI));

			Double[] deltaFlapTotal = new Double[flapTypeIndex.size()];
			for(int i=0; i<deltaFlap.size(); i++) {
				deltaFlapTotal[i] = 0.0;
				for(int j=0; j<deltaFlap.get(i).length; j++) {
					deltaFlapTotal[i] += deltaFlap.get(i)[j];
				}
			}

			List<Double> etaDeltaFlap = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++) {
				if(flapTypeIndex.get(i) == 3.0)
					etaDeltaFlap.add(
							theWing
							.getAerodynamics()
							.getHighLiftDatabaseReader()
							.getEtaDeltaVsDeltaFlapPlain(deltaFlapTotal[i], cfc.get(i)));
				else
					etaDeltaFlap.add(
							theWing
							.getAerodynamics()
							.getHighLiftDatabaseReader()
							.getEtaDeltaVsDeltaFlap(deltaFlapTotal[i], flapTypeIndex.get(i))
							);
			}

			List<Double> deltaCl0First = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				deltaCl0First.add(
						alphaDelta.get(i).doubleValue()
						*etaDeltaFlap.get(i).doubleValue()
						*deltaFlapTotal[i]
								*meanAirfoil.getAerodynamics().get_clAlpha()*(Math.PI/180)
						);

			List<Double> deltaCCfFlap = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				deltaCCfFlap.add(
						theWing
						.getAerodynamics()
						.getHighLiftDatabaseReader()
						.getDeltaCCfVsDeltaFlap(deltaFlapTotal[i],flapTypeIndex.get(i))
						);

			List<Double> cFirstCFlap = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				cFirstCFlap.add(1+(deltaCCfFlap.get(i).doubleValue()*cfc.get(i).doubleValue()));

			deltaCl0FlapList = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				deltaCl0FlapList.add(
						(deltaCl0First.get(i).doubleValue()*cFirstCFlap.get(i).doubleValue())
						+(meanAirfoil.getAerodynamics().calculateClAtAlpha(0.0)*(cFirstCFlap.get(i).doubleValue()-1))
						);

			for(int i=0; i<flapTypeIndex.size(); i++)
				deltaCl0Flap += deltaCl0FlapList.get(i).doubleValue();

			//---------------------------------------------------------------
			// deltaClmax (flap)
			List<Double> deltaClmaxBase = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				deltaClmaxBase.add(
						theWing
						.getAerodynamics()
						.getHighLiftDatabaseReader()
						.getDeltaCLmaxBaseVsTc(
								meanAirfoil.getGeometry().get_maximumThicknessOverChord(),
								flapTypeIndex.get(i)
								)
						);

			List<Double> k1 = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				k1.add(theWing
						.getAerodynamics()
						.getHighLiftDatabaseReader()
						.getK1vsFlapChordRatio(cfc.get(i), flapTypeIndex.get(i))
						);


			List<Double> k2 = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				k2.add(theWing
						.getAerodynamics()
						.getHighLiftDatabaseReader()
						.getK2VsDeltaFlap(deltaFlapTotal[i], flapTypeIndex.get(i))
						);

			List<Double> k3 = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				k3.add(theWing
						.getAerodynamics()
						.getHighLiftDatabaseReader()
						.getK3VsDfDfRef(
								deltaFlapTotal[i],
								deltaFlapRef.get(i),
								flapTypeIndex.get(i)
								)
						);

			deltaClmaxFlapList = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				deltaClmaxFlapList.add(k1.get(i).doubleValue()
						*k2.get(i).doubleValue()
						*k3.get(i).doubleValue()
						*deltaClmaxBase.get(i).doubleValue()
						);

			for(int i=0; i<flapTypeIndex.size(); i++)
				deltaClmaxFlap += deltaClmaxFlapList.get(i).doubleValue();

			//---------------------------------------------------------------
			// deltaClmax (slat)
			if(deltaSlat != null) {

				List<Double> dCldDelta = new ArrayList<Double>();
				for(int i=0; i<deltaSlat.size(); i++)
					dCldDelta.add(theWing
							.getAerodynamics()
							.getHighLiftDatabaseReader()
							.getDCldDeltaVsCsC(csc.get(i))
							);

				List<Double> etaMaxSlat = new ArrayList<Double>();
				for(int i=0; i<deltaSlat.size(); i++)
					etaMaxSlat.add(theWing
							.getAerodynamics()
							.getHighLiftDatabaseReader()
							.getEtaMaxVsLEradiusTicknessRatio(
									leRadiusSlatRatio.get(i),
									meanAirfoil.getGeometry().get_maximumThicknessOverChord())
							);

				List<Double> etaDeltaSlat = new ArrayList<Double>();
				for(int i=0; i<deltaSlat.size(); i++)
					etaDeltaSlat.add(
							theWing
							.getAerodynamics()
							.getHighLiftDatabaseReader()
							.getEtaDeltaVsDeltaSlat(deltaSlat.get(i))
							);

				deltaClmaxSlatList = new ArrayList<Double>();
				for(int i=0; i<deltaSlat.size(); i++)
					deltaClmaxSlatList.add(
							dCldDelta.get(i).doubleValue()
							*etaMaxSlat.get(i).doubleValue()
							*etaDeltaSlat.get(i).doubleValue()
							*deltaSlat.get(i).doubleValue()
							*cExtcSlat.get(i).doubleValue()
							);

				for(int i=0; i<deltaSlat.size(); i++)
					deltaClmaxSlat += deltaClmaxSlatList.get(i).doubleValue();

			}

			//---------------------------------------------------------------
			// deltaCL0 (flap)
			List<Double> kc = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				kc.add(theWing
						.getAerodynamics()
						.getHighLiftDatabaseReader()
						.getKcVsAR(
								theWing.get_aspectRatio(),
								alphaDelta.get(i))	
						);

			List<Double> kb = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				kb.add(theWing
						.getAerodynamics()
						.getHighLiftDatabaseReader()
						.getKbVsFlapSpanRatio(etaInFlap.get(i), etaOutFlap.get(i))	
						);

			CalcCLAlpha calcLinearSlope = new CalcCLAlpha();
			double cLLinearSlope = calcLinearSlope.nasaBlackwell(new CalcCLAtAlpha());
			
			deltaCL0FlapList = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				deltaCL0FlapList.add(
						kb.get(i).doubleValue()
						*kc.get(i).doubleValue()
						*deltaCl0FlapList.get(i).doubleValue()
						*((cLLinearSlope)/meanAirfoil.getAerodynamics().get_clAlpha())
						);

			for(int i=0; i<flapTypeIndex.size(); i++)
				deltaCL0Flap += deltaCL0FlapList.get(i).doubleValue();

			//---------------------------------------------------------------
			// deltaCLmax (flap)
			List<Double> flapSurface = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				flapSurface.add(
						Math.abs(
								theWing.get_span().getEstimatedValue()							
								/2*theWing.get_chordRootEquivalentWing().getEstimatedValue()
								*(2-((1-theWing.get_taperRatioEquivalent())*(etaInFlap.get(i)-etaOutFlap.get(i))))
								*(etaInFlap.get(i)-etaOutFlap.get(i))
								)
						);

			List<Double> kLambdaFlap = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				kLambdaFlap.add(
						Math.pow(Math.cos(theWing.get_sweepQuarterChordEq().getEstimatedValue()),0.75)
						*(1-(0.08*Math.pow(Math.cos(theWing.get_sweepQuarterChordEq().getEstimatedValue()), 2)))
						);

			deltaCLmaxFlapList = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				deltaCLmaxFlapList.add(deltaClmaxFlapList.get(i)
						*(flapSurface.get(i)/theWing.get_surface().getEstimatedValue())
						*kLambdaFlap.get(i)
						);

			for(int i=0; i<flapTypeIndex.size(); i++)
				deltaCLmaxFlap += deltaCLmaxFlapList.get(i).doubleValue();

			//---------------------------------------------------------------
			// deltaCLmax (slat)
			if(deltaSlat != null) {

				List<Double> kLambdaSlat = new ArrayList<Double>();
				for(int i=0; i<deltaSlat.size(); i++)
					kLambdaSlat.add(
							Math.pow(Math.cos(theWing.get_sweepQuarterChordEq().getEstimatedValue()),0.75)
							*(1-(0.08*Math.pow(Math.cos(theWing.get_sweepQuarterChordEq().getEstimatedValue()), 2)))
							);

				List<Double> slatSurface = new ArrayList<Double>();
				for(int i=0; i<deltaSlat.size(); i++)
					slatSurface.add(
							Math.abs(theWing.get_span().getEstimatedValue()
									/2*theWing.get_chordRootEquivalentWing().getEstimatedValue()
									*(2-(1-theWing.get_taperRatioEquivalent())*(etaInSlat.get(i)-etaOutSlat.get(i)))
									*(etaInSlat.get(i)-etaOutSlat.get(i))
									)
							);

				deltaCLmaxSlatList = new ArrayList<Double>();
				for(int i=0; i<deltaSlat.size(); i++)
					deltaCLmaxSlatList.add(deltaClmaxSlatList.get(i)
							*(slatSurface.get(i)/theWing.get_surface().getEstimatedValue())
							*kLambdaSlat.get(i));

				for(int i=0; i<deltaSlat.size(); i++)
					deltaCLmaxSlat += deltaCLmaxSlatList.get(i).doubleValue();

			}
			//---------------------------------------------------------------
			// new CLalpha
			
			cLalphaNewList = new ArrayList<Double>();
			List<Double> swf = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++) {
				cLalphaNewList.add(
						cLLinearSlope*(Math.PI/180)
						*(1+((deltaCL0FlapList.get(i)/deltaCl0FlapList.get(i))
								*(cFirstCFlap.get(i)*(1-((cfc.get(i))*(1/cFirstCFlap.get(i))
										*Math.pow(Math.sin(deltaFlapTotal[i]*Math.PI/180), 2)))-1))));
				swf.add(flapSurface.get(i)/theWing.get_surface().getEstimatedValue());
			}

			double swfTot = 0;
			for(int i=0; i<swf.size(); i++)
				swfTot += swf.get(i);

			for(int i=0; i<flapTypeIndex.size(); i++)
				cLalphaNew += cLalphaNewList.get(i)*swf.get(i);

			cLalphaNew /= swfTot;

			//---------------------------------------------------------------
			// deltaCD
			List<Double> delta1 = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++) {
				if(flapTypeIndex.get(i) == 3.0)
					delta1.add(
							theWing
							.getAerodynamics()
							.getHighLiftDatabaseReader()
							.getDelta1VsCfCPlain(cfc.get(i), theWing.get_maxThicknessMean())
							);
				else
					delta1.add(
							theWing
							.getAerodynamics()
							.getHighLiftDatabaseReader()
							.getDelta1VsCfCSlotted(cfc.get(i), theWing.get_maxThicknessMean())
							);
			}

			List<Double> delta2 = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++) {
				if(flapTypeIndex.get(i) == 3.0)
					delta2.add(
							theWing
							.getAerodynamics()
							.getHighLiftDatabaseReader()
							.getDelta2VsDeltaFlapPlain(deltaFlapTotal[i])
							);
				else
					delta2.add(
							theWing
							.getAerodynamics()
							.getHighLiftDatabaseReader()
							.getDelta2VsDeltaFlapSlotted(deltaFlapTotal[i], theWing.get_maxThicknessMean())
							);
			}

			List<Double> delta3 = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++) {
				delta3.add(
						theWing
						.getAerodynamics()
						.getHighLiftDatabaseReader()
						.getDelta3VsBfB(etaInFlap.get(i), etaOutFlap.get(i), theWing.get_taperRatioEquivalent())
						);
			}

			deltaCDList = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++) {
				deltaCDList.add(delta1.get(i)*delta2.get(i)*delta3.get(i));
			}

			for(int i=0; i<flapTypeIndex.size(); i++)
				deltaCD += deltaCDList.get(i).doubleValue();	

			//---------------------------------------------------------------
			// deltaCM_c/4
			List<Double> mu1 = new ArrayList<Double>();
			for (int i=0; i<flapTypeIndex.size(); i++)
				if(flapTypeIndex.get(i) == 3.0)
					mu1.add(
							theWing
							.getAerodynamics()
							.getHighLiftDatabaseReader()
							.getMu1VsCfCFirstPlain(
									(cfc.get(i))*(1/cFirstCFlap.get(i)),
									deltaFlapTotal[i]
									)
							);
				else
					mu1.add(theWing
							.getAerodynamics()
							.getHighLiftDatabaseReader()
							.getMu1VsCfCFirstSlottedFowler((cfc.get(i))*(1/cFirstCFlap.get(i)))
							);

			List<Double> mu2 = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				mu2.add(theWing
						.getAerodynamics()
						.getHighLiftDatabaseReader()
						.getMu2VsBfB(
								etaInFlap.get(i),
								etaOutFlap.get(i),
								theWing.get_taperRatioEquivalent()
								)
						);

			List<Double> mu3 = new ArrayList<Double>();
			for(int i=0; i<flapTypeIndex.size(); i++)
				mu3.add(theWing
						.getAerodynamics()
						.getHighLiftDatabaseReader()
						.getMu3VsBfB(
								etaInFlap.get(i),
								etaOutFlap.get(i),
								theWing.get_taperRatioEquivalent()
								)
						);

			deltaCMC4List = new ArrayList<Double>();
			double cL = calcCLatAlphaHighLiftDevice(theConditions.get_alphaCurrent());
			for(int i=0; i<flapTypeIndex.size(); i++)
				deltaCMC4List.add(
						(mu2.get(i)*(-(mu1.get(i)*deltaClmaxFlapList.get(i)
								*cFirstCFlap.get(i))-(cFirstCFlap.get(i)
										*((cFirstCFlap.get(i))-1)
										*(cL + (deltaClmaxFlapList.get(i)
												*(1-(flapSurface.get(i)/theWing
														.get_surface()
														.getEstimatedValue()))))
										*(1/8)))) + (0.7*(theWing
												.get_aspectRatio()/(1+(theWing
														.get_aspectRatio()/2)))
												*mu3.get(i)*deltaClmaxFlapList.get(i)
												*Math.tan(theWing
														.get_sweepQuarterChordEq()
														.getEstimatedValue()))
						);

			for(int i=0; i<flapTypeIndex.size(); i++)
				deltaCMC4 += deltaCMC4List.get(i).doubleValue();	
		}


		//---------------------------------------------------------------

		/**
		 * This method calculates CL at alpha given as input for a wing with high lift devices.
		 * This method calculates both linear trait and non linear trait. 
		 * It use the NasaBlackwell method in order to evaluate the slope of the linear trait
		 * and it builds the non-linear trait using a cubic interpolation. 
		 * 
		 * * NOTE THAT AN HIGH LIFT DEVICES ANALYSIS OF THE AIRCRAFT IS REQUIRED!! --> calculateHighLiftDevicesEffects
		 * 
		 * @author Manuela Ruocco
		 *
		 */		
		
		
		
		public double[] calcCLvsAlphaHighLiftDevices(Amount<Angle> alphaMin, Amount<Angle> alphaMax, int nValue){

			//double [] cLActualArray = new double[nValue];
			alphaArrayActualHighLift =new MyArray();
			cLActualArray = new double[nValue];
			
			if (alphaMin.getUnit() == NonSI.DEGREE_ANGLE){
				alphaMin = alphaMin.to(SI.RADIAN);
			}
			
			if (alphaMax.getUnit() == NonSI.DEGREE_ANGLE){
				alphaMax = alphaMax.to(SI.RADIAN);
			} 
			
			alphaArrayActualHighLift.linspace(alphaMin.getEstimatedValue() , alphaMax.getEstimatedValue(), nValue);
			cLActualArrayHighLift = LiftCalc.calculateCLvsAlphaHighLiftArrayNasaBlackwell(
					getTheLiftingSurface(), 
					alphaArrayActualHighLift,
					nValue,
					cLalphaNew,
					deltaCL0Flap,
					deltaAlphaMaxFlap,
					cLMaxFlap,
					deltaClmaxSlat);
			
			return cLActualArrayHighLift;
		}
		
		public double[] calcCLvsAlphaBodyHighLiftDevices(Amount<Angle> alphaMin, Amount<Angle> alphaMax, int nValue){

			//double [] cLActualArray = new double[nValue];
			alphaArrayActualHighLift =new MyArray();
			cLActualArray = new double[nValue];
			
			if (alphaMin.getUnit() == NonSI.DEGREE_ANGLE){
				alphaMin = alphaMin.to(SI.RADIAN);
			}
			
			if (alphaMax.getUnit() == NonSI.DEGREE_ANGLE){
				alphaMax = alphaMax.to(SI.RADIAN);
			}
			
			alphaArrayActualHighLift.linspace(alphaMin.getEstimatedValue() + theLiftingSurface.get_iw().getEstimatedValue() ,
					alphaMax.getEstimatedValue() + theLiftingSurface.get_iw().getEstimatedValue(), nValue);
			cLActualArrayHighLift = LiftCalc.calculateCLvsAlphaHighLiftArrayNasaBlackwell(
					getTheLiftingSurface(), 
					alphaArrayActualHighLift,
					nValue,
					cLalphaNew,
					deltaCL0Flap,
					deltaAlphaMaxFlap,
					cLMaxFlap,
					deltaClmaxSlat);
			
			return cLActualArrayHighLift;
		}
		
		
		public double calcCLatAlphaHighLiftDevices(Amount<Angle> alpha){

			if (alpha.getUnit() == NonSI.DEGREE_ANGLE) 
				alpha = alpha.to(SI.RADIAN);
			
			double  [] clValue = getcLActualArrayHighLift();
			double [] alphaArray = getAlphaArrayActualHighLift();
			
			double cLAtAlpha = MyMathUtils.getInterpolatedValue1DLinear(alphaArray,clValue, alpha.getEstimatedValue());
			return cLAtAlpha;
		}
			
			
			public double calcCLatAlphaHighLiftDevice(Amount<Angle> alpha){

				if (alpha.getUnit() == NonSI.DEGREE_ANGLE) 
					alpha = alpha.to(SI.RADIAN);

			double cLAlphaFlap = cLalphaNew*57.3; // need it in 1/rad

			MyAirfoil meanAirfoil = new MeanAirfoil().calculateMeanAirfoil(getTheLiftingSurface());
			double alphaStarClean = meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue();

			Amount<Angle> alphaStarCleanAmount = Amount.valueOf(alphaStarClean, SI.RADIAN);

			CalcCLvsAlphaCurve theCLvsAlphaCurve = new CalcCLvsAlphaCurve();
			Amount<Angle> alphaMin = Amount.valueOf(Math.toRadians(-5), SI.RADIAN );
			Amount<Angle> alphaMaxim = Amount.valueOf(Math.toRadians(20), SI.RADIAN );
			//theCLvsAlphaCurve.nasaBlackwellCompleteCurve(alphaMin, alphaMaxim, 50, false); // new method
			CalcCLAtAlpha theCLCleanCalculator = new CalcCLAtAlpha();
			double cLStarClean = theCLCleanCalculator.nasaBlackwellCompleteCurveValue(alphaStarCleanAmount);

			double cL0Clean =  theCLCleanCalculator.nasaBlackwellCompleteCurveValue(Amount.valueOf(0.0, SI.RADIAN));
			double cL0HighLift = cL0Clean + deltaCL0Flap;
			double qValue = cL0HighLift;
			double alphaStar = (cLStarClean - qValue)/cLAlphaFlap;
			double cLMaxClean = get_cLMaxClean();
			Amount<Angle> alphaMax = get_alphaMaxClean().to(NonSI.DEGREE_ANGLE);	
			cLMaxFlap = cLMaxClean + deltaCLmaxFlap + deltaCLmaxSlat;
			
			double alphaMaxHighLift;

			alphaMaxHighLift = ((cLMaxFlap-cL0HighLift)/cLalphaNew) 
								+ get_AerodynamicDatabaseReader().getD_Alpha_Vs_LambdaLE_VsDy(
										getTheLiftingSurface()
										.get_sweepLEEquivalent().to(NonSI.DEGREE_ANGLE).getEstimatedValue(),
										meanAirfoil.getGeometry().get_deltaYPercent());
			
			alphaMaxHighLift = Amount.valueOf(toRadians(alphaMaxHighLift), SI.RADIAN).getEstimatedValue();

			double alphaStarFlap; 

			if(deltaSlat == null)
				alphaStarFlap = (alphaStar + alphaStarClean)/2;
			else
				alphaStarFlap = alphaMaxHighLift-(alphaMax.to(SI.RADIAN).getEstimatedValue()-alphaStarClean);

			double cLStarFlap = cLAlphaFlap * alphaStarFlap + qValue;	
			theWing.getAerodynamics().set_alphaStarHigLift(Amount.valueOf(alphaStarFlap, SI.RADIAN));

			if (alpha.getEstimatedValue() < alphaStarFlap ){ 
				double cLActual = cLAlphaFlap * alpha.getEstimatedValue() + qValue;	
				return cLActual;
			}
			else{
				double[][] matrixData = { {Math.pow(alphaMaxHighLift, 3), Math.pow(alphaMaxHighLift, 2)
					, alphaMaxHighLift,1.0},
						{3* Math.pow(alphaMaxHighLift, 2), 2*alphaMaxHighLift, 1.0, 0.0},
						{3* Math.pow(alphaStarFlap, 2), 2*alphaStarFlap, 1.0, 0.0},
						{Math.pow(alphaStarFlap, 3), Math.pow(alphaStarFlap, 2),alphaStarFlap,1.0}};
				RealMatrix m = MatrixUtils.createRealMatrix(matrixData);


				double [] vector = {cLMaxFlap, 0,cLAlphaFlap, cLStarFlap};

				double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

				double a = solSystem[0];
				double b = solSystem[1];
				double c = solSystem[2];
				double d = solSystem[3];

				double clActual = a * Math.pow(alpha.getEstimatedValue(), 3) + 
						b * Math.pow(alpha.getEstimatedValue(), 2) + 
						c * alpha.getEstimatedValue() + d;

				return clActual;
			}

		}

		/** 
		 * This function plot CL vs Alpha curve using 30 value of alpha between alpha=- 2 deg and
		 * alphaMax+2 for a wing with high lift devices.
		 * 
		 * @author Manuela Ruocco
		 * @throws IllegalAccessException 
		 * @throws InstantiationException 
		 */

		public void plotHighLiftCurve() throws InstantiationException, IllegalAccessException{ 


			String folderPathHL = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
			if(subfolderPathCheck){
				plotCLvsAlphaCurve();
			subfolderPathHL = JPADStaticWriteUtils.createNewFolder(folderPathHL + "CL_vs_Alpha_Highlift" + File.separator);
			}
			double cLAlphaFlap = cLalphaNew*57.3; // need it in 1/rad
			
			Amount<Angle> alphaActual;

			List<Double[]> cLListPlot = new ArrayList<>(); 
			List<Double[]> alphaListPlot = new ArrayList<>(); 

			double[] cLArrayClean = get_cLArrayPlot();
			double[] alphaArrayClean = get_alphaArrayPlot();

			double alphaFirst = -13.0;
			int nPoints = 50;
			MyAirfoil meanAirfoil = new MeanAirfoil().calculateMeanAirfoil(getTheLiftingSurface());
			double alphaStarClean = meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue();
			Amount<Angle> alphaStarCleanAmount = Amount.valueOf(alphaStarClean, SI.RADIAN);

			CalcCLAtAlpha theCLCleanCalculator = new CalcCLAtAlpha();

			double cLStarClean = theCLCleanCalculator.nasaBlackwellCompleteCurveValue(alphaStarCleanAmount);
			double cL0Clean =  theCLCleanCalculator.nasaBlackwellCompleteCurveValue(Amount.valueOf(0.0, SI.RADIAN));
			double cL0HighLift = cL0Clean + deltaCL0Flap;
			double qValue = cL0HighLift;
			double alphaStar = (cLStarClean - qValue)/cLAlphaFlap;
			
			double cLMaxClean = get_cLMaxClean();
			Amount<Angle> alphaMax = get_alphaMaxClean().to(NonSI.DEGREE_ANGLE);	
			cLMaxFlap = cLMaxClean + deltaCLmaxFlap + deltaCLmaxSlat;

			double alphaMaxHighLift;

			alphaMaxHighLift = ((cLMaxFlap-cL0HighLift)/cLalphaNew) 
								+ get_AerodynamicDatabaseReader().getD_Alpha_Vs_LambdaLE_VsDy(
										getTheLiftingSurface()
										.get_sweepLEEquivalent().to(NonSI.DEGREE_ANGLE).getEstimatedValue(),
										meanAirfoil.getGeometry().get_deltaYPercent());
			
			alphaMaxHighLift = Amount.valueOf(toRadians(alphaMaxHighLift), SI.RADIAN).getEstimatedValue();
			double alphaMaxHighLiftDegree =  
					Amount.valueOf(alphaMaxHighLift, SI.RADIAN)
					.to(NonSI.DEGREE_ANGLE)
					.getEstimatedValue();

			double alphaStarFlap; 

			if(deltaSlat == null)
				alphaStarFlap = (alphaStar + alphaStarClean)/2;
			else
				alphaStarFlap = alphaMaxHighLift-(alphaMax.to(SI.RADIAN).getEstimatedValue()-alphaStarClean);

			theWing.getAerodynamics().set_alphaStarHigLift(Amount.valueOf(alphaStarFlap, SI.RADIAN));
			double cLStarFlap = cLAlphaFlap * alphaStarFlap + qValue;	

			
			double[][] matrixData = { {Math.pow(alphaMaxHighLift, 3), Math.pow(alphaMaxHighLift, 2)
				, alphaMaxHighLift,1.0},
					{3* Math.pow(alphaMaxHighLift, 2), 2*alphaMaxHighLift, 1.0, 0.0},
					{3* Math.pow(alphaStarFlap, 2), 2*alphaStarFlap, 1.0, 0.0},
					{Math.pow(alphaStarFlap, 3), Math.pow(alphaStarFlap, 2),alphaStarFlap,1.0}};
			RealMatrix m = MatrixUtils.createRealMatrix(matrixData);


			double [] vector = {cLMaxFlap, 0,cLAlphaFlap, cLStarFlap};
			System.out.println(" -----------HIGH LIFT-------------- ");
			System.out.println(" alpha max " + alphaMaxHighLiftDegree + " (deg)");
			System.out.println(" alpha star " + alphaStarFlap*57.3 + " (deg)");
			System.out.println(" cL max " + cLMaxFlap);
			System.out.println(" cL star " + cLStarFlap);
			System.out.println(" cL alpha " + cLAlphaFlap + " (1/rad)");

			double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

			double a = solSystem[0];
			double b = solSystem[1];
			double c = solSystem[2];
			double d = solSystem[3];

			double[] alphaArrayHighLiftPlot = MyArrayUtils.linspace(alphaFirst,
					alphaMaxHighLiftDegree + 4,
					nPoints);

			double[] cLArrayHighLiftPlot = new double [nPoints];


			for ( int i=0 ; i< alphaArrayHighLiftPlot.length ; i++){
				alphaActual = Amount.valueOf(toRadians(alphaArrayHighLiftPlot[i]), SI.RADIAN);
				if (alphaActual.getEstimatedValue() <= alphaStarFlap) { 
					cLArrayHighLiftPlot[i] = cLAlphaFlap * alphaActual.getEstimatedValue() + qValue;}
				else {
					cLArrayHighLiftPlot[i] = a * Math.pow(alphaActual.getEstimatedValue(), 3) + 
							b * Math.pow(alphaActual.getEstimatedValue(), 2) + 
							c * alphaActual.getEstimatedValue() + d;
				}

			}

			// Convert from double to Double in order to use JFreeChart to plot.
			MyArray alphaArrayCleanMyArray = new MyArray(alphaArrayClean);
			Double[] alphaArrayCleanDouble = alphaArrayCleanMyArray.getdDouble();

			MyArray alphaArrayHighLiftMyArray = new MyArray(alphaArrayHighLiftPlot);
			Double[] alphaArrayHighLiftDouble = alphaArrayHighLiftMyArray.getdDouble();

			MyArray cLArrayCleanMyArray = new MyArray(cLArrayClean);
			Double[] cLArrayCleanDouble = cLArrayCleanMyArray.getdDouble();

			MyArray cLArrayHighLiftMyArray = new MyArray(cLArrayHighLiftPlot);
			Double[] cLArrayHighLiftDouble = cLArrayHighLiftMyArray.getdDouble();


			alphaListPlot.add(alphaArrayCleanDouble);
			alphaListPlot.add(alphaArrayHighLiftDouble);

			cLListPlot.add(cLArrayCleanDouble);
			cLListPlot.add(cLArrayHighLiftDouble);



			List<String> legend  = new ArrayList<>(); 


			legend.add("clean");
			legend.add("high lift");

			MyChartToFileUtils.plotJFreeChart(alphaListPlot, 
					cLListPlot,
					"CL vs alpha",
					"alpha", 
					"CL",
					null, null, null, null,
					"deg",
					"",
					true,
					legend,
					subfolderPathHL,
					"CL alpha high lift WING");

			System.out.println("\n----------------------------DONE------------------------------");

		}
		
		public void plotHighLiftCurve(String subfolderPath) throws InstantiationException, IllegalAccessException{
			this.subfolderPathHL = subfolderPath;
			subfolderPathCLAlpha = subfolderPath;
			subfolderPathCheck = false;
			plotCLvsAlphaCurve();
			plotHighLiftCurve();
			subfolderPathCheck = true;

		};
		//-------------------------------------------------------------------------------------
		// GETTERS OF RESULTS:

		public ArrayList<Double> getDeltaCl0_flap_list() {
			return deltaCl0FlapList;
		}

		public double getDeltaCl0_flap() {
			return deltaCl0Flap;
		}

		public ArrayList<Double> getDeltaCL0_flap_list() {
			return deltaCL0FlapList;
		}

		public double getDeltaCL0_flap() {
			return deltaCL0Flap;
		}

		public ArrayList<Double> getDeltaClmax_flap_list() {
			return deltaClmaxFlapList;
		}

		public double getDeltaClmax_flap() {
			return deltaClmaxFlap;
		}

		public ArrayList<Double> getDeltaCLmax_flap_list() {
			return deltaCLmaxFlapList;
		}

		public double getDeltaCLmax_flap() {
			return deltaCLmaxFlap;
		}

		public ArrayList<Double> getDeltaClmax_slat_list() {
			return deltaClmaxSlatList;
		}

		public double getDeltaClmax_slat() {
			return deltaClmaxSlat;
		}

		public ArrayList<Double> getDeltaCLmax_slat_list() {
			return deltaCLmaxSlatList;
		}

		public double getDeltaCLmax_slat() {
			return deltaCLmaxSlat;
		}

		public ArrayList<Double> getcLalpha_new_list() {
			return cLalphaNewList;
		}

		public double getcLalpha_new() {
			return cLalphaNew;
		}

		public double getDeltaAlphaMaxFlap() {
			return deltaAlphaMaxFlap;
		}

		public void setDeltaAlphaMaxFlap(double deltaAlphaMaxFlap) {
			this.deltaAlphaMaxFlap = deltaAlphaMaxFlap;
		}

		public ArrayList<Double> getDeltaCD_list() {
			return deltaCDList;
		}

		public double getDeltaCD() {
			return deltaCD;
		}

		public ArrayList<Double> getDeltaCM_c4_list() {
			return deltaCMC4List;
		}

		public double getDeltaCM_c4() {
			return deltaCMC4;
		}

		public List<Double[]> getDeltaFlap() {
			return deltaFlap;
		}

		public void setDeltaFlap(List<Double[]> deltaFlap) {
			this.deltaFlap = deltaFlap;
		}

		public List<Double> getFlapType() {
			return flapTypeIndex;
		}

		public void setFlapType(List<Double> flapType) {
			this.flapTypeIndex = flapType;
		}

		public List<Double> getDeltaSlat() {
			return deltaSlat;
		}

		public void setDeltaSlat(List<Double> deltaSlat) {
			this.deltaSlat = deltaSlat;
		}

		public List<Double> getEta_in_flap() {
			return etaInFlap;
		}

		public void setEta_in_flap(List<Double> eta_in_flap) {
			this.etaInFlap = eta_in_flap;
		}

		public List<Double> getEta_out_flap() {
			return etaOutFlap;
		}

		public void setEta_out_flap(List<Double> eta_out_flap) {
			this.etaOutFlap = eta_out_flap;
		}

		public List<Double> getEta_in_slat() {
			return etaInSlat;
		}

		public void setEta_in_slat(List<Double> eta_in_slat) {
			this.etaInSlat = eta_in_slat;
		}

		public List<Double> getEta_out_slat() {
			return etaOutSlat;
		}

		public void setEta_out_slat(List<Double> eta_out_slat) {
			this.etaOutSlat = eta_out_slat;
		}

		public List<Double> getCf_c() {
			return cfc;
		}

		public void setCf_c(List<Double> cf_c) {
			this.cfc = cf_c;
		}

		public List<Double> getCs_c() {
			return csc;
		}

		public void setCs_c(List<Double> cs_c) {
			this.csc = cs_c;
		}

		public List<Double> getLeRadius_c_slat() {
			return leRadiusSlatRatio;
		}

		public void setLeRadius_c_slat(List<Double> leRadius_c_slat) {
			this.leRadiusSlatRatio = leRadius_c_slat;
		}

		public List<Double> getcExt_c_slat() {
			return cExtcSlat;
		}

		public void setcExt_c_slat(List<Double> cExt_c_slat) {
			this.cExtcSlat = cExt_c_slat;
		}

		public double getcL_Max_Flap() {
			return cLMaxFlap;
		}

		public void setcL_Max_Flap(double cL_Max_Flap) {
			this.cLMaxFlap = cL_Max_Flap;
		}
	}

	/**
	 * Evaluate alpha zero lift of the entire lifting surface
	 * The alpha0L is considered relative to the root chord
	 * 
	 * @author Lorenzo Attanasio
	 */
	public class CalcAlpha0L extends InnerCalculator {

		private double surfaceInteg;
		private double semispanInteg;
		private MyArray _alpha0lDistributionInteg;
		private MyArray _twistDistributionInteg;
		private MyArray _chordsVsYIntegral;

		public Amount<Angle> integralMeanNoTwist() {

			//			if ( theAircraft.get_exposedWing() != null ){
			//				System.out.println(" y s tation integral " + Arrays.toString(_yStationsIntegral));
			//				surfaceInteg = theAircraft.get_exposedWing().get_surface().getEstimatedValue();
			//				System.out.println(" surface " + theAircraft.get_exposedWing().get_surface().getEstimatedValue());
			//				semispanInteg = theAircraft.get_exposedWing().get_semispan().getEstimatedValue();
			//				System.out.println(" smispan " + theAircraft.get_exposedWing().get_semispan().getEstimatedValue());
			//				MyArray alphaZeroLiftExposed = new MyArray();
			//				MyArray chordDistributionExposed = new MyArray();
			//				alphaZeroLiftExposed.add(
			//						theAircraft
			//						.get_exposedWing()
			//						.get_theAirfoilsListExposed()
			//						.get(0).getAerodynamics()
			//						.get_alphaZeroLift()
			//						.getEstimatedValue());
			//				
			//				System.out.println(" alpha zero lift root " + theAircraft
			//						.get_exposedWing()
			//						.get_theAirfoilsListExposed()
			//						.get(0).getAerodynamics()
			//						.get_alphaZeroLift().to(NonSI.DEGREE_ANGLE)
			//						.getEstimatedValue());
			//				
			//				chordDistributionExposed.add(theAircraft.get_wing().getChordAtYActual(
			//						theAircraft
			//						.get_exposedWing()
			//						.get_theAirfoilsListExposed()
			//						.get(0).getGeometry().get_yStation()));
			//				
			//				System.out.println(" corda " + theAircraft
			//						.get_exposedWing()
			//						.get_theAirfoilsListExposed()
			//						.get(0).getGeometry().get_yStation());
			////						theAircraft.get_fuselage().getWidthAtX(
			////								theAircraft.get_wing().get_xLEMacActualBRF().getEstimatedValue())));
			//				
			//				chordDistributionExposed.add(theAircraft.get_wing().getChordAtYActual(
			//						theAircraft.get_wing().get_semispan().getEstimatedValue()));
			//				
			//				if ( theAircraft.get_exposedWing().get_numberOfAirfoils()<3){
			//					alphaZeroLiftExposed.add(
			//							theAircraft
			//							.get_exposedWing()
			//							.get_theAirfoilsListExposed()
			//							.get(1).getAerodynamics()
			//							.get_alphaZeroLift()
			//							.getEstimatedValue());
			//				}
			//				else{
			//				alphaZeroLiftExposed.add(
			//						theAircraft
			//						.get_exposedWing()
			//						.get_theAirfoilsListExposed()
			//						.get(1).getAerodynamics()
			//						.get_alphaZeroLift()
			//						.getEstimatedValue());
			//				
			//				System.out.println("alpha zero lift kink " + theAircraft
			//						.get_exposedWing()
			//						.get_theAirfoilsListExposed()
			//						.get(1).getAerodynamics()
			//						.get_alphaZeroLift().to(NonSI.DEGREE_ANGLE)
			//						.getEstimatedValue());
			//				
			//				alphaZeroLiftExposed.add(
			//						theAircraft
			//						.get_exposedWing()
			//						.get_theAirfoilsListExposed()
			//						.get(2).getAerodynamics()
			//						.get_alphaZeroLift()
			//						.getEstimatedValue());
			//				
			//				System.out.println(" alpha zero lift tip " + theAircraft
			//						.get_exposedWing()
			//						.get_theAirfoilsListExposed()
			//						.get(2).getAerodynamics()
			//						.get_alphaZeroLift().to(NonSI.DEGREE_ANGLE)
			//						.getEstimatedValue());
			//				}
			//				
			//				double [] yStationAlpha0lExposed = new double [theAircraft.get_exposedWing().get_numberOfAirfoils()];
			//				double [] yStationChordExposed = new double [2];
			//				
			//				for ( int i = 0 ; i < yStationAlpha0lExposed.length ; i ++)
			//					yStationAlpha0lExposed[i] = theAircraft
			//					.get_exposedWing()
			//					.get_theAirfoilsListExposed()
			//					.get(i).getGeometry().get_yStation();
			//				
			//				yStationChordExposed [0] = theAircraft
			//						.get_exposedWing()
			//						.get_theAirfoilsListExposed()
			//						.get(0).getGeometry().get_yStation();
			//				
			//				yStationChordExposed [1] = theAircraft
			//						.get_exposedWing()
			//						.get_theAirfoilsListExposed()
			//						.get(2).getGeometry().get_yStation();
			//				
			//				
			//				
			//				
			//				_alpha0lDistribution = MyArray.createArray(
			//						alphaZeroLiftExposed
			//						.interpolate(yStationAlpha0lExposed, _yStationsIntegral));
			//				
			//				_chordsVsY = MyArray.createArray(
			//						chordDistributionExposed
			//						.interpolate(yStationChordExposed, _yStationsIntegral));
			//				
			//			}
			//			else{
			//				surfaceInteg = surface;
			//				semispanInteg = semispan;
			//			}


			_alpha0L = Amount.valueOf(
					AnglesCalc.alpha0LintegralMeanNoTwist(surface, semispan, 
							_yStationsIntegral, _chordsVsY.toArray(), _alpha0lDistribution.toArray()),
					SI.RADIAN);
			_methodsMap.put(MethodEnum.INTEGRAL_MEAN_NO_TWIST, _alpha0L.doubleValue(SI.RADIAN));

			return _alpha0L;
		}

		public Amount<Angle>  integralMeanWithTwist() {
			
//			System.out.println(" y stat " + _yStationsIntegral.length);
//			System.out.println(" chord vs y " + _chordsVsY.size());
//			System.out.println(" chord " + _chordsVsY.toString());
//			System.out.println(" twist " + _twistDistribution.size());
//			System.out.println("alpha zero lift " + _alpha0lDistribution.size());

			_alpha0L = Amount.valueOf(
					AnglesCalc.alpha0LintegralMeanWithTwist(surface, semispan, 
							_yStationsIntegral, _chordsVsY.toArray(), 
							_alpha0lDistribution.toArray(), _twistDistribution.toArray()),
					SI.RADIAN);
			_methodsMap.put(MethodEnum.INTEGRAL_MEAN_TWIST, _alpha0L.doubleValue(SI.RADIAN));	

			return _alpha0L;
		}

		public Amount<Angle> integralMeanExposedNoTwist() {


			if ( theAircraft.get_exposedWing() != null && theLiftingSurface.getType() == ComponentEnum.WING){
				//System.out.println(" y station integral " + Arrays.toString(_yStationsIntegral));
				surfaceInteg = theAircraft.get_exposedWing().get_surface().getEstimatedValue();
				semispanInteg = theAircraft.get_exposedWing().get_semispan().getEstimatedValue();
				//				MyArray alphaZeroLiftExposed = new MyArray();
				//				MyArray chordDistributionExposed = new MyArray();
				//				alphaZeroLiftExposed.add(
				//						theAircraft
				//						.get_exposedWing()
				//						.get_theAirfoilsListExposed()
				//						.get(0).getAerodynamics()
				//						.get_alphaZeroLift()
				//						.getEstimatedValue());
				//
				//				chordDistributionExposed.add(theAircraft.get_wing().getChordAtYActual(
				//						theAircraft
				//						.get_exposedWing()
				//						.get_theAirfoilsListExposed()
				//						.get(0).getGeometry().get_yStation()));
				//
				//				chordDistributionExposed.add(theAircraft.get_wing().getChordAtYActual(
				//						theAircraft.get_wing().get_semispan().getEstimatedValue()));
				//
				//				if ( theAircraft.get_exposedWing().get_numberOfAirfoils()<3){
				//					alphaZeroLiftExposed.add(
				//							theAircraft
				//							.get_exposedWing()
				//							.get_theAirfoilsListExposed()
				//							.get(1).getAerodynamics()
				//							.get_alphaZeroLift()
				//							.getEstimatedValue());
				//				}
				//				else{
				//					alphaZeroLiftExposed.add(
				//							theAircraft
				//							.get_exposedWing()
				//							.get_theAirfoilsListExposed()
				//							.get(1).getAerodynamics()
				//							.get_alphaZeroLift()
				//							.getEstimatedValue());
				//
				//					alphaZeroLiftExposed.add(
				//							theAircraft
				//							.get_exposedWing()
				//							.get_theAirfoilsListExposed()
				//							.get(2).getAerodynamics()
				//							.get_alphaZeroLift()
				//							.getEstimatedValue());
				//				}
				//
				//				double [] yStationAlpha0lExposed = new double [theAircraft.get_exposedWing().get_numberOfAirfoils()];
				//				double [] yStationChordExposed = new double [2];
				//
				//				for ( int i = 0 ; i < yStationAlpha0lExposed.length ; i ++)
				//					yStationAlpha0lExposed[i] = theAircraft
				//					.get_exposedWing()
				//					.get_theAirfoilsListExposed()
				//					.get(i).getGeometry().get_yStation();
				//
				//				yStationChordExposed [0] = theAircraft
				//						.get_exposedWing()
				//						.get_theAirfoilsListExposed()
				//						.get(0).getGeometry().get_yStation();
				//
				//				yStationChordExposed [1] = theAircraft
				//						.get_exposedWing()
				//						.get_theAirfoilsListExposed()
				//						.get(2).getGeometry().get_yStation();




				_alpha0lDistribution = theAircraft.get_exposedWing().get_alpha0lDistributionExposed();

				_chordsVsY = theAircraft.get_exposedWing().get_chordsVsYExposed();

			}
			else{
				surfaceInteg = surface;
				semispanInteg = semispan;
				System.out.println(" Exposed wing is the wing. There isn't fuselage in the aircraft.");
			}
			_alpha0L = Amount.valueOf(
					AnglesCalc.alpha0LintegralMeanNoTwist(surfaceInteg, semispanInteg, 
							_yStationsIntegral, _chordsVsY.toArray(), _alpha0lDistribution.toArray()),
					SI.RADIAN);
			_methodsMap.put(MethodEnum.INTEGRAL_MEAN_NO_TWIST, _alpha0L.doubleValue(SI.RADIAN));

			return _alpha0L;
		}

		public Amount<Angle>  integralMeanExposedWithTwist() {
			if ( theAircraft.get_exposedWing() != null ){
				//System.out.println(" y station integral " + Arrays.toString(_yStationsIntegral));
				surfaceInteg = theAircraft.get_exposedWing().get_surface().getEstimatedValue();
				semispanInteg = theAircraft.get_exposedWing().get_semispan().getEstimatedValue();
				//				MyArray alphaZeroLiftExposed = new MyArray();
				//				MyArray chordDistributionExposed = new MyArray();
				//				MyArray twistExposed = new MyArray();
				//				
				//				alphaZeroLiftExposed.add(
				//						theAircraft
				//						.get_exposedWing()
				//						.get_theAirfoilsListExposed()
				//						.get(0).getAerodynamics()
				//						.get_alphaZeroLift()
				//						.getEstimatedValue());
				//				
				//				twistExposed.add(
				//						theAircraft
				//						.get_exposedWing()
				//						.get_theAirfoilsListExposed()
				//						.get(0).getGeometry()
				//						.get_twist()
				//						.getEstimatedValue());
				//
				//				chordDistributionExposed.add(theAircraft.get_wing().getChordAtYActual(
				//						theAircraft
				//						.get_exposedWing()
				//						.get_theAirfoilsListExposed()
				//						.get(0).getGeometry().get_yStation()));
				//
				//				chordDistributionExposed.add(theAircraft.get_wing().getChordAtYActual(
				//						theAircraft.get_wing().get_semispan().getEstimatedValue()));
				//
				//				if ( theAircraft.get_exposedWing().get_numberOfAirfoils()<3){
				//					alphaZeroLiftExposed.add(
				//							theAircraft
				//							.get_exposedWing()
				//							.get_theAirfoilsListExposed()
				//							.get(1).getAerodynamics()
				//							.get_alphaZeroLift()
				//							.getEstimatedValue());
				//					
				//					twistExposed.add(
				//							theAircraft
				//							.get_exposedWing()
				//							.get_theAirfoilsListExposed()
				//							.get(1).getGeometry()
				//							.get_twist()
				//							.getEstimatedValue());
				//				}
				//				else{
				//					alphaZeroLiftExposed.add(
				//							theAircraft
				//							.get_exposedWing()
				//							.get_theAirfoilsListExposed()
				//							.get(1).getAerodynamics()
				//							.get_alphaZeroLift()
				//							.getEstimatedValue());
				//
				//					alphaZeroLiftExposed.add(
				//							theAircraft
				//							.get_exposedWing()
				//							.get_theAirfoilsListExposed()
				//							.get(2).getAerodynamics()
				//							.get_alphaZeroLift()
				//							.getEstimatedValue());
				//					
				//
				//					twistExposed.add(
				//							theAircraft
				//							.get_exposedWing()
				//							.get_theAirfoilsListExposed()
				//							.get(1).getGeometry()
				//							.get_twist()
				//							.getEstimatedValue());
				//					
				//
				//					twistExposed.add(
				//							theAircraft
				//							.get_exposedWing()
				//							.get_theAirfoilsListExposed()
				//							.get(2).getGeometry()
				//							.get_twist()
				//							.getEstimatedValue());
				//				}
				//
				//				double [] yStationAlpha0lExposed = new double [theAircraft.get_exposedWing().get_numberOfAirfoils()];
				//				double [] yStationChordExposed = new double [2];
				//				double [] yStationTwistExposed = new double [theAircraft.get_exposedWing().get_numberOfAirfoils()];
				//
				//				for ( int i = 0 ; i < yStationAlpha0lExposed.length ; i ++){
				//					yStationAlpha0lExposed[i] = 
				//							theAircraft
				//							.get_exposedWing()
				//							.get_theAirfoilsListExposed()
				//							.get(i).getGeometry().get_yStation();
				//
				//					yStationTwistExposed [i] = 
				//							theAircraft
				//							.get_exposedWing()
				//							.get_theAirfoilsListExposed()
				//							.get(i).getGeometry().get_yStation();
				//				}
				//
				//				yStationChordExposed [0] = theAircraft
				//						.get_exposedWing()
				//						.get_theAirfoilsListExposed()
				//						.get(0).getGeometry().get_yStation();
				//
				//				yStationChordExposed [1] = theAircraft
				//						.get_exposedWing()
				//						.get_theAirfoilsListExposed()
				//						.get(2).getGeometry().get_yStation();
				//
				//
				//
				//
				//				_alpha0lDistribution = MyArray.createArray(
				//						alphaZeroLiftExposed
				//						.interpolate(yStationAlpha0lExposed, _yStationsIntegral));
				//
				//				_chordsVsY = MyArray.createArray(
				//						chordDistributionExposed
				//						.interpolate(yStationChordExposed, _yStationsIntegral));
				//				
				//				_twistDistribution = MyArray.createArray(
				//						twistExposed
				//						.interpolate(yStationTwistExposed, _yStationsIntegral));

				_alpha0lDistribution = theAircraft.get_exposedWing().get_alpha0lDistributionExposed();
				_chordsVsY = theAircraft.get_exposedWing().get_chordsVsYExposed();

				_twistDistribution =theAircraft.get_exposedWing().get_twistDistributionExposed();

			}
			else{
				surfaceInteg = surface;
				semispanInteg = semispan;
				System.out.println(" Exposed wing is the wing. There isn't fuselage in the aircraft.");
			}

			_alpha0L = Amount.valueOf(
					AnglesCalc.alpha0LintegralMeanWithTwist(surfaceInteg, semispanInteg, 
							_yStationsIntegral, _chordsVsY.toArray(), 
							_alpha0lDistribution.toArray(), _twistDistribution.toArray()),
					SI.RADIAN);
			_methodsMap.put(MethodEnum.INTEGRAL_MEAN_TWIST, _alpha0L.doubleValue(SI.RADIAN));	

			return _alpha0L;
		}

		public void allMethods() {
			integralMeanNoTwist();
			integralMeanWithTwist();
		}

	}

	/** 
	 * Calculate the lift coefficient gradient of the whole lifting surface.
	 * The class hold all available methods to estimate such gradient
	 * (1/rad)
	 * 
	 * @author Lorenzo Attanasio
	 */
	public class CalcCLAlpha extends InnerCalculator {

		public CalcCLAlpha() {

		}

		/**
		 * This function determines the linear trait slope of the CL-alpha curve using the NasaBlackwell method.
		 * It evaluate CL wing in correspondence of two alpha and calculates the equation of the line.
		 * 
		 * @author Manuela Ruocco
		 * @param LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha
		 */  

		public double nasaBlackwell(LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha){

			Amount<Angle> _alphaOne = Amount.valueOf(toRadians(0.), SI.RADIAN);
			double _clOne = theCLatAlpha.nasaBlackwell(_alphaOne);
//			System.out.println("CL at alpha " + _alphaOne + " = "+ _clOne);

			Amount<Angle>_alphaTwo = Amount.valueOf(toRadians(4.), SI.RADIAN);
			double alphaTwoDouble = 4.0;
			double _clTwo = theCLatAlpha.nasaBlackwell(_alphaTwo);
//			System.out.println("CL at alpha " + _alphaTwo + " = "+ _clTwo);

			double cLSlope = (_clTwo-_clOne)/_alphaTwo.getEstimatedValue();
			double cLSlopeDeg = Math.toRadians(cLSlope);

			double q = _clTwo- cLSlopeDeg* alphaTwoDouble;
			return cLSlope;
		}
		
		public double polhamus() {

			_kPolhamus = LiftCalc.kFactorPolhamus(
					ar, machCurrent, 
					getTheLiftingSurface()._sweepLEEquivalent, taperRatioEq);

			_cLAlpha = LiftCalc.calculateCLalphaPolhamus(ar, machCurrent, 
					getTheLiftingSurface()._sweepLEEquivalent, taperRatioEq);

			_methodsMap.put(MethodEnum.POLHAMUS, _cLAlpha);		

			return _cLAlpha;
		}

		/**
		 * pag. 49 ADAS
		 */
		public double andersonSweptCompressibleSubsonic() {

			_cLAlpha = LiftCalc.calcCLalphaAndersonSweptCompressibleSubsonic(
					machCurrent, ar, semispan, sweepHalfChordEq, 
					_yStations, _clAlphaVsY.toArray(), _chordsVsY.toArray());

			_methodsMap.put(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC, _cLAlpha);

			return _cLAlpha;
		}

		/** 
		 * This method gets called by andersonSweptCompressibleSubsonic
		 *
		 * @author Lorenzo Attanasio
		 * @return
		 */
		public double integralMean2D() {
			_cLAlpha = LiftCalc.calcCLalphaIntegralMean2D(
					surface, semispan, _yStations, 
					_clAlphaVsY.toArray(), _chordsVsY.toArray());

			_methodsMap.put(MethodEnum.INTEGRAL_MEAN_NO_TWIST, _cLAlpha);
			return _cLAlpha;
		}


		public void allMethods() {
			polhamus();
			andersonSweptCompressibleSubsonic();
		}

	}

	public class CalcCL0 extends InnerCalculator {

		public CalcCL0() {

		}

		public double andersonSweptCompressibleSubsonic() {
			_cL0 = LiftCalc.calculateLiftCoefficientAtAlpha0(
					calculateAlpha0L.integralMeanWithTwist().getEstimatedValue(),
					calculateCLAlpha.andersonSweptCompressibleSubsonic());

			_methodsMap.put(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC, _cL0);
			return _cL0;
		}

		public void allMethods() {
			andersonSweptCompressibleSubsonic();
		}

	}

	/**
	 * Evaluate alpha zero lift of the entire lifting surface
	 * 
	 * @author Lorenzo Attanasio
	 */
	public class CalcCmAC extends InnerCalculator {

		/** A local array to manage chords along span */
		private MyArray _chordsVsYActualAirfoils;

		public CalcCmAC() {
			_chordsVsYActualAirfoils = getTheLiftingSurface()._chordsVsYActual.interpolate(
					getTheLiftingSurface()._eta.toArray(), etaAirfoil);
		}

		public double additional() {
			if (!_methodsMap.containsKey(MethodEnum.ADDITIONAL)) {
				_cMacAdditional = (2/(surface*getTheLiftingSurface()._meanAerodChordActual.getEstimatedValue()))
						* MyMathUtils.integrate1DSimpsonSpline(
								getTheLiftingSurface()._etaAirfoil.times(semispan), 
								getTheLiftingSurface()._cmAC_y.times(_chordsVsYActualAirfoils.getRealVector().map(new Power(2))).toArray(), 
								0., semispan);
				_methodsMap.put(MethodEnum.ADDITIONAL, _cMacAdditional);

			} else {
				_cMacAdditional = _methodsMap.get(MethodEnum.ADDITIONAL); 
			}
			return _cMacAdditional;
		}

		public double basic() {
			if (!_methodsMap.containsKey(MethodEnum.BASIC)) {
				_cMacBasic = (2/(surface*getTheLiftingSurface()._meanAerodChordActual.getEstimatedValue()))
						* MyMathUtils.integrate1DSimpsonSpline(
								getTheLiftingSurface()._yStationsAirfoil.toArray(), 
								getTheLiftingSurface()._clBasic_y
								.times(_chordsVsYActualAirfoils)
								.times(getTheLiftingSurface()._distanceAirfoilACFromWingAC)
								.toArray(), 
								0., semispan);
				_methodsMap.put(MethodEnum.BASIC, _cMacBasic);

			} else {
				_cMacBasic = _methodsMap.get(MethodEnum.BASIC); 
			}
			return _cMacBasic;
		}

		public void integralMean() {
			_cMacTotal = (2/(surface
					*getTheLiftingSurface()._meanAerodChordActual.getEstimatedValue()))
					* MyMathUtils.integrate1DSimpsonSpline(
							getTheLiftingSurface()._etaAirfoil.times(semispan), 
							getTheLiftingSurface()._cmAC_y.times(_chordsVsYActualAirfoils.times(_chordsVsYActualAirfoils)).toArray(), 
							0., semispan);
			_methodsMap.put(MethodEnum.INTEGRAL_MEAN, _cMacTotal);
		}

		public void total() {
			_cMacTotal = additional() + basic();
			_methodsMap.put(MethodEnum.TOTAL, _cMacTotal);
		}

		public void allMethods() {
			total();
		}

	}

	public class CalcCm0 extends InnerCalculator {

		public CalcCm0() {

		}

		//FIXME: I think there is something wrong with this method
		public double andersonSweptCompressibleSubsonic(Aircraft aircraft) {
			_cM0 = LiftCalc.calcCLalphaAndersonSweptCompressibleSubsonic(
					machCurrent, ar, semispan, 
					sweepHalfChordEq, _yStations, _clAlphaVsY.toArray(), _chordsVsY.toArray())
					+ calculateCL0.andersonSweptCompressibleSubsonic()
					* (aircraft.get_wing().get_AC_CGdistance().getEstimatedValue()
							/aircraft.get_wing().get_meanAerodChordActual().getEstimatedValue());

			_methodsMap.put(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC, _cM0);
			return _cM0;
		}

		public void allMethods() {

		}

	}

	public class CalcCmAlpha extends InnerCalculator {

		private Aircraft theLocalAircraft;

		public CalcCmAlpha(Aircraft aircraft) {
			theLocalAircraft = aircraft;
		}

		public double andersonSweptCompressibleSubsonic() {

			CenterOfGravity cg;

			if (theLocalAircraft != null) {
				cg = theLocalAircraft.get_theBalance().get_cgMTOM();
			} else {
				cg = theLiftingSurface.get_cg();
			}
			System.out.println("the xbr is --> " + cg.get_xBRF());
			System.out.println("the cg is --> " + cg);

			_cmAlpha = MomentCalc.calcCMalphaLS(calculateCLAlpha.andersonSweptCompressibleSubsonic(),
					cg.get_xBRF().getEstimatedValue(), 
					calculateXAC.atQuarterMAC(), getTheLiftingSurface()._xLEMacActualBRF.getEstimatedValue(), 
					getTheLiftingSurface()._meanAerodChordActual.getEstimatedValue());

			_methodsMap.put(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC, _cmAlpha);
			return _cmAlpha;
		}

		public double polhamus() {

			CenterOfGravity cg;

			if (theLocalAircraft != null) {
				cg = theLocalAircraft.get_theBalance().get_cgMTOM();
			} else {
				cg = theLiftingSurface.get_cg();
			}
			System.out.println("the xbr is --> " + cg.get_xBRF());
			System.out.println("the cg is --> " + cg);
			_cmAlpha = MomentCalc.calcCMalphaLS(calculateCLAlpha.polhamus(),
					cg.get_xBRF().getEstimatedValue(), 
					calculateXAC.atQuarterMAC(), getTheLiftingSurface()._xLEMacActualBRF.getEstimatedValue(), 
					getTheLiftingSurface()._meanAerodChordActual.getEstimatedValue());

			_methodsMap.put(MethodEnum.POLHAMUS, _cmAlpha);
			return _cmAlpha;
		}

		public void allMethods(){
			andersonSweptCompressibleSubsonic();
			polhamus();
		}

	}

	//	/**
	//	 * Store aerodynamic results in order to
	//	 * access them easier.
	//	 * 
	//	 * @author Lorenzo Attanasio
	//	 */
	//	private class Results {
	//
	//		private Amount<Angle> alpha;
	//		private double cL, cD, cM;
	//		private MyArray cly = new MyArray(Unit.ONE), 
	//				ccly = new MyArray(Unit.ONE),
	//				cdy = new MyArray(Unit.ONE),
	//				cmy = new MyArray(Unit.ONE);
	//		
	//		private CustomMap cl = new CustomMap();
	//
	////		private Map<Amount<Angle>, Double> cLVsAlpha = new TreeMap<Amount<Angle>, Double>();
	////		private Map<Amount<Angle>, Double> cDVsAlpha = new TreeMap<Amount<Angle>, Double>();
	////		private Map<Amount<Angle>, Double> cMVsAlpha = new TreeMap<Amount<Angle>, Double>();
	////
	////		private Map<MyMethodEnum, Map<Amount<Angle>, Double>> cLVsAlphaMap =
	////				new TreeMap<MyMethodEnum, Map<Amount<Angle>, Double>>();
	////		private Map<MyMethodEnum, Map<Amount<Angle>, Double>> cDVsAlphaMap =
	////				new TreeMap<MyMethodEnum, Map<Amount<Angle>, Double>>();
	////		private Map<MyMethodEnum, Map<Amount<Angle>, Double>> cMVsAlphaMap =
	////				new TreeMap<MyMethodEnum, Map<Amount<Angle>, Double>>();
	////
	////		private Map<Amount<Angle>, MyArray> clyVsAlpha = new TreeMap<Amount<Angle>, MyArray>();
	////		private Map<Amount<Angle>, MyArray> cclyVsAlpha = new TreeMap<Amount<Angle>, MyArray>();
	////		private Map<Amount<Angle>, MyArray> cdyVsAlpha = new TreeMap<Amount<Angle>, MyArray>();
	////		private Map<Amount<Angle>, MyArray> cmyVsAlpha = new TreeMap<Amount<Angle>, MyArray>();
	////
	////		private Map<MyMethodEnum, Map<Amount<Angle>, MyArray>> clyVsAlphaMap =
	////				new TreeMap<MyMethodEnum, Map<Amount<Angle>, MyArray>>();
	////		private Map<MyMethodEnum, Map<Amount<Angle>, MyArray>> cclyVsAlphaMap =
	////				new TreeMap<MyMethodEnum, Map<Amount<Angle>, MyArray>>();
	////		private Map<MyMethodEnum, Map<Amount<Angle>, MyArray>> cdyVsAlphaMap =
	////				new TreeMap<MyMethodEnum, Map<Amount<Angle>, MyArray>>();
	////		private Map<MyMethodEnum, Map<Amount<Angle>, MyArray>> cmyVsAlphaMap =
	////				new TreeMap<MyMethodEnum, Map<Amount<Angle>, MyArray>>();
	//
	//		public MyArray getCly(Amount<Angle> alpha, MyMethodEnum method) {
	//			return cl.geclyVsAlphaMap.get(method).get(alpha);
	//		}
	//
	//		public void setCly(Amount<Angle> alpha, MyArray cly, MyMethodEnum method) {
	//			clyVsAlpha.put(alpha, cly);
	//			clyVsAlphaMap.put(method, clyVsAlpha);
	//		}
	//
	//	}

	
public class CalcCdvsAlpha {
		
		public Double[] calculateCDParasiteFromAirfoil(Amount<Angle> alphaMin,
				Amount<Angle> alphaMax,
				int nValue){
			
			CalcLiftDistribution calculateLiftDistribution = getTheLiftingSurface().getAerodynamics().getCalculateLiftDistribution();
			double [] cDDistribution;
			
			if (alphaMin.getUnit() == NonSI.DEGREE_ANGLE)
				alphaMin= alphaMin.to(SI.RADIAN);
			
			if (alphaMax.getUnit() == NonSI.DEGREE_ANGLE)
				alphaMax= alphaMax.to(SI.RADIAN);
			
			int nValueTemp = 30;
			
			double[] alphaCDArray =  MyArrayUtils.linspace(
					alphaMin.to(NonSI.DEGREE_ANGLE).getEstimatedValue(),
					alphaMax.to(NonSI.DEGREE_ANGLE).getEstimatedValue(),
					nValue); // array in rad
			
			
			double[] alphaCDArrayTemp =  MyArrayUtils.linspace(
					alphaMin.getEstimatedValue(),
					alphaMax.getEstimatedValue(),
					nValueTemp); // array in rad
			
			double[] alphaCDArrayDeg =  MyArrayUtils.linspace(
					alphaMin.to(NonSI.DEGREE_ANGLE).getEstimatedValue(),
					alphaMax.to(NonSI.DEGREE_ANGLE).getEstimatedValue(),
					nValueTemp); // array in rad
			MyAirfoil intermediateAirfoil;
			Amount<Angle> alphaActual;
			
			double [] cDDistributionTemp =  new double [nValueTemp];
			double[] clDistribution; 
			
			for (int i=0; i<alphaCDArrayTemp.length ; i++){
				
				alphaActual = Amount.valueOf(alphaCDArrayTemp[i], SI.RADIAN);
				calculateLiftDistribution.getNasaBlackwell().calculate(alphaActual);
				clDistribution = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
				int nValueNasaBlackwell = clDistribution.length;
				clDistribution[clDistribution.length-1] = 0;
			
				cDDistribution = new double [nValueNasaBlackwell];
				
				double[] yLoc = new double [nValueNasaBlackwell];
				double[] yLocNonDm;
				yLoc = MyArrayUtils.linspace(0, getTheLiftingSurface().get_semispan().getEstimatedValue(),nValueNasaBlackwell );
				yLocNonDm = MyArrayUtils.linspace(0, 1,nValueNasaBlackwell );
				for (int j=0; j<nValueNasaBlackwell; j++){
					
					intermediateAirfoil = calculateIntermediateAirfoil(getTheLiftingSurface(), yLoc[j]);
					cDDistribution[j] = intermediateAirfoil.getAerodynamics().calculateCdAtClLinear(clDistribution[j]);
					
				}
				
				cDDistributionTemp[i] = MyMathUtils.integrate1DTrapezoidLinear(yLocNonDm, cDDistribution,0, 1);
			}
			
			
			
			Double[] cDArray = MyMathUtils.getInterpolatedValue1DLinear(alphaCDArrayDeg,cDDistributionTemp, alphaCDArray);
			
			
			return cDArray;
		
	}
		public double[] calculateCDInduced(Amount<Angle> alphaMin,
				Amount<Angle> alphaMax,
				int nValue){

			if (alphaMin.getUnit() == NonSI.DEGREE_ANGLE)
				alphaMin= alphaMin.to(SI.RADIAN);
			
			if (alphaMax.getUnit() == NonSI.DEGREE_ANGLE)
				alphaMax= alphaMax.to(SI.RADIAN);
			
			double[] clDistribution; 
			AlphaEffective theAlphaInducedCalculator = new AlphaEffective(getTheLiftingSurface().getAerodynamics(),
					getTheLiftingSurface(), theOperatingConditions);
			
			CalcLiftDistribution calculateLiftDistribution = getTheLiftingSurface().getAerodynamics().getCalculateLiftDistribution();
			
			double[] alphaCDInduced =  MyArrayUtils.linspace(
					alphaMin.getEstimatedValue(),
					alphaMax.getEstimatedValue(),
					nValue); // array in rad
			
			MyAirfoil intermediateAirfoil;
			Amount<Angle> alphaActual;
			double[] cdDistribution, cDDistribution;
			Double []  alphaInduced;
			cDDistribution = new double[nValue];
			
			for (int ii=0; ii<alphaCDInduced.length; ii++){
				alphaActual = Amount.valueOf(alphaCDInduced[ii], SI.RADIAN);
				calculateLiftDistribution.getNasaBlackwell().calculate(alphaActual);
				clDistribution = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
				int nValueNasaBlackwell = clDistribution.length;
				alphaInduced = new Double [nValueNasaBlackwell];
				cdDistribution = new double [nValueNasaBlackwell];
				double[] yLoc = new double [nValueNasaBlackwell];
				double [] yLocNonDm = MyArrayUtils.linspace(0, 1,nValueNasaBlackwell );
				clDistribution[clDistribution.length-1] = 0;
				yLoc = MyArrayUtils.linspace(0, getTheLiftingSurface().get_semispan().getEstimatedValue(),nValueNasaBlackwell );
				theAlphaInducedCalculator.calculateAlphaEffective(alphaActual);
				double [] alphaInducedTemp = theAlphaInducedCalculator.getAlphaInduced();
				alphaInduced = MyMathUtils.getInterpolatedValue1DLinear(
						theAlphaInducedCalculator.getyStationsActual(), alphaInducedTemp, yLoc);
				for (int i = 0; i<nValueNasaBlackwell; i++ ){
				
				cdDistribution[i] =  clDistribution[i] * (double) alphaInduced[i];
				}
				
				cDDistribution[ii] = MyMathUtils.integrate1DTrapezoidLinear(yLocNonDm, cdDistribution,0, 1);
			}
			
			
			
			return cDDistribution;
		}
	}
	/**
	 * This class calculates the cd distribution along the semispan 
	 * 
	 * @author Manuela Ruocco
	 */

	public class CalcCdDistribution {

		public double[] nasaBlackwell(Amount<Angle> alpha, LSAerodynamicsManager theLSManager) {


			MyAirfoil airfoilActual;
			double yActual;
			cdDistributionNasaBlackwell = new double [_nPointsSemispanWise];
			LiftingSurface theLS = getTheLiftingSurface();

			for (int i=0 ; i<_nPointsSemispanWise ; i++){
				yActual = get_yStations()[i];
				airfoilActual = calculateIntermediateAirfoil(theLS, yActual);
				CalculateCdAirfoil calculateCd =  new CalculateCdAirfoil();
				cdDistributionNasaBlackwell [i] = calculateCd.nasaBlackwell(alpha, theLSManager, airfoilActual);
			}
			cdDistributionNasaBlackwell [_nPointsSemispanWise-1] = 0 ;
			return cdDistributionNasaBlackwell ;
		}

	}

	/**
	 * This nested class has some methods that evaluates the cd of an airfoil corresponding to an alpha that 
	 * is the input value of the method. The difference between the methods is the way to calculate cl from alpha.
	 * 
	 * @author Manuela Ruocco
	 */  


	public class CalculateCdAirfoil {

		double [] clNasaBlackwell , clSchrenk;
		double [] yStations ;
		double clLocal , clLocalSchrenk, cdLocal, cdLocalSchrenk;

		/**
		 * @param alpha of the wing
		 * @param LSAerodynamic Manager
		 * 
		 * @author Manuela Ruocco
		 */  

		public double nasaBlackwell(Amount<Angle> alpha, LSAerodynamicsManager theLSManager, MyAirfoil airfoil){

			double yLoc = airfoil.getGeometry().get_yStation();
			LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();
			calculateLiftDistribution.getNasaBlackwell().calculate(alpha);
			clNasaBlackwell = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
			yStations = calculateLiftDistribution.getNasaBlackwell().getyStations();

			clLocal = MyMathUtils.getInterpolatedValue1DLinear(yStations, clNasaBlackwell, yLoc);

			cdLocal = airfoil.getAerodynamics().get_cdMin()
					+ Math.pow(clLocal - airfoil.getAerodynamics().get_clAtCdMin(), 2 )
					* airfoil.getAerodynamics().get_kFactorDragPolar();

			return cdLocal;
		}

		public double schrenk(Amount<Angle> alpha, LSAerodynamicsManager theLSManager, MyAirfoil airfoil){

			double yLoc = airfoil.getGeometry().get_yStation();
			LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution =  theLSManager.getCalculateLiftDistribution();
			LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha= theLSManager.new CalcCLAtAlpha();
			theCLatAlpha.nasaBlackwell(alpha);
			clSchrenk = calculateLiftDistribution.schrenk();
			yStations = theLSManager.get_yStations();

			clLocalSchrenk = MyMathUtils.getInterpolatedValue1DLinear(yStations, clSchrenk, yLoc);

			System.out.println(" cl local schrenk " + clLocalSchrenk);

			cdLocalSchrenk = airfoil.getAerodynamics().get_cdMin() 
					+ Math.pow(clLocalSchrenk - airfoil.getAerodynamics().get_clAtCdMin(), 2 ) 
					* airfoil.getAerodynamics().get_kFactorDragPolar();

			System.out.println(" cd local schrenk " + cdLocalSchrenk);
			return cdLocalSchrenk;
		}


		/**
		 * This function plots the airfoil drag polar using different methods to evaluate the cl at alpha. 
		 * 
		 * @author Manuela Ruocco
		 * @param LSAerodynamicsManager theLSManager
		 * @param MethodEnum method
		 * 
		 */  

		public void plotPolar(LSAerodynamicsManager theLSManager, MethodEnum method, MyAirfoil airfoil){

			System.out.println("\n \n-----------------------------------------------------");
			System.out.println("STARTING PLOT AIRFOIL DRAG POLAR");
			System.out.println("-----------------------------------------------------");


			MyArray alphaArray = new MyArray();
			int _numberOfAlpha = 40;
			double [] cdArrayPolar = new double [_numberOfAlpha];
			double [] clArrayPolar = new double [_numberOfAlpha];
			double [] cdArrayPolarSchrenk = new double [_numberOfAlpha];
			double [] clArrayPolarSchrenk = new double [_numberOfAlpha];
			Amount<Angle> alphaActualAmount;


			Amount<Angle> alphaStart = Amount.valueOf(toRadians(-6.), SI.RADIAN);
			Amount<Angle> alphaEnd = Amount.valueOf(toRadians(12.), SI.RADIAN);

			alphaArray.setDouble(MyArrayUtils.linspace(
					alphaStart.getEstimatedValue(), 
					alphaEnd.getEstimatedValue(), 
					_numberOfAlpha));

			String folderPathPolar = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;

			switch (method) {

			case NASA_BLACKWELL:
				String subfolderPathPolar = JPADStaticWriteUtils.createNewFolder(folderPathPolar + "Polar_Airfoil_NasaBlackwell" + File.separator);

				for (int i=0 ; i<_numberOfAlpha ; i++){
					alphaActualAmount = Amount.valueOf( alphaArray.get(i), SI.RADIAN); 
					cdArrayPolar[i] = nasaBlackwell(alphaActualAmount, theLSManager, airfoil);
					clArrayPolar[i] = getClLocal();
				}
				System.out.println(" CL NASA " + Arrays.toString(clArrayPolar));
				System.out.println(" CD NASA " + Arrays.toString(clArrayPolar));

				MyChartToFileUtils.plotNoLegend
				( cdArrayPolar,clArrayPolar ,0.0 , 0.1 ,
						-1.0 ,1.5, "Cd", "Cl", "" , "", subfolderPathPolar,
						"Polar Airfoil at station " + airfoil.getGeometry().get_yStation());

				break; 

			case SCHRENK:
				String subfolderPathPolarSchrenk = JPADStaticWriteUtils.createNewFolder(folderPathPolar + "Polar_Airfoil_Schrenk" + File.separator);

				for (int j=0 ; j<_numberOfAlpha ; j++){
					alphaActualAmount = Amount.valueOf( alphaArray.get(j), SI.RADIAN); 
					cdArrayPolarSchrenk[j] = schrenk(alphaActualAmount, theLSManager, airfoil);
					clArrayPolarSchrenk[j] = getClLocalShrenk();
				}

				System.out.println(" CL SHRENK " + Arrays.toString(clArrayPolarSchrenk));
				System.out.println(" CD SCHRENK " + Arrays.toString(clArrayPolarSchrenk));

				MyChartToFileUtils.plotNoLegend
				( cdArrayPolarSchrenk,clArrayPolarSchrenk ,0.0 , 0.1 ,
						-1.0 ,1.5, "Cd", "Cl", "" , "", subfolderPathPolarSchrenk,
						"Polar Airfoil at station " + airfoil.getGeometry().get_yStation());
				break;

			case AIRFOIL_DISTRIBUTION:
				break;
			}

			System.out.println("\n \n-----------------------------------------------------");
			System.out.println("DONE ");
			System.out.println("-----------------------------------------------------");

		}

		public double getClLocal() {
			return clLocal;
		}

		public double getClLocalShrenk() {
			return clLocalSchrenk;
		}
	}


	/**
	 * This class evaluates the drag coefficient of a wing. This method calls CalcCdDistribution in order
	 * to evaluate the Cd distribution at alpha.
	 * 
	 * 
	 * @author Manuela Ruocco
	 */

	public class CalcCDAtAlpha{

		public Double integralFromCdAirfoil(
				Amount<Angle> alpha, MethodEnum method, LSAerodynamicsManager theLSManager){

			double [] yStations = get_yStationsND();
			double [] cdLocal;
			Double cdAtAlpha = null;

			switch (method) {
			case NASA_BLACKWELL:
				CalcCdDistribution theCdCalculator = new CalcCdDistribution();
				theCdCalculator.nasaBlackwell(alpha, theLSManager);
				cdLocal = getCdDistributionNasaBlackwell();
				cdAtAlpha = MyMathUtils.integrate1DSimpsonSpline(
						yStations, cdLocal);		

				//System.out.println(" CD Total at alpha " + alpha.getEstimatedValue()*57.3 + " = " + cdAtAlpha);
				break;

			}

			return cdAtAlpha;
		}
	}



	/**
	 * Store aerodynamic results in order to
	 * access them easier.
	 * 
	 * @author Lorenzo Attanasio
	 */
	public class CoefficientWrapper {

		private MethodEnum method;
		private Amount<Angle> alpha;
		private double cX, cXMax;
		private MyArray cxy = new MyArray(Unit.ONE);
		private MyArray xValues;

		//		private Map<Amount<Angle>, Double> cXVsAlpha = new TreeMap<Amount<Angle>, Double>();
		private TreeMap<MethodEnum, Double> cXMaxMap;
		private HashBasedTable<MethodEnum, MyArray, MyArray> cXVsAlphaAsArrayTable;
		private TreeBasedTable<MethodEnum, Amount<Angle>, Double> cXVsAlphaTable;

		//		private Map<Amount<Angle>, MyArray> cxyVsAlpha = new TreeMap<Amount<Angle>, MyArray>();
		//		private Map<Amount<Angle>, MyArray> ccxyVsAlpha = new TreeMap<Amount<Angle>, MyArray>();
		private TreeBasedTable<MethodEnum, Amount<Angle>, MyArray> cxyVsAlphaTable;
		private TreeBasedTable<MethodEnum, Amount<Angle>, MyArray> ccxyVsAlphaTable;

		public CoefficientWrapper() {
			setcXMaxMap(new TreeMap<MethodEnum, Double>());
			cXVsAlphaTable = TreeBasedTable.create();
			cXVsAlphaAsArrayTable = HashBasedTable.create();
			cxyVsAlphaTable = TreeBasedTable.create();
			ccxyVsAlphaTable = TreeBasedTable.create();
		}



		public int getNumberOfMethods() {
			return cxyVsAlphaTable.size();
		}

		public double getcXMax() {
			return cXMax;
		}

		public void setcXMax(double cXMax) {
			this.cXMax = cXMax;
		}

		public double getcX() {
			return cX;
		}

		public TreeBasedTable<MethodEnum, Amount<Angle>, Double> getcXVsAlphaTable() {
			return cXVsAlphaTable;
		}

		public TreeBasedTable<MethodEnum, Amount<Angle>, MyArray> getCxyVsAlphaTable() {
			return cxyVsAlphaTable;
		}

		public TreeBasedTable<MethodEnum, Amount<Angle>, MyArray> getCcxyVsAlphaTable() {
			return ccxyVsAlphaTable;
		}

		public HashBasedTable<MethodEnum, MyArray, MyArray> getcXVsAlphaAsArrayTable() {

			if(cXVsAlphaTable != null) {

				// Loop over methods
				for (Entry<MethodEnum, Map<Amount<Angle>, Double>> m : cXVsAlphaTable.rowMap().entrySet()) {

					MyArray alpha = new MyArray(SI.RADIAN), 
							cL = new MyArray(Unit.ONE);

					Map<Amount<Angle>, Double> innerMap = m.getValue();

					// Loop over alphas
					for (Entry<Amount<Angle>, Double> mm : innerMap.entrySet()){
						alpha.add(mm.getKey().getEstimatedValue());
						cL.add(mm.getValue());
					}

					cXVsAlphaAsArrayTable.put(m.getKey(), alpha, cL);
				}
			}

			return cXVsAlphaAsArrayTable;
		}

		public void setcXVsAlphaAsArrayMap(HashBasedTable<MethodEnum, MyArray, MyArray> cXVsAlphaAsArrayMap) {
			this.cXVsAlphaAsArrayTable = cXVsAlphaAsArrayMap;
		}

		public TreeMap<MethodEnum, Double> getcXMaxMap() {
			return cXMaxMap;
		}

		public void setcXMaxMap(TreeMap<MethodEnum, Double> cXMaxMap) {
			this.cXMaxMap = cXMaxMap;
		}

		public MyArray getxValues() {
			return xValues;
		}

		public void setxValues(MyArray xValues) {
			this.xValues = xValues;
		}

	}


	public class MeanAirfoil { //Behind ADAS p39
		private double influenceAreaRoot, influenceAreaKink, influenceAreaTip ;
		private double kRoot, kKink, kTip;
		private double rootChord, kinkChord, tipChord, dimensionalKinkStation, dimensionalOverKink;
		private double alphaStarRoot, alphaStarKink, alphaStarTip;
		private double alphaZeroLiftRoot, alphaZeroLiftKink, alphaZeroLiftTip;
		private double clAplhaRoot, clAplhaKink, clAplhaTip;
		private double clStarRoot, clStarKink, clStarTip;
		private double alphaMaxRoot, alphaMaxKink, alphaMaxTip;
		private double clMaxRoot, clMaxKink, clMaxTip;
		private double cdMinRoot, cdMinKink, cdMinTip;
		private double cl_cdMinRoot, cl_cdMinKink, cl_cdMinTip;
		private double kDragPolarRoot, kDragPolarKink, kDragPolarTip;
		private double x_acRoot, x_acKink, x_acTip;
		private double cm_acRoot, cm_acKink, cm_acTip;
		private double cm_ac_StallRoot, cm_ac_StallKink, cm_ac_StallTip;
		private double cmAlpha_acRoot, cmalpha_acKink, cmAlpha_acTip;
		private double reynoldsCruiseRoot, reynoldsCruiseKink, reynoldsCruiseTip;
		private double reynoldsStallRoot, reynoldsStallKink, reynoldsStallTip;
		private double twistRoot, twistKink, twistTip;
		private double phi_TERoot, phi_TEKink, phi_TETip;
		private double radius_LERoot, radius_LEKink, radius_LETip;
		private double thicknessOverChordUnit_Root, thicknessOverChordUnit_Kink, thicknessOverChordUnit_Tip;
		private double maxThicknessOverChord_Root, maxThicknessOverChord_Kink, maxThicknessOverChord_Tip;



		/**
		 * This function calculates the characteristics of the mean airfoil using the influence areas.
		 * This function creates a new airfoil with its own characteristics that are the characteristics of the
		 * mean airfoil.
		 * 
		 * @author Manuela Ruocco ft Vittorio Trifari
		 * @param Airfoilroot
		 * @param root position
		 * @param Airfoilkink
		 * @param Kink position
		 * @param Airfoiltip
		 * @param Tip position
		 */  



		public MyAirfoil calculateMeanAirfoil (LiftingSurface theWing){

			MyAirfoil meanAirfoil = new MyAirfoil(theWing);

			if ( theLiftingSurface.get_type() == ComponentEnum.WING ){
				MyAirfoil airfoilRoot = theWing.get_theAirfoilsList().get(0);
				MyAirfoil airfoilKink = theWing.get_theAirfoilsList().get(1);
				MyAirfoil airfoilTip = theWing.get_theAirfoilsList().get(2);

				//			System.out.println( "---------------------------------------");
				//			System.out.println( "STARTING EVALUATION OF THE MEAN AIRFOIL");
				//			System.out.println( "---------------------------------------");
				//			System.out.println("\n \nSTART OF THE EVALUTATION OF THE INFLUENCE AREAS...");

				rootChord = theWing.get_chordRoot().getEstimatedValue();
				kinkChord = theWing.get_chordKink().getEstimatedValue();
				tipChord = theWing.get_chordTip().getEstimatedValue();
				dimensionalKinkStation = theWing.get_spanStationKink()*theWing.get_semispan().getEstimatedValue();
				dimensionalOverKink = theWing.get_semispan().getEstimatedValue() - dimensionalKinkStation;

				influenceAreaRoot = rootChord * dimensionalKinkStation/2;
				influenceAreaKink = (kinkChord * dimensionalKinkStation/2) + (kinkChord * dimensionalOverKink/2);
				influenceAreaTip = tipChord * dimensionalOverKink/2;
				//
				//			System.out.println("The influence area of root chord is [m^] = " + influenceAreaRoot );
				//			System.out.println("The influence area of kink chord is [m^] = " + influenceAreaKink );
				//			System.out.println("The influence area of tip chord is [m^] = " + influenceAreaTip);

				kRoot = 2*influenceAreaRoot/theWing.get_surface().getEstimatedValue();
				kKink = 2*influenceAreaKink/theWing.get_surface().getEstimatedValue();
				kTip = 2*influenceAreaTip/theWing.get_surface().getEstimatedValue();

				//			System.out.println("The coefficients of influence areas are: \n k1= " + kRoot + 
				//					"\n k2= " + kKink + "\n k3= " + kTip);
				//
				//			System.out.println( "\n \n---------------------------------------");
				//			System.out.println("DONE");

				//ALPHA ZERO LIFT
				alphaZeroLiftRoot = airfoilRoot.getAerodynamics().get_alphaZeroLift().getEstimatedValue();
				alphaZeroLiftKink = airfoilKink.getAerodynamics().get_alphaZeroLift().getEstimatedValue();
				alphaZeroLiftTip = airfoilTip.getAerodynamics().get_alphaZeroLift().getEstimatedValue();

				double alphaZeroLiftMeanAirfoil = alphaZeroLiftRoot * kRoot + alphaZeroLiftKink * kKink + alphaZeroLiftTip * kTip;

				meanAirfoil.getAerodynamics().set_alphaZeroLift(
						Amount.valueOf(
								(alphaZeroLiftMeanAirfoil), SI.RADIAN));

				//CL_ALPHA
				clAplhaRoot = airfoilRoot.getAerodynamics().get_clAlpha();
				clAplhaKink = airfoilKink.getAerodynamics().get_clAlpha();
				clAplhaTip = airfoilTip.getAerodynamics().get_clAlpha();

				double clAlphaMeanAirfoil = clAplhaRoot * kRoot + clAplhaKink * kKink + clAplhaTip * kTip;

				meanAirfoil.getAerodynamics().set_clAlpha(clAlphaMeanAirfoil);

				//CL STAR
				clStarRoot = airfoilRoot.getAerodynamics().get_clStar();
				clStarKink = airfoilKink.getAerodynamics().get_clStar();
				clStarTip = airfoilTip.getAerodynamics().get_clStar();

				double clStarMeanAirfoil = clStarRoot * kRoot + clStarKink * kKink + clStarTip * kTip;

				meanAirfoil.getAerodynamics().set_clStar(clStarMeanAirfoil);

				//ALPHA MAX
				alphaMaxRoot = airfoilRoot.getAerodynamics().get_alphaStall().getEstimatedValue();
				alphaMaxKink = airfoilKink.getAerodynamics().get_alphaStall().getEstimatedValue();
				alphaMaxTip = airfoilTip.getAerodynamics().get_alphaStall().getEstimatedValue();

				double alphaMaxMeanAirfoil = alphaMaxRoot * kRoot + alphaMaxKink * kKink + alphaMaxTip * kTip;

				meanAirfoil.getAerodynamics().set_alphaStall(
						Amount.valueOf(
								(alphaMaxMeanAirfoil), SI.RADIAN));

				//CL MAX
				clMaxRoot = airfoilRoot.getAerodynamics().get_clMax();
				clMaxKink = airfoilKink.getAerodynamics().get_clMax();
				clMaxTip = airfoilTip.getAerodynamics().get_clMax();

				double clMaxMeanAirfoil = clMaxRoot * kRoot + clMaxKink * kKink + clMaxTip * kTip;

				meanAirfoil.getAerodynamics().set_clMax(clMaxMeanAirfoil);

				//CD MIN
				cdMinRoot = airfoilRoot.getAerodynamics().get_cdMin();
				cdMinKink = airfoilKink.getAerodynamics().get_cdMin();
				cdMinTip = airfoilTip.getAerodynamics().get_cdMin();

				double cdMinMeanAirfoil = cdMinRoot * kRoot + cdMinKink * kKink + cdMinTip * kTip;

				meanAirfoil.getAerodynamics().set_cdMin(cdMinMeanAirfoil);

				//CL AT CD MIN
				cl_cdMinRoot = airfoilRoot.getAerodynamics().get_clAtCdMin();
				cl_cdMinKink = airfoilKink.getAerodynamics().get_clAtCdMin();
				cl_cdMinTip = airfoilTip.getAerodynamics().get_clAtCdMin();

				double cl_cdMinMeanAirfoil = cl_cdMinRoot * kRoot + cl_cdMinKink * kKink + cl_cdMinTip * kTip;

				meanAirfoil.getAerodynamics().set_clAtCdMin(cl_cdMinMeanAirfoil);

				//K FACTOR DRAG POLAR
				kDragPolarRoot = airfoilRoot.getAerodynamics().get_kFactorDragPolar();
				kDragPolarKink = airfoilKink.getAerodynamics().get_kFactorDragPolar();
				kDragPolarTip = airfoilTip.getAerodynamics().get_kFactorDragPolar();

				double kDragPolarMeanAirfoil = kDragPolarRoot * kRoot + kDragPolarKink * kKink + kDragPolarTip * kTip;

				meanAirfoil.getAerodynamics().set_kFactorDragPolar(kDragPolarMeanAirfoil);

				//Xac
				x_acRoot = airfoilRoot.getAerodynamics().get_aerodynamicCenterX();
				x_acKink = airfoilKink.getAerodynamics().get_aerodynamicCenterX();
				x_acTip = airfoilTip.getAerodynamics().get_aerodynamicCenterX();

				double x_acMeanAirfoil = x_acRoot * kRoot + x_acKink * kKink + x_acTip * kTip;

				meanAirfoil.getAerodynamics().set_aerodynamicCenterX(x_acMeanAirfoil);

				//CMac
				cm_acRoot = airfoilRoot.getAerodynamics().get_cmAC();
				cm_acKink = airfoilKink.getAerodynamics().get_cmAC();
				cm_acTip = airfoilTip.getAerodynamics().get_cmAC();

				double cm_acMeanAirfoil = cm_acRoot * kRoot + cm_acKink * kKink + cm_acTip * kTip;

				meanAirfoil.getAerodynamics().set_cmAC(cm_acMeanAirfoil);

				//CMac_Stall
				cm_ac_StallRoot = airfoilRoot.getAerodynamics().get_cmACStall();
				cm_ac_StallKink = airfoilKink.getAerodynamics().get_cmACStall();
				cm_ac_StallTip = airfoilTip.getAerodynamics().get_cmACStall();

				double cm_acStallMeanAirfoil = cm_ac_StallRoot * kRoot + cm_ac_StallKink * kKink + cm_ac_StallTip * kTip;

				meanAirfoil.getAerodynamics().set_cmACStall(cm_acStallMeanAirfoil);

				//CM ALPHA LE
				cmAlpha_acRoot = airfoilRoot.getAerodynamics().get_cmAlphaAC();
				cmalpha_acKink = airfoilKink.getAerodynamics().get_cmAlphaAC();
				cmAlpha_acTip = airfoilTip.getAerodynamics().get_cmAlphaAC();

				double cmAlpha_acMeanAirfoil = cmAlpha_acRoot * kRoot + cmalpha_acKink * kKink + cmAlpha_acTip * kTip;

				meanAirfoil.getAerodynamics().set_cmAlphaAC(cmAlpha_acMeanAirfoil);

				//REYNOLDS CRUISE
				reynoldsCruiseRoot = airfoilRoot.getAerodynamics().get_reynoldsCruise();
				reynoldsCruiseKink = airfoilKink.getAerodynamics().get_reynoldsCruise();
				reynoldsCruiseTip = airfoilTip.getAerodynamics().get_reynoldsCruise();

				double reynoldsCruiseMeanAirfoil = reynoldsCruiseRoot * kRoot + reynoldsCruiseKink * kKink + reynoldsCruiseTip * kTip;

				meanAirfoil.getAerodynamics().set_reynoldsCruise(reynoldsCruiseMeanAirfoil);

				//REYNOLDS STALL
				reynoldsStallRoot = airfoilRoot.getAerodynamics().get_reynoldsNumberStall();
				reynoldsStallKink = airfoilKink.getAerodynamics().get_reynoldsNumberStall();
				reynoldsStallTip = airfoilTip.getAerodynamics().get_reynoldsNumberStall();

				double reynoldsStallMeanAirfoil = reynoldsStallRoot * kRoot + reynoldsStallKink * kKink + reynoldsStallTip * kTip;

				meanAirfoil.getAerodynamics().set_reynoldsNumberStall(reynoldsStallMeanAirfoil);

				//TWIST
				twistRoot = airfoilRoot.getGeometry().get_twist().getEstimatedValue();
				twistKink = airfoilKink.getGeometry().get_twist().getEstimatedValue();
				twistTip = airfoilTip.getGeometry().get_twist().getEstimatedValue();

				double twistMeanAirfoil = twistRoot * kRoot + twistKink * kKink + twistTip * kTip;

				meanAirfoil.getGeometry().set_twist(
						Amount.valueOf(
								(twistMeanAirfoil), SI.RADIAN));

				//PHI_TE
				phi_TERoot = airfoilRoot.getGeometry().get_anglePhiTE().getEstimatedValue();
				phi_TEKink = airfoilKink.getGeometry().get_anglePhiTE().getEstimatedValue();
				phi_TETip = airfoilTip.getGeometry().get_anglePhiTE().getEstimatedValue();

				double phi_TEMeanAirfoil = phi_TERoot * kRoot + phi_TEKink * kKink + phi_TETip * kTip;

				meanAirfoil.getGeometry().set_anglePhiTE(
						Amount.valueOf(
								(phi_TEMeanAirfoil), SI.RADIAN));

				//RADIUS LE
				radius_LERoot = airfoilRoot.getGeometry().get_radiusLE();
				radius_LEKink = airfoilKink.getGeometry().get_radiusLE();
				radius_LETip = airfoilTip.getGeometry().get_radiusLE();

				double radius_LEMeanAirfoil = radius_LERoot * kRoot + radius_LEKink * kKink + radius_LETip * kTip;

				meanAirfoil.getGeometry().set_radiusLE(radius_LEMeanAirfoil);

				//THICKNESS OVER CHORD UNIT
				thicknessOverChordUnit_Root = airfoilRoot.getGeometry().get_thicknessOverChordUnit();
				thicknessOverChordUnit_Kink = airfoilKink.getGeometry().get_thicknessOverChordUnit();
				thicknessOverChordUnit_Tip = airfoilTip.getGeometry().get_thicknessOverChordUnit();

				double thicknessOverChordUnit_MeanAirfoil = thicknessOverChordUnit_Root * kRoot + thicknessOverChordUnit_Kink * kKink + thicknessOverChordUnit_Tip * kTip;

				meanAirfoil.getGeometry().set_thicknessOverChordUnit(thicknessOverChordUnit_MeanAirfoil);

				//MAX THICKNESS OVER CHORD
				maxThicknessOverChord_Root = airfoilRoot.getGeometry().get_maximumThicknessOverChord();
				maxThicknessOverChord_Kink = airfoilKink.getGeometry().get_maximumThicknessOverChord();
				maxThicknessOverChord_Tip = airfoilTip.getGeometry().get_maximumThicknessOverChord();

				double maxThicknessOverChord_MeanAirfoil = maxThicknessOverChord_Root * kRoot + maxThicknessOverChord_Kink * kKink + maxThicknessOverChord_Tip * kTip;

				meanAirfoil.getGeometry().set_thicknessOverChordUnit(maxThicknessOverChord_MeanAirfoil);

				//ALPHA STAR 

				alphaStarRoot = airfoilRoot.getAerodynamics().get_alphaStar().getEstimatedValue();
				alphaStarKink = airfoilKink.getAerodynamics().get_alphaStar().getEstimatedValue();
				alphaStarTip = airfoilTip.getAerodynamics().get_alphaStar().getEstimatedValue();

				double alphaStarMeanAirfoil = alphaStarRoot * kRoot + alphaStarKink * kKink + alphaStarTip * kTip;

				meanAirfoil.getAerodynamics().set_alphaStar(
						Amount.valueOf(
								(alphaStarMeanAirfoil), SI.RADIAN));



				//LEADING EDGE SHARPNESS PARAMETER

				double LESharpnessParameterRoot = airfoilRoot.getGeometry().get_deltaYPercent();
				double LESharpnessParameterKink =  airfoilKink.getGeometry().get_deltaYPercent();
				double LESharpnessParameterTip =  airfoilTip.getGeometry().get_deltaYPercent();

				double meanLESharpParam =LESharpnessParameterRoot * kRoot + LESharpnessParameterKink *  kKink +
						LESharpnessParameterTip * kTip;

				meanAirfoil.getGeometry().set_deltaYPercent(meanLESharpParam);

			}

			if ( theLiftingSurface.get_type() == ComponentEnum.HORIZONTAL_TAIL ){

				MyAirfoil airfoilRoot = theWing.get_theAirfoilsList().get(0);
				MyAirfoil airfoilTip = theWing.get_theAirfoilsList().get(1);

				rootChord = theWing.get_chordRoot().getEstimatedValue();
				tipChord = theWing.get_chordTip().getEstimatedValue();

				kRoot = rootChord /(rootChord + tipChord);
				kTip = tipChord /(rootChord + tipChord);



				//ALPHA ZERO LIFT
				alphaZeroLiftRoot = airfoilRoot.getAerodynamics().get_alphaZeroLift().getEstimatedValue();

				alphaZeroLiftTip = airfoilTip.getAerodynamics().get_alphaZeroLift().getEstimatedValue();

				double alphaZeroLiftMeanAirfoil = alphaZeroLiftRoot * alphaZeroLiftTip * kTip;

				meanAirfoil.getAerodynamics().set_alphaZeroLift(
						Amount.valueOf(
								(alphaZeroLiftMeanAirfoil), SI.RADIAN));

				//CL_ALPHA
				clAplhaRoot = airfoilRoot.getAerodynamics().get_clAlpha();
				clAplhaTip = airfoilTip.getAerodynamics().get_clAlpha();

				double clAlphaMeanAirfoil = clAplhaRoot * kRoot + clAplhaTip * kTip;

				meanAirfoil.getAerodynamics().set_clAlpha(clAlphaMeanAirfoil);

				//CL STAR
				clStarRoot = airfoilRoot.getAerodynamics().get_clStar();
				clStarTip = airfoilTip.getAerodynamics().get_clStar();

				double clStarMeanAirfoil = clStarRoot * kRoot  + clStarTip * kTip;

				meanAirfoil.getAerodynamics().set_clStar(clStarMeanAirfoil);

				//ALPHA MAX
				alphaMaxRoot = airfoilRoot.getAerodynamics().get_alphaStall().getEstimatedValue();
				alphaMaxTip = airfoilTip.getAerodynamics().get_alphaStall().getEstimatedValue();

				double alphaMaxMeanAirfoil = alphaMaxRoot * kRoot + alphaMaxTip * kTip;

				meanAirfoil.getAerodynamics().set_alphaStall(
						Amount.valueOf(
								(alphaMaxMeanAirfoil), SI.RADIAN));

				//CL MAX
				clMaxRoot = airfoilRoot.getAerodynamics().get_clMax();
				clMaxTip = airfoilTip.getAerodynamics().get_clMax();

				double clMaxMeanAirfoil = clMaxRoot * kRoot + clMaxTip * kTip;

				meanAirfoil.getAerodynamics().set_clMax(clMaxMeanAirfoil);

				//CD MIN
				cdMinRoot = airfoilRoot.getAerodynamics().get_cdMin();
				cdMinTip = airfoilTip.getAerodynamics().get_cdMin();

				double cdMinMeanAirfoil = cdMinRoot * kRoot + cdMinTip * kTip;

				meanAirfoil.getAerodynamics().set_cdMin(cdMinMeanAirfoil);

				//CL AT CD MIN
				cl_cdMinRoot = airfoilRoot.getAerodynamics().get_clAtCdMin();
				cl_cdMinTip = airfoilTip.getAerodynamics().get_clAtCdMin();

				double cl_cdMinMeanAirfoil = cl_cdMinRoot * kRoot + cl_cdMinTip * kTip;

				meanAirfoil.getAerodynamics().set_clAtCdMin(cl_cdMinMeanAirfoil);

				//K FACTOR DRAG POLAR
				kDragPolarRoot = airfoilRoot.getAerodynamics().get_kFactorDragPolar();
				kDragPolarTip = airfoilTip.getAerodynamics().get_kFactorDragPolar();

				double kDragPolarMeanAirfoil = kDragPolarRoot * kRoot + kDragPolarTip * kTip;

				meanAirfoil.getAerodynamics().set_kFactorDragPolar(kDragPolarMeanAirfoil);

				//Xac
				x_acRoot = airfoilRoot.getAerodynamics().get_aerodynamicCenterX();
				x_acTip = airfoilTip.getAerodynamics().get_aerodynamicCenterX();

				double x_acMeanAirfoil = x_acRoot * kRoot + x_acTip * kTip;

				meanAirfoil.getAerodynamics().set_aerodynamicCenterX(x_acMeanAirfoil);

				//CMac
				cm_acRoot = airfoilRoot.getAerodynamics().get_cmAC();
				cm_acTip = airfoilTip.getAerodynamics().get_cmAC();

				double cm_acMeanAirfoil = cm_acRoot * kRoot + cm_acTip * kTip;

				meanAirfoil.getAerodynamics().set_cmAC(cm_acMeanAirfoil);

				//CMac_Stall
				cm_ac_StallRoot = airfoilRoot.getAerodynamics().get_cmACStall();
				cm_ac_StallTip = airfoilTip.getAerodynamics().get_cmACStall();

				double cm_acStallMeanAirfoil = cm_ac_StallRoot * kRoot + cm_ac_StallTip * kTip;

				meanAirfoil.getAerodynamics().set_cmACStall(cm_acStallMeanAirfoil);

				//CM ALPHA LE
				cmAlpha_acRoot = airfoilRoot.getAerodynamics().get_cmAlphaAC();
				cmAlpha_acTip = airfoilTip.getAerodynamics().get_cmAlphaAC();

				double cmAlpha_acMeanAirfoil = cmAlpha_acRoot * kRoot + cmAlpha_acTip * kTip;

				meanAirfoil.getAerodynamics().set_cmAlphaAC(cmAlpha_acMeanAirfoil);

				//REYNOLDS CRUISE
				reynoldsCruiseRoot = airfoilRoot.getAerodynamics().get_reynoldsCruise();
				reynoldsCruiseTip = airfoilTip.getAerodynamics().get_reynoldsCruise();

				double reynoldsCruiseMeanAirfoil = reynoldsCruiseRoot * kRoot + reynoldsCruiseTip * kTip;

				meanAirfoil.getAerodynamics().set_reynoldsCruise(reynoldsCruiseMeanAirfoil);

				//REYNOLDS STALL
				reynoldsStallRoot = airfoilRoot.getAerodynamics().get_reynoldsNumberStall();
				reynoldsStallTip = airfoilTip.getAerodynamics().get_reynoldsNumberStall();

				double reynoldsStallMeanAirfoil = reynoldsStallRoot * kRoot + reynoldsStallTip * kTip;

				meanAirfoil.getAerodynamics().set_reynoldsNumberStall(reynoldsStallMeanAirfoil);

				//TWIST
				twistRoot = airfoilRoot.getGeometry().get_twist().getEstimatedValue();
				twistTip = airfoilTip.getGeometry().get_twist().getEstimatedValue();

				double twistMeanAirfoil = twistRoot * kRoot + twistTip * kTip;

				meanAirfoil.getGeometry().set_twist(
						Amount.valueOf(
								(twistMeanAirfoil), SI.RADIAN));

				//PHI_TE
				phi_TERoot = airfoilRoot.getGeometry().get_anglePhiTE().getEstimatedValue();
				phi_TETip = airfoilTip.getGeometry().get_anglePhiTE().getEstimatedValue();

				double phi_TEMeanAirfoil = phi_TERoot * kRoot + phi_TETip * kTip;

				meanAirfoil.getGeometry().set_anglePhiTE(
						Amount.valueOf(
								(phi_TEMeanAirfoil), SI.RADIAN));

				//RADIUS LE
				radius_LERoot = airfoilRoot.getGeometry().get_radiusLE();
				radius_LETip = airfoilTip.getGeometry().get_radiusLE();

				double radius_LEMeanAirfoil = radius_LERoot * kRoot + radius_LETip * kTip;

				meanAirfoil.getGeometry().set_radiusLE(radius_LEMeanAirfoil);

				//THICKNESS OVER CHORD UNIT
				thicknessOverChordUnit_Root = airfoilRoot.getGeometry().get_thicknessOverChordUnit();
				thicknessOverChordUnit_Tip = airfoilTip.getGeometry().get_thicknessOverChordUnit();

				double thicknessOverChordUnit_MeanAirfoil = thicknessOverChordUnit_Root * kRoot + thicknessOverChordUnit_Tip * kTip;

				meanAirfoil.getGeometry().set_thicknessOverChordUnit(thicknessOverChordUnit_MeanAirfoil);

				//MAX THICKNESS OVER CHORD
				maxThicknessOverChord_Root = airfoilRoot.getGeometry().get_maximumThicknessOverChord();
				maxThicknessOverChord_Tip = airfoilTip.getGeometry().get_maximumThicknessOverChord();

				double maxThicknessOverChord_MeanAirfoil = maxThicknessOverChord_Root * kRoot + maxThicknessOverChord_Tip * kTip;

				meanAirfoil.getGeometry().set_thicknessOverChordUnit(maxThicknessOverChord_MeanAirfoil);

				//ALPHA STAR 

				alphaStarRoot = airfoilRoot.getAerodynamics().get_alphaStar().getEstimatedValue();
				alphaStarTip = airfoilTip.getAerodynamics().get_alphaStar().getEstimatedValue();

				double alphaStarMeanAirfoil = alphaStarRoot * kRoot + alphaStarTip * kTip;

				meanAirfoil.getAerodynamics().set_alphaStar(
						Amount.valueOf(
								(alphaStarMeanAirfoil), SI.RADIAN));



				//LEADING EDGE SHARPNESS PARAMETER

				double LESharpnessParameterRoot = airfoilRoot.getGeometry().get_deltaYPercent();
				double LESharpnessParameterTip =  airfoilTip.getGeometry().get_deltaYPercent();

				double meanLESharpParam =LESharpnessParameterRoot * kRoot + LESharpnessParameterTip * kTip;

				meanAirfoil.getGeometry().set_deltaYPercent(meanLESharpParam);


			}

			return meanAirfoil;

		}
	}




	/**
	 * This function calculates the characteristics of an intermediate airfoil.
	 * 
	 * @author Manuela Ruocco
	 * @param Airfoilroot
	 * @param Airfoilkink
	 * @param Airfoiltip
	 * @param Dimensional station where the airfoil is located.
	 */ 

	public static MyAirfoil calculateIntermediateAirfoil (LiftingSurface theWing, double yLoc){

		MyAirfoil intermediateAirfoil = new MyAirfoil(theWing);

		//			System.out.println( "---------------------------------------");
		//			System.out.println( "STARTING EVALUATION OF INTERMEDIATE AIRFOIL");
		//			System.out.println( "---------------------------------------");
		//			System.out.println( " The position of arifoil is --> " + yLoc);

		rootChord = theWing.get_chordRoot().getEstimatedValue();
		kinkChord = theWing.get_chordKink().getEstimatedValue();
		tipChord = theWing.get_chordTip().getEstimatedValue();
		dimensionalKinkStation = theWing.get_spanStationKink()*theWing.get_semispan().getEstimatedValue();
		dimensionalOverKink = theWing.get_semispan().getEstimatedValue() - dimensionalKinkStation;
		//			MyAirfoil airfoilRoot = theWing.get_theAirfoilsList().get(0);
		//			MyAirfoil airfoilKink = theWing.get_theAirfoilsList().get(1);
		//			MyAirfoil airfoilTip = theWing.get_theAirfoilsList().get(2);


		// ETA
		intermediateEta = MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_etaAirfoil().toArray(), yLoc);
		intermediateAirfoil.getGeometry().set_etaLocation(intermediateEta);

		// TWIST
		intermediateTwist = MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_twistVsY().toArray(), yLoc);
		intermediateAirfoil.getGeometry().set_twist(Amount.valueOf((intermediateTwist), SI.RADIAN));

		// CL MAX
		intermediateClMax = MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_clMaxVsY().toArray(), yLoc);
		intermediateAirfoil.getAerodynamics().set_clMax(intermediateClMax);

		// CHORD
		intermediateAirfoil.getGeometry().update(yLoc);

		// ALFA ZERO LIFT
		intermediateAlphaZL = MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_alpha0VsY().toArray(), yLoc);
		intermediateAirfoil.getAerodynamics().set_alphaZeroLift(Amount.valueOf((intermediateAlphaZL), SI.RADIAN));

		//CL ALPHA
		intermediateClAlpha = MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_clAlpha_y().toArray(), yLoc);
		intermediateAirfoil.getAerodynamics().set_clAlpha(intermediateClAlpha);

		// ALFA STAR
		intermediateAlphaStar = MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_alphaStar_y().toArray(), yLoc);
		intermediateAirfoil.getAerodynamics().set_alphaStar((Amount.valueOf((intermediateAlphaStar), SI.RADIAN)));

		//ALFA STALL
		intermediateAlphaStall = MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_alphaStall().toArray(), yLoc);
		intermediateAirfoil.getAerodynamics().set_alphaStall((Amount.valueOf((intermediateAlphaStall), SI.RADIAN)));

		// CL STAR 
		intermediateClStar =  MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_clStar_y().toArray(), yLoc);
		intermediateAirfoil.getAerodynamics().set_clStar(intermediateClStar);

		// CL MAX SWEEP
		//intermediateClMaxSweep =  MyMathUtils.getInterpolatedValue1DLinear(
		//	theWing.get_yStationsAirfoil().toArray(),theWing.get_clMaxSweep_y().toArray(), yLoc);
		//intermediateAirfoil.getAerodynamics().set_clMaxSweep(intermediateClMaxSweep);

		// CL AT CD MIN
		intermediateClatMinCD =  MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_clAtCdMin_y().toArray(), yLoc);
		intermediateAirfoil.getAerodynamics().set_clAtCdMin(intermediateClatMinCD);

		// CD MIN
		intermediateCdMin =   MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_cdMin_y().toArray(), yLoc);
		intermediateAirfoil.getAerodynamics().set_cdMin(intermediateCdMin);

		// CM AC
		intermediateCm = MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_cmAC_y().toArray(), yLoc);
		intermediateAirfoil.getAerodynamics().set_cmAC(intermediateCm );

		// CM AT LE
		intermediateCmAlphaLE = MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_cmAlphaLE_y().toArray(), yLoc);
		intermediateAirfoil.getAerodynamics().set_cmAlphaLE(intermediateCmAlphaLE);

		// AERODYNAMIC CENTRE X CHOORD
		intermediateAerodynamicCentre = MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_aerodynamicCenterXcoord_y().toArray(), yLoc);
		intermediateAirfoil.getAerodynamics().set_aerodynamicCenterX(intermediateAerodynamicCentre);

		// MAX THICKNESS
		intermediateMaxThickness = MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_maxThicknessVsY().toArray(), yLoc);
		intermediateAirfoil.getGeometry().set_maximumThicknessOverChord(intermediateMaxThickness);	

		// K FACTOR DRAG POLAR
		intermediatekFactorPolar = MyMathUtils.getInterpolatedValue1DLinear(
				theWing.get_yStationsAirfoil().toArray(),theWing.get_kFactorDragPolar_y().toArray(), yLoc);
		intermediateAirfoil.getAerodynamics().set_kFactorDragPolar(intermediatekFactorPolar);


		return intermediateAirfoil;

	}


	public double getcLLinearSlopeNB() {
		CalcCLAlpha theLineraSlopeCalculator = new CalcCLAlpha();
		CalcCLAtAlpha theCLCalculator = new CalcCLAtAlpha();
		double cLLinearSlopeNB = theLineraSlopeCalculator.nasaBlackwell(theCLCalculator);
		return cLLinearSlopeNB;
	}


	public void setcLLinearSlopeNB(double cLLinearSlope) {
		this.cLLinearSlope = cLLinearSlope;
	}


	public Amount<Angle> get_alphaCurrent() {
		return _alphaCurrent;
	}


	public void set_alphaCurrent(Amount<Angle> _alphaCurrent) {
		this._alphaCurrent = _alphaCurrent;
	}

	public CalcCLAlpha getCalculateCLAlpha() {
		return calculateCLAlpha;
	}

	public CalcCLvsAlphaCurve getCalculateCLvsAlphaCurve() {
		return calculateCLvsAlphaCurve;
	}

	public CalcAlpha0L getCalculateAlpha0L() {
		return calculateAlpha0L;
	}

	public CalcCmAC getCalculateCmAC() {
		return calculateCmAC;
	}

	public Integer get_numberOfAlpha() {
		return _numberOfAlpha;
	}

	public void setNumberOfAlpha(Integer numberOfAlpha) {
		this._numberOfAlpha = numberOfAlpha;
	}

	public MyArray getAlphaArray() {
		return alphaArray;
	}

	public MyArray getcLArray() {
		return cLArray;
	}

	public CalcXAC getCalculateXAC() {
		return calculateXAC;
	}

	public AirfoilTypeEnum get_airfoilType() {
		return _airfoilType;
	}

	public void set_airfoilType(AirfoilTypeEnum _airfoilType) {
		this._airfoilType = _airfoilType;
	}

	public CalcMachCr getCalculateMachCr() {
		return calculateMachCr;
	}

	public CalcCdWaveDrag getCalculateCdWaveDrag() {
		return calculateCdWaveDrag;
	}

	public CalcLiftDistribution getCalculateLiftDistribution() {
		return calculateLiftDistribution;
	}

	public CalcCLAtAlpha getCalculateCLAtAlpha() {
		return calculateCLAtAlpha;
	}

	public CoefficientWrapper getcLMap() {
		return cLMap;
	}

	public double[] get_yStations() {
		return _yStations;
	}

	public CalcCmAlpha getCalculateCmAlpha() {
		return calculateCmAlpha;
	}

	public double get_dynamicPressureRatio() {
		return _dynamicPressureRatio;
	}

	public void set_dynamicPressureRatio(double _dynamicPressureRatio) {
		this._dynamicPressureRatio = _dynamicPressureRatio;
	}

	public MyArray get_clTotalVsY() {
		return _clTotalVsY;
	}

	public CalcBasicLoad getCalculateBasicLoadDistribution() {
		return calculateBasicLoadDistribution;
	}

	public double[] get_yStationsND() {
		return _yStationsND;
	}

	public Double get_reynolds() {
		return _reynolds;
	}


	public Double get_cD0Parasite() {
		return _cd0Parasite;
	}


	public Double get_cdWFInterf() {
		return _cdWFInterf;
	}


	public Double get_cdWNInterf() {
		return _cdWNInterf;
	}


	public Double get_cdGap() {
		return _cdGap;
	}


	public Double get_cL() {
		return _cL;
	}


	public Double get_compressibilityFactor() {
		return _compressibilityFactor;
	}

	public Double get_cF() {
		return _cF;
	}

	public Double get_cD0Total() {
		return _cD0Total;
	}

	public Double get_cLAlpha() {
		return _cLAlpha;
	}

	public Double get_cLCurrent() {
		return _cLCurrent;
	}

	public void set_cLCurrent(Double _cLCurrent) {
		this._cLCurrent = _cLCurrent;
	}

	public Double get_cDw() {
		return _cDw;
	}

	public Amount<Angle> get_alpha0L() {
		return _alpha0L;
	}

	public void set_alpha0L(Amount<Angle> _alphaZeroLift) {
		this._alpha0L = _alphaZeroLift;
	}

	public Amount<Angle> get_alphaZeroLiftInnerPanel() {
		return _alphaZeroLiftInnerPanel;
	}

	public void set_alphaZeroLiftInnerPanel(Amount<Angle> _alphaZeroLiftInnerPanel) {
		this._alphaZeroLiftInnerPanel = _alphaZeroLiftInnerPanel;
	}

	public Amount<Angle> get_alphaZeroLiftOuterPanel() {
		return _alphaZeroLiftOuterPanel;
	}

	public void set_alphaZeroLiftOuterPanel(Amount<Angle> _alphaZeroLiftOuterPanel) {
		this._alphaZeroLiftOuterPanel = _alphaZeroLiftOuterPanel;
	}

	public Double get_liftCoefficientGradient() {
		return _liftCoefficientGradient;
	}

	public void set_liftCoefficientGradient(Double _liftCoefficientGradient) {
		this._liftCoefficientGradient = _liftCoefficientGradient;
	}

	public Double get_liftCoefficientGradientInnerPanel() {
		return _liftCoefficientGradientInnerPanel;
	}

	public void set_liftCoefficientGradientInnerPanel(
			Double _liftCoefficientGradientInnerPanel) {
		this._liftCoefficientGradientInnerPanel = _liftCoefficientGradientInnerPanel;
	}

	public Double get_liftCoefficientGradientOuterPanel() {
		return _liftCoefficientGradientOuterPanel;
	}

	public void set_liftCoefficientGradientOuterPanel(
			Double _liftCoefficientGradientOuterPanel) {
		this._liftCoefficientGradientOuterPanel = _liftCoefficientGradientOuterPanel;
	}

	public Double get_pitchCoefficientAC() {
		return _pitchCoefficientAC;
	}

	public void set_pitchCoefficientAC(Double _pitchCoefficientAC) {
		this._pitchCoefficientAC = _pitchCoefficientAC;
	}

	public Double get_pitchCoefficientACInnerPanel() {
		return _pitchCoefficientACInnerPanel;
	}

	public void set_pitchCoefficientACInnerPanel(
			Double _pitchCoefficientACInnerPanel) {
		this._pitchCoefficientACInnerPanel = _pitchCoefficientACInnerPanel;
	}

	public Double get_pitchCoefficientACOuterPanel() {
		return _pitchCoefficientACOuterPanel;
	}

	public void set_pitchCoefficientACOuterPanel(
			Double _pitchCoefficientACOuterPanel) {
		this._pitchCoefficientACOuterPanel = _pitchCoefficientACOuterPanel;
	}

	public Amount<Angle> get_alphaStar() {
		return _alphaStar;
	}

	public void set_alphaStar(Amount<Angle> _alphaStar) {
		this._alphaStar = _alphaStar;
	}

	public Amount<Angle> get_alphaStall() {
		return _alphaStall;
	}

	public void set_alphaStall(Amount<Angle> _alphaStall) {
		this._alphaStall = _alphaStall;
	}

	public Double get_liftCoefficientMax() {
		return _liftCoefficientMax;
	}

	public void set_liftCoefficientMax(Double _liftCoefficientMax) {
		this._liftCoefficientMax = _liftCoefficientMax;
	}

	public Double get_rollCoefficientGradient() {
		return _rollCoefficientGradient;
	}

	public void set_rollCoefficientGradient(Double _rollCoefficientGradient) {
		this._rollCoefficientGradient = _rollCoefficientGradient;
	}

	public Double get_pitchCoefficientGradient() {
		return _pitchCoefficientGradient;
	}

	public void set_pitchCoefficientGradient(Double _pitchCoefficientGradient) {
		this._pitchCoefficientGradient = _pitchCoefficientGradient;
	}

	public Double get_yawCoefficientGradient() {
		return _yawCoefficientGradient;
	}

	public void set_yawCoefficientGradient(Double _yawCoefficientGradient) {
		this._yawCoefficientGradient = _yawCoefficientGradient;
	}

	public Double get_machTransonicThreshold() {
		return _machTransonicThreshold;
	}


	public void set_machCompressibilityThreshold(
			Double _machCompressibilityThreshold) {
		this._machTransonicThreshold = _machCompressibilityThreshold;
	}

	public Amount<Angle> get_alphaRootCurrent() {
		return _alphaRootCurrent;
	}


	public void set_alphaRootCurrent(Amount<Angle> _alphaCurrent) {
		this._alphaRootCurrent = _alphaCurrent;
	}

	public Amount<Angle> getAlphaStart() {
		return alphaStart;
	}


	public Amount<Angle> getAlphaEnd() {
		return alphaEnd;
	}

	@Override
	public void calculateAll() {
		// TODO Auto-generated method stub

	}

	public CalcCLMaxClean getCalculateCLMaxClean() {
		return calculateCLMaxClean;
	}

	public LiftingSurface getTheLiftingSurface() {
		return theLiftingSurface;
	}

	public void setTheLiftingSurface(LiftingSurface theLiftingSurface) {
		this.theLiftingSurface = theLiftingSurface;
	}

	public double get_vortexSemiSpanToSemiSpanRatio() {
		return _vortexSemiSpanToSemiSpanRatio;
	}

	public void set_vortexSemiSpanToSemiSpanRatio(double _vortexSemiSpanToSemiSpanRatio) {
		this._vortexSemiSpanToSemiSpanRatio = _vortexSemiSpanToSemiSpanRatio;
	}

	public AerodynamicDatabaseReader get_AerodynamicDatabaseReader() {
		return _aerodynamicDatabaseReader;
	}

	public void set_AerodynamicDatabaseReader(AerodynamicDatabaseReader _aerodynamicDatabaseReader) {

		this._aerodynamicDatabaseReader = _aerodynamicDatabaseReader;
	}

	public HighLiftDatabaseReader getHighLiftDatabaseReader() {
		return _highLiftDatabaseReader;
	}

	public void setHighLiftDatabaseReader(HighLiftDatabaseReader _highLiftDatabaseReader) {
		this._highLiftDatabaseReader = _highLiftDatabaseReader;
	}
	public MyArray get_alpha0lDistribution() {
		return _alpha0lDistribution;
	}

	public void set_alpha0lDistribution(MyArray _alpha0lDistribution) {
		this._alpha0lDistribution = _alpha0lDistribution;
	}
	public double[] getCdDistributionNasaBlackwell() {
		return cdDistributionNasaBlackwell;
	}

	public void setCdDistributionNasaBlackwell(double[] cdDistributionNasaBlackwell) {
		this.cdDistributionNasaBlackwell = cdDistributionNasaBlackwell;
	}

	public int get_nPointsSemispanWise() {
		return _nPointsSemispanWise;
	}
	public Double get_cLMaxClean() {
		return _cLMaxClean;
	}


	public void set_cLMaxClean(Double _cLMaxClean) {
		this._cLMaxClean = _cLMaxClean;
	}
	public Amount<Angle> get_alphaMaxClean() {
		return _alphaMaxClean;
	}


	public void set_alphaMaxClean(Amount<Angle> _alphaMaxClean) {
		this._alphaMaxClean = _alphaMaxClean;
	}

	public double[] get_alphaArrayPlot(){
		return alphaArrayPlot;
	}

	public double[] get_cLArrayPlot(){
		return cLArrayPlot;
	}

	public void setDatabaseReaders(Pair... args) {
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);

		for (Pair a : args) {
			DatabaseReaderEnum key = (DatabaseReaderEnum)a.getKey(); 
			String databaseFileName = (String)a.getValue();

			switch (key) {
			case AERODYNAMIC:
				_aerodynamicDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath, databaseFileName); 
				listDatabaseReaders.add(_aerodynamicDatabaseReader);
				if( theAircraft!= null)
					theAircraft.get_theAerodynamics().set_aerodynamicDatabaseReader(_aerodynamicDatabaseReader);
				break;

			case HIGHLIFT:
				_highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, databaseFileName); 
				listDatabaseReaders.add(_highLiftDatabaseReader);
				if( theAircraft!= null)
					theAircraft.get_theAerodynamics().set_highLiftDatabaseReader(_highLiftDatabaseReader);
				break;	

			}

			/*
			 * TODO: manage other types of database reader
			 * 
			case ENGINE_TURBOFAN:
				listDatabaseReaders.add(
						new TurbofanEngineDatabaseReader(databaseFolderPath, databaseFileName)
						);
				break;
			case ENGINE_TURBOPROP:
				listDatabaseReaders.add(
						new TurbopropEngineDatabaseReader(databaseFolderPath, databaseFileName)
						);
				break;
			 */
		}

	}


	public double getcLAlphaZero() {
		return cLAlphaZero;
	}


	public void setcLAlphaZero(double cLAlphaZero) {
		this.cLAlphaZero = cLAlphaZero;
	}

	public double getcLStarWing() {
		return cLStarWing;
	}


	public void setcLStarWing(double cLStarWing) {
		this.cLStarWing = cLStarWing;
	}


	public double getAlphaZeroLiftWingClean() {
		return alphaZeroLiftWingClean;
	}


	public void setAlphaZeroLiftWingClean(double alphaZeroLiftWingClean) {
		this.alphaZeroLiftWingClean = alphaZeroLiftWingClean;
	}


	public OperatingConditions getTheOperatingConditions() {
		return theOperatingConditions;
	}


	public void setTheOperatingConditions(OperatingConditions theOperatingConditions) {
		this.theOperatingConditions = theOperatingConditions;
	}


	public double[] getcLActualArray() {
		return cLActualArray;
	}


	public void setcLActualArray(double[] cLActualArray) {
		this.cLActualArray = cLActualArray;
	}


	public double[] getAlphaArrayActual() {
		return alphaArrayActual.toArray();
	}


	public double[] getcLActualArrayHighLift() {
		return cLActualArrayHighLift;
	}


	public double[] getAlphaArrayActualHighLift() {
		return alphaArrayActualHighLift.toArray();
	}


	public void setAlphaArrayActualHighLift(MyArray alphaArrayActualHighLift) {
		this.alphaArrayActualHighLift = alphaArrayActualHighLift;
	}


	public Amount<Angle> get_alphaStarHigLift() {
		return _alphaStarHigLift;
	}


	public void set_alphaStarHigLift(Amount<Angle> _alphaStarHigLift) {
		this._alphaStarHigLift = _alphaStarHigLift;
	}


	public void setcLActualArrayHighLift(double[] cLActualArrayHighLift) {
		this.cLActualArrayHighLift = cLActualArrayHighLift;
	}


	



}
