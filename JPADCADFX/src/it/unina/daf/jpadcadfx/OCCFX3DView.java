package it.unina.daf.jpadcadfx;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.fxyz3d.scene.CubeViewer;
//import org.fxyz3d.shapes.primitives.BezierMesh;
//import org.fxyz3d.shapes.primitives.helper.InterpolateBezier;
//
//import com.interactivemesh.jfx.importer.ImportException;
//import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
//
//import configuration.MyConfiguration;
//import javafx.beans.property.BooleanProperty;
//import javafx.beans.property.SimpleBooleanProperty;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.event.EventHandler;
//import javafx.geometry.BoundingBox;
//import javafx.geometry.Point3D;
//import javafx.scene.AmbientLight;
//import javafx.scene.Group;
//import javafx.scene.Node;
//import javafx.scene.PerspectiveCamera;
//import javafx.scene.PointLight;
//import javafx.scene.SceneAntialiasing;
//import javafx.scene.SubScene;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.input.PickResult;
//import javafx.scene.input.ScrollEvent;
//import javafx.scene.paint.Color;
//import javafx.scene.paint.Material;
//import javafx.scene.paint.PhongMaterial;
//import javafx.scene.shape.Box;
//import javafx.scene.shape.CullFace;
//import javafx.scene.shape.Cylinder;
//import javafx.scene.shape.DrawMode;
//import javafx.scene.shape.Mesh;
//import javafx.scene.shape.MeshView;
//import javafx.scene.shape.Sphere;
//import javafx.scene.shape.TriangleMesh;
//import javafx.scene.transform.Affine;
//import javafx.scene.transform.Rotate;
//import javafx.scene.transform.Transform;
//import javafx.scene.transform.Translate;
//
//public class OCCFX3DView { // evolves from MyFXAircraft3DView
//	
//	OCCFXMeshExtractor _theOCCFXMeshExtractor;
//	// keep track of mesh-Ids in the scene
//	private Map <TriangleMesh, String> _theTriangleMeshMap = new HashMap<TriangleMesh, String>();
//
//	private double _axisXLength = 1.0;
//	private double _axisYLength = 1.0;
//	private double _axisZLength = 1.0;
//	private double _axisThickness = 0.5;
//
//	// vantage point, i.e. observation point setting for 3D view
//	public static enum VantagePoint3DView {
//		Select("Select"),
//		BOTTOM("Bottom (Z+)"), 
//		CORNER("Corner"), 
//		FRONT("Front (X+)"), 
//		TOP("Top (Z-)"), 
//		BACK("Back (X-)"),
//		RIGHT("Right (Y-)"),
//		LEFT("Left (Y+)")
//		;
//		VantagePoint3DView(String listName) {
//			this.listName = listName;
//		}
//		private String listName;
//		public String getListName() {
//			return listName;
//		}
//		public String toString() {
//            return listName;
//        }
//	}
//	
//	private OCCFX3DView.VantagePoint3DView _lastSelectedVantagePoint = OCCFX3DView.VantagePoint3DView.FRONT;
//
//	private SubScene _theSubScene = null;
//
//	private Group _theViewingGroup = null; 
//	private Affine _theViewingAffineRotate = new Affine();  
//
//	private Translate _theViewingTranslate = new Translate();
//	private double _startX = 0;
//	private double _startY = 0;
//
//	private PerspectiveCamera _thePerspectiveCamera = null;  
//
//	private AmbientLight _theAmbientLight = null;
//	private PointLight _thePointLight = null;
//
//	private Group _theAxisGroup = null;
//
//	private BoundingBox _theBoundingBoxInLocal = null;
//	private double _sceneDiameter =  0;
//
//	private Transform _theCurrentRotate = null;
//	
//	private int pointsBetweenTwoConsecutiveKnots = 3;
//	private int pointsPerCurveSection = 4;
//	
//	private MeshView[] _theMeshViews = null;
//
//	private TriangleMesh _theFuselageImportedMesh = null;
//	private MeshView _theFuselageImportedMeshView = null;
//
//	private OCCFXForm testCurvesXform = new OCCFXForm();
//	private List<BezierMesh> beziers = new ArrayList<BezierMesh>();
//
//	private OCCFXForm xform = new OCCFXForm();
//
//	private CubeViewer cubeViewer;
//	private Group _subSceneRoot;
//	
//	private ObservableList<MeshView> _selectedMeshViewObservableList = FXCollections.observableArrayList();
//	private ObservableList<BezierMesh> _selectedBezierMeshObservableList = FXCollections.observableArrayList();
//
//	// TODO: use the list of ViewableCAD-related Xform for selection
//	private ObservableList<OCCFXForm> _selectedViewableXformObservableList = FXCollections.observableArrayList();
//
//	public static final String SELECTABLE_ID_GENERIC_FACE = new String("Generic:Face");
//	public static final String SELECTABLE_ID_FUSELAGE_FACE = new String("Fuselage:Face");
//	public static final String SELECTABLE_ID_WING_FACE = new String("Wing:Face");
//	public static final String SELECTABLE_ID_HTAIL_FACE = new String("HorizontalTail:Face");
//	public static final String SELECTABLE_ID_VTail_FACE = new String("VerticalTail:Face");
//	
//	public static final List<String> selectableId = 
//		Arrays.asList(
//			SELECTABLE_ID_GENERIC_FACE,
//			SELECTABLE_ID_FUSELAGE_FACE, 
//			SELECTABLE_ID_WING_FACE, 
//			SELECTABLE_ID_HTAIL_FACE
//			);
//	
//	final static PhongMaterial SELECTED_MATERIAL = new PhongMaterial();
//	
//	// boolean _selectionModeOn = false;
//	private BooleanProperty _selectionModeOnProperty = new SimpleBooleanProperty(false);
//	
//	// Constructor
//	public OCCFX3DView() {
//		createBaseScene();
//		createSubScene(800, 800, SceneAntialiasing.BALANCED);
//		setVantagePoint(_lastSelectedVantagePoint);
//		
//		buildTestCurves();
//		
//	}
//
//	private void buildTestCurves() {
//
//		// see https://github.com/Birdasaur/FXyz
//
//		Rotate rotateY = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
//
//		// the control points TODO
//		List<org.fxyz3d.geometry.Point3D> knots = Arrays.asList(
//				new org.fxyz3d.geometry.Point3D(0f,0f,0f),
//				new org.fxyz3d.geometry.Point3D(0,5,5),
//				new org.fxyz3d.geometry.Point3D(0,10,-10),
//				new org.fxyz3d.geometry.Point3D(10,15,-7),
//				new org.fxyz3d.geometry.Point3D(15,5,-4),
//				new org.fxyz3d.geometry.Point3D(0,-1,0),
//				new org.fxyz3d.geometry.Point3D(0,-3,0)
//				);
//
//		boolean showControlPoints=true;
//		boolean showKnots=true;
//
//		// the interpolating bezier
//		InterpolateBezier interpolate = new InterpolateBezier(knots);
//
//		AtomicInteger sp=new AtomicInteger();
//		if(showKnots || showControlPoints){
//			interpolate.getSplines().forEach(spline->{ // <===================== LAMBDA
//				org.fxyz3d.geometry.Point3D k0=spline.getPoints().get(0);
//				org.fxyz3d.geometry.Point3D k1=spline.getPoints().get(1);
//				org.fxyz3d.geometry.Point3D k2=spline.getPoints().get(2);
//				org.fxyz3d.geometry.Point3D k3=spline.getPoints().get(3);
//				if(showKnots){
//					Sphere s=new Sphere(0.2d);
//					s.getTransforms().add(new Translate(k0.x, k0.y, k0.z));
//					s.setMaterial(new PhongMaterial(Color.GREENYELLOW));
//					testCurvesXform.getChildren().add(s);
//					s=new Sphere(0.4d);
//					s.getTransforms().add(new Translate(k3.x, k3.y, k3.z));
//					s.setMaterial(new PhongMaterial(Color.GREENYELLOW));
//					testCurvesXform.getChildren().add(s);
//				}
//				if(showControlPoints){
//					org.fxyz3d.geometry.Point3D dir=k1.substract(k0).crossProduct(new org.fxyz3d.geometry.Point3D(0,-1,0));
//					double angle=Math.acos(k1.substract(k0).normalize().dotProduct(new org.fxyz3d.geometry.Point3D(0,-1,0)));
//					double h1=k1.substract(k0).magnitude();
//					Cylinder c=new Cylinder(0.03d,h1);
//					c.getTransforms().addAll(new Translate(k0.x, k0.y-h1/2d, k0.z),
//							new Rotate(-Math.toDegrees(angle), 0d,h1/2d,0d,
//									new javafx.geometry.Point3D(dir.x,-dir.y,dir.z)));
//					c.setMaterial(new PhongMaterial(Color.GREEN));
//					testCurvesXform.getChildren().add(c);
//
//					dir=k2.substract(k1).crossProduct(new org.fxyz3d.geometry.Point3D(0,-1,0));
//					angle=Math.acos(k2.substract(k1).normalize().dotProduct(new org.fxyz3d.geometry.Point3D(0,-1,0)));
//					h1=k2.substract(k1).magnitude();
//					c=new Cylinder(0.03d,h1);
//					c.getTransforms().addAll(new Translate(k1.x, k1.y-h1/2d, k1.z),
//							new Rotate(-Math.toDegrees(angle), 0d,h1/2d,0d,
//									new javafx.geometry.Point3D(dir.x,-dir.y,dir.z)));
//					c.setMaterial(new PhongMaterial(Color.GREEN));
//					testCurvesXform.getChildren().add(c);
//
//					dir=k3.substract(k2).crossProduct(new org.fxyz3d.geometry.Point3D(0,-1,0));
//					angle=Math.acos(k3.substract(k2).normalize().dotProduct(new org.fxyz3d.geometry.Point3D(0,-1,0)));
//					h1=k3.substract(k2).magnitude();
//					c=new Cylinder(0.03d,h1);
//					c.getTransforms().addAll(new Translate(k2.x, k2.y-h1/2d, k2.z),
//							new Rotate(-Math.toDegrees(angle), 0d,h1/2d,0d,
//									new javafx.geometry.Point3D(dir.x,-dir.y,dir.z)));
//					c.setMaterial(new PhongMaterial(Color.GREEN));
//					testCurvesXform.getChildren().add(c);
//
//					Sphere s=new Sphere(0.1d);
//					s.getTransforms().add(new Translate(k1.x, k1.y, k1.z));
//					s.setMaterial(new PhongMaterial(Color.RED));
//					testCurvesXform.getChildren().add(s);
//					s=new Sphere(0.1d);
//					s.getTransforms().add(new Translate(k2.x, k2.y, k2.z));
//					s.setMaterial(new PhongMaterial(Color.RED));
//					testCurvesXform.getChildren().add(s);
//				}
//			});
//		}
//		long time=System.currentTimeMillis();
//		interpolate.getSplines().stream().forEach(spline->{ // <===================== LAMBDA
//			BezierMesh bezier = new BezierMesh(
//					spline, // BezierHelper spline
//					0.1d, // double wireRadius
//					30, // int rDivs (n. points between two consecutive knots)
//					10, // int tDivs
//					0, // int lengthCrop
//					0 // int wireCrop
//					);
//			// bezier.setDrawMode(DrawMode.LINE);
//			bezier.setCullFace(CullFace.NONE);
//			//          bezier.setSectionType(SectionType.TRIANGLE);
//			// NONE
//			bezier.setTextureModeNone(
//					Color.CRIMSON
//					// Color.hsb(360d*sp.getAndIncrement()/interpolate.getSplines().size(), 1, 1)
//					);
//			// IMAGE
//			//          bezier.setTextureModeImage(getClass().getResource("res/LaminateSteel.jpg").toExternalForm());
//			// PATTERN
//			// bezier.setTextureModePattern(3d);
//			// FUNCTION
//			// bezier.setTextureModeVertices1D(256*256,t->spline.getKappa(t)); // t -> Math.cos( 2.0 * Math.PI * spline.getKappa(t) )
//			// DENSITY
//			//          bezier.setTextureModeVertices3D(256*256,dens);
//			// FACES
//			// bezier.setTextureModeFaces(256*256);
//
//			bezier.getTransforms().addAll(new Rotate(0,Rotate.X_AXIS),rotateY);
//			beziers.add(bezier);
//		});
//		System.out.println("time: "+(System.currentTimeMillis()-time)); //43.815->25.606->15
//
//		// add bezier to group member variable
//		testCurvesXform.setId("testCurvesXform");
//		testCurvesXform.getChildren().addAll(beziers);
//
//	}
//
//
//	private void createBaseScene() {
//
//		//
//		// Viewing : Camera & Light
//		//
//
//		// SubScene's camera
//		_thePerspectiveCamera = new PerspectiveCamera(true);
//		_thePerspectiveCamera.setVerticalFieldOfView(false);
//		_thePerspectiveCamera.setFarClip(250);
//		_thePerspectiveCamera.setNearClip(0.1);
//		_thePerspectiveCamera.setFieldOfView(44);
//
//		// SubScene's lights
//		_thePointLight = new PointLight(Color.WHITE);
//		_thePointLight.setTranslateZ(-20000);
//
//		_theAmbientLight = new AmbientLight(Color.color(0.3, 0.3, 0.3));
//
//		// Viewing group: camera and headlight
//		_theViewingGroup = new Group(_thePerspectiveCamera, _thePointLight);
//		_theViewingGroup.getTransforms().setAll(_theViewingAffineRotate, _theViewingTranslate);
//
//
//	}
//
//	private void createSubScene(final double width, final double height, final SceneAntialiasing sceneAA) {
//
//		//
//		// SubScene & Root 
//		//
//		_subSceneRoot = new Group();
//
//		_theSubScene = new SubScene(_subSceneRoot, width, height, true, sceneAA);
//
//		// otherwise _theSubScene doesn't receive mouse events
//		// TODO bug ??   
//		_theSubScene.setFill(Color.TRANSPARENT);
//
//		// Perspective camera
//		_theSubScene.setCamera(_thePerspectiveCamera);
//
//		// Add all to SubScene
//		_subSceneRoot.getChildren().addAll(
//				_theViewingGroup, _theAmbientLight
//				);
//
//		// Add Cube
//		if (cubeViewer != null) {
//			_subSceneRoot.getChildren().add(cubeViewer);
//		}
//
//		// Add imported fuselage
//		if (_theFuselageImportedMeshView != null)
//			_subSceneRoot.getChildren().add(_theFuselageImportedMeshView);
//
//		// Add curves
//		_subSceneRoot.getChildren().add(testCurvesXform);
//
//		// Add wing mesh
//		_subSceneRoot.getChildren().add(xform);
//
//		// Add fuselage section
////		_theFuselageSectionXform.setId("Fuselage::Sections");
////		_subSceneRoot.getChildren().add(_theFuselageSectionXform);
//
//		// add axes
//		buildAxes(_subSceneRoot);
//
//		// Navigator on SubScene
//
//		final Rotate viewingRotX = new Rotate(0, 0,0,0, Rotate.X_AXIS);      
//		final Rotate viewingRotY = new Rotate(0, 0,0,0, Rotate.Y_AXIS);        
//
//		_theSubScene.setOnMouseDragged(new EventHandler<MouseEvent>() {
//			@Override public void handle(MouseEvent event) {
//				// System.out.println("OnMouseDragged " + event);
//				// System.out.println("isPrimaryButtonDown? " + event.isPrimaryButtonDown());
//				// System.out.println("viewingRotX angle 1) " + viewingRotX.getAngle());
//				// System.out.println("event.getButton() " + event.getButton());
//
//				// TODO: no movement in select mode (acceptable??)
//				if (_selectionModeOnProperty.get()) {					
//					return; // no movement in select mode
//				}
//				
//				double multiplier = 1.0;
//				if (event.isShiftDown()) {
//					multiplier = 10.0;
//				}
//				if (event.isPrimaryButtonDown()) {
//					viewingRotX.setAngle(multiplier*(_startY - event.getSceneY())/10); 
//					// System.out.println("viewingRotX angle 2) " + viewingRotX.getAngle());
//					viewingRotY.setAngle(multiplier*(event.getSceneX() - _startX)/10);                                        
//					_theViewingAffineRotate.append(viewingRotX.createConcatenation(viewingRotY));
//				}
//				else if (event.isSecondaryButtonDown()) {
//					_theViewingTranslate.setX(_theViewingTranslate.getX() + multiplier*(_startX - event.getSceneX())/100);
//					_theViewingTranslate.setY(_theViewingTranslate.getY() + multiplier*(_startY - event.getSceneY())/100);
//				}
//				else if (event.isMiddleButtonDown()) {
//					_theViewingTranslate.setZ(_theViewingTranslate.getZ() + multiplier*(event.getSceneY() - _startY)/40);
//				}
//
//				_startX = event.getSceneX();
//				_startY = event.getSceneY();
//			}
//		});
//		_theSubScene.setOnScroll(new EventHandler<ScrollEvent>() {
//			@Override public void handle(ScrollEvent event) {
//				// System.out.println("OnScroll event.getDeltaY() = " + event.getDeltaY());
//				_theViewingTranslate.setZ(_theViewingTranslate.getZ() - event.getDeltaY()/40);
//			}
//		});
//		_theSubScene.setOnMousePressed(new EventHandler<MouseEvent>() {
//			@Override public void handle(MouseEvent event) {
//				_startX = event.getSceneX();
//				_startY = event.getSceneY();
//				// System.out.println("OnMousePressed = " + event);
//				// System.out.println("event.getButton() " + event.getButton());
//			}
//		});
//
//		// test picking
//		// TODO: refactor pick logic and make more general
//		
//		_theSubScene.setOnMouseClicked( (event) -> { // ================> LAMBDA
//			PickResult res = event.getPickResult();
//			System.out.println("PickResult:: " + res);
//			
//			// BezierMesh
//			if (res.getIntersectedNode() instanceof BezierMesh){
//				
//				BezierMesh bmPicked = (BezierMesh) res.getIntersectedNode();
//				
//				System.out.println("BezierMesh Id: " + bmPicked.getId() );
//				System.out.println("Parent Id: " + bmPicked.getParent().getId() );
//
//				if (bmPicked.getParent().getId() != null) { 
//					
//					if (isSelectionModeOn()) { 
//						
//						if (
//								// !alreadySelectedMeshView
//								!_selectedBezierMeshObservableList.contains(bmPicked)
//								) {
//							_selectedBezierMeshObservableList.add(bmPicked);
//						} else {
//							_selectedBezierMeshObservableList.remove(bmPicked);
//						}
//						
//						// TODO: try coloring all objects
//						String parentXformId = bmPicked.getParent().getId();
//						if (parentXformId.indexOf("FUSELAGE::SECTIONS") != -1) { // MyPane3D.FUSELAGE_ID+":SECTIONS"
//							if (bmPicked.getParent() instanceof OCCFXForm) {
//								OCCFXForm parentXform = (OCCFXForm) bmPicked.getParent();
//
//								// color all mesh views
//								if (
//										// !alreadySelectedXform
//										!_selectedViewableXformObservableList.contains(parentXform)										
//										) {
//									
//									// update Xform selection list
//									_selectedViewableXformObservableList.add(parentXform);
//									
//									for(Node p : parentXform.getChildren()) {
//										if (p instanceof BezierMesh) {
//											// color all BezierMesh objects accordingly
//											BezierMesh b = ((BezierMesh) p);
//											b.setMaterial(
//													new PhongMaterial(event.isShiftDown() ? Color.CHARTREUSE : Color.DARKORANGE));
//										}
//									}
//									
//								} else {
//									
//									_selectedViewableXformObservableList.remove(parentXform);
//									
//									for(Node p : parentXform.getChildren()) {
//										if (p instanceof BezierMesh) {
//											// color all BezierMesh objects accordingly
//											BezierMesh b = ((BezierMesh) p);
//											b.setMaterial(
//													new PhongMaterial(event.isShiftDown() ? Color.BLUEVIOLET : Color.YELLOW));
//										}
//									}
//								}
//							}
//						}
//					}					
//				}
//			}
//			
//			// MeshView
//			if (res.getIntersectedNode() instanceof MeshView){
//				System.out.println("Id: " + ((MeshView) res.getIntersectedNode()).getId() );
//				if (((MeshView) res.getIntersectedNode()).getId() != null) {
//					
//					// TODO: manage pick/selection of Viewable/MeshView objects
//					
//					if (isSelectionModeOn()) {
//						
//						// check if MeshView object was already selected
//						MeshView mvPicked = (MeshView) res.getIntersectedNode();
////						String idPicked = mvPicked.getId();
////						long countFilteredMeshViews =
////							_selectedMeshViewObservableList.stream() // of VewableCAD
////							.filter(meshView -> meshView.getId().equals(idPicked))
////							.count();
////						boolean alreadySelectedMeshView = (countFilteredMeshViews > 0);
////						System.out.println("---------------------------------------------------");
////						System.out.println("PickResult::Parent Xform: " + mvPicked.getParent().getId());
////						System.out.println("PickResult:: already Picked?: " + alreadySelectedMeshView);
////						System.out.println("Count filtered: " + countFilteredMeshViews);
////						System.out.println("Selected MeshView objects: " + _selectedMeshViewObservableList);
////						System.out.println("MeshView count: " + _selectedMeshViewObservableList.size());
////						System.out.println("---------------------------------------------------");
//						
//						// update selected-object-list
//						if (
//							// !alreadySelectedMeshView
//							!_selectedMeshViewObservableList.contains(mvPicked)
//								) {
//							_selectedMeshViewObservableList.add(mvPicked);
//						} else {
//							_selectedMeshViewObservableList.remove(mvPicked);
//						}
//						
//						// TODO: try coloring fuselage-related MeshView objects
//						String parentXformId = mvPicked.getParent().getId();
//						if (parentXformId.indexOf("FUSELAGE") != -1) { // MyPane3D.FUSELAGE_ID TODO: move ID constant in a static class??
//							if (mvPicked.getParent() instanceof OCCFXForm) {
//								OCCFXForm parentXform = (OCCFXForm) mvPicked.getParent();
//								
////								long countFilteredXforms =
////										_selectedViewableXformObservableList.stream() // of Xform
////										.filter(xform -> xform.getId().equals(parentXformId))
////										.count();
////								boolean alreadySelectedXform = (countFilteredXforms > 0);
////								
////								System.out.println("###################################################");
////								System.out.println("Select Xform: " + parentXform.getId());
////								System.out.println("PickResult:: already Picked?: " + alreadySelectedXform);
////								System.out.println("Count filtered: " + countFilteredXforms);
////								System.out.println("###################################################");
//
//								// color all mesh views
//								if (
//										// !alreadySelectedXform
//										!_selectedViewableXformObservableList.contains(parentXform)										
//										) {
//									
//									// update Xform selection list
//									_selectedViewableXformObservableList.add(parentXform);
//									
//									for(Node p : parentXform.getChildren()) {
//										if (p instanceof MeshView) {
//											// color all MeshView objects accordingly
//											MeshView mv = ((MeshView) p);
//											if(mv.getId().indexOf(OCCFXViewableCAD.MESHVIEW_FRONT_ID) != -1)
//												mv.setMaterial(OCCFXViewableCAD.FACE_FRONT_SELECTED_MATERIAL);
//											if(mv.getId().indexOf(OCCFXViewableCAD.MESHVIEW_BACK_ID) != -1)
//												mv.setMaterial(OCCFXViewableCAD.FACE_BACK_SELECTED_MATERIAL);
//										}
//									}
//									
//								} else {
//									
//									_selectedViewableXformObservableList.remove(parentXform);
//									
//									for(Node p : parentXform.getChildren()) {
//										// System.out.println((i++) + ": " + p.getClass());
//										if (p instanceof MeshView) {
//											// color all MeshView objects accordingly
//											MeshView mv = ((MeshView) p);
//											if(mv.getId().indexOf(OCCFXViewableCAD.MESHVIEW_FRONT_ID) != -1)
//												mv.setMaterial(OCCFXViewableCAD.FACE_FRONT_MATERIAL);
//											if(mv.getId().indexOf(OCCFXViewableCAD.MESHVIEW_BACK_ID) != -1)
//												mv.setMaterial(OCCFXViewableCAD.FACE_BACK_MATERIAL);
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		});
//
//	}
//
//	public SubScene getSubScene() {
//		return _theSubScene;
//	}
//
//	public SubScene exchangeSubScene(final SceneAntialiasing sceneAA) {
//
//		// Clear current SubScene
//		((Group)_theSubScene.getRoot()).getChildren().clear();
//		_theSubScene.setCamera(null);
//		_theSubScene.setOnMouseDragged(null);
//		_theSubScene.setOnScroll(null);
//		_theSubScene.setOnMousePressed(null);
//
//		// Create and return a new SubScene
//		createSubScene(_theSubScene.getWidth(), _theSubScene.getHeight(), sceneAA);
//
//		return _theSubScene;
//	}
//
//	private void buildAxes(Group parentGroup) {
//		System.out.println("buildAxes()");
//		final PhongMaterial redMaterial = new PhongMaterial();
//		redMaterial.setDiffuseColor(Color.DARKRED);
//		redMaterial.setSpecularColor(Color.RED);
//
//		final PhongMaterial greenMaterial = new PhongMaterial();
//		greenMaterial.setDiffuseColor(Color.DARKGREEN);
//		greenMaterial.setSpecularColor(Color.GREEN);
//
//		final PhongMaterial blueMaterial = new PhongMaterial();
//		blueMaterial.setDiffuseColor(Color.DARKBLUE);
//		blueMaterial.setSpecularColor(Color.BLUE);
//
//		_axisXLength = 
//				//			Math.max(
//				//				parentGroup.getBoundsInLocal().getHeight(), 
//				//				parentGroup.getBoundsInLocal().getWidth()
//				//			)
//				//			*10.0
//				2.0
//				;
//		_axisYLength = 1.0*_axisXLength; 
//		_axisZLength = 1.0*_axisXLength;
//		_axisThickness = 
//				//			_axisXLength/200.0
//				0.02
//				;
//
//		final Box xAxis = new Box(_axisXLength, _axisThickness, _axisThickness);
//		final Box yAxis = new Box(_axisThickness, _axisYLength, _axisThickness);
//		final Box zAxis = new Box(_axisThickness, _axisThickness, _axisZLength);
//
//		xAxis.setMaterial(redMaterial);
//		yAxis.setMaterial(greenMaterial);
//		zAxis.setMaterial(blueMaterial);
//
//		_theAxisGroup = new Group();
//		_theAxisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
//		_theAxisGroup.setVisible(true);
//
//		StlMeshImporter stlImporter = new StlMeshImporter();
//		try {
//
//			System.out.println("Loading STL file, axis labels");
//
//			// X label
//
//			String filePathXStl = 
//					MyConfiguration.objects3dDirectory + "X.stl"; 
//
//			// need to import reversed triangles ???
//			//stlImporter.setOptions(StlImportOption.REVERSE_GEOMETRY);
//			stlImporter.read(filePathXStl);
//
//			TriangleMesh txtXMesh = stlImporter.getImport();
//			stlImporter.clear();
//			MeshView txtXMeshView = new MeshView(txtXMesh);
//			txtXMeshView.setMaterial(redMaterial);
//
//			Group axisXLabelGroup = new Group();
//			axisXLabelGroup.getChildren().add(txtXMeshView);
//
//			Rotate rotX1 = new Rotate(90, 0,0,0, Rotate.X_AXIS);      
//			Rotate rotX2 = new Rotate(90, 0,0,0, Rotate.Y_AXIS);
//			Transform transX = rotX1.createConcatenation(rotX2);
//			axisXLabelGroup.getTransforms().add(transX);
//			axisXLabelGroup.setTranslateX(_axisXLength*0.505);
//			axisXLabelGroup.scaleXProperty().set(1.0d);
//			axisXLabelGroup.scaleYProperty().set(1.0d);
//			axisXLabelGroup.scaleZProperty().set(1.0d);
//
//			_theAxisGroup.getChildren().add(axisXLabelGroup);
//
//			// Y label
//			String filePathYStl = 
//					MyConfiguration.objects3dDirectory + "Y.stl"; 
//
//			stlImporter.read(filePathYStl);
//
//			TriangleMesh txtYMesh = stlImporter.getImport();
//			stlImporter.clear();
//			MeshView txtYMeshView = new MeshView(txtYMesh);
//			txtYMeshView.setMaterial(greenMaterial);
//
//			Group axisYLabelGroup = new Group();
//			axisYLabelGroup.getChildren().add(txtYMeshView);
//
//			Rotate rotY1 = new Rotate(90, 0,0,0, Rotate.Y_AXIS);      
//			Rotate rotY2 = new Rotate(-90, 0,0,0, Rotate.Z_AXIS);
//			Transform transY = rotY1.createConcatenation(rotY2);
//			axisYLabelGroup.getTransforms().add(transY);
//			axisYLabelGroup.setTranslateY(_axisYLength*0.505);
//			axisYLabelGroup.scaleXProperty().set(1.0d);
//			axisYLabelGroup.scaleYProperty().set(1.0d);
//			axisYLabelGroup.scaleZProperty().set(1.0d);
//
//			_theAxisGroup.getChildren().add(axisYLabelGroup);
//
//			// Z label
//
//			String filePathZStl = 
//					MyConfiguration.objects3dDirectory + "Z.stl"; 
//
//			stlImporter.read(filePathZStl);
//
//			TriangleMesh txtZMesh = stlImporter.getImport();
//			stlImporter.clear();
//			MeshView txtZMeshView = new MeshView(txtZMesh);
//			txtZMeshView.setMaterial(blueMaterial);
//
//			Group axisZLabelGroup = new Group();
//			axisZLabelGroup.getChildren().add(txtZMeshView);
//
//			Rotate rotZ1 = new Rotate(90, 0,0,0, Rotate.X_AXIS);      
//			Rotate rotZ2 = new Rotate(270, 0,0,0, Rotate.Y_AXIS);
//			Transform transZ = rotZ1.createConcatenation(rotZ2);
//			axisZLabelGroup.getTransforms().add(transZ);
//			axisZLabelGroup.setTranslateZ(_axisYLength*0.535);
//			axisZLabelGroup.scaleXProperty().set(1.0d);
//			axisZLabelGroup.scaleYProperty().set(1.0d);
//			axisZLabelGroup.scaleZProperty().set(1.0d);
//
//			_theAxisGroup.getChildren().add(axisZLabelGroup);
//
//		} catch (ImportException e) { //  | MalformedURLException e
//			// handle exception
//			System.out.println("Unable to load axis label STL\n" + e);
//		}
//
//		///
//		parentGroup.getChildren().addAll(_theAxisGroup);
//	}
//
//	public Group getAxisGroup() {
//		return _theAxisGroup;
//	}
//
//	public void setVantagePoint(OCCFX3DView.VantagePoint3DView vp) {
//
//		Transform rotate = null;
//
//		final double distance = distToSceneCenter(_sceneDiameter/2);
//
//		switch(vp) {
//		case BOTTOM:
//			//			rotate = new Rotate(90, Rotate.X_AXIS);
//			rotate = new Rotate(-90, Rotate.Z_AXIS);
//			break;
//		case CORNER:
//			//			Rotate rotateX = new Rotate(-45, Rotate.X_AXIS);
//			//			Rotate rotateY = new Rotate(-45, new Point3D(0, 1, 1).normalize());
//			//			rotate = rotateX.createConcatenation(rotateY);
//			Rotate rotateCorner1 = new Rotate(-90, Rotate.X_AXIS);
//			Rotate rotateCorner2 = new Rotate(-30, new Point3D(-1,-1, 0).normalize());
//			rotate = rotateCorner1.createConcatenation(rotateCorner2);
//			break;
//		case FRONT:
//			//			rotate = new Rotate();
//			Rotate rotateFront1 = new Rotate( 90, Rotate.Y_AXIS);
//			Rotate rotateFront2 = new Rotate(-90, Rotate.Z_AXIS);
//			rotate = rotateFront1.createConcatenation(rotateFront2);
//			break;
//		case TOP:
//			//			rotate = new Rotate(-90, Rotate.X_AXIS);
//			Rotate rotateTop1 = new Rotate(-90, Rotate.Z_AXIS);
//			Rotate rotateTop2 = new Rotate(180, Rotate.X_AXIS);
//			rotate = rotateTop1.createConcatenation(rotateTop2);
//			break;
//		case BACK:
//			//			rotate = new Rotate();
//			Rotate rotateBack1 = new Rotate(-90, Rotate.Y_AXIS);
//			Rotate rotateBack2 = new Rotate( 90, Rotate.Z_AXIS);
//			rotate = rotateBack1.createConcatenation(rotateBack2);
//			break;
//		case RIGHT:
//			//			rotate = new Rotate();
//			Rotate rotateRight1 = new Rotate( 90, Rotate.X_AXIS);
//			Rotate rotateRight2 = new Rotate(180, Rotate.Z_AXIS);
//			rotate = rotateRight1.createConcatenation(rotateRight2);
//			break;
//		case LEFT:
//			//			rotate = new Rotate();
//			Rotate rotateLeft1 = new Rotate(-90, Rotate.X_AXIS);
//			Rotate rotateLeft2 = new Rotate(  0, Rotate.Z_AXIS);
//			rotate = rotateLeft1.createConcatenation(rotateLeft2);
//			break;
//		}
//
//		_lastSelectedVantagePoint = vp;
//
//		_theViewingAffineRotate.setToTransform(rotate);
//
//		_theViewingTranslate.setX(0);
//		_theViewingTranslate.setY(0);
//		_theViewingTranslate.setZ(-distance);
//	}
//
//	public void setDrawMode(DrawMode drawMode) {
//		for (MeshView mv : _theMeshViews) {
//			mv.setDrawMode(drawMode);
//		}
//	}
//
//	// TODO
//	public void setProjectionMode(String mode) {
//		if (mode.equalsIgnoreCase("Parallel")) {
//
//		}
//		else {
//
//		}
//	}
//
//	private MeshView createMeshView(Mesh mesh, Material material, MeshView[] meshViews, int index) {
//		final MeshView meshView = new MeshView(mesh);
//		meshView.setMaterial(material);
//		meshViews[index] = meshView;
//		return meshView;
//	} 
//
//	private PhongMaterial[] createMaterials(int dim) {
//
//		final PhongMaterial[] materials = new PhongMaterial[dim*dim];
//
//		int k = 0;
//		int direction = 1;
//		float hue = 0;
//
//		final float step = 1.0f / (dim*dim);
//
//		for (int i=0; i < dim; i++) {
//			for (int j=0; j < dim; j++) {
//
//				//System.out.println("hue = " + hue);
//				materials[k] = new PhongMaterial();
//				java.awt.Color hsbCol = new java.awt.Color(java.awt.Color.HSBtoRGB(hue, 0.85f, 0.7f));
//				materials[k].setDiffuseColor(Color.rgb(hsbCol.getRed(), hsbCol.getGreen(), hsbCol.getBlue()));
//				//materials[k].setDiffuseColor(Color.hsb(hue, 0.85, 0.7));
//				materials[k].setSpecularColor(Color.color(0.2, 0.2, 0.2));
//				materials[k].setSpecularPower(16f);
//
//				hue += step * direction;
//				k++;
//			}
//
//			direction *= (-1);
//			if (direction < 0) {
//				hue += step * (dim-1); 
//			}
//			else {
//				hue += step * (dim+1);
//			}
//
//			//            hue *= 360;
//		}        
//
//		return materials;
//	}
//
//	private double distToSceneCenter(double sceneRadius) {
//		// Extra space
//		final double borderFactor = 1.0;
//
//		final double fov = _thePerspectiveCamera.getFieldOfView();
//
//		final double c3dWidth = _theSubScene.getWidth();
//		final double c3dHeight = _theSubScene.getHeight();
//		// Consider ratio of canvas' width and height
//		double ratioFactor = 1.0;
//		if (c3dWidth > c3dHeight) {
//			ratioFactor = c3dWidth/c3dHeight;
//		}
//		//System.out.println("sceneRadius       = " + sceneRadius);
//
//		final double distToSceneCenter = borderFactor * ratioFactor * sceneRadius / Math.tan(Math.toRadians(fov/2));
//		//System.out.println("distToSceneCenter = " + distToSceneCenter);
//		return distToSceneCenter;        
//	}
//
//	public PerspectiveCamera getPerspectiveCamera() {
//		return _thePerspectiveCamera;
//	}
//
//	public OCCFXForm getTestCurvesXform() {
//		return testCurvesXform;
//	}
//
//	public List<BezierMesh> getBeziers() {
//		return beziers;
//	}
//
//	public CubeViewer getCubeViewer() {
//		return cubeViewer;
//	}
//
//	public Affine getViewingAffineRotate() {
//		return _theViewingAffineRotate;
//	}
//
//	public Transform getCurrentRotate() {
//		return _theCurrentRotate;
//	}
//
//	public void setCurrentRotate(Transform _theCurrentRotate) {
//		this._theCurrentRotate = _theCurrentRotate;
//	}
//
//	// TODO
//	private void populateXFormCurve(
//			OCCFXForm curveXform,
//			InterpolateBezier interpolate,
//			boolean showKnots,
//			boolean showControlPoints,
//			double pointSize
//			) {
//
//		//		System.out.println(
//		//				"=========================================================\n" +
//		//				"... populateXformCurve \n" +
//		//				"========================================================="
//		//		);
//		//		interpolate.getSplines().forEach(
//		//			spline -> {
//		//				System.out.println(
//		//					spline.getPoints()
//		//				);
//		//			}
//		//		);
//
//		double multiplier = 0.4d;
//		if(showKnots || showControlPoints){
//			interpolate.getSplines().forEach(spline->{ // <===================== LAMBDA
//				org.fxyz3d.geometry.Point3D k0=spline.getPoints().get(0);
//				org.fxyz3d.geometry.Point3D k1=spline.getPoints().get(1);
//				org.fxyz3d.geometry.Point3D k2=spline.getPoints().get(2);
//				org.fxyz3d.geometry.Point3D k3=spline.getPoints().get(3);
//				if(showKnots){
//					Sphere s=new Sphere(pointSize);
//					s.getTransforms().add(new Translate(k0.x, k0.y, k0.z));
//					s.setMaterial(new PhongMaterial(Color.GREENYELLOW));
//					curveXform.getChildren().add(s);
//					s=new Sphere(pointSize);
//					s.getTransforms().add(new Translate(k3.x, k3.y, k3.z));
//					s.setMaterial(new PhongMaterial(Color.GREENYELLOW));
//					curveXform.getChildren().add(s);
//				}
//				if(showControlPoints){
//					org.fxyz3d.geometry.Point3D dir=k1.substract(k0).crossProduct(new org.fxyz3d.geometry.Point3D(0,-1,0));
//					double angle=Math.acos(k1.substract(k0).normalize().dotProduct(new org.fxyz3d.geometry.Point3D(0,-1,0)));
//					double h1=k1.substract(k0).magnitude();
//					Cylinder c=new Cylinder(pointSize*multiplier,h1);
//					c.getTransforms().addAll(new Translate(k0.x, k0.y-h1/2d, k0.z),
//							new Rotate(-Math.toDegrees(angle), 0d,h1/2d,0d,
//									new javafx.geometry.Point3D(dir.x,-dir.y,dir.z)));
//					c.setMaterial(new PhongMaterial(Color.GREEN));
//					curveXform.getChildren().add(c);
//
//					dir=k2.substract(k1).crossProduct(new org.fxyz3d.geometry.Point3D(0,-1,0));
//					angle=Math.acos(k2.substract(k1).normalize().dotProduct(new org.fxyz3d.geometry.Point3D(0,-1,0)));
//					h1=k2.substract(k1).magnitude();
//					c=new Cylinder(pointSize*multiplier,h1);
//					c.getTransforms().addAll(new Translate(k1.x, k1.y-h1/2d, k1.z),
//							new Rotate(-Math.toDegrees(angle), 0d,h1/2d,0d,
//									new javafx.geometry.Point3D(dir.x,-dir.y,dir.z)));
//					c.setMaterial(new PhongMaterial(Color.GREEN));
//					curveXform.getChildren().add(c);
//
//					dir=k3.substract(k2).crossProduct(new org.fxyz3d.geometry.Point3D(0,-1,0));
//					angle=Math.acos(k3.substract(k2).normalize().dotProduct(new org.fxyz3d.geometry.Point3D(0,-1,0)));
//					h1=k3.substract(k2).magnitude();
//					c=new Cylinder(pointSize*multiplier,h1);
//					c.getTransforms().addAll(new Translate(k2.x, k2.y-h1/2d, k2.z),
//							new Rotate(-Math.toDegrees(angle), 0d,h1/2d,0d,
//									new javafx.geometry.Point3D(dir.x,-dir.y,dir.z)));
//					c.setMaterial(new PhongMaterial(Color.GREEN));
//					curveXform.getChildren().add(c);
//
//					Sphere s=new Sphere(pointSize);
//					s.getTransforms().add(new Translate(k1.x, k1.y, k1.z));
//					s.setMaterial(new PhongMaterial(Color.RED));
//					curveXform.getChildren().add(s);
//					s=new Sphere(pointSize);
//					s.getTransforms().add(new Translate(k2.x, k2.y, k2.z));
//					s.setMaterial(new PhongMaterial(Color.RED));
//					curveXform.getChildren().add(s);
//				}
//			});
//		}// end-of-if showKnots || showControlPoints
//		//		long time=System.currentTimeMillis();
//		List<BezierMesh> beziersInSection = new ArrayList<BezierMesh>();
//		interpolate.getSplines().stream().forEach(spline->{ // <===================== LAMBDA
//			BezierMesh bezier = new BezierMesh(
//					spline, // BezierHelper spline
//					pointSize*multiplier, // double wireRadius
//					pointsBetweenTwoConsecutiveKnots, // int rDivs (n. points between two consecutive knots)
//					pointsPerCurveSection, // int tDivs (n. points per curve section)
//					0, // int lengthCrop
//					0 // int wireCrop
//					);
//			
//			// bezier.setDrawMode(DrawMode.LINE);
//			bezier.setCullFace(CullFace.NONE);
//			//          bezier.setSectionType(SectionType.TRIANGLE);
//			// NONE
//			bezier.setTextureModeNone(
//					Color.CRIMSON
//					// Color.hsb(360d*sp.getAndIncrement()/interpolate.getSplines().size(), 1, 1)
//					);
//			// IMAGE
//			//          bezier.setTextureModeImage(getClass().getResource("res/LaminateSteel.jpg").toExternalForm());
//			// PATTERN
//			// bezier.setTextureModePattern(3d);
//			// FUNCTION
//			// bezier.setTextureModeVertices1D(256*256,t->spline.getKappa(t)); // t -> Math.cos( 2.0 * Math.PI * spline.getKappa(t) )
//			// DENSITY
//			//          bezier.setTextureModeVertices3D(256*256,dens);
//			// FACES
//			// bezier.setTextureModeFaces(256*256);
//
//			Rotate rotateY = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
//			bezier.getTransforms().addAll(new Rotate(0,Rotate.X_AXIS),rotateY);
//			beziersInSection.add(bezier);
//		});
//		//		System.out.println("time: "+(System.currentTimeMillis()-time)); //43.815->25.606->15
//
//		// add bezier to group member variable
//		curveXform.getChildren().addAll(beziersInSection);
//
//	}
//
//	// http://stackoverflow.com/questions/12324799/javafx-2-0-fxml-strange-lookup-behaviour
//	private <T> T lookup(Node parent, String id, Class<T> clazz) {
//		for (Node node : parent.lookupAll(id)) {
//			if (node.getClass().isAssignableFrom(clazz)) {
//				return (T)node;
//			}
//		}
//		throw new IllegalArgumentException("Parent " + parent + " doesn't contain node with id " + id);
//	}
//
//	public Group get_subSceneRoot() {
//		return _subSceneRoot;
//	}
//
//	public int getPointsBetweenTwoConsecutiveKnots() {
//		return pointsBetweenTwoConsecutiveKnots;
//	}
//
//	public void setPointsBetweenTwoConsecutiveKnots(
//			int pointsBetweenTwoConsecutiveKnots) {
//		this.pointsBetweenTwoConsecutiveKnots = pointsBetweenTwoConsecutiveKnots;
//	}
//
//	public int getPointsPerCurveSection() {
//		return pointsPerCurveSection;
//	}
//
//	public void setPointsPerCurveSection(int pointsPerCurveSection) {
//		this.pointsPerCurveSection = pointsPerCurveSection;
//	}
//
//	public double getSceneDiameter() {
//		return _sceneDiameter;
//	}
//
//	public void setSceneDiameter(double sceneDiameter) {
//		this._sceneDiameter = sceneDiameter;
//	}
//
//	public BooleanProperty getSelectionModeOnProperty() {
//		return _selectionModeOnProperty; // _selectionModeOn;
//	}
//
//	public boolean isSelectionModeOn() {
//		return _selectionModeOnProperty.get(); // _selectionModeOn;
//	}
//
//	public void setSelectionMode(boolean mode) {
//		this._selectionModeOnProperty.setValue(mode); // _selectionModeOn = mode;
//	}
//	
//	public void toggleSelectionMode() {
//		this._selectionModeOnProperty.setValue(!this._selectionModeOnProperty.getValue()); // _selectionModeOn = !this._selectionModeOn;
//	}
//	
//	
//}// end of class
