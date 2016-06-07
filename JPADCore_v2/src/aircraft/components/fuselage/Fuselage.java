package aircraft.components.fuselage;

import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.tan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//import static org.eclipse.uomo.units.SI.*;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.solvers.AllowedSolution;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import aircraft.OperatingConditions;
import aircraft.calculators.ACPerformanceManager;
import aircraft.componentmodel.AeroComponent;
import aircraft.components.Aircraft;
import aircraft.components.LandingGear;
import aircraft.components.fuselage.creator.FuselageCreator;
import aircraft.components.liftingSurface.creator.SpoilerCreator;
import aircraft.components.nacelles.Nacelle;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.WindshieldType;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

public class Fuselage extends AeroComponent implements IFuselage {

	AerodynamicDatabaseReader _aerodynamicDatabaseReader;
	
	private String _name, _description;
	private Amount<Length> _X0, _Y0, _Z0;
	private ComponentEnum _type = ComponentEnum.FUSELAGE; 

	// Note: construction axes, 
	// X from FUSELAGE nose to tail,
	// Y from left wing to right wing,
	// Z from pilots feet to head 

	// view from left wing to right wing
	private List<Double> _outlineXZUpperCurveX = new ArrayList<Double>();
	private List<Double> _outlineXZUpperCurveZ = new ArrayList<Double>();

	// view from left wing to right wing
	private List<Double> _outlineXZLowerCurveX = new ArrayList<Double>();
	private List<Double> _outlineXZLowerCurveZ = new ArrayList<Double>();

	//	// Mesh XZ Upper Curve
	//	private List<Double> _meshXZCurveX = new ArrayList<Double>();
	//	private List<Double> _meshXZUpperCurveZ = new ArrayList<Double>();
	//	private List<Double> _meshXZLowerCurveZ = new ArrayList<Double>();

	// view from left wing to right wing
	private List<Double> _outlineXZCamberLineX = new ArrayList<Double>();
	private List<Double> _outlineXZCamberLineZ = new ArrayList<Double>();

	// view from top, right part of body
	private List<Double> _outlineXYSideRCurveX = new ArrayList<Double>();
	private List<Double> _outlineXYSideRCurveY = new ArrayList<Double>();
	private List<Double> _outlineXYSideRCurveZ = new ArrayList<Double>();
	// view from top, left part of body
	private List<Double> _outlineXYSideLCurveX = new ArrayList<Double>();
	private List<Double> _outlineXYSideLCurveY = new ArrayList<Double>();
	private List<Double> _outlineXYSideLCurveZ = new ArrayList<Double>();

	// view section Upper curve (fuselage front view, looking from -X towards +X)
	private List<Double> _sectionUpperCurveY = new ArrayList<Double>();
	private List<Double> _sectionUpperCurveZ = new ArrayList<Double>();

	// view section Lower curve (fuselage front view, looking from -X towards +X)
	private List<Double> _sectionLowerCurveY = new ArrayList<Double>();
	private List<Double> _sectionLowerCurveZ = new ArrayList<Double>();


	// EXPERIMENTAL
	//	// view section Upper curve (fuselage front view, looking from -X towards +X)
	//	public List<Double> _sectionUpperCurveY1 = new ArrayList<Double>();
	//	public List<Double> _sectionUpperCurveZ1 = new ArrayList<Double>();
	//
	//	// view section Lower curve (fuselage front view, looking from -X towards +X)
	//	public List<Double> _sectionLowerCurveY1 = new ArrayList<Double>();
	//	public List<Double> _sectionLowerCurveZ1 = new ArrayList<Double>();

	List<FuselageCurvesSection> _sectionsYZ = new ArrayList<FuselageCurvesSection>();
	public final int IDX_SECTION_YZ_NOSE_TIP   = 0;
	public final int IDX_SECTION_YZ_NOSE_CAP   = 1;
	public final int IDX_SECTION_YZ_MID_NOSE   = 2;
	public final int IDX_SECTION_YZ_CYLINDER_1 = 3;
	public final int IDX_SECTION_YZ_CYLINDER_2 = 4;
	public final int IDX_SECTION_YZ_MID_TAIL   = 5;
	public final int IDX_SECTION_YZ_TAIL_CAP   = 6;
	public final int IDX_SECTION_YZ_TAIL_TIP   = 7;
	public final int NUM_SECTIONS_YZ           = 8;
	// X-coordinates (m) of each YZ section
	List<Amount<Length> > _sectionsYZStations = new ArrayList<Amount<Length>>();

	List<List<Double>> _sectionUpperCurvesY = new ArrayList<List<Double>>();
	List<List<Double>> _sectionUpperCurvesZ = new ArrayList<List<Double>>();
	List<List<Double>> _sectionLowerCurvesY = new ArrayList<List<Double>>();
	List<List<Double>> _sectionLowerCurvesZ = new ArrayList<List<Double>>();

	//-----------------------------------------------------------------------
	// DESIGN PARAMETERS
	//-----------------------------------------------------------------------

	// Fuselage overall length
	//private Double _len_F, _len_F_MIN, _len_F_MAX;
	private Amount<Length> _len_F, _len_F_MIN,_len_F_MAX;

	// Fuselage nose length
	//private Double _len_N, _len_N_MIN, _len_N_MAX;
	private Amount<Length> _len_N, _len_N_MIN, _len_N_MAX;

	//private Double _len_C, _len_C_MIN, _len_C_MAX;
	private Amount<Length> _len_C, _len_C_MIN, _len_C_MAX;

	//private Double _len_T, _len_T_MIN, _len_T_MAX;
	private Amount<Length> _len_T, _len_T_MIN, _len_T_MAX;

	//private Double _len_N1, _len_N2, _len_N3;
	private Amount<Length> _len_N1, _len_N2, _len_N3;
	//private Double _diam_C,_diam_C_MIN , _diam_C_MAX ,_width_C;
	private Amount<Length> _sectionCylinderHeight,_diam_C_MIN , _diam_C_MAX;	

	// GM = Geometric Mean, RMS = Root Mean Square, AM = Arithmetic Mean
	private Amount<Length> _equivalentDiameterCylinderGM,
	_equivalentDiameterGM,
	_equivalentDiameterCylinderAM,
	_equivalentDiameterAM,
	_equivalentDiameterCylinderRMS,
	_equivalentDiameterRMS;

	//cylindrical section base area
	private Amount<Area> _area_C;
	private Amount<Area> _windshieldArea;
	private String _windshieldType; // Possible types (Roskam part VI, page 134): Flat,protruding; Flat,flush; Single,round; Single,sharp; Double 

	//Wetted area estimate
	private Amount<Area> _sWetNose;
	private Amount<Area> _sWetTail;
	private Amount<Area> _sWetC;
	private Amount<Area> _sFront; // CANNOT FIND FUSELAGE HEIGHT in MyAeroFuselage!!
	private Amount<Area> _sWet; 

	// Distance of fuselage lowest part from ground
	private Amount<Length> _heightFromGround;

	//private Double _phi_1, _phi_2, _phi_3;
	private Amount<Angle> _phi_1, _phi_2, _phi_3;

	private Amount<Angle> _phi_N, _phi_T;
	private Amount<Length> _height_N,_height_N_MIN,_height_N_MAX, _height_T,_height_T_MIN,_height_T_MAX;

	private Amount<Angle> _upsweepAngle, _windshieldAngle;
	
	private Amount<Length> _roughness;

	// Non-dimensional parameters
	private Double _lambda_F,_lambda_F_MIN,_lambda_F_MAX;
	private Double _lambda_N,_lambda_N_MIN,_lambda_N_MAX; 
	private Double _lambda_C,_lambda_C_MIN,_lambda_C_MAX;
	private Double _lambda_T,_lambda_T_MIN,_lambda_T_MAX;
	private Double _lenRatio_NF,_lenRatio_NF_MIN,_lenRatio_NF_MAX;
	private Double _lenRatio_CF,_lenRatio_CF_MIN,_lenRatio_CF_MAX;
	private Double _lenRatio_TF,_lenRatio_TF_MIN,_lenRatio_TF_MAX;
	private Double _lenRatio, _formFactor;

	// Fuselage section parameters

	// Width and height
	private Amount<Length>  
	_sectionCylinderWidth, _sectionWidth_MIN, _sectionWidth_MAX,
	_windshieldHeight, _windshieldWidth;

	private Amount<Length>  
	_dxNoseCap, _dxNoseCap_MIN, _dxNoseCap_MAX,
	_dxTailCap, _dxTailCap_MIN, _dxTailCap_MAX;

	// Non dimensional section parameters

	// how lower part is different from half diameter 
	private Double 
	_sectionCylinderLowerToTotalHeightRatio,
	_sectionLowerToTotalHeightRatio_MIN, _sectionLowerToTotalHeightRatio_MAX,
	_sectionNoseMidLowerToTotalHeightRatio,
	_sectionNoseMidToTotalHeightRatio_MIN, _sectionNoseMidToTotalHeightRatio_MAX,
	_sectionTailMidLowerToTotalHeightRatio,
	_sectionTailMidToTotalHeightRatio_MIN, _sectionTailMidToTotalHeightRatio_MAX;


	// shape index, 1 --> close to a rectangle; 0 --> close to a circle
	private Double 
	_sectionCylinderRhoUpper, _sectionRhoUpper_MIN, _sectionRhoUpper_MAX, 
	_sectionCylinderRhoLower, _sectionRhoLower_MIN, _sectionRhoLower_MAX,
	_sectionMidNoseRhoUpper,_sectionMidNoseRhoUpper_MIN,_sectionMidNoseRhoUpper_MAX,
	_sectionMidNoseRhoLower, _sectionMidNoseRhoLower_MIN,_sectionMidNoseRhoLower_MAX,
	_sectionMidTailRhoUpper,_sectionMidTailRhoUpper_MIN,_sectionMidTailRhoUpper_MAX,
	_sectionMidTailRhoLower, _sectionMidTailRhoLower_MIN,_sectionMidTailRhoLower_MAX;

	// meshing stuff
	private int _np_N = 10, _np_C = 4, _np_T = 10, _np_SecUp = 10, _np_SecLow = 10;
	private double _deltaXNose, _deltaXCylinder, _deltaXTail;
	protected Object mouseClicked;

	private FuselageAdjustCriteria _adjustCriterion = FuselageAdjustCriteria.NONE;

	private OperatingConditions _theOperatingConditions;
	private Aircraft _theAircraft;
	private Amount<Mass> _mass, _massEstimated, _massReference;
	private Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	private Map <MethodEnum, Amount<Length>> _xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	private Map <MethodEnum, Amount<Length>> _yCGMap = new TreeMap<MethodEnum, Amount<Length>>();

	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
	private List<MethodEnum> _methodsList = new ArrayList<MethodEnum>();
	private Double[] _percentDifference;

	private double _dxNoseCapPercent;
	private double _dxTailCapPercent;

	private Boolean _pressurized;
	private Double _massCorrectionFactor = 1.;
	private int _deckNumber = 1;

	private Amount<Length> _xCG, _yCG, _xCGReference, 
	_xCGEstimated, _yCGReference, _yCGEstimated,
	_zCG, _zCGEstimated;
	private Double[] _percentDifferenceXCG;

	private CenterOfGravity _cg;
	private FuselageAerodynamicsManager aerodynamics;
	private String databaseFolderPath;
	private String databaseFileName;

	//=========================================================================================================================================
	// BEGIN OF THE CONSTRUCTOR USING BUILDER PATTERN
	//=========================================================================================================================================
	// the creator object
	private String _id = null;
	private Amount<Length> _xApexConstructionAxes = null; 
	private Amount<Length> _yApexConstructionAxes = null; 
	private Amount<Length> _zApexConstructionAxes = null;

	private FuselageCreator _fuselageCreator;
	
	//**********************************************************************
	// Builder pattern via a nested public static class
	public static class FuselageBuilder {
		
		private String __id = null;
		private ComponentEnum __type;
		private Amount<Length> __xApexConstructionAxes = null; 
		private Amount<Length> __yApexConstructionAxes = null; 
		private Amount<Length> __zApexConstructionAxes = null;
		private FuselageCreator __fuselageCreator;
		
		public FuselageBuilder(String id, ComponentEnum type) {
			// required parameter
			this.__id = id;
			this.__type = type;

			// optional parameters ...

		}

		public FuselageBuilder fuselageCreator(FuselageCreator fuselage) {
			this.__fuselageCreator = fuselage;
			return this;
		}
		
		public Fuselage build() {
			return new Fuselage(this);
		}

	}
	
	//**********************************************************************
	
	private Fuselage(FuselageBuilder builder) {
		super(builder.__id, builder.__type);
		this.setId(builder.__id); 
		this._type = builder.__type;
		this._xApexConstructionAxes = builder.__xApexConstructionAxes; 
		this._yApexConstructionAxes = builder.__yApexConstructionAxes; 
		this._zApexConstructionAxes = builder.__zApexConstructionAxes;
		this._fuselageCreator = builder.__fuselageCreator;
	}
	
	@Override
	public int getDeckNumber() {
		return _fuselageCreator.getDeckNumber();
	}

	@Override
	public Amount<Length> getLength() {
		return _fuselageCreator.getLength();
	}

	@Override
	public Amount<Mass> getReferenceMass() {
		return _fuselageCreator.getMassReference();
	}

	@Override
	public Amount<Length> getRoughness() {
		return _fuselageCreator.getRoughness();
	}

	@Override
	public Double getNoseLengthRatio() {
		return _fuselageCreator.getLenRatioNF();
	}

	@Override
	public Double getFinesseRatio() {
		return _fuselageCreator.getLambdaN();
	}

	@Override
	public Amount<Length> getNoseTipHeightOffset() {
		return _fuselageCreator.getHeightN();
	}

	@Override
	public Double getNoseDxCapPercent() {
		return _fuselageCreator.getDxNoseCapPercent();
	}

	@Override
	public WindshieldType getWindshieldType() {
		return _fuselageCreator.getWindshieldType();
	}

	@Override
	public Amount<Length> getWindshieldWidht() {
		return _fuselageCreator.getWindshieldWidth();
	}

	@Override
	public Amount<Length> getWindshieldHeight() {
		return _fuselageCreator.getWindshieldHeight();
	}

	@Override
	public Double getNoseMidSectionLowerToTotalHeightRatio() {
		return _fuselageCreator.getSectionNoseMidLowerToTotalHeightRatio();
	}

	@Override
	public Double getNoseMidSectionRhoUpper() {
		return _fuselageCreator.getSectionMidNoseRhoUpper();
	}

	@Override
	public Double getNoseMidSectionRhoLower() {
		return _fuselageCreator.getSectionMidNoseRhoLower();
	}

	@Override
	public Double getCylindricalLengthRatio() {
		return _fuselageCreator.getLenRatioCF();
	}

	@Override
	public Amount<Length> getSectionWidht() {
		return _fuselageCreator.getSectionCylinderWidth();
	}

	@Override
	public Amount<Length> getSectionHeight() {
		return _fuselageCreator.getSectionCylinderHeight();
	}

	@Override
	public Amount<Length> getHeightFromGround() {
		return _fuselageCreator.getHeightFromGround();
	}

	@Override
	public Double getSectionLowerToTotalHeightRatio() {
		return _fuselageCreator.getSectionCylinderLowerToTotalHeightRatio();
	}

	@Override
	public Double getSectionRhoUpper() {
		return _fuselageCreator.getSectionCylinderRhoUpper();
	}

	@Override
	public Double getSectionRhoLower() {
		return _fuselageCreator.getSectionCylinderRhoLower();
	}

	@Override
	public Amount<Length> getTailTipHeightOffset() {
		return _fuselageCreator.getHeightT();
	}

	@Override
	public Double getTailDxCapPercent() {
		return _fuselageCreator.getDxTailCapPercent();
	}

	@Override
	public Double getTailMidSectionLowerToTotalHeightRatio() {
		return _fuselageCreator.getSectionTailMidLowerToTotalHeightRatio();
	}

	@Override
	public Double getTailMidSectionRhoUpper() {
		return _fuselageCreator.getSectionMidTailRhoUpper();
	}

	@Override
	public Double getTailMidSectionRhoLower() {
		return _fuselageCreator.getSectionMidTailRhoLower();
	}

	@Override
	public List<SpoilerCreator> getSpoilers() {
		return _fuselageCreator.getSpoilers();
	}

	@Override
	public void calculateGeometry(
			int np_N, int np_C, int np_T, // no. points @ Nose/Cabin/Tail
			int np_SecUp, int np_SecLow   // no. points @ Upper/Lower section
			) {
		_fuselageCreator.calculateGeometry(np_N, np_C, np_T, np_SecUp, np_SecLow);
	}

	@Override
	public Amount<Area> getSurfaceWetted(Boolean recalculate) {
		return _fuselageCreator.getSurfaceWetted(recalculate);
	}

	// TODO : ADD OTHER GETTERS VIA _fuselageCreator
	
	@Override
	public FuselageCreator getFuselageCreator() {
		return _fuselageCreator;
	}
	
	//=========================================================================================================================================
	// 	END CONSTRUCTOR VIA BUILDER PATTERN
	//=========================================================================================================================================

	// Construct isolated Fuselage
	public Fuselage(String name, 
			String description, 
			double x, double y, double z) {

		super(name, description, x, y, z, ComponentEnum.FUSELAGE);

		_name = name;
		_description = description;
		_X0 = Amount.valueOf(x, SI.METER);
		_Y0 = Amount.valueOf(y, SI.METER);
		_Z0 = Amount.valueOf(z, SI.METER);

		_cg = new CenterOfGravity(_X0, _Y0, _Z0);

		initializeDefaultVariables();
		calculateGeometry();
		checkGeometry();

	} // end-of-constructor
	
	/**
	 * Overload of the previous builder that recognize the aircraft name
	 * 
	 *@author Vittorio Trifari
	 */
	public Fuselage(AircraftEnum aircraftName,
			String name, 
			String description, 
			double x, double y, double z) {

		super(name, description, x, y, z, ComponentEnum.FUSELAGE);

		_name = name;
		_description = description;
		_X0 = Amount.valueOf(x, SI.METER);
		_Y0 = Amount.valueOf(y, SI.METER);
		_Z0 = Amount.valueOf(z, SI.METER);

		_cg = new CenterOfGravity(_X0, _Y0, _Z0);

		initializeDefaultVariables(aircraftName);
		calculateGeometry();
		checkGeometry();

	} // end-of-constructor

	// My own copy constructor
	// TODO: check use in MyFuselagePanel
	public Fuselage(Fuselage aFuselage) {
		this(
				aFuselage.get_name(),
				aFuselage.get_description(),
				aFuselage.get_X0().doubleValue(SI.METER), 
				aFuselage.get_Y0().doubleValue(SI.METER), 
				aFuselage.get_Z0().doubleValue(SI.METER)
				);

		_deckNumber = aFuselage.get_deckNumber();
		_len_F =  aFuselage.get_len_F();
		_lenRatio_NF = aFuselage.get_lenRatio_NF();
		_lenRatio_CF = aFuselage.get_lenRatio_CF();

		_sectionCylinderWidth = aFuselage.get_sectionCylinderWidth();
		_sectionCylinderHeight = aFuselage.get_sectionCylinderHeight();

		// Nose fineness ratio, _len_N/_diam_N
		_lambda_N = aFuselage.get_lambda_N(); 

		// Height from ground of lowest part of fuselage
		_heightFromGround = aFuselage.get_heightFromGround();

		// Fuselage Roughness
		_roughness = aFuselage.get_roughness();

		// positive if nose tip higher than cylindrical part ref. line (in XZ plane)
		_height_N = aFuselage.get_height_N();
		_height_T = aFuselage.get_height_T();

		_massReference = aFuselage.get_massReference();
		_pressurized = aFuselage.is_pressurized();

		// Section parameters
		_dxNoseCapPercent = 
				aFuselage.get_dxNoseCap().doubleValue(SI.METER)
				/ aFuselage.get_len_N().doubleValue(SI.METER);
		_dxTailCapPercent = 
				aFuselage.get_dxTailCap().doubleValue(SI.METER)
				/ aFuselage.get_len_T().doubleValue(SI.METER);

		_windshieldType = aFuselage.get_windshieldType();
		_windshieldHeight = aFuselage.get_windshieldHeight();
		_windshieldWidth = aFuselage.get_windshieldWidth();

		_sectionCylinderLowerToTotalHeightRatio = aFuselage.get_sectionCylinderLowerToTotalHeightRatio();
		_sectionCylinderRhoUpper = aFuselage.get_sectionCylinderRhoUpper();
		_sectionCylinderRhoLower = aFuselage.get_sectionCylinderRhoLower();

		_sectionNoseMidLowerToTotalHeightRatio      = _sectionCylinderLowerToTotalHeightRatio.doubleValue();
		_sectionTailMidLowerToTotalHeightRatio      = _sectionCylinderLowerToTotalHeightRatio.doubleValue();
		//++++++++++++++++++++++++++++++++++++
		_sectionNoseMidToTotalHeightRatio_MIN       = _sectionLowerToTotalHeightRatio_MIN;
		_sectionNoseMidToTotalHeightRatio_MAX       = _sectionLowerToTotalHeightRatio_MAX;
		_sectionTailMidToTotalHeightRatio_MIN       = _sectionLowerToTotalHeightRatio_MIN;
		_sectionTailMidToTotalHeightRatio_MAX       = _sectionLowerToTotalHeightRatio_MAX;

		//+++++++++++++++++++++++++++++++++++++
		_sectionMidNoseRhoUpper      = _sectionCylinderRhoUpper.doubleValue();
		_sectionMidNoseRhoUpper_MIN  = _sectionRhoUpper_MIN;
		_sectionMidNoseRhoUpper_MAX  = _sectionRhoUpper_MAX;
		_sectionMidTailRhoUpper      = _sectionCylinderRhoUpper.doubleValue();
		_sectionMidTailRhoUpper_MIN  = _sectionRhoUpper_MIN;
		_sectionMidTailRhoUpper_MAX  = _sectionRhoUpper_MAX;

		//+++++++++++++++++++++++++++++++++++++
		_sectionMidNoseRhoLower      = _sectionCylinderRhoLower.doubleValue();
		_sectionMidNoseRhoLower_MIN  = _sectionRhoLower_MIN;
		_sectionMidNoseRhoLower_MAX  = _sectionRhoLower_MAX;
		_sectionMidTailRhoLower      = _sectionCylinderRhoLower.doubleValue();
		_sectionMidTailRhoLower_MIN  = _sectionRhoLower_MIN;
		_sectionMidTailRhoLower_MAX  = _sectionRhoLower_MAX;

		calculateGeometry();
		checkGeometry();

	}

	// see also deepCopy function from here
	// 1) http://howtodoinjava.com/2012/11/22/how-to-do-deep-cloning-using-in-memory-serialization-in-java/
	// 2) http://alvinalexander.com/java/java-deep-clone-example-source-code

	// Avoid clone at all costs and go for your own copy solution
	// http://stackoverflow.com/questions/2427883/clone-vs-copy-constructor-which-is-recommended-in-java
	//	/** Construct fuselage using data from isolated fuselage and from other components */
	//	public MyAeroFuselage(MyAeroFuselage fuselage, 
	//			MyAeroConfiguration configuration,
	//			MyPerformances performances) {
	//
	//		super(fuselage.get_name(),
	//				fuselage.get_description(), 
	//				fuselage.get_x(), 
	//				fuselage.get_y(), 
	//				fuselage.get_z(), 
	//				MyAeroComponent.MAIN_BODY);
	//
	//		_theAircraft = configuration;
	//		_thePerformances = performances;
	//
	//		try {
	//			fuselage.clone();
	//		} catch (CloneNotSupportedException e) {
	//			e.printStackTrace();
	//		}
	//
	//	} // end-of-constructor


