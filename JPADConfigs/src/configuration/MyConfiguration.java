package configuration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.measure.unit.NonSI;
import javax.measure.unit.UnitFormat;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;

import configuration.enumerations.FoldersEnum;
import javolution.text.TypeFormat;

/**
 * Group together all the settings needed to run the application
 * 
 * @author Lorenzo Attanasio
 *
 */
public class MyConfiguration {

	// Software info
	public final double version = 1.0;
	public final String name = "ADOpT";
	public final String description = "none";
	public final String release = "pre-alpha";

	// Files and folders names
	public static final  String outputFolderName = "out";
	public static final String inputFolderName = "in";
	public static final String databaseFolderName = "data";
	public static final String databasePackageName = "database";

	//final String databaseFolderPath = "/" + databasePackageName + "/" + databaseFolderName;
	public static final String databaseFolderPath = databaseFolderName; // <=== NB !!!

	public static final String cadFolderName = "cad";
	public static final String imagesFolderName = "images";

	public static final String tabAsSpaces = "    ";
	public static final String notInitializedWarning = "not_initialized";

	// Directories
	public static String currentImagesDirectory;
	public static final String currentDirectoryString = System.getProperty("user.dir");
	public static final File currentDirectory = new File(currentDirectoryString);

	public static final String src_it_unina_adopt_Directory = currentDirectoryString
			+ File.separator + "src"
			+ File.separator + "it"
			+ File.separator + "unina"
			+ File.separator + "adopt" 
			+ File.separator;

	public static final  String objects3dDirectory = src_it_unina_adopt_Directory	+ "objects3d" + File.separator;

	public static final String runsDirectory = currentDirectoryString + File.separator + "runs" + File.separator;

	public static final String cadDirectory = currentDirectoryString + File.separator + outputFolderName + File.separator + cadFolderName + File.separator;

	public static final String testDirectory = currentDirectoryString + File.separator + "test" + File.separator;

	public static final String inputDirectory = currentDirectoryString + File.separator + inputFolderName + File.separator;

	public static final String outputDirectory = currentDirectoryString + File.separator + outputFolderName + File.separator;

	public static final String imagesDirectory = outputDirectory + imagesFolderName + File.separator;

	public static final String databaseDirectory = currentDirectoryString + File.separator + databaseFolderName + File.separator;
 
	// Serialized database interpolating functions
	public static final String interpolaterVeDSCDatabaseSerializedDirectory = databaseDirectory + File.separator + "serializedDatabase" 
			+ File.separator; 
	public static final String interpolaterVeDSCDatabaseSerializedName = "interpolaterVeDSCDatabase.xml"; 
	public static final String interpolaterVeDSCDatabaseSerializedFullName = 
			interpolaterVeDSCDatabaseSerializedDirectory + File.separator + interpolaterVeDSCDatabaseSerializedName; 

	//	final String interpolaterFusDesDatabaseSerializedDirectory = currentDirectoryString + File.separator + "database" + File.separator; 
	public static final String interpolaterFusDesDatabaseSerializedDirectory = databaseDirectory + File.separator + "serializedDatabase" 
			+ File.separator; 
	public static final String interpolaterFusDesDatabaseSerializedName = "interpolaterFusDesDatabase.xml"; 
	public static final String interpolaterFusDesatabaseSerializedFullName = 
			interpolaterFusDesDatabaseSerializedDirectory + File.separator + interpolaterFusDesDatabaseSerializedName;
	
	public static final String interpolaterAerodynamicDatabaseSerializedDirectory = databaseDirectory + File.separator + "serializedDatabase" 
			+ File.separator; 
	public static final String interpolaterAerodynamicDatabaseSerializedName = "interpolaterAerodynamicDatabase.xml"; 
	public static final String interpolaterAerodynamicDatabaseSerializedFullName = 
			interpolaterAerodynamicDatabaseSerializedDirectory + File.separator + interpolaterAerodynamicDatabaseSerializedName;
	
