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
	HighLiftInputController theHighLiftController;
	
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
	List<TextField> slatChordRatioFXMLList = new ArrayList<>();	
	@FXML
	List<TextField> slatExtensionRatioFXMLList = new ArrayList<>();	
	@FXML
	List<TextField> slatAngleFXMLList = new ArrayList<>();	
	@FXML
	List<ChoiceBox> slatAngleUnitFXMLList = new ArrayList<>();	
	@FXML
	List<TextField> slatNonDimensionalInnerFXMLList = new ArrayList<>();	
	@FXML
	List<TextField> slatNonDimensionalOuterFXMLList = new ArrayList<>();	
	
	//slat values
	@FXML
	ChoiceBox slatUnit1;
	@FXML
	ChoiceBox slatUnit2;
	@FXML
	ChoiceBox slatUnit3;
	@FXML
	ChoiceBox slatUnit4;
	@FXML
	ChoiceBox slatUnit5;
	@FXML
	ChoiceBox slatUnit6;
	@FXML
	TextField slatChordRatio1;
	@FXML
	TextField slatChordRatio2;
	@FXML
	TextField slatChordRatio3;
	@FXML
	TextField slatChordRatio4;
	@FXML
	TextField slatChordRatio5;
	@FXML
	TextField slatChordRatio6;
	@FXML
	TextField slatExtensionRatio1;
	@FXML
	TextField slatExtensionRatio2;
	@FXML
	TextField slatExtensionRatio3;
	@FXML
	TextField slatExtensionRatio4;
	@FXML
	TextField slatExtensionRatio5;
	@FXML
	TextField slatExtensionRatio6;
	@FXML
	TextField slatInnerStation1;
	@FXML
	TextField slatInnerStation2;
	@FXML
	TextField slatInnerStation3;
	@FXML
	TextField slatInnerStation4;
	@FXML
	TextField slatInnerStation5;
	@FXML
	TextField slatInnerStation6;
	@FXML
	TextField slatOuterStation1;
	@FXML
	TextField slatOuterStation2;
	@FXML
	TextField slatOuterStation3;
	@FXML
	TextField slatOuterStation4;
	@FXML
	TextField slatOuterStation5;
	@FXML
	TextField slatOuterStation6;
	@FXML
	TextField slatDeflection1;
	@FXML
	TextField slatDeflection2;
	@FXML
	TextField slatDeflection3;
	@FXML
	TextField slatDeflection4;
	@FXML
	TextField slatDeflection5;
	@FXML
	TextField slatDeflection6;
	
	@FXML
	TabPane flapTabPane;
	@FXML
	TabPane slatTabPane;
	
	
	Main main;
	
	ObservableList<String> flapFamily = FXCollections.observableArrayList(
			"SINGLE_SLOTTED",
			"DOUBLE_SLOTTED",
			"PLAIN",
			"FOWLER",
			"OPTIMIZED_FOWLER",
			"TRIPLE_SLOTTED"
			);
	ObservableList<String> flapUnit = FXCollections.observableArrayList("°","rad");
	ObservableList<String> slatUnit = FXCollections.observableArrayList("°","rad");
	
	@FXML
	public void initializeTabAnalyses(){
		// Define lists
		int j=0;
		if(theInputTree.getNumberOfFlaps() >= 1) {
		flapTypeFXMLList.add(j,flapType1);
		flapAngleUnitFXMLList.add(j,flapUnit1);
		flapAngleFXMLList.add(j, flapDeflection1);
		flapChordRatioFXMLList.add(j, flapChordRatio1);
		flapNonDimensionalInnerFXMLList.add(j, flapInnerStation1);
		flapNonDimensionalOuterFXMLList.add(j, flapOuterStation1);	
		j++;
		}
		if(theInputTree.getNumberOfFlaps() >= 2) {
		flapTypeFXMLList.add(j,flapType2);
		flapAngleUnitFXMLList.add(j,flapUnit2);
		flapAngleFXMLList.add(j, flapDeflection2);
		flapChordRatioFXMLList.add(j, flapChordRatio2);
		flapNonDimensionalInnerFXMLList.add(j, flapInnerStation2);
		flapNonDimensionalOuterFXMLList.add(j, flapOuterStation2);	
		j++;
		}
		if(theInputTree.getNumberOfFlaps() >= 3) {
		flapTypeFXMLList.add(j,flapType3);
		flapAngleUnitFXMLList.add(j,flapUnit3);	
		flapAngleFXMLList.add(j, flapDeflection3);
		flapChordRatioFXMLList.add(j, flapChordRatio3);
		flapNonDimensionalInnerFXMLList.add(j, flapInnerStation3);
		flapNonDimensionalOuterFXMLList.add(j, flapOuterStation3);	
		j++;
		}
		if(theInputTree.getNumberOfFlaps() >= 4) {
		flapTypeFXMLList.add(j,flapType4);
		flapAngleUnitFXMLList.add(j,flapUnit4);
		flapAngleFXMLList.add(j, flapDeflection4);
		flapChordRatioFXMLList.add(j, flapChordRatio4);
		flapNonDimensionalInnerFXMLList.add(j, flapInnerStation4);
		flapNonDimensionalOuterFXMLList.add(j, flapOuterStation4);	
		j++;
		}
		if(theInputTree.getNumberOfFlaps() >= 5) {
		flapTypeFXMLList.add(j,flapType5);
		flapAngleUnitFXMLList.add(j,flapUnit5);
		flapAngleFXMLList.add(j, flapDeflection5);
		flapChordRatioFXMLList.add(j, flapChordRatio5);
		flapNonDimensionalInnerFXMLList.add(j, flapInnerStation5);
		flapNonDimensionalOuterFXMLList.add(j, flapOuterStation5);	
		j++;
		}
		
		j=0;
		if(theInputTree.getNumberOfSlats() >= 1) {
			slatAngleUnitFXMLList.add(j,slatUnit1);
			slatAngleFXMLList.add(j, slatDeflection1);
			slatChordRatioFXMLList.add(j, slatChordRatio1);
			slatExtensionRatioFXMLList.add(j, slatExtensionRatio1);
			slatNonDimensionalInnerFXMLList.add(j,slatInnerStation1);
			slatNonDimensionalOuterFXMLList.add(j, slatOuterStation1);
			j++;
		}
		if(theInputTree.getNumberOfSlats() >= 2) {
			slatAngleUnitFXMLList.add(j,slatUnit2);
			slatAngleFXMLList.add(j, slatDeflection2);
			slatChordRatioFXMLList.add(j, slatChordRatio2);
			slatExtensionRatioFXMLList.add(j, slatExtensionRatio2);
			slatNonDimensionalInnerFXMLList.add(j,slatInnerStation2);
			slatNonDimensionalOuterFXMLList.add(j, slatOuterStation2);
			j++;
		}
		if(theInputTree.getNumberOfSlats() >= 3) {
			slatAngleUnitFXMLList.add(j,slatUnit3);
			slatAngleFXMLList.add(j, slatDeflection3);
			slatChordRatioFXMLList.add(j, slatChordRatio3);
			slatExtensionRatioFXMLList.add(j, slatExtensionRatio3);
			slatNonDimensionalInnerFXMLList.add(j,slatInnerStation3);
			slatNonDimensionalOuterFXMLList.add(j, slatOuterStation3);
			j++;
		}
		if(theInputTree.getNumberOfSlats() >= 4) {
			slatAngleUnitFXMLList.add(j,slatUnit4);
			slatAngleFXMLList.add(j, slatDeflection4);
			slatChordRatioFXMLList.add(j, slatChordRatio4);
			slatExtensionRatioFXMLList.add(j, slatExtensionRatio4);
			slatNonDimensionalInnerFXMLList.add(j,slatInnerStation4);
			slatNonDimensionalOuterFXMLList.add(j, slatOuterStation4);
			j++;
		}
		if(theInputTree.getNumberOfSlats() >= 5) {
			slatAngleUnitFXMLList.add(j,slatUnit5);
			slatAngleFXMLList.add(j, slatDeflection5);
			slatChordRatioFXMLList.add(j, slatChordRatio5);
			slatExtensionRatioFXMLList.add(j, slatExtensionRatio5);
			slatNonDimensionalInnerFXMLList.add(j,slatInnerStation5);
			slatNonDimensionalOuterFXMLList.add(j, slatOuterStation5);
			j++;
		}
		if(theInputTree.getNumberOfSlats() >= 6) {
			slatAngleUnitFXMLList.add(j,slatUnit6);
			slatAngleFXMLList.add(j, slatDeflection6);
			slatChordRatioFXMLList.add(j, slatChordRatio6);
			slatExtensionRatioFXMLList.add(j, slatExtensionRatio6);
			slatNonDimensionalInnerFXMLList.add(j,slatInnerStation6);
			slatNonDimensionalOuterFXMLList.add(j, slatOuterStation6);
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
		for(int i=0; i<numberOfSlat; i++) {
		slatAngleUnitFXMLList.get(i).setItems(slatUnit);
		}
	}
	@SuppressWarnings("unchecked")
	@FXML
	private void confirmHighLiftData() throws IOException{
		
		cleanFlapSlatInputValues();
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
			if(flapTypeFXMLList.get(i).getValue().toString() == "OPTIMIZED_FOWLER")
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
			
			theInputTree.getSlatExtensionRatio().add(
					Double.parseDouble(
							slatExtensionRatioFXMLList.get(i).getText())
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
		// set high lift tree is filled
		theInputTree.setHighLiftInputTreeIsFilled(true);
		
		// close windows
		
		theVariableInputClass.getLeftPane().getChildren().remove(theVariableInputClass.getLeftPane().getChildren().size()-1);
		theHighLiftController.getBottomPane().getChildren().remove(theHighLiftController.getBottomPane().getChildren().size()-1);
	}
	
	private void cleanFlapSlatInputValues() {
		theInputTree.getFlapTypes().clear();
		theInputTree.getFlapChordRatio().clear();
		theInputTree.getFlapInnerStation().clear();
		theInputTree.getFlapOuterStation().clear();
		theInputTree.getFlapTypes().clear();
		
		theInputTree.getSlatChordRatio().clear();
		theInputTree.getSlatDeflection().clear();
		theInputTree.getSlatExtensionRatio().clear();
		theInputTree.getSlatInnerStation().clear();
		theInputTree.getSlatOuterStation().clear();
	}
	
	public void writeFlapData() {
		int j=0;
		if(theInputTree.getNumberOfFlaps() >= 1) {
			flapType1.setValue(theInputTree.getFlapTypes().get(j).toString());
			flapUnit1.setValue(theInputTree.getFlapDeflection().get(j).getUnit().toString());
			flapDeflection1.setText(Double.toString(theInputTree.getFlapDeflection().get(j).doubleValue(theInputTree.getFlapDeflection().get(j).getUnit())));
			flapChordRatio1.setText(Double.toString(theInputTree.getFlapChordRatio().get(j)));
			flapInnerStation1.setText(Double.toString(theInputTree.getFlapInnerStation().get(j)));
			flapOuterStation1.setText(Double.toString(theInputTree.getFlapOuterStation().get(j)));
			j++;
		}
		if(theInputTree.getNumberOfFlaps() >= 2) {
			flapType2.setValue(theInputTree.getFlapTypes().get(j).toString());
			flapUnit2.setValue(theInputTree.getFlapDeflection().get(j).getUnit().toString());
			flapDeflection2.setText(Double.toString(theInputTree.getFlapDeflection().get(j).doubleValue(theInputTree.getFlapDeflection().get(j).getUnit())));
			flapChordRatio2.setText(Double.toString(theInputTree.getFlapChordRatio().get(j)));
			flapInnerStation2.setText(Double.toString(theInputTree.getFlapInnerStation().get(j)));
			flapOuterStation2.setText(Double.toString(theInputTree.getFlapOuterStation().get(j)));
			j++;
		}
		if(theInputTree.getNumberOfFlaps() >= 3) {
			flapType3.setValue(theInputTree.getFlapTypes().get(j).toString());
			flapUnit3.setValue(theInputTree.getFlapDeflection().get(j).getUnit().toString());
			flapDeflection3.setText(Double.toString(theInputTree.getFlapDeflection().get(j).doubleValue(theInputTree.getFlapDeflection().get(j).getUnit())));
			flapChordRatio3.setText(Double.toString(theInputTree.getFlapChordRatio().get(j)));
			flapInnerStation3.setText(Double.toString(theInputTree.getFlapInnerStation().get(j)));
			flapOuterStation3.setText(Double.toString(theInputTree.getFlapOuterStation().get(j)));
			j++;
		}
		if(theInputTree.getNumberOfFlaps() >= 4) {
			flapType4.setValue(theInputTree.getFlapTypes().get(j).toString());
			flapUnit4.setValue(theInputTree.getFlapDeflection().get(j).getUnit().toString());
			flapDeflection4.setText(Double.toString(theInputTree.getFlapDeflection().get(j).doubleValue(theInputTree.getFlapDeflection().get(j).getUnit())));
			flapChordRatio4.setText(Double.toString(theInputTree.getFlapChordRatio().get(j)));
			flapInnerStation4.setText(Double.toString(theInputTree.getFlapInnerStation().get(j)));
			flapOuterStation4.setText(Double.toString(theInputTree.getFlapOuterStation().get(j)));
			j++;
		}
		if(theInputTree.getNumberOfFlaps() >= 5) {
			flapType5.setValue(theInputTree.getFlapTypes().get(j).toString());
			flapUnit5.setValue(theInputTree.getFlapDeflection().get(j).getUnit().toString());
			flapDeflection5.setText(Double.toString(theInputTree.getFlapDeflection().get(j).doubleValue(theInputTree.getFlapDeflection().get(j).getUnit())));
			flapChordRatio5.setText(Double.toString(theInputTree.getFlapChordRatio().get(j)));
			flapInnerStation5.setText(Double.toString(theInputTree.getFlapInnerStation().get(j)));
			flapOuterStation5.setText(Double.toString(theInputTree.getFlapOuterStation().get(j)));
			j++;
		}
		j=0;
		if(theInputTree.getNumberOfSlats() >= 1) {
			slatUnit1.setValue(theInputTree.getSlatDeflection().get(j).getUnit().toString());
			slatDeflection1.setText(Double.toString(theInputTree.getSlatDeflection().get(j).doubleValue(theInputTree.getSlatDeflection().get(j).getUnit())));
			slatChordRatio1.setText(Double.toString(theInputTree.getSlatChordRatio().get(j)));
			slatExtensionRatio1.setText(Double.toString(theInputTree.getSlatChordRatio().get(j)));
			slatInnerStation1.setText(Double.toString(theInputTree.getSlatInnerStation().get(j)));
			slatOuterStation1.setText(Double.toString(theInputTree.getSlatOuterStation().get(j)));
			j++;
		}
		if(theInputTree.getNumberOfSlats() >= 2) {
			slatUnit2.setValue(theInputTree.getSlatDeflection().get(j).getUnit().toString());
			slatDeflection2.setText(Double.toString(theInputTree.getSlatDeflection().get(j).doubleValue(theInputTree.getSlatDeflection().get(j).getUnit())));
			slatChordRatio2.setText(Double.toString(theInputTree.getSlatChordRatio().get(j)));
			slatExtensionRatio2.setText(Double.toString(theInputTree.getSlatChordRatio().get(j)));
			slatInnerStation2.setText(Double.toString(theInputTree.getSlatInnerStation().get(j)));
			slatOuterStation2.setText(Double.toString(theInputTree.getSlatOuterStation().get(j)));
			j++;
		}
		if(theInputTree.getNumberOfSlats() >= 3) {
			slatUnit3.setValue(theInputTree.getSlatDeflection().get(j).getUnit().toString());
			slatDeflection3.setText(Double.toString(theInputTree.getSlatDeflection().get(j).doubleValue(theInputTree.getSlatDeflection().get(j).getUnit())));
			slatChordRatio3.setText(Double.toString(theInputTree.getSlatChordRatio().get(j)));
			slatExtensionRatio3.setText(Double.toString(theInputTree.getSlatChordRatio().get(j)));
			slatInnerStation3.setText(Double.toString(theInputTree.getSlatInnerStation().get(j)));
			slatOuterStation3.setText(Double.toString(theInputTree.getSlatOuterStation().get(j)));
			j++;
		}
		if(theInputTree.getNumberOfSlats() >= 4) {
			slatUnit4.setValue(theInputTree.getSlatDeflection().get(j).getUnit().toString());
			slatDeflection4.setText(Double.toString(theInputTree.getSlatDeflection().get(j).doubleValue(theInputTree.getSlatDeflection().get(j).getUnit())));
			slatChordRatio4.setText(Double.toString(theInputTree.getSlatChordRatio().get(j)));
			slatExtensionRatio4.setText(Double.toString(theInputTree.getSlatChordRatio().get(j)));
			slatInnerStation4.setText(Double.toString(theInputTree.getSlatInnerStation().get(j)));
			slatOuterStation4.setText(Double.toString(theInputTree.getSlatOuterStation().get(j)));
			j++;
		}
		if(theInputTree.getNumberOfSlats() >= 5) {
			slatUnit5.setValue(theInputTree.getSlatDeflection().get(j).getUnit().toString());
			slatDeflection5.setText(Double.toString(theInputTree.getSlatDeflection().get(j).doubleValue(theInputTree.getSlatDeflection().get(j).getUnit())));
			slatChordRatio5.setText(Double.toString(theInputTree.getSlatChordRatio().get(j)));
			slatExtensionRatio5.setText(Double.toString(theInputTree.getSlatChordRatio().get(j)));
			slatInnerStation5.setText(Double.toString(theInputTree.getSlatInnerStation().get(j)));
			slatOuterStation5.setText(Double.toString(theInputTree.getSlatOuterStation().get(j)));
			j++;
		}
		if(theInputTree.getNumberOfSlats() >= 6) {
			slatUnit6.setValue(theInputTree.getSlatDeflection().get(j).getUnit().toString());
			slatDeflection6.setText(Double.toString(theInputTree.getSlatDeflection().get(j).doubleValue(theInputTree.getSlatDeflection().get(j).getUnit())));
			slatChordRatio6.setText(Double.toString(theInputTree.getSlatChordRatio().get(j)));
			slatExtensionRatio6.setText(Double.toString(theInputTree.getSlatChordRatio().get(j)));
			slatInnerStation6.setText(Double.toString(theInputTree.getSlatInnerStation().get(j)));
			slatOuterStation6.setText(Double.toString(theInputTree.getSlatOuterStation().get(j)));
			j++;
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
	public HighLiftInputController getTheHighLiftController() {
		return theHighLiftController;
	}
	public void setTheHighLiftController(HighLiftInputController theHighLiftController) {
		this.theHighLiftController = theHighLiftController;
	}
	public ChoiceBox getFlapType1() {
		return flapType1;
	}
	public ChoiceBox getFlapType2() {
		return flapType2;
	}
	public ChoiceBox getFlapType3() {
		return flapType3;
	}
	public ChoiceBox getFlapType4() {
		return flapType4;
	}
	public ChoiceBox getFlapType5() {
		return flapType5;
	}
	public void setFlapType1(ChoiceBox flapType1) {
		this.flapType1 = flapType1;
	}
	public void setFlapType2(ChoiceBox flapType2) {
		this.flapType2 = flapType2;
	}
	public void setFlapType3(ChoiceBox flapType3) {
		this.flapType3 = flapType3;
	}
	public void setFlapType4(ChoiceBox flapType4) {
		this.flapType4 = flapType4;
	}
	public void setFlapType5(ChoiceBox flapType5) {
		this.flapType5 = flapType5;
	}
}
