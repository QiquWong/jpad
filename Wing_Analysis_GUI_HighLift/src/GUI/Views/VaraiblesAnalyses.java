package GUI.Views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;

import Calculator.InputOutputTree;
import Calculator.Reader;
import Calculator.WingAnalysisCalculator;
import GUI.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import standaloneutils.MyArrayUtils;
import standaloneutils.customdata.MyArray;

public class VaraiblesAnalyses {
	
	InputOutputTree theInputOutputTree;
	private Main main;
	
	@FXML
	RadioButton enableLoadAnalysis;
	@FXML
	RadioButton disableLoadAnalysis;
	@FXML
	RadioButton enableLiftAnalysis;
	@FXML
	RadioButton disableLiftAnalysis;
	@FXML
	RadioButton enableHighLiftAnalysis;
	@FXML
	RadioButton disableHighLiftAnalysis;
	
	@FXML
	RadioButton yesStallPath;
	
	@FXML
	RadioButton noStallPath;
	
	//LOAD
	@FXML
	TextField alphaInitial;
	@FXML
	TextField alphaFinal;
	@FXML
	TextField numberOfAlphasArray;
	@FXML
	ChoiceBox alphaInitialUnits;
	@FXML
	ChoiceBox alphaFinalUnits;	
	@FXML
	ChoiceBox numberOfAlphasValues;	
	@FXML
	Button okButton;	
	@FXML
	TextField alphaOne;
	@FXML
	TextField alphaTwo;
	@FXML
	TextField alphaThree;
	@FXML
	TextField alphaFour;
	@FXML
	TextField alphaFive;
	@FXML
	TextField alphaSix;	
	@FXML
	RadioButton alphasAsArray;
	@FXML
	RadioButton alphasAsInput;	
	@FXML
	Label numberOfAlphasValuesLabel;
	@FXML
	Label alphaAnglesValuesLabel;
	@FXML
	Label alphaInitialLabel;
	@FXML
	Label alphaFinalLabel;
	@FXML
	Label numberOfAlphasArrayLabel;	
	@FXML
	Label arrayLabel;	
	@FXML
	Label valuesLabel;	
	@FXML
	Label alphasInputLabel;
	@FXML
	Label anglesUnitLabel;
	@FXML
	ChoiceBox anglesUnit;
	
	//LIFT
	@FXML
	TextField alphaInitiallLift;
	@FXML
	TextField alphaFinalLift;
	@FXML
	TextField numberOfAlphasArrayLift;
	@FXML
	ChoiceBox alphaInitialUnitsLift;
	@FXML
	ChoiceBox alphaFinalUnitsLift;	
	@FXML
	ChoiceBox numberOfAlphasValuesLift;	
	@FXML
	Button okButtonLift;	
	@FXML
	TextField alphaOneLift;
	@FXML
	TextField alphaTwoLift;
	@FXML
	TextField alphaThreeLift;
	@FXML
	TextField alphaFourLift;
	@FXML
	TextField alphaFiveLift;
	@FXML
	TextField alphaSixLift;	
	@FXML
	TextField alphaSevenLift;	
	@FXML
	TextField alphaEightLift;	
	@FXML
	TextField alphaNineLift;	
	@FXML
	TextField alphaTenLift;	
	@FXML
	RadioButton alphasAsArrayLift;
	@FXML
	RadioButton alphasAsInputLift;	
	@FXML
	Label numberOfAlphasValuesLabelLift;
	@FXML
	Label alphaAnglesValuesLabelLift;
	@FXML
	Label alphaInitialLabelLift;
	@FXML
	Label alphaFinalLabelLift;
	@FXML
	Label numberOfAlphasArrayLabelLift;	
	@FXML
	Label arrayLabelLift;	
	@FXML
	Label valuesLabelLift;	
	@FXML
	Label alphasInputLabelLift;
	@FXML
	Label anglesUnitLabelLift;
	@FXML
	ChoiceBox anglesUnitLift;
	@FXML 
	Label drawStallPath;

	
	//HIGH LIFT
	@FXML
	TextField alphaInitialHighLift;
	@FXML
	TextField alphaFinalHighLift;
	@FXML
	TextField numberOfAlphasArrayHighLift;
	@FXML
	ChoiceBox alphaInitialUnitsHighLift;
	@FXML
	ChoiceBox alphaFinalUnitsHighLift;	
	@FXML
	ChoiceBox numberOfAlphasValuesHighLift;	
	@FXML
	Button okButtonHighLift;	
	@FXML
	TextField alphaOneHighLift;
	@FXML
	TextField alphaTwoHighLift;
	@FXML
	TextField alphaThreeHighLift;
	@FXML
	TextField alphaFourHighLift;
	@FXML
	TextField alphaFiveHighLift;
	@FXML
	TextField alphaSixHighLift;	
	@FXML
	TextField alphaSevenHighLift;	
	@FXML
	TextField alphaEightHighLift;	
	@FXML
	TextField alphaNineHighLift;	
	@FXML
	TextField alphaTenHighLift;	
	@FXML
	RadioButton alphasAsArrayHighLift;
	@FXML
	RadioButton alphasAsInputHighLift;	
	@FXML
	Label numberOfAlphasValuesLabelHighLift;
	@FXML
	Label alphaAnglesValuesLabelHighLift;
	@FXML
	Label alphaInitialLabelHighLift;
	@FXML
	Label alphaFinalLabelHighLift;
	@FXML
	Label numberOfAlphasArrayLabelHighLift;	
	@FXML
	Label arrayLabelHighLift;	
	@FXML
	Label valuesLabelHighLift;	
	@FXML
	Label alphasInputLabelHighLift;
	@FXML
	Label anglesUnitLabelHighLift;
	@FXML
	ChoiceBox anglesUnitHighLift;
	@FXML
	RadioButton yesHighLift;
	@FXML
	RadioButton noHighLift;
	@FXML
	Label performHighLiftLabel;
	
	// OUTPUT----------
	@FXML
	Pane outputPaneFinalLOAD;
	
	@FXML
	GridPane outputPaneTextLOAD;
	
	@FXML
	TextArea outputTextLOAD;
	
	@FXML
	Pane outputPaneFinalLIFT;
	
	@FXML
	GridPane outputPaneTextLIFT;
	
	@FXML
	TextArea outputTextLIFT;
	
	@FXML
	Pane outputPaneFinalHIGHLIFT;
	
	@FXML
	GridPane outputPaneTextHIGHLIFT;
	
	@FXML
	TextArea outputTextHIGHLIFT;
	
