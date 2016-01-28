package aircraft.components.liftingSurface;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Volume;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.calculators.ACAerodynamicsManager;
import aircraft.componentmodel.AeroComponent;
import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.nacelles.Nacelle;
import configuration.enumerations.AirfoilStationEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.CenterOfGravity;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class LiftingSurface extends AeroComponent{

	String id = null;

	Fuselage _theFuselage;
	ComponentEnum _type;
	OperatingConditions _theOperatingConditions;
	Aircraft _theAircraft;

	Amount<Length> _xApexConstructionAxes = null;
	Amount<Length> _yApexConstructionAxes = null;
	Amount<Length> _zApexConstructionAxes = null;
	Double _positionRelativeToAttachment = null; // 1.0 = high wing aircraft, 0.0 = low wing

	public final int IDX_SECTION_ROOT = 0;
	public final int IDX_SECTION_KINK = 1;
	public final int IDX_SECTION_TIP = 2;
	public final int NUM_SECTIONS    = 3;

	Amount<Area> _surface = null;
	Amount<Area> _surfaceCranked = null;
	Double _aspectRatio = null;
	Double _taperRatioEquivalent = null;
	Double _taperRatioActual = null;
	Double _taperRatioOpt = null;
	Amount<Angle> _sweepQuarterChordEq = null;
	Amount<Angle> _sweepHalfChordEq = null;
	Double _spanStationKink = null;

	MyArray _chordsVsYEq = new MyArray(SI.METER);
	MyArray _chordsVsYActual = new MyArray(SI.METER);
	MyArray _eta = new MyArray(Unit.ONE);
	MyArray _yStationEq = new MyArray(SI.METER);
	MyArray _yStationActual = new MyArray(SI.METER);
	MyArray _xLEvsYActual = new MyArray(SI.METER);
	MyArray _xTEvsYActual = new MyArray(SI.METER);
	MyArray _ellChordVsY = new MyArray(SI.METER);

	//	 List<Double> _eta = new ArrayList<Double>();

	Double _extensionLERootChordLinPanel = null;
	Double _extensionTERootChordLinPanel = null;

	/** Wing root chord twist relative to body x-axis */
	Amount<Angle> _twistRootRelativeToXbody = null;
	Amount<Angle> _twistKink = null;
	Amount<Angle> _twistTip = null;

	Amount<Angle> _dihedralInnerPanel = null;
	Amount<Angle> _dihedralOuterPanel = null;
	Amount<Angle> _dihedralMean = null;
	MyArray _dihedral = new MyArray(SI.RADIAN);

	Amount<Length> 
	_span = null, 

	/**
	 * Returns the semispan value for the wing and the horizontal tail.
	 * Returns the full span if the lifting surface is a vertical tail.
	 */
	_semispan = null;


	Amount<Length> _semiSpanInnerPanel = null;   // _semi_spanKink
	Amount<Length> _semiSpanOuterPanel = null;
	Amount<Length> _chordRootEquivalentWing = null, _meanGeometricChord = null;
	Amount<Length> _chordTip = null; // 1.59 m ATR72
	Amount<Length> _chordKink = null;
	Amount<Length> _chordRoot = null; // 2.5 m ATR72
	Amount<Length> _chordLinPanel = null;
	Amount<Length> _geomChordEq     = null;
	Amount<Length> _meanAerodChordEq     = null;
	Amount<Length> _meanAerodChordActual    = null;
	Amount<Length> _xLERoot = null;
	Amount<Length> _x_LE_Root_eq = null;
	Amount<Length> _xLETip = null;
	Amount<Length> _xLEKink = null;
	Amount<Length> _xTETip = null;
	Amount<Length> _xTEKink = null;
	Amount<Length>_x_LE_Mac_Eq = null;
	Amount<Length>_y_LE_Mac_Eq = null; 
	Amount<Length>_xLEMacActualLRF = null;
	Amount<Length>_yLEMacActualLRF = null; 
	Amount<Length> _deltaXWingFus = null;

	Double _tc_root, _tc_kink, _tc_tip;
	Double _thicknessMean, _thicknessMax;
	Double _k_c_wing;
	Double _FF_wing;
	Double _xTransitionU;
	Double _xTransitionL;
	Amount<Length> _roughness;

	Double _taperRatioInnerPanel = null;
	Double _taperRatioOuterPanel = null;
	Double _aspectRatioInnerPanel = null;
	Double _aspectRatioOuterPanel = null;

	Amount<Area> _semiSurfaceInnerPanel = null;
	Amount<Area> _semiSurfaceOuterPanel = null;
	Amount<Area> _surfaceExposed = null;
	Amount<Area> _surfaceWetted = null;
	Amount<Area> _surfaceWettedExposed = null;

	Amount<Area> _semiSurfaceWettedInnerPanel = null;
	Amount<Area> _semiSurfaceWettedOuterPanel = null;

	Amount<Volume> _volume = null;
	Amount<Volume> _volumeExposed = null;
	Amount<Volume> _semiVolumeInnerPanel = null;
	Amount<Volume> _semiVolumeOuterPanel = null;

	Amount<Angle> _sweepLEEquivalent = null;
	Amount<Angle> _sweepXChordEq = null;

	Amount<Angle> _sweepTEEquivalentWing = null;
	Amount<Angle> _sweepQuarterChordInnerPanel = null;
	Amount<Angle> _sweepLEInnerPanel = null;
	Amount<Angle> _sweepTEInnerPanel = null;
	Amount<Angle> _sweepQuarterChordOuterPanel = null;
	Amount<Angle> _sweepLEOuterPanel = null;
	Amount<Angle> _sweepTEOuterPanel = null;
	Amount<Angle> _sweepStructuralAxis = null;

	boolean hasWinglet = false;
	Amount<Length> _wingletHeight;

	// Distance of HTail aerodynamic center from aircraft's CG
	Amount<Length> _AC_CGdistance;
	Amount<Length> _ACw_ACdistance;
	Amount<Area> _surfaceCS;

	Double 
	_formFactor,
	_CeCt, _volumetricRatio,
	_compositeCorretionFactor, _massCorrectionFactor,
	_maxThicknessMean;

	/** Lifting surface root chord angle with respect to body x axis */
	Amount<Angle> _iw = null;

	// AERODYNAMICS
	Amount<Length> _meanAerodynamicChord = null;
	Amount<Length> _xLEMeanAerodynamicChord = null;
	Amount<Length> _yMeanAerodynamicChord = null;
	Amount<Length> _spanStationMeanAerodynamicChord = null;

	Amount<Length> _xLEMeanAerodynamicChordInnerPanel = null;
	Amount<Length> _yMeanAerodynamicChordInnerPanel = null;
	Amount<Length> _spanStationMeanAerodynamicChordInnerPanel = null;

	Amount<Length> _xLEMeanAerodynamicChordOuterPanel = null;
	Amount<Length> _yMeanAerodynamicChordOuterPanel = null;
	Amount<Length> _spanStationMeanAerodynamicChordOuterPanel = null;

	Amount<Length> _aerodynamicCenterX = null;
	Amount<Length> _aerodynamicCenterY = Amount.valueOf(1.0, SI.METRE);
	Amount<Length> _aerodynamicCenterZ = Amount.valueOf(1.0, SI.METRE);

	Amount<Length> _aerodynamicCenterXInnerPanel = null;
	Amount<Length> _aerodynamicCenterYInnerPanel = null;
	Amount<Length> _aerodynamicCenterZInnerPanel = null;

	Amount<Length> _aerodynamicCenterXOuterPanel = null;
	Amount<Length> _aerodynamicCenterYOuterPanel = null;
	Amount<Length> _aerodynamicCenterZOuterPanel = null;

	Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	Map <MethodEnum, Amount<Length>> _xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	Map <MethodEnum, Amount<Length>> _yCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();

	Double[] _percentDifference;
	Amount<Length> _X0, _Y0, _Z0,
	_xCG, _yCG, _zCG;

	Amount<Mass> _mass, _massReference, _massEstimated;
	boolean _variableIncidence;
	static final int _numberOfPointsChordDistribution = 30;
	Integer _numberOfAirfoils = 0;
	Integer _numberOfAirfoilsExposed = 0;
	double[] _vc2;
	double[] _vx_le;
	double[] _vy_le;
	Amount<Length> _x_le_exposed, _x_te_exposed;
	Amount<Length> _xCGReference;
	Amount<Length> _xCGEstimated;
	Double[] _percentDifferenceXCG;
	Amount<Length> _yCGReference;
	Double[] _percentDifferenceYCG;
	Amount<Length> _yCGEstimated;

	CenterOfGravity _cg;
	Amount<Length> _xLEMacActualBRF;
	Amount<Length> _yLEMacActualBRF;

	MyAirfoil _theCurrentAirfoil = null;


	/**
	 * Contains all the airfoil relative to the current lifting surface
	 */
	List<MyAirfoil> _theAirfoilsList = new ArrayList<MyAirfoil>();
	List<MyAirfoil> _theAirfoilsListExposed = new ArrayList<MyAirfoil>();

	MyArray _etaAirfoil,
	_twistVsY,
	_yStationsAirfoil,
	_chordVsYAirfoils,
	_distanceAirfoilACFromWingAC,

	/** Dimensional location of the Aerodynamic center of the airfoil */
	_xAcAirfoil,

	_alpha0VsY, 
	_alphaStar_y,
	_alphaStall,
	_clAlpha_y, 
	_clBasic_y,
	_clStar_y,
	_clMaxVsY,
	_clMaxSweep_y,
	_clAtCdMin_y,
	_cdMin_y,
	_cmAC_y,
	_cmAlphaLE_y,
	_aerodynamicCenterXcoord_y,
	_reynoldsCruise_y,
	_kFactorDragPolar_y,
	_maxThicknessVsY;



	Amount _xACActualMRF, _xACActualLRF;

	private LSAerodynamicsManager aerodynamics;
	public void setAerodynamics(LSAerodynamicsManager aerodynamics) {
		this.aerodynamics = aerodynamics;
	}

	private LSGeometryManager geometry;

	private MyArray _etaAirfoilExposed;

	private MyArray _twistVsYExposed;

	private MyArray _yStationsAirfoilExposed;

	private MyArray _alpha0VsYExposed;

	private MyArray _chordVsYAirfoilsExposed;


	private void initializeDefaultSurface(ComponentEnum type){

		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
		_type = type;

		switch (type){
		case WING: {

			//////////////////////////////
			// Input data
			//////////////////////////////
			_surface = Amount.valueOf(61, SI.SQUARE_METRE);
			_aspectRatio = 12.0;
			_taperRatioEquivalent = 0.636;
			_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(1.4),SI.RADIAN);
			_spanStationKink = 0.3478;
			_iw = Amount.valueOf(Math.toRadians(2.0), SI.RADIAN);
			_twistKink = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_twistTip = Amount.valueOf(Math.toRadians(-2.0),SI.RADIAN);
			_dihedralInnerPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralOuterPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);

			// distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point
			_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);  
			_roughness = Amount.valueOf(0.152e-5, SI.METER);
			_xTransitionU = 0.15;
			_xTransitionL = 0.12;

			// Thickness of 3 section
			_tc_root = 0.18;               
			_tc_kink = 0.18;               
			_tc_tip = 0.135;        

			// Z position relative to the height of component to which this one is attached
			_positionRelativeToAttachment = 1.0; 

			// Extension of control surfaces in percent of total surface
			_surfaceCS = Amount.valueOf(_surface.times(0.25).getEstimatedValue(), SI.SQUARE_METRE);

			// Additional chord extension at root (LE) with respect to equivalent wing
			_extensionLERootChordLinPanel = 0.;

			// Additional chord extension at root (TE) with respect to equivalent wing
			_extensionTERootChordLinPanel = 0.;

			// Percent of composite material used for wing structure
			_compositeCorretionFactor = 0.1;

			// A reference value chosen by the user
			_massReference = Amount.valueOf(2080.6, SI.KILOGRAM);

			// Calibration constant (to account for slight weight changes due to composites etc...)
			_massCorrectionFactor = 1.;


			//aerodynamics = new LSAerodynamicsManager(_theOperatingConditions, this); //? 

		} break;

		case HORIZONTAL_TAIL : {

			_positionRelativeToAttachment = 1.0;
			_surface = Amount.valueOf(11.73, SI.SQUARE_METRE) ;
			_aspectRatio = 4.555;
			_taperRatioEquivalent = 0.57;
			// ATR72
			// _tipChord = 1.167 m;
			// _tipRoot = 2.047 m;
			_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(0.),SI.RADIAN);

			// if _spanStationKink=1.0 the wing has no crank (simply tapered wing)
			_spanStationKink = 1.0;  
			_extensionLERootChordLinPanel = 0.;
			_extensionTERootChordLinPanel = 0.;
			_iw = Amount.valueOf(0., SI.RADIAN);
			_twistKink = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_twistTip = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralInnerPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralOuterPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_CeCt = 0.3; // Elevator to tail chord ratio
			_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);
			//			_deltaXWingFus = Amount.valueOf(24.6,SI.METER); // distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point 
			_xTransitionU = 0.12;
			_xTransitionL = 0.12;
			_roughness = Amount.valueOf(0.052e-5, SI.METER);

			//  INPUT DATA
			// Thickness of 3 section
			_tc_root = .12;               // value in %
			_tc_kink = .12;               // value in %
			_tc_tip = .12;              // value in %

			// Variable incidence horizontal tail ?
			_variableIncidence = false;

			_surfaceCS = Amount.valueOf(_surface.times(0.25).getEstimatedValue(), SI.SQUARE_METRE);
			_massReference = Amount.valueOf(192.6, SI.KILOGRAM);

		} break;

		case VERTICAL_TAIL : {

			_positionRelativeToAttachment = 1.0;
			_surface = Amount.valueOf(12.48, SI.SQUARE_METRE) ;
			_aspectRatio = 1.66;
			_taperRatioEquivalent = 0.32;
			// ATR72
			// _tipChord = 1.796 m;
			// _tipRoot = 5.55 m;
			_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(28.6),SI.RADIAN);
			_spanStationKink = 0.3;
			_extensionLERootChordLinPanel = 0.;
			_extensionTERootChordLinPanel = 0.;
			_iw = Amount.valueOf(0., SI.RADIAN);
			_twistKink = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_twistTip = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralInnerPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralOuterPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_CeCt = 0.3;
			_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);
			//			_deltaXWingFus = Amount.valueOf(21.9,SI.METER); // distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point 
			_xTransitionU = 0.12;
			_xTransitionL = 0.12;
			_roughness = Amount.valueOf(0.052e-5, SI.METER);
			//  INPUT DATA
			// Thickness of 3 section
			_tc_root = .12;               // value in %
			_tc_kink = .12;               // value in %
			_tc_tip = .12;              // value in %

			_surfaceCS = Amount.valueOf(_surface.times(0.25).getEstimatedValue(), SI.SQUARE_METRE);
			_massReference = Amount.valueOf(271.7, SI.KILOGRAM);

			setMirrored(false);
		} break;

		case CANARD : {

			_positionRelativeToAttachment = 0.5;
			_surface = Amount.valueOf(61, SI.SQUARE_METRE) ;
			_aspectRatio = 12.0;
			_taperRatioEquivalent = 0.636;
			_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(1.4),SI.RADIAN);
			_spanStationKink = 1.0;
			_extensionLERootChordLinPanel = 0.;
			_extensionTERootChordLinPanel = 0.;
			_iw = Amount.valueOf(0., SI.RADIAN);
			_twistKink = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_twistTip = Amount.valueOf(Math.toRadians(-2.0),SI.RADIAN);
			_dihedralInnerPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralOuterPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);
			//			_deltaXWingFus = Amount.valueOf(24.6,SI.METER); // distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point 
			_xTransitionU = 0.1;
			_xTransitionL = 0.1;
			_roughness = Amount.valueOf(0.052e-5, SI.METER);
			//  INPUT DATA
			// Thickness of 3 section
			_tc_root = .12;               // value in %
			_tc_kink = .12;               // value in %
			_tc_tip = .12;              // value in %

			_massReference = Amount.valueOf(100., SI.KILOGRAM);

		} break;

		default : { } break;

		}

		initializeAirfoils(this);

	}

	protected void initializeAirfoils(LiftingSurface ls) {

		// TODO: this methods is executed twice!!! (the second time in MyAnalysis)
		// calculateGeometry();
		double span = sqrt(_surface.getEstimatedValue()*_aspectRatio);
		_numberOfAirfoils = 3;

		// The id must be initially 0 to properly read the airfoils 
		MyAirfoil.idCounter = 0;
		aircraft.auxiliary.airfoil.Geometry.idCounter = 0;
		aircraft.auxiliary.airfoil.Aerodynamics.idCounter = 0;

		// Create three default airfoils
		_theAirfoilsList.add(new MyAirfoil(ls,0.));

		if (_type.equals(ComponentEnum.VERTICAL_TAIL)) {
			_theAirfoilsList.add(new MyAirfoil(ls, _spanStationKink*span));
			_theAirfoilsList.add(new MyAirfoil(ls, span));

		} else {
			_theAirfoilsList.add(new MyAirfoil(ls, _spanStationKink*span/2.));
			_theAirfoilsList.add(new MyAirfoil(ls, span/2.));
		}
	}




	/**
	 * 
	 * Overload of the airfoils initializer method that recognize aircraft name and sets it's value.
	 * 
	 * @author Vittorio Trifari
	 */
	protected void initializeAirfoils(String aircraftName, LiftingSurface ls) {

		// TODO: this methods is executed twice!!! (the second time in MyAnalysis)
		// calculateGeometry();
		double span = sqrt(_surface.getEstimatedValue()*_aspectRatio);
		_numberOfAirfoils = 3;

		// The id must be initially 0 to properly read the airfoils 
		MyAirfoil.idCounter = 0;
		aircraft.auxiliary.airfoil.Geometry.idCounter = 0;
		aircraft.auxiliary.airfoil.Aerodynamics.idCounter = 0;

		// Create three default airfoils
		_theAirfoilsList.add(new MyAirfoil(aircraftName, AirfoilStationEnum.ROOT , ls, 0.));

		if (_type.equals(ComponentEnum.VERTICAL_TAIL)) {
			_theAirfoilsList.add(new MyAirfoil(aircraftName, ls, _spanStationKink*span));
			_theAirfoilsList.add(new MyAirfoil(aircraftName, ls, span));

		} else {
			_theAirfoilsList.add(new MyAirfoil(aircraftName,  AirfoilStationEnum.KINK ,ls, _spanStationKink*span/2.));
			_theAirfoilsList.add(new MyAirfoil(aircraftName,  AirfoilStationEnum.TIP,ls, span/2.));
		}
	}


	// Construct Wing without fuselage
	public LiftingSurface(String name, 
			String description, 
			Double x, 
			Double y, 
			Double z, 
			ComponentEnum type) { 

		super(name, description, x, y, z, type);

		_X0 = Amount.valueOf(x, SI.METER);
		_Y0 = Amount.valueOf(y, SI.METER);
		_Z0 = Amount.valueOf(z, SI.METER);

		_cg = new CenterOfGravity(_X0, _Y0, _Z0);

		// ATR 72 Data (matlab file)
		initializeDefaultSurface(type);

	} // end of constructor


	// Construct Wing without fuselage, with rigging angle
	public LiftingSurface(String name, 
			String description, 
			Double x, 
			Double y, 
			Double z, 
			Double riggingAngle, 
			ComponentEnum type, 
			Fuselage fuselage) { 

		super(name, description, x, y, z, type);

		_X0 = Amount.valueOf(x, SI.METER);
		_Y0 = Amount.valueOf(y, SI.METER);
		_Z0 = Amount.valueOf(z, SI.METER);
		_cg = new CenterOfGravity(_X0, _Y0, _Z0);

		_theFuselage = fuselage;
		// ATR 72 Data (matlab file)


		initializeDefaultSurface(type);

	} // end of constructor

	// Construct Wing without fuselage, with rigging angle
	public LiftingSurface(String name, 
			String description, 
			Double x, 
			Double y, 
			Double z, 
			Double riggingAngle, 
			ComponentEnum type
			) { 

		super(name, description, x, y, z, type);

		_X0 = Amount.valueOf(x, SI.METER);
		_Y0 = Amount.valueOf(y, SI.METER);
		_Z0 = Amount.valueOf(z, SI.METER);
		_cg = new CenterOfGravity(_X0, _Y0, _Z0);

		// ATR 72 Data (matlab file)


		initializeDefaultSurface(type);

	} // end of constructor



	// Construct Wing with other components
	public LiftingSurface(String name, 
			String description, 
			Double x, 
			Double y, 
			Double z, 
			ComponentEnum type, 
			Fuselage theFuselage,
			Nacelle theNacelle,
			LiftingSurface ... liftingSurface) { 

		super(name, description, x, y, z, type);

		_X0 = Amount.valueOf(x, SI.METER);
		_Y0 = Amount.valueOf(y, SI.METER);
		_Z0 = Amount.valueOf(z, SI.METER);
		_cg = new CenterOfGravity(_X0, _Y0, _Z0);

		_theFuselage = theFuselage;

		// ATR 72 Data (matlab file)
		//		initializeDefaultSurface(type);

	} // end of constructor


	// Import from file
	public LiftingSurface(
			File xmlFile, 
			String name, String description, Double x, Double y, Double z, Fuselage fuselage, Double riggingAngle) { 

		super(name, description, x, y, z, ComponentEnum.WING);
		_cg = new CenterOfGravity(_X0, _Y0, _Z0);

		//		importFromXMLFile(xmlFile); // may overwrite: name, description 	

	}


	public LiftingSurface(ComponentEnum type) {
		super("New lifting surface", "Default lifting surface", 0.0, 0.0, 0.0, type);
		_description = "Default lifting surface (" + type.name() + ")";
		_X0 = Amount.valueOf(0.0, SI.METER);
		_Y0 = Amount.valueOf(0.0, SI.METER);
		_Z0 = Amount.valueOf(0.0, SI.METER);
		initializeDefaultSurface(type);
	}

	public void calculateGeometry(){

		buildPlanform();

		if (isMirrored()) _semispan = _span.divide(2.).copy();
		else _semispan = _span.copy();

		calculateChordYAxisEquivalent();
		calculateChordYAxisActual();
		calculateMACactual();
		calculateXYMacLE();
		calculateMeanAirfoil();
		calculateSurfaceExposedAndWetted();
		updateAirfoilsGeometry();
		geometry = new LSGeometryManager(this);

	}

	/**
	 * 
	 */
	public void updateAirfoilsGeometry() {

		//		MyAirfoil.idCounter = 0;
		//		core.auxiliary.airfoil.Geometry.idCounter = 0;
		//		core.auxiliary.airfoil.Aerodynamics.idCounter = 0;

		_theAirfoilsList.get(0).getGeometry().update(0.);

		if (_theAirfoilsList.size() < 3) {
			_theAirfoilsList.get(1).getGeometry().update(_semispan.getEstimatedValue());

		} else {
			_theAirfoilsList.get(1).getGeometry().update(_spanStationKink*_semispan.getEstimatedValue());
			_theAirfoilsList.get(2).getGeometry().update(_semispan.getEstimatedValue());
		}

		_numberOfAirfoils = new Integer(_theAirfoilsList.size());
		getAirfoilsPropertiesAsArray();


		//				addAirfoil(0., "Airfoil_1");
		//		if (_spanStationKink != 1.) {
		//			addAirfoil(_spanStationKink, "Airfoil_2");
		//			addAirfoil(1., "Airfoil_3");
		//		} else {
		//			addAirfoil(1., "Airfoil_2");
		//		}
	}

	public void updateAirfoilsGeometryExposedWing( Aircraft aircraft) {

		//		MyAirfoil.idCounter = 0;
		//		core.auxiliary.airfoil.Geometry.idCounter = 0;
		//		core.auxiliary.airfoil.Aerodynamics.idCounter = 0;

		double yLoc = aircraft.get_fuselage().getWidthAtX(aircraft.get_wing()
				.get_xLEMacActualBRF().getEstimatedValue())/2;
		MyAirfoil airfoilRootExposed =LSAerodynamicsManager.calculateIntermediateAirfoil(
				aircraft.get_wing(),
				yLoc);
		_theAirfoilsListExposed.add(0, airfoilRootExposed);
		_theAirfoilsListExposed.get(0).getGeometry().update(yLoc);

		if (aircraft.get_wing().get_theAirfoilsList().size() < 3) {
			MyAirfoil airfoilTipExposed = aircraft.get_wing().get_theAirfoilsList().get(1);
			_theAirfoilsListExposed.add(1, airfoilTipExposed);
		}

		else{
			MyAirfoil airfoilKinkExposed = aircraft.get_wing().get_theAirfoilsList().get(1);
			MyAirfoil airfoilTipExposed = aircraft.get_wing().get_theAirfoilsList().get(2);
			_theAirfoilsListExposed.add(1, airfoilKinkExposed);
			_theAirfoilsListExposed.add(2, airfoilTipExposed);
		}

		_numberOfAirfoilsExposed = new Integer(_theAirfoilsListExposed.size());

		//getAirfoilsPropertiesAsArrayExposed(aircraft);

		//				addAirfoil(0., "Airfoil_1");
		//		if (_spanStationKink != 1.) {
		//			addAirfoil(_spanStationKink, "Airfoil_2");
		//			addAirfoil(1., "Airfoil_3");
		//		} else {
		//			addAirfoil(1., "Airfoil_2");
		//		}
	}


	private void buildPlanform() {

		_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);

		// TODO : given S,AR,eta_kink,lambda_eq_c4,taper_ratio;ex_le,ex_te
		//calculate wing geometry.

		//////////////////////////////
		// Output data
		//////////////////////////////
		_dihedralMean = Amount.valueOf(_dihedralInnerPanel.getEstimatedValue() * _spanStationKink +
				_dihedralOuterPanel.getEstimatedValue() * (1-_spanStationKink), SI.RADIAN);

		_sweepStructuralAxis = Amount.valueOf(_sweepQuarterChordEq.to(SI.RADIAN).getEstimatedValue(), SI.RADIAN);
		_sweepHalfChordEq = calculateSweep(_sweepQuarterChordEq.to(SI.RADIAN).getEstimatedValue(), 0.5, 0.25);

		_span = Amount.valueOf(Math.sqrt(_surface.doubleValue(SI.SQUARE_METRE) * _aspectRatio), SI.METER);
		_meanGeometricChord = _surface.divide(_span).to(SI.METER);
		_chordRootEquivalentWing = Amount.valueOf(2 * _surface.doubleValue(SI.SQUARE_METRE)/
				(_span.doubleValue( SI.METER) * (1+_taperRatioEquivalent)),SI.METER);		


		// Semi-span of inner and outer panel
		if (_spanStationKink!=1.){
			_semiSpanInnerPanel = Amount.valueOf(_spanStationKink * (_span.doubleValue(SI.METER)/2),SI.METER);
			_semiSpanOuterPanel = Amount.valueOf(_span.doubleValue(SI.METER)/2 - _semiSpanInnerPanel.doubleValue(SI.METER),SI.METER);
		} else {
			// if _spanStationKink=1.0 (simply tapered wing) outer panel doesn't exist: there is only the inner panel
			_semiSpanInnerPanel = Amount.valueOf((_span.doubleValue(SI.METER))/2,SI.METER);
			_semiSpanOuterPanel = Amount.valueOf(0.,SI.METER);
		}


		//Tip chord
		_chordTip = Amount.valueOf(_chordRootEquivalentWing.doubleValue(SI.METER) * _taperRatioEquivalent,SI.METER);

		// _chordLinPanel = Root chord as if kink chord is extended linearly till wing root.
		_chordLinPanel = Amount.valueOf((_surface.doubleValue(SI.SQUARE_METRE) - _chordTip.doubleValue(SI.METER) * 
				(_span.doubleValue(SI.METER)/2))/((_extensionLERootChordLinPanel + _extensionTERootChordLinPanel) * 
						_semiSpanInnerPanel.doubleValue(SI.METER) + _span.doubleValue(SI.METER)/2),SI.METER);


		// Cranked wing <==> _extensionLE/TERootChordLinPanel !=0 and _spanStationKink!=1.0 (if branch)
		// Constant chord (inner panel) + simply tapered (outer panel) wing <==> _extensionLE/TERootChordLinPanel = 0 and _spanStationKink!=1.0 (else branch)
		// Simply tapered wing <==> _extensionLE/TERootChordLinPanel = 0 and _spanStationKink=1.0 (if branch)
		if (((_extensionLERootChordLinPanel != 0.0 | _extensionTERootChordLinPanel != 0.0) | _spanStationKink==1.0)){
			_chordRoot = Amount.valueOf(_chordLinPanel.doubleValue(SI.METER) *  
					(1 + _extensionLERootChordLinPanel + _extensionTERootChordLinPanel),SI.METER);
			_chordKink = Amount.valueOf(_chordLinPanel.doubleValue(SI.METER) * 
					(1-_spanStationKink) + _chordTip.doubleValue(SI.METER) * _spanStationKink, SI.METER);

		} else {
			_chordRoot = Amount.valueOf(2 * (_surface.getEstimatedValue()/2 - 
					_chordTip.getEstimatedValue() * _semiSpanOuterPanel.getEstimatedValue()/2)/
					(_span.getEstimatedValue()/2 + _semiSpanInnerPanel.getEstimatedValue()), SI.METER);
			_chordKink = (Amount<Length>) JPADStaticWriteUtils.cloneAmount(_chordRoot);
		}

		_sweepLEEquivalent = Amount.valueOf(Math.atan(Math.tan(_sweepQuarterChordEq.doubleValue(SI.RADIAN))+
				(1-_taperRatioEquivalent)/(_aspectRatio * (1+_taperRatioEquivalent))),SI.RADIAN);


		// Taper ratios
		_taperRatioInnerPanel = _chordKink.doubleValue(SI.METER)/_chordRoot.doubleValue(SI.METER);
		_taperRatioOuterPanel = _chordTip.doubleValue(SI.METER)/_chordKink.doubleValue(SI.METER);
		_taperRatioActual = _chordTip.doubleValue(SI.METER)/_chordRoot.doubleValue(SI.METER);
		_taperRatioOpt = 0.45 * 
				Math.pow(Math.E,-0.0375 * _sweepQuarterChordEq.to(NonSI.DEGREE_ANGLE).getEstimatedValue());


		// X coordinates of root, tip and kink chords
		_xLETip = Amount.valueOf(Math.tan(_sweepLEEquivalent.doubleValue(SI.RADIAN)) * 
				_span.doubleValue(SI.METER)/2+
				(_chordLinPanel.doubleValue(SI.METER) * _extensionLERootChordLinPanel * (1-_spanStationKink)),SI.METER);
		_xLERoot = Amount.valueOf(0,SI.METER);
		if ((_extensionLERootChordLinPanel != 0.0 | _extensionTERootChordLinPanel != 0.0) | _spanStationKink==1.){
			_xLEKink = Amount.valueOf(_chordLinPanel.doubleValue(SI.METER) * 
					_extensionLERootChordLinPanel+_spanStationKink * 
					(_xLETip.doubleValue(SI.METER)-_chordLinPanel.doubleValue(SI.METER) * _extensionLERootChordLinPanel),SI.METER);
		} else {
			_xLEKink = (Amount<Length>) JPADStaticWriteUtils.cloneAmount(_xLERoot);
		}
		_xTEKink = Amount.valueOf(_xLEKink.doubleValue(SI.METER)+_chordKink.doubleValue(SI.METER),SI.METER);
		_xTETip = Amount.valueOf(_xLETip.doubleValue(SI.METER)+_chordTip.doubleValue(SI.METER), SI.METER);


		// Sweep of LE and TE of inner panel
		if ((_extensionLERootChordLinPanel != 0.0 | _extensionTERootChordLinPanel != 0.0) | _spanStationKink==1.){
			_sweepLEInnerPanel = Amount.valueOf(Math.atan(_xLEKink.doubleValue(SI.METER)/
					_semiSpanInnerPanel.doubleValue(SI.METER)),SI.RADIAN);
			_sweepTEInnerPanel = Amount.valueOf(Math.atan((_xTEKink.doubleValue(SI.METER)-_chordRoot.doubleValue(SI.METER))/
					(_semiSpanInnerPanel.doubleValue(SI.METER))),SI.RADIAN);
		} else {
			_sweepLEInnerPanel = Amount.valueOf(0.0, SI.RADIAN);
			_sweepTEInnerPanel = Amount.valueOf(0.0, SI.RADIAN);
		}


		// Outer panel LE and TE sweep
		if(_spanStationKink!=1.){
			_sweepLEOuterPanel = Amount.valueOf(Math.atan((_xLETip.doubleValue(SI.METER)-_xLEKink.doubleValue(SI.METER))/
					(_semiSpanOuterPanel.doubleValue(SI.METER))),SI.RADIAN);
			_sweepTEOuterPanel = Amount.valueOf(Math.atan((_xTETip.doubleValue(SI.METER)-_xTEKink.doubleValue(SI.METER))/
					(_semiSpanOuterPanel.doubleValue(SI.METER))),SI.RADIAN);
		} else {
			_sweepLEOuterPanel = (Amount<Angle>) JPADStaticWriteUtils.cloneAmount(_sweepLEInnerPanel);
			_sweepTEOuterPanel = (Amount<Angle>) JPADStaticWriteUtils.cloneAmount(_sweepTEInnerPanel);
		}


		// Semi surfaces of inner and outer panels
		if((_extensionLERootChordLinPanel != 0.0 | _extensionTERootChordLinPanel != 0.0)){
			_semiSurfaceInnerPanel = Amount.valueOf((_chordRoot.doubleValue(SI.METER)+_chordKink.doubleValue(SI.METER)) * 
					_semiSpanInnerPanel.doubleValue(SI.METER)/2, SI.SQUARE_METRE);
			_semiSurfaceOuterPanel = Amount.valueOf((_chordKink.doubleValue(SI.METER)+_chordTip.doubleValue(SI.METER)) * 
					((_span.doubleValue(SI.METER)/2)-_semiSpanInnerPanel.doubleValue(SI.METER))/2, SI.SQUARE_METRE);
		} else if (_spanStationKink==1.) {
			_semiSurfaceInnerPanel = Amount.valueOf(_surface.getEstimatedValue()/2, SI.SQUARE_METRE);
			_semiSurfaceOuterPanel = Amount.valueOf(0., SI.SQUARE_METRE);
		} else {
			_semiSurfaceInnerPanel = Amount.valueOf(_surface.getEstimatedValue()/4, SI.SQUARE_METRE);
			_semiSurfaceOuterPanel = Amount.valueOf(_surface.getEstimatedValue()/4, SI.SQUARE_METRE);
		}


		// Aspect ratio of each panel
		_aspectRatioInnerPanel = Math.pow(2 * _semiSpanInnerPanel.doubleValue(SI.METER), 2)/
				(2 * _semiSurfaceInnerPanel.doubleValue(SI.SQUARE_METRE));
		if (_spanStationKink!=1.){
			_aspectRatioOuterPanel = Math.pow(2 * _semiSpanOuterPanel.doubleValue(SI.METER), 2)/
					(2 * _semiSurfaceOuterPanel.doubleValue(SI.SQUARE_METRE));
		} else {
			_aspectRatioOuterPanel = _aspectRatioInnerPanel; 
		}


		// Sweep at c/4 of inner panel
		if ((_extensionLERootChordLinPanel != 0.0 | _extensionTERootChordLinPanel != 0.0) | _spanStationKink==1.){
			_sweepQuarterChordInnerPanel = Amount.valueOf(Math.atan(Math.tan(_sweepLEInnerPanel.doubleValue(SI.RADIAN))-
					((1-_taperRatioInnerPanel)/(_aspectRatioInnerPanel * (1+_taperRatioInnerPanel)))),SI.RADIAN);  // to check
		} else {
			_sweepQuarterChordInnerPanel = Amount.valueOf(0.0, SI.RADIAN);
		}


		// Sweep angle at 1/4 of outer panel
		if((_extensionLERootChordLinPanel != 0.0 | _extensionTERootChordLinPanel != 0.0)) {
			_sweepQuarterChordOuterPanel = Amount.valueOf(Math.atan(Math.tan(_sweepLEOuterPanel.doubleValue(SI.RADIAN))-
					((1-_taperRatioOuterPanel)/(_aspectRatioOuterPanel * (1+_taperRatioOuterPanel)))),SI.RADIAN);
		} else {
			_sweepQuarterChordOuterPanel = _sweepQuarterChordInnerPanel;
		}


		// Surface of actual wing
		_surfaceCranked = Amount.valueOf(2 * (_semiSurfaceInnerPanel.doubleValue(SI.SQUARE_METRE)+_semiSurfaceOuterPanel.doubleValue(SI.SQUARE_METRE)),SI.SQUARE_METRE);

		//	  Calculate Mean Aerodynamic Chord and Geometric for Equivalent Wing
		_x_LE_Root_eq = Amount.valueOf(_xLETip.doubleValue(SI.METER)-((0.5 * _span.doubleValue(SI.METER)) * Math.tan(Math.toRadians(_sweepLEEquivalent.doubleValue(NonSI.DEGREE_ANGLE)))),SI.METER);
		_geomChordEq = Amount.valueOf(_surface.doubleValue(SI.SQUARE_METRE)/_span.doubleValue(SI.METER), SI.METER);
		_meanAerodChordEq = Amount.valueOf(2 * _chordRootEquivalentWing.doubleValue(SI.METER) * ((1+_taperRatioEquivalent+Math.pow(_taperRatioEquivalent, 2))/(3 * (1+_taperRatioEquivalent))),SI.METER);		
		_y_LE_Mac_Eq = Amount.valueOf((_span.doubleValue(SI.METER) * (1+2 * _taperRatioEquivalent))/(6 * (1+_taperRatioEquivalent)), SI.METER);  // Graphic Method
		_x_LE_Mac_Eq = Amount.valueOf(_x_LE_Root_eq.doubleValue(SI.METER)+_y_LE_Mac_Eq.doubleValue(SI.METER) * Math.tan(_sweepLEEquivalent.doubleValue(SI.RADIAN)),SI.METER);

	}


	/** 
	 * Calculate Chord Distribution of the Equivalent Wing along y axis
	 */
	private void calculateChordYAxisEquivalent() {

		List<Amount<Length>> chordsEqList = new ArrayList<Amount<Length>>();
		_eta.setDouble(MyArrayUtils.linspace(0., 1., _numberOfPointsChordDistribution));
		_yStationEq.setRealVector(_eta.getRealVector().mapMultiply(0.5*_span.doubleValue(SI.METER)));

		for(int i = 0; i < _eta.size(); i++){

			chordsEqList.add(Amount.valueOf(
					getChordEquivalentAtY(_yStationEq.get(i)) 
					, SI.METER));
		}

		_chordsVsYEq.setAmountList(chordsEqList);

		//		_eta.add(1.00);
		//		_yStation.add(Amount.valueOf(0.5 * _span.doubleValue(SI.METER) * _eta.get(_eta.size()-1),SI.METER));
		//		_chordsEqList.add(Amount.valueOf(((2 * _surface.doubleValue(SI.SQUARE_METRE))/
		//				(_span.doubleValue(SI.METER) * (1+_taperRatioEquivalent))) * 
		//				(1-((2 * (1-_taperRatioEquivalent)/_span.doubleValue(SI.METER)) * 
		//						_yStation.get(_yStation.size()-1).doubleValue(SI.METER))), SI.METER));
		//
		//		_chordsEquivalentVsYVector.setEntry(k+1, ((2 * _surface.doubleValue(SI.SQUARE_METRE))/
		//				(_span.doubleValue(SI.METER) * (1+_taperRatioEquivalent))) * 
		//				(1-((2 * (1-_taperRatioEquivalent)/_span.doubleValue(SI.METER)) * 
		//						_yStation.get(_yStation.size()-1).doubleValue(SI.METER))));

	}

	/**
	 * Calculate Chord Distribution of the Actual Wing along y axis
	 */
	private void calculateChordYAxisActual() {
		//_chordsCk

		//		for(Double i = 0;i<1;i+= 1/(_nPoints)){
		//			
		//			if(i< = _spanStationKink){
		//			
		//			_eta.add(i);
		//			_yStation.add(Amount.valueOf(0.5 * _span.doubleValue(SI.METER) * _eta.get(_eta.size()-1),SI.METER));
		//			_chordsCk.add(Amount.valueOf(((2 * (2 * _semiSurfaceInnerPanel.doubleValue(SI.SQUARE_METRE)))/(2 * _semiSpanInnerPanel.doubleValue(SI.METER) * (1+_taperRatioInnerPanel))) * (1-((2 * (1-_taperRatioInnerPanel)/(2 * _semiSpanInnerPanel.doubleValue(SI.METER))) * _yStation.get(_yStation.size()-1).doubleValue(SI.METER))), SI.METER));
		//		}
		//			else {
		//				_eta.add(i);
		//				_yStation.add(Amount.valueOf(0.5 * _span.doubleValue(SI.METER) * _eta.get(_eta.size()-1),SI.METER));
		//				_chordsCk.add(Amount.valueOf(((2 * (2 * _semiSurfaceOuterPanel.doubleValue(SI.SQUARE_METRE)))/(2 * _semiSpanOuterPanel.doubleValue(SI.METER) * (1+_taperRatioOuterPanel))) * (1-((2 * (1-_taperRatioOuterPanel)/(2 * _semiSpanOuterPanel.doubleValue(SI.METER))) * _yStation.get(_yStation.size()-1).doubleValue(SI.METER))), SI.METER));
		//			}
		//			}
		//		
		//		_eta.add(1.0);
		//		_yStation.add(Amount.valueOf(0.5 * _span.doubleValue(SI.METER) * _eta.get(_eta.size()-1),SI.METER));
		//		_chordsCk.add(Amount.valueOf(((2 * (2 * _semiSurfaceOuterPanel.doubleValue(SI.SQUARE_METRE)))/(2 * _semiSpanOuterPanel.doubleValue(SI.METER) * (1+_taperRatioOuterPanel))) * (1-((2 * (1-_taperRatioOuterPanel)/(2 * _semiSpanOuterPanel.doubleValue(SI.METER))) * _yStation.get(_yStation.size()-1).doubleValue(SI.METER))), SI.METER));

		List<Double> chordsActualVsYList = new ArrayList<Double>();
		List<Amount<Length>> _xLEvsY = new ArrayList<Amount<Length>>();
		List<Amount<Length>> _xTEvsY = new ArrayList<Amount<Length>>();

		_yStationActual.setRealVector(_eta.getRealVector().mapMultiply(0.5*_span.doubleValue(SI.METER)));

		for (int i=0; i < _numberOfPointsChordDistribution; i++) {

			if(_eta.get(i) <= _spanStationKink){
				_xLEvsY.add(Amount.valueOf(
						_xLERoot.doubleValue(SI.METER) +
						Math.tan(_sweepLEInnerPanel.doubleValue(SI.RADIAN)) * 
						_yStationActual.get(i)
						,SI.METER));

				_xTEvsY.add(Amount.valueOf((_xLERoot.doubleValue(SI.METER)+
						_chordRoot.doubleValue(SI.METER))+
						Math.tan(_sweepTEInnerPanel.doubleValue(SI.RADIAN)) * 
						_yStationActual.get(i),SI.METER));

			} else if(_spanStationKink !=1.) { // Handle simply tapered wing
				_xLEvsY.add(Amount.valueOf(_xLEKink.doubleValue(SI.METER) +
						Math.tan(_sweepLEOuterPanel.doubleValue(SI.RADIAN)) * 
						(_yStationActual.get(i)-
								_semiSpanInnerPanel.doubleValue(SI.METER)),SI.METER));

				_xTEvsY.add(Amount.valueOf(_xTEKink.doubleValue(SI.METER) +
						Math.tan(_sweepTEOuterPanel.doubleValue(SI.RADIAN)) * 
						(_yStationActual.get(i) -
								_semiSpanInnerPanel.doubleValue(SI.METER)),SI.METER));

			}

			chordsActualVsYList.add(_xTEvsY.get(_xTEvsY.size()-1).doubleValue(SI.METER)-
					_xLEvsY.get(_xLEvsY.size()-1).doubleValue(SI.METER));

		}

		_chordsVsYActual.setList(chordsActualVsYList);
		_xLEvsYActual.setAmountList(_xLEvsY);
		_xTEvsYActual.setAmountList(_xTEvsY);

	}

	/**
	 * Calculate Mean Aerodynamic Chord for Actual Wing
	 */
	private void calculateMACactual() {

		_vc2 = new double [_chordsVsYActual.size()];

		for (int i = 0; i < _vc2.length; i++) {
			_vc2[i] = Math.pow(_chordsVsYActual.get(i), 2);
		}

		_vx_le = new double [_xLEvsYActual.size()];
		for (int i = 0; i < _vx_le.length; i++) {		
			_vx_le[i] = _xLEvsYActual.get(i) * _chordsVsYActual.get(i);             
		}	

		_vy_le = new double [ _yStationActual.size()];
		for (int i = 0; i < _vy_le.length; i++) {		
			_vy_le[i] = _yStationActual.get(i) * _chordsVsYActual.get(i);             
		}	

		TrapezoidIntegrator trapezoid = new TrapezoidIntegrator();	
		UnivariateInterpolator interpolatorChordSquared = new LinearInterpolator();
		UnivariateFunction myInterpolationFunctionChordSquared = 
				interpolatorChordSquared.interpolate(_yStationActual.toArray(), _vc2);

		_meanAerodChordActual = Amount.valueOf(
				(2/_surface.doubleValue(SI.SQUARE_METRE)) 
				*  trapezoid.integrate(
						Integer.MAX_VALUE, 
						myInterpolationFunctionChordSquared, 
						_yStationActual.get(0), 
						_yStationActual.get(_yStationActual.size()-1)
						),SI.METER);

	}

	/** 
	 * Calculate x and y position of MAC LE
	 */
	private void calculateXYMacLE() {

		TrapezoidIntegrator trapezoid = new TrapezoidIntegrator();

		UnivariateInterpolator interpolatorXleCkWing = new LinearInterpolator(); // SplineInterpolator();
		UnivariateFunction    myInterpolationFunctionXleCkWing = 
				interpolatorXleCkWing.interpolate(_yStationActual.toArray(),_vx_le);

		UnivariateInterpolator interpolatorYleCkWing = new LinearInterpolator(); // SplineInterpolator();
		UnivariateFunction    myInterpolationFunctionYleCkWing = 
				interpolatorYleCkWing.interpolate(_yStationActual.toArray(),_vy_le);

		_xLEMacActualLRF = Amount.valueOf((2/_surface.doubleValue(SI.SQUARE_METRE)) 
				* trapezoid.integrate(
						Integer.MAX_VALUE, 
						myInterpolationFunctionXleCkWing, 
						_yStationActual.get(0), 
						_yStationActual.get(_yStationActual.size()-1)
						), SI.METER);

		_yLEMacActualLRF = Amount.valueOf((2/_surface.doubleValue(SI.SQUARE_METRE)) 
				* trapezoid.integrate(
						Integer.MAX_VALUE, 
						myInterpolationFunctionYleCkWing, 
						_yStationActual.get(0), 
						_yStationActual.get(_yStationActual.size()-1)
						),SI.METER);

		_xLEMacActualBRF = _xLEMacActualLRF.plus(_X0);
		_yLEMacActualBRF = _yLEMacActualLRF.plus(_Y0);
	}

	private void calculateMeanAirfoil(){

		//---------------------------------------
		Amount<Angle> lambda_c4_eq = Amount.valueOf(_sweepQuarterChordEq.doubleValue(SI.RADIAN), 
				SI.RADIAN);              // sweep c/4 equivalent wing
		//---------------------------------------

		// Calculate t/c of mean profile
		Amount<Area> area_1 = null;
		Amount<Area> area_2 = null;
		Amount<Area> area_3 = null;

		Double x_a_1 = _xLEKink.doubleValue(SI.METER);
		Double y_a_1 = _semiSpanInnerPanel.doubleValue(SI.METER);
		Double x_b_1 = _chordRoot.doubleValue(SI.METER);
		Double y_b_1 = 0.;
		Double x_c_1 = 0.;
		Double y_c_1 = 0.;
		Double x_a_2 = _xLEKink.doubleValue(SI.METER);
		Double y_a_2 = _semiSpanInnerPanel.doubleValue(SI.METER);
		Double x_b_2 = _xTETip.doubleValue(SI.METER);
		Double y_b_2 = 0.5 * _span.doubleValue(SI.METER);
		Double x_c_2 = _xLETip.doubleValue(SI.METER);
		Double y_c_2 = 0.5 * _span.doubleValue(SI.METER);

		// Evaluate area of each semi-panel
		area_1 = Amount.valueOf(Math.abs((x_b_1 * y_a_1-x_a_1 * y_b_1)+(x_c_1 * y_b_1-x_b_1 * y_c_1)+
				(x_a_1 * y_c_1-x_c_1 * y_a_1))/2,
				SI.SQUARE_METRE);
		area_2 = Amount.valueOf(Math.abs((x_b_2 * y_a_2-x_a_2 * y_b_2)+(x_c_2 * y_b_2-x_b_2 * y_c_2)+
				(x_a_2 * y_c_2-x_c_2 * y_a_2))/2,
				SI.SQUARE_METRE);
		area_3 = Amount.valueOf(0.5 * _surfaceCranked.doubleValue(SI.SQUARE_METRE)-area_1.doubleValue(SI.SQUARE_METRE)-
				area_2.doubleValue(SI.SQUARE_METRE),SI.SQUARE_METRE);		

		Double k_a = (2 * area_1.doubleValue(SI.SQUARE_METRE))/_surfaceCranked.doubleValue(SI.SQUARE_METRE);
		Double k_b = (2 * area_2.doubleValue(SI.SQUARE_METRE))/_surfaceCranked.doubleValue(SI.SQUARE_METRE);
		Double k_c = (2 * area_3.doubleValue(SI.SQUARE_METRE))/_surfaceCranked.doubleValue(SI.SQUARE_METRE);

		// Max thickness of mean airfoil
		_thicknessMean = (k_a*_tc_root + k_b*_tc_kink + k_c*_tc_tip);

	}

	/** 
	 * Evaluate exposed and wetted surfaces
	 */
	private void calculateSurfaceExposedAndWetted() {
		// Calculate Sexp of Wing
		Amount<Area> area_1_exposed = null;
		Amount<Area> area_2_exposed = null;

		if ( _theFuselage != null ){

			Double w_b_half = 0.5 * _theFuselage.getWidthAtX(_deltaXWingFus.getEstimatedValue());

			_x_le_exposed = Amount.valueOf(w_b_half * Math.tan(_sweepLEInnerPanel.doubleValue(SI.RADIAN)),SI.METER);
			_x_te_exposed = Amount.valueOf(_chordRoot.doubleValue(SI.METER)+(w_b_half * Math.tan(_sweepTEInnerPanel.doubleValue(SI.RADIAN))),SI.METER);


			Double x_a_1_exposed = _x_le_exposed.doubleValue(SI.METER);
			Double y_a_1_exposed = w_b_half;
			Double x_b_1_exposed = _chordRoot.doubleValue(SI.METER);
			Double y_b_1_exposed = 0.;
			Double x_c_1_exposed = 0.;
			Double y_c_1_exposed = 0.;
			Double x_a_2_exposed = _x_le_exposed.doubleValue(SI.METER);
			Double y_a_2_exposed = w_b_half;
			Double x_b_2_exposed = _chordRoot.doubleValue(SI.METER);
			Double y_b_2_exposed = 0.;
			Double x_c_2_exposed = _x_te_exposed.doubleValue(SI.METER);
			Double y_c_2_exposed = w_b_half;

			area_1_exposed = Amount.valueOf(Math.abs((x_b_1_exposed * y_a_1_exposed-x_a_1_exposed * y_b_1_exposed)+
					(x_c_1_exposed * y_b_1_exposed-x_b_1_exposed * y_c_1_exposed)+
					(x_a_1_exposed * y_c_1_exposed-x_c_1_exposed * y_a_1_exposed))/2,
					SI.SQUARE_METRE);
			area_2_exposed = Amount.valueOf(Math.abs((x_b_2_exposed * y_a_2_exposed-x_a_2_exposed * y_b_2_exposed)+
					(x_c_2_exposed * y_b_2_exposed-x_b_2_exposed * y_c_2_exposed)+
					(x_a_2_exposed * y_c_2_exposed-x_c_2_exposed * y_a_2_exposed))/2,
					SI.SQUARE_METRE);

		}
		else {
			area_1_exposed = Amount.valueOf(0.0, SI.SQUARE_METRE);
			area_2_exposed = Amount.valueOf(0.0, SI.SQUARE_METRE);

			_surfaceWetted =_surface;
		}

		// Exposed planform surface
		_surfaceExposed = Amount.valueOf(
				_surface.doubleValue(SI.SQUARE_METRE)-
				(2 * area_1_exposed.doubleValue(SI.SQUARE_METRE)+
						2 * area_2_exposed.doubleValue(SI.SQUARE_METRE)), 
				SI.SQUARE_METRE);

		//  ADAS pag.98
		_surfaceWettedExposed = Amount.valueOf(
				2 * (1+0.2 * _thicknessMean)* 
				_surfaceExposed.doubleValue(SI.SQUARE_METRE),SI.SQUARE_METRE);
		// End of Calculate Sexp of Wing	

		//  ADAS pag.98
		_surfaceWetted = Amount.valueOf(
				2 * (1 + 0.2 * _thicknessMean)*
				_surface.doubleValue(SI.SQUARE_METRE),SI.SQUARE_METRE); 

	}
	/** 
	 * Calculate tailplane arms
	 * 
	 * @author Lorenzo Attanasio
	 * @param aircraft
	 */
	public void calculateArms(Aircraft aircraft){
		calculateAircraftCGACdistance(aircraft);
		calculateACwACdistance(aircraft);
		calculateVolumetricRatio(aircraft);
	}

	public void calculateAircraftCGACdistance(Aircraft aircraft){
		// TODO: edit this
		_AC_CGdistance = Amount.valueOf(
				- aircraft.get_theBalance().get_cgMTOM().get_xBRF().getEstimatedValue()
				+ (_xLEMacActualBRF.getEstimatedValue() + 
						_meanAerodChordActual.getEstimatedValue()*0.25), 
				SI.METER);
	}

	public void calculateACwACdistance(Aircraft aircraft) {
		_ACw_ACdistance = Amount.valueOf(
				_xLEMacActualBRF.getEstimatedValue() + 
				_meanAerodChordActual.getEstimatedValue()*0.25 - 
				(aircraft.get_wing().get_xLEMacActualBRF().getEstimatedValue() + 
						0.25*aircraft.get_wing().get_meanAerodChordActual().getEstimatedValue())
				, SI.METER);
	}

	public void calculateVolumetricRatio(Aircraft aircraft) {
		if (_type == ComponentEnum.HORIZONTAL_TAIL) {
			_volumetricRatio = (_surface.divide(aircraft.get_wing().get_surface())).times(
					_ACw_ACdistance.divide(aircraft.get_wing().get_meanAerodChordActual())).getEstimatedValue();
		} else if(_type == ComponentEnum.VERTICAL_TAIL) {
			_volumetricRatio = (_surface.divide(aircraft.get_wing().get_surface())).times(
					_ACw_ACdistance.divide(aircraft.get_wing().get_span())).getEstimatedValue();
		}
	}

	/** 
	 * Returns the chord of the 
	 * equivalent wing at y station
	 * 
	 * @author Lorenzo Attanasio
	 * @param y in meter or foot
	 * @return
	 */
	private double getChordEquivalentAtY(Double y) {

		double chord = ((2 * _surface.getEstimatedValue())/
				(_span.getEstimatedValue() * (1+_taperRatioEquivalent))) * 
				(1-((2 * (1-_taperRatioEquivalent)/_span.getEstimatedValue()) * 
						y));
		return chord;

	}

	/** 
	 * Returns the chord of the 
	 * actual wing at y station
	 * 
	 * @author Lorenzo Attanasio
	 * @param y in meter or foot
	 * @return
	 */
	private double getChordSemispanAtYActual(Double y) {
		return MyMathUtils.getInterpolatedValue1DLinear(_yStationActual.toArray(), _chordsVsYActual.toArray(), y);
	}


	public double getChordAtYActual(Double y) {
		return standaloneutils.GeometryCalc.getChordAtYActual(_yStationActual.toArray(), _chordsVsYActual.toArray(), y);
	}

	/** 
	 * Get LE of the equivalent lifting surface 
	 * x coordinate at y location.
	 * 
	 * @param y
	 * @return
	 */
	private double getXLEAtYEquivalent(Double y){
		return (_xLETip.getEstimatedValue()/_span.getEstimatedValue()) * y;
	}

	/** 
	 * Get LE of the actual lifting surface 
	 * x coordinate at y location.
	 * 
	 * @param y
	 * @return
	 */
	private double getXLESemispanAtYActual(Double y){
		return MyMathUtils.getInterpolatedValue1DLinear(_yStationActual.toArray(), _xLEvsYActual.toArray(), y);
	}

	/** 
	 * Get LE of the actual lifting surface 
	 * x coordinate at y location.
	 * 
	 * @param y
	 * @return
	 */
	public double getXLEAtYActual(Double y){
		return standaloneutils.GeometryCalc.getXLEAtYActual(_yStationActual.toArray(), _xLEvsYActual.toArray(), y);
	}

	/**
	 * Return the dihedral at y semi-span station
	 * 
	 * @param y
	 * @return
	 */
	private Amount<Angle> getDihedralSemispanAtYActual(Double y){
		if (y <= _semispan.times(_spanStationKink).getEstimatedValue()) return _dihedralInnerPanel;
		else return _dihedralOuterPanel;
	}

	public Amount<Angle> getDihedralAtYActual(Double y){
		if (y >= 0) return getDihedralSemispanAtYActual(y);
		else return getDihedralSemispanAtYActual(-y);
	}


	////////////////////////////////////////////////////////////////
	// Methods for evaluating dependent parameters (mass, cd ...)
	////////////////////////////////////////////////////////////////

	public void calculateFormFactor(double compressibilityFactor) {
		// Wing Form Factor (ADAS pag 93 graphic or pag 9 meccanica volo appunti)
		_formFactor = ((1 + 1.2*get_thicknessMean()*
				Math.cos(get_sweepQuarterChordEq().doubleValue(SI.RADIAN))+
				100*Math.pow(compressibilityFactor,3)*
				(Math.pow(Math.cos(get_sweepQuarterChordEq().doubleValue(SI.RADIAN)),2))*
				Math.pow(get_thicknessMean(),4)));
	}


	/** 
	 * Calculate sweep at x percent of chord known sweep at LE
	 * 
	 * @param x
	 * @return 
	 */
	public Amount<Angle> calculateSweep(Double x) {
		return Amount.valueOf(
				Math.atan(
						Math.tan(_sweepLEEquivalent.getEstimatedValue()) -
						(4./_aspectRatio)*
						(x*(1 - _taperRatioEquivalent)/(1 + _taperRatioEquivalent))),
				SI.RADIAN);
	}


	/** 
	 * Calculate sweep at x percent of chord known sweep at y percent
	 * 
	 * @param sweepAtY
	 * @param x
	 * @param y
	 */
	public Amount<Angle> calculateSweep(double sweepAtY, double x, double y) {
		return calculators.geometry.LSGeometryCalc.calculateSweep(_aspectRatio, _taperRatioEquivalent, sweepAtY, x, y);
	}

	public void calculateMass(Aircraft aircraft, OperatingConditions conditions) {
		calculateMass(aircraft, conditions, MethodEnum.KROO);
		calculateMass(aircraft, conditions, MethodEnum.JENKINSON);
		calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_2013);
		calculateMass(aircraft, conditions, MethodEnum.RAYMER);
		calculateMass(aircraft, conditions, MethodEnum.NICOLAI_2013);
		calculateMass(aircraft, conditions, MethodEnum.HOWE);
		calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_1976);
	}

	/** 
	 * Calculate mass of the generic lifting surface
	 * 
	 * @author Lorenzo Attanasio
	 * @param aircraft
	 * @param method
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void calculateMass(
			Aircraft aircraft, 
			OperatingConditions conditions, 
			MethodEnum method) {

		List<MethodEnum> methodsList = new ArrayList<MethodEnum>();

		Double surface = _surface.to(MyUnits.FOOT2).getEstimatedValue();
		Double surfaceExposed = _surfaceExposed.to(MyUnits.FOOT2).getEstimatedValue();

		switch(_type) {
		case WING : {
			switch (method) {

			/* This method poor results
			case ROSKAM : { // Roskam page 85 (pdf) part V
				_methodsList.add(method);

				System.out.println("---" + _sweepHalfChordEq.to(SI.RADIAN));
				_mass = Amount.valueOf(
						Amount.valueOf(2*(0.00428*
						Math.pow(surface, 0.48)*
						_aspectRatio*
						Math.pow(aircraft.get_performances().get_machDive0(), 0.43)*
						Math.pow(aircraft.get_weights().get_MTOW().to(NonSI.POUND_FORCE).
								times(aircraft.get_performances().get_nUltimate()).
								getEstimatedValue(),0.84)*
								Math.pow(_taperRatioEquivalent, 0.14))/
								(Math.pow(100*_tc_root,0.76)*
										Math.pow(Math.cos(_sweepHalfChordEq.to(SI.RADIAN).getEstimatedValue()), 1.54)),
										NonSI.POUND_FORCE).to(NonSI.KILOGRAM_FORCE).getEstimatedValue(),
										SI.KILOGRAM);

				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
//			*/
			case KROO : { // page 430 Aircraft design synthesis
				methodsList.add(method);

				if (aircraft.get_powerPlant().get_engineType().equals(EngineTypeEnum.TURBOPROP)) {
					_mass = Amount.valueOf((4.22*surface +
							1.642e-6*
							(aircraft.get_performances().get_nUltimate()*
									Math.pow(_span.to(NonSI.FOOT).getEstimatedValue(),3)*
									Math.sqrt(aircraft.get_weights().get_MTOM().to(NonSI.POUND).getEstimatedValue()*
											aircraft.get_weights().get_MZFM().to(NonSI.POUND).getEstimatedValue())*
									(1 + 2*_taperRatioEquivalent))/
							(_thicknessMean*Math.pow(Math.cos(_sweepQuarterChordEq.to(SI.RADIAN).getEstimatedValue()),2)*
									surface*(1 + _taperRatioEquivalent))),
							NonSI.POUND).to(SI.KILOGRAM);
					_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
				} else {
					_mass = null;
					_massMap.put(method, null);
				}
			} break;

			case JENKINSON : { // page 134 Jenkinson - Civil Jet Aircraft Design

				methodsList.add(method);

				if (!aircraft.get_powerPlant().get_engineType().equals(EngineTypeEnum.TURBOPROP)) {

					double R, kComp;

					if (_compositeCorretionFactor != null) {
						kComp = _compositeCorretionFactor;
					} else {
						kComp = 0.;					
					}

					for (int i = 0; i < 10; i++) {

						try {
							R = _mass.getEstimatedValue() + aircraft.get_theFuelTank().get_fuelMass().getEstimatedValue() +
									((2*(aircraft.get_theNacelles().get_totalMass().getEstimatedValue() + 
											aircraft.get_powerPlant().get_massDryEngineActual().getEstimatedValue())*
											aircraft.get_theNacelles().get_distanceBetweenInboardNacellesY())/
											(0.4*_span.getEstimatedValue())) + 
									((2*(aircraft.get_theNacelles().get_totalMass().getEstimatedValue() + 
											aircraft.get_powerPlant().get_massDryEngineActual().getEstimatedValue())*
											aircraft.get_theNacelles().get_distanceBetweenOutboardNacellesY())/
											(0.4*_span.getEstimatedValue()));
						} catch(NullPointerException e) {R = 0.;}

						_mass = Amount.valueOf(
								(1 - kComp) * 0.021265*
								(pow(aircraft.get_weights().get_MTOM().getEstimatedValue()*
										aircraft.get_performances().get_nUltimate(),0.4843)*
										pow(_surface.getEstimatedValue(),0.7819)*
										pow(_aspectRatio,0.993)*
										pow(1 + _taperRatioEquivalent,0.4)*
										pow(1 - R/aircraft.get_weights().get_MTOM().getEstimatedValue(),0.4))/
								(cos(_sweepQuarterChordEq.to(SI.RADIAN).getEstimatedValue())*
										pow(_thicknessMean,0.4)), 
								SI.KILOGRAM);
					}
					_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
				} else {
					_mass = null;
					_massMap.put(method, null);
				}
			} break;

			/* This method gives poor results
			case RAYMER : { // page 403 (211 pdf) Raymer 
				_methodsList.add(method);
				_mass = Amount.valueOf(0.0051 * pow(aircraft.get_weights().
						get_MTOW().to(NonSI.POUND_FORCE).times(aircraft.get_performances().
								get_nUltimate()).getEstimatedValue(),
								0.557)*
								pow(_surface.to(MyUnits.FOOT2).getEstimatedValue(),0.649)*
								pow(_aspectRatio, 0.5)*
								pow(_tc_root, -0.4)*
								pow(1+_taperRatioEquivalent, 0.1)*
								pow(cos(_sweepQuarterChordEq.to(SI.RADIAN).getEstimatedValue()), -1)*
								pow(_surfaceCS.to(MyUnits.FOOT2).getEstimatedValue(), 0.1), NonSI.POUND).
								to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 *//*
			case SADRAY : { // page 583 pdf Sadray Aircraft Design System Engineering Approach
				// results very similar to Jenkinson
				_methodsList.add(method);
				Double _kRho = 0.0035;
				_mass = Amount.valueOf(
						_surface.getEstimatedValue()*
						_meanAerodChordCk.getEstimatedValue()*
						(_tc_root)*aircraft.get_weights().get_materialDensity().getEstimatedValue()*
						_kRho*
						pow((_aspectRatio*aircraft.get_performances().get_nUltimate())/
								cos(_sweepQuarterChordEq.to(SI.RADIAN).getEstimatedValue()),0.6)*
								pow(_taperRatioEquivalent, 0.04), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			  */
			/* The method gives an average 20 percent difference from real value 
		case TORENBEEK_1982 : {
			_methodsList.add(method);
			_mass = Amount.valueOf(
					0.0017*
					aircraft.get_weights().get_MZFW().to(NonSI.POUND_FORCE).getEstimatedValue()*
					Math.pow(_span.to(NonSI.FOOT).getEstimatedValue()/
							Math.cos(_sweepHalfChordEq.to(SI.RADIAN).getEstimatedValue()),0.75)*
					(1 + Math.pow(6.3*Math.cos(_sweepHalfChordEq.to(SI.RADIAN).getEstimatedValue())/
							_span.to(NonSI.FOOT).getEstimatedValue(), 0.5))*
					Math.pow(aircraft.get_performances().get_nUltimate(), 0.55)*
					Math.pow(
							_span.to(NonSI.FOOT).getEstimatedValue()*surface/
							(_tc_root*_chordRoot.to(NonSI.FOOT).getEstimatedValue()*
									aircraft.get_weights().get_MZFW().to(NonSI.POUND_FORCE).getEstimatedValue()*
									Math.cos(_sweepHalfChordEq.to(SI.RADIAN).getEstimatedValue())), 0.3)
					, NonSI.POUND).to(SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
			 */
			case TORENBEEK_2013 : { // page 253 pdf
				methodsList.add(method);

				if (aircraft.get_powerPlant().get_engineType().equals(EngineTypeEnum.TURBOPROP)) {
					_mass = Amount.valueOf(
							(0.0013*
									aircraft.get_performances().get_nUltimate()*
									Math.pow(aircraft.get_weights().get_MTOW()
											.times(aircraft.get_weights().get_MZFW()).getEstimatedValue(), 
											0.5)*
									0.36*Math.pow(1 + _taperRatioEquivalent, 0.5)*
									(_span.getEstimatedValue()/100)*
									(_aspectRatio/
											(_thicknessMean
													*Math.pow(
															Math.cos(_sweepHalfChordEq.to(SI.RADIAN).getEstimatedValue())
															, 2))) +
									210*_surface.getEstimatedValue())/
							AtmosphereCalc.g0.getEstimatedValue()
							, SI.KILOGRAM);
					_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
				} else {
					_mass = null;
					_massMap.put(method, null);
				}

			}

			default : { }
			}
		} break;
		////////////////////////////////////
		////////////////////////////////////
		case HORIZONTAL_TAIL : {
			switch (method) {

			/*
			case HOWE : { // page 381 Howe Aircraft Conceptual Design Synthesis
				_methodsList.add(method);
				_mass = Amount.valueOf(0.047*
						aircraft.get_performances().get_vDiveEAS().getEstimatedValue()*
						pow(_surface.getEstimatedValue(), 1.24), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			case JENKINSON : { // Jenkinson page 149 pdf
				methodsList.add(method);
				_mass = Amount.valueOf(22*_surface.getEstimatedValue(), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case NICOLAI_2013 : {
				methodsList.add(method);
				double gamma = pow(aircraft.get_weights().get_MTOM().to(NonSI.POUND).getEstimatedValue()*
						aircraft.get_performances().get_nUltimate(), 0.813)*
						pow(_surface.to(MyUnits.FOOT2).getEstimatedValue(), 0.584)*
						pow(_span.getEstimatedValue()/
								(_tc_root*_chordRoot.getEstimatedValue()), 0.033) * 
						pow(aircraft.get_wing().get_meanAerodChordActual().getEstimatedValue()/
								_ACw_ACdistance.getEstimatedValue(), 0.28);

				_mass = Amount.valueOf(0.0034 * 
						pow(gamma, 0.915), NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case RAYMER : { // Raymer page 211 pdf
				methodsList.add(method);
				_mass = Amount.valueOf(0.0379 * 
						pow(aircraft.get_weights().get_MTOM().to(NonSI.POUND).getEstimatedValue(), 0.639)*
						pow(aircraft.get_performances().get_nUltimate(), 0.1) * 
						pow(_ACw_ACdistance.to(NonSI.FOOT).getEstimatedValue(), -1.) *
						pow(_surface.to(MyUnits.FOOT2).getEstimatedValue(), 0.75) * 
						pow(0.3*_ACw_ACdistance.to(NonSI.FOOT).getEstimatedValue(), 0.704) * 
						pow(cos(_sweepQuarterChordEq.to(SI.RADIAN).getEstimatedValue()), -1) *
						pow(_aspectRatio, 0.166) * 
						pow(1 + aircraft.get_fuselage().get_equivalentDiameterCylinderGM().to(NonSI.FOOT).getEstimatedValue()/
								_span.to(NonSI.FOOT).getEstimatedValue(), -0.25) * 
						pow(1 + _surfaceCS.to(MyUnits.FOOT2).getEstimatedValue()/
								_surface.to(MyUnits.FOOT2).getEstimatedValue(), 0.1),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case KROO : {
				methodsList.add(method);
				_mass = Amount.valueOf((5.25*surfaceExposed +
						0.8e-6*
						(aircraft.get_performances().get_nUltimate()*
								Math.pow(_span.to(NonSI.FOOT).getEstimatedValue(),3)*
								aircraft.get_weights().get_MTOM().to(NonSI.POUND).getEstimatedValue()*
								_meanAerodChordActual.to(NonSI.FOOT).getEstimatedValue()*
								Math.sqrt(surfaceExposed))/
						(_thicknessMean*Math.pow(Math.cos(_sweepStructuralAxis.to(SI.RADIAN).getEstimatedValue()),2)*
								_ACw_ACdistance.to(NonSI.FOOT).getEstimatedValue()*Math.pow(surface,1.5))),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			/* Average error > 50 %
			case SADRAY : { // page 584 pdf Sadray Aircraft Design System Engineering Approach
				_methodsList.add(method);
				// TODO ADD kRho table
				Double _kRho = 0.0275;
				_mass = Amount.valueOf(
						_surface.getEstimatedValue()*
						_meanAerodChordCk.getEstimatedValue()*
						(_tc_root)*aircraft.get_weights().get_materialDensity().getEstimatedValue()*
						_kRho*
						pow(_aspectRatio/
								cos(_sweepQuarterChordEq.getEstimatedValue()),0.6)*
								pow(_taperRatioEquivalent, 0.04)*
								pow(_volumetricRatio, 0.3)*
								pow(_CeCt, 0.4), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			/*
			case TORENBEEK_1976 : { // Roskam page 90 (pdf) part V
				_methodsList.add(method);
				double kh = 1.;
				if (_variableIncidence == true) { kh = 1.1;}

				_mass = Amount.valueOf(kh*3.81*
						aircraft.get_performances().get_vDiveEAS().to(NonSI.KNOT).getEstimatedValue()*
						pow(_surface.to(MyUnits.FOOT2).getEstimatedValue(), 1.2)/
						(1000*sqrt(cos(_sweepQuarterChordEq.to(SI.RADIAN).getEstimatedValue()))) - 0.287,
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			default : { } break;
			}
		} break;
		////////////////////////////////////
		////////////////////////////////////
		case VERTICAL_TAIL : {
			switch (method) {

			case HOWE : { // page 381 Howe Aircraft Conceptual Design Synthesis
				methodsList.add(method);
				double k = 0.;
				if (_positionRelativeToAttachment == 1.0) {
					k = 1.5;
				} else {
					k = 1.;
				}
				_mass = Amount.valueOf(0.05*k*
						aircraft.get_performances().get_vDiveEAS().getEstimatedValue()*
						pow(_surface.getEstimatedValue(), 1.15), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;


			case JENKINSON : {
				methodsList.add(method);
				_mass = Amount.valueOf(22*_surface.getEstimatedValue(), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			/* Average error > 50 %
			case RAYMER : { // Raymer page 211 pdf
				_methodsList.add(method);
				_mass = Amount.valueOf(0.0026 * 
						pow(aircraft.get_weights().get_MTOM().to(NonSI.POUND).getEstimatedValue(), 0.556)*
						pow(aircraft.get_performances().get_nUltimate(), 0.536) * 
						pow(_ACw_ACdistance.to(NonSI.FOOT).getEstimatedValue(), -0.5) *
						pow(_surface.to(MyUnits.FOOT2).getEstimatedValue(), 0.5) * 
						pow(0.3*_ACw_ACdistance.to(NonSI.FOOT).getEstimatedValue(), 0.875) * 
						pow(cos(_sweepQuarterChordEq.to(SI.RADIAN).getEstimatedValue()), -1.) *
						pow(_aspectRatio, 0.35) * 
						pow(_tc_root, -0.5) *
						pow(1 + _positionRelativeToAttachment, 0.225),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			case TORENBEEK_1976 : { // Roskam page 90 (pdf) part V
				methodsList.add(method);
				double kv = 1.;
				if (_positionRelativeToAttachment == 1.) { 
					kv = 1 + 0.15*
							(aircraft.get_HTail().get_surface().getEstimatedValue()/
									_surface.getEstimatedValue())*
							_positionRelativeToAttachment;}
				_mass = Amount.valueOf(kv*3.81*
						aircraft.get_performances().get_vDiveEAS().to(NonSI.KNOT).getEstimatedValue()*
						pow(_surface.to(MyUnits.FOOT2).getEstimatedValue(), 1.2)/
						(1000*sqrt(cos(_sweepHalfChordEq.to(SI.RADIAN).getEstimatedValue()))) - 0.287,
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case KROO : {
				methodsList.add(method);
				_mass = Amount.valueOf((2.62*surface +
						1.5e-5*
						(aircraft.get_performances().get_nUltimate()*
								Math.pow(_span.to(NonSI.FOOT).getEstimatedValue(),3)*(
										8.0 + 0.44*aircraft.get_weights().get_MTOW().to(NonSI.POUND_FORCE).getEstimatedValue()/
										aircraft.get_wing().get_surface().to(MyUnits.FOOT2).getEstimatedValue())/
								(_thicknessMean*Math.pow(Math.cos(_sweepStructuralAxis.getEstimatedValue()),2)))),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			/* Average error > 50 %
			case SADRAY : { // page 584 pdf Sadray Aircraft Design System Engineering Approach
				_methodsList.add(method);
				// TODO ADD kRho table
				Double _kRho = 0.05;
				_mass = Amount.valueOf(
						_surface.getEstimatedValue()*
						_meanAerodChordCk.getEstimatedValue()*
						(_tc_root)*aircraft.get_weights().get_materialDensity().getEstimatedValue()*
						_kRho*
						pow(_aspectRatio/
								cos(_sweepQuarterChordEq.getEstimatedValue()),0.6)*
								pow(_taperRatioEquivalent, 0.04)*
								pow(_volumetricRatio, 0.2)*
								pow(_CeCt, 0.4), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			}break;
			 */

			default : { } break;
			}
		} break;
		case CANARD : {

		} break;
		}

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, methodsList);
		_percentDifference =  new Double[_massMap.size()]; 

		_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_massReference, 
				_massMap,
				_percentDifference,
				20.).getFilteredMean(), SI.KILOGRAM);

		if (_massCorrectionFactor != null && _massEstimated != null) {
			_massEstimated = _massEstimated.times(_massCorrectionFactor);
		}

		_mass = Amount.valueOf(_massEstimated.getEstimatedValue(), SI.KILOGRAM);

	}

	public void calculateCG(Aircraft aircraft, OperatingConditions conditions) {
		calculateCG(aircraft, conditions, MethodEnum.SFORZA);
		calculateCG(aircraft, conditions, MethodEnum.TORENBEEK_1982);
	}

	/**
	 * Evaluate lifting surface center of gravity.
	 * xCG always refers to local coordinate system 
	 * (the origin is the lifting surface apex)
	 * 
	 * @author Lorenzo Attanasio
	 * @param method
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void calculateCG(
			Aircraft aircraft, 
			OperatingConditions conditions,
			MethodEnum method) {

		List<MethodEnum> methodsList = new ArrayList<MethodEnum>();

		_cg.setLRForigin(_X0, _Y0, _Z0);
		_cg.set_xLRFref(_chordRoot.times(0.4));
		_cg.set_yLRFref(_span.times(0.5*0.4));
		_cg.set_zLRFref(Amount.valueOf(0., SI.METER));

		// Initialize _methodsList again to clear it
		// from old entries
		methodsList = new ArrayList<MethodEnum>();

		_xCG = Amount.valueOf(0., SI.METER);
		_yCG = Amount.valueOf(0., SI.METER);
		_zCG = Amount.valueOf(0., SI.METER);

		Double lambda = _taperRatioEquivalent,
				span = _span.getEstimatedValue(),
				xRearSpar,
				xFrontSpar;

		switch (_type) {
		case WING : {
			switch(method) {

			//		 Bad results ...
			case SFORZA : { // page 359 Sforza (2014) - Aircraft Design
				methodsList.add(method);
				_yCG = Amount.valueOf(
						(span/6) * 
						((1+2*lambda)/(1-lambda)),
						SI.METER);

				_xCG = Amount.valueOf(
						getChordEquivalentAtY(_yCG.getEstimatedValue())
						+ getXLEAtYEquivalent(_yCG.getEstimatedValue())
						, SI.METER);
				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			// page 359 Sforza (2014) - Aircraft Design
			// page 313 Torenbeek (1982)
			case TORENBEEK_1982 : { 
				methodsList.add(method);
				_yCG = Amount.valueOf(
						0.35*(span/2) 
						, SI.METER);

				xRearSpar = 0.6*getChordEquivalentAtY(_yCG.getEstimatedValue());
				xFrontSpar = 0.25*getChordEquivalentAtY(_yCG.getEstimatedValue());

				_xCG = Amount.valueOf(
						0.7*(xRearSpar - xFrontSpar)
						+ 0.25*getChordEquivalentAtY(_yCG.getEstimatedValue())
						+ getXLEAtYEquivalent(_yCG.getEstimatedValue())
						, SI.METER);

				//				System.out.println("x: " + _xCG 
				//				+ ", y: " + _yCG 
				//				+ ", xLE: " + getXLEAtYEquivalent(_yCG.getEstimatedValue()));
				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;

			}

		} break;

		case HORIZONTAL_TAIL : {

			switch(method) {

			// page 359 Sforza (2014) - Aircraft Design
			// page 313 Torenbeek (1982)
			case TORENBEEK_1982 : { 
				methodsList.add(method);
				_yCG = Amount.valueOf(
						0.38*(span/2) 
						, SI.METER);

				_xCG = Amount.valueOf(
						0.42*getChordEquivalentAtY(_yCG.getEstimatedValue())
						+ getXLEAtYEquivalent(_yCG.getEstimatedValue())
						, SI.METER);

				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;
			}
		} break;

		case VERTICAL_TAIL : {

			switch(method) {

			// page 359 Sforza (2014) - Aircraft Design
			// page 313 Torenbeek (1982)
			case TORENBEEK_1982 : { 
				methodsList.add(method);

				if (_positionRelativeToAttachment > 0.8) {
					_yCG = Amount.valueOf(
							0.55*(span/2) 
							, SI.METER);
					_xCG = Amount.valueOf(
							0.42*getChordEquivalentAtY(_yCG.getEstimatedValue())
							+ getXLEAtYEquivalent(_yCG.getEstimatedValue())
							, SI.METER);
				} else {
					_yCG = Amount.valueOf(
							0.38*(span/2) 
							, SI.METER);
					_xCG = Amount.valueOf(
							0.42*getChordEquivalentAtY(_yCG.getEstimatedValue())
							+ getXLEAtYEquivalent(_yCG.getEstimatedValue())
							, SI.METER);
				}

				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;
			}
		} break;

		case CANARD : {

		} break;

		default : {} break;

		}

		_methodsMap.put(AnalysisTypeEnum.BALANCE, methodsList);
		_percentDifferenceXCG = new Double[_xCGMap.size()];
		_percentDifferenceYCG = new Double[_yCGMap.size()];

		_cg.set_xLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.get_xLRFref(), 
				_xCGMap,
				_percentDifferenceXCG,
				30.).getFilteredMean(), SI.METER));

		_cg.set_yLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.get_yLRFref(), 
				_yCGMap,
				_percentDifferenceYCG,
				30.).getFilteredMean(), SI.METER));

		_cg.calculateCGinBRF();

	}

	private void getAirfoilsPropertiesAsArray() {

		System.out.println("--- Calling getAirfoilsPropertiesAsArray ---");

		// Remove airfoils which have the same y (that is, eta) coordinate
		for (int i = 0; i < _numberOfAirfoils - 1; i++) {
			if (_theAirfoilsList.get(i).getGeometry().get_yStation()
					.equals(_theAirfoilsList.get(i+1).getGeometry().get_yStation())) {
				_theAirfoilsList.remove(i);
			}
		}

		_numberOfAirfoils = _theAirfoilsList.size();

		_etaAirfoil = new MyArray();
		_twistVsY = new MyArray(SI.RADIAN);
		_yStationsAirfoil = new MyArray();
		_chordVsYAirfoils = new MyArray();
		_distanceAirfoilACFromWingAC = new MyArray();
		_xAcAirfoil = new MyArray();
		_alpha0VsY = new MyArray();
		_alphaStar_y = new MyArray();
		_alphaStall = new MyArray();
		_clAlpha_y = new MyArray();
		_clBasic_y = new MyArray();
		_clStar_y = new MyArray();
		_clMaxVsY = new MyArray();
		_clMaxSweep_y = new MyArray();
		_clAtCdMin_y = new MyArray();
		_cdMin_y = new MyArray();
		_cmAC_y = new MyArray();
		_cmAlphaLE_y = new MyArray();
		_aerodynamicCenterXcoord_y = new MyArray();
		_maxThicknessVsY = new MyArray();
		_reynoldsCruise_y = new MyArray();
		_kFactorDragPolar_y = new MyArray();

		for (int i = 0; i < _numberOfAirfoils; i++){
			_etaAirfoil.add(_theAirfoilsList.get(i).getGeometry().get_etaStation());
			_twistVsY.add(_theAirfoilsList.get(i).getGeometry().get_twist().getEstimatedValue());
			_yStationsAirfoil.add(_theAirfoilsList.get(i).getGeometry().get_yStation());
			_chordVsYAirfoils.add(getChordAtYActual(_yStationsAirfoil.get(i)));
			_maxThicknessVsY.add(_theAirfoilsList.get(i).getGeometry().get_maximumThicknessOverChord().doubleValue());

			_alpha0VsY.add(_theAirfoilsList.get(i).getAerodynamics().get_alphaZeroLift().getEstimatedValue());
			System.out.println("alpha zero lift wing " + _theAirfoilsList.get(i).getAerodynamics().get_alphaZeroLift().to(NonSI.DEGREE_ANGLE).getEstimatedValue());
			_alphaStar_y.add(_theAirfoilsList.get(i).getAerodynamics().get_alphaStar().getEstimatedValue());
			_clAlpha_y.add(_theAirfoilsList.get(i).getAerodynamics().get_clAlpha().doubleValue());
			_alphaStall.add(_theAirfoilsList.get(i).getAerodynamics().get_alphaStall().getEstimatedValue());
			_clStar_y.add(_theAirfoilsList.get(i).getAerodynamics().get_clStar().doubleValue());
			_clMaxVsY.add(_theAirfoilsList.get(i).getAerodynamics().get_clMax().doubleValue());
			_clMaxSweep_y.add(_theAirfoilsList.get(i).getAerodynamics().get_clMax().doubleValue()*pow(cos(_sweepLEEquivalent.getEstimatedValue()),2));
			_clAtCdMin_y.add(_theAirfoilsList.get(i).getAerodynamics().get_clAtCdMin().doubleValue());
			_cdMin_y.add(_theAirfoilsList.get(i).getAerodynamics().get_cdMin().doubleValue());
			_cmAC_y.add(_theAirfoilsList.get(i).getAerodynamics().get_cmAC().doubleValue());
			_cmAlphaLE_y.add(_theAirfoilsList.get(i).getAerodynamics().get_cmAlphaLE().doubleValue());
			_aerodynamicCenterXcoord_y.add(_theAirfoilsList.get(i).getAerodynamics().get_aerodynamicCenterX().doubleValue());
			_reynoldsCruise_y.add(_theAirfoilsList.get(i).getAerodynamics().get_reynoldsCruise().doubleValue());
			_kFactorDragPolar_y.add(_theAirfoilsList.get(i).getAerodynamics().get_kFactorDragPolar().doubleValue());
		}

		_etaAirfoil.toArray();
		_twistVsY.toArray();
		_yStationsAirfoil.toArray();
		_chordVsYAirfoils.toArray();
		_alpha0VsY.toArray();
		_alphaStar_y.toArray();
		_alphaStall.toArray();
		_clAlpha_y.toArray();
		_clStar_y.toArray();
		_clMaxVsY.toArray();
		_clMaxSweep_y.toArray();
		_clAtCdMin_y.toArray();
		_cdMin_y.toArray();
		_cmAC_y.toArray();
		_cmAlphaLE_y.toArray();
		_aerodynamicCenterXcoord_y.toArray();
		_reynoldsCruise_y.toArray();
		_kFactorDragPolar_y.toArray();
		_maxThicknessVsY.toArray();
		_thicknessMax = _maxThicknessVsY.getMax();

		_chordsVsYEq.interpolate(_eta.toArray(), _etaAirfoil.toArray());
		_twistVsY.interpolate(_etaAirfoil.toArray(), _eta.toArray());
		_maxThicknessVsY.interpolate(_etaAirfoil.toArray(), _eta.toArray());

	}

	//	private void getAirfoilsPropertiesAsArrayExposed(Aircraft aircraft) {
	//		// Remove airfoils which have the same y (that is, eta) coordinate
	//				for (int i = 0; i < _numberOfAirfoils - 1; i++) {
	//					if (_theAirfoilsListExposed.get(i).getGeometry().get_yStation()
	//							.equals(_theAirfoilsListExposed.get(i+1).getGeometry().get_yStation())) {
	//						_theAirfoilsListExposed.remove(i);
	//					}
	//				}
	//		
	//		_numberOfAirfoils = _theAirfoilsListExposed.size();
	//
	//		_etaAirfoilExposed = new MyArray();
	//		_twistVsYExposed = new MyArray(SI.RADIAN);
	//		_yStationsAirfoilExposed = new MyArray();
	//		_alpha0VsYExposed = new MyArray();
	//		_chordVsYAirfoilsExposed = new MyArray();
	//		_eta.setDouble(MyArrayUtils.linspace(0., 1., _numberOfPointsChordDistribution));
	//		_yStationActual.setRealVector(_eta.getRealVector().mapMultiply(0.5*_span.doubleValue(SI.METER)));
	//		
	//		for (int i = 0; i < _numberOfAirfoils; i++){
	//		_etaAirfoilExposed.add(_theAirfoilsListExposed.get(i).getGeometry().get_etaStation());
	//		_twistVsYExposed.add(_theAirfoilsListExposed.get(i).getGeometry().get_twist().getEstimatedValue());
	//		_yStationsAirfoilExposed.add(_theAirfoilsListExposed.get(i).getGeometry().get_yStation());
	//		System.out.println(" y station exposed " + _yStationsAirfoilExposed.get(i));
	//		System.out.println(" yStation actual " + _yStationActual.toString());
	//		_chordVsYAirfoilsExposed.add(getChordAtYActual(_yStationsAirfoilExposed.get(i)));
	//		_alpha0VsYExposed.add(_theAirfoilsListExposed.get(i).getAerodynamics().get_alphaZeroLift().getEstimatedValue());
	//		}
	//		
	//		
	//		
	//	}
	//-------------------------------------------------------------------


	////////////////////////////////////////////////
	// Getters and Setters
	////////////////////////////////////////////////

	public MyArray get_alphaStall() {
		return _alphaStall;
	}

	public void set_alphaStall(MyArray _alphaStall) {
		this._alphaStall = _alphaStall;
	}

	public MyArray get_clAlpha_y() {
		return _clAlpha_y;
	}

	public void set_clAlpha_y(MyArray _clAlpha_y) {
		this._clAlpha_y = _clAlpha_y;
	}

	public MyArray get_alphaStar_y() {
		return _alphaStar_y;
	}

	public void set_alphaStar_y(MyArray _alphaStar_y) {
		this._alphaStar_y = _alphaStar_y;
	}

	public MyArray get_kFactorDragPolar_y() {
		return _kFactorDragPolar_y;
	}

	public MyArray get_aerodynamicCenterXcoord_y() {
		return _aerodynamicCenterXcoord_y;
	}

	public MyArray get_cmAlphaLE_y() {
		return _cmAlphaLE_y;
	}

	public MyArray get_cmAC_y() {
		return _cmAC_y;
	}

	public MyArray get_cdMin_y() {
		return _cdMin_y;
	}

	public MyArray get_clAtCdMin_y() {
		return _clAtCdMin_y;
	}

	public MyArray get_clMaxSweep_y() {
		return _clMaxSweep_y;
	}

	public MyArray get_clStar_y() {
		return _clStar_y;
	}

	public Amount<Area> get_surface() {
		return _surface;
	}

	public void set_surface(Amount<Area> _surface) {
		this._surface = _surface;
	}

	public Amount<Area> get_surfaceCranked() {
		return _surfaceCranked;
	}

	public void set_surfaceCranked(Amount<Area> _surfaceCranked) {
		this._surfaceCranked = _surfaceCranked;
	}

	public Amount<Area> get_semiSurfaceInnerPanel() {
		return _semiSurfaceInnerPanel;
	}

	public void set_semiSurfaceInnerPanel(Amount<Area> _semiSurfaceInnerPanel) {
		this._semiSurfaceInnerPanel = _semiSurfaceInnerPanel;
	}
	public Amount<Area> get_semiSurfaceOuterPanel() {
		return _semiSurfaceOuterPanel;
	}

	public void set_semiSurfaceOuterPanel(Amount<Area> _semiSurfaceOuterPanel) {
		this._semiSurfaceOuterPanel = _semiSurfaceOuterPanel;
	}


	public Double get_aspectRatio() {
		return _aspectRatio;
	}

	public void set_aspectRatio(Double _aspectRatio) {
		this._aspectRatio = _aspectRatio;
	}

	public Double get_aspectRatioInnerPanel() {
		return _aspectRatioInnerPanel;
	}

	public void set_aspectRatioInnerPanel(Double _aspectRatioInnerPanel) {
		this._aspectRatioInnerPanel = _aspectRatioInnerPanel;
	}

	public Double get_aspectRatioOuterPanel() {
		return _aspectRatioOuterPanel;
	}

	public void set_aspectRatioOuterPanel(Double _aspectRatioOuterPanel) {
		this._aspectRatioOuterPanel = _aspectRatioOuterPanel;
	}	

	public Double get_taperRatioActual() {
		return _taperRatioActual;
	}

	public void _taperRatioCrankedWing(Double _taperRatioCrankedWing) {
		this._taperRatioActual = _taperRatioCrankedWing;
	}

	public Double get_taperRatioInnerPanel() {
		return _taperRatioInnerPanel;
	}

	public void _taperRatioInnerPanel(Double _taperRatioInnerPanel) {
		this._taperRatioInnerPanel = _taperRatioInnerPanel;
	}

	public Double get_taperRatioOuterPanel() {
		return _taperRatioOuterPanel;
	}

	public void _taperRatioOuterPanel(Double _taperRatioOuterPanel) {
		this._taperRatioOuterPanel = _taperRatioOuterPanel;
	}

	public Double get_spanStationKink() {
		return _spanStationKink;
	}

	public void set_spanStationKink(Double _spanStationKink) {
		this._spanStationKink = _spanStationKink;
	}

	public Double get_extensionLERootChordLinPanel() {
		return _extensionLERootChordLinPanel;
	}

	public void set_extensionLERootChordLinPanel(Double _extensionLERootChordLinPanel) {
		this._extensionLERootChordLinPanel = _extensionLERootChordLinPanel;
	}


	public Double get_extensionTERootChordLinPanel() {
		return _extensionTERootChordLinPanel;
	}

	public void set_extensionTERootChordLinPanel(Double _extensionTERootChordLinPanel) {
		this._extensionTERootChordLinPanel = _extensionTERootChordLinPanel;
	}



	public Amount<Length> get_chordRootEquivalentWing() {
		return 	_chordRootEquivalentWing;
	}

	public void set_chordRootEquivalentWing(Amount<Length> _chordRootEquivalentWing) {
		this._chordRootEquivalentWing = _chordRootEquivalentWing;
	}

	public Amount<Length> get_chordRoot() {
		return 	_chordRoot;
	}

	public void set_chordRoot(Amount<Length> _chordRoot) {
		this._chordRoot = _chordRoot;
	}

	public Amount<Length> get_chordKink() {
		return _chordKink;
	}

	public void set_chordKink(Amount<Length> _chordKink) {
		this._chordKink = _chordKink;
	}

	public Amount<Length> get_xLEKink() {
		return _xLEKink;
	}

	public MyArray get_alpha0VsYExposed() {
		return _alpha0VsYExposed;
	}

	public void set_alpha0VsYExposed(MyArray _alpha0VsYExposed) {
		this._alpha0VsYExposed = _alpha0VsYExposed;
	}

	public void set_xLEKink(Amount<Length> _xLEKink) {
		this._xLEKink = _xLEKink;
	}

	public Amount<Length> get_chordTip() {
		return _chordTip;
	}

	public void set_chordTip(Amount<Length> _chordTip) {
		this._chordTip = _chordTip;
	}

	public Amount<Length> get_xLETip() {
		return _xLETip;
	}

	public void set_xLETip(Amount<Length> _xLETip) {
		this._xLETip = _xLETip;
	}

	public Amount<Length> get_x_LE_Root_eq() {
		return	_x_LE_Root_eq;
	}

	public void set_x_LE_Root_eq(Amount<Length> _x_LE_Root_eq) {
		this._x_LE_Root_eq = _x_LE_Root_eq;
	}

	public Amount<Length> get_geomChordEq() {
		return	_geomChordEq;
	}

	public void set_geomChordEq(Amount<Length> _geomChordEq) {
		this._geomChordEq = _geomChordEq;
	}

	public Amount<Length> get_meanAerodChordEq() {
		return	_meanAerodChordEq;
	}

	public void set_meanAerodChordEq(Amount<Length> _aerodChordEq) {
		this._meanAerodChordEq = _aerodChordEq;
	}

	public Amount<Length> get_meanAerodChordActual() {
		return	_meanAerodChordActual;
	}

	public void set_meanAerodChordActual(Amount<Length> _aerodChordCk) {
		this._meanAerodChordActual = _aerodChordCk;
	}


	public Amount<Length> get_x_LE_Mac_Eq() {
		return	_x_LE_Mac_Eq;
	}

	public void set_x_LE_Mac_Eq(Amount<Length> _x_LE_Mac_Eq) {
		this._x_LE_Mac_Eq = _x_LE_Mac_Eq;
	}

	public Amount<Length> get_y_LE_Mac_Eq() {
		return	_y_LE_Mac_Eq;
	}

	public void set_y_LE_Mac_Eq(Amount<Length> _y_LE_Mac_Eq) {
		this._y_LE_Mac_Eq = _y_LE_Mac_Eq;
	}




	public Amount<Angle> get_sweepLEInnerPanel() {

		return _sweepLEInnerPanel;
	}

	public void set_sweepLEInnerPanel(Amount<Angle> _sweepLEInnerPanel) {
		this._sweepLEInnerPanel = _sweepLEInnerPanel;
	}


	public Amount<Angle> get_sweepLEOuterPanel() {

		return _sweepLEOuterPanel;
	}

	public void set_sweepLEOuterPanel(Amount<Angle> _sweepLEOuterPanel) {
		this._sweepLEOuterPanel = _sweepLEOuterPanel;
	}	


	public Amount<Angle> get_sweepQuarterChordInnerPanel() {

		return _sweepQuarterChordInnerPanel;
	}

	public void set_sweepQuarterChordInnerPanel(Amount<Angle> _sweepQuarterChordInnerPanel) {
		this._sweepQuarterChordInnerPanel = _sweepQuarterChordInnerPanel;
	}

	public Amount<Angle> get_sweepQuarterChordOuterPanel() {
		return _sweepQuarterChordOuterPanel;
	}

	public void set_sweepQuarterChordOuterPanel(Amount<Angle> _sweepQuarterChordOuterPanel) {
		this._sweepQuarterChordOuterPanel = _sweepQuarterChordOuterPanel;
	}

	public void set_sweepLEEquivalent(Amount<Angle> _sweepLEEquivalentWing) {
		this._sweepLEEquivalent = _sweepLEEquivalentWing;
	}


	public Amount<Length> get_semiSpanInnerPanel() {
		return 	_semiSpanInnerPanel;
	}

	public void set_semiSpanInnerPanel(Amount<Length> _semiSpanInnerPanel) {
		this._semiSpanInnerPanel = 	_semiSpanInnerPanel;
	}

	public Amount<Length> get_span() {
		return 	_span;
	}

	public void set_span(Amount<Length> _span) {
		this._span = _span;
	}

	public Amount<Length> get_xLERoot() {
		return _xLERoot;
	}

	public void set_xLERoot(Amount<Length> _xLERoot) {
		this._xLERoot = _xLERoot;
	}	


	public List<Amount<Length>> get_chordsActualVsYList() {
		return _chordsVsYActual.getAmountList();
	}

	public List<Amount<Length>> get_chordsEqList() {
		return _chordsVsYEq.getAmountList();
	}

	public List<Double> get_eta(){
		return _eta.getList();
	}

	public Amount<Length> get_xLEMacActualLRF() {
		return _xLEMacActualLRF;
	}

	public void set_xLEMacActualLRF(Amount<Length> _x_LE_Mac_Ck) {
		this._xLEMacActualLRF = _x_LE_Mac_Ck;
	}

	public Amount<Length> get_yLEMacActualLRF() {
		return _yLEMacActualLRF;
	}

	public void set_yLEMacActualLRF(Amount<Length> _y_LE_Mac_Ck) {
		this._yLEMacActualLRF = _y_LE_Mac_Ck;
	}

	public Amount<Length> get_deltaXWingFus() {
		return _deltaXWingFus;
	}

	public void set_deltaXWingFus(Amount<Length> _deltaXWingFus) {
		this._deltaXWingFus = _deltaXWingFus;
	}	

	public Amount<Length> get_roughness() {
		return _roughness;
	}

	public void set_roughness(Amount<Length> _roughness) {
		this._roughness = _roughness;
	}

	public Double get_xTransitionU() {
		return _xTransitionU;
	}

	public void set_xTransitionU(Double _xTransitionU) {
		this._xTransitionU = _xTransitionU;
	}

	public Double get_xTransitionL() {
		return _xTransitionL;
	}

	public void set_xTransitionL(Double _xTransitionL) {
		this._xTransitionL = _xTransitionL;
	}	

	public Amount<Area> get_surfaceWettedExposed() {
		return _surfaceWettedExposed;
	}

	public void set_surfaceWettedExposed(Amount<Area> _surfaceWettedExposed) {
		this._surfaceWettedExposed = _surfaceWettedExposed;
	}

	public Double get_FF_wing() {
		return _FF_wing;
	}

	public void set_FF_wing(Double _FF_wing) {
		this._FF_wing = _FF_wing;
	}

	public Amount<Angle> get_dihedralMean() {
		return _dihedralMean;
	}

	public void set_dihedralMean(Amount<Angle> _dihedralMean) {
		this._dihedralMean = _dihedralMean;
	}

	public boolean isHasWinglet() {
		return hasWinglet;
	}

	public void setHasWinglet(boolean hasWinglet) {
		this.hasWinglet = hasWinglet;
	}

	public Amount<Length> get_wingletHeight() {
		return _wingletHeight;
	}

	public void set_wingletHeight(Amount<Length> _wingletHeight) {
		this._wingletHeight = _wingletHeight;
	}

	public Double get_taperRatioOpt() {
		return _taperRatioOpt;
	}

	public Double get_thicknessMean() {
		return _thicknessMean;
	}

	public void set_thicknessMean(Double _thicknessMean) {
		this._thicknessMean = _thicknessMean;
	}

	public Double get_tc_root() {
		return _tc_root;
	}

	public void set_tc_root(Double tc_root) {
		this._tc_root = tc_root;
	}

	public Double get_tc_kink() {
		return _tc_kink;
	}

	public void set_tc_kink(Double tc_kink) {
		this._tc_kink = tc_kink;
	}

	public Double get_tc_tip() {
		return _tc_tip;
	}

	public void set_tc_tip(Double tc_tip) {
		this._tc_tip = tc_tip;
	}

	public Amount<Area> get_surfaceWetted() {
		return _surfaceWetted;
	}

	public Amount<Length> get_chordLinPanel() {
		return _chordLinPanel;
	}

	public void set_chordLinPanel(Amount<Length> _chordLinPanel) {
		this._chordLinPanel = _chordLinPanel;
	}

	public Amount<Length> get_semiSpanOuterPanel() {
		return _semiSpanOuterPanel;
	}

	public void set_semiSpanOuterPanel(Amount<Length> _semiSpanOuterPanel) {
		this._semiSpanOuterPanel = _semiSpanOuterPanel;
	}

	public void set_taperRatioInnerPanel(Double _taperRatioInnerPanel) {
		this._taperRatioInnerPanel = _taperRatioInnerPanel;
	}

	public void set_taperRatioOuterPanel(Double _taperRatioOuterPanel) {
		this._taperRatioOuterPanel = _taperRatioOuterPanel;
	}

	public void set_taperRatioCrankedWing(Double _taperRatioCrankedWing) {
		this._taperRatioActual = _taperRatioCrankedWing;
	}

	public void set_taperRatioOpt(Double _taperRatioOpt) {
		this._taperRatioOpt = _taperRatioOpt;
	}

	public Amount<Length> get_xApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	public void set_xApexConstructionAxes(Amount<Length> _xApexConstructionAxes) {
		this._xApexConstructionAxes = _xApexConstructionAxes;
	}

	public Amount<Length> get_yApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	public void set_yApexConstructionAxes(Amount<Length> _yApexConstructionAxes) {
		this._yApexConstructionAxes = _yApexConstructionAxes;
	}

	public Amount<Length> get_zApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	public void set_zApexConstructionAxes(Amount<Length> _zApexConstructionAxes) {
		this._zApexConstructionAxes = _zApexConstructionAxes;
	}

	public Amount<Angle> get_twistKink() {
		return _twistKink;
	}

	public void set_twistKink(Amount<Angle> _twistKink) {
		this._twistKink = _twistKink;
	}

	public Amount<Angle> get_twistTip() {
		return _twistTip;
	}

	public void set_twistTip(Amount<Angle> _twistTip) {
		this._twistTip = _twistTip;
	}

	public Amount<Angle> get_dihedralInnerPanel() {
		return _dihedralInnerPanel;
	}

	public void set_dihedralInnerPanel(Amount<Angle> _dihedralInnerPanel) {
		this._dihedralInnerPanel = _dihedralInnerPanel;
	}

	public Amount<Angle> get_dihedralOuterPanel() {
		return _dihedralOuterPanel;
	}

	public void set_dihedralOuterPanel(Amount<Angle> _dihedralOuterPanel) {
		this._dihedralOuterPanel = _dihedralOuterPanel;
	}

	public Amount<Length> get_xTETip() {
		return _xTETip;
	}

	public void set_xTETip(Amount<Length> _xTETip) {
		this._xTETip = _xTETip;
	}

	public Amount<Length> get_xTEKink() {
		return _xTEKink;
	}

	public void set_xTEKink(Amount<Length> _xTEKink) {
		this._xTEKink = _xTEKink;
	}

	public List<Amount<Length>> get_yStationEqList() {
		return _yStationEq.getAmountList();
	}

	public List<Amount<Length>> get_xLEvsYActualList() {
		return _xLEvsYActual.getAmountList();
	}

	public List<Amount<Length>> get_xTEActualVsYList() {
		return _xTEvsYActual.getAmountList();
	}

	public Double get_k_c_wing() {
		return _k_c_wing;
	}

	public void set_k_c_wing(Double _k_c_wing) {
		this._k_c_wing = _k_c_wing;
	}

	public Amount<Area> get_surfaceExposed() {
		return _surfaceExposed;
	}

	public void set_surfaceExposed(Amount<Area> _surfaceExposed) {
		this._surfaceExposed = _surfaceExposed;
	}

	public Amount<Area> get_semiSurfaceWettedInnerPanel() {
		return _semiSurfaceWettedInnerPanel;
	}

	public void set_semiSurfaceWettedInnerPanel(
			Amount<Area> _semiSurfaceWettedInnerPanel) {
		this._semiSurfaceWettedInnerPanel = _semiSurfaceWettedInnerPanel;
	}

	public Amount<Area> get_semiSurfaceWettedOuterPanel() {
		return _semiSurfaceWettedOuterPanel;
	}

	public void set_semiSurfaceWettedOuterPanel(
			Amount<Area> _semiSurfaceWettedOuterPanel) {
		this._semiSurfaceWettedOuterPanel = _semiSurfaceWettedOuterPanel;
	}

	public Amount<Volume> get_volume() {
		return _volume;
	}

	public void set_volume(Amount<Volume> _volume) {
		this._volume = _volume;
	}

	public Amount<Volume> get_volumeExposed() {
		return _volumeExposed;
	}

	public void set_volumeExposed(Amount<Volume> _volumeExposed) {
		this._volumeExposed = _volumeExposed;
	}

	public Amount<Volume> get_semiVolumeInnerPanel() {
		return _semiVolumeInnerPanel;
	}

	public void set_semiVolumeInnerPanel(Amount<Volume> _semiVolumeInnerPanel) {
		this._semiVolumeInnerPanel = _semiVolumeInnerPanel;
	}

	public Amount<Volume> get_semiVolumeOuterPanel() {
		return _semiVolumeOuterPanel;
	}

	public void set_semiVolumeOuterPanel(Amount<Volume> _semiVolumeOuterPanel) {
		this._semiVolumeOuterPanel = _semiVolumeOuterPanel;
	}

	public Amount<Angle> get_sweepTEEquivalentWing() {
		return _sweepTEEquivalentWing;
	}

	public void set_sweepTEEquivalentWing(Amount<Angle> _sweepTEEquivalentWing) {
		this._sweepTEEquivalentWing = _sweepTEEquivalentWing;
	}

	public Amount<Angle> get_sweepTEInnerPanel() {
		return _sweepTEInnerPanel;
	}

	public void set_sweepTEInnerPanel(Amount<Angle> _sweepTEInnerPanel) {
		this._sweepTEInnerPanel = _sweepTEInnerPanel;
	}

	public Amount<Angle> get_sweepTEOuterPanel() {
		return _sweepTEOuterPanel;
	}

	public void set_sweepTEOuterPanel(Amount<Angle> _sweepTEOuterPanel) {
		this._sweepTEOuterPanel = _sweepTEOuterPanel;
	}


	public Amount<Length> get_meanAerodynamicChord() {
		return _meanAerodynamicChord;
	}

	public void set_meanAerodynamicChord(Amount<Length> _meanAerodynamicChord) {
		this._meanAerodynamicChord = _meanAerodynamicChord;
	}

	public Amount<Length> get_spanStationMeanAerodynamicChord() {
		return _spanStationMeanAerodynamicChord;
	}

	public void set_spanStationMeanAerodynamicChord(
			Amount<Length> _spanStationMeanAerodynamicChord) {
		this._spanStationMeanAerodynamicChord = _spanStationMeanAerodynamicChord;
	}

	public Amount<Length> get_xLEMeanAerodynamicChordInnerPanel() {
		return _xLEMeanAerodynamicChordInnerPanel;
	}

	public void set_xLEMeanAerodynamicChordInnerPanel(
			Amount<Length> _xLEMeanAerodynamicChordInnerPanel) {
		this._xLEMeanAerodynamicChordInnerPanel = _xLEMeanAerodynamicChordInnerPanel;
	}

	public Amount<Length> get_yMeanAerodynamicChordInnerPanel() {
		return _yMeanAerodynamicChordInnerPanel;
	}

	public void set_yMeanAerodynamicChordInnerPanel(
			Amount<Length> _yMeanAerodynamicChordInnerPanel) {
		this._yMeanAerodynamicChordInnerPanel = _yMeanAerodynamicChordInnerPanel;
	}

	public Amount<Length> get_spanStationMeanAerodynamicChordInnerPanel() {
		return _spanStationMeanAerodynamicChordInnerPanel;
	}

	public void set_spanStationMeanAerodynamicChordInnerPanel(
			Amount<Length> _spanStationMeanAerodynamicChordInnerPanel) {
		this._spanStationMeanAerodynamicChordInnerPanel = _spanStationMeanAerodynamicChordInnerPanel;
	}

	public Amount<Length> get_xLEMeanAerodynamicChordOuterPanel() {
		return _xLEMeanAerodynamicChordOuterPanel;
	}

	public void set_xLEMeanAerodynamicChordOuterPanel(
			Amount<Length> _xLEMeanAerodynamicChordOuterPanel) {
		this._xLEMeanAerodynamicChordOuterPanel = _xLEMeanAerodynamicChordOuterPanel;
	}

	public Amount<Length> get_yMeanAerodynamicChordOuterPanel() {
		return _yMeanAerodynamicChordOuterPanel;
	}

	public void set_yMeanAerodynamicChordOuterPanel(
			Amount<Length> _yMeanAerodynamicChordOuterPanel) {
		this._yMeanAerodynamicChordOuterPanel = _yMeanAerodynamicChordOuterPanel;
	}

	public Amount<Length> get_spanStationMeanAerodynamicChordOuterPanel() {
		return _spanStationMeanAerodynamicChordOuterPanel;
	}

	public void set_spanStationMeanAerodynamicChordOuterPanel(
			Amount<Length> _spanStationMeanAerodynamicChordOuterPanel) {
		this._spanStationMeanAerodynamicChordOuterPanel = _spanStationMeanAerodynamicChordOuterPanel;
	}


	public Amount<Length> get_aerodynamicCenterX() {
		return _aerodynamicCenterX;
	}

	public void set_aerodynamicCenterX(Amount<Length> _aerodynamicCenterX) {
		this._aerodynamicCenterX = _aerodynamicCenterX;
	}

	public Amount<Length> get_aerodynamicCenterY() {
		return _aerodynamicCenterY;
	}

	public void set_aerodynamicCenterY(Amount<Length> _aerodynamicCenterY) {
		this._aerodynamicCenterY = _aerodynamicCenterY;
	}

	public Amount<Length> get_aerodynamicCenterZ() {
		return _aerodynamicCenterZ;
	}

	public void set_aerodynamicCenterZ(Amount<Length> _aerodynamicCenterZ) {
		this._aerodynamicCenterZ = _aerodynamicCenterZ;
	}


	public Amount<Length> get_aerodynamicCenterXInnerPanel() {
		return _aerodynamicCenterXInnerPanel;
	}

	public void set_aerodynamicCenterXInnerPanel(
			Amount<Length> _aerodynamicCenterXInnerPanel) {
		this._aerodynamicCenterXInnerPanel = _aerodynamicCenterXInnerPanel;
	}

	public Amount<Length> get_aerodynamicCenterYInnerPanel() {
		return _aerodynamicCenterYInnerPanel;
	}

	public void set_aerodynamicCenterYInnerPanel(
			Amount<Length> _aerodynamicCenterYInnerPanel) {
		this._aerodynamicCenterYInnerPanel = _aerodynamicCenterYInnerPanel;
	}

	public Amount<Length> get_aerodynamicCenterZInnerPanel() {
		return _aerodynamicCenterZInnerPanel;
	}

	public void set_aerodynamicCenterZInnerPanel(
			Amount<Length> _aerodynamicCenterZInnerPanel) {
		this._aerodynamicCenterZInnerPanel = _aerodynamicCenterZInnerPanel;
	}


	public Amount<Length> get_aerodynamicCenterXOuterPanel() {
		return _aerodynamicCenterXOuterPanel;
	}

	public void set_aerodynamicCenterXOuterPanel(
			Amount<Length> _aerodynamicCenterXOuterPanel) {
		this._aerodynamicCenterXOuterPanel = _aerodynamicCenterXOuterPanel;
	}

	public Amount<Length> get_aerodynamicCenterYOuterPanel() {
		return _aerodynamicCenterYOuterPanel;
	}

	public void set_aerodynamicCenterYOuterPanel(
			Amount<Length> _aerodynamicCenterYOuterPanel) {
		this._aerodynamicCenterYOuterPanel = _aerodynamicCenterYOuterPanel;
	}

	public Amount<Length> get_aerodynamicCenterZOuterPanel() {
		return _aerodynamicCenterZOuterPanel;
	}

	public void set_aerodynamicCenterZOuterPanel(
			Amount<Length> _aerodynamicCenterZOuterPanel) {
		this._aerodynamicCenterZOuterPanel = _aerodynamicCenterZOuterPanel;
	}

	public int getIDX_SECTION_ROOT() {
		return IDX_SECTION_ROOT;
	}

	public int getIDX_SECTION_KINK() {
		return IDX_SECTION_KINK;
	}

	public int getIDX_SECTION_TIP() {
		return IDX_SECTION_TIP;
	}

	public int getNUM_SECTIONS() {
		return NUM_SECTIONS;
	}

	public void set_taperRatioEquivalent(Double _taperRatioEquivalentWing) {
		this._taperRatioEquivalent = _taperRatioEquivalentWing;
	}

	public void set_surfaceWetted(Amount<Area> _surfaceWetted) {
		this._surfaceWetted = _surfaceWetted;
	}


	public ComponentEnum get_type() {
		return _type;
	}


	public void set_type(ComponentEnum _type) {
		this._type = _type;
	}


	public Object get_formFactor() {
		return _formFactor;
	}


	public Amount<Length> get_AC_CGdistance() {
		return _AC_CGdistance;
	}


	public Double get_volumetricRatio() {
		return _volumetricRatio;
	}


	public Amount<Mass> get_mass() {
		return _mass;
	}


	public void set_mass(Amount<Mass> _mass) {
		this._mass = _mass;
	}


	public Amount<Length> get_ACw_ACdistance() {
		return _ACw_ACdistance;
	}

	@Override
	public Map<AnalysisTypeEnum, List<MethodEnum>> get_methodsMap() {
		return _methodsMap;
	}


	public Map<MethodEnum, Amount<Mass>> get_massMap() {
		return _massMap;
	}


	public Double[] get_percentDifference() {
		return _percentDifference;
	}


	public Double get_positionRelativeToAttachment() {
		return _positionRelativeToAttachment;
	}


	public Double get_taperRatioEquivalent() {
		return _taperRatioEquivalent;
	}


	public Amount<Angle> get_sweepQuarterChordEq() {
		return _sweepQuarterChordEq;
	}


	public Amount<Angle> get_sweepHalfChordEq() {
		return _sweepHalfChordEq;
	}


	public Double getTc_root() {
		return _tc_root;
	}


	public Double getTc_kink() {
		return _tc_kink;
	}


	public Double getTc_tip() {
		return _tc_tip;
	}


	public Amount<Angle> get_sweepLEEquivalent() {
		return _sweepLEEquivalent;
	}


	public Amount<Angle> get_sweepStructuralAxis() {
		return _sweepStructuralAxis;
	}


	public void set_positionRelativeToAttachment(Double positionRelativeToFus) {
		this._positionRelativeToAttachment = positionRelativeToFus;
	}


	public Amount<Area> get_surfaceCS() {
		return _surfaceCS;
	}


	public void set_surfaceCS(Amount<Area> _surfaceCS) {
		this._surfaceCS = _surfaceCS;
	}

	@Override
	public Amount<Length> get_X0() { return _X0; }

	@Override
	public void set_X0(Amount<Length> x) { _X0 = x; };


	@Override
	public Amount<Length> get_Y0() { return _Y0; }

	@Override
	public void set_Y0(Amount<Length> y) { _Y0 = y; };

	@Override
	public Amount<Length> get_Z0() { return _Z0; }

	@Override
	public void set_Z0(Amount<Length> z) { _Z0 = z; }


	public void set_sweepQuarterChordEq(Amount<Angle> _sweepQuarterChordEq) {
		this._sweepQuarterChordEq = _sweepQuarterChordEq;
	}


	public Amount<Mass> get_massReference() {
		return _massReference;
	}


	public void set_massReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}


	public boolean is_variableIncidence() {
		return _variableIncidence;
	}


	public void set_variableIncidence(boolean _variableIncidence) {
		this._variableIncidence = _variableIncidence;
	}


	public Double get_massCorrectionFactor() {
		return _massCorrectionFactor;
	}


	public void set_massCorrectionFactor(Double _massCorrectionFactor) {
		this._massCorrectionFactor = _massCorrectionFactor;
	}


	public Double get_compositeCorretionFactor() {
		return _compositeCorretionFactor;
	}


	public void set_compositeCorretionFactor(Double _compositeCorretionFactor) {
		this._compositeCorretionFactor = _compositeCorretionFactor;
	}


	public Amount<Mass> get_massEstimated() {
		return _massEstimated;
	}


	public Amount<Length> get_xCG() {
		return _xCG;
	}


	public void set_xCG(Amount<Length> _xCG) {
		this._xCG = _xCG;
	}


	public Amount<Length> get_yCG() {
		return _yCG;
	}


	public void set_yCG(Amount<Length> _yCG) {
		this._yCG = _yCG;
	}


	public Amount<Length> get_zCG() {
		return _zCG;
	}


	public void set_zCG(Amount<Length> _zCG) {
		this._zCG = _zCG;
	}


	public Amount<Length> get_xCGEstimated() {
		return _xCGEstimated;
	}


	public void set_xCGEstimated(Amount<Length> _xCGEstimated) {
		this._xCGEstimated = _xCGEstimated;
	}


	public Map<MethodEnum, Amount<Length>> get_xCGMap() {
		return _xCGMap;
	}


	public void set_xCGMap(Map<MethodEnum, Amount<Length>> _xCGMap) {
		this._xCGMap = _xCGMap;
	}


	public Double[] get_percentDifferenceXCG() {
		return _percentDifferenceXCG;
	}


	public Map<MethodEnum, Amount<Length>> get_yCGMap() {
		return _yCGMap;
	}


	public void set_yCGMap(Map<MethodEnum, Amount<Length>> _yCGMap) {
		this._yCGMap = _yCGMap;
	}


	public Amount<Length> get_yCGReference() {
		return _yCGReference;
	}


	public void set_yCGReference(Amount<Length> _yCGReference) {
		this._yCGReference = _yCGReference;
	}


	public Double[] get_percentDifferenceYCG() {
		return _percentDifferenceYCG;
	}


	public Amount<Length> get_yCGEstimated() {
		return _yCGEstimated;
	}


	public CenterOfGravity get_cg() {
		return _cg;
	}


	public void set_cg(CenterOfGravity _cg) {
		this._cg = _cg;
	}


	public Amount<Length> get_xLEMacActualBRF() {
		return _xLEMacActualBRF;
	}


	public Amount<Length> get_yLEMacActualBRF() {
		return _yLEMacActualBRF;
	}

	public MyAirfoil get_theCurrentAirfoil() {
		return _theCurrentAirfoil;
	}


	public void set_theCurrentAirfoil(MyAirfoil _theAirfoil) {
		this._theCurrentAirfoil = _theAirfoil;
	}


	public List<MyAirfoil> get_theAirfoilsList() {
		return _theAirfoilsList;
	}


	public void set_theAirfoilsList(List<MyAirfoil> _theAirfoilsList) {
		this._theAirfoilsList = _theAirfoilsList;
	}


	public Integer get_numberOfAirfoils() {
		return _numberOfAirfoils;
	}


	public void set_numberOfAirfoils(Integer _numberOfAirfoils) {
		_numberOfAirfoils = _numberOfAirfoils;
	}


	public Amount<Angle> get_twistRootRelativeToXbody() {
		return _twistRootRelativeToXbody;
	}


	public void set_twistRootRelativeToXbody(Amount<Angle> _twistRootRelativeToXbody) {
		this._twistRootRelativeToXbody = _twistRootRelativeToXbody;
	}


	public Amount<Angle> get_iw() {
		return _iw;
	}


	public void set_iw(Amount<Angle> _iw) {
		this._iw = _iw;
	}

	public MyArray get_clBasic_y() {
		return _clBasic_y;
	}


	public MyArray get_ellChordVsY() {
		return _ellChordVsY;
	}

	public void initializeAerodynamics(OperatingConditions conditions, Aircraft aircraft) {
		aerodynamics = new LSAerodynamicsManager(conditions, this, aircraft);
	}

	public LSAerodynamicsManager getAerodynamics() {
		return aerodynamics;
	}


	public LSGeometryManager getGeometry() {
		return geometry;
	}


	public MyArray get_alpha0VsY() {
		return _alpha0VsY;
	}


	public Amount<Length> get_semispan() {
		return _semispan;
	}


	public void set_semispan(Amount<Length> _semispan) {
		this._semispan = _semispan;
	}

	public Amount<Length> get_meanGeometricChord() {
		return _meanGeometricChord;
	}


	public MyArray get_clMaxVsY() {
		return _clMaxVsY;
	}

	public static String getId() {
		return null;
	}

	public String getObjectId() {
		return id;
	}

	public MyArray get_dihedral() {
		return _dihedral;
	}

	public void set_dihedral(MyArray _dihedral) {
		this._dihedral = _dihedral;
	}

	public MyArray get_maxThicknessVsY() {
		return _maxThicknessVsY;
	}

	public void set_maxThicknessVsY(MyArray _maxThicknessVsY) {
		this._maxThicknessVsY = _maxThicknessVsY;
	}

	public Double get_thicknessMax() {
		return _thicknessMax;
	}

	public MyArray get_twistVsY() {
		return _twistVsY;
	}

	public void set_twistVsY(MyArray _twistVsY) {
		this._twistVsY = _twistVsY;
	}

	public MyArray get_etaAirfoil() {
		return _etaAirfoil;
	}

	public Double get_maxThicknessMean() {
		return _maxThicknessMean;
	}

	public MyArray get_yStationEq() {
		return _yStationEq;
	}

	public MyArray get_yStationActual() {
		return _yStationActual;
	}

	public MyArray get_xLEvsYActual() {
		return _xLEvsYActual;
	}

	public MyArray get_xTEvsYActual() {
		return _xTEvsYActual;
	}

	public List<MyAirfoil> get_theAirfoilsListExposed() {
		return _theAirfoilsListExposed;
	}

	public void set_theAirfoilsListExposed(List<MyAirfoil> _theAirfoilsListExposed) {
		this._theAirfoilsListExposed = _theAirfoilsListExposed;
	}

	public MyArray get_chordsVsYActual() {
		return _chordsVsYActual;
	}

	public MyArray get_yStationsAirfoil() {
		return _yStationsAirfoil;
	}

	public MyArray get_etaAirfoilExposed() {
		return _etaAirfoilExposed;
	}

	public void set_etaAirfoilExposed(MyArray _etaAirfoilExposed) {
		this._etaAirfoilExposed = _etaAirfoilExposed;
	}

	public MyArray get_twistVsYExposed() {
		return _twistVsYExposed;
	}

	public void set_twistVsYExposed(MyArray _twistVsYExposed) {
		this._twistVsYExposed = _twistVsYExposed;
	}

	public MyArray get_chordVsYAirfoilsExposed() {
		return _chordVsYAirfoilsExposed;
	}

	public void set_chordVsYAirfoilsExposed(MyArray _chordVsYAirfoilsExposed) {
		this._chordVsYAirfoilsExposed = _chordVsYAirfoilsExposed;
	}


}
