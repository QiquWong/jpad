package sandbox2.gt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import configuration.MyConfiguration;
import de.dlr.sc.tigl.Tigl;
import de.dlr.sc.tigl.TiglException;
import javaslang.Tuple;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.cpacs.CPACSReader;
import standaloneutils.cpacs.CPACSUtils;
import standaloneutils.jsbsim.JSBSimModel;
import writers.JPADStaticWriteUtils;

class ArgumentsCPACSReaderTest2 {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "Cpacs file")
	private File _inputFile;
	
	@Option(name = "-o", aliases = { "--output" }, required = false,
			usage = "Jsbsim file")
	private File _outputFile;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}
	public File getOutputFile() {
		return _outputFile;
	}

}


public class CPACSReaderTest2 {
	String dirPath = null;

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	
	public static void main(String[] args) throws TiglException, IOException, ParserConfigurationException, InterruptedException {
		
		System.out.println("CPACSReader test");
		System.out.println("--------------------------------");

		ArgumentsCPACSReaderTest2 va = new ArgumentsCPACSReaderTest2();
		CPACSReaderTest1.theCmdLineParser = new CmdLineParser(va);

		try {
			CPACSReaderTest1.theCmdLineParser.parseArgument(args);
			String cpacsFilePath = va.getInputFile().getAbsolutePath();
			String dirPath = va.getInputFile().getParent();
			System.out.println("TiGL Version: " + Tigl.getVersion());
			System.out.println("--------------------------------");
			
			// create the main object
			CPACSReader cpacsReader = new CPACSReader(cpacsFilePath);
			
			if (cpacsReader.getStatus() == CPACSReader.ReadStatus.OK) {
				
				System.out.println("--------------------------------");
				System.out.println("Getting the list of wings");

				NodeList wingsNodes = cpacsReader.getWingList();
				int wingIndex = 0;
				System.out.println("[JPAD] Number of wings: " + wingsNodes.getLength());
				for (int i = 0; i < wingsNodes.getLength(); i++) {
					Node nodeWing  = wingsNodes.item(i); // .getNodeValue();
					Element elementWing = (Element) nodeWing;
					wingIndex = cpacsReader.getWingIndexZeroBased(elementWing.getAttribute("uID"));
					System.out.println("WingIndex = "+ wingIndex);
		            System.out.println("wing[" + i + "] --> uid: " + elementWing.getAttribute("uID"));
				}
				System.out.println("--------------------------------");

				System.out.println("Getting a given tag content");
				
				String pathToMainWingUID = "/cpacs/toolspecific/UNINA_modules/input/wings/MainWing/mainWingUID";
				System.out.println("--> " + pathToMainWingUID);

				String flagwing = cpacsReader.getJpadXmlReader()
						.getXMLPropertyByPath(pathToMainWingUID);
				System.out.println("----> " + flagwing);
				
				System.out.println("--------------------------------");
			
				

				System.out.println("--------------------------------");
				
				System.out.println("Getting a given tag content + attribute");
				
//				String pathToIxx = "//toolspecific/UNINA_modules/JSBSim_data/mass_balance/ixx";
//				System.out.println("--> " + pathToIxx);
//				Double ixx = Double.valueOf(
//						cpacsReader.getJpadXmlReader()
//						.getXMLPropertyByPath(pathToIxx)
//						);
//				String unitIxx = cpacsReader.getJpadXmlReader()
//						.getXMLAttributeByPath(pathToIxx,"unit");
//				System.out.println("----> " + ixx + " " + unitIxx);
//				
//				System.out.println("--------------------------------");
				
				String cpacsOutputFileFolderPath = 
						MyConfiguration.currentDirectoryString + File.separator
						+ MyConfiguration.outputDirectory + File.separator
						+ "CPACS";
				String jsbsimOutputFilePath = cpacsOutputFileFolderPath + File.separator + "pippo_cpacs.xml";
				
				JSBSimModel jsbsimModel = new JSBSimModel(cpacsReader);
//				jsbsimModel.appendToCPACSFile(new File(cpacsOutputFileFolderPath)); // TODO
				jsbsimModel.readVariablesFromCPACS(); // TODO
//				jsbsimModel.exportToXML(new File(jsbsimOutputFilePath)); // TODO
				
				//table system
				JPADXmlReader _jpadXmlReader = new JPADXmlReader(cpacsFilePath);
				Document _importDoc = _jpadXmlReader.getXmlDoc();

				
//				List<Double> clVector = cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
//						"cpacs/vehicles/aircraft/model/analyses/aeroPerformanceMap/cfx");
//				System.out.println("--------------------------------");
//				System.out.println("Size is = "+clVector.size());
//				System.out.println("--------------------------------");
//				
//				
//				List<Double> clSurfaceVector = cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
//						"cpacs/vehicles/aircraft/model/analyses/aeroPerformanceMap/controlSurfaces/controlSurface/dcmz");
//				System.out.println("--------------------------------");
//				System.out.println("Size is = "+clSurfaceVector.size());
//				System.out.println("--------------------------------");
				
				//Generate script
				DocumentBuilderFactory docScriptFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docScriptBuilder;
				Document docScript = null;
				docScriptBuilder = docScriptFactory.newDocumentBuilder();
				docScript = docScriptBuilder.newDocument();
				String scriptName = "uninaProva.xml";
				String scriptPath = dirPath+"/scripts/"+scriptName;
				org.w3c.dom.Element runscriptElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
						docScript,"runscript",
						Tuple.of("name", "D150_JSBSim"), // TODO: get aircraft name from _cpaceReader
						Tuple.of("initialize", "initialCondition")
						);
				docScript.appendChild(runscriptElement);
				JPADStaticWriteUtils.writeSingleNode("decription",
						"CPACS simulation with JSBSim through JPAD software",runscriptElement,docScript);
				org.w3c.dom.Element useElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
						docScript,"use",
						Tuple.of("aircraft", "D150_JSBSim"), // TODO: get aircraft name from _cpaceReader
						Tuple.of("initialize", "initialCondition")
						);
				runscriptElement.appendChild(useElement);
				org.w3c.dom.Element runElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
						docScript,"run",
						Tuple.of("start", "0"), // TODO: get aircraft name from _cpaceReader
						Tuple.of("end", "1000"),
						Tuple.of("dt", "0.01")
						);
				runscriptElement.appendChild(runElement);
				JPADStaticWriteUtils.writeSingleNode("property","simulation/notify-time-trigger",runElement,docScript);
				org.w3c.dom.Element propertyElem = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
						docScript, "property", "simulation/run_id", 
						3, 6, Tuple.of("value", "1"));
				runElement.appendChild(propertyElem);
				propertyElem = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
						docScript, "property", "fcs/left-brake-cmd-norm", 
						3, 6, Tuple.of("value", "1"));
				runElement.appendChild(propertyElem);
				propertyElem = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
						docScript, "property", "fcs/right-brake-cmd-norm", 
						3, 6, Tuple.of("value", "1"));
				runElement.appendChild(propertyElem);
				//Start Element
				org.w3c.dom.Element eventElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
						docScript,"event",
						Tuple.of("name", "engine start") // TODO: get aircraft name from _cpaceReader
						);
				runElement.appendChild(eventElem);	
				JPADStaticWriteUtils.writeSingleNode("description","Start the engine",eventElem,docScript);
				JPADStaticWriteUtils.writeSingleNode("condition"," simulation/sim-time-sec >= 0.2",eventElem,docScript);
				org.w3c.dom.Element setElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
						docScript,"set",
						Tuple.of("name", "fcs/throttle-cmd-norm"), 
						Tuple.of("value", "1.0"),
						Tuple.of("action", "FG_RAMP"),
						Tuple.of("tc", "0.5"));
				eventElem.appendChild(setElem);	
				setElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
						docScript,"set",
						Tuple.of("name", "fcs/mixture-cmd-norm"), 
						Tuple.of("value", "1.0"),
						Tuple.of("action", "FG_RAMP"),
						Tuple.of("tc", "0.5"));
				eventElem.appendChild(setElem);	
