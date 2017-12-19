package jpadcommander.analysismanager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AnalysisManagerController {

	
	//-------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	//-------------------------------------------------------------------------------------------
	//...........................................................................................
	// LAYOUTS:
	//...........................................................................................
	@FXML
	private SplitPane analysisManagerViewsAndDataLogSplitPane;
	@FXML
	private TabPane analysisManagerTabPane;
		@FXML
	private TextArea textAreaAnalysisManagerConsoleOutput;
	//...........................................................................................
	// BUTTONS:
	//...........................................................................................
	@FXML
	private Button newAnalysisButton;
	@FXML
	private Button loadAnalysisButton;
	@FXML
	private Button saveAnalysisButton;
	@FXML
	private Button runAnalysisButton;
	@FXML
	private Button weightsDetailsButton;
	@FXML
	private Button aeroTODetailsButton;
	@FXML
	private Button aeroCLDetailsButton;
	@FXML
	private Button aeroCRDetailsButton;
	@FXML
	private Button aeroLNDDetailsButton;
	@FXML
	private Button perfoDetailsButton;
	@FXML
	private Button costsDetailsButton;
	//...........................................................................................
	// SCROLL PANE - GLOBAL DATA
	//...........................................................................................	
	@FXML
	private TextField textFieldPositiveLimitLoadFactor;
	@FXML
	private TextField textFieldNegativeLimitLoadFactor;
	//...........................................................................................
	// SCROLL PANE - WEIGHTS
	//...........................................................................................	

	
	
}
