package sandbox.vt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import standaloneutils.JPADXmlReader;
import org.kohsuke.args4j.CmdLineException;

public class XML_ReadingTest_VT
{
	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	@Option(name = "-i", aliases = { "--input" }, required = false,
			usage = "my input file")
	private File _inputFile;
	
	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	
	@Argument
    private List<String> arguments = new ArrayList<String>();
	
	//-------------------------------------------------------------------------------------
	// BUILDER
	public XML_ReadingTest_VT()
	{
		theCmdLineParser = new CmdLineParser(this);
	}
	
	//-------------------------------------------------------------------------------------
	// MAIN
	public static void main(String[] args)
	{
		System.out.println("------------------------------------------------------------");
		System.out.println("XML_ReadingTest_VT :: main ");
		System.out.println("------------------------------------------------------------");
		
		// prints the usage of the _inputFile decoration
		XML_ReadingTest_VT test = new XML_ReadingTest_VT();
		System.out.println("Input variable usage:");
		test.theCmdLineParser.printUsage(System.out);
		test.manageCmdLineArguments(args);
		
		// building file path name
		String path = test.get_inputFile().getAbsolutePath();
		/*
		// ALTERNATIVE METHOD TO BUID "path" WITHOUT ARGUMENTS IN RUN CONFIGURATION 
		String inputFolder = "in";
		String inputFileFolderPath = MyConfiguration.currentDirectoryString 
									 + File.separator + inputFolder;
		String path = inputFileFolderPath + File.separator + "input_VT_XML.xml";
		
		 */
		System.out.println("------------------------------------------------------------");
		System.out.println("XML File Path : " + path);
		System.out.println("------------------------------------------------------------");
		
		// now MyXMLReader Class is used to read data from input file
		JPADXmlReader reader = new JPADXmlReader(path);
		System.out.println("------------------------------------------------------------");
		System.out.println("Initialize reading");
	
	//------------------------------------------------------------------------------
		// # PRINCIPAL METHOD (implements unit conversions)
		// CASE #1 : A380
		String expression_1_A380 = "//wing_data_A380/span";
		String expression_2_A380 = "//wing_data_A380/area";
		Amount<Length> span_A380 = reader.getXMLAmountLengthByPath(expression_1_A380);
		Amount<?> area_A380 = reader.getXMLAmountWithUnitByPath(expression_2_A380).to(SI.SQUARE_METRE);
		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
		double AR_A380 = Math.pow(span_A380.getEstimatedValue(), 2)/area_A380.getEstimatedValue();
		// print results
		System.out.println("------------------------------------------------------------");
		System.out.println("CASE 1 : A380");
		System.out.println("------------------------------------------------------------");
		System.out.println("Expression span : " + expression_1_A380);
		System.out.println("Wing Span : " + span_A380);
		System.out.println("");
		System.out.println("Expression area : " + expression_2_A380);
		System.out.println("Wing Area : " + area_A380);
		System.out.println("");
		System.out.println("AR A380 : " + AR_A380);
		System.out.println("------------------------------------------------------------");
		
		// CASE #2 : ATR72
		String expression_1_ATR72 = "//wing_data_ATR72/span";
		String expression_2_ATR72 = "//wing_data_ATR72/area";
		Amount<Length> span_ATR72 = reader.getXMLAmountLengthByPath(expression_1_ATR72);
		Amount<?> area_ATR72 = reader.getXMLAmountWithUnitByPath(expression_2_ATR72).to(SI.SQUARE_METRE);
		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
		double AR_ATR72 = Math.pow(span_ATR72.getEstimatedValue(), 2)/area_ATR72.getEstimatedValue();
		// print results
		System.out.println("------------------------------------------------------------");
		System.out.println("CASE 2 : ATR72");
		System.out.println("------------------------------------------------------------");
		System.out.println("Expression span : " + expression_1_ATR72);
		System.out.println("Wing Span : " + span_ATR72);
		System.out.println("");
		System.out.println("Expression area : " + expression_2_ATR72);
		System.out.println("Wing Area : " + area_ATR72);
		System.out.println("");
		System.out.println("AR ATR72 : " + AR_ATR72);
		System.out.println("------------------------------------------------------------");
		
	//------------------------------------------------------------------------------------
		/*
		// # ALTERNATIVE METHOD (no unit conversion allowed)
		
		// generating ArrayList of properties
		List<String> spanProperty = new ArrayList<String>();
		List<String> areaProperty = new ArrayList<String>();
		String expression_1 = "//span";
		String expression_2 = "//area";
		
		// collecting occurrence of the data specified with measure units
		spanProperty = reader.getXMLPropertiesByPath(expression_1);
		areaProperty = reader.getXMLPropertiesByPath(expression_2);
		
		// print results
		System.out.println("------------------------------------------------------------");
		System.out.println("Expression span : " + expression_1);
		System.out.println("Wing Spans : " + spanProperty);
		System.out.println("");
		System.out.println("Expression area : " + expression_2);
		System.out.println("Wing Areas : " + areaProperty);
		System.out.println("------------------------------------------------------------");
		
		// calculating AR
		Double[] AR = new Double[spanProperty.size()];
		for (int i=0; i<AR.length; i++)
		{
			AR[i] = Math.pow(Double.valueOf(spanProperty.get(i)), 2)/Double.valueOf(areaProperty.get(i));
			System.out.println("AR aircraft #" + (i+1) + ": " + AR[i]);
			System.out.println("------------------------------------------------------------");
		}
		*/
	}

	//-------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	public File get_inputFile()
	{
		return _inputFile;
	}
	
	public void set_inputFile(File _inputFile)
	{
		this._inputFile = _inputFile;
	}
	
	//-------------------------------------------------------------------------------------
	// OTHER METHODS
	public void manageCmdLineArguments(String[] args) 
	{

	try 
		{
        // parse the arguments.
		theCmdLineParser.parseArgument(args);
		
        // you can parse additional arguments if you want.
        // parser.parseArgument("more","args");

        // after parsing arguments, you should check
        // if enough arguments are given.
		
        if( arguments.isEmpty() )
            throw new CmdLineException(theCmdLineParser,"No additional argument are given!!!");
        else
        	System.out.println("");
        	System.out.println("Additional arguments: " + arguments);
    	} 
	
	catch( CmdLineException e ) 
		{
        // if there's a problem in the command line,
        // you'll get this exception. this will report
        // an error message.
		System.out.println("");
        System.out.println(e.getMessage());
        return;        	
		}
	}
}