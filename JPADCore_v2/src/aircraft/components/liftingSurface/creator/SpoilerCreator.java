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

public class SpoilerCreator {

	//-----------------------------------------------------------------
	// VARIABLE DECLARATION
	private ISpoilerCreator _theSpoilerInterface;

	//-----------------------------------------------------------------
	// BUILDER
	public SpoilerCreator(ISpoilerCreator theSpoilerInterface) {
		this._theSpoilerInterface = theSpoilerInterface;
	}

	//-----------------------------------------------------------------
	// METHODS
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
		double innerStationSpanwisePosition = Double
				.valueOf(innerStationSpanwisePositionProperty);
		
		String outerStationSpanwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//spoiler/outer_station_spanwise_position/text()");
		double outerStationSpanwisePosition = Double
				.valueOf(outerStationSpanwisePositionProperty);
		
		String innerStationChordwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//spoiler/inner_station_chordwise_position/text()");
		double innerStationChordwisePosition = Double
				.valueOf(innerStationChordwisePositionProperty);
		
		String outerStationChordwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//spoiler/outer_station_chordwise_position/text()");
		double outerStationChordwisePosition = Double
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
		SpoilerCreator spoiler = new SpoilerCreator(
				new ISpoilerCreator.Builder()
				.setId(id)
				.setInnerStationSpanwisePosition(innerStationSpanwisePosition)
				.setOuterStationSpanwisePosition(outerStationSpanwisePosition)
				.setInnerStationChordwisePosition(innerStationChordwisePosition)
				.setOuterStationChordwisePosition(outerStationChordwisePosition)
				.setMinimumDeflection(minimumDeflection)
				.setMaximumDeflection(maximumDeflection)
				.build()
				);

		return spoiler;
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
			.append("\t-------------------------------------\n")
			.append("\tSpoiler\n")
			.append("\t-------------------------------------\n")
			.append("\tID: '" + _theSpoilerInterface.getId() + "'\n")
			.append("\tInner station spanwise position = " + _theSpoilerInterface.getInnerStationSpanwisePosition() + "\n")
			.append("\tOuter station spanwise position = " + _theSpoilerInterface.getOuterStationSpanwisePosition() + "\n")
			.append("\tInner station spanwise position = " + _theSpoilerInterface.getInnerStationChordwisePosition() + "\n")
			.append("\tOuter station spanwise position = " + _theSpoilerInterface.getOuterStationChordwisePosition() + "\n")
			.append("\tMinimum deflection = " + _theSpoilerInterface.getMinimumDeflection().doubleValue(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tMaximum deflection = " + _theSpoilerInterface.getMaximumDeflection().doubleValue(NonSI.DEGREE_ANGLE) + "\n")
			.append("\t.....................................\n")
			;
		return sb.toString();
		
	}

	//-----------------------------------------------------------------
	// GETTERS & SETTERS
	public ISpoilerCreator getTheSpoilerInterface() {
		return _theSpoilerInterface;
	}
	
	public void setTheSpoilerInterface (ISpoilerCreator theSpoilerInterface) {
		this._theSpoilerInterface = theSpoilerInterface;
	}
	
	public String getId() {
		return _theSpoilerInterface.getId();
	};
	
	public void setId (String id) {
		setTheSpoilerInterface(ISpoilerCreator.Builder.from(_theSpoilerInterface).setId(id).build());
	}
	
	public double getInnerStationSpanwisePosition() {
		return _theSpoilerInterface.getInnerStationSpanwisePosition();
	}

	public void setInnerStationSpanwisePosition(double etaIn) {
		setTheSpoilerInterface(ISpoilerCreator.Builder.from(_theSpoilerInterface).setInnerStationSpanwisePosition(etaIn).build());
	}
	
	public double getOuterStationSpanwisePosition() {
		return _theSpoilerInterface.getOuterStationSpanwisePosition();
	}

	public void setOuterStationSpanwisePosition(double etaOut) {
		setTheSpoilerInterface(ISpoilerCreator.Builder.from(_theSpoilerInterface).setOuterStationSpanwisePosition(etaOut).build());
	}

	public double getInnerStationChordwisePosition() {
		return _theSpoilerInterface.getInnerStationChordwisePosition();
	}

	public void setInnerStationChordwisePosition(double xIn) {
		setTheSpoilerInterface(ISpoilerCreator.Builder.from(_theSpoilerInterface).setInnerStationChordwisePosition(xIn).build());
	}
	
	public double getOuterStationChordwisePosition() {
		return _theSpoilerInterface.getOuterStationChordwisePosition();
	}

	public void setOuterStationChordwisePosition(double xOut) {
		setTheSpoilerInterface(ISpoilerCreator.Builder.from(_theSpoilerInterface).setOuterStationChordwisePosition(xOut).build());
	}

	public Amount<Angle> getMinimumDeflection() {
		return _theSpoilerInterface.getMinimumDeflection();
	}

	public void setMinimumDeflection(Amount<Angle> deltaSpoilerMin) {
		setTheSpoilerInterface(ISpoilerCreator.Builder.from(_theSpoilerInterface).setMinimumDeflection(deltaSpoilerMin).build());
	}

	public Amount<Angle> getMaximumDeflection() {
		return _theSpoilerInterface.getMaximumDeflection();
	}

	public void setMaximumDeflection(Amount<Angle> deltaSpoilerMax) {
		setTheSpoilerInterface(ISpoilerCreator.Builder.from(_theSpoilerInterface).setMaximumDeflection(deltaSpoilerMax).build());
	}
	
}
