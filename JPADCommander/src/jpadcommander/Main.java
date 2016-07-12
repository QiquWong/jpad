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
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

	private Stage primaryStage;
	private BorderPane mainLayout;
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("JPADCommander - UniNa - DAF");
		showMainView();
		
	}

	private void showMainView() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("view/MainView.fxml"));
		this.mainLayout = loader.load();
		Scene scene = new Scene(mainLayout);
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}

	public static void main(String[] args) {
		launch(args);
	}
}
