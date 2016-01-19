package sandbox.adm;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.fxyz.cameras.CameraTransformer;
import org.fxyz.geometry.Point3D;
import org.fxyz.shapes.primitives.BezierMesh;
import org.jcae.opencascade.jni.BRep_Builder;
import org.jcae.opencascade.jni.TopoDS_Compound;
import org.jcae.opencascade.jni.TopoDS_Shape;

import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import cad.aircraft.MyFuselageBuilder;
import cad.occ.OCCFX3DView;
import cad.occ.OCCFX3DView.VantagePoint3DView;
import cad.occ.OCCFXForm;
import cad.occ.OCCFXFuselageSection;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import cad.occ.OCCFXMeshExtractor;
import cad.occ.OCCFXSubScene;
import cad.occ.OCCFXViewableCAD;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;

public class JavaFXTest_02_CAD extends Application {

	private final double sceneWidth = 600;
	private final double sceneHeight = 600;

	private SubScene _subScene = null;

	private long lastTime = 0L;
	private int frameCounter = 0;
	private int elapsedFrames = 100;

	private AnimationTimer fpsTimer = null;
	private HUDLabel fpsTitleLabel = null;
	private HUDLabel fpsLabel = null;
	private HUDLabel mpfTitleLabel = null;
	private HUDLabel mpfLabel = null;

	// TODO: manage this
	//private FourWayNavControl fourWayNavControl = null;

	private boolean isUpdateFPS = false;
	private boolean isTuxRotating = false;
	private boolean isCubeRotating = false;          
	private static BooleanProperty _isMouseDraggedProperty = new SimpleBooleanProperty(false);

	private DrawMode drawMode = DrawMode.FILL;

	private Background blackBG = null;
	private Background blueBG = null;
	private Background greenBG = null;

	private int gap = 0;
	private int border = 0;

	private Font titleFont = null;
	private Font textFont = null;
	private Font cellFont = null;

	private NumberFormat numFormat = null;

	private OCCFX3DView _the3DView = null;

	private OCCFX3DView.VantagePoint3DView _theCurrentVP = OCCFX3DView.VantagePoint3DView.FRONT;
	private static BooleanProperty _notAtVantagePointProperty = new SimpleBooleanProperty(true);

	// Aircraft-related property
	public static BooleanProperty aircraftAllocatedProperty = new SimpleBooleanProperty(false);
	public ObjectProperty<Aircraft> theCurrentAircraftProperty = 
			new SimpleObjectProperty<Aircraft>();

	public static BooleanProperty fuselageAllocatedProperty = new SimpleBooleanProperty(false);
	public ObjectProperty<Fuselage> theCurrentFuselageProperty = 
			new SimpleObjectProperty<Fuselage>();

	public static ObservableMap<OCCFXViewableCAD,String> viewablesMap = FXCollections.observableHashMap();

	private StringProperty _strFuselageNameProperty = new SimpleStringProperty("---");

	// bezier-related properties
	private StringProperty _strBezierColorNameProperty = new SimpleStringProperty("---");

	// selection mode
	private StringProperty _strSelectionModeProperty = new SimpleStringProperty("OFF");
	private BooleanProperty _isSelectionModeOnProperty = new SimpleBooleanProperty(false);
	private Menu _menuSelectionMode;
	private ToggleGroup _toggleSelectionModeGroup;
	private RadioMenuItem _itemSelectionModeOn;
	private RadioMenuItem _itemSelectionModeOff;

	public static final String FUSELAGE_ID = new String("FUSELAGE:");
	public static final String WING_ID = new String("WING:");
	public static final String HORIZONTAL_TAIL_ID = new String("HORIZONTAL:TAIL:");
	public static final String VERTICAL_TAIL_ID = new String("VERTICAL:TAIL:");

	Aircraft _theAircraft = null;
	