	@FXML 
	Tab highLiftTab;
	
	// EXTERNAL CURVES
	
	MenuItem addExternalCurveLoad;
	MenuItem deleteLastCurveLoad;
	MenuItem deleteAllCurveLoad;

	MenuItem addExternalCurveLift;
	MenuItem deleteLastCurveLift;
	MenuItem deleteAllCurveLift;
	
	int runLoad;
	int runLift;
	int runHighLift;
	
	//initialize choiche box
	ObservableList<String> alphaInitialUnitsList = FXCollections.observableArrayList("�","rad" );
	ObservableList<String> alphaFinalUnitsList = FXCollections.observableArrayList("�","rad" );
	
	ObservableList<String> numberOfAlphasInput = FXCollections.observableArrayList("1","2","3","4","5","6" );
	ObservableList<String> numberOfAlphasInputLift = FXCollections.observableArrayList("1","2","3","4","5","6","8","9","10" );
	
	
	//Manage external curves
	
	List<List<Double>> externalLiftDistributionCurves = new ArrayList<>();
	List<List<Double>> xArrayExternalLiftDistributionCurves = new ArrayList<>();
	List<List<Double>> externalLiftCoefficient = new ArrayList<>();
	List<List<Double>> xArrayExternalLiftCoefficient = new ArrayList<>();
	
	@FXML
	private void initialize(){
		alphaInitialUnits.setValue("�");
		alphaInitialUnits.setItems(alphaInitialUnitsList);
	
		alphaFinalUnits.setValue("�");
		alphaFinalUnits.setItems(alphaFinalUnitsList);
		
		numberOfAlphasValues.setItems(numberOfAlphasInput);
		
		anglesUnit.setValue("�");
		anglesUnit.setItems(alphaInitialUnitsList);
		
		alphaInitialUnitsLift.setValue("�");
		alphaInitialUnitsLift.setItems(alphaInitialUnitsList);
	
		alphaFinalUnitsLift.setValue("�");
		alphaFinalUnitsLift.setItems(alphaFinalUnitsList);
		anglesUnitLift.setValue("�");
		anglesUnitLift.setItems(alphaInitialUnitsList);
		
		numberOfAlphasValuesHighLift.setItems(numberOfAlphasInputLift);
		numberOfAlphasValuesLift.setItems(numberOfAlphasInputLift);
		runLoad =0;
		runLift =0;
		runHighLift =0;
		
		alphaInitialUnitsHighLift.setItems(alphaInitialUnitsList);
		alphaInitialUnitsHighLift.setValue("�");
		alphaFinalUnitsHighLift.setItems(alphaInitialUnitsList);
		alphaFinalUnitsHighLift.setValue("�");
		anglesUnitHighLift.setItems(alphaInitialUnitsList);
		anglesUnitHighLift.setValue("�");
		
		
	}
	
	


	
	@FXML
	private void disableInputAsValues(){
		numberOfAlphasValues.setDisable(true);
		okButton.setDisable(true);
		alphaOne.setDisable(true);
		alphaTwo.setDisable(true);
		alphaThree.setDisable(true);
		alphaFour.setDisable(true);
		alphaFive.setDisable(true);
		alphaSix.setDisable(true);
		numberOfAlphasValuesLabel.setDisable(true);
		alphaAnglesValuesLabel.setDisable(true);
		arrayLabel.setDisable(false);
		anglesUnit.setDisable(true);
		anglesUnitLabel.setDisable(true);
		
		alphaInitial.setDisable(false);
		alphaFinal.setDisable(false);
		alphaInitialUnits.setDisable(false);
		alphaFinalUnits.setDisable(false);
		numberOfAlphasArray.setDisable(false);
		alphaInitialLabel.setDisable(false);
		alphaFinalLabel.setDisable(false);
		numberOfAlphasArrayLabel.setDisable(false);	
		valuesLabel.setDisable(true);
	}
	
	@FXML
	private void disableInputAsArray(){
		numberOfAlphasValues.setDisable(false);
		okButton.setDisable(false);
		alphaOne.setDisable(false);
		alphaTwo.setDisable(false);
		alphaThree.setDisable(false);
		alphaFour.setDisable(false);
		alphaFive.setDisable(false);
		alphaSix.setDisable(false);
		numberOfAlphasValuesLabel.setDisable(false);
		alphaAnglesValuesLabel.setDisable(false);
		arrayLabel.setDisable(true);
		anglesUnit.setDisable(false);
		anglesUnitLabel.setDisable(false);
		
		alphaInitial.setDisable(true);
		alphaFinal.setDisable(true);
		alphaInitialUnits.setDisable(true);
		alphaFinalUnits.setDisable(true);
		numberOfAlphasArray.setDisable(true);
		alphaInitialLabel.setDisable(true);
		alphaFinalLabel.setDisable(true);
		numberOfAlphasArrayLabel.setDisable(true);
		valuesLabel.setDisable(false);
		
	}
	
	@FXML
	private void disableLoadAnalysis(){
		numberOfAlphasValues.setDisable(true);
		okButton.setDisable(true);
		alphaOne.setDisable(true);
		alphaTwo.setDisable(true);
		alphaThree.setDisable(true);
		alphaFour.setDisable(true);
		alphaFive.setDisable(true);
		alphaSix.setDisable(true);
		numberOfAlphasValuesLabel.setDisable(true);
		alphaAnglesValuesLabel.setDisable(true);
		anglesUnit.setDisable(true);
		anglesUnitLabel.setDisable(true);
		
		alphaInitial.setDisable(true);
		alphaFinal.setDisable(true);
		alphaInitialUnits.setDisable(true);
		alphaFinalUnits.setDisable(true);
		numberOfAlphasArray.setDisable(true);
		alphaInitialLabel.setDisable(true);
		alphaFinalLabel.setDisable(true);
		numberOfAlphasArrayLabel.setDisable(true);	
		
		alphasAsArray.setDisable(true);
		alphasAsInput.setDisable(true);
		
		arrayLabel.setDisable(true);
		valuesLabel.setDisable(true);
		alphasInputLabel.setDisable(true);
		theInputOutputTree.setPerformLoadAnalysis(false);

		
	}
	
