package it.unina.daf.jpadcad.occfx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.unina.daf.jpadcad.enums.CADComponentEnum;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.OCCCompSolid;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occfx.OCCFXMeshExtractor.FaceData;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import standaloneutils.MyArrayUtils;

public class OCCFX3DView {
	
	// Observation point setting for 3D view
	public static enum VantagePoint3DView {
		BOTTOM("Bottom (Z+)"),
		FRONT("Front (X+)"),
		TOP("Top (Z-)"),
		BACK("Back (X-)"),
		RIGHT("Rigth (Y-)"),
		LEFT("Left (Y+)");
		
		private String listName;
		
		VantagePoint3DView(String listName) {
			this.listName = listName;
		}
		
		public String getListName() {
			return listName;
		}
		
		public String toString() {
			return listName;
		}
	}
	
	private static final double SHIFT_MULTIPLIER = 10.0;
	private static final double CONTROL_MULTIPLIER = 0.1;
	private static final double ROTATE_MULTIPLIER = 0.1;
	private static final double TRANSLATE_MULTIPLIER = 0.05;
	private static final double SCROLL_MULTIPLIER = 0.025;
	
	private static final double POINT_LIGHT_DISTANCE = 30000;

	private double _axesLength = 1.0;
	private double _axesThickness = 0.1;
	
	private VantagePoint3DView _lastSelectedVantagePoint = VantagePoint3DView.FRONT;
	
	private Scene _theScene = null;
	private SubScene _theSubScene = null;
	
	private Group _theViewingGroup = null;
	private Affine _theViewingAffineRotate = new Affine();
	
	private Translate _theViewingTranslate = new Translate();
	private double _startX = 0;
	private double _startY = 0;

	private PerspectiveCamera _thePerspectiveCamera = null;
	
	private AmbientLight _theAmbientLight = null;
	private PointLight _thePointLight = null;
	
	private Group _theAxesGroup = new Group();
	
	private Transform _theCurrentRotate = null;
	
	private List<TriangleMesh> _theAircraftImportedMesh = new ArrayList<>();
	private List<TriangleMesh> _theFuselageImportedMesh = new ArrayList<>();
	private List<TriangleMesh> _theWingImportedMesh = new ArrayList<>();
	private List<TriangleMesh> _theHTailImportedMesh = new ArrayList<>();
	private List<TriangleMesh> _theVTailImportedMesh = new ArrayList<>();
	private List<TriangleMesh> _theCanardImportedMesh = new ArrayList<>();
	private List<TriangleMesh> _theEnginesImportedMesh = new ArrayList<>();
	private List<TriangleMesh> _theWingFairingImportedMesh = new ArrayList<>();
	private List<TriangleMesh> _theCanardFairingImportedMesh = new ArrayList<>();
	private List<MeshView> _theAircraftImportedMeshView = new ArrayList<>();
	private List<MeshView> _theFuselageImportedMeshView = new ArrayList<>();
	private List<MeshView> _theWingImportedMeshView = new ArrayList<>();
	private List<MeshView> _theHTailImportedMeshView = new ArrayList<>();
	private List<MeshView> _theVTailImportedMeshView = new ArrayList<>();
	private List<MeshView> _theCanardImportedMeshView = new ArrayList<>();
	private List<MeshView> _theEnginesImportedMeshView = new ArrayList<>();
	private List<MeshView> _theWingFairingImportedMeshView = new ArrayList<>();
	private List<MeshView> _theCanardFairingImportedMeshView = new ArrayList<>();
	
	private double maxLength = 0.0;
	private double maxWidth = 0.0;
	private double maxHeight = 0.0;
	
	private Group _theSceneRoot = new Group();
	private Group _theSubSceneRoot = new Group();
	
	private HBox _theSubSceneControls = new HBox();
	private VBox _theSubSceneWithControls = new VBox();
	
	private boolean _viewOnSubScene = false;
	
	private Map<CADComponentEnum, List<OCCShape>> _theAircraftSolidsMap = new HashMap<>();
	private Map<CADComponentEnum, List<TriangleMesh>> _theAircraftMeshMap = new HashMap<>();
	private Map<CADComponentEnum, List<MeshView>> _theAircraftMeshViewMap = new HashMap<>();
	
