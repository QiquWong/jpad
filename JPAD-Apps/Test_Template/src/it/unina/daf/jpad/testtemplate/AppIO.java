package it.unina.daf.jpad.testtemplate;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import javax.measure.quantity.Length;
import javax.xml.parsers.ParserConfigurationException;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.FoldersEnum;
import javaslang.Tuple2;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.JPADXmlReader.Status;

public class AppIO {

	public enum FoldersEnum {	
		CURRENT_DIR,	
		INPUT_DIR,
		DATABASE_DIR,
		OUTPUT_DIR,
		OUTPUT_CHART_DIR;
	}
	
	private static HashMap<FoldersEnum, String> mapPaths = new HashMap<FoldersEnum, String>();

	public static final String currentDirectoryString = System.getProperty("user.dir");
	
	private static String inputDir = "in";
	public static String inputDirectory;

	private static String inputFilename = "input.xml";
	private static String inputFilenameFullPath;
	
	private static String outputDir = "out";
	public static String outputDirectory;

	private static String databaseDir = "data";
	public static String databaseDirectory;
	
	private static String outputFilename = "output.xml";
	private static String outputFilenameFullPath;
	
	private static String outputChartDir = "out" + File.separator + "charts";
	private static String outputChartDirectory;
	
	static InputOutputTree input = new InputOutputTree();

	/**
	 * Read the file <user-dir>/config/config.xml
	 * and initialize the map of folders
	 * 
	 * @author Agostino De Marco 
	 */	
	public static void parseConfig() throws ParserConfigurationException {
		
		String pathToXML = "config/config.xml";
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		StringBuilder message = new StringBuilder()
				.append("-------------------------------------\n")
				.append("Read status: ")
					.append(reader.getStatus()).append("\n")
				.append("-------------------------------------")
				;
		
		if (reader.getStatus().equals(Status.PARSED_OK)) {
			
			try {
				String in = MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(), "//input_dir/@value");
				if (!in.isEmpty()) inputDir = in;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("\tInput folder set to default: " + inputDir);
			}

			// update global value
			inputDirectory = currentDirectoryString + File.separator + inputDir;

			try {
				String infile = MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(), "//input_file/@value");
				if (!infile.isEmpty()) inputFilename = infile;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("\tInput file name set to default: " + inputFilename);
			}
			
			// update global value
			inputFilenameFullPath = inputDirectory  + File.separator + inputFilename;
			