	@Override
	public void start(Stage primaryStage) throws Exception {

		System.out.println("------------------------------------------------------------");
		System.out.println("OCCFX test -- see MyTestJavaFXPane3");
		System.out.println("------------------------------------------------------------");

		
		//==========================================================================================
		// taken from MyPane3D constructor

		//--------------------------------------------------------------------------
		// property bindings
		
		// ???
		
		
		// TODO: assign viewablesMap
		viewablesMap = FXCollections.observableHashMap();
		
		//--------------------------------------------------------------------------
		// materials
		
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(Color.DARKRED);
		redMaterial.setSpecularColor(Color.RED);

		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(Color.DARKGREEN);
		greenMaterial.setSpecularColor(Color.GREEN);

		//-------------------------------------------------------------------------

		final Rectangle2D screenRect = Screen.getPrimary().getBounds();
		final double screenWidth = screenRect.getWidth();
		final double screenHeight = screenRect.getHeight();

		// screenHeight > 1200      (1440/27', 1600/30')
		int cellFontSize = 16;
		int textFontSize = 20;
		int titleFontSize = 38;
		gap = 6;
		border = 50;

		// 900 < screenHeight  <= 1080/23'
		if (screenHeight <= 1080) {
			cellFontSize = 14;
			textFontSize = 16;
			titleFontSize = 30;
			gap = 4;
			border = 30;
		}
		// 1080 < screenHeight <= 1200/24'
		else if (screenHeight <= 1200) {
			cellFontSize = 14;
			textFontSize = 18;
			titleFontSize = 34;
			gap = 5;
			border = 40;
		}

		final String fontFamily = "Dialog";

		titleFont = Font.font(fontFamily, FontWeight.NORMAL, titleFontSize);
		textFont = Font.font(fontFamily, FontWeight.NORMAL, textFontSize);
		cellFont = Font.font(fontFamily, FontWeight.NORMAL, cellFontSize);

		numFormat = NumberFormat.getIntegerInstance();
		numFormat.setGroupingUsed(true);

		//
		// 3D subscene
		//
		_the3DView = new OCCFX3DView(); // _theAircraft 
		_subScene = _the3DView.getSubScene();

		_isSelectionModeOnProperty.bind(_the3DView.getSelectionModeOnProperty());

		if (_subScene == null) {
			System.out.println("SubScene null!");
		}

		
		//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		
		testViewableCAD(); // creates _theAircraft
		
		//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		
		
		
		
		//
		// Title
		//

		final HUDLabel titleLeftLabel = new HUDLabel("", titleFont);
		final HUDLabel titleRightLabel = new HUDLabel("OCCJavaFX 3D Window", titleFont);        
		//
		// 3D scene details and performance
		//

		// FPS - frames per second

		fpsTitleLabel = new HUDLabel("F P S");
		fpsTitleLabel.setTooltip(new Tooltip("frames per second"));
		fpsLabel = new HUDLabel("0");
		fpsLabel.setTooltip(new Tooltip("frames per second"));

		// MPF - milliseconds per frame

		mpfTitleLabel = new HUDLabel("M P F");
		mpfTitleLabel.setTooltip(new Tooltip("milliseconds per frame"));
		mpfLabel = new HUDLabel("0");
		mpfLabel.setTooltip(new Tooltip("milliseconds per frame"));

		// Tuxes/Shape3Ds/triangles

//		final HUDLabel tuxesLabel = new HUDLabel("Tuxes");
//		tuxesLabel.setTooltip(new Tooltip("number of Tux models and RotateTransitions"));
		final HUDLabel shape3dLabel = new HUDLabel("Shape3Ds");
		shape3dLabel.setTooltip(new Tooltip("number of Shape3D nodes"));
		final HUDLabel triangleLabel = new HUDLabel("Triangles");
		triangleLabel.setTooltip(new Tooltip("number of triangles"));

		// Set initial output values for autosizing
//		final HUDLabel numTuxesLabel = new HUDLabel(numFormat.format(27));
//		numTuxesLabel.setTooltip(new Tooltip("number of Tux models and RotateTransitions"));
		final HUDLabel numShape3dLabel = new HUDLabel(numFormat.format(162));
		numShape3dLabel.setTooltip(new Tooltip("number of Shape3D nodes"));
		final HUDLabel numTriaLabel = new HUDLabel(numFormat.format(371088));
		numTriaLabel.setTooltip(new Tooltip("number of triangles"));

		// Size of FXCanvas3D

		final HUDLabel heightLabel = new HUDLabel("Height");
		heightLabel.setTooltip(new Tooltip("height of 3D SubScene"));
		final HUDLabel pixHeightLabel = new HUDLabel("0");        
		pixHeightLabel.setTooltip(new Tooltip("height of 3D SubScene"));
		final HUDLabel widthLabel = new HUDLabel("Width");
		widthLabel.setTooltip(new Tooltip("width of 3D SubScene"));
		final HUDLabel pixWidthLabel = new HUDLabel("0");        
		pixWidthLabel.setTooltip(new Tooltip("width of 3D SubScene"));

		// Collect all outputs 

		final Rectangle gap1 = new Rectangle(gap, gap, Color.TRANSPARENT);
		final Rectangle gap2 = new Rectangle(gap, gap, Color.TRANSPARENT);

		final GridPane outputPane = new GridPane();
		outputPane.setHgap(10);
		outputPane.setVgap(0);
		outputPane.setGridLinesVisible(false);

		outputPane.add(fpsLabel, 0, 0);
		outputPane.add(fpsTitleLabel, 1, 0);
		outputPane.add(mpfLabel, 0, 1);
		outputPane.add(mpfTitleLabel, 1, 1);       
		outputPane.add(gap1, 0, 2);       
//		outputPane.add(numTuxesLabel, 0, 3);
//		outputPane.add(tuxesLabel, 1, 3);
		outputPane.add(numShape3dLabel, 0, 4);
		outputPane.add(shape3dLabel, 1, 4);
		outputPane.add(numTriaLabel, 0, 5);
		outputPane.add(triangleLabel, 1, 5);
		outputPane.add(gap2, 0, 6);
		outputPane.add(pixWidthLabel, 0, 7);
		outputPane.add(widthLabel, 1, 7);
		outputPane.add(pixHeightLabel, 0, 8);
		outputPane.add(heightLabel, 1, 8);

		final ColumnConstraints leftColumn = new ColumnConstraints();
		leftColumn.setHalignment(HPos.RIGHT);

		final ColumnConstraints rightColumn = new ColumnConstraints();
		rightColumn.setHalignment(HPos.LEFT);

		outputPane.getColumnConstraints().addAll(leftColumn, rightColumn);

		//
		// Controls
		//

		// Cube size

//		final HUDLabel numTitleLabel = new HUDLabel("Cube");
//		numTitleLabel.setTooltip(new Tooltip("width x height x depth"));

		final ObservableList<Number> nums = FXCollections.<Number>observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);