	public OCCFX3DView(Map<CADComponentEnum, List<OCCShape>> aircraftSolidsMap) {
		init(aircraftSolidsMap, 1024, 800, false);
	}
	
	public OCCFX3DView(Map<CADComponentEnum, List<OCCShape>> aircraftSolidsMap, boolean viewOnSubScene) {			
		init(aircraftSolidsMap, 1024, 800, viewOnSubScene);
	}
	
	public OCCFX3DView(Map<CADComponentEnum, List<OCCShape>> aircraftSolidsMap, double width, double height, boolean viewOnSubScene) {
		init(aircraftSolidsMap, width, height, viewOnSubScene);
	}
	
	private void init(Map<CADComponentEnum, List<OCCShape>> aircraftSolidsMap, double width, double height, boolean viewOnSubScene) {
		
		this._theAircraftSolidsMap = aircraftSolidsMap;
		this._viewOnSubScene = viewOnSubScene;
		
		convertSolidsToMeshViews();
		createBaseScene();
		createScene(width, height, SceneAntialiasing.BALANCED);
		setVantagePoint(_lastSelectedVantagePoint);	
	}
	
	private void convertSolidsToMeshViews() {	
		initializeACMeshMaps();
		
		for (Iterator<CADComponentEnum> iter = _theAircraftSolidsMap.keySet().iterator(); iter.hasNext(); ) {
			CADComponentEnum comp = iter.next();
			List<OCCShape> solids = _theAircraftSolidsMap.get(comp);
			
			OCCCompSolid compSolids = (OCCCompSolid) OCCUtils.theFactory.newCompSolid(
					solids.stream().map(s -> (CADShape) s).collect(Collectors.toList()));
			
			List<TriangleMesh> mesh = extractMesh(compSolids, true);
			List<MeshView> meshView = generateMeshView(mesh);
			
			_theAircraftMeshMap.put(comp, mesh);
			_theAircraftMeshViewMap.put(comp, meshView);
			
			_theAircraftImportedMesh.addAll(mesh);
			_theAircraftImportedMeshView.addAll(meshView);
			
			// Fill each mesh view and triangle mesh list
			switch (comp) {
			
			case FUSELAGE:
				_theFuselageImportedMesh = mesh;
				_theFuselageImportedMeshView = meshView;
				break;
				
			case WING:
				_theWingImportedMesh = mesh;
				_theWingImportedMeshView = meshView;
				break;
				
			case HORIZONTAL:
				_theHTailImportedMesh = mesh;
				_theHTailImportedMeshView = meshView;
				break;
				
			case VERTICAL:
				_theVTailImportedMesh = mesh;
				_theVTailImportedMeshView = meshView;
				break;
				
			case CANARD:
				_theCanardImportedMesh = mesh;
				_theCanardImportedMeshView = meshView;
				break;
				
			case ENGINES:
				_theEnginesImportedMesh = mesh;
				_theEnginesImportedMeshView = meshView;
				
			case WING_FAIRING:
				_theWingFairingImportedMesh = mesh;
				_theWingFairingImportedMeshView = meshView;
				break;
				
			case CANARD_FAIRING:
				_theCanardFairingImportedMesh = mesh;
				_theCanardFairingImportedMeshView = meshView;
				break;		
			}
		}
	}
	
	private void initializeACMeshMaps() {
		
		for (Iterator<CADComponentEnum> iter = _theAircraftSolidsMap.keySet().iterator(); iter.hasNext(); ) {
			CADComponentEnum comp = iter.next();	
			
			_theAircraftMeshMap.put(comp, new ArrayList<>());
			_theAircraftMeshViewMap.put(comp, new ArrayList<>());
		}		
	}
	
	private void createBaseScene() {
		
		// -----------------------------
		// Set up camera and lighting
		// -----------------------------
		
		// Camera
		_thePerspectiveCamera = new PerspectiveCamera(true);
		_thePerspectiveCamera.setVerticalFieldOfView(false);
		_thePerspectiveCamera.setFarClip(250);
		_thePerspectiveCamera.setNearClip(0.1);
		_thePerspectiveCamera.setFieldOfView(44);
		
		// Light
		_thePointLight = new PointLight(Color.LIGHTGREY);
		_thePointLight.setTranslateZ(-POINT_LIGHT_DISTANCE);
		
		_theAmbientLight = new AmbientLight(Color.color(0.1, 0.1, 0.1));
		
		// Set camera and lighting for the viewing group
		_theViewingGroup = new Group(_thePerspectiveCamera, _thePointLight);
		_theViewingGroup.getTransforms().setAll(_theViewingAffineRotate, _theViewingTranslate);
		
	}
	
