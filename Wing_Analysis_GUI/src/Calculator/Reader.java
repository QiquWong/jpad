package Calculator;

import java.io.IOException;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;

import GUI.Views.VariablesInputData;
import configuration.enumerations.AirfoilFamilyEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

public class Reader {
	
	
	public void readInputFromXML(VariablesInputData theVariables, String pathToXML) throws IOException{
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		Amount<Length> altitude = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//altitude");
		Unit unitOfMeasurement = altitude.getUnit();
		theVariables.getAltitude().setText(Double.toString(altitude.doubleValue(unitOfMeasurement)));
		theVariables.getAltitudeUnits().setValue(unitOfMeasurement.toString());
		
		double machNumber =  Double.parseDouble(reader.getXMLPropertiesByPath("//mach_number").get(0));
		theVariables.getMachNumber().setText(Double.toString(machNumber));
		
		Amount<Area> surface = (Amount<Area>) reader.getXMLAmountWithUnitByPath("//surface");
		Unit unitOfMeasurementSurface = surface.getUnit();
		theVariables.getSurface().setText(Double.toString( surface.doubleValue(unitOfMeasurementSurface)));
		theVariables.getSurfaceUnits().setValue(unitOfMeasurementSurface.toString());

		double aspectRatio =  Double.parseDouble(reader.getXMLPropertiesByPath("//aspect_ratio").get(0));
		theVariables.getAspectRatio().setText(Double.toString(aspectRatio));
		
		int numberOfPointsSemiSpan =  (int)Double.parseDouble(reader.getXMLPropertiesByPath("//number_of_point_semispan").get(0));
		theVariables.getNumberOfPoints().setText(Double.toString(numberOfPointsSemiSpan));
		
		double adimensionalKinkStation =  Double.parseDouble(reader.getXMLPropertiesByPath("//adimensional_kink_station").get(0));
		theVariables.getAdimensionalKinkStation().setText(Double.toString(adimensionalKinkStation));
		
		double thickenssMeanAirfoil =  Double.parseDouble(reader.getXMLPropertiesByPath("//max_thickness_mean_airfoil").get(0));
		theVariables.getMaxThickness().setText(Double.toString(thickenssMeanAirfoil));
		
		List<String> airfoilFamilyProperty = reader.getXMLPropertiesByPath("//airfoil_family");
			theVariables.getAirfoilFamily().setValue(airfoilFamilyProperty.get(0));
		
	
			int numberOfSection =  (int)Double.parseDouble(reader.getXMLPropertiesByPath("//number_of_given_sections").get(0));
			String intNumberOfSection = String.valueOf(numberOfSection);
			theVariables.getNumberOfGivenSections().setValue(intNumberOfSection );
			
		theVariables.setNumberOfGivenSection();	
		
		
		// DISTRIBUTIONS
		
		List<String> stationsDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//y_adimensional_stations").get(0));
		for(int i=0; i<stationsDistribution.size(); i++)
			theVariables.getStationList().get(i).setText((stationsDistribution.get(i)));
		
		Unit unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//chord_distribution" + "/@unit"));
		List<String> chordDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//chord_distribution").get(0));
		for(int i=0; i<chordDistribution.size(); i++)
			theVariables.getChordList().get(i).setText((chordDistribution.get(i)));
		theVariables.getChordsUnits().setValue(unitOfMeas);
		
		
		unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//x_le_distribution" + "/@unit"));
		List<String> xleDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//x_le_distribution").get(0));
		for(int i=0; i<xleDistribution.size(); i++)
			theVariables.getXleList().get(i).setText((xleDistribution.get(i)));
		theVariables.getXleUnits().setValue(unitOfMeas);
		
		unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//twist_distribution" + "/@unit"));
		List<String> twistDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//twist_distribution").get(0));
		for(int i=0; i<twistDistribution.size(); i++)
			theVariables.getTwistList().get(i).setText((twistDistribution.get(i)));
		theVariables.getTwistUnits().setValue(unitOfMeas);
		
		unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//alpha_zero_lift_distribution" + "/@unit"));
		List<String> alphaZeroLiftDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_zero_lift_distribution").get(0));
		for(int i=0; i<alphaZeroLiftDistribution.size(); i++)
			theVariables.getAlphaZeroList().get(i).setText((alphaZeroLiftDistribution.get(i)));
		theVariables.getAlphaZeroLiftUnits().setValue(unitOfMeas);
		
		unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//alpha_star_distribution" + "/@unit"));
		List<String> alphaStarDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_star_distribution").get(0));
		for(int i=0; i<alphaStarDistribution.size(); i++)
			theVariables.getAlphaStarList().get(i).setText((alphaStarDistribution.get(i)));
		theVariables.getAlphaStarUnits().setValue(unitOfMeas);
		
		
		List<String> clMaxDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//maximum_lift_coefficient_distribution").get(0));
		for(int i=0; i<clMaxDistribution.size(); i++)
			theVariables.getClMaxList().get(i).setText((clMaxDistribution.get(i)));
	
	
	}
	
