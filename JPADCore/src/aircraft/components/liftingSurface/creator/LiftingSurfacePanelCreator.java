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

import configuration.MyConfiguration;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class LiftingSurfacePanelCreator implements ILiftingSurfacePanelCreator {

	String _id;

	private Amount<Length> _chordRoot;
	private Amount<Length> _chordTip;
	private AirfoilCreator _airfoilRoot;
	private AirfoilCreator _airfoilTip;
	private Amount<Angle> _twistGeometricTip;
	private Amount<Length> _semiSpan, _span;
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

	// commented out, use the builder pattern instead
//	public LiftingSurfacePanelCreator(
//			Amount<Length> chordRoot,
//			Amount<Length> chordTip,
//			Airfoil airfoilRoot,
//			Airfoil airfoilTip,
//			Amount<Angle> twistGeometricTip,
//			Amount<Length> semiSpan,
//			Amount<Angle> sweepLeadingEdge,
//			Amount<Angle> dihedral
//			) {
//		_chordRoot = chordRoot;
//		_chordTip = chordTip;
//		_airfoilRoot = airfoilRoot;
//		_airfoilTip = airfoilTip;
//		_twistGeometricTip = twistGeometricTip;
//		_semiSpan = semiSpan;
//		_span = _semiSpan.times(2.0);
//		_sweepLeadingEdge = sweepLeadingEdge;
//		_dihedral = dihedral;
//
//		calculateGeometry();
//
//	}

	@SuppressWarnings("unchecked")
	@Override
	public void calculateGeometry() {

		_taperRatio = _chordTip.divide(_chordRoot).getEstimatedValue();
		_surfacePlanform = (Amount<Area>) ( _chordRoot.plus(_chordTip) ).times(_semiSpan);
		_surfaceWetted = _surfacePlanform.times(2.0);
		_aspectRatio = _span.times(_span).divide(_surfacePlanform).getEstimatedValue();

		_sweepQuarterChord = calculateSweep(0.25);
		_sweepHalfChord = calculateSweep(0.50);
		_sweepTrailingEdge = calculateSweep(1.00);

		_meanAerodynamicChord =
			_chordRoot.times(2.0/3.0)
				.times(1.0 + _taperRatio + _taperRatio*_taperRatio)
				.divide(1.0 + _taperRatio)
			;
		_meanAerodynamicChordLeadingEdgeY =
				_span
					.divide(6)
					.times(1 + 2.0*_taperRatio)
					.divide(1.0 + _taperRatio);

		_meanAerodynamicChordLeadingEdgeX =
			_meanAerodynamicChordLeadingEdgeY
				.times(Math.tan(_sweepLeadingEdge.to(SI.RADIAN).getEstimatedValue()));

		_meanAerodynamicChordLeadingEdgeZ =
			_meanAerodynamicChordLeadingEdgeY
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
	public Amount<Length> getSpan() {
		return _span;
	}

	@Override
	public void setSpan(Amount<Length> b) {
		_span = b;
		_semiSpan = _span.times(0.5);
		calculateGeometry();
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
	public Amount<Length> getSemiSpan() {
		return _semiSpan;
	}

	@Override
	public void setSemiSpan(Amount<Length> s) {
		_semiSpan = s;
		_span = _semiSpan.times(2.0);
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

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}

	// Builder pattern via a nested public static class

	public static class LiftingSurfacePanelBuilder {
		// required parameters
		private String __id;
		private Amount<Length> __chordRoot;
		private Amount<Length> __chordTip;
		private AirfoilCreator __airfoilRoot;
		private AirfoilCreator __airfoilTip;
		private Amount<Angle> __twistGeometricTip;
		private Amount<Length> __semiSpan;
		private Amount<Angle> __sweepLeadingEdge;
		private Amount<Angle> __dihedral;

		// optional parameters ... defaults
		// ...

		public LiftingSurfacePanelBuilder(
				String id,
				Amount<Length> cR, Amount<Length> cT,
				AirfoilCreator airfR, AirfoilCreator airfT,
				Amount<Angle> twistGeometricT,
				Amount<Length> semiSpan,
				Amount<Angle> sweepLE,
				Amount<Angle> dih
				){
			this.__id = id;
			this.__chordRoot = cR;
			this.__chordTip = cT;
			this.__airfoilRoot = airfR;
			this.__airfoilTip = airfT;
			this.__twistGeometricTip = twistGeometricT;
			this.__semiSpan = semiSpan;
			this.__sweepLeadingEdge = sweepLE;
			this.__dihedral = dih;
		}

		public LiftingSurfacePanelCreator build() {
			return new LiftingSurfacePanelCreator(this);
		}

	}

	private LiftingSurfacePanelCreator(LiftingSurfacePanelBuilder builder) {
		_id = builder.__id;
		_chordRoot = builder.__chordRoot;
		_chordTip = builder.__chordTip;
		_airfoilRoot = builder.__airfoilRoot;
		_airfoilTip = builder.__airfoilTip;
		_twistGeometricTip = builder.__twistGeometricTip;
		_semiSpan = builder.__semiSpan;
		_span = _semiSpan.times(2.0);
		_sweepLeadingEdge = builder.__sweepLeadingEdge;
		_dihedral = builder.__dihedral;
		calculateGeometry();

	}

	public static LiftingSurfacePanelCreator importFromXML(String pathToXML, String airfoilsDir) {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading wing panel data ...");

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//panel/@id");

		Amount<Length> semiSpan = reader.getXMLAmountLengthByPath("//panel/semispan");

		Amount<Angle> dihedral = reader.getXMLAmountAngleByPath("//panel/dihedral");

		Amount<Angle> sweepLeadingEdge = reader.getXMLAmountAngleByPath("//panel/sweep_leading_edge");

		Amount<Length> chordRoot = reader.getXMLAmountLengthByPath("//panel/inner_section/chord");

		String airfoilFileName1 =
			MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//panel/inner_section/airfoil/@file");
		String airFoilPath1 = airfoilsDir + File.separator + airfoilFileName1;
		AirfoilCreator airfoilRoot = AirfoilCreator.importFromXML(airFoilPath1);

		Amount<Length> chordTip = reader.getXMLAmountLengthByPath("//panel/outer_section/chord");

		String airfoilFileName2 =
			MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//panel/outer_section/airfoil/@file");
		String airFoilPath2 = airfoilsDir + File.separator + airfoilFileName2;
		AirfoilCreator airfoilTip = AirfoilCreator.importFromXML(airFoilPath2);

		Amount<Angle> twistGeometricTip = reader.getXMLAmountAngleByPath("//panel/outer_section/geometric_twist");

		// create the wing panel via its builder
		LiftingSurfacePanelCreator panel =
			new LiftingSurfacePanelBuilder(
				id,
				chordRoot, chordTip,
				airfoilRoot, airfoilTip,
				twistGeometricTip,
				semiSpan, sweepLeadingEdge, dihedral
				)
			.build();

		return panel;
	}

	private static LiftingSurfacePanelCreator importFromPanelNodeImpl(Document doc, String airfoilsDir) {

		System.out.println("Reading lifting surface panel data from XML doc ...");

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//panel/@id");

		Amount<Length> semiSpan = MyXMLReaderUtils.getXMLAmountLengthByPath(doc, xpath, "//semispan");
		Amount<Angle> dihedral = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//dihedral");
		Amount<Angle> sweepLeadingEdge = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//sweep_leading_edge");
		Amount<Length> chordRoot = MyXMLReaderUtils.getXMLAmountLengthByPath(doc, xpath, "//inner_section/chord");

		String airfoilFileName1 =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//inner_section/airfoil/@file");
		String airFoilPath1 = airfoilsDir + File.separator + airfoilFileName1;
		AirfoilCreator airfoilRoot = AirfoilCreator.importFromXML(airFoilPath1);

		Amount<Length> chordTip = MyXMLReaderUtils.getXMLAmountLengthByPath(doc, xpath, "//outer_section/chord");

		String airfoilFileName2 =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//outer_section/airfoil/@file");
		String airFoilPath2 = airfoilsDir + File.separator + airfoilFileName2;
		AirfoilCreator airfoilTip = AirfoilCreator.importFromXML(airFoilPath2);

		Amount<Angle> twistGeometricTip = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//outer_section/geometric_twist");
		// create the wing panel via its builder
		LiftingSurfacePanelCreator panel =
			new LiftingSurfacePanelBuilder(
				id,
				chordRoot, chordTip,
				airfoilRoot, airfoilTip,
				twistGeometricTip,
				semiSpan, sweepLeadingEdge, dihedral
				)
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

		System.out.println("Reading LINKED lifting surface panel data from XML doc ...");

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//panel/@id");

		Amount<Length> semiSpan = MyXMLReaderUtils.getXMLAmountLengthByPath(doc, xpath, "//semispan");
		Amount<Angle> dihedral = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//dihedral");
		Amount<Angle> sweepLeadingEdge = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//sweep_leading_edge");

		Amount<Length> chordRoot = panel0.getChordTip(); // from linked panel

		AirfoilCreator airfoilRoot = panel0.getAirfoilTip(); // from linked panel

		Amount<Length> chordTip = MyXMLReaderUtils.getXMLAmountLengthByPath(doc, xpath, "//outer_section/chord");

		String airfoilFileName2 =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//outer_section/airfoil/@file");
		String airFoilPath2 = airfoilsDir + File.separator + airfoilFileName2;
		AirfoilCreator airfoilTip = AirfoilCreator.importFromXML(airFoilPath2);

		Amount<Angle> twistGeometricTip = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//outer_section/geometric_twist");
		// create the wing panel via its builder
		LiftingSurfacePanelCreator panel =
			new LiftingSurfacePanelBuilder(
				id,
				chordRoot, chordTip,
				airfoilRoot, airfoilTip,
				twistGeometricTip,
				semiSpan, sweepLeadingEdge, dihedral
				)
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
			.append("\tb = " + _span.to(SI.METER) + "\n")
			.append("\tb/2 = " + _semiSpan.to(SI.METER) + "\n")
			.append("\tLambda_LE = " + _sweepLeadingEdge.to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tLambda_c/4 = " + _sweepQuarterChord.to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tLambda_c/2 = " + _sweepHalfChord.to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tLambda_TE = " + _sweepTrailingEdge.to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\t.....................................\n")
			.append("\t                           panel root\n")
			.append("\tc_r = " + _chordRoot.to(SI.METER) + "\n")
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

}
