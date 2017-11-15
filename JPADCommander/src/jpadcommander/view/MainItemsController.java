package jpadcommander.view;

import java.io.IOException;

import javafx.fxml.FXML;
import jpadcommander.Main;

public class MainItemsController {

	@FXML
	private void goInputManager() throws IOException {
		Main.showInputManager();
	}
}
