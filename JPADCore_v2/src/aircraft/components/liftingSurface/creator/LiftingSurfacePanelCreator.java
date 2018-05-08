package aircraft.components.liftingSurface.creator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import aircraft.components.liftingSurface.airfoils.Airfoil;
import configuration.MyConfiguration;
import standaloneutils.MyXMLReaderUtils;

public class LiftingSurfacePanelCreator {

	//-----------------------------------------------------------------
	// VARIABLE DECLARATION
	private ILiftingSurfacePanelCreator _theLiftingSurfacePanelInterface;
	
	private String _airfoilRootPath;
	private String _airfoilTipPath;
	private Amount<Angle> _sweepQuarterChord, _sweepHalfChord, _sweepTrailingEdge;
	private Amount<Area> _surfacePlanform;
	private Amount<Area> _surfaceWetted;
	private Double _aspectRatio;
	private Double _taperRatio;
	private Amount<Length> _meanAerodynamicChordLeadingEdgeZ;
	private Amount<Length> _meanAerodynamicChordLeadingEdgeY;
	private Amount<Length> _meanAerodynamicChordLeadingEdgeX;
	private Amount<Length> _meanAerodynamicChord;
	
	//-----------------------------------------------------------------
	// BUILDER
	public LiftingSurfacePanelCreator (ILiftingSurfacePanelCreator theLiftingSurfacePanelInterface) {
		_theLiftingSurfacePanelInterface = theLiftingSurfacePanelInterface;
		calculateGeometry();
	}
	
	//-----------------------------------------------------------------
	// METHODS
	/*
	 * Given root chord, tip chord, span, l.e. sweep angle
	 * calculates the rest of the wing parameters:
	 * - planform and wetted surface, 
	 * - taper ratio, 
	 * - aspect ratio, 
	 * - sweep angle of quarter-chord line,
	 * - sweep angle of half-chord line,
	 * - sweep angle of trailing edge line,
	 * - mean aerodynamic chord and its position in LRF 
	 */
	@SuppressWarnings("unchecked")
	public void calculateGeometry() {

		_taperRatio = _theLiftingSurfacePanelInterface.getChordRoot().to(SI.METER)
				.divide(_theLiftingSurfacePanelInterface.getChordRoot().to(SI.METER))
				.getEstimatedValue();
		_surfacePlanform = (Amount<Area>) (_theLiftingSurfacePanelInterface.getChordRoot().to(SI.METER)
				.plus(_theLiftingSurfacePanelInterface.getChordTip().to(SI.METER)))
				.times(_theLiftingSurfacePanelInterface.getSpan().to(SI.METER))
				.divide(2);
		_surfaceWetted = _surfacePlanform.to(SI.SQUARE_METRE).times(2.0);
		_aspectRatio = _theLiftingSurfacePanelInterface.getSpan().to(SI.METER)
				.times(_theLiftingSurfacePanelInterface.getSpan().to(SI.METER))
				.divide(_surfacePlanform.to(SI.SQUARE_METRE))
				.getEstimatedValue();

		_sweepQuarterChord = calculateSweep(0.25);
		_sweepHalfChord = calculateSweep(0.50);
		_sweepTrailingEdge = calculateSweep(1.00);

		_meanAerodynamicChord =
			_theLiftingSurfacePanelInterface.getChordRoot().to(SI.METER).times(2.0/3.0)
				.times(1.0 + _taperRatio + _taperRatio*_taperRatio)
				.divide(1.0 + _taperRatio)
			;
		_meanAerodynamicChordLeadingEdgeY =
				_theLiftingSurfacePanelInterface.getSpan().to(SI.METER)
					.divide(6)
					.times(1 + 2.0*_taperRatio)
					.divide(1.0 + _taperRatio);

		_meanAerodynamicChordLeadingEdgeX =
			_meanAerodynamicChordLeadingEdgeY.to(SI.METER)
				.times(Math.tan(_theLiftingSurfacePanelInterface.getSweepLeadingEdge().to(SI.RADIAN).getEstimatedValue()));

		_meanAerodynamicChordLeadingEdgeZ =
			_meanAerodynamicChordLeadingEdgeY.to(SI.METER)
				.times(Math.tan(_theLiftingSurfacePanelInterface.getDihedral().to(SI.RADIAN).getEstimatedValue()));

	}

