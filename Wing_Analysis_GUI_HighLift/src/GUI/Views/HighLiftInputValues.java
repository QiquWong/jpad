package GUI.Views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import Calculator.InputOutputTree;
import GUI.Main;
import configuration.enumerations.FlapTypeEnum;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

public class HighLiftInputValues {

	InputOutputTree theInputTree;
	VariablesInputData theVariableInputClass;
	
	//flap input values
	@FXML
	List<ChoiceBox> flapTypeFXMLList = new ArrayList<>();	
	@FXML
	List<TextField> flapChordRatioFXMLList = new ArrayList<>();	
	@FXML
	List<TextField> flapAngleFXMLList = new ArrayList<>();	
	@FXML
	List<ChoiceBox> flapAngleUnitFXMLList = new ArrayList<>();	
	@FXML
	List<TextField> flapNonDimensionalInnerFXMLList = new ArrayList<>();	
	@FXML
	List<TextField> flapNonDimensionalOuterFXMLList = new ArrayList<>();	
	
	//flap values
	@FXML
	ChoiceBox flapType1;
	@FXML
	ChoiceBox flapType2;
	@FXML
	ChoiceBox flapType3;
	@FXML
	ChoiceBox flapType4;
	@FXML
	ChoiceBox flapType5;
	@FXML
	ChoiceBox flapUnit1;
	@FXML
	ChoiceBox flapUnit2;
	@FXML
	ChoiceBox flapUnit3;
	@FXML
	ChoiceBox flapUnit4;
	@FXML
	ChoiceBox flapUnit5;
	@FXML
	TextField flapChordRatio1;
	@FXML
	TextField flapChordRatio2;
	@FXML
	TextField flapChordRatio3;
	@FXML
	TextField flapChordRatio4;
	@FXML
	TextField flapChordRatio5;
	@FXML
	TextField flapInnerStation1;
	@FXML
	TextField flapInnerStation2;
	@FXML
	TextField flapInnerStation3;
	@FXML
	TextField flapInnerStation4;
	@FXML
	TextField flapInnerStation5;
	@FXML
	TextField flapOuterStation1;
	@FXML
	TextField flapOuterStation2;
	@FXML
	TextField flapOuterStation3;
	@FXML
	TextField flapOuterStation4;
	@FXML
	TextField flapOuterStation5;
	@FXML
	TextField flapDeflection1;
	@FXML
	TextField flapDeflection2;
	@FXML
	TextField flapDeflection3;
	@FXML
	TextField flapDeflection4;
	@FXML
	TextField flapDeflection5;
	
	
	
	//slat input values
	@FXML
	List<TextField> slatChordRatioFXMLList;	
	@FXML
	List<TextField> slatExtensionRatioFXMLList;	
	@FXML
	List<TextField> slatAngleFXMLList;
	@FXML
	List<ChoiceBox> slatAngleUnitFXMLList;
	@FXML
	List<TextField> slatNonDimensionalInnerFXMLList;
	@FXML
	List<TextField> slatNonDimensionalOuterFXMLList;
	
	@FXML
	TabPane flapTabPane;
	@FXML
	TabPane slatTabPane;
	
	
	List<FlapTypeEnum> flapTypes;
	List<Amount<Angle>> flapDeflection;
	List<Double> flapChordRatio;
	List<Double> flapInnerStation;
	List<Double> flapOuterStation;
	
	Main main;
	
