package database.databasefunctions;

import java.io.File;
import java.net.URL;

import standaloneutils.database.hdf.MyHDFReader;

public abstract class DatabaseReader {

	protected String databaseFolderPath, databaseFileName;

	protected URL databaseFolder;
	protected MyHDFReader database;

	/**
	 * This class is the father of all the classes used to read from the h5 databases.
	 * The database which has to be read must be located in the databaseFolderPath.
	 * 
	 * When exporting the project as an executable jar, the h5 database file which has to
	 * be read MUST be in a folder equal to databaseFolderPath, which in turn has to be 
	 * located in the same folder of the jar file.
	 * 
	 * @param databaseFolderPath
	 * @param databaseFileName
	 */
	public DatabaseReader(String databaseFolderPath, String databaseFileName) {

		this.databaseFolderPath = databaseFolderPath;
		this.databaseFileName = databaseFileName;

		//		if (!databaseFolderPath.endsWith("/")) databaseFolderPath = databaseFolderPath + "/";
		if (!databaseFolderPath.endsWith(File.separator)) databaseFolderPath = databaseFolderPath + File.separator;

		String databaseFileNameWithPath = databaseFolderPath + databaseFileName;

		database = new MyHDFReader(databaseFileNameWithPath);

		/**
		 *		Old snippet
		 * 
		 * 		databaseFolder = getClass().getResource(databaseFileNameWithPath);
				database = new MyHDFReader(databaseFolder.getPath());		
		 */

	}
	
}
