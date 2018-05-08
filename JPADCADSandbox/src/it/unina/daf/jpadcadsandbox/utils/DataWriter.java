package it.unina.daf.jpadcadsandbox.utils;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DataWriter {
	
	private OperatingConditions operatingConditions;
	private GeometricData geometricData;
	private SimulationParameters simulationParameters;
	private Document doc;

	public DataWriter(
			OperatingConditions operatingConditions, 
			GeometricData geometricData, 
			SimulationParameters simulationParameters
			) {
		
		this.operatingConditions = operatingConditions;
		this.geometricData = geometricData;
		this.simulationParameters = simulationParameters;
		
		try {
			// Create new document
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			// Create root element data
			Element rootElement = doc.createElement("data");
			doc.appendChild(rootElement);
			
			// Create sub-elements operating conditions, geometric data and simulation parameters
			Element operatingConditionsElement = doc.createElement("operating_conditions");
			Element geometricDataElement = doc.createElement("geometric_data");
			Element simulationParametersElement = doc.createElement("simulation_parameters");
			
			rootElement.appendChild(operatingConditionsElement);
			rootElement.appendChild(geometricDataElement);
			rootElement.appendChild(simulationParametersElement);
			
			// Create elements for operating conditions
			Element angleOfAttack = doc.createElement("angle_of_attack");
			Element sideslipAngle = doc.createElement("sideslip_angle");
			Element machNumber = doc.createElement("Mach");
			Element reynoldsNumber = doc.createElement("Reynolds");
			Element altitude = doc.createElement("altitude");
			Element pressure = doc.createElement("pressure");
			Element density = doc.createElement("density");
			Element temperature = doc.createElement("temperature");
			Element speedOfSound = doc.createElement("speed_of_sound");
			Element dynamicViscosity = doc.createElement("dynamic_viscosity");
			Element velocity = doc.createElement("velocity");
			
			altitude.setAttribute("unit", "ft");
			pressure.setAttribute("unit", "Pa");
			density.setAttribute("unit", "kg/m^3");
			temperature.setAttribute("unit", "K");
			speedOfSound.setAttribute("unit", "m/s");
			dynamicViscosity.setAttribute("unit", "Pa*s");
			velocity.setAttribute("unit", "m/s");
			
			angleOfAttack.appendChild(doc.createTextNode(Double.toString(operatingConditions.getAngleOfAttack())));
			sideslipAngle.appendChild(doc.createTextNode(Double.toString(operatingConditions.getSideslipAngle())));
			machNumber.appendChild(doc.createTextNode(Double.toString(operatingConditions.getMachNumber())));
			reynoldsNumber.appendChild(doc.createTextNode(Double.toString(operatingConditions.getReynoldsNumber())));
			altitude.appendChild(doc.createTextNode(Double.toString(operatingConditions.getAltitude())));
			pressure.appendChild(doc.createTextNode(Double.toString(operatingConditions.getPressure())));
			density.appendChild(doc.createTextNode(Double.toString(operatingConditions.getDensity())));
			temperature.appendChild(doc.createTextNode(Double.toString(operatingConditions.getTemperature())));
			speedOfSound.appendChild(doc.createTextNode(Double.toString(operatingConditions.getSpeedOfSound())));
			dynamicViscosity.appendChild(doc.createTextNode(Double.toString(operatingConditions.getDynamicViscosity())));
			velocity.appendChild(doc.createTextNode(Double.toString(operatingConditions.getVelocity())));
			
			operatingConditionsElement.appendChild(angleOfAttack);
			operatingConditionsElement.appendChild(sideslipAngle);
			operatingConditionsElement.appendChild(machNumber);
			operatingConditionsElement.appendChild(reynoldsNumber);
			operatingConditionsElement.appendChild(altitude);
			operatingConditionsElement.appendChild(pressure);
			operatingConditionsElement.appendChild(density);
			operatingConditionsElement.appendChild(temperature);
			operatingConditionsElement.appendChild(speedOfSound);
			operatingConditionsElement.appendChild(dynamicViscosity);
			operatingConditionsElement.appendChild(velocity);
			
			// Create elements for geometric data
			Element cadUnits = doc.createElement("CAD_units");
			Element aeroComponents = doc.createElement("aero_components");
			Element componentsNumber = doc.createElement("components_number");
			Element fuselageLength = doc.createElement("fuselage_length");
			Element wingMAC = doc.createElement("wing_MAC");
			Element wingS = doc.createElement("wing_S");
			Element wingSpan = doc.createElement("wing_span");
			Element momentPoleXCoord = doc.createElement("moment_pole_Xcoord");
			
			fuselageLength.setAttribute("unit", "m");
			wingMAC.setAttribute("unit", "m");
			wingS.setAttribute("unit", "m^2");
			wingSpan.setAttribute("unit", "m");
			momentPoleXCoord.setAttribute("unit", "m");
			
			cadUnits.appendChild(doc.createTextNode(geometricData.getCADUnits()));
			aeroComponents.appendChild(doc.createTextNode(geometricData.getComponents()));
			componentsNumber.appendChild(doc.createTextNode(geometricData.getComponentsNumber()));
			fuselageLength.appendChild(doc.createTextNode(Double.toString(geometricData.getFuselageLength())));
			wingMAC.appendChild(doc.createTextNode(Double.toString(geometricData.getMeanAerodynamicChord())));
			wingS.appendChild(doc.createTextNode(Double.toString(geometricData.getWingSurface())));
			wingSpan.appendChild(doc.createTextNode(Double.toString(geometricData.getWingSpan())));
			momentPoleXCoord.appendChild(doc.createTextNode(Double.toString(geometricData.getMomentPoleXCoord())));
			
			geometricDataElement.appendChild(cadUnits);
			geometricDataElement.appendChild(aeroComponents);
			geometricDataElement.appendChild(componentsNumber);
			geometricDataElement.appendChild(fuselageLength);
			geometricDataElement.appendChild(wingMAC);
			geometricDataElement.appendChild(wingS);
			geometricDataElement.appendChild(wingSpan);
			geometricDataElement.appendChild(momentPoleXCoord);
			
			// Create elements for simulation parameters
			Element type = doc.createElement("type");
			Element symmetrical = doc.createElement("symmetrical");
			Element executeAutomesh = doc.createElement("execute_automesh");
			
			type.appendChild(doc.createTextNode(simulationParameters.getSimulationType()));
			symmetrical.appendChild(doc.createTextNode(Boolean.toString(simulationParameters.isSimulationSymmetrical())));
			executeAutomesh.appendChild(doc.createTextNode(Boolean.toString(simulationParameters.executeMeshOperation())));
			
			simulationParametersElement.appendChild(type);
			simulationParametersElement.appendChild(symmetrical);
			simulationParametersElement.appendChild(executeAutomesh);
			
			// Pass the document
			this.doc = doc;
		}
		
		catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}
	}
	
	public void write(String filepath) {
		
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			StreamResult result = new StreamResult(new File(filepath));			
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.transform(source, result);
		}
		
		catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
	
	public OperatingConditions getOperatingConditions() {
		return this.operatingConditions;
	}

	public GeometricData getGeometricData() {
		return this.geometricData;
	}
	
	public SimulationParameters getSimulationParameters() {
		return this.simulationParameters;
	}
	
	public Document getDocument() {
		return this.doc;
	}
}
