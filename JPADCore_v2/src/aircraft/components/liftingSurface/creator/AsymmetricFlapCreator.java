package aircraft.components.liftingSurface.creator;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import configuration.MyConfiguration;
import configuration.enumerations.FlapTypeEnum;
import standaloneutils.MyXMLReaderUtils;

public class AsymmetricFlapCreator implements IAsymmetricFlapCreator {

	String _id;
	
	private FlapTypeEnum _type;
	private Double _innerStationSpanwisePosition,
				   _outerStationSpanwisePosition,
				   _innerChordRatio,
				   _outerChordRatio,
				   _meanChordRatio;
	private Amount<Angle> _minimumDeflection;
	private Amount<Angle> _maximumDeflection;

	//=================================================================
	// Builder pattern via a nested public static class
	
	public static class AsymmetricFlapBuilder {
		// required parameters
		private String __id;
		private FlapTypeEnum __type;
		private Double __innerStationSpanwisePosition;
		private Double __outerStationSpanwisePosition;
		private Double __innerChordRatio;
		private Double __outerChordRatio;
		private Amount<Angle> __minimumDeflection;
		private Amount<Angle> __maximumDeflection;

		// optional parameters ... defaults
		// ...

		public AsymmetricFlapBuilder(
				String id,
				FlapTypeEnum type,
				Double innerStationSpanwisePosition,
				Double outerStationSpanwisePosition,
				Double innerChordRatio,
				Double outerChordRatio,
				Amount<Angle> minimumDeflection,
				Amount<Angle> maximumDeflection
				){
			this.__id = id;
			this.__type = type;
			this.__innerStationSpanwisePosition = innerStationSpanwisePosition;
			this.__outerStationSpanwisePosition = outerStationSpanwisePosition;
			this.__innerChordRatio = innerChordRatio;
			this.__outerChordRatio = outerChordRatio;
			this.__minimumDeflection = minimumDeflection;
			this.__maximumDeflection = maximumDeflection;
		}

		public AsymmetricFlapCreator build() {
			return new AsymmetricFlapCreator(this);
		}
	}
	//=================================================================
	
	private AsymmetricFlapCreator(AsymmetricFlapBuilder builder) {
		_id = builder.__id;
		_type = builder.__type;
		_innerStationSpanwisePosition = builder.__innerStationSpanwisePosition;
		_outerStationSpanwisePosition = builder.__outerStationSpanwisePosition;
		_innerChordRatio = builder.__innerChordRatio;
		_outerChordRatio = builder.__outerChordRatio;
		_minimumDeflection = builder.__minimumDeflection;
		_maximumDeflection = builder.__maximumDeflection;
		
		calculateMeanChordRatio(_innerChordRatio, _outerChordRatio);
	}

