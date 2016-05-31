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
import standaloneutils.MyXMLReaderUtils;

public class SlatCreator implements ISlatCreator {
	
	String _id;
	
	private Double _innerStationSpanwisePosition,
				   _outerStationSpanwisePosition,
				   _innerChordRatio,
				   _outerChordRatio,
				   _meanChordRatio,
				   _extensionRatio;
	private Amount<Angle> _deflection;

	//=================================================================
	// Builder pattern via a nested public static class
	
	public static class SlatBuilder {
		// required parameters
		private String __id;
		private Double __innerStationSpanwisePosition;
		private Double __outerStationSpanwisePosition;
		private Double __innerChordRatio;
		private Double __outerChordRatio;
		private Double __extensionRatio;
		private Amount<Angle> __deflection;

		// optional parameters ... defaults
		// ...

		public SlatBuilder(
				String id,
				Double innerStationSpanwisePosition,
				Double outerStationSpanwisePosition,
				Double innerChordRatio,
				Double outerChordRatio,
				Double extensionRatio,
				Amount<Angle> deflection
				){
			this.__id = id;
			this.__innerStationSpanwisePosition = innerStationSpanwisePosition;
			this.__outerStationSpanwisePosition = outerStationSpanwisePosition;
			this.__innerChordRatio = innerChordRatio;
			this.__outerChordRatio = outerChordRatio;
			this.__extensionRatio = extensionRatio;
			this.__deflection = deflection;
		}

		public SlatCreator build() {
			return new SlatCreator(this);
		}
	}
	//=================================================================
	
	private SlatCreator(SlatBuilder builder) {
		_id = builder.__id;
		_innerStationSpanwisePosition = builder.__innerStationSpanwisePosition;
		_outerStationSpanwisePosition = builder.__outerStationSpanwisePosition;
		_innerChordRatio = builder.__innerChordRatio;
		_outerChordRatio = builder.__outerChordRatio;
		_extensionRatio = builder.__extensionRatio;
		_deflection = builder.__deflection;
		
		calculateMeanChordRatio(_innerChordRatio, _outerChordRatio);
	}

	public static SlatCreator importFromSymmetricSlatNode(Node nodeSymmetricFlap) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(nodeSymmetricFlap, true);
			doc.appendChild(importedNode);
			return SlatCreator.importFromSlatNodeImpl(doc);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static SlatCreator importFromSlatNodeImpl(Document doc) {

		System.out.println("Reading slat data from XML doc ...");

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//slat/@id");
		
		Amount<Angle> deflection = MyXMLReaderUtils
				.getXMLAmountAngleByPath(
						doc, xpath,
						"//slat/deflection");
		
		String innerChordRatioProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//slat/inner_chord_ratio/text()");
		Double innerChordRatio = Double
				.valueOf(innerChordRatioProperty);
		
		String outerChordRatioProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//slat/outer_chord_ratio/text()");
		Double outerChordRatio = Double
				.valueOf(outerChordRatioProperty);

		String extensionRatioProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//slat/extension_ratio/text()");
		Double extensionRatio = Double
				.valueOf(extensionRatioProperty);
		
		String innerStationSpanwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//slat/inner_station_spanwise_position/text()");
		Double innerStationSpanwisePosition = Double
				.valueOf(innerStationSpanwisePositionProperty);
		
		String outerStationSpanwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//slat/outer_station_spanwise_position/text()");
		Double outerStationSpanwisePosition = Double
				.valueOf(outerStationSpanwisePositionProperty);
		
		// create the wing panel via its builder
		SlatCreator slat =
				new SlatBuilder(
						id,
						innerStationSpanwisePosition,
						outerStationSpanwisePosition,
						innerChordRatio,
						outerChordRatio,
						extensionRatio,
						deflection
						)
				.build();

		return slat;
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
			.append("\t-------------------------------------\n")
			.append("\tSlat\n")
			.append("\t-------------------------------------\n")
			.append("\tID: '" + _id + "'\n")
			.append("\tDeflection = " + _deflection.to(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tInner chord ratio = " + _innerChordRatio + "\n")
			.append("\tOuter chord ratio = " + _outerChordRatio + "\n")
			.append("\tMean chord ratio = " + _meanChordRatio + "\n")
			.append("\tChord extension ratio = " + _extensionRatio + "\n")
			.append("\tInner station spanwise position = " + _innerStationSpanwisePosition + "\n")
			.append("\tOuter station spanwise position = " + _outerStationSpanwisePosition + "\n")
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
	public Amount<Angle> getDeflection() {
		return _deflection;
	}

	@Override
	public void setDeflection(Amount<Angle> deltaFlap) {
		_deflection = deltaFlap;
	}
	
	@Override
	public Double getExtensionRatio() {
		return _extensionRatio;
	}

	@Override
	public void setExtensionRatio(Double extensionRatio) {
		_extensionRatio = extensionRatio;
	}

}
