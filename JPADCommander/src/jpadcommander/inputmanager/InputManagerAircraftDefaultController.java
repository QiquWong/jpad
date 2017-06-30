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
import javafx.concurrent.Worker.State;
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

		Main.setStatus(State.RUNNING);
		Main.checkStatus(Main.getStatus());

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
			logAircraftDefaultToInterface();
			InputManagerAircraftFromFileController.logFuselageFromFileToInterface();
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

		// AIRCRAFT 2-VIEW
		InputManagerAircraftFromFileController.createAircraftTopView();
		InputManagerAircraftFromFileController.createAircraftSideView();
		InputManagerAircraftFromFileController.createAircraftFrontView();
		
		// FUSELAGE 3-VIEW
		InputManagerAircraftFromFileController.createFuselageTopView();
		InputManagerAircraftFromFileController.createFuselageSideView();
		InputManagerAircraftFromFileController.createFuselageFrontView();
		
		
		// write again
		System.setOut(originalOut);
		
		//////////////////////////////////////////////////////////////////////////////////
		Main.setStatus(State.READY);
		Main.checkStatus(Main.getStatus());

	}	
	
	@SuppressWarnings("unchecked")
	public static void logAircraftDefaultToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		if(Main.getTextAreaAircraftConsoleOutput() == null)
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
		if(Main.getChoiceBoxAircraftType() == null)
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
		if(Main.getChoiceBoxRegulationsType() == null)
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
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldAircraftFuselageX().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftFuselageX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftFuselageY() == null)
			Main.setTextFieldAircraftFuselageY(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftFuselageY")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldAircraftFuselageY().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftFuselageY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftFuselageZ() == null)
			Main.setTextFieldAircraftFuselageZ(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftFuselageZ")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldAircraftFuselageZ().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getZApexConstructionAxes()
					.toString()
					);
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
		if(Main.getTheAircraft().getWing() != null)
			Main.getTextFieldAircraftWingX().setText(
					Main.getTheAircraft()
					.getWing()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftWingX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftWingY() == null)
			Main.setTextFieldAircraftWingY(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingY")
					);
		if(Main.getTheAircraft().getWing() != null)
			Main.getTextFieldAircraftWingY().setText(
					Main.getTheAircraft()
					.getWing()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftWingY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftWingZ() == null)
			Main.setTextFieldAircraftWingZ(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingZ")
					);
		if(Main.getTheAircraft().getWing() != null)
			Main.getTextFieldAircraftWingZ().setText(
					Main.getTheAircraft()
					.getWing()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftWingZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftWingRiggingAngle() == null)
			Main.setTextFieldAircraftWingRiggingAngle(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingRiggingAngle")
					);
		if(Main.getTheAircraft().getWing() != null)
			Main.getTextFieldAircraftWingRiggingAngle().setText(
					Main.getTheAircraft()
					.getWing()
					.getRiggingAngle()
					.toString()
					);
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
		if(Main.getTheAircraft().getHTail() != null)
			Main.getTextFieldAircraftHorizontalTailX().setText(
					Main.getTheAircraft()
					.getHTail()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftHorizontalTailX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftHorizontalTailY() == null)
			Main.setTextFieldAircraftHorizontalTailY(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailY")
					);
		if(Main.getTheAircraft().getHTail() != null)
			Main.getTextFieldAircraftHorizontalTailY().setText(
					Main.getTheAircraft()
					.getHTail()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftHorizontalTailY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftHorizontalTailZ() == null)
			Main.setTextFieldAircraftHorizontalTailZ(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailZ")
					);
		if(Main.getTheAircraft().getHTail() != null)
			Main.getTextFieldAircraftHorizontalTailZ().setText(
					Main.getTheAircraft()
					.getHTail()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftHorizontalTailZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftHorizontalTailRiggingAngle() == null)
			Main.setTextFieldAircraftHorizontalTailRiggingAngle(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailRiggingAngle")
					);
		if(Main.getTheAircraft().getHTail() != null)
			Main.getTextFieldAircraftHorizontalTailRiggingAngle().setText(
					Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle()
					.toString()
					);
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
		if(Main.getTheAircraft().getVTail() != null)
			Main.getTextFieldAircraftVerticalTailX().setText(
					Main.getTheAircraft()
					.getVTail()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftVerticalTailX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftVerticalTailY() == null)
			Main.setTextFieldAircraftVerticalTailY(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailY")
					);
		if(Main.getTheAircraft().getVTail() != null)
			Main.getTextFieldAircraftVerticalTailY().setText(
					Main.getTheAircraft()
					.getVTail()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftVerticalTailY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftVerticalTailZ() == null)
			Main.setTextFieldAircraftVerticalTailZ(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailZ")
					);
		if(Main.getTheAircraft().getVTail() != null)
			Main.getTextFieldAircraftVerticalTailZ().setText(
					Main.getTheAircraft()
					.getVTail()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftVerticalTailZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftVerticalTailRiggingAngle() == null)
			Main.setTextFieldAircraftVerticalTailRiggingAngle(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailRiggingAngle")
					);
		if(Main.getTheAircraft().getVTail() != null)
			Main.getTextFieldAircraftVerticalTailRiggingAngle().setText(
					Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle()
					.toString()
					);
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
		if(Main.getTheAircraft().getCanard() != null)
			Main.getTextFieldAircraftCanardX().setText(
					Main.getTheAircraft()
					.getCanard()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftCanardX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftCanardY() == null)
			Main.setTextFieldAircraftCanardY(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardY")
					);
		if(Main.getTheAircraft().getCanard() != null)
			Main.getTextFieldAircraftCanardY().setText(
					Main.getTheAircraft()
					.getCanard()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftCanardY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftCanardZ() == null)
			Main.setTextFieldAircraftCanardZ(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardZ")
					);
		if(Main.getTheAircraft().getCanard() != null)
			Main.getTextFieldAircraftCanardZ().setText(
					Main.getTheAircraft()
					.getCanard()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftCanardZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftCanardRiggingAngle() == null)
			Main.setTextFieldAircraftCanardRiggingAngle(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardRiggingAngle")
					);
		if(Main.getTheAircraft().getCanard() != null)
			Main.getTextFieldAircraftCanardRiggingAngle().setText(
					Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle()
					.toString()
					);
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
		if(Main.getTheAircraft().getPowerPlant() != null) {
			for (int i = 0; i < Main.getTheAircraft().getPowerPlant().getEngineNumber(); i++) {
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineXList().get(i).setText(
							Main.getTheAircraft()
							.getPowerPlant()
							.getEngineList()
							.get(i)
							.getXApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftEngineXList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineYList().get(i).setText(
							Main.getTheAircraft()
							.getPowerPlant()
							.getEngineList()
							.get(i)
							.getYApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftEngineYList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineZList().get(i).setText(
							Main.getTheAircraft()
							.getPowerPlant()
							.getEngineList()
							.get(i)
							.getZApexConstructionAxes()
							.toString()
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
							Main.getTheAircraft()
							.getPowerPlant()
							.getEngineList()
							.get(i)
							.getTiltingAngle()
							.toString()
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
		if(Main.getTheAircraft().getNacelles() != null) {
			for (int i = 0; i < Main.getTheAircraft().getNacelles().getNacellesNumber(); i++) {
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleXList().get(i).setText(
							Main.getTheAircraft()
							.getNacelles()
							.getNacellesList()
							.get(i)
							.getXApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftNacelleXList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleYList().get(i).setText(
							Main.getTheAircraft()
							.getNacelles()
							.getNacellesList()
							.get(i)
							.getYApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftNacelleYList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleZList().get(i).setText(
							Main.getTheAircraft()
							.getNacelles()
							.getNacellesList()
							.get(i)
							.getZApexConstructionAxes()
							.toString()
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
		if(Main.getTheAircraft().getLandingGears() != null)
			Main.getTextFieldAircraftLandingGearsX().setText(
					Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftLandingGearsX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftLandingGearsY() == null)
			Main.setTextFieldAircraftLandingGearsY(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftLandingGearsY")
					);
		if(Main.getTheAircraft().getLandingGears() != null)
			Main.getTextFieldAircraftLandingGearsY().setText(
					Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftLandingGearsY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftLandingGearsZ() == null)
			Main.setTextFieldAircraftLandingGearsZ(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftLandingGearsZ")
					);
		if(Main.getTheAircraft().getLandingGears() != null)
			Main.getTextFieldAircraftLandingGearsZ().setText(
					Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxes()
					.toString()
					);
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
		if(Main.getTheAircraft().getSystems() != null)
			Main.getTextFieldAircraftSystemsX().setText(
					Main.getTheAircraft()
					.getSystems()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftSystemsX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftSystemsY() == null)
			Main.setTextFieldAircraftSystemsY(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftSystemsY")
					);
		if(Main.getTheAircraft().getSystems() != null)
			Main.getTextFieldAircraftSystemsY().setText(
					Main.getTheAircraft()
					.getSystems()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftSystemsY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftSystemsZ() == null)
			Main.setTextFieldAircraftSystemsZ(
					(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftSystemsZ")
					);
		if(Main.getTheAircraft().getSystems() != null)
			Main.getTextFieldAircraftSystemsZ().setText(
					Main.getTheAircraft()
					.getSystems()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftSystemsZ().setText(
					"NOT INITIALIZED"
					);
	}
	
}