	public static void writeInputToXML(InputOutputTree theInputTree, String filenameWithPathAndExt) {
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			defineXmlTree(doc, docBuilder, theInputTree);
			
			JPADStaticWriteUtils.writeDocumentToXml(doc, filenameWithPathAndExt);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	
	private static void defineXmlTree(Document doc, DocumentBuilder docBuilder, InputOutputTree input) {
		
		org.w3c.dom.Element rootElement = doc.createElement("Wing_analysis");
		doc.appendChild(rootElement);
		
		//--------------------------------------------------------------------------------------
		// INPUT
		//--------------------------------------------------------------------------------------
		org.w3c.dom.Element flightConditionsElement = doc.createElement("operating_conditions");
		rootElement.appendChild(flightConditionsElement);
		
		JPADStaticWriteUtils.writeSingleNode("altitude", input.getAltitude(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("mach_number", input.getMachNumber(), flightConditionsElement, doc);
				
		org.w3c.dom.Element wingDataElement = doc.createElement("wing");
		rootElement.appendChild(wingDataElement);
		org.w3c.dom.Element geometryDataElement = doc.createElement("global");
		wingDataElement.appendChild(geometryDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("surface", input.getSurface(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("aspect_ratio", input.getAspectRatio(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("number_of_point_semispan", input.getNumberOfPointSemispan(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("adimensional_kink_station", input.getAdimensionalKinkStation(), geometryDataElement, doc);
		
		
		org.w3c.dom.Element cleanConfigurationDataElement = doc.createElement("distibution");
		wingDataElement.appendChild(cleanConfigurationDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("number_of_given_sections", input.getNumberOfSections(), cleanConfigurationDataElement, doc);
		
		String airfoilFamily = null;

		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_4_Digit)
			airfoilFamily = "NACA_4_DIGIT";
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_5_Digit)
			airfoilFamily = "NACA_5_DIGIT";
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_63_Series)
			airfoilFamily = "NACA_63_SERIES";
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_64_Series)
			airfoilFamily = "NACA_64_SERIES";
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_65_Series)
			airfoilFamily = "NACA_65_SERIES";
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_66_Series)
			airfoilFamily = "NACA_66_SERIES";
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.BICONVEX)
			airfoilFamily = "BICONVEX";
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.DOUBLE_WEDGE)
			airfoilFamily = "DOUBLE_WEDGE";
		
		JPADStaticWriteUtils.writeSingleNode("airfoil_family", airfoilFamily, cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("max_thickness_mean_airfoil", input.getMeanThickness(), cleanConfigurationDataElement, doc);
		
		org.w3c.dom.Element childDistribution = doc.createElement("geometry");
		cleanConfigurationDataElement.appendChild(childDistribution);
		
		JPADStaticWriteUtils.writeSingleNode("y_adimensional_stations", input.getyAdimensionalStationInput(),childDistribution, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("chord_distribution", input.getChordDistribution(), childDistribution, doc, input.getChordDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("x_le_distribution", input.getxLEDistribution(), childDistribution, doc, input.getxLEDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("twist_distribution", input.getTwistDistribution(), childDistribution, doc, input.getTwistDistribution().get(0).getUnit().toString());
		
		org.w3c.dom.Element childDistributionNew = doc.createElement("aerodynamics");
		cleanConfigurationDataElement.appendChild(childDistributionNew);
		
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_zero_lift_distribution", input.getAlphaZeroLiftDistribution(), childDistributionNew, doc,  input.getAlphaZeroLiftDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_star_distribution", input.getAlphaStarDistribution(), childDistributionNew, doc,  input.getAlphaStarDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("maximum_lift_coefficient_distribution", input.getMaximumliftCoefficientDistribution(), childDistributionNew, doc);
		
	}
	

}
