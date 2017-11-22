package standaloneutils.jsbsim;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javaslang.Tuple;
import standaloneutils.cpacs.CPACSUtils;
import writers.JPADStaticWriteUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class JSBSimUtils {

	public static void writeLandingGear(
			org.w3c.dom.Element rootElementGear,List<Double> properties, Double []position,
			Document doc, String landingGearString, String brake) {
		org.w3c.dom.Element noseGearElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
				doc,"contact",
				Tuple.of("type", "BOGEY"), 
				Tuple.of("name", landingGearString)
				);
		org.w3c.dom.Element locationElementNoseGear = doc.createElement("location");
		locationElementNoseGear.setAttribute("unit", "M");
		JPADStaticWriteUtils.writeSingleNode("x",position[0],locationElementNoseGear,doc);
		JPADStaticWriteUtils.writeSingleNode("y",position[1],locationElementNoseGear,doc);
		JPADStaticWriteUtils.writeSingleNode("z",position[2],locationElementNoseGear,doc);
		noseGearElement.appendChild(locationElementNoseGear);

		JPADStaticWriteUtils.writeSingleNode("static_friction",properties.get(0),noseGearElement,doc);
		JPADStaticWriteUtils.writeSingleNode("dynamic_friction",properties.get(1),noseGearElement,doc);
		JPADStaticWriteUtils.writeSingleNode("rolling_friction",properties.get(2),noseGearElement,doc);
		org.w3c.dom.Element springCoeff = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "spring_coeff", properties.get(3), 
				3, 6, Tuple.of("unit", "LBS/FT"));
		noseGearElement.appendChild(springCoeff);
		org.w3c.dom.Element dynamicCoeff = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "dynamic_coeff", properties.get(4), 
				3, 6, Tuple.of("unit", "LBS/FT"));
		noseGearElement.appendChild(dynamicCoeff);
		org.w3c.dom.Element dynamicCoeffRebound = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "damping_coeff_rebound", properties.get(5), 
				3, 6, Tuple.of("unit", "LBS/FT"));
		noseGearElement.appendChild(dynamicCoeffRebound);
		org.w3c.dom.Element maxSteer = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "max_steer", properties.get(6), 
				3, 6, Tuple.of("unit", "DEG"));
		noseGearElement.appendChild(maxSteer);
		JPADStaticWriteUtils.writeSingleNode("brake_group",brake,locationElementNoseGear,doc);
		JPADStaticWriteUtils.writeSingleNode("retractable",properties.get(7),locationElementNoseGear,doc);

		rootElementGear.appendChild(noseGearElement);
		
	}
	
	public static void createEngineXML(List<Double> properties, String engineName,
			String type, String dirPath, String Control) 
					throws IOException, TransformerException, ParserConfigurationException {
		System.out.println(dirPath);
		String engineXMLPath = dirPath+"/"+engineName+".xml";
		Document doc = makeEngineXmlTree(properties, engineName, type);
		
		System.out.println("[JSBSimModel.exportToXML] writing file " + engineXMLPath + " ...");
		JPADStaticWriteUtils.writeDocumentToXml(doc, engineXMLPath);
		if (Control.equals("JET")) {
			String thrusterXMLPath = dirPath+"/"+"direct.xml";
			Document docThruster = writeThrusterJetXML();
			JPADStaticWriteUtils.writeDocumentToXml(docThruster, thrusterXMLPath);

		}
		
	}
	
	
	private static Document writeThrusterJetXML() throws ParserConfigurationException {
		DocumentBuilderFactory docFactorythruster = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilderthruster;
		Document thrusterdoc = null;
		
			try {
				docBuilderthruster = docFactorythruster.newDocumentBuilder();
				thrusterdoc = docBuilderthruster.newDocument();
				
				org.w3c.dom.Element rootElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
						thrusterdoc,"direct",
						Tuple.of("name", "Direct") 
						);
				thrusterdoc.appendChild(rootElement);
			} catch (SecurityException Se) {
				System.out.println("Error while creating file " + Se);
			}	
		return thrusterdoc;
	}
	
	
	
	
	private static Document makeEngineXmlTree(List<Double> properties, String engineName,
			String typeEngine) throws TransformerException, ParserConfigurationException {
		DocumentBuilderFactory docFactoryEngine = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilderEngine;
		Document engineXMLdoc = null;
		
			try {
				docBuilderEngine = docFactoryEngine.newDocumentBuilder();
				engineXMLdoc = docBuilderEngine.newDocument();
				
				org.w3c.dom.Element rootElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
						engineXMLdoc,typeEngine+"_engine",
						Tuple.of("name", engineName) // TODO: get aircraft name from _cpaceReader
						);
//				engineXMLdoc.appendChild(rootElement);
				JPADStaticWriteUtils.writeSingleNode("milthrust", properties.get(0), rootElement, engineXMLdoc);
				JPADStaticWriteUtils.writeSingleNode("bypassratio", properties.get(1), rootElement, engineXMLdoc);
				JPADStaticWriteUtils.writeSingleNode("augmented", 0, rootElement, engineXMLdoc);
				JPADStaticWriteUtils.writeSingleNode("injected", 0, rootElement, engineXMLdoc);
				engineXMLdoc.appendChild(rootElement);

				

			} catch (SecurityException Se) {
				System.out.println("Error while creating file " + Se);
			}
		
		return engineXMLdoc;
	}
	/**
	 * @param rootElementEngine
	 * @param properties
	 * @param position
	 * @param rotation
	 * @param doc
	 * @param engineName
	 * @param type
	 * @param dirPath 
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 */
	public static void writeEngine(
			org.w3c.dom.Element rootElementEngine,List<Double> properties, double []position, double[] rotation,
			Document doc, String engineName, String type) {

		org.w3c.dom.Element engineElement = doc.createElement("engine");
		engineElement.setAttribute("file", engineName);

		rootElementEngine.appendChild(engineElement);

		org.w3c.dom.Element engineLocationElement = doc.createElement("location");
		engineLocationElement.setAttribute("unit", "M");
		JPADStaticWriteUtils.writeSingleNode("x",position[0],engineLocationElement,doc);
		JPADStaticWriteUtils.writeSingleNode("y",position[1],engineLocationElement,doc);
		JPADStaticWriteUtils.writeSingleNode("z",position[2],engineLocationElement,doc);
		engineElement.appendChild(engineLocationElement);

		org.w3c.dom.Element engineRotationElement = doc.createElement("orient");
		engineRotationElement.setAttribute("unit", "DEG");
		JPADStaticWriteUtils.writeSingleNode("roll",rotation[0],engineRotationElement,doc);
		JPADStaticWriteUtils.writeSingleNode("pitch",rotation[1],engineRotationElement,doc);
		JPADStaticWriteUtils.writeSingleNode("yaw",rotation[2],engineRotationElement,doc);
		engineElement.appendChild(engineRotationElement);
		org.w3c.dom.Element thrusterElement = doc.createElement("thruster");
		thrusterElement.setAttribute("file", "direct");
		if (type.equals("JET")) {
			org.w3c.dom.Element thrusterLocationElement = doc.createElement("location");
			thrusterLocationElement.setAttribute("unit", "M");
			JPADStaticWriteUtils.writeSingleNode("x",position[0],thrusterLocationElement,doc);
			JPADStaticWriteUtils.writeSingleNode("y",position[1],thrusterLocationElement,doc);
			JPADStaticWriteUtils.writeSingleNode("z",position[2],thrusterLocationElement,doc);
			thrusterElement.appendChild(thrusterLocationElement);

			org.w3c.dom.Element thrusterRotationElement = doc.createElement("orient");
			thrusterRotationElement.setAttribute("unit", "DEG");
			JPADStaticWriteUtils.writeSingleNode("roll",rotation[0],thrusterRotationElement,doc);
			JPADStaticWriteUtils.writeSingleNode("pitch",rotation[1],thrusterRotationElement,doc);
			JPADStaticWriteUtils.writeSingleNode("yaw",rotation[2],thrusterRotationElement,doc);
			thrusterElement.appendChild(thrusterRotationElement);
		}
		engineElement.appendChild(thrusterElement);
		rootElementEngine.appendChild(engineElement);
	}
	
	
	public static void writeTank(
			org.w3c.dom.Element rootElementEngine, double[][] properties,
			Document doc, String position) {
		//INNER
		org.w3c.dom.Element tankInnerElement = doc.createElement("tank");
		tankInnerElement.setAttribute("tank", "FUEL");
		rootElementEngine.appendChild(tankInnerElement);
		org.w3c.dom.Element tankInnerLocationElement = doc.createElement("location");
		tankInnerLocationElement.setAttribute("unit", "M");
		JPADStaticWriteUtils.writeSingleNode("x",properties[1][0],tankInnerLocationElement,doc);
		if(position.equals("RIGHT")) {
			JPADStaticWriteUtils.writeSingleNode("y",properties[1][1],tankInnerLocationElement,doc);
		}
		else {
			JPADStaticWriteUtils.writeSingleNode("y",-properties[1][1],tankInnerLocationElement,doc);
		}
		JPADStaticWriteUtils.writeSingleNode("z",properties[1][2],tankInnerLocationElement,doc);
		tankInnerElement.appendChild(tankInnerLocationElement);
		org.w3c.dom.Element tankInnerCapacityElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "capacity", properties[1][3], 
				3, 6, Tuple.of("unit", "KG"));		
		tankInnerElement.appendChild(tankInnerCapacityElement);
		org.w3c.dom.Element tankInnerContentsElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "contents", properties[1][3], 
				3, 6, Tuple.of("unit", "KG"));		
		tankInnerElement.appendChild(tankInnerContentsElement);
		rootElementEngine.appendChild(tankInnerElement);
		//MID
		org.w3c.dom.Element tankMidElement = doc.createElement("tank");
		tankMidElement.setAttribute("tank", "FUEL");
		rootElementEngine.appendChild(tankMidElement);
		org.w3c.dom.Element tankMidLocationElement = doc.createElement("location");
		tankMidLocationElement.setAttribute("unit", "M");
		JPADStaticWriteUtils.writeSingleNode("x",properties[2][0],tankMidLocationElement,doc);
		JPADStaticWriteUtils.writeSingleNode("z",properties[2][2],tankMidLocationElement,doc);
		if(position.equals("RIGHT")) {
			JPADStaticWriteUtils.writeSingleNode("y",properties[2][1],tankMidLocationElement,doc);
		}
		else {
			JPADStaticWriteUtils.writeSingleNode("y",-properties[2][1],tankMidLocationElement,doc);
		}
		tankMidElement.appendChild(tankMidLocationElement);
		org.w3c.dom.Element tankMidCapacityElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "capacity", properties[2][3], 
				3, 6, Tuple.of("unit", "KG"));		
		tankMidElement.appendChild(tankMidCapacityElement);
		org.w3c.dom.Element tankMidContentsElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "contents", properties[2][3], 
				3, 6, Tuple.of("unit", "KG"));		
		tankMidElement.appendChild(tankMidContentsElement);
		rootElementEngine.appendChild(tankMidElement);
		
		//OUTER
		
		org.w3c.dom.Element tankOuterElement = doc.createElement("tank");
		tankOuterElement.setAttribute("tank", "FUEL");
		rootElementEngine.appendChild(tankOuterElement);
		org.w3c.dom.Element tankOuterLocationElement = doc.createElement("location");
		tankOuterLocationElement.setAttribute("unit", "M");
		JPADStaticWriteUtils.writeSingleNode("x",properties[0][0],tankOuterLocationElement,doc);
		JPADStaticWriteUtils.writeSingleNode("z",properties[0][2],tankOuterLocationElement,doc);
		if(position.equals("RIGHT")) {
			JPADStaticWriteUtils.writeSingleNode("y",properties[0][1],tankOuterLocationElement,doc);
		}
		else {
			JPADStaticWriteUtils.writeSingleNode("y",-properties[0][1],tankOuterLocationElement,doc);
		}
		tankOuterElement.appendChild(tankOuterLocationElement);
		org.w3c.dom.Element tankOuterCapacityElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "capacity", properties[0][3], 
				3, 6, Tuple.of("unit", "KG"));		
		tankOuterElement.appendChild(tankOuterCapacityElement);
		org.w3c.dom.Element tankOuterContentsElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "contents", properties[0][3], 
				3, 6, Tuple.of("unit", "KG"));		
		tankOuterElement.appendChild(tankOuterContentsElement);
		rootElementEngine.appendChild(tankOuterElement);
		
	}
	
	public static void writeSymmetricalControl(
			org.w3c.dom.Element rootElementChannel,List<String> deflection,
			Document doc, List<Integer> number, String controlSurface, String axis, int index) {

		org.w3c.dom.Element summerElement = doc.createElement("summer");
		summerElement.setAttribute("name", axis+" Trim Sum");

		rootElementChannel.appendChild(summerElement);


		JPADStaticWriteUtils.writeSingleNode("input","fcs/" + controlSurface + "-cmd-norm",summerElement,doc);
		if (controlSurface.equals("elevator")) {
			JPADStaticWriteUtils.writeSingleNode("input","pitch-trim-cmd-norm",summerElement,doc);
		}
		if (controlSurface.equals("rudder")) {
			JPADStaticWriteUtils.writeSingleNode("input","fcs/yaw-trim-sum",summerElement,doc);
		}
		org.w3c.dom.Element cliptoElement = doc.createElement("clipto");
		summerElement.appendChild(cliptoElement);
		JPADStaticWriteUtils.writeSingleNode("min", -1 ,cliptoElement,doc);
		JPADStaticWriteUtils.writeSingleNode("max", 1,cliptoElement,doc);
		org.w3c.dom.Element aeroSurfaceElement = doc.createElement("aerosurface_scale");

		if (controlSurface.equals("elevator")) {
			aeroSurfaceElement.setAttribute("name", "Elevator Control");
		}
		if (controlSurface.equals("rudder")) {
			aeroSurfaceElement.setAttribute("name", "Rudder Control");
		}
		rootElementChannel.appendChild(aeroSurfaceElement);
		
		
		//Normalized
		org.w3c.dom.Element aeroSurfaceNormElement = doc.createElement("aerosurface_scale");

		if (controlSurface.equals("elevator")) {
			aeroSurfaceNormElement.setAttribute("name", "Elevator position normalized");
		}
		if (controlSurface.equals("rudder")) {
			aeroSurfaceNormElement.setAttribute("name", "Rudder position normalized");
		}
		rootElementChannel.appendChild(aeroSurfaceNormElement);
		JPADStaticWriteUtils.writeSingleNode("input","fcs/" + controlSurface + "-pos-deg",aeroSurfaceNormElement,doc);
		org.w3c.dom.Element domainElement = doc.createElement("domain");
		aeroSurfaceNormElement.appendChild(domainElement);
		org.w3c.dom.Element rangeElement1 = doc.createElement("range");
		aeroSurfaceNormElement.appendChild(rangeElement1);

		//Need this for the if and else  
		
		JPADStaticWriteUtils.writeSingleNode("input","fcs/" + controlSurface + "-trim-sum",aeroSurfaceElement,doc);
		JPADStaticWriteUtils.writeSingleNode("gain",Math.toRadians(1),aeroSurfaceElement,doc);
		int flag = 0;
		int numberDeflection = number.get(index);
		org.w3c.dom.Element rangeElement = doc.createElement("range");
		
		
		System.out.println("Number of Deflection = "+ deflection.size());
		aeroSurfaceElement.appendChild(rangeElement);
		if (index>0) {
			for (int i = 0;i<index;i++) {
				flag = flag + number.get(i);
			}
			flag = flag-numberDeflection + 1 ;//+1 due to array start from 0
			String[] arrayDataMin = deflection.get(flag-1).split(";");
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],rangeElement,doc);
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],domainElement,doc);
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[1],rangeElement1,doc);
		}
		else {
			String[] arrayDataMin = deflection.get(flag).split(";");
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],rangeElement,doc);
		}
		String[] arrayDataMax = deflection.get(flag + numberDeflection - 2 ).split(";");
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[0],rangeElement,doc);
		JPADStaticWriteUtils.writeSingleNode("output","fcs/" + controlSurface + "-pos-rad",aeroSurfaceElement,doc);
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[0],domainElement,doc);
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[1],rangeElement1,doc);
		JPADStaticWriteUtils.writeSingleNode("output","fcs/" + controlSurface + "-pos-norm",aeroSurfaceNormElement,doc);
	}
	
}//end class
