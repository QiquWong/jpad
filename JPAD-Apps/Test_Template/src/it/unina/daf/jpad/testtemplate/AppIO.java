package it.unina.daf.jpad.testtemplate;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
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
	
	private static String outputDir = "out";
	public static String outputDirectory;

	private static String databaseDir = "data";
	public static String databaseDirectory;
	
	private static String outputFilename = "output.xml";
	private static String outputFilenameFullPath;
	
	private static String outputChartDir = "out" + File.separator + "charts" + File.separator;
	private static String outputChartDirectory;
	
	static InputOutputTree input = new InputOutputTree();

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
						reader.getXmlDoc(), reader.getXpath(), "//input_folder/@value");
				if (!in.isEmpty()) inputDir = in;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("\tInput folder set to default: " + inputDir);
			}

			// update global value
			inputDirectory = currentDirectoryString + File.separator + inputDir + File.separator;
			
			try {
				String out = MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(), "//output_folder/@value");
				if (!out.isEmpty()) outputDir = out;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("\tOutput folder set to default: " + outputDir);
			}

			// update global value
			outputDirectory = currentDirectoryString + File.separator + outputDir + File.separator;

			try {
				String db = MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(), "//database_folder/@value");
				if (!db.isEmpty()) databaseDir = db;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("\tDatabase folder set to default: " + databaseDir);
			}

			// update global value
			databaseDirectory = currentDirectoryString + File.separator + databaseDir + File.separator;
			
			try {
				String outfile = MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(), "//output_file/@value");
				if (!outfile.isEmpty()) outputFilename = outfile;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("\tOutput file name set to default: " + outputFilename);
			}
			
			// update global value
			outputFilenameFullPath = outputDirectory + outputFilename;

			try {
				String charts = MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(), "//output_chart_folder/@value");
				if (!charts.isEmpty()) outputChartDir = outputDir + File.separator + charts;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("\tOutput chart folder set to default: " + outputChartDir);
			}
			// update global value
			outputChartDirectory = currentDirectoryString + File.separator + outputChartDir + File.separator;
			
		}
		System.out.println(message.toString());
		
		initWorkingDirectoryTree();
	}

	public static String createNewFolder(String path) {
		File folder = new File(path);
		try{
			if(folder.mkdir() && !folder.exists()) return path;
			else return path;

		} catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Initialize the working directory tree and fill the map of folders
	 * @return mapPaths HashMap<MyConfiguration.FoldersEnum, String>
	 * 
	 * @author Agostino De Marco 
	 */
	public static HashMap<FoldersEnum, String> initWorkingDirectoryTree(){

		mapPaths.put(FoldersEnum.CURRENT_DIR, currentDirectoryString);
		mapPaths.put(FoldersEnum.DATABASE_DIR, databaseDirectory);
		mapPaths.put(FoldersEnum.INPUT_DIR, inputDirectory);
		mapPaths.put(FoldersEnum.OUTPUT_DIR, outputDirectory);
		mapPaths.put(FoldersEnum.OUTPUT_CHART_DIR, outputChartDirectory);

		// Create the folder from map values
		mapPaths.entrySet().stream().forEach(
				//				e -> System.out.println(e.getKey() + ": " + e.getValue())
				e -> createNewFolder(e.getValue())
				);

		return mapPaths;
	}	
	
	// TODO: implement setters!!!!
	
	public static String getInputDirectory() {
		return inputDirectory;
	}

	public static String getOutputDirectory() {
		return outputDirectory;
	}

	public static String getDatabaseDirectory() {
		return databaseDirectory;
	}
	
	public static String getOutputFilename() {
		return outputFilename;
	}

	public static String getOutputFilenameFullPath() {
		return outputFilenameFullPath;
	}
	
	public static String getOutputChartDirectory() {
		return outputChartDirectory;
	}
	
}
