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

public class MOEAFrameworkOptimization_Test01  {

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
		String subFolderPath = JPADStaticWriteUtils.createNewFolder(outputPath + "MOEA_Framework_Tests_01" + File.separator);
		
		//------------------------------------------------------------------------------
		// TODO: The next step should be to read these data from an external file ...
		//------------------------------------------------------------------------------

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

		double[][][] yObjective2 = new double[yObjective1.length][yObjective1[0].length][yObjective1[0][0].length];
		for(int i=0; i<yObjective1.length; i++)
			for(int j=0; j<yObjective1[i].length; j++)
				for(int k=0; k<yObjective1[i][j].length; k++)
					yObjective2[i][j][k] = -2*yObjective1[i][j][k];

		////////////////////////////////////////////////////////////////////////
		// Optimization ...
		System.out.println("\n\n\tRunning MOEA Framework optimization ... \n");

		//......................................................................
		// Defining the optimization problem ...

		//------------------------------------------------------------------------------
		// TODO: Generalize for n-objectives 
		//       (also for the plot --> evaluate each combination of objective and generate each pareto front)
		//------------------------------------------------------------------------------

//		ProblemFromResponseSurface problem = new ProblemFromResponseSurface(
//				3,
//				2
//				);
//		problem.interpolateResponseSurface(0, xArrays, yObjective1);
//		problem.interpolateResponseSurface(1, xArrays, yObjective2);
//		problem.setVariablesUpperBounds(new double[] {5.0, 37.0, 42.5});
//		problem.setVariablesLowerBounds(new double[] {0.0, 1.0, 0.5});

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
					.withProblem("UF1")
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
