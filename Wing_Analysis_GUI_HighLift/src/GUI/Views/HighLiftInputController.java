package GUI.Views;

import java.io.IOException;

import org.jscience.physics.amount.Amount;

import Calculator.InputOutputTree;
import GUI.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
public class HighLiftInputController {
	
	InputOutputTree theInputTree;
	VariablesInputData theVariableInputClass;
	
	//HIGH LIFT input-----------------------------------------
	@FXML
	Pane bottomPane;
	@FXML
	ChoiceBox numberOfFlap;
	@FXML
	ChoiceBox numberOfSlat;
	@FXML
	Button confirmNumberButton;
	
	@FXML
	Button exitButton;
	
	ObservableList<String> numberOfFlapList = FXCollections.observableArrayList("1","2","3","4","5");
	ObservableList<String> numberOfSlatList = FXCollections.observableArrayList("1","2","3","4","5","6" );
	
	@FXML
	private void initialize(){
	numberOfFlap.setItems(numberOfFlapList);
	numberOfFlap.setValue("1");
	numberOfSlat.setItems(numberOfSlatList);
	numberOfSlat.setValue("1");
	
	exitButton.setBorder(
			new Border(
					new BorderStroke(Color.RED,
							BorderStrokeStyle.SOLID, 
							new CornerRadii(3),
							BorderWidths.DEFAULT)));
	}
	
	@FXML
	private void confirmNumber() throws IOException{
		theInputTree.setNumberOfFlaps((int)Double.parseDouble(numberOfFlap.getValue().toString()));
		theInputTree.setNumberOfSlats((int)Double.parseDouble(numberOfSlat.getValue().toString()));
		
		Main.confirmNumber(theVariableInputClass, this, theInputTree);
		
	}

	public void writeData() throws IOException {
		numberOfFlap.setValue(Integer.toString(theInputTree.getNumberOfFlaps()));
		numberOfSlat.setValue(Integer.toString(theInputTree.getNumberOfSlats()));
		confirmNumber();
	}
	
	@FXML
	private void exitFromFlap() throws IOException{
		
		Main.exitFromFlap();
		
	}
	
	public InputOutputTree getTheInputTree() {
		return theInputTree;
	}

	public void setTheInputTree(InputOutputTree theInputTree) {
		this.theInputTree = theInputTree;
	}

	public Pane getBottomPane() {
		return bottomPane;
	}

	public void setBottomPane(Pane bottomPane) {
		this.bottomPane = bottomPane;
	}

	public VariablesInputData getTheVariableInputClass() {
		return theVariableInputClass;
	}

	public void setTheVariableInputClass(VariablesInputData theVariableInputClass) {
		this.theVariableInputClass = theVariableInputClass;
	}
}