		final ComboBox<Number> cubeCombo = new ComboBox<>();
		cubeCombo.setTooltip(new Tooltip("width x height x depth"));
		cubeCombo.setItems(nums);
		cubeCombo.setVisibleRowCount(12);
		cubeCombo.getSelectionModel().select(2);               
		cubeCombo.getSelectionModel().selectedItemProperty().addListener(
				new ChangeListener<Number>() {
					@Override 
					public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {  
						int num = (Integer)new_val;
						int num3 = num*num*num;

						//						_theAircraft3DView.createTuxCubeOfDim(num, isTuxRotating, drawMode);      
						//						_theAircraft3DView.setVantagePoint(VP.FRONT);

						//						numTuxesLabel.setText( numFormat.format(num3) );
						numShape3dLabel.setText( numFormat.format(num3*6) );
						numTriaLabel.setText( numFormat.format(num3*13744) );
					}
				}
				);                
		cubeCombo.setButtonCell(new ListCell<Number>() {
			{
				this.setFont(cellFont);
			}      
			@Override protected void updateItem(Number item, boolean empty) {
				// calling super here is very important - don't skip this!
				super.updateItem(item, empty);                                               
				if (item != null) {
					this.setText(Integer.toString((Integer)item));
				}
			}
		});       
		cubeCombo.setCellFactory(new Callback<ListView<Number>, ListCell<Number>>() {
			@Override public ListCell<Number> call(ListView<Number> p) {
				return new ListCell<Number>() {
					{
						this.setFont(cellFont);
					}       
					@Override protected void updateItem(Number item, boolean empty) {
						// calling super here is very important - don't skip this!
						super.updateItem(item, empty);                                               
						if (item != null) {
							this.setText(Integer.toString((Integer)item));
						}
					}
				};
			}
		});

		// Viewpoints   

		final HUDLabel vpTitleLabel = new HUDLabel("Viewpoint");
		vpTitleLabel.setTooltip(new Tooltip("select viewpoint"));

		// Prompt text workaround !!!!!!!!!!!!!!!!!!

		final ObservableList<OCCFX3DView.VantagePoint3DView> vps = FXCollections
				.<OCCFX3DView.VantagePoint3DView>observableArrayList(
						OCCFX3DView.VantagePoint3DView.values());
		
		final ComboBox<OCCFX3DView.VantagePoint3DView> vpCombo = 
				new ComboBox<OCCFX3DView.VantagePoint3DView>();
		vpCombo.setTooltip(new Tooltip("select viewpoint"));		
//		vpCombo.getItems().addAll(
//				OCCFX3DView.VantagePoint3DView.BOTTOM, OCCFX3DView.VantagePoint3DView.TOP, 
//				OCCFX3DView.VantagePoint3DView.CORNER, 
//				OCCFX3DView.VantagePoint3DView.FRONT, OCCFX3DView.VantagePoint3DView.BACK,
//				OCCFX3DView.VantagePoint3DView.RIGHT, OCCFX3DView.VantagePoint3DView.LEFT); // DO NOT add VP.Select !!
		vpCombo.setItems(vps);
//		// Pre-select the prompt text item
//		vpCombo.setValue(OCCFX3DView.VantagePoint3DView.Select);       
//		// Handle ComboBox event.
//		vpCombo.setOnAction((event) -> {
//			OCCFX3DView.VantagePoint3DView vp = vpCombo.getSelectionModel().getSelectedItem();
//			// System.out.println("ComboBox Action (selected: " + vp.toString() + ")");
//			if (
//					vp.equals(OCCFX3DView.VantagePoint3DView.Select)
//					// vp == null
//				) {
//				vpCombo.setValue(OCCFX3DView.VantagePoint3DView.Select);
//				// _theAircraft3DView.setVantagePoint(_theCurrentVP);
//				System.out.println("...................");
//				event.consume();
//			} else {
//				_the3DView.setVantagePoint(vp);
//				_notAtVantagePointProperty.set(false);
//				// Select the prompt text item
//				vpCombo.setValue(OCCFX3DView.VantagePoint3DView.Select);
//			}
//			_theCurrentVP = vp;
//			// TODO: when user selects twice the same item,
//			//       the same action is not performed a second time
//		});
//		vpCombo.setButtonCell(new ListCell<OCCFX3DView.VP>() {
//			{
//				this.setFont(cellFont);
//			}
//			@Override protected void updateItem(OCCFX3DView.VP item, boolean empty) {
//				// calling super here is very important - don't skip this!
//				super.updateItem(item, empty);                                    
//				if (item != null) {
//					this.setText(item.getListName());
//				}
//			}
//		});       
//		vpCombo.setCellFactory(new Callback<ListView<OCCFX3DView.VP>, ListCell<OCCFX3DView.VP>>() {
//			@Override public ListCell<OCCFX3DView.VP> call(ListView<OCCFX3DView.VP> p) {
//				return new ListCell<OCCFX3DView.VP>() {
//					{
//						this.setFont(cellFont);
//					}
//					@Override protected void updateItem(OCCFX3DView.VP item, boolean empty) {
//						// calling super here is very important - don't skip this!
//						super.updateItem(item, empty);
//						if (item != null) {
//							this.setText(item.getListName());
//						}
//					}
//				};
//			}
//		});

		// Tux rotation
//		final HUDLabel tuxRotTitlelabel = new HUDLabel("Tux");
//		tuxRotTitlelabel.setTooltip(new Tooltip("start/pause rotation of Tuxes"));

