package database.databasefunctions.aerodynamics;

import java.io.File;
import java.util.Collections;
import java.util.List;

import configuration.MyConfiguration;
import configuration.enumerations.FusDesDatabaseEnum;
import configuration.enumerations.VeDSCDatabaseEnum;
import database.databasefunctions.DatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.database.io.DatabaseFileReader;
import standaloneutils.database.io.DatabaseIOmanager;
import writers.JPADStaticWriteUtils;

public class AerodynamicsDatabaseManager {

	/**
	 *   
	 * @param reader
	 * @return
	 */
	
	public static VeDSCDatabaseReader initializeVeDSC(VeDSCDatabaseReader reader){
		//		// TODO: two functions initializeFusDes, initializeVeDSC
		//
		//		/**
		//		 * This block is executed only once: the first time the current class is called
		//		 */
		//		 static {

		// Serialization of the database to an xml file to spare time in interpolating the data
		File fileVeDSC = new File(MyConfiguration.interpolaterVeDSCDatabaseSerializedFullName); 

		if (fileVeDSC.exists()) {

			System.out.println("De-serializing file: " + fileVeDSC.getAbsolutePath() + " ...");
			reader = (VeDSCDatabaseReader) MyXMLReaderUtils.deserializeObject(reader,
					MyConfiguration.interpolaterVeDSCDatabaseSerializedFullName);

		} else {

			System.out.println(
					"Serializing file " + "==> VeDSC_database.h5 ==> "
							+ fileVeDSC.getAbsolutePath() + " ...");
			reader = new VeDSCDatabaseReader(
					MyConfiguration.databaseFolderPath,"VeDSC_database.h5");

			File dir = new File(MyConfiguration.interpolaterVeDSCDatabaseSerializedDirectory);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			JPADStaticWriteUtils.serializeObject(reader, 
					MyConfiguration.interpolaterVeDSCDatabaseSerializedDirectory,
					MyConfiguration.interpolaterVeDSCDatabaseSerializedName);
		}
		return reader; 
	}
	
	// Overload
	
	public static VeDSCDatabaseReader initializeVeDSC(VeDSCDatabaseReader reader, String databaseDirectory){

		// Serialization of the database to an xml file to spare time in interpolating the data
		
		// Set the database folders
		String interpolaterVeDSCDatabaseSerializedDirectory = databaseDirectory + File.separator + "serializedDatabase" + File.separator;
		String interpolaterVeDSCDatabaseSerializedFullName = interpolaterVeDSCDatabaseSerializedDirectory 
				+ MyConfiguration.interpolaterVeDSCDatabaseSerializedName; 
		
		File fileVeDSC = new File(interpolaterVeDSCDatabaseSerializedFullName); 

		if (fileVeDSC.exists()) {

			System.out.println("De-serializing file: " + fileVeDSC.getAbsolutePath() + " ...");
			reader = (VeDSCDatabaseReader) MyXMLReaderUtils.deserializeObject(reader,
					interpolaterVeDSCDatabaseSerializedFullName);

		} else {

			System.out.println(
					"Serializing file " + "==> VeDSC_database.h5 ==> "
							+ fileVeDSC.getAbsolutePath() + " ...");
			reader = new VeDSCDatabaseReader(
					MyConfiguration.databaseFolderPath,"VeDSC_database.h5");

			File dir = new File(interpolaterVeDSCDatabaseSerializedDirectory);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			JPADStaticWriteUtils.serializeObject(reader, 
					interpolaterVeDSCDatabaseSerializedDirectory,
					MyConfiguration.interpolaterVeDSCDatabaseSerializedName);
		}
		return reader; 
	}

	
	
	// TODO: put here other static final database objects

/**
 * Fuselage database
 * 
 * @param reader
 * @return
 */
	public static FusDesDatabaseReader initializeFusDes(FusDesDatabaseReader reader){

		File fileFusDes = new File(MyConfiguration.interpolaterFusDesatabaseSerializedFullName);

		if(fileFusDes.exists()){
			System.out.println("De-serializing file: " + fileFusDes.getAbsolutePath() + " ...");
			reader = (FusDesDatabaseReader) 
					MyXMLReaderUtils.deserializeObject(reader,
							MyConfiguration.interpolaterFusDesatabaseSerializedFullName);
		}
		else {
			System.out.println(	"Serializing file " + "==> FusDes_database.h5 ==> "+ 
					fileFusDes.getAbsolutePath() + " ...");
			reader = new FusDesDatabaseReader(MyConfiguration.databaseDirectory,"FusDes_database.h5");


			File dir = new File(MyConfiguration.interpolaterFusDesDatabaseSerializedDirectory);
			if(!dir.exists()){
				dir.mkdirs(); 
			}else{
				JPADStaticWriteUtils.serializeObject(reader, 
						MyConfiguration.interpolaterFusDesDatabaseSerializedDirectory,
						MyConfiguration.interpolaterFusDesDatabaseSerializedName);
			}
		}
		return reader;
	}
	
	
	// Overload
/**
 * This method allows to define an arbitrary folder path for the fuselage database
 * 
 * @param reader
 * @param databaseDirectory
 * @return
 */
	public static FusDesDatabaseReader initializeFusDes(FusDesDatabaseReader reader, String databaseDirectory){

		//Set the fuselage database folder

		String interpolaterFusDesDatabaseSerializedDirectory = databaseDirectory + File.separator + "serializedDatabase" 
				+ File.separator; 
		String interpolaterFusDesatabaseSerializedFullName = interpolaterFusDesDatabaseSerializedDirectory +  
				MyConfiguration.interpolaterFusDesDatabaseSerializedName;
		
		File fileFusDes = new File(interpolaterFusDesatabaseSerializedFullName);

		if(fileFusDes.exists()){
			System.out.println("De-serializing file: " + fileFusDes.getAbsolutePath() + " ...");
			reader = (FusDesDatabaseReader) 
					MyXMLReaderUtils.deserializeObject(reader,
							interpolaterFusDesatabaseSerializedFullName);
		}
		else {
			System.out.println(	"Serializing file " + "==> FusDes_database.h5 ==> "+ 
					fileFusDes.getAbsolutePath() + " ...");
			reader = new FusDesDatabaseReader(MyConfiguration.databaseDirectory,"FusDes_database.h5");


			File dir = new File(interpolaterFusDesDatabaseSerializedDirectory);
			if(!dir.exists()){
				dir.mkdirs(); 
			}else{
				JPADStaticWriteUtils.serializeObject(reader, 
						interpolaterFusDesDatabaseSerializedDirectory,
						MyConfiguration.interpolaterFusDesDatabaseSerializedName);
			}
		}
		return reader;
	}
}

