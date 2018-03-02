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
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FlapTypeEnum;
import standaloneutils.MyXMLReaderUtils;

public class SymmetricFlapCreator {

	//-----------------------------------------------------------------
	// VARIABLE DECLARATION
	private ISymmetricFlapCreator _theSymmetricFlapInterface;
	private double _meanChordRatio;
	
	//-----------------------------------------------------------------
	// BUILDER
	public SymmetricFlapCreator(ISymmetricFlapCreator theSymmetricFlapInterface) {
		this._theSymmetricFlapInterface = theSymmetricFlapInterface;
		calculateMeanChordRatio(
				_theSymmetricFlapInterface.getInnerChordRatio(), 
				_theSymmetricFlapInterface.getOuterChordRatio()
				);
	}

	//-----------------------------------------------------------------
	// METHODS
	public static SymmetricFlapCreator importFromSymmetricFlapNode(Node nodeSymmetricFlap, ComponentEnum type) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(nodeSymmetricFlap, true);
			doc.appendChild(importedNode);
			return SymmetricFlapCreator.importFromSymmetricFlapNodeImpl(doc, type);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static SymmetricFlapCreator importFromSymmetricFlapNodeImpl(Document doc, ComponentEnum type) {

		System.out.println("Reading symmetric flap data from XML doc ...");

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//symmetric_flap/@id");
		
		String flapTypeProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//symmetric_flap/@type");
		
		FlapTypeEnum flapType = null;
		
		if(type.equals(ComponentEnum.WING)) {
		
		if(flapTypeProperty.equalsIgnoreCase("SINGLE_SLOTTED"))
			flapType = FlapTypeEnum.SINGLE_SLOTTED;
		else if(flapTypeProperty.equalsIgnoreCase("double_SLOTTED"))
			flapType = FlapTypeEnum.DOUBLE_SLOTTED;
		else if(flapTypeProperty.equalsIgnoreCase("TRIPLE_SLOTTED"))
			flapType = FlapTypeEnum.TRIPLE_SLOTTED;
		else if(flapTypeProperty.equalsIgnoreCase("FOWLER"))
			flapType = FlapTypeEnum.FOWLER;
		else if(flapTypeProperty.equalsIgnoreCase("PLAIN"))
			flapType = FlapTypeEnum.PLAIN;
		else
			System.err.println("INVALID FLAP TYPE !!");
		
		}
		
		if(type.equals(ComponentEnum.HORIZONTAL_TAIL)) {

		    if(flapTypeProperty.equalsIgnoreCase("PLAIN"))
				flapType = FlapTypeEnum.PLAIN;
			else
				System.err.println("INVALID ELEVATOR TYPE !!");

		}

		if(type.equals(ComponentEnum.VERTICAL_TAIL)) {

			if(flapTypeProperty.equalsIgnoreCase("PLAIN"))
				flapType = FlapTypeEnum.PLAIN;
			else
				System.err.println("INVALID RUDDER TYPE !!");

		}
		
		if(type.equals(ComponentEnum.CANARD)) {

		    if(flapTypeProperty.equalsIgnoreCase("PLAIN"))
				flapType = FlapTypeEnum.PLAIN;
			else
				System.err.println("INVALID CONTROL SURFACE TYPE !!");

		}
		
		String innerStationSpanwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//symmetric_flap/inner_station_spanwise_position/text()");
		double innerStationSpanwisePosition = Double
				.valueOf(innerStationSpanwisePositionProperty);
		
		String outerStationSpanwisePositionProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//symmetric_flap/outer_station_spanwise_position/text()");
		double outerStationSpanwisePosition = Double
				.valueOf(outerStationSpanwisePositionProperty);
		
		String innerChordRatioProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//symmetric_flap/inner_chord_ratio/text()");
		double innerChordRatio = Double
				.valueOf(innerChordRatioProperty);
		
		String outerChordRatioProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//symmetric_flap/outer_chord_ratio/text()");
		double outerChordRatio = Double
				.valueOf(outerChordRatioProperty);
		
		Amount<Angle> minimumDeflection = MyXMLReaderUtils
				.getXMLAmountAngleByPath(
						doc, xpath,
						"//symmetric_flap/min_deflection");
		
		Amount<Angle> maximumDeflection = MyXMLReaderUtils
				.getXMLAmountAngleByPath(
						doc, xpath,
						"//symmetric_flap/max_deflection");
		
		// create the wing panel via its builder
		SymmetricFlapCreator symmetricFlap = new SymmetricFlapCreator(
				new ISymmetricFlapCreator.Builder()
				.setId(id)
				.setType(flapType)
				.setInnerStationSpanwisePosition(innerStationSpanwisePosition)
				.setOuterStationSpanwisePosition(outerStationSpanwisePosition)
				.setInnerChordRatio(innerChordRatio)
				.setOuterChordRatio(outerChordRatio)
				.setMinimumDeflection(minimumDeflection)
				.setMaximumDeflection(maximumDeflection)
				.build()
				);		

		return symmetricFlap;
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
			.append("\t-------------------------------------\n")
			.append("\tSymmetric flap\n")
			.append("\t-------------------------------------\n")
			.append("\tID: '" + _theSymmetricFlapInterface.getId() + "'\n")
			.append("\tFlap type = " + _theSymmetricFlapInterface.getType() + "\n")
			.append("\tInner station spanwise position = " + _theSymmetricFlapInterface.getInnerStationSpanwisePosition() + "\n")
			.append("\tOuter station spanwise position = " + _theSymmetricFlapInterface.getOuterStationSpanwisePosition() + "\n")
			.append("\tInner chord ratio = " + _theSymmetricFlapInterface.getInnerChordRatio() + "\n")
			.append("\tOuter chord ratio = " + _theSymmetricFlapInterface.getOuterChordRatio() + "\n")
			.append("\tMean chord ratio = " + _meanChordRatio + "\n")
			.append("\tMinimum deflection = " + _theSymmetricFlapInterface.getMinimumDeflection().doubleValue(NonSI.DEGREE_ANGLE) + "\n")
			.append("\tMaximum deflection = " + _theSymmetricFlapInterface.getMaximumDeflection().doubleValue(NonSI.DEGREE_ANGLE) + "\n")
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
	public ISymmetricFlapCreator getTheSymmetricFlapInterface() {
		return _theSymmetricFlapInterface;
	}
	
	public void setTheSymmetricFlapInterface (ISymmetricFlapCreator theSymmetricFlapInterface) {
		this._theSymmetricFlapInterface = theSymmetricFlapInterface;
	}
	
	public String getId() {
		return _theSymmetricFlapInterface.getId();
	}
	
	public void setId (String id) {
		setTheSymmetricFlapInterface(ISymmetricFlapCreator.Builder.from(_theSymmetricFlapInterface).setId(id).build());
	}
	
	public double getInnerStationSpanwisePosition() {
		return _theSymmetricFlapInterface.getInnerStationSpanwisePosition();
	}

	public void setInnerStationSpanwisePosition(double etaIn) {
		setTheSymmetricFlapInterface(ISymmetricFlapCreator.Builder.from(_theSymmetricFlapInterface).setInnerStationSpanwisePosition(etaIn).build());
	}
	
	public double getOuterStationSpanwisePosition() {
		return _theSymmetricFlapInterface.getOuterStationSpanwisePosition();
	}
	
	public void setOuterStationSpanwisePosition(double etaOut) {
		setTheSymmetricFlapInterface(ISymmetricFlapCreator.Builder.from(_theSymmetricFlapInterface).setOuterStationSpanwisePosition(etaOut).build());
	}

	public double getInnerChordRatio() {
		return _theSymmetricFlapInterface.getInnerChordRatio();
	}

	public void setInnerChordRatio(double cfcIn) {
		setTheSymmetricFlapInterface(ISymmetricFlapCreator.Builder.from(_theSymmetricFlapInterface).setInnerChordRatio(cfcIn).build());
	}
	
	public double getOuterChordRatio() {
		return _theSymmetricFlapInterface.getOuterChordRatio();
	}

	public void setOuterChordRatio(double cfcOut) {
		setTheSymmetricFlapInterface(ISymmetricFlapCreator.Builder.from(_theSymmetricFlapInterface).setOuterChordRatio(cfcOut).build());
	}
	
	public double getMeanChordRatio() {
		return _meanChordRatio;
	}

	public void setMeanChordRatio(double cfcMean) {
		_meanChordRatio = cfcMean;
	}
	
	public Amount<Angle> getMinimumDeflection() {
		return _theSymmetricFlapInterface.getMinimumDeflection();
	}

	public void setMinimumDeflection(Amount<Angle> deltaFlapMin) {
		setTheSymmetricFlapInterface(ISymmetricFlapCreator.Builder.from(_theSymmetricFlapInterface).setMinimumDeflection(deltaFlapMin).build());
	}

	public Amount<Angle> getMaximumDeflection() {
		return _theSymmetricFlapInterface.getMaximumDeflection();
	}

	public void setMaximumDeflection(Amount<Angle> deltaFlapMax) {
		setTheSymmetricFlapInterface(ISymmetricFlapCreator.Builder.from(_theSymmetricFlapInterface).setMaximumDeflection(deltaFlapMax).build());
	}
	
	public FlapTypeEnum getType() {
		return _theSymmetricFlapInterface.getType();
	}
	
	public void setType(FlapTypeEnum flapType) {
		setTheSymmetricFlapInterface(ISymmetricFlapCreator.Builder.from(_theSymmetricFlapInterface).setType(flapType).build());
	}

}
