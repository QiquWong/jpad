package GUI.Views;

import java.io.IOException;

import GUI.Main;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public class Controllers {

	private Main main;
	private Main theMainClass = new Main();

	
	@FXML
	private void reStartNewAnalysis() throws IOException{
		main.reStartNewAnalysis();
	}
	
	@FXML
	private void goHome() throws IOException{
		main.showCenterItem();
		main.getNewStageWindows().close();
	} 
	
	@FXML
	private void remainHere(){
		main.getNewStageWindows().close();
	}
	
	@FXML
	private void saveAndExit() throws IOException {
		main.saveAndExit();
		goHome();
	}
	
	@FXML
	private void startNewAnalysis() throws IOException{
		main.startNewAnalysis();

	}
	
	@FXML
	private void infoButton() throws IOException{
		main.showInfo();
	}

	public Main getTheMainClass() {
		return theMainClass;
	}
	public void setTheMainClass(Main theMainClass) {
		this.theMainClass = theMainClass;
	}
	
	

}