	@FXML
	private void enableLoadAnalysis(){
//		numberOfAlphasValues.setDisable(false);
//		okButton.setDisable(false);
//		alphaOne.setDisable(false);
//		alphaTwo.setDisable(false);
//		alphaThree.setDisable(false);
//		alphaFour.setDisable(false);
//		alphaFive.setDisable(false);
//		alphaSix.setDisable(false);
//		numberOfAlphasValuesLabel.setDisable(false);
//		alphaAnglesValuesLabel.setDisable(false);
//		anglesUnit.setDisable(false);
//		anglesUnitLabel.setDisable(false);
//		
//		alphaInitial.setDisable(false);
//		alphaFinal.setDisable(false);
//		alphaInitialUnits.setDisable(false);
//		alphaFinalUnits.setDisable(false);
//		numberOfAlphasArray.setDisable(false);
//		alphaInitialLabel.setDisable(false);
//		alphaFinalLabel.setDisable(false);
//		numberOfAlphasArrayLabel.setDisable(false);
		
		alphasAsArray.setDisable(false);
		alphasAsInput.setDisable(false);
		
//		arrayLabel.setDisable(false);
//		valuesLabel.setDisable(false);
		alphasInputLabel.setDisable(false);
	}
	
	@FXML
	private void disableHighLiftAnalysis(){
		yesHighLift.setDisable(true);
		noHighLift.setDisable(true);
		performHighLiftLabel.setDisable(true);
		
		numberOfAlphasValuesHighLift.setDisable(true);
		okButtonHighLift.setDisable(true);
		alphaOneHighLift.setDisable(true);
		alphaTwoHighLift.setDisable(true);
		alphaThreeHighLift.setDisable(true);
		alphaFourHighLift.setDisable(true);
		alphaFiveHighLift.setDisable(true);
		alphaSixHighLift.setDisable(true);
		alphaSevenHighLift.setDisable(true);
		alphaEightHighLift.setDisable(true);
		alphaNineHighLift.setDisable(true);
		alphaTenHighLift.setDisable(true);
		numberOfAlphasValuesLabelHighLift.setDisable(true);
		alphaAnglesValuesLabelHighLift.setDisable(true);
		anglesUnitHighLift.setDisable(true);
		anglesUnitLabelHighLift.setDisable(true);
		
		alphaInitialHighLift.setDisable(true);
		alphaFinalHighLift.setDisable(true);
		alphaInitialUnitsHighLift.setDisable(true);
		alphaFinalUnitsHighLift.setDisable(true);
		numberOfAlphasArrayHighLift.setDisable(true);
		alphaInitialLabelHighLift.setDisable(true);
		alphaFinalLabelHighLift.setDisable(true);
		numberOfAlphasArrayLabelHighLift.setDisable(true);	

		arrayLabelHighLift.setDisable(true);
		valuesLabelHighLift.setDisable(true);
		theInputOutputTree.setPerformHighLiftAnalysis(false);
	}
	
	@FXML
	private void enableHighLiftAnalysis(){
		yesHighLift.setDisable(false);
		noHighLift.setDisable(false);
		performHighLiftLabel.setDisable(false);
		
		numberOfAlphasValuesHighLift.setDisable(false);
		okButtonHighLift.setDisable(false);
		alphaOneHighLift.setDisable(false);
		alphaTwoHighLift.setDisable(false);
		alphaThreeHighLift.setDisable(false);
		alphaFourHighLift.setDisable(false);
		alphaFiveHighLift.setDisable(false);
		alphaSixHighLift.setDisable(false);
		alphaSevenHighLift.setDisable(false);
		alphaEightHighLift.setDisable(false);
		alphaNineHighLift.setDisable(false);
		alphaTenHighLift.setDisable(false);
		numberOfAlphasValuesLabelHighLift.setDisable(false);
		alphaAnglesValuesLabelHighLift.setDisable(false);
		anglesUnitHighLift.setDisable(false);
		anglesUnitLabelHighLift.setDisable(false);
		
		alphaInitialHighLift.setDisable(false);
		alphaFinalHighLift.setDisable(false);
		alphaInitialUnitsHighLift.setDisable(false);
		alphaFinalUnitsHighLift.setDisable(false);
		numberOfAlphasArrayHighLift.setDisable(false);
		alphaInitialLabelHighLift.setDisable(false);
		alphaFinalLabelHighLift.setDisable(false);
		numberOfAlphasArrayLabelHighLift.setDisable(false);	

		arrayLabelHighLift.setDisable(false);
		valuesLabelHighLift.setDisable(false);
		theInputOutputTree.setPerformHighLiftAnalysis(true);
	}
	
	@FXML
	private void disableInputAsValuesLift(){
		numberOfAlphasValuesLift.setDisable(true);
		okButtonLift.setDisable(true);
		alphaOneLift.setDisable(true);
		alphaTwoLift.setDisable(true);
		alphaThreeLift.setDisable(true);
		alphaFourLift.setDisable(true);
		alphaFiveLift.setDisable(true);
		alphaSixLift.setDisable(true);
		numberOfAlphasValuesLabelLift.setDisable(true);
//		alphaAnglesValuesLabelLift.setDisable(true);
		arrayLabelLift.setDisable(false);
		anglesUnitLift.setDisable(true);
		anglesUnitLabelLift.setDisable(true);
		
		alphaInitiallLift.setDisable(false);
		alphaFinalLift.setDisable(false);
		alphaInitialUnitsLift.setDisable(false);
		alphaFinalUnitsLift.setDisable(false);
		numberOfAlphasArrayLift.setDisable(false);
		alphaInitialLabelLift.setDisable(false);
		alphaFinalLabelLift.setDisable(false);
		numberOfAlphasArrayLabelLift.setDisable(false);	
		valuesLabelLift.setDisable(true);
	}
	
	@FXML
	private void disableInputAsArrayLift(){
		numberOfAlphasValuesLift.setDisable(false);
		okButtonLift.setDisable(false);
		alphaOneLift.setDisable(false);
		alphaTwoLift.setDisable(false);
		alphaThreeLift.setDisable(false);
		alphaFourLift.setDisable(false);
		alphaFiveLift.setDisable(false);
		alphaSixLift.setDisable(false);
		alphaSevenLift.setDisable(false);
		alphaEightLift.setDisable(false);
		alphaNineLift.setDisable(false);
		alphaTenLift.setDisable(false);
		numberOfAlphasValuesLabelLift.setDisable(false);
		alphaAnglesValuesLabelLift.setDisable(false);
		arrayLabelLift.setDisable(true);
		anglesUnitLift.setDisable(false);
		anglesUnitLabelLift.setDisable(false);
		
		alphaInitiallLift.setDisable(true);
		alphaFinalLift.setDisable(true);
		alphaInitialUnitsLift.setDisable(true);
		alphaFinalUnitsLift.setDisable(true);
		numberOfAlphasArrayLift.setDisable(true);
		alphaInitialLabelLift.setDisable(true);
		alphaFinalLabelLift.setDisable(true);
		numberOfAlphasArrayLabelLift.setDisable(true);
		valuesLabelLift.setDisable(false);
		
	}
	
