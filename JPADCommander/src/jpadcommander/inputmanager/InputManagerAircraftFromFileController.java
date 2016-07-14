package jpadcommander.inputmanager;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import jpadcommander.Main;

public class InputManagerAircraftFromFileController {

	@FXML
	private void chooseAircraftFile() throws IOException {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Open File");
		File file = chooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			Main.getTextFieldAircraftInputFile().setText(file.getAbsolutePath());
		}
	}

}
