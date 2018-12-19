package it.unina.daf.jpadcad.occfx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.unina.daf.jpadcad.CADManager.CADComponentEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occfx.OCCFXMeshExtractor.FaceData;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;

public class OCCFX3DView {
	
	// Observation point setting for 3D view
	public static enum VantagePoint3DView {
		SELECT("Select"),
		BOTTOM("Bottom (Z+)"),
		CORNER("Corner"),
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
	
	private double _axisLength = 1.0;
	private double _axisThickness = 0.5;
	
	private VantagePoint3DView _lastSelectedVantagePoint = VantagePoint3DView.FRONT;
	
	private SubScene _theSubScene = null;
	
	private Group _theViewingGroup = null;
	private Affine _theViewingAffineRotate = new Affine();
	
	private Translate _theViewingTranslate = new Translate();
	private double _startX = 0;
	private double _startY = 0;

	private PerspectiveCamera _thePerspectiveCamera = null;
	
	private AmbientLight _theAmbientLight = null;
	private PointLight _thePointLight = null;
	
	private Group _theAxisGroup = null;
	
	private BoundingBox _theBoundingBoxInLocal = null;
	private double _sceneDiameter = 0; // The max dimension of the object that need to be rendered inside the scene (wing span or fuselage length, for example)
	
	private Transform _theCurrentRotate = null;
	
	private List<TriangleMesh> _theAircraftImportedMesh = null;
	private List<TriangleMesh> _theFuselageImportedMesh = null;
	private List<TriangleMesh> _theWingImportedMesh = null;
	private List<TriangleMesh> _theHTailImportedMesh = null;
	private List<TriangleMesh> _theVTailImportedMesh = null;
	private List<TriangleMesh> _theCanardImportedMesh = null;
	private List<TriangleMesh> _theWingFairingImportedMesh = null;
	private List<TriangleMesh> _theCanardFairingImportedMesh = null;
	private List<MeshView> _theAircraftImportedMeshView = null;
	private List<MeshView> _theFuselageImportedMeshView = null;
	private List<MeshView> _theWingImportedMeshView = null;
	private List<MeshView> _theHTailImportedMeshView = null;
	private List<MeshView> _theVTailImportedMeshView = null;
	private List<MeshView> _theCanardImportedMeshView = null;
	private List<MeshView> _theWingFairingImportedMeshView = null;
	private List<MeshView> _theCanardFairingImportedMeshView = null;
	
	private Group _theSubSceneRoot;
	
	private Map<CADComponentEnum, List<OCCShape>> _theAircraftSolidsMap = new HashMap<>();
	private Map<CADComponentEnum, List<TriangleMesh>> _theAircraftMeshMap = new HashMap<>();
	private Map<CADComponentEnum, List<MeshView>> _theAircraftMeshViewMap = new HashMap<>();
	
	// Constructor
	public OCCFX3DView(Map<CADComponentEnum, List<OCCShape>> aircraftSolidsMap) {	
		
		this._theAircraftSolidsMap = aircraftSolidsMap;
		
		convertSolidsToMeshViews();
		createBaseScene();
		createSubScene(1024, 800, SceneAntialiasing.BALANCED);
		setVantagePoint(_lastSelectedVantagePoint);	
	}
	
