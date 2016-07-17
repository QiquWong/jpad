package jpadcommander.inputmanager;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;

public class InputManagerAircraftDefaultController {

	ObservableList<String> defaultAircraftList = FXCollections.observableArrayList("ATR-72","B747-100B","AGILE DC-1");
	
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox defaultAircraftChioseBox;
	
	@FXML
	@SuppressWarnings("unchecked")
	private void initialize() {
		defaultAircraftChioseBox.setItems(defaultAircraftList);
		defaultAircraftChioseBox.setValue("mmm");
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
