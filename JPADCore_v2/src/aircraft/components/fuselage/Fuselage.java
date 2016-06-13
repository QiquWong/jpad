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
import aircraft.components.liftingSurface.LiftingSurface;
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
import aircraft.components.fuselage.FuselageAerodynamicsManager;

public class Fuselage extends AeroComponent implements IFuselage {

	AerodynamicDatabaseReader _aerodynamicDatabaseReader;
	
	private String _name, _description;
	private Amount<Length> _X0, _Y0, _Z0;
	private ComponentEnum _type = ComponentEnum.FUSELAGE; 

	// Note: construction axes, 
	// X from FUSELAGE nose to tail,
	// Y from left wing to right wing,
	// Z from pilots feet to head 

//	List<FuselageCurvesSection> _sectionsYZ = new ArrayList<FuselageCurvesSection>();
//	public final int IDX_SECTION_YZ_NOSE_TIP   = 0;
//	public final int IDX_SECTION_YZ_NOSE_CAP   = 1;
//	public final int IDX_SECTION_YZ_MID_NOSE   = 2;
//	public final int IDX_SECTION_YZ_CYLINDER_1 = 3;
//	public final int IDX_SECTION_YZ_CYLINDER_2 = 4;
//	public final int IDX_SECTION_YZ_MID_TAIL   = 5;
//	public final int IDX_SECTION_YZ_TAIL_CAP   = 6;
//	public final int IDX_SECTION_YZ_TAIL_TIP   = 7;
//	public final int NUM_SECTIONS_YZ           = 8;
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
//	private int _np_N = 10, _np_C = 4, _np_T = 10, _np_SecUp = 10, _np_SecLow = 10;
//	private double _deltaXNose, _deltaXCylinder, _deltaXTail;
//	protected Object mouseClicked;

	private FuselageAdjustCriteria _adjustCriterion = FuselageAdjustCriteria.NONE;

	private OperatingConditions _theOperatingConditions;
	private Aircraft _theAircraft;
	private LiftingSurface _liftingSurface;
	
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

	//=========================================================================================================================================
	// BEGIN OF THE CONSTRUCTOR USING BUILDER PATTERN
	//=========================================================================================================================================
	// the creator object
	private static String _id = null;
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


	public FuselageAerodynamicsManager initializeAerodynamics(OperatingConditions ops, Aircraft aircraft) {
		_aerodynamicDatabaseReader = aircraft.get_theAerodynamics().get_aerodynamicDatabaseReader();
		aerodynamics = new FuselageAerodynamicsManager(ops, aircraft);
		return aerodynamics;
	}
	
	
	/**
	 *  Old import (from xml) method. It can be useful for ADOpT.
	 *  It is different from importFromXML method of FuselageCreator class.
	 */

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
				_fuselageCreator.setLenF(Amount.valueOf(value_l_F_METER.doubleValue(), SI.METRE));
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
				_fuselageCreator.setLenN(Amount.valueOf(value_l_N_METER.doubleValue(), SI.METRE));
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
				_fuselageCreator.setLenC(Amount.valueOf(value_l_C_METER.doubleValue(), SI.METRE));
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
				_fuselageCreator.setLenT(Amount.valueOf(value_l_T_METER.doubleValue(), SI.METRE));
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
				_fuselageCreator.setSectionCylinderHeight(Amount.valueOf(value_d_C_METER.doubleValue(), SI.METRE));
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
				_fuselageCreator.setHeightN(Amount.valueOf(value_h_N_METER.doubleValue(), SI.METRE));
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
				_fuselageCreator.setHeightT(Amount.valueOf(value_h_T_METER.doubleValue(), SI.METRE));
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
				_fuselageCreator.setSectionCylinderWidth(Amount.valueOf(value_w_B_METER.doubleValue(), SI.METRE));
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
				_fuselageCreator.setSectionCylinderRhoUpper(new Double(value_rho_upper.doubleValue()));
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
				_fuselageCreator.setSectionCylinderRhoLower(new Double(value_rho_lower.doubleValue()));
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
				_fuselageCreator.setSectionCylinderLowerToTotalHeightRatio(new Double(value_a.doubleValue()));
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
					_fuselageCreator.getLenN().doubleValue(SI.METRE) 
					+ _fuselageCreator.getLenC().doubleValue(SI.METRE) 
					+ _fuselageCreator.getLenT().doubleValue(SI.METRE);

			if ( Math.abs(value_l_F_METER_1 - value_l_F_METER) > 1e-06 ) {
				JPADStaticWriteUtils.logToConsole(
						"The ADOpT | Import Data | WARNING: Fuse_Length not consistent. Reassigned.\n"
								+ "The ADOpT | Import Data | (" + value_l_F_METER_1 + " != " + value_l_F_METER + ")\n"
						);
				_fuselageCreator.setLenF(Amount.valueOf( value_l_F_METER_1, SI.METRE));
				AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
			}

