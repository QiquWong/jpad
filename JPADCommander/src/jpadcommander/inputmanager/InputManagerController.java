package jpadcommander.inputmanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.controlsfx.control.CheckComboBox;
import org.controlsfx.validation.ValidationSupport;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.powerplant.Engine;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineTypeEnum;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jpadcommander.Main;
import standaloneutils.atmosphere.AtmosphereCalc;

/*
 * FIXME: IN ORDER TO REDUCE THE AMOUNT OF MEMORY USED FOR EACH OPERATION, TRY TO DEFINE ALL THE 
 *        EVENT HANDLER IN A SEPARATE CLASS (WITH A METHOD INITIALIZE TO BE CALLED IN THE INITIALIZE
 *        METHOD OD THIS CLASS TO DEFINE ALL THE "new EventHandler"). IN THIS WAY, FOR EACH ACTION
 *        THE SAME EVENT HANDLER WILL BE USED AVOIVING TO CREATE MANY OF THEM.
 */

public class InputManagerController {

	//-------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	//-------------------------------------------------------------------------------------------
	private InputManagerControllerMainActionUtilities inputManagerControllerMainActionUtilities;
	private InputManagerControllerSecondaryActionUtilities inputManagerControllerSecondaryActionUtilities;
	private InputManagerControllerGraphicUtilities inputManagerControllerGraphicUtilities;
	private InputManagerControllerLogUtilities inputManagerControllerLogUtilities;
	private InputManagerControllerUpdateUtilites inputManagerControllerUpdateUtilites;
	
	//...........................................................................................
	// LAYOUTS:
	//...........................................................................................
	@FXML 
	private ToolBar actionButtonToolbar;
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
	private Button chooseAircraftFileButton;	
	@FXML
	private Button loadAircraftButton;
	@FXML
	private Button newAircraftButton;
	@FXML
	private Button updateAircraftDataButton;
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
	private Button aircraftAddEngineButton;
	@FXML
	private Button aircraftAddNacelleButton;
	@FXML
	private Button fuselageAddSpoilerButton;
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
	// CHECK BOXES and UPDATE related data:
	//...........................................................................................
	private CheckComboBox<String> updateAircraftDataFromFileComboBox;
	
	private boolean updateCabinConfigurationDataFromFile;
	private boolean updateFuselageDataFromFile;
	private boolean updateWingDataFromFile;
	private boolean updateHTailDataFromFile;
	private boolean updateVTailDataFromFile;
	private boolean updateCanardDataFromFile;
	private boolean updatePowerPlantDataFromFile;
	private boolean updateNacellesDataFromFile;
	private boolean updateLandingGearsDataFromFile;

	private boolean noneAdjustCriterionFuselage = false;
	private boolean noneAdjustCriterionWing = false;
	private boolean noneAdjustCriterionHTail = false;
	private boolean noneAdjustCriterionVTail = false;
	private boolean noneAdjustCriterionCanard = false;
	
	private String fuselageXPositionValue = "";
	private String fuselageXPositionUnit = "";
	private String fuselageYPositionValue = "";
	private String fuselageYPositionUnit = "";
	private String fuselageZPositionValue = "";
	private String fuselageZPositionUnit = "";

	private String wingXPositionValue = "";
	private String wingXPositionUnit = "";
	private String wingYPositionValue = "";
	private String wingYPositionUnit = "";
	private String wingZPositionValue = "";
	private String wingZPositionUnit = "";
	private String wingRiggingAngleValue = "";
	private String wingRiggingAngleUnit = "";

	private String hTailXPositionValue = "";
	private String hTailXPositionUnit = "";
	private String hTailYPositionValue = "";
	private String hTailYPositionUnit = "";
	private String hTailZPositionValue = "";
	private String hTailZPositionUnit = "";
	private String hTailRiggingAngleValue = "";
	private String hTailRiggingAngleUnit = "";

	private String vTailXPositionValue = "";
	private String vTailXPositionUnit = "";
	private String vTailYPositionValue = "";
	private String vTailYPositionUnit = "";
	private String vTailZPositionValue = "";
	private String vTailZPositionUnit = "";
	private String vTailRiggingAngleValue = "";
	private String vTailRiggingAngleUnit = "";
	
	private String canardXPositionValue = "";
	private String canardXPositionUnit = "";
	private String canardYPositionValue = "";
	private String canardYPositionUnit = "";
	private String canardZPositionValue = "";
	private String canardZPositionUnit = "";
	private String canardRiggingAngleValue = "";
	private String canardRiggingAngleUnit = "";

	private List<String> engineXPositionValueList = new ArrayList<>();
	private List<String> engineXPositionUnitList = new ArrayList<>();
	private List<String> engineYPositionValueList = new ArrayList<>();
	private List<String> engineYPositionUnitList = new ArrayList<>();
	private List<String> engineZPositionValueList = new ArrayList<>();
	private List<String> engineZPositionUnitList = new ArrayList<>();
	private List<String> engineTiltAngleValueList = new ArrayList<>();
	private List<String> engineTiltAngleUnitList = new ArrayList<>();
	private List<String> engineMountinPositionValueList = new ArrayList<>();

	private List<String> nacelleXPositionValueList = new ArrayList<>();
	private List<String> nacelleXPositionUnitList = new ArrayList<>();
	private List<String> nacelleYPositionValueList = new ArrayList<>();
	private List<String> nacelleYPositionUnitList = new ArrayList<>();
	private List<String> nacelleZPositionValueList = new ArrayList<>();
	private List<String> nacelleZPositionUnitList = new ArrayList<>();
	private List<String> nacelleMountinPositionValueList = new ArrayList<>();

	private String noseLandingGearsXPositionValue = "";
	private String noseLandingGearsXPositionUnit = "";
	private String noseLandingGearsYPositionValue = "";
	private String noseLandingGearsYPositionUnit = "";
	private String noseLandingGearsZPositionValue = "";
	private String noseLandingGearsZPositionUnit = "";
	private String mainLandingGearsXPositionValue = "";
	private String mainLandingGearsXPositionUnit = "";
	private String mainLandingGearsYPositionValue = "";
	private String mainLandingGearsYPositionUnit = "";
	private String mainLandingGearsZPositionValue = "";
	private String mainLandingGearsZPositionUnit = "";
	private String landingGearsMountinPositionValue = "";
	
	private String systemsPrimaryElectricalTypeValue = "";

	//...........................................................................................
	// FILE CHOOSER:
	//...........................................................................................
	private FileChooser airfoilFileChooser;
	private FileChooser aircraftFileChooser;
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
	
	//...........................................................................................
	// VALIDATIONS (ControlsFX):
	//...........................................................................................
	private ValidationSupport validation = new ValidationSupport();
	
	//...........................................................................................
	// STYLES (.css):
	//...........................................................................................
	private String textFieldAlertStyle = "-fx-control-inner-background: #FFA500";
	private String buttonSuggestedActionStyle = "-fx-border-color: #32CD32; -fx-border-width: 2px;";
	
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
	ObservableList<String> primaryElectricalSystemsTypeList = FXCollections.observableArrayList(
			"AC",
			"DC"
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
			"MODIFY TAILCONE LENGTH, CONSTANT TOTAL LENGTH, DIAMETERS AND NOSE LENGTH RATIO",
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
	ObservableList<String> jetEngineTypeList = FXCollections.observableArrayList(
			"TURBOFAN",
			"TURBOJET"
			);
	ObservableList<String> turbopropEngineTypeList = FXCollections.observableArrayList(
			"TURBOPROP"
			);
	ObservableList<String> pistonEngineTypeList = FXCollections.observableArrayList(
			"PISTON"
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
	ObservableList<String> componentsList = FXCollections.observableArrayList(
			"Cabin Configuration",
			"Fuselage", 
			"Wing",
			"Horizontal Tail",
			"Vertical Tail",
			"Canard",
			"Power Plant",
			"Nacelles",
			"Landing Gears",
			"Systems"
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
	@FXML
	private ChoiceBox<String> systemsPrimaryElectricalTypeChoiceBox;
	
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
	private TextField textFieldAircraftNoseLandingGearsX;
	@FXML
	private TextField textFieldAircraftNoseLandingGearsY;
	@FXML
	private TextField textFieldAircraftNoseLandingGearsZ;
	@FXML
	private TextField textFieldAircraftMainLandingGearsX;
	@FXML
	private TextField textFieldAircraftMainLandingGearsY;
	@FXML
	private TextField textFieldAircraftMainLandingGearsZ;
	
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
	private ChoiceBox<String> noseLandingGearsXUnitChoiceBox;
	@FXML
	private ChoiceBox<String> noseLandingGearsYUnitChoiceBox;
	@FXML
	private ChoiceBox<String> noseLandingGearsZUnitChoiceBox;
	@FXML
	private ChoiceBox<String> mainLandingGearsXUnitChoiceBox;
	@FXML
	private ChoiceBox<String> mainLandingGearsYUnitChoiceBox;
	@FXML
	private ChoiceBox<String> mainLandingGearsZUnitChoiceBox;
	
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
	private ChoiceBox<String> wingMinimumDeflectionAngleAileronRightUnitChoiceBox;
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
	private TextField textFieldHTailMainSparAdimensionalPosition;
	@FXML
	private TextField textFieldHTailSecondarySparAdimensionalPosition;
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
	private TextField textFieldVTailMainSparAdimensionalPosition;
	@FXML
	private TextField textFieldVTailSecondarySparAdimensionalPosition;
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
	private TextField textFieldCanardMainSparAdimensionalPosition;
	@FXML
	private TextField textFieldCanardSecondarySparAdimensionalPosition;
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
	
	private Map<EngineTypeEnum, Pane> powerPlantPaneMap;
	private Map<Integer, Map<EngineTypeEnum, Pane>> powerPlantEngineTypePaneMap;
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
		
		inputManagerControllerMainActionUtilities = new InputManagerControllerMainActionUtilities(this);
		inputManagerControllerSecondaryActionUtilities = new InputManagerControllerSecondaryActionUtilities(this);
		inputManagerControllerLogUtilities = new InputManagerControllerLogUtilities(this);
		inputManagerControllerGraphicUtilities = new InputManagerControllerGraphicUtilities(this);
		inputManagerControllerUpdateUtilites = new InputManagerControllerUpdateUtilites(this);
		
		Main.setAircraftSaved(false);
		Main.setAircraftUpdated(false);
		Platform.setImplicitExit(false);
		
		chooseAircraftFileButton.setStyle(buttonSuggestedActionStyle);
		
		updateCabinConfigurationDataFromFile = false;
		updateFuselageDataFromFile = false;
		updateWingDataFromFile = false;
		updateHTailDataFromFile = false;
		updateVTailDataFromFile = false;
		updateCanardDataFromFile = false;
		updatePowerPlantDataFromFile = false;
		updateNacellesDataFromFile = false;
		updateLandingGearsDataFromFile = false;
		
		noneAdjustCriterionFuselage = false;
		noneAdjustCriterionWing = false;
		noneAdjustCriterionHTail = false;
		noneAdjustCriterionVTail = false;
		noneAdjustCriterionCanard = false;
		
		updateAircraftDataFromFileComboBox = new CheckComboBox<>(componentsList);
		updateAircraftDataFromFileComboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
		     public void onChanged(ListChangeListener.Change<? extends String> c) {
		    	 
		    	if (updateAircraftDataFromFileComboBox.getCheckModel().getCheckedItems().contains("Cabin Configuration"))
		    		updateCabinConfigurationDataFromFile = true;
		    	if (updateAircraftDataFromFileComboBox.getCheckModel().getCheckedItems().contains("Fuselage"))
		    		updateFuselageDataFromFile = true;
		    	if (updateAircraftDataFromFileComboBox.getCheckModel().getCheckedItems().contains("Wing"))
		    		updateWingDataFromFile = true;
		    	if (updateAircraftDataFromFileComboBox.getCheckModel().getCheckedItems().contains("Horizontal Tail"))
		    		updateHTailDataFromFile = true;
		    	if (updateAircraftDataFromFileComboBox.getCheckModel().getCheckedItems().contains("Vertical Tail"))
		    		updateVTailDataFromFile = true;
		    	if (updateAircraftDataFromFileComboBox.getCheckModel().getCheckedItems().contains("Canard"))
		    		updateCanardDataFromFile = true;
		    	if (updateAircraftDataFromFileComboBox.getCheckModel().getCheckedItems().contains("Power Plant"))
		    		updatePowerPlantDataFromFile = true;
		    	if (updateAircraftDataFromFileComboBox.getCheckModel().getCheckedItems().contains("Nacelles"))
		    		updateNacellesDataFromFile = true;
		    	if (updateAircraftDataFromFileComboBox.getCheckModel().getCheckedItems().contains("Landing Gears"))
		    		updateLandingGearsDataFromFile = true;
		     }
		 });
		actionButtonToolbar.getItems().add(updateAircraftDataFromFileComboBox);
		actionButtonToolbar.getItems().add(new Label("<- Update Components Using Files"));
		
		ObjectProperty<Aircraft> aircraft = new SimpleObjectProperty<>();
		ObjectProperty<Boolean> aircraftSavedFlag = new SimpleObjectProperty<>();

		try {
			aircraftSavedFlag.set(Main.getAircraftSaved());
			saveAircraftButton.disableProperty().bind(
					Bindings.equal(aircraftSavedFlag, true).or(Bindings.isNull(aircraft))
					);
		} catch (Exception e) {
			saveAircraftButton.setDisable(true);
		}

		try {
			updateAircraftDataButton.disableProperty().bind(
					Bindings.isNull(aircraft)
					);
			updateAircraftDataFromFileComboBox.disableProperty().bind(
					Bindings.isNull(aircraft)
					);
		} catch (Exception e) {
			updateAircraftDataButton.setDisable(true);
			updateAircraftDataFromFileComboBox.setDisable(true);
		}
		
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
			inputManagerControllerSecondaryActionUtilities.removeContentOnSpoilerTabClose(tabPaneFuselageSpoilers.getTabs().get(i), ComponentEnum.FUSELAGE);
		
		tabPaneAircraftEngines.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		for(int i=0; i<tabPaneAircraftEngines.getTabs().size(); i++)
			inputManagerControllerSecondaryActionUtilities.removeContentOnAircraftEngineTabClose(tabPaneAircraftEngines.getTabs().get(i));
		
		tabPaneAircraftNacelles.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		for(int i=0; i<tabPaneAircraftNacelles.getTabs().size(); i++)
			inputManagerControllerSecondaryActionUtilities.removeContentOnAircraftNacelleTabClose(tabPaneAircraftNacelles.getTabs().get(i));
		
		tabPaneWingPanels.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneWingFlaps.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneWingSlats.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneWingSpoilers.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneWingViewAndAirfoils.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneWingViewAndAirfoils.getTabs().get(0).closableProperty().set(false);
		tabPaneWingViewAndAirfoils.getTabs().get(1).closableProperty().set(false);
		
		for(int i=0; i<tabPaneWingPanels.getTabs().size(); i++)
			inputManagerControllerSecondaryActionUtilities.removeContentOnPanelTabClose(tabPaneWingPanels.getTabs().get(i), ComponentEnum.WING);
		for(int i=0; i<tabPaneWingFlaps.getTabs().size(); i++)
			inputManagerControllerSecondaryActionUtilities.removeContentOnFlapTabClose(tabPaneWingFlaps.getTabs().get(i), ComponentEnum.WING);
		for(int i=0; i<tabPaneWingSlats.getTabs().size(); i++)
			inputManagerControllerSecondaryActionUtilities.removeContentOnSlatTabClose(tabPaneWingSlats.getTabs().get(i));
		for(int i=0; i<tabPaneWingSpoilers.getTabs().size(); i++)
			inputManagerControllerSecondaryActionUtilities.removeContentOnSpoilerTabClose(tabPaneWingSpoilers.getTabs().get(i), ComponentEnum.WING);
		
		tabPaneHTailPanels.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneHTailElevators.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneHTailViewAndAirfoils.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneHTailViewAndAirfoils.getTabs().get(0).closableProperty().set(false);
		
		for(int i=0; i<tabPaneHTailPanels.getTabs().size(); i++)
			inputManagerControllerSecondaryActionUtilities.removeContentOnPanelTabClose(tabPaneHTailPanels.getTabs().get(i), ComponentEnum.HORIZONTAL_TAIL);
		for(int i=0; i<tabPaneHTailElevators.getTabs().size(); i++)
			inputManagerControllerSecondaryActionUtilities.removeContentOnFlapTabClose(tabPaneHTailElevators.getTabs().get(i), ComponentEnum.HORIZONTAL_TAIL);
		
		tabPaneVTailPanels.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneVTailRudders.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneVTailViewAndAirfoils.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneVTailViewAndAirfoils.getTabs().get(0).closableProperty().set(false);
		
		for(int i=0; i<tabPaneVTailPanels.getTabs().size(); i++)
			inputManagerControllerSecondaryActionUtilities.removeContentOnPanelTabClose(tabPaneVTailPanels.getTabs().get(i), ComponentEnum.VERTICAL_TAIL);
		for(int i=0; i<tabPaneVTailRudders.getTabs().size(); i++)
			inputManagerControllerSecondaryActionUtilities.removeContentOnFlapTabClose(tabPaneVTailRudders.getTabs().get(i), ComponentEnum.VERTICAL_TAIL);
		
		tabPaneCanardPanels.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneCanardControlSurfaces.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneCanardViewAndAirfoils.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		tabPaneCanardViewAndAirfoils.getTabs().get(0).closableProperty().set(false);
		
		for(int i=0; i<tabPaneCanardPanels.getTabs().size(); i++)
			inputManagerControllerSecondaryActionUtilities.removeContentOnPanelTabClose(tabPaneCanardPanels.getTabs().get(i), ComponentEnum.CANARD);
		for(int i=0; i<tabPaneCanardControlSurfaces.getTabs().size(); i++)
			inputManagerControllerSecondaryActionUtilities.removeContentOnFlapTabClose(tabPaneCanardControlSurfaces.getTabs().get(i), ComponentEnum.CANARD);
		
		tabPaneNacelles.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		for(int i=0; i<tabPaneNacelles.getTabs().size(); i++)
			inputManagerControllerSecondaryActionUtilities.removeContentOnNacelleTabClose(tabPaneAircraftNacelles.getTabs().get(i));
		
		tabPaneEngines.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		for(int i=0; i<tabPaneEngines.getTabs().size(); i++)
			inputManagerControllerSecondaryActionUtilities.removeContentOnEngineTabClose(tabPaneAircraftEngines.getTabs().get(i));
		
		//.......................................................................................
		// CHOICE BOX INITIALIZATION
		aircraftTypeChoiceBox.setItems(aircraftTypeList);
		regulationsTypeChoiceBox.setItems(regulationsTypeList);
		getSystemsPrimaryElectricalTypeChoiceBox().setItems(primaryElectricalSystemsTypeList);
		
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
		noseLandingGearsXUnitChoiceBox.setItems(lengthUnitsList);
		noseLandingGearsYUnitChoiceBox.setItems(lengthUnitsList);
		noseLandingGearsZUnitChoiceBox.setItems(lengthUnitsList);
		mainLandingGearsXUnitChoiceBox.setItems(lengthUnitsList);
		mainLandingGearsYUnitChoiceBox.setItems(lengthUnitsList);
		mainLandingGearsZUnitChoiceBox.setItems(lengthUnitsList);
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
		wingMinimumDeflectionAngleAileronRightUnitChoiceBox.setItems(angleUnitsList);
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
		landingGearsMainLegLengthUnitChoiceBox.setItems(lengthUnitsList);
		landingGearsDistanceBetweenWheelsUnitChoiceBox.setItems(lengthUnitsList);
		landingGearsFrontalWheelsHeigthUnitChoiceBox.setItems(lengthUnitsList);
		landingGearsFrontalWheelsWidthUnitChoiceBox.setItems(lengthUnitsList);
		landingGearsRearWheelsHeigthUnitChoiceBox.setItems(lengthUnitsList);
		landingGearsRearWheelsWidthUnitChoiceBox.setItems(lengthUnitsList);
		
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
		powerPlantJetRadioButton1.setUserData(0);
		powerPlantTurbopropRadioButton1.setToggleGroup(powerPlantToggleGroup1);
		powerPlantTurbopropRadioButton1.setUserData(1);
		powerPlantPistonRadioButton1.setToggleGroup(powerPlantToggleGroup1);
		powerPlantPistonRadioButton1.setUserData(2);
		
		powerPlantJetRadioButtonList = new ArrayList<>();
		powerPlantJetRadioButtonList.add(powerPlantJetRadioButton1);
		
		powerPlantTurbopropRadioButtonList = new ArrayList<>();
		powerPlantTurbopropRadioButtonList.add(powerPlantTurbopropRadioButton1);
		
		powerPlantPistonRadioButtonList = new ArrayList<>();
		powerPlantPistonRadioButtonList.add(powerPlantPistonRadioButton1);
		
		powerPlantToggleGropuList = new ArrayList<>();
		powerPlantToggleGropuList.add(powerPlantToggleGroup1);
		
		powerPlantPaneMap = new HashMap<>();
		powerPlantEngineTypePaneMap = new HashMap<>();
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
		
		inputManagerControllerSecondaryActionUtilities.aircraftLoadButtonDisableCheck();
		inputManagerControllerSecondaryActionUtilities.setChooseNacelleFileAction();
		inputManagerControllerSecondaryActionUtilities.setChooseEngineFileAction();
		inputManagerControllerSecondaryActionUtilities.cabinConfigurationClassesNumberDisableCheck();
		inputManagerControllerSecondaryActionUtilities.checkCabinConfigurationClassesNumber();
		inputManagerControllerSecondaryActionUtilities.setAirfoilDetailsActionAndDisableCheck(equivalentWingAirfoilRootDetailButton, textFieldEquivalentWingAirfoilRootPath, ComponentEnum.WING);
		inputManagerControllerSecondaryActionUtilities.setAirfoilDetailsActionAndDisableCheck(equivalentWingAirfoilKinkDetailButton, textFieldEquivalentWingAirfoilKinkPath, ComponentEnum.WING);
		inputManagerControllerSecondaryActionUtilities.setAirfoilDetailsActionAndDisableCheck(equivalentWingAirfoilTipDetailButton, textFieldEquivalentWingAirfoilTipPath, ComponentEnum.WING);
		inputManagerControllerSecondaryActionUtilities.setAirfoilDetailsActionAndDisableCheck(wingInnerSectionAirfoilDetailsPanel1Button, textFieldWingAirfoilPathInnerSectionPanel1, ComponentEnum.WING);
		inputManagerControllerSecondaryActionUtilities.setAirfoilDetailsActionAndDisableCheck(wingOuterSectionAirfoilDetailsPanel1Button, textFieldWingAirfoilPathOuterSectionPanel1, ComponentEnum.WING);
		inputManagerControllerSecondaryActionUtilities.setAirfoilDetailsActionAndDisableCheck(hTailInnerSectionAirfoilDetailsPanel1Button, textFieldHTailAirfoilPathInnerSectionPanel1, ComponentEnum.HORIZONTAL_TAIL);
		inputManagerControllerSecondaryActionUtilities.setAirfoilDetailsActionAndDisableCheck(hTailOuterSectionAirfoilDetailsPanel1Button, textFieldHTailAirfoilPathOuterSectionPanel1, ComponentEnum.HORIZONTAL_TAIL);
		inputManagerControllerSecondaryActionUtilities.setAirfoilDetailsActionAndDisableCheck(vTailInnerSectionAirfoilDetailsPanel1Button, textFieldVTailAirfoilPathInnerSectionPanel1, ComponentEnum.VERTICAL_TAIL);
		inputManagerControllerSecondaryActionUtilities.setAirfoilDetailsActionAndDisableCheck(vTailOuterSectionAirfoilDetailsPanel1Button, textFieldVTailAirfoilPathOuterSectionPanel1, ComponentEnum.VERTICAL_TAIL);
		inputManagerControllerSecondaryActionUtilities.setAirfoilDetailsActionAndDisableCheck(canardInnerSectionAirfoilDetailsPanel1Button, textFieldCanardAirfoilPathInnerSectionPanel1, ComponentEnum.CANARD);
		inputManagerControllerSecondaryActionUtilities.setAirfoilDetailsActionAndDisableCheck(canardOuterSectionAirfoilDetailsPanel1Button, textFieldCanardAirfoilPathOuterSectionPanel1, ComponentEnum.CANARD);
		inputManagerControllerSecondaryActionUtilities.setChooseAirfoilFileAction(equivalentWingChooseAirfoilRootButton, textFieldEquivalentWingAirfoilRootPath);
		inputManagerControllerSecondaryActionUtilities.setChooseAirfoilFileAction(equivalentWingChooseAirfoilKinkButton, textFieldEquivalentWingAirfoilKinkPath);
		inputManagerControllerSecondaryActionUtilities.setChooseAirfoilFileAction(equivalentWingChooseAirfoilTipButton, textFieldEquivalentWingAirfoilTipPath);
		inputManagerControllerSecondaryActionUtilities.setChooseAirfoilFileAction(wingChooseInnerAirfoilPanel1Button, textFieldWingAirfoilPathInnerSectionPanel1);
		inputManagerControllerSecondaryActionUtilities.setChooseAirfoilFileAction(wingChooseOuterAirfoilPanel1Button, textFieldWingAirfoilPathOuterSectionPanel1);
		inputManagerControllerSecondaryActionUtilities.setChooseAirfoilFileAction(hTailChooseInnerAirfoilPanel1Button, textFieldHTailAirfoilPathInnerSectionPanel1);
		inputManagerControllerSecondaryActionUtilities.setChooseAirfoilFileAction(hTailChooseOuterAirfoilPanel1Button, textFieldHTailAirfoilPathOuterSectionPanel1);
		inputManagerControllerSecondaryActionUtilities.setChooseAirfoilFileAction(vTailChooseInnerAirfoilPanel1Button, textFieldVTailAirfoilPathInnerSectionPanel1);
		inputManagerControllerSecondaryActionUtilities.setChooseAirfoilFileAction(vTailChooseOuterAirfoilPanel1Button, textFieldVTailAirfoilPathOuterSectionPanel1);
		inputManagerControllerSecondaryActionUtilities.setChooseAirfoilFileAction(canardChooseInnerAirfoilPanel1Button, textFieldCanardAirfoilPathInnerSectionPanel1);
		inputManagerControllerSecondaryActionUtilities.setChooseAirfoilFileAction(canardChooseOuterAirfoilPanel1Button, textFieldCanardAirfoilPathOuterSectionPanel1);
		inputManagerControllerSecondaryActionUtilities.equivalentWingDisableCheck();
		inputManagerControllerSecondaryActionUtilities.linkedToDisableCheck(ComponentEnum.WING);
		inputManagerControllerSecondaryActionUtilities.linkedToDisableCheck(ComponentEnum.HORIZONTAL_TAIL);
		inputManagerControllerSecondaryActionUtilities.linkedToDisableCheck(ComponentEnum.VERTICAL_TAIL);
		inputManagerControllerSecondaryActionUtilities.linkedToDisableCheck(ComponentEnum.CANARD);
		inputManagerControllerSecondaryActionUtilities.setEstimateNacelleGeometryAction(nacelleEstimateGeometryButton1, tabPaneNacelles.getTabs().get(0));
		inputManagerControllerSecondaryActionUtilities.setShowEngineDataAction(powerPlantJetRadioButton1, 0, EngineTypeEnum.TURBOFAN);
		inputManagerControllerSecondaryActionUtilities.setShowEngineDataAction(powerPlantTurbopropRadioButton1, 0, EngineTypeEnum.TURBOPROP);
		inputManagerControllerSecondaryActionUtilities.setShowEngineDataAction(powerPlantPistonRadioButton1, 0, EngineTypeEnum.PISTON);
		
	}
	
	@FXML
	private void addAircraftEngine() throws IOException {
		
		inputManagerControllerSecondaryActionUtilities.addAircraftEngineImplementation();
	
		//..................................................................................
		// NEW ENGINE WARNING
		Stage newEngineWaring = new Stage();
		
		newEngineWaring.setTitle("New Engine Warning");
		newEngineWaring.initModality(Modality.WINDOW_MODAL);
		newEngineWaring.initStyle(StageStyle.UNDECORATED);
		newEngineWaring.initOwner(Main.getPrimaryStage());

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("inputmanager/AddAircraftEngineWarning.fxml"));
		BorderPane newEngineWarningBorderPane = loader.load();
		
		Button continueButton = (Button) newEngineWarningBorderPane.lookup("#warningContinueButton");
		continueButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent arg0) {
				newEngineWaring.close();
				inputManagerControllerSecondaryActionUtilities.addEngineImplementation();
				inputManagerControllerSecondaryActionUtilities.addNacelleImplementation();
				inputManagerControllerSecondaryActionUtilities.addAircraftNacelleImplementation();
			}
			
		});
		
		Scene scene = new Scene(newEngineWarningBorderPane);
		newEngineWaring.setScene(scene);
		newEngineWaring.sizeToScene();
		newEngineWaring.show();
		
	}
	

	
	@FXML
	private void addAircraftNacelle() throws IOException {
		
		inputManagerControllerSecondaryActionUtilities.addAircraftNacelleImplementation();
		
		//..................................................................................
		// NEW NACELLE WARNING
		Stage newNacelleWaring = new Stage();
		
		newNacelleWaring.setTitle("New Nacelle Warning");
		newNacelleWaring.initModality(Modality.WINDOW_MODAL);
		newNacelleWaring.initStyle(StageStyle.UNDECORATED);
		newNacelleWaring.initOwner(Main.getPrimaryStage());

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("inputmanager/AddAircraftNacelleWarning.fxml"));
		BorderPane newNacelleWarningBorderPane = loader.load();
		
		Button continueButton = (Button) newNacelleWarningBorderPane.lookup("#warningContinueButton");
		continueButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent arg0) {
				newNacelleWaring.close();
				inputManagerControllerSecondaryActionUtilities.addEngineImplementation();
				inputManagerControllerSecondaryActionUtilities.addNacelleImplementation();
				inputManagerControllerSecondaryActionUtilities.addAircraftEngineImplementation();
			}
			
		});
		
		Scene scene = new Scene(newNacelleWarningBorderPane);
		newNacelleWaring.setScene(scene);
		newNacelleWaring.sizeToScene();
		newNacelleWaring.show();
		
	}
	
	@FXML
	public void addFuselageSpoiler() {
		
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
	public void addWingPanel() {
		
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
		
	}
	
	@FXML
	public void addFlap() {
		
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
		
	}
	
	@FXML
	public void addSlat() {
		
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
		
	}
	
	@FXML
	public void addSpoiler() {
		
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
		
	}
	
	@FXML
	public void addHTailPanel() {
		
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
		
	}
	
	@FXML
	public void addElevator() {
		
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
		
	}
	
	@FXML
	public void addVTailPanel() {
		
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
	
	}
	
	@FXML
	public void addRudder() {
		
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
		
	}
	
	@FXML
	public void addCanardPanel() {
		
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
		
	}
	
	@FXML
	public void addControlSurface() {
		
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
		
	}
	
	@FXML
	private void addNacelle() throws IOException {
		
		inputManagerControllerSecondaryActionUtilities.addNacelleImplementation();
		
		//..................................................................................
		// NEW NACELLE WARNING
		Stage newNacelleWaring = new Stage();
		
		newNacelleWaring.setTitle("New Nacelle Warning");
		newNacelleWaring.initModality(Modality.WINDOW_MODAL);
		newNacelleWaring.initStyle(StageStyle.UNDECORATED);
		newNacelleWaring.initOwner(Main.getPrimaryStage());

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("inputmanager/AddNacelleWarning.fxml"));
		BorderPane newNacelleWarningBorderPane = loader.load();
		
		Button continueButton = (Button) newNacelleWarningBorderPane.lookup("#warningContinueButton");
		continueButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent arg0) {
				newNacelleWaring.close();
				inputManagerControllerSecondaryActionUtilities.addEngineImplementation();
				inputManagerControllerSecondaryActionUtilities.addAircraftNacelleImplementation();
				inputManagerControllerSecondaryActionUtilities.addAircraftEngineImplementation();
			}
			
		});
		
		Scene scene = new Scene(newNacelleWarningBorderPane);
		newNacelleWaring.setScene(scene);
		newNacelleWaring.sizeToScene();
		newNacelleWaring.show();
		
		
		
	}
	

	
	@FXML
	private void addEngine() throws IOException {
		
		inputManagerControllerSecondaryActionUtilities.addEngineImplementation();
		
		//..................................................................................
		// NEW ENGINE WARNING
		Stage newEngineWaring = new Stage();
		
		newEngineWaring.setTitle("New Engine Warning");
		newEngineWaring.initModality(Modality.WINDOW_MODAL);
		newEngineWaring.initStyle(StageStyle.UNDECORATED);
		newEngineWaring.initOwner(Main.getPrimaryStage());

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("inputmanager/AddEngineWarning.fxml"));
		BorderPane newEngineWarningBorderPane = loader.load();
		
		Button continueButton = (Button) newEngineWarningBorderPane.lookup("#warningContinueButton");
		continueButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent arg0) {
				newEngineWaring.close();
				inputManagerControllerSecondaryActionUtilities.addNacelleImplementation();
				inputManagerControllerSecondaryActionUtilities.addAircraftNacelleImplementation();
				inputManagerControllerSecondaryActionUtilities.addAircraftEngineImplementation();
			}
			
		});
		
		Scene scene = new Scene(newEngineWarningBorderPane);
		newEngineWaring.setScene(scene);
		newEngineWaring.sizeToScene();
		newEngineWaring.show();
		
		
		
	}
	

	
	@FXML
	public void showTurbojetTurboFanDataRadioButton (int indexOfEngineTab) {
		
		if (powerPlantEngineTypePaneMap.get(indexOfEngineTab) == null)
			inputManagerControllerSecondaryActionUtilities.showTurbojetTurboFanDataRadioButtonImplementation(indexOfEngineTab);
		else if (powerPlantEngineTypePaneMap.get(indexOfEngineTab).get(EngineTypeEnum.TURBOFAN) == null) 
			inputManagerControllerSecondaryActionUtilities.showTurbojetTurboFanDataRadioButtonImplementation(indexOfEngineTab);
		else 
			powerPlantBorderPaneMap.get(indexOfEngineTab).setCenter(
					powerPlantEngineTypePaneMap.get(indexOfEngineTab).get(EngineTypeEnum.TURBOFAN)
					);
		
	}

	@FXML
	public void showTurbopropDataRadioButton (int indexOfEngineTab) {
		
		if (powerPlantEngineTypePaneMap.get(indexOfEngineTab) == null)
			inputManagerControllerSecondaryActionUtilities.showTurbopropDataRadioButtonImplementation(indexOfEngineTab);
		else if (powerPlantEngineTypePaneMap.get(indexOfEngineTab).get(EngineTypeEnum.TURBOPROP) == null) 
			inputManagerControllerSecondaryActionUtilities.showTurbopropDataRadioButtonImplementation(indexOfEngineTab);
		else 
			powerPlantBorderPaneMap.get(indexOfEngineTab).setCenter(
					powerPlantEngineTypePaneMap.get(indexOfEngineTab).get(EngineTypeEnum.TURBOPROP)
					);
		
	}
	

	
	@FXML
	public void showPistonDataRadioButton (int indexOfEngineTab) {
		
		if (powerPlantEngineTypePaneMap.get(indexOfEngineTab) == null)
			inputManagerControllerSecondaryActionUtilities.showPistonDataRadioButtonImplementation(indexOfEngineTab);
		else if (powerPlantEngineTypePaneMap.get(indexOfEngineTab).get(EngineTypeEnum.PISTON) == null) 
			inputManagerControllerSecondaryActionUtilities.showPistonDataRadioButtonImplementation(indexOfEngineTab);
		else 
			powerPlantBorderPaneMap.get(indexOfEngineTab).setCenter(
					powerPlantEngineTypePaneMap.get(indexOfEngineTab).get(EngineTypeEnum.PISTON)
					);
		
	}
	
	@FXML
	public void calculateEngineDryMass (TextField dryMassTextField, ChoiceBox<String> dryMassChoiceBox, int indexOfEngineTab) {

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
	}
	
	@FXML
	public void showAirfoilData(String airfoilFileName, Button detailButton, ComponentEnum type) throws IOException {
		
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
					Main.getTheAircraft().getWing().getAirfoilList().get(airfoilWingIndex)
					);
			airfoilInputManagerController.createAirfoilView();
			airfoilInputManagerController.createClCurve();
			airfoilInputManagerController.createCdCurve();
			airfoilInputManagerController.createCmCurve();

			airfoilTab.setContent(contentPane);
			inputManagerControllerSecondaryActionUtilities.removeAirfoilDetailsButtonFromMapOnTabClose(airfoilTab, type);

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
					Main.getTheAircraft().getHTail().getAirfoilList().get(airfoilHTailIndex)
					);
			airfoilInputManagerController.createAirfoilView();
			airfoilInputManagerController.createClCurve();
			airfoilInputManagerController.createCdCurve();
			airfoilInputManagerController.createCmCurve();

			airfoilTab.setContent(contentPane);
			inputManagerControllerSecondaryActionUtilities.removeAirfoilDetailsButtonFromMapOnTabClose(airfoilTab, type);

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
					Main.getTheAircraft().getVTail().getAirfoilList().get(airfoilVTailIndex)
					);
			airfoilInputManagerController.createAirfoilView();
			airfoilInputManagerController.createClCurve();
			airfoilInputManagerController.createCdCurve();
			airfoilInputManagerController.createCmCurve();

			airfoilTab.setContent(contentPane);
			inputManagerControllerSecondaryActionUtilities.removeAirfoilDetailsButtonFromMapOnTabClose(airfoilTab, type);

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
					Main.getTheAircraft().getCanard().getAirfoilList().get(airfoilCanardIndex)
					);
			airfoilInputManagerController.createAirfoilView();
			airfoilInputManagerController.createClCurve();
			airfoilInputManagerController.createCdCurve();
			airfoilInputManagerController.createCmCurve();

			airfoilTab.setContent(contentPane);
			inputManagerControllerSecondaryActionUtilities.removeAirfoilDetailsButtonFromMapOnTabClose(airfoilTab, type);

			tabPaneCanardViewAndAirfoils.getTabs().add(airfoilTab);
			break;
			
		default:
			break;
		}
		
		
		
	}
	
	@FXML
	public void chooseAirfoilFile(TextField panelInnerSectionAirfoilPathTextField) {
		
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
		
		
		
	}
	
	@FXML
	public void chooseEngineDatabase(TextField engineDatabasePathTextField) {
		
		engineDatabaseFileChooser = new FileChooser();
		engineDatabaseFileChooser.setTitle("Open File");
		engineDatabaseFileChooser.setInitialDirectory(
				new File(Main.getDatabaseDirectoryPath())
				);
		File file = engineDatabaseFileChooser.showOpenDialog(null);
		if (file != null) {
			engineDatabasePathTextField.setText(Paths.get(file.getAbsolutePath()).getFileName().toString());
		}
		
		
		
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
			chooseAircraftFileButton.setStyle("");
			loadAircraftButton.setStyle(buttonSuggestedActionStyle);
		}
		
		
		
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
				inputManagerControllerMainActionUtilities.newAircraftImplementation();
				inputDataWarning.close();
				updateAircraftDataButton.setStyle("");
				saveAircraftButton.setStyle("");
				chooseAircraftFileButton.setStyle(buttonSuggestedActionStyle);
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
	
	@FXML
	private void loadAircraftFile() throws IOException, InterruptedException {
	
		if(inputManagerControllerSecondaryActionUtilities.isAircraftFile(textFieldAircraftInputFile.getText()))
			try {
				
				inputManagerControllerMainActionUtilities.loadAircraftFileImplementation();
				loadAircraftButton.setStyle("");
				saveAircraftButton.setStyle(buttonSuggestedActionStyle);
				updateAircraftDataButton.setStyle(buttonSuggestedActionStyle);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		
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
				adjustCriterionAircraftWarningBorderPane.setPrefSize(600, 400);
				
				VBox actionVBox = (VBox) adjustCriterionAircraftWarningBorderPane.lookup("#actionVBox");
				
				Button continueButton = (Button) adjustCriterionAircraftWarningBorderPane.lookup("#warningContinueButton");
				continueButton.setOnAction(new EventHandler<ActionEvent>() {
					
					@Override
					public void handle(ActionEvent arg0) {
						adjustCriterionAircraftWarning.close();
						return;
					}
					
				});
				
				ObservableList<String> componentsList = FXCollections.observableArrayList(
						"Fuselage", 
						"Wing",
						"Horizontal Tail",
						"Vertical Tail",
						"Canard"
						);
				
				CheckComboBox<String> chooseComponentsNoneAdjustCriterionComboBox = new CheckComboBox<String>(componentsList);
				actionVBox.getChildren().add(chooseComponentsNoneAdjustCriterionComboBox);
				chooseComponentsNoneAdjustCriterionComboBox.setPadding(new Insets(0.0, 0.0, 0.0, 100.0));
				chooseComponentsNoneAdjustCriterionComboBox.setPrefSize(520, 31);
				
				chooseComponentsNoneAdjustCriterionComboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
				     public void onChanged(ListChangeListener.Change<? extends String> c) {
				    	 
				    	if (chooseComponentsNoneAdjustCriterionComboBox.getCheckModel().getCheckedItems().contains("Fuselage"))
				    		noneAdjustCriterionFuselage = true;
				    	if (chooseComponentsNoneAdjustCriterionComboBox.getCheckModel().getCheckedItems().contains("Wing"))
				    		noneAdjustCriterionWing = true;
				    	if (chooseComponentsNoneAdjustCriterionComboBox.getCheckModel().getCheckedItems().contains("Horizontal Tail"))
				    		noneAdjustCriterionHTail = true;
				    	if (chooseComponentsNoneAdjustCriterionComboBox.getCheckModel().getCheckedItems().contains("Vertical Tail"))
				    		noneAdjustCriterionVTail = true;
				    	if (chooseComponentsNoneAdjustCriterionComboBox.getCheckModel().getCheckedItems().contains("Canard"))
				    		noneAdjustCriterionCanard = true;
				     }
				 });
				
				Button continueAndSetToNoneButton = (Button) adjustCriterionAircraftWarningBorderPane.lookup("#warningContinueAndNoneButton");
				continueAndSetToNoneButton.setOnAction(new EventHandler<ActionEvent>() {
					
					@Override
					public void handle(ActionEvent arg0) {
						adjustCriterionAircraftWarning.close();
						if(Main.getTheAircraft().getFuselage() != null)
							if (noneAdjustCriterionFuselage)
								fuselageAdjustCriterionChoiceBox.getSelectionModel().select(0);
						if(Main.getTheAircraft().getWing() != null)
							if (noneAdjustCriterionWing)
								wingAdjustCriterionChoiceBox.getSelectionModel().select(0);
						if(Main.getTheAircraft().getHTail() != null)
							if (noneAdjustCriterionHTail)
								hTailAdjustCriterionChoiceBox.getSelectionModel().select(0);
						if(Main.getTheAircraft().getVTail() != null)
							if (noneAdjustCriterionVTail)
								vTailAdjustCriterionChoiceBox.getSelectionModel().select(0);
						if(Main.getTheAircraft().getCanard() != null)
							if (noneAdjustCriterionCanard)
								canardAdjustCriterionChoiceBox.getSelectionModel().select(0);
						
						boolean proceed = true;
						
						if ( (Main.getTheAircraft().getFuselage() != null && fuselageAdjustCriterionChoiceBox.getSelectionModel().isEmpty())
								|| (Main.getTheAircraft().getWing() != null && wingAdjustCriterionChoiceBox.getSelectionModel().isEmpty())
								|| (Main.getTheAircraft().getHTail() != null && hTailAdjustCriterionChoiceBox.getSelectionModel().isEmpty())
								|| (Main.getTheAircraft().getVTail() != null && vTailAdjustCriterionChoiceBox.getSelectionModel().isEmpty())
								|| (Main.getTheAircraft().getCanard() != null && canardAdjustCriterionChoiceBox.getSelectionModel().isEmpty())
								)
							proceed = false;
						
						if (proceed == true)
							inputManagerControllerMainActionUtilities.updateAircraftDataImplementation();
						else {
							Main.getStatusBar().textProperty().unbind();
							Main.getStatusBar().setText("WARINING: ADJUST CRITERIA. TERMINATING ...");
						}
					}
					
				});
				
				Scene scene = new Scene(adjustCriterionAircraftWarningBorderPane);
				adjustCriterionAircraftWarning.setScene(scene);
				adjustCriterionAircraftWarning.sizeToScene();
				adjustCriterionAircraftWarning.show();
				
			}
			else 
				inputManagerControllerMainActionUtilities.updateAircraftDataImplementation();
			
			fuselageAdjustCriterionChoiceBox.getSelectionModel().clearAndSelect(0);
			wingAdjustCriterionChoiceBox.getSelectionModel().clearAndSelect(0);
			hTailAdjustCriterionChoiceBox.getSelectionModel().clearAndSelect(0);
			vTailAdjustCriterionChoiceBox.getSelectionModel().clearAndSelect(0);
			canardAdjustCriterionChoiceBox.getSelectionModel().clearAndSelect(0);
			
		}
	}
	
	@FXML
	private void saveAircraftToFile() throws IOException {
		
		if (Main.getTheAircraft() != null) {
			
			if (Main.getAircraftUpdated()) {
				
				inputManagerControllerMainActionUtilities.saveAircraftToFileImplementation();
				
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
							inputManagerControllerMainActionUtilities.saveAircraftToFileImplementation();
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
	}
	
	//...........................................................................................
	// INFO ACTIONS:
	//...........................................................................................
	
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
	public void showNacelleKInletInfo() {
		
		// TODO
		
	};
	
	@FXML
	public void showNacelleKOutletInfo() {
		
		// TODO
		
	};
	
	@FXML
	public void showNacelleKLengthInfo() {
		
		// TODO
		
	};
	
	@FXML
	public void showNacelleKDiameterOutletInfo() {
		
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
	}

	//..........................................................................................
	// GETTERS AND SETTERS
	//..........................................................................................
	
	public ToolBar getActionButtonToolbar() {
		return actionButtonToolbar;
	}

	public void setActionButtonToolbar(ToolBar actionButtonToolbar) {
		this.actionButtonToolbar = actionButtonToolbar;
	}

	public SplitPane getAircraftViewsAndDataLogSplitPane() {
		return aircraftViewsAndDataLogSplitPane;
	}

	public void setAircraftViewsAndDataLogSplitPane(SplitPane aircraftViewsAndDataLogSplitPane) {
		this.aircraftViewsAndDataLogSplitPane = aircraftViewsAndDataLogSplitPane;
	}

	public SplitPane getFuselageViewsAndDataLogSplitPane() {
		return fuselageViewsAndDataLogSplitPane;
	}

	public void setFuselageViewsAndDataLogSplitPane(SplitPane fuselageViewsAndDataLogSplitPane) {
		this.fuselageViewsAndDataLogSplitPane = fuselageViewsAndDataLogSplitPane;
	}

	public SplitPane getCabinConfigurationViewsAndDataLogSplitPane() {
		return cabinConfigurationViewsAndDataLogSplitPane;
	}

	public void setCabinConfigurationViewsAndDataLogSplitPane(SplitPane cabinConfigurationViewsAndDataLogSplitPane) {
		this.cabinConfigurationViewsAndDataLogSplitPane = cabinConfigurationViewsAndDataLogSplitPane;
	}

	public SplitPane getWingViewsAndDataLogSplitPane() {
		return wingViewsAndDataLogSplitPane;
	}

	public void setWingViewsAndDataLogSplitPane(SplitPane wingViewsAndDataLogSplitPane) {
		this.wingViewsAndDataLogSplitPane = wingViewsAndDataLogSplitPane;
	}

	public SplitPane gethTailViewsAndDataLogSplitPane() {
		return hTailViewsAndDataLogSplitPane;
	}

	public void sethTailViewsAndDataLogSplitPane(SplitPane hTailViewsAndDataLogSplitPane) {
		this.hTailViewsAndDataLogSplitPane = hTailViewsAndDataLogSplitPane;
	}

	public SplitPane getvTailViewsAndDataLogSplitPane() {
		return vTailViewsAndDataLogSplitPane;
	}

	public void setvTailViewsAndDataLogSplitPane(SplitPane vTailViewsAndDataLogSplitPane) {
		this.vTailViewsAndDataLogSplitPane = vTailViewsAndDataLogSplitPane;
	}

	public SplitPane getCanardViewsAndDataLogSplitPane() {
		return canardViewsAndDataLogSplitPane;
	}

	public void setCanardViewsAndDataLogSplitPane(SplitPane canardViewsAndDataLogSplitPane) {
		this.canardViewsAndDataLogSplitPane = canardViewsAndDataLogSplitPane;
	}

	public SplitPane getNacelleViewsAndDataLogSplitPane() {
		return nacelleViewsAndDataLogSplitPane;
	}

	public void setNacelleViewsAndDataLogSplitPane(SplitPane nacelleViewsAndDataLogSplitPane) {
		this.nacelleViewsAndDataLogSplitPane = nacelleViewsAndDataLogSplitPane;
	}

	public Pane getAircraftFrontViewPane() {
		return aircraftFrontViewPane;
	}

	public void setAircraftFrontViewPane(Pane aircraftFrontViewPane) {
		this.aircraftFrontViewPane = aircraftFrontViewPane;
	}

	public Pane getAircraftSideViewPane() {
		return aircraftSideViewPane;
	}

	public void setAircraftSideViewPane(Pane aircraftSideViewPane) {
		this.aircraftSideViewPane = aircraftSideViewPane;
	}

	public Pane getAircraftTopViewPane() {
		return aircraftTopViewPane;
	}

	public void setAircraftTopViewPane(Pane aircraftTopViewPane) {
		this.aircraftTopViewPane = aircraftTopViewPane;
	}

	public Pane getFuselageFrontViewPane() {
		return fuselageFrontViewPane;
	}

	public void setFuselageFrontViewPane(Pane fuselageFrontViewPane) {
		this.fuselageFrontViewPane = fuselageFrontViewPane;
	}

	public Pane getFuselageSideViewPane() {
		return fuselageSideViewPane;
	}

	public void setFuselageSideViewPane(Pane fuselageSideViewPane) {
		this.fuselageSideViewPane = fuselageSideViewPane;
	}

	public Pane getFuselageTopViewPane() {
		return fuselageTopViewPane;
	}

	public void setFuselageTopViewPane(Pane fuselageTopViewPane) {
		this.fuselageTopViewPane = fuselageTopViewPane;
	}

	public Pane getCabinConfigurationSeatMapPane() {
		return cabinConfigurationSeatMapPane;
	}

	public void setCabinConfigurationSeatMapPane(Pane cabinConfigurationSeatMapPane) {
		this.cabinConfigurationSeatMapPane = cabinConfigurationSeatMapPane;
	}

	public Pane getWingPlanformPane() {
		return wingPlanformPane;
	}

	public void setWingPlanformPane(Pane wingPlanformPane) {
		this.wingPlanformPane = wingPlanformPane;
	}

	public Pane getEquivalentWingPane() {
		return equivalentWingPane;
	}

	public void setEquivalentWingPane(Pane equivalentWingPane) {
		this.equivalentWingPane = equivalentWingPane;
	}

	public Pane gethTailPlanformPane() {
		return hTailPlanformPane;
	}

	public void sethTailPlanformPane(Pane hTailPlanformPane) {
		this.hTailPlanformPane = hTailPlanformPane;
	}

	public Pane getvTailPlanformPane() {
		return vTailPlanformPane;
	}

	public void setvTailPlanformPane(Pane vTailPlanformPane) {
		this.vTailPlanformPane = vTailPlanformPane;
	}

	public Pane getCanardPlanformPane() {
		return canardPlanformPane;
	}

	public void setCanardPlanformPane(Pane canardPlanformPane) {
		this.canardPlanformPane = canardPlanformPane;
	}

	public Pane getNacelle1FrontViewPane() {
		return nacelle1FrontViewPane;
	}

	public void setNacelle1FrontViewPane(Pane nacelle1FrontViewPane) {
		this.nacelle1FrontViewPane = nacelle1FrontViewPane;
	}

	public Pane getNacelle1SideViewPane() {
		return nacelle1SideViewPane;
	}

	public void setNacelle1SideViewPane(Pane nacelle1SideViewPane) {
		this.nacelle1SideViewPane = nacelle1SideViewPane;
	}

	public Pane getNacelle1TopViewPane() {
		return nacelle1TopViewPane;
	}

	public void setNacelle1TopViewPane(Pane nacelle1TopViewPane) {
		this.nacelle1TopViewPane = nacelle1TopViewPane;
	}

	public BorderPane getEngine1BorderPane() {
		return engine1BorderPane;
	}

	public void setEngine1BorderPane(BorderPane engine1BorderPane) {
		this.engine1BorderPane = engine1BorderPane;
	}

	public TextArea getTextAreaAircraftConsoleOutput() {
		return textAreaAircraftConsoleOutput;
	}

	public void setTextAreaAircraftConsoleOutput(TextArea textAreaAircraftConsoleOutput) {
		this.textAreaAircraftConsoleOutput = textAreaAircraftConsoleOutput;
	}

	public TextArea getTextAreaFuselageConsoleOutput() {
		return textAreaFuselageConsoleOutput;
	}

	public void setTextAreaFuselageConsoleOutput(TextArea textAreaFuselageConsoleOutput) {
		this.textAreaFuselageConsoleOutput = textAreaFuselageConsoleOutput;
	}

	public TextArea getTextAreaCabinConfigurationConsoleOutput() {
		return textAreaCabinConfigurationConsoleOutput;
	}

	public void setTextAreaCabinConfigurationConsoleOutput(TextArea textAreaCabinConfigurationConsoleOutput) {
		this.textAreaCabinConfigurationConsoleOutput = textAreaCabinConfigurationConsoleOutput;
	}

	public TextArea getTextAreaWingConsoleOutput() {
		return textAreaWingConsoleOutput;
	}

	public void setTextAreaWingConsoleOutput(TextArea textAreaWingConsoleOutput) {
		this.textAreaWingConsoleOutput = textAreaWingConsoleOutput;
	}

	public TextArea getTextAreaHTailConsoleOutput() {
		return textAreaHTailConsoleOutput;
	}

	public void setTextAreaHTailConsoleOutput(TextArea textAreaHTailConsoleOutput) {
		this.textAreaHTailConsoleOutput = textAreaHTailConsoleOutput;
	}

	public TextArea getTextAreaVTailConsoleOutput() {
		return textAreaVTailConsoleOutput;
	}

	public void setTextAreaVTailConsoleOutput(TextArea textAreaVTailConsoleOutput) {
		this.textAreaVTailConsoleOutput = textAreaVTailConsoleOutput;
	}

	public TextArea getTextAreaCanardConsoleOutput() {
		return textAreaCanardConsoleOutput;
	}

	public void setTextAreaCanardConsoleOutput(TextArea textAreaCanardConsoleOutput) {
		this.textAreaCanardConsoleOutput = textAreaCanardConsoleOutput;
	}

	public TextArea getTextAreaNacelleConsoleOutput() {
		return textAreaNacelleConsoleOutput;
	}

	public void setTextAreaNacelleConsoleOutput(TextArea textAreaNacelleConsoleOutput) {
		this.textAreaNacelleConsoleOutput = textAreaNacelleConsoleOutput;
	}

	public TextArea getTextAreaPowerPlantConsoleOutput() {
		return textAreaPowerPlantConsoleOutput;
	}

	public void setTextAreaPowerPlantConsoleOutput(TextArea textAreaPowerPlantConsoleOutput) {
		this.textAreaPowerPlantConsoleOutput = textAreaPowerPlantConsoleOutput;
	}

	public TextArea getTextAreaLandingGearsConsoleOutput() {
		return textAreaLandingGearsConsoleOutput;
	}

	public void setTextAreaLandingGearsConsoleOutput(TextArea textAreaLandingGearsConsoleOutput) {
		this.textAreaLandingGearsConsoleOutput = textAreaLandingGearsConsoleOutput;
	}

	public TabPane getTabPaneAircraftEngines() {
		return tabPaneAircraftEngines;
	}

	public void setTabPaneAircraftEngines(TabPane tabPaneAircraftEngines) {
		this.tabPaneAircraftEngines = tabPaneAircraftEngines;
	}

	public TabPane getTabPaneAircraftNacelles() {
		return tabPaneAircraftNacelles;
	}

	public void setTabPaneAircraftNacelles(TabPane tabPaneAircraftNacelles) {
		this.tabPaneAircraftNacelles = tabPaneAircraftNacelles;
	}

	public TabPane getTabPaneFuselageSpoilers() {
		return tabPaneFuselageSpoilers;
	}

	public void setTabPaneFuselageSpoilers(TabPane tabPaneFuselageSpoilers) {
		this.tabPaneFuselageSpoilers = tabPaneFuselageSpoilers;
	}

	public TabPane getTabPaneWingPanels() {
		return tabPaneWingPanels;
	}

	public void setTabPaneWingPanels(TabPane tabPaneWingPanels) {
		this.tabPaneWingPanels = tabPaneWingPanels;
	}

	public TabPane getTabPaneWingFlaps() {
		return tabPaneWingFlaps;
	}

	public void setTabPaneWingFlaps(TabPane tabPaneWingFlaps) {
		this.tabPaneWingFlaps = tabPaneWingFlaps;
	}

	public TabPane getTabPaneWingSlats() {
		return tabPaneWingSlats;
	}

	public void setTabPaneWingSlats(TabPane tabPaneWingSlats) {
		this.tabPaneWingSlats = tabPaneWingSlats;
	}

	public TabPane getTabPaneWingSpoilers() {
		return tabPaneWingSpoilers;
	}

	public void setTabPaneWingSpoilers(TabPane tabPaneWingSpoilers) {
		this.tabPaneWingSpoilers = tabPaneWingSpoilers;
	}

	public TabPane getTabPaneWingViewAndAirfoils() {
		return tabPaneWingViewAndAirfoils;
	}

	public void setTabPaneWingViewAndAirfoils(TabPane tabPaneWingViewAndAirfoils) {
		this.tabPaneWingViewAndAirfoils = tabPaneWingViewAndAirfoils;
	}

	public TabPane getTabPaneHTailPanels() {
		return tabPaneHTailPanels;
	}

	public void setTabPaneHTailPanels(TabPane tabPaneHTailPanels) {
		this.tabPaneHTailPanels = tabPaneHTailPanels;
	}

	public TabPane getTabPaneHTailElevators() {
		return tabPaneHTailElevators;
	}

	public void setTabPaneHTailElevators(TabPane tabPaneHTailElevators) {
		this.tabPaneHTailElevators = tabPaneHTailElevators;
	}

	public TabPane getTabPaneHTailViewAndAirfoils() {
		return tabPaneHTailViewAndAirfoils;
	}

	public void setTabPaneHTailViewAndAirfoils(TabPane tabPaneHTailViewAndAirfoils) {
		this.tabPaneHTailViewAndAirfoils = tabPaneHTailViewAndAirfoils;
	}

	public TabPane getTabPaneVTailPanels() {
		return tabPaneVTailPanels;
	}

	public void setTabPaneVTailPanels(TabPane tabPaneVTailPanels) {
		this.tabPaneVTailPanels = tabPaneVTailPanels;
	}

	public TabPane getTabPaneVTailRudders() {
		return tabPaneVTailRudders;
	}

	public void setTabPaneVTailRudders(TabPane tabPaneVTailRudders) {
		this.tabPaneVTailRudders = tabPaneVTailRudders;
	}

	public TabPane getTabPaneVTailViewAndAirfoils() {
		return tabPaneVTailViewAndAirfoils;
	}

	public void setTabPaneVTailViewAndAirfoils(TabPane tabPaneVTailViewAndAirfoils) {
		this.tabPaneVTailViewAndAirfoils = tabPaneVTailViewAndAirfoils;
	}

	public TabPane getTabPaneCanardPanels() {
		return tabPaneCanardPanels;
	}

	public void setTabPaneCanardPanels(TabPane tabPaneCanardPanels) {
		this.tabPaneCanardPanels = tabPaneCanardPanels;
	}

	public TabPane getTabPaneCanardControlSurfaces() {
		return tabPaneCanardControlSurfaces;
	}

	public void setTabPaneCanardControlSurfaces(TabPane tabPaneCanardControlSurfaces) {
		this.tabPaneCanardControlSurfaces = tabPaneCanardControlSurfaces;
	}

	public TabPane getTabPaneCanardViewAndAirfoils() {
		return tabPaneCanardViewAndAirfoils;
	}

	public void setTabPaneCanardViewAndAirfoils(TabPane tabPaneCanardViewAndAirfoils) {
		this.tabPaneCanardViewAndAirfoils = tabPaneCanardViewAndAirfoils;
	}

	public TabPane getTabPaneNacelles() {
		return tabPaneNacelles;
	}

	public void setTabPaneNacelles(TabPane tabPaneNacelles) {
		this.tabPaneNacelles = tabPaneNacelles;
	}

	public TabPane getTabPaneNacellesTopViews() {
		return tabPaneNacellesTopViews;
	}

	public void setTabPaneNacellesTopViews(TabPane tabPaneNacellesTopViews) {
		this.tabPaneNacellesTopViews = tabPaneNacellesTopViews;
	}

	public TabPane getTabPaneNacellesSideViews() {
		return tabPaneNacellesSideViews;
	}

	public void setTabPaneNacellesSideViews(TabPane tabPaneNacellesSideViews) {
		this.tabPaneNacellesSideViews = tabPaneNacellesSideViews;
	}

	public TabPane getTabPaneNacellesFrontViews() {
		return tabPaneNacellesFrontViews;
	}

	public void setTabPaneNacellesFrontViews(TabPane tabPaneNacellesFrontViews) {
		this.tabPaneNacellesFrontViews = tabPaneNacellesFrontViews;
	}

	public TabPane getTabPaneEngines() {
		return tabPaneEngines;
	}

	public void setTabPaneEngines(TabPane tabPaneEngines) {
		this.tabPaneEngines = tabPaneEngines;
	}

	public Button getChooseAircraftFileButton() {
		return chooseAircraftFileButton;
	}

	public void setChooseAircraftFileButton(Button chooseAircraftFileButton) {
		this.chooseAircraftFileButton = chooseAircraftFileButton;
	}

	public Button getLoadAircraftButton() {
		return loadAircraftButton;
	}

	public void setLoadAircraftButton(Button loadAircraftButton) {
		this.loadAircraftButton = loadAircraftButton;
	}

	public Button getNewAircraftButton() {
		return newAircraftButton;
	}

	public void setNewAircraftButton(Button newAircraftButton) {
		this.newAircraftButton = newAircraftButton;
	}

	public Button getUpdateAircraftDataButton() {
		return updateAircraftDataButton;
	}

	public void setUpdateAircraftDataButton(Button updateAircraftDataButton) {
		this.updateAircraftDataButton = updateAircraftDataButton;
	}

	public Button getSaveAircraftButton() {
		return saveAircraftButton;
	}

	public void setSaveAircraftButton(Button saveAircraftButton) {
		this.saveAircraftButton = saveAircraftButton;
	}

	public Button getAircraftChooseCabinConfigurationFileButton() {
		return aircraftChooseCabinConfigurationFileButton;
	}

	public void setAircraftChooseCabinConfigurationFileButton(Button aircraftChooseCabinConfigurationFileButton) {
		this.aircraftChooseCabinConfigurationFileButton = aircraftChooseCabinConfigurationFileButton;
	}

	public Button getAircraftChooseFuselageFileButton() {
		return aircraftChooseFuselageFileButton;
	}

	public void setAircraftChooseFuselageFileButton(Button aircraftChooseFuselageFileButton) {
		this.aircraftChooseFuselageFileButton = aircraftChooseFuselageFileButton;
	}

	public Button getAircraftChooseWingFileButton() {
		return aircraftChooseWingFileButton;
	}

	public void setAircraftChooseWingFileButton(Button aircraftChooseWingFileButton) {
		this.aircraftChooseWingFileButton = aircraftChooseWingFileButton;
	}

	public Button getAircraftChooseHTailFileButton() {
		return aircraftChooseHTailFileButton;
	}

	public void setAircraftChooseHTailFileButton(Button aircraftChooseHTailFileButton) {
		this.aircraftChooseHTailFileButton = aircraftChooseHTailFileButton;
	}

	public Button getAircraftChooseVTailFileButton() {
		return aircraftChooseVTailFileButton;
	}

	public void setAircraftChooseVTailFileButton(Button aircraftChooseVTailFileButton) {
		this.aircraftChooseVTailFileButton = aircraftChooseVTailFileButton;
	}

	public Button getAircraftChooseCanardFileButton() {
		return aircraftChooseCanardFileButton;
	}

	public void setAircraftChooseCanardFileButton(Button aircraftChooseCanardFileButton) {
		this.aircraftChooseCanardFileButton = aircraftChooseCanardFileButton;
	}

	public Button getAircraftChooseEngine1FileButton() {
		return aircraftChooseEngine1FileButton;
	}

	public void setAircraftChooseEngine1FileButton(Button aircraftChooseEngine1FileButton) {
		this.aircraftChooseEngine1FileButton = aircraftChooseEngine1FileButton;
	}

	public Button getAircraftChooseNacelle1FileButton() {
		return aircraftChooseNacelle1FileButton;
	}

	public void setAircraftChooseNacelle1FileButton(Button aircraftChooseNacelle1FileButton) {
		this.aircraftChooseNacelle1FileButton = aircraftChooseNacelle1FileButton;
	}

	public Button getAircraftChooseLandingGearsFileButton() {
		return aircraftChooseLandingGearsFileButton;
	}

	public void setAircraftChooseLandingGearsFileButton(Button aircraftChooseLandingGearsFileButton) {
		this.aircraftChooseLandingGearsFileButton = aircraftChooseLandingGearsFileButton;
	}

	public Button getAircraftAddEngineButton() {
		return aircraftAddEngineButton;
	}

	public void setAircraftAddEngineButton(Button aircraftAddEngineButton) {
		this.aircraftAddEngineButton = aircraftAddEngineButton;
	}

	public Button getAircraftAddNacelleButton() {
		return aircraftAddNacelleButton;
	}

	public void setAircraftAddNacelleButton(Button aircraftAddNacelleButton) {
		this.aircraftAddNacelleButton = aircraftAddNacelleButton;
	}

	public Button getFuselageAddSpoilerButton() {
		return fuselageAddSpoilerButton;
	}

	public void setFuselageAddSpoilerButton(Button fuselageAddSpoilerButton) {
		this.fuselageAddSpoilerButton = fuselageAddSpoilerButton;
	}

	public Button getEquivalentWingInfoButton() {
		return equivalentWingInfoButton;
	}

	public void setEquivalentWingInfoButton(Button equivalentWingInfoButton) {
		this.equivalentWingInfoButton = equivalentWingInfoButton;
	}

	public Button getEquivalentWingRootXOffsetLEInfoButton() {
		return equivalentWingRootXOffsetLEInfoButton;
	}

	public void setEquivalentWingRootXOffsetLEInfoButton(Button equivalentWingRootXOffsetLEInfoButton) {
		this.equivalentWingRootXOffsetLEInfoButton = equivalentWingRootXOffsetLEInfoButton;
	}

	public Button getEquivalentWingRootXOffseTLEInfoButton() {
		return equivalentWingRootXOffseTLEInfoButton;
	}

	public void setEquivalentWingRootXOffseTLEInfoButton(Button equivalentWingRootXOffseTLEInfoButton) {
		this.equivalentWingRootXOffseTLEInfoButton = equivalentWingRootXOffseTLEInfoButton;
	}

	public Button getEquivalentWingAirfoilRootDetailButton() {
		return equivalentWingAirfoilRootDetailButton;
	}

	public void setEquivalentWingAirfoilRootDetailButton(Button equivalentWingAirfoilRootDetailButton) {
		this.equivalentWingAirfoilRootDetailButton = equivalentWingAirfoilRootDetailButton;
	}

	public Button getEquivalentWingChooseAirfoilRootButton() {
		return equivalentWingChooseAirfoilRootButton;
	}

	public void setEquivalentWingChooseAirfoilRootButton(Button equivalentWingChooseAirfoilRootButton) {
		this.equivalentWingChooseAirfoilRootButton = equivalentWingChooseAirfoilRootButton;
	}

	public Button getEquivalentWingAirfoilKinkDetailButton() {
		return equivalentWingAirfoilKinkDetailButton;
	}

	public void setEquivalentWingAirfoilKinkDetailButton(Button equivalentWingAirfoilKinkDetailButton) {
		this.equivalentWingAirfoilKinkDetailButton = equivalentWingAirfoilKinkDetailButton;
	}

	public Button getEquivalentWingChooseAirfoilKinkButton() {
		return equivalentWingChooseAirfoilKinkButton;
	}

	public void setEquivalentWingChooseAirfoilKinkButton(Button equivalentWingChooseAirfoilKinkButton) {
		this.equivalentWingChooseAirfoilKinkButton = equivalentWingChooseAirfoilKinkButton;
	}

	public Button getEquivalentWingAirfoilTipDetailButton() {
		return equivalentWingAirfoilTipDetailButton;
	}

	public void setEquivalentWingAirfoilTipDetailButton(Button equivalentWingAirfoilTipDetailButton) {
		this.equivalentWingAirfoilTipDetailButton = equivalentWingAirfoilTipDetailButton;
	}

	public Button getEquivalentWingChooseAirfoilTipButton() {
		return equivalentWingChooseAirfoilTipButton;
	}

	public void setEquivalentWingChooseAirfoilTipButton(Button equivalentWingChooseAirfoilTipButton) {
		this.equivalentWingChooseAirfoilTipButton = equivalentWingChooseAirfoilTipButton;
	}

	public Button getWingAddPanelButton() {
		return wingAddPanelButton;
	}

	public void setWingAddPanelButton(Button wingAddPanelButton) {
		this.wingAddPanelButton = wingAddPanelButton;
	}

	public Button getWingInnerSectionAirfoilDetailsPanel1Button() {
		return wingInnerSectionAirfoilDetailsPanel1Button;
	}

	public void setWingInnerSectionAirfoilDetailsPanel1Button(Button wingInnerSectionAirfoilDetailsPanel1Button) {
		this.wingInnerSectionAirfoilDetailsPanel1Button = wingInnerSectionAirfoilDetailsPanel1Button;
	}

	public Button getWingOuterSectionAirfoilDetailsPanel1Button() {
		return wingOuterSectionAirfoilDetailsPanel1Button;
	}

	public void setWingOuterSectionAirfoilDetailsPanel1Button(Button wingOuterSectionAirfoilDetailsPanel1Button) {
		this.wingOuterSectionAirfoilDetailsPanel1Button = wingOuterSectionAirfoilDetailsPanel1Button;
	}

	public Button getWingAddFlapButton() {
		return wingAddFlapButton;
	}

	public void setWingAddFlapButton(Button wingAddFlapButton) {
		this.wingAddFlapButton = wingAddFlapButton;
	}

	public Button getWingAddSlatButton() {
		return wingAddSlatButton;
	}

	public void setWingAddSlatButton(Button wingAddSlatButton) {
		this.wingAddSlatButton = wingAddSlatButton;
	}

	public Button getWingAddSpoilerButton() {
		return wingAddSpoilerButton;
	}

	public void setWingAddSpoilerButton(Button wingAddSpoilerButton) {
		this.wingAddSpoilerButton = wingAddSpoilerButton;
	}

	public Button getWingChooseInnerAirfoilPanel1Button() {
		return wingChooseInnerAirfoilPanel1Button;
	}

	public void setWingChooseInnerAirfoilPanel1Button(Button wingChooseInnerAirfoilPanel1Button) {
		this.wingChooseInnerAirfoilPanel1Button = wingChooseInnerAirfoilPanel1Button;
	}

	public Button getWingChooseOuterAirfoilPanel1Button() {
		return wingChooseOuterAirfoilPanel1Button;
	}

	public void setWingChooseOuterAirfoilPanel1Button(Button wingChooseOuterAirfoilPanel1Button) {
		this.wingChooseOuterAirfoilPanel1Button = wingChooseOuterAirfoilPanel1Button;
	}

	public Button gethTailAddPanelButton() {
		return hTailAddPanelButton;
	}

	public void sethTailAddPanelButton(Button hTailAddPanelButton) {
		this.hTailAddPanelButton = hTailAddPanelButton;
	}

	public Button gethTailInnerSectionAirfoilDetailsPanel1Button() {
		return hTailInnerSectionAirfoilDetailsPanel1Button;
	}

	public void sethTailInnerSectionAirfoilDetailsPanel1Button(Button hTailInnerSectionAirfoilDetailsPanel1Button) {
		this.hTailInnerSectionAirfoilDetailsPanel1Button = hTailInnerSectionAirfoilDetailsPanel1Button;
	}

	public Button gethTailOuterSectionAirfoilDetailsPanel1Button() {
		return hTailOuterSectionAirfoilDetailsPanel1Button;
	}

	public void sethTailOuterSectionAirfoilDetailsPanel1Button(Button hTailOuterSectionAirfoilDetailsPanel1Button) {
		this.hTailOuterSectionAirfoilDetailsPanel1Button = hTailOuterSectionAirfoilDetailsPanel1Button;
	}

	public Button gethTailAddElevatorButton() {
		return hTailAddElevatorButton;
	}

	public void sethTailAddElevatorButton(Button hTailAddElevatorButton) {
		this.hTailAddElevatorButton = hTailAddElevatorButton;
	}

	public Button gethTailChooseInnerAirfoilPanel1Button() {
		return hTailChooseInnerAirfoilPanel1Button;
	}

	public void sethTailChooseInnerAirfoilPanel1Button(Button hTailChooseInnerAirfoilPanel1Button) {
		this.hTailChooseInnerAirfoilPanel1Button = hTailChooseInnerAirfoilPanel1Button;
	}

	public Button gethTailChooseOuterAirfoilPanel1Button() {
		return hTailChooseOuterAirfoilPanel1Button;
	}

	public void sethTailChooseOuterAirfoilPanel1Button(Button hTailChooseOuterAirfoilPanel1Button) {
		this.hTailChooseOuterAirfoilPanel1Button = hTailChooseOuterAirfoilPanel1Button;
	}

	public Button getvTailAddPanelButton() {
		return vTailAddPanelButton;
	}

	public void setvTailAddPanelButton(Button vTailAddPanelButton) {
		this.vTailAddPanelButton = vTailAddPanelButton;
	}

	public Button getvTailInnerSectionAirfoilDetailsPanel1Button() {
		return vTailInnerSectionAirfoilDetailsPanel1Button;
	}

	public void setvTailInnerSectionAirfoilDetailsPanel1Button(Button vTailInnerSectionAirfoilDetailsPanel1Button) {
		this.vTailInnerSectionAirfoilDetailsPanel1Button = vTailInnerSectionAirfoilDetailsPanel1Button;
	}

	public Button getvTailOuterSectionAirfoilDetailsPanel1Button() {
		return vTailOuterSectionAirfoilDetailsPanel1Button;
	}

	public void setvTailOuterSectionAirfoilDetailsPanel1Button(Button vTailOuterSectionAirfoilDetailsPanel1Button) {
		this.vTailOuterSectionAirfoilDetailsPanel1Button = vTailOuterSectionAirfoilDetailsPanel1Button;
	}

	public Button getvTailAddRudderButton() {
		return vTailAddRudderButton;
	}

	public void setvTailAddRudderButton(Button vTailAddRudderButton) {
		this.vTailAddRudderButton = vTailAddRudderButton;
	}

	public Button getvTailChooseInnerAirfoilPanel1Button() {
		return vTailChooseInnerAirfoilPanel1Button;
	}

	public void setvTailChooseInnerAirfoilPanel1Button(Button vTailChooseInnerAirfoilPanel1Button) {
		this.vTailChooseInnerAirfoilPanel1Button = vTailChooseInnerAirfoilPanel1Button;
	}

	public Button getvTailChooseOuterAirfoilPanel1Button() {
		return vTailChooseOuterAirfoilPanel1Button;
	}

	public void setvTailChooseOuterAirfoilPanel1Button(Button vTailChooseOuterAirfoilPanel1Button) {
		this.vTailChooseOuterAirfoilPanel1Button = vTailChooseOuterAirfoilPanel1Button;
	}

	public Button getCanardAddPanelButton() {
		return canardAddPanelButton;
	}

	public void setCanardAddPanelButton(Button canardAddPanelButton) {
		this.canardAddPanelButton = canardAddPanelButton;
	}

	public Button getCanardInnerSectionAirfoilDetailsPanel1Button() {
		return canardInnerSectionAirfoilDetailsPanel1Button;
	}

	public void setCanardInnerSectionAirfoilDetailsPanel1Button(Button canardInnerSectionAirfoilDetailsPanel1Button) {
		this.canardInnerSectionAirfoilDetailsPanel1Button = canardInnerSectionAirfoilDetailsPanel1Button;
	}

	public Button getCanardOuterSectionAirfoilDetailsPanel1Button() {
		return canardOuterSectionAirfoilDetailsPanel1Button;
	}

	public void setCanardOuterSectionAirfoilDetailsPanel1Button(Button canardOuterSectionAirfoilDetailsPanel1Button) {
		this.canardOuterSectionAirfoilDetailsPanel1Button = canardOuterSectionAirfoilDetailsPanel1Button;
	}

	public Button getCanardAddControlSurfaceButton() {
		return canardAddControlSurfaceButton;
	}

	public void setCanardAddControlSurfaceButton(Button canardAddControlSurfaceButton) {
		this.canardAddControlSurfaceButton = canardAddControlSurfaceButton;
	}

	public Button getCanardChooseInnerAirfoilPanel1Button() {
		return canardChooseInnerAirfoilPanel1Button;
	}

	public void setCanardChooseInnerAirfoilPanel1Button(Button canardChooseInnerAirfoilPanel1Button) {
		this.canardChooseInnerAirfoilPanel1Button = canardChooseInnerAirfoilPanel1Button;
	}

	public Button getCanardChooseOuterAirfoilPanel1Button() {
		return canardChooseOuterAirfoilPanel1Button;
	}

	public void setCanardChooseOuterAirfoilPanel1Button(Button canardChooseOuterAirfoilPanel1Button) {
		this.canardChooseOuterAirfoilPanel1Button = canardChooseOuterAirfoilPanel1Button;
	}

	public Button getNacelleEstimateGeometryButton1() {
		return nacelleEstimateGeometryButton1;
	}

	public void setNacelleEstimateGeometryButton1(Button nacelleEstimateGeometryButton1) {
		this.nacelleEstimateGeometryButton1 = nacelleEstimateGeometryButton1;
	}

	public Button getNacelleKInletInfoButton1() {
		return nacelleKInletInfoButton1;
	}

	public void setNacelleKInletInfoButton1(Button nacelleKInletInfoButton1) {
		this.nacelleKInletInfoButton1 = nacelleKInletInfoButton1;
	}

	public Button getNacelleKOutletInfoButton1() {
		return nacelleKOutletInfoButton1;
	}

	public void setNacelleKOutletInfoButton1(Button nacelleKOutletInfoButton1) {
		this.nacelleKOutletInfoButton1 = nacelleKOutletInfoButton1;
	}

	public Button getNacelleKLengthInfoButton1() {
		return nacelleKLengthInfoButton1;
	}

	public void setNacelleKLengthInfoButton1(Button nacelleKLengthInfoButton1) {
		this.nacelleKLengthInfoButton1 = nacelleKLengthInfoButton1;
	}

	public Button getNacelleKDiameterOutletInfoButton1() {
		return nacelleKDiameterOutletInfoButton1;
	}

	public void setNacelleKDiameterOutletInfoButton1(Button nacelleKDiameterOutletInfoButton1) {
		this.nacelleKDiameterOutletInfoButton1 = nacelleKDiameterOutletInfoButton1;
	}

	public Button getLandingGearsKMainLegLengthInfoButton() {
		return landingGearsKMainLegLengthInfoButton;
	}

	public void setLandingGearsKMainLegLengthInfoButton(Button landingGearsKMainLegLengthInfoButton) {
		this.landingGearsKMainLegLengthInfoButton = landingGearsKMainLegLengthInfoButton;
	}

	public Map<Button, Integer> getWingAirfoilDetailsButtonAndTabsMap() {
		return wingAirfoilDetailsButtonAndTabsMap;
	}

	public void setWingAirfoilDetailsButtonAndTabsMap(Map<Button, Integer> wingAirfoilDetailsButtonAndTabsMap) {
		this.wingAirfoilDetailsButtonAndTabsMap = wingAirfoilDetailsButtonAndTabsMap;
	}

	public Map<Button, Integer> gethTailAirfoilDetailsButtonAndTabsMap() {
		return hTailAirfoilDetailsButtonAndTabsMap;
	}

	public void sethTailAirfoilDetailsButtonAndTabsMap(Map<Button, Integer> hTailAirfoilDetailsButtonAndTabsMap) {
		this.hTailAirfoilDetailsButtonAndTabsMap = hTailAirfoilDetailsButtonAndTabsMap;
	}

	public Map<Button, Integer> getvTailAirfoilDetailsButtonAndTabsMap() {
		return vTailAirfoilDetailsButtonAndTabsMap;
	}

	public void setvTailAirfoilDetailsButtonAndTabsMap(Map<Button, Integer> vTailAirfoilDetailsButtonAndTabsMap) {
		this.vTailAirfoilDetailsButtonAndTabsMap = vTailAirfoilDetailsButtonAndTabsMap;
	}

	public Map<Button, Integer> getCanardAirfoilDetailsButtonAndTabsMap() {
		return canardAirfoilDetailsButtonAndTabsMap;
	}

	public void setCanardAirfoilDetailsButtonAndTabsMap(Map<Button, Integer> canardAirfoilDetailsButtonAndTabsMap) {
		this.canardAirfoilDetailsButtonAndTabsMap = canardAirfoilDetailsButtonAndTabsMap;
	}

	public CheckComboBox<String> getUpdateAircraftDataFromFileComboBox() {
		return updateAircraftDataFromFileComboBox;
	}

	public void setUpdateAircraftDataFromFileComboBox(CheckComboBox<String> updateAircraftDataFromFileComboBox) {
		this.updateAircraftDataFromFileComboBox = updateAircraftDataFromFileComboBox;
	}

	public boolean isUpdateCabinConfigurationDataFromFile() {
		return updateCabinConfigurationDataFromFile;
	}

	public void setUpdateCabinConfigurationDataFromFile(boolean updateCabinConfigurationDataFromFile) {
		this.updateCabinConfigurationDataFromFile = updateCabinConfigurationDataFromFile;
	}

	public boolean isUpdateFuselageDataFromFile() {
		return updateFuselageDataFromFile;
	}

	public void setUpdateFuselageDataFromFile(boolean updateFuselageDataFromFile) {
		this.updateFuselageDataFromFile = updateFuselageDataFromFile;
	}

	public boolean isUpdateWingDataFromFile() {
		return updateWingDataFromFile;
	}

	public void setUpdateWingDataFromFile(boolean updateWingDataFromFile) {
		this.updateWingDataFromFile = updateWingDataFromFile;
	}

	public boolean isUpdateHTailDataFromFile() {
		return updateHTailDataFromFile;
	}

	public void setUpdateHTailDataFromFile(boolean updateHTailDataFromFile) {
		this.updateHTailDataFromFile = updateHTailDataFromFile;
	}

	public boolean isUpdateVTailDataFromFile() {
		return updateVTailDataFromFile;
	}

	public void setUpdateVTailDataFromFile(boolean updateVTailDataFromFile) {
		this.updateVTailDataFromFile = updateVTailDataFromFile;
	}

	public boolean isUpdateCanardDataFromFile() {
		return updateCanardDataFromFile;
	}

	public void setUpdateCanardDataFromFile(boolean updateCanardDataFromFile) {
		this.updateCanardDataFromFile = updateCanardDataFromFile;
	}

	public boolean isUpdatePowerPlantDataFromFile() {
		return updatePowerPlantDataFromFile;
	}

	public void setUpdatePowerPlantDataFromFile(boolean updatePowerPlantDataFromFile) {
		this.updatePowerPlantDataFromFile = updatePowerPlantDataFromFile;
	}

	public boolean isUpdateNacellesDataFromFile() {
		return updateNacellesDataFromFile;
	}

	public void setUpdateNacellesDataFromFile(boolean updateNacellesDataFromFile) {
		this.updateNacellesDataFromFile = updateNacellesDataFromFile;
	}

	public boolean isUpdateLandingGearsDataFromFile() {
		return updateLandingGearsDataFromFile;
	}

	public void setUpdateLandingGearsDataFromFile(boolean updateLandingGearsDataFromFile) {
		this.updateLandingGearsDataFromFile = updateLandingGearsDataFromFile;
	}

	public String getFuselageXPositionValue() {
		return fuselageXPositionValue;
	}

	public void setFuselageXPositionValue(String fuselageXPositionValue) {
		this.fuselageXPositionValue = fuselageXPositionValue;
	}

	public String getFuselageXPositionUnit() {
		return fuselageXPositionUnit;
	}

	public void setFuselageXPositionUnit(String fuselageXPositionUnit) {
		this.fuselageXPositionUnit = fuselageXPositionUnit;
	}

	public String getFuselageYPositionValue() {
		return fuselageYPositionValue;
	}

	public void setFuselageYPositionValue(String fuselageYPositionValue) {
		this.fuselageYPositionValue = fuselageYPositionValue;
	}

	public String getFuselageYPositionUnit() {
		return fuselageYPositionUnit;
	}

	public void setFuselageYPositionUnit(String fuselageYPositionUnit) {
		this.fuselageYPositionUnit = fuselageYPositionUnit;
	}

	public String getFuselageZPositionValue() {
		return fuselageZPositionValue;
	}

	public void setFuselageZPositionValue(String fuselageZPositionValue) {
		this.fuselageZPositionValue = fuselageZPositionValue;
	}

	public String getFuselageZPositionUnit() {
		return fuselageZPositionUnit;
	}

	public void setFuselageZPositionUnit(String fuselageZPositionUnit) {
		this.fuselageZPositionUnit = fuselageZPositionUnit;
	}

	public String getWingXPositionValue() {
		return wingXPositionValue;
	}

	public void setWingXPositionValue(String wingXPositionValue) {
		this.wingXPositionValue = wingXPositionValue;
	}

	public String getWingXPositionUnit() {
		return wingXPositionUnit;
	}

	public void setWingXPositionUnit(String wingXPositionUnit) {
		this.wingXPositionUnit = wingXPositionUnit;
	}

	public String getWingYPositionValue() {
		return wingYPositionValue;
	}

	public void setWingYPositionValue(String wingYPositionValue) {
		this.wingYPositionValue = wingYPositionValue;
	}

	public String getWingYPositionUnit() {
		return wingYPositionUnit;
	}

	public void setWingYPositionUnit(String wingYPositionUnit) {
		this.wingYPositionUnit = wingYPositionUnit;
	}

	public String getWingZPositionValue() {
		return wingZPositionValue;
	}

	public void setWingZPositionValue(String wingZPositionValue) {
		this.wingZPositionValue = wingZPositionValue;
	}

	public String getWingZPositionUnit() {
		return wingZPositionUnit;
	}

	public void setWingZPositionUnit(String wingZPositionUnit) {
		this.wingZPositionUnit = wingZPositionUnit;
	}

	public String getWingRiggingAngleValue() {
		return wingRiggingAngleValue;
	}

	public void setWingRiggingAngleValue(String wingRiggingAngleValue) {
		this.wingRiggingAngleValue = wingRiggingAngleValue;
	}

	public String getWingRiggingAngleUnit() {
		return wingRiggingAngleUnit;
	}

	public void setWingRiggingAngleUnit(String wingRiggingAngleUnit) {
		this.wingRiggingAngleUnit = wingRiggingAngleUnit;
	}

	public String gethTailXPositionValue() {
		return hTailXPositionValue;
	}

	public void sethTailXPositionValue(String hTailXPositionValue) {
		this.hTailXPositionValue = hTailXPositionValue;
	}

	public String gethTailXPositionUnit() {
		return hTailXPositionUnit;
	}

	public void sethTailXPositionUnit(String hTailXPositionUnit) {
		this.hTailXPositionUnit = hTailXPositionUnit;
	}

	public String gethTailYPositionValue() {
		return hTailYPositionValue;
	}

	public void sethTailYPositionValue(String hTailYPositionValue) {
		this.hTailYPositionValue = hTailYPositionValue;
	}

	public String gethTailYPositionUnit() {
		return hTailYPositionUnit;
	}

	public void sethTailYPositionUnit(String hTailYPositionUnit) {
		this.hTailYPositionUnit = hTailYPositionUnit;
	}

	public String gethTailZPositionValue() {
		return hTailZPositionValue;
	}

	public void sethTailZPositionValue(String hTailZPositionValue) {
		this.hTailZPositionValue = hTailZPositionValue;
	}

	public String gethTailZPositionUnit() {
		return hTailZPositionUnit;
	}

	public void sethTailZPositionUnit(String hTailZPositionUnit) {
		this.hTailZPositionUnit = hTailZPositionUnit;
	}

	public String gethTailRiggingAngleValue() {
		return hTailRiggingAngleValue;
	}

	public void sethTailRiggingAngleValue(String hTailRiggingAngleValue) {
		this.hTailRiggingAngleValue = hTailRiggingAngleValue;
	}

	public String gethTailRiggingAngleUnit() {
		return hTailRiggingAngleUnit;
	}

	public void sethTailRiggingAngleUnit(String hTailRiggingAngleUnit) {
		this.hTailRiggingAngleUnit = hTailRiggingAngleUnit;
	}

	public String getvTailXPositionValue() {
		return vTailXPositionValue;
	}

	public void setvTailXPositionValue(String vTailXPositionValue) {
		this.vTailXPositionValue = vTailXPositionValue;
	}

	public String getvTailXPositionUnit() {
		return vTailXPositionUnit;
	}

	public void setvTailXPositionUnit(String vTailXPositionUnit) {
		this.vTailXPositionUnit = vTailXPositionUnit;
	}

	public String getvTailYPositionValue() {
		return vTailYPositionValue;
	}

	public void setvTailYPositionValue(String vTailYPositionValue) {
		this.vTailYPositionValue = vTailYPositionValue;
	}

	public String getvTailYPositionUnit() {
		return vTailYPositionUnit;
	}

	public void setvTailYPositionUnit(String vTailYPositionUnit) {
		this.vTailYPositionUnit = vTailYPositionUnit;
	}

	public String getvTailZPositionValue() {
		return vTailZPositionValue;
	}

	public void setvTailZPositionValue(String vTailZPositionValue) {
		this.vTailZPositionValue = vTailZPositionValue;
	}

	public String getvTailZPositionUnit() {
		return vTailZPositionUnit;
	}

	public void setvTailZPositionUnit(String vTailZPositionUnit) {
		this.vTailZPositionUnit = vTailZPositionUnit;
	}

	public String getvTailRiggingAngleValue() {
		return vTailRiggingAngleValue;
	}

	public void setvTailRiggingAngleValue(String vTailRiggingAngleValue) {
		this.vTailRiggingAngleValue = vTailRiggingAngleValue;
	}

	public String getvTailRiggingAngleUnit() {
		return vTailRiggingAngleUnit;
	}

	public void setvTailRiggingAngleUnit(String vTailRiggingAngleUnit) {
		this.vTailRiggingAngleUnit = vTailRiggingAngleUnit;
	}

	public String getCanardXPositionValue() {
		return canardXPositionValue;
	}

	public void setCanardXPositionValue(String canardXPositionValue) {
		this.canardXPositionValue = canardXPositionValue;
	}

	public String getCanardXPositionUnit() {
		return canardXPositionUnit;
	}

	public void setCanardXPositionUnit(String canardXPositionUnit) {
		this.canardXPositionUnit = canardXPositionUnit;
	}

	public String getCanardYPositionValue() {
		return canardYPositionValue;
	}

	public void setCanardYPositionValue(String canardYPositionValue) {
		this.canardYPositionValue = canardYPositionValue;
	}

	public String getCanardYPositionUnit() {
		return canardYPositionUnit;
	}

	public void setCanardYPositionUnit(String canardYPositionUnit) {
		this.canardYPositionUnit = canardYPositionUnit;
	}

	public String getCanardZPositionValue() {
		return canardZPositionValue;
	}

	public void setCanardZPositionValue(String canardZPositionValue) {
		this.canardZPositionValue = canardZPositionValue;
	}

	public String getCanardZPositionUnit() {
		return canardZPositionUnit;
	}

	public void setCanardZPositionUnit(String canardZPositionUnit) {
		this.canardZPositionUnit = canardZPositionUnit;
	}

	public String getCanardRiggingAngleValue() {
		return canardRiggingAngleValue;
	}

	public void setCanardRiggingAngleValue(String canardRiggingAngleValue) {
		this.canardRiggingAngleValue = canardRiggingAngleValue;
	}

	public String getCanardRiggingAngleUnit() {
		return canardRiggingAngleUnit;
	}

	public void setCanardRiggingAngleUnit(String canardRiggingAngleUnit) {
		this.canardRiggingAngleUnit = canardRiggingAngleUnit;
	}

	public List<String> getEngineXPositionValueList() {
		return engineXPositionValueList;
	}

	public void setEngineXPositionValueList(List<String> engineXPositionValueList) {
		this.engineXPositionValueList = engineXPositionValueList;
	}

	public List<String> getEngineXPositionUnitList() {
		return engineXPositionUnitList;
	}

	public void setEngineXPositionUnitList(List<String> engineXPositionUnitList) {
		this.engineXPositionUnitList = engineXPositionUnitList;
	}

	public List<String> getEngineYPositionValueList() {
		return engineYPositionValueList;
	}

	public void setEngineYPositionValueList(List<String> engineYPositionValueList) {
		this.engineYPositionValueList = engineYPositionValueList;
	}

	public List<String> getEngineYPositionUnitList() {
		return engineYPositionUnitList;
	}

	public void setEngineYPositionUnitList(List<String> engineYPositionUnitList) {
		this.engineYPositionUnitList = engineYPositionUnitList;
	}

	public List<String> getEngineZPositionValueList() {
		return engineZPositionValueList;
	}

	public void setEngineZPositionValueList(List<String> engineZPositionValueList) {
		this.engineZPositionValueList = engineZPositionValueList;
	}

	public List<String> getEngineZPositionUnitList() {
		return engineZPositionUnitList;
	}

	public void setEngineZPositionUnitList(List<String> engineZPositionUnitList) {
		this.engineZPositionUnitList = engineZPositionUnitList;
	}

	public List<String> getEngineTiltAngleValueList() {
		return engineTiltAngleValueList;
	}

	public void setEngineTiltAngleValueList(List<String> engineTiltAngleValueList) {
		this.engineTiltAngleValueList = engineTiltAngleValueList;
	}

	public List<String> getEngineTiltAngleUnitList() {
		return engineTiltAngleUnitList;
	}

	public void setEngineTiltAngleUnitList(List<String> engineTiltAngleUnitList) {
		this.engineTiltAngleUnitList = engineTiltAngleUnitList;
	}

	public List<String> getEngineMountinPositionValueList() {
		return engineMountinPositionValueList;
	}

	public void setEngineMountinPositionValueList(List<String> engineMountinPositionValueList) {
		this.engineMountinPositionValueList = engineMountinPositionValueList;
	}

	public List<String> getNacelleXPositionValueList() {
		return nacelleXPositionValueList;
	}

	public void setNacelleXPositionValueList(List<String> nacelleXPositionValueList) {
		this.nacelleXPositionValueList = nacelleXPositionValueList;
	}

	public List<String> getNacelleXPositionUnitList() {
		return nacelleXPositionUnitList;
	}

	public void setNacelleXPositionUnitList(List<String> nacelleXPositionUnitList) {
		this.nacelleXPositionUnitList = nacelleXPositionUnitList;
	}

	public List<String> getNacelleYPositionValueList() {
		return nacelleYPositionValueList;
	}

	public void setNacelleYPositionValueList(List<String> nacelleYPositionValueList) {
		this.nacelleYPositionValueList = nacelleYPositionValueList;
	}

	public List<String> getNacelleYPositionUnitList() {
		return nacelleYPositionUnitList;
	}

	public void setNacelleYPositionUnitList(List<String> nacelleYPositionUnitList) {
		this.nacelleYPositionUnitList = nacelleYPositionUnitList;
	}

	public List<String> getNacelleZPositionValueList() {
		return nacelleZPositionValueList;
	}

	public void setNacelleZPositionValueList(List<String> nacelleZPositionValueList) {
		this.nacelleZPositionValueList = nacelleZPositionValueList;
	}

	public List<String> getNacelleZPositionUnitList() {
		return nacelleZPositionUnitList;
	}

	public void setNacelleZPositionUnitList(List<String> nacelleZPositionUnitList) {
		this.nacelleZPositionUnitList = nacelleZPositionUnitList;
	}

	public List<String> getNacelleMountinPositionValueList() {
		return nacelleMountinPositionValueList;
	}

	public void setNacelleMountinPositionValueList(List<String> nacelleMountinPositionValueList) {
		this.nacelleMountinPositionValueList = nacelleMountinPositionValueList;
	}

	public String getNoseLandingGearsXPositionValue() {
		return noseLandingGearsXPositionValue;
	}

	public void setNoseLandingGearsXPositionValue(String noseLandingGearsXPositionValue) {
		this.noseLandingGearsXPositionValue = noseLandingGearsXPositionValue;
	}

	public String getNoseLandingGearsXPositionUnit() {
		return noseLandingGearsXPositionUnit;
	}

	public void setNoseLandingGearsXPositionUnit(String noseLandingGearsXPositionUnit) {
		this.noseLandingGearsXPositionUnit = noseLandingGearsXPositionUnit;
	}

	public String getNoseLandingGearsYPositionValue() {
		return noseLandingGearsYPositionValue;
	}

	public void setNoseLandingGearsYPositionValue(String noseLandingGearsYPositionValue) {
		this.noseLandingGearsYPositionValue = noseLandingGearsYPositionValue;
	}

	public String getNoseLandingGearsYPositionUnit() {
		return noseLandingGearsYPositionUnit;
	}

	public void setNoseLandingGearsYPositionUnit(String noseLandingGearsYPositionUnit) {
		this.noseLandingGearsYPositionUnit = noseLandingGearsYPositionUnit;
	}

	public String getNoseLandingGearsZPositionValue() {
		return noseLandingGearsZPositionValue;
	}

	public void setNoseLandingGearsZPositionValue(String noseLandingGearsZPositionValue) {
		this.noseLandingGearsZPositionValue = noseLandingGearsZPositionValue;
	}

	public String getNoseLandingGearsZPositionUnit() {
		return noseLandingGearsZPositionUnit;
	}

	public void setNoseLandingGearsZPositionUnit(String noseLandingGearsZPositionUnit) {
		this.noseLandingGearsZPositionUnit = noseLandingGearsZPositionUnit;
	}

	public String getLandingGearsMountinPositionValue() {
		return landingGearsMountinPositionValue;
	}

	public void setLandingGearsMountinPositionValue(String landingGearsMountinPositionValue) {
		this.landingGearsMountinPositionValue = landingGearsMountinPositionValue;
	}

	public String getMainLandingGearsXPositionValue() {
		return mainLandingGearsXPositionValue;
	}

	public void setMainLandingGearsXPositionValue(String mainLandingGearsXPositionValue) {
		this.mainLandingGearsXPositionValue = mainLandingGearsXPositionValue;
	}

	public String getMainLandingGearsXPositionUnit() {
		return mainLandingGearsXPositionUnit;
	}

	public void setMainLandingGearsXPositionUnit(String mainLandingGearsXPositionUnit) {
		this.mainLandingGearsXPositionUnit = mainLandingGearsXPositionUnit;
	}

	public String getMainLandingGearsYPositionValue() {
		return mainLandingGearsYPositionValue;
	}

	public void setMainLandingGearsYPositionValue(String mainLandingGearsYPositionValue) {
		this.mainLandingGearsYPositionValue = mainLandingGearsYPositionValue;
	}

	public String getMainLandingGearsYPositionUnit() {
		return mainLandingGearsYPositionUnit;
	}

	public void setMainLandingGearsYPositionUnit(String mainLandingGearsYPositionUnit) {
		this.mainLandingGearsYPositionUnit = mainLandingGearsYPositionUnit;
	}

	public String getMainLandingGearsZPositionValue() {
		return mainLandingGearsZPositionValue;
	}

	public void setMainLandingGearsZPositionValue(String mainLandingGearsZPositionValue) {
		this.mainLandingGearsZPositionValue = mainLandingGearsZPositionValue;
	}

	public String getMainLandingGearsZPositionUnit() {
		return mainLandingGearsZPositionUnit;
	}

	public void setMainLandingGearsZPositionUnit(String mainLandingGearsZPositionUnit) {
		this.mainLandingGearsZPositionUnit = mainLandingGearsZPositionUnit;
	}
	
	public FileChooser getAirfoilFileChooser() {
		return airfoilFileChooser;
	}

	public void setAirfoilFileChooser(FileChooser airfoilFileChooser) {
		this.airfoilFileChooser = airfoilFileChooser;
	}

	public FileChooser getAircraftFileChooser() {
		return aircraftFileChooser;
	}

	public void setAircraftFileChooser(FileChooser aircraftFileChooser) {
		this.aircraftFileChooser = aircraftFileChooser;
	}

	public FileChooser getEngineDatabaseFileChooser() {
		return engineDatabaseFileChooser;
	}

	public void setEngineDatabaseFileChooser(FileChooser engineDatabaseFileChooser) {
		this.engineDatabaseFileChooser = engineDatabaseFileChooser;
	}

	public FileChooser getSaveAircraftFileChooser() {
		return saveAircraftFileChooser;
	}

	public void setSaveAircraftFileChooser(FileChooser saveAircraftFileChooser) {
		this.saveAircraftFileChooser = saveAircraftFileChooser;
	}

	public FileChooser getCabinConfigurationFileChooser() {
		return cabinConfigurationFileChooser;
	}

	public void setCabinConfigurationFileChooser(FileChooser cabinConfigurationFileChooser) {
		this.cabinConfigurationFileChooser = cabinConfigurationFileChooser;
	}

	public FileChooser getFuselageFileChooser() {
		return fuselageFileChooser;
	}

	public void setFuselageFileChooser(FileChooser fuselageFileChooser) {
		this.fuselageFileChooser = fuselageFileChooser;
	}

	public FileChooser getWingFileChooser() {
		return wingFileChooser;
	}

	public void setWingFileChooser(FileChooser wingFileChooser) {
		this.wingFileChooser = wingFileChooser;
	}

	public FileChooser gethTailFileChooser() {
		return hTailFileChooser;
	}

	public void sethTailFileChooser(FileChooser hTailFileChooser) {
		this.hTailFileChooser = hTailFileChooser;
	}

	public FileChooser getvTailFileChooser() {
		return vTailFileChooser;
	}

	public void setvTailFileChooser(FileChooser vTailFileChooser) {
		this.vTailFileChooser = vTailFileChooser;
	}

	public FileChooser getCanardFileChooser() {
		return canardFileChooser;
	}

	public void setCanardFileChooser(FileChooser canardFileChooser) {
		this.canardFileChooser = canardFileChooser;
	}

	public FileChooser getEngineFileChooser() {
		return engineFileChooser;
	}

	public void setEngineFileChooser(FileChooser engineFileChooser) {
		this.engineFileChooser = engineFileChooser;
	}

	public FileChooser getNacelleFileChooser() {
		return nacelleFileChooser;
	}

	public void setNacelleFileChooser(FileChooser nacelleFileChooser) {
		this.nacelleFileChooser = nacelleFileChooser;
	}

	public FileChooser getLandingGearsFileChooser() {
		return landingGearsFileChooser;
	}

	public void setLandingGearsFileChooser(FileChooser landingGearsFileChooser) {
		this.landingGearsFileChooser = landingGearsFileChooser;
	}

	public ValidationSupport getValidation() {
		return validation;
	}

	public void setValidation(ValidationSupport validation) {
		this.validation = validation;
	}

	public String getTextFieldAlertStyle() {
		return textFieldAlertStyle;
	}

	public void setTextFieldAlertStyle(String textFieldAlertStyle) {
		this.textFieldAlertStyle = textFieldAlertStyle;
	}

	public String getButtonSuggestedActionStyle() {
		return buttonSuggestedActionStyle;
	}

	public void setButtonSuggestedActionStyle(String buttonSuggestedActionStyle) {
		this.buttonSuggestedActionStyle = buttonSuggestedActionStyle;
	}

	public ObservableList<String> getAircraftTypeList() {
		return aircraftTypeList;
	}

	public void setAircraftTypeList(ObservableList<String> aircraftTypeList) {
		this.aircraftTypeList = aircraftTypeList;
	}

	public ObservableList<String> getRegulationsTypeList() {
		return regulationsTypeList;
	}

	public void setRegulationsTypeList(ObservableList<String> regulationsTypeList) {
		this.regulationsTypeList = regulationsTypeList;
	}

	public ObservableList<String> getWindshieldTypeList() {
		return windshieldTypeList;
	}

	public void setWindshieldTypeList(ObservableList<String> windshieldTypeList) {
		this.windshieldTypeList = windshieldTypeList;
	}

	public ObservableList<String> getPowerPlantMountingPositionTypeList() {
		return powerPlantMountingPositionTypeList;
	}

	public void setPowerPlantMountingPositionTypeList(ObservableList<String> powerPlantMountingPositionTypeList) {
		this.powerPlantMountingPositionTypeList = powerPlantMountingPositionTypeList;
	}

	public ObservableList<String> getNacelleMountingPositionTypeList() {
		return nacelleMountingPositionTypeList;
	}

	public void setNacelleMountingPositionTypeList(ObservableList<String> nacelleMountingPositionTypeList) {
		this.nacelleMountingPositionTypeList = nacelleMountingPositionTypeList;
	}

	public ObservableList<String> getLandingGearsMountingPositionTypeList() {
		return landingGearsMountingPositionTypeList;
	}

	public void setLandingGearsMountingPositionTypeList(ObservableList<String> landingGearsMountingPositionTypeList) {
		this.landingGearsMountingPositionTypeList = landingGearsMountingPositionTypeList;
	}

	public ObservableList<String> getFuselageAdjustCriteriaTypeList() {
		return fuselageAdjustCriteriaTypeList;
	}

	public void setFuselageAdjustCriteriaTypeList(ObservableList<String> fuselageAdjustCriteriaTypeList) {
		this.fuselageAdjustCriteriaTypeList = fuselageAdjustCriteriaTypeList;
	}

	public ObservableList<String> getLiftingSurfaceAdjustCriteriaTypeList() {
		return liftingSurfaceAdjustCriteriaTypeList;
	}

	public void setLiftingSurfaceAdjustCriteriaTypeList(ObservableList<String> liftingSurfaceAdjustCriteriaTypeList) {
		this.liftingSurfaceAdjustCriteriaTypeList = liftingSurfaceAdjustCriteriaTypeList;
	}

	public ObservableList<String> getCabinConfigurationClassesTypeList() {
		return cabinConfigurationClassesTypeList;
	}

	public void setCabinConfigurationClassesTypeList(ObservableList<String> cabinConfigurationClassesTypeList) {
		this.cabinConfigurationClassesTypeList = cabinConfigurationClassesTypeList;
	}

	public ObservableList<String> getJetEngineTypeList() {
		return jetEngineTypeList;
	}

	public void setJetEngineTypeList(ObservableList<String> jetEngineTypeList) {
		this.jetEngineTypeList = jetEngineTypeList;
	}

	public ObservableList<String> getTurbopropEngineTypeList() {
		return turbopropEngineTypeList;
	}

	public void setTurbopropEngineTypeList(ObservableList<String> turbopropEngineTypeList) {
		this.turbopropEngineTypeList = turbopropEngineTypeList;
	}

	public ObservableList<String> getPistonEngineTypeList() {
		return pistonEngineTypeList;
	}

	public void setPistonEngineTypeList(ObservableList<String> pistonEngineTypeList) {
		this.pistonEngineTypeList = pistonEngineTypeList;
	}

	public ObservableList<String> getFlapTypeList() {
		return flapTypeList;
	}

	public void setFlapTypeList(ObservableList<String> flapTypeList) {
		this.flapTypeList = flapTypeList;
	}

	public ObservableList<String> getAileronTypeList() {
		return aileronTypeList;
	}

	public void setAileronTypeList(ObservableList<String> aileronTypeList) {
		this.aileronTypeList = aileronTypeList;
	}

	public ObservableList<String> getElevatorTypeList() {
		return elevatorTypeList;
	}

	public void setElevatorTypeList(ObservableList<String> elevatorTypeList) {
		this.elevatorTypeList = elevatorTypeList;
	}

	public ObservableList<String> getRudderTypeList() {
		return rudderTypeList;
	}

	public void setRudderTypeList(ObservableList<String> rudderTypeList) {
		this.rudderTypeList = rudderTypeList;
	}

	public ObservableList<String> getCanardSurfaceTypeList() {
		return canardSurfaceTypeList;
	}

	public void setCanardSurfaceTypeList(ObservableList<String> canardSurfaceTypeList) {
		this.canardSurfaceTypeList = canardSurfaceTypeList;
	}

	public ObservableList<String> getLengthUnitsList() {
		return lengthUnitsList;
	}

	public void setLengthUnitsList(ObservableList<String> lengthUnitsList) {
		this.lengthUnitsList = lengthUnitsList;
	}

	public ObservableList<String> getAngleUnitsList() {
		return angleUnitsList;
	}

	public void setAngleUnitsList(ObservableList<String> angleUnitsList) {
		this.angleUnitsList = angleUnitsList;
	}

	public ObservableList<String> getMassUnitsList() {
		return massUnitsList;
	}

	public void setMassUnitsList(ObservableList<String> massUnitsList) {
		this.massUnitsList = massUnitsList;
	}

	public ObservableList<String> getAreaUnitsList() {
		return areaUnitsList;
	}

	public void setAreaUnitsList(ObservableList<String> areaUnitsList) {
		this.areaUnitsList = areaUnitsList;
	}

	public ObservableList<String> getForceUnitsList() {
		return forceUnitsList;
	}

	public void setForceUnitsList(ObservableList<String> forceUnitsList) {
		this.forceUnitsList = forceUnitsList;
	}

	public ObservableList<String> getPowerUnitsList() {
		return powerUnitsList;
	}

	public void setPowerUnitsList(ObservableList<String> powerUnitsList) {
		this.powerUnitsList = powerUnitsList;
	}

	public ObservableList<String> getComponentsList() {
		return componentsList;
	}

	public void setComponentsList(ObservableList<String> componentsList) {
		this.componentsList = componentsList;
	}

	public ChoiceBox<String> getAircraftTypeChoiceBox() {
		return aircraftTypeChoiceBox;
	}

	public void setAircraftTypeChoiceBox(ChoiceBox<String> aircraftTypeChoiceBox) {
		this.aircraftTypeChoiceBox = aircraftTypeChoiceBox;
	}

	public ChoiceBox<String> getRegulationsTypeChoiceBox() {
		return regulationsTypeChoiceBox;
	}

	public void setRegulationsTypeChoiceBox(ChoiceBox<String> regulationsTypeChoiceBox) {
		this.regulationsTypeChoiceBox = regulationsTypeChoiceBox;
	}

	public ChoiceBox<String> getEngineMountingPositionTypeChoiceBox1() {
		return engineMountingPositionTypeChoiceBox1;
	}

	public void setEngineMountingPositionTypeChoiceBox1(ChoiceBox<String> engineMountingPositionTypeChoiceBox1) {
		this.engineMountingPositionTypeChoiceBox1 = engineMountingPositionTypeChoiceBox1;
	}

	public ChoiceBox<String> getNacelleMountingPositionTypeChoiceBox1() {
		return nacelleMountingPositionTypeChoiceBox1;
	}

	public void setNacelleMountingPositionTypeChoiceBox1(ChoiceBox<String> nacelleMountingPositionTypeChoiceBox1) {
		this.nacelleMountingPositionTypeChoiceBox1 = nacelleMountingPositionTypeChoiceBox1;
	}

	public ChoiceBox<String> getLandingGearsMountingPositionTypeChoiceBox() {
		return landingGearsMountingPositionTypeChoiceBox;
	}

	public void setLandingGearsMountingPositionTypeChoiceBox(ChoiceBox<String> landingGearsMountingPositionTypeChoiceBox) {
		this.landingGearsMountingPositionTypeChoiceBox = landingGearsMountingPositionTypeChoiceBox;
	}

	public List<TextField> getTextFieldsAircraftEngineFileList() {
		return textFieldsAircraftEngineFileList;
	}

	public void setTextFieldsAircraftEngineFileList(List<TextField> textFieldsAircraftEngineFileList) {
		this.textFieldsAircraftEngineFileList = textFieldsAircraftEngineFileList;
	}

	public List<TextField> getTextFieldAircraftEngineXList() {
		return textFieldAircraftEngineXList;
	}

	public void setTextFieldAircraftEngineXList(List<TextField> textFieldAircraftEngineXList) {
		this.textFieldAircraftEngineXList = textFieldAircraftEngineXList;
	}

	public List<TextField> getTextFieldAircraftEngineYList() {
		return textFieldAircraftEngineYList;
	}

	public void setTextFieldAircraftEngineYList(List<TextField> textFieldAircraftEngineYList) {
		this.textFieldAircraftEngineYList = textFieldAircraftEngineYList;
	}

	public List<TextField> getTextFieldAircraftEngineZList() {
		return textFieldAircraftEngineZList;
	}

	public void setTextFieldAircraftEngineZList(List<TextField> textFieldAircraftEngineZList) {
		this.textFieldAircraftEngineZList = textFieldAircraftEngineZList;
	}

	public List<ChoiceBox<String>> getChoiceBoxesAircraftEnginePositonList() {
		return choiceBoxesAircraftEnginePositonList;
	}

	public void setChoiceBoxesAircraftEnginePositonList(List<ChoiceBox<String>> choiceBoxesAircraftEnginePositonList) {
		this.choiceBoxesAircraftEnginePositonList = choiceBoxesAircraftEnginePositonList;
	}

	public List<TextField> getTextFieldAircraftEngineTiltList() {
		return textFieldAircraftEngineTiltList;
	}

	public void setTextFieldAircraftEngineTiltList(List<TextField> textFieldAircraftEngineTiltList) {
		this.textFieldAircraftEngineTiltList = textFieldAircraftEngineTiltList;
	}

	public List<ChoiceBox<String>> getChoiceBoxAircraftEngineXUnitList() {
		return choiceBoxAircraftEngineXUnitList;
	}

	public void setChoiceBoxAircraftEngineXUnitList(List<ChoiceBox<String>> choiceBoxAircraftEngineXUnitList) {
		this.choiceBoxAircraftEngineXUnitList = choiceBoxAircraftEngineXUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxAircraftEngineYUnitList() {
		return choiceBoxAircraftEngineYUnitList;
	}

	public void setChoiceBoxAircraftEngineYUnitList(List<ChoiceBox<String>> choiceBoxAircraftEngineYUnitList) {
		this.choiceBoxAircraftEngineYUnitList = choiceBoxAircraftEngineYUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxAircraftEngineZUnitList() {
		return choiceBoxAircraftEngineZUnitList;
	}

	public void setChoiceBoxAircraftEngineZUnitList(List<ChoiceBox<String>> choiceBoxAircraftEngineZUnitList) {
		this.choiceBoxAircraftEngineZUnitList = choiceBoxAircraftEngineZUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxAircraftEngineTiltUnitList() {
		return choiceBoxAircraftEngineTiltUnitList;
	}

	public void setChoiceBoxAircraftEngineTiltUnitList(List<ChoiceBox<String>> choiceBoxAircraftEngineTiltUnitList) {
		this.choiceBoxAircraftEngineTiltUnitList = choiceBoxAircraftEngineTiltUnitList;
	}

	public List<Button> getChooseEngineFileButtonList() {
		return chooseEngineFileButtonList;
	}

	public void setChooseEngineFileButtonList(List<Button> chooseEngineFileButtonList) {
		this.chooseEngineFileButtonList = chooseEngineFileButtonList;
	}

	public List<TextField> getTextFieldsAircraftNacelleFileList() {
		return textFieldsAircraftNacelleFileList;
	}

	public void setTextFieldsAircraftNacelleFileList(List<TextField> textFieldsAircraftNacelleFileList) {
		this.textFieldsAircraftNacelleFileList = textFieldsAircraftNacelleFileList;
	}

	public List<TextField> getTextFieldAircraftNacelleXList() {
		return textFieldAircraftNacelleXList;
	}

	public void setTextFieldAircraftNacelleXList(List<TextField> textFieldAircraftNacelleXList) {
		this.textFieldAircraftNacelleXList = textFieldAircraftNacelleXList;
	}

	public List<TextField> getTextFieldAircraftNacelleYList() {
		return textFieldAircraftNacelleYList;
	}

	public void setTextFieldAircraftNacelleYList(List<TextField> textFieldAircraftNacelleYList) {
		this.textFieldAircraftNacelleYList = textFieldAircraftNacelleYList;
	}

	public List<TextField> getTextFieldAircraftNacelleZList() {
		return textFieldAircraftNacelleZList;
	}

	public void setTextFieldAircraftNacelleZList(List<TextField> textFieldAircraftNacelleZList) {
		this.textFieldAircraftNacelleZList = textFieldAircraftNacelleZList;
	}

	public List<ChoiceBox<String>> getChoiceBoxesAircraftNacellePositonList() {
		return choiceBoxesAircraftNacellePositonList;
	}

	public void setChoiceBoxesAircraftNacellePositonList(List<ChoiceBox<String>> choiceBoxesAircraftNacellePositonList) {
		this.choiceBoxesAircraftNacellePositonList = choiceBoxesAircraftNacellePositonList;
	}

	public List<ChoiceBox<String>> getChoiceBoxAircraftNacelleXUnitList() {
		return choiceBoxAircraftNacelleXUnitList;
	}

	public void setChoiceBoxAircraftNacelleXUnitList(List<ChoiceBox<String>> choiceBoxAircraftNacelleXUnitList) {
		this.choiceBoxAircraftNacelleXUnitList = choiceBoxAircraftNacelleXUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxAircraftNacelleYUnitList() {
		return choiceBoxAircraftNacelleYUnitList;
	}

	public void setChoiceBoxAircraftNacelleYUnitList(List<ChoiceBox<String>> choiceBoxAircraftNacelleYUnitList) {
		this.choiceBoxAircraftNacelleYUnitList = choiceBoxAircraftNacelleYUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxAircraftNacelleZUnitList() {
		return choiceBoxAircraftNacelleZUnitList;
	}

	public void setChoiceBoxAircraftNacelleZUnitList(List<ChoiceBox<String>> choiceBoxAircraftNacelleZUnitList) {
		this.choiceBoxAircraftNacelleZUnitList = choiceBoxAircraftNacelleZUnitList;
	}

	public List<Button> getChooseNacelleFileButtonList() {
		return chooseNacelleFileButtonList;
	}

	public void setChooseNacelleFileButtonList(List<Button> chooseNacelleFileButtonList) {
		this.chooseNacelleFileButtonList = chooseNacelleFileButtonList;
	}

	public TextField getTextFieldAircraftInputFile() {
		return textFieldAircraftInputFile;
	}

	public void setTextFieldAircraftInputFile(TextField textFieldAircraftInputFile) {
		this.textFieldAircraftInputFile = textFieldAircraftInputFile;
	}

	public TextField getTextFieldAircraftCabinConfigurationFile() {
		return textFieldAircraftCabinConfigurationFile;
	}

	public void setTextFieldAircraftCabinConfigurationFile(TextField textFieldAircraftCabinConfigurationFile) {
		this.textFieldAircraftCabinConfigurationFile = textFieldAircraftCabinConfigurationFile;
	}

	public TextField getTextFieldAircraftFuselageFile() {
		return textFieldAircraftFuselageFile;
	}

	public void setTextFieldAircraftFuselageFile(TextField textFieldAircraftFuselageFile) {
		this.textFieldAircraftFuselageFile = textFieldAircraftFuselageFile;
	}

	public TextField getTextFieldAircraftFuselageX() {
		return textFieldAircraftFuselageX;
	}

	public void setTextFieldAircraftFuselageX(TextField textFieldAircraftFuselageX) {
		this.textFieldAircraftFuselageX = textFieldAircraftFuselageX;
	}

	public TextField getTextFieldAircraftFuselageY() {
		return textFieldAircraftFuselageY;
	}

	public void setTextFieldAircraftFuselageY(TextField textFieldAircraftFuselageY) {
		this.textFieldAircraftFuselageY = textFieldAircraftFuselageY;
	}

	public TextField getTextFieldAircraftFuselageZ() {
		return textFieldAircraftFuselageZ;
	}

	public void setTextFieldAircraftFuselageZ(TextField textFieldAircraftFuselageZ) {
		this.textFieldAircraftFuselageZ = textFieldAircraftFuselageZ;
	}

	public TextField getTextFieldAircraftWingFile() {
		return textFieldAircraftWingFile;
	}

	public void setTextFieldAircraftWingFile(TextField textFieldAircraftWingFile) {
		this.textFieldAircraftWingFile = textFieldAircraftWingFile;
	}

	public TextField getTextFieldAircraftWingX() {
		return textFieldAircraftWingX;
	}

	public void setTextFieldAircraftWingX(TextField textFieldAircraftWingX) {
		this.textFieldAircraftWingX = textFieldAircraftWingX;
	}

	public TextField getTextFieldAircraftWingY() {
		return textFieldAircraftWingY;
	}

	public void setTextFieldAircraftWingY(TextField textFieldAircraftWingY) {
		this.textFieldAircraftWingY = textFieldAircraftWingY;
	}

	public TextField getTextFieldAircraftWingZ() {
		return textFieldAircraftWingZ;
	}

	public void setTextFieldAircraftWingZ(TextField textFieldAircraftWingZ) {
		this.textFieldAircraftWingZ = textFieldAircraftWingZ;
	}

	public TextField getTextFieldAircraftWingRiggingAngle() {
		return textFieldAircraftWingRiggingAngle;
	}

	public void setTextFieldAircraftWingRiggingAngle(TextField textFieldAircraftWingRiggingAngle) {
		this.textFieldAircraftWingRiggingAngle = textFieldAircraftWingRiggingAngle;
	}

	public TextField getTextFieldAircraftHTailFile() {
		return textFieldAircraftHTailFile;
	}

	public void setTextFieldAircraftHTailFile(TextField textFieldAircraftHTailFile) {
		this.textFieldAircraftHTailFile = textFieldAircraftHTailFile;
	}

	public TextField getTextFieldAircraftHTailX() {
		return textFieldAircraftHTailX;
	}

	public void setTextFieldAircraftHTailX(TextField textFieldAircraftHTailX) {
		this.textFieldAircraftHTailX = textFieldAircraftHTailX;
	}

	public TextField getTextFieldAircraftHTailY() {
		return textFieldAircraftHTailY;
	}

	public void setTextFieldAircraftHTailY(TextField textFieldAircraftHTailY) {
		this.textFieldAircraftHTailY = textFieldAircraftHTailY;
	}

	public TextField getTextFieldAircraftHTailZ() {
		return textFieldAircraftHTailZ;
	}

	public void setTextFieldAircraftHTailZ(TextField textFieldAircraftHTailZ) {
		this.textFieldAircraftHTailZ = textFieldAircraftHTailZ;
	}

	public TextField getTextFieldAircraftHTailRiggingAngle() {
		return textFieldAircraftHTailRiggingAngle;
	}

	public void setTextFieldAircraftHTailRiggingAngle(TextField textFieldAircraftHTailRiggingAngle) {
		this.textFieldAircraftHTailRiggingAngle = textFieldAircraftHTailRiggingAngle;
	}

	public TextField getTextFieldAircraftVTailFile() {
		return textFieldAircraftVTailFile;
	}

	public void setTextFieldAircraftVTailFile(TextField textFieldAircraftVTailFile) {
		this.textFieldAircraftVTailFile = textFieldAircraftVTailFile;
	}

	public TextField getTextFieldAircraftVTailX() {
		return textFieldAircraftVTailX;
	}

	public void setTextFieldAircraftVTailX(TextField textFieldAircraftVTailX) {
		this.textFieldAircraftVTailX = textFieldAircraftVTailX;
	}

	public TextField getTextFieldAircraftVTailY() {
		return textFieldAircraftVTailY;
	}

	public void setTextFieldAircraftVTailY(TextField textFieldAircraftVTailY) {
		this.textFieldAircraftVTailY = textFieldAircraftVTailY;
	}

	public TextField getTextFieldAircraftVTailZ() {
		return textFieldAircraftVTailZ;
	}

	public void setTextFieldAircraftVTailZ(TextField textFieldAircraftVTailZ) {
		this.textFieldAircraftVTailZ = textFieldAircraftVTailZ;
	}

	public TextField getTextFieldAircraftVTailRiggingAngle() {
		return textFieldAircraftVTailRiggingAngle;
	}

	public void setTextFieldAircraftVTailRiggingAngle(TextField textFieldAircraftVTailRiggingAngle) {
		this.textFieldAircraftVTailRiggingAngle = textFieldAircraftVTailRiggingAngle;
	}

	public TextField getTextFieldAircraftCanardFile() {
		return textFieldAircraftCanardFile;
	}

	public void setTextFieldAircraftCanardFile(TextField textFieldAircraftCanardFile) {
		this.textFieldAircraftCanardFile = textFieldAircraftCanardFile;
	}

	public TextField getTextFieldAircraftCanardX() {
		return textFieldAircraftCanardX;
	}

	public void setTextFieldAircraftCanardX(TextField textFieldAircraftCanardX) {
		this.textFieldAircraftCanardX = textFieldAircraftCanardX;
	}

	public TextField getTextFieldAircraftCanardY() {
		return textFieldAircraftCanardY;
	}

	public void setTextFieldAircraftCanardY(TextField textFieldAircraftCanardY) {
		this.textFieldAircraftCanardY = textFieldAircraftCanardY;
	}

	public TextField getTextFieldAircraftCanardZ() {
		return textFieldAircraftCanardZ;
	}

	public void setTextFieldAircraftCanardZ(TextField textFieldAircraftCanardZ) {
		this.textFieldAircraftCanardZ = textFieldAircraftCanardZ;
	}

	public TextField getTextFieldAircraftCanardRiggingAngle() {
		return textFieldAircraftCanardRiggingAngle;
	}

	public void setTextFieldAircraftCanardRiggingAngle(TextField textFieldAircraftCanardRiggingAngle) {
		this.textFieldAircraftCanardRiggingAngle = textFieldAircraftCanardRiggingAngle;
	}

	public TextField getTextFieldAircraftEngineFile1() {
		return textFieldAircraftEngineFile1;
	}

	public void setTextFieldAircraftEngineFile1(TextField textFieldAircraftEngineFile1) {
		this.textFieldAircraftEngineFile1 = textFieldAircraftEngineFile1;
	}

	public TextField getTextFieldAircraftEngineX1() {
		return textFieldAircraftEngineX1;
	}

	public void setTextFieldAircraftEngineX1(TextField textFieldAircraftEngineX1) {
		this.textFieldAircraftEngineX1 = textFieldAircraftEngineX1;
	}

	public TextField getTextFieldAircraftEngineY1() {
		return textFieldAircraftEngineY1;
	}

	public void setTextFieldAircraftEngineY1(TextField textFieldAircraftEngineY1) {
		this.textFieldAircraftEngineY1 = textFieldAircraftEngineY1;
	}

	public TextField getTextFieldAircraftEngineZ1() {
		return textFieldAircraftEngineZ1;
	}

	public void setTextFieldAircraftEngineZ1(TextField textFieldAircraftEngineZ1) {
		this.textFieldAircraftEngineZ1 = textFieldAircraftEngineZ1;
	}

	public TextField getTextFieldAircraftEngineTilt1() {
		return textFieldAircraftEngineTilt1;
	}

	public void setTextFieldAircraftEngineTilt1(TextField textFieldAircraftEngineTilt1) {
		this.textFieldAircraftEngineTilt1 = textFieldAircraftEngineTilt1;
	}

	public TextField getTextFieldAircraftNacelleFile1() {
		return textFieldAircraftNacelleFile1;
	}

	public void setTextFieldAircraftNacelleFile1(TextField textFieldAircraftNacelleFile1) {
		this.textFieldAircraftNacelleFile1 = textFieldAircraftNacelleFile1;
	}

	public TextField getTextFieldAircraftNacelleX1() {
		return textFieldAircraftNacelleX1;
	}

	public void setTextFieldAircraftNacelleX1(TextField textFieldAircraftNacelleX1) {
		this.textFieldAircraftNacelleX1 = textFieldAircraftNacelleX1;
	}

	public TextField getTextFieldAircraftNacelleY1() {
		return textFieldAircraftNacelleY1;
	}

	public void setTextFieldAircraftNacelleY1(TextField textFieldAircraftNacelleY1) {
		this.textFieldAircraftNacelleY1 = textFieldAircraftNacelleY1;
	}

	public TextField getTextFieldAircraftNacelleZ1() {
		return textFieldAircraftNacelleZ1;
	}

	public void setTextFieldAircraftNacelleZ1(TextField textFieldAircraftNacelleZ1) {
		this.textFieldAircraftNacelleZ1 = textFieldAircraftNacelleZ1;
	}

	public TextField getTextFieldAircraftLandingGearsFile() {
		return textFieldAircraftLandingGearsFile;
	}

	public void setTextFieldAircraftLandingGearsFile(TextField textFieldAircraftLandingGearsFile) {
		this.textFieldAircraftLandingGearsFile = textFieldAircraftLandingGearsFile;
	}

	public TextField getTextFieldAircraftNoseLandingGearsX() {
		return textFieldAircraftNoseLandingGearsX;
	}

	public void setTextFieldAircraftNoseLandingGearsX(TextField textFieldAircraftNoseLandingGearsX) {
		this.textFieldAircraftNoseLandingGearsX = textFieldAircraftNoseLandingGearsX;
	}

	public TextField getTextFieldAircraftNoseLandingGearsY() {
		return textFieldAircraftNoseLandingGearsY;
	}

	public void setTextFieldAircraftNoseLandingGearsY(TextField textFieldAircraftNoseLandingGearsY) {
		this.textFieldAircraftNoseLandingGearsY = textFieldAircraftNoseLandingGearsY;
	}

	public TextField getTextFieldAircraftNoseLandingGearsZ() {
		return textFieldAircraftNoseLandingGearsZ;
	}

	public void setTextFieldAircraftNoseLandingGearsZ(TextField textFieldAircraftNoseLandingGearsZ) {
		this.textFieldAircraftNoseLandingGearsZ = textFieldAircraftNoseLandingGearsZ;
	}

	public TextField getTextFieldAircraftMainLandingGearsX() {
		return textFieldAircraftMainLandingGearsX;
	}

	public void setTextFieldAircraftMainLandingGearsX(TextField textFieldAircraftMainLandingGearsX) {
		this.textFieldAircraftMainLandingGearsX = textFieldAircraftMainLandingGearsX;
	}

	public TextField getTextFieldAircraftMainLandingGearsY() {
		return textFieldAircraftMainLandingGearsY;
	}

	public void setTextFieldAircraftMainLandingGearsY(TextField textFieldAircraftMainLandingGearsY) {
		this.textFieldAircraftMainLandingGearsY = textFieldAircraftMainLandingGearsY;
	}

	public TextField getTextFieldAircraftMainLandingGearsZ() {
		return textFieldAircraftMainLandingGearsZ;
	}

	public void setTextFieldAircraftMainLandingGearsZ(TextField textFieldAircraftMainLandingGearsZ) {
		this.textFieldAircraftMainLandingGearsZ = textFieldAircraftMainLandingGearsZ;
	}
	
	public ChoiceBox<String> getFuselageXUnitChoiceBox() {
		return fuselageXUnitChoiceBox;
	}

	public void setFuselageXUnitChoiceBox(ChoiceBox<String> fuselageXUnitChoiceBox) {
		this.fuselageXUnitChoiceBox = fuselageXUnitChoiceBox;
	}

	public ChoiceBox<String> getFuselageYUnitChoiceBox() {
		return fuselageYUnitChoiceBox;
	}

	public void setFuselageYUnitChoiceBox(ChoiceBox<String> fuselageYUnitChoiceBox) {
		this.fuselageYUnitChoiceBox = fuselageYUnitChoiceBox;
	}

	public ChoiceBox<String> getFuselageZUnitChoiceBox() {
		return fuselageZUnitChoiceBox;
	}

	public void setFuselageZUnitChoiceBox(ChoiceBox<String> fuselageZUnitChoiceBox) {
		this.fuselageZUnitChoiceBox = fuselageZUnitChoiceBox;
	}

	public ChoiceBox<String> getWingXUnitChoiceBox() {
		return wingXUnitChoiceBox;
	}

	public void setWingXUnitChoiceBox(ChoiceBox<String> wingXUnitChoiceBox) {
		this.wingXUnitChoiceBox = wingXUnitChoiceBox;
	}

	public ChoiceBox<String> getWingYUnitChoiceBox() {
		return wingYUnitChoiceBox;
	}

	public void setWingYUnitChoiceBox(ChoiceBox<String> wingYUnitChoiceBox) {
		this.wingYUnitChoiceBox = wingYUnitChoiceBox;
	}

	public ChoiceBox<String> getWingZUnitChoiceBox() {
		return wingZUnitChoiceBox;
	}

	public void setWingZUnitChoiceBox(ChoiceBox<String> wingZUnitChoiceBox) {
		this.wingZUnitChoiceBox = wingZUnitChoiceBox;
	}

	public ChoiceBox<String> getWingRiggingAngleUnitChoiceBox() {
		return wingRiggingAngleUnitChoiceBox;
	}

	public void setWingRiggingAngleUnitChoiceBox(ChoiceBox<String> wingRiggingAngleUnitChoiceBox) {
		this.wingRiggingAngleUnitChoiceBox = wingRiggingAngleUnitChoiceBox;
	}

	public ChoiceBox<String> gethTailXUnitChoiceBox() {
		return hTailXUnitChoiceBox;
	}

	public void sethTailXUnitChoiceBox(ChoiceBox<String> hTailXUnitChoiceBox) {
		this.hTailXUnitChoiceBox = hTailXUnitChoiceBox;
	}

	public ChoiceBox<String> gethTailYUnitChoiceBox() {
		return hTailYUnitChoiceBox;
	}

	public void sethTailYUnitChoiceBox(ChoiceBox<String> hTailYUnitChoiceBox) {
		this.hTailYUnitChoiceBox = hTailYUnitChoiceBox;
	}

	public ChoiceBox<String> getHtailZUnitChoiceBox() {
		return htailZUnitChoiceBox;
	}

	public void setHtailZUnitChoiceBox(ChoiceBox<String> htailZUnitChoiceBox) {
		this.htailZUnitChoiceBox = htailZUnitChoiceBox;
	}

	public ChoiceBox<String> gethTailRiggingAngleUnitChoiceBox() {
		return hTailRiggingAngleUnitChoiceBox;
	}

	public void sethTailRiggingAngleUnitChoiceBox(ChoiceBox<String> hTailRiggingAngleUnitChoiceBox) {
		this.hTailRiggingAngleUnitChoiceBox = hTailRiggingAngleUnitChoiceBox;
	}

	public ChoiceBox<String> getvTailXUnitChoiceBox() {
		return vTailXUnitChoiceBox;
	}

	public void setvTailXUnitChoiceBox(ChoiceBox<String> vTailXUnitChoiceBox) {
		this.vTailXUnitChoiceBox = vTailXUnitChoiceBox;
	}

	public ChoiceBox<String> getvTailYUnitChoiceBox() {
		return vTailYUnitChoiceBox;
	}

	public void setvTailYUnitChoiceBox(ChoiceBox<String> vTailYUnitChoiceBox) {
		this.vTailYUnitChoiceBox = vTailYUnitChoiceBox;
	}

	public ChoiceBox<String> getvTailZUnitChoiceBox() {
		return vTailZUnitChoiceBox;
	}

	public void setvTailZUnitChoiceBox(ChoiceBox<String> vTailZUnitChoiceBox) {
		this.vTailZUnitChoiceBox = vTailZUnitChoiceBox;
	}

	public ChoiceBox<String> getvTailRiggingAngleUnitChoiceBox() {
		return vTailRiggingAngleUnitChoiceBox;
	}

	public void setvTailRiggingAngleUnitChoiceBox(ChoiceBox<String> vTailRiggingAngleUnitChoiceBox) {
		this.vTailRiggingAngleUnitChoiceBox = vTailRiggingAngleUnitChoiceBox;
	}

	public ChoiceBox<String> getCanardXUnitChoiceBox() {
		return canardXUnitChoiceBox;
	}

	public void setCanardXUnitChoiceBox(ChoiceBox<String> canardXUnitChoiceBox) {
		this.canardXUnitChoiceBox = canardXUnitChoiceBox;
	}

	public ChoiceBox<String> getCanardYUnitChoiceBox() {
		return canardYUnitChoiceBox;
	}

	public void setCanardYUnitChoiceBox(ChoiceBox<String> canardYUnitChoiceBox) {
		this.canardYUnitChoiceBox = canardYUnitChoiceBox;
	}

	public ChoiceBox<String> getCanardZUnitChoiceBox() {
		return canardZUnitChoiceBox;
	}

	public void setCanardZUnitChoiceBox(ChoiceBox<String> canardZUnitChoiceBox) {
		this.canardZUnitChoiceBox = canardZUnitChoiceBox;
	}

	public ChoiceBox<String> getCanardRiggingAngleUnitChoiceBox() {
		return canardRiggingAngleUnitChoiceBox;
	}

	public void setCanardRiggingAngleUnitChoiceBox(ChoiceBox<String> canardRiggingAngleUnitChoiceBox) {
		this.canardRiggingAngleUnitChoiceBox = canardRiggingAngleUnitChoiceBox;
	}

	public ChoiceBox<String> getEngineX1UnitChoiceBox() {
		return engineX1UnitChoiceBox;
	}

	public void setEngineX1UnitChoiceBox(ChoiceBox<String> engineX1UnitChoiceBox) {
		this.engineX1UnitChoiceBox = engineX1UnitChoiceBox;
	}

	public ChoiceBox<String> getEngineY1UnitChoiceBox() {
		return engineY1UnitChoiceBox;
	}

	public void setEngineY1UnitChoiceBox(ChoiceBox<String> engineY1UnitChoiceBox) {
		this.engineY1UnitChoiceBox = engineY1UnitChoiceBox;
	}

	public ChoiceBox<String> getEngineZ1UnitChoiceBox() {
		return engineZ1UnitChoiceBox;
	}

	public void setEngineZ1UnitChoiceBox(ChoiceBox<String> engineZ1UnitChoiceBox) {
		this.engineZ1UnitChoiceBox = engineZ1UnitChoiceBox;
	}

	public ChoiceBox<String> getEngineTilt1AngleUnitChoiceBox() {
		return engineTilt1AngleUnitChoiceBox;
	}

	public void setEngineTilt1AngleUnitChoiceBox(ChoiceBox<String> engineTilt1AngleUnitChoiceBox) {
		this.engineTilt1AngleUnitChoiceBox = engineTilt1AngleUnitChoiceBox;
	}

	public ChoiceBox<String> getNacelleX1UnitChoiceBox() {
		return nacelleX1UnitChoiceBox;
	}

	public void setNacelleX1UnitChoiceBox(ChoiceBox<String> nacelleX1UnitChoiceBox) {
		this.nacelleX1UnitChoiceBox = nacelleX1UnitChoiceBox;
	}

	public ChoiceBox<String> getNacelleY1UnitChoiceBox() {
		return nacelleY1UnitChoiceBox;
	}

	public void setNacelleY1UnitChoiceBox(ChoiceBox<String> nacelleY1UnitChoiceBox) {
		this.nacelleY1UnitChoiceBox = nacelleY1UnitChoiceBox;
	}

	public ChoiceBox<String> getNacelleZ1UnitChoiceBox() {
		return nacelleZ1UnitChoiceBox;
	}

	public void setNacelleZ1UnitChoiceBox(ChoiceBox<String> nacelleZ1UnitChoiceBox) {
		this.nacelleZ1UnitChoiceBox = nacelleZ1UnitChoiceBox;
	}

	public ChoiceBox<String> getNoseLandingGearsXUnitChoiceBox() {
		return noseLandingGearsXUnitChoiceBox;
	}

	public void setNoseLandingGearsXUnitChoiceBox(ChoiceBox<String> noseLandingGearsXUnitChoiceBox) {
		this.noseLandingGearsXUnitChoiceBox = noseLandingGearsXUnitChoiceBox;
	}

	public ChoiceBox<String> getNoseLandingGearsYUnitChoiceBox() {
		return noseLandingGearsYUnitChoiceBox;
	}

	public void setNoseLandingGearsYUnitChoiceBox(ChoiceBox<String> noseLandingGearsYUnitChoiceBox) {
		this.noseLandingGearsYUnitChoiceBox = noseLandingGearsYUnitChoiceBox;
	}

	public ChoiceBox<String> getNoseLandingGearsZUnitChoiceBox() {
		return noseLandingGearsZUnitChoiceBox;
	}

	public void setNoseLandingGearsZUnitChoiceBox(ChoiceBox<String> noseLandingGearsZUnitChoiceBox) {
		this.noseLandingGearsZUnitChoiceBox = noseLandingGearsZUnitChoiceBox;
	}
	
	public ChoiceBox<String> getMainLandingGearsXUnitChoiceBox() {
		return mainLandingGearsXUnitChoiceBox;
	}

	public void setMainLandingGearsXUnitChoiceBox(ChoiceBox<String> mainLandingGearsXUnitChoiceBox) {
		this.mainLandingGearsXUnitChoiceBox = mainLandingGearsXUnitChoiceBox;
	}

	public ChoiceBox<String> getMainLandingGearsYUnitChoiceBox() {
		return mainLandingGearsYUnitChoiceBox;
	}

	public void setMainLandingGearsYUnitChoiceBox(ChoiceBox<String> mainLandingGearsYUnitChoiceBox) {
		this.mainLandingGearsYUnitChoiceBox = mainLandingGearsYUnitChoiceBox;
	}

	public ChoiceBox<String> getMainLandingGearsZUnitChoiceBox() {
		return mainLandingGearsZUnitChoiceBox;
	}

	public void setMainLandingGearsZUnitChoiceBox(ChoiceBox<String> mainLandingGearsZUnitChoiceBox) {
		this.mainLandingGearsZUnitChoiceBox = mainLandingGearsZUnitChoiceBox;
	}

	public ChoiceBox<String> getWindshieldTypeChoiceBox() {
		return windshieldTypeChoiceBox;
	}

	public void setWindshieldTypeChoiceBox(ChoiceBox<String> windshieldTypeChoiceBox) {
		this.windshieldTypeChoiceBox = windshieldTypeChoiceBox;
	}

	public ChoiceBox<String> getFuselageAdjustCriterionChoiceBox() {
		return fuselageAdjustCriterionChoiceBox;
	}

	public void setFuselageAdjustCriterionChoiceBox(ChoiceBox<String> fuselageAdjustCriterionChoiceBox) {
		this.fuselageAdjustCriterionChoiceBox = fuselageAdjustCriterionChoiceBox;
	}

	public CheckBox getFuselagePressurizedCheckBox() {
		return fuselagePressurizedCheckBox;
	}

	public void setFuselagePressurizedCheckBox(CheckBox fuselagePressurizedCheckBox) {
		this.fuselagePressurizedCheckBox = fuselagePressurizedCheckBox;
	}

	public TextField getTextFieldFuselageDeckNumber() {
		return textFieldFuselageDeckNumber;
	}

	public void setTextFieldFuselageDeckNumber(TextField textFieldFuselageDeckNumber) {
		this.textFieldFuselageDeckNumber = textFieldFuselageDeckNumber;
	}

	public TextField getTextFieldFuselageLength() {
		return textFieldFuselageLength;
	}

	public void setTextFieldFuselageLength(TextField textFieldFuselageLength) {
		this.textFieldFuselageLength = textFieldFuselageLength;
	}

	public TextField getTextFieldFuselageSurfaceRoughness() {
		return textFieldFuselageSurfaceRoughness;
	}

	public void setTextFieldFuselageSurfaceRoughness(TextField textFieldFuselageSurfaceRoughness) {
		this.textFieldFuselageSurfaceRoughness = textFieldFuselageSurfaceRoughness;
	}

	public TextField getTextFieldFuselageNoseLengthRatio() {
		return textFieldFuselageNoseLengthRatio;
	}

	public void setTextFieldFuselageNoseLengthRatio(TextField textFieldFuselageNoseLengthRatio) {
		this.textFieldFuselageNoseLengthRatio = textFieldFuselageNoseLengthRatio;
	}

	public TextField getTextFieldFuselageNoseTipOffset() {
		return textFieldFuselageNoseTipOffset;
	}

	public void setTextFieldFuselageNoseTipOffset(TextField textFieldFuselageNoseTipOffset) {
		this.textFieldFuselageNoseTipOffset = textFieldFuselageNoseTipOffset;
	}

	public TextField getTextFieldFuselageNoseDxCap() {
		return textFieldFuselageNoseDxCap;
	}

	public void setTextFieldFuselageNoseDxCap(TextField textFieldFuselageNoseDxCap) {
		this.textFieldFuselageNoseDxCap = textFieldFuselageNoseDxCap;
	}

	public TextField getTextFieldFuselageNoseWindshieldWidth() {
		return textFieldFuselageNoseWindshieldWidth;
	}

	public void setTextFieldFuselageNoseWindshieldWidth(TextField textFieldFuselageNoseWindshieldWidth) {
		this.textFieldFuselageNoseWindshieldWidth = textFieldFuselageNoseWindshieldWidth;
	}

	public TextField getTextFieldFuselageNoseWindshieldHeight() {
		return textFieldFuselageNoseWindshieldHeight;
	}

	public void setTextFieldFuselageNoseWindshieldHeight(TextField textFieldFuselageNoseWindshieldHeight) {
		this.textFieldFuselageNoseWindshieldHeight = textFieldFuselageNoseWindshieldHeight;
	}

	public TextField getTextFieldFuselageNoseMidSectionHeight() {
		return textFieldFuselageNoseMidSectionHeight;
	}

	public void setTextFieldFuselageNoseMidSectionHeight(TextField textFieldFuselageNoseMidSectionHeight) {
		this.textFieldFuselageNoseMidSectionHeight = textFieldFuselageNoseMidSectionHeight;
	}

	public TextField getTextFieldFuselageNoseMidSectionRhoUpper() {
		return textFieldFuselageNoseMidSectionRhoUpper;
	}

	public void setTextFieldFuselageNoseMidSectionRhoUpper(TextField textFieldFuselageNoseMidSectionRhoUpper) {
		this.textFieldFuselageNoseMidSectionRhoUpper = textFieldFuselageNoseMidSectionRhoUpper;
	}

	public TextField getTextFieldFuselageNoseMidSectionRhoLower() {
		return textFieldFuselageNoseMidSectionRhoLower;
	}

	public void setTextFieldFuselageNoseMidSectionRhoLower(TextField textFieldFuselageNoseMidSectionRhoLower) {
		this.textFieldFuselageNoseMidSectionRhoLower = textFieldFuselageNoseMidSectionRhoLower;
	}

	public TextField getTextFieldFuselageCylinderLengthRatio() {
		return textFieldFuselageCylinderLengthRatio;
	}

	public void setTextFieldFuselageCylinderLengthRatio(TextField textFieldFuselageCylinderLengthRatio) {
		this.textFieldFuselageCylinderLengthRatio = textFieldFuselageCylinderLengthRatio;
	}

	public TextField getTextFieldFuselageCylinderSectionWidth() {
		return textFieldFuselageCylinderSectionWidth;
	}

	public void setTextFieldFuselageCylinderSectionWidth(TextField textFieldFuselageCylinderSectionWidth) {
		this.textFieldFuselageCylinderSectionWidth = textFieldFuselageCylinderSectionWidth;
	}

	public TextField getTextFieldFuselageCylinderSectionHeight() {
		return textFieldFuselageCylinderSectionHeight;
	}

	public void setTextFieldFuselageCylinderSectionHeight(TextField textFieldFuselageCylinderSectionHeight) {
		this.textFieldFuselageCylinderSectionHeight = textFieldFuselageCylinderSectionHeight;
	}

	public TextField getTextFieldFuselageCylinderHeightFromGround() {
		return textFieldFuselageCylinderHeightFromGround;
	}

	public void setTextFieldFuselageCylinderHeightFromGround(TextField textFieldFuselageCylinderHeightFromGround) {
		this.textFieldFuselageCylinderHeightFromGround = textFieldFuselageCylinderHeightFromGround;
	}

	public TextField getTextFieldFuselageCylinderSectionHeightRatio() {
		return textFieldFuselageCylinderSectionHeightRatio;
	}

	public void setTextFieldFuselageCylinderSectionHeightRatio(TextField textFieldFuselageCylinderSectionHeightRatio) {
		this.textFieldFuselageCylinderSectionHeightRatio = textFieldFuselageCylinderSectionHeightRatio;
	}

	public TextField getTextFieldFuselageCylinderSectionRhoUpper() {
		return textFieldFuselageCylinderSectionRhoUpper;
	}

	public void setTextFieldFuselageCylinderSectionRhoUpper(TextField textFieldFuselageCylinderSectionRhoUpper) {
		this.textFieldFuselageCylinderSectionRhoUpper = textFieldFuselageCylinderSectionRhoUpper;
	}

	public TextField getTextFieldFuselageCylinderSectionRhoLower() {
		return textFieldFuselageCylinderSectionRhoLower;
	}

	public void setTextFieldFuselageCylinderSectionRhoLower(TextField textFieldFuselageCylinderSectionRhoLower) {
		this.textFieldFuselageCylinderSectionRhoLower = textFieldFuselageCylinderSectionRhoLower;
	}

	public TextField getTextFieldFuselageTailTipOffset() {
		return textFieldFuselageTailTipOffset;
	}

	public void setTextFieldFuselageTailTipOffset(TextField textFieldFuselageTailTipOffset) {
		this.textFieldFuselageTailTipOffset = textFieldFuselageTailTipOffset;
	}

	public TextField getTextFieldFuselageTailDxCap() {
		return textFieldFuselageTailDxCap;
	}

	public void setTextFieldFuselageTailDxCap(TextField textFieldFuselageTailDxCap) {
		this.textFieldFuselageTailDxCap = textFieldFuselageTailDxCap;
	}

	public TextField getTextFieldFuselageTailMidSectionHeight() {
		return textFieldFuselageTailMidSectionHeight;
	}

	public void setTextFieldFuselageTailMidSectionHeight(TextField textFieldFuselageTailMidSectionHeight) {
		this.textFieldFuselageTailMidSectionHeight = textFieldFuselageTailMidSectionHeight;
	}

	public TextField getTextFieldFuselageTailMidRhoUpper() {
		return textFieldFuselageTailMidRhoUpper;
	}

	public void setTextFieldFuselageTailMidRhoUpper(TextField textFieldFuselageTailMidRhoUpper) {
		this.textFieldFuselageTailMidRhoUpper = textFieldFuselageTailMidRhoUpper;
	}

	public TextField getTextFieldFuselageTailMidRhoLower() {
		return textFieldFuselageTailMidRhoLower;
	}

	public void setTextFieldFuselageTailMidRhoLower(TextField textFieldFuselageTailMidRhoLower) {
		this.textFieldFuselageTailMidRhoLower = textFieldFuselageTailMidRhoLower;
	}

	public TextField getTextFieldFuselageSpoilerXin1() {
		return textFieldFuselageSpoilerXin1;
	}

	public void setTextFieldFuselageSpoilerXin1(TextField textFieldFuselageSpoilerXin1) {
		this.textFieldFuselageSpoilerXin1 = textFieldFuselageSpoilerXin1;
	}

	public TextField getTextFieldFuselageSpoilerXout1() {
		return textFieldFuselageSpoilerXout1;
	}

	public void setTextFieldFuselageSpoilerXout1(TextField textFieldFuselageSpoilerXout1) {
		this.textFieldFuselageSpoilerXout1 = textFieldFuselageSpoilerXout1;
	}

	public TextField getTextFieldFuselageSpoilerYin1() {
		return textFieldFuselageSpoilerYin1;
	}

	public void setTextFieldFuselageSpoilerYin1(TextField textFieldFuselageSpoilerYin1) {
		this.textFieldFuselageSpoilerYin1 = textFieldFuselageSpoilerYin1;
	}

	public TextField getTextFieldFuselageSpoilerYout1() {
		return textFieldFuselageSpoilerYout1;
	}

	public void setTextFieldFuselageSpoilerYout1(TextField textFieldFuselageSpoilerYout1) {
		this.textFieldFuselageSpoilerYout1 = textFieldFuselageSpoilerYout1;
	}

	public TextField getTextFieldFuselageSpoilerMinDeflection1() {
		return textFieldFuselageSpoilerMinDeflection1;
	}

	public void setTextFieldFuselageSpoilerMinDeflection1(TextField textFieldFuselageSpoilerMinDeflection1) {
		this.textFieldFuselageSpoilerMinDeflection1 = textFieldFuselageSpoilerMinDeflection1;
	}

	public TextField getTextFieldFuselageSpoilerMaxDeflection1() {
		return textFieldFuselageSpoilerMaxDeflection1;
	}

	public void setTextFieldFuselageSpoilerMaxDeflection1(TextField textFieldFuselageSpoilerMaxDeflection1) {
		this.textFieldFuselageSpoilerMaxDeflection1 = textFieldFuselageSpoilerMaxDeflection1;
	}

	public List<TextField> getTextFieldFuselageInnerSpanwisePositionSpoilerList() {
		return textFieldFuselageInnerSpanwisePositionSpoilerList;
	}

	public void setTextFieldFuselageInnerSpanwisePositionSpoilerList(
			List<TextField> textFieldFuselageInnerSpanwisePositionSpoilerList) {
		this.textFieldFuselageInnerSpanwisePositionSpoilerList = textFieldFuselageInnerSpanwisePositionSpoilerList;
	}

	public List<TextField> getTextFieldFuselageOuterSpanwisePositionSpoilerList() {
		return textFieldFuselageOuterSpanwisePositionSpoilerList;
	}

	public void setTextFieldFuselageOuterSpanwisePositionSpoilerList(
			List<TextField> textFieldFuselageOuterSpanwisePositionSpoilerList) {
		this.textFieldFuselageOuterSpanwisePositionSpoilerList = textFieldFuselageOuterSpanwisePositionSpoilerList;
	}

	public List<TextField> getTextFieldFuselageInnerChordwisePositionSpoilerList() {
		return textFieldFuselageInnerChordwisePositionSpoilerList;
	}

	public void setTextFieldFuselageInnerChordwisePositionSpoilerList(
			List<TextField> textFieldFuselageInnerChordwisePositionSpoilerList) {
		this.textFieldFuselageInnerChordwisePositionSpoilerList = textFieldFuselageInnerChordwisePositionSpoilerList;
	}

	public List<TextField> getTextFieldFuselageOuterChordwisePositionSpoilerList() {
		return textFieldFuselageOuterChordwisePositionSpoilerList;
	}

	public void setTextFieldFuselageOuterChordwisePositionSpoilerList(
			List<TextField> textFieldFuselageOuterChordwisePositionSpoilerList) {
		this.textFieldFuselageOuterChordwisePositionSpoilerList = textFieldFuselageOuterChordwisePositionSpoilerList;
	}

	public List<TextField> getTextFieldFuselageMinimumDeflectionAngleSpoilerList() {
		return textFieldFuselageMinimumDeflectionAngleSpoilerList;
	}

	public void setTextFieldFuselageMinimumDeflectionAngleSpoilerList(
			List<TextField> textFieldFuselageMinimumDeflectionAngleSpoilerList) {
		this.textFieldFuselageMinimumDeflectionAngleSpoilerList = textFieldFuselageMinimumDeflectionAngleSpoilerList;
	}

	public List<TextField> getTextFieldFuselageMaximumDeflectionAngleSpoilerList() {
		return textFieldFuselageMaximumDeflectionAngleSpoilerList;
	}

	public void setTextFieldFuselageMaximumDeflectionAngleSpoilerList(
			List<TextField> textFieldFuselageMaximumDeflectionAngleSpoilerList) {
		this.textFieldFuselageMaximumDeflectionAngleSpoilerList = textFieldFuselageMaximumDeflectionAngleSpoilerList;
	}

	public List<ChoiceBox<String>> getChoiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList() {
		return choiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList;
	}

	public void setChoiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList(
			List<ChoiceBox<String>> choiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList) {
		this.choiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList = choiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList() {
		return choiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList;
	}

	public void setChoiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList(
			List<ChoiceBox<String>> choiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList) {
		this.choiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList = choiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList;
	}

	public ChoiceBox<String> getFuselageLengthUnitChoiceBox() {
		return fuselageLengthUnitChoiceBox;
	}

	public void setFuselageLengthUnitChoiceBox(ChoiceBox<String> fuselageLengthUnitChoiceBox) {
		this.fuselageLengthUnitChoiceBox = fuselageLengthUnitChoiceBox;
	}

	public ChoiceBox<String> getFuselageRoughnessUnitChoiceBox() {
		return fuselageRoughnessUnitChoiceBox;
	}

	public void setFuselageRoughnessUnitChoiceBox(ChoiceBox<String> fuselageRoughnessUnitChoiceBox) {
		this.fuselageRoughnessUnitChoiceBox = fuselageRoughnessUnitChoiceBox;
	}

	public ChoiceBox<String> getFuselageNoseTipOffsetZUnitChoiceBox() {
		return fuselageNoseTipOffsetZUnitChoiceBox;
	}

	public void setFuselageNoseTipOffsetZUnitChoiceBox(ChoiceBox<String> fuselageNoseTipOffsetZUnitChoiceBox) {
		this.fuselageNoseTipOffsetZUnitChoiceBox = fuselageNoseTipOffsetZUnitChoiceBox;
	}

	public ChoiceBox<String> getFuselageWindshieldWidthUnitChoiceBox() {
		return fuselageWindshieldWidthUnitChoiceBox;
	}

	public void setFuselageWindshieldWidthUnitChoiceBox(ChoiceBox<String> fuselageWindshieldWidthUnitChoiceBox) {
		this.fuselageWindshieldWidthUnitChoiceBox = fuselageWindshieldWidthUnitChoiceBox;
	}

	public ChoiceBox<String> getFuselageWindshieldHeightUnitChoiceBox() {
		return fuselageWindshieldHeightUnitChoiceBox;
	}

	public void setFuselageWindshieldHeightUnitChoiceBox(ChoiceBox<String> fuselageWindshieldHeightUnitChoiceBox) {
		this.fuselageWindshieldHeightUnitChoiceBox = fuselageWindshieldHeightUnitChoiceBox;
	}

	public ChoiceBox<String> getFuselageCylinderSectionWidthUnitChoiceBox() {
		return fuselageCylinderSectionWidthUnitChoiceBox;
	}

	public void setFuselageCylinderSectionWidthUnitChoiceBox(ChoiceBox<String> fuselageCylinderSectionWidthUnitChoiceBox) {
		this.fuselageCylinderSectionWidthUnitChoiceBox = fuselageCylinderSectionWidthUnitChoiceBox;
	}

	public ChoiceBox<String> getFuselageCylinderSectionHeightUnitChoiceBox() {
		return fuselageCylinderSectionHeightUnitChoiceBox;
	}

	public void setFuselageCylinderSectionHeightUnitChoiceBox(
			ChoiceBox<String> fuselageCylinderSectionHeightUnitChoiceBox) {
		this.fuselageCylinderSectionHeightUnitChoiceBox = fuselageCylinderSectionHeightUnitChoiceBox;
	}

	public ChoiceBox<String> getFuselageHeightFromGroundUnitChoiceBox() {
		return fuselageHeightFromGroundUnitChoiceBox;
	}

	public void setFuselageHeightFromGroundUnitChoiceBox(ChoiceBox<String> fuselageHeightFromGroundUnitChoiceBox) {
		this.fuselageHeightFromGroundUnitChoiceBox = fuselageHeightFromGroundUnitChoiceBox;
	}

	public ChoiceBox<String> getFuselageTailTipOffsetZUnitChoiceBox() {
		return fuselageTailTipOffsetZUnitChoiceBox;
	}

	public void setFuselageTailTipOffsetZUnitChoiceBox(ChoiceBox<String> fuselageTailTipOffsetZUnitChoiceBox) {
		this.fuselageTailTipOffsetZUnitChoiceBox = fuselageTailTipOffsetZUnitChoiceBox;
	}

	public ChoiceBox<String> getFuselageSpoiler1DeltaMinUnitChoiceBox() {
		return fuselageSpoiler1DeltaMinUnitChoiceBox;
	}

	public void setFuselageSpoiler1DeltaMinUnitChoiceBox(ChoiceBox<String> fuselageSpoiler1DeltaMinUnitChoiceBox) {
		this.fuselageSpoiler1DeltaMinUnitChoiceBox = fuselageSpoiler1DeltaMinUnitChoiceBox;
	}

	public ChoiceBox<String> getFuselageSpoiler1DeltaMaxUnitChoiceBox() {
		return fuselageSpoiler1DeltaMaxUnitChoiceBox;
	}

	public void setFuselageSpoiler1DeltaMaxUnitChoiceBox(ChoiceBox<String> fuselageSpoiler1DeltaMaxUnitChoiceBox) {
		this.fuselageSpoiler1DeltaMaxUnitChoiceBox = fuselageSpoiler1DeltaMaxUnitChoiceBox;
	}

	public TextField getTextFieldActualPassengersNumber() {
		return textFieldActualPassengersNumber;
	}

	public void setTextFieldActualPassengersNumber(TextField textFieldActualPassengersNumber) {
		this.textFieldActualPassengersNumber = textFieldActualPassengersNumber;
	}

	public TextField getTextFieldMaximumPassengersNumber() {
		return textFieldMaximumPassengersNumber;
	}

	public void setTextFieldMaximumPassengersNumber(TextField textFieldMaximumPassengersNumber) {
		this.textFieldMaximumPassengersNumber = textFieldMaximumPassengersNumber;
	}

	public TextField getTextFieldFlightCrewNumber() {
		return textFieldFlightCrewNumber;
	}

	public void setTextFieldFlightCrewNumber(TextField textFieldFlightCrewNumber) {
		this.textFieldFlightCrewNumber = textFieldFlightCrewNumber;
	}

	public TextField getTextFieldClassesNumber() {
		return textFieldClassesNumber;
	}

	public void setTextFieldClassesNumber(TextField textFieldClassesNumber) {
		this.textFieldClassesNumber = textFieldClassesNumber;
	}

	public ChoiceBox<String> getCabinConfigurationClassesTypeChoiceBox1() {
		return cabinConfigurationClassesTypeChoiceBox1;
	}

	public void setCabinConfigurationClassesTypeChoiceBox1(ChoiceBox<String> cabinConfigurationClassesTypeChoiceBox1) {
		this.cabinConfigurationClassesTypeChoiceBox1 = cabinConfigurationClassesTypeChoiceBox1;
	}

	public ChoiceBox<String> getCabinConfigurationClassesTypeChoiceBox2() {
		return cabinConfigurationClassesTypeChoiceBox2;
	}

	public void setCabinConfigurationClassesTypeChoiceBox2(ChoiceBox<String> cabinConfigurationClassesTypeChoiceBox2) {
		this.cabinConfigurationClassesTypeChoiceBox2 = cabinConfigurationClassesTypeChoiceBox2;
	}

	public ChoiceBox<String> getCabinConfigurationClassesTypeChoiceBox3() {
		return cabinConfigurationClassesTypeChoiceBox3;
	}

	public void setCabinConfigurationClassesTypeChoiceBox3(ChoiceBox<String> cabinConfigurationClassesTypeChoiceBox3) {
		this.cabinConfigurationClassesTypeChoiceBox3 = cabinConfigurationClassesTypeChoiceBox3;
	}

	public TextField getTextFieldAislesNumber() {
		return textFieldAislesNumber;
	}

	public void setTextFieldAislesNumber(TextField textFieldAislesNumber) {
		this.textFieldAislesNumber = textFieldAislesNumber;
	}

	public TextField getTextFieldXCoordinateFirstRow() {
		return textFieldXCoordinateFirstRow;
	}

	public void setTextFieldXCoordinateFirstRow(TextField textFieldXCoordinateFirstRow) {
		this.textFieldXCoordinateFirstRow = textFieldXCoordinateFirstRow;
	}

	public TextField getTextFieldNumberOfBrakesEconomy() {
		return textFieldNumberOfBrakesEconomy;
	}

	public void setTextFieldNumberOfBrakesEconomy(TextField textFieldNumberOfBrakesEconomy) {
		this.textFieldNumberOfBrakesEconomy = textFieldNumberOfBrakesEconomy;
	}

	public TextField getTextFieldNumberOfBrakesBusiness() {
		return textFieldNumberOfBrakesBusiness;
	}

	public void setTextFieldNumberOfBrakesBusiness(TextField textFieldNumberOfBrakesBusiness) {
		this.textFieldNumberOfBrakesBusiness = textFieldNumberOfBrakesBusiness;
	}

	public TextField getTextFieldNumberOfBrakesFirst() {
		return textFieldNumberOfBrakesFirst;
	}

	public void setTextFieldNumberOfBrakesFirst(TextField textFieldNumberOfBrakesFirst) {
		this.textFieldNumberOfBrakesFirst = textFieldNumberOfBrakesFirst;
	}

	public TextField getTextFieldNumberOfRowsEconomy() {
		return textFieldNumberOfRowsEconomy;
	}

	public void setTextFieldNumberOfRowsEconomy(TextField textFieldNumberOfRowsEconomy) {
		this.textFieldNumberOfRowsEconomy = textFieldNumberOfRowsEconomy;
	}

	public TextField getTextFieldNumberOfRowsBusiness() {
		return textFieldNumberOfRowsBusiness;
	}

	public void setTextFieldNumberOfRowsBusiness(TextField textFieldNumberOfRowsBusiness) {
		this.textFieldNumberOfRowsBusiness = textFieldNumberOfRowsBusiness;
	}

	public TextField getTextFieldNumberOfRowsFirst() {
		return textFieldNumberOfRowsFirst;
	}

	public void setTextFieldNumberOfRowsFirst(TextField textFieldNumberOfRowsFirst) {
		this.textFieldNumberOfRowsFirst = textFieldNumberOfRowsFirst;
	}

	public TextField getTextFieldNumberOfColumnsEconomy() {
		return textFieldNumberOfColumnsEconomy;
	}

	public void setTextFieldNumberOfColumnsEconomy(TextField textFieldNumberOfColumnsEconomy) {
		this.textFieldNumberOfColumnsEconomy = textFieldNumberOfColumnsEconomy;
	}

	public TextField getTextFieldNumberOfColumnsBusiness() {
		return textFieldNumberOfColumnsBusiness;
	}

	public void setTextFieldNumberOfColumnsBusiness(TextField textFieldNumberOfColumnsBusiness) {
		this.textFieldNumberOfColumnsBusiness = textFieldNumberOfColumnsBusiness;
	}

	public TextField getTextFieldNumberOfColumnsFirst() {
		return textFieldNumberOfColumnsFirst;
	}

	public void setTextFieldNumberOfColumnsFirst(TextField textFieldNumberOfColumnsFirst) {
		this.textFieldNumberOfColumnsFirst = textFieldNumberOfColumnsFirst;
	}

	public TextField getTextFieldSeatsPitchEconomy() {
		return textFieldSeatsPitchEconomy;
	}

	public void setTextFieldSeatsPitchEconomy(TextField textFieldSeatsPitchEconomy) {
		this.textFieldSeatsPitchEconomy = textFieldSeatsPitchEconomy;
	}

	public TextField getTextFieldSeatsPitchBusiness() {
		return textFieldSeatsPitchBusiness;
	}

	public void setTextFieldSeatsPitchBusiness(TextField textFieldSeatsPitchBusiness) {
		this.textFieldSeatsPitchBusiness = textFieldSeatsPitchBusiness;
	}

	public TextField getTextFieldSeatsPitchFirst() {
		return textFieldSeatsPitchFirst;
	}

	public void setTextFieldSeatsPitchFirst(TextField textFieldSeatsPitchFirst) {
		this.textFieldSeatsPitchFirst = textFieldSeatsPitchFirst;
	}

	public TextField getTextFieldSeatsWidthEconomy() {
		return textFieldSeatsWidthEconomy;
	}

	public void setTextFieldSeatsWidthEconomy(TextField textFieldSeatsWidthEconomy) {
		this.textFieldSeatsWidthEconomy = textFieldSeatsWidthEconomy;
	}

	public TextField getTextFieldSeatsWidthBusiness() {
		return textFieldSeatsWidthBusiness;
	}

	public void setTextFieldSeatsWidthBusiness(TextField textFieldSeatsWidthBusiness) {
		this.textFieldSeatsWidthBusiness = textFieldSeatsWidthBusiness;
	}

	public TextField getTextFieldSeatsWidthFirst() {
		return textFieldSeatsWidthFirst;
	}

	public void setTextFieldSeatsWidthFirst(TextField textFieldSeatsWidthFirst) {
		this.textFieldSeatsWidthFirst = textFieldSeatsWidthFirst;
	}

	public TextField getTextFieldDistanceFromWallEconomy() {
		return textFieldDistanceFromWallEconomy;
	}

	public void setTextFieldDistanceFromWallEconomy(TextField textFieldDistanceFromWallEconomy) {
		this.textFieldDistanceFromWallEconomy = textFieldDistanceFromWallEconomy;
	}

	public TextField getTextFieldDistanceFromWallBusiness() {
		return textFieldDistanceFromWallBusiness;
	}

	public void setTextFieldDistanceFromWallBusiness(TextField textFieldDistanceFromWallBusiness) {
		this.textFieldDistanceFromWallBusiness = textFieldDistanceFromWallBusiness;
	}

	public TextField getTextFieldDistanceFromWallFirst() {
		return textFieldDistanceFromWallFirst;
	}

	public void setTextFieldDistanceFromWallFirst(TextField textFieldDistanceFromWallFirst) {
		this.textFieldDistanceFromWallFirst = textFieldDistanceFromWallFirst;
	}

	public ChoiceBox<String> getCabinConfigurationXCoordinateFirstRowUnitChoiceBox() {
		return cabinConfigurationXCoordinateFirstRowUnitChoiceBox;
	}

	public void setCabinConfigurationXCoordinateFirstRowUnitChoiceBox(
			ChoiceBox<String> cabinConfigurationXCoordinateFirstRowUnitChoiceBox) {
		this.cabinConfigurationXCoordinateFirstRowUnitChoiceBox = cabinConfigurationXCoordinateFirstRowUnitChoiceBox;
	}

	public ChoiceBox<String> getCabinConfigurationSeatsPitchEconomyUnitChoiceBox() {
		return cabinConfigurationSeatsPitchEconomyUnitChoiceBox;
	}

	public void setCabinConfigurationSeatsPitchEconomyUnitChoiceBox(
			ChoiceBox<String> cabinConfigurationSeatsPitchEconomyUnitChoiceBox) {
		this.cabinConfigurationSeatsPitchEconomyUnitChoiceBox = cabinConfigurationSeatsPitchEconomyUnitChoiceBox;
	}

	public ChoiceBox<String> getCabinConfigurationSeatsPitchBusinessUnitChoiceBox() {
		return cabinConfigurationSeatsPitchBusinessUnitChoiceBox;
	}

	public void setCabinConfigurationSeatsPitchBusinessUnitChoiceBox(
			ChoiceBox<String> cabinConfigurationSeatsPitchBusinessUnitChoiceBox) {
		this.cabinConfigurationSeatsPitchBusinessUnitChoiceBox = cabinConfigurationSeatsPitchBusinessUnitChoiceBox;
	}

	public ChoiceBox<String> getCabinConfigurationSeatsPitchFirstUnitChoiceBox() {
		return cabinConfigurationSeatsPitchFirstUnitChoiceBox;
	}

	public void setCabinConfigurationSeatsPitchFirstUnitChoiceBox(
			ChoiceBox<String> cabinConfigurationSeatsPitchFirstUnitChoiceBox) {
		this.cabinConfigurationSeatsPitchFirstUnitChoiceBox = cabinConfigurationSeatsPitchFirstUnitChoiceBox;
	}

	public ChoiceBox<String> getCabinConfigurationSeatsWidthEconomyUnitChoiceBox() {
		return cabinConfigurationSeatsWidthEconomyUnitChoiceBox;
	}

	public void setCabinConfigurationSeatsWidthEconomyUnitChoiceBox(
			ChoiceBox<String> cabinConfigurationSeatsWidthEconomyUnitChoiceBox) {
		this.cabinConfigurationSeatsWidthEconomyUnitChoiceBox = cabinConfigurationSeatsWidthEconomyUnitChoiceBox;
	}

	public ChoiceBox<String> getCabinConfigurationSeatsWidthBusinessUnitChoiceBox() {
		return cabinConfigurationSeatsWidthBusinessUnitChoiceBox;
	}

	public void setCabinConfigurationSeatsWidthBusinessUnitChoiceBox(
			ChoiceBox<String> cabinConfigurationSeatsWidthBusinessUnitChoiceBox) {
		this.cabinConfigurationSeatsWidthBusinessUnitChoiceBox = cabinConfigurationSeatsWidthBusinessUnitChoiceBox;
	}

	public ChoiceBox<String> getCabinConfigurationSeatsWidthFirstUnitChoiceBox() {
		return cabinConfigurationSeatsWidthFirstUnitChoiceBox;
	}

	public void setCabinConfigurationSeatsWidthFirstUnitChoiceBox(
			ChoiceBox<String> cabinConfigurationSeatsWidthFirstUnitChoiceBox) {
		this.cabinConfigurationSeatsWidthFirstUnitChoiceBox = cabinConfigurationSeatsWidthFirstUnitChoiceBox;
	}

	public ChoiceBox<String> getCabinConfigurationDistanceFromWallEconomyUnitChoiceBox() {
		return cabinConfigurationDistanceFromWallEconomyUnitChoiceBox;
	}

	public void setCabinConfigurationDistanceFromWallEconomyUnitChoiceBox(
			ChoiceBox<String> cabinConfigurationDistanceFromWallEconomyUnitChoiceBox) {
		this.cabinConfigurationDistanceFromWallEconomyUnitChoiceBox = cabinConfigurationDistanceFromWallEconomyUnitChoiceBox;
	}

	public ChoiceBox<String> getCabinConfigurationDistanceFromWallBusinessUnitChoiceBox() {
		return cabinConfigurationDistanceFromWallBusinessUnitChoiceBox;
	}

	public void setCabinConfigurationDistanceFromWallBusinessUnitChoiceBox(
			ChoiceBox<String> cabinConfigurationDistanceFromWallBusinessUnitChoiceBox) {
		this.cabinConfigurationDistanceFromWallBusinessUnitChoiceBox = cabinConfigurationDistanceFromWallBusinessUnitChoiceBox;
	}

	public ChoiceBox<String> getCabinConfigurationDistanceFromWallFirstUnitChoiceBox() {
		return cabinConfigurationDistanceFromWallFirstUnitChoiceBox;
	}

	public void setCabinConfigurationDistanceFromWallFirstUnitChoiceBox(
			ChoiceBox<String> cabinConfigurationDistanceFromWallFirstUnitChoiceBox) {
		this.cabinConfigurationDistanceFromWallFirstUnitChoiceBox = cabinConfigurationDistanceFromWallFirstUnitChoiceBox;
	}

	public ChoiceBox<String> getWingAdjustCriterionChoiceBox() {
		return wingAdjustCriterionChoiceBox;
	}

	public void setWingAdjustCriterionChoiceBox(ChoiceBox<String> wingAdjustCriterionChoiceBox) {
		this.wingAdjustCriterionChoiceBox = wingAdjustCriterionChoiceBox;
	}

	public CheckBox getEquivalentWingCheckBox() {
		return equivalentWingCheckBox;
	}

	public void setEquivalentWingCheckBox(CheckBox equivalentWingCheckBox) {
		this.equivalentWingCheckBox = equivalentWingCheckBox;
	}

	public TextField getTextFieldWingMainSparAdimensionalPosition() {
		return textFieldWingMainSparAdimensionalPosition;
	}

	public void setTextFieldWingMainSparAdimensionalPosition(TextField textFieldWingMainSparAdimensionalPosition) {
		this.textFieldWingMainSparAdimensionalPosition = textFieldWingMainSparAdimensionalPosition;
	}

	public TextField getTextFieldWingSecondarySparAdimensionalPosition() {
		return textFieldWingSecondarySparAdimensionalPosition;
	}

	public void setTextFieldWingSecondarySparAdimensionalPosition(
			TextField textFieldWingSecondarySparAdimensionalPosition) {
		this.textFieldWingSecondarySparAdimensionalPosition = textFieldWingSecondarySparAdimensionalPosition;
	}

	public TextField getTextFieldWingRoughness() {
		return textFieldWingRoughness;
	}

	public void setTextFieldWingRoughness(TextField textFieldWingRoughness) {
		this.textFieldWingRoughness = textFieldWingRoughness;
	}

	public TextField getTextFieldWingWingletHeight() {
		return textFieldWingWingletHeight;
	}

	public void setTextFieldWingWingletHeight(TextField textFieldWingWingletHeight) {
		this.textFieldWingWingletHeight = textFieldWingWingletHeight;
	}

	public TextField getTextFieldEquivalentWingArea() {
		return textFieldEquivalentWingArea;
	}

	public void setTextFieldEquivalentWingArea(TextField textFieldEquivalentWingArea) {
		this.textFieldEquivalentWingArea = textFieldEquivalentWingArea;
	}

	public TextField getTextFieldEquivalentWingAspectRatio() {
		return textFieldEquivalentWingAspectRatio;
	}

	public void setTextFieldEquivalentWingAspectRatio(TextField textFieldEquivalentWingAspectRatio) {
		this.textFieldEquivalentWingAspectRatio = textFieldEquivalentWingAspectRatio;
	}

	public TextField getTextFieldEquivalentWingKinkPosition() {
		return textFieldEquivalentWingKinkPosition;
	}

	public void setTextFieldEquivalentWingKinkPosition(TextField textFieldEquivalentWingKinkPosition) {
		this.textFieldEquivalentWingKinkPosition = textFieldEquivalentWingKinkPosition;
	}

	public TextField getTextFieldEquivalentWingSweepLeadingEdge() {
		return textFieldEquivalentWingSweepLeadingEdge;
	}

	public void setTextFieldEquivalentWingSweepLeadingEdge(TextField textFieldEquivalentWingSweepLeadingEdge) {
		this.textFieldEquivalentWingSweepLeadingEdge = textFieldEquivalentWingSweepLeadingEdge;
	}

	public TextField getTextFieldEquivalentWingTwistAtTip() {
		return textFieldEquivalentWingTwistAtTip;
	}

	public void setTextFieldEquivalentWingTwistAtTip(TextField textFieldEquivalentWingTwistAtTip) {
		this.textFieldEquivalentWingTwistAtTip = textFieldEquivalentWingTwistAtTip;
	}

	public TextField getTextFieldEquivalentWingDihedral() {
		return textFieldEquivalentWingDihedral;
	}

	public void setTextFieldEquivalentWingDihedral(TextField textFieldEquivalentWingDihedral) {
		this.textFieldEquivalentWingDihedral = textFieldEquivalentWingDihedral;
	}

	public TextField getTextFieldEquivalentWingTaperRatio() {
		return textFieldEquivalentWingTaperRatio;
	}

	public void setTextFieldEquivalentWingTaperRatio(TextField textFieldEquivalentWingTaperRatio) {
		this.textFieldEquivalentWingTaperRatio = textFieldEquivalentWingTaperRatio;
	}

	public TextField getTextFieldEquivalentWingRootXOffsetLE() {
		return textFieldEquivalentWingRootXOffsetLE;
	}

	public void setTextFieldEquivalentWingRootXOffsetLE(TextField textFieldEquivalentWingRootXOffsetLE) {
		this.textFieldEquivalentWingRootXOffsetLE = textFieldEquivalentWingRootXOffsetLE;
	}

	public TextField getTextFieldEquivalentWingRootXOffsetTE() {
		return textFieldEquivalentWingRootXOffsetTE;
	}

	public void setTextFieldEquivalentWingRootXOffsetTE(TextField textFieldEquivalentWingRootXOffsetTE) {
		this.textFieldEquivalentWingRootXOffsetTE = textFieldEquivalentWingRootXOffsetTE;
	}

	public TextField getTextFieldEquivalentWingAirfoilRootPath() {
		return textFieldEquivalentWingAirfoilRootPath;
	}

	public void setTextFieldEquivalentWingAirfoilRootPath(TextField textFieldEquivalentWingAirfoilRootPath) {
		this.textFieldEquivalentWingAirfoilRootPath = textFieldEquivalentWingAirfoilRootPath;
	}

	public TextField getTextFieldEquivalentWingAirfoilKinkPath() {
		return textFieldEquivalentWingAirfoilKinkPath;
	}

	public void setTextFieldEquivalentWingAirfoilKinkPath(TextField textFieldEquivalentWingAirfoilKinkPath) {
		this.textFieldEquivalentWingAirfoilKinkPath = textFieldEquivalentWingAirfoilKinkPath;
	}

	public TextField getTextFieldEquivalentWingAirfoilTipPath() {
		return textFieldEquivalentWingAirfoilTipPath;
	}

	public void setTextFieldEquivalentWingAirfoilTipPath(TextField textFieldEquivalentWingAirfoilTipPath) {
		this.textFieldEquivalentWingAirfoilTipPath = textFieldEquivalentWingAirfoilTipPath;
	}

	public TextField getTextFieldWingSpanPanel1() {
		return textFieldWingSpanPanel1;
	}

	public void setTextFieldWingSpanPanel1(TextField textFieldWingSpanPanel1) {
		this.textFieldWingSpanPanel1 = textFieldWingSpanPanel1;
	}

	public TextField getTextFieldWingSweepLeadingEdgePanel1() {
		return textFieldWingSweepLeadingEdgePanel1;
	}

	public void setTextFieldWingSweepLeadingEdgePanel1(TextField textFieldWingSweepLeadingEdgePanel1) {
		this.textFieldWingSweepLeadingEdgePanel1 = textFieldWingSweepLeadingEdgePanel1;
	}

	public TextField getTextFieldWingDihedralPanel1() {
		return textFieldWingDihedralPanel1;
	}

	public void setTextFieldWingDihedralPanel1(TextField textFieldWingDihedralPanel1) {
		this.textFieldWingDihedralPanel1 = textFieldWingDihedralPanel1;
	}

	public TextField getTextFieldWingChordInnerSectionPanel1() {
		return textFieldWingChordInnerSectionPanel1;
	}

	public void setTextFieldWingChordInnerSectionPanel1(TextField textFieldWingChordInnerSectionPanel1) {
		this.textFieldWingChordInnerSectionPanel1 = textFieldWingChordInnerSectionPanel1;
	}

	public TextField getTextFieldWingTwistInnerSectionPanel1() {
		return textFieldWingTwistInnerSectionPanel1;
	}

	public void setTextFieldWingTwistInnerSectionPanel1(TextField textFieldWingTwistInnerSectionPanel1) {
		this.textFieldWingTwistInnerSectionPanel1 = textFieldWingTwistInnerSectionPanel1;
	}

	public TextField getTextFieldWingAirfoilPathInnerSectionPanel1() {
		return textFieldWingAirfoilPathInnerSectionPanel1;
	}

	public void setTextFieldWingAirfoilPathInnerSectionPanel1(TextField textFieldWingAirfoilPathInnerSectionPanel1) {
		this.textFieldWingAirfoilPathInnerSectionPanel1 = textFieldWingAirfoilPathInnerSectionPanel1;
	}

	public TextField getTextFieldWingChordOuterSectionPanel1() {
		return textFieldWingChordOuterSectionPanel1;
	}

	public void setTextFieldWingChordOuterSectionPanel1(TextField textFieldWingChordOuterSectionPanel1) {
		this.textFieldWingChordOuterSectionPanel1 = textFieldWingChordOuterSectionPanel1;
	}

	public TextField getTextFieldWingTwistOuterSectionPanel1() {
		return textFieldWingTwistOuterSectionPanel1;
	}

	public void setTextFieldWingTwistOuterSectionPanel1(TextField textFieldWingTwistOuterSectionPanel1) {
		this.textFieldWingTwistOuterSectionPanel1 = textFieldWingTwistOuterSectionPanel1;
	}

	public TextField getTextFieldWingAirfoilPathOuterSectionPanel1() {
		return textFieldWingAirfoilPathOuterSectionPanel1;
	}

	public void setTextFieldWingAirfoilPathOuterSectionPanel1(TextField textFieldWingAirfoilPathOuterSectionPanel1) {
		this.textFieldWingAirfoilPathOuterSectionPanel1 = textFieldWingAirfoilPathOuterSectionPanel1;
	}

	public ChoiceBox<String> getWingFlap1TypeChoichBox() {
		return wingFlap1TypeChoichBox;
	}

	public void setWingFlap1TypeChoichBox(ChoiceBox<String> wingFlap1TypeChoichBox) {
		this.wingFlap1TypeChoichBox = wingFlap1TypeChoichBox;
	}

	public TextField getTextFieldWingInnerPositionFlap1() {
		return textFieldWingInnerPositionFlap1;
	}

	public void setTextFieldWingInnerPositionFlap1(TextField textFieldWingInnerPositionFlap1) {
		this.textFieldWingInnerPositionFlap1 = textFieldWingInnerPositionFlap1;
	}

	public TextField getTextFieldWingOuterPositionFlap1() {
		return textFieldWingOuterPositionFlap1;
	}

	public void setTextFieldWingOuterPositionFlap1(TextField textFieldWingOuterPositionFlap1) {
		this.textFieldWingOuterPositionFlap1 = textFieldWingOuterPositionFlap1;
	}

	public TextField getTextFieldWingInnerChordRatioFlap1() {
		return textFieldWingInnerChordRatioFlap1;
	}

	public void setTextFieldWingInnerChordRatioFlap1(TextField textFieldWingInnerChordRatioFlap1) {
		this.textFieldWingInnerChordRatioFlap1 = textFieldWingInnerChordRatioFlap1;
	}

	public TextField getTextFieldWingOuterChordRatioFlap1() {
		return textFieldWingOuterChordRatioFlap1;
	}

	public void setTextFieldWingOuterChordRatioFlap1(TextField textFieldWingOuterChordRatioFlap1) {
		this.textFieldWingOuterChordRatioFlap1 = textFieldWingOuterChordRatioFlap1;
	}

	public TextField getTextFieldWingMinimumDeflectionAngleFlap1() {
		return textFieldWingMinimumDeflectionAngleFlap1;
	}

	public void setTextFieldWingMinimumDeflectionAngleFlap1(TextField textFieldWingMinimumDeflectionAngleFlap1) {
		this.textFieldWingMinimumDeflectionAngleFlap1 = textFieldWingMinimumDeflectionAngleFlap1;
	}

	public TextField getTextFieldWingMaximumDeflectionAngleFlap1() {
		return textFieldWingMaximumDeflectionAngleFlap1;
	}

	public void setTextFieldWingMaximumDeflectionAngleFlap1(TextField textFieldWingMaximumDeflectionAngleFlap1) {
		this.textFieldWingMaximumDeflectionAngleFlap1 = textFieldWingMaximumDeflectionAngleFlap1;
	}

	public TextField getTextFieldWingInnerPositionSlat1() {
		return textFieldWingInnerPositionSlat1;
	}

	public void setTextFieldWingInnerPositionSlat1(TextField textFieldWingInnerPositionSlat1) {
		this.textFieldWingInnerPositionSlat1 = textFieldWingInnerPositionSlat1;
	}

	public TextField getTextFieldWingOuterPositionSlat1() {
		return textFieldWingOuterPositionSlat1;
	}

	public void setTextFieldWingOuterPositionSlat1(TextField textFieldWingOuterPositionSlat1) {
		this.textFieldWingOuterPositionSlat1 = textFieldWingOuterPositionSlat1;
	}

	public TextField getTextFieldWingInnerChordRatioSlat1() {
		return textFieldWingInnerChordRatioSlat1;
	}

	public void setTextFieldWingInnerChordRatioSlat1(TextField textFieldWingInnerChordRatioSlat1) {
		this.textFieldWingInnerChordRatioSlat1 = textFieldWingInnerChordRatioSlat1;
	}

	public TextField getTextFieldWingOuterChordRatioSlat1() {
		return textFieldWingOuterChordRatioSlat1;
	}

	public void setTextFieldWingOuterChordRatioSlat1(TextField textFieldWingOuterChordRatioSlat1) {
		this.textFieldWingOuterChordRatioSlat1 = textFieldWingOuterChordRatioSlat1;
	}

	public TextField getTextFieldWingExtensionRatioSlat1() {
		return textFieldWingExtensionRatioSlat1;
	}

	public void setTextFieldWingExtensionRatioSlat1(TextField textFieldWingExtensionRatioSlat1) {
		this.textFieldWingExtensionRatioSlat1 = textFieldWingExtensionRatioSlat1;
	}

	public TextField getTextFieldWingMinimumDeflectionAngleSlat1() {
		return textFieldWingMinimumDeflectionAngleSlat1;
	}

	public void setTextFieldWingMinimumDeflectionAngleSlat1(TextField textFieldWingMinimumDeflectionAngleSlat1) {
		this.textFieldWingMinimumDeflectionAngleSlat1 = textFieldWingMinimumDeflectionAngleSlat1;
	}

	public TextField getTextFieldWingMaximumDeflectionAngleSlat1() {
		return textFieldWingMaximumDeflectionAngleSlat1;
	}

	public void setTextFieldWingMaximumDeflectionAngleSlat1(TextField textFieldWingMaximumDeflectionAngleSlat1) {
		this.textFieldWingMaximumDeflectionAngleSlat1 = textFieldWingMaximumDeflectionAngleSlat1;
	}

	public ChoiceBox<String> getWingLeftAileronTypeChoichBox() {
		return wingLeftAileronTypeChoichBox;
	}

	public void setWingLeftAileronTypeChoichBox(ChoiceBox<String> wingLeftAileronTypeChoichBox) {
		this.wingLeftAileronTypeChoichBox = wingLeftAileronTypeChoichBox;
	}

	public TextField getTextFieldWingInnerPositionAileronLeft() {
		return textFieldWingInnerPositionAileronLeft;
	}

	public void setTextFieldWingInnerPositionAileronLeft(TextField textFieldWingInnerPositionAileronLeft) {
		this.textFieldWingInnerPositionAileronLeft = textFieldWingInnerPositionAileronLeft;
	}

	public TextField getTextFieldWingOuterPositionAileronLeft() {
		return textFieldWingOuterPositionAileronLeft;
	}

	public void setTextFieldWingOuterPositionAileronLeft(TextField textFieldWingOuterPositionAileronLeft) {
		this.textFieldWingOuterPositionAileronLeft = textFieldWingOuterPositionAileronLeft;
	}

	public TextField getTextFieldWingInnerChordRatioAileronLeft() {
		return textFieldWingInnerChordRatioAileronLeft;
	}

	public void setTextFieldWingInnerChordRatioAileronLeft(TextField textFieldWingInnerChordRatioAileronLeft) {
		this.textFieldWingInnerChordRatioAileronLeft = textFieldWingInnerChordRatioAileronLeft;
	}

	public TextField getTextFieldWingOuterChordRatioAileronLeft() {
		return textFieldWingOuterChordRatioAileronLeft;
	}

	public void setTextFieldWingOuterChordRatioAileronLeft(TextField textFieldWingOuterChordRatioAileronLeft) {
		this.textFieldWingOuterChordRatioAileronLeft = textFieldWingOuterChordRatioAileronLeft;
	}

	public TextField getTextFieldWingMinimumDeflectionAngleAileronLeft() {
		return textFieldWingMinimumDeflectionAngleAileronLeft;
	}

	public void setTextFieldWingMinimumDeflectionAngleAileronLeft(
			TextField textFieldWingMinimumDeflectionAngleAileronLeft) {
		this.textFieldWingMinimumDeflectionAngleAileronLeft = textFieldWingMinimumDeflectionAngleAileronLeft;
	}

	public TextField getTextFieldWingMaximumDeflectionAngleAileronLeft() {
		return textFieldWingMaximumDeflectionAngleAileronLeft;
	}

	public void setTextFieldWingMaximumDeflectionAngleAileronLeft(
			TextField textFieldWingMaximumDeflectionAngleAileronLeft) {
		this.textFieldWingMaximumDeflectionAngleAileronLeft = textFieldWingMaximumDeflectionAngleAileronLeft;
	}

	public ChoiceBox<String> getWingRightAileronTypeChoichBox() {
		return wingRightAileronTypeChoichBox;
	}

	public void setWingRightAileronTypeChoichBox(ChoiceBox<String> wingRightAileronTypeChoichBox) {
		this.wingRightAileronTypeChoichBox = wingRightAileronTypeChoichBox;
	}

	public TextField getTextFieldWingInnerPositionAileronRight() {
		return textFieldWingInnerPositionAileronRight;
	}

	public void setTextFieldWingInnerPositionAileronRight(TextField textFieldWingInnerPositionAileronRight) {
		this.textFieldWingInnerPositionAileronRight = textFieldWingInnerPositionAileronRight;
	}

	public TextField getTextFieldWingOuterPositionAileronRight() {
		return textFieldWingOuterPositionAileronRight;
	}

	public void setTextFieldWingOuterPositionAileronRight(TextField textFieldWingOuterPositionAileronRight) {
		this.textFieldWingOuterPositionAileronRight = textFieldWingOuterPositionAileronRight;
	}

	public TextField getTextFieldWingInnerChordRatioAileronRight() {
		return textFieldWingInnerChordRatioAileronRight;
	}

	public void setTextFieldWingInnerChordRatioAileronRight(TextField textFieldWingInnerChordRatioAileronRight) {
		this.textFieldWingInnerChordRatioAileronRight = textFieldWingInnerChordRatioAileronRight;
	}

	public TextField getTextFieldWingOuterChordRatioAileronRight() {
		return textFieldWingOuterChordRatioAileronRight;
	}

	public void setTextFieldWingOuterChordRatioAileronRight(TextField textFieldWingOuterChordRatioAileronRight) {
		this.textFieldWingOuterChordRatioAileronRight = textFieldWingOuterChordRatioAileronRight;
	}

	public TextField getTextFieldWingMinimumDeflectionAngleAileronRight() {
		return textFieldWingMinimumDeflectionAngleAileronRight;
	}

	public void setTextFieldWingMinimumDeflectionAngleAileronRight(
			TextField textFieldWingMinimumDeflectionAngleAileronRight) {
		this.textFieldWingMinimumDeflectionAngleAileronRight = textFieldWingMinimumDeflectionAngleAileronRight;
	}

	public TextField getTextFieldWingMaximumDeflectionAngleAileronRight() {
		return textFieldWingMaximumDeflectionAngleAileronRight;
	}

	public void setTextFieldWingMaximumDeflectionAngleAileronRight(
			TextField textFieldWingMaximumDeflectionAngleAileronRight) {
		this.textFieldWingMaximumDeflectionAngleAileronRight = textFieldWingMaximumDeflectionAngleAileronRight;
	}

	public TextField getTextFieldWingInnerSpanwisePositionSpolier1() {
		return textFieldWingInnerSpanwisePositionSpolier1;
	}

	public void setTextFieldWingInnerSpanwisePositionSpolier1(TextField textFieldWingInnerSpanwisePositionSpolier1) {
		this.textFieldWingInnerSpanwisePositionSpolier1 = textFieldWingInnerSpanwisePositionSpolier1;
	}

	public TextField getTextFieldWingOuterSpanwisePositionSpolier1() {
		return textFieldWingOuterSpanwisePositionSpolier1;
	}

	public void setTextFieldWingOuterSpanwisePositionSpolier1(TextField textFieldWingOuterSpanwisePositionSpolier1) {
		this.textFieldWingOuterSpanwisePositionSpolier1 = textFieldWingOuterSpanwisePositionSpolier1;
	}

	public TextField getTextFieldWingInnerChordwisePositionSpolier1() {
		return textFieldWingInnerChordwisePositionSpolier1;
	}

	public void setTextFieldWingInnerChordwisePositionSpolier1(TextField textFieldWingInnerChordwisePositionSpolier1) {
		this.textFieldWingInnerChordwisePositionSpolier1 = textFieldWingInnerChordwisePositionSpolier1;
	}

	public TextField getTextFieldWingOuterChordwisePositionSpolier1() {
		return textFieldWingOuterChordwisePositionSpolier1;
	}

	public void setTextFieldWingOuterChordwisePositionSpolier1(TextField textFieldWingOuterChordwisePositionSpolier1) {
		this.textFieldWingOuterChordwisePositionSpolier1 = textFieldWingOuterChordwisePositionSpolier1;
	}

	public TextField getTextFieldWingMinimumDeflectionAngleSpolier1() {
		return textFieldWingMinimumDeflectionAngleSpolier1;
	}

	public void setTextFieldWingMinimumDeflectionAngleSpolier1(TextField textFieldWingMinimumDeflectionAngleSpolier1) {
		this.textFieldWingMinimumDeflectionAngleSpolier1 = textFieldWingMinimumDeflectionAngleSpolier1;
	}

	public TextField getTextFieldWingMaximumDeflectionAngleSpoiler1() {
		return textFieldWingMaximumDeflectionAngleSpoiler1;
	}

	public void setTextFieldWingMaximumDeflectionAngleSpoiler1(TextField textFieldWingMaximumDeflectionAngleSpoiler1) {
		this.textFieldWingMaximumDeflectionAngleSpoiler1 = textFieldWingMaximumDeflectionAngleSpoiler1;
	}

	public List<TextField> getTextFieldWingSpanPanelList() {
		return textFieldWingSpanPanelList;
	}

	public void setTextFieldWingSpanPanelList(List<TextField> textFieldWingSpanPanelList) {
		this.textFieldWingSpanPanelList = textFieldWingSpanPanelList;
	}

	public List<TextField> getTextFieldWingSweepLEPanelList() {
		return textFieldWingSweepLEPanelList;
	}

	public void setTextFieldWingSweepLEPanelList(List<TextField> textFieldWingSweepLEPanelList) {
		this.textFieldWingSweepLEPanelList = textFieldWingSweepLEPanelList;
	}

	public List<TextField> getTextFieldWingDihedralPanelList() {
		return textFieldWingDihedralPanelList;
	}

	public void setTextFieldWingDihedralPanelList(List<TextField> textFieldWingDihedralPanelList) {
		this.textFieldWingDihedralPanelList = textFieldWingDihedralPanelList;
	}

	public List<TextField> getTextFieldWingInnerChordPanelList() {
		return textFieldWingInnerChordPanelList;
	}

	public void setTextFieldWingInnerChordPanelList(List<TextField> textFieldWingInnerChordPanelList) {
		this.textFieldWingInnerChordPanelList = textFieldWingInnerChordPanelList;
	}

	public List<TextField> getTextFieldWingInnerTwistPanelList() {
		return textFieldWingInnerTwistPanelList;
	}

	public void setTextFieldWingInnerTwistPanelList(List<TextField> textFieldWingInnerTwistPanelList) {
		this.textFieldWingInnerTwistPanelList = textFieldWingInnerTwistPanelList;
	}

	public List<TextField> getTextFieldWingInnerAirfoilPanelList() {
		return textFieldWingInnerAirfoilPanelList;
	}

	public void setTextFieldWingInnerAirfoilPanelList(List<TextField> textFieldWingInnerAirfoilPanelList) {
		this.textFieldWingInnerAirfoilPanelList = textFieldWingInnerAirfoilPanelList;
	}

	public List<TextField> getTextFieldWingOuterChordPanelList() {
		return textFieldWingOuterChordPanelList;
	}

	public void setTextFieldWingOuterChordPanelList(List<TextField> textFieldWingOuterChordPanelList) {
		this.textFieldWingOuterChordPanelList = textFieldWingOuterChordPanelList;
	}

	public List<TextField> getTextFieldWingOuterTwistPanelList() {
		return textFieldWingOuterTwistPanelList;
	}

	public void setTextFieldWingOuterTwistPanelList(List<TextField> textFieldWingOuterTwistPanelList) {
		this.textFieldWingOuterTwistPanelList = textFieldWingOuterTwistPanelList;
	}

	public List<TextField> getTextFieldWingOuterAirfoilPanelList() {
		return textFieldWingOuterAirfoilPanelList;
	}

	public void setTextFieldWingOuterAirfoilPanelList(List<TextField> textFieldWingOuterAirfoilPanelList) {
		this.textFieldWingOuterAirfoilPanelList = textFieldWingOuterAirfoilPanelList;
	}

	public List<CheckBox> getCheckBoxWingLinkedToPreviousPanelList() {
		return checkBoxWingLinkedToPreviousPanelList;
	}

	public void setCheckBoxWingLinkedToPreviousPanelList(List<CheckBox> checkBoxWingLinkedToPreviousPanelList) {
		this.checkBoxWingLinkedToPreviousPanelList = checkBoxWingLinkedToPreviousPanelList;
	}

	public List<ChoiceBox<String>> getChoiceBoxWingSpanPanelUnitList() {
		return choiceBoxWingSpanPanelUnitList;
	}

	public void setChoiceBoxWingSpanPanelUnitList(List<ChoiceBox<String>> choiceBoxWingSpanPanelUnitList) {
		this.choiceBoxWingSpanPanelUnitList = choiceBoxWingSpanPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxWingSweepLEPanelUnitList() {
		return choiceBoxWingSweepLEPanelUnitList;
	}

	public void setChoiceBoxWingSweepLEPanelUnitList(List<ChoiceBox<String>> choiceBoxWingSweepLEPanelUnitList) {
		this.choiceBoxWingSweepLEPanelUnitList = choiceBoxWingSweepLEPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxWingDihedralPanelUnitList() {
		return choiceBoxWingDihedralPanelUnitList;
	}

	public void setChoiceBoxWingDihedralPanelUnitList(List<ChoiceBox<String>> choiceBoxWingDihedralPanelUnitList) {
		this.choiceBoxWingDihedralPanelUnitList = choiceBoxWingDihedralPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxWingInnerChordPanelUnitList() {
		return choiceBoxWingInnerChordPanelUnitList;
	}

	public void setChoiceBoxWingInnerChordPanelUnitList(List<ChoiceBox<String>> choiceBoxWingInnerChordPanelUnitList) {
		this.choiceBoxWingInnerChordPanelUnitList = choiceBoxWingInnerChordPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxWingInnerTwistPanelUnitList() {
		return choiceBoxWingInnerTwistPanelUnitList;
	}

	public void setChoiceBoxWingInnerTwistPanelUnitList(List<ChoiceBox<String>> choiceBoxWingInnerTwistPanelUnitList) {
		this.choiceBoxWingInnerTwistPanelUnitList = choiceBoxWingInnerTwistPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxWingOuterChordPanelUnitList() {
		return choiceBoxWingOuterChordPanelUnitList;
	}

	public void setChoiceBoxWingOuterChordPanelUnitList(List<ChoiceBox<String>> choiceBoxWingOuterChordPanelUnitList) {
		this.choiceBoxWingOuterChordPanelUnitList = choiceBoxWingOuterChordPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxWingOuterTwistPanelUnitList() {
		return choiceBoxWingOuterTwistPanelUnitList;
	}

	public void setChoiceBoxWingOuterTwistPanelUnitList(List<ChoiceBox<String>> choiceBoxWingOuterTwistPanelUnitList) {
		this.choiceBoxWingOuterTwistPanelUnitList = choiceBoxWingOuterTwistPanelUnitList;
	}

	public List<Button> getDetailButtonWingInnerAirfoilList() {
		return detailButtonWingInnerAirfoilList;
	}

	public void setDetailButtonWingInnerAirfoilList(List<Button> detailButtonWingInnerAirfoilList) {
		this.detailButtonWingInnerAirfoilList = detailButtonWingInnerAirfoilList;
	}

	public List<Button> getDetailButtonWingOuterAirfoilList() {
		return detailButtonWingOuterAirfoilList;
	}

	public void setDetailButtonWingOuterAirfoilList(List<Button> detailButtonWingOuterAirfoilList) {
		this.detailButtonWingOuterAirfoilList = detailButtonWingOuterAirfoilList;
	}

	public List<Button> getChooseInnerWingAirfoilFileButtonList() {
		return chooseInnerWingAirfoilFileButtonList;
	}

	public void setChooseInnerWingAirfoilFileButtonList(List<Button> chooseInnerWingAirfoilFileButtonList) {
		this.chooseInnerWingAirfoilFileButtonList = chooseInnerWingAirfoilFileButtonList;
	}

	public List<Button> getChooseOuterWingAirfoilFileButtonList() {
		return chooseOuterWingAirfoilFileButtonList;
	}

	public void setChooseOuterWingAirfoilFileButtonList(List<Button> chooseOuterWingAirfoilFileButtonList) {
		this.chooseOuterWingAirfoilFileButtonList = chooseOuterWingAirfoilFileButtonList;
	}

	public List<ChoiceBox<String>> getChoiceBoxWingFlapTypeList() {
		return choiceBoxWingFlapTypeList;
	}

	public void setChoiceBoxWingFlapTypeList(List<ChoiceBox<String>> choiceBoxWingFlapTypeList) {
		this.choiceBoxWingFlapTypeList = choiceBoxWingFlapTypeList;
	}

	public List<TextField> getTextFieldWingInnerPositionFlapList() {
		return textFieldWingInnerPositionFlapList;
	}

	public void setTextFieldWingInnerPositionFlapList(List<TextField> textFieldWingInnerPositionFlapList) {
		this.textFieldWingInnerPositionFlapList = textFieldWingInnerPositionFlapList;
	}

	public List<TextField> getTextFieldWingOuterPositionFlapList() {
		return textFieldWingOuterPositionFlapList;
	}

	public void setTextFieldWingOuterPositionFlapList(List<TextField> textFieldWingOuterPositionFlapList) {
		this.textFieldWingOuterPositionFlapList = textFieldWingOuterPositionFlapList;
	}

	public List<TextField> getTextFieldWingInnerChordRatioFlapList() {
		return textFieldWingInnerChordRatioFlapList;
	}

	public void setTextFieldWingInnerChordRatioFlapList(List<TextField> textFieldWingInnerChordRatioFlapList) {
		this.textFieldWingInnerChordRatioFlapList = textFieldWingInnerChordRatioFlapList;
	}

	public List<TextField> getTextFieldWingOuterChordRatioFlapList() {
		return textFieldWingOuterChordRatioFlapList;
	}

	public void setTextFieldWingOuterChordRatioFlapList(List<TextField> textFieldWingOuterChordRatioFlapList) {
		this.textFieldWingOuterChordRatioFlapList = textFieldWingOuterChordRatioFlapList;
	}

	public List<TextField> getTextFieldWingMinimumDeflectionAngleFlapList() {
		return textFieldWingMinimumDeflectionAngleFlapList;
	}

	public void setTextFieldWingMinimumDeflectionAngleFlapList(
			List<TextField> textFieldWingMinimumDeflectionAngleFlapList) {
		this.textFieldWingMinimumDeflectionAngleFlapList = textFieldWingMinimumDeflectionAngleFlapList;
	}

	public List<TextField> getTextFieldWingMaximumDeflectionAngleFlapList() {
		return textFieldWingMaximumDeflectionAngleFlapList;
	}

	public void setTextFieldWingMaximumDeflectionAngleFlapList(
			List<TextField> textFieldWingMaximumDeflectionAngleFlapList) {
		this.textFieldWingMaximumDeflectionAngleFlapList = textFieldWingMaximumDeflectionAngleFlapList;
	}

	public List<ChoiceBox<String>> getChoiceBoxWingMinimumDeflectionAngleFlapUnitList() {
		return choiceBoxWingMinimumDeflectionAngleFlapUnitList;
	}

	public void setChoiceBoxWingMinimumDeflectionAngleFlapUnitList(
			List<ChoiceBox<String>> choiceBoxWingMinimumDeflectionAngleFlapUnitList) {
		this.choiceBoxWingMinimumDeflectionAngleFlapUnitList = choiceBoxWingMinimumDeflectionAngleFlapUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxWingMaximumDeflectionAngleFlapUnitList() {
		return choiceBoxWingMaximumDeflectionAngleFlapUnitList;
	}

	public void setChoiceBoxWingMaximumDeflectionAngleFlapUnitList(
			List<ChoiceBox<String>> choiceBoxWingMaximumDeflectionAngleFlapUnitList) {
		this.choiceBoxWingMaximumDeflectionAngleFlapUnitList = choiceBoxWingMaximumDeflectionAngleFlapUnitList;
	}

	public List<TextField> getTextFieldWingInnerPositionSlatList() {
		return textFieldWingInnerPositionSlatList;
	}

	public void setTextFieldWingInnerPositionSlatList(List<TextField> textFieldWingInnerPositionSlatList) {
		this.textFieldWingInnerPositionSlatList = textFieldWingInnerPositionSlatList;
	}

	public List<TextField> getTextFieldWingOuterPositionSlatList() {
		return textFieldWingOuterPositionSlatList;
	}

	public void setTextFieldWingOuterPositionSlatList(List<TextField> textFieldWingOuterPositionSlatList) {
		this.textFieldWingOuterPositionSlatList = textFieldWingOuterPositionSlatList;
	}

	public List<TextField> getTextFieldWingInnerChordRatioSlatList() {
		return textFieldWingInnerChordRatioSlatList;
	}

	public void setTextFieldWingInnerChordRatioSlatList(List<TextField> textFieldWingInnerChordRatioSlatList) {
		this.textFieldWingInnerChordRatioSlatList = textFieldWingInnerChordRatioSlatList;
	}

	public List<TextField> getTextFieldWingOuterChordRatioSlatList() {
		return textFieldWingOuterChordRatioSlatList;
	}

	public void setTextFieldWingOuterChordRatioSlatList(List<TextField> textFieldWingOuterChordRatioSlatList) {
		this.textFieldWingOuterChordRatioSlatList = textFieldWingOuterChordRatioSlatList;
	}

	public List<TextField> getTextFieldWingExtensionRatioSlatList() {
		return textFieldWingExtensionRatioSlatList;
	}

	public void setTextFieldWingExtensionRatioSlatList(List<TextField> textFieldWingExtensionRatioSlatList) {
		this.textFieldWingExtensionRatioSlatList = textFieldWingExtensionRatioSlatList;
	}

	public List<TextField> getTextFieldWingMinimumDeflectionAngleSlatList() {
		return textFieldWingMinimumDeflectionAngleSlatList;
	}

	public void setTextFieldWingMinimumDeflectionAngleSlatList(
			List<TextField> textFieldWingMinimumDeflectionAngleSlatList) {
		this.textFieldWingMinimumDeflectionAngleSlatList = textFieldWingMinimumDeflectionAngleSlatList;
	}

	public List<TextField> getTextFieldWingMaximumDeflectionAngleSlatList() {
		return textFieldWingMaximumDeflectionAngleSlatList;
	}

	public void setTextFieldWingMaximumDeflectionAngleSlatList(
			List<TextField> textFieldWingMaximumDeflectionAngleSlatList) {
		this.textFieldWingMaximumDeflectionAngleSlatList = textFieldWingMaximumDeflectionAngleSlatList;
	}

	public List<ChoiceBox<String>> getChoiceBoxWingMinimumDeflectionAngleSlatUnitList() {
		return choiceBoxWingMinimumDeflectionAngleSlatUnitList;
	}

	public void setChoiceBoxWingMinimumDeflectionAngleSlatUnitList(
			List<ChoiceBox<String>> choiceBoxWingMinimumDeflectionAngleSlatUnitList) {
		this.choiceBoxWingMinimumDeflectionAngleSlatUnitList = choiceBoxWingMinimumDeflectionAngleSlatUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxWingMaximumDeflectionAngleSlatUnitList() {
		return choiceBoxWingMaximumDeflectionAngleSlatUnitList;
	}

	public void setChoiceBoxWingMaximumDeflectionAngleSlatUnitList(
			List<ChoiceBox<String>> choiceBoxWingMaximumDeflectionAngleSlatUnitList) {
		this.choiceBoxWingMaximumDeflectionAngleSlatUnitList = choiceBoxWingMaximumDeflectionAngleSlatUnitList;
	}

	public List<TextField> getTextFieldWingInnerSpanwisePositionSpoilerList() {
		return textFieldWingInnerSpanwisePositionSpoilerList;
	}

	public void setTextFieldWingInnerSpanwisePositionSpoilerList(
			List<TextField> textFieldWingInnerSpanwisePositionSpoilerList) {
		this.textFieldWingInnerSpanwisePositionSpoilerList = textFieldWingInnerSpanwisePositionSpoilerList;
	}

	public List<TextField> getTextFieldWingOuterSpanwisePositionSpoilerList() {
		return textFieldWingOuterSpanwisePositionSpoilerList;
	}

	public void setTextFieldWingOuterSpanwisePositionSpoilerList(
			List<TextField> textFieldWingOuterSpanwisePositionSpoilerList) {
		this.textFieldWingOuterSpanwisePositionSpoilerList = textFieldWingOuterSpanwisePositionSpoilerList;
	}

	public List<TextField> getTextFieldWingInnerChordwisePositionSpoilerList() {
		return textFieldWingInnerChordwisePositionSpoilerList;
	}

	public void setTextFieldWingInnerChordwisePositionSpoilerList(
			List<TextField> textFieldWingInnerChordwisePositionSpoilerList) {
		this.textFieldWingInnerChordwisePositionSpoilerList = textFieldWingInnerChordwisePositionSpoilerList;
	}

	public List<TextField> getTextFieldWingOuterChordwisePositionSpoilerList() {
		return textFieldWingOuterChordwisePositionSpoilerList;
	}

	public void setTextFieldWingOuterChordwisePositionSpoilerList(
			List<TextField> textFieldWingOuterChordwisePositionSpoilerList) {
		this.textFieldWingOuterChordwisePositionSpoilerList = textFieldWingOuterChordwisePositionSpoilerList;
	}

	public List<TextField> getTextFieldWingMinimumDeflectionAngleSpoilerList() {
		return textFieldWingMinimumDeflectionAngleSpoilerList;
	}

	public void setTextFieldWingMinimumDeflectionAngleSpoilerList(
			List<TextField> textFieldWingMinimumDeflectionAngleSpoilerList) {
		this.textFieldWingMinimumDeflectionAngleSpoilerList = textFieldWingMinimumDeflectionAngleSpoilerList;
	}

	public List<TextField> getTextFieldWingMaximumDeflectionAngleSpoilerList() {
		return textFieldWingMaximumDeflectionAngleSpoilerList;
	}

	public void setTextFieldWingMaximumDeflectionAngleSpoilerList(
			List<TextField> textFieldWingMaximumDeflectionAngleSpoilerList) {
		this.textFieldWingMaximumDeflectionAngleSpoilerList = textFieldWingMaximumDeflectionAngleSpoilerList;
	}

	public List<ChoiceBox<String>> getChoiceBoxWingMinimumDeflectionAngleSpoilerUnitList() {
		return choiceBoxWingMinimumDeflectionAngleSpoilerUnitList;
	}

	public void setChoiceBoxWingMinimumDeflectionAngleSpoilerUnitList(
			List<ChoiceBox<String>> choiceBoxWingMinimumDeflectionAngleSpoilerUnitList) {
		this.choiceBoxWingMinimumDeflectionAngleSpoilerUnitList = choiceBoxWingMinimumDeflectionAngleSpoilerUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxWingMaximumDeflectionAngleSpoilerUnitList() {
		return choiceBoxWingMaximumDeflectionAngleSpoilerUnitList;
	}

	public void setChoiceBoxWingMaximumDeflectionAngleSpoilerUnitList(
			List<ChoiceBox<String>> choiceBoxWingMaximumDeflectionAngleSpoilerUnitList) {
		this.choiceBoxWingMaximumDeflectionAngleSpoilerUnitList = choiceBoxWingMaximumDeflectionAngleSpoilerUnitList;
	}

	public ChoiceBox<String> getWingRoughnessUnitChoiceBox() {
		return wingRoughnessUnitChoiceBox;
	}

	public void setWingRoughnessUnitChoiceBox(ChoiceBox<String> wingRoughnessUnitChoiceBox) {
		this.wingRoughnessUnitChoiceBox = wingRoughnessUnitChoiceBox;
	}

	public ChoiceBox<String> getWingWingletHeightUnitChoiceBox() {
		return wingWingletHeightUnitChoiceBox;
	}

	public void setWingWingletHeightUnitChoiceBox(ChoiceBox<String> wingWingletHeightUnitChoiceBox) {
		this.wingWingletHeightUnitChoiceBox = wingWingletHeightUnitChoiceBox;
	}

	public ChoiceBox<String> getEquivalentWingAreaUnitChoiceBox() {
		return equivalentWingAreaUnitChoiceBox;
	}

	public void setEquivalentWingAreaUnitChoiceBox(ChoiceBox<String> equivalentWingAreaUnitChoiceBox) {
		this.equivalentWingAreaUnitChoiceBox = equivalentWingAreaUnitChoiceBox;
	}

	public ChoiceBox<String> getEquivalentWingSweepLEUnitChoiceBox() {
		return equivalentWingSweepLEUnitChoiceBox;
	}

	public void setEquivalentWingSweepLEUnitChoiceBox(ChoiceBox<String> equivalentWingSweepLEUnitChoiceBox) {
		this.equivalentWingSweepLEUnitChoiceBox = equivalentWingSweepLEUnitChoiceBox;
	}

	public ChoiceBox<String> getEquivalentWingTwistAtTipUnitChoiceBox() {
		return equivalentWingTwistAtTipUnitChoiceBox;
	}

	public void setEquivalentWingTwistAtTipUnitChoiceBox(ChoiceBox<String> equivalentWingTwistAtTipUnitChoiceBox) {
		this.equivalentWingTwistAtTipUnitChoiceBox = equivalentWingTwistAtTipUnitChoiceBox;
	}

	public ChoiceBox<String> getEquivalentWingDihedralUnitChoiceBox() {
		return equivalentWingDihedralUnitChoiceBox;
	}

	public void setEquivalentWingDihedralUnitChoiceBox(ChoiceBox<String> equivalentWingDihedralUnitChoiceBox) {
		this.equivalentWingDihedralUnitChoiceBox = equivalentWingDihedralUnitChoiceBox;
	}

	public ChoiceBox<String> getWingSpanPanel1UnitChoiceBox() {
		return wingSpanPanel1UnitChoiceBox;
	}

	public void setWingSpanPanel1UnitChoiceBox(ChoiceBox<String> wingSpanPanel1UnitChoiceBox) {
		this.wingSpanPanel1UnitChoiceBox = wingSpanPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getWingSweepLEPanel1UnitChoiceBox() {
		return wingSweepLEPanel1UnitChoiceBox;
	}

	public void setWingSweepLEPanel1UnitChoiceBox(ChoiceBox<String> wingSweepLEPanel1UnitChoiceBox) {
		this.wingSweepLEPanel1UnitChoiceBox = wingSweepLEPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getWingDihedralPanel1UnitChoiceBox() {
		return wingDihedralPanel1UnitChoiceBox;
	}

	public void setWingDihedralPanel1UnitChoiceBox(ChoiceBox<String> wingDihedralPanel1UnitChoiceBox) {
		this.wingDihedralPanel1UnitChoiceBox = wingDihedralPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getWingInnerSectionChordPanel1UnitChoiceBox() {
		return wingInnerSectionChordPanel1UnitChoiceBox;
	}

	public void setWingInnerSectionChordPanel1UnitChoiceBox(ChoiceBox<String> wingInnerSectionChordPanel1UnitChoiceBox) {
		this.wingInnerSectionChordPanel1UnitChoiceBox = wingInnerSectionChordPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getWingInnerSectionTwistTipPanel1UnitChoiceBox() {
		return wingInnerSectionTwistTipPanel1UnitChoiceBox;
	}

	public void setWingInnerSectionTwistTipPanel1UnitChoiceBox(
			ChoiceBox<String> wingInnerSectionTwistTipPanel1UnitChoiceBox) {
		this.wingInnerSectionTwistTipPanel1UnitChoiceBox = wingInnerSectionTwistTipPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getWingOuterSectionChordPanel1UnitChoiceBox() {
		return wingOuterSectionChordPanel1UnitChoiceBox;
	}

	public void setWingOuterSectionChordPanel1UnitChoiceBox(ChoiceBox<String> wingOuterSectionChordPanel1UnitChoiceBox) {
		this.wingOuterSectionChordPanel1UnitChoiceBox = wingOuterSectionChordPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getWingOuterSectionTwistTipPanel1UnitChoiceBox() {
		return wingOuterSectionTwistTipPanel1UnitChoiceBox;
	}

	public void setWingOuterSectionTwistTipPanel1UnitChoiceBox(
			ChoiceBox<String> wingOuterSectionTwistTipPanel1UnitChoiceBox) {
		this.wingOuterSectionTwistTipPanel1UnitChoiceBox = wingOuterSectionTwistTipPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getWingMinimumDeflectionAngleFlap1UnitChoiceBox() {
		return wingMinimumDeflectionAngleFlap1UnitChoiceBox;
	}

	public void setWingMinimumDeflectionAngleFlap1UnitChoiceBox(
			ChoiceBox<String> wingMinimumDeflectionAngleFlap1UnitChoiceBox) {
		this.wingMinimumDeflectionAngleFlap1UnitChoiceBox = wingMinimumDeflectionAngleFlap1UnitChoiceBox;
	}

	public ChoiceBox<String> getWingMaximumDeflectionAngleFlap1UnitChoiceBox() {
		return wingMaximumDeflectionAngleFlap1UnitChoiceBox;
	}

	public void setWingMaximumDeflectionAngleFlap1UnitChoiceBox(
			ChoiceBox<String> wingMaximumDeflectionAngleFlap1UnitChoiceBox) {
		this.wingMaximumDeflectionAngleFlap1UnitChoiceBox = wingMaximumDeflectionAngleFlap1UnitChoiceBox;
	}

	public ChoiceBox<String> getWingMinimumDeflectionAngleSlat1UnitChoiceBox() {
		return wingMinimumDeflectionAngleSlat1UnitChoiceBox;
	}

	public void setWingMinimumDeflectionAngleSlat1UnitChoiceBox(
			ChoiceBox<String> wingMinimumDeflectionAngleSlat1UnitChoiceBox) {
		this.wingMinimumDeflectionAngleSlat1UnitChoiceBox = wingMinimumDeflectionAngleSlat1UnitChoiceBox;
	}

	public ChoiceBox<String> getWingMaximumDeflectionAngleSlat1UnitChoiceBox() {
		return wingMaximumDeflectionAngleSlat1UnitChoiceBox;
	}

	public void setWingMaximumDeflectionAngleSlat1UnitChoiceBox(
			ChoiceBox<String> wingMaximumDeflectionAngleSlat1UnitChoiceBox) {
		this.wingMaximumDeflectionAngleSlat1UnitChoiceBox = wingMaximumDeflectionAngleSlat1UnitChoiceBox;
	}

	public ChoiceBox<String> getWingMinimumDeflectionAngleAileronLeftUnitChoiceBox() {
		return wingMinimumDeflectionAngleAileronLeftUnitChoiceBox;
	}

	public void setWingMinimumDeflectionAngleAileronLeftUnitChoiceBox(
			ChoiceBox<String> wingMinimumDeflectionAngleAileronLeftUnitChoiceBox) {
		this.wingMinimumDeflectionAngleAileronLeftUnitChoiceBox = wingMinimumDeflectionAngleAileronLeftUnitChoiceBox;
	}

	public ChoiceBox<String> getWingMaximumDeflectionAngleAileronLeftUnitChoiceBox() {
		return wingMaximumDeflectionAngleAileronLeftUnitChoiceBox;
	}

	public void setWingMaximumDeflectionAngleAileronLeftUnitChoiceBox(
			ChoiceBox<String> wingMaximumDeflectionAngleAileronLeftUnitChoiceBox) {
		this.wingMaximumDeflectionAngleAileronLeftUnitChoiceBox = wingMaximumDeflectionAngleAileronLeftUnitChoiceBox;
	}

	public ChoiceBox<String> getWingMinimumDeflectionAngleAileronRigthUnitChoiceBox() {
		return wingMinimumDeflectionAngleAileronRightUnitChoiceBox;
	}

	public void setWingMinimumDeflectionAngleAileronRigthUnitChoiceBox(
			ChoiceBox<String> wingMinimumDeflectionAngleAileronRigthUnitChoiceBox) {
		this.wingMinimumDeflectionAngleAileronRightUnitChoiceBox = wingMinimumDeflectionAngleAileronRigthUnitChoiceBox;
	}

	public ChoiceBox<String> getWingMaximumDeflectionAngleAileronRightUnitChoiceBox() {
		return wingMaximumDeflectionAngleAileronRightUnitChoiceBox;
	}

	public void setWingMaximumDeflectionAngleAileronRightUnitChoiceBox(
			ChoiceBox<String> wingMaximumDeflectionAngleAileronRightUnitChoiceBox) {
		this.wingMaximumDeflectionAngleAileronRightUnitChoiceBox = wingMaximumDeflectionAngleAileronRightUnitChoiceBox;
	}

	public ChoiceBox<String> getWingMinimumDeflectionAngleSpoiler1UnitChoiceBox() {
		return wingMinimumDeflectionAngleSpoiler1UnitChoiceBox;
	}

	public void setWingMinimumDeflectionAngleSpoiler1UnitChoiceBox(
			ChoiceBox<String> wingMinimumDeflectionAngleSpoiler1UnitChoiceBox) {
		this.wingMinimumDeflectionAngleSpoiler1UnitChoiceBox = wingMinimumDeflectionAngleSpoiler1UnitChoiceBox;
	}

	public ChoiceBox<String> getWingMaximumDeflectionAngleSpoiler1UnitChoiceBox() {
		return wingMaximumDeflectionAngleSpoiler1UnitChoiceBox;
	}

	public void setWingMaximumDeflectionAngleSpoiler1UnitChoiceBox(
			ChoiceBox<String> wingMaximumDeflectionAngleSpoiler1UnitChoiceBox) {
		this.wingMaximumDeflectionAngleSpoiler1UnitChoiceBox = wingMaximumDeflectionAngleSpoiler1UnitChoiceBox;
	}

	public ChoiceBox<String> gethTailAdjustCriterionChoiceBox() {
		return hTailAdjustCriterionChoiceBox;
	}

	public void sethTailAdjustCriterionChoiceBox(ChoiceBox<String> hTailAdjustCriterionChoiceBox) {
		this.hTailAdjustCriterionChoiceBox = hTailAdjustCriterionChoiceBox;
	}

	public TextField getTextFieldHTailRoughness() {
		return textFieldHTailRoughness;
	}

	public void setTextFieldHTailRoughness(TextField textFieldHTailRoughness) {
		this.textFieldHTailRoughness = textFieldHTailRoughness;
	}

	public TextField getTextFieldHTailSpanPanel1() {
		return textFieldHTailSpanPanel1;
	}

	public void setTextFieldHTailSpanPanel1(TextField textFieldHTailSpanPanel1) {
		this.textFieldHTailSpanPanel1 = textFieldHTailSpanPanel1;
	}

	public TextField getTextFieldHTailSweepLeadingEdgePanel1() {
		return textFieldHTailSweepLeadingEdgePanel1;
	}

	public void setTextFieldHTailSweepLeadingEdgePanel1(TextField textFieldHTailSweepLeadingEdgePanel1) {
		this.textFieldHTailSweepLeadingEdgePanel1 = textFieldHTailSweepLeadingEdgePanel1;
	}

	public TextField getTextFieldHTailDihedralPanel1() {
		return textFieldHTailDihedralPanel1;
	}

	public void setTextFieldHTailDihedralPanel1(TextField textFieldHTailDihedralPanel1) {
		this.textFieldHTailDihedralPanel1 = textFieldHTailDihedralPanel1;
	}

	public TextField getTextFieldHTailChordInnerSectionPanel1() {
		return textFieldHTailChordInnerSectionPanel1;
	}

	public void setTextFieldHTailChordInnerSectionPanel1(TextField textFieldHTailChordInnerSectionPanel1) {
		this.textFieldHTailChordInnerSectionPanel1 = textFieldHTailChordInnerSectionPanel1;
	}

	public TextField getTextFieldHTailTwistInnerSectionPanel1() {
		return textFieldHTailTwistInnerSectionPanel1;
	}

	public void setTextFieldHTailTwistInnerSectionPanel1(TextField textFieldHTailTwistInnerSectionPanel1) {
		this.textFieldHTailTwistInnerSectionPanel1 = textFieldHTailTwistInnerSectionPanel1;
	}

	public TextField getTextFieldHTailAirfoilPathInnerSectionPanel1() {
		return textFieldHTailAirfoilPathInnerSectionPanel1;
	}

	public void setTextFieldHTailAirfoilPathInnerSectionPanel1(TextField textFieldHTailAirfoilPathInnerSectionPanel1) {
		this.textFieldHTailAirfoilPathInnerSectionPanel1 = textFieldHTailAirfoilPathInnerSectionPanel1;
	}

	public TextField getTextFieldHTailChordOuterSectionPanel1() {
		return textFieldHTailChordOuterSectionPanel1;
	}

	public void setTextFieldHTailChordOuterSectionPanel1(TextField textFieldHTailChordOuterSectionPanel1) {
		this.textFieldHTailChordOuterSectionPanel1 = textFieldHTailChordOuterSectionPanel1;
	}

	public TextField getTextFieldHTailTwistOuterSectionPanel1() {
		return textFieldHTailTwistOuterSectionPanel1;
	}

	public void setTextFieldHTailTwistOuterSectionPanel1(TextField textFieldHTailTwistOuterSectionPanel1) {
		this.textFieldHTailTwistOuterSectionPanel1 = textFieldHTailTwistOuterSectionPanel1;
	}

	public TextField getTextFieldHTailAirfoilPathOuterSectionPanel1() {
		return textFieldHTailAirfoilPathOuterSectionPanel1;
	}

	public void setTextFieldHTailAirfoilPathOuterSectionPanel1(TextField textFieldHTailAirfoilPathOuterSectionPanel1) {
		this.textFieldHTailAirfoilPathOuterSectionPanel1 = textFieldHTailAirfoilPathOuterSectionPanel1;
	}

	public ChoiceBox<String> gethTailElevator1TypeChoiceBox() {
		return hTailElevator1TypeChoiceBox;
	}

	public void sethTailElevator1TypeChoiceBox(ChoiceBox<String> hTailElevator1TypeChoiceBox) {
		this.hTailElevator1TypeChoiceBox = hTailElevator1TypeChoiceBox;
	}

	public TextField getTextFieldHTailInnerPositionElevator1() {
		return textFieldHTailInnerPositionElevator1;
	}

	public void setTextFieldHTailInnerPositionElevator1(TextField textFieldHTailInnerPositionElevator1) {
		this.textFieldHTailInnerPositionElevator1 = textFieldHTailInnerPositionElevator1;
	}

	public TextField getTextFieldHTailOuterPositionElevator1() {
		return textFieldHTailOuterPositionElevator1;
	}

	public void setTextFieldHTailOuterPositionElevator1(TextField textFieldHTailOuterPositionElevator1) {
		this.textFieldHTailOuterPositionElevator1 = textFieldHTailOuterPositionElevator1;
	}

	public TextField getTextFieldHTailInnerChordRatioElevator1() {
		return textFieldHTailInnerChordRatioElevator1;
	}

	public void setTextFieldHTailInnerChordRatioElevator1(TextField textFieldHTailInnerChordRatioElevator1) {
		this.textFieldHTailInnerChordRatioElevator1 = textFieldHTailInnerChordRatioElevator1;
	}

	public TextField getTextFieldHTailOuterChordRatioElevator1() {
		return textFieldHTailOuterChordRatioElevator1;
	}

	public void setTextFieldHTailOuterChordRatioElevator1(TextField textFieldHTailOuterChordRatioElevator1) {
		this.textFieldHTailOuterChordRatioElevator1 = textFieldHTailOuterChordRatioElevator1;
	}

	public TextField getTextFieldHTailMinimumDeflectionAngleElevator1() {
		return textFieldHTailMinimumDeflectionAngleElevator1;
	}

	public void setTextFieldHTailMinimumDeflectionAngleElevator1(TextField textFieldHTailMinimumDeflectionAngleElevator1) {
		this.textFieldHTailMinimumDeflectionAngleElevator1 = textFieldHTailMinimumDeflectionAngleElevator1;
	}

	public TextField getTextFieldHTailMaximumDeflectionAngleElevator1() {
		return textFieldHTailMaximumDeflectionAngleElevator1;
	}

	public void setTextFieldHTailMaximumDeflectionAngleElevator1(TextField textFieldHTailMaximumDeflectionAngleElevator1) {
		this.textFieldHTailMaximumDeflectionAngleElevator1 = textFieldHTailMaximumDeflectionAngleElevator1;
	}

	public List<TextField> getTextFieldHTailSpanPanelList() {
		return textFieldHTailSpanPanelList;
	}

	public void setTextFieldHTailSpanPanelList(List<TextField> textFieldHTailSpanPanelList) {
		this.textFieldHTailSpanPanelList = textFieldHTailSpanPanelList;
	}

	public List<TextField> getTextFieldHTailSweepLEPanelList() {
		return textFieldHTailSweepLEPanelList;
	}

	public void setTextFieldHTailSweepLEPanelList(List<TextField> textFieldHTailSweepLEPanelList) {
		this.textFieldHTailSweepLEPanelList = textFieldHTailSweepLEPanelList;
	}

	public List<TextField> getTextFieldHTailDihedralPanelList() {
		return textFieldHTailDihedralPanelList;
	}

	public void setTextFieldHTailDihedralPanelList(List<TextField> textFieldHTailDihedralPanelList) {
		this.textFieldHTailDihedralPanelList = textFieldHTailDihedralPanelList;
	}

	public List<TextField> getTextFieldHTailInnerChordPanelList() {
		return textFieldHTailInnerChordPanelList;
	}

	public void setTextFieldHTailInnerChordPanelList(List<TextField> textFieldHTailInnerChordPanelList) {
		this.textFieldHTailInnerChordPanelList = textFieldHTailInnerChordPanelList;
	}

	public List<TextField> getTextFieldHTailInnerTwistPanelList() {
		return textFieldHTailInnerTwistPanelList;
	}

	public void setTextFieldHTailInnerTwistPanelList(List<TextField> textFieldHTailInnerTwistPanelList) {
		this.textFieldHTailInnerTwistPanelList = textFieldHTailInnerTwistPanelList;
	}

	public List<TextField> getTextFieldHTailInnerAirfoilPanelList() {
		return textFieldHTailInnerAirfoilPanelList;
	}

	public void setTextFieldHTailInnerAirfoilPanelList(List<TextField> textFieldHTailInnerAirfoilPanelList) {
		this.textFieldHTailInnerAirfoilPanelList = textFieldHTailInnerAirfoilPanelList;
	}

	public List<TextField> getTextFieldHTailOuterChordPanelList() {
		return textFieldHTailOuterChordPanelList;
	}

	public void setTextFieldHTailOuterChordPanelList(List<TextField> textFieldHTailOuterChordPanelList) {
		this.textFieldHTailOuterChordPanelList = textFieldHTailOuterChordPanelList;
	}

	public List<TextField> getTextFieldHTailOuterTwistPanelList() {
		return textFieldHTailOuterTwistPanelList;
	}

	public void setTextFieldHTailOuterTwistPanelList(List<TextField> textFieldHTailOuterTwistPanelList) {
		this.textFieldHTailOuterTwistPanelList = textFieldHTailOuterTwistPanelList;
	}

	public List<TextField> getTextFieldHTailOuterAirfoilPanelList() {
		return textFieldHTailOuterAirfoilPanelList;
	}

	public void setTextFieldHTailOuterAirfoilPanelList(List<TextField> textFieldHTailOuterAirfoilPanelList) {
		this.textFieldHTailOuterAirfoilPanelList = textFieldHTailOuterAirfoilPanelList;
	}

	public List<CheckBox> getCheckBoxHTailLinkedToPreviousPanelList() {
		return checkBoxHTailLinkedToPreviousPanelList;
	}

	public void setCheckBoxHTailLinkedToPreviousPanelList(List<CheckBox> checkBoxHTailLinkedToPreviousPanelList) {
		this.checkBoxHTailLinkedToPreviousPanelList = checkBoxHTailLinkedToPreviousPanelList;
	}

	public List<ChoiceBox<String>> getChoiceBoxHTailSpanPanelUnitList() {
		return choiceBoxHTailSpanPanelUnitList;
	}

	public void setChoiceBoxHTailSpanPanelUnitList(List<ChoiceBox<String>> choiceBoxHTailSpanPanelUnitList) {
		this.choiceBoxHTailSpanPanelUnitList = choiceBoxHTailSpanPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxHTailSweepLEPanelUnitList() {
		return choiceBoxHTailSweepLEPanelUnitList;
	}

	public void setChoiceBoxHTailSweepLEPanelUnitList(List<ChoiceBox<String>> choiceBoxHTailSweepLEPanelUnitList) {
		this.choiceBoxHTailSweepLEPanelUnitList = choiceBoxHTailSweepLEPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxHTailDihedralPanelUnitList() {
		return choiceBoxHTailDihedralPanelUnitList;
	}

	public void setChoiceBoxHTailDihedralPanelUnitList(List<ChoiceBox<String>> choiceBoxHTailDihedralPanelUnitList) {
		this.choiceBoxHTailDihedralPanelUnitList = choiceBoxHTailDihedralPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxHTailInnerChordPanelUnitList() {
		return choiceBoxHTailInnerChordPanelUnitList;
	}

	public void setChoiceBoxHTailInnerChordPanelUnitList(List<ChoiceBox<String>> choiceBoxHTailInnerChordPanelUnitList) {
		this.choiceBoxHTailInnerChordPanelUnitList = choiceBoxHTailInnerChordPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxHTailInnerTwistPanelUnitList() {
		return choiceBoxHTailInnerTwistPanelUnitList;
	}

	public void setChoiceBoxHTailInnerTwistPanelUnitList(List<ChoiceBox<String>> choiceBoxHTailInnerTwistPanelUnitList) {
		this.choiceBoxHTailInnerTwistPanelUnitList = choiceBoxHTailInnerTwistPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxHTailOuterChordPanelUnitList() {
		return choiceBoxHTailOuterChordPanelUnitList;
	}

	public void setChoiceBoxHTailOuterChordPanelUnitList(List<ChoiceBox<String>> choiceBoxHTailOuterChordPanelUnitList) {
		this.choiceBoxHTailOuterChordPanelUnitList = choiceBoxHTailOuterChordPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxHTailOuterTwistPanelUnitList() {
		return choiceBoxHTailOuterTwistPanelUnitList;
	}

	public void setChoiceBoxHTailOuterTwistPanelUnitList(List<ChoiceBox<String>> choiceBoxHTailOuterTwistPanelUnitList) {
		this.choiceBoxHTailOuterTwistPanelUnitList = choiceBoxHTailOuterTwistPanelUnitList;
	}

	public List<Button> getDetailButtonHTailInnerAirfoilList() {
		return detailButtonHTailInnerAirfoilList;
	}

	public void setDetailButtonHTailInnerAirfoilList(List<Button> detailButtonHTailInnerAirfoilList) {
		this.detailButtonHTailInnerAirfoilList = detailButtonHTailInnerAirfoilList;
	}

	public List<Button> getDetailButtonHTailOuterAirfoilList() {
		return detailButtonHTailOuterAirfoilList;
	}

	public void setDetailButtonHTailOuterAirfoilList(List<Button> detailButtonHTailOuterAirfoilList) {
		this.detailButtonHTailOuterAirfoilList = detailButtonHTailOuterAirfoilList;
	}

	public List<Button> getChooseInnerHTailAirfoilFileButtonList() {
		return chooseInnerHTailAirfoilFileButtonList;
	}

	public void setChooseInnerHTailAirfoilFileButtonList(List<Button> chooseInnerHTailAirfoilFileButtonList) {
		this.chooseInnerHTailAirfoilFileButtonList = chooseInnerHTailAirfoilFileButtonList;
	}

	public List<Button> getChooseOuterHTailAirfoilFileButtonList() {
		return chooseOuterHTailAirfoilFileButtonList;
	}

	public void setChooseOuterHTailAirfoilFileButtonList(List<Button> chooseOuterHTailAirfoilFileButtonList) {
		this.chooseOuterHTailAirfoilFileButtonList = chooseOuterHTailAirfoilFileButtonList;
	}

	public List<ChoiceBox<String>> getChoiceBoxHTailElevatorTypeList() {
		return choiceBoxHTailElevatorTypeList;
	}

	public void setChoiceBoxHTailElevatorTypeList(List<ChoiceBox<String>> choiceBoxHTailElevatorTypeList) {
		this.choiceBoxHTailElevatorTypeList = choiceBoxHTailElevatorTypeList;
	}

	public List<TextField> getTextFieldHTailInnerPositionElevatorList() {
		return textFieldHTailInnerPositionElevatorList;
	}

	public void setTextFieldHTailInnerPositionElevatorList(List<TextField> textFieldHTailInnerPositionElevatorList) {
		this.textFieldHTailInnerPositionElevatorList = textFieldHTailInnerPositionElevatorList;
	}

	public List<TextField> getTextFieldHTailOuterPositionElevatorList() {
		return textFieldHTailOuterPositionElevatorList;
	}

	public void setTextFieldHTailOuterPositionElevatorList(List<TextField> textFieldHTailOuterPositionElevatorList) {
		this.textFieldHTailOuterPositionElevatorList = textFieldHTailOuterPositionElevatorList;
	}

	public List<TextField> getTextFieldHTailInnerChordRatioElevatorList() {
		return textFieldHTailInnerChordRatioElevatorList;
	}

	public void setTextFieldHTailInnerChordRatioElevatorList(List<TextField> textFieldHTailInnerChordRatioElevatorList) {
		this.textFieldHTailInnerChordRatioElevatorList = textFieldHTailInnerChordRatioElevatorList;
	}

	public List<TextField> getTextFieldHTailOuterChordRatioElevatorList() {
		return textFieldHTailOuterChordRatioElevatorList;
	}

	public void setTextFieldHTailOuterChordRatioElevatorList(List<TextField> textFieldHTailOuterChordRatioElevatorList) {
		this.textFieldHTailOuterChordRatioElevatorList = textFieldHTailOuterChordRatioElevatorList;
	}

	public List<TextField> getTextFieldHTailMinimumDeflectionAngleElevatorList() {
		return textFieldHTailMinimumDeflectionAngleElevatorList;
	}

	public void setTextFieldHTailMinimumDeflectionAngleElevatorList(
			List<TextField> textFieldHTailMinimumDeflectionAngleElevatorList) {
		this.textFieldHTailMinimumDeflectionAngleElevatorList = textFieldHTailMinimumDeflectionAngleElevatorList;
	}

	public List<TextField> getTextFieldHTailMaximumDeflectionAngleElevatorList() {
		return textFieldHTailMaximumDeflectionAngleElevatorList;
	}

	public void setTextFieldHTailMaximumDeflectionAngleElevatorList(
			List<TextField> textFieldHTailMaximumDeflectionAngleElevatorList) {
		this.textFieldHTailMaximumDeflectionAngleElevatorList = textFieldHTailMaximumDeflectionAngleElevatorList;
	}

	public List<ChoiceBox<String>> getChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList() {
		return choiceBoxHTailMinimumDeflectionAngleElevatorUnitList;
	}

	public void setChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList(
			List<ChoiceBox<String>> choiceBoxHTailMinimumDeflectionAngleElevatorUnitList) {
		this.choiceBoxHTailMinimumDeflectionAngleElevatorUnitList = choiceBoxHTailMinimumDeflectionAngleElevatorUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxHTailMaximumDeflectionAngleElevatorUnitList() {
		return choiceBoxHTailMaximumDeflectionAngleElevatorUnitList;
	}

	public void setChoiceBoxHTailMaximumDeflectionAngleElevatorUnitList(
			List<ChoiceBox<String>> choiceBoxHTailMaximumDeflectionAngleElevatorUnitList) {
		this.choiceBoxHTailMaximumDeflectionAngleElevatorUnitList = choiceBoxHTailMaximumDeflectionAngleElevatorUnitList;
	}

	public ChoiceBox<String> gethTailRoughnessUnitChoiceBox() {
		return hTailRoughnessUnitChoiceBox;
	}

	public void sethTailRoughnessUnitChoiceBox(ChoiceBox<String> hTailRoughnessUnitChoiceBox) {
		this.hTailRoughnessUnitChoiceBox = hTailRoughnessUnitChoiceBox;
	}

	public ChoiceBox<String> gethTailSpanPanel1UnitChoiceBox() {
		return hTailSpanPanel1UnitChoiceBox;
	}

	public void sethTailSpanPanel1UnitChoiceBox(ChoiceBox<String> hTailSpanPanel1UnitChoiceBox) {
		this.hTailSpanPanel1UnitChoiceBox = hTailSpanPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> gethTailSweepLEPanel1UnitChoiceBox() {
		return hTailSweepLEPanel1UnitChoiceBox;
	}

	public void sethTailSweepLEPanel1UnitChoiceBox(ChoiceBox<String> hTailSweepLEPanel1UnitChoiceBox) {
		this.hTailSweepLEPanel1UnitChoiceBox = hTailSweepLEPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> gethTailDihedralPanel1UnitChoiceBox() {
		return hTailDihedralPanel1UnitChoiceBox;
	}

	public void sethTailDihedralPanel1UnitChoiceBox(ChoiceBox<String> hTailDihedralPanel1UnitChoiceBox) {
		this.hTailDihedralPanel1UnitChoiceBox = hTailDihedralPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> gethTailInnerSectionChordPanel1UnitChoiceBox() {
		return hTailInnerSectionChordPanel1UnitChoiceBox;
	}

	public void sethTailInnerSectionChordPanel1UnitChoiceBox(ChoiceBox<String> hTailInnerSectionChordPanel1UnitChoiceBox) {
		this.hTailInnerSectionChordPanel1UnitChoiceBox = hTailInnerSectionChordPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> gethTailInnerSectionTwistTipPanel1UnitChoiceBox() {
		return hTailInnerSectionTwistTipPanel1UnitChoiceBox;
	}

	public void sethTailInnerSectionTwistTipPanel1UnitChoiceBox(
			ChoiceBox<String> hTailInnerSectionTwistTipPanel1UnitChoiceBox) {
		this.hTailInnerSectionTwistTipPanel1UnitChoiceBox = hTailInnerSectionTwistTipPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> gethTailOuterSectionChordPanel1UnitChoiceBox() {
		return hTailOuterSectionChordPanel1UnitChoiceBox;
	}

	public void sethTailOuterSectionChordPanel1UnitChoiceBox(ChoiceBox<String> hTailOuterSectionChordPanel1UnitChoiceBox) {
		this.hTailOuterSectionChordPanel1UnitChoiceBox = hTailOuterSectionChordPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> gethTailOuterSectionTwistTipPanel1UnitChoiceBox() {
		return hTailOuterSectionTwistTipPanel1UnitChoiceBox;
	}

	public void sethTailOuterSectionTwistTipPanel1UnitChoiceBox(
			ChoiceBox<String> hTailOuterSectionTwistTipPanel1UnitChoiceBox) {
		this.hTailOuterSectionTwistTipPanel1UnitChoiceBox = hTailOuterSectionTwistTipPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> gethTailMinimumDeflectionAngleElevator1UnitChoiceBox() {
		return hTailMinimumDeflectionAngleElevator1UnitChoiceBox;
	}

	public void sethTailMinimumDeflectionAngleElevator1UnitChoiceBox(
			ChoiceBox<String> hTailMinimumDeflectionAngleElevator1UnitChoiceBox) {
		this.hTailMinimumDeflectionAngleElevator1UnitChoiceBox = hTailMinimumDeflectionAngleElevator1UnitChoiceBox;
	}

	public ChoiceBox<String> gethTailMaximumDeflectionAngleElevator1UnitChoiceBox() {
		return hTailMaximumDeflectionAngleElevator1UnitChoiceBox;
	}

	public void sethTailMaximumDeflectionAngleElevator1UnitChoiceBox(
			ChoiceBox<String> hTailMaximumDeflectionAngleElevator1UnitChoiceBox) {
		this.hTailMaximumDeflectionAngleElevator1UnitChoiceBox = hTailMaximumDeflectionAngleElevator1UnitChoiceBox;
	}

	public ChoiceBox<String> getvTailAdjustCriterionChoiceBox() {
		return vTailAdjustCriterionChoiceBox;
	}

	public void setvTailAdjustCriterionChoiceBox(ChoiceBox<String> vTailAdjustCriterionChoiceBox) {
		this.vTailAdjustCriterionChoiceBox = vTailAdjustCriterionChoiceBox;
	}

	public TextField getTextFieldVTailRoughness() {
		return textFieldVTailRoughness;
	}

	public void setTextFieldVTailRoughness(TextField textFieldVTailRoughness) {
		this.textFieldVTailRoughness = textFieldVTailRoughness;
	}

	public TextField getTextFieldVTailSpanPanel1() {
		return textFieldVTailSpanPanel1;
	}

	public void setTextFieldVTailSpanPanel1(TextField textFieldVTailSpanPanel1) {
		this.textFieldVTailSpanPanel1 = textFieldVTailSpanPanel1;
	}

	public TextField getTextFieldVTailSweepLeadingEdgePanel1() {
		return textFieldVTailSweepLeadingEdgePanel1;
	}

	public void setTextFieldVTailSweepLeadingEdgePanel1(TextField textFieldVTailSweepLeadingEdgePanel1) {
		this.textFieldVTailSweepLeadingEdgePanel1 = textFieldVTailSweepLeadingEdgePanel1;
	}

	public TextField getTextFieldVTailDihedralPanel1() {
		return textFieldVTailDihedralPanel1;
	}

	public void setTextFieldVTailDihedralPanel1(TextField textFieldVTailDihedralPanel1) {
		this.textFieldVTailDihedralPanel1 = textFieldVTailDihedralPanel1;
	}

	public TextField getTextFieldVTailChordInnerSectionPanel1() {
		return textFieldVTailChordInnerSectionPanel1;
	}

	public void setTextFieldVTailChordInnerSectionPanel1(TextField textFieldVTailChordInnerSectionPanel1) {
		this.textFieldVTailChordInnerSectionPanel1 = textFieldVTailChordInnerSectionPanel1;
	}

	public TextField getTextFieldVTailTwistInnerSectionPanel1() {
		return textFieldVTailTwistInnerSectionPanel1;
	}

	public void setTextFieldVTailTwistInnerSectionPanel1(TextField textFieldVTailTwistInnerSectionPanel1) {
		this.textFieldVTailTwistInnerSectionPanel1 = textFieldVTailTwistInnerSectionPanel1;
	}

	public TextField getTextFieldVTailAirfoilPathInnerSectionPanel1() {
		return textFieldVTailAirfoilPathInnerSectionPanel1;
	}

	public void setTextFieldVTailAirfoilPathInnerSectionPanel1(TextField textFieldVTailAirfoilPathInnerSectionPanel1) {
		this.textFieldVTailAirfoilPathInnerSectionPanel1 = textFieldVTailAirfoilPathInnerSectionPanel1;
	}

	public TextField getTextFieldVTailChordOuterSectionPanel1() {
		return textFieldVTailChordOuterSectionPanel1;
	}

	public void setTextFieldVTailChordOuterSectionPanel1(TextField textFieldVTailChordOuterSectionPanel1) {
		this.textFieldVTailChordOuterSectionPanel1 = textFieldVTailChordOuterSectionPanel1;
	}

	public TextField getTextFieldVTailTwistOuterSectionPanel1() {
		return textFieldVTailTwistOuterSectionPanel1;
	}

	public void setTextFieldVTailTwistOuterSectionPanel1(TextField textFieldVTailTwistOuterSectionPanel1) {
		this.textFieldVTailTwistOuterSectionPanel1 = textFieldVTailTwistOuterSectionPanel1;
	}

	public TextField getTextFieldVTailAirfoilPathOuterSectionPanel1() {
		return textFieldVTailAirfoilPathOuterSectionPanel1;
	}

	public void setTextFieldVTailAirfoilPathOuterSectionPanel1(TextField textFieldVTailAirfoilPathOuterSectionPanel1) {
		this.textFieldVTailAirfoilPathOuterSectionPanel1 = textFieldVTailAirfoilPathOuterSectionPanel1;
	}

	public ChoiceBox<String> getvTailRudder1TypeChoiceBox() {
		return vTailRudder1TypeChoiceBox;
	}

	public void setvTailRudder1TypeChoiceBox(ChoiceBox<String> vTailRudder1TypeChoiceBox) {
		this.vTailRudder1TypeChoiceBox = vTailRudder1TypeChoiceBox;
	}

	public TextField getTextFieldVTailInnerPositionRudder1() {
		return textFieldVTailInnerPositionRudder1;
	}

	public void setTextFieldVTailInnerPositionRudder1(TextField textFieldVTailInnerPositionRudder1) {
		this.textFieldVTailInnerPositionRudder1 = textFieldVTailInnerPositionRudder1;
	}

	public TextField getTextFieldVTailOuterPositionRudder1() {
		return textFieldVTailOuterPositionRudder1;
	}

	public void setTextFieldVTailOuterPositionRudder1(TextField textFieldVTailOuterPositionRudder1) {
		this.textFieldVTailOuterPositionRudder1 = textFieldVTailOuterPositionRudder1;
	}

	public TextField getTextFieldVTailInnerChordRatioRudder1() {
		return textFieldVTailInnerChordRatioRudder1;
	}

	public void setTextFieldVTailInnerChordRatioRudder1(TextField textFieldVTailInnerChordRatioRudder1) {
		this.textFieldVTailInnerChordRatioRudder1 = textFieldVTailInnerChordRatioRudder1;
	}

	public TextField getTextFieldVTailOuterChordRatioRudder1() {
		return textFieldVTailOuterChordRatioRudder1;
	}

	public void setTextFieldVTailOuterChordRatioRudder1(TextField textFieldVTailOuterChordRatioRudder1) {
		this.textFieldVTailOuterChordRatioRudder1 = textFieldVTailOuterChordRatioRudder1;
	}

	public TextField getTextFieldVTailMinimumDeflectionAngleRudder1() {
		return textFieldVTailMinimumDeflectionAngleRudder1;
	}

	public void setTextFieldVTailMinimumDeflectionAngleRudder1(TextField textFieldVTailMinimumDeflectionAngleRudder1) {
		this.textFieldVTailMinimumDeflectionAngleRudder1 = textFieldVTailMinimumDeflectionAngleRudder1;
	}

	public TextField getTextFieldVTailMaximumDeflectionAngleRudder1() {
		return textFieldVTailMaximumDeflectionAngleRudder1;
	}

	public void setTextFieldVTailMaximumDeflectionAngleRudder1(TextField textFieldVTailMaximumDeflectionAngleRudder1) {
		this.textFieldVTailMaximumDeflectionAngleRudder1 = textFieldVTailMaximumDeflectionAngleRudder1;
	}

	public List<TextField> getTextFieldVTailSpanPanelList() {
		return textFieldVTailSpanPanelList;
	}

	public void setTextFieldVTailSpanPanelList(List<TextField> textFieldVTailSpanPanelList) {
		this.textFieldVTailSpanPanelList = textFieldVTailSpanPanelList;
	}

	public List<TextField> getTextFieldVTailSweepLEPanelList() {
		return textFieldVTailSweepLEPanelList;
	}

	public void setTextFieldVTailSweepLEPanelList(List<TextField> textFieldVTailSweepLEPanelList) {
		this.textFieldVTailSweepLEPanelList = textFieldVTailSweepLEPanelList;
	}

	public List<TextField> getTextFieldVTailDihedralPanelList() {
		return textFieldVTailDihedralPanelList;
	}

	public void setTextFieldVTailDihedralPanelList(List<TextField> textFieldVTailDihedralPanelList) {
		this.textFieldVTailDihedralPanelList = textFieldVTailDihedralPanelList;
	}

	public List<TextField> getTextFieldVTailInnerChordPanelList() {
		return textFieldVTailInnerChordPanelList;
	}

	public void setTextFieldVTailInnerChordPanelList(List<TextField> textFieldVTailInnerChordPanelList) {
		this.textFieldVTailInnerChordPanelList = textFieldVTailInnerChordPanelList;
	}

	public List<TextField> getTextFieldVTailInnerTwistPanelList() {
		return textFieldVTailInnerTwistPanelList;
	}

	public void setTextFieldVTailInnerTwistPanelList(List<TextField> textFieldVTailInnerTwistPanelList) {
		this.textFieldVTailInnerTwistPanelList = textFieldVTailInnerTwistPanelList;
	}

	public List<TextField> getTextFieldVTailInnerAirfoilPanelList() {
		return textFieldVTailInnerAirfoilPanelList;
	}

	public void setTextFieldVTailInnerAirfoilPanelList(List<TextField> textFieldVTailInnerAirfoilPanelList) {
		this.textFieldVTailInnerAirfoilPanelList = textFieldVTailInnerAirfoilPanelList;
	}

	public List<TextField> getTextFieldVTailOuterChordPanelList() {
		return textFieldVTailOuterChordPanelList;
	}

	public void setTextFieldVTailOuterChordPanelList(List<TextField> textFieldVTailOuterChordPanelList) {
		this.textFieldVTailOuterChordPanelList = textFieldVTailOuterChordPanelList;
	}

	public List<TextField> getTextFieldVTailOuterTwistPanelList() {
		return textFieldVTailOuterTwistPanelList;
	}

	public void setTextFieldVTailOuterTwistPanelList(List<TextField> textFieldVTailOuterTwistPanelList) {
		this.textFieldVTailOuterTwistPanelList = textFieldVTailOuterTwistPanelList;
	}

	public List<TextField> getTextFieldVTailOuterAirfoilPanelList() {
		return textFieldVTailOuterAirfoilPanelList;
	}

	public void setTextFieldVTailOuterAirfoilPanelList(List<TextField> textFieldVTailOuterAirfoilPanelList) {
		this.textFieldVTailOuterAirfoilPanelList = textFieldVTailOuterAirfoilPanelList;
	}

	public List<CheckBox> getCheckBoxVTailLinkedToPreviousPanelList() {
		return checkBoxVTailLinkedToPreviousPanelList;
	}

	public void setCheckBoxVTailLinkedToPreviousPanelList(List<CheckBox> checkBoxVTailLinkedToPreviousPanelList) {
		this.checkBoxVTailLinkedToPreviousPanelList = checkBoxVTailLinkedToPreviousPanelList;
	}

	public List<ChoiceBox<String>> getChoiceBoxVTailSpanPanelUnitList() {
		return choiceBoxVTailSpanPanelUnitList;
	}

	public void setChoiceBoxVTailSpanPanelUnitList(List<ChoiceBox<String>> choiceBoxVTailSpanPanelUnitList) {
		this.choiceBoxVTailSpanPanelUnitList = choiceBoxVTailSpanPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxVTailSweepLEPanelUnitList() {
		return choiceBoxVTailSweepLEPanelUnitList;
	}

	public void setChoiceBoxVTailSweepLEPanelUnitList(List<ChoiceBox<String>> choiceBoxVTailSweepLEPanelUnitList) {
		this.choiceBoxVTailSweepLEPanelUnitList = choiceBoxVTailSweepLEPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxVTailDihedralPanelUnitList() {
		return choiceBoxVTailDihedralPanelUnitList;
	}

	public void setChoiceBoxVTailDihedralPanelUnitList(List<ChoiceBox<String>> choiceBoxVTailDihedralPanelUnitList) {
		this.choiceBoxVTailDihedralPanelUnitList = choiceBoxVTailDihedralPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxVTailInnerChordPanelUnitList() {
		return choiceBoxVTailInnerChordPanelUnitList;
	}

	public void setChoiceBoxVTailInnerChordPanelUnitList(List<ChoiceBox<String>> choiceBoxVTailInnerChordPanelUnitList) {
		this.choiceBoxVTailInnerChordPanelUnitList = choiceBoxVTailInnerChordPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxVTailInnerTwistPanelUnitList() {
		return choiceBoxVTailInnerTwistPanelUnitList;
	}

	public void setChoiceBoxVTailInnerTwistPanelUnitList(List<ChoiceBox<String>> choiceBoxVTailInnerTwistPanelUnitList) {
		this.choiceBoxVTailInnerTwistPanelUnitList = choiceBoxVTailInnerTwistPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxVTailOuterChordPanelUnitList() {
		return choiceBoxVTailOuterChordPanelUnitList;
	}

	public void setChoiceBoxVTailOuterChordPanelUnitList(List<ChoiceBox<String>> choiceBoxVTailOuterChordPanelUnitList) {
		this.choiceBoxVTailOuterChordPanelUnitList = choiceBoxVTailOuterChordPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxVTailOuterTwistPanelUnitList() {
		return choiceBoxVTailOuterTwistPanelUnitList;
	}

	public void setChoiceBoxVTailOuterTwistPanelUnitList(List<ChoiceBox<String>> choiceBoxVTailOuterTwistPanelUnitList) {
		this.choiceBoxVTailOuterTwistPanelUnitList = choiceBoxVTailOuterTwistPanelUnitList;
	}

	public List<Button> getDetailButtonVTailInnerAirfoilList() {
		return detailButtonVTailInnerAirfoilList;
	}

	public void setDetailButtonVTailInnerAirfoilList(List<Button> detailButtonVTailInnerAirfoilList) {
		this.detailButtonVTailInnerAirfoilList = detailButtonVTailInnerAirfoilList;
	}

	public List<Button> getDetailButtonVTailOuterAirfoilList() {
		return detailButtonVTailOuterAirfoilList;
	}

	public void setDetailButtonVTailOuterAirfoilList(List<Button> detailButtonVTailOuterAirfoilList) {
		this.detailButtonVTailOuterAirfoilList = detailButtonVTailOuterAirfoilList;
	}

	public List<Button> getChooseInnerVTailAirfoilFileButtonList() {
		return chooseInnerVTailAirfoilFileButtonList;
	}

	public void setChooseInnerVTailAirfoilFileButtonList(List<Button> chooseInnerVTailAirfoilFileButtonList) {
		this.chooseInnerVTailAirfoilFileButtonList = chooseInnerVTailAirfoilFileButtonList;
	}

	public List<Button> getChooseOuterVTailAirfoilFileButtonList() {
		return chooseOuterVTailAirfoilFileButtonList;
	}

	public void setChooseOuterVTailAirfoilFileButtonList(List<Button> chooseOuterVTailAirfoilFileButtonList) {
		this.chooseOuterVTailAirfoilFileButtonList = chooseOuterVTailAirfoilFileButtonList;
	}

	public List<ChoiceBox<String>> getChoiceBoxVTailRudderTypeList() {
		return choiceBoxVTailRudderTypeList;
	}

	public void setChoiceBoxVTailRudderTypeList(List<ChoiceBox<String>> choiceBoxVTailRudderTypeList) {
		this.choiceBoxVTailRudderTypeList = choiceBoxVTailRudderTypeList;
	}

	public List<TextField> getTextFieldVTailInnerPositionRudderList() {
		return textFieldVTailInnerPositionRudderList;
	}

	public void setTextFieldVTailInnerPositionRudderList(List<TextField> textFieldVTailInnerPositionRudderList) {
		this.textFieldVTailInnerPositionRudderList = textFieldVTailInnerPositionRudderList;
	}

	public List<TextField> getTextFieldVTailOuterPositionRudderList() {
		return textFieldVTailOuterPositionRudderList;
	}

	public void setTextFieldVTailOuterPositionRudderList(List<TextField> textFieldVTailOuterPositionRudderList) {
		this.textFieldVTailOuterPositionRudderList = textFieldVTailOuterPositionRudderList;
	}

	public List<TextField> getTextFieldVTailInnerChordRatioRudderList() {
		return textFieldVTailInnerChordRatioRudderList;
	}

	public void setTextFieldVTailInnerChordRatioRudderList(List<TextField> textFieldVTailInnerChordRatioRudderList) {
		this.textFieldVTailInnerChordRatioRudderList = textFieldVTailInnerChordRatioRudderList;
	}

	public List<TextField> getTextFieldVTailOuterChordRatioRudderList() {
		return textFieldVTailOuterChordRatioRudderList;
	}

	public void setTextFieldVTailOuterChordRatioRudderList(List<TextField> textFieldVTailOuterChordRatioRudderList) {
		this.textFieldVTailOuterChordRatioRudderList = textFieldVTailOuterChordRatioRudderList;
	}

	public List<TextField> getTextFieldVTailMinimumDeflectionAngleRudderList() {
		return textFieldVTailMinimumDeflectionAngleRudderList;
	}

	public void setTextFieldVTailMinimumDeflectionAngleRudderList(
			List<TextField> textFieldVTailMinimumDeflectionAngleRudderList) {
		this.textFieldVTailMinimumDeflectionAngleRudderList = textFieldVTailMinimumDeflectionAngleRudderList;
	}

	public List<TextField> getTextFieldVTailMaximumDeflectionAngleRudderList() {
		return textFieldVTailMaximumDeflectionAngleRudderList;
	}

	public void setTextFieldVTailMaximumDeflectionAngleRudderList(
			List<TextField> textFieldVTailMaximumDeflectionAngleRudderList) {
		this.textFieldVTailMaximumDeflectionAngleRudderList = textFieldVTailMaximumDeflectionAngleRudderList;
	}

	public List<ChoiceBox<String>> getChoiceBoxVTailMinimumDeflectionAngleRudderUnitList() {
		return choiceBoxVTailMinimumDeflectionAngleRudderUnitList;
	}

	public void setChoiceBoxVTailMinimumDeflectionAngleRudderUnitList(
			List<ChoiceBox<String>> choiceBoxVTailMinimumDeflectionAngleRudderUnitList) {
		this.choiceBoxVTailMinimumDeflectionAngleRudderUnitList = choiceBoxVTailMinimumDeflectionAngleRudderUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxVTailMaximumDeflectionAngleRudderUnitList() {
		return choiceBoxVTailMaximumDeflectionAngleRudderUnitList;
	}

	public void setChoiceBoxVTailMaximumDeflectionAngleRudderUnitList(
			List<ChoiceBox<String>> choiceBoxVTailMaximumDeflectionAngleRudderUnitList) {
		this.choiceBoxVTailMaximumDeflectionAngleRudderUnitList = choiceBoxVTailMaximumDeflectionAngleRudderUnitList;
	}

	public ChoiceBox<String> getvTailRoughnessUnitChoiceBox() {
		return vTailRoughnessUnitChoiceBox;
	}

	public void setvTailRoughnessUnitChoiceBox(ChoiceBox<String> vTailRoughnessUnitChoiceBox) {
		this.vTailRoughnessUnitChoiceBox = vTailRoughnessUnitChoiceBox;
	}

	public ChoiceBox<String> getvTailSpanPanel1UnitChoiceBox() {
		return vTailSpanPanel1UnitChoiceBox;
	}

	public void setvTailSpanPanel1UnitChoiceBox(ChoiceBox<String> vTailSpanPanel1UnitChoiceBox) {
		this.vTailSpanPanel1UnitChoiceBox = vTailSpanPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getvTailSweepLEPanel1UnitChoiceBox() {
		return vTailSweepLEPanel1UnitChoiceBox;
	}

	public void setvTailSweepLEPanel1UnitChoiceBox(ChoiceBox<String> vTailSweepLEPanel1UnitChoiceBox) {
		this.vTailSweepLEPanel1UnitChoiceBox = vTailSweepLEPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getvTailDihedralPanel1UnitChoiceBox() {
		return vTailDihedralPanel1UnitChoiceBox;
	}

	public void setvTailDihedralPanel1UnitChoiceBox(ChoiceBox<String> vTailDihedralPanel1UnitChoiceBox) {
		this.vTailDihedralPanel1UnitChoiceBox = vTailDihedralPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getvTailInnerSectionChordPanel1UnitChoiceBox() {
		return vTailInnerSectionChordPanel1UnitChoiceBox;
	}

	public void setvTailInnerSectionChordPanel1UnitChoiceBox(ChoiceBox<String> vTailInnerSectionChordPanel1UnitChoiceBox) {
		this.vTailInnerSectionChordPanel1UnitChoiceBox = vTailInnerSectionChordPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getvTailInnerSectionTwistTipPanel1UnitChoiceBox() {
		return vTailInnerSectionTwistTipPanel1UnitChoiceBox;
	}

	public void setvTailInnerSectionTwistTipPanel1UnitChoiceBox(
			ChoiceBox<String> vTailInnerSectionTwistTipPanel1UnitChoiceBox) {
		this.vTailInnerSectionTwistTipPanel1UnitChoiceBox = vTailInnerSectionTwistTipPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getvTailOuterSectionChordPanel1UnitChoiceBox() {
		return vTailOuterSectionChordPanel1UnitChoiceBox;
	}

	public void setvTailOuterSectionChordPanel1UnitChoiceBox(ChoiceBox<String> vTailOuterSectionChordPanel1UnitChoiceBox) {
		this.vTailOuterSectionChordPanel1UnitChoiceBox = vTailOuterSectionChordPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getvTailOuterSectionTwistTipPanel1UnitChoiceBox() {
		return vTailOuterSectionTwistTipPanel1UnitChoiceBox;
	}

	public void setvTailOuterSectionTwistTipPanel1UnitChoiceBox(
			ChoiceBox<String> vTailOuterSectionTwistTipPanel1UnitChoiceBox) {
		this.vTailOuterSectionTwistTipPanel1UnitChoiceBox = vTailOuterSectionTwistTipPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getvTailMinimumDeflectionAngleRudder1UnitChoiceBox() {
		return vTailMinimumDeflectionAngleRudder1UnitChoiceBox;
	}

	public void setvTailMinimumDeflectionAngleRudder1UnitChoiceBox(
			ChoiceBox<String> vTailMinimumDeflectionAngleRudder1UnitChoiceBox) {
		this.vTailMinimumDeflectionAngleRudder1UnitChoiceBox = vTailMinimumDeflectionAngleRudder1UnitChoiceBox;
	}

	public ChoiceBox<String> getvTailMaximumDeflectionAngleRudder1UnitChoiceBox() {
		return vTailMaximumDeflectionAngleRudder1UnitChoiceBox;
	}

	public void setvTailMaximumDeflectionAngleRudder1UnitChoiceBox(
			ChoiceBox<String> vTailMaximumDeflectionAngleRudder1UnitChoiceBox) {
		this.vTailMaximumDeflectionAngleRudder1UnitChoiceBox = vTailMaximumDeflectionAngleRudder1UnitChoiceBox;
	}

	public ChoiceBox<String> getCanardAdjustCriterionChoiceBox() {
		return canardAdjustCriterionChoiceBox;
	}

	public void setCanardAdjustCriterionChoiceBox(ChoiceBox<String> canardAdjustCriterionChoiceBox) {
		this.canardAdjustCriterionChoiceBox = canardAdjustCriterionChoiceBox;
	}

	public TextField getTextFieldCanardRoughness() {
		return textFieldCanardRoughness;
	}

	public void setTextFieldCanardRoughness(TextField textFieldCanardRoughness) {
		this.textFieldCanardRoughness = textFieldCanardRoughness;
	}

	public TextField getTextFieldCanardSpanPanel1() {
		return textFieldCanardSpanPanel1;
	}

	public void setTextFieldCanardSpanPanel1(TextField textFieldCanardSpanPanel1) {
		this.textFieldCanardSpanPanel1 = textFieldCanardSpanPanel1;
	}

	public TextField getTextFieldCanardSweepLeadingEdgePanel1() {
		return textFieldCanardSweepLeadingEdgePanel1;
	}

	public void setTextFieldCanardSweepLeadingEdgePanel1(TextField textFieldCanardSweepLeadingEdgePanel1) {
		this.textFieldCanardSweepLeadingEdgePanel1 = textFieldCanardSweepLeadingEdgePanel1;
	}

	public TextField getTextFieldCanardDihedralPanel1() {
		return textFieldCanardDihedralPanel1;
	}

	public void setTextFieldCanardDihedralPanel1(TextField textFieldCanardDihedralPanel1) {
		this.textFieldCanardDihedralPanel1 = textFieldCanardDihedralPanel1;
	}

	public TextField getTextFieldCanardChordInnerSectionPanel1() {
		return textFieldCanardChordInnerSectionPanel1;
	}

	public void setTextFieldCanardChordInnerSectionPanel1(TextField textFieldCanardChordInnerSectionPanel1) {
		this.textFieldCanardChordInnerSectionPanel1 = textFieldCanardChordInnerSectionPanel1;
	}

	public TextField getTextFieldCanardTwistInnerSectionPanel1() {
		return textFieldCanardTwistInnerSectionPanel1;
	}

	public void setTextFieldCanardTwistInnerSectionPanel1(TextField textFieldCanardTwistInnerSectionPanel1) {
		this.textFieldCanardTwistInnerSectionPanel1 = textFieldCanardTwistInnerSectionPanel1;
	}

	public TextField getTextFieldCanardAirfoilPathInnerSectionPanel1() {
		return textFieldCanardAirfoilPathInnerSectionPanel1;
	}

	public void setTextFieldCanardAirfoilPathInnerSectionPanel1(TextField textFieldCanardAirfoilPathInnerSectionPanel1) {
		this.textFieldCanardAirfoilPathInnerSectionPanel1 = textFieldCanardAirfoilPathInnerSectionPanel1;
	}

	public TextField getTextFieldCanardChordOuterSectionPanel1() {
		return textFieldCanardChordOuterSectionPanel1;
	}

	public void setTextFieldCanardChordOuterSectionPanel1(TextField textFieldCanardChordOuterSectionPanel1) {
		this.textFieldCanardChordOuterSectionPanel1 = textFieldCanardChordOuterSectionPanel1;
	}

	public TextField getTextFieldCanardTwistOuterSectionPanel1() {
		return textFieldCanardTwistOuterSectionPanel1;
	}

	public void setTextFieldCanardTwistOuterSectionPanel1(TextField textFieldCanardTwistOuterSectionPanel1) {
		this.textFieldCanardTwistOuterSectionPanel1 = textFieldCanardTwistOuterSectionPanel1;
	}

	public TextField getTextFieldCanardAirfoilPathOuterSectionPanel1() {
		return textFieldCanardAirfoilPathOuterSectionPanel1;
	}

	public void setTextFieldCanardAirfoilPathOuterSectionPanel1(TextField textFieldCanardAirfoilPathOuterSectionPanel1) {
		this.textFieldCanardAirfoilPathOuterSectionPanel1 = textFieldCanardAirfoilPathOuterSectionPanel1;
	}

	public ChoiceBox<String> getCanardControlSurface1TypeChoiceBox() {
		return canardControlSurface1TypeChoiceBox;
	}

	public void setCanardControlSurface1TypeChoiceBox(ChoiceBox<String> canardControlSurface1TypeChoiceBox) {
		this.canardControlSurface1TypeChoiceBox = canardControlSurface1TypeChoiceBox;
	}

	public TextField getTextFieldCanardInnerPositionControlSurface1() {
		return textFieldCanardInnerPositionControlSurface1;
	}

	public void setTextFieldCanardInnerPositionControlSurface1(TextField textFieldCanardInnerPositionControlSurface1) {
		this.textFieldCanardInnerPositionControlSurface1 = textFieldCanardInnerPositionControlSurface1;
	}

	public TextField getTextFieldCanardOuterPositionControlSurface1() {
		return textFieldCanardOuterPositionControlSurface1;
	}

	public void setTextFieldCanardOuterPositionControlSurface1(TextField textFieldCanardOuterPositionControlSurface1) {
		this.textFieldCanardOuterPositionControlSurface1 = textFieldCanardOuterPositionControlSurface1;
	}

	public TextField getTextFieldCanardInnerChordRatioControlSurface1() {
		return textFieldCanardInnerChordRatioControlSurface1;
	}

	public void setTextFieldCanardInnerChordRatioControlSurface1(TextField textFieldCanardInnerChordRatioControlSurface1) {
		this.textFieldCanardInnerChordRatioControlSurface1 = textFieldCanardInnerChordRatioControlSurface1;
	}

	public TextField getTextFieldCanardOuterChordRatioControlSurface1() {
		return textFieldCanardOuterChordRatioControlSurface1;
	}

	public void setTextFieldCanardOuterChordRatioControlSurface1(TextField textFieldCanardOuterChordRatioControlSurface1) {
		this.textFieldCanardOuterChordRatioControlSurface1 = textFieldCanardOuterChordRatioControlSurface1;
	}

	public TextField getTextFieldCanardMinimumDeflectionAngleControlSurface1() {
		return textFieldCanardMinimumDeflectionAngleControlSurface1;
	}

	public void setTextFieldCanardMinimumDeflectionAngleControlSurface1(
			TextField textFieldCanardMinimumDeflectionAngleControlSurface1) {
		this.textFieldCanardMinimumDeflectionAngleControlSurface1 = textFieldCanardMinimumDeflectionAngleControlSurface1;
	}

	public TextField getTextFieldCanardMaximumDeflectionAngleControlSurface1() {
		return textFieldCanardMaximumDeflectionAngleControlSurface1;
	}

	public void setTextFieldCanardMaximumDeflectionAngleControlSurface1(
			TextField textFieldCanardMaximumDeflectionAngleControlSurface1) {
		this.textFieldCanardMaximumDeflectionAngleControlSurface1 = textFieldCanardMaximumDeflectionAngleControlSurface1;
	}

	public List<TextField> getTextFieldCanardSpanPanelList() {
		return textFieldCanardSpanPanelList;
	}

	public void setTextFieldCanardSpanPanelList(List<TextField> textFieldCanardSpanPanelList) {
		this.textFieldCanardSpanPanelList = textFieldCanardSpanPanelList;
	}

	public List<TextField> getTextFieldCanardSweepLEPanelList() {
		return textFieldCanardSweepLEPanelList;
	}

	public void setTextFieldCanardSweepLEPanelList(List<TextField> textFieldCanardSweepLEPanelList) {
		this.textFieldCanardSweepLEPanelList = textFieldCanardSweepLEPanelList;
	}

	public List<TextField> getTextFieldCanardDihedralPanelList() {
		return textFieldCanardDihedralPanelList;
	}

	public void setTextFieldCanardDihedralPanelList(List<TextField> textFieldCanardDihedralPanelList) {
		this.textFieldCanardDihedralPanelList = textFieldCanardDihedralPanelList;
	}

	public List<TextField> getTextFieldCanardInnerChordPanelList() {
		return textFieldCanardInnerChordPanelList;
	}

	public void setTextFieldCanardInnerChordPanelList(List<TextField> textFieldCanardInnerChordPanelList) {
		this.textFieldCanardInnerChordPanelList = textFieldCanardInnerChordPanelList;
	}

	public List<TextField> getTextFieldCanardInnerTwistPanelList() {
		return textFieldCanardInnerTwistPanelList;
	}

	public void setTextFieldCanardInnerTwistPanelList(List<TextField> textFieldCanardInnerTwistPanelList) {
		this.textFieldCanardInnerTwistPanelList = textFieldCanardInnerTwistPanelList;
	}

	public List<TextField> getTextFieldCanardInnerAirfoilPanelList() {
		return textFieldCanardInnerAirfoilPanelList;
	}

	public void setTextFieldCanardInnerAirfoilPanelList(List<TextField> textFieldCanardInnerAirfoilPanelList) {
		this.textFieldCanardInnerAirfoilPanelList = textFieldCanardInnerAirfoilPanelList;
	}

	public List<TextField> getTextFieldCanardOuterChordPanelList() {
		return textFieldCanardOuterChordPanelList;
	}

	public void setTextFieldCanardOuterChordPanelList(List<TextField> textFieldCanardOuterChordPanelList) {
		this.textFieldCanardOuterChordPanelList = textFieldCanardOuterChordPanelList;
	}

	public List<TextField> getTextFieldCanardOuterTwistPanelList() {
		return textFieldCanardOuterTwistPanelList;
	}

	public void setTextFieldCanardOuterTwistPanelList(List<TextField> textFieldCanardOuterTwistPanelList) {
		this.textFieldCanardOuterTwistPanelList = textFieldCanardOuterTwistPanelList;
	}

	public List<TextField> getTextFieldCanardOuterAirfoilPanelList() {
		return textFieldCanardOuterAirfoilPanelList;
	}

	public void setTextFieldCanardOuterAirfoilPanelList(List<TextField> textFieldCanardOuterAirfoilPanelList) {
		this.textFieldCanardOuterAirfoilPanelList = textFieldCanardOuterAirfoilPanelList;
	}

	public List<CheckBox> getCheckBoxCanardLinkedToPreviousPanelList() {
		return checkBoxCanardLinkedToPreviousPanelList;
	}

	public void setCheckBoxCanardLinkedToPreviousPanelList(List<CheckBox> checkBoxCanardLinkedToPreviousPanelList) {
		this.checkBoxCanardLinkedToPreviousPanelList = checkBoxCanardLinkedToPreviousPanelList;
	}

	public List<ChoiceBox<String>> getChoiceBoxCanardSpanPanelUnitList() {
		return choiceBoxCanardSpanPanelUnitList;
	}

	public void setChoiceBoxCanardSpanPanelUnitList(List<ChoiceBox<String>> choiceBoxCanardSpanPanelUnitList) {
		this.choiceBoxCanardSpanPanelUnitList = choiceBoxCanardSpanPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxCanardSweepLEPanelUnitList() {
		return choiceBoxCanardSweepLEPanelUnitList;
	}

	public void setChoiceBoxCanardSweepLEPanelUnitList(List<ChoiceBox<String>> choiceBoxCanardSweepLEPanelUnitList) {
		this.choiceBoxCanardSweepLEPanelUnitList = choiceBoxCanardSweepLEPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxCanardDihedralPanelUnitList() {
		return choiceBoxCanardDihedralPanelUnitList;
	}

	public void setChoiceBoxCanardDihedralPanelUnitList(List<ChoiceBox<String>> choiceBoxCanardDihedralPanelUnitList) {
		this.choiceBoxCanardDihedralPanelUnitList = choiceBoxCanardDihedralPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxCanardInnerChordPanelUnitList() {
		return choiceBoxCanardInnerChordPanelUnitList;
	}

	public void setChoiceBoxCanardInnerChordPanelUnitList(List<ChoiceBox<String>> choiceBoxCanardInnerChordPanelUnitList) {
		this.choiceBoxCanardInnerChordPanelUnitList = choiceBoxCanardInnerChordPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxCanardInnerTwistPanelUnitList() {
		return choiceBoxCanardInnerTwistPanelUnitList;
	}

	public void setChoiceBoxCanardInnerTwistPanelUnitList(List<ChoiceBox<String>> choiceBoxCanardInnerTwistPanelUnitList) {
		this.choiceBoxCanardInnerTwistPanelUnitList = choiceBoxCanardInnerTwistPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxCanardOuterChordPanelUnitList() {
		return choiceBoxCanardOuterChordPanelUnitList;
	}

	public void setChoiceBoxCanardOuterChordPanelUnitList(List<ChoiceBox<String>> choiceBoxCanardOuterChordPanelUnitList) {
		this.choiceBoxCanardOuterChordPanelUnitList = choiceBoxCanardOuterChordPanelUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxCanardOuterTwistPanelUnitList() {
		return choiceBoxCanardOuterTwistPanelUnitList;
	}

	public void setChoiceBoxCanardOuterTwistPanelUnitList(List<ChoiceBox<String>> choiceBoxCanardOuterTwistPanelUnitList) {
		this.choiceBoxCanardOuterTwistPanelUnitList = choiceBoxCanardOuterTwistPanelUnitList;
	}

	public List<Button> getDetailButtonCanardInnerAirfoilList() {
		return detailButtonCanardInnerAirfoilList;
	}

	public void setDetailButtonCanardInnerAirfoilList(List<Button> detailButtonCanardInnerAirfoilList) {
		this.detailButtonCanardInnerAirfoilList = detailButtonCanardInnerAirfoilList;
	}

	public List<Button> getDetailButtonCanardOuterAirfoilList() {
		return detailButtonCanardOuterAirfoilList;
	}

	public void setDetailButtonCanardOuterAirfoilList(List<Button> detailButtonCanardOuterAirfoilList) {
		this.detailButtonCanardOuterAirfoilList = detailButtonCanardOuterAirfoilList;
	}

	public List<Button> getChooseInnerCanardAirfoilFileButtonList() {
		return chooseInnerCanardAirfoilFileButtonList;
	}

	public void setChooseInnerCanardAirfoilFileButtonList(List<Button> chooseInnerCanardAirfoilFileButtonList) {
		this.chooseInnerCanardAirfoilFileButtonList = chooseInnerCanardAirfoilFileButtonList;
	}

	public List<Button> getChooseOuterCanardAirfoilFileButtonList() {
		return chooseOuterCanardAirfoilFileButtonList;
	}

	public void setChooseOuterCanardAirfoilFileButtonList(List<Button> chooseOuterCanardAirfoilFileButtonList) {
		this.chooseOuterCanardAirfoilFileButtonList = chooseOuterCanardAirfoilFileButtonList;
	}

	public List<ChoiceBox<String>> getChoiceBoxCanardControlSurfaceTypeList() {
		return choiceBoxCanardControlSurfaceTypeList;
	}

	public void setChoiceBoxCanardControlSurfaceTypeList(List<ChoiceBox<String>> choiceBoxCanardControlSurfaceTypeList) {
		this.choiceBoxCanardControlSurfaceTypeList = choiceBoxCanardControlSurfaceTypeList;
	}

	public List<TextField> getTextFieldCanardInnerPositionControlSurfaceList() {
		return textFieldCanardInnerPositionControlSurfaceList;
	}

	public void setTextFieldCanardInnerPositionControlSurfaceList(
			List<TextField> textFieldCanardInnerPositionControlSurfaceList) {
		this.textFieldCanardInnerPositionControlSurfaceList = textFieldCanardInnerPositionControlSurfaceList;
	}

	public List<TextField> getTextFieldCanardOuterPositionControlSurfaceList() {
		return textFieldCanardOuterPositionControlSurfaceList;
	}

	public void setTextFieldCanardOuterPositionControlSurfaceList(
			List<TextField> textFieldCanardOuterPositionControlSurfaceList) {
		this.textFieldCanardOuterPositionControlSurfaceList = textFieldCanardOuterPositionControlSurfaceList;
	}

	public List<TextField> getTextFieldCanardInnerChordRatioControlSurfaceList() {
		return textFieldCanardInnerChordRatioControlSurfaceList;
	}

	public void setTextFieldCanardInnerChordRatioControlSurfaceList(
			List<TextField> textFieldCanardInnerChordRatioControlSurfaceList) {
		this.textFieldCanardInnerChordRatioControlSurfaceList = textFieldCanardInnerChordRatioControlSurfaceList;
	}

	public List<TextField> getTextFieldCanardOuterChordRatioControlSurfaceList() {
		return textFieldCanardOuterChordRatioControlSurfaceList;
	}

	public void setTextFieldCanardOuterChordRatioControlSurfaceList(
			List<TextField> textFieldCanardOuterChordRatioControlSurfaceList) {
		this.textFieldCanardOuterChordRatioControlSurfaceList = textFieldCanardOuterChordRatioControlSurfaceList;
	}

	public List<TextField> getTextFieldCanardMinimumDeflectionAngleControlSurfaceList() {
		return textFieldCanardMinimumDeflectionAngleControlSurfaceList;
	}

	public void setTextFieldCanardMinimumDeflectionAngleControlSurfaceList(
			List<TextField> textFieldCanardMinimumDeflectionAngleControlSurfaceList) {
		this.textFieldCanardMinimumDeflectionAngleControlSurfaceList = textFieldCanardMinimumDeflectionAngleControlSurfaceList;
	}

	public List<TextField> getTextFieldCanardMaximumDeflectionAngleControlSurfaceList() {
		return textFieldCanardMaximumDeflectionAngleControlSurfaceList;
	}

	public void setTextFieldCanardMaximumDeflectionAngleControlSurfaceList(
			List<TextField> textFieldCanardMaximumDeflectionAngleControlSurfaceList) {
		this.textFieldCanardMaximumDeflectionAngleControlSurfaceList = textFieldCanardMaximumDeflectionAngleControlSurfaceList;
	}

	public List<ChoiceBox<String>> getChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList() {
		return choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList;
	}

	public void setChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList(
			List<ChoiceBox<String>> choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList) {
		this.choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList = choiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList() {
		return choiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList;
	}

	public void setChoiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList(
			List<ChoiceBox<String>> choiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList) {
		this.choiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList = choiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList;
	}

	public ChoiceBox<String> getCanardRoughnessUnitChoiceBox() {
		return canardRoughnessUnitChoiceBox;
	}

	public void setCanardRoughnessUnitChoiceBox(ChoiceBox<String> canardRoughnessUnitChoiceBox) {
		this.canardRoughnessUnitChoiceBox = canardRoughnessUnitChoiceBox;
	}

	public ChoiceBox<String> getCanardSpanPanel1UnitChoiceBox() {
		return canardSpanPanel1UnitChoiceBox;
	}

	public void setCanardSpanPanel1UnitChoiceBox(ChoiceBox<String> canardSpanPanel1UnitChoiceBox) {
		this.canardSpanPanel1UnitChoiceBox = canardSpanPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getCanardSweepLEPanel1UnitChoiceBox() {
		return canardSweepLEPanel1UnitChoiceBox;
	}

	public void setCanardSweepLEPanel1UnitChoiceBox(ChoiceBox<String> canardSweepLEPanel1UnitChoiceBox) {
		this.canardSweepLEPanel1UnitChoiceBox = canardSweepLEPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getCanardDihedralPanel1UnitChoiceBox() {
		return canardDihedralPanel1UnitChoiceBox;
	}

	public void setCanardDihedralPanel1UnitChoiceBox(ChoiceBox<String> canardDihedralPanel1UnitChoiceBox) {
		this.canardDihedralPanel1UnitChoiceBox = canardDihedralPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getCanardInnerSectionChordPanel1UnitChoiceBox() {
		return canardInnerSectionChordPanel1UnitChoiceBox;
	}

	public void setCanardInnerSectionChordPanel1UnitChoiceBox(
			ChoiceBox<String> canardInnerSectionChordPanel1UnitChoiceBox) {
		this.canardInnerSectionChordPanel1UnitChoiceBox = canardInnerSectionChordPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getCanardInnerSectionTwistTipPanel1UnitChoiceBox() {
		return canardInnerSectionTwistTipPanel1UnitChoiceBox;
	}

	public void setCanardInnerSectionTwistTipPanel1UnitChoiceBox(
			ChoiceBox<String> canardInnerSectionTwistTipPanel1UnitChoiceBox) {
		this.canardInnerSectionTwistTipPanel1UnitChoiceBox = canardInnerSectionTwistTipPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getCanardOuterSectionChordPanel1UnitChoiceBox() {
		return canardOuterSectionChordPanel1UnitChoiceBox;
	}

	public void setCanardOuterSectionChordPanel1UnitChoiceBox(
			ChoiceBox<String> canardOuterSectionChordPanel1UnitChoiceBox) {
		this.canardOuterSectionChordPanel1UnitChoiceBox = canardOuterSectionChordPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getCanardOuterSectionTwistTipPanel1UnitChoiceBox() {
		return canardOuterSectionTwistTipPanel1UnitChoiceBox;
	}

	public void setCanardOuterSectionTwistTipPanel1UnitChoiceBox(
			ChoiceBox<String> canardOuterSectionTwistTipPanel1UnitChoiceBox) {
		this.canardOuterSectionTwistTipPanel1UnitChoiceBox = canardOuterSectionTwistTipPanel1UnitChoiceBox;
	}

	public ChoiceBox<String> getCanardMinimumDeflectionAngleControlSurface1UnitChoiceBox() {
		return canardMinimumDeflectionAngleControlSurface1UnitChoiceBox;
	}

	public void setCanardMinimumDeflectionAngleControlSurface1UnitChoiceBox(
			ChoiceBox<String> canardMinimumDeflectionAngleControlSurface1UnitChoiceBox) {
		this.canardMinimumDeflectionAngleControlSurface1UnitChoiceBox = canardMinimumDeflectionAngleControlSurface1UnitChoiceBox;
	}

	public ChoiceBox<String> getCanardMaximumDeflectionAngleControlSurface1UnitChoiceBox() {
		return canardMaximumDeflectionAngleControlSurface1UnitChoiceBox;
	}

	public void setCanardMaximumDeflectionAngleControlSurface1UnitChoiceBox(
			ChoiceBox<String> canardMaximumDeflectionAngleControlSurface1UnitChoiceBox) {
		this.canardMaximumDeflectionAngleControlSurface1UnitChoiceBox = canardMaximumDeflectionAngleControlSurface1UnitChoiceBox;
	}

	public TextField getTextFieldNacelleRoughness1() {
		return textFieldNacelleRoughness1;
	}

	public void setTextFieldNacelleRoughness1(TextField textFieldNacelleRoughness1) {
		this.textFieldNacelleRoughness1 = textFieldNacelleRoughness1;
	}

	public TextField getTextFieldNacelleLength1() {
		return textFieldNacelleLength1;
	}

	public void setTextFieldNacelleLength1(TextField textFieldNacelleLength1) {
		this.textFieldNacelleLength1 = textFieldNacelleLength1;
	}

	public TextField getTextFieldNacelleMaximumDiameter1() {
		return textFieldNacelleMaximumDiameter1;
	}

	public void setTextFieldNacelleMaximumDiameter1(TextField textFieldNacelleMaximumDiameter1) {
		this.textFieldNacelleMaximumDiameter1 = textFieldNacelleMaximumDiameter1;
	}

	public TextField getTextFieldNacelleKInlet1() {
		return textFieldNacelleKInlet1;
	}

	public void setTextFieldNacelleKInlet1(TextField textFieldNacelleKInlet1) {
		this.textFieldNacelleKInlet1 = textFieldNacelleKInlet1;
	}

	public TextField getTextFieldNacelleKOutlet1() {
		return textFieldNacelleKOutlet1;
	}

	public void setTextFieldNacelleKOutlet1(TextField textFieldNacelleKOutlet1) {
		this.textFieldNacelleKOutlet1 = textFieldNacelleKOutlet1;
	}

	public TextField getTextFieldNacelleKLength1() {
		return textFieldNacelleKLength1;
	}

	public void setTextFieldNacelleKLength1(TextField textFieldNacelleKLength1) {
		this.textFieldNacelleKLength1 = textFieldNacelleKLength1;
	}

	public TextField getTextFieldNacelleKDiameterOutlet1() {
		return textFieldNacelleKDiameterOutlet1;
	}

	public void setTextFieldNacelleKDiameterOutlet1(TextField textFieldNacelleKDiameterOutlet1) {
		this.textFieldNacelleKDiameterOutlet1 = textFieldNacelleKDiameterOutlet1;
	}

	public List<TextField> getTextFieldNacelleRoughnessList() {
		return textFieldNacelleRoughnessList;
	}

	public void setTextFieldNacelleRoughnessList(List<TextField> textFieldNacelleRoughnessList) {
		this.textFieldNacelleRoughnessList = textFieldNacelleRoughnessList;
	}

	public List<TextField> getTextFieldNacelleLengthList() {
		return textFieldNacelleLengthList;
	}

	public void setTextFieldNacelleLengthList(List<TextField> textFieldNacelleLengthList) {
		this.textFieldNacelleLengthList = textFieldNacelleLengthList;
	}

	public List<TextField> getTextFieldNacelleMaximumDiameterList() {
		return textFieldNacelleMaximumDiameterList;
	}

	public void setTextFieldNacelleMaximumDiameterList(List<TextField> textFieldNacelleMaximumDiameterList) {
		this.textFieldNacelleMaximumDiameterList = textFieldNacelleMaximumDiameterList;
	}

	public List<TextField> getTextFieldNacelleKInletList() {
		return textFieldNacelleKInletList;
	}

	public void setTextFieldNacelleKInletList(List<TextField> textFieldNacelleKInletList) {
		this.textFieldNacelleKInletList = textFieldNacelleKInletList;
	}

	public List<TextField> getTextFieldNacelleKOutletList() {
		return textFieldNacelleKOutletList;
	}

	public void setTextFieldNacelleKOutletList(List<TextField> textFieldNacelleKOutletList) {
		this.textFieldNacelleKOutletList = textFieldNacelleKOutletList;
	}

	public List<TextField> getTextFieldNacelleKLengthList() {
		return textFieldNacelleKLengthList;
	}

	public void setTextFieldNacelleKLengthList(List<TextField> textFieldNacelleKLengthList) {
		this.textFieldNacelleKLengthList = textFieldNacelleKLengthList;
	}

	public List<TextField> getTextFieldNacelleKDiameterOutletList() {
		return textFieldNacelleKDiameterOutletList;
	}

	public void setTextFieldNacelleKDiameterOutletList(List<TextField> textFieldNacelleKDiameterOutletList) {
		this.textFieldNacelleKDiameterOutletList = textFieldNacelleKDiameterOutletList;
	}

	public List<ChoiceBox<String>> getChoiceBoxNacelleRoughnessUnitList() {
		return choiceBoxNacelleRoughnessUnitList;
	}

	public void setChoiceBoxNacelleRoughnessUnitList(List<ChoiceBox<String>> choiceBoxNacelleRoughnessUnitList) {
		this.choiceBoxNacelleRoughnessUnitList = choiceBoxNacelleRoughnessUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxNacelleLengthUnitList() {
		return choiceBoxNacelleLengthUnitList;
	}

	public void setChoiceBoxNacelleLengthUnitList(List<ChoiceBox<String>> choiceBoxNacelleLengthUnitList) {
		this.choiceBoxNacelleLengthUnitList = choiceBoxNacelleLengthUnitList;
	}

	public List<ChoiceBox<String>> getChoiceBoxNacelleMaximumDiameterUnitList() {
		return choiceBoxNacelleMaximumDiameterUnitList;
	}

	public void setChoiceBoxNacelleMaximumDiameterUnitList(
			List<ChoiceBox<String>> choiceBoxNacelleMaximumDiameterUnitList) {
		this.choiceBoxNacelleMaximumDiameterUnitList = choiceBoxNacelleMaximumDiameterUnitList;
	}

	public List<Button> getNacelleEstimateDimesnsionButtonList() {
		return nacelleEstimateDimesnsionButtonList;
	}

	public void setNacelleEstimateDimesnsionButtonList(List<Button> nacelleEstimateDimesnsionButtonList) {
		this.nacelleEstimateDimesnsionButtonList = nacelleEstimateDimesnsionButtonList;
	}

	public List<Button> getNacelleKInletInfoButtonList() {
		return nacelleKInletInfoButtonList;
	}

	public void setNacelleKInletInfoButtonList(List<Button> nacelleKInletInfoButtonList) {
		this.nacelleKInletInfoButtonList = nacelleKInletInfoButtonList;
	}

	public List<Button> getNacelleKOutletInfoButtonList() {
		return nacelleKOutletInfoButtonList;
	}

	public void setNacelleKOutletInfoButtonList(List<Button> nacelleKOutletInfoButtonList) {
		this.nacelleKOutletInfoButtonList = nacelleKOutletInfoButtonList;
	}

	public List<Button> getNacelleKLengthInfoButtonList() {
		return nacelleKLengthInfoButtonList;
	}

	public void setNacelleKLengthInfoButtonList(List<Button> nacelleKLengthInfoButtonList) {
		this.nacelleKLengthInfoButtonList = nacelleKLengthInfoButtonList;
	}

	public List<Button> getNacelleKDiameterOutletInfoButtonList() {
		return nacelleKDiameterOutletInfoButtonList;
	}

	public void setNacelleKDiameterOutletInfoButtonList(List<Button> nacelleKDiameterOutletInfoButtonList) {
		this.nacelleKDiameterOutletInfoButtonList = nacelleKDiameterOutletInfoButtonList;
	}

	public List<Pane> getNacelleTopViewPaneList() {
		return nacelleTopViewPaneList;
	}

	public void setNacelleTopViewPaneList(List<Pane> nacelleTopViewPaneList) {
		this.nacelleTopViewPaneList = nacelleTopViewPaneList;
	}

	public List<Pane> getNacelleSideViewPaneList() {
		return nacelleSideViewPaneList;
	}

	public void setNacelleSideViewPaneList(List<Pane> nacelleSideViewPaneList) {
		this.nacelleSideViewPaneList = nacelleSideViewPaneList;
	}

	public List<Pane> getNacelleFrontViewPaneList() {
		return nacelleFrontViewPaneList;
	}

	public void setNacelleFrontViewPaneList(List<Pane> nacelleFrontViewPaneList) {
		this.nacelleFrontViewPaneList = nacelleFrontViewPaneList;
	}

	public ChoiceBox<String> getNacelleRoughnessUnitChoiceBox1() {
		return nacelleRoughnessUnitChoiceBox1;
	}

	public void setNacelleRoughnessUnitChoiceBox1(ChoiceBox<String> nacelleRoughnessUnitChoiceBox1) {
		this.nacelleRoughnessUnitChoiceBox1 = nacelleRoughnessUnitChoiceBox1;
	}

	public ChoiceBox<String> getNacelleLengthUnitChoiceBox1() {
		return nacelleLengthUnitChoiceBox1;
	}

	public void setNacelleLengthUnitChoiceBox1(ChoiceBox<String> nacelleLengthUnitChoiceBox1) {
		this.nacelleLengthUnitChoiceBox1 = nacelleLengthUnitChoiceBox1;
	}

	public ChoiceBox<String> getNacelleMaximumDiameterUnitChoiceBox1() {
		return nacelleMaximumDiameterUnitChoiceBox1;
	}

	public void setNacelleMaximumDiameterUnitChoiceBox1(ChoiceBox<String> nacelleMaximumDiameterUnitChoiceBox1) {
		this.nacelleMaximumDiameterUnitChoiceBox1 = nacelleMaximumDiameterUnitChoiceBox1;
	}

	public RadioButton getPowerPlantJetRadioButton1() {
		return powerPlantJetRadioButton1;
	}

	public void setPowerPlantJetRadioButton1(RadioButton powerPlantJetRadioButton1) {
		this.powerPlantJetRadioButton1 = powerPlantJetRadioButton1;
	}

	public RadioButton getPowerPlantTurbopropRadioButton1() {
		return powerPlantTurbopropRadioButton1;
	}

	public void setPowerPlantTurbopropRadioButton1(RadioButton powerPlantTurbopropRadioButton1) {
		this.powerPlantTurbopropRadioButton1 = powerPlantTurbopropRadioButton1;
	}

	public RadioButton getPowerPlantPistonRadioButton1() {
		return powerPlantPistonRadioButton1;
	}

	public void setPowerPlantPistonRadioButton1(RadioButton powerPlantPistonRadioButton1) {
		this.powerPlantPistonRadioButton1 = powerPlantPistonRadioButton1;
	}

	public ToggleGroup getPowerPlantToggleGroup1() {
		return powerPlantToggleGroup1;
	}

	public void setPowerPlantToggleGroup1(ToggleGroup powerPlantToggleGroup1) {
		this.powerPlantToggleGroup1 = powerPlantToggleGroup1;
	}

	public List<RadioButton> getPowerPlantJetRadioButtonList() {
		return powerPlantJetRadioButtonList;
	}

	public void setPowerPlantJetRadioButtonList(List<RadioButton> powerPlantJetRadioButtonList) {
		this.powerPlantJetRadioButtonList = powerPlantJetRadioButtonList;
	}

	public List<RadioButton> getPowerPlantTurbopropRadioButtonList() {
		return powerPlantTurbopropRadioButtonList;
	}

	public void setPowerPlantTurbopropRadioButtonList(List<RadioButton> powerPlantTurbopropRadioButtonList) {
		this.powerPlantTurbopropRadioButtonList = powerPlantTurbopropRadioButtonList;
	}

	public List<RadioButton> getPowerPlantPistonRadioButtonList() {
		return powerPlantPistonRadioButtonList;
	}

	public void setPowerPlantPistonRadioButtonList(List<RadioButton> powerPlantPistonRadioButtonList) {
		this.powerPlantPistonRadioButtonList = powerPlantPistonRadioButtonList;
	}

	public List<ToggleGroup> getPowerPlantToggleGropuList() {
		return powerPlantToggleGropuList;
	}

	public void setPowerPlantToggleGropuList(List<ToggleGroup> powerPlantToggleGropuList) {
		this.powerPlantToggleGropuList = powerPlantToggleGropuList;
	}

	public Map<Integer, ChoiceBox<String>> getEngineTurbojetTurbofanTypeChoiceBoxMap() {
		return engineTurbojetTurbofanTypeChoiceBoxMap;
	}

	public void setEngineTurbojetTurbofanTypeChoiceBoxMap(
			Map<Integer, ChoiceBox<String>> engineTurbojetTurbofanTypeChoiceBoxMap) {
		this.engineTurbojetTurbofanTypeChoiceBoxMap = engineTurbojetTurbofanTypeChoiceBoxMap;
	}

	public Map<Integer, TextField> getEngineTurbojetTurbofanDatabaseTextFieldMap() {
		return engineTurbojetTurbofanDatabaseTextFieldMap;
	}

	public void setEngineTurbojetTurbofanDatabaseTextFieldMap(
			Map<Integer, TextField> engineTurbojetTurbofanDatabaseTextFieldMap) {
		this.engineTurbojetTurbofanDatabaseTextFieldMap = engineTurbojetTurbofanDatabaseTextFieldMap;
	}

	public Map<Integer, TextField> getEngineTurbojetTurbofanLengthTextFieldMap() {
		return engineTurbojetTurbofanLengthTextFieldMap;
	}

	public void setEngineTurbojetTurbofanLengthTextFieldMap(
			Map<Integer, TextField> engineTurbojetTurbofanLengthTextFieldMap) {
		this.engineTurbojetTurbofanLengthTextFieldMap = engineTurbojetTurbofanLengthTextFieldMap;
	}

	public Map<Integer, ChoiceBox<String>> getEngineTurbojetTurbofanLengthUnitChoiceBoxMap() {
		return engineTurbojetTurbofanLengthUnitChoiceBoxMap;
	}

	public void setEngineTurbojetTurbofanLengthUnitChoiceBoxMap(
			Map<Integer, ChoiceBox<String>> engineTurbojetTurbofanLengthUnitChoiceBoxMap) {
		this.engineTurbojetTurbofanLengthUnitChoiceBoxMap = engineTurbojetTurbofanLengthUnitChoiceBoxMap;
	}

	public Map<Integer, TextField> getEngineTurbojetTurbofanDryMassTextFieldMap() {
		return engineTurbojetTurbofanDryMassTextFieldMap;
	}

	public void setEngineTurbojetTurbofanDryMassTextFieldMap(
			Map<Integer, TextField> engineTurbojetTurbofanDryMassTextFieldMap) {
		this.engineTurbojetTurbofanDryMassTextFieldMap = engineTurbojetTurbofanDryMassTextFieldMap;
	}

	public Map<Integer, ChoiceBox<String>> getEngineTurbojetTurbofanDryMassUnitChoiceBoxMap() {
		return engineTurbojetTurbofanDryMassUnitChoiceBoxMap;
	}

	public void setEngineTurbojetTurbofanDryMassUnitChoiceBoxMap(
			Map<Integer, ChoiceBox<String>> engineTurbojetTurbofanDryMassUnitChoiceBoxMap) {
		this.engineTurbojetTurbofanDryMassUnitChoiceBoxMap = engineTurbojetTurbofanDryMassUnitChoiceBoxMap;
	}

	public Map<Integer, TextField> getEngineTurbojetTurbofanStaticThrustTextFieldMap() {
		return engineTurbojetTurbofanStaticThrustTextFieldMap;
	}

	public void setEngineTurbojetTurbofanStaticThrustTextFieldMap(
			Map<Integer, TextField> engineTurbojetTurbofanStaticThrustTextFieldMap) {
		this.engineTurbojetTurbofanStaticThrustTextFieldMap = engineTurbojetTurbofanStaticThrustTextFieldMap;
	}

	public Map<Integer, ChoiceBox<String>> getEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap() {
		return engineTurbojetTurbofanStaticThrustUnitChoiceBoxMap;
	}

	public void setEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap(
			Map<Integer, ChoiceBox<String>> engineTurbojetTurbofanStaticThrustUnitChoiceBoxMap) {
		this.engineTurbojetTurbofanStaticThrustUnitChoiceBoxMap = engineTurbojetTurbofanStaticThrustUnitChoiceBoxMap;
	}

	public Map<Integer, TextField> getEngineTurbojetTurbofanBPRTextFieldMap() {
		return engineTurbojetTurbofanBPRTextFieldMap;
	}

	public void setEngineTurbojetTurbofanBPRTextFieldMap(Map<Integer, TextField> engineTurbojetTurbofanBPRTextFieldMap) {
		this.engineTurbojetTurbofanBPRTextFieldMap = engineTurbojetTurbofanBPRTextFieldMap;
	}

	public Map<Integer, TextField> getEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap() {
		return engineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap;
	}

	public void setEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap(
			Map<Integer, TextField> engineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap) {
		this.engineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap = engineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap;
	}

	public Map<Integer, TextField> getEngineTurbojetTurbofanNumberOfShaftsTextFieldMap() {
		return engineTurbojetTurbofanNumberOfShaftsTextFieldMap;
	}

	public void setEngineTurbojetTurbofanNumberOfShaftsTextFieldMap(
			Map<Integer, TextField> engineTurbojetTurbofanNumberOfShaftsTextFieldMap) {
		this.engineTurbojetTurbofanNumberOfShaftsTextFieldMap = engineTurbojetTurbofanNumberOfShaftsTextFieldMap;
	}

	public Map<Integer, TextField> getEngineTurbojetTurbofanOverallPressureRatioTextFieldMap() {
		return engineTurbojetTurbofanOverallPressureRatioTextFieldMap;
	}

	public void setEngineTurbojetTurbofanOverallPressureRatioTextFieldMap(
			Map<Integer, TextField> engineTurbojetTurbofanOverallPressureRatioTextFieldMap) {
		this.engineTurbojetTurbofanOverallPressureRatioTextFieldMap = engineTurbojetTurbofanOverallPressureRatioTextFieldMap;
	}

	public Map<Integer, ChoiceBox<String>> getEngineTurbopropTypeChoiceBoxMap() {
		return engineTurbopropTypeChoiceBoxMap;
	}

	public void setEngineTurbopropTypeChoiceBoxMap(Map<Integer, ChoiceBox<String>> engineTurbopropTypeChoiceBoxMap) {
		this.engineTurbopropTypeChoiceBoxMap = engineTurbopropTypeChoiceBoxMap;
	}

	public Map<Integer, TextField> getEngineTurbopropDatabaseTextFieldMap() {
		return engineTurbopropDatabaseTextFieldMap;
	}

	public void setEngineTurbopropDatabaseTextFieldMap(Map<Integer, TextField> engineTurbopropDatabaseTextFieldMap) {
		this.engineTurbopropDatabaseTextFieldMap = engineTurbopropDatabaseTextFieldMap;
	}

	public Map<Integer, TextField> getEngineTurbopropLengthTextFieldMap() {
		return engineTurbopropLengthTextFieldMap;
	}

	public void setEngineTurbopropLengthTextFieldMap(Map<Integer, TextField> engineTurbopropLengthTextFieldMap) {
		this.engineTurbopropLengthTextFieldMap = engineTurbopropLengthTextFieldMap;
	}

	public Map<Integer, ChoiceBox<String>> getEngineTurbopropLengthUnitChoiceBoxMap() {
		return engineTurbopropLengthUnitChoiceBoxMap;
	}

	public void setEngineTurbopropLengthUnitChoiceBoxMap(
			Map<Integer, ChoiceBox<String>> engineTurbopropLengthUnitChoiceBoxMap) {
		this.engineTurbopropLengthUnitChoiceBoxMap = engineTurbopropLengthUnitChoiceBoxMap;
	}

	public Map<Integer, TextField> getEngineTurbopropDryMassTextFieldMap() {
		return engineTurbopropDryMassTextFieldMap;
	}

	public void setEngineTurbopropDryMassTextFieldMap(Map<Integer, TextField> engineTurbopropDryMassTextFieldMap) {
		this.engineTurbopropDryMassTextFieldMap = engineTurbopropDryMassTextFieldMap;
	}

	public Map<Integer, ChoiceBox<String>> getEngineTurbopropDryMassUnitChoiceBoxMap() {
		return engineTurbopropDryMassUnitChoiceBoxMap;
	}

	public void setEngineTurbopropDryMassUnitChoiceBoxMap(
			Map<Integer, ChoiceBox<String>> engineTurbopropDryMassUnitChoiceBoxMap) {
		this.engineTurbopropDryMassUnitChoiceBoxMap = engineTurbopropDryMassUnitChoiceBoxMap;
	}

	public Map<Integer, TextField> getEngineTurbopropStaticPowerTextFieldMap() {
		return engineTurbopropStaticPowerTextFieldMap;
	}

	public void setEngineTurbopropStaticPowerTextFieldMap(Map<Integer, TextField> engineTurbopropStaticPowerTextFieldMap) {
		this.engineTurbopropStaticPowerTextFieldMap = engineTurbopropStaticPowerTextFieldMap;
	}

	public Map<Integer, ChoiceBox<String>> getEngineTurbopropStaticPowerUnitChoiceBoxMap() {
		return engineTurbopropStaticPowerUnitChoiceBoxMap;
	}

	public void setEngineTurbopropStaticPowerUnitChoiceBoxMap(
			Map<Integer, ChoiceBox<String>> engineTurbopropStaticPowerUnitChoiceBoxMap) {
		this.engineTurbopropStaticPowerUnitChoiceBoxMap = engineTurbopropStaticPowerUnitChoiceBoxMap;
	}

	public Map<Integer, TextField> getEngineTurbopropPropellerDiameterTextFieldMap() {
		return engineTurbopropPropellerDiameterTextFieldMap;
	}

	public void setEngineTurbopropPropellerDiameterTextFieldMap(
			Map<Integer, TextField> engineTurbopropPropellerDiameterTextFieldMap) {
		this.engineTurbopropPropellerDiameterTextFieldMap = engineTurbopropPropellerDiameterTextFieldMap;
	}

	public Map<Integer, ChoiceBox<String>> getEngineTurbopropPropellerDiameterUnitChoiceBoxMap() {
		return engineTurbopropPropellerDiameterUnitChoiceBoxMap;
	}

	public void setEngineTurbopropPropellerDiameterUnitChoiceBoxMap(
			Map<Integer, ChoiceBox<String>> engineTurbopropPropellerDiameterUnitChoiceBoxMap) {
		this.engineTurbopropPropellerDiameterUnitChoiceBoxMap = engineTurbopropPropellerDiameterUnitChoiceBoxMap;
	}

	public Map<Integer, TextField> getEngineTurbopropNumberOfBladesTextFieldMap() {
		return engineTurbopropNumberOfBladesTextFieldMap;
	}

	public void setEngineTurbopropNumberOfBladesTextFieldMap(
			Map<Integer, TextField> engineTurbopropNumberOfBladesTextFieldMap) {
		this.engineTurbopropNumberOfBladesTextFieldMap = engineTurbopropNumberOfBladesTextFieldMap;
	}

	public Map<Integer, TextField> getEngineTurbopropPropellerEfficiencyTextFieldMap() {
		return engineTurbopropPropellerEfficiencyTextFieldMap;
	}

	public void setEngineTurbopropPropellerEfficiencyTextFieldMap(
			Map<Integer, TextField> engineTurbopropPropellerEfficiencyTextFieldMap) {
		this.engineTurbopropPropellerEfficiencyTextFieldMap = engineTurbopropPropellerEfficiencyTextFieldMap;
	}

	public Map<Integer, TextField> getEngineTurbopropNumberOfCompressorStagesTextFieldMap() {
		return engineTurbopropNumberOfCompressorStagesTextFieldMap;
	}

	public void setEngineTurbopropNumberOfCompressorStagesTextFieldMap(
			Map<Integer, TextField> engineTurbopropNumberOfCompressorStagesTextFieldMap) {
		this.engineTurbopropNumberOfCompressorStagesTextFieldMap = engineTurbopropNumberOfCompressorStagesTextFieldMap;
	}

	public Map<Integer, TextField> getEngineTurbopropNumberOfShaftsTextFieldMap() {
		return engineTurbopropNumberOfShaftsTextFieldMap;
	}

	public void setEngineTurbopropNumberOfShaftsTextFieldMap(
			Map<Integer, TextField> engineTurbopropNumberOfShaftsTextFieldMap) {
		this.engineTurbopropNumberOfShaftsTextFieldMap = engineTurbopropNumberOfShaftsTextFieldMap;
	}

	public Map<Integer, TextField> getEngineTurbopropOverallPressureRatioTextFieldMap() {
		return engineTurbopropOverallPressureRatioTextFieldMap;
	}

	public void setEngineTurbopropOverallPressureRatioTextFieldMap(
			Map<Integer, TextField> engineTurbopropOverallPressureRatioTextFieldMap) {
		this.engineTurbopropOverallPressureRatioTextFieldMap = engineTurbopropOverallPressureRatioTextFieldMap;
	}

	public Map<Integer, ChoiceBox<String>> getEnginePistonTypeChoiceBoxMap() {
		return enginePistonTypeChoiceBoxMap;
	}

	public void setEnginePistonTypeChoiceBoxMap(Map<Integer, ChoiceBox<String>> enginePistonTypeChoiceBoxMap) {
		this.enginePistonTypeChoiceBoxMap = enginePistonTypeChoiceBoxMap;
	}

	public Map<Integer, TextField> getEnginePistonDatabaseTextFieldMap() {
		return enginePistonDatabaseTextFieldMap;
	}

	public void setEnginePistonDatabaseTextFieldMap(Map<Integer, TextField> enginePistonDatabaseTextFieldMap) {
		this.enginePistonDatabaseTextFieldMap = enginePistonDatabaseTextFieldMap;
	}

	public Map<Integer, TextField> getEnginePistonLengthTextFieldMap() {
		return enginePistonLengthTextFieldMap;
	}

	public void setEnginePistonLengthTextFieldMap(Map<Integer, TextField> enginePistonLengthTextFieldMap) {
		this.enginePistonLengthTextFieldMap = enginePistonLengthTextFieldMap;
	}

	public Map<Integer, ChoiceBox<String>> getEnginePistonLengthUnitChoiceBoxMap() {
		return enginePistonLengthUnitChoiceBoxMap;
	}

	public void setEnginePistonLengthUnitChoiceBoxMap(Map<Integer, ChoiceBox<String>> enginePistonLengthUnitChoiceBoxMap) {
		this.enginePistonLengthUnitChoiceBoxMap = enginePistonLengthUnitChoiceBoxMap;
	}

	public Map<Integer, TextField> getEnginePistonDryMassTextFieldMap() {
		return enginePistonDryMassTextFieldMap;
	}

	public void setEnginePistonDryMassTextFieldMap(Map<Integer, TextField> enginePistonDryMassTextFieldMap) {
		this.enginePistonDryMassTextFieldMap = enginePistonDryMassTextFieldMap;
	}

	public Map<Integer, ChoiceBox<String>> getEnginePistonDryMassUnitChoiceBoxMap() {
		return enginePistonDryMassUnitChoiceBoxMap;
	}

	public void setEnginePistonDryMassUnitChoiceBoxMap(
			Map<Integer, ChoiceBox<String>> enginePistonDryMassUnitChoiceBoxMap) {
		this.enginePistonDryMassUnitChoiceBoxMap = enginePistonDryMassUnitChoiceBoxMap;
	}

	public Map<Integer, TextField> getEnginePistonStaticPowerTextFieldMap() {
		return enginePistonStaticPowerTextFieldMap;
	}

	public void setEnginePistonStaticPowerTextFieldMap(Map<Integer, TextField> enginePistonStaticPowerTextFieldMap) {
		this.enginePistonStaticPowerTextFieldMap = enginePistonStaticPowerTextFieldMap;
	}

	public Map<Integer, ChoiceBox<String>> getEnginePistonStaticPowerUnitChoiceBoxMap() {
		return enginePistonStaticPowerUnitChoiceBoxMap;
	}

	public void setEnginePistonStaticPowerUnitChoiceBoxMap(
			Map<Integer, ChoiceBox<String>> enginePistonStaticPowerUnitChoiceBoxMap) {
		this.enginePistonStaticPowerUnitChoiceBoxMap = enginePistonStaticPowerUnitChoiceBoxMap;
	}

	public Map<Integer, TextField> getEnginePistonPropellerDiameterTextFieldMap() {
		return enginePistonPropellerDiameterTextFieldMap;
	}

	public void setEnginePistonPropellerDiameterTextFieldMap(
			Map<Integer, TextField> enginePistonPropellerDiameterTextFieldMap) {
		this.enginePistonPropellerDiameterTextFieldMap = enginePistonPropellerDiameterTextFieldMap;
	}

	public Map<Integer, ChoiceBox<String>> getEnginePistonPropellerDiameterUnitChoiceBoxMap() {
		return enginePistonPropellerDiameterUnitChoiceBoxMap;
	}

	public void setEnginePistonPropellerDiameterUnitChoiceBoxMap(
			Map<Integer, ChoiceBox<String>> enginePistonPropellerDiameterUnitChoiceBoxMap) {
		this.enginePistonPropellerDiameterUnitChoiceBoxMap = enginePistonPropellerDiameterUnitChoiceBoxMap;
	}

	public Map<Integer, TextField> getEnginePistonNumberOfBladesTextFieldMap() {
		return enginePistonNumberOfBladesTextFieldMap;
	}

	public void setEnginePistonNumberOfBladesTextFieldMap(Map<Integer, TextField> enginePistonNumberOfBladesTextFieldMap) {
		this.enginePistonNumberOfBladesTextFieldMap = enginePistonNumberOfBladesTextFieldMap;
	}

	public Map<Integer, TextField> getEnginePistonPropellerEfficiencyTextFieldMap() {
		return enginePistonPropellerEfficiencyTextFieldMap;
	}

	public void setEnginePistonPropellerEfficiencyTextFieldMap(
			Map<Integer, TextField> enginePistonPropellerEfficiencyTextFieldMap) {
		this.enginePistonPropellerEfficiencyTextFieldMap = enginePistonPropellerEfficiencyTextFieldMap;
	}

	public Map<EngineTypeEnum, Pane> getPowerPlantPaneMap() {
		return powerPlantPaneMap;
	}

	public void setPowerPlantPaneMap(Map<EngineTypeEnum, Pane> powerPlantPaneMap) {
		this.powerPlantPaneMap = powerPlantPaneMap;
	}

	public Map<Integer, Map<EngineTypeEnum, Pane>> getPowerPlantEngineTypePaneMap() {
		return powerPlantEngineTypePaneMap;
	}

	public void setPowerPlantEngineTypePaneMap(Map<Integer, Map<EngineTypeEnum, Pane>> powerPlantEngineTypePaneMap) {
		this.powerPlantEngineTypePaneMap = powerPlantEngineTypePaneMap;
	}

	public Map<Integer, BorderPane> getPowerPlantBorderPaneMap() {
		return powerPlantBorderPaneMap;
	}

	public void setPowerPlantBorderPaneMap(Map<Integer, BorderPane> powerPlantBorderPaneMap) {
		this.powerPlantBorderPaneMap = powerPlantBorderPaneMap;
	}

	public TextField getTextFieldLandingGearsMainLegLength() {
		return textFieldLandingGearsMainLegLength;
	}

	public void setTextFieldLandingGearsMainLegLength(TextField textFieldLandingGearsMainLegLength) {
		this.textFieldLandingGearsMainLegLength = textFieldLandingGearsMainLegLength;
	}

	public TextField getTextFieldLandingGearsKMainLegLength() {
		return textFieldLandingGearsKMainLegLength;
	}

	public void setTextFieldLandingGearsKMainLegLength(TextField textFieldLandingGearsKMainLegLength) {
		this.textFieldLandingGearsKMainLegLength = textFieldLandingGearsKMainLegLength;
	}

	public TextField getTextFieldLandingGearsDistanceBetweenWheels() {
		return textFieldLandingGearsDistanceBetweenWheels;
	}

	public void setTextFieldLandingGearsDistanceBetweenWheels(TextField textFieldLandingGearsDistanceBetweenWheels) {
		this.textFieldLandingGearsDistanceBetweenWheels = textFieldLandingGearsDistanceBetweenWheels;
	}

	public TextField getTextFieldLandingGearsNumberOfFrontalWheels() {
		return textFieldLandingGearsNumberOfFrontalWheels;
	}

	public void setTextFieldLandingGearsNumberOfFrontalWheels(TextField textFieldLandingGearsNumberOfFrontalWheels) {
		this.textFieldLandingGearsNumberOfFrontalWheels = textFieldLandingGearsNumberOfFrontalWheels;
	}

	public TextField getTextFieldLandingGearsNumberOfRearWheels() {
		return textFieldLandingGearsNumberOfRearWheels;
	}

	public void setTextFieldLandingGearsNumberOfRearWheels(TextField textFieldLandingGearsNumberOfRearWheels) {
		this.textFieldLandingGearsNumberOfRearWheels = textFieldLandingGearsNumberOfRearWheels;
	}

	public TextField getTextFieldLandingGearsFrontalWheelsHeight() {
		return textFieldLandingGearsFrontalWheelsHeight;
	}

	public void setTextFieldLandingGearsFrontalWheelsHeight(TextField textFieldLandingGearsFrontalWheelsHeight) {
		this.textFieldLandingGearsFrontalWheelsHeight = textFieldLandingGearsFrontalWheelsHeight;
	}

	public TextField getTextFieldLandingGearsFrontalWheelsWidth() {
		return textFieldLandingGearsFrontalWheelsWidth;
	}

	public void setTextFieldLandingGearsFrontalWheelsWidth(TextField textFieldLandingGearsFrontalWheelsWidth) {
		this.textFieldLandingGearsFrontalWheelsWidth = textFieldLandingGearsFrontalWheelsWidth;
	}

	public TextField getTextFieldLandingGearsRearWheelsHeight() {
		return textFieldLandingGearsRearWheelsHeight;
	}

	public void setTextFieldLandingGearsRearWheelsHeight(TextField textFieldLandingGearsRearWheelsHeight) {
		this.textFieldLandingGearsRearWheelsHeight = textFieldLandingGearsRearWheelsHeight;
	}

	public TextField getTextFieldLandingGearsRearWheelsWidth() {
		return textFieldLandingGearsRearWheelsWidth;
	}

	public void setTextFieldLandingGearsRearWheelsWidth(TextField textFieldLandingGearsRearWheelsWidth) {
		this.textFieldLandingGearsRearWheelsWidth = textFieldLandingGearsRearWheelsWidth;
	}

	public ChoiceBox<String> getLandingGearsMainLegLengthUnitChoiceBox() {
		return landingGearsMainLegLengthUnitChoiceBox;
	}

	public void setLandingGearsMainLegLengthUnitChoiceBox(ChoiceBox<String> landingGearsMainLegLengthUnitChoiceBox) {
		this.landingGearsMainLegLengthUnitChoiceBox = landingGearsMainLegLengthUnitChoiceBox;
	}

	public ChoiceBox<String> getLandingGearsDistanceBetweenWheelsUnitChoiceBox() {
		return landingGearsDistanceBetweenWheelsUnitChoiceBox;
	}

	public void setLandingGearsDistanceBetweenWheelsUnitChoiceBox(
			ChoiceBox<String> landingGearsDistanceBetweenWheelsUnitChoiceBox) {
		this.landingGearsDistanceBetweenWheelsUnitChoiceBox = landingGearsDistanceBetweenWheelsUnitChoiceBox;
	}

	public ChoiceBox<String> getLandingGearsFrontalWheelsHeigthUnitChoiceBox() {
		return landingGearsFrontalWheelsHeigthUnitChoiceBox;
	}

	public void setLandingGearsFrontalWheelsHeigthUnitChoiceBox(
			ChoiceBox<String> landingGearsFrontalWheelsHeigthUnitChoiceBox) {
		this.landingGearsFrontalWheelsHeigthUnitChoiceBox = landingGearsFrontalWheelsHeigthUnitChoiceBox;
	}

	public ChoiceBox<String> getLandingGearsFrontalWheelsWidthUnitChoiceBox() {
		return landingGearsFrontalWheelsWidthUnitChoiceBox;
	}

	public void setLandingGearsFrontalWheelsWidthUnitChoiceBox(
			ChoiceBox<String> landingGearsFrontalWheelsWidthUnitChoiceBox) {
		this.landingGearsFrontalWheelsWidthUnitChoiceBox = landingGearsFrontalWheelsWidthUnitChoiceBox;
	}

	public ChoiceBox<String> getLandingGearsRearWheelsHeigthUnitChoiceBox() {
		return landingGearsRearWheelsHeigthUnitChoiceBox;
	}

	public void setLandingGearsRearWheelsHeigthUnitChoiceBox(ChoiceBox<String> landingGearsRearWheelsHeigthUnitChoiceBox) {
		this.landingGearsRearWheelsHeigthUnitChoiceBox = landingGearsRearWheelsHeigthUnitChoiceBox;
	}

	public ChoiceBox<String> getLandingGearsRearWheelsWidthUnitChoiceBox() {
		return landingGearsRearWheelsWidthUnitChoiceBox;
	}

	public void setLandingGearsRearWheelsWidthUnitChoiceBox(ChoiceBox<String> landingGearsRearWheelsWidthUnitChoiceBox) {
		this.landingGearsRearWheelsWidthUnitChoiceBox = landingGearsRearWheelsWidthUnitChoiceBox;
	}

	public InputManagerControllerSecondaryActionUtilities getInputManagerControllerUtilities() {
		return inputManagerControllerSecondaryActionUtilities;
	}

	public void setInputManagerControllerUtilities(InputManagerControllerSecondaryActionUtilities inputManagerControllerUtilities) {
		this.inputManagerControllerSecondaryActionUtilities = inputManagerControllerUtilities;
	}

	public boolean isNoneAdjustCriterionFuselage() {
		return noneAdjustCriterionFuselage;
	}

	public void setNoneAdjustCriterionFuselage(boolean noneAdjustCriterionFuselage) {
		this.noneAdjustCriterionFuselage = noneAdjustCriterionFuselage;
	}

	public boolean isNoneAdjustCriterionWing() {
		return noneAdjustCriterionWing;
	}

	public void setNoneAdjustCriterionWing(boolean noneAdjustCriterionWing) {
		this.noneAdjustCriterionWing = noneAdjustCriterionWing;
	}

	public boolean isNoneAdjustCriterionHTail() {
		return noneAdjustCriterionHTail;
	}

	public void setNoneAdjustCriterionHTail(boolean noneAdjustCriterionHTail) {
		this.noneAdjustCriterionHTail = noneAdjustCriterionHTail;
	}

	public boolean isNoneAdjustCriterionVTail() {
		return noneAdjustCriterionVTail;
	}

	public void setNoneAdjustCriterionVTail(boolean noneAdjustCriterionVTail) {
		this.noneAdjustCriterionVTail = noneAdjustCriterionVTail;
	}

	public boolean isNoneAdjustCriterionCanard() {
		return noneAdjustCriterionCanard;
	}

	public void setNoneAdjustCriterionCanard(boolean noneAdjustCriterionCanard) {
		this.noneAdjustCriterionCanard = noneAdjustCriterionCanard;
	}

	public InputManagerControllerMainActionUtilities getInputManagerControllerMainActionUtilities() {
		return inputManagerControllerMainActionUtilities;
	}

	public void setInputManagerControllerMainActionUtilities(
			InputManagerControllerMainActionUtilities inputManagerControllerMainActionUtilities) {
		this.inputManagerControllerMainActionUtilities = inputManagerControllerMainActionUtilities;
	}

	public InputManagerControllerSecondaryActionUtilities getInputManagerControllerSecondaryActionUtilities() {
		return inputManagerControllerSecondaryActionUtilities;
	}

	public void setInputManagerControllerSecondaryActionUtilities(
			InputManagerControllerSecondaryActionUtilities inputManagerControllerSecondaryActionUtilities) {
		this.inputManagerControllerSecondaryActionUtilities = inputManagerControllerSecondaryActionUtilities;
	}

	public ChoiceBox<String> getSystemsPrimaryElectricalTypeChoiceBox() {
		return systemsPrimaryElectricalTypeChoiceBox;
	}

	public void setSystemsPrimaryElectricalTypeChoiceBox(ChoiceBox<String> systemsPrimaryElectricalTypeChoiceBox) {
		this.systemsPrimaryElectricalTypeChoiceBox = systemsPrimaryElectricalTypeChoiceBox;
	}

	public TextField getTextFieldHTailMainSparAdimensionalPosition() {
		return textFieldHTailMainSparAdimensionalPosition;
	}

	public void setTextFieldHTailMainSparAdimensionalPosition(TextField textFieldHTailMainSparAdimensionalPosition) {
		this.textFieldHTailMainSparAdimensionalPosition = textFieldHTailMainSparAdimensionalPosition;
	}

	public TextField getTextFieldHTailSecondarySparAdimensionalPosition() {
		return textFieldHTailSecondarySparAdimensionalPosition;
	}

	public void setTextFieldHTailSecondarySparAdimensionalPosition(
			TextField textFieldHTailSecondarySparAdimensionalPosition) {
		this.textFieldHTailSecondarySparAdimensionalPosition = textFieldHTailSecondarySparAdimensionalPosition;
	}

	public TextField getTextFieldVTailMainSparAdimensionalPosition() {
		return textFieldVTailMainSparAdimensionalPosition;
	}

	public void setTextFieldVTailMainSparAdimensionalPosition(TextField textFieldVTailMainSparAdimensionalPosition) {
		this.textFieldVTailMainSparAdimensionalPosition = textFieldVTailMainSparAdimensionalPosition;
	}

	public TextField getTextFieldVTailSecondarySparAdimensionalPosition() {
		return textFieldVTailSecondarySparAdimensionalPosition;
	}

	public void setTextFieldVTailSecondarySparAdimensionalPosition(
			TextField textFieldVTailSecondarySparAdimensionalPosition) {
		this.textFieldVTailSecondarySparAdimensionalPosition = textFieldVTailSecondarySparAdimensionalPosition;
	}

	public TextField getTextFieldCanardMainSparAdimensionalPosition() {
		return textFieldCanardMainSparAdimensionalPosition;
	}

	public void setTextFieldCanardMainSparAdimensionalPosition(TextField textFieldCanardMainSparAdimensionalPosition) {
		this.textFieldCanardMainSparAdimensionalPosition = textFieldCanardMainSparAdimensionalPosition;
	}

	public TextField getTextFieldCanardSecondarySparAdimensionalPosition() {
		return textFieldCanardSecondarySparAdimensionalPosition;
	}

	public void setTextFieldCanardSecondarySparAdimensionalPosition(
			TextField textFieldCanardSecondarySparAdimensionalPosition) {
		this.textFieldCanardSecondarySparAdimensionalPosition = textFieldCanardSecondarySparAdimensionalPosition;
	}

	public InputManagerControllerGraphicUtilities getInputManagerControllerGraphicUtilities() {
		return inputManagerControllerGraphicUtilities;
	}

	public void setInputManagerControllerGraphicUtilities(InputManagerControllerGraphicUtilities inputManagerControllerGraphicUtilities) {
		this.inputManagerControllerGraphicUtilities = inputManagerControllerGraphicUtilities;
	}

	public InputManagerControllerLogUtilities getInputManagerControllerLogUtilities() {
		return inputManagerControllerLogUtilities;
	}

	public void setInputManagerControllerLogUtilities(InputManagerControllerLogUtilities inputManagerControllerLogUtilities) {
		this.inputManagerControllerLogUtilities = inputManagerControllerLogUtilities;
	}

	public InputManagerControllerUpdateUtilites getInputManagerControllerUpdateUtilites() {
		return inputManagerControllerUpdateUtilites;
	}

	public void setInputManagerControllerUpdateUtilites(InputManagerControllerUpdateUtilites inputManagerControllerUpdateUtilites) {
		this.inputManagerControllerUpdateUtilites = inputManagerControllerUpdateUtilites;
	}

	public String getSystemsPrimaryElectricalTypeValue() {
		return systemsPrimaryElectricalTypeValue;
	}

	public void setSystemsPrimaryElectricalTypeValue(String systemsPrimaryElectricalTypeValue) {
		this.systemsPrimaryElectricalTypeValue = systemsPrimaryElectricalTypeValue;
	};
	
}
