package sandbox2.bs.javafx.test1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("pippoGUI.fxml"));
		BorderPane root = loader.load();
		Scene scene = new Scene(root, 500, 500);
		primaryStage.setTitle("Pippo 1");
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
	
	void buttonClicked() {
		System.out.println("Hello!");
	}
	
	public static void main(String[] args) {
		launch(args);

	}


}
