package sandbox.mr;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import static org.kohsuke.args4j.ExampleMode.ALL;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import standaloneutils.JPADXmlReader;


public class Test_MR_03_Downwash {

	@Option(name = "-i", aliases = { "--input" }, required = false,
			usage = "my input file")
	private File _inputFile;

	public CmdLineParser theCmdLineParser; 

	@Argument
	private List<String> arguments = new ArrayList<String>();

	// The class constructor
	public Test_MR_03_Downwash() {

		theCmdLineParser = new CmdLineParser(this);
	}
	
	public static void main(String[] args) {

		if(args.length == 0){
			System.err.println("No input file");
			return;
		}
		System.out.println("XML read test ...");

		
		//Start Reading XML file

		Test_MR_03_Downwash theObj = new Test_MR_03_Downwash();

		theObj.theCmdLineParser.printUsage(System.out);
		theObj.manageCmdLineArguments(args);

		String xmlFilePath = theObj.get_inputFile().getAbsolutePath(); 

		System.out.println("\n INPUT FILE, full path: " + xmlFilePath + "\n");


		JPADXmlReader xmlReaderUnits = new JPADXmlReader(xmlFilePath);

		System.out.println("\n -----------------------------------------");
		System.out.println("Start reading file:" + xmlFilePath);
		System.out.println(" ----------------------------------------- \n");

		
		//Extraction of data from XML file. 
		
		//Sweep Angle at c/4
		Amount<?> sweepQuarterChordEqAmountDeg = xmlReaderUnits.getXMLAmountWithUnitByPath("//Equivalent_Wing_parameters/sweepc4").to(SI.RADIAN);
		double sweepQuarterChordEq=sweepQuarterChordEqAmountDeg.getEstimatedValue();
		System.out.println("Sweep angle at c/4, equivalent wing [rad] --> " + sweepQuarterChordEq );

		//Sweep angle at LE
		Amount<?> sweepLEEqAmountDeg = xmlReaderUnits.getXMLAmountWithUnitByPath("//Equivalent_Wing_parameters/sweepLE").to(SI.RADIAN);
		double sweepLEEq=sweepQuarterChordEqAmountDeg.getEstimatedValue();
		System.out.println("Sweep angle at c/4, equivalent wing [rad] --> " + sweepLEEq );

		
		//Aspect Ratio
		List<String> aspectRatioWing= xmlReaderUnits.getXMLPropertiesByPath("//Equivalent_Wing_parameters/aspectRatio");
		double aspectRatioDouble = Double.valueOf(aspectRatioWing.get(0));
		System.out.println("Aspect Ratio --> " + aspectRatioDouble); 
		
		//Taper Ratio
		List<String> taperRatio = xmlReaderUnits.getXMLPropertiesByPath("//Equivalent_Wing_parameters/taperRatio");
		double taperRatioDouble = Double.valueOf(taperRatio.get(0));
		System.out.println("Taper Ratio --> " + taperRatioDouble); 

		//distAerodynamicCenter Distance between the points at c/4 of the mean aerodynamic chord of the wing and the horizontal tail.
		Amount<Length> xCoordinateHTail = xmlReaderUnits.getXMLAmountLengthByPath("//Equivalent_HTail_parameters/Xcoordinate");
		Amount<Length> xCoordinateMAC = xmlReaderUnits.getXMLAmountLengthByPath("//Actual_Wing_parameters/Mean_aerodynamic_chord_xLE_BRF");
		Amount<Length> meanAerodynamicChord = xmlReaderUnits.getXMLAmountLengthByPath("//Equivalent_Wing_parameters/Mean_aerodynamic_chord_MAC");
		Amount<Length> hTailRootChord = xmlReaderUnits.getXMLAmountLengthByPath("//Actual_HTail_parameters/rootChord");
		double distAerodynamicCenter = (xCoordinateHTail.getEstimatedValue()-(xCoordinateMAC.getEstimatedValue()+
				0.25*meanAerodynamicChord.getEstimatedValue())+0.25*hTailRootChord.getEstimatedValue() ); 		
		System.out.println("Distance between aerodynamic center of wing and c/4 point of the h tail root chord [m] --> " + distAerodynamicCenter);

		//Distance between the AC of the wing and the horizontal tail.
		Amount<Length> acDistanceHtailWing = xmlReaderUnits.getXMLAmountLengthByPath("//Actual_HTail_parameters/AC_to_Wing_AC_distance");
		double acDistanceDouble = Double.valueOf(acDistanceHtailWing.getEstimatedValue());
		System.out.println("Distance between aerodynamic center of wing and h tail [m] --> " + acDistanceDouble); 
		
		//Distance between the horizontal tail and the wing root chord
		Amount<Length> zCoordinateWing = xmlReaderUnits.getXMLAmountLengthByPath("//Equivalent_Wing_parameters/Zcoordinate");
		Amount<Length> ZCoordinateHTail = xmlReaderUnits.getXMLAmountLengthByPath("//Equivalent_HTail_parameters/Zcoordinate");		
		double distWingToHTail= -zCoordinateWing.getEstimatedValue()+ZCoordinateHTail.getEstimatedValue();
		System.out.println("Distance among z axis between root chord of wing and H tail [m] --> "+ distWingToHTail);
		
		//TODO verify the value of Mach number in the input file
		//CL_alfa
		List<String> clAlfaAnderson = xmlReaderUnits.getXMLPropertiesByPath("//Wing_Analysis/Aerodynamics/CL_alpha/Integral_mean_2d");
		double clAlfaIntegralDouble= Double.valueOf(clAlfaAnderson.get(0));
		System.out.println("CL Alfa [1/rad] --> " + clAlfaIntegralDouble); 
		
		// CL alfa evaluated by Polhamus formula
		List<String> sweepLe = xmlReaderUnits.getXMLPropertiesByPath("//Equivalent_Wing_parameters/sweepLE");
		double sweepLeDouble= Math.toRadians(Double.valueOf(sweepLe.get(0)));
		double machNumber=0.8; // NB --> check the Mach value from data input
		double clAlfaPolhamus= AeroLibraryCalculator.calculateCLalfaPolhamus(aspectRatioDouble,sweepLeDouble,machNumber,taperRatioDouble);
		System.out.println("CL evaluated by Polhamus formula --> "+ clAlfaPolhamus);
		
		//Wing Span
		List<String> wingSpan = xmlReaderUnits.getXMLPropertiesByPath("//Actual_Wing_parameters/span");
		double wingSpanDouble= Double.valueOf(wingSpan.get(0));
		System.out.println("Wing Span [m] --> " + wingSpanDouble); 

		// Define an object to do the downwash test
		System.out.println("\n Start calculating Downwash gradiendt ... \n ");
				
		DownwashCalculator_03 test = new DownwashCalculator_03( aspectRatioDouble,
				                                          taperRatioDouble);
		double downwashDatcom=test.calculateDownwashDatcom(distAerodynamicCenter, distWingToHTail,
														    wingSpanDouble,sweepQuarterChordEq);
		double downwashDelft=test.calculateDownwashDelft(acDistanceDouble, distWingToHTail,
															clAlfaIntegralDouble, wingSpanDouble,sweepQuarterChordEq);
		
		System.out.println("The value of downwash gradient, calculate with Datcom Method is: --> " + downwashDatcom);
		System.out.println("The value of downwash gradient, calculate with Delft Method 2 is: --> " + downwashDelft);
	
		
	  // Check the sensibility
		
	  System.out.println("\n Start testing the sensibility of methods with the sweep angle... \n");	
	  double [] sweepAngles = new double [5];
	  double [] downwashArrayDatcom = new double [5];
	  double [] downwashArrayDelft= new double [5];
	  
	  sweepAngles[2]=  Math.toDegrees(sweepQuarterChordEq);
	  for (int i=3; i<sweepAngles.length; i++) {
		  sweepAngles[i]=sweepAngles[i-1]+3; //5 degree
	  }
	  for (int i=1; i>=0; i--) {
		  sweepAngles[i]=sweepAngles[i+1]-3; //5 degree
	  }
		System.out.println("Sweep Angle array --> " + Arrays.toString(sweepAngles));
		
		
		for (int i=0; i<sweepAngles.length; i++){
			
		downwashArrayDatcom[i]=test.calculateDownwashDatcom(distAerodynamicCenter, distWingToHTail,
				    wingSpanDouble,Math.toRadians(sweepAngles[i]));
		downwashArrayDelft[i]=test.calculateDownwashDelft(distAerodynamicCenter, distWingToHTail,
					clAlfaIntegralDouble, wingSpanDouble,Math.toRadians(sweepAngles[i]));
		}
		
		System.out.println("\nArray Downwash evaluated by Datcom Method  --> " + Arrays.toString(downwashArrayDatcom));
		System.out.println("Array Downwash evaluated by Delft Method --> " + Arrays.toString(downwashArrayDelft));
		
		if (downwashArrayDatcom[2]<downwashArrayDatcom[3]){
			System.out.println("\nThe Downwash gradient evaluated by Datcom Method INCREASE with sweep angle");
		}
		else System.out.println("\nThe Downwash gradient evaluated by Datcom Method DECREASE with sweep angle");
		
		if (downwashArrayDelft[2]<downwashArrayDelft[3]){
			System.out.println("The Downwash gradient evaluated by Delft Method INCREASE with sweep angle");
		}
		else System.out.println("The Downwash gradient evaluated by Delft Method DECREASE with sweep angle");
		
	
		int n = sweepAngles.length;
		
		DownwashCalculator_03.createSweepAngleDownwashGradientChart(n, sweepAngles, downwashArrayDatcom, downwashArrayDelft);
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
