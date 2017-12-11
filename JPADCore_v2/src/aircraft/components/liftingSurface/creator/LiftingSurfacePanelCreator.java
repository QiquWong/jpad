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

import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import configuration.MyConfiguration;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class LiftingSurfacePanelCreator implements ILiftingSurfacePanelCreator {

	String _id;
	
	
	private boolean _isLinked;
	private Amount<Length> _chordRoot;
	private Amount<Length> _chordTip;
	private AirfoilCreator _airfoilRoot;
	private String _airfoilRootPath;
	private AirfoilCreator _airfoilTip;
	private String _airfoilTipPath;
	private Amount<Angle> _twistGeometricTip;
	private Amount<Angle> _twistGeometricRoot;
	private Amount<Length> _span;
	private Amount<Angle> _sweepLeadingEdge,
		_sweepQuarterChord, _sweepHalfChord, _sweepTrailingEdge;
	private Amount<Angle> _dihedral;
	private Amount<Area> _surfacePlanform;
	private Amount<Area> _surfaceWetted;
	private Double _aspectRatio;
	private Double _taperRatio;
	private Amount<Length> _meanAerodynamicChordLeadingEdgeZ;
	private Amount<Length> _meanAerodynamicChordLeadingEdgeY;
	private Amount<Length> _meanAerodynamicChordLeadingEdgeX;
	private Amount<Length> _meanAerodynamicChord;

	@SuppressWarnings("unchecked")
	@Override
	public void calculateGeometry() {

		_taperRatio = _chordTip.to(SI.METER).divide(_chordRoot.to(SI.METER)).getEstimatedValue();
		_surfacePlanform = (Amount<Area>) (_chordRoot.to(SI.METER).plus(_chordTip.to(SI.METER))).times(_span.to(SI.METER)).divide(2);
		_surfaceWetted = _surfacePlanform.to(SI.SQUARE_METRE).times(2.0);
		_aspectRatio = _span.to(SI.METER).times(_span.to(SI.METER)).divide(_surfacePlanform.to(SI.SQUARE_METRE)).getEstimatedValue();

		_sweepQuarterChord = calculateSweep(0.25);
		_sweepHalfChord = calculateSweep(0.50);
		_sweepTrailingEdge = calculateSweep(1.00);

		_meanAerodynamicChord =
			_chordRoot.to(SI.METER).times(2.0/3.0)
				.times(1.0 + _taperRatio + _taperRatio*_taperRatio)
				.divide(1.0 + _taperRatio)
			;
		_meanAerodynamicChordLeadingEdgeY =
				_span.to(SI.METER)
					.divide(6)
					.times(1 + 2.0*_taperRatio)
					.divide(1.0 + _taperRatio);

		_meanAerodynamicChordLeadingEdgeX =
			_meanAerodynamicChordLeadingEdgeY.to(SI.METER)
				.times(Math.tan(_sweepLeadingEdge.to(SI.RADIAN).getEstimatedValue()));

		_meanAerodynamicChordLeadingEdgeZ =
			_meanAerodynamicChordLeadingEdgeY.to(SI.METER)
				.times(Math.tan(_dihedral.to(SI.RADIAN).getEstimatedValue()));

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
						Math.tan( _sweepLeadingEdge.to(SI.RADIAN).getEstimatedValue() )
						- (4./_aspectRatio)*
						( x*(1 - _taperRatio)/(1 + _taperRatio)) ),
			1e-9, // precision
			SI.RADIAN);
	}


	@Override
	public Amount<Length> getChordRoot() {
		return _chordRoot;
	}

	@Override
	public void setChordRoot(Amount<Length> cr) {
		_chordRoot = cr;
		calculateGeometry();
	}

	@Override
	public Amount<Length> getChordTip() {
		return _chordTip;
	}

	@Override
	public void setChordTip(Amount<Length> ct) {
		_chordTip = ct;
		calculateGeometry();
	}

	@Override
	public AirfoilCreator getAirfoilRoot() {
		return _airfoilRoot;
	}

	@Override
	public void setAirfoilRoot(AirfoilCreator a) {
		_airfoilRoot = a;
	}

	@Override
	public AirfoilCreator getAirfoilTip() {
		return _airfoilTip;
	}

	@Override
	public void setAirfoilTip(AirfoilCreator a) {
		_airfoilTip = a;
	}

	@Override
	public Amount<Angle> getSweepLeadingEdge() {
		return _sweepLeadingEdge;
	}

	@Override
	public Amount<Angle> getSweepQuarterChord() {
		return _sweepQuarterChord;
	}

	@Override
	public Amount<Angle> getSweepHalfChord() {
		return _sweepHalfChord;
	}

	@Override
	public Amount<Angle> getSweepAtTrailingEdge() {
		return _sweepTrailingEdge;
	}

	@Override
	public void setSweepAtLeadingEdge(Amount<Angle> lambda) {
		_sweepLeadingEdge = lambda;
		calculateGeometry();
	}

	@Override
	public Amount<Angle> getDihedral() {
		return _dihedral;
	}

	@Override
	public void setDihedral(Amount<Angle> gamma) {
		_dihedral = gamma;
		calculateGeometry();
	}

	@Override
	public Amount<Length> getMeanAerodynamicChord() {
		return _meanAerodynamicChord;
	}

	@Override
	public List<Amount<Length>> getMeanAerodynamicChordLeadingEdge() {
		List<Amount<Length>> list = new ArrayList<Amount<Length>>();
		list.add(_meanAerodynamicChordLeadingEdgeX);
		list.add(_meanAerodynamicChordLeadingEdgeY);
		list.add(_meanAerodynamicChordLeadingEdgeZ);
		return list;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeX() {
		return _meanAerodynamicChordLeadingEdgeX;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeY() {
		return _meanAerodynamicChordLeadingEdgeY;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeZ() {
		return _meanAerodynamicChordLeadingEdgeZ;
	}

	@Override
	public Amount<Area> getSurfacePlanform() {
		return _surfacePlanform;
	}

	@Override
	public Amount<Area> getSurfaceWetted() {
		return _surfaceWetted;
	}

	@Override
	public Double getAspectRatio() {
		return _aspectRatio;
	}

	@Override
	public Double getTaperRatio() {
		return _taperRatio;
	}

	@Override
	public Amount<Length> getSpan() {
		return _span;
	}

	@Override
	public void setSpan(Amount<Length> s) {
		_span = s;
		calculateGeometry();
	}

	@Override
	public Amount<Angle> getTwistGeometricAtTip() {
		return _twistGeometricTip;
	}

	@Override
	public void setTwistGeometricAtTip(Amount<Angle> epsilonG) {
		_twistGeometricTip = epsilonG;
	}

	@Override
	public Amount<Angle> getTwistAerodynamicAtTip() {
		return
			_twistGeometricTip
			.minus(_airfoilTip.getAlphaZeroLift())
			.plus(_airfoilRoot.getAlphaZeroLift());
	}

	@Override
	public String getId() {
		return _id;
	}
	
	@Override
	public void setId(String id) {
		this._id = id;
	}

	// Builder pattern via a nested public static class

	public static class LiftingSurfacePanelBuilder {
		// required parameters
		private String __id;
		private boolean __isLinked;
		private Amount<Length> __chordRoot;
		private Amount<Length> __chordTip;
		private AirfoilCreator __airfoilRoot;
		private AirfoilCreator __airfoilTip;
		private Amount<Angle> __twistGeometricRoot;
		private Amount<Angle> __twistGeometricTip;
		private Amount<Length> __span;
		private Amount<Angle> __sweepLeadingEdge;
		private Amount<Angle> __dihedral;

		// optional parameters ... defaults
		// ...
		private String __airfoilRootPath;
		private String __airfoilTipPath;

		public LiftingSurfacePanelBuilder(
				String id,
				boolean isLinked,
				Amount<Length> cR, Amount<Length> cT,
				AirfoilCreator airfR, AirfoilCreator airfT,
				Amount<Angle> twistGeometricR,
				Amount<Angle> twistGeometricT,
				Amount<Length> span,
				Amount<Angle> sweepLE,
				Amount<Angle> dih
				){
			this.__id = id;
			this.__isLinked = isLinked;
			this.__chordRoot = cR;
			this.__chordTip = cT;
			this.__airfoilRoot = airfR;
			this.__airfoilTip = airfT;
			this.__twistGeometricRoot = twistGeometricR;
			this.__twistGeometricTip = twistGeometricT;
			this.__span = span;
			this.__sweepLeadingEdge = sweepLE;
			this.__dihedral = dih;
		}

		public LiftingSurfacePanelBuilder setAirfoilRootPath (String path) {
			this.__airfoilRootPath = path;
			return this;
		}
		
		public LiftingSurfacePanelBuilder setAirfoilTipPath (String path) {
			this.__airfoilTipPath = path;
			return this;
		}
		
		public LiftingSurfacePanelCreator build() {
			return new LiftingSurfacePanelCreator(this);
		}

	}

	private LiftingSurfacePanelCreator(LiftingSurfacePanelBuilder builder) {
		_id = builder.__id;
		_isLinked = builder.__isLinked;
		_chordRoot = builder.__chordRoot;
		_chordTip = builder.__chordTip;
		_airfoilRoot = builder.__airfoilRoot;
		_airfoilRootPath = builder.__airfoilRootPath;
		_airfoilTip = builder.__airfoilTip;
		_airfoilTipPath = builder.__airfoilTipPath;
		_twistGeometricRoot = builder.__twistGeometricRoot;
		_twistGeometricTip = builder.__twistGeometricTip;
		_span = builder.__span;
		_sweepLeadingEdge = builder.__sweepLeadingEdge;
		_dihedral = builder.__dihedral;
		calculateGeometry();

	}

	public static LiftingSurfacePanelCreator importFromXML(String pathToXML, String airfoilsDir) {
		
		boolean isLinked = false;
		Amount<Length> span = null;
		Amount<Angle> dihedral = null;
		Amount<Angle> sweepLeadingEdge = null;
		Amount<Length> chordRoot = null;
		Amount<Length> chordTip = null;
		Amount<Angle> twistGeometricRoot = null;
		Amount<Angle> twistGeometricTip = null;
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading wing panel data ...");

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//panel/@id");

		String isLinkedProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//panel/@linked_to");
		if(isLinkedProperty != null)
			isLinked = true;
		
		String spanProperty = reader.getXMLPropertyByPath("//panel/span/text()");
		if(spanProperty != null)
			span = reader.getXMLAmountLengthByPath("//panel/span");
		
		String dihedralProperty = reader.getXMLPropertyByPath("//panel/dihedral/text()");
		if(dihedralProperty != null)
			dihedral = reader.getXMLAmountAngleByPath("//panel/dihedral");

		String sweepLEProperty = reader.getXMLPropertyByPath("//panel/sweep_leading_edge/text()");
		if(sweepLEProperty != null)
			sweepLeadingEdge = reader.getXMLAmountAngleByPath("//panel/sweep_leading_edge");

		String chordRootProperty = reader.getXMLPropertyByPath("//panel/inner_section/chord/text()");
		if(chordRootProperty != null)
			chordRoot = reader.getXMLAmountLengthByPath("//panel/inner_section/chord");

		String airfoilFileName1 =
			MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//panel/inner_section/airfoil/@file");
		String airFoilPath1 = "";
		if(airfoilFileName1 != null)
			 airFoilPath1 = airfoilsDir + File.separator + airfoilFileName1;
		
		AirfoilCreator airfoilRoot = AirfoilCreator.importFromXML(airFoilPath1);

		String chordTipProperty = reader.getXMLPropertyByPath("//panel/outer_section/chord/text()");
		if(chordTipProperty != null)
			chordTip = reader.getXMLAmountLengthByPath("//panel/outer_section/chord");

		String twistGeometricRootProperty = reader.getXMLPropertyByPath("//panel/inner_section/geometric_twist/text()");
		if(twistGeometricRootProperty != null)
			twistGeometricRoot = reader.getXMLAmountAngleByPath("//panel/inner_section/geometric_twist");
		
		String airfoilFileName2 =
			MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//panel/outer_section/airfoil/@file");
		String airFoilPath2 = "";
		if(airfoilFileName2 != null)
			airFoilPath2 = airfoilsDir + File.separator + airfoilFileName2;
		
		AirfoilCreator airfoilTip = AirfoilCreator.importFromXML(airFoilPath2);

		String twistGeometricTipProperty = reader.getXMLPropertyByPath("//panel/outer_section/geometric_twist/text()");
		if(twistGeometricTipProperty != null)
			twistGeometricTip = reader.getXMLAmountAngleByPath("//panel/outer_section/geometric_twist");

		// create the wing panel via its builder
		LiftingSurfacePanelCreator panel =
			new LiftingSurfacePanelBuilder(
				id,
				isLinked,
				chordRoot, chordTip,
				airfoilRoot, airfoilTip,
				twistGeometricRoot,
				twistGeometricTip,
				span, sweepLeadingEdge, dihedral
				)
			.setAirfoilRootPath(airFoilPath1)
			.setAirfoilTipPath(airFoilPath2)
			.build();

		return panel;
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
		
		AirfoilCreator airfoilRoot = AirfoilCreator.importFromXML(airFoilPath1);

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
		
		AirfoilCreator airfoilTip = AirfoilCreator.importFromXML(airFoilPath2);

		String twistGeometricTipProperty = MyXMLReaderUtils.getXMLPropertyByPath(doc, xpath, "//outer_section/geometric_twist/text()");
		if(twistGeometricTipProperty != null)
			twistGeometricTip = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//outer_section/geometric_twist");
		
		// create the wing panel via its builder
		LiftingSurfacePanelCreator panel =
			new LiftingSurfacePanelBuilder(
				id,
				isLinked,
				chordRoot, chordTip,
				airfoilRoot, airfoilTip,
				twistGeometricRoot,
				twistGeometricTip,
				span, sweepLeadingEdge, dihedral
				)
			.setAirfoilRootPath(airFoilPath1)
			.setAirfoilTipPath(airFoilPath2)
			.build();

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

		AirfoilCreator airfoilRoot = panel0.getAirfoilTip(); // from linked panel
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
		
		AirfoilCreator airfoilTip = AirfoilCreator.importFromXML(airFoilPath2);

		String twistGeometricTipProperty = MyXMLReaderUtils.getXMLPropertyByPath(doc, xpath, "//outer_section/geometric_twist/text()");
		if(twistGeometricTipProperty != null)
			twistGeometricTip = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//outer_section/geometric_twist");
		
		// create the wing panel via its builder
		LiftingSurfacePanelCreator panel =
			new LiftingSurfacePanelBuilder(
				id, isLinked,
				chordRoot, chordTip,
				airfoilRoot, airfoilTip,
				twistGeometricRoot,
				twistGeometricTip,
				span, sweepLeadingEdge, dihedral
				)
			.setAirfoilRootPath(airfoilRootPath)
			.setAirfoilTipPath(airFoilPath2)
			.build();

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
			.append("\tID: '" + _id + "'\n")
			.append("\tSpan = " + _span.to(SI.METER) + "\n")
			.append("\tLambda_LE = " + _sweepLeadingEdge.to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tLambda_c/4 = " + _sweepQuarterChord.to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tLambda_c/2 = " + _sweepHalfChord.to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tLambda_TE = " + _sweepTrailingEdge.to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tLambda_TE = " + _sweepTrailingEdge.to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\t.....................................\n")
			.append("\t                           panel root\n")
			.append("\tc_r = " + _chordRoot.to(SI.METER) + "\n")
			.append("\tepsilon_r = " + _twistGeometricRoot.to(NonSI.DEGREE_ANGLE) + "\n")
			.append(_airfoilRoot + "\n")
			.append("\t.....................................\n")
			.append("\t                            panel tip\n")
			.append("\tc_t = " + _chordTip.to(SI.METER) + "\n")
			.append("\tepsilon_t = " + _twistGeometricTip.to(NonSI.DEGREE_ANGLE) + "\n")
			.append(_airfoilTip + "\n")
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

	public boolean isLinked() {
		return _isLinked;
	}

	public void setLinked(boolean isLinked) {
		this._isLinked = isLinked;
	}

	public Amount<Angle> getTwistGeometricRoot() {
		return _twistGeometricRoot;
	}

	public void setTwistGeometricRoot(Amount<Angle> _twistGeometricRoot) {
		this._twistGeometricRoot = _twistGeometricRoot;
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