	public static final String interpolaterHighLiftDatabaseSerializedDirectory = databaseDirectory + File.separator + "serializedDatabase" 
			+ File.separator; 
	public static final String interpolaterHighLiftDatabaseSerializedName = "interpolaterHighLiftDatabase.xml"; 
	public static final String interpolaterHighLiftDatabaseSerializedFullName = 
			interpolaterHighLiftDatabaseSerializedDirectory + File.separator + interpolaterHighLiftDatabaseSerializedName;

	private static HashMap<FoldersEnum, String>  mapPaths = new HashMap<FoldersEnum, String>();

	
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
	 * 
	 * Customize output format of Amount variables
	 * 
	 * @author Agostino De Marco
	 */

	public static void customizeAmountOutput(){
		
		//============================================================================
		// Trick to write the ".getEstimatedValue() + unit" format
		// http://stackoverflow.com/questions/8514293/is-there-a-way-to-make-jscience-output-in-a-more-human-friendly-format
		UnitFormat uf = UnitFormat.getInstance();
		
		// Customize labels
		uf.label(NonSI.DEGREE_ANGLE, "deg"); // instead of default '°' symbol
		
		
		AmountFormat.setInstance(new AmountFormat() {
		    @Override
		    public Appendable format(Amount<?> m, Appendable a) throws IOException {
		        TypeFormat.format(m.getEstimatedValue(), -1, false, false, a);
		        a.append(" ");
		        return uf.format(m.getUnit(), a);
		    }

		    @Override
		    public Amount<?> parse(CharSequence csq, Cursor c) throws IllegalArgumentException {
		        throw new UnsupportedOperationException("Parsing not supported.");
		    }
		});
	}	
	
	
	/**
	 * Initialize the working directory tree and fill the map of folders
	 * @return mapPaths HashMap<MyConfiguration.FoldersEnum, String>
	 * 
	 * @author Vincenzo Cusati 
	 */
	public static HashMap<FoldersEnum, String> initWorkingDirectoryTree(){

		mapPaths.put(FoldersEnum.CURRENT_DIR, currentDirectoryString);
		mapPaths.put(FoldersEnum.DATABASE_DIR, databaseDirectory);
		mapPaths.put(FoldersEnum.INPUT_DIR, inputDirectory);
		mapPaths.put(FoldersEnum.OUTPUT_DIR, outputDirectory);
		mapPaths.put(FoldersEnum.RUNS_DIR, runsDirectory);
		mapPaths.put(FoldersEnum.CAD_DIR, cadDirectory);
		mapPaths.put(FoldersEnum.IMAGE_DIR, imagesDirectory);
		mapPaths.put(FoldersEnum.TEST_DIR, testDirectory);
		mapPaths.put(FoldersEnum.OBJECTS3D_DIR, objects3dDirectory);

		// Create the folder from map values
		mapPaths.entrySet().stream().forEach(
				//				e -> System.out.println(e.getKey() + ": " + e.getValue())
				e -> createNewFolder(e.getValue())
				);

		return mapPaths;
	}	


	/**
	 * 
	 * @param str  
	 * @return
	 * 
	 * @author Vincenzo Cusati
	 */

	public static HashMap<FoldersEnum, String> initWorkingDirectoryTree(String ... str){

		if(Arrays.asList(str).contains(currentDirectoryString)) mapPaths.put(FoldersEnum.CURRENT_DIR, currentDirectoryString);
		if(Arrays.asList(str).contains(databaseDirectory)) mapPaths.put(FoldersEnum.DATABASE_DIR, databaseDirectory);
		if(Arrays.asList(str).contains(inputDirectory)) mapPaths.put(FoldersEnum.INPUT_DIR, inputDirectory);
		if(Arrays.asList(str).contains(outputDirectory)) mapPaths.put(FoldersEnum.OUTPUT_DIR, outputDirectory);
		if(Arrays.asList(str).contains(runsDirectory)) mapPaths.put(FoldersEnum.RUNS_DIR, runsDirectory);
		if(Arrays.asList(str).contains(cadDirectory)) mapPaths.put(FoldersEnum.CAD_DIR, cadDirectory);
		if(Arrays.asList(str).contains(imagesDirectory)) mapPaths.put(FoldersEnum.IMAGE_DIR, imagesDirectory);

		return mapPaths;
	}


