package it.unina.daf.jpad.testtemplate;

import java.io.File;
import javax.xml.parsers.ParserConfigurationException;
import configuration.MyConfiguration;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.JPADXmlReader.Status;

public class AppIO {
	
	private static String inputDir = "in";
	public static String inputDirectory;
	
	private static String outputDir = "out";
	public static String outputDirectory;
	
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

			// update global values of directory paths
			inputDirectory = MyConfiguration.currentDirectoryString + File.separator + inputDir + File.separator;
			outputDirectory = MyConfiguration.currentDirectoryString + File.separator + outputDir + File.separator;
			
			try {
				String out = MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(), "//output_folder/@value");
				if (!out.isEmpty()) outputDir = out;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("\tOutput folder set to default: " + outputDir);
			}

			try {
				String outfile = MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(), "//output_file/@value");
				if (!outfile.isEmpty()) outputFilename = outfile;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("\tOutput file name set to default: " + outputFilename);
			}
			
			outputFilenameFullPath = MyConfiguration.currentDirectoryString + File.separator + outputFilename;

			try {
				String charts = MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(), "//output_chart_folder/@value");
				if (!charts.isEmpty()) outputChartDir = outputDir + File.separator + charts;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("\tOutput chart folder set to default: " + outputChartDir);
			}
			outputChartDirectory = MyConfiguration.currentDirectoryString + File.separator + outputChartDir + File.separator;
			
		}
		System.out.println(message.toString());
	}

	public static String getInputDirectory() {
		return inputDirectory;
	}

	public static String getOutputDirectory() {
		return outputDirectory;
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