	private static LiftingSurfacePanelCreator importFromPanelNodeImpl(Document doc, String airfoilsDir) {

		boolean isLinked = false;
		Amount<Length> span = null;
		Amount<Angle> dihedral = null;
		Amount<Angle> sweepLeadingEdge = null;
		Amount<Length> chordRoot = null;
		Amount<Length> chordTip = null;
		Amount<Angle> twistGeometricRoot = null;
		Amount<Angle> twistGeometricTip = null;
		
		System.out.println("Reading lifting surface panel data from XML doc ...");
		
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//panel/@id");
		
		String isLinkedProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//panel/@linked_to");
		if(isLinkedProperty != null)
			isLinked = true;

		String spanProperty = MyXMLReaderUtils.getXMLPropertyByPath(doc, xpath, "//span/text()");
		if(spanProperty != null)
			span = MyXMLReaderUtils.getXMLAmountLengthByPath(doc, xpath, "//span");
		
		String dihedralProperty = MyXMLReaderUtils.getXMLPropertyByPath(doc, xpath, "//dihedral/text()");
		if(dihedralProperty != null)
			dihedral = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//dihedral");
		
		String sweepLeadingEdgeProperty = MyXMLReaderUtils.getXMLPropertyByPath(doc, xpath, "//sweep_leading_edge/text()");
		if(sweepLeadingEdgeProperty != null)
			sweepLeadingEdge = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//sweep_leading_edge");
		
		String chordRootProperty = MyXMLReaderUtils.getXMLPropertyByPath(doc, xpath, "//inner_section/chord/text()");
		if(chordRootProperty != null)
			chordRoot = MyXMLReaderUtils.getXMLAmountLengthByPath(doc, xpath, "//inner_section/chord");

		String airfoilFileName1 =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//inner_section/airfoil/@file");
		String airFoilPath1 = "";
		if(airfoilFileName1 != null)
			airFoilPath1 = airfoilsDir + File.separator + airfoilFileName1;
		
		Airfoil airfoilRoot = Airfoil.importFromXML(airFoilPath1);

		String twistGeometricRootProperty = MyXMLReaderUtils.getXMLPropertyByPath(doc, xpath, "//inner_section/geometric_twist/text()");
		if(twistGeometricRootProperty != null)
			twistGeometricRoot = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//inner_section/geometric_twist");
		
		String chordTipProperty = MyXMLReaderUtils.getXMLPropertyByPath(doc, xpath, "//outer_section/chord/text()");
		if(chordTipProperty != null)
			chordTip = MyXMLReaderUtils.getXMLAmountLengthByPath(doc, xpath, "//outer_section/chord");

		String airfoilFileName2 =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//outer_section/airfoil/@file");
		String airFoilPath2 = "";
		if(airfoilFileName2 != null)
			airFoilPath2 = airfoilsDir + File.separator + airfoilFileName2;
		
		Airfoil airfoilTip = Airfoil.importFromXML(airFoilPath2);

		String twistGeometricTipProperty = MyXMLReaderUtils.getXMLPropertyByPath(doc, xpath, "//outer_section/geometric_twist/text()");
		if(twistGeometricTipProperty != null)
			twistGeometricTip = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//outer_section/geometric_twist");
		
		// create the wing panel via its builder
		LiftingSurfacePanelCreator panel = new LiftingSurfacePanelCreator(
				new ILiftingSurfacePanelCreator.Builder()
				.setId(id)
				.setLinkedTo(isLinked)
				.setChordRoot(chordRoot)
				.setChordTip(chordTip)
				.setAirfoilRoot(airfoilRoot)
				.setAirfoilRootFilePath(airFoilPath1)
				.setAirfoilTip(airfoilTip)
				.setAirfoilTipFilePath(airFoilPath2)
				.setTwistGeometricAtRoot(twistGeometricRoot)
				.setTwistGeometricAtTip(twistGeometricTip)
				.setSpan(span)
				.setSweepLeadingEdge(sweepLeadingEdge)
				.setDihedral(dihedral)
				.build()
				);

		return panel;
	}

