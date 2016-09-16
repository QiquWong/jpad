package jpadcommander.view;

import java.io.IOException;

import javafx.fxml.FXML;
import jpadcommander.Main;

public class MainViewController {

	@FXML
	private void goHome() throws IOException {
		Main.showMainItems();
		
		// TODO : IF AIRCRAFT != NULL RE-FILL ALL FIELDS AS THEY WERE BEFORE
	}
	
}
