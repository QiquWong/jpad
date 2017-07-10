package jpadcommander.inputmanager;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.RegulationsEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import jpadcommander.Main;

public class InputManagerAircraftDefaultController {

	ObservableList<String> defaultAircraftList = FXCollections.observableArrayList(
			"ATR-72",
			"B747-100B",
			"AGILE-DC1"
			);
	
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox defaultAircraftChioseBox;
	
	@FXML
	@SuppressWarnings("unchecked")
	private void initialize() {
		defaultAircraftChioseBox.setItems(defaultAircraftList);
	}
	
	@FXML
	private void loadAircraftFile() throws IOException {

		Main.getStatusBar().setText("Loading Aircraft ...");
		
		MyConfiguration.setDir(FoldersEnum.DATABASE_DIR, Main.getDatabaseDirectoryPath());
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		String highLiftDatabaseFileName = "HighLiftDatabase.h5";
		String fusDesDatabaseFilename = "FusDes_database.h5";
		String vedscDatabaseFilename = "VeDSC_database.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
		FusDesDatabaseReader fusDesDatabaseReader = new FusDesDatabaseReader(databaseFolderPath, fusDesDatabaseFilename);
		VeDSCDatabaseReader veDSCDatabaseReader = new VeDSCDatabaseReader(databaseFolderPath, vedscDatabaseFilename);

		Main.getProgressBar().setProgress(0.1);
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
		System.setOut(filterStream);

		if(defaultAircraftChioseBox
				.getSelectionModel()
					.getSelectedItem()
						.equals("ATR-72")) {
			Main.setChoiseBoxSelectionDefaultAircraft(
					defaultAircraftChioseBox
						.getSelectionModel()
							.getSelectedItem()
							);
			Main.setInputFileAbsolutePath("");
			Main.setTheAircraft(new Aircraft.AircraftBuilder(
					"ATR-72",
					AircraftEnum.ATR72,
					aeroDatabaseReader,
					highLiftDatabaseReader,
					fusDesDatabaseReader,
					veDSCDatabaseReader
					)
					.build()
					);
			Main.getProgressBar().setProgress(0.2);
			logAircraftDefaultToInterface();
			Main.getProgressBar().setProgress(0.3);
			InputManagerAircraftFromFileController.logFuselageFromFileToInterface();
			Main.getProgressBar().setProgress(0.4);
		}
		else if(defaultAircraftChioseBox
				.getSelectionModel()
				.getSelectedItem()
					.equals("B747-100B")) {
			Main.setChoiseBoxSelectionDefaultAircraft(
					defaultAircraftChioseBox
						.getSelectionModel()
							.getSelectedItem()
							);
			Alert alert = new Alert(
					AlertType.INFORMATION, 
					"Hello from DAF!\nThis action is still unimplemented.", 
					ButtonType.OK);
			alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			alert.show();
		}
		else if(defaultAircraftChioseBox
				.getSelectionModel()
				.getSelectedItem()
					.equals("AGILE-DC1")) {
			Main.setChoiseBoxSelectionDefaultAircraft(
					defaultAircraftChioseBox
						.getSelectionModel()
							.getSelectedItem()
							);
			Alert alert = new Alert(
					AlertType.INFORMATION, 
					"Hello from DAF!\nThis action is still unimplemented.", 
					ButtonType.OK);
			alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			alert.show();
		}

		// AIRCRAFT 3-VIEW
		InputManagerAircraftFromFileController.createAircraftTopView();
		Main.getProgressBar().setProgress(0.5);
		InputManagerAircraftFromFileController.createAircraftSideView();
		Main.getProgressBar().setProgress(0.6);
		InputManagerAircraftFromFileController.createAircraftFrontView();
		Main.getProgressBar().setProgress(0.7);
		
		// FUSELAGE 3-VIEW
		InputManagerAircraftFromFileController.createFuselageTopView();
		Main.getProgressBar().setProgress(0.8);
		InputManagerAircraftFromFileController.createFuselageSideView();
		Main.getProgressBar().setProgress(0.9);
		InputManagerAircraftFromFileController.createFuselageFrontView();
		Main.getProgressBar().setProgress(1.0);
		
		
		// write again
		System.setOut(originalOut);
		
		Main.getStatusBar().setText("Aircraft loaded!");
		
	}	
	