	private void convertSolidsToMeshViews() {	
		initializeACMeshMaps();
		
		for (Iterator<CADComponentEnum> iter = _theAircraftSolidsMap.keySet().iterator(); iter.hasNext(); ) {
			CADComponentEnum comp = iter.next();
			List<OCCShape> solids = _theAircraftSolidsMap.get(comp);
			
			if (solids.size() != 1) 
				System.err.println("Warning: the number of solids found in " + comp.toString() + " shape list is incorrect!");
			
			List<TriangleMesh> mesh = extractMesh(solids.get(0));
			List<MeshView> meshView = generateMeshView(mesh);
			
			_theAircraftMeshMap.put(comp, mesh);
			_theAircraftMeshViewMap.put(comp, meshView);
			
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
		_thePointLight = new PointLight(Color.WHITE);
		_thePointLight.setTranslateZ(-20000);
		
		_theAmbientLight = new AmbientLight(Color.color(0.3, 0.3, 0.3));
		
		// Set camera and lighting for the viewing group
		_theViewingGroup = new Group(_thePerspectiveCamera, _thePointLight);
		_theViewingGroup.getTransforms().setAll(_theViewingAffineRotate, _theViewingTranslate);
		
	}
	
	private void createSubScene(final double width, final double height, final SceneAntialiasing sceneAA) {
		
		// -------------------------------
		// Sub scene & root
		// -------------------------------
		
		// Generate the sub scene
		_theSubSceneRoot = new Group();
		_theSubScene = new SubScene(_theSubSceneRoot, width, height, true, sceneAA);
//		_theSubScene.setFill(Color.TRANSPARENT);
		_theSubScene.setFill(new RadialGradient(225, 225, 300, 300, 500, false,
                CycleMethod.NO_CYCLE, new Stop[]
                { new Stop(0f, Color.LIGHTSKYBLUE),
                  new Stop(1f, Color.LIGHTBLUE) }));
		
		// Add the perspective camera
		_theSubScene.setCamera(_thePerspectiveCamera);
		
		// Add all to the sub scene
		_theSubSceneRoot.getChildren().addAll(_theViewingGroup, _theAmbientLight);
		
		// Add imported mesh views
		_theSubSceneRoot.getChildren().addAll(_theAircraftImportedMeshView);
		
		// Add the axes
		buildAxes(_theSubSceneRoot);
		
		// Navigator on sub scene
		final Rotate viewingRotX = new Rotate(0.0, 0.0, 0.0, 0.0, Rotate.X_AXIS);
		final Rotate viewingRotY = new Rotate(0.0, 0.0, 0.0, 0.0, Rotate.Y_AXIS);
		
		_theSubScene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				
				double multiplier = 1.0;
				
				if (event.isShiftDown()) {
					multiplier = 10.0;
				}
				
				if (event.isPrimaryButtonDown()) {
					viewingRotX.setAngle(multiplier*(_startY - event.getSceneY())/10);
					viewingRotY.setAngle(multiplier*(event.getSceneX() - _startX)/10);
					_theViewingAffineRotate.append(viewingRotX.createConcatenation(viewingRotY));
				}
				
				else if (event.isSecondaryButtonDown()) {
					_theViewingTranslate.setX(_theViewingTranslate.getX() + multiplier*(_startX - event.getSceneX())/100);
					_theViewingTranslate.setY(_theViewingTranslate.getY() + multiplier*(_startY - event.getSceneY())/100);
				}
				
				else if (event.isMiddleButtonDown()) {
					_theViewingTranslate.setZ(_theViewingTranslate.getZ() + multiplier*(event.getSceneY() - _startY)/40);
				}
				
				_startX = event.getSceneX();
				_startY = event.getSceneY();
			}
		});
		
