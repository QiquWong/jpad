package aircraft.components.fuselage.creator;

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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.solvers.AllowedSolution;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.components.fuselage.FuselageCurvesSection;
import aircraft.components.fuselage.FuselageCurvesSideView;
import aircraft.components.fuselage.FuselageCurvesUpperView;
import aircraft.components.liftingSurface.creator.SpoilerCreator;
import configuration.MyConfiguration;
import configuration.enumerations.FuselageAdjustCriteriaEnum;
import configuration.enumerations.WindshieldTypeEnum;
import processing.core.PVector;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyXMLReaderUtils;

public class FuselageCreator {

	//----------------------------------------------------------------------
	// VARIABLE DECLARATION:
	private IFuselageCreator theFuselageCreatorInterface;
	// calculate geometry parameters
	private int npN = 10, npC = 4, npT = 10, npSecUp = 10, npSecLow = 10;
	public final int IDX_SECTION_YZ_NOSE_TIP   = 0;
	public final int IDX_SECTION_YZ_NOSE_CAP   = 1;
	public final int IDX_SECTION_YZ_MID_NOSE   = 2;
	public final int IDX_SECTION_YZ_CYLINDER_1 = 3;
	public final int IDX_SECTION_YZ_CYLINDER_2 = 4;
	public final int IDX_SECTION_YZ_MID_TAIL   = 5;
	public final int IDX_SECTION_YZ_TAIL_CAP   = 6;
	public final int IDX_SECTION_YZ_TAIL_TIP   = 7;
	public final int NUM_SECTIONS_YZ           = 8;
	
	//----------------------------------------------------------------------
	// DERIVED INPUT DATA
	// GM = Geometric Mean, RMS = Root Mean Square, AM = Arithmetic Mean
	private Amount<Length> equivalentDiameterCylinderGM, equivalentDiameterGM,	equivalentDiameterCylinderAM;
	private Amount<Length> lengthNose;
	private Amount<Length> lengthCylinder;
	private Amount<Length> lengthTail;
	private Amount<Area> cylinderSectionArea;  //cylindrical section base area
	private Amount<Area> windshieldArea;
	private Amount<Area> sWetNose;
	private Amount<Area> sWetTail;
	private Amount<Area> sWetCylinder;
	private Amount<Area> frontSurface; 
	private Amount<Area> sWetTotal;
	private Amount<Angle> phiNose, phiTail;
	private Amount<Angle> upsweepAngle, windshieldAngle;
	private Amount<Length> noseCapOffset, tailCapOffset;
	private double fuselageFinenessRatio;
	private double noseFinenessRatio;
	private double cylinderFinenessRatio;
	private double tailFinenessRatio;
	private double tailLengthRatio;
	private double formFactor;
	private double deltaXNose, deltaXCylinder, deltaXTail;
	
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
	
	private List<FuselageCurvesSection> sectionsYZ = new ArrayList<FuselageCurvesSection>();
	List<Amount<Length> > sectionsYZStations = new ArrayList<Amount<Length>>();
	List<List<Double>> sectionUpperCurvesY = new ArrayList<List<Double>>();
	List<List<Double>> sectionUpperCurvesZ = new ArrayList<List<Double>>();
	List<List<Double>> sectionLowerCurvesY = new ArrayList<List<Double>>();
	List<List<Double>> sectionLowerCurvesZ = new ArrayList<List<Double>>();
	
	//----------------------------------------------------------------------
	// OUTPUT DATA (TO BE MOVED)
	private Amount<Mass> massReference;
	
	//------------------------------------------------------------------------------------------
	// BUILDER 
	public FuselageCreator (IFuselageCreator theFuselageCreatorInterface) {
		
		this.theFuselageCreatorInterface = theFuselageCreatorInterface;
		calculateGeometry();
		
	}
	
	//------------------------------------------------------------------------------------------
	// METHODS
	public void calculateGeometry(int np_N, int np_C, int np_T, int np_SecUp, int np_SecLow) {
		npN           = np_N;
		npC           = np_C;
		npT           = np_T;
		double lN = lengthNose.doubleValue(SI.METRE);
		double lC = lengthCylinder.doubleValue(SI.METRE);
		double lT = lengthTail.doubleValue(SI.METRE);
		deltaXNose     = lN/(npN-1);
		deltaXCylinder = lC/(npC-1);
		deltaXTail     = lT/(npT-1);
		npSecUp  = np_SecUp;
		npSecLow = np_SecLow;

		// clean all points before recalculating
		clearOutlines();
		calculateGeometry();
	}

	public void calculateGeometry() {

		tailLengthRatio = 1.0 - theFuselageCreatorInterface.getCylinderLengthRatio() - theFuselageCreatorInterface.getNoseLengthRatio();

		lengthNose = Amount.valueOf( theFuselageCreatorInterface.getNoseLengthRatio() * theFuselageCreatorInterface.getFuselageLength().doubleValue(SI.METRE), SI.METRE);
		lengthCylinder = Amount.valueOf( theFuselageCreatorInterface.getCylinderLengthRatio() * theFuselageCreatorInterface.getFuselageLength().doubleValue(SI.METRE), SI.METRE);
		lengthTail = Amount.valueOf( tailLengthRatio * theFuselageCreatorInterface.getFuselageLength().doubleValue(SI.METRE), SI.METRE);
		
		// Equivalent diameters
		equivalentDiameterCylinderGM = Amount.valueOf(
				Math.sqrt(theFuselageCreatorInterface.getSectionCylinderWidth().doubleValue(SI.METER)
						*theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METER)),
				SI.METRE);

