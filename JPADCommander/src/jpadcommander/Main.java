package jpadcommander;

import java.io.IOException;

import org.controlsfx.control.StatusBar;

import aircraft.Aircraft;
import it.unina.daf.jpadcad.CADManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class Main extends Application {

	//-------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	//...........................................................................................
	// FOLDER CONFIGURATION FIELDS:
	private static String _inputDirectoryPath;
	private static String _outputDirectoryPath;
	private static String _databaseDirectoryPath;
	private static String _inputFileAbsolutePath;
	private static String _cadConfigurationFileAbsolutePath;
	//...........................................................................................
	// LAYOUTS:
	private static Stage _primaryStage;
	private static BorderPane _mainLayout;
	private static BorderPane _mainInputManagerLayout;
	//...........................................................................................
	// STATUS BAR
	private static StatusBar _statusBar;
	private static ProgressBar _progressBar;
	private static StatusBar _taskPercentage;
	//...........................................................................................
	// AIRCRAFT OBJECT
	private static Aircraft _theAircraft;
	private static Boolean _aircraftSaved;
	private static Boolean _aircraftUpdated;
	//...........................................................................................
	// CAD MANAGER
	private static CADManager _theCADManager;
	
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
		dialogConfig.initStyle(StageStyle.UNDECORATED);
		dialogConfig.show();
		Platform.setImplicitExit(false);
		
		_primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent event) {
				
				event.consume();
				
				if(_aircraftSaved != null) {
					if(!_aircraftSaved) {

						//..................................................................................
						// AIRCRAFT DATA NOT SAVED WARNING
						Stage inputDataWarning = new Stage();

						inputDataWarning.setTitle("Aircraft Not Saved Warning");
						inputDataWarning.initModality(Modality.WINDOW_MODAL);
						inputDataWarning.initStyle(StageStyle.UNDECORATED);
						inputDataWarning.initOwner(Main.getPrimaryStage());

						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(Main.class.getResource("inputmanager/InputManagerWarning.fxml"));
						BorderPane inputDataWarningBorderPane = null;
						try {
							inputDataWarningBorderPane = loader.load();
						} catch (IOException e) {
							e.printStackTrace();
						}

						Scene scene = new Scene(inputDataWarningBorderPane);
						inputDataWarning.setScene(scene);
						inputDataWarning.sizeToScene();
						inputDataWarning.show();

						Button yesButton = (Button) inputDataWarningBorderPane.lookup("#warningYesButton");
						yesButton.setOnAction(new EventHandler<ActionEvent>() {

							@Override
							public void handle(ActionEvent arg0) {
								_primaryStage.close();
								inputDataWarning.close();
								System.exit(1);
							}

						});
						Button noButton = (Button) inputDataWarningBorderPane.lookup("#warningNoButton");
						noButton.setOnAction(new EventHandler<ActionEvent>() {

							@Override
							public void handle(ActionEvent arg0) {
								inputDataWarning.close();
							}

						});
					}
					else {
						_primaryStage.close();
						System.exit(1);
					}
				}
				else {
					_primaryStage.close();
					System.exit(1);
				}
			}
		});
		
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
		_mainLayout.setMinWidth(Screen.getPrimary().getBounds().getWidth()*0.85);
		_mainLayout.setMaxWidth(Screen.getPrimary().getBounds().getWidth()*0.85);
		_mainLayout.setMinHeight(Screen.getPrimary().getBounds().getHeight()*0.85);
		_mainLayout.setMaxHeight(Screen.getPrimary().getBounds().getHeight()*0.85);
		_statusBar = new StatusBar();
		_taskPercentage = new StatusBar();
		_progressBar = new ProgressBar(0.0);
		ToolBar statusToolbar = new ToolBar();
		statusToolbar.getItems().add(_statusBar);
		statusToolbar.getItems().add(_progressBar);
		statusToolbar.getItems().add(_taskPercentage);
		_progressBar.setPrefHeight(statusToolbar.getPrefHeight());
		_progressBar.setPrefWidth(_mainLayout.getPrefWidth()*0.5);
		_statusBar.setPrefWidth(_mainLayout.getPrefWidth()*0.25);
		_statusBar.setText("Welcome to JPADCommander!");
		_taskPercentage.setPrefWidth(_mainLayout.getPrefWidth()*0.2);
		_taskPercentage.setText("");
		_mainLayout.setBottom(statusToolbar);
		Scene scene = new Scene(_mainLayout);
		_primaryStage.setScene(scene);
		_primaryStage.setFullScreen(false);
		_primaryStage.setResizable(false);
		_primaryStage.show();
		
		
	}
	
	public static void showInputManager() throws IOException {
		
		if(_theAircraft == null) {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("inputmanager/InputManager.fxml"));
			_mainInputManagerLayout = loader.load();
		}
		
		_statusBar.textProperty().unbind();
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

	public static BorderPane getMainLayout() {
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

	public static BorderPane getMainInputManagerLayout() {
		return _mainInputManagerLayout;
	}

	public static void setMainInputManagerLayout(BorderPane _mainInputManagerLayout) {
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
	
	public static String getCADConfigurationFileAbsolutePath() {
		return _cadConfigurationFileAbsolutePath;
	}
	
	public static void setCADConfigurationFileAbsolutePath(String cadConfigurationFileAbsolutePath) {
		Main._cadConfigurationFileAbsolutePath = cadConfigurationFileAbsolutePath;
	}

	public static StatusBar getTaskPercentage() {
		return _taskPercentage;
	}

	public static void setTaskPercentage(StatusBar _taskPercentage) {
		Main._taskPercentage = _taskPercentage;
	}

	public static Boolean getAircraftSaved() {
		return _aircraftSaved;
	}

	public static void setAircraftSaved(Boolean _aircraftSaved) {
		Main._aircraftSaved = _aircraftSaved;
	}

	public static Boolean getAircraftUpdated() {
		return _aircraftUpdated;
	}

	public static void setAircraftUpdated(Boolean _aircraftUpdated) {
		Main._aircraftUpdated = _aircraftUpdated;
	}
	
	public static CADManager getTheCADManager() {
		return _theCADManager;
	}
	
	public static void setTheCADManager(CADManager _theCADManager) {
		Main._theCADManager = _theCADManager;
	}

}