	ObservableList<String> flapFamily = FXCollections.observableArrayList(
			"SINGLE_SLOTTED",
			"DOUBLE_SLOTTED",
			"PLAIN",
			"FOWLER",
			"OPTMIZED_FOWLER",
			"TRIPLE_SLOTTED"
			);
	ObservableList<String> flapUnit = FXCollections.observableArrayList("°","rad");
	
	
	@FXML
	public void initializeTabAnalyses(){
		// Define lists
		int j=0;
		if(theInputTree.getNumberOfFlaps() >= 1) {
		flapTypeFXMLList.add(j,flapType1);
		flapAngleUnitFXMLList.add(j,flapUnit1);
		j++;
		}
		if(theInputTree.getNumberOfFlaps() >= 2) {
		flapTypeFXMLList.add(j,flapType2);
		flapAngleUnitFXMLList.add(j,flapUnit2);
		j++;
		}
		if(theInputTree.getNumberOfFlaps() >= 3) {
		flapTypeFXMLList.add(j,flapType3);
		flapAngleUnitFXMLList.add(j,flapUnit3);
		j++;
		}
		if(theInputTree.getNumberOfFlaps() >= 4) {
		flapTypeFXMLList.add(j,flapType4);
		flapAngleUnitFXMLList.add(j,flapUnit4);
		j++;
		}
		if(theInputTree.getNumberOfFlaps() >= 5) {
		flapTypeFXMLList.add(j,flapType5);
		flapAngleUnitFXMLList.add(j,flapUnit5);
		j++;
		}
		
		//-----------------------------------------
		int numberOfFlap = theInputTree.getNumberOfFlaps();
		int maxNumberOfFlap = 5;
		int numberOfSlat = theInputTree.getNumberOfSlats();
		int maximumNumberOfSlat = 6;
		
		for(int i=numberOfFlap; i<maxNumberOfFlap; i++) {
			flapTabPane.getTabs().get(i).setDisable(true);
		}
		
		for(int i=numberOfSlat; i<maximumNumberOfSlat; i++) {
			slatTabPane.getTabs().get(i).setDisable(true);
		}
		for(int i=0; i<numberOfFlap; i++) {
		flapTypeFXMLList.get(i).setItems(flapFamily);
		flapAngleUnitFXMLList.get(i).setItems(flapUnit);
		}
	}
	@FXML
	private void confirmHighLiftData() throws IOException{
		// fill flap values
		for(int i=0; i<theInputTree.getNumberOfFlaps(); i++) {
			//flap type
			if(flapTypeFXMLList.get(i).getValue().toString() == "SINGLE_SLOTTED")
			theInputTree.getFlapTypes().add(FlapTypeEnum.SINGLE_SLOTTED);
			if(flapTypeFXMLList.get(i).getValue().toString() == "DOUBLE_SLOTTED")
			theInputTree.getFlapTypes().add(FlapTypeEnum.DOUBLE_SLOTTED);
			if(flapTypeFXMLList.get(i).getValue().toString() == "PLAIN")
			theInputTree.getFlapTypes().add(FlapTypeEnum.PLAIN);
			if(flapTypeFXMLList.get(i).getValue().toString() == "FOWLER")
			theInputTree.getFlapTypes().add(FlapTypeEnum.FOWLER);
			if(flapTypeFXMLList.get(i).getValue().toString() == "OPTMIZED_FOWLER")
			theInputTree.getFlapTypes().add(FlapTypeEnum.OPTIMIZED_FOWLER);
			if(flapTypeFXMLList.get(i).getValue().toString() == "TRIPLE_SLOTTED")
			theInputTree.getFlapTypes().add(FlapTypeEnum.TRIPLE_SLOTTED);

			Unit unit = main.recognizeUnit(flapAngleUnitFXMLList.get(i));
			theInputTree.getFlapDeflection().add(Amount.valueOf(
					Double.parseDouble(
							flapAngleFXMLList.get(i).getText()), 
					unit)
					);
			
			theInputTree.getFlapChordRatio().add(
					Double.parseDouble(
							flapChordRatioFXMLList.get(i).getText())
					);
			
			theInputTree.getFlapInnerStation().add(
					Double.parseDouble(
							flapNonDimensionalInnerFXMLList.get(i).getText())
					);
			
			theInputTree.getFlapOuterStation().add(
					Double.parseDouble(
							flapNonDimensionalOuterFXMLList.get(i).getText())
					);
		}
		//fill slat values
		for(int i=0; i<theInputTree.getNumberOfSlats(); i++) {
			
			Unit unit = main.recognizeUnit(slatAngleUnitFXMLList.get(i));
			theInputTree.getSlatDeflection().add(Amount.valueOf(
					Double.parseDouble(
							slatAngleFXMLList.get(i).getText()), 
					unit)
					);
			
			theInputTree.getSlatChordRatio().add(
					Double.parseDouble(
							slatChordRatioFXMLList.get(i).getText())
					);
			
			theInputTree.getSlatInnerStation().add(
					Double.parseDouble(
							slatNonDimensionalInnerFXMLList.get(i).getText())
					);
			
			theInputTree.getSlatOuterStation().add(
					Double.parseDouble(
							slatNonDimensionalOuterFXMLList.get(i).getText())
					);
		}
		
	}
	
	public InputOutputTree getTheInputTree() {
		return theInputTree;
	}
	public void setTheInputTree(InputOutputTree theInputTree) {
		this.theInputTree = theInputTree;
	}
	public VariablesInputData getTheVariableInputClass() {
		return theVariableInputClass;
	}
	public void setTheVariableInputClass(VariablesInputData theVariableInputClass) {
		this.theVariableInputClass = theVariableInputClass;
	}
}
