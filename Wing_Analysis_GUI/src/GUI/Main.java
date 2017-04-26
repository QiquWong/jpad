package GUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import Calculator.InputOutputTree;
import GUI.Views.Controllers;
import GUI.Views.VaraiblesAnalyses;
import GUI.Views.VariablesInputData;
import GUI.Views.VariablesMainCentralButtons;
import GUI.Views.WarningController;
import configuration.MyConfiguration;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {

	private static Stage primaryStage; // è la Finestra principale
	private static BorderPane mainLayout;
	private static Controllers theController;
	File inputFile;
	static Stage newStageWindows;
	static Stage newStageWindowsWarning;
	static FXMLLoader loaderInputClass;
	static InputOutputTree theInputTree;
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		
		MyConfiguration.initWorkingDirectoryTree();
		
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Wing Analysis");
		showMainView();
		showCenterItem();
	}

	// MAIN VIEW--------------------------------------
	public static void showMainView () throws IOException{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("Views/MainView.fxml"));
		
		mainLayout = loader.load(); // associo al mainLayout il border pain 
		
		Scene scene = new Scene(mainLayout);
		primaryStage.setScene(scene); // setto la screna principale
		
		primaryStage.show();
	}
	
	public static void showCenterItem () throws IOException{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("Views/MainCentral.fxml"));
		
		BorderPane centralItems = loader.load();
		mainLayout.setCenter(centralItems);
	}
	
	public static void startNewAnalysis() throws IOException{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("Views/MainCentralButtons.fxml"));
		
		BorderPane centralItems = loader.load();
		mainLayout.setCenter(centralItems);
	}
	
	public static void startAnalysis(InputOutputTree theInputTree) throws IOException{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("Views/MainCentralButtons.fxml"));
		
		BorderPane centralItems = loader.load();
		mainLayout.setCenter(centralItems);
		VariablesMainCentralButtons theMainController = loader.getController();
		theMainController.enableAnalysisButton();
		theMainController.setTheInputTree(theInputTree);
	}
	
	
	public static void showInfo () throws IOException{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("Views/InfoButton.fxml"));
	
		BorderPane newWindowBorder = loader.load();
		
		// devo definire una nuova finestra e settarci dentro questo nuovo border pane
		
		Stage newStage = new Stage();
		newStage.setTitle("Informations");
		newStage.initModality(Modality.WINDOW_MODAL);
		newStage.initOwner(primaryStage);

		// Ora devo settare la scena definita
		
		Scene scene = new Scene(newWindowBorder);
		newStage.setScene(scene);
		newStage.showAndWait();
		
	}
	
	public static void setInputData() throws IOException{
		loaderInputClass = new FXMLLoader();
		loaderInputClass .setLocation(Main.class.getResource("Views/InputData.fxml"));
		
		BorderPane centralItems = loaderInputClass .load();
		mainLayout.setCenter(centralItems);
	}
	
	public static void setInputData(InputOutputTree theInputTree) throws IOException{
		loaderInputClass = new FXMLLoader();
		loaderInputClass.setLocation(Main.class.getResource("Views/InputData.fxml"));
		
		BorderPane centralItems = loaderInputClass.load();
		mainLayout.setCenter(centralItems);
		VariablesInputData theInputDataClass = loaderInputClass.getController();
		theInputDataClass.writeAllData(theInputTree);
	}
	
	public static void goToAnalyses() throws IOException{
		loaderInputClass = new FXMLLoader();
		loaderInputClass .setLocation(Main.class.getResource("Views/Analyses.fxml"));
		
		BorderPane centralItems = loaderInputClass .load();
		mainLayout.setCenter(centralItems);
		
		VaraiblesAnalyses theAnalysesClass = loaderInputClass.getController();
		theAnalysesClass.setTheInputOutputTree(theInputTree);
	}
	
	public static void reStartNewAnalysis() throws IOException{
		newStageWindows = new Stage();
		
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("Views/ExitWindows.fxml"));
	
		BorderPane newWindowBorder = loader.load();
		
		// devo definire una nuova finestra e settarci dentro questo nuovo border pane
		
		newStageWindows.setTitle("Wing Analysis");
		newStageWindows.initModality(Modality.WINDOW_MODAL);
		newStageWindows.initOwner(primaryStage);

		// Ora devo settare la scena definita
		
		Scene scene = new Scene(newWindowBorder);
		newStageWindows.setScene(scene);
		newStageWindows.showAndWait();
			
	}
	
	public static void warningAreaMismatch(VariablesInputData theInputVariables) throws IOException{
		newStageWindowsWarning = new Stage();
		
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("Views/AreaMismatch.fxml"));
	
		BorderPane newWindowBorder = loader.load();
		
		// devo definire una nuova finestra e settarci dentro questo nuovo border pane
		
		newStageWindowsWarning.setTitle("Wing Analysis");
		newStageWindowsWarning.initModality(Modality.WINDOW_MODAL);
		newStageWindowsWarning.initOwner(primaryStage);

		WarningController theControllerClass = loader.getController();
		theControllerClass.setTheVariablesInputClass(theInputVariables);
		
		// Ora devo settare la scena definita
		
		Scene scene = new Scene(newWindowBorder);
		newStageWindowsWarning.setScene(scene);
		newStageWindowsWarning.showAndWait();
		
	
			
	}
	
	public static void saveAndExit() throws IOException{
		VariablesInputData theInputDataClass = loaderInputClass.getController();
		theInputDataClass.saveInputFile();
		
	}
	
	public static Double[] convertFromDoubleToPrimitive(double[] vec) {
		
		Double[] vec_D = new Double[vec.length];
		
		for (int i=0; i<vec.length; i++)
			vec_D[i] = Double.valueOf(vec[i]);
		
		return vec_D;
		
	}
	public static List<Double> convertDoubleArrayToListDouble(Double[] vec){ 

		List<Double> list = new ArrayList<Double>();

		for(int i=0; i<vec.length; i++)
			list.add(vec[i]);
		
		return list;
	}
	
	public static Unit recognizeUnit(ChoiceBox textUnit){

		Unit unit = null;

		
		if (textUnit.getValue().toString().equalsIgnoreCase("m"))
			unit = SI.METER;

		if (textUnit.getValue().toString().equalsIgnoreCase("ft"))
			unit = NonSI.FOOT;

		if (textUnit.getValue().toString().equalsIgnoreCase("°"))
			unit = NonSI.DEGREE_ANGLE;

		if (textUnit.getValue().toString().equalsIgnoreCase("rad"))
			unit = SI.RADIAN;
		
		if (textUnit.getValue().toString().equalsIgnoreCase("m²"))
			unit = SI.SQUARE_METRE;
		
		
		return unit;
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public static Stage getNewStageWindows() {
		return newStageWindows;
	}

	public static void setNewStageWindows(Stage newStageWindows) {
		Main.newStageWindows = newStageWindows;
	}

	public static InputOutputTree getTheInputTree() {
		return theInputTree;
	}

	public static void setTheInputTree(InputOutputTree theInputTree) {
		Main.theInputTree = theInputTree;
	}

	public static Stage getNewStageWindowsWarning() {
		return newStageWindowsWarning;
	}

	public static void setNewStageWindowsWarning(Stage newStageWindowsWarning) {
		Main.newStageWindowsWarning = newStageWindowsWarning;
	}
}
