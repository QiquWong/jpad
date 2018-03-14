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
import configuration.enumerations.ConstraintsViolationConditionEnum;
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
			ProblemFromResponseSurface problem = new ProblemFromResponseSurface(
					5,
					3,
					1
					);
			problem.setMaximizationProblemConditionArray(new boolean[] {true, false, true});
			problem.importResponseSurface(inputFilePath);
			problem.setVariablesLowerBounds(new double[] {19.0, 0.95, 6.0, 4.0, 1.0});
			problem.setVariablesUpperBounds(new double[] {20.5, 1.2, 9.0, 7.0, 1.25});
			problem.setConstraintsValues(new double[] {0.03});
			problem.setConstraintsViolationConditions(new ConstraintsViolationConditionEnum[] {ConstraintsViolationConditionEnum.LESS_EQUAL_THAN});
			
			//......................................................................
			// Defining the optimization problem ...
			String[] algorithms = new String[] {
					"eNSGAII",
					"OMOPSO"
					};
			List<NondominatedPopulation> resultList = new ArrayList<>();
			for(int i=0; i<algorithms.length; i++) {
				
				System.out.println("\n\tRunning " + algorithms[i] + " algorithm ...");
				
				resultList.add(new Executor()
						.withAlgorithm(algorithms[i])
						.withProblem(problem)
						.withMaxEvaluations(10000)
						.withProperty("population.size", "50")
						.run()
						);
				
				System.out.println("\tDone!! \n");
			}

			//......................................................................
			// Print results and plots
			System.out.println("\n\tCreating Pareto Fronts and printing results ...\n");
			
			List<List<Double>> optimumObjectiveValuesList = new ArrayList<>();
			List<Map<String, List<Double>>> optimumObjectiveMapList = new ArrayList<>();
			
			List<Double> optimumCurrentObjectiveValues = new ArrayList<>();
			Map<String, List<Double>> optimumCurrentObjectiveMap = new HashMap<>();

			for (int obj=0; obj<problem.getNumberOfObjectives(); obj++) {
				for (int i=0; i<algorithms.length; i++) {
					optimumCurrentObjectiveValues = new ArrayList<>();
					for (int j=0; j<resultList.get(i).size(); j++) {
						if (!resultList.get(i).get(j).violatesConstraints()) 
						optimumCurrentObjectiveValues.add(resultList.get(i).get(j).getObjective(obj));
					}
					optimumCurrentObjectiveMap.put(algorithms[i], optimumCurrentObjectiveValues);
				}
				optimumObjectiveValuesList.add(optimumCurrentObjectiveValues);
				optimumObjectiveMapList.add(optimumCurrentObjectiveMap);
			}

			for(int i=0; i<problem.getNumberOfObjectives(); i++) {
				for(int j=i+1; j<problem.getNumberOfObjectives(); j++) {
					
					double[][] xMatrix = new double[algorithms.length][optimumObjectiveValuesList.get(i).size()];
					double[][] yMatrix = new double[algorithms.length][optimumObjectiveValuesList.get(i).size()];
					
					for(int k=0; k<algorithms.length; k++) {
						xMatrix[k] = MyArrayUtils.convertToDoublePrimitive(optimumObjectiveMapList.get(i).get(algorithms[k]));
						yMatrix[k] = MyArrayUtils.convertToDoublePrimitive(optimumObjectiveMapList.get(j).get(algorithms[k]));
					}
					
					MyChartToFileUtils.scatterPlot(
							xMatrix,
							yMatrix, 
							null, null, null, null, 
							problem.getObjectivesLabelArray()[i], problem.getObjectivesLabelArray()[j], "", "",
							algorithms,
							subFolderPath,
							("ParetoFront_" 
									+ problem.getObjectivesLabelArray()[i] + "_" 
									+ problem.getObjectivesLabelArray()[j]
									), 
							true,
							true
							);
				}
			}

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
