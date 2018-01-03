package standaloneutils.jsbsim;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
//import org.apache.commons.io.IOUtils;

//import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javaslang.Tuple;
import standaloneutils.cpacs.CPACSUtils;
import writers.JPADStaticWriteUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class JSBSimUtils {

	public static Element createLandingGearElement( List<Double> properties, Double[] position,
			Document doc, String landingGearString, String brake) {
		org.w3c.dom.Element gearElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
				doc,"contact",
				Tuple.of("type", "BOGEY"), 
				Tuple.of("name", landingGearString)
				);
		org.w3c.dom.Element locationElementNoseGear = doc.createElement("location");
		locationElementNoseGear.setAttribute("unit", "M");
		JPADStaticWriteUtils.writeSingleNode("x",position[0],locationElementNoseGear,doc);
		JPADStaticWriteUtils.writeSingleNode("y",position[1],locationElementNoseGear,doc);
		JPADStaticWriteUtils.writeSingleNode("z",position[2],locationElementNoseGear,doc);
		gearElement.appendChild(locationElementNoseGear);

		JPADStaticWriteUtils.writeSingleNode("static_friction",properties.get(0),gearElement,doc);
		JPADStaticWriteUtils.writeSingleNode("dynamic_friction",properties.get(1),gearElement,doc);
		JPADStaticWriteUtils.writeSingleNode("rolling_friction",properties.get(2),gearElement,doc);
		org.w3c.dom.Element springCoeff = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "spring_coeff", properties.get(3), 
				3, 6, Tuple.of("unit", "LBS/FT"));
		gearElement.appendChild(springCoeff);
		org.w3c.dom.Element dynamicCoeff = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "dynamic_coeff", properties.get(4), 
				3, 6, Tuple.of("unit", "LBS/FT"));
		gearElement.appendChild(dynamicCoeff);
		org.w3c.dom.Element dynamicCoeffRebound = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "damping_coeff_rebound", properties.get(5), 
				3, 6, Tuple.of("unit", "LBS/FT/SEC"));
		gearElement.appendChild(dynamicCoeffRebound);
		org.w3c.dom.Element maxSteer = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "max_steer", properties.get(6), 
				3, 6, Tuple.of("unit", "DEG"));
		gearElement.appendChild(maxSteer);
		JPADStaticWriteUtils.writeSingleNode("brake_group",brake,gearElement,doc);
		JPADStaticWriteUtils.writeSingleNode("retractable",properties.get(7),gearElement,doc);

		return gearElement;
		
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
//				org.w3c.dom.Element milthrustElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
//						engineXMLdoc, "milthrust", properties.get(0), 
//						3, 6, Tuple.of("unit", "N"));
//				rootElement.appendChild(milthrustElement);
				JPADStaticWriteUtils.writeSingleNode("milthrust", properties.get(0), rootElement, engineXMLdoc);
				JPADStaticWriteUtils.writeSingleNode("bypassratio", properties.get(1), rootElement, engineXMLdoc);
				JPADStaticWriteUtils.writeSingleNode("idlen1", 30, rootElement, engineXMLdoc);
				JPADStaticWriteUtils.writeSingleNode("idlen2", 60, rootElement, engineXMLdoc);
				JPADStaticWriteUtils.writeSingleNode("maxn1", 100, rootElement, engineXMLdoc);
				JPADStaticWriteUtils.writeSingleNode("maxn2", 100, rootElement, engineXMLdoc);
				JPADStaticWriteUtils.writeSingleNode("augmented", 0, rootElement, engineXMLdoc);
				JPADStaticWriteUtils.writeSingleNode("injected", 0, rootElement, engineXMLdoc);
				engineXMLdoc.appendChild(rootElement);
				org.w3c.dom.Element indleFunctionElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
						engineXMLdoc,"function",
						Tuple.of("name", "IdleThrust") // TODO: get aircraft name from _cpaceReader
						);
				JPADStaticWriteUtils.writeSingleNode("value", 0.03, indleFunctionElement, engineXMLdoc);
				rootElement.appendChild(indleFunctionElement);
				
				org.w3c.dom.Element millFunctionElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
						engineXMLdoc,"function",
						Tuple.of("name", "MilThrust") // TODO: get aircraft name from _cpaceReader
						);
				JPADStaticWriteUtils.writeSingleNode("value", 1.0, millFunctionElement, engineXMLdoc);
				rootElement.appendChild(millFunctionElement);

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
	public static Element createEngineElement(
			List<Double> properties, double []position, double[] rotation,
			Document doc, String engineName, String type, double[][] tankMatrix , String direction) {
		int flagTank = tankMatrix.length;
		org.w3c.dom.Element engineElement = doc.createElement("engine");
		engineElement.setAttribute("file", engineName);
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
		if (direction.equals("right")) {
			for (int i=0; i<flagTank;i++) {
				JPADStaticWriteUtils.writeSingleNode("feed",i,engineElement,doc);
			}
		}
		
		else if (direction.equals("left")) {
			for (int i=flagTank; i<2*flagTank ; i++) {
				JPADStaticWriteUtils.writeSingleNode("feed",i,engineElement,doc);
			}
		}
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
		return engineElement;
	}
	
	
	public static Element createTankElement(
			double[][] properties, Document doc, String position, org.w3c.dom.Element rootElementEngine) {
		//INNER
		org.w3c.dom.Element tankInnerElement = doc.createElement("tank");
		tankInnerElement.setAttribute("type", "FUEL");
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
		tankMidElement.setAttribute("type", "FUEL");
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
		tankOuterElement.setAttribute("type", "FUEL");
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
		return rootElementEngine;
	}
	
	public static Element createSymmetricalControl(
			List<String> deflection, Document doc, List<Integer> number, String controlSurface,
			String axis, int index, String elementName) {
		
		org.w3c.dom.Element rootElementChannel = JPADStaticWriteUtils.createXMLElementWithAttributes(
				doc,"channel",
				Tuple.of("name", elementName )
				);
		org.w3c.dom.Element summerElement = doc.createElement("summer");
		summerElement.setAttribute("name","fcs/"+ axis+"-trim-sum");
		rootElementChannel.appendChild(summerElement);
		JPADStaticWriteUtils.writeSingleNode("input","fcs/" + controlSurface + "-cmd-norm",summerElement,doc);
		if (controlSurface.equals("elevator")) {
			JPADStaticWriteUtils.writeSingleNode("input","fcs/pitch-trim-cmd-norm",summerElement,doc);

		}
		if (controlSurface.equals("rudder")) {
			JPADStaticWriteUtils.writeSingleNode("input","fcs/yaw-trim-cmd-norm",summerElement,doc);
		}
		org.w3c.dom.Element cliptoElement = doc.createElement("clipto");
		summerElement.appendChild(cliptoElement);
		JPADStaticWriteUtils.writeSingleNode("min", -1 ,cliptoElement,doc);
		JPADStaticWriteUtils.writeSingleNode("max", 1,cliptoElement,doc);
		org.w3c.dom.Element aeroSurfaceElement = doc.createElement("aerosurface_scale");
		rootElementChannel.appendChild(aeroSurfaceElement);

		if (controlSurface.equals("elevator")) {
			aeroSurfaceElement.setAttribute("name", "Elevator Control");
			JPADStaticWriteUtils.writeSingleNode("input","fcs/pitch-trim-sum",aeroSurfaceElement,doc);
		}
		if (controlSurface.equals("rudder")) {
			aeroSurfaceElement.setAttribute("name", "Rudder Control");
			JPADStaticWriteUtils.writeSingleNode("input","fcs/yaw-trim-sum",aeroSurfaceElement,doc);
		}
		rootElementChannel.appendChild(aeroSurfaceElement);		
		//Normalized
		org.w3c.dom.Element aeroSurfaceNormElement = doc.createElement("aerosurface_scale");
		if (controlSurface.equals("elevator")) {
			aeroSurfaceNormElement.setAttribute("name", "Elevator Normalized");
		}
		if (controlSurface.equals("rudder")) {
			aeroSurfaceNormElement.setAttribute("name", "Rudder Normalized");
		}
		rootElementChannel.appendChild(aeroSurfaceNormElement);
		JPADStaticWriteUtils.writeSingleNode("input","fcs/" + controlSurface + "-pos-deg",aeroSurfaceNormElement,doc);
		org.w3c.dom.Element domainElement = doc.createElement("domain");
		aeroSurfaceNormElement.appendChild(domainElement);
		org.w3c.dom.Element rangeElement1 = doc.createElement("range");
		aeroSurfaceNormElement.appendChild(rangeElement1);
		//Need this for the if and else  
		int flag = 0;
		int numberDeflection = number.get(index);
		org.w3c.dom.Element rangeElement = doc.createElement("range");	
		aeroSurfaceElement.appendChild(rangeElement);
		if (index>0) {
			for (int i = 0;i<index;i++) {
				flag = flag + number.get(i);
			}
			flag = flag+1 ;//+1 due to array start from 0
			String[] arrayDataMin = deflection.get(flag-1).split(";");

			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],rangeElement,doc);
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],domainElement,doc);
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[1],rangeElement1,doc);
		}
		else {
			String[] arrayDataMin = deflection.get(flag).split(";");
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],rangeElement,doc);
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],domainElement,doc);
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[1],rangeElement1,doc);
		}
		String[] arrayDataMax = deflection.get(flag + numberDeflection - 2 ).split(";");
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[0],rangeElement,doc);
		JPADStaticWriteUtils.writeSingleNode("output","fcs/" + controlSurface + "-pos-deg",aeroSurfaceElement,doc);
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[0],domainElement,doc);
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[1],rangeElement1,doc);
		JPADStaticWriteUtils.writeSingleNode("output","fcs/" + controlSurface + "-pos-norm",aeroSurfaceNormElement,doc);
		return rootElementChannel;
	}
	
	public static Element createAlileronElement(
			List<String> deflection, Document doc, List<Integer> number, String controlSurface, String axis, int index, String elementName) {
		
		org.w3c.dom.Element rootElementChannel = JPADStaticWriteUtils.createXMLElementWithAttributes(
				doc,"channel",
				Tuple.of("name", elementName )
				);
		org.w3c.dom.Element summerElement = doc.createElement("summer");
		summerElement.setAttribute("name", axis+" Trim Sum");

		rootElementChannel.appendChild(summerElement);


		JPADStaticWriteUtils.writeSingleNode("input","fcs/" + controlSurface + "-cmd-norm",summerElement,doc);
		JPADStaticWriteUtils.writeSingleNode("input","fcs/roll-trim-cmd-norm",summerElement,doc);
		org.w3c.dom.Element cliptoElement = doc.createElement("clipto");
		summerElement.appendChild(cliptoElement);
		JPADStaticWriteUtils.writeSingleNode("min", -1 ,cliptoElement,doc);
		JPADStaticWriteUtils.writeSingleNode("max", 1,cliptoElement,doc);
		org.w3c.dom.Element aeroSurfaceElementRight = doc.createElement("aerosurface_scale");
		rootElementChannel.appendChild(aeroSurfaceElementRight);
		aeroSurfaceElementRight.setAttribute("name", "Right Aileron Control");
		JPADStaticWriteUtils.writeSingleNode("input","fcs/roll-trim-sum",aeroSurfaceElementRight,doc);
		JPADStaticWriteUtils.writeSingleNode("output","fcs/right-aileron-pos-deg",aeroSurfaceElementRight,doc);
		rootElementChannel.appendChild(aeroSurfaceElementRight);
		org.w3c.dom.Element rangeElementRight = doc.createElement("range");
		aeroSurfaceElementRight.appendChild(rangeElementRight);
		
		//Normalized
		org.w3c.dom.Element aeroSurfaceNormElementRight = doc.createElement("aerosurface_scale");
		aeroSurfaceNormElementRight.setAttribute("name", "Right Aileron Normalized");
		rootElementChannel.appendChild(aeroSurfaceNormElementRight);
		JPADStaticWriteUtils.writeSingleNode("input","fcs/right-aileron-pos-deg",aeroSurfaceNormElementRight,doc);
		org.w3c.dom.Element domainElementRight = doc.createElement("domain");
		aeroSurfaceNormElementRight.appendChild(domainElementRight);
		org.w3c.dom.Element rangeElement1Right = doc.createElement("range");
		aeroSurfaceNormElementRight.appendChild(rangeElement1Right); 	
		int flag = 0;
		int numberDeflection = number.get(index);

		if (index>0) {
			for (int i = 0;i<index;i++) {
				flag = flag + number.get(i);
			}
			String[] arrayDataMin = deflection.get(flag).split(";");
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],rangeElementRight,doc);
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],domainElementRight,doc);
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[1],rangeElement1Right,doc);
		}
		else {
			String[] arrayDataMin = deflection.get(flag).split(";");
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],rangeElementRight,doc);
		}
		String[] arrayDataMax = deflection.get(flag + numberDeflection -1).split(";");
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[0],rangeElementRight,doc);
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[0],domainElementRight,doc);
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[1],rangeElement1Right,doc);
		JPADStaticWriteUtils.writeSingleNode("output","fcs/right-aileron-pos-norm",aeroSurfaceNormElementRight,doc);
		//Left Aileron
		org.w3c.dom.Element aeroSurfaceElementLeft = doc.createElement("aerosurface_scale");
		rootElementChannel.appendChild(aeroSurfaceElementLeft);
		aeroSurfaceElementLeft.setAttribute("name", "Left Aileron Control");
		JPADStaticWriteUtils.writeSingleNode("input","fcs/roll-trim-sum",aeroSurfaceElementLeft,doc);
		JPADStaticWriteUtils.writeSingleNode("output","fcs/left-aileron-pos-rad",aeroSurfaceElementLeft,doc);
		rootElementChannel.appendChild(aeroSurfaceElementLeft);
		org.w3c.dom.Element rangeElementLeft = doc.createElement("range");
		aeroSurfaceElementLeft.appendChild(rangeElementLeft);
		//Normalized
		org.w3c.dom.Element aeroSurfaceNormElementLeft = doc.createElement("aerosurface_scale");
		aeroSurfaceNormElementLeft.setAttribute("name", "Left Aileron position normalized");
		rootElementChannel.appendChild(aeroSurfaceNormElementLeft);
		JPADStaticWriteUtils.writeSingleNode("input","fcs/left-aileron-pos-deg",aeroSurfaceNormElementLeft,doc);
		org.w3c.dom.Element domainElementLeft = doc.createElement("domain");
		aeroSurfaceNormElementLeft.appendChild(domainElementLeft);
		org.w3c.dom.Element rangeElement1Left = doc.createElement("range");
		aeroSurfaceNormElementLeft.appendChild(rangeElement1Left); 
		JPADStaticWriteUtils.writeSingleNode("gain",Math.toRadians(1),aeroSurfaceElementLeft,doc);
		String[] arrayDataMin = deflection.get(flag).split(";");
		JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],rangeElementLeft,doc);
		JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],domainElementLeft,doc);
		JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[1],rangeElement1Left,doc);

		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[0],rangeElementLeft,doc);
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[0],domainElementLeft,doc);
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[1],rangeElement1Left,doc);
		JPADStaticWriteUtils.writeSingleNode("output","fcs/left-aileron-pos-norm",aeroSurfaceNormElementLeft,doc);
		return rootElementChannel;
	}
	
	
	public static Element createFlapElement(
			List<String> deflection, Document doc, List<Integer> number, String controlSurface, int index, String elementName) {
		org.w3c.dom.Element rootElementChannel = JPADStaticWriteUtils.createXMLElementWithAttributes(
				doc,"channel", Tuple.of("name", elementName )
				);
		//Normalized
		org.w3c.dom.Element aeroSurfaceNormElement = doc.createElement("aerosurface_scale");
		aeroSurfaceNormElement.setAttribute("name", "Flap Position Normalizer");
		rootElementChannel.appendChild(aeroSurfaceNormElement);
		
		
		JPADStaticWriteUtils.writeSingleNode("input","fcs/" + controlSurface + "-pos-deg",aeroSurfaceNormElement,doc);
		org.w3c.dom.Element domainElement = doc.createElement("domain");
		aeroSurfaceNormElement.appendChild(domainElement);
		org.w3c.dom.Element rangeElement1 = doc.createElement("range");
		aeroSurfaceNormElement.appendChild(rangeElement1);
		org.w3c.dom.Element kinematicElement = doc.createElement("kinematic");
		kinematicElement.setAttribute("name", "fcs/flaps-control");
		rootElementChannel.appendChild(kinematicElement);
		JPADStaticWriteUtils.writeSingleNode("input","fcs/flap-cmd-norm",kinematicElement,doc);
		org.w3c.dom.Element traverseElement = doc.createElement("traverse");
		kinematicElement.appendChild(traverseElement);
		org.w3c.dom.Element settingElement = doc.createElement("setting");
		traverseElement.appendChild(settingElement);
		org.w3c.dom.Element settingElement1 = doc.createElement("setting");
		traverseElement.appendChild(settingElement1);
		int flag = 0;
		int numberDeflection = number.get(index);
		if (index>0) {
			for (int i = 0;i<index;i++) {
				flag = flag + number.get(i);
			}
			flag = flag+1 ;//+1 due to array start from 0
			String[] arrayDataMin = deflection.get(flag-1).split(";");
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],domainElement,doc);
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[1],rangeElement1,doc);
			JPADStaticWriteUtils.writeSingleNode("position",arrayDataMin[0],settingElement,doc);
			JPADStaticWriteUtils.writeSingleNode("time",0,settingElement,doc);
		}
		else {
			String[] arrayDataMin = deflection.get(flag).split(";");
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],domainElement,doc);
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[1],rangeElement1,doc);
			JPADStaticWriteUtils.writeSingleNode("position",arrayDataMin[0],settingElement,doc);
			JPADStaticWriteUtils.writeSingleNode("time",0,settingElement,doc);
		}
		String[] arrayDataMax = deflection.get(flag + numberDeflection - 2 ).split(";");
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[0],domainElement,doc);
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[1],rangeElement1,doc);
		JPADStaticWriteUtils.writeSingleNode("position",arrayDataMax[0],settingElement1,doc);
		JPADStaticWriteUtils.writeSingleNode("time",1,settingElement1,doc);
		JPADStaticWriteUtils.writeSingleNode("output","fcs/" + controlSurface + "-pos-norm",aeroSurfaceNormElement,doc);
		JPADStaticWriteUtils.writeSingleNode("output","fcs/flap-pos-deg",kinematicElement,doc);

		return rootElementChannel;
	}

	public static Element createAeroDataBodyAxisElement(Document doc, org.w3c.dom.Element outputElement,List<String> aeroData,
			int machDimension, double[] machVector, String axis, org.w3c.dom.Element axisElement) {

		//FORCE		
		org.w3c.dom.Element forceFunctionElement = 
				doc.createElement("function");
		forceFunctionElement.setAttribute("name", "aero/coefficient/"+axis+"_basic_Mach");
		axisElement.appendChild(forceFunctionElement);
		JPADStaticWriteUtils.writeSingleNode(
				"description",axis+" force due to alpha beta Reynolds number and Mach number",forceFunctionElement,doc);
		org.w3c.dom.Element productForceElement = 
				doc.createElement("product");
		forceFunctionElement.appendChild(productForceElement);
//		JPADStaticWriteUtils.writeSingleNode("property","aero/qbar-psf",productForceElement,doc);
//		JPADStaticWriteUtils.writeSingleNode("property","metrics/Sw-sqft",productForceElement,doc);
		JPADStaticWriteUtils.writeSingleNode("property","aero/coefficient/"+axis+"_basic_Mach",outputElement,doc);
		if ((axis.equals("roll"))||(axis.equals("yaw"))) {
			JPADStaticWriteUtils.writeSingleNode("property","metrics/bw-ft",productForceElement,doc);
		}
		if (axis.equals("pitch")) {
			JPADStaticWriteUtils.writeSingleNode("property","metrics/cbarw-ft",productForceElement,doc);
		}
	
		org.w3c.dom.Element forceInerp1Element = 
				doc.createElement("interpolate1d");
		productForceElement.appendChild(forceInerp1Element);
		JPADStaticWriteUtils.writeSingleNode("p","velocities/mach",forceInerp1Element,doc);
		JPADStaticWriteUtils.writeSingleNode("property","aero/function/"+axis+"_coeff_basic_Mach",outputElement,doc);
		for (int i = 0; i<machDimension; i++) {


			//Force
			org.w3c.dom.Element valueForceInner = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "v", machVector[i], 
					3, 6);
			forceInerp1Element.appendChild(valueForceInner);
			
			org.w3c.dom.Element propertyForceInner = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "p", "aero/function/"+axis+"_coeff_basic_M" + i, 
					3, 6);
			
			forceInerp1Element.appendChild(propertyForceInner);

		}
		
		return axisElement;
	}
	
	public static Element createAeroDataExternalFunctionElement(Document doc, List<String> aeroData, int machDimension,
			double[] machVector, int reynoldsDimension, double[] reynoldsVector, String axis, org.w3c.dom.Element aeroElement ) {

		int counterList = 0;
		for (int i = 0; i<machDimension; i++) {
			org.w3c.dom.Element functionElementInner = 
					doc.createElement("function");
			functionElementInner.setAttribute("name", "aero/function/"+axis+"_coeff_basic_M" + i);
			aeroElement.appendChild(functionElementInner);
			JPADStaticWriteUtils.writeSingleNode("description",
					axis+"_coefficient as function of alpha beta Reynolds, fixed Mach = "+machVector[i] ,functionElementInner,doc);
			org.w3c.dom.Element tableElement = 
					doc.createElement("table");
			functionElementInner.appendChild(tableElement);
			org.w3c.dom.Element rowElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "independentVar", "aero/alpha-deg", 
					3, 6, Tuple.of("lookup", "row"));
			tableElement.appendChild(rowElement);

			org.w3c.dom.Element columnElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "independentVar", "aero/beta-deg", 
					3, 6, Tuple.of("lookup", "column"));
			tableElement.appendChild(columnElement);

			org.w3c.dom.Element tableElementInner = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "independentVar", "aero/Re", 
					3, 6, Tuple.of("lookup", "table"));
			tableElement.appendChild(tableElementInner);
			for (int j = 0; j<reynoldsDimension;j++) {
				org.w3c.dom.Element tableElementBreakPoint = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
						doc, "tableData", aeroData.get(counterList), 
						3, 6, Tuple.of("breakPoint", String.valueOf(reynoldsVector[j])));
				tableElement.appendChild(tableElementBreakPoint);
				counterList = counterList+1;
			}
		}
		
		//Force coefficient
		org.w3c.dom.Element functionElement = 
				doc.createElement("function");
		functionElement.setAttribute("name", "aero/function/"+axis+"_coeff_basic_Mach");
		aeroElement.appendChild(functionElement);
		JPADStaticWriteUtils.writeSingleNode("description",
				axis+"_coefficient as function of alpha beta Reynolds and Mach" ,functionElement,doc);
		org.w3c.dom.Element interpElement = doc.createElement("interpolate1d");
		functionElement.appendChild(interpElement);
		JPADStaticWriteUtils.writeSingleNode("p",
				"velocities/mach" ,interpElement,doc);
		for (int i = 0; i<machDimension; i++) {
			//Coefficient
			org.w3c.dom.Element valueInner = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "v", machVector[i], 
					3, 6);
			interpElement.appendChild(valueInner);
			
			org.w3c.dom.Element propertyInner = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "p", "aero/function/"+axis+"_coeff_basic_M" + i, 
					3, 6);
			interpElement.appendChild(propertyInner);
		}
		return aeroElement;
	}
	
	
	public static Element createAeroDataBodyAxisControlSurfaceElement(
			Document doc, List<String> deltaAeroDataDeflection, int machDimension, double[] machVector, String axis,
			org.w3c.dom.Element axisElement, double[] deflection, String controlSurfaceUID, org.w3c.dom.Element outputElement) {
		
		//Force
		org.w3c.dom.Element forceFunctionElement = 
				doc.createElement("function");
		forceFunctionElement.setAttribute("name", "aero/coefficient/"+controlSurfaceUID+"_"+axis+"_basic_Mach");
		axisElement.appendChild(forceFunctionElement);
		JPADStaticWriteUtils.writeSingleNode(
				"description",axis+" force due to alpha beta Reynolds number and Mach number",forceFunctionElement,doc);
		org.w3c.dom.Element productForceElement = 
				doc.createElement("product");
		forceFunctionElement.appendChild(productForceElement);
		JPADStaticWriteUtils.writeSingleNode("property","aero/qbar-psf",productForceElement,doc);
		JPADStaticWriteUtils.writeSingleNode("property","metrics/Sw-sqft",productForceElement,doc);
		if ((axis.equals("roll"))||(axis.equals("yaw"))) {
			JPADStaticWriteUtils.writeSingleNode("property","metrics/bw-ft",productForceElement,doc);
		}
		if (axis.equals("pitch")) {
			JPADStaticWriteUtils.writeSingleNode("property","metrics/cbarw-ft",productForceElement,doc);
		}
		org.w3c.dom.Element forceInerp1Element = 
				doc.createElement("interpolate1d");
		productForceElement.appendChild(forceInerp1Element);
		JPADStaticWriteUtils.writeSingleNode("p","velocities/mach",forceInerp1Element,doc);
		JPADStaticWriteUtils.writeSingleNode("property","aero/function/"+controlSurfaceUID+"_"+axis+"_coeff_basic_Mach",outputElement,doc);
		JPADStaticWriteUtils.writeSingleNode("property", "aero/coefficient/"+controlSurfaceUID+"_"+axis+"_basic_Mach",outputElement,doc);
		for (int k = 0; k< machVector.length;k++) {
			//Force
			JPADStaticWriteUtils.writeSingleNode("v",machVector[k],forceInerp1Element,doc);
			JPADStaticWriteUtils.writeSingleNode(
					"p","aero/function/"+controlSurfaceUID+"_"+axis+"_coeff_basic_deflection_Mach_"+ k 
					,forceInerp1Element,doc);
		}

		return axisElement;
	}
	
	
	public static Element createAeroDataExternalFunctionControlSurfaceElement(
			Document doc, List<String> deltaAeroDataDeflection, int machDimension, double[] machVector, int reynoldsDimension,
			double[] reynoldsVector, String axis, double[] deflection,String controlSurfaceUID, double[] betaVector, org.w3c.dom.Element aeroElement) {

		int betaDimension = betaVector.length;

		int counter = 0;
		for (int k = 0; k< machVector.length;k++) {	
			for (int i = 0;i<reynoldsDimension;i++) {
				org.w3c.dom.Element functionElementBot = 
						doc.createElement("function");
				functionElementBot.setAttribute(
						"name", "aero/function/" + controlSurfaceUID +
						"_" + axis + "_coeff_basic_Reynolds" + i + "_Mach_" + k);
				aeroElement.appendChild(functionElementBot);
				JPADStaticWriteUtils.writeSingleNode("description",
						axis+"_coefficient as function of "+controlSurfaceUID+"_deflection, alpha,"
								+ " beta, fixed Reynolds = "+ reynoldsVector[i]
								+ "fixed Mach = "+machVector[k],functionElementBot,doc);
				org.w3c.dom.Element tableElement = 
						doc.createElement("table");
				functionElementBot.appendChild(tableElement);
				org.w3c.dom.Element rowElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
						doc, "independentVar", "aero/alpha-deg", 
						3, 6, Tuple.of("lookup", "row"));
				tableElement.appendChild(rowElement);

				if(controlSurfaceUID.equals("aileron")) {
					org.w3c.dom.Element columnElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "independentVar", "fcs/right-"+controlSurfaceUID+"-pos-deg", 
							3, 6, Tuple.of("lookup", "column"));
					tableElement.appendChild(columnElement);

				}
				else if(controlSurfaceUID.equals("flap_inner")) {
					org.w3c.dom.Element columnElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "independentVar", "fcs/flap-pos-deg", 
							3, 6, Tuple.of("lookup", "column"));
					tableElement.appendChild(columnElement);
				}
				else if(controlSurfaceUID.equals("flap_outer")) {
					org.w3c.dom.Element columnElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "independentVar", "fcs/flap-pos-deg", 
							3, 6, Tuple.of("lookup", "column"));
					tableElement.appendChild(columnElement);
				}
				else{
					org.w3c.dom.Element columnElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "independentVar", "fcs/"+controlSurfaceUID+"-pos-deg", 
							3, 6, Tuple.of("lookup", "column"));
					tableElement.appendChild(columnElement);
				}

				org.w3c.dom.Element tableElementInner = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
						doc, "independentVar", "aero/beta-deg", 
						3, 6, Tuple.of("lookup", "table"));
				tableElement.appendChild(tableElementInner);
				
				for (int j = 0; j< betaDimension;j++) {
					org.w3c.dom.Element tableElementBreakPoint = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "tableData", deltaAeroDataDeflection.get(counter), 
							3, 6, Tuple.of("breakPoint", String.valueOf(betaVector[j])));
					tableElement.appendChild(tableElementBreakPoint);
					counter = counter+1;
				}
			}
		}
		
		//Mid Function
		
		for (int k = 0; k< machVector.length;k++) {
			org.w3c.dom.Element functionElementMid = 
					doc.createElement("function");
			functionElementMid.setAttribute(
					"name", "aero/function/"+controlSurfaceUID+"_"+axis+"_coeff_basic_deflection_Mach_"+ k);
			aeroElement.appendChild(functionElementMid);
			JPADStaticWriteUtils.writeSingleNode("description",
					axis+"_coefficient as function of "+controlSurfaceUID+"_deflection, alpha,"
							+ " beta, Reynolds, fixed Mach = "+machVector[k],functionElementMid,doc);
			org.w3c.dom.Element interpElementMid = 
					doc.createElement("interpolate1d");
			functionElementMid.appendChild(interpElementMid);
			JPADStaticWriteUtils.writeSingleNode("p","aero/Re",interpElementMid,doc);
			for (int i = 0;i<reynoldsDimension;i++) {
				JPADStaticWriteUtils.writeSingleNode("v",reynoldsVector[i],interpElementMid,doc);
				JPADStaticWriteUtils.writeSingleNode("p",
						"aero/function/" + controlSurfaceUID + "_" + axis + 
						"_coeff_basic_Reynolds" + i + "_Mach_" + k,
						interpElementMid,doc);
			}
		}
		//Top Function
		org.w3c.dom.Element functionElementTop = 
				doc.createElement("function");
		functionElementTop.setAttribute(
				"name","aero/function/"+controlSurfaceUID+"_"+axis+"_coeff_basic_Mach");
		aeroElement.appendChild(functionElementTop);
		JPADStaticWriteUtils.writeSingleNode("description",
				axis+"_coefficient as function of "+controlSurfaceUID+"_deflection, alpha, beta, Reynolds, Mach ",functionElementTop,doc);
		org.w3c.dom.Element interpElementTop = 
				doc.createElement("interpolate1d");
		functionElementTop.appendChild(interpElementTop);
		JPADStaticWriteUtils.writeSingleNode("p","velocities/mach",interpElementTop,doc);
		for (int k = 0; k< machVector.length;k++) {
			//Coefficient
			JPADStaticWriteUtils.writeSingleNode("v",machVector[k],interpElementTop,doc);
			JPADStaticWriteUtils.writeSingleNode(
					"p","aero/function/"+controlSurfaceUID+"_"+axis+"_coeff_basic_deflection_Mach_"+ k 
					,interpElementTop,doc);
		}
		return aeroElement;
	}
	//Damping derivatives
	public static Element createAeroDataDampingDerivativesBodyAxisElement(Document doc, org.w3c.dom.Element outputElement,List<String> aeroData,
			int machDimension, double[] machVector, String axis, org.w3c.dom.Element axisElement, String DampingDerivatives) {

		//FORCE		
		org.w3c.dom.Element forceFunctionElement = 
				doc.createElement("function");
		forceFunctionElement.setAttribute("name", "aero/coefficient/"+axis+"_basic_Mach");
		axisElement.appendChild(forceFunctionElement);
		JPADStaticWriteUtils.writeSingleNode(
				"description",axis+" force due to alpha beta Reynolds"
						+ " number and Mach number",forceFunctionElement,doc);
		org.w3c.dom.Element productForceElement = 
				doc.createElement("product");
		forceFunctionElement.appendChild(productForceElement);
		JPADStaticWriteUtils.writeSingleNode("property","aero/qbar-psf",productForceElement,doc);
		JPADStaticWriteUtils.writeSingleNode("property","metrics/Sw-sqft",productForceElement,doc);		if (DampingDerivatives.equals("p")) {
			JPADStaticWriteUtils.writeSingleNode("property","velocities/p-aero-rad_sec",productForceElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","aero/bi2vel",productForceElement,doc);
		}
		if (DampingDerivatives.equals("q")) {
			JPADStaticWriteUtils.writeSingleNode("property","velocities/q-aero-rad_sec",productForceElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","aero/ci2vel",productForceElement,doc);
		}
		if (DampingDerivatives.equals("r")) {
			JPADStaticWriteUtils.writeSingleNode("property","velocities/r-aero-rad_sec",productForceElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","aero/bi2vel",productForceElement,doc);
		}
		JPADStaticWriteUtils.writeSingleNode("property","aero/coefficient/"+axis+"_basic_Mach",outputElement,doc);
		if ((axis.equals(DampingDerivatives+"-roll"))||(axis.equals(DampingDerivatives+"-yaw"))) {
			JPADStaticWriteUtils.writeSingleNode("property","metrics/bw-ft",productForceElement,doc);
		}
		if (axis.equals(DampingDerivatives+"-pitch")) {
			JPADStaticWriteUtils.writeSingleNode("property","metrics/cbarw-ft",productForceElement,doc);
		}
		
		org.w3c.dom.Element forceInerp1Element = 
				doc.createElement("interpolate1d");
		productForceElement.appendChild(forceInerp1Element);
		JPADStaticWriteUtils.writeSingleNode("p","velocities/mach",forceInerp1Element,doc);
		JPADStaticWriteUtils.writeSingleNode("property","aero/function/"+axis+"_coeff_basic_Mach",outputElement,doc);
		for (int i = 0; i<machDimension; i++) {


			//Force
			org.w3c.dom.Element valueForceInner = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "v", machVector[i], 
					3, 6);
			forceInerp1Element.appendChild(valueForceInner);
			
			org.w3c.dom.Element propertyForceInner = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "p", "aero/function/"+axis+"_coeff_basic_M" + i, 
					3, 6);
			
			forceInerp1Element.appendChild(propertyForceInner);

		}
		
		return axisElement;
	}
	
	public static Element createAeroDataDampingDerivativesExternalFunctionElement(Document doc, List<String> aeroData, int machDimension,
			double[] machVector, int reynoldsDimension, double[] reynoldsVector, String axis, org.w3c.dom.Element aeroElement ) {
		int counterList = 0;
		for (int i = 0; i<machDimension; i++) {
			org.w3c.dom.Element functionElementInner = 
					doc.createElement("function");
			functionElementInner.setAttribute("name", "aero/function/"+axis+"_coeff_basic_M" + i);
			aeroElement.appendChild(functionElementInner);
			JPADStaticWriteUtils.writeSingleNode(
					"description",axis+" coefficient due to alpha beta Reynolds"
							+ " number, fixed Mach = "+machVector[i],functionElementInner,doc);
			org.w3c.dom.Element tableElement = 
					doc.createElement("table");
			functionElementInner.appendChild(tableElement);
			org.w3c.dom.Element rowElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "independentVar", "aero/alpha-deg", 
					3, 6, Tuple.of("lookup", "row"));
			tableElement.appendChild(rowElement);

			org.w3c.dom.Element columnElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "independentVar", "aero/beta-deg", 
					3, 6, Tuple.of("lookup", "column"));
			tableElement.appendChild(columnElement);

			org.w3c.dom.Element tableElementInner = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "independentVar", "aero/Re", 
					3, 6, Tuple.of("lookup", "table"));
			tableElement.appendChild(tableElementInner);
			for (int j = 0; j< reynoldsDimension;j++) {
				org.w3c.dom.Element tableElementBreakPoint = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
						doc, "tableData", aeroData.get(counterList), 
						3, 6, Tuple.of("breakPoint", String.valueOf(reynoldsVector[j])));
				tableElement.appendChild(tableElementBreakPoint);
				counterList = counterList+1;
			}
		}
		//Force coefficient
		org.w3c.dom.Element functionElement = 
				doc.createElement("function");
		functionElement.setAttribute("name", "aero/function/"+axis+"_coeff_basic_Mach");
		aeroElement.appendChild(functionElement);
		JPADStaticWriteUtils.writeSingleNode(
				"description",axis+" coefficient due to alpha beta Reynolds"
						+ " number and Mach number",functionElement,doc);
		org.w3c.dom.Element interpElement = doc.createElement("interpolate1d");
		functionElement.appendChild(interpElement);
		JPADStaticWriteUtils.writeSingleNode("p",
				"velocities/mach" ,interpElement,doc);
		for (int i = 0; i<machDimension; i++) {
			org.w3c.dom.Element valueInner = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "v", machVector[i], 
					3, 6);
			interpElement.appendChild(valueInner);
			
			org.w3c.dom.Element propertyInner = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "p", "aero/function/"+axis+"_coeff_basic_M" + i, 
					3, 6);
			interpElement.appendChild(propertyInner);
		}
		return aeroElement;
	}
	
	
	public static void runJSBSIM(List<String> commandList, String Argument) throws IOException, InterruptedException {
//		public static void runJSBSIM( List<String> commandList) throws IOException {
	        List<String> command = new ArrayList<String>();

	            command.add("C:/WINDOWS/system32/cmd.exe");
	            command.add("/C");
	            command.add("start");
//	            command.add("/wait");


	        List<String> FinalCommandList = new ArrayList<String>();
	        FinalCommandList.addAll(command);
	        FinalCommandList.addAll(commandList);
//	        FinalCommandList.add("pause");
	        

	        InputStream inputStream = null;
	        InputStream errorStream = null;
	        try {
	            ProcessBuilder processBuilder = new ProcessBuilder(FinalCommandList);
	            processBuilder.directory(new File(Argument));
//	            processBuilder.redirectErrorStream(true); // This is the important part
//	            processBuilder.command(Argument);
	            Process process = processBuilder.start();
	            inputStream = process.getInputStream();
	            errorStream = process.getErrorStream();

//	            System.out.println("Process InputStream: " + IOUtils.toString(inputStream, "utf-8"));
//	            System.out.println("Process ErrorStream: " + IOUtils.toString(errorStream, "utf-8"));
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (inputStream != null) {
	                inputStream .close();
	            }
	            if (errorStream != null) {
	                errorStream.close();
	            }
	        }
	    }


}//end class
