package sandbox2.gt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

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
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.cpacs.CPACSReader;
import standaloneutils.cpacs.CPACSUtils;
import standaloneutils.jsbsim.JSBSimModel;

class ArgumentsCPACSReaderTest2 {
	@Option(name = "-f", aliases = { "--file" }, required = true,
			usage = "my input file")
	private File _inputFile;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}

}


public class CPACSReaderTest2 {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	
	public static void main(String[] args) throws TiglException {
		
		System.out.println("CPACSReader test");
		System.out.println("--------------------------------");

		ArgumentsCPACSReaderTest1 va = new ArgumentsCPACSReaderTest1();
		CPACSReaderTest1.theCmdLineParser = new CmdLineParser(va);

		try {
			CPACSReaderTest1.theCmdLineParser.parseArgument(args);
			String cpacsFilePath = va.getInputFile().getAbsolutePath();

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

				
				List<Double> clVector = cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
						"cpacs/vehicles/aircraft/model/analyses/aeroPerformanceMap/cfx");
				System.out.println("--------------------------------");
				System.out.println("Size is = "+clVector.size());
				System.out.println("--------------------------------");
				
				
				List<Double> clSurfaceVector = cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
						"cpacs/vehicles/aircraft/model/analyses/aeroPerformanceMap/controlSurfaces/controlSurface/dcmz");
				System.out.println("--------------------------------");
				System.out.println("Size is = "+clSurfaceVector.size());
				System.out.println("--------------------------------");
				
				double[][] systemControl = new double[2][3];
				systemControl = cpacsReader.getControlSurfaceRotation(0, "D150_VAMP_W1_CompSeg1_innerFlap", "flapSettingDistributor");
				System.out.println(systemControl);
				
				
				
			} // status OK
			
		} catch (CmdLineException e) {
			System.err.println("A problem occurred with command line arguments!");
			e.printStackTrace();
		}

		
		
	}// end-of-main

}
