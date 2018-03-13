package sandbox2.vt.optimizations;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import writers.JPADStaticWriteUtils;

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

public class MOEAFrameworkOptimization_Test02  {

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
		MOEAFrameworkOptimization_Test02.theCmdLineParser = new CmdLineParser(va);
		MyConfiguration.initWorkingDirectoryTree();
		String outputPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
		String subFolderPath = JPADStaticWriteUtils.createNewFolder(outputPath + "MOEA_Framework_Tests_02" + File.separator);
		
		try {
			MOEAFrameworkOptimization_Test02.theCmdLineParser.parseArgument(args);

			String inputFilePath = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT FILE ===> " + inputFilePath);

			System.out.println("--------------");

			////////////////////////////////////////////////////////////////////////
			// Optimization ...
			System.out.println("\n\n\tRunning MOEA Framework optimization ... \n");
			
			//......................................................................
			// Defining the optimization problem ...
			
			//------------------------------------------------------------------------------
			// TODO: Generalize for n-objectives 
			//       (also for the plot --> evaluate each combination of objective and generate each pareto front)
			//------------------------------------------------------------------------------
			
			ProblemFromResponseSurface problem = new ProblemFromResponseSurface(
					3,
					2
					);
			problem.importResponseSurface(inputFilePath);
			problem.setVariablesLowerBounds(new double[] {19.0, 11.0, 4.0});
			problem.setVariablesUpperBounds(new double[] {22.0, 13.0, 7.0});
			
			//......................................................................
			// Defining the optimization problem ...
			String[] algorithms = new String[] {
					"NSGAII",
					"OMOPSO"
					};
			List<NondominatedPopulation> resultList = new ArrayList<>();
			for(int i=0; i<algorithms.length; i++) {
				
				System.out.println("\n\tRunning " + algorithms[i] + " algorithm ...");
				
				resultList.add(new Executor()
						.withAlgorithm(algorithms[i])
						.withProblem(problem)
						.withMaxEvaluations(10000)
						.run()
						);
				
				System.out.println("\tDone!! \n");
			}

			//......................................................................
			// Print results and plots
			System.out.println("\n\tCreating Pareto Fronts and printing results ...\n");
			
			List<Double> optimumObjective1Values = new ArrayList<>();
			List<Double> optimumObjective2Values = new ArrayList<>();
			Map<String, List<Double>> optimumObjective1Map = new HashMap<>();
			Map<String, List<Double>> optimumObjective2Map = new HashMap<>();

			for (int i=0; i<algorithms.length; i++) {
				optimumObjective1Values = new ArrayList<>();
				optimumObjective2Values = new ArrayList<>();
				for (int j=0; j<resultList.get(i).size(); j++) {
					// if (!solution.violatesConstraints()) {
					optimumObjective1Values.add(resultList.get(i).get(j).getObjective(0));
					optimumObjective2Values.add(resultList.get(i).get(j).getObjective(1));
				}
				optimumObjective1Map.put(algorithms[i], optimumObjective1Values);
				optimumObjective2Map.put(algorithms[i], optimumObjective2Values);
			}

			double[][] xMatrix = new double[algorithms.length][optimumObjective1Values.size()];
			double[][] yMatrix = new double[algorithms.length][optimumObjective1Values.size()];
			for(int i=0; i<algorithms.length; i++) {
				xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(optimumObjective1Map.get(algorithms[i]));
				yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(optimumObjective2Map.get(algorithms[i]));
			}
				
			MyChartToFileUtils.scatterPlot(
					xMatrix,
					yMatrix, 
					null, null, null, null, 
					"Efficiency", "Static Stability Margin", "", "%",
					algorithms,
					subFolderPath, "OptimizationTest_Pareto", 
					true,
					true
					);
			
			System.out.println("\n\tDone!! \n\n");

			long estimatedTime = System.currentTimeMillis() - startTime;
			DecimalFormat numberFormat = new DecimalFormat("0.000");
			System.out.println("\n\n\tTIME ESTIMATED = " + numberFormat.format(estimatedTime*0.001) + " seconds");

		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			MOEAFrameworkOptimization_Test02.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	

		System.exit(1);
	}
}
