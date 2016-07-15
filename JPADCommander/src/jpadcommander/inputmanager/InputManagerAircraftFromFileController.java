package jpadcommander.inputmanager;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import jpadcommander.Main;

public class InputManagerAircraftFromFileController {

	@FXML
	private void chooseAircraftFile() throws IOException {
		
		// get the text field for aircraft input file name
		Main.setTextFieldAircraftInputFile(
				(TextField) Main.getMainInputManagerAircraftFromFileLayout().lookup("#textFieldAircraftInputFile")
		);

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Open File");
		File file = chooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			Main.getTextFieldAircraftInputFile().setText(file.getAbsolutePath());
		}
	}

	@FXML
	private void loadAircraftFile() throws IOException {
		Alert alert = new Alert(
				AlertType.INFORMATION, 
				"Hello from DAF!\nThis action is still unimplemented.", 
				ButtonType.OK);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.show();
	}	
}