	@FXML
	private void disableLoadAnalysisLift(){
		numberOfAlphasValuesLift.setDisable(true);
		okButtonLift.setDisable(true);
		alphaOneLift.setDisable(true);
		alphaTwoLift.setDisable(true);
		alphaThreeLift.setDisable(true);
		alphaFourLift.setDisable(true);
		alphaFiveLift.setDisable(true);
		alphaSixLift.setDisable(true);
		alphaSevenLift.setDisable(true);
		alphaEightLift.setDisable(true);
		alphaNineLift.setDisable(true);
		alphaTenLift.setDisable(true);
		numberOfAlphasValuesLabelLift.setDisable(true);
		alphaAnglesValuesLabelLift.setDisable(true);
		anglesUnitLift.setDisable(true);
		anglesUnitLabelLift.setDisable(true);
		
		alphaInitiallLift.setDisable(true);
		alphaFinalLift.setDisable(true);
		alphaInitialUnitsLift.setDisable(true);
		alphaFinalUnitsLift.setDisable(true);
		numberOfAlphasArrayLift.setDisable(true);
		alphaInitialLabelLift.setDisable(true);
		alphaFinalLabelLift.setDisable(true);
		numberOfAlphasArrayLabelLift.setDisable(true);	
		
		alphasAsArrayLift.setDisable(true);
		alphasAsInputLift.setDisable(true);
		
		arrayLabelLift.setDisable(true);
		valuesLabelLift.setDisable(true);
		alphasInputLabelLift.setDisable(true);
		
		theInputOutputTree.setPerformLiftAnalysis(false);
		
		
		drawStallPath.setDisable(true);
		yesStallPath.setDisable(true);
		noStallPath.setDisable(true);
	}
	
	
	@FXML
	private void enableLoadAnalysisLift(){
			
		drawStallPath.setDisable(false);
		yesStallPath.setDisable(false);
		noStallPath.setDisable(false);
		
//		numberOfAlphasValuesLift.setDisable(false);
//		okButtonLift.setDisable(false);
//		alphaOneLift.setDisable(false);
//		alphaTwoLift.setDisable(false);
//		alphaThreeLift.setDisable(false);
//		alphaFourLift.setDisable(false);
//		alphaFiveLift.setDisable(false);
//		alphaSixLift.setDisable(false);
//		alphaSevenLift.setDisable(false);
//		alphaEightLift.setDisable(false);
//		alphaNineLift.setDisable(false);
//		alphaTenLift.setDisable(false);
//		numberOfAlphasValuesLabelLift.setDisable(false);
//		alphaAnglesValuesLabelLift.setDisable(false);
//		anglesUnitLift.setDisable(false);
//		anglesUnitLabelLift.setDisable(false);
//		
//		alphaInitiallLift.setDisable(false);
//		alphaFinalLift.setDisable(false);
//		alphaInitialUnitsLift.setDisable(false);
//		alphaFinalUnitsLift.setDisable(false);
//		numberOfAlphasArrayLift.setDisable(false);
//		alphaInitialLabelLift.setDisable(false);
//		alphaFinalLabelLift.setDisable(false);
//		numberOfAlphasArrayLabelLift.setDisable(false);
//		
		alphasAsArrayLift.setDisable(false);
		alphasAsInputLift.setDisable(false);
		
//		arrayLabelLift.setDisable(false);
//		valuesLabelLift.setDisable(false);
		alphasInputLabelLift.setDisable(false);
		
	}
	
	
	@FXML
	private void disableLoadDistributionHighLift(){
		numberOfAlphasValuesHighLift.setDisable(true);
		okButtonHighLift.setDisable(true);
		alphaOneHighLift.setDisable(true);
		alphaTwoHighLift.setDisable(true);
		alphaThreeHighLift.setDisable(true);
		alphaFourHighLift.setDisable(true);
		alphaFiveHighLift.setDisable(true);
		alphaSixHighLift.setDisable(true);
		alphaSevenHighLift.setDisable(true);
		alphaEightHighLift.setDisable(true);
		alphaNineHighLift.setDisable(true);
		alphaTenHighLift.setDisable(true);
		numberOfAlphasValuesLabelHighLift.setDisable(true);
		alphaAnglesValuesLabelHighLift.setDisable(true);
		anglesUnitHighLift.setDisable(true);
		anglesUnitLabelHighLift.setDisable(true);
	}
	
