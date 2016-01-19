package sandbox.adm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.kohsuke.args4j.ExampleMode.ALL;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.componentmodel.Component;
import aircraft.components.Aircraft;
import cad.aircraft.MyAircraftBuilder;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;


public class MySandbox_ADM {

	
	@Option(name="-n",usage="repeat <n> times\nusage can have new lines in it and also it can be verrrrrrrrrrrrrrrrrry long")
	private int _num = -1;
	
	@Option(name = "-i", aliases = { "--input" }, required = false,
			usage = "my input file")
	private File _inputFile;

	
	public CmdLineParser theCmdLineParser;
	
	// receives other command line parameters than options
    @Argument
    private List<String> arguments = new ArrayList<String>();

	
	@SuppressWarnings("unused")
	public MySandbox_ADM() {

		// Test OccJava: Cone and curvature.
		if ( false ) {
			System.out.println("---------------------------");
			System.out.println("testing occJava (MyTest_ADM_00) ...");
			try {
				MyTest_ADM_00 myTest_ADM_00 = new MyTest_ADM_00(); 
				System.out.println("Test ADM 00 OK: build a cone and calculate curvature.");
			}
			catch (Exception exc) {
				System.out.println(exc);
				System.out.println("Test ADM 01 went wrong!");
			}
			System.out.println("... end of occJava test");
			System.out.println("---------------------------");
		}

		// Test OccJava: Trim a sphere with square and mesh it.
		if ( false ) {
			System.out.println("---------------------------");
			System.out.println("testing occJava (MyTest_ADM_01) ...");
			try {
				MyTest_ADM_01 myTest_ADM_01 = new MyTest_ADM_01(); 
				System.out.println("Test ADM 01 OK: Trim a sphere with square and mesh it.");
			}
			catch (Exception exc) {
				System.out.println(exc);
				System.out.println("Test ADM 01 went wrong!");
			}
			System.out.println("... end of occJava test");
			System.out.println("---------------------------");
		}

		// Test OccJava: Trim a sphere with square and mesh it.
		if ( false ) {
			System.out.println("---------------------------");
			System.out.println("testing occJava (MyTest_ADM_02) ...");
			try {
				MyTest_ADM_02 myTest_ADM_02 = new MyTest_ADM_02(); 
				System.out.println("Test ADM 02 OK: Make a NURBS surface.");
			}
			catch (Exception exc) {
				System.out.println(exc);
				System.out.println("Test ADM 02 went wrong!");
			}
			System.out.println("... end of occJava test");
			System.out.println("---------------------------");
		}
		
		// Test OccJava: Splene, loft, intersections ...
		if ( false ) {
			System.out.println("---------------------------");
			System.out.println("occJava test ...");

			System.out.println("testing occJava (MyTest_ADM_03a) ...");
			try {
				MyTest_ADM_03aSplines myTest_ADM_03a = new MyTest_ADM_03aSplines(); 
				System.out.println("Test ADM 03a OK: Make a spline, loft, etc.");
			}
			catch (Exception exc) {
				System.out.println(exc);
				System.out.println("Test ADM 03a went wrong!");
			}

			System.out.println("testing occJava (MyTest_ADM_03b) ...");
			MyTest_ADM_03bCAD myTest_ADM_03b = new MyTest_ADM_03bCAD(); 

			System.out.println("testing occJava (MyTest_ADM_03c) ...");
			MyTest_ADM_03cCAD myTest_ADM_03c = new MyTest_ADM_03cCAD(); 
		}

		// Test OccJava: Spline, loft, intersections ...
		if ( true ) {
			System.out.println("---------------------------");
			System.out.println("occJava test ...");
			System.out.println("testing occJava (MyTest_ADM_03dCAD) ...");

			Aircraft theAircraft = new Aircraft(
					ComponentEnum.FUSELAGE, 
					ComponentEnum.WING,
					ComponentEnum.HORIZONTAL_TAIL,
					ComponentEnum.VERTICAL_TAIL,
					ComponentEnum.POWER_PLANT,
					ComponentEnum.NACELLE,
					ComponentEnum.LANDING_GEAR
					);

			MyTest_ADM_03dCAD myTest_ADM_03d = new MyTest_ADM_03dCAD(theAircraft); 

			
/*
			System.out.println("Wetted area: " + myTest_ADM_03d.get_wettedArea());

			//		myTest_ADM_03d.write(
			//				new File(MyWriteUtils._CurrentDirectory.getAbsolutePath()+"/test"), 
			//				"test03d_fuselage.igs");

			myTest_ADM_03d.write(
					new File(MyWriteUtils._CurrentDirectory.getAbsolutePath()+"/test"), 
					"test03d_fuselage.brep");
			myTest_ADM_03d.write(
					new File(MyWriteUtils._CurrentDirectory.getAbsolutePath()+"/test"), 
					"test03d_fuselage.stl");
*/
			System.out.println("... end of occJava test");
			System.out.println("---------------------------");

		}		
		
		////////////////////////////////////////////////////////////////////////////////////////////

		// Test HDF features
//		System.out.println("---------------------------");
//		System.out.println("HDF5 test ...");
//		try {
//			System.out.println("MyTest_ADM_05a ...");
//			MyTest_ADM_05a myTest_ADM_05a = new MyTest_ADM_05a(); 
//		}
//		catch (Exception exc) {
//			System.out.println(exc);
//			System.out.println("Test ADM 05a went wrong!");
//		}
// ok

//		try {
//			System.out.println("MyTest_ADM_05b ...");
//			MyTest_ADM_05b myTest_ADM_05b = new MyTest_ADM_05b(); 
//		}
//		catch (Exception exc) {
//			System.out.println(exc);
//			System.out.println("Test ADM 05b went wrong!");
//		}
 // ok
//
//		try {
//			System.out.println("MyTest_ADM_05c ...");
//			MyTest_ADM_05c_HDF_Interpolation myTest_ADM_05c = new MyTest_ADM_05c_HDF_Interpolation(); 
//		}
//		catch (Exception exc) {
//			System.out.println(exc);
//			System.out.println("Test ADM 05c went wrong!");
//		}
//		
//		System.out.println("... end of HDF5 test");
//		System.out.println("---------------------------");
// ok
		////////////////////////////////////////////////////////////////////////////////////////////
		
		theCmdLineParser = new CmdLineParser(this);
		
	}// end-of constructor
	
	
	
