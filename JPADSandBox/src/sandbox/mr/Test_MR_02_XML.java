//Version 1, without unit of measure

package sandbox.mr;


import static org.kohsuke.args4j.ExampleMode.ALL;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import standaloneutils.JPADXmlReader;

//the input file is given as argument of main

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;


public class Test_MR_02_XML {
	
	
	@Option(name = "-f", aliases = { "--inputname" }, required = false,
			usage = "my input file name")
	private String _inputFileName;
	
	@Option(name = "-i", aliases = { "--input" }, required = false,
			usage = "my input file")
	private File _inputFile;
	
	public CmdLineParser theCmdLineParser; 
	
	@Argument
    private List<String> arguments = new ArrayList<String>();
	
	
	// The class constructor
		public Test_MR_02_XML() {
			
			theCmdLineParser = new CmdLineParser(this);
			
		}
	    
	
	public static void main(String[] args) {
		
		System.out.println("XML read test ...");
		
		Test_MR_02_XML theObj = new Test_MR_02_XML();
		
		theObj.theCmdLineParser.printUsage(System.out);
		theObj.manageCmdLineArguments(args);
		
		//System.out.println("user.dir: " + System.getProperty("user.dir"));
		
		//String inputFileFullPath = MyConfiguration.currentDirectoryString + File.separator + theObj.get_inputFile().getName();

		String xmlFilePath = theObj.get_inputFile().getAbsolutePath(); 
		
		System.out.println("INPUT FILE, full path: " + xmlFilePath);
		System.out.println("The input file name: " + theObj.get_inputFileName());
	
	
		
		//--------------------------------------------------------------
				// Use file via MyXMLReader class

				JPADXmlReader xmlReader = new JPADXmlReader(xmlFilePath);

				System.out.println("[main] Now search test");
				
				List<String> propertyNamesSpan = new ArrayList<>();
				List<String> propertyNamesSurface = new ArrayList<>();
				String expression = "";
		
		// search via getXMLPropertiesByPath wing span
				expression = "//span";
				propertyNamesSpan = xmlReader.getXMLPropertiesByPath(expression);
				//System.out.println("[main]\t expression: \"" + expression + "\"");
				System.out.println("Wing Span:");
				System.out.println("[main]\t result: " + propertyNamesSpan);
				
	   // search via getXMLPropertiesByPath wing surface
				expression = "//area";
				propertyNamesSurface = xmlReader.getXMLPropertiesByPath(expression);
				//System.out.println("[main]\t expression: \"" + expression + "\"");
				System.out.println("Wing Area:");
				System.out.println("[main]\t result: " + propertyNamesSurface);		
		
      // evaluate aspect ratio
				
				double[] aspectRatio = new double[propertyNamesSpan.size()];
		
			
				System.out.println("aspect ratio:");
				for (int i = 0; i <= (propertyNamesSpan.size() -1); i++){
					
					
					
					//aspectRatio[i]= Math.pow(Double.parseDouble(propertyNamesSpan.get(i)),Double.parseDouble(propertyNamesSpan.get(i)))/Double.parseDouble(propertyNamesSurface.get(i));
					aspectRatio[i]= (Double.parseDouble(propertyNamesSpan.get(i))*Double.parseDouble(propertyNamesSpan.get(i)))/Double.parseDouble(propertyNamesSurface.get(i));
					
					System.out.println("Wing number " + (i+1)+ " = " + aspectRatio[i] );
				}
				
				
				
				}
					
	
	
	public void manageCmdLineArguments(String[] args) {

		try {
            // parse the arguments.
			theCmdLineParser.parseArgument(args);
			
            // you can parse additional arguments if you want.
            // parser.parseArgument("more","args");

            // after parsing arguments, you should check
            // if enough arguments are given.
			
            if( arguments.isEmpty() )
               System.out.println("No additition argument are given!");
            else
            	System.out.println("Additional arguments: " + arguments);
            

        } catch( CmdLineException e ) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.println("java MySandbox_ADM [options...] arguments...");
            // print the list of available options
            // theCmdLineParser.printUsage(System.err);
            System.err.println();

            // print option sample. This is useful some time
            System.err.println("  Example: java SampleMain"+theCmdLineParser.printExample(ALL));

            return;        	
        }

	}
	
	public File get_inputFile() {
		return _inputFile;
	}

	public void set_inputFile(File f) {
		this._inputFile = f;
	}

	public String get_inputFileName() {
		return _inputFileName;
	}


}
