package jpadcommander.inputmanager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

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
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.WindshieldTypeEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import graphics.ChartCanvas;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javaslang.Tuple;
import javaslang.Tuple2;
import jpadcommander.Main;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;

public class InputManagerController {

	//-------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	//-------------------------------------------------------------------------------------------
	//...........................................................................................
	// LAYOUTS:
	//...........................................................................................
	@FXML
	private SplitPane aircraftViewsAndDataLogSplitPane;
	@FXML
	private SplitPane fuselageViewsAndDataLogSplitPane;
	@FXML
	private Pane aircraftFrontViewPane;
	@FXML
	private Pane aircraftSideViewPane;
	@FXML
	private Pane aircraftTopViewPane;
	@FXML
	private Pane fuselageFrontViewPane;
	@FXML
	private Pane fuselageSideViewPane;
	@FXML
	private Pane fuselageTopViewPane;
	@FXML
	private TextArea textAreaAircraftConsoleOutput;
	@FXML
	private TextArea textAreaFuselageConsoleOutput;
	
	//...........................................................................................
	// BUTTONS:
	//...........................................................................................
	@FXML
	private Button loadAircraftButton;
	@FXML
	private Button newAircraftButton;
	@FXML
	private Button missingSeatRowCabinConfigurationInfoButton;
	@FXML
	private Button referenceMassCabinConfigurationInfoButton;
	
	//...........................................................................................
	// FILE CHOOSER:
	//...........................................................................................
	private FileChooser aircraftFileChooser;
	
	//...........................................................................................
	// OBSERVABLE LISTS:
	//...........................................................................................
	ObservableList<String> aircraftTypeList = FXCollections.observableArrayList(
			"JET",
			"FIGHTER",
			"BUSINESS_JET",
			"TURBOPROP", 
			"GENERAL_AVIATION",
			"COMMUTER",
			"ACROBATIC"
			);
	ObservableList<String> regulationsTypeList = FXCollections.observableArrayList(
			"FAR-23",
			"FAR-25"
			);
	ObservableList<String> windshieldTypeList = FXCollections.observableArrayList(
			"DOUBLE",
			"FLAT_FLUSH",
			"FLAT_PROTRUDING",
			"SINGLE_ROUND",
			"SINGLE_SHARP"
			);
	ObservableList<String> powerPlantMountingPositionTypeList = FXCollections.observableArrayList(
			"BURIED",
			"WING",
			"AFT_FUSELAGE",
			"HTAIL",
			"REAR_FUSELAGE"
			);
	ObservableList<String> nacelleMountingPositionTypeList = FXCollections.observableArrayList(
			"WING",
			"FUSELAGE",
			"HTAIL",
			"UNDERCARRIAGE_HOUSING"
			);
	ObservableList<String> landingGearsMountingPositionTypeList = FXCollections.observableArrayList(
			"FUSELAGE",
			"WING",
			"NACELLE"
			);
	ObservableList<String> fuselageAdjustCriteriaTypeList = FXCollections.observableArrayList(
			"NONE",
			"ADJUST TOTAL LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS",
			"ADJUST TOTAL LENGTH, CONSTANT FINENESS-RATIOS",	
			"ADJUST CYLINDER LENGTH (streching)",                            
			"ADJUST NOSE LENGTH, CONSTANT TOTAL LENGTH AND DIAMETERS",
			"ADJUST NOSE LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS",
			"ADJUST NOSE LENGTH, CONSTANT FINENESS-RATIOS",
			"ADJUST TAILCONE LENGTH, CONSTANT TOTAL LENGTH AND DIAMETERS",
			"ADJUST TAILCONE LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS",
			"ADJUST TAILCONE LENGTH, CONSTANT FINENESS-RATIOS",
			"ADJUST FUSELAGE LENGTH, CONSTANT FINENESS-RATIOS"
			);
	ObservableList<String> wingAdjustCriteriaTypeList = FXCollections.observableArrayList(
			"NONE",
			"FIXED AR, SPAN AND ROOT CHORD",
			"FIXED AR, SPAN AND TIP CHORD",
			"FIXED AR, SPAN AND TAPER-RATIO",
			"FIXED AR, AREA AND ROOT CHORD",
			"FIXED AR, AREA AND TIP CHORD",
			"FIXED AR, AREA AND TAPER-RATIO", 
			"FIXED AR, ROOT CHORD AND TIP CHORD",
			"FIXED AR, ROOT CHORD AND TAPER-RATIO",
			"FIXED AR, TIP CHORD AND TAPER-RATIO",
			"FIXED SPAN, AREA AND ROOT CHORD",
			"FIXED SPAN, AREA AND TIP CHORD",
			"FIXED SPAN, AREA AND TAPER-RATIO",
			"FIXED SPAN, ROOT CHORD AND TIP CHORD", 
			"FIXED SPAN, ROOT CHORD AND TAPER-RATIO",
			"FIXED SPAN, TIP CHORD AND TAPER-RATIO",
			"FIXED AREA, ROOT CHORD AND TIP CHORD",
			"FIXED AREA, ROOT CHORD AND TAPER-RATIO",
			"FIXED AREA, TIP CHORD AND TAPER-RATIO"
			);
	ObservableList<String> cabinConfigurationClassesTypeList = FXCollections.observableArrayList(
			"ECONOMY",
			"BUSINESS",
			"FIRST"
			);
	ObservableList<String> lengthUnitsList = FXCollections.observableArrayList(
			"m",
			"ft"
			);
	ObservableList<String> angleUnitsList = FXCollections.observableArrayList(
			"°",
			"rad" 
			);
	
	//...........................................................................................
	// AIRCRAFT TAB (DATA):
	//...........................................................................................
	// Choice Box
	@FXML
	private ChoiceBox<String> aircraftTypeChoiceBox;
	@FXML
	private ChoiceBox<String> regulationsTypeChoiceBox;
	@FXML
	private ChoiceBox<String> powerPlantMountingPositionTypeChoiceBox1;
	@FXML
	private ChoiceBox<String> powerPlantMountingPositionTypeChoiceBox2;
	@FXML
	private ChoiceBox<String> powerPlantMountingPositionTypeChoiceBox3;
	@FXML
	private ChoiceBox<String> powerPlantMountingPositionTypeChoiceBox4;
	@FXML
	private ChoiceBox<String> powerPlantMountingPositionTypeChoiceBox5;
	@FXML
	private ChoiceBox<String> powerPlantMountingPositionTypeChoiceBox6;
	@FXML
	private ChoiceBox<String> nacellesMountingPositionTypeChoiceBox1;
	@FXML
	private ChoiceBox<String> nacellesMountingPositionTypeChoiceBox2;
	@FXML
	private ChoiceBox<String> nacellesMountingPositionTypeChoiceBox3;
	@FXML
	private ChoiceBox<String> nacellesMountingPositionTypeChoiceBox4;
	@FXML
	private ChoiceBox<String> nacellesMountingPositionTypeChoiceBox5;
	@FXML
	private ChoiceBox<String> nacellesMountingPositionTypeChoiceBox6;
	@FXML
	private ChoiceBox<String> landingGearsMountingPositionTypeChoiceBox;
	
	private List<TextField> textFieldsAircraftEngineFileList = new ArrayList<>();
	private List<TextField> textFieldAircraftEngineXList = new ArrayList<>();
	private List<TextField> textFieldAircraftEngineYList = new ArrayList<>();
	private List<TextField> textFieldAircraftEngineZList = new ArrayList<>();
	private List<ChoiceBox<String>> choiceBoxesAircraftEnginePositonList = new ArrayList<>();
	private List<TextField> textFieldAircraftEngineTiltList = new ArrayList<>();
	
	private List<TextField> textFieldsAircraftNacelleFileList = new ArrayList<>();
	private List<TextField> textFieldAircraftNacelleXList = new ArrayList<>();
	private List<TextField> textFieldAircraftNacelleYList = new ArrayList<>();
	private List<TextField> textFieldAircraftNacelleZList = new ArrayList<>();
	private List<ChoiceBox<String>> choiceBoxesAircraftNacellePositonList = new ArrayList<>();
	
	//...........................................................................................
	// Text fields
	@FXML
	private TextField textFieldAircraftInputFile;
	@FXML
	private TextField textFieldAircraftCabinConfigurationFile;
	@FXML
	private TextField textFieldAircraftFuselageFile;
	@FXML
	private TextField textFieldAircraftFuselageX;
	@FXML
	private TextField textFieldAircraftFuselageY;
	@FXML
	private TextField textFieldAircraftFuselageZ;
	@FXML
	private TextField textFieldAircraftWingFile;
	@FXML
	private TextField textFieldAircraftWingX;
	@FXML
	private TextField textFieldAircraftWingY;
	@FXML
	private TextField textFieldAircraftWingZ;
	@FXML
	private TextField textFieldAircraftWingRiggingAngle;
	@FXML
	private TextField textFieldAircraftHTailFile;
	@FXML
	private TextField textFieldAircraftHTailX;
	@FXML
	private TextField textFieldAircraftHTailY;
	@FXML
	private TextField textFieldAircraftHTailZ;
	@FXML
	private TextField textFieldAircraftHTailRiggingAngle;
	@FXML
	private TextField textFieldAircraftVTailFile;
	@FXML
	private TextField textFieldAircraftVTailX;
	@FXML
	private TextField textFieldAircraftVTailY;
	@FXML
	private TextField textFieldAircraftVTailZ;
	@FXML
	private TextField textFieldAircraftVTailRiggingAngle;
	@FXML
	private TextField textFieldAircraftCanardFile;
	@FXML
	private TextField textFieldAircraftCanardX;
	@FXML
	private TextField textFieldAircraftCanardY;
	@FXML
	private TextField textFieldAircraftCanardZ;
	@FXML
	private TextField textFieldAircraftCanardRiggingAngle;
	@FXML
	private TextField textFieldAircraftEngineFile1;
	@FXML
	private TextField textFieldAircraftEngineFile2;
	@FXML
	private TextField textFieldAircraftEngineFile3;
	@FXML
	private TextField textFieldAircraftEngineFile4;
	@FXML
	private TextField textFieldAircraftEngineFile5;
	@FXML
	private TextField textFieldAircraftEngineFile6;
	@FXML
	private TextField textFieldAircraftEngineX1;
	@FXML
	private TextField textFieldAircraftEngineX2;
	@FXML
	private TextField textFieldAircraftEngineX3;
	@FXML
	private TextField textFieldAircraftEngineX4;
	@FXML
	private TextField textFieldAircraftEngineX5;
	@FXML
	private TextField textFieldAircraftEngineX6;
	@FXML
	private TextField textFieldAircraftEngineY1;
	@FXML
	private TextField textFieldAircraftEngineY2;
	@FXML
	private TextField textFieldAircraftEngineY3;
	@FXML
	private TextField textFieldAircraftEngineY4;
	@FXML
	private TextField textFieldAircraftEngineY5;
	@FXML
	private TextField textFieldAircraftEngineY6;
	@FXML
	private TextField textFieldAircraftEngineZ1;
	@FXML
	private TextField textFieldAircraftEngineZ2;
	@FXML
	private TextField textFieldAircraftEngineZ3;
	@FXML
	private TextField textFieldAircraftEngineZ4;
	@FXML
	private TextField textFieldAircraftEngineZ5;
	@FXML
	private TextField textFieldAircraftEngineZ6;
	@FXML
	private TextField textFieldAircraftEngineTilt1;
	@FXML
	private TextField textFieldAircraftEngineTilt2;
	@FXML
	private TextField textFieldAircraftEngineTilt3;
	@FXML
	private TextField textFieldAircraftEngineTilt4;
	@FXML
	private TextField textFieldAircraftEngineTilt5;
	@FXML
	private TextField textFieldAircraftEngineTilt6;
	@FXML
	private TextField textFieldAircraftNacelleFile1;
	@FXML
	private TextField textFieldAircraftNacelleFile2;
	@FXML
	private TextField textFieldAircraftNacelleFile3;
	@FXML
	private TextField textFieldAircraftNacelleFile4;
	@FXML
	private TextField textFieldAircraftNacelleFile5;
	@FXML
	private TextField textFieldAircraftNacelleFile6;
	@FXML
	private TextField textFieldAircraftNacelleX1;
	@FXML
	private TextField textFieldAircraftNacelleX2;
	@FXML
	private TextField textFieldAircraftNacelleX3;
	@FXML
	private TextField textFieldAircraftNacelleX4;
	@FXML
	private TextField textFieldAircraftNacelleX5;
	@FXML
	private TextField textFieldAircraftNacelleX6;
	@FXML
	private TextField textFieldAircraftNacelleY1;
	@FXML
	private TextField textFieldAircraftNacelleY2;
	@FXML
	private TextField textFieldAircraftNacelleY3;
	@FXML
	private TextField textFieldAircraftNacelleY4;
	@FXML
	private TextField textFieldAircraftNacelleY5;
	@FXML
	private TextField textFieldAircraftNacelleY6;
	@FXML
	private TextField textFieldAircraftNacelleZ1;
	@FXML
	private TextField textFieldAircraftNacelleZ2;
	@FXML
	private TextField textFieldAircraftNacelleZ3;
	@FXML
	private TextField textFieldAircraftNacelleZ4;
	@FXML
	private TextField textFieldAircraftNacelleZ5;
	@FXML
	private TextField textFieldAircraftNacelleZ6;
	@FXML
	private TextField textFieldAircraftLandingGearsFile;
	@FXML
	private TextField textFieldAircraftLandingGearsX;
	@FXML
	private TextField textFieldAircraftLandingGearsY;
	@FXML
	private TextField textFieldAircraftLandingGearsZ;
	@FXML
	private TextField textFieldAircraftSystemsFile;
	@FXML
	private TextField textFieldAircraftSystemsX;
	@FXML
	private TextField textFieldAircraftSystemsY;
	@FXML
	private TextField textFieldAircraftSystemsZ;
	
