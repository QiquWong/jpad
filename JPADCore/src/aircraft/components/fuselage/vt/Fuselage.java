package aircraft.components.fuselage.vt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.solvers.AllowedSolution;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;

import aircraft.components.fuselage.MyFuselageCurvesSection;
import aircraft.components.fuselage.MyFuselageCurvesSideView;
import aircraft.components.fuselage.MyFuselageCurvesUpperView;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import processing.core.PVector;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyXMLReaderUtils;

public class Fuselage implements IFuselage {

	private String id;
	private int deckNumber;
	private Amount<Mass> massReference;

	private Boolean pressurized;

	// Fuselage overall length
	private Amount<Length> lenF;

	// Fuselage nose length
	private Amount<Length> lenN;

	private Amount<Length> lenC;

	private Amount<Length> lenT;

	private Amount<Length> sectionCylinderHeight;

	// GM = Geometric Mean, RMS = Root Mean Square, AM = Arithmetic Mean
	private Amount<Length> equivalentDiameterCylinderGM,
		equivalentDiameterGM,
		equivalentDiameterCylinderAM;

	//cylindrical section base area
	private Amount<Area> areaC;
	private Amount<Area> windshieldArea;
	private String windshieldType; // Possible types (Roskam part VI, page 134): Flat,protruding; Flat,flush; Single,round; Single,sharp; Double

	//Wetted area estimate
	private Amount<Area> sWetNose;
	private Amount<Area> sWetTail;
	private Amount<Area> sWetC;
	private Amount<Area> sFront; // CANNOT FIND FUSELAGE HEIGHT in MyAeroFuselage!!
	private Amount<Area> sWet;

	// Distance of fuselage lowest part from ground
	private Amount<Length> heightFromGround;

	//private Double phi1, phi2, phi3;
	private Amount<Angle> phi1, phi2, phi3;

	private Amount<Angle> phiN, phiT;
	private Amount<Length> heightN, heightT;

	private Amount<Angle> upsweepAngle, windshieldAngle;

	private Amount<Length> roughness;

	// Non-dimensional parameters
	private Double lambdaF;
	private Double lambdaN;
	private Double lambdaC;
	private Double lambdaT;
	private Double lenRatioNF;
	private Double lenRatioCF;
	private Double lenRatioTF;
	private Double formFactor;

	// Fuselage section parameters

	// Width and height
	private Amount<Length> sectionCylinderWidth;

	private Amount<Length> windshieldHeight, windshieldWidth;

	private Amount<Length> dxNoseCap, dxTailCap;

	// Non dimensional section parameters

	// how lower part is different from half diameter
	private Double
		sectionCylinderLowerToTotalHeightRatio,
		sectionNoseMidLowerToTotalHeightRatio,
		sectionTailMidLowerToTotalHeightRatio;


	// shape index, 1 --> close to a rectangle; 0 --> close to a circle
	private Double
		sectionCylinderRhoUpper,
		sectionCylinderRhoLower,
		sectionMidNoseRhoUpper,
		sectionMidNoseRhoLower,
		sectionMidTailRhoUpper,
		sectionMidTailRhoLower;

	// meshing stuff
	private int npN = 10, npC = 4, npT = 10, npSecUp = 10, npSecLow = 10;
	private double deltaXNose, deltaXCylinder, deltaXTail;

	private double dxNoseCapPercent;
	private double dxTailCapPercent;

	// Note: construction axes,
	// X from FUSELAGE nose to tail,
	// Y from left wing to right wing,
	// Z from pilots feet to head

	// view from left wing to right wing
	private List<Double> outlineXZUpperCurveX = new ArrayList<Double>();
	private List<Double> outlineXZUpperCurveZ = new ArrayList<Double>();

	// view from left wing to right wing
	private List<Double> outlineXZLowerCurveX = new ArrayList<Double>();
	private List<Double> outlineXZLowerCurveZ = new ArrayList<Double>();

	// view from left wing to right wing
	private List<Double> outlineXZCamberLineX = new ArrayList<Double>();
	private List<Double> outlineXZCamberLineZ = new ArrayList<Double>();

	// view from top, right part of body
	private List<Double> outlineXYSideRCurveX = new ArrayList<Double>();
	private List<Double> outlineXYSideRCurveY = new ArrayList<Double>();
	private List<Double> outlineXYSideRCurveZ = new ArrayList<Double>();
	// view from top, left part of body
	private List<Double> outlineXYSideLCurveX = new ArrayList<Double>();
	private List<Double> outlineXYSideLCurveY = new ArrayList<Double>();
	private List<Double> outlineXYSideLCurveZ = new ArrayList<Double>();

	// view section Upper curve (fuselage front view, looking from -X towards +X)
	private List<Double> sectionUpperCurveY = new ArrayList<Double>();
	private List<Double> sectionUpperCurveZ = new ArrayList<Double>();

	// view section Lower curve (fuselage front view, looking from -X towards +X)
	private List<Double> sectionLowerCurveY = new ArrayList<Double>();
	private List<Double> sectionLowerCurveZ = new ArrayList<Double>();

	private List<MyFuselageCurvesSection> sectionsYZ = new ArrayList<MyFuselageCurvesSection>();

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
	List<Amount<Length> > sectionsYZStations = new ArrayList<Amount<Length>>();

	List<List<Double>> sectionUpperCurvesY = new ArrayList<List<Double>>();
	List<List<Double>> sectionUpperCurvesZ = new ArrayList<List<Double>>();
	List<List<Double>> sectionLowerCurvesY = new ArrayList<List<Double>>();
	List<List<Double>> sectionLowerCurvesZ = new ArrayList<List<Double>>();

	// Constructor
//	public Fuselage(String id) {
//		this.id = id;
//	}
	// build via FuselageBuilder

	@Override
	public void calculateGeometry(int np_N, int np_C, int np_T, int np_SecUp, int np_SecLow) {
		npN           = np_N;
		npC           = np_C;
		npT           = np_T;
		Double l_F = lenF.doubleValue(SI.METRE);
		Double l_N = lenN.doubleValue(SI.METRE);
		Double l_C = lenC.doubleValue(SI.METRE);
		Double l_T = lenT.doubleValue(SI.METRE);
		deltaXNose     = l_N/(npN-1);
		deltaXCylinder = l_C/(npC-1);
		deltaXTail     = l_T/(npT-1);
		npSecUp  = np_SecUp;
		npSecLow = np_SecLow;

		// clean all points before recalculating
		clearOutlines();
//		calculateGeometry();
	}

