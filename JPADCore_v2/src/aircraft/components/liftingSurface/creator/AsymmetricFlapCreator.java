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

public class AsymmetricFlapCreator {

	//-----------------------------------------------------------------
	// VARIABLE DECLARATION
	private IAsymmetricFlapCreator _theAsymmetricFlapInterface;
	private double _meanChordRatio;

	//-----------------------------------------------------------------
	// BUILDER
	public AsymmetricFlapCreator(IAsymmetricFlapCreator theAsymmetricFlapInterface) {
		this._theAsymmetricFlapInterface = theAsymmetricFlapInterface;
		calculateMeanChordRatio(
				this._theAsymmetricFlapInterface.getInnerChordRatio(), 
				this._theAsymmetricFlapInterface.getOuterChordRatio()
				);
	}

	//-----------------------------------------------------------------
	// METHODS
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
		double innerStationSpanwisePosition = Double
				.valueOf(innerStationSpanwisePositionProperty);
		
		String outerStationSpanwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//asymmetric_flap/outer_station_spanwise_position/text()");
		double outerStationSpanwisePosition = Double
				.valueOf(outerStationSpanwisePositionProperty);
		
		String innerChordRatioProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//asymmetric_flap/inner_chord_ratio/text()");
		double innerChordRatio = Double
				.valueOf(innerChordRatioProperty);
		
		String outerChordRatioProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//asymmetric_flap/outer_chord_ratio/text()");
		double outerChordRatio = Double
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
		AsymmetricFlapCreator asymmetricFlap = new AsymmetricFlapCreator(
				new IAsymmetricFlapCreator.Builder()
				.setId(id)
				.setType(type)
				.setInnerStationSpanwisePosition(innerStationSpanwisePosition)
				.setOuterStationSpanwisePosition(outerStationSpanwisePosition)
				.setInnerChordRatio(innerChordRatio)
				.setOuterChordRatio(outerChordRatio)
				.setMinimumDeflection(minimumDeflection)
				.setMaximumDeflection(maximumDeflection)
				.build()
				);

		return asymmetricFlap;
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
			.append("\t-------------------------------------\n")
			.append("\tAsymmetric flap\n")
			.append("\t-------------------------------------\n")
			.append("\tID: '" + _theAsymmetricFlapInterface.getId() + "'\n")
			.append("\tType = " + _theAsymmetricFlapInterface.getType() + "\n")
			.append("\tInner station spanwise position = " + _theAsymmetricFlapInterface.getInnerStationSpanwisePosition() + "\n")
			.append("\tOuter station spanwise position = " + _theAsymmetricFlapInterface.getOuterStationSpanwisePosition() + "\n")
			.append("\tInner chord ratio = " + _theAsymmetricFlapInterface.getInnerChordRatio() + "\n")
			.append("\tOuter chord ratio = " + _theAsymmetricFlapInterface.getOuterChordRatio() + "\n")
			.append("\tMean chord ratio = " + _meanChordRatio + "\n")
			.append("\tMinimum deflection = " + _theAsymmetricFlapInterface.getMinimumDeflection().doubleValue(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tMaximum deflection = " + _theAsymmetricFlapInterface.getMaximumDeflection().doubleValue(NonSI.DEGREE_ANGLE) + "\n")
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
	public IAsymmetricFlapCreator getTheAsymmetricFlapInterface() {
		return _theAsymmetricFlapInterface;
	}

	public void setTheAsymmetricFlapInterface(IAsymmetricFlapCreator theAsymmetricFlapInterface) {
		this._theAsymmetricFlapInterface = theAsymmetricFlapInterface;
	}

	public String getId() {
		return _theAsymmetricFlapInterface.getId();
	};
	
	public void setId (String id) {
		setTheAsymmetricFlapInterface(IAsymmetricFlapCreator.Builder.from(_theAsymmetricFlapInterface).setId(id).build());
	}
	
	public FlapTypeEnum getType() {
		return _theAsymmetricFlapInterface.getType();
	}
	
	public double getInnerStationSpanwisePosition() {
		return _theAsymmetricFlapInterface.getInnerStationSpanwisePosition();
	}
	
	public void setInnerStationSpanwisePosition (double _innerStationSpanwisePosition) {
		setTheAsymmetricFlapInterface(
				IAsymmetricFlapCreator.Builder.from(_theAsymmetricFlapInterface).setInnerStationSpanwisePosition(_innerStationSpanwisePosition)
				.build()
				);
	}
	
	public double getOuterStationSpanwisePosition() {
		return _theAsymmetricFlapInterface.getOuterStationSpanwisePosition();
	}
	
	public void setOuterStationSpanwisePosition (double _outerStationSpanwisePosition) {
		setTheAsymmetricFlapInterface(
				IAsymmetricFlapCreator.Builder.from(_theAsymmetricFlapInterface).setOuterStationSpanwisePosition(_outerStationSpanwisePosition)
				.build()
				);
	}
	
	public double getInnerChordRatio() {
		return _theAsymmetricFlapInterface.getInnerChordRatio();
	}
	
	public void setInnerChordRatio (double innerChordRatio) {
		setTheAsymmetricFlapInterface(
				IAsymmetricFlapCreator.Builder.from(_theAsymmetricFlapInterface).setInnerChordRatio(innerChordRatio)
				.build()
				);
	}
	
	public double getOuterChordRatio() {
		return _theAsymmetricFlapInterface.getOuterChordRatio();
	}
	
	public void setOuterChordRatio (double outerChordRatio) {
		setTheAsymmetricFlapInterface(
				IAsymmetricFlapCreator.Builder.from(_theAsymmetricFlapInterface).setOuterChordRatio(outerChordRatio)
				.build()
				);
	}
	
	public Amount<Angle> getMinimumDeflection() {
		return _theAsymmetricFlapInterface.getMinimumDeflection();
	}
	
	public void setMinimumDeflection (Amount<Angle> minimumDeflection) {
		setTheAsymmetricFlapInterface(
				IAsymmetricFlapCreator.Builder.from(_theAsymmetricFlapInterface).setMinimumDeflection(minimumDeflection)
				.build()
				);
	}
	
	public Amount<Angle> getMaximumDeflection() {
		return _theAsymmetricFlapInterface.getMaximumDeflection();
	}
	
	public void setMaximumDeflection (Amount<Angle> maximumDeflection) {
		setTheAsymmetricFlapInterface(
				IAsymmetricFlapCreator.Builder.from(_theAsymmetricFlapInterface).setMaximumDeflection(maximumDeflection)
				.build()
				);
	}
	
	public double getMeanChordRatio() {
		return _meanChordRatio;
	}

	public void setMeanChordRatio(double cfcMean) {
		_meanChordRatio = cfcMean;
	}
}

