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

	public static void writeLandingGear(
			org.w3c.dom.Element rootElementGear, List<Double> properties, Double[] position,
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
				3, 6, Tuple.of("unit", "LBS/FT/SEC"));
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
				org.w3c.dom.Element milthrustElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
						engineXMLdoc, "milthrust", properties.get(0), 
						3, 6, Tuple.of("unit", "N"));
				rootElement.appendChild(milthrustElement);
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
	public static void writeEngine(
			org.w3c.dom.Element rootElementEngine,List<Double> properties, double []position, double[] rotation,
			Document doc, String engineName, String type, double[][] tankMatrix , String direction) {
		int flagTank = tankMatrix.length;
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
		rootElementEngine.appendChild(engineElement);
	}
	
	
	public static void writeTank(
			org.w3c.dom.Element rootElementEngine, double[][] properties,
			Document doc, String position) {
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
		
	}
	
	public static void writeSymmetricalControl(
			org.w3c.dom.Element rootElementChannel,List<String> deflection,
			Document doc, List<Integer> number, String controlSurface, String axis, int index) {

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
		
//		JPADStaticWriteUtils.writeSingleNode("gain",Math.toRadians(1),aeroSurfaceElement,doc);
		int flag = 0;
		int numberDeflection = number.get(index);
//		System.out.println("number ="+number);
		org.w3c.dom.Element rangeElement = doc.createElement("range");
		
		
//		System.out.println("Number of Deflection = "+ deflection.size());
		aeroSurfaceElement.appendChild(rangeElement);
		if (index>0) {
			for (int i = 0;i<index;i++) {
//				System.out.println("deflection number = " + number.get(i));
				flag = flag + number.get(i);
			}
			flag = flag+1 ;//+1 due to array start from 0
			String[] arrayDataMin = deflection.get(flag-1).split(";");
//			System.out.println("Delta_min = " + arrayDataMin[0]);
//			System.out.println("Delta_max = " + arrayDataMin[1]);
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
	}
	
	
	
	
	public static void writeAlileron(
			org.w3c.dom.Element rootElementChannel,List<String> deflection,
			Document doc, List<Integer> number, String controlSurface, String axis, int index) {

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
		
//		JPADStaticWriteUtils.writeSingleNode("gain",Math.toRadians(1),aeroSurfaceElementRight,doc);
		int flag = 0;
		int numberDeflection = number.get(index);

		if (index>0) {
			for (int i = 0;i<index;i++) {
				flag = flag + number.get(i);
			}
//			flag = flag-numberDeflection + 1 ;//+1 due to array start from 0
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
//		int flag = 0;
//		int numberDeflection = number.get(index);

//		System.out.println(flag);
		//			flag = flag;//+1 due to array start from 0
		String[] arrayDataMin = deflection.get(flag).split(";");
//		System.out.println(flag);
		JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],rangeElementLeft,doc);
		JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],domainElementLeft,doc);
		JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[1],rangeElement1Left,doc);

		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[0],rangeElementLeft,doc);
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[0],domainElementLeft,doc);
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[1],rangeElement1Left,doc);
		JPADStaticWriteUtils.writeSingleNode("output","fcs/left-aileron-pos-norm",aeroSurfaceNormElementLeft,doc);
	}
	
	
	public static void writeFlap(
			org.w3c.dom.Element rootElementChannel,List<String> deflection,
			Document doc, List<Integer> number, String controlSurface, int index) {

		//Normalized
		org.w3c.dom.Element aeroSurfaceNormElement = doc.createElement("aerosurface_scale");
		aeroSurfaceNormElement.setAttribute("name", "Flap Position Normalizer");
		rootElementChannel.appendChild(aeroSurfaceNormElement);
		
		
		JPADStaticWriteUtils.writeSingleNode("input","fcs/" + controlSurface + "-pos-deg",aeroSurfaceNormElement,doc);
		org.w3c.dom.Element domainElement = doc.createElement("domain");
		aeroSurfaceNormElement.appendChild(domainElement);
		org.w3c.dom.Element rangeElement1 = doc.createElement("range");
		aeroSurfaceNormElement.appendChild(rangeElement1);
		
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
		}
		else {
			String[] arrayDataMin = deflection.get(flag).split(";");
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[0],domainElement,doc);
			JPADStaticWriteUtils.writeSingleNode("min",arrayDataMin[1],rangeElement1,doc);
		}
		String[] arrayDataMax = deflection.get(flag + numberDeflection - 2 ).split(";");
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[0],domainElement,doc);
		JPADStaticWriteUtils.writeSingleNode("max",arrayDataMax[1],rangeElement1,doc);
		JPADStaticWriteUtils.writeSingleNode("output","fcs/" + controlSurface + "-pos-norm",aeroSurfaceNormElement,doc);
	}
	
	
	
	
	
	
	public static void writeAeroDataBodyAxis(Document doc, org.w3c.dom.Element outputElement,
			List<String> aeroData, int machDimension, double[] machVector,
			int reynoldsDimension, double[] reynoldsVector, String axis, org.w3c.dom.Element axisElement) {

//		org.w3c.dom.Element axisElement = doc.createElement("axis");
//		axisElement.setAttribute("name", axis);
//		rootElementAero.appendChild(axisElement);
		org.w3c.dom.Element functionElement = 
				doc.createElement("function");
		functionElement.setAttribute("name", "aero/function/"+axis+"_coeff_basic_Mach");
		axisElement.appendChild(functionElement);
		JPADStaticWriteUtils.writeSingleNode("description",
				"Lift coefficient due to alpha, beta, Reynolds, Mach" ,functionElement,doc);
		org.w3c.dom.Element productElement = doc.createElement("product");
		functionElement.appendChild(productElement);
		org.w3c.dom.Element interpElement = doc.createElement("interpolate1d");
		productElement.appendChild(interpElement);
		JPADStaticWriteUtils.writeSingleNode("p",
				"velocities/mach" ,interpElement,doc);
		int counterList = 0;
		for (int i = 0; i<machDimension; i++) {
			org.w3c.dom.Element functionElementInner = 
					doc.createElement("function");
			functionElementInner.setAttribute("name", "aero/function/"+axis+"_coeff_basic_M" + i);
			axisElement.appendChild(functionElementInner);
			JPADStaticWriteUtils.writeSingleNode("description",
					"CL = f(alpha, beta, Re) at M = " +machVector[i] ,functionElementInner,doc);
			org.w3c.dom.Element productElementInner = 
					doc.createElement("product");
			functionElementInner.appendChild(productElementInner);
			org.w3c.dom.Element tableElement = 
					doc.createElement("table");
			productElementInner.appendChild(tableElement);
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
			
			org.w3c.dom.Element valueInner = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "v", machVector[i], 
					3, 6);
			interpElement.appendChild(valueInner);
			
			org.w3c.dom.Element propertyInner = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "p", "aero/function/"+axis+"_coeff_basic_M" + i, 
					3, 6);
			interpElement.appendChild(propertyInner);
			for (int j = 0; j< reynoldsDimension;j++) {
				org.w3c.dom.Element tableElementBreakPoint = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
						doc, "tableData", aeroData.get(counterList), 
						3, 6, Tuple.of("breakPoint", String.valueOf(reynoldsVector[j])));
				tableElement.appendChild(tableElementBreakPoint);
				counterList = counterList+1;
			}
			JPADStaticWriteUtils.writeSingleNode("property","aero/function/"+axis+"_coeff_basic_M" + i,outputElement,doc);
		}

	}
	
	public static void writeAeroDataBodyAxisControlSurface(Document doc, org.w3c.dom.Element outputElement,
			List<String> aeroData, int machDimension, double[] machVector,
			int reynoldsDimension, double[] reynoldsVector, String axis, org.w3c.dom.Element axisElement, 
			double[] deflection, String controlSurfaceUID) {

		org.w3c.dom.Element functionElementTop = 
				doc.createElement("function");
		functionElementTop.setAttribute(
				"name", "aero/function/"+controlSurfaceUID+"_"+axis+"_coeff_basic_Mach_deflection");
		axisElement.appendChild(functionElementTop);
		org.w3c.dom.Element productElementTop = 
				doc.createElement("product");
		functionElementTop.appendChild(productElementTop);
		org.w3c.dom.Element interpElementTop = 
				doc.createElement("interpolate1d");
		productElementTop.appendChild(interpElementTop);
		
		if(controlSurfaceUID.equals("aileron")) {
			JPADStaticWriteUtils.writeSingleNode("p","fcs/left-"+controlSurfaceUID+"-pos-deg",interpElementTop,doc);

		}
		else {
			JPADStaticWriteUtils.writeSingleNode("p","fcs/"+controlSurfaceUID+"-pos-deg",interpElementTop,doc);
		}
		int counter = 0;
		for (int k = 0; k< deflection.length;k++) {
			JPADStaticWriteUtils.writeSingleNode("v",deflection[k],interpElementTop,doc);
			JPADStaticWriteUtils.writeSingleNode(
					"p","aero/function/"+controlSurfaceUID+"_"+axis+"_coeff_basic_Mach_deflection_"+ k 
					,interpElementTop,doc);
			org.w3c.dom.Element functionElementMid = 
					doc.createElement("function");
			functionElementMid.setAttribute(
					"name", "aero/function/"+controlSurfaceUID+"_"+axis+"_coeff_basic_Mach_deflection_"+ k);
			axisElement.appendChild(functionElementMid);
			org.w3c.dom.Element productElementMid = 
					doc.createElement("product");
			functionElementMid.appendChild(productElementMid);
			org.w3c.dom.Element interpElementMid = 
					doc.createElement("interpolate1d");
			productElementMid.appendChild(interpElementMid);
			for (int i = 0;i<machDimension;i++) {
				JPADStaticWriteUtils.writeSingleNode("p","velocities/mach",interpElementMid,doc);
				JPADStaticWriteUtils.writeSingleNode("v",machVector[i],interpElementMid,doc);
				JPADStaticWriteUtils.writeSingleNode("p",
						"aero/function/" + controlSurfaceUID + "_" + axis + "_coeff_basic_M" + i + "_deflection_" + k,
						interpElementMid,doc);

				org.w3c.dom.Element functionElementBot = 
						doc.createElement("function");
				functionElementBot.setAttribute(
						"name", "aero/function/" + controlSurfaceUID + "_" + axis + 
						"_coeff_basic_M" + i + "_deflection_" + k);
				axisElement.appendChild(functionElementBot);
				org.w3c.dom.Element productElementBot = 
						doc.createElement("product");
				functionElementBot.appendChild(productElementBot);
				org.w3c.dom.Element tableElement = 
						doc.createElement("table");
				productElementBot.appendChild(tableElement);
				
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
							doc, "tableData", aeroData.get(counter), 
							3, 6, Tuple.of("breakPoint", String.valueOf(reynoldsVector[j])));
					tableElement.appendChild(tableElementBreakPoint);
					counter = counter+1;
				}
			}
		}
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