	private void createScene(double width, double height, SceneAntialiasing sceneAA) {
		if (_viewOnSubScene) {
			_theSubScene = new SubScene(_theSubSceneRoot, width, height, true, sceneAA);
			
			_theSubScene.setFill(Color.TRANSPARENT);
			
			_theSubScene.setCamera(_thePerspectiveCamera);
			_theSubSceneRoot.getChildren().addAll(_theViewingGroup, _theAmbientLight);
			_theSubSceneRoot.getChildren().addAll(_theAircraftImportedMeshView);
			
			calculateMaxDimensions(_theSubSceneRoot);
			buildAxes(_theSubSceneRoot);
			
			// Navigator on sub scene
			final Rotate viewingRotX = new Rotate(0.0, 0.0, 0.0, 0.0, Rotate.X_AXIS);
			final Rotate viewingRotY = new Rotate(0.0, 0.0, 0.0, 0.0, Rotate.Y_AXIS);
			
			_theSubScene.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					
					double multiplier = 1.0;
					
					if (event.isShiftDown()) {
						multiplier = SHIFT_MULTIPLIER;
					}
					
					if (event.isControlDown()) {
						multiplier = CONTROL_MULTIPLIER;
					}
					
					if (event.isPrimaryButtonDown()) {
						viewingRotX.setAngle(multiplier*(_startY - event.getSceneY())*ROTATE_MULTIPLIER);
						viewingRotY.setAngle(multiplier*(event.getSceneX() - _startX)*ROTATE_MULTIPLIER);
						_theViewingAffineRotate.append(viewingRotX.createConcatenation(viewingRotY));
					}
					
					else if (event.isSecondaryButtonDown()) {
						_theViewingTranslate.setX(_theViewingTranslate.getX() + multiplier*(_startX - event.getSceneX())*TRANSLATE_MULTIPLIER);
						_theViewingTranslate.setY(_theViewingTranslate.getY() + multiplier*(_startY - event.getSceneY())*TRANSLATE_MULTIPLIER);
					}
					
					_startX = event.getSceneX();
					_startY = event.getSceneY();
				}
			});
			
			_theSubScene.setOnScroll(new EventHandler<ScrollEvent>() {
				@Override
				public void handle(ScrollEvent event) {
					_theViewingTranslate.setZ(_theViewingTranslate.getZ() - event.getDeltaY()*SCROLL_MULTIPLIER);
				}
			});
			
			_theSubScene.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					_startX = event.getSceneX();
					_startY = event.getSceneY();
				}
			});
			
			_theSubScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					switch (event.getCode()) {
					case A:
						_theAxesGroup.setVisible(!_theAxesGroup.isVisible());
						break;
					case B:
						setVantagePoint(VantagePoint3DView.BACK);
						break;
					case D: 
						setVantagePoint(VantagePoint3DView.BOTTOM);
						break;
					case F:
						setVantagePoint(VantagePoint3DView.FRONT);
						break;
					case L:
						setVantagePoint(VantagePoint3DView.LEFT);
						break;
					case P:
						_theSubSceneRoot.setVisible(!_theSubSceneRoot.isVisible());
						break;
					case R:
						setVantagePoint(VantagePoint3DView.RIGHT);
						break;		
					case T:
						setVantagePoint(VantagePoint3DView.TOP);
						break;											
					default:
						break;
					}
				}
			});
			
		} else {
			_theScene = new Scene(_theSceneRoot, width, height, true, sceneAA);
			
			_theScene.setFill(new RadialGradient(225, 225, 300, 300, 500, false,
	                CycleMethod.NO_CYCLE, new Stop[]
	                { new Stop(0f, Color.LIGHTSKYBLUE),
	                  new Stop(1f, Color.LIGHTBLUE) }));
			
			_theScene.setCamera(_thePerspectiveCamera);
			_theSceneRoot.getChildren().addAll(_theViewingGroup, _theAmbientLight);
			_theSceneRoot.getChildren().addAll(_theAircraftImportedMeshView);
			
			calculateMaxDimensions(_theSceneRoot);
			buildAxes(_theSceneRoot);
			
			// Navigator on sub scene
			final Rotate viewingRotX = new Rotate(0.0, 0.0, 0.0, 0.0, Rotate.X_AXIS);
			final Rotate viewingRotY = new Rotate(0.0, 0.0, 0.0, 0.0, Rotate.Y_AXIS);
			
			_theScene.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					
					double multiplier = 1.0;
					
					if (event.isShiftDown()) {
						multiplier = SHIFT_MULTIPLIER;
					}
					
					if (event.isControlDown()) {
						multiplier = CONTROL_MULTIPLIER;
					}
					
					if (event.isPrimaryButtonDown()) {
						viewingRotX.setAngle(multiplier*(_startY - event.getSceneY())*ROTATE_MULTIPLIER);
						viewingRotY.setAngle(multiplier*(event.getSceneX() - _startX)*ROTATE_MULTIPLIER);
						_theViewingAffineRotate.append(viewingRotX.createConcatenation(viewingRotY));
					}
					
					else if (event.isSecondaryButtonDown()) {
						_theViewingTranslate.setX(_theViewingTranslate.getX() + multiplier*(_startX - event.getSceneX())*TRANSLATE_MULTIPLIER);
						_theViewingTranslate.setY(_theViewingTranslate.getY() + multiplier*(_startY - event.getSceneY())*TRANSLATE_MULTIPLIER);
					}
					
					_startX = event.getSceneX();
					_startY = event.getSceneY();
				}
			});
			
			_theScene.setOnScroll(new EventHandler<ScrollEvent>() {
				@Override
				public void handle(ScrollEvent event) {
					_theViewingTranslate.setZ(_theViewingTranslate.getZ() - event.getDeltaY()*SCROLL_MULTIPLIER);
				}
			});
			
			_theScene.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					_startX = event.getSceneX();
					_startY = event.getSceneY();
				}
			});
			
			_theScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					switch (event.getCode()) {
					case A:
						_theAxesGroup.setVisible(!_theAxesGroup.isVisible());
						break;
					case B:
						setVantagePoint(VantagePoint3DView.BACK);
						break;
					case D: 
						setVantagePoint(VantagePoint3DView.BOTTOM);
						break;
					case F:
						setVantagePoint(VantagePoint3DView.FRONT);
						break;
					case L:
						setVantagePoint(VantagePoint3DView.LEFT);
						break;
					case P:
						_theSceneRoot.setVisible(!_theSceneRoot.isVisible());
						break;
					case R:
						setVantagePoint(VantagePoint3DView.RIGHT);
						break;		
					case T:
						setVantagePoint(VantagePoint3DView.TOP);
						break;											
					default:
						break;
					}
				}
			});
		}		
	}
	
	private void createSubSceneControls() { 
  
//		Button frontView = new Button("", new ImageView(
//				new Image(new FileInputStream("src\\it\\unina\\daf\\jpadcadsandbox\\img\\cad_icons\\frontview.png")))); 
		Button frontView = new Button("FRONT");
//		frontView.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		frontView.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setVantagePoint(VantagePoint3DView.FRONT);
			}
		});
		
