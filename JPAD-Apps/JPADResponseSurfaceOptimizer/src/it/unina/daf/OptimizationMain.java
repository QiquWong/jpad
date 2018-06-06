package it.unina.daf;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import writers.JPADStaticWriteUtils;

class MyArgumentsOptimization {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "Input file")
	private File _inputFile;
	
	@Option(name = "-rs", aliases = { "--response_surface" }, required = true,
			usage = "Response Surfrace file")
	private File _responseSurfaceFile;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}
	
	public File getResponseSurfaceFile() {
		return _responseSurfaceFile;
	}

}

public class OptimizationMain  {

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
	public static void main(String[] args) throws InvalidFormatException, ClassNotFoundException, IOException {

		DecimalFormat numberFormat = new DecimalFormat("0.000");
		long startTime = System.currentTimeMillis();        

		System.out.println("\t-------------------");
		System.out.println("\tMOEA Framework Test");
		System.out.println("\t-------------------");

		MyArgumentsOptimization va = new MyArgumentsOptimization();
		OptimizationMain.theCmdLineParser = new CmdLineParser(va);
		MyConfiguration.initWorkingDirectoryTree(
				MyConfiguration.currentDirectoryString,
				MyConfiguration.inputDirectory, 
				MyConfiguration.outputDirectory
				); 
		String outputPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
		FileUtils.cleanDirectory(new File(outputPath)); 
		
		try {
			OptimizationMain.theCmdLineParser.parseArgument(args);

			String inputFilePath = va.getInputFile().getAbsolutePath();
			System.out.println("\tINPUT FILE ===> " + inputFilePath);
			
			String responseSurfacePath = va.getResponseSurfaceFile().getAbsolutePath();
			System.out.println("\tRESPONSE SURFACE FILE ===> " + responseSurfacePath);


			System.out.println("\t--------------");

			////////////////////////////////////////////////////////////////////////
			// Optimization ...
			System.out.println("\n\n\tRunning JPAD response surface optimization ... \n");
			
			//......................................................................
			// Defining the optimization problem ...
			InputManagerInterface inputManager = MOEAProblemResponseSurface.importInputFile(inputFilePath);
			
			MOEAProblemResponseSurface problem = new MOEAProblemResponseSurface(
					inputManager.getNumberOfVariables(),
					inputManager.getNumberOfObjectives(),
					inputManager.getNumberOfConstraints()
					);
			problem.setMaximizationProblemConditionArray(inputManager.getMaximizationProblemConditionArray());
			problem.importResponseSurface(responseSurfacePath);
			problem.setVariablesLowerBounds(inputManager.getVariablesLowerBounds());
			problem.setVariablesUpperBounds(inputManager.getVariablesUpperBounds());
			problem.setConstraintsDictionary(inputManager.getConstraintsDictionary());
			
			//......................................................................
			// Defining the optimization problem ...
			List<NondominatedPopulation> resultList = new ArrayList<>();
			for(int i=0; i<inputManager.getAlgorithms().length; i++) {
				
				System.out.println("\n\tRunning " + inputManager.getAlgorithms()[i] + " algorithm ...");
				
				long algorithmStartTime = System.currentTimeMillis();   
				
				resultList.add(new Executor()
						.withAlgorithm(inputManager.getAlgorithms()[i])
						.withProblem(problem)
						.withMaxEvaluations(inputManager.getMaximumNumberOfEvaluations())
						.withProperty("population.size", String.valueOf(inputManager.getPopulationSize()))
						.run()
						);
				
				long algorithmEstimatedTime = System.currentTimeMillis() - algorithmStartTime;
				
				System.out.println("\tDone!!");
				System.out.println("\tTIME ESTIMATED " + inputManager.getAlgorithms()[i] + " = " + numberFormat.format(algorithmEstimatedTime*0.001) + " seconds");
			}

			//......................................................................
			// Print results and plots
			Map<Integer, Map<String, List<Double>>> optimumObjectiveMapList = new HashMap<>();
			List<Double> optimumCurrentObjectiveValues = new ArrayList<>();
			Map<String, List<Double>> optimumCurrentObjectiveMap = new HashMap<>();

			for (int obj=0; obj<problem.getNumberOfObjectives(); obj++) {
				optimumCurrentObjectiveMap = new HashMap<>();
				for (int i=0; i<inputManager.getAlgorithms().length; i++) {
					optimumCurrentObjectiveValues = new ArrayList<>();
					for (int j=0; j<resultList.get(i).size(); j++) {
						if (!resultList.get(i).get(j).violatesConstraints()) 
						optimumCurrentObjectiveValues.add(resultList.get(i).get(j).getObjective(obj));
					}
					optimumCurrentObjectiveMap.put(inputManager.getAlgorithms()[i], optimumCurrentObjectiveValues);
				}
				optimumObjectiveMapList.put(obj, optimumCurrentObjectiveMap);
			}
			
			Map<Integer, Map<String, List<Double>>> optimumVariableMapList = new HashMap<>();
			List<Double> optimumCurrentVariableValues = new ArrayList<>();
			Map<String, List<Double>> optimumCurrentVariableMap = new HashMap<>();
			
			for (int var=0; var<problem.getNumberOfVariables(); var++) {
				optimumCurrentVariableMap = new HashMap<>();
				for (int i=0; i<inputManager.getAlgorithms().length; i++) {
					optimumCurrentVariableValues = new ArrayList<>();
					for (int j=0; j<resultList.get(i).size(); j++) {
						if (!resultList.get(i).get(j).violatesConstraints()) 
							optimumCurrentVariableValues.add(Double.valueOf(resultList.get(i).get(j).getVariable(var).toString()));
					}
					optimumCurrentVariableMap.put(inputManager.getAlgorithms()[i], optimumCurrentVariableValues);
				}
				optimumVariableMapList.put(var, optimumCurrentVariableMap);
			}

			if(problem.getNumberOfObjectives() > 1) {
				System.out.println("\n\tCreating Pareto Fronts and printing results ...");
				for(int i=0; i<problem.getNumberOfObjectives(); i++) {
					for(int j=i+1; j<problem.getNumberOfObjectives(); j++) {

						double[][] xMatrix = new double[inputManager.getAlgorithms().length][];
						double[][] yMatrix = new double[inputManager.getAlgorithms().length][];

						for(int k=0; k<inputManager.getAlgorithms().length; k++) {

							if(problem.getMaximizationProblemConditionArray()[i] == false)
								xMatrix[k] = MyArrayUtils.convertToDoublePrimitive(optimumObjectiveMapList.get(i).get(inputManager.getAlgorithms()[k]));
							else
								xMatrix[k] = MyArrayUtils.convertToDoublePrimitive(
										optimumObjectiveMapList.get(i).get(inputManager.getAlgorithms()[k]).stream()
										.map(d -> -d)
										.collect(Collectors.toList())
										);

							if(problem.getMaximizationProblemConditionArray()[j] == false)
								yMatrix[k] = MyArrayUtils.convertToDoublePrimitive(optimumObjectiveMapList.get(j).get(inputManager.getAlgorithms()[k]));
							else
								yMatrix[k] = MyArrayUtils.convertToDoublePrimitive(
										optimumObjectiveMapList.get(j).get(inputManager.getAlgorithms()[k]).stream()
										.map(d -> -d)
										.collect(Collectors.toList())
										);

						}

						MyChartToFileUtils.scatterPlot(
								xMatrix,
								yMatrix, 
								null, null, null, null, 
								problem.getObjectivesLabelArray()[i], problem.getObjectivesLabelArray()[j], "", "",
								inputManager.getAlgorithms(),
								outputPath,
								("ParetoFront_" 
										+ problem.getObjectivesLabelArray()[i] + "_" 
										+ problem.getObjectivesLabelArray()[j]
										), 
								true,
								true
								);
					}
				}
			}

			System.out.println("\n\tSaving Optimum Variable and Objective values to file ...");
			List<List<Double[]>> variableAndObjectiveArraysList = new ArrayList<>();
			List<List<String>> variableAndObjectiveNameList = new ArrayList<>();
			for(int i=0; i<inputManager.getAlgorithms().length; i++) {
				List<Double[]> currentVariableAndObjectiveArraysList = new ArrayList<>();
				for(int j=0; j<problem.getNumberOfVariables(); j++) 
					currentVariableAndObjectiveArraysList.add(
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									optimumVariableMapList.get(j).get(inputManager.getAlgorithms()[i])
									)
							);
				for(int j=0; j<problem.getNumberOfObjectives(); j++) {
					if(problem.getMaximizationProblemConditionArray()[j] == true)
						currentVariableAndObjectiveArraysList.add(
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										optimumObjectiveMapList.get(j).get(inputManager.getAlgorithms()[i]).stream()
										.map(d -> -d)
										.collect(Collectors.toList())
										)
								);
					else
						currentVariableAndObjectiveArraysList.add(
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										optimumObjectiveMapList.get(j).get(inputManager.getAlgorithms()[i])
										)
								);
				}
				variableAndObjectiveArraysList.add(currentVariableAndObjectiveArraysList);
				
				List<String> labelList = new ArrayList<>();
				labelList.addAll(Arrays.asList(problem.getVariablesLabelArray()));
				labelList.addAll(Arrays.asList(problem.getObjectivesLabelArray()));
				variableAndObjectiveNameList.add(labelList);
			}
			
			JPADStaticWriteUtils.exportToCSV(
					variableAndObjectiveArraysList,
					Arrays.asList(inputManager.getAlgorithms()), 
					variableAndObjectiveNameList,
					MyConfiguration.createNewFolder(
							outputPath
							+ File.separator 
							+ "Optimum Variable and Objective Values" 
							)
					);
			
			System.out.println("\n\tDone!! \n");

			long estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println("\n\tTOTAL TIME ESTIMATED = " + numberFormat.format(estimatedTime*0.001) + " seconds");

		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			OptimizationMain.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	

		System.exit(1);
	}
}
