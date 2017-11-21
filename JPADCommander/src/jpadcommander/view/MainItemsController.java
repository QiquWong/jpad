package jpadcommander.view;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import aircraft.components.Aircraft;
import analyses.ACAnalysisManager;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import jpadcommander.Main;

public class MainItemsController {

	//-------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	//-------------------------------------------------------------------------------------------
	//...........................................................................................
	// BUTTONS:
	//...........................................................................................
	@FXML
	private Button inputManagerButton;
	@FXML
	private Button analysisManagerButton;
	@FXML
	private Button resultsManagerButton;
	
	//-------------------------------------------------------------------------------------------
	// METHODS
	//-------------------------------------------------------------------------------------------
	@FXML
	private void initialize() {
		
		if(Main.getTheAircraft() != null) {
			//FIXME
			inputManagerButton.setStyle("fx-background-color: #00CC00");
		}
		
		ObjectProperty<Aircraft> aircraft = new SimpleObjectProperty<>();
		ObjectProperty<ACAnalysisManager> analysisManager = new SimpleObjectProperty<>();

		try {
			aircraft.set(Main.getTheAircraft());
			analysisManagerButton.disableProperty().bind(
					Bindings.isNull(aircraft)
					);
		} catch (Exception e) {
			analysisManagerButton.setDisable(true);
		}
		
		try {
			analysisManager.set(Main.getTheAircraft().getTheAnalysisManager());
			resultsManagerButton.disableProperty().bind(
					Bindings.isNull(analysisManager)
					);
		} catch (Exception e) {
			resultsManagerButton.setDisable(true);
		}
		
	}
	
	@FXML
	private void goInputManager() throws IOException {
		Main.showInputManager();
	}
}
