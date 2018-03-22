package sandbox2.vt.optimizations;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

public class MOEAFrameworkOptimization_Test04  {

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
		String subFolderPath = JPADStaticWriteUtils.createNewFolder(outputPath + "MOEA_Framework_Tests_04" + File.separator);
		
		////////////////////////////////////////////////////////////////////////
		// Optimization ...
		System.out.println("\n\n\tRunning MOEA Framework optimization ... \n");

		//......................................................................
		// Defining the optimization problem ...
		ProblemComplexNash problem = new ProblemComplexNash(
				2,
				1
				);

		//......................................................................
		// Defining the optimization problem ...
		String[] algorithms = new String[] {
				"eNSGAII",
				"OMOPSO"
		};
		List<NondominatedPopulation> resultList = new ArrayList<>();
		for(int i=0; i<algorithms.length; i++)
			resultList.add(new Executor()
					.withAlgorithm(algorithms[i])
					.withProblem(problem)
					.withMaxEvaluations(10000)
					.withProperty("population.size", "10000")
					.run()
					);

		//......................................................................
		// Print results and plots
		Map<Integer, Map<String, List<Double>>> optimumObjectiveMapList = new HashMap<>();
		List<Double> optimumCurrentObjectiveValues = new ArrayList<>();
		Map<String, List<Double>> optimumCurrentObjectiveMap = new HashMap<>();

		for (int obj=0; obj<problem.getNumberOfObjectives(); obj++) {
			optimumCurrentObjectiveMap = new HashMap<>();
			for (int i=0; i<algorithms.length; i++) {
				optimumCurrentObjectiveValues = new ArrayList<>();
				for (int j=0; j<resultList.get(i).size(); j++) {
					if (!resultList.get(i).get(j).violatesConstraints()) 
					optimumCurrentObjectiveValues.add(resultList.get(i).get(j).getObjective(obj));
				}
				optimumCurrentObjectiveMap.put(algorithms[i], optimumCurrentObjectiveValues);
			}
			optimumObjectiveMapList.put(obj, optimumCurrentObjectiveMap);
		}
		
		Map<Integer, Map<String, List<Double>>> optimumVariableMapList = new HashMap<>();
		List<Double> optimumCurrentVariableValues = new ArrayList<>();
		Map<String, List<Double>> optimumCurrentVariableMap = new HashMap<>();
		
		for (int var=0; var<problem.getNumberOfVariables(); var++) {
			optimumCurrentVariableMap = new HashMap<>();
			for (int i=0; i<algorithms.length; i++) {
				optimumCurrentVariableValues = new ArrayList<>();
				for (int j=0; j<resultList.get(i).size(); j++) {
					if (!resultList.get(i).get(j).violatesConstraints()) 
						optimumCurrentVariableValues.add(Double.valueOf(resultList.get(i).get(j).getVariable(var).toString()));
				}
				optimumCurrentVariableMap.put(algorithms[i], optimumCurrentVariableValues);
			}
			optimumVariableMapList.put(var, optimumCurrentVariableMap);
		}

		if(problem.getNumberOfObjectives() > 1) {
			System.out.println("\n\tCreating Pareto Fronts and printing results ...");
			for(int i=0; i<problem.getNumberOfObjectives(); i++) {
				for(int j=i+1; j<problem.getNumberOfObjectives(); j++) {

					double[][] xMatrix = new double[algorithms.length][];
					double[][] yMatrix = new double[algorithms.length][];

					for(int k=0; k<algorithms.length; k++) {

						xMatrix[k] = MyArrayUtils.convertToDoublePrimitive(optimumObjectiveMapList.get(i).get(algorithms[k]));
						yMatrix[k] = MyArrayUtils.convertToDoublePrimitive(optimumObjectiveMapList.get(j).get(algorithms[k]));

					}

					MyChartToFileUtils.scatterPlot(
							xMatrix,
							yMatrix, 
							0.0, 1.0, 0.0, 1.0, 
							"Objective 1", "Objective 2", "", "",
							algorithms,
							subFolderPath,
							("ParetoFront"), 
							true,
							true
							);
				}
			}
		}