	public static AsymmetricFlapCreator importFromAsymmetricFlapNode(Node nodeAsymmetricFlap) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(nodeAsymmetricFlap, true);
			doc.appendChild(importedNode);
			return AsymmetricFlapCreator.importFromAsymmetricFlapNodeImpl(doc);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static AsymmetricFlapCreator importFromAsymmetricFlapNodeImpl(Document doc) {

		System.out.println("Reading asymmetric flap data from XML doc ...");

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//asymmetric_flap/@id");
		
		String flapTypeProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//asymmetric_flap/@type");
		
		FlapTypeEnum type = null;
	    if(flapTypeProperty.equalsIgnoreCase("PLAIN"))
			type = FlapTypeEnum.PLAIN;
		else
			System.err.println("INVALID ASYMMETRIC FLAP TYPE !!");
		
		String innerStationSpanwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//asymmetric_flap/inner_station_spanwise_position/text()");
		Double innerStationSpanwisePosition = Double
				.valueOf(innerStationSpanwisePositionProperty);
		
		String outerStationSpanwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//asymmetric_flap/outer_station_spanwise_position/text()");
		Double outerStationSpanwisePosition = Double
				.valueOf(outerStationSpanwisePositionProperty);
		
		String innerChordRatioProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//asymmetric_flap/inner_chord_ratio/text()");
		Double innerChordRatio = Double
				.valueOf(innerChordRatioProperty);
		
		String outerChordRatioProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//asymmetric_flap/outer_chord_ratio/text()");
		Double outerChordRatio = Double
				.valueOf(outerChordRatioProperty);
		
		Amount<Angle> minimumDeflection = MyXMLReaderUtils
				.getXMLAmountAngleByPath(
						doc, xpath,
						"//asymmetric_flap/min_deflection");
		
		Amount<Angle> maximumDeflection = MyXMLReaderUtils
				.getXMLAmountAngleByPath(
						doc, xpath,
						"//asymmetric_flap/max_deflection");
		
		// create the wing panel via its builder
		AsymmetricFlapCreator asymmetricFlap =
				new AsymmetricFlapBuilder(
						id,
						type,
						innerStationSpanwisePosition,
						outerStationSpanwisePosition,
						innerChordRatio,
						outerChordRatio,
						minimumDeflection,
						maximumDeflection
						)
				.build();

		return asymmetricFlap;
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
			.append("\t-------------------------------------\n")
			.append("\tAsymmetric flap\n")
			.append("\t-------------------------------------\n")
			.append("\tID: '" + _id + "'\n")
			.append("\tType = " + _type + "\n")
			.append("\tInner station spanwise position = " + _innerStationSpanwisePosition + "\n")
			.append("\tOuter station spanwise position = " + _outerStationSpanwisePosition + "\n")
			.append("\tInner chord ratio = " + _innerChordRatio + "\n")
			.append("\tOuter chord ratio = " + _outerChordRatio + "\n")
			.append("\tMean chord ratio = " + _meanChordRatio + "\n")
			.append("\tMinimum deflection = " + _minimumDeflection.doubleValue(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tMaximum deflection = " + _maximumDeflection.doubleValue(NonSI.DEGREE_ANGLE) + "\n")
			.append("\t.....................................\n")
			;
		return sb.toString();
		
	}

	@Override
	public void calculateMeanChordRatio(Double cfcIn, Double cfcOut) {
		// TODO : WHEN AVAILABLE, IMPLEMENT A METHOD TO EVALUATES EACH cf/c CONTRIBUTION.
		setMeanChordRatio((cfcIn + cfcOut)/2);
	}
	
	@Override
	public Double getInnerStationSpanwisePosition() {
		return _innerStationSpanwisePosition;
	}

	@Override
	public void setInnerStationSpanwisePosition(Double etaIn) {
		_innerStationSpanwisePosition = etaIn;
	}
	
	@Override
	public Double getOuterStationSpanwisePosition() {
		return _outerStationSpanwisePosition;
	}
	
	@Override
	public void setOuterStationSpanwisePosition(Double etaOut) {
		_outerStationSpanwisePosition = etaOut;
	}

	@Override
	public Double getInnerChordRatio() {
		return _innerChordRatio;
	}

	@Override
	public void setInnerChordRatio(Double cfcIn) {
		_innerChordRatio = cfcIn;
	}
	
	@Override
	public Double getOuterChordRatio() {
		return _outerChordRatio;
	}

	@Override
	public void setOuterChordRatio(Double cfcOut) {
		_outerChordRatio = cfcOut;
	}
	
	@Override
	public Double getMeanChordRatio() {
		return _meanChordRatio;
	}

	@Override
	public void setMeanChordRatio(Double cfcMean) {
		_meanChordRatio = cfcMean;
	}
	
	@Override
	public Amount<Angle> getMinimumDeflection() {
		return _minimumDeflection;
	}

	@Override
	public void setMinimumDeflection(Amount<Angle> deltaFlapMin) {
		_minimumDeflection = deltaFlapMin;
	}
	
	@Override
	public Amount<Angle> getMaximumDeflection() {
		return _maximumDeflection;
	}

	@Override
	public void setMaximumDeflection(Amount<Angle> deltaFlapMax) {
		_maximumDeflection = deltaFlapMax;
	}
	
	@Override
	public FlapTypeEnum getType() {
		return _type;
	}
	
	@Override
	public void setType(FlapTypeEnum flapType) {
		_type = flapType;
	}
}

