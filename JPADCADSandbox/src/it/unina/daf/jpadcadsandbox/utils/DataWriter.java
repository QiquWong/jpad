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
			Element wingRootChord = doc.createElement("equivalent_wing_root_chord");
			Element wingTaperRatio = doc.createElement("equivalent_wing_taper_ratio");
			Element wingS = doc.createElement("wing_S");
			Element wingSpan = doc.createElement("wing_span");
			Element momentPoleXCoord = doc.createElement("moment_pole_Xcoord");
			Element xPosWing = doc.createElement("wing_x_position");
			Element zPosWing = doc.createElement("wing_z_position");
			Element sweepWing = doc.createElement("wing_LEsweep_angle");
			Element dihedralWing = doc.createElement("wing_dihedral_angle");
			Element riggingWing = doc.createElement("wing_rigging_angle");
			Element xPosCanard = doc.createElement("canard_x_position");
			Element zPosCanard = doc.createElement("canard_z_position");
			Element canardRootChord = doc.createElement("equivalent_canard_root_chord");
			Element canardTaperRatio = doc.createElement("equivalent_canard_taper_ratio");
			Element spanCanard = doc.createElement("canard_span");
			Element sweepCanard = doc.createElement("canard_LEsweep_angle");
			Element dihedralCanard = doc.createElement("canard_dihedral_angle");
			Element riggingCanard = doc.createElement("canard_rigging_angle");
			
			
			
			fuselageLength.setAttribute("unit", "m");
			wingMAC.setAttribute("unit", "m");
			wingRootChord.setAttribute("unit", "m");
			wingS.setAttribute("unit", "m^2");
			wingSpan.setAttribute("unit", "m");
			momentPoleXCoord.setAttribute("unit", "m");
			xPosWing.setAttribute("unit", "m");
			zPosWing.setAttribute("unit", "m");
			sweepWing.setAttribute("unit", "degree");
			dihedralWing.setAttribute("unit", "degree");
			riggingWing.setAttribute("unit", "degree");
			xPosCanard.setAttribute("unit", "m");
			zPosCanard.setAttribute("unit", "m");
			canardRootChord.setAttribute("unit", "m");
			spanCanard.setAttribute("unit", "m");
			sweepCanard.setAttribute("unit", "degree");
			dihedralCanard.setAttribute("unit", "degree");
			riggingCanard.setAttribute("unit", "degree");
		    
		    
			
			cadUnits.appendChild(doc.createTextNode(geometricData.getCADUnits()));
			aeroComponents.appendChild(doc.createTextNode(geometricData.getComponents()));
			componentsNumber.appendChild(doc.createTextNode(geometricData.getComponentsNumber()));
			fuselageLength.appendChild(doc.createTextNode(Double.toString(geometricData.getFuselageLength())));
			wingMAC.appendChild(doc.createTextNode(Double.toString(geometricData.getMeanAerodynamicChord())));
			wingRootChord.appendChild(doc.createTextNode(Double.toString(geometricData.getwingRootChord())));
			wingTaperRatio.appendChild(doc.createTextNode(Double.toString(geometricData.getwingTaperRatio())));
			wingS.appendChild(doc.createTextNode(Double.toString(geometricData.getWingSurface())));
			wingSpan.appendChild(doc.createTextNode(Double.toString(geometricData.getWingSpan())));
			momentPoleXCoord.appendChild(doc.createTextNode(Double.toString(geometricData.getMomentPoleXCoord())));
			xPosWing.appendChild(doc.createTextNode(Double.toString(geometricData.getxPosWing())));
			zPosWing.appendChild(doc.createTextNode(Double.toString(geometricData.getzPosWing())));
			sweepWing.appendChild(doc.createTextNode(Double.toString(geometricData.getsweepWing())));
			dihedralWing.appendChild(doc.createTextNode(Double.toString(geometricData.getdihedralWing())));
			riggingWing.appendChild(doc.createTextNode(Double.toString(geometricData.getriggingWing())));
			xPosCanard.appendChild(doc.createTextNode(Double.toString(geometricData.getxPosCanard())));
			zPosCanard.appendChild(doc.createTextNode(Double.toString(geometricData.getzPosCanard())));
			canardRootChord.appendChild(doc.createTextNode(Double.toString(geometricData.getcanardRootChord())));			
			canardTaperRatio.appendChild(doc.createTextNode(Double.toString(geometricData.getcanardTaperRatio())));
			spanCanard.appendChild(doc.createTextNode(Double.toString(geometricData.getspanCanard())));
			sweepCanard.appendChild(doc.createTextNode(Double.toString(geometricData.getsweepCanard())));
			dihedralCanard.appendChild(doc.createTextNode(Double.toString(geometricData.getdihedralCanard())));
			riggingCanard.appendChild(doc.createTextNode(Double.toString(geometricData.getriggingCanard())));
			
			
			
			
			geometricDataElement.appendChild(cadUnits);
			geometricDataElement.appendChild(aeroComponents);
			geometricDataElement.appendChild(componentsNumber);
			geometricDataElement.appendChild(fuselageLength);
			geometricDataElement.appendChild(wingMAC);
			geometricDataElement.appendChild(wingRootChord);
			geometricDataElement.appendChild(wingTaperRatio);
			geometricDataElement.appendChild(wingS);
			geometricDataElement.appendChild(wingSpan);
			geometricDataElement.appendChild(momentPoleXCoord);
			geometricDataElement.appendChild(xPosWing);
			geometricDataElement.appendChild(zPosWing);
			geometricDataElement.appendChild(sweepWing);
			geometricDataElement.appendChild(dihedralWing);
			geometricDataElement.appendChild(riggingWing);
			geometricDataElement.appendChild(xPosCanard);
			geometricDataElement.appendChild(zPosCanard);
			geometricDataElement.appendChild(canardRootChord);
			geometricDataElement.appendChild(canardTaperRatio);
			geometricDataElement.appendChild(spanCanard);
			geometricDataElement.appendChild(sweepCanard);
			geometricDataElement.appendChild(dihedralCanard);
			geometricDataElement.appendChild(riggingCanard);
			
			
			
			// Create elements for simulation parameters
			Element type = doc.createElement("type");
			Element symmetrical = doc.createElement("symmetrical");
			Element executeAutomesh = doc.createElement("execute_automesh");
			Element xPosCase = doc.createElement("x_position_case");
			Element zPosCase = doc.createElement("z_position_case");
			Element spanCase = doc.createElement("span_case");
			Element sweepCase = doc.createElement("sweep_case");
			Element dihedralCase = doc.createElement("dihedral_angle_case");
			Element riggingCase = doc.createElement("rigging_angle_case");
			Element machCase = doc.createElement("mach_case");
			Element alphaCase = doc.createElement("alpha_case");
			
			type.appendChild(doc.createTextNode(simulationParameters.getSimulationType()));
			symmetrical.appendChild(doc.createTextNode(Boolean.toString(simulationParameters.isSimulationSymmetrical())));
			executeAutomesh.appendChild(doc.createTextNode(Boolean.toString(simulationParameters.executeMeshOperation())));
			xPosCase.appendChild(doc.createTextNode(Integer.toString(simulationParameters.xPos_Case())));
			zPosCase.appendChild(doc.createTextNode(Integer.toString(simulationParameters.zPos_Case())));
			spanCase.appendChild(doc.createTextNode(Integer.toString(simulationParameters.span_Case())));
			sweepCase.appendChild(doc.createTextNode(Integer.toString(simulationParameters.sweep_Case())));
			dihedralCase.appendChild(doc.createTextNode(Integer.toString(simulationParameters.dihedral_Case())));
			riggingCase.appendChild(doc.createTextNode(Integer.toString(simulationParameters.rigging_Case())));
			machCase.appendChild(doc.createTextNode(Integer.toString(simulationParameters.mach_Case())));
			alphaCase.appendChild(doc.createTextNode(Integer.toString(simulationParameters.alpha_Case())));
			
			simulationParametersElement.appendChild(type);
			simulationParametersElement.appendChild(symmetrical);
			simulationParametersElement.appendChild(executeAutomesh);
			simulationParametersElement.appendChild(xPosCase);
			simulationParametersElement.appendChild(zPosCase);
			simulationParametersElement.appendChild(spanCase);
			simulationParametersElement.appendChild(sweepCase);
			simulationParametersElement.appendChild(dihedralCase);
			simulationParametersElement.appendChild(riggingCase);
			simulationParametersElement.appendChild(machCase);
			simulationParametersElement.appendChild(alphaCase);
			
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