		System.out.println("\n\tSaving Optimum Variable values to file ...");
		List<List<Double[]>> variableArraysList = new ArrayList<>();
		List<List<String>> variableNameList = new ArrayList<>();
		for(int i=0; i<algorithms.length; i++) {
			List<Double[]> currentVariableArraysList = new ArrayList<>();
			for(int j=0; j<problem.getNumberOfVariables(); j++) {
				currentVariableArraysList.add(
						MyArrayUtils.convertListOfDoubleToDoubleArray(
								optimumVariableMapList.get(j).get(algorithms[i])
								)
						);
			}
			variableArraysList.add(currentVariableArraysList);
			variableNameList.add(Arrays.asList(new String[] {"Variable 1" , "Variable 2"}));
		}
		
		JPADStaticWriteUtils.exportToCSV(
				variableArraysList,
				Arrays.asList(algorithms), 
				variableNameList,
				MyConfiguration.createNewFolder(
						subFolderPath
						+ File.separator 
						+ "Optimum Variable Values" 
						)
				);
		
		System.out.println("\n\n\tVARIABLES:");
		for(int i=0; i<algorithms.length; i++) {
			System.out.println("\tOptimized Variables with algorithm "  
					+ algorithms[i] + ":");
			for(int j=0; j<resultList.get(i).size(); j++) {
				System.out.print("\tSolution " + (j+1) + " :	");
				for(int k=0; k<resultList.get(i).get(j).getNumberOfVariables(); k++) {
					if(!resultList.get(i).get(j).violatesConstraints())
						System.out.print(resultList.get(i).get(j).getVariable(k) + "	");
					else
						System.err.print(resultList.get(i).get(j).getVariable(k) + "	");
				}
				System.out.println("");
			}
		}
		
		System.out.println("\tSaving Optimum Objective values to file ...");
		List<List<Double[]>> objectiveArraysList = new ArrayList<>();
		List<List<String>> objectiveNameList = new ArrayList<>();
		for(int i=0; i<algorithms.length; i++) {
			List<Double[]> currentObjectiveArraysList = new ArrayList<>();
			for(int j=0; j<problem.getNumberOfObjectives(); j++) {
				currentObjectiveArraysList.add(
						MyArrayUtils.convertListOfDoubleToDoubleArray(
								optimumObjectiveMapList.get(j).get(algorithms[i])
								)
						);
			}
			objectiveArraysList.add(currentObjectiveArraysList);
			objectiveNameList.add(Arrays.asList(new String[] {"Objective 1"}));
		}

		JPADStaticWriteUtils.exportToCSV(
				objectiveArraysList,
				Arrays.asList(algorithms), 
				objectiveNameList,
				MyConfiguration.createNewFolder(
						subFolderPath
						+ File.separator 
						+ "Optimum Objective Values" 
						)
				);

		System.out.println("\n\tOBJECTIVES:");
		for(int i=0; i<algorithms.length; i++) {
			System.out.println("\tOptimized Objectives with algorithm " 
					+ algorithms[i] + ":");
			for(int j=0; j<resultList.get(i).size(); j++) {
				System.out.print("\tSolution " + (j+1) + " :	");
				for(int k=0; k<resultList.get(i).get(j).getNumberOfObjectives(); k++) {
					if(!resultList.get(i).get(j).violatesConstraints())
						System.out.print(resultList.get(i).get(j).getObjective(k) + "	");
					else
						System.err.print(resultList.get(i).get(j).getObjective(k) + "	");
				}
				System.out.println("");
			}
		}
		
		System.out.println("\n\tDone!! \n");

		DecimalFormat numberFormat = new DecimalFormat("0.000");
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("\n\tTIME ESTIMATED = " + numberFormat.format(estimatedTime*0.001) + " seconds");

		System.exit(1);
	}
}