	public static LiftingSurfacePanelCreator importFromPanelNode(Node nodePanel, String airfoilsDir) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(nodePanel, true);
			doc.appendChild(importedNode);
			return LiftingSurfacePanelCreator.importFromPanelNodeImpl(doc, airfoilsDir);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static LiftingSurfacePanelCreator importFromPanelNodeLinkedImpl(Document doc, LiftingSurfacePanelCreator panel0, String airfoilsDir) {

		boolean isLinked = false;
		Amount<Length> span = null;
		Amount<Angle> dihedral = null;
		Amount<Angle> sweepLeadingEdge = null;
		Amount<Length> chordRoot = null;
		Amount<Length> chordTip = null;
		Amount<Angle> twistGeometricRoot = null;
		Amount<Angle> twistGeometricTip = null;
		
		System.out.println("Reading LINKED lifting surface panel data from XML doc ...");

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//panel/@id");
		
		String isLinkedProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//panel/@linked_to");
		if (isLinkedProperty != null)
			isLinked = true;

		String spanProperty = MyXMLReaderUtils.getXMLPropertyByPath(doc, xpath, "//span/text()");
		if(spanProperty != null)
			span = MyXMLReaderUtils.getXMLAmountLengthByPath(doc, xpath, "//span");
		
		String dihedralProperty = MyXMLReaderUtils.getXMLPropertyByPath(doc, xpath, "//dihedral/text()");
		if(dihedralProperty != null)
			dihedral = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//dihedral");
		
		String sweepLeadingEdgeProperty = MyXMLReaderUtils.getXMLPropertyByPath(doc, xpath, "//sweep_leading_edge/text()");
		if(sweepLeadingEdgeProperty != null)
			sweepLeadingEdge = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//sweep_leading_edge");

		chordRoot = panel0.getChordTip(); // from linked panel

		Airfoil airfoilRoot = panel0.getAirfoilTip(); // from linked panel
		String airfoilRootPath = panel0.getAirfoilRootPath();
		
		twistGeometricRoot = panel0.getTwistAerodynamicAtTip();
		
		String chordTipProperty = MyXMLReaderUtils.getXMLPropertyByPath(doc, xpath, "//outer_section/chord/text()");
		if(chordTipProperty != null)
			chordTip = MyXMLReaderUtils.getXMLAmountLengthByPath(doc, xpath, "//outer_section/chord");

		String airfoilFileName2 =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//outer_section/airfoil/@file");
		String airFoilPath2 = "";
		if(airfoilFileName2 != null)
			airFoilPath2 = airfoilsDir + File.separator + airfoilFileName2;
		
		Airfoil airfoilTip = Airfoil.importFromXML(airFoilPath2);

		String twistGeometricTipProperty = MyXMLReaderUtils.getXMLPropertyByPath(doc, xpath, "//outer_section/geometric_twist/text()");
		if(twistGeometricTipProperty != null)
			twistGeometricTip = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//outer_section/geometric_twist");
		
		// create the wing panel via its builder
		LiftingSurfacePanelCreator panel =
			new LiftingSurfacePanelCreator(
					new ILiftingSurfacePanelCreator.Builder()
					.setId(id)
					.setLinkedTo(isLinked)
					.setChordRoot(chordRoot)
					.setChordTip(chordTip)
					.setAirfoilRoot(airfoilRoot)
					.setAirfoilRootFilePath(airfoilRootPath)
					.setAirfoilTip(airfoilTip)
					.setAirfoilTipFilePath(airFoilPath2)
					.setTwistGeometricAtRoot(twistGeometricRoot)
					.setTwistGeometricAtTip(twistGeometricTip)
					.setSpan(span)
					.setSweepLeadingEdge(sweepLeadingEdge)
					.setDihedral(dihedral)
					.buildPartial()
					);

		return panel;
	}