	public static String getDir(FoldersEnum dir){
		return mapPaths.get(dir);
	}




}



// TODO Move string composition of dir name into initWorkingDirectoryTree

//// Software info
//public static final double version = 1.0;
//public static final String name = "ADOpT";
//public static final String description = "none";
//public static final String release = "pre-alpha";
//
//// Files and folders names
//public static final String outputFolderName = "out";
//public static final String inputFolderName = "in";
//public static final String databaseFolderName = "data";
//public static final String databasePackageName = "database";
//
////public static final String databaseFolderPath = "/" + databasePackageName + "/" + databaseFolderName;
//public static final String databaseFolderPath = databaseFolderName; // <=== NB !!!
//
//
//public static final String cadFolderName = "cad";
//public static final String imagesFolderName = "images";
//
//public static final String tabAsSpaces = "    ";
//public static final String notInitializedWarning = "not_initialized";
//
//// Directories
//public static String currentImagesDirectory;
//public static final String currentDirectoryString = System.getProperty("user.dir");
//public static final File currentDirectory = new File(currentDirectoryString);
//
//public static final String src_it_unina_adopt_Directory = currentDirectoryString
//		+ File.separator + "src"
//		+ File.separator + "it"
//		+ File.separator + "unina"
//		+ File.separator + "adopt" 
//		+ File.separator;
//
//public static final String objects3dDirectory = 
//		src_it_unina_adopt_Directory
//		+ "objects3d" + File.separator;
//
//public static final String runsDirectory = 
//		createNewFolder(currentDirectoryString 
//				+ File.separator + "runs" + File.separator);
//
//public static final String cadDirectory = 
//		createNewFolder(currentDirectoryString 
//				+ File.separator + outputFolderName + File.separator + cadFolderName + File.separator);
//
//public static final String testDirectory = 
//		createNewFolder(currentDirectoryString 
//				+ File.separator + "test" + File.separator);
//
//public static final String inputDirectory = 
//		createNewFolder(currentDirectoryString 
//				+ File.separator + inputFolderName + File.separator);
//
//public static final String outputDirectory = 
//		createNewFolder(currentDirectoryString 
//				+ File.separator + outputFolderName + File.separator);	
//
//public static final String imagesDirectory = 
//		createNewFolder(outputDirectory + imagesFolderName + File.separator);
//
//public static final String databaseDirectory = 
//		createNewFolder(currentDirectoryString + File.separator + databaseFolderName + File.separator);
//
////Serialized database interpolating functions
////	public static final String interpolaterVeDSCDatabaseSerializedDirectory = currentDirectoryString + File.separator + "database" + File.separator; 
//public static final String interpolaterVeDSCDatabaseSerializedDirectory = databaseDirectory + File.separator + "serializedDatabase" 
//		+ File.separator; 
//public static final String interpolaterVeDSCDatabaseSerializedName = "interpolaterVeDSCDatabase.xml"; 
//public static final String interpolaterVeDSCDatabaseSerializedFullName = 
//		interpolaterVeDSCDatabaseSerializedDirectory + File.separator + interpolaterVeDSCDatabaseSerializedName; 
//
////	public static final String interpolaterFusDesDatabaseSerializedDirectory = currentDirectoryString + File.separator + "database" + File.separator; 
//public static final String interpolaterFusDesDatabaseSerializedDirectory = databaseDirectory + File.separator + "serializedDatabase" 
//		+ File.separator; 
//public static final String interpolaterFusDesDatabaseSerializedName = "interpolaterFusDesDatabase.xml"; 
//public static final String interpolaterFusDesatabaseSerializedFullName = 
//		interpolaterFusDesDatabaseSerializedDirectory + File.separator + interpolaterFusDesDatabaseSerializedName;

