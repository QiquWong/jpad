//Version 2, there are 3 cases: the first one is without units, the second one is with units but is necessary to use different
// names for different objects in xml file. the third one is with unit measure and it's possible to use the same name for
// wings.

package sandbox.mr;


import static org.kohsuke.args4j.ExampleMode.ALL;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import standaloneutils.JPADXmlReader;

//the input file is given as argument of main

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;


public class Test_MR_02a_XML {



	@Option(name = "-i", aliases = { "--input" }, required = false,
			usage = "my input file")
	private File _inputFile;

	public CmdLineParser theCmdLineParser; 

	@Argument
	private List<String> arguments = new ArrayList<String>();


	// The class constructor
	public Test_MR_02a_XML() {

		theCmdLineParser = new CmdLineParser(this);

	}


	public static void main(String[] args) throws XPathExpressionException {

		System.out.println("XML read test ...");

		Test_MR_02_XML theObj = new Test_MR_02_XML();

		theObj.theCmdLineParser.printUsage(System.out);
		theObj.manageCmdLineArguments(args);

		//System.out.println("user.dir: " + System.getProperty("user.dir"));

		//String inputFileFullPath = MyConfiguration.currentDirectoryString + File.separator + theObj.get_inputFile().getName();



		System.out.println("[main] Now search test");

		String xmlFilePath = theObj.get_inputFile().getAbsolutePath(); 

		System.out.println("INPUT FILE, full path: " + xmlFilePath);
		//System.out.println("The input file name: " + theObj.get_inputFileName());

		JPADXmlReader xmlReader = new JPADXmlReader(xmlFilePath);

		//CASE 1--------------------------------------------------------------
		// Use file via MyXMLReader class (getXMLPropertiesByPath) to read XML without units.
		System.out.println("CASE 1:----------------");

		List<String> propertyNamesSurface = new ArrayList<>();
		List<String> propertyNamesSpanString = new ArrayList<>();
		String expression = "";

		// search via getXMLPropertiesByPath wing span
		expression = "//span";
		propertyNamesSpanString = xmlReader.getXMLPropertiesByPath(expression);
		//System.out.println("[main]\t expression: \"" + expression + "\"");
		System.out.println("Wing Span:");
		System.out.println("[main]\t result: " + propertyNamesSpanString);

		// search via getXMLPropertiesByPath wing surface
		expression = "//area";
		propertyNamesSurface = xmlReader.getXMLPropertiesByPath(expression);
		//System.out.println("[main]\t expression: \"" + expression + "\"");
		System.out.println("Wing Area:");
		System.out.println("[main]\t result: " + propertyNamesSurface);		

		// evaluate aspect ratio

		double[] aspectRatio = new double[propertyNamesSpanString.size()];

		System.out.println("aspect ratio:");
		for (int i = 0; i <= (propertyNamesSpanString.size()-1) ; i++){
			//aspectRatio[i]= Math.pow(Double.parseDouble(propertyNamesSpan.get(i),2)/Double.parseDouble(propertyNamesSurface.get(i)));
			aspectRatio[i]= (Double.parseDouble(propertyNamesSpanString.get(i))*Double.parseDouble(propertyNamesSpanString.get(i)))/Double.parseDouble(propertyNamesSurface.get(i));
			System.out.println("Wing number " + (i+1)+ " = " + aspectRatio[i] );
		}



		//CASE 2--------------------------------------------------------------
		// Use file via MyXMLReader class to read XML with units. it's possible to read one data for expression		


		System.out.println("\n \n CASE 2---------------------");	

		JPADXmlReader xmlReaderUnits = new JPADXmlReader(xmlFilePath);

		String[] expressionArea = {"//wing_1/area", "//wing_2/area", "//wing_3/area"};
		String[] expressionSpan = {"//wing_1/span", "//wing_2/span", "//wing_3/span"};
		double[] aspectRatioArray= new double[expressionArea.length];

		for (int i = 0; i < expressionArea.length; i++){

			String exprArea=expressionArea[i];
			String exprSpan=expressionSpan[i];

			Amount<Length> span = xmlReaderUnits.getXMLAmountLengthByPath(exprSpan).to(SI.METRE);	
			Amount<?> area = xmlReaderUnits.getXMLAmountWithUnitByPath(exprArea).to(SI.SQUARE_METRE);
			AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());

			double aspectRatioUnit = (span.getEstimatedValue()*span.getEstimatedValue())/area.getEstimatedValue();	

			aspectRatioArray[i]=aspectRatioUnit;

			System.out.println("\n Wing number " + (i+1));
			System.out.println("Span  " + span);
			System.out.println("Area  " + area); 

		}

		System.out.println("Aspect Ratio with units: " + Arrays.toString(aspectRatioArray));


		//CASE 3--------------------------------------------------------------
		// Use file via MyXMLReader class to read XML with units. 

		System.out.println("\n \n CASE 3---------------------");
		//List<Double> propertyNamesSpan = new ArrayList<>();

		// TO DO: to evaluate length of array its necessary to call method getXMLNodeListByPath.

		double[] propertyNamesSpan=new double [6];
		double[] propertyNamesArea=new double [6];
		String expressionUnitsSpan;
		String expressionUnitsArea;
		expressionUnitsSpan = "//span";
		propertyNamesSpan = xmlReader.getXMLAmountsLengthByPath(expressionUnitsSpan);
		//System.out.println("[main]\t expression: \"" + expression + "\"");
		System.out.println("Wing Span calculate with units (CASE 3):");
		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
		System.out.println("Wing Span in Meter: " + Arrays.toString(propertyNamesSpan));


		// TO DO: need to implement how to read surface and calculate aspect ratio		
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

	


}
