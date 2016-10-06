package jpadcommander.view;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import jpadcommander.Main;

public class MainItemsController {

	@FXML
	@SuppressWarnings("unchecked")
	private void goInputManager() throws IOException {
		Main.showInputManager();
		
		// get the choice box for the aircraft type
		Main.setChoiceBoxAircraftType(
				(ChoiceBox<String>) Main.getMainInputManagerLayout()
				.lookup("#choiceBoxAircraftType")
				);
		
		// get the choice box for the regulations type
		Main.setChoiceBoxRegulationsType(
				(ChoiceBox<String>) Main.getMainInputManagerLayout()
				.lookup("#choiceBoxRegulationsType")
				);
	}
	
}