	@SuppressWarnings("unchecked")
	public static void logAircraftDefaultToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
//		if(Main.getTextAreaAircraftConsoleOutput() == null)
			Main.setTextAreaAircraftConsoleOutput(
					(TextArea) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#output")
					);
		Main.getTextAreaAircraftConsoleOutput().setText(
				Main.getTheAircraft().toString()
				);

		// clear all the file path text fields: 
		if(Main.getChoiceBoxAircraftType() != null)
			Main.getChoiceBoxAircraftType().getSelectionModel().clearSelection();
		if(Main.getChoiceBoxRegulationsType() != null)
			Main.getChoiceBoxRegulationsType().getSelectionModel().clearSelection();
		if(Main.getTextFieldAircraftCabinConfiguration() != null)
			Main.getTextFieldAircraftCabinConfiguration().clear();
		if(Main.getTextFieldAircraftFuselageFile() != null)
			Main.getTextFieldAircraftFuselageFile().clear();
		if(Main.getTextFieldAircraftWingFile() != null)
			Main.getTextFieldAircraftWingFile().clear();
		if(Main.getTextFieldAircraftHorizontalTailFile() != null)
			Main.getTextFieldAircraftHorizontalTailFile().clear();
		if(Main.getTextFieldAircraftVerticalTailFile() != null)
			Main.getTextFieldAircraftVerticalTailFile().clear();
		if(Main.getTextFieldAircraftCanardFile() != null)
			Main.getTextFieldAircraftCanardFile().clear();
		if(Main.getTextFieldAircraftEngineFileList() != null)
			Main.getTextFieldAircraftEngineFileList().clear();
		if(Main.getTextFieldAircraftNacelleFileList() != null)
			Main.getTextFieldAircraftNacelleFileList().clear();
		if(Main.getTextFieldAircraftLandingGearsFile() != null)
			Main.getTextFieldAircraftLandingGearsFile().clear();
		if(Main.getTextFieldAircraftSystemsFile() != null)
			Main.getTextFieldAircraftSystemsFile().clear();
		
		//---------------------------------------------------------------------------------
		// AIRCRAFT TYPE:
//		if(Main.getChoiceBoxAircraftType() == null)
			Main.setChoiceBoxAircraftType(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftType")
					);
		
		AircraftTypeEnum aircraftTypeFileName = Main.getTheAircraft().getTypeVehicle();
		