	public Fuselage() {
		super("New fuselage", "Default fuselage", 0.0, 0.0, 0.0, ComponentEnum.FUSELAGE);

		_name = "New fuselage";
		_description = "Default fuselage";
		_X0 = Amount.valueOf(0.0, SI.METER);
		_Y0 = Amount.valueOf(0.0, SI.METER);
		_Z0 = Amount.valueOf(0.0, SI.METER);

		initializeDefaultVariables();
		calculateGeometry();
		checkGeometry();
	}

	// Import from file
		public Fuselage(
				String pathToXML, 
				String name, String description, 
				Double x, Double y, Double z, // fuselage apex wrt BRF
				ComponentEnum type // example: ComponentEnum.FUSELAGE
				) { 		
			super(name, description, x, y, z, type);
			
			if (
				!type.equals(ComponentEnum.FUSELAGE) 
				)
				throw new IllegalArgumentException("type must be a FUSELAGE!"); 
			
			_cg = new CenterOfGravity(_X0, _Y0, _Z0); // set the cg initially at the origin
			
			importFromXML(pathToXML);
		}
	
		private void importFromXML(String pathToXML) {
			_fuselageCreator = FuselageCreator.importFromXML(pathToXML);
		}
		
	public void initializeDefaultVariables() {

		// init variables - Reference aircraft: 
		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());

		// --- INPUT DATA ------------------------------------------

		_deckNumber = 1;

		_len_F         =  Amount.valueOf(27.166, 0.0, SI.METRE);
		_lenRatio_NF       = 0.1496;
		_lenRatio_CF       = 0.62;

		_sectionCylinderWidth     = Amount.valueOf(2.865,SI.METRE);
		_sectionCylinderHeight    = Amount.valueOf(2.6514, SI.METRE);

		// Nose fineness ratio, _len_N/_diam_N
		_lambda_N      = 1.2; 

		// Height from ground of lowest part of fuselage
		_heightFromGround = Amount.valueOf(0.66, SI.METER);

		// Fuselage Roughness
		_roughness = Amount.valueOf(0.405e-5, SI.METRE);

		// positive if nose tip higher than cylindrical part ref. line (in XZ plane)
		_height_N      = Amount.valueOf(-0.15*_sectionCylinderHeight.doubleValue(SI.METRE), SI.METRE);
		_height_T      = Amount.valueOf(  0.8*(0.5*_sectionCylinderHeight.doubleValue(SI.METRE)), SI.METRE);

		_massReference = Amount.valueOf(3340.6, SI.KILOGRAM);
		_pressurized = true;

		// Section parameters
		_dxNoseCapPercent = 0.0750;
		_dxTailCapPercent = 0.020; // TODO: check this! 0.050

		_windshieldType = "Single,round";
		_windshieldHeight = Amount.valueOf(0.8, SI.METER);
		_windshieldWidth = Amount.valueOf(2.5, SI.METER);

		_sectionCylinderLowerToTotalHeightRatio     = 0.4;
		_sectionCylinderRhoUpper     = 0.2;
		_sectionCylinderRhoLower     =  0.3;

		_sectionNoseMidLowerToTotalHeightRatio      = _sectionCylinderLowerToTotalHeightRatio.doubleValue();
		_sectionTailMidLowerToTotalHeightRatio      = _sectionCylinderLowerToTotalHeightRatio.doubleValue();
		//++++++++++++++++++++++++++++++++++++
		_sectionNoseMidToTotalHeightRatio_MIN       = _sectionLowerToTotalHeightRatio_MIN;
		_sectionNoseMidToTotalHeightRatio_MAX       = _sectionLowerToTotalHeightRatio_MAX;
		_sectionTailMidToTotalHeightRatio_MIN       = _sectionLowerToTotalHeightRatio_MIN;
		_sectionTailMidToTotalHeightRatio_MAX       = _sectionLowerToTotalHeightRatio_MAX;

		//+++++++++++++++++++++++++++++++++++++
		_sectionMidNoseRhoUpper      = _sectionCylinderRhoUpper.doubleValue();
		_sectionMidNoseRhoUpper_MIN  = _sectionRhoUpper_MIN;
		_sectionMidNoseRhoUpper_MAX  = _sectionRhoUpper_MAX;
		_sectionMidTailRhoUpper      = _sectionCylinderRhoUpper.doubleValue();
		_sectionMidTailRhoUpper_MIN  = _sectionRhoUpper_MIN;
		_sectionMidTailRhoUpper_MAX  = _sectionRhoUpper_MAX;

		//+++++++++++++++++++++++++++++++++++++
		_sectionMidNoseRhoLower      = _sectionCylinderRhoLower.doubleValue();
		_sectionMidNoseRhoLower_MIN  = _sectionRhoLower_MIN;
		_sectionMidNoseRhoLower_MAX  = _sectionRhoLower_MAX;
		_sectionMidTailRhoLower      = _sectionCylinderRhoLower.doubleValue();
		_sectionMidTailRhoLower_MIN  = _sectionRhoLower_MIN;
		_sectionMidTailRhoLower_MAX  = _sectionRhoLower_MAX;