		_theSubScene.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				_theViewingTranslate.setZ(_theViewingTranslate.getZ() - event.getDeltaY()/40);
			}
		});
		
		_theSubScene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				_startX = event.getSceneX();
				_startY = event.getSceneY();
			}
		});
	}
	
	private void buildAxes(Group parentGroup) {
		Group axis = new Group();
		
		// Generate material objects for each axis
		final PhongMaterial whiteMaterial = new PhongMaterial();
		whiteMaterial.setDiffuseColor(Color.WHITE);
		
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(Color.DARKRED);
		redMaterial.setSpecularColor(Color.RED);
		
		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(Color.DARKGREEN);
		greenMaterial.setSpecularColor(Color.GREEN);
		
		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(Color.DARKBLUE);
		blueMaterial.setSpecularColor(Color.BLUE);
		
		// Generate cylinders for each axis and a sphere for the center of the reference frame
		final Sphere sphere = new Sphere(2*_axisThickness);
		final Cylinder xAxis = new Cylinder(_axisThickness, _axisLength);
		final Cylinder yAxis = new Cylinder(_axisThickness, _axisLength);
		final Cylinder zAxis = new Cylinder(_axisThickness, _axisLength);
		
		sphere.setMaterial(redMaterial);
		xAxis.setMaterial(whiteMaterial);
		yAxis.setMaterial(whiteMaterial);
		zAxis.setMaterial(whiteMaterial);
		
		xAxis.setRotationAxis(Rotate.Z_AXIS);
		xAxis.setTranslateX(_axisLength/2);
		xAxis.setRotate(90);
		
		yAxis.setTranslateY(-_axisLength/2);
		
		zAxis.setRotationAxis(Rotate.X_AXIS);
		zAxis.setTranslateZ(-_axisLength/2);
		zAxis.setRotate(90);
		
		axis.getChildren().addAll(sphere, xAxis, yAxis, zAxis);
		
		// Generate a cone for each axis
		float coneRadius = (float) (2*_axisThickness);
		float coneHeight = (float) (0.25*_axisLength);
		
		TriangleMesh coneMeshX = createCone(coneRadius, coneHeight);		
		TriangleMesh coneMeshY = createCone(coneRadius, coneHeight);		
		TriangleMesh coneMeshZ = createCone(coneRadius, coneHeight);	
		MeshView xCone = new MeshView(coneMeshX);
		MeshView yCone = new MeshView(coneMeshY);
		MeshView zCone = new MeshView(coneMeshZ);
		
		xCone.setMaterial(redMaterial);
		xCone.setTranslateY(-coneRadius);
		xCone.setRotationAxis(Rotate.Z_AXIS);		
		xCone.setRotate(90);
		xCone.setTranslateX(_axisLength);
		xCone.setDrawMode(DrawMode.FILL);
		
		yCone.setMaterial(greenMaterial);
		yCone.setTranslateY(-_axisLength);
		yCone.setDrawMode(DrawMode.FILL);
		
		zCone.setMaterial(blueMaterial);
		zCone.setTranslateY(-coneRadius);
		zCone.setRotationAxis(Rotate.X_AXIS);	
		zCone.setRotate(90);
		zCone.setTranslateZ(-_axisLength);
		zCone.setDrawMode(DrawMode.FILL);
		
		axis.getChildren().addAll(xCone, yCone, zCone);
		
		// Populate the axis group
		_theAxisGroup.getChildren().add(axis);
		
	}
	
	private TriangleMesh createCone(float radius, float height) {
		float x, z;
		double angle;
		
		int divisions = 500;
		double segmentAngle = 2.0*Math.PI/divisions;	
		double halfCount = Math.PI/2 - Math.PI/(divisions/2);
		
		TriangleMesh mesh = new TriangleMesh();
		mesh.getPoints().addAll(0, 0, 0);
		
		for (int i = divisions + 1; --i > 0; ) {
			angle = segmentAngle*i;
			x = (float) (radius*Math.cos(angle - halfCount));
			z = (float) (radius*Math.sin(angle - halfCount));
			mesh.getPoints().addAll(x, height, z);
		}
		mesh.getPoints().addAll(0, height, 0);
		
		mesh.getTexCoords().addAll(0, 0);
		
		for (int i = 1; i < divisions; i++) {
			mesh.getFaces().addAll(
					0, 0, i+1, 0, i, 0,
					divisions+2, 0, i, 0, i+1, 0
					);
		}
		
		return mesh;
	}
	
	public SubScene exchangeSubScene(final SceneAntialiasing sceneAA) {
		
		// Clear the current sub scene
		((Group) _theSubScene.getRoot()).getChildren().clear();
		_theSubScene.setCamera(null);
		_theSubScene.setOnMouseDragged(null);
		_theSubScene.setOnScroll(null);
		_theSubScene.setOnMousePressed(null);
		
		// Create and return a new sub scene
		createSubScene(_theSubScene.getWidth(), _theSubScene.getHeight(), sceneAA);
		
		return _theSubScene;
	}
	
	public void setVantagePoint(VantagePoint3DView vp) {

		Transform rotate = null;

		final double distance = distToSceneCenter(_sceneDiameter/2);
		
		switch (vp) {
		
		case BOTTOM:
			rotate = new Rotate(-90, Rotate.Z_AXIS);
			break;
		case CORNER:
			Rotate rotateCorner1 = new Rotate( 90, Rotate.X_AXIS);
			Rotate rotateCorner2 = new Rotate(-30, new Point3D(-1, -1, 0).normalize());
			rotate = rotateCorner1.createConcatenation(rotateCorner2);
			break;
		case FRONT:
			Rotate rotateFront1 = new Rotate( 90, Rotate.Y_AXIS);
			Rotate rotateFront2 = new Rotate(-90, Rotate.Z_AXIS);
			rotate = rotateFront1.createConcatenation(rotateFront2);
			break;
		case TOP:
			Rotate rotateTop1 = new Rotate(-90, Rotate.Z_AXIS);
			Rotate rotateTop2 = new Rotate(180, Rotate.X_AXIS);
			rotate = rotateTop1.createConcatenation(rotateTop2);
			break;
		case BACK:
			Rotate rotateBack1 = new Rotate(-90, Rotate.Y_AXIS);
			Rotate rotateBack2 = new Rotate( 90, Rotate.Z_AXIS);
			rotate = rotateBack1.createConcatenation(rotateBack2);
			break;
		case RIGHT:
			Rotate rotateRight1 = new Rotate( 90, Rotate.X_AXIS);
			Rotate rotateRight2 = new Rotate(180, Rotate.Z_AXIS);
			rotate = rotateRight1.createConcatenation(rotateRight2);
			break;
		case LEFT:
			Rotate rotateLeft1 = new Rotate(-90, Rotate.X_AXIS);
			Rotate rotateLeft2 = new Rotate(  0, Rotate.Z_AXIS);
			rotate = rotateLeft1.createConcatenation(rotateLeft2);
			break;
		default:
			break;
		}
		
		_lastSelectedVantagePoint = vp;
		
		_theViewingAffineRotate.setToTransform(rotate);
		
		_theViewingTranslate.setX(0);
		_theViewingTranslate.setY(0);
		_theViewingTranslate.setZ(-distance);
	}
	
	private double distToSceneCenter(double sceneRadius) {
		final double borderFactor = 1.0;
		final double fov = _thePerspectiveCamera.getFieldOfView();
		final double c3DWidth = _theSubScene.getWidth();
		final double c3DHeight = _theSubScene.getHeight();
		
		double ratioFactor = 1.0;
		
		if (c3DWidth > c3DHeight) 
			ratioFactor = c3DWidth/c3DHeight;
		
		double distToSceneCenter = borderFactor*ratioFactor*sceneRadius/Math.tan(Math.toRadians(fov/2));
		
		return distToSceneCenter;
	}
	
	private List<TriangleMesh> extractMesh(OCCShape shape) {
		
		// Generate the mesh extractor
		OCCFXMeshExtractor meshExtractor = new OCCFXMeshExtractor(shape.getShape());
		Collection<TopoDS_Face> faces = meshExtractor.getFaces();
		
		// Generate a list of triangle mesh
		List<TriangleMesh> mesh = new ArrayList<>();
		
		mesh.addAll(faces.stream()
			 .map(f -> {
				 FaceData data = new FaceData(f, true);
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
	
	public SubScene getSubScene() {
		return _theSubScene;
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
	
	public double getSceneDiameter() {
		return _sceneDiameter;
	}
	
	public void setSceneDiameter(double sceneDiameter) {
		this._sceneDiameter = sceneDiameter;
	}
	
	public Group getAxisGroup() {
		return _theAxisGroup;
	}
	
}
