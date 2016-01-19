package sandbox.vc.dirstab;

import java.io.File;

import com.google.common.io.Files;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;

public class Test_VC_DirStab_02 {


	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("No input file name given. Terminating.");
			return;
		}

		String fileNameWithPathAndExt = args[0];

		// Set the folders tree
		MyConfiguration.initWorkingDirectoryTree(MyConfiguration.currentDirectoryString,
											     MyConfiguration.inputDirectory, 
												 MyConfiguration.outputDirectory,
												 MyConfiguration.databaseDirectory);

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
				fileNameWithPathAndExt, 
				outputFileNameWithPathAndExt);

		System.out.println("Done.");

	}

}
