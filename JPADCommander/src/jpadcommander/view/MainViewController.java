package jpadcommander.view;

import java.io.IOException;

import javafx.fxml.FXML;
import jpadcommander.Main;

public class MainViewController {

	@FXML
	private void goHome() throws IOException {
		
		Main.showMainItems();
		
		Main.getProgressBar().progressProperty().unbind();
		Main.getStatusBar().textProperty().unbind();
		Main.getTaskPercentage().textProperty().unbind();
		
		Main.getProgressBar().setProgress(0);
		Main.getStatusBar().setText("Welcome to JPADCommander!");
		Main.getTaskPercentage().setText("");
		
	}
	
}
