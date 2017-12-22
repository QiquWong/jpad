package jpadcommander.analysismanager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
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
	private Button balanceDetailsButton;
	@FXML
	private Button aeroTODetailsButton;
	@FXML
	private Button aeroCLDetailsButton;
	@FXML
	private Button aeroCRDetailsButton;
	@FXML
	private Button aeroLNDDetailsButton;
	@FXML
	private Button performanceDetailsButton;
	@FXML
	private Button costsDetailsButton;
	//...........................................................................................
	// ANALYSIS MANAGER
	//...........................................................................................	
	@FXML
	private TextField fileAnalysisTextField;
	@FXML
	private CheckBox importFromFileAnalysisCheckBox;
	//...........................................................................................
	// SCROLL PANE - GLOBAL DATA
	//...........................................................................................	
	@FXML
	private TextField positiveLimitLoadFactorTextField;
	@FXML
	private TextField negativeLimitLoadFactorTextField;
	//...........................................................................................
	// SCROLL PANE - WEIGHTS, BALANCE, AERO, PERFO, COSTS.
	//...........................................................................................	
	@FXML
	private TextField fileWeightsTextField;
	@FXML
	private CheckBox calculateWeightsCheckBox;
	@FXML
	private CheckBox plotWeightsCheckBox;
		
	@FXML
	private TextField FileBalanceTextField;
	@FXML
	private CheckBox calculateBalanceCheckBox;
	@FXML
	private CheckBox plotBalanceCheckBox;
	
	@FXML
	private TextField FileAeroTOTextField;
	@FXML
	private CheckBox calculateAeroTOCheckBox;
	@FXML
	private CheckBox plotAeroTOCheckBox;
	
	@FXML
	private TextField FileAeroCLTextField;
	@FXML
	private CheckBox calculateAeroCLCheckBox;
	@FXML
	private CheckBox plotAeroCLCheckBox;
	
	@FXML
	private TextField FileAeroCRTextField;
	@FXML
	private CheckBox calculateAeroCRCheckBox;
	@FXML
	private CheckBox plotAeroCRCheckBox;
	
	@FXML
	private TextField FileAeroLNDTextField;
	@FXML
	private CheckBox calculateAeroLNDCheckBox;
	@FXML
	private CheckBox plotAeroLNDCheckBox;
	
	@FXML
	private TextField FilePerformanceTextField;
	@FXML
	private CheckBox calculatePerformanceCheckBox;
	@FXML
	private CheckBox plotPerformanceCheckBox;
	@FXML
	private CheckBox performTakeOffAnalysisPerformanceCheckBox;
	@FXML
	private CheckBox performClimbAnalysisPerformanceCheckBox;
	@FXML
	private CheckBox performCruiseAnalysisPerformanceCheckBox;
	@FXML
	private CheckBox performDescentAnalysisPerformanceCheckBox;
	@FXML
	private CheckBox performLandingAnalysisPerformanceCheckBox;
	@FXML
	private CheckBox performMissionProfileAnalysisPerformanceCheckBox;
	@FXML
	private CheckBox performPayloadRangeAnalysisPerformanceCheckBox;
	@FXML
	private CheckBox performVnDiagramAnalysisPerformanceCheckBox;
	
	@FXML
	private TextField FileCostsTextField;
	@FXML
	private CheckBox calculateCostsCheckBox;
	@FXML
	private CheckBox plotCostsCheckBox;
	@FXML
	private CheckBox performCapitalDocAnalysisCheckBox;
	@FXML
	private CheckBox performCrewDocAnalysisCheckBox;
	@FXML
	private CheckBox performFuelDocAnalysisCheckBox;
	@FXML
	private CheckBox performChargesDocAnalysisCheckBox;
	@FXML
	private CheckBox performMaintenanceDocAnalysisCheckBox;
	
	//...........................................................................................
	// SCROLL PANE - WEIGHTS, BALANCE, COSTS.
	// CHOICE BOX
	//...........................................................................................	
	
	// WEIGHTS
	@FXML
	private ChoiceBox<String> fuselageWeightsMethodChoiceBox;	
	@FXML
	private ChoiceBox<String> wingWeightsMethodChoiceBox;
	@FXML
	private ChoiceBox<String> horizontalWeightsMethodChoiceBox;
	@FXML
	private ChoiceBox<String> verticalWeightsMethodChoiceBox;
	@FXML
	private ChoiceBox<String> nacellesWeightsMethodChoiceBox;
	@FXML
	private ChoiceBox<String> landingGearsWeightsMethodChoiceBox;
	@FXML
	private ChoiceBox<String> systemsWeightsMethodChoiceBox;
	
	// BALANCE
	@FXML
	private ChoiceBox<String> fuselageBalanceMethodChoiceBox;	
	@FXML
	private ChoiceBox<String> wingBalanceMethodChoiceBox;
	@FXML
	private ChoiceBox<String> horizontalBalanceMethodChoiceBox;
	@FXML
	private ChoiceBox<String> verticalBalanceMethodChoiceBox;
	
	// COSTS
	@FXML
	private ChoiceBox<String> capitalDocMethodChoiceBox;	
	@FXML
	private ChoiceBox<String> crewDocMethodChoiceBox;
	@FXML
	private ChoiceBox<String> fuelDocMethodChoiceBox;
	@FXML
	private ChoiceBox<String> chargesDocMethodChoiceBox;
	@FXML
	private ChoiceBox<String> maintananceDocMethodChoiceBox;
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}



