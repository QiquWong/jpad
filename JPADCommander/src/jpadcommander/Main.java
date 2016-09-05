/* 
 * References:
 * 
 *     https://www.youtube.com/channel/UClbVoA_U-OBFKx89y-TRofA/videos
 *     https://youtu.be/q5A-qW2eGKs
 *     
 */

package jpadcommander;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import aircraft.components.Aircraft;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {

	private static Stage primaryStage;
	private static BorderPane mainLayout;
	private static BorderPane mainInputManagerLayout;
	private static BorderPane mainInputManagerAircraftSubContentLayout;
	
	private static TextArea textAreaAircraftConsoleOutput;
	
	private static TextField textFieldAircraftInputFile;
	
	private static TextField textFieldAircraftCabinConfiguration;
	
	private static TextField textFieldAircraftFuselageFile;
	private static TextField textFieldAircraftFuselageX;
	private static TextField textFieldAircraftFuselageY;
	private static TextField textFieldAircraftFuselageZ;
	
	private static TextField textFieldAircraftWingFile;
	private static TextField textFieldAircraftWingX;
	private static TextField textFieldAircraftWingY;
	private static TextField textFieldAircraftWingZ;
	private static TextField textFieldAircraftWingRiggingAngle;
	
	private static TextField textFieldAircraftHorizontalTailFile;
	private static TextField textFieldAircraftHorizontalTailX;
	private static TextField textFieldAircraftHorizontalTailY;
	private static TextField textFieldAircraftHorizontalTailZ;
	private static TextField textFieldAircraftHorizontalTailRiggingAngle;
	
	private static TextField textFieldAircraftVerticalTailFile;
	private static TextField textFieldAircraftVerticalTailX;
	private static TextField textFieldAircraftVerticalTailY;
	private static TextField textFieldAircraftVerticalTailZ;
	private static TextField textFieldAircraftVerticalTailRiggingAngle;
	
	private static TextField textFieldAircraftCanardFile;
	private static TextField textFieldAircraftCanardX;
	private static TextField textFieldAircraftCanardY;
	private static TextField textFieldAircraftCanardZ;
	private static TextField textFieldAircraftCanardRiggingAngle;
	
	private static List<TextField> textFieldAircraftEngineFileList = new ArrayList<>();
	private static List<TextField> textFieldAircraftEngineXList = new ArrayList<>();
	private static List<TextField> textFieldAircraftEngineYList = new ArrayList<>();
	private static List<TextField> textFieldAircraftEngineZList = new ArrayList<>();
	private static List<TextField> textFieldAircraftEnginePositonList = new ArrayList<>();
	private static List<TextField> textFieldAircraftEngineTiltList = new ArrayList<>();
	
	private static List<TextField> textFieldAircraftNacelleFileList = new ArrayList<>();
	private static List<TextField> textFieldAircraftNacelleXList = new ArrayList<>();
	private static List<TextField> textFieldAircraftNacelleYList = new ArrayList<>();
	private static List<TextField> textFieldAircraftNacelleZList = new ArrayList<>();
	private static List<TextField> textFieldAircraftNacellePositonList = new ArrayList<>();
	
	private static TextField textFieldAircraftLandingGearsFile;
	private static TextField textFieldAircraftLandingGearsX;
	private static TextField textFieldAircraftLandingGearsY;
	private static TextField textFieldAircraftLandingGearsZ;
	private static TextField textFieldAircraftLandingGearsPosition;
	
	private static TextField textFieldAircraftSystemsFile;
	private static TextField textFieldAircraftSystemsX;
	private static TextField textFieldAircraftSystemsY;
	private static TextField textFieldAircraftSystemsZ;
	
	private static BorderPane mainInputManagerAircraftFromFileLayout;
	private static BorderPane mainInputManagerAircraftDefaultLayout;
	private static String inputDirectoryPath;
	private static String outputDirectoryPath;
	private static String databaseDirectoryPath;
	
	private static Aircraft theAircraft;
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		
		Main.primaryStage = primaryStage;
		Main.primaryStage.setTitle("JPADCommander - UniNa - DAF");
		showMainView();
		
		Stage dialogConfig = new DialogConfig(primaryStage);
		dialogConfig.setTitle("JPADCommander Configuration - UniNa - DAF");
		dialogConfig.sizeToScene();
		dialogConfig.show();
		
		showMainItems();
		
	}
	
	private void showMainView() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("view/MainView.fxml"));
		Main.mainLayout = loader.load();
		Scene scene = new Scene(mainLayout);
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}

	public static void showMainItems() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("view/MainItems.fxml"));
		HBox mainItems = loader.load();
		mainLayout.setCenter(mainItems);
		primaryStage.show();
		
	}
	
	public static void showInputManager() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("inputmanager/InputManager.fxml"));
		mainInputManagerLayout = loader.load();
		mainLayout.setCenter(mainInputManagerLayout);
		primaryStage.show();
		
	}

	public static void showInputManagerAircraftFromFile() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("inputmanager/InputManagerAircraftFromFile.fxml"));
		mainInputManagerAircraftFromFileLayout = loader.load();
		mainInputManagerAircraftSubContentLayout.setCenter(mainInputManagerAircraftFromFileLayout);
		primaryStage.show();
		
	}

	public static void showInputManagerAircraftDefault() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("inputmanager/InputManagerAircraftDefault.fxml"));
		mainInputManagerAircraftDefaultLayout = loader.load();
		mainInputManagerAircraftSubContentLayout.setCenter(mainInputManagerAircraftDefaultLayout);
		primaryStage.show();
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	public static TextField getTextFieldAircraftInputFile() {
		return textFieldAircraftInputFile;
	}

	public static BorderPane getMainInputManagerAircraftFromFileLayout() {
		return mainInputManagerAircraftFromFileLayout;
	}

	public static BorderPane getMainInputManagerAircraftDefaultLayout() {
		return mainInputManagerAircraftDefaultLayout;
	}
	
	public static void setTextFieldAircraftInputFile(TextField textFieldAircraftInputFile) {
		Main.textFieldAircraftInputFile = textFieldAircraftInputFile;
	}

	public static BorderPane getMainInputManagerLayout() {
		return mainInputManagerLayout;
	}

	public static void setMainInputManagerLayout(BorderPane mainInputManagerLayout) {
		Main.mainInputManagerLayout = mainInputManagerLayout;
	}

	public static BorderPane getMainInputManagerAircraftSubContentLayout() {
		return mainInputManagerAircraftSubContentLayout;
	}

	public static void setMainInputManagerAircraftSubContentLayout(BorderPane mainInputManagerAircraftSubContentLayout) {
		Main.mainInputManagerAircraftSubContentLayout = mainInputManagerAircraftSubContentLayout;
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static String getInputDirectoryPath() {
		return inputDirectoryPath;
	}

	public static void setInputDirectoryPath(String inputDirectoryPath) {
		Main.inputDirectoryPath = inputDirectoryPath;
	}

	public static String getOutputDirectoryPath() {
		return outputDirectoryPath;
	}

	public static void setOutputDirectoryPath(String outputDirectoryPath) {
		Main.outputDirectoryPath = outputDirectoryPath;
	}

	public static String getDatabaseDirectoryPath() {
		return databaseDirectoryPath;
	}

	public static void setDatabaseDirectoryPath(String databaseDirectoryPath) {
		Main.databaseDirectoryPath = databaseDirectoryPath;
	}

	public static Aircraft getTheAircraft() {
		return theAircraft;
	}

	public static void setTheAircraft(Aircraft theAircraft) {
		Main.theAircraft = theAircraft;
	}
	
	public static TextField getTextFieldAircraftCabinConfiguration() {
		return textFieldAircraftCabinConfiguration;
	}

	public static void setTextFieldAircraftCabinConfiguration(TextField textFieldAircraftCabinConfiguration) {
		Main.textFieldAircraftCabinConfiguration = textFieldAircraftCabinConfiguration;
	}

	public static TextField getTextFieldAircraftFuselageFile() {
		return textFieldAircraftFuselageFile;
	}

	public static void setTextFieldAircraftFuselageFile(TextField textFieldAircraftFuselageFile) {
		Main.textFieldAircraftFuselageFile = textFieldAircraftFuselageFile;
	}

	public static TextField getTextFieldAircraftFuselageX() {
		return textFieldAircraftFuselageX;
	}

	public static void setTextFieldAircraftFuselageX(TextField textFieldAircraftFuselageX) {
		Main.textFieldAircraftFuselageX = textFieldAircraftFuselageX;
	}

	public static TextField getTextFieldAircraftFuselageY() {
		return textFieldAircraftFuselageY;
	}

	public static void setTextFieldAircraftFuselageY(TextField textFieldAircraftFuselageY) {
		Main.textFieldAircraftFuselageY = textFieldAircraftFuselageY;
	}

	public static TextField getTextFieldAircraftFuselageZ() {
		return textFieldAircraftFuselageZ;
	}

	public static void setTextFieldAircraftFuselageZ(TextField textFieldAircraftFuselageZ) {
		Main.textFieldAircraftFuselageZ = textFieldAircraftFuselageZ;
	}

	public static TextField getTextFieldAircraftWingFile() {
		return textFieldAircraftWingFile;
	}

	public static void setTextFieldAircraftWingFile(TextField textFieldAircraftWingFile) {
		Main.textFieldAircraftWingFile = textFieldAircraftWingFile;
	}

	public static TextField getTextFieldAircraftWingX() {
		return textFieldAircraftWingX;
	}

	public static void setTextFieldAircraftWingX(TextField textFieldAircraftWingX) {
		Main.textFieldAircraftWingX = textFieldAircraftWingX;
	}

	public static TextField getTextFieldAircraftWingY() {
		return textFieldAircraftWingY;
	}

	public static void setTextFieldAircraftWingY(TextField textFieldAircraftWingY) {
		Main.textFieldAircraftWingY = textFieldAircraftWingY;
	}

	public static TextField getTextFieldAircraftWingZ() {
		return textFieldAircraftWingZ;
	}

	public static void setTextFieldAircraftWingZ(TextField textFieldAircraftWingZ) {
		Main.textFieldAircraftWingZ = textFieldAircraftWingZ;
	}

	public static TextField getTextFieldAircraftWingRiggingAngle() {
		return textFieldAircraftWingRiggingAngle;
	}

	public static void setTextFieldAircraftWingRiggingAngle(TextField textFieldAircraftWingRiggingAngle) {
		Main.textFieldAircraftWingRiggingAngle = textFieldAircraftWingRiggingAngle;
	}

	public static TextField getTextFieldAircraftHorizontalTailFile() {
		return textFieldAircraftHorizontalTailFile;
	}

	public static void setTextFieldAircraftHorizontalTailFile(TextField textFieldAircraftHorizontalTailFile) {
		Main.textFieldAircraftHorizontalTailFile = textFieldAircraftHorizontalTailFile;
	}

	public static TextField getTextFieldAircraftHorizontalTailX() {
		return textFieldAircraftHorizontalTailX;
	}

	public static void setTextFieldAircraftHorizontalTailX(TextField textFieldAircraftHorizontalTailX) {
		Main.textFieldAircraftHorizontalTailX = textFieldAircraftHorizontalTailX;
	}

	public static TextField getTextFieldAircraftHorizontalTailY() {
		return textFieldAircraftHorizontalTailY;
	}

	public static void setTextFieldAircraftHorizontalTailY(TextField textFieldAircraftHorizontalTailY) {
		Main.textFieldAircraftHorizontalTailY = textFieldAircraftHorizontalTailY;
	}

	public static TextField getTextFieldAircraftHorizontalTailZ() {
		return textFieldAircraftHorizontalTailZ;
	}

	public static void setTextFieldAircraftHorizontalTailZ(TextField textFieldAircraftHorizontalTailZ) {
		Main.textFieldAircraftHorizontalTailZ = textFieldAircraftHorizontalTailZ;
	}

	public static TextField getTextFieldAircraftHorizontalTailRiggingAngle() {
		return textFieldAircraftHorizontalTailRiggingAngle;
	}

	public static void setTextFieldAircraftHorizontalTailRiggingAngle(
			TextField textFieldAircraftHorizontalTailRiggingAngle) {
		Main.textFieldAircraftHorizontalTailRiggingAngle = textFieldAircraftHorizontalTailRiggingAngle;
	}

	public static TextField getTextFieldAircraftVerticalTailFile() {
		return textFieldAircraftVerticalTailFile;
	}

	public static void setTextFieldAircraftVerticalTailFile(TextField textFieldAircraftVerticalTailFile) {
		Main.textFieldAircraftVerticalTailFile = textFieldAircraftVerticalTailFile;
	}

	public static TextField getTextFieldAircraftVerticalTailX() {
		return textFieldAircraftVerticalTailX;
	}

	public static void setTextFieldAircraftVerticalTailX(TextField textFieldAircraftVerticalTailX) {
		Main.textFieldAircraftVerticalTailX = textFieldAircraftVerticalTailX;
	}

	public static TextField getTextFieldAircraftVerticalTailY() {
		return textFieldAircraftVerticalTailY;
	}

	public static void setTextFieldAircraftVerticalTailY(TextField textFieldAircraftVerticalTailY) {
		Main.textFieldAircraftVerticalTailY = textFieldAircraftVerticalTailY;
	}

	public static TextField getTextFieldAircraftVerticalTailZ() {
		return textFieldAircraftVerticalTailZ;
	}

	public static void setTextFieldAircraftVerticalTailZ(TextField textFieldAircraftVerticalTailZ) {
		Main.textFieldAircraftVerticalTailZ = textFieldAircraftVerticalTailZ;
	}

	public static TextField getTextFieldAircraftVerticalTailRiggingAngle() {
		return textFieldAircraftVerticalTailRiggingAngle;
	}

	public static void setTextFieldAircraftVerticalTailRiggingAngle(TextField textFieldAircraftVerticalTailRiggingAngle) {
		Main.textFieldAircraftVerticalTailRiggingAngle = textFieldAircraftVerticalTailRiggingAngle;
	}

	public static TextField getTextFieldAircraftCanardFile() {
		return textFieldAircraftCanardFile;
	}

	public static void setTextFieldAircraftCanardFile(TextField textFieldAircraftCanardFile) {
		Main.textFieldAircraftCanardFile = textFieldAircraftCanardFile;
	}

	public static TextField getTextFieldAircraftCanardX() {
		return textFieldAircraftCanardX;
	}

	public static void setTextFieldAircraftCanardX(TextField textFieldAircraftCanardX) {
		Main.textFieldAircraftCanardX = textFieldAircraftCanardX;
	}

	public static TextField getTextFieldAircraftCanardY() {
		return textFieldAircraftCanardY;
	}

	public static void setTextFieldAircraftCanardY(TextField textFieldAircraftCanardY) {
		Main.textFieldAircraftCanardY = textFieldAircraftCanardY;
	}

	public static TextField getTextFieldAircraftCanardZ() {
		return textFieldAircraftCanardZ;
	}

	public static void setTextFieldAircraftCanardZ(TextField textFieldAircraftCanardZ) {
		Main.textFieldAircraftCanardZ = textFieldAircraftCanardZ;
	}

	public static TextField getTextFieldAircraftCanardRiggingAngle() {
		return textFieldAircraftCanardRiggingAngle;
	}

	public static void setTextFieldAircraftCanardRiggingAngle(TextField textFieldAircraftCanardRiggingAngle) {
		Main.textFieldAircraftCanardRiggingAngle = textFieldAircraftCanardRiggingAngle;
	}

	public static List<TextField> getTextFieldAircraftEngineFileList() {
		return textFieldAircraftEngineFileList;
	}

	public static void setTextFieldAircraftEngineFileList(List<TextField> textFieldAircraftEngineFileList) {
		Main.textFieldAircraftEngineFileList = textFieldAircraftEngineFileList;
	}

	public static List<TextField> getTextFieldAircraftEngineXList() {
		return textFieldAircraftEngineXList;
	}

	public static void setTextFieldAircraftEngineXList(List<TextField> textFieldAircraftEngineXList) {
		Main.textFieldAircraftEngineXList = textFieldAircraftEngineXList;
	}

	public static List<TextField> getTextFieldAircraftEngineYList() {
		return textFieldAircraftEngineYList;
	}

	public static void setTextFieldAircraftEngineYList(List<TextField> textFieldAircraftEngineYList) {
		Main.textFieldAircraftEngineYList = textFieldAircraftEngineYList;
	}

	public static List<TextField> getTextFieldAircraftEngineZList() {
		return textFieldAircraftEngineZList;
	}

	public static void setTextFieldAircraftEngineZList(List<TextField> textFieldAircraftEngineZList) {
		Main.textFieldAircraftEngineZList = textFieldAircraftEngineZList;
	}

	public static List<TextField> getTextFieldAircraftEnginePositonList() {
		return textFieldAircraftEnginePositonList;
	}

	public static void setTextFieldAircraftEnginePositonList(List<TextField> textFieldAircraftEnginePositonList) {
		Main.textFieldAircraftEnginePositonList = textFieldAircraftEnginePositonList;
	}

	public static List<TextField> getTextFieldAircraftEngineTiltList() {
		return textFieldAircraftEngineTiltList;
	}

	public static void setTextFieldAircraftEngineTiltList(List<TextField> textFieldAircraftEngineTiltList) {
		Main.textFieldAircraftEngineTiltList = textFieldAircraftEngineTiltList;
	}

	public static List<TextField> getTextFieldAircraftNacelleFileList() {
		return textFieldAircraftNacelleFileList;
	}

	public static void setTextFieldAircraftNacelleFileList(List<TextField> textFieldAircraftNacelleFileList) {
		Main.textFieldAircraftNacelleFileList = textFieldAircraftNacelleFileList;
	}

	public static List<TextField> getTextFieldAircraftNacelleXList() {
		return textFieldAircraftNacelleXList;
	}

	public static void setTextFieldAircraftNacelleXList(List<TextField> textFieldAircraftNacelleXList) {
		Main.textFieldAircraftNacelleXList = textFieldAircraftNacelleXList;
	}

	public static List<TextField> getTextFieldAircraftNacelleYList() {
		return textFieldAircraftNacelleYList;
	}

	public static void setTextFieldAircraftNacelleYList(List<TextField> textFieldAircraftNacelleYList) {
		Main.textFieldAircraftNacelleYList = textFieldAircraftNacelleYList;
	}

	public static List<TextField> getTextFieldAircraftNacelleZList() {
		return textFieldAircraftNacelleZList;
	}

	public static void setTextFieldAircraftNacelleZList(List<TextField> textFieldAircraftNacelleZList) {
		Main.textFieldAircraftNacelleZList = textFieldAircraftNacelleZList;
	}

	public static List<TextField> getTextFieldAircraftNacellePositonList() {
		return textFieldAircraftNacellePositonList;
	}

	public static void setTextFieldAircraftNacellePositonList(List<TextField> textFieldAircraftNacellePositonList) {
		Main.textFieldAircraftNacellePositonList = textFieldAircraftNacellePositonList;
	}

	public static TextField getTextFieldAircraftLandingGearsFile() {
		return textFieldAircraftLandingGearsFile;
	}

	public static void setTextFieldAircraftLandingGearsFile(TextField textFieldAircraftLandingGearsFile) {
		Main.textFieldAircraftLandingGearsFile = textFieldAircraftLandingGearsFile;
	}

	public static TextField getTextFieldAircraftLandingGearsX() {
		return textFieldAircraftLandingGearsX;
	}

	public static void setTextFieldAircraftLandingGearsX(TextField textFieldAircraftLandingGearsX) {
		Main.textFieldAircraftLandingGearsX = textFieldAircraftLandingGearsX;
	}

	public static TextField getTextFieldAircraftLandingGearsY() {
		return textFieldAircraftLandingGearsY;
	}

	public static void setTextFieldAircraftLandingGearsY(TextField textFieldAircraftLandingGearsY) {
		Main.textFieldAircraftLandingGearsY = textFieldAircraftLandingGearsY;
	}

	public static TextField getTextFieldAircraftLandingGearsZ() {
		return textFieldAircraftLandingGearsZ;
	}

	public static void setTextFieldAircraftLandingGearsZ(TextField textFieldAircraftLandingGearsZ) {
		Main.textFieldAircraftLandingGearsZ = textFieldAircraftLandingGearsZ;
	}

	public static TextField getTextFieldAircraftLandingGearsPosition() {
		return textFieldAircraftLandingGearsPosition;
	}

	public static void setTextFieldAircraftLandingGearsPosition(TextField textFieldAircraftLandingGearsPosition) {
		Main.textFieldAircraftLandingGearsPosition = textFieldAircraftLandingGearsPosition;
	}

	public static TextField getTextFieldAircraftSystemsFile() {
		return textFieldAircraftSystemsFile;
	}

	public static void setTextFieldAircraftSystemsFile(TextField textFieldAircraftSystemsFile) {
		Main.textFieldAircraftSystemsFile = textFieldAircraftSystemsFile;
	}

	public static TextField getTextFieldAircraftSystemsX() {
		return textFieldAircraftSystemsX;
	}

	public static void setTextFieldAircraftSystemsX(TextField textFieldAircraftSystemsX) {
		Main.textFieldAircraftSystemsX = textFieldAircraftSystemsX;
	}

	public static TextField getTextFieldAircraftSystemsY() {
		return textFieldAircraftSystemsY;
	}

	public static void setTextFieldAircraftSystemsY(TextField textFieldAircraftSystemsY) {
		Main.textFieldAircraftSystemsY = textFieldAircraftSystemsY;
	}

	public static TextField getTextFieldAircraftSystemsZ() {
		return textFieldAircraftSystemsZ;
	}

	public static void setTextFieldAircraftSystemsZ(TextField textFieldAircraftSystemsZ) {
		Main.textFieldAircraftSystemsZ = textFieldAircraftSystemsZ;
	}

	public static TextArea getTextAreaAircraftConsoleOutput() {
		return textAreaAircraftConsoleOutput;
	}

	public static void setTextAreaAircraftConsoleOutput(TextArea textAreaAircraftConsoleOutput) {
		Main.textAreaAircraftConsoleOutput = textAreaAircraftConsoleOutput;
	}

}