	//...........................................................................................
	// AIRCRAFT TAB (UNITS):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> fuselageXUnitChoiceBox;
	@FXML
	private ChoiceBox<String> fuselageYUnitChoiceBox;
	@FXML
	private ChoiceBox<String> fuselageZUnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingXUnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingYUnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingZUnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingRiggingAngleUnitChoiceBox;
	@FXML
	private ChoiceBox<String> hTailXUnitChoiceBox;
	@FXML
	private ChoiceBox<String> hTailYUnitChoiceBox;
	@FXML
	private ChoiceBox<String> htailZUnitChoiceBox;
	@FXML
	private ChoiceBox<String> hTailRiggingAngleUnitChoiceBox;
	@FXML
	private ChoiceBox<String> vTailXUnitChoiceBox;
	@FXML
	private ChoiceBox<String> vTailYUnitChoiceBox;
	@FXML
	private ChoiceBox<String> vTailZUnitChoiceBox;
	@FXML
	private ChoiceBox<String> vTailRiggingAngleUnitChoiceBox;
	@FXML
	private ChoiceBox<String> canardXUnitChoiceBox;
	@FXML
	private ChoiceBox<String> canardYUnitChoiceBox;
	@FXML
	private ChoiceBox<String> canardZUnitChoiceBox;
	@FXML
	private ChoiceBox<String> canardRiggingAngleUnitChoiceBox;
	@FXML
	private ChoiceBox<String> powerPlantXUnitChoiceBox;
	@FXML
	private ChoiceBox<String> powerPlantYUnitChoiceBox;
	@FXML
	private ChoiceBox<String> powerPlantZUnitChoiceBox;
	@FXML
	private ChoiceBox<String> powerPlantTiltAngleUnitChoiceBox;
	@FXML
	private ChoiceBox<String> nacelleXUnitChoiceBox;
	@FXML
	private ChoiceBox<String> nacelleYUnitChoiceBox;
	@FXML
	private ChoiceBox<String> nacelleZUnitChoiceBox;
	@FXML
	private ChoiceBox<String> landingGearsXUnitChoiceBox;
	@FXML
	private ChoiceBox<String> landingGearsYUnitChoiceBox;
	@FXML
	private ChoiceBox<String> landingGearsZUnitChoiceBox;
	@FXML
	private ChoiceBox<String> systemsXUnitChoiceBox;
	@FXML
	private ChoiceBox<String> systemsYUnitChoiceBox;
	@FXML
	private ChoiceBox<String> systemsZUnitChoiceBox;
	
	//...........................................................................................
	// FUSELAGE TAB (DATA):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> windshieldTypeChoiceBox;
	@FXML
	private ChoiceBox<String> fuselageAdjustCriterionChoiceBox;
	@FXML
	private CheckBox fuselagePressurizedCheckBox;
	@FXML
	private TextField textFieldFuselageDeckNumber;
	@FXML
	private TextField textFieldFuselageLength;
	@FXML
	private TextField textFieldFuselageSurfaceRoughness;
	@FXML
	private TextField textFieldFuselageNoseLengthRatio;
	@FXML
	private TextField textFieldFuselageNoseTipOffset;
	@FXML
	private TextField textFieldFuselageNoseDxCap;
	@FXML
	private TextField textFieldFuselageNoseWindshieldWidth;
	@FXML
	private TextField textFieldFuselageNoseWindshieldHeight;
	@FXML
	private TextField textFieldFuselageNoseMidSectionHeight;
	@FXML
	private TextField textFieldFuselageNoseMidSectionRhoUpper;
	@FXML
	private TextField textFieldFuselageNoseMidSectionRhoLower;
	@FXML
	private TextField textFieldFuselageCylinderLengthRatio;
	@FXML
	private TextField textFieldFuselageCylinderSectionWidth;
	@FXML
	private TextField textFieldFuselageCylinderSectionHeight;
	@FXML
	private TextField textFieldFuselageCylinderHeightFromGround;
	@FXML
	private TextField textFieldFuselageCylinderSectionHeightRatio;
	@FXML
	private TextField textFieldFuselageCylinderSectionRhoUpper;
	@FXML
	private TextField textFieldFuselageCylinderSectionRhoLower;
	@FXML
	private TextField textFieldFuselageTailTipOffset;
	@FXML
	private TextField textFieldFuselageTailDxCap;
	@FXML
	private TextField textFieldFuselageTailMidSectionHeight;
	@FXML
	private TextField textFieldFuselageTailMidRhoUpper;
	@FXML
	private TextField textFieldFuselageTailMidRhoLower;
	@FXML
	private TextField textFieldFuselageSpoilerXin1;
	@FXML
	private TextField textFieldFuselageSpoilerXin2;
	@FXML
	private TextField textFieldFuselageSpoilerXin3;
	@FXML
	private TextField textFieldFuselageSpoilerXin4;
	@FXML
	private TextField textFieldFuselageSpoilerXout1;
	@FXML
	private TextField textFieldFuselageSpoilerXout2;
	@FXML
	private TextField textFieldFuselageSpoilerXout3;
	@FXML
	private TextField textFieldFuselageSpoilerXout4;
	@FXML
	private TextField textFieldFuselageSpoilerYin1;
	@FXML
	private TextField textFieldFuselageSpoilerYin2;
	@FXML
	private TextField textFieldFuselageSpoilerYin3;
	@FXML
	private TextField textFieldFuselageSpoilerYin4;
	@FXML
	private TextField textFieldFuselageSpoilerYout1;
	@FXML
	private TextField textFieldFuselageSpoilerYout2;
	@FXML
	private TextField textFieldFuselageSpoilerYout3;
	@FXML
	private TextField textFieldFuselageSpoilerYout4;
	@FXML
	private TextField textFieldFuselageSpoilerMinDeflection1;
	@FXML
	private TextField textFieldFuselageSpoilerMinDeflection2;
	@FXML
	private TextField textFieldFuselageSpoilerMinDeflection3;
	@FXML
	private TextField textFieldFuselageSpoilerMinDeflection4;
	@FXML
	private TextField textFieldFuselageSpoilerMaxDeflection1;
	@FXML
	private TextField textFieldFuselageSpoilerMaxDeflection2;
	@FXML
	private TextField textFieldFuselageSpoilerMaxDeflection3;
	@FXML
	private TextField textFieldFuselageSpoilerMaxDeflection4;
	
	private List<TextField> textFieldSpoilersXInboradList = new ArrayList<>();
	private List<TextField> textFieldSpoilersXOutboradList = new ArrayList<>();
	private List<TextField> textFieldSpoilersYInboradList = new ArrayList<>();
	private List<TextField> textFieldSpoilersYOutboradList = new ArrayList<>();
	private List<TextField> textFieldSpoilersMinimumDeflectionList = new ArrayList<>();
	private List<TextField> textFieldSpoilersMaximumDeflectionList = new ArrayList<>();
	
	//...........................................................................................
	// FUSELAGE TAB (UNITS):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> fuselageLengthUnitChoiceBox;
	@FXML
	private ChoiceBox<String> fuselageRoughnessUnitChoiceBox;
	@FXML
	private ChoiceBox<String> fuselageNoseTipOffsetZUnitChoiceBox;
	@FXML
	private ChoiceBox<String> fuselageWindshieldWidthUnitChoiceBox;
	@FXML
	private ChoiceBox<String> fuselageWindshieldHeightUnitChoiceBox;
	@FXML
	private ChoiceBox<String> fuselageCylinderSectionWidthUnitChoiceBox;
	@FXML
	private ChoiceBox<String> fuselageCylinderSectionHeightUnitChoiceBox;
	@FXML
	private ChoiceBox<String> fuselageHeightFromGroundUnitChoiceBox;
	@FXML
	private ChoiceBox<String> fuselageTailTipOffsetZUnitChoiceBox;
	@FXML
	private ChoiceBox<String> fuselageSpoilersDeltaMinUnitChoiceBox;
	@FXML
	private ChoiceBox<String> fuselageSpoilersDeltaMaxUnitChoiceBox;
	
	//...........................................................................................
	// CABIN CONFIGURATION TAB (DATA):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> cabinConfigurationClassesChoiceBox;
	
	//...........................................................................................
	// WING TAB (DATA):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> wingAdjustCriterionChoiceBox;
	
	// TODO: COMPLETE ME !!
	
	//...........................................................................................
	// WING TAB (UNITS):
	//...........................................................................................
	
	// TODO: COMPLETE ME !!
	
	
	//-------------------------------------------------------------------------------------------
	// METHODS
	//-------------------------------------------------------------------------------------------
	@FXML
	private void initialize() {
		
		ObjectProperty<Aircraft> aircraft = new SimpleObjectProperty<>();

		try {
			aircraft.set(Main.getTheAircraft());
			newAircraftButton.disableProperty().bind(
					Bindings.isNull(aircraft)
					);
		} catch (Exception e) {
			newAircraftButton.setDisable(true);
		}
		
		aircraftLoadButtonDisableCheck();
		
		//.......................................................................................
		// CHOICE BOX INITIALIZATION
		aircraftTypeChoiceBox.setItems(aircraftTypeList);
		regulationsTypeChoiceBox.setItems(regulationsTypeList);
		windshieldTypeChoiceBox.setItems(windshieldTypeList);
		powerPlantMountingPositionTypeChoiceBox1.setItems(powerPlantMountingPositionTypeList);
		powerPlantMountingPositionTypeChoiceBox2.setItems(powerPlantMountingPositionTypeList);
		powerPlantMountingPositionTypeChoiceBox3.setItems(powerPlantMountingPositionTypeList);
		powerPlantMountingPositionTypeChoiceBox4.setItems(powerPlantMountingPositionTypeList);
		powerPlantMountingPositionTypeChoiceBox5.setItems(powerPlantMountingPositionTypeList);
		powerPlantMountingPositionTypeChoiceBox6.setItems(powerPlantMountingPositionTypeList);
		nacellesMountingPositionTypeChoiceBox1.setItems(nacelleMountingPositionTypeList);
		nacellesMountingPositionTypeChoiceBox2.setItems(nacelleMountingPositionTypeList);
		nacellesMountingPositionTypeChoiceBox3.setItems(nacelleMountingPositionTypeList);
		nacellesMountingPositionTypeChoiceBox4.setItems(nacelleMountingPositionTypeList);
		nacellesMountingPositionTypeChoiceBox5.setItems(nacelleMountingPositionTypeList);
		nacellesMountingPositionTypeChoiceBox6.setItems(nacelleMountingPositionTypeList);
		landingGearsMountingPositionTypeChoiceBox.setItems(landingGearsMountingPositionTypeList);
		fuselageAdjustCriterionChoiceBox.setItems(fuselageAdjustCriteriaTypeList);
//		cabinConfigurationClassesChoiceBox.setItems(cabinConfigurationClassesTypeList);
//		wingAdjustCriterionChoiceBox.setItems(wingAdjustCriteriaTypeList);
		
		// Units 
		fuselageXUnitChoiceBox.setItems(lengthUnitsList);
		fuselageYUnitChoiceBox.setItems(lengthUnitsList);
		fuselageZUnitChoiceBox.setItems(lengthUnitsList);
		wingXUnitChoiceBox.setItems(lengthUnitsList);
		wingYUnitChoiceBox.setItems(lengthUnitsList);
		wingZUnitChoiceBox.setItems(lengthUnitsList);
		wingRiggingAngleUnitChoiceBox.setItems(angleUnitsList);
		hTailXUnitChoiceBox.setItems(lengthUnitsList);
		hTailYUnitChoiceBox.setItems(lengthUnitsList);
		htailZUnitChoiceBox.setItems(lengthUnitsList);
		hTailRiggingAngleUnitChoiceBox.setItems(angleUnitsList);
		vTailXUnitChoiceBox.setItems(lengthUnitsList);
		vTailYUnitChoiceBox.setItems(lengthUnitsList);
		vTailZUnitChoiceBox.setItems(lengthUnitsList);
		vTailRiggingAngleUnitChoiceBox.setItems(angleUnitsList);
		canardXUnitChoiceBox.setItems(lengthUnitsList);
		canardYUnitChoiceBox.setItems(lengthUnitsList);
		canardZUnitChoiceBox.setItems(lengthUnitsList);
		canardRiggingAngleUnitChoiceBox.setItems(angleUnitsList);
		powerPlantXUnitChoiceBox.setItems(lengthUnitsList);
		powerPlantYUnitChoiceBox.setItems(lengthUnitsList);
		powerPlantZUnitChoiceBox.setItems(lengthUnitsList);
		powerPlantTiltAngleUnitChoiceBox.setItems(angleUnitsList);
		nacelleXUnitChoiceBox.setItems(lengthUnitsList);
		nacelleYUnitChoiceBox.setItems(lengthUnitsList);
		nacelleZUnitChoiceBox.setItems(lengthUnitsList);
		landingGearsXUnitChoiceBox.setItems(lengthUnitsList);
		landingGearsYUnitChoiceBox.setItems(lengthUnitsList);
		landingGearsZUnitChoiceBox.setItems(lengthUnitsList);
		systemsXUnitChoiceBox.setItems(lengthUnitsList);
		systemsYUnitChoiceBox.setItems(lengthUnitsList);
		systemsZUnitChoiceBox.setItems(lengthUnitsList);
		fuselageLengthUnitChoiceBox.setItems(lengthUnitsList);
		fuselageRoughnessUnitChoiceBox.setItems(lengthUnitsList);
		fuselageNoseTipOffsetZUnitChoiceBox.setItems(lengthUnitsList);
		fuselageWindshieldWidthUnitChoiceBox.setItems(lengthUnitsList);
		fuselageWindshieldHeightUnitChoiceBox.setItems(lengthUnitsList);
		fuselageCylinderSectionWidthUnitChoiceBox.setItems(lengthUnitsList);
		fuselageCylinderSectionHeightUnitChoiceBox.setItems(lengthUnitsList);
		fuselageHeightFromGroundUnitChoiceBox.setItems(lengthUnitsList);
		fuselageTailTipOffsetZUnitChoiceBox.setItems(lengthUnitsList);
		fuselageSpoilersDeltaMinUnitChoiceBox.setItems(angleUnitsList);
		fuselageSpoilersDeltaMaxUnitChoiceBox.setItems(angleUnitsList);

	}
	
