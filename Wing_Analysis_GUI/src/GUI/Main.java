package GUI;

import java.io.File;
import java.io.IOException;

import GUI.Views.Controllers;
import configuration.MyConfiguration;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
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
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("Views/InputData.fxml"));
		
		BorderPane centralItems = loader.load();
		mainLayout.setCenter(centralItems);
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
}
