package jpadcommander.view;

import java.io.IOException;

import javafx.fxml.FXML;
import jpadcommander.Main;

public class MainViewController {

	@FXML
	private void goHome() throws IOException {
		Main.showMainItems();
		Main.getProgressBar().setProgress(0.0);
		Main.getStatusBar().setText("Welcome to JPADCommander!");
	}
	
}
