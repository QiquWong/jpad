package jpadcommander.inputmanager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;

import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import graphics.ChartCanvas;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javaslang.Tuple;
import javaslang.Tuple2;
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
	private TextField textFieldAirfoilName;
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
	@FXML
	private ChoiceBox<String> choiceBoxAirfoilFamily;
	@FXML
	private ChoiceBox<String> choiceBoxAirfoilType;
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
	ObservableList<String> airfoilFamilyList = FXCollections.observableArrayList(
			"NACA_4_Digit",
			"NACA_5_Digit",
			"NACA_63_Series",
			"NACA_64_Series",
			"NACA_65_Series",
			"NACA_66_Series",
			"BICONVEX",
			"DOUBLE_WEDGE"
			);
	ObservableList<String> airfoilTypeList = FXCollections.observableArrayList(
			"CONVENTIONAL",
			"PEAKY",
			"SUPERCRITICAL",
			"LAMINAR",
			"MODERN_SUPERCRITICAL"
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
		choiceBoxAirfoilFamily.setItems(airfoilFamilyList);
		choiceBoxAirfoilType.setItems(airfoilTypeList);
		
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
	
	@SuppressWarnings("static-access")
	public void loadAirfoilData(AirfoilCreator airfoil) {
		
		//---------------------------------------------------------------------------------
		// NAME:
		if (airfoil.getName() != null) 
			textFieldAirfoilName.setText(String.valueOf(airfoil.getName()));
		else
			textFieldAirfoilName.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// FAMILY:
		if(airfoil.getFamily() != null) { 
			if(airfoil.getFamily().toString().equalsIgnoreCase("NACA_4_Digit"))
				choiceBoxAirfoilFamily.getSelectionModel().select(0);
			else if(airfoil.getFamily().toString().equalsIgnoreCase("NACA_5_Digit"))
				choiceBoxAirfoilFamily.getSelectionModel().select(1);
			else if(airfoil.getFamily().toString().equalsIgnoreCase("NACA_63_Series"))
				choiceBoxAirfoilFamily.getSelectionModel().select(2);
			else if(airfoil.getFamily().toString().equalsIgnoreCase("NACA_64_Series"))
				choiceBoxAirfoilFamily.getSelectionModel().select(3);
			else if(airfoil.getFamily().toString().equalsIgnoreCase("NACA_65_Series"))
				choiceBoxAirfoilFamily.getSelectionModel().select(4);
			else if(airfoil.getFamily().toString().equalsIgnoreCase("NACA_66_Series"))
				choiceBoxAirfoilFamily.getSelectionModel().select(5);
			else if(airfoil.getFamily().toString().equalsIgnoreCase("BICONVEX"))
				choiceBoxAirfoilFamily.getSelectionModel().select(6);
			else if(airfoil.getFamily().toString().equalsIgnoreCase("DOUBLE_WEDGE"))
				choiceBoxAirfoilFamily.getSelectionModel().select(7);
		}
		
		//---------------------------------------------------------------------------------
		// TYPE:
		if(airfoil.getType() != null) { 
			if(airfoil.getType().toString().equalsIgnoreCase("CONVENTIONAL"))
				choiceBoxAirfoilType.getSelectionModel().select(0);
			else if(airfoil.getType().toString().equalsIgnoreCase("PEAKY"))
				choiceBoxAirfoilType.getSelectionModel().select(1);
			else if(airfoil.getType().toString().equalsIgnoreCase("SUPERCRITICAL"))
				choiceBoxAirfoilType.getSelectionModel().select(2);
			else if(airfoil.getType().toString().equalsIgnoreCase("LAMINAR"))
				choiceBoxAirfoilType.getSelectionModel().select(3);
			else if(airfoil.getType().toString().equalsIgnoreCase("MODERN_SUPERCRITICAL"))
				choiceBoxAirfoilType.getSelectionModel().select(4);
		}
		
		//---------------------------------------------------------------------------------
		// X COORDINATES:
		if (airfoil.getXCoords() != null) {
			for (int i=0; i<airfoil.getXCoords().length; i++) {
				
				int columnIndex = 0;
				int rowIndex = i+4;
				
				// FIXME !!
				for (Node child : gridPaneAirfoilCoordinates.getChildren().sorted()) {
				    Integer column = GridPane.getColumnIndex(child);
				    Integer row = GridPane.getRowIndex(child);
				    if (column == columnIndex && row == rowIndex) {
				        ((TextField) child).setText(String.valueOf(airfoil.getXCoords()[i]));
				    }
				}
			}
		}
		
		//---------------------------------------------------------------------------------
		// Z COORDINATES:
		if (airfoil.getZCoords() != null) {
			for (int i=0; i<airfoil.getZCoords().length; i++) {
				
				int columnIndex = 1;
				int rowIndex = i+4;

				// FIXME !!
				for (Node child : gridPaneAirfoilCoordinates.getChildren()) {
				    Integer column = GridPane.getColumnIndex(child);
				    Integer row = GridPane.getRowIndex(child);
				    if (column == columnIndex && row == rowIndex) {
				        ((TextField) child).setText(String.valueOf(airfoil.getZCoords()[i]));
				    }
				}
			}
		}
		
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
		
		//---------------------------------------------------------------------------------
		// Cl STAR: 
		if(airfoil.getClEndLinearTrait() != null) 
			textFieldClStar.setText(String.valueOf(airfoil.getClEndLinearTrait()));
		else
			textFieldClStar.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// Cl MAX: 
		if(airfoil.getClMax() != null) 
			textFieldClMax.setText(String.valueOf(airfoil.getClMax()));
		else
			textFieldClMax.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// Cl AT Cd MIN: 
		if(airfoil.getClAtCdMin() != null) 
			textFieldClAtCdMin.setText(String.valueOf(airfoil.getClAtCdMin()));
		else
			textFieldClAtCdMin.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// Cd MIN: 
		if(airfoil.getCdMin() != null) 
			textFieldCdMin.setText(String.valueOf(airfoil.getCdMin()));
		else
			textFieldCdMin.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// LAMINAR BUCKET SEMI-EXTENSION: 
		if(airfoil.getLaminarBucketSemiExtension() != null) 
			textFieldLaminarBucketSemiExtension.setText(String.valueOf(airfoil.getLaminarBucketSemiExtension()));
		else
			textFieldLaminarBucketSemiExtension.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// LAMINAR BUCKET DEPTH: 
		if(airfoil.getLaminarBucketDepth() != null) 
			textFieldLaminarBucketDepth.setText(String.valueOf(airfoil.getLaminarBucketDepth()));
		else
			textFieldLaminarBucketDepth.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// K FACTOR DRAG POLAR: 
		if(airfoil.getKFactorDragPolar() != null) 
			textFieldKFactorDragPolar.setText(String.valueOf(airfoil.getKFactorDragPolar()));
		else
			textFieldKFactorDragPolar.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// Cm ALPHA: 
		if(airfoil.getCmAlphaQuarterChord() != null) {
			
			textFieldCmAlpha.setText(String.valueOf(airfoil.getCmAlphaQuarterChord().getEstimatedValue()));
			
			if(airfoil.getCmAlphaQuarterChord().getUnit().toString().equalsIgnoreCase("1/°")
					|| airfoil.getCmAlphaQuarterChord().getUnit().toString().equalsIgnoreCase("1/deg"))
				choiceBoxCmAlphaUnit.getSelectionModel().select(0);
			else if(airfoil.getCmAlphaQuarterChord().getUnit().toString().equalsIgnoreCase("1/rad"))
				choiceBoxCmAlphaUnit.getSelectionModel().select(1);
			
		}
		else
			textFieldCmAlpha.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// Cm AC: 
		if(airfoil.getCmAC() != null) 
			textFieldCmAC.setText(String.valueOf(airfoil.getCmAC()));
		else
			textFieldCmAC.setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// Cm AC STALL: 
		if(airfoil.getCmACAtStall() != null) 
			textFieldCmACStall.setText(String.valueOf(airfoil.getCmACAtStall()));
		else
			textFieldCmACStall.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NORMALIZED Xac: 
		if(airfoil.getXACNormalized() != null) 
			textFieldAerodynamicCenterAdimensionalPosition.setText(String.valueOf(airfoil.getXACNormalized()));
		else
			textFieldAerodynamicCenterAdimensionalPosition.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CRITICAL MACH NUMBER: 
		if(airfoil.getMachCritical() != null) 
			textFieldCriticalMachNumber.setText(String.valueOf(airfoil.getMachCritical()));
		else
			textFieldCriticalMachNumber.setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// X TRANSITION UPPER: 
		if(airfoil.getXTransitionUpper() != null) 
			textFieldXTransitionUpper.setText(String.valueOf(airfoil.getXTransitionUpper()));
		else
			textFieldXTransitionUpper.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// X TRANSITION LOWER: 
		if(airfoil.getXTransitionLower() != null) 
			textFieldXTransitionLower.setText(String.valueOf(airfoil.getXTransitionLower()));
		else
			textFieldXTransitionLower.setText(
					"NOT INITIALIZED"
					);
		
	}
	
	public void createAirfoilView() {
		
		//--------------------------------------------------
		// get data vectors from airfoil
		//--------------------------------------------------
		
		// TODO: POINT AT THE CORRECT COORDINATES
		Double[] xCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(0).getAirfoilCreator().getXCoords();
		Double[] zCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(0).getAirfoilCreator().getZCoords();
		
		XYSeries seriesAirfoil = new XYSeries("Airfoil", false);
		IntStream.range(0, xCoordinates.length)
		.forEach(i -> {
			seriesAirfoil.add(
					xCoordinates[i],
					zCoordinates[i]
					);
		});
		
		double xMax = 1.1;
		double xMin = -0.1;
		double yMax = xMax;
		double yMin = xMin;
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesAirfoil, Color.decode("#87CEFA")));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Airfoil" + textFieldAirfoilName.getText() + " coordinates representation", 
				"x (m)", 
				"z (m)",
				(XYDataset) dataset,
				PlotOrientation.VERTICAL,
                false, // legend
                true,  // tooltips
                false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.0f);
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(xMin, xMax);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(yMin, yMax);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
		}
		
		plot.setRenderer(0, xyAreaRenderer);
		plot.setDataset(0, dataset);
		plot.setRenderer(1, xyLineAndShapeRenderer);
		plot.setDataset(1, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "Airfoil_" + textFieldAirfoilName.getText() + ".svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  
		
		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  
		
		//create browser
		Scene sceneSideView = new Scene(stackPane, WIDTH+10, HEIGHT+10, javafx.scene.paint.Color.web("#666970"));
		paneAirfoilCoordinates.getChildren().add(sceneSideView.getRoot());
		
	}
	
	public void createAirfoilCurves() {
		
	}
	
}