	public static void main(String[] args) throws IOException {
		
		System.out.println("MySandbox_ADM :: main ");
		
		// Test OccJava, compiled with MS Visual Studio Community Ed., OpenCASCADE Community Ed. 6.8.0 (March 2015)
		
		MySandbox_ADM theObj = new MySandbox_ADM();
		
		// theObj.theCmdLineParser.printUsage(System.out);
		
		theObj.manageCmdLineArguments(args);
		
		// System.out.println("The input file: " + theObj.get_inputFile());
		// System.out.println("The num: " + theObj.get_num());
		
	}

	public void manageCmdLineArguments(String[] args) {

		try {
            // parse the arguments.
			theCmdLineParser.parseArgument(args);

            // you can parse additional arguments if you want.
            // parser.parseArgument("more","args");

            // after parsing arguments, you should check
            // if enough arguments are given.
			/*
            if( arguments.isEmpty() )
                throw new CmdLineException(theCmdLineParser,"No argument is given!!!");
            */

        } catch( CmdLineException e ) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.println("java MySandbox_ADM [options...] arguments...");
            // print the list of available options
            theCmdLineParser.printUsage(System.err);
            System.err.println();

            // print option sample. This is useful some time
            System.err.println("  Example: java SampleMain"+theCmdLineParser.printExample(ALL));

            return;        	
        }

	}
	
	public int get_num() {
		return _num;
	}

	public void set_num(int n) {
		this._num = n;
	}

	public File get_inputFile() {
		return _inputFile;
	}

	public void set_inputFile(File f) {
		this._inputFile = f;
	}
	

}// end-of class

//System.out.println("TIGL test ...");
//
//TIGLInterface tiglInterface = new TIGLInterface();
//
//String cpacsFileName = "pippo_cpacs.xml";
//String configurationUID = "Cpacs2Test";
//if ( tiglInterface.tiglOpenCPACSConfigurationFromDisk(cpacsFileName, configurationUID) )
//{
//	System.out.println("CPACS file " + cpacsFileName + "read.");
//	System.out.println("Configuration ID **" + configurationUID + "** read.");
//}
//else
//{
//	System.out.println("Cannot read CPACS file " + cpacsFileName + " .");
//}
//
//System.out.println("... end of TIGL test");


////////////////////////////////////////////////////////////////////////////////////////////



//Test Jython
//import org.python.core.PyException;
//import org.python.core.PyInteger;
//import org.python.core.PyObject;
//import org.python.util.PythonInterpreter;

//PythonInterpreter interp = new PythonInterpreter();
//interp.exec("import sys");
//interp.exec("print sys");
//interp.set("a", new PyInteger(42));
//interp.exec("print a");
//interp.exec("x = 2+2");
//PyObject x = interp.get("x");
//System.out.println("x: " + x);

