package jpadcommander.inputmanager;

import java.io.File;
import java.io.IOException;

import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import jpadcommander.Main;

public class AirfoilInputManagerController {

	//-------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	//-------------------------------------------------------------------------------------------
	//...........................................................................................
	// LAYOUTS:
	//...........................................................................................
	@FXML
	private TabPane tabPaneAirfoilDetails;
	@FXML
	private Pane paneAirfoilCoordinates;
	@FXML
	private Pane paneAirfoilView;
	@FXML
	private Pane paneAirfoilClCurveView;
	@FXML
	private Pane paneAirfoilCdCurveView;
	@FXML
	private Pane paneAirfoilCmCurveView;
	//...........................................................................................
	// BUTTONS:
	//...........................................................................................
	@FXML
	private Button buttonLoadCoordinatesFile;
	@FXML
	private Button buttonChooseCoordinatesFile;
	@FXML
	private Button buttonLoadClCurve;
	@FXML
	private Button buttonChooseClCurveFile;
	@FXML
	private Button buttonLoadCdCurve;
	@FXML
	private Button buttonChooseCdCurveFile;
	@FXML
	private Button buttonLoadCmCurve;
	@FXML
	private Button buttonChooseCmCurveFile;
	//...........................................................................................
	// FILE CHOOSER:
	//...........................................................................................
	private FileChooser airfoilCoordinatesFileChooser;
	private FileChooser airfoilClCurveFileChooser;
	private FileChooser airfoilCdCurveFileChooser;
	private FileChooser airfoilCmCurveFileChooser;
	//...........................................................................................
	// FILES:
	//...........................................................................................
	private String airfoilCoordinatesFilePath;
	private String airfoilClCurveFilePath;
	private String airfoilCdCurveFilePath;
	private String airfoilCmCurveFilePath;
	//...........................................................................................
	// SCROLL PANES:
	//...........................................................................................
	@FXML
	private ScrollPane scrollPaneClCurve;
	@FXML
	private ScrollPane scrollPaneCdCurve;
	@FXML
	private ScrollPane scrollPaneCmCurve;
	
	//...........................................................................................
	// CHECK BOXES:
	//...........................................................................................
	@FXML
	private CheckBox checkBoxExternalCoordinates;
	@FXML
	private CheckBox checkBoxExternalClCurve;
	@FXML
	private CheckBox checkBoxExternalCdCurve;
	@FXML
	private CheckBox checkBoxExternalCmCurve;
	@FXML
	private CheckBox checkBoxLoadClCurve;
	@FXML
	private CheckBox checkBoxLoadCdCurve;
	@FXML
	private CheckBox checkBoxLoadCmCurve;
	//...........................................................................................
	// GRID PANES:
	//...........................................................................................
	@FXML
	private GridPane gridPaneAirfoilCoordinates;
	@FXML
	private GridPane gridPaneAirfoilClCurve;
	@FXML
	private GridPane gridPaneAirfoilCdCurve;
	@FXML
	private GridPane gridPaneAirfoilCmCurve;
	//...........................................................................................
	// TEXT FIELDS:
	//...........................................................................................
	@FXML
	private TextField textFieldMaximumThicknessRatio;
	@FXML
	private TextField textFieldNoramlizedLERadius;
	@FXML
	private TextField textFieldAlphaZeroLift;
	@FXML
	private TextField textFieldAlphaStar;
	@FXML
	private TextField textFieldAlphaStall;
	@FXML
	private TextField textFieldClAlpha;
	@FXML
	private TextField textFieldClZero;
	@FXML
	private TextField textFieldClStar;
	@FXML
	private TextField textFieldClMax;
	@FXML
	private TextField textFieldCdMin;
	@FXML
	private TextField textFieldClAtCdMin;
	@FXML
	private TextField textFieldLaminarBucketSemiExtension;
	@FXML
	private TextField textFieldLaminarBucketDepth;
	@FXML
	private TextField textFieldKFactorDragPolar;
	@FXML
	private TextField textFieldCmAlpha;
	@FXML
	private TextField textFieldCmAC;
	@FXML
	private TextField textFieldCmACStall;
	@FXML
	private TextField textFieldAerodynamicCenterAdimensionalPosition;
	@FXML
	private TextField textFieldCriticalMachNumber;
	@FXML
	private TextField textFieldXTransitionUpper;
	@FXML
	private TextField textFieldXTransitionLower;
	//...........................................................................................
	// CHOICE BOXES:
	//...........................................................................................
	@FXML
	private ChoiceBox<String> choiceBoxAlphaZeroLiftUnit;
	@FXML
	private ChoiceBox<String> choiceBoxAlphaStarUnit;
	@FXML
	private ChoiceBox<String> choiceBoxAlphaStallUnit;
	@FXML
	private ChoiceBox<String> choiceBoxClAlphaUnit;
	@FXML
	private ChoiceBox<String> choiceBoxCmAlphaUnit;
	//...........................................................................................
	// OBSERVABLE LISTS:
	//...........................................................................................
	ObservableList<String> angleUnitList = FXCollections.observableArrayList(
			"°",
			"rad"
			);
	ObservableList<String> slopeUnitList = FXCollections.observableArrayList(
			"1/°",
			"1/rad"
			);
	
