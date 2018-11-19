package database;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import configuration.MyConfiguration;
import database.databasefunctions.FuelFractionDatabaseReader;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import database.databasefunctions.engine.EngineDatabaseManager;
import database.databasefunctions.engine.TurbofanEngineDatabaseReader;
import database.databasefunctions.engine.TurbopropEngineDatabaseReader;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

public class DatabaseManager {

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
			reader = (VeDSCDatabaseReader) MyXMLReaderUtils.deserializeObject(
					reader,
					MyConfiguration.interpolaterVeDSCDatabaseSerializedFullName,
					StandardCharsets.UTF_8
					);

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
			reader = (VeDSCDatabaseReader) MyXMLReaderUtils.deserializeObject(
					reader,
					interpolaterVeDSCDatabaseSerializedFullName,
					StandardCharsets.UTF_8
					);

		} else {

			System.out.println(
					"Serializing file " + "==> VeDSC_database.h5 ==> "
							+ fileVeDSC.getAbsolutePath() + " ...");
			reader = new VeDSCDatabaseReader(
					databaseDirectory,"VeDSC_database.h5");

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
					MyXMLReaderUtils.deserializeObject(
							reader,
							MyConfiguration.interpolaterFusDesatabaseSerializedFullName,
							StandardCharsets.UTF_8
							);
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
					MyXMLReaderUtils.deserializeObject(
							reader,
							interpolaterFusDesatabaseSerializedFullName,
							StandardCharsets.UTF_8
							);
		}
		else {
			System.out.println(	"Serializing file " + "==> FusDes_database.h5 ==> "+ 
					fileFusDes.getAbsolutePath() + " ...");
			reader = new FusDesDatabaseReader(databaseDirectory,"FusDes_database.h5");


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

	/**
	 * Aerodynamic database
	 * 
	 * @param reader
	 * @return
	 */
	public static AerodynamicDatabaseReader initializeAeroDatabase(AerodynamicDatabaseReader reader){

		File fileAeroDatabase = new File(MyConfiguration.interpolaterAerodynamicDatabaseSerializedFullName);

		if(fileAeroDatabase.exists()){
			System.out.println("De-serializing file: " + fileAeroDatabase.getAbsolutePath() + " ...");
			reader = (AerodynamicDatabaseReader) 
					MyXMLReaderUtils.deserializeObject(
							reader,
							MyConfiguration.interpolaterAerodynamicDatabaseSerializedFullName,
							StandardCharsets.UTF_8
							);
		}
		else {
			System.out.println(	"Serializing file " + "==> Aerodynamic_Database_Ultimate.h5 ==> "+ 
					fileAeroDatabase.getAbsolutePath() + " ...");
			reader = new AerodynamicDatabaseReader(MyConfiguration.databaseDirectory,"Aerodynamic_Database_Ultimate.h5");


			File dir = new File(MyConfiguration.interpolaterAerodynamicDatabaseSerializedDirectory);
			if(!dir.exists()){
				dir.mkdirs(); 
			}else{
				JPADStaticWriteUtils.serializeObject(reader, 
						MyConfiguration.interpolaterAerodynamicDatabaseSerializedDirectory,
						MyConfiguration.interpolaterAerodynamicDatabaseSerializedName);
			}
		}
		return reader;
	}


	// Overload
	/**
	 * This method allows to define an arbitrary folder path for the aerodynamic database
	 * 
	 * @param reader
	 * @param databaseDirectory
	 * @return
	 */
	public static AerodynamicDatabaseReader initializeAeroDatabase(AerodynamicDatabaseReader reader, String databaseDirectory){

		//Set the aerodynamic database folder

		String interpolaterAerodynamicDatabaseSerializedDirectory = databaseDirectory + File.separator + "serializedDatabase" 
				+ File.separator; 
		String interpolaterAerodynamicDatabaseSerializedFullName = interpolaterAerodynamicDatabaseSerializedDirectory +  
				MyConfiguration.interpolaterAerodynamicDatabaseSerializedName;

		File fileAeroDatabase = new File(interpolaterAerodynamicDatabaseSerializedFullName);

		if(fileAeroDatabase.exists()){
			System.out.println("De-serializing file: " + fileAeroDatabase.getAbsolutePath() + " ...");
			reader = (AerodynamicDatabaseReader) 
					MyXMLReaderUtils.deserializeObject(
							reader,
							interpolaterAerodynamicDatabaseSerializedFullName,
							StandardCharsets.UTF_8
							);
		}
		else {
			System.out.println(	"Serializing file " + "==> Aerodynamic_Database_Ultimate.h5 ==> "+ 
					fileAeroDatabase.getAbsolutePath() + " ...");
			reader = new AerodynamicDatabaseReader(databaseDirectory,"Aerodynamic_Database_Ultimate.h5");


			File dir = new File(interpolaterAerodynamicDatabaseSerializedDirectory);
			if(!dir.exists()){
				dir.mkdirs(); 
			}else{
				JPADStaticWriteUtils.serializeObject(reader, 
						interpolaterAerodynamicDatabaseSerializedDirectory,
						MyConfiguration.interpolaterAerodynamicDatabaseSerializedName);
			}
		}
		return reader;
	}

	/**
	 * High lift database
	 * 
	 * @param reader
	 * @return
	 */
	public static HighLiftDatabaseReader initializeHighLiftDatabase(HighLiftDatabaseReader reader){

		File fileHighLiftDatabase = new File(MyConfiguration.interpolaterHighLiftDatabaseSerializedFullName);

		if(fileHighLiftDatabase.exists()){
			System.out.println("De-serializing file: " + fileHighLiftDatabase.getAbsolutePath() + " ...");
			reader = (HighLiftDatabaseReader) 
					MyXMLReaderUtils.deserializeObject(
							reader,
							MyConfiguration.interpolaterHighLiftDatabaseSerializedFullName,
							StandardCharsets.UTF_8
							);
		}
		else {
			System.out.println(	"Serializing file " + "==> HighLiftDatabase.h5 ==> "+ 
					fileHighLiftDatabase.getAbsolutePath() + " ...");
			reader = new HighLiftDatabaseReader(MyConfiguration.databaseDirectory,"HighLiftDatabase.h5");


			File dir = new File(MyConfiguration.interpolaterHighLiftDatabaseSerializedDirectory);
			if(!dir.exists()){
				dir.mkdirs(); 
			}else{
				JPADStaticWriteUtils.serializeObject(reader, 
						MyConfiguration.interpolaterHighLiftDatabaseSerializedDirectory,
						MyConfiguration.interpolaterHighLiftDatabaseSerializedName);
			}
		}
		return reader;
	}


	// Overload
	/**
	 * This method allows to define an arbitrary folder path for the high lift database
	 * 
	 * @param reader
	 * @param databaseDirectory
	 * @return
	 */
	public static HighLiftDatabaseReader initializeHighLiftDatabase(HighLiftDatabaseReader reader, String databaseDirectory){

		//Set the high lift database folder

		String interpolaterHighLiftDatabaseSerializedDirectory = databaseDirectory + File.separator + "serializedDatabase" 
				+ File.separator; 
		String interpolaterHighLiftDatabaseSerializedFullName = interpolaterHighLiftDatabaseSerializedDirectory +  
				MyConfiguration.interpolaterHighLiftDatabaseSerializedName;

		File fileHighLiftDatabase = new File(interpolaterHighLiftDatabaseSerializedFullName);

		if(fileHighLiftDatabase.exists()){
			System.out.println("De-serializing file: " + fileHighLiftDatabase.getAbsolutePath() + " ...");
			reader = (HighLiftDatabaseReader) 
					MyXMLReaderUtils.deserializeObject(
							reader,
							interpolaterHighLiftDatabaseSerializedFullName,
							StandardCharsets.UTF_8
							);
		}
		else {
			System.out.println(	"Serializing file " + "==> HighLiftDatabase.h5 ==> "+ 
					fileHighLiftDatabase.getAbsolutePath() + " ...");
			reader = new HighLiftDatabaseReader(databaseDirectory,"HighLiftDatabase.h5");


			File dir = new File(interpolaterHighLiftDatabaseSerializedDirectory);
			if(!dir.exists()){
				dir.mkdirs(); 
			}else{
				JPADStaticWriteUtils.serializeObject(reader, 
						interpolaterHighLiftDatabaseSerializedDirectory,
						MyConfiguration.interpolaterHighLiftDatabaseSerializedName);
			}
		}
		return reader;
	}
	
	public static TurbofanEngineDatabaseReader initializeTurbofanDatabase(TurbofanEngineDatabaseReader reader, String databaseDirectory, String databaseName){

		String databaseNameXML = null;
		
		if(databaseName.endsWith(".h5"))
			databaseNameXML = databaseName.replace(".h5", ".xml");
		
		String interpolaterTurbofanDatabaseSerializedDirectory = databaseDirectory + File.separator + "serializedDatabase" 
				+ File.separator; 
		String interpolaterTurbofanDatabaseSerializedFullName = interpolaterTurbofanDatabaseSerializedDirectory +  
				File.separator + databaseNameXML;

		File fileTurbofanDatabase = new File(interpolaterTurbofanDatabaseSerializedFullName);

		if(fileTurbofanDatabase.exists()){
			System.out.println("De-serializing file: " + fileTurbofanDatabase.getAbsolutePath() + " ...");
			reader = (TurbofanEngineDatabaseReader) 
					MyXMLReaderUtils.deserializeObject(
							reader,
							interpolaterTurbofanDatabaseSerializedFullName,
							StandardCharsets.UTF_8
							);
		}
		else {
			System.out.println(	"Serializing file " + "==> " + databaseName + " ==> "+ 
					fileTurbofanDatabase.getAbsolutePath() + " ...");
			reader = new TurbofanEngineDatabaseReader(databaseDirectory, databaseName);


			File dir = new File(interpolaterTurbofanDatabaseSerializedDirectory);
			if(!dir.exists()){
				dir.mkdirs(); 
			}else{
				JPADStaticWriteUtils.serializeObject(reader, 
						interpolaterTurbofanDatabaseSerializedDirectory,
						databaseNameXML);
			}
		}
		return reader;
	}
	
	public static TurbopropEngineDatabaseReader initializeTurbopropDatabase(TurbopropEngineDatabaseReader reader, String databaseDirectory, String databaseName){

		String databaseNameXML = null;
		
		if(databaseName.endsWith(".h5"))
			databaseNameXML = databaseName.replace(".h5", ".xml");
		
		String interpolaterTurbopropDatabaseSerializedDirectory = databaseDirectory + File.separator + "serializedDatabase" 
				+ File.separator; 
		String interpolaterTurbopropDatabaseSerializedFullName = interpolaterTurbopropDatabaseSerializedDirectory +  
				File.separator + databaseNameXML;

		File fileTurbopropDatabase = new File(interpolaterTurbopropDatabaseSerializedFullName);

		if(fileTurbopropDatabase.exists()){
			System.out.println("De-serializing file: " + fileTurbopropDatabase.getAbsolutePath() + " ...");
			reader = (TurbopropEngineDatabaseReader) 
					MyXMLReaderUtils.deserializeObject(
							reader,
							interpolaterTurbopropDatabaseSerializedFullName,
							StandardCharsets.UTF_8
							);
		}
		else {
			System.out.println(	"Serializing file " + "==> " + databaseName + "  ==> "+ 
					fileTurbopropDatabase.getAbsolutePath() + " ...");
			try {
				reader = new TurbopropEngineDatabaseReader(databaseDirectory, databaseName);
			} catch (HDF5LibraryException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}


			File dir = new File(interpolaterTurbopropDatabaseSerializedDirectory);
			if(!dir.exists()){
				dir.mkdirs(); 
			}else{
				JPADStaticWriteUtils.serializeObject(reader, 
						interpolaterTurbopropDatabaseSerializedDirectory,
						databaseNameXML);
			}
		}
		return reader;
	}
	
	public static EngineDatabaseManager initializeEngineDatabase(EngineDatabaseManager databaseManager, String databaseDirectory, String databaseName) throws InvalidFormatException, IOException{

		String databaseNameXML = null;
		
		if(databaseName.endsWith(".xlsx"))
			databaseNameXML = databaseName.replace(".xlsx", ".xml");
		else if(databaseName.endsWith(".xls"))
			databaseNameXML = databaseName.replace(".xls", ".xml");
		
		String serializedDatabaseDirectory = databaseDirectory + "serializedDatabase"; 
		String serializedDatabaseFullName = serializedDatabaseDirectory + File.separator + databaseNameXML;

		File serializedEngineDatabaseFile = new File(serializedDatabaseFullName);

		if(serializedEngineDatabaseFile.exists()){
			System.out.println("De-serializing file: " + serializedEngineDatabaseFile.getAbsolutePath() + " ...");
			databaseManager = (EngineDatabaseManager) 
					MyXMLReaderUtils.deserializeObject(
							databaseManager,
							serializedDatabaseFullName,
							StandardCharsets.ISO_8859_1
							);
		}
		else {
			System.out.println(	"Serializing file " + "==> " + databaseName + "  ==> "+ 
					serializedEngineDatabaseFile.getAbsolutePath() + " ...");
			databaseManager = new EngineDatabaseManager();
			databaseManager.importDatabaseFromFile(databaseDirectory, databaseName);

			File dir = new File(serializedDatabaseDirectory);
			if(!dir.exists()){
				dir.mkdirs(); 
			}else{
				JPADStaticWriteUtils.serializeObject(
						databaseManager, 
						serializedDatabaseDirectory,
						databaseNameXML);
			}
		}
		return databaseManager;
	}
	
	public static FuelFractionDatabaseReader initializeFuelFractionDatabase(FuelFractionDatabaseReader reader, String databaseDirectory) {
		
		String interpolaterFuelFractionDatabaseSerializedDirectory = databaseDirectory + File.separator + "serializedDatabase" 
				+ File.separator; 
		String interpolaterFuelFractionDatabaseSerializedFullName = interpolaterFuelFractionDatabaseSerializedDirectory +  
				MyConfiguration.interpolaterFuelFractionDatabaseSerializedName;

		File fileFuelFractionsDatabase = new File(interpolaterFuelFractionDatabaseSerializedFullName);

		if(fileFuelFractionsDatabase.exists()){
			System.out.println("De-serializing file: " + fileFuelFractionsDatabase.getAbsolutePath() + " ...");
			reader = (FuelFractionDatabaseReader) 
					MyXMLReaderUtils.deserializeObject(
							reader,
							interpolaterFuelFractionDatabaseSerializedFullName,
							StandardCharsets.UTF_8
							);
		}
		else {
			System.out.println(	"Serializing file " + "==> FuelFractions.h5 ==> "+ 
					fileFuelFractionsDatabase.getAbsolutePath() + " ...");
			try {
				reader = new FuelFractionDatabaseReader(databaseDirectory,"FuelFractions.h5");
			} catch (NullPointerException e) {
				e.printStackTrace();
			}


			File dir = new File(interpolaterFuelFractionDatabaseSerializedDirectory);
			if(!dir.exists()){
				dir.mkdirs(); 
			}else{
				JPADStaticWriteUtils.serializeObject(reader, 
						interpolaterFuelFractionDatabaseSerializedDirectory,
						MyConfiguration.interpolaterFuelFractionDatabaseSerializedName);
			}
		}
		return reader;
	}
}

