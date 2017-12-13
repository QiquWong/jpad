package sandbox2.adm.cpacs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;
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
import sandbox2.mds.Test4;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.cpacs.CPACSReader;
import standaloneutils.cpacs.CPACSUtils;
import standaloneutils.jsbsim.JSBSimModel;
import standaloneutils.jsbsim.JSBSimScriptsTemplateEnums;
import writers.JPADStaticWriteUtils;

class ArgumentsCPACSReaderTest2a {
	@Option(name = "-cpacs", aliases = { "--cpacs-input" }, required = true,
			usage = "CPACS file")
	private File _cpacsFile;
	
	@Option(name = "-fdm", aliases = { "--jsbsim-fdm" }, required = false,
			usage = "JSBSim FDM file")
	private File _jsbsimFDMFile;

	@Option(name = "-script", aliases = { "--jsbsim-script" }, required = false,
			usage = "JSBSim script file")
	private File _jsbsimScriptFile;

	@Option(name = "-ic", aliases = { "--jsbsim-init" }, required = false,
			usage = "JSBSim initialization file (only name, no full path; placed besides fdm file)")
	private File _jsbsimInitFile;
	
	@Option(name = "-exe", aliases = { "--jsbsim-exec" }, required = false,
			usage = "JSBSim executable file")
	private File _jsbsimExecFile;

	@Option(name = "-ns", aliases = { "--no-sim" }, required = false,
			usage = "Jsbsim file")
	private boolean _noSim = false;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getCPACSFile() {
		return _cpacsFile;
	}
	public File getJSBSimScriptFile() {
		return _jsbsimScriptFile;
	}
	public File getJSBSimExecFile() {
		return _jsbsimExecFile;
	}	
	public File getJSBSimFDMFile() {
		return _jsbsimFDMFile;
	}
	public File getJSBSimInitFile() {
		return _jsbsimInitFile;
	}
	public boolean isNoSim() {
		return _noSim;
	}

}


public class CPACSReaderTest2a {

