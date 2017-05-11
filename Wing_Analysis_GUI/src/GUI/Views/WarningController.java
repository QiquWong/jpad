package GUI.Views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import Calculator.InputOutputTree;
import GUI.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
	CheckBox surface;

	@FXML
	CheckBox aspectRatio;

	@FXML
	CheckBox chords;

	@FXML
	RadioButton aspectRatioChoice;

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
	
	@FXML
	RadioButton sameSweep;
	
	@FXML
	RadioButton sameXLE;
	

	ObservableList<String> chordsUnitsList = FXCollections.observableArrayList("m","ft" );

	int numberOfSections;
	List<Double> newDimensionalStationMeters = new ArrayList<>();
	List<Double> oldDimensionalStationMeters = new ArrayList<>();
	List<Double> dimensionalXLEMeters = new ArrayList<>();
	Double oldSemiSpanMeter;
			
	@FXML
	private void initialize(){
		chordsUnitsWarning.setValue("m");
		chordsUnitsWarning.setItems(chordsUnitsList);
	}
	
	public void initialization(){
		numberOfSections = (int) Double.parseDouble(theVariablesInputClass.getNumberOfGivenSections().getValue().toString());
	
		// DIMENSIONAL STATIONS IN METER
		
		oldSemiSpanMeter = Math.sqrt(
				((theVariablesInputClass.getTheInputTree().getSurface().doubleValue(SI.SQUARE_METRE))*theVariablesInputClass.getTheInputTree().getAspectRatio())
				)/2;
		Double semispanMeter = Math.sqrt(
				(Double.parseDouble(theVariablesInputClass.getSurface().getText().toString())*Double.parseDouble(theVariablesInputClass.getAspectRatio().getText().toString()))
				)/2;
		

			newDimensionalStationMeters.add(0,
					Double.parseDouble(theVariablesInputClass.getAdimensionalStations1().getText().toString())*semispanMeter
					);
			
			newDimensionalStationMeters.add(1,
					Double.parseDouble(theVariablesInputClass.getAdimensionalStations2().getText().toString())*semispanMeter
					);
		
			if(numberOfSections == 3){
				newDimensionalStationMeters.add(2,
						Double.parseDouble(theVariablesInputClass.getAdimensionalStations3().getText().toString())*semispanMeter
						);
			}
			
			if(numberOfSections == 4){
				newDimensionalStationMeters.add(3,
						Double.parseDouble(theVariablesInputClass.getAdimensionalStations4().getText().toString())*semispanMeter
						);
			}
			
			if(numberOfSections == 5){
				newDimensionalStationMeters.add(4,
						Double.parseDouble(theVariablesInputClass.getAdimensionalStations5().getText().toString())*semispanMeter
						);
			}
			
			
			//old dimensional
			
			oldDimensionalStationMeters.add(0,
					Double.parseDouble(theVariablesInputClass.getAdimensionalStations1().getText().toString())*oldSemiSpanMeter
					);
			
			oldDimensionalStationMeters.add(1,
					Double.parseDouble(theVariablesInputClass.getAdimensionalStations2().getText().toString())*oldSemiSpanMeter
					);
		
			if(numberOfSections == 3){
				oldDimensionalStationMeters.add(2,
						Double.parseDouble(theVariablesInputClass.getAdimensionalStations3().getText().toString())*oldSemiSpanMeter
						);
			}
			
			if(numberOfSections == 4){
				oldDimensionalStationMeters.add(3,
						Double.parseDouble(theVariablesInputClass.getAdimensionalStations4().getText().toString())*oldSemiSpanMeter
						);
			}
			
			if(numberOfSections == 5){
				oldDimensionalStationMeters.add(4,
						Double.parseDouble(theVariablesInputClass.getAdimensionalStations5().getText().toString())*oldSemiSpanMeter
						);
			}
			
			// DIMENSIONAL xle in meter
			

				dimensionalXLEMeters.add(0,
						Double.parseDouble(theVariablesInputClass.getXle1().getText().toString())
						);
				
				dimensionalXLEMeters.add(1,
						Double.parseDouble(theVariablesInputClass.getXle2().getText().toString())
						);
			
				if(numberOfSections == 3){
					dimensionalXLEMeters.add(2,
							Double.parseDouble(theVariablesInputClass.getXle3().getText().toString())
							);
				}
				
				if(numberOfSections == 4){
					dimensionalXLEMeters.add(3,
							Double.parseDouble(theVariablesInputClass.getXle4().getText().toString())
							);
				}
				
				if(numberOfSections == 5){
					dimensionalXLEMeters.add(4,
							Double.parseDouble(theVariablesInputClass.getXle5().getText().toString())
							);
				}
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
		if(sameSweep.isSelected() || sameXLE.isSelected()){
		confirmData.setDisable(false);
		}
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
	private void setNewAspectRatio(){
		setNewValuesChord.setDisable(true);
		chord1.setDisable(true);
		chord2.setDisable(true);
		chord3.setDisable(true);
		chord4.setDisable(true);
		chord5.setDisable(true);
		chordsUnitsWarning.setDisable(true);
		useCalculatedValues.setDisable(true);
		if(sameSweep.isSelected() || sameXLE.isSelected()){
		confirmData.setDisable(false);
		}
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
		if(sameSweep.isSelected() || sameXLE.isSelected()){
		confirmData.setDisable(false);
		}
	}

	@FXML
	private void chordsCalculatedValue(){
		chord1.setDisable(true);
		chord2.setDisable(true);
		chord3.setDisable(true);
		chord4.setDisable(true);
		chord5.setDisable(true);
		chordsUnitsWarning.setDisable(true);
		if(sameSweep.isSelected() || sameXLE.isSelected()){
		confirmData.setDisable(false);
		}
	}

	@FXML
	private void enableOptions() throws IOException{
		if(surface.isSelected() & aspectRatio.isSelected() & !chords.isSelected()){
			newChordsInput.setDisable(false);
			aspectRatioChoice.setDisable(true);
			calculatedSurface.setDisable(true);
			chord1.setDisable(true);
			chord2.setDisable(true);
			chord3.setDisable(true);
			chord4.setDisable(true);
			chord5.setDisable(true);
			chordsUnitsWarning.setDisable(true);
			setNewValuesChord.setDisable(true);
			useCalculatedValues.setDisable(true);
		}

		if(surface.isSelected() & chords.isSelected() & !aspectRatio.isSelected()){
			aspectRatioChoice.setDisable(false);
			newChordsInput.setDisable(true);
			calculatedSurface.setDisable(true);
			chord1.setDisable(true);
			chord2.setDisable(true);
			chord3.setDisable(true);
			chord4.setDisable(true);
			chord5.setDisable(true);
			chordsUnitsWarning.setDisable(true);
			setNewValuesChord.setDisable(true);
			useCalculatedValues.setDisable(true);
		}

		if(chords.isSelected() & aspectRatio.isSelected() & !surface.isSelected()){
			calculatedSurface.setDisable(false);
			newChordsInput.setDisable(true);
			aspectRatioChoice.setDisable(true);
			chord1.setDisable(true);
			chord2.setDisable(true);
			chord3.setDisable(true);
			chord4.setDisable(true);
			chord5.setDisable(true);
			chordsUnitsWarning.setDisable(true);
			setNewValuesChord.setDisable(true);
			useCalculatedValues.setDisable(true);
		}
		if(chords.isSelected() & aspectRatio.isSelected() & surface.isSelected()){
			calculatedSurface.setDisable(true);
			aspectRatioChoice.setDisable(true);
			newChordsInput.setDisable(true);
			aspectRatioChoice.setDisable(true);
			chord1.setDisable(true);
			chord2.setDisable(true);
			chord3.setDisable(true);
			chord4.setDisable(true);
			chord5.setDisable(true);
			chordsUnitsWarning.setDisable(true);
			setNewValuesChord.setDisable(true);
			useCalculatedValues.setDisable(true);
		}

	}
	
	@FXML
	private void Sweep(){
		if(aspectRatioChoice.isSelected() || calculatedSurface.isSelected() || setNewValuesChord.isSelected() || useCalculatedValues.isSelected()){
			confirmData.setDisable(false);
		}
	}


	// CONFIRM DATA---------
	@FXML
	private void confirmData() throws IOException{
		if (calculatedSurface.isSelected()){
			Amount<Area> newSurface = calculateSurfaceWithARAndChordsConstant();
			theVariablesInputClass.getSurface().clear();
			theVariablesInputClass.getSurface().appendText(String.valueOf(newSurface.doubleValue(SI.SQUARE_METRE)));
			theVariablesInputClass.getTheInputTree().setSurface(theVariablesInputClass.getCalculatedArea());
	

		}
		if (aspectRatioChoice.isSelected()){
			Amount<Length> newSpan = calculateSpanWithSurfaceAndChord();
			Double aspectRatio = (Math.pow(newSpan.doubleValue(SI.METER), 2))/(Double.parseDouble(theVariablesInputClass.getSurface().getText()));
			
			theVariablesInputClass.getAspectRatio().clear();
			theVariablesInputClass.getAspectRatio().appendText(String.valueOf(aspectRatio));
			theVariablesInputClass.getTheInputTree().setAspectRatio(aspectRatio);
	

		}
		
		if (setNewValuesChord.isSelected()){

			int numSection = (int)Double.parseDouble(theVariablesInputClass.getNumberOfGivenSections().getValue().toString());

			if(numSection==2){
			theVariablesInputClass.getChords1().clear();
			theVariablesInputClass.getChords1().appendText(chord1.getText());
			theVariablesInputClass.getChords2().clear();
			theVariablesInputClass.getChords2().appendText(chord2.getText());
			}
		
			if(numSection==3){
			theVariablesInputClass.getChords1().clear();
			theVariablesInputClass.getChords1().appendText(chord1.getText());
			theVariablesInputClass.getChords2().clear();
			theVariablesInputClass.getChords2().appendText(chord2.getText());
			theVariablesInputClass.getChords3().clear();
			theVariablesInputClass.getChords3().appendText(chord3.getText());
			}
			
			if(numSection==4){
			theVariablesInputClass.getChords1().clear();
			theVariablesInputClass.getChords1().appendText(chord1.getText());
			theVariablesInputClass.getChords2().clear();
			theVariablesInputClass.getChords2().appendText(chord2.getText());
			theVariablesInputClass.getChords3().clear();
			theVariablesInputClass.getChords3().appendText(chord3.getText());
			theVariablesInputClass.getChords4().clear();
			theVariablesInputClass.getChords4().appendText(chord4.getText());
			}
			
			if(numSection==5){
			theVariablesInputClass.getChords1().clear();
			theVariablesInputClass.getChords1().appendText(chord1.getText());
			theVariablesInputClass.getChords2().clear();
			theVariablesInputClass.getChords2().appendText(chord2.getText());
			theVariablesInputClass.getChords3().clear();
			theVariablesInputClass.getChords3().appendText(chord3.getText());
			theVariablesInputClass.getChords4().clear();
			theVariablesInputClass.getChords4().appendText(chord4.getText());
			theVariablesInputClass.getChords5().clear();
			theVariablesInputClass.getChords5().appendText(chord5.getText());
			}
	
		}
		
		if (useCalculatedValues.isSelected()){
			int numSection = (int)Double.parseDouble(theVariablesInputClass.getNumberOfGivenSections().getValue().toString());
			
			List<Amount<Length>> newChordsValues = calculateChordsFromSurfaceAndSpan();
			theVariablesInputClass.getChordsUnits().setValue("m");
			theVariablesInputClass.getChords1().clear();;
			theVariablesInputClass.getChords1().appendText(String.valueOf(newChordsValues.get(0).doubleValue(SI.METER)));
			theVariablesInputClass.getChords2().clear();;
			theVariablesInputClass.getChords2().appendText(String.valueOf(newChordsValues.get(1).doubleValue(SI.METER)));

			if(numSection==3){
				theVariablesInputClass.getChords3().clear();;
				theVariablesInputClass.getChords3().appendText(String.valueOf(newChordsValues.get(2).doubleValue(SI.METER)));
			
			
			if(numSection==4){
				theVariablesInputClass.getChords4().clear();;
				theVariablesInputClass.getChords4().appendText(String.valueOf(newChordsValues.get(3).doubleValue(SI.METER)));
				
			
			
			if(numSection==5){
				theVariablesInputClass.getChords5().clear();;
				theVariablesInputClass.getChords5().appendText(String.valueOf(newChordsValues.get(4).doubleValue(SI.METER)));
			}
			}
			}
			

		}
		
		if (sameSweep.isSelected()){
			// calculating sweep
			List<Double> sweepLEDeg = new ArrayList<>();
			for (int i=0; i<numberOfSections-1; i++){
				sweepLEDeg.add(i,
						Math.toDegrees(
						Math.atan(
								dimensionalXLEMeters.get(i+1)/oldDimensionalStationMeters.get(i+1)
								)
						));
			}
			
			
			// calculating new xle
			
			List<Double> xleMeter = new ArrayList<>();
			xleMeter.add(0, 0.0);
			for(int i=1; i<numberOfSections; i++){
				xleMeter.add(i,
						newDimensionalStationMeters.get(i)*Math.tan(Math.toRadians(sweepLEDeg.get(i-1)))
						);
			}
			
			//set new xle
			
			theVariablesInputClass.getXleUnits().setValue("m");
			
			theVariablesInputClass.getXle1().clear();
			theVariablesInputClass.getXle1().appendText(String.valueOf(xleMeter.get(0)));
			theVariablesInputClass.getXle2().clear();;
			theVariablesInputClass.getXle2().appendText(String.valueOf(xleMeter.get(1)));
			
			if (numberOfSections==3){
				
				theVariablesInputClass.getXle3().clear();;
				theVariablesInputClass.getXle3().appendText(String.valueOf(xleMeter.get(2)));
				
				if(numberOfSections == 4){
					
					theVariablesInputClass.getXle4().clear();;
					theVariablesInputClass.getXle4().appendText(String.valueOf(xleMeter.get(3)));
					
					if(numberOfSections == 5){
						
						theVariablesInputClass.getXle5().clear();;
						theVariablesInputClass.getXle5().appendText(String.valueOf(xleMeter.get(4)));
						
				}
				}
				
			}

			
			
			theVariablesInputClass.getMain().getNewStageWindowsWarning().close();
		}
		
		if(sameXLE.isSelected()){
			// the previous xle will be setted
			theVariablesInputClass.getMain().getNewStageWindowsWarning().close();
		}
	}

	// METHODS
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
	
	public Amount<Length> calculateSpanWithSurfaceAndChord(){
		
		Amount<Length> span = Amount.valueOf(0.0, SI.METER);

		double summation= 0.;
		

			for(int i=0; i<(int)(Double.parseDouble(theVariablesInputClass.getNumberOfGivenSections().getValue().toString()))-1; i++){

				summation = summation + ((theVariablesInputClass.getChordListTemp().get(i).doubleValue(SI.METER) +
						theVariablesInputClass.getChordListTemp().get(i+1).doubleValue(SI.METER))*
						theVariablesInputClass.getStationListTemp().get(i+1));
				
			}

		
			span = Amount.valueOf(
					(2* Double.parseDouble(theVariablesInputClass.getSurface().getText()))/(summation), 
					SI.METER);

		return span;

	}
	public VariablesInputData getTheVariablesInputClass() {
		return theVariablesInputClass;
	}

	public void setTheVariablesInputClass(VariablesInputData theVariablesInputClass) {
		this.theVariablesInputClass = theVariablesInputClass;
	}

	public List<Amount<Length>> calculateChordsFromSurfaceAndSpan(){
		
		List<Amount<Length>> newChordDistribution = new ArrayList<>();
		
		int numberOfSections = (int) Double.parseDouble(theVariablesInputClass.getNumberOfGivenSections().getValue().toString());

		// starting chords
		List<Amount<Length>> oldChordDistribution = new ArrayList<>();
		
		Unit unitOfMeasureChord = Main.recognizeUnit(theVariablesInputClass.getChordsUnits()); 
		
		oldChordDistribution.add(0,
				Amount.valueOf(
						 Double.parseDouble(theVariablesInputClass.getChords1().getText().toString()),
						unitOfMeasureChord)
				);
		
		oldChordDistribution.add(1,
				Amount.valueOf(
						 Double.parseDouble(theVariablesInputClass.getChords2().getText().toString()),
						unitOfMeasureChord)
				);
		if(numberOfSections == 3){
			oldChordDistribution.add(2,
					Amount.valueOf(
							 Double.parseDouble(theVariablesInputClass.getChords3().getText().toString()),
							unitOfMeasureChord)
					);
		}
		if(numberOfSections == 4){
			oldChordDistribution.add(3,
					Amount.valueOf(
							 Double.parseDouble(theVariablesInputClass.getChords4().getText().toString()),
							unitOfMeasureChord)
					);
		}
		if(numberOfSections == 5){
			oldChordDistribution.add(4,
					Amount.valueOf(
							 Double.parseDouble(theVariablesInputClass.getChords5().getText().toString()),
							unitOfMeasureChord)
					);
		}
	
		// calculating taper ratios
		List<Double> taperRatios = new ArrayList<>();
				
		for (int i=0; i<numberOfSections-1; i++){
			taperRatios.add(i, 
					(oldChordDistribution.get(i+1).doubleValue(SI.METER)/oldChordDistribution.get(i).doubleValue(SI.METER))
					);
		}
	
		
		// FIRSVALUE
			
			double denominator = 0.;
			double prod = 1.;
			
			for(int i=0; i<numberOfSections-1; i++){
				for(int ii=0; ii<i; ii++)
				prod= prod * taperRatios.get(ii);
				denominator = denominator + prod*(1+taperRatios.get(0))*newDimensionalStationMeters.get(i+1);
			}
			
			newChordDistribution.add(0, 
					Amount.valueOf(
							(Double.parseDouble(theVariablesInputClass.getSurface().getText().toString()))/
							denominator,
							SI.METER)
					);
			
			//other values
			
			for(int i=1; i<numberOfSections; i++){
				newChordDistribution.add(i, 
						Amount.valueOf(
								newChordDistribution.get(i-1).doubleValue(SI.METER)*taperRatios.get(i-1),
								SI.METER)
						);
				
			}
			
		
		return newChordDistribution;
	}
}
