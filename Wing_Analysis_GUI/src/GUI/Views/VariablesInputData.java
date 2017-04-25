package GUI.Views;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.geography.coordinates.Altitude;
import org.jscience.physics.amount.Amount;

import Calculator.InputOutputTree;
import Calculator.Reader;
import GUI.Main;
import configuration.MyConfiguration;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.FoldersEnum;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;


public class VariablesInputData {

	private Main main;

	File inputFile;
	File outputFile;
	InputOutputTree theInputTree = new InputOutputTree();

	@FXML
	RadioButton fromFile;	
	@FXML
	RadioButton userDefined;
	@FXML
	Button load;
	@FXML
	TextField filePath;
	@FXML
	Button searchButton;
	@FXML
	Button goToAnalysisButton;
	@FXML
	Button saveButton;

	//input data variables
	@FXML
	TextField altitude;
	@FXML
	TextField machNumber;
	@FXML
	TextField alphaInitial;
	@FXML
	TextField alphaFinal;
	@FXML
	TextField numberOfAlphas;
	@FXML
	TextField surface;
	@FXML
	TextField aspectRatio;
	@FXML
	TextField numberOfPoints;
	@FXML
	TextField adimensionalKinkStation;
	@FXML
	ChoiceBox airfoilFamily;
	@FXML
	TextField maxThickness;


	@FXML
	TextField adimensionalStations1;
	@FXML
	TextField adimensionalStations2;
	@FXML
	TextField adimensionalStations3;
	@FXML
	TextField adimensionalStations4;
	@FXML
	TextField adimensionalStations5;

	List<TextField> stationList = new ArrayList<>();
	
	@FXML
	TextField chords1;
	@FXML
	TextField chords2;
	@FXML
	TextField chords3;
	@FXML
	TextField chords4;
	@FXML
	TextField chords5;

	List<TextField> chordList = new ArrayList<>();
	
	@FXML
	TextField xle1;
	@FXML
	TextField xle2;
	@FXML
	TextField xle3;
	@FXML
	TextField xle4;
	@FXML
	TextField xle5;

	List<TextField> xleList = new ArrayList<>();
	
	@FXML
	TextField twist1;
	@FXML
	TextField twist2;
	@FXML
	TextField twist3;
	@FXML
	TextField twist4;
	@FXML
	TextField twist5;

	List<TextField> twistList = new ArrayList<>();
	
	@FXML
	TextField alphaZeroLift1;
	@FXML
	TextField alphaZeroLift2;
	@FXML
	TextField alphaZeroLift3;
	@FXML
	TextField alphaZeroLift4;
	@FXML
	TextField alphaZeroLift5;

	List<TextField> alphaZeroList = new ArrayList<>();
	
	@FXML
	TextField alphaStar1;
	@FXML
	TextField alphaStar2;
	@FXML
	TextField alphaStar3;
	@FXML
	TextField alphaStar4;
	@FXML
	TextField alphaStar5;

	List<TextField> alphaStarList = new ArrayList<>();
	
	@FXML
	TextField clMax1;
	@FXML
	TextField clMax2;
	@FXML
	TextField clMax3;
	@FXML
	TextField clMax4;
	@FXML
	TextField clMax5;

	List<TextField> clMaxList = new ArrayList<>();
	
	// units
	@FXML
	ChoiceBox altitudeUnits;
	@FXML
	ChoiceBox alphaInitialUnits;
	@FXML
	ChoiceBox alphaFinalUnits;
	@FXML
	ChoiceBox surfaceUnits;
	@FXML
	ChoiceBox xleUnits;
	@FXML
	ChoiceBox twistUnits;
	@FXML
	ChoiceBox chordsUnits;
	@FXML
	ChoiceBox alphaStarUnits;
	@FXML
	ChoiceBox alphaZeroLiftUnits;
	@FXML
	ChoiceBox numberOfGivenSections;

	
	@FXML
	Pane graphPane;

	// Initialize units box

	ObservableList<String> altitudeUnitsList = FXCollections.observableArrayList("m","ft" );
	ObservableList<String> alphaInitialUnitsList = FXCollections.observableArrayList("°","rad" );
	ObservableList<String> alphaFinalUnitsList = FXCollections.observableArrayList("°","rad" );
	ObservableList<String> surfaceUnitsList = FXCollections.observableArrayList("m²");
	ObservableList<String> xleUnitsList = FXCollections.observableArrayList("m","ft" );
	ObservableList<String> twistUnitsList = FXCollections.observableArrayList("°","rad" );
	ObservableList<String> chordsUnitsList = FXCollections.observableArrayList("m","ft" );
	ObservableList<String> alphaStarUnitsList = FXCollections.observableArrayList("°","rad" );
	ObservableList<String> alphaZeroLiftUnitsList = FXCollections.observableArrayList("°","rad" );
	ObservableList<String> aifoilList = FXCollections.observableArrayList("NACA_4_DIGIT",
			"NACA_5_DIGIT",
			"NACA_63_SERIES",
			"NACA_64_SERIES",
			"NACA_65_SERIES",
			"NACA_66_SERIES",
			"BICONVEX",
			"DOUBLE_WEDGE");
	ObservableList<String> numberOfGivenSectionsList = FXCollections.observableArrayList("2","3","4","5" );

