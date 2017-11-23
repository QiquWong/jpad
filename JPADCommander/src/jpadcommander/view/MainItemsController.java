package jpadcommander.view;

import java.io.IOException;

import aircraft.components.Aircraft;
import analyses.ACAnalysisManager;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import jpadcommander.Main;

public class MainItemsController {

	//-------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	//-------------------------------------------------------------------------------------------
	//...........................................................................................
	// LAYOUTS:
	//...........................................................................................
	@FXML
	private HBox backgroundHBox;
	
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
		
		ObjectProperty<Aircraft> aircraft = new SimpleObjectProperty<>();
		ObjectProperty<ACAnalysisManager> analysisManager = new SimpleObjectProperty<>();

		inputManagerButton.backgroundProperty().bind(
				Bindings.when(aircraft.isNotNull())
                .then(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)))
                .otherwise(new Background(new BackgroundFill(Color.valueOf("#F0FFFF"), CornerRadii.EMPTY, Insets.EMPTY)))
                );
		analysisManagerButton.backgroundProperty().bind(
				Bindings.when(analysisManager.isNotNull())
                .then(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)))
                .otherwise(new Background(new BackgroundFill(Color.valueOf("#F0FFFF"), CornerRadii.EMPTY, Insets.EMPTY)))
                );
		
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
