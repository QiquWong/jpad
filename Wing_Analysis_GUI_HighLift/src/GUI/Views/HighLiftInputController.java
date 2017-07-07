package GUI.Views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class HighLiftInputController {
	
	//HIGH LIFT input-----------------------------------------
	@FXML
	Pane bottomPane;
	@FXML
	ChoiceBox numberOfFlap;
	@FXML
	ChoiceBox numberOfSlat;
	@FXML
	Button confirmNumberButton;
	
	ObservableList<String> numberOfFlapList = FXCollections.observableArrayList("1","2","3","4","5");
	ObservableList<String> numberOfSlatList = FXCollections.observableArrayList("1","2","3","4","5","6" );
	
	@FXML
	private void initialize(){
	numberOfFlap.setItems(numberOfFlapList);
	numberOfSlat.setItems(numberOfSlatList);
	}
}