	@FXML
	private void enableLoadDistributionHighLift(){
		numberOfAlphasValuesHighLift.setDisable(false);
		okButtonHighLift.setDisable(false);
		alphaOneHighLift.setDisable(false);
		alphaTwoHighLift.setDisable(false);
		alphaThreeHighLift.setDisable(false);
		alphaFourHighLift.setDisable(false);
		alphaFiveHighLift.setDisable(false);
		alphaSixHighLift.setDisable(false);
		alphaSevenHighLift.setDisable(false);
		alphaEightHighLift.setDisable(false);
		alphaNineHighLift.setDisable(false);
		alphaTenHighLift.setDisable(false);
		numberOfAlphasValuesLabelHighLift.setDisable(false);
		alphaAnglesValuesLabelHighLift.setDisable(false);
		anglesUnitHighLift.setDisable(false);
		anglesUnitLabelHighLift.setDisable(false);
	}
	@FXML
	public void setNumberOfGivenSection() throws IOException{
		if((int)Double.parseDouble(numberOfAlphasValues.getValue().toString()) == 1){
			alphaOne.setDisable(false);
			alphaTwo.setDisable(true);
			alphaThree.setDisable(true);
			alphaFour.setDisable(true);
			alphaFive.setDisable(true);
			alphaSix.setDisable(true);}

		if((int)Double.parseDouble(numberOfAlphasValues.getValue().toString()) == 2){
			alphaOne.setDisable(false);
			alphaTwo.setDisable(false);
			alphaThree.setDisable(true);
			alphaFour.setDisable(true);
			alphaFive.setDisable(true);
			alphaSix.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValues.getValue().toString()) == 3){
			alphaOne.setDisable(false);
			alphaTwo.setDisable(false);
			alphaThree.setDisable(false);
			alphaFour.setDisable(true);
			alphaFive.setDisable(true);
			alphaSix.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValues.getValue().toString()) == 4){
			alphaOne.setDisable(false);
			alphaTwo.setDisable(false);
			alphaThree.setDisable(false);
			alphaFour.setDisable(false);
			alphaFive.setDisable(true);
			alphaSix.setDisable(true);
		}
		
		if((int)Double.parseDouble(numberOfAlphasValues.getValue().toString()) == 5){
			alphaOne.setDisable(false);
			alphaTwo.setDisable(false);
			alphaThree.setDisable(false);
			alphaFour.setDisable(false);
			alphaFive.setDisable(false);
			alphaSix.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValues.getValue().toString()) == 6){
			alphaOne.setDisable(false);
			alphaTwo.setDisable(false);
			alphaThree.setDisable(false);
			alphaFour.setDisable(false);
			alphaFive.setDisable(false);
			alphaSix.setDisable(false);
		}
	}
	
	@FXML
	public void setNumberOfGivenSectionLift() throws IOException{
		if((int)Double.parseDouble(numberOfAlphasValuesLift.getValue().toString()) == 1){
			alphaOneLift.setDisable(false);
			alphaTwoLift.setDisable(true);
			alphaThreeLift.setDisable(true);
			alphaFourLift.setDisable(true);
			alphaFiveLift.setDisable(true);
			alphaSixLift.setDisable(true);
			alphaSevenLift.setDisable(true);
			alphaEightLift.setDisable(true);
			alphaNineLift.setDisable(true);
			alphaTenLift.setDisable(true);
			}

		if((int)Double.parseDouble(numberOfAlphasValuesLift.getValue().toString()) == 2){
			alphaOneLift.setDisable(false);
			alphaTwoLift.setDisable(false);
			alphaThreeLift.setDisable(true);
			alphaFourLift.setDisable(true);
			alphaFiveLift.setDisable(true);
			alphaSixLift.setDisable(true);
			alphaSevenLift.setDisable(true);
			alphaEightLift.setDisable(true);
			alphaNineLift.setDisable(true);
			alphaTenLift.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValuesLift.getValue().toString()) == 3){
			alphaOneLift.setDisable(false);
			alphaTwoLift.setDisable(false);
			alphaThreeLift.setDisable(false);
			alphaFourLift.setDisable(true);
			alphaFiveLift.setDisable(true);
			alphaSixLift.setDisable(true);
			alphaSevenLift.setDisable(true);
			alphaEightLift.setDisable(true);
			alphaNineLift.setDisable(true);
			alphaTenLift.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValuesLift.getValue().toString()) == 4){
			alphaOneLift.setDisable(false);
			alphaTwoLift.setDisable(false);
			alphaThreeLift.setDisable(false);
			alphaFourLift.setDisable(false);
			alphaFiveLift.setDisable(true);
			alphaSixLift.setDisable(true);
			alphaSevenLift.setDisable(true);
			alphaEightLift.setDisable(true);
			alphaNineLift.setDisable(true);
			alphaTenLift.setDisable(true);
		}
		
		if((int)Double.parseDouble(numberOfAlphasValuesLift.getValue().toString()) == 5){
			alphaOneLift.setDisable(false);
			alphaTwoLift.setDisable(false);
			alphaThreeLift.setDisable(false);
			alphaFourLift.setDisable(false);
			alphaFiveLift.setDisable(false);
			alphaSixLift.setDisable(true);
			alphaSevenLift.setDisable(true);
			alphaEightLift.setDisable(true);
			alphaNineLift.setDisable(true);
			alphaTenLift.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValuesLift.getValue().toString()) == 6){
			alphaOneLift.setDisable(false);
			alphaTwoLift.setDisable(false);
			alphaThreeLift.setDisable(false);
			alphaFourLift.setDisable(false);
			alphaFiveLift.setDisable(false);
			alphaSixLift.setDisable(false);
			alphaSevenLift.setDisable(true);
			alphaEightLift.setDisable(true);
			alphaNineLift.setDisable(true);
			alphaTenLift.setDisable(true);
		}
		
		if((int)Double.parseDouble(numberOfAlphasValuesLift.getValue().toString()) == 7){
			alphaOneLift.setDisable(false);
			alphaTwoLift.setDisable(false);
			alphaThreeLift.setDisable(false);
			alphaFourLift.setDisable(false);
			alphaFiveLift.setDisable(false);
			alphaSixLift.setDisable(false);
			alphaSevenLift.setDisable(false);
			alphaEightLift.setDisable(true);
			alphaNineLift.setDisable(true);
			alphaTenLift.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValuesLift.getValue().toString()) == 8){
			alphaOneLift.setDisable(false);
			alphaTwoLift.setDisable(false);
			alphaThreeLift.setDisable(false);
			alphaFourLift.setDisable(false);
			alphaFiveLift.setDisable(false);
			alphaSixLift.setDisable(false);
			alphaSevenLift.setDisable(false);
			alphaEightLift.setDisable(false);
			alphaNineLift.setDisable(true);
			alphaTenLift.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValuesLift.getValue().toString()) == 9){
			alphaOneLift.setDisable(false);
			alphaTwoLift.setDisable(false);
			alphaThreeLift.setDisable(false);
			alphaFourLift.setDisable(false);
			alphaFiveLift.setDisable(false);
			alphaSixLift.setDisable(false);
			alphaSevenLift.setDisable(false);
			alphaEightLift.setDisable(false);
			alphaNineLift.setDisable(false);
			alphaTenLift.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValuesLift.getValue().toString()) == 10){
			alphaOneLift.setDisable(false);
			alphaTwoLift.setDisable(false);
			alphaThreeLift.setDisable(false);
			alphaFourLift.setDisable(false);
			alphaFiveLift.setDisable(false);
			alphaSixLift.setDisable(false);
			alphaSevenLift.setDisable(false);
			alphaEightLift.setDisable(false);
			alphaNineLift.setDisable(false);
			alphaTenLift.setDisable(false);
		}
	}
	
	@FXML
	public void setNumberOfGivenSectionHighLift() throws IOException{
		if((int)Double.parseDouble(numberOfAlphasValuesHighLift.getValue().toString()) == 1){
			alphaOneHighLift.setDisable(false);
			alphaTwoHighLift.setDisable(true);
			alphaThreeHighLift.setDisable(true);
			alphaFourHighLift.setDisable(true);
			alphaFiveHighLift.setDisable(true);
			alphaSixHighLift.setDisable(true);
			alphaSevenHighLift.setDisable(true);
			alphaEightHighLift.setDisable(true);
			alphaNineHighLift.setDisable(true);
			alphaTenHighLift.setDisable(true);
			}

		if((int)Double.parseDouble(numberOfAlphasValuesHighLift.getValue().toString()) == 2){
			alphaOneHighLift.setDisable(false);
			alphaTwoHighLift.setDisable(false);
			alphaThreeHighLift.setDisable(true);
			alphaFourHighLift.setDisable(true);
			alphaFiveHighLift.setDisable(true);
			alphaSixHighLift.setDisable(true);
			alphaSevenHighLift.setDisable(true);
			alphaEightHighLift.setDisable(true);
			alphaNineHighLift.setDisable(true);
			alphaTenHighLift.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValuesHighLift.getValue().toString()) == 3){
			alphaOneHighLift.setDisable(false);
			alphaTwoHighLift.setDisable(false);
			alphaThreeHighLift.setDisable(false);
			alphaFourHighLift.setDisable(true);
			alphaFiveHighLift.setDisable(true);
			alphaSixHighLift.setDisable(true);
			alphaSevenHighLift.setDisable(true);
			alphaEightHighLift.setDisable(true);
			alphaNineHighLift.setDisable(true);
			alphaTenHighLift.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValuesHighLift.getValue().toString()) == 4){
			alphaOneHighLift.setDisable(false);
			alphaTwoHighLift.setDisable(false);
			alphaThreeHighLift.setDisable(false);
			alphaFourHighLift.setDisable(false);
			alphaFiveHighLift.setDisable(true);
			alphaSixHighLift.setDisable(true);
			alphaSevenHighLift.setDisable(true);
			alphaEightHighLift.setDisable(true);
			alphaNineHighLift.setDisable(true);
			alphaTenHighLift.setDisable(true);
		}
		
		if((int)Double.parseDouble(numberOfAlphasValuesHighLift.getValue().toString()) == 5){
			alphaOneHighLift.setDisable(false);
			alphaTwoHighLift.setDisable(false);
			alphaThreeHighLift.setDisable(false);
			alphaFourHighLift.setDisable(false);
			alphaFiveHighLift.setDisable(false);
			alphaSixHighLift.setDisable(true);
			alphaSevenHighLift.setDisable(true);
			alphaEightHighLift.setDisable(true);
			alphaNineHighLift.setDisable(true);
			alphaTenHighLift.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValuesHighLift.getValue().toString()) == 6){
			alphaOneHighLift.setDisable(false);
			alphaTwoHighLift.setDisable(false);
			alphaThreeHighLift.setDisable(false);
			alphaFourHighLift.setDisable(false);
			alphaFiveHighLift.setDisable(false);
			alphaSixHighLift.setDisable(false);
			alphaSevenHighLift.setDisable(true);
			alphaEightHighLift.setDisable(true);
			alphaNineHighLift.setDisable(true);
			alphaTenHighLift.setDisable(true);
		}
		
		if((int)Double.parseDouble(numberOfAlphasValuesHighLift.getValue().toString()) == 7){
			alphaOneHighLift.setDisable(false);
			alphaTwoHighLift.setDisable(false);
			alphaThreeHighLift.setDisable(false);
			alphaFourHighLift.setDisable(false);
			alphaFiveHighLift.setDisable(false);
			alphaSixHighLift.setDisable(false);
			alphaSevenHighLift.setDisable(false);
			alphaEightHighLift.setDisable(true);
			alphaNineHighLift.setDisable(true);
			alphaTenHighLift.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValuesHighLift.getValue().toString()) == 8){
			alphaOneHighLift.setDisable(false);
			alphaTwoHighLift.setDisable(false);
			alphaThreeHighLift.setDisable(false);
			alphaFourHighLift.setDisable(false);
			alphaFiveHighLift.setDisable(false);
			alphaSixHighLift.setDisable(false);
			alphaSevenHighLift.setDisable(false);
			alphaEightHighLift.setDisable(false);
			alphaNineHighLift.setDisable(true);
			alphaTenHighLift.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValuesHighLift.getValue().toString()) == 9){
			alphaOneHighLift.setDisable(false);
			alphaTwoHighLift.setDisable(false);
			alphaThreeHighLift.setDisable(false);
			alphaFourHighLift.setDisable(false);
			alphaFiveHighLift.setDisable(false);
			alphaSixHighLift.setDisable(false);
			alphaSevenHighLift.setDisable(false);
			alphaEightHighLift.setDisable(false);
			alphaNineHighLift.setDisable(false);
			alphaTenHighLift.setDisable(true);
		}
		if((int)Double.parseDouble(numberOfAlphasValuesHighLift.getValue().toString()) == 10){
			alphaOneHighLift.setDisable(false);
			alphaTwoHighLift.setDisable(false);
			alphaThreeHighLift.setDisable(false);
			alphaFourHighLift.setDisable(false);
			alphaFiveHighLift.setDisable(false);
			alphaSixHighLift.setDisable(false);
			alphaSevenHighLift.setDisable(false);
			alphaEightHighLift.setDisable(false);
			alphaNineHighLift.setDisable(false);
			alphaTenHighLift.setDisable(false);
		}
	}
		
	//ANALYSES
	//-----------------------------------------------------------------------------------------------
	
	@FXML
	private void performLoadAnalyses(){
		theInputOutputTree.setPerformLoadAnalysis(true);
		List<Amount<Angle>> alphaLoadArray = new ArrayList<>();
		
		theInputOutputTree.setAlphaArrayLiftDistribution(new ArrayList<>());
		
		//filling wing load alpha load
		if (alphasAsArray.isSelected()){
			Amount<Angle> alphaInitialAmount = Amount.valueOf(
					Double.parseDouble(alphaInitial.getText()),
					main.recognizeUnit(alphaInitialUnits));
			
			Amount<Angle> alphaFinalAmount = Amount.valueOf(
					Double.parseDouble(alphaFinal.getText()),
					main.recognizeUnit(alphaFinalUnits));
			
			alphaLoadArray = MyArrayUtils.convertDoubleArrayToListOfAmount(
					MyArrayUtils.linspace(
					alphaInitialAmount.doubleValue(NonSI.DEGREE_ANGLE),
					alphaFinalAmount.doubleValue(NonSI.DEGREE_ANGLE),
					(int)Double.parseDouble(numberOfAlphasArray.getText())),
					NonSI.DEGREE_ANGLE);
		}
		
		if (alphasAsInput.isSelected()){
			double [] alphaArray = new double[(int) Double.parseDouble(numberOfAlphasValues.getValue().toString())];
			int i=0;
			if(!alphaOne.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaOne.getText()); 
				i++;
			}
			if(!alphaTwo.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaTwo.getText()); 
				i++;
			}
			if(!alphaThree.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaThree.getText()); 
				i++;
			}
			if(!alphaFour.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaFour.getText()); 
				i++;
			}
			if(!alphaFive.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaFive.getText()); 
				i++;
			}
			if(!alphaSix.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaSix.getText()); 
				i++;
			}
			
			alphaLoadArray = MyArrayUtils.convertDoubleArrayToListOfAmount(
					alphaArray, 
					main.recognizeUnit(anglesUnit)
					);
		}
		
		theInputOutputTree.setAlphaArrayLiftDistribution(alphaLoadArray);
		WingAnalysisCalculator.calculateLoadDistributions(theInputOutputTree, this);
		theInputOutputTree.getSaveButton().setDisable(false);
		
	}


	@FXML
	private void performLiftAnalyses(){
		theInputOutputTree.setPerformLiftAnalysis(true);
		List<Amount<Angle>> alphaLiftArray = new ArrayList<>();
		
		theInputOutputTree.setAlphaArrayLiftCurve(new ArrayList<>());
		
		//filling wing load alpha load
		if (alphasAsArrayLift.isSelected()){
			Amount<Angle> alphaInitialAmount = Amount.valueOf(
					Double.parseDouble(alphaInitiallLift.getText()),
					main.recognizeUnit(alphaInitialUnitsLift));
			
			Amount<Angle> alphaFinalAmount = Amount.valueOf(
					Double.parseDouble(alphaFinalLift.getText()),
					main.recognizeUnit(alphaFinalUnitsLift));
			
			alphaLiftArray = MyArrayUtils.convertDoubleArrayToListOfAmount(
					MyArrayUtils.linspace(
					alphaInitialAmount.doubleValue(NonSI.DEGREE_ANGLE),
					alphaFinalAmount.doubleValue(NonSI.DEGREE_ANGLE),
					(int)Double.parseDouble(numberOfAlphasArrayLift.getText())),
					NonSI.DEGREE_ANGLE);
		}
		
		if (alphasAsInputLift.isSelected()){
			double [] alphaArray = new double[(int) Double.parseDouble(numberOfAlphasValuesLift.getValue().toString())];
			int i=0;
			if(!alphaOneLift.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaOneLift.getText()); 
				i++;
			}
			if(!alphaTwoLift.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaTwoLift.getText()); 
				i++;
			}
			if(!alphaThreeLift.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaThreeLift.getText()); 
				i++;
			}
			if(!alphaFourLift.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaFourLift.getText()); 
				i++;
			}
			if(!alphaFiveLift.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaFiveLift.getText()); 
				i++;
			}
			if(!alphaSixLift.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaSixLift.getText()); 
				i++;
			}
			if(!alphaSevenLift.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaSevenLift.getText()); 
				i++;
			}
			if(!alphaEightLift.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaEightLift.getText()); 
				i++;
			}
			if(!alphaNineLift.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaNineLift.getText()); 
				i++;
			}
			if(!alphaTenLift.getText().trim().isEmpty()){
				alphaArray[i] = Double.parseDouble(alphaTenLift.getText()); 
				i++;
			}
			
			Arrays.sort(alphaArray);
			
			alphaLiftArray = MyArrayUtils.convertDoubleArrayToListOfAmount(
					alphaArray, 
					main.recognizeUnit(anglesUnitLift)
					);
		}
		
		theInputOutputTree.setAlphaArrayLiftCurve(alphaLiftArray);
		WingAnalysisCalculator.calculateLiftCurve(theInputOutputTree, this);
		theInputOutputTree.getSaveButton().setDisable(false);
		
	}
	@FXML
	private void performHighLiftAnalyses(){
		// SET DATA IN INPUT TREE
		
		//
		Amount<Angle> alphaInitialAmount = Amount.valueOf(
				Double.parseDouble(alphaInitialHighLift.getText()),
				main.recognizeUnit(alphaInitialUnitsHighLift));
		
		Amount<Angle> alphaFinalAmount = Amount.valueOf(
				Double.parseDouble(alphaFinalHighLift.getText()),
				main.recognizeUnit(alphaFinalUnitsHighLift));
		
		List<Amount<Angle>>alphaLiftArray = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(
				alphaInitialAmount.doubleValue(NonSI.DEGREE_ANGLE),
				alphaFinalAmount.doubleValue(NonSI.DEGREE_ANGLE),
				(int)Double.parseDouble(numberOfAlphasArrayHighLift.getText())),
				NonSI.DEGREE_ANGLE);
		
		theInputOutputTree.setAlphaArrayHighLiftCurve(alphaLiftArray);
		
		// high lift distribution
		
		if(yesHighLift.isSelected()) {
		double [] alphaArray = new double[(int) Double.parseDouble(numberOfAlphasValuesHighLift.getValue().toString())];
		int i=0;
		if(!alphaOneHighLift.getText().trim().isEmpty()){
			alphaArray[i] = Double.parseDouble(alphaOneHighLift.getText()); 
			i++;
		}
		if(!alphaTwoHighLift.getText().trim().isEmpty()){
			alphaArray[i] = Double.parseDouble(alphaTwoHighLift.getText()); 
			i++;
		}
		if(!alphaThreeHighLift.getText().trim().isEmpty()){
			alphaArray[i] = Double.parseDouble(alphaThreeHighLift.getText()); 
			i++;
		}
		if(!alphaFourHighLift.getText().trim().isEmpty()){
			alphaArray[i] = Double.parseDouble(alphaFourHighLift.getText()); 
			i++;
		}
		if(!alphaFiveHighLift.getText().trim().isEmpty()){
			alphaArray[i] = Double.parseDouble(alphaFiveHighLift.getText()); 
			i++;
		}
		if(!alphaSixHighLift.getText().trim().isEmpty()){
			alphaArray[i] = Double.parseDouble(alphaSixHighLift.getText()); 
			i++;
		}
		if(!alphaSevenHighLift.getText().trim().isEmpty()){
			alphaArray[i] = Double.parseDouble(alphaSevenHighLift.getText()); 
			i++;
		}
		if(!alphaEightHighLift.getText().trim().isEmpty()){
			alphaArray[i] = Double.parseDouble(alphaEightHighLift.getText()); 
			i++;
		}
		if(!alphaNineHighLift.getText().trim().isEmpty()){
			alphaArray[i] = Double.parseDouble(alphaNineHighLift.getText()); 
			i++;
		}
		if(!alphaTenHighLift.getText().trim().isEmpty()){
			alphaArray[i] = Double.parseDouble(alphaTenHighLift.getText()); 
			i++;
		}
		
		Arrays.sort(alphaArray);
		
		alphaLiftArray = MyArrayUtils.convertDoubleArrayToListOfAmount(
				alphaArray, 
				main.recognizeUnit(anglesUnitHighLift)
				);
		
		theInputOutputTree.setAlphaArrayHighLiftDistribution(alphaLiftArray);
		}
		// LIFT CURVE
		WingAnalysisCalculator.calculateHighLiftCurve(theInputOutputTree, this);
		theInputOutputTree.getSaveButton().setDisable(false);
	

		
	}
	
	public void addExternalCurveLoadFunct() throws IOException{
	Main.addNewCurveLoad(this);
		
	}

	public InputOutputTree getTheInputOutputTree() {
		return theInputOutputTree;
	}





	public void setTheInputOutputTree(InputOutputTree theInputOutputTree) {
		this.theInputOutputTree = theInputOutputTree;
	}




	public Pane getOutputPaneFinalLOAD() {
		return outputPaneFinalLOAD;
	}





	public void setOutputPaneFinalLOAD(Pane outputPaneFinalLOAD) {
		this.outputPaneFinalLOAD = outputPaneFinalLOAD;
	}





	public GridPane getOutputPaneTextLOAD() {
		return outputPaneTextLOAD;
	}





	public void setOutputPaneTextLOAD(GridPane outputPaneTextLOAD) {
		this.outputPaneTextLOAD = outputPaneTextLOAD;
	}





	public TextArea getOutputTextLOAD() {
		return outputTextLOAD;
	}





	public void setOutputTextLOAD(TextArea outputTextLOAD) {
		this.outputTextLOAD = outputTextLOAD;
	}





	public int getRunLoad() {
		return runLoad;
	}





	public void setRunLoad(int runLoad) {
		this.runLoad = runLoad;
	}





	public Pane getOutputPaneFinalLIFT() {
		return outputPaneFinalLIFT;
	}





	public GridPane getOutputPaneTextLIFT() {
		return outputPaneTextLIFT;
	}





	public TextArea getOutputTextLIFT() {
		return outputTextLIFT;
	}





	public void setOutputPaneFinalLIFT(Pane outputPaneFinalLIFT) {
		this.outputPaneFinalLIFT = outputPaneFinalLIFT;
	}





	public void setOutputPaneTextLIFT(GridPane outputPaneTextLIFT) {
		this.outputPaneTextLIFT = outputPaneTextLIFT;
	}





	public void setOutputTextLIFT(TextArea outputTextLIFT) {
		this.outputTextLIFT = outputTextLIFT;
	}





	public int getRunLift() {
		return runLift;
	}





	public void setRunLift(int runLift) {
		this.runLift = runLift;
	}





	public RadioButton getYesStallPath() {
		return yesStallPath;
	}





	public RadioButton getNoStallPath() {
		return noStallPath;
	}





	public void setYesStallPath(RadioButton yesStallPath) {
		this.yesStallPath = yesStallPath;
	}





	public void setNoStallPath(RadioButton noStallPath) {
		this.noStallPath = noStallPath;
	}





	public Main getMain() {
		return main;
	}





	public void setMain(Main main) {
		this.main = main;
	}





	public List<List<Double>> getExternalLiftDistributionCurves() {
		return externalLiftDistributionCurves;
	}





	public List<List<Double>> getExternalLiftCoefficient() {
		return externalLiftCoefficient;
	}





	public void setExternalLiftDistributionCurves(List<List<Double>> externalLiftDistributionCurves) {
		this.externalLiftDistributionCurves = externalLiftDistributionCurves;
	}





	public void setExternalLiftCoefficient(List<List<Double>> externalLiftCoefficient) {
		this.externalLiftCoefficient = externalLiftCoefficient;
	}





	public List<List<Double>> getxArrayExternalLiftDistributionCurves() {
		return xArrayExternalLiftDistributionCurves;
	}





	public List<List<Double>> getxArrayExternalLiftCoefficient() {
		return xArrayExternalLiftCoefficient;
	}





	public void setxArrayExternalLiftDistributionCurves(List<List<Double>> xArrayExternalLiftDistributionCurves) {
		this.xArrayExternalLiftDistributionCurves = xArrayExternalLiftDistributionCurves;
	}





	public void setxArrayExternalLiftCoefficient(List<List<Double>> xArrayExternalLiftCoefficient) {
		this.xArrayExternalLiftCoefficient = xArrayExternalLiftCoefficient;
	}





	public int getRunHighLift() {
		return runHighLift;
	}





	public void setRunHighLift(int runHighLift) {
		this.runHighLift = runHighLift;
	}





	public Pane getOutputPaneFinalHIGHLIFT() {
		return outputPaneFinalHIGHLIFT;
	}





	public GridPane getOutputPaneTextHIGHLIFT() {
		return outputPaneTextHIGHLIFT;
	}





	public TextArea getOutputTextHIGHLIFT() {
		return outputTextHIGHLIFT;
	}





	public void setOutputPaneFinalHIGHLIFT(Pane outputPaneFinalHIGHLIFT) {
		this.outputPaneFinalHIGHLIFT = outputPaneFinalHIGHLIFT;
	}





	public void setOutputPaneTextHIGHLIFT(GridPane outputPaneTextHIGHLIFT) {
		this.outputPaneTextHIGHLIFT = outputPaneTextHIGHLIFT;
	}





	public void setOutputTextHIGHLIFT(TextArea outputTextHIGHLIFT) {
		this.outputTextHIGHLIFT = outputTextHIGHLIFT;
	}





	public Tab getHighLiftTab() {
		return highLiftTab;
	}





	public void setHighLiftTab(Tab highLiftTab) {
		this.highLiftTab = highLiftTab;
	}





	public RadioButton getYesHighLift() {
		return yesHighLift;
	}





	public void setYesHighLift(RadioButton yesHighLift) {
		this.yesHighLift = yesHighLift;
	}





	public RadioButton getNoHighLift() {
		return noHighLift;
	}





	public void setNoHighLift(RadioButton noHighLift) {
		this.noHighLift = noHighLift;
	}
	
}
