package GUI.Views;

import java.io.File;
import java.io.IOException;

import Calculator.InputOutputTree;
import Calculator.Reader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;

public class SaveOutput {
	
	InputOutputTree theInputOutputTree;

	@FXML
	RadioButton svg;
	
	@FXML
	RadioButton xml;
	
	@FXML
	RadioButton xls;
	
	@FXML
	RadioButton png;
	
	@FXML
	Button save;

	@FXML
	public void saveFiles() throws IOException{

		if(svg.isSelected()){
			
		}
		
		if(xml.isSelected()){
			
		}
		
		if(xls.isSelected()){
			
		}
		
		if(png.isSelected()){
			
		}
	}
	
	
	public InputOutputTree getTheInputOutputTree() {
		return theInputOutputTree;
	}

	public void setTheInputOutputTree(InputOutputTree theInputOutputTree) {
		this.theInputOutputTree = theInputOutputTree;
	}
}