		equivalentDiameterCylinderAM = Amount.valueOf(
				MyMathUtils.arithmeticMean(
						theFuselageCreatorInterface.getSectionCylinderWidth().doubleValue(SI.METER),
						theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METER)
						),
				SI.METRE
				);
		
		// Fineness ratios
		noseFinenessRatio = lengthNose.doubleValue(SI.METER)/equivalentDiameterCylinderGM.doubleValue(SI.METER);
		cylinderFinenessRatio = lengthCylinder.doubleValue(SI.METRE)/equivalentDiameterCylinderGM.doubleValue(SI.METRE); 
		tailFinenessRatio = lengthTail.doubleValue(SI.METRE)/equivalentDiameterCylinderGM.doubleValue(SI.METRE); 
		fuselageFinenessRatio = theFuselageCreatorInterface.getFuselageLength().doubleValue(SI.METER)/equivalentDiameterCylinderGM.doubleValue(SI.METER); 
		
		noseCapOffset = Amount.valueOf(lengthNose.times(theFuselageCreatorInterface.getNoseCapOffsetPercent()).doubleValue(SI.METRE), SI.METRE);
		tailCapOffset = Amount.valueOf(lengthTail.times(theFuselageCreatorInterface.getTailCapOffsetPercent()).doubleValue(SI.METRE), SI.METRE);

		windshieldArea = Amount.valueOf(
				theFuselageCreatorInterface.getWindshieldHeight().doubleValue(SI.METER)
				*theFuselageCreatorInterface.getWindshieldWidth().doubleValue(SI.METER),
				SI.SQUARE_METRE
				);

		phiNose = Amount.valueOf(
				Math.atan(
						(theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METRE) 
								- theFuselageCreatorInterface.getNoseTipOffset().doubleValue(SI.METRE))
						/ lengthNose.doubleValue(SI.METRE)
						),
						SI.RADIAN);

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

		equivalentDiameterGM = Amount.valueOf(calculateEquivalentDiameter(), SI.METRE);
		
		// cylindrical section base area
		cylinderSectionArea = Amount.valueOf(
				Math.PI *(
						theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METER)
						*theFuselageCreatorInterface.getSectionCylinderWidth().doubleValue(SI.METER)
						)
				/4,
				SI.SQUARE_METRE
				);

		calculateSwet("Stanford");

		// Form factor Kff
		formFactor =  calculateFormFactor(fuselageFinenessRatio);

		calculateUpsweepAngle();
		calculateWindshieldAngle(); 

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
	public void calculateOutlines(int np_N, int np_C, int np_T, int np_SecUp, int np_SecLow){

		// calculate initial curves
		// get variables

		double lF = theFuselageCreatorInterface.getFuselageLength().doubleValue(SI.METER);
		double lN = lengthNose.doubleValue(SI.METER);
		double lC = lengthCylinder.doubleValue(SI.METER);
		double lT = lengthTail.doubleValue(SI.METER);
		double dC = theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METER);
		double hN = theFuselageCreatorInterface.getNoseTipOffset().doubleValue(SI.METER); // FuselageCreator origin O_T at nose (>0, when below the cylindrical midline)
		double hT = theFuselageCreatorInterface.getTailTipOffest().doubleValue(SI.METER);
		double wB = theFuselageCreatorInterface.getSectionCylinderWidth().doubleValue(SI.METER);
		double a  = theFuselageCreatorInterface.getSectionCylinderLowerToTotalHeightRatio();
		double rhoUpper = theFuselageCreatorInterface.getSectionCylinderRhoUpper();
		double rhoLower = theFuselageCreatorInterface.getSectionCylinderRhoLower();

		npN           = np_N;
		npC           = np_C;
		npT           = np_T;
		deltaXNose     = lN/(npN-1);
		deltaXCylinder = lC/(npC-1);
		deltaXTail     = lT/(npT-1);
		npSecUp  = np_SecUp;
		npSecLow = np_SecLow;

		// clean all points before recalculating
		clearOutlines();

		//------------------------------------------------
		// XZ VIEW -- Side View
		//------------------------------------------------

		FuselageCurvesSideView fuselageCurvesSideView = new FuselageCurvesSideView(
				lN, hN, lC, lF, hT, dC/2, a, // lengths
				npN, npC, npT        // no. points (nose, cylinder, tail)
				);

		// UPPER CURVES ----------------------------------

		// UPPER NOSE
		for (int i = 0; i < fuselageCurvesSideView.getNoseUpperPoints().size(); i++){
			outlineXZUpperCurveX.add((double) fuselageCurvesSideView.getNoseUpperPoints().get(i).x);
			outlineXZUpperCurveZ.add((double) fuselageCurvesSideView.getNoseUpperPoints().get(i).y);
		}

		// UPPER CYLINDER
		for (int i = 1; i < fuselageCurvesSideView.getCylinderUpperPoints().size(); i++){
			outlineXZUpperCurveX.add((double) fuselageCurvesSideView.getCylinderUpperPoints().get(i).x);
			outlineXZUpperCurveZ.add((double) fuselageCurvesSideView.getCylinderUpperPoints().get(i).y);
		}

		// UPPER TAIL
		for (int i = 1; i < fuselageCurvesSideView.getTailUpperPoints().size(); i++){
			outlineXZUpperCurveX.add((double) fuselageCurvesSideView.getTailUpperPoints().get(i).x);
			outlineXZUpperCurveZ.add((double) fuselageCurvesSideView.getTailUpperPoints().get(i).y);
		}

		// LOWER CURVES ----------------------------------

		// LOWER NOSE
		for (int i = 0; i < fuselageCurvesSideView.getNoseLowerPoints().size(); i++){
			outlineXZLowerCurveX.add((double) fuselageCurvesSideView.getNoseLowerPoints().get(i).x);
			outlineXZLowerCurveZ.add((double) fuselageCurvesSideView.getNoseLowerPoints().get(i).y);
		}

		// LOWER CYLINDER
		for (int i = 1; i< fuselageCurvesSideView.getCylinderLowerPoints().size(); i++){
			outlineXZLowerCurveX.add((double) fuselageCurvesSideView.getCylinderLowerPoints().get(i).x);
			outlineXZLowerCurveZ.add((double) fuselageCurvesSideView.getCylinderLowerPoints().get(i).y);
		}

		// LOWER TAIL
		for (int i = 1; i < fuselageCurvesSideView.getTailLowerPoints().size(); i++)
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
		for (int i = 0; i < fuselageCurvesSideView.getCylinderCamberlinePoints().size(); i++){
			outlineXZCamberLineX.add((double) fuselageCurvesSideView.getCylinderCamberlinePoints().get(i).x);
			outlineXZCamberLineZ.add((double) fuselageCurvesSideView.getCylinderCamberlinePoints().get(i).y);
		}

		//  TAIL CAMBER LINE
		for (int i = 1; i < fuselageCurvesSideView.getTailCamberlinePoints().size(); i++){
			outlineXZCamberLineX.add((double) fuselageCurvesSideView.getTailCamberlinePoints().get(i).x);
			outlineXZCamberLineZ.add((double) fuselageCurvesSideView.getTailCamberlinePoints().get(i).y);
		}

		//------------------------------------------------
		// XY VIEW -- Upper View
		//------------------------------------------------
		FuselageCurvesUpperView fuselageCurvesUpperView = new FuselageCurvesUpperView(
				lN, lC, lF, wB/2, // lengths
				npN, npC, npT   // no. points (nose, cylinder, tail)
				);

		// RIGHT CURVE -----------------------------------

		// RIGHT NOSE
		for (int i=0; i<fuselageCurvesUpperView.getNoseUpperPoints().size(); i++){
			outlineXYSideRCurveX.add((double) fuselageCurvesUpperView.getNoseUpperPoints().get(i).x);
			outlineXYSideRCurveY.add((double) fuselageCurvesUpperView.getNoseUpperPoints().get(i).y);
		}

		// RIGHT CYLINDER
		for (int i=1; i<fuselageCurvesUpperView.getCylinderUpperPoints().size(); i++){
			outlineXYSideRCurveX.add((double) fuselageCurvesUpperView.getCylinderUpperPoints().get(i).x);
			outlineXYSideRCurveY.add((double) fuselageCurvesUpperView.getCylinderUpperPoints().get(i).y);
		}

		// RIGHT TAIL
		for (int i=1; i<fuselageCurvesUpperView.getTailUpperPoints().size(); i++){
			outlineXYSideRCurveX.add((double) fuselageCurvesUpperView.getTailUpperPoints().get(i).x);
			outlineXYSideRCurveY.add((double) fuselageCurvesUpperView.getTailUpperPoints().get(i).y);
		}

		//------------------------------------------------
		// YZ VIEW -- Section/Front view
		//------------------------------------------------

		FuselageCurvesSection fuselageCurvesSection = new FuselageCurvesSection(
				wB, dC, a, rhoUpper, rhoLower, // lengths
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
		double x  = 0.;//_dxNoseCap.doubleValue(SI.METRE)  ; // NOTE
		double hf =  Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		double wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		sectionsYZ.add(
				new FuselageCurvesSection(
						wf, hf, 0.5, 0.0, 0.0, // lengths & parameters
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(Amount.valueOf(0.0,SI.METRE));

		// NOSE CAP
		// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
		x  = noseCapOffset.doubleValue(SI.METRE); // NOTE
		hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		sectionsYZ.add(
				new FuselageCurvesSection(
						wf, hf, 0.5, 0.0, 0.0, // lengths & parameters
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// MID-NOSE
		// IDX_SECTION_YZ_MID_NOSE
		x  =  0.5*lengthNose.doubleValue(SI.METRE);
		hf =  Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		sectionsYZ.add(
				new FuselageCurvesSection(
						wf,
						hf,
						theFuselageCreatorInterface.getSectionNoseMidLowerToTotalHeightRatio(), 
						theFuselageCreatorInterface.getSectionMidNoseRhoUpper(),
						theFuselageCreatorInterface.getSectionMidNoseRhoLower(),
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// CYLINDER 1
		// IDX_SECTION_YZ_CYLINDER
		x  =  lengthNose.doubleValue(SI.METRE);
		wf =  theFuselageCreatorInterface.getSectionCylinderWidth().doubleValue(SI.METRE);
		hf =  theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METRE);
		sectionsYZ.add(
				new FuselageCurvesSection(
						wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// CYLINDER 2
		// IDX_SECTION_YZ_CYLINDER
		x  =  lengthNose.doubleValue(SI.METRE) + lengthCylinder.doubleValue(SI.METRE);
		wf =  theFuselageCreatorInterface.getSectionCylinderWidth().doubleValue(SI.METRE);
		hf =  theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METRE);
		sectionsYZ.add(
				new FuselageCurvesSection(
						wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// MID-TAIL
		// IDX_SECTION_YZ_MID_TAIL
		x = theFuselageCreatorInterface.getFuselageLength().doubleValue(SI.METRE) - 0.5*lengthTail.doubleValue(SI.METRE);
		hf =  Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		sectionsYZ.add(
				new FuselageCurvesSection(
						wf, 
						hf, 
						theFuselageCreatorInterface.getSectionTailMidLowerToTotalHeightRatio(),
						theFuselageCreatorInterface.getSectionMidTailRhoUpper(),
						theFuselageCreatorInterface.getSectionMidTailRhoLower(), 
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// TAIL CAP
		// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
		x =  theFuselageCreatorInterface.getFuselageLength().doubleValue(SI.METRE) - tailCapOffset.doubleValue(SI.METRE);
		hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		sectionsYZ.add(
				new FuselageCurvesSection(
						wf, hf, 0.5, 0.0, 0.0, // lengths & parameters
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(Amount.valueOf(x,SI.METRE));

		// TAIL TIP
		// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
		x =  theFuselageCreatorInterface.getFuselageLength().times(0.999995).doubleValue(SI.METRE);// - _dxTailCap.doubleValue(SI.METRE);
		hf =  Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ));
		wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ));
		sectionsYZ.add(
				new FuselageCurvesSection(
						wf, hf, 0.5, 0.0, 0.0, // lengths & parameters
						npSecUp, npSecLow  // no. points (nose, cylinder, tail)
						)
				.translateZ(this.getZOutlineXZLowerAtX(x) + 0.5*hf)
				);
		sectionsYZStations.add(theFuselageCreatorInterface.getFuselageLength().to(SI.METER));

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

		}

		updateCurveSections();

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

	}
	
	/** Return equivalent diameter of entire fuselage */
	public double calculateEquivalentDiameter(){

		// BEWARE: Gtmat library starts indexing arrays from 1!
		// To workaround this problem use .data to extract a double[] array
		double[] x = MyArrayUtils.linspace(0., theFuselageCreatorInterface.getFuselageLength().doubleValue(SI.METER)*(1-0.0001), 200);

		return MyMathUtils.arithmeticMean((getEquivalentDiameterAtX(x)));

	}
	
	public double getCamberAngleAtX(double x) {
		if (x<= this.getNoseLength().getEstimatedValue()) return Math.atan(getCamberZAtX(x)/x); 
		if (x>= this.getCylinderLength().getEstimatedValue()) return Math.atan(-getCamberZAtX(x)/x);
		return 0.;
	}

	/** Return Camber z-coordinate at x-coordinate */
	public double getCamberZAtX(double x) {
		double zUp = getZOutlineXZUpperAtX(x);
		double zDown = getZOutlineXZLowerAtX(x);
		return zUp/2 + zDown/2;
	}

	/** Return equivalent diameter at x-coordinate */
	public double getEquivalentDiameterAtX(double x) {

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

	//	Return width at x-coordinate
	public double getSectionWidthAtZ(double z) {
		return -2*getYOutlineYZSectionRightCurveAtZ(z);
	}
	
	public double getYOutlineYZSectionRightCurveAtZ(double z) {
	
		List<Double> outlineYZSideRCurveY = new ArrayList<Double>();
		List<Double> outlineYZSideRCurveZ = new ArrayList<Double>();
		
		for (int i = 0; i <= getSectionsYZ().get(IDX_SECTION_YZ_CYLINDER_1).getSectionLowerRightPoints().size() - 1; i++){
			outlineYZSideRCurveY.add(
					(double) getSectionsYZ().get(IDX_SECTION_YZ_CYLINDER_1).getSectionLowerRightPoints().get(i).x
					);
			outlineYZSideRCurveZ.add(
					(double) getSectionsYZ().get(IDX_SECTION_YZ_CYLINDER_1).getSectionLowerRightPoints().get(i).y
					);
		}
		
		for (int i = 1; i <= getSectionsYZ().get(IDX_SECTION_YZ_CYLINDER_1).getSectionUpperRightPoints().size() - 1; i++){
			outlineYZSideRCurveY.add(
					(double) getSectionsYZ().get(IDX_SECTION_YZ_CYLINDER_1).getSectionUpperRightPoints().get(i).x
					);
			outlineYZSideRCurveZ.add(
					(double) getSectionsYZ().get(IDX_SECTION_YZ_CYLINDER_1).getSectionUpperRightPoints().get(i).y
					);
		}
		
		double vyu[] = new double[outlineYZSideRCurveY.size()];
		double vzu[] = new double[outlineYZSideRCurveZ.size()];
		
		for (int i = 0; i < vzu.length; i++)
		{
			vyu[i] = outlineYZSideRCurveY.get(i);
			vzu[i] = outlineYZSideRCurveZ.get(i);
		}
		
		// interpolation - lower
		UnivariateInterpolator interpolatorSectionRightCurve = new SplineInterpolator();
		UnivariateFunction myInterpolationFunctionUpper =
				interpolatorSectionRightCurve.interpolate(vzu, vyu);

		// section y-coordinates at z
		double ySection = 0.0;
		if (z < vzu[0]) {
			ySection = vyu[0];
		}
		if (z > vzu[vzu.length-1]) {
			ySection = vyu[vyu.length-1];
		}
		if ((z >= vzu[0]) && (z <= vzu[vzu.length-1])){
			ySection = myInterpolationFunctionUpper.value(z);
		}
		return ySection;
	}
	
	//  Return width at x-coordinate
	public double getWidthAtX(double x) {
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
		return z_F_u;
	}


	public double getZOutlineXZLowerAtX(double x) {
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
		return z_F_l;
	}


	public double getYOutlineXYSideRAtX(double x) {
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
		return y_F_r;
	}


	public double getYOutlineXYSideLAtX(double x) {
		return -getYOutlineXYSideRAtX(x);
	}

	/**
	 * section points on SideR are ordered as follows:
	 * first point is at Y=0 at the top of the section,
	 * successive points are taken going counter-clockwise when looking at
	 * YZ section from X- towards X+
	 *
	 * @param len_x, dimensional x-coordinate of the desired section
	 * @return a list of 3D points (pvectors)
	 */
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
	 * @param len_x, dimensional x-coordinate of the desired section
	 * @return a list of 3D points (pvectors)
	 */
	public List<PVector> getUniqueValuesYZSideLCurve(Amount<Length> len_x)
	{
		List<PVector> pts  = getUniqueValuesYZSideRCurve(len_x);
		// simply change all Y-coordinates
		for (PVector p : pts){ p.y = -p.y; }
		Collections.reverse(pts);
		return pts;
	}

	/**
	 * section points of a fuselage section, ordered as follows:
	 * first point is at Y=0 at the top of the section,
	 * successive points are taken going counter-clockwise when looking at
	 * YZ section from X- towards X+.
	 * The section is closed, i.e. the last point is again at Y=0, at the top 
	 * of the section, and coincides with the first point.
	 *
	 * @param len_x, dimensional x-coordinate of the desired section
	 * @return a list of 3D points (pvectors)
	 */
	public List<PVector> getUniqueValuesYZSectionCurve(Amount<Length> len_x)
	{
		List<PVector> result = new ArrayList<>();
		List<PVector> ptsR  = getUniqueValuesYZSideRCurve(len_x);
		result.addAll(ptsR);
		List<PVector> ptsL  = getUniqueValuesYZSideLCurve(len_x); // points in SideL curves are in the right order
		ptsL.stream().skip(1) // skipping the first, being an undesired duplicate
			.forEach(p -> result.add(p));
		return result;
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
	public FuselageCurvesSection makeSection(double x){

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

		FuselageCurvesSection fuselageCurvesSection = new FuselageCurvesSection(
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

		FuselageCurvesSection section = makeSection(x);

		if ( section == null ) {
			System.out.println("null makeSection");
			return 0.0;
		}

		int iLast = section.getSectionUpperLeftPoints().size() - 1;
		// Left Points when section is seen from X- to X+

		return section.getSectionUpperLeftPoints().get(iLast).y;
	}

	
	/**
	 * This method updates the all the fuselage parameters when one of its dimensions changes.
	 * To do this it uses several criteria described below. In particular, for each dimension,
	 * all the available criteria will be enumerated.
	 * 
	 * Fuselage Length (lenF):
	 * ADJ_TOT_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS
	 * ADJ_TOT_LENGTH_CONST_FINENESS_RATIOS
	 * 
	 * Cabin (Cylinder) Length (lenC):
	 * ADJ_CYL_LENGTH
	 * 
	 * Nose Length (lenN):
	 * ADJ_NOSE_LENGTH_CONST_TOT_LENGTH_DIAMETERS
	 * ADJ_NOSE_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS
	 * ADJ_NOSE_LENGTH_CONST_FINENESS_RATIOS_VAR_LENGTHS
	 * 
	 * Tail Length (lenT):
	 * ADJ_TAILCONE_LENGTH_CONST_TOT_LENGTH_DIAMETERS
	 * ADJ_TAILCONE_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS
	 * ADJ_TAILCONE_LENGTH_CONST_FINENESS_RATIOS_VAR_LENGTHS
	 * 
	 * Section Cylider Height:
	 * ADJ_FUS_LENGTH_CONST_FINENESS_RATIOS_VAR_DIAMETERS
	 * 
	 * @param len
	 * @param criterion
	 */
	public void adjustDimensions(Amount<Length> len, FuselageAdjustCriteriaEnum criterion) {

		/*
		 * ALL THE VARIABLES USED IN CALCULATE GEOMETRY ARE FILLED FOR EACH CASE. IF SOME OF 
		 * THEM ARE CONSTANTS, THEIR VALUE IS SET AS THE CURRENT VALUE FROM THE FUSELAGE CREATOR.
		 * 
		 * AT THE END ALL THE REQUIRED VARIABLES ARE SET IN THEIR RELATED FIELDS AND USED IN 
		 * CALCULATE GEOMETRY.
		 */

		// VARIABLE USED IN CALCULATE GEOMETRY:
		Amount<Length> lenF = null;
		Double lenRatioCF = null;
		Double lenRatioNF = null;
		Amount<Length> sectionCylinderHeight = null;
		Amount<Length> sectionCylinderWidth = null;
		
		// DATA NOT TO BE SET AS FIELDS BUT NEEDED FOR THE CALCULATION:
		Double sectionHeightWidthRatio = null;
		Amount<Length> equivalentDiameterGM = null;
		Double lenRatioTF = null;
		Amount<Length> lenN = null;
		Amount<Length> lenC = null;
		Amount<Length> lenT = null;
		Double lambdaF = null;
		Double lambdaN = null;
		Double lambdaC = null;
		Double lambdaT = null;
		
		switch (criterion) {

		case ADJ_TOT_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS:
			
			// new variable
			lenF = len;
			
			// constant values
			lenRatioCF = theFuselageCreatorInterface.getCylinderLengthRatio();
			lenRatioNF = theFuselageCreatorInterface.getNoseLengthRatio();
			sectionCylinderHeight = theFuselageCreatorInterface.getSectionCylinderHeight();
			sectionCylinderWidth = theFuselageCreatorInterface.getSectionCylinderWidth();
			break;

		case ADJ_TOT_LENGTH_CONST_FINENESS_RATIOS:
			
			// new variable
			lenF = len;
			
			// constant values
			lambdaF = fuselageFinenessRatio;
			lambdaC = cylinderFinenessRatio;
			lambdaN = noseFinenessRatio;
			
			// values to be calculated
			sectionHeightWidthRatio = 
					theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METER)
					/theFuselageCreatorInterface.getSectionCylinderWidth().doubleValue(SI.METER);
			
			equivalentDiameterGM = Amount.valueOf(
					lenF.doubleValue(SI.METRE)/lambdaF,
					SI.METER
					); 
			
			sectionCylinderHeight = Amount.valueOf( 
					Math.sqrt(
							sectionHeightWidthRatio
							*Math.pow(equivalentDiameterGM.doubleValue(SI.METER),2)
							),
					SI.METER
					);
			sectionCylinderWidth = sectionCylinderHeight.divide(sectionHeightWidthRatio);
			
			lenN = equivalentDiameterGM.times(lambdaN);
			
			lenC = equivalentDiameterGM.times(lambdaC);
			
			lenRatioNF =
					lenN.doubleValue(SI.METER)
					/ lenF.doubleValue(SI.METRE);
			lenRatioCF =   
					lenC.doubleValue(SI.METER)
					/ lenF.doubleValue(SI.METRE);
			break;

		case ADJ_CYL_LENGTH:
			
			// new variable
			lenC = len;
			
			// constant values
			lenN = lengthNose;
			lenT = lengthTail;
			equivalentDiameterGM = equivalentDiameterCylinderGM; 
			sectionCylinderHeight = theFuselageCreatorInterface.getSectionCylinderHeight();
			sectionCylinderWidth = theFuselageCreatorInterface.getSectionCylinderWidth();
			
			// values to be calculated
			lenF = Amount.valueOf( 
					lenN.doubleValue(SI.METRE)
					+ lenC.doubleValue(SI.METRE) 
					+ lenT.doubleValue(SI.METRE) , 
					SI.METRE);
			lenRatioNF =   
					lenN.doubleValue(SI.METRE)
					/ lenF.doubleValue(SI.METRE);
			lenRatioCF =  
					lenC.doubleValue(SI.METRE)
					/ lenF.doubleValue(SI.METRE);
			break;

		case ADJ_NOSE_LENGTH_CONST_TOT_LENGTH_DIAMETERS:
			
			// new variable
			lenN = len;
			
			// constant values
			equivalentDiameterGM = equivalentDiameterCylinderGM;
			sectionCylinderHeight = theFuselageCreatorInterface.getSectionCylinderHeight();
			sectionCylinderWidth = theFuselageCreatorInterface.getSectionCylinderWidth();
			lenF = theFuselageCreatorInterface.getFuselageLength();
			lenT = lengthTail;
			
			// values to be calculated
			lenC = Amount.valueOf(  
					lenF.doubleValue(SI.METRE)
					- lenN.doubleValue(SI.METRE) 
					- lenT.doubleValue(SI.METRE), 
					SI.METRE);
			lenRatioNF =  
					lenN.doubleValue(SI.METRE)
					/ lenF.doubleValue(SI.METRE);
			lenRatioCF =  
					lenC.doubleValue(SI.METRE)
					/ lenF.doubleValue(SI.METRE);
			break;

		case ADJ_NOSE_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS:
			
			// new variable
			lenN = len;	
			
			// constant values
			lenRatioCF = theFuselageCreatorInterface.getCylinderLengthRatio();
			lenRatioNF = tailLengthRatio;
			equivalentDiameterGM = equivalentDiameterCylinderGM;
			sectionCylinderHeight = theFuselageCreatorInterface.getSectionCylinderHeight();
			sectionCylinderWidth = theFuselageCreatorInterface.getSectionCylinderWidth();
			
			// values to be calculated
			lenF = Amount.valueOf( 
					lenN.doubleValue(SI.METRE)/lenRatioNF, 
					SI.METRE); // _len_N/_lenRatio_NF;
			break;

		case  ADJ_NOSE_LENGTH_CONST_FINENESS_RATIOS_VAR_LENGTHS:
			
			// new variable
			lenN = len;	
			
			// constant values
			lambdaN = noseFinenessRatio;
			lambdaC = cylinderFinenessRatio;
			lambdaT = tailFinenessRatio;
			
			// values to be calculated
			sectionHeightWidthRatio = 
					theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METER)
					/theFuselageCreatorInterface.getSectionCylinderWidth().doubleValue(SI.METER);
			
			equivalentDiameterGM = Amount.valueOf(
					lenN.doubleValue(SI.METRE)/lambdaN,
					SI.METER
					); 
			
			sectionCylinderHeight = Amount.valueOf( 
					Math.sqrt(
							sectionHeightWidthRatio
							*Math.pow(equivalentDiameterGM.doubleValue(SI.METER),2)
							),
					SI.METER
					);
			sectionCylinderWidth = sectionCylinderHeight.divide(sectionHeightWidthRatio);
			
			lenC = Amount.valueOf(
					lambdaC * equivalentDiameterGM.doubleValue(SI.METRE), 
					SI.METRE);
			lenT = Amount.valueOf(
					lambdaT * equivalentDiameterGM.doubleValue(SI.METRE) , 
					SI.METRE);
			lenF = Amount.valueOf(
					lenN.doubleValue(SI.METRE)
					+ lenC.doubleValue(SI.METRE) 
					+ lenT.doubleValue(SI.METRE), 
					SI.METRE);
			lenRatioNF =   
					lenN.doubleValue(SI.METRE)
					/ lenF.doubleValue(SI.METRE) ;
			lenRatioCF =  
					lenC.doubleValue(SI.METRE)
					/ lenF.doubleValue(SI.METRE) ;
			break;

		case ADJ_TAILCONE_LENGTH_CONST_TOT_LENGTH_DIAMETERS:
			
			// new variable
			lenT = len;
			
			// constant values
			equivalentDiameterGM = equivalentDiameterCylinderGM;
			sectionCylinderHeight = theFuselageCreatorInterface.getSectionCylinderHeight();
			sectionCylinderWidth = theFuselageCreatorInterface.getSectionCylinderWidth();
			lenF = theFuselageCreatorInterface.getFuselageLength();
			lenN = lengthNose;
			lenRatioNF = theFuselageCreatorInterface.getNoseLengthRatio();
			
			// values to be calculated
			lenC = Amount.valueOf( 
					lenF.doubleValue(SI.METRE)
					- lenN.doubleValue(SI.METRE) 
					- lenT.doubleValue(SI.METRE) , 
					SI.METRE);
			lenRatioCF = 
					lenC.doubleValue(SI.METRE)
					/ lenF.doubleValue(SI.METRE);
			break;

		case ADJ_TAILCONE_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS:
			
			// new variable
			lenT = len;
			
			// constant values
			lenRatioNF = theFuselageCreatorInterface.getNoseLengthRatio();
			lenRatioCF = theFuselageCreatorInterface.getCylinderLengthRatio();
			lenRatioTF = tailLengthRatio;
			equivalentDiameterGM = this.equivalentDiameterCylinderGM;
			sectionCylinderHeight = theFuselageCreatorInterface.getSectionCylinderHeight();
			sectionCylinderWidth = theFuselageCreatorInterface.getSectionCylinderWidth();
			
			// values to be calculated
			lenF = Amount.valueOf( 
					lenT.doubleValue(SI.METRE)/ lenRatioTF, 
					SI.METRE); // _len_N/_lenRatio_NF;
			break;

		case ADJ_TAILCONE_LENGTH_CONST_FINENESS_RATIOS_VAR_LENGTHS:
			
			// new variable
			lenT = len;
			
			// constant values
			lambdaF = fuselageFinenessRatio;
			lambdaT = tailFinenessRatio;
			lambdaN = noseFinenessRatio;
			lambdaC = cylinderFinenessRatio;
			
			// values to be calculated
			sectionHeightWidthRatio = 
					theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METER)
					/theFuselageCreatorInterface.getSectionCylinderWidth().doubleValue(SI.METER);
			
			equivalentDiameterGM = Amount.valueOf(
					lenT.doubleValue(SI.METRE)/lambdaT,
					SI.METER
					); 
			
			sectionCylinderHeight = Amount.valueOf( 
					Math.sqrt(
							sectionHeightWidthRatio
							*Math.pow(equivalentDiameterGM.doubleValue(SI.METER),2)
							),
					SI.METER
					);
			sectionCylinderWidth = sectionCylinderHeight.divide(sectionHeightWidthRatio);
			
			lenN = Amount.valueOf( 
					lambdaN * equivalentDiameterGM.doubleValue(SI.METRE) , 
					SI.METRE);
			lenC = Amount.valueOf( 
					lambdaC * equivalentDiameterGM.doubleValue(SI.METRE) , 
					SI.METRE);
			lenF = Amount.valueOf( 
					lambdaF * equivalentDiameterGM.doubleValue(SI.METRE) , 
					SI.METRE);
			lenRatioNF = 
					lenN.doubleValue(SI.METRE)
					/ lenF.doubleValue(SI.METRE);
			lenRatioCF =  
					lenC.doubleValue(SI.METRE)
					/ lenF.doubleValue(SI.METRE);
			break;

		case ADJ_FUS_LENGTH_CONST_FINENESS_RATIOS_VAR_DIAMETERS:
			
			// new variable
			equivalentDiameterGM= len;
			
			// constant values
			lambdaF = fuselageFinenessRatio;
			lambdaN = noseFinenessRatio;
			lambdaC = cylinderFinenessRatio;
			lambdaT = tailFinenessRatio;
			
			// values to be calculated
			sectionHeightWidthRatio = 
					theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METER)
					/theFuselageCreatorInterface.getSectionCylinderWidth().doubleValue(SI.METER);
			
			sectionCylinderHeight = Amount.valueOf( 
					Math.sqrt(
							sectionHeightWidthRatio
							*Math.pow(equivalentDiameterGM.doubleValue(SI.METER),2)
							),
					SI.METER
					);
			sectionCylinderWidth = sectionCylinderHeight.divide(sectionHeightWidthRatio);
			
			lenN  =Amount.valueOf(
					lambdaN * equivalentDiameterGM.doubleValue(SI.METRE) , 
					SI.METRE);
			lenC = Amount.valueOf(  
					lambdaC * equivalentDiameterGM.doubleValue(SI.METRE) , 
					SI.METRE);
			lenF = Amount.valueOf( 
					lambdaF * equivalentDiameterGM.doubleValue(SI.METRE) , 
					SI.METRE);
			lenRatioNF =
					lenN.doubleValue(SI.METRE)
					/ lenF.doubleValue(SI.METRE);
			lenRatioCF =  
					lenC.doubleValue(SI.METRE)
					/ lenF.doubleValue(SI.METRE);
			break;
		default:
			break;
		}
		
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface)
				.setFuselageLength(lenF)
				.setCylinderLengthRatio(lenRatioCF)
				.setNoseLengthRatio(lenRatioNF)
				.setSectionCylinderHeight(sectionCylinderHeight)
				.setSectionCylinderWidth(sectionCylinderWidth)
				.build()
				);
		
		calculateGeometry();
		
	}  // End  AdjustLength
	
	public void adjustSectionShapeParameters(int idx, double a, double rhoUpper, double rhoLower) {

		switch (idx) {

		case IDX_SECTION_YZ_NOSE_CAP: // a, rhoUpper, rhoLower NOT USED
			// NOSE CAP
			// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
			double x  =  noseCapOffset.doubleValue(SI.METRE);
			double hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ) );
			double wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ) );
			sectionsYZ.get(idx).setSectionParameters(
					wf, hf, 0.5, 0.0, 0.0,         // lengths & parameters
					npSecUp, npSecLow          // num. points
					);
			
			setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface)
					.setSectionCylinderLowerToTotalHeightRatio(0.5)
					.setSectionCylinderRhoUpper(0.0)
					.setSectionCylinderRhoLower(0.0)
					.build()
					);
			
			break;

		case IDX_SECTION_YZ_MID_NOSE:

			// MID-NOSE
			// IDX_SECTION_YZ_MID_NOSE

			//System.out.println("+++ rhoUpper: "+ _sectionsYZ.get(idx).get_RhoUpper());

			x  = 0.5*lengthNose.doubleValue(SI.METRE);
			hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ) );
			wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ) );
			sectionsYZ.get(idx).setSectionParameters(
					wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
					npSecUp, npSecLow          // num. points
					);

			setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface)
					.setSectionCylinderLowerToTotalHeightRatio(a)
					.setSectionCylinderRhoUpper(rhoUpper)
					.setSectionCylinderRhoLower(rhoLower)
					.build()
					);
			
			break;

		case IDX_SECTION_YZ_CYLINDER_1:

			// CYLINDER
			// IDX_SECTION_YZ_CYLINDER
			x  = lengthNose.doubleValue(SI.METRE);
			wf = theFuselageCreatorInterface.getSectionCylinderWidth().doubleValue(SI.METRE);
			hf = theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METRE);
			sectionsYZ.get(idx).setSectionParameters(
					wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
					npSecUp, npSecLow          // num. points
					);
			
			setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface)
					.setSectionCylinderLowerToTotalHeightRatio(a)
					.setSectionCylinderRhoUpper(rhoUpper)
					.setSectionCylinderRhoLower(rhoLower)
					.build()
					);
			
			break;

		case IDX_SECTION_YZ_CYLINDER_2:
			// CYLINDER
			// IDX_SECTION_YZ_CYLINDER
			x  = lengthNose.doubleValue(SI.METRE) + lengthCylinder.doubleValue(SI.METRE);
			wf = theFuselageCreatorInterface.getSectionCylinderWidth().doubleValue(SI.METRE);
			hf = theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METRE);
			sectionsYZ.get(idx).setSectionParameters(
					wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
					npSecUp, npSecLow          // num. points
					);
			
			setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface)
					.setSectionCylinderLowerToTotalHeightRatio(a)
					.setSectionCylinderRhoUpper(rhoUpper)
					.setSectionCylinderRhoLower(rhoLower)
					.build()
					);
			
			break;

		case IDX_SECTION_YZ_MID_TAIL:
			// MID-TAIL
			// IDX_SECTION_YZ_MID_TAIL
			x  = theFuselageCreatorInterface.getFuselageLength().doubleValue(SI.METRE) - 0.5*lengthTail.doubleValue(SI.METRE);
			hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ) );
			wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ) );
			sectionsYZ.get(idx).setSectionParameters(
					wf, hf, a, rhoUpper, rhoLower, // lengths & parameters
					npSecUp, npSecLow          // num. points
					);
			
			setTheFuselageCreatorInterface(
					IFuselageCreator.Builder.from(theFuselageCreatorInterface)
					.setSectionCylinderLowerToTotalHeightRatio(a)
					.setSectionCylinderRhoUpper(rhoUpper)
					.setSectionCylinderRhoLower(rhoLower)
					.build()
					);
			
			break;

		case IDX_SECTION_YZ_TAIL_CAP: // a, rhoUpper, rhoLower NOT USED
			// TAIL CAP
			// IDX_SECTION_YZ_NOSE_CAP (elliptical and centered)
			x  = theFuselageCreatorInterface.getFuselageLength().doubleValue(SI.METRE) - tailCapOffset.doubleValue(SI.METRE);
			hf = Math.abs( getZOutlineXZUpperAtX( x ) - getZOutlineXZLowerAtX( x ) );
			wf = Math.abs( 2.0*getYOutlineXYSideRAtX( x ) );
			sectionsYZ.get(idx).setSectionParameters(
					wf, hf, 0.5, 0.0, 0.0,         // lengths & parameters
					npSecUp, npSecLow          // num. points
					);
			
			setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface)
					.setSectionCylinderLowerToTotalHeightRatio(0.5)
					.setSectionCylinderRhoUpper(0.0)
					.setSectionCylinderRhoLower(0.0)
					.build()
					);
			
			break;

		default:
			// do nothing
			break;
		}

		calculateOutlines(npN, npC, npT, npSecUp, npSecLow);
		
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
			sWetNose = Amount.valueOf(0.75 * Math.PI * equivalentDiameterCylinderGM.doubleValue(SI.METER)*lengthNose.doubleValue(SI.METER), SI.SQUARE_METRE);
			sWetTail = Amount.valueOf(0.72 * Math.PI * equivalentDiameterCylinderGM.doubleValue(SI.METER)*lengthTail.doubleValue(SI.METER), SI.SQUARE_METRE);
			sWetCylinder = Amount.valueOf(Math.PI * equivalentDiameterCylinderGM.doubleValue(SI.METER)*lengthCylinder.doubleValue(SI.METER), SI.SQUARE_METRE);
			sWetTotal = sWetNose.to(SI.SQUARE_METRE).plus(sWetTail.to(SI.SQUARE_METRE)).plus(sWetCylinder.to(SI.SQUARE_METRE)); 
			break;
		}

		case "Torenbeek" : { // page 409 torenbeek 2013
			frontSurface = Amount.valueOf(
					(Math.PI/4) 
					* Math.pow(theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METER),2),
					SI.SQUARE_METRE
					); 
			sWetTotal = Amount.valueOf(frontSurface.getEstimatedValue()*4*(fuselageFinenessRatio - 1.30), Area.UNIT); 
			break;
		}

		}

	}

	/**
	 * This method computes the upsweep angle of the fuselage. To locate where the upsweep must be
	 * calculated, a specific intersection point has been set. The height of the intersection point between
	 * the horizontal line and the tangent to tail contour is equal to  0.26 of fuselage height (taken from
	 * the bottom-line).
	 *
	 * see FuselageCreator Aerodynamic Prediction Methods
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
		double x0 = lengthNose.doubleValue(SI.METER) + lengthCylinder.doubleValue(SI.METER);

		// values filtered as x >= l_N + l_C
		List<Double> vX = new ArrayList<Double>();
		outlineXZLowerCurveX.stream().filter(x -> x > x0 ).distinct().forEach(vX::add);

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
		double z1 = vZ.get(0) + 0.26*theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METER);
		List<Double> vZ1 = new ArrayList<Double>();
		vZ.stream().map(z -> z1).forEach(vZ1::add);

		double xu = MyArrayUtils.intersectArraysBrent(
				ArrayUtils.toPrimitive(vX.toArray(new Double[vX.size()])),
				ArrayUtils.toPrimitive(vZ.toArray(new Double[vZ.size()])),
				ArrayUtils.toPrimitive(vZ1.toArray(new Double[vZ1.size()])),
				vX.get(0), vX.get(vX.size()-1),
				AllowedSolution.ANY_SIDE);

		// generate a vector of constant x = xu, same size of vZ, or vX
		List<Double> vX1 = new ArrayList<Double>();
		vX.stream().map(x -> xu).forEach(vX1::add);

		double zu = MyArrayUtils.intersectArraysBrent(
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
	}

	/**
	 * This method computes the windshield angle of the fuselage. To locate where the windshield must be
	 * calculated, a specific intersection point has been set. The height of the intersection point between
	 * the horizontal line and the tangent to tail contour is equal to  0.75 of fuselage height (taken from
	 * the bottom-line).
	 *
	 * see FuselageCreator Aerodynamic Prediction Methods
	 * DOI: 10.2514/6.2015-2257
	 *
	 * @author Vincenzo Cusati
	 */
	private void calculateWindshieldAngle() {

		// xcalculate point (x,z) from intersection of:
		// - horiz. line at 0.75 of fuselage height (d_C) - taken from the bottom-line
		// - upper profile of the nose sideview
		//
		// Using Java 8 features

		// x at l_N
		double xLNose = lengthNose.doubleValue(SI.METER);

		// values filtered as x <= l_N
		List<Double> vXNose = new ArrayList<Double>();
		outlineXZUpperCurveX.stream().filter(x -> x < xLNose ).distinct().forEach(vXNose::add);

		// index of last x in _outlineXZUpperCurveX >= xLNose
		int idxXNose = vXNose.size();

		// the coupled z-values
		List<Double> vZNose = new ArrayList<Double>();
		vZNose = IntStream.range(0, outlineXZUpperCurveZ.size()).filter(i -> i < idxXNose)
				.mapToObj(i -> outlineXZUpperCurveZ.get(i))
				.distinct()
				.collect(Collectors.toList());

		// generate a vector of constant z = z_min + 0.75*d_C, same size of vZNose, or vXNose
		// Check if it's better to take the value of z at 0.60*d_C (for the methodology)
		double z1Nose = MyArrayUtils.getMin(outlineXZLowerCurveZ) + 0.75*theFuselageCreatorInterface.getSectionCylinderHeight().doubleValue(SI.METER);
		List<Double> vZ1Nose = new ArrayList<Double>();
		vZNose.stream().map(z -> z1Nose).forEach(vZ1Nose::add);

		double xw = MyArrayUtils.intersectArraysBrent(
				ArrayUtils.toPrimitive(vXNose.toArray(new Double[vXNose.size()])),
				ArrayUtils.toPrimitive(vZNose.toArray(new Double[vZNose.size()])),
				ArrayUtils.toPrimitive(vZ1Nose.toArray(new Double[vZ1Nose.size()])),
				vXNose.get(0), vXNose.get(vXNose.size()-1),
				AllowedSolution.ANY_SIDE);

		// generate a vector of constant x = xw, same size of vZNose, or vXNose
		List<Double> vX1Nose = new ArrayList<Double>();
		vXNose.stream().map(x -> xw).forEach(vX1Nose::add);

		double zw = MyArrayUtils.intersectArraysBrent(
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

	public static FuselageCreator importFromXML(String pathToXML) {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading fuselage data ...");

		//.......................................................
		// data initialization
		int deckNum = 0;
		boolean pressurized = false;
		Amount<Length> len = Amount.valueOf(0.0, SI.METER);
		Amount<Length> roughness = Amount.valueOf(0.0, SI.METER);
		double lenRatioNF = 0.0;
		Amount<Length> heightN = Amount.valueOf(0.0, SI.METER);
		double dxNoseCapPercent = 0.0;
		WindshieldTypeEnum windshieldType = null;
		Amount<Length> windshieldWidth = Amount.valueOf(0.0, SI.METER);
		Amount<Length> windshieldHeight = Amount.valueOf(0.0, SI.METER);
		double sectionNoseMidLowerToTotalHeightRatio = 0.0;
		double sectionMidNoseRhoUpper = 0.0;
		double sectionMidNoseRhoLower = 0.0;
		double lenRatioCF = 0.0;
		Amount<Length> sectionCylinderWidth = Amount.valueOf(0.0, SI.METER);
		Amount<Length> sectionCylinderHeight = Amount.valueOf(0.0, SI.METER);
		Amount<Length> heightFromGround = Amount.valueOf(0.0, SI.METER);
		double sectionCylinderLowerToTotalHeightRatio = 0.0;
		double sectionCylinderRhoUpper = 0.0;
		double sectionCylinderRhoLower = 0.0;
		Amount<Length> heightT = Amount.valueOf(0.0, SI.METER);
		double dxTailCapPercent = 0.0;
		double sectionTailMidLowerToTotalHeightRatio = 0.0;
		double sectionMidTailRhoUpper = 0.0;
		double sectionMidTailRhoLower = 0.0;
		List<SpoilerCreator> spoilers = new ArrayList<>();
		//.......................................................
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage/@id");
		String pressProp = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage/@pressurized");
		if(pressProp != null)
			pressurized = Boolean.valueOf(pressProp);

		// GLOBAL DATA
		String deckProp = reader.getXMLPropertyByPath("//global_data/deck_number");
		if(deckProp != null)
			deckNum = Integer.valueOf(deckProp);
		
		String lenProp = reader.getXMLPropertyByPath("//global_data/length");
		if(lenProp != null)
			len = reader.getXMLAmountLengthByPath("//global_data/length");
		
		String roughnessProp = reader.getXMLPropertyByPath("//global_data/roughness");
		if(roughnessProp != null)
			roughness = reader.getXMLAmountLengthByPath("//global_data/roughness");
		
		// NOSE TRUNK
		String lenRatioNoseProp = reader.getXMLPropertyByPath("//nose_trunk/length_ratio");
		if(lenRatioNoseProp != null)
			lenRatioNF = Double.valueOf(lenRatioNoseProp);
		
		String heightNProp = reader.getXMLPropertyByPath("//nose_trunk/tip_height_offset");
		if(heightNProp != null)
			heightN = reader.getXMLAmountLengthByPath("//nose_trunk/tip_height_offset");
		
		String dxCapPercentNoseProp = reader.getXMLPropertyByPath("//nose_trunk/dx_cap_percent");
		if(dxCapPercentNoseProp != null)
			dxNoseCapPercent = Double.valueOf(dxCapPercentNoseProp);
		
		String windshieldTypeProp = reader.getXMLPropertyByPath("//nose_trunk/windshield_type");
		if(windshieldTypeProp != null) {
			if(windshieldTypeProp.equalsIgnoreCase("DOUBLE"))
				windshieldType = WindshieldTypeEnum.DOUBLE;
			if(windshieldTypeProp.equalsIgnoreCase("FLAT_FLUSH"))
				windshieldType = WindshieldTypeEnum.FLAT_FLUSH;
			if(windshieldTypeProp.equalsIgnoreCase("FLAT_PROTRUDING"))
				windshieldType = WindshieldTypeEnum.FLAT_PROTRUDING;
			if(windshieldTypeProp.equalsIgnoreCase("SINGLE_ROUND"))
				windshieldType = WindshieldTypeEnum.SINGLE_ROUND;
			if(windshieldTypeProp.equalsIgnoreCase("SINGLE_SHARP"))
				windshieldType = WindshieldTypeEnum.SINGLE_SHARP;
		}
		
		String windshieldWidthProp = reader.getXMLPropertyByPath("//nose_trunk/windshield_width");
		if(windshieldWidthProp != null)
			windshieldWidth = reader.getXMLAmountLengthByPath("//nose_trunk/windshield_width");
		
		String windshieldHeightProp = reader.getXMLPropertyByPath("//nose_trunk/windshield_height");
		if(windshieldHeightProp != null)
			windshieldHeight = reader.getXMLAmountLengthByPath("//nose_trunk/windshield_height");
		
		String midSectionLowerToTotalHeightRatioProp = reader.getXMLPropertyByPath("//nose_trunk/mid_section_lower_to_total_height_ratio");
		if(midSectionLowerToTotalHeightRatioProp != null)
			sectionNoseMidLowerToTotalHeightRatio = Double.valueOf(midSectionLowerToTotalHeightRatioProp);
		
		String midSectionRhoUpperNoseProp = reader.getXMLPropertyByPath("//nose_trunk/mid_section_rho_upper");
		if(midSectionRhoUpperNoseProp != null)
			sectionMidNoseRhoUpper = Double.valueOf(midSectionRhoUpperNoseProp);
		
		String midSectionRhoLowerNoseProp = reader.getXMLPropertyByPath("//nose_trunk/mid_section_rho_lower");
		if(midSectionRhoLowerNoseProp != null)
			sectionMidNoseRhoLower = Double.valueOf(midSectionRhoLowerNoseProp);
		
		// CYLINDRICAL TRUNK
		String lenRatioCylProp = reader.getXMLPropertyByPath("//cylindrical_trunk/length_ratio");
		if(lenRatioCylProp != null)
			lenRatioCF = Double.valueOf(lenRatioCylProp);
		
		String sectionCylinderWidthProp = reader.getXMLPropertyByPath("//cylindrical_trunk/section_width");
		if(sectionCylinderWidthProp != null)
			sectionCylinderWidth = reader.getXMLAmountLengthByPath("//cylindrical_trunk/section_width");
		
		String sectionCylinderHeightProp = reader.getXMLPropertyByPath("//cylindrical_trunk/section_height");
		if(sectionCylinderHeightProp != null)
			sectionCylinderHeight = reader.getXMLAmountLengthByPath("//cylindrical_trunk/section_height");
		
		String heightFromGroundProp = reader.getXMLPropertyByPath("//cylindrical_trunk/height_from_ground");
		if(heightFromGroundProp != null)
			heightFromGround = reader.getXMLAmountLengthByPath("//cylindrical_trunk/height_from_ground");
		
		String sectionLowerToTotalHeightRatioProp = reader.getXMLPropertyByPath("//cylindrical_trunk/section_lower_to_total_height_ratio");
		if(sectionLowerToTotalHeightRatioProp != null)
			sectionCylinderLowerToTotalHeightRatio = Double.valueOf(sectionLowerToTotalHeightRatioProp);
		
		String sectionRhoUpperProp = reader.getXMLPropertyByPath("//cylindrical_trunk/section_rho_upper");
		if(sectionRhoUpperProp != null)
			sectionCylinderRhoUpper = Double.valueOf(sectionRhoUpperProp);
		
		String sectionRhoLowerProp = reader.getXMLPropertyByPath("//cylindrical_trunk/section_rho_lower");
		if(sectionRhoLowerProp != null)
			sectionCylinderRhoLower = Double.valueOf(sectionRhoLowerProp);
		
		// TAIL TRUNK
		String heightTProp = reader.getXMLPropertyByPath("//tail_trunk/tip_height_offset");
		if(heightTProp != null)
			heightT = reader.getXMLAmountLengthByPath("//tail_trunk/tip_height_offset");
		
		String dxCapPercentTailProp = reader.getXMLPropertyByPath("//tail_trunk/dx_cap_percent");
		if(dxCapPercentTailProp != null)
			dxTailCapPercent = Double.valueOf(dxCapPercentTailProp);
		
		String sectionTailMidLowerToTotalHeightRatioProp = reader.getXMLPropertyByPath("//tail_trunk/mid_section_lower_to_total_height_ratio");
		if(sectionTailMidLowerToTotalHeightRatioProp != null)
			sectionTailMidLowerToTotalHeightRatio = Double.valueOf(sectionTailMidLowerToTotalHeightRatioProp);
		
		String midSectionRhoUpperTailProp = reader.getXMLPropertyByPath("//tail_trunk/mid_section_rho_upper");
		if(midSectionRhoUpperTailProp != null)
			sectionMidTailRhoUpper = Double.valueOf(midSectionRhoUpperTailProp);
		
		String midSectionRhoLowerTailProp = reader.getXMLPropertyByPath("//tail_trunk/mid_section_rho_lower");
		if(midSectionRhoLowerTailProp != null)
			sectionMidTailRhoLower = Double.valueOf(midSectionRhoLowerTailProp);
		
		// SPOILERS
		NodeList nodelistSpoilers = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//spoilers/spoiler");
		
		System.out.println("Spoilers found: " + nodelistSpoilers.getLength());
		
		for (int i = 0; i < nodelistSpoilers.getLength(); i++) {
			Node nodeSpoiler  = nodelistSpoilers.item(i); // .getNodeValue();
			Element elementSpoiler = (Element) nodeSpoiler;
            System.out.println("[" + i + "]\nSlat id: " + elementSpoiler.getAttribute("id"));
            
            spoilers.add(SpoilerCreator.importFromSpoilerNode(nodeSpoiler));
		}
		
		// create the fuselage via its builder
		FuselageCreator fuselage = new FuselageCreator(
				new IFuselageCreator.Builder()
				.setId(id)
				.setPressurized(pressurized)
				.setFuselageLength(len)
				.setDeckNumber(deckNum)
				.setRoughness(roughness)
				.setNoseCapOffsetPercent(dxNoseCapPercent)
				.setNoseTipOffset(heightN)
				.setNoseLengthRatio(lenRatioNF)
				.setSectionNoseMidLowerToTotalHeightRatio(sectionNoseMidLowerToTotalHeightRatio)
				.setSectionMidNoseRhoUpper(sectionMidNoseRhoUpper)
				.setSectionMidNoseRhoLower(sectionMidNoseRhoLower)
				.setWindshieldType(windshieldType)
				.setWindshieldHeight(windshieldHeight)
				.setWindshieldWidth(windshieldWidth)
				.setCylinderLengthRatio(lenRatioCF)
				.setSectionCylinderHeight(sectionCylinderHeight)
				.setSectionCylinderWidth(sectionCylinderWidth)
				.setHeightFromGround(heightFromGround)
				.setSectionCylinderLowerToTotalHeightRatio(sectionCylinderLowerToTotalHeightRatio)
				.setSectionCylinderRhoUpper(sectionCylinderRhoUpper)
				.setSectionCylinderRhoLower(sectionCylinderRhoLower)
				.setTailTipOffest(heightT)
				.setTailCapOffsetPercent(dxTailCapPercent)
				.setSectionTailMidLowerToTotalHeightRatio(sectionTailMidLowerToTotalHeightRatio)
				.setSectionMidTailRhoUpper(sectionMidTailRhoUpper)
				.setSectionMidTailRhoLower(sectionMidTailRhoLower)
				.addAllSpoilers(spoilers)
				.build()
				);

		return fuselage;
	}

	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tFuselage\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + theFuselageCreatorInterface.getId() + "'\n")
				.append("\tPressurized: '" + theFuselageCreatorInterface.isPressurized() + "'\n")
				.append("\tNumber of decks: " + theFuselageCreatorInterface.getDeckNumber() + "\n")
				.append("\tRoughness: " + theFuselageCreatorInterface.getRoughness() + "\n")
				.append("\t...............................................................................................................\n")
				.append("\tLength: " + theFuselageCreatorInterface.getFuselageLength() + "\n")
				.append("\tNose length: " + lengthNose + "\n")
				.append("\tCabin length: " + lengthCylinder + "\n")
				.append("\tTail length: " + lengthTail + "\n")
				.append("\t...............................................................................................................\n")
				.append("\tNose length ratio: " + theFuselageCreatorInterface.getNoseLengthRatio() + "\n")
				.append("\tCabin length ratio: " + theFuselageCreatorInterface.getCylinderLengthRatio() + "\n")
				.append("\tTail length ratio: " + tailLengthRatio + "\n")
				.append("\t...............................................................................................................\n")
				.append("\tCabin width: " + theFuselageCreatorInterface.getSectionCylinderWidth() + "\n")
				.append("\tCabin height: " + theFuselageCreatorInterface.getSectionCylinderHeight() + "\n")
				.append("\t...............................................................................................................\n")
				.append("\tNose fineness ratio: " + noseFinenessRatio + "\n")
				.append("\tCabin fineness ratio: " + cylinderFinenessRatio + "\n")
				.append("\tTail fineness ratio: " + tailFinenessRatio + "\n")
				.append("\tFuselage fineness ratio: " + fuselageFinenessRatio + "\n")
				.append("\t...............................................................................................................\n")
				.append("\tHeight from ground: " + theFuselageCreatorInterface.getHeightFromGround() + "\n")
				.append("\t...............................................................................................................\n")
				.append("\tDiscretization\n")
				.append("\tOutline XY Left Top View - X (m): " + outlineXYSideLCurveX + "\n")
				.append("\tOutline XY Left Top View - Y (m): " + outlineXYSideLCurveY + "\n")
				.append("\tOutline XY Right Top View - X (m): " + outlineXYSideRCurveX + "\n")
				.append("\tOutline XY Right Top View - Y (m): " + outlineXYSideRCurveY + "\n")
				.append("\tOutline XZ Upper Side View - X (m): " + outlineXZUpperCurveX + "\n")
				.append("\tOutline XZ Upper Side View - Z (m): " + outlineXZUpperCurveZ + "\n")
				.append("\tOutline XZ Lower Side View - X (m): " + outlineXZLowerCurveX + "\n")
				.append("\tOutline XZ Lower Side View - Z (m): " + outlineXZLowerCurveZ + "\n")
				.append("\tOutline YZ Upper Section View - Y (m): " + getSectionUpperCurveAmountY().stream().map(y -> y.doubleValue(SI.METER)).collect(Collectors.toList())+ "\n")
				.append("\tOutline YZ Upper Section View - Z (m): " + getSectionUpperCurveAmountZ().stream().map(z -> z.doubleValue(SI.METER)).collect(Collectors.toList()) + "\n")
				.append("\tOutline YZ Upper Section View - Y (m): " + getSectionLowerCurveAmountY().stream().map(y -> y.doubleValue(SI.METER)).collect(Collectors.toList()) + "\n")
				.append("\tOutline YZ Upper Section View - Z (m): " + getSectionLowerCurveAmountZ().stream().map(z -> z.doubleValue(SI.METER)).collect(Collectors.toList()) + "\n")
				.append("\t...............................................................................................................\n");
		
		if(!(theFuselageCreatorInterface.getSpoilers().isEmpty())) {
			for (SpoilerCreator spoilers : theFuselageCreatorInterface.getSpoilers()) {
				sb.append(spoilers.toString());
			}
		}
		
		return sb.toString();
	}
	
	//---------------------------------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	
	public IFuselageCreator getTheFuselageCreatorInterface() {
		return theFuselageCreatorInterface;
	}
	
	public void setTheFuselageCreatorInterface(IFuselageCreator theFuselageCreatorInterface) {
		this.theFuselageCreatorInterface = theFuselageCreatorInterface;
	}
	
	public String getId() {
		return theFuselageCreatorInterface.getId();
	}
	
	public void setId(String id) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setId(id).build());
	}
	
	public Boolean getPressurized() {
		return theFuselageCreatorInterface.isPressurized();
	}

	public void setPressurized(Boolean pressurized) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setPressurized(pressurized).build());
	}
	
	public Amount<Length> getRoughness() {
		return theFuselageCreatorInterface.getRoughness();
	}

	public void setRoughness(Amount<Length> roughness) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setRoughness(roughness).build());
	}
	
	public Amount<Length> getFuselageLength() {
		return theFuselageCreatorInterface.getFuselageLength();
	}

	public void setFuselageLength(Amount<Length> lenF) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setFuselageLength(lenF).build());
	}
	
	public Amount<Length> getNoseLength() {
		return lengthNose;
	}

	public void setNoseLength(Amount<Length> lenN) {
		this.lengthNose = lenN;
	}
	
	public Amount<Length> getCylinderLength() {
		return lengthCylinder;
	}

	public void setCylinderLength(Amount<Length> lenC) {
		this.lengthCylinder = lenC;
	}
	
	public Amount<Length> getTailLength() {
		return lengthTail;
	}

	public void setTailLength(Amount<Length> lenT) {
		this.lengthTail = lenT;
	}
	
	public Amount<Length> getSectionCylinderHeight() {
		return theFuselageCreatorInterface.getSectionCylinderHeight();
	}

	public void setSectionCylinderHeight(Amount<Length> sectionCylinderHeight) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setSectionCylinderHeight(sectionCylinderHeight).build());
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

	public Amount<Area> getCylinderSectionArea() {
		return cylinderSectionArea;
	}

	public void setCylinderSectionArea(Amount<Area> areaC) {
		this.cylinderSectionArea = areaC;
	}

	public Amount<Area> getWindshieldArea() {
		return windshieldArea;
	}

	public void setWindshieldArea(Amount<Area> windshieldArea) {
		this.windshieldArea = windshieldArea;
	}

	public WindshieldTypeEnum getWindshieldType() {
		return theFuselageCreatorInterface.getWindshieldType();
	}

	public void setWindshieldType(WindshieldTypeEnum windshieldType) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setWindshieldType(windshieldType).build());
	}

	public Amount<Area> getSWetNose() {
		return sWetNose;
	}

	public void setSWetNose(Amount<Area> sWetNose) {
		this.sWetNose = sWetNose;
	}

	public Amount<Area> getSWetTail() {
		return sWetTail;
	}

	public void setSWetTail(Amount<Area> sWetTail) {
		this.sWetTail = sWetTail;
	}

	public Amount<Area> getSWetCylinder() {
		return sWetCylinder;
	}

	public void setSWetCylinder(Amount<Area> sWetC) {
		this.sWetCylinder = sWetC;
	}
	
	public Amount<Area> getFrontSurface() {
		return frontSurface;
	}

	public void setFrontSurface(Amount<Area> sFront) {
		this.frontSurface = sFront;
	}
	
	public Amount<Area> getSWetTotal() {
		return sWetTotal;
	}

	public void setSWetTotal(Amount<Area> sWet) {
		this.sWetTotal = sWet;
	}
	
	public Amount<Length> getHeightFromGround() {
		return theFuselageCreatorInterface.getHeightFromGround();
	}

	public void setHeightFromGround(Amount<Length> heightFromGround) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setHeightFromGround(heightFromGround).build());
	}

	public Amount<Angle> getPhiNose() {
		return phiNose;
	}

	public void setPhiNose(Amount<Angle> phiN) {
		this.phiNose = phiN;
	}

	public Amount<Angle> getPhiTail() {
		return phiTail;
	}

	public void setPhiTail(Amount<Angle> phiT) {
		this.phiTail = phiT;
	}

	public Amount<Length> getNoseTipOffset() {
		return theFuselageCreatorInterface.getNoseTipOffset();
	}

	public void setNoseTipOffset(Amount<Length> heightN) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setNoseTipOffset(heightN).build());
	}

	public Amount<Length> getTailTipOffset() {
		return theFuselageCreatorInterface.getTailTipOffest();
	}

	public void setTailTipOffset(Amount<Length> heightT) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setTailTipOffest(heightT).build());
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

	public Double getFuselageFinenessRatio() {
		return fuselageFinenessRatio;
	}

	public void setFuselageFinenessRatio(Double lambdaF) {
		this.fuselageFinenessRatio = lambdaF;
	}

	public Double getNoseFinenessRatio() {
		return noseFinenessRatio;
	}

	public void setNoseFinenessRatio(Double lambdaN) {
		this.noseFinenessRatio = lambdaN;
	}
	
	public Double getCylinderFinenessRatio() {
		return cylinderFinenessRatio;
	}

	public void setCylinderFinenessRatio(Double lambdaC) {
		this.cylinderFinenessRatio = lambdaC;
	}

	public Double getTailFinenessRatio() {
		return tailFinenessRatio;
	}

	public void setTailFinenessRatio(Double lambdaT) {
		this.tailFinenessRatio = lambdaT;
	}

	public Double getNoseLengthRatio() {
		return theFuselageCreatorInterface.getNoseLengthRatio();
	}

	public void setNoseLengthRatio(Double lenRatioNF) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setNoseLengthRatio(lenRatioNF).build());
	}
	
	public Double getCylinderLengthRatio() {
		return theFuselageCreatorInterface.getCylinderLengthRatio();
	}

	public void setCylinderLengthRatio(Double lenRatioCF) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setCylinderLengthRatio(lenRatioCF).build());
	}

	public Double getTailLengthRatio() {
		return tailLengthRatio;
	}

	public void setTailLengthRatio(Double lenRatioTF) {
		this.tailLengthRatio = lenRatioTF;
	}

	public Double getFormFactor() {
		return formFactor;
	}

	public void setFormFactor(Double formFactor) {
		this.formFactor = formFactor;
	}

	public Amount<Length> getSectionCylinderWidth() {
		return theFuselageCreatorInterface.getSectionCylinderWidth();
	}

	public void setSectionCylinderWidth(Amount<Length> sectionCylinderWidth) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setSectionCylinderWidth(sectionCylinderWidth).build());
	}

	public Amount<Length> getWindshieldHeight() {
		return theFuselageCreatorInterface.getWindshieldHeight();
	}

	public void setWindshieldHeight(Amount<Length> windshieldHeight) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setWindshieldHeight(windshieldHeight).build());
	}

	public Amount<Length> getWindshieldWidth() {
		return theFuselageCreatorInterface.getWindshieldWidth();
	}

	public void setWindshieldWidth(Amount<Length> windshieldWidth) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setWindshieldWidth(windshieldWidth).build());
	}

	public Amount<Length> getNoseCapOffset() {
		return noseCapOffset;
	}

	public void setNoseCapOffset(Amount<Length> dxNoseCap) {
		this.noseCapOffset = dxNoseCap;
	}

	public Amount<Length> getTailCapOffset() {
		return tailCapOffset;
	}

	public void setTailCapOffset(Amount<Length> dxTailCap) {
		this.tailCapOffset = dxTailCap;
	}

	public Double getSectionCylinderLowerToTotalHeightRatio() {
		return theFuselageCreatorInterface.getSectionCylinderLowerToTotalHeightRatio();
	}

	public void setSectionCylinderLowerToTotalHeightRatio(Double sectionCylinderLowerToTotalHeightRatio) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setSectionCylinderLowerToTotalHeightRatio(sectionCylinderLowerToTotalHeightRatio).build());
	}

	public Double getSectionNoseMidLowerToTotalHeightRatio() {
		return theFuselageCreatorInterface.getSectionNoseMidLowerToTotalHeightRatio();
	}

	public void setSectionNoseMidLowerToTotalHeightRatio(Double sectionNoseMidLowerToTotalHeightRatio) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setSectionNoseMidLowerToTotalHeightRatio(sectionNoseMidLowerToTotalHeightRatio).build());
	}

	public Double getSectionTailMidLowerToTotalHeightRatio() {
		return theFuselageCreatorInterface.getSectionTailMidLowerToTotalHeightRatio();
	}

	public void setSectionTailMidLowerToTotalHeightRatio(Double sectionTailMidLowerToTotalHeightRatio) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setSectionTailMidLowerToTotalHeightRatio(sectionTailMidLowerToTotalHeightRatio).build());
	}

	public Double getSectionCylinderRhoUpper() {
		return theFuselageCreatorInterface.getSectionCylinderRhoLower();
	}

	public void setSectionCylinderRhoUpper(Double sectionCylinderRhoUpper) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setSectionCylinderRhoUpper(sectionCylinderRhoUpper).build());
	}

	public Double getSectionCylinderRhoLower() {
		return theFuselageCreatorInterface.getSectionCylinderRhoLower();
	}

	public void setSectionCylinderRhoLower(Double sectionCylinderRhoLower) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setSectionCylinderRhoLower(sectionCylinderRhoLower).build());
	}

	public Double getSectionMidNoseRhoUpper() {
		return theFuselageCreatorInterface.getSectionMidNoseRhoUpper();
	}

	public void setSectionMidNoseRhoUpper(Double sectionMidNoseRhoUpper) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setSectionMidNoseRhoUpper(sectionMidNoseRhoUpper).build());
	}

	public Double getSectionMidNoseRhoLower() {
		return theFuselageCreatorInterface.getSectionMidNoseRhoLower();
	}

	public void setSectionMidNoseRhoLower(Double sectionMidNoseRhoLower) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setSectionMidNoseRhoLower(sectionMidNoseRhoLower).build());
	}

	public Double getSectionMidTailRhoUpper() {
		return theFuselageCreatorInterface.getSectionMidTailRhoUpper();
	}

	public void setSectionMidTailRhoUpper(Double sectionMidTailRhoUpper) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setSectionMidTailRhoUpper(sectionMidTailRhoUpper).build());
	}

	public Double getSectionMidTailRhoLower() {
		return theFuselageCreatorInterface.getSectionMidTailRhoLower();
	}

	public void setSectionMidTailRhoLower(Double sectionMidTailRhoLower) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setSectionMidTailRhoLower(sectionMidTailRhoLower).build());
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

	public double getNoseCapOffsetPercent() {
		return theFuselageCreatorInterface.getNoseCapOffsetPercent();
	}

	public void setNoseCapOffsetPercent(double dxNoseCapPercent) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setNoseCapOffsetPercent(dxNoseCapPercent).build());
	}

	public double getTailCapOffsetPercent() {
		return theFuselageCreatorInterface.getTailCapOffsetPercent();
	}

	public void setTailCapOffsetPercent(double dxTailCapPercent) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setTailCapOffsetPercent(dxTailCapPercent).build());
	}

	public List<Double> getOutlineXZUpperCurveX() {
		return outlineXZUpperCurveX;
	}

	public List<Amount<Length>> getOutlineXZUpperCurveAmountX() {
		return outlineXZUpperCurveX.stream()
				.map(x -> Amount.valueOf(x, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setOutlineXZUpperCurveX(List<Double> outlineXZUpperCurveX) {
		this.outlineXZUpperCurveX = outlineXZUpperCurveX;
	}

	public List<Double> getOutlineXZUpperCurveZ() {
		return outlineXZUpperCurveZ;
	}

	public List<Amount<Length>> getOutlineXZUpperCurveAmountZ() {
		return outlineXZUpperCurveZ.stream()
				.map(z -> Amount.valueOf(z, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setOutlineXZUpperCurveZ(List<Double> outlineXZUpperCurveZ) {
		this.outlineXZUpperCurveZ = outlineXZUpperCurveZ;
	}

	public List<Double> getOutlineXZLowerCurveX() {
		return outlineXZLowerCurveX;
	}

	public List<Amount<Length>> getOutlineXZLowerCurveAmountX() {
		return outlineXZLowerCurveX.stream()
				.map(x -> Amount.valueOf(x, SI.METER))
				.collect(Collectors.toList());
	}

	public void setOutlineXZLowerCurveX(List<Double> outlineXZLowerCurveX) {
		this.outlineXZLowerCurveX = outlineXZLowerCurveX;
	}

	public List<Double> getOutlineXZLowerCurveZ() {
		return outlineXZLowerCurveZ;
	}

	public List<Amount<Length>> getOutlineXZLowerCurveAmountZ() {
		return outlineXZLowerCurveZ.stream()
				.map(z -> Amount.valueOf(z, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setOutlineXZLowerCurveZ(List<Double> outlineXZLowerCurveZ) {
		this.outlineXZLowerCurveZ = outlineXZLowerCurveZ;
	}

	public List<Double> getOutlineXZCamberLineX() {
		return outlineXZCamberLineX;
	}

	public List<Amount<Length>> getOutlineXZCamberLineAmountX() {
		return outlineXZCamberLineX.stream()
				.map(x -> Amount.valueOf(x, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setOutlineXZCamberLineX(List<Double> outlineXZCamberLineX) {
		this.outlineXZCamberLineX = outlineXZCamberLineX;
	}

	public List<Double> getOutlineXZCamberLineZ() {
		return outlineXZCamberLineZ;
	}

	public List<Amount<Length>> getOutlineXZCamberLineAmountZ() {
		return outlineXZCamberLineZ.stream()
				.map(z -> Amount.valueOf(z, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setOutlineXZCamberLineZ(List<Double> outlineXZCamberLineZ) {
		this.outlineXZCamberLineZ = outlineXZCamberLineZ;
	}

	public List<Double> getOutlineXYSideRCurveX() {
		return outlineXYSideRCurveX;
	}

	public List<Amount<Length>> getOutlineXYSideRCurveAmountX() {
		return outlineXYSideRCurveX.stream()
				.map(x -> Amount.valueOf(x, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setOutlineXYSideRCurveX(List<Double> outlineXYSideRCurveX) {
		this.outlineXYSideRCurveX = outlineXYSideRCurveX;
	}

	public List<Double> getOutlineXYSideRCurveY() {
		return outlineXYSideRCurveY;
	}

	public List<Amount<Length>> getOutlineXYSideRCurveAmountY() {
		return outlineXYSideRCurveY.stream()
				.map(y -> Amount.valueOf(y, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setOutlineXYSideRCurveY(List<Double> outlineXYSideRCurveY) {
		this.outlineXYSideRCurveY = outlineXYSideRCurveY;
	}

	public List<Double> getOutlineXYSideRCurveZ() {
		return outlineXYSideRCurveZ;
	}

	public List<Amount<Length>> getOutlineXYSideRCurveAmountZ() {
		return outlineXYSideRCurveZ.stream()
				.map(z -> Amount.valueOf(z, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setOutlineXYSideRCurveZ(List<Double> outlineXYSideRCurveZ) {
		this.outlineXYSideRCurveZ = outlineXYSideRCurveZ;
	}

	public List<Double> getOutlineXYSideLCurveX() {
		return outlineXYSideLCurveX;
	}

	public List<Amount<Length>> getOutlineXYSideLCurveAmountX() {
		return outlineXYSideLCurveX.stream()
				.map(x -> Amount.valueOf(x, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setOutlineXYSideLCurveX(List<Double> outlineXYSideLCurveX) {
		this.outlineXYSideLCurveX = outlineXYSideLCurveX;
	}

	public List<Double> getOutlineXYSideLCurveY() {
		return outlineXYSideLCurveY;
	}

	public List<Amount<Length>> getOutlineXYSideLCurveAmountY() {
		return outlineXYSideLCurveY.stream()
				.map(y -> Amount.valueOf(y, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setOutlineXYSideLCurveY(List<Double> outlineXYSideLCurveY) {
		this.outlineXYSideLCurveY = outlineXYSideLCurveY;
	}

	public List<Double> getOutlineXYSideLCurveZ() {
		return outlineXYSideLCurveZ;
	}

	public List<Amount<Length>> getOutlineXYSideLCurveAmountZ() {
		return outlineXYSideLCurveZ.stream()
				.map(z -> Amount.valueOf(z, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setOutlineXYSideLCurveZ(List<Double> outlineXYSideLCurveZ) {
		this.outlineXYSideLCurveZ = outlineXYSideLCurveZ;
	}

	public List<Double> getSectionUpperCurveY() {
		return sectionUpperCurveY;
	}

	public List<Amount<Length>> getSectionUpperCurveAmountY() {
		return sectionUpperCurveY.stream()
				.map(y -> Amount.valueOf(y, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setSectionUpperCurveY(List<Double> sectionUpperCurveY) {
		this.sectionUpperCurveY = sectionUpperCurveY;
	}

	public List<Double> getSectionUpperCurveZ() {
		return sectionUpperCurveZ;
	}

	public List<Amount<Length>> getSectionUpperCurveAmountZ() {
		return sectionUpperCurveZ.stream()
				.map(z -> Amount.valueOf(z, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setSectionUpperCurveZ(List<Double> sectionUpperCurveZ) {
		this.sectionUpperCurveZ = sectionUpperCurveZ;
	}

	public List<Double> getSectionLowerCurveY() {
		return sectionLowerCurveY;
	}

	public List<Amount<Length>> getSectionLowerCurveAmountY() {
		return sectionLowerCurveY.stream()
				.map(y -> Amount.valueOf(y, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setSectionLowerCurveY(List<Double> sectionLowerCurveY) {
		this.sectionLowerCurveY = sectionLowerCurveY;
	}

	public List<Double> getSectionLowerCurveZ() {
		return sectionLowerCurveZ;
	}

	public List<Amount<Length>> getSectionLowerCurveAmountZ() {
		return sectionLowerCurveZ.stream()
				.map(z -> Amount.valueOf(z, SI.METER))
				.collect(Collectors.toList());
	}
	
	public void setSectionLowerCurveZ(List<Double> sectionLowerCurveZ) {
		this.sectionLowerCurveZ = sectionLowerCurveZ;
	}

	public List<FuselageCurvesSection> getSectionsYZ() {
		return sectionsYZ;
	}

	public void setSectionsYZ(List<FuselageCurvesSection> sectionsYZ) {
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

	
	public List<SpoilerCreator> getSpoilers() {
		return theFuselageCreatorInterface.getSpoilers();
	}
	
	public int getDeckNumber() {
		return theFuselageCreatorInterface.getDeckNumber();
	}

	
	public void setDeckNumber(int deckNumber) {
		setTheFuselageCreatorInterface(IFuselageCreator.Builder.from(theFuselageCreatorInterface).setDeckNumber(deckNumber).build());
	}

	
	public Amount<Mass> getMassReference() {
		return massReference;
	}

	
	public void setMassReference(Amount<Mass> massReference) {
		this.massReference = massReference;
	}
	
	
	public void setSpoilers(List<SpoilerCreator> spoilers) {
		setTheFuselageCreatorInterface(
				IFuselageCreator.Builder.from(theFuselageCreatorInterface)
				.clearSpoilers()
				.addAllSpoilers(spoilers)
				.build()
				);
	}

	public int getNumberPointsNose() {
		return npN;
	}

	public int getNumberPointsCylinder() {
		return npC;
	}

	public int getNumberPointsTail() {
		return npT;
	}

	public int getNumberPointsSectionUpper() {
		return npSecUp;
	}

	public int getNumberPointsSectionLower() {
		return npSecLow;
	}
	
}
