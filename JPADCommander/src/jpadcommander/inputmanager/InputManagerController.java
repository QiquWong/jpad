package jpadcommander.inputmanager;

import java.io.IOException;

import javafx.fxml.FXML;
import jpadcommander.Main;

public class InputManagerController {
	
	@FXML
	private void showInputManagerAircraftFromFileContent() throws IOException {
		Main.showInputManagerAircraftFromFile();
	}

}