	@Override
	public void calculateGeometry() {
		// --- OUTPUT DATA ------------------------------------------

		lenRatioTF   = 1.0 - lenRatioCF - lenRatioNF;

		lenN         = Amount.valueOf( lenRatioNF * lenF.doubleValue(SI.METRE), SI.METRE);
		lenC         = Amount.valueOf( lenRatioCF * lenF.doubleValue(SI.METRE), SI.METRE);
		lenT         = Amount.valueOf( lenRatioTF * lenF.doubleValue(SI.METRE), SI.METRE);
		lambdaC      = lenC.doubleValue(SI.METRE)/sectionCylinderHeight.doubleValue(SI.METRE); // _len_C / _diam_C;
		lambdaT      = lenT.doubleValue(SI.METRE)/sectionCylinderHeight.doubleValue(SI.METRE); // _len_T / _diam_C; // (_len_F - _len_N - _len_C) / _diam_C

		dxNoseCap = Amount.valueOf(lenN.times(dxNoseCapPercent).doubleValue(SI.METRE), SI.METRE);
		dxTailCap = Amount.valueOf(lenT.times(dxTailCapPercent).doubleValue(SI.METRE), SI.METRE);

		windshieldArea = Amount.valueOf(windshieldHeight.getEstimatedValue()*windshieldWidth.getEstimatedValue(), Area.UNIT);

		phiN         = Amount.valueOf(
				Math.atan(
						(sectionCylinderHeight.doubleValue(SI.METRE) - heightN.doubleValue(SI.METRE))
						/ lenN.doubleValue(SI.METRE)
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
				npN, // num. points Nose
				npC,  // num. points Cylinder
				npT, // num. points Tail
				npSecUp, // num. points Upper section
				npSecLow  // num. points Lower section
				);

		// Equivalent diameters
		equivalentDiameterCylinderGM = Amount.valueOf(
				Math.sqrt(sectionCylinderWidth.getEstimatedValue()*sectionCylinderHeight.getEstimatedValue())
				,SI.METRE);

		equivalentDiameterGM = Amount.valueOf(calculateEquivalentDiameter(), SI.METRE);

		equivalentDiameterCylinderAM = Amount.valueOf(
				MyMathUtils.arithmeticMean(
						sectionCylinderWidth.getEstimatedValue(),sectionCylinderHeight.getEstimatedValue())
						,SI.METRE);

		// Whole Fuselage fineness ratio
		lambdaF = lenF.getEstimatedValue()/equivalentDiameterCylinderGM.getEstimatedValue(); // _len_F/_diam_C;

		// cylindrical section base area
		areaC = Amount.valueOf(Math.PI *(sectionCylinderHeight.getEstimatedValue()*sectionCylinderWidth.getEstimatedValue())/4, Area.UNIT);

		calculateSwet("Stanford");

		// Form factor Kff
		formFactor =  calculateFormFactor(lambdaF);

		//		_reynolds = _theOperatingConditions.calculateRe(_len_F.getEstimatedValue(), _roughness.getEstimatedValue());

		calculateUpsweepAngle();
		calculateWindshieldAngle();

		// --- END OF OUTPUT DATA -----------------------------------------

	}

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

		Double l_F = lenF.doubleValue(SI.METRE);
		Double l_N = lenN.doubleValue(SI.METRE);
		Double l_C = lenC.doubleValue(SI.METRE);
		Double l_T = lenT.doubleValue(SI.METRE);
		Double d_C = sectionCylinderHeight.doubleValue(SI.METRE);
		Double h_N = heightN.doubleValue(SI.METRE); // Fuselage origin O_T at nose (>0, when below the cylindrical midline)
		Double h_T = heightT.doubleValue(SI.METRE);
		Double w_B = sectionCylinderWidth.doubleValue(SI.METRE);
		Double a   = sectionCylinderLowerToTotalHeightRatio.doubleValue();
		Double rhoUpper = sectionCylinderRhoUpper.doubleValue();
		Double rhoLower = sectionCylinderRhoLower.doubleValue();

		npN           = np_N;
		npC           = np_C;
		npT           = np_T;
		deltaXNose     = l_N/(npN-1);
		deltaXCylinder = l_C/(npC-1);
		deltaXTail     = l_T/(npT-1);
		npSecUp  = np_SecUp;
		npSecLow = np_SecLow;

		// clean all points before recalculating
		clearOutlines();

		//------------------------------------------------
		// XZ VIEW -- Side View
		//------------------------------------------------

		MyFuselageCurvesSideView fuselageCurvesSideView = new MyFuselageCurvesSideView(
				l_N, h_N, l_C, l_F, h_T, d_C/2, a, // lengths
				npN, npC, npT        // no. points (nose, cylinder, tail)
				);

		// UPPER CURVES ----------------------------------

		// UPPER NOSE
		for (int i = 0; i <= fuselageCurvesSideView.getNoseUpperPoints().size() - 1; i++){
			outlineXZUpperCurveX.add((double) fuselageCurvesSideView.getNoseUpperPoints().get(i).x);
			outlineXZUpperCurveZ.add((double) fuselageCurvesSideView.getNoseUpperPoints().get(i).y);
		}

		// UPPER CYLINDER
		for (int i = 0; i <= fuselageCurvesSideView.getCylinderUpperPoints().size() - 1; i++){
			outlineXZUpperCurveX.add((double) fuselageCurvesSideView.getCylinderUpperPoints().get(i).x);
			outlineXZUpperCurveZ.add((double) fuselageCurvesSideView.getCylinderUpperPoints().get(i).y);
		}

		// UPPER TAIL
		for (int i = 0; i <= fuselageCurvesSideView.getTailUpperPoints().size() - 1; i++){
			outlineXZUpperCurveX.add((double) fuselageCurvesSideView.getTailUpperPoints().get(i).x);
			outlineXZUpperCurveZ.add((double) fuselageCurvesSideView.getTailUpperPoints().get(i).y);
		}

		// LOWER CURVES ----------------------------------

		// LOWER NOSE
		for (int i = 0; i <= fuselageCurvesSideView.getNoseLowerPoints().size() - 1; i++){
			outlineXZLowerCurveX.add((double) fuselageCurvesSideView.getNoseLowerPoints().get(i).x);
			outlineXZLowerCurveZ.add((double) fuselageCurvesSideView.getNoseLowerPoints().get(i).y);
		}

		// LOWER CYLINDER
		for (int i = 0; i<= fuselageCurvesSideView.getCylinderLowerPoints().size() - 1; i++){
			outlineXZLowerCurveX.add((double) fuselageCurvesSideView.getCylinderLowerPoints().get(i).x);
			outlineXZLowerCurveZ.add((double) fuselageCurvesSideView.getCylinderLowerPoints().get(i).y);
		}

		// LOWER TAIL
		for (int i = 0; i <= fuselageCurvesSideView.getTailLowerPoints().size() - 1; i++)
		{
			outlineXZLowerCurveX.add((double) fuselageCurvesSideView.getTailLowerPoints().get(i).x);
			outlineXZLowerCurveZ.add((double) fuselageCurvesSideView.getTailLowerPoints().get(i).y);
		}

		//  NOSE CAMBER LINE
		for (int i = 0; i <= fuselageCurvesSideView.getNoseCamberlinePoints().size() - 1; i++){
			outlineXZCamberLineX.add((double) fuselageCurvesSideView.getNoseCamberlinePoints().get(i).x);
			outlineXZCamberLineZ.add((double) fuselageCurvesSideView.getNoseCamberlinePoints().get(i).y);
		}

		//  CYLINDER CAMBER LINE
		for (int i = 0; i <= fuselageCurvesSideView.getCylinderCamberlinePoints().size() - 1; i++){
			outlineXZCamberLineX.add((double) fuselageCurvesSideView.getCylinderCamberlinePoints().get(i).x);
			outlineXZCamberLineZ.add((double) fuselageCurvesSideView.getCylinderCamberlinePoints().get(i).y);
		}

		//  TAIL CAMBER LINE
		for (int i = 0; i <= fuselageCurvesSideView.getTailCamberlinePoints().size() - 1; i++){
			outlineXZCamberLineX.add((double) fuselageCurvesSideView.getTailCamberlinePoints().get(i).x);
			outlineXZCamberLineZ.add((double) fuselageCurvesSideView.getTailCamberlinePoints().get(i).y);
		}

		//------------------------------------------------
		// XY VIEW -- Upper View
		//------------------------------------------------
		MyFuselageCurvesUpperView fuselageCurvesUpperView = new MyFuselageCurvesUpperView(
				l_N, l_C, l_F, w_B/2, // lengths
				npN, npC, npT   // no. points (nose, cylinder, tail)
				);

		// RIGHT CURVE -----------------------------------

		// RIGHT NOSE
		for (int i=0; i<=fuselageCurvesUpperView.getNoseUpperPoints().size()-1; i++){
			outlineXYSideRCurveX.add((double) fuselageCurvesUpperView.getNoseUpperPoints().get(i).x);
			outlineXYSideRCurveY.add((double) fuselageCurvesUpperView.getNoseUpperPoints().get(i).y);
		}

		// RIGHT CYLINDER
		for (int i=0; i<=fuselageCurvesUpperView.getCylinderUpperPoints().size()-1; i++){
			outlineXYSideRCurveX.add((double) fuselageCurvesUpperView.getCylinderUpperPoints().get(i).x);
			outlineXYSideRCurveY.add((double) fuselageCurvesUpperView.getCylinderUpperPoints().get(i).y);
		}

		// RIGHT TAIL
		for (int i=0; i<=fuselageCurvesUpperView.getTailUpperPoints().size()-1; i++){
			outlineXYSideRCurveX.add((double) fuselageCurvesUpperView.getTailUpperPoints().get(i).x);
			outlineXYSideRCurveY.add((double) fuselageCurvesUpperView.getTailUpperPoints().get(i).y);
		}

		//		//------------------------------------------------
		//		// YZ VIEW -- Section/Front view
		//		//------------------------------------------------
		//
		MyFuselageCurvesSection fuselageCurvesSection = new MyFuselageCurvesSection(
				w_B, d_C, a, rhoUpper, rhoLower, // lengths
				npSecUp, npSecLow            // no. points (nose, cylinder, tail)
				);

		// UPPER CURVE -----------------------------------
		// counter-clockwise
		for (int i = 0; i <= fuselageCurvesSection.getSectionUpperRightPoints().size() - 1; i++){
			sectionUpperCurveY.add(
					(double) fuselageCurvesSection.getSectionUpperRightPoints().get(i).x
					);
			sectionUpperCurveZ.add(
					(double) fuselageCurvesSection.getSectionUpperRightPoints().get(i).y
					);
		}
		// TO DO: CAREFUL WITH REPEATED POINTS
		for (int i = 0; i <= fuselageCurvesSection.getSectionUpperLeftPoints().size() - 1; i++){
			sectionUpperCurveY.add(
					(double) fuselageCurvesSection.getSectionUpperLeftPoints().get(i).x
					);
			sectionUpperCurveZ.add(
					(double) fuselageCurvesSection.getSectionUpperLeftPoints().get(i).y
					);
		}

		// LOWER CURVE -----------------------------------
		// counter-clockwise
		for (int i = 0; i <= fuselageCurvesSection.getSectionLowerLeftPoints().size() - 1; i++){
			sectionLowerCurveY.add(
					(double) fuselageCurvesSection.getSectionLowerLeftPoints().get(i).x
					);
			sectionLowerCurveZ.add(
					(double) fuselageCurvesSection.getSectionLowerLeftPoints().get(i).y
					);
		}
		// TO DO: CAREFUL WITH REPEATED POINTS
		for (int i = 0; i <= fuselageCurvesSection.getSectionLowerRightPoints().size() - 1; i++){
			sectionLowerCurveY.add(
					(double) fuselageCurvesSection.getSectionLowerRightPoints().get(i).x
					);
			sectionLowerCurveZ.add(
					(double) fuselageCurvesSection.getSectionLowerRightPoints().get(i).y
					);

		}

		//-------------------------------------------------------
		// Create section-YZ objects
		//-------------------------------------------------------

		// Populate the list of YZ sections
		sectionsYZ.clear();
		sectionsYZStations.clear();

		// NOSE TIP
		// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
		Double x  = 0.;//_dxNoseCap.doubleValue(SI.METRE)  ; // NOTE
		Double hf =  Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		Double wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		sectionsYZ.add(
				new MyFuselageCurvesSection(
						wf, hf, 0.5, 0.0, 0.0, // lengths & parameters
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(Amount.valueOf(0.0,SI.METRE));

		// NOSE CAP
		// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
		x  = dxNoseCap.doubleValue(SI.METRE); // NOTE
		hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		sectionsYZ.add(
				new MyFuselageCurvesSection(
						wf, hf, 0.5, 0.0, 0.0, // lengths & parameters
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// MID-NOSE
		// IDX_SECTION_YZ_MID_NOSE
		x  =  0.5*lenN.doubleValue(SI.METRE);
		hf =  Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		sectionsYZ.add(
				new MyFuselageCurvesSection(
						wf, hf, sectionNoseMidLowerToTotalHeightRatio, sectionMidNoseRhoUpper, sectionMidNoseRhoLower, // lengths & parameters
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// CYLINDER 1
		// IDX_SECTION_YZ_CYLINDER
		x  =  lenN.doubleValue(SI.METRE);
		wf =  sectionCylinderWidth.doubleValue(SI.METRE);
		hf =  sectionCylinderHeight.doubleValue(SI.METRE);
		sectionsYZ.add(
				new MyFuselageCurvesSection(
						wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// CYLINDER 2
		// IDX_SECTION_YZ_CYLINDER
		x  =  lenN.doubleValue(SI.METRE) + lenC.doubleValue(SI.METRE);
		wf =  sectionCylinderWidth.doubleValue(SI.METRE);
		hf =  sectionCylinderHeight.doubleValue(SI.METRE);
		sectionsYZ.add(
				new MyFuselageCurvesSection(
						wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// MID-TAIL
		// IDX_SECTION_YZ_MID_TAIL
		x = lenF.doubleValue(SI.METRE) - 0.5*lenT.doubleValue(SI.METRE);
		hf =  Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		sectionsYZ.add(
				new MyFuselageCurvesSection(
						wf, hf, sectionTailMidLowerToTotalHeightRatio, sectionMidTailRhoUpper, sectionMidTailRhoLower, // lengths & parameters
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// TAIL CAP
		// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
		x =  lenF.doubleValue(SI.METRE) - dxTailCap.doubleValue(SI.METRE);
		hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		sectionsYZ.add(
				new MyFuselageCurvesSection(
						wf, hf, 0.5, 0.0, 0.0, // lengths & parameters
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// TAIL TIP
		// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
		x =  lenF.times(0.999995).doubleValue(SI.METRE);// - _dxTailCap.doubleValue(SI.METRE);
		hf =  Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		sectionsYZ.add(
				new MyFuselageCurvesSection(
						wf, hf, 0.5, 0.0, 0.0, // lengths & parameters
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(lenF);

		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// Upper and lower coordinates of YZ sections

		for ( List<Double> l : sectionUpperCurvesY) l.clear();
		sectionUpperCurvesY.clear();
		for ( List<Double> l : sectionUpperCurvesZ) l.clear();
		sectionUpperCurvesZ.clear();
		for ( List<Double> l : sectionLowerCurvesY) l.clear();
		sectionLowerCurvesY.clear();
		for ( List<Double> l : sectionLowerCurvesZ) l.clear();
		sectionLowerCurvesZ.clear();

		//++++++++++++++
		// TO DO: Careful with repeated points
		for (int idx = 0; idx < NUM_SECTIONS_YZ; idx++)
		{
			List<Double> listDoubleYu = new ArrayList<Double>(); // a new array for each section
			List<Double> listDoubleZu = new ArrayList<Double>(); // a new array for each section
			for (int i=0; i < sectionsYZ.get(idx).getSectionUpperRightPoints().size(); i++) {
				listDoubleYu.add( (double) sectionsYZ.get(idx).getSectionUpperRightPoints().get(i).x);
				listDoubleZu.add( (double) sectionsYZ.get(idx).getSectionUpperRightPoints().get(i).y);
			}
			for (int i=0; i < sectionsYZ.get(idx).getSectionUpperLeftPoints().size(); i++) {
				listDoubleYu.add( (double) sectionsYZ.get(idx).getSectionUpperLeftPoints().get(i).x);
				listDoubleZu.add( (double) sectionsYZ.get(idx).getSectionUpperLeftPoints().get(i).y);
			}
			sectionUpperCurvesY.add(listDoubleYu);
			sectionUpperCurvesZ.add(listDoubleZu);

			//			System.out.println("_sectionUpperCurvesY:\n"+_sectionUpperCurvesY.get(idx));
			//			System.out.println("_sectionUpperCurvesZ:\n"+_sectionUpperCurvesZ.get(idx));

			List<Double> listDoubleYl = new ArrayList<Double>(); // a new array for each section
			List<Double> listDoubleZl = new ArrayList<Double>(); // a new array for each section
			for (int i=0; i < sectionsYZ.get(idx).getSectionLowerLeftPoints().size(); i++) {
				listDoubleYl.add( (double) sectionsYZ.get(idx).getSectionLowerLeftPoints().get(i).x);
				listDoubleZl.add( (double) sectionsYZ.get(idx).getSectionLowerLeftPoints().get(i).y);
			}
			for (int i=0; i < sectionsYZ.get(idx).getSectionLowerRightPoints().size(); i++) {
				listDoubleYl.add( (double) sectionsYZ.get(idx).getSectionLowerRightPoints().get(i).x);
				listDoubleZl.add( (double) sectionsYZ.get(idx).getSectionLowerRightPoints().get(i).y);
			}
			sectionLowerCurvesY.add(listDoubleYl);
			sectionLowerCurvesZ.add(listDoubleZl);

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

		outlineXYSideRCurveZ.clear();
		for (int i = 0; i < outlineXZUpperCurveX.size(); i++){
			double xs = outlineXZUpperCurveX.get(i);
			double zs = this.getZSide(xs);
			outlineXYSideRCurveZ.add(zs);
		}

		// LEFT CURVE (mirror)----------------------------------
		outlineXYSideLCurveX.clear();
		outlineXYSideLCurveY.clear();
		outlineXYSideLCurveZ.clear();
		for (int i = 0; i < outlineXYSideRCurveX.size(); i++){
			//
			outlineXYSideLCurveX.add(  outlineXYSideRCurveX.get(i) ); // <== X
			outlineXYSideLCurveY.add( -outlineXYSideRCurveY.get(i) ); // <== -Y
			outlineXYSideLCurveZ.add(  outlineXYSideRCurveZ.get(i) ); // <== Z
		}
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	}// end-of calculateOutlines

	public int getNumberPointsNose() {
		return npN;
	}

	public int getNumberPointsCylinder() {
		return npC;
	}

	public int getNumberPointsTail() {
		return npT;
	}

	public int getgetNumberPointsSectionUpper() {
		return npSecUp;
	}

	public int getgetNumberPointsSectionLower() {
		return npSecLow;
	}

	/** Return equivalent diameter of entire fuselage */
	public Double calculateEquivalentDiameter(){

		// BEWARE: Gtmat library starts indexing arrays from 1!
		// To workaround this problem use .data to extract a double[] array
		double[] x = MyArrayUtils.linspace(0., lenF.getEstimatedValue()*(1-0.0001), 200);

		return MyMathUtils.arithmeticMean((getEquivalentDiameterAtX(x)));

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

	//  Return width at x-coordinate
	public Double getWidthAtX(double x) {
		return 2*getYOutlineXYSideRAtX(x);
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

		// section z-coordinates at x
		Double z_F_u = 0.0;
		if (x < vxu[0]) {
			z_F_u = vzu[0];
		}
		if (x > vxu[vxu.length-1]) {
			z_F_u = vzu[vzu.length-1];
		}
		if ((x >= vxu[0]) && (x <= vxu[vxu.length-1])){
			z_F_u = myInterpolationFunctionUpper.value(x);
		}
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
		Double z_F_l = 0.0;
		if (x < vxl[0]) {
			z_F_l = vzl[0];
		}
		if (x > vxl[vxl.length-1]) {
			z_F_l = vzl[vzl.length-1];
		}
		if ((x >= vxl[0]) && (x <= vxl[vxl.length-1])){
			z_F_l = myInterpolationFunctionLower.value(x);
		}
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

		Double y_F_r = 0.0;
		if (x < vxs[0]) {
			y_F_r = vys[0];
		}
		if (x > vxs[vxs.length-1]) {
			y_F_r = vys[vxs.length-1];
		}
		if ((x >= vxs[0]) && (x <= vxs[vxs.length-1])){
			y_F_r = myInterpolationFunctionSide.value(x);
		}
		return y_F_r;
	}


	public Double getYOutlineXYSideLAtX(double x) {
		return -getYOutlineXYSideRAtX(x);
	}

	// NOTE: section points on SideR are ordered as follows:
	//       first point is at Y=0 at the top of the section,
	//       successive points are taken going counter-clockwise when looking at
	//       YZ section from X- towards X+
	public List<PVector> getUniqueValuesYZSideRCurve(Amount<Length> len_x)
	{
		List<PVector> p  = new ArrayList<PVector>();

		MyFuselageCurvesSection curvesSection = makeSection(len_x.doubleValue(SI.METRE));

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
		if ( outlineXZUpperCurveX.size() != 0 )
			p.add(
					new PVector(
							(float)outlineXZUpperCurveX.get(0).doubleValue(),
							(float)0.0,
							(float)outlineXZUpperCurveZ.get(0).doubleValue()
							)
					);

		for(int i = 1; i <= outlineXZUpperCurveX.size()-1; i++)
		{
			if ( !outlineXZUpperCurveX.get(i-1).equals( outlineXZUpperCurveX.get(i) ) )
			{
				p.add(
						new PVector(
								(float)outlineXZUpperCurveX.get(i).doubleValue(),
								(float)0.0,
								(float)outlineXZUpperCurveZ.get(i).doubleValue()
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
		if ( outlineXZLowerCurveX.size() != 0 )
			p.add(
					new PVector(
							(float)outlineXZLowerCurveX.get(0).doubleValue(),
							(float)0.0,
							(float)outlineXZLowerCurveZ.get(0).doubleValue()
							)
					);
		for(int i = 1; i <= outlineXZLowerCurveX.size()-1; i++)
		{
			if ( !outlineXZLowerCurveX.get(i-1).equals( outlineXZLowerCurveX.get(i) ) )
			{
				p.add(
						new PVector(
								(float)outlineXZLowerCurveX.get(i).doubleValue(),
								(float)0.0,
								(float)outlineXZLowerCurveZ.get(i).doubleValue()
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
		if ( outlineXYSideRCurveX.size() != 0 )
			p.add(
					new PVector(
							(float)outlineXYSideRCurveX.get(0).doubleValue(),
							(float)outlineXYSideRCurveY.get(0).doubleValue(),
							(float)0.0 // _outlineXYSideRCurveZ.get(0).doubleValue()
							)
					);
		for(int i = 1; i <= outlineXYSideRCurveX.size()-1; i++)
		{
			if ( ! outlineXYSideRCurveX.get(i-1).equals( outlineXYSideRCurveX.get(i) ) )
			{
				p.add(
						new PVector(
								(float)outlineXYSideRCurveX.get(i).doubleValue(),
								(float)outlineXYSideRCurveY.get(i).doubleValue(),
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
		if ( outlineXYSideLCurveX.size() != 0 )
			p.add(
					new PVector(
							(float)outlineXYSideLCurveX.get(0).doubleValue(),
							(float)outlineXYSideLCurveY.get(0).doubleValue(),
							(float)outlineXYSideLCurveZ.get(0).doubleValue()
							)
					);
		for(int i = 1; i <= outlineXYSideLCurveX.size()-1; i++)
		{
			if ( !outlineXYSideLCurveX.get(i-1).equals( outlineXYSideLCurveX.get(i) ) )
			{
				p.add(
						new PVector(
								(float)outlineXYSideLCurveX.get(i).doubleValue(),
								(float)outlineXYSideLCurveY.get(i).doubleValue(),
								(float)outlineXYSideLCurveZ.get(i).doubleValue()
								)
						);
			}
		}
		return p;
	}



	/**
	 * Calculate a fuselage section profile for a given coordinate x,
	 * with interpolated values of section shape parameters
	 * @param x section X-coordinate
	 * @return a MyFuselageCurvesSection object
	 */
	public MyFuselageCurvesSection makeSection(double x){

		//		System.out.println("makeSection :: _sectionsYZ size: "+ _sectionsYZ.size() +" x: "+ x);

		if ( sectionsYZ == null )
		{
			System.out.println("ERROR -- MyFuselageCurvesSection :: makeSection -- _sectionsYZ is null ");
			return null;
		}
		if ( sectionsYZ.size() == 0 )
		{
			System.out.println("ERROR -- MyFuselageCurvesSection :: makeSection -- _sectionsYZ.size() = 0 ");
			return null;
		}

		if ( sectionsYZStations.size() != NUM_SECTIONS_YZ )
		{
			System.out.println("ERROR -- MyFuselageCurvesSection :: makeSection -- _sectionsYZStations.size()="
					+ sectionsYZStations.size() +" != NUM_SECTIONS_YZ="+ NUM_SECTIONS_YZ);
			return null;
		}

		//		System.out.println("makeSection :: _sectionsYZ size: "+ _sectionsYZ.size());

		// breakpoints
		double vxSec[] = new double[NUM_SECTIONS_YZ];
		vxSec[IDX_SECTION_YZ_NOSE_TIP   ] = sectionsYZStations.get(IDX_SECTION_YZ_NOSE_TIP).doubleValue(SI.METRE);
		vxSec[IDX_SECTION_YZ_NOSE_CAP   ] = sectionsYZStations.get(IDX_SECTION_YZ_NOSE_CAP).doubleValue(SI.METRE);
		vxSec[IDX_SECTION_YZ_MID_NOSE   ] = sectionsYZStations.get(IDX_SECTION_YZ_MID_NOSE).doubleValue(SI.METRE);
		vxSec[IDX_SECTION_YZ_CYLINDER_1 ] = sectionsYZStations.get(IDX_SECTION_YZ_CYLINDER_1).doubleValue(SI.METRE);
		vxSec[IDX_SECTION_YZ_CYLINDER_2 ] = sectionsYZStations.get(IDX_SECTION_YZ_CYLINDER_2).doubleValue(SI.METRE);
		vxSec[IDX_SECTION_YZ_MID_TAIL   ] = sectionsYZStations.get(IDX_SECTION_YZ_MID_TAIL).doubleValue(SI.METRE);
		vxSec[IDX_SECTION_YZ_TAIL_CAP   ] = sectionsYZStations.get(IDX_SECTION_YZ_TAIL_CAP).doubleValue(SI.METRE);
		vxSec[IDX_SECTION_YZ_TAIL_TIP   ] = sectionsYZStations.get(IDX_SECTION_YZ_TAIL_TIP).doubleValue(SI.METRE);

		// values of section parameters at breakpoints
		double vA[]    = new double[NUM_SECTIONS_YZ];
		double vRhoU[] = new double[NUM_SECTIONS_YZ];
		double vRhoL[] = new double[NUM_SECTIONS_YZ];

		for (int i = 0; i < NUM_SECTIONS_YZ; i++)
		{
			// parameter a, 0.5 -> ellipse/circle, 0.0 -> squeeze lower part, 1.0 -> squeeze upper part
			vA[i]    = sectionsYZ.get(i).get_LowerToTotalHeightRatio();
			// parameter rho,
			vRhoU[i] = sectionsYZ.get(i).get_RhoUpper();
			vRhoL[i] = sectionsYZ.get(i).get_RhoLower();
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

		double z_F_u = 0.0;
		if (x < vxu[0]) {
			z_F_u = vzu[0];
		}
		if (x > vxu[vxu.length-1]) {
			z_F_u = vzu[vzu.length-1];
		}
		if ((x >= vxu[0]) && (x <= vxu[vxu.length-1])){
			z_F_u = myInterpolationFunctionUpper.value(x);
		}
		z_F_u = myInterpolationFunctionUpper.value(x);


		double z_F_l = 0.0;
		if (x < vxl[0]) {
			z_F_l = vzl[0];
		}
		if (x > vxl[vxl.length-1]) {
			z_F_l = vzl[vzl.length-1];
		}
		if ((x >= vxl[0]) && (x <= vxl[vxl.length-1])){
			z_F_l = myInterpolationFunctionLower.value(x);
		}

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

		double y_F_r = 0.0;
		if (x < vxs[0]) {
			y_F_r = vys[0];
		}
		if (x > vxs[vxs.length-1]) {
			y_F_r = vys[vxs.length-1];
		}
		if ((x >= vxs[0]) && (x <= vxs[vxs.length-1])){
			y_F_r = myInterpolationFunctionSide.value(x);
		}
		double w_F = 2.0*y_F_r;

		MyFuselageCurvesSection fuselageCurvesSection = new MyFuselageCurvesSection(
				w_F, h_F, // lengths
				sectionLowerToTotalHeightRatio, sectionRhoUpper, sectionRhoLower, // current parameters
				//				_sectionCylinderLowerToTotalHeightRatio, _sectionCylinderRhoUpper, _sectionCylinderRhoLower, // object parameters
				npSecUp, npSecLow // no. points (nose, cylinder, tail)
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

		MyFuselageCurvesSection section = makeSection(x);

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
			Double x  =  dxNoseCap.doubleValue(SI.METRE);
			Double hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ) );
			Double wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ) );
			sectionsYZ.get(idx).setSectionParameters(
					wf, hf, 0.5, 0.0, 0.0,         // lengths & parameters
					npSecUp, npSecLow          // num. points
					);
			break;

		case IDX_SECTION_YZ_MID_NOSE:

			// MID-NOSE
			// IDX_SECTION_YZ_MID_NOSE

			//System.out.println("+++ rhoUpper: "+ _sectionsYZ.get(idx).get_RhoUpper());

			x  = 0.5*lenN.doubleValue(SI.METRE);
			hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ) );
			wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ) );
			sectionsYZ.get(idx).setSectionParameters(
					wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
					npSecUp, npSecLow          // num. points
					);

			//System.out.println("--+ rhoUpper: "+ _sectionsYZ.get(idx).get_RhoUpper());

			break;

		case IDX_SECTION_YZ_CYLINDER_1:

			// CYLINDER
			// IDX_SECTION_YZ_CYLINDER
			x  = lenN.doubleValue(SI.METRE);
			wf = sectionCylinderWidth.doubleValue(SI.METRE);
			hf = sectionCylinderHeight.doubleValue(SI.METRE);
			sectionsYZ.get(idx).setSectionParameters(
					wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
					npSecUp, npSecLow          // num. points
					);
			break;

		case IDX_SECTION_YZ_CYLINDER_2:
			// CYLINDER
			// IDX_SECTION_YZ_CYLINDER
			x  = lenN.doubleValue(SI.METRE) + lenC.doubleValue(SI.METRE);
			wf = sectionCylinderWidth.doubleValue(SI.METRE);
			hf = sectionCylinderHeight.doubleValue(SI.METRE);
			sectionsYZ.get(idx).setSectionParameters(
					wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
					npSecUp, npSecLow          // num. points
					);
			break;

		case IDX_SECTION_YZ_MID_TAIL:
			// MID-TAIL
			// IDX_SECTION_YZ_MID_TAIL
			x  = lenF.doubleValue(SI.METRE) - 0.5*lenT.doubleValue(SI.METRE);
			hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ) );
			wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ) );
			sectionsYZ.get(idx).setSectionParameters(
					wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
					npSecUp, npSecLow          // num. points
					);
			break;

		case IDX_SECTION_YZ_TAIL_CAP: // a, rhoUpper, rhoLower NOT USED
			// TAIL CAP
			// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
			x  = lenF.doubleValue(SI.METRE) - dxTailCap.doubleValue(SI.METRE);
			hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ) );
			wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ) );
			sectionsYZ.get(idx).setSectionParameters(
					wf, hf, 0.5, 0.0, 0.0,         // lengths & parameters
					npSecUp, npSecLow          // num. points
					);
			break;

		default:
			// do nothing
			break;
		}

		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// adjust Z-coordinates in side curves
		// Take Z-values from section shape scaled at x
		outlineXYSideRCurveZ.clear();
		for (int i = 0; i < outlineXZUpperCurveX.size(); i++){
			double x = outlineXZUpperCurveX.get(i);
			double z = this.getZSide(x);
			outlineXYSideRCurveZ.add(z);
		}

		// LEFT CURVE (mirror)----------------------------------
		outlineXYSideLCurveX.clear();
		outlineXYSideLCurveY.clear();
		outlineXYSideLCurveZ.clear();
		for (int i = 0; i < outlineXYSideRCurveX.size(); i++){
			//
			outlineXYSideLCurveX.add(  outlineXYSideRCurveX.get(i) ); // <== X
			outlineXYSideLCurveY.add( -outlineXYSideRCurveY.get(i) ); // <== -Y
			outlineXYSideLCurveZ.add(  outlineXYSideRCurveZ.get(i) ); // <== Z
		}
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	}

	public void calculateOutlinesUpperLowerSectionYZ(int idx)
	{

		// initial checks
		if ( sectionUpperCurvesY.size() == 0 ) return;
		if ( sectionUpperCurvesY.size() != NUM_SECTIONS_YZ ) return;
		if ( idx < 0 ) return;
		if ( idx >= NUM_SECTIONS_YZ ) return;

		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// Upper and lower coordinates of selected (idx) YZ section

		//++++++++++++++
		// TO DO: Careful with repeated points

		// Upper curve
		sectionUpperCurvesY.get(idx).clear();
		sectionUpperCurvesZ.get(idx).clear();
		for (int i=0; i < sectionsYZ.get(idx).getSectionUpperRightPoints().size(); i++) {
			sectionUpperCurvesY.get(idx).add(new Double(sectionsYZ.get(idx).getSectionUpperRightPoints().get(i).x));
			sectionUpperCurvesZ.get(idx).add(new Double(sectionsYZ.get(idx).getSectionUpperRightPoints().get(i).y));
		}
		for (int i=0; i < sectionsYZ.get(idx).getSectionUpperLeftPoints().size(); i++) {
			sectionUpperCurvesY.get(idx).add(new Double(sectionsYZ.get(idx).getSectionUpperLeftPoints().get(i).x));
			sectionUpperCurvesZ.get(idx).add(new Double(sectionsYZ.get(idx).getSectionUpperLeftPoints().get(i).y));
		}

		// Lower curve
		sectionLowerCurvesY.get(idx).clear();
		sectionLowerCurvesZ.get(idx).clear();
		for (int i=0; i < sectionsYZ.get(idx).getSectionLowerLeftPoints().size(); i++) {
			sectionLowerCurvesY.get(idx).add(new Double(sectionsYZ.get(idx).getSectionLowerLeftPoints().get(i).x));
			sectionLowerCurvesZ.get(idx).add(new Double(sectionsYZ.get(idx).getSectionLowerLeftPoints().get(i).y));
		}
		for (int i=0; i < sectionsYZ.get(idx).getSectionLowerRightPoints().size(); i++) {
			sectionLowerCurvesY.get(idx).add(new Double(sectionsYZ.get(idx).getSectionLowerRightPoints().get(i).x));
			sectionLowerCurvesZ.get(idx).add(new Double(sectionsYZ.get(idx).getSectionLowerRightPoints().get(i).y));
		}

	}


	public void calculateSwet(String method) {

		switch (method) {

		case "Stanford" : {
			sWetNose = Amount.valueOf(0.75 * Math.PI * equivalentDiameterCylinderGM.getEstimatedValue()*lenN.getEstimatedValue(), Area.UNIT);
			sWetTail = Amount.valueOf(0.72 * Math.PI * equivalentDiameterCylinderGM.getEstimatedValue()*lenT.getEstimatedValue(), Area.UNIT);
			sWetC = Amount.valueOf(Math.PI * equivalentDiameterCylinderGM.getEstimatedValue()*lenC.getEstimatedValue(), Area.UNIT);
			sWet = Amount.valueOf(sWetNose.getEstimatedValue() + sWetTail.getEstimatedValue() + sWetC.getEstimatedValue(), Area.UNIT); break;
		}

		case "Torenbeek" : { // page 409 torenbeek 2013
			sFront = Amount.valueOf((Math.PI/4) * Math.pow(sectionCylinderHeight.getEstimatedValue(),2), Area.UNIT); // CANNOT FIND FUSELAGE HEIGHT in MyAeroFuselage!!
			sWet = Amount.valueOf(sFront.getEstimatedValue()*4*(lambdaF - 1.30), Area.UNIT); break;
		}

		}

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
		double x0 = lenN.doubleValue(SI.METER) + lenC.doubleValue(SI.METER);
//		System.out.println("l_N + l_C: " + x0 + " (m)");

		// values filtered as x >= l_N + l_C
		List<Double> vX = new ArrayList<Double>();
		outlineXZLowerCurveX.stream().filter(x -> x > x0 ).distinct().forEach(vX::add);
//		vX.stream().forEach(e -> System.out.println(e));

		// index of first x in _outlineXZLowerCurveX >= x0
		int idxX0 = IntStream.range(0,outlineXZLowerCurveX.size())
	            .reduce((i,j) -> outlineXZLowerCurveX.get(i) > x0 ? i : j)
	            .getAsInt();  // or throw

		// the coupled z-values
		List<Double> vZ = new ArrayList<Double>();
		vZ = IntStream.range(0, outlineXZLowerCurveZ.size()).filter(i -> i >= idxX0)
			 .mapToObj(i -> outlineXZLowerCurveZ.get(i)).distinct()
	         .collect(Collectors.toList());

		// generate a vector of constant z = z_min + 0.26*d_C, same size of vZ, or vX
		Double z1 = vZ.get(0) + 0.26*sectionCylinderHeight.doubleValue(SI.METER);
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

		upsweepAngle = Amount.valueOf(Math.atan((vZ.get(idxXu)-zu)/(vX.get(idxXu)-xu)), SI.RADIAN).to(NonSI.DEGREE_ANGLE);

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
				double xLNose = lenN.doubleValue(SI.METER);

				// values filtered as x <= l_N
				List<Double> vXNose = new ArrayList<Double>();
				outlineXZUpperCurveX.stream().filter(x -> x <= xLNose ).distinct().forEach(vXNose::add);

				// index of last x in _outlineXZUpperCurveX >= xLNose
				int idxXNose = IntStream.range(0,outlineXZUpperCurveX.size())
			            .reduce((i,j) -> outlineXZUpperCurveX.get(i) > xLNose ? i : j)
			            .getAsInt();  // or throw

				// the coupled z-values
				// In this case is necessary filtered the strem with idxXNose-1 and not with idxXNose
				// because the values _outlineXZUpperCurveZ(idxXNose) and _outlineXZUpperCurveZ(idxXNose-1)
				// are different, in spite of they have the same value of _outlineXZUpperCurveX.
				List<Double> vZNose = new ArrayList<Double>();
				vZNose = IntStream.range(0, outlineXZUpperCurveZ.size()).filter(i -> i <= idxXNose-1)
					 .mapToObj(i -> outlineXZUpperCurveZ.get(i)).distinct()
			         .collect(Collectors.toList());


				// generate a vector of constant z = z_min + 0.75*d_C, same size of vZNose, or vXNose
				// It's better to take the value of z at 0.60*d_C (for the methodology)
				Double z1Nose = outlineXZLowerCurveZ.get(9) + 0.75*sectionCylinderHeight.doubleValue(SI.METER);
//				Double z1Nose = outlineXZLowerCurveZ.get(9) + 0.60*sectionCylinderHeight.doubleValue(SI.METER);
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

				windshieldAngle = Amount.valueOf(Math.atan((vZNose.get(idxXw)-zw)/(vXNose.get(idxXw)-xw)), SI.RADIAN)
						.to(NonSI.DEGREE_ANGLE);

//				System.out.println("---");
//				System.out.println("Windshield angle:" + _windshieldAngle.to(NonSI.DEGREE_ANGLE));
//				System.out.println("---");
	}

	public double calculateFormFactor(double lambdaF) {
		return 1. + 60./Math.pow(lambdaF,3) + 0.0025*(lambdaF);
	}

	public static double calculateSfront(double fuselageDiameter){
		return Math.PI*Math.pow(fuselageDiameter, 2)/4;
	}

	private void updateCurveSections()
	{
		for (int k = 0; k < sectionsYZ.size(); k++)
		{
			sectionsYZ.get(k).set_x(sectionsYZStations.get(k).doubleValue(SI.METER));
		}

	}

	public void clearOutlines( )
	{
		outlineXZUpperCurveX.clear();
		outlineXZUpperCurveZ.clear();
		outlineXZLowerCurveX.clear();
		outlineXZLowerCurveZ.clear();
		outlineXZCamberLineX.clear();
		outlineXZCamberLineZ.clear();
		outlineXYSideRCurveX.clear();
		outlineXYSideRCurveY.clear();
		outlineXYSideRCurveZ.clear();
		outlineXYSideLCurveX.clear();
		outlineXYSideLCurveY.clear();
		outlineXYSideLCurveZ.clear();
		sectionUpperCurveY.clear();
		sectionUpperCurveZ.clear();
		sectionLowerCurveY.clear();
		sectionLowerCurveZ.clear();
		sectionsYZ.clear();
		sectionsYZStations.clear();
	}

	//========================================================================

	@Override
	public List<Amount<Length>> getXYZ0() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getX0() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getY0() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getZ0() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setXYZ0(Amount<Length> x0, Amount<Length> y0, Amount<Length> z0) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Amount<Length>> getXYZPole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getXPole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getYPole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getZPole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setXYZPole(Amount<Length> xp, Amount<Length> yp, Amount<Length> zp) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getDeckNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDeckNumber(int dn) {
		// TODO Auto-generated method stub

	}

	@Override
	public Amount<Length> getLength() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLength(Amount<Length> len) {
		// TODO Auto-generated method stub

	}

	@Override
	public Amount<Mass> getMassReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMassReference(Amount<Mass> massRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void discretizeGeometry(int numberSpanwiseStations) {
		// TODO Auto-generated method stub

	}

	@Override
	public Amount<Area> getSurfaceWetted(boolean recalculate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Area> getSurfaceWetted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Amount<Length>> getDiscretizedYs() {
		// TODO Auto-generated method stub
		return null;
	}

	public static Fuselage importFromXML(String pathToXML) {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);
		
		
		System.out.println("Reading fuselage data ...");

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage/@id");
		String pressProp = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage/@pressurized");
		Boolean pressurized = Boolean.valueOf(pressProp);

		
		// GLOBAL DATA
		List<String> deckProp = reader.getXMLPropertiesByPath("//global_data/deck_number");
		Integer deckNum = Integer.valueOf(deckProp.get(0));
//		Amount<?> deckNum = reader.getXMLAmountWithUnitByPath("//global_data/deck_number").to(Unit.ONE);
		
		Amount<Length> len = reader.getXMLAmountLengthByPath("//global_data/length");
		List<String> refMassProp = reader.getXMLPropertiesByPath("//global_data/mass_reference");
		Amount massReference = Amount.valueOf(Double.valueOf(refMassProp.get(0)), SI.KILOGRAM);
		Amount<Length> roughness = reader.getXMLAmountLengthByPath("//global_data/roughness");
		
		// NOSE TRUNK
		List<String> lenRatioNoseProp = reader.getXMLPropertiesByPath("//nose_trunk/length_ratio");
		Double lenRatioNF = Double.valueOf(lenRatioNoseProp.get(0));
		List<String> finenessRatioNoseProp = reader.getXMLPropertiesByPath("//nose_trunk/fineness_ratio");
		Double lambdaN = Double.valueOf(finenessRatioNoseProp.get(0));
		Amount<Length> heightN = reader.getXMLAmountLengthByPath("//nose_trunk/tip_height_offset");
		
		List<String>  dxCapPercentNoseProp = reader.getXMLPropertiesByPath("//nose_trunk/dx_cap_percent");
		Double dxNoseCapPercent = Double.valueOf(dxCapPercentNoseProp.get(0));
		
		List<String>  windshieldType = reader.getXMLPropertiesByPath("//nose_trunk/windshield_type");
		Amount<Length> windshieldWidth = reader.getXMLAmountLengthByPath("//nose_trunk/windshield_width");
		Amount<Length> windshieldHeight = reader.getXMLAmountLengthByPath("//nose_trunk/windshield_height");
		List<String>  midSectionLowerToTotalHeightRatioProp = reader.getXMLPropertiesByPath("//nose_trunk/mid_section_lower_to_total_height_ratio");
		Double sectionNoseMidLowerToTotalHeightRatio = Double.valueOf(midSectionLowerToTotalHeightRatioProp.get(0));
		List<String>  midSectionRhoUpperNoseProp = reader.getXMLPropertiesByPath("//nose_trunk/mid_section_rho_upper");
		Double sectionMidNoseRhoUpper = Double.valueOf(midSectionRhoUpperNoseProp.get(0));
		List<String>  midSectionRhoLowerNoseProp = reader.getXMLPropertiesByPath("//nose_trunk/mid_section_rho_lower");
		Double sectionMidNoseRhoLower = Double.valueOf(midSectionRhoLowerNoseProp.get(0));
		
		// CYLINDRICAL TRUNK
		List<String>  lenRatioCylProp = reader.getXMLPropertiesByPath("//cylindrical_trunk/length_ratio");
		Double lenRatioCF = Double.valueOf(lenRatioCylProp.get(0));
		Amount<Length> sectionCylinderWidth = reader.getXMLAmountLengthByPath("//cylindrical_trunk/section_width");
		Amount<Length> sectionCylinderHeight = reader.getXMLAmountLengthByPath("//cylindrical_trunk/section_height");
		Amount<Length> heightFromGround = reader.getXMLAmountLengthByPath("//cylindrical_trunk/height_from_ground");
		List<String>  sectionLowerToTotalHeightRatioProp = reader.getXMLPropertiesByPath("//cylindrical_trunk/section_lower_to_total_height_ratio");
		Double sectionCylinderLowerToTotalHeightRatio = Double.valueOf(sectionLowerToTotalHeightRatioProp.get(0));
		List<String>  sectionRhoUpperProp = reader.getXMLPropertiesByPath("//cylindrical_trunk/section_rho_upper");
		Double sectionCylinderRhoUpper = Double.valueOf(sectionRhoUpperProp.get(0));
		List<String>  sectionRhoLowerProp = reader.getXMLPropertiesByPath("//cylindrical_trunk/section_rho_lower");
		Double sectionCylinderRhoLower = Double.valueOf(sectionRhoUpperProp.get(0));
		
		// TAIL TRUNK
		Amount<Length> heightT = reader.getXMLAmountLengthByPath("//tail_trunk/tip_height_offset");
		List<String>  dxCapPercentTailProp = reader.getXMLPropertiesByPath("//tail_trunk/dx_cap_percent");
		Double dxTailCapPercent = Double.valueOf(dxCapPercentTailProp.get(0));
		List<String>  sectionTailMidLowerToTotalHeightRatioProp = reader.getXMLPropertiesByPath("//tail_trunk/mid_section_lower_to_total_height_ratio");
		Double sectionTailMidLowerToTotalHeightRatio = Double.valueOf(sectionTailMidLowerToTotalHeightRatioProp.get(0));
		List<String>  midSectionRhoUpperTailProp = reader.getXMLPropertiesByPath("//tail_trunk/mid_section_rho_upper");
		Double sectionMidTailRhoUpper = Double.valueOf(midSectionRhoUpperTailProp.get(0));
		List<String>  midSectionRhoLowerTailProp = reader.getXMLPropertiesByPath("//tail_trunk/mid_section_rho_lower");
		Double sectionMidTailRhoLower = Double.valueOf(midSectionRhoLowerTailProp.get(0));
		
		// create the fuselage via its builder
		Fuselage fuselage = new FuselageBuilder(id)
				// TOP LEVEL
				.pressurized(pressurized)
				//GLOBAL DATA
				.length(len)
				.deckNumber(deckNum)
				.massReference(massReference)
				.roughness(roughness)
				// NOSE TRUNK
				.dxNoseCapPercent(dxNoseCapPercent)
				.heightN(heightN)
				.lenRatioNF(lenRatioNF)
				.lambdaN(lambdaN)
				.sectionMidNoseRhoLower(sectionMidNoseRhoLower)
				.sectionMidNoseRhoUpper(sectionMidNoseRhoUpper)
				.sectionNoseMidLowerToTotalHeightRatio(sectionNoseMidLowerToTotalHeightRatio)
				.windshieldHeight(windshieldHeight)
				.windshieldWidth(windshieldWidth)
				// CYLINDRICAL TRUNK
				.lenRatioCF(lenRatioCF)
				.sectionCylinderHeight(sectionCylinderHeight)
				.sectionCylinderWidth(sectionCylinderWidth)
				.heightFromGround(heightFromGround)
				.sectionCylinderLowerToTotalHeightRatio(sectionCylinderLowerToTotalHeightRatio)
				.sectionCylinderRhoLower(sectionCylinderRhoLower)
				.sectionCylinderRhoUpper(sectionCylinderRhoUpper)
				// TAIL TRUNK
				.heightT(heightT)
				.dxTailCapPercent(dxTailCapPercent)
				.sectionMidTailRhoLower(sectionMidTailRhoLower)
				.sectionMidTailRhoUpper(sectionMidTailRhoUpper)
				.sectionTailMidLowerToTotalHeightRatio(sectionTailMidLowerToTotalHeightRatio)
				//
				.build();

		return fuselage;
	}

	// Builder pattern via a nested public static class
	public static class FuselageBuilder {
		// required parameters
		private String _id;

		// optional parameters ... defaults
		// ...
		private int _deckNumber = 1;
		private Amount<Mass> _massReference = Amount.valueOf(3300, SI.KILOGRAM);

		private Boolean _pressurized;

		// Fuselage overall length
		private Amount<Length> _lenF;

		private Amount<Length> _lenN;
		private Amount<Length> _lenC;
		private Amount<Length> _lenT;

		private Amount<Length> _sectionCylinderHeight;

		private String _windshieldType; // Possible types (Roskam part VI, page 134): Flat,protruding; Flat,flush; Single,round; Single,sharp; Double

		// Distance of fuselage lowest part from ground
		private Amount<Length> _heightFromGround;

		private Amount<Length> _heightN, _heightT;

		private Amount<Length> _roughness;

		// Non-dimensional parameters
		private Double _lambdaN;
		private Double _lambdaC;
		private Double _lambdaT;
		private Double _lenRatioNF;
		private Double _lenRatioCF;
		private Double _lenRatioTF;
		private Double _formFactor;

		// Fuselage section parameters

		// Width and height
		private Amount<Length> _sectionCylinderWidth;
		private Amount<Length> _windshieldHeight, _windshieldWidth;

		private Amount<Length> _dxNoseCap, _dxTailCap;

		// Non dimensional section parameters

		// how lower part is different from half diameter
		private Double
			_sectionCylinderLowerToTotalHeightRatio,
			_sectionNoseMidLowerToTotalHeightRatio,
			_sectionTailMidLowerToTotalHeightRatio;


		// shape index, 1 --> close to a rectangle; 0 --> close to a circle
		private Double
			_sectionCylinderRhoUpper,
			_sectionCylinderRhoLower,
			_sectionMidNoseRhoUpper,
			_sectionMidNoseRhoLower,
			_sectionMidTailRhoUpper,
			_sectionMidTailRhoLower;

		// meshing stuff
		private int _np_N = 10, _np_C = 4, _np_T = 10, _np_SecUp = 10, _np_SecLow = 10;
		private double _deltaXNose, _deltaXCylinder, _deltaXTail;

		private double _dxNoseCapPercent;
		private double _dxTailCapPercent;


		public FuselageBuilder(String id){ // defaults to ATR72 fuselage
			this._id = id;
			this.initializeDefaultVariables(AircraftEnum.ATR72);
		}

		public FuselageBuilder(String id, AircraftEnum aircraftName){
			this._id = id;
			this.initializeDefaultVariables(aircraftName);
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

				_lenF         =  Amount.valueOf(27.166, 0.0, SI.METRE);
				_lenRatioNF       = 0.1496;
				_lenRatioCF       = 0.62;

				_sectionCylinderWidth     = Amount.valueOf(2.865,SI.METRE);
				_sectionCylinderHeight    = Amount.valueOf(2.6514, SI.METRE);

				// Nose fineness ratio, _len_N/_diam_N
				_lambdaN      = 1.2;

				// Height from ground of lowest part of fuselage
				_heightFromGround = Amount.valueOf(0.66, SI.METER);

				// Fuselage Roughness
				_roughness = Amount.valueOf(0.405e-5, SI.METRE);

				// positive if nose tip higher than cylindrical part ref. line (in XZ plane)
				_heightN      = Amount.valueOf(-0.15*_sectionCylinderHeight.doubleValue(SI.METRE), SI.METRE);
				_heightT      = Amount.valueOf(  0.8*(0.5*_sectionCylinderHeight.doubleValue(SI.METRE)), SI.METRE);

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

				_sectionMidNoseRhoUpper      = _sectionCylinderRhoUpper.doubleValue();
				_sectionMidTailRhoUpper      = _sectionCylinderRhoUpper.doubleValue();

				_sectionMidNoseRhoLower      = _sectionCylinderRhoLower.doubleValue();
				_sectionMidTailRhoLower      = _sectionCylinderRhoLower.doubleValue();
				break;

			case B747_100B:
				_deckNumber = 1;

				_lenF         =  Amount.valueOf(68.63, 0.0, SI.METRE);
				_lenRatioNF       = 0.1635;
				_lenRatioCF       = 0.4964;

				_sectionCylinderWidth     = Amount.valueOf(6.5,SI.METRE);
				_sectionCylinderHeight    = Amount.valueOf(7.1, SI.METRE);

				// Nose fineness ratio, _len_N/_diam_N
				_lambdaN      = 1.521;

				// Height from ground of lowest part of fuselage
				_heightFromGround = Amount.valueOf(2.1, SI.METER);

				// Fuselage Roughness
				_roughness = Amount.valueOf(0.405e-5, SI.METRE);

				// positive if nose tip higher than cylindrical part ref. line (in XZ plane)
				_heightN      = Amount.valueOf(-0.089*_sectionCylinderHeight.doubleValue(SI.METRE), SI.METRE);
				_heightT      = Amount.valueOf( 0.457*(0.5*_sectionCylinderHeight.doubleValue(SI.METRE)), SI.METRE);

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

				_sectionMidNoseRhoUpper      = _sectionCylinderRhoUpper.doubleValue();
				_sectionMidTailRhoUpper      = _sectionCylinderRhoUpper.doubleValue();

				_sectionMidNoseRhoLower      = _sectionCylinderRhoLower.doubleValue();
				_sectionMidTailRhoLower      = _sectionCylinderRhoLower.doubleValue();
				break;

			case AGILE_DC1:
				_deckNumber = 1;

				_lenF         =  Amount.valueOf(34., 0.0, SI.METRE);
				_lenRatioNF       = 0.1420;
				_lenRatioCF       = 0.6148;

				_sectionCylinderWidth     = Amount.valueOf(3.,SI.METRE);
				_sectionCylinderHeight    = Amount.valueOf(3., SI.METRE);

				// Nose fineness ratio, _len_N/_diam_N
				_lambdaN      = 1.4;

				// Height from ground of lowest part of fuselage
				_heightFromGround = Amount.valueOf(4.25, SI.METER);

				// Fuselage Roughness
				_roughness = Amount.valueOf(0.405e-5, SI.METRE);

				// positive if nose tip higher than cylindrical part ref. line (in XZ plane)
				_heightN      = Amount.valueOf(-0.1698*_sectionCylinderHeight.doubleValue(SI.METRE), SI.METRE);
				_heightT      = Amount.valueOf( 0.262*(0.5*_sectionCylinderHeight.doubleValue(SI.METRE)), SI.METRE);

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

				_sectionMidNoseRhoUpper      = _sectionCylinderRhoUpper.doubleValue();
				_sectionMidTailRhoUpper      = _sectionCylinderRhoUpper.doubleValue();

				_sectionMidNoseRhoLower      = _sectionCylinderRhoLower.doubleValue();
				_sectionMidTailRhoLower      = _sectionCylinderRhoLower.doubleValue();
				break;
			}
			// --- END OF INPUT DATA ------------------------------------------
		}
		
		
		public FuselageBuilder deckNumber(Integer deckNum) {
			_deckNumber = deckNum;
			return this;
		}

		public FuselageBuilder length(Amount<Length> len) {
			_lenF = len;
			return this;
		}
		
		public FuselageBuilder massReference(Amount<Mass> massReference) {
			_massReference = massReference;
			return this;
		}

		public FuselageBuilder pressurized(Boolean pressurized){
			_pressurized = pressurized;
			return this;
		}
		
		public FuselageBuilder lenRatioNF(Double lenRatioNF) {
			_lenRatioNF = lenRatioNF;
			return this;
		}
		
		public FuselageBuilder lenRatioCF(Double lenRatioCF) {
			_lenRatioCF = lenRatioCF;
			return this;
		}
		
		public FuselageBuilder sectionCylinderWidth(Amount<Length> sectionCylinderWidth) {
			_sectionCylinderWidth = sectionCylinderWidth;
			return this;
		}
		
		public FuselageBuilder sectionCylinderHeight(Amount<Length> sectionCylinderHeight) {
			_sectionCylinderHeight = sectionCylinderHeight;
			return this;
		}
		
		public FuselageBuilder lambdaN(Double lambdaN) {
			_lambdaN = lambdaN;
			return this;
		}
		
		public FuselageBuilder heightFromGround(Amount<Length> heightFromGround) {
			_heightFromGround = heightFromGround;
			return this;
		}
		
		public FuselageBuilder roughness(Amount<Length> roughness) {
			_roughness = roughness;
			return this;
		}
		
		public FuselageBuilder heightN(Amount<Length> heightN) {
			_heightN = heightN;
			return this;
		}
		
		public FuselageBuilder heightT(Amount<Length> heightT) {
			_heightT = heightT;
			return this;
		}
		
		public FuselageBuilder dxNoseCapPercent(double dxNoseCapPercent) {
			_dxNoseCapPercent = dxNoseCapPercent;
			return this;
		}
		
		public FuselageBuilder dxTailCapPercent(double dxTailCapPercent) {
			_dxTailCapPercent = dxTailCapPercent;
			return this;
		}
		
		public FuselageBuilder windshieldType(String windshieldType) {
			_windshieldType = windshieldType;
			return this;
		}
		
		public FuselageBuilder windshieldHeight(Amount<Length> windshieldHeight) {
			_windshieldHeight = windshieldHeight;
			return this;
		}
		
		public FuselageBuilder windshieldWidth(Amount<Length> windshieldWidth) {
			_windshieldWidth = windshieldWidth;
			return this;
		}
		
		public FuselageBuilder sectionCylinderLowerToTotalHeightRatio(Double sectionCylinderLowerToTotalHeightRatio) {
			_sectionCylinderLowerToTotalHeightRatio = sectionCylinderLowerToTotalHeightRatio;
			return this;
		}
		
		public FuselageBuilder sectionCylinderRhoUpper(Double sectionCylinderRhoUpper) {
			_sectionCylinderRhoUpper = sectionCylinderRhoUpper;
			return this;
		}
		
		public FuselageBuilder sectionCylinderRhoLower(Double sectionCylinderRhoLower) {
			_sectionCylinderRhoLower = sectionCylinderRhoLower;
			return this;
		}
		
		public FuselageBuilder sectionNoseMidLowerToTotalHeightRatio(Double sectionNoseMidLowerToTotalHeightRatio) {
			_sectionNoseMidLowerToTotalHeightRatio = sectionNoseMidLowerToTotalHeightRatio;
			return this;
		}
		
		public FuselageBuilder sectionTailMidLowerToTotalHeightRatio(Double sectionTailMidLowerToTotalHeightRatio) {
			_sectionTailMidLowerToTotalHeightRatio = sectionTailMidLowerToTotalHeightRatio;
			return this;
		}
		
		public FuselageBuilder sectionMidNoseRhoUpper(Double sectionMidNoseRhoUpper) {
			_sectionMidNoseRhoUpper = sectionMidNoseRhoUpper;
			return this;
		}
		
		public FuselageBuilder sectionMidTailRhoUpper(Double sectionMidTailRhoUpper) {
			_sectionMidTailRhoUpper = sectionMidTailRhoUpper;
			return this;
		}
		
		public FuselageBuilder sectionMidNoseRhoLower(Double sectionMidNoseRhoLower) {
			_sectionMidNoseRhoLower = sectionMidNoseRhoLower;
			return this;
		}
		
		public FuselageBuilder sectionMidTailRhoLower(Double sectionMidTailRhoLower) {
			_sectionMidTailRhoLower = sectionMidTailRhoLower;
			return this;
		}
		
		public Fuselage build() {
			return new Fuselage(this);
		}
	}

	private Fuselage(FuselageBuilder builder) {
		this.id = builder._id;
		this.deckNumber = builder._deckNumber;
		this.massReference = builder._massReference;
		this.pressurized = builder._pressurized;
		this.lenF = builder._lenF;
		this.lenRatioNF = builder._lenRatioNF;
		this.lenRatioCF = builder._lenRatioCF;
		this.sectionCylinderWidth = builder._sectionCylinderWidth;
		this.sectionCylinderHeight = builder._sectionCylinderHeight;
		this.lambdaN = builder._lambdaN;
		this.heightFromGround = builder._heightFromGround;
		this.roughness = builder._roughness;
		this.heightN = builder._heightN;
		this.heightT = builder._heightT;
		this.dxNoseCapPercent = builder._dxNoseCapPercent;
		this.dxTailCapPercent = builder._dxTailCapPercent;
		this.windshieldType = builder._windshieldType;
		this.windshieldHeight = builder._windshieldHeight;
		this.windshieldWidth = builder._windshieldWidth;
		this.sectionCylinderLowerToTotalHeightRatio = builder._sectionCylinderLowerToTotalHeightRatio;
		this.sectionCylinderRhoUpper = builder._sectionCylinderRhoUpper;
		this.sectionCylinderRhoLower = builder._sectionCylinderRhoLower;
		this.sectionNoseMidLowerToTotalHeightRatio = builder._sectionNoseMidLowerToTotalHeightRatio;
		this.sectionTailMidLowerToTotalHeightRatio = builder._sectionTailMidLowerToTotalHeightRatio;
		this.sectionMidNoseRhoUpper = builder._sectionMidNoseRhoUpper;
		this.sectionMidTailRhoUpper = builder._sectionMidTailRhoUpper;
		this.sectionMidNoseRhoLower = builder._sectionMidNoseRhoLower;
		this.sectionMidTailRhoLower = builder._sectionMidTailRhoLower;

		calculateGeometry();
	}

	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tFuselage\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + id + "'\n")
				.append("\tPressurized: '" + pressurized + "'\n")
				.append("\tNumber of decks: " + deckNumber + "\n")
				.append("\tMass reference: " + massReference + "\n")
				.append("\tRoughness: " + roughness + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tLength: " + lenF + "\n")
				.append("\tNose length: " + lenN + "\n")
				.append("\tCabin length: " + lenC + "\n")
				.append("\tTail length: " + lenT + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tNose length ratio: " + lenRatioNF + "\n")
				.append("\tCabin length ratio: " + lenRatioCF + "\n")
				.append("\tTail length ratio: " + lenRatioTF + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tCabin width: " + sectionCylinderWidth + "\n")
				.append("\tCabin height: " + sectionCylinderHeight + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tNose fineness ratio: " + lambdaN + "\n")
				.append("\tCabin fineness ratio: " + lambdaC + "\n")
				.append("\tTail fineness ratio: " + lambdaT + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tHeight from ground: " + heightFromGround + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tDiscretization\n")
				.append("\tNo. points (Nose/Cabin/Tail); " + npN + ", " + npC + ", " + npT + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
//
// TODO
//				dxNoseCapPercent;
//				dxTailCapPercent;
//				windshieldType;
//				windshieldHeight;
//				windshieldWidth;
//				sectionCylinderLowerToTotalHeightRatio;
//				sectionCylinderRhoUpper;
//				sectionCylinderRhoLower;
//				sectionNoseMidLowerToTotalHeightRatio;
//				sectionTailMidLowerToTotalHeightRatio;
//				sectionMidNoseRhoUpper;
//				sectionMidTailRhoUpper;
//				sectionMidNoseRhoLower;
//				sectionMidTailRhoLower;
//
// TODO add discretized data output
				;
		return sb.toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getPressurized() {
		return pressurized;
	}

	public void setPressurized(Boolean pressurized) {
		this.pressurized = pressurized;
	}

	public Amount<Length> getLenF() {
		return lenF;
	}

	public void setLenF(Amount<Length> lenF) {
		this.lenF = lenF;
	}

	public Amount<Length> getLenN() {
		return lenN;
	}

	public void setLenN(Amount<Length> lenN) {
		this.lenN = lenN;
	}

	public Amount<Length> getLenC() {
		return lenC;
	}

	public void setLenC(Amount<Length> lenC) {
		this.lenC = lenC;
	}

	public Amount<Length> getLenT() {
		return lenT;
	}

	public void setLenT(Amount<Length> lenT) {
		this.lenT = lenT;
	}

	public Amount<Length> getSectionCylinderHeight() {
		return sectionCylinderHeight;
	}

	public void setSectionCylinderHeight(Amount<Length> sectionCylinderHeight) {
		this.sectionCylinderHeight = sectionCylinderHeight;
	}

	public Amount<Length> getEquivalentDiameterCylinderGM() {
		return equivalentDiameterCylinderGM;
	}

	public void setEquivalentDiameterCylinderGM(Amount<Length> equivalentDiameterCylinderGM) {
		this.equivalentDiameterCylinderGM = equivalentDiameterCylinderGM;
	}

	public Amount<Length> getEquivalentDiameterGM() {
		return equivalentDiameterGM;
	}

	public void setEquivalentDiameterGM(Amount<Length> equivalentDiameterGM) {
		this.equivalentDiameterGM = equivalentDiameterGM;
	}

	public Amount<Length> getEquivalentDiameterCylinderAM() {
		return equivalentDiameterCylinderAM;
	}

	public void setEquivalentDiameterCylinderAM(Amount<Length> equivalentDiameterCylinderAM) {
		this.equivalentDiameterCylinderAM = equivalentDiameterCylinderAM;
	}

	public Amount<Area> getAreaC() {
		return areaC;
	}

	public void setAreaC(Amount<Area> areaC) {
		this.areaC = areaC;
	}

	public Amount<Area> getWindshieldArea() {
		return windshieldArea;
	}

	public void setWindshieldArea(Amount<Area> windshieldArea) {
		this.windshieldArea = windshieldArea;
	}

	public String getWindshieldType() {
		return windshieldType;
	}

	public void setWindshieldType(String windshieldType) {
		this.windshieldType = windshieldType;
	}

	public Amount<Area> getsWetNose() {
		return sWetNose;
	}

	public void setsWetNose(Amount<Area> sWetNose) {
		this.sWetNose = sWetNose;
	}

	public Amount<Area> getsWetTail() {
		return sWetTail;
	}

	public void setsWetTail(Amount<Area> sWetTail) {
		this.sWetTail = sWetTail;
	}

	public Amount<Area> getsWetC() {
		return sWetC;
	}

	public void setsWetC(Amount<Area> sWetC) {
		this.sWetC = sWetC;
	}

	public Amount<Area> getsFront() {
		return sFront;
	}

	public void setsFront(Amount<Area> sFront) {
		this.sFront = sFront;
	}

	public Amount<Area> getsWet() {
		return sWet;
	}

	public void setsWet(Amount<Area> sWet) {
		this.sWet = sWet;
	}

	public Amount<Length> getHeightFromGround() {
		return heightFromGround;
	}

	public void setHeightFromGround(Amount<Length> heightFromGround) {
		this.heightFromGround = heightFromGround;
	}

	public Amount<Angle> getPhi1() {
		return phi1;
	}

	public void setPhi1(Amount<Angle> phi1) {
		this.phi1 = phi1;
	}

	public Amount<Angle> getPhi2() {
		return phi2;
	}

	public void setPhi2(Amount<Angle> phi2) {
		this.phi2 = phi2;
	}

	public Amount<Angle> getPhi3() {
		return phi3;
	}

	public void setPhi3(Amount<Angle> phi3) {
		this.phi3 = phi3;
	}

	public Amount<Angle> getPhiN() {
		return phiN;
	}

	public void setPhiN(Amount<Angle> phiN) {
		this.phiN = phiN;
	}

	public Amount<Angle> getPhiT() {
		return phiT;
	}

	public void setPhiT(Amount<Angle> phiT) {
		this.phiT = phiT;
	}

	public Amount<Length> getHeightN() {
		return heightN;
	}

	public void setHeightN(Amount<Length> heightN) {
		this.heightN = heightN;
	}

	public Amount<Length> getHeightT() {
		return heightT;
	}

	public void setHeightT(Amount<Length> heightT) {
		this.heightT = heightT;
	}

	public Amount<Angle> getUpsweepAngle() {
		return upsweepAngle;
	}

	public void setUpsweepAngle(Amount<Angle> upsweepAngle) {
		this.upsweepAngle = upsweepAngle;
	}

	public Amount<Angle> getWindshieldAngle() {
		return windshieldAngle;
	}

	public void setWindshieldAngle(Amount<Angle> windshieldAngle) {
		this.windshieldAngle = windshieldAngle;
	}

	public Amount<Length> getRoughness() {
		return roughness;
	}

	public void setRoughness(Amount<Length> roughness) {
		this.roughness = roughness;
	}

	public Double getLambdaF() {
		return lambdaF;
	}

	public void setLambdaF(Double lambdaF) {
		this.lambdaF = lambdaF;
	}

	public Double getLambdaN() {
		return lambdaN;
	}

	public void setLambdaN(Double lambdaN) {
		this.lambdaN = lambdaN;
	}

	public Double getLambdaC() {
		return lambdaC;
	}

	public void setLambdaC(Double lambdaC) {
		this.lambdaC = lambdaC;
	}

	public Double getLambdaT() {
		return lambdaT;
	}

	public void setLambdaT(Double lambdaT) {
		this.lambdaT = lambdaT;
	}

	public Double getLenRatioNF() {
		return lenRatioNF;
	}

	public void setLenRatioNF(Double lenRatioNF) {
		this.lenRatioNF = lenRatioNF;
	}

	public Double getLenRatioCF() {
		return lenRatioCF;
	}

	public void setLenRatioCF(Double lenRatioCF) {
		this.lenRatioCF = lenRatioCF;
	}

	public Double getLenRatioTF() {
		return lenRatioTF;
	}

	public void setLenRatioTF(Double lenRatioTF) {
		this.lenRatioTF = lenRatioTF;
	}

	public Double getFormFactor() {
		return formFactor;
	}

	public void setFormFactor(Double formFactor) {
		this.formFactor = formFactor;
	}

	public Amount<Length> getSectionCylinderWidth() {
		return sectionCylinderWidth;
	}

	public void setSectionCylinderWidth(Amount<Length> sectionCylinderWidth) {
		this.sectionCylinderWidth = sectionCylinderWidth;
	}

	public Amount<Length> getWindshieldHeight() {
		return windshieldHeight;
	}

	public void setWindshieldHeight(Amount<Length> windshieldHeight) {
		this.windshieldHeight = windshieldHeight;
	}

	public Amount<Length> getWindshieldWidth() {
		return windshieldWidth;
	}

	public void setWindshieldWidth(Amount<Length> windshieldWidth) {
		this.windshieldWidth = windshieldWidth;
	}

	public Amount<Length> getDxNoseCap() {
		return dxNoseCap;
	}

	public void setDxNoseCap(Amount<Length> dxNoseCap) {
		this.dxNoseCap = dxNoseCap;
	}

	public Amount<Length> getDxTailCap() {
		return dxTailCap;
	}

	public void setDxTailCap(Amount<Length> dxTailCap) {
		this.dxTailCap = dxTailCap;
	}

	public Double getSectionCylinderLowerToTotalHeightRatio() {
		return sectionCylinderLowerToTotalHeightRatio;
	}

	public void setSectionCylinderLowerToTotalHeightRatio(Double sectionCylinderLowerToTotalHeightRatio) {
		this.sectionCylinderLowerToTotalHeightRatio = sectionCylinderLowerToTotalHeightRatio;
	}

	public Double getSectionNoseMidLowerToTotalHeightRatio() {
		return sectionNoseMidLowerToTotalHeightRatio;
	}

	public void setSectionNoseMidLowerToTotalHeightRatio(Double sectionNoseMidLowerToTotalHeightRatio) {
		this.sectionNoseMidLowerToTotalHeightRatio = sectionNoseMidLowerToTotalHeightRatio;
	}

	public Double getSectionTailMidLowerToTotalHeightRatio() {
		return sectionTailMidLowerToTotalHeightRatio;
	}

	public void setSectionTailMidLowerToTotalHeightRatio(Double sectionTailMidLowerToTotalHeightRatio) {
		this.sectionTailMidLowerToTotalHeightRatio = sectionTailMidLowerToTotalHeightRatio;
	}

	public Double getSectionCylinderRhoUpper() {
		return sectionCylinderRhoUpper;
	}

	public void setSectionCylinderRhoUpper(Double sectionCylinderRhoUpper) {
		this.sectionCylinderRhoUpper = sectionCylinderRhoUpper;
	}

	public Double getSectionCylinderRhoLower() {
		return sectionCylinderRhoLower;
	}

	public void setSectionCylinderRhoLower(Double sectionCylinderRhoLower) {
		this.sectionCylinderRhoLower = sectionCylinderRhoLower;
	}

	public Double getSectionMidNoseRhoUpper() {
		return sectionMidNoseRhoUpper;
	}

	public void setSectionMidNoseRhoUpper(Double sectionMidNoseRhoUpper) {
		this.sectionMidNoseRhoUpper = sectionMidNoseRhoUpper;
	}

	public Double getSectionMidNoseRhoLower() {
		return sectionMidNoseRhoLower;
	}

	public void setSectionMidNoseRhoLower(Double sectionMidNoseRhoLower) {
		this.sectionMidNoseRhoLower = sectionMidNoseRhoLower;
	}

	public Double getSectionMidTailRhoUpper() {
		return sectionMidTailRhoUpper;
	}

	public void setSectionMidTailRhoUpper(Double sectionMidTailRhoUpper) {
		this.sectionMidTailRhoUpper = sectionMidTailRhoUpper;
	}

	public Double getSectionMidTailRhoLower() {
		return sectionMidTailRhoLower;
	}

	public void setSectionMidTailRhoLower(Double sectionMidTailRhoLower) {
		this.sectionMidTailRhoLower = sectionMidTailRhoLower;
	}

	public int getNpN() {
		return npN;
	}

	public void setNpN(int npN) {
		this.npN = npN;
	}

	public int getNpC() {
		return npC;
	}

	public void setNpC(int npC) {
		this.npC = npC;
	}

	public int getNpT() {
		return npT;
	}

	public void setNpT(int npT) {
		this.npT = npT;
	}

	public int getNpSecUp() {
		return npSecUp;
	}

	public void setNpSecUp(int npSecUp) {
		this.npSecUp = npSecUp;
	}

	public int getNpSecLow() {
		return npSecLow;
	}

	public void setNpSecLow(int npSecLow) {
		this.npSecLow = npSecLow;
	}

	public double getDeltaXNose() {
		return deltaXNose;
	}

	public void setDeltaXNose(double deltaXNose) {
		this.deltaXNose = deltaXNose;
	}

	public double getDeltaXCylinder() {
		return deltaXCylinder;
	}

	public void setDeltaXCylinder(double deltaXCylinder) {
		this.deltaXCylinder = deltaXCylinder;
	}

	public double getDeltaXTail() {
		return deltaXTail;
	}

	public void setDeltaXTail(double deltaXTail) {
		this.deltaXTail = deltaXTail;
	}

	public double getDxNoseCapPercent() {
		return dxNoseCapPercent;
	}

	public void setDxNoseCapPercent(double dxNoseCapPercent) {
		this.dxNoseCapPercent = dxNoseCapPercent;
	}

	public double getDxTailCapPercent() {
		return dxTailCapPercent;
	}

	public void setDxTailCapPercent(double dxTailCapPercent) {
		this.dxTailCapPercent = dxTailCapPercent;
	}

	public List<Double> getOutlineXZUpperCurveX() {
		return outlineXZUpperCurveX;
	}

	public void setOutlineXZUpperCurveX(List<Double> outlineXZUpperCurveX) {
		this.outlineXZUpperCurveX = outlineXZUpperCurveX;
	}

	public List<Double> getOutlineXZUpperCurveZ() {
		return outlineXZUpperCurveZ;
	}

	public void setOutlineXZUpperCurveZ(List<Double> outlineXZUpperCurveZ) {
		this.outlineXZUpperCurveZ = outlineXZUpperCurveZ;
	}

	public List<Double> getOutlineXZLowerCurveX() {
		return outlineXZLowerCurveX;
	}

	public void setOutlineXZLowerCurveX(List<Double> outlineXZLowerCurveX) {
		this.outlineXZLowerCurveX = outlineXZLowerCurveX;
	}

	public List<Double> getOutlineXZLowerCurveZ() {
		return outlineXZLowerCurveZ;
	}

	public void setOutlineXZLowerCurveZ(List<Double> outlineXZLowerCurveZ) {
		this.outlineXZLowerCurveZ = outlineXZLowerCurveZ;
	}

	public List<Double> getOutlineXZCamberLineX() {
		return outlineXZCamberLineX;
	}

	public void setOutlineXZCamberLineX(List<Double> outlineXZCamberLineX) {
		this.outlineXZCamberLineX = outlineXZCamberLineX;
	}

	public List<Double> getOutlineXZCamberLineZ() {
		return outlineXZCamberLineZ;
	}

	public void setOutlineXZCamberLineZ(List<Double> outlineXZCamberLineZ) {
		this.outlineXZCamberLineZ = outlineXZCamberLineZ;
	}

	public List<Double> getOutlineXYSideRCurveX() {
		return outlineXYSideRCurveX;
	}

	public void setOutlineXYSideRCurveX(List<Double> outlineXYSideRCurveX) {
		this.outlineXYSideRCurveX = outlineXYSideRCurveX;
	}

	public List<Double> getOutlineXYSideRCurveY() {
		return outlineXYSideRCurveY;
	}

	public void setOutlineXYSideRCurveY(List<Double> outlineXYSideRCurveY) {
		this.outlineXYSideRCurveY = outlineXYSideRCurveY;
	}

	public List<Double> getOutlineXYSideRCurveZ() {
		return outlineXYSideRCurveZ;
	}

	public void setOutlineXYSideRCurveZ(List<Double> outlineXYSideRCurveZ) {
		this.outlineXYSideRCurveZ = outlineXYSideRCurveZ;
	}

	public List<Double> getOutlineXYSideLCurveX() {
		return outlineXYSideLCurveX;
	}

	public void setOutlineXYSideLCurveX(List<Double> outlineXYSideLCurveX) {
		this.outlineXYSideLCurveX = outlineXYSideLCurveX;
	}

	public List<Double> getOutlineXYSideLCurveY() {
		return outlineXYSideLCurveY;
	}

	public void setOutlineXYSideLCurveY(List<Double> outlineXYSideLCurveY) {
		this.outlineXYSideLCurveY = outlineXYSideLCurveY;
	}

	public List<Double> getOutlineXYSideLCurveZ() {
		return outlineXYSideLCurveZ;
	}

	public void setOutlineXYSideLCurveZ(List<Double> outlineXYSideLCurveZ) {
		this.outlineXYSideLCurveZ = outlineXYSideLCurveZ;
	}

	public List<Double> getSectionUpperCurveY() {
		return sectionUpperCurveY;
	}

	public void setSectionUpperCurveY(List<Double> sectionUpperCurveY) {
		this.sectionUpperCurveY = sectionUpperCurveY;
	}

	public List<Double> getSectionUpperCurveZ() {
		return sectionUpperCurveZ;
	}

	public void setSectionUpperCurveZ(List<Double> sectionUpperCurveZ) {
		this.sectionUpperCurveZ = sectionUpperCurveZ;
	}

	public List<Double> getSectionLowerCurveY() {
		return sectionLowerCurveY;
	}

	public void setSectionLowerCurveY(List<Double> sectionLowerCurveY) {
		this.sectionLowerCurveY = sectionLowerCurveY;
	}

	public List<Double> getSectionLowerCurveZ() {
		return sectionLowerCurveZ;
	}

	public void setSectionLowerCurveZ(List<Double> sectionLowerCurveZ) {
		this.sectionLowerCurveZ = sectionLowerCurveZ;
	}

	public List<MyFuselageCurvesSection> getSectionsYZ() {
		return sectionsYZ;
	}

	public void setSectionsYZ(List<MyFuselageCurvesSection> sectionsYZ) {
		this.sectionsYZ = sectionsYZ;
	}

	public List<Amount<Length>> getSectionsYZStations() {
		return sectionsYZStations;
	}

	public void setSectionsYZStations(List<Amount<Length>> sectionsYZStations) {
		this.sectionsYZStations = sectionsYZStations;
	}

	public List<List<Double>> getSectionUpperCurvesY() {
		return sectionUpperCurvesY;
	}

	public void setSectionUpperCurvesY(List<List<Double>> sectionUpperCurvesY) {
		this.sectionUpperCurvesY = sectionUpperCurvesY;
	}

	public List<List<Double>> getSectionUpperCurvesZ() {
		return sectionUpperCurvesZ;
	}

	public void setSectionUpperCurvesZ(List<List<Double>> sectionUpperCurvesZ) {
		this.sectionUpperCurvesZ = sectionUpperCurvesZ;
	}

	public List<List<Double>> getSectionLowerCurvesY() {
		return sectionLowerCurvesY;
	}

	public void setSectionLowerCurvesY(List<List<Double>> sectionLowerCurvesY) {
		this.sectionLowerCurvesY = sectionLowerCurvesY;
	}

	public List<List<Double>> getSectionLowerCurvesZ() {
		return sectionLowerCurvesZ;
	}

	public void setSectionLowerCurvesZ(List<List<Double>> sectionLowerCurvesZ) {
		this.sectionLowerCurvesZ = sectionLowerCurvesZ;
	}

	public int getIDX_SECTION_YZ_NOSE_TIP() {
		return IDX_SECTION_YZ_NOSE_TIP;
	}

	public int getIDX_SECTION_YZ_NOSE_CAP() {
		return IDX_SECTION_YZ_NOSE_CAP;
	}

	public int getIDX_SECTION_YZ_MID_NOSE() {
		return IDX_SECTION_YZ_MID_NOSE;
	}

	public int getIDX_SECTION_YZ_CYLINDER_1() {
		return IDX_SECTION_YZ_CYLINDER_1;
	}

	public int getIDX_SECTION_YZ_CYLINDER_2() {
		return IDX_SECTION_YZ_CYLINDER_2;
	}

	public int getIDX_SECTION_YZ_MID_TAIL() {
		return IDX_SECTION_YZ_MID_TAIL;
	}

	public int getIDX_SECTION_YZ_TAIL_CAP() {
		return IDX_SECTION_YZ_TAIL_CAP;
	}

	public int getIDX_SECTION_YZ_TAIL_TIP() {
		return IDX_SECTION_YZ_TAIL_TIP;
	}

	public int getNUM_SECTIONS_YZ() {
		return NUM_SECTIONS_YZ;
	}
}
