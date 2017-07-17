package GUI.Views;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;

import Calculator.InputOutputTree;
import Calculator.Reader;
import GUI.Main;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import ncsa.hdf.view.NewAttributeDialog;
import standaloneutils.JPADXmlReader;

public class Controllers {

	private Main main;
	private Main theMainClass = new Main();
	VaraiblesAnalyses theVariablesAnalysisClass;
	File inputFile;
	InputOutputTree theInputTree;
	
	@FXML
	RadioButton writeArrays;
	@FXML
	RadioButton fromInputFile;
	@FXML
	Label xArray;
	@FXML
	Label yArray;
	@FXML
	TextField xArrayText;
	@FXML
	TextField yArrayText;
	@FXML
	TextField pathInputFile;
	@FXML
	Button searchButton;
	@FXML
	Button loadButton;

	@FXML
	Button saveButton;
	
	@FXML
	private void reStartNewAnalysis() throws IOException{
		main.reStartNewAnalysis();
	}
	
	@FXML
	private void goHome() throws IOException{
		main.showCenterItem();
		main.getNewStageWindows().close();
	} 
	
	@FXML
	private void remainHere(){
		main.getNewStageWindows().close();
	}
	
	@FXML
	private void saveAndExit() throws IOException {
		main.saveAndExit();
		goHome();
	}
	
	@FXML
	private void startNewAnalysis() throws IOException{
		main.startNewAnalysis();

	}
	
	@FXML
	private void infoButton() throws IOException{
		main.showInfo();
	}
	
	@FXML
	private void saveOutput() throws IOException{
		main.saveOutput();
	}
	 
	@FXML
	private void inputArrayFromData() throws IOException{
	xArray.setDisable(false);
	yArray.setDisable(false);
	xArrayText.setDisable(false);
	yArrayText.setDisable(false);
	
	pathInputFile.setDisable(true);
	searchButton.setDisable(true);
	
	loadButton.setDisable(false);
	}
	
	@FXML
	private void inputArrayFromFile() throws IOException{
	xArray.setDisable(true);
	yArray.setDisable(true);
	xArrayText.setDisable(true);
	yArrayText.setDisable(true);
	
	pathInputFile.setDisable(false);
	searchButton.setDisable(false);
	
	loadButton.setDisable(false);
	}
	
	@FXML
	private void loadNewCurve() throws IOException{
		if(writeArrays.isSelected()){
			if(!xArrayText.getText().trim().isEmpty() || !yArrayText.getText().trim().isEmpty() ){
				theVariablesAnalysisClass.getExternalLiftDistributionCurves().add(recognizeTextFieldAsArray(yArrayText));
				theVariablesAnalysisClass.getxArrayExternalLiftCoefficient().add(recognizeTextFieldAsArray(xArrayText));
			}
		}
		
		if(fromInputFile.isSelected()){
//CONTINUE HERE			
		}
		
		
	}
	
	@FXML
	private void searchInputFile() throws IOException{

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose an XML input file");
		chooser.setInitialDirectory(new File(MyConfiguration.getDir(FoldersEnum.INPUT_DIR)));
		chooser.getExtensionFilters().addAll(new ExtensionFilter("XML File","*.xml"));
		File file = chooser.showOpenDialog(null);
		if (file != null) {
			pathInputFile.setText(file.getAbsolutePath());
			inputFile = file;

			// CHECK IF THE TEXT FIELD IS NOT EMPTY ...
			loadButton.disableProperty().bind(
					Bindings.isEmpty(pathInputFile.textProperty())
					);

			// CHECK IF THE FILE IN TEXTFIELD IS A VALID FILE ...
			final Tooltip warning = new Tooltip("WARNING : The selected file is not a valid input !!");
			loadButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					Point2D p = loadButton
							.localToScreen(
									-2.5*loadButton.getLayoutBounds().getMaxX(),
									1.2*loadButton.getLayoutBounds().getMaxY()
									);
					if(!isInputFile(pathInputFile.getText())
							) {
						warning.show(loadButton, p.getX(), p.getY());
					}
				}
			});
			loadButton.setOnMouseExited(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					warning.hide();
				}
			});

		}
	}
	
	private boolean isInputFile(String pathToAircraftXML) {

		boolean isInputFile = false;

		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
		System.setOut(filterStream);

		String pathToXML = pathInputFile.getText();
		if(pathToAircraftXML.endsWith(".xml")) {
			File inputFile = new File(pathToAircraftXML);
			theInputTree.setInputFile(inputFile);
			if(inputFile.exists()) {
				JPADXmlReader reader = new JPADXmlReader(pathToXML);
				isInputFile = true;
			}
		}
		// write again
		System.setOut(originalOut);

		return isInputFile;
	}
	
	@FXML
	private void inputFromFile() throws IOException{
		main.saveOutput();
	}

	private List<Double> recognizeTextFieldAsArray(TextField textField){
		List<Double> outputList = new ArrayList<>();
		
		String textString = textField.toString();
		textString.trim();
		List<String> listString = JPADXmlReader.readArrayFromXML(textString);
		for(int i=0; i<listString.size(); i++)
			outputList.add(Double.valueOf(listString.get(i)));
		return outputList;
	}
	
	@FXML
	private void yes() throws IOException{
		main.exitFromHighLift();
	}

	
	public Main getTheMainClass() {
		return theMainClass;
	}
	public void setTheMainClass(Main theMainClass) {
		this.theMainClass = theMainClass;
	}

	public Button getSaveButton() {
		return saveButton;
	}

	public void setSaveButton(Button saveButton) {
		this.saveButton = saveButton;
	}

	public VaraiblesAnalyses getTheVariablesAnalysisClass() {
		return theVariablesAnalysisClass;
	}

	public void setTheVariablesAnalysisClass(VaraiblesAnalyses theVariablesAnalysisClass) {
		this.theVariablesAnalysisClass = theVariablesAnalysisClass;
	}

	public InputOutputTree getTheInputTree() {
		return theInputTree;
	}

	public void setTheInputTree(InputOutputTree theInputTree) {
		this.theInputTree = theInputTree;
	}


}