//				setElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
//						docScript,"set",
//						Tuple.of("name", "propulsion/magneto_cmd"), 
//						Tuple.of("value", "3.0"));
//				eventElem.appendChild(setElem);	
//				setElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
//						docScript,"set",
//						Tuple.of("name", "propulsion/starter_cmd"), 
//						Tuple.of("value", "1.0"));
//				eventElem.appendChild(setElem);	
				org.w3c.dom.Element notifyElem = docScript.createElement("notify");
				eventElem.appendChild(notifyElem);
				JPADStaticWriteUtils.writeSingleNode("property"," position/h-agl-ft",notifyElem,docScript);
				JPADStaticWriteUtils.writeSingleNode("property"," velocities/vc-kts",notifyElem,docScript);
				
				// 2nd event
				eventElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
						docScript,"event",
						Tuple.of("name", "begin roll") // TODO: get aircraft name from _cpaceReader
						);
				runElement.appendChild(eventElem);	
				JPADStaticWriteUtils.writeSingleNode("description","Release brakes and get rolling with flaps at 30 degrees.",eventElem,docScript);
				JPADStaticWriteUtils.writeSingleNode("condition"," simulation/sim-time-sec >= 2.5",eventElem,docScript);
				setElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
						docScript,"set",
						Tuple.of("name", "fcs/left-brake-cmd-norm"), 
						Tuple.of("value", "0"));
				eventElem.appendChild(setElem);	
				setElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
						docScript,"set",
						Tuple.of("name", "fcs/right-brake-cmd-norm"), 
						Tuple.of("value", "0"));
				eventElem.appendChild(setElem);	
				setElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
						docScript,"set",
						Tuple.of("name", "fcs/flap-cmd-norm"), 
						Tuple.of("value", "0.66"));
				eventElem.appendChild(setElem);	
				notifyElem = docScript.createElement("notify");
				eventElem.appendChild(notifyElem);
				JPADStaticWriteUtils.writeSingleNode("property"," position/h-agl-ft",notifyElem,docScript);
				JPADStaticWriteUtils.writeSingleNode("property"," velocities/vc-kts",notifyElem,docScript);
				
				// 3rd event
				eventElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
						docScript,"event",
						Tuple.of("name", "Remove flap") // TODO: get aircraft name from _cpaceReader
						);
				runElement.appendChild(eventElem);	
				JPADStaticWriteUtils.writeSingleNode("description","at 1000 feet remove flap",eventElem,docScript);
				JPADStaticWriteUtils.writeSingleNode("condition"," position/h-agl-ft >= 1000",eventElem,docScript);
				setElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
						docScript,"set",
						Tuple.of("name", "fcs/flap-cmd-norm"), 
						Tuple.of("value", "0.0"));
				eventElem.appendChild(setElem);	
				notifyElem = docScript.createElement("notify");
				eventElem.appendChild(notifyElem);
				JPADStaticWriteUtils.writeSingleNode("property"," position/h-agl-ft",notifyElem,docScript);
				JPADStaticWriteUtils.writeSingleNode("property"," velocities/vc-kts",notifyElem,docScript);
				JPADStaticWriteUtils.writeDocumentToXml(docScript, scriptPath);
				try {
					// export to JSBSim format
					String outputFile = va.getOutputFile().getAbsolutePath();
					String outputDirPath = va.getOutputFile().getParent();

					jsbsimModel.exportToXML(outputFile,dirPath);
					//Initial condition are given as double value
					double ubody = 0.0;
					double vbody = 0.0;
					double wbody = 0.0;
					double longitude = 20.0;
					double latitude = 30.0;
					double phi = 0.0;
					double theta = 1.0;
					double psi = 30.0;
					double altitude = 2.5;
					double elevation = 2.0;
					double hwind = 0.0;
					String dirIC = outputDirPath+"/initialCondition.xml";
					jsbsimModel.initialize(dirIC, ubody, vbody, wbody,
							longitude, latitude, phi, theta, psi, altitude, elevation, hwind);
					jsbsimModel.startJSBSimSimulation(dirPath,scriptName);
				}
				catch (NullPointerException e) {
					System.err.println("Output file not givem");
				}
			} // status OK
			
		} catch (CmdLineException e) {
			System.err.println("A problem occurred with command line arguments!");
			e.printStackTrace();
		}

		
		
	}// end-of-main

}