		if(aircraftTypeFileName != null) { 
			if(Main.getChoiceBoxAircraftType() != null) {
				if(aircraftTypeFileName.toString().equalsIgnoreCase("JET"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(0);
				else if(aircraftTypeFileName.toString().equalsIgnoreCase("FIGHTER"))		
					Main.getChoiceBoxAircraftType().getSelectionModel().select(1);
				else if(aircraftTypeFileName.toString().equalsIgnoreCase("BUSINESS_JET"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(2);
				else if(aircraftTypeFileName.toString().equalsIgnoreCase("TURBOPROP"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(3);
				else if(aircraftTypeFileName.toString().equalsIgnoreCase("GENERAL_AVIATION"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(4);
				else if(aircraftTypeFileName.toString().equalsIgnoreCase("COMMUTER"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(5);
				else if(aircraftTypeFileName.toString().equalsIgnoreCase("ACROBATIC"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(6);
			}
		}
		
		//---------------------------------------------------------------------------------
		// REGULATIONS TYPE:
//		if(Main.getChoiceBoxRegulationsType() == null)
			Main.setChoiceBoxRegulationsType(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxRegulationsType")
					);
		
		RegulationsEnum regulationsTypeFileName = Main.getTheAircraft().getRegulations();
		
		if(regulationsTypeFileName != null) { 
			if(Main.getChoiceBoxRegulationsType() != null) {
				if(regulationsTypeFileName.toString().equalsIgnoreCase("FAR_23"))
					Main.getChoiceBoxRegulationsType().getSelectionModel().select(0);
				else if(regulationsTypeFileName.toString().equalsIgnoreCase("FAR_25"))		
					Main.getChoiceBoxRegulationsType().getSelectionModel().select(1);
			}
		}
		
		//---------------------------------------------------------------------------------
		// FUSELAGE:
		if(Main.getTextFieldAircraftFuselageX() == null)
			Main.setTextFieldAircraftFuselageX(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftFuselageX")
					);
		if(Main.getFuselageXUnitChoiceBox() == null)
			Main.setFuselageXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#fuselageXUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldAircraftFuselageX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageXUnitChoiceBox().getSelectionModel().select(1);
			
		}
		
		else
			Main.getTextFieldAircraftFuselageX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftFuselageY() == null)
			Main.setTextFieldAircraftFuselageY(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftFuselageY")
					);
		if(Main.getFuselageYUnitChoiceBox() == null)
			Main.setFuselageYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#fuselageYUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldAircraftFuselageY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageYUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftFuselageY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftFuselageZ() == null)
			Main.setTextFieldAircraftFuselageZ(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftFuselageZ")
					);
		if(Main.getFuselageZUnitChoiceBox() == null)
			Main.setFuselageZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#fuselageZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldAircraftFuselageZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftFuselageZ().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// WING:
		if(Main.getTextFieldAircraftWingX() == null)
			Main.setTextFieldAircraftWingX(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingX")
					);
		if(Main.getWingXUnitChoiceBox() == null)
			Main.setWingXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#wingXUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getWing() != null) {
			
			Main.getTextFieldAircraftWingX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getWing()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getWingXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getWingXUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftWingX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftWingY() == null)
			Main.setTextFieldAircraftWingY(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingY")
					);
		if(Main.getWingYUnitChoiceBox() == null)
			Main.setWingYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#wingYUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getWing() != null) {
			
			Main.getTextFieldAircraftWingY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getWing()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getWingYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getWingYUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftWingY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftWingZ() == null)
			Main.setTextFieldAircraftWingZ(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingZ")
					);
		if(Main.getWingZUnitChoiceBox() == null)
			Main.setWingZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#wingZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getWing() != null) {
			
			Main.getTextFieldAircraftWingZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getWing()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getWingZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getWingZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftWingZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftWingRiggingAngle() == null)
			Main.setTextFieldAircraftWingRiggingAngle(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingRiggingAngle")
					);
		if(Main.getWingRiggingAngleUnitChoiceBox() == null)
			Main.setWingRiggingAngleUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#wingRiggingAngleUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getWing() != null) {
			
			Main.getTextFieldAircraftWingRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getWing()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				Main.getWingRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				Main.getWingRiggingAngleUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftWingRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// HORIZONTAL TAIL:
		if(Main.getTextFieldAircraftHorizontalTailX() == null)
			Main.setTextFieldAircraftHorizontalTailX(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailX")
					);
		if(Main.gethTailXUnitChoiceBox() == null)
			Main.sethTailXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#hTailXUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getHTail() != null) {
			
			Main.getTextFieldAircraftHorizontalTailX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getHTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.gethTailXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.gethTailXUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftHorizontalTailX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftHorizontalTailY() == null)
			Main.setTextFieldAircraftHorizontalTailY(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailY")
					);
		if(Main.gethTailYUnitChoiceBox() == null)
			Main.sethTailYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#hTailYUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getHTail() != null) {
			
			Main.getTextFieldAircraftHorizontalTailY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getHTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.gethTailYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.gethTailYUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftHorizontalTailY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftHorizontalTailZ() == null)
			Main.setTextFieldAircraftHorizontalTailZ(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailZ")
					);
		if(Main.gethtailZUnitChoiceBox() == null)
			Main.sethtailZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#hTailZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getHTail() != null) {
			
			Main.getTextFieldAircraftHorizontalTailZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getHTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.gethtailZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.gethtailZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftHorizontalTailZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftHorizontalTailRiggingAngle() == null)
			Main.setTextFieldAircraftHorizontalTailRiggingAngle(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailRiggingAngle")
					);
		if(Main.gethTailRiggingAngleUnitChoiceBox() == null)
			Main.sethTailRiggingAngleUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#hTailRiggingAngleUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getHTail() != null) {
			
			Main.getTextFieldAircraftHorizontalTailRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				Main.gethTailRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				Main.gethTailRiggingAngleUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftHorizontalTailRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// VERTICAL TAIL:
		if(Main.getTextFieldAircraftVerticalTailX() == null)
			Main.setTextFieldAircraftVerticalTailX(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailX")
					);
		if(Main.getvTailXUnitChoiceBox() == null)
			Main.setvTailXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#vTailXUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getVTail() != null) {
			
			Main.getTextFieldAircraftVerticalTailX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getVTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getvTailXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getvTailXUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftVerticalTailX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftVerticalTailY() == null)
			Main.setTextFieldAircraftVerticalTailY(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailY")
					);
		if(Main.getvTailYUnitChoiceBox() == null)
			Main.setvTailYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#vTailYUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getVTail() != null) {
			
			Main.getTextFieldAircraftVerticalTailY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getVTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getvTailYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getvTailYUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftVerticalTailY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftVerticalTailZ() == null)
			Main.setTextFieldAircraftVerticalTailZ(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailZ")
					);
		if(Main.getvTailZUnitChoiceBox() == null)
			Main.setvTailZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#vTailZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getVTail() != null) {
			
			Main.getTextFieldAircraftVerticalTailZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getVTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getvTailZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getvTailZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftVerticalTailZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftVerticalTailRiggingAngle() == null)
			Main.setTextFieldAircraftVerticalTailRiggingAngle(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailRiggingAngle")
					);
		if(Main.getvTailRiggingAngleUnitChoiceBox() == null)
			Main.setvTailRiggingAngleUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#vTailRiggingAngleUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getVTail() != null) {
			
			Main.getTextFieldAircraftVerticalTailRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				Main.getvTailRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				Main.getvTailRiggingAngleUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftHorizontalTailRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// CANARD:
		if(Main.getTextFieldAircraftCanardX() == null)
			Main.setTextFieldAircraftCanardX(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardX")
					);
		if(Main.getCanardXUnitChoiceBox() == null)
			Main.setCanardXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#canardXUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getCanard() != null) {
			
			Main.getTextFieldAircraftCanardX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getCanard()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getCanardXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getCanardXUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftCanardX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftCanardY() == null)
			Main.setTextFieldAircraftCanardY(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardY")
					);
		if(Main.getCanardYUnitChoiceBox() == null)
			Main.setCanardYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#canardYUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getCanard() != null) {
			
