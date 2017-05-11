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
	VaraiblesAnalyses theVariablesAnalysisClass;

	@FXML
	Button saveButton;
	
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
	
	@FXML
	private void saveOutput() throws IOException{
		main.saveOutput();
	}
	 

	public Main getTheMainClass() {
		return theMainClass;
	}
	public void setTheMainClass(Main theMainClass) {
		this.theMainClass = theMainClass;
	}

	public Button getSaveButton() {
		return saveButton;
	}

	public void setSaveButton(Button saveButton) {
		this.saveButton = saveButton;
	}

	public VaraiblesAnalyses getTheVariablesAnalysisClass() {
		return theVariablesAnalysisClass;
	}

	public void setTheVariablesAnalysisClass(VaraiblesAnalyses theVariablesAnalysisClass) {
		this.theVariablesAnalysisClass = theVariablesAnalysisClass;
	}
	
	

}
