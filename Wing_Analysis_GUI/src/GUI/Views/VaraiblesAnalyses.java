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
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
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
	
	int runLoad;
	int runLift;
	
	//initialize choiche box
	ObservableList<String> alphaInitialUnitsList = FXCollections.observableArrayList("°","rad" );
	ObservableList<String> alphaFinalUnitsList = FXCollections.observableArrayList("°","rad" );
	
	ObservableList<String> numberOfAlphasInput = FXCollections.observableArrayList("1","2","3","4","5","6" );
	ObservableList<String> numberOfAlphasInputLift = FXCollections.observableArrayList("1","2","3","4","5","6","8","9","10" );
	
	@FXML
	private void initialize(){
		alphaInitialUnits.setValue("°");
		alphaInitialUnits.setItems(alphaInitialUnitsList);
	
		alphaFinalUnits.setValue("°");
		alphaFinalUnits.setItems(alphaFinalUnitsList);
		
		numberOfAlphasValues.setItems(numberOfAlphasInput);
		
		anglesUnit.setValue("°");
		anglesUnit.setItems(alphaInitialUnitsList);
		
		alphaInitialUnitsLift.setValue("°");
		alphaInitialUnitsLift.setItems(alphaInitialUnitsList);
	
		alphaFinalUnitsLift.setValue("°");
		alphaFinalUnitsLift.setItems(alphaFinalUnitsList);
		anglesUnitLift.setValue("°");
		anglesUnitLift.setItems(alphaInitialUnitsList);
		
		
		numberOfAlphasValuesLift.setItems(numberOfAlphasInputLift);
		runLoad =0;
		runLift =0;
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
		arrayLabel.setDisable(true);
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
		valuesLabel.setDisable(false);
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
		arrayLabel.setDisable(false);
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
		valuesLabel.setDisable(true);
		
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
	}
	
	@FXML
	private void enableLoadAnalysis(){
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
		anglesUnit.setDisable(false);
		anglesUnitLabel.setDisable(false);
		
		alphaInitial.setDisable(false);
		alphaFinal.setDisable(false);
		alphaInitialUnits.setDisable(false);
		alphaFinalUnits.setDisable(false);
		numberOfAlphasArray.setDisable(false);
		alphaInitialLabel.setDisable(false);
		alphaFinalLabel.setDisable(false);
		numberOfAlphasArrayLabel.setDisable(false);
		
		alphasAsArray.setDisable(false);
		alphasAsInput.setDisable(false);
		
		arrayLabel.setDisable(false);
		valuesLabel.setDisable(false);
		alphasInputLabel.setDisable(false);
		
		
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
		alphaAnglesValuesLabelLift.setDisable(true);
		arrayLabelLift.setDisable(true);
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
		valuesLabelLift.setDisable(false);
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
		arrayLabelLift.setDisable(false);
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
		valuesLabelLift.setDisable(true);
		
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
	}
	
	@FXML
	private void enableLoadAnalysisLift(){
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
		anglesUnitLift.setDisable(false);
		anglesUnitLabelLift.setDisable(false);
		
		alphaInitiallLift.setDisable(false);
		alphaFinalLift.setDisable(false);
		alphaInitialUnitsLift.setDisable(false);
		alphaFinalUnitsLift.setDisable(false);
		numberOfAlphasArrayLift.setDisable(false);
		alphaInitialLabelLift.setDisable(false);
		alphaFinalLabelLift.setDisable(false);
		numberOfAlphasArrayLabelLift.setDisable(false);
		
		alphasAsArrayLift.setDisable(false);
		alphasAsInputLift.setDisable(false);
		
		arrayLabelLift.setDisable(false);
		valuesLabelLift.setDisable(false);
		alphasInputLabelLift.setDisable(false);
		
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
		
	//ANALYSES
	//-----------------------------------------------------------------------------------------------
	
	@FXML
	private void performLoadAnalyses(){
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
	}


	@FXML
	private void performLiftAnalyses(){
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
	
}