		final CheckBox tuxRotCheck = new CheckBox();
		tuxRotCheck.setTooltip(new Tooltip("start/pause rotation of Tuxes"));
		tuxRotCheck.setStyle("-fx-label-padding: 0");
		tuxRotCheck.setGraphicTextGap(0);
		tuxRotCheck.setFont(textFont); // determines size of graphic !?
		tuxRotCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				isTuxRotating = !isTuxRotating;
//				_theAircraft3DView.playPauseTuxRotation(isTuxRotating);
				checkFPS();
			}
		});

		// Cube rotation
		final HUDLabel rotationLabel = new HUDLabel("    <  Cube Rotation  >    ");
		rotationLabel.setTooltip(new Tooltip("direction & speed of rotation"));

		final Slider rotationSlider = new Slider(20, 80, 50);
		rotationSlider.setBlockIncrement(0.6);
		rotationSlider.valueProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				isCubeRotating = (newValue.floatValue() < 49f || newValue.floatValue() > 51f);
//				_theAircraft3DView.setRotationSpeed(((Double)newValue).floatValue());
				checkFPS();
			}
		});
		rotationSlider.setTooltip(new Tooltip("direction & speed of rotation"));

		// Collect all controls
		final GridPane controlPane = new GridPane();
		controlPane.setHgap(10);
		controlPane.setVgap(4);
		controlPane.setGridLinesVisible(false);

//		controlPane.add(numTitleLabel, 0, 0);
		controlPane.add(cubeCombo, 0, 1);

		controlPane.add(vpTitleLabel, 1, 0);
		controlPane.add(vpCombo, 1, 1);

