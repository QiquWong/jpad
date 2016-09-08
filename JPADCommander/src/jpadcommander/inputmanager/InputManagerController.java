package jpadcommander.inputmanager;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import jpadcommander.Main;

public class InputManagerController {
	
	@FXML
	private void showInputManagerAircraftFromFileContent() throws IOException {
		
		// get the content of Input-Aircraft-From-File
		Main.setMainInputManagerAircraftSubContentFieldsLayout(
				(BorderPane) Main.getMainInputManagerLayout().lookup("#mainInputManagerAircraftSubContentFields")
				);
		
		Main.showInputManagerAircraftFromFile();
	}

	@FXML
	private void showInputManagerAircraftDefaultContent() throws IOException {
		
		// get the content of Default-Aircraft
		Main.setMainInputManagerAircraftSubContentFieldsLayout(
				(BorderPane) Main.getMainInputManagerLayout().lookup("#mainInputManagerAircraftSubContentFields")
				);

		Main.showInputManagerAircraftDefault();
	}
	
}