//		Button backView = new Button("", new ImageView(
//				new Image(new FileInputStream("src\\it\\unina\\daf\\jpadcadsandbox\\img\\cad_icons\\rearview.png"))));
		Button backView = new Button("REAR");
//		backView.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		backView.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setVantagePoint(VantagePoint3DView.BACK);
			}
		});
		
//		Button topView = new Button("", new ImageView(
//				new Image(new FileInputStream("src\\it\\unina\\daf\\jpadcadsandbox\\img\\cad_icons\\topview.png"))));
		Button topView = new Button("TOP");
//		topView.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		topView.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setVantagePoint(VantagePoint3DView.TOP);
			}
		});
		
//		Button bottomView = new Button("", new ImageView(
//				new Image(new FileInputStream("src\\it\\unina\\daf\\jpadcadsandbox\\img\\cad_icons\\bottomview.png"))));
		Button bottomView = new Button("BOTTOM");
//		bottomView.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		bottomView.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setVantagePoint(VantagePoint3DView.BOTTOM);
			}
		});
		
//		Button leftView = new Button("", new ImageView(
//				new Image(new FileInputStream("src\\it\\unina\\daf\\jpadcadsandbox\\img\\cad_icons\\leftview.png"))));
		Button leftView = new Button("LEFT");
//		leftView.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		leftView.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setVantagePoint(VantagePoint3DView.LEFT);
			}
		});
		
