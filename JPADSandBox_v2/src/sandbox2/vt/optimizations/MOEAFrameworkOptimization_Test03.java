package sandbox2.vt.optimizations;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import writers.JPADStaticWriteUtils;

public class MOEAFrameworkOptimization_Test03  {

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

		MyConfiguration.initWorkingDirectoryTree();
		String outputPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
		String subFolderPath = JPADStaticWriteUtils.createNewFolder(outputPath + "MOEA_Framework_Tests_03" + File.separator);
		
		////////////////////////////////////////////////////////////////////////
		// Optimization ...
		System.out.println("\n\n\tRunning MOEA Framework optimization ... \n");

		//......................................................................
		// Defining the optimization problem ...

		//------------------------------------------------------------------------------
		// TODO: Generalize for n-objectives 
		//       (also for the plot --> evaluate each combination of objective and generate each pareto front)
		//------------------------------------------------------------------------------

		ProblemSimpleNash problem = new ProblemSimpleNash(
				3,
				2
				);

		//......................................................................
		// Defining the optimization problem ...
		String[] algorithms = new String[] {
				"NSGAII",
				"OMOPSO"
		};
		List<NondominatedPopulation> resultList = new ArrayList<>();
		for(int i=0; i<algorithms.length; i++)
			resultList.add(new Executor()
					.withAlgorithm(algorithms[i])
					.withProblem(problem)
					.withMaxEvaluations(10000)
					.run()
					);

		//......................................................................
		// Print results and plots
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
				"Objective 1", "Objective 2", "", "",
				algorithms,
				subFolderPath, "OptimizationTest_Pareto", 
				true,
				true
				);

		System.out.println("\n\tDone!! \n\n");

		long estimatedTime = System.currentTimeMillis() - startTime;
		DecimalFormat numberFormat = new DecimalFormat("0.000");
		System.out.println("\n\n\tTIME ESTIMATED = " + numberFormat.format(estimatedTime*0.001) + " seconds");

		System.exit(1);
	}
}
