package sandbox2.vt.optimizations;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

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

public class OMOPSO_Test  {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;

	/**
	 * Main
	 *
	 * @param args
	 * @throws InvalidFormatException 
	 * @throws HDF5LibraryException 
	 */
	public static void main(String[] args) throws InvalidFormatException, HDF5LibraryException {

		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
		long startTime = System.currentTimeMillis();        

		System.out.println("-------------------");
		System.out.println("OMOPSO Test");
		System.out.println("-------------------");

		MyArgumentsAnalysis va = new MyArgumentsAnalysis();
		OMOPSO_Test.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class-> start ...)
		try {
			// before launching the JavaFX application thread (launch -
			OMOPSO_Test.theCmdLineParser.parseArgument(args);

			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("AIRCRAFT INPUT ===> " + pathToXML);

			System.out.println("--------------");

			////////////////////////////////////////////////////////////////////////
			// Optimization ...
			System.setOut(originalOut);
			System.out.println("\n\n\tRunning OMOPSO ... \n\n");
			
			NondominatedPopulation result = new Executor()
					.withProblem("UF1")
					.withAlgorithm("OMOPSO")
					.withMaxEvaluations(100000)
					.run();
			
			//display the results
			System.out.format("\tObjective1  Objective2%n\n");
			
			for (Solution solution : result) {
				System.out.format("\t%.4f      %.4f%n",
						solution.getObjective(0),
						solution.getObjective(1));
			}
			
			// setup the instrumenter to record the generational distance metric
//			Instrumenter instrumenter = new Instrumenter()
//					.withProblem("UF1")
//					.withFrequency(100)
//					.attachElapsedTimeCollector()
//					.attachGenerationalDistanceCollector();
//			
//			// use the executor to run the algorithm with the instrumenter
//			new Executor()
//					.withProblem("UF1")
//					.withAlgorithm("NSGAII")
//					.withMaxEvaluations(10000)
//					.withInstrumenter(instrumenter)
//					.run();
//			
//			Accumulator accumulator = instrumenter.getLastAccumulator();
//			
//			// print the runtime dynamics
//			System.out.format("  NFE    Time      Generational Distance%n");
//			
//			for (int i=0; i<accumulator.size("NFE"); i++) {
//				System.out.format("%5d    %-8.4f  %-8.4f%n",
//						accumulator.get("NFE", i),
//						accumulator.get("Elapsed Time", i),
//						accumulator.get("GenerationalDistance", i));
//			}
			
			System.out.println("\n\n\tDone!! \n\n");

			long estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println("\n\n\tTIME ESTIMATED = " + (estimatedTime) + " milliseconds");

			System.setOut(filterStream);

		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			OMOPSO_Test.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	

		System.exit(1);
	}
}
