package jpadcommander;

import java.io.IOException;

import org.controlsfx.control.StatusBar;

import aircraft.components.Aircraft;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {

	//-------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	//...........................................................................................
	// FOLDER CONFIGURATION FIELDS:
	private static String _inputDirectoryPath;
	private static String _outputDirectoryPath;
	private static String _databaseDirectoryPath;
	private static String _inputFileAbsolutePath;
	//...........................................................................................
	// LAYOUTS:
	private static Stage _primaryStage;
	private static BorderPane _mainLayout;
	private static BorderPane _mainInputManagerLayout;
	//...........................................................................................
	// STATUS BAR
	private static StatusBar _statusBar;
	private static ProgressBar _progressBar;
	//...........................................................................................
	// AIRCRAFT OBJECT
	private static Aircraft _theAircraft;
	
	//-------------------------------------------------------------------------------------------
	// METHODS
	//-------------------------------------------------------------------------------------------
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		
		_primaryStage = primaryStage;
		_primaryStage.setTitle("JPADCommander - UniNa - DAF");
		showMainView();
		Stage dialogConfig = new DialogConfig(primaryStage);
		dialogConfig.setTitle("JPADCommander Configuration - UniNa - DAF");
		dialogConfig.sizeToScene();
		dialogConfig.show();
		
		showMainItems();
		
	}

	public static void showMainItems() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("view/MainItems.fxml"));
		HBox mainItems = loader.load();
		_mainLayout.setCenter(mainItems);
		_primaryStage.show();
		
	}
	
	public void showMainView() throws IOException {
		
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("view/MainView.fxml"));
		_mainLayout = loader.load();
		_statusBar = new StatusBar();
		_progressBar = new ProgressBar();
		_progressBar.setProgress(0.0);
		ToolBar statusToolbar = new ToolBar();
		statusToolbar.getItems().add(_statusBar);
		statusToolbar.getItems().add(_progressBar);
		_progressBar.setPrefHeight(statusToolbar.getPrefHeight());
		_progressBar.setPrefWidth(_mainLayout.getPrefWidth()*0.6);
		_statusBar.setPrefWidth(_mainLayout.getPrefWidth()*0.2);
		_statusBar.setText("Welcome to JPADCommander!");
		_mainLayout.setBottom(statusToolbar);
		Scene scene = new Scene(_mainLayout);
		_primaryStage.setScene(scene);
		_primaryStage.show();
		
	}
	
	public static void showInputManager() throws IOException {
		
		if(_theAircraft == null) {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("inputmanager/InputManager.fxml"));
			_mainInputManagerLayout = loader.load();
		}
		
		_progressBar.setProgress(0.0);
		_statusBar.setText("Ready!");
		_mainLayout.setCenter(_mainInputManagerLayout);
		
		_primaryStage.show();
		
	}
	
	//-------------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	public static Stage getPrimaryStage() {
		return _primaryStage;
	}

	public static void setPrimaryStage(Stage primaryStage) {
		Main._primaryStage = primaryStage;
	}

	public static BorderPane getMain2Layout() {
		return _mainLayout;
	}

	public static void setMain2Layout(BorderPane _mainLayout) {
		Main._mainLayout = _mainLayout;
	}

	public static StatusBar getStatusBar() {
		return _statusBar;
	}

	public static void setStatusBar(StatusBar _statusBar) {
		Main._statusBar = _statusBar;
	}

	public static ProgressBar getProgressBar() {
		return _progressBar;
	}

	public static void setProgressBar(ProgressBar _progressBar) {
		Main._progressBar = _progressBar;
	}

	public static BorderPane getMain2InputManagerLayout() {
		return _mainInputManagerLayout;
	}

	public static void setMain2InputManagerLayout(BorderPane _mainInputManagerLayout) {
		Main._mainInputManagerLayout = _mainInputManagerLayout;
	}

	public static Aircraft getTheAircraft() {
		return _theAircraft;
	}

	public static void setTheAircraft(Aircraft _theAircraft) {
		Main._theAircraft = _theAircraft;
	}

	public static String getInputDirectoryPath() {
		return _inputDirectoryPath;
	}

	public static void setInputDirectoryPath(String inputDirectoryPath) {
		Main._inputDirectoryPath = inputDirectoryPath;
	}

	public static String getOutputDirectoryPath() {
		return _outputDirectoryPath;
	}

	public static void setOutputDirectoryPath(String outputDirectoryPath) {
		Main._outputDirectoryPath = outputDirectoryPath;
	}

	public static String getDatabaseDirectoryPath() {
		return _databaseDirectoryPath;
	}

	public static void setDatabaseDirectoryPath(String databaseDirectoryPath) {
		Main._databaseDirectoryPath = databaseDirectoryPath;
	}

	public static String getInputFileAbsolutePath() {
		return _inputFileAbsolutePath;
	}

	public static void setInputFileAbsolutePath(String inputFileAbsolutePath) {
		Main._inputFileAbsolutePath = inputFileAbsolutePath;
	}

}
