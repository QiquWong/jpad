/* 
 * References:
 * 
 *     https://www.youtube.com/channel/UClbVoA_U-OBFKx89y-TRofA/videos
 *     https://youtu.be/q5A-qW2eGKs
 *     
 */

package jpadcommander;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {

	private static Stage primaryStage;
	private static BorderPane mainLayout;
	private static BorderPane mainInputManagerLayout;
	private static BorderPane mainInputManagerAircraftSubContentLayout;
	private static TextField textFieldAircraftInputFile;
	private static BorderPane mainInputManagerAircraftFromFileLayout;
	private static BorderPane mainInputManagerAircraftDefaultLayout;
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		
		// Modal dialog - setup
	     
		
		Main.primaryStage = primaryStage;
		Main.primaryStage.setTitle("JPADCommander - UniNa - DAF");
		showMainView();
		
		Stage dialogConfig = new DialogConfig(primaryStage);
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
}