	//-------------------------------------------------------------------------------------------
	// METHODS:
	//-------------------------------------------------------------------------------------------
	@FXML
	private void initialize() {
		
		externalCoordinatesDisableCheck();
		externalClCurveDisableCheck();
		externalCdCurveDisableCheck();
		externalCmCurveDisableCheck();
		externalClCurveGridPaneDisableCheck();
		externalCdCurveGridPaneDisableCheck();
		externalCmCurveGridPaneDisableCheck();
		
		//...........................................................................................
		// CHOICHE BOXES INITIALIZATION:
		//...........................................................................................
		choiceBoxAlphaZeroLiftUnit.setItems(angleUnitList);
		choiceBoxAlphaStarUnit.setItems(angleUnitList);
		choiceBoxAlphaStallUnit.setItems(angleUnitList);
		choiceBoxClAlphaUnit.setItems(slopeUnitList);
		choiceBoxCmAlphaUnit.setItems(slopeUnitList);
		
		
	}
	
	private void externalClCurveDisableCheck() {

		textFieldAlphaZeroLift.disableProperty().bind(checkBoxExternalClCurve.selectedProperty());
		textFieldAlphaStar.disableProperty().bind(checkBoxExternalClCurve.selectedProperty());
		textFieldAlphaStall.disableProperty().bind(checkBoxExternalClCurve.selectedProperty());
		textFieldClAlpha.disableProperty().bind(checkBoxExternalClCurve.selectedProperty());
		textFieldClZero.disableProperty().bind(checkBoxExternalClCurve.selectedProperty());
		textFieldClStar.disableProperty().bind(checkBoxExternalClCurve.selectedProperty());
		textFieldClMax.disableProperty().bind(checkBoxExternalClCurve.selectedProperty());
		choiceBoxAlphaZeroLiftUnit.disableProperty().bind(checkBoxExternalClCurve.selectedProperty());
		choiceBoxAlphaStarUnit.disableProperty().bind(checkBoxExternalClCurve.selectedProperty());
		choiceBoxAlphaStallUnit.disableProperty().bind(checkBoxExternalClCurve.selectedProperty());
		choiceBoxClAlphaUnit.disableProperty().bind(checkBoxExternalClCurve.selectedProperty());
		
		scrollPaneClCurve.disableProperty().bind(checkBoxExternalClCurve.selectedProperty().not());

	}