//		Button rightView = new Button("", new ImageView(
//				new Image(new FileInputStream("src\\it\\unina\\daf\\jpadcadsandbox\\img\\cad_icons\\rightview.png"))));
		Button rightView = new Button("RIGHT"); 
//		rightView.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		rightView.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setVantagePoint(VantagePoint3DView.RIGHT);
			}
		});
		
		CheckBox axesToggle = new CheckBox("Toggle axes");
		axesToggle.selectedProperty().addListener(observable -> {
			if (axesToggle.isSelected())
				_theAxesGroup.setVisible(true);
			else
				_theAxesGroup.setVisible(false);
		});
		
		_theSubSceneControls = new HBox(12, 
				frontView,
				backView,
				topView,
				bottomView,
				leftView,
				rightView,
				axesToggle);
		
		_theSubSceneControls.setPadding(new Insets(12));
		_theSubSceneControls.setBackground(new Background(
				new BackgroundFill(
						Color.LIGHTGRAY, 
						null, null)));
		
	}
	
	private void calculateAxesLengths() {
		
		_axesLength = MyArrayUtils.getMax(new Double[] {maxLength, maxWidth, maxHeight})/2;
		_axesThickness = 0.005*_axesLength;
	}
	
	private void buildAxes(Group parentGroup) {
	
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(Color.DARKRED);
		redMaterial.setSpecularColor(Color.RED);
		
		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(Color.DARKGREEN);
		greenMaterial.setSpecularColor(Color.GREEN);
		
		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(Color.DARKBLUE);
		blueMaterial.setSpecularColor(Color.BLUE);
		
		calculateAxesLengths();
		
		final Box xAxis = new Box(_axesLength, _axesThickness, _axesThickness);
		final Box yAxis = new Box(_axesThickness, _axesLength, _axesThickness);
		final Box zAxis = new Box(_axesThickness, _axesThickness, _axesLength);
		
		xAxis.setMaterial(redMaterial);
		yAxis.setMaterial(greenMaterial);
		zAxis.setMaterial(blueMaterial);
		
		_theAxesGroup.getChildren().addAll(xAxis, yAxis, zAxis);
		_theAxesGroup.setVisible(false);
		parentGroup.getChildren().add(_theAxesGroup);
		
	}
	
	public void addControls() {
		
		if (_viewOnSubScene) {
			
			createSubSceneControls();
			_theSubSceneWithControls = new VBox(_theSubSceneControls, _theSubScene);
		}
		
		else {
			
			System.out.println("Controls cannot be added to the Scene, please generate a SubScene instead.");
		}
	}
	
	public Scene exchangeScene(final SceneAntialiasing sceneAA) {
		
		// Clear the current scene
		((Group) _theScene.getRoot()).getChildren().clear();
		_theScene.setCamera(null);
		_theScene.setOnMouseDragged(null);
		_theScene.setOnScroll(null);
		_theScene.setOnMousePressed(null);
		
		// Create and return a new sub scene
		createScene(_theScene.getWidth(), _theScene.getHeight(), sceneAA);
		
		return _theScene;
	}
	
	public SubScene exchangeSubScene(final SceneAntialiasing sceneAA) {
		
		// Clear the current sub scene
		((Group) _theSubScene.getRoot()).getChildren().clear();
		_theSubScene.setCamera(null);
		_theSubScene.setOnMouseDragged(null);
		_theSubScene.setOnScroll(null);
		_theSubScene.setOnMousePressed(null);
		
		// Create and return a new sub scene
		createScene(_theSubScene.getWidth(), _theSubScene.getHeight(), sceneAA);
		
		return _theSubScene;
	}
	
	private void calculateMaxDimensions(Group group) {
		
		maxLength = group.getBoundsInLocal().getWidth();
		maxWidth = group.getBoundsInLocal().getHeight();
		maxHeight = group.getBoundsInLocal().getDepth() - POINT_LIGHT_DISTANCE;
		
	}
	
	private void setVantagePoint(VantagePoint3DView vp) {

		Transform rotate = null;
		
		double viewRadius = 0.0;
		
		double xTrans = 0.0;
		double yTrans = 0.0;
		double zTrans = 0.0;
		
		switch (vp) {

		case BOTTOM:
			rotate = new Rotate(0, Rotate.Z_AXIS);
			
			viewRadius = Math.max(maxWidth, maxLength)/2;
			
			xTrans = 0.0 - maxLength/2;
			yTrans = 0.0;
			zTrans = 0.0 + distToSceneCenter(viewRadius + viewRadius*0.10);
			
			break;
			
		case FRONT:
			Rotate rotateFront1 = new Rotate( 90, Rotate.Y_AXIS);
			Rotate rotateFront2 = new Rotate(-90, Rotate.Z_AXIS);
			rotate = rotateFront1.createConcatenation(rotateFront2);
			
			viewRadius = Math.max(maxWidth, maxHeight)/2;
			
			xTrans = 0.0;
			yTrans = 0.0 + maxHeight/2;
			zTrans = 0.0 + distToSceneCenter(viewRadius - viewRadius*0.10);
			
			break;
			
		case TOP:
			Rotate rotateTop1 = new Rotate(  0, Rotate.Z_AXIS);
			Rotate rotateTop2 = new Rotate(180, Rotate.X_AXIS);
			rotate = rotateTop1.createConcatenation(rotateTop2);
			
			viewRadius = Math.max(maxWidth, maxLength)/2;
			
			xTrans = 0.0 - maxLength/2;
			yTrans = 0.0;
			zTrans = 0.0 + distToSceneCenter(viewRadius + viewRadius*0.10);
			
			break;
			
		case BACK:
			Rotate rotateBack1 = new Rotate(-90, Rotate.Y_AXIS);
			Rotate rotateBack2 = new Rotate( 90, Rotate.Z_AXIS);
			rotate = rotateBack1.createConcatenation(rotateBack2);
			
			viewRadius = Math.max(maxWidth, maxHeight)/2;
			
			xTrans = 0.0;
			yTrans = 0.0 + maxHeight/2;
			zTrans = 0.0 + maxLength + distToSceneCenter(viewRadius - viewRadius*0.10);
			
			break;
			
		case RIGHT:
			Rotate rotateRight1 = new Rotate( 90, Rotate.X_AXIS);
			Rotate rotateRight2 = new Rotate(180, Rotate.Z_AXIS);
			rotate = rotateRight1.createConcatenation(rotateRight2);
			
			viewRadius = Math.max(maxLength, maxHeight)/2;
			
			xTrans = 0.0 + maxLength/2;
			yTrans = 0.0 + maxHeight/2;
			zTrans = 0.0 + distToSceneCenter(viewRadius - viewRadius*0.10);
			
			break;
			
		case LEFT:
			Rotate rotateLeft1 = new Rotate(-90, Rotate.X_AXIS);
			Rotate rotateLeft2 = new Rotate(  0, Rotate.Z_AXIS);
			rotate = rotateLeft1.createConcatenation(rotateLeft2);
			
			viewRadius = Math.max(maxLength, maxHeight)/2;
			
			xTrans = 0.0 - maxLength/2;
			yTrans = 0.0 + maxHeight/2;
			zTrans = 0.0 + distToSceneCenter(viewRadius - viewRadius*0.10);
			
			break;
			
		}
		
		_lastSelectedVantagePoint = vp;
		
		_theViewingAffineRotate.setToTransform(rotate);
		
		_theViewingTranslate.setX(-xTrans);
		_theViewingTranslate.setY(-yTrans);
		_theViewingTranslate.setZ(-zTrans);
	}
	
	private double distToSceneCenter(double sceneRadius) {
		final double borderFactor = 1.0;
		final double fov = _thePerspectiveCamera.getFieldOfView();
		final double c3DWidth = (_viewOnSubScene) ? _theSubScene.getWidth(): _theScene.getWidth();
		final double c3DHeight = (_viewOnSubScene) ? _theSubScene.getHeight(): _theScene.getHeight();
		
		double ratioFactor = 1.0;
		
		if (c3DWidth > c3DHeight) 
			ratioFactor = c3DWidth/c3DHeight;
		
		double distToSceneCenter = borderFactor*ratioFactor*sceneRadius/Math.tan(Math.toRadians(fov/2));
		
		return distToSceneCenter;
	}
	
	private List<TriangleMesh> extractMesh(OCCShape shape, boolean faceReversed) {
		
		// Generate the mesh extractor
		OCCFXMeshExtractor meshExtractor = new OCCFXMeshExtractor(shape.getShape());
		Collection<TopoDS_Face> faces = meshExtractor.getFaces();
		
		// Generate a list of triangle mesh
		List<TriangleMesh> mesh = new ArrayList<>();
		
		mesh.addAll(faces.stream()
			 .map(f -> {
				 FaceData data = new FaceData(f, faceReversed);
				 data.load();
				 return data.getTriangleMesh();
			 })
			 .collect(Collectors.toList()));
		
		return mesh;
	}
	
	private List<MeshView> generateMeshView(List<TriangleMesh> triangleMeshes) {
		
		List<MeshView> meshViews = new ArrayList<>();
		
		meshViews.addAll(triangleMeshes.stream()
					  .map(tm -> {
						  MeshView mw = new MeshView(tm);
						  mw.setDrawMode(DrawMode.FILL);
						  return mw;
					  })
					  .collect(Collectors.toList()));
		
		return meshViews;
	}
	
	public List<TriangleMesh> getAircraftImportedMesh() {
		return _theAircraftImportedMesh;
	}
	
	public List<TriangleMesh> getFuselageImportedMesh() {
		return _theFuselageImportedMesh;
	}
	
	public List<TriangleMesh> getWingImportedMesh() {
		return _theWingImportedMesh;
	}
	
	public List<TriangleMesh> getHTailImportedMesh() {
		return _theHTailImportedMesh;
	}
	
	public List<TriangleMesh> getVTailImportedMesh() {
		return _theVTailImportedMesh;
	}
	
	public List<TriangleMesh> getCanardImportedMesh() {
		return _theCanardImportedMesh;
	}
	
	public List<TriangleMesh> getWingFairingImportedMesh() {
		return _theWingFairingImportedMesh;
	}
	
	public List<TriangleMesh> getCanardFairingImportedMesh() {
		return _theCanardFairingImportedMesh;
	}
	
	public List<MeshView> getAircraftImportedMeshView() {
		return _theAircraftImportedMeshView;
	}
	
	public List<MeshView> getFuselageImportedMeshView() {
		return _theFuselageImportedMeshView;
	}
	
	public List<MeshView> getWingImportedMeshView() {
		return _theWingImportedMeshView;
	}
	
	public List<MeshView> getHTailImportedMeshView() {
		return _theHTailImportedMeshView;
	}
	
	public List<MeshView> getVTailImportedMeshView() {
		return _theVTailImportedMeshView;
	}
	
	public List<MeshView> getCanardImportedMeshView() {
		return _theCanardImportedMeshView;
	}
	
	public List<MeshView> getWingFairingImportedMeshView() {
		return _theWingFairingImportedMeshView;
	}
	
	public List<MeshView> getCanardFairingImportedMeshView() {
		return _theCanardFairingImportedMeshView;
	}
	
	public boolean getViewOnSubScene() {
		return _viewOnSubScene;
	}
	
	public SubScene getSubScene() {
		return _theSubScene;
	}
	
	public Scene getScene() {
		return _theScene;
	}
	
	public PerspectiveCamera getPerspectiveCamera() {
		return _thePerspectiveCamera;
	}
	
	public Affine getViewingAffineRotate() {
		return _theViewingAffineRotate;
	}
	
	public Transform getCurrentRotate() {
		return _theCurrentRotate;
	}
	
	public void setCurrentRotate(Transform rotate) {
		this._theCurrentRotate = rotate;
	}
	
	public Group getSubSceneRoot() {
		return _theSubSceneRoot;
	}
	
	public Group getSceneRoot() {
		return _theSceneRoot;
	}
	
	public Group getAxesGroup() {
		return _theAxesGroup;
	}
	
	public Group getViewingGroup() {
		return _theViewingGroup;
	}
	
	public AmbientLight getAmbientLight() {
		return _theAmbientLight;
	}
	
	public Translate getViewingTranslate() {
		return _theViewingTranslate;
	}
	
	public VBox getSubSceneWithControls() {
		return _theSubSceneWithControls;
	}
	
}
