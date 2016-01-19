package sandbox.vc.fusdesdatabase;

import java.io.File;
import com.google.common.io.Files;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;

public class FusDesMain {

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("No input file name given. Terminating.");
			return;
		}

		//		System.out.println("currentDirectoryString --> " + MyConfiguration.currentDirectoryString);

		// NO-GUI mode
		if (!args[0].equals("-g")) {

			String fileNameWithPathAndExt = args[0];

			// Set the folders tree
			MyConfiguration.initWorkingDirectoryTree();

			String databaseName = "FusDes_database.h5"; 

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

			FusDesDatabaseCalc.executeStandaloneFusDes(
					databaseName,
					fileNameWithPathAndExt, 
					outputFileNameWithPathAndExt);

			System.out.println("Done.");
		}

	}

}
