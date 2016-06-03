package aircraft.components.fuselage.creator;

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

import aircraft.components.fuselage.creator.SpoilerCreator;
import configuration.MyConfiguration;
import standaloneutils.MyXMLReaderUtils;

public class SpoilerCreator implements ISpoilerCreator {

String _id;
	
	private Double _innerStationSpanwisePosition,
				   _outerStationSpanwisePosition,
				   _innerStationChordwisePosition,
				   _outerStationChordwisePosition;
	private Amount<Angle> _minimumDeflection;
	private Amount<Angle> _maximumDeflection;

	//=================================================================
	// Builder pattern via a nested public static class
	
	public static class SpoilerBuilder {
		// required parameters
		private String __id;
		private Double __innerStationSpanwisePosition;
		private Double __outerStationSpanwisePosition;
		private Double __innerStationChordwisePosition;
		private Double __outerStationChordwisePosition;
		private Amount<Angle> __minimumDeflection;
		private Amount<Angle> __maximumDeflection;

		// optional parameters ... defaults
		// ...

		public SpoilerBuilder(
				String id,
				Double innerStationSpanwisePosition,
				Double outerStationSpanwisePosition,
				Double innerStationChordwisePosition,
				Double outerStationChordwisePosition,
				Amount<Angle> minimumDeflection,
				Amount<Angle> maximumDeflection
				){
			this.__id = id;
			this.__innerStationSpanwisePosition = innerStationSpanwisePosition;
			this.__outerStationSpanwisePosition = outerStationSpanwisePosition;
			this.__innerStationChordwisePosition = innerStationChordwisePosition;
			this.__outerStationChordwisePosition = outerStationChordwisePosition;
			this.__minimumDeflection = minimumDeflection;
			this.__maximumDeflection = maximumDeflection;
		}

		public SpoilerCreator build() {
			return new SpoilerCreator(this);
		}
	}
	//=================================================================
	
	private SpoilerCreator(SpoilerBuilder builder) {
 		_id = builder.__id;
		_innerStationSpanwisePosition = builder.__innerStationSpanwisePosition;
		_outerStationSpanwisePosition = builder.__outerStationSpanwisePosition;
		_innerStationChordwisePosition = builder.__innerStationChordwisePosition;
		_outerStationChordwisePosition = builder.__outerStationChordwisePosition;
		_minimumDeflection = builder.__minimumDeflection;
		_maximumDeflection = builder.__maximumDeflection;
	}

	public static SpoilerCreator importFromSpoilerNode(Node nodeSpoiler) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(nodeSpoiler, true);
			doc.appendChild(importedNode);
			return SpoilerCreator.importFromSpoilerNodeImpl(doc);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static SpoilerCreator importFromSpoilerNodeImpl(Document doc) {

		System.out.println("Reading spoiler data from XML doc ...");

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//spoiler/@id");
		
		String innerStationSpanwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//spoiler/inner_station_spanwise_position/text()");
		Double innerStationSpanwisePosition = Double
				.valueOf(innerStationSpanwisePositionProperty);
		
		String outerStationSpanwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//spoiler/outer_station_spanwise_position/text()");
		Double outerStationSpanwisePosition = Double
				.valueOf(outerStationSpanwisePositionProperty);
		
		String innerStationChordwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//spoiler/inner_station_chordwise_position/text()");
		Double innerStationChordwisePosition = Double
				.valueOf(innerStationChordwisePositionProperty);
		
		String outerStationChordwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//spoiler/outer_station_chordwise_position/text()");
		Double outerStationChordwisePosition = Double
				.valueOf(outerStationChordwisePositionProperty);
		
		Amount<Angle> minimumDeflection = MyXMLReaderUtils
				.getXMLAmountAngleByPath(
						doc, xpath,
						"//spoiler/min_deflection");
		
		Amount<Angle> maximumDeflection = MyXMLReaderUtils
				.getXMLAmountAngleByPath(
						doc, xpath,
						"//spoiler/max_deflection");
		
		// create the spoiler via its builder
		SpoilerCreator spoiler =
				new SpoilerBuilder(
						id,
						innerStationSpanwisePosition,
						outerStationSpanwisePosition,
						innerStationChordwisePosition,
						outerStationChordwisePosition,
						minimumDeflection,
						maximumDeflection
						)
				.build();

		return spoiler;
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
			.append("\t-------------------------------------\n")
			.append("\tSpoiler\n")
			.append("\t-------------------------------------\n")
			.append("\tID: '" + _id + "'\n")
			.append("\tInner station spanwise position = " + _innerStationSpanwisePosition + "\n")
			.append("\tOuter station spanwise position = " + _outerStationSpanwisePosition + "\n")
			.append("\tInner station spanwise position = " + _innerStationChordwisePosition + "\n")
			.append("\tOuter station spanwise position = " + _outerStationChordwisePosition + "\n")
			.append("\tMinimum deflection = " + _minimumDeflection.doubleValue(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tMaximum deflection = " + _maximumDeflection.doubleValue(NonSI.DEGREE_ANGLE) + "\n")
			.append("\t.....................................\n")
			;
		return sb.toString();
		
	}

	@Override
	public Double getInnerStationSpanwisePosition() {
		return _innerStationSpanwisePosition;
	}

	@Override
	public Double getOuterStationSpanwisePosition() {
		return _outerStationSpanwisePosition;
	}

	@Override
	public void setInnerStationSpanwisePosition(Double etaIn) {
		this._innerStationSpanwisePosition = etaIn;
	}

	@Override
	public void setOuterStationSpanwisePosition(Double etaOut) {
		this._outerStationSpanwisePosition = etaOut;
	}

	@Override
	public Double getInnerStationChordwisePosition() {
		return _innerStationChordwisePosition;
	}

	@Override
	public Double getOuterStationChordwisePosition() {
		return _outerStationChordwisePosition;
	}

	@Override
	public void setInnerStationChordwisePosition(Double xIn) {
		this._innerStationChordwisePosition = xIn;
	}

	@Override
	public void setOuterStationChordwisePosition(Double xOut) {
		this._outerStationChordwisePosition = xOut;
	}

	@Override
	public Amount<Angle> getMinimumDeflection() {
		return _minimumDeflection;
	}

	@Override
	public void setMinimumDeflection(Amount<Angle> deltaSpoilerMin) {
		this._minimumDeflection = deltaSpoilerMin;
	}

	@Override
	public Amount<Angle> getMaximumDeflection() {
		return _maximumDeflection;
	}

	@Override
	public void setMaximumDeflection(Amount<Angle> deltaSpoilerMax) {
		this._maximumDeflection = deltaSpoilerMax;
	}
	
}