	public static LiftingSurfacePanelCreator importFromPanelNodeLinked(Node nodePanel, LiftingSurfacePanelCreator panel0, String airfoilsDir) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(nodePanel, true);
			doc.appendChild(importedNode);
			return LiftingSurfacePanelCreator.importFromPanelNodeLinkedImpl(doc, panel0, airfoilsDir);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
			.append("\t-------------------------------------\n")
			.append("\tLifting surface panel\n")
			.append("\t-------------------------------------\n")
			.append("\tID: '" + _theLiftingSurfacePanelInterface.getId() + "'\n")
			.append("\tSpan = " + _theLiftingSurfacePanelInterface.getSpan().to(SI.METER) + "\n")
			.append("\tLambda_LE = " + _theLiftingSurfacePanelInterface.getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tLambda_c/4 = " + _sweepQuarterChord.to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tLambda_c/2 = " + _sweepHalfChord.to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tLambda_TE = " + _sweepTrailingEdge.to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tLambda_TE = " + _sweepTrailingEdge.to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\t.....................................\n")
			.append("\t                           panel root\n")
			.append("\tc_r = " + _theLiftingSurfacePanelInterface.getChordRoot().to(SI.METER) + "\n")
			.append("\tepsilon_r = " + _theLiftingSurfacePanelInterface.getTwistGeometricAtRoot().to(NonSI.DEGREE_ANGLE) + "\n")
			.append(_theLiftingSurfacePanelInterface.getAirfoilRoot() + "\n")
			.append("\t.....................................\n")
			.append("\t                            panel tip\n")
			.append("\tc_t = " + _theLiftingSurfacePanelInterface.getChordRoot().to(SI.METER) + "\n")
			.append("\tepsilon_t = " + _theLiftingSurfacePanelInterface.getTwistGeometricAtRoot().to(NonSI.DEGREE_ANGLE) + "\n")
			.append(_theLiftingSurfacePanelInterface.getAirfoilTip() + "\n")
			.append("\t.....................................\n")
			.append("\t                   panel derived data\n")
			.append("\tS = " + _surfacePlanform.to(SI.SQUARE_METRE) + "\n")
			.append("\tS_wet = " + _surfaceWetted.to(SI.SQUARE_METRE) + "\n")
			.append("\tlambda = " + _taperRatio + "\n")
			.append("\tAR = " + _aspectRatio + "\n")
			.append("\tc_MAC = " + _meanAerodynamicChord.to(SI.METER) + "\n")
			.append("\tX_LE_MAC = " + _meanAerodynamicChordLeadingEdgeX.to(SI.METER) + "\n")
			.append("\tY_LE_MAC = " + _meanAerodynamicChordLeadingEdgeY.to(SI.METER) + "\n")
			.append("\tZ_LE_MAC = " + _meanAerodynamicChordLeadingEdgeZ.to(SI.METER) + "\n")
			;
		return sb.toString();

	}
	
	/**
	 * Calculate sweep at x fraction of chords, known sweep at LE
	 *
	 * @param x (0<= x <=1)
	 * @return
	 */
	public Amount<Angle> calculateSweep(Double x) {
		return
			Amount.valueOf(
				Math.atan(
						Math.tan( _theLiftingSurfacePanelInterface.getSweepLeadingEdge().to(SI.RADIAN).getEstimatedValue() )
						- (4./_aspectRatio)*
						( x*(1 - _taperRatio)/(1 + _taperRatio)) ),
			1e-9, // precision
			SI.RADIAN);
	}

	public List<Amount<Length>> getMeanAerodynamicChordLeadingEdge() {
		List<Amount<Length>> list = new ArrayList<Amount<Length>>();
		list.add(_meanAerodynamicChordLeadingEdgeX);
		list.add(_meanAerodynamicChordLeadingEdgeY);
		list.add(_meanAerodynamicChordLeadingEdgeZ);
		return list;
	}
	
	public Amount<Angle> getTwistAerodynamicAtTip() {
		return
			_theLiftingSurfacePanelInterface.getTwistGeometricAtTip()
			.minus(_theLiftingSurfacePanelInterface.getAirfoilTip().getAlphaZeroLift())
			.plus(_theLiftingSurfacePanelInterface.getAirfoilRoot().getAlphaZeroLift());
	}
	
	//-----------------------------------------------------------------
	// GETTERS & SETTERS
	public ILiftingSurfacePanelCreator getTheLiftingSurfacePanelInterface() {
		return _theLiftingSurfacePanelInterface;
	}

	public void setTheLiftingSurfacePanelInterface(ILiftingSurfacePanelCreator _theLiftingSurfacePanelInterface) {
		this._theLiftingSurfacePanelInterface = _theLiftingSurfacePanelInterface;
	}

	public Amount<Length> getChordRoot() {
		return _theLiftingSurfacePanelInterface.getChordRoot();
	}

	public void setChordRoot(Amount<Length> cr) {
		setTheLiftingSurfacePanelInterface(ILiftingSurfacePanelCreator.Builder.from(_theLiftingSurfacePanelInterface).setChordRoot(cr).build());
	}

	public Amount<Length> getChordTip() {
		return _theLiftingSurfacePanelInterface.getChordTip();
	}

	public void setChordTip(Amount<Length> ct) {
		setTheLiftingSurfacePanelInterface(ILiftingSurfacePanelCreator.Builder.from(_theLiftingSurfacePanelInterface).setChordTip(ct).build());
	}

	public Airfoil getAirfoilRoot() {
		return _theLiftingSurfacePanelInterface.getAirfoilRoot();
	}

	public void setAirfoilRoot(Airfoil a) {
		setTheLiftingSurfacePanelInterface(ILiftingSurfacePanelCreator.Builder.from(_theLiftingSurfacePanelInterface).setAirfoilRoot(a).build());
	}

	public Airfoil getAirfoilTip() {
		return _theLiftingSurfacePanelInterface.getAirfoilTip();
	}

