package it.unina.daf.test.Costs;


import java.io.File;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.google.common.io.Files;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import standaloneutils.JPADXmlReader;

public class Test_Costs_v01 {
	
	//------------------------------------------------------------------------------------------
		// VARIABLE DECLARATION:
		@Option(name = "-i", aliases = { "--input" }, required = true,
				usage = "my input file")
		private File _inputFile;
		
		// declaration necessary for Concrete Object usage
		public CmdLineParser theCmdLineParser;
		public JPADXmlReader reader;
	
		// Builder
		public Test_Costs_v01() {
			theCmdLineParser = new CmdLineParser(this);
			}
	
	public static void main(String[] args) throws CmdLineException {
		
		// Allocate the main object
		Test_Costs_v01 theTestObject = new Test_Costs_v01();
		
		theTestObject.theCmdLineParser.parseArgument(args);
		String inputFileNameWithPathAndExt = theTestObject.get_inputFile().getAbsolutePath();
		
		System.out.println("File name: " + inputFileNameWithPathAndExt);
		
		// Set the folders tree
		MyConfiguration.initWorkingDirectoryTree(MyConfiguration.currentDirectoryString,
											     MyConfiguration.inputDirectory, 
												 MyConfiguration.outputDirectory);


		File inputFile = new File(inputFileNameWithPathAndExt);
		String inputFileName = new String(inputFile.getName());

		if (!inputFile.exists()) {
			System.out.println("Input file " + inputFileNameWithPathAndExt + " not found! Terminating.");
			return;
		}
		if (inputFile.isDirectory()) {
			System.out.println("Input string " + inputFileNameWithPathAndExt + " is not a file. Terminating.");
			return;
		}

		System.out.println("Input file found. Running ...");

		String outputFileNameWithPathAndExt = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) + File.separator + 
				Files.getNameWithoutExtension(inputFileName) + "Out.xml";

		CostsCalc.executeStandaloneCosts(  
				inputFileNameWithPathAndExt, 
				outputFileNameWithPathAndExt);

		System.out.println("Done.");
		 
	}

	public File get_inputFile() {
		return _inputFile;
	}

}
