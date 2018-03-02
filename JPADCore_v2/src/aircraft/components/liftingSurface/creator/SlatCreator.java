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

public class SlatCreator {

	//-----------------------------------------------------------------
	// VARIABLE DECLARATION
	private ISlatCreator _theSlatInterface;
	private double _meanChordRatio;

	//-----------------------------------------------------------------
	// BUILDER
	public SlatCreator(ISlatCreator theSlatInterface) {
		this._theSlatInterface = theSlatInterface;
		calculateMeanChordRatio(
				_theSlatInterface.getInnerChordRatio(),
				_theSlatInterface.getOuterChordRatio()
				);
	}

	//-----------------------------------------------------------------
	// METHODS
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
		
		Amount<Angle> minimumDeflection = MyXMLReaderUtils
				.getXMLAmountAngleByPath(
						doc, xpath,
						"//slat/min_deflection");
		
		Amount<Angle> maximumDeflection = MyXMLReaderUtils
				.getXMLAmountAngleByPath(
						doc, xpath,
						"//slat/max_deflection");
		
		String innerChordRatioProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//slat/inner_chord_ratio/text()");
		double innerChordRatio = Double
				.valueOf(innerChordRatioProperty);
		
		String outerChordRatioProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//slat/outer_chord_ratio/text()");
		double outerChordRatio = Double
				.valueOf(outerChordRatioProperty);

		String extensionRatioProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//slat/extension_ratio/text()");
		double extensionRatio = Double
				.valueOf(extensionRatioProperty);
		
		String innerStationSpanwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//slat/inner_station_spanwise_position/text()");
		double innerStationSpanwisePosition = Double
				.valueOf(innerStationSpanwisePositionProperty);
		
		String outerStationSpanwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//slat/outer_station_spanwise_position/text()");
		double outerStationSpanwisePosition = Double
				.valueOf(outerStationSpanwisePositionProperty);
		
		// create the wing panel via its builder
		SlatCreator slat =	new SlatCreator(
				new ISlatCreator.Builder()
				.setId(id)
				.setInnerStationSpanwisePosition(innerStationSpanwisePosition)
				.setOuterStationSpanwisePosition(outerStationSpanwisePosition)
				.setInnerChordRatio(innerChordRatio)
				.setOuterChordRatio(outerChordRatio)
				.setExtensionRatio(extensionRatio)
				.setMinimumDeflection(minimumDeflection)
				.setMaximumDeflection(maximumDeflection)
				.build()
				);

		return slat;
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
			.append("\t-------------------------------------\n")
			.append("\tSlat\n")
			.append("\t-------------------------------------\n")
			.append("\tID: '" + _theSlatInterface.getId() + "'\n")
			.append("\tMinimum deflection = " + _theSlatInterface.getMinimumDeflection().doubleValue(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tMaximum deflection = " + _theSlatInterface.getMaximumDeflection().doubleValue(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tInner chord ratio = " + _theSlatInterface.getInnerChordRatio() + "\n")
			.append("\tOuter chord ratio = " + _theSlatInterface.getOuterChordRatio() + "\n")
			.append("\tMean chord ratio = " + _meanChordRatio + "\n")
			.append("\tChord extension ratio = " + _theSlatInterface.getExtensionRatio() + "\n")
			.append("\tInner station spanwise position = " + _theSlatInterface.getInnerStationSpanwisePosition() + "\n")
			.append("\tOuter station spanwise position = " + _theSlatInterface.getOuterStationSpanwisePosition() + "\n")
			.append("\t.....................................\n")
			;
		return sb.toString();
		
	}

	public void calculateMeanChordRatio(double cfcIn, double cfcOut) {
		// TODO : WHEN AVAILABLE, IMPLEMENT A METHOD TO EVALUATES EACH cf/c CONTRIBUTION.
		setMeanChordRatio((cfcIn + cfcOut)/2);
	}

	//-----------------------------------------------------------------
	// GETTERS & SETTERS
	
	public ISlatCreator getTheSlatInterface() {
		return _theSlatInterface;
	}
	
	public void setTheSlatInterface (ISlatCreator theSlatInterface) {
		this._theSlatInterface = theSlatInterface;
	}
	
	public double getInnerStationSpanwisePosition() {
		return _theSlatInterface.getInnerStationSpanwisePosition();
	}

	public void setInnerStationSpanwisePosition(double etaIn) {
		setTheSlatInterface(ISlatCreator.Builder.from(_theSlatInterface).setInnerStationSpanwisePosition(etaIn).build());
	}
	
	public double getOuterStationSpanwisePosition() {
		return _theSlatInterface.getOuterStationSpanwisePosition();
	}
	
	public void setOuterStationSpanwisePosition(double etaOut) {
		setTheSlatInterface(ISlatCreator.Builder.from(_theSlatInterface).setOuterStationSpanwisePosition(etaOut).build());
	}

	public double getInnerChordRatio() {
		return _theSlatInterface.getInnerChordRatio();
	}

	public void setInnerChordRatio(double cfcIn) {
		setTheSlatInterface(ISlatCreator.Builder.from(_theSlatInterface).setInnerChordRatio(cfcIn).build());
	}
	
	public double getOuterChordRatio() {
		return _theSlatInterface.getOuterChordRatio();
	}

	public void setOuterChordRatio(double cfcOut) {
		setTheSlatInterface(ISlatCreator.Builder.from(_theSlatInterface).setOuterChordRatio(cfcOut).build());
	}
	
	public double getMeanChordRatio() {
		return _meanChordRatio;
	}

	public void setMeanChordRatio(double cfcMean) {
		_meanChordRatio = cfcMean;
	}
	
	public Amount<Angle> getMinimumDeflection() {
		return _theSlatInterface.getMinimumDeflection();
	}

	public void setMinimumDeflection(Amount<Angle> deltaSlatMin) {
		setTheSlatInterface(ISlatCreator.Builder.from(_theSlatInterface).setMinimumDeflection(deltaSlatMin).build());
	}
	
	public Amount<Angle> getMaximumDeflection() {
		return _theSlatInterface.getMaximumDeflection();
	}

	public void setMaximumDeflection(Amount<Angle> deltaSlatMax) {
		setTheSlatInterface(ISlatCreator.Builder.from(_theSlatInterface).setMaximumDeflection(deltaSlatMax).build());
	}
	
	public double getExtensionRatio() {
		return _theSlatInterface.getExtensionRatio();
	}

	public void setExtensionRatio(double extensionRatio) {
		setTheSlatInterface(ISlatCreator.Builder.from(_theSlatInterface).setExtensionRatio(extensionRatio).build());
	}

}
