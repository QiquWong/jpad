package sandbox.vc.PayloadRange;

import java.io.File;
import com.google.common.io.Files;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.FuelFractionDatabaseReader;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

public class Test_PayloadRange_01 {

	public static void main(String[] args) throws HDF5LibraryException, NullPointerException, ClassNotFoundException {

		//--------------------------------------------------------------------------------------
		// Arguments check and initial activities
		if (args.length == 0){
			System.err.println("NO INPUT FILE GIVEN --> TERMINATING");
			return;
		}
		
		String fileNameWithPathAndExt = args[0];
		
		// Set the folders tree
		MyConfiguration.initWorkingDirectoryTree(MyConfiguration.currentDirectoryString,
											     MyConfiguration.inputDirectory, 
												 MyConfiguration.outputDirectory,
												 MyConfiguration.databaseDirectory);
		//--------------------------------------------------------------------------------------
		// Setup database(s)	
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		// TODO Set the engine databases
		String fuelFractionDatabaseFileName = "FuelFractions.h5";
		FuelFractionDatabaseReader fuelFractionReader = new FuelFractionDatabaseReader(databaseFolderPath, fuelFractionDatabaseFileName);
		
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
		
		PayloadRangeCalcSA.executeStandalonePayloadRange(fileNameWithPathAndExt, outputFileNameWithPathAndExt, fuelFractionReader);

		System.out.println("Done.");
		
		
	}

}