	@FXML
	private void initialize(){
		altitudeUnits.setValue("m");
		altitudeUnits.setItems(altitudeUnitsList);

		surfaceUnits.setValue("m²");
		surfaceUnits.setItems(surfaceUnitsList);

		xleUnits.setValue("m");
		xleUnits.setItems(xleUnitsList);

		twistUnits.setValue("°");
		twistUnits.setItems(twistUnitsList);

		chordsUnits.setValue("m");
		chordsUnits.setItems(chordsUnitsList);

		alphaStarUnits.setValue("°");
		alphaStarUnits.setItems(alphaStarUnitsList);

		alphaZeroLiftUnits.setValue("°");
		alphaZeroLiftUnits.setItems(alphaZeroLiftUnitsList);

		airfoilFamily.setItems(aifoilList);

		numberOfGivenSections.setValue("2");
		numberOfGivenSections.setItems(numberOfGivenSectionsList);

		stationList.add(adimensionalStations1);
		stationList.add(adimensionalStations2);
		stationList.add(adimensionalStations3);
		stationList.add(adimensionalStations4);
		stationList.add(adimensionalStations5);
		chordList.add(chords1);
		chordList.add(chords2);
		chordList.add(chords3);
		chordList.add(chords4);
		chordList.add(chords5);
		xleList.add(xle1);
		xleList.add(xle2);
		xleList.add(xle3);
		xleList.add(xle4);
		xleList.add(xle5);
		twistList.add(twist1);
		twistList.add(twist2);
		twistList.add(twist3);
		twistList.add(twist4);
		twistList.add(twist5);
		alphaStarList.add(alphaStar1);
		alphaStarList.add(alphaStar2);
		alphaStarList.add(alphaStar3);
		alphaStarList.add(alphaStar4);
		alphaStarList.add(alphaStar5);
		alphaZeroList.add(alphaZeroLift1);
		alphaZeroList.add(alphaZeroLift2);
		alphaZeroList.add(alphaZeroLift3);
		alphaZeroList.add(alphaZeroLift4);
		alphaZeroList.add(alphaZeroLift5);
		clMaxList.add(clMax1);
		clMaxList.add(clMax2);
		clMaxList.add(clMax3);
		clMaxList.add(clMax4);
		clMaxList.add(clMax5);
		
		
		
	}

	@FXML
	private void clickInputButton() throws IOException{
		// enable buttons
		filePath.setDisable(false);
		searchButton.setDisable(false);
	}