//		controlPane.add(tuxRotTitlelabel, 2, 0);
		controlPane.add(tuxRotCheck, 2, 1);

		controlPane.add(rotationLabel, 3, 0);
		controlPane.add(rotationSlider, 3, 1);     

		//-------------------------------------------------------
		// Aircraft label

		final GridPane statusPane = new GridPane();
		statusPane.setHgap(4);
		statusPane.setGridLinesVisible(false);

		final HUDLabel aircraftLabel = new HUDLabel("Aircraft:");
		final HUDLabel aircraftNameLabel = new HUDLabel("---");
		aircraftNameLabel.setTextFill(Color.DARKORANGE);

		final HUDLabel fuselageLabel = new HUDLabel(" \u2022 Fuselage:");
		final HUDLabel fuselageNameLabel = new HUDLabel("---");
		fuselageNameLabel.setTextFill(Color.DARKORANGE);

		// add to grid
		
		// Aircraft label
		statusPane.add(aircraftLabel, 0, 0);
		GridPane.setHalignment(aircraftLabel, HPos.LEFT);
		statusPane.add(aircraftNameLabel, 1, 0);
		GridPane.setHalignment(aircraftNameLabel, HPos.LEFT);

		// Fuselage label
		statusPane.add(fuselageLabel, 2, 0);
		GridPane.setHalignment(fuselageLabel, HPos.LEFT);
		statusPane.add(fuselageNameLabel, 3, 0);
		GridPane.setHalignment(fuselageNameLabel, HPos.LEFT);
		
		//-------------------------------------------------------

		for (int i=0; i < 4; i++) {
			final ColumnConstraints cC = new ColumnConstraints();
			cC.setHalignment(HPos.CENTER);
			controlPane.getColumnConstraints().add(cC);
		}

		final RowConstraints topRow = new RowConstraints();
		topRow.setValignment(VPos.CENTER);

		final RowConstraints botRow = new RowConstraints();
		botRow.setValignment(VPos.CENTER);

		controlPane.getRowConstraints().addAll(topRow, botRow);


		final EventHandler onMouseEnteredHandler = new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {
				controlPane.setOpacity(1.0);
			}
		};
		final EventHandler onMouseExitedHandler = new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {
				controlPane.setOpacity(0.5);
			}
		};

		controlPane.setOpacity(0.5);
		controlPane.setOnMouseEntered(onMouseEnteredHandler);    
		controlPane.setOnMouseExited(onMouseExitedHandler);

		final EventHandler onMouseEnteredStatusPaneHandler = new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {
				statusPane.setOpacity(1.0);
			}
		};
		final EventHandler onMouseExitedStatusPaneHandler = new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {
				statusPane.setOpacity(0.5);
			}
		};

		statusPane.setOpacity(0.5);
		statusPane.setOnMouseEntered(onMouseEnteredStatusPaneHandler);    
		statusPane.setOnMouseExited(onMouseExitedStatusPaneHandler);

		// Layout      

		// best smooth resizing if 3D-_subScene is re-sized in Scene's ChangeListener
		// and HUDs in layeredPane.layoutChildren()

		// best background painting and mouse event handling
		// if _subScene.setFill(Color.TRANSPARENT)
		// and layeredPane.setBackground(...). Don't use Scene.setFill.

		//==========================================================================================

		final double size = Math.min(screenWidth*0.8, screenHeight*0.8);
		_subScene.setWidth(size);
		_subScene.setHeight(size);
		
		final Group rootGroup = new Group();
		final Scene scene = new Scene(rootGroup, size, size, true);               
		final ChangeListener sceneBoundsListener = new ChangeListener() {
			@Override
			public void changed(ObservableValue observable, Object oldXY, Object newXY) {
				_subScene.setWidth(scene.getWidth());
				_subScene.setHeight(scene.getHeight());                            
			}
		};        
		scene.widthProperty().addListener(sceneBoundsListener);
		scene.heightProperty().addListener(sceneBoundsListener);

		final Pane layeredPane = new Pane() {
			@Override protected void layoutChildren() {

				double width = scene.getWidth();
				double height = scene.getHeight();

				titleLeftLabel.autosize();
				titleLeftLabel.relocate(border, border);

				titleRightLabel.autosize();
				titleRightLabel.relocate(width-titleRightLabel.getWidth()-border, border);

				controlPane.autosize();  
				controlPane.relocate(border, height - controlPane.getHeight() - statusPane.getHeight() - border);

				statusPane.autosize();
				statusPane.relocate(border, height - statusPane.getHeight() - border + 7); // TODO: check this offset

				outputPane.autosize();   
				outputPane.relocate(width - border - outputPane.getWidth(), 
						height - outputPane.getHeight() - border);

				pixWidthLabel.setText(numFormat.format((int)width));
				pixHeightLabel.setText(numFormat.format((int)height));
			}
		};
		layeredPane.getChildren().addAll(
				_subScene, 
				titleLeftLabel, titleRightLabel,
				controlPane, outputPane, 
				statusPane
				);

		// Backgrounds 

		final Stop[] stopsRG = new Stop[]{new Stop(0.0, Color.LIGHTGRAY),
				new Stop(0.2, Color.BLACK),
				new Stop(1.0, Color.BLACK)};
		final RadialGradient rg = new RadialGradient(0, 0, 0.5, 0.5, 1, true, CycleMethod.NO_CYCLE, stopsRG);
		blackBG = new Background(new BackgroundFill(rg, null, null));

		final Stop[] stopsLG = new Stop[] {new Stop(0.0, Color.rgb(0, 73, 255)),
				new Stop(0.7, Color.rgb(127, 164, 255)), 
				new Stop(1.0, Color.rgb(0, 73, 255))};
		final LinearGradient lg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stopsLG);   
		blueBG = new Background(new BackgroundFill(lg, null, null));

		greenBG = new Background(new BackgroundFill(Color.TURQUOISE, null, null));

		layeredPane.setBackground(blueBG); // initial background

		rootGroup.getChildren().add(layeredPane);

		//
		// ContextMenu 
		//
		// Projection
		final Menu menuProjection = new Menu("Projection");       
		final ToggleGroup toggleProjectionGroup = new ToggleGroup();

		final RadioMenuItem itemParallel = new RadioMenuItem("Parallel");
		itemParallel.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				_the3DView.setProjectionMode("Parallel");
			}
		});
		itemParallel.setToggleGroup(toggleProjectionGroup);
		itemParallel.setDisable(true); // Not implemented yet

		final RadioMenuItem itemPerspective = new RadioMenuItem("Perspective");
		itemPerspective.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				_the3DView.setProjectionMode("Perspective");
			}
		});
		itemPerspective.setSelected(true);
		itemPerspective.setToggleGroup(toggleProjectionGroup);

		menuProjection.getItems().addAll(itemParallel, itemPerspective);

		// Polygon mode
		final Menu menuPolygon = new Menu("Polygon mode");
		final ToggleGroup togglePolygonGroup = new ToggleGroup();

		final EventHandler polyModeHandler = new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				
				if (drawMode == DrawMode.FILL) {
					drawMode = DrawMode.LINE;
				
				} else {
					drawMode = DrawMode.FILL;
				}
				_the3DView.setDrawMode(drawMode);
			}
		};

		final RadioMenuItem itemPolyFill = new RadioMenuItem("Fill");
		itemPolyFill.setToggleGroup(togglePolygonGroup);
		itemPolyFill.setSelected(true);
		itemPolyFill.setOnAction(polyModeHandler);

		final RadioMenuItem itemPolyLine = new RadioMenuItem("Line");
		itemPolyLine.setToggleGroup(togglePolygonGroup);
		itemPolyLine.setOnAction(polyModeHandler);

		menuPolygon.getItems().addAll(itemPolyFill, itemPolyLine);

		// Background
		final Menu menuBackground = new Menu("Background");       
		final ToggleGroup toggleBackgroundGroup = new ToggleGroup();

		final RadioMenuItem itemBlackBG = new RadioMenuItem("Black RadialGradient");
		itemBlackBG.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				layeredPane.setBackground(blackBG);
			}
		});
		itemBlackBG.setToggleGroup(toggleBackgroundGroup);

		final RadioMenuItem itemBlueBG = new RadioMenuItem("Blue LinearGradient");
		itemBlueBG.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				layeredPane.setBackground(blueBG);
			}
		});
		itemBlueBG.setToggleGroup(toggleBackgroundGroup);
		itemBlueBG.setSelected(true);

		final RadioMenuItem itemGreenBG = new RadioMenuItem("Turquois Color");
		itemGreenBG.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				layeredPane.setBackground(greenBG);
			}
		});
		itemGreenBG.setToggleGroup(toggleBackgroundGroup);

		menuBackground.getItems().addAll(itemBlackBG, itemBlueBG, itemGreenBG);

		// Reset cube rotation
		final MenuItem itemStopCubeRot = new MenuItem("Reset cube rotation");
		itemStopCubeRot.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
//				_theAircraft3DView.stopCubeRotation();
				rotationSlider.setValue(50);
			}
		}); 

		// Reset tux rotation
		final MenuItem itemStopTuxRot = new MenuItem("Reset Tux rotation");
		itemStopTuxRot.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				if (isTuxRotating) {
					tuxRotCheck.setSelected(false);
				}