	public void setAirfoilTip(Airfoil a) {
		setTheLiftingSurfacePanelInterface(ILiftingSurfacePanelCreator.Builder.from(_theLiftingSurfacePanelInterface).setAirfoilTip(a).build());
	}

	public Amount<Angle> getSweepLeadingEdge() {
		return _theLiftingSurfacePanelInterface.getSweepLeadingEdge();
	}

	public Amount<Angle> getSweepQuarterChord() {
		return _sweepQuarterChord;
	}

	public Amount<Angle> getSweepHalfChord() {
		return _sweepHalfChord;
	}

	public Amount<Angle> getSweepAtTrailingEdge() {
		return _sweepTrailingEdge;
	}

	public void setSweepAtLeadingEdge(Amount<Angle> lambda) {
		setTheLiftingSurfacePanelInterface(ILiftingSurfacePanelCreator.Builder.from(_theLiftingSurfacePanelInterface).setSweepLeadingEdge(lambda).build());
	}

	public Amount<Angle> getDihedral() {
		return _theLiftingSurfacePanelInterface.getDihedral();
	}

	public void setDihedral(Amount<Angle> gamma) {
		setTheLiftingSurfacePanelInterface(ILiftingSurfacePanelCreator.Builder.from(_theLiftingSurfacePanelInterface).setDihedral(gamma).build());
	}

	public Amount<Length> getMeanAerodynamicChord() {
		return _meanAerodynamicChord;
	}

	public Amount<Length> getMeanAerodynamicChordLeadingEdgeX() {
		return _meanAerodynamicChordLeadingEdgeX;
	}

	public Amount<Length> getMeanAerodynamicChordLeadingEdgeY() {
		return _meanAerodynamicChordLeadingEdgeY;
	}

	public Amount<Length> getMeanAerodynamicChordLeadingEdgeZ() {
		return _meanAerodynamicChordLeadingEdgeZ;
	}

	public Amount<Area> getSurfacePlanform() {
		return _surfacePlanform;
	}

	public Amount<Area> getSurfaceWetted() {
		return _surfaceWetted;
	}

	public Double getAspectRatio() {
		return _aspectRatio;
	}

	public Double getTaperRatio() {
		return _taperRatio;
	}

	public Amount<Length> getSpan() {
		return _theLiftingSurfacePanelInterface.getSpan();
	}

	public void setSpan(Amount<Length> s) {
		setTheLiftingSurfacePanelInterface(ILiftingSurfacePanelCreator.Builder.from(_theLiftingSurfacePanelInterface).setSpan(s).build());
	}

	public Amount<Angle> getTwistGeometricAtTip() {
		return _theLiftingSurfacePanelInterface.getTwistGeometricAtTip();
	}

	public void setTwistGeometricAtTip(Amount<Angle> epsilonG) {
		setTheLiftingSurfacePanelInterface(ILiftingSurfacePanelCreator.Builder.from(_theLiftingSurfacePanelInterface).setTwistGeometricAtTip(epsilonG).build());
	}

	public String getId() {
		return _theLiftingSurfacePanelInterface.getId();
	}
	
	public void setId(String id) {
		setTheLiftingSurfacePanelInterface(ILiftingSurfacePanelCreator.Builder.from(_theLiftingSurfacePanelInterface).setId(id).build());
	}

	public boolean isLinked() {
		return _theLiftingSurfacePanelInterface.isLinkedTo();
	}

	public void setLinked(boolean isLinked) {
		setTheLiftingSurfacePanelInterface(ILiftingSurfacePanelCreator.Builder.from(_theLiftingSurfacePanelInterface).setLinkedTo(isLinked).build());
	}

	public Amount<Angle> getTwistGeometricRoot() {
		return _theLiftingSurfacePanelInterface.getTwistGeometricAtRoot();
	}

	public void setTwistGeometricRoot(Amount<Angle> _twistGeometricRoot) {
		setTheLiftingSurfacePanelInterface(ILiftingSurfacePanelCreator.Builder.from(_theLiftingSurfacePanelInterface).setTwistGeometricAtRoot(_twistGeometricRoot).build());
	}

	public String getAirfoilRootPath() {
		return _airfoilRootPath;
	}

	public void setAirfoilRootPath(String _airfoilRootPath) {
		this._airfoilRootPath = _airfoilRootPath;
	}

	public String getAirfoilTipPath() {
		return _airfoilTipPath;
	}

	public void setAirfoilTipPath(String _airfoilTipPath) {
		this._airfoilTipPath = _airfoilTipPath;
	}

}