	public static void main(String[] args) throws TiglException, IOException, ParserConfigurationException, InterruptedException {
		
		CmdLineParser theCmdLineParser;
		ArgumentsCPACSReaderTest2a va = new ArgumentsCPACSReaderTest2a();
		theCmdLineParser = new CmdLineParser(va);
		
		System.out.println("--------------------------------");
		System.out.println("CPACSReader test");
		System.out.println("--------------------------------");

		try {
			System.out.println("TiGL Version: " + Tigl.getVersion());
			System.out.println("--------------------------------");

			theCmdLineParser.parseArgument(args);
			String cpacsFilePath = va.getCPACSFile().getAbsolutePath();
			String cpacsRootDirPath = 
					// va.getCPACSFile().getParent();
					FilenameUtils.getFullPathNoEndSeparator(cpacsFilePath);
			
			// create the main object
			CPACSReader cpacsReader = new CPACSReader(cpacsFilePath);
			
			if (cpacsReader.getStatus() == CPACSReader.ReadStatus.OK) {
				
				//================================================================================
				// manage JSBSim root, aircraft name, etc
				
				String jsbsimRootPath = cpacsRootDirPath;
				String jsbsimAircraftName = null;
				String jsbsimFDMFilePath = null;
				String jsbsimFDMDirPath = null;
				
				if (va.getJSBSimFDMFile() != null) {
					jsbsimFDMFilePath = va.getJSBSimFDMFile().getAbsolutePath();
					jsbsimFDMDirPath = va.getJSBSimFDMFile().getParent();
					jsbsimAircraftName = FilenameUtils.getBaseName(jsbsimFDMFilePath);
				} else {
					// no output directive given by user
					// assign a JSBSim aircraft name according to CPACS file name
					jsbsimAircraftName = FilenameUtils.getBaseName(cpacsFilePath) + "_agodemar";
					jsbsimFDMDirPath = jsbsimRootPath + File.separator 
							+ "aircraft" + File.separator + jsbsimAircraftName;
					jsbsimFDMFilePath = jsbsimFDMDirPath + File.separator + jsbsimAircraftName + ".xml";
				}
				
				//================================================================================
				// manage JSBSim executable
				String jsbsimExecPath = null;
				if (va.getJSBSimExecFile() != null) {
					jsbsimExecPath = va.getJSBSimExecFile().getAbsolutePath();
					// TODO: check the existence of JSBSim.exe
					
				} else {
					// TODO: revert to some default path
					
				}
					
				//================================================================================
				// the main object
				JSBSimModel jsbsimModel = new JSBSimModel(cpacsReader);
				// parsing the CPACS
				jsbsimModel.readVariablesFromCPACS();
				
				//================================================================================
				// manage script generation settings
				String jsbsimScriptPath = null;
				String jsbsimScriptDirPath = null;
				if (va.getJSBSimScriptFile() != null) {
					jsbsimScriptPath = va.getJSBSimScriptFile().getAbsolutePath();
					jsbsimScriptDirPath = va.getJSBSimScriptFile().getParent();
				} else {
					// revert to default a path
					jsbsimScriptDirPath = cpacsRootDirPath + File.separator + "scripts";
					jsbsimScriptPath = jsbsimScriptDirPath + File.separator + "uninaTest_agodemar.xml";
				}

				//================================================================================
				// manage JSBSim init file
				String jsbsimInitFileBaseName = "initialCondition";
				String jsbsimInitDirPath = jsbsimFDMDirPath;
				String jsbsimInitFilePath = jsbsimInitDirPath + File.separator + jsbsimInitFileBaseName + ".xml";
				
				if (va.getJSBSimInitFile() != null) {
					jsbsimInitFilePath = va.getJSBSimScriptFile().getName();
					jsbsimInitFileBaseName = FilenameUtils.getBaseName(jsbsimInitFilePath);
				}
				
				//================================================================================
				// manage JSBSim script file
				jsbsimModel.writeScriptFile(jsbsimScriptPath, jsbsimAircraftName, jsbsimInitFileBaseName, JSBSimScriptsTemplateEnums.TAKEOFF);
				
				System.out.println("-----------------------------------------------------------");
				System.out.println("CPACS input: " + cpacsFilePath);
				System.out.println("CPACS root dir: " + cpacsRootDirPath);
				System.out.println("JSBSim root: " + jsbsimRootPath);
				System.out.println("JSBSim aircraft name: " + jsbsimAircraftName);
				System.out.println("JSBSim fdm dir: " + jsbsimFDMDirPath);
				System.out.println("JSBSim fdm file: " + jsbsimFDMFilePath);
				System.out.println("JSBSim script file: " + jsbsimScriptPath);
				System.out.println("JSBSim script dir: " + jsbsimScriptDirPath);
				System.out.println("JSBSim script file name: " + FilenameUtils.getName(jsbsimScriptPath));
				System.out.println("JSBSim init file: " + jsbsimInitFilePath);
				System.out.println("JSBSim init file base name: " + jsbsimInitFileBaseName);
				
				System.out.println("-----------------------------------------------------------");
				// System.exit(1);
				
				try {
					
					//================================================================================
					// export to JSBSim FDM file
					jsbsimModel.exportToXML(jsbsimFDMFilePath, cpacsRootDirPath);
					
					//================================================================================
					// Define and export Initial conditions
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
					jsbsimModel.writeInitialConditionsFile(jsbsimInitFilePath, 
							ubody, vbody, wbody,
							longitude, latitude, 
							phi, theta, psi, 
							altitude, elevation, hwind);
					
					if (!va.isNoSim())
						jsbsimModel.startJSBSimSimulation(
								cpacsRootDirPath, 
								FilenameUtils.getName(jsbsimScriptPath) // TODO: check --script=...
								);
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
