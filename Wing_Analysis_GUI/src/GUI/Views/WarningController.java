package GUI.Views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import Calculator.InputOutputTree;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

public class WarningController {
	VariablesInputData theVariablesInputClass;
	
	//WARNING MISMATCH windows
		@FXML
		RadioButton calculatedSurface;
		
		@FXML
		RadioButton newChordsInput;
		
		@FXML
		RadioButton setNewValuesChord;
		
		@FXML
		RadioButton useCalculatedValues;
		
		@FXML
		TextField chord1;
		@FXML
		TextField chord2;
		@FXML
		TextField chord3;
		@FXML
		TextField chord4;
		@FXML
		TextField chord5;
		
		@FXML
		ChoiceBox chordsUnitsWarning;
		
		@FXML
		Button confirmData;
		
		ObservableList<String> chordsUnitsList = FXCollections.observableArrayList("m","ft" );
		
		@FXML
		private void initialize(){
			chordsUnitsWarning.setValue("m");
			chordsUnitsWarning.setItems(chordsUnitsList);
		}

		@FXML
		private void setCalculatedSurface(){
			setNewValuesChord.setDisable(true);
			chord1.setDisable(true);
			chord2.setDisable(true);
			chord3.setDisable(true);
			chord4.setDisable(true);
			chord5.setDisable(true);
			chordsUnitsWarning.setDisable(true);
			useCalculatedValues.setDisable(true);
			confirmData.setDisable(false);
		}
		
		@FXML
		private void setNewChordValues(){
			setNewValuesChord.setDisable(false);
			chord1.setDisable(false);
			chord2.setDisable(false);
			chord3.setDisable(false);
			chord4.setDisable(false);
			chord5.setDisable(false);
			chordsUnitsWarning.setDisable(false);
			useCalculatedValues.setDisable(false);
			
		}
		
		@FXML
		private void chordsNewValues(){
			
			int numSection = (int)Double.parseDouble(theVariablesInputClass.getNumberOfGivenSections().getValue().toString());
			
			if(numSection==2){
			chord1.setDisable(false);
			chord2.setDisable(false);
			chord3.setDisable(true);
			chord4.setDisable(true);
			chord5.setDisable(true);
			}
			if(numSection == 3){
			chord3.setDisable(false);
			chord4.setDisable(true);
			chord5.setDisable(true);
			}
			if(numSection == 4){
			chord3.setDisable(false);
			chord4.setDisable(false);
			chord5.setDisable(true);
			}
			if(numSection == 5){
			chord3.setDisable(false);
			chord4.setDisable(false);
			chord5.setDisable(false);
			}
			
			chordsUnitsWarning.setDisable(false);
		}
		
		@FXML
		private void chordsCalculatedValue(){
			chord1.setDisable(true);
			chord2.setDisable(true);
			chord3.setDisable(true);
			chord4.setDisable(true);
			chord5.setDisable(true);
			chordsUnitsWarning.setDisable(true);
		}
		
		@FXML
		private void confirmData() throws IOException{
			if (calculatedSurface.isSelected()){
				Amount<Area> newSurface = calculateSurfaceWithARAndChordsConstant();
			theVariablesInputClass.getSurface().clear();
			theVariablesInputClass.getSurface().appendText(String.valueOf(newSurface.doubleValue(SI.SQUARE_METRE)));
			theVariablesInputClass.getTheInputTree().setSurface(theVariablesInputClass.getCalculatedArea());
			theVariablesInputClass.getMain().getNewStageWindowsWarning().close();

			}
		}

		public Amount<Area> calculateSurfaceWithARAndChordsConstant(){
			Amount<Area> surface = Amount.valueOf(0.0, SI.SQUARE_METRE);
			
			Amount<Area> surfaceNew = Amount.valueOf(0.0, SI.SQUARE_METRE);
			Amount<Area> surfaceOld = Amount.valueOf(0.0, SI.SQUARE_METRE);
			Amount<Length> spanTemp = Amount.valueOf(0.0, SI.METER);
			
			surfaceNew = theVariablesInputClass.getCalculatedArea();
			
			while( Math.abs(surfaceNew.doubleValue(SI.SQUARE_METRE) - surfaceOld.doubleValue(SI.SQUARE_METRE)) > 1){
				
				spanTemp = Amount.valueOf(
						Math.sqrt(Double.parseDouble(theVariablesInputClass.getAspectRatio().getText())*surfaceNew.doubleValue(SI.SQUARE_METRE)), 
						SI.METER);
				
				surfaceOld = surfaceNew;
				
				List<Amount<Area>> panelsArea = new ArrayList<>();
				Amount<Area> semiArea = Amount.valueOf(0., SI.SQUARE_METRE);
				
				for(int i=0; i<(int)(Double.parseDouble(theVariablesInputClass.getNumberOfGivenSections().getValue().toString()))-1; i++){
					
					panelsArea.add(
							Amount.valueOf(
									((theVariablesInputClass.getChordListTemp().get(i).doubleValue(SI.METER) + theVariablesInputClass.getChordListTemp().get(i+1).doubleValue(SI.METER)) *
									((theVariablesInputClass.getStationListTemp().get(i+1)-theVariablesInputClass.getStationListTemp().get(i)) * spanTemp.divide(2).doubleValue(SI.METER))/2)
											,
									SI.SQUARE_METRE)
							);
					
					semiArea = semiArea.plus(panelsArea.get(i));
				}
				
				surfaceNew = semiArea.times(2);
				
			}
			
			surface = surfaceNew; 
			
			return surface;
					
		}
		public VariablesInputData getTheVariablesInputClass() {
			return theVariablesInputClass;
		}

		public void setTheVariablesInputClass(VariablesInputData theVariablesInputClass) {
			this.theVariablesInputClass = theVariablesInputClass;
		}


}
