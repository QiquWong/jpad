package sandbox.vc.dirstab;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.google.common.io.Files;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import standaloneutils.JPADXmlReader;

public class Test_VC_DirStab_03 {
	
	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;
	
	@Option(name = "-d", aliases = { "--database-path" }, required = true,
			usage = "path for database files")
	private File _databasePath;

	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	public Test_VC_DirStab_03 (){
		theCmdLineParser = new CmdLineParser(this);
	}
	
	public static void main(String[] args) throws CmdLineException, InstantiationException, IllegalAccessException {

		// Allocate the main object
		Test_VC_DirStab_03 theTestObject = new Test_VC_DirStab_03();
		
		// Arguments check
//		if (args.length < 2){
//			System.err.println("NO INPUT FILE GIVEN, No database path given--> TERMINATING");
//			return;
//		}
		
		theTestObject.theCmdLineParser.parseArgument(args);
		String fileNameWithPathAndExt = theTestObject.get_inputFile().getAbsolutePath();
		String databaseDirectoryAbsolutePath = theTestObject.get_databasePath().getAbsolutePath();
		
		System.out.println("File name: " + fileNameWithPathAndExt);
		System.out.println("Database path: " + databaseDirectoryAbsolutePath);
		
		
		// Set the folders tree
		MyConfiguration.initWorkingDirectoryTree(MyConfiguration.currentDirectoryString,
											     MyConfiguration.inputDirectory, 
												 MyConfiguration.outputDirectory,
												 databaseDirectoryAbsolutePath); // coming from main arguments
//												 MyConfiguration.databaseDirectory);

		// Set database name	
		String veDSCDatabaseFileName = "VeDSC_database.h5";
		String fusDesDatabaseFileName = "FusDes_database.h5";

		File inputFile = new File(fileNameWithPathAndExt);
		String inputFileName = new String(inputFile.getName());

		if (!inputFile.exists()) {
			System.out.println("Input file " + fileNameWithPathAndExt + " not found! Terminating.");
			return;
		}
		if (inputFile.isDirectory()) {
			System.out.println("Input string " + fileNameWithPathAndExt + " is not a file. Terminating.");
			return;
		}

		System.out.println("Input file found. Running ...");

		String outputFileNameWithPathAndExt = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) + File.separator + 
				Files.getNameWithoutExtension(inputFileName) + "Out.xml";

		DirStabCalc.executeStandaloneDirStab(veDSCDatabaseFileName,  
				fusDesDatabaseFileName,
				databaseDirectoryAbsolutePath,
				fileNameWithPathAndExt, 
				outputFileNameWithPathAndExt);

		System.out.println("Done.");
		 
	}

	public File get_inputFile() {
		return _inputFile;
	}

	public File get_databasePath() {
		return _databasePath;
	}
}