//				_theAircraft3DView.stopTuxRotation();
			}
		}); 

		// Scene anti-aliasing mode
		final Menu menuSceneAA = new Menu("Scene anti-aliasing");
		final ToggleGroup toggleGroupSceneAA = new ToggleGroup();

		final RadioMenuItem itemBalancedAA = new RadioMenuItem("BALANCED");
		itemBalancedAA.setToggleGroup(toggleGroupSceneAA);
		itemBalancedAA.setSelected(true);

		final RadioMenuItem itemDisabledAA = new RadioMenuItem("DISABLED");
		itemDisabledAA.setToggleGroup(toggleGroupSceneAA);

		final EventHandler sceneAAHandler = new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {

				SceneAntialiasing sceneAA = SceneAntialiasing.DISABLED;
				if (toggleGroupSceneAA.getSelectedToggle() == itemBalancedAA) {
					sceneAA = SceneAntialiasing.BALANCED;
				}

				// Exchange SubScene

				_subScene = _the3DView.exchangeSubScene(sceneAA);
				layeredPane.getChildren().set(0, _subScene);
			}
		};
		itemBalancedAA.setOnAction(sceneAAHandler);
		itemDisabledAA.setOnAction(sceneAAHandler);

		menuSceneAA.getItems().addAll(itemBalancedAA, itemDisabledAA);

		// Exit
		//     final MenuItem itemExit = new MenuItem("Exit");
		//     itemExit.setOnAction(new EventHandler<ActionEvent>() {
		//         @Override public void handle(ActionEvent e) {
		//             exit();
		//         }
		//     }); 

		// Selection mode

		_menuSelectionMode = new Menu("Selection mode");
		_toggleSelectionModeGroup = new ToggleGroup();

		_itemSelectionModeOn = new RadioMenuItem("ON");
		_itemSelectionModeOn.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				_the3DView.setSelectionMode(true);
				_strSelectionModeProperty.set("ON");
			}
		});
		_itemSelectionModeOn.setToggleGroup(_toggleSelectionModeGroup);

		_itemSelectionModeOff = new RadioMenuItem("OFF");
		_itemSelectionModeOff.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				_the3DView.setSelectionMode(false);
				_strSelectionModeProperty.set("OFF");
			}
		});
		_itemSelectionModeOff.setToggleGroup(_toggleSelectionModeGroup);
		
		if (_the3DView.isSelectionModeOn()) 
			_itemSelectionModeOn.setSelected(true);
		else
			_itemSelectionModeOff.setSelected(true);
		
		_menuSelectionMode.getItems().addAll(_itemSelectionModeOn, _itemSelectionModeOff);
		
		// Bezier color

		final Menu menuBezierColor = new Menu("Bezier color");       
		final ToggleGroup toggleBezierColorGroup = new ToggleGroup();

		final RadioMenuItem itemRedBezierColor = new RadioMenuItem("Red");
		itemRedBezierColor.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				// layeredPane.setBackground(blackBG);
				for ( BezierMesh bez : _the3DView.getBeziers() ) {
					bez.setMaterial(redMaterial);
				}
				_strBezierColorNameProperty.set("RED");
				// System.out.println("RRRRRRRRRRRRRRRRRRRRRRRRRED");
			}
		});
		itemRedBezierColor.setToggleGroup(toggleBezierColorGroup);

		final RadioMenuItem itemDarkGreenBezierColor = new RadioMenuItem("Dark green");
		itemDarkGreenBezierColor.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				for ( BezierMesh bez : _the3DView.getBeziers() ) {
					bez.setMaterial(greenMaterial);
				}
				_strBezierColorNameProperty.set("GREEN");
				// System.out.println("GGGGGGGGGGGGGGGGGGREEEEEN");
			}
		});
		itemDarkGreenBezierColor.setToggleGroup(toggleBezierColorGroup);
		// itemDarkGreenBezierColor.setSelected(true);

		menuBezierColor.getItems().addAll(itemRedBezierColor, itemDarkGreenBezierColor);

		// aircraft menu
		final Menu menuAircraft = new Menu("Aircraft");       
		final MenuItem menuAircraftSection1 = new MenuItem("Section 1");
		menuAircraft.getItems().add(menuAircraftSection1);

		menuAircraftSection1.setDisable(!aircraftAllocatedProperty.get());
		menuAircraftSection1.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				// TODO: add code here
				System.out.println("TODO: Draw fuselage cross-section 1");
			}
		});

		// build the context menu
		final ContextMenu contextMenu = new ContextMenu();
		contextMenu.setHideOnEscape(true);       
		contextMenu.getItems().addAll(
				menuBackground, menuPolygon, menuProjection,
				_menuSelectionMode, // agodemar
				new SeparatorMenuItem(), 
				itemStopCubeRot, itemStopTuxRot, 
				new SeparatorMenuItem(),
				menuSceneAA,
				new SeparatorMenuItem(),
				menuBezierColor, // agodemar
				new SeparatorMenuItem(),
				menuAircraft
				);   

		layeredPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {
				if (contextMenu.isShowing()) {
					contextMenu.hide();
				}
				if (e.getButton() == MouseButton.SECONDARY && !_isMouseDraggedProperty.get()) {
					contextMenu.show(layeredPane, e.getScreenX()+2, e.getScreenY()+2);
				}

				_isMouseDraggedProperty.set(false);

				checkFPS();
			}
		});
		layeredPane.setOnDragDetected(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) { 
				_isMouseDraggedProperty.set(true);
				_notAtVantagePointProperty.set(false);
				checkFPS();
			}
		});         

		// keyboard events here
		handleKeyboard(layeredPane);
		
		//-------------------------------------------------------------------------

		// finally, attach the scene to the stage and show
		
		primaryStage.setTitle("AGODEMAR - OCCFX test");
		primaryStage.setScene(scene);
		primaryStage.show();   

		
		// post-show stuff
		//----------------------------------------------------------		
		// Initial states

		lastTime = System.nanoTime();
