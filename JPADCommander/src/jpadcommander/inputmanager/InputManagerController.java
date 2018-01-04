package jpadcommander.inputmanager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.auxiliary.SeatsBlock;
import aircraft.components.Aircraft;
import aircraft.components.CabinConfiguration;
import aircraft.components.CabinConfiguration.ConfigurationBuilder;
import aircraft.components.liftingSurface.creator.AsymmetricFlapCreator;
import aircraft.components.liftingSurface.creator.LiftingSurfacePanelCreator;
import aircraft.components.liftingSurface.creator.SlatCreator;
import aircraft.components.liftingSurface.creator.SpoilerCreator;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.nacelles.NacelleCreator.MountingPosition;
import aircraft.components.powerplant.Engine;
import calculators.geometry.FusNacGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.ClassTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.RegulationsEnum;
import configuration.enumerations.RelativePositionEnum;
import configuration.enumerations.WindshieldTypeEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import graphics.ChartCanvas;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javaslang.Tuple;
import javaslang.Tuple2;
import jpadcommander.Main;
import standaloneutils.GeometryCalc;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMapUtils;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import writers.AircraftSaveDirectives;
import writers.JPADStaticWriteUtils;

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
	private SplitPane cabinConfigurationViewsAndDataLogSplitPane;
	@FXML
	private SplitPane wingViewsAndDataLogSplitPane;
	@FXML
	private SplitPane hTailViewsAndDataLogSplitPane;
	@FXML
	private SplitPane vTailViewsAndDataLogSplitPane;
	@FXML
	private SplitPane canardViewsAndDataLogSplitPane;
	@FXML
	private SplitPane nacelleViewsAndDataLogSplitPane;
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
	private Pane cabinConfigurationSeatMapPane;
	@FXML
	private Pane wingPlanformPane;
	@FXML
	private Pane equivalentWingPane;
	@FXML
	private Pane hTailPlanformPane;
	@FXML
	private Pane vTailPlanformPane;
	@FXML
	private Pane canardPlanformPane;
	@FXML
	private Pane nacelle1FrontViewPane;
	@FXML
	private Pane nacelle1SideViewPane;
	@FXML
	private Pane nacelle1TopViewPane;
	@FXML
	private BorderPane engine1BorderPane;
	@FXML	
	private TextArea textAreaAircraftConsoleOutput;
	@FXML
	private TextArea textAreaFuselageConsoleOutput;
	@FXML
	private TextArea textAreaCabinConfigurationConsoleOutput;
	@FXML
	private TextArea textAreaWingConsoleOutput;
	@FXML
	private TextArea textAreaHTailConsoleOutput;
	@FXML
	private TextArea textAreaVTailConsoleOutput;
	@FXML
	private TextArea textAreaCanardConsoleOutput;
	@FXML
	private TextArea textAreaNacelleConsoleOutput;
	@FXML
	private TextArea textAreaPowerPlantConsoleOutput;
	@FXML
	private TextArea textAreaLandingGearsConsoleOutput;
	@FXML
	private TabPane tabPaneAircraftEngines;
	@FXML
	private TabPane tabPaneAircraftNacelles;
	@FXML
	private TabPane tabPaneFuselageSpoilers;	
	@FXML
	private TabPane tabPaneWingPanels;
	@FXML
	private TabPane tabPaneWingFlaps;
	@FXML
	private TabPane tabPaneWingSlats;
	@FXML
	private TabPane tabPaneWingSpoilers;	
	@FXML
	private TabPane tabPaneWingViewAndAirfoils;	
	@FXML
	private TabPane tabPaneHTailPanels;
	@FXML
	private TabPane tabPaneHTailElevators;
	@FXML
	private TabPane tabPaneHTailViewAndAirfoils;	
	@FXML
	private TabPane tabPaneVTailPanels;
	@FXML
	private TabPane tabPaneVTailRudders;
	@FXML
	private TabPane tabPaneVTailViewAndAirfoils;	
	@FXML
	private TabPane tabPaneCanardPanels;
	@FXML
	private TabPane tabPaneCanardControlSurfaces;
	@FXML
	private TabPane tabPaneCanardViewAndAirfoils;	
	@FXML
	private TabPane tabPaneNacelles;	
	@FXML
	private TabPane tabPaneNacellesTopViews;
	@FXML
	private TabPane tabPaneNacellesSideViews;
	@FXML
	private TabPane tabPaneNacellesFrontViews;
	@FXML
	private TabPane tabPaneEngines;	
	//...........................................................................................
	// BUTTONS:
	//...........................................................................................
	@FXML
	private Button loadAircraftButton;
	@FXML
	private Button newAircraftButton;
	@FXML
	private Button updateGeometryButton;
	@FXML
	private Button saveAircraftButton;
	@FXML
	private Button aircraftChooseCabinConfigurationFileButton;
	@FXML
	private Button aircraftChooseFuselageFileButton;
	@FXML
	private Button aircraftChooseWingFileButton;
	@FXML
	private Button aircraftChooseHTailFileButton;
	@FXML
	private Button aircraftChooseVTailFileButton;
	@FXML
	private Button aircraftChooseCanardFileButton;
	@FXML
	private Button aircraftChooseEngine1FileButton;
	@FXML
	private Button aircraftChooseNacelle1FileButton;
	@FXML
	private Button aircraftChooseLandingGearsFileButton;
	@FXML
	private Button aircraftChooseSystemsFileButton;
	@FXML
	private Button aircraftAddEngineButton;
	@FXML
	private Button aircraftAddNacelleButton;
	@FXML
	private Button fuselageAddSpoilerButton;
	@FXML
	private Button missingSeatRowCabinConfigurationInfoButton;
	@FXML
	private Button referenceMassCabinConfigurationInfoButton;
	@FXML
	private Button equivalentWingInfoButton;
	@FXML
	private Button equivalentWingRootXOffsetLEInfoButton;
	@FXML
	private Button equivalentWingRootXOffseTLEInfoButton;
	@FXML
	private Button equivalentWingAirfoilRootDetailButton;
	@FXML
	private Button equivalentWingChooseAirfoilRootButton;
	@FXML
	private Button equivalentWingAirfoilKinkDetailButton;
	@FXML
	private Button equivalentWingChooseAirfoilKinkButton;
	@FXML
	private Button equivalentWingAirfoilTipDetailButton;
	@FXML
	private Button equivalentWingChooseAirfoilTipButton;
	@FXML
	private Button wingAddPanelButton;
	@FXML
	private Button wingInnerSectionAirfoilDetailsPanel1Button;
	@FXML
	private Button wingOuterSectionAirfoilDetailsPanel1Button;
	@FXML
	private Button wingAddFlapButton;
	@FXML
	private Button wingAddSlatButton;
	@FXML
	private Button wingAddSpoilerButton;
	@FXML
	private Button wingChooseInnerAirfoilPanel1Button;
	@FXML
	private Button wingChooseOuterAirfoilPanel1Button;
	@FXML
	private Button hTailAddPanelButton;
	@FXML
	private Button hTailInnerSectionAirfoilDetailsPanel1Button;
	@FXML
	private Button hTailOuterSectionAirfoilDetailsPanel1Button;
	@FXML
	private Button hTailAddElevatorButton;
	@FXML
	private Button hTailChooseInnerAirfoilPanel1Button;
	@FXML
	private Button hTailChooseOuterAirfoilPanel1Button;
	@FXML
	private Button vTailAddPanelButton;
	@FXML
	private Button vTailInnerSectionAirfoilDetailsPanel1Button;
	@FXML
	private Button vTailOuterSectionAirfoilDetailsPanel1Button;
	@FXML
	private Button vTailAddRudderButton;
	@FXML
	private Button vTailChooseInnerAirfoilPanel1Button;
	@FXML
	private Button vTailChooseOuterAirfoilPanel1Button;
	@FXML
	private Button canardAddPanelButton;
	@FXML
	private Button canardInnerSectionAirfoilDetailsPanel1Button;
	@FXML
	private Button canardOuterSectionAirfoilDetailsPanel1Button;
	@FXML
	private Button canardAddControlSurfaceButton;
	@FXML
	private Button canardChooseInnerAirfoilPanel1Button;
	@FXML
	private Button canardChooseOuterAirfoilPanel1Button;
	@FXML
	private Button nacelleEstimateGeometryButton1;
	@FXML
	private Button nacelleKInletInfoButton1;
	@FXML
	private Button nacelleKOutletInfoButton1;
	@FXML
	private Button nacelleKLengthInfoButton1;
	@FXML
	private Button nacelleKDiameterOutletInfoButton1;
	@FXML
	private Button landingGearsKMainLegLengthInfoButton;
	
	//...........................................................................................
	// BUTTON MAP:
	//...........................................................................................
	Map<Button, Integer> wingAirfoilDetailsButtonAndTabsMap = new HashMap<>();
	Map<Button, Integer> hTailAirfoilDetailsButtonAndTabsMap = new HashMap<>();
	Map<Button, Integer> vTailAirfoilDetailsButtonAndTabsMap = new HashMap<>();
	Map<Button, Integer> canardAirfoilDetailsButtonAndTabsMap = new HashMap<>();
	
	//...........................................................................................
	// FILE CHOOSER:
	//...........................................................................................
	private FileChooser aircraftFileChooser;
	@SuppressWarnings("unused")
	private FileChooser airfoilFileChooser;
	private FileChooser engineDatabaseFileChooser;
	private FileChooser saveAircraftFileChooser;
	private FileChooser cabinConfigurationFileChooser;
	private FileChooser fuselageFileChooser;
	private FileChooser wingFileChooser;
	private FileChooser hTailFileChooser;
	private FileChooser vTailFileChooser;
	private FileChooser canardFileChooser;
	private FileChooser engineFileChooser;
	private FileChooser nacelleFileChooser;
	private FileChooser landingGearsFileChooser;
	private FileChooser systemsFileChooser;
	
	//...........................................................................................
	// VALIDATIONS (ControlsFX):
	//...........................................................................................
	private ValidationSupport validation = new ValidationSupport();
	
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
			"MODIFY TOTAL LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS",
			"MODIFY TOTAL LENGTH, CONSTANT FINENESS-RATIOS",	
			"MODIFY CYLINDER LENGTH (streching)",                            
			"MODIFY NOSE LENGTH, CONSTANT TOTAL LENGTH AND DIAMETERS",
			"MODIFY NOSE LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS",
			"MODIFY NOSE LENGTH, CONSTANT FINENESS-RATIOS",
			"MODIFY TAILCONE LENGTH, CONSTANT TOTAL LENGTH AND DIAMETERS",
			"MODIFY TAILCONE LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS",
			"MODIFY TAILCONE LENGTH, CONSTANT FINENESS-RATIOS",
			"MODIFY FUSELAGE DIAMETER, CONSTANT FINENESS-RATIOS"
			);
	ObservableList<String> liftingSurfaceAdjustCriteriaTypeList = FXCollections.observableArrayList(
			"NONE",
			"MODIFY AR, SPAN AND ROOT CHORD",
			"MODIFY AR, SPAN AND TIP CHORD",
			"MODIFY AR, SPAN AND TAPER-RATIO",
			"MODIFY AR, AREA AND ROOT CHORD",
			"MODIFY AR, AREA AND TIP CHORD",
			"MODIFY AR, AREA AND TAPER-RATIO", 
			"MODIFY AR, ROOT CHORD AND TIP CHORD",
			"MODIFY AR, ROOT CHORD AND TAPER-RATIO",
			"MODIFY AR, TIP CHORD AND TAPER-RATIO",
			"MODIFY SPAN, AREA AND ROOT CHORD",
			"MODIFY SPAN, AREA AND TIP CHORD",
			"MODIFY SPAN, AREA AND TAPER-RATIO",
			"MODIFY SPAN, ROOT CHORD AND TIP CHORD", 
			"MODIFY SPAN, ROOT CHORD AND TAPER-RATIO",
			"MODIFY SPAN, TIP CHORD AND TAPER-RATIO",
			"MODIFY AREA, ROOT CHORD AND TIP CHORD",
			"MODIFY AREA, ROOT CHORD AND TAPER-RATIO",
			"MODIFY AREA, TIP CHORD AND TAPER-RATIO"
			);
	ObservableList<String> cabinConfigurationClassesTypeList = FXCollections.observableArrayList(
			"ECONOMY",
			"BUSINESS",
			"FIRST"
			);
	ObservableList<String> engineTypeList = FXCollections.observableArrayList(
			"PISTON",
			"TURBOPROP",
			"TURBOFAN",
			"TURBOJET"
			);
	ObservableList<String> flapTypeList = FXCollections.observableArrayList(
			"SINGLE_SLOTTED",
			"DOUBLE_SLOTTED",
			"PLAIN",
			"FOWLER",
			"OPTIMIZED_FOWLER",
			"TRIPLE_SLOTTED"
			);
	ObservableList<String> aileronTypeList = FXCollections.observableArrayList(
			"PLAIN"
			);
	ObservableList<String> elevatorTypeList = FXCollections.observableArrayList(
			"PLAIN"
			);
	ObservableList<String> rudderTypeList = FXCollections.observableArrayList(
			"PLAIN"
			);
	ObservableList<String> canardSurfaceTypeList = FXCollections.observableArrayList(
			"PLAIN"
			);
	ObservableList<String> lengthUnitsList = FXCollections.observableArrayList(
			"m",
			"ft"
			);
	ObservableList<String> angleUnitsList = FXCollections.observableArrayList(
			"°",
			"rad" 
			);
	ObservableList<String> massUnitsList = FXCollections.observableArrayList(
			"kg",
			"lb" 
			);
	ObservableList<String> areaUnitsList = FXCollections.observableArrayList(
			"m²",
			"ft²" 
			);
	ObservableList<String> forceUnitsList = FXCollections.observableArrayList(
			"N",
			"lbf" 
			);
	ObservableList<String> powerUnitsList = FXCollections.observableArrayList(
			"W",
			"hp" 
			);
	
	//...........................................................................................
	// AIRCRAFT TAB (CHOICE BOX):
	//...........................................................................................
	// Choice Box
	@FXML
	private ChoiceBox<String> aircraftTypeChoiceBox;
	@FXML
	private ChoiceBox<String> regulationsTypeChoiceBox;
	@FXML
	private ChoiceBox<String> engineMountingPositionTypeChoiceBox1;
	@FXML
	private ChoiceBox<String> nacelleMountingPositionTypeChoiceBox1;
	@FXML
	private ChoiceBox<String> landingGearsMountingPositionTypeChoiceBox;
	
	private List<TextField> textFieldsAircraftEngineFileList;
	private List<TextField> textFieldAircraftEngineXList;
	private List<TextField> textFieldAircraftEngineYList;
	private List<TextField> textFieldAircraftEngineZList;
	private List<ChoiceBox<String>> choiceBoxesAircraftEnginePositonList;
	private List<TextField> textFieldAircraftEngineTiltList;
	private List<ChoiceBox<String>> choiceBoxAircraftEngineXUnitList;
	private List<ChoiceBox<String>> choiceBoxAircraftEngineYUnitList;
	private List<ChoiceBox<String>> choiceBoxAircraftEngineZUnitList;
	private List<ChoiceBox<String>> choiceBoxAircraftEngineTiltUnitList;
	private List<Button> chooseEngineFileButtonList;
	
	private List<TextField> textFieldsAircraftNacelleFileList;
	private List<TextField> textFieldAircraftNacelleXList;
	private List<TextField> textFieldAircraftNacelleYList;
	private List<TextField> textFieldAircraftNacelleZList;
	private List<ChoiceBox<String>> choiceBoxesAircraftNacellePositonList;
	private List<ChoiceBox<String>> choiceBoxAircraftNacelleXUnitList;
	private List<ChoiceBox<String>> choiceBoxAircraftNacelleYUnitList;
	private List<ChoiceBox<String>> choiceBoxAircraftNacelleZUnitList;
	private List<Button> chooseNacelleFileButtonList;
	
	//...........................................................................................
	// AIRCRAFT TAB (DATA):
	//...........................................................................................
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
	private TextField textFieldAircraftEngineX1;
	@FXML
	private TextField textFieldAircraftEngineY1;
	@FXML
	private TextField textFieldAircraftEngineZ1;
	@FXML
	private TextField textFieldAircraftEngineTilt1;
	@FXML
	private TextField textFieldAircraftNacelleFile1;
	@FXML
	private TextField textFieldAircraftNacelleX1;
	@FXML
	private TextField textFieldAircraftNacelleY1;
	@FXML
	private TextField textFieldAircraftNacelleZ1;
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
	private ChoiceBox<String> engineX1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> engineY1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> engineZ1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> engineTilt1AngleUnitChoiceBox;
	@FXML
	private ChoiceBox<String> nacelleX1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> nacelleY1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> nacelleZ1UnitChoiceBox;
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
	private TextField textFieldFuselageSpoilerXout1;
	@FXML
	private TextField textFieldFuselageSpoilerYin1;
	@FXML
	private TextField textFieldFuselageSpoilerYout1;
	@FXML
	private TextField textFieldFuselageSpoilerMinDeflection1;
	@FXML
	private TextField textFieldFuselageSpoilerMaxDeflection1;
	
	// spoilers
	private List<TextField> textFieldFuselageInnerSpanwisePositionSpoilerList;
	private List<TextField> textFieldFuselageOuterSpanwisePositionSpoilerList;
	private List<TextField> textFieldFuselageInnerChordwisePositionSpoilerList;
	private List<TextField> textFieldFuselageOuterChordwisePositionSpoilerList;
	private List<TextField> textFieldFuselageMinimumDeflectionAngleSpoilerList;
	private List<TextField> textFieldFuselageMaximumDeflectionAngleSpoilerList;
	private List<ChoiceBox<String>> choiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList;
	private List<ChoiceBox<String>> choiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList;
	
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
	private ChoiceBox<String> fuselageSpoiler1DeltaMinUnitChoiceBox;
	@FXML
	private ChoiceBox<String> fuselageSpoiler1DeltaMaxUnitChoiceBox;
	
	//...........................................................................................
	// CABIN CONFIGURATION TAB (DATA):
	//...........................................................................................
	@FXML
	private TextField textFieldActualPassengersNumber;
	@FXML
	private TextField textFieldMaximumPassengersNumber;
	@FXML
	private TextField textFieldFlightCrewNumber;
	@FXML
	private TextField textFieldClassesNumber;
	@FXML
	private ChoiceBox<String> cabinConfigurationClassesTypeChoiceBox1;
	@FXML
	private ChoiceBox<String> cabinConfigurationClassesTypeChoiceBox2;
	@FXML
	private ChoiceBox<String> cabinConfigurationClassesTypeChoiceBox3;
	@FXML
	private TextField textFieldAislesNumber;
	@FXML
	private TextField textFieldXCoordinateFirstRow;
	@FXML
	private TextField textFieldMissingSeatRow1;
	@FXML
	private TextField textFieldMissingSeatRow2;
	@FXML
	private TextField textFieldMissingSeatRow3;
	@FXML
	private TextField textFieldNumberOfBrakesEconomy;
	@FXML
	private TextField textFieldNumberOfBrakesBusiness;
	@FXML
	private TextField textFieldNumberOfBrakesFirst;
	@FXML
	private TextField textFieldNumberOfRowsEconomy;
	@FXML
	private TextField textFieldNumberOfRowsBusiness;
	@FXML
	private TextField textFieldNumberOfRowsFirst;
	@FXML
	private TextField textFieldNumberOfColumnsEconomy;
	@FXML
	private TextField textFieldNumberOfColumnsBusiness;
	@FXML
	private TextField textFieldNumberOfColumnsFirst;
	@FXML
	private TextField textFieldSeatsPitchEconomy;
	@FXML
	private TextField textFieldSeatsPitchBusiness;
	@FXML
	private TextField textFieldSeatsPitchFirst;
	@FXML
	private TextField textFieldSeatsWidthEconomy;
	@FXML
	private TextField textFieldSeatsWidthBusiness;
	@FXML
	private TextField textFieldSeatsWidthFirst;
	@FXML
	private TextField textFieldDistanceFromWallEconomy;
	@FXML
	private TextField textFieldDistanceFromWallBusiness;
	@FXML
	private TextField textFieldDistanceFromWallFirst;
	@FXML
	private TextField textFieldMassFurnishingsAndEquipment;
	
	//...........................................................................................
	// CABIN CONFIGURATION TAB (UNITS):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> cabinConfigurationXCoordinateFirstRowUnitChoiceBox;
	@FXML
	private ChoiceBox<String> cabinConfigurationSeatsPitchEconomyUnitChoiceBox;
	@FXML
	private ChoiceBox<String> cabinConfigurationSeatsPitchBusinessUnitChoiceBox;
	@FXML
	private ChoiceBox<String> cabinConfigurationSeatsPitchFirstUnitChoiceBox;
	@FXML
	private ChoiceBox<String> cabinConfigurationSeatsWidthEconomyUnitChoiceBox;
	@FXML
	private ChoiceBox<String> cabinConfigurationSeatsWidthBusinessUnitChoiceBox;
	@FXML
	private ChoiceBox<String> cabinConfigurationSeatsWidthFirstUnitChoiceBox;
	@FXML
	private ChoiceBox<String> cabinConfigurationDistanceFromWallEconomyUnitChoiceBox;
	@FXML
	private ChoiceBox<String> cabinConfigurationDistanceFromWallBusinessUnitChoiceBox;
	@FXML
	private ChoiceBox<String> cabinConfigurationDistanceFromWallFirstUnitChoiceBox;
	@FXML
	private ChoiceBox<String> cabinConfigurationMassFurnishingsAndEquipmentUnitChoiceBox;
	
	//...........................................................................................
	// WING TAB (DATA):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> wingAdjustCriterionChoiceBox;
	@FXML
	private CheckBox equivalentWingCheckBox;
	@FXML
	private TextField textFieldWingMainSparAdimensionalPosition;
	@FXML
	private TextField textFieldWingSecondarySparAdimensionalPosition;
	@FXML
	private TextField textFieldWingCompositeMassCorrectionFactor;
	@FXML
	private TextField textFieldWingRoughness;
	@FXML
	private TextField textFieldWingWingletHeight;
	@FXML
	private TextField textFieldEquivalentWingArea;
	@FXML
	private TextField textFieldEquivalentWingAspectRatio;
	@FXML
	private TextField textFieldEquivalentWingKinkPosition;
	@FXML
	private TextField textFieldEquivalentWingSweepLeadingEdge;
	@FXML
	private TextField textFieldEquivalentWingTwistAtTip;
	@FXML
	private TextField textFieldEquivalentWingDihedral;
	@FXML
	private TextField textFieldEquivalentWingTaperRatio;
	@FXML
	private TextField textFieldEquivalentWingRootXOffsetLE;
	@FXML
	private TextField textFieldEquivalentWingRootXOffsetTE;
	@FXML
	private TextField textFieldEquivalentWingAirfoilRootPath;
	@FXML
	private TextField textFieldEquivalentWingAirfoilKinkPath;
	@FXML
	private TextField textFieldEquivalentWingAirfoilTipPath;
	@FXML
	private TextField textFieldWingSpanPanel1;
	@FXML
	private TextField textFieldWingSweepLeadingEdgePanel1;
	@FXML
	private TextField textFieldWingDihedralPanel1;
	@FXML
	private TextField textFieldWingChordInnerSectionPanel1;
	@FXML
	private TextField textFieldWingTwistInnerSectionPanel1;
	@FXML
	private TextField textFieldWingAirfoilPathInnerSectionPanel1;
	@FXML
	private TextField textFieldWingChordOuterSectionPanel1;
	@FXML
	private TextField textFieldWingTwistOuterSectionPanel1;
	@FXML
	private TextField textFieldWingAirfoilPathOuterSectionPanel1;
	@FXML
	private ChoiceBox<String> wingFlap1TypeChoichBox;
	@FXML
	private TextField textFieldWingInnerPositionFlap1;
	@FXML
	private TextField textFieldWingOuterPositionFlap1;
	@FXML
	private TextField textFieldWingInnerChordRatioFlap1;
	@FXML
	private TextField textFieldWingOuterChordRatioFlap1;
	@FXML
	private TextField textFieldWingMinimumDeflectionAngleFlap1;
	@FXML
	private TextField textFieldWingMaximumDeflectionAngleFlap1;
	@FXML
	private TextField textFieldWingInnerPositionSlat1;
	@FXML
	private TextField textFieldWingOuterPositionSlat1;
	@FXML
	private TextField textFieldWingInnerChordRatioSlat1;
	@FXML
	private TextField textFieldWingOuterChordRatioSlat1;
	@FXML
	private TextField textFieldWingExtensionRatioSlat1;
	@FXML
	private TextField textFieldWingMinimumDeflectionAngleSlat1;
	@FXML
	private TextField textFieldWingMaximumDeflectionAngleSlat1;
	@FXML
	private ChoiceBox<String> wingLeftAileronTypeChoichBox;
	@FXML
	private TextField textFieldWingInnerPositionAileronLeft;
	@FXML
	private TextField textFieldWingOuterPositionAileronLeft;
	@FXML
	private TextField textFieldWingInnerChordRatioAileronLeft;
	@FXML
	private TextField textFieldWingOuterChordRatioAileronLeft;
	@FXML
	private TextField textFieldWingMinimumDeflectionAngleAileronLeft;
	@FXML
	private TextField textFieldWingMaximumDeflectionAngleAileronLeft;
	@FXML
	private ChoiceBox<String> wingRightAileronTypeChoichBox;
	@FXML
	private TextField textFieldWingInnerPositionAileronRight;
	@FXML
	private TextField textFieldWingOuterPositionAileronRight;
	@FXML
	private TextField textFieldWingInnerChordRatioAileronRight;
	@FXML
	private TextField textFieldWingOuterChordRatioAileronRight;
	@FXML
	private TextField textFieldWingMinimumDeflectionAngleAileronRight;
	@FXML
	private TextField textFieldWingMaximumDeflectionAngleAileronRight;
	@FXML
	private TextField textFieldWingInnerSpanwisePositionSpolier1;
	@FXML
	private TextField textFieldWingOuterSpanwisePositionSpolier1;
	@FXML
	private TextField textFieldWingInnerChordwisePositionSpolier1;
	@FXML
	private TextField textFieldWingOuterChordwisePositionSpolier1;
	@FXML
	private TextField textFieldWingMinimumDeflectionAngleSpolier1;
	@FXML
	private TextField textFieldWingMaximumDeflectionAngleSpoiler1;
	
	// lists:
	// panels
	private List<TextField> textFieldWingSpanPanelList;
	private List<TextField> textFieldWingSweepLEPanelList;
	private List<TextField> textFieldWingDihedralPanelList;
	private List<TextField> textFieldWingInnerChordPanelList;
	private List<TextField> textFieldWingInnerTwistPanelList;
	private List<TextField> textFieldWingInnerAirfoilPanelList;
	private List<TextField> textFieldWingOuterChordPanelList;
	private List<TextField> textFieldWingOuterTwistPanelList;
	private List<TextField> textFieldWingOuterAirfoilPanelList;
	private List<CheckBox> checkBoxWingLinkedToPreviousPanelList;
	private List<ChoiceBox<String>> choiceBoxWingSpanPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxWingSweepLEPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxWingDihedralPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxWingInnerChordPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxWingInnerTwistPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxWingOuterChordPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxWingOuterTwistPanelUnitList;
	private List<Button> detailButtonWingInnerAirfoilList;
	private List<Button> detailButtonWingOuterAirfoilList;
	private List<Button> chooseInnerWingAirfoilFileButtonList;
	private List<Button> chooseOuterWingAirfoilFileButtonList;
	
	// flaps
	private List<ChoiceBox<String>> choiceBoxWingFlapTypeList;
	private List<TextField> textFieldWingInnerPositionFlapList;
	private List<TextField> textFieldWingOuterPositionFlapList;
	private List<TextField> textFieldWingInnerChordRatioFlapList;
	private List<TextField> textFieldWingOuterChordRatioFlapList;
	private List<TextField> textFieldWingMinimumDeflectionAngleFlapList;
	private List<TextField> textFieldWingMaximumDeflectionAngleFlapList;
	private List<ChoiceBox<String>> choiceBoxWingMinimumDeflectionAngleFlapUnitList;
	private List<ChoiceBox<String>> choiceBoxWingMaximumDeflectionAngleFlapUnitList;
	
	// slats
	private List<TextField> textFieldWingInnerPositionSlatList;
	private List<TextField> textFieldWingOuterPositionSlatList;
	private List<TextField> textFieldWingInnerChordRatioSlatList;
	private List<TextField> textFieldWingOuterChordRatioSlatList;
	private List<TextField> textFieldWingExtensionRatioSlatList;
	private List<TextField> textFieldWingMinimumDeflectionAngleSlatList;
	private List<TextField> textFieldWingMaximumDeflectionAngleSlatList;
	private List<ChoiceBox<String>> choiceBoxWingMinimumDeflectionAngleSlatUnitList;
	private List<ChoiceBox<String>> choiceBoxWingMaximumDeflectionAngleSlatUnitList;
	
	// spoilers
	private List<TextField> textFieldWingInnerSpanwisePositionSpoilerList;
	private List<TextField> textFieldWingOuterSpanwisePositionSpoilerList;
	private List<TextField> textFieldWingInnerChordwisePositionSpoilerList;
	private List<TextField> textFieldWingOuterChordwisePositionSpoilerList;
	private List<TextField> textFieldWingMinimumDeflectionAngleSpoilerList;
	private List<TextField> textFieldWingMaximumDeflectionAngleSpoilerList;
	private List<ChoiceBox<String>> choiceBoxWingMinimumDeflectionAngleSpoilerUnitList;
	private List<ChoiceBox<String>> choiceBoxWingMaximumDeflectionAngleSpoilerUnitList;
	
	//...........................................................................................
	// WING TAB (UNITS):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> wingRoughnessUnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingWingletHeightUnitChoiceBox;
	@FXML
	private ChoiceBox<String> equivalentWingAreaUnitChoiceBox;
	@FXML
	private ChoiceBox<String> equivalentWingSweepLEUnitChoiceBox;
	@FXML
	private ChoiceBox<String> equivalentWingTwistAtTipUnitChoiceBox;
	@FXML
	private ChoiceBox<String> equivalentWingDihedralUnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingSpanPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingSweepLEPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingDihedralPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingInnerSectionChordPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingInnerSectionTwistTipPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingOuterSectionChordPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingOuterSectionTwistTipPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingMinimumDeflectionAngleFlap1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingMaximumDeflectionAngleFlap1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingMinimumDeflectionAngleSlat1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingMaximumDeflectionAngleSlat1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingMinimumDeflectionAngleAileronLeftUnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingMaximumDeflectionAngleAileronLeftUnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingMinimumDeflectionAngleAileronRigthUnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingMaximumDeflectionAngleAileronRightUnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingMinimumDeflectionAngleSpoiler1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> wingMaximumDeflectionAngleSpoiler1UnitChoiceBox;
	
	//...........................................................................................
	// HTAIL TAB (DATA):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> hTailAdjustCriterionChoiceBox;
	@FXML
	private TextField textFieldHTailCompositeMassCorrectionFactor;
	@FXML
	private TextField textFieldHTailRoughness;
	@FXML
	private TextField textFieldHTailSpanPanel1;
	@FXML
	private TextField textFieldHTailSweepLeadingEdgePanel1;
	@FXML
	private TextField textFieldHTailDihedralPanel1;
	@FXML
	private TextField textFieldHTailChordInnerSectionPanel1;
	@FXML
	private TextField textFieldHTailTwistInnerSectionPanel1;
	@FXML
	private TextField textFieldHTailAirfoilPathInnerSectionPanel1;
	@FXML
	private TextField textFieldHTailChordOuterSectionPanel1;
	@FXML
	private TextField textFieldHTailTwistOuterSectionPanel1;
	@FXML
	private TextField textFieldHTailAirfoilPathOuterSectionPanel1;
	@FXML
	private ChoiceBox<String> hTailElevator1TypeChoiceBox;
	@FXML
	private TextField textFieldHTailInnerPositionElevator1;
	@FXML
	private TextField textFieldHTailOuterPositionElevator1;
	@FXML
	private TextField textFieldHTailInnerChordRatioElevator1;
	@FXML
	private TextField textFieldHTailOuterChordRatioElevator1;
	@FXML
	private TextField textFieldHTailMinimumDeflectionAngleElevator1;
	@FXML
	private TextField textFieldHTailMaximumDeflectionAngleElevator1;
	
	// lists:
	// panels
	private List<TextField> textFieldHTailSpanPanelList;
	private List<TextField> textFieldHTailSweepLEPanelList;
	private List<TextField> textFieldHTailDihedralPanelList;
	private List<TextField> textFieldHTailInnerChordPanelList;
	private List<TextField> textFieldHTailInnerTwistPanelList;
	private List<TextField> textFieldHTailInnerAirfoilPanelList;
	private List<TextField> textFieldHTailOuterChordPanelList;
	private List<TextField> textFieldHTailOuterTwistPanelList;
	private List<TextField> textFieldHTailOuterAirfoilPanelList;
	private List<CheckBox> checkBoxHTailLinkedToPreviousPanelList;
	private List<ChoiceBox<String>> choiceBoxHTailSpanPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxHTailSweepLEPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxHTailDihedralPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxHTailInnerChordPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxHTailInnerTwistPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxHTailOuterChordPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxHTailOuterTwistPanelUnitList;
	private List<Button> detailButtonHTailInnerAirfoilList;
	private List<Button> detailButtonHTailOuterAirfoilList;
	private List<Button> chooseInnerHTailAirfoilFileButtonList;
	private List<Button> chooseOuterHTailAirfoilFileButtonList;
	
	// elevators
	private List<ChoiceBox<String>> choiceBoxHTailElevatorTypeList;
	private List<TextField> textFieldHTailInnerPositionElevatorList;
	private List<TextField> textFieldHTailOuterPositionElevatorList;
	private List<TextField> textFieldHTailInnerChordRatioElevatorList;
	private List<TextField> textFieldHTailOuterChordRatioElevatorList;
	private List<TextField> textFieldHTailMinimumDeflectionAngleElevatorList;
	private List<TextField> textFieldHTailMaximumDeflectionAngleElevatorList;
	private List<ChoiceBox<String>> choiceBoxHTailMinimumDeflectionAngleElevatorUnitList;
	private List<ChoiceBox<String>> choiceBoxHTailMaximumDeflectionAngleElevatorUnitList;
	
	//...........................................................................................
	// HTAIL TAB (UNITS):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> hTailRoughnessUnitChoiceBox;
	@FXML
	private ChoiceBox<String> hTailSpanPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> hTailSweepLEPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> hTailDihedralPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> hTailInnerSectionChordPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> hTailInnerSectionTwistTipPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> hTailOuterSectionChordPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> hTailOuterSectionTwistTipPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> hTailMinimumDeflectionAngleElevator1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> hTailMaximumDeflectionAngleElevator1UnitChoiceBox;
	
	//...........................................................................................
	// VTAIL TAB (DATA):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> vTailAdjustCriterionChoiceBox;
	@FXML
	private TextField textFieldVTailCompositeMassCorrectionFactor;
	@FXML
	private TextField textFieldVTailRoughness;
	@FXML
	private TextField textFieldVTailSpanPanel1;
	@FXML
	private TextField textFieldVTailSweepLeadingEdgePanel1;
	@FXML
	private TextField textFieldVTailDihedralPanel1;
	@FXML
	private TextField textFieldVTailChordInnerSectionPanel1;
	@FXML
	private TextField textFieldVTailTwistInnerSectionPanel1;
	@FXML
	private TextField textFieldVTailAirfoilPathInnerSectionPanel1;
	@FXML
	private TextField textFieldVTailChordOuterSectionPanel1;
	@FXML
	private TextField textFieldVTailTwistOuterSectionPanel1;
	@FXML
	private TextField textFieldVTailAirfoilPathOuterSectionPanel1;
	@FXML
	private ChoiceBox<String> vTailRudder1TypeChoiceBox;
	@FXML
	private TextField textFieldVTailInnerPositionRudder1;
	@FXML
	private TextField textFieldVTailOuterPositionRudder1;
	@FXML
	private TextField textFieldVTailInnerChordRatioRudder1;
	@FXML
	private TextField textFieldVTailOuterChordRatioRudder1;
	@FXML
	private TextField textFieldVTailMinimumDeflectionAngleRudder1;
	@FXML
	private TextField textFieldVTailMaximumDeflectionAngleRudder1;
	
	// lists:
	// panels
	private List<TextField> textFieldVTailSpanPanelList;
	private List<TextField> textFieldVTailSweepLEPanelList;
	private List<TextField> textFieldVTailDihedralPanelList;
	private List<TextField> textFieldVTailInnerChordPanelList;
	private List<TextField> textFieldVTailInnerTwistPanelList;
	private List<TextField> textFieldVTailInnerAirfoilPanelList;
	private List<TextField> textFieldVTailOuterChordPanelList;
	private List<TextField> textFieldVTailOuterTwistPanelList;
	private List<TextField> textFieldVTailOuterAirfoilPanelList;
	private List<CheckBox> checkBoxVTailLinkedToPreviousPanelList;
	private List<ChoiceBox<String>> choiceBoxVTailSpanPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxVTailSweepLEPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxVTailDihedralPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxVTailInnerChordPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxVTailInnerTwistPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxVTailOuterChordPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxVTailOuterTwistPanelUnitList;
	private List<Button> detailButtonVTailInnerAirfoilList;
	private List<Button> detailButtonVTailOuterAirfoilList;
	private List<Button> chooseInnerVTailAirfoilFileButtonList;
	private List<Button> chooseOuterVTailAirfoilFileButtonList;
	
	// rudders
	private List<ChoiceBox<String>> choiceBoxVTailRudderTypeList;
	private List<TextField> textFieldVTailInnerPositionRudderList;
	private List<TextField> textFieldVTailOuterPositionRudderList;
	private List<TextField> textFieldVTailInnerChordRatioRudderList;
	private List<TextField> textFieldVTailOuterChordRatioRudderList;
	private List<TextField> textFieldVTailMinimumDeflectionAngleRudderList;
	private List<TextField> textFieldVTailMaximumDeflectionAngleRudderList;
	private List<ChoiceBox<String>> choiceBoxVTailMinimumDeflectionAngleRudderUnitList;
	private List<ChoiceBox<String>> choiceBoxVTailMaximumDeflectionAngleRudderUnitList;
	
	//...........................................................................................
	// VTAIL TAB (UNITS):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> vTailRoughnessUnitChoiceBox;
	@FXML
	private ChoiceBox<String> vTailSpanPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> vTailSweepLEPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> vTailDihedralPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> vTailInnerSectionChordPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> vTailInnerSectionTwistTipPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> vTailOuterSectionChordPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> vTailOuterSectionTwistTipPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> vTailMinimumDeflectionAngleRudder1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> vTailMaximumDeflectionAngleRudder1UnitChoiceBox;
	
	//...........................................................................................
	// CANARD TAB (DATA):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> canardAdjustCriterionChoiceBox;
	@FXML
	private TextField textFieldCanardCompositeMassCorrectionFactor;
	@FXML
	private TextField textFieldCanardRoughness;
	@FXML
	private TextField textFieldCanardSpanPanel1;
	@FXML
	private TextField textFieldCanardSweepLeadingEdgePanel1;
	@FXML
	private TextField textFieldCanardDihedralPanel1;
	@FXML
	private TextField textFieldCanardChordInnerSectionPanel1;
	@FXML
	private TextField textFieldCanardTwistInnerSectionPanel1;
	@FXML
	private TextField textFieldCanardAirfoilPathInnerSectionPanel1;
	@FXML
	private TextField textFieldCanardChordOuterSectionPanel1;
	@FXML
	private TextField textFieldCanardTwistOuterSectionPanel1;
	@FXML
	private TextField textFieldCanardAirfoilPathOuterSectionPanel1;
	@FXML
	private ChoiceBox<String> canardControlSurface1TypeChoiceBox;
	@FXML
	private TextField textFieldCanardInnerPositionControlSurface1;
	@FXML
	private TextField textFieldCanardOuterPositionControlSurface1;
	@FXML
	private TextField textFieldCanardInnerChordRatioControlSurface1;
	@FXML
	private TextField textFieldCanardOuterChordRatioControlSurface1;
	@FXML
	private TextField textFieldCanardMinimumDeflectionAngleControlSurface1;
	@FXML
	private TextField textFieldCanardMaximumDeflectionAngleControlSurface1;
	
	// lists:
	// panels
	private List<TextField> textFieldCanardSpanPanelList;
	private List<TextField> textFieldCanardSweepLEPanelList;
	private List<TextField> textFieldCanardDihedralPanelList;
	private List<TextField> textFieldCanardInnerChordPanelList;
	private List<TextField> textFieldCanardInnerTwistPanelList;
	private List<TextField> textFieldCanardInnerAirfoilPanelList;
	private List<TextField> textFieldCanardOuterChordPanelList;
	private List<TextField> textFieldCanardOuterTwistPanelList;
	private List<TextField> textFieldCanardOuterAirfoilPanelList;
	private List<CheckBox> checkBoxCanardLinkedToPreviousPanelList;
	private List<ChoiceBox<String>> choiceBoxCanardSpanPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxCanardSweepLEPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxCanardDihedralPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxCanardInnerChordPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxCanardInnerTwistPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxCanardOuterChordPanelUnitList;
	private List<ChoiceBox<String>> choiceBoxCanardOuterTwistPanelUnitList;
	private List<Button> detailButtonCanardInnerAirfoilList;
	private List<Button> detailButtonCanardOuterAirfoilList;
	private List<Button> chooseInnerCanardAirfoilFileButtonList;
	private List<Button> chooseOuterCanardAirfoilFileButtonList;
	
	// control surfaces
	private List<ChoiceBox<String>> choiceBoxCanardControlSurfaceTypeList;
	private List<TextField> textFieldCanardInnerPositionControlSurfaceList;
	private List<TextField> textFieldCanardOuterPositionControlSurfaceList;
	private List<TextField> textFieldCanardInnerChordRatioControlSurfaceList;
	private List<TextField> textFieldCanardOuterChordRatioControlSurfaceList;
	private List<TextField> textFieldCanardMinimumDeflectionAngleControlSurfaceList;
	private List<TextField> textFieldCanardMaximumDeflectionAngleControlSurfaceList;
	private List<ChoiceBox<String>> choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList;
	private List<ChoiceBox<String>> choiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList;
	
	//...........................................................................................
	// CANARD TAB (UNITS):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> canardRoughnessUnitChoiceBox;
	@FXML
	private ChoiceBox<String> canardSpanPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> canardSweepLEPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> canardDihedralPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> canardInnerSectionChordPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> canardInnerSectionTwistTipPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> canardOuterSectionChordPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> canardOuterSectionTwistTipPanel1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> canardMinimumDeflectionAngleControlSurface1UnitChoiceBox;
	@FXML
	private ChoiceBox<String> canardMaximumDeflectionAngleControlSurface1UnitChoiceBox;
	
	//...........................................................................................
	// NACELLE TAB (DATA):
	//...........................................................................................
	@FXML
	private TextField textFieldNacelleRoughness1;
	@FXML
	private TextField textFieldNacelleLength1;
	@FXML
	private TextField textFieldNacelleMaximumDiameter1;
	@FXML
	private TextField textFieldNacelleKInlet1;
	@FXML
	private TextField textFieldNacelleKOutlet1;
	@FXML
	private TextField textFieldNacelleKLength1;
	@FXML
	private TextField textFieldNacelleKDiameterOutlet1;
	
	private List<TextField> textFieldNacelleRoughnessList;
	private List<TextField> textFieldNacelleLengthList;
	private List<TextField> textFieldNacelleMaximumDiameterList;
	private List<TextField> textFieldNacelleKInletList;
	private List<TextField> textFieldNacelleKOutletList;
	private List<TextField> textFieldNacelleKLengthList;
	private List<TextField> textFieldNacelleKDiameterOutletList;
	private List<ChoiceBox<String>> choiceBoxNacelleRoughnessUnitList;
	private List<ChoiceBox<String>> choiceBoxNacelleLengthUnitList;
	private List<ChoiceBox<String>> choiceBoxNacelleMaximumDiameterUnitList;
	private List<Button> nacelleEstimateDimesnsionButtonList;
	private List<Button> nacelleKInletInfoButtonList;
	private List<Button> nacelleKOutletInfoButtonList;
	private List<Button> nacelleKLengthInfoButtonList;
	private List<Button> nacelleKDiameterOutletInfoButtonList;
	private List<Pane> nacelleTopViewPaneList;
	private List<Pane> nacelleSideViewPaneList;
	private List<Pane> nacelleFrontViewPaneList;
	
	//...........................................................................................
	// NACELLE TAB (UNITS):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> nacelleRoughnessUnitChoiceBox1;
	@FXML
	private ChoiceBox<String> nacelleLengthUnitChoiceBox1;
	@FXML
	private ChoiceBox<String> nacelleMaximumDiameterUnitChoiceBox1;
	
	//...........................................................................................
	// POWER PLANT (RADIO BUTTONS):
	//...........................................................................................
	@FXML
	private RadioButton powerPlantJetRadioButton1;
	@FXML
	private RadioButton powerPlantTurbopropRadioButton1;
	@FXML
	private RadioButton powerPlantPistonRadioButton1;
	
	private ToggleGroup powerPlantToggleGroup1;
	
	private List<RadioButton> powerPlantJetRadioButtonList;
	private List<RadioButton> powerPlantTurbopropRadioButtonList;
	private List<RadioButton> powerPlantPistonRadioButtonList;
	private List<ToggleGroup> powerPlantToggleGropuList;
	
	private Map<Integer, ChoiceBox<String>> engineTurbojetTurbofanTypeChoiceBoxMap; 
	private Map<Integer, TextField> engineTurbojetTurbofanDatabaseTextFieldMap;
	private Map<Integer, TextField> engineTurbojetTurbofanLengthTextFieldMap;
	private Map<Integer, ChoiceBox<String>> engineTurbojetTurbofanLengthUnitChoiceBoxMap;
	private Map<Integer, TextField> engineTurbojetTurbofanDryMassTextFieldMap;
	private Map<Integer, ChoiceBox<String>> engineTurbojetTurbofanDryMassUnitChoiceBoxMap;
	private Map<Integer, TextField> engineTurbojetTurbofanStaticThrustTextFieldMap;
	private Map<Integer, ChoiceBox<String>> engineTurbojetTurbofanStaticThrustUnitChoiceBoxMap;
	private Map<Integer, TextField> engineTurbojetTurbofanBPRTextFieldMap;
	private Map<Integer, TextField> engineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap;
	private Map<Integer, TextField> engineTurbojetTurbofanNumberOfShaftsTextFieldMap;
	private Map<Integer, TextField> engineTurbojetTurbofanOverallPressureRatioTextFieldMap;
	
	private Map<Integer, ChoiceBox<String>> engineTurbopropTypeChoiceBoxMap; 
	private Map<Integer, TextField> engineTurbopropDatabaseTextFieldMap;
	private Map<Integer, TextField> engineTurbopropLengthTextFieldMap;
	private Map<Integer, ChoiceBox<String>> engineTurbopropLengthUnitChoiceBoxMap;
	private Map<Integer, TextField> engineTurbopropDryMassTextFieldMap;
	private Map<Integer, ChoiceBox<String>> engineTurbopropDryMassUnitChoiceBoxMap;
	private Map<Integer, TextField> engineTurbopropStaticPowerTextFieldMap;
	private Map<Integer, ChoiceBox<String>> engineTurbopropStaticPowerUnitChoiceBoxMap;
	private Map<Integer, TextField> engineTurbopropPropellerDiameterTextFieldMap;
	private Map<Integer, ChoiceBox<String>> engineTurbopropPropellerDiameterUnitChoiceBoxMap;
	private Map<Integer, TextField> engineTurbopropNumberOfBladesTextFieldMap;
	private Map<Integer, TextField> engineTurbopropPropellerEfficiencyTextFieldMap;
	private Map<Integer, TextField> engineTurbopropNumberOfCompressorStagesTextFieldMap;
	private Map<Integer, TextField> engineTurbopropNumberOfShaftsTextFieldMap;
	private Map<Integer, TextField> engineTurbopropOverallPressureRatioTextFieldMap;
	
	private Map<Integer, ChoiceBox<String>> enginePistonTypeChoiceBoxMap; 
	private Map<Integer, TextField> enginePistonDatabaseTextFieldMap;
	private Map<Integer, TextField> enginePistonLengthTextFieldMap;
	private Map<Integer, ChoiceBox<String>> enginePistonLengthUnitChoiceBoxMap;
	private Map<Integer, TextField> enginePistonDryMassTextFieldMap;
	private Map<Integer, ChoiceBox<String>> enginePistonDryMassUnitChoiceBoxMap;
	private Map<Integer, TextField> enginePistonStaticPowerTextFieldMap;
	private Map<Integer, ChoiceBox<String>> enginePistonStaticPowerUnitChoiceBoxMap;
	private Map<Integer, TextField> enginePistonPropellerDiameterTextFieldMap;
	private Map<Integer, ChoiceBox<String>> enginePistonPropellerDiameterUnitChoiceBoxMap;
	private Map<Integer, TextField> enginePistonNumberOfBladesTextFieldMap;
	private Map<Integer, TextField> enginePistonPropellerEfficiencyTextFieldMap;
	
	private Map<Integer, Map<EngineTypeEnum, Pane>> powerPlantPaneMap;
	private Map<Integer, BorderPane> powerPlantBorderPaneMap;
	
	/*
	 * TODO: SEE HOW TO SHOW THE DATABASE CURVE (LATER ... ALL ENGINES SHOULD HAVE A SIMILAR DATABASE STRUCTURE)
	 */
	
	//...........................................................................................
	// LANDING GEARS TAB (DATA):
	//...........................................................................................
	@FXML
	private TextField textFieldLandingGearsMainLegLength;
	@FXML
	private TextField textFieldLandingGearsKMainLegLength;
	@FXML
	private TextField textFieldLandingGearsDistanceBetweenWheels;
	@FXML
	private TextField textFieldLandingGearsNumberOfFrontalWheels;
	@FXML
	private TextField textFieldLandingGearsNumberOfRearWheels;
	@FXML
	private TextField textFieldLandingGearsFrontalWheelsHeight;
	@FXML
	private TextField textFieldLandingGearsFrontalWheelsWidth;
	@FXML
	private TextField textFieldLandingGearsRearWheelsHeight;
	@FXML
	private TextField textFieldLandingGearsRearWheelsWidth;
	
	//...........................................................................................
	// LANDING GEARS TAB (UNITS):
	//...........................................................................................
	@FXML
	private ChoiceBox<String> landingGearsMainLegLengthUnitChoiceBox;
	@FXML
	private ChoiceBox<String> landingGearsDistanceBetweenWheelsUnitChoiceBox;
	@FXML
	private ChoiceBox<String> landingGearsFrontalWheelsHeigthUnitChoiceBox;
	@FXML
	private ChoiceBox<String> landingGearsFrontalWheelsWidthUnitChoiceBox;
	@FXML
	private ChoiceBox<String> landingGearsRearWheelsHeigthUnitChoiceBox;
	@FXML
	private ChoiceBox<String> landingGearsRearWheelsWidthUnitChoiceBox;
	
	//-------------------------------------------------------------------------------------------
	// METHODS
	//-------------------------------------------------------------------------------------------
	@FXML
	private void initialize() {
		
		Main.setAircraftSaved(false);
		Main.setAircraftUpdated(false);
		Platform.setImplicitExit(false);
		
		ObjectProperty<Boolean> aircraftSavedFlag = new SimpleObjectProperty<>();

		try {
			aircraftSavedFlag.set(Main.getAircraftSaved());
			saveAircraftButton.disableProperty().bind(
					Bindings.equal(aircraftSavedFlag, true)
					);
		} catch (Exception e) {
			saveAircraftButton.setDisable(true);
		}
		
		ObjectProperty<Aircraft> aircraft = new SimpleObjectProperty<>();

		try {
			aircraft.set(Main.getTheAircraft());
			newAircraftButton.disableProperty().bind(
					Bindings.isNull(aircraft)
					);
		} catch (Exception e) {
			newAircraftButton.setDisable(true);
		}
		
		tabPaneFuselageSpoilers.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		for(int i=0; i<tabPaneFuselageSpoilers.getTabs().size(); i++)
			removeContentOnSpoilerTabClose(tabPaneFuselageSpoilers.getTabs().get(i), ComponentEnum.FUSELAGE);
		
		tabPaneAircraftEngines.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		for(int i=0; i<tabPaneAircraftEngines.getTabs().size(); i++)
			removeContentOnAircraftEngineTabClose(tabPaneAircraftEngines.getTabs().get(i));
		
		tabPaneAircraftNacelles.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		for(int i=0; i<tabPaneAircraftNacelles.getTabs().size(); i++)
			removeContentOnAircraftNacelleTabClose(tabPaneAircraftNacelles.getTabs().get(i));
		
		tabPaneWingPanels.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneWingFlaps.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneWingSlats.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneWingSpoilers.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneWingViewAndAirfoils.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneWingViewAndAirfoils.getTabs().get(0).closableProperty().set(false);
		tabPaneWingViewAndAirfoils.getTabs().get(1).closableProperty().set(false);
		
		for(int i=0; i<tabPaneWingPanels.getTabs().size(); i++)
			removeContentOnPanelTabClose(tabPaneWingPanels.getTabs().get(i), ComponentEnum.WING);
		for(int i=0; i<tabPaneWingFlaps.getTabs().size(); i++)
			removeContentOnFlapTabClose(tabPaneWingFlaps.getTabs().get(i), ComponentEnum.WING);
		for(int i=0; i<tabPaneWingSlats.getTabs().size(); i++)
			removeContentOnSlatTabClose(tabPaneWingSlats.getTabs().get(i));
		for(int i=0; i<tabPaneWingSpoilers.getTabs().size(); i++)
			removeContentOnSpoilerTabClose(tabPaneWingSpoilers.getTabs().get(i), ComponentEnum.WING);
		
		tabPaneHTailPanels.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneHTailElevators.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneHTailViewAndAirfoils.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneHTailViewAndAirfoils.getTabs().get(0).closableProperty().set(false);
		
		for(int i=0; i<tabPaneHTailPanels.getTabs().size(); i++)
			removeContentOnPanelTabClose(tabPaneHTailPanels.getTabs().get(i), ComponentEnum.HORIZONTAL_TAIL);
		for(int i=0; i<tabPaneHTailElevators.getTabs().size(); i++)
			removeContentOnFlapTabClose(tabPaneHTailElevators.getTabs().get(i), ComponentEnum.HORIZONTAL_TAIL);
		
		tabPaneVTailPanels.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneVTailRudders.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneVTailViewAndAirfoils.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneVTailViewAndAirfoils.getTabs().get(0).closableProperty().set(false);
		
		for(int i=0; i<tabPaneVTailPanels.getTabs().size(); i++)
			removeContentOnPanelTabClose(tabPaneVTailPanels.getTabs().get(i), ComponentEnum.VERTICAL_TAIL);
		for(int i=0; i<tabPaneVTailRudders.getTabs().size(); i++)
			removeContentOnFlapTabClose(tabPaneVTailRudders.getTabs().get(i), ComponentEnum.VERTICAL_TAIL);
		
		tabPaneCanardPanels.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneCanardControlSurfaces.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneCanardViewAndAirfoils.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneCanardViewAndAirfoils.getTabs().get(0).closableProperty().set(false);
		
		for(int i=0; i<tabPaneCanardPanels.getTabs().size(); i++)
			removeContentOnPanelTabClose(tabPaneCanardPanels.getTabs().get(i), ComponentEnum.CANARD);
		for(int i=0; i<tabPaneCanardControlSurfaces.getTabs().size(); i++)
			removeContentOnFlapTabClose(tabPaneCanardControlSurfaces.getTabs().get(i), ComponentEnum.CANARD);
		
		tabPaneNacelles.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		for(int i=0; i<tabPaneNacelles.getTabs().size(); i++)
			removeContentOnNacelleTabClose(tabPaneAircraftNacelles.getTabs().get(i));
		
		tabPaneEngines.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		for(int i=0; i<tabPaneEngines.getTabs().size(); i++)
			removeContentOnEngineTabClose(tabPaneAircraftEngines.getTabs().get(i));
		
		//.......................................................................................
		// CHOICE BOX INITIALIZATION
		aircraftTypeChoiceBox.setItems(aircraftTypeList);
		regulationsTypeChoiceBox.setItems(regulationsTypeList);
		windshieldTypeChoiceBox.setItems(windshieldTypeList);
		engineMountingPositionTypeChoiceBox1.setItems(powerPlantMountingPositionTypeList);
		nacelleMountingPositionTypeChoiceBox1.setItems(nacelleMountingPositionTypeList);
		landingGearsMountingPositionTypeChoiceBox.setItems(landingGearsMountingPositionTypeList);
		fuselageAdjustCriterionChoiceBox.setItems(fuselageAdjustCriteriaTypeList);
		cabinConfigurationClassesTypeChoiceBox1.setItems(cabinConfigurationClassesTypeList);
		cabinConfigurationClassesTypeChoiceBox2.setItems(cabinConfigurationClassesTypeList);
		cabinConfigurationClassesTypeChoiceBox3.setItems(cabinConfigurationClassesTypeList);
		wingAdjustCriterionChoiceBox.setItems(liftingSurfaceAdjustCriteriaTypeList);
		hTailAdjustCriterionChoiceBox.setItems(liftingSurfaceAdjustCriteriaTypeList);
		vTailAdjustCriterionChoiceBox.setItems(liftingSurfaceAdjustCriteriaTypeList);
		canardAdjustCriterionChoiceBox.setItems(liftingSurfaceAdjustCriteriaTypeList);
		wingFlap1TypeChoichBox.setItems(flapTypeList);
		wingLeftAileronTypeChoichBox.setItems(aileronTypeList);
		wingRightAileronTypeChoichBox.setItems(aileronTypeList);
		hTailElevator1TypeChoiceBox.setItems(elevatorTypeList);
		vTailRudder1TypeChoiceBox.setItems(rudderTypeList);
		canardControlSurface1TypeChoiceBox.setItems(canardSurfaceTypeList);
		
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
		engineX1UnitChoiceBox.setItems(lengthUnitsList);
		engineY1UnitChoiceBox.setItems(lengthUnitsList);
		engineZ1UnitChoiceBox.setItems(lengthUnitsList);
		engineTilt1AngleUnitChoiceBox.setItems(angleUnitsList);
		nacelleX1UnitChoiceBox.setItems(lengthUnitsList);
		nacelleY1UnitChoiceBox.setItems(lengthUnitsList);
		nacelleZ1UnitChoiceBox.setItems(lengthUnitsList);
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
		fuselageSpoiler1DeltaMinUnitChoiceBox.setItems(angleUnitsList);
		fuselageSpoiler1DeltaMaxUnitChoiceBox.setItems(angleUnitsList);
		cabinConfigurationXCoordinateFirstRowUnitChoiceBox.setItems(lengthUnitsList);
		cabinConfigurationSeatsPitchEconomyUnitChoiceBox.setItems(lengthUnitsList);
		cabinConfigurationSeatsPitchBusinessUnitChoiceBox.setItems(lengthUnitsList);
		cabinConfigurationSeatsPitchFirstUnitChoiceBox.setItems(lengthUnitsList);
		cabinConfigurationSeatsWidthEconomyUnitChoiceBox.setItems(lengthUnitsList);
		cabinConfigurationSeatsWidthBusinessUnitChoiceBox.setItems(lengthUnitsList);
		cabinConfigurationSeatsWidthFirstUnitChoiceBox.setItems(lengthUnitsList);
		cabinConfigurationDistanceFromWallEconomyUnitChoiceBox.setItems(lengthUnitsList);
		cabinConfigurationDistanceFromWallBusinessUnitChoiceBox.setItems(lengthUnitsList);
		cabinConfigurationDistanceFromWallFirstUnitChoiceBox.setItems(lengthUnitsList);
		cabinConfigurationMassFurnishingsAndEquipmentUnitChoiceBox.setItems(massUnitsList);
		wingRoughnessUnitChoiceBox.setItems(lengthUnitsList);
		wingWingletHeightUnitChoiceBox.setItems(lengthUnitsList);
		equivalentWingAreaUnitChoiceBox.setItems(areaUnitsList);
		equivalentWingSweepLEUnitChoiceBox.setItems(angleUnitsList);
		equivalentWingTwistAtTipUnitChoiceBox.setItems(angleUnitsList);
		equivalentWingDihedralUnitChoiceBox.setItems(angleUnitsList);
		wingSpanPanel1UnitChoiceBox.setItems(lengthUnitsList);
		wingSweepLEPanel1UnitChoiceBox.setItems(angleUnitsList);
		wingDihedralPanel1UnitChoiceBox.setItems(angleUnitsList);
		wingInnerSectionChordPanel1UnitChoiceBox.setItems(lengthUnitsList);
		wingInnerSectionTwistTipPanel1UnitChoiceBox.setItems(angleUnitsList);
		wingOuterSectionChordPanel1UnitChoiceBox.setItems(lengthUnitsList);
		wingOuterSectionTwistTipPanel1UnitChoiceBox.setItems(angleUnitsList);
		wingMinimumDeflectionAngleFlap1UnitChoiceBox.setItems(angleUnitsList);
		wingMaximumDeflectionAngleFlap1UnitChoiceBox.setItems(angleUnitsList);
		wingMinimumDeflectionAngleSlat1UnitChoiceBox.setItems(angleUnitsList);
		wingMaximumDeflectionAngleSlat1UnitChoiceBox.setItems(angleUnitsList);
		wingMinimumDeflectionAngleAileronLeftUnitChoiceBox.setItems(angleUnitsList);
		wingMaximumDeflectionAngleAileronLeftUnitChoiceBox.setItems(angleUnitsList);
		wingMinimumDeflectionAngleAileronRigthUnitChoiceBox.setItems(angleUnitsList);
		wingMaximumDeflectionAngleAileronRightUnitChoiceBox.setItems(angleUnitsList);
		wingMinimumDeflectionAngleSpoiler1UnitChoiceBox.setItems(angleUnitsList);
		wingMaximumDeflectionAngleSpoiler1UnitChoiceBox.setItems(angleUnitsList);
		hTailRoughnessUnitChoiceBox.setItems(lengthUnitsList);
		hTailSpanPanel1UnitChoiceBox.setItems(lengthUnitsList);
		hTailSweepLEPanel1UnitChoiceBox.setItems(angleUnitsList);
		hTailDihedralPanel1UnitChoiceBox.setItems(angleUnitsList);
		hTailInnerSectionChordPanel1UnitChoiceBox.setItems(lengthUnitsList);
		hTailInnerSectionTwistTipPanel1UnitChoiceBox.setItems(angleUnitsList);
		hTailOuterSectionChordPanel1UnitChoiceBox.setItems(lengthUnitsList);
		hTailOuterSectionTwistTipPanel1UnitChoiceBox.setItems(angleUnitsList);
		hTailMinimumDeflectionAngleElevator1UnitChoiceBox.setItems(angleUnitsList);
		hTailMaximumDeflectionAngleElevator1UnitChoiceBox.setItems(angleUnitsList);
		vTailRoughnessUnitChoiceBox.setItems(lengthUnitsList);
		vTailSpanPanel1UnitChoiceBox.setItems(lengthUnitsList);
		vTailSweepLEPanel1UnitChoiceBox.setItems(angleUnitsList);
		vTailDihedralPanel1UnitChoiceBox.setItems(angleUnitsList);
		vTailInnerSectionChordPanel1UnitChoiceBox.setItems(lengthUnitsList);
		vTailInnerSectionTwistTipPanel1UnitChoiceBox.setItems(angleUnitsList);
		vTailOuterSectionChordPanel1UnitChoiceBox.setItems(lengthUnitsList);
		vTailOuterSectionTwistTipPanel1UnitChoiceBox.setItems(angleUnitsList);
		vTailMinimumDeflectionAngleRudder1UnitChoiceBox.setItems(angleUnitsList);
		vTailMaximumDeflectionAngleRudder1UnitChoiceBox.setItems(angleUnitsList);
		canardRoughnessUnitChoiceBox.setItems(lengthUnitsList);
		canardSpanPanel1UnitChoiceBox.setItems(lengthUnitsList);
		canardSweepLEPanel1UnitChoiceBox.setItems(angleUnitsList);
		canardDihedralPanel1UnitChoiceBox.setItems(angleUnitsList);
		canardInnerSectionChordPanel1UnitChoiceBox.setItems(lengthUnitsList);
		canardInnerSectionTwistTipPanel1UnitChoiceBox.setItems(angleUnitsList);
		canardOuterSectionChordPanel1UnitChoiceBox.setItems(lengthUnitsList);
		canardOuterSectionTwistTipPanel1UnitChoiceBox.setItems(angleUnitsList);
		canardMinimumDeflectionAngleControlSurface1UnitChoiceBox.setItems(angleUnitsList);
		canardMaximumDeflectionAngleControlSurface1UnitChoiceBox.setItems(angleUnitsList);
		nacelleRoughnessUnitChoiceBox1.setItems(lengthUnitsList);
		nacelleLengthUnitChoiceBox1.setItems(lengthUnitsList);
		nacelleMaximumDiameterUnitChoiceBox1.setItems(lengthUnitsList);
		
		//.......................................................................................
		// AIRCRAFT ENGINE LISTS INITIALIZATION
		textFieldsAircraftEngineFileList = new ArrayList<>();
		textFieldsAircraftEngineFileList.add(textFieldAircraftEngineFile1);
		
		chooseEngineFileButtonList = new ArrayList<>();
		chooseEngineFileButtonList.add(aircraftChooseEngine1FileButton);
		
		textFieldAircraftEngineXList = new ArrayList<>();
		textFieldAircraftEngineXList.add(textFieldAircraftEngineX1);
		
		textFieldAircraftEngineYList = new ArrayList<>();
		textFieldAircraftEngineYList.add(textFieldAircraftEngineY1);
		
		textFieldAircraftEngineZList = new ArrayList<>();
		textFieldAircraftEngineZList.add(textFieldAircraftEngineZ1);
		
		textFieldAircraftEngineTiltList = new ArrayList<>();
		textFieldAircraftEngineTiltList.add(textFieldAircraftEngineTilt1);
		
		choiceBoxesAircraftEnginePositonList = new ArrayList<>();
		choiceBoxesAircraftEnginePositonList.add(engineMountingPositionTypeChoiceBox1);
		
		choiceBoxAircraftEngineXUnitList = new ArrayList<>();
		choiceBoxAircraftEngineXUnitList.add(engineX1UnitChoiceBox);
		
		choiceBoxAircraftEngineYUnitList = new ArrayList<>();
		choiceBoxAircraftEngineYUnitList.add(engineY1UnitChoiceBox);
		
		choiceBoxAircraftEngineZUnitList = new ArrayList<>();
		choiceBoxAircraftEngineZUnitList.add(engineZ1UnitChoiceBox);
		
		choiceBoxAircraftEngineTiltUnitList = new ArrayList<>();
		choiceBoxAircraftEngineTiltUnitList.add(engineTilt1AngleUnitChoiceBox);
		
		//.......................................................................................
		// AIRCRAFT NACELLE LISTS INITIALIZATION
		textFieldsAircraftNacelleFileList = new ArrayList<>();
		textFieldsAircraftNacelleFileList.add(textFieldAircraftNacelleFile1);
		
		chooseNacelleFileButtonList = new ArrayList<>();
		chooseNacelleFileButtonList.add(aircraftChooseNacelle1FileButton);
		
		textFieldAircraftNacelleXList = new ArrayList<>();
		textFieldAircraftNacelleXList.add(textFieldAircraftNacelleX1);
		
		textFieldAircraftNacelleYList = new ArrayList<>();
		textFieldAircraftNacelleYList.add(textFieldAircraftNacelleY1);
		
		textFieldAircraftNacelleZList = new ArrayList<>();
		textFieldAircraftNacelleZList.add(textFieldAircraftNacelleZ1);
		
		choiceBoxesAircraftNacellePositonList = new ArrayList<>();
		choiceBoxesAircraftNacellePositonList.add(nacelleMountingPositionTypeChoiceBox1);
		
		choiceBoxAircraftNacelleXUnitList = new ArrayList<>();
		choiceBoxAircraftNacelleXUnitList.add(nacelleX1UnitChoiceBox);
		
		choiceBoxAircraftNacelleYUnitList = new ArrayList<>();
		choiceBoxAircraftNacelleYUnitList.add(nacelleY1UnitChoiceBox);
		
		choiceBoxAircraftNacelleZUnitList = new ArrayList<>();
		choiceBoxAircraftNacelleZUnitList.add(nacelleZ1UnitChoiceBox);
		
		//.......................................................................................
		// FUSELAGE LISTS INITIALIZATION
		// spoilers
		textFieldFuselageInnerSpanwisePositionSpoilerList = new ArrayList<>();
		textFieldFuselageInnerSpanwisePositionSpoilerList.add(textFieldFuselageSpoilerYin1);
		
		textFieldFuselageOuterSpanwisePositionSpoilerList = new ArrayList<>();
		textFieldFuselageOuterSpanwisePositionSpoilerList.add(textFieldFuselageSpoilerYout1);
		
		textFieldFuselageInnerChordwisePositionSpoilerList = new ArrayList<>();
		textFieldFuselageInnerChordwisePositionSpoilerList.add(textFieldFuselageSpoilerXin1);
		
		textFieldFuselageOuterChordwisePositionSpoilerList = new ArrayList<>();
		textFieldFuselageOuterChordwisePositionSpoilerList.add(textFieldFuselageSpoilerYout1);
		
		textFieldFuselageMinimumDeflectionAngleSpoilerList = new ArrayList<>();
		textFieldFuselageMinimumDeflectionAngleSpoilerList.add(textFieldFuselageSpoilerMinDeflection1);
		
		textFieldFuselageMaximumDeflectionAngleSpoilerList = new ArrayList<>();
		textFieldFuselageMaximumDeflectionAngleSpoilerList.add(textFieldFuselageSpoilerMaxDeflection1);
		
		choiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList = new ArrayList<>();
		choiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList.add(fuselageSpoiler1DeltaMinUnitChoiceBox);
		
		choiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList = new ArrayList<>();
		choiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList.add(fuselageSpoiler1DeltaMaxUnitChoiceBox);
		
		//.......................................................................................
		// WING LISTS INITIALIZATION
		// panels
		textFieldWingSpanPanelList = new ArrayList<>();
		textFieldWingSpanPanelList.add(textFieldWingSpanPanel1);
		
		textFieldWingSweepLEPanelList = new ArrayList<>();
		textFieldWingSweepLEPanelList.add(textFieldWingSweepLeadingEdgePanel1);
		
		textFieldWingDihedralPanelList = new ArrayList<>();
		textFieldWingDihedralPanelList.add(textFieldWingDihedralPanel1);
		
		textFieldWingInnerChordPanelList = new ArrayList<>();
		textFieldWingInnerChordPanelList.add(textFieldWingChordInnerSectionPanel1);
		
		textFieldWingInnerTwistPanelList = new ArrayList<>();
		textFieldWingInnerTwistPanelList.add(textFieldWingTwistInnerSectionPanel1);
		
		textFieldWingInnerAirfoilPanelList = new ArrayList<>();
		textFieldWingInnerAirfoilPanelList.add(textFieldWingAirfoilPathInnerSectionPanel1);
		
		textFieldWingOuterChordPanelList = new ArrayList<>();
		textFieldWingOuterChordPanelList.add(textFieldWingChordOuterSectionPanel1);
		
		textFieldWingOuterTwistPanelList = new ArrayList<>();
		textFieldWingOuterTwistPanelList.add(textFieldWingTwistOuterSectionPanel1);
		
		textFieldWingOuterAirfoilPanelList = new ArrayList<>();
		textFieldWingOuterAirfoilPanelList.add(textFieldWingAirfoilPathOuterSectionPanel1);
		
		checkBoxWingLinkedToPreviousPanelList = new ArrayList<>();
		
		choiceBoxWingSpanPanelUnitList = new ArrayList<>();
		choiceBoxWingSpanPanelUnitList.add(wingSpanPanel1UnitChoiceBox);
		
		choiceBoxWingSweepLEPanelUnitList = new ArrayList<>();
		choiceBoxWingSweepLEPanelUnitList.add(wingSweepLEPanel1UnitChoiceBox);
		
		choiceBoxWingDihedralPanelUnitList = new ArrayList<>();
		choiceBoxWingDihedralPanelUnitList.add(wingDihedralPanel1UnitChoiceBox);
		
		choiceBoxWingInnerChordPanelUnitList = new ArrayList<>();
		choiceBoxWingInnerChordPanelUnitList.add(wingInnerSectionChordPanel1UnitChoiceBox);
		
		choiceBoxWingInnerTwistPanelUnitList = new ArrayList<>();
		choiceBoxWingInnerTwistPanelUnitList.add(wingInnerSectionTwistTipPanel1UnitChoiceBox);
		
		choiceBoxWingOuterChordPanelUnitList = new ArrayList<>();
		choiceBoxWingOuterChordPanelUnitList.add(wingOuterSectionChordPanel1UnitChoiceBox);
		
		choiceBoxWingOuterTwistPanelUnitList = new ArrayList<>();
		choiceBoxWingOuterTwistPanelUnitList.add(wingOuterSectionTwistTipPanel1UnitChoiceBox);
		
		chooseInnerWingAirfoilFileButtonList = new ArrayList<>();
		chooseInnerWingAirfoilFileButtonList.add(wingChooseInnerAirfoilPanel1Button);
		
		detailButtonWingInnerAirfoilList = new ArrayList<>();
		detailButtonWingInnerAirfoilList.add(wingInnerSectionAirfoilDetailsPanel1Button);
		
		chooseOuterWingAirfoilFileButtonList = new ArrayList<>();
		chooseOuterWingAirfoilFileButtonList.add(wingChooseOuterAirfoilPanel1Button);
		
		detailButtonWingOuterAirfoilList = new ArrayList<>();
		detailButtonWingOuterAirfoilList.add(wingOuterSectionAirfoilDetailsPanel1Button);
		
		// flaps
		choiceBoxWingFlapTypeList= new ArrayList<>();
		choiceBoxWingFlapTypeList.add(wingFlap1TypeChoichBox);
		
		textFieldWingInnerPositionFlapList = new ArrayList<>();
		textFieldWingInnerPositionFlapList.add(textFieldWingInnerPositionFlap1);
		
		textFieldWingOuterPositionFlapList = new ArrayList<>();
		textFieldWingOuterPositionFlapList.add(textFieldWingOuterPositionFlap1);
		
		textFieldWingInnerChordRatioFlapList = new ArrayList<>();
		textFieldWingInnerChordRatioFlapList.add(textFieldWingInnerChordRatioFlap1);
		
		textFieldWingOuterChordRatioFlapList = new ArrayList<>();
		textFieldWingOuterChordRatioFlapList.add(textFieldWingOuterChordRatioFlap1);
		
		textFieldWingMinimumDeflectionAngleFlapList = new ArrayList<>();
		textFieldWingMinimumDeflectionAngleFlapList.add(textFieldWingMinimumDeflectionAngleFlap1);
		
		textFieldWingMaximumDeflectionAngleFlapList = new ArrayList<>();
		textFieldWingMaximumDeflectionAngleFlapList.add(textFieldWingMaximumDeflectionAngleFlap1);
		
		choiceBoxWingMinimumDeflectionAngleFlapUnitList = new ArrayList<>();
		choiceBoxWingMinimumDeflectionAngleFlapUnitList.add(wingMinimumDeflectionAngleFlap1UnitChoiceBox);
		
		choiceBoxWingMaximumDeflectionAngleFlapUnitList = new ArrayList<>();
		choiceBoxWingMaximumDeflectionAngleFlapUnitList.add(wingMaximumDeflectionAngleFlap1UnitChoiceBox);
		
		// slats
		textFieldWingInnerPositionSlatList = new ArrayList<>();
		textFieldWingInnerPositionSlatList.add(textFieldWingInnerPositionSlat1);
		
		textFieldWingOuterPositionSlatList = new ArrayList<>();
		textFieldWingOuterPositionSlatList.add(textFieldWingOuterPositionSlat1);
		
		textFieldWingInnerChordRatioSlatList = new ArrayList<>();
		textFieldWingInnerChordRatioSlatList.add(textFieldWingInnerChordRatioSlat1);
		
		textFieldWingOuterChordRatioSlatList = new ArrayList<>();
		textFieldWingOuterChordRatioSlatList.add(textFieldWingOuterChordRatioSlat1);
		
		textFieldWingExtensionRatioSlatList = new ArrayList<>();
		textFieldWingExtensionRatioSlatList.add(textFieldWingExtensionRatioSlat1);
		
		textFieldWingMinimumDeflectionAngleSlatList = new ArrayList<>();
		textFieldWingMinimumDeflectionAngleSlatList.add(textFieldWingMinimumDeflectionAngleSlat1);
		
		textFieldWingMaximumDeflectionAngleSlatList = new ArrayList<>();
		textFieldWingMaximumDeflectionAngleSlatList.add(textFieldWingMaximumDeflectionAngleSlat1);
		
		choiceBoxWingMinimumDeflectionAngleSlatUnitList = new ArrayList<>();
		choiceBoxWingMinimumDeflectionAngleSlatUnitList.add(wingMinimumDeflectionAngleSlat1UnitChoiceBox);
		
		choiceBoxWingMaximumDeflectionAngleSlatUnitList = new ArrayList<>();
		choiceBoxWingMaximumDeflectionAngleSlatUnitList.add(wingMaximumDeflectionAngleSlat1UnitChoiceBox);
		
		// spoilers
		textFieldWingInnerSpanwisePositionSpoilerList = new ArrayList<>();
		textFieldWingInnerSpanwisePositionSpoilerList.add(textFieldWingInnerSpanwisePositionSpolier1);
		
		textFieldWingOuterSpanwisePositionSpoilerList = new ArrayList<>();
		textFieldWingOuterSpanwisePositionSpoilerList.add(textFieldWingOuterSpanwisePositionSpolier1);
		
		textFieldWingInnerChordwisePositionSpoilerList = new ArrayList<>();
		textFieldWingInnerChordwisePositionSpoilerList.add(textFieldWingInnerChordwisePositionSpolier1);
		
		textFieldWingOuterChordwisePositionSpoilerList = new ArrayList<>();
		textFieldWingOuterChordwisePositionSpoilerList.add(textFieldWingOuterChordwisePositionSpolier1);
		
		textFieldWingMinimumDeflectionAngleSpoilerList = new ArrayList<>();
		textFieldWingMinimumDeflectionAngleSpoilerList.add(textFieldWingMinimumDeflectionAngleSpolier1);
		
		textFieldWingMaximumDeflectionAngleSpoilerList = new ArrayList<>();
		textFieldWingMaximumDeflectionAngleSpoilerList.add(textFieldWingMaximumDeflectionAngleSpoiler1);
		
		choiceBoxWingMinimumDeflectionAngleSpoilerUnitList = new ArrayList<>();
		choiceBoxWingMinimumDeflectionAngleSpoilerUnitList.add(wingMinimumDeflectionAngleSpoiler1UnitChoiceBox);
		
		choiceBoxWingMaximumDeflectionAngleSpoilerUnitList = new ArrayList<>();
		choiceBoxWingMaximumDeflectionAngleSpoilerUnitList.add(wingMaximumDeflectionAngleSpoiler1UnitChoiceBox);
		
		//.......................................................................................
		// HTAIL LISTS INITIALIZATION
		// panels
		textFieldHTailSpanPanelList = new ArrayList<>();
		textFieldHTailSpanPanelList.add(textFieldHTailSpanPanel1);

		textFieldHTailSweepLEPanelList = new ArrayList<>();
		textFieldHTailSweepLEPanelList.add(textFieldHTailSweepLeadingEdgePanel1);

		textFieldHTailDihedralPanelList = new ArrayList<>();
		textFieldHTailDihedralPanelList.add(textFieldHTailDihedralPanel1);

		textFieldHTailInnerChordPanelList = new ArrayList<>();
		textFieldHTailInnerChordPanelList.add(textFieldHTailChordInnerSectionPanel1);

		textFieldHTailInnerTwistPanelList = new ArrayList<>();
		textFieldHTailInnerTwistPanelList.add(textFieldHTailTwistInnerSectionPanel1);

		textFieldHTailInnerAirfoilPanelList = new ArrayList<>();
		textFieldHTailInnerAirfoilPanelList.add(textFieldHTailAirfoilPathInnerSectionPanel1);

		textFieldHTailOuterChordPanelList = new ArrayList<>();
		textFieldHTailOuterChordPanelList.add(textFieldHTailChordOuterSectionPanel1);

		textFieldHTailOuterTwistPanelList = new ArrayList<>();
		textFieldHTailOuterTwistPanelList.add(textFieldHTailTwistOuterSectionPanel1);

		textFieldHTailOuterAirfoilPanelList = new ArrayList<>();
		textFieldHTailOuterAirfoilPanelList.add(textFieldHTailAirfoilPathOuterSectionPanel1);

		checkBoxHTailLinkedToPreviousPanelList = new ArrayList<>();

		choiceBoxHTailSpanPanelUnitList = new ArrayList<>();
		choiceBoxHTailSpanPanelUnitList.add(hTailSpanPanel1UnitChoiceBox);

		choiceBoxHTailSweepLEPanelUnitList = new ArrayList<>();
		choiceBoxHTailSweepLEPanelUnitList.add(hTailSweepLEPanel1UnitChoiceBox);

		choiceBoxHTailDihedralPanelUnitList = new ArrayList<>();
		choiceBoxHTailDihedralPanelUnitList.add(hTailDihedralPanel1UnitChoiceBox);

		choiceBoxHTailInnerChordPanelUnitList = new ArrayList<>();
		choiceBoxHTailInnerChordPanelUnitList.add(hTailInnerSectionChordPanel1UnitChoiceBox);

		choiceBoxHTailInnerTwistPanelUnitList = new ArrayList<>();
		choiceBoxHTailInnerTwistPanelUnitList.add(hTailInnerSectionTwistTipPanel1UnitChoiceBox);

		choiceBoxHTailOuterChordPanelUnitList = new ArrayList<>();
		choiceBoxHTailOuterChordPanelUnitList.add(hTailOuterSectionChordPanel1UnitChoiceBox);

		choiceBoxHTailOuterTwistPanelUnitList = new ArrayList<>();
		choiceBoxHTailOuterTwistPanelUnitList.add(hTailOuterSectionTwistTipPanel1UnitChoiceBox);

		chooseInnerHTailAirfoilFileButtonList = new ArrayList<>();
		chooseInnerHTailAirfoilFileButtonList.add(hTailChooseInnerAirfoilPanel1Button);

		detailButtonHTailInnerAirfoilList = new ArrayList<>();
		detailButtonHTailInnerAirfoilList.add(hTailInnerSectionAirfoilDetailsPanel1Button);

		chooseOuterHTailAirfoilFileButtonList = new ArrayList<>();
		chooseOuterHTailAirfoilFileButtonList.add(hTailChooseOuterAirfoilPanel1Button);

		detailButtonHTailOuterAirfoilList = new ArrayList<>();
		detailButtonHTailOuterAirfoilList.add(hTailOuterSectionAirfoilDetailsPanel1Button);

		// elevators
		choiceBoxHTailElevatorTypeList= new ArrayList<>();
		choiceBoxHTailElevatorTypeList.add(hTailElevator1TypeChoiceBox);
		
		textFieldHTailInnerPositionElevatorList = new ArrayList<>();
		textFieldHTailInnerPositionElevatorList.add(textFieldHTailInnerPositionElevator1);

		textFieldHTailOuterPositionElevatorList = new ArrayList<>();
		textFieldHTailOuterPositionElevatorList.add(textFieldHTailOuterPositionElevator1);

		textFieldHTailInnerChordRatioElevatorList = new ArrayList<>();
		textFieldHTailInnerChordRatioElevatorList.add(textFieldHTailInnerChordRatioElevator1);

		textFieldHTailOuterChordRatioElevatorList = new ArrayList<>();
		textFieldHTailOuterChordRatioElevatorList.add(textFieldHTailOuterChordRatioElevator1);

		textFieldHTailMinimumDeflectionAngleElevatorList = new ArrayList<>();
		textFieldHTailMinimumDeflectionAngleElevatorList.add(textFieldHTailMinimumDeflectionAngleElevator1);

		textFieldHTailMaximumDeflectionAngleElevatorList = new ArrayList<>();
		textFieldHTailMaximumDeflectionAngleElevatorList.add(textFieldHTailMaximumDeflectionAngleElevator1);

		choiceBoxHTailMinimumDeflectionAngleElevatorUnitList = new ArrayList<>();
		choiceBoxHTailMinimumDeflectionAngleElevatorUnitList.add(hTailMinimumDeflectionAngleElevator1UnitChoiceBox);

		choiceBoxHTailMaximumDeflectionAngleElevatorUnitList = new ArrayList<>();
		choiceBoxHTailMaximumDeflectionAngleElevatorUnitList.add(hTailMaximumDeflectionAngleElevator1UnitChoiceBox);

		//.......................................................................................
		// VTAIL LISTS INITIALIZATION
		// panels
		textFieldVTailSpanPanelList = new ArrayList<>();
		textFieldVTailSpanPanelList.add(textFieldVTailSpanPanel1);

		textFieldVTailSweepLEPanelList = new ArrayList<>();
		textFieldVTailSweepLEPanelList.add(textFieldVTailSweepLeadingEdgePanel1);

		textFieldVTailDihedralPanelList = new ArrayList<>();
		textFieldVTailDihedralPanelList.add(textFieldVTailDihedralPanel1);

		textFieldVTailInnerChordPanelList = new ArrayList<>();
		textFieldVTailInnerChordPanelList.add(textFieldVTailChordInnerSectionPanel1);

		textFieldVTailInnerTwistPanelList = new ArrayList<>();
		textFieldVTailInnerTwistPanelList.add(textFieldVTailTwistInnerSectionPanel1);

		textFieldVTailInnerAirfoilPanelList = new ArrayList<>();
		textFieldVTailInnerAirfoilPanelList.add(textFieldVTailAirfoilPathInnerSectionPanel1);

		textFieldVTailOuterChordPanelList = new ArrayList<>();
		textFieldVTailOuterChordPanelList.add(textFieldVTailChordOuterSectionPanel1);

		textFieldVTailOuterTwistPanelList = new ArrayList<>();
		textFieldVTailOuterTwistPanelList.add(textFieldVTailTwistOuterSectionPanel1);

		textFieldVTailOuterAirfoilPanelList = new ArrayList<>();
		textFieldVTailOuterAirfoilPanelList.add(textFieldVTailAirfoilPathOuterSectionPanel1);

		checkBoxVTailLinkedToPreviousPanelList = new ArrayList<>();

		choiceBoxVTailSpanPanelUnitList = new ArrayList<>();
		choiceBoxVTailSpanPanelUnitList.add(vTailSpanPanel1UnitChoiceBox);

		choiceBoxVTailSweepLEPanelUnitList = new ArrayList<>();
		choiceBoxVTailSweepLEPanelUnitList.add(vTailSweepLEPanel1UnitChoiceBox);

		choiceBoxVTailDihedralPanelUnitList = new ArrayList<>();
		choiceBoxVTailDihedralPanelUnitList.add(vTailDihedralPanel1UnitChoiceBox);

		choiceBoxVTailInnerChordPanelUnitList = new ArrayList<>();
		choiceBoxVTailInnerChordPanelUnitList.add(vTailInnerSectionChordPanel1UnitChoiceBox);

		choiceBoxVTailInnerTwistPanelUnitList = new ArrayList<>();
		choiceBoxVTailInnerTwistPanelUnitList.add(vTailInnerSectionTwistTipPanel1UnitChoiceBox);

		choiceBoxVTailOuterChordPanelUnitList = new ArrayList<>();
		choiceBoxVTailOuterChordPanelUnitList.add(vTailOuterSectionChordPanel1UnitChoiceBox);

		choiceBoxVTailOuterTwistPanelUnitList = new ArrayList<>();
		choiceBoxVTailOuterTwistPanelUnitList.add(vTailOuterSectionTwistTipPanel1UnitChoiceBox);

		chooseInnerVTailAirfoilFileButtonList = new ArrayList<>();
		chooseInnerVTailAirfoilFileButtonList.add(vTailChooseInnerAirfoilPanel1Button);

		detailButtonVTailInnerAirfoilList = new ArrayList<>();
		detailButtonVTailInnerAirfoilList.add(vTailInnerSectionAirfoilDetailsPanel1Button);

		chooseOuterVTailAirfoilFileButtonList = new ArrayList<>();
		chooseOuterVTailAirfoilFileButtonList.add(vTailChooseOuterAirfoilPanel1Button);

		detailButtonVTailOuterAirfoilList = new ArrayList<>();
		detailButtonVTailOuterAirfoilList.add(vTailOuterSectionAirfoilDetailsPanel1Button);

		// rudders
		choiceBoxVTailRudderTypeList= new ArrayList<>();
		choiceBoxVTailRudderTypeList.add(vTailRudder1TypeChoiceBox);
		
		textFieldVTailInnerPositionRudderList = new ArrayList<>();
		textFieldVTailInnerPositionRudderList.add(textFieldVTailInnerPositionRudder1);

		textFieldVTailOuterPositionRudderList = new ArrayList<>();
		textFieldVTailOuterPositionRudderList.add(textFieldVTailOuterPositionRudder1);

		textFieldVTailInnerChordRatioRudderList = new ArrayList<>();
		textFieldVTailInnerChordRatioRudderList.add(textFieldVTailInnerChordRatioRudder1);

		textFieldVTailOuterChordRatioRudderList = new ArrayList<>();
		textFieldVTailOuterChordRatioRudderList.add(textFieldVTailOuterChordRatioRudder1);

		textFieldVTailMinimumDeflectionAngleRudderList = new ArrayList<>();
		textFieldVTailMinimumDeflectionAngleRudderList.add(textFieldVTailMinimumDeflectionAngleRudder1);

		textFieldVTailMaximumDeflectionAngleRudderList = new ArrayList<>();
		textFieldVTailMaximumDeflectionAngleRudderList.add(textFieldVTailMaximumDeflectionAngleRudder1);

		choiceBoxVTailMinimumDeflectionAngleRudderUnitList = new ArrayList<>();
		choiceBoxVTailMinimumDeflectionAngleRudderUnitList.add(vTailMinimumDeflectionAngleRudder1UnitChoiceBox);

		choiceBoxVTailMaximumDeflectionAngleRudderUnitList = new ArrayList<>();
		choiceBoxVTailMaximumDeflectionAngleRudderUnitList.add(vTailMaximumDeflectionAngleRudder1UnitChoiceBox);
		
		//.......................................................................................
		// CANARD LISTS INITIALIZATION
		// panels
		textFieldCanardSpanPanelList = new ArrayList<>();
		textFieldCanardSpanPanelList.add(textFieldCanardSpanPanel1);

		textFieldCanardSweepLEPanelList = new ArrayList<>();
		textFieldCanardSweepLEPanelList.add(textFieldCanardSweepLeadingEdgePanel1);

		textFieldCanardDihedralPanelList = new ArrayList<>();
		textFieldCanardDihedralPanelList.add(textFieldCanardDihedralPanel1);

		textFieldCanardInnerChordPanelList = new ArrayList<>();
		textFieldCanardInnerChordPanelList.add(textFieldCanardChordInnerSectionPanel1);

		textFieldCanardInnerTwistPanelList = new ArrayList<>();
		textFieldCanardInnerTwistPanelList.add(textFieldCanardTwistInnerSectionPanel1);

		textFieldCanardInnerAirfoilPanelList = new ArrayList<>();
		textFieldCanardInnerAirfoilPanelList.add(textFieldCanardAirfoilPathInnerSectionPanel1);

		textFieldCanardOuterChordPanelList = new ArrayList<>();
		textFieldCanardOuterChordPanelList.add(textFieldCanardChordOuterSectionPanel1);

		textFieldCanardOuterTwistPanelList = new ArrayList<>();
		textFieldCanardOuterTwistPanelList.add(textFieldCanardTwistOuterSectionPanel1);

		textFieldCanardOuterAirfoilPanelList = new ArrayList<>();
		textFieldCanardOuterAirfoilPanelList.add(textFieldCanardAirfoilPathOuterSectionPanel1);

		checkBoxCanardLinkedToPreviousPanelList = new ArrayList<>();

		choiceBoxCanardSpanPanelUnitList = new ArrayList<>();
		choiceBoxCanardSpanPanelUnitList.add(canardSpanPanel1UnitChoiceBox);

		choiceBoxCanardSweepLEPanelUnitList = new ArrayList<>();
		choiceBoxCanardSweepLEPanelUnitList.add(canardSweepLEPanel1UnitChoiceBox);

		choiceBoxCanardDihedralPanelUnitList = new ArrayList<>();
		choiceBoxCanardDihedralPanelUnitList.add(canardDihedralPanel1UnitChoiceBox);

		choiceBoxCanardInnerChordPanelUnitList = new ArrayList<>();
		choiceBoxCanardInnerChordPanelUnitList.add(canardInnerSectionChordPanel1UnitChoiceBox);

		choiceBoxCanardInnerTwistPanelUnitList = new ArrayList<>();
		choiceBoxCanardInnerTwistPanelUnitList.add(canardInnerSectionTwistTipPanel1UnitChoiceBox);

		choiceBoxCanardOuterChordPanelUnitList = new ArrayList<>();
		choiceBoxCanardOuterChordPanelUnitList.add(canardOuterSectionChordPanel1UnitChoiceBox);

		choiceBoxCanardOuterTwistPanelUnitList = new ArrayList<>();
		choiceBoxCanardOuterTwistPanelUnitList.add(canardOuterSectionTwistTipPanel1UnitChoiceBox);

		chooseInnerCanardAirfoilFileButtonList = new ArrayList<>();
		chooseInnerCanardAirfoilFileButtonList.add(canardChooseInnerAirfoilPanel1Button);

		detailButtonCanardInnerAirfoilList = new ArrayList<>();
		detailButtonCanardInnerAirfoilList.add(canardInnerSectionAirfoilDetailsPanel1Button);

		chooseOuterCanardAirfoilFileButtonList = new ArrayList<>();
		chooseOuterCanardAirfoilFileButtonList.add(canardChooseOuterAirfoilPanel1Button);

		detailButtonCanardOuterAirfoilList = new ArrayList<>();
		detailButtonCanardOuterAirfoilList.add(canardOuterSectionAirfoilDetailsPanel1Button);

		// control surfaces
		choiceBoxCanardControlSurfaceTypeList= new ArrayList<>();
		choiceBoxCanardControlSurfaceTypeList.add(canardControlSurface1TypeChoiceBox);
		
		textFieldCanardInnerPositionControlSurfaceList = new ArrayList<>();
		textFieldCanardInnerPositionControlSurfaceList.add(textFieldCanardInnerPositionControlSurface1);

		textFieldCanardOuterPositionControlSurfaceList = new ArrayList<>();
		textFieldCanardOuterPositionControlSurfaceList.add(textFieldCanardOuterPositionControlSurface1);

		textFieldCanardInnerChordRatioControlSurfaceList = new ArrayList<>();
		textFieldCanardInnerChordRatioControlSurfaceList.add(textFieldCanardInnerChordRatioControlSurface1);

		textFieldCanardOuterChordRatioControlSurfaceList = new ArrayList<>();
		textFieldCanardOuterChordRatioControlSurfaceList.add(textFieldCanardOuterChordRatioControlSurface1);

		textFieldCanardMinimumDeflectionAngleControlSurfaceList = new ArrayList<>();
		textFieldCanardMinimumDeflectionAngleControlSurfaceList.add(textFieldCanardMinimumDeflectionAngleControlSurface1);

		textFieldCanardMaximumDeflectionAngleControlSurfaceList = new ArrayList<>();
		textFieldCanardMaximumDeflectionAngleControlSurfaceList.add(textFieldCanardMaximumDeflectionAngleControlSurface1);

		choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList = new ArrayList<>();
		choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList.add(canardMinimumDeflectionAngleControlSurface1UnitChoiceBox);

		choiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList = new ArrayList<>();
		choiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList.add(canardMaximumDeflectionAngleControlSurface1UnitChoiceBox);
		
		//.......................................................................................
		// NACELLE LISTS INITIALIZATION
		textFieldNacelleRoughnessList = new ArrayList<>();
		textFieldNacelleRoughnessList.add(textFieldNacelleRoughness1);
		
		textFieldNacelleLengthList = new ArrayList<>();
		textFieldNacelleLengthList.add(textFieldNacelleLength1);
		
		textFieldNacelleMaximumDiameterList = new ArrayList<>();
		textFieldNacelleMaximumDiameterList.add(textFieldNacelleMaximumDiameter1);
		
		textFieldNacelleKInletList = new ArrayList<>();
		textFieldNacelleKInletList.add(textFieldNacelleKInlet1);
		
		textFieldNacelleKOutletList = new ArrayList<>();
		textFieldNacelleKOutletList.add(textFieldNacelleKOutlet1);
		
		textFieldNacelleKLengthList = new ArrayList<>();
		textFieldNacelleKLengthList.add(textFieldNacelleKLength1);
		
		textFieldNacelleKDiameterOutletList = new ArrayList<>();
		textFieldNacelleKDiameterOutletList.add(textFieldNacelleKDiameterOutlet1);
		
		choiceBoxNacelleRoughnessUnitList = new ArrayList<>();
		choiceBoxNacelleRoughnessUnitList.add(nacelleRoughnessUnitChoiceBox1);
		
		choiceBoxNacelleLengthUnitList = new ArrayList<>();
		choiceBoxNacelleLengthUnitList.add(nacelleLengthUnitChoiceBox1);
		
		choiceBoxNacelleMaximumDiameterUnitList = new ArrayList<>();
		choiceBoxNacelleMaximumDiameterUnitList.add(nacelleMaximumDiameterUnitChoiceBox1);
		
		nacelleEstimateDimesnsionButtonList = new ArrayList<>();
		nacelleEstimateDimesnsionButtonList.add(nacelleEstimateGeometryButton1);
		
		nacelleKInletInfoButtonList = new ArrayList<>();
		nacelleKInletInfoButtonList.add(nacelleKInletInfoButton1);
		
		nacelleKOutletInfoButtonList = new ArrayList<>();
		nacelleKOutletInfoButtonList.add(nacelleKOutletInfoButton1);
		
		nacelleKLengthInfoButtonList = new ArrayList<>();
		nacelleKLengthInfoButtonList.add(nacelleKLengthInfoButton1);
		
		nacelleKDiameterOutletInfoButtonList = new ArrayList<>();
		nacelleKDiameterOutletInfoButtonList.add(nacelleKDiameterOutletInfoButton1);
		
		nacelleTopViewPaneList = new ArrayList<>();
		nacelleTopViewPaneList.add(nacelle1TopViewPane);
		
		nacelleSideViewPaneList = new ArrayList<>();
		nacelleSideViewPaneList.add(nacelle1SideViewPane);
		
		nacelleFrontViewPaneList = new ArrayList<>();
		nacelleFrontViewPaneList.add(nacelle1FrontViewPane);
		
		//.......................................................................................
		// POWER PLANT LISTS INITIALIZATION
		powerPlantToggleGroup1 = new ToggleGroup();
		powerPlantJetRadioButton1.setToggleGroup(powerPlantToggleGroup1);
		powerPlantTurbopropRadioButton1.setToggleGroup(powerPlantToggleGroup1);
		powerPlantPistonRadioButton1.setToggleGroup(powerPlantToggleGroup1);
		
		powerPlantJetRadioButtonList = new ArrayList<>();
		powerPlantJetRadioButtonList.add(powerPlantJetRadioButton1);
		
		powerPlantTurbopropRadioButtonList = new ArrayList<>();
		powerPlantTurbopropRadioButtonList.add(powerPlantTurbopropRadioButton1);
		
		powerPlantPistonRadioButtonList = new ArrayList<>();
		powerPlantPistonRadioButtonList.add(powerPlantPistonRadioButton1);
		
		powerPlantToggleGropuList = new ArrayList<>();
		powerPlantToggleGropuList.add(powerPlantToggleGroup1);
		
		powerPlantPaneMap = new HashMap<>();
		powerPlantBorderPaneMap = new HashMap<>();
		powerPlantBorderPaneMap.put(0, engine1BorderPane);
		
		engineTurbojetTurbofanTypeChoiceBoxMap = new HashMap<>(); 
		engineTurbojetTurbofanDatabaseTextFieldMap = new HashMap<>();
		engineTurbojetTurbofanLengthTextFieldMap = new HashMap<>();
		engineTurbojetTurbofanLengthUnitChoiceBoxMap = new HashMap<>();
		engineTurbojetTurbofanDryMassTextFieldMap = new HashMap<>();
		engineTurbojetTurbofanDryMassUnitChoiceBoxMap = new HashMap<>();
		engineTurbojetTurbofanStaticThrustTextFieldMap = new HashMap<>();
		engineTurbojetTurbofanStaticThrustUnitChoiceBoxMap = new HashMap<>();
		engineTurbojetTurbofanBPRTextFieldMap = new HashMap<>();
		engineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap = new HashMap<>();
		engineTurbojetTurbofanNumberOfShaftsTextFieldMap = new HashMap<>();
		engineTurbojetTurbofanOverallPressureRatioTextFieldMap = new HashMap<>();
		
		engineTurbopropTypeChoiceBoxMap = new HashMap<>(); 
		engineTurbopropDatabaseTextFieldMap = new HashMap<>();
		engineTurbopropLengthTextFieldMap = new HashMap<>();
		engineTurbopropLengthUnitChoiceBoxMap = new HashMap<>();
		engineTurbopropDryMassTextFieldMap = new HashMap<>();
		engineTurbopropDryMassUnitChoiceBoxMap = new HashMap<>();
		engineTurbopropStaticPowerTextFieldMap = new HashMap<>();
		engineTurbopropStaticPowerUnitChoiceBoxMap = new HashMap<>();
		engineTurbopropPropellerDiameterTextFieldMap = new HashMap<>();
		engineTurbopropPropellerDiameterUnitChoiceBoxMap = new HashMap<>();
		engineTurbopropNumberOfBladesTextFieldMap = new HashMap<>();
		engineTurbopropPropellerEfficiencyTextFieldMap = new HashMap<>();
		engineTurbopropNumberOfCompressorStagesTextFieldMap = new HashMap<>();
		engineTurbopropNumberOfShaftsTextFieldMap = new HashMap<>();
		engineTurbopropOverallPressureRatioTextFieldMap = new HashMap<>();
		
		enginePistonTypeChoiceBoxMap = new HashMap<>(); 
		enginePistonDatabaseTextFieldMap = new HashMap<>();
		enginePistonLengthTextFieldMap = new HashMap<>();
		enginePistonLengthUnitChoiceBoxMap = new HashMap<>();
		enginePistonDryMassTextFieldMap = new HashMap<>();
		enginePistonDryMassUnitChoiceBoxMap = new HashMap<>();
		enginePistonStaticPowerTextFieldMap = new HashMap<>();
		enginePistonStaticPowerUnitChoiceBoxMap = new HashMap<>();
		enginePistonPropellerDiameterTextFieldMap = new HashMap<>();
		enginePistonPropellerDiameterUnitChoiceBoxMap = new HashMap<>();
		enginePistonNumberOfBladesTextFieldMap = new HashMap<>();
		enginePistonPropellerEfficiencyTextFieldMap = new HashMap<>();
		
		aircraftLoadButtonDisableCheck();
		setChooseNacelleFileAction();
		setChooseEngineFileAction();
		cabinConfigurationClassesNumberDisableCheck();
		checkCabinConfigurationClassesNumber();
		setAirfoilDetailsActionAndDisableCheck(equivalentWingAirfoilRootDetailButton, textFieldEquivalentWingAirfoilRootPath, ComponentEnum.WING);
		setAirfoilDetailsActionAndDisableCheck(equivalentWingAirfoilKinkDetailButton, textFieldEquivalentWingAirfoilKinkPath, ComponentEnum.WING);
		setAirfoilDetailsActionAndDisableCheck(equivalentWingAirfoilTipDetailButton, textFieldEquivalentWingAirfoilTipPath, ComponentEnum.WING);
		setAirfoilDetailsActionAndDisableCheck(wingInnerSectionAirfoilDetailsPanel1Button, textFieldWingAirfoilPathInnerSectionPanel1, ComponentEnum.WING);
		setAirfoilDetailsActionAndDisableCheck(wingOuterSectionAirfoilDetailsPanel1Button, textFieldWingAirfoilPathOuterSectionPanel1, ComponentEnum.WING);
		setAirfoilDetailsActionAndDisableCheck(hTailInnerSectionAirfoilDetailsPanel1Button, textFieldHTailAirfoilPathInnerSectionPanel1, ComponentEnum.HORIZONTAL_TAIL);
		setAirfoilDetailsActionAndDisableCheck(hTailOuterSectionAirfoilDetailsPanel1Button, textFieldHTailAirfoilPathOuterSectionPanel1, ComponentEnum.HORIZONTAL_TAIL);
		setAirfoilDetailsActionAndDisableCheck(vTailInnerSectionAirfoilDetailsPanel1Button, textFieldVTailAirfoilPathInnerSectionPanel1, ComponentEnum.VERTICAL_TAIL);
		setAirfoilDetailsActionAndDisableCheck(vTailOuterSectionAirfoilDetailsPanel1Button, textFieldVTailAirfoilPathOuterSectionPanel1, ComponentEnum.VERTICAL_TAIL);
		setAirfoilDetailsActionAndDisableCheck(canardInnerSectionAirfoilDetailsPanel1Button, textFieldCanardAirfoilPathInnerSectionPanel1, ComponentEnum.CANARD);
		setAirfoilDetailsActionAndDisableCheck(canardOuterSectionAirfoilDetailsPanel1Button, textFieldCanardAirfoilPathOuterSectionPanel1, ComponentEnum.CANARD);
		setChooseAirfoilFileAction(equivalentWingChooseAirfoilRootButton, textFieldEquivalentWingAirfoilRootPath);
		setChooseAirfoilFileAction(equivalentWingChooseAirfoilKinkButton, textFieldEquivalentWingAirfoilKinkPath);
		setChooseAirfoilFileAction(equivalentWingChooseAirfoilTipButton, textFieldEquivalentWingAirfoilTipPath);
		setChooseAirfoilFileAction(wingChooseInnerAirfoilPanel1Button, textFieldWingAirfoilPathInnerSectionPanel1);
		setChooseAirfoilFileAction(wingChooseOuterAirfoilPanel1Button, textFieldWingAirfoilPathOuterSectionPanel1);
		setChooseAirfoilFileAction(hTailChooseInnerAirfoilPanel1Button, textFieldHTailAirfoilPathInnerSectionPanel1);
		setChooseAirfoilFileAction(hTailChooseOuterAirfoilPanel1Button, textFieldHTailAirfoilPathOuterSectionPanel1);
		setChooseAirfoilFileAction(vTailChooseInnerAirfoilPanel1Button, textFieldVTailAirfoilPathInnerSectionPanel1);
		setChooseAirfoilFileAction(vTailChooseOuterAirfoilPanel1Button, textFieldVTailAirfoilPathOuterSectionPanel1);
		setChooseAirfoilFileAction(canardChooseInnerAirfoilPanel1Button, textFieldCanardAirfoilPathInnerSectionPanel1);
		setChooseAirfoilFileAction(canardChooseOuterAirfoilPanel1Button, textFieldCanardAirfoilPathOuterSectionPanel1);
		equivalentWingDisableCheck();
		linkedToDisableCheck(ComponentEnum.WING);
		linkedToDisableCheck(ComponentEnum.HORIZONTAL_TAIL);
		linkedToDisableCheck(ComponentEnum.VERTICAL_TAIL);
		linkedToDisableCheck(ComponentEnum.CANARD);
		setEstimateNacelleGeometryAction(nacelleEstimateGeometryButton1, tabPaneNacelles.getTabs().get(0));
		setShowEngineDataAction(powerPlantJetRadioButton1, 0, EngineTypeEnum.TURBOFAN);
		setShowEngineDataAction(powerPlantTurbopropRadioButton1, 0, EngineTypeEnum.TURBOPROP);
		setShowEngineDataAction(powerPlantPistonRadioButton1, 0, EngineTypeEnum.PISTON);
		fuselageAdjustCriterionDisableCheck();
		wingAdjustCriterionDisableCheck();
		hTailAdjustCriterionDisableCheck();
		vTailAdjustCriterionDisableCheck();
		canardAdjustCriterionDisableCheck();
		
		System.gc();
		
	}
	
	private void removeContentOnAircraftEngineTabClose (Tab tab) {

		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {

				int indexEngine = tabPaneAircraftEngines.getTabs().indexOf(tab);

				textFieldsAircraftEngineFileList.remove(indexEngine);
				textFieldAircraftEngineXList.remove(indexEngine);
				textFieldAircraftEngineYList.remove(indexEngine);
				textFieldAircraftEngineZList.remove(indexEngine);
				textFieldAircraftEngineTiltList.remove(indexEngine);
				choiceBoxesAircraftEnginePositonList.remove(indexEngine);

				choiceBoxAircraftEngineXUnitList.remove(indexEngine);
				choiceBoxAircraftEngineYUnitList.remove(indexEngine);
				choiceBoxAircraftEngineZUnitList.remove(indexEngine);
				choiceBoxAircraftEngineTiltUnitList.remove(indexEngine);
				
			}
		});
	}
	
	private void removeContentOnAircraftNacelleTabClose (Tab tab) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
		
				int indexNacelle = tabPaneAircraftNacelles.getTabs().indexOf(tab);

				textFieldsAircraftNacelleFileList.remove(indexNacelle);
				textFieldAircraftNacelleXList.remove(indexNacelle);
				textFieldAircraftNacelleYList.remove(indexNacelle);
				textFieldAircraftNacelleZList.remove(indexNacelle);
				choiceBoxesAircraftNacellePositonList.remove(indexNacelle);

				choiceBoxAircraftNacelleXUnitList.remove(indexNacelle);
				choiceBoxAircraftNacelleYUnitList.remove(indexNacelle);
				choiceBoxAircraftNacelleZUnitList.remove(indexNacelle);
				
			}
		});
	}
	
	private void removeContentOnNacelleTabClose (Tab tab) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
		
				int indexNacelle = tabPaneNacelles.getTabs().indexOf(tab);

				textFieldNacelleRoughnessList.remove(indexNacelle);
				textFieldNacelleLengthList.remove(indexNacelle);
				textFieldNacelleMaximumDiameterList.remove(indexNacelle);
				textFieldNacelleRoughnessList.remove(indexNacelle);
				textFieldNacelleKInletList.remove(indexNacelle);
				textFieldNacelleKOutletList.remove(indexNacelle);
				textFieldNacelleKLengthList.remove(indexNacelle);
				textFieldNacelleKDiameterOutletList.remove(indexNacelle);
				
				choiceBoxNacelleRoughnessUnitList.remove(indexNacelle);
				choiceBoxNacelleLengthUnitList.remove(indexNacelle);
				choiceBoxNacelleMaximumDiameterUnitList.remove(indexNacelle);
				
				nacelleEstimateDimesnsionButtonList.remove(indexNacelle);
				nacelleKInletInfoButtonList.remove(indexNacelle);
				nacelleKOutletInfoButtonList.remove(indexNacelle);
				nacelleKLengthInfoButtonList.remove(indexNacelle);
				nacelleKDiameterOutletInfoButtonList.remove(indexNacelle);
				
			}
		});
	}
	
	private void removeContentOnEngineTabClose (Tab tab) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
		
				int indexEngine = tabPaneEngines.getTabs().indexOf(tab);
				powerPlantJetRadioButtonList.remove(indexEngine);
				powerPlantTurbopropRadioButtonList.remove(indexEngine);
				powerPlantPistonRadioButtonList.remove(indexEngine);
				powerPlantToggleGropuList.remove(indexEngine);
				
				powerPlantBorderPaneMap.remove(indexEngine);
				powerPlantPaneMap.remove(indexEngine);
				
			}
		});
	}
	
	private void removeAirfoilDetailsButtonFromMapOnTabClose(Tab tab, ComponentEnum type) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				
				switch (type) {
				case WING:
					MyMapUtils.removeEntryByValue(
							wingAirfoilDetailsButtonAndTabsMap,
							tabPaneWingViewAndAirfoils.getTabs().indexOf(tab)
							);
					break;
					
				case HORIZONTAL_TAIL:
					MyMapUtils.removeEntryByValue(
							hTailAirfoilDetailsButtonAndTabsMap,
							tabPaneHTailViewAndAirfoils.getTabs().indexOf(tab)
							);
					break;

				case VERTICAL_TAIL:
					MyMapUtils.removeEntryByValue(
							vTailAirfoilDetailsButtonAndTabsMap,
							tabPaneVTailViewAndAirfoils.getTabs().indexOf(tab)
							);
					break;
					
				case CANARD:
					MyMapUtils.removeEntryByValue(
							canardAirfoilDetailsButtonAndTabsMap,
							tabPaneCanardViewAndAirfoils.getTabs().indexOf(tab)
							);
					break;
					
				default:
					break;
				}
				
			}
		});
		
	}
	
	private void removeContentOnPanelTabClose(Tab tab, ComponentEnum type) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {
			
			@Override
			public void handle(Event event) {
				
				switch (type) {
				case WING:
					int indexWing = tabPaneWingPanels.getTabs().indexOf(tab); 
					
					if (indexWing > 0)
						checkBoxWingLinkedToPreviousPanelList.remove(indexWing-1);
					
					textFieldWingSpanPanelList.remove(indexWing);
					textFieldWingSweepLEPanelList.remove(indexWing);
					textFieldWingDihedralPanelList.remove(indexWing);
					textFieldWingInnerChordPanelList.remove(indexWing);
					textFieldWingInnerTwistPanelList.remove(indexWing);
					textFieldWingInnerAirfoilPanelList.remove(indexWing);
					textFieldWingOuterChordPanelList.remove(indexWing);
					textFieldWingOuterTwistPanelList.remove(indexWing);
					textFieldWingOuterAirfoilPanelList.remove(indexWing);
					
					choiceBoxWingSpanPanelUnitList.remove(indexWing);
					choiceBoxWingSweepLEPanelUnitList.remove(indexWing);
					choiceBoxWingDihedralPanelUnitList.remove(indexWing);
					choiceBoxWingInnerChordPanelUnitList.remove(indexWing);
					choiceBoxWingInnerTwistPanelUnitList.remove(indexWing);
					choiceBoxWingOuterChordPanelUnitList.remove(indexWing);
					choiceBoxWingOuterTwistPanelUnitList.remove(indexWing);
					
					chooseInnerWingAirfoilFileButtonList.remove(indexWing);
					chooseOuterWingAirfoilFileButtonList.remove(indexWing);
					detailButtonWingInnerAirfoilList.remove(indexWing);
					detailButtonWingOuterAirfoilList.remove(indexWing);
					break;
					
				case HORIZONTAL_TAIL:
					int indexHTail = tabPaneHTailPanels.getTabs().indexOf(tab); 
					
					if (indexHTail > 0)
						checkBoxHTailLinkedToPreviousPanelList.remove(indexHTail-1);
					
					textFieldHTailSpanPanelList.remove(indexHTail);
					textFieldHTailSweepLEPanelList.remove(indexHTail);
					textFieldHTailDihedralPanelList.remove(indexHTail);
					textFieldHTailInnerChordPanelList.remove(indexHTail);
					textFieldHTailInnerTwistPanelList.remove(indexHTail);
					textFieldHTailInnerAirfoilPanelList.remove(indexHTail);
					textFieldHTailOuterChordPanelList.remove(indexHTail);
					textFieldHTailOuterTwistPanelList.remove(indexHTail);
					textFieldHTailOuterAirfoilPanelList.remove(indexHTail);
					
					choiceBoxHTailSpanPanelUnitList.remove(indexHTail);
					choiceBoxHTailSweepLEPanelUnitList.remove(indexHTail);
					choiceBoxHTailDihedralPanelUnitList.remove(indexHTail);
					choiceBoxHTailInnerChordPanelUnitList.remove(indexHTail);
					choiceBoxHTailInnerTwistPanelUnitList.remove(indexHTail);
					choiceBoxHTailOuterChordPanelUnitList.remove(indexHTail);
					choiceBoxHTailOuterTwistPanelUnitList.remove(indexHTail);
					
					chooseInnerHTailAirfoilFileButtonList.remove(indexHTail);
					chooseOuterHTailAirfoilFileButtonList.remove(indexHTail);
					detailButtonHTailInnerAirfoilList.remove(indexHTail);
					detailButtonHTailOuterAirfoilList.remove(indexHTail);
					break;
					
				case VERTICAL_TAIL:
					int indexVTail = tabPaneVTailPanels.getTabs().indexOf(tab); 
					
					if (indexVTail > 0)
						checkBoxVTailLinkedToPreviousPanelList.remove(indexVTail-1);
					
					textFieldVTailSpanPanelList.remove(indexVTail);
					textFieldVTailSweepLEPanelList.remove(indexVTail);
					textFieldVTailDihedralPanelList.remove(indexVTail);
					textFieldVTailInnerChordPanelList.remove(indexVTail);
					textFieldVTailInnerTwistPanelList.remove(indexVTail);
					textFieldVTailInnerAirfoilPanelList.remove(indexVTail);
					textFieldVTailOuterChordPanelList.remove(indexVTail);
					textFieldVTailOuterTwistPanelList.remove(indexVTail);
					textFieldVTailOuterAirfoilPanelList.remove(indexVTail);
					
					choiceBoxVTailSpanPanelUnitList.remove(indexVTail);
					choiceBoxVTailSweepLEPanelUnitList.remove(indexVTail);
					choiceBoxVTailDihedralPanelUnitList.remove(indexVTail);
					choiceBoxVTailInnerChordPanelUnitList.remove(indexVTail);
					choiceBoxVTailInnerTwistPanelUnitList.remove(indexVTail);
					choiceBoxVTailOuterChordPanelUnitList.remove(indexVTail);
					choiceBoxVTailOuterTwistPanelUnitList.remove(indexVTail);
					
					chooseInnerVTailAirfoilFileButtonList.remove(indexVTail);
					chooseOuterVTailAirfoilFileButtonList.remove(indexVTail);
					detailButtonVTailInnerAirfoilList.remove(indexVTail);
					detailButtonVTailOuterAirfoilList.remove(indexVTail);
					break;
					
				case CANARD:
					int indexCanard = tabPaneCanardPanels.getTabs().indexOf(tab); 
					
					if (indexCanard > 0)
						checkBoxCanardLinkedToPreviousPanelList.remove(indexCanard-1);
					
					textFieldCanardSpanPanelList.remove(indexCanard);
					textFieldCanardSweepLEPanelList.remove(indexCanard);
					textFieldCanardDihedralPanelList.remove(indexCanard);
					textFieldCanardInnerChordPanelList.remove(indexCanard);
					textFieldCanardInnerTwistPanelList.remove(indexCanard);
					textFieldCanardInnerAirfoilPanelList.remove(indexCanard);
					textFieldCanardOuterChordPanelList.remove(indexCanard);
					textFieldCanardOuterTwistPanelList.remove(indexCanard);
					textFieldCanardOuterAirfoilPanelList.remove(indexCanard);
					
					choiceBoxCanardSpanPanelUnitList.remove(indexCanard);
					choiceBoxCanardSweepLEPanelUnitList.remove(indexCanard);
					choiceBoxCanardDihedralPanelUnitList.remove(indexCanard);
					choiceBoxCanardInnerChordPanelUnitList.remove(indexCanard);
					choiceBoxCanardInnerTwistPanelUnitList.remove(indexCanard);
					choiceBoxCanardOuterChordPanelUnitList.remove(indexCanard);
					choiceBoxCanardOuterTwistPanelUnitList.remove(indexCanard);
					
					chooseInnerCanardAirfoilFileButtonList.remove(indexCanard);
					chooseOuterCanardAirfoilFileButtonList.remove(indexCanard);
					detailButtonCanardInnerAirfoilList.remove(indexCanard);
					detailButtonCanardOuterAirfoilList.remove(indexCanard);
					break;
					
				default:
					break;
				}
			}
		});
		
	}

	private void removeContentOnFlapTabClose(Tab tab, ComponentEnum type) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				
				switch (type) {
				case WING:
					int indexWing = tabPaneWingFlaps.getTabs().indexOf(tab);
					
					textFieldWingInnerPositionFlapList.remove(indexWing);
					textFieldWingOuterPositionFlapList.remove(indexWing);
					textFieldWingInnerChordRatioFlapList.remove(indexWing);
					textFieldWingOuterChordRatioFlapList.remove(indexWing);
					textFieldWingMinimumDeflectionAngleFlapList.remove(indexWing);
					textFieldWingMaximumDeflectionAngleFlapList.remove(indexWing);
					
					choiceBoxWingMinimumDeflectionAngleFlapUnitList.remove(indexWing);
					choiceBoxWingMaximumDeflectionAngleFlapUnitList.remove(indexWing);					
					break;

				case HORIZONTAL_TAIL:
					int indexHTail = tabPaneHTailElevators.getTabs().indexOf(tab);
					
					textFieldHTailInnerPositionElevatorList.remove(indexHTail);
					textFieldHTailOuterPositionElevatorList.remove(indexHTail);
					textFieldHTailInnerChordRatioElevatorList.remove(indexHTail);
					textFieldHTailOuterChordRatioElevatorList.remove(indexHTail);
					textFieldHTailMinimumDeflectionAngleElevatorList.remove(indexHTail);
					textFieldHTailMaximumDeflectionAngleElevatorList.remove(indexHTail);
					
					choiceBoxHTailMinimumDeflectionAngleElevatorUnitList.remove(indexHTail);
					choiceBoxHTailMaximumDeflectionAngleElevatorUnitList.remove(indexHTail);
					break;
					
				case VERTICAL_TAIL:
					int indexVTail = tabPaneVTailRudders.getTabs().indexOf(tab);
					
					textFieldVTailInnerPositionRudderList.remove(indexVTail);
					textFieldVTailOuterPositionRudderList.remove(indexVTail);
					textFieldVTailInnerChordRatioRudderList.remove(indexVTail);
					textFieldVTailOuterChordRatioRudderList.remove(indexVTail);
					textFieldVTailMinimumDeflectionAngleRudderList.remove(indexVTail);
					textFieldVTailMaximumDeflectionAngleRudderList.remove(indexVTail);
					
					choiceBoxVTailMinimumDeflectionAngleRudderUnitList.remove(indexVTail);
					choiceBoxVTailMaximumDeflectionAngleRudderUnitList.remove(indexVTail);
					break;
					
				case CANARD:
					int indexCanard = tabPaneCanardControlSurfaces.getTabs().indexOf(tab);
					
					textFieldCanardInnerPositionControlSurfaceList.remove(indexCanard);
					textFieldCanardOuterPositionControlSurfaceList.remove(indexCanard);
					textFieldCanardInnerChordRatioControlSurfaceList.remove(indexCanard);
					textFieldCanardOuterChordRatioControlSurfaceList.remove(indexCanard);
					textFieldCanardMinimumDeflectionAngleControlSurfaceList.remove(indexCanard);
					textFieldCanardMaximumDeflectionAngleControlSurfaceList.remove(indexCanard);
					
					choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList.remove(indexCanard);
					choiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList.remove(indexCanard);
					break;
					
				default:
					break;
				}
			}
		});
		
	}
	
	private void removeContentOnSlatTabClose(Tab tab) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				
				int index = tabPaneWingSlats.getTabs().indexOf(tab);
				
				textFieldWingInnerPositionSlatList.remove(index);
				textFieldWingOuterPositionSlatList.remove(index);
				textFieldWingInnerChordRatioSlatList.remove(index);
				textFieldWingOuterChordRatioSlatList.remove(index);
				textFieldWingExtensionRatioSlatList.remove(index);
				textFieldWingMinimumDeflectionAngleSlatList.remove(index);
				textFieldWingMaximumDeflectionAngleSlatList.remove(index);
				
				choiceBoxWingMinimumDeflectionAngleSlatUnitList.remove(index);
				choiceBoxWingMaximumDeflectionAngleSlatUnitList.remove(index);
				
			}
		});
		
	}
	
	private void removeContentOnSpoilerTabClose (Tab tab, ComponentEnum type) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				
				switch (type) {
				case WING:
					int indexWing = tabPaneWingSpoilers.getTabs().indexOf(tab);
					
					textFieldWingInnerSpanwisePositionSpoilerList.remove(indexWing);
					textFieldWingOuterSpanwisePositionSpoilerList.remove(indexWing);
					textFieldWingInnerChordwisePositionSpoilerList.remove(indexWing);
					textFieldWingOuterChordwisePositionSpoilerList.remove(indexWing);
					textFieldWingMinimumDeflectionAngleSpoilerList.remove(indexWing);
					textFieldWingMaximumDeflectionAngleSpoilerList.remove(indexWing);
					
					choiceBoxWingMinimumDeflectionAngleSpoilerUnitList.remove(indexWing);
					choiceBoxWingMaximumDeflectionAngleSpoilerUnitList.remove(indexWing);					
					break;
					
				case FUSELAGE:
					int indexFuselage = tabPaneFuselageSpoilers.getTabs().indexOf(tab);
					
					textFieldFuselageInnerSpanwisePositionSpoilerList.remove(indexFuselage);
					textFieldFuselageOuterSpanwisePositionSpoilerList.remove(indexFuselage);
					textFieldFuselageInnerChordwisePositionSpoilerList.remove(indexFuselage);
					textFieldFuselageOuterChordwisePositionSpoilerList.remove(indexFuselage);
					textFieldFuselageMinimumDeflectionAngleSpoilerList.remove(indexFuselage);
					textFieldFuselageMaximumDeflectionAngleSpoilerList.remove(indexFuselage);
					
					choiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList.remove(indexFuselage);
					choiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList.remove(indexFuselage);
					break;

				default:
					break;
				}
			}
		});
		
	}
	
	private void linkedToDisableCheck(ComponentEnum type) {

		switch (type) {
		case WING:
			for(int i=0; i<checkBoxWingLinkedToPreviousPanelList.size(); i++) {
				
				textFieldWingInnerChordPanelList.get(i+1).disableProperty().bind(
						checkBoxWingLinkedToPreviousPanelList.get(i).selectedProperty()
						);	
				choiceBoxWingInnerChordPanelUnitList.get(i+1).disableProperty().bind(
						checkBoxWingLinkedToPreviousPanelList.get(i).selectedProperty()
						);	
				textFieldWingInnerTwistPanelList.get(i+1).disableProperty().bind(
						checkBoxWingLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				choiceBoxWingInnerTwistPanelUnitList.get(i+1).disableProperty().bind(
						checkBoxWingLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				textFieldWingInnerAirfoilPanelList.get(i+1).disableProperty().bind(
						checkBoxWingLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				detailButtonWingInnerAirfoilList.get(i+1).disableProperty().bind(
						checkBoxWingLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				chooseInnerWingAirfoilFileButtonList.get(i+1).disableProperty().bind(
						checkBoxWingLinkedToPreviousPanelList.get(i).selectedProperty()
						);
			}
			break;
			
		case HORIZONTAL_TAIL:
			for(int i=0; i<checkBoxHTailLinkedToPreviousPanelList.size(); i++) {
				
				textFieldHTailInnerChordPanelList.get(i+1).disableProperty().bind(
						checkBoxHTailLinkedToPreviousPanelList.get(i).selectedProperty()
						);	
				choiceBoxHTailInnerChordPanelUnitList.get(i+1).disableProperty().bind(
						checkBoxHTailLinkedToPreviousPanelList.get(i).selectedProperty()
						);	
				textFieldHTailInnerTwistPanelList.get(i+1).disableProperty().bind(
						checkBoxHTailLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				choiceBoxHTailInnerTwistPanelUnitList.get(i+1).disableProperty().bind(
						checkBoxHTailLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				textFieldHTailInnerAirfoilPanelList.get(i+1).disableProperty().bind(
						checkBoxHTailLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				detailButtonHTailInnerAirfoilList.get(i+1).disableProperty().bind(
						checkBoxHTailLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				chooseInnerHTailAirfoilFileButtonList.get(i+1).disableProperty().bind(
						checkBoxHTailLinkedToPreviousPanelList.get(i).selectedProperty()
						);
			}
			break;
			
		case VERTICAL_TAIL:
			for(int i=0; i<checkBoxVTailLinkedToPreviousPanelList.size(); i++) {
				
				textFieldVTailInnerChordPanelList.get(i+1).disableProperty().bind(
						checkBoxVTailLinkedToPreviousPanelList.get(i).selectedProperty()
						);	
				choiceBoxVTailInnerChordPanelUnitList.get(i+1).disableProperty().bind(
						checkBoxVTailLinkedToPreviousPanelList.get(i).selectedProperty()
						);	
				textFieldVTailInnerTwistPanelList.get(i+1).disableProperty().bind(
						checkBoxVTailLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				choiceBoxVTailInnerTwistPanelUnitList.get(i+1).disableProperty().bind(
						checkBoxVTailLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				textFieldVTailInnerAirfoilPanelList.get(i+1).disableProperty().bind(
						checkBoxVTailLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				detailButtonVTailInnerAirfoilList.get(i+1).disableProperty().bind(
						checkBoxVTailLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				chooseInnerVTailAirfoilFileButtonList.get(i+1).disableProperty().bind(
						checkBoxVTailLinkedToPreviousPanelList.get(i).selectedProperty()
						);
			}
			break;

		case CANARD:
			for(int i=0; i<checkBoxCanardLinkedToPreviousPanelList.size(); i++) {
				
				textFieldCanardInnerChordPanelList.get(i+1).disableProperty().bind(
						checkBoxCanardLinkedToPreviousPanelList.get(i).selectedProperty()
						);	
				choiceBoxCanardInnerChordPanelUnitList.get(i+1).disableProperty().bind(
						checkBoxCanardLinkedToPreviousPanelList.get(i).selectedProperty()
						);	
				textFieldCanardInnerTwistPanelList.get(i+1).disableProperty().bind(
						checkBoxCanardLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				choiceBoxCanardInnerTwistPanelUnitList.get(i+1).disableProperty().bind(
						checkBoxCanardLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				textFieldCanardInnerAirfoilPanelList.get(i+1).disableProperty().bind(
						checkBoxCanardLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				detailButtonCanardInnerAirfoilList.get(i+1).disableProperty().bind(
						checkBoxCanardLinkedToPreviousPanelList.get(i).selectedProperty()
						);
				chooseInnerCanardAirfoilFileButtonList.get(i+1).disableProperty().bind(
						checkBoxCanardLinkedToPreviousPanelList.get(i).selectedProperty()
						);
			}
			break;
			
		default:
			break;
		}
	}

	private void setChooseAirfoilFileAction (Button chooseFileButton, TextField airfoilPathTextField) {
		
		chooseFileButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				chooseAirfoilFile(airfoilPathTextField);
			}
		});
		
	}
	
	private void setChooseNacelleFileAction() {
		
		for(int i=0; i<tabPaneAircraftNacelles.getTabs().size(); i++) {
			
			int indexOfNacelle = i;
			chooseNacelleFileButtonList.get(i).setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {
					try {
						chooseAircraftNacelleFile(indexOfNacelle);
					} catch (IOException e) {
						e.printStackTrace();
					}					
				}
			});
		}
	}
	
	private void setChooseEngineFileAction() {
		
		for(int i=0; i<tabPaneAircraftEngines.getTabs().size(); i++) {
			
			int indexOfEngine = i;
			chooseEngineFileButtonList.get(i).setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					try {
						chooseAircraftEngineFile(indexOfEngine);
					} catch (IOException e) {
						e.printStackTrace();
					}					
				}
			});
		}
	}
	
	private void setAirfoilDetailsActionAndDisableCheck (Button detailsButton, TextField airfoilPathTextField, ComponentEnum type) {

		detailsButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				switch (type) {
				case WING:
					if(!wingAirfoilDetailsButtonAndTabsMap.containsKey(detailsButton)) {
						try {
							wingAirfoilDetailsButtonAndTabsMap.put(
									detailsButton, 
									tabPaneWingViewAndAirfoils.getTabs().size()
									);
							showAirfoilData(
									Paths.get(airfoilPathTextField.getText()).getFileName().toString(),
									detailsButton,
									type
									);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
					
				case HORIZONTAL_TAIL:
					if(!hTailAirfoilDetailsButtonAndTabsMap.containsKey(detailsButton)) {
						try {
							hTailAirfoilDetailsButtonAndTabsMap.put(
									detailsButton, 
									tabPaneHTailViewAndAirfoils.getTabs().size()
									);
							showAirfoilData(
									Paths.get(airfoilPathTextField.getText()).getFileName().toString(),
									detailsButton,
									type
									);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
					
				case VERTICAL_TAIL:
					if(!vTailAirfoilDetailsButtonAndTabsMap.containsKey(detailsButton)) {
						try {
							vTailAirfoilDetailsButtonAndTabsMap.put(
									detailsButton, 
									tabPaneVTailViewAndAirfoils.getTabs().size()
									);
							showAirfoilData(
									Paths.get(airfoilPathTextField.getText()).getFileName().toString(),
									detailsButton,
									type
									);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
					
				case CANARD:
					if(!canardAirfoilDetailsButtonAndTabsMap.containsKey(detailsButton)) {
						try {
							canardAirfoilDetailsButtonAndTabsMap.put(
									detailsButton, 
									tabPaneCanardViewAndAirfoils.getTabs().size()
									);
							showAirfoilData(
									Paths.get(airfoilPathTextField.getText()).getFileName().toString(),
									detailsButton,
									type
									);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
					
				default:
					break;
				}
			}
		});
		
		final Tooltip warning = new Tooltip("WARNING : The airfoil details are already opened !!");
		detailsButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				
				switch (type) {
				case WING:
					Point2D pW = detailsButton
					.localToScreen(
							-2.5*detailsButton.getLayoutBounds().getMaxX(),
							1.2*detailsButton.getLayoutBounds().getMaxY()
							);
					if(wingAirfoilDetailsButtonAndTabsMap.containsKey(detailsButton)) {
						warning.show(detailsButton, pW.getX(), pW.getY());
					}
					break;

				case HORIZONTAL_TAIL:
					Point2D pH = detailsButton
					.localToScreen(
							-2.5*detailsButton.getLayoutBounds().getMaxX(),
							1.2*detailsButton.getLayoutBounds().getMaxY()
							);
					if(hTailAirfoilDetailsButtonAndTabsMap.containsKey(detailsButton)) {
						warning.show(detailsButton, pH.getX(), pH.getY());
					}
					break;
					
				case VERTICAL_TAIL:
					Point2D pV = detailsButton
					.localToScreen(
							-2.5*detailsButton.getLayoutBounds().getMaxX(),
							1.2*detailsButton.getLayoutBounds().getMaxY()
							);
					if(vTailAirfoilDetailsButtonAndTabsMap.containsKey(detailsButton)) {
						warning.show(detailsButton, pV.getX(), pV.getY());
					}
					break;
					
				case CANARD:
					Point2D pC = detailsButton
					.localToScreen(
							-2.5*detailsButton.getLayoutBounds().getMaxX(),
							1.2*detailsButton.getLayoutBounds().getMaxY()
							);
					if(canardAirfoilDetailsButtonAndTabsMap.containsKey(detailsButton)) {
						warning.show(detailsButton, pC.getX(), pC.getY());
					}
					break;

				default:
					break;
				}

			}
		});

		detailsButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				warning.hide();
			}
		});

		detailsButton.disableProperty().bind(
				Bindings.isEmpty(airfoilPathTextField.textProperty())
				);	

	}

	private void setShowEngineDataAction (RadioButton radioButton, int indexOfEngineTab, EngineTypeEnum type) {

		radioButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				switch (type) {
				case TURBOFAN:
					if (powerPlantPaneMap.containsKey(indexOfEngineTab)) {
						if(powerPlantPaneMap.get(indexOfEngineTab).containsKey(EngineTypeEnum.TURBOFAN))
							powerPlantBorderPaneMap.get(indexOfEngineTab).setCenter(
									powerPlantPaneMap.get(indexOfEngineTab).get(EngineTypeEnum.TURBOFAN)
									);
						else
							showTurbojetTurboFanDataRadioButton(indexOfEngineTab);
					}
					else
						showTurbojetTurboFanDataRadioButton(indexOfEngineTab);
					break;
				case TURBOPROP:
					if (powerPlantPaneMap.containsKey(indexOfEngineTab)) {
						if(powerPlantPaneMap.get(indexOfEngineTab).containsKey(EngineTypeEnum.TURBOPROP))
							powerPlantBorderPaneMap.get(indexOfEngineTab).setCenter(
									powerPlantPaneMap.get(indexOfEngineTab).get(EngineTypeEnum.TURBOPROP)
									);
						else
							showTurbopropDataRadioButton(indexOfEngineTab);
					}
					else
						showTurbopropDataRadioButton(indexOfEngineTab);
					break;
				case PISTON:
					if (powerPlantPaneMap.containsKey(indexOfEngineTab)) {
						if(powerPlantPaneMap.get(indexOfEngineTab).containsKey(EngineTypeEnum.PISTON))
							powerPlantBorderPaneMap.get(indexOfEngineTab).setCenter(
									powerPlantPaneMap.get(indexOfEngineTab).get(EngineTypeEnum.PISTON)
									);
						else
							showPistonDataRadioButton(indexOfEngineTab);
					}
					else
						showPistonDataRadioButton(indexOfEngineTab);
					break;
				default:
					break;
				}
			}
		});
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
	
	private void checkCabinConfigurationClassesNumber() {
		
		validation.registerValidator(
				textFieldClassesNumber,
				false,
				Validator.createPredicateValidator(
						o -> {
							if(textFieldClassesNumber.getText().equals("") 
									|| !StringUtils.isNumeric(textFieldClassesNumber.getText())
									|| textFieldClassesNumber.getText().length() > 1
									)
								return false;
							else
								return Integer.valueOf(textFieldClassesNumber.getText()) <= 3;
						},
						"The maximum number of classes should be less than or equal to 3",
						Severity.WARNING
						)
				);
	}
	
	private void cabinConfigurationClassesNumberDisableCheck () {

		BooleanBinding cabinConfigurationClassesTypeChoiceBox1Binding = 
				textFieldClassesNumber.textProperty().isNotEqualTo("1")
				.and(textFieldClassesNumber.textProperty().isNotEqualTo("2"))
				.and(textFieldClassesNumber.textProperty().isNotEqualTo("3"));
		BooleanBinding cabinConfigurationClassesTypeChoiceBox2Binding = 
				textFieldClassesNumber.textProperty().isNotEqualTo("2")
				.and(textFieldClassesNumber.textProperty().isNotEqualTo("3"));
		BooleanBinding cabinConfigurationClassesTypeChoiceBox3Binding = 
				textFieldClassesNumber.textProperty().isNotEqualTo("3");
		
		
		cabinConfigurationClassesTypeChoiceBox1.disableProperty().bind(
				cabinConfigurationClassesTypeChoiceBox1Binding
				);
		cabinConfigurationClassesTypeChoiceBox2.disableProperty().bind(
				cabinConfigurationClassesTypeChoiceBox2Binding
				);
		cabinConfigurationClassesTypeChoiceBox3.disableProperty().bind(
				cabinConfigurationClassesTypeChoiceBox3Binding
				);
		textFieldMissingSeatRow1.disableProperty().bind(
				cabinConfigurationClassesTypeChoiceBox1Binding
				);
		textFieldMissingSeatRow2.disableProperty().bind(
				cabinConfigurationClassesTypeChoiceBox2Binding
				);
		textFieldMissingSeatRow3.disableProperty().bind(
				cabinConfigurationClassesTypeChoiceBox3Binding
				);
		
	}
	
	private void equivalentWingDisableCheck () {

		// disable equivalent wing if the check-box is not checked
		textFieldEquivalentWingArea.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		textFieldEquivalentWingAspectRatio.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		textFieldEquivalentWingKinkPosition.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		textFieldEquivalentWingSweepLeadingEdge.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		textFieldEquivalentWingTwistAtTip.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		textFieldEquivalentWingDihedral.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		textFieldEquivalentWingTaperRatio.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		textFieldEquivalentWingRootXOffsetLE.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		textFieldEquivalentWingRootXOffsetTE.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		textFieldEquivalentWingAirfoilRootPath.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		textFieldEquivalentWingAirfoilKinkPath.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		textFieldEquivalentWingAirfoilTipPath.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		equivalentWingAreaUnitChoiceBox.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		equivalentWingSweepLEUnitChoiceBox.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		equivalentWingTwistAtTipUnitChoiceBox.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		equivalentWingDihedralUnitChoiceBox.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		equivalentWingAirfoilRootDetailButton.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		equivalentWingChooseAirfoilRootButton.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		equivalentWingAirfoilKinkDetailButton.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		equivalentWingChooseAirfoilKinkButton.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		equivalentWingAirfoilTipDetailButton.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		equivalentWingChooseAirfoilTipButton.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		equivalentWingInfoButton.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		equivalentWingRootXOffsetLEInfoButton.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		equivalentWingRootXOffseTLEInfoButton.disableProperty().bind(equivalentWingCheckBox.selectedProperty().not());
		
		// disable the panels tab pane if the check-box is checked
		tabPaneWingPanels.disableProperty().bind(equivalentWingCheckBox.selectedProperty());
		wingAddPanelButton.disableProperty().bind(equivalentWingCheckBox.selectedProperty());
		
	}
	
	private void fuselageAdjustCriterionDisableCheck() {
		
		// TODO: SEE HOW TO MATCH THE DATA MODEL WITH THE ADJUST CRITERIONS
		
	}
	
	private void wingAdjustCriterionDisableCheck() {
		
		// TODO: SEE HOW TO MATCH THE DATA MODEL WITH THE ADJUST CRITERIONS
		
	}
	
	private void hTailAdjustCriterionDisableCheck() {
		
		// TODO: SEE HOW TO MATCH THE DATA MODEL WITH THE ADJUST CRITERIONS
		
	}
	
	private void vTailAdjustCriterionDisableCheck() {
		
		// TODO: SEE HOW TO MATCH THE DATA MODEL WITH THE ADJUST CRITERIONS
		
	}
	
	private void canardAdjustCriterionDisableCheck() {
		
		// TODO: SEE HOW TO MATCH THE DATA MODEL WITH THE ADJUST CRITERIONS
		
	}
	
	@FXML
	private void addAircraftEngine() {
		
		Tab newEngineTab = new Tab("Engine " + (tabPaneAircraftEngines.getTabs().size()+1));
		Pane contentPane = new Pane();
		
		Label engineFileLabel = new Label("File:");
		engineFileLabel.setFont(Font.font("System", 15));
		engineFileLabel.setLayoutX(6.0);
		engineFileLabel.setLayoutY(0.0);
		contentPane.getChildren().add(engineFileLabel);
		
		TextField engineFileTextField = new TextField();
		engineFileTextField.setLayoutX(6.0);
		engineFileTextField.setLayoutY(21);
		engineFileTextField.setPrefWidth(340);
		engineFileTextField.setPrefHeight(31);
		contentPane.getChildren().add(engineFileTextField);
		
		Button engineChooseFileButton = new Button("...");
		engineChooseFileButton.setLayoutX(348);
		engineChooseFileButton.setLayoutY(21);
		engineChooseFileButton.setPrefWidth(44);
		engineChooseFileButton.setPrefHeight(31);
		engineChooseFileButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				
				try {
					chooseAircraftEngineFile(tabPaneAircraftEngines.getTabs().size()-1);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		});
		contentPane.getChildren().add(engineChooseFileButton);
		
		Label engineXPositionLabel = new Label("X:");
		engineXPositionLabel.setFont(Font.font("System", 15));
		engineXPositionLabel.setLayoutX(6.0);
		engineXPositionLabel.setLayoutY(52.0);
		contentPane.getChildren().add(engineXPositionLabel);
		
		TextField engineXPositionTextField = new TextField();
		engineXPositionTextField.setLayoutX(6.0);
		engineXPositionTextField.setLayoutY(73);
		engineXPositionTextField.setPrefWidth(340);
		engineXPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(engineXPositionTextField);
		
		ChoiceBox<String> engineXChoiceBox = new ChoiceBox<String>();
		engineXChoiceBox.setLayoutX(348.0);
		engineXChoiceBox.setLayoutY(74);
		engineXChoiceBox.setPrefWidth(47);
		engineXChoiceBox.setPrefHeight(30);
		engineXChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(engineXChoiceBox);
		
		Label engineYPositionLabel = new Label("Y:");
		engineYPositionLabel.setFont(Font.font("System", 15));
		engineYPositionLabel.setLayoutX(6.0);
		engineYPositionLabel.setLayoutY(104.0);
		contentPane.getChildren().add(engineYPositionLabel);
		
		TextField engineYPositionTextField = new TextField();
		engineYPositionTextField.setLayoutX(6.0);
		engineYPositionTextField.setLayoutY(125);
		engineYPositionTextField.setPrefWidth(340);
		engineYPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(engineYPositionTextField);
		
		ChoiceBox<String> engineYChoiceBox = new ChoiceBox<String>();
		engineYChoiceBox.setLayoutX(348.0);
		engineYChoiceBox.setLayoutY(126);
		engineYChoiceBox.setPrefWidth(47);
		engineYChoiceBox.setPrefHeight(30);
		engineYChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(engineYChoiceBox);
		
		Label engineZPositionLabel = new Label("Z:");
		engineZPositionLabel.setFont(Font.font("System", 15));
		engineZPositionLabel.setLayoutX(7.0);
		engineZPositionLabel.setLayoutY(156.0);
		contentPane.getChildren().add(engineZPositionLabel);
		
		TextField engineZPositionTextField = new TextField();
		engineZPositionTextField.setLayoutX(7.0);
		engineZPositionTextField.setLayoutY(177);
		engineZPositionTextField.setPrefWidth(340);
		engineZPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(engineZPositionTextField);
		
		ChoiceBox<String> engineZChoiceBox = new ChoiceBox<String>();
		engineZChoiceBox.setLayoutX(348.0);
		engineZChoiceBox.setLayoutY(178);
		engineZChoiceBox.setPrefWidth(47);
		engineZChoiceBox.setPrefHeight(30);
		engineZChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(engineZChoiceBox);
		
		Label engineTiltAngleLabel = new Label("Tilting Angle:");
		engineTiltAngleLabel.setFont(Font.font("System", 15));
		engineTiltAngleLabel.setLayoutX(7.0);
		engineTiltAngleLabel.setLayoutY(208.0);
		contentPane.getChildren().add(engineTiltAngleLabel);
		
		TextField engineTiltAngleTextField = new TextField();
		engineTiltAngleTextField.setLayoutX(7.0);
		engineTiltAngleTextField.setLayoutY(229);
		engineTiltAngleTextField.setPrefWidth(340);
		engineTiltAngleTextField.setPrefHeight(31);
		contentPane.getChildren().add(engineTiltAngleTextField);
		
		ChoiceBox<String> engineTiltAngleChoiceBox = new ChoiceBox<String>();
		engineTiltAngleChoiceBox.setLayoutX(348.0);
		engineTiltAngleChoiceBox.setLayoutY(230);
		engineTiltAngleChoiceBox.setPrefWidth(47);
		engineTiltAngleChoiceBox.setPrefHeight(30);
		engineTiltAngleChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(engineTiltAngleChoiceBox);
		
		Label engineMountingPositionLabel = new Label("Position:");
		engineMountingPositionLabel.setFont(Font.font("System", 15));
		engineMountingPositionLabel.setLayoutX(7.0);
		engineMountingPositionLabel.setLayoutY(260.0);
		contentPane.getChildren().add(engineMountingPositionLabel);
		
		ChoiceBox<String> engineMountingPositionChoiceBox = new ChoiceBox<String>();
		engineMountingPositionChoiceBox.setLayoutX(6.0);
		engineMountingPositionChoiceBox.setLayoutY(282);
		engineMountingPositionChoiceBox.setPrefWidth(340);
		engineMountingPositionChoiceBox.setPrefHeight(31);
		engineMountingPositionChoiceBox.setItems(powerPlantMountingPositionTypeList);
		contentPane.getChildren().add(engineMountingPositionChoiceBox);
		
		textFieldsAircraftEngineFileList.add(engineFileTextField);
		textFieldAircraftEngineXList.add(engineXPositionTextField);
		textFieldAircraftEngineYList.add(engineYPositionTextField);
		textFieldAircraftEngineZList.add(engineZPositionTextField);
		textFieldAircraftEngineTiltList.add(engineTiltAngleTextField);
		choiceBoxesAircraftEnginePositonList.add(engineMountingPositionChoiceBox);
		
		choiceBoxAircraftEngineXUnitList.add(engineXChoiceBox);
		choiceBoxAircraftEngineYUnitList.add(engineYChoiceBox);
		choiceBoxAircraftEngineZUnitList.add(engineZChoiceBox);
		choiceBoxAircraftEngineTiltUnitList.add(engineTiltAngleChoiceBox);
		
		newEngineTab.setContent(contentPane);
		tabPaneAircraftEngines.getTabs().add(newEngineTab);
	
		System.gc();
		
	}
	
	@FXML
	private void addAircraftNacelle() {
		
		Tab newNacelleTab = new Tab("Nacelle " + (tabPaneAircraftNacelles.getTabs().size()+1));
		Pane contentPane = new Pane();
		
		Label nacelleFileLabel = new Label("File:");
		nacelleFileLabel.setFont(Font.font("System", 15));
		nacelleFileLabel.setLayoutX(6.0);
		nacelleFileLabel.setLayoutY(0.0);
		contentPane.getChildren().add(nacelleFileLabel);
		
		TextField nacelleFileTextField = new TextField();
		nacelleFileTextField.setLayoutX(6.0);
		nacelleFileTextField.setLayoutY(21);
		nacelleFileTextField.setPrefWidth(340);
		nacelleFileTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleFileTextField);
		
		Button nacelleChooseFileButton = new Button("...");
		nacelleChooseFileButton.setLayoutX(348);
		nacelleChooseFileButton.setLayoutY(21);
		nacelleChooseFileButton.setPrefWidth(44);
		nacelleChooseFileButton.setPrefHeight(31);
		nacelleChooseFileButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				
				try {
					chooseAircraftNacelleFile(tabPaneAircraftNacelles.getTabs().size()-1);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		});
		contentPane.getChildren().add(nacelleChooseFileButton);
		
		Label nacelleXPositionLabel = new Label("X:");
		nacelleXPositionLabel.setFont(Font.font("System", 15));
		nacelleXPositionLabel.setLayoutX(6.0);
		nacelleXPositionLabel.setLayoutY(52.0);
		contentPane.getChildren().add(nacelleXPositionLabel);
		
		TextField nacelleXPositionTextField = new TextField();
		nacelleXPositionTextField.setLayoutX(6.0);
		nacelleXPositionTextField.setLayoutY(73);
		nacelleXPositionTextField.setPrefWidth(340);
		nacelleXPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleXPositionTextField);
		
		ChoiceBox<String> nacelleXChoiceBox = new ChoiceBox<String>();
		nacelleXChoiceBox.setLayoutX(348.0);
		nacelleXChoiceBox.setLayoutY(74);
		nacelleXChoiceBox.setPrefWidth(47);
		nacelleXChoiceBox.setPrefHeight(30);
		nacelleXChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(nacelleXChoiceBox);
		
		Label nacelleYPositionLabel = new Label("Y:");
		nacelleYPositionLabel.setFont(Font.font("System", 15));
		nacelleYPositionLabel.setLayoutX(6.0);
		nacelleYPositionLabel.setLayoutY(104.0);
		contentPane.getChildren().add(nacelleYPositionLabel);
		
		TextField nacelleYPositionTextField = new TextField();
		nacelleYPositionTextField.setLayoutX(6.0);
		nacelleYPositionTextField.setLayoutY(125);
		nacelleYPositionTextField.setPrefWidth(340);
		nacelleYPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleYPositionTextField);
		
		ChoiceBox<String> nacelleYChoiceBox = new ChoiceBox<String>();
		nacelleYChoiceBox.setLayoutX(348.0);
		nacelleYChoiceBox.setLayoutY(126);
		nacelleYChoiceBox.setPrefWidth(47);
		nacelleYChoiceBox.setPrefHeight(30);
		nacelleYChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(nacelleYChoiceBox);
		
		Label nacelleZPositionLabel = new Label("Z:");
		nacelleZPositionLabel.setFont(Font.font("System", 15));
		nacelleZPositionLabel.setLayoutX(7.0);
		nacelleZPositionLabel.setLayoutY(156.0);
		contentPane.getChildren().add(nacelleZPositionLabel);
		
		TextField nacelleZPositionTextField = new TextField();
		nacelleZPositionTextField.setLayoutX(7.0);
		nacelleZPositionTextField.setLayoutY(177);
		nacelleZPositionTextField.setPrefWidth(340);
		nacelleZPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleZPositionTextField);
		
		ChoiceBox<String> nacelleZChoiceBox = new ChoiceBox<String>();
		nacelleZChoiceBox.setLayoutX(348.0);
		nacelleZChoiceBox.setLayoutY(178);
		nacelleZChoiceBox.setPrefWidth(47);
		nacelleZChoiceBox.setPrefHeight(30);
		nacelleZChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(nacelleZChoiceBox);
		
		Label nacelleMountingPositionLabel = new Label("Position:");
		nacelleMountingPositionLabel.setFont(Font.font("System", 15));
		nacelleMountingPositionLabel.setLayoutX(6.0);
		nacelleMountingPositionLabel.setLayoutY(208.0);
		contentPane.getChildren().add(nacelleMountingPositionLabel);
		
		ChoiceBox<String> nacelleMountingPositionChoiceBox = new ChoiceBox<String>();
		nacelleMountingPositionChoiceBox.setLayoutX(6.0);
		nacelleMountingPositionChoiceBox.setLayoutY(230);
		nacelleMountingPositionChoiceBox.setPrefWidth(340);
		nacelleMountingPositionChoiceBox.setPrefHeight(31);
		nacelleMountingPositionChoiceBox.setItems(nacelleMountingPositionTypeList);
		contentPane.getChildren().add(nacelleMountingPositionChoiceBox);
		
		textFieldsAircraftNacelleFileList.add(nacelleFileTextField);
		textFieldAircraftNacelleXList.add(nacelleXPositionTextField);
		textFieldAircraftNacelleYList.add(nacelleYPositionTextField);
		textFieldAircraftNacelleZList.add(nacelleZPositionTextField);
		choiceBoxesAircraftNacellePositonList.add(nacelleMountingPositionChoiceBox);
		
		choiceBoxAircraftNacelleXUnitList.add(nacelleXChoiceBox);
		choiceBoxAircraftNacelleYUnitList.add(nacelleYChoiceBox);
		choiceBoxAircraftNacelleZUnitList.add(nacelleZChoiceBox);
		
		newNacelleTab.setContent(contentPane);
		tabPaneAircraftNacelles.getTabs().add(newNacelleTab);
		
		System.gc();
		
	}
	
	@FXML
	private void addFuselageSpoiler() {
		
		Tab newSpoilerTab = new Tab("Spoiler " + (tabPaneFuselageSpoilers.getTabs().size()+1));
		Pane contentPane = new Pane();
		
		Label spoilerInnerSpanwisePositionLabel = new Label("Inner Y position (% semispan):");
		spoilerInnerSpanwisePositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		spoilerInnerSpanwisePositionLabel.setLayoutX(6.0);
		spoilerInnerSpanwisePositionLabel.setLayoutY(0.0);
		contentPane.getChildren().add(spoilerInnerSpanwisePositionLabel);
		
		TextField spoilerInnerSpanwisePositionTextField = new TextField();
		spoilerInnerSpanwisePositionTextField.setLayoutX(6.0);
		spoilerInnerSpanwisePositionTextField.setLayoutY(21);
		spoilerInnerSpanwisePositionTextField.setPrefWidth(340);
		spoilerInnerSpanwisePositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(spoilerInnerSpanwisePositionTextField);
		
		Label spoilerOuterSpanwisePositionLabel = new Label("Outer Y position (% semispan):");
		spoilerOuterSpanwisePositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		spoilerOuterSpanwisePositionLabel.setLayoutX(6.0);
		spoilerOuterSpanwisePositionLabel.setLayoutY(52.0);
		contentPane.getChildren().add(spoilerOuterSpanwisePositionLabel);
		
		TextField spoilerSpanwisePositionTextField = new TextField();
		spoilerSpanwisePositionTextField.setLayoutX(6.0);
		spoilerSpanwisePositionTextField.setLayoutY(73);
		spoilerSpanwisePositionTextField.setPrefWidth(340);
		spoilerSpanwisePositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(spoilerSpanwisePositionTextField);
		
		Label spoilerInnerChordwisePositionLabel = new Label("Inner X position (% local chord):");
		spoilerInnerChordwisePositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		spoilerInnerChordwisePositionLabel.setLayoutX(6.0);
		spoilerInnerChordwisePositionLabel.setLayoutY(104.0);
		contentPane.getChildren().add(spoilerInnerChordwisePositionLabel);
		
		TextField spoilerInnerChordisePositionTextField = new TextField();
		spoilerInnerChordisePositionTextField.setLayoutX(6.0);
		spoilerInnerChordisePositionTextField.setLayoutY(125);
		spoilerInnerChordisePositionTextField.setPrefWidth(340);
		spoilerInnerChordisePositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(spoilerInnerChordisePositionTextField);
		
		Label spoilerOuterChordwisePositionLabel = new Label("Outer X position (% local chord):");
		spoilerOuterChordwisePositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		spoilerOuterChordwisePositionLabel.setLayoutX(7.0);
		spoilerOuterChordwisePositionLabel.setLayoutY(156.0);
		contentPane.getChildren().add(spoilerOuterChordwisePositionLabel);
		
		TextField spoilerOuterChordRatioTextField = new TextField();
		spoilerOuterChordRatioTextField.setLayoutX(7.0);
		spoilerOuterChordRatioTextField.setLayoutY(177);
		spoilerOuterChordRatioTextField.setPrefWidth(340);
		spoilerOuterChordRatioTextField.setPrefHeight(31);
		contentPane.getChildren().add(spoilerOuterChordRatioTextField);
		
		Label spoilerMinimumDeflectionLabel = new Label("Minimum deflection angle:");
		spoilerMinimumDeflectionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		spoilerMinimumDeflectionLabel.setLayoutX(7.0);
		spoilerMinimumDeflectionLabel.setLayoutY(208.0);
		contentPane.getChildren().add(spoilerMinimumDeflectionLabel);
		
		TextField spoilerMinimumDeflectionTextField = new TextField();
		spoilerMinimumDeflectionTextField.setLayoutX(7.0);
		spoilerMinimumDeflectionTextField.setLayoutY(229);
		spoilerMinimumDeflectionTextField.setPrefWidth(340);
		spoilerMinimumDeflectionTextField.setPrefHeight(31);
		contentPane.getChildren().add(spoilerMinimumDeflectionTextField);
		
		ChoiceBox<String> spoilerMinimumDeflectionChoiceBox = new ChoiceBox<String>();
		spoilerMinimumDeflectionChoiceBox.setLayoutX(348.0);
		spoilerMinimumDeflectionChoiceBox.setLayoutY(230);
		spoilerMinimumDeflectionChoiceBox.setPrefWidth(47);
		spoilerMinimumDeflectionChoiceBox.setPrefHeight(30);
		spoilerMinimumDeflectionChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(spoilerMinimumDeflectionChoiceBox);
		
		Label spoilerMaximumDeflectionLabel = new Label("Maximum deflection angle:");
		spoilerMaximumDeflectionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		spoilerMaximumDeflectionLabel.setLayoutX(7.0);
		spoilerMaximumDeflectionLabel.setLayoutY(260.0);
		contentPane.getChildren().add(spoilerMaximumDeflectionLabel);
		
		TextField spoilerMaximumDeflectionTextField = new TextField();
		spoilerMaximumDeflectionTextField.setLayoutX(7.0);
		spoilerMaximumDeflectionTextField.setLayoutY(281);
		spoilerMaximumDeflectionTextField.setPrefWidth(340);
		spoilerMaximumDeflectionTextField.setPrefHeight(31);
		contentPane.getChildren().add(spoilerMaximumDeflectionTextField);
		
		ChoiceBox<String> spoilerMaximumDeflectionChoiceBox = new ChoiceBox<String>();
		spoilerMaximumDeflectionChoiceBox.setLayoutX(348.0);
		spoilerMaximumDeflectionChoiceBox.setLayoutY(282);
		spoilerMaximumDeflectionChoiceBox.setPrefWidth(47);
		spoilerMaximumDeflectionChoiceBox.setPrefHeight(30);
		spoilerMaximumDeflectionChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(spoilerMaximumDeflectionChoiceBox);
		
		textFieldFuselageInnerSpanwisePositionSpoilerList.add(spoilerInnerSpanwisePositionTextField);
		textFieldFuselageOuterSpanwisePositionSpoilerList.add(spoilerSpanwisePositionTextField);
		textFieldFuselageInnerChordwisePositionSpoilerList.add(spoilerInnerChordisePositionTextField);
		textFieldFuselageOuterChordwisePositionSpoilerList.add(spoilerOuterChordRatioTextField);
		textFieldFuselageMinimumDeflectionAngleSpoilerList.add(spoilerMinimumDeflectionTextField);
		textFieldFuselageMaximumDeflectionAngleSpoilerList.add(spoilerMaximumDeflectionTextField);
		choiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList.add(spoilerMinimumDeflectionChoiceBox);
		choiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList.add(spoilerMaximumDeflectionChoiceBox);
		
		newSpoilerTab.setContent(contentPane);
		tabPaneFuselageSpoilers.getTabs().add(newSpoilerTab);
		
	}
	
	@FXML
	private void addWingPanel() {
		
		Tab newPanelTab = new Tab("Panel " + (tabPaneWingPanels.getTabs().size()+1));
		Pane contentPane = new Pane();
		
		int additionalYSize = 0;
		
		if(tabPaneWingPanels.getTabs().size() > 0) {

			Label linkedToPreviousPanelLabel = new Label("Linked to previous panel:");
			linkedToPreviousPanelLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
			linkedToPreviousPanelLabel.setLayoutX(6.0);
			linkedToPreviousPanelLabel.setLayoutY(14.0);
			contentPane.getChildren().add(linkedToPreviousPanelLabel);

			CheckBox linkedToPreviousCheckBox = new CheckBox();
			linkedToPreviousCheckBox.setPrefWidth(21.0);
			linkedToPreviousCheckBox.setPrefHeight(17.0);
			linkedToPreviousCheckBox.setLayoutX(190.0);
			linkedToPreviousCheckBox.setLayoutY(16.0);
			contentPane.getChildren().add(linkedToPreviousCheckBox);
			checkBoxWingLinkedToPreviousPanelList.add(linkedToPreviousCheckBox);

			additionalYSize = 47;
			
		}

		Label panelSpanLabel = new Label("Span:");
		panelSpanLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelSpanLabel.setLayoutX(6.0);
		panelSpanLabel.setLayoutY(additionalYSize);
		contentPane.getChildren().add(panelSpanLabel);
		
		TextField panelSpanTextField = new TextField();
		panelSpanTextField.setLayoutX(6.0);
		panelSpanTextField.setLayoutY(21+additionalYSize);
		panelSpanTextField.setPrefWidth(340);
		panelSpanTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelSpanTextField);
		
		ChoiceBox<String> panelSpanChoiceBox = new ChoiceBox<String>();
		panelSpanChoiceBox.setLayoutX(347.0);
		panelSpanChoiceBox.setLayoutY(22+additionalYSize);
		panelSpanChoiceBox.setPrefWidth(47);
		panelSpanChoiceBox.setPrefHeight(30);
		panelSpanChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(panelSpanChoiceBox);
		
		Label panelSweepLELabel = new Label("Sweep L.E.:");
		panelSweepLELabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelSweepLELabel.setLayoutX(6.0);
		panelSweepLELabel.setLayoutY(52+additionalYSize);
		contentPane.getChildren().add(panelSweepLELabel);
		
		TextField panelSweepLETextField = new TextField();
		panelSweepLETextField.setLayoutX(6.0);
		panelSweepLETextField.setLayoutY(73+additionalYSize);
		panelSweepLETextField.setPrefWidth(340);
		panelSweepLETextField.setPrefHeight(31);
		contentPane.getChildren().add(panelSweepLETextField);
		
		ChoiceBox<String> panelSweepLEChoiceBox = new ChoiceBox<String>();
		panelSweepLEChoiceBox.setLayoutX(347.0);
		panelSweepLEChoiceBox.setLayoutY(74+additionalYSize);
		panelSweepLEChoiceBox.setPrefWidth(47);
		panelSweepLEChoiceBox.setPrefHeight(30);
		panelSweepLEChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelSweepLEChoiceBox);
		
		Label panelDihedralLabel = new Label("Dihedral angle:");
		panelDihedralLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelDihedralLabel.setLayoutX(6.0);
		panelDihedralLabel.setLayoutY(104+additionalYSize);
		contentPane.getChildren().add(panelDihedralLabel);
		
		TextField panelDihedralTextField = new TextField();
		panelDihedralTextField.setLayoutX(6.0);
		panelDihedralTextField.setLayoutY(125+additionalYSize);
		panelDihedralTextField.setPrefWidth(340);
		panelDihedralTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelDihedralTextField);
		
		ChoiceBox<String> panelDihedralChoiceBox = new ChoiceBox<String>();
		panelDihedralChoiceBox.setLayoutX(347.0);
		panelDihedralChoiceBox.setLayoutY(126+additionalYSize);
		panelDihedralChoiceBox.setPrefWidth(47);
		panelDihedralChoiceBox.setPrefHeight(30);
		panelDihedralChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelDihedralChoiceBox);
		
		Separator innerSectionUpperSeparator = new Separator();
		innerSectionUpperSeparator.setLayoutX(-2);
		innerSectionUpperSeparator.setLayoutY(164+additionalYSize);
		innerSectionUpperSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(innerSectionUpperSeparator);
		
		Label innerSectionLabel = new Label("Inner section");
		innerSectionLabel.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 15));
		innerSectionLabel.setTextFill(Paint.valueOf("#0400ff"));
		innerSectionLabel.setLayoutX(6.0);
		innerSectionLabel.setLayoutY(164+additionalYSize);
		contentPane.getChildren().add(innerSectionLabel);
		
		Separator innerSectionLowerSeparator = new Separator();
		innerSectionLowerSeparator.setLayoutX(-8);
		innerSectionLowerSeparator.setLayoutY(184+additionalYSize);
		innerSectionLowerSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(innerSectionLowerSeparator);
		
		Label panelInnerSectionChordLabel = new Label("Chord:");
		panelInnerSectionChordLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelInnerSectionChordLabel.setLayoutX(7.0);
		panelInnerSectionChordLabel.setLayoutY(184+additionalYSize);
		contentPane.getChildren().add(panelInnerSectionChordLabel);
		
		TextField panelInnerSectionChordTextField = new TextField();
		panelInnerSectionChordTextField.setLayoutX(7.0);
		panelInnerSectionChordTextField.setLayoutY(205+additionalYSize);
		panelInnerSectionChordTextField.setPrefWidth(340);
		panelInnerSectionChordTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelInnerSectionChordTextField);
		
		ChoiceBox<String> panelInnerSectionChordChoiceBox = new ChoiceBox<String>();
		panelInnerSectionChordChoiceBox.setLayoutX(348.0);
		panelInnerSectionChordChoiceBox.setLayoutY(206+additionalYSize);
		panelInnerSectionChordChoiceBox.setPrefWidth(47);
		panelInnerSectionChordChoiceBox.setPrefHeight(30);
		panelInnerSectionChordChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(panelInnerSectionChordChoiceBox);
		
		Label panelInnerSectionTwistLabel = new Label("Twist angle:");
		panelInnerSectionTwistLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelInnerSectionTwistLabel.setLayoutX(7.0);
		panelInnerSectionTwistLabel.setLayoutY(236+additionalYSize);
		contentPane.getChildren().add(panelInnerSectionTwistLabel);
		
		TextField panelInnerSectionTwistTextField = new TextField();
		panelInnerSectionTwistTextField.setLayoutX(7.0);
		panelInnerSectionTwistTextField.setLayoutY(257+additionalYSize);
		panelInnerSectionTwistTextField.setPrefWidth(340);
		panelInnerSectionTwistTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelInnerSectionTwistTextField);
		
		ChoiceBox<String> panelInnerSectionTwistChoiceBox = new ChoiceBox<String>();
		panelInnerSectionTwistChoiceBox.setLayoutX(348.0);
		panelInnerSectionTwistChoiceBox.setLayoutY(258+additionalYSize);
		panelInnerSectionTwistChoiceBox.setPrefWidth(47);
		panelInnerSectionTwistChoiceBox.setPrefHeight(30);
		panelInnerSectionTwistChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelInnerSectionTwistChoiceBox);
		
		Label panelInnerSectionAirfoilPathLabel = new Label("Airfoil:");
		panelInnerSectionAirfoilPathLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelInnerSectionAirfoilPathLabel.setLayoutX(7.0);
		panelInnerSectionAirfoilPathLabel.setLayoutY(288+additionalYSize);
		contentPane.getChildren().add(panelInnerSectionAirfoilPathLabel);
		
		TextField panelInnerSectionAirfoilPathTextField = new TextField();
		panelInnerSectionAirfoilPathTextField.setLayoutX(6.0);
		panelInnerSectionAirfoilPathTextField.setLayoutY(309+additionalYSize);
		panelInnerSectionAirfoilPathTextField.setPrefWidth(340);
		panelInnerSectionAirfoilPathTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelInnerSectionAirfoilPathTextField);
		
		Button panelInnerSectionChooseAirfoilPathButton = new Button("...");
		panelInnerSectionChooseAirfoilPathButton.setLayoutX(350.0);
		panelInnerSectionChooseAirfoilPathButton.setLayoutY(309+additionalYSize);
		panelInnerSectionChooseAirfoilPathButton.setPrefWidth(24);
		panelInnerSectionChooseAirfoilPathButton.setPrefHeight(31);
		panelInnerSectionChooseAirfoilPathButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chooseAirfoilFile(panelInnerSectionAirfoilPathTextField);
			}
		});
		contentPane.getChildren().add(panelInnerSectionChooseAirfoilPathButton);
		
		Button panelInnerSectionAirfoilDetailsButton = new Button("Details");
		panelInnerSectionAirfoilDetailsButton.setLayoutX(380);
		panelInnerSectionAirfoilDetailsButton.setLayoutY(309+additionalYSize);
		panelInnerSectionAirfoilDetailsButton.setPrefWidth(55);
		panelInnerSectionAirfoilDetailsButton.setPrefHeight(31);
		panelInnerSectionAirfoilDetailsButton.setFont(Font.font("System", FontWeight.BOLD, 12));
		panelInnerSectionAirfoilDetailsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				
				if(!wingAirfoilDetailsButtonAndTabsMap.containsKey(panelInnerSectionAirfoilDetailsButton)) {
					try {
						wingAirfoilDetailsButtonAndTabsMap.put(
								panelInnerSectionAirfoilDetailsButton, 
								tabPaneWingViewAndAirfoils.getTabs().size()
								);
						showAirfoilData(
								Paths.get(panelInnerSectionAirfoilPathTextField.getText()).getFileName().toString(),
								panelInnerSectionAirfoilDetailsButton,
								ComponentEnum.WING
								);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		final Tooltip warning = new Tooltip("WARNING : The airfoil details are already opened !!");
		panelInnerSectionAirfoilDetailsButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Point2D p = panelInnerSectionAirfoilDetailsButton
						.localToScreen(
								-2.5*panelInnerSectionAirfoilDetailsButton.getLayoutBounds().getMaxX(),
								1.2*panelInnerSectionAirfoilDetailsButton.getLayoutBounds().getMaxY()
								);
				if(wingAirfoilDetailsButtonAndTabsMap.containsKey(panelInnerSectionAirfoilDetailsButton)) {
					warning.show(panelInnerSectionAirfoilDetailsButton, p.getX(), p.getY());
				}
			}
		});
		panelInnerSectionAirfoilDetailsButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				warning.hide();
			}
		});
		contentPane.getChildren().add(panelInnerSectionAirfoilDetailsButton);
		panelInnerSectionAirfoilDetailsButton.disableProperty().bind(
				Bindings.isEmpty(panelInnerSectionAirfoilPathTextField.textProperty())
				);	
		
		Separator outerSectionUpperSeparator = new Separator();
		outerSectionUpperSeparator.setLayoutX(-2);
		outerSectionUpperSeparator.setLayoutY(360+additionalYSize);
		outerSectionUpperSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(outerSectionUpperSeparator);
		
		Label outerSectionLabel = new Label("Outer section");
		outerSectionLabel.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 15));
		outerSectionLabel.setTextFill(Paint.valueOf("#0400ff"));
		outerSectionLabel.setLayoutX(6.0);
		outerSectionLabel.setLayoutY(360+additionalYSize);
		contentPane.getChildren().add(outerSectionLabel);
		
		Separator outerSectionLowerSeparator = new Separator();
		outerSectionLowerSeparator.setLayoutX(-8);
		outerSectionLowerSeparator.setLayoutY(380+additionalYSize);
		outerSectionLowerSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(outerSectionLowerSeparator);
		
		Label panelOuterSectionChordLabel = new Label("Chord:");
		panelOuterSectionChordLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelOuterSectionChordLabel.setLayoutX(6.0);
		panelOuterSectionChordLabel.setLayoutY(383+additionalYSize);
		contentPane.getChildren().add(panelOuterSectionChordLabel);
		
		TextField panelOuterSectionChordTextField = new TextField();
		panelOuterSectionChordTextField.setLayoutX(6.0);
		panelOuterSectionChordTextField.setLayoutY(404+additionalYSize);
		panelOuterSectionChordTextField.setPrefWidth(340);
		panelOuterSectionChordTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelOuterSectionChordTextField);
		
		ChoiceBox<String> panelOuterSectionChordChoiceBox = new ChoiceBox<String>();
		panelOuterSectionChordChoiceBox.setLayoutX(348.0);
		panelOuterSectionChordChoiceBox.setLayoutY(405+additionalYSize);
		panelOuterSectionChordChoiceBox.setPrefWidth(47);
		panelOuterSectionChordChoiceBox.setPrefHeight(30);
		panelOuterSectionChordChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(panelOuterSectionChordChoiceBox);
		
		Label panelOuterSectionTwistLabel = new Label("Twist angle:");
		panelOuterSectionTwistLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelOuterSectionTwistLabel.setLayoutX(6.0);
		panelOuterSectionTwistLabel.setLayoutY(435+additionalYSize);
		contentPane.getChildren().add(panelOuterSectionTwistLabel);
		
		TextField panelOuterSectionTwistTextField = new TextField();
		panelOuterSectionTwistTextField.setLayoutX(6.0);
		panelOuterSectionTwistTextField.setLayoutY(456+additionalYSize);
		panelOuterSectionTwistTextField.setPrefWidth(340);
		panelOuterSectionTwistTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelOuterSectionTwistTextField);
		
		ChoiceBox<String> panelOuterSectionTwistChoiceBox = new ChoiceBox<String>();
		panelOuterSectionTwistChoiceBox.setLayoutX(348.0);
		panelOuterSectionTwistChoiceBox.setLayoutY(457+additionalYSize);
		panelOuterSectionTwistChoiceBox.setPrefWidth(47);
		panelOuterSectionTwistChoiceBox.setPrefHeight(30);
		panelOuterSectionTwistChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelOuterSectionTwistChoiceBox);
		
		Label panelOuterSectionAirfoilPathLabel = new Label("Airfoil:");
		panelOuterSectionAirfoilPathLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelOuterSectionAirfoilPathLabel.setLayoutX(6.0);
		panelOuterSectionAirfoilPathLabel.setLayoutY(487+additionalYSize);
		contentPane.getChildren().add(panelOuterSectionAirfoilPathLabel);
		
		TextField panelOuterSectionAirfoilPathTextField = new TextField();
		panelOuterSectionAirfoilPathTextField.setLayoutX(6.0);
		panelOuterSectionAirfoilPathTextField.setLayoutY(508+additionalYSize);
		panelOuterSectionAirfoilPathTextField.setPrefWidth(340);
		panelOuterSectionAirfoilPathTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelOuterSectionAirfoilPathTextField);
		
		Button panelOuterSectionChooseAirfoilPathButton = new Button("...");
		panelOuterSectionChooseAirfoilPathButton.setLayoutX(350.0);
		panelOuterSectionChooseAirfoilPathButton.setLayoutY(508+additionalYSize);
		panelOuterSectionChooseAirfoilPathButton.setPrefWidth(24);
		panelOuterSectionChooseAirfoilPathButton.setPrefHeight(31);
		panelOuterSectionChooseAirfoilPathButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chooseAirfoilFile(panelOuterSectionAirfoilPathTextField);
			}
		});
		contentPane.getChildren().add(panelOuterSectionChooseAirfoilPathButton);
		
		Button panelOuterSectionAirfoilDetailsButton = new Button("Details");
		panelOuterSectionAirfoilDetailsButton.setLayoutX(380);
		panelOuterSectionAirfoilDetailsButton.setLayoutY(508+additionalYSize);
		panelOuterSectionAirfoilDetailsButton.setPrefWidth(55);
		panelOuterSectionAirfoilDetailsButton.setPrefHeight(31);
		panelOuterSectionAirfoilDetailsButton.setFont(Font.font("System", FontWeight.BOLD, 12));
		panelOuterSectionAirfoilDetailsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				if(!wingAirfoilDetailsButtonAndTabsMap.containsKey(panelOuterSectionAirfoilDetailsButton)) {
					try {
						wingAirfoilDetailsButtonAndTabsMap.put(
								panelOuterSectionAirfoilDetailsButton, 
								tabPaneWingViewAndAirfoils.getTabs().size()
								);
						showAirfoilData(
								Paths.get(panelOuterSectionAirfoilPathTextField.getText()).getFileName().toString(),
								panelOuterSectionAirfoilDetailsButton,
								ComponentEnum.WING
								);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		});
		panelOuterSectionAirfoilDetailsButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Point2D p = panelOuterSectionAirfoilDetailsButton
						.localToScreen(
								-2.5*panelOuterSectionAirfoilDetailsButton.getLayoutBounds().getMaxX(),
								1.2*panelOuterSectionAirfoilDetailsButton.getLayoutBounds().getMaxY()
								);
				if(wingAirfoilDetailsButtonAndTabsMap.containsKey(panelOuterSectionAirfoilDetailsButton)) {
					warning.show(panelOuterSectionAirfoilDetailsButton, p.getX(), p.getY());
				}
			}
		});
		panelOuterSectionAirfoilDetailsButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				warning.hide();
			}
		});
		contentPane.getChildren().add(panelOuterSectionAirfoilDetailsButton);
		panelOuterSectionAirfoilDetailsButton.disableProperty().bind(
				Bindings.isEmpty(panelOuterSectionAirfoilPathTextField.textProperty())
				);		
		
		textFieldWingSpanPanelList.add(panelSpanTextField);
		textFieldWingSweepLEPanelList.add(panelSweepLETextField);
		textFieldWingDihedralPanelList.add(panelDihedralTextField);
		textFieldWingInnerChordPanelList.add(panelInnerSectionChordTextField);
		textFieldWingInnerTwistPanelList.add(panelInnerSectionTwistTextField);
		textFieldWingInnerAirfoilPanelList.add(panelInnerSectionAirfoilPathTextField);
		textFieldWingOuterChordPanelList.add(panelOuterSectionChordTextField);
		textFieldWingOuterTwistPanelList.add(panelOuterSectionTwistTextField);
		textFieldWingOuterAirfoilPanelList.add(panelOuterSectionAirfoilPathTextField);
		choiceBoxWingSpanPanelUnitList.add(panelSpanChoiceBox);
		choiceBoxWingSweepLEPanelUnitList.add(panelSweepLEChoiceBox);
		choiceBoxWingDihedralPanelUnitList.add(panelDihedralChoiceBox);
		choiceBoxWingInnerChordPanelUnitList.add(panelInnerSectionChordChoiceBox);
		choiceBoxWingInnerTwistPanelUnitList.add(panelInnerSectionTwistChoiceBox);
		choiceBoxWingOuterChordPanelUnitList.add(panelOuterSectionChordChoiceBox);
		choiceBoxWingOuterTwistPanelUnitList.add(panelOuterSectionTwistChoiceBox);
		detailButtonWingInnerAirfoilList.add(panelInnerSectionAirfoilDetailsButton);
		detailButtonWingOuterAirfoilList.add(panelOuterSectionAirfoilDetailsButton);
		chooseInnerWingAirfoilFileButtonList.add(panelInnerSectionChooseAirfoilPathButton);
		chooseOuterWingAirfoilFileButtonList.add(panelOuterSectionChooseAirfoilPathButton);
		
		newPanelTab.setContent(contentPane);
		tabPaneWingPanels.getTabs().add(newPanelTab);
		
		System.gc();
		
	}
	
	@FXML
	private void addFlap() {
		
		Tab newFlapTab = new Tab("Flap " + (tabPaneWingFlaps.getTabs().size()+1));
		Pane contentPane = new Pane();
		
		Label flapTypeLabel = new Label("Type:");
		flapTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		flapTypeLabel.setLayoutX(6.0);
		flapTypeLabel.setLayoutY(0.0);
		contentPane.getChildren().add(flapTypeLabel);
		
		ChoiceBox<String> flapTypeChoiceBox = new ChoiceBox<String>();
		flapTypeChoiceBox.setLayoutX(6.0);
		flapTypeChoiceBox.setLayoutY(21);
		flapTypeChoiceBox.setPrefWidth(340);
		flapTypeChoiceBox.setPrefHeight(31);
		flapTypeChoiceBox.setItems(flapTypeList);
		contentPane.getChildren().add(flapTypeChoiceBox);
		
		Label flapInnerPositionLabel = new Label("Inner position (% semispan):");
		flapInnerPositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		flapInnerPositionLabel.setLayoutX(6.0);
		flapInnerPositionLabel.setLayoutY(50.0);
		contentPane.getChildren().add(flapInnerPositionLabel);
		
		TextField flapInnerPositionTextField = new TextField();
		flapInnerPositionTextField.setLayoutX(6.0);
		flapInnerPositionTextField.setLayoutY(71);
		flapInnerPositionTextField.setPrefWidth(340);
		flapInnerPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(flapInnerPositionTextField);
		
		Label flapOuterPositionLabel = new Label("Outer position (% semispan):");
		flapOuterPositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		flapOuterPositionLabel.setLayoutX(6.0);
		flapOuterPositionLabel.setLayoutY(102.0);
		contentPane.getChildren().add(flapOuterPositionLabel);
		
		TextField flapOuterPositionTextField = new TextField();
		flapOuterPositionTextField.setLayoutX(6.0);
		flapOuterPositionTextField.setLayoutY(123);
		flapOuterPositionTextField.setPrefWidth(340);
		flapOuterPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(flapOuterPositionTextField);
		
		Label flapInnerChordRatioLabel = new Label("Inner chord ratio:");
		flapInnerChordRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		flapInnerChordRatioLabel.setLayoutX(6.0);
		flapInnerChordRatioLabel.setLayoutY(154.0);
		contentPane.getChildren().add(flapInnerChordRatioLabel);
		
		TextField flapInnerChordRatioTextField = new TextField();
		flapInnerChordRatioTextField.setLayoutX(6.0);
		flapInnerChordRatioTextField.setLayoutY(175);
		flapInnerChordRatioTextField.setPrefWidth(340);
		flapInnerChordRatioTextField.setPrefHeight(31);
		contentPane.getChildren().add(flapInnerChordRatioTextField);
		
		Label flapOuterChordRatioLabel = new Label("Outer chord ratio:");
		flapOuterChordRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		flapOuterChordRatioLabel.setLayoutX(6.0);
		flapOuterChordRatioLabel.setLayoutY(206.0);
		contentPane.getChildren().add(flapOuterChordRatioLabel);
		
		TextField flapOuterChordRatioTextField = new TextField();
		flapOuterChordRatioTextField.setLayoutX(6.0);
		flapOuterChordRatioTextField.setLayoutY(227);
		flapOuterChordRatioTextField.setPrefWidth(340);
		flapOuterChordRatioTextField.setPrefHeight(31);
		contentPane.getChildren().add(flapOuterChordRatioTextField);
		
		Label flapMinimumDeflectionLabel = new Label("Minimum deflection angle:");
		flapMinimumDeflectionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		flapMinimumDeflectionLabel.setLayoutX(6.0);
		flapMinimumDeflectionLabel.setLayoutY(258.0);
		contentPane.getChildren().add(flapMinimumDeflectionLabel);
		
		TextField flapMinimumDeflectionTextField = new TextField();
		flapMinimumDeflectionTextField.setLayoutX(6.0);
		flapMinimumDeflectionTextField.setLayoutY(279);
		flapMinimumDeflectionTextField.setPrefWidth(340);
		flapMinimumDeflectionTextField.setPrefHeight(31);
		contentPane.getChildren().add(flapMinimumDeflectionTextField);
		
		ChoiceBox<String> flapMinimumDeflectionChoiceBox = new ChoiceBox<String>();
		flapMinimumDeflectionChoiceBox.setLayoutX(348.0);
		flapMinimumDeflectionChoiceBox.setLayoutY(279);
		flapMinimumDeflectionChoiceBox.setPrefWidth(47);
		flapMinimumDeflectionChoiceBox.setPrefHeight(30);
		flapMinimumDeflectionChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(flapMinimumDeflectionChoiceBox);
		
		Label flapMaximumDeflectionLabel = new Label("Maximum deflection angle:");
		flapMaximumDeflectionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		flapMaximumDeflectionLabel.setLayoutX(6.0);
		flapMaximumDeflectionLabel.setLayoutY(310.0);
		contentPane.getChildren().add(flapMaximumDeflectionLabel);
		
		TextField flapMaximumDeflectionTextField = new TextField();
		flapMaximumDeflectionTextField.setLayoutX(6.0);
		flapMaximumDeflectionTextField.setLayoutY(281);
		flapMaximumDeflectionTextField.setPrefWidth(331);
		flapMaximumDeflectionTextField.setPrefHeight(31);
		contentPane.getChildren().add(flapMaximumDeflectionTextField);
		
		ChoiceBox<String> flapMaximumDeflectionChoiceBox = new ChoiceBox<String>();
		flapMaximumDeflectionChoiceBox.setLayoutX(348.0);
		flapMaximumDeflectionChoiceBox.setLayoutY(331);
		flapMaximumDeflectionChoiceBox.setPrefWidth(47);
		flapMaximumDeflectionChoiceBox.setPrefHeight(30);
		flapMaximumDeflectionChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(flapMaximumDeflectionChoiceBox);
		
		choiceBoxWingFlapTypeList.add(flapTypeChoiceBox);
		textFieldWingInnerPositionFlapList.add(flapInnerPositionTextField);
		textFieldWingOuterPositionFlapList.add(flapOuterPositionTextField);
		textFieldWingInnerChordRatioFlapList.add(flapInnerChordRatioTextField);
		textFieldWingOuterChordRatioFlapList.add(flapOuterChordRatioTextField);
		textFieldWingMinimumDeflectionAngleFlapList.add(flapMinimumDeflectionTextField);
		textFieldWingMaximumDeflectionAngleFlapList.add(flapMaximumDeflectionTextField);
		choiceBoxWingMinimumDeflectionAngleFlapUnitList.add(flapMinimumDeflectionChoiceBox);
		choiceBoxWingMaximumDeflectionAngleFlapUnitList.add(flapMaximumDeflectionChoiceBox);
		
		newFlapTab.setContent(contentPane);
		tabPaneWingFlaps.getTabs().add(newFlapTab);
		
		System.gc();
		
	}
	
	@FXML
	private void addSlat() {
		
		Tab newSlatTab = new Tab("Slat " + (tabPaneWingSlats.getTabs().size()+1));
		Pane contentPane = new Pane();
		
		Label slatInnerPositionLabel = new Label("Inner position (% semispan):");
		slatInnerPositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		slatInnerPositionLabel.setLayoutX(6.0);
		slatInnerPositionLabel.setLayoutY(0.0);
		contentPane.getChildren().add(slatInnerPositionLabel);
		
		TextField slatInnerPositionTextField = new TextField();
		slatInnerPositionTextField.setLayoutX(6.0);
		slatInnerPositionTextField.setLayoutY(21);
		slatInnerPositionTextField.setPrefWidth(340);
		slatInnerPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(slatInnerPositionTextField);
		
		Label slatOuterPositionLabel = new Label("Outer position (% semispan):");
		slatOuterPositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		slatOuterPositionLabel.setLayoutX(6.0);
		slatOuterPositionLabel.setLayoutY(52.0);
		contentPane.getChildren().add(slatOuterPositionLabel);
		
		TextField slatOuterPositionTextField = new TextField();
		slatOuterPositionTextField.setLayoutX(6.0);
		slatOuterPositionTextField.setLayoutY(73);
		slatOuterPositionTextField.setPrefWidth(340);
		slatOuterPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(slatOuterPositionTextField);
		
		Label slatInnerChordRatioLabel = new Label("Inner chord ratio:");
		slatInnerChordRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		slatInnerChordRatioLabel.setLayoutX(6.0);
		slatInnerChordRatioLabel.setLayoutY(104.0);
		contentPane.getChildren().add(slatInnerChordRatioLabel);
		
		TextField slatInnerChordRatioTextField = new TextField();
		slatInnerChordRatioTextField.setLayoutX(6.0);
		slatInnerChordRatioTextField.setLayoutY(125);
		slatInnerChordRatioTextField.setPrefWidth(340);
		slatInnerChordRatioTextField.setPrefHeight(31);
		contentPane.getChildren().add(slatInnerChordRatioTextField);
		
		Label slatOuterChordRatioLabel = new Label("Outer chord ratio:");
		slatOuterChordRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		slatOuterChordRatioLabel.setLayoutX(7.0);
		slatOuterChordRatioLabel.setLayoutY(156.0);
		contentPane.getChildren().add(slatOuterChordRatioLabel);
		
		TextField slatOuterChordRatioTextField = new TextField();
		slatOuterChordRatioTextField.setLayoutX(7.0);
		slatOuterChordRatioTextField.setLayoutY(177);
		slatOuterChordRatioTextField.setPrefWidth(340);
		slatOuterChordRatioTextField.setPrefHeight(31);
		contentPane.getChildren().add(slatOuterChordRatioTextField);
		
		Label slatExtensionRatioLabel = new Label("Extension ratio:");
		slatExtensionRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		slatExtensionRatioLabel.setLayoutX(6.0);
		slatExtensionRatioLabel.setLayoutY(208.0);
		contentPane.getChildren().add(slatExtensionRatioLabel);
		
		TextField slatExtensionRatioTextField = new TextField();
		slatExtensionRatioTextField.setLayoutX(6.0);
		slatExtensionRatioTextField.setLayoutY(229);
		slatExtensionRatioTextField.setPrefWidth(340);
		slatExtensionRatioTextField.setPrefHeight(31);
		contentPane.getChildren().add(slatExtensionRatioTextField);
		
		Label slatMinimumDeflectionLabel = new Label("Minimum deflection angle:");
		slatMinimumDeflectionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		slatMinimumDeflectionLabel.setLayoutX(6.0);
		slatMinimumDeflectionLabel.setLayoutY(262.0);
		contentPane.getChildren().add(slatMinimumDeflectionLabel);
		
		TextField slatMinimumDeflectionTextField = new TextField();
		slatMinimumDeflectionTextField.setLayoutX(6.0);
		slatMinimumDeflectionTextField.setLayoutY(285);
		slatMinimumDeflectionTextField.setPrefWidth(340);
		slatMinimumDeflectionTextField.setPrefHeight(31);
		contentPane.getChildren().add(slatMinimumDeflectionTextField);
		
		ChoiceBox<String> slatMinimumDeflectionChoiceBox = new ChoiceBox<String>();
		slatMinimumDeflectionChoiceBox.setLayoutX(347.0);
		slatMinimumDeflectionChoiceBox.setLayoutY(285);
		slatMinimumDeflectionChoiceBox.setPrefWidth(47);
		slatMinimumDeflectionChoiceBox.setPrefHeight(30);
		slatMinimumDeflectionChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(slatMinimumDeflectionChoiceBox);
		
		Label slatMaximumDeflectionLabel = new Label("Maximum deflection angle:");
		slatMaximumDeflectionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		slatMaximumDeflectionLabel.setLayoutX(6.0);
		slatMaximumDeflectionLabel.setLayoutY(320.0);
		contentPane.getChildren().add(slatMaximumDeflectionLabel);
		
		TextField slatMaximumDeflectionTextField = new TextField();
		slatMaximumDeflectionTextField.setLayoutX(6.0);
		slatMaximumDeflectionTextField.setLayoutY(340);
		slatMaximumDeflectionTextField.setPrefWidth(340);
		slatMaximumDeflectionTextField.setPrefHeight(31);
		contentPane.getChildren().add(slatMaximumDeflectionTextField);
		
		ChoiceBox<String> slatMaximumDeflectionChoiceBox = new ChoiceBox<String>();
		slatMaximumDeflectionChoiceBox.setLayoutX(347.0);
		slatMaximumDeflectionChoiceBox.setLayoutY(340);
		slatMaximumDeflectionChoiceBox.setPrefWidth(47);
		slatMaximumDeflectionChoiceBox.setPrefHeight(30);
		slatMaximumDeflectionChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(slatMaximumDeflectionChoiceBox);
		
		textFieldWingInnerPositionSlatList.add(slatInnerPositionTextField);
		textFieldWingOuterPositionSlatList.add(slatOuterPositionTextField);
		textFieldWingInnerChordRatioSlatList.add(slatInnerChordRatioTextField);
		textFieldWingOuterChordRatioSlatList.add(slatExtensionRatioTextField);
		textFieldWingExtensionRatioSlatList.add(slatExtensionRatioTextField);
		textFieldWingMinimumDeflectionAngleSlatList.add(slatMinimumDeflectionTextField);
		textFieldWingMaximumDeflectionAngleSlatList.add(slatMaximumDeflectionTextField);
		choiceBoxWingMinimumDeflectionAngleSlatUnitList.add(slatMinimumDeflectionChoiceBox);
		choiceBoxWingMaximumDeflectionAngleSlatUnitList.add(slatMaximumDeflectionChoiceBox);
		
		newSlatTab.setContent(contentPane);
		tabPaneWingSlats.getTabs().add(newSlatTab);
		
		System.gc();
		
	}
	
	@FXML
	private void addSpoiler() {
		
		Tab newSpoilerTab = new Tab("Spoiler " + (tabPaneWingSpoilers.getTabs().size()+1));
		Pane contentPane = new Pane();
		
		Label spoilerInnerSpanwisePositionLabel = new Label("Inner Y position (% semispan):");
		spoilerInnerSpanwisePositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		spoilerInnerSpanwisePositionLabel.setLayoutX(6.0);
		spoilerInnerSpanwisePositionLabel.setLayoutY(0.0);
		contentPane.getChildren().add(spoilerInnerSpanwisePositionLabel);
		
		TextField spoilerInnerSpanwisePositionTextField = new TextField();
		spoilerInnerSpanwisePositionTextField.setLayoutX(6.0);
		spoilerInnerSpanwisePositionTextField.setLayoutY(21);
		spoilerInnerSpanwisePositionTextField.setPrefWidth(340);
		spoilerInnerSpanwisePositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(spoilerInnerSpanwisePositionTextField);
		
		Label spoilerOuterSpanwisePositionLabel = new Label("Outer Y position (% semispan):");
		spoilerOuterSpanwisePositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		spoilerOuterSpanwisePositionLabel.setLayoutX(6.0);
		spoilerOuterSpanwisePositionLabel.setLayoutY(52.0);
		contentPane.getChildren().add(spoilerOuterSpanwisePositionLabel);
		
		TextField spoilerSpanwisePositionTextField = new TextField();
		spoilerSpanwisePositionTextField.setLayoutX(6.0);
		spoilerSpanwisePositionTextField.setLayoutY(73);
		spoilerSpanwisePositionTextField.setPrefWidth(340);
		spoilerSpanwisePositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(spoilerSpanwisePositionTextField);
		
		Label spoilerInnerChordwisePositionLabel = new Label("Inner X position (% local chord):");
		spoilerInnerChordwisePositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		spoilerInnerChordwisePositionLabel.setLayoutX(6.0);
		spoilerInnerChordwisePositionLabel.setLayoutY(104.0);
		contentPane.getChildren().add(spoilerInnerChordwisePositionLabel);
		
		TextField spoilerInnerChordisePositionTextField = new TextField();
		spoilerInnerChordisePositionTextField.setLayoutX(6.0);
		spoilerInnerChordisePositionTextField.setLayoutY(125);
		spoilerInnerChordisePositionTextField.setPrefWidth(340);
		spoilerInnerChordisePositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(spoilerInnerChordisePositionTextField);
		
		Label spoilerOuterChordwisePositionLabel = new Label("Outer X position (% local chord):");
		spoilerOuterChordwisePositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		spoilerOuterChordwisePositionLabel.setLayoutX(7.0);
		spoilerOuterChordwisePositionLabel.setLayoutY(156.0);
		contentPane.getChildren().add(spoilerOuterChordwisePositionLabel);
		
		TextField spoilerOuterChordRatioTextField = new TextField();
		spoilerOuterChordRatioTextField.setLayoutX(7.0);
		spoilerOuterChordRatioTextField.setLayoutY(177);
		spoilerOuterChordRatioTextField.setPrefWidth(340);
		spoilerOuterChordRatioTextField.setPrefHeight(31);
		contentPane.getChildren().add(spoilerOuterChordRatioTextField);
		
		Label spoilerMinimumDeflectionLabel = new Label("Minimum deflection angle:");
		spoilerMinimumDeflectionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		spoilerMinimumDeflectionLabel.setLayoutX(7.0);
		spoilerMinimumDeflectionLabel.setLayoutY(208.0);
		contentPane.getChildren().add(spoilerMinimumDeflectionLabel);
		
		TextField spoilerMinimumDeflectionTextField = new TextField();
		spoilerMinimumDeflectionTextField.setLayoutX(7.0);
		spoilerMinimumDeflectionTextField.setLayoutY(229);
		spoilerMinimumDeflectionTextField.setPrefWidth(340);
		spoilerMinimumDeflectionTextField.setPrefHeight(31);
		contentPane.getChildren().add(spoilerMinimumDeflectionTextField);
		
		ChoiceBox<String> spoilerMinimumDeflectionChoiceBox = new ChoiceBox<String>();
		spoilerMinimumDeflectionChoiceBox.setLayoutX(348.0);
		spoilerMinimumDeflectionChoiceBox.setLayoutY(230);
		spoilerMinimumDeflectionChoiceBox.setPrefWidth(47);
		spoilerMinimumDeflectionChoiceBox.setPrefHeight(30);
		spoilerMinimumDeflectionChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(spoilerMinimumDeflectionChoiceBox);
		
		Label spoilerMaximumDeflectionLabel = new Label("Maximum deflection angle:");
		spoilerMaximumDeflectionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		spoilerMaximumDeflectionLabel.setLayoutX(7.0);
		spoilerMaximumDeflectionLabel.setLayoutY(260.0);
		contentPane.getChildren().add(spoilerMaximumDeflectionLabel);
		
		TextField spoilerMaximumDeflectionTextField = new TextField();
		spoilerMaximumDeflectionTextField.setLayoutX(7.0);
		spoilerMaximumDeflectionTextField.setLayoutY(281);
		spoilerMaximumDeflectionTextField.setPrefWidth(340);
		spoilerMaximumDeflectionTextField.setPrefHeight(31);
		contentPane.getChildren().add(spoilerMaximumDeflectionTextField);
		
		ChoiceBox<String> spoilerMaximumDeflectionChoiceBox = new ChoiceBox<String>();
		spoilerMaximumDeflectionChoiceBox.setLayoutX(348.0);
		spoilerMaximumDeflectionChoiceBox.setLayoutY(282);
		spoilerMaximumDeflectionChoiceBox.setPrefWidth(47);
		spoilerMaximumDeflectionChoiceBox.setPrefHeight(30);
		spoilerMaximumDeflectionChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(spoilerMaximumDeflectionChoiceBox);
		
		textFieldWingInnerSpanwisePositionSpoilerList.add(spoilerInnerSpanwisePositionTextField);
		textFieldWingOuterSpanwisePositionSpoilerList.add(spoilerSpanwisePositionTextField);
		textFieldWingInnerChordwisePositionSpoilerList.add(spoilerInnerChordisePositionTextField);
		textFieldWingOuterChordwisePositionSpoilerList.add(spoilerOuterChordRatioTextField);
		textFieldWingMinimumDeflectionAngleSpoilerList.add(spoilerMinimumDeflectionTextField);
		textFieldWingMaximumDeflectionAngleSpoilerList.add(spoilerMaximumDeflectionTextField);
		choiceBoxWingMinimumDeflectionAngleSpoilerUnitList.add(spoilerMinimumDeflectionChoiceBox);
		choiceBoxWingMaximumDeflectionAngleSpoilerUnitList.add(spoilerMaximumDeflectionChoiceBox);
		
		newSpoilerTab.setContent(contentPane);
		tabPaneWingSpoilers.getTabs().add(newSpoilerTab);
		
		System.gc();
		
	}
	
	@FXML
	private void addHTailPanel() {
		
		Tab newPanelTab = new Tab("Panel " + (tabPaneHTailPanels.getTabs().size()+1));
		Pane contentPane = new Pane();
		
		int additionalYSize = 0;
		
		if(tabPaneHTailPanels.getTabs().size() > 0) {

			Label linkedToPreviousPanelLabel = new Label("Linked to previous panel:");
			linkedToPreviousPanelLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
			linkedToPreviousPanelLabel.setLayoutX(6.0);
			linkedToPreviousPanelLabel.setLayoutY(14.0);
			contentPane.getChildren().add(linkedToPreviousPanelLabel);

			CheckBox linkedToPreviousCheckBox = new CheckBox();
			linkedToPreviousCheckBox.setPrefWidth(21.0);
			linkedToPreviousCheckBox.setPrefHeight(17.0);
			linkedToPreviousCheckBox.setLayoutX(190.0);
			linkedToPreviousCheckBox.setLayoutY(16.0);
			contentPane.getChildren().add(linkedToPreviousCheckBox);
			checkBoxHTailLinkedToPreviousPanelList.add(linkedToPreviousCheckBox);

			additionalYSize = 47;
			
		}

		Label panelSpanLabel = new Label("Span:");
		panelSpanLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelSpanLabel.setLayoutX(6.0);
		panelSpanLabel.setLayoutY(additionalYSize);
		contentPane.getChildren().add(panelSpanLabel);
		
		TextField panelSpanTextField = new TextField();
		panelSpanTextField.setLayoutX(6.0);
		panelSpanTextField.setLayoutY(21+additionalYSize);
		panelSpanTextField.setPrefWidth(340);
		panelSpanTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelSpanTextField);
		
		ChoiceBox<String> panelSpanChoiceBox = new ChoiceBox<String>();
		panelSpanChoiceBox.setLayoutX(347.0);
		panelSpanChoiceBox.setLayoutY(22+additionalYSize);
		panelSpanChoiceBox.setPrefWidth(47);
		panelSpanChoiceBox.setPrefHeight(30);
		panelSpanChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(panelSpanChoiceBox);
		
		Label panelSweepLELabel = new Label("Sweep L.E.:");
		panelSweepLELabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelSweepLELabel.setLayoutX(6.0);
		panelSweepLELabel.setLayoutY(52+additionalYSize);
		contentPane.getChildren().add(panelSweepLELabel);
		
		TextField panelSweepLETextField = new TextField();
		panelSweepLETextField.setLayoutX(6.0);
		panelSweepLETextField.setLayoutY(73+additionalYSize);
		panelSweepLETextField.setPrefWidth(340);
		panelSweepLETextField.setPrefHeight(31);
		contentPane.getChildren().add(panelSweepLETextField);
		
		ChoiceBox<String> panelSweepLEChoiceBox = new ChoiceBox<String>();
		panelSweepLEChoiceBox.setLayoutX(347.0);
		panelSweepLEChoiceBox.setLayoutY(74+additionalYSize);
		panelSweepLEChoiceBox.setPrefWidth(47);
		panelSweepLEChoiceBox.setPrefHeight(30);
		panelSweepLEChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelSweepLEChoiceBox);
		
		Label panelDihedralLabel = new Label("Dihedral angle:");
		panelDihedralLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelDihedralLabel.setLayoutX(6.0);
		panelDihedralLabel.setLayoutY(104+additionalYSize);
		contentPane.getChildren().add(panelDihedralLabel);
		
		TextField panelDihedralTextField = new TextField();
		panelDihedralTextField.setLayoutX(6.0);
		panelDihedralTextField.setLayoutY(125+additionalYSize);
		panelDihedralTextField.setPrefWidth(340);
		panelDihedralTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelDihedralTextField);
		
		ChoiceBox<String> panelDihedralChoiceBox = new ChoiceBox<String>();
		panelDihedralChoiceBox.setLayoutX(347.0);
		panelDihedralChoiceBox.setLayoutY(126+additionalYSize);
		panelDihedralChoiceBox.setPrefWidth(47);
		panelDihedralChoiceBox.setPrefHeight(30);
		panelDihedralChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelDihedralChoiceBox);
		
		Separator innerSectionUpperSeparator = new Separator();
		innerSectionUpperSeparator.setLayoutX(-2);
		innerSectionUpperSeparator.setLayoutY(164+additionalYSize);
		innerSectionUpperSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(innerSectionUpperSeparator);
		
		Label innerSectionLabel = new Label("Inner section");
		innerSectionLabel.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 15));
		innerSectionLabel.setTextFill(Paint.valueOf("#0400ff"));
		innerSectionLabel.setLayoutX(6.0);
		innerSectionLabel.setLayoutY(164+additionalYSize);
		contentPane.getChildren().add(innerSectionLabel);
		
		Separator innerSectionLowerSeparator = new Separator();
		innerSectionLowerSeparator.setLayoutX(-8);
		innerSectionLowerSeparator.setLayoutY(184+additionalYSize);
		innerSectionLowerSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(innerSectionLowerSeparator);
		
		Label panelInnerSectionChordLabel = new Label("Chord:");
		panelInnerSectionChordLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelInnerSectionChordLabel.setLayoutX(7.0);
		panelInnerSectionChordLabel.setLayoutY(184+additionalYSize);
		contentPane.getChildren().add(panelInnerSectionChordLabel);
		
		TextField panelInnerSectionChordTextField = new TextField();
		panelInnerSectionChordTextField.setLayoutX(7.0);
		panelInnerSectionChordTextField.setLayoutY(205+additionalYSize);
		panelInnerSectionChordTextField.setPrefWidth(340);
		panelInnerSectionChordTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelInnerSectionChordTextField);
		
		ChoiceBox<String> panelInnerSectionChordChoiceBox = new ChoiceBox<String>();
		panelInnerSectionChordChoiceBox.setLayoutX(348.0);
		panelInnerSectionChordChoiceBox.setLayoutY(206+additionalYSize);
		panelInnerSectionChordChoiceBox.setPrefWidth(47);
		panelInnerSectionChordChoiceBox.setPrefHeight(30);
		panelInnerSectionChordChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(panelInnerSectionChordChoiceBox);
		
		Label panelInnerSectionTwistLabel = new Label("Twist angle:");
		panelInnerSectionTwistLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelInnerSectionTwistLabel.setLayoutX(7.0);
		panelInnerSectionTwistLabel.setLayoutY(236+additionalYSize);
		contentPane.getChildren().add(panelInnerSectionTwistLabel);
		
		TextField panelInnerSectionTwistTextField = new TextField();
		panelInnerSectionTwistTextField.setLayoutX(7.0);
		panelInnerSectionTwistTextField.setLayoutY(257+additionalYSize);
		panelInnerSectionTwistTextField.setPrefWidth(340);
		panelInnerSectionTwistTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelInnerSectionTwistTextField);
		
		ChoiceBox<String> panelInnerSectionTwistChoiceBox = new ChoiceBox<String>();
		panelInnerSectionTwistChoiceBox.setLayoutX(348.0);
		panelInnerSectionTwistChoiceBox.setLayoutY(258+additionalYSize);
		panelInnerSectionTwistChoiceBox.setPrefWidth(47);
		panelInnerSectionTwistChoiceBox.setPrefHeight(30);
		panelInnerSectionTwistChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelInnerSectionTwistChoiceBox);
		
		Label panelInnerSectionAirfoilPathLabel = new Label("Airfoil:");
		panelInnerSectionAirfoilPathLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelInnerSectionAirfoilPathLabel.setLayoutX(7.0);
		panelInnerSectionAirfoilPathLabel.setLayoutY(288+additionalYSize);
		contentPane.getChildren().add(panelInnerSectionAirfoilPathLabel);
		
		TextField panelInnerSectionAirfoilPathTextField = new TextField();
		panelInnerSectionAirfoilPathTextField.setLayoutX(6.0);
		panelInnerSectionAirfoilPathTextField.setLayoutY(309+additionalYSize);
		panelInnerSectionAirfoilPathTextField.setPrefWidth(340);
		panelInnerSectionAirfoilPathTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelInnerSectionAirfoilPathTextField);
		
		Button panelInnerSectionChooseAirfoilPathButton = new Button("...");
		panelInnerSectionChooseAirfoilPathButton.setLayoutX(350.0);
		panelInnerSectionChooseAirfoilPathButton.setLayoutY(309+additionalYSize);
		panelInnerSectionChooseAirfoilPathButton.setPrefWidth(24);
		panelInnerSectionChooseAirfoilPathButton.setPrefHeight(31);
		panelInnerSectionChooseAirfoilPathButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chooseAirfoilFile(panelInnerSectionAirfoilPathTextField);
			}
		});
		contentPane.getChildren().add(panelInnerSectionChooseAirfoilPathButton);
		
		Button panelInnerSectionAirfoilDetailsButton = new Button("Details");
		panelInnerSectionAirfoilDetailsButton.setLayoutX(380);
		panelInnerSectionAirfoilDetailsButton.setLayoutY(309+additionalYSize);
		panelInnerSectionAirfoilDetailsButton.setPrefWidth(55);
		panelInnerSectionAirfoilDetailsButton.setPrefHeight(31);
		panelInnerSectionAirfoilDetailsButton.setFont(Font.font("System", FontWeight.BOLD, 12));
		panelInnerSectionAirfoilDetailsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				
				if(!hTailAirfoilDetailsButtonAndTabsMap.containsKey(panelInnerSectionAirfoilDetailsButton)) {
					try {
						hTailAirfoilDetailsButtonAndTabsMap.put(
								panelInnerSectionAirfoilDetailsButton, 
								tabPaneHTailViewAndAirfoils.getTabs().size()
								);
						showAirfoilData(
								Paths.get(panelInnerSectionAirfoilPathTextField.getText()).getFileName().toString(),
								panelInnerSectionAirfoilDetailsButton,
								ComponentEnum.HORIZONTAL_TAIL
								);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		final Tooltip warning = new Tooltip("WARNING : The airfoil details are already opened !!");
		panelInnerSectionAirfoilDetailsButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Point2D p = panelInnerSectionAirfoilDetailsButton
						.localToScreen(
								-2.5*panelInnerSectionAirfoilDetailsButton.getLayoutBounds().getMaxX(),
								1.2*panelInnerSectionAirfoilDetailsButton.getLayoutBounds().getMaxY()
								);
				if(hTailAirfoilDetailsButtonAndTabsMap.containsKey(panelInnerSectionAirfoilDetailsButton)) {
					warning.show(panelInnerSectionAirfoilDetailsButton, p.getX(), p.getY());
				}
			}
		});
		panelInnerSectionAirfoilDetailsButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				warning.hide();
			}
		});
		contentPane.getChildren().add(panelInnerSectionAirfoilDetailsButton);
		panelInnerSectionAirfoilDetailsButton.disableProperty().bind(
				Bindings.isEmpty(panelInnerSectionAirfoilPathTextField.textProperty())
				);	
		
		Separator outerSectionUpperSeparator = new Separator();
		outerSectionUpperSeparator.setLayoutX(-2);
		outerSectionUpperSeparator.setLayoutY(360+additionalYSize);
		outerSectionUpperSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(outerSectionUpperSeparator);
		
		Label outerSectionLabel = new Label("Outer section");
		outerSectionLabel.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 15));
		outerSectionLabel.setTextFill(Paint.valueOf("#0400ff"));
		outerSectionLabel.setLayoutX(6.0);
		outerSectionLabel.setLayoutY(360+additionalYSize);
		contentPane.getChildren().add(outerSectionLabel);
		
		Separator outerSectionLowerSeparator = new Separator();
		outerSectionLowerSeparator.setLayoutX(-8);
		outerSectionLowerSeparator.setLayoutY(380+additionalYSize);
		outerSectionLowerSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(outerSectionLowerSeparator);
		
		Label panelOuterSectionChordLabel = new Label("Chord:");
		panelOuterSectionChordLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelOuterSectionChordLabel.setLayoutX(6.0);
		panelOuterSectionChordLabel.setLayoutY(383+additionalYSize);
		contentPane.getChildren().add(panelOuterSectionChordLabel);
		
		TextField panelOuterSectionChordTextField = new TextField();
		panelOuterSectionChordTextField.setLayoutX(6.0);
		panelOuterSectionChordTextField.setLayoutY(404+additionalYSize);
		panelOuterSectionChordTextField.setPrefWidth(340);
		panelOuterSectionChordTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelOuterSectionChordTextField);
		
		ChoiceBox<String> panelOuterSectionChordChoiceBox = new ChoiceBox<String>();
		panelOuterSectionChordChoiceBox.setLayoutX(348.0);
		panelOuterSectionChordChoiceBox.setLayoutY(405+additionalYSize);
		panelOuterSectionChordChoiceBox.setPrefWidth(47);
		panelOuterSectionChordChoiceBox.setPrefHeight(30);
		panelOuterSectionChordChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(panelOuterSectionChordChoiceBox);
		
		Label panelOuterSectionTwistLabel = new Label("Twist angle:");
		panelOuterSectionTwistLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelOuterSectionTwistLabel.setLayoutX(6.0);
		panelOuterSectionTwistLabel.setLayoutY(435+additionalYSize);
		contentPane.getChildren().add(panelOuterSectionTwistLabel);
		
		TextField panelOuterSectionTwistTextField = new TextField();
		panelOuterSectionTwistTextField.setLayoutX(6.0);
		panelOuterSectionTwistTextField.setLayoutY(456+additionalYSize);
		panelOuterSectionTwistTextField.setPrefWidth(340);
		panelOuterSectionTwistTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelOuterSectionTwistTextField);
		
		ChoiceBox<String> panelOuterSectionTwistChoiceBox = new ChoiceBox<String>();
		panelOuterSectionTwistChoiceBox.setLayoutX(348.0);
		panelOuterSectionTwistChoiceBox.setLayoutY(457+additionalYSize);
		panelOuterSectionTwistChoiceBox.setPrefWidth(47);
		panelOuterSectionTwistChoiceBox.setPrefHeight(30);
		panelOuterSectionTwistChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelOuterSectionTwistChoiceBox);
		
		Label panelOuterSectionAirfoilPathLabel = new Label("Airfoil:");
		panelOuterSectionAirfoilPathLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelOuterSectionAirfoilPathLabel.setLayoutX(6.0);
		panelOuterSectionAirfoilPathLabel.setLayoutY(487+additionalYSize);
		contentPane.getChildren().add(panelOuterSectionAirfoilPathLabel);
		
		TextField panelOuterSectionAirfoilPathTextField = new TextField();
		panelOuterSectionAirfoilPathTextField.setLayoutX(6.0);
		panelOuterSectionAirfoilPathTextField.setLayoutY(508+additionalYSize);
		panelOuterSectionAirfoilPathTextField.setPrefWidth(340);
		panelOuterSectionAirfoilPathTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelOuterSectionAirfoilPathTextField);
		
		Button panelOuterSectionChooseAirfoilPathButton = new Button("...");
		panelOuterSectionChooseAirfoilPathButton.setLayoutX(350.0);
		panelOuterSectionChooseAirfoilPathButton.setLayoutY(508+additionalYSize);
		panelOuterSectionChooseAirfoilPathButton.setPrefWidth(24);
		panelOuterSectionChooseAirfoilPathButton.setPrefHeight(31);
		panelOuterSectionChooseAirfoilPathButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chooseAirfoilFile(panelOuterSectionAirfoilPathTextField);
			}
		});
		contentPane.getChildren().add(panelOuterSectionChooseAirfoilPathButton);
		
		Button panelOuterSectionAirfoilDetailsButton = new Button("Details");
		panelOuterSectionAirfoilDetailsButton.setLayoutX(380);
		panelOuterSectionAirfoilDetailsButton.setLayoutY(508+additionalYSize);
		panelOuterSectionAirfoilDetailsButton.setPrefWidth(55);
		panelOuterSectionAirfoilDetailsButton.setPrefHeight(31);
		panelOuterSectionAirfoilDetailsButton.setFont(Font.font("System", FontWeight.BOLD, 12));
		panelOuterSectionAirfoilDetailsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				if(!hTailAirfoilDetailsButtonAndTabsMap.containsKey(panelOuterSectionAirfoilDetailsButton)) {
					try {
						hTailAirfoilDetailsButtonAndTabsMap.put(
								panelOuterSectionAirfoilDetailsButton, 
								tabPaneHTailViewAndAirfoils.getTabs().size()
								);
						showAirfoilData(
								Paths.get(panelOuterSectionAirfoilPathTextField.getText()).getFileName().toString(),
								panelOuterSectionAirfoilDetailsButton,
								ComponentEnum.HORIZONTAL_TAIL
								);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		});
		panelOuterSectionAirfoilDetailsButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Point2D p = panelOuterSectionAirfoilDetailsButton
						.localToScreen(
								-2.5*panelOuterSectionAirfoilDetailsButton.getLayoutBounds().getMaxX(),
								1.2*panelOuterSectionAirfoilDetailsButton.getLayoutBounds().getMaxY()
								);
				if(hTailAirfoilDetailsButtonAndTabsMap.containsKey(panelOuterSectionAirfoilDetailsButton)) {
					warning.show(panelOuterSectionAirfoilDetailsButton, p.getX(), p.getY());
				}
			}
		});
		panelOuterSectionAirfoilDetailsButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				warning.hide();
			}
		});
		contentPane.getChildren().add(panelOuterSectionAirfoilDetailsButton);
		panelOuterSectionAirfoilDetailsButton.disableProperty().bind(
				Bindings.isEmpty(panelOuterSectionAirfoilPathTextField.textProperty())
				);		
		
		textFieldHTailSpanPanelList.add(panelSpanTextField);
		textFieldHTailSweepLEPanelList.add(panelSweepLETextField);
		textFieldHTailDihedralPanelList.add(panelDihedralTextField);
		textFieldHTailInnerChordPanelList.add(panelInnerSectionChordTextField);
		textFieldHTailInnerTwistPanelList.add(panelInnerSectionTwistTextField);
		textFieldHTailInnerAirfoilPanelList.add(panelInnerSectionAirfoilPathTextField);
		textFieldHTailOuterChordPanelList.add(panelOuterSectionChordTextField);
		textFieldHTailOuterTwistPanelList.add(panelOuterSectionTwistTextField);
		textFieldHTailOuterAirfoilPanelList.add(panelOuterSectionAirfoilPathTextField);
		choiceBoxHTailSpanPanelUnitList.add(panelSpanChoiceBox);
		choiceBoxHTailSweepLEPanelUnitList.add(panelSweepLEChoiceBox);
		choiceBoxHTailDihedralPanelUnitList.add(panelDihedralChoiceBox);
		choiceBoxHTailInnerChordPanelUnitList.add(panelInnerSectionChordChoiceBox);
		choiceBoxHTailInnerTwistPanelUnitList.add(panelInnerSectionTwistChoiceBox);
		choiceBoxHTailOuterChordPanelUnitList.add(panelOuterSectionChordChoiceBox);
		choiceBoxHTailOuterTwistPanelUnitList.add(panelOuterSectionTwistChoiceBox);
		detailButtonHTailInnerAirfoilList.add(panelInnerSectionAirfoilDetailsButton);
		detailButtonHTailOuterAirfoilList.add(panelOuterSectionAirfoilDetailsButton);
		chooseInnerHTailAirfoilFileButtonList.add(panelInnerSectionChooseAirfoilPathButton);
		chooseOuterHTailAirfoilFileButtonList.add(panelOuterSectionChooseAirfoilPathButton);
		
		newPanelTab.setContent(contentPane);
		tabPaneHTailPanels.getTabs().add(newPanelTab);
		
		System.gc();
		
	}
	
	@FXML
	private void addElevator() {
		
		Tab newElevatorTab = new Tab("Elevator " + (tabPaneHTailElevators.getTabs().size()+1));
		Pane contentPane = new Pane();
		
		Label elevatorTypeLabel = new Label("Type:");
		elevatorTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		elevatorTypeLabel.setLayoutX(6.0);
		elevatorTypeLabel.setLayoutY(0.0);
		contentPane.getChildren().add(elevatorTypeLabel);
		
		ChoiceBox<String> elevatorTypeChoiceBox = new ChoiceBox<String>();
		elevatorTypeChoiceBox.setLayoutX(6.0);
		elevatorTypeChoiceBox.setLayoutY(21);
		elevatorTypeChoiceBox.setPrefWidth(340);
		elevatorTypeChoiceBox.setPrefHeight(31);
		elevatorTypeChoiceBox.setItems(elevatorTypeList);
		contentPane.getChildren().add(elevatorTypeChoiceBox);
		
		Label elevatorInnerPositionLabel = new Label("Inner position (% semispan):");
		elevatorInnerPositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		elevatorInnerPositionLabel.setLayoutX(6.0);
		elevatorInnerPositionLabel.setLayoutY(50.0);
		contentPane.getChildren().add(elevatorInnerPositionLabel);
		
		TextField elevatorInnerPositionTextField = new TextField();
		elevatorInnerPositionTextField.setLayoutX(6.0);
		elevatorInnerPositionTextField.setLayoutY(71);
		elevatorInnerPositionTextField.setPrefWidth(340);
		elevatorInnerPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(elevatorInnerPositionTextField);
		
		Label elevatorOuterPositionLabel = new Label("Outer position (% semispan):");
		elevatorOuterPositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		elevatorOuterPositionLabel.setLayoutX(6.0);
		elevatorOuterPositionLabel.setLayoutY(102.0);
		contentPane.getChildren().add(elevatorOuterPositionLabel);
		
		TextField elevatorOuterPositionTextField = new TextField();
		elevatorOuterPositionTextField.setLayoutX(6.0);
		elevatorOuterPositionTextField.setLayoutY(123);
		elevatorOuterPositionTextField.setPrefWidth(340);
		elevatorOuterPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(elevatorOuterPositionTextField);
		
		Label elevatorInnerChordRatioLabel = new Label("Inner chord ratio:");
		elevatorInnerChordRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		elevatorInnerChordRatioLabel.setLayoutX(6.0);
		elevatorInnerChordRatioLabel.setLayoutY(154.0);
		contentPane.getChildren().add(elevatorInnerChordRatioLabel);
		
		TextField elevatorInnerChordRatioTextField = new TextField();
		elevatorInnerChordRatioTextField.setLayoutX(6.0);
		elevatorInnerChordRatioTextField.setLayoutY(175);
		elevatorInnerChordRatioTextField.setPrefWidth(340);
		elevatorInnerChordRatioTextField.setPrefHeight(31);
		contentPane.getChildren().add(elevatorInnerChordRatioTextField);
		
		Label elevatorOuterChordRatioLabel = new Label("Outer chord ratio:");
		elevatorOuterChordRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		elevatorOuterChordRatioLabel.setLayoutX(6.0);
		elevatorOuterChordRatioLabel.setLayoutY(206.0);
		contentPane.getChildren().add(elevatorOuterChordRatioLabel);
		
		TextField elevatorOuterChordRatioTextField = new TextField();
		elevatorOuterChordRatioTextField.setLayoutX(6.0);
		elevatorOuterChordRatioTextField.setLayoutY(227);
		elevatorOuterChordRatioTextField.setPrefWidth(340);
		elevatorOuterChordRatioTextField.setPrefHeight(31);
		contentPane.getChildren().add(elevatorOuterChordRatioTextField);
		
		Label elevatorMinimumDeflectionLabel = new Label("Minimum deflection angle:");
		elevatorMinimumDeflectionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		elevatorMinimumDeflectionLabel.setLayoutX(6.0);
		elevatorMinimumDeflectionLabel.setLayoutY(258.0);
		contentPane.getChildren().add(elevatorMinimumDeflectionLabel);
		
		TextField elevatorMinimumDeflectionTextField = new TextField();
		elevatorMinimumDeflectionTextField.setLayoutX(6.0);
		elevatorMinimumDeflectionTextField.setLayoutY(279);
		elevatorMinimumDeflectionTextField.setPrefWidth(340);
		elevatorMinimumDeflectionTextField.setPrefHeight(31);
		contentPane.getChildren().add(elevatorMinimumDeflectionTextField);
		
		ChoiceBox<String> elevatorMinimumDeflectionChoiceBox = new ChoiceBox<String>();
		elevatorMinimumDeflectionChoiceBox.setLayoutX(348.0);
		elevatorMinimumDeflectionChoiceBox.setLayoutY(279);
		elevatorMinimumDeflectionChoiceBox.setPrefWidth(47);
		elevatorMinimumDeflectionChoiceBox.setPrefHeight(30);
		elevatorMinimumDeflectionChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(elevatorMinimumDeflectionChoiceBox);
		
		Label elevatorMaximumDeflectionLabel = new Label("Maximum deflection angle:");
		elevatorMaximumDeflectionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		elevatorMaximumDeflectionLabel.setLayoutX(6.0);
		elevatorMaximumDeflectionLabel.setLayoutY(310.0);
		contentPane.getChildren().add(elevatorMaximumDeflectionLabel);
		
		TextField elevatorMaximumDeflectionTextField = new TextField();
		elevatorMaximumDeflectionTextField.setLayoutX(6.0);
		elevatorMaximumDeflectionTextField.setLayoutY(331);
		elevatorMaximumDeflectionTextField.setPrefWidth(340);
		elevatorMaximumDeflectionTextField.setPrefHeight(31);
		contentPane.getChildren().add(elevatorMaximumDeflectionTextField);
		
		ChoiceBox<String> elevatorMaximumDeflectionChoiceBox = new ChoiceBox<String>();
		elevatorMaximumDeflectionChoiceBox.setLayoutX(348.0);
		elevatorMaximumDeflectionChoiceBox.setLayoutY(331);
		elevatorMaximumDeflectionChoiceBox.setPrefWidth(47);
		elevatorMaximumDeflectionChoiceBox.setPrefHeight(30);
		elevatorMaximumDeflectionChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(elevatorMaximumDeflectionChoiceBox);
		
		choiceBoxHTailElevatorTypeList.add(elevatorTypeChoiceBox);
		textFieldHTailInnerPositionElevatorList.add(elevatorInnerPositionTextField);
		textFieldHTailOuterPositionElevatorList.add(elevatorOuterPositionTextField);
		textFieldHTailInnerChordRatioElevatorList.add(elevatorInnerChordRatioTextField);
		textFieldHTailOuterChordRatioElevatorList.add(elevatorOuterChordRatioTextField);
		textFieldHTailMinimumDeflectionAngleElevatorList.add(elevatorMinimumDeflectionTextField);
		textFieldHTailMaximumDeflectionAngleElevatorList.add(elevatorMaximumDeflectionTextField);
		choiceBoxHTailMinimumDeflectionAngleElevatorUnitList.add(elevatorMinimumDeflectionChoiceBox);
		choiceBoxHTailMaximumDeflectionAngleElevatorUnitList.add(elevatorMaximumDeflectionChoiceBox);
		
		newElevatorTab.setContent(contentPane);
		tabPaneHTailElevators.getTabs().add(newElevatorTab);
		
		System.gc();
		
	}
	
	@FXML
	private void addVTailPanel() {
		
		Tab newPanelTab = new Tab("Panel " + (tabPaneVTailPanels.getTabs().size()+1));
		Pane contentPane = new Pane();
		
		int additionalYSize = 0;
		
		if(tabPaneVTailPanels.getTabs().size() > 0) {

			Label linkedToPreviousPanelLabel = new Label("Linked to previous panel:");
			linkedToPreviousPanelLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
			linkedToPreviousPanelLabel.setLayoutX(6.0);
			linkedToPreviousPanelLabel.setLayoutY(14.0);
			contentPane.getChildren().add(linkedToPreviousPanelLabel);

			CheckBox linkedToPreviousCheckBox = new CheckBox();
			linkedToPreviousCheckBox.setPrefWidth(21.0);
			linkedToPreviousCheckBox.setPrefHeight(17.0);
			linkedToPreviousCheckBox.setLayoutX(190.0);
			linkedToPreviousCheckBox.setLayoutY(16.0);
			contentPane.getChildren().add(linkedToPreviousCheckBox);
			checkBoxVTailLinkedToPreviousPanelList.add(linkedToPreviousCheckBox);

			additionalYSize = 47;
			
		}

		Label panelSpanLabel = new Label("Span:");
		panelSpanLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelSpanLabel.setLayoutX(6.0);
		panelSpanLabel.setLayoutY(additionalYSize);
		contentPane.getChildren().add(panelSpanLabel);
		
		TextField panelSpanTextField = new TextField();
		panelSpanTextField.setLayoutX(6.0);
		panelSpanTextField.setLayoutY(21+additionalYSize);
		panelSpanTextField.setPrefWidth(340);
		panelSpanTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelSpanTextField);
		
		ChoiceBox<String> panelSpanChoiceBox = new ChoiceBox<String>();
		panelSpanChoiceBox.setLayoutX(347.0);
		panelSpanChoiceBox.setLayoutY(22+additionalYSize);
		panelSpanChoiceBox.setPrefWidth(47);
		panelSpanChoiceBox.setPrefHeight(30);
		panelSpanChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(panelSpanChoiceBox);
		
		Label panelSweepLELabel = new Label("Sweep L.E.:");
		panelSweepLELabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelSweepLELabel.setLayoutX(6.0);
		panelSweepLELabel.setLayoutY(52+additionalYSize);
		contentPane.getChildren().add(panelSweepLELabel);
		
		TextField panelSweepLETextField = new TextField();
		panelSweepLETextField.setLayoutX(6.0);
		panelSweepLETextField.setLayoutY(73+additionalYSize);
		panelSweepLETextField.setPrefWidth(340);
		panelSweepLETextField.setPrefHeight(31);
		contentPane.getChildren().add(panelSweepLETextField);
		
		ChoiceBox<String> panelSweepLEChoiceBox = new ChoiceBox<String>();
		panelSweepLEChoiceBox.setLayoutX(347.0);
		panelSweepLEChoiceBox.setLayoutY(74+additionalYSize);
		panelSweepLEChoiceBox.setPrefWidth(47);
		panelSweepLEChoiceBox.setPrefHeight(30);
		panelSweepLEChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelSweepLEChoiceBox);
		
		Label panelDihedralLabel = new Label("Dihedral angle:");
		panelDihedralLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelDihedralLabel.setLayoutX(6.0);
		panelDihedralLabel.setLayoutY(104+additionalYSize);
		contentPane.getChildren().add(panelDihedralLabel);
		
		TextField panelDihedralTextField = new TextField();
		panelDihedralTextField.setLayoutX(6.0);
		panelDihedralTextField.setLayoutY(125+additionalYSize);
		panelDihedralTextField.setPrefWidth(340);
		panelDihedralTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelDihedralTextField);
		
		ChoiceBox<String> panelDihedralChoiceBox = new ChoiceBox<String>();
		panelDihedralChoiceBox.setLayoutX(347.0);
		panelDihedralChoiceBox.setLayoutY(126+additionalYSize);
		panelDihedralChoiceBox.setPrefWidth(47);
		panelDihedralChoiceBox.setPrefHeight(30);
		panelDihedralChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelDihedralChoiceBox);
		
		Separator innerSectionUpperSeparator = new Separator();
		innerSectionUpperSeparator.setLayoutX(-2);
		innerSectionUpperSeparator.setLayoutY(164+additionalYSize);
		innerSectionUpperSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(innerSectionUpperSeparator);
		
		Label innerSectionLabel = new Label("Inner section");
		innerSectionLabel.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 15));
		innerSectionLabel.setTextFill(Paint.valueOf("#0400ff"));
		innerSectionLabel.setLayoutX(6.0);
		innerSectionLabel.setLayoutY(164+additionalYSize);
		contentPane.getChildren().add(innerSectionLabel);
		
		Separator innerSectionLowerSeparator = new Separator();
		innerSectionLowerSeparator.setLayoutX(-8);
		innerSectionLowerSeparator.setLayoutY(184+additionalYSize);
		innerSectionLowerSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(innerSectionLowerSeparator);
		
		Label panelInnerSectionChordLabel = new Label("Chord:");
		panelInnerSectionChordLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelInnerSectionChordLabel.setLayoutX(7.0);
		panelInnerSectionChordLabel.setLayoutY(184+additionalYSize);
		contentPane.getChildren().add(panelInnerSectionChordLabel);
		
		TextField panelInnerSectionChordTextField = new TextField();
		panelInnerSectionChordTextField.setLayoutX(7.0);
		panelInnerSectionChordTextField.setLayoutY(205+additionalYSize);
		panelInnerSectionChordTextField.setPrefWidth(340);
		panelInnerSectionChordTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelInnerSectionChordTextField);
		
		ChoiceBox<String> panelInnerSectionChordChoiceBox = new ChoiceBox<String>();
		panelInnerSectionChordChoiceBox.setLayoutX(348.0);
		panelInnerSectionChordChoiceBox.setLayoutY(206+additionalYSize);
		panelInnerSectionChordChoiceBox.setPrefWidth(47);
		panelInnerSectionChordChoiceBox.setPrefHeight(30);
		panelInnerSectionChordChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(panelInnerSectionChordChoiceBox);
		
		Label panelInnerSectionTwistLabel = new Label("Twist angle:");
		panelInnerSectionTwistLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelInnerSectionTwistLabel.setLayoutX(7.0);
		panelInnerSectionTwistLabel.setLayoutY(236+additionalYSize);
		contentPane.getChildren().add(panelInnerSectionTwistLabel);
		
		TextField panelInnerSectionTwistTextField = new TextField();
		panelInnerSectionTwistTextField.setLayoutX(7.0);
		panelInnerSectionTwistTextField.setLayoutY(257+additionalYSize);
		panelInnerSectionTwistTextField.setPrefWidth(340);
		panelInnerSectionTwistTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelInnerSectionTwistTextField);
		
		ChoiceBox<String> panelInnerSectionTwistChoiceBox = new ChoiceBox<String>();
		panelInnerSectionTwistChoiceBox.setLayoutX(348.0);
		panelInnerSectionTwistChoiceBox.setLayoutY(258+additionalYSize);
		panelInnerSectionTwistChoiceBox.setPrefWidth(47);
		panelInnerSectionTwistChoiceBox.setPrefHeight(30);
		panelInnerSectionTwistChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelInnerSectionTwistChoiceBox);
		
		Label panelInnerSectionAirfoilPathLabel = new Label("Airfoil:");
		panelInnerSectionAirfoilPathLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelInnerSectionAirfoilPathLabel.setLayoutX(7.0);
		panelInnerSectionAirfoilPathLabel.setLayoutY(288+additionalYSize);
		contentPane.getChildren().add(panelInnerSectionAirfoilPathLabel);
		
		TextField panelInnerSectionAirfoilPathTextField = new TextField();
		panelInnerSectionAirfoilPathTextField.setLayoutX(6.0);
		panelInnerSectionAirfoilPathTextField.setLayoutY(309+additionalYSize);
		panelInnerSectionAirfoilPathTextField.setPrefWidth(340);
		panelInnerSectionAirfoilPathTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelInnerSectionAirfoilPathTextField);
		
		Button panelInnerSectionChooseAirfoilPathButton = new Button("...");
		panelInnerSectionChooseAirfoilPathButton.setLayoutX(350.0);
		panelInnerSectionChooseAirfoilPathButton.setLayoutY(309+additionalYSize);
		panelInnerSectionChooseAirfoilPathButton.setPrefWidth(24);
		panelInnerSectionChooseAirfoilPathButton.setPrefHeight(31);
		panelInnerSectionChooseAirfoilPathButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chooseAirfoilFile(panelInnerSectionAirfoilPathTextField);
			}
		});
		contentPane.getChildren().add(panelInnerSectionChooseAirfoilPathButton);
		
		Button panelInnerSectionAirfoilDetailsButton = new Button("Details");
		panelInnerSectionAirfoilDetailsButton.setLayoutX(380);
		panelInnerSectionAirfoilDetailsButton.setLayoutY(309+additionalYSize);
		panelInnerSectionAirfoilDetailsButton.setPrefWidth(55);
		panelInnerSectionAirfoilDetailsButton.setPrefHeight(31);
		panelInnerSectionAirfoilDetailsButton.setFont(Font.font("System", FontWeight.BOLD, 12));
		panelInnerSectionAirfoilDetailsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				
				if(!vTailAirfoilDetailsButtonAndTabsMap.containsKey(panelInnerSectionAirfoilDetailsButton)) {
					try {
						vTailAirfoilDetailsButtonAndTabsMap.put(
								panelInnerSectionAirfoilDetailsButton, 
								tabPaneVTailViewAndAirfoils.getTabs().size()
								);
						showAirfoilData(
								Paths.get(panelInnerSectionAirfoilPathTextField.getText()).getFileName().toString(),
								panelInnerSectionAirfoilDetailsButton,
								ComponentEnum.VERTICAL_TAIL
								);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		final Tooltip warning = new Tooltip("WARNING : The airfoil details are already opened !!");
		panelInnerSectionAirfoilDetailsButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Point2D p = panelInnerSectionAirfoilDetailsButton
						.localToScreen(
								-2.5*panelInnerSectionAirfoilDetailsButton.getLayoutBounds().getMaxX(),
								1.2*panelInnerSectionAirfoilDetailsButton.getLayoutBounds().getMaxY()
								);
				if(vTailAirfoilDetailsButtonAndTabsMap.containsKey(panelInnerSectionAirfoilDetailsButton)) {
					warning.show(panelInnerSectionAirfoilDetailsButton, p.getX(), p.getY());
				}
			}
		});
		panelInnerSectionAirfoilDetailsButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				warning.hide();
			}
		});
		contentPane.getChildren().add(panelInnerSectionAirfoilDetailsButton);
		panelInnerSectionAirfoilDetailsButton.disableProperty().bind(
				Bindings.isEmpty(panelInnerSectionAirfoilPathTextField.textProperty())
				);	
		
		Separator outerSectionUpperSeparator = new Separator();
		outerSectionUpperSeparator.setLayoutX(-2);
		outerSectionUpperSeparator.setLayoutY(360+additionalYSize);
		outerSectionUpperSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(outerSectionUpperSeparator);
		
		Label outerSectionLabel = new Label("Outer section");
		outerSectionLabel.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 15));
		outerSectionLabel.setTextFill(Paint.valueOf("#0400ff"));
		outerSectionLabel.setLayoutX(6.0);
		outerSectionLabel.setLayoutY(360+additionalYSize);
		contentPane.getChildren().add(outerSectionLabel);
		
		Separator outerSectionLowerSeparator = new Separator();
		outerSectionLowerSeparator.setLayoutX(-8);
		outerSectionLowerSeparator.setLayoutY(380+additionalYSize);
		outerSectionLowerSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(outerSectionLowerSeparator);
		
		Label panelOuterSectionChordLabel = new Label("Chord:");
		panelOuterSectionChordLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelOuterSectionChordLabel.setLayoutX(6.0);
		panelOuterSectionChordLabel.setLayoutY(383+additionalYSize);
		contentPane.getChildren().add(panelOuterSectionChordLabel);
		
		TextField panelOuterSectionChordTextField = new TextField();
		panelOuterSectionChordTextField.setLayoutX(6.0);
		panelOuterSectionChordTextField.setLayoutY(404+additionalYSize);
		panelOuterSectionChordTextField.setPrefWidth(340);
		panelOuterSectionChordTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelOuterSectionChordTextField);
		
		ChoiceBox<String> panelOuterSectionChordChoiceBox = new ChoiceBox<String>();
		panelOuterSectionChordChoiceBox.setLayoutX(348.0);
		panelOuterSectionChordChoiceBox.setLayoutY(405+additionalYSize);
		panelOuterSectionChordChoiceBox.setPrefWidth(47);
		panelOuterSectionChordChoiceBox.setPrefHeight(30);
		panelOuterSectionChordChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(panelOuterSectionChordChoiceBox);
		
		Label panelOuterSectionTwistLabel = new Label("Twist angle:");
		panelOuterSectionTwistLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelOuterSectionTwistLabel.setLayoutX(6.0);
		panelOuterSectionTwistLabel.setLayoutY(435+additionalYSize);
		contentPane.getChildren().add(panelOuterSectionTwistLabel);
		
		TextField panelOuterSectionTwistTextField = new TextField();
		panelOuterSectionTwistTextField.setLayoutX(6.0);
		panelOuterSectionTwistTextField.setLayoutY(456+additionalYSize);
		panelOuterSectionTwistTextField.setPrefWidth(340);
		panelOuterSectionTwistTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelOuterSectionTwistTextField);
		
		ChoiceBox<String> panelOuterSectionTwistChoiceBox = new ChoiceBox<String>();
		panelOuterSectionTwistChoiceBox.setLayoutX(348.0);
		panelOuterSectionTwistChoiceBox.setLayoutY(457+additionalYSize);
		panelOuterSectionTwistChoiceBox.setPrefWidth(47);
		panelOuterSectionTwistChoiceBox.setPrefHeight(30);
		panelOuterSectionTwistChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelOuterSectionTwistChoiceBox);
		
		Label panelOuterSectionAirfoilPathLabel = new Label("Airfoil:");
		panelOuterSectionAirfoilPathLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelOuterSectionAirfoilPathLabel.setLayoutX(6.0);
		panelOuterSectionAirfoilPathLabel.setLayoutY(487+additionalYSize);
		contentPane.getChildren().add(panelOuterSectionAirfoilPathLabel);
		
		TextField panelOuterSectionAirfoilPathTextField = new TextField();
		panelOuterSectionAirfoilPathTextField.setLayoutX(6.0);
		panelOuterSectionAirfoilPathTextField.setLayoutY(508+additionalYSize);
		panelOuterSectionAirfoilPathTextField.setPrefWidth(340);
		panelOuterSectionAirfoilPathTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelOuterSectionAirfoilPathTextField);
		
		Button panelOuterSectionChooseAirfoilPathButton = new Button("...");
		panelOuterSectionChooseAirfoilPathButton.setLayoutX(350.0);
		panelOuterSectionChooseAirfoilPathButton.setLayoutY(508+additionalYSize);
		panelOuterSectionChooseAirfoilPathButton.setPrefWidth(24);
		panelOuterSectionChooseAirfoilPathButton.setPrefHeight(31);
		panelOuterSectionChooseAirfoilPathButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chooseAirfoilFile(panelOuterSectionAirfoilPathTextField);
			}
		});
		contentPane.getChildren().add(panelOuterSectionChooseAirfoilPathButton);
		
		Button panelOuterSectionAirfoilDetailsButton = new Button("Details");
		panelOuterSectionAirfoilDetailsButton.setLayoutX(380);
		panelOuterSectionAirfoilDetailsButton.setLayoutY(508+additionalYSize);
		panelOuterSectionAirfoilDetailsButton.setPrefWidth(55);
		panelOuterSectionAirfoilDetailsButton.setPrefHeight(31);
		panelOuterSectionAirfoilDetailsButton.setFont(Font.font("System", FontWeight.BOLD, 12));
		panelOuterSectionAirfoilDetailsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				if(!vTailAirfoilDetailsButtonAndTabsMap.containsKey(panelOuterSectionAirfoilDetailsButton)) {
					try {
						vTailAirfoilDetailsButtonAndTabsMap.put(
								panelOuterSectionAirfoilDetailsButton, 
								tabPaneVTailViewAndAirfoils.getTabs().size()
								);
						showAirfoilData(
								Paths.get(panelOuterSectionAirfoilPathTextField.getText()).getFileName().toString(),
								panelOuterSectionAirfoilDetailsButton,
								ComponentEnum.VERTICAL_TAIL
								);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		});
		panelOuterSectionAirfoilDetailsButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Point2D p = panelOuterSectionAirfoilDetailsButton
						.localToScreen(
								-2.5*panelOuterSectionAirfoilDetailsButton.getLayoutBounds().getMaxX(),
								1.2*panelOuterSectionAirfoilDetailsButton.getLayoutBounds().getMaxY()
								);
				if(vTailAirfoilDetailsButtonAndTabsMap.containsKey(panelOuterSectionAirfoilDetailsButton)) {
					warning.show(panelOuterSectionAirfoilDetailsButton, p.getX(), p.getY());
				}
			}
		});
		panelOuterSectionAirfoilDetailsButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				warning.hide();
			}
		});
		contentPane.getChildren().add(panelOuterSectionAirfoilDetailsButton);
		panelOuterSectionAirfoilDetailsButton.disableProperty().bind(
				Bindings.isEmpty(panelOuterSectionAirfoilPathTextField.textProperty())
				);		
		
		textFieldVTailSpanPanelList.add(panelSpanTextField);
		textFieldVTailSweepLEPanelList.add(panelSweepLETextField);
		textFieldVTailDihedralPanelList.add(panelDihedralTextField);
		textFieldVTailInnerChordPanelList.add(panelInnerSectionChordTextField);
		textFieldVTailInnerTwistPanelList.add(panelInnerSectionTwistTextField);
		textFieldVTailInnerAirfoilPanelList.add(panelInnerSectionAirfoilPathTextField);
		textFieldVTailOuterChordPanelList.add(panelOuterSectionChordTextField);
		textFieldVTailOuterTwistPanelList.add(panelOuterSectionTwistTextField);
		textFieldVTailOuterAirfoilPanelList.add(panelOuterSectionAirfoilPathTextField);
		choiceBoxVTailSpanPanelUnitList.add(panelSpanChoiceBox);
		choiceBoxVTailSweepLEPanelUnitList.add(panelSweepLEChoiceBox);
		choiceBoxVTailDihedralPanelUnitList.add(panelDihedralChoiceBox);
		choiceBoxVTailInnerChordPanelUnitList.add(panelInnerSectionChordChoiceBox);
		choiceBoxVTailInnerTwistPanelUnitList.add(panelInnerSectionTwistChoiceBox);
		choiceBoxVTailOuterChordPanelUnitList.add(panelOuterSectionChordChoiceBox);
		choiceBoxVTailOuterTwistPanelUnitList.add(panelOuterSectionTwistChoiceBox);
		detailButtonVTailInnerAirfoilList.add(panelInnerSectionAirfoilDetailsButton);
		detailButtonVTailOuterAirfoilList.add(panelOuterSectionAirfoilDetailsButton);
		chooseInnerVTailAirfoilFileButtonList.add(panelInnerSectionChooseAirfoilPathButton);
		chooseOuterVTailAirfoilFileButtonList.add(panelOuterSectionChooseAirfoilPathButton);
		
		newPanelTab.setContent(contentPane);
		tabPaneVTailPanels.getTabs().add(newPanelTab);
	
		System.gc();
		
	}
	
	@FXML
	private void addRudder() {
		
		Tab newRudderTab = new Tab("Rudder " + (tabPaneVTailRudders.getTabs().size()+1));
		Pane contentPane = new Pane();
		
		Label rudderTypeLabel = new Label("Type:");
		rudderTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		rudderTypeLabel.setLayoutX(6.0);
		rudderTypeLabel.setLayoutY(0.0);
		contentPane.getChildren().add(rudderTypeLabel);
		
		ChoiceBox<String> rudderTypeChoiceBox = new ChoiceBox<String>();
		rudderTypeChoiceBox.setLayoutX(6.0);
		rudderTypeChoiceBox.setLayoutY(21);
		rudderTypeChoiceBox.setPrefWidth(340);
		rudderTypeChoiceBox.setPrefHeight(31);
		rudderTypeChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(rudderTypeChoiceBox);
		
		Label rudderInnerPositionLabel = new Label("Inner position (% semispan):");
		rudderInnerPositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		rudderInnerPositionLabel.setLayoutX(6.0);
		rudderInnerPositionLabel.setLayoutY(52.0);
		contentPane.getChildren().add(rudderInnerPositionLabel);
		
		TextField rudderInnerPositionTextField = new TextField();
		rudderInnerPositionTextField.setLayoutX(6.0);
		rudderInnerPositionTextField.setLayoutY(73);
		rudderInnerPositionTextField.setPrefWidth(340);
		rudderInnerPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(rudderInnerPositionTextField);
		
		Label rudderOuterPositionLabel = new Label("Outer position (% semispan):");
		rudderOuterPositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		rudderOuterPositionLabel.setLayoutX(6.0);
		rudderOuterPositionLabel.setLayoutY(104.0);
		contentPane.getChildren().add(rudderOuterPositionLabel);
		
		TextField rudderOuterPositionTextField = new TextField();
		rudderOuterPositionTextField.setLayoutX(6.0);
		rudderOuterPositionTextField.setLayoutY(125);
		rudderOuterPositionTextField.setPrefWidth(340);
		rudderOuterPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(rudderOuterPositionTextField);
		
		Label rudderInnerChordRatioLabel = new Label("Inner chord ratio:");
		rudderInnerChordRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		rudderInnerChordRatioLabel.setLayoutX(6.0);
		rudderInnerChordRatioLabel.setLayoutY(156.0);
		contentPane.getChildren().add(rudderInnerChordRatioLabel);
		
		TextField rudderInnerChordRatioTextField = new TextField();
		rudderInnerChordRatioTextField.setLayoutX(6.0);
		rudderInnerChordRatioTextField.setLayoutY(177);
		rudderInnerChordRatioTextField.setPrefWidth(340);
		rudderInnerChordRatioTextField.setPrefHeight(31);
		contentPane.getChildren().add(rudderInnerChordRatioTextField);
		
		Label rudderOuterChordRatioLabel = new Label("Outer chord ratio:");
		rudderOuterChordRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		rudderOuterChordRatioLabel.setLayoutX(6.0);
		rudderOuterChordRatioLabel.setLayoutY(208.0);
		contentPane.getChildren().add(rudderOuterChordRatioLabel);
		
		TextField rudderOuterChordRatioTextField = new TextField();
		rudderOuterChordRatioTextField.setLayoutX(6.0);
		rudderOuterChordRatioTextField.setLayoutY(229);
		rudderOuterChordRatioTextField.setPrefWidth(340);
		rudderOuterChordRatioTextField.setPrefHeight(31);
		contentPane.getChildren().add(rudderOuterChordRatioTextField);
		
		Label rudderMinimumDeflectionLabel = new Label("Minimum deflection angle:");
		rudderMinimumDeflectionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		rudderMinimumDeflectionLabel.setLayoutX(6.0);
		rudderMinimumDeflectionLabel.setLayoutY(260.0);
		contentPane.getChildren().add(rudderMinimumDeflectionLabel);
		
		TextField rudderMinimumDeflectionTextField = new TextField();
		rudderMinimumDeflectionTextField.setLayoutX(6.0);
		rudderMinimumDeflectionTextField.setLayoutY(281);
		rudderMinimumDeflectionTextField.setPrefWidth(340);
		rudderMinimumDeflectionTextField.setPrefHeight(31);
		contentPane.getChildren().add(rudderMinimumDeflectionTextField);
		
		ChoiceBox<String> rudderMinimumDeflectionChoiceBox = new ChoiceBox<String>();
		rudderMinimumDeflectionChoiceBox.setLayoutX(348.0);
		rudderMinimumDeflectionChoiceBox.setLayoutY(281);
		rudderMinimumDeflectionChoiceBox.setPrefWidth(47);
		rudderMinimumDeflectionChoiceBox.setPrefHeight(30);
		rudderMinimumDeflectionChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(rudderMinimumDeflectionChoiceBox);
		
		Label rudderMaximumDeflectionLabel = new Label("Maximum deflection angle:");
		rudderMaximumDeflectionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		rudderMaximumDeflectionLabel.setLayoutX(6.0);
		rudderMaximumDeflectionLabel.setLayoutY(312.0);
		contentPane.getChildren().add(rudderMaximumDeflectionLabel);
		
		TextField rudderMaximumDeflectionTextField = new TextField();
		rudderMaximumDeflectionTextField.setLayoutX(6.0);
		rudderMaximumDeflectionTextField.setLayoutY(333);
		rudderMaximumDeflectionTextField.setPrefWidth(340);
		rudderMaximumDeflectionTextField.setPrefHeight(31);
		contentPane.getChildren().add(rudderMaximumDeflectionTextField);
		
		ChoiceBox<String> rudderMaximumDeflectionChoiceBox = new ChoiceBox<String>();
		rudderMaximumDeflectionChoiceBox.setLayoutX(348.0);
		rudderMaximumDeflectionChoiceBox.setLayoutY(333);
		rudderMaximumDeflectionChoiceBox.setPrefWidth(47);
		rudderMaximumDeflectionChoiceBox.setPrefHeight(30);
		rudderMaximumDeflectionChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(rudderMaximumDeflectionChoiceBox);
		
		choiceBoxVTailRudderTypeList.add(rudderTypeChoiceBox);
		textFieldVTailInnerPositionRudderList.add(rudderInnerPositionTextField);
		textFieldVTailOuterPositionRudderList.add(rudderOuterPositionTextField);
		textFieldVTailInnerChordRatioRudderList.add(rudderInnerChordRatioTextField);
		textFieldVTailOuterChordRatioRudderList.add(rudderOuterChordRatioTextField);
		textFieldVTailMinimumDeflectionAngleRudderList.add(rudderMinimumDeflectionTextField);
		textFieldVTailMaximumDeflectionAngleRudderList.add(rudderMaximumDeflectionTextField);
		choiceBoxVTailMinimumDeflectionAngleRudderUnitList.add(rudderMinimumDeflectionChoiceBox);
		choiceBoxVTailMaximumDeflectionAngleRudderUnitList.add(rudderMaximumDeflectionChoiceBox);
		
		newRudderTab.setContent(contentPane);
		tabPaneVTailRudders.getTabs().add(newRudderTab);
		
		System.gc();
		
	}
	
	@FXML
	private void addCanardPanel() {
		
		Tab newPanelTab = new Tab("Panel " + (tabPaneCanardPanels.getTabs().size()+1));
		Pane contentPane = new Pane();
		
		int additionalYSize = 0;
		
		if(tabPaneCanardPanels.getTabs().size() > 0) {

			Label linkedToPreviousPanelLabel = new Label("Linked to previous panel:");
			linkedToPreviousPanelLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
			linkedToPreviousPanelLabel.setLayoutX(6.0);
			linkedToPreviousPanelLabel.setLayoutY(14.0);
			contentPane.getChildren().add(linkedToPreviousPanelLabel);

			CheckBox linkedToPreviousCheckBox = new CheckBox();
			linkedToPreviousCheckBox.setPrefWidth(21.0);
			linkedToPreviousCheckBox.setPrefHeight(17.0);
			linkedToPreviousCheckBox.setLayoutX(190.0);
			linkedToPreviousCheckBox.setLayoutY(16.0);
			contentPane.getChildren().add(linkedToPreviousCheckBox);
			checkBoxCanardLinkedToPreviousPanelList.add(linkedToPreviousCheckBox);

			additionalYSize = 47;
			
		}

		Label panelSpanLabel = new Label("Span:");
		panelSpanLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelSpanLabel.setLayoutX(6.0);
		panelSpanLabel.setLayoutY(additionalYSize);
		contentPane.getChildren().add(panelSpanLabel);
		
		TextField panelSpanTextField = new TextField();
		panelSpanTextField.setLayoutX(6.0);
		panelSpanTextField.setLayoutY(21+additionalYSize);
		panelSpanTextField.setPrefWidth(340);
		panelSpanTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelSpanTextField);
		
		ChoiceBox<String> panelSpanChoiceBox = new ChoiceBox<String>();
		panelSpanChoiceBox.setLayoutX(347.0);
		panelSpanChoiceBox.setLayoutY(22+additionalYSize);
		panelSpanChoiceBox.setPrefWidth(47);
		panelSpanChoiceBox.setPrefHeight(30);
		panelSpanChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(panelSpanChoiceBox);
		
		Label panelSweepLELabel = new Label("Sweep L.E.:");
		panelSweepLELabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelSweepLELabel.setLayoutX(6.0);
		panelSweepLELabel.setLayoutY(52+additionalYSize);
		contentPane.getChildren().add(panelSweepLELabel);
		
		TextField panelSweepLETextField = new TextField();
		panelSweepLETextField.setLayoutX(6.0);
		panelSweepLETextField.setLayoutY(73+additionalYSize);
		panelSweepLETextField.setPrefWidth(340);
		panelSweepLETextField.setPrefHeight(31);
		contentPane.getChildren().add(panelSweepLETextField);
		
		ChoiceBox<String> panelSweepLEChoiceBox = new ChoiceBox<String>();
		panelSweepLEChoiceBox.setLayoutX(347.0);
		panelSweepLEChoiceBox.setLayoutY(74+additionalYSize);
		panelSweepLEChoiceBox.setPrefWidth(47);
		panelSweepLEChoiceBox.setPrefHeight(30);
		panelSweepLEChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelSweepLEChoiceBox);
		
		Label panelDihedralLabel = new Label("Dihedral angle:");
		panelDihedralLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelDihedralLabel.setLayoutX(6.0);
		panelDihedralLabel.setLayoutY(104+additionalYSize);
		contentPane.getChildren().add(panelDihedralLabel);
		
		TextField panelDihedralTextField = new TextField();
		panelDihedralTextField.setLayoutX(6.0);
		panelDihedralTextField.setLayoutY(125+additionalYSize);
		panelDihedralTextField.setPrefWidth(340);
		panelDihedralTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelDihedralTextField);
		
		ChoiceBox<String> panelDihedralChoiceBox = new ChoiceBox<String>();
		panelDihedralChoiceBox.setLayoutX(347.0);
		panelDihedralChoiceBox.setLayoutY(126+additionalYSize);
		panelDihedralChoiceBox.setPrefWidth(47);
		panelDihedralChoiceBox.setPrefHeight(30);
		panelDihedralChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelDihedralChoiceBox);
		
		Separator innerSectionUpperSeparator = new Separator();
		innerSectionUpperSeparator.setLayoutX(-2);
		innerSectionUpperSeparator.setLayoutY(164+additionalYSize);
		innerSectionUpperSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(innerSectionUpperSeparator);
		
		Label innerSectionLabel = new Label("Inner section");
		innerSectionLabel.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 15));
		innerSectionLabel.setTextFill(Paint.valueOf("#0400ff"));
		innerSectionLabel.setLayoutX(6.0);
		innerSectionLabel.setLayoutY(164+additionalYSize);
		contentPane.getChildren().add(innerSectionLabel);
		
		Separator innerSectionLowerSeparator = new Separator();
		innerSectionLowerSeparator.setLayoutX(-8);
		innerSectionLowerSeparator.setLayoutY(184+additionalYSize);
		innerSectionLowerSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(innerSectionLowerSeparator);
		
		Label panelInnerSectionChordLabel = new Label("Chord:");
		panelInnerSectionChordLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelInnerSectionChordLabel.setLayoutX(7.0);
		panelInnerSectionChordLabel.setLayoutY(184+additionalYSize);
		contentPane.getChildren().add(panelInnerSectionChordLabel);
		
		TextField panelInnerSectionChordTextField = new TextField();
		panelInnerSectionChordTextField.setLayoutX(7.0);
		panelInnerSectionChordTextField.setLayoutY(205+additionalYSize);
		panelInnerSectionChordTextField.setPrefWidth(340);
		panelInnerSectionChordTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelInnerSectionChordTextField);
		
		ChoiceBox<String> panelInnerSectionChordChoiceBox = new ChoiceBox<String>();
		panelInnerSectionChordChoiceBox.setLayoutX(348.0);
		panelInnerSectionChordChoiceBox.setLayoutY(206+additionalYSize);
		panelInnerSectionChordChoiceBox.setPrefWidth(47);
		panelInnerSectionChordChoiceBox.setPrefHeight(30);
		panelInnerSectionChordChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(panelInnerSectionChordChoiceBox);
		
		Label panelInnerSectionTwistLabel = new Label("Twist angle:");
		panelInnerSectionTwistLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelInnerSectionTwistLabel.setLayoutX(7.0);
		panelInnerSectionTwistLabel.setLayoutY(236+additionalYSize);
		contentPane.getChildren().add(panelInnerSectionTwistLabel);
		
		TextField panelInnerSectionTwistTextField = new TextField();
		panelInnerSectionTwistTextField.setLayoutX(7.0);
		panelInnerSectionTwistTextField.setLayoutY(257+additionalYSize);
		panelInnerSectionTwistTextField.setPrefWidth(340);
		panelInnerSectionTwistTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelInnerSectionTwistTextField);
		
		ChoiceBox<String> panelInnerSectionTwistChoiceBox = new ChoiceBox<String>();
		panelInnerSectionTwistChoiceBox.setLayoutX(348.0);
		panelInnerSectionTwistChoiceBox.setLayoutY(258+additionalYSize);
		panelInnerSectionTwistChoiceBox.setPrefWidth(47);
		panelInnerSectionTwistChoiceBox.setPrefHeight(30);
		panelInnerSectionTwistChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelInnerSectionTwistChoiceBox);
		
		Label panelInnerSectionAirfoilPathLabel = new Label("Airfoil:");
		panelInnerSectionAirfoilPathLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelInnerSectionAirfoilPathLabel.setLayoutX(7.0);
		panelInnerSectionAirfoilPathLabel.setLayoutY(288+additionalYSize);
		contentPane.getChildren().add(panelInnerSectionAirfoilPathLabel);
		
		TextField panelInnerSectionAirfoilPathTextField = new TextField();
		panelInnerSectionAirfoilPathTextField.setLayoutX(6.0);
		panelInnerSectionAirfoilPathTextField.setLayoutY(309+additionalYSize);
		panelInnerSectionAirfoilPathTextField.setPrefWidth(340);
		panelInnerSectionAirfoilPathTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelInnerSectionAirfoilPathTextField);
		
		Button panelInnerSectionChooseAirfoilPathButton = new Button("...");
		panelInnerSectionChooseAirfoilPathButton.setLayoutX(350.0);
		panelInnerSectionChooseAirfoilPathButton.setLayoutY(309+additionalYSize);
		panelInnerSectionChooseAirfoilPathButton.setPrefWidth(24);
		panelInnerSectionChooseAirfoilPathButton.setPrefHeight(31);
		panelInnerSectionChooseAirfoilPathButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chooseAirfoilFile(panelInnerSectionAirfoilPathTextField);
			}
		});
		contentPane.getChildren().add(panelInnerSectionChooseAirfoilPathButton);
		
		Button panelInnerSectionAirfoilDetailsButton = new Button("Details");
		panelInnerSectionAirfoilDetailsButton.setLayoutX(380);
		panelInnerSectionAirfoilDetailsButton.setLayoutY(309+additionalYSize);
		panelInnerSectionAirfoilDetailsButton.setPrefWidth(55);
		panelInnerSectionAirfoilDetailsButton.setPrefHeight(31);
		panelInnerSectionAirfoilDetailsButton.setFont(Font.font("System", FontWeight.BOLD, 12));
		panelInnerSectionAirfoilDetailsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				
				if(!canardAirfoilDetailsButtonAndTabsMap.containsKey(panelInnerSectionAirfoilDetailsButton)) {
					try {
						canardAirfoilDetailsButtonAndTabsMap.put(
								panelInnerSectionAirfoilDetailsButton, 
								tabPaneCanardViewAndAirfoils.getTabs().size()
								);
						showAirfoilData(
								Paths.get(panelInnerSectionAirfoilPathTextField.getText()).getFileName().toString(),
								panelInnerSectionAirfoilDetailsButton,
								ComponentEnum.CANARD
								);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		final Tooltip warning = new Tooltip("WARNING : The airfoil details are already opened !!");
		panelInnerSectionAirfoilDetailsButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Point2D p = panelInnerSectionAirfoilDetailsButton
						.localToScreen(
								-2.5*panelInnerSectionAirfoilDetailsButton.getLayoutBounds().getMaxX(),
								1.2*panelInnerSectionAirfoilDetailsButton.getLayoutBounds().getMaxY()
								);
				if(canardAirfoilDetailsButtonAndTabsMap.containsKey(panelInnerSectionAirfoilDetailsButton)) {
					warning.show(panelInnerSectionAirfoilDetailsButton, p.getX(), p.getY());
				}
			}
		});
		panelInnerSectionAirfoilDetailsButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				warning.hide();
			}
		});
		contentPane.getChildren().add(panelInnerSectionAirfoilDetailsButton);
		panelInnerSectionAirfoilDetailsButton.disableProperty().bind(
				Bindings.isEmpty(panelInnerSectionAirfoilPathTextField.textProperty())
				);	
		
		Separator outerSectionUpperSeparator = new Separator();
		outerSectionUpperSeparator.setLayoutX(-2);
		outerSectionUpperSeparator.setLayoutY(360+additionalYSize);
		outerSectionUpperSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(outerSectionUpperSeparator);
		
		Label outerSectionLabel = new Label("Outer section");
		outerSectionLabel.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 15));
		outerSectionLabel.setTextFill(Paint.valueOf("#0400ff"));
		outerSectionLabel.setLayoutX(6.0);
		outerSectionLabel.setLayoutY(360+additionalYSize);
		contentPane.getChildren().add(outerSectionLabel);
		
		Separator outerSectionLowerSeparator = new Separator();
		outerSectionLowerSeparator.setLayoutX(-8);
		outerSectionLowerSeparator.setLayoutY(380+additionalYSize);
		outerSectionLowerSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(outerSectionLowerSeparator);
		
		Label panelOuterSectionChordLabel = new Label("Chord:");
		panelOuterSectionChordLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelOuterSectionChordLabel.setLayoutX(6.0);
		panelOuterSectionChordLabel.setLayoutY(383+additionalYSize);
		contentPane.getChildren().add(panelOuterSectionChordLabel);
		
		TextField panelOuterSectionChordTextField = new TextField();
		panelOuterSectionChordTextField.setLayoutX(6.0);
		panelOuterSectionChordTextField.setLayoutY(404+additionalYSize);
		panelOuterSectionChordTextField.setPrefWidth(340);
		panelOuterSectionChordTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelOuterSectionChordTextField);
		
		ChoiceBox<String> panelOuterSectionChordChoiceBox = new ChoiceBox<String>();
		panelOuterSectionChordChoiceBox.setLayoutX(348.0);
		panelOuterSectionChordChoiceBox.setLayoutY(405+additionalYSize);
		panelOuterSectionChordChoiceBox.setPrefWidth(47);
		panelOuterSectionChordChoiceBox.setPrefHeight(30);
		panelOuterSectionChordChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(panelOuterSectionChordChoiceBox);
		
		Label panelOuterSectionTwistLabel = new Label("Twist angle:");
		panelOuterSectionTwistLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelOuterSectionTwistLabel.setLayoutX(6.0);
		panelOuterSectionTwistLabel.setLayoutY(435+additionalYSize);
		contentPane.getChildren().add(panelOuterSectionTwistLabel);
		
		TextField panelOuterSectionTwistTextField = new TextField();
		panelOuterSectionTwistTextField.setLayoutX(6.0);
		panelOuterSectionTwistTextField.setLayoutY(456+additionalYSize);
		panelOuterSectionTwistTextField.setPrefWidth(340);
		panelOuterSectionTwistTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelOuterSectionTwistTextField);
		
		ChoiceBox<String> panelOuterSectionTwistChoiceBox = new ChoiceBox<String>();
		panelOuterSectionTwistChoiceBox.setLayoutX(348.0);
		panelOuterSectionTwistChoiceBox.setLayoutY(457+additionalYSize);
		panelOuterSectionTwistChoiceBox.setPrefWidth(47);
		panelOuterSectionTwistChoiceBox.setPrefHeight(30);
		panelOuterSectionTwistChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(panelOuterSectionTwistChoiceBox);
		
		Label panelOuterSectionAirfoilPathLabel = new Label("Airfoil:");
		panelOuterSectionAirfoilPathLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		panelOuterSectionAirfoilPathLabel.setLayoutX(6.0);
		panelOuterSectionAirfoilPathLabel.setLayoutY(487+additionalYSize);
		contentPane.getChildren().add(panelOuterSectionAirfoilPathLabel);
		
		TextField panelOuterSectionAirfoilPathTextField = new TextField();
		panelOuterSectionAirfoilPathTextField.setLayoutX(6.0);
		panelOuterSectionAirfoilPathTextField.setLayoutY(508+additionalYSize);
		panelOuterSectionAirfoilPathTextField.setPrefWidth(340);
		panelOuterSectionAirfoilPathTextField.setPrefHeight(31);
		contentPane.getChildren().add(panelOuterSectionAirfoilPathTextField);
		
		Button panelOuterSectionChooseAirfoilPathButton = new Button("...");
		panelOuterSectionChooseAirfoilPathButton.setLayoutX(350.0);
		panelOuterSectionChooseAirfoilPathButton.setLayoutY(508+additionalYSize);
		panelOuterSectionChooseAirfoilPathButton.setPrefWidth(24);
		panelOuterSectionChooseAirfoilPathButton.setPrefHeight(31);
		panelOuterSectionChooseAirfoilPathButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chooseAirfoilFile(panelOuterSectionAirfoilPathTextField);
			}
		});
		contentPane.getChildren().add(panelOuterSectionChooseAirfoilPathButton);
		
		Button panelOuterSectionAirfoilDetailsButton = new Button("Details");
		panelOuterSectionAirfoilDetailsButton.setLayoutX(380);
		panelOuterSectionAirfoilDetailsButton.setLayoutY(508+additionalYSize);
		panelOuterSectionAirfoilDetailsButton.setPrefWidth(55);
		panelOuterSectionAirfoilDetailsButton.setPrefHeight(31);
		panelOuterSectionAirfoilDetailsButton.setFont(Font.font("System", FontWeight.BOLD, 12));
		panelOuterSectionAirfoilDetailsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				if(!canardAirfoilDetailsButtonAndTabsMap.containsKey(panelOuterSectionAirfoilDetailsButton)) {
					try {
						canardAirfoilDetailsButtonAndTabsMap.put(
								panelOuterSectionAirfoilDetailsButton, 
								tabPaneCanardViewAndAirfoils.getTabs().size()
								);
						showAirfoilData(
								Paths.get(panelOuterSectionAirfoilPathTextField.getText()).getFileName().toString(),
								panelOuterSectionAirfoilDetailsButton,
								ComponentEnum.CANARD
								);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		});
		panelOuterSectionAirfoilDetailsButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Point2D p = panelOuterSectionAirfoilDetailsButton
						.localToScreen(
								-2.5*panelOuterSectionAirfoilDetailsButton.getLayoutBounds().getMaxX(),
								1.2*panelOuterSectionAirfoilDetailsButton.getLayoutBounds().getMaxY()
								);
				if(canardAirfoilDetailsButtonAndTabsMap.containsKey(panelOuterSectionAirfoilDetailsButton)) {
					warning.show(panelOuterSectionAirfoilDetailsButton, p.getX(), p.getY());
				}
			}
		});
		panelOuterSectionAirfoilDetailsButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				warning.hide();
			}
		});
		contentPane.getChildren().add(panelOuterSectionAirfoilDetailsButton);
		panelOuterSectionAirfoilDetailsButton.disableProperty().bind(
				Bindings.isEmpty(panelOuterSectionAirfoilPathTextField.textProperty())
				);		
		
		textFieldCanardSpanPanelList.add(panelSpanTextField);
		textFieldCanardSweepLEPanelList.add(panelSweepLETextField);
		textFieldCanardDihedralPanelList.add(panelDihedralTextField);
		textFieldCanardInnerChordPanelList.add(panelInnerSectionChordTextField);
		textFieldCanardInnerTwistPanelList.add(panelInnerSectionTwistTextField);
		textFieldCanardInnerAirfoilPanelList.add(panelInnerSectionAirfoilPathTextField);
		textFieldCanardOuterChordPanelList.add(panelOuterSectionChordTextField);
		textFieldCanardOuterTwistPanelList.add(panelOuterSectionTwistTextField);
		textFieldCanardOuterAirfoilPanelList.add(panelOuterSectionAirfoilPathTextField);
		choiceBoxCanardSpanPanelUnitList.add(panelSpanChoiceBox);
		choiceBoxCanardSweepLEPanelUnitList.add(panelSweepLEChoiceBox);
		choiceBoxCanardDihedralPanelUnitList.add(panelDihedralChoiceBox);
		choiceBoxCanardInnerChordPanelUnitList.add(panelInnerSectionChordChoiceBox);
		choiceBoxCanardInnerTwistPanelUnitList.add(panelInnerSectionTwistChoiceBox);
		choiceBoxCanardOuterChordPanelUnitList.add(panelOuterSectionChordChoiceBox);
		choiceBoxCanardOuterTwistPanelUnitList.add(panelOuterSectionTwistChoiceBox);
		detailButtonCanardInnerAirfoilList.add(panelInnerSectionAirfoilDetailsButton);
		detailButtonCanardOuterAirfoilList.add(panelOuterSectionAirfoilDetailsButton);
		chooseInnerCanardAirfoilFileButtonList.add(panelInnerSectionChooseAirfoilPathButton);
		chooseOuterCanardAirfoilFileButtonList.add(panelOuterSectionChooseAirfoilPathButton);
		
		newPanelTab.setContent(contentPane);
		tabPaneCanardPanels.getTabs().add(newPanelTab);
		
		System.gc();
		
	}
	
	@FXML
	private void addControlSurface() {
		
		Tab newControlSurfaceTab = new Tab("Control Surface " + (tabPaneCanardControlSurfaces.getTabs().size()+1));
		Pane contentPane = new Pane();
		
		Label controlSurfaceTypeLabel = new Label("Type:");
		controlSurfaceTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		controlSurfaceTypeLabel.setLayoutX(6.0);
		controlSurfaceTypeLabel.setLayoutY(0.0);
		contentPane.getChildren().add(controlSurfaceTypeLabel);
		
		ChoiceBox<String> controlSurfaceTypeChoiceBox = new ChoiceBox<String>();
		controlSurfaceTypeChoiceBox.setLayoutX(6.0);
		controlSurfaceTypeChoiceBox.setLayoutY(21.0);
		controlSurfaceTypeChoiceBox.setPrefWidth(340);
		controlSurfaceTypeChoiceBox.setPrefHeight(31);
		controlSurfaceTypeChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(controlSurfaceTypeChoiceBox);
		
		Label controlSurfaceInnerPositionLabel = new Label("Inner position (% semispan):");
		controlSurfaceInnerPositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		controlSurfaceInnerPositionLabel.setLayoutX(6.0);
		controlSurfaceInnerPositionLabel.setLayoutY(52.0);
		contentPane.getChildren().add(controlSurfaceInnerPositionLabel);
		
		TextField controlSurfaceInnerPositionTextField = new TextField();
		controlSurfaceInnerPositionTextField.setLayoutX(6.0);
		controlSurfaceInnerPositionTextField.setLayoutY(73);
		controlSurfaceInnerPositionTextField.setPrefWidth(340);
		controlSurfaceInnerPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(controlSurfaceInnerPositionTextField);
		
		Label controlSurfaceOuterPositionLabel = new Label("Outer position (% semispan):");
		controlSurfaceOuterPositionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		controlSurfaceOuterPositionLabel.setLayoutX(6.0);
		controlSurfaceOuterPositionLabel.setLayoutY(104.0);
		contentPane.getChildren().add(controlSurfaceOuterPositionLabel);
		
		TextField controlSurfaceOuterPositionTextField = new TextField();
		controlSurfaceOuterPositionTextField.setLayoutX(6.0);
		controlSurfaceOuterPositionTextField.setLayoutY(125);
		controlSurfaceOuterPositionTextField.setPrefWidth(340);
		controlSurfaceOuterPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(controlSurfaceOuterPositionTextField);
		
		Label controlSurfaceInnerChordRatioLabel = new Label("Inner chord ratio:");
		controlSurfaceInnerChordRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		controlSurfaceInnerChordRatioLabel.setLayoutX(6.0);
		controlSurfaceInnerChordRatioLabel.setLayoutY(156.0);
		contentPane.getChildren().add(controlSurfaceInnerChordRatioLabel);
		
		TextField controlSurfaceInnerChordRatioTextField = new TextField();
		controlSurfaceInnerChordRatioTextField.setLayoutX(6.0);
		controlSurfaceInnerChordRatioTextField.setLayoutY(177);
		controlSurfaceInnerChordRatioTextField.setPrefWidth(340);
		controlSurfaceInnerChordRatioTextField.setPrefHeight(31);
		contentPane.getChildren().add(controlSurfaceInnerChordRatioTextField);
		
		Label controlSurfaceOuterChordRatioLabel = new Label("Outer chord ratio:");
		controlSurfaceOuterChordRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		controlSurfaceOuterChordRatioLabel.setLayoutX(6.0);
		controlSurfaceOuterChordRatioLabel.setLayoutY(208.0);
		contentPane.getChildren().add(controlSurfaceOuterChordRatioLabel);
		
		TextField controlSurfaceOuterChordRatioTextField = new TextField();
		controlSurfaceOuterChordRatioTextField.setLayoutX(6.0);
		controlSurfaceOuterChordRatioTextField.setLayoutY(229);
		controlSurfaceOuterChordRatioTextField.setPrefWidth(340);
		controlSurfaceOuterChordRatioTextField.setPrefHeight(31);
		contentPane.getChildren().add(controlSurfaceOuterChordRatioTextField);
		
		Label controlSurfaceMinimumDeflectionLabel = new Label("Minimum deflection angle:");
		controlSurfaceMinimumDeflectionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		controlSurfaceMinimumDeflectionLabel.setLayoutX(6.0);
		controlSurfaceMinimumDeflectionLabel.setLayoutY(260.0);
		contentPane.getChildren().add(controlSurfaceMinimumDeflectionLabel);
		
		TextField controlSurfaceMinimumDeflectionTextField = new TextField();
		controlSurfaceMinimumDeflectionTextField.setLayoutX(6.0);
		controlSurfaceMinimumDeflectionTextField.setLayoutY(281);
		controlSurfaceMinimumDeflectionTextField.setPrefWidth(340);
		controlSurfaceMinimumDeflectionTextField.setPrefHeight(31);
		contentPane.getChildren().add(controlSurfaceMinimumDeflectionTextField);
		
		ChoiceBox<String> controlSurfaceMinimumDeflectionChoiceBox = new ChoiceBox<String>();
		controlSurfaceMinimumDeflectionChoiceBox.setLayoutX(348.0);
		controlSurfaceMinimumDeflectionChoiceBox.setLayoutY(281);
		controlSurfaceMinimumDeflectionChoiceBox.setPrefWidth(47);
		controlSurfaceMinimumDeflectionChoiceBox.setPrefHeight(30);
		controlSurfaceMinimumDeflectionChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(controlSurfaceMinimumDeflectionChoiceBox);
		
		Label controlSurfaceMaximumDeflectionLabel = new Label("Maximum deflection angle:");
		controlSurfaceMaximumDeflectionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		controlSurfaceMaximumDeflectionLabel.setLayoutX(6.0);
		controlSurfaceMaximumDeflectionLabel.setLayoutY(312.0);
		contentPane.getChildren().add(controlSurfaceMaximumDeflectionLabel);
		
		TextField controlSurfaceMaximumDeflectionTextField = new TextField();
		controlSurfaceMaximumDeflectionTextField.setLayoutX(6.0);
		controlSurfaceMaximumDeflectionTextField.setLayoutY(333);
		controlSurfaceMaximumDeflectionTextField.setPrefWidth(340);
		controlSurfaceMaximumDeflectionTextField.setPrefHeight(31);
		contentPane.getChildren().add(controlSurfaceMaximumDeflectionTextField);
		
		ChoiceBox<String> controlSurfaceMaximumDeflectionChoiceBox = new ChoiceBox<String>();
		controlSurfaceMaximumDeflectionChoiceBox.setLayoutX(348.0);
		controlSurfaceMaximumDeflectionChoiceBox.setLayoutY(333);
		controlSurfaceMaximumDeflectionChoiceBox.setPrefWidth(47);
		controlSurfaceMaximumDeflectionChoiceBox.setPrefHeight(30);
		controlSurfaceMaximumDeflectionChoiceBox.setItems(angleUnitsList);
		contentPane.getChildren().add(controlSurfaceMaximumDeflectionChoiceBox);
		
		choiceBoxCanardControlSurfaceTypeList.add(controlSurfaceTypeChoiceBox);
		textFieldCanardInnerPositionControlSurfaceList.add(controlSurfaceInnerPositionTextField);
		textFieldCanardOuterPositionControlSurfaceList.add(controlSurfaceOuterPositionTextField);
		textFieldCanardInnerChordRatioControlSurfaceList.add(controlSurfaceInnerChordRatioTextField);
		textFieldCanardOuterChordRatioControlSurfaceList.add(controlSurfaceOuterChordRatioTextField);
		textFieldCanardMinimumDeflectionAngleControlSurfaceList.add(controlSurfaceMinimumDeflectionTextField);
		textFieldCanardMaximumDeflectionAngleControlSurfaceList.add(controlSurfaceMaximumDeflectionTextField);
		choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList.add(controlSurfaceMinimumDeflectionChoiceBox);
		choiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList.add(controlSurfaceMaximumDeflectionChoiceBox);
		
		newControlSurfaceTab.setContent(contentPane);
		tabPaneCanardControlSurfaces.getTabs().add(newControlSurfaceTab);
		
		System.gc();
		
	}
	
	@FXML
	private void addNacelle() {
		
		Tab newNacelleTab = new Tab("Nacelle " + (tabPaneNacelles.getTabs().size()+1));
		Pane contentPane = new Pane();
		
		Label nacelleGlobalDataLabel = new Label("GLOBAL DATA:");
		nacelleGlobalDataLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleGlobalDataLabel.setTextFill(Paint.valueOf("#0400ff"));
		nacelleGlobalDataLabel.setLayoutX(6.0);
		nacelleGlobalDataLabel.setLayoutY(6.0);
		contentPane.getChildren().add(nacelleGlobalDataLabel);
		
		Separator globalDataLowerSeparator = new Separator();
		globalDataLowerSeparator.setLayoutX(-20);
		globalDataLowerSeparator.setLayoutY(33);
		globalDataLowerSeparator.setPrefWidth(1312);
		contentPane.getChildren().add(globalDataLowerSeparator);
		
		Label nacelleRoughnessLabel = new Label("Roughness:");
		nacelleRoughnessLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleRoughnessLabel.setLayoutX(6.0);
		nacelleRoughnessLabel.setLayoutY(34.0);
		contentPane.getChildren().add(nacelleRoughnessLabel);
		
		TextField nacelleRoughnessTextField = new TextField();
		nacelleRoughnessTextField.setLayoutX(6.0);
		nacelleRoughnessTextField.setLayoutY(55);
		nacelleRoughnessTextField.setPrefWidth(340);
		nacelleRoughnessTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleRoughnessTextField);
		
		ChoiceBox<String> nacelleRoughnessChoiceBox = new ChoiceBox<String>();
		nacelleRoughnessChoiceBox.setLayoutX(348.0);
		nacelleRoughnessChoiceBox.setLayoutY(56);
		nacelleRoughnessChoiceBox.setPrefWidth(47);
		nacelleRoughnessChoiceBox.setPrefHeight(30);
		nacelleRoughnessChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(nacelleRoughnessChoiceBox);
		
		Separator roughnessLowerSeparator = new Separator();
		roughnessLowerSeparator.setLayoutX(0);
		roughnessLowerSeparator.setLayoutY(95);
		roughnessLowerSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(roughnessLowerSeparator);
		
		Label nacelleGeometryLabel = new Label("GEOMETRY:");
		nacelleGeometryLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleGeometryLabel.setTextFill(Paint.valueOf("#0400ff"));
		nacelleGeometryLabel.setLayoutX(6.0);
		nacelleGeometryLabel.setLayoutY(103.0);
		contentPane.getChildren().add(nacelleGeometryLabel);
		
		Button nacelleEstimateGeometryButton = new Button("Estimate");
		nacelleEstimateGeometryButton.setLayoutX(100);
		nacelleEstimateGeometryButton.setLayoutY(98);
		nacelleEstimateGeometryButton.setPrefWidth(77);
		nacelleEstimateGeometryButton.setPrefHeight(31);
		setEstimateNacelleGeometryAction(nacelleEstimateGeometryButton, newNacelleTab);
		contentPane.getChildren().add(nacelleEstimateGeometryButton);
		
		Separator geometryLowerSeparator = new Separator();
		geometryLowerSeparator.setLayoutX(-28);
		geometryLowerSeparator.setLayoutY(131);
		geometryLowerSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(geometryLowerSeparator);
		
		Label nacelleLengthLabel = new Label("Length:");
		nacelleLengthLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleLengthLabel.setLayoutX(6.0);
		nacelleLengthLabel.setLayoutY(135.0);
		contentPane.getChildren().add(nacelleLengthLabel);
		
		TextField nacelleLengthTextField = new TextField();
		nacelleLengthTextField.setLayoutX(6.0);
		nacelleLengthTextField.setLayoutY(158);
		nacelleLengthTextField.setPrefWidth(340);
		nacelleLengthTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleLengthTextField);
		
		ChoiceBox<String> nacelleLengthChoiceBox = new ChoiceBox<String>();
		nacelleLengthChoiceBox.setLayoutX(348.0);
		nacelleLengthChoiceBox.setLayoutY(158);
		nacelleLengthChoiceBox.setPrefWidth(47);
		nacelleLengthChoiceBox.setPrefHeight(30);
		nacelleLengthChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(nacelleLengthChoiceBox);
		
		Label nacelleMaximumDiameterLabel = new Label("Maximum Diameter:");
		nacelleMaximumDiameterLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleMaximumDiameterLabel.setLayoutX(6.0);
		nacelleMaximumDiameterLabel.setLayoutY(189.0);
		contentPane.getChildren().add(nacelleMaximumDiameterLabel);
		
		TextField nacelleMaximumDiameterTextField = new TextField();
		nacelleMaximumDiameterTextField.setLayoutX(6.0);
		nacelleMaximumDiameterTextField.setLayoutY(210);
		nacelleMaximumDiameterTextField.setPrefWidth(340);
		nacelleMaximumDiameterTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleMaximumDiameterTextField);
		
		ChoiceBox<String> nacelleMaximumDiameterChoiceBox = new ChoiceBox<String>();
		nacelleMaximumDiameterChoiceBox.setLayoutX(348.0);
		nacelleMaximumDiameterChoiceBox.setLayoutY(211);
		nacelleMaximumDiameterChoiceBox.setPrefWidth(47);
		nacelleMaximumDiameterChoiceBox.setPrefHeight(30);
		nacelleMaximumDiameterChoiceBox.setItems(lengthUnitsList);
		contentPane.getChildren().add(nacelleMaximumDiameterChoiceBox);
		
		Label nacelleKInletLabel = new Label("k Inlet:");
		nacelleKInletLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleKInletLabel.setLayoutX(6.0);
		nacelleKInletLabel.setLayoutY(245.0);
		contentPane.getChildren().add(nacelleKInletLabel);
		
		TextField nacelleKInletTextField = new TextField();
		nacelleKInletTextField.setLayoutX(6.0);
		nacelleKInletTextField.setLayoutY(266);
		nacelleKInletTextField.setPrefWidth(340);
		nacelleKInletTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleKInletTextField);
		
		Button nacelleKInletInfoButton = new Button("?");
		nacelleKInletInfoButton.setStyle("-fx-font-size: 8;");
		nacelleKInletInfoButton.setStyle("-fx-font-weight: bold;");
		nacelleKInletInfoButton.setLayoutX(63);
		nacelleKInletInfoButton.setLayoutY(245);
		nacelleKInletInfoButton.setPrefWidth(16);
		nacelleKInletInfoButton.setPrefHeight(18);
		nacelleKInletInfoButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showNacelleKInletInfo();
			}
		});
		contentPane.getChildren().add(nacelleKInletInfoButton);
		
		Label nacelleKOutletLabel = new Label("k Outlet:");
		nacelleKOutletLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleKOutletLabel.setLayoutX(6.0); 
		nacelleKOutletLabel.setLayoutY(304.0);
		contentPane.getChildren().add(nacelleKOutletLabel);
		
		TextField nacelleKOutletTextField = new TextField();
		nacelleKOutletTextField.setLayoutX(6.0);
		nacelleKOutletTextField.setLayoutY(327);
		nacelleKOutletTextField.setPrefWidth(340);
		nacelleKOutletTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleKOutletTextField);
		
		Button nacelleKOutletInfoButton = new Button("?");
		nacelleKOutletInfoButton.setStyle("-fx-font-size: 8;");
		nacelleKOutletInfoButton.setStyle("-fx-font-weight: bold;");
		nacelleKOutletInfoButton.setLayoutX(71);
		nacelleKOutletInfoButton.setLayoutY(304);
		nacelleKOutletInfoButton.setPrefWidth(16);
		nacelleKOutletInfoButton.setPrefHeight(18);
		nacelleKOutletInfoButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showNacelleKOutletInfo();
			}
		});
		contentPane.getChildren().add(nacelleKOutletInfoButton);
		
		Label nacelleKLengthLabel = new Label("k Length:");
		nacelleKLengthLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleKLengthLabel.setLayoutX(6.0);
		nacelleKLengthLabel.setLayoutY(364.0);
		contentPane.getChildren().add(nacelleKLengthLabel);
		
		TextField nacelleKLengthTextField = new TextField();
		nacelleKLengthTextField.setLayoutX(6.0);
		nacelleKLengthTextField.setLayoutY(389);
		nacelleKLengthTextField.setPrefWidth(340);
		nacelleKLengthTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleKLengthTextField);
		
		Button nacelleKLengthInfoButton = new Button("?");
		nacelleKLengthInfoButton.setStyle("-fx-font-size: 8;");
		nacelleKLengthInfoButton.setStyle("-fx-font-weight: bold;");
		nacelleKLengthInfoButton.setLayoutX(75);
		nacelleKLengthInfoButton.setLayoutY(365);
		nacelleKLengthInfoButton.setPrefWidth(16);
		nacelleKLengthInfoButton.setPrefHeight(18);
		nacelleKLengthInfoButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showNacelleKLengthInfo();
			}
		});
		contentPane.getChildren().add(nacelleKLengthInfoButton);
		
		Label nacelleKDiameterOutletLabel = new Label("k Diameter Outlet:");
		nacelleKDiameterOutletLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleKDiameterOutletLabel.setLayoutX(6.0);
		nacelleKDiameterOutletLabel.setLayoutY(430.0);
		contentPane.getChildren().add(nacelleKDiameterOutletLabel);
		
		TextField nacelleKDiameterOutletTextField = new TextField();
		nacelleKDiameterOutletTextField.setLayoutX(6.0);
		nacelleKDiameterOutletTextField.setLayoutY(452);
		nacelleKDiameterOutletTextField.setPrefWidth(340);
		nacelleKDiameterOutletTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleKDiameterOutletTextField);
		
		Button nacelleKDiameterOutletInfoButton = new Button("?");
		nacelleKDiameterOutletInfoButton.setStyle("-fx-font-size: 8;");
		nacelleKDiameterOutletInfoButton.setStyle("-fx-font-weight: bold;");
		nacelleKDiameterOutletInfoButton.setLayoutX(137);
		nacelleKDiameterOutletInfoButton.setLayoutY(432);
		nacelleKDiameterOutletInfoButton.setPrefWidth(16);
		nacelleKDiameterOutletInfoButton.setPrefHeight(18);
		nacelleKDiameterOutletInfoButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showNacelleKDiameterOutletInfo();
			}
		});
		contentPane.getChildren().add(nacelleKDiameterOutletInfoButton);
		
		textFieldNacelleRoughnessList.add(nacelleRoughnessTextField);
		textFieldNacelleLengthList.add(nacelleLengthTextField);
		textFieldNacelleMaximumDiameterList.add(nacelleMaximumDiameterTextField);
		textFieldNacelleKInletList.add(nacelleKInletTextField);
		textFieldNacelleKOutletList.add(nacelleKOutletTextField);
		textFieldNacelleKLengthList.add(nacelleKLengthTextField);
		textFieldNacelleKDiameterOutletList.add(nacelleKDiameterOutletTextField);
		choiceBoxNacelleRoughnessUnitList.add(nacelleRoughnessChoiceBox);
		choiceBoxNacelleLengthUnitList.add(nacelleLengthChoiceBox);
		choiceBoxNacelleMaximumDiameterUnitList.add(nacelleMaximumDiameterChoiceBox);
		nacelleEstimateDimesnsionButtonList.add(nacelleEstimateGeometryButton);
		nacelleKInletInfoButtonList.add(nacelleKInletInfoButton);
		nacelleKOutletInfoButtonList.add(nacelleKOutletInfoButton);
		nacelleKLengthInfoButtonList.add(nacelleKLengthInfoButton);
		nacelleKDiameterOutletInfoButtonList.add(nacelleKDiameterOutletInfoButton);
		
		newNacelleTab.setContent(contentPane);
		tabPaneNacelles.getTabs().add(newNacelleTab);
		
		System.gc();
		
	}
	
	@FXML
	private void addEngine() {
		
		int indexOfEngineTab = tabPaneEngines.getTabs().size();
		
		Tab newEngineTab = new Tab("Engine" + (tabPaneEngines.getTabs().size()+1));
		BorderPane contentBorderPane = new BorderPane();
		
		ToggleGroup enigneToggleGroup = new ToggleGroup();
		powerPlantToggleGropuList.add(enigneToggleGroup);
		
		RadioButton turbojetTurbofanRadioButton = new RadioButton("Turbofan/Turbojet");
		turbojetTurbofanRadioButton.setPadding(new Insets(0, 30, 0, 0));
		turbojetTurbofanRadioButton.setToggleGroup(enigneToggleGroup);
		powerPlantJetRadioButtonList.add(turbojetTurbofanRadioButton);
		setShowEngineDataAction(turbojetTurbofanRadioButton, indexOfEngineTab, EngineTypeEnum.TURBOFAN);
		
		RadioButton turbopropRadioButton = new RadioButton("Turboprop");
		turbopropRadioButton.setPadding(new Insets(0, 30, 0, 0));
		turbopropRadioButton.setToggleGroup(enigneToggleGroup);
		powerPlantTurbopropRadioButtonList.add(turbopropRadioButton);
		setShowEngineDataAction(turbopropRadioButton, indexOfEngineTab, EngineTypeEnum.TURBOPROP);
		
		RadioButton pistonRadioButton = new RadioButton("Piston");
		pistonRadioButton.setPadding(new Insets(0, 30, 0, 0));
		pistonRadioButton.setToggleGroup(enigneToggleGroup);
		powerPlantPistonRadioButtonList.add(pistonRadioButton);
		setShowEngineDataAction(pistonRadioButton, indexOfEngineTab, EngineTypeEnum.PISTON);
		
		ToolBar engineTypeToolBar = new ToolBar();
		engineTypeToolBar.setPrefWidth(200);
		engineTypeToolBar.setPrefHeight(40);
		engineTypeToolBar.getItems().add(turbojetTurbofanRadioButton);
		engineTypeToolBar.getItems().add(turbopropRadioButton);
		engineTypeToolBar.getItems().add(pistonRadioButton);
		
		Pane engineDataPane = new Pane();
		
		contentBorderPane.setTop(engineTypeToolBar);
		contentBorderPane.setCenter(engineDataPane);
		powerPlantBorderPaneMap.put(indexOfEngineTab, contentBorderPane);
		newEngineTab.setContent(contentBorderPane);
		tabPaneEngines.getTabs().add(newEngineTab);
		
		System.gc();
		
	}
	
	@FXML
	private void showTurbojetTurboFanDataRadioButton (int indexOfEngineTab) {
		
		Pane engineDataPane = new Pane();

		Label engineTypeLabel = new Label("Type:");
		engineTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineTypeLabel.setLayoutX(6.0);
		engineTypeLabel.setLayoutY(0.0);
		engineDataPane.getChildren().add(engineTypeLabel);
		
		ChoiceBox<String> engineTypeChoiceBox = new ChoiceBox<String>();
		engineTypeChoiceBox.setLayoutX(6.0);
		engineTypeChoiceBox.setLayoutY(21.0);
		engineTypeChoiceBox.setPrefWidth(340);
		engineTypeChoiceBox.setPrefHeight(31);
		engineTypeChoiceBox.setItems(engineTypeList);
		engineDataPane.getChildren().add(engineTypeChoiceBox);
		engineTurbojetTurbofanTypeChoiceBoxMap.put(indexOfEngineTab, engineTypeChoiceBox); 
		
		Label engineDatabaseLabel = new Label("Database:");
		engineDatabaseLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineDatabaseLabel.setLayoutX(6.0);
		engineDatabaseLabel.setLayoutY(52.0);
		engineDataPane.getChildren().add(engineDatabaseLabel);
		
		TextField engineDatabasePathTextField = new TextField();
		engineDatabasePathTextField.setLayoutX(6.0);
		engineDatabasePathTextField.setLayoutY(73);
		engineDatabasePathTextField.setPrefWidth(340);
		engineDatabasePathTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineDatabasePathTextField);
		engineTurbojetTurbofanDatabaseTextFieldMap.put(indexOfEngineTab, engineDatabasePathTextField);
		
		Button engineDatabasePathChooseButton = new Button("...");
		engineDatabasePathChooseButton.setLayoutX(350.0);
		engineDatabasePathChooseButton.setLayoutY(73);
		engineDatabasePathChooseButton.setPrefWidth(24);
		engineDatabasePathChooseButton.setPrefHeight(31);
		engineDatabasePathChooseButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chooseEngineDatabase(engineDatabasePathTextField);
			}
		});
		engineDataPane.getChildren().add(engineDatabasePathChooseButton);
		
		Label engineLengthLabel = new Label("Length:");
		engineLengthLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineLengthLabel.setLayoutX(6.0);
		engineLengthLabel.setLayoutY(104.0);
		engineDataPane.getChildren().add(engineLengthLabel);
		
		TextField engineLengthTextField = new TextField();
		engineLengthTextField.setLayoutX(6.0);
		engineLengthTextField.setLayoutY(125);
		engineLengthTextField.setPrefWidth(340);
		engineLengthTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineLengthTextField);
		engineTurbojetTurbofanLengthTextFieldMap.put(indexOfEngineTab, engineLengthTextField);
		
		ChoiceBox<String> engineLengthChoiceBox = new ChoiceBox<String>();
		engineLengthChoiceBox.setLayoutX(348.0);
		engineLengthChoiceBox.setLayoutY(125);
		engineLengthChoiceBox.setPrefWidth(47);
		engineLengthChoiceBox.setPrefHeight(30);
		engineLengthChoiceBox.setItems(lengthUnitsList);
		engineDataPane.getChildren().add(engineLengthChoiceBox);
		engineTurbojetTurbofanLengthUnitChoiceBoxMap.put(indexOfEngineTab, engineLengthChoiceBox);
		
		Label engineStaticThrustLabel = new Label("Static Thrust:");
		engineStaticThrustLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineStaticThrustLabel.setLayoutX(6.0);
		engineStaticThrustLabel.setLayoutY(159.0);
		engineDataPane.getChildren().add(engineStaticThrustLabel);
		
		TextField engineStaticThrustTextField = new TextField();
		engineStaticThrustTextField.setLayoutX(6.0);
		engineStaticThrustTextField.setLayoutY(180);
		engineStaticThrustTextField.setPrefWidth(340);
		engineStaticThrustTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineStaticThrustTextField);
		engineTurbojetTurbofanStaticThrustTextFieldMap.put(indexOfEngineTab, engineStaticThrustTextField);
		
		ChoiceBox<String> engineStaticThrustChoiceBox = new ChoiceBox<String>();
		engineStaticThrustChoiceBox.setLayoutX(348.0);
		engineStaticThrustChoiceBox.setLayoutY(180);
		engineStaticThrustChoiceBox.setPrefWidth(47);
		engineStaticThrustChoiceBox.setPrefHeight(30);
		engineStaticThrustChoiceBox.setItems(forceUnitsList);
		engineDataPane.getChildren().add(engineStaticThrustChoiceBox);
		engineTurbojetTurbofanStaticThrustUnitChoiceBoxMap.put(indexOfEngineTab, engineStaticThrustChoiceBox);
		
		Label engineDryMassLabel = new Label("Dry Mass:");
		engineDryMassLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineDryMassLabel.setLayoutX(6.0);
		engineDryMassLabel.setLayoutY(213.0);
		engineDataPane.getChildren().add(engineDryMassLabel);
		
		TextField engineDryMassTextField = new TextField();
		engineDryMassTextField.setLayoutX(6.0);
		engineDryMassTextField.setLayoutY(234);
		engineDryMassTextField.setPrefWidth(340);
		engineDryMassTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineDryMassTextField);
		engineTurbojetTurbofanDryMassTextFieldMap.put(indexOfEngineTab, engineDryMassTextField);
		
		ChoiceBox<String> engineDryMassChoiceBox = new ChoiceBox<String>();
		engineDryMassChoiceBox.setLayoutX(348.0);
		engineDryMassChoiceBox.setLayoutY(234);
		engineDryMassChoiceBox.setPrefWidth(47);
		engineDryMassChoiceBox.setPrefHeight(30);
		engineDryMassChoiceBox.setItems(massUnitsList);
		engineDataPane.getChildren().add(engineDryMassChoiceBox);
		engineTurbojetTurbofanDryMassUnitChoiceBoxMap.put(indexOfEngineTab, engineDryMassChoiceBox);
		
		Button engineDryMassCalculateButton = new Button("Calculate");
		engineDryMassCalculateButton.setLayoutX(397.0);
		engineDryMassCalculateButton.setLayoutY(234);
		engineDryMassCalculateButton.setPrefHeight(31);
		engineDryMassCalculateButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				calculateEngineDryMass(engineDryMassTextField, engineDryMassChoiceBox, indexOfEngineTab);
			}
		});
		engineDataPane.getChildren().add(engineDryMassCalculateButton);
		
		Label engineBPRLabel = new Label("BPR:");
		engineBPRLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineBPRLabel.setLayoutX(6.0);
		engineBPRLabel.setLayoutY(268.0);
		engineDataPane.getChildren().add(engineBPRLabel);
		
		TextField engineBPRTextField = new TextField();
		engineBPRTextField.setLayoutX(6.0);
		engineBPRTextField.setLayoutY(289);
		engineBPRTextField.setPrefWidth(340);
		engineBPRTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineBPRTextField);
		engineTurbojetTurbofanBPRTextFieldMap.put(indexOfEngineTab, engineBPRTextField);
		
		Label engineNumberOfCompressorStagesLabel = new Label("Number Of Compressor Stages:");
		engineNumberOfCompressorStagesLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineNumberOfCompressorStagesLabel.setLayoutX(6.0);
		engineNumberOfCompressorStagesLabel.setLayoutY(323.0);
		engineDataPane.getChildren().add(engineNumberOfCompressorStagesLabel);
		
		TextField engineNumberOfCompressorStagesTextField = new TextField();
		engineNumberOfCompressorStagesTextField.setLayoutX(6.0);
		engineNumberOfCompressorStagesTextField.setLayoutY(344);
		engineNumberOfCompressorStagesTextField.setPrefWidth(340);
		engineNumberOfCompressorStagesTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineNumberOfCompressorStagesTextField);
		engineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap.put(indexOfEngineTab, engineNumberOfCompressorStagesTextField);
		
		Label engineNumberOfShaftsLabel = new Label("Number Of Shafts:");
		engineNumberOfShaftsLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineNumberOfShaftsLabel.setLayoutX(6.0);
		engineNumberOfShaftsLabel.setLayoutY(378.0);
		engineDataPane.getChildren().add(engineNumberOfShaftsLabel);
		
		TextField engineNumberOfShaftsTextField = new TextField();
		engineNumberOfShaftsTextField.setLayoutX(6.0);
		engineNumberOfShaftsTextField.setLayoutY(399);
		engineNumberOfShaftsTextField.setPrefWidth(340);
		engineNumberOfShaftsTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineNumberOfShaftsTextField);
		engineTurbojetTurbofanNumberOfShaftsTextFieldMap.put(indexOfEngineTab, engineNumberOfShaftsTextField);
		
		Label engineOverallPressureRatioLabel = new Label("Overall Pressure Ratio:");
		engineOverallPressureRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineOverallPressureRatioLabel.setLayoutX(6.0);
		engineOverallPressureRatioLabel.setLayoutY(433.0);
		engineDataPane.getChildren().add(engineOverallPressureRatioLabel);
		
		TextField engineOverallPressureRatioTextField = new TextField();
		engineOverallPressureRatioTextField.setLayoutX(6.0);
		engineOverallPressureRatioTextField.setLayoutY(454);
		engineOverallPressureRatioTextField.setPrefWidth(340);
		engineOverallPressureRatioTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineOverallPressureRatioTextField);
		engineTurbojetTurbofanOverallPressureRatioTextFieldMap.put(indexOfEngineTab, engineOverallPressureRatioTextField);
		
		Map<EngineTypeEnum, Pane> enginePaneMap = new HashMap<>();
		enginePaneMap.put(EngineTypeEnum.TURBOFAN, engineDataPane);
		powerPlantPaneMap.put(indexOfEngineTab, enginePaneMap);
		powerPlantBorderPaneMap.get(indexOfEngineTab).setCenter(engineDataPane);
	
		System.gc();
		
	}

	@FXML
	private void showTurbopropDataRadioButton (int indexOfEngineTab) {
		
		Pane engineDataPane = new Pane();
		
		Label engineTypeLabel = new Label("Type:");
		engineTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineTypeLabel.setLayoutX(6.0);
		engineTypeLabel.setLayoutY(0.0);
		engineDataPane.getChildren().add(engineTypeLabel);
		
		ChoiceBox<String> engineTypeChoiceBox = new ChoiceBox<String>();
		engineTypeChoiceBox.setLayoutX(6.0);
		engineTypeChoiceBox.setLayoutY(21.0);
		engineTypeChoiceBox.setPrefWidth(340);
		engineTypeChoiceBox.setPrefHeight(31);
		engineTypeChoiceBox.setItems(engineTypeList);
		engineDataPane.getChildren().add(engineTypeChoiceBox);
		engineTurbopropTypeChoiceBoxMap.put(indexOfEngineTab, engineTypeChoiceBox); 
		
		Label engineDatabaseLabel = new Label("Database:");
		engineDatabaseLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineDatabaseLabel.setLayoutX(6.0);
		engineDatabaseLabel.setLayoutY(52.0);
		engineDataPane.getChildren().add(engineDatabaseLabel);
		
		TextField engineDatabasePathTextField = new TextField();
		engineDatabasePathTextField.setLayoutX(6.0);
		engineDatabasePathTextField.setLayoutY(73);
		engineDatabasePathTextField.setPrefWidth(340);
		engineDatabasePathTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineDatabasePathTextField);
		engineTurbopropDatabaseTextFieldMap.put(indexOfEngineTab, engineDatabasePathTextField);
		
		Button engineDatabasePathChooseButton = new Button("...");
		engineDatabasePathChooseButton.setLayoutX(350.0);
		engineDatabasePathChooseButton.setLayoutY(73);
		engineDatabasePathChooseButton.setPrefWidth(24);
		engineDatabasePathChooseButton.setPrefHeight(31);
		engineDatabasePathChooseButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chooseEngineDatabase(engineDatabasePathTextField);
			}
		});
		engineDataPane.getChildren().add(engineDatabasePathChooseButton);
		
		Label engineLengthLabel = new Label("Length:");
		engineLengthLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineLengthLabel.setLayoutX(6.0);
		engineLengthLabel.setLayoutY(104.0);
		engineDataPane.getChildren().add(engineLengthLabel);
		
		TextField engineLengthTextField = new TextField();
		engineLengthTextField.setLayoutX(6.0);
		engineLengthTextField.setLayoutY(125);
		engineLengthTextField.setPrefWidth(340);
		engineLengthTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineLengthTextField);
		engineTurbopropLengthTextFieldMap.put(indexOfEngineTab, engineLengthTextField);
		
		ChoiceBox<String> engineLengthChoiceBox = new ChoiceBox<String>();
		engineLengthChoiceBox.setLayoutX(348.0);
		engineLengthChoiceBox.setLayoutY(125);
		engineLengthChoiceBox.setPrefWidth(47);
		engineLengthChoiceBox.setPrefHeight(30);
		engineLengthChoiceBox.setItems(lengthUnitsList);
		engineDataPane.getChildren().add(engineLengthChoiceBox);
		engineTurbopropLengthUnitChoiceBoxMap.put(indexOfEngineTab, engineLengthChoiceBox);
		
		Label engineStaticPowerLabel = new Label("Static Power:");
		engineStaticPowerLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineStaticPowerLabel.setLayoutX(6.0);
		engineStaticPowerLabel.setLayoutY(159.0);
		engineDataPane.getChildren().add(engineStaticPowerLabel);
		
		TextField engineStaticPowerTextField = new TextField();
		engineStaticPowerTextField.setLayoutX(6.0);
		engineStaticPowerTextField.setLayoutY(180);
		engineStaticPowerTextField.setPrefWidth(340);
		engineStaticPowerTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineStaticPowerTextField);
		engineTurbopropStaticPowerTextFieldMap.put(indexOfEngineTab, engineStaticPowerTextField);
		
		ChoiceBox<String> engineStaticPowerChoiceBox = new ChoiceBox<String>();
		engineStaticPowerChoiceBox.setLayoutX(348.0);
		engineStaticPowerChoiceBox.setLayoutY(180);
		engineStaticPowerChoiceBox.setPrefWidth(47);
		engineStaticPowerChoiceBox.setPrefHeight(30);
		engineStaticPowerChoiceBox.setItems(powerUnitsList);
		engineDataPane.getChildren().add(engineStaticPowerChoiceBox);
		engineTurbopropStaticPowerUnitChoiceBoxMap.put(indexOfEngineTab, engineStaticPowerChoiceBox);
		
		Label engineDryMassLabel = new Label("Dry Mass:");
		engineDryMassLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineDryMassLabel.setLayoutX(6.0);
		engineDryMassLabel.setLayoutY(213.0);
		engineDataPane.getChildren().add(engineDryMassLabel);
		
		TextField engineDryMassTextField = new TextField();
		engineDryMassTextField.setLayoutX(6.0);
		engineDryMassTextField.setLayoutY(234);
		engineDryMassTextField.setPrefWidth(340);
		engineDryMassTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineDryMassTextField);
		engineTurbopropDryMassTextFieldMap.put(indexOfEngineTab, engineDryMassTextField);
		
		ChoiceBox<String> engineDryMassChoiceBox = new ChoiceBox<String>();
		engineDryMassChoiceBox.setLayoutX(348.0);
		engineDryMassChoiceBox.setLayoutY(234);
		engineDryMassChoiceBox.setPrefWidth(47);
		engineDryMassChoiceBox.setPrefHeight(30);
		engineDryMassChoiceBox.setItems(massUnitsList);
		engineDataPane.getChildren().add(engineDryMassChoiceBox);
		engineTurbopropDryMassUnitChoiceBoxMap.put(indexOfEngineTab, engineDryMassChoiceBox);
		
		Button engineDryMassCalculateButton = new Button("Calculate");
		engineDryMassCalculateButton.setLayoutX(397.0);
		engineDryMassCalculateButton.setLayoutY(234);
		engineDryMassCalculateButton.setPrefHeight(31);
		engineDryMassCalculateButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				calculateEngineDryMass(engineDryMassTextField, engineDryMassChoiceBox, indexOfEngineTab);
			}
		});
		engineDataPane.getChildren().add(engineDryMassCalculateButton);
		
		Label enginePropellerDiameterLabel = new Label("Propeller Diameter:");
		enginePropellerDiameterLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		enginePropellerDiameterLabel.setLayoutX(6.0);
		enginePropellerDiameterLabel.setLayoutY(268.0);
		engineDataPane.getChildren().add(enginePropellerDiameterLabel);
		
		TextField enginePropellerDiameterTextField = new TextField();
		enginePropellerDiameterTextField.setLayoutX(6.0);
		enginePropellerDiameterTextField.setLayoutY(289);
		enginePropellerDiameterTextField.setPrefWidth(340);
		enginePropellerDiameterTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(enginePropellerDiameterTextField);
		engineTurbopropPropellerDiameterTextFieldMap.put(indexOfEngineTab, enginePropellerDiameterTextField);
		
		ChoiceBox<String> enginePropellerDiameterChoiceBox = new ChoiceBox<String>();
		enginePropellerDiameterChoiceBox.setLayoutX(348.0);
		enginePropellerDiameterChoiceBox.setLayoutY(289);
		enginePropellerDiameterChoiceBox.setPrefWidth(47);
		enginePropellerDiameterChoiceBox.setPrefHeight(30);
		enginePropellerDiameterChoiceBox.setItems(lengthUnitsList);
		engineDataPane.getChildren().add(enginePropellerDiameterChoiceBox);
		engineTurbopropPropellerDiameterUnitChoiceBoxMap.put(indexOfEngineTab, enginePropellerDiameterChoiceBox);
		
		Label engineNumberOfBladesLabel = new Label("Number Of Blades:");
		engineNumberOfBladesLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineNumberOfBladesLabel.setLayoutX(6.0);
		engineNumberOfBladesLabel.setLayoutY(323.0);
		engineDataPane.getChildren().add(engineNumberOfBladesLabel);
		
		TextField engineNumberOfBladesTextField = new TextField();
		engineNumberOfBladesTextField.setLayoutX(6.0);
		engineNumberOfBladesTextField.setLayoutY(344);
		engineNumberOfBladesTextField.setPrefWidth(340);
		engineNumberOfBladesTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineNumberOfBladesTextField);
		engineTurbopropNumberOfBladesTextFieldMap.put(indexOfEngineTab, engineNumberOfBladesTextField);
		
		Label enginePropellerEfficiencyLabel = new Label("Propeller Efficiency:");
		enginePropellerEfficiencyLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		enginePropellerEfficiencyLabel.setLayoutX(6.0);
		enginePropellerEfficiencyLabel.setLayoutY(378.0);
		engineDataPane.getChildren().add(enginePropellerEfficiencyLabel);
		
		TextField enginePropellerEfficiencyTextField = new TextField();
		enginePropellerEfficiencyTextField.setLayoutX(6.0);
		enginePropellerEfficiencyTextField.setLayoutY(399);
		enginePropellerEfficiencyTextField.setPrefWidth(340);
		enginePropellerEfficiencyTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(enginePropellerEfficiencyTextField);
		engineTurbopropPropellerEfficiencyTextFieldMap.put(indexOfEngineTab, enginePropellerEfficiencyTextField);
		
		Label engineNumberOfCompressorStagesLabel = new Label("Number Of Compressor Stages:");
		engineNumberOfCompressorStagesLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineNumberOfCompressorStagesLabel.setLayoutX(6.0);
		engineNumberOfCompressorStagesLabel.setLayoutY(433.0);
		engineDataPane.getChildren().add(engineNumberOfCompressorStagesLabel);
		
		TextField engineNumberOfCompressorStagesTextField = new TextField();
		engineNumberOfCompressorStagesTextField.setLayoutX(6.0);
		engineNumberOfCompressorStagesTextField.setLayoutY(454);
		engineNumberOfCompressorStagesTextField.setPrefWidth(340);
		engineNumberOfCompressorStagesTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineNumberOfCompressorStagesTextField);
		engineTurbopropNumberOfCompressorStagesTextFieldMap.put(indexOfEngineTab, engineNumberOfCompressorStagesTextField);
		
		Label engineNumberOfShaftsLabel = new Label("Number Of Shafts:");
		engineNumberOfShaftsLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineNumberOfShaftsLabel.setLayoutX(6.0);
		engineNumberOfShaftsLabel.setLayoutY(487.0);
		engineDataPane.getChildren().add(engineNumberOfShaftsLabel);
		
		TextField engineNumberOfShaftsTextField = new TextField();
		engineNumberOfShaftsTextField.setLayoutX(6.0);
		engineNumberOfShaftsTextField.setLayoutY(508);
		engineNumberOfShaftsTextField.setPrefWidth(340);
		engineNumberOfShaftsTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineNumberOfShaftsTextField);
		engineTurbopropNumberOfShaftsTextFieldMap.put(indexOfEngineTab, engineNumberOfShaftsTextField);
		
		Label engineOverallPressureRatioLabel = new Label("Overall Pressure Ratio:");
		engineOverallPressureRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineOverallPressureRatioLabel.setLayoutX(6.0);
		engineOverallPressureRatioLabel.setLayoutY(542.0);
		engineDataPane.getChildren().add(engineOverallPressureRatioLabel);
		
		TextField engineOverallPressureRatioTextField = new TextField();
		engineOverallPressureRatioTextField.setLayoutX(6.0);
		engineOverallPressureRatioTextField.setLayoutY(563);
		engineOverallPressureRatioTextField.setPrefWidth(340);
		engineOverallPressureRatioTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineOverallPressureRatioTextField);
		engineTurbopropOverallPressureRatioTextFieldMap.put(indexOfEngineTab, engineOverallPressureRatioTextField);
		
		Map<EngineTypeEnum, Pane> enginePaneMap = new HashMap<>();
		enginePaneMap.put(EngineTypeEnum.TURBOPROP, engineDataPane);
		powerPlantPaneMap.put(indexOfEngineTab, enginePaneMap);
		powerPlantBorderPaneMap.get(indexOfEngineTab).setCenter(engineDataPane);
		
		System.gc();
		
	}
	
	@FXML
	private void showPistonDataRadioButton (int indexOfEngineTab) {
		
		Pane engineDataPane = new Pane();
		
		Label engineTypeLabel = new Label("Type:");
		engineTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineTypeLabel.setLayoutX(6.0);
		engineTypeLabel.setLayoutY(0.0);
		engineDataPane.getChildren().add(engineTypeLabel);
		
		ChoiceBox<String> engineTypeChoiceBox = new ChoiceBox<String>();
		engineTypeChoiceBox.setLayoutX(6.0);
		engineTypeChoiceBox.setLayoutY(21.0);
		engineTypeChoiceBox.setPrefWidth(340);
		engineTypeChoiceBox.setPrefHeight(31);
		engineTypeChoiceBox.setItems(engineTypeList);
		engineDataPane.getChildren().add(engineTypeChoiceBox);
		enginePistonTypeChoiceBoxMap.put(indexOfEngineTab, engineTypeChoiceBox); 
		
		Label engineDatabaseLabel = new Label("Database:");
		engineDatabaseLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineDatabaseLabel.setLayoutX(6.0);
		engineDatabaseLabel.setLayoutY(52.0);
		engineDataPane.getChildren().add(engineDatabaseLabel);
		
		TextField engineDatabasePathTextField = new TextField();
		engineDatabasePathTextField.setLayoutX(6.0);
		engineDatabasePathTextField.setLayoutY(73);
		engineDatabasePathTextField.setPrefWidth(340);
		engineDatabasePathTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineDatabasePathTextField);
		enginePistonDatabaseTextFieldMap.put(indexOfEngineTab, engineDatabasePathTextField);
		
		Button engineDatabasePathChooseButton = new Button("...");
		engineDatabasePathChooseButton.setLayoutX(350.0);
		engineDatabasePathChooseButton.setLayoutY(73);
		engineDatabasePathChooseButton.setPrefWidth(24);
		engineDatabasePathChooseButton.setPrefHeight(31);
		engineDatabasePathChooseButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chooseEngineDatabase(engineDatabasePathTextField);
			}
		});
		engineDataPane.getChildren().add(engineDatabasePathChooseButton);
		
		Label engineLengthLabel = new Label("Length:");
		engineLengthLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineLengthLabel.setLayoutX(6.0);
		engineLengthLabel.setLayoutY(104.0);
		engineDataPane.getChildren().add(engineLengthLabel);
		
		TextField engineLengthTextField = new TextField();
		engineLengthTextField.setLayoutX(6.0);
		engineLengthTextField.setLayoutY(125);
		engineLengthTextField.setPrefWidth(340);
		engineLengthTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineLengthTextField);
		enginePistonLengthTextFieldMap.put(indexOfEngineTab, engineLengthTextField);
		
		ChoiceBox<String> engineLengthChoiceBox = new ChoiceBox<String>();
		engineLengthChoiceBox.setLayoutX(348.0);
		engineLengthChoiceBox.setLayoutY(125);
		engineLengthChoiceBox.setPrefWidth(47);
		engineLengthChoiceBox.setPrefHeight(30);
		engineLengthChoiceBox.setItems(lengthUnitsList);
		engineDataPane.getChildren().add(engineLengthChoiceBox);
		enginePistonLengthUnitChoiceBoxMap.put(indexOfEngineTab, engineLengthChoiceBox);
		
		Label engineStaticPowerLabel = new Label("Static Power:");
		engineStaticPowerLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineStaticPowerLabel.setLayoutX(6.0);
		engineStaticPowerLabel.setLayoutY(159.0);
		engineDataPane.getChildren().add(engineStaticPowerLabel);
		
		TextField engineStaticPowerTextField = new TextField();
		engineStaticPowerTextField.setLayoutX(6.0);
		engineStaticPowerTextField.setLayoutY(180);
		engineStaticPowerTextField.setPrefWidth(340);
		engineStaticPowerTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineStaticPowerTextField);
		enginePistonStaticPowerTextFieldMap.put(indexOfEngineTab, engineStaticPowerTextField);
		
		ChoiceBox<String> engineStaticPowerChoiceBox = new ChoiceBox<String>();
		engineStaticPowerChoiceBox.setLayoutX(348.0);
		engineStaticPowerChoiceBox.setLayoutY(180);
		engineStaticPowerChoiceBox.setPrefWidth(47);
		engineStaticPowerChoiceBox.setPrefHeight(30);
		engineStaticPowerChoiceBox.setItems(powerUnitsList);
		engineDataPane.getChildren().add(engineStaticPowerChoiceBox);
		enginePistonStaticPowerUnitChoiceBoxMap.put(indexOfEngineTab, engineStaticPowerChoiceBox);
		
		Label engineDryMassLabel = new Label("Dry Mass:");
		engineDryMassLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineDryMassLabel.setLayoutX(6.0);
		engineDryMassLabel.setLayoutY(213.0);
		engineDataPane.getChildren().add(engineDryMassLabel);
		
		TextField engineDryMassTextField = new TextField();
		engineDryMassTextField.setLayoutX(6.0);
		engineDryMassTextField.setLayoutY(234);
		engineDryMassTextField.setPrefWidth(340);
		engineDryMassTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineDryMassTextField);
		enginePistonDryMassTextFieldMap.put(indexOfEngineTab, engineDryMassTextField);
		
		ChoiceBox<String> engineDryMassChoiceBox = new ChoiceBox<String>();
		engineDryMassChoiceBox.setLayoutX(348.0);
		engineDryMassChoiceBox.setLayoutY(234);
		engineDryMassChoiceBox.setPrefWidth(47);
		engineDryMassChoiceBox.setPrefHeight(30);
		engineDryMassChoiceBox.setItems(massUnitsList);
		engineDataPane.getChildren().add(engineDryMassChoiceBox);
		enginePistonDryMassUnitChoiceBoxMap.put(indexOfEngineTab, engineDryMassChoiceBox);
		
		Button engineDryMassCalculateButton = new Button("Calculate");
		engineDryMassCalculateButton.setLayoutX(397.0);
		engineDryMassCalculateButton.setLayoutY(234);
		engineDryMassCalculateButton.setPrefHeight(31);
		engineDryMassCalculateButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				calculateEngineDryMass(engineDryMassTextField, engineDryMassChoiceBox, indexOfEngineTab);
			}
		});
		engineDataPane.getChildren().add(engineDryMassCalculateButton);
		
		Label enginePropellerDiameterLabel = new Label("Propeller Diameter:");
		enginePropellerDiameterLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		enginePropellerDiameterLabel.setLayoutX(6.0);
		enginePropellerDiameterLabel.setLayoutY(268.0);
		engineDataPane.getChildren().add(enginePropellerDiameterLabel);
		
		TextField enginePropellerDiameterTextField = new TextField();
		enginePropellerDiameterTextField.setLayoutX(6.0);
		enginePropellerDiameterTextField.setLayoutY(289);
		enginePropellerDiameterTextField.setPrefWidth(340);
		enginePropellerDiameterTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(enginePropellerDiameterTextField);
		enginePistonPropellerDiameterTextFieldMap.put(indexOfEngineTab, enginePropellerDiameterTextField);
		
		ChoiceBox<String> enginePropellerDiameterChoiceBox = new ChoiceBox<String>();
		enginePropellerDiameterChoiceBox.setLayoutX(348.0);
		enginePropellerDiameterChoiceBox.setLayoutY(289);
		enginePropellerDiameterChoiceBox.setPrefWidth(47);
		enginePropellerDiameterChoiceBox.setPrefHeight(30);
		enginePropellerDiameterChoiceBox.setItems(lengthUnitsList);
		engineDataPane.getChildren().add(enginePropellerDiameterChoiceBox);
		enginePistonPropellerDiameterUnitChoiceBoxMap.put(indexOfEngineTab, enginePropellerDiameterChoiceBox);
		
		Label engineNumberOfBladesLabel = new Label("Number Of Blades:");
		engineNumberOfBladesLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineNumberOfBladesLabel.setLayoutX(6.0);
		engineNumberOfBladesLabel.setLayoutY(323.0);
		engineDataPane.getChildren().add(engineNumberOfBladesLabel);
		
		TextField engineNumberOfBladesTextField = new TextField();
		engineNumberOfBladesTextField.setLayoutX(6.0);
		engineNumberOfBladesTextField.setLayoutY(344);
		engineNumberOfBladesTextField.setPrefWidth(340);
		engineNumberOfBladesTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineNumberOfBladesTextField);
		enginePistonNumberOfBladesTextFieldMap.put(indexOfEngineTab, engineNumberOfBladesTextField);
		
		Label enginePropellerEfficiencyLabel = new Label("Propeller Efficiency:");
		enginePropellerEfficiencyLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		enginePropellerEfficiencyLabel.setLayoutX(6.0);
		enginePropellerEfficiencyLabel.setLayoutY(378.0);
		engineDataPane.getChildren().add(enginePropellerEfficiencyLabel);
		
		TextField enginePropellerEfficiencyTextField = new TextField();
		enginePropellerEfficiencyTextField.setLayoutX(6.0);
		enginePropellerEfficiencyTextField.setLayoutY(399);
		enginePropellerEfficiencyTextField.setPrefWidth(340);
		enginePropellerEfficiencyTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(enginePropellerEfficiencyTextField);
		enginePistonPropellerEfficiencyTextFieldMap.put(indexOfEngineTab, enginePropellerEfficiencyTextField);
		
		Map<EngineTypeEnum, Pane> enginePaneMap = new HashMap<>();
		enginePaneMap.put(EngineTypeEnum.PISTON, engineDataPane);
		powerPlantPaneMap.put(indexOfEngineTab, enginePaneMap);
		powerPlantBorderPaneMap.get(indexOfEngineTab).setCenter(engineDataPane);
		
		System.gc();
		
	}
	
	@FXML
	private void setEstimateNacelleGeometryAction (Button estimateButton, Tab currentTab) {
		
		estimateButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				int tabIndex = tabPaneNacelles.getTabs().indexOf(currentTab);	
				
				NacelleCreator currentNacelle = Main.getTheAircraft().getNacelles().getNacellesList().get(tabIndex);

				currentNacelle.estimateDimensions(currentNacelle.getTheEngine());

				textFieldNacelleLengthList.get(tabIndex).clear();
				textFieldNacelleLengthList.get(tabIndex).setText(
						String.valueOf(currentNacelle.getLength().getEstimatedValue()));

				if(currentNacelle.getLength().getUnit().toString().equalsIgnoreCase("m"))
					choiceBoxNacelleLengthUnitList.get(tabIndex).getSelectionModel().select(0);
				else if(currentNacelle.getLength().getUnit().toString().equalsIgnoreCase("ft"))
					choiceBoxNacelleLengthUnitList.get(tabIndex).getSelectionModel().select(1);

				textFieldNacelleMaximumDiameterList.get(tabIndex).clear();
				textFieldNacelleMaximumDiameterList.get(tabIndex).setText(
						String.valueOf(currentNacelle.getDiameterMax().getEstimatedValue()));

				if(currentNacelle.getDiameterMax().getUnit().toString().equalsIgnoreCase("m"))
					choiceBoxNacelleMaximumDiameterUnitList.get(tabIndex).getSelectionModel().select(0);
				else if(currentNacelle.getDiameterMax().getUnit().toString().equalsIgnoreCase("ft"))
					choiceBoxNacelleMaximumDiameterUnitList.get(tabIndex).getSelectionModel().select(1);

			}
		});

		System.gc();
		
	}

	@FXML
	private void calculateEngineDryMass (TextField dryMassTextField, ChoiceBox<String> dryMassChoiceBox, int indexOfEngineTab) {

		if (Main.getTheAircraft() != null) {
			if (Main.getTheAircraft().getPowerPlant() != null) {
				if (Main.getTheAircraft().getPowerPlant().getEngineList().get(indexOfEngineTab) != null) {

					Engine currentEngine = Main.getTheAircraft().getPowerPlant().getEngineList().get(indexOfEngineTab);

					if (currentEngine.getEngineType().equals(EngineTypeEnum.TURBOFAN)
							|| currentEngine.getEngineType().equals(EngineTypeEnum.TURBOJET)) {

						if (currentEngine.getT0() != null) {
							if (currentEngine.getT0().doubleValue(NonSI.POUND_FORCE) < 10000) {
								currentEngine.setDryMassPublicDomain(
										Amount.valueOf(
												Amount.valueOf(
														Math.pow(
																0.4054*currentEngine.getT0().doubleValue(NonSI.POUND_FORCE),
																0.9255
																),
														NonSI.POUND_FORCE)
												.to(SI.NEWTON)
												.divide(AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND))
												.getEstimatedValue(),
												SI.KILOGRAM
												)
										);
								if (currentEngine.getDryMassPublicDomain() != null) {
									dryMassTextField.setText(
											String.valueOf(
													currentEngine.getDryMassPublicDomain().getEstimatedValue()
													)
											);

									if (currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
										dryMassChoiceBox.getSelectionModel().select(0);
									else if (currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
										dryMassChoiceBox.getSelectionModel().select(1);

								}
								else
									dryMassTextField.setText("INVALID VALUE");
							}
							else {
								currentEngine.setDryMassPublicDomain(
										Amount.valueOf(
												Amount.valueOf(
														Math.pow(
																0.616*currentEngine.getT0().doubleValue(NonSI.POUND_FORCE),
																0.886
																),
														NonSI.POUND_FORCE)
												.to(SI.NEWTON)
												.divide(AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND))
												.getEstimatedValue(),
												SI.KILOGRAM
												)
										);
								if(currentEngine.getDryMassPublicDomain() != null) {
									dryMassTextField.setText(
											String.valueOf(
													currentEngine.getDryMassPublicDomain().getEstimatedValue()
													)
											);

									if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
										dryMassChoiceBox.getSelectionModel().select(0);
									else if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
										dryMassChoiceBox.getSelectionModel().select(1);

								}
								else
									dryMassTextField.setText("INVALID VALUE");
							}
						}
						else {
							dryMassTextField.setText("INVALID VALUE. T0 IS SET TO NULL");
						}
					}
					else if (currentEngine.getEngineType().equals(EngineTypeEnum.TURBOPROP)
							|| currentEngine.getEngineType().equals(EngineTypeEnum.PISTON)) {

						if (currentEngine.getP0() != null) {
							if (currentEngine.getP0().doubleValue(NonSI.HORSEPOWER)*2.8 < 10000) {
								currentEngine.setDryMassPublicDomain(
										Amount.valueOf(
												Amount.valueOf(
														Math.pow(
																0.4054*currentEngine.getP0().doubleValue(NonSI.HORSEPOWER)*2.8,
																0.9255
																),
														NonSI.POUND_FORCE)
												.to(SI.NEWTON)
												.divide(AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND))
												.getEstimatedValue(),
												SI.KILOGRAM
												)
										);
								if (currentEngine.getDryMassPublicDomain() != null) {
									dryMassTextField.setText(
											String.valueOf(
													currentEngine.getDryMassPublicDomain().getEstimatedValue()
													)
											);

									if (currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
										dryMassChoiceBox.getSelectionModel().select(0);
									else if (currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
										dryMassChoiceBox.getSelectionModel().select(1);

								}
								else
									dryMassTextField.setText("INVALID VALUE");
							}
							else {
								currentEngine.setDryMassPublicDomain(
										Amount.valueOf(
												Amount.valueOf(
														Math.pow(
																0.616*currentEngine.getP0().doubleValue(NonSI.HORSEPOWER)*2.8,
																0.886
																),
														NonSI.POUND_FORCE)
												.to(SI.NEWTON)
												.divide(AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND))
												.getEstimatedValue(),
												SI.KILOGRAM
												)
										);
								if(currentEngine.getDryMassPublicDomain() != null) {
									dryMassTextField.setText(
											String.valueOf(
													currentEngine.getDryMassPublicDomain().getEstimatedValue()
													)
											);

									if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
										dryMassChoiceBox.getSelectionModel().select(0);
									else if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
										dryMassChoiceBox.getSelectionModel().select(1);

								}
								else
									dryMassTextField.setText("INVALID VALUE");
							}
						}
						else {
							dryMassTextField.setText("INVALID VALUE. P0 IS SET TO NULL");
						}
					}
				}
				else {
					dryMassTextField.setText("INVALID VALUE. THE ENGINE IS NULL");
				}
			}
			else {
				dryMassTextField.setText("INVALID VALUE. THE POWER PLANT IS NULL");
			}
		}
		else {
			dryMassTextField.setText("INVALID VALUE. THE AIRCRAFT IS NULL");
		}
		
		System.gc();
		
	}
	
	@FXML
	private void showAirfoilData(String airfoilFileName, Button detailButton, ComponentEnum type) throws IOException {
		
		Tab airfoilTab = new Tab("Airfoil: " + airfoilFileName);
		
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("inputmanager/AirfoilInputManager.fxml"));
		BorderPane contentPane = loader.load();
		AirfoilInputManagerController airfoilInputManagerController = loader.getController();
		
		switch (type) {
		case WING:
			String wingAirfoilPathFromList = Main.getTheAircraft().getWing().getAirfoilPathList()
			.stream()
			.filter(
					airfoilPath -> Paths.get(airfoilPath).getFileName().toString().equals(airfoilFileName))
			.findFirst()
			.get();

			int airfoilWingIndex = Main.getTheAircraft().getWing().getAirfoilPathList().indexOf(wingAirfoilPathFromList);

			airfoilInputManagerController.loadAirfoilData(
					Main.getTheAircraft().getWing().getAirfoilList().get(airfoilWingIndex).getAirfoilCreator()
					);
			airfoilInputManagerController.createAirfoilView();
			airfoilInputManagerController.createClCurve();
			airfoilInputManagerController.createCdCurve();
			airfoilInputManagerController.createCmCurve();

			airfoilTab.setContent(contentPane);
			removeAirfoilDetailsButtonFromMapOnTabClose(airfoilTab, type);

			tabPaneWingViewAndAirfoils.getTabs().add(airfoilTab);			
			break;

		case HORIZONTAL_TAIL:
			String hTailAirfoilPathFromList = Main.getTheAircraft().getHTail().getAirfoilPathList()
			.stream()
			.filter(
					airfoilPath -> Paths.get(airfoilPath).getFileName().toString().equals(airfoilFileName))
			.findFirst()
			.get();

			int airfoilHTailIndex = Main.getTheAircraft().getHTail().getAirfoilPathList().indexOf(hTailAirfoilPathFromList);

			airfoilInputManagerController.loadAirfoilData(
					Main.getTheAircraft().getHTail().getAirfoilList().get(airfoilHTailIndex).getAirfoilCreator()
					);
			airfoilInputManagerController.createAirfoilView();
			airfoilInputManagerController.createClCurve();
			airfoilInputManagerController.createCdCurve();
			airfoilInputManagerController.createCmCurve();

			airfoilTab.setContent(contentPane);
			removeAirfoilDetailsButtonFromMapOnTabClose(airfoilTab, type);

			tabPaneHTailViewAndAirfoils.getTabs().add(airfoilTab);
			break;
			
		case VERTICAL_TAIL:
			String vTailAirfoilPathFromList = Main.getTheAircraft().getVTail().getAirfoilPathList()
			.stream()
			.filter(
					airfoilPath -> Paths.get(airfoilPath).getFileName().toString().equals(airfoilFileName))
			.findFirst()
			.get();

			int airfoilVTailIndex = Main.getTheAircraft().getVTail().getAirfoilPathList().indexOf(vTailAirfoilPathFromList);

			airfoilInputManagerController.loadAirfoilData(
					Main.getTheAircraft().getVTail().getAirfoilList().get(airfoilVTailIndex).getAirfoilCreator()
					);
			airfoilInputManagerController.createAirfoilView();
			airfoilInputManagerController.createClCurve();
			airfoilInputManagerController.createCdCurve();
			airfoilInputManagerController.createCmCurve();

			airfoilTab.setContent(contentPane);
			removeAirfoilDetailsButtonFromMapOnTabClose(airfoilTab, type);

			tabPaneVTailViewAndAirfoils.getTabs().add(airfoilTab);
			break;
			
		case CANARD:
			String canardAirfoilPathFromList = Main.getTheAircraft().getCanard().getAirfoilPathList()
			.stream()
			.filter(
					airfoilPath -> Paths.get(airfoilPath).getFileName().toString().equals(airfoilFileName))
			.findFirst()
			.get();

			int airfoilCanardIndex = Main.getTheAircraft().getCanard().getAirfoilPathList().indexOf(canardAirfoilPathFromList);

			airfoilInputManagerController.loadAirfoilData(
					Main.getTheAircraft().getCanard().getAirfoilList().get(airfoilCanardIndex).getAirfoilCreator()
					);
			airfoilInputManagerController.createAirfoilView();
			airfoilInputManagerController.createClCurve();
			airfoilInputManagerController.createCdCurve();
			airfoilInputManagerController.createCmCurve();

			airfoilTab.setContent(contentPane);
			removeAirfoilDetailsButtonFromMapOnTabClose(airfoilTab, type);

			tabPaneCanardViewAndAirfoils.getTabs().add(airfoilTab);
			break;
			
		default:
			break;
		}
		
		System.gc();
		
	}
	
	@FXML
	private void chooseAirfoilFile(TextField panelInnerSectionAirfoilPathTextField) {
		
		aircraftFileChooser = new FileChooser();
		aircraftFileChooser.setTitle("Open File");
		aircraftFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() + File.separator 
						+ "Template_Aircraft" + File.separator 
						+ "lifting_surfaces" + File.separator
						+ "airfoils"));
		File file = aircraftFileChooser.showOpenDialog(null);
		if (file != null) {
			panelInnerSectionAirfoilPathTextField.setText(file.getAbsolutePath());
		}
		
		System.gc();
		
	}
	
	@FXML
	private void chooseEngineDatabase(TextField engineDatabasePathTextField) {
		
		engineDatabaseFileChooser = new FileChooser();
		engineDatabaseFileChooser.setTitle("Open File");
		engineDatabaseFileChooser.setInitialDirectory(
				new File(Main.getDatabaseDirectoryPath())
				);
		File file = engineDatabaseFileChooser.showOpenDialog(null);
		if (file != null) {
			engineDatabasePathTextField.setText(Paths.get(file.getAbsolutePath()).getFileName().toString());
		}
		
		System.gc();
		
	}
	
	@FXML
	private void chooseAircraftFile() throws IOException {

		aircraftFileChooser = new FileChooser();
		aircraftFileChooser.setTitle("Open File");
		aircraftFileChooser.setInitialDirectory(new File(Main.getInputDirectoryPath() + File.separator + "Template_Aircraft"));
		File file = aircraftFileChooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			textFieldAircraftInputFile.setText(file.getAbsolutePath());
			Main.setInputFileAbsolutePath(file.getAbsolutePath());
		}
		
		System.gc();
		
	}
	
	@FXML
	private void chooseAircraftCabinConfigurationFile() throws IOException {

		cabinConfigurationFileChooser = new FileChooser();
		cabinConfigurationFileChooser.setTitle("Open File");
		cabinConfigurationFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() 
						+ File.separator 
						+ "Template_Aircraft" 
						+ File.separator 
						+ "cabin_configurations"
						)
				);
		File file = cabinConfigurationFileChooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			textFieldAircraftCabinConfigurationFile.setText(file.getAbsolutePath());
		}
		
		System.gc();
		
	}
	
	@FXML
	private void chooseAircraftFuslegeFile() throws IOException {

		fuselageFileChooser = new FileChooser();
		fuselageFileChooser.setTitle("Open File");
		fuselageFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() 
						+ File.separator 
						+ "Template_Aircraft" 
						+ File.separator 
						+ "fuselages"
						)
				);
		File file = fuselageFileChooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			textFieldAircraftFuselageFile.setText(file.getAbsolutePath());
		}
		
		System.gc();
		
	}
	
	@FXML
	private void chooseAircraftWingFile() throws IOException {

		wingFileChooser = new FileChooser();
		wingFileChooser.setTitle("Open File");
		wingFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() 
						+ File.separator 
						+ "Template_Aircraft" 
						+ File.separator 
						+ "lifting_surfaces"
						)
				);
		File file = wingFileChooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			textFieldAircraftWingFile.setText(file.getAbsolutePath());
		}
		
		System.gc();
		
	}
	
	@FXML
	private void chooseAircraftHTailFile() throws IOException {

		hTailFileChooser = new FileChooser();
		hTailFileChooser.setTitle("Open File");
		hTailFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() 
						+ File.separator 
						+ "Template_Aircraft" 
						+ File.separator 
						+ "lifting_surfaces"
						)
				);
		File file = hTailFileChooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			textFieldAircraftHTailFile.setText(file.getAbsolutePath());
		}
		
		System.gc();
		
	}
	
	@FXML
	private void chooseAircraftVTailFile() throws IOException {

		vTailFileChooser = new FileChooser();
		vTailFileChooser.setTitle("Open File");
		vTailFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() 
						+ File.separator 
						+ "Template_Aircraft" 
						+ File.separator 
						+ "lifting_surfaces"
						)
				);
		File file = vTailFileChooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			textFieldAircraftVTailFile.setText(file.getAbsolutePath());
		}
		
		System.gc();
		
	}
	
	@FXML
	private void chooseAircraftCanardFile() throws IOException {

		canardFileChooser = new FileChooser();
		canardFileChooser.setTitle("Open File");
		canardFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() 
						+ File.separator 
						+ "Template_Aircraft" 
						+ File.separator 
						+ "lifting_surfaces"
						)
				);
		File file = canardFileChooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			textFieldAircraftCanardFile.setText(file.getAbsolutePath());
		}
		
		System.gc();
		
	}
	
	private void chooseAircraftEngineFile(int indexOfEngine) throws IOException {

		engineFileChooser = new FileChooser();
		engineFileChooser.setTitle("Open File");
		engineFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() 
						+ File.separator 
						+ "Template_Aircraft" 
						+ File.separator 
						+ "engines"
						)
				);
		File file = engineFileChooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			textFieldsAircraftEngineFileList.get(indexOfEngine).setText(file.getAbsolutePath());
		}
		
		System.gc();
		
	}
	
	private void chooseAircraftNacelleFile(int indexOfNacelle) throws IOException {

		nacelleFileChooser = new FileChooser();
		nacelleFileChooser.setTitle("Open File");
		nacelleFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() 
						+ File.separator 
						+ "Template_Aircraft" 
						+ File.separator 
						+ "nacelles"
						)
				);
		File file = nacelleFileChooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			textFieldsAircraftNacelleFileList.get(indexOfNacelle).setText(file.getAbsolutePath());
		}
		
		System.gc();
		
	}
	
	@FXML
	private void chooseAircraftLandingGearsFile() throws IOException {

		landingGearsFileChooser = new FileChooser();
		landingGearsFileChooser.setTitle("Open File");
		landingGearsFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() 
						+ File.separator 
						+ "Template_Aircraft" 
						+ File.separator 
						+ "landing_gears"
						)
				);
		File file = landingGearsFileChooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			textFieldAircraftLandingGearsFile.setText(file.getAbsolutePath());
		}
		
		System.gc();
		
	}
	
	@FXML
	private void chooseAircraftSystemsFile() throws IOException {

		systemsFileChooser = new FileChooser();
		systemsFileChooser.setTitle("Open File");
		systemsFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() 
						+ File.separator 
						+ "Template_Aircraft" 
						+ File.separator 
						+ "systems"
						)
				);
		File file = systemsFileChooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			textFieldAircraftSystemsFile.setText(file.getAbsolutePath());
		}
		
		System.gc();
		
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
		inputDataWarning.initStyle(StageStyle.UNDECORATED);
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
        
        System.gc();
        
	}
	
	private void newAircraftImplementation() {
		
		//..................................................................................
		// PRELIMINARY OPERATIONS
		Main.getStatusBar().textProperty().unbind();
		Main.getProgressBar().progressProperty().unbind();
		Main.getTaskPercentage().textProperty().unbind();
		
		Main.getStatusBar().setText("Ready!");
		Main.getProgressBar().setProgress(0.0);
		Main.getTaskPercentage().setText("");

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
		textFieldAircraftEngineFile1.clear();
		textFieldAircraftEngineX1.clear();
		textFieldAircraftEngineY1.clear();
		textFieldAircraftEngineZ1.clear();
		textFieldAircraftEngineTilt1.clear();
		engineX1UnitChoiceBox.getSelectionModel().clearSelection();
		engineY1UnitChoiceBox.getSelectionModel().clearSelection();
		engineZ1UnitChoiceBox.getSelectionModel().clearSelection();
		engineTilt1AngleUnitChoiceBox.getSelectionModel().clearSelection();
		engineMountingPositionTypeChoiceBox1.getSelectionModel().clearSelection();
		textFieldsAircraftEngineFileList.stream().forEach(t -> t.clear());
		textFieldAircraftEngineXList.stream().forEach(t -> t.clear());
		textFieldAircraftEngineYList.stream().forEach(t -> t.clear());
		textFieldAircraftEngineZList.stream().forEach(t -> t.clear());
		choiceBoxesAircraftEnginePositonList.stream().forEach(ep -> ep.getSelectionModel().clearSelection());
		textFieldAircraftEngineTiltList.stream().forEach(t -> t.clear());
		choiceBoxAircraftEngineXUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		choiceBoxAircraftEngineYUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		choiceBoxAircraftEngineZUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		choiceBoxAircraftEngineTiltUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		tabPaneAircraftEngines.getTabs().remove(1, tabPaneAircraftEngines.getTabs().size());
		
		// Nacelle
		textFieldAircraftNacelleFile1.clear();
		textFieldAircraftNacelleX1.clear();
		textFieldAircraftNacelleY1.clear();
		textFieldAircraftNacelleZ1.clear();
		nacelleMountingPositionTypeChoiceBox1.getSelectionModel().clearSelection();
		nacelleX1UnitChoiceBox.getSelectionModel().clearSelection();
		nacelleY1UnitChoiceBox.getSelectionModel().clearSelection();
		nacelleZ1UnitChoiceBox.getSelectionModel().clearSelection();
		textFieldsAircraftNacelleFileList.stream().forEach(t -> t.clear());
		textFieldAircraftNacelleXList.stream().forEach(t -> t.clear());
		textFieldAircraftNacelleYList.stream().forEach(t -> t.clear());
		textFieldAircraftNacelleZList.stream().forEach(t -> t.clear());
		choiceBoxesAircraftNacellePositonList.stream().forEach(ep -> ep.getSelectionModel().clearSelection());
		choiceBoxAircraftNacelleXUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		choiceBoxAircraftNacelleYUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		choiceBoxAircraftNacelleZUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		tabPaneAircraftNacelles.getTabs().remove(1, tabPaneAircraftNacelles.getTabs().size());
		
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
			fuselageSpoiler1DeltaMaxUnitChoiceBox.getSelectionModel().clearSelection();
			fuselageSpoiler1DeltaMinUnitChoiceBox.getSelectionModel().clearSelection();	
			textFieldFuselageInnerSpanwisePositionSpoilerList.stream().forEach(t -> t.clear());
			textFieldFuselageOuterSpanwisePositionSpoilerList.stream().forEach(t -> t.clear());
			textFieldFuselageInnerChordwisePositionSpoilerList.stream().forEach(t -> t.clear());
			textFieldFuselageOuterChordwisePositionSpoilerList.stream().forEach(t -> t.clear());
			textFieldFuselageMinimumDeflectionAngleSpoilerList.stream().forEach(t -> t.clear());
			textFieldFuselageMaximumDeflectionAngleSpoilerList.stream().forEach(t -> t.clear());
			tabPaneFuselageSpoilers.getTabs().remove(1, tabPaneFuselageSpoilers.getTabs().size());
		}

		// 3 View and TextArea
		textAreaFuselageConsoleOutput.clear();
		fuselageTopViewPane.getChildren().clear();
		fuselageSideViewPane.getChildren().clear();
		fuselageFrontViewPane.getChildren().clear();

		//..................................................................................
		// CABIN CONFIGURATION
		textFieldActualPassengersNumber.clear();
		textFieldMaximumPassengersNumber.clear();
		textFieldFlightCrewNumber.clear();
		textFieldClassesNumber.clear();
		cabinConfigurationClassesTypeChoiceBox1.getSelectionModel().clearSelection();
		cabinConfigurationClassesTypeChoiceBox2.getSelectionModel().clearSelection();
		cabinConfigurationClassesTypeChoiceBox3.getSelectionModel().clearSelection();
		textFieldAislesNumber.clear();
		textFieldXCoordinateFirstRow.clear();
		textFieldMissingSeatRow1.clear();
		textFieldMissingSeatRow2.clear();
		textFieldMissingSeatRow3.clear();
		textFieldNumberOfBrakesEconomy.clear();
		textFieldNumberOfBrakesBusiness.clear();
		textFieldNumberOfBrakesFirst.clear();
		textFieldNumberOfRowsEconomy.clear();
		textFieldNumberOfRowsBusiness.clear();
		textFieldNumberOfRowsFirst.clear();
		textFieldNumberOfColumnsEconomy.clear();
		textFieldNumberOfColumnsBusiness.clear();
		textFieldNumberOfColumnsFirst.clear();
		textFieldSeatsPitchEconomy.clear();
		textFieldSeatsPitchBusiness.clear();
		textFieldSeatsPitchFirst.clear();
		textFieldSeatsWidthEconomy.clear();
		textFieldSeatsWidthBusiness.clear();
		textFieldSeatsWidthFirst.clear();
		textFieldDistanceFromWallEconomy.clear();
		textFieldDistanceFromWallBusiness.clear();
		textFieldDistanceFromWallFirst.clear();
		textFieldMassFurnishingsAndEquipment.clear();
		cabinConfigurationXCoordinateFirstRowUnitChoiceBox.getSelectionModel().clearSelection();
		cabinConfigurationSeatsPitchEconomyUnitChoiceBox.getSelectionModel().clearSelection();
		cabinConfigurationSeatsPitchBusinessUnitChoiceBox.getSelectionModel().clearSelection();
		cabinConfigurationSeatsPitchFirstUnitChoiceBox.getSelectionModel().clearSelection();
		cabinConfigurationSeatsWidthEconomyUnitChoiceBox.getSelectionModel().clearSelection();
		cabinConfigurationSeatsWidthBusinessUnitChoiceBox.getSelectionModel().clearSelection();
		cabinConfigurationSeatsWidthFirstUnitChoiceBox.getSelectionModel().clearSelection();
		cabinConfigurationDistanceFromWallEconomyUnitChoiceBox.getSelectionModel().clearSelection();
		cabinConfigurationDistanceFromWallBusinessUnitChoiceBox.getSelectionModel().clearSelection();
		cabinConfigurationDistanceFromWallFirstUnitChoiceBox.getSelectionModel().clearSelection();
		cabinConfigurationMassFurnishingsAndEquipmentUnitChoiceBox.getSelectionModel().clearSelection();
		
		// 3 View and TextArea
		textAreaCabinConfigurationConsoleOutput.clear();
		cabinConfigurationSeatMapPane.getChildren().clear();
		
		//..................................................................................
		// WING
		wingAdjustCriterionChoiceBox.getSelectionModel().clearSelection();
		equivalentWingCheckBox.setSelected(false);
		textFieldWingMainSparAdimensionalPosition.clear();
		textFieldWingSecondarySparAdimensionalPosition.clear();
		textFieldWingCompositeMassCorrectionFactor.clear();
		textFieldWingRoughness.clear();
		textFieldWingWingletHeight.clear();
		textFieldEquivalentWingArea.clear();
		textFieldEquivalentWingAspectRatio.clear();
		textFieldEquivalentWingKinkPosition.clear();
		textFieldEquivalentWingSweepLeadingEdge.clear();
		textFieldEquivalentWingTwistAtTip.clear();
		textFieldEquivalentWingDihedral.clear();
		textFieldEquivalentWingTaperRatio.clear();
		textFieldEquivalentWingRootXOffsetLE.clear();
		textFieldEquivalentWingRootXOffsetTE.clear();
		textFieldEquivalentWingAirfoilRootPath.clear();
		textFieldEquivalentWingAirfoilKinkPath.clear();
		textFieldEquivalentWingAirfoilTipPath.clear();
		wingRoughnessUnitChoiceBox.getSelectionModel().clearSelection();
		wingWingletHeightUnitChoiceBox.getSelectionModel().clearSelection();
		equivalentWingAreaUnitChoiceBox.getSelectionModel().clearSelection();
		equivalentWingSweepLEUnitChoiceBox.getSelectionModel().clearSelection();
		equivalentWingTwistAtTipUnitChoiceBox.getSelectionModel().clearSelection();
		equivalentWingDihedralUnitChoiceBox.getSelectionModel().clearSelection();
		wingMinimumDeflectionAngleAileronLeftUnitChoiceBox.getSelectionModel().clearSelection();
		wingMaximumDeflectionAngleAileronLeftUnitChoiceBox.getSelectionModel().clearSelection();
		wingMinimumDeflectionAngleAileronRigthUnitChoiceBox.getSelectionModel().clearSelection();
		wingMaximumDeflectionAngleAileronRightUnitChoiceBox.getSelectionModel().clearSelection();
		
		// View and TextArea
		textAreaWingConsoleOutput.clear();
		wingPlanformPane.getChildren().clear();
		equivalentWingPane.getChildren().clear();
		for (int i=2; i < tabPaneWingViewAndAirfoils.getTabs().size(); i++)
			tabPaneWingViewAndAirfoils.getTabs().remove(i);
		
		// panels
		textFieldWingSpanPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldWingSpanPanelList.size() > 1)
			textFieldWingSpanPanelList.subList(1, textFieldWingSpanPanelList.size()).clear();
		textFieldWingSweepLEPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldWingSweepLEPanelList.size() > 1)
			textFieldWingSweepLEPanelList.subList(1, textFieldWingSweepLEPanelList.size()).clear();
		textFieldWingDihedralPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldWingDihedralPanelList.size() > 1)
			textFieldWingDihedralPanelList.subList(1, textFieldWingDihedralPanelList.size()).clear();
		textFieldWingInnerChordPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldWingInnerChordPanelList.size() > 1)
			textFieldWingInnerChordPanelList.subList(1, textFieldWingInnerChordPanelList.size()).clear();
		textFieldWingInnerTwistPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldWingInnerTwistPanelList.size() > 1)
			textFieldWingInnerTwistPanelList.subList(1, textFieldWingInnerTwistPanelList.size()).clear();
		textFieldWingInnerAirfoilPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldWingInnerAirfoilPanelList.size() > 1)
			textFieldWingInnerAirfoilPanelList.subList(1, textFieldWingInnerAirfoilPanelList.size()).clear();
		textFieldWingOuterChordPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldWingOuterChordPanelList.size() > 1)
			textFieldWingOuterChordPanelList.subList(1, textFieldWingOuterChordPanelList.size()).clear();
		textFieldWingOuterTwistPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldWingOuterTwistPanelList.size() > 1)
			textFieldWingOuterTwistPanelList.subList(1, textFieldWingOuterTwistPanelList.size()).clear();
		textFieldWingOuterAirfoilPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldWingOuterAirfoilPanelList.size() > 1)
			textFieldWingOuterAirfoilPanelList.subList(1, textFieldWingOuterAirfoilPanelList.size()).clear();
		checkBoxWingLinkedToPreviousPanelList.stream().forEach(cb -> cb.selectedProperty().set(false));
		if (checkBoxWingLinkedToPreviousPanelList.size() > 0)
			checkBoxWingLinkedToPreviousPanelList.subList(0, checkBoxWingLinkedToPreviousPanelList.size()).clear();
		choiceBoxWingSpanPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxWingSpanPanelUnitList.size() > 1)
			choiceBoxWingSpanPanelUnitList.subList(1, choiceBoxWingSpanPanelUnitList.size()).clear();
		choiceBoxWingSweepLEPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxWingSweepLEPanelUnitList.size() > 1)
			choiceBoxWingSweepLEPanelUnitList.subList(1, choiceBoxWingSweepLEPanelUnitList.size()).clear();
		choiceBoxWingDihedralPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxWingDihedralPanelUnitList.size() > 1)
			choiceBoxWingDihedralPanelUnitList.subList(1, choiceBoxWingDihedralPanelUnitList.size()).clear();
		choiceBoxWingInnerChordPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxWingInnerChordPanelUnitList.size() > 1)
			choiceBoxWingInnerChordPanelUnitList.subList(1, choiceBoxWingInnerChordPanelUnitList.size()).clear();
		choiceBoxWingInnerTwistPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxWingInnerTwistPanelUnitList.size() > 1)
			choiceBoxWingInnerTwistPanelUnitList.subList(1, choiceBoxWingInnerTwistPanelUnitList.size()).clear();
		choiceBoxWingOuterChordPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxWingOuterChordPanelUnitList.size() > 1)
			choiceBoxWingOuterChordPanelUnitList.subList(1, choiceBoxWingOuterChordPanelUnitList.size()).clear();
		choiceBoxWingOuterTwistPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxWingOuterTwistPanelUnitList.size() > 1)
			choiceBoxWingOuterTwistPanelUnitList.subList(1, choiceBoxWingOuterTwistPanelUnitList.size()).clear();
		if (detailButtonWingInnerAirfoilList.size() > 1)
			detailButtonWingInnerAirfoilList.subList(1, detailButtonWingInnerAirfoilList.size()).clear();
		if (detailButtonWingOuterAirfoilList.size() > 1)
			detailButtonWingOuterAirfoilList.subList(1, detailButtonWingOuterAirfoilList.size()).clear();
		tabPaneWingPanels.getTabs().remove(1, tabPaneWingPanels.getTabs().size());
		
		// flaps
		textFieldWingInnerPositionFlapList.stream().forEach(tf -> tf.clear());
		if (textFieldWingInnerPositionFlapList.size() > 1)
			textFieldWingInnerPositionFlapList.subList(1, textFieldWingInnerPositionFlapList.size()).clear();
		textFieldWingOuterPositionFlapList.stream().forEach(tf -> tf.clear());
		if (textFieldWingOuterPositionFlapList.size() > 1)
			textFieldWingOuterPositionFlapList.subList(1, textFieldWingOuterPositionFlapList.size()).clear();
		textFieldWingInnerChordRatioFlapList.stream().forEach(tf -> tf.clear());
		if (textFieldWingInnerChordRatioFlapList.size() > 1)
			textFieldWingInnerChordRatioFlapList.subList(1, textFieldWingInnerChordRatioFlapList.size()).clear();
		textFieldWingOuterChordRatioFlapList.stream().forEach(tf -> tf.clear());
		if (textFieldWingOuterChordRatioFlapList.size() > 1)
			textFieldWingOuterChordRatioFlapList.subList(1, textFieldWingOuterChordRatioFlapList.size()).clear();
		textFieldWingMinimumDeflectionAngleFlapList.stream().forEach(tf -> tf.clear());
		if (textFieldWingMinimumDeflectionAngleFlapList.size() > 1)
			textFieldWingMinimumDeflectionAngleFlapList.subList(1, textFieldWingMinimumDeflectionAngleFlapList.size()).clear();
		textFieldWingMaximumDeflectionAngleFlapList.stream().forEach(tf -> tf.clear());
		if (textFieldWingMaximumDeflectionAngleFlapList.size() > 1)
			textFieldWingMaximumDeflectionAngleFlapList.subList(1, textFieldWingMaximumDeflectionAngleFlapList.size()).clear();
		choiceBoxWingMinimumDeflectionAngleFlapUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxWingMinimumDeflectionAngleFlapUnitList.size() > 1)
			choiceBoxWingMinimumDeflectionAngleFlapUnitList.subList(1, choiceBoxWingMinimumDeflectionAngleFlapUnitList.size()).clear();
		choiceBoxWingMinimumDeflectionAngleFlapUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxWingMinimumDeflectionAngleFlapUnitList.size() > 1)
			choiceBoxWingMinimumDeflectionAngleFlapUnitList.subList(1, choiceBoxWingMinimumDeflectionAngleFlapUnitList.size()).clear();
		tabPaneWingFlaps.getTabs().remove(1, tabPaneWingFlaps.getTabs().size());
		
		// slats
		textFieldWingInnerPositionSlatList.stream().forEach(tf -> tf.clear());
		if (textFieldWingInnerPositionSlatList.size() > 1)
			textFieldWingInnerPositionSlatList.subList(1, textFieldWingInnerPositionSlatList.size()).clear();
		textFieldWingOuterPositionSlatList.stream().forEach(tf -> tf.clear());
		if (textFieldWingOuterPositionSlatList.size() > 1)
			textFieldWingOuterPositionSlatList.subList(1, textFieldWingOuterPositionSlatList.size()).clear();
		textFieldWingInnerChordRatioSlatList.stream().forEach(tf -> tf.clear());
		if (textFieldWingInnerChordRatioSlatList.size() > 1)
			textFieldWingInnerChordRatioSlatList.subList(1, textFieldWingInnerChordRatioSlatList.size()).clear();
		textFieldWingOuterChordRatioSlatList.stream().forEach(tf -> tf.clear());
		if (textFieldWingOuterChordRatioSlatList.size() > 1)
			textFieldWingOuterChordRatioSlatList.subList(1, textFieldWingOuterChordRatioSlatList.size()).clear();
		textFieldWingExtensionRatioSlatList.stream().forEach(tf -> tf.clear());
		if (textFieldWingExtensionRatioSlatList.size() > 1)
			textFieldWingExtensionRatioSlatList.subList(1, textFieldWingExtensionRatioSlatList.size()).clear();
		textFieldWingMinimumDeflectionAngleSlatList.stream().forEach(tf -> tf.clear());
		if (textFieldWingMinimumDeflectionAngleSlatList.size() > 1)
			textFieldWingMinimumDeflectionAngleSlatList.subList(1, textFieldWingMinimumDeflectionAngleSlatList.size()).clear();
		textFieldWingMaximumDeflectionAngleSlatList.stream().forEach(tf -> tf.clear());
		if (textFieldWingMaximumDeflectionAngleSlatList.size() > 1)
			textFieldWingMaximumDeflectionAngleSlatList.subList(1, textFieldWingMaximumDeflectionAngleSlatList.size()).clear();
		choiceBoxWingMinimumDeflectionAngleSlatUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxWingMinimumDeflectionAngleSlatUnitList.size() > 1)
			choiceBoxWingMinimumDeflectionAngleSlatUnitList.subList(1, choiceBoxWingMinimumDeflectionAngleSlatUnitList.size()).clear();
		choiceBoxWingMaximumDeflectionAngleSlatUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxWingMaximumDeflectionAngleSlatUnitList.size() > 1)
			choiceBoxWingMaximumDeflectionAngleSlatUnitList.subList(1, choiceBoxWingMaximumDeflectionAngleSlatUnitList.size()).clear();
		tabPaneWingSlats.getTabs().remove(1, tabPaneWingSlats.getTabs().size());

		// ailerons
		textFieldWingInnerPositionAileronLeft.clear();
		textFieldWingInnerPositionAileronLeft.clear();
		textFieldWingOuterPositionAileronLeft.clear();
		textFieldWingInnerChordRatioAileronLeft.clear();
		textFieldWingOuterChordRatioAileronLeft.clear();
		textFieldWingMinimumDeflectionAngleAileronLeft.clear();
		textFieldWingMaximumDeflectionAngleAileronLeft.clear();
		textFieldWingInnerPositionAileronRight.clear();
		textFieldWingOuterPositionAileronRight.clear();
		textFieldWingInnerChordRatioAileronRight.clear();
		textFieldWingOuterChordRatioAileronRight.clear();
		textFieldWingMinimumDeflectionAngleAileronRight.clear();
		textFieldWingMaximumDeflectionAngleAileronRight.clear();
		
		// spoilers
		textFieldWingInnerSpanwisePositionSpoilerList.stream().forEach(tf -> tf.clear());
		if (textFieldWingInnerSpanwisePositionSpoilerList.size() > 1)
			textFieldWingInnerSpanwisePositionSpoilerList.subList(1, textFieldWingInnerSpanwisePositionSpoilerList.size()).clear();
		textFieldWingOuterSpanwisePositionSpoilerList.stream().forEach(tf -> tf.clear());
		if (textFieldWingOuterSpanwisePositionSpoilerList.size() > 1)
			textFieldWingOuterSpanwisePositionSpoilerList.subList(1, textFieldWingOuterSpanwisePositionSpoilerList.size()).clear();
		textFieldWingInnerChordwisePositionSpoilerList.stream().forEach(tf -> tf.clear());
		if (textFieldWingInnerChordwisePositionSpoilerList.size() > 1)
			textFieldWingInnerChordwisePositionSpoilerList.subList(1, textFieldWingInnerChordwisePositionSpoilerList.size()).clear();
		textFieldWingOuterChordwisePositionSpoilerList.stream().forEach(tf -> tf.clear());
		if (textFieldWingOuterChordwisePositionSpoilerList.size() > 1)
			textFieldWingOuterChordwisePositionSpoilerList.subList(1, textFieldWingOuterChordwisePositionSpoilerList.size()).clear();
		textFieldWingMinimumDeflectionAngleSpoilerList.stream().forEach(tf -> tf.clear());
		if (textFieldWingMinimumDeflectionAngleSpoilerList.size() > 1)
			textFieldWingMinimumDeflectionAngleSpoilerList.subList(1, textFieldWingMinimumDeflectionAngleSpoilerList.size()).clear();
		textFieldWingMaximumDeflectionAngleSpoilerList.stream().forEach(tf -> tf.clear());
		if (textFieldWingMaximumDeflectionAngleSpoilerList.size() > 1)
			textFieldWingMaximumDeflectionAngleSpoilerList.subList(1, textFieldWingMaximumDeflectionAngleSpoilerList.size()).clear();
		choiceBoxWingMinimumDeflectionAngleSpoilerUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxWingMinimumDeflectionAngleSpoilerUnitList.size() > 1)
			choiceBoxWingMinimumDeflectionAngleSpoilerUnitList.subList(1, choiceBoxWingMinimumDeflectionAngleSpoilerUnitList.size()).clear();
		choiceBoxWingMaximumDeflectionAngleSpoilerUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxWingMaximumDeflectionAngleSpoilerUnitList.size() > 1)
			choiceBoxWingMaximumDeflectionAngleSpoilerUnitList.subList(1, choiceBoxWingMaximumDeflectionAngleSpoilerUnitList.size()).clear();
		tabPaneWingSpoilers.getTabs().remove(1, tabPaneWingSpoilers.getTabs().size());
		
		//..................................................................................
		// HTAIL
		hTailAdjustCriterionChoiceBox.getSelectionModel().clearSelection();
		textFieldHTailCompositeMassCorrectionFactor.clear();
		textFieldHTailRoughness.clear();
		hTailRoughnessUnitChoiceBox.getSelectionModel().clearSelection();
		
		// View and TextArea
		textAreaHTailConsoleOutput.clear();
		hTailPlanformPane.getChildren().clear();
		for (int i=1; i < tabPaneHTailViewAndAirfoils.getTabs().size(); i++)
			tabPaneHTailViewAndAirfoils.getTabs().remove(i);
		
		// panels
		textFieldHTailSpanPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailSpanPanelList.size() > 1)
			textFieldHTailSpanPanelList.subList(1, textFieldHTailSpanPanelList.size()).clear();
		textFieldHTailSweepLEPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailSweepLEPanelList.size() > 1)
			textFieldHTailSweepLEPanelList.subList(1, textFieldHTailSweepLEPanelList.size()).clear();
		textFieldHTailDihedralPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailDihedralPanelList.size() > 1)
			textFieldHTailDihedralPanelList.subList(1, textFieldHTailDihedralPanelList.size()).clear();
		textFieldHTailInnerChordPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailInnerChordPanelList.size() > 1)
			textFieldHTailInnerChordPanelList.subList(1, textFieldHTailInnerChordPanelList.size()).clear();
		textFieldHTailInnerTwistPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailInnerTwistPanelList.size() > 1)
			textFieldHTailInnerTwistPanelList.subList(1, textFieldHTailInnerTwistPanelList.size()).clear();
		textFieldHTailInnerAirfoilPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailInnerAirfoilPanelList.size() > 1)
			textFieldHTailInnerAirfoilPanelList.subList(1, textFieldHTailInnerAirfoilPanelList.size()).clear();
		textFieldHTailOuterChordPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailOuterChordPanelList.size() > 1)
			textFieldHTailOuterChordPanelList.subList(1, textFieldHTailOuterChordPanelList.size()).clear();
		textFieldHTailOuterTwistPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailOuterTwistPanelList.size() > 1)
			textFieldHTailOuterTwistPanelList.subList(1, textFieldHTailOuterTwistPanelList.size()).clear();
		textFieldHTailOuterAirfoilPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailOuterAirfoilPanelList.size() > 1)
			textFieldHTailOuterAirfoilPanelList.subList(1, textFieldHTailOuterAirfoilPanelList.size()).clear();
		checkBoxHTailLinkedToPreviousPanelList.stream().forEach(cb -> cb.selectedProperty().set(false));
		if (checkBoxHTailLinkedToPreviousPanelList.size() > 0)
			checkBoxHTailLinkedToPreviousPanelList.subList(0, checkBoxHTailLinkedToPreviousPanelList.size()).clear();
		choiceBoxHTailSpanPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxHTailSpanPanelUnitList.size() > 1)
			choiceBoxHTailSpanPanelUnitList.subList(1, choiceBoxHTailSpanPanelUnitList.size()).clear();
		choiceBoxHTailSweepLEPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxHTailSweepLEPanelUnitList.size() > 1)
			choiceBoxHTailSweepLEPanelUnitList.subList(1, choiceBoxHTailSweepLEPanelUnitList.size()).clear();
		choiceBoxHTailDihedralPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxHTailDihedralPanelUnitList.size() > 1)
			choiceBoxHTailDihedralPanelUnitList.subList(1, choiceBoxHTailDihedralPanelUnitList.size()).clear();
		choiceBoxHTailInnerChordPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxHTailInnerChordPanelUnitList.size() > 1)
			choiceBoxHTailInnerChordPanelUnitList.subList(1, choiceBoxHTailInnerChordPanelUnitList.size()).clear();
		choiceBoxHTailInnerTwistPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxHTailInnerTwistPanelUnitList.size() > 1)
			choiceBoxHTailInnerTwistPanelUnitList.subList(1, choiceBoxHTailInnerTwistPanelUnitList.size()).clear();
		choiceBoxHTailOuterChordPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxHTailOuterChordPanelUnitList.size() > 1)
			choiceBoxHTailOuterChordPanelUnitList.subList(1, choiceBoxHTailOuterChordPanelUnitList.size()).clear();
		choiceBoxHTailOuterTwistPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxHTailOuterTwistPanelUnitList.size() > 1)
			choiceBoxHTailOuterTwistPanelUnitList.subList(1, choiceBoxHTailOuterTwistPanelUnitList.size()).clear();
		if (detailButtonHTailInnerAirfoilList.size() > 1)
			detailButtonHTailInnerAirfoilList.subList(1, detailButtonHTailInnerAirfoilList.size()).clear();
		if (detailButtonHTailOuterAirfoilList.size() > 1)
			detailButtonHTailOuterAirfoilList.subList(1, detailButtonHTailOuterAirfoilList.size()).clear();
		tabPaneHTailPanels.getTabs().remove(1, tabPaneHTailPanels.getTabs().size());
		
		// elevators
		textFieldHTailInnerPositionElevatorList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailInnerPositionElevatorList.size() > 1)
			textFieldHTailInnerPositionElevatorList.subList(1, textFieldHTailInnerPositionElevatorList.size()).clear();
		textFieldHTailOuterPositionElevatorList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailOuterPositionElevatorList.size() > 1)
			textFieldHTailOuterPositionElevatorList.subList(1, textFieldHTailOuterPositionElevatorList.size()).clear();
		textFieldHTailInnerChordRatioElevatorList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailInnerChordRatioElevatorList.size() > 1)
			textFieldHTailInnerChordRatioElevatorList.subList(1, textFieldHTailInnerChordRatioElevatorList.size()).clear();
		textFieldHTailOuterChordRatioElevatorList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailOuterChordRatioElevatorList.size() > 1)
			textFieldHTailOuterChordRatioElevatorList.subList(1, textFieldHTailOuterChordRatioElevatorList.size()).clear();
		textFieldHTailMinimumDeflectionAngleElevatorList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailMinimumDeflectionAngleElevatorList.size() > 1)
			textFieldHTailMinimumDeflectionAngleElevatorList.subList(1, textFieldHTailMinimumDeflectionAngleElevatorList.size()).clear();
		textFieldHTailMaximumDeflectionAngleElevatorList.stream().forEach(tf -> tf.clear());
		if (textFieldHTailMaximumDeflectionAngleElevatorList.size() > 1)
			textFieldHTailMaximumDeflectionAngleElevatorList.subList(1, textFieldHTailMaximumDeflectionAngleElevatorList.size()).clear();
		choiceBoxHTailMinimumDeflectionAngleElevatorUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxHTailMinimumDeflectionAngleElevatorUnitList.size() > 1)
			choiceBoxHTailMinimumDeflectionAngleElevatorUnitList.subList(1, choiceBoxHTailMinimumDeflectionAngleElevatorUnitList.size()).clear();
		choiceBoxHTailMinimumDeflectionAngleElevatorUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxHTailMinimumDeflectionAngleElevatorUnitList.size() > 1)
			choiceBoxHTailMinimumDeflectionAngleElevatorUnitList.subList(1, choiceBoxHTailMinimumDeflectionAngleElevatorUnitList.size()).clear();
		tabPaneHTailElevators.getTabs().remove(1, tabPaneHTailElevators.getTabs().size());
		
		//..................................................................................
		// VTAIL
		vTailAdjustCriterionChoiceBox.getSelectionModel().clearSelection();
		textFieldVTailCompositeMassCorrectionFactor.clear();
		textFieldVTailRoughness.clear();
		vTailRoughnessUnitChoiceBox.getSelectionModel().clearSelection();
		
		// View and TextArea
		textAreaVTailConsoleOutput.clear();
		vTailPlanformPane.getChildren().clear();
		for (int i=1; i < tabPaneVTailViewAndAirfoils.getTabs().size(); i++)
			tabPaneVTailViewAndAirfoils.getTabs().remove(i);
		
		// panels
		textFieldVTailSpanPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailSpanPanelList.size() > 1)
			textFieldVTailSpanPanelList.subList(1, textFieldVTailSpanPanelList.size()).clear();
		textFieldVTailSweepLEPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailSweepLEPanelList.size() > 1)
			textFieldVTailSweepLEPanelList.subList(1, textFieldVTailSweepLEPanelList.size()).clear();
		textFieldVTailDihedralPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailDihedralPanelList.size() > 1)
			textFieldVTailDihedralPanelList.subList(1, textFieldVTailDihedralPanelList.size()).clear();
		textFieldVTailInnerChordPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailInnerChordPanelList.size() > 1)
			textFieldVTailInnerChordPanelList.subList(1, textFieldVTailInnerChordPanelList.size()).clear();
		textFieldVTailInnerTwistPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailInnerTwistPanelList.size() > 1)
			textFieldVTailInnerTwistPanelList.subList(1, textFieldVTailInnerTwistPanelList.size()).clear();
		textFieldVTailInnerAirfoilPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailInnerAirfoilPanelList.size() > 1)
			textFieldVTailInnerAirfoilPanelList.subList(1, textFieldVTailInnerAirfoilPanelList.size()).clear();
		textFieldVTailOuterChordPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailOuterChordPanelList.size() > 1)
			textFieldVTailOuterChordPanelList.subList(1, textFieldVTailOuterChordPanelList.size()).clear();
		textFieldVTailOuterTwistPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailOuterTwistPanelList.size() > 1)
			textFieldVTailOuterTwistPanelList.subList(1, textFieldVTailOuterTwistPanelList.size()).clear();
		textFieldVTailOuterAirfoilPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailOuterAirfoilPanelList.size() > 1)
			textFieldVTailOuterAirfoilPanelList.subList(1, textFieldVTailOuterAirfoilPanelList.size()).clear();
		checkBoxVTailLinkedToPreviousPanelList.stream().forEach(cb -> cb.selectedProperty().set(false));
		if (checkBoxVTailLinkedToPreviousPanelList.size() > 0)
			checkBoxVTailLinkedToPreviousPanelList.subList(0, checkBoxVTailLinkedToPreviousPanelList.size()).clear();
		choiceBoxVTailSpanPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxVTailSpanPanelUnitList.size() > 1)
			choiceBoxVTailSpanPanelUnitList.subList(1, choiceBoxVTailSpanPanelUnitList.size()).clear();
		choiceBoxVTailSweepLEPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxVTailSweepLEPanelUnitList.size() > 1)
			choiceBoxVTailSweepLEPanelUnitList.subList(1, choiceBoxVTailSweepLEPanelUnitList.size()).clear();
		choiceBoxVTailDihedralPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxVTailDihedralPanelUnitList.size() > 1)
			choiceBoxVTailDihedralPanelUnitList.subList(1, choiceBoxVTailDihedralPanelUnitList.size()).clear();
		choiceBoxVTailInnerChordPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxVTailInnerChordPanelUnitList.size() > 1)
			choiceBoxVTailInnerChordPanelUnitList.subList(1, choiceBoxVTailInnerChordPanelUnitList.size()).clear();
		choiceBoxVTailInnerTwistPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxVTailInnerTwistPanelUnitList.size() > 1)
			choiceBoxVTailInnerTwistPanelUnitList.subList(1, choiceBoxVTailInnerTwistPanelUnitList.size()).clear();
		choiceBoxVTailOuterChordPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxVTailOuterChordPanelUnitList.size() > 1)
			choiceBoxVTailOuterChordPanelUnitList.subList(1, choiceBoxVTailOuterChordPanelUnitList.size()).clear();
		choiceBoxVTailOuterTwistPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxVTailOuterTwistPanelUnitList.size() > 1)
			choiceBoxVTailOuterTwistPanelUnitList.subList(1, choiceBoxVTailOuterTwistPanelUnitList.size()).clear();
		if (detailButtonVTailInnerAirfoilList.size() > 1)
			detailButtonVTailInnerAirfoilList.subList(1, detailButtonVTailInnerAirfoilList.size()).clear();
		if (detailButtonVTailOuterAirfoilList.size() > 1)
			detailButtonVTailOuterAirfoilList.subList(1, detailButtonVTailOuterAirfoilList.size()).clear();
		tabPaneVTailPanels.getTabs().remove(1, tabPaneVTailPanels.getTabs().size());
		
		// Rudders
		textFieldVTailInnerPositionRudderList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailInnerPositionRudderList.size() > 1)
			textFieldVTailInnerPositionRudderList.subList(1, textFieldVTailInnerPositionRudderList.size()).clear();
		textFieldVTailOuterPositionRudderList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailOuterPositionRudderList.size() > 1)
			textFieldVTailOuterPositionRudderList.subList(1, textFieldVTailOuterPositionRudderList.size()).clear();
		textFieldVTailInnerChordRatioRudderList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailInnerChordRatioRudderList.size() > 1)
			textFieldVTailInnerChordRatioRudderList.subList(1, textFieldVTailInnerChordRatioRudderList.size()).clear();
		textFieldVTailOuterChordRatioRudderList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailOuterChordRatioRudderList.size() > 1)
			textFieldVTailOuterChordRatioRudderList.subList(1, textFieldVTailOuterChordRatioRudderList.size()).clear();
		textFieldVTailMinimumDeflectionAngleRudderList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailMinimumDeflectionAngleRudderList.size() > 1)
			textFieldVTailMinimumDeflectionAngleRudderList.subList(1, textFieldVTailMinimumDeflectionAngleRudderList.size()).clear();
		textFieldVTailMaximumDeflectionAngleRudderList.stream().forEach(tf -> tf.clear());
		if (textFieldVTailMaximumDeflectionAngleRudderList.size() > 1)
			textFieldVTailMaximumDeflectionAngleRudderList.subList(1, textFieldVTailMaximumDeflectionAngleRudderList.size()).clear();
		choiceBoxVTailMinimumDeflectionAngleRudderUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxVTailMinimumDeflectionAngleRudderUnitList.size() > 1)
			choiceBoxVTailMinimumDeflectionAngleRudderUnitList.subList(1, choiceBoxVTailMinimumDeflectionAngleRudderUnitList.size()).clear();
		choiceBoxVTailMinimumDeflectionAngleRudderUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxVTailMinimumDeflectionAngleRudderUnitList.size() > 1)
			choiceBoxVTailMinimumDeflectionAngleRudderUnitList.subList(1, choiceBoxVTailMinimumDeflectionAngleRudderUnitList.size()).clear();
		tabPaneVTailRudders.getTabs().remove(1, tabPaneVTailRudders.getTabs().size());
		
		//..................................................................................
		// CANARD
		canardAdjustCriterionChoiceBox.getSelectionModel().clearSelection();
		textFieldCanardCompositeMassCorrectionFactor.clear();
		textFieldCanardRoughness.clear();
		canardRoughnessUnitChoiceBox.getSelectionModel().clearSelection();
		
		// View and TextArea
		textAreaCanardConsoleOutput.clear();
		canardPlanformPane.getChildren().clear();
		for (int i=1; i < tabPaneCanardViewAndAirfoils.getTabs().size(); i++)
			tabPaneCanardViewAndAirfoils.getTabs().remove(i);
		
		// panels
		textFieldCanardSpanPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardSpanPanelList.size() > 1)
			textFieldCanardSpanPanelList.subList(1, textFieldCanardSpanPanelList.size()).clear();
		textFieldCanardSweepLEPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardSweepLEPanelList.size() > 1)
			textFieldCanardSweepLEPanelList.subList(1, textFieldCanardSweepLEPanelList.size()).clear();
		textFieldCanardDihedralPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardDihedralPanelList.size() > 1)
			textFieldCanardDihedralPanelList.subList(1, textFieldCanardDihedralPanelList.size()).clear();
		textFieldCanardInnerChordPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardInnerChordPanelList.size() > 1)
			textFieldCanardInnerChordPanelList.subList(1, textFieldCanardInnerChordPanelList.size()).clear();
		textFieldCanardInnerTwistPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardInnerTwistPanelList.size() > 1)
			textFieldCanardInnerTwistPanelList.subList(1, textFieldCanardInnerTwistPanelList.size()).clear();
		textFieldCanardInnerAirfoilPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardInnerAirfoilPanelList.size() > 1)
			textFieldCanardInnerAirfoilPanelList.subList(1, textFieldCanardInnerAirfoilPanelList.size()).clear();
		textFieldCanardOuterChordPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardOuterChordPanelList.size() > 1)
			textFieldCanardOuterChordPanelList.subList(1, textFieldCanardOuterChordPanelList.size()).clear();
		textFieldCanardOuterTwistPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardOuterTwistPanelList.size() > 1)
			textFieldCanardOuterTwistPanelList.subList(1, textFieldCanardOuterTwistPanelList.size()).clear();
		textFieldCanardOuterAirfoilPanelList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardOuterAirfoilPanelList.size() > 1)
			textFieldCanardOuterAirfoilPanelList.subList(1, textFieldCanardOuterAirfoilPanelList.size()).clear();
		checkBoxCanardLinkedToPreviousPanelList.stream().forEach(cb -> cb.selectedProperty().set(false));
		if (checkBoxCanardLinkedToPreviousPanelList.size() > 0)
			checkBoxCanardLinkedToPreviousPanelList.subList(0, checkBoxCanardLinkedToPreviousPanelList.size()).clear();
		choiceBoxCanardSpanPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxCanardSpanPanelUnitList.size() > 1)
			choiceBoxCanardSpanPanelUnitList.subList(1, choiceBoxCanardSpanPanelUnitList.size()).clear();
		choiceBoxCanardSweepLEPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxCanardSweepLEPanelUnitList.size() > 1)
			choiceBoxCanardSweepLEPanelUnitList.subList(1, choiceBoxCanardSweepLEPanelUnitList.size()).clear();
		choiceBoxCanardDihedralPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxCanardDihedralPanelUnitList.size() > 1)
			choiceBoxCanardDihedralPanelUnitList.subList(1, choiceBoxCanardDihedralPanelUnitList.size()).clear();
		choiceBoxCanardInnerChordPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxCanardInnerChordPanelUnitList.size() > 1)
			choiceBoxCanardInnerChordPanelUnitList.subList(1, choiceBoxCanardInnerChordPanelUnitList.size()).clear();
		choiceBoxCanardInnerTwistPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxCanardInnerTwistPanelUnitList.size() > 1)
			choiceBoxCanardInnerTwistPanelUnitList.subList(1, choiceBoxCanardInnerTwistPanelUnitList.size()).clear();
		choiceBoxCanardOuterChordPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxCanardOuterChordPanelUnitList.size() > 1)
			choiceBoxCanardOuterChordPanelUnitList.subList(1, choiceBoxCanardOuterChordPanelUnitList.size()).clear();
		choiceBoxCanardOuterTwistPanelUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxCanardOuterTwistPanelUnitList.size() > 1)
			choiceBoxCanardOuterTwistPanelUnitList.subList(1, choiceBoxCanardOuterTwistPanelUnitList.size()).clear();
		if (detailButtonCanardInnerAirfoilList.size() > 1)
			detailButtonCanardInnerAirfoilList.subList(1, detailButtonCanardInnerAirfoilList.size()).clear();
		if (detailButtonCanardOuterAirfoilList.size() > 1)
			detailButtonCanardOuterAirfoilList.subList(1, detailButtonCanardOuterAirfoilList.size()).clear();
		tabPaneCanardPanels.getTabs().remove(1, tabPaneCanardPanels.getTabs().size());
		
		// Control Surfaces
		textFieldCanardInnerPositionControlSurfaceList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardInnerPositionControlSurfaceList.size() > 1)
			textFieldCanardInnerPositionControlSurfaceList.subList(1, textFieldCanardInnerPositionControlSurfaceList.size()).clear();
		textFieldCanardOuterPositionControlSurfaceList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardOuterPositionControlSurfaceList.size() > 1)
			textFieldCanardOuterPositionControlSurfaceList.subList(1, textFieldCanardOuterPositionControlSurfaceList.size()).clear();
		textFieldCanardInnerChordRatioControlSurfaceList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardInnerChordRatioControlSurfaceList.size() > 1)
			textFieldCanardInnerChordRatioControlSurfaceList.subList(1, textFieldCanardInnerChordRatioControlSurfaceList.size()).clear();
		textFieldCanardOuterChordRatioControlSurfaceList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardOuterChordRatioControlSurfaceList.size() > 1)
			textFieldCanardOuterChordRatioControlSurfaceList.subList(1, textFieldCanardOuterChordRatioControlSurfaceList.size()).clear();
		textFieldCanardMinimumDeflectionAngleControlSurfaceList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardMinimumDeflectionAngleControlSurfaceList.size() > 1)
			textFieldCanardMinimumDeflectionAngleControlSurfaceList.subList(1, textFieldCanardMinimumDeflectionAngleControlSurfaceList.size()).clear();
		textFieldCanardMaximumDeflectionAngleControlSurfaceList.stream().forEach(tf -> tf.clear());
		if (textFieldCanardMaximumDeflectionAngleControlSurfaceList.size() > 1)
			textFieldCanardMaximumDeflectionAngleControlSurfaceList.subList(1, textFieldCanardMaximumDeflectionAngleControlSurfaceList.size()).clear();
		choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList.size() > 1)
			choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList.subList(1, choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList.size()).clear();
		choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList.stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList.size() > 1)
			choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList.subList(1, choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList.size()).clear();
		tabPaneCanardControlSurfaces.getTabs().remove(1, tabPaneCanardControlSurfaces.getTabs().size());
		
		//..................................................................................
		// NACELLE
		textFieldNacelleRoughness1.clear();
		nacelleRoughnessUnitChoiceBox1.getSelectionModel().clearSelection();
		textFieldNacelleLength1.clear();
		nacelleLengthUnitChoiceBox1.getSelectionModel().clearSelection();
		textFieldNacelleMaximumDiameter1.clear();
		nacelleMaximumDiameterUnitChoiceBox1.getSelectionModel().clearSelection();
		textFieldNacelleKInlet1.clear();
		textFieldNacelleKOutlet1.clear();
		textFieldNacelleKLength1.clear();
		textFieldNacelleKDiameterOutlet1.clear();
		
		textFieldNacelleRoughnessList.subList(1, textFieldNacelleRoughnessList.size()).clear();
		textFieldNacelleLengthList.subList(1, textFieldNacelleLengthList.size()).clear();
		textFieldNacelleMaximumDiameterList.subList(1, textFieldNacelleMaximumDiameterList.size()).clear();
		textFieldNacelleKInletList.subList(1, textFieldNacelleKInletList.size()).clear();
		textFieldNacelleKOutletList.subList(1, textFieldNacelleKOutletList.size()).clear();
		textFieldNacelleKLengthList.subList(1, textFieldNacelleKLengthList.size()).clear();
		textFieldNacelleKDiameterOutletList.subList(1, textFieldNacelleKDiameterOutletList.size()).clear();
		choiceBoxNacelleRoughnessUnitList.subList(1, choiceBoxNacelleRoughnessUnitList.size()).clear();
		choiceBoxNacelleLengthUnitList.subList(1, choiceBoxNacelleLengthUnitList.size()).clear();
		choiceBoxNacelleMaximumDiameterUnitList.subList(1, choiceBoxNacelleMaximumDiameterUnitList.size()).clear();
		nacelleEstimateDimesnsionButtonList.subList(1, nacelleEstimateDimesnsionButtonList.size()).clear();
		nacelleKInletInfoButtonList.subList(1, nacelleKInletInfoButtonList.size()).clear();
		nacelleKOutletInfoButtonList.subList(1, nacelleKOutletInfoButtonList.size()).clear();
		nacelleKLengthInfoButtonList.subList(1, nacelleKLengthInfoButtonList.size()).clear();
		nacelleKDiameterOutletInfoButtonList.subList(1, nacelleKDiameterOutletInfoButtonList.size()).clear();
		
		tabPaneNacelles.getTabs().remove(1, tabPaneNacelles.getTabs().size());
		
		// View and TextArea
		textAreaNacelleConsoleOutput.clear();
		nacelle1TopViewPane.getChildren().clear();
		nacelle1SideViewPane.getChildren().clear();
		nacelle1FrontViewPane.getChildren().clear();
		nacelleTopViewPaneList.subList(1, nacelleTopViewPaneList.size()).clear();
		nacelleSideViewPaneList.subList(1, nacelleSideViewPaneList.size()).clear();
		nacelleFrontViewPaneList.subList(1, nacelleFrontViewPaneList.size()).clear();
		tabPaneNacellesTopViews.getTabs().remove(1, tabPaneNacellesTopViews.getTabs().size());
		tabPaneNacellesSideViews.getTabs().remove(1, tabPaneNacellesSideViews.getTabs().size());
		tabPaneNacellesFrontViews.getTabs().remove(1, tabPaneNacellesFrontViews.getTabs().size());
		
		
		//..................................................................................
		// POWER PLANT
		textAreaPowerPlantConsoleOutput.clear();
		
		powerPlantJetRadioButtonList.subList(1, tabPaneEngines.getTabs().size()).clear();
		powerPlantTurbopropRadioButtonList.subList(1, tabPaneEngines.getTabs().size()).clear();
		powerPlantPistonRadioButtonList.subList(1, tabPaneEngines.getTabs().size()).clear();
		powerPlantToggleGropuList.subList(1, tabPaneEngines.getTabs().size()).clear();
		
		engineTurbojetTurbofanTypeChoiceBoxMap.clear();
		engineTurbojetTurbofanDatabaseTextFieldMap.clear();
		engineTurbojetTurbofanLengthTextFieldMap.clear();
		engineTurbojetTurbofanLengthUnitChoiceBoxMap.clear();
		engineTurbojetTurbofanDryMassTextFieldMap.clear();
		engineTurbojetTurbofanDryMassUnitChoiceBoxMap.clear();
		engineTurbojetTurbofanStaticThrustTextFieldMap.clear();
		engineTurbojetTurbofanStaticThrustUnitChoiceBoxMap.clear();
		engineTurbojetTurbofanBPRTextFieldMap.clear();
		engineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap.clear();
		engineTurbojetTurbofanNumberOfShaftsTextFieldMap.clear();
		engineTurbojetTurbofanOverallPressureRatioTextFieldMap.clear();
		
		engineTurbopropTypeChoiceBoxMap.clear(); 
		engineTurbopropDatabaseTextFieldMap.clear();
		engineTurbopropLengthTextFieldMap.clear();
		engineTurbopropLengthUnitChoiceBoxMap.clear();
		engineTurbopropDryMassTextFieldMap.clear();
		engineTurbopropDryMassUnitChoiceBoxMap.clear();
		engineTurbopropStaticPowerTextFieldMap.clear();
		engineTurbopropStaticPowerUnitChoiceBoxMap.clear();
		engineTurbopropPropellerDiameterTextFieldMap.clear();
		engineTurbopropPropellerDiameterUnitChoiceBoxMap.clear();
		engineTurbopropNumberOfBladesTextFieldMap.clear();
		engineTurbopropPropellerEfficiencyTextFieldMap.clear();
		engineTurbopropNumberOfCompressorStagesTextFieldMap.clear();
		engineTurbopropNumberOfShaftsTextFieldMap.clear();
		engineTurbopropOverallPressureRatioTextFieldMap.clear();
		
		enginePistonTypeChoiceBoxMap.clear(); 
		enginePistonDatabaseTextFieldMap.clear();
		enginePistonLengthTextFieldMap.clear();
		enginePistonLengthUnitChoiceBoxMap.clear();
		enginePistonDryMassTextFieldMap.clear();
		enginePistonDryMassUnitChoiceBoxMap.clear();
		enginePistonStaticPowerTextFieldMap.clear();
		enginePistonStaticPowerUnitChoiceBoxMap.clear();
		enginePistonPropellerDiameterTextFieldMap.clear();
		enginePistonPropellerDiameterUnitChoiceBoxMap.clear();
		enginePistonNumberOfBladesTextFieldMap.clear();
		enginePistonPropellerEfficiencyTextFieldMap.clear();
		
		for(int i=0; i<tabPaneEngines.getTabs().size(); i++) {
			if (powerPlantPaneMap.get(i).containsKey(EngineTypeEnum.TURBOFAN)) 
				powerPlantPaneMap.get(i).get(EngineTypeEnum.TURBOFAN).getChildren().clear();
			if (powerPlantPaneMap.get(i).containsKey(EngineTypeEnum.TURBOPROP)) 
				powerPlantPaneMap.get(i).get(EngineTypeEnum.TURBOPROP).getChildren().clear();
			if (powerPlantPaneMap.get(i).containsKey(EngineTypeEnum.PISTON)) 
				powerPlantPaneMap.get(i).get(EngineTypeEnum.PISTON).getChildren().clear();
			powerPlantBorderPaneMap.remove(i);
			powerPlantPaneMap.remove(i);
		}
		
		powerPlantJetRadioButtonList.get(0).setSelected(false);
		powerPlantTurbopropRadioButtonList.get(0).setSelected(false);
		powerPlantPistonRadioButtonList.get(0).setSelected(false);
		
		tabPaneEngines.getTabs().remove(1, tabPaneEngines.getTabs().size());
		
		//..................................................................................
		// LANDING GEARS
		textAreaLandingGearsConsoleOutput.clear();
		
		textFieldLandingGearsMainLegLength.clear();
		textFieldLandingGearsKMainLegLength.clear();
		textFieldLandingGearsNumberOfFrontalWheels.clear();
		textFieldLandingGearsNumberOfRearWheels.clear();
		textFieldLandingGearsDistanceBetweenWheels.clear();
		textFieldLandingGearsFrontalWheelsHeight.clear();
		textFieldLandingGearsFrontalWheelsWidth.clear();
		textFieldLandingGearsRearWheelsHeight.clear();
		textFieldLandingGearsRearWheelsWidth.clear();
		
		landingGearsMainLegLengthUnitChoiceBox.getSelectionModel().clearSelection();
		landingGearsDistanceBetweenWheelsUnitChoiceBox.getSelectionModel().clearSelection();
		landingGearsFrontalWheelsHeigthUnitChoiceBox.getSelectionModel().clearSelection();
		landingGearsFrontalWheelsWidthUnitChoiceBox.getSelectionModel().clearSelection();
		landingGearsRearWheelsHeigthUnitChoiceBox.getSelectionModel().clearSelection();
		landingGearsRearWheelsWidthUnitChoiceBox.getSelectionModel().clearSelection();
		
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
	private void loadAircraftFile() throws IOException, InterruptedException {
	
		if(isAircraftFile(textFieldAircraftInputFile.getText()))
			try {
				loadAircraftFileImplementation();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		
		System.gc();
		
	}

	@SuppressWarnings({ "rawtypes" })
	private void loadAircraftFileImplementation() throws IOException, InterruptedException {
		
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
		System.setOut(filterStream);
		
		Service loadAircraftService = new Service() {

			@Override
			protected Task createTask() {
				return new Task() {
					
					@Override
					protected Object call() throws Exception {
						
						System.setOut(filterStream);
						
						int numberOfOperations = 28;
						double progressIncrement = 100/numberOfOperations;
						
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
						updateProgress(1, numberOfOperations);
						updateMessage("Reading Database...");
						updateTitle(String.valueOf(progressIncrement) + "%");
						
						String dirLiftingSurfaces = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces";
						String dirFuselages = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "fuselages";
						String dirEngines = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "engines";
						String dirNacelles = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "nacelles";
						String dirLandingGears = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "landing_gears";
						String dirSystems = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "systems";
						String dirCabinConfiguration = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "cabin_configurations";
						String dirAirfoils = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces" + File.separator + "airfoils";

						String pathToXML = textFieldAircraftInputFile.getText(); 

						Main.setTheAircraft(
								Aircraft.importFromXML(
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
										veDSCDatabaseReader
										)
								);
						updateProgress(2, numberOfOperations);
						updateMessage("Creating Aircraft Object...");
						updateTitle(String.valueOf(progressIncrement*2) + "%");

						// COMPONENTS LOG TO INTERFACE
						if (Main.getTheAircraft() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									logAircraftFromFileToInterface();
									
								}
							});
							updateProgress(3, numberOfOperations);
							updateMessage("Logging Aircraft Object Data...");
							updateTitle(String.valueOf(progressIncrement*3) + "%");
						}
						if (Main.getTheAircraft().getFuselage() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									logFuselageFromFileToInterface();
									
								}
							});							
							updateProgress(4, numberOfOperations);
							updateMessage("Logging Fuselage Object Data...");
							updateTitle(String.valueOf(progressIncrement*4) + "%");
						}
						if (Main.getTheAircraft().getCabinConfiguration() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									logCabinConfigutionFromFileToInterface();
									
								}
							});				
							updateProgress(5, numberOfOperations);
							updateMessage("Logging Cabin Configuration Object Data...");
							updateTitle(String.valueOf(progressIncrement*5) + "%");
						}
						if (Main.getTheAircraft().getWing() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									logWingFromFileToInterface();
									
								}
							});				
							updateProgress(6, numberOfOperations);
							updateMessage("Logging Wing Object Data...");
							updateTitle(String.valueOf(progressIncrement*6) + "%");
						}
						if (Main.getTheAircraft().getHTail() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									logHTailFromFileToInterface();
									
								}
							});		
							updateProgress(7, numberOfOperations);
							updateMessage("Logging Horizontal Tail Object Data...");
							updateTitle(String.valueOf(progressIncrement*7) + "%");
						}
						if (Main.getTheAircraft().getVTail() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									logVTailFromFileToInterface();
									
								}
							});		
							updateProgress(8, numberOfOperations);
							updateMessage("Logging Vertical Tail Object Data...");
							updateTitle(String.valueOf(progressIncrement*8) + "%");
						}
						if (Main.getTheAircraft().getCanard() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									logCanardFromFileToInterface();
									
								}
							});		
							updateProgress(9, numberOfOperations);
							updateMessage("Logging Canard Object Data...");
							updateTitle(String.valueOf(progressIncrement*9) + "%");
						}
						if (Main.getTheAircraft().getNacelles() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									logNacelleFromFileToInterface();
									
								}
							});	
							updateProgress(10, numberOfOperations);
							updateMessage("Logging Nacelle Object Data...");
							updateTitle(String.valueOf(progressIncrement*10) + "%");
						}
						if (Main.getTheAircraft().getPowerPlant() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									logPowerPlantFromFileToInterface();
									
								}
							});	
							updateProgress(11, numberOfOperations);
							updateMessage("Logging Power Plant Object Data...");
							updateTitle(String.valueOf(progressIncrement*11) + "%");
						}
						if (Main.getTheAircraft().getLandingGears() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									logLandingGearsFromFileToInterface();
									
								}
							});	
							updateProgress(12, numberOfOperations);
							updateMessage("Logging Landing Gears Object Data...");
							updateTitle(String.valueOf(progressIncrement*12) + "%");
						}
						//............................
						// COMPONENTS 3 VIEW CREATION
						//............................
						// aircraft
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								createAircraftTopView();
								
							}
						});	
						updateProgress(13, numberOfOperations);
						updateMessage("Creating Aircraft Top View...");
						updateTitle(String.valueOf(progressIncrement*13) + "%");
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								createAircraftSideView();
								
							}
						});	
						updateProgress(14, numberOfOperations);
						updateMessage("Creating Aircraft Side View...");
						updateTitle(String.valueOf(progressIncrement*14) + "%");
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								createAircraftFrontView();
								
							}
						});	
						updateProgress(15, numberOfOperations);
						updateMessage("Creating Aircraft Front View...");
						updateTitle(String.valueOf(progressIncrement*15) + "%");
						//............................
						// fuselage
						if (Main.getTheAircraft().getFuselage() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createFuselageTopView();
									
								}
							});	
							updateProgress(16, numberOfOperations);
							updateMessage("Creating Fuselage Top View...");
							updateTitle(String.valueOf(progressIncrement*16) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createFuselageSideView();
									
								}
							});	
							updateProgress(17, numberOfOperations);
							updateMessage("Creating Fuselage Side View...");
							updateTitle(String.valueOf(progressIncrement*17) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createFuselageFrontView();
									
								}
							});	
							updateProgress(18, numberOfOperations);
							updateMessage("Creating Fuselage Front View...");
							updateTitle(String.valueOf(progressIncrement*18) + "%");
						}
						//............................
						// cabin configuration
						if (Main.getTheAircraft().getCabinConfiguration() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createSeatMap();
									
								}
							});	
							updateProgress(19, numberOfOperations);
							updateMessage("Creating Seat Map...");
							updateTitle(String.valueOf(progressIncrement*19) + "%");
						}
						//............................
						// wing
						if (Main.getTheAircraft().getWing() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createWingPlanformView();
									
								}
							});	
							updateProgress(20, numberOfOperations);
							updateMessage("Creating Wing Planform View...");
							updateTitle(String.valueOf(progressIncrement*20) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createEquivalentWingView();
									
								}
							});	
							updateProgress(21, numberOfOperations);
							updateMessage("Creating Equivalent Wing View...");
							updateTitle(String.valueOf(progressIncrement*21) + "%");
						}
						//............................
						// hTail
						if (Main.getTheAircraft().getHTail() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createHTailPlanformView();
									
								}
							});	
							updateProgress(22, numberOfOperations);
							updateMessage("Creating HTail Planform View...");
							updateTitle(String.valueOf(progressIncrement*22) + "%");
						}
						//............................
						// vTail
						if (Main.getTheAircraft().getVTail() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createVTailPlanformView();
									
								}
							});	
							updateProgress(23, numberOfOperations);
							updateMessage("Creating VTail Planform View...");
							updateTitle(String.valueOf(progressIncrement*23) + "%");
						}
						//............................
						// canard
						if (Main.getTheAircraft().getCanard() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createCanardPlanformView();
									
								}
							});	
							updateProgress(24, numberOfOperations);
							updateMessage("Creating Canard Planform View...");
							updateTitle(String.valueOf(progressIncrement*24) + "%");
						}
						//............................
						// nacelle
						if (Main.getTheAircraft().getNacelles() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createNacelleTopView();
									
								}
							});	
							updateProgress(25, numberOfOperations);
							updateMessage("Creating Nacelles Top View...");
							updateTitle(String.valueOf(progressIncrement*25) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createNacelleSideView();
									
								}
							});	
							updateProgress(26, numberOfOperations);
							updateMessage("Creating Nacelles Side View...");
							updateTitle(String.valueOf(progressIncrement*26) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createNacelleFrontView();
									
								}
							});	
							updateProgress(27, numberOfOperations);
							updateMessage("Creating Nacelles FrontView...");
							updateTitle(String.valueOf(progressIncrement*27) + "%");	
						}

						updateProgress(28, numberOfOperations);
						updateMessage("Aircraft Loaded!");
						updateTitle(String.valueOf(100) + "%");
						
						//Block the thread for a short time, but be sure
			            //to check the InterruptedException for cancellation
			            try {
			                Thread.sleep(100);
			            } catch (InterruptedException interrupted) {
			                if (isCancelled()) {
			                    updateMessage("Cancelled");
			                    updateProgress(0, 4);
								updateTitle(String.valueOf(0) + "%");
			                    return null;
			                }
			                else {
			                	updateMessage("Terminated");
			                	updateProgress(0, 4);
								updateTitle(String.valueOf(0) + "%");
			                    return null;
			                }
			            }
						
			    		ObjectProperty<Aircraft> aircraft = new SimpleObjectProperty<>();

			    		try {
			    			aircraft.set(Main.getTheAircraft());
			    			newAircraftButton.disableProperty().bind(
			    					Bindings.isNull(aircraft)
			    					);
			    		} catch (Exception e) {
			    			newAircraftButton.setDisable(true);
			    		}
			    		
						return null;
					}
					
				};
			}
		}; 
		
		Main.getProgressBar().progressProperty().unbind();
		Main.getStatusBar().textProperty().unbind();
		Main.getTaskPercentage().textProperty().unbind();
		
		Main.getProgressBar().progressProperty().bind(loadAircraftService.progressProperty());
		Main.getStatusBar().textProperty().bind(loadAircraftService.messageProperty());
		Main.getTaskPercentage().textProperty().bind(loadAircraftService.titleProperty());
		
		loadAircraftService.start();
		
		// write again
		System.setOut(originalOut);
		
	}
	
	@FXML
	private void updateAircraftData() throws IOException {
		
		if (Main.getTheAircraft() != null) {
			
			boolean adjustCrtiterionCheck = false;
			boolean fuselageCheck = true;
			boolean wingCheck = true;
			boolean hTailCheck = true;
			boolean vTailCheck = true;
			boolean canardCheck = true;
			
			if(Main.getTheAircraft().getFuselage() != null)
				if(fuselageAdjustCriterionChoiceBox.getSelectionModel().isEmpty())
					fuselageCheck = false;
			if(Main.getTheAircraft().getWing() != null)
				if(wingAdjustCriterionChoiceBox.getSelectionModel().isEmpty())
					wingCheck = false;
			if(Main.getTheAircraft().getHTail() != null)
				if(hTailAdjustCriterionChoiceBox.getSelectionModel().isEmpty())
					hTailCheck = false;
			if(Main.getTheAircraft().getVTail() != null)
				if(vTailAdjustCriterionChoiceBox.getSelectionModel().isEmpty())
					vTailCheck = false;
			if(Main.getTheAircraft().getCanard() != null)
				if(canardAdjustCriterionChoiceBox.getSelectionModel().isEmpty())
					canardCheck = false;
			
			if(fuselageCheck && wingCheck && hTailCheck && vTailCheck && canardCheck)
				adjustCrtiterionCheck = true;
			
			if(!adjustCrtiterionCheck) {
				
				//..................................................................................
				// ADJUST CRITERION AIRCRAFT COMPONENTS WARNING
				Stage adjustCriterionAircraftWarning = new Stage();
				
				adjustCriterionAircraftWarning.setTitle("No Adjust Criterion For Some Aircraft Components");
				adjustCriterionAircraftWarning.initModality(Modality.WINDOW_MODAL);
				adjustCriterionAircraftWarning.initStyle(StageStyle.UNDECORATED);
				adjustCriterionAircraftWarning.initOwner(Main.getPrimaryStage());

				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(Main.class.getResource("inputmanager/AdjustCriterionAircraftWarning.fxml"));
				BorderPane adjustCriterionAircraftWarningBorderPane = loader.load();
				
				Button continueButton = (Button) adjustCriterionAircraftWarningBorderPane.lookup("#warningContinueButton");
				continueButton.setOnAction(new EventHandler<ActionEvent>() {
					
					@Override
					public void handle(ActionEvent arg0) {
						adjustCriterionAircraftWarning.close();
						return;
					}
					
				});
				
				Button continueAndSetToNoneButton = (Button) adjustCriterionAircraftWarningBorderPane.lookup("#warningContinueNoneButton");
				continueAndSetToNoneButton.setOnAction(new EventHandler<ActionEvent>() {
					
					@Override
					public void handle(ActionEvent arg0) {
						adjustCriterionAircraftWarning.close();
						if(Main.getTheAircraft().getFuselage() != null)
							fuselageAdjustCriterionChoiceBox.getSelectionModel().select(0);
						if(Main.getTheAircraft().getWing() != null)
							wingAdjustCriterionChoiceBox.getSelectionModel().select(0);
						if(Main.getTheAircraft().getHTail() != null)
							hTailAdjustCriterionChoiceBox.getSelectionModel().select(0);
						if(Main.getTheAircraft().getVTail() != null)
							vTailAdjustCriterionChoiceBox.getSelectionModel().select(0);
						if(Main.getTheAircraft().getCanard() != null)
							canardAdjustCriterionChoiceBox.getSelectionModel().select(0);
						
						updateAircraftDataImplementation();
					}
					
				});
				
				Scene scene = new Scene(adjustCriterionAircraftWarningBorderPane);
				adjustCriterionAircraftWarning.setScene(scene);
				adjustCriterionAircraftWarning.sizeToScene();
				adjustCriterionAircraftWarning.show();
				
			}
			else 
				updateAircraftDataImplementation();
			
		}
		else {
			
			//..................................................................................
			// NULL AIRCRAFT DATA WARNING
			Stage saveAircraftDataWarning = new Stage();
			
			saveAircraftDataWarning.setTitle("Null Aircraft Warning");
			saveAircraftDataWarning.initModality(Modality.WINDOW_MODAL);
			saveAircraftDataWarning.initStyle(StageStyle.UNDECORATED);
			saveAircraftDataWarning.initOwner(Main.getPrimaryStage());

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("inputmanager/SaveAircraftDataWarningNoAircraft.fxml"));
			BorderPane saveAircraftDataWarningBorderPane = loader.load();
			
			Button continueButton = (Button) saveAircraftDataWarningBorderPane.lookup("#warningContinueButton");
			continueButton.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent arg0) {
					saveAircraftDataWarning.close();
				}
				
			});
			
			Scene scene = new Scene(saveAircraftDataWarningBorderPane);
			saveAircraftDataWarning.setScene(scene);
			saveAircraftDataWarning.sizeToScene();
	        saveAircraftDataWarning.show();
			
		}
		
		System.gc();
		
	}
	
	@SuppressWarnings("rawtypes")
	private void updateAircraftDataImplementation() {
		
		Service updateAircraftDataService = new Service() {

			@Override
			protected Task createTask() {
				return new Task() {

					@Override
					protected Object call() throws Exception {
						
						int numberOfOperations = 37;
						int progressIncrement = 100/numberOfOperations;
						
						// DATA UPDATE
						updateProgress(0, numberOfOperations);
						updateMessage("Start Updating Aircraft Data ...");
						updateTitle(String.valueOf(0) + "%");
						
						updateProgress(1, numberOfOperations);
						updateMessage("Updating Fuselage Tab Data ...");
						updateTitle(String.valueOf(progressIncrement) + "%");
						updateFuselageTabData();
						
						updateProgress(2, numberOfOperations);
						updateMessage("Updating Cabin Configuration Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*2) + "%");
						updateCabinConfigurationTabData();
						
						updateProgress(3, numberOfOperations);
						updateMessage("Updating Wing Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*3) + "%");
						updateWingTabData();
						
						updateProgress(4, numberOfOperations);
						updateMessage("Updating Horizontal Tail Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*4) + "%");
						updateHTailTabData();
						
						updateProgress(5, numberOfOperations);
						updateMessage("Updating Vertical Tail Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*5) + "%");
						updateVTailTabData();
						
						updateProgress(6, numberOfOperations);
						updateMessage("Updating Canard Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*6)+ "%");
						updateCanardTabData();
						
						updateProgress(7, numberOfOperations);
						updateMessage("Updating Nacelles Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*7) + "%");
						updateNacelleTabData();
						
						updateProgress(8, numberOfOperations);
						updateMessage("Updating Power Plant Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*8) + "%");
						updatePowerPlantTabData();
						
						updateProgress(9, numberOfOperations);
						updateMessage("Updating Landing Gears Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*9) + "%");
						updateLandingGearsTabData();
						
						updateProgress(10, numberOfOperations);
						updateMessage("Updating Aircraft Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*10) + "%");
						updateAircraftTabData();
						
						updateProgress(11, numberOfOperations);
						updateMessage("Creating the Aircraft Object ...");
						updateTitle(String.valueOf(progressIncrement*11) + "%");
						createAircraftObjectFromData();
						
						// COMPONENTS LOG TO INTERFACE
						if (Main.getTheAircraft() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									textAreaAircraftConsoleOutput.setText(Main.getTheAircraft().toString());
									
								}
							});
							updateProgress(12, numberOfOperations);
							updateMessage("Logging Aircraft Object Data...");
							updateTitle(String.valueOf(progressIncrement*12) + "%");
						}
						if (Main.getTheAircraft().getFuselage() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									textAreaFuselageConsoleOutput.setText(
											Main.getTheAircraft().getFuselage().getFuselageCreator().toString()
											);
									
								}
							});							
							updateProgress(13, numberOfOperations);
							updateMessage("Logging Fuselage Object Data...");
							updateTitle(String.valueOf(progressIncrement*13) + "%");
						}
						if (Main.getTheAircraft().getCabinConfiguration() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									textAreaCabinConfigurationConsoleOutput.setText(
											Main.getTheAircraft().getCabinConfiguration().toString()
											);
									
								}
							});				
							updateProgress(14, numberOfOperations);
							updateMessage("Logging Cabin Configuration Object Data...");
							updateTitle(String.valueOf(progressIncrement*14) + "%");
						}
						if (Main.getTheAircraft().getWing() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									textAreaWingConsoleOutput.setText(
											Main.getTheAircraft().getWing().getLiftingSurfaceCreator().toString()
											);
									
								}
							});				
							updateProgress(15, numberOfOperations);
							updateMessage("Logging Wing Object Data...");
							updateTitle(String.valueOf(progressIncrement*15) + "%");
						}
						if (Main.getTheAircraft().getHTail() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									textAreaHTailConsoleOutput.setText(
											Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().toString()
											);
									
								}
							});		
							updateProgress(16, numberOfOperations);
							updateMessage("Logging Horizontal Tail Object Data...");
							updateTitle(String.valueOf(progressIncrement*16) + "%");
						}
						if (Main.getTheAircraft().getVTail() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									textAreaVTailConsoleOutput.setText(
											Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().toString()
											);
									
								}
							});		
							updateProgress(17, numberOfOperations);
							updateMessage("Logging Vertical Tail Object Data...");
							updateTitle(String.valueOf(progressIncrement*17) + "%");
						}
						if (Main.getTheAircraft().getCanard() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									textAreaCanardConsoleOutput.setText(
											Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().toString()
											);
									
								}
							});		
							updateProgress(18, numberOfOperations);
							updateMessage("Logging Canard Object Data...");
							updateTitle(String.valueOf(progressIncrement*18) + "%");
						}
						if (Main.getTheAircraft().getNacelles() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									textAreaNacelleConsoleOutput.setText(
											Main.getTheAircraft().getNacelles().toString()
											);
									
								}
							});	
							updateProgress(19, numberOfOperations);
							updateMessage("Logging Nacelle Object Data...");
							updateTitle(String.valueOf(progressIncrement*19) + "%");
						}
						if (Main.getTheAircraft().getPowerPlant() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									textAreaPowerPlantConsoleOutput.setText(
											Main.getTheAircraft().getPowerPlant().toString()
											);
									
								}
							});	
							updateProgress(20, numberOfOperations);
							updateMessage("Logging Power Plant Object Data...");
							updateTitle(String.valueOf(progressIncrement*20) + "%");
						}
						if (Main.getTheAircraft().getLandingGears() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									textAreaLandingGearsConsoleOutput.setText(
											Main.getTheAircraft().getLandingGears().toString()
											);
									
								}
							});	
							updateProgress(21, numberOfOperations);
							updateMessage("Logging Landing Gears Object Data...");
							updateTitle(String.valueOf(progressIncrement*21) + "%");
						}
						//............................
						// COMPONENTS 3 VIEW CREATION
						//............................
						// aircraft
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								createAircraftTopView();
								
							}
						});	
						updateProgress(22, numberOfOperations);
						updateMessage("Creating Aircraft Top View...");
						updateTitle(String.valueOf(progressIncrement*22) + "%");
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								createAircraftSideView();
								
							}
						});	
						updateProgress(23, numberOfOperations);
						updateMessage("Creating Aircraft Side View...");
						updateTitle(String.valueOf(progressIncrement*23) + "%");
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								createAircraftFrontView();
								
							}
						});	
						updateProgress(24, numberOfOperations);
						updateMessage("Creating Aircraft Front View...");
						updateTitle(String.valueOf(progressIncrement*24) + "%");
						
						//............................
						// fuselage
						if (Main.getTheAircraft().getFuselage() != null) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									createFuselageTopView();

								}
							});	
							updateProgress(25, numberOfOperations);
							updateMessage("Creating Fuselage Top View...");
							updateTitle(String.valueOf(progressIncrement*25) + "%");
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									createFuselageSideView();

								}
							});	
							updateProgress(26, numberOfOperations);
							updateMessage("Creating Fuselage Side View...");
							updateTitle(String.valueOf(progressIncrement*26) + "%");
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									createFuselageFrontView();

								}
							});	
							updateProgress(27, numberOfOperations);
							updateMessage("Creating Fuselage Front View...");
							updateTitle(String.valueOf(progressIncrement*27) + "%");
						}
						//............................
						// cabin configuration
						if (Main.getTheAircraft().getCabinConfiguration() != null) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									createSeatMap();

								}
							});	
							updateProgress(28, numberOfOperations);
							updateMessage("Creating Seat Map...");
							updateTitle(String.valueOf(progressIncrement*28) + "%");
						}
						//............................
						// wing
						if (Main.getTheAircraft().getWing() != null) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									createWingPlanformView();

								}
							});	
							updateProgress(29, numberOfOperations);
							updateMessage("Creating Wing Planform View...");
							updateTitle(String.valueOf(progressIncrement*29) + "%");
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									createEquivalentWingView();

								}
							});	
							updateProgress(30, numberOfOperations);
							updateMessage("Creating Equivalent Wing View...");
							updateTitle(String.valueOf(progressIncrement*30) + "%");
						}
						//............................
						// hTail
						if (Main.getTheAircraft().getHTail() != null) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									createHTailPlanformView();

								}
							});	
							updateProgress(31, numberOfOperations);
							updateMessage("Creating HTail Planform View...");
							updateTitle(String.valueOf(progressIncrement*31) + "%");
						}
						//............................
						// vTail
						if (Main.getTheAircraft().getVTail() != null) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									createVTailPlanformView();

								}
							});	
							updateProgress(32, numberOfOperations);
							updateMessage("Creating VTail Planform View...");
							updateTitle(String.valueOf(progressIncrement*32) + "%");
						}
						//............................
						// canard
						if (Main.getTheAircraft().getCanard() != null) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									createCanardPlanformView();

								}
							});	
							updateProgress(33, numberOfOperations);
							updateMessage("Creating Canard Planform View...");
							updateTitle(String.valueOf(progressIncrement*33) + "%");
						}
						//............................
						// nacelle
						if (Main.getTheAircraft().getNacelles() != null) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									createNacelleTopView();

								}
							});	
							updateProgress(34, numberOfOperations);
							updateMessage("Creating Nacelles Top View...");
							updateTitle(String.valueOf(progressIncrement*34) + "%");
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									createNacelleSideView();

								}
							});	
							updateProgress(35, numberOfOperations);
							updateMessage("Creating Nacelles Side View...");
							updateTitle(String.valueOf(progressIncrement*35) + "%");
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									createNacelleFrontView();

								}
							});	
							updateProgress(36, numberOfOperations);
							updateMessage("Creating Nacelles FrontView...");
							updateTitle(String.valueOf(progressIncrement*36) + "%");	
						}

						updateProgress(37, numberOfOperations);
						updateMessage("Aircraft Updated!");
						updateTitle(String.valueOf(100) + "%");

						//Block the thread for a short time, but be sure
						//to check the InterruptedException for cancellation
						try {
							Thread.sleep(100);
						} catch (InterruptedException interrupted) {
							if (isCancelled()) {
								updateMessage("Cancelled");
								updateProgress(0, 4);
								updateTitle(String.valueOf(0) + "%");
								return null;
							}
							else {
								updateMessage("Terminated");
								updateProgress(0, 4);
								updateTitle(String.valueOf(0) + "%");
								return null;
							}
						}

			            Main.setAircraftSaved(false);
						Main.setAircraftUpdated(true);
			            
						ObjectProperty<Boolean> aircraftSavedFlag = new SimpleObjectProperty<>();

						try {
							aircraftSavedFlag.set(Main.getAircraftSaved());
							saveAircraftButton.disableProperty().bind(
									Bindings.equal(aircraftSavedFlag, true)
									);
						} catch (Exception e) {
							saveAircraftButton.setDisable(true);
						}
			            
						return null;
					}
				};
			}
		};
		
		Main.getProgressBar().progressProperty().unbind();
		Main.getStatusBar().textProperty().unbind();
		Main.getTaskPercentage().textProperty().unbind();
		
		Main.getProgressBar().progressProperty().bind(updateAircraftDataService.progressProperty());
		Main.getStatusBar().textProperty().bind(updateAircraftDataService.messageProperty());
		Main.getTaskPercentage().textProperty().bind(updateAircraftDataService.titleProperty());
		
		updateAircraftDataService.start();
		
	}
	
	@SuppressWarnings("unchecked")
	private void updateAircraftTabData() {
		
		//.................................................................................................
		// DATA INITIALIZATION
		//.................................................................................................
		String aircraftType = "";
		String aircraftRegulation = "";
		//.................................................................................................
		String cabinConfigurationFilePath = "";
		//.................................................................................................
		String fuselageFilePath = "";
		String fuselageXPosition = "";
		String fuselageXPositionUnit = "";
		String fuselageYPosition = "";
		String fuselageYPositionUnit = "";
		String fuselageZPosition = "";
		String fuselageZPositionUnit = "";
		//.................................................................................................
		String wingFilePath = "";
		String wingXPosition = "";
		String wingXPositionUnit = "";
		String wingYPosition = "";
		String wingYPositionUnit = "";
		String wingZPosition = "";
		String wingZPositionUnit = "";
		String wingRiggingAngle = "";
		String wingRiggingAngleUnit = "";
		//.................................................................................................
		String hTailFilePath = "";
		String hTailXPosition = "";
		String hTailXPositionUnit = "";
		String hTailYPosition = "";
		String hTailYPositionUnit = "";
		String hTailZPosition = "";
		String hTailZPositionUnit = "";
		String hTailRiggingAngle = "";
		String hTailRiggingAngleUnit = "";
		//.................................................................................................
		String vTailFilePath = "";
		String vTailXPosition = "";
		String vTailXPositionUnit = "";
		String vTailYPosition = "";
		String vTailYPositionUnit = "";
		String vTailZPosition = "";
		String vTailZPositionUnit = "";
		String vTailRiggingAngle = "";
		String vTailRiggingAngleUnit = "";
		//.................................................................................................
		String canardFilePath = "";
		String canardXPosition = "";
		String canardXPositionUnit = "";
		String canardYPosition = "";
		String canardYPositionUnit = "";
		String canardZPosition = "";
		String canardZPositionUnit = "";
		String canardRiggingAngle = "";
		String canardRiggingAngleUnit = "";
		//.................................................................................................
		List<String> engineFilePathList = new ArrayList<>();
		List<String> engineXPositionList = new ArrayList<>();
		List<String> engineXPositionUnitList = new ArrayList<>();
		List<String> engineYPositionList = new ArrayList<>();
		List<String> engineYPositionUnitList = new ArrayList<>();
		List<String> engineZPositionList = new ArrayList<>();
		List<String> engineZPositionUnitList = new ArrayList<>();
		List<String> engineTiltAngleList = new ArrayList<>();
		List<String> engineTiltAngleUnitList = new ArrayList<>();
		List<String> engineMountinPositionList = new ArrayList<>();
		//.................................................................................................
		List<String> nacelleFilePathList = new ArrayList<>();
		List<String> nacelleXPositionList = new ArrayList<>();
		List<String> nacelleXPositionUnitList = new ArrayList<>();
		List<String> nacelleYPositionList = new ArrayList<>();
		List<String> nacelleYPositionUnitList = new ArrayList<>();
		List<String> nacelleZPositionList = new ArrayList<>();
		List<String> nacelleZPositionUnitList = new ArrayList<>();
		List<String> nacelleMountinPositionList = new ArrayList<>();
		//.................................................................................................
		String landingGearsFilePath = "";
		String landingGearsXPosition = "";
		String landingGearsXPositionUnit = "";
		String landingGearsYPosition = "";
		String landingGearsYPositionUnit = "";
		String landingGearsZPosition = "";
		String landingGearsZPositionUnit = "";
		String landingGearsMountinPosition = "";
		//.................................................................................................
		String systemsFilePath = "";
		String systemsXPosition = "";
		String systemsXPositionUnit = "";
		String systemsYPosition = "";
		String systemsYPositionUnit = "";
		String systemsZPosition = "";
		String systemsZPositionUnit = "";
		
		//.................................................................................................
		// FETCHING DATA FROM GUI FIELDS ...
		//.................................................................................................
		if(!aircraftTypeChoiceBox.getSelectionModel().isEmpty())
			aircraftType = aircraftTypeChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(!regulationsTypeChoiceBox.getSelectionModel().isEmpty())
			aircraftRegulation = regulationsTypeChoiceBox.getSelectionModel().getSelectedItem().toString().replace('-', '_');
		//.................................................................................................
		if(textFieldAircraftCabinConfigurationFile.getText() != null)
			cabinConfigurationFilePath = textFieldAircraftCabinConfigurationFile.getText();
		//.................................................................................................
		if(textFieldAircraftFuselageFile.getText() != null)
			fuselageFilePath = textFieldAircraftFuselageFile.getText();
		if(textFieldAircraftFuselageX.getText() != null)
			fuselageXPosition = textFieldAircraftFuselageX.getText();
		if(!fuselageXUnitChoiceBox.getSelectionModel().isEmpty())
			fuselageXPositionUnit = fuselageXUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftFuselageY.getText() != null)
			fuselageYPosition = textFieldAircraftFuselageY.getText();
		if(!fuselageYUnitChoiceBox.getSelectionModel().isEmpty())
			fuselageYPositionUnit = fuselageYUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftFuselageZ.getText() != null)
			fuselageZPosition = textFieldAircraftFuselageZ.getText();
		if(!fuselageZUnitChoiceBox.getSelectionModel().isEmpty())
			fuselageZPositionUnit = fuselageZUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		//.................................................................................................
		if(textFieldAircraftWingFile.getText() != null)
			wingFilePath = textFieldAircraftWingFile.getText();
		if(textFieldAircraftWingX.getText() != null)
			wingXPosition = textFieldAircraftWingX.getText();
		if(!wingXUnitChoiceBox.getSelectionModel().isEmpty())
			wingXPositionUnit = wingXUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftWingY.getText() != null)
			wingYPosition = textFieldAircraftWingY.getText();
		if(!wingYUnitChoiceBox.getSelectionModel().isEmpty())
			wingYPositionUnit = wingYUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftWingZ.getText() != null)
			wingZPosition = textFieldAircraftWingZ.getText();
		if(!wingZUnitChoiceBox.getSelectionModel().isEmpty())
			wingZPositionUnit = wingZUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftWingRiggingAngle.getText() != null)
			wingRiggingAngle = textFieldAircraftWingRiggingAngle.getText();
		if(!wingRiggingAngleUnitChoiceBox.getSelectionModel().isEmpty())
			wingRiggingAngleUnit = wingRiggingAngleUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		//.................................................................................................
		if(textFieldAircraftHTailFile.getText() != null)
			hTailFilePath = textFieldAircraftHTailFile.getText();
		if(textFieldAircraftHTailX.getText() != null)
			hTailXPosition = textFieldAircraftHTailX.getText();
		if(!hTailXUnitChoiceBox.getSelectionModel().isEmpty())
			hTailXPositionUnit = hTailXUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftHTailY.getText() != null)
			hTailYPosition = textFieldAircraftHTailY.getText();
		if(!hTailYUnitChoiceBox.getSelectionModel().isEmpty())
			hTailYPositionUnit = hTailYUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftHTailZ.getText() != null)
			hTailZPosition = textFieldAircraftHTailZ.getText();
		if(!htailZUnitChoiceBox.getSelectionModel().isEmpty())
			hTailZPositionUnit = htailZUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftHTailRiggingAngle.getText() != null)
			hTailRiggingAngle = textFieldAircraftHTailRiggingAngle.getText();
		if(!hTailRiggingAngleUnitChoiceBox.getSelectionModel().isEmpty())
			hTailRiggingAngleUnit = hTailRiggingAngleUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		//.................................................................................................
		if(textFieldAircraftVTailFile.getText() != null)
			vTailFilePath = textFieldAircraftVTailFile.getText();
		if(textFieldAircraftVTailX.getText() != null)
			vTailXPosition = textFieldAircraftVTailX.getText();
		if(!vTailXUnitChoiceBox.getSelectionModel().isEmpty())
			vTailXPositionUnit = vTailXUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftVTailY.getText() != null)
			vTailYPosition = textFieldAircraftVTailY.getText();
		if(!vTailYUnitChoiceBox.getSelectionModel().isEmpty())
			vTailYPositionUnit = vTailYUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftVTailZ.getText() != null)
			vTailZPosition = textFieldAircraftVTailZ.getText();
		if(!vTailZUnitChoiceBox.getSelectionModel().isEmpty())
			vTailZPositionUnit = vTailZUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftVTailRiggingAngle.getText() != null)
			vTailRiggingAngle = textFieldAircraftVTailRiggingAngle.getText();
		if(!vTailRiggingAngleUnitChoiceBox.getSelectionModel().isEmpty())
			vTailRiggingAngleUnit = vTailRiggingAngleUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		//.................................................................................................
		if(textFieldAircraftCanardFile.getText() != null)
			canardFilePath = textFieldAircraftCanardFile.getText();
		if(textFieldAircraftCanardX.getText() != null)
			canardXPosition = textFieldAircraftCanardX.getText();
		if(!canardXUnitChoiceBox.getSelectionModel().isEmpty())
			canardXPositionUnit = canardXUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftCanardY.getText() != null)
			canardYPosition = textFieldAircraftCanardY.getText();
		if(!canardYUnitChoiceBox.getSelectionModel().isEmpty())
			canardYPositionUnit = canardYUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftCanardZ.getText() != null)
			canardZPosition = textFieldAircraftCanardZ.getText();
		if(!canardZUnitChoiceBox.getSelectionModel().isEmpty())
			canardZPositionUnit = canardZUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftCanardRiggingAngle.getText() != null)
			canardRiggingAngle = textFieldAircraftCanardRiggingAngle.getText();
		if(!canardRiggingAngleUnitChoiceBox.getSelectionModel().isEmpty())
			canardRiggingAngleUnit = canardRiggingAngleUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		//.................................................................................................
		if(!textFieldsAircraftEngineFileList.isEmpty())
			textFieldsAircraftEngineFileList.stream().forEach(tf -> engineFilePathList.add(tf.getText()));
		if(!textFieldAircraftEngineXList.isEmpty())
			textFieldAircraftEngineXList.stream().forEach(tf -> engineXPositionList.add(tf.getText()));
		if(!choiceBoxAircraftEngineXUnitList.isEmpty())
			choiceBoxAircraftEngineXUnitList.stream().forEach(cb -> engineXPositionUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!textFieldAircraftEngineYList.isEmpty())
			textFieldAircraftEngineYList.stream().forEach(tf -> engineYPositionList.add(tf.getText()));
		if(!choiceBoxAircraftEngineYUnitList.isEmpty())
			choiceBoxAircraftEngineYUnitList.stream().forEach(cb -> engineYPositionUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!textFieldAircraftEngineZList.isEmpty())
			textFieldAircraftEngineZList.stream().forEach(tf -> engineZPositionList.add(tf.getText()));
		if(!choiceBoxAircraftEngineZUnitList.isEmpty())
			choiceBoxAircraftEngineZUnitList.stream().forEach(cb -> engineZPositionUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!textFieldAircraftEngineTiltList.isEmpty())
			textFieldAircraftEngineTiltList.stream().forEach(tf -> engineTiltAngleList.add(tf.getText()));
		if(!choiceBoxAircraftEngineTiltUnitList.isEmpty())
			choiceBoxAircraftEngineTiltUnitList.stream().forEach(cb -> engineTiltAngleUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!choiceBoxesAircraftEnginePositonList.isEmpty())
			choiceBoxesAircraftEnginePositonList.stream().forEach(cb -> engineMountinPositionList.add(cb.getSelectionModel().getSelectedItem()));
		//.................................................................................................
		if(!textFieldsAircraftNacelleFileList.isEmpty())
			textFieldsAircraftNacelleFileList.stream().forEach(tf -> nacelleFilePathList.add(tf.getText()));
		if(!textFieldAircraftNacelleXList.isEmpty())
			textFieldAircraftNacelleXList.stream().forEach(tf -> nacelleXPositionList.add(tf.getText()));
		if(!choiceBoxAircraftNacelleXUnitList.isEmpty())
			choiceBoxAircraftNacelleXUnitList.stream().forEach(cb -> nacelleXPositionUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!textFieldAircraftNacelleYList.isEmpty())
			textFieldAircraftNacelleYList.stream().forEach(tf -> nacelleYPositionList.add(tf.getText()));
		if(!choiceBoxAircraftNacelleYUnitList.isEmpty())
			choiceBoxAircraftNacelleYUnitList.stream().forEach(cb -> nacelleYPositionUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!textFieldAircraftNacelleZList.isEmpty())
			textFieldAircraftNacelleZList.stream().forEach(tf -> nacelleZPositionList.add(tf.getText()));
		if(!choiceBoxAircraftNacelleZUnitList.isEmpty())
			choiceBoxAircraftNacelleZUnitList.stream().forEach(cb -> nacelleZPositionUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!choiceBoxesAircraftNacellePositonList.isEmpty())
			choiceBoxesAircraftNacellePositonList.stream().forEach(cb -> nacelleMountinPositionList.add(cb.getSelectionModel().getSelectedItem()));
		//.................................................................................................
		if(textFieldAircraftLandingGearsFile.getText() != null)
			landingGearsFilePath = textFieldAircraftLandingGearsFile.getText();
		if(textFieldAircraftLandingGearsX.getText() != null)
			landingGearsXPosition = textFieldAircraftLandingGearsX.getText();
		if(!landingGearsXUnitChoiceBox.getSelectionModel().isEmpty())
			landingGearsXPositionUnit = landingGearsXUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftLandingGearsY.getText() != null)
			landingGearsYPosition = textFieldAircraftLandingGearsY.getText();
		if(!landingGearsYUnitChoiceBox.getSelectionModel().isEmpty())
			landingGearsYPositionUnit = landingGearsYUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftLandingGearsZ.getText() != null)
			landingGearsZPosition = textFieldAircraftLandingGearsZ.getText();
		if(!landingGearsZUnitChoiceBox.getSelectionModel().isEmpty())
			landingGearsZPositionUnit = landingGearsZUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(!landingGearsMountingPositionTypeChoiceBox.getSelectionModel().isEmpty())
			landingGearsMountinPosition = landingGearsMountingPositionTypeChoiceBox.getSelectionModel().getSelectedItem().toString();
		//.................................................................................................
		if(textFieldAircraftSystemsFile.getText() != null)
			systemsFilePath = textFieldAircraftSystemsFile.getText();
		if(textFieldAircraftSystemsX.getText() != null)
			systemsXPosition = textFieldAircraftSystemsX.getText();
		if(!systemsXUnitChoiceBox.getSelectionModel().isEmpty())
			systemsXPositionUnit = systemsXUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftSystemsY.getText() != null)
			systemsYPosition = textFieldAircraftSystemsY.getText();
		if(!systemsYUnitChoiceBox.getSelectionModel().isEmpty())
			systemsYPositionUnit = systemsYUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldAircraftSystemsZ.getText() != null)
			systemsZPosition = textFieldAircraftSystemsZ.getText();
		if(!systemsZUnitChoiceBox.getSelectionModel().isEmpty())
			systemsZPositionUnit = systemsZUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		
		//.................................................................................................
		// SETTING ALL DATA INSIDE THE AIRCRAFT OBJECT ...
		//.................................................................................................
		Main.getTheAircraft().setTypeVehicle(AircraftTypeEnum.valueOf(aircraftType));
		Main.getTheAircraft().setRegulations(RegulationsEnum.valueOf(aircraftRegulation));
		//.................................................................................................
		Main.getTheAircraft().setCabinConfigurationFilePath(cabinConfigurationFilePath);
		//.................................................................................................
		if(Main.getTheAircraft().getFuselage() != null) {
			Main.getTheAircraft().setFuselageFilePath(fuselageFilePath);
			Main.getTheAircraft().getFuselage().setXApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(fuselageXPosition), Unit.valueOf(fuselageXPositionUnit))
					);
			Main.getTheAircraft().getFuselage().setYApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(fuselageYPosition), Unit.valueOf(fuselageYPositionUnit))
					);
			Main.getTheAircraft().getFuselage().setZApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(fuselageZPosition), Unit.valueOf(fuselageZPositionUnit))
					);
		}
		//.................................................................................................
		if(Main.getTheAircraft().getWing() != null) {
			Main.getTheAircraft().setWingFilePath(wingFilePath);
			Main.getTheAircraft().getWing().setXApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(wingXPosition), Unit.valueOf(wingXPositionUnit))
					);
			Main.getTheAircraft().getWing().setYApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(wingYPosition), Unit.valueOf(wingYPositionUnit))
					);
			Main.getTheAircraft().getWing().setZApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(wingZPosition), Unit.valueOf(wingZPositionUnit))
					);
			Main.getTheAircraft().getWing().setRiggingAngle(
					(Amount<Angle>) Amount.valueOf(Double.valueOf(wingRiggingAngle), Unit.valueOf(wingRiggingAngleUnit))
					);
		}
		//.................................................................................................
		if(Main.getTheAircraft().getHTail() != null) {
			Main.getTheAircraft().setHTailFilePath(hTailFilePath);
			Main.getTheAircraft().getHTail().setXApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(hTailXPosition), Unit.valueOf(hTailXPositionUnit))
					);
			Main.getTheAircraft().getHTail().setYApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(hTailYPosition), Unit.valueOf(hTailYPositionUnit))
					);
			Main.getTheAircraft().getHTail().setZApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(hTailZPosition), Unit.valueOf(hTailZPositionUnit))
					);
			Main.getTheAircraft().getHTail().setRiggingAngle(
					(Amount<Angle>) Amount.valueOf(Double.valueOf(hTailRiggingAngle), Unit.valueOf(hTailRiggingAngleUnit))
					);
		}
		//.................................................................................................
		if(Main.getTheAircraft().getVTail() != null) {
			Main.getTheAircraft().setVTailFilePath(vTailFilePath);
			Main.getTheAircraft().getVTail().setXApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(vTailXPosition), Unit.valueOf(vTailXPositionUnit))
					);
			Main.getTheAircraft().getVTail().setYApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(vTailYPosition), Unit.valueOf(vTailYPositionUnit))
					);
			Main.getTheAircraft().getVTail().setZApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(vTailZPosition), Unit.valueOf(vTailZPositionUnit))
					);
			Main.getTheAircraft().getVTail().setRiggingAngle(
					(Amount<Angle>) Amount.valueOf(Double.valueOf(vTailRiggingAngle), Unit.valueOf(vTailRiggingAngleUnit))
					);
		}
		//.................................................................................................
		if(Main.getTheAircraft().getCanard() != null) {
			
			Main.getTheAircraft().setCanardFilePath(canardFilePath);
			Main.getTheAircraft().getCanard().setXApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(canardXPosition), Unit.valueOf(canardXPositionUnit))
					);
			Main.getTheAircraft().getCanard().setYApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(canardYPosition), Unit.valueOf(canardYPositionUnit))
					);
			Main.getTheAircraft().getCanard().setZApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(canardZPosition), Unit.valueOf(canardZPositionUnit))
					);
			Main.getTheAircraft().getCanard().setRiggingAngle(
					(Amount<Angle>) Amount.valueOf(Double.valueOf(canardRiggingAngle), Unit.valueOf(canardRiggingAngleUnit))
					);
		}
		//.................................................................................................
		if(Main.getTheAircraft().getPowerPlant() != null) {
			Main.getTheAircraft().setEngineFilePathList(new ArrayList<>());
			Main.getTheAircraft().getEngineFilePathList().addAll(engineFilePathList);
			for(int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				Engine currentEngine = Main.getTheAircraft().getPowerPlant().getEngineList().get(i);

				currentEngine.setXApexConstructionAxes(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(engineXPositionList.get(i)), 
								Unit.valueOf(engineXPositionUnitList.get(i))
								)
						);
				currentEngine.setYApexConstructionAxes(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(engineYPositionList.get(i)), 
								Unit.valueOf(engineYPositionUnitList.get(i))
								)
						);
				currentEngine.setZApexConstructionAxes(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(engineZPositionList.get(i)), 
								Unit.valueOf(engineZPositionUnitList.get(i))
								)
						);
				currentEngine.setTiltingAngle(
						(Amount<Angle>) Amount.valueOf(
								Double.valueOf(engineTiltAngleList.get(i)), 
								Unit.valueOf(engineTiltAngleUnitList.get(i))
								)
						);
				currentEngine.setMountingPosition(EngineMountingPositionEnum.valueOf(engineMountinPositionList.get(i)));
			}
		}
		//.................................................................................................
		if(Main.getTheAircraft().getNacelles() != null) {
			Main.getTheAircraft().setNacelleFilePathList(new ArrayList<>());
			Main.getTheAircraft().getNacelleFilePathList().addAll(nacelleFilePathList);
			for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

				NacelleCreator currentNacelle = Main.getTheAircraft().getNacelles().getNacellesList().get(i);

				currentNacelle.setXApexConstructionAxes(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(nacelleXPositionList.get(i)), 
								Unit.valueOf(nacelleXPositionUnitList.get(i))
								)
						);
				currentNacelle.setYApexConstructionAxes(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(nacelleYPositionList.get(i)), 
								Unit.valueOf(nacelleYPositionUnitList.get(i))
								)
						);
				currentNacelle.setZApexConstructionAxes(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(nacelleZPositionList.get(i)), 
								Unit.valueOf(nacelleZPositionUnitList.get(i))
								)
						);
				currentNacelle.setMountingPosition(MountingPosition.valueOf(nacelleMountinPositionList.get(i)));
			}
		}
		//.................................................................................................
		if(Main.getTheAircraft().getLandingGears() != null) {
			Main.getTheAircraft().setLandingGearsFilePath(landingGearsFilePath);
			Main.getTheAircraft().getLandingGears().setXApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(landingGearsXPosition), Unit.valueOf(landingGearsXPositionUnit))
					);
			Main.getTheAircraft().getLandingGears().setYApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(landingGearsYPosition), Unit.valueOf(landingGearsYPositionUnit))
					);
			Main.getTheAircraft().getLandingGears().setZApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(landingGearsZPosition), Unit.valueOf(landingGearsZPositionUnit))
					);
			Main.getTheAircraft().getLandingGears().setMountingPosition(aircraft.components.LandingGears.MountingPosition.valueOf(landingGearsMountinPosition));
		}
		//.................................................................................................
		if(Main.getTheAircraft().getSystems() != null) {
			Main.getTheAircraft().setSystemsFilePath(systemsFilePath);
			Main.getTheAircraft().getSystems().setXApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(systemsXPosition), Unit.valueOf(systemsXPositionUnit))
					);
			Main.getTheAircraft().getSystems().setYApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(systemsYPosition), Unit.valueOf(systemsYPositionUnit))
					);
			Main.getTheAircraft().getSystems().setZApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(systemsZPosition), Unit.valueOf(systemsZPositionUnit))
					);
		}
	}
	
	private void updateFuselageTabData() {
		
		// TODO: AFTER MATHCING ADJUST CRITERION WITH THE DATA MODEL
		
	}
	
	@SuppressWarnings({ "unchecked" })
	private void updateCabinConfigurationTabData() {
		
		//.................................................................................................
		// DATA INITIALIZATION
		//.................................................................................................
		String actualPassengerNumber = "";
		String maximumPassengerNumber = "";
		String flightCrewNumber = "";
		String classesNumber = "";
		List<String> classesType = new ArrayList<>();
		String aislesNumber = "";
		String xCoordinateFirstRow = "";
		String xCoordinateFirstRowUnit = "";
		List<String> missingSeaRows = new ArrayList<>();
		//.................................................................................................
		String numberOfBrakesEconomyClass = "";
		String numberOfBrakesBusinessClass = "";
		String numberOfBrakesFirstClass = "";
		String numberOfRowsEconomyClass = "";
		String numberOfRowsBusinessClass = "";
		String numberOfRowsFirstClass = "";
		String numberOfColumnsEconomyClass = "";
		String numberOfColumnsBusinessClass = "";
		String numberOfColumnsFirstClass = "";
		String seatsPitchEconomyClass = "";
		String seatsPitchBusinessClass = "";
		String seatsPitchFirstClass = "";
		String seatsPitchEconomyClassUnit = "";
		String seatsPitchBusinessClassUnit = "";
		String seatsPitchFirstClassUnit = "";
		String seatsWidthEconomyClass = "";
		String seatsWidthBusinessClass = "";
		String seatsWidthFirstClass = "";
		String seatsWidthEconomyClassUnit = "";
		String seatsWidthBusinessClassUnit = "";
		String seatsWidthFirstClassUnit = "";
		String distanceFromWallEconomyClass = "";
		String distanceFromWallBusinessClass = "";
		String distanceFromWallFirstClass = "";
		String distanceFromWallEconomyClassUnit = "";
		String distanceFromWallBusinessClassUnit = "";
		String distanceFromWallFirstClassUnit = "";
		//.................................................................................................
		String referenceMassFurnishingsAndEquipments = "";
		String referenceMassFurnishingsAndEquipmentsUnit = "";
		
		//.................................................................................................
		// FETCHING DATA FROM GUI FIELDS ...
		//.................................................................................................
		if(textFieldActualPassengersNumber.getText() != null)
			actualPassengerNumber = textFieldActualPassengersNumber.getText();
		if(textFieldMaximumPassengersNumber.getText() != null)
			maximumPassengerNumber = textFieldMaximumPassengersNumber.getText();
		if(textFieldFlightCrewNumber.getText() != null)
			flightCrewNumber = textFieldFlightCrewNumber.getText();
		if(textFieldClassesNumber.getText() != null)
			classesNumber = textFieldClassesNumber.getText();
		if(!cabinConfigurationClassesTypeChoiceBox1.getSelectionModel().isEmpty())
			classesType.add(cabinConfigurationClassesTypeChoiceBox1.getSelectionModel().getSelectedItem().toString());
		if(!cabinConfigurationClassesTypeChoiceBox2.getSelectionModel().isEmpty())
			classesType.add(cabinConfigurationClassesTypeChoiceBox2.getSelectionModel().getSelectedItem().toString());
		if(!cabinConfigurationClassesTypeChoiceBox3.getSelectionModel().isEmpty())
			classesType.add(cabinConfigurationClassesTypeChoiceBox3.getSelectionModel().getSelectedItem().toString());
		if(textFieldAislesNumber.getText() != null)
			aislesNumber = textFieldAislesNumber.getText();
		if(textFieldXCoordinateFirstRow.getText() != null)
			xCoordinateFirstRow = textFieldXCoordinateFirstRow.getText();
		if(!cabinConfigurationXCoordinateFirstRowUnitChoiceBox.getSelectionModel().isEmpty())
			xCoordinateFirstRowUnit = cabinConfigurationXCoordinateFirstRowUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldMissingSeatRow1.getText() != null)
			missingSeaRows.add(textFieldMissingSeatRow1.getText());
		if(textFieldMissingSeatRow2.getText() != null)
			missingSeaRows.add(textFieldMissingSeatRow2.getText());
		if(textFieldMissingSeatRow3.getText() != null)
			missingSeaRows.add(textFieldMissingSeatRow3.getText());
		//.................................................................................................
		if(textFieldNumberOfBrakesEconomy.getText() != null)
			numberOfBrakesEconomyClass = textFieldNumberOfBrakesEconomy.getText();
		if(textFieldNumberOfBrakesBusiness.getText() != null)
			numberOfBrakesBusinessClass = textFieldNumberOfBrakesBusiness.getText();
		if(textFieldNumberOfBrakesFirst.getText() != null)
			numberOfBrakesFirstClass = textFieldNumberOfBrakesFirst.getText();
		if(textFieldNumberOfRowsEconomy.getText() != null)
			numberOfRowsEconomyClass = textFieldNumberOfRowsEconomy.getText();
		if(textFieldNumberOfRowsBusiness.getText() != null)
			numberOfRowsBusinessClass = textFieldNumberOfRowsBusiness.getText();
		if(textFieldNumberOfRowsFirst.getText() != null)
			numberOfRowsFirstClass = textFieldNumberOfRowsFirst.getText();
		if(textFieldNumberOfColumnsEconomy.getText() != null)
			numberOfColumnsEconomyClass = textFieldNumberOfColumnsEconomy.getText();
		if(textFieldNumberOfColumnsBusiness.getText() != null)
			numberOfColumnsBusinessClass = textFieldNumberOfColumnsBusiness.getText();
		if(textFieldNumberOfColumnsFirst.getText() != null)
			numberOfColumnsFirstClass = textFieldNumberOfColumnsFirst.getText();
		if(textFieldSeatsPitchEconomy.getText() != null)
			seatsPitchEconomyClass = textFieldSeatsPitchEconomy.getText();
		if(textFieldSeatsPitchBusiness.getText() != null)
			seatsPitchBusinessClass = textFieldSeatsPitchBusiness.getText();
		if(textFieldSeatsPitchFirst.getText() != null)
			seatsPitchFirstClass = textFieldSeatsPitchFirst.getText();
		if(!cabinConfigurationSeatsPitchEconomyUnitChoiceBox.getSelectionModel().isEmpty())
			seatsPitchEconomyClassUnit = cabinConfigurationSeatsPitchEconomyUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(!cabinConfigurationSeatsPitchBusinessUnitChoiceBox.getSelectionModel().isEmpty())
			seatsPitchBusinessClassUnit = cabinConfigurationSeatsPitchBusinessUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(!cabinConfigurationSeatsPitchFirstUnitChoiceBox.getSelectionModel().isEmpty())
			seatsPitchFirstClassUnit = cabinConfigurationSeatsPitchFirstUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldSeatsWidthEconomy.getText() != null)
			seatsWidthEconomyClass = textFieldSeatsWidthEconomy.getText();
		if(textFieldSeatsWidthBusiness.getText() != null)
			seatsWidthBusinessClass = textFieldSeatsWidthBusiness.getText();
		if(textFieldSeatsWidthFirst.getText() != null)
			seatsWidthFirstClass = textFieldSeatsWidthFirst.getText();
		if(!cabinConfigurationSeatsWidthEconomyUnitChoiceBox.getSelectionModel().isEmpty())
			seatsWidthEconomyClassUnit = cabinConfigurationSeatsWidthEconomyUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(!cabinConfigurationSeatsWidthBusinessUnitChoiceBox.getSelectionModel().isEmpty())
			seatsWidthBusinessClassUnit = cabinConfigurationSeatsWidthBusinessUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(!cabinConfigurationSeatsWidthFirstUnitChoiceBox.getSelectionModel().isEmpty())
			seatsWidthFirstClassUnit = cabinConfigurationSeatsWidthFirstUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(textFieldDistanceFromWallEconomy.getText() != null)
			distanceFromWallEconomyClass = textFieldDistanceFromWallEconomy.getText();
		if(textFieldDistanceFromWallBusiness.getText() != null)
			distanceFromWallBusinessClass = textFieldDistanceFromWallBusiness.getText();
		if(textFieldDistanceFromWallFirst.getText() != null)
			distanceFromWallFirstClass = textFieldDistanceFromWallFirst.getText();
		if(!cabinConfigurationDistanceFromWallEconomyUnitChoiceBox.getSelectionModel().isEmpty())
			distanceFromWallEconomyClassUnit = cabinConfigurationDistanceFromWallEconomyUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(!cabinConfigurationDistanceFromWallBusinessUnitChoiceBox.getSelectionModel().isEmpty())
			distanceFromWallBusinessClassUnit = cabinConfigurationDistanceFromWallBusinessUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		if(!cabinConfigurationDistanceFromWallFirstUnitChoiceBox.getSelectionModel().isEmpty())
			distanceFromWallFirstClassUnit = cabinConfigurationDistanceFromWallFirstUnitChoiceBox.getSelectionModel().getSelectedItem().toString();
		//.................................................................................................
		if(textFieldMassFurnishingsAndEquipment.getText() != null)
			referenceMassFurnishingsAndEquipments = textFieldMassFurnishingsAndEquipment.getText();
		if(!cabinConfigurationMassFurnishingsAndEquipmentUnitChoiceBox.getSelectionModel().isEmpty())
			referenceMassFurnishingsAndEquipmentsUnit = cabinConfigurationMassFurnishingsAndEquipmentUnitChoiceBox.getSelectionModel().getSelectedItem().toString();

		//.................................................................................................
		// SETTING ALL DATA INSIDE THE AIRCRAFT OBJECT ...
		//.................................................................................................
		List<List<String>> missingSeatsRowSplitList = new ArrayList<>(); 
		missingSeaRows.stream()
		.filter(str -> !str.isEmpty())
		.forEach(str -> {
			String currentString = str.trim();
			currentString = currentString.replace("[", "");
			currentString = currentString.replace("]", "");
			currentString = currentString.replace(",", ";");
			missingSeatsRowSplitList.add(Arrays.asList(currentString.split(";")));
		});
		List<Integer[]> missingSeatsRowsFinal = new ArrayList<>();
		missingSeatsRowSplitList.stream().forEach(list -> {
			
			List<Integer> integerList = new ArrayList<>();
			list.stream().forEach(element -> integerList.add(Integer.valueOf(element.trim())));
			missingSeatsRowsFinal.add(MyArrayUtils.convertListOfIntegerToIntegerArray(integerList));
			
		});
		
		List<String> numberOfColumnsEconomyClassSplitList = new ArrayList<>(); 
		String numberOfColumnsEconomyClassString = numberOfColumnsEconomyClass.trim();
		numberOfColumnsEconomyClassString = numberOfColumnsEconomyClassString.replace("[", "");
		numberOfColumnsEconomyClassString = numberOfColumnsEconomyClassString.replace("]", "");
		numberOfColumnsEconomyClassString = numberOfColumnsEconomyClassString.replace(",", ";");
		numberOfColumnsEconomyClassSplitList = Arrays.asList(numberOfColumnsEconomyClassString.split(";"));
		Integer[] numberOfColumnsEconomyClassArray = new Integer[numberOfColumnsEconomyClassSplitList.size()];
		for (int i=0; i<numberOfColumnsEconomyClassSplitList.size(); i++)
			numberOfColumnsEconomyClassArray[i] = Integer.valueOf(numberOfColumnsEconomyClassSplitList.get(i).trim());
		
		List<String> numberOfColumnsBusinessClassSplitList = new ArrayList<>();
		String numberOfColumnsBusinessClassString = numberOfColumnsBusinessClass.trim();
		numberOfColumnsBusinessClassString = numberOfColumnsBusinessClassString.replace("[", "");
		numberOfColumnsBusinessClassString = numberOfColumnsBusinessClassString.replace("]", "");
		numberOfColumnsBusinessClassString = numberOfColumnsBusinessClassString.replace(",", ";");
		numberOfColumnsBusinessClassSplitList = Arrays.asList(numberOfColumnsBusinessClassString.split(";"));
		Integer[] numberOfColumnsBusinessClassArray = new Integer[numberOfColumnsBusinessClassSplitList.size()];
		for (int i=0; i<numberOfColumnsBusinessClassSplitList.size(); i++)
			numberOfColumnsBusinessClassArray[i] = Integer.valueOf(numberOfColumnsBusinessClassSplitList.get(i).trim());
		
		List<String> numberOfColumnsFirstClassSplitList = new ArrayList<>();
		String numberOfColumnsFirstClassString = numberOfColumnsFirstClass.trim();
		numberOfColumnsFirstClassString = numberOfColumnsFirstClassString.replace("[", "");
		numberOfColumnsFirstClassString = numberOfColumnsFirstClassString.replace("]", "");
		numberOfColumnsFirstClassString = numberOfColumnsFirstClassString.replace(",", ";");
		numberOfColumnsFirstClassSplitList = Arrays.asList(numberOfColumnsFirstClassString.split(";"));
		Integer[] numberOfColumnsFirstClassArray = new Integer[numberOfColumnsFirstClassSplitList.size()];
		for (int i=0; i<numberOfColumnsFirstClassSplitList.size(); i++)
			numberOfColumnsFirstClassArray[i] = Integer.valueOf(numberOfColumnsFirstClassSplitList.get(i).trim());
		
		CabinConfiguration aircraftCabinConfiguration = new ConfigurationBuilder("Cabin Configuration - " + Main.getTheAircraft().getId())
				.nPax(Integer.valueOf(actualPassengerNumber))
				.maxPax(Integer.valueOf(maximumPassengerNumber))
				.flightCrewNumber(Integer.valueOf(flightCrewNumber))
				.classesNumber(Integer.valueOf(classesNumber))
				.typeList(classesType.stream()
						.map(string -> ClassTypeEnum.valueOf(string))
						.collect(Collectors.toList())
						)
				.aislesNumber(Integer.valueOf(aislesNumber))
				.xCoordinateFirstRow(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(xCoordinateFirstRow),
								Unit.valueOf(xCoordinateFirstRowUnit)
								)
						)
				.missingSeatsRowList(missingSeatsRowsFinal)
				.numberOfBreaksEconomyClass(Integer.valueOf(numberOfBrakesEconomyClass))
				.numberOfBreaksBusinessClass(Integer.valueOf(numberOfBrakesBusinessClass))
				.numberOfBreaksFirstClass(Integer.valueOf(numberOfBrakesFirstClass))
				.numberOfRowsEconomyClass(Integer.valueOf(numberOfRowsEconomyClass))
				.numberOfRowsBusinessClass(Integer.valueOf(numberOfRowsBusinessClass))
				.numberOfRowsFirstClass(Integer.valueOf(numberOfRowsFirstClass))
				.numberOfColumnsEconomyClass(numberOfColumnsEconomyClassArray)
				.numberOfColumnsBusinessClass(numberOfColumnsBusinessClassArray)
				.numberOfColumnsFirstClass(numberOfColumnsFirstClassArray)
				.pitchEconomyClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(seatsPitchEconomyClass),
								Unit.valueOf(seatsPitchEconomyClassUnit)
								)
						)
				.pitchBusinessClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(seatsPitchBusinessClass),
								Unit.valueOf(seatsPitchBusinessClassUnit)
								)
						)
				.pitchFirstClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(seatsPitchFirstClass),
								Unit.valueOf(seatsPitchFirstClassUnit)
								)
						)
				.widthEconomyClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(seatsWidthEconomyClass),
								Unit.valueOf(seatsWidthEconomyClassUnit)
								)
						)
				.widthBusinessClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(seatsWidthBusinessClass),
								Unit.valueOf(seatsWidthBusinessClassUnit)
								)
						)
				.widthFirstClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(seatsWidthFirstClass),
								Unit.valueOf(seatsWidthFirstClassUnit)
								)
						)
				.distanceFromWallEconomyClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(distanceFromWallEconomyClass),
								Unit.valueOf(distanceFromWallEconomyClassUnit)
								)
						)
				.distanceFromWallBusinessClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(distanceFromWallBusinessClass),
								Unit.valueOf(distanceFromWallBusinessClassUnit)
								)
						)
				.distanceFromWallFirstClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(distanceFromWallFirstClass),
								Unit.valueOf(distanceFromWallFirstClassUnit)
								)
						)
				.massFurnishingsAndEquipmentReference(
						(Amount<Mass>) Amount.valueOf(
								Double.valueOf(referenceMassFurnishingsAndEquipments),
								Unit.valueOf(referenceMassFurnishingsAndEquipmentsUnit)
								)
						)
				.build();
		
		Main.getTheAircraft().setCabinConfiguration(aircraftCabinConfiguration);

	}

	private void updateWingTabData() {
		
		// TODO: AFTER MATHCING ADJUST CRITERION WITH THE DATA MODEL
		
	}
	
	private void updateHTailTabData() {
		
		// TODO: AFTER MATHCING ADJUST CRITERION WITH THE DATA MODEL
		
	}
	
	private void updateVTailTabData() {
		
		// TODO: AFTER MATHCING ADJUST CRITERION WITH THE DATA MODEL
		
	}
	
	private void updateCanardTabData() {
		
		// TODO: AFTER MATHCING ADJUST CRITERION WITH THE DATA MODEL
		
	}
	
	private void updateNacelleTabData() {
		
		// TODO: CONTIUNE FROM HERE
		
	}
	
	private void updatePowerPlantTabData() {
		
		
		
	}
	
	private void updateLandingGearsTabData() {
		
		
		
	}
	
	private void createAircraftObjectFromData() {
		
		/*
		 * TODO: IF THE FILE PATH ARE FILLED, OPEN A DIALOG TO ASK THE USER TO CHOOSE BETWEEN FILE DATA AND 
		 *       TEXTFIELDS DATA FOR EACH COMPONENTS (USE CHECKBOXES) 
		 */
		
	}
	
	@FXML
	private void saveAircraftToFile() throws IOException {
		
		if (Main.getTheAircraft() != null) {
			
			if (Main.getAircraftUpdated()) {
				
				saveAircraftToFileImplementation();
				
			}
			else {
				
				//..................................................................................
				// NOT UPDATED AIRCRAFT DATA WARNING
				Stage saveAircraftDataWarning = new Stage();
				
				saveAircraftDataWarning.setTitle("Not Updated Aircraft Warning");
				saveAircraftDataWarning.initModality(Modality.WINDOW_MODAL);
				saveAircraftDataWarning.initStyle(StageStyle.UNDECORATED);
				saveAircraftDataWarning.initOwner(Main.getPrimaryStage());

				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(Main.class.getResource("inputmanager/SaveAircraftDataWarningNoUpdate.fxml"));
				BorderPane saveAircraftDataWarningBorderPane = loader.load();
				
				Button continueButton = (Button) saveAircraftDataWarningBorderPane.lookup("#warningContinueButton");
				continueButton.setOnAction(new EventHandler<ActionEvent>() {
					
					@Override
					public void handle(ActionEvent arg0) {
						saveAircraftDataWarning.close();
					}
					
				});
				
				Button ignoreButton = (Button) saveAircraftDataWarningBorderPane.lookup("#warningIgnoreButton");
				ignoreButton.setOnAction(new EventHandler<ActionEvent>() {
					
					@Override
					public void handle(ActionEvent arg0) {
						saveAircraftDataWarning.close();
						try {
							saveAircraftToFileImplementation();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
				});
				
				Scene scene = new Scene(saveAircraftDataWarningBorderPane);
				saveAircraftDataWarning.setScene(scene);
				saveAircraftDataWarning.sizeToScene();
		        saveAircraftDataWarning.show();
				
			}
		}
		else {
			
			//..................................................................................
			// NULL AIRCRAFT DATA WARNING
			Stage saveAircraftDataWarning = new Stage();
			
			saveAircraftDataWarning.setTitle("Null Aircraft Warning");
			saveAircraftDataWarning.initModality(Modality.WINDOW_MODAL);
			saveAircraftDataWarning.initStyle(StageStyle.UNDECORATED);
			saveAircraftDataWarning.initOwner(Main.getPrimaryStage());

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("inputmanager/SaveAircraftDataWarningNoAircraft.fxml"));
			BorderPane saveAircraftDataWarningBorderPane = loader.load();
			
			Button continueButton = (Button) saveAircraftDataWarningBorderPane.lookup("#warningContinueButton");
			continueButton.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent arg0) {
					saveAircraftDataWarning.close();
				}
				
			});
			
			Scene scene = new Scene(saveAircraftDataWarningBorderPane);
			saveAircraftDataWarning.setScene(scene);
			saveAircraftDataWarning.sizeToScene();
	        saveAircraftDataWarning.show();
			
		}
		
		System.gc();
		
	}
	
	@SuppressWarnings("rawtypes")
	private void saveAircraftToFileImplementation() throws IOException {
		
		saveAircraftFileChooser = new FileChooser();
		saveAircraftFileChooser.setTitle("Save as ...");
		saveAircraftFileChooser.setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() 
						+ File.separator 
						+ "Template_Aircraft"
						+ File.separator 
						)
				);

		File file = saveAircraftFileChooser.showSaveDialog(null);
		
		Service saveAircraftService = new Service() {

			@Override
			protected Task createTask() {
				return new Task() {

					@Override
					protected Object call() throws Exception {

						updateProgress(1, 4);
						updateMessage("Saving Aircraft to File ...");
						updateTitle(String.valueOf(25) + "%");

						if (file != null) {

							List<String> wingAirfoilsName = new ArrayList<>();
							List<String> hTailAirfoilsName = new ArrayList<>();
							List<String> vTailAirfoilsName = new ArrayList<>();
							List<String> canardAirfoilsName = new ArrayList<>();

							if(Main.getTheAircraft().getWing() != null) 
								wingAirfoilsName.addAll(
										Main.getTheAircraft().getWing().getAirfoilList().stream()
										.map(a -> a.getAirfoilCreator().getName() + ".xml")
										.collect(Collectors.toList())
										);
							if(Main.getTheAircraft().getHTail() != null) 
								hTailAirfoilsName.addAll(
										Main.getTheAircraft().getHTail().getAirfoilList().stream()
										.map(a -> a.getAirfoilCreator().getName() + ".xml")
										.collect(Collectors.toList())
										);
							if(Main.getTheAircraft().getVTail() != null) 
								vTailAirfoilsName.addAll(
										Main.getTheAircraft().getVTail().getAirfoilList().stream()
										.map(a -> a.getAirfoilCreator().getName() + ".xml")
										.collect(Collectors.toList())
										);
							if(Main.getTheAircraft().getCanard() != null) 
								canardAirfoilsName.addAll(
										Main.getTheAircraft().getCanard().getAirfoilList().stream()
										.map(a -> a.getAirfoilCreator().getName() + ".xml")
										.collect(Collectors.toList())
										);

							updateProgress(2, 4);
							updateMessage("Creating airfoil file name lists ...");
							updateTitle(String.valueOf(50) + "%");
							
							AircraftSaveDirectives asd = new AircraftSaveDirectives
									.Builder("_" + file.getName())
									.setAircraftFileName("aircraft_" + file.getName() + ".xml")
									.addAllWingAirfoilFileNames(wingAirfoilsName)
									.addAllHTailAirfoilFileNames(hTailAirfoilsName)
									.addAllVTailAirfoilFileNames(vTailAirfoilsName)
									.addAllCanardAirfoilFileNames(canardAirfoilsName)
									.build();

							updateProgress(3, 4);
							updateMessage("Creating Aircraft Save Directives ...");
							updateTitle(String.valueOf(75) + "%");
							
							JPADStaticWriteUtils.saveAircraftToXML(
									Main.getTheAircraft(), 
									file.getParent() + File.separator, 
									file.getName(), 
									asd);

							updateProgress(4, 4);
							updateMessage("Aircraft saved!");
							updateTitle(String.valueOf(100) + "%");
							
							Main.setAircraftSaved(true);
							
							ObjectProperty<Boolean> aircraftSavedFlag = new SimpleObjectProperty<>();

							try {
								aircraftSavedFlag.set(Main.getAircraftSaved());
								saveAircraftButton.disableProperty().bind(
										Bindings.equal(aircraftSavedFlag, true)
										);
							} catch (Exception e) {
								saveAircraftButton.setDisable(true);
							}
						}
						else 
							cancel();
						
						//Block the thread for a short time, but be sure
			            //to check the InterruptedException for cancellation
			            try {
			                Thread.sleep(100);
			            } catch (InterruptedException interrupted) {
			                if (isCancelled()) {
			                    updateMessage("Cancelled");
			                    updateProgress(0, 4);
								updateTitle(String.valueOf(0) + "%");
			                    return null;
			                }
			                else {
			                	updateMessage("Terminated");
			                	updateProgress(0, 4);
								updateTitle(String.valueOf(0) + "%");
			                    return null;
			                }
			            }
						
						return null;
					}
				};
			}
		};
		
		Main.getProgressBar().progressProperty().unbind();
		Main.getStatusBar().textProperty().unbind();
		Main.getTaskPercentage().textProperty().unbind();
		
		Main.getProgressBar().progressProperty().bind(saveAircraftService.progressProperty());
		Main.getStatusBar().textProperty().bind(saveAircraftService.messageProperty());
		Main.getTaskPercentage().textProperty().bind(saveAircraftService.titleProperty());
		
		saveAircraftService.start();
		
	}

	private void createAircraftTopView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			// left curve, upperview
			List<Amount<Length>> vX1Left = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountX().stream().forEach(x -> vX1Left.add(x));
			int nX1Left = vX1Left.size();
			List<Amount<Length>> vY1Left = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountY().stream().forEach(y -> vY1Left.add(y));

			// right curve, upperview
			List<Amount<Length>> vX2Right = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountX().stream().forEach(x -> vX2Right.add(x));;
			int nX2Right = vX2Right.size();
			List<Amount<Length>> vY2Right = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountY().stream().forEach(y -> vY2Right.add(y));;

			IntStream.range(0, nX1Left)
			.forEach(i -> {
				seriesFuselageCurve.add(vX1Left.get(i).doubleValue(SI.METRE), vY1Left.get(i).doubleValue(SI.METRE));
			});
			IntStream.range(0, nX2Right)
			.forEach(i -> {
				seriesFuselageCurve.add(vX2Right.get(vX2Right.size()-1-i).doubleValue(SI.METRE), vY2Right.get(vY2Right.size()-1-i).doubleValue(SI.METRE));
			});
		}
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		XYSeries seriesWingTopView = new XYSeries("Wing - Top View", false);
		
		if (Main.getTheAircraft().getWing() != null) {
			Double[][] dataTopViewIsolated = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.WING);

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
		}
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		XYSeries seriesHTailTopView = new XYSeries("HTail - Top View", false);
		
		if (Main.getTheAircraft().getHTail() != null) {
			Double[][] dataTopViewIsolatedHTail = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.HORIZONTAL_TAIL);

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
		}
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		XYSeries seriesVTailRootAirfoilTopView = new XYSeries("VTail Root - Top View", false);
		XYSeries seriesVTailTipAirfoilTopView = new XYSeries("VTail Tip - Top View", false);
		
		if (Main.getTheAircraft().getVTail() != null) {

			Double[] vTailRootXCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(0).getAirfoilCreator().getXCoords();
			Double[] vTailRootYCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(0).getAirfoilCreator().getZCoords();
			Double[] vTailTipXCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(Main.getTheAircraft().getVTail().getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
			Double[] vTailTipYCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(Main.getTheAircraft().getVTail().getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
			int nPointsVTail = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedXle().size();

			IntStream.range(0, vTailRootXCoordinates.length)
			.forEach(i -> {
				seriesVTailRootAirfoilTopView.add(
						(vTailRootXCoordinates[i]*Main.getTheAircraft().getVTail().getChordRoot().getEstimatedValue()) + Main.getTheAircraft().getVTail().getXApexConstructionAxes().getEstimatedValue(),
						(vTailRootYCoordinates[i]*Main.getTheAircraft().getVTail().getChordRoot().getEstimatedValue())
						);
			});

			IntStream.range(0, vTailTipXCoordinates.length)
			.forEach(i -> {
				seriesVTailTipAirfoilTopView.add(
						(vTailTipXCoordinates[i]*Main.getTheAircraft().getVTail().getChordTip().getEstimatedValue()) 
						+ Main.getTheAircraft().getVTail().getXApexConstructionAxes().getEstimatedValue()
						+ Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsVTail-1).getEstimatedValue(),
						(vTailTipYCoordinates[i]*Main.getTheAircraft().getVTail().getChordTip().getEstimatedValue())
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from canard discretization
		//--------------------------------------------------
		XYSeries seriesCanardTopView = new XYSeries("Canard - Top View", false);
		
		if (Main.getTheAircraft().getCanard() != null) {
			Double[][] dataTopViewIsolatedCanard = Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.CANARD);

			IntStream.range(0, dataTopViewIsolatedCanard.length)
			.forEach(i -> {
				seriesCanardTopView.add(
						dataTopViewIsolatedCanard[i][1] + Main.getTheAircraft().getCanard().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewIsolatedCanard[i][0] + Main.getTheAircraft().getCanard().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
			IntStream.range(0, dataTopViewIsolatedCanard.length)
			.forEach(i -> {
				seriesCanardTopView.add(
						dataTopViewIsolatedCanard[dataTopViewIsolatedCanard.length-1-i][1] + Main.getTheAircraft().getCanard().getXApexConstructionAxes().doubleValue(SI.METER),
						- dataTopViewIsolatedCanard[dataTopViewIsolatedCanard.length-1-i][0] + Main.getTheAircraft().getCanard().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesTopViewList = new ArrayList<>();

		if(Main.getTheAircraft().getNacelles() != null) {
			for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

				// upper curve, sideview
				List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
				int nacelleCurveXPoints = nacelleCurveX.size();
				List<Amount<Length>> nacelleCurveUpperY = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYRight().stream().forEach(y -> nacelleCurveUpperY.add(y));

				// lower curve, sideview
				List<Amount<Length>> nacelleCurveLowerY = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYLeft().stream().forEach(y -> nacelleCurveLowerY.add(y));;

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
		}

		//-------------------------------------------------------------------------------
		// get data vectors from power plant propellers (only for PISTON and TURBOPROP)
		//-------------------------------------------------------------------------------
		List<XYSeries> seriesPropellerTopViewList = new ArrayList<>();
		
		if(Main.getTheAircraft().getPowerPlant() != null) {
			for(int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				if (Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
						|| Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {

					XYSeries seriesPropellerCruvesTopView = new XYSeries("Propeller " + i, false);
					seriesPropellerCruvesTopView.add(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getYApexConstructionAxes().doubleValue(SI.METER)
							+ Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);
					seriesPropellerCruvesTopView.add(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getYApexConstructionAxes().doubleValue(SI.METER)
							- Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);

					seriesPropellerTopViewList.add(seriesPropellerCruvesTopView);
				}
			}
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
		if (Main.getTheAircraft().getFuselage() != null) 
			componentZList.put(
					Main.getTheAircraft().getFuselage().getZApexConstructionAxes().doubleValue(SI.METER)
					+ Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionCylinderHeight().divide(2).doubleValue(SI.METER),
					Tuple.of(seriesFuselageCurve, Color.WHITE) 
					);
		if (Main.getTheAircraft().getWing() != null) 
			componentZList.put(
					Main.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesWingTopView, Color.decode("#87CEFA"))
					); 
		if (Main.getTheAircraft().getHTail() != null) 
			componentZList.put(
					Main.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesHTailTopView, Color.decode("#00008B"))
					);
		if (Main.getTheAircraft().getCanard() != null) 
			componentZList.put(
					Main.getTheAircraft().getCanard().getZApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesCanardTopView, Color.decode("#228B22"))
					);
		if (Main.getTheAircraft().getVTail() != null)
			if (Main.getTheAircraft().getFuselage() != null)
				componentZList.put(
						Main.getTheAircraft().getFuselage().getZApexConstructionAxes().doubleValue(SI.METER)
						+ Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionCylinderHeight().divide(2).doubleValue(SI.METER)
						+ 0.0001,
						Tuple.of(seriesVTailRootAirfoilTopView, Color.decode("#FFD700"))
						);
		if (Main.getTheAircraft().getVTail() != null)
			componentZList.put(
					Main.getTheAircraft().getVTail().getZApexConstructionAxes().doubleValue(SI.METER) 
					+ Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSpan().doubleValue(SI.METER), 
					Tuple.of(seriesVTailTipAirfoilTopView, Color.decode("#FFD700"))
					);
		if (Main.getTheAircraft().getNacelles() != null) 
			seriesNacelleCruvesTopViewList.stream().forEach(
					nac -> componentZList.put(
							Main.getTheAircraft().getNacelles().getNacellesList().get(
									seriesNacelleCruvesTopViewList.indexOf(nac)
									).getZApexConstructionAxes().doubleValue(SI.METER)
							+ 0.001
							+ seriesNacelleCruvesTopViewList.indexOf(nac)*0.001, 
							Tuple.of(nac, Color.decode("#FF7F50"))
							)
					);
		if (Main.getTheAircraft().getPowerPlant() != null) 
			seriesPropellerTopViewList.stream().forEach(
					prop -> componentZList.put(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(
									seriesPropellerTopViewList.indexOf(prop)
									).getZApexConstructionAxes().doubleValue(SI.METER)
							+ 0.0015
							+ seriesPropellerTopViewList.indexOf(prop)*0.001, 
							Tuple.of(prop, Color.BLACK)
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
		range.setInverted(Boolean.FALSE);

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
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		
		if (Main.getTheAircraft().getFuselage() != null) {
			// upper curve, sideview
			List<Amount<Length>> vX1Upper = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountX().stream().forEach(x -> vX1Upper.add(x));
			int nX1Upper = vX1Upper.size();
			List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

			// lower curve, sideview
			List<Amount<Length>> vX2Lower = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountX().stream().forEach(x -> vX2Lower.add(x));
			int nX2Lower = vX2Lower.size();
			List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));

			IntStream.range(0, nX1Upper)
			.forEach(i -> {
				seriesFuselageCurve.add(vX1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
			});
			IntStream.range(0, nX2Lower)
			.forEach(i -> {
				seriesFuselageCurve.add(vX2Lower.get(vX2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
			});
		}
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		XYSeries seriesWingRootAirfoil = new XYSeries("Wing Root - Side View", false);
		XYSeries seriesWingTipAirfoil = new XYSeries("Wing Tip - Side View", false);

		if (Main.getTheAircraft().getWing() != null) {
			Double[] wingRootXCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(0).getAirfoilCreator().getXCoords();
			Double[] wingRootZCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(0).getAirfoilCreator().getZCoords();
			Double[] wingTipXCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(Main.getTheAircraft().getWing().getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
			Double[] wingTipZCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(Main.getTheAircraft().getWing().getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
			int nPointsWing = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().size();

			IntStream.range(0, wingRootXCoordinates.length)
			.forEach(i -> {
				seriesWingRootAirfoil.add(
						(wingRootXCoordinates[i]*Main.getTheAircraft().getWing().getChordRoot().getEstimatedValue()) 
						+ Main.getTheAircraft().getWing().getXApexConstructionAxes().getEstimatedValue(),
						(wingRootZCoordinates[i]*Main.getTheAircraft().getWing().getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getWing().getZApexConstructionAxes().getEstimatedValue()
						);
			});

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
		}
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		XYSeries seriesHTailRootAirfoil = new XYSeries("HTail Root - Side View", false);
		XYSeries seriesHTailTipAirfoil = new XYSeries("HTail Tip - Side View", false);

		if (Main.getTheAircraft().getHTail() != null) {
			Double[] hTailRootXCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(0).getAirfoilCreator().getXCoords();
			Double[] hTailRootZCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(0).getAirfoilCreator().getZCoords();
			Double[] hTailTipXCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(Main.getTheAircraft().getHTail().getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
			Double[] hTailTipZCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(Main.getTheAircraft().getHTail().getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
			int nPointsHTail = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedXle().size();

			IntStream.range(0, hTailRootXCoordinates.length)
			.forEach(i -> {
				seriesHTailRootAirfoil.add(
						(hTailRootXCoordinates[i]*Main.getTheAircraft().getHTail().getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getHTail().getXApexConstructionAxes().getEstimatedValue(),
						(hTailRootZCoordinates[i]*Main.getTheAircraft().getHTail().getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getHTail().getZApexConstructionAxes().getEstimatedValue()
						);
			});

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
		}
		//--------------------------------------------------
		// get data vectors from canard discretization
		//--------------------------------------------------
		XYSeries seriesCanardRootAirfoil = new XYSeries("Canard Root - Side View", false);
		XYSeries seriesCanardTipAirfoil = new XYSeries("Canard Tip - Side View", false);

		if (Main.getTheAircraft().getCanard() != null) {
			Double[] canardRootXCoordinates = Main.getTheAircraft().getCanard().getAirfoilList().get(0).getAirfoilCreator().getXCoords();
			Double[] canardRootZCoordinates = Main.getTheAircraft().getCanard().getAirfoilList().get(0).getAirfoilCreator().getZCoords();
			Double[] canardTipXCoordinates = Main.getTheAircraft().getCanard().getAirfoilList().get(Main.getTheAircraft().getCanard().getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
			Double[] canardTipZCoordinates = Main.getTheAircraft().getCanard().getAirfoilList().get(Main.getTheAircraft().getCanard().getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
			int nPointsHTail = Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedXle().size();

			IntStream.range(0, canardRootXCoordinates.length)
			.forEach(i -> {
				seriesCanardRootAirfoil.add(
						(canardRootXCoordinates[i]*Main.getTheAircraft().getCanard().getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getCanard().getXApexConstructionAxes().getEstimatedValue(),
						(canardRootZCoordinates[i]*Main.getTheAircraft().getCanard().getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getCanard().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, canardTipXCoordinates.length)
			.forEach(i -> {
				seriesCanardTipAirfoil.add(
						(canardTipXCoordinates[i]*Main.getTheAircraft().getCanard().getChordTip().getEstimatedValue()) 
						+ Main.getTheAircraft().getCanard().getXApexConstructionAxes().getEstimatedValue()
						+ Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(),
						(canardTipZCoordinates[i]*Main.getTheAircraft().getCanard().getChordTip().getEstimatedValue())
						+ Main.getTheAircraft().getCanard().getZApexConstructionAxes().getEstimatedValue()
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		XYSeries seriesVTailSideView = new XYSeries("VTail - Side View", false);

		if (Main.getTheAircraft().getVTail() != null) {
			Double[][] dataTopViewVTail = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);

			IntStream.range(0, dataTopViewVTail.length)
			.forEach(i -> {
				seriesVTailSideView.add(
						dataTopViewVTail[i][0] + Main.getTheAircraft().getVTail().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewVTail[i][1] + Main.getTheAircraft().getVTail().getZApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}

		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesSideViewList = new ArrayList<>();

		if (Main.getTheAircraft().getNacelles() != null) {
			for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

				// upper curve, sideview
				List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
				int nacelleCurveXPoints = nacelleCurveX.size();
				List<Amount<Length>> nacelleCurveUpperZ = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZUpper().stream().forEach(z -> nacelleCurveUpperZ.add(z));

				// lower curve, sideview
				List<Amount<Length>> nacelleCurveLowerZ = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZLower().stream().forEach(z -> nacelleCurveLowerZ.add(z));;

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
		}
		//-------------------------------------------------------------------------------
		// get data vectors from power plant propellers (only for PISTON and TURBOPROP)
		//-------------------------------------------------------------------------------
		List<XYSeries> seriesPropellerSideViewList = new ArrayList<>();
		
		if(Main.getTheAircraft().getPowerPlant() != null) {
			for(int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				if (Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
						|| Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {

					XYSeries seriesPropellerCruvesSideView = new XYSeries("Propeller " + i, false);
					seriesPropellerCruvesSideView.add(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							1.0015*Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
							+ Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);
					seriesPropellerCruvesSideView.add(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							1.0015*Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
							- Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);

					seriesPropellerSideViewList.add(seriesPropellerCruvesSideView);
				}
			}
		}		
		//--------------------------------------------------
		// get data vectors from landing gears 
		//--------------------------------------------------
		XYSeries seriesLandingGearSideView = new XYSeries("Landing Gears - Side View", false);
		
		if (Main.getTheAircraft().getLandingGears() != null) {
			Amount<Length> radius = Main.getTheAircraft().getLandingGears().getRearWheelsHeight().divide(2);
			Double[] wheelCenterPosition = new Double[] {
					Main.getTheAircraft().getLandingGears().getXApexConstructionAxes().doubleValue(SI.METER),
					Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					- radius.doubleValue(SI.METER)
			};
			Double[] thetaArray = MyArrayUtils.linspaceDouble(0, 2*Math.PI, 360);

			IntStream.range(0, thetaArray.length)
			.forEach(i -> {
				seriesLandingGearSideView.add(
						radius.doubleValue(SI.METER)*Math.cos(thetaArray[i]) + wheelCenterPosition[0],
						radius.doubleValue(SI.METER)*Math.sin(thetaArray[i]) + wheelCenterPosition[1]
						);
			});
		}
		
		double xMaxSideView = 1.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double xMinSideView = -0.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMaxSideView = 1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double yMinSideView = -1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		if (Main.getTheAircraft().getPowerPlant() != null)
			seriesPropellerSideViewList.stream().forEach(
					prop -> seriesAndColorList.add(Tuple.of(prop, Color.BLACK))
					);
		if (Main.getTheAircraft().getNacelles() != null)
			seriesNacelleCruvesSideViewList.stream().forEach(
					nac -> seriesAndColorList.add(Tuple.of(nac, Color.decode("#FF7F50")))
					);
		if (Main.getTheAircraft().getWing() != null) {
			seriesAndColorList.add(Tuple.of(seriesWingRootAirfoil, Color.decode("#87CEFA")));
			seriesAndColorList.add(Tuple.of(seriesWingTipAirfoil, Color.decode("#87CEFA")));
		}
		if (Main.getTheAircraft().getHTail() != null) {
			seriesAndColorList.add(Tuple.of(seriesHTailRootAirfoil, Color.decode("#00008B")));
			seriesAndColorList.add(Tuple.of(seriesHTailTipAirfoil, Color.decode("#00008B")));
		}
		if (Main.getTheAircraft().getCanard() != null) {
			seriesAndColorList.add(Tuple.of(seriesCanardRootAirfoil, Color.decode("#228B22")));
			seriesAndColorList.add(Tuple.of(seriesCanardTipAirfoil, Color.decode("#228B22")));
		}
		if (Main.getTheAircraft().getLandingGears() != null) 
			seriesAndColorList.add(Tuple.of(seriesLandingGearSideView, Color.decode("#404040")));
		if (Main.getTheAircraft().getFuselage() != null)
			seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));
		if (Main.getTheAircraft().getVTail() != null)
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
		XYLineAndShapeRenderer xyLineAndShapePropRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapePropRenderer.setDefaultShapesVisible(false);
		xyLineAndShapePropRenderer.setDefaultLinesVisible(true);
		xyLineAndShapePropRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if (i < Main.getTheAircraft().getPowerPlant().getEngineNumber()) {
				xyLineAndShapePropRenderer.setSeriesVisible(i, true);
				xyLineAndShapePropRenderer.setSeriesPaint(i, Color.BLACK);
				xyLineAndShapePropRenderer.setSeriesStroke(
						i, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}
			else
				xyLineAndShapePropRenderer.setSeriesVisible(i, false);
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

		if (Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
				|| Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {
			plot.setRenderer(2, xyLineAndShapeRenderer);
			plot.setDataset(2, dataset);
			plot.setRenderer(1, xyAreaRenderer);
			plot.setDataset(1, dataset);
			plot.setRenderer(0, xyLineAndShapePropRenderer);
			plot.setDataset(0, dataset);
		}
		else {
			plot.setRenderer(1, xyLineAndShapeRenderer);
			plot.setDataset(1, dataset);
			plot.setRenderer(0, xyAreaRenderer);
			plot.setDataset(0, dataset);
		}

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
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Front View", false);
		
		if (Main.getTheAircraft().getFuselage() != null) {
			// section upper curve
			List<Amount<Length>> vY1Upper = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionUpperCurveAmountY().stream().forEach(y -> vY1Upper.add(y));
			int nY1Upper = vY1Upper.size();
			List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

			// section lower curve
			List<Amount<Length>> vY2Lower = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionLowerCurveAmountY().stream().forEach(y -> vY2Lower.add(y));
			int nY2Lower = vY2Lower.size();
			List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));

			IntStream.range(0, nY1Upper)
			.forEach(i -> {
				seriesFuselageCurve.add(vY1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
			});
			IntStream.range(0, nY2Lower)
			.forEach(i -> {
				seriesFuselageCurve.add(vY2Lower.get(vY2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
			});
		}
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		XYSeries seriesWingFrontView = new XYSeries("Wing - Front View", false);

		if (Main.getTheAircraft().getWing() != null) {
			List<Amount<Length>> wingBreakPointsYCoordinates = new ArrayList<>(); 
			Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getYBreakPoints().stream().forEach(y -> wingBreakPointsYCoordinates.add(y));
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
		}
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		XYSeries seriesHTailFrontView = new XYSeries("HTail - Front View", false);

		if (Main.getTheAircraft().getHTail() != null) {
			List<Amount<Length>> hTailBreakPointsYCoordinates = new ArrayList<>(); 
			Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getYBreakPoints().stream().forEach(y -> hTailBreakPointsYCoordinates.add(y));
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
		}
		//--------------------------------------------------
		// get data vectors from canard discretization
		//--------------------------------------------------
		XYSeries seriesCanardFrontView = new XYSeries("Canard - Front View", false);

		if (Main.getTheAircraft().getCanard() != null) {
			List<Amount<Length>> canardBreakPointsYCoordinates = new ArrayList<>(); 
			Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getYBreakPoints().stream().forEach(y -> canardBreakPointsYCoordinates.add(y));
			int nYPointsCanardTemp = canardBreakPointsYCoordinates.size();
			for(int i=0; i<nYPointsCanardTemp; i++)
				canardBreakPointsYCoordinates.add(Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getYBreakPoints().get(nYPointsCanardTemp-i-1));
			int nYPointsCanard = canardBreakPointsYCoordinates.size();

			List<Amount<Length>> canardThicknessZCoordinates = new ArrayList<Amount<Length>>();
			for(int i=0; i<Main.getTheAircraft().getCanard().getAirfoilList().size(); i++)
				canardThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
								MyArrayUtils.getMax(Main.getTheAircraft().getCanard().getAirfoilList().get(i).getAirfoilCreator().getZCoords()),
								SI.METER
								)
						);
			for(int i=0; i<nYPointsCanardTemp; i++)
				canardThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getChordsBreakPoints().get(nYPointsCanardTemp-i-1).doubleValue(SI.METER)*
								MyArrayUtils.getMin(Main.getTheAircraft().getCanard().getAirfoilList().get(nYPointsCanardTemp-i-1).getAirfoilCreator().getZCoords()),
								SI.METER
								)
						);

			List<Amount<Angle>> dihedralListCanard = new ArrayList<>();
			for (int i = 0; i < Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDihedralsBreakPoints().size(); i++) {
				dihedralListCanard.add(
						Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDihedralsBreakPoints().get(i)
						);
			}
			for (int i = 0; i < Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDihedralsBreakPoints().size(); i++) {
				dihedralListCanard.add(
						Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDihedralsBreakPoints().get(
								Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDihedralsBreakPoints().size()-1-i)
						);
			}

			IntStream.range(0, nYPointsCanard)
			.forEach(i -> {
				seriesCanardFrontView.add(
						canardBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getCanard().getYApexConstructionAxes()).doubleValue(SI.METRE),
						canardThicknessZCoordinates.get(i)
						.plus(Main.getTheAircraft().getCanard().getZApexConstructionAxes())
						.plus(canardBreakPointsYCoordinates.get(i)
								.times(Math.sin(dihedralListCanard.get(i)
										.doubleValue(SI.RADIAN))
										)
								)
						.doubleValue(SI.METRE)
						);
			});
			IntStream.range(0, nYPointsCanard)
			.forEach(i -> {
				seriesCanardFrontView.add(
						-canardBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getCanard().getYApexConstructionAxes()).doubleValue(SI.METRE),
						canardThicknessZCoordinates.get(i)
						.plus(Main.getTheAircraft().getCanard().getZApexConstructionAxes())
						.plus(canardBreakPointsYCoordinates.get(i)
								.times(Math.sin(dihedralListCanard.get(i)
										.doubleValue(SI.RADIAN))
										)
								)
						.doubleValue(SI.METRE)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		XYSeries seriesVTailFrontView = new XYSeries("VTail - Front View", false);

		if (Main.getTheAircraft().getVTail() != null) {
			List<Amount<Length>> vTailBreakPointsYCoordinates = new ArrayList<>(); 
			Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getYBreakPoints().stream().forEach(y -> vTailBreakPointsYCoordinates.add(y));
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

			IntStream.range(0, nYPointsVTail)
			.forEach(i -> {
				seriesVTailFrontView.add(
						vTailThicknessZCoordinates.get(i).plus(Main.getTheAircraft().getVTail().getYApexConstructionAxes()).doubleValue(SI.METRE),
						vTailBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getVTail().getZApexConstructionAxes()).doubleValue(SI.METRE)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from nacelles discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesFrontViewList = new ArrayList<>();

		if (Main.getTheAircraft().getNacelles() != null) {
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
		}
		//-------------------------------------------------------------------------------
		// get data vectors from power plant propellers (only for PISTON and TURBOPROP)
		//-------------------------------------------------------------------------------
		List<XYSeries> seriesPropellerFrontViewList = new ArrayList<>();
		
		if(Main.getTheAircraft().getPowerPlant() != null) {
			for(int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				if (Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
						|| Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {
					
					Amount<Length> radius = Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2);
					Double[] centerPosition = new Double[] {
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getYApexConstructionAxes().doubleValue(SI.METER),
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
					};
					Double[] thetaArray = MyArrayUtils.linspaceDouble(0, 2*Math.PI, 360);
					
					XYSeries seriesPropellerCruvesFrontView = new XYSeries("Propeller " + i, false);
					IntStream.range(0, thetaArray.length)
					.forEach(j -> {
						seriesPropellerCruvesFrontView.add(
								radius.doubleValue(SI.METER)*Math.cos(thetaArray[j]) + centerPosition[0],
								radius.doubleValue(SI.METER)*Math.sin(thetaArray[j]) + centerPosition[1]
								);
					});

					seriesPropellerFrontViewList.add(seriesPropellerCruvesFrontView);
					
				}
			}
		}
		//--------------------------------------------------
		// get data vectors from landing gears
		//--------------------------------------------------
		List<XYSeries> serieLandingGearsCruvesFrontViewList = new ArrayList<>();

		if (Main.getTheAircraft().getLandingGears() != null) {
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
		}
		
		double yMaxFrontView = 1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double yMinFrontView = -1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
		double zMaxFrontView = yMaxFrontView; 
		double zMinFrontView = yMinFrontView;
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		Color propellerColor = new Color(255, 255, 255, 0);
		
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		if (Main.getTheAircraft().getFuselage() != null)
			seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));
		if (Main.getTheAircraft().getPowerPlant() != null)
			seriesPropellerFrontViewList.stream().forEach(
					prop -> seriesAndColorList.add(Tuple.of(prop, propellerColor))
					);
		if (Main.getTheAircraft().getNacelles() != null)
			seriesNacelleCruvesFrontViewList.stream().forEach(
					nac -> seriesAndColorList.add(Tuple.of(nac, Color.decode("#FF7F50")))
					);
		if (Main.getTheAircraft().getCanard() != null)
			seriesAndColorList.add(Tuple.of(seriesCanardFrontView, Color.decode("#228B22")));
		if (Main.getTheAircraft().getWing() != null)
			seriesAndColorList.add(Tuple.of(seriesWingFrontView, Color.decode("#87CEFA")));
		if (Main.getTheAircraft().getHTail() != null)
			seriesAndColorList.add(Tuple.of(seriesHTailFrontView, Color.decode("#00008B")));
		if (Main.getTheAircraft().getVTail() != null)
			seriesAndColorList.add(Tuple.of(seriesVTailFrontView, Color.decode("#FFD700")));
		if (Main.getTheAircraft().getLandingGears() != null)
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

		else {
			textFieldAircraftFuselageX.setText("0.0");
			fuselageXUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftFuselageY.setText("0.0");
			fuselageYUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftFuselageZ.setText("0.0");
			fuselageZUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftWingX.setText("0.0");
			wingXUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftWingY.setText("0.0");
			wingYUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftWingZ.setText("0.0");
			wingZUnitChoiceBox.getSelectionModel().select(0);
		}
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
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("°")
					|| Main.getTheAircraft()
					.getWing()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				wingRiggingAngleUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				wingRiggingAngleUnitChoiceBox.getSelectionModel().select(1);

		}
		else {
			textFieldAircraftWingRiggingAngle.setText("0.0");
			wingRiggingAngleUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftHTailX.setText("0.0");
			hTailXUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftHTailY.setText("0.0");
			hTailYUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftHTailZ.setText("0.0");
			htailZUnitChoiceBox.getSelectionModel().select(0);
		}
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
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("°")
					|| Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				hTailRiggingAngleUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				hTailRiggingAngleUnitChoiceBox.getSelectionModel().select(1);

		}
		else {
			textFieldAircraftHTailRiggingAngle.setText("0.0");
			hTailRiggingAngleUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftVTailX.setText("0.0");
			vTailXUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftVTailY.setText("0.0");
			vTailYUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftVTailZ.setText("0.0");
			vTailZUnitChoiceBox.getSelectionModel().select(0);
		}
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
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("°")
					|| Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				vTailRiggingAngleUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				vTailRiggingAngleUnitChoiceBox.getSelectionModel().select(1);

		}
		else {
			textFieldAircraftVTailRiggingAngle.setText("0.0");
			vTailRiggingAngleUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftCanardX.setText("0.0");
			canardXUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftCanardY.setText("0.0");
			canardYUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftCanardZ.setText("0.0");
			canardZUnitChoiceBox.getSelectionModel().select(0);
		}
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
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("°")
					|| Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				canardRiggingAngleUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				canardRiggingAngleUnitChoiceBox.getSelectionModel().select(1);

		}
		else {
			textFieldAircraftCanardRiggingAngle.setText("0.0");
			canardRiggingAngleUnitChoiceBox.getSelectionModel().select(0);
		}
		
		//---------------------------------------------------------------------------------
		// ENGINES NUMBER CHECK:
		if (Main.getTheAircraft().getPowerPlant().getEngineList().size() >= 
				tabPaneAircraftEngines.getTabs().size()) {
			
			int iStart = tabPaneAircraftEngines.getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++)
				addAircraftEngine();
			
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER ENGINES:
		//---------------------------------------------------------------------------------
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
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) {
					textFieldAircraftEngineXList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getXApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxAircraftEngineXUnitList.get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxAircraftEngineXUnitList.get(i).getSelectionModel().select(1);
				}
				else {
					textFieldAircraftEngineXList.get(i).setText("0.0");
					choiceBoxAircraftEngineXUnitList.get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) {
					textFieldAircraftEngineYList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getYApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxAircraftEngineYUnitList.get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxAircraftEngineYUnitList.get(i).getSelectionModel().select(1);
				}
				else {
					textFieldAircraftEngineYList.get(i).setText("0.0");
					choiceBoxAircraftEngineYUnitList.get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) {
					textFieldAircraftEngineZList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getZApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxAircraftEngineZUnitList.get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxAircraftEngineZUnitList.get(i).getSelectionModel().select(1);
				}
				else {
					textFieldAircraftEngineZList.get(i).setText("0.0");
					choiceBoxAircraftEngineZUnitList.get(i).getSelectionModel().select(0);
				}
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
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) {
					textFieldAircraftEngineTiltList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getTiltingAngle()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getTiltingAngle().getUnit().toString().equalsIgnoreCase("°")
							|| Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getTiltingAngle().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxAircraftEngineTiltUnitList.get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getTiltingAngle().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxAircraftEngineTiltUnitList.get(i).getSelectionModel().select(1);
				}
				else {
					textFieldAircraftEngineTiltList.get(i).setText("0.0");
					choiceBoxAircraftEngineTiltUnitList.get(i).getSelectionModel().select(0);
				}
			}
		}

		//---------------------------------------------------------------------------------
		// NACELLE NUMBER CHECK:
		if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
				tabPaneAircraftNacelles.getTabs().size()) {
			
			int iStart = tabPaneAircraftNacelles.getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++)
				addAircraftNacelle();
			
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER ENGINES:
		//---------------------------------------------------------------------------------
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
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null) {
					textFieldAircraftNacelleXList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getXApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxAircraftNacelleXUnitList.get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxAircraftNacelleXUnitList.get(i).getSelectionModel().select(1);
				}
				else {
					textFieldAircraftNacelleXList.get(i).setText("0.0");
					choiceBoxAircraftNacelleXUnitList.get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null) {
					textFieldAircraftNacelleYList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getYApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxAircraftNacelleYUnitList.get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxAircraftNacelleYUnitList.get(i).getSelectionModel().select(1);
				}
				else {
					textFieldAircraftNacelleYList.get(i).setText("0.0");
					choiceBoxAircraftNacelleYUnitList.get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null) {
					textFieldAircraftNacelleZList.get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getZApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxAircraftNacelleZUnitList.get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxAircraftNacelleZUnitList.get(i).getSelectionModel().select(1);
				}
				else {
					textFieldAircraftNacelleZList.get(i).setText("0.0");
					choiceBoxAircraftNacelleZUnitList.get(i).getSelectionModel().select(0);
				}
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
		else {
			textFieldAircraftLandingGearsX.setText("0.0");
			landingGearsXUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftLandingGearsY.setText("0.0");
			landingGearsYUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftLandingGearsZ.setText("0.0");
			landingGearsZUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftSystemsX.setText("0.0");
			systemsXUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftSystemsY.setText("0.0");
			systemsYUnitChoiceBox.getSelectionModel().select(0);
		}
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
		else {
			textFieldAircraftSystemsZ.setText("0.0");
			systemsZUnitChoiceBox.getSelectionModel().select(0);
		}
	}
	
	private void createFuselageTopView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// left curve, upperview
		List<Amount<Length>> vX1Left = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountX().stream().forEach(x -> vX1Left.add(x));
		int nX1Left = vX1Left.size();
		List<Amount<Length>> vY1Left = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountY().stream().forEach(y -> vY1Left.add(y));

		// right curve, upperview
		List<Amount<Length>> vX2Right = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountX().stream().forEach(x -> vX2Right.add(x));
		int nX2Right = vX2Right.size();
		List<Amount<Length>> vY2Right = new ArrayList<>();
		Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountY().stream().forEach(y -> vY2Right.add(y));

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
		range.setInverted(Boolean.FALSE);

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
		List<Amount<Length>> vX1Upper = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountX().stream().forEach(x -> vX1Upper.add(x));
		int nX1Upper = vX1Upper.size();
		List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

		// lower curve, sideview
		List<Amount<Length>> vX2Lower = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountX().stream().forEach(x -> vX2Lower.add(x));
		int nX2Lower = vX2Lower.size();
		List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));

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
		List<Amount<Length>> vY1Upper = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionUpperCurveAmountY().stream().forEach(y -> vY1Upper.add(y));
		int nY1Upper = vY1Upper.size();
		List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

		// section lower curve
		List<Amount<Length>> vY2Lower = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionLowerCurveAmountY().stream().forEach(y -> vY2Lower.add(y));
		int nY2Lower = vY2Lower.size();
		List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));
		
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
		// SPOILERS NUMBER CHECK:
		if (Main.getTheAircraft().getFuselage().getSpoilers().size() >= 
				tabPaneFuselageSpoilers.getTabs().size()) {
			
			int iStart = tabPaneFuselageSpoilers.getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getFuselage().getSpoilers().size(); i++)
				addFuselageSpoiler();
			
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER SPOILERS:
		for (int i=0; i<Main.getTheAircraft().getFuselage().getSpoilers().size(); i++) {
			
			SpoilerCreator currentSpoiler = Main.getTheAircraft().getFuselage().getSpoilers().get(i);
			
			//---------------------------------------------------------------------------------
			// INNER SPANWISE POSITION:
			if(currentSpoiler.getInnerStationSpanwisePosition() != null) {
				textFieldFuselageInnerSpanwisePositionSpoilerList.get(i).setText(
						String.valueOf(currentSpoiler.getInnerStationSpanwisePosition())
						);
			}
			else
				textFieldFuselageInnerSpanwisePositionSpoilerList.get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// OUTER SPANWISE POSITION:
			if(currentSpoiler.getOuterStationSpanwisePosition() != null) {
				textFieldFuselageOuterSpanwisePositionSpoilerList.get(i).setText(
						String.valueOf(currentSpoiler.getOuterStationSpanwisePosition())
						);
			}
			else
				textFieldFuselageOuterSpanwisePositionSpoilerList.get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// INNER CHORDWISE POSITION:
			if(currentSpoiler.getInnerStationChordwisePosition() != null) {
				textFieldFuselageInnerChordwisePositionSpoilerList.get(i).setText(
						String.valueOf(currentSpoiler.getInnerStationChordwisePosition())
						);
			}
			else
				textFieldFuselageInnerChordwisePositionSpoilerList.get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// OUTER CHORDWISE POSITION:
			if(currentSpoiler.getOuterStationChordwisePosition() != null) {
				textFieldFuselageOuterChordwisePositionSpoilerList.get(i).setText(
						String.valueOf(currentSpoiler.getOuterStationChordwisePosition())
						);
			}
			else
				textFieldFuselageOuterChordwisePositionSpoilerList.get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// MINIMUM DEFLECTION:
			if(currentSpoiler.getMinimumDeflection() != null) {
				
				textFieldFuselageMinimumDeflectionAngleSpoilerList.get(i).setText(
						String.valueOf(currentSpoiler.getMinimumDeflection().getEstimatedValue())
						);
				
				if(currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
						|| currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
					choiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList.get(i).getSelectionModel().select(0);
				else if(currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
					choiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList.get(i).getSelectionModel().select(1);
				
			}
			else
				textFieldFuselageMinimumDeflectionAngleSpoilerList.get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// MAXIMUM DEFLECTION:
			if(currentSpoiler.getMaximumDeflection() != null) {
				
				textFieldFuselageMaximumDeflectionAngleSpoilerList.get(i).setText(
						String.valueOf(currentSpoiler.getMaximumDeflection().getEstimatedValue())
						);
				
				if(currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
						|| currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
					choiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList.get(i).getSelectionModel().select(0);
				else if(currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
					choiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList.get(i).getSelectionModel().select(1);
				
			}
			else
				textFieldFuselageMaximumDeflectionAngleSpoilerList.get(i).setText(
						"NOT INITIALIZED"
						);
			
		}
	}

	private void createSeatMap() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// left curve, upperview
		List<Amount<Length>> vX1Left = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountX().stream().forEach(x -> vX1Left.add(x));
		int nX1Left = vX1Left.size();
		List<Amount<Length>> vY1Left = new ArrayList<>();
		Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountY().stream().forEach(y -> vY1Left.add(y));

		// right curve, upperview
		List<Amount<Length>> vX2Right = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountX().stream().forEach(x -> vX2Right.add(x));
		int nX2Right = vX2Right.size();
		List<Amount<Length>> vY2Right = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountY().stream().forEach(y -> vY2Right.add(y));

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
		// creating seat blocks outlines and seats (starting from FIRST class)
		//--------------------------------------------------
		List<SeatsBlock> seatBlockList = new ArrayList<>();
		List<XYSeries> seatBlockSeriesList = new ArrayList<>();
		List<XYSeries> seatsSeriesList = new ArrayList<>();
		
		Amount<Length> length = Amount.valueOf(0., SI.METER);
		Map<Integer, Amount<Length>> breaksMap = new HashMap<>();
		List<Map<Integer, Amount<Length>>> breaksMapList = new ArrayList<>();
		int classNumber = Main.getTheAircraft().getCabinConfiguration().getClassesNumber()-1;
		
		for (int i = 0; i < Main.getTheAircraft().getCabinConfiguration().getClassesNumber(); i++) {

			SeatsBlock seatsBlock = new SeatsBlock();
			
			breaksMap.put(Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaks().get(classNumber-i), Main.getTheAircraft().getCabinConfiguration().getWidth().get(i));
			breaksMapList.add(breaksMap);

			seatsBlock.createSeatsBlock(
					RelativePositionEnum.RIGHT,
					Main.getTheAircraft().getCabinConfiguration().getXCoordinateFirstRow().plus(length),
					Main.getTheAircraft().getCabinConfiguration().getPitch().get(classNumber-i),
					Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i),
					Main.getTheAircraft().getCabinConfiguration().getDistanceFromWall().get(classNumber-i),
					breaksMapList.get(i),
					Main.getTheAircraft().getCabinConfiguration().getNumberOfRows().get(classNumber-i),
					Main.getTheAircraft().getCabinConfiguration().getNumberOfColumns().get(classNumber-i)[0],
					Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().get(classNumber-i),
					Main.getTheAircraft().getCabinConfiguration().getTypeList().get(classNumber-i));

			seatBlockList.add(seatsBlock);
			//........................................................................................................
			XYSeries seriesSeatBlock = new XYSeries("Seat Block " + i + " start", false);
			seriesSeatBlock.add(
					Main.getTheAircraft().getCabinConfiguration().getXCoordinateFirstRow().doubleValue(SI.METRE)
					+ length.doubleValue(SI.METER),
					- FusNacGeometryCalc.getWidthAtX(
							Main.getTheAircraft().getCabinConfiguration().getXCoordinateFirstRow().doubleValue(SI.METRE)
							+ length.doubleValue(SI.METER),
							Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveX(),
							Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveY()
							)/2
					+ Main.getTheAircraft().getCabinConfiguration().getDistanceFromWall().get(classNumber-i).doubleValue(SI.METER)
					);
			seriesSeatBlock.add(
					Main.getTheAircraft().getCabinConfiguration().getXCoordinateFirstRow().doubleValue(SI.METRE)
					+ length.doubleValue(SI.METER),
					FusNacGeometryCalc.getWidthAtX(
							Main.getTheAircraft().getCabinConfiguration().getXCoordinateFirstRow().doubleValue(SI.METRE)
							+ length.doubleValue(SI.METER),
							Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveX(),
							Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveY()
							)/2
					- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWall().get(classNumber-i).doubleValue(SI.METER)
					);
			//........................................................................................................
			Amount<Length> aisleWidth = Amount.valueOf(0.0, SI.METER);
			Amount<Length> currentYPosition = Amount.valueOf(0.0, SI.METER);
			Double breakLengthPitchFraction = 0.25;
			List<Integer> breakesPositionsIndexList = new ArrayList<>();
			for (int iBrake=0; iBrake<Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaks().get(classNumber-i); iBrake++) {
				Integer brekesInteval = Math.round(
						(Main.getTheAircraft().getCabinConfiguration().getNumberOfRows().get(classNumber-i)
						+ Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaks().get(classNumber-i))
						/ (Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaks().get(classNumber-i) + 1)
						);
				breakesPositionsIndexList.add((iBrake+1)*brekesInteval);
			}
			
			for(int j=0; j<Main.getTheAircraft().getCabinConfiguration().getNumberOfColumns().get(classNumber-i).length; j++) {
				if(j>0) {
					aisleWidth = Amount.valueOf( 
							(Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionCylinderWidth().doubleValue(SI.METER) 
									- (MyArrayUtils.sumArrayElements(Main.getTheAircraft().getCabinConfiguration().getNumberOfColumns().get(classNumber-i))
											* Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).doubleValue(SI.METER))
									- 2*Main.getTheAircraft().getCabinConfiguration().getDistanceFromWall().get(classNumber-i).doubleValue(SI.METER))
							/Main.getTheAircraft().getCabinConfiguration().getAislesNumber(),
							SI.METER
							);
					currentYPosition = Amount.valueOf(
							seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getYValue(),
							SI.METER
							);
				}
				for (int k = 0; k <Main.getTheAircraft().getCabinConfiguration().getNumberOfColumns().get(classNumber-i)[j]; k++) {
					
					int indexOfCurrentBrake = 10000;
					
					for (int r = 0;
							 r < Main.getTheAircraft().getCabinConfiguration().getNumberOfRows().get(classNumber-i); 
							 r++) {
						
						XYSeries seriesSeats = new XYSeries("Column " + i + j + k + r, false);
						if(j>0) {
							int rowIndex = r;
							if(breakesPositionsIndexList.stream().anyMatch(x -> x == rowIndex)) {
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ breakLengthPitchFraction * Main.getTheAircraft().getCabinConfiguration().getPitch().get(classNumber-i).doubleValue(SI.METER)
										+ Main.getTheAircraft().getCabinConfiguration().getPitch().get(classNumber-i).doubleValue(SI.METER),
										currentYPosition.doubleValue(SI.METER)
										- aisleWidth.doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).doubleValue(SI.METER)
										);
								indexOfCurrentBrake = r;
							}
							else if (r > indexOfCurrentBrake)
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ Main.getTheAircraft().getCabinConfiguration().getPitch().get(classNumber-i).doubleValue(SI.METER),
										currentYPosition.doubleValue(SI.METER)
										- aisleWidth.doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).doubleValue(SI.METER)
										);
							else
								seriesSeats.add(
										Main.getTheAircraft().getCabinConfiguration().getXCoordinateFirstRow().doubleValue(SI.METER)
										+ length.doubleValue(SI.METER)
										+ Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).doubleValue(SI.METER)
										+ r * Main.getTheAircraft().getCabinConfiguration().getPitch().get(classNumber-i).doubleValue(SI.METER),
										currentYPosition.doubleValue(SI.METER)
										- aisleWidth.doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).doubleValue(SI.METER)
										);
						}
						else {
							int columnIndex = r;
							if(breakesPositionsIndexList.stream().anyMatch(x -> x == columnIndex)) {
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ breakLengthPitchFraction * Main.getTheAircraft().getCabinConfiguration().getPitch().get(classNumber-i).doubleValue(SI.METER)
										+ Main.getTheAircraft().getCabinConfiguration().getPitch().get(classNumber-i).doubleValue(SI.METER),
										Main.getTheAircraft().getFuselage().getSectionWidht().divide(2).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWall().get(classNumber-i).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).divide(2).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).doubleValue(SI.METER)
										);
								indexOfCurrentBrake = r;
							}
							else if (r > indexOfCurrentBrake)
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ Main.getTheAircraft().getCabinConfiguration().getPitch().get(classNumber-i).doubleValue(SI.METER),
										Main.getTheAircraft().getFuselage().getSectionWidht().divide(2).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWall().get(classNumber-i).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).divide(2).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).doubleValue(SI.METER)
										);
							else
								seriesSeats.add(
										Main.getTheAircraft().getCabinConfiguration().getXCoordinateFirstRow().doubleValue(SI.METER)
										+ length.doubleValue(SI.METER)
										+ Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).doubleValue(SI.METER)
										+ r * Main.getTheAircraft().getCabinConfiguration().getPitch().get(classNumber-i).doubleValue(SI.METER),
										Main.getTheAircraft().getFuselage().getSectionWidht().divide(2).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWall().get(classNumber-i).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).divide(2).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidth().get(classNumber-i).doubleValue(SI.METER)
										);
								
						}
						seatsSeriesList.add(seriesSeats);
						
					}
				}				
			}
		
			length = length.plus(seatsBlock.getLenghtOverall());
			seatBlockSeriesList.add(seriesSeatBlock);
			
		}
		
		XYSeries seriesSeatBlock = new XYSeries("Seat Block " + classNumber + " ending", false);
		seriesSeatBlock.add(
				Main.getTheAircraft().getCabinConfiguration().getXCoordinateFirstRow().doubleValue(SI.METRE)
				+ length.doubleValue(SI.METER),
				- FusNacGeometryCalc.getWidthAtX(
						Main.getTheAircraft().getCabinConfiguration().getXCoordinateFirstRow().doubleValue(SI.METRE)
						+ length.doubleValue(SI.METER),
						Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveX(),
						Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveY()
						)/2
				+ Main.getTheAircraft().getCabinConfiguration().getDistanceFromWall().get(classNumber).doubleValue(SI.METER)
				);
		seriesSeatBlock.add(
				Main.getTheAircraft().getCabinConfiguration().getXCoordinateFirstRow().doubleValue(SI.METRE)
				+ length.doubleValue(SI.METER),
				FusNacGeometryCalc.getWidthAtX(
						Main.getTheAircraft().getCabinConfiguration().getXCoordinateFirstRow().doubleValue(SI.METRE)
						+ length.doubleValue(SI.METER),
						Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveX(),
						Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveY()
						)/2
				- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWall().get(classNumber).doubleValue(SI.METER)
				);
		seatBlockSeriesList.add(seriesSeatBlock);
		
		double xMaxTopView = Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMinTopView = 0.0;

		int WIDTH = 700;
		int HEIGHT = 600;

		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seatsSeriesList.stream().forEach(
				s -> seriesAndColorList.add(Tuple.of(s, Color.WHITE))
				);
		seatBlockSeriesList.stream().forEach(
				sb -> seriesAndColorList.add(Tuple.of(sb, Color.WHITE))
				);
		seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Seat Map representation", 
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
		range.setInverted(Boolean.FALSE);

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
			if(i<seatsSeriesList.size()) {
				xyLineAndShapeRenderer.setSeriesLinesVisible(i, false);
				xyLineAndShapeRenderer.setSeriesShapesVisible(i, true);
				xyLineAndShapeRenderer.setSeriesShape(
						i,
						ShapeUtils.createDiamond(2.5f).getBounds()
						);
			}
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "SeatMap.svg";
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
		cabinConfigurationSeatMapPane.getChildren().add(sceneTopView.getRoot());
	}
	
	private void logCabinConfigutionFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		textAreaCabinConfigurationConsoleOutput.setText(
				Main.getTheAircraft().getCabinConfiguration().toString()
				);

		if(Main.getTheAircraft().getCabinConfiguration() != null) {

			//---------------------------------------------------------------------------------
			// ACTUAL PASSENGERS NUMBER:
			if(Main.getTheAircraft().getCabinConfiguration().getNPax() != null) 
			textFieldActualPassengersNumber.setText(
					Integer.toString(
							Main.getTheAircraft()
							.getCabinConfiguration()
							.getNPax()
							)
					);
			else
				textFieldActualPassengersNumber.setText("0");
			//---------------------------------------------------------------------------------
			// MAXIMUM PASSENGERS NUMBER:
			if(Main.getTheAircraft().getCabinConfiguration().getMaxPax() != null)
				textFieldMaximumPassengersNumber.setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getMaxPax()
								)
						);
			else
				textFieldMaximumPassengersNumber.setText("0");
			//---------------------------------------------------------------------------------
			// FLIGHT CREW NUMBER:
			if(Main.getTheAircraft().getCabinConfiguration().getFlightCrewNumber() != null)
				textFieldFlightCrewNumber.setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getFlightCrewNumber()
								)
						);
			else
				textFieldFlightCrewNumber.setText("0");
			//---------------------------------------------------------------------------------
			// CLASSES NUMBER:
			if(Main.getTheAircraft().getCabinConfiguration().getClassesNumber() != null) 
				textFieldClassesNumber.setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getClassesNumber()
								)
						);
			else
				textFieldFlightCrewNumber.setText("0");
			//---------------------------------------------------------------------------------
			// CLASSES TYPE:
			if(Main.getTheAircraft().getCabinConfiguration().getTypeList() != null) {
				
				for(int i=0; i<Main.getTheAircraft().getCabinConfiguration().getTypeList().size(); i++) {
					
					// CLASS 1
					if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(0).equals(ClassTypeEnum.ECONOMY))
						cabinConfigurationClassesTypeChoiceBox1.getSelectionModel().select(0);
					if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(0).equals(ClassTypeEnum.BUSINESS))
						cabinConfigurationClassesTypeChoiceBox1.getSelectionModel().select(1);
					if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(0).equals(ClassTypeEnum.FIRST))
						cabinConfigurationClassesTypeChoiceBox1.getSelectionModel().select(2);
					
					if (i==1) {
					
						// CLASS 1
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(0).equals(ClassTypeEnum.ECONOMY))
							cabinConfigurationClassesTypeChoiceBox1.getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(0).equals(ClassTypeEnum.BUSINESS))
							cabinConfigurationClassesTypeChoiceBox1.getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(0).equals(ClassTypeEnum.FIRST))
							cabinConfigurationClassesTypeChoiceBox1.getSelectionModel().select(2);

						// CLASS 2
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(1).equals(ClassTypeEnum.ECONOMY))
							cabinConfigurationClassesTypeChoiceBox2.getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(1).equals(ClassTypeEnum.BUSINESS))
							cabinConfigurationClassesTypeChoiceBox2.getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(1).equals(ClassTypeEnum.FIRST))
							cabinConfigurationClassesTypeChoiceBox2.getSelectionModel().select(2);
						
					}
					if (i==2) {
						
						// CLASS 1
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(0).equals(ClassTypeEnum.ECONOMY))
							cabinConfigurationClassesTypeChoiceBox1.getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(0).equals(ClassTypeEnum.BUSINESS))
							cabinConfigurationClassesTypeChoiceBox1.getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(0).equals(ClassTypeEnum.FIRST))
							cabinConfigurationClassesTypeChoiceBox1.getSelectionModel().select(2);

						// CLASS 2
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(1).equals(ClassTypeEnum.ECONOMY))
							cabinConfigurationClassesTypeChoiceBox2.getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(1).equals(ClassTypeEnum.BUSINESS))
							cabinConfigurationClassesTypeChoiceBox2.getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(1).equals(ClassTypeEnum.FIRST))
							cabinConfigurationClassesTypeChoiceBox2.getSelectionModel().select(2);
						
						// CLASS 3
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(2).equals(ClassTypeEnum.ECONOMY))
							cabinConfigurationClassesTypeChoiceBox3.getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(2).equals(ClassTypeEnum.BUSINESS))
							cabinConfigurationClassesTypeChoiceBox3.getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTypeList().get(2).equals(ClassTypeEnum.FIRST))
							cabinConfigurationClassesTypeChoiceBox3.getSelectionModel().select(2);
						
					}
				}
			}
			
			//---------------------------------------------------------------------------------
			// AISLES NUMBER:
			if(Main.getTheAircraft().getCabinConfiguration().getAislesNumber() != null)
				textFieldAislesNumber.setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getAislesNumber()
								)
						);
			else
				textFieldAislesNumber.setText("0");
			//---------------------------------------------------------------------------------
			// X COORDINATE FIRST ROW:
			if(Main.getTheAircraft().getCabinConfiguration().getXCoordinateFirstRow() != null) {
				
				textFieldXCoordinateFirstRow.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getXCoordinateFirstRow()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getXCoordinateFirstRow().getUnit().toString().equalsIgnoreCase("m"))
					cabinConfigurationXCoordinateFirstRowUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getXCoordinateFirstRow().getUnit().toString().equalsIgnoreCase("ft"))
					cabinConfigurationXCoordinateFirstRowUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else {
				textFieldXCoordinateFirstRow.setText("0.0");
				cabinConfigurationXCoordinateFirstRowUnitChoiceBox.getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// MISSING SEATS ROW:
			if(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow() != null) {
				
				for(int i=0; i<Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().size(); i++) {
					
					// CLASS 1
					if(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().get(0) != null)
					textFieldMissingSeatRow1.setText(
							String.valueOf(
									Arrays.toString(
											Main.getTheAircraft()
											.getCabinConfiguration()
											.getMissingSeatsRow()
											.get(0)
											)
									)
							);
					else
						textFieldMissingSeatRow1.setText("0");
					
					if (i==1) {
					
						// CLASS 1
						if(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().get(0) != null)
						textFieldMissingSeatRow1.setText(
								String.valueOf(
										Arrays.toString(
												Main.getTheAircraft()
												.getCabinConfiguration()
												.getMissingSeatsRow()
												.get(0)
												)
										)
								);
						else
							textFieldMissingSeatRow1.setText("0");

						// CLASS 2
						if(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().get(1) != null)
						textFieldMissingSeatRow2.setText(
								String.valueOf(
										Arrays.toString(
												Main.getTheAircraft()
												.getCabinConfiguration()
												.getMissingSeatsRow()
												.get(1)
												)
										)
								);
						else
							textFieldMissingSeatRow2.setText("0");
						
					}
					if (i==2) {
						
						// CLASS 1
						if(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().get(0) != null)
						textFieldMissingSeatRow1.setText(
								String.valueOf(
										Arrays.toString(
												Main.getTheAircraft()
												.getCabinConfiguration()
												.getMissingSeatsRow()
												.get(0)
												)
										)
								);
						else
							textFieldMissingSeatRow1.setText("0");

						// CLASS 2
						if(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().get(1) != null)
						textFieldMissingSeatRow2.setText(
								String.valueOf(
										Arrays.toString(
												Main.getTheAircraft()
												.getCabinConfiguration()
												.getMissingSeatsRow()
												.get(1)
												)
										)
								);
						else
							textFieldMissingSeatRow2.setText("0");
						
						// CLASS 3
						if(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().get(2) != null)
						textFieldMissingSeatRow3.setText(
								String.valueOf(
										Arrays.toString(
												Main.getTheAircraft()
												.getCabinConfiguration()
												.getMissingSeatsRow()
												.get(2)
												)
										)
								);
						else
							textFieldMissingSeatRow3.setText("0");
					}
				}
			}

			//---------------------------------------------------------------------------------
			// NUMBER OF BRAKES ECONOMY:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksEconomyClass() != null)
				textFieldNumberOfBrakesEconomy.setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfBreaksEconomyClass()
								)
						);
			else
				textFieldNumberOfBrakesEconomy.setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF BRAKES BUSINESS:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksBusinessClass() != null)
				textFieldNumberOfBrakesBusiness.setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfBreaksBusinessClass()
								)
						);
			else
				textFieldNumberOfBrakesBusiness.setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF BRAKES FIRST:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksFirstClass() != null)
				textFieldNumberOfBrakesFirst.setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfBreaksFirstClass()
								)
						);
			else
				textFieldNumberOfBrakesFirst.setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF ROWS ECONOMY:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsEconomyClass() != null)
				textFieldNumberOfRowsEconomy.setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfRowsEconomyClass()
								)
						);
			else
				textFieldNumberOfRowsEconomy.setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF ROWS BUSINESS:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsBusinessClass() != null)
				textFieldNumberOfRowsBusiness.setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfRowsBusinessClass()
								)
						);
			else
				textFieldNumberOfRowsBusiness.setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF ROWS FIRST:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsFirstClass() != null)
				textFieldNumberOfRowsFirst.setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfRowsFirstClass()
								)
						);
			else
				textFieldNumberOfRowsFirst.setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF COLUMNS ECONOMY:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsEconomyClass() != null)
				textFieldNumberOfColumnsEconomy.setText(
						String.valueOf(
								Arrays.toString(
										Main.getTheAircraft()
										.getCabinConfiguration()
										.getNumberOfColumnsEconomyClass()
										)
								)
						);
			else
				textFieldNumberOfColumnsEconomy.setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF COLUMNS BUSINESS:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsBusinessClass() != null)
				textFieldNumberOfColumnsBusiness.setText(
						String.valueOf(
								Arrays.toString(
										Main.getTheAircraft()
										.getCabinConfiguration()
										.getNumberOfColumnsBusinessClass()
										)
								)
						);
			else
				textFieldNumberOfColumnsBusiness.setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF COLUMNS FIRST:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsFirstClass() != null)
				textFieldNumberOfColumnsFirst.setText(
						String.valueOf(
								Arrays.toString(
										Main.getTheAircraft()
										.getCabinConfiguration()
										.getNumberOfColumnsFirstClass()
										)
								)
						);
			else
				textFieldNumberOfColumnsFirst.setText("0");
			//---------------------------------------------------------------------------------
			// SEATS PITCH ECONOMY:
			if(Main.getTheAircraft().getCabinConfiguration().getPitchEconomyClass() != null) {
				
				textFieldSeatsPitchEconomy.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getPitchEconomyClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchEconomyClass().getUnit().toString().equalsIgnoreCase("m"))
					cabinConfigurationSeatsPitchEconomyUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchEconomyClass().getUnit().toString().equalsIgnoreCase("ft"))
					cabinConfigurationSeatsPitchEconomyUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else {
				textFieldSeatsPitchEconomy.setText("0.0");
				cabinConfigurationSeatsPitchEconomyUnitChoiceBox.getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// SEATS PITCH BUSINESS:
			if(Main.getTheAircraft().getCabinConfiguration().getPitchBusinessClass() != null) {
				
				textFieldSeatsPitchBusiness.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getPitchBusinessClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchBusinessClass().getUnit().toString().equalsIgnoreCase("m"))
					cabinConfigurationSeatsPitchBusinessUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchBusinessClass().getUnit().toString().equalsIgnoreCase("ft"))
					cabinConfigurationSeatsPitchBusinessUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else {
				textFieldSeatsPitchBusiness.setText("0.0");
				cabinConfigurationSeatsPitchBusinessUnitChoiceBox.getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// SEATS PITCH FIRST:
			if(Main.getTheAircraft().getCabinConfiguration().getPitchFirstClass() != null) {
				
				textFieldSeatsPitchFirst.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getPitchFirstClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchFirstClass().getUnit().toString().equalsIgnoreCase("m"))
					cabinConfigurationSeatsPitchFirstUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchFirstClass().getUnit().toString().equalsIgnoreCase("ft"))
					cabinConfigurationSeatsPitchFirstUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else {
				textFieldSeatsPitchFirst.setText("0.0");
				cabinConfigurationSeatsPitchFirstUnitChoiceBox.getSelectionModel().select(0);
			}
			//---------------------------------------------------------------------------------
			// SEATS WIDTH ECONOMY:
			if(Main.getTheAircraft().getCabinConfiguration().getWidthEconomyClass() != null) {
				
				textFieldSeatsWidthEconomy.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getWidthEconomyClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthEconomyClass().getUnit().toString().equalsIgnoreCase("m"))
					cabinConfigurationSeatsWidthEconomyUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthEconomyClass().getUnit().toString().equalsIgnoreCase("ft"))
					cabinConfigurationSeatsWidthEconomyUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else {
				textFieldSeatsWidthEconomy.setText("0.0");
				cabinConfigurationSeatsWidthEconomyUnitChoiceBox.getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// SEATS WIDTH BUSINESS:
			if(Main.getTheAircraft().getCabinConfiguration().getWidthBusinessClass() != null) {
				
				textFieldSeatsWidthBusiness.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getWidthBusinessClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthBusinessClass().getUnit().toString().equalsIgnoreCase("m"))
					cabinConfigurationSeatsWidthBusinessUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthBusinessClass().getUnit().toString().equalsIgnoreCase("ft"))
					cabinConfigurationSeatsWidthBusinessUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else {
				textFieldSeatsWidthBusiness.setText("0.0");
				cabinConfigurationSeatsWidthBusinessUnitChoiceBox.getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// SEATS WIDTH FIRST:
			if(Main.getTheAircraft().getCabinConfiguration().getWidthFirstClass() != null) {
				
				textFieldSeatsWidthFirst.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getWidthFirstClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthFirstClass().getUnit().toString().equalsIgnoreCase("m"))
					cabinConfigurationSeatsWidthFirstUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthFirstClass().getUnit().toString().equalsIgnoreCase("ft"))
					cabinConfigurationSeatsWidthFirstUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else {
				textFieldSeatsWidthFirst.setText("0.0");
				cabinConfigurationSeatsWidthFirstUnitChoiceBox.getSelectionModel().select(0);
			}
			//---------------------------------------------------------------------------------
			// DISTANCE FROM WALL ECONOMY:
			if(Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallEconomyClass() != null) {
				
				textFieldDistanceFromWallEconomy.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getDistanceFromWallEconomyClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallEconomyClass().getUnit().toString().equalsIgnoreCase("m"))
					cabinConfigurationDistanceFromWallEconomyUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallEconomyClass().getUnit().toString().equalsIgnoreCase("ft"))
					cabinConfigurationDistanceFromWallEconomyUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else {
				textFieldDistanceFromWallEconomy.setText("0.0");
				cabinConfigurationDistanceFromWallEconomyUnitChoiceBox.getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// DISTANCE FROM WALL BUSINESS:
			if(Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallBusinessClass() != null) {
				
				textFieldDistanceFromWallBusiness.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getDistanceFromWallBusinessClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallBusinessClass().getUnit().toString().equalsIgnoreCase("m"))
					cabinConfigurationDistanceFromWallBusinessUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallBusinessClass().getUnit().toString().equalsIgnoreCase("ft"))
					cabinConfigurationDistanceFromWallBusinessUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else {
				textFieldDistanceFromWallBusiness.setText("0.0");
				cabinConfigurationDistanceFromWallBusinessUnitChoiceBox.getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// DISTANCE FROM WALL FIRST:
			if(Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallFirstClass() != null) {
				
				textFieldDistanceFromWallFirst.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getDistanceFromWallFirstClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallFirstClass().getUnit().toString().equalsIgnoreCase("m"))
					cabinConfigurationDistanceFromWallFirstUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallFirstClass().getUnit().toString().equalsIgnoreCase("ft"))
					cabinConfigurationDistanceFromWallFirstUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else {
				textFieldDistanceFromWallFirst.setText("0.0");
				cabinConfigurationDistanceFromWallFirstUnitChoiceBox.getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// MASS FURNISHINGS AND EQUIPMENT:
			if(Main.getTheAircraft().getCabinConfiguration().getMassFurnishingsAndEquipmentReference() != null) {
				
				textFieldMassFurnishingsAndEquipment.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getMassFurnishingsAndEquipmentReference()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getMassFurnishingsAndEquipmentReference().getUnit().toString().equalsIgnoreCase("kg"))
					cabinConfigurationMassFurnishingsAndEquipmentUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getMassFurnishingsAndEquipmentReference().getUnit().toString().equalsIgnoreCase("lb"))
					cabinConfigurationMassFurnishingsAndEquipmentUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else {
				textFieldMassFurnishingsAndEquipment.setText("0.0");
				cabinConfigurationMassFurnishingsAndEquipmentUnitChoiceBox.getSelectionModel().select(0);
			}
		}
	}
	
	private void createWingPlanformView() {
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		
		XYSeries seriesWingTopView = new XYSeries("Wing Planform", false);
		IntStream.range(0, dataTopViewIsolated.length)
		.forEach(i -> {
			seriesWingTopView.add(
					dataTopViewIsolated[i][1],
					dataTopViewIsolated[i][0]
					);
		});
		seriesWingTopView.add(
				dataTopViewIsolated[0][1],
				dataTopViewIsolated[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from flaps
		//--------------------------------------------------
		List<XYSeries> seriesFlapsTopViewList = new ArrayList<>();
		if (!Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++) {
				
				double yIn = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getOuterChordRatio();
				
				XYSeries seriesFlapsTopView = new XYSeries("Flap" + i, false);
				seriesFlapsTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
				seriesFlapsTopView.add(
						xLELocalInnerChord + (localChordInner),
						yIn
						);
				seriesFlapsTopView.add(
						xLELocalOuterChord + (localChordOuter),
						yOut
						);
				seriesFlapsTopView.add(
						xLELocalOuterChord + (localChordOuter*(1-outerChordRatio)),
						yOut
						);
				seriesFlapsTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
			
				seriesFlapsTopViewList.add(seriesFlapsTopView);
			}
		}
		//--------------------------------------------------
		// get data vectors from slats
		//--------------------------------------------------
		List<XYSeries> seriesSlatsTopViewList = new ArrayList<>();
		if (!Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSlats().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSlats().size(); i++) {
				
				double yIn = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSlats().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSlats().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSlats().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSlats().get(i).getOuterChordRatio();
				
				XYSeries seriesSlatTopView = new XYSeries("Slat" + i, false);
				seriesSlatTopView.add(
						xLELocalInnerChord,
						yIn
						);
				seriesSlatTopView.add(
						xLELocalInnerChord + (innerChordRatio*localChordInner),
						yIn
						);
				seriesSlatTopView.add(
						xLELocalOuterChord + (outerChordRatio*localChordOuter),
						yOut
						);
				seriesSlatTopView.add(
						xLELocalOuterChord,
						yOut
						);
				seriesSlatTopView.add(
						xLELocalInnerChord,
						yIn
						);

				seriesSlatsTopViewList.add(seriesSlatTopView);
			}
		}
		//--------------------------------------------------
		// get data vectors from ailerons
		//--------------------------------------------------
		XYSeries seriesAileronTopView = new XYSeries("Right Slat", false);
		
		if (!Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getAsymmetricFlaps().isEmpty()) {
			
			double yIn = Main.getTheAircraft().getWing().getSemiSpan().times(
					Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getAsymmetricFlaps().get(0).getInnerStationSpanwisePosition()
					).doubleValue(SI.METER);
			double yOut = Main.getTheAircraft().getWing().getSemiSpan().times(
					Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getAsymmetricFlaps().get(0).getOuterStationSpanwisePosition()
					).doubleValue(SI.METER);

			double localChordInner = GeometryCalc.getChordAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedChords()),
					yIn);
			double localChordOuter = GeometryCalc.getChordAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedChords()),
					yOut);

			double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle()),
					yIn);
			double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle()),
					yOut);

			double innerChordRatio = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getAsymmetricFlaps().get(0).getInnerChordRatio();
			double outerChordRatio = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getAsymmetricFlaps().get(0).getOuterChordRatio();

			seriesAileronTopView.add(
					xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
					yIn
					);
			seriesAileronTopView.add(
					xLELocalInnerChord + (localChordInner),
					yIn
					);
			seriesAileronTopView.add(
					xLELocalOuterChord + (localChordOuter),
					yOut
					);
			seriesAileronTopView.add(
					xLELocalOuterChord + (localChordOuter*(1-outerChordRatio)),
					yOut
					);
			seriesAileronTopView.add(
					xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
					yIn
					);

		}
		//--------------------------------------------------
		// get data vectors from spoilers
		//--------------------------------------------------
		List<XYSeries> seriesSpoilersTopViewList = new ArrayList<>();
		if (!Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSpoilers().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSpoilers().size(); i++) {
				
				double yIn = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSpoilers().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSpoilers().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle()),
						yOut);
				
				double innerChordwisePosition = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSpoilers().get(i).getInnerStationChordwisePosition();
				double outerChordwisePosition = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSpoilers().get(i).getOuterStationChordwisePosition();
				
				XYSeries seriesSpoilerTopView = new XYSeries("Spoiler" + i, false);
				seriesSpoilerTopView.add(
						xLELocalInnerChord + (localChordInner*innerChordwisePosition),
						yIn
						);
				seriesSpoilerTopView.add(
						xLELocalInnerChord + (localChordInner*outerChordwisePosition),
						yIn
						);
				seriesSpoilerTopView.add(
						xLELocalOuterChord + (localChordOuter*outerChordwisePosition),
						yOut
						);
				seriesSpoilerTopView.add(
						xLELocalOuterChord + (localChordOuter*innerChordwisePosition),
						yOut
						);
				seriesSpoilerTopView.add(
						xLELocalInnerChord + (localChordInner*innerChordwisePosition),
						yIn
						);

				seriesSpoilersTopViewList.add(seriesSpoilerTopView);
			}
		}
		
		double semispan = Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().get(
				Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesFlapsTopViewList.stream().forEach(
				flap -> seriesAndColorList.add(Tuple.of(flap, Color.decode("#778899")))
				);
		seriesSlatsTopViewList.stream().forEach(
				slat -> seriesAndColorList.add(Tuple.of(slat, Color.decode("#6495ED")))
				);
		seriesAndColorList.add(Tuple.of(seriesAileronTopView, Color.decode("#1E90FF")));
		seriesSpoilersTopViewList.stream().forEach(
				spoiler -> seriesAndColorList.add(Tuple.of(spoiler, Color.decode("#ADD8E6")))
				);
		seriesAndColorList.add(Tuple.of(seriesWingTopView, Color.decode("#87CEFA")));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Wing Planform representation", 
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
		range.setInverted(Boolean.FALSE);

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
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "WingPlanform.svg";
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
		wingPlanformPane.getChildren().add(sceneTopView.getRoot());
	}
	
	private void createEquivalentWingView() {
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		
		XYSeries seriesWingTopView = new XYSeries("Wing Planform", false);
		IntStream.range(0, dataTopViewIsolated.length)
		.forEach(i -> {
			seriesWingTopView.add(
					dataTopViewIsolated[i][1],
					dataTopViewIsolated[i][0]
					);
		});
		seriesWingTopView.add(
				dataTopViewIsolated[0][1],
				dataTopViewIsolated[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from equivalent wing discretization
		//--------------------------------------------------
		int nSec = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().size();
		int nPanels = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().size();

		XYSeries seriesEquivalentWingTopView = new XYSeries("Equivalent Wing", false);
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getXOffsetEquivalentWingRootLE().doubleValue(),
				0.0
				);
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().get(nSec - 1).doubleValue(SI.METER),
				Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER)
				);
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().get(nSec - 1).plus(
						Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().get(nPanels - 1).getChordTip()
						).doubleValue(SI.METER),
				Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER)
				);
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
				- Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getXOffsetEquivalentWingRootTE(),
				0.0
				);
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getXOffsetEquivalentWingRootLE().doubleValue(),
				0.0
				);
		
		XYSeries seriesMeanAerodinamicChordView = new XYSeries("M.A.C.", false);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE),
				Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METRE),
				Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		
		double semispan = Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().get(
				Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
			
		int WIDTH = 700;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesEquivalentWingTopView, Color.decode("#87CEFA")));
		seriesAndColorList.add(Tuple.of(seriesWingTopView, Color.decode("#87CEFA")));
		seriesAndColorList.add(Tuple.of(seriesMeanAerodinamicChordView, Color.BLACK));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Wing Planform representation", 
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
		range.setInverted(Boolean.FALSE);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		xyAreaRenderer.setDefaultSeriesVisible(false);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if(i==0)
				xyAreaRenderer.setSeriesVisible(i, true);
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		xyLineAndShapeRenderer.setDrawSeriesLineAsPath(true);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if (i==1 || i==2) {
				xyLineAndShapeRenderer.setSeriesStroke(
						i,
						new BasicStroke(
								2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
								1.0f, new float[] {5.0f}, 1.0f));
			}
			else {
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
			}
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "EquivalentWing.svg";
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
		equivalentWingPane.getChildren().add(sceneTopView.getRoot());
	}
	
	private void logWingFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		textAreaWingConsoleOutput.setText(
				Main.getTheAircraft().getWing().getLiftingSurfaceCreator().toString()
				);

		if(Main.getTheAircraft().getWing() != null) {

			//---------------------------------------------------------------------------------
			// ADJUST CRITERION CHOICE BOX:
			if(Main.getTheAircraft() != null)
				wingAdjustCriterionChoiceBox.setDisable(false);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING FLAG: 
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getEquivalentWingFlag().equals(Boolean.TRUE))
				equivalentWingCheckBox.setSelected(true);

			//---------------------------------------------------------------------------------
			// MAIN SPAR LOCATION:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getMainSparNonDimensionalPosition() != null) 
				textFieldWingMainSparAdimensionalPosition.setText(
						Double.toString(
								Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getMainSparNonDimensionalPosition()
								)
						);
			else
				textFieldWingMainSparAdimensionalPosition.setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// SECONDARY SPAR LOCATION:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSecondarySparNonDimensionalPosition() != null) 
				textFieldWingSecondarySparAdimensionalPosition.setText(
						Double.toString(
								Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getSecondarySparNonDimensionalPosition()
								)
						);
			else
				textFieldWingSecondarySparAdimensionalPosition.setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// COMPOSITE CORRECTION FACTOR:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getCompositeCorrectionFactor() != null) 
				textFieldWingCompositeMassCorrectionFactor.setText(
						Double.toString(
								Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getCompositeCorrectionFactor()
								)
						);
			else
				textFieldWingCompositeMassCorrectionFactor.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// ROUGHNESS:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getRoughness() != null) {
				
				textFieldWingRoughness.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getRoughness()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator()
						.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
					wingRoughnessUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator()
						.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
					wingRoughnessUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else
				textFieldWingRoughness.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// WINGLET HEIGHT:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getWingletHeight() != null) {
				
				textFieldWingWingletHeight.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getWingletHeight()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator()
						.getWingletHeight().getUnit().toString().equalsIgnoreCase("m"))
					wingWingletHeightUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator()
						.getWingletHeight().getUnit().toString().equalsIgnoreCase("ft"))
					wingWingletHeightUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else
				textFieldWingWingletHeight.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING AREA:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSurfacePlanform() != null) {
				
				textFieldEquivalentWingArea.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getSurfacePlanform()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator()
						.getSurfacePlanform().getUnit().toString().equalsIgnoreCase("m²"))
					equivalentWingAreaUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator()
						.getSurfacePlanform().getUnit().toString().equalsIgnoreCase("ft²"))
					equivalentWingAreaUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else
				textFieldEquivalentWingArea.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING ASPECT RATIO:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getAspectRatio() != null) {
				
				textFieldEquivalentWingAspectRatio.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getAspectRatio()
								)
						);
			}
			else
				textFieldEquivalentWingAspectRatio.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING KINK POSITION:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getNonDimensionalSpanStationKink() != null) {
				
				textFieldEquivalentWingKinkPosition.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getEquivalentWing()
								.getNonDimensionalSpanStationKink()
								)
						);
			}
			else
				textFieldEquivalentWingKinkPosition.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING SWEEP LE:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge() != null) {
				
				textFieldEquivalentWingSweepLeadingEdge.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getEquivalentWing()
								.getPanels().get(0)
								.getSweepLeadingEdge()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
						.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("°")
						|| Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
						.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
					equivalentWingSweepLEUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
						.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
					equivalentWingSweepLEUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else
				textFieldEquivalentWingSweepLeadingEdge.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING TWIST AT TIP:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip() != null) {
				
				textFieldEquivalentWingTwistAtTip.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getEquivalentWing()
								.getPanels().get(0)
								.getTwistGeometricAtTip()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
						.getTwistAerodynamicAtTip().getUnit().toString().equalsIgnoreCase("°")
						|| Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
						.getTwistAerodynamicAtTip().getUnit().toString().equalsIgnoreCase("deg"))
					equivalentWingTwistAtTipUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
						.getTwistAerodynamicAtTip().getUnit().toString().equalsIgnoreCase("rad"))
					equivalentWingTwistAtTipUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else
				textFieldEquivalentWingTwistAtTip.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING DIHEDRAL:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getDihedral() != null) {
				
				textFieldEquivalentWingDihedral.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getEquivalentWing()
								.getPanels().get(0)
								.getDihedral()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
						.getDihedral().getUnit().toString().equalsIgnoreCase("°")
						|| Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
						.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
					equivalentWingDihedralUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
						.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
					equivalentWingDihedralUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else
				textFieldEquivalentWingDihedral.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING TAPER RATIO:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio() != null) {
				
				textFieldEquivalentWingTaperRatio.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getEquivalentWing()
								.getPanels().get(0)
								.getTaperRatio()
								)
						);
			}
			else
				textFieldEquivalentWingTaperRatio.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING X OFFSET LE:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getXOffsetEquivalentWingRootLE() != null) {
				
				textFieldEquivalentWingRootXOffsetLE.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getEquivalentWing()
								.getXOffsetEquivalentWingRootLE()
								)
						);
			}
			else
				textFieldEquivalentWingRootXOffsetLE.setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// EQUIVALENT WING X OFFSET TE:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getXOffsetEquivalentWingRootTE() != null) {
				
				textFieldEquivalentWingRootXOffsetTE.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getEquivalentWing()
								.getXOffsetEquivalentWingRootTE()
								)
						);
			}
			else
				textFieldEquivalentWingRootXOffsetTE.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING AIRFOIL ROOT PATH:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getAirfoilRoot() != null) {
				
				textFieldEquivalentWingAirfoilRootPath.setText(
						String.valueOf(
								Main.getInputDirectoryPath()
								+ File.separator
								+ "Template_Aircraft"
								+ File.separator
								+ "lifting_surfaces"
								+ File.separator
								+ "airfoils"
								+ File.separator
								+ Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getEquivalentWing()
								.getPanels().get(0)
								.getAirfoilRoot()
								.getName()
								+ ".xml"
								)
						);
			}
			else
				textFieldEquivalentWingAirfoilRootPath.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING AIRFOIL KINK PATH:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getAirfoilKinkEquivalentWing() != null) {
				
				textFieldEquivalentWingAirfoilKinkPath.setText(
						String.valueOf(
								Main.getInputDirectoryPath()
								+ File.separator
								+ "Template_Aircraft"
								+ File.separator
								+ "lifting_surfaces"
								+ File.separator
								+ "airfoils"
								+ File.separator
								+ Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getAirfoilKinkEquivalentWing()
								.getName()
								+ ".xml"
								)
						);
			}
			else
				textFieldEquivalentWingAirfoilKinkPath.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING AIRFOIL TIP PATH:
			if(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getAirfoilTip() != null) {
				
				textFieldEquivalentWingAirfoilTipPath.setText(
						String.valueOf(
								Main.getInputDirectoryPath()
								+ File.separator
								+ "Template_Aircraft"
								+ File.separator
								+ "lifting_surfaces"
								+ File.separator
								+ "airfoils"
								+ File.separator
								+ Main.getTheAircraft()
								.getWing()
								.getLiftingSurfaceCreator()
								.getEquivalentWing()
								.getPanels().get(0)
								.getAirfoilTip()
								.getName()
								+ ".xml"
								)
						);
			}
			else
				textFieldEquivalentWingAirfoilTipPath.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// PANELS NUMBER CHECK:
			if (Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().size() >= 
					tabPaneWingPanels.getTabs().size()) {
				
				int iStart = tabPaneWingPanels.getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().size(); i++)
					addWingPanel();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER PANELS:
			for (int i=0; i<Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().get(i);
				
				//---------------------------------------------------------------------------------
				// LINKED TO (from the second panel on):
				if(i>0)
					if(currentPanel.isLinked())
						checkBoxWingLinkedToPreviousPanelList.get(i-1).setSelected(true);
				
				//---------------------------------------------------------------------------------
				// PANEL SPAN:
				if(currentPanel.getSpan() != null) {
					
					textFieldWingSpanPanelList.get(i).setText(
							String.valueOf(currentPanel.getSpan().getEstimatedValue())
							);
					
					if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxWingSpanPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxWingSpanPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldWingSpanPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// SWEEP LE:
				if(currentPanel.getSweepLeadingEdge() != null) {
					
					textFieldWingSweepLEPanelList.get(i).setText(
							String.valueOf(currentPanel.getSweepLeadingEdge().getEstimatedValue())
							);
					
					if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxWingSweepLEPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxWingSweepLEPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldWingSweepLEPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// DIHEDRAL:
				if(currentPanel.getDihedral() != null) {
					
					textFieldWingDihedralPanelList.get(i).setText(
							String.valueOf(currentPanel.getDihedral().getEstimatedValue())
							);
					
					if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxWingDihedralPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxWingDihedralPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldWingDihedralPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD (ROOT):
				if(currentPanel.getChordRoot() != null) {
					
					textFieldWingInnerChordPanelList.get(i).setText(
							String.valueOf(currentPanel.getChordRoot().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxWingInnerChordPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxWingInnerChordPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldWingInnerChordPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER TWIST (ROOT):
				if(currentPanel.getTwistGeometricRoot() != null) {
					
					textFieldWingInnerTwistPanelList.get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricRoot().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxWingInnerTwistPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxWingInnerTwistPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldWingInnerTwistPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER AIRFOIL PATH (ROOT):
				if(currentPanel.getAirfoilRoot().getName() != null) {

					textFieldWingInnerAirfoilPanelList.get(i).setText(
							Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().get(i).getAirfoilRootPath()
							);
				}
				else
					textFieldWingInnerAirfoilPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD (TIP):
				if(currentPanel.getChordTip() != null) {
					
					textFieldWingOuterChordPanelList.get(i).setText(
							String.valueOf(currentPanel.getChordTip().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxWingInnerChordPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxWingInnerChordPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldWingInnerChordPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER TWIST (TIP):
				if(currentPanel.getTwistGeometricAtTip() != null) {
					
					textFieldWingOuterTwistPanelList.get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricAtTip().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxWingOuterTwistPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxWingOuterTwistPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldWingOuterTwistPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER AIRFOIL PATH (TIP):
				if(currentPanel.getAirfoilTip().getName() != null) {

					textFieldWingOuterAirfoilPanelList.get(i).setText(
							Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getPanels().get(i).getAirfoilTipPath()
							);
				}
				else
					textFieldWingOuterAirfoilPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// FLAPS NUMBER CHECK:
			if (Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSymmetricFlaps().size() >= 
					tabPaneWingFlaps.getTabs().size()) {
				
				int iStart = tabPaneWingFlaps.getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++)
					addFlap();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER FLAPS:
			for (int i=0; i<Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++) {
				
				SymmetricFlapCreator currentFlap = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSymmetricFlaps().get(i);
				
				//---------------------------------------------------------------------------------
				// TYPE:
				if(currentFlap.getType() != null) {
					if(currentFlap.getType().equals(FlapTypeEnum.SINGLE_SLOTTED))
						choiceBoxWingFlapTypeList.get(i).getSelectionModel().select(0);
					else if(currentFlap.getType().equals(FlapTypeEnum.DOUBLE_SLOTTED))
						choiceBoxWingFlapTypeList.get(i).getSelectionModel().select(1);
					else if(currentFlap.getType().equals(FlapTypeEnum.PLAIN))
						choiceBoxWingFlapTypeList.get(i).getSelectionModel().select(2);
					else if(currentFlap.getType().equals(FlapTypeEnum.FOWLER))
						choiceBoxWingFlapTypeList.get(i).getSelectionModel().select(3);
					else if(currentFlap.getType().equals(FlapTypeEnum.OPTIMIZED_FOWLER))
						choiceBoxWingFlapTypeList.get(i).getSelectionModel().select(4);
					else if(currentFlap.getType().equals(FlapTypeEnum.TRIPLE_SLOTTED))
						choiceBoxWingFlapTypeList.get(i).getSelectionModel().select(5);
				}
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(currentFlap.getInnerStationSpanwisePosition() != null) {
					textFieldWingInnerPositionFlapList.get(i).setText(
							String.valueOf(currentFlap.getInnerStationSpanwisePosition())
							);
				}
				else
					textFieldWingInnerPositionFlapList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(currentFlap.getOuterStationSpanwisePosition() != null) {
					textFieldWingOuterPositionFlapList.get(i).setText(
							String.valueOf(currentFlap.getOuterStationSpanwisePosition())
							);
				}
				else
					textFieldWingOuterPositionFlapList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(currentFlap.getInnerChordRatio() != null) {
					textFieldWingInnerChordRatioFlapList.get(i).setText(
							String.valueOf(currentFlap.getInnerChordRatio())
							);
				}
				else
					textFieldWingInnerChordRatioFlapList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(currentFlap.getOuterChordRatio() != null) {
					textFieldWingOuterChordRatioFlapList.get(i).setText(
							String.valueOf(currentFlap.getOuterChordRatio())
							);
				}
				else
					textFieldWingOuterChordRatioFlapList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentFlap.getMinimumDeflection() != null) {
					
					textFieldWingMinimumDeflectionAngleFlapList.get(i).setText(
							String.valueOf(currentFlap.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentFlap.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentFlap.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxWingMinimumDeflectionAngleFlapUnitList.get(i).getSelectionModel().select(0);
					else if(currentFlap.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxWingMinimumDeflectionAngleFlapUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldWingMinimumDeflectionAngleFlapList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentFlap.getMaximumDeflection() != null) {
					
					textFieldWingMaximumDeflectionAngleFlapList.get(i).setText(
							String.valueOf(currentFlap.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentFlap.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentFlap.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxWingMaximumDeflectionAngleFlapUnitList.get(i).getSelectionModel().select(0);
					else if(currentFlap.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxWingMaximumDeflectionAngleFlapUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldWingMaximumDeflectionAngleFlapList.get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// SLATS NUMBER CHECK:
			if (Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSlats().size() >= 
					tabPaneWingSlats.getTabs().size()) {
				
				int iStart = tabPaneWingSlats.getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSlats().size(); i++)
					addSlat();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER SLATS:
			for (int i=0; i<Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSlats().size(); i++) {
				
				SlatCreator currentSlat = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSlats().get(i);
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(currentSlat.getInnerStationSpanwisePosition() != null) {
					textFieldWingInnerPositionSlatList.get(i).setText(
							String.valueOf(currentSlat.getInnerStationSpanwisePosition())
							);
				}
				else
					textFieldWingInnerPositionSlatList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(currentSlat.getOuterStationSpanwisePosition() != null) {
					textFieldWingOuterPositionSlatList.get(i).setText(
							String.valueOf(currentSlat.getOuterStationSpanwisePosition())
							);
				}
				else
					textFieldWingOuterPositionSlatList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(currentSlat.getInnerChordRatio() != null) {
					textFieldWingInnerChordRatioSlatList.get(i).setText(
							String.valueOf(currentSlat.getInnerChordRatio())
							);
				}
				else
					textFieldWingInnerChordRatioSlatList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(currentSlat.getOuterChordRatio() != null) {
					textFieldWingOuterChordRatioSlatList.get(i).setText(
							String.valueOf(currentSlat.getOuterChordRatio())
							);
				}
				else
					textFieldWingOuterChordRatioSlatList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// EXTENSION RATIO:
				if(currentSlat.getExtensionRatio() != null) {
					textFieldWingExtensionRatioSlatList.get(i).setText(
							String.valueOf(currentSlat.getExtensionRatio())
							);
				}
				else
					textFieldWingExtensionRatioSlatList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentSlat.getMinimumDeflection() != null) {
					
					textFieldWingMinimumDeflectionAngleSlatList.get(i).setText(
							String.valueOf(currentSlat.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentSlat.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentSlat.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxWingMinimumDeflectionAngleSlatUnitList.get(i).getSelectionModel().select(0);
					else if(currentSlat.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxWingMinimumDeflectionAngleSlatUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldWingMinimumDeflectionAngleSlatList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentSlat.getMaximumDeflection() != null) {
					
					textFieldWingMaximumDeflectionAngleSlatList.get(i).setText(
							String.valueOf(currentSlat.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentSlat.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentSlat.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxWingMaximumDeflectionAngleSlatUnitList.get(i).getSelectionModel().select(0);
					else if(currentSlat.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxWingMaximumDeflectionAngleSlatUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldWingMaximumDeflectionAngleSlatList.get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// LEFT AILERONS:

			AsymmetricFlapCreator leftAileron = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getAsymmetricFlaps().get(0);

			//---------------------------------------------------------------------------------
			// TYPE:
			if(leftAileron.getType() != null) {
				if(leftAileron.getType().equals(FlapTypeEnum.PLAIN))
					wingLeftAileronTypeChoichBox.getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// INNER POSITION:
			if(leftAileron.getInnerStationSpanwisePosition() != null) {
				textFieldWingInnerPositionAileronLeft.setText(
						String.valueOf(leftAileron.getInnerStationSpanwisePosition())
						);
			}
			else
				textFieldWingInnerPositionAileronLeft.setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// OUTER POSITION:
			if(leftAileron.getOuterStationSpanwisePosition() != null) {
				textFieldWingOuterPositionAileronLeft.setText(
						String.valueOf(leftAileron.getOuterStationSpanwisePosition())
						);
			}
			else
				textFieldWingOuterPositionAileronLeft.setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// INNER CHORD RATIO:
			if(leftAileron.getInnerChordRatio() != null) {
				textFieldWingInnerChordRatioAileronLeft.setText(
						String.valueOf(leftAileron.getInnerChordRatio())
						);
			}
			else
				textFieldWingInnerChordRatioAileronLeft.setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// OUTER CHORD RATIO:
			if(leftAileron.getOuterChordRatio() != null) {
				textFieldWingOuterChordRatioAileronLeft.setText(
						String.valueOf(leftAileron.getOuterChordRatio())
						);
			}
			else
				textFieldWingOuterChordRatioAileronLeft.setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// MINIMUM DEFLECTION:
			if(leftAileron.getMinimumDeflection() != null) {

				textFieldWingMinimumDeflectionAngleAileronLeft.setText(
						String.valueOf(leftAileron.getMinimumDeflection().getEstimatedValue())
						);

				if(leftAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
						|| leftAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
					wingMinimumDeflectionAngleAileronLeftUnitChoiceBox.getSelectionModel().select(0);
				else if(leftAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
					wingMinimumDeflectionAngleAileronLeftUnitChoiceBox.getSelectionModel().select(1);

			}
			else
				textFieldWingMinimumDeflectionAngleAileronLeft.setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// MAXIMUM DEFLECTION:
			if(leftAileron.getMaximumDeflection() != null) {

				textFieldWingMaximumDeflectionAngleAileronLeft.setText(
						String.valueOf(leftAileron.getMaximumDeflection().getEstimatedValue())
						);

				if(leftAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
						|| leftAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
					wingMaximumDeflectionAngleAileronLeftUnitChoiceBox.getSelectionModel().select(0);
				else if(leftAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
					wingMaximumDeflectionAngleAileronLeftUnitChoiceBox.getSelectionModel().select(1);

			}
			else
				textFieldWingMaximumDeflectionAngleAileronLeft.setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// RIGHT AILERONS:

			AsymmetricFlapCreator rightAileron = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getAsymmetricFlaps().get(1);

			//---------------------------------------------------------------------------------
			// TYPE:
			if(rightAileron.getType() != null) {
				if(rightAileron.getType().equals(FlapTypeEnum.PLAIN))
					wingRightAileronTypeChoichBox.getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// INNER POSITION:
			if(rightAileron.getInnerStationSpanwisePosition() != null) {
				textFieldWingInnerPositionAileronRight.setText(
						String.valueOf(rightAileron.getInnerStationSpanwisePosition())
						);
			}
			else
				textFieldWingInnerPositionAileronRight.setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// OUTER POSITION:
			if(rightAileron.getOuterStationSpanwisePosition() != null) {
				textFieldWingOuterPositionAileronRight.setText(
						String.valueOf(rightAileron.getOuterStationSpanwisePosition())
						);
			}
			else
				textFieldWingOuterPositionAileronRight.setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// INNER CHORD RATIO:
			if(rightAileron.getInnerChordRatio() != null) {
				textFieldWingInnerChordRatioAileronRight.setText(
						String.valueOf(rightAileron.getInnerChordRatio())
						);
			}
			else
				textFieldWingInnerChordRatioAileronRight.setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// OUTER CHORD RATIO:
			if(rightAileron.getOuterChordRatio() != null) {
				textFieldWingOuterChordRatioAileronRight.setText(
						String.valueOf(rightAileron.getOuterChordRatio())
						);
			}
			else
				textFieldWingOuterChordRatioAileronRight.setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// MINIMUM DEFLECTION:
			if(rightAileron.getMinimumDeflection() != null) {

				textFieldWingMinimumDeflectionAngleAileronRight.setText(
						String.valueOf(rightAileron.getMinimumDeflection().getEstimatedValue())
						);

				if(rightAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
						|| rightAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
					wingMinimumDeflectionAngleAileronRigthUnitChoiceBox.getSelectionModel().select(0);
				else if(rightAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
					wingMinimumDeflectionAngleAileronRigthUnitChoiceBox.getSelectionModel().select(1);

			}
			else
				textFieldWingMinimumDeflectionAngleAileronRight.setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// MAXIMUM DEFLECTION:
			if(rightAileron.getMaximumDeflection() != null) {

				textFieldWingMaximumDeflectionAngleAileronRight.setText(
						String.valueOf(rightAileron.getMaximumDeflection().getEstimatedValue())
						);

				if(rightAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
						|| rightAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
					wingMaximumDeflectionAngleAileronRightUnitChoiceBox.getSelectionModel().select(0);
				else if(rightAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
					wingMaximumDeflectionAngleAileronRightUnitChoiceBox.getSelectionModel().select(1);

			}
			else
				textFieldWingMaximumDeflectionAngleAileronRight.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// SPOILERS NUMBER CHECK:
			if (Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSpoilers().size() >= 
					tabPaneWingSpoilers.getTabs().size()) {
				
				int iStart = tabPaneWingSpoilers.getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSpoilers().size(); i++)
					addSpoiler();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER SPOILERS:
			for (int i=0; i<Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSpoilers().size(); i++) {
				
				SpoilerCreator currentSpoiler = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getSpoilers().get(i);
				
				//---------------------------------------------------------------------------------
				// INNER SPANWISE POSITION:
				if(currentSpoiler.getInnerStationSpanwisePosition() != null) {
					textFieldWingInnerSpanwisePositionSpoilerList.get(i).setText(
							String.valueOf(currentSpoiler.getInnerStationSpanwisePosition())
							);
				}
				else
					textFieldWingInnerSpanwisePositionSpoilerList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER SPANWISE POSITION:
				if(currentSpoiler.getOuterStationSpanwisePosition() != null) {
					textFieldWingOuterSpanwisePositionSpoilerList.get(i).setText(
							String.valueOf(currentSpoiler.getOuterStationSpanwisePosition())
							);
				}
				else
					textFieldWingOuterSpanwisePositionSpoilerList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORDWISE POSITION:
				if(currentSpoiler.getInnerStationChordwisePosition() != null) {
					textFieldWingInnerChordwisePositionSpoilerList.get(i).setText(
							String.valueOf(currentSpoiler.getInnerStationChordwisePosition())
							);
				}
				else
					textFieldWingInnerChordwisePositionSpoilerList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORDWISE POSITION:
				if(currentSpoiler.getOuterStationChordwisePosition() != null) {
					textFieldWingOuterChordwisePositionSpoilerList.get(i).setText(
							String.valueOf(currentSpoiler.getOuterStationChordwisePosition())
							);
				}
				else
					textFieldWingOuterChordwisePositionSpoilerList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentSpoiler.getMinimumDeflection() != null) {
					
					textFieldWingMinimumDeflectionAngleSpoilerList.get(i).setText(
							String.valueOf(currentSpoiler.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxWingMinimumDeflectionAngleSpoilerUnitList.get(i).getSelectionModel().select(0);
					else if(currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxWingMinimumDeflectionAngleSpoilerUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldWingMinimumDeflectionAngleSpoilerList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentSpoiler.getMaximumDeflection() != null) {
					
					textFieldWingMaximumDeflectionAngleSpoilerList.get(i).setText(
							String.valueOf(currentSpoiler.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxWingMaximumDeflectionAngleSpoilerUnitList.get(i).getSelectionModel().select(0);
					else if(currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxWingMaximumDeflectionAngleSpoilerUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldWingMaximumDeflectionAngleSpoilerList.get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
		}
	}
	
	private void createHTailPlanformView() {
		
		//--------------------------------------------------
		// get data vectors from HTail discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.HORIZONTAL_TAIL);
		
		XYSeries seriesHTailTopView = new XYSeries("HTail Planform", false);
		IntStream.range(0, dataTopViewIsolated.length)
		.forEach(i -> {
			seriesHTailTopView.add(
					dataTopViewIsolated[i][1],
					dataTopViewIsolated[i][0]
					);
		});
		seriesHTailTopView.add(
				dataTopViewIsolated[0][1],
				dataTopViewIsolated[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from flaps
		//--------------------------------------------------
		List<XYSeries> seriesElevatorsTopViewList = new ArrayList<>();
		if (!Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++) {
				
				double yIn = Main.getTheAircraft().getHTail().getSemiSpan().times(
						Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getHTail().getSemiSpan().times(
						Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getOuterChordRatio();
				
				XYSeries seriesElevatorTopView = new XYSeries("Elevator" + i, false);
				seriesElevatorTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
				seriesElevatorTopView.add(
						xLELocalInnerChord + (localChordInner),
						yIn
						);
				seriesElevatorTopView.add(
						xLELocalOuterChord + (localChordOuter),
						yOut
						);
				seriesElevatorTopView.add(
						xLELocalOuterChord + (localChordOuter*(1-outerChordRatio)),
						yOut
						);
				seriesElevatorTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
			
				seriesElevatorsTopViewList.add(seriesElevatorTopView);
			}
		}
		
		XYSeries seriesMeanAerodinamicChordView = new XYSeries("M.A.C.", false);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE),
				Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METRE),
				Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		
		double semispan = Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedXle().get(
				Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesMeanAerodinamicChordView, Color.BLACK));
		seriesElevatorsTopViewList.stream().forEach(
				flap -> seriesAndColorList.add(Tuple.of(flap, Color.decode("#0066CC")))
				);
		seriesAndColorList.add(Tuple.of(seriesHTailTopView, Color.decode("#00008B")));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Horizontal Tail Planform representation", 
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
		range.setInverted(Boolean.FALSE);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if(i==0)
				xyAreaRenderer.setSeriesVisible(i, false);
			else
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
			if (i==0) {
				xyLineAndShapeRenderer.setSeriesStroke(
						i,
						new BasicStroke(
								2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
								1.0f, new float[] {5.0f}, 1.0f));
			}
			else {
				xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						i, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "HTailPlanform.svg";
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
		hTailPlanformPane.getChildren().add(sceneTopView.getRoot());
	}
	
	private void logHTailFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		textAreaHTailConsoleOutput.setText(
				Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().toString()
				);

		if(Main.getTheAircraft().getHTail() != null) {

			//---------------------------------------------------------------------------------
			// ADJUST CRITERION CHOICE BOX:
			if(Main.getTheAircraft() != null)
				hTailAdjustCriterionChoiceBox.setDisable(false);
			
			//---------------------------------------------------------------------------------
			// COMPOSITE CORRECTION FACTOR:
			if(Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getCompositeCorrectionFactor() != null) 
				textFieldHTailCompositeMassCorrectionFactor.setText(
						Double.toString(
								Main.getTheAircraft()
								.getHTail()
								.getLiftingSurfaceCreator()
								.getCompositeCorrectionFactor()
								)
						);
			else
				textFieldHTailCompositeMassCorrectionFactor.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// ROUGHNESS:
			if(Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getRoughness() != null) {
				
				textFieldHTailRoughness.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getHTail()
								.getLiftingSurfaceCreator()
								.getRoughness()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getHTail().getLiftingSurfaceCreator()
						.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
					hTailRoughnessUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getHTail().getLiftingSurfaceCreator()
						.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
					hTailRoughnessUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else
				textFieldHTailRoughness.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// PANELS NUMBER CHECK:
			if (Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getPanels().size() >= 
					tabPaneHTailPanels.getTabs().size()) {
				
				int iStart = tabPaneHTailPanels.getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getPanels().size(); i++)
					addHTailPanel();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER PANELS:
			for (int i=0; i<Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getPanels().get(i);
				
				//---------------------------------------------------------------------------------
				// LINKED TO (from the second panel on):
				if(i>0)
					if(currentPanel.isLinked())
						checkBoxHTailLinkedToPreviousPanelList.get(i-1).setSelected(true);
				
				//---------------------------------------------------------------------------------
				// PANEL SPAN:
				if(currentPanel.getSpan() != null) {
					
					textFieldHTailSpanPanelList.get(i).setText(
							String.valueOf(currentPanel.getSpan().getEstimatedValue())
							);
					
					if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxHTailSpanPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxHTailSpanPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldHTailSpanPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// SWEEP LE:
				if(currentPanel.getSweepLeadingEdge() != null) {
					
					textFieldHTailSweepLEPanelList.get(i).setText(
							String.valueOf(currentPanel.getSweepLeadingEdge().getEstimatedValue())
							);
					
					if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxHTailSweepLEPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxHTailSweepLEPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldHTailSweepLEPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// DIHEDRAL:
				if(currentPanel.getDihedral() != null) {
					
					textFieldHTailDihedralPanelList.get(i).setText(
							String.valueOf(currentPanel.getDihedral().getEstimatedValue())
							);
					
					if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxHTailDihedralPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxHTailDihedralPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldHTailDihedralPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD (ROOT):
				if(currentPanel.getChordRoot() != null) {
					
					textFieldHTailInnerChordPanelList.get(i).setText(
							String.valueOf(currentPanel.getChordRoot().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxHTailInnerChordPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxHTailInnerChordPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldHTailInnerChordPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER TWIST (ROOT):
				if(currentPanel.getTwistGeometricRoot() != null) {
					
					textFieldHTailInnerTwistPanelList.get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricRoot().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxHTailInnerTwistPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxHTailInnerTwistPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldHTailInnerTwistPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER AIRFOIL PATH (ROOT):
				if(currentPanel.getAirfoilRoot().getName() != null) {

					textFieldHTailInnerAirfoilPanelList.get(i).setText(
							Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getPanels().get(i).getAirfoilRootPath()
							);
				}
				else
					textFieldHTailInnerAirfoilPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD (TIP):
				if(currentPanel.getChordTip() != null) {
					
					textFieldHTailOuterChordPanelList.get(i).setText(
							String.valueOf(currentPanel.getChordTip().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxHTailInnerChordPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxHTailInnerChordPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldHTailInnerChordPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER TWIST (TIP):
				if(currentPanel.getTwistGeometricAtTip() != null) {
					
					textFieldHTailOuterTwistPanelList.get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricAtTip().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxHTailOuterTwistPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxHTailOuterTwistPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldHTailOuterTwistPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER AIRFOIL PATH (TIP):
				if(currentPanel.getAirfoilTip().getName() != null) {

					textFieldHTailOuterAirfoilPanelList.get(i).setText(
							Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getPanels().get(i).getAirfoilTipPath()
							);
				}
				else
					textFieldHTailOuterAirfoilPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// ELEVATORS NUMBER CHECK:
			if (Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getSymmetricFlaps().size() >= 
					tabPaneHTailElevators.getTabs().size()) {
				
				int iStart = tabPaneHTailElevators.getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++)
					addElevator();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER ELEVATORS:
			for (int i=0; i<Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++) {
				
				SymmetricFlapCreator currentElevator = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(i);
				
				//---------------------------------------------------------------------------------
				// TYPE:
				if(currentElevator.getType() != null) {
					if(currentElevator.getType().equals(FlapTypeEnum.PLAIN))
						choiceBoxHTailElevatorTypeList.get(i).getSelectionModel().select(0);
				}
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(currentElevator.getInnerStationSpanwisePosition() != null) {
					textFieldHTailInnerPositionElevatorList.get(i).setText(
							String.valueOf(currentElevator.getInnerStationSpanwisePosition())
							);
				}
				else
					textFieldHTailInnerPositionElevatorList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(currentElevator.getOuterStationSpanwisePosition() != null) {
					textFieldHTailOuterPositionElevatorList.get(i).setText(
							String.valueOf(currentElevator.getOuterStationSpanwisePosition())
							);
				}
				else
					textFieldHTailOuterPositionElevatorList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(currentElevator.getInnerChordRatio() != null) {
					textFieldHTailInnerChordRatioElevatorList.get(i).setText(
							String.valueOf(currentElevator.getInnerChordRatio())
							);
				}
				else
					textFieldHTailInnerChordRatioElevatorList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(currentElevator.getOuterChordRatio() != null) {
					textFieldHTailOuterChordRatioElevatorList.get(i).setText(
							String.valueOf(currentElevator.getOuterChordRatio())
							);
				}
				else
					textFieldHTailOuterChordRatioElevatorList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentElevator.getMinimumDeflection() != null) {
					
					textFieldHTailMinimumDeflectionAngleElevatorList.get(i).setText(
							String.valueOf(currentElevator.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentElevator.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentElevator.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxHTailMinimumDeflectionAngleElevatorUnitList.get(i).getSelectionModel().select(0);
					else if(currentElevator.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxHTailMinimumDeflectionAngleElevatorUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldHTailMinimumDeflectionAngleElevatorList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentElevator.getMaximumDeflection() != null) {
					
					textFieldHTailMaximumDeflectionAngleElevatorList.get(i).setText(
							String.valueOf(currentElevator.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentElevator.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentElevator.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxHTailMaximumDeflectionAngleElevatorUnitList.get(i).getSelectionModel().select(0);
					else if(currentElevator.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxHTailMaximumDeflectionAngleElevatorUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldHTailMaximumDeflectionAngleElevatorList.get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
		}
	}
	
	private void createVTailPlanformView() {
		
		//--------------------------------------------------
		// get data vectors from VTail discretization
		//--------------------------------------------------
		Double[][] dataTopViewVTail = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);
		
		XYSeries seriesVTailTopView = new XYSeries("VTail", false);
		IntStream.range(0, dataTopViewVTail.length)
		.forEach(i -> {
			seriesVTailTopView.add(
					dataTopViewVTail[i][1],
					dataTopViewVTail[i][0]
					);
		});
		seriesVTailTopView.add(
				dataTopViewVTail[0][1],
				dataTopViewVTail[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from rudders
		//--------------------------------------------------
		List<XYSeries> seriesRudderTopViewList = new ArrayList<>();
		if (!Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++) {
				
				double yIn = Main.getTheAircraft().getVTail().getSemiSpan().times(
						Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getVTail().getSemiSpan().times(
						Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getOuterChordRatio();
				
				XYSeries seriesRudderTopView = new XYSeries("Rudder" + i, false);
				seriesRudderTopView.add(
						yIn,
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio))
						);
				seriesRudderTopView.add(
						yIn,
						xLELocalInnerChord + (localChordInner)
						);
				seriesRudderTopView.add(
						yOut,
						xLELocalOuterChord + (localChordOuter)
						);
				seriesRudderTopView.add(
						yOut,
						xLELocalOuterChord + (localChordOuter*(1-outerChordRatio))
						);
				seriesRudderTopView.add(
						yIn,
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio))
						);
			
				seriesRudderTopViewList.add(seriesRudderTopView);
			}
		}
		
		XYSeries seriesMeanAerodinamicChordView = new XYSeries("M.A.C.", false);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE),
				Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE),
				Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METRE)
				);
		
		double span = Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedXle().get(
				Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - span/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*span) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesMeanAerodinamicChordView, Color.BLACK));
		seriesRudderTopViewList.stream().forEach(
				flap -> seriesAndColorList.add(Tuple.of(flap, Color.decode("#FF8C00")))
				);
		seriesAndColorList.add(Tuple.of(seriesVTailTopView, Color.decode("#FFD700")));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Vertical Tail Planform representation", 
				"z (m)", 
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
		domain.setRange(xMinTopView, xMaxTopView);
		domain.setInverted(Boolean.FALSE);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(yMinTopView, yMaxTopView);
		range.setInverted(Boolean.FALSE);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if(i==0)
				xyAreaRenderer.setSeriesVisible(i, false);
			else
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
			if (i==0) {
				xyLineAndShapeRenderer.setSeriesStroke(
						i,
						new BasicStroke(
								2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
								1.0f, new float[] {5.0f}, 1.0f));
			}
			else {
				xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						i, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "VTailPlanform.svg";
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
		vTailPlanformPane.getChildren().add(sceneTopView.getRoot());
	}
	
	private void logVTailFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		textAreaVTailConsoleOutput.setText(
				Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().toString()
				);

		if(Main.getTheAircraft().getVTail() != null) {

			//---------------------------------------------------------------------------------
			// ADJUST CRITERION CHOICE BOX:
			if(Main.getTheAircraft() != null)
				vTailAdjustCriterionChoiceBox.setDisable(false);
			
			//---------------------------------------------------------------------------------
			// COMPOSITE CORRECTION FACTOR:
			if(Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getCompositeCorrectionFactor() != null) 
				textFieldVTailCompositeMassCorrectionFactor.setText(
						Double.toString(
								Main.getTheAircraft()
								.getVTail()
								.getLiftingSurfaceCreator()
								.getCompositeCorrectionFactor()
								)
						);
			else
				textFieldVTailCompositeMassCorrectionFactor.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// ROUGHNESS:
			if(Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getRoughness() != null) {
				
				textFieldVTailRoughness.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getVTail()
								.getLiftingSurfaceCreator()
								.getRoughness()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getVTail().getLiftingSurfaceCreator()
						.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
					vTailRoughnessUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getVTail().getLiftingSurfaceCreator()
						.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
					vTailRoughnessUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else
				textFieldVTailRoughness.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// PANELS NUMBER CHECK:
			if (Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getPanels().size() >= 
					tabPaneVTailPanels.getTabs().size()) {
				
				int iStart = tabPaneVTailPanels.getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getPanels().size(); i++)
					addVTailPanel();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER PANELS:
			for (int i=0; i<Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getPanels().get(i);
				
				//---------------------------------------------------------------------------------
				// LINKED TO (from the second panel on):
				if(i>0)
					if(currentPanel.isLinked())
						checkBoxVTailLinkedToPreviousPanelList.get(i-1).setSelected(true);
				
				//---------------------------------------------------------------------------------
				// PANEL SPAN:
				if(currentPanel.getSpan() != null) {
					
					textFieldVTailSpanPanelList.get(i).setText(
							String.valueOf(currentPanel.getSpan().getEstimatedValue())
							);
					
					if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxVTailSpanPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxVTailSpanPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldVTailSpanPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// SWEEP LE:
				if(currentPanel.getSweepLeadingEdge() != null) {
					
					textFieldVTailSweepLEPanelList.get(i).setText(
							String.valueOf(currentPanel.getSweepLeadingEdge().getEstimatedValue())
							);
					
					if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxVTailSweepLEPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxVTailSweepLEPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldVTailSweepLEPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// DIHEDRAL:
				if(currentPanel.getDihedral() != null) {
					
					textFieldVTailDihedralPanelList.get(i).setText(
							String.valueOf(currentPanel.getDihedral().getEstimatedValue())
							);
					
					if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxVTailDihedralPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxVTailDihedralPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldVTailDihedralPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD (ROOT):
				if(currentPanel.getChordRoot() != null) {
					
					textFieldVTailInnerChordPanelList.get(i).setText(
							String.valueOf(currentPanel.getChordRoot().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxVTailInnerChordPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxVTailInnerChordPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldVTailInnerChordPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER TWIST (ROOT):
				if(currentPanel.getTwistGeometricRoot() != null) {
					
					textFieldVTailInnerTwistPanelList.get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricRoot().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxVTailInnerTwistPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxVTailInnerTwistPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldVTailInnerTwistPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER AIRFOIL PATH (ROOT):
				if(currentPanel.getAirfoilRoot().getName() != null) {

					textFieldVTailInnerAirfoilPanelList.get(i).setText(
							Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getPanels().get(i).getAirfoilRootPath()
							);
				}
				else
					textFieldVTailInnerAirfoilPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD (TIP):
				if(currentPanel.getChordTip() != null) {
					
					textFieldVTailOuterChordPanelList.get(i).setText(
							String.valueOf(currentPanel.getChordTip().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxVTailInnerChordPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxVTailInnerChordPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldVTailInnerChordPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER TWIST (TIP):
				if(currentPanel.getTwistGeometricAtTip() != null) {
					
					textFieldVTailOuterTwistPanelList.get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricAtTip().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxVTailOuterTwistPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxVTailOuterTwistPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldVTailOuterTwistPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER AIRFOIL PATH (TIP):
				if(currentPanel.getAirfoilTip().getName() != null) {

					textFieldVTailOuterAirfoilPanelList.get(i).setText(
							Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getPanels().get(i).getAirfoilTipPath()
							);
				}
				else
					textFieldVTailOuterAirfoilPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// RudderS NUMBER CHECK:
			if (Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().size() >= 
					tabPaneVTailRudders.getTabs().size()) {
				
				int iStart = tabPaneVTailRudders.getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++)
					addRudder();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER RudderS:
			for (int i=0; i<Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++) {
				
				SymmetricFlapCreator currentRudder = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(i);
				
				//---------------------------------------------------------------------------------
				// TYPE:
				if(currentRudder.getType() != null) {
					if(currentRudder.getType().equals(FlapTypeEnum.PLAIN))
						choiceBoxVTailRudderTypeList.get(i).getSelectionModel().select(0);
				}
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(currentRudder.getInnerStationSpanwisePosition() != null) {
					textFieldVTailInnerPositionRudderList.get(i).setText(
							String.valueOf(currentRudder.getInnerStationSpanwisePosition())
							);
				}
				else
					textFieldVTailInnerPositionRudderList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(currentRudder.getOuterStationSpanwisePosition() != null) {
					textFieldVTailOuterPositionRudderList.get(i).setText(
							String.valueOf(currentRudder.getOuterStationSpanwisePosition())
							);
				}
				else
					textFieldVTailOuterPositionRudderList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(currentRudder.getInnerChordRatio() != null) {
					textFieldVTailInnerChordRatioRudderList.get(i).setText(
							String.valueOf(currentRudder.getInnerChordRatio())
							);
				}
				else
					textFieldVTailInnerChordRatioRudderList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(currentRudder.getOuterChordRatio() != null) {
					textFieldVTailOuterChordRatioRudderList.get(i).setText(
							String.valueOf(currentRudder.getOuterChordRatio())
							);
				}
				else
					textFieldVTailOuterChordRatioRudderList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentRudder.getMinimumDeflection() != null) {
					
					textFieldVTailMinimumDeflectionAngleRudderList.get(i).setText(
							String.valueOf(currentRudder.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentRudder.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentRudder.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxVTailMinimumDeflectionAngleRudderUnitList.get(i).getSelectionModel().select(0);
					else if(currentRudder.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxVTailMinimumDeflectionAngleRudderUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldVTailMinimumDeflectionAngleRudderList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentRudder.getMaximumDeflection() != null) {
					
					textFieldVTailMaximumDeflectionAngleRudderList.get(i).setText(
							String.valueOf(currentRudder.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentRudder.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentRudder.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxVTailMaximumDeflectionAngleRudderUnitList.get(i).getSelectionModel().select(0);
					else if(currentRudder.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxVTailMaximumDeflectionAngleRudderUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldVTailMaximumDeflectionAngleRudderList.get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
		}
	}
	
	private void createCanardPlanformView() {
		
		//--------------------------------------------------
		// get data vectors from Canard discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.CANARD);
		
		XYSeries seriesCanardTopView = new XYSeries("Canard Planform", false);
		IntStream.range(0, dataTopViewIsolated.length)
		.forEach(i -> {
			seriesCanardTopView.add(
					dataTopViewIsolated[i][1],
					dataTopViewIsolated[i][0]
					);
		});
		seriesCanardTopView.add(
				dataTopViewIsolated[0][1],
				dataTopViewIsolated[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from control surface
		//--------------------------------------------------
		List<XYSeries> seriesControlSurfacesTopViewList = new ArrayList<>();
		if (!Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++) {
				
				double yIn = Main.getTheAircraft().getCanard().getSemiSpan().times(
						Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getCanard().getSemiSpan().times(
						Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getSymmetricFlaps().get(i).getOuterChordRatio();
				
				XYSeries seriesControlSurfaceTopView = new XYSeries("Control Surface" + i, false);
				seriesControlSurfaceTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
				seriesControlSurfaceTopView.add(
						xLELocalInnerChord + (localChordInner),
						yIn
						);
				seriesControlSurfaceTopView.add(
						xLELocalOuterChord + (localChordOuter),
						yOut
						);
				seriesControlSurfaceTopView.add(
						xLELocalOuterChord + (localChordOuter*(1-outerChordRatio)),
						yOut
						);
				seriesControlSurfaceTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
			
				seriesControlSurfacesTopViewList.add(seriesControlSurfaceTopView);
			}
		}
		
		XYSeries seriesMeanAerodinamicChordView = new XYSeries("M.A.C.", false);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE),
				Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METRE),
				Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		
		double semispan = Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedXle().get(
				Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesMeanAerodinamicChordView, Color.BLACK));
		seriesControlSurfacesTopViewList.stream().forEach(
				flap -> seriesAndColorList.add(Tuple.of(flap, Color.decode("#32CD32")))
				);
		seriesAndColorList.add(Tuple.of(seriesCanardTopView, Color.decode("#228B22")));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Canard Planform representation", 
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
		range.setInverted(Boolean.FALSE);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if(i==0)
				xyAreaRenderer.setSeriesVisible(i, false);
			else
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
			if (i==0) {
				xyLineAndShapeRenderer.setSeriesStroke(
						i,
						new BasicStroke(
								2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
								1.0f, new float[] {5.0f}, 1.0f));
			}
			else {
				xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						i, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "CanardPlanform.svg";
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
		canardPlanformPane.getChildren().add(sceneTopView.getRoot());
	}
	
	private void logCanardFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		textAreaCanardConsoleOutput.setText(
				Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().toString()
				);

		if(Main.getTheAircraft().getCanard() != null) {

			//---------------------------------------------------------------------------------
			// ADJUST CRITERION CHOICE BOX:
			if(Main.getTheAircraft() != null)
				canardAdjustCriterionChoiceBox.setDisable(false);
			
			//---------------------------------------------------------------------------------
			// COMPOSITE CORRECTION FACTOR:
			if(Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getCompositeCorrectionFactor() != null) 
				textFieldCanardCompositeMassCorrectionFactor.setText(
						Double.toString(
								Main.getTheAircraft()
								.getCanard()
								.getLiftingSurfaceCreator()
								.getCompositeCorrectionFactor()
								)
						);
			else
				textFieldCanardCompositeMassCorrectionFactor.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// ROUGHNESS:
			if(Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getRoughness() != null) {
				
				textFieldCanardRoughness.setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCanard()
								.getLiftingSurfaceCreator()
								.getRoughness()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCanard().getLiftingSurfaceCreator()
						.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
					canardRoughnessUnitChoiceBox.getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCanard().getLiftingSurfaceCreator()
						.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
					canardRoughnessUnitChoiceBox.getSelectionModel().select(1);
				
			}
			else
				textFieldCanardRoughness.setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// PANELS NUMBER CHECK:
			if (Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getPanels().size() >= 
					tabPaneCanardPanels.getTabs().size()) {
				
				int iStart = tabPaneCanardPanels.getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getPanels().size(); i++)
					addCanardPanel();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER PANELS:
			for (int i=0; i<Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getPanels().get(i);
				
				//---------------------------------------------------------------------------------
				// LINKED TO (from the second panel on):
				if(i>0)
					if(currentPanel.isLinked())
						checkBoxCanardLinkedToPreviousPanelList.get(i-1).setSelected(true);
				
				//---------------------------------------------------------------------------------
				// PANEL SPAN:
				if(currentPanel.getSpan() != null) {
					
					textFieldCanardSpanPanelList.get(i).setText(
							String.valueOf(currentPanel.getSpan().getEstimatedValue())
							);
					
					if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxCanardSpanPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxCanardSpanPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldCanardSpanPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// SWEEP LE:
				if(currentPanel.getSweepLeadingEdge() != null) {
					
					textFieldCanardSweepLEPanelList.get(i).setText(
							String.valueOf(currentPanel.getSweepLeadingEdge().getEstimatedValue())
							);
					
					if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxCanardSweepLEPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxCanardSweepLEPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldCanardSweepLEPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// DIHEDRAL:
				if(currentPanel.getDihedral() != null) {
					
					textFieldCanardDihedralPanelList.get(i).setText(
							String.valueOf(currentPanel.getDihedral().getEstimatedValue())
							);
					
					if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxCanardDihedralPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxCanardDihedralPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldCanardDihedralPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD (ROOT):
				if(currentPanel.getChordRoot() != null) {
					
					textFieldCanardInnerChordPanelList.get(i).setText(
							String.valueOf(currentPanel.getChordRoot().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxCanardInnerChordPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxCanardInnerChordPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldCanardInnerChordPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER TWIST (ROOT):
				if(currentPanel.getTwistGeometricRoot() != null) {
					
					textFieldCanardInnerTwistPanelList.get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricRoot().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxCanardInnerTwistPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxCanardInnerTwistPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldCanardInnerTwistPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER AIRFOIL PATH (ROOT):
				if(currentPanel.getAirfoilRoot().getName() != null) {

					textFieldCanardInnerAirfoilPanelList.get(i).setText(
							Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getPanels().get(i).getAirfoilRootPath()
							);
				}
				else
					textFieldCanardInnerAirfoilPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD (TIP):
				if(currentPanel.getChordTip() != null) {
					
					textFieldCanardOuterChordPanelList.get(i).setText(
							String.valueOf(currentPanel.getChordTip().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxCanardInnerChordPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxCanardInnerChordPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldCanardInnerChordPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER TWIST (TIP):
				if(currentPanel.getTwistGeometricAtTip() != null) {
					
					textFieldCanardOuterTwistPanelList.get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricAtTip().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxCanardOuterTwistPanelUnitList.get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxCanardOuterTwistPanelUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldCanardOuterTwistPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER AIRFOIL PATH (TIP):
				if(currentPanel.getAirfoilTip().getName() != null) {

					textFieldCanardOuterAirfoilPanelList.get(i).setText(
							Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getPanels().get(i).getAirfoilTipPath()
							);
				}
				else
					textFieldCanardOuterAirfoilPanelList.get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// ControlSurfaceS NUMBER CHECK:
			if (Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getSymmetricFlaps().size() >= 
					tabPaneCanardControlSurfaces.getTabs().size()) {
				
				int iStart = tabPaneCanardControlSurfaces.getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++)
					addControlSurface();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER ControlSurfaces:
			for (int i=0; i<Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++) {
				
				SymmetricFlapCreator currentControlSurface = Main.getTheAircraft().getCanard().getLiftingSurfaceCreator().getSymmetricFlaps().get(i);
				
				//---------------------------------------------------------------------------------
				// TYPE:
				if(currentControlSurface.getType() != null) {
					if(currentControlSurface.getType().equals(FlapTypeEnum.PLAIN))
						choiceBoxCanardControlSurfaceTypeList.get(i).getSelectionModel().select(0);
				}
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(currentControlSurface.getInnerStationSpanwisePosition() != null) {
					textFieldCanardInnerPositionControlSurfaceList.get(i).setText(
							String.valueOf(currentControlSurface.getInnerStationSpanwisePosition())
							);
				}
				else
					textFieldCanardInnerPositionControlSurfaceList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(currentControlSurface.getOuterStationSpanwisePosition() != null) {
					textFieldCanardOuterPositionControlSurfaceList.get(i).setText(
							String.valueOf(currentControlSurface.getOuterStationSpanwisePosition())
							);
				}
				else
					textFieldCanardOuterPositionControlSurfaceList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(currentControlSurface.getInnerChordRatio() != null) {
					textFieldCanardInnerChordRatioControlSurfaceList.get(i).setText(
							String.valueOf(currentControlSurface.getInnerChordRatio())
							);
				}
				else
					textFieldCanardInnerChordRatioControlSurfaceList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(currentControlSurface.getOuterChordRatio() != null) {
					textFieldCanardOuterChordRatioControlSurfaceList.get(i).setText(
							String.valueOf(currentControlSurface.getOuterChordRatio())
							);
				}
				else
					textFieldCanardOuterChordRatioControlSurfaceList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentControlSurface.getMinimumDeflection() != null) {
					
					textFieldCanardMinimumDeflectionAngleControlSurfaceList.get(i).setText(
							String.valueOf(currentControlSurface.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentControlSurface.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentControlSurface.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList.get(i).getSelectionModel().select(0);
					else if(currentControlSurface.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList.get(i).getSelectionModel().select(1);
					
				}
				else
					textFieldCanardMinimumDeflectionAngleControlSurfaceList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentControlSurface.getMaximumDeflection() != null) {
					
					textFieldCanardMaximumDeflectionAngleControlSurfaceList.get(i).setText(
							String.valueOf(currentControlSurface.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentControlSurface.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentControlSurface.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						choiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList.get(i).getSelectionModel().select(0);
					else if(currentControlSurface.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						choiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList.get(i).getSelectionModel().select(1);

				}
				else
					textFieldCanardMaximumDeflectionAngleControlSurfaceList.get(i).setText(
							"NOT INITIALIZED"
							);

			}
		}
	}

	private void createNacelleTopView() {

		//---------------------------------------------------------------------------------
		// NACELLES NUMBER CHECK:
		if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
				tabPaneNacellesTopViews.getTabs().size()) {
			
			int iStart = tabPaneNacellesTopViews.getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
				
				Tab nacelleTab = new Tab("Nacelle " + (i+1));
				Pane topView = new Pane();
				nacelleTopViewPaneList.add(topView);
				nacelleTab.setContent(topView);
				tabPaneNacellesTopViews.getTabs().add(nacelleTab);
				
			}
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER NACELLES:
		for (int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

			//--------------------------------------------------
			// get data vectors from nacelle discretization
			//--------------------------------------------------

			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperY = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYRight().stream().forEach(y -> nacelleCurveUpperY.add(y));

			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerY = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYLeft().stream().forEach(y -> nacelleCurveLowerY.add(y));

			List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
			List<Amount<Length>> dataOutlineXZCurveNacelleY = new ArrayList<>();

			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j));
				dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(j));
			}

			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1) );
				dataOutlineXZCurveNacelleY.add(nacelleCurveLowerY.get(nacelleCurveXPoints-j-1));
			}

			dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0));
			dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(0));

			XYSeries seriesNacelleCruvesTopView = new XYSeries("Nacelle " + i + " XZ Curve - Top View", false);
			IntStream.range(0, dataOutlineXZCurveNacelleX.size())
			.forEach(k -> {
				seriesNacelleCruvesTopView.add(
						dataOutlineXZCurveNacelleX.get(k).doubleValue(SI.METER),
						dataOutlineXZCurveNacelleY.get(k).doubleValue(SI.METER)
						);
			});

			double xMaxTopView = 1.40*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double xMinTopView = -1.40*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double yMaxTopView = 1.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);
			double yMinTopView = -0.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);

			int WIDTH = 700;
			int HEIGHT = 600;

			//-------------------------------------------------------------------------------------
			// DATASET CRATION
			List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
			seriesAndColorList.add(Tuple.of(seriesNacelleCruvesTopView, Color.decode("#FF7F50")));

			XYSeriesCollection dataset = new XYSeriesCollection();
			seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

			//-------------------------------------------------------------------------------------
			// CHART CRATION
			JFreeChart chart = ChartFactory.createXYAreaChart(
					"Nacelle data representation - Top View", 
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
			range.setInverted(Boolean.FALSE);

			XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
			xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
			xyAreaRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyAreaRenderer.setSeriesPaint(
						j,
						seriesAndColorList.get(j)._2()
						);
			}
			XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
			xyLineAndShapeRenderer.setDefaultShapesVisible(false);
			xyLineAndShapeRenderer.setDefaultLinesVisible(true);
			xyLineAndShapeRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyLineAndShapeRenderer.setSeriesPaint(j, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						j, 
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
					+ "Nacelle_" + (i+1) + "_TopView.svg";
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
			nacelleTopViewPaneList.get(i).getChildren().add(sceneTopView.getRoot());

		}
	}
	
	private void createNacelleSideView() {

		//---------------------------------------------------------------------------------
		// NACELLES NUMBER CHECK:
		if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
				tabPaneNacellesSideViews.getTabs().size()) {
			
			int iStart = tabPaneNacellesSideViews.getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
				
				Tab nacelleTab = new Tab("Nacelle " + (i+1));
				Pane sideView = new Pane();
				nacelleSideViewPaneList.add(sideView);
				nacelleTab.setContent(sideView);
				tabPaneNacellesSideViews.getTabs().add(nacelleTab);
				
			}
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER NACELLES:
		for (int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

			//--------------------------------------------------
			// get data vectors from nacelle discretization
			//--------------------------------------------------

			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperZ = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZUpper().stream().forEach(z -> nacelleCurveUpperZ.add(z));

			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerZ = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZLower().stream().forEach(z -> nacelleCurveLowerZ.add(z));;

			List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
			List<Amount<Length>> dataOutlineXZCurveNacelleZ = new ArrayList<>();

			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(j));
			}

			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveLowerZ.get(nacelleCurveXPoints-j-1));
			}

			dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0));
			dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(0));

			XYSeries seriesNacelleCruvesSideView = new XYSeries("Nacelle " + i + " XY Curve - Side View", false);
			IntStream.range(0, dataOutlineXZCurveNacelleX.size())
			.forEach(j -> {
				seriesNacelleCruvesSideView.add(
						dataOutlineXZCurveNacelleZ.get(j).doubleValue(SI.METER),
						dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER)
						);
			});

			double xMaxSideView = 1.40*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double xMinSideView = -1.40*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double yMaxSideView = 1.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);
			double yMinSideView = -0.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);

			int WIDTH = 700;
			int HEIGHT = 600;

			//-------------------------------------------------------------------------------------
			// DATASET CRATION
			List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
			seriesAndColorList.add(Tuple.of(seriesNacelleCruvesSideView, Color.decode("#FF7F50")));

			XYSeriesCollection dataset = new XYSeriesCollection();
			seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

			//-------------------------------------------------------------------------------------
			// CHART CRATION
			JFreeChart chart = ChartFactory.createXYAreaChart(
					"Nacelle data representation - Side View", 
					"z (m)", 
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
			domain.setRange(xMinSideView, xMaxSideView);
			domain.setInverted(Boolean.FALSE);
			NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
			range.setRange(yMinSideView, yMaxSideView);
			range.setInverted(Boolean.FALSE);

			XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
			xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
			xyAreaRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyAreaRenderer.setSeriesPaint(
						j,
						seriesAndColorList.get(j)._2()
						);
			}
			XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
			xyLineAndShapeRenderer.setDefaultShapesVisible(false);
			xyLineAndShapeRenderer.setDefaultLinesVisible(true);
			xyLineAndShapeRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyLineAndShapeRenderer.setSeriesPaint(j, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						j, 
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
					+ "Nacelle_" + (i+1) + "_SideView.svg";
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

			Scene sceneSideView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
			nacelleSideViewPaneList.get(i).getChildren().add(sceneSideView.getRoot());

		}
	}
	
	private void createNacelleFrontView() {

		//---------------------------------------------------------------------------------
		// NACELLES NUMBER CHECK:
		if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
				tabPaneNacellesFrontViews.getTabs().size()) {
			
			int iStart = tabPaneNacellesFrontViews.getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
				
				Tab nacelleTab = new Tab("Nacelle " + (i+1));
				Pane frontView = new Pane();
				nacelleFrontViewPaneList.add(frontView);
				nacelleTab.setContent(frontView);
				tabPaneNacellesFrontViews.getTabs().add(nacelleTab);
				
			}
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER NACELLES:
		for (int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

			//--------------------------------------------------
			// get data vectors from nacelle discretization
			//--------------------------------------------------
			double[] angleArray = MyArrayUtils.linspace(0.0, 2*Math.PI, 100);
			double[] yCoordinate = new double[angleArray.length];
			double[] zCoordinate = new double[angleArray.length];

			double radius = Main.getTheAircraft().getNacelles()
					.getNacellesList().get(i)
					.getDiameterMax()
					.divide(2)
					.doubleValue(SI.METER);
			double y0 = 0.0;
			double z0 = 0.0;

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

			double yMaxFrontView = 1.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getDiameterMax().doubleValue(SI.METRE);
			double yMinFrontView = -1.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getDiameterMax().doubleValue(SI.METRE);
			double xMaxFrontView = yMaxFrontView;
			double xMinFrontView = yMinFrontView;

			int WIDTH = 700;
			int HEIGHT = 600;

			//-------------------------------------------------------------------------------------
			// DATASET CRATION
			List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
			seriesAndColorList.add(Tuple.of(seriesNacelleCruvesFrontView, Color.decode("#FF7F50")));

			XYSeriesCollection dataset = new XYSeriesCollection();
			seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

			//-------------------------------------------------------------------------------------
			// CHART CRATION
			JFreeChart chart = ChartFactory.createXYAreaChart(
					"Nacelle data representation - Front View", 
					"x (m)", 
					"z (m)",
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
			domain.setRange(xMinFrontView, xMaxFrontView);
			domain.setInverted(Boolean.TRUE);
			NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
			range.setRange(yMinFrontView, yMaxFrontView);
			range.setInverted(Boolean.FALSE);

			XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
			xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
			xyAreaRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyAreaRenderer.setSeriesPaint(
						j,
						seriesAndColorList.get(j)._2()
						);
			}
			XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
			xyLineAndShapeRenderer.setDefaultShapesVisible(false);
			xyLineAndShapeRenderer.setDefaultLinesVisible(true);
			xyLineAndShapeRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyLineAndShapeRenderer.setSeriesPaint(j, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						j, 
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
					+ "Nacelle_" + (i+1) + "_FrontView.svg";
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

			Scene sceneFrontView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
			nacelleFrontViewPaneList.get(i).getChildren().add(sceneFrontView.getRoot());

		}
	}
	
	private void logNacelleFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		textAreaNacelleConsoleOutput.setText(
				Main.getTheAircraft().getNacelles().toString()
				);

		if(Main.getTheAircraft().getNacelles() != null) {

			//---------------------------------------------------------------------------------
			// NACELLES NUMBER CHECK:
			if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
					tabPaneNacelles.getTabs().size()) {

				int iStart = tabPaneNacelles.getTabs().size();

				for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++)
					addNacelle();

			}

			//---------------------------------------------------------------------------------
			// LOOP OVER NACELLES:
			for (int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

				NacelleCreator currentNacelle = Main.getTheAircraft().getNacelles().getNacellesList().get(i);

				//---------------------------------------------------------------------------------
				// ROUGHNESS:
				if(currentNacelle.getRoughness() != null) {
					textFieldNacelleRoughnessList.get(i).setText(
							String.valueOf(currentNacelle.getRoughness().getEstimatedValue()));

					if(currentNacelle.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxNacelleRoughnessUnitList.get(i).getSelectionModel().select(0);
					else if(currentNacelle.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxNacelleRoughnessUnitList.get(i).getSelectionModel().select(1);

				}
				else
					textFieldNacelleRoughnessList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// LENGTH:
				if(currentNacelle.getLength() != null) {
					textFieldNacelleLengthList.get(i).setText(
							String.valueOf(currentNacelle.getLength().getEstimatedValue()));

					if(currentNacelle.getLength().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxNacelleLengthUnitList.get(i).getSelectionModel().select(0);
					else if(currentNacelle.getLength().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxNacelleLengthUnitList.get(i).getSelectionModel().select(1);

				}
				else
					textFieldNacelleLengthList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DIAMETER:
				if(currentNacelle.getDiameterMax() != null) {
					textFieldNacelleMaximumDiameterList.get(i).setText(
							String.valueOf(currentNacelle.getDiameterMax().getEstimatedValue()));

					if(currentNacelle.getDiameterMax().getUnit().toString().equalsIgnoreCase("m"))
						choiceBoxNacelleMaximumDiameterUnitList.get(i).getSelectionModel().select(0);
					else if(currentNacelle.getDiameterMax().getUnit().toString().equalsIgnoreCase("ft"))
						choiceBoxNacelleMaximumDiameterUnitList.get(i).getSelectionModel().select(1);

				}
				else
					textFieldNacelleLengthList.get(i).setText(
							"NOT INITIALIZED"
							);

				//---------------------------------------------------------------------------------
				// K INLET:
				if(currentNacelle.getKInlet() != null) {
					textFieldNacelleKInletList.get(i).setText(
							String.valueOf(currentNacelle.getKInlet()));
				}
				else
					textFieldNacelleKInletList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// K OULET:
				if(currentNacelle.getKOutlet() != null) {
					textFieldNacelleKOutletList.get(i).setText(
							String.valueOf(currentNacelle.getKOutlet()));
				}
				else
					textFieldNacelleKOutletList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// K LENGTH:
				if(currentNacelle.getKLength() != null) {
					textFieldNacelleKLengthList.get(i).setText(
							String.valueOf(currentNacelle.getKLength()));
				}
				else
					textFieldNacelleKLengthList.get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// K DIAMETER OUTLET:
				if(currentNacelle.getKDiameterOutlet() != null) {
					textFieldNacelleKDiameterOutletList.get(i).setText(
							String.valueOf(currentNacelle.getKDiameterOutlet()));
				}
				else
					textFieldNacelleKDiameterOutletList.get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void logPowerPlantFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		textAreaPowerPlantConsoleOutput.setText(
				Main.getTheAircraft().getPowerPlant().toString()
				);

		if(Main.getTheAircraft().getPowerPlant() != null) {

			//---------------------------------------------------------------------------------
			// ENGINES NUMBER CHECK:
			if (Main.getTheAircraft().getPowerPlant().getEngineList().size() >= 
					tabPaneEngines.getTabs().size()) {

				int iStart = tabPaneEngines.getTabs().size();

				for(int i=iStart; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++)
					addEngine();

			}

			//---------------------------------------------------------------------------------
			// LOOP OVER ENGINES:
			for (int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				Engine currentEngine = Main.getTheAircraft().getPowerPlant().getEngineList().get(i);
				
				switch (currentEngine.getEngineType()) {
				case TURBOFAN:
					
					powerPlantJetRadioButtonList.get(i).setSelected(true);
					showTurbojetTurboFanDataRadioButton(i);
					
					//---------------------------------------------------------------------------------
					// ENGINE TYPE:
					if(currentEngine.getEngineType() != null) 
						engineTurbojetTurbofanTypeChoiceBoxMap.get(i).getSelectionModel().select(2);
					
					//---------------------------------------------------------------------------------
					// ENGINE DATABASE:
					if(currentEngine.getEngineDatabaseName() != null) {
						engineTurbojetTurbofanDatabaseTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getEngineDatabaseName())
								);
					}
					else
						engineTurbojetTurbofanDatabaseTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE LENGTH:
					if(currentEngine.getLength() != null) {
						engineTurbojetTurbofanLengthTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getLength().getEstimatedValue())
								);
						
						if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("m"))
							engineTurbojetTurbofanLengthUnitChoiceBoxMap.get(i).getSelectionModel().select(0);
						else if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("ft"))
							engineTurbojetTurbofanLengthUnitChoiceBoxMap.get(i).getSelectionModel().select(1);
						
					}
					else
						engineTurbojetTurbofanLengthTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE DRY MASS:
					if(currentEngine.getDryMassPublicDomain() != null) {
						engineTurbojetTurbofanDryMassTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getDryMassPublicDomain().getEstimatedValue())
								);
						
						if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
							engineTurbojetTurbofanDryMassUnitChoiceBoxMap.get(i).getSelectionModel().select(0);
						else if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
							engineTurbojetTurbofanDryMassUnitChoiceBoxMap.get(i).getSelectionModel().select(1);
						
					}
					else
						engineTurbojetTurbofanDryMassTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE STATIC THRUST:
					if(currentEngine.getT0() != null) {
						engineTurbojetTurbofanStaticThrustTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getT0().getEstimatedValue())
								);
						
						if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("N"))
							engineTurbojetTurbofanStaticThrustUnitChoiceBoxMap.get(i).getSelectionModel().select(0);
						else if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("lbf"))
							engineTurbojetTurbofanStaticThrustUnitChoiceBoxMap.get(i).getSelectionModel().select(1);
						
					}
					else
						engineTurbojetTurbofanStaticThrustTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE BPR:
					if(currentEngine.getBPR() != null) 
						engineTurbojetTurbofanBPRTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getBPR())
								);
					else
						engineTurbojetTurbofanBPRTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF COMPRESSOR STAGES:
					if((Integer) currentEngine.getNumberOfCompressorStages() != null) 
						engineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getNumberOfCompressorStages())
								);
					else
						engineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF SHAFTS:
					if((Integer) currentEngine.getNumberOfShafts() != null) 
						engineTurbojetTurbofanNumberOfShaftsTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getNumberOfShafts())
								);
					else
						engineTurbojetTurbofanNumberOfShaftsTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE OVERALL PRESSURE RATIO:
					if((Double) currentEngine.getOverallPressureRatio() != null) 
						engineTurbojetTurbofanOverallPressureRatioTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getOverallPressureRatio())
								);
					else
						engineTurbojetTurbofanOverallPressureRatioTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					break;
				case TURBOJET:
					
					powerPlantJetRadioButtonList.get(i).setSelected(true);
					showTurbojetTurboFanDataRadioButton(i);
					
					//---------------------------------------------------------------------------------
					// ENGINE TYPE:
					if(currentEngine.getEngineType() != null) 
						engineTurbojetTurbofanTypeChoiceBoxMap.get(i).getSelectionModel().select(3);
					
					//---------------------------------------------------------------------------------
					// ENGINE DATABASE:
					if(currentEngine.getEngineDatabaseName() != null) {
						engineTurbojetTurbofanDatabaseTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getEngineDatabaseName())
								);
					}
					else
						engineTurbojetTurbofanDatabaseTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE LENGTH:
					if(currentEngine.getLength() != null) {
						engineTurbojetTurbofanLengthTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getLength().getEstimatedValue())
								);
						
						if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("m"))
							engineTurbojetTurbofanLengthUnitChoiceBoxMap.get(i).getSelectionModel().select(0);
						else if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("ft"))
							engineTurbojetTurbofanLengthUnitChoiceBoxMap.get(i).getSelectionModel().select(1);
						
					}
					else
						engineTurbojetTurbofanLengthTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE DRY MASS:
					if(currentEngine.getDryMassPublicDomain() != null) {
						engineTurbojetTurbofanDryMassTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getDryMassPublicDomain().getEstimatedValue())
								);
						
						if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
							engineTurbojetTurbofanDryMassUnitChoiceBoxMap.get(i).getSelectionModel().select(0);
						else if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
							engineTurbojetTurbofanDryMassUnitChoiceBoxMap.get(i).getSelectionModel().select(1);
						
					}
					else
						engineTurbojetTurbofanDryMassTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE STATIC THRUST:
					if(currentEngine.getT0() != null) {
						engineTurbojetTurbofanStaticThrustTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getT0().getEstimatedValue())
								);
						
						if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("N"))
							engineTurbojetTurbofanStaticThrustUnitChoiceBoxMap.get(i).getSelectionModel().select(0);
						else if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("lbf"))
							engineTurbojetTurbofanStaticThrustUnitChoiceBoxMap.get(i).getSelectionModel().select(1);
						
					}
					else
						engineTurbojetTurbofanStaticThrustTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE BPR:
					if(currentEngine.getBPR() != null) 
						engineTurbojetTurbofanBPRTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getBPR())
								);
					else
						engineTurbojetTurbofanBPRTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF COMPRESSOR STAGES:
					if((Integer) currentEngine.getNumberOfCompressorStages() != null) 
						engineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getNumberOfCompressorStages())
								);
					else
						engineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF SHAFTS:
					if((Integer) currentEngine.getNumberOfShafts() != null) 
						engineTurbojetTurbofanNumberOfShaftsTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getNumberOfShafts())
								);
					else
						engineTurbojetTurbofanNumberOfShaftsTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE OVERALL PRESSURE RATIO:
					if((Double) currentEngine.getOverallPressureRatio() != null) 
						engineTurbojetTurbofanOverallPressureRatioTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getOverallPressureRatio())
								);
					else
						engineTurbojetTurbofanOverallPressureRatioTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					break;
				case TURBOPROP:
					
					powerPlantTurbopropRadioButtonList.get(i).setSelected(true);
					showTurbopropDataRadioButton(i);
					
					//---------------------------------------------------------------------------------
					// ENGINE TYPE:
					if(currentEngine.getEngineType() != null) 
						engineTurbopropTypeChoiceBoxMap.get(i).getSelectionModel().select(1);
					
					//---------------------------------------------------------------------------------
					// ENGINE DATABASE:
					if(currentEngine.getEngineDatabaseName() != null) {
						engineTurbopropDatabaseTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getEngineDatabaseName())
								);
					}
					else
						engineTurbopropDatabaseTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE LENGTH:
					if(currentEngine.getLength() != null) {
						engineTurbopropLengthTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getLength().getEstimatedValue())
								);
						
						if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("m"))
							engineTurbopropLengthUnitChoiceBoxMap.get(i).getSelectionModel().select(0);
						else if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("ft"))
							engineTurbopropLengthUnitChoiceBoxMap.get(i).getSelectionModel().select(1);
						
					}
					else
						engineTurbopropLengthTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE DRY MASS:
					if(currentEngine.getDryMassPublicDomain() != null) {
						engineTurbopropDryMassTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getDryMassPublicDomain().getEstimatedValue())
								);
						
						if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
							engineTurbopropDryMassUnitChoiceBoxMap.get(i).getSelectionModel().select(0);
						else if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
							engineTurbopropDryMassUnitChoiceBoxMap.get(i).getSelectionModel().select(1);
						
					}
					else
						engineTurbopropDryMassTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE STATIC POWER:
					if(currentEngine.getP0() != null) {
						engineTurbopropStaticPowerTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getP0().getEstimatedValue())
								);
						
						if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("W"))
							engineTurbopropStaticPowerUnitChoiceBoxMap.get(i).getSelectionModel().select(0);
						else if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("hp"))
							engineTurbopropStaticPowerUnitChoiceBoxMap.get(i).getSelectionModel().select(1);
						
					}
					else
						engineTurbopropStaticPowerTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE PROPELLER DIAMETER:
					if(currentEngine.getPropellerDiameter() != null) {
						engineTurbopropPropellerDiameterTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getPropellerDiameter().getEstimatedValue())
								);
						
						if(currentEngine.getPropellerDiameter().getUnit().toString().equalsIgnoreCase("m"))
							engineTurbopropPropellerDiameterUnitChoiceBoxMap.get(i).getSelectionModel().select(0);
						else if(currentEngine.getPropellerDiameter().getUnit().toString().equalsIgnoreCase("ft"))
							engineTurbopropPropellerDiameterUnitChoiceBoxMap.get(i).getSelectionModel().select(1);
						
					}
					else
						engineTurbopropPropellerDiameterTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF BLADES:
					if((Integer) currentEngine.getNumberOfBlades() != null) 
						engineTurbopropNumberOfBladesTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getNumberOfBlades())
								);
					else
						engineTurbopropNumberOfBladesTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE PROPELLER EFFICIENCY:
					if((Double) currentEngine.getEtaPropeller() != null) 
						engineTurbopropPropellerEfficiencyTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getEtaPropeller())
								);
					else
						engineTurbopropPropellerEfficiencyTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF COMPRESSOR STAGES:
					if((Integer) currentEngine.getNumberOfCompressorStages() != null) 
						engineTurbopropNumberOfCompressorStagesTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getNumberOfCompressorStages())
								);
					else
						engineTurbopropNumberOfCompressorStagesTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF SHAFTS:
					if((Integer) currentEngine.getNumberOfShafts() != null) 
						engineTurbopropNumberOfShaftsTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getNumberOfShafts())
								);
					else
						engineTurbopropNumberOfShaftsTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE OVERALL PRESSURE RATIO:
					if((Double) currentEngine.getOverallPressureRatio() != null) 
						engineTurbopropOverallPressureRatioTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getOverallPressureRatio())
								);
					else
						engineTurbopropOverallPressureRatioTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
										
					break;
				case PISTON:
					
					powerPlantPistonRadioButtonList.get(i).setSelected(true);
					showPistonDataRadioButton(i);
					
					//---------------------------------------------------------------------------------
					// ENGINE TYPE:
					if(currentEngine.getEngineType() != null) 
						enginePistonTypeChoiceBoxMap.get(i).getSelectionModel().select(0);
					
					//---------------------------------------------------------------------------------
					// ENGINE DATABASE:
					if(currentEngine.getEngineDatabaseName() != null) {
						enginePistonDatabaseTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getEngineDatabaseName())
								);
					}
					else
						enginePistonDatabaseTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE LENGTH:
					if(currentEngine.getLength() != null) {
						enginePistonLengthTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getLength().getEstimatedValue())
								);
						
						if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("m"))
							enginePistonLengthUnitChoiceBoxMap.get(i).getSelectionModel().select(0);
						else if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("ft"))
							enginePistonLengthUnitChoiceBoxMap.get(i).getSelectionModel().select(1);
						
					}
					else
						enginePistonLengthTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE DRY MASS:
					if(currentEngine.getDryMassPublicDomain() != null) {
						enginePistonDryMassTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getDryMassPublicDomain().getEstimatedValue())
								);
						
						if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
							enginePistonDryMassUnitChoiceBoxMap.get(i).getSelectionModel().select(0);
						else if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
							enginePistonDryMassUnitChoiceBoxMap.get(i).getSelectionModel().select(1);
						
					}
					else
						enginePistonDryMassTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE STATIC POWER:
					if(currentEngine.getP0() != null) {
						enginePistonStaticPowerTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getP0().getEstimatedValue())
								);
						
						if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("W"))
							enginePistonStaticPowerUnitChoiceBoxMap.get(i).getSelectionModel().select(0);
						else if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("hp"))
							enginePistonStaticPowerUnitChoiceBoxMap.get(i).getSelectionModel().select(1);
						
					}
					else
						enginePistonStaticPowerTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE PROPELLER DIAMETER:
					if(currentEngine.getPropellerDiameter() != null) {
						enginePistonPropellerDiameterTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getPropellerDiameter().getEstimatedValue())
								);
						
						if(currentEngine.getPropellerDiameter().getUnit().toString().equalsIgnoreCase("m"))
							enginePistonPropellerDiameterUnitChoiceBoxMap.get(i).getSelectionModel().select(0);
						else if(currentEngine.getPropellerDiameter().getUnit().toString().equalsIgnoreCase("ft"))
							enginePistonPropellerDiameterUnitChoiceBoxMap.get(i).getSelectionModel().select(1);
						
					}
					else
						enginePistonPropellerDiameterTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF BLADES:
					if((Integer) currentEngine.getNumberOfBlades() != null) 
						enginePistonNumberOfBladesTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getNumberOfBlades())
								);
					else
						enginePistonNumberOfBladesTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE PROPELLER EFFICIENCY:
					if((Double) currentEngine.getEtaPropeller() != null) 
						enginePistonPropellerEfficiencyTextFieldMap.get(i).setText(
								String.valueOf(currentEngine.getEtaPropeller())
								);
					else
						enginePistonPropellerEfficiencyTextFieldMap.get(i).setText(
								"NOT INITIALIZED"
								);
					
					break;
				default:
					break;
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void logLandingGearsFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		textAreaLandingGearsConsoleOutput.setText(
				Main.getTheAircraft().getLandingGears().toString()
				);

		//---------------------------------------------------------------------------------
		// MAIN LEG LENGTH
		if(Main.getTheAircraft().getLandingGears().getMainLegsLenght() != null) {
			
			textFieldLandingGearsMainLegLength.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getMainLegsLenght()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getMainLegsLenght()
					.getUnit().toString().equalsIgnoreCase("m"))
				landingGearsMainLegLengthUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getMainLegsLenght()
					.getUnit().toString().equalsIgnoreCase("ft"))
				landingGearsMainLegLengthUnitChoiceBox.getSelectionModel().select(1);
			
		}
		else
			textFieldLandingGearsMainLegLength.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// K MAIN LEG LENGTH
		if(Main.getTheAircraft().getLandingGears().getKMainLegsLength() != null) {
			
			textFieldLandingGearsKMainLegLength.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getKMainLegsLength()
							)
					);
		}
		else
			textFieldLandingGearsKMainLegLength.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// DISTANCE BETWEEN WHEELS
		if(Main.getTheAircraft().getLandingGears().getDistanceBetweenWheels() != null) {
			
			textFieldLandingGearsDistanceBetweenWheels.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getDistanceBetweenWheels()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getDistanceBetweenWheels()
					.getUnit().toString().equalsIgnoreCase("m"))
				landingGearsDistanceBetweenWheelsUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getDistanceBetweenWheels()
					.getUnit().toString().equalsIgnoreCase("ft"))
				landingGearsDistanceBetweenWheelsUnitChoiceBox.getSelectionModel().select(1);
			
		}
		else
			textFieldLandingGearsDistanceBetweenWheels.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NUMBER OF FRONTAL WHEELS 
		if((Integer) Main.getTheAircraft().getLandingGears().getNumberOfFrontalWheels() != null) {
			
			textFieldLandingGearsNumberOfFrontalWheels.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getNumberOfFrontalWheels()
							)
					);
		}
		else
			textFieldLandingGearsNumberOfFrontalWheels.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NUMBER OF REAR WHEELS 
		if((Integer) Main.getTheAircraft().getLandingGears().getNumberOfRearWheels() != null) {
			
			textFieldLandingGearsNumberOfRearWheels.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getNumberOfRearWheels()
							)
					);
		}
		else
			textFieldLandingGearsNumberOfRearWheels.setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// FRONTAL WHEELS HEIGHT
		if(Main.getTheAircraft().getLandingGears().getFrontalWheelsHeight() != null) {
			
			textFieldLandingGearsFrontalWheelsHeight.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getFrontalWheelsHeight()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getFrontalWheelsHeight()
					.getUnit().toString().equalsIgnoreCase("m"))
				landingGearsFrontalWheelsHeigthUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getFrontalWheelsHeight()
					.getUnit().toString().equalsIgnoreCase("ft"))
				landingGearsFrontalWheelsHeigthUnitChoiceBox.getSelectionModel().select(1);
			
		}
		else
			textFieldLandingGearsFrontalWheelsHeight.setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// FRONTAL WHEELS WIDTH
		if(Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth() != null) {
			
			textFieldLandingGearsFrontalWheelsWidth.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getFrontalWheelsWidth()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth()
					.getUnit().toString().equalsIgnoreCase("m"))
				landingGearsFrontalWheelsWidthUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth()
					.getUnit().toString().equalsIgnoreCase("ft"))
				landingGearsFrontalWheelsWidthUnitChoiceBox.getSelectionModel().select(1);

		}
		else
			textFieldLandingGearsFrontalWheelsWidth.setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// Rear WHEELS HEIGHT
		if(Main.getTheAircraft().getLandingGears().getRearWheelsHeight() != null) {

			textFieldLandingGearsRearWheelsHeight.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getRearWheelsHeight()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getRearWheelsHeight()
					.getUnit().toString().equalsIgnoreCase("m"))
				landingGearsRearWheelsHeigthUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getRearWheelsHeight()
					.getUnit().toString().equalsIgnoreCase("ft"))
				landingGearsRearWheelsHeigthUnitChoiceBox.getSelectionModel().select(1);
			
		}
		else
			textFieldLandingGearsRearWheelsHeight.setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// Rear WHEELS WIDTH
		if(Main.getTheAircraft().getLandingGears().getRearWheelsWidth() != null) {
			
			textFieldLandingGearsRearWheelsWidth.setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getRearWheelsWidth()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getRearWheelsWidth()
					.getUnit().toString().equalsIgnoreCase("m"))
				landingGearsRearWheelsWidthUnitChoiceBox.getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getRearWheelsWidth()
					.getUnit().toString().equalsIgnoreCase("ft"))
				landingGearsRearWheelsWidthUnitChoiceBox.getSelectionModel().select(1);
			
		}
		else
			textFieldLandingGearsRearWheelsWidth.setText(
					"NOT INITIALIZED"
					);
		
	}
	
	//...........................................................................................
	// INFO ACTIONS:
	//...........................................................................................
	
	@FXML
	private void showMissingSeatRowInfo() {
		
		// TODO
		
	};
	
	@FXML
	private void showCabinConfigurationReferenceMassInfo() {
		
		// TODO
		
	};
	
	@FXML
	private void showEquivalentWingInfo() {
		
		// TODO
		
	};
	
	@FXML
	private void showEquivalentWingXOffsetLEInfo() {
		
		// TODO
		
	};
	
	@FXML
	private void showEquivalentWingXOffsetTEInfo() {
		
		// TODO
		
	};
	
	@FXML
	private void showNacelleKInletInfo() {
		
		// TODO
		
	};
	
	@FXML
	private void showNacelleKOutletInfo() {
		
		// TODO
		
	};
	
	@FXML
	private void showNacelleKLengthInfo() {
		
		// TODO
		
	};
	
	@FXML
	private void showNacelleKDiameterOutletInfo() {
		
		// TODO
		
	};
	
	@FXML
	private void showLandingGearsKMainLegLengthInfo() {
		
		// TODO
		
	};
	
	//...........................................................................................
	// LAYOUT ADJUST ACTIONS:
	//...........................................................................................
	
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
	
	@FXML
	private void zoomDataLogAndMessagesCabinConfiguration() {
		cabinConfigurationViewsAndDataLogSplitPane.setDividerPositions(0.5);
	};
	
	@FXML
	private void zoomViewsCabinConfiguration() {
		cabinConfigurationViewsAndDataLogSplitPane.setDividerPositions(0.9);
	};
	
	@FXML
	private void zoomDataLogAndMessagesWing() {
		wingViewsAndDataLogSplitPane.setDividerPositions(0.5);
	};
	
	@FXML
	private void zoomViewsWing() {
		wingViewsAndDataLogSplitPane.setDividerPositions(0.9);
	};
	
	@FXML
	private void zoomDataLogAndMessagesHTail() {
		hTailViewsAndDataLogSplitPane.setDividerPositions(0.5);
	};
	
	@FXML
	private void zoomViewsHTail() {
		hTailViewsAndDataLogSplitPane.setDividerPositions(0.9);
	};
	
	@FXML
	private void zoomDataLogAndMessagesVTail() {
		vTailViewsAndDataLogSplitPane.setDividerPositions(0.5);
	};
	
	@FXML
	private void zoomViewsVTail() {
		vTailViewsAndDataLogSplitPane.setDividerPositions(0.9);
	};
	
	@FXML
	private void zoomDataLogAndMessagesCanard() {
		canardViewsAndDataLogSplitPane.setDividerPositions(0.5);
	};
	
	@FXML
	private void zoomViewsCanard() {
		canardViewsAndDataLogSplitPane.setDividerPositions(0.9);
	};
	
	@FXML
	private void zoomDataLogAndMessagesNacelle() {
		nacelleViewsAndDataLogSplitPane.setDividerPositions(0.5);
	};
	
	@FXML
	private void zoomViewsNacelle() {
		nacelleViewsAndDataLogSplitPane.setDividerPositions(0.9);
	};
	
}