			Main.getTextFieldAircraftCanardY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getCanard()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getCanardYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getCanardYUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftCanardY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftCanardZ() == null)
			Main.setTextFieldAircraftCanardZ(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardZ")
					);
		if(Main.getCanardZUnitChoiceBox() == null)
			Main.setCanardZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#canardZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getCanard() != null) {
			
			Main.getTextFieldAircraftCanardZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getCanard()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getCanardZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getCanardZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftCanardZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftCanardRiggingAngle() == null)
			Main.setTextFieldAircraftCanardRiggingAngle(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardRiggingAngle")
					);
		if(Main.getCanardRiggingAngleUnitChoiceBox() == null)
			Main.setCanardRiggingAngleUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#canardRiggingAngleUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getCanard() != null) {
			
			Main.getTextFieldAircraftCanardRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				Main.getCanardRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				Main.getCanardRiggingAngleUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftCanardRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// POWER PLANT:
		if(Main.getTextFieldAircraftEngineFileList().isEmpty()) {
			Main.getTextFieldAircraftEngineFileList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile1")
					);
			Main.getTextFieldAircraftEngineFileList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile2")
					);
			Main.getTextFieldAircraftEngineFileList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile3")
					);
			Main.getTextFieldAircraftEngineFileList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile4")
					);
			Main.getTextFieldAircraftEngineFileList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile5")
					);
			Main.getTextFieldAircraftEngineFileList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile6")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldAircraftEngineXList().isEmpty()) {
			Main.getTextFieldAircraftEngineXList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX1")
					);
			Main.getTextFieldAircraftEngineXList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX2")
					);
			Main.getTextFieldAircraftEngineXList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX3")
					);
			Main.getTextFieldAircraftEngineXList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX4")
					);
			Main.getTextFieldAircraftEngineXList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX5")
					);
			Main.getTextFieldAircraftEngineXList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX6")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldAircraftEngineYList().isEmpty()) {
			Main.getTextFieldAircraftEngineYList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY1")
					);
			Main.getTextFieldAircraftEngineYList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY2")
					);
			Main.getTextFieldAircraftEngineYList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY3")
					);
			Main.getTextFieldAircraftEngineYList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY4")
					);
			Main.getTextFieldAircraftEngineYList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY5")
					);
			Main.getTextFieldAircraftEngineYList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY6")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldAircraftEngineZList().isEmpty()) {
			Main.getTextFieldAircraftEngineZList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ1")
					);
			Main.getTextFieldAircraftEngineZList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ2")
					);
			Main.getTextFieldAircraftEngineZList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ3")
					);
			Main.getTextFieldAircraftEngineZList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ4")
					);
			Main.getTextFieldAircraftEngineZList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ5")
					);
			Main.getTextFieldAircraftEngineZList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ6")
					);
		}
		//..........................................................................................................
		if(Main.getChoiceBoxesAircraftEnginePositonList().isEmpty()) {
			Main.getChoiceBoxesAircraftEnginePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#choiceBoxAircraftEnginePosition1")
					);
			Main.getChoiceBoxesAircraftEnginePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#choiceBoxAircraftEnginePosition2")
					);
			Main.getChoiceBoxesAircraftEnginePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#choiceBoxAircraftEnginePosition3")
					);
			Main.getChoiceBoxesAircraftEnginePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#choiceBoxAircraftEnginePosition4")
					);
			Main.getChoiceBoxesAircraftEnginePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#choiceBoxAircraftEnginePosition5")
					);
			Main.getChoiceBoxesAircraftEnginePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#choiceBoxAircraftEnginePosition6")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldAircraftEngineTiltList().isEmpty()) {
			Main.getTextFieldAircraftEngineTiltList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt1")
					);
			Main.getTextFieldAircraftEngineTiltList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt2")
					);
			Main.getTextFieldAircraftEngineTiltList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt3")
					);
			Main.getTextFieldAircraftEngineTiltList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt4")
					);
			Main.getTextFieldAircraftEngineTiltList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt5")
					);
			Main.getTextFieldAircraftEngineTiltList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt6")
					);
		}
		//..........................................................................................................
		if(Main.getPowerPlantXUnitChoiceBox() == null)
			Main.setPowerPlantXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#powerPlantXUnitChoiceBox")
					);
		if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			Main.getPowerPlantXUnitChoiceBox().getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			Main.getPowerPlantXUnitChoiceBox().getSelectionModel().select(1);
		
		
		if(Main.getPowerPlantYUnitChoiceBox() == null)
			Main.setPowerPlantYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#powerPlantYUnitChoiceBox")
					);
		if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			Main.getPowerPlantYUnitChoiceBox().getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			Main.getPowerPlantYUnitChoiceBox().getSelectionModel().select(1);
		
		
		if(Main.getPowerPlantZUnitChoiceBox() == null)
			Main.setPowerPlantZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#powerPlantZUnitChoiceBox")
					);
		if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			Main.getPowerPlantZUnitChoiceBox().getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			Main.getPowerPlantZUnitChoiceBox().getSelectionModel().select(1);
		
		
		if(Main.getPowerPlantTiltAngleUnitChoiceBox() == null)
			Main.setPowerPlantTiltAngleUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#powerPlantTiltingAngleUnitChoiceBox")
					);
		if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getTiltingAngle().getUnit().toString().equalsIgnoreCase("deg"))
			Main.getPowerPlantTiltAngleUnitChoiceBox().getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getTiltingAngle().getUnit().toString().equalsIgnoreCase("rad"))
			Main.getPowerPlantTiltAngleUnitChoiceBox().getSelectionModel().select(1);
		
		//..........................................................................................................
		if(Main.getTheAircraft().getPowerPlant() != null) {
			for (int i = 0; i < Main.getTheAircraft().getPowerPlant().getEngineNumber(); i++) {
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) 
					Main.getTextFieldAircraftEngineXList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getXApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					Main.getTextFieldAircraftEngineXList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineYList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getYApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					Main.getTextFieldAircraftEngineYList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineZList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getZApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					Main.getTextFieldAircraftEngineZList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)

					if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("BURIED")
							)
						Main.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("WING")
							)
						Main.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(1);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("AFT_FUSELAGE")
							)
						Main.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(2);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("HTAIL")
							)
						Main.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(3);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("REAR_FUSELAGE")
							)
						Main.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(4);

				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineTiltList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getTiltingAngle()
									.getEstimatedValue()
									)
							);
				else
					Main.getTextFieldAircraftEngineTiltList().get(i).setText(
							"NOT INITIALIZED"
							);
			}
		}
		//---------------------------------------------------------------------------------
		// NACELLES:
		if(Main.getTextFieldAircraftNacelleFileList().isEmpty()) {
			Main.getTextFieldAircraftNacelleFileList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile1")
					);
			Main.getTextFieldAircraftNacelleFileList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile2")
					);
			Main.getTextFieldAircraftNacelleFileList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile3")
					);
			Main.getTextFieldAircraftNacelleFileList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile4")
					);
			Main.getTextFieldAircraftNacelleFileList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile5")
					);
			Main.getTextFieldAircraftNacelleFileList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile6")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldAircraftNacelleXList().isEmpty()) {
			Main.getTextFieldAircraftNacelleXList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX1")
					);
			Main.getTextFieldAircraftNacelleXList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX2")
					);
			Main.getTextFieldAircraftNacelleXList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX3")
					);
			Main.getTextFieldAircraftNacelleXList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX4")
					);
			Main.getTextFieldAircraftNacelleXList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX5")
					);
			Main.getTextFieldAircraftNacelleXList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX6")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldAircraftNacelleYList().isEmpty()) {
			Main.getTextFieldAircraftNacelleYList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY1")
					);
			Main.getTextFieldAircraftNacelleYList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY2")
					);
			Main.getTextFieldAircraftNacelleYList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY3")
					);
			Main.getTextFieldAircraftNacelleYList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY4")
					);
			Main.getTextFieldAircraftNacelleYList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY5")
					);
			Main.getTextFieldAircraftNacelleYList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY6")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldAircraftNacelleZList().isEmpty()) {
			Main.getTextFieldAircraftNacelleZList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ1")
					);
			Main.getTextFieldAircraftNacelleZList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ2")
					);
			Main.getTextFieldAircraftNacelleZList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ3")
					);
			Main.getTextFieldAircraftNacelleZList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ4")
					);
			Main.getTextFieldAircraftNacelleZList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ5")
					);
			Main.getTextFieldAircraftNacelleZList().add(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ6")
					);
		}
		//..........................................................................................................
		if(Main.getChoiceBoxesAircraftNacellePositonList().isEmpty()) {
			Main.getChoiceBoxesAircraftNacellePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#choiceBoxAircraftNacellePosition1")
					);
			Main.getChoiceBoxesAircraftNacellePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#choiceBoxAircraftNacellePosition2")
					);
			Main.getChoiceBoxesAircraftNacellePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#choiceBoxAircraftNacellePosition3")
					);
			Main.getChoiceBoxesAircraftNacellePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#choiceBoxAircraftNacellePosition4")
					);
			Main.getChoiceBoxesAircraftNacellePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#choiceBoxAircraftNacellePosition5")
					);
			Main.getChoiceBoxesAircraftNacellePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#choiceBoxAircraftNacellePosition6")
					);
		}
		//..........................................................................................................
		if(Main.getNacelleXUnitChoiceBox() == null)
			Main.setNacelleXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#nacellesXUnitChoiceBox")
					);
		if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			Main.getNacelleXUnitChoiceBox().getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			Main.getNacelleXUnitChoiceBox().getSelectionModel().select(1);
		
		
		if(Main.getNacelleYUnitChoiceBox() == null)
			Main.setNacelleYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#nacellesYUnitChoiceBox")
					);
		if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			Main.getNacelleYUnitChoiceBox().getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			Main.getNacelleYUnitChoiceBox().getSelectionModel().select(1);
		
		
		if(Main.getNacelleZUnitChoiceBox() == null)
			Main.setNacelleZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#nacelleZUnitChoiceBox")
					);
		if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			Main.getNacelleZUnitChoiceBox().getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			Main.getNacelleZUnitChoiceBox().getSelectionModel().select(1);
		
		//..........................................................................................................
		if(Main.getTheAircraft().getNacelles() != null) {
			for (int i = 0; i < Main.getTheAircraft().getNacelles().getNacellesNumber(); i++) {
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleXList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getXApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					Main.getTextFieldAircraftNacelleXList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleYList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getYApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					Main.getTextFieldAircraftNacelleYList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleZList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getZApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					Main.getTextFieldAircraftNacelleZList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					
					if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("WING")
							)
						Main.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("FUSELAGE")
							)
						Main.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(1);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("HTAIL")
							)
						Main.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(2);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("UNDERCARRIAGE_HOUSING")
							)
						Main.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(3);
					
			}
		}
		//---------------------------------------------------------------------------------
		// LANDING GEARS:
		if(Main.getTextFieldAircraftLandingGearsX() == null)
			Main.setTextFieldAircraftLandingGearsX(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftLandingGearsX")
					);
		if(Main.getLandingGearsXUnitChoiceBox() == null)
			Main.setLandingGearsXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#landingGearsXUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getLandingGears() != null) {
			
			Main.getTextFieldAircraftLandingGearsX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getLandingGearsXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getLandingGearsXUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftLandingGearsX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftLandingGearsY() == null)
			Main.setTextFieldAircraftLandingGearsY(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftLandingGearsY")
					);
		if(Main.getLandingGearsYUnitChoiceBox() == null)
			Main.setLandingGearsYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#landingGearsYUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getLandingGears() != null) {
			
			Main.getTextFieldAircraftLandingGearsY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getLandingGearsYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getLandingGearsYUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftLandingGearsY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftLandingGearsZ() == null)
			Main.setTextFieldAircraftLandingGearsZ(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftLandingGearsZ")
					);
		if(Main.getLandingGearsZUnitChoiceBox() == null)
			Main.setLandingGearsZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#landingGearsZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getLandingGears() != null) {
			
			Main.getTextFieldAircraftLandingGearsZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getLandingGearsZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getLandingGearsZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftLandingGearsZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getChoiceBoxAircraftLandingGearsPosition() == null)
			Main.setChoiceBoxAircraftLandingGearsPosition(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#choiceBoxAircraftLandingGearsPosition")
					);
		if(Main.getTheAircraft().getLandingGears() != null)
			
			if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("FUSELAGE")
					)
				Main.getChoiceBoxAircraftLandingGearsPosition().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("WING")
					)
				Main.getChoiceBoxAircraftLandingGearsPosition().getSelectionModel().select(1);
			else if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("NACELLE")
					)
				Main.getChoiceBoxAircraftLandingGearsPosition().getSelectionModel().select(2);
		//---------------------------------------------------------------------------------
		// SYSTEMS:
		if(Main.getTextFieldAircraftSystemsX() == null)
			Main.setTextFieldAircraftSystemsX(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftSystemsX")
					);
		if(Main.getSystemsXUnitChoiceBox() == null)
			Main.setSystemsXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#systemsXUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getSystems() != null) {
			
			Main.getTextFieldAircraftSystemsX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getSystems()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getSystems()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getSystemsXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getSystems()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getSystemsXUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftSystemsX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftSystemsY() == null)
			Main.setTextFieldAircraftSystemsY(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftSystemsY")
					);
		if(Main.getSystemsYUnitChoiceBox() == null)
			Main.setSystemsYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#systemsYUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getSystems() != null) {
			
			Main.getTextFieldAircraftSystemsY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getSystems()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getSystems()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getSystemsYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getSystems()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getSystemsYUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftSystemsY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftSystemsZ() == null)
			Main.setTextFieldAircraftSystemsZ(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftSystemsZ")
					);
		if(Main.getSystemsZUnitChoiceBox() == null)
			Main.setSystemsZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#systemsZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getSystems() != null) {
			
			Main.getTextFieldAircraftSystemsZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getSystems()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getSystems()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getSystemsZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getSystems()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getSystemsZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftSystemsZ().setText(
					"NOT INITIALIZED"
					);
	}
	
}