//		_theAircraft3DView.createTuxCubeOfDim(3, isTuxRotating, drawMode);
		_the3DView.setVantagePoint(VantagePoint3DView.FRONT);

		//
		fpsTimer = new AnimationTimer() {
			@Override public void handle(long now) {

				if (isUpdateFPS == false) {
					return;
				}

				frameCounter++;

				if (frameCounter > elapsedFrames) {

					final long currTime = System.nanoTime();
					final double duration = ((double)(currTime - lastTime)) / frameCounter;
					lastTime = currTime;

					// frames per second
					final int fps = (int)(1000000000d / duration + 0.5);
					// milliseconds per frame
					final int mpf = (int)(duration/1000000d + 0.5);

					frameCounter = 0;
					elapsedFrames = (int)Math.max(1, (fps/3f)); // update: 3 times per sec
					elapsedFrames = Math.min(100, elapsedFrames); 

					fpsLabel.setText(Integer.toString(fps));
					mpfLabel.setText(Integer.toString(mpf));
				}
				/*
             float fpsf = PerformanceTracker.getSceneTracker(scene).getInstantFPS();
             System.out.println("fps     = " + fpsf + 
                              "\npulses  = " + PerformanceTracker.getSceneTracker(scene).getInstantPulses() +
                              "\nfps avg = " + PerformanceTracker.getSceneTracker(scene).getAverageFPS());
				 */
			}
		};

		// lookup for a group/node
		if (_the3DView.getTestCurvesXform().getChildren().size() != 0) {
			OCCFXForm testXform = lookup(
					_the3DView.getTestCurvesXform(), 
					"#testCurvesXform", 
					OCCFXForm.class);

			System.out.println(
					"------------------------------------\n" +
					"lookup testCurvesXform :: " + testXform + "#<--\n" +
					"------------------------------------"        				
					);
		}

//		// Adding a change listener (lambda expression)
//		aircraftAllocatedProperty.addListener(
//			(ObservableValue<? extends Boolean> ov, Boolean oldAircraftInMemory, Boolean newAircraftInMemory) -> {
//				if (newAircraftInMemory) {
//					aircraftNameLabel.setTextFill(Color.YELLOW);
//					aircraftNameLabel.setText(
//							_theAircraft.get_name() // GlobalData.getTheCurrentAircraft()
//							);
//					menuAircraftSection1.setDisable(!newAircraftInMemory);
//					
//					testViewableCAD(); // (newAircraftInMemory); // TODO: testViewableCAD
//
//				} else {
//					aircraftNameLabel.setTextFill(Color.RED);
//					aircraftNameLabel.setText("null");
//					menuAircraftSection1.setDisable(!newAircraftInMemory);
//				}
//			}
//		);
//		fuselageAllocatedProperty.addListener(
//			(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
//				// code goes here
//				if (newVal) {
//					fuselageNameLabel.setTextFill(Color.YELLOW);
//					fuselageNameLabel.setText(
//						_theAircraft.get_fuselage().get_name() // GlobalData.getTheCurrentAircraft()
//					);
//				} else {
//					fuselageNameLabel.setTextFill(Color.RED);
//					fuselageNameLabel.setText("null");
//				}
//			}
//		);

		// TODO: listeners to aircraft and fuselage