	@FXML
	private void clickUserDefinedButton() throws IOException{
		// enable buttons
		load.setDisable(true);
		filePath.setDisable(true);
		searchButton.setDisable(true);
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

		String pathToXML = filePath.getText();
		if(pathToAircraftXML.endsWith(".xml")) {
			File inputFile = new File(pathToAircraftXML);
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
	private void searchInputFile() throws IOException{

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose an XML input file");
		chooser.setInitialDirectory(new File(MyConfiguration.getDir(FoldersEnum.INPUT_DIR)));
		chooser.getExtensionFilters().addAll(new ExtensionFilter("XML File","*.xml"));
		File file = chooser.showOpenDialog(null);
		if (file != null) {
			filePath.setText(file.getAbsolutePath());
			inputFile = file;

			// CHECK IF THE TEXT FIELD IS NOT EMPTY ...
			load.disableProperty().bind(
					Bindings.isEmpty(filePath.textProperty())
					);

			// CHECK IF THE FILE IN TEXTFIELD IS A VALID FILE ...
			final Tooltip warning = new Tooltip("WARNING : The selected file is not a valid input !!");
			load.setOnMouseEntered(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					Point2D p = load
							.localToScreen(
									-2.5*load.getLayoutBounds().getMaxX(),
									1.2*load.getLayoutBounds().getMaxY()
									);
					if(!isInputFile(filePath.getText())
							) {
						warning.show(load, p.getX(), p.getY());
					}
				}
			});
			load.setOnMouseExited(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					warning.hide();
				}
			});

		}
	}
	
	@FXML
	private void chooseOutputFile() throws IOException{

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Save input file");
		chooser.setInitialDirectory(new File(MyConfiguration.getDir(FoldersEnum.INPUT_DIR)));
		chooser.getExtensionFilters().addAll(new ExtensionFilter("XML File","*.xml"));
		File file = chooser.showSaveDialog(null);
		if (file != null) {
			outputFile = file;

			// CHECK IF THE TEXT FIELD IS NOT EMPTY ...
			load.disableProperty().bind(
					Bindings.isEmpty(filePath.textProperty())
					);

			// CHECK IF THE FILE IN TEXTFIELD IS A VALID FILE ...
			final Tooltip warning = new Tooltip("WARNING : The selected file is not a valid input !!");
			load.setOnMouseEntered(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					Point2D p = load
							.localToScreen(
									-2.5*load.getLayoutBounds().getMaxX(),
									1.2*load.getLayoutBounds().getMaxY()
									);
					if(!isInputFile(filePath.getText())
							) {
						warning.show(load, p.getX(), p.getY());
					}
				}
			});
			load.setOnMouseExited(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					warning.hide();
				}
			});

		}
	}
	@FXML
	private void goHome() throws IOException{
		main.showCenterItem();
	} 
	
	@FXML
	public void loadDataFile() throws IOException{
		Reader theReader = new Reader();
		theReader.readInputFromXML(this, inputFile.getAbsolutePath());
	}
	
	@FXML
	public void writeInputFile() throws IOException{
		Reader theReader = new Reader();
		System.out.println( "estensione file " + outputFile.getAbsolutePath() + File.separator +  outputFile.getName());
		theReader.writeInputToXML(theInputTree, outputFile.getAbsolutePath() );
	}

	@FXML
	public void saveInputFile() throws IOException{
		chooseOutputFile();
		writeInputFile();
	}
	@SuppressWarnings("unchecked")
	public void ConfirmData(){
		theInputTree = new InputOutputTree();

		//data with units
		theInputTree.setAltitude(
				Amount.valueOf(
						Double.parseDouble(altitude.getText()),
						main.recognizeUnit(altitudeUnits))); 


		theInputTree.setSurface(
				Amount.valueOf(
						Double.parseDouble(surface.getText()),
						SI.SQUARE_METRE)); 


		// data without units
		theInputTree.setMachNumber(Double.parseDouble(machNumber.getText()));
		theInputTree.setAspectRatio(Double.parseDouble(aspectRatio.getText()));
		theInputTree.setNumberOfPointSemispan((int)Double.parseDouble(numberOfPoints.getText()));
		theInputTree.setAdimensionalKinkStation(Double.parseDouble(adimensionalKinkStation.getText()));
		theInputTree.setMeanThickness(Double.parseDouble(maxThickness.getText()));
		theInputTree.setNumberOfSections((int)Double.parseDouble(numberOfGivenSections.getValue().toString()));

		// airfoil
		if(airfoilFamily.getValue().equals("NACA_4_DIGIT"))
			theInputTree.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_4_Digit);
		else if(airfoilFamily.getValue().equals("NACA_5_DIGIT"))
			theInputTree.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_5_Digit);
		else if(airfoilFamily.getValue().equals("NACA_63_SERIES"))
			theInputTree.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_63_Series);
		else if(airfoilFamily.getValue().equals("NACA_64_SERIES"))
			theInputTree.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_64_Series);
		else if(airfoilFamily.getValue().equals("NACA_65_SERIES"))
			theInputTree.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_65_Series);
		else if(airfoilFamily.getValue().equals("NACA_66_SERIES"))
			theInputTree.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_66_Series);
		else if(airfoilFamily.getValue().equals("BICONVEX"))
			theInputTree.setMeanAirfoilFamily(AirfoilFamilyEnum.BICONVEX);
		else if(airfoilFamily.getValue().equals("DOUBLE_WEDGE"))
			theInputTree.setMeanAirfoilFamily(AirfoilFamilyEnum.DOUBLE_WEDGE);

		// distributions

		//STATIONS
		List<Double> inputList= new ArrayList<>();

		inputList.add(Double.parseDouble(adimensionalStations1.getText()));
		inputList.add(Double.parseDouble(adimensionalStations2.getText()));
		if (!adimensionalStations3.getText().trim().isEmpty())
			inputList.add(Double.parseDouble(adimensionalStations3.getText()));
		if (!adimensionalStations4.getText().trim().isEmpty())
			inputList.add(Double.parseDouble(adimensionalStations4.getText()));
		if (!adimensionalStations5.getText().trim().isEmpty())
			inputList.add(Double.parseDouble(adimensionalStations5.getText()));	
		theInputTree.setyAdimensionalStationInput(inputList);

		//CHORDS
		List<Amount<Length>> inputListAmount= new ArrayList<>();

		Unit unit = main.recognizeUnit(chordsUnits);

		inputListAmount.add(Amount.valueOf(Double.parseDouble(chords1.getText()), unit));
		inputListAmount.add(Amount.valueOf(Double.parseDouble(chords2.getText()), unit));
		if (!chords3.getText().trim().isEmpty())
			inputListAmount.add(Amount.valueOf(Double.parseDouble(chords3.getText()), unit));
		if (!chords4.getText().trim().isEmpty())
			inputListAmount.add(Amount.valueOf(Double.parseDouble(chords4.getText()), unit));
		if (!chords5.getText().trim().isEmpty())
			inputListAmount.add(Amount.valueOf(Double.parseDouble(chords5.getText()), unit));
		theInputTree.setChordDistribution(inputListAmount);

		//XLE
		inputListAmount= new ArrayList<>();

		unit = main.recognizeUnit(xleUnits);

		inputListAmount.add(Amount.valueOf(Double.parseDouble(xle1.getText()), unit));
		inputListAmount.add(Amount.valueOf(Double.parseDouble(xle2.getText()), unit));
		if (!xle3.getText().trim().isEmpty())
			inputListAmount.add(Amount.valueOf(Double.parseDouble(xle3.getText()), unit));
		if (!xle4.getText().trim().isEmpty())
			inputListAmount.add(Amount.valueOf(Double.parseDouble(xle4.getText()), unit));
		if (!xle5.getText().trim().isEmpty())
			inputListAmount.add(Amount.valueOf(Double.parseDouble(xle5.getText()), unit));
		theInputTree.setxLEDistribution(inputListAmount);

		//TWIST
		List<Amount<Angle>> inputListAmountAngle = new ArrayList<>();

		unit = main.recognizeUnit(twistUnits);

		inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(twist1.getText()), unit));
		inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(twist2.getText()), unit));
		if (!twist3.getText().trim().isEmpty())
			inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(twist3.getText()), unit));
		if (!twist4.getText().trim().isEmpty())
			inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(twist4.getText()), unit));
		if (!twist5.getText().trim().isEmpty())
			inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(twist5.getText()), unit));
		theInputTree.setTwistDistribution((inputListAmountAngle));

		//ALPHAZEROLIFT
		inputListAmountAngle = new ArrayList<>();

		unit = main.recognizeUnit(alphaZeroLiftUnits);

		inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(alphaZeroLift1.getText()), unit));
		inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(alphaZeroLift2.getText()), unit));
		if (!alphaZeroLift3.getText().trim().isEmpty())
			inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(alphaZeroLift3.getText()), unit));
		if (!alphaZeroLift4.getText().trim().isEmpty())
			inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(alphaZeroLift4.getText()), unit));
		if (!alphaZeroLift5.getText().trim().isEmpty())
			inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(alphaZeroLift5.getText()), unit));
		theInputTree.setAlphaZeroLiftDistribution((inputListAmountAngle));
		
		//ALPHA STAR
		inputListAmountAngle = new ArrayList<>();

		unit = main.recognizeUnit(alphaStarUnits);

		inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(alphaStar1.getText()), unit));
		inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(alphaStar2.getText()), unit));
		if (!alphaStar3.getText().trim().isEmpty())
			inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(alphaStar3.getText()), unit));
		if (!alphaStar4.getText().trim().isEmpty())
			inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(alphaStar4.getText()), unit));
		if (!alphaStar5.getText().trim().isEmpty())
			inputListAmountAngle.add(Amount.valueOf(Double.parseDouble(alphaStar5.getText()), unit));
		theInputTree.setAlphaStarDistribution((inputListAmountAngle));
		
		
		//CL MAX
		inputList= new ArrayList<>();

		inputList.add(Double.parseDouble(clMax1.getText()));
		inputList.add(Double.parseDouble(clMax2.getText()));
		if (!clMax3.getText().trim().isEmpty())
			inputList.add(Double.parseDouble(clMax3.getText()));
		if (!clMax4.getText().trim().isEmpty())
			inputList.add(Double.parseDouble(clMax4.getText()));
		if (!clMax5.getText().trim().isEmpty())
			inputList.add(Double.parseDouble(clMax5.getText()));	
		theInputTree.setMaximumliftCoefficientDistribution(inputList);
		
		theInputTree.calculateDerivedData();
		Scene graph = D3PlotterClass.createWingDesign(theInputTree);
		graphPane.getChildren().add(graph.getRoot());

		goToAnalysisButton.setDisable(false);
		saveButton.setDisable(false);
		
		main.setTheInputTree(theInputTree);
	}

	@FXML
	public void returnToAnalysis() throws IOException{
		Main.startAnalysis(theInputTree);
		VariablesMainCentralButtons theMainClassButtons = new VariablesMainCentralButtons();
		//theMainClassButtons.enableAnalysisButton();
		
	}
	
	@FXML
	public void writeAllData(InputOutputTree theInputTree) throws IOException{

		this.altitude.setText(Double.toString(theInputTree.getAltitude().doubleValue(
				theInputTree.getAltitude().getUnit())));
		this.altitudeUnits.setValue(theInputTree.getAltitude().getUnit().toString());

		this.machNumber.setText(Double.toString(theInputTree.getMachNumber()));
		
		this.surface.setText(Double.toString(theInputTree.getSurface().doubleValue(
				theInputTree.getSurface().getUnit())));
		this.surfaceUnits.setValue(theInputTree.getSurface().getUnit().toString());
		
		this.aspectRatio.setText(Double.toString(theInputTree.getAspectRatio()));
		
		this.numberOfPoints.setText(Double.toString(theInputTree.getNumberOfPointSemispan()));
		
		this.adimensionalKinkStation.setText(Double.toString(theInputTree.getAdimensionalKinkStation()));
		
		if(theInputTree.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_4_Digit)
			this.airfoilFamily.setValue("NACA_4_DIGIT");	
		if(theInputTree.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_5_Digit)
			this.airfoilFamily.setValue("NACA_5_DIGIT");	
		if(theInputTree.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_63_Series)
			this.airfoilFamily.setValue("NACA_63_SERIES");
		if(theInputTree.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_64_Series)
			this.airfoilFamily.setValue("NACA_64_SERIES");
		if(theInputTree.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_65_Series)
			this.airfoilFamily.setValue("NACA_65_SERIES");
		if(theInputTree.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_66_Series)
			this.airfoilFamily.setValue("NACA_66_SERIES");
		if(theInputTree.getMeanAirfoilFamily() == AirfoilFamilyEnum.BICONVEX)
			this.airfoilFamily.setValue("BICONVEX");
		if(theInputTree.getMeanAirfoilFamily() == AirfoilFamilyEnum.DOUBLE_WEDGE)
			this.airfoilFamily.setValue("DOUBLE_WEDGE");
		
		this.maxThickness.setText(Double.toString(theInputTree.getMeanThickness()));
		
		this.numberOfGivenSections.setValue(theInputTree.getNumberOfSections());
		setNumberOfGivenSection();

		this.adimensionalStations1.setText(theInputTree.getyAdimensionalStationInput().get(0).toString());
		this.adimensionalStations2.setText(theInputTree.getyAdimensionalStationInput().get(1).toString());
		if(theInputTree.getNumberOfSections()==3)
			this.adimensionalStations3.setText(theInputTree.getyAdimensionalStationInput().get(2).toString());
		if(theInputTree.getNumberOfSections()==4){
			this.adimensionalStations3.setText(theInputTree.getyAdimensionalStationInput().get(2).toString());	
			this.adimensionalStations4.setText(theInputTree.getyAdimensionalStationInput().get(3).toString());
		}
		if(theInputTree.getNumberOfSections()==5){
			this.adimensionalStations3.setText(theInputTree.getyAdimensionalStationInput().get(2).toString());	
			this.adimensionalStations4.setText(theInputTree.getyAdimensionalStationInput().get(3).toString());	
			this.adimensionalStations5.setText(theInputTree.getyAdimensionalStationInput().get(4).toString());	
			}
			
		this.chords1.setText(Double.toString(theInputTree.getChordDistribution().get(0).doubleValue(SI.METER)));
		this.chords2.setText(Double.toString(theInputTree.getChordDistribution().get(1).doubleValue(SI.METER)));
		if(theInputTree.getNumberOfSections()==3)
			this.chords3.setText(Double.toString(theInputTree.getChordDistribution().get(2).doubleValue(SI.METER)));
		if(theInputTree.getNumberOfSections()==4){
			this.chords3.setText(Double.toString(theInputTree.getChordDistribution().get(2).doubleValue(SI.METER)));
			this.chords4.setText(Double.toString(theInputTree.getChordDistribution().get(3).doubleValue(SI.METER)));
		}
		if(theInputTree.getNumberOfSections()==5){
			this.chords3.setText(Double.toString(theInputTree.getChordDistribution().get(2).doubleValue(SI.METER)));
			this.chords4.setText(Double.toString(theInputTree.getChordDistribution().get(3).doubleValue(SI.METER)));
			this.chords5.setText(Double.toString(theInputTree.getChordDistribution().get(4).doubleValue(SI.METER)));
			}
		this.chordsUnits.setValue(theInputTree.getChordDistribution().get(0).getUnit().toString());
		
		this.xle1.setText(Double.toString(theInputTree.getxLEDistribution().get(0).doubleValue(SI.METER)));
		this.xle2.setText(Double.toString(theInputTree.getxLEDistribution().get(1).doubleValue(SI.METER)));
		if(theInputTree.getNumberOfSections()==3)
			this.xle3.setText(Double.toString(theInputTree.getxLEDistribution().get(2).doubleValue(SI.METER)));
		if(theInputTree.getNumberOfSections()==4){
			this.xle3.setText(Double.toString(theInputTree.getxLEDistribution().get(2).doubleValue(SI.METER)));
			this.xle4.setText(Double.toString(theInputTree.getxLEDistribution().get(3).doubleValue(SI.METER)));
		}
		if(theInputTree.getNumberOfSections()==5){
			this.xle3.setText(Double.toString(theInputTree.getxLEDistribution().get(2).doubleValue(SI.METER)));
			this.xle4.setText(Double.toString(theInputTree.getxLEDistribution().get(3).doubleValue(SI.METER)));
			this.xle5.setText(Double.toString(theInputTree.getxLEDistribution().get(4).doubleValue(SI.METER)));
			}
		this.xleUnits.setValue(theInputTree.getxLEDistribution().get(0).getUnit().toString());
		
		this.twist1.setText(Double.toString(theInputTree.getTwistDistribution().get(0).doubleValue(NonSI.DEGREE_ANGLE)));
		this.twist2.setText(Double.toString(theInputTree.getTwistDistribution().get(1).doubleValue(NonSI.DEGREE_ANGLE)));
		if(theInputTree.getNumberOfSections()==3)
			this.twist3.setText(Double.toString(theInputTree.getTwistDistribution().get(2).doubleValue(NonSI.DEGREE_ANGLE)));
		if(theInputTree.getNumberOfSections()==4){
			this.twist3.setText(Double.toString(theInputTree.getTwistDistribution().get(2).doubleValue(NonSI.DEGREE_ANGLE)));
			this.twist4.setText(Double.toString(theInputTree.getTwistDistribution().get(3).doubleValue(NonSI.DEGREE_ANGLE)));
		}
		if(theInputTree.getNumberOfSections()==5){
			this.twist3.setText(Double.toString(theInputTree.getTwistDistribution().get(2).doubleValue(NonSI.DEGREE_ANGLE)));
			this.twist4.setText(Double.toString(theInputTree.getTwistDistribution().get(3).doubleValue(NonSI.DEGREE_ANGLE)));
			this.twist5.setText(Double.toString(theInputTree.getTwistDistribution().get(4).doubleValue(NonSI.DEGREE_ANGLE)));
			}
		this.twistUnits.setValue(theInputTree.getTwistDistribution().get(0).getUnit().toString());
		
		
		this.alphaZeroLift1.setText(Double.toString(theInputTree.getAlphaZeroLiftDistribution().get(0).doubleValue(NonSI.DEGREE_ANGLE)));
		this.alphaZeroLift2.setText(Double.toString(theInputTree.getAlphaZeroLiftDistribution().get(1).doubleValue(NonSI.DEGREE_ANGLE)));
		if(theInputTree.getNumberOfSections()==3)
			this.alphaZeroLift3.setText(Double.toString(theInputTree.getAlphaZeroLiftDistribution().get(2).doubleValue(NonSI.DEGREE_ANGLE)));
		if(theInputTree.getNumberOfSections()==4){
			this.alphaZeroLift3.setText(Double.toString(theInputTree.getAlphaZeroLiftDistribution().get(2).doubleValue(NonSI.DEGREE_ANGLE)));
			this.alphaZeroLift4.setText(Double.toString(theInputTree.getAlphaZeroLiftDistribution().get(3).doubleValue(NonSI.DEGREE_ANGLE)));
		}
		if(theInputTree.getNumberOfSections()==5){
			this.alphaZeroLift3.setText(Double.toString(theInputTree.getAlphaZeroLiftDistribution().get(2).doubleValue(NonSI.DEGREE_ANGLE)));
			this.alphaZeroLift4.setText(Double.toString(theInputTree.getAlphaZeroLiftDistribution().get(3).doubleValue(NonSI.DEGREE_ANGLE)));
			this.alphaZeroLift5.setText(Double.toString(theInputTree.getAlphaZeroLiftDistribution().get(4).doubleValue(NonSI.DEGREE_ANGLE)));
			}
		this.alphaZeroLiftUnits.setValue(theInputTree.getAlphaZeroLiftDistribution().get(0).getUnit().toString());
		
		this.alphaStar1.setText(Double.toString(theInputTree.getAlphaStarDistribution().get(0).doubleValue(NonSI.DEGREE_ANGLE)));
		this.alphaStar2.setText(Double.toString(theInputTree.getAlphaStarDistribution().get(1).doubleValue(NonSI.DEGREE_ANGLE)));
		if(theInputTree.getNumberOfSections()==3)
			this.alphaStar3.setText(Double.toString(theInputTree.getAlphaStarDistribution().get(2).doubleValue(NonSI.DEGREE_ANGLE)));
		if(theInputTree.getNumberOfSections()==4){
			this.alphaStar3.setText(Double.toString(theInputTree.getAlphaStarDistribution().get(2).doubleValue(NonSI.DEGREE_ANGLE)));
			this.alphaStar4.setText(Double.toString(theInputTree.getAlphaStarDistribution().get(3).doubleValue(NonSI.DEGREE_ANGLE)));
		}
		if(theInputTree.getNumberOfSections()==5){
			this.alphaStar3.setText(Double.toString(theInputTree.getAlphaStarDistribution().get(2).doubleValue(NonSI.DEGREE_ANGLE)));
			this.alphaStar4.setText(Double.toString(theInputTree.getAlphaStarDistribution().get(3).doubleValue(NonSI.DEGREE_ANGLE)));
			this.alphaStar5.setText(Double.toString(theInputTree.getAlphaStarDistribution().get(4).doubleValue(NonSI.DEGREE_ANGLE)));
			}
		this.alphaStarUnits.setValue(theInputTree.getAlphaStarDistribution().get(0).getUnit().toString());
		 
		this.clMax1.setText(theInputTree.getMaximumliftCoefficientDistribution().get(0).toString());
		this.clMax2.setText(theInputTree.getMaximumliftCoefficientDistribution().get(1).toString());
		if(theInputTree.getNumberOfSections()==3)
			this.clMax3.setText(theInputTree.getMaximumliftCoefficientDistribution().get(2).toString());
		if(theInputTree.getNumberOfSections()==4){
			this.clMax3.setText(theInputTree.getMaximumliftCoefficientDistribution().get(2).toString());	
			this.clMax4.setText(theInputTree.getMaximumliftCoefficientDistribution().get(3).toString());
		}
		if(theInputTree.getNumberOfSections()==5){
			this.clMax3.setText(theInputTree.getMaximumliftCoefficientDistribution().get(2).toString());	
			this.clMax4.setText(theInputTree.getMaximumliftCoefficientDistribution().get(3).toString());	
			this.clMax5.setText(theInputTree.getMaximumliftCoefficientDistribution().get(4).toString());	
			}
		

		
		

	}
	


	@FXML
	public void setNumberOfGivenSection() throws IOException{
		if((int)Double.parseDouble(numberOfGivenSections.getValue().toString()) == 2){
			adimensionalStations3.setDisable(true);
			adimensionalStations4.setDisable(true);
			adimensionalStations5.setDisable(true);

			chords3.setDisable(true);
			chords4.setDisable(true);
			chords5.setDisable(true);

			xle3.setDisable(true);
			xle4.setDisable(true);
			xle5.setDisable(true);

			twist3.setDisable(true);
			twist4.setDisable(true);
			twist5.setDisable(true);

			alphaStar3.setDisable(true);
			alphaStar4.setDisable(true);
			alphaStar5.setDisable(true);

			alphaZeroLift3.setDisable(true);
			alphaZeroLift4.setDisable(true);
			alphaZeroLift5.setDisable(true);

			clMax3.setDisable(true);
			clMax4.setDisable(true);
			clMax5.setDisable(true);

		}
		if((int)Double.parseDouble(numberOfGivenSections.getValue().toString()) == 3){
			adimensionalStations3.setDisable(false);
			adimensionalStations4.setDisable(true);
			adimensionalStations5.setDisable(true);

			chords3.setDisable(false);
			chords4.setDisable(true);
			chords5.setDisable(true);

			xle3.setDisable(false);
			xle4.setDisable(true);
			xle5.setDisable(true);

			twist3.setDisable(false);
			twist4.setDisable(true);
			twist5.setDisable(true);

			alphaStar3.setDisable(false);
			alphaStar4.setDisable(true);
			alphaStar5.setDisable(true);

			alphaZeroLift3.setDisable(false);
			alphaZeroLift4.setDisable(true);
			alphaZeroLift5.setDisable(true);

			clMax3.setDisable(false);
			clMax4.setDisable(true);
			clMax5.setDisable(true);

		}
		if((int)Double.parseDouble(numberOfGivenSections.getValue().toString()) == 4){
			adimensionalStations3.setDisable(false);
			adimensionalStations4.setDisable(false);
			adimensionalStations5.setDisable(true);

			chords3.setDisable(false);
			chords4.setDisable(false);
			chords5.setDisable(true);

			xle3.setDisable(false);
			xle4.setDisable(false);
			xle5.setDisable(true);

			twist3.setDisable(false);
			twist4.setDisable(false);
			twist5.setDisable(true);

			alphaStar3.setDisable(false);
			alphaStar4.setDisable(false);
			alphaStar5.setDisable(true);

			alphaZeroLift3.setDisable(false);
			alphaZeroLift4.setDisable(false);
			alphaZeroLift5.setDisable(true);

			clMax3.setDisable(false);
			clMax4.setDisable(false);
			clMax5.setDisable(true);

		}
		if((int)Double.parseDouble(numberOfGivenSections.getValue().toString()) == 5){

			adimensionalStations3.setDisable(false);
			adimensionalStations4.setDisable(false);
			adimensionalStations5.setDisable(false);

			chords3.setDisable(false);
			chords4.setDisable(false);
			chords5.setDisable(false);

			xle3.setDisable(false);
			xle4.setDisable(false);
			xle5.setDisable(false);

			twist3.setDisable(false);
			twist4.setDisable(false);
			twist5.setDisable(false);

			alphaStar3.setDisable(false);
			alphaStar4.setDisable(false);
			alphaStar5.setDisable(false);

			alphaZeroLift3.setDisable(false);
			alphaZeroLift4.setDisable(false);
			alphaZeroLift5.setDisable(false);

			clMax3.setDisable(false);
			clMax4.setDisable(false);
			clMax5.setDisable(false);

		}
	}

	public TextField getAltitude() {
		return altitude;
	}

	public void setAltitude(TextField altitude) {
		this.altitude = altitude;
	}

	public ChoiceBox getAltitudeUnits() {
		return altitudeUnits;
	}

	public void setAltitudeUnits(ChoiceBox altitudeUnits) {
		this.altitudeUnits = altitudeUnits;
	}

	public Main getMain() {
		return main;
	}

	public File getInputFile() {
		return inputFile;
	}

	public InputOutputTree getTheInputTree() {
		return theInputTree;
	}

	public RadioButton getFromFile() {
		return fromFile;
	}

	public RadioButton getUserDefined() {
		return userDefined;
	}

	public Button getLoad() {
		return load;
	}

	public TextField getFilePath() {
		return filePath;
	}

	public Button getSearchButton() {
		return searchButton;
	}

	public TextField getMachNumber() {
		return machNumber;
	}

	public TextField getAlphaInitial() {
		return alphaInitial;
	}

	public TextField getAlphaFinal() {
		return alphaFinal;
	}

	public TextField getNumberOfAlphas() {
		return numberOfAlphas;
	}

	public TextField getSurface() {
		return surface;
	}

	public TextField getAspectRatio() {
		return aspectRatio;
	}

	public TextField getNumberOfPoints() {
		return numberOfPoints;
	}

	public TextField getAdimensionalKinkStation() {
		return adimensionalKinkStation;
	}

	public ChoiceBox getAirfoilFamily() {
		return airfoilFamily;
	}

	public TextField getMaxThickness() {
		return maxThickness;
	}

	public TextField getAdimensionalStations1() {
		return adimensionalStations1;
	}

	public TextField getAdimensionalStations2() {
		return adimensionalStations2;
	}

	public TextField getAdimensionalStations3() {
		return adimensionalStations3;
	}

	public TextField getAdimensionalStations4() {
		return adimensionalStations4;
	}

	public TextField getAdimensionalStations5() {
		return adimensionalStations5;
	}

	public TextField getChords1() {
		return chords1;
	}

	public TextField getChords2() {
		return chords2;
	}

	public TextField getChords3() {
		return chords3;
	}

	public TextField getChords4() {
		return chords4;
	}

	public TextField getChords5() {
		return chords5;
	}

	public TextField getXle1() {
		return xle1;
	}

	public TextField getXle2() {
		return xle2;
	}

	public TextField getXle3() {
		return xle3;
	}

	public TextField getXle4() {
		return xle4;
	}

	public TextField getXle5() {
		return xle5;
	}

	public TextField getTwist1() {
		return twist1;
	}

	public TextField getTwist2() {
		return twist2;
	}

	public TextField getTwist3() {
		return twist3;
	}

	public TextField getTwist4() {
		return twist4;
	}

	public TextField getTwist5() {
		return twist5;
	}

	public TextField getAlphaZeroLift1() {
		return alphaZeroLift1;
	}

	public TextField getAlphaZeroLift2() {
		return alphaZeroLift2;
	}

	public TextField getAlphaZeroLift3() {
		return alphaZeroLift3;
	}

	public TextField getAlphaZeroLift4() {
		return alphaZeroLift4;
	}

	public TextField getAlphaZeroLift5() {
		return alphaZeroLift5;
	}

	public TextField getAlphaStar1() {
		return alphaStar1;
	}

	public TextField getAlphaStar2() {
		return alphaStar2;
	}

	public TextField getAlphaStar3() {
		return alphaStar3;
	}

	public TextField getAlphaStar4() {
		return alphaStar4;
	}

	public TextField getAlphaStar5() {
		return alphaStar5;
	}

	public TextField getClMax1() {
		return clMax1;
	}

	public TextField getClMax2() {
		return clMax2;
	}

	public TextField getClMax3() {
		return clMax3;
	}

	public TextField getClMax4() {
		return clMax4;
	}

	public TextField getClMax5() {
		return clMax5;
	}

	public ChoiceBox getAlphaInitialUnits() {
		return alphaInitialUnits;
	}

	public ChoiceBox getAlphaFinalUnits() {
		return alphaFinalUnits;
	}

	public ChoiceBox getSurfaceUnits() {
		return surfaceUnits;
	}

	public ChoiceBox getXleUnits() {
		return xleUnits;
	}

	public ChoiceBox getTwistUnits() {
		return twistUnits;
	}

	public ChoiceBox getChordsUnits() {
		return chordsUnits;
	}

	public ChoiceBox getAlphaStarUnits() {
		return alphaStarUnits;
	}

	public ChoiceBox getAlphaZeroLiftUnits() {
		return alphaZeroLiftUnits;
	}

	public ChoiceBox getNumberOfGivenSections() {
		return numberOfGivenSections;
	}

	public ObservableList<String> getAltitudeUnitsList() {
		return altitudeUnitsList;
	}

	public ObservableList<String> getAlphaInitialUnitsList() {
		return alphaInitialUnitsList;
	}

	public ObservableList<String> getAlphaFinalUnitsList() {
		return alphaFinalUnitsList;
	}

	public ObservableList<String> getSurfaceUnitsList() {
		return surfaceUnitsList;
	}

	public ObservableList<String> getXleUnitsList() {
		return xleUnitsList;
	}

	public ObservableList<String> getTwistUnitsList() {
		return twistUnitsList;
	}

	public ObservableList<String> getChordsUnitsList() {
		return chordsUnitsList;
	}

	public ObservableList<String> getAlphaStarUnitsList() {
		return alphaStarUnitsList;
	}

	public ObservableList<String> getAlphaZeroLiftUnitsList() {
		return alphaZeroLiftUnitsList;
	}

	public ObservableList<String> getAifoilList() {
		return aifoilList;
	}

	public ObservableList<String> getNumberOfGivenSectionsList() {
		return numberOfGivenSectionsList;
	}

	public void setMain(Main main) {
		this.main = main;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public void setTheInputTree(InputOutputTree theInputTree) {
		this.theInputTree = theInputTree;
	}

	public void setFromFile(RadioButton fromFile) {
		this.fromFile = fromFile;
	}

	public void setUserDefined(RadioButton userDefined) {
		this.userDefined = userDefined;
	}

	public void setLoad(Button load) {
		this.load = load;
	}

	public void setFilePath(TextField filePath) {
		this.filePath = filePath;
	}

	public void setSearchButton(Button searchButton) {
		this.searchButton = searchButton;
	}

	public void setMachNumber(TextField machNumber) {
		this.machNumber = machNumber;
	}

	public void setAlphaInitial(TextField alphaInitial) {
		this.alphaInitial = alphaInitial;
	}

	public void setAlphaFinal(TextField alphaFinal) {
		this.alphaFinal = alphaFinal;
	}

	public void setNumberOfAlphas(TextField numberOfAlphas) {
		this.numberOfAlphas = numberOfAlphas;
	}

	public void setSurface(TextField surface) {
		this.surface = surface;
	}

	public void setAspectRatio(TextField aspectRatio) {
		this.aspectRatio = aspectRatio;
	}

	public void setNumberOfPoints(TextField numberOfPoints) {
		this.numberOfPoints = numberOfPoints;
	}

	public void setAdimensionalKinkStation(TextField adimensionalKinkStation) {
		this.adimensionalKinkStation = adimensionalKinkStation;
	}

	public void setAirfoilFamily(ChoiceBox airfoilFamily) {
		this.airfoilFamily = airfoilFamily;
	}

	public void setMaxThickness(TextField maxThickness) {
		this.maxThickness = maxThickness;
	}

	public void setAdimensionalStations1(TextField adimensionalStations1) {
		this.adimensionalStations1 = adimensionalStations1;
	}

	public void setAdimensionalStations2(TextField adimensionalStations2) {
		this.adimensionalStations2 = adimensionalStations2;
	}

	public void setAdimensionalStations3(TextField adimensionalStations3) {
		this.adimensionalStations3 = adimensionalStations3;
	}

	public void setAdimensionalStations4(TextField adimensionalStations4) {
		this.adimensionalStations4 = adimensionalStations4;
	}

	public void setAdimensionalStations5(TextField adimensionalStations5) {
		this.adimensionalStations5 = adimensionalStations5;
	}

	public void setChords1(TextField chords1) {
		this.chords1 = chords1;
	}

	public void setChords2(TextField chords2) {
		this.chords2 = chords2;
	}

	public void setChords3(TextField chords3) {
		this.chords3 = chords3;
	}

	public void setChords4(TextField chords4) {
		this.chords4 = chords4;
	}

	public void setChords5(TextField chords5) {
		this.chords5 = chords5;
	}

	public void setXle1(TextField xle1) {
		this.xle1 = xle1;
	}

	public void setXle2(TextField xle2) {
		this.xle2 = xle2;
	}

	public void setXle3(TextField xle3) {
		this.xle3 = xle3;
	}

	public void setXle4(TextField xle4) {
		this.xle4 = xle4;
	}

	public void setXle5(TextField xle5) {
		this.xle5 = xle5;
	}

	public void setTwist1(TextField twist1) {
		this.twist1 = twist1;
	}

	public void setTwist2(TextField twist2) {
		this.twist2 = twist2;
	}

	public void setTwist3(TextField twist3) {
		this.twist3 = twist3;
	}

	public void setTwist4(TextField twist4) {
		this.twist4 = twist4;
	}

	public void setTwist5(TextField twist5) {
		this.twist5 = twist5;
	}

	public void setAlphaZeroLift1(TextField alphaZeroLift1) {
		this.alphaZeroLift1 = alphaZeroLift1;
	}

	public void setAlphaZeroLift2(TextField alphaZeroLift2) {
		this.alphaZeroLift2 = alphaZeroLift2;
	}

	public void setAlphaZeroLift3(TextField alphaZeroLift3) {
		this.alphaZeroLift3 = alphaZeroLift3;
	}

	public void setAlphaZeroLift4(TextField alphaZeroLift4) {
		this.alphaZeroLift4 = alphaZeroLift4;
	}

	public void setAlphaZeroLift5(TextField alphaZeroLift5) {
		this.alphaZeroLift5 = alphaZeroLift5;
	}

	public void setAlphaStar1(TextField alphaStar1) {
		this.alphaStar1 = alphaStar1;
	}

	public void setAlphaStar2(TextField alphaStar2) {
		this.alphaStar2 = alphaStar2;
	}

	public void setAlphaStar3(TextField alphaStar3) {
		this.alphaStar3 = alphaStar3;
	}

	public void setAlphaStar4(TextField alphaStar4) {
		this.alphaStar4 = alphaStar4;
	}

	public void setAlphaStar5(TextField alphaStar5) {
		this.alphaStar5 = alphaStar5;
	}

	public void setClMax1(TextField clMax1) {
		this.clMax1 = clMax1;
	}

	public void setClMax2(TextField clMax2) {
		this.clMax2 = clMax2;
	}

	public void setClMax3(TextField clMax3) {
		this.clMax3 = clMax3;
	}

	public void setClMax4(TextField clMax4) {
		this.clMax4 = clMax4;
	}

	public void setClMax5(TextField clMax5) {
		this.clMax5 = clMax5;
	}

	public void setAlphaInitialUnits(ChoiceBox alphaInitialUnits) {
		this.alphaInitialUnits = alphaInitialUnits;
	}

	public void setAlphaFinalUnits(ChoiceBox alphaFinalUnits) {
		this.alphaFinalUnits = alphaFinalUnits;
	}

	public void setSurfaceUnits(ChoiceBox surfaceUnits) {
		this.surfaceUnits = surfaceUnits;
	}

	public void setXleUnits(ChoiceBox xleUnits) {
		this.xleUnits = xleUnits;
	}

	public void setTwistUnits(ChoiceBox twistUnits) {
		this.twistUnits = twistUnits;
	}

	public void setChordsUnits(ChoiceBox chordsUnits) {
		this.chordsUnits = chordsUnits;
	}

	public void setAlphaStarUnits(ChoiceBox alphaStarUnits) {
		this.alphaStarUnits = alphaStarUnits;
	}

	public void setAlphaZeroLiftUnits(ChoiceBox alphaZeroLiftUnits) {
		this.alphaZeroLiftUnits = alphaZeroLiftUnits;
	}

	public void setNumberOfGivenSections(ChoiceBox numberOfGivenSections) {
		this.numberOfGivenSections = numberOfGivenSections;
	}

	public void setAltitudeUnitsList(ObservableList<String> altitudeUnitsList) {
		this.altitudeUnitsList = altitudeUnitsList;
	}

	public void setAlphaInitialUnitsList(ObservableList<String> alphaInitialUnitsList) {
		this.alphaInitialUnitsList = alphaInitialUnitsList;
	}

	public void setAlphaFinalUnitsList(ObservableList<String> alphaFinalUnitsList) {
		this.alphaFinalUnitsList = alphaFinalUnitsList;
	}

	public void setSurfaceUnitsList(ObservableList<String> surfaceUnitsList) {
		this.surfaceUnitsList = surfaceUnitsList;
	}

	public void setXleUnitsList(ObservableList<String> xleUnitsList) {
		this.xleUnitsList = xleUnitsList;
	}

	public void setTwistUnitsList(ObservableList<String> twistUnitsList) {
		this.twistUnitsList = twistUnitsList;
	}

	public void setChordsUnitsList(ObservableList<String> chordsUnitsList) {
		this.chordsUnitsList = chordsUnitsList;
	}

	public void setAlphaStarUnitsList(ObservableList<String> alphaStarUnitsList) {
		this.alphaStarUnitsList = alphaStarUnitsList;
	}

	public void setAlphaZeroLiftUnitsList(ObservableList<String> alphaZeroLiftUnitsList) {
		this.alphaZeroLiftUnitsList = alphaZeroLiftUnitsList;
	}

	public void setAifoilList(ObservableList<String> aifoilList) {
		this.aifoilList = aifoilList;
	}

	public void setNumberOfGivenSectionsList(ObservableList<String> numberOfGivenSectionsList) {
		this.numberOfGivenSectionsList = numberOfGivenSectionsList;
	}

	public List<TextField> getChordList() {
		return chordList;
	}

	public void setChordList(List<TextField> chordList) {
		this.chordList = chordList;
	}

	public List<TextField> getStationList() {
		return stationList;
	}

	public List<TextField> getXleList() {
		return xleList;
	}

	public List<TextField> getTwistList() {
		return twistList;
	}

	public List<TextField> getAlphaZeroList() {
		return alphaZeroList;
	}

	public List<TextField> getAlphaStarList() {
		return alphaStarList;
	}

	public List<TextField> getClMaxList() {
		return clMaxList;
	}

	public void setStationList(List<TextField> stationList) {
		this.stationList = stationList;
	}

	public void setXleList(List<TextField> xleList) {
		this.xleList = xleList;
	}

	public void setTwistList(List<TextField> twistList) {
		this.twistList = twistList;
	}

	public void setAlphaZeroList(List<TextField> alphaZeroList) {
		this.alphaZeroList = alphaZeroList;
	}

	public void setAlphaStarList(List<TextField> alphaStarList) {
		this.alphaStarList = alphaStarList;
	}

	public void setClMaxList(List<TextField> clMaxList) {
		this.clMaxList = clMaxList;
	}
}
