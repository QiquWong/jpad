package sandbox2.vt.optimizations;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

class MyArgumentsAnalysis {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}

}

public class MOEAFrameworkOptimization_Test01  {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;

	/**
	 * Main
	 *
	 * @param args
	 * @throws InvalidFormatException 
	 * @throws HDF5LibraryException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InvalidFormatException, HDF5LibraryException, ClassNotFoundException, IOException {

		long startTime = System.currentTimeMillis();        

		System.out.println("-------------------");
		System.out.println("MOEA Framework Test");
		System.out.println("-------------------");

		MyArgumentsAnalysis va = new MyArgumentsAnalysis();
		MOEAFrameworkOptimization_Test01.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class-> start ...)
		try {
			// before launching the JavaFX application thread (launch -
			MOEAFrameworkOptimization_Test01.theCmdLineParser.parseArgument(args);

			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT FILE ===> " + pathToXML);

			System.out.println("--------------");

			// Data from --> https://www.ee.ucl.ac.uk/~mflanaga/java/PolyCubicSplineExample.java
			// TODO: The next step should be to read these data from an external file ...
	        // Array of x1
	        double[] x1 = {0.0,	1.0, 2.0, 3.0, 4.0, 5.0};
	        // Array of x2
	        double[] x2 = {1.0, 5.0, 9.0, 13.0, 17.0, 21.0, 25.0, 29.0, 33.0, 37.0};
	        // Array of x3
	        double[] x3 = {0.5, 6.5, 12.5, 18.5, 24.5, 30.5, 36.5, 42.5};

	        // Pack arrays x1, x2 and x3 into a two dimensional array
	        double[][] xArrays= new double[3][];
	        xArrays[0] = x1;
	        xArrays[1] = x2;
	        xArrays[2] = x3;

	        // Three dimensional array of corresponding y values
	        double[][][] yObjective1 = { 
	        		{
	        			{1.5, 7.5, 13.5, 19.5, 25.5, 31.5, 37.5, 43.5}, 
	        			{5.5, 11.5, 17.5, 23.5, 29.5, 35.5, 41.5, 47.5},  
	        			{9.5, 15.5, 21.5, 27.5, 33.5, 39.5, 45.5, 51.5},  
	        			{13.5, 19.5, 25.5, 31.5, 37.5, 43.5, 49.5, 55.5}, 
	        			{17.5, 23.5, 29.5, 35.5, 41.5, 47.5, 53.5, 59.5}, 
	        			{21.5, 27.5, 33.5, 39.5, 45.5, 51.5, 57.5, 63.5}, 
	        			{25.5, 31.5, 37.5, 43.5, 49.5, 55.5, 61.5, 67.5}, 
	        			{29.5, 35.5, 41.5, 47.5, 53.5, 59.5, 65.5, 71.5},  
	        			{33.5, 39.5, 45.5, 51.5, 57.5, 63.5, 69.5, 75.5},  
	        			{37.5, 43.5, 49.5, 55.5, 61.5, 67.5, 73.5, 79.5} 
	        		},
	        		{
	        			{2.5, 8.5, 14.5, 20.5, 26.5, 32.5, 38.5, 44.5},
	        			{6.5, 12.5, 18.5, 24.5, 30.5, 36.5, 42.5, 48.5}, 
	        			{10.5, 16.5, 22.5, 28.5, 34.5, 40.5, 46.5, 52.5}, 
	        			{14.5, 20.5, 26.5, 32.5, 38.5, 44.5, 50.5, 56.5}, 
	        			{18.5, 24.5, 30.5, 36.5, 42.5, 48.5, 54.5, 60.5}, 
	        			{22.5, 28.5, 34.5, 40.5, 46.5, 52.5, 58.5, 64.5}, 
	        			{26.5, 32.5, 38.5, 44.5, 50.5, 56.5, 62.5, 68.5}, 
	        			{30.5, 36.5, 42.5, 48.5, 54.5, 60.5, 66.5, 72.5}, 
	        			{34.5, 40.5, 46.5, 52.5, 58.5, 64.5, 70.5, 76.5}, 
	        			{38.5, 44.5, 50.5, 56.5, 62.5, 68.5, 74.5, 80.5} 
	        		},
	        		{ 
	        			{3.5, 9.5, 15.5, 21.5, 27.5, 33.5, 39.5, 45.5},
	        			{7.5, 13.5, 19.5, 25.5, 31.5, 37.5, 43.5, 49.5},
	        			{11.5, 17.5, 23.5, 29.5, 35.5, 41.5, 47.5, 53.5},
	        			{15.5, 21.5, 27.5, 33.5, 39.5, 45.5, 51.5, 57.5}, 
	        			{19.5, 25.5, 31.5, 37.5, 43.5, 49.5, 55.5, 61.5}, 
	        			{23.5, 29.5, 35.5, 41.5, 47.5, 53.5, 59.5, 65.5}, 
	        			{27.5, 33.5, 39.5, 45.5, 51.5, 57.5, 63.5, 69.5}, 
	        			{31.5, 37.5, 43.5, 49.5, 55.5, 61.5, 67.5, 73.5}, 
	        			{35.5, 41.5, 47.5, 53.5, 59.5, 65.5, 71.5, 77.5}, 
	        			{39.5, 45.5, 51.5, 57.5, 63.5, 69.5, 75.5, 81.5} 
	        		},  
	        		{ 
	        			{4.5, 10.5, 16.5, 22.5, 28.5, 34.5, 40.5, 46.5}, 
	        			{8.5, 14.5, 20.5, 26.5, 32.5, 38.5, 44.5, 50.5}, 
	        			{12.5, 18.5, 24.5, 30.5, 36.5, 42.5, 48.5, 54.5},
	        			{16.5, 22.5, 28.5, 34.5, 40.5, 46.5, 52.5, 58.5}, 
	        			{20.5, 26.5, 32.5, 38.5, 44.5, 50.5, 56.5, 62.5}, 
	        			{24.5, 30.5, 36.5, 42.5, 48.5, 54.5, 60.5, 66.5}, 
	        			{28.5, 34.5, 40.5, 46.5, 52.5, 58.5, 64.5, 70.5}, 
	        			{32.5, 38.5, 44.5, 50.5, 56.5, 62.5, 68.5, 74.5},  
	        			{36.5, 42.5, 48.5, 54.5, 60.5, 66.5, 72.5, 78.5}, 
	        			{40.5, 46.5, 52.5, 58.5, 64.5, 70.5, 76.5, 82.5} 
	        		}, 
	        		{ 
	        			{5.5, 11.5, 17.5, 23.5, 29.5, 35.5, 41.5, 47.5}, 
	        			{9.5, 15.5, 21.5, 27.5, 33.5, 39.5, 45.5, 51.5}, 
	        			{13.5, 19.5, 25.5, 31.5, 37.5, 43.5, 49.5, 55.5},
	        			{17.5, 23.5, 29.5, 35.5, 41.5, 47.5, 53.5, 59.5}, 
	        			{21.5, 27.5, 33.5, 39.5, 45.5, 51.5, 57.5, 63.5}, 
	        			{25.5, 31.5, 37.5, 43.5, 49.5, 55.5, 61.5, 67.5}, 
	        			{29.5, 35.5, 41.5, 47.5, 53.5, 59.5, 65.5, 71.5}, 
	        			{33.5, 39.5, 45.5, 51.5, 57.5, 63.5, 69.5, 75.5}, 
	        			{37.5, 43.5, 49.5, 55.5, 61.5, 67.5, 73.5, 79.5}, 
	        			{41.5, 47.5, 53.5, 59.5, 65.5, 71.5, 77.5, 83.5} 
	        		},
	        		{
	        			{6.5, 12.5, 18.5, 24.5, 30.5, 36.5, 42.5, 48.5},
	        			{10.5, 16.5, 22.5, 28.5, 34.5, 40.5, 46.5, 52.5}, 
	        			{14.5, 20.5, 26.5, 32.5, 38.5, 44.5, 50.5, 56.5}, 
	        			{18.5, 24.5, 30.5, 36.5, 42.5, 48.5, 54.5, 60.5}, 
	        			{22.5, 28.5, 34.5, 40.5, 46.5, 52.5, 58.5, 64.5}, 
	        			{26.5, 32.5, 38.5, 44.5, 50.5, 56.5, 62.5, 68.5}, 
	        			{30.5, 36.5, 42.5, 48.5, 54.5, 60.5, 66.5, 72.5},  
	        			{34.5, 40.5, 46.5, 52.5, 58.5, 64.5, 70.5, 76.5}, 
	        			{38.5, 44.5, 50.5, 56.5, 62.5, 68.5, 74.5, 80.5}, 
	        			{42.5, 48.5, 54.5, 60.5, 66.5, 72.5, 78.5, 84.5}
	        			}
	        		};

	        // TODO: CREATE THE SECOND EXAMPLE OBJECTIVE (-2*Obj1)
	        // double[][][] yObjective2 =  
	        
			////////////////////////////////////////////////////////////////////////
			// Optimization ...
			System.out.println("\n\n\tRunning MOEA Framework optimization ... \n\n");
			
	        // TODO !!
			
			System.out.println("\n\n\tDone!! \n\n");

			long estimatedTime = System.currentTimeMillis() - startTime;
			DecimalFormat numberFormat = new DecimalFormat("0.000");
			System.out.println("\n\n\tTIME ESTIMATED = " + numberFormat.format(estimatedTime*0.001) + " seconds");

		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			MOEAFrameworkOptimization_Test01.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	

		System.exit(1);
	}
}