//		theCurrentFuselageProperty.addListener(
//			new InvalidationListener() {
//				@Override
//				public void invalidated(Observable o) {
//					System.out.println(
//							"=========================================================\n" +
//									"theCurrentFuselageProperty INVALIDATE:: The binding is now invalid.\n" +
//									"========================================================="
//							);
//					_theAircraft3DView.drawFuselageSection(
//						((ObjectProperty<MyFuselage>)o).get()
//					);
//				}
//			}
//		);
		// TODO: ChangeListener for eager evaluation 
		theCurrentFuselageProperty.addListener(
			(ObservableValue<? extends Fuselage> ov, Fuselage oldVal, Fuselage newFuselage) -> {
				// code goes here
				System.out.println(
						"=========================================================\n" +
						"theCurrentFuselageProperty CHANGE:: The binding is now invalid.\n" +
						"========================================================="
				);
//				_theAircraft3DView.setPointsPerCurveSection(6);
//				_theAircraft3DView.drawFuselageSection(newFuselage);
			}
		);
		
		_isSelectionModeOnProperty.addListener(
				(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
					// code goes here
					if (newVal) { // select mode ON
						titleLeftLabel.setTextFill(Color.RED);
						titleLeftLabel.setText("Selection Mode ON (ALT+S to toggle)");
						_itemSelectionModeOn.setSelected(true);
					} else { // select mode OFF
						titleLeftLabel.setText("");
						_itemSelectionModeOn.setSelected(false);
					}
				}
			);
		
		
		
		

	}// end of start

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}    

	private void handleKeyboard(Pane pane) {
		pane.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				// System.out.println("setOnKeyPressed :: ");
				switch (event.getCode()) {
				case Z:
					// System.out.println("Z");
					_the3DView.setVantagePoint(VantagePoint3DView.FRONT);
					break;
				case X:
					// System.out.println("X");
					_the3DView.getAxisGroup().setVisible(
							!_the3DView.getAxisGroup().isVisible()
							);
					break;
				case V:
					// System.out.println("V");
					//					_theAircraft3DView.getTuxCubeRotGroup().setVisible(
					//							!_theAircraft3DView.getTuxCubeRotGroup().isVisible()
					//							);
					break;
				case S:
					if (event.isAltDown()) {
						// Toggle selection mode
						System.out.println("ALT+S (toggle Selection Mode)");
						_the3DView.toggleSelectionMode();

						// being this bound to _isSelectionModeOnProperty
						// the updates of MyPane3D controls are managed in
						// _isSelectionModeOnProperty change listener

					}
					break;
				case T:
					// System.out.println("T");
					break;
				}
			}
		});
	}	

	private void checkFPS() {
		boolean isRendering = isTuxRotating || isCubeRotating || _isMouseDraggedProperty.get();
		if (isUpdateFPS != isRendering) {
			isUpdateFPS = isRendering;
		}
		else {
			return;
		}

		if (isUpdateFPS == false) {
			fpsTimer.stop();
			fpsLabel.setText(Integer.toString(0));
			mpfLabel.setText(Integer.toString(0));
		}
		else {
			fpsTimer.start(); 
		}
	}	

	// HUD : head-up display
	private final class HUDLabel extends Label {
		private HUDLabel(String text) {
			this(text, textFont);
		}
		private HUDLabel(String text, Font font) {
			super(text);
			setFont(font);
			setTextFill(Color.WHITE);            
		}
	}    

	// http://stackoverflow.com/questions/12324799/javafx-2-0-fxml-strange-lookup-behaviour
	private <T> T lookup(Node parent, String id, Class<T> clazz) {
		for (Node node : parent.lookupAll(id)) {
			if (node.getClass().isAssignableFrom(clazz)) {
				return (T)node;
			}
		}
		throw new IllegalArgumentException("Parent " + parent + " doesn't contain node with id " + id);
	}

	// TODO: testViewableCAD implementation
	private void testViewableCAD() {


		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// the test object
		// Initialize Aircraft with default parameters

		_theAircraft = new Aircraft(
				ComponentEnum.FUSELAGE, 
				ComponentEnum.WING,
				ComponentEnum.HORIZONTAL_TAIL,
				ComponentEnum.VERTICAL_TAIL,
				ComponentEnum.POWER_PLANT,
				ComponentEnum.NACELLE,
				ComponentEnum.LANDING_GEAR
				);
		_theAircraft.set_name("AGODEMAR:Test:Aircraft");
		
		// Default operating conditions
		OperatingConditions theOperatingConditions = new OperatingConditions();

		// Gotta create this object
		ACAnalysisManager _theAnalysis = new ACAnalysisManager(
				theOperatingConditions,
				_theAircraft,
				AnalysisTypeEnum.AERODYNAMIC);

		_theAnalysis.updateGeometry(_theAircraft);

		aircraftAllocatedProperty.set(true);
		fuselageAllocatedProperty.set(true);
		
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++



		// play around with ObservableMap 

		if (_theAircraft.get_fuselage() != null) {

			BRep_Builder theBuilder = new BRep_Builder();
			TopoDS_Compound theCompound = new TopoDS_Compound();
			theBuilder.makeCompound(theCompound);

			// MyLiftingSurfaceBuilder lsCADBuilder = new MyLiftingSurfaceBuilder(aircraft.get_wing());
			MyFuselageBuilder fusCADBuilder = new MyFuselageBuilder(_theAircraft.get_fuselage());
			boolean solid = false;
			fusCADBuilder.set_makeSolid(solid);
			boolean ruled = true;
			fusCADBuilder.set_makeRuledLoft(ruled);
			boolean closedSplines = false;
			fusCADBuilder.set_closedSplines(closedSplines);
			fusCADBuilder.buildCAD();			
			theBuilder.add(theCompound, fusCADBuilder.getTheCompound());

			// Xform viewableXform = new Xform();

			if (_the3DView != null) {
				OCCFXViewableCAD viewableCADAircraft = new OCCFXViewableCAD(
						_the3DView, // the aircraft 3D view object
						new OCCFXForm(), // the node containing the ViewableCAD
						(TopoDS_Shape)theCompound, // what to add to ViewableCAD
						OCCFXViewableCAD.ShapeType.FACE,
						FUSELAGE_ID
						);

				Fuselage fuselage = _theAircraft.get_fuselage();
				OCCFXFuselageSection theFuselageSection;
				for( int kSection = 1; kSection < fuselage.NUM_SECTIONS_YZ; kSection++) {
					theFuselageSection = new OCCFXFuselageSection(
							fuselage, // MyFuselage object
							kSection // index of fuselage section
							);
					OCCFXViewableCAD sectionViewable = new OCCFXViewableCAD(
							_the3DView, // the 3D view object 
							new OCCFXForm(), // the viewableNode
							(OCCFXMeshExtractor)null, // pass a null, dummy OCCMeshExtractor
							theFuselageSection.getInterpolateBezier(),
							false, // show knots
							false, // show control points
							0.055, // point size
							FUSELAGE_ID, // prefix to Id
							3, // points between two consecutive knots
							6 // points per curve section
							);
				}

				// force fit view
				_the3DView.setSceneDiameter(viewableCADAircraft.getViewableDiameter());
				_the3DView.setVantagePoint(OCCFX3DView.VantagePoint3DView.FRONT);

			}
		} else {
			System.out.println("No ViewableCAD object: fuselage null.");
		}
	}

}// end of class