		// --- END OF INPUT DATA ------------------------------------------ 

	}

	/**
	 * Overload of the previous method that recognize aircrafts and initialize fuselage data with the relative ones. 
	 * 
	 * @author Vittorio Trifari
	 */
	public void initializeDefaultVariables(AircraftEnum aircraftName) {

		// init variables - Reference aircraft: 
		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());

		// --- INPUT DATA ------------------------------------------

		switch(aircraftName) {
		case ATR72:
			_deckNumber = 1;

			_len_F         =  Amount.valueOf(27.166, 0.0, SI.METRE);
			_lenRatio_NF       = 0.1496;
			_lenRatio_CF       = 0.62;

			_sectionCylinderWidth     = Amount.valueOf(2.865,SI.METRE);
			_sectionCylinderHeight    = Amount.valueOf(2.6514, SI.METRE);

			// Nose fineness ratio, _len_N/_diam_N
			_lambda_N      = 1.2; 

			// Height from ground of lowest part of fuselage
			_heightFromGround = Amount.valueOf(0.66, SI.METER);

			// Fuselage Roughness
			_roughness = Amount.valueOf(0.405e-5, SI.METRE);

			// positive if nose tip higher than cylindrical part ref. line (in XZ plane)
			_height_N      = Amount.valueOf(-0.15*_sectionCylinderHeight.doubleValue(SI.METRE), SI.METRE);
			_height_T      = Amount.valueOf(  0.8*(0.5*_sectionCylinderHeight.doubleValue(SI.METRE)), SI.METRE);

			_massReference = Amount.valueOf(3340.6, SI.KILOGRAM);
			_pressurized = true;

			// Section parameters
			_dxNoseCapPercent = 0.0750;
			_dxTailCapPercent = 0.020; // TODO: check this! 0.050

			_windshieldType = "Single,round";
			_windshieldHeight = Amount.valueOf(0.8, SI.METER);
			_windshieldWidth = Amount.valueOf(2.5, SI.METER);

			_sectionCylinderLowerToTotalHeightRatio     = 0.4;
			_sectionCylinderRhoUpper     = 0.2;
			_sectionCylinderRhoLower     =  0.3;

			_sectionNoseMidLowerToTotalHeightRatio      = _sectionCylinderLowerToTotalHeightRatio.doubleValue();
			_sectionTailMidLowerToTotalHeightRatio      = _sectionCylinderLowerToTotalHeightRatio.doubleValue();
			//++++++++++++++++++++++++++++++++++++
			_sectionNoseMidToTotalHeightRatio_MIN       = _sectionLowerToTotalHeightRatio_MIN;
			_sectionNoseMidToTotalHeightRatio_MAX       = _sectionLowerToTotalHeightRatio_MAX;
			_sectionTailMidToTotalHeightRatio_MIN       = _sectionLowerToTotalHeightRatio_MIN;
			_sectionTailMidToTotalHeightRatio_MAX       = _sectionLowerToTotalHeightRatio_MAX;

			//+++++++++++++++++++++++++++++++++++++
			_sectionMidNoseRhoUpper      = _sectionCylinderRhoUpper.doubleValue();
			_sectionMidNoseRhoUpper_MIN  = _sectionRhoUpper_MIN;
			_sectionMidNoseRhoUpper_MAX  = _sectionRhoUpper_MAX;
			_sectionMidTailRhoUpper      = _sectionCylinderRhoUpper.doubleValue();
			_sectionMidTailRhoUpper_MIN  = _sectionRhoUpper_MIN;
			_sectionMidTailRhoUpper_MAX  = _sectionRhoUpper_MAX;

			//+++++++++++++++++++++++++++++++++++++
			_sectionMidNoseRhoLower      = _sectionCylinderRhoLower.doubleValue();
			_sectionMidNoseRhoLower_MIN  = _sectionRhoLower_MIN;
			_sectionMidNoseRhoLower_MAX  = _sectionRhoLower_MAX;
			_sectionMidTailRhoLower      = _sectionCylinderRhoLower.doubleValue();
			_sectionMidTailRhoLower_MIN  = _sectionRhoLower_MIN;
			_sectionMidTailRhoLower_MAX  = _sectionRhoLower_MAX;
			break;
			
		case B747_100B:
			_deckNumber = 1;

			_len_F         =  Amount.valueOf(68.63, 0.0, SI.METRE);
			_lenRatio_NF       = 0.1635;
			_lenRatio_CF       = 0.4964;

			_sectionCylinderWidth     = Amount.valueOf(6.5,SI.METRE);
			_sectionCylinderHeight    = Amount.valueOf(7.1, SI.METRE);

			// Nose fineness ratio, _len_N/_diam_N
			_lambda_N      = 1.521; 

			// Height from ground of lowest part of fuselage
			_heightFromGround = Amount.valueOf(2.1, SI.METER);

			// Fuselage Roughness
			_roughness = Amount.valueOf(0.405e-5, SI.METRE);

			// positive if nose tip higher than cylindrical part ref. line (in XZ plane)
			_height_N      = Amount.valueOf(-0.089*_sectionCylinderHeight.doubleValue(SI.METRE), SI.METRE);
			_height_T      = Amount.valueOf( 0.457*(0.5*_sectionCylinderHeight.doubleValue(SI.METRE)), SI.METRE);

			_massReference = Amount.valueOf(32061.0, SI.KILOGRAM);
			_pressurized = true;

			// Section parameters
			_dxNoseCapPercent = 0.075;
			_dxTailCapPercent = 0.020; 

			_windshieldType = "Single,round";
			_windshieldHeight = Amount.valueOf(0.5, SI.METER);
			_windshieldWidth = Amount.valueOf(2.6, SI.METER);

			_sectionCylinderLowerToTotalHeightRatio     = 0.4;
			_sectionCylinderRhoUpper     = 0.2;
			_sectionCylinderRhoLower     =  0.3;

			_sectionNoseMidLowerToTotalHeightRatio      = _sectionCylinderLowerToTotalHeightRatio.doubleValue();
			_sectionTailMidLowerToTotalHeightRatio      = _sectionCylinderLowerToTotalHeightRatio.doubleValue();
			//++++++++++++++++++++++++++++++++++++
			_sectionNoseMidToTotalHeightRatio_MIN       = _sectionLowerToTotalHeightRatio_MIN;
			_sectionNoseMidToTotalHeightRatio_MAX       = _sectionLowerToTotalHeightRatio_MAX;
			_sectionTailMidToTotalHeightRatio_MIN       = _sectionLowerToTotalHeightRatio_MIN;
			_sectionTailMidToTotalHeightRatio_MAX       = _sectionLowerToTotalHeightRatio_MAX;

			//+++++++++++++++++++++++++++++++++++++
			_sectionMidNoseRhoUpper      = _sectionCylinderRhoUpper.doubleValue();
			_sectionMidNoseRhoUpper_MIN  = _sectionRhoUpper_MIN;
			_sectionMidNoseRhoUpper_MAX  = _sectionRhoUpper_MAX;
			_sectionMidTailRhoUpper      = _sectionCylinderRhoUpper.doubleValue();
			_sectionMidTailRhoUpper_MIN  = _sectionRhoUpper_MIN;
			_sectionMidTailRhoUpper_MAX  = _sectionRhoUpper_MAX;

			//+++++++++++++++++++++++++++++++++++++
			_sectionMidNoseRhoLower      = _sectionCylinderRhoLower.doubleValue();
			_sectionMidNoseRhoLower_MIN  = _sectionRhoLower_MIN;
			_sectionMidNoseRhoLower_MAX  = _sectionRhoLower_MAX;
			_sectionMidTailRhoLower      = _sectionCylinderRhoLower.doubleValue();
			_sectionMidTailRhoLower_MIN  = _sectionRhoLower_MIN;
			_sectionMidTailRhoLower_MAX  = _sectionRhoLower_MAX;
			break;
			
		case AGILE_DC1:
			_deckNumber = 1;

			_len_F         =  Amount.valueOf(34., 0.0, SI.METRE);
			_lenRatio_NF       = 0.1420;
			_lenRatio_CF       = 0.6148;

			_sectionCylinderWidth     = Amount.valueOf(3.,SI.METRE);
			_sectionCylinderHeight    = Amount.valueOf(3., SI.METRE);

			// Nose fineness ratio, _len_N/_diam_N
			_lambda_N      = 1.4; 

			// Height from ground of lowest part of fuselage
			_heightFromGround = Amount.valueOf(4.25, SI.METER);

			// Fuselage Roughness
			_roughness = Amount.valueOf(0.405e-5, SI.METRE);

			// positive if nose tip higher than cylindrical part ref. line (in XZ plane)
			_height_N      = Amount.valueOf(-0.1698*_sectionCylinderHeight.doubleValue(SI.METRE), SI.METRE);
			_height_T      = Amount.valueOf( 0.262*(0.5*_sectionCylinderHeight.doubleValue(SI.METRE)), SI.METRE);

			_massReference = Amount.valueOf(6106.0, SI.KILOGRAM);
			_pressurized = true;

			// Section parameters
			_dxNoseCapPercent = 0.075; // TODO: Have to Check
			_dxTailCapPercent = 0.020; // TODO: Have to Check

			_windshieldType = "Single,round";
			_windshieldHeight = Amount.valueOf(0.5, SI.METER);
			_windshieldWidth = Amount.valueOf(3.0, SI.METER);

			_sectionCylinderLowerToTotalHeightRatio     = 0.4;
			_sectionCylinderRhoUpper     = 0.1;
			_sectionCylinderRhoLower     = 0.1;

			_sectionNoseMidLowerToTotalHeightRatio      = _sectionCylinderLowerToTotalHeightRatio.doubleValue();
			_sectionTailMidLowerToTotalHeightRatio      = _sectionCylinderLowerToTotalHeightRatio.doubleValue();
			//++++++++++++++++++++++++++++++++++++
			_sectionNoseMidToTotalHeightRatio_MIN       = _sectionLowerToTotalHeightRatio_MIN;
			_sectionNoseMidToTotalHeightRatio_MAX       = _sectionLowerToTotalHeightRatio_MAX;
			_sectionTailMidToTotalHeightRatio_MIN       = _sectionLowerToTotalHeightRatio_MIN;
			_sectionTailMidToTotalHeightRatio_MAX       = _sectionLowerToTotalHeightRatio_MAX;

			//+++++++++++++++++++++++++++++++++++++
			_sectionMidNoseRhoUpper      = _sectionCylinderRhoUpper.doubleValue();
			_sectionMidNoseRhoUpper_MIN  = _sectionRhoUpper_MIN;
			_sectionMidNoseRhoUpper_MAX  = _sectionRhoUpper_MAX;
			_sectionMidTailRhoUpper      = _sectionCylinderRhoUpper.doubleValue();
			_sectionMidTailRhoUpper_MIN  = _sectionRhoUpper_MIN;
			_sectionMidTailRhoUpper_MAX  = _sectionRhoUpper_MAX;

			//+++++++++++++++++++++++++++++++++++++
			_sectionMidNoseRhoLower      = _sectionCylinderRhoLower.doubleValue();
			_sectionMidNoseRhoLower_MIN  = _sectionRhoLower_MIN;
			_sectionMidNoseRhoLower_MAX  = _sectionRhoLower_MAX;
			_sectionMidTailRhoLower      = _sectionCylinderRhoLower.doubleValue();
			_sectionMidTailRhoLower_MIN  = _sectionRhoLower_MIN;
			_sectionMidTailRhoLower_MAX  = _sectionRhoLower_MAX;
			break;
		}
		// --- END OF INPUT DATA ------------------------------------------ 
	}

	public void calculateGeometry() {

		// --- OUTPUT DATA ------------------------------------------

		_lenRatio_TF   = 1.0 - _lenRatio_CF - _lenRatio_NF;

		_len_N         = Amount.valueOf( _lenRatio_NF * _len_F.doubleValue(SI.METRE), SI.METRE);
		_len_C         = Amount.valueOf( _lenRatio_CF * _len_F.doubleValue(SI.METRE), SI.METRE);
		_len_T         = Amount.valueOf( _lenRatio_TF * _len_F.doubleValue(SI.METRE), SI.METRE);
		_lambda_C      = _len_C.doubleValue(SI.METRE)/_sectionCylinderHeight.doubleValue(SI.METRE); // _len_C / _diam_C;
		_lambda_T      = _len_T.doubleValue(SI.METRE)/_sectionCylinderHeight.doubleValue(SI.METRE); // _len_T / _diam_C; // (_len_F - _len_N - _len_C) / _diam_C

		_dxNoseCap = Amount.valueOf(_len_N.times(_dxNoseCapPercent).doubleValue(SI.METRE), SI.METRE);
		_dxTailCap = Amount.valueOf(_len_T.times(_dxTailCapPercent).doubleValue(SI.METRE), SI.METRE);

		_windshieldArea = Amount.valueOf(_windshieldHeight.getEstimatedValue()*_windshieldWidth.getEstimatedValue(), Area.UNIT);

		_phi_N         = Amount.valueOf( 
				Math.atan(
						(_sectionCylinderHeight.doubleValue(SI.METRE) - _height_N.doubleValue(SI.METRE))
						/ _len_N.doubleValue(SI.METRE)
						), 
						SI.RADIAN);

		//		// mesh stuff
		//		_deltaXNose     = _len_N.doubleValue(SI.METRE)/(_np_N-1);
		//		_deltaXCylinder = _len_C.doubleValue(SI.METRE)/(_np_C-1);
		//		_deltaXTail     = _len_T.doubleValue(SI.METRE)/(_np_T-1);

		//////////////////////////////////////////////////
		// make all calculations
		//////////////////////////////////////////////////
		calculateOutlines(
				10, // num. points Nose
				4,  // num. points Cylinder
				10, // num. points Tail
				10, // num. points Upper section
				10  // num. points Lower section
				);

		// Equivalent diameters
		_equivalentDiameterCylinderGM = Amount.valueOf(
				Math.sqrt(_sectionCylinderWidth.getEstimatedValue()*_sectionCylinderHeight.getEstimatedValue())
				,SI.METRE);

		_equivalentDiameterGM = Amount.valueOf(calculateEquivalentDiameter(), SI.METRE);

		_equivalentDiameterCylinderAM = Amount.valueOf(
				MyMathUtils.arithmeticMean(
						_sectionCylinderWidth.getEstimatedValue(),_sectionCylinderHeight.getEstimatedValue())
						,SI.METRE);
		//		_equivalentDiameterAM = ;

		// Whole Fuselage fineness ratio 
		_lambda_F = _len_F.getEstimatedValue()/_equivalentDiameterCylinderGM.getEstimatedValue(); // _len_F/_diam_C;

		// cylindrical section base area
		_area_C = Amount.valueOf(Math.PI *(_sectionCylinderHeight.getEstimatedValue()*_sectionCylinderWidth.getEstimatedValue())/4, Area.UNIT);

		calculateSwet("Stanford");

		// Form factor Kff
		_formFactor =  calculateFormFactor(_lambda_F);

		//		_reynolds = _theOperatingConditions.calculateRe(_len_F.getEstimatedValue(), _roughness.getEstimatedValue());

		calculateUpsweepAngle();
		calculateWindshieldAngle();
		
		// --- END OF OUTPUT DATA -----------------------------------------

	}

	public void checkGeometry() {

		// --- CHECKS -----------------------------------------------------

		_lambda_C_MIN  = 3.0;
		_lambda_C_MAX  = 7.0;
		_len_F_MIN     =  Amount.valueOf(10.0,SI.METRE);		
		_len_F_MAX     =  Amount.valueOf(80.0,SI.METRE);
		_lenRatio_NF_MIN   = 0.1;
		_lenRatio_NF_MAX   = 0.2;

		_len_N_MIN     = Amount.valueOf(1.0, SI.METRE);
		_len_N_MAX     = Amount.valueOf(8.0, SI.METRE);
		_lenRatio_CF_MIN   = 0.4;
		_lenRatio_CF_MAX   = 0.8;
		_lambda_N_MIN  = 1.2;
		_lambda_N_MAX  = 2.5;

		_lambda_T_MIN  = 2.8;
		_lambda_T_MAX  = 3.2;
		_lenRatio_TF_MIN = 1.0-_lenRatio_CF_MIN - _lenRatio_NF_MIN;
		_lenRatio_TF_MAX = 1.0-_lenRatio_CF_MAX - _lenRatio_NF_MAX;
		_len_T_MIN     = Amount.valueOf( 2.0, SI.METRE);
		_len_T_MAX     = Amount.valueOf( 25.0, SI.METRE);

		// Bounds to diameter value input
		_diam_C_MIN    = Amount.valueOf(2.0, SI.METRE);
		_diam_C_MAX    = Amount.valueOf( 10.0, SI.METRE);

		_sectionWidth_MIN         = Amount.valueOf(0.7*_diam_C_MIN.doubleValue(SI.METRE), SI.METRE);
		_sectionWidth_MAX         = Amount.valueOf(1.3*_diam_C_MAX.doubleValue(SI.METRE), SI.METRE);

		_height_N_MIN  =(Amount.valueOf( -0.2 *_sectionCylinderHeight.doubleValue(SI.METRE), SI.METRE));
		_height_N_MAX  =(Amount.valueOf( 0.2 *_sectionCylinderHeight.doubleValue(SI.METRE), SI.METRE));

		_height_T_MIN  =(Amount.valueOf(  0.4*(0.5*_sectionCylinderHeight.doubleValue(SI.METRE)), SI.METRE));
		_height_T_MAX  =(Amount.valueOf(  1.0*(0.5*_sectionCylinderHeight.doubleValue(SI.METRE)), SI.METRE));

		_dxNoseCap_MIN            = Amount.valueOf(0.015 * _len_N.doubleValue(SI.METRE), SI.METRE);
		_dxNoseCap_MAX            = Amount.valueOf(0.0150* _len_N.doubleValue(SI.METRE), SI.METRE);

		_dxTailCap_MIN            = Amount.valueOf(0.000*_len_T.doubleValue(SI.METRE), SI.METRE);
		_dxTailCap_MAX            = Amount.valueOf(0.100*_len_T.doubleValue(SI.METRE), SI.METRE);

		_lambda_F_MIN  = 8.0;
		_lambda_F_MAX  = 12.5;

		_sectionLowerToTotalHeightRatio_MIN = 0.1;
		_sectionLowerToTotalHeightRatio_MAX = 0.5;

		_sectionRhoUpper_MIN = 0.0;
		_sectionRhoUpper_MAX = 1.0;
		_sectionRhoLower_MIN = 0.0;
		_sectionRhoLower_MAX = 1.0;

		_len_C_MIN     = Amount.valueOf( 0.35 * _len_F_MIN.doubleValue(SI.METRE), SI.METRE);
		_len_C_MAX     = Amount.valueOf(0.75 * _len_F_MAX.doubleValue(SI.METRE), SI.METRE);

		// --- END OF CHECKS ----------------------------------------

	}

	public FuselageAerodynamicsManager initializeAerodynamics(OperatingConditions ops, Aircraft aircraft) {
		_aerodynamicDatabaseReader = aircraft.get_theAerodynamics().get_aerodynamicDatabaseReader();
		aerodynamics = new FuselageAerodynamicsManager(ops, aircraft);
		return aerodynamics;
	}
	
	public double calculateFormFactor(double lambdaF) {
		return 1. + 60./Math.pow(lambdaF,3) + 0.0025*(lambdaF);
	}

	public void calculateSwet(String method) {

		switch (method) {

		case "Stanford" : {
			_sWetNose = Amount.valueOf(0.75 * Math.PI * get_equivalentDiameterCylinderGM().getEstimatedValue()*_len_N.getEstimatedValue(), Area.UNIT);
			_sWetTail = Amount.valueOf(0.72 * Math.PI * get_equivalentDiameterCylinderGM().getEstimatedValue()*_len_T.getEstimatedValue(), Area.UNIT);
			_sWetC = Amount.valueOf(Math.PI * get_equivalentDiameterCylinderGM().getEstimatedValue()*_len_C.getEstimatedValue(), Area.UNIT);
			_sWet = Amount.valueOf(_sWetNose.getEstimatedValue() + _sWetTail.getEstimatedValue() + _sWetC.getEstimatedValue(), Area.UNIT); break;		
		}

		case "Torenbeek" : { // page 409 torenbeek 2013
			_sFront = Amount.valueOf((Math.PI/4) * Math.pow(_sectionCylinderHeight.getEstimatedValue(),2), Area.UNIT); // CANNOT FIND FUSELAGE HEIGHT in MyAeroFuselage!!
			_sWet = Amount.valueOf(_sFront.getEstimatedValue()*4*(get_lambda_F() - 1.30), Area.UNIT); break;	
		}

		}
		
	}

	public static double calculateSfront(double fuselageDiameter){
		return Math.PI*Math.pow(fuselageDiameter, 2)/4;
	}

	// see MyInitiatorPaneFuselage::recalculateCurves()
	/**
	 * Generate the fuselage profile curves in XZ plane, i.e. upper and lower curves in A/C symmetry plane
	 * and generate side curves, as seen from topview, i.e. view from Z+ to Z-
	 * 
	 * @param np_N number of points discretizing the nose part
	 * @param np_C number of points discretizing the cilyndrical part
	 * @param np_T number of points discretizing the tail part
	 * @param np_SecUp number of points discretizing the upper YZ sections
	 * @param np_SecLow number of points discretizing the lower YZ sections
	 */
	public void calculateOutlines(int np_N, int np_C, int np_T, int np_SecUp, int np_SecLow)
	{

		// calculate initial curves
		// get variables

		Double l_F = get_len_F().doubleValue(SI.METRE);
		Double l_N = get_len_N().doubleValue(SI.METRE);
		Double l_C = get_len_C().doubleValue(SI.METRE);
		Double l_T = get_len_T().doubleValue(SI.METRE);
		Double d_C = get_sectionCylinderHeight().doubleValue(SI.METRE);
		Double h_N = get_height_N().doubleValue(SI.METRE); // Fuselage origin O_T at nose (>0, when below the cylindrical midline)
		Double h_T = get_height_T().doubleValue(SI.METRE);
		Double w_B = get_sectionCylinderWidth().doubleValue(SI.METRE);
		Double a   = get_sectionCylinderLowerToTotalHeightRatio().doubleValue();
		Double rhoUpper = get_sectionCylinderRhoUpper().doubleValue();
		Double rhoLower = get_sectionCylinderRhoLower().doubleValue();

		_np_N           = np_N;
		_np_C           = np_C;
		_np_T           = np_T;
		_deltaXNose     = l_N/(_np_N-1);
		_deltaXCylinder = l_C/(_np_C-1);
		_deltaXTail     = l_T/(_np_T-1);
		_np_SecUp  = np_SecUp;
		_np_SecLow = np_SecLow;

		// clean all points before recalculating
		clearOutlines();

		//------------------------------------------------
		// XZ VIEW -- Side View
		//------------------------------------------------

		FuselageCurvesSideView fuselageCurvesSideView = new FuselageCurvesSideView(
				l_N, h_N, l_C, l_F, h_T, d_C/2, a, // lengths 
				_np_N, _np_C, _np_T        // no. points (nose, cylinder, tail)
				);

		// UPPER CURVES ----------------------------------

		// UPPER NOSE
		for (int i = 0; i <= fuselageCurvesSideView.getNoseUpperPoints().size() - 1; i++){
			getOutlineXZUpperCurveX().add((double) fuselageCurvesSideView.getNoseUpperPoints().get(i).x);
			getOutlineXZUpperCurveZ().add((double) fuselageCurvesSideView.getNoseUpperPoints().get(i).y);
		}

		//		// MESH
		//		// get points in XZ plane as a basis for interpolation
		//		List<Double> pNoseUpperPointsX = new ArrayList<Double>();
		//		List<Double> pNoseUpperPointsZ = new ArrayList<Double>();
		//
		//		for (int k=0; k < fuselageCurvesSideView.getNoseUpperPoints().size(); k++)
		//		{
		//			pNoseUpperPointsX.add((double) fuselageCurvesSideView.getNoseUpperPoints().get(k).x);
		//			pNoseUpperPointsZ.add((double) fuselageCurvesSideView.getNoseUpperPoints().get(k).y);
		//		}
		//
		//		double vxUpperNose[] = new double[pNoseUpperPointsX.size()];
		//		for (int i = 0; i < vxUpperNose.length; i++) {
		//			vxUpperNose[i] = pNoseUpperPointsX.get(i);
		//			
		//		}
		//		double vzUpperNose []  = new double[pNoseUpperPointsZ.size()];
		//		for (int i = 0; i < vzUpperNose.length; i++) {
		//			vzUpperNose[i] = pNoseUpperPointsZ.get(i);
		//		}
		//		//  build the interpolation objects
		//		UnivariateInterpolator interpolatorUpperNose = new LinearInterpolator(); // SplineInterpolator();
		//		UnivariateFunction myInterpolationFunctionUpperNose = interpolatorUpperNose.interpolate(vxUpperNose, vzUpperNose);
		//		// now interpolate
		//		for (int i = 0; i < _np_N; i++) {
		//			double x = 0.0 + _deltaXNose*i;
		//			_meshXZCurveX.add(x);
		//			//System.out.println(vx[i]);
		//			double z = myInterpolationFunctionUpperNose.value(x);
		//			_meshXZUpperCurveZ.add(z);
		//
		//		}

		// UPPER CYLINDER
		for (int i = 0; i <= fuselageCurvesSideView.getCylinderUpperPoints().size() - 1; i++){
			getOutlineXZUpperCurveX().add((double) fuselageCurvesSideView.getCylinderUpperPoints().get(i).x);
			getOutlineXZUpperCurveZ().add((double) fuselageCurvesSideView.getCylinderUpperPoints().get(i).y);
		}

		//		// MESH
		//		for (int i = 0; i < _np_C; i++) {
		//			double x = l_N + _deltaXCylinder*i;
		//			_meshXZCurveX.add(x);
		//			double z = 0.5*d_C; 
		//			_meshXZUpperCurveZ.add(z);
		//		}

		// UPPER TAIL
		for (int i = 0; i <= fuselageCurvesSideView.getTailUpperPoints().size() - 1; i++){
			getOutlineXZUpperCurveX().add((double) fuselageCurvesSideView.getTailUpperPoints().get(i).x);
			getOutlineXZUpperCurveZ().add((double) fuselageCurvesSideView.getTailUpperPoints().get(i).y);
		}

		//		// MESH
		//		// get points in XZ plane as a basis for interpolation
		//		List<Double> pTailUpperPointsX = new ArrayList<Double>();
		//		List<Double> pTailUpperPointsZ = new ArrayList<Double>();
		//		for (int k=0; k < fuselageCurvesSideView.getTailUpperPoints().size(); k++)
		//		{
		//			pTailUpperPointsX.add((double) fuselageCurvesSideView.getTailUpperPoints().get(k).x);
		//			pTailUpperPointsZ.add((double) fuselageCurvesSideView.getTailUpperPoints().get(k).y);
		//		}
		//		double vxUpperTail[] = new double[pTailUpperPointsX.size()];
		//		for (int i = 0; i < vxUpperTail.length; i++) {
		//			vxUpperTail[i] = pTailUpperPointsX.get(i);
		//		}
		//		double vzUpperTail []  = new double[pTailUpperPointsZ.size()];
		//		for (int i = 0; i < vzUpperTail.length; i++) {
		//			vzUpperTail[i] = pTailUpperPointsZ.get(i);
		//		}
		//		//  build the interpolation objects
		//		UnivariateInterpolator interpolatorUpperTail = new LinearInterpolator(); // SplineInterpolator();
		//		UnivariateFunction myInterpolationFunctionUpperTail = interpolatorUpperTail.interpolate(vxUpperTail, vzUpperTail);
		//		// now interpolate
		//		for (int i = 0; i < _np_T; i++) {
		//			double x = l_N+l_C + _deltaXTail*i;
		//			_meshXZCurveX.add(x);
		//			double z = myInterpolationFunctionUpperTail.value(x);
		//			_meshXZUpperCurveZ.add(z);
		//
		//		}

		// LOWER CURVES ----------------------------------

		// LOWER NOSE
		for (int i = 0; i <= fuselageCurvesSideView.getNoseLowerPoints().size() - 1; i++){
			getOutlineXZLowerCurveX().add((double) fuselageCurvesSideView.getNoseLowerPoints().get(i).x);
			getOutlineXZLowerCurveZ().add((double) fuselageCurvesSideView.getNoseLowerPoints().get(i).y);
		}

		//		// MESH
		//		// get points in XZ plane as a basis for interpolation
		//		List<Double> pNoseLowerPointsX = new ArrayList<Double>();
		//		List<Double> pNoseLowerPointsZ = new ArrayList<Double>();
		//		for (int k=0; k < fuselageCurvesSideView.getNoseLowerPoints().size(); k++)
		//		{
		//			pNoseLowerPointsX.add((double) fuselageCurvesSideView.getNoseLowerPoints().get(k).x);
		//			pNoseLowerPointsZ.add((double) fuselageCurvesSideView.getNoseLowerPoints().get(k).y);
		//		}
		//		double vxLowerNose[] = new double[pNoseLowerPointsX.size()];
		//		for (int i = 0; i < vxLowerNose.length; i++) {
		//			vxLowerNose[i] = pNoseLowerPointsX.get(i);
		//		}
		//		double vzLowerNose []  = new double[pNoseLowerPointsZ.size()];
		//		for (int i = 0; i < vzLowerNose.length; i++) {
		//			vzLowerNose[i] = pNoseLowerPointsZ.get(i);
		//		}
		//		//  build the interpolation objects
		//		UnivariateInterpolator interpolatorLowerNose = new LinearInterpolator(); // SplineInterpolator();
		//		UnivariateFunction myInterpolationFunctionLowerNose = interpolatorLowerNose.interpolate(vxLowerNose, vzLowerNose);
		//		for (int i = 0; i < _np_N; i++) {
		//			double x = 0.0 + _deltaXNose*i;
		//			_meshXZCurveX.add(x);
		//			double z = myInterpolationFunctionLowerNose.value(x);
		//			_meshXZLowerCurveZ.add(z);
		//		}

		// LOWER CYLINDER
		for (int i = 0; i<= fuselageCurvesSideView.getCylinderLowerPoints().size() - 1; i++){
			getOutlineXZLowerCurveX().add((double) fuselageCurvesSideView.getCylinderLowerPoints().get(i).x);
			getOutlineXZLowerCurveZ().add((double) fuselageCurvesSideView.getCylinderLowerPoints().get(i).y);
		}

		//		// MESH
		//		for (int i = 0; i < _np_C; i++) {
		//			double x = l_N+ _deltaXCylinder*i;
		//			_meshXZCurveX.add(x);
		//			double z = -0.5*d_C; // -a*d_C;
		//			_meshXZLowerCurveZ.add(z);
		//		}

		// LOWER TAIL
		for (int i = 0; i <= fuselageCurvesSideView.getTailLowerPoints().size() - 1; i++)
		{
			getOutlineXZLowerCurveX().add((double) fuselageCurvesSideView.getTailLowerPoints().get(i).x);
			getOutlineXZLowerCurveZ().add((double) fuselageCurvesSideView.getTailLowerPoints().get(i).y);
		}

		// MESH
		// get points in XZ plane as a basis for interpolation
		//		List<Double> pTailLowerPointsX = new ArrayList<Double>();
		//		List<Double> pTailLowerPointsZ = new ArrayList<Double>();
		//		for (int k=0; k < fuselageCurvesSideView.getTailLowerPoints().size(); k++)
		//		{
		//			pTailLowerPointsX.add((double) fuselageCurvesSideView.getTailLowerPoints().get(k).x);
		//			pTailLowerPointsZ.add((double) fuselageCurvesSideView.getTailLowerPoints().get(k).y);
		//		}
		//		double vxLowerTail[] = new double[pTailLowerPointsX.size()];
		//		for (int i = 0; i < vxLowerTail.length; i++) {
		//			vxLowerTail[i] = pTailLowerPointsX.get(i);
		//		}
		//		double vzLowerTail []  = new double[pTailLowerPointsZ.size()];
		//		for (int i = 0; i < vzLowerTail.length; i++) {
		//			vzLowerTail[i] = pTailLowerPointsZ.get(i);
		//		}
		//		//  build the interpolation objects
		//		UnivariateInterpolator interpolatorLowerTail = new LinearInterpolator(); // SplineInterpolator();
		//		UnivariateFunction myInterpolationFunctionLowerTail = interpolatorLowerTail.interpolate(vxLowerTail, vzLowerTail);
		//		// now interpolate
		//		for (int i = 0; i < _np_T; i++) {
		//			double x =l_N+l_C + _deltaXTail*i;
		//			_meshXZCurveX.add(x);
		//			double z = myInterpolationFunctionLowerTail.value(x);
		//			_meshXZLowerCurveZ.add(z);
		//		}

		//  NOSE CAMBER LINE
		for (int i = 0; i <= fuselageCurvesSideView.getNoseCamberlinePoints().size() - 1; i++){
			getOutlineXZCamberLineX().add((double) fuselageCurvesSideView.getNoseCamberlinePoints().get(i).x);
			getOutlineXZCamberLineZ().add((double) fuselageCurvesSideView.getNoseCamberlinePoints().get(i).y);
		}

		//  CYLINDER CAMBER LINE
		for (int i = 0; i <= fuselageCurvesSideView.getCylinderCamberlinePoints().size() - 1; i++){
			getOutlineXZCamberLineX().add((double) fuselageCurvesSideView.getCylinderCamberlinePoints().get(i).x);
			getOutlineXZCamberLineZ().add((double) fuselageCurvesSideView.getCylinderCamberlinePoints().get(i).y);
		}

		//  TAIL CAMBER LINE
		for (int i = 0; i <= fuselageCurvesSideView.getTailCamberlinePoints().size() - 1; i++){
			getOutlineXZCamberLineX().add((double) fuselageCurvesSideView.getTailCamberlinePoints().get(i).x);
			getOutlineXZCamberLineZ().add((double) fuselageCurvesSideView.getTailCamberlinePoints().get(i).y);
		}

		//------------------------------------------------
		// XY VIEW -- Upper View
		//------------------------------------------------
		FuselageCurvesUpperView fuselageCurvesUpperView = new FuselageCurvesUpperView(
				l_N, l_C, l_F, w_B/2, // lengths 
				_np_N, _np_C, _np_T   // no. points (nose, cylinder, tail)
				);

		// RIGHT CURVE -----------------------------------

		// RIGHT NOSE
		for (int i=0; i<=fuselageCurvesUpperView.getNoseUpperPoints().size()-1; i++){
			getOutlineXYSideRCurveX().add((double) fuselageCurvesUpperView.getNoseUpperPoints().get(i).x);
			getOutlineXYSideRCurveY().add((double) fuselageCurvesUpperView.getNoseUpperPoints().get(i).y);
		}

		// RIGHT CYLINDER
		for (int i=0; i<=fuselageCurvesUpperView.getCylinderUpperPoints().size()-1; i++){
			getOutlineXYSideRCurveX().add((double) fuselageCurvesUpperView.getCylinderUpperPoints().get(i).x);
			getOutlineXYSideRCurveY().add((double) fuselageCurvesUpperView.getCylinderUpperPoints().get(i).y);
		}

		// RIGHT TAIL
		for (int i=0; i<=fuselageCurvesUpperView.getTailUpperPoints().size()-1; i++){
			getOutlineXYSideRCurveX().add((double) fuselageCurvesUpperView.getTailUpperPoints().get(i).x);
			getOutlineXYSideRCurveY().add((double) fuselageCurvesUpperView.getTailUpperPoints().get(i).y);
		}

		//		//------------------------------------------------
		//		// YZ VIEW -- Section/Front view
		//		//------------------------------------------------
		//
		FuselageCurvesSection fuselageCurvesSection = new FuselageCurvesSection(
				w_B, d_C, a, rhoUpper, rhoLower, // lengths 
				_np_SecUp, _np_SecLow            // no. points (nose, cylinder, tail)
				);

		// UPPER CURVE -----------------------------------
		// counter-clockwise
		for (int i = 0; i <= fuselageCurvesSection.getSectionUpperRightPoints().size() - 1; i++){
			getSectionUpperCurveY().add(
					(double) fuselageCurvesSection.getSectionUpperRightPoints().get(i).x
					);
			getSectionUpperCurveZ().add(
					(double) fuselageCurvesSection.getSectionUpperRightPoints().get(i).y
					);
		}
		// TO DO: CAREFUL WITH REPEATED POINTS
		for (int i = 0; i <= fuselageCurvesSection.getSectionUpperLeftPoints().size() - 1; i++){
			getSectionUpperCurveY().add(
					(double) fuselageCurvesSection.getSectionUpperLeftPoints().get(i).x
					);
			getSectionUpperCurveZ().add(
					(double) fuselageCurvesSection.getSectionUpperLeftPoints().get(i).y
					);
		}

		// LOWER CURVE -----------------------------------
		// counter-clockwise
		for (int i = 0; i <= fuselageCurvesSection.getSectionLowerLeftPoints().size() - 1; i++){
			getSectionLowerCurveY().add(
					(double) fuselageCurvesSection.getSectionLowerLeftPoints().get(i).x
					);
			getSectionLowerCurveZ().add(
					(double) fuselageCurvesSection.getSectionLowerLeftPoints().get(i).y
					);
		}
		// TO DO: CAREFUL WITH REPEATED POINTS
		for (int i = 0; i <= fuselageCurvesSection.getSectionLowerRightPoints().size() - 1; i++){
			getSectionLowerCurveY().add(
					(double) fuselageCurvesSection.getSectionLowerRightPoints().get(i).x
					);
			getSectionLowerCurveZ().add(
					(double) fuselageCurvesSection.getSectionLowerRightPoints().get(i).y
					);

		}

		//		// EXPERIMENTAL
		//		
		////		MyFuselageCurvesSection fusCS1 = makeSection(0.5*_len_N.doubleValue(SI.METRE));
		////		if ( fusCS1 != null )
		////		{
		//			_sectionUpperCurveY1.clear();
		//			_sectionUpperCurveZ1.clear();
		//			_sectionLowerCurveY1.clear();
		//			_sectionLowerCurveZ1.clear();
		//			for (int i=0; i < fusCS1.getSectionUpperLeftPoints().size(); i++) {
		//				_sectionUpperCurveY1.add( (double) fusCS1.getSectionUpperLeftPoints().get(i).x); 
		//				_sectionUpperCurveZ1.add( (double) fusCS1.getSectionUpperLeftPoints().get(i).y); 
		//			}
		//			for (int i=0; i < fusCS1.getSectionUpperRightPoints().size(); i++) {
		//				_sectionUpperCurveY1.add( (double) fusCS1.getSectionUpperRightPoints().get(i).x); 
		//				_sectionUpperCurveZ1.add( (double) fusCS1.getSectionUpperRightPoints().get(i).y); 
		//			}
		//			for (int i=0; i < fusCS1.getSectionLowerLeftPoints().size(); i++) {
		//				_sectionLowerCurveY1.add( (double) fusCS1.getSectionLowerLeftPoints().get(i).x); 
		//				_sectionLowerCurveZ1.add( (double) fusCS1.getSectionLowerLeftPoints().get(i).y); 
		//			}
		//			for (int i=0; i < fusCS1.getSectionLowerRightPoints().size(); i++) {
		//				_sectionLowerCurveY1.add( (double) fusCS1.getSectionLowerRightPoints().get(i).x); 
		//				_sectionLowerCurveZ1.add( (double) fusCS1.getSectionLowerRightPoints().get(i).y); 
		//			}
		//		} // fusCS1 != null



		//-------------------------------------------------------
		// Create section-YZ objects
		//-------------------------------------------------------

		// Populate the list of YZ sections
		_sectionsYZ.clear();
		_sectionsYZStations.clear();

		// NOSE TIP
		// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
		Double x  = 0.;//_dxNoseCap.doubleValue(SI.METRE)  ; // NOTE
		Double hf =  Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		Double wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		_sectionsYZ.add(
				new FuselageCurvesSection(
						wf, hf, 0.5, 0.0, 0.0, // lengths & parameters 
						_np_SecUp, _np_SecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		_sectionsYZStations.add(Amount.valueOf(0.0,SI.METRE));

		// NOSE CAP
		// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
		x  = _dxNoseCap.doubleValue(SI.METRE); // NOTE
		hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		_sectionsYZ.add(
				new FuselageCurvesSection(
						wf, hf, 0.5, 0.0, 0.0, // lengths & parameters 
						_np_SecUp, _np_SecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		_sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// MID-NOSE
		// IDX_SECTION_YZ_MID_NOSE
		x  =  0.5*_len_N.doubleValue(SI.METRE);
		hf =  Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		_sectionsYZ.add(
				new FuselageCurvesSection(
						wf, hf, _sectionNoseMidLowerToTotalHeightRatio, _sectionMidNoseRhoUpper, _sectionMidNoseRhoLower, // lengths & parameters 
						_np_SecUp, _np_SecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		_sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// CYLINDER 1
		// IDX_SECTION_YZ_CYLINDER
		x  =  _len_N.doubleValue(SI.METRE);
		wf =  _sectionCylinderWidth.doubleValue(SI.METRE); 
		hf =  _sectionCylinderHeight.doubleValue(SI.METRE);
		_sectionsYZ.add(
				new FuselageCurvesSection(
						wf, hf, a, rhoUpper, rhoLower, // lengths & parameters 
						_np_SecUp, _np_SecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		_sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// CYLINDER 2
		// IDX_SECTION_YZ_CYLINDER
		x  =  _len_N.doubleValue(SI.METRE) + _len_C.doubleValue(SI.METRE);
		wf =  _sectionCylinderWidth.doubleValue(SI.METRE); 
		hf =  _sectionCylinderHeight.doubleValue(SI.METRE);
		_sectionsYZ.add(
				new FuselageCurvesSection(
						wf, hf, a, rhoUpper, rhoLower, // lengths & parameters 
						_np_SecUp, _np_SecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		_sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// MID-TAIL
		// IDX_SECTION_YZ_MID_TAIL
		x = _len_F.doubleValue(SI.METRE) - 0.5*_len_T.doubleValue(SI.METRE);
		hf =  Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		_sectionsYZ.add(
				new FuselageCurvesSection(
						wf, hf, _sectionTailMidLowerToTotalHeightRatio, _sectionMidTailRhoUpper, _sectionMidTailRhoLower, // lengths & parameters 
						_np_SecUp, _np_SecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		_sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// TAIL CAP
		// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
		x =  _len_F.doubleValue(SI.METRE) - _dxTailCap.doubleValue(SI.METRE);
		hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		_sectionsYZ.add(
				new FuselageCurvesSection(
						wf, hf, 0.5, 0.0, 0.0, // lengths & parameters 
						_np_SecUp, _np_SecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		_sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// TAIL TIP
		// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
		x =  _len_F.times(0.999995).doubleValue(SI.METRE);// - _dxTailCap.doubleValue(SI.METRE);
		hf =  Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		_sectionsYZ.add(
				new FuselageCurvesSection(
						wf, hf, 0.5, 0.0, 0.0, // lengths & parameters 
						_np_SecUp, _np_SecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		_sectionsYZStations.add(_len_F);

		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// Upper and lower coordinates of YZ sections

		for ( List<Double> l : _sectionUpperCurvesY) l.clear();
		_sectionUpperCurvesY.clear();
		for ( List<Double> l : _sectionUpperCurvesZ) l.clear();
		_sectionUpperCurvesZ.clear();
		for ( List<Double> l : _sectionLowerCurvesY) l.clear();
		_sectionLowerCurvesY.clear();
		for ( List<Double> l : _sectionLowerCurvesZ) l.clear();
		_sectionLowerCurvesZ.clear();

		//++++++++++++++
		// TO DO: Careful with repeated points
		for (int idx = 0; idx < NUM_SECTIONS_YZ; idx++)
		{
			List<Double> listDoubleYu = new ArrayList<Double>(); // a new array for each section
			List<Double> listDoubleZu = new ArrayList<Double>(); // a new array for each section
			for (int i=0; i < _sectionsYZ.get(idx).getSectionUpperRightPoints().size(); i++) {
				listDoubleYu.add( (double) _sectionsYZ.get(idx).getSectionUpperRightPoints().get(i).x); 
				listDoubleZu.add( (double) _sectionsYZ.get(idx).getSectionUpperRightPoints().get(i).y); 
			}
			for (int i=0; i < _sectionsYZ.get(idx).getSectionUpperLeftPoints().size(); i++) {
				listDoubleYu.add( (double) _sectionsYZ.get(idx).getSectionUpperLeftPoints().get(i).x); 
				listDoubleZu.add( (double) _sectionsYZ.get(idx).getSectionUpperLeftPoints().get(i).y); 
			}
			_sectionUpperCurvesY.add(listDoubleYu);
			_sectionUpperCurvesZ.add(listDoubleZu);

			//			System.out.println("_sectionUpperCurvesY:\n"+_sectionUpperCurvesY.get(idx));
			//			System.out.println("_sectionUpperCurvesZ:\n"+_sectionUpperCurvesZ.get(idx));

			List<Double> listDoubleYl = new ArrayList<Double>(); // a new array for each section
			List<Double> listDoubleZl = new ArrayList<Double>(); // a new array for each section
			for (int i=0; i < _sectionsYZ.get(idx).getSectionLowerLeftPoints().size(); i++) {
				listDoubleYl.add( (double) _sectionsYZ.get(idx).getSectionLowerLeftPoints().get(i).x); 
				listDoubleZl.add( (double) _sectionsYZ.get(idx).getSectionLowerLeftPoints().get(i).y); 
			}
			for (int i=0; i < _sectionsYZ.get(idx).getSectionLowerRightPoints().size(); i++) {
				listDoubleYl.add( (double) _sectionsYZ.get(idx).getSectionLowerRightPoints().get(i).x); 
				listDoubleZl.add( (double) _sectionsYZ.get(idx).getSectionLowerRightPoints().get(i).y); 
			}
			_sectionLowerCurvesY.add(listDoubleYl);
			_sectionLowerCurvesZ.add(listDoubleZl);

			//			System.out.println("_sectionLowerCurvesY:\n"+_sectionLowerCurvesY.get(idx));
			//			System.out.println("_sectionLowerCurvesZ:\n"+_sectionLowerCurvesZ.get(idx));

		}

		updateCurveSections();

		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

		//		System.out.println("Size _sectionLowerCurvesY: "+ _sectionLowerCurvesY.size());

		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// ADJUST SIDE CURVE Z-COORDINATES
		// Take Z-values from section shape scaled at x

		// see: adjustSectionShapeParameters

		getOutlineXYSideRCurveZ().clear();
		for (int i = 0; i < getOutlineXZUpperCurveX().size(); i++){
			double xs = getOutlineXZUpperCurveX().get(i);
			double zs = this.getZSide(xs);
			getOutlineXYSideRCurveZ().add(zs);
		}

		// LEFT CURVE (mirror)----------------------------------
		getOutlineXYSideLCurveX().clear();
		getOutlineXYSideLCurveY().clear();
		getOutlineXYSideLCurveZ().clear();
		for (int i = 0; i < getOutlineXYSideRCurveX().size(); i++){
			//	
			getOutlineXYSideLCurveX().add(  getOutlineXYSideRCurveX().get(i) ); // <== X
			getOutlineXYSideLCurveY().add( -getOutlineXYSideRCurveY().get(i) ); // <== -Y
			getOutlineXYSideLCurveZ().add(  getOutlineXYSideRCurveZ().get(i) ); // <== Z
		}
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	}// end-of calculateOutlines

	public void calculateOutlinesUpperLowerSectionYZ(int idx)
	{

		// initial checks
		if ( _sectionUpperCurvesY.size() == 0 ) return;
		if ( _sectionUpperCurvesY.size() != NUM_SECTIONS_YZ ) return;
		if ( idx < 0 ) return;
		if ( idx >= NUM_SECTIONS_YZ ) return;

		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// Upper and lower coordinates of selected (idx) YZ section

		//++++++++++++++
		// TO DO: Careful with repeated points

		// Upper curve
		_sectionUpperCurvesY.get(idx).clear();
		_sectionUpperCurvesZ.get(idx).clear();
		for (int i=0; i < _sectionsYZ.get(idx).getSectionUpperRightPoints().size(); i++) {
			_sectionUpperCurvesY.get(idx).add(new Double(_sectionsYZ.get(idx).getSectionUpperRightPoints().get(i).x));
			_sectionUpperCurvesZ.get(idx).add(new Double(_sectionsYZ.get(idx).getSectionUpperRightPoints().get(i).y));
		}
		for (int i=0; i < _sectionsYZ.get(idx).getSectionUpperLeftPoints().size(); i++) {
			_sectionUpperCurvesY.get(idx).add(new Double(_sectionsYZ.get(idx).getSectionUpperLeftPoints().get(i).x));
			_sectionUpperCurvesZ.get(idx).add(new Double(_sectionsYZ.get(idx).getSectionUpperLeftPoints().get(i).y));
		}

		// Lower curve
		_sectionLowerCurvesY.get(idx).clear();
		_sectionLowerCurvesZ.get(idx).clear();
		for (int i=0; i < _sectionsYZ.get(idx).getSectionLowerLeftPoints().size(); i++) {
			_sectionLowerCurvesY.get(idx).add(new Double(_sectionsYZ.get(idx).getSectionLowerLeftPoints().get(i).x));
			_sectionLowerCurvesZ.get(idx).add(new Double(_sectionsYZ.get(idx).getSectionLowerLeftPoints().get(i).y));
		}
		for (int i=0; i < _sectionsYZ.get(idx).getSectionLowerRightPoints().size(); i++) {
			_sectionLowerCurvesY.get(idx).add(new Double(_sectionsYZ.get(idx).getSectionLowerRightPoints().get(i).x));
			_sectionLowerCurvesZ.get(idx).add(new Double(_sectionsYZ.get(idx).getSectionLowerRightPoints().get(i).y));
		}

	}


	public void calculateOutlines( )
	{
		calculateOutlines(_np_N, _np_C, _np_T, _np_SecUp, _np_SecLow);

	}


	public double getZOutlineXZUpperAtX(double x) {
		// base vectors - upper
		// unique values
		double vxu[] = new double[getUniqueValuesXZUpperCurve().size()];
		double vzu[] = new double[getUniqueValuesXZUpperCurve().size()];
		for (int i = 0; i < vxu.length; i++)
		{
			vxu[i] = getUniqueValuesXZUpperCurve().get(i).x;
			vzu[i] = getUniqueValuesXZUpperCurve().get(i).z;			
		}
		// interpolation - lower
		UnivariateInterpolator interpolatorUpper = new LinearInterpolator(); // SplineInterpolator();
		UnivariateFunction myInterpolationFunctionUpper = 
				interpolatorUpper.interpolate(vxu, vzu);

		Double z_F_u = myInterpolationFunctionUpper.value(x);
		return z_F_u;
	}


	public Double getZOutlineXZLowerAtX(double x) {
		// base vectors - lower
		// unique values
		double vxl[] = new double[getUniqueValuesXZLowerCurve().size()];
		double vzl[] = new double[getUniqueValuesXZLowerCurve().size()];
		for (int i = 0; i < vxl.length; i++)
		{
			vxl[i] = getUniqueValuesXZLowerCurve().get(i).x;
			vzl[i] = getUniqueValuesXZLowerCurve().get(i).z;			
		}
		// Interpolation - lower
		UnivariateInterpolator interpolatorLower = new LinearInterpolator(); // SplineInterpolator();
		UnivariateFunction myInterpolationFunctionLower = 
				interpolatorLower.interpolate(vxl, vzl);
		// section z-coordinates at x
		Double z_F_l = myInterpolationFunctionLower.value(x); 
		return z_F_l;
	}


	public Double getYOutlineXYSideRAtX(double x) {
		// base vectors - side (right)
		// unique values
		double vxs[] = new double[getUniqueValuesXYSideRCurve().size()];
		double vys[] = new double[getUniqueValuesXYSideRCurve().size()];
		for (int i = 0; i < vxs.length; i++)
		{
			vxs[i] = getUniqueValuesXYSideRCurve().get(i).x;
			vys[i] = getUniqueValuesXYSideRCurve().get(i).y;			
		}
		// Interpolation - side (right)
		UnivariateInterpolator interpolatorSide = new LinearInterpolator(); // SplineInterpolator();
		UnivariateFunction myInterpolationFunctionSide = 
				interpolatorSide.interpolate(vxs, vys);

		Double y_F_r = myInterpolationFunctionSide.value(x);
		return y_F_r;
	}


	public Double getYOutlineXYSideLAtX(double x) {
		return -getYOutlineXYSideRAtX(x);
	}

	public double getCamberAngleAtX(double x) {
		if (x<=_len_N.getEstimatedValue()) return Math.atan(getCamberZAtX(x)/x); 
		if (x>=_len_C.getEstimatedValue()) return Math.atan(-getCamberZAtX(x)/x);
		return 0.;
	}


	/** Return Camber z-coordinate at x-coordinate */
	public Double getCamberZAtX(double x) {
		double zUp = getZOutlineXZUpperAtX(x);
		double zDown = getZOutlineXZLowerAtX(x);
		return zUp/2 + zDown/2;
	}

	/** Return equivalent diameter at x-coordinate */
	public Double getEquivalentDiameterAtX(double x) {

		double zUp = getZOutlineXZUpperAtX(x);
		double zDown = getZOutlineXZLowerAtX(x);
		double height = zUp - zDown;
		double width = 2*getYOutlineXYSideRAtX(x);
		return Math.sqrt(height*width);

	}


	/** Return equivalent diameter at x-coordinates (x is an array)
	 * 
	 * @author Lorenzo Attanasio
	 * @param x
	 * @return
	 */
	public Double[] getEquivalentDiameterAtX(double ... x) {

		Double[] diameter = new Double[x.length];

		for(int i=0; i < x.length ; i++){
			double zUp = getZOutlineXZUpperAtX(x[i]);
			double zDown = getZOutlineXZLowerAtX(x[i]);
			double height = zUp - zDown;
			double width = 2*getYOutlineXYSideRAtX(x[i]);
			diameter[i] = Math.sqrt(height*width);
		}

		return diameter;
	}


	/** Return equivalent diameter of entire fuselage */
	public Double calculateEquivalentDiameter(){

		// BEWARE: Gtmat library starts indexing arrays from 1! 
		// To workaround this problem use .data to extract a double[] array
		double[] x = MyArrayUtils.linspace(0., _len_F.getEstimatedValue()*(1-0.0001), 200);

		return MyMathUtils.arithmeticMean((getEquivalentDiameterAtX(x)));

	}


	//  Return width at x-coordinate
	public Double getWidthAtX(double x) {
		return 2*getYOutlineXYSideRAtX(x);
	}


	/**
	 * Calculate a fuselage section profile for a given coordinate x, 
	 * with interpolated values of section shape parameters
	 * @param x section X-coordinate
	 * @return a MyFuselageCurvesSection object
	 */
	public FuselageCurvesSection makeSection(double x){

		//		System.out.println("makeSection :: _sectionsYZ size: "+ _sectionsYZ.size() +" x: "+ x);

		if ( _sectionsYZ == null )
		{
			System.out.println("ERROR -- MyFuselageCurvesSection :: makeSection -- _sectionsYZ is null ");
			return null;			
		} 
		if ( _sectionsYZ.size() == 0 ) 		
		{
			System.out.println("ERROR -- MyFuselageCurvesSection :: makeSection -- _sectionsYZ.size() = 0 ");
			return null;			
		} 

		if ( _sectionsYZStations.size() != NUM_SECTIONS_YZ ) 		
		{
			System.out.println("ERROR -- MyFuselageCurvesSection :: makeSection -- _sectionsYZStations.size()="
					+ _sectionsYZStations.size() +" != NUM_SECTIONS_YZ="+ NUM_SECTIONS_YZ);
			return null;
		}

		//		System.out.println("makeSection :: _sectionsYZ size: "+ _sectionsYZ.size());

		// breakpoints
		double vxSec[] = new double[NUM_SECTIONS_YZ];
		vxSec[IDX_SECTION_YZ_NOSE_TIP   ] = _sectionsYZStations.get(IDX_SECTION_YZ_NOSE_TIP).doubleValue(SI.METRE);
		vxSec[IDX_SECTION_YZ_NOSE_CAP   ] = _sectionsYZStations.get(IDX_SECTION_YZ_NOSE_CAP).doubleValue(SI.METRE);
		vxSec[IDX_SECTION_YZ_MID_NOSE   ] = _sectionsYZStations.get(IDX_SECTION_YZ_MID_NOSE).doubleValue(SI.METRE);
		vxSec[IDX_SECTION_YZ_CYLINDER_1 ] = _sectionsYZStations.get(IDX_SECTION_YZ_CYLINDER_1).doubleValue(SI.METRE);
		vxSec[IDX_SECTION_YZ_CYLINDER_2 ] = _sectionsYZStations.get(IDX_SECTION_YZ_CYLINDER_2).doubleValue(SI.METRE);
		vxSec[IDX_SECTION_YZ_MID_TAIL   ] = _sectionsYZStations.get(IDX_SECTION_YZ_MID_TAIL).doubleValue(SI.METRE);
		vxSec[IDX_SECTION_YZ_TAIL_CAP   ] = _sectionsYZStations.get(IDX_SECTION_YZ_TAIL_CAP).doubleValue(SI.METRE);
		vxSec[IDX_SECTION_YZ_TAIL_TIP   ] = _sectionsYZStations.get(IDX_SECTION_YZ_TAIL_TIP).doubleValue(SI.METRE);

		// values of section parameters at breakpoints
		double vA[]    = new double[NUM_SECTIONS_YZ];
		double vRhoU[] = new double[NUM_SECTIONS_YZ];
		double vRhoL[] = new double[NUM_SECTIONS_YZ];

		for (int i = 0; i < NUM_SECTIONS_YZ; i++)
		{
			// parameter a, 0.5 -> ellipse/circle, 0.0 -> squeeze lower part, 1.0 -> squeeze upper part
			vA[i]    = _sectionsYZ.get(i).get_LowerToTotalHeightRatio();
			// parameter rho, 
			vRhoU[i] = _sectionsYZ.get(i).get_RhoUpper();
			vRhoL[i] = _sectionsYZ.get(i).get_RhoLower();
			//			System.out.println("x0: "+ vxSec[i] +" a0: "+ vA[i] +" rhoU0: "+ vRhoU[i]+" rhoL0: "+ vRhoL[i] );
		}
		// interpolation - lower
		UnivariateInterpolator interpolatorA = new LinearInterpolator(); // SplineInterpolator(); 
		UnivariateFunction myInterpolationFunctionA = interpolatorA.interpolate(vxSec, vA);
		UnivariateInterpolator interpolatorRhoU = new LinearInterpolator(); // SplineInterpolator(); 
		UnivariateFunction myInterpolationFunctionRhoU = interpolatorRhoU.interpolate(vxSec, vRhoU);
		UnivariateInterpolator interpolatorRhoL = new LinearInterpolator(); // SplineInterpolator(); 
		UnivariateFunction myInterpolationFunctionRhoL = interpolatorRhoL.interpolate(vxSec, vRhoL);

		double sectionLowerToTotalHeightRatio = 0.5;
		double sectionRhoUpper                = 0.0;
		double sectionRhoLower                = 0.0;
		// when interpolating manage the out of range exceptions 
		try {
			sectionLowerToTotalHeightRatio = myInterpolationFunctionA.value(x);
			sectionRhoUpper                = myInterpolationFunctionRhoU.value(x);
			sectionRhoLower                = myInterpolationFunctionRhoL.value(x);
		} catch (OutOfRangeException e) {
			// do repair
			if ( x <= e.getLo().doubleValue() )
			{
				sectionLowerToTotalHeightRatio = vA[0];
				sectionRhoUpper                = vRhoU[0];
				sectionRhoLower                = vRhoL[0];
			}
			if ( x >= e.getHi().doubleValue() )
			{
				int kLast = vxSec.length - 1;
				sectionLowerToTotalHeightRatio = vA[kLast];
				sectionRhoUpper                = vRhoU[kLast];
				sectionRhoLower                = vRhoL[kLast];
			}
		}		


		//		System.out.println("x: "+ x +
		//				" a(x): "+ sectionLowerToTotalHeightRatio +
		//				" rhoU(x): "+ sectionRhoUpper+
		//				" rhoL(x): "+ sectionRhoLower);

		// Sets of unique values of the x, y, z coordinates are generated

		// base vectors - upper
		// unique values
		double vxu[] = new double[getUniqueValuesXZUpperCurve().size()];
		double vzu[] = new double[getUniqueValuesXZUpperCurve().size()];
		for (int i = 0; i < vxu.length; i++)
		{
			vxu[i] = getUniqueValuesXZUpperCurve().get(i).x;
			vzu[i] = getUniqueValuesXZUpperCurve().get(i).z;			
		}
		// interpolation - lower
		UnivariateInterpolator interpolatorUpper = new LinearInterpolator(); // SplineInterpolator();
		UnivariateFunction myInterpolationFunctionUpper = 
				interpolatorUpper.interpolate(vxu, vzu);

		// base vectors - lower
		// unique values
		double vxl[] = new double[getUniqueValuesXZLowerCurve().size()];
		double vzl[] = new double[getUniqueValuesXZLowerCurve().size()];
		for (int i = 0; i < vxl.length; i++)
		{
			vxl[i] = getUniqueValuesXZLowerCurve().get(i).x;
			vzl[i] = getUniqueValuesXZLowerCurve().get(i).z;			
		}
		// Interpolation - lower
		UnivariateInterpolator interpolatorLower = new LinearInterpolator(); // SplineInterpolator();
		UnivariateFunction myInterpolationFunctionLower = 
				interpolatorLower.interpolate(vxl, vzl);
		// section z-coordinates at x
		double z_F_u = myInterpolationFunctionUpper.value(x); 
		double z_F_l = myInterpolationFunctionLower.value(x); 
		// section height at x
		double h_F = Math.abs(z_F_u - z_F_l);

		// base vectors - side (right)
		// unique values
		double vxs[] = new double[getUniqueValuesXYSideRCurve().size()];
		double vys[] = new double[getUniqueValuesXYSideRCurve().size()];
		for (int i = 0; i < vxs.length; i++)
		{
			vxs[i] = getUniqueValuesXYSideRCurve().get(i).x;
			vys[i] = getUniqueValuesXYSideRCurve().get(i).y;			
		}
		// Interpolation - side (right)
		UnivariateInterpolator interpolatorSide = new LinearInterpolator(); // SplineInterpolator();
		UnivariateFunction myInterpolationFunctionSide = 
				interpolatorSide.interpolate(vxs, vys);

		double w_F = 2.0*myInterpolationFunctionSide.value(x);

		FuselageCurvesSection fuselageCurvesSection = new FuselageCurvesSection(
				w_F, h_F, // lengths
				sectionLowerToTotalHeightRatio, sectionRhoUpper, sectionRhoLower, // current parameters 
				//				_sectionCylinderLowerToTotalHeightRatio, _sectionCylinderRhoUpper, _sectionCylinderRhoLower, // object parameters 
				_np_SecUp, _np_SecLow // no. points (nose, cylinder, tail)
				);

		// translation: x=0 --> dZ=h_N; x=l_N --> dZ=0, etc 
		double dZ = z_F_l + 0.5*h_F;
		return fuselageCurvesSection.translateZ(dZ);

	}

	private double getZSide(double x)
	{
		// Return the z-coordinate of the side curve at x
		// Note: the y-coordinate is known from the outline-side-R curve

		//		System.out.println("getZSide :: x ==> "+x);

		FuselageCurvesSection section = makeSection(x);

		if ( section == null ) {
			System.out.println("null makeSection");
			return 0.0;
		}

		int iLast = section.getSectionUpperLeftPoints().size() - 1;
		// Left Points when section is seen from X- to X+

		return section.getSectionUpperLeftPoints().get(iLast).y;
	}

	public void adjustSectionShapeParameters(int idx, Double a, Double rhoUpper, Double rhoLower) {

		//		getSectionUpperCurveY().clear();
		//		getSectionUpperCurveZ().clear();

		switch (idx) {

		case IDX_SECTION_YZ_NOSE_CAP: // a, rhoUpper, rhoLower NOT USED
			// NOSE CAP
			// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
			Double x  =  _dxNoseCap.doubleValue(SI.METRE);
			Double hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ) );
			Double wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ) );
			_sectionsYZ.get(idx).setSectionParameters(
					wf, hf, 0.5, 0.0, 0.0,         // lengths & parameters
					_np_SecUp, _np_SecLow          // num. points
					);
			break;

		case IDX_SECTION_YZ_MID_NOSE:

			// MID-NOSE
			// IDX_SECTION_YZ_MID_NOSE

			//System.out.println("+++ rhoUpper: "+ _sectionsYZ.get(idx).get_RhoUpper());

			x  = 0.5*_len_N.doubleValue(SI.METRE);
			hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ) );
			wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ) );
			_sectionsYZ.get(idx).setSectionParameters(
					wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
					_np_SecUp, _np_SecLow          // num. points
					);

			//System.out.println("--+ rhoUpper: "+ _sectionsYZ.get(idx).get_RhoUpper());

			break;

		case IDX_SECTION_YZ_CYLINDER_1:

			// CYLINDER 
			// IDX_SECTION_YZ_CYLINDER
			x  = _len_N.doubleValue(SI.METRE);
			wf = _sectionCylinderWidth.doubleValue(SI.METRE); 
			hf = _sectionCylinderHeight.doubleValue(SI.METRE);
			_sectionsYZ.get(idx).setSectionParameters(
					wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
					_np_SecUp, _np_SecLow          // num. points
					);
			break;

		case IDX_SECTION_YZ_CYLINDER_2:
			// CYLINDER 
			// IDX_SECTION_YZ_CYLINDER
			x  = _len_N.doubleValue(SI.METRE) + _len_C.doubleValue(SI.METRE);
			wf = _sectionCylinderWidth.doubleValue(SI.METRE); 
			hf = _sectionCylinderHeight.doubleValue(SI.METRE);
			_sectionsYZ.get(idx).setSectionParameters(
					wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
					_np_SecUp, _np_SecLow          // num. points
					);
			break;

		case IDX_SECTION_YZ_MID_TAIL:
			// MID-TAIL
			// IDX_SECTION_YZ_MID_TAIL
			x  = _len_F.doubleValue(SI.METRE) - 0.5*_len_T.doubleValue(SI.METRE);
			hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ) );
			wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ) );
			_sectionsYZ.get(idx).setSectionParameters(
					wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
					_np_SecUp, _np_SecLow          // num. points
					);
			break;

		case IDX_SECTION_YZ_TAIL_CAP: // a, rhoUpper, rhoLower NOT USED
			// TAIL CAP
			// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
			x  = _len_F.doubleValue(SI.METRE) - _dxTailCap.doubleValue(SI.METRE);
			hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ) );
			wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ) );
			_sectionsYZ.get(idx).setSectionParameters(
					wf, hf, 0.5, 0.0, 0.0,         // lengths & parameters
					_np_SecUp, _np_SecLow          // num. points
					);
			break;

		default:
			// do nothing
			break;
		}

		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// adjust Z-coordinates in side curves
		// Take Z-values from section shape scaled at x
		getOutlineXYSideRCurveZ().clear();
		for (int i = 0; i < getOutlineXZUpperCurveX().size(); i++){
			double x = getOutlineXZUpperCurveX().get(i);
			double z = this.getZSide(x);
			getOutlineXYSideRCurveZ().add(z);
		}

		// LEFT CURVE (mirror)----------------------------------
		getOutlineXYSideLCurveX().clear();
		getOutlineXYSideLCurveY().clear();
		getOutlineXYSideLCurveZ().clear();
		for (int i = 0; i < getOutlineXYSideRCurveX().size(); i++){
			//	
			getOutlineXYSideLCurveX().add(  getOutlineXYSideRCurveX().get(i) ); // <== X
			getOutlineXYSideLCurveY().add( -getOutlineXYSideRCurveY().get(i) ); // <== -Y
			getOutlineXYSideLCurveZ().add(  getOutlineXYSideRCurveZ().get(i) ); // <== Z
		}
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	}
	//	
	//----------------------------------------------------------------------
	// import values from file
	//----------------------------------------------------------------------
	public void importFromXMLFile(File xmlFile) {

		//  create a document builder using DocumentBuilderFactory class
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc = null;

		//Once we have a document builder object. We uses it to parse XML file and create a document object.
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(xmlFile.toString());

			//Once we have document object. We are ready to use XPath. Just create an xpath object using XPathFactory.

			// Create XPathFactory object
			XPathFactory xpathFactory = XPathFactory.newInstance();

			// Create XPath object
			XPath xpath = xpathFactory.newXPath();


			// READ FUSELAGE PARAMETERS

			// read l_F numeric value
			String s_value_l_F = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Parms/Fuse_Length/text()");
			// read l_F unit
			String s_unit_l_F = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Parms/Fuse_Length/@unit");

			Double value_l_F_METER = null;
			value_l_F_METER = convertFromTo(s_value_l_F,s_unit_l_F,"m");

			//			MyStaticWriteUtils.logToConsole(
			//					"The ADOpT | Fuse_Length | original value: " + s_value_l_F + "\n" +
			//							"The ADOpT | Fuse_Length | original unit: " +	s_unit_l_F + "\n" +
			//							"The ADOpT | Fuse_Length | value (m): " +	value_l_F_METER + "\n"
			//					);

			if ( value_l_F_METER != null ) {
				_len_F = Amount.valueOf(value_l_F_METER.doubleValue(), SI.METRE);
				AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
			}
			else {
				JPADStaticWriteUtils.logToConsole(
						"The ADOpT | Fuse_Length | WARNING: import l_F failed.\n"
						);
			}

			// read l_N numeric value
			String s_value_l_N = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Parms/Nose_Length/text()");
			// read l_N unit
			String s_unit_l_N = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Parms/Nose_Length/@unit");

			Double value_l_N_METER = null;
			value_l_N_METER = convertFromTo(s_value_l_N,s_unit_l_N,"m");

			//			MyStaticWriteUtils.logToConsole(
			//					"The ADOpT | Nose_Length | original value: " + s_value_l_N + "\n" +
			//							"The ADOpT | Nose_Length | original unit: " +	s_unit_l_N + "\n" +
			//							"The ADOpT | Nose_Length | value (m): " +	value_l_N_METER + "\n"
			//					);

			if ( value_l_N_METER != null ) {
				_len_N = Amount.valueOf(value_l_N_METER.doubleValue(), SI.METRE);
				AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
			}
			else {
				JPADStaticWriteUtils.logToConsole(
						"The ADOpT | Nose_Length | WARNING: import l_N failed.\n"
						);
			}

			// read l_C numeric value
			String s_value_l_C = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Parms/Cylindrical_Length/text()");
			// read l_F unit
			String s_unit_l_C = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Parms/Cylindrical_Length/@unit");

			Double value_l_C_METER = null;
			value_l_C_METER = convertFromTo(s_value_l_C,s_unit_l_C,"m");

			//			MyStaticWriteUtils.logToConsole(
			//					"The ADOpT | Cylindrical_Length | original value: " + s_value_l_C + "\n" +
			//							"The ADOpT | Cylindrical_Length | original unit: " +	s_unit_l_C + "\n" +
			//							"The ADOpT | Cylindrical_Length | value (m): " +	value_l_C_METER + "\n"
			//	);

			if ( value_l_C_METER != null ) {
				_len_C = Amount.valueOf(value_l_C_METER.doubleValue(), SI.METRE);
				AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
			}
			else {
				JPADStaticWriteUtils.logToConsole(
						"The ADOpT | Cylindrical_Length | WARNING: import l_C failed.\n"
						);
			}

			// read l_T numeric value
			String s_value_l_T = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Parms/TailCone_Length/text()");
			// read l_T unit
			String s_unit_l_T = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Parms/TailCone_Length/@unit");

			Double value_l_T_METER = null;
			value_l_T_METER = convertFromTo(s_value_l_T,s_unit_l_T,"m");

			//			MyStaticWriteUtils.logToConsole(
			//					"The ADOpT | TailCone_Length | original value: " + s_value_l_T + "\n" +
			//							"The ADOpT | TailCone_Length | original unit: " +	s_unit_l_T + "\n" +
			//							"The ADOpT | TailCone_Length | value (m): " +	value_l_T_METER + "\n"
			//					);

			if ( value_l_T_METER != null ) {
				_len_T = Amount.valueOf(value_l_T_METER.doubleValue(), SI.METRE);
				AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
			}
			else {
				JPADStaticWriteUtils.logToConsole(
						"The ADOpT | TailCone_Length | WARNING: import l_T failed.\n"
						);
			}

			// read diam_C numeric value
			String s_value_d_C = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Parms/Fuse_Cylinder_Heigth/text()");
			// read diam_C unit
			String s_unit_d_C = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Parms/Fuse_Cylinder_Heigth/@unit");

			Double value_d_C_METER = null;
			value_d_C_METER = convertFromTo(s_value_d_C,s_unit_d_C,"m");

			//			MyStaticWriteUtils.logToConsole(
			//					"The ADOpT | Fuse_Heigth | original value: " + s_value_d_C + "\n" +
			//							"The ADOpT | Fuse_Heigth | original unit: " +	s_unit_d_C + "\n" +
			//							"The ADOpT | Fuse_Heigth | value (m): " +	value_d_C_METER + "\n"
			//					);

			if ( value_d_C_METER != null ) {
				_sectionCylinderHeight = Amount.valueOf(value_d_C_METER.doubleValue(), SI.METRE);
				AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
			}
			else {
				JPADStaticWriteUtils.logToConsole(
						"The ADOpT | Fuse_Heigth | WARNING: import d_C failed.\n"
						);
			}

			// read h_N numeric value
			String s_value_h_N = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Parms/Fuse_Nose_Tip/text()");
			// read h_N  unit
			String s_unit_h_N = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Parms/Fuse_Nose_Tip/@unit");

			Double value_h_N_METER = null;
			value_h_N_METER = convertFromTo(s_value_h_N ,s_unit_h_N ,"m");

			//			MyStaticWriteUtils.logToConsole(
			//					"The ADOpT | Fuse_Nose_Tip | original value: " + s_value_h_N + "\n" +
			//					"The ADOpT | Fuse_Nose_Tip | original unit: " +	s_unit_h_N  + "\n" +
			//					"The ADOpT | Fuse_Nose_Tip | value (m): " +value_h_N_METER + "\n"
			//					);

			if ( value_h_N_METER != null ) {
				_height_N = Amount.valueOf(value_h_N_METER.doubleValue(), SI.METRE);
				AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
			}
			else {
				JPADStaticWriteUtils.logToConsole(
						"The ADOpT | Fuse_Nose_Tip | WARNING: import h_N failed.\n"
						);
			}

			// read h_T numeric value
			String s_value_h_T = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Parms/Fuse_Tail_Tip/text()");
			// read h_T unit
			String s_unit_h_T = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Parms/Fuse_Tail_Tip/@unit");

			Double value_h_T_METER = null;
			value_h_T_METER = convertFromTo(s_value_h_T ,s_unit_h_T ,"m");

			//			MyStaticWriteUtils.logToConsole(
			//					"The ADOpT | Fuse_Tail_Tip | original value: " + s_value_h_T + "\n" +
			//					"The ADOpT | Fuse_Tail_Tip | original unit: " +	s_unit_h_T  + "\n" +
			//					"The ADOpT | Fuse_Tail_Tip | value (m): " +value_h_T_METER + "\n"
			//					);

			if ( value_h_T_METER != null ) {
				_height_T = Amount.valueOf(value_h_T_METER.doubleValue(), SI.METRE);
				AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
			}
			else {
				JPADStaticWriteUtils.logToConsole(
						"The ADOpT | Fuse_Tail_Tip | WARNING: import h_T failed.\n"
						);
			}

			// READ FUSELAGE CROSS SECTION PARAMETERS

			// read width w_B numerical value

			String s_value_w_B = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Cylinder_Section/Fuse_Cylinder_Section_Width/text()");
			// read w_B  unit
			String s_unit_w_B = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Cylinder_Section/Fuse_Cylinder_Section_Width/@unit");

			Double value_w_B_METER = null;
			value_w_B_METER = convertFromTo(s_value_w_B,s_unit_w_B,"m");

			//			MyStaticWriteUtils.logToConsole(
			//					"The ADOpT     | Section_Width | original value: " + s_value_w_B+ "\n" +
			//							"The ADOpT | Section_Width | original unit: " +	s_unit_w_B + "\n" +
			//							"The ADOpT | Section_Width | value (m): " +	value_w_B_METER + "\n"
			//					);

			if ( value_w_B_METER != null ) {
				_sectionCylinderWidth = Amount.valueOf(value_w_B_METER.doubleValue(), SI.METRE);
				AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
			}
			else {
				JPADStaticWriteUtils.logToConsole(
						"The ADOpT | Section_Width  | WARNING: import w_B failed.\n"
						);
			}

			// read rho_upper numerical value

			String s_value_rho_upper = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Cylinder_Section/Fuse_Cylinder_Section_Rho_Upper/text()");
			// read w_B  unit
			String s_unit_rho_upper = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Cylinder_Section/Fuse_Cylinder_Section_Rho_Upper/@unit");

			Double value_rho_upper = null;
			value_rho_upper = convertFromTo(s_value_rho_upper,s_unit_rho_upper,"");

			//			MyStaticWriteUtils.logToConsole(
			//					"The ADOpT | Section_Rho_Upper | original value: " +s_value_rho_upper+ "\n" +
			//					"The ADOpT | Section_Rho_Upper| original unit: " +	s_unit_rho_upper+ "\n" +
			//					"The ADOpT | Section_Rho_Upper | value (m): " +value_rho_upper+ "\n"
			//					);

			if ( value_rho_upper != null ) {
				_sectionCylinderRhoUpper = new Double(value_rho_upper.doubleValue());
				AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
			}
			else {
				JPADStaticWriteUtils.logToConsole(
						"The ADOpT | Section_Rho_Upper  | WARNING: import rho_upper failed.\n"
						);
			}

			// read rho_upper numerical value

			String s_value_rho_lower = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Cylinder_Section/Fuse_Cylinder_Section_Rho_Lower/text()");
			// read w_B  unit
			String s_unit_rho_lower = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Cylinder_Section/Fuse_Cylinder_Section_Rho_Lower/@unit");

			Double value_rho_lower = null;
			value_rho_lower = convertFromTo(s_value_rho_lower,s_unit_rho_lower,"");

			//			MyStaticWriteUtils.logToConsole(
			//					"The ADOpT | Section_Rho_Lower | original value: " +s_value_rho_lower+ "\n" +
			//					"The ADOpT | Section_Rho_Lower | original unit: " +	s_unit_rho_lower+ "\n" +
			//					"The ADOpT | Section_Rho_Lower | value (m): " +value_rho_lower+ "\n"
			//					);

			if ( value_rho_lower  != null ) {
				_sectionCylinderRhoLower = new Double(value_rho_lower.doubleValue());
				AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
			}
			else {
				JPADStaticWriteUtils.logToConsole(
						"The ADOpT | Section_Rho_Lower | WARNING: import rho_lower failed.\n"
						);
			}

			// read a numerical value

			String s_value_a = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Cylinder_Section/Fuse_Cylinder_Lower_Section_a_Control_Point/text()");
			// read w_B  unit
			String s_unit_a = 
					getXMLPropertyByPath(doc, xpath, "//Fuse_Cylinder_Section/Fuse_Lower_Section_a_Control_Point/@unit");

			Double value_a = null;
			value_a = convertFromTo(s_value_a,s_unit_a,"");

			//			MyStaticWriteUtils.logToConsole(
			//					"The ADOpT | Lower_Section_a_Control_Point| original value: " +s_value_a + "\n" +
			//							"The ADOpT | Lower_Section_a_Control_Point | original unit: " +	s_unit_a + "\n" +
			//							"The ADOpT | Lower_Section_a_Control_Point | value (m): " +value_a + "\n"
			//					);

			if ( value_a  != null ) {
				_sectionCylinderLowerToTotalHeightRatio = new Double(value_a.doubleValue());
				AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
			}
			else {
				JPADStaticWriteUtils.logToConsole(
						"The ADOpT | Lower_Section_a_Control_Point  | WARNING: import a failed.\n"
						);
			} 


			// Check data consistency
			// assume that user is giving all lengths and simply reassign the total length

			Double value_l_F_METER_1 = 
					_len_N.doubleValue(SI.METRE) 
					+ _len_C.doubleValue(SI.METRE) 
					+ _len_T.doubleValue(SI.METRE);

			if ( Math.abs(value_l_F_METER_1 - value_l_F_METER) > 1e-06 ) {
				JPADStaticWriteUtils.logToConsole(
						"The ADOpT | Import Data | WARNING: Fuse_Length not consistent. Reassigned.\n"
								+ "The ADOpT | Import Data | (" + value_l_F_METER_1 + " != " + value_l_F_METER + ")\n"
						);
				_len_F = Amount.valueOf( value_l_F_METER_1, SI.METRE);
				AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
			}

			// recalculate dependent data
			calculateDependentData();

		} catch (ParserConfigurationException | SAXException | IOException ex0) {
			ex0.printStackTrace();
		}

	} // end-of-importFromXMLFile


	private void calculateDependentData() {
		_lambda_N =   
				_len_N.doubleValue(SI.METRE)
				/ _sectionCylinderHeight.doubleValue(SI.METRE) ; // _len_N / _diam_C;
		_lambda_C =  
				_len_C.doubleValue(SI.METRE)
				/ _sectionCylinderHeight.doubleValue(SI.METRE) ; // _len_C / _diam_C;
		_lambda_T =   
				_len_T.doubleValue(SI.METRE)
				/ _sectionCylinderHeight.doubleValue(SI.METRE) ; // _len_T / _diam_C;
		_lambda_F =
				_len_F.doubleValue(SI.METRE)
				/ _sectionCylinderHeight.doubleValue(SI.METRE) ; // _len_F / _diam_C;
		_lenRatio_NF =  
				_len_N.doubleValue(SI.METRE)
				/ _len_F.doubleValue(SI.METRE);
		_lenRatio_CF =   
				_len_C.doubleValue(SI.METRE)
				/ _len_F.doubleValue(SI.METRE);
		_lenRatio_TF =   
				_len_T.doubleValue(SI.METRE)
				/ _len_F.doubleValue(SI.METRE) ;
	}

	// Function adjustLength

	public void adjustLength(Amount<Length> len, FuselageAdjustCriteria criterion) {

		switch (criterion) {

		case ADJ_TOT_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS:
			_len_F = len;
			_len_N = Amount.valueOf(
					_lenRatio_NF * _len_F.doubleValue(SI.METRE), 
					SI.METRE); // _lenRatio_NF*_len_F;
			_len_C = Amount.valueOf( 
					_lenRatio_CF * _len_F.doubleValue(SI.METRE), 
					SI.METRE); // _lenRatio_CF*_len_F;
			_len_T = Amount.valueOf(
					_len_F.doubleValue(SI.METRE)
					-_len_N.doubleValue(SI.METRE)
					-_len_C.doubleValue(SI.METRE), 
					SI.METRE); // _len_F - _len_N - _len_C;
			_lambda_N =   
					_len_N.doubleValue(SI.METRE)
					/_sectionCylinderHeight.doubleValue(SI.METRE); // _len_N / _diam_C;
			_lambda_C =
					_len_C.doubleValue(SI.METRE)
					/_sectionCylinderHeight.doubleValue(SI.METRE); // _len_C / _diam_C;
			_lambda_T =
					_len_T.doubleValue(SI.METRE)
					/_sectionCylinderHeight.doubleValue(SI.METRE); // _len_T / _diam_C;
			_lambda_F =  
					_len_F.doubleValue(SI.METRE)
					/_sectionCylinderHeight.doubleValue(SI.METRE); // _len_F / _diam_C;
			break;

		case ADJ_TOT_LENGTH_CONST_FINENESS_RATIOS:
			_len_F = len;
			_sectionCylinderHeight = Amount.valueOf(
					_len_F.doubleValue(SI.METRE)
					/_lambda_F, 
					SI.METRE);
			_len_N = Amount.valueOf(
					_lambda_N * _sectionCylinderHeight.doubleValue(SI.METRE), 
					SI.METRE);
			_len_C = Amount.valueOf(
					_lambda_C * _sectionCylinderHeight.doubleValue(SI.METRE), 
					SI.METRE);;
					_len_T = Amount.valueOf(
							_lambda_T * _sectionCylinderHeight.doubleValue(SI.METRE), 
							SI.METRE);
					_lenRatio_NF =
							_len_N.doubleValue(SI.METRE)
							/ _len_F.doubleValue(SI.METRE);
					_lenRatio_CF =   
							_len_C.doubleValue(SI.METRE)
							/ _len_F.doubleValue(SI.METRE);
					_lenRatio_TF =  
							_len_T.doubleValue(SI.METRE)
							/ _len_F.doubleValue(SI.METRE);
					break;

		case ADJ_CYL_LENGTH:
			_len_C = len;
			_len_F = Amount.valueOf( 
					_len_N.doubleValue(SI.METRE)
					+ _len_C.doubleValue(SI.METRE) 
					+ _len_T.doubleValue(SI.METRE) , 
					SI.METRE);
			_lambda_C = 
					_len_C.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE); // _len_C / _diam_C;
			_lambda_F =  
					_len_F.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE); // _len_F / _diam_C;
			_lenRatio_NF =   
					_len_N.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE);
			_lenRatio_CF =  
					_len_C.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE);
			_lenRatio_TF = 
					_len_T.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE);
			break;

		case ADJ_NOSE_LENGTH_CONST_TOT_LENGTH_DIAMETERS:
			_len_N = len;
			_len_C = Amount.valueOf(  
					_len_F.doubleValue(SI.METRE)
					- _len_N.doubleValue(SI.METRE) 
					- _len_T.doubleValue(SI.METRE), 
					SI.METRE);
			_lambda_N =  
					_len_N.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE); // _len_N / _diam_C;
			_lambda_C = 
					_len_C.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE); // _len_C / _diam_C;
			_lenRatio_NF =  
					_len_N.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE);
			_lenRatio_CF =  
					_len_C.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE);
			break;

		case ADJ_NOSE_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS:
			_len_N = len;	
			_len_F = Amount.valueOf( 
					_len_N.doubleValue(SI.METRE)/_lenRatio_NF, 
					SI.METRE); // _len_N/_lenRatio_NF;
			_len_C = Amount.valueOf(
					_lenRatio_CF * _len_F.doubleValue(SI.METRE), 
					SI.METRE); // _lenRatio_CF*_len_F;
			_len_T = Amount.valueOf(
					_lenRatio_TF * _len_F.doubleValue(SI.METRE) , 
					SI.METRE); // _lenRatio_CF*_len_F;
			_lambda_N = 
					_len_N.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE); // _len_N / _diam_C;
			_lambda_C =  
					_len_C.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE); // _len_C / _diam_C;
			_lambda_T =  
					_len_T.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE); // _len_T / _diam_C;
			_lambda_F =  
					_len_F.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE) ; // _len_F / _diam_C;
			break;

		case  ADJ_NOSE_LENGTH_CONST_FINENESS_RATIOS_VAR_LENGTHS:
			_len_N = len;	
			_sectionCylinderHeight = Amount.valueOf(
					_len_N.doubleValue(SI.METRE)
					/ _lambda_N, 
					SI.METRE);
			_len_C = Amount.valueOf(
					_lambda_C * _sectionCylinderHeight.doubleValue(SI.METRE), 
					SI.METRE);
			_len_T = Amount.valueOf(
					_lambda_T * _sectionCylinderHeight.doubleValue(SI.METRE) , 
					SI.METRE);
			_len_F = Amount.valueOf(
					_len_N.doubleValue(SI.METRE)
					+ _len_C.doubleValue(SI.METRE) 
					+ _len_T.doubleValue(SI.METRE), 
					SI.METRE);
			_lenRatio_NF =   
					_len_N.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE) ;
			_lenRatio_CF =  
					_len_C.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE) ;
			_lenRatio_TF = 
					_len_T.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE);
			_lambda_F = 
					_len_F.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE); // _len_F / _diam_C;
			break;

		case ADJ_TAILCONE_LENGTH_CONST_TOT_LENGTH_DIAMETERS:
			_len_T = len;
			_lambda_T = 
					_len_T.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE) ; // _len_T / _diam_C;
			_len_C = Amount.valueOf( 
					_len_F.doubleValue(SI.METRE)
					- _len_N.doubleValue(SI.METRE) 
					- _len_T.doubleValue(SI.METRE) , 
					SI.METRE);
			_lambda_C = 
					_len_C.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE) ; // _len_C / _diam_C;
			_lenRatio_CF = 
					_len_C.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE);
			_lenRatio_TF =  
					_len_T.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE) ;
			break;

		case ADJ_TAILCONE_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS:
			_len_T = len;
			_len_F = Amount.valueOf( 
					_len_T.doubleValue(SI.METRE)/ _lenRatio_TF, 
					SI.METRE); // _len_N/_lenRatio_NF;
			_len_N = Amount.valueOf( 
					_lenRatio_NF * _len_F.doubleValue(SI.METRE) , 
					SI.METRE); // _lenRatio_NF*_len_F;
			_len_C = Amount.valueOf( 
					_lenRatio_CF * _len_F.doubleValue(SI.METRE) , 
					SI.METRE); // _lenRatio_CF*_len_F;
			_lambda_N =  
					_len_N.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE) ; // _len_N / _diam_C;
			_lambda_C =  
					_len_C.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE) ; // _len_C / _diam_C;
			_lambda_T =   
					_len_T.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE); // _len_T / _diam_C;
			_lambda_F = 
					_len_F.doubleValue(SI.METRE)
					/ _sectionCylinderHeight.doubleValue(SI.METRE); // _len_F / _diam_C;
			break;

		case ADJ_TAILCONE_LENGTH_CONST_FINENESS_RATIOS_VAR_LENGTHS:
			_len_T = len;
			_sectionCylinderHeight = Amount.valueOf(
					_len_T.doubleValue(SI.METRE)
					/ _lambda_T , 
					SI.METRE);
			_len_N = Amount.valueOf( 
					_lambda_N * _sectionCylinderHeight.doubleValue(SI.METRE) , 
					SI.METRE);
			_len_C = Amount.valueOf( 
					_lambda_C * _sectionCylinderHeight.doubleValue(SI.METRE) , 
					SI.METRE);
			_len_F = Amount.valueOf( 
					_lambda_F * _sectionCylinderHeight.doubleValue(SI.METRE) , 
					SI.METRE);
			_lenRatio_NF = 
					_len_N.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE);
			_lenRatio_CF =  
					_len_C.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE);
			_lenRatio_TF = 
					_len_T.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE) ;
			break;

		case ADJ_FUS_LENGTH_CONST_FINENESS_RATIOS_VAR_DIAMETERS:
			_sectionCylinderHeight= len;
			_len_N  =Amount.valueOf(
					_lambda_N * _sectionCylinderHeight.doubleValue(SI.METRE) , 
					SI.METRE);
			_len_C = Amount.valueOf(  
					_lambda_C * _sectionCylinderHeight.doubleValue(SI.METRE) , 
					SI.METRE);
			_len_T = Amount.valueOf(  
					_lambda_T * _sectionCylinderHeight.doubleValue(SI.METRE) , 
					SI.METRE);
			_len_F = Amount.valueOf( 
					_lambda_F * _sectionCylinderHeight.doubleValue(SI.METRE) , 
					SI.METRE);
			_lenRatio_NF =
					_len_N.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE);
			_lenRatio_CF =  
					_len_C.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE);
			_lenRatio_TF = 
					_len_T.doubleValue(SI.METRE)
					/ _len_F.doubleValue(SI.METRE);
			break;
		default:
			break;
		}

	}  // End  Event adjustLength


	///////////////////////////////////////////////////////////////////
	// Methods for evaluation of derived quantities (mass, cd...)
	///////////////////////////////////////////////////////////////////

	public void calculateStructure(OperatingConditions conditions,
			Aircraft configuration, 
			ACPerformanceManager performances,
			MethodEnum method) {

		_theOperatingConditions = conditions;
		_theAircraft = configuration;

	}

	public void calculateMass(Aircraft aircraft, 
			OperatingConditions conditions) {
		calculateMass(aircraft, conditions, MethodEnum.RAYMER);
		calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_1976);
		calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_2013);
		calculateMass(aircraft, conditions, MethodEnum.JENKINSON);
		calculateMass(aircraft, conditions, MethodEnum.KROO);
		calculateMass(aircraft, conditions, MethodEnum.SADRAY);
		calculateMass(aircraft, conditions, MethodEnum.NICOLAI_1984);
		calculateMass(aircraft, conditions, MethodEnum.ROSKAM);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void calculateMass(Aircraft aircraft, 
			OperatingConditions conditions, 
			MethodEnum method) {

		switch (method){

		/* 80 percent difference from true mass for some aircraft 
		 * */
		case JENKINSON : { // page 150 Jenkinson - Civil Jet Aircraft Design
			_methodsList.add(method);

			double k = 0.;

			if (_pressurized == true) {
				k = k + 0.08;
			}

			if (aircraft.get_theNacelles().get_nacellesList().get(0).get_mounting() == Nacelle.MountingPosition.FUSELAGE) {
				k = k + 0.04;
			}

			if (aircraft.get_landingGear().get_mounting() == LandingGear.MountingPosition.FUSELAGE) {
				k = k + 0.07;
			}

			_mass = Amount.valueOf(0.039*
					Math.pow((1 + k) * 
							2*_len_F.getEstimatedValue()*
							_equivalentDiameterCylinderGM.getEstimatedValue()*
							Math.pow(aircraft.get_performances().get_vDiveEAS().getEstimatedValue(),0.5),
							1.5), SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
//		 *//*
		case NICOLAI_1984 : {
			_methodsList.add(method);
			_mass = Amount.valueOf(
					0.0737*
					pow(2*_equivalentDiameterCylinderGM.getEstimatedValue()*
							pow(aircraft.get_performances().get_vDiveEAS().getEstimatedValue(), 0.338) * 
							pow(_len_F.getEstimatedValue(), 0.857)*
							pow(aircraft.get_weights().get_MTOM().getEstimatedValue()*
									aircraft.get_performances().get_nUltimate(), 0.286)
									, 1.1)
									, SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
//		  *//*
		case ROSKAM : { // page 92 Roskam page 92 (pdf) part V (Nicolai 2013 is the same)
			// TODO
			double Kinlet = 1.0;
			_methodsList.add(method);
			_mass = Amount.valueOf(2*10.43*
					pow(Kinlet, 1.42)*
					pow(
							aircraft.get_performances().get_maxDynamicPressure().to(MyUnits.LB_FT2).getEstimatedValue()/100,
							0.283)*
							pow(aircraft.get_weights().get_MTOM().to(NonSI.POUND).getEstimatedValue()/1000, 0.95)*
							pow(_len_F.divide(_sectionCylinderHeight).getEstimatedValue(), 0.71), 
							NonSI.POUND).to(SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), _mass.getUnit()));
		} break;
//		   */
		case RAYMER : { // page 403 Raymer - Aircraft Design a conceptual approach
			_mass = calculateMassRaymer(aircraft);
			_methodsList.add(method);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
		/* 18 % average difference from actual value
		 * */
		case SADRAY : { // page 585 Sadray Aircraft Design System Engineering Approach
			_methodsList.add(method);
			double Kinlet = 1.;
			double kRho = 0.0032;
			_mass = Amount.valueOf(_len_F.getEstimatedValue()*
					pow(_equivalentDiameterCylinderGM.getEstimatedValue(),2)*
					aircraft.get_weights().get_materialDensity().getEstimatedValue()*
					kRho*
					pow(aircraft.get_performances().get_nUltimate(),0.25)*
					Kinlet,
					SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
//		 */
		//		 The method gives poor results
		/*
		 * */
		case KROO : { // page 432 Stanford University pdf
			_methodsList.add(method);
			double Ifuse;
			double Ip = 1.5e-3 * 
					conditions.get_maxDeltaP().to(MyUnits.LB_FT2).getEstimatedValue()*
					get_sectionCylinderWidth().to(NonSI.FOOT).getEstimatedValue();

			double Ib = 1.91e-4 * aircraft.get_performances().get_nLimitZFW() * 
					(aircraft.get_weights().get_MZFM().to(NonSI.POUND).getEstimatedValue() - 
							aircraft.get_wing().get_mass().to(NonSI.POUND).getEstimatedValue()
							//					- aircraft.get_nacelle().get_mass().getEstimatedValue()*aircraft.get_propulsion().get_engineNumber()) TODO ADD!
		 * _len_F.minus(aircraft.get_wing().get_chordRoot().divide(2.)).to(NonSI.FOOT).getEstimatedValue()/
							pow(_sectionCylinderHeight.to(NonSI.FOOT).getEstimatedValue(),2));

			if (Ip > Ib) {
				Ifuse = Ip;
			} else {
				Ifuse = (Math.pow(Ip,2) + Math.pow(Ib,2))/(2*Ib); 
			}

			_mass = Amount.valueOf((1.051 + 0.102*Ifuse)*
					_sWet.to(MyUnits.FOOT2).getEstimatedValue(), NonSI.POUND).to(SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
//		 */
		case TORENBEEK_2013 : {
			_mass = calculateMassTorenbeek2013(aircraft.get_performances().get_nUltimate());
			_methodsList.add(method);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case TORENBEEK_1976 : { // page 302 Synthesis 1976
			_mass = calculateMassTorenbeek1976(aircraft);
			_methodsList.add(method);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		default : { } break;

		}

		if (_massCorrectionFactor != null) {
			_mass = _mass.times(_massCorrectionFactor);
		}

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_percentDifference =  new Double[_massMap.size()]; 

		_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_massReference, 
				_massMap,
				_percentDifference,
				100.).getFilteredMean(), SI.KILOGRAM);

		_mass = Amount.valueOf(_massEstimated.getEstimatedValue(), SI.KILOGRAM); 
	}

	private Amount<Mass> calculateMassRaymer(Aircraft aircraft) {
		double Kdoor = 1.0;
		double Klg = 1.12;
		double Kws = 0.75*
				((1+2*aircraft.get_wing().get_taperRatioEquivalent())/
						(1+aircraft.get_wing().get_taperRatioEquivalent()))*
						aircraft.get_wing().get_span().to(NonSI.FOOT).getEstimatedValue()*
						tan(aircraft.get_wing().get_sweepQuarterChordEq().to(SI.RADIAN).getEstimatedValue())/
						_len_F.to(NonSI.FOOT).getEstimatedValue();

		return Amount.valueOf(0.328*
				Kdoor*Klg*
				pow(aircraft.get_weights().
						get_MTOM().to(NonSI.POUND).times(aircraft.get_performances().
								get_nUltimate()).getEstimatedValue(),
								0.5)*
								pow(_len_F.to(NonSI.FOOT).getEstimatedValue(),0.25)*
								pow(_sWet.to(MyUnits.FOOT2).getEstimatedValue(), 0.302)*
								pow(1+Kws, 0.04)*
								pow(_len_F.to(NonSI.FOOT).
										divide(_equivalentDiameterCylinderGM.to(NonSI.FOOT)).getEstimatedValue(), 0.1), 
										NonSI.POUND).to(SI.KILOGRAM);
	}

	private Amount<Mass> calculateMassTorenbeek2013(double nUltimate) {
		return Amount.valueOf((60*
				pow(get_equivalentDiameterCylinderGM().getEstimatedValue(),2)*
				(_len_F.getEstimatedValue() + 1.5)+
				160*pow(nUltimate, 0.5)*
				get_equivalentDiameterCylinderGM().getEstimatedValue()*
				_len_F.getEstimatedValue()),
				SI.NEWTON).divide(AtmosphereCalc.g0).to(SI.KILOGRAM);

	}

	private Amount<Mass> calculateMassTorenbeek1976(Aircraft aircraft) {
		double k = 0.;
		if (_pressurized) {k = k + 0.08;}
		if (aircraft.get_landingGear().get_mounting() == LandingGear.MountingPosition.FUSELAGE){
			k = k + 0.07;
		}

		return Amount.valueOf((1 + k) * 0.23 * 
				Math.sqrt(
						aircraft.get_performances().get_vDiveEAS().getEstimatedValue() *
						aircraft.get_HTail().get_ACw_ACdistance().getEstimatedValue()/
						(2*_equivalentDiameterCylinderGM.getEstimatedValue())) *
						Math.pow(_sWet.getEstimatedValue(), 1.2),
						SI.KILOGRAM);
	}

	public void calculateCG(Aircraft aircraft, OperatingConditions conditions) {
		calculateCG(aircraft, conditions, MethodEnum.SFORZA);
		calculateCG(aircraft, conditions, MethodEnum.TORENBEEK_1982);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void calculateCG(
			Aircraft aircraft, 
			OperatingConditions conditions,
			MethodEnum method) {

		_cg.setLRForigin(_X0, _Y0, _Z0);
		_cg.set_xLRFref(_len_F.times(0.45));
		_cg.set_yLRFref(Amount.valueOf(0., SI.METER));
		_cg.set_zLRFref(Amount.valueOf(_Z0.getEstimatedValue(), SI.METER));

		// Initialize _methodsList again to clear it
		// from old entries
		_methodsList = new ArrayList<MethodEnum>();

		switch(method) {

		// page 359 Sforza (2014) - Aircraft Design
		case SFORZA : { 
			_methodsList.add(method);

			_xCG = Amount.valueOf(
					_len_F.divide(_lambda_F).getEstimatedValue()*
					(_lambda_N + (_lambda_F - 5.)/1.8)
					, SI.METER);
			_xCGMap.put(method, _xCG);
		} break;

		// page 313 Torenbeek (1982)
		case TORENBEEK_1982 : { 
			_methodsList.add(method);

			if (aircraft.get_powerPlant().get_engineNumber() == 1 && 
					(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.PISTON |
					aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP)) {

				_xCG = _len_F.times(0.335);
			}

			if (aircraft.get_powerPlant().get_position() == EngineMountingPositionEnum.WING) {
				if ((aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.PISTON |
						aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP)) {
					_xCG = _len_F.times(0.39); 
				} else {
					_xCG = _len_F.times(0.435);
				}
			}

			if (aircraft.get_powerPlant().get_position() == EngineMountingPositionEnum.REAR_FUSELAGE) {
				_xCG = _len_F.times(0.47);
			}

			if (aircraft.get_powerPlant().get_position() == EngineMountingPositionEnum.BURIED) {
				_xCG = _len_F.times(0.45);
			}

			_xCGMap.put(method, _xCG);
		} break;

		default : break;

		}

		_methodsMap.put(AnalysisTypeEnum.BALANCE, _methodsList);
		_percentDifferenceXCG = new Double[_xCGMap.size()];

		_cg.set_xLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.get_xLRFref(), 
				_xCGMap,
				_percentDifferenceXCG,
				30.).getFilteredMean(), SI.METER));

		_cg.calculateCGinBRF();

	}


	public void adjustCylinderSectionWidth(Amount<Length> w_B){

		_sectionCylinderWidth = w_B;

	}

	public void adjustCylinderSectionRhoUpper(Double rho_upper){

		_sectionCylinderRhoUpper = rho_upper;
	}

	public void adjustMidNoseSectionRhoUpper(Double rho_upper){

		_sectionMidNoseRhoUpper = rho_upper;
	}

	public void adjustMidTailSectionRhoUpper(Double rho_upper){

		_sectionMidTailRhoUpper = rho_upper;
	}


	public void adjustCylinderSectionRhoLower(Double rho_lower){

		_sectionCylinderRhoLower = rho_lower;
	}

	public void adjustMidNoseSectionRhoLower(Double rho_lower){

		_sectionMidNoseRhoLower = rho_lower;
	}

	public void adjustMidTailSectionRhoLower(Double rho_lower){

		_sectionMidTailRhoLower = rho_lower;
	}


	public void adjustCylinderSectionLowerToTotalHeightRatio(Double a){

		_sectionCylinderLowerToTotalHeightRatio=a;

	}

	public void adjustNoseMidSectionLowerToTotalHeightRatio(Double a){

		_sectionNoseMidLowerToTotalHeightRatio=a;

	}

	public void adjustTailMidSectionLowerToTotalHeightRatio(Double a){

		_sectionTailMidLowerToTotalHeightRatio=a;

	}


	public void clearOutlines( ) 
	{
		_outlineXZUpperCurveX.clear();
		_outlineXZUpperCurveZ.clear();
		_outlineXZLowerCurveX.clear();
		_outlineXZLowerCurveZ.clear();
		_outlineXZCamberLineX.clear();
		_outlineXZCamberLineZ.clear();
		_outlineXYSideRCurveX.clear();
		_outlineXYSideRCurveY.clear();
		_outlineXYSideRCurveZ.clear();
		_outlineXYSideLCurveX.clear();
		_outlineXYSideLCurveY.clear();
		_outlineXYSideLCurveZ.clear();
		_sectionUpperCurveY.clear();
		_sectionUpperCurveZ.clear();
		_sectionLowerCurveY.clear();
		_sectionLowerCurveZ.clear();
		//		_meshXZUpperCurveZ.clear(); 
		//		_meshXZLowerCurveZ.clear(); 
		//		_meshXZCurveX.clear();
		_sectionsYZ.clear();
		_sectionsYZStations.clear();
	}

	public List<Double> getOutlineXZUpperCurveX() {
		return _outlineXZUpperCurveX;
	}

	public void setOutlineXZUpperCurveX(List<Double> _outlineXZUpperCurveX) {
		this._outlineXZUpperCurveX = _outlineXZUpperCurveX;
	}

	public List<Double> getOutlineXZUpperCurveZ() {
		return _outlineXZUpperCurveZ;
	}

	public void setOutlineXZUpperCurveZ(List<Double> _outlineXZUpperCurveZ) {
		this._outlineXZUpperCurveZ = _outlineXZUpperCurveZ;
	}

	public List<Double> getOutlineXZLowerCurveX() {
		return _outlineXZLowerCurveX;
	}

	public void setOutlineXZLowerCurveX(List<Double> _outlineXZLowerCurveX) {
		this._outlineXZLowerCurveX = _outlineXZLowerCurveX;
	}

	public List<Double> getOutlineXZLowerCurveZ() {
		return _outlineXZLowerCurveZ;
	}

	public void setOutlineXZLowerCurveZ(List<Double> _outlineXZLowerCurveZ) {
		this._outlineXZLowerCurveZ = _outlineXZLowerCurveZ;
	}

	public List<Double> getSectionUpperCurveY() {
		return _sectionUpperCurveY;
	}

	public void setsectionUpperCurveY(List<Double> _sectionUpperCurveY) {
		this._sectionUpperCurveY = _sectionUpperCurveY;
	}

	public List<Double> getSectionUpperCurveZ() {
		return _sectionUpperCurveZ;
	}

	public void setsectionUpperCurveZ(List<Double> _sectionUpperCurveZ) {
		this._sectionUpperCurveZ = _sectionUpperCurveZ;
	}

	public List<Double> getSectionLowerCurveY() {
		return _sectionLowerCurveY;
	}

	public void setsectionLowerCurveY(List<Double> _sectionLowerCurveY) {
		this._sectionLowerCurveY = _sectionLowerCurveY;
	}

	public List<Double> getSectionLowerCurveZ() {
		return _sectionLowerCurveZ;
	}

	public void setsectionLowerCurveZ(List<Double> _sectionLowerCurveZ) {
		this._sectionLowerCurveZ = _sectionLowerCurveZ;
	}

	public List<Double> getSectionLowerCurvesY(int idx) {
		try {  
			return _sectionLowerCurvesY.get(idx);
		}
		catch( Exception ex ){  
			JPADStaticWriteUtils.logToConsole(
					"The ADOpT | ERROR:\n" + ex.toString() 
					);
			return new ArrayList<Double>();
		}
	}
	public List<Double> getSectionLowerCurvesZ(int idx) {
		try {  
			return _sectionLowerCurvesZ.get(idx);
		}
		catch( Exception ex ){  
			JPADStaticWriteUtils.logToConsole(
					"The ADOpT | ERROR:\n" + ex.toString() 
					);
			return new ArrayList<Double>();
		}
	}

	public List<Double> getSectionUpperCurvesY(int idx) {
		try {  
			return _sectionUpperCurvesY.get(idx);
		}
		catch( Exception ex ){  
			JPADStaticWriteUtils.logToConsole(
					"The ADOpT | ERROR:\n" + ex.toString() 
					);
			return new ArrayList<Double>();
		}
	}
	public List<Double> getSectionUpperCurvesZ(int idx) {
		try {  
			return _sectionUpperCurvesZ.get(idx);
		}
		catch( Exception ex ){  
			JPADStaticWriteUtils.logToConsole(
					"The ADOpT | ERROR:\n" + ex.toString() 
					);
			return new ArrayList<Double>();
		}
	}


	public List<Double> getOutlineXZCamberLineX() {
		return _outlineXZCamberLineX;
	}

	public void setOutlineXZCamberLineX(List<Double> _outlineXZCamberLineX) {
		this._outlineXZCamberLineX = _outlineXZCamberLineX;
	}

	public List<Double> getOutlineXZCamberLineZ() {
		return _outlineXZCamberLineZ;
	}

	public void setOutlineXZCamberLineZ(List<Double> _outlineXZCamberLineZ) {
		this._outlineXZCamberLineZ = _outlineXZCamberLineZ;
	}

	public List<Double> getOutlineXYSideRCurveX() {
		return _outlineXYSideRCurveX;
	}

	public void setOutlineXYSideRCurveX(List<Double> _outlineXYSideCurveX) {
		this._outlineXYSideRCurveX = _outlineXYSideCurveX;
	}

	public List<Double> getOutlineXYSideRCurveY() {
		return _outlineXYSideRCurveY;
	}

	public void setOutlineXYSideRCurveY(List<Double> _outlineXYSideCurveY) {
		this._outlineXYSideRCurveY = _outlineXYSideCurveY;
	}

	public List<Double> getOutlineXYSideRCurveZ() {
		return _outlineXYSideRCurveZ;
	}

	public void setOutlineXYSideRCurveZ(List<Double> _outlineXYSideCurveZ) {
		this._outlineXYSideRCurveZ = _outlineXYSideCurveZ;
	}

	public List<Double> getOutlineXYSideLCurveX() {
		return _outlineXYSideLCurveX;
	}

	public void setOutlineXYSideLCurveX(List<Double> _outlineXYSideCurveX) {
		this._outlineXYSideLCurveX = _outlineXYSideCurveX;
	}

	public List<Double> getOutlineXYSideLCurveY() {
		return _outlineXYSideLCurveY;
	}

	public void setOutlineXYSideLCurveY(List<Double> _outlineXYSideCurveY) {
		this._outlineXYSideLCurveY = _outlineXYSideCurveY;
	}

	public List<Double> getOutlineXYSideLCurveZ() {
		return _outlineXYSideLCurveZ;
	}

	public void setOutlineXYSideLCurveZ(List<Double> _outlineXYSideCurveZ) {
		this._outlineXYSideLCurveZ = _outlineXYSideCurveZ;
	}

	public Amount<Length> get_len_F() {
		return _len_F;
	}

	public void set_len_F(Amount<Length> len_F) {
		// check bounds
		if ( !(len_F.doubleValue(SI.METRE) < _len_F_MIN.doubleValue(SI.METRE)) 
				&& !(len_F.doubleValue(SI.METRE) > _len_F_MAX.doubleValue(SI.METRE)) ) {
			this._len_F = len_F;		
		}
	}

	public Amount<Length> get_len_F_MIN() {
		return _len_F_MIN;
	}

	public void set_len_F_MIN(Amount<Length> len_F_MIN) {
		this._len_F_MIN = len_F_MIN;
	}

	public Amount<Length> get_len_F_MAX() {
		return _len_F_MAX;
	}

	public void set_len_F_MAX(Amount<Length> len_F_MAX) {
		this._len_F_MAX = len_F_MAX;
	}

	public Amount<Length> get_len_N() {
		return _len_N;
	}

	public void set_len_N(Amount<Length> len_N) {
		// check bounds
		if ( !(len_N.doubleValue(SI.METRE) < _len_N_MIN.doubleValue(SI.METRE)) 
				&& !(len_N.doubleValue(SI.METRE) > _len_N_MAX.doubleValue(SI.METRE)) ) {
			this._len_N = len_N;

			Double value_l_F_METER_1 = 
					_len_N.doubleValue(SI.METRE) 
					+ _len_C.doubleValue(SI.METRE) 
					+ _len_T.doubleValue(SI.METRE);

			_len_F = Amount.valueOf(value_l_F_METER_1, SI.METRE);

			calculateDependentData(); // TO DO: check _dxNoseCap_MIN/MAX etc
		}
	}

	public Amount<Length> get_len_N_MIN() {
		return _len_N_MIN;
	}

	public void set_len_N_MIN(Amount<Length> len_N_MIN) {
		this._len_N_MIN = len_N_MIN;
	}

	public Amount<Length> get_len_N_MAX() {
		return _len_N_MAX;
	}

	public void set_len_N_MAX(Amount<Length> len_N_MAX) {
		this._len_N_MAX = len_N_MAX;
	}

	public Amount<Length> get_len_C() {
		return _len_C;
	}

	public void set_len_C(Amount<Length> len_C) {
		// check bounds
		if ( !(len_C.doubleValue(SI.METRE) < _len_C_MIN.doubleValue(SI.METRE)) 
				&& !(len_C.doubleValue(SI.METRE) > _len_C_MAX.doubleValue(SI.METRE)) ) {
			this._len_C = len_C;

			Double value_l_F_METER_1 = 
					_len_N.doubleValue(SI.METRE) 
					+ _len_C.doubleValue(SI.METRE) 
					+ _len_T.doubleValue(SI.METRE);

			_len_F = Amount.valueOf(value_l_F_METER_1, SI.METRE);

			calculateDependentData();
		}
	}

	public Amount<Length> get_len_C_MIN() {
		return _len_C_MIN;
	}

	public void set_len_C_MIN(Amount<Length> len_C_MIN) {
		this._len_C_MIN = len_C_MIN;
	}

	public Amount<Length> get_len_C_MAX() {
		return _len_C_MAX;
	}

	public void set_len_C_MAX(Amount<Length> len_C_MAX) {
		this._len_C_MAX = len_C_MAX;
	}


	public Amount<Length> get_len_T() {
		return _len_T;
	}

	public void set_len_T(Amount<Length> len_T) {
		// check bounds
		if ( !(len_T.doubleValue(SI.METRE) < _len_T_MIN.doubleValue(SI.METRE)) 
				&& !(len_T.doubleValue(SI.METRE) > _len_T_MAX.doubleValue(SI.METRE)) ) {
			this._len_T = len_T;			

			Double value_l_F_METER_1 = 
					_len_N.doubleValue(SI.METRE) 
					+ _len_C.doubleValue(SI.METRE) 
					+ _len_T.doubleValue(SI.METRE);

			_len_F = Amount.valueOf(value_l_F_METER_1, SI.METRE);

			calculateDependentData();
		}
	}

	public Amount<Length> get_len_T_MIN() {
		return _len_T_MIN;
	}

	public void set_len_T_MIN(Amount<Length> len_T_MIN) {
		this._len_C_MIN = len_T_MIN;
	}

	public Amount<Length> get_len_T_MAX() {
		return _len_T_MAX;
	}

	public void set_len_T_MAX(Amount<Length> len_T_MAX) {
		this._len_T_MAX = len_T_MAX;
	}

	public Amount<Length> get_len_N1() {
		return _len_N1;
	}

	public void set_len_N1(Amount<Length> _len_N1) {
		this._len_N1 = _len_N1;
	}

	public Amount<Length> get_len_N2() {
		return _len_N2;
	}

	public void set_len_N2(Amount<Length> _len_N2) {
		this._len_N2 = _len_N2;
	}

	public Amount<Length> get_len_N3() {
		return _len_N3;
	}

	public void set_len_N3(Amount<Length> _len_N3) {
		this._len_N3 = _len_N3;
	}


	public Double get_lambda_F() {
		// TO DO: check what should be adjusted
		return _lambda_F;
	}

	public void set_lambda_F(Double _lambda_F) {
		this._lambda_F = _lambda_F;
	}

	public Double get_lambda_F_MIN() {
		// TO DO: check what should be adjusted
		return _lambda_F_MIN;
	}

	public void set_lambda_F_MIN(Double _lambda_F_MIN) {
		this._lambda_F_MIN = _lambda_F_MIN;
	}	

	public Double get_lambda_F_MAX() {
		// TO DO: check what should be adjusted
		return _lambda_F_MAX;
	}

	public void set_lambda_F_MAX(Double _lambda_F_MAX) {
		this._lambda_F_MAX = _lambda_F_MAX;
	}	

	public Double get_lambda_N() {
		// TO DO: check what should be adjusted
		return _lambda_N;
	}

	public void set_lambda_N(Double _lambda_N) {
		this._lambda_N = _lambda_N;
	}	

	public Double get_lambda_N_MIN() {
		// TO DO: check what should be adjusted
		return _lambda_N_MIN;
	}

	public void set_lambda_N_MIN(Double _lambda_N_MIN) {
		this._lambda_N_MIN = _lambda_N_MIN;
	}	

	public Double get_lambda_N_MAX() {
		// TO DO: check what should be adjusted
		return _lambda_N_MAX;
	}

	public void set_lambda_N_MAX(Double _lambda_N_MAX) {
		this._lambda_N_MAX = _lambda_N_MAX;
	}	

	public Double get_lambda_C() {
		// TO DO: check what should be adjusted
		return _lambda_C;
	}

	public void set_lambda_C(Double _lambda_C) {
		this._lambda_C = _lambda_C;
	}

	public Double get_lambda_C_MIN() {
		// TO DO: check what should be adjusted
		return _lambda_C_MIN;
	}

	public void set_lambda_C_MIN(Double _lambda_C_MIN) {
		this._lambda_C_MIN = _lambda_C_MIN;
	}

	public Double get_lambda_C_MAX() {
		// TO DO: check what should be adjusted
		return _lambda_C_MAX;
	}

	public void set_lambda_C_MAX(Double _lambda_C_MAX) {
		this._lambda_C_MAX = _lambda_C_MAX;
	}

	public Double get_lambda_T() {
		// TO DO: check what should be adjusted
		return _lambda_T;
	}

	public void set_lambda_T(Double _lambda_T) {
		this._lambda_T = _lambda_T;
	}

	public Double get_lambda_T_MIN() {
		// TO DO: check what should be adjusted
		return _lambda_T_MIN;
	}

	public void set_lambda_T_MIN(Double _lambda_T_MIN) {
		this._lambda_T_MIN = _lambda_T_MIN;
	}

	public Double get_lambda_T_MAX() {
		// TO DO: check what should be adjusted
		return _lambda_T_MAX;
	}

	public void set_lambda_T_MAX(Double _lambda_T_MAX) {
		this._lambda_T_MAX = _lambda_T_MAX;
	}


	public Amount<Length> get_sectionCylinderHeight() {
		// TODO: check what should be adjusted
		return _sectionCylinderHeight;
	}

	public Amount<Area> get_area_C() {
		// TODO: check what should be adjusted
		return _area_C;
	}

	public void set_sectionCylinderHeight(Amount<Length> diam_C) {
		// check bounds
		if ( !(diam_C.doubleValue(SI.METRE) < _diam_C_MIN.doubleValue(SI.METRE)) 
				&& !(diam_C.doubleValue(SI.METRE) > diam_C.doubleValue(SI.METRE)) ) {
			this._sectionCylinderHeight = diam_C;			

			Double value_l_F_METER_1 = 
					_len_N.doubleValue(SI.METRE) 
					+ _len_C.doubleValue(SI.METRE) 
					+ _len_T.doubleValue(SI.METRE);

			_len_F = Amount.valueOf(value_l_F_METER_1 , SI.METRE);

			calculateDependentData();
		}
	}

	public Amount<Length> get_diam_C_MIN() {
		// TO DO: check what should be adjusted
		return _diam_C_MIN;
	}


	public void set_diam_C_MIN(Amount<Length> diam_C_MIN) {
		this._diam_C_MIN = diam_C_MIN;
	}
	public Amount<Length> get_diam_C_MAX() {
		// TO DO: check what should be adjusted
		return _diam_C_MAX;
	}


	public void set_diam_C_MAX(Amount<Length> diam_C_MAX) {
		this._diam_C_MAX = diam_C_MAX;
	}

	public Double get_lenRatio_NF() {
		// TO DO: check what should be adjusted
		return _lenRatio_NF;
	}

	public void set_lenRatio_NF(Double lenRatio_NF) {
		this._lenRatio_NF = lenRatio_NF;
	}

	public Double get_lenRatio_NF_MIN() {
		// TO DO: check what should be adjusted
		return _lenRatio_NF_MIN;
	}

	public void set_lenRatio_NF_MIN(Double lenRatio_NF_MIN) {
		this._lenRatio_NF_MIN = lenRatio_NF_MIN;
	}

	public Double get_lenRatio_NF_MAX() {
		// TO DO: check what should be adjusted
		return _lenRatio_NF_MAX;
	}

	public void set_lenRatio_NF_MAX(Double lenRatio_NF_MAX) {
		this._lenRatio_NF_MAX = lenRatio_NF_MAX;
	}

	public Double get_lenRatio_CF() {
		// TO DO: check what should be adjusted
		return _lenRatio_CF;
	}

	public void set_lenRatio_CF(Double lenRatio_CF) {
		this._lenRatio_CF = lenRatio_CF;
	}

	public Double get_lenRatio_CF_MIN() {
		// TO DO: check what should be adjusted
		return _lenRatio_CF_MIN;
	}

	public void set_lenRatio_CF_MIN(Double lenRatio_CF_MIN) {
		this._lenRatio_CF_MIN = lenRatio_CF_MIN;
	}

	public Double get_lenRatio_CF_MAX() {
		// TO DO: check what should be adjusted
		return _lenRatio_CF_MAX;
	}

	public void set_lenRatio_CF_MAX(Double lenRatio_CF_MAX) {
		this._lenRatio_CF_MAX = lenRatio_CF_MAX;
	}

	public Double get_lenRatio_TF() {
		// TO DO: check what should be adjusted
		return _lenRatio_TF;
	}

	public void set_lenRatio_TF(Double lenRatio_TF) {
		this._lenRatio_TF = lenRatio_TF;
	}

	public Double get_lenRatio_TF_MIN() {
		// TO DO: check what should be adjusted
		return _lenRatio_TF_MIN;
	}

	public void set_lenRatio_TF_MIN(Double lenRatio_TF_MIN) {
		this._lenRatio_TF_MIN = lenRatio_TF_MIN;
	}

	public Double get_lenRatio_TF_MAX() {
		// TO DO: check what should be adjusted
		return _lenRatio_TF_MAX;
	}

	public void set_lenRatio_TF_MAX(Double lenRatio_TF_MAX) {
		this._lenRatio_TF_MAX = lenRatio_TF_MAX;
	}


	public Amount<Angle> get_phi_1() {
		return _phi_1;
	}


	public void set_phi_1(Amount<Angle> _phi_1) {
		this._phi_1 = _phi_1;
	}


	public Amount<Angle> get_phi_2() {
		return _phi_2;
	}


	public void set_phi_2(Amount<Angle> _phi_2) {
		this._phi_2 = _phi_2;
	}


	public Amount<Angle> get_phi_3() {
		return _phi_3;
	}


	public void set_phi_3(Amount<Angle> _phi_3) {
		this._phi_3 = _phi_3;
	}	

	private String getXMLPropertyByPath(Document doc, XPath xpath, String expression) {

		try {

			XPathExpression expr =
					xpath.compile(expression);
			// evaluate expression result on XML document
			List<String> list_elements = new ArrayList<>();
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nodes.getLength(); i++)
				list_elements.add(nodes.item(i).getNodeValue());

			//			MyStaticWriteUtils.logToConsole(
			//					"The ADOpT | Imported " + expression + ": " +
			//					Arrays.toString(list_elements.toArray()) + "\n"
			//					);

			if ( !list_elements.isEmpty() ) {
				return list_elements.get(0);
			} else {
				return null;
			}

		} catch (XPathExpressionException ex1) {

			JPADStaticWriteUtils.logToConsole(
					"The ADOpT | WARNING: error occured while reading XML file.\n"
					);
			ex1.printStackTrace();
			return null; // ??
		}
	} // end-of-getXMLPropertyByPath:  

	private Double convertFromTo(String s_value, String s_unit_source, String s_unit_target, int precision) {
		Double result = null;

		switch (s_unit_target) {
		case "":
			result = new Double( s_value );
			break;
		case "NON_DIMENSIONAL":        	
			result = new Double( s_value );
			break;
		case "m":
			if (s_unit_source != null) {
				Unit<Length> meter = Unit.valueOf("m").asType(Length.class);
				Unit<Length> givenLengthUnit = Unit.valueOf(s_unit_source).asType(Length.class);
				UnitConverter converter = givenLengthUnit.getConverterTo(meter);
				result = converter.convert( new Double( s_value ) );
			}
			else {
				// no conversion
				result = new Double( s_value );
			}
			break;

		case "kg":
			break;
		case "m^2":
			break;
			// TO DO: add other units

		default:
			break;
		}
		return result;
	}// end-of-convertFromTo

	// overloaded function
	private Double convertFromTo(String s_value, String s_unit_source, String s_unit_target) {
		return convertFromTo(s_value, s_unit_source, s_unit_target, 3); 
	}	

	public FuselageAdjustCriteria getAdjustCriterion() {
		return _adjustCriterion;
	}

	public void setAdjustCriterion(FuselageAdjustCriteria _adjustCriterion) {
		this._adjustCriterion = _adjustCriterion;
	}

	public void set_sectionRhoUpper(Double _sectionRhoUpper) {
		this._sectionCylinderRhoUpper = _sectionRhoUpper;
	}

	public Double get_sectionRhoUpper_MIN() {
		return _sectionRhoUpper_MIN;
	}

	public void set_sectionRhoUpper_MIN(Double _sectionRhoUpper_MIN) {
		this._sectionRhoUpper_MIN = _sectionRhoUpper_MIN;
	}

	public Double get_sectionRhoUpper_MAX() {
		return _sectionRhoUpper_MAX;
	}

	public void set_sectionRhoUpper_MAX(Double _sectionRhoUpper_MAX) {
		this._sectionRhoUpper_MAX = _sectionRhoUpper_MAX;
	}

	public void set_sectionCylinderRhoLower(Double _sectionRhoLower) {
		this._sectionCylinderRhoLower = _sectionRhoLower;
	}

	public Double get_sectionCylinderRhoLower_MIN() {
		return _sectionRhoLower_MIN;
	}

	public void set_sectionRhoLower_MIN(Double _sectionRhoLower_MIN) {
		this._sectionRhoLower_MIN = _sectionRhoLower_MIN;
	}

	public Double get_sectionCylinderRhoLower_MAX() {
		return _sectionRhoLower_MAX;
	}

	public void set_sectionRhoLower_MAX(Double _sectionRhoLower_MAX) {
		this._sectionRhoLower_MAX = _sectionRhoLower_MAX;
	}

	public Double get_sectionLowerToTotalHeightRatio() {
		return _sectionCylinderLowerToTotalHeightRatio;
	}

	public void set_sectionLowerToTotalHeightRatio(Double _sectionLowerToTotalHeightRatio) {
		this._sectionCylinderLowerToTotalHeightRatio = _sectionLowerToTotalHeightRatio;
	}

	public Double get_sectionLowerToTotalHeightRatio_MIN() {
		return _sectionLowerToTotalHeightRatio_MIN;
	}

	public void set_sectionLowerToTotalHeightRatio_MIN(Double _sectionLowerToTotalHeightRatio_MIN) {
		this._sectionLowerToTotalHeightRatio_MIN = _sectionLowerToTotalHeightRatio_MIN;
	}

	public Double get_sectionLowerToTotalHeightRatio_MAX() {
		return _sectionLowerToTotalHeightRatio_MAX;
	}

	public void set_sectionLowerToTotalHeightRatio_MAX(Double _sectionLowerToTotalHeightRatio_MAX) {
		this._sectionLowerToTotalHeightRatio_MAX = _sectionLowerToTotalHeightRatio_MAX;
	}

	public Amount<Length> get_sectionWidth_MIN() {
		return _sectionWidth_MIN;
	}

	public void set_sectionWidth_MIN(Amount<Length> _width_Body_MIN) {
		this._sectionWidth_MIN = _width_Body_MIN;
	}

	public Amount<Length> get_sectionWidthMAX() {
		return _sectionWidth_MAX;
	}

	public void set_sectionWidth_Body_MAX(Amount<Length> _width_Body_MAX) {
		this._sectionWidth_MAX = _width_Body_MAX;
	}

	public Amount<Angle> get_phi_N() {
		return _phi_N;
	}

	public void set_phi_N(Amount<Angle> _phi_N) {
		this._phi_N = _phi_N;
	}

	public Amount<Angle> get_phi_T() {
		return _phi_T;
	}

	public void set_phi_T(Amount<Angle> _phi_T) {
		this._phi_T = _phi_T;
	}

	public Amount<Length> get_height_N() {
		return _height_N;
	}

	public void set_height_N(Amount<Length> _height_N) {
		this._height_N = _height_N;
	}

	public Amount<Length> get_height_T() {
		return _height_T;
	}

	public void set_height_T(Amount<Length> _height_T) {
		this._height_T = _height_T;
	}

	public Amount<Length> get_height_N_MIN() {
		return _height_N_MIN;
	}

	public void set_height_N_MIN(Amount<Length> _height_N_MIN) {
		this._height_N_MIN = _height_N_MIN;
	}

	public Amount<Length> get_height_N_MAX() {
		return _height_N_MAX;
	}

	public void set_height_N_MAX(Amount<Length> _height_N_MAX) {
		this._height_N_MAX = _height_N_MAX;
	}

	// NOTE: section points on SideR are ordered as follows: 
	//       first point is at Y=0 at the top of the section,
	//       successive points are taken going counter-clockwise when looking at
	//       YZ section from X- towards X+
	public List<PVector> getUniqueValuesYZSideRCurve(Amount<Length> len_x)
	{
		List<PVector> p  = new ArrayList<PVector>();

		FuselageCurvesSection curvesSection = makeSection(len_x.doubleValue(SI.METRE));

		for ( int i = 0; i < curvesSection.getSectionUpperLeftPoints().size() - 1; i++ )
		{
			p.add(
					new PVector(
							(float) len_x.doubleValue(SI.METRE),
							(float) curvesSection.getSectionUpperLeftPoints().get(i).x,
							(float) curvesSection.getSectionUpperLeftPoints().get(i).y
							)
					);
		}
		for ( int i = 0; i < curvesSection.getSectionLowerLeftPoints().size(); i++ )
		{
			p.add(
					new PVector(
							(float) len_x.doubleValue(SI.METRE),
							(float) curvesSection.getSectionLowerLeftPoints().get(i).x,
							(float) curvesSection.getSectionLowerLeftPoints().get(i).y
							)
					);
		}
		return p;
	}

	/** 
	 * Section points on SideL are ordered as follows: 
	 * first point is at Y=0 at the bottom of the section,
	 * successive points are taken going counter-clockwise when looking at
	 * YZ section from X- towards X+
	 * 
	 * @param len_x
	 * @return
	 */
	public List<PVector> getUniqueValuesYZSideLCurve(Amount<Length> len_x)
	{
		List<PVector> pts  = getUniqueValuesYZSideRCurve(len_x);
		// simply change all Y-coordinates
		for (PVector p : pts){ p.y = -p.y; }
		Collections.reverse(pts);
		return pts;
	}


	public List<PVector> getUniqueValuesXZUpperCurve()
	{
		List<PVector> p  = new ArrayList<PVector>();
		// add the first element
		if ( _outlineXZUpperCurveX.size() != 0 )
			p.add(
					new PVector(
							(float)_outlineXZUpperCurveX.get(0).doubleValue(),
							(float)0.0,
							(float)_outlineXZUpperCurveZ.get(0).doubleValue()
							)
					);

		for(int i = 1; i <= _outlineXZUpperCurveX.size()-1; i++)
		{
			if ( !_outlineXZUpperCurveX.get(i-1).equals( _outlineXZUpperCurveX.get(i) ) )
			{
				p.add(
						new PVector(
								(float)_outlineXZUpperCurveX.get(i).doubleValue(),
								(float)0.0,
								(float)_outlineXZUpperCurveZ.get(i).doubleValue()
								)
						);
			}
		}
		return p;
	}


	public List<PVector> getUniqueValuesXZLowerCurve()
	{
		List<PVector> p  = new ArrayList<PVector>();
		// add the first element
		if ( _outlineXZLowerCurveX.size() != 0 )
			p.add(
					new PVector(
							(float)_outlineXZLowerCurveX.get(0).doubleValue(),
							(float)0.0,
							(float)_outlineXZLowerCurveZ.get(0).doubleValue()
							)
					);
		for(int i = 1; i <= _outlineXZLowerCurveX.size()-1; i++)
		{
			if ( !_outlineXZLowerCurveX.get(i-1).equals( _outlineXZLowerCurveX.get(i) ) )
			{
				p.add(
						new PVector(
								(float)_outlineXZLowerCurveX.get(i).doubleValue(),
								(float)0.0,
								(float)_outlineXZLowerCurveZ.get(i).doubleValue()
								)
						);
			}
		}
		return p;
	}

	public List<PVector> getUniqueValuesXYSideRCurve()
	{
		List<PVector> p  = new ArrayList<PVector>();
		// add the first element
		if ( _outlineXYSideRCurveX.size() != 0 )
			p.add(
					new PVector(
							(float)_outlineXYSideRCurveX.get(0).doubleValue(),
							(float)_outlineXYSideRCurveY.get(0).doubleValue(),
							(float)0.0 // _outlineXYSideRCurveZ.get(0).doubleValue()
							)
					);
		for(int i = 1; i <= _outlineXYSideRCurveX.size()-1; i++)
		{
			if ( ! _outlineXYSideRCurveX.get(i-1).equals( _outlineXYSideRCurveX.get(i) ) )
			{
				p.add(
						new PVector(
								(float)_outlineXYSideRCurveX.get(i).doubleValue(),
								(float)_outlineXYSideRCurveY.get(i).doubleValue(),
								(float)0.0 // _outlineXYSideRCurveZ.get(i).doubleValue()
								)
						);
			}
		}
		return p;
	}

	public List<PVector> getUniqueValuesXYSideLCurve()
	{
		List<PVector> p  = new ArrayList<PVector>();
		// add the first element
		if ( _outlineXYSideLCurveX.size() != 0 )
			p.add(
					new PVector(
							(float)_outlineXYSideLCurveX.get(0).doubleValue(),
							(float)_outlineXYSideLCurveY.get(0).doubleValue(),
							(float)_outlineXYSideLCurveZ.get(0).doubleValue()
							)
					);
		for(int i = 1; i <= _outlineXYSideLCurveX.size()-1; i++)
		{
			if ( !_outlineXYSideLCurveX.get(i-1).equals( _outlineXYSideLCurveX.get(i) ) )
			{
				p.add(
						new PVector(
								(float)_outlineXYSideLCurveX.get(i).doubleValue(),
								(float)_outlineXYSideLCurveY.get(i).doubleValue(),
								(float)_outlineXYSideLCurveZ.get(i).doubleValue()
								)
						);
			}
		}
		return p;
	}
	
	/**
	 * This method computes the upsweep angle of the fuselage. To locate where the upsweep must be 
	 * calculated, a specific intersection point has been set. The height of the intersection point between 
	 * the horizontal line and the tangent to tail contour is equal to  0.26 of fuselage height (taken from 
	 * the bottom-line).
	 * 
	 * see Fuselage Aerodynamic Prediction Methods
	 * DOI: 10.2514/6.2015-2257
	 * 
	 * @author Vincenzo Cusati
	 */
	private void calculateUpsweepAngle() {
		
		// xcalculate point (x,z) from intersection of:
		// - horiz. line at 0.26 of fuselage height (d_C) - taken from the bottom-line
		// - lower profile of the tail sideview
		//
		// Using Java 8 features
		
		// x at l_N + l_C
		double x0 = _len_N.doubleValue(SI.METER) + _len_C.doubleValue(SI.METER);
//		System.out.println("l_N + l_C: " + x0 + " (m)");

		// values filtered as x >= l_N + l_C 
		List<Double> vX = new ArrayList<Double>(); 
		_outlineXZLowerCurveX.stream().filter(x -> x >= x0 ).distinct().forEach(vX::add);
//		vX.stream().forEach(e -> System.out.println(e));
		
		// index of first x in _outlineXZLowerCurveX >= x0
		int idxX0 = IntStream.range(0,_outlineXZLowerCurveX.size())
	            .reduce((i,j) -> _outlineXZLowerCurveX.get(i) > x0 ? i : j)
	            .getAsInt();  // or throw
		
		// the coupled z-values
		List<Double> vZ = new ArrayList<Double>();
		vZ = IntStream.range(0, _outlineXZLowerCurveZ.size()).filter(i -> i >= idxX0)
			 .mapToObj(i -> _outlineXZLowerCurveZ.get(i)).distinct()
	         .collect(Collectors.toList());
		
		// generate a vector of constant z = z_min + 0.26*d_C, same size of vZ, or vX
		Double z1 = vZ.get(0) + 0.26*_sectionCylinderHeight.doubleValue(SI.METER);
		List<Double> vZ1 = new ArrayList<Double>();
		vZ.stream().map(z -> z1).forEach(vZ1::add);
		
		Double xu = MyArrayUtils.intersectArraysBrent(
				ArrayUtils.toPrimitive(vX.toArray(new Double[vX.size()])), 
				ArrayUtils.toPrimitive(vZ.toArray(new Double[vZ.size()])), 
				ArrayUtils.toPrimitive(vZ1.toArray(new Double[vZ1.size()])), 
				vX.get(0), vX.get(vX.size()-1), 
				AllowedSolution.ANY_SIDE);
		
		// generate a vector of constant x = xu, same size of vZ, or vX
		List<Double> vX1 = new ArrayList<Double>();
		vX.stream().map(x -> xu).forEach(vX1::add);
		
		Double zu = MyArrayUtils.intersectArraysBrent(
				ArrayUtils.toPrimitive(vZ.toArray(new Double[vZ.size()])), 
				ArrayUtils.toPrimitive(vX.toArray(new Double[vX.size()])), 
				ArrayUtils.toPrimitive(vX1.toArray(new Double[vX1.size()])), 
				vZ.get(0), vZ.get(vZ.size()-1), 
				AllowedSolution.ANY_SIDE);
		
		// index of first x after xu
		int idxXu = IntStream.range(0,vX.size())
	            .reduce((i,j) -> vX.get(i)-xu > 0 ? i : j)
	            .getAsInt();  // or throw
		
		_upsweepAngle = Amount.valueOf(Math.atan((vZ.get(idxXu)-zu)/(vX.get(idxXu)-xu)), SI.RADIAN).to(NonSI.DEGREE_ANGLE); 
		
//		System.out.println("---");
//		System.out.println("Upsweep angle:" + _upsweepAngle.to(NonSI.DEGREE_ANGLE));
//		System.out.println("---");
	}
	
	/**
	 * This method computes the windshield angle of the fuselage. To locate where the windshield must be 
	 * calculated, a specific intersection point has been set. The height of the intersection point between 
	 * the horizontal line and the tangent to tail contour is equal to  0.75 of fuselage height (taken from 
	 * the bottom-line).
	 * 
	 * see Fuselage Aerodynamic Prediction Methods
	 * DOI: 10.2514/6.2015-2257
	 * 
	 * @author Vincenzo Cusati
	 */
	private void calculateWindshieldAngle() {
				// x at l_N 
				double xLNose = _len_N.doubleValue(SI.METER);
				
				// values filtered as x <= l_N 
				List<Double> vXNose = new ArrayList<Double>(); 
				_outlineXZUpperCurveX.stream().filter(x -> x <= xLNose ).distinct().forEach(vXNose::add);
				
				// index of last x in _outlineXZUpperCurveX >= xLNose
				int idxXNose = IntStream.range(0,_outlineXZUpperCurveX.size())
			            .reduce((i,j) -> _outlineXZUpperCurveX.get(i) > xLNose ? i : j)
			            .getAsInt();  // or throw
				
				// the coupled z-values
				// In this case is necessary filtered the strem with idxXNose-1 and not with idxXNose
				// because the values _outlineXZUpperCurveZ(idxXNose) and _outlineXZUpperCurveZ(idxXNose-1)
				// are different, in spite of they have the same value of _outlineXZUpperCurveX.
				List<Double> vZNose = new ArrayList<Double>();
				vZNose = IntStream.range(0, _outlineXZUpperCurveZ.size()).filter(i -> i <= idxXNose-1)
					 .mapToObj(i -> _outlineXZUpperCurveZ.get(i)).distinct()
			         .collect(Collectors.toList());
				
				
				// generate a vector of constant z = z_min + 0.75*d_C, same size of vZNose, or vXNose
				// It's better to take the value of z at 0.60*d_C (for the methodology)
//				Double z1Nose = _outlineXZLowerCurveZ.get(9) + 0.75*_sectionCylinderHeight.doubleValue(SI.METER);
				Double z1Nose = _outlineXZLowerCurveZ.get(9) + 0.60*_sectionCylinderHeight.doubleValue(SI.METER);
				List<Double> vZ1Nose = new ArrayList<Double>();
				vZNose.stream().map(z -> z1Nose).forEach(vZ1Nose::add);
				
				Double xw = MyArrayUtils.intersectArraysBrent(
						ArrayUtils.toPrimitive(vXNose.toArray(new Double[vXNose.size()])), 
						ArrayUtils.toPrimitive(vZNose.toArray(new Double[vZNose.size()])), 
						ArrayUtils.toPrimitive(vZ1Nose.toArray(new Double[vZ1Nose.size()])), 
						vXNose.get(0), vXNose.get(vXNose.size()-1), 
						AllowedSolution.ANY_SIDE);
				
				// generate a vector of constant x = xw, same size of vZNose, or vXNose
				List<Double> vX1Nose = new ArrayList<Double>();
				vXNose.stream().map(x -> xw).forEach(vX1Nose::add);
				
				Double zw = MyArrayUtils.intersectArraysBrent(
						ArrayUtils.toPrimitive(vZNose.toArray(new Double[vZNose.size()])), 
						ArrayUtils.toPrimitive(vXNose.toArray(new Double[vXNose.size()])), 
						ArrayUtils.toPrimitive(vX1Nose.toArray(new Double[vX1Nose.size()])), 
						vZNose.get(0), vZNose.get(vZNose.size()-1), 
						AllowedSolution.ANY_SIDE);
				
				// index of first x after xu
				int idxXw = IntStream.range(0,vXNose.size())
			            .reduce((i,j) -> vXNose.get(i)-xw > 0 ? i : j)
			            .getAsInt();  // or throw
				
				_windshieldAngle = Amount.valueOf(Math.atan((vZNose.get(idxXw)-zw)/(vXNose.get(idxXw)-xw)), SI.RADIAN).to(NonSI.DEGREE_ANGLE); 
				
//				System.out.println("---");
//				System.out.println("Windshield angle:" + _windshieldAngle.to(NonSI.DEGREE_ANGLE));
//				System.out.println("---");
	}
	
	public Amount<Length> get_dxNoseCap() {
		return _dxNoseCap;
	}

	public void set_dxNoseCap(Amount<Length> _dxNoseCap) {
		this._dxNoseCap = _dxNoseCap;
	}

	public Amount<Length> get_dxNoseCap_MIN() {
		return _dxNoseCap_MIN;
	}

	public void set_dxNoseCap_MIN(Amount<Length> _dxNoseCap_MIN) {
		this._dxNoseCap_MIN = _dxNoseCap_MIN;
	}

	public Amount<Length> get_dxNoseCap_MAX() {
		return _dxNoseCap_MAX;
	}

	public void set_dxNoseCap_MAX(Amount<Length> _dxNoseCap_MAX) {
		this._dxNoseCap_MAX = _dxNoseCap_MAX;
	}

	public Amount<Length> get_dxTailCap() {
		return _dxTailCap;
	}

	public void set_dxTailCap(Amount<Length> _dxTailCap) {
		this._dxTailCap = _dxTailCap;
	}

	public Amount<Length> get_dxTailCap_MIN() {
		return _dxTailCap_MIN;
	}

	public void set_dxTailCap_MIN(Amount<Length> _dxTailCap_MIN) {
		this._dxTailCap_MIN = _dxTailCap_MIN;
	}

	public Amount<Length> get_dxTailCap_MAX() {
		return _dxTailCap_MAX;
	}

	public void set_dxTailCap_MAX(Amount<Length> _dxTailCap_MAX) {
		this._dxTailCap_MAX = _dxTailCap_MAX;
	}

	public List<Amount<Length>> get_sectionsYZStations() {
		return _sectionsYZStations;
	}

	public List<FuselageCurvesSection> get_sectionsYZ() {
		return _sectionsYZ;
	}

	public Double get_sectionMidNoseRhoUpper() {
		return _sectionMidNoseRhoUpper;
	}

	public void set_sectionMidNoseRhoUpper(Double _sectionMidNoseRhoUpper) {
		this._sectionMidNoseRhoUpper = _sectionMidNoseRhoUpper;
	}

	public Double get_sectionMidNoseRhoUpper_MIN() {
		return _sectionMidNoseRhoUpper_MIN;
	}

	public void set_sectionMidNoseRhoUpper_MIN(
			Double _sectionMidNoseRhoUpper_MIN) {
		this._sectionMidNoseRhoUpper_MIN = _sectionMidNoseRhoUpper_MIN;
	}

	public Double get_sectionMidNoseRhoUpper_MAX() {
		return _sectionMidNoseRhoUpper_MAX;
	}

	public void set_sectionMidNoseRhoUpper_MAX(
			Double _sectionMidNoseRhoUpper_MAX) {
		this._sectionMidNoseRhoUpper_MAX = _sectionMidNoseRhoUpper_MAX;
	}

	public Double get_sectionMidNoseRhoLower() {
		return _sectionMidNoseRhoLower;
	}

	public void set_sectionMidNoseRhoLower(Double _sectionMidNoseRhoLower) {
		this._sectionMidNoseRhoLower = _sectionMidNoseRhoLower;
	}

	public Double get_sectionMidNoseRhoLower_MIN() {
		return _sectionMidNoseRhoLower_MIN;
	}

	public void set_sectionMidNoseRhoLower_MIN(
			Double _sectionMidNoseRhoLower_MIN) {
		this._sectionMidNoseRhoLower_MIN = _sectionMidNoseRhoLower_MIN;
	}

	public Double get_sectionMidNoseRhoLower_MAX() {
		return _sectionMidNoseRhoLower_MAX;
	}

	public void set_sectionMidNoseRhoLower_MAX(
			Double _sectionMidNoseRhoLower_MAX) {
		this._sectionMidNoseRhoLower_MAX = _sectionMidNoseRhoLower_MAX;
	}

	public Double get_sectionMidTailRhoLower() {
		return _sectionMidTailRhoLower;
	}

	public void set_sectionMidTailRhoLower(Double _sectionMidTailRhoLower) {
		this._sectionMidTailRhoLower = _sectionMidTailRhoLower;
	}

	public Double get_sectionMidTailRhoLower_MIN() {
		return _sectionMidTailRhoLower_MIN;
	}

	public void set_sectionMidTailRhoLower_MIN(
			Double _sectionMidTailRhoLower_MIN) {
		this._sectionMidTailRhoLower_MIN = _sectionMidTailRhoLower_MIN;
	}

	public Double get_sectionMidTailRhoLower_MAX() {
		return _sectionMidTailRhoLower_MAX;
	}

	public void set_sectionMidTailRhoLower_MAX(
			Double _sectionMidTailRhoLower_MAX) {
		this._sectionMidTailRhoLower_MAX = _sectionMidTailRhoLower_MAX;
	}

	public Double get_sectionMidTailRhoUpper() {
		return _sectionMidTailRhoUpper;
	}

	public void set_sectionMidTailRhoUpper(Double _sectionMidTailRhoUpper) {
		this._sectionMidTailRhoUpper = _sectionMidTailRhoUpper;
	}

	public Double get_sectionMidTailRhoUpper_MIN() {
		return _sectionMidTailRhoUpper_MIN;
	}

	public void set_sectionMidTailRhoUpper_MIN(
			Double _sectionMidTailRhoUpper_MIN) {
		this._sectionMidTailRhoUpper_MIN = _sectionMidTailRhoUpper_MIN;
	}

	public Double get_sectionMidTailRhoUpper_MAX() {
		return _sectionMidTailRhoUpper_MAX;
	}

	public void set_sectionMidTailRhoUpper_MAX(
			Double _sectionMidTailRhoUpper_MAX) {
		this._sectionMidTailRhoUpper_MAX = _sectionMidTailRhoUpper_MAX;
	}

	public Double get_sectionNoseMidLowerToTotalHeightRatio() {
		return _sectionNoseMidLowerToTotalHeightRatio;
	}

	public void set_sectionNoseMidLowerToTotalHeightRatio(
			Double _sectionNoseMidLowerToTotalHeightRatio) {
		this._sectionNoseMidLowerToTotalHeightRatio = _sectionNoseMidLowerToTotalHeightRatio;
	}

	public Double get_sectionNoseMidToTotalHeightRatio_MIN() {
		return _sectionNoseMidToTotalHeightRatio_MIN;
	}

	public void set_sectionNoseMidToTotalHeightRatio_MIN(
			Double _sectionNoseMidToTotalHeightRatio_MIN) {
		this._sectionNoseMidToTotalHeightRatio_MIN = _sectionNoseMidToTotalHeightRatio_MIN;
	}

	public Double get_sectionNoseMidToTotalHeightRatio_MAX() {
		return _sectionNoseMidToTotalHeightRatio_MAX;
	}

	public void set_sectionNoseMidToTotalHeightRatio_MAX(
			Double _sectionNoseMidToTotalHeightRatio_MAX) {
		this._sectionNoseMidToTotalHeightRatio_MAX = _sectionNoseMidToTotalHeightRatio_MAX;
	}

	public Double get_sectionTailMidLowerToTotalHeightRatio() {
		return _sectionTailMidLowerToTotalHeightRatio;
	}

	public void set_sectionTailMidLowerToTotalHeightRatio(
			Double _sectionTailMidLowerToTotalHeightRatio) {
		this._sectionTailMidLowerToTotalHeightRatio = _sectionTailMidLowerToTotalHeightRatio;
	}

	public Double get_sectionTailMidToTotalHeightRatio_MIN() {
		return _sectionTailMidToTotalHeightRatio_MIN;
	}

	public void set_sectionTailMidToTotalHeightRatio_MIN(
			Double _sectionTailMidToTotalHeightRatio_MIN) {
		this._sectionTailMidToTotalHeightRatio_MIN = _sectionTailMidToTotalHeightRatio_MIN;
	}

	public Double get_sectionTailMidToTotalHeightRatio_MAX() {
		return _sectionTailMidToTotalHeightRatio_MAX;
	}

	public void set_sectionTailMidToTotalHeightRatio_MAX(
			Double _sectionTailMidToTotalHeightRatio_MAX) {
		this._sectionTailMidToTotalHeightRatio_MAX = _sectionTailMidToTotalHeightRatio_MAX;
	}

	public Amount<Length> get_height_T_MIN() {
		return _height_T_MIN;
	}

	public void set_height_T_MIN(Amount<Length> _height_T_MIN) {
		this._height_T_MIN = _height_T_MIN;
	}

	public Amount<Length> get_height_T_MAX() {
		return _height_T_MAX;
	}

	public void set_height_T_MAX(Amount<Length> _height_T_MAX) {
		this._height_T_MAX = _height_T_MAX;
	}

	public double get_formFactor() {
		return _formFactor;
	}

	public void set_formFactor(double _formFactor) {
		this._formFactor = _formFactor;
	}

	public Amount<Length> get_roughness(){
		return _roughness;
	}

	public void set_roughness(Amount<Length> _roughness) {
		this._roughness = _roughness;
	}


	public Amount<Area> get_sWet() {
		return _sWet;
	}

	public void set_sWet(Amount<Area> _sWet) {
		this._sWet = _sWet;
	}

	public Amount<Area> get_sFront() {
		return _sFront;
	}

	public void set_sFront(Amount<Area> _sFront) {
		this._sFront = _sFront;
	}

	public FuselageAdjustCriteria get_adjustCriterion() {
		return _adjustCriterion;
	}

	public void set_adjustCriterion(FuselageAdjustCriteria _adjustCriterion) {
		this._adjustCriterion = _adjustCriterion;
	}

	public Amount<Length> get__height_T() {
		return _height_T;
	}

	public Amount<Length> get__diam_C() {
		return _sectionCylinderHeight;
	}

	public Amount<Length> get_sectionCylinderWidth() {
		return _sectionCylinderWidth;
	}

	public Double get_sectionCylinderRhoUpper() {
		return _sectionCylinderRhoUpper;
	}

	public Double get_sectionCylinderRhoLower() {
		return _sectionCylinderRhoLower;
	}

	public Double get_sectionCylinderLowerToTotalHeightRatio() {
		return _sectionCylinderLowerToTotalHeightRatio;
	}

	public Amount<Area> get_sWetC() {
		return _sWetC;
	}

	public void set_sWetC(Amount<Area> _sWetC) {
		this._sWetC = _sWetC;
	}

	public Amount<Area> get_sWetTail() {
		return _sWetTail;
	}

	public void set_sWetTail(Amount<Area> _sWetTail) {
		this._sWetTail = _sWetTail;
	}

	public Amount<Area> get_sWetNose() {
		return _sWetNose;
	}

	public void set_sWetNose(Amount<Area> _sWetNose) {
		this._sWetNose = _sWetNose;
	}

	public Amount<Length> get_equivalentDiameterCylinderGM() {
		return _equivalentDiameterCylinderGM;
	}

	public Amount<Length> get_equivalentDiameterGM() {
		return _equivalentDiameterGM;
	}

	public Amount<Area> get_windshieldArea() {
		return _windshieldArea;
	}

	public void set_windshieldArea(Amount<Area> _windshieldArea) {
		this._windshieldArea = _windshieldArea;
	}

	public Amount<Length> get_windshieldHeight() {
		return _windshieldHeight;
	}

	public void set_windshieldHeight(Amount<Length> _windshieldHeight) {
		this._windshieldHeight = _windshieldHeight;
	}

	public Amount<Length> get_windshieldWidth() {
		return _windshieldWidth;
	}

	public void set_windshieldWidth(Amount<Length> _windshieldWidth) {
		this._windshieldWidth = _windshieldWidth;
	}

	public String get_windshieldType() {
		return _windshieldType;
	}

	public void set_windshieldType(String _windshieldType) {
		this._windshieldType = _windshieldType;
	}

	public Amount<Length> get_heightFromGround() {
		return _heightFromGround;
	}

	public void set_heightGround(Amount<Length> _heightGround) {
		this._heightFromGround = _heightGround;
	}

	public Amount<Length> get_equivalentDiameterCylinderAM() {
		return _equivalentDiameterCylinderAM;
	}


	public String get_name() {
		return _name;
	}

	public void set_name(String n) {
		this._name = n;
	}

	public String get_description() {
		return _description;
	}


	public Amount<Mass> get_mass() {
		return _mass;
	}

	public void set_mass(Amount<Mass> _mass) {
		this._mass = _mass;
	}


	public Map<MethodEnum, Amount<Mass>> get_massMap() {
		return _massMap;
	}

	@Override
	public Map<AnalysisTypeEnum, List<MethodEnum>> get_methodsMap() {
		return _methodsMap;
	}

	private void updateCurveSections()
	{
		for (int k = 0; k < _sectionsYZ.size(); k++)
		{
			_sectionsYZ.get(k).set_x(_sectionsYZStations.get(k).doubleValue(SI.METER));
		}

	}

	public Double[] get_percentDifference() {
		return _percentDifference;
	}


	public void set_sectionCylinderRhoUpper(Double _sectionCylinderRhoUpper) {
		this._sectionCylinderRhoUpper = _sectionCylinderRhoUpper;
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


	public Amount<Mass> get_massReference() {
		return _massReference;
	}


	public void set_massReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}


	public Boolean is_pressurized() {
		return _pressurized;
	}


	public void set_pressurized(boolean _pressurized) {
		this._pressurized = _pressurized;
	}


	public Double get_massCorrectionFactor() {
		return _massCorrectionFactor;
	}


	public void set_massCorrectionFactor(Double _massCorrectionFactor) {
		this._massCorrectionFactor = _massCorrectionFactor;
	}


	public Amount<Mass> get_massEstimated() {
		return _massEstimated;
	}

	// ----------------------- Already corrected ----------------------------------------
	public int get_deckNumber() {
		return _fuselageCreator.getDeckNumber();
	}

	
	public void set_deckNumber(int deckNumber) {
		_fuselageCreator.setDeckNumber(deckNumber);
	}
	// ---------------------------------------------------------------


	public Amount<Length> get_xCGReference() {
		return _xCGReference;
	}
	

	public void set_xCGReference(Amount<Length> _xCGReference) {
		this._xCGReference = _xCGReference;
	}


	public Map<MethodEnum, Amount<Length>> get_xCGMap() {
		return _xCGMap;
	}


	public Amount<Length> get_xCGEstimated() {
		return _xCGEstimated;
	}


	public Double[] get_percentDifferenceXCG() {
		return _percentDifferenceXCG;
	}


	public Amount<Length> get_zCGEstimated() {
		return _zCGEstimated;
	}


	public CenterOfGravity get_cg() {
		return _cg;
	}


	public void set_cg(CenterOfGravity _cg) {
		this._cg = _cg;
	}

	@Override
	public ComponentEnum get_type() {
		return _type;
	}


	public FuselageAerodynamicsManager getAerodynamics() {
		return aerodynamics;
	}

	public void setAerodynamics(FuselageAerodynamicsManager aerodynamics) {
		this.aerodynamics = aerodynamics;
	}

	public static String getId() {
		return "0";
	}

	public Amount<Angle> get_upsweepAngle() {
		return _upsweepAngle;
	}

	public Amount<Angle> get_windshieldAngle() {
		return _windshieldAngle;
	}

	public void set_upsweepAngle(Amount<Angle> _upsweepAngle) {
		this._upsweepAngle = _upsweepAngle;
	}

	public void set_windshieldAngle(Amount<Angle> _windshieldAngle) {
		this._windshieldAngle = _windshieldAngle;
	}
	
	
	// --------------------------- Already done --------------------------------------

	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	public void setId(String _id) {
		this._id = _id;
	}

	public void setXApexConstructionAxes(Amount<Length> _xApexConstructionAxes) {
		this._xApexConstructionAxes = _xApexConstructionAxes;
	}

	public void setYApexConstructionAxes(Amount<Length> _yApexConstructionAxes) {
		this._yApexConstructionAxes = _yApexConstructionAxes;
	}

	public void setZApexConstructionAxes(Amount<Length> _zApexConstructionAxes) {
		this._zApexConstructionAxes = _zApexConstructionAxes;
	}

	public void setFuselageCreator(FuselageCreator _fuselageCreator) {
		this._fuselageCreator = _fuselageCreator;
	}
	// -----------------------------------------------------------------

} // end of class
