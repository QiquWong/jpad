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
import standaloneutils.CPACSReader;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
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
					wingIndex = cpacsReader.getWingIndex(elementWing.getAttribute("uID"));
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
				
				String pathToIxx = "//toolspecific/UNINA_modules/JSBSim_data/mass_balance/ixx";
				System.out.println("--> " + pathToIxx);
				Double ixx = Double.valueOf(
						cpacsReader.getJpadXmlReader()
						.getXMLPropertyByPath(pathToIxx)
						);
				String unitIxx = cpacsReader.getJpadXmlReader()
						.getXMLAttributeByPath(pathToIxx,"unit");
				System.out.println("----> " + ixx + " " + unitIxx);
				
				System.out.println("--------------------------------");
				
				String cpacsOutputFileFolderPath = 
						MyConfiguration.currentDirectoryString + File.separator
						+ MyConfiguration.outputDirectory + File.separator
						+ "CPACS";
				String cpacsOutputFilePath = cpacsOutputFileFolderPath + File.separator + "pippo_cpacs.xml";
				
				JSBSimModel jsbsimModel = new JSBSimModel(cpacsReader);
				jsbsimModel.appendToCPACSFile(new File(cpacsOutputFileFolderPath)); // TODO
				
				//table system
				JPADXmlReader _jpadXmlReader = new JPADXmlReader(cpacsFilePath);
				Document _importDoc = _jpadXmlReader.getXmlDoc();
//				String pathInTheCpacs = "cpacs/vehicles/aircraft/model/systems/controlDistributors";
				List<String> ControlSurfaceUIDList = new ArrayList<>();
				DefaultTableModel tableModel =  new DefaultTableModel();
				NodeList SystemList = MyXMLReaderUtils.getXMLNodeListByPath(
						_jpadXmlReader.getXmlDoc(),"cpacs/vehicles/aircraft/model/systems/controlDistributors/controlDistributor");	
				System.out.println(SystemList.getLength());
				for (int i = 0;i<SystemList.getLength();i++) {
					Node nodeSystem  = SystemList.item(i); // .getNodeValue();
					Element SystemElement = (Element) nodeSystem;
					ControlSurfaceUIDList.add( SystemElement.getAttribute("uID"));
		            System.out.println("System[" + i + "] --> uid: " + SystemElement.getAttribute("uID"));
				}
				System.out.println("--------------------------------------------------");
				System.out.println(ControlSurfaceUIDList.get(0));
				System.out.println("--------------------------------------------------");
				String UIDControlSurface;
		        List<String> ReturnSystem = new ArrayList<String>();
				for (int i=1;i<SystemList.getLength()+1;i++) {
					UIDControlSurface = ControlSurfaceUIDList.get(i-1);

					String InputSystem = _jpadXmlReader.getXMLPropertyByPath(
							"cpacs/vehicles/aircraft/model/systems/controlDistributors"
							+ "/controlDistributor["+i+"]/controlElements/controlElement/commandInputs");
					String RelativeDeflection = _jpadXmlReader.getXMLPropertyByPath(
							"cpacs/vehicles/aircraft/model/systems/controlDistributors"
							+ "/controlDistributor["+i+"]/controlElements/controlElement/relativeDeflection");
					ReturnSystem.add(UIDControlSurface);
					ReturnSystem.add(InputSystem);					
					ReturnSystem.add(RelativeDeflection);
					System.out.println("--------------------------------------------------");
					System.out.println("TableData = "+InputSystem);
					System.out.println("--------------------------------------------------");
				}
//				JTable table = new JTable(tableModel);
				System.out.println("--------------------------------------------------");
				System.out.println(ReturnSystem.size());
				System.out.println("--------------------------------------------------");
				
		        List<Double> HeightGroundEffect = _jpadXmlReader.readArrayDoubleFromXMLSplit(
						"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/Height");
		        List<Double> KCLGroundEffect = _jpadXmlReader.readArrayDoubleFromXMLSplit(
						"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/KCL");
		        List<Double> KCDGroundEffect = _jpadXmlReader.readArrayDoubleFromXMLSplit(
						"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/KCD");
				double[][] KCLMatrix = new double [KCLGroundEffect.size()][2];
				double[][] KCDMatrix = new double [KCLGroundEffect.size()][2];
				for (int i=0;i<KCLGroundEffect.size();i++) {
					KCLMatrix[i][0] = HeightGroundEffect.get(i);
					KCLMatrix[i][1] = KCLGroundEffect.get(i);
					KCDMatrix[i][0] = HeightGroundEffect.get(i);
					KCDMatrix[i][1] = KCDGroundEffect.get(i);
				}
				String Pippo = cpacsReader.MatrixDoubleToTable1Variables(KCLMatrix);
				System.out.println(Pippo);
//				List<Double> TableArray1 = cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
//						"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/Drag/CD0");
//				List<Double> TableArray2 = cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
//						"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/Drag/Alpha");
////				List<Double> TableArray3;
////				for (int i = 0;i<TableArray1.size();i++) {
////					
////				}
//				
//				List<Double> FlightLevelEngine = cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
//						"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/flightLevel");
//				List<Double> MachNumberEngine = cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
//						"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/machNumber");
//				List<Double> IdleThrust = cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
//						"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/IdleThrust");
//				List<Double> MilThrust = cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
//						"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/MilThrust");
//				double [][] IdleThrustTable = new double [MachNumberEngine.size()+1][FlightLevelEngine.size()+1];
//				int k = 0;
//				for(int i= 0;i<MachNumberEngine.size()+1;i++) {
//					for (int j = 0;j<FlightLevelEngine.size()+1;j++) {
////						System.out.println(k);
//						if (i==0&&j==0) {
//							IdleThrustTable[i][j]=0;
//						}
//						if (j==0 && i !=0 ){
//							IdleThrustTable[i][j]=MachNumberEngine.get(i-1);
//						}
//						if (i==0 && j !=0 ){
//							IdleThrustTable[i][j]=FlightLevelEngine.get(j-1);
//						}
//						else {
//							IdleThrustTable[i][j]=IdleThrust.get(k);
////							System.out.println(IdleThrust.get(k));
//							if (k<FlightLevelEngine.size()*MachNumberEngine.size()-1) {
//							k++;							
//							}
//						}
//
//					}						
//				}
//				String TableString = cpacsReader.toString(IdleThrustTable, ";");
//				System.out.println(TableString);
//				for(int i= 0;i<MachNumberEngine.size()+1;i++) {
//					for (int j = 0;j<FlightLevelEngine.size()+1;j++) {
//						System.out.println(IdleThrustTable[i][j]);
//					}						
//				}
//				System.out.println(IdleThrustTable);
				
			} // status OK
			
		} catch (CmdLineException e) {
			System.err.println("A problem occurred with command line arguments!");
			e.printStackTrace();
		}

		
		
	}// end-of-main

}
