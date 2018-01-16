package jpadcommander.inputmanager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.unit.NonSI;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javaslang.Tuple;
import javaslang.Tuple2;
import jpadcommander.Main;
import standaloneutils.MyArrayUtils;
import writers.JPADStaticWriteUtils;

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
	@FXML
	private Button buttonExportCoordinatesToCSV;
	@FXML
	private Button buttonExportClCurveToCSV;
	@FXML
	private Button buttonExportCdCurveToCSV;
	@FXML
	private Button buttonExportCmCurveToCSV;
	//...........................................................................................
	// FILE CHOOSER:
	//...........................................................................................
	private FileChooser airfoilCoordinatesFileChooser;
	private FileChooser airfoilClCurveFileChooser;
	private FileChooser airfoilCdCurveFileChooser;
	private FileChooser airfoilCmCurveFileChooser;
	private FileChooser airfoilCoordinatesExportCSVFileChooser;
	private FileChooser airfoilClCurveExportCSVFileChooser;
	private FileChooser airfoilCdCurveExportCSVFileChooser;
	private FileChooser airfoilCmCurveExportCSVFileChooser;
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
		loadCoordiantesButtonCSVFileCheck();
		loadClCurveButtonCSVFileCheck();
		loadCdCurveButtonCSVFileCheck();
		loadCmCurveButtonCSVFileCheck();
		
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
		
		//...........................................................................................
		// GRID PANE ROW CONSTRAINS:
		//...........................................................................................
		gridPaneAirfoilCoordinates.getRowConstraints().stream().forEach(rc -> rc.setMinHeight(25));
		gridPaneAirfoilClCurve.getRowConstraints().stream().forEach(rc -> rc.setMinHeight(25));
		gridPaneAirfoilCdCurve.getRowConstraints().stream().forEach(rc -> rc.setMinHeight(25));
		gridPaneAirfoilCmCurve.getRowConstraints().stream().forEach(rc -> rc.setMinHeight(25));
		
	}
	
	private boolean isCSVFile(String pathToCSV) {

		boolean isCSVFile = false;
		
		if(pathToCSV.endsWith(".csv")) {
			File inputFile = new File(pathToCSV);
			if(inputFile.exists()) {
				isCSVFile = true;
			}
		}
		
		return isCSVFile;
	}
	
	private void loadCoordiantesButtonCSVFileCheck() {

		final Tooltip warning = new Tooltip("WARNING : The selected file is not a .csv !!");
		buttonLoadCoordinatesFile.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Point2D p = buttonLoadCoordinatesFile
						.localToScreen(
								-2.5*buttonLoadCoordinatesFile.getLayoutBounds().getMaxX(),
								1.2*buttonLoadCoordinatesFile.getLayoutBounds().getMaxY()
								);
				if(airfoilCoordinatesFilePath != null)
					if(!isCSVFile(airfoilCoordinatesFilePath)) 
						warning.show(buttonLoadCoordinatesFile, p.getX(), p.getY());
			}
		});
		buttonLoadCoordinatesFile.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				warning.hide();
			}
		});
	}
	
	private void loadClCurveButtonCSVFileCheck() {

		final Tooltip warning = new Tooltip("WARNING : The selected file is not a .csv !!");
		buttonLoadClCurve.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Point2D p = buttonLoadClCurve
						.localToScreen(
								-2.5*buttonLoadClCurve.getLayoutBounds().getMaxX(),
								1.2*buttonLoadClCurve.getLayoutBounds().getMaxY()
								);
				if(airfoilClCurveFilePath != null)
					if(!isCSVFile(airfoilClCurveFilePath)) 
						warning.show(buttonLoadClCurve, p.getX(), p.getY());
			}
		});
		buttonLoadClCurve.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				warning.hide();
			}
		});
	}
	
	private void loadCdCurveButtonCSVFileCheck() {

		final Tooltip warning = new Tooltip("WARNING : The selected file is not a .csv !!");
		buttonChooseCdCurveFile.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Point2D p = buttonChooseCdCurveFile
						.localToScreen(
								-2.5*buttonChooseCdCurveFile.getLayoutBounds().getMaxX(),
								1.2*buttonChooseCdCurveFile.getLayoutBounds().getMaxY()
								);
				if(airfoilCdCurveFilePath != null)
					if(!isCSVFile(airfoilCdCurveFilePath)) 
						warning.show(buttonChooseCdCurveFile, p.getX(), p.getY());
			}
		});
		buttonChooseCdCurveFile.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				warning.hide();
			}
		});
	}
	
	private void loadCmCurveButtonCSVFileCheck() {

		final Tooltip warning = new Tooltip("WARNING : The selected file is not a .csv !!");
		buttonChooseCmCurveFile.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Point2D p = buttonChooseCmCurveFile
						.localToScreen(
								-2.5*buttonChooseCmCurveFile.getLayoutBounds().getMaxX(),
								1.2*buttonChooseCmCurveFile.getLayoutBounds().getMaxY()
								);
				if(airfoilCmCurveFilePath != null)
					if(!isCSVFile(airfoilCmCurveFilePath)) 
						warning.show(buttonChooseCmCurveFile, p.getX(), p.getY());
			}
		});
		buttonChooseCmCurveFile.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				warning.hide();
			}
		});
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
		
		gridPaneAirfoilClCurve.getChildren()
		.stream()
		.forEach(child -> {
			if(GridPane.getColumnIndex(child) == null)
				GridPane.setColumnIndex(child, 0);
		});
		gridPaneAirfoilClCurve.getChildren().stream()
		.filter(child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3)
		.map(child -> (TextField) child)
		.forEach(tf -> tf.disableProperty().bind(checkBoxExternalClCurve.selectedProperty().not()));
		buttonChooseClCurveFile.disableProperty().bind(checkBoxExternalClCurve.selectedProperty().not());
		buttonLoadClCurve.disableProperty().bind(checkBoxExternalClCurve.selectedProperty().not());
		buttonExportClCurveToCSV.disableProperty().bind(checkBoxExternalClCurve.selectedProperty().not());
		checkBoxLoadClCurve.disableProperty().bind(checkBoxExternalClCurve.selectedProperty().not());

		checkBoxExternalClCurve.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				if (checkBoxExternalClCurve.selectedProperty().get())
					tabPaneAirfoilDetails.getSelectionModel().select(2);
			}
		});
		
	}

	private void externalCdCurveDisableCheck() {

		textFieldCdMin.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty());
		textFieldClAtCdMin.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty());
		textFieldLaminarBucketSemiExtension.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty());
		textFieldLaminarBucketDepth.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty());
		textFieldKFactorDragPolar.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty());

		gridPaneAirfoilCdCurve.getChildren()
		.stream()
		.forEach(child -> {
			if(GridPane.getColumnIndex(child) == null)
				GridPane.setColumnIndex(child, 0);
		});
		gridPaneAirfoilCdCurve.getChildren().stream()
		.filter(child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3)
		.map(child -> (TextField) child)
		.forEach(tf -> tf.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty().not()));
		buttonChooseCdCurveFile.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty().not());
		buttonLoadCdCurve.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty().not());
		buttonExportCdCurveToCSV.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty().not());
		checkBoxLoadCdCurve.disableProperty().bind(checkBoxExternalCdCurve.selectedProperty().not());
		
		checkBoxExternalCdCurve.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				if (checkBoxExternalCdCurve.selectedProperty().get())
					tabPaneAirfoilDetails.getSelectionModel().select(3);
			}
		});
		
	}

	private void externalCmCurveDisableCheck() {

		textFieldCmAlpha.disableProperty().bind(checkBoxExternalCmCurve.selectedProperty());
		textFieldCmAC.disableProperty().bind(checkBoxExternalCmCurve.selectedProperty());
		textFieldCmACStall.disableProperty().bind(checkBoxExternalCmCurve.selectedProperty());
		choiceBoxCmAlphaUnit.disableProperty().bind(checkBoxExternalCmCurve.selectedProperty());

		gridPaneAirfoilCmCurve.getChildren()
		.stream()
		.forEach(child -> {
			if(GridPane.getColumnIndex(child) == null)
				GridPane.setColumnIndex(child, 0);
		});
		gridPaneAirfoilCmCurve.getChildren().stream()
		.filter(child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3)
		.map(child -> (TextField) child)
		.forEach(tf -> tf.disableProperty().bind(checkBoxExternalCmCurve.selectedProperty().not()));
		buttonChooseCmCurveFile.disableProperty().bind(checkBoxExternalCmCurve.selectedProperty().not());
		buttonLoadCmCurve.disableProperty().bind(checkBoxExternalCmCurve.selectedProperty().not());
		buttonExportCmCurveToCSV.disableProperty().bind(checkBoxExternalCmCurve.selectedProperty().not());
		checkBoxLoadCmCurve.disableProperty().bind(checkBoxExternalCmCurve.selectedProperty().not());
		
		checkBoxExternalCmCurve.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				if (checkBoxExternalCmCurve.selectedProperty().get())
					tabPaneAirfoilDetails.getSelectionModel().select(4);
			}
		});
		
	}
	
	private void externalClCurveGridPaneDisableCheck() {
		
		gridPaneAirfoilClCurve.getChildren()
		.stream()
		.forEach(child -> {
			if(GridPane.getColumnIndex(child) == null)
				GridPane.setColumnIndex(child, 0);
		});
		gridPaneAirfoilClCurve.getChildren().stream()
		.filter(child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3)
		.map(child -> (TextField) child)
		.forEach(tf -> tf.editableProperty().bind(checkBoxLoadClCurve.selectedProperty().not()));
		
		buttonChooseClCurveFile.disableProperty().bind(checkBoxLoadClCurve.selectedProperty().not());
		buttonLoadClCurve.disableProperty().bind(checkBoxLoadClCurve.selectedProperty().not());
		
	}
	
	private void externalCdCurveGridPaneDisableCheck() {
		
		gridPaneAirfoilCdCurve.getChildren()
		.stream()
		.forEach(child -> {
			if(GridPane.getColumnIndex(child) == null)
				GridPane.setColumnIndex(child, 0);
		});
		gridPaneAirfoilCdCurve.getChildren().stream()
		.filter(child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3)
		.map(child -> (TextField) child)
		.forEach(tf -> tf.editableProperty().bind(checkBoxLoadCdCurve.selectedProperty().not()));
		
		buttonChooseCdCurveFile.disableProperty().bind(checkBoxLoadCdCurve.selectedProperty().not());
		buttonLoadCdCurve.disableProperty().bind(checkBoxLoadCdCurve.selectedProperty().not());
		
	}
	
	private void externalCmCurveGridPaneDisableCheck() {
		
		gridPaneAirfoilCmCurve.getChildren()
		.stream()
		.forEach(child -> {
			if(GridPane.getColumnIndex(child) == null)
				GridPane.setColumnIndex(child, 0);
		});
		gridPaneAirfoilCmCurve.getChildren().stream()
		.filter(child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3)
		.map(child -> (TextField) child)
		.forEach(tf -> tf.editableProperty().bind(checkBoxLoadCmCurve.selectedProperty().not()));
		
		buttonChooseCmCurveFile.disableProperty().bind(checkBoxLoadCmCurve.selectedProperty().not());
		buttonLoadCmCurve.disableProperty().bind(checkBoxLoadCmCurve.selectedProperty().not());
		
	}
	
	private void externalCoordinatesDisableCheck() {
				
		buttonChooseCoordinatesFile.disableProperty().bind(checkBoxExternalCoordinates.selectedProperty().not());
		buttonLoadCoordinatesFile.disableProperty().bind(checkBoxExternalCoordinates.selectedProperty().not());
		
		gridPaneAirfoilCoordinates.getChildren()
		.stream()
		.forEach(child -> {
			if(GridPane.getColumnIndex(child) == null)
				GridPane.setColumnIndex(child, 0);
		});
		gridPaneAirfoilCoordinates.getChildren().stream()
		.filter(child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3)
		.map(child -> (TextField) child)
		.forEach(tf -> tf.editableProperty().bind(checkBoxExternalCoordinates.selectedProperty().not()));
		
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

		gridPaneAirfoilCoordinates.getChildren()
		.stream()
		.filter(child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3)
		.forEach(child -> ((TextField) child).clear());
		
		paneAirfoilView.getChildren().clear();
		
		final String DEFAULT_SEPARATOR = ",";

		if(isCSVFile(airfoilCoordinatesFilePath)) {

			BufferedReader reader = null;
			try {
				reader = new BufferedReader(
						new FileReader(new File(airfoilCoordinatesFilePath))
						);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			List<List<String>> coordinates = reader.lines()
					.map(line -> line.trim())
					.map(line -> Arrays.asList(line.split(DEFAULT_SEPARATOR)))
					.collect(Collectors.toList());

			List<Double> xCoordinates = coordinates.subList(1, coordinates.size()).stream()
					.map(coord -> Double.valueOf(coord.get(0)))
					.collect(Collectors.toList());

			List<Double> zCoordinates = coordinates.subList(1, coordinates.size()).stream()
					.map(coord -> Double.valueOf(coord.get(1)))
					.collect(Collectors.toList());

			if (xCoordinates.size() != zCoordinates.size()) {

				System.err.println("WARNING (LOAD COORINATES - AIRFOIL): X AND Z COORDINATES DO NOT HAVE THE SAME SIZE.");
				return;

			}

			//---------------------------------------------------------------------------------
			// XZ COORDINATES GRIDPANE DIMENSION CHECK:
			if (xCoordinates.size() > (gridPaneAirfoilCoordinates.getChildren().size()-7)/2) {
				while ((gridPaneAirfoilCoordinates.getChildren().size()-7)/2 <= xCoordinates.size()) {

					TextField xCoordinateTextField = new TextField();
					xCoordinateTextField.setPrefSize(187, 25);
					TextField zCoordinateTextField = new TextField();
					zCoordinateTextField.setPrefSize(187, 25);

					int currentRowIndex = (gridPaneAirfoilCoordinates.getChildren().size())/2;

					gridPaneAirfoilCoordinates.add(xCoordinateTextField, 0, currentRowIndex);
					gridPaneAirfoilCoordinates.add(zCoordinateTextField, 1, currentRowIndex);

				}
			}

			//---------------------------------------------------------------------------------
			// X COORDINATES:
			if (xCoordinates != null) {

				for (int i=0; i<xCoordinates.size(); i++) {

					int columnIndex = 0;
					int rowIndex = i+4;

					for (Node child : gridPaneAirfoilCoordinates.getChildren().sorted()) {
						Integer column = GridPane.getColumnIndex(child);
						Integer row = GridPane.getRowIndex(child);
						if(row == null)
							row = 0;
						if(column == null)
							column = 0;
						if ((column == columnIndex || column == null) && row == rowIndex) {
							((TextField) child).setText(String.valueOf(xCoordinates.get(i)));
						}
					}
				}
			}

			//---------------------------------------------------------------------------------
			// Z COORDINATES:
			if (zCoordinates != null) {
				for (int i=0; i<zCoordinates.size(); i++) {

					int columnIndex = 1;
					int rowIndex = i+4;

					for (Node child : gridPaneAirfoilCoordinates.getChildren()) {
						Integer column = GridPane.getColumnIndex(child);
						Integer row = GridPane.getRowIndex(child);
						if(row == null)
							row = 0;
						if(column == null)
							column = 0;
						if (column == columnIndex && row == rowIndex) {
							((TextField) child).setText(String.valueOf(zCoordinates.get(i)));
						}
					}
				}
			}

			createAirfoilView();
			externalCoordinatesDisableCheck();
		}
	}
	
	@FXML
	private void loadClCurveFile() {
		
		gridPaneAirfoilClCurve.getChildren()
		.stream()
		.filter(child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3)
		.forEach(child -> ((TextField) child).clear());
		
		paneAirfoilClCurveView.getChildren().clear();
		
		final String DEFAULT_SEPARATOR = ",";

		if(isCSVFile(airfoilClCurveFilePath)) {

			BufferedReader reader = null;
			try {
				reader = new BufferedReader(
						new FileReader(new File(airfoilClCurveFilePath))
						);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			List<List<String>> points = reader.lines()
					.map(line -> line.trim())
					.map(line -> Arrays.asList(line.split(DEFAULT_SEPARATOR)))
					.collect(Collectors.toList());

			List<Double> alpha = points.subList(1, points.size()).stream()
					.map(p -> Double.valueOf(p.get(0)))
					.collect(Collectors.toList());

			List<Double> cl = points.subList(1, points.size()).stream()
					.map(p -> Double.valueOf(p.get(1)))
					.collect(Collectors.toList());

			if (alpha.size() != cl.size()) {

				System.err.println("WARNING (LOAD Cl CURVE - AIRFOIL): ALPHA AND Cl COORDINATES DO NOT HAVE THE SAME SIZE.");
				return;

			}

			//---------------------------------------------------------------------------------
			// GRIDPANE DIMENSION CHECK:
			if (alpha.size() > (gridPaneAirfoilClCurve.getChildren().size()-7)/2) {
				while ((gridPaneAirfoilClCurve.getChildren().size()-7)/2 <= alpha.size()) {

					TextField alphaTextField = new TextField();
					alphaTextField.setPrefSize(187, 25);
					TextField clTextField = new TextField();
					clTextField.setPrefSize(187, 25);

					int currentRowIndex = (gridPaneAirfoilClCurve.getChildren().size())/2;

					gridPaneAirfoilClCurve.add(alphaTextField, 0, currentRowIndex);
					gridPaneAirfoilClCurve.add(clTextField, 1, currentRowIndex);

				}
			}

			//---------------------------------------------------------------------------------
			// ALPHA:
			if (alpha != null) {

				for (int i=0; i<alpha.size(); i++) {

					int columnIndex = 0;
					int rowIndex = i+4;

					for (Node child : gridPaneAirfoilClCurve.getChildren().sorted()) {
						Integer column = GridPane.getColumnIndex(child);
						Integer row = GridPane.getRowIndex(child);
						if(row == null)
							row = 0;
						if(column == null)
							column = 0;
						if ((column == columnIndex || column == null) && row == rowIndex) {
							((TextField) child).setText(String.valueOf(alpha.get(i)));
						}
					}
				}
			}

			//---------------------------------------------------------------------------------
			// Cl:
			if (cl != null) {
				for (int i=0; i<cl.size(); i++) {

					int columnIndex = 1;
					int rowIndex = i+4;

					for (Node child : gridPaneAirfoilClCurve.getChildren()) {
						Integer column = GridPane.getColumnIndex(child);
						Integer row = GridPane.getRowIndex(child);
						if(row == null)
							row = 0;
						if(column == null)
							column = 0;
						if (column == columnIndex && row == rowIndex) {
							((TextField) child).setText(String.valueOf(cl.get(i)));
						}
					}
				}
			}

			createClCurve();
			externalClCurveDisableCheck();
			externalClCurveGridPaneDisableCheck();
		}
	}
	
	@FXML
	private void loadCdCurveFile() {
		
		gridPaneAirfoilCdCurve.getChildren()
		.stream()
		.filter(child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3)
		.forEach(child -> ((TextField) child).clear());
		
		paneAirfoilCdCurveView.getChildren().clear();
		
		final String DEFAULT_SEPARATOR = ",";

		if(isCSVFile(airfoilCdCurveFilePath)) {

			BufferedReader reader = null;
			try {
				reader = new BufferedReader(
						new FileReader(new File(airfoilCdCurveFilePath))
						);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			List<List<String>> points = reader.lines()
					.map(line -> line.trim())
					.map(line -> Arrays.asList(line.split(DEFAULT_SEPARATOR)))
					.collect(Collectors.toList());

			List<Double> cl = points.subList(1, points.size()).stream()
					.map(p -> Double.valueOf(p.get(0)))
					.collect(Collectors.toList());

			List<Double> cd = points.subList(1, points.size()).stream()
					.map(p -> Double.valueOf(p.get(1)))
					.collect(Collectors.toList());

			if (cl.size() != cd.size()) {

				System.err.println("WARNING (LOAD Cd CURVE - AIRFOIL): Cl AND Cd COORDINATES DO NOT HAVE THE SAME SIZE.");
				return;

			}

			//---------------------------------------------------------------------------------
			// GRIDPANE DIMENSION CHECK:
			if (cl.size() > (gridPaneAirfoilCdCurve.getChildren().size()-7)/2) {
				while ((gridPaneAirfoilCdCurve.getChildren().size()-7)/2 <= cl.size()) {

					TextField clTextField = new TextField();
					clTextField.setPrefSize(187, 25);
					TextField cdTextField = new TextField();
					cdTextField.setPrefSize(187, 25);

					int currentRowIndex = (gridPaneAirfoilCdCurve.getChildren().size())/2;

					gridPaneAirfoilCdCurve.add(clTextField, 0, currentRowIndex);
					gridPaneAirfoilCdCurve.add(cdTextField, 1, currentRowIndex);

				}
			}

			//---------------------------------------------------------------------------------
			// Cl:
			if (cl != null) {

				for (int i=0; i<cl.size(); i++) {

					int columnIndex = 0;
					int rowIndex = i+4;

					for (Node child : gridPaneAirfoilCdCurve.getChildren().sorted()) {
						Integer column = GridPane.getColumnIndex(child);
						Integer row = GridPane.getRowIndex(child);
						if(row == null)
							row = 0;
						if(column == null)
							column = 0;
						if ((column == columnIndex || column == null) && row == rowIndex) {
							((TextField) child).setText(String.valueOf(cl.get(i)));
						}
					}
				}
			}

			//---------------------------------------------------------------------------------
			// Cd:
			if (cd != null) {
				for (int i=0; i<cd.size(); i++) {

					int columnIndex = 1;
					int rowIndex = i+4;

					for (Node child : gridPaneAirfoilCdCurve.getChildren()) {
						Integer column = GridPane.getColumnIndex(child);
						Integer row = GridPane.getRowIndex(child);
						if(row == null)
							row = 0;
						if(column == null)
							column = 0;
						if (column == columnIndex && row == rowIndex) {
							((TextField) child).setText(String.valueOf(cd.get(i)));
						}
					}
				}
			}

			createCdCurve();
			externalCdCurveDisableCheck();
			externalCdCurveGridPaneDisableCheck();
		}
		
	}
	
	@FXML
	private void loadCmCurveFile() {
		
		gridPaneAirfoilCmCurve.getChildren()
		.stream()
		.filter(child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3)
		.forEach(child -> ((TextField) child).clear());
		
		paneAirfoilCmCurveView.getChildren().clear();
		
		final String DEFAULT_SEPARATOR = ",";

		if(isCSVFile(airfoilCmCurveFilePath)) {

			BufferedReader reader = null;
			try {
				reader = new BufferedReader(
						new FileReader(new File(airfoilCmCurveFilePath))
						);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			List<List<String>> points = reader.lines()
					.map(line -> line.trim())
					.map(line -> Arrays.asList(line.split(DEFAULT_SEPARATOR)))
					.collect(Collectors.toList());

			List<Double> cl = points.subList(1, points.size()).stream()
					.map(p -> Double.valueOf(p.get(0)))
					.collect(Collectors.toList());

			List<Double> cm = points.subList(1, points.size()).stream()
					.map(p -> Double.valueOf(p.get(1)))
					.collect(Collectors.toList());

			if (cl.size() != cm.size()) {

				System.err.println("WARNING (LOAD Cm CURVE - AIRFOIL): Cl AND Cm COORDINATES DO NOT HAVE THE SAME SIZE.");
				return;

			}

			//---------------------------------------------------------------------------------
			// GRIDPANE DIMENSION CHECK:
			if (cl.size() > (gridPaneAirfoilCmCurve.getChildren().size()-7)/2) {
				while ((gridPaneAirfoilCmCurve.getChildren().size()-7)/2 <= cl.size()) {

					TextField clTextField = new TextField();
					clTextField.setPrefSize(187, 25);
					TextField cmTextField = new TextField();
					cmTextField.setPrefSize(187, 25);

					int currentRowIndex = (gridPaneAirfoilCmCurve.getChildren().size())/2;

					gridPaneAirfoilCmCurve.add(clTextField, 0, currentRowIndex);
					gridPaneAirfoilCmCurve.add(cmTextField, 1, currentRowIndex);

				}
			}

			//---------------------------------------------------------------------------------
			// Cl:
			if (cl != null) {

				for (int i=0; i<cl.size(); i++) {

					int columnIndex = 0;
					int rowIndex = i+4;

					for (Node child : gridPaneAirfoilCmCurve.getChildren().sorted()) {
						Integer column = GridPane.getColumnIndex(child);
						Integer row = GridPane.getRowIndex(child);
						if(row == null)
							row = 0;
						if(column == null)
							column = 0;
						if ((column == columnIndex || column == null) && row == rowIndex) {
							((TextField) child).setText(String.valueOf(cl.get(i)));
						}
					}
				}
			}

			//---------------------------------------------------------------------------------
			// Cm:
			if (cm != null) {
				for (int i=0; i<cm.size(); i++) {

					int columnIndex = 1;
					int rowIndex = i+4;

					for (Node child : gridPaneAirfoilCmCurve.getChildren()) {
						Integer column = GridPane.getColumnIndex(child);
						Integer row = GridPane.getRowIndex(child);
						if(row == null)
							row = 0;
						if(column == null)
							column = 0;
						if (column == columnIndex && row == rowIndex) {
							((TextField) child).setText(String.valueOf(cm.get(i)));
						}
					}
				}
			}

			createCmCurve();
			externalCmCurveDisableCheck();
			externalCmCurveGridPaneDisableCheck();
		}
		
	}
	
	@FXML
	private void exportCoordinatesToCSV() {

		List<Double[]> xCoordinatesList = new ArrayList<>();
		List<Double[]> zCoordinatesList = new ArrayList<>();
		List<String> fileNameList = new ArrayList<>();
		List<String> xLabelNameList = new ArrayList<>();
		List<String> yLabelNameList = new ArrayList<>();

		Double[] xCoordinates = MyArrayUtils.convertListOfDoubleToDoubleArray( 
				gridPaneAirfoilCoordinates.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 0
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList())
				);

		Double[] zCoordinates = MyArrayUtils.convertListOfDoubleToDoubleArray( 
				gridPaneAirfoilCoordinates.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 1
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList())
				);
		
		
		airfoilCoordinatesExportCSVFileChooser = new FileChooser();
        airfoilCoordinatesExportCSVFileChooser.setTitle("Save as ...");
		airfoilCoordinatesExportCSVFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() + File.separator 
						+ "Template_Aircraft" + File.separator 
						+ "lifting_surfaces" + File.separator
						+ "airfoils"  + File.separator
						+ "coordiantesAndCurvePoints"));
		File file = airfoilCoordinatesExportCSVFileChooser.showSaveDialog(null);
		xCoordinatesList.add(xCoordinates);
		zCoordinatesList.add(zCoordinates);
		fileNameList.add(file.getName());
		xLabelNameList.add("X");
		yLabelNameList.add("Z");

		JPADStaticWriteUtils.exportToCSV(
				xCoordinatesList,
				zCoordinatesList,
				fileNameList,
				xLabelNameList,
				yLabelNameList,
				file.getParent()
				);
	}
	
	@FXML
	private void exportClCurveToCSV() {

		List<Double[]> alphaList = new ArrayList<>();
		List<Double[]> clList = new ArrayList<>();
		List<String> fileNameList = new ArrayList<>();
		List<String> xLabelNameList = new ArrayList<>();
		List<String> yLabelNameList = new ArrayList<>();

		Double[] alpha = MyArrayUtils.convertListOfDoubleToDoubleArray( 
				gridPaneAirfoilClCurve.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 0
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList())
				);

		Double[] cl = MyArrayUtils.convertListOfDoubleToDoubleArray( 
				gridPaneAirfoilClCurve.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 1
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList())
				);
		
		airfoilClCurveExportCSVFileChooser = new FileChooser();
		airfoilClCurveExportCSVFileChooser.setTitle("Save as ...");
		airfoilClCurveExportCSVFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() + File.separator 
						+ "Template_Aircraft" + File.separator 
						+ "lifting_surfaces" + File.separator
						+ "airfoils"  + File.separator
						+ "coordiantesAndCurvePoints"));
		
		File file = airfoilClCurveExportCSVFileChooser.showSaveDialog(null);
		if(file != null) {
			alphaList.add(alpha);
			clList.add(cl);
			fileNameList.add(file.getName());
			xLabelNameList.add("Alpha (deg)");
			yLabelNameList.add("Cl");

			JPADStaticWriteUtils.exportToCSV(
					alphaList,
					clList,
					fileNameList,
					xLabelNameList,
					yLabelNameList,
					file.getParent()
					);
		}
	}
	
	@FXML
	private void exportCdCurveToCSV() {

		List<Double[]> clList = new ArrayList<>();
		List<Double[]> cdList = new ArrayList<>();
		List<String> fileNameList = new ArrayList<>();
		List<String> xLabelNameList = new ArrayList<>();
		List<String> yLabelNameList = new ArrayList<>();

		Double[] cl = MyArrayUtils.convertListOfDoubleToDoubleArray( 
				gridPaneAirfoilCdCurve.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 0
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList())
				);

		Double[] cd = MyArrayUtils.convertListOfDoubleToDoubleArray( 
				gridPaneAirfoilCdCurve.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 1
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList())
				);
		
		airfoilCdCurveExportCSVFileChooser = new FileChooser();
		airfoilCdCurveExportCSVFileChooser.setTitle("Save as ...");
		airfoilCdCurveExportCSVFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() + File.separator 
						+ "Template_Aircraft" + File.separator 
						+ "lifting_surfaces" + File.separator
						+ "airfoils"  + File.separator
						+ "coordiantesAndCurvePoints"));
		File file = airfoilCdCurveExportCSVFileChooser.showSaveDialog(null);
		if(file != null) {
			clList.add(cl);
			cdList.add(cd);
			fileNameList.add(file.getName());
			xLabelNameList.add("Cl");
			yLabelNameList.add("Cd");

			JPADStaticWriteUtils.exportToCSV(
					clList,
					cdList,
					fileNameList,
					xLabelNameList,
					yLabelNameList,
					file.getParent()
					);
		}
	}
	
	@FXML
	private void exportCmCurveToCSV() {

		List<Double[]> clList = new ArrayList<>();
		List<Double[]> cmList = new ArrayList<>();
		List<String> fileNameList = new ArrayList<>();
		List<String> xLabelNameList = new ArrayList<>();
		List<String> yLabelNameList = new ArrayList<>();

		Double[] cl = MyArrayUtils.convertListOfDoubleToDoubleArray( 
				gridPaneAirfoilCmCurve.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 0
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList())
				);

		Double[] cm = MyArrayUtils.convertListOfDoubleToDoubleArray( 
				gridPaneAirfoilCmCurve.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 1
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList())
				);
		
		airfoilCmCurveExportCSVFileChooser = new FileChooser();
		airfoilCmCurveExportCSVFileChooser.setTitle("Save as ...");
		airfoilCmCurveExportCSVFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() + File.separator 
						+ "Template_Aircraft" + File.separator 
						+ "lifting_surfaces" + File.separator
						+ "airfoils"  + File.separator
						+ "coordiantesAndCurvePoints"));
		File file = airfoilCmCurveExportCSVFileChooser.showSaveDialog(null);
		if(file != null) {
			clList.add(cl);
			cmList.add(cm);
			fileNameList.add(file.getName());
			xLabelNameList.add("Cl");
			yLabelNameList.add("Cd");

			JPADStaticWriteUtils.exportToCSV(
					clList,
					cmList,
					fileNameList,
					xLabelNameList,
					yLabelNameList,
					file.getParent()
					);
		}
	}
	
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
		// XZ COORDINATES GRIDPANE DIMENSION CHECK:
		if (airfoil.getXCoords().length > (gridPaneAirfoilCoordinates.getChildren().size()-7)/2) {
			while ((gridPaneAirfoilCoordinates.getChildren().size()-7)/2 <= airfoil.getXCoords().length) {
			
				TextField xCoordinateTextField = new TextField();
				xCoordinateTextField.setPrefSize(187, 25);
				TextField zCoordinateTextField = new TextField();
				zCoordinateTextField.setPrefSize(187, 25);
				
				int currentRowIndex = (gridPaneAirfoilCoordinates.getChildren().size())/2;
				
				gridPaneAirfoilCoordinates.add(xCoordinateTextField, 0, currentRowIndex);
				gridPaneAirfoilCoordinates.add(zCoordinateTextField, 1, currentRowIndex);
				
			}
		}
					
		//---------------------------------------------------------------------------------
		// X COORDINATES:
		if (airfoil.getXCoords() != null) {
			
			for (int i=0; i<airfoil.getXCoords().length; i++) {
				
				int columnIndex = 0;
				int rowIndex = i+4;
				
				for (Node child : gridPaneAirfoilCoordinates.getChildren().sorted()) {
				    Integer column = GridPane.getColumnIndex(child);
				    Integer row = GridPane.getRowIndex(child);
				    if(row == null)
				    	row = 0;
				    if(column == null)
				    	column = 0;
				    if ((column == columnIndex || column == null) && row == rowIndex) {
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

				for (Node child : gridPaneAirfoilCoordinates.getChildren()) {
				    Integer column = GridPane.getColumnIndex(child);
				    Integer row = GridPane.getRowIndex(child);
				    if(row == null)
				    	row = 0;
				    if(column == null)
				    	column = 0;
				    if (column == columnIndex && row == rowIndex) {
				        ((TextField) child).setText(String.valueOf(airfoil.getZCoords()[i]));
				    }
				}
			}
		}
		
		//---------------------------------------------------------------------------------
		// Cl CURVE POINTS GRIDPANE DIMENSION CHECK:
		if(airfoil.getAlphaForClCurve().size() != airfoil.getClCurve().size()) {
			System.err.println("WARNING: (AIRFOIL - Cl CURVE POINTS) ALPHA AND CL CURVE POINTS DO NOT HAVE THE SAME SIZE");
			return;
		}
		
		if (airfoil.getAlphaForClCurve().size() > (gridPaneAirfoilClCurve.getChildren().size()-7)/2) {
			while ((gridPaneAirfoilClCurve.getChildren().size()-7)/2 <= airfoil.getAlphaForClCurve().size()) {
			
				TextField alphaPointTextField = new TextField();
				alphaPointTextField.setPrefSize(187, 25);
				TextField clPointTextField = new TextField();
				clPointTextField.setPrefSize(187, 25);
				
				int currentRowIndex = (gridPaneAirfoilClCurve.getChildren().size())/2;
				
				gridPaneAirfoilClCurve.add(alphaPointTextField, 0, currentRowIndex);
				gridPaneAirfoilClCurve.add(clPointTextField, 1, currentRowIndex);
				
			}
		}
					
		//---------------------------------------------------------------------------------
		// ALPHA FOR Cl CURVE:
		if (airfoil.getAlphaForClCurve() != null) {
			
			for (int i=0; i<airfoil.getAlphaForClCurve().size(); i++) {
				
				int columnIndex = 0;
				int rowIndex = i+4;
				
				for (Node child : gridPaneAirfoilClCurve.getChildren().sorted()) {
				    Integer column = GridPane.getColumnIndex(child);
				    Integer row = GridPane.getRowIndex(child);
				    if(row == null)
				    	row = 0;
				    if(column == null)
				    	column = 0;
				    if ((column == columnIndex || column == null) && row == rowIndex) {
				        ((TextField) child).setText(String.valueOf(airfoil.getAlphaForClCurve().get(i).doubleValue(NonSI.DEGREE_ANGLE)));
				    }
				}
			}
		}
		
		//---------------------------------------------------------------------------------
		// Cl CURVE:
		if (airfoil.getClCurve() != null) {
			for (int i=0; i<airfoil.getClCurve().size(); i++) {
				
				int columnIndex = 1;
				int rowIndex = i+4;

				for (Node child : gridPaneAirfoilClCurve.getChildren()) {
				    Integer column = GridPane.getColumnIndex(child);
				    Integer row = GridPane.getRowIndex(child);
				    if(row == null)
				    	row = 0;
				    if(column == null)
				    	column = 0;
				    if (column == columnIndex && row == rowIndex) {
				        ((TextField) child).setText(String.valueOf(airfoil.getClCurve().get(i)));
				    }
				}
			}
		}
		
		//---------------------------------------------------------------------------------
		// Cd CURVE POINTS GRIDPANE DIMENSION CHECK:
		if(airfoil.getClForCdCurve().size() != airfoil.getCdCurve().size()) {
			System.err.println("WARNING: (AIRFOIL - Cd CURVE POINTS) CL AND CD CURVE POINTS DO NOT HAVE THE SAME SIZE");
			return;
		}
		
		if (airfoil.getClForCdCurve().size() > (gridPaneAirfoilCdCurve.getChildren().size()-7)/2) {
			while ((gridPaneAirfoilCdCurve.getChildren().size()-7)/2 <= airfoil.getClForCdCurve().size()) {
			
				TextField clPointTextField = new TextField();
				clPointTextField.setPrefSize(187, 25);
				TextField cdPointTextField = new TextField();
				cdPointTextField.setPrefSize(187, 25);
				
				int currentRowIndex = (gridPaneAirfoilCdCurve.getChildren().size())/2;
				
				gridPaneAirfoilCdCurve.add(clPointTextField, 0, currentRowIndex);
				gridPaneAirfoilCdCurve.add(cdPointTextField, 1, currentRowIndex);
				
			}
		}
					
		//---------------------------------------------------------------------------------
		// Cl FOR Cd CURVE:
		if (airfoil.getClForCdCurve() != null) {
			
			for (int i=0; i<airfoil.getClForCdCurve().size(); i++) {
				
				int columnIndex = 0;
				int rowIndex = i+4;
				
				for (Node child : gridPaneAirfoilCdCurve.getChildren().sorted()) {
				    Integer column = GridPane.getColumnIndex(child);
				    Integer row = GridPane.getRowIndex(child);
				    if(row == null)
				    	row = 0;
				    if(column == null)
				    	column = 0;
				    if ((column == columnIndex || column == null) && row == rowIndex) {
				        ((TextField) child).setText(String.valueOf(airfoil.getClForCdCurve().get(i)));
				    }
				}
			}
		}
		
		//---------------------------------------------------------------------------------
		// Cd CURVE:
		if (airfoil.getCdCurve() != null) {
			for (int i=0; i<airfoil.getCdCurve().size(); i++) {
				
				int columnIndex = 1;
				int rowIndex = i+4;

				for (Node child : gridPaneAirfoilCdCurve.getChildren()) {
				    Integer column = GridPane.getColumnIndex(child);
				    Integer row = GridPane.getRowIndex(child);
				    if(row == null)
				    	row = 0;
				    if(column == null)
				    	column = 0;
				    if (column == columnIndex && row == rowIndex) {
				        ((TextField) child).setText(String.valueOf(airfoil.getCdCurve().get(i)));
				    }
				}
			}
		}

		//---------------------------------------------------------------------------------
		// Cm CURVE POINTS GRIDPANE DIMENSION CHECK:
		if(airfoil.getClForCmCurve().size() != airfoil.getCmCurve().size()) {
			System.err.println("WARNING: (AIRFOIL - Cm CURVE POINTS) CL AND CM CURVE POINTS DO NOT HAVE THE SAME SIZE");
			return;
		}
		
		if (airfoil.getClForCmCurve().size() > (gridPaneAirfoilCmCurve.getChildren().size()-7)/2) {
			while ((gridPaneAirfoilCmCurve.getChildren().size()-7)/2 <= airfoil.getClForCmCurve().size()) {
			
				TextField clPointTextField = new TextField();
				clPointTextField.setPrefSize(187, 25);
				TextField cmPointTextField = new TextField();
				cmPointTextField.setPrefSize(187, 25);
				
				int currentRowIndex = (gridPaneAirfoilCmCurve.getChildren().size())/2;
				
				gridPaneAirfoilCmCurve.add(clPointTextField, 0, currentRowIndex);
				gridPaneAirfoilCmCurve.add(cmPointTextField, 1, currentRowIndex);
				
			}
		}
					
		//---------------------------------------------------------------------------------
		// Cl FOR Cm CURVE:
		if (airfoil.getClForCmCurve() != null) {
			
			for (int i=0; i<airfoil.getClForCmCurve().size(); i++) {
				
				int columnIndex = 0;
				int rowIndex = i+4;
				
				for (Node child : gridPaneAirfoilCmCurve.getChildren().sorted()) {
				    Integer column = GridPane.getColumnIndex(child);
				    Integer row = GridPane.getRowIndex(child);
				    if(row == null)
				    	row = 0;
				    if(column == null)
				    	column = 0;
				    if ((column == columnIndex || column == null) && row == rowIndex) {
				        ((TextField) child).setText(String.valueOf(airfoil.getClForCmCurve().get(i)));
				    }
				}
			}
		}
		
		//---------------------------------------------------------------------------------
		// Cm CURVE:
		if (airfoil.getCmCurve() != null) {
			for (int i=0; i<airfoil.getCmCurve().size(); i++) {
				
				int columnIndex = 1;
				int rowIndex = i+4;

				for (Node child : gridPaneAirfoilCmCurve.getChildren()) {
				    Integer column = GridPane.getColumnIndex(child);
				    Integer row = GridPane.getRowIndex(child);
				    if(row == null)
				    	row = 0;
				    if(column == null)
				    	column = 0;
				    if (column == columnIndex && row == rowIndex) {
				        ((TextField) child).setText(String.valueOf(airfoil.getCmCurve().get(i)));
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

		gridPaneAirfoilCoordinates.getChildren()
		.stream()
		.forEach(child -> {
			if(GridPane.getColumnIndex(child) == null)
				GridPane.setColumnIndex(child, 0);
		});
		
		List<Double> xCoordinates = gridPaneAirfoilCoordinates.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 0
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList());
		
		List<Double> zCoordinates = gridPaneAirfoilCoordinates.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 1
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList());
		
		XYSeries seriesAirfoil = new XYSeries("Airfoil", false);
		IntStream.range(0, xCoordinates.size())
		.forEach(i -> {
			seriesAirfoil.add(
					xCoordinates.get(i),
					zCoordinates.get(i)
					);
		});
		
		double xMax = 1.1;
		double xMin = -0.1;
		double yMax = 0.575;
		double yMin = -0.575;
		
		int WIDTH = 550;
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
				"Airfoil " + textFieldAirfoilName.getText() + " coordinates representation", 
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
		
		plot.setRenderer(xyLineAndShapeRenderer);
		plot.setDataset(dataset);

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
		paneAirfoilView.getChildren().clear();
		paneAirfoilView.getChildren().add(sceneSideView.getRoot());
		
	}
	
	public void createClCurve() {
		
		gridPaneAirfoilClCurve.getChildren()
		.stream()
		.forEach(child -> {
			if(GridPane.getColumnIndex(child) == null)
				GridPane.setColumnIndex(child, 0);
		});
		
		List<Double> alpha = gridPaneAirfoilClCurve.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 0
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList());
		
		List<Double> cl = gridPaneAirfoilClCurve.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 1
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList());
		
		XYSeries seriesClCurve = new XYSeries("Airfoil Cl Curve", false);
		IntStream.range(0, alpha.size())
		.forEach(i -> {
			seriesClCurve.add(
					alpha.get(i),
					cl.get(i)
					);
		});
		
		double xMax = MyArrayUtils.getMax(alpha) + 0.1*Math.abs(MyArrayUtils.getMax(alpha));
		double xMin = MyArrayUtils.getMin(alpha) - 0.1*Math.abs(MyArrayUtils.getMin(alpha));
		double yMax = MyArrayUtils.getMax(cl) + 0.1*Math.abs(MyArrayUtils.getMax(cl));
		double yMin = MyArrayUtils.getMin(cl) - 0.1*Math.abs(MyArrayUtils.getMin(cl));
		
		int WIDTH = 350;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesClCurve, Color.decode("#87CEFA")));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Cl Curve", 
				"Alpha (deg)", 
				"Cl",
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
		
		plot.setRenderer(xyLineAndShapeRenderer);
		plot.setDataset(dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "Airfoil_" + textFieldAirfoilName.getText() + "_ClCurve.svg";
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
		paneAirfoilClCurveView.getChildren().clear();
		paneAirfoilClCurveView.getChildren().add(sceneSideView.getRoot());
		
	}
	
	public void createCdCurve() {
		
		gridPaneAirfoilCdCurve.getChildren()
		.stream()
		.forEach(child -> {
			if(GridPane.getColumnIndex(child) == null)
				GridPane.setColumnIndex(child, 0);
		});
		
		List<Double> cl = gridPaneAirfoilCdCurve.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 0
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList());
		
		List<Double> cd = gridPaneAirfoilCdCurve.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 1
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList());
		
		XYSeries seriesCdCurve = new XYSeries("Airfoil Cd Curve", false);
		IntStream.range(0, cl.size())
		.forEach(i -> {
			seriesCdCurve.add(
					cd.get(i),
					cl.get(i)
					);
		});
		
		double xMax = MyArrayUtils.getMax(cd) + 0.1*Math.abs(MyArrayUtils.getMax(cd));
		double xMin = MyArrayUtils.getMin(cd) - 0.1*Math.abs(MyArrayUtils.getMin(cd));
		double yMax = MyArrayUtils.getMax(cl) + 0.1*Math.abs(MyArrayUtils.getMax(cl));
		double yMin = MyArrayUtils.getMin(cl) - 0.1*Math.abs(MyArrayUtils.getMin(cl));
		
		int WIDTH = 350;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesCdCurve, Color.decode("#87CEFA")));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Cd Curve", 
				"Cd", 
				"Cl",
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
		
		plot.setRenderer(xyLineAndShapeRenderer);
		plot.setDataset(dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "Airfoil_" + textFieldAirfoilName.getText() + "_CdCurve.svg";
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
		paneAirfoilCdCurveView.getChildren().clear();
		paneAirfoilCdCurveView.getChildren().add(sceneSideView.getRoot());
		
	}
	
	public void createCmCurve() {
		
		gridPaneAirfoilCmCurve.getChildren()
		.stream()
		.forEach(child -> {
			if(GridPane.getColumnIndex(child) == null)
				GridPane.setColumnIndex(child, 0);
		});
		
		List<Double> cl = gridPaneAirfoilCmCurve.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 0
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList());
		
		List<Double> cm = gridPaneAirfoilCmCurve.getChildren()
				.stream()
				.filter(
						child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) > 3
						&& GridPane.getColumnIndex(child) == 1
						&& !((TextField) child).getText().isEmpty()
						)
				.map(child -> (TextField) child)
				.map(tf -> Double.valueOf(tf.getText()))
				.collect(Collectors.toList());
		
		XYSeries seriesCmCurve = new XYSeries("Airfoil Cm Curve", false);
		IntStream.range(0, cl.size())
		.forEach(i -> {
			seriesCmCurve.add(
					cl.get(i),
					cm.get(i)
					);
		});
		
		double xMax = MyArrayUtils.getMax(cl) + 0.1*Math.abs(MyArrayUtils.getMax(cl));
		double xMin = MyArrayUtils.getMin(cl) - 0.1*Math.abs(MyArrayUtils.getMin(cl));
		double yMax = 0.0;
		double yMin = MyArrayUtils.getMin(cm) - 0.1*Math.abs(MyArrayUtils.getMin(cm));
		
		int WIDTH = 350;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesCmCurve, Color.decode("#87CEFA")));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Cm Curve", 
				"Cl", 
				"Cm",
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
		
		plot.setRenderer(xyLineAndShapeRenderer);
		plot.setDataset(dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "Airfoil_" + textFieldAirfoilName.getText() + "_CmCurve.svg";
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
		paneAirfoilCmCurveView.getChildren().clear();
		paneAirfoilCmCurveView.getChildren().add(sceneSideView.getRoot());
		
	}
	
}