			try {
				String out = MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(), "//output_dir/@value");
				if (!out.isEmpty()) outputDir = out;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("\tOutput folder set to default: " + outputDir);
			}

			// update global value
			outputDirectory = currentDirectoryString + File.separator + outputDir;

			try {
				String db = MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(), "//database_dir/@value");
				if (!db.isEmpty()) databaseDir = db;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("\tDatabase folder set to default: " + databaseDir);
			}

			// update global value
			databaseDirectory = currentDirectoryString + File.separator + databaseDir;
			
			try {
				String outfile = MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(), "//output_file/@value");
				if (!outfile.isEmpty()) outputFilename = outfile;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("\tOutput file name set to default: " + outputFilename);
			}
			
			// update global value
			outputFilenameFullPath = outputDirectory + File.separator + outputFilename;

			try {
				String charts = MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(), "//output_chart_dir/@value");
				if (!charts.isEmpty()) outputChartDir = outputDir + File.separator + charts;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("\tOutput chart folder set to default: " + outputChartDir);
			}
			// update global value
			outputChartDirectory = currentDirectoryString + File.separator + outputChartDir;
			
		}
		System.out.println(message.toString());
		
		// initWorkingDirectoryTree();
		// 
		// NOTE: let the user create the directories by calling this
		//       function explicitly
		
		// initialize the map of folders 
		mapPaths.put(FoldersEnum.CURRENT_DIR, currentDirectoryString);
		mapPaths.put(FoldersEnum.DATABASE_DIR, databaseDirectory);
		mapPaths.put(FoldersEnum.INPUT_DIR, inputDirectory);
		mapPaths.put(FoldersEnum.OUTPUT_DIR, outputDirectory);
		mapPaths.put(FoldersEnum.OUTPUT_CHART_DIR, outputChartDirectory);

	}

	public static String createNewFolder(String path) {
		File folder = new File(path);

		if (folder.exists()) {
			System.out.println("\tFolder " + path + " exists, not created.");
			return path;
		}

		try{
			if(folder.mkdir() && !folder.exists()) return path;
			else return path;
		} catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Initialize the working directory tree, i.e. creates the folders
	 * listed currently in the map of folders.
	 * @return mapPaths HashMap<MyConfiguration.FoldersEnum, String>
	 * 
	 * @author Agostino De Marco 
	 */
	public static HashMap<FoldersEnum, String> initWorkingDirectoryTree(){

		// Create the folder from map values
		mapPaths.entrySet().stream()
			.filter(e -> !e.getKey().equals(FoldersEnum.CURRENT_DIR)) // do not try to create the current dir
			.forEach(
				//				e -> System.out.println(e.getKey() + ": " + e.getValue())
				e -> createNewFolder(e.getValue())
				);

		return mapPaths;
	}	
	
	public static String reportDirectories(){
		
		StringBuilder sb = new StringBuilder()
				.append("AppIO folders:\n")
				.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
		mapPaths.entrySet().stream()
			.forEach(entry -> 
				sb.append(entry.getKey() + ": " + entry.getValue() +"\n")
			);
		return sb.toString();
	}
	public static String reportFiles(){
		
		StringBuilder sb = new StringBuilder()
				.append("AppIO files:\n")
				.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
		sb
			.append("Input file: " + AppIO.getInputFilenameFullPath() +"\n")
			.append("Output file: " + AppIO.getOutputFilenameFullPath() +"\n")
			;
		return sb.toString();
	}

	public static String reportAll(){
		StringBuilder sb = new StringBuilder()
				.append(reportDirectories() + "\n")
				.append(reportFiles())
				;
		//sb.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
		return sb.toString();
	}
	
	@SafeVarargs
	/**
	 * Puts a set of (key,value) pairs into the map of folders and creates the directories.
	 * Only keys enumerated in AppIO.FoldersEnum are filtered and inserted.
	 * Previous pairs already in the map are replaced.
	 * The directories given by the filtered values are also created (if legal paths). 
	 * @return mapPaths HashMap<MyConfiguration.FoldersEnum, String>
	 * 
	 * @author Agostino De Marco 
	 */
	public static HashMap<FoldersEnum, String> initWorkingDirectoryTree(
			Tuple2<FoldersEnum, String> ... keyValuePairs){
		Arrays.asList(keyValuePairs).stream()
			.filter(kv -> Arrays.asList(FoldersEnum.values()).contains(kv._1))
			.filter(kv -> !kv._1.equals(FoldersEnum.CURRENT_DIR)) // do not try to create the current dir
			.forEach(kv -> {
				System.out.println("1: " + kv._1 + ", 2: " + kv._2);
				mapPaths.put(kv._1, kv._2);
				createNewFolder(kv._2);
			});
		return mapPaths;
	}

	@SafeVarargs
	/**
	 * Puts a set of (key,value) pairs into the map of folders.
	 * Only keys enumerated in AppIO.FoldersEnum are filtered and inserted.
	 * Previous pairs already in the map are replaced. 
	 * @return mapPaths HashMap<MyConfiguration.FoldersEnum, String>
	 * 
	 * @author Agostino De Marco 
	 */
	public static HashMap<FoldersEnum, String> putWorkingDirectoryTree(
			Tuple2<FoldersEnum, String> ... keyValuePairs){
		Arrays.asList(keyValuePairs).stream()
			.filter(kv -> Arrays.asList(FoldersEnum.values()).contains(kv._1))
			.forEach(kv -> {
				mapPaths.put(kv._1, kv._2);
			});
		return mapPaths;
	}
	
	public static HashMap<FoldersEnum, String> getMapPaths() {
		return mapPaths;
	}

	public static String getDirectory(FoldersEnum dir){
		return mapPaths.get(dir);
	}

	public static String getInputDirectory() {
		return inputDirectory;
	}
	public static void setInputDirectory(String inputDirectory) {
		AppIO.inputDirectory = inputDirectory;
	}

	public static String getInputFilename() {
		return inputFilename;
	}
	public static String getInputFilenameFullPath() {
		return inputFilenameFullPath;
	}
	public static void setInputFilenameFullPath(String inputFilenameFullPath) {
		AppIO.inputFilenameFullPath = inputFilenameFullPath;
	}
	
	public static String getOutputDirectory() {
		return outputDirectory;
	}
	public static void setOutputDirectory(String outputDirectory) {
		AppIO.outputDirectory = outputDirectory;
	}

	public static String getDatabaseDirectory() {
		return databaseDirectory;
	}
	public static void setDatabaseDirectory(String databaseDirectory) {
		AppIO.databaseDirectory = databaseDirectory;
	}
	
	public static String getOutputFilename() {
		return outputFilename;
	}
	public static String getOutputFilenameFullPath() {
		return outputFilenameFullPath;
	}
	public static void setOutputFilenameFullPath(String outputFilenameFullPath) {
		AppIO.outputFilenameFullPath = outputFilenameFullPath;
	}
	
	public static String getOutputChartDirectory() {
		return outputChartDirectory;
	}
	public static void setOutputChartDirectory(String outputChartDirectory) {
		AppIO.outputChartDirectory = outputChartDirectory;
	}
	
}