	private void externalCdCurveDisableCheck() {

		textFieldCdMin.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty());
		textFieldClAtCdMin.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty());
		textFieldLaminarBucketSemiExtension.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty());
		textFieldLaminarBucketDepth.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty());
		textFieldKFactorDragPolar.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty());

		scrollPaneCdCurve.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty().not());
		
	}

	private void externalCmCurveDisableCheck() {

		textFieldCmAlpha.disableProperty().bind(checkBoxExternalCmCurve.selectedProperty());
		textFieldCmAC.disableProperty().bind(checkBoxExternalCmCurve.selectedProperty());
		textFieldCmACStall.disableProperty().bind(checkBoxExternalCmCurve.selectedProperty());
		choiceBoxCmAlphaUnit.disableProperty().bind(checkBoxExternalCmCurve.selectedProperty());

		scrollPaneCmCurve.disableProperty().bind(checkBoxExternalCmCurve.selectedProperty().not());
		
	}
	
	private void externalClCurveGridPaneDisableCheck() {
		
		for(int i=7; i<gridPaneAirfoilClCurve.getChildren().size(); i++)
			gridPaneAirfoilClCurve.getChildren().sorted().get(i).disableProperty().bind(checkBoxLoadClCurve.selectedProperty());
		
		buttonChooseClCurveFile.disableProperty().bind(checkBoxLoadClCurve.selectedProperty().not());
		buttonLoadClCurve.disableProperty().bind(checkBoxLoadClCurve.selectedProperty().not());
		
	}
	
	private void externalCdCurveGridPaneDisableCheck() {
		
		for(int i=7; i<gridPaneAirfoilCdCurve.getChildren().size(); i++)
			gridPaneAirfoilCdCurve.getChildren().sorted().get(i).disableProperty().bind(checkBoxLoadCdCurve.selectedProperty());
		
		buttonChooseCdCurveFile.disableProperty().bind(checkBoxLoadCdCurve.selectedProperty().not());
		buttonLoadCdCurve.disableProperty().bind(checkBoxLoadCdCurve.selectedProperty().not());
		
	}
	
	private void externalCmCurveGridPaneDisableCheck() {
		
		for(int i=7; i<gridPaneAirfoilCmCurve.getChildren().size(); i++)
			gridPaneAirfoilCmCurve.getChildren().sorted().get(i).disableProperty().bind(checkBoxLoadCmCurve.selectedProperty());
		
		buttonChooseCmCurveFile.disableProperty().bind(checkBoxLoadCmCurve.selectedProperty().not());
		buttonLoadCmCurve.disableProperty().bind(checkBoxLoadCmCurve.selectedProperty().not());
		
	}
	
	private void externalCoordinatesDisableCheck() {
				
		buttonChooseCoordinatesFile.disableProperty().bind(checkBoxExternalCoordinates.selectedProperty().not());
		buttonLoadCoordinatesFile.disableProperty().bind(checkBoxExternalCoordinates.selectedProperty().not());
		
		for(int i=7; i<gridPaneAirfoilCoordinates.getChildren().size(); i++)
			gridPaneAirfoilCoordinates.getChildren().sorted().get(i).disableProperty().bind(checkBoxExternalCoordinates.selectedProperty());
		
	}
	
	@FXML
	private void chooseCoordinatesFile() throws IOException {

		airfoilCoordinatesFileChooser = new FileChooser();
		airfoilCoordinatesFileChooser.setTitle("Open File");
		airfoilCoordinatesFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() + File.separator 
						+ "Template_Aircraft" + File.separator 
						+ "lifting_surfaces" + File.separator
						+ "airfoils"));
		File file = airfoilCoordinatesFileChooser.showOpenDialog(null);
		if (file != null) {
			airfoilCoordinatesFilePath = file.getAbsolutePath();
		}
	}
	
	@FXML
	private void chooseClCurveFile() throws IOException {

		airfoilClCurveFileChooser = new FileChooser();
		airfoilClCurveFileChooser.setTitle("Open File");
		airfoilClCurveFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() + File.separator 
						+ "Template_Aircraft" + File.separator 
						+ "lifting_surfaces" + File.separator
						+ "airfoils"));
		File file = airfoilClCurveFileChooser.showOpenDialog(null);
		if (file != null) {
			airfoilClCurveFilePath = file.getAbsolutePath();
		}
	}
	
	@FXML
	private void chooseCdCurveFile() throws IOException {

		airfoilCdCurveFileChooser = new FileChooser();
		airfoilCdCurveFileChooser.setTitle("Open File");
		airfoilCdCurveFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() + File.separator 
						+ "Template_Aircraft" + File.separator 
						+ "lifting_surfaces" + File.separator
						+ "airfoils"));
		File file = airfoilCdCurveFileChooser.showOpenDialog(null);
		if (file != null) {
			airfoilCdCurveFilePath = file.getAbsolutePath();
		}
	}
	
	@FXML
	private void chooseCmCurveFile() throws IOException {

		airfoilCmCurveFileChooser = new FileChooser();
		airfoilCmCurveFileChooser.setTitle("Open File");
		airfoilCmCurveFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() + File.separator 
						+ "Template_Aircraft" + File.separator 
						+ "lifting_surfaces" + File.separator
						+ "airfoils"));
		File file = airfoilCmCurveFileChooser.showOpenDialog(null);
		if (file != null) {
			airfoilCmCurveFilePath = file.getAbsolutePath();
		}
	}
	
	@FXML
	private void loadCoordinatesFile() {
		
		// TODO ...
		
	}
	
	@FXML
	private void loadClCurveFile() {
		
		// TODO ...
		
	}
	
	@FXML
	private void loadCdCurveFile() {
		
		// TODO ...
		
	}
	
	@FXML
	private void loadCmCurveFile() {
		
		// TODO ...
		
	}
	
	public void loadAirfoilData(AirfoilCreator airfoil) {
		
		//---------------------------------------------------------------------------------
		// t/c MAX:
		if (airfoil.getThicknessToChordRatio() != null) 
			textFieldMaximumThicknessRatio.setText(String.valueOf(airfoil.getThicknessToChordRatio()));
		else
			textFieldMaximumThicknessRatio.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NORMALIZED LE RADIUS:
		if (airfoil.getRadiusLeadingEdge() != null) 
			textFieldNoramlizedLERadius.setText(String.valueOf(airfoil.getRadiusLeadingEdge()));
		else
			textFieldNoramlizedLERadius.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// EXTERNAL Cl CURVE FLAG: 
		if(airfoil.getClCurveFromFile().equals(Boolean.TRUE))
			checkBoxExternalClCurve.setSelected(true);
		
		//---------------------------------------------------------------------------------
		// EXTERNAL Cd CURVE FLAG: 
		if(airfoil.getCdCurveFromFile().equals(Boolean.TRUE))
			checkBoxExternalCdCurve.setSelected(true);
		
		//---------------------------------------------------------------------------------
		// EXTERNAL Cm CURVE FLAG: 
		if(airfoil.getCmCurveFromFile().equals(Boolean.TRUE))
			checkBoxExternalCmCurve.setSelected(true);
		
		//---------------------------------------------------------------------------------
		// ALPHA ZERO LIFT: 
		if(airfoil.getAlphaZeroLift() != null) {
			
			textFieldAlphaZeroLift.setText(String.valueOf(airfoil.getAlphaZeroLift().getEstimatedValue()));
			
			if(airfoil.getAlphaZeroLift().getUnit().toString().equalsIgnoreCase("°")
					|| airfoil.getAlphaZeroLift().getUnit().toString().equalsIgnoreCase("deg"))
				choiceBoxAlphaZeroLiftUnit.getSelectionModel().select(0);
			else if(airfoil.getAlphaZeroLift().getUnit().toString().equalsIgnoreCase("rad"))
				choiceBoxAlphaZeroLiftUnit.getSelectionModel().select(1);
			
		}
		else
			textFieldAlphaZeroLift.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// ALPHA STAR: 
		if(airfoil.getAlphaEndLinearTrait() != null) {
			
			textFieldAlphaStar.setText(String.valueOf(airfoil.getAlphaEndLinearTrait().getEstimatedValue()));
			
			if(airfoil.getAlphaEndLinearTrait().getUnit().toString().equalsIgnoreCase("°")
					|| airfoil.getAlphaEndLinearTrait().getUnit().toString().equalsIgnoreCase("deg"))
				choiceBoxAlphaStarUnit.getSelectionModel().select(0);
			else if(airfoil.getAlphaEndLinearTrait().getUnit().toString().equalsIgnoreCase("rad"))
				choiceBoxAlphaStarUnit.getSelectionModel().select(1);
			
		}
		else
			textFieldAlphaStar.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// ALPHA STALL: 
		if(airfoil.getAlphaStall() != null) {
			
			textFieldAlphaStall.setText(String.valueOf(airfoil.getAlphaStall().getEstimatedValue()));
			
			if(airfoil.getAlphaStall().getUnit().toString().equalsIgnoreCase("°")
					|| airfoil.getAlphaStall().getUnit().toString().equalsIgnoreCase("deg"))
				choiceBoxAlphaStallUnit.getSelectionModel().select(0);
			else if(airfoil.getAlphaStall().getUnit().toString().equalsIgnoreCase("rad"))
				choiceBoxAlphaStallUnit.getSelectionModel().select(1);
			
		}
		else
			textFieldAlphaStall.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// Cl ALPHA: 
		if(airfoil.getClAlphaLinearTrait() != null) {
			
			textFieldClAlpha.setText(String.valueOf(airfoil.getClAlphaLinearTrait().getEstimatedValue()));
			
			if(airfoil.getClAlphaLinearTrait().getUnit().toString().equalsIgnoreCase("1/°")
					|| airfoil.getClAlphaLinearTrait().getUnit().toString().equalsIgnoreCase("1/deg"))
				choiceBoxClAlphaUnit.getSelectionModel().select(0);
			else if(airfoil.getClAlphaLinearTrait().getUnit().toString().equalsIgnoreCase("1/rad"))
				choiceBoxClAlphaUnit.getSelectionModel().select(1);
			
		}
		else
			textFieldClAlpha.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// Cl ZERO: 
		if(airfoil.getClAtAlphaZero() != null) 
			textFieldClZero.setText(String.valueOf(airfoil.getClAtAlphaZero()));
		else
			textFieldClZero.setText(
					"NOT INITIALIZED"
					);
		
		
	}
	
	public void createAirfoilView() {
		
	}
	
	public void createAirfoilCurves() {
		
	}
	
}
