package GUI.Views;

import java.io.IOException;

import GUI.Main;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public class VariablesMainCentralButtons {
	
	private Main main;
	
	@FXML
	Button inputButton;
	@FXML
	Button inputCheck;
	@FXML
	Button analysisButton;
	@FXML
	Button analysisCheck;
	
	@FXML
	private void initialize(){
		inputCheck.setBackground(
        		new Background(
        				new BackgroundFill(
        						Color.LIGHTGREEN,
        						new CornerRadii(2),
        						new Insets(4)
        						)
        				)
        		);
		analysisCheck.setBackground(
        		new Background(
        				new BackgroundFill(
        						Color.ORANGERED,
        						new CornerRadii(2),
        						new Insets(4)
        						)
        				)
        		);
	}
	
	@FXML
	private void setInputData() throws IOException{
		main.setInputData();
	}
	
	@FXML
	private void enableAnalysisButton() throws IOException{ // dopo modifica e metti abilita se pieno il file di input
		inputButton.setBorder(
				new Border(
						new BorderStroke(Color.LIGHTGREEN,
								BorderStrokeStyle.SOLID, 
								new CornerRadii(30),
								BorderWidths.DEFAULT)));
		analysisButton.setDisable(false); 
		analysisCheck.setBackground(
        		new Background(
        				new BackgroundFill(
        						Color.LIGHTGREEN,
        						new CornerRadii(2),
        						new Insets(4)
        						)
        				)
        		);
	}

	public Button getAnalysisCheck() {
		return analysisCheck;
	}

	public void setAnalysisCheck(Button analysisCheck) {
		this.analysisCheck = analysisCheck;
	}
}