	private void aircraftLoadButtonDisableCheck () {
		
		//.......................................................................................
		// CHECK IF THE AIRCRAFT FILE TEXT FIELD IS NOT EMPTY
		loadAircraftButton.disableProperty().bind(
				Bindings.isEmpty(textFieldAircraftInputFile.textProperty())
				);
		
		// CHECK IF THE FILE IN TEXTFIELD IS AN AIRCRAFT
        final Tooltip warning = new Tooltip("WARNING : The selected file is not an aircraft !!");
        loadAircraftButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
        	
        	@Override
        	public void handle(MouseEvent event) {
        		Point2D p = loadAircraftButton
        				.localToScreen(
        						-2.5*loadAircraftButton.getLayoutBounds().getMaxX(),
        						1.2*loadAircraftButton.getLayoutBounds().getMaxY()
        						);
        		if(!isAircraftFile(textFieldAircraftInputFile.getText())
        				) {
        			warning.show(loadAircraftButton, p.getX(), p.getY());
        		}
        	}
        });
        loadAircraftButton.setOnMouseExited(new EventHandler<MouseEvent>() {
        	
        	@Override
        	public void handle(MouseEvent event) {
        		warning.hide();
        	}
        });
	}
	
	private boolean isAircraftFile(String pathToAircraftXML) {

		boolean isAircraftFile = false;
		
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
		System.setOut(filterStream);

		if(pathToAircraftXML.endsWith(".xml")) {
			File inputFile = new File(pathToAircraftXML);
			if(inputFile.exists()) {
				JPADXmlReader reader = new JPADXmlReader(pathToAircraftXML);
				if(reader.getXmlDoc().getElementsByTagName("aircraft").getLength() > 0)
					isAircraftFile = true;
			}
		}
		// write again
		System.setOut(originalOut);
		
		return isAircraftFile;
	}
	
	@FXML
	private void newAircraft() throws IOException {
		
		//..................................................................................
		// INPUT DATA WARNING
		Stage inputDataWarning = new Stage();
		
		inputDataWarning.setTitle("New Aircraft Warning");
		inputDataWarning.initModality(Modality.WINDOW_MODAL);
		inputDataWarning.initOwner(Main.getPrimaryStage());

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("inputmanager/InputManagerWarning.fxml"));
		BorderPane inputDataWarningBorderPane = loader.load();
		
		Scene scene = new Scene(inputDataWarningBorderPane);
        inputDataWarning.setScene(scene);
        inputDataWarning.sizeToScene();
        inputDataWarning.show();

        Button yesButton = (Button) inputDataWarningBorderPane.lookup("#warningYesButton");
        yesButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				newAircraftImplementation();
				inputDataWarning.close();
			}
        	
		});
        Button noButton = (Button) inputDataWarningBorderPane.lookup("#warningNoButton");
        noButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				 inputDataWarning.close();
			}
        	
		});
        
	}
	
	private void newAircraftImplementation() {
		
		//..................................................................................
		// PRELIMINARY OPERATIONS
		Main.getStatusBar().setText("Ready!");
		Main.getProgressBar().setProgress(0.0);

		textFieldAircraftInputFile.clear();
		
		//..................................................................................
		// AIRCRAFT
		aircraftTypeChoiceBox.getSelectionModel().clearSelection();
		regulationsTypeChoiceBox.getSelectionModel().clearSelection();
		
		// cabin configuration
		textFieldAircraftCabinConfigurationFile.clear();
		
		// fuselage
		textFieldAircraftFuselageFile.clear();
		textFieldAircraftFuselageX.clear();
		fuselageXUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftFuselageY.clear();
		fuselageYUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftFuselageZ.clear();
		fuselageZUnitChoiceBox.getSelectionModel().clearSelection();
		
		// wing
		textFieldAircraftWingFile.clear();
		textFieldAircraftWingX.clear();
		wingXUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftWingY.clear();
		wingYUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftWingZ.clear();
		wingZUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftWingRiggingAngle.clear();
		wingRiggingAngleUnitChoiceBox.getSelectionModel().clearSelection();
		
		// hTail
		textFieldAircraftHTailFile.clear();
		textFieldAircraftHTailX.clear();
		hTailXUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftHTailY.clear();
		hTailYUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftHTailZ.clear();
		htailZUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftHTailRiggingAngle.clear();
		hTailRiggingAngleUnitChoiceBox.getSelectionModel().clearSelection();
		
		// vTail
		textFieldAircraftVTailFile.clear();
		textFieldAircraftVTailX.clear();
		vTailXUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftVTailY.clear();
		vTailYUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftVTailZ.clear();
		vTailZUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftVTailRiggingAngle.clear();
		vTailRiggingAngleUnitChoiceBox.getSelectionModel().clearSelection();
		
		// canard
		textFieldAircraftCanardFile.clear();
		textFieldAircraftCanardX.clear();
		canardXUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftCanardY.clear();
		canardYUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftCanardZ.clear();
		canardZUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftCanardRiggingAngle.clear();
		canardRiggingAngleUnitChoiceBox.getSelectionModel().clearSelection();
		
		// Power Plant
		powerPlantXUnitChoiceBox.getSelectionModel().clearSelection();
		powerPlantYUnitChoiceBox.getSelectionModel().clearSelection();
		powerPlantZUnitChoiceBox.getSelectionModel().clearSelection();
		powerPlantTiltAngleUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldsAircraftEngineFileList.stream().forEach(t -> t.clear());
		textFieldAircraftEngineXList.stream().forEach(t -> t.clear());
		textFieldAircraftEngineYList.stream().forEach(t -> t.clear());
		textFieldAircraftEngineZList.stream().forEach(t -> t.clear());
		choiceBoxesAircraftEnginePositonList.stream().forEach(ep -> ep.getSelectionModel().clearSelection());
		textFieldAircraftEngineTiltList.stream().forEach(t -> t.clear());
		
		// Nacelle
		nacelleXUnitChoiceBox.getSelectionModel().clearSelection();
		nacelleYUnitChoiceBox.getSelectionModel().clearSelection();
		nacelleZUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldsAircraftNacelleFileList.stream().forEach(t -> t.clear());
		textFieldAircraftNacelleXList.stream().forEach(t -> t.clear());
		textFieldAircraftNacelleYList.stream().forEach(t -> t.clear());
		textFieldAircraftNacelleZList.stream().forEach(t -> t.clear());
		choiceBoxesAircraftNacellePositonList.stream().forEach(ep -> ep.getSelectionModel().clearSelection());
		
		// Landing Gears
		textFieldAircraftLandingGearsFile.clear();
		textFieldAircraftLandingGearsX.clear();
		landingGearsXUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftLandingGearsY.clear();
		landingGearsYUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftLandingGearsZ.clear();
		landingGearsZUnitChoiceBox.getSelectionModel().clearSelection();
		landingGearsMountingPositionTypeChoiceBox.getSelectionModel().clearSelection();
		
		// Systems
		textFieldAircraftSystemsFile.clear();
		textFieldAircraftSystemsX.clear();
		systemsXUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftSystemsY.clear();
		systemsYUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldAircraftSystemsZ.clear();
		systemsZUnitChoiceBox.getSelectionModel().clearSelection();
		
		// 3 View and TextArea
		textAreaAircraftConsoleOutput.clear();
		aircraftTopViewPane.getChildren().clear();
		aircraftSideViewPane.getChildren().clear();
		aircraftFrontViewPane.getChildren().clear();
		
		//..................................................................................
		// FUSELAGE
		fuselageAdjustCriterionChoiceBox.getSelectionModel().clearSelection();
		fuselageAdjustCriterionChoiceBox.setDisable(true);
		
		// Pressurized
		fuselagePressurizedCheckBox.setSelected(false);
		
		// Global Data
		textFieldFuselageDeckNumber.clear();
		textFieldFuselageLength.clear();
		fuselageLengthUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldFuselageSurfaceRoughness.clear();
		fuselageRoughnessUnitChoiceBox.getSelectionModel().clearSelection();
		
		// Nose Trunk
		textFieldFuselageNoseLengthRatio.clear();
		textFieldFuselageNoseTipOffset.clear();
		fuselageNoseTipOffsetZUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldFuselageNoseDxCap.clear();
		windshieldTypeChoiceBox.getSelectionModel().clearSelection();
		textFieldFuselageNoseWindshieldHeight.clear();
		fuselageWindshieldHeightUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldFuselageNoseWindshieldWidth.clear();
		fuselageWindshieldWidthUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldFuselageNoseMidSectionHeight.clear();
		textFieldFuselageNoseMidSectionRhoUpper.clear();
		textFieldFuselageNoseMidSectionRhoLower.clear();

		// Cylindrical Trunk
		textFieldFuselageCylinderLengthRatio.clear();
		textFieldFuselageCylinderSectionWidth.clear();
		fuselageCylinderSectionWidthUnitChoiceBox.getSelectionModel().clearSelection();		
		textFieldFuselageCylinderSectionHeight.clear();
		fuselageCylinderSectionHeightUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldFuselageCylinderHeightFromGround.clear();
		fuselageHeightFromGroundUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldFuselageCylinderSectionHeightRatio.clear();
		textFieldFuselageCylinderSectionRhoUpper.clear();
		textFieldFuselageCylinderSectionRhoLower.clear();
		
		// Tail Trunk
		textFieldFuselageTailTipOffset.clear();
		fuselageTailTipOffsetZUnitChoiceBox.getSelectionModel().clearSelection();
		textFieldFuselageTailDxCap.clear();
		textFieldFuselageTailMidSectionHeight.clear();
		textFieldFuselageTailMidRhoUpper.clear();
		textFieldFuselageTailMidRhoLower.clear();
		
		// Spoilers
		if(!Main.getTheAircraft().getFuselage().getSpoilers().isEmpty()) {
			fuselageSpoilersDeltaMaxUnitChoiceBox.getSelectionModel().clearSelection();
			fuselageSpoilersDeltaMinUnitChoiceBox.getSelectionModel().clearSelection();	
			textFieldSpoilersXInboradList.stream().forEach(t -> t.clear());
			textFieldSpoilersXOutboradList.stream().forEach(t -> t.clear());
			textFieldSpoilersYInboradList.stream().forEach(t -> t.clear());
			textFieldSpoilersYOutboradList.stream().forEach(t -> t.clear());
			textFieldSpoilersMaximumDeflectionList.stream().forEach(t -> t.clear());
			textFieldSpoilersMinimumDeflectionList.stream().forEach(t -> t.clear());
		}
		
		// 3 View and TextArea
		textAreaFuselageConsoleOutput.clear();
		fuselageTopViewPane.getChildren().clear();
		fuselageSideViewPane.getChildren().clear();
		fuselageFrontViewPane.getChildren().clear();
		
		// TODO: CONTINUE WITH WING, etc ...
		
		Main.setTheAircraft(null);

		ObjectProperty<Aircraft> aircraft = new SimpleObjectProperty<>();

		try {
			aircraft.set(Main.getTheAircraft());
			newAircraftButton.disableProperty().bind(
					Bindings.isNull(aircraft)
					);
		} catch (Exception e) {
			newAircraftButton.setDisable(true);
		}

	}
	
	@FXML
	private void chooseAircraftFile() throws IOException {

		aircraftFileChooser = new FileChooser();
		aircraftFileChooser.setTitle("Open File");
		aircraftFileChooser.setInitialDirectory(new File(Main.getInputDirectoryPath()));
		File file = aircraftFileChooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			textFieldAircraftInputFile.setText(file.getAbsolutePath());
			Main.setInputFileAbsolutePath(file.getAbsolutePath());
		}
	}
	
	@FXML
	private void loadAircraftFile() throws IOException, InterruptedException {
	
		if(isAircraftFile(textFieldAircraftInputFile.getText()))
			try {
				loadAircraftFileImplementation();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
	}

	private void loadAircraftFileImplementation() throws IOException, InterruptedException {
		
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
		System.setOut(filterStream);
		
		MyConfiguration.setDir(FoldersEnum.DATABASE_DIR, Main.getDatabaseDirectoryPath());
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		String highLiftDatabaseFileName = "HighLiftDatabase.h5";
		String fusDesDatabaseFilename = "FusDes_database.h5";
		String vedscDatabaseFilename = "VeDSC_database.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = DatabaseManager.initializeAeroDatabase(
				new AerodynamicDatabaseReader(
						databaseFolderPath,	aerodynamicDatabaseFileName
						),
				databaseFolderPath
				);
		HighLiftDatabaseReader highLiftDatabaseReader = DatabaseManager.initializeHighLiftDatabase(
				new HighLiftDatabaseReader(
						databaseFolderPath,	highLiftDatabaseFileName
						),
				databaseFolderPath
				);
		FusDesDatabaseReader fusDesDatabaseReader = DatabaseManager.initializeFusDes(
				new FusDesDatabaseReader(
						databaseFolderPath,	fusDesDatabaseFilename
						),
				databaseFolderPath
				);
		VeDSCDatabaseReader veDSCDatabaseReader = DatabaseManager.initializeVeDSC(
				new VeDSCDatabaseReader(
						databaseFolderPath,	vedscDatabaseFilename
						),
				databaseFolderPath
				);
		Main.getProgressBar().setProgress(0.1);
		Main.getStatusBar().setText("Reading Database...");
		
		String dirLiftingSurfaces = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces";
		String dirFuselages = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "fuselages";
		String dirEngines = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "engines";
		String dirNacelles = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "nacelles";
		String dirLandingGears = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "landing_gears";
		String dirSystems = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "systems";
		String dirCabinConfiguration = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "cabin_configurations";
		String dirAirfoils = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces" + File.separator + "airfoils";

		String pathToXML = textFieldAircraftInputFile.getText(); 

		Main.setTheAircraft(Aircraft.importFromXML(
				pathToXML,
				dirLiftingSurfaces,
				dirFuselages,
				dirEngines,
				dirNacelles,
				dirLandingGears,
				dirSystems,
				dirCabinConfiguration,
				dirAirfoils,
				aeroDatabaseReader,
				highLiftDatabaseReader,
				fusDesDatabaseReader,
				veDSCDatabaseReader)
				);
		Main.getProgressBar().setProgress(0.2);
		Main.getStatusBar().setText("Creating Aircraft Object...");

		// COMPONENTS LOG TO INTERFACE
		logAircraftFromFileToInterface();
		Main.getProgressBar().setProgress(0.3);
		Main.getStatusBar().setText("Logging Aircraft Object Data...");
		logFuselageFromFileToInterface();
		Main.getProgressBar().setProgress(0.4);
		Main.getStatusBar().setText("Logging Fuselage Object Data...");
		
		// COMPONENTS 3 VIEW CREATION
		//............................
		// aircraft
		createAircraftTopView();
		Main.getProgressBar().setProgress(0.5);
		Main.getStatusBar().setText("Creating Aircraft Top View...");
		createAircraftSideView();
		Main.getProgressBar().setProgress(0.6);
		Main.getStatusBar().setText("Creating Aircraft Side View...");
		createAircraftFrontView();
		Main.getProgressBar().setProgress(0.7);
		Main.getStatusBar().setText("Creating Aircraft Front View...");
		//............................
		// fuselage
		createFuselageTopView();
		Main.getProgressBar().setProgress(0.8);
		Main.getStatusBar().setText("Creating Fuselage Top View...");
		createFuselageSideView();
		Main.getProgressBar().setProgress(0.9);
		Main.getStatusBar().setText("Creating Fuselage Side View...");
		createFuselageFrontView();
		Main.getProgressBar().setProgress(1.0);
		Main.getStatusBar().setText("Creating Fuselage Front View...");
		
		// write again
		System.setOut(originalOut);
		
		Main.getStatusBar().setText("Task Complete!");
		
		ObjectProperty<Aircraft> aircraft = new SimpleObjectProperty<>();

		try {
			aircraft.set(Main.getTheAircraft());
			newAircraftButton.disableProperty().bind(
					Bindings.isNull(aircraft)
					);
		} catch (Exception e) {
			newAircraftButton.setDisable(true);
		}
		
	}
	
	private void createAircraftTopView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// left curve, upperview
		List<Amount<Length>> vX1Left = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountX();
		int nX1Left = vX1Left.size();
		List<Amount<Length>> vY1Left = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountY();

		// right curve, upperview
		List<Amount<Length>> vX2Right = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountX();
		int nX2Right = vX2Right.size();
		List<Amount<Length>> vY2Right = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountY();

		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		IntStream.range(0, nX1Left)
		.forEach(i -> {
			seriesFuselageCurve.add(vX1Left.get(i).doubleValue(SI.METRE), vY1Left.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nX2Right)
		.forEach(i -> {
			seriesFuselageCurve.add(vX2Right.get(vX2Right.size()-1-i).doubleValue(SI.METRE), vY2Right.get(vY2Right.size()-1-i).doubleValue(SI.METRE));
		});
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		
		XYSeries seriesWingTopView = new XYSeries("Wing - Top View", false);
		IntStream.range(0, dataTopViewIsolated.length)
		.forEach(i -> {
			seriesWingTopView.add(
					dataTopViewIsolated[i][1] + Main.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER),
					dataTopViewIsolated[i][0] + Main.getTheAircraft().getWing().getYApexConstructionAxes().doubleValue(SI.METER)
					);
		});
		IntStream.range(0, dataTopViewIsolated.length)
		.forEach(i -> {
			seriesWingTopView.add(
					dataTopViewIsolated[dataTopViewIsolated.length-1-i][1] + Main.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER),
					-dataTopViewIsolated[dataTopViewIsolated.length-1-i][0] + Main.getTheAircraft().getWing().getYApexConstructionAxes().doubleValue(SI.METER)
					);
		});
		
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolatedHTail = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.HORIZONTAL_TAIL);

		XYSeries seriesHTailTopView = new XYSeries("HTail - Top View", false);
		IntStream.range(0, dataTopViewIsolatedHTail.length)
		.forEach(i -> {
			seriesHTailTopView.add(
					dataTopViewIsolatedHTail[i][1] + Main.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER),
					dataTopViewIsolatedHTail[i][0] + Main.getTheAircraft().getHTail().getYApexConstructionAxes().doubleValue(SI.METER)
					);
		});
		IntStream.range(0, dataTopViewIsolatedHTail.length)
		.forEach(i -> {
			seriesHTailTopView.add(
					dataTopViewIsolatedHTail[dataTopViewIsolatedHTail.length-1-i][1] + Main.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER),
					- dataTopViewIsolatedHTail[dataTopViewIsolatedHTail.length-1-i][0] + Main.getTheAircraft().getHTail().getYApexConstructionAxes().doubleValue(SI.METER)
					);
		});
		
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		Double[] vTailRootXCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(0).getAirfoilCreator().getXCoords();
		Double[] vTailRootYCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(0).getAirfoilCreator().getZCoords();
		Double[] vTailTipXCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(Main.getTheAircraft().getVTail().getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
		Double[] vTailTipYCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(Main.getTheAircraft().getVTail().getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
		int nPointsVTail = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedXle().size();
		
		XYSeries seriesVTailRootAirfoilTopView = new XYSeries("VTail Root - Top View", false);
		IntStream.range(0, vTailRootXCoordinates.length)
		.forEach(i -> {
			seriesVTailRootAirfoilTopView.add(
					(vTailRootXCoordinates[i]*Main.getTheAircraft().getVTail().getChordRoot().getEstimatedValue()) + Main.getTheAircraft().getVTail().getXApexConstructionAxes().getEstimatedValue(),
					(vTailRootYCoordinates[i]*Main.getTheAircraft().getVTail().getChordRoot().getEstimatedValue())
					);
		});
		
		XYSeries seriesVTailTipAirfoilTopView = new XYSeries("VTail Tip - Top View", false);
		IntStream.range(0, vTailTipXCoordinates.length)
		.forEach(i -> {
			seriesVTailTipAirfoilTopView.add(
					(vTailTipXCoordinates[i]*Main.getTheAircraft().getVTail().getChordTip().getEstimatedValue()) 
					  + Main.getTheAircraft().getVTail().getXApexConstructionAxes().getEstimatedValue()
					  + Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsVTail-1).getEstimatedValue(),
					  (vTailTipYCoordinates[i]*Main.getTheAircraft().getVTail().getChordTip().getEstimatedValue())
					);
		});
		
		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesTopViewList = new ArrayList<>();
		
		for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
			
			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline();
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperY = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYRight();
			
			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerY = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYLeft();

			List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
			List<Amount<Length>> dataOutlineXZCurveNacelleY = new ArrayList<>();
			
			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(j)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));
			}
			
			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1) 
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleY.add(nacelleCurveLowerY.get(nacelleCurveXPoints-j-1)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));
			}
			
			dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0)
					.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
			dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(0)
					.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));
			
			XYSeries seriesNacelleCruvesTopView = new XYSeries("Nacelle " + i + " XZ Curve - Top View", false);
			IntStream.range(0, dataOutlineXZCurveNacelleX.size())
			.forEach(j -> {
				seriesNacelleCruvesTopView.add(
						dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER),
						dataOutlineXZCurveNacelleY.get(j).doubleValue(SI.METER)
						);
			});
			
			seriesNacelleCruvesTopViewList.add(seriesNacelleCruvesTopView);
		}
		
		double xMaxTopView = 1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = 1.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMinTopView = -0.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
			
		int WIDTH = 700;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		Map<Double, Tuple2<XYSeries, Color>> componentZList = new HashMap<>();
		componentZList.put(
				Main.getTheAircraft().getFuselage().getZApexConstructionAxes().doubleValue(SI.METER)
				+ Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionCylinderHeight().divide(2).doubleValue(SI.METER),
				Tuple.of(seriesFuselageCurve, Color.WHITE) 
				);
		componentZList.put(
				Main.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER),
				Tuple.of(seriesWingTopView, Color.decode("#87CEFA"))
				); 
		componentZList.put(
				Main.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER),
				Tuple.of(seriesHTailTopView, Color.decode("#00008B"))
				);
		componentZList.put(
				Main.getTheAircraft().getFuselage().getZApexConstructionAxes().doubleValue(SI.METER)
				+ Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionCylinderHeight().divide(2).doubleValue(SI.METER)
				+ 0.0001,
				Tuple.of(seriesVTailRootAirfoilTopView, Color.decode("#FFD700"))
				);
		componentZList.put(
				Main.getTheAircraft().getVTail().getZApexConstructionAxes().doubleValue(SI.METER) 
				+ Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSpan().doubleValue(SI.METER), 
				Tuple.of(seriesVTailTipAirfoilTopView, Color.decode("#FFD700"))
				);
		seriesNacelleCruvesTopViewList.stream().forEach(
				nac -> componentZList.put(
						Main.getTheAircraft().getNacelles().getNacellesList().get(
								seriesNacelleCruvesTopViewList.indexOf(nac)
								).getZApexConstructionAxes().doubleValue(SI.METER)
						+ seriesNacelleCruvesTopViewList.indexOf(nac)*0.001, 
						Tuple.of(nac, Color.decode("#FF7F50"))
						)
				);
		
		Map<Double, Tuple2<XYSeries, Color>> componentZListSorted = 
				componentZList.entrySet().stream()
			    .sorted(Entry.comparingByKey(Comparator.reverseOrder()))
			    .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
			                              (e1, e2) -> e1, LinkedHashMap::new));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		componentZListSorted.values().stream().forEach(t -> dataset.addSeries(t._1()));
		
		List<Color> colorList = new ArrayList<>();
		componentZListSorted.values().stream().forEach(t -> colorList.add(t._2()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Aircraft data representation - Top View", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
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
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.TRUE);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					colorList.get(i)
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
				+ "AircraftTopView.svg";
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
		
		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		aircraftTopViewPane.getChildren().add(sceneTopView.getRoot());
	}
	
	private void createAircraftSideView() {
	
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// upper curve, sideview
		List<Amount<Length>> vX1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountX();
		int nX1Upper = vX1Upper.size();
		List<Amount<Length>> vZ1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountZ();

		// lower curve, sideview
		List<Amount<Length>> vX2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountX();
		int nX2Lower = vX2Lower.size();
		List<Amount<Length>> vZ2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountZ();

		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		IntStream.range(0, nX1Upper)
		.forEach(i -> {
			seriesFuselageCurve.add(vX1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nX2Lower)
		.forEach(i -> {
			seriesFuselageCurve.add(vX2Lower.get(vX2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
		});
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		Double[] wingRootXCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(0).getAirfoilCreator().getXCoords();
		Double[] wingRootZCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(0).getAirfoilCreator().getZCoords();
		Double[] wingTipXCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(Main.getTheAircraft().getWing().getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
		Double[] wingTipZCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(Main.getTheAircraft().getWing().getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
		int nPointsWing = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().size();
		
		XYSeries seriesWingRootAirfoil = new XYSeries("Wing Root - Side View", false);
		IntStream.range(0, wingRootXCoordinates.length)
		.forEach(i -> {
			seriesWingRootAirfoil.add(
					(wingRootXCoordinates[i]*Main.getTheAircraft().getWing().getChordRoot().getEstimatedValue()) 
					+ Main.getTheAircraft().getWing().getXApexConstructionAxes().getEstimatedValue(),
					(wingRootZCoordinates[i]*Main.getTheAircraft().getWing().getChordRoot().getEstimatedValue())
					+ Main.getTheAircraft().getWing().getZApexConstructionAxes().getEstimatedValue()
					);
		});
		
		XYSeries seriesWingTipAirfoil = new XYSeries("Wing Tip - Side View", false);
		IntStream.range(0, wingTipXCoordinates.length)
		.forEach(i -> {
			seriesWingTipAirfoil.add(
					(wingTipXCoordinates[i]*Main.getTheAircraft().getWing().getChordTip().getEstimatedValue()) 
					+ Main.getTheAircraft().getWing().getXApexConstructionAxes().getEstimatedValue()
					+ Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsWing-1).getEstimatedValue(),
					(wingTipZCoordinates[i]*Main.getTheAircraft().getWing().getChordTip().getEstimatedValue())
					+ Main.getTheAircraft().getWing().getZApexConstructionAxes().getEstimatedValue()
					);
		});
		
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		Double[] hTailRootXCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(0).getAirfoilCreator().getXCoords();
		Double[] hTailRootZCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(0).getAirfoilCreator().getZCoords();
		Double[] hTailTipXCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(Main.getTheAircraft().getHTail().getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
		Double[] hTailTipZCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(Main.getTheAircraft().getHTail().getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
		int nPointsHTail = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedXle().size();
		
		XYSeries seriesHTailRootAirfoil = new XYSeries("HTail Root - Side View", false);
		IntStream.range(0, hTailRootXCoordinates.length)
		.forEach(i -> {
			seriesHTailRootAirfoil.add(
					(hTailRootXCoordinates[i]*Main.getTheAircraft().getHTail().getChordRoot().getEstimatedValue())
					+ Main.getTheAircraft().getHTail().getXApexConstructionAxes().getEstimatedValue(),
					(hTailRootZCoordinates[i]*Main.getTheAircraft().getHTail().getChordRoot().getEstimatedValue())
					+ Main.getTheAircraft().getHTail().getZApexConstructionAxes().getEstimatedValue()
					);
		});
		
		XYSeries seriesHTailTipAirfoil = new XYSeries("HTail Tip - Side View", false);
		IntStream.range(0, hTailTipXCoordinates.length)
		.forEach(i -> {
			seriesHTailTipAirfoil.add(
					(hTailTipXCoordinates[i]*Main.getTheAircraft().getHTail().getChordTip().getEstimatedValue()) 
					+ Main.getTheAircraft().getHTail().getXApexConstructionAxes().getEstimatedValue()
					+ Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(),
					(hTailTipZCoordinates[i]*Main.getTheAircraft().getHTail().getChordTip().getEstimatedValue())
					+ Main.getTheAircraft().getHTail().getZApexConstructionAxes().getEstimatedValue()
					);
		});
		
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		Double[][] dataTopViewVTail = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);
		
		XYSeries seriesVTailSideView = new XYSeries("VTail - Side View", false);
		IntStream.range(0, dataTopViewVTail.length)
		.forEach(i -> {
			seriesVTailSideView.add(
					dataTopViewVTail[i][0] + Main.getTheAircraft().getVTail().getXApexConstructionAxes().doubleValue(SI.METER),
					dataTopViewVTail[i][1] + Main.getTheAircraft().getVTail().getZApexConstructionAxes().doubleValue(SI.METER)
					);
		});
		
		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesSideViewList = new ArrayList<>();
		
		for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
			
			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline();
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperZ = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZUpper();
			
			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerZ = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZLower();

			List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
			List<Amount<Length>> dataOutlineXZCurveNacelleZ = new ArrayList<>();
			
			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(j)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
			}
			
			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1) 
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveLowerZ.get(nacelleCurveXPoints-j-1)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
			}
			
			dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0)
					.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
			dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(0)
					.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
			
			
			XYSeries seriesNacelleCruvesSideView = new XYSeries("Nacelle " + i + " XY Curve - Side View", false);
			IntStream.range(0, dataOutlineXZCurveNacelleX.size())
			.forEach(j -> {
				seriesNacelleCruvesSideView.add(
						dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER),
						dataOutlineXZCurveNacelleZ.get(j).doubleValue(SI.METER)
						);
			});
			
			seriesNacelleCruvesSideViewList.add(seriesNacelleCruvesSideView);
			
		}
		
		//--------------------------------------------------
		// get data vectors from landing gears 
		//--------------------------------------------------
		Amount<Length> radius = Main.getTheAircraft().getLandingGears().getRearWheelsHeight().divide(2);
		Double[] wheelCenterPosition = new Double[] {
				Main.getTheAircraft().getLandingGears().getXApexConstructionAxes().doubleValue(SI.METER),
				Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
				- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
				- radius.doubleValue(SI.METER)
		};
		Double[] thetaArray = MyArrayUtils.linspaceDouble(0, 2*Math.PI, 360);
		
		XYSeries seriesLandingGearSideView = new XYSeries("Landing Gears - Side View", false);
		IntStream.range(0, thetaArray.length)
		.forEach(i -> {
			seriesLandingGearSideView.add(
					radius.doubleValue(SI.METER)*Math.cos(thetaArray[i]) + wheelCenterPosition[0],
					radius.doubleValue(SI.METER)*Math.sin(thetaArray[i]) + wheelCenterPosition[1]
					);
		});

		double xMaxSideView = 1.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double xMinSideView = -0.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMaxSideView = 1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double yMinSideView = -1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesNacelleCruvesSideViewList.stream().forEach(
				nac -> seriesAndColorList.add(Tuple.of(nac, Color.decode("#FF7F50")))
				);
		seriesAndColorList.add(Tuple.of(seriesWingRootAirfoil, Color.decode("#87CEFA")));
		seriesAndColorList.add(Tuple.of(seriesWingTipAirfoil, Color.decode("#87CEFA")));
		seriesAndColorList.add(Tuple.of(seriesHTailRootAirfoil, Color.decode("#00008B")));
		seriesAndColorList.add(Tuple.of(seriesHTailTipAirfoil, Color.decode("#00008B")));
		seriesAndColorList.add(Tuple.of(seriesLandingGearSideView, Color.decode("#404040")));
		seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));
		seriesAndColorList.add(Tuple.of(seriesVTailSideView, Color.decode("#FFD700")));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Aircraft data representation - Side View", 
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
		domain.setRange(xMinSideView, xMaxSideView);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(yMinSideView, yMaxSideView);

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
				+ "AircraftSideView.svg";
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
		aircraftSideViewPane.getChildren().add(sceneSideView.getRoot());		
		
	}
	
	private void createAircraftFrontView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// section upper curve
		List<Amount<Length>> vY1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionUpperCurveAmountY();
		int nY1Upper = vY1Upper.size();
		List<Amount<Length>> vZ1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionUpperCurveAmountZ();

		// section lower curve
		List<Amount<Length>> vY2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionLowerCurveAmountY();
		int nY2Lower = vY2Lower.size();
		List<Amount<Length>> vZ2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionLowerCurveAmountZ();
		
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Front View", false);
		IntStream.range(0, nY1Upper)
		.forEach(i -> {
			seriesFuselageCurve.add(vY1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nY2Lower)
		.forEach(i -> {
			seriesFuselageCurve.add(vY2Lower.get(vY2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
		});
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		List<Amount<Length>> wingBreakPointsYCoordinates = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getYBreakPoints();
		int nYPointsWingTemp = wingBreakPointsYCoordinates.size();
		for(int i=0; i<nYPointsWingTemp; i++)
			wingBreakPointsYCoordinates.add(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getYBreakPoints().get(nYPointsWingTemp-i-1));
		int nYPointsWing = wingBreakPointsYCoordinates.size();
		
		List<Amount<Length>> wingThicknessZCoordinates = new ArrayList<Amount<Length>>();
		for(int i=0; i<Main.getTheAircraft().getWing().getAirfoilList().size(); i++)
			wingThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
							MyArrayUtils.getMax(Main.getTheAircraft().getWing().getAirfoilList().get(i).getAirfoilCreator().getZCoords()),
							SI.METER
							)
					);
		for(int i=0; i<nYPointsWingTemp; i++) {
			wingThicknessZCoordinates.add(
					Amount.valueOf(
							(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getChordsBreakPoints().get(nYPointsWingTemp-i-1).doubleValue(SI.METER)*
									MyArrayUtils.getMin(Main.getTheAircraft().getWing().getAirfoilList().get(nYPointsWingTemp-i-1).getAirfoilCreator().getZCoords())),
							SI.METER
							)
					);
		}
		
		List<Amount<Angle>> dihedralList = new ArrayList<>();
		for (int i = 0; i < Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().size(); i++) {
			dihedralList.add(
					Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().get(i)
					);
		}
		for (int i = 0; i < Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().size(); i++) {
			dihedralList.add(
					Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().get(
							Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().size()-1-i)
					);
		}
		
		XYSeries seriesWingFrontView = new XYSeries("Wing - Front View", false);
		IntStream.range(0, nYPointsWing)
		.forEach(i -> {
			seriesWingFrontView.add(
					wingBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getWing().getYApexConstructionAxes()).doubleValue(SI.METRE),
					wingThicknessZCoordinates.get(i)
					.plus(Main.getTheAircraft().getWing().getZApexConstructionAxes())
						.plus(wingBreakPointsYCoordinates.get(i)
										.times(Math.sin(dihedralList.get(i)
												.doubleValue(SI.RADIAN))
												)
										)
							.doubleValue(SI.METRE)
					);
		});
		IntStream.range(0, nYPointsWing)
		.forEach(i -> {
			seriesWingFrontView.add(
					-wingBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getWing().getYApexConstructionAxes()).doubleValue(SI.METRE),
					wingThicknessZCoordinates.get(i)
					.plus(Main.getTheAircraft().getWing().getZApexConstructionAxes())
						.plus(wingBreakPointsYCoordinates.get(i)
										.times(Math.sin(dihedralList.get(i)
												.doubleValue(SI.RADIAN))
												)
										)
							.doubleValue(SI.METRE)
					);
		});
		
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		List<Amount<Length>> hTailBreakPointsYCoordinates = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getYBreakPoints();
		int nYPointsHTailTemp = hTailBreakPointsYCoordinates.size();
		for(int i=0; i<nYPointsHTailTemp; i++)
			hTailBreakPointsYCoordinates.add(Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getYBreakPoints().get(nYPointsHTailTemp-i-1));
		int nYPointsHTail = hTailBreakPointsYCoordinates.size();
		
		List<Amount<Length>> hTailThicknessZCoordinates = new ArrayList<Amount<Length>>();
		for(int i=0; i<Main.getTheAircraft().getHTail().getAirfoilList().size(); i++)
			hTailThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
							MyArrayUtils.getMax(Main.getTheAircraft().getHTail().getAirfoilList().get(i).getAirfoilCreator().getZCoords()),
							SI.METER
							)
					);
		for(int i=0; i<nYPointsHTailTemp; i++)
			hTailThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getChordsBreakPoints().get(nYPointsHTailTemp-i-1).doubleValue(SI.METER)*
							MyArrayUtils.getMin(Main.getTheAircraft().getHTail().getAirfoilList().get(nYPointsHTailTemp-i-1).getAirfoilCreator().getZCoords()),
							SI.METER
							)
					);
		
		List<Amount<Angle>> dihedralListHTail = new ArrayList<>();
		for (int i = 0; i < Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().size(); i++) {
			dihedralListHTail.add(
					Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().get(i)
					);
		}
		for (int i = 0; i < Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().size(); i++) {
			dihedralListHTail.add(
					Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().get(
							Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().size()-1-i)
					);
		}
		
		XYSeries seriesHTailFrontView = new XYSeries("HTail - Front View", false);
		IntStream.range(0, nYPointsHTail)
		.forEach(i -> {
			seriesHTailFrontView.add(
					hTailBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getHTail().getYApexConstructionAxes()).doubleValue(SI.METRE),
					hTailThicknessZCoordinates.get(i)
					.plus(Main.getTheAircraft().getHTail().getZApexConstructionAxes())
					.plus(hTailBreakPointsYCoordinates.get(i)
									.times(Math.sin(dihedralListHTail.get(i)
											.doubleValue(SI.RADIAN))
											)
									)
						.doubleValue(SI.METRE)
					);
		});
		IntStream.range(0, nYPointsHTail)
		.forEach(i -> {
			seriesHTailFrontView.add(
					-hTailBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getHTail().getYApexConstructionAxes()).doubleValue(SI.METRE),
					hTailThicknessZCoordinates.get(i)
					.plus(Main.getTheAircraft().getHTail().getZApexConstructionAxes())
					.plus(hTailBreakPointsYCoordinates.get(i)
									.times(Math.sin(dihedralListHTail.get(i)
											.doubleValue(SI.RADIAN))
											)
									)
						.doubleValue(SI.METRE)
					);
		});
		
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		List<Amount<Length>> vTailBreakPointsYCoordinates = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getYBreakPoints();
		int nYPointsVTailTemp = vTailBreakPointsYCoordinates.size();
		for(int i=0; i<nYPointsVTailTemp; i++)
			vTailBreakPointsYCoordinates.add(Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getYBreakPoints().get(nYPointsVTailTemp-i-1));
		int nYPointsVTail = vTailBreakPointsYCoordinates.size();
		
		List<Amount<Length>> vTailThicknessZCoordinates = new ArrayList<Amount<Length>>();
		for(int i=0; i<Main.getTheAircraft().getVTail().getAirfoilList().size(); i++)
			vTailThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
							MyArrayUtils.getMax(Main.getTheAircraft().getVTail().getAirfoilList().get(i).getAirfoilCreator().getZCoords()),
							SI.METER
							)
					);
		for(int i=0; i<nYPointsVTailTemp; i++)
			vTailThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getChordsBreakPoints().get(nYPointsVTailTemp-i-1).doubleValue(SI.METER)*
							MyArrayUtils.getMin(Main.getTheAircraft().getVTail().getAirfoilList().get(nYPointsVTailTemp-i-1).getAirfoilCreator().getZCoords()),
							SI.METER
							)
					);
		
		XYSeries seriesVTailFrontView = new XYSeries("VTail - Front View", false);
		IntStream.range(0, nYPointsVTail)
		.forEach(i -> {
			seriesVTailFrontView.add(
					vTailThicknessZCoordinates.get(i).plus(Main.getTheAircraft().getVTail().getYApexConstructionAxes()).doubleValue(SI.METRE),
					vTailBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getVTail().getZApexConstructionAxes()).doubleValue(SI.METRE)
					);
		});
		
		//--------------------------------------------------
		// get data vectors from nacelles discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesFrontViewList = new ArrayList<>();
		
		for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

			double[] angleArray = MyArrayUtils.linspace(0.0, 2*Math.PI, 20);
			double[] yCoordinate = new double[angleArray.length];
			double[] zCoordinate = new double[angleArray.length];

			double radius = Main.getTheAircraft().getNacelles()
					.getNacellesList().get(i)
					.getDiameterMax()
					.divide(2)
					.doubleValue(SI.METER);
			double y0 = Main.getTheAircraft().getNacelles()
					.getNacellesList().get(i)
					.getYApexConstructionAxes()
					.doubleValue(SI.METER);

			double z0 = Main.getTheAircraft().getNacelles()
					.getNacellesList().get(i)
					.getZApexConstructionAxes()
					.doubleValue(SI.METER);

			for(int j=0; j<angleArray.length; j++) {
				yCoordinate[j] = radius*Math.cos(angleArray[j]);
				zCoordinate[j] = radius*Math.sin(angleArray[j]);
			}

			XYSeries seriesNacelleCruvesFrontView = new XYSeries("Nacelle " + i + " - Front View", false);
			IntStream.range(0, yCoordinate.length)
			.forEach(j -> {
				seriesNacelleCruvesFrontView.add(
						yCoordinate[j] + y0,
						zCoordinate[j] + z0
						);
			});
			
			seriesNacelleCruvesFrontViewList.add(seriesNacelleCruvesFrontView);
		}
		
		//--------------------------------------------------
		// get data vectors from landing gears
		//--------------------------------------------------
		List<XYSeries> serieLandingGearsCruvesFrontViewList = new ArrayList<>();
		
		for(int i=0; i<Main.getTheAircraft().getLandingGears().getNumberOfRearWheels()/2; i++) {
			
			XYSeries seriesLeftLandingGearCruvesFrontView = new XYSeries("Left Landing Gear " + i + " - Front View", false);
			seriesLeftLandingGearCruvesFrontView.add(
					Main.getTheAircraft().getLandingGears()
					.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
					+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
					Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					);
			seriesLeftLandingGearCruvesFrontView.add(
					Main.getTheAircraft().getLandingGears()
					.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
					+ Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
					+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
					Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					);
			seriesLeftLandingGearCruvesFrontView.add(
					Main.getTheAircraft().getLandingGears()
					.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
					+ Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
					+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
					Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
					);
			seriesLeftLandingGearCruvesFrontView.add(
					Main.getTheAircraft().getLandingGears()
					.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
					+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
					Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
					);
			seriesLeftLandingGearCruvesFrontView.add(
					Main.getTheAircraft().getLandingGears()
					.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
					+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
					Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					);

			XYSeries seriesRightLandingGearCruvesFrontView = new XYSeries("Right Landing Gear " + i + " - Front View", false);
			seriesRightLandingGearCruvesFrontView.add(
					- (Main.getTheAircraft().getLandingGears()
							.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
							+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
					Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					);
			seriesRightLandingGearCruvesFrontView.add(
					- (Main.getTheAircraft().getLandingGears()
							.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
							+ Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
							+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
					Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					);
			seriesRightLandingGearCruvesFrontView.add(
					- (Main.getTheAircraft().getLandingGears()
							.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
							+ Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
							+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
					Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
					);
			seriesRightLandingGearCruvesFrontView.add(
					- (Main.getTheAircraft().getLandingGears()
							.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
							+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
					Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
					);
			seriesRightLandingGearCruvesFrontView.add(
					- (Main.getTheAircraft().getLandingGears()
							.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
							+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
					Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					);
			
			serieLandingGearsCruvesFrontViewList.add(seriesLeftLandingGearCruvesFrontView);
			serieLandingGearsCruvesFrontViewList.add(seriesRightLandingGearCruvesFrontView);
		}
		
		double yMaxFrontView = 1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double yMinFrontView = -1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
		double zMaxFrontView = yMaxFrontView; 
		double zMinFrontView = yMinFrontView;
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));
		seriesNacelleCruvesFrontViewList.stream().forEach(
				nac -> seriesAndColorList.add(Tuple.of(nac, Color.decode("#FF7F50")))
				);
		seriesAndColorList.add(Tuple.of(seriesWingFrontView, Color.decode("#87CEFA")));
		seriesAndColorList.add(Tuple.of(seriesHTailFrontView, Color.decode("#00008B")));
		seriesAndColorList.add(Tuple.of(seriesVTailFrontView, Color.decode("#FFD700")));
		serieLandingGearsCruvesFrontViewList.stream().forEach(
				lg -> seriesAndColorList.add(Tuple.of(lg, Color.decode("#404040")))
				);
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Aircraft data representation - Front View", 
				"y (m)", 
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
		domain.setRange(yMinFrontView, yMaxFrontView);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(zMinFrontView, zMaxFrontView);

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
				+ "AircraftFrontView.svg";
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
		Scene sceneFrontView = new Scene(stackPane, WIDTH+10, HEIGHT+10, javafx.scene.paint.Color.web("#666970"));
		aircraftFrontViewPane.getChildren().add(sceneFrontView.getRoot());
		
	}
	
	private void logAircraftFromFileToInterface() {

		String dirLiftingSurfaces = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces";
		String dirFuselages = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "fuselages";
		String dirEngines = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "engines";
		String dirLandingGears = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "landing_gears";
		String dirSystems = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "systems";
		String dirCabinConfiguration = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "cabin_configurations";
		String pathToXML = textFieldAircraftInputFile.getText();

		// print the toString method of the aircraft inside the text area of the GUI ...
		textAreaAircraftConsoleOutput.setText(
				Main.getTheAircraft().toString()
				);

		// get the text field for aircraft input data
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		//---------------------------------------------------------------------------------
		// AIRCRAFT TYPE:
		String aircraftTypeFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@type");
		
		if(aircraftTypeFileName != null) { 
			if(aircraftTypeChoiceBox != null) {
				if(aircraftTypeFileName.equalsIgnoreCase("JET"))
					aircraftTypeChoiceBox.getSelectionModel().select(0);
				else if(aircraftTypeFileName.equalsIgnoreCase("FIGHTER"))		
					aircraftTypeChoiceBox.getSelectionModel().select(1);
				else if(aircraftTypeFileName.equalsIgnoreCase("BUSINESS_JET"))
					aircraftTypeChoiceBox.getSelectionModel().select(2);
				else if(aircraftTypeFileName.equalsIgnoreCase("TURBOPROP"))
					aircraftTypeChoiceBox.getSelectionModel().select(3);
				else if(aircraftTypeFileName.equalsIgnoreCase("GENERAL_AVIATION"))
					aircraftTypeChoiceBox.getSelectionModel().select(4);
				else if(aircraftTypeFileName.equalsIgnoreCase("COMMUTER"))
					aircraftTypeChoiceBox.getSelectionModel().select(5);
				else if(aircraftTypeFileName.equalsIgnoreCase("ACROBATIC"))
					aircraftTypeChoiceBox.getSelectionModel().select(6);
			}
		}
		
		//---------------------------------------------------------------------------------
		// REGULATIONS TYPE:
		String regulationsTypeFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@regulations");
		
		if(regulationsTypeFileName != null) { 
			if(regulationsTypeChoiceBox != null) {
				if(regulationsTypeFileName.equalsIgnoreCase("FAR_23"))
					regulationsTypeChoiceBox.getSelectionModel().select(0);
				else if(regulationsTypeFileName.equalsIgnoreCase("FAR_25"))		
					regulationsTypeChoiceBox.getSelectionModel().select(1);
			}
		}
		
		//---------------------------------------------------------------------------------
		// CABIN CONFIGURATION:
		String cabinConfigrationFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//global_data/cabin_configuration/@file");

		if(textFieldAircraftCabinConfigurationFile != null) 
			textFieldAircraftCabinConfigurationFile.setText(
					dirCabinConfiguration 
					+ File.separator
					+ cabinConfigrationFileName
					);
		else
			textFieldAircraftCabinConfigurationFile.setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// FUSELAGE:
		String fuselageFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselages/fuselage/@file");
		if(fuselageFileName != null) 
			textFieldAircraftFuselageFile.setText(
					dirFuselages 
					+ File.separator
					+ fuselageFileName
					);
		else
			textFieldAircraftFuselageFile.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getFuselage() != null) {
			textFieldAircraftFuselageX.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getFuselage()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				fuselageXUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				fuselageXUnitChoiceBox.getSelectionModel().select(1);

		}

		else
			textFieldAircraftFuselageX.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getFuselage() != null) {
			textFieldAircraftFuselageY.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getFuselage()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				fuselageYUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				fuselageYUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftFuselageY.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getFuselage() != null) {
			textFieldAircraftFuselageZ.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getFuselage()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				fuselageZUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				fuselageZUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftFuselageZ.setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// WING:
		String wingFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/wing/@file");
		if(wingFileName != null)
			textFieldAircraftWingFile.setText(
					dirLiftingSurfaces 
					+ File.separator
					+ wingFileName
					);
		else
			textFieldAircraftWingFile.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getWing() != null) {

			textFieldAircraftWingX.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getWing()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				wingXUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				wingXUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftWingX.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getWing() != null) {

			textFieldAircraftWingY.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getWing()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				wingYUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				wingYUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftWingY.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getWing() != null) {

			textFieldAircraftWingZ.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getWing()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				wingZUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				wingZUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftWingZ.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getWing() != null) {

			textFieldAircraftWingRiggingAngle.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getWing()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				wingRiggingAngleUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				wingRiggingAngleUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftWingRiggingAngle.setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// HORIZONTAL TAIL:
		String hTailFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/horizontal_tail/@file");
		if(hTailFileName != null)
			textFieldAircraftHTailFile.setText(
					dirLiftingSurfaces 
					+ File.separator
					+ hTailFileName
					);
		else
			textFieldAircraftHTailFile.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getHTail() != null) {

			textFieldAircraftHTailX.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getHTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				hTailXUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				hTailXUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftHTailX.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getHTail() != null) {

			textFieldAircraftHTailY.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getHTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				hTailYUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				hTailYUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftHTailY.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getHTail() != null) {

			textFieldAircraftHTailZ.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getHTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				htailZUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				htailZUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftHTailZ.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getHTail() != null) {

			textFieldAircraftHTailRiggingAngle.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				hTailRiggingAngleUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				hTailRiggingAngleUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftHTailRiggingAngle.setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// VERTICAL TAIL:
		String vTailFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/vertical_tail/@file");
		if(vTailFileName != null)
			textFieldAircraftVTailFile.setText(
					dirLiftingSurfaces 
					+ File.separator
					+ vTailFileName
					);
		else
			textFieldAircraftVTailFile.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getVTail() != null) {

			textFieldAircraftVTailX.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getVTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				vTailXUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				vTailXUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftVTailX.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getVTail() != null) {

			textFieldAircraftVTailY.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getVTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				vTailYUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				vTailYUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftVTailY.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getVTail() != null) {

			textFieldAircraftVTailZ.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getVTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				vTailZUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				vTailZUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftVTailZ.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getVTail() != null) {

			textFieldAircraftVTailRiggingAngle.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				vTailRiggingAngleUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				vTailRiggingAngleUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftVTailRiggingAngle.setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// CANARD:
		String canardFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/canard/@file");
		if(canardFileName != null)
			textFieldAircraftCanardFile.setText(
					dirLiftingSurfaces 
					+ File.separator
					+ canardFileName
					);
		else
			textFieldAircraftCanardFile.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getCanard() != null) {

			textFieldAircraftCanardX.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getCanard()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				canardXUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				canardXUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftCanardX.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getCanard() != null) {

			textFieldAircraftCanardY.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getCanard()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				canardYUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				canardYUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftCanardY.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getCanard() != null) {

			textFieldAircraftCanardZ.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getCanard()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				canardZUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				canardZUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftCanardZ.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getCanard() != null) {

			textFieldAircraftCanardRiggingAngle.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				canardRiggingAngleUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				canardRiggingAngleUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftCanardRiggingAngle.setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// POWER PLANT:
		textFieldsAircraftEngineFileList = new ArrayList<>();
		textFieldsAircraftEngineFileList.add(textFieldAircraftEngineFile1);
		textFieldsAircraftEngineFileList.add(textFieldAircraftEngineFile2);
		textFieldsAircraftEngineFileList.add(textFieldAircraftEngineFile3);
		textFieldsAircraftEngineFileList.add(textFieldAircraftEngineFile4);
		textFieldsAircraftEngineFileList.add(textFieldAircraftEngineFile5);
		textFieldsAircraftEngineFileList.add(textFieldAircraftEngineFile6);
		
		textFieldAircraftEngineXList = new ArrayList<>();
		textFieldAircraftEngineXList.add(textFieldAircraftEngineX1);
		textFieldAircraftEngineXList.add(textFieldAircraftEngineX2);
		textFieldAircraftEngineXList.add(textFieldAircraftEngineX3);
		textFieldAircraftEngineXList.add(textFieldAircraftEngineX4);
		textFieldAircraftEngineXList.add(textFieldAircraftEngineX5);
		textFieldAircraftEngineXList.add(textFieldAircraftEngineX6);
		
		textFieldAircraftEngineYList = new ArrayList<>();
		textFieldAircraftEngineYList.add(textFieldAircraftEngineY1);
		textFieldAircraftEngineYList.add(textFieldAircraftEngineY2);
		textFieldAircraftEngineYList.add(textFieldAircraftEngineY3);
		textFieldAircraftEngineYList.add(textFieldAircraftEngineY4);
		textFieldAircraftEngineYList.add(textFieldAircraftEngineY5);
		textFieldAircraftEngineYList.add(textFieldAircraftEngineY6);
		
		textFieldAircraftEngineZList = new ArrayList<>();
		textFieldAircraftEngineZList.add(textFieldAircraftEngineZ1);
		textFieldAircraftEngineZList.add(textFieldAircraftEngineZ2);
		textFieldAircraftEngineZList.add(textFieldAircraftEngineZ3);
		textFieldAircraftEngineZList.add(textFieldAircraftEngineZ4);
		textFieldAircraftEngineZList.add(textFieldAircraftEngineZ5);
		textFieldAircraftEngineZList.add(textFieldAircraftEngineZ6);
		
		choiceBoxesAircraftEnginePositonList = new ArrayList<>();
		choiceBoxesAircraftEnginePositonList.add(powerPlantMountingPositionTypeChoiceBox1);
		choiceBoxesAircraftEnginePositonList.add(powerPlantMountingPositionTypeChoiceBox2);
		choiceBoxesAircraftEnginePositonList.add(powerPlantMountingPositionTypeChoiceBox3);
		choiceBoxesAircraftEnginePositonList.add(powerPlantMountingPositionTypeChoiceBox4);
		choiceBoxesAircraftEnginePositonList.add(powerPlantMountingPositionTypeChoiceBox5);
		choiceBoxesAircraftEnginePositonList.add(powerPlantMountingPositionTypeChoiceBox6);
		
		textFieldAircraftEngineTiltList = new ArrayList<>();
		textFieldAircraftEngineTiltList.add(textFieldAircraftEngineTilt1);
		textFieldAircraftEngineTiltList.add(textFieldAircraftEngineTilt2);
		textFieldAircraftEngineTiltList.add(textFieldAircraftEngineTilt3);
		textFieldAircraftEngineTiltList.add(textFieldAircraftEngineTilt4);
		textFieldAircraftEngineTiltList.add(textFieldAircraftEngineTilt5);
		textFieldAircraftEngineTiltList.add(textFieldAircraftEngineTilt6);
		
		if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			powerPlantXUnitChoiceBox.getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			powerPlantXUnitChoiceBox.getSelectionModel().select(1);


		if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			powerPlantYUnitChoiceBox.getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			powerPlantYUnitChoiceBox.getSelectionModel().select(1);


		if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			powerPlantZUnitChoiceBox.getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			powerPlantZUnitChoiceBox.getSelectionModel().select(1);


		if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getTiltingAngle().getUnit().toString().equalsIgnoreCase("deg"))
			powerPlantTiltAngleUnitChoiceBox.getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getTiltingAngle().getUnit().toString().equalsIgnoreCase("rad"))
			powerPlantTiltAngleUnitChoiceBox.getSelectionModel().select(1);

		//..........................................................................................................
		NodeList nodelistEngines = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//power_plant/engine");
		if(nodelistEngines != null) {
			for (int i = 0; i < nodelistEngines.getLength(); i++) {
				//..........................................................................................................
				Node nodeEngine  = nodelistEngines.item(i); 
				Element elementEngine = (Element) nodeEngine;
				if(elementEngine.getAttribute("file") != null)
					textFieldsAircraftEngineFileList.get(i).setText(
							dirEngines 
							+ File.separator
							+ elementEngine.getAttribute("file")	
							);
				else
					textFieldsAircraftEngineFileList.get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) 
					textFieldAircraftEngineXList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getXApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					textFieldAircraftEngineXList.get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					textFieldAircraftEngineYList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getYApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					textFieldAircraftEngineYList.get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					textFieldAircraftEngineZList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getZApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					textFieldAircraftEngineZList.get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)

					if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("BURIED")
							)
						choiceBoxesAircraftEnginePositonList.get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("WING")
							)
						choiceBoxesAircraftEnginePositonList.get(i).getSelectionModel().select(1);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("AFT_FUSELAGE")
							)
						choiceBoxesAircraftEnginePositonList.get(i).getSelectionModel().select(2);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("HTAIL")
							)
						choiceBoxesAircraftEnginePositonList.get(i).getSelectionModel().select(3);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("REAR_FUSELAGE")
							)
						choiceBoxesAircraftEnginePositonList.get(i).getSelectionModel().select(4);

				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					textFieldAircraftEngineTiltList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getTiltingAngle()
									.getEstimatedValue()
									)
							);
				else
					textFieldAircraftEngineTiltList.get(i).setText(
							"NOT INITIALIZED"
							);
			}
		}
		//---------------------------------------------------------------------------------
		// NACELLES:
		textFieldsAircraftNacelleFileList = new ArrayList<>();
		textFieldsAircraftNacelleFileList.add(textFieldAircraftNacelleFile1);
		textFieldsAircraftNacelleFileList.add(textFieldAircraftNacelleFile2);
		textFieldsAircraftNacelleFileList.add(textFieldAircraftNacelleFile3);
		textFieldsAircraftNacelleFileList.add(textFieldAircraftNacelleFile4);
		textFieldsAircraftNacelleFileList.add(textFieldAircraftNacelleFile5);
		textFieldsAircraftNacelleFileList.add(textFieldAircraftNacelleFile6);
		
		textFieldAircraftNacelleXList = new ArrayList<>();
		textFieldAircraftNacelleXList.add(textFieldAircraftNacelleX1);
		textFieldAircraftNacelleXList.add(textFieldAircraftNacelleX2);
		textFieldAircraftNacelleXList.add(textFieldAircraftNacelleX3);
		textFieldAircraftNacelleXList.add(textFieldAircraftNacelleX4);
		textFieldAircraftNacelleXList.add(textFieldAircraftNacelleX5);
		textFieldAircraftNacelleXList.add(textFieldAircraftNacelleX6);
		
		textFieldAircraftNacelleYList = new ArrayList<>();
		textFieldAircraftNacelleYList.add(textFieldAircraftNacelleY1);
		textFieldAircraftNacelleYList.add(textFieldAircraftNacelleY2);
		textFieldAircraftNacelleYList.add(textFieldAircraftNacelleY3);
		textFieldAircraftNacelleYList.add(textFieldAircraftNacelleY4);
		textFieldAircraftNacelleYList.add(textFieldAircraftNacelleY5);
		textFieldAircraftNacelleYList.add(textFieldAircraftNacelleY6);
		
		textFieldAircraftNacelleZList = new ArrayList<>();
		textFieldAircraftNacelleZList.add(textFieldAircraftNacelleZ1);
		textFieldAircraftNacelleZList.add(textFieldAircraftNacelleZ2);
		textFieldAircraftNacelleZList.add(textFieldAircraftNacelleZ3);
		textFieldAircraftNacelleZList.add(textFieldAircraftNacelleZ4);
		textFieldAircraftNacelleZList.add(textFieldAircraftNacelleZ5);
		textFieldAircraftNacelleZList.add(textFieldAircraftNacelleZ6);
		
		choiceBoxesAircraftNacellePositonList = new ArrayList<>();
		choiceBoxesAircraftNacellePositonList.add(nacellesMountingPositionTypeChoiceBox1);
		choiceBoxesAircraftNacellePositonList.add(nacellesMountingPositionTypeChoiceBox2);
		choiceBoxesAircraftNacellePositonList.add(nacellesMountingPositionTypeChoiceBox3);
		choiceBoxesAircraftNacellePositonList.add(nacellesMountingPositionTypeChoiceBox4);
		choiceBoxesAircraftNacellePositonList.add(nacellesMountingPositionTypeChoiceBox5);
		choiceBoxesAircraftNacellePositonList.add(nacellesMountingPositionTypeChoiceBox6);
		
		//..........................................................................................................
		if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			nacelleXUnitChoiceBox.getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			nacelleXUnitChoiceBox.getSelectionModel().select(1);

		if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			nacelleYUnitChoiceBox.getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			nacelleYUnitChoiceBox.getSelectionModel().select(1);

		if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			nacelleZUnitChoiceBox.getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			nacelleZUnitChoiceBox.getSelectionModel().select(1);

		//..........................................................................................................
		NodeList nodelistNacelles = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//nacelles/nacelle");
		if(nodelistNacelles != null) {
			for (int i = 0; i < nodelistNacelles.getLength(); i++) {
				//..........................................................................................................
				Node nodeNacelle  = nodelistNacelles.item(i); 
				Element elementNacelle = (Element) nodeNacelle;
				if(elementNacelle.getAttribute("file") != null)
					textFieldsAircraftNacelleFileList.get(i).setText(
							dirEngines 
							+ File.separator
							+ elementNacelle.getAttribute("file")	
							);
				else
					textFieldsAircraftNacelleFileList.get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					textFieldAircraftNacelleXList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getXApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					textFieldAircraftNacelleXList.get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					textFieldAircraftNacelleYList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getYApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					textFieldAircraftNacelleYList.get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					textFieldAircraftNacelleZList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getZApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					textFieldAircraftNacelleZList.get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)

					if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("WING")
							)
						choiceBoxesAircraftNacellePositonList.get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("FUSELAGE")
							)
						choiceBoxesAircraftNacellePositonList.get(i).getSelectionModel().select(1);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("HTAIL")
							)
						choiceBoxesAircraftNacellePositonList.get(i).getSelectionModel().select(2);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("UNDERCARRIAGE_HOUSING")
							)
						choiceBoxesAircraftNacellePositonList.get(i).getSelectionModel().select(3);

			}
		}
		//---------------------------------------------------------------------------------
		// LANDING GEARS:
		String landingGearsFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//landing_gears/@file");
		if(landingGearsFileName != null) 
			textFieldAircraftLandingGearsFile.setText(
					dirLandingGears 
					+ File.separator
					+ landingGearsFileName
					);
		else
			textFieldAircraftLandingGearsFile.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getLandingGears() != null) {

			textFieldAircraftLandingGearsX.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				landingGearsXUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				landingGearsXUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftLandingGearsX.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getLandingGears() != null) {

			textFieldAircraftLandingGearsY.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				landingGearsYUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				landingGearsYUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftLandingGearsY.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getLandingGears() != null) {

			textFieldAircraftLandingGearsZ.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				landingGearsZUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				landingGearsZUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftLandingGearsZ.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getLandingGears() != null)

			if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("FUSELAGE")
					)
				landingGearsMountingPositionTypeChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("WING")
					)
				landingGearsMountingPositionTypeChoiceBox.getSelectionModel().select(1);
			else if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("NACELLE")
					)
				landingGearsMountingPositionTypeChoiceBox.getSelectionModel().select(2);

		//---------------------------------------------------------------------------------
		// SYSTEMS:
		String systemsFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//systems/@file");
		if(systemsFileName != null) 
			textFieldAircraftSystemsFile.setText(
					dirSystems 
					+ File.separator
					+ systemsFileName
					);
		else
			textFieldAircraftSystemsFile.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getSystems() != null) {

			textFieldAircraftSystemsX.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getSystems()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getSystems()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				systemsXUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getSystems()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				systemsXUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftSystemsX.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getSystems() != null) {

			textFieldAircraftSystemsY.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getSystems()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getSystems()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				systemsYUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getSystems()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				systemsYUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftSystemsY.setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getSystems() != null) {

			textFieldAircraftSystemsZ.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getSystems()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getSystems()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				systemsZUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getSystems()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				systemsZUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldAircraftSystemsZ.setText(
					"NOT INITIALIZED"
					);
	}
	
	private void createFuselageTopView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// left curve, upperview
		List<Amount<Length>> vX1Left = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountX();
		int nX1Left = vX1Left.size();
		List<Amount<Length>> vY1Left = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountY();

		// right curve, upperview
		List<Amount<Length>> vX2Right = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountX();
		int nX2Right = vX2Right.size();
		List<Amount<Length>> vY2Right = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountY();

		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		IntStream.range(0, nX1Left)
		.forEach(i -> {
			seriesFuselageCurve.add(vX1Left.get(i).doubleValue(SI.METRE), vY1Left.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nX2Right)
		.forEach(i -> {
			seriesFuselageCurve.add(vX2Right.get(vX2Right.size()-1-i).doubleValue(SI.METRE), vY2Right.get(vY2Right.size()-1-i).doubleValue(SI.METRE));
		});

		double xMaxTopView = 1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = 1.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMinTopView = -0.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);

		int WIDTH = 700;
		int HEIGHT = 600;

		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Fuselage data representation - Top View", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
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
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.TRUE);

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
				+ "FuselageTopView.svg";
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

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		fuselageTopViewPane.getChildren().add(sceneTopView.getRoot());
	}

	private void createFuselageSideView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// upper curve, sideview
		List<Amount<Length>> vX1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountX();
		int nX1Upper = vX1Upper.size();
		List<Amount<Length>> vZ1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountZ();

		// lower curve, sideview
		List<Amount<Length>> vX2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountX();
		int nX2Lower = vX2Lower.size();
		List<Amount<Length>> vZ2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountZ();

		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		IntStream.range(0, nX1Upper)
		.forEach(i -> {
			seriesFuselageCurve.add(vX1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nX2Lower)
		.forEach(i -> {
			seriesFuselageCurve.add(vX2Lower.get(vX2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
		});
		
		double xMaxSideView = 1.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double xMinSideView = -0.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMaxSideView = 1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double yMinSideView = -1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Fuselage data representation - Side View", 
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
		domain.setRange(xMinSideView, xMaxSideView);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(yMinSideView, yMaxSideView);

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
				+ "FuselageSideView.svg";
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
		fuselageSideViewPane.getChildren().add(sceneSideView.getRoot());		
		
	}
	
	private void createFuselageFrontView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// section upper curve
		List<Amount<Length>> vY1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionUpperCurveAmountY();
		int nY1Upper = vY1Upper.size();
		List<Amount<Length>> vZ1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionUpperCurveAmountZ();

		// section lower curve
		List<Amount<Length>> vY2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionLowerCurveAmountY();
		int nY2Lower = vY2Lower.size();
		List<Amount<Length>> vZ2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionLowerCurveAmountZ();
		
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Front View", false);
		IntStream.range(0, nY1Upper)
		.forEach(i -> {
			seriesFuselageCurve.add(vY1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nY2Lower)
		.forEach(i -> {
			seriesFuselageCurve.add(vY2Lower.get(vY2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
		});
		
		double yMaxFrontView = 1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double yMinFrontView = -1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
		double zMaxFrontView = yMaxFrontView; 
		double zMinFrontView = yMinFrontView;
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Fuselage data representation - Front View", 
				"y (m)", 
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
		domain.setRange(yMinFrontView, yMaxFrontView);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(zMinFrontView, zMaxFrontView);

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
				+ "FuselageFrontView.svg";
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
		Scene sceneFrontView = new Scene(stackPane, WIDTH+10, HEIGHT+10, javafx.scene.paint.Color.web("#666970"));
		fuselageFrontViewPane.getChildren().add(sceneFrontView.getRoot());
	}
	
	private void logFuselageFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		textAreaFuselageConsoleOutput.setText(
				Main.getTheAircraft().getFuselage().getFuselageCreator().toString()
				);

		//---------------------------------------------------------------------------------
		// ADJUST CRITERION CHOICE BOX:
		if(Main.getTheAircraft() != null)
			fuselageAdjustCriterionChoiceBox.setDisable(false);
		
		//---------------------------------------------------------------------------------
		// PRESSURIZED FLAG: 
		if(Main.getTheAircraft().getFuselage().getFuselageCreator().getPressurized().equals(Boolean.TRUE))
			fuselagePressurizedCheckBox.setSelected(true);
			
		//---------------------------------------------------------------------------------
		// DECK NUMBER:
		if(Main.getTheAircraft().getFuselage() != null)
			textFieldFuselageDeckNumber.setText(
					Integer.toString(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getDeckNumber()
							)
					);
		else
			textFieldFuselageDeckNumber.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// LENGTH:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			textFieldFuselageLength.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getLenF()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getLenF().getUnit().toString().equalsIgnoreCase("m"))
				fuselageLengthUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getLenF().getUnit().toString().equalsIgnoreCase("ft"))
				fuselageLengthUnitChoiceBox.getSelectionModel().select(1);
			
		}
		else
			textFieldFuselageLength.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// SURFACE ROUGHNESS:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			textFieldFuselageSurfaceRoughness.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getRoughness()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
				fuselageRoughnessUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
				fuselageRoughnessUnitChoiceBox.getSelectionModel().select(1);
			
		}
		else
			textFieldFuselageSurfaceRoughness.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// LENGTH RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			textFieldFuselageNoseLengthRatio.setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getLenRatioNF()
					.toString()
					);
		else
			textFieldFuselageNoseLengthRatio.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE TIP OFFSET RATIO:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			textFieldFuselageNoseTipOffset.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getHeightN()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getHeightN().getUnit().toString().equalsIgnoreCase("m"))
				fuselageNoseTipOffsetZUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getHeightN().getUnit().toString().equalsIgnoreCase("ft"))
				fuselageNoseTipOffsetZUnitChoiceBox.getSelectionModel().select(1);
			
		}
		else
			textFieldFuselageNoseTipOffset.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE DX CAP PERCENT:
		if(Main.getTheAircraft().getFuselage() != null)
			textFieldFuselageNoseDxCap.setText(
					Double.toString(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getDxNoseCapPercent()
							)
					);
		else
			textFieldFuselageNoseDxCap.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// WINDSHIELD TYPE:
		if(Main.getTheAircraft().getFuselage() != null) { 
			if(windshieldTypeChoiceBox != null) {
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getWindshieldType() == WindshieldTypeEnum.DOUBLE)
					windshieldTypeChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft().getFuselage().getFuselageCreator().getWindshieldType() == WindshieldTypeEnum.FLAT_FLUSH)		
					windshieldTypeChoiceBox.getSelectionModel().select(1);
				else if(Main.getTheAircraft().getFuselage().getFuselageCreator().getWindshieldType() == WindshieldTypeEnum.FLAT_PROTRUDING)
					windshieldTypeChoiceBox.getSelectionModel().select(2);
				else if(Main.getTheAircraft().getFuselage().getFuselageCreator().getWindshieldType() == WindshieldTypeEnum.SINGLE_ROUND)
					windshieldTypeChoiceBox.getSelectionModel().select(3);
				else if(Main.getTheAircraft().getFuselage().getFuselageCreator().getWindshieldType() == WindshieldTypeEnum.SINGLE_SHARP)
					windshieldTypeChoiceBox.getSelectionModel().select(4);
			}
		}
		
		//---------------------------------------------------------------------------------
		// WINDSHIELD WIDTH:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			textFieldFuselageNoseWindshieldWidth.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getWindshieldWidth()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getWindshieldWidth().getUnit().toString().equalsIgnoreCase("m"))
				fuselageWindshieldWidthUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getWindshieldWidth().getUnit().toString().equalsIgnoreCase("ft"))
				fuselageWindshieldWidthUnitChoiceBox.getSelectionModel().select(1);
			
		}
		else
			textFieldFuselageNoseWindshieldWidth.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// WINDSHIELD HEIGHT:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			textFieldFuselageNoseWindshieldHeight.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getWindshieldHeight()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getWindshieldHeight().getUnit().toString().equalsIgnoreCase("m"))
				fuselageWindshieldHeightUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getWindshieldHeight().getUnit().toString().equalsIgnoreCase("ft"))
				fuselageWindshieldHeightUnitChoiceBox.getSelectionModel().select(1);
			
		}
		else
			textFieldFuselageNoseWindshieldHeight.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE MID-SECTION HEIGHT TO TOTAL SECTION HEIGHT RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			textFieldFuselageNoseMidSectionHeight.setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionNoseMidLowerToTotalHeightRatio()
					.toString()
					);
		else
			textFieldFuselageNoseMidSectionHeight.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE MID-SECTION RHO UPPER:
		if(Main.getTheAircraft().getFuselage() != null)
			textFieldFuselageNoseMidSectionRhoUpper.setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionMidNoseRhoUpper()
					.toString()
					);
		else
			textFieldFuselageNoseMidSectionRhoUpper.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE MID-SECTION RHO LOWER:
		if(Main.getTheAircraft().getFuselage() != null)
			textFieldFuselageNoseMidSectionRhoLower.setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionMidNoseRhoLower()
					.toString()
					);
		else
			textFieldFuselageNoseMidSectionRhoLower.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER LENGTH RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			textFieldFuselageCylinderLengthRatio.setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getLenRatioCF()
					.toString()
					);
		else
			textFieldFuselageCylinderLengthRatio.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION WIDTH:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			textFieldFuselageCylinderSectionWidth.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getSectionCylinderWidth()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSectionCylinderWidth().getUnit().toString().equalsIgnoreCase("m"))
				fuselageCylinderSectionWidthUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSectionCylinderWidth().getUnit().toString().equalsIgnoreCase("ft"))
				fuselageCylinderSectionWidthUnitChoiceBox.getSelectionModel().select(1);
			
		}
		else
			textFieldFuselageCylinderSectionWidth.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION HEIGHT:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			textFieldFuselageCylinderSectionHeight.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getSectionCylinderHeight()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSectionCylinderHeight().getUnit().toString().equalsIgnoreCase("m"))
				fuselageCylinderSectionHeightUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSectionCylinderHeight().getUnit().toString().equalsIgnoreCase("ft"))
				fuselageCylinderSectionHeightUnitChoiceBox.getSelectionModel().select(1);
			
		}
		else
			textFieldFuselageCylinderSectionHeight.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION HEIGHT FROM GROUND:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			textFieldFuselageCylinderHeightFromGround.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getHeightFromGround()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getHeightFromGround().getUnit().toString().equalsIgnoreCase("m"))
				fuselageHeightFromGroundUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getHeightFromGround().getUnit().toString().equalsIgnoreCase("ft"))
				fuselageHeightFromGroundUnitChoiceBox.getSelectionModel().select(1);
			
		}
		else
			textFieldFuselageCylinderHeightFromGround.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION HEIGHT TO TOTAL HEIGH RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			textFieldFuselageCylinderSectionHeightRatio.setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionCylinderLowerToTotalHeightRatio()
					.toString()
					);
		else
			textFieldFuselageCylinderSectionHeightRatio.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION RHO UPPER:
		if(Main.getTheAircraft().getFuselage() != null)
			textFieldFuselageCylinderSectionRhoUpper.setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionCylinderRhoUpper()
					.toString()
					);
		else
			textFieldFuselageCylinderSectionRhoUpper.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION RHO LOWER:
		if(Main.getTheAircraft().getFuselage() != null)
			textFieldFuselageCylinderSectionRhoLower.setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionCylinderRhoLower()
					.toString()
					);
		else
			textFieldFuselageCylinderSectionRhoLower.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// TAIL CONE TIP OFFSET:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			textFieldFuselageTailTipOffset.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getHeightT()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getHeightT().getUnit().toString().equalsIgnoreCase("m"))
				fuselageTailTipOffsetZUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getHeightT().getUnit().toString().equalsIgnoreCase("ft"))
				fuselageTailTipOffsetZUnitChoiceBox.getSelectionModel().select(1);
			
		}
		else
			textFieldFuselageTailTipOffset.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// TAIL CONE DX CAP PERCENT:
		if(Main.getTheAircraft().getFuselage() != null)
			textFieldFuselageTailDxCap.setText(
					Double.toString(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getDxTailCapPercent()
							)
					);
		else
			textFieldFuselageTailDxCap.setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// TAIL CONE MID SECTION HEIGHT TO TOTAL HEIGHT RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			textFieldFuselageTailMidSectionHeight.setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionTailMidLowerToTotalHeightRatio()
					.toString()
					);
		else
			textFieldFuselageTailMidSectionHeight.setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// TAIL CONE MID SECTION RHO UPPER:
		if(Main.getTheAircraft().getFuselage() != null)
			textFieldFuselageTailMidRhoUpper.setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionMidTailRhoUpper()
					.toString()
					);
		else
			textFieldFuselageTailMidRhoUpper.setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// TAIL CONE MID SECTION RHO LOWER:
		if(Main.getTheAircraft().getFuselage() != null)
			textFieldFuselageTailMidRhoLower.setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionMidTailRhoLower()
					.toString()
					);
		else
			textFieldFuselageTailMidRhoLower.setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// SPOILERS:
		textFieldSpoilersXInboradList = new ArrayList<>();
		textFieldSpoilersXInboradList.add(textFieldFuselageSpoilerXin1);
		textFieldSpoilersXInboradList.add(textFieldFuselageSpoilerXin2);
		textFieldSpoilersXInboradList.add(textFieldFuselageSpoilerXin3);
		textFieldSpoilersXInboradList.add(textFieldFuselageSpoilerXin4);
		
		textFieldSpoilersXOutboradList = new ArrayList<>();
		textFieldSpoilersXOutboradList.add(textFieldFuselageSpoilerXout1);
		textFieldSpoilersXOutboradList.add(textFieldFuselageSpoilerXout2);
		textFieldSpoilersXOutboradList.add(textFieldFuselageSpoilerXout3);
		textFieldSpoilersXOutboradList.add(textFieldFuselageSpoilerXout4);
		
		textFieldSpoilersYInboradList = new ArrayList<>();
		textFieldSpoilersYInboradList.add(textFieldFuselageSpoilerYin1);
		textFieldSpoilersYInboradList.add(textFieldFuselageSpoilerYin2);
		textFieldSpoilersYInboradList.add(textFieldFuselageSpoilerYin3);
		textFieldSpoilersYInboradList.add(textFieldFuselageSpoilerYin4);
		
		textFieldSpoilersYOutboradList = new ArrayList<>();
		textFieldSpoilersYOutboradList.add(textFieldFuselageSpoilerYout1);
		textFieldSpoilersYOutboradList.add(textFieldFuselageSpoilerYout2);
		textFieldSpoilersYOutboradList.add(textFieldFuselageSpoilerYout3);
		textFieldSpoilersYOutboradList.add(textFieldFuselageSpoilerYout4);
		
		textFieldSpoilersMinimumDeflectionList = new ArrayList<>();
		textFieldSpoilersMinimumDeflectionList.add(textFieldFuselageSpoilerMinDeflection1);
		textFieldSpoilersMinimumDeflectionList.add(textFieldFuselageSpoilerMinDeflection2);
		textFieldSpoilersMinimumDeflectionList.add(textFieldFuselageSpoilerMinDeflection3);
		textFieldSpoilersMinimumDeflectionList.add(textFieldFuselageSpoilerMinDeflection4);
		
		textFieldSpoilersMaximumDeflectionList = new ArrayList<>();
		textFieldSpoilersMaximumDeflectionList.add(textFieldFuselageSpoilerMaxDeflection1);
		textFieldSpoilersMaximumDeflectionList.add(textFieldFuselageSpoilerMaxDeflection2);
		textFieldSpoilersMaximumDeflectionList.add(textFieldFuselageSpoilerMaxDeflection3);
		textFieldSpoilersMaximumDeflectionList.add(textFieldFuselageSpoilerMaxDeflection4);
		
		//..........................................................................................................
		if(!Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().isEmpty()) {
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSpoilers().get(0).getMinimumDeflection().toString().equalsIgnoreCase("°"))
				fuselageSpoilersDeltaMinUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSpoilers().get(0).getMinimumDeflection().toString().equalsIgnoreCase("rad"))
				fuselageSpoilersDeltaMinUnitChoiceBox.getSelectionModel().select(1);


			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSpoilers().get(0).getMaximumDeflection().toString().equalsIgnoreCase("°"))
				fuselageSpoilersDeltaMaxUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSpoilers().get(0).getMaximumDeflection().toString().equalsIgnoreCase("rad"))
				fuselageSpoilersDeltaMaxUnitChoiceBox.getSelectionModel().select(1);

			//..........................................................................................................
			for (int i = 0; i < Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().size(); i++) {
				//..........................................................................................................
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getInnerStationChordwisePosition() != null)
					textFieldSpoilersXInboradList.get(i).setText(
							Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getInnerStationChordwisePosition().toString()
							);
				else
					textFieldSpoilersXInboradList.get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getOuterStationChordwisePosition() != null)
					textFieldSpoilersXOutboradList.get(i).setText(
							Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getOuterStationChordwisePosition().toString()
							);
				else
					textFieldSpoilersXOutboradList.get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getInnerStationSpanwisePosition() != null)
					textFieldSpoilersYInboradList.get(i).setText(
							Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getInnerStationSpanwisePosition().toString()
							);
				else
					textFieldSpoilersYInboradList.get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getOuterStationSpanwisePosition() != null)
					textFieldSpoilersYOutboradList.get(i).setText(
							Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getOuterStationSpanwisePosition().toString()
							);
				else
					textFieldSpoilersYOutboradList.get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getMinimumDeflection() != null)
					textFieldSpoilersMinimumDeflectionList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getMinimumDeflection().getEstimatedValue()
									)
							);
				else
					textFieldSpoilersMinimumDeflectionList.get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getMaximumDeflection() != null)
					textFieldSpoilersMaximumDeflectionList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getMaximumDeflection().getEstimatedValue()
									)
							);
				else
					textFieldSpoilersMaximumDeflectionList.get(i).setText(
							"NOT INITIALIZED"
							);
			}
		}
	}
	
	@FXML
	private void zoomDataLogAndMessagesAircraft() {
		aircraftViewsAndDataLogSplitPane.setDividerPositions(0.5);
	};
	
	@FXML
	private void zoomViewsAircraft() {
		aircraftViewsAndDataLogSplitPane.setDividerPositions(0.9);
	};
	
	@FXML
	private void zoomDataLogAndMessagesFuselage() {
		fuselageViewsAndDataLogSplitPane.setDividerPositions(0.5);
	};
	
	@FXML
	private void zoomViewsFuselage() {
		fuselageViewsAndDataLogSplitPane.setDividerPositions(0.9);
	};
	
}