			// recalculate dependent data
			_fuselageCreator.calculateDependentData();

		} catch (ParserConfigurationException | SAXException | IOException ex0) {
			ex0.printStackTrace();
		}

	} // end-of-importFromXMLFile
	

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
							2*_fuselageCreator.getLenF().getEstimatedValue()*
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
							pow(_fuselageCreator.getLenF().getEstimatedValue(), 0.857)*
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
							pow(_fuselageCreator.getLenF().divide(_fuselageCreator.getSectionCylinderHeight()).getEstimatedValue(), 0.71), 
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
			_mass = Amount.valueOf(_fuselageCreator.getLenF().getEstimatedValue()*
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
//							_liftingSurface.get_wing().get_mass().to(NonSI.POUND).getEstimatedValue()
							aircraft.get_wing().get_mass().to(NonSI.POUND).getEstimatedValue()
//												- aircraft.get_nacelle().get_mass().getEstimatedValue()*aircraft.get_propulsion().get_engineNumber()) TODO ADD!
		 * _fuselageCreator.getLenF().minus(aircraft.get_wing().get_chordRoot().divide(2.)).to(NonSI.FOOT).getEstimatedValue()/
							pow(_fuselageCreator.getSectionCylinderHeight().to(NonSI.FOOT).getEstimatedValue(),2));

			if (Ip > Ib) {
				Ifuse = Ip;
			} else {
				Ifuse = (Math.pow(Ip,2) + Math.pow(Ib,2))/(2*Ib); 
			}

			_mass = Amount.valueOf((1.051 + 0.102*Ifuse)*
					_fuselageCreator.getsWet().to(MyUnits.FOOT2).getEstimatedValue(), NonSI.POUND).to(SI.KILOGRAM);
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
						_fuselageCreator.getLenF().to(NonSI.FOOT).getEstimatedValue();

		return Amount.valueOf(0.328*
				Kdoor*Klg*
				pow(aircraft.get_weights().
						get_MTOM().to(NonSI.POUND).times(aircraft.get_performances().
								get_nUltimate()).getEstimatedValue(),
								0.5)*
								pow(_fuselageCreator.getLenF().to(NonSI.FOOT).getEstimatedValue(),0.25)*
								pow(_fuselageCreator.getsWet().to(MyUnits.FOOT2).getEstimatedValue(), 0.302)*
								pow(1+Kws, 0.04)*
								pow(_fuselageCreator.getLenF().to(NonSI.FOOT).
										divide(_equivalentDiameterCylinderGM.to(NonSI.FOOT)).getEstimatedValue(), 0.1), 
										NonSI.POUND).to(SI.KILOGRAM);
	}

	private Amount<Mass> calculateMassTorenbeek2013(double nUltimate) {
		return Amount.valueOf((60*
				pow(get_equivalentDiameterCylinderGM().getEstimatedValue(),2)*
				(_fuselageCreator.getLenF().getEstimatedValue() + 1.5)+
				160*pow(nUltimate, 0.5)*
				get_equivalentDiameterCylinderGM().getEstimatedValue()*
				_fuselageCreator.getLenF().getEstimatedValue()),
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
						Math.pow(_fuselageCreator.getsWet().getEstimatedValue(), 1.2),
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
		_cg.set_xLRFref(_fuselageCreator.getLenF().times(0.45));
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
					_fuselageCreator.getLenF().divide(_fuselageCreator.getLambdaF()).getEstimatedValue()*
					(_lambda_N + (_fuselageCreator.getLambdaF() - 5.)/1.8)
					, SI.METER);
			_xCGMap.put(method, _xCG);
		} break;

		// page 313 Torenbeek (1982)
		case TORENBEEK_1982 : { 
			_methodsList.add(method);

			if (aircraft.get_powerPlant().get_engineNumber() == 1 && 
					(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.PISTON |
					aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP)) {

				_xCG = _fuselageCreator.getLenF().times(0.335);
			}

			if (aircraft.get_powerPlant().get_position() == EngineMountingPositionEnum.WING) {
				if ((aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.PISTON |
						aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP)) {
					_xCG = _fuselageCreator.getLenF().times(0.39); 
				} else {
					_xCG = _fuselageCreator.getLenF().times(0.435);
				}
			}

			if (aircraft.get_powerPlant().get_position() == EngineMountingPositionEnum.REAR_FUSELAGE) {
				_xCG = _fuselageCreator.getLenF().times(0.47);
			}

			if (aircraft.get_powerPlant().get_position() == EngineMountingPositionEnum.BURIED) {
				_xCG = _fuselageCreator.getLenF().times(0.45);
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

	
	// ----------------------- Already corrected ----------------------------------------
	
	public Amount<Mass> get_massReference() {
		return _fuselageCreator.getMassReference(); 
//		return _massReference;
	}


	public void set_massReference(Amount<Mass> _massReference) {
		 _fuselageCreator.setMassReference(_massReference);
//		this._massReference = _massReference;
	}


	public Boolean is_pressurized() {
		return _fuselageCreator.getPressurized();
//		return _pressurized;
	}


	public void set_pressurized(boolean _pressurized) {
		_fuselageCreator.setPressurized(_pressurized);
//		this._pressurized = _pressurized;
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
	public ComponentEnum getType() {
		return _type;
	}


	public FuselageAerodynamicsManager getAerodynamics() {
		return aerodynamics;
	}

	public void setAerodynamics(FuselageAerodynamicsManager aerodynamics) {
		this.aerodynamics = aerodynamics;
	}

	// TODO check this
//	public static String getId() {
//		return "0";
//	}
	
	public static String getId() {
		return _id;
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
