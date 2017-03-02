package sandbox2.bs.javafx.test1;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class Main extends Application {

	Scene theScene;
	Parent theRoot;
	
	@FXML
    private TextArea myTextArea;
	
	@Override
	public void start(Stage stage) throws Exception {
		
        theRoot = FXMLLoader.load(getClass().getResource("pippoGUI.fxml"));
        
        stage.setTitle("FXML Welcome");
        theScene = new Scene(theRoot, 700, 500);
        stage.setScene(theScene);
        stage.show();
    }
	
	@FXML
	void buttonClicked() throws IOException {
		System.out.println("Hello!");
		// Get the current date and time
		LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDateTime = currentTime.format(formatter);
		myTextArea.appendText("("+formattedDateTime+") AAAA!\n");
		
	}
	
	public static void main(String[] args) {
		Application.launch(Main.class, args);
	}


}
