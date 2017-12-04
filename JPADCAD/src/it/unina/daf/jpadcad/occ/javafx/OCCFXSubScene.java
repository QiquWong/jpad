package it.unina.daf.jpadcad.occ.javafx;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.fxyz3d.scene.CubeViewer;
import org.fxyz3d.shapes.primitives.BezierMesh;
import org.fxyz3d.shapes.primitives.helper.InterpolateBezier;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import com.interactivemesh.jfx.importer.stl.StlImportOption;
import com.interactivemesh.jfx.importer.stl.StlMeshImporter;

import configuration.MyConfiguration;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

final public class OCCFXSubScene {

	private double axisXLength = 1.0;
	private double axisYLength = 1.0;
	private double axisZLength = 1.0;
	private double axisThickness = 0.5;
	
    enum VP {
        Select("Select"),
        BOTTOM("Bottom"), CORNER("Corner"), FRONT("Front"), TOP("Top");
        VP(String listName) {
            this.listName = listName;
        }
        private String listName;
        String getListName() {
            return listName;
        }
    }

    private SubScene subScene = null;
    
    private Group viewingGroup = null; 
    private Affine viewingRotate = new Affine();  
    private Translate viewingTranslate = new Translate();
    private double startX = 0;
    private double startY = 0;
    
    private PerspectiveCamera perspectiveCamera = null;  
    
	private AmbientLight ambLight = null;
    private PointLight pointLight = null;
    
    private Group axisGroup = null;
    
    private Group tuxCubeRotGroup = null;

	private Group tuxCubeCenterGroup = null;
    
    private Rotate tuxCubeRotate = null;
    private Timeline tuxCubeRotTimeline = null;
    
    private RotateTransition[] tuxRotTransAll = null;    
       
    private BoundingBox tuxCubeBinL = null;
    private double sceneDiameter =  0;
        
    private MeshView[] meshViews = null;
    
    private PhongMaterial eyesMat = null;
    private PhongMaterial feetMat = null;
    private PhongMaterial mouthMat = null;
    private PhongMaterial pupilsMat = null;
    
    private TriangleMesh bodyMesh = null;
    private TriangleMesh frontMesh = null;
    private TriangleMesh eyesMesh = null;
    private TriangleMesh feetMesh = null;
    private TriangleMesh mouthMesh = null;
    private TriangleMesh pupilsMesh = null;
    
    private TriangleMesh fuselageMesh = null;
    private MeshView fuselageMeshView = null;
    
	private OCCFXForm testCurvesXForm = new OCCFXForm();
	private List<BezierMesh> beziers = new ArrayList<BezierMesh>();
	
	private OCCFXForm xForm = new OCCFXForm();
	
	private CubeViewer cubeViewer;
    
	public OCCFXSubScene() {

		buildTestCurves();
		createBaseScene();
		createSubScene(800, 800, SceneAntialiasing.BALANCED);
	}

    private void buildTestCurves() {

		// see https://github.com/Birdasaur/FXyz
		
		Rotate rotateY = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
    	
    	// the control points
    	List<org.fxyz3d.geometry.Point3D> knots = Arrays.asList(
    			new org.fxyz3d.geometry.Point3D(0f,0f,0f),
    			new org.fxyz3d.geometry.Point3D(0,5,5),
    			new org.fxyz3d.geometry.Point3D(0,10,-10),
    			new org.fxyz3d.geometry.Point3D(10,15,-7),
    			new org.fxyz3d.geometry.Point3D(15,5,-4),
    			new org.fxyz3d.geometry.Point3D(0,-1,0),
    			new org.fxyz3d.geometry.Point3D(0,-3,0)
    			);

    	boolean showControlPoints=true;
    	boolean showKnots=true;

    	// the interpolating bezier
    	InterpolateBezier interpolate = new InterpolateBezier(knots);

    	AtomicInteger sp=new AtomicInteger();
    	if(showKnots || showControlPoints){
    		interpolate.getSplines().forEach(spline->{ // <===================== LAMBDA
    			org.fxyz3d.geometry.Point3D k0=spline.getPoints().get(0);
    			org.fxyz3d.geometry.Point3D k1=spline.getPoints().get(1);
    			org.fxyz3d.geometry.Point3D k2=spline.getPoints().get(2);
    			org.fxyz3d.geometry.Point3D k3=spline.getPoints().get(3);
    			if(showKnots){
    				Sphere s=new Sphere(0.2d);
    				s.getTransforms().add(new Translate(k0.x, k0.y, k0.z));
    				s.setMaterial(new PhongMaterial(Color.GREENYELLOW));
    				testCurvesXForm.getChildren().add(s);
    				s=new Sphere(0.4d);
    				s.getTransforms().add(new Translate(k3.x, k3.y, k3.z));
    				s.setMaterial(new PhongMaterial(Color.GREENYELLOW));
    				testCurvesXForm.getChildren().add(s);
    			}
    			if(showControlPoints){
    				org.fxyz3d.geometry.Point3D dir=k1.substract(k0).crossProduct(new org.fxyz3d.geometry.Point3D(0,-1,0));
    				double angle=Math.acos(k1.substract(k0).normalize().dotProduct(new org.fxyz3d.geometry.Point3D(0,-1,0)));
    				double h1=k1.substract(k0).magnitude();
    				Cylinder c=new Cylinder(0.03d,h1);
    				c.getTransforms().addAll(new Translate(k0.x, k0.y-h1/2d, k0.z),
    						new Rotate(-Math.toDegrees(angle), 0d,h1/2d,0d,
    								new javafx.geometry.Point3D(dir.x,-dir.y,dir.z)));
    				c.setMaterial(new PhongMaterial(Color.GREEN));
    				testCurvesXForm.getChildren().add(c);

    				dir=k2.substract(k1).crossProduct(new org.fxyz3d.geometry.Point3D(0,-1,0));
    				angle=Math.acos(k2.substract(k1).normalize().dotProduct(new org.fxyz3d.geometry.Point3D(0,-1,0)));
    				h1=k2.substract(k1).magnitude();
    				c=new Cylinder(0.03d,h1);
    				c.getTransforms().addAll(new Translate(k1.x, k1.y-h1/2d, k1.z),
    						new Rotate(-Math.toDegrees(angle), 0d,h1/2d,0d,
    								new javafx.geometry.Point3D(dir.x,-dir.y,dir.z)));
    				c.setMaterial(new PhongMaterial(Color.GREEN));
    				testCurvesXForm.getChildren().add(c);

    				dir=k3.substract(k2).crossProduct(new org.fxyz3d.geometry.Point3D(0,-1,0));
    				angle=Math.acos(k3.substract(k2).normalize().dotProduct(new org.fxyz3d.geometry.Point3D(0,-1,0)));
    				h1=k3.substract(k2).magnitude();
    				c=new Cylinder(0.03d,h1);
    				c.getTransforms().addAll(new Translate(k2.x, k2.y-h1/2d, k2.z),
    						new Rotate(-Math.toDegrees(angle), 0d,h1/2d,0d,
    								new javafx.geometry.Point3D(dir.x,-dir.y,dir.z)));
    				c.setMaterial(new PhongMaterial(Color.GREEN));
    				testCurvesXForm.getChildren().add(c);

    				Sphere s=new Sphere(0.1d);
    				s.getTransforms().add(new Translate(k1.x, k1.y, k1.z));
    				s.setMaterial(new PhongMaterial(Color.RED));
    				testCurvesXForm.getChildren().add(s);
    				s=new Sphere(0.1d);
    				s.getTransforms().add(new Translate(k2.x, k2.y, k2.z));
    				s.setMaterial(new PhongMaterial(Color.RED));
    				testCurvesXForm.getChildren().add(s);
    			}
    		});
    	}
    	long time=System.currentTimeMillis();
    	interpolate.getSplines().stream().forEach(spline->{ // <===================== LAMBDA
    		BezierMesh bezier = new BezierMesh(
    				spline, // BezierHelper spline
    				0.1d, // double wireRadius
    				30, // int rDivs (n. points between two consecutive knots)
    				10, // int tDivs
    				0, // int lengthCrop
    				0 // int wireCrop
    		);
    		// bezier.setDrawMode(DrawMode.LINE);
    		bezier.setCullFace(CullFace.NONE);
    		//          bezier.setSectionType(SectionType.TRIANGLE);
    		// NONE
    		bezier.setTextureModeNone(
    				Color.CRIMSON
    				// Color.hsb(360d*sp.getAndIncrement()/interpolate.getSplines().size(), 1, 1)
    				);
    		// IMAGE
    		//          bezier.setTextureModeImage(getClass().getResource("res/LaminateSteel.jpg").toExternalForm());
    		// PATTERN
    		// bezier.setTextureModePattern(3d);
    		// FUNCTION
    		// bezier.setTextureModeVertices1D(256*256,t->spline.getKappa(t)); // t -> Math.cos( 2.0 * Math.PI * spline.getKappa(t) )
    		// DENSITY
    		//          bezier.setTextureModeVertices3D(256*256,dens);
    		// FACES
    		// bezier.setTextureModeFaces(256*256);

    		bezier.getTransforms().addAll(new Rotate(0,Rotate.X_AXIS),rotateY);
    		beziers.add(bezier);
    	});
    	System.out.println("time: "+(System.currentTimeMillis()-time)); //43.815->25.606->15

    	// add bezier to group member variable
    	testCurvesXForm.setId("testCurvesXform");
    	testCurvesXForm.getChildren().addAll(beziers);
    	
    }
    
    private void createBaseScene() {

        //
        // Viewing : Camera & Light
        //
        
        // SubScene's camera
        perspectiveCamera = new PerspectiveCamera(true);
        perspectiveCamera.setVerticalFieldOfView(false);
        perspectiveCamera.setFarClip(250);
        perspectiveCamera.setNearClip(0.1);
        perspectiveCamera.setFieldOfView(44);
       
        // SubScene's lights
        pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateZ(-20000);
        
        ambLight = new AmbientLight(Color.color(0.3, 0.3, 0.3));
          
        // Viewing group: camera and headlight
        viewingGroup = new Group(perspectiveCamera, pointLight);
        viewingGroup.getTransforms().setAll(viewingRotate, viewingTranslate);
        
        //
        // Group hierarchy of the cube
        //
        
        // Centers the entire cube at (0,0,0)
        tuxCubeCenterGroup = new Group();

        // Cube rotation target
        tuxCubeRotGroup = new Group(tuxCubeCenterGroup);
        
        tuxCubeRotate = new Rotate(0, 0,0,0, Rotate.Y_AXIS);
        
        final KeyValue kv0 = new KeyValue(tuxCubeRotate.angleProperty(), 0, Interpolator.LINEAR);
        final KeyValue kv1 = new KeyValue(tuxCubeRotate.angleProperty(), 360, Interpolator.LINEAR);
        final KeyFrame kf0 = new KeyFrame(Duration.millis(0), kv0);
        final KeyFrame kf1 = new KeyFrame(Duration.millis(50000), kv1); // min speed, max duration

        tuxCubeRotTimeline = new Timeline();
        tuxCubeRotTimeline.setCycleCount(Timeline.INDEFINITE);        
        tuxCubeRotTimeline.getKeyFrames().setAll(kf0, kf1);
        
        tuxCubeRotGroup.getTransforms().setAll(tuxCubeRotate);        
    }
        
    private void createSubScene(final double width, final double height, final SceneAntialiasing sceneAA) {
            
        //
        // SubScene & Root 
        //
        final Group subSceneRoot  = new Group();

        subScene = new SubScene(subSceneRoot, width, height, true, sceneAA);
                       
        // otherwise subScene doesn't receive mouse events
        // TODO bug ??   
        subScene.setFill(Color.TRANSPARENT);
        
        // Perspective camera
        subScene.setCamera(perspectiveCamera);
        
        // Add all to SubScene
        subSceneRoot.getChildren().addAll(tuxCubeRotGroup, viewingGroup, ambLight);
        
        // Add Cube
        if (cubeViewer != null) {
        	subSceneRoot.getChildren().add(cubeViewer);
        	//subSceneRoot.getChildren().add(cubeWorld);
        }

        // Add curves
        subSceneRoot.getChildren().add(testCurvesXForm);

        // Add wing mesh
        subSceneRoot.getChildren().add(xForm);
        
        // add axes
        buildAxes(subSceneRoot);
        
        // Navigator on SubScene
        
        final Rotate viewingRotX = new Rotate(0, 0,0,0, Rotate.X_AXIS);      
        final Rotate viewingRotY = new Rotate(0, 0,0,0, Rotate.Y_AXIS);        
        
        subScene.setOnMouseDragged(new EventHandler<MouseEvent>() {
        	@Override public void handle(MouseEvent event) {
        		// System.out.println("OnMouseDragged " + event);
    			// System.out.println("isPrimaryButtonDown? " + event.isPrimaryButtonDown());
    			// System.out.println("viewingRotX angle 1) " + viewingRotX.getAngle());
    			// System.out.println("event.getButton() " + event.getButton());
        		
        		double multiplier = 1.0;
        		if (event.isShiftDown()) {
        			multiplier = 10.0;
        		}
        		if (
        				// event.getButton() == MouseButton.PRIMARY
        				event.isPrimaryButtonDown()
        				) {
        			viewingRotX.setAngle(multiplier*(startY - event.getSceneY())/10); 
        			// System.out.println("viewingRotX angle 2) " + viewingRotX.getAngle());
        			viewingRotY.setAngle(multiplier*(event.getSceneX() - startX)/10);                                        
        			viewingRotate.append(viewingRotX.createConcatenation(viewingRotY));
        			
//        			System.out.println("###################################### MyFXSubScene :: setOnMouseDragged : PrimaryButtonDown");
        			// Borrow code from  FXyz/src/org/fxyz/cameras/CameraTransformer.java
        			// https://github.com/Birdasaur/FXyz/ --> /src/org/fxyz/cameras/CameraTransformer.java
//        			cubeViewer.adjustPanelsByPos(
//        					perspectiveCamera.rx.getAngle(), 
//        					perspectiveCamera.ry.getAngle(), 
//        					perspectiveCamera.rz.getAngle()
//        					);

        			
        		}
        		else if (
        				// event.getButton() == MouseButton.SECONDARY
        				event.isSecondaryButtonDown()
        				) {
        			viewingTranslate.setX(viewingTranslate.getX() + multiplier*(startX - event.getSceneX())/100);
        			viewingTranslate.setY(viewingTranslate.getY() + multiplier*(startY - event.getSceneY())/100);
        		}
        		else if (
        				// event.getButton() == MouseButton.MIDDLE
        				event.isMiddleButtonDown()
        				) {
        			viewingTranslate.setZ(viewingTranslate.getZ() + multiplier*(event.getSceneY() - startY)/40);
        		}

        		startX = event.getSceneX();
        		startY = event.getSceneY();
        	}
        });
        subScene.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override public void handle(ScrollEvent event) {
            	// System.out.println("OnScroll event.getDeltaY() = " + event.getDeltaY());
                viewingTranslate.setZ(viewingTranslate.getZ() - event.getDeltaY()/40);
            }
        });
        subScene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                startX = event.getSceneX();
                startY = event.getSceneY();
                // System.out.println("OnMousePressed = " + event);
                // System.out.println("event.getButton() " + event.getButton());
            }
        });
        
        // test picking
        subScene.setOnMouseClicked((event)->{
        	PickResult res = event.getPickResult();
        	System.out.println("PickResult:: " + res);
        	if (res.getIntersectedNode() instanceof BezierMesh){
        		((BezierMesh)res.getIntersectedNode()).setMaterial(
        				new PhongMaterial(event.isShiftDown() ? Color.CHARTREUSE : Color.DARKORANGE));
        	}
        	if (res.getIntersectedNode() instanceof MeshView){
        		System.out.println("Id: " + ((MeshView) res.getIntersectedNode()).getId() );
        		if (((MeshView) res.getIntersectedNode()).getId() != null) {
        			if (((MeshView) res.getIntersectedNode()).getId().equals("Wing-Face")) {
        				((MeshView) res.getIntersectedNode()).setMaterial(
        						new PhongMaterial(event.isShiftDown() ? Color.GREEN : Color.CHARTREUSE));
        			}
        		}
        	}
        });

    }
   
    public SubScene getSubScene() {
        return subScene;
    }
   
    SubScene exchangeSubScene(final SceneAntialiasing sceneAA) {
        
        // Clear current SubScene
        ((Group)subScene.getRoot()).getChildren().clear();
        subScene.setCamera(null);
        subScene.setOnMouseDragged(null);
        subScene.setOnScroll(null);
        subScene.setOnMousePressed(null);
        
        // Create and return a new SubScene
        createSubScene(subScene.getWidth(), subScene.getHeight(), sceneAA);
        
        return subScene;
    }

	private void buildAxes(Group parentGroup) {
		System.out.println("buildAxes()");
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(Color.DARKRED);
		redMaterial.setSpecularColor(Color.RED);

		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(Color.DARKGREEN);
		greenMaterial.setSpecularColor(Color.GREEN);

		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(Color.DARKBLUE);
		blueMaterial.setSpecularColor(Color.BLUE);

		axisXLength = 
//			Math.max(
//				parentGroup.getBoundsInLocal().getHeight(), 
//				parentGroup.getBoundsInLocal().getWidth()
//			)
//			*10.0
			75.0
			;
		axisYLength = 0.3*axisXLength; 
		axisZLength = 0.3*axisXLength;
		axisThickness = 
//			axisXLength/200.0
			0.06
			;
		
		final Box xAxis = new Box(axisXLength, axisThickness, axisThickness);
		final Box yAxis = new Box(axisThickness, axisYLength, axisThickness);
		final Box zAxis = new Box(axisThickness, axisThickness, axisZLength);

		xAxis.setMaterial(redMaterial);
		yAxis.setMaterial(greenMaterial);
		zAxis.setMaterial(blueMaterial);

		axisGroup = new Group();
		axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
		axisGroup.setVisible(true);

		StlMeshImporter stlImporter = new StlMeshImporter();
		try {

			System.out.println("Loading STL file, axis labels");

			// X label

			String filePathXStl = 
					MyConfiguration.objects3dDirectory + "X.stl"; 

			// need to import reversed triangles ???
			//stlImporter.setOptions(StlImportOption.REVERSE_GEOMETRY);
			stlImporter.read(filePathXStl);

			TriangleMesh txtXMesh = stlImporter.getImport();
			stlImporter.clear();
			MeshView txtXMeshView = new MeshView(txtXMesh);
			txtXMeshView.setMaterial(redMaterial);
			
			Group axisXLabelGroup = new Group();
			axisXLabelGroup.getChildren().add(txtXMeshView);
			
			Rotate rotX1 = new Rotate(90, 0,0,0, Rotate.X_AXIS);      
			Rotate rotX2 = new Rotate(90, 0,0,0, Rotate.Y_AXIS);
			Transform transX = rotX1.createConcatenation(rotX2);
			axisXLabelGroup.getTransforms().add(transX);
			axisXLabelGroup.setTranslateX(axisXLength*0.505);
			axisXLabelGroup.scaleXProperty().set(3.0d);
			axisXLabelGroup.scaleYProperty().set(3.0d);
			axisXLabelGroup.scaleZProperty().set(3.0d);

			axisGroup.getChildren().add(axisXLabelGroup);

			// Y label
			String filePathYStl = 
					MyConfiguration.objects3dDirectory + "Y.stl"; 

			stlImporter.read(filePathYStl);

			TriangleMesh txtYMesh = stlImporter.getImport();
			stlImporter.clear();
			MeshView txtYMeshView = new MeshView(txtYMesh);
			txtYMeshView.setMaterial(greenMaterial);
			
			Group axisYLabelGroup = new Group();
			axisYLabelGroup.getChildren().add(txtYMeshView);
			
			Rotate rotY1 = new Rotate(90, 0,0,0, Rotate.Y_AXIS);      
			Rotate rotY2 = new Rotate(-90, 0,0,0, Rotate.Z_AXIS);
			Transform transY = rotY1.createConcatenation(rotY2);
			axisYLabelGroup.getTransforms().add(transY);
			axisYLabelGroup.setTranslateY(axisYLength*0.505);
			axisYLabelGroup.scaleXProperty().set(3.0d);
			axisYLabelGroup.scaleYProperty().set(3.0d);
			axisYLabelGroup.scaleZProperty().set(3.0d);

			axisGroup.getChildren().add(axisYLabelGroup);

			// Z label

			String filePathZStl = 
					MyConfiguration.objects3dDirectory + "Z.stl"; 

			stlImporter.read(filePathZStl);

			TriangleMesh txtZMesh = stlImporter.getImport();
			stlImporter.clear();
			MeshView txtZMeshView = new MeshView(txtZMesh);
			txtZMeshView.setMaterial(blueMaterial);
			
			Group axisZLabelGroup = new Group();
			axisZLabelGroup.getChildren().add(txtZMeshView);
			
			Rotate rotZ1 = new Rotate(90, 0,0,0, Rotate.X_AXIS);      
			Rotate rotZ2 = new Rotate(270, 0,0,0, Rotate.Y_AXIS);
			Transform transZ = rotZ1.createConcatenation(rotZ2);
			axisZLabelGroup.getTransforms().add(transZ);
			axisZLabelGroup.setTranslateZ(axisYLength*0.535);
			axisZLabelGroup.scaleXProperty().set(3.0d);
			axisZLabelGroup.scaleYProperty().set(3.0d);
			axisZLabelGroup.scaleZProperty().set(3.0d);

			axisGroup.getChildren().add(axisZLabelGroup);
			
		} catch (ImportException e) { //  | MalformedURLException e
			// handle exception
			System.out.println("Unable to load axis label STL\n" + e);
		}

		///
		parentGroup.getChildren().addAll(axisGroup);
	}
    
    public Group getAxisGroup() {
		return axisGroup;
	}

	// 
    void createTuxCubeOfDim(int dim, boolean play, DrawMode drawMode) {
        
        // Tux : 13.744 triangles, 6 MeshViews
        
        // TuxBody :   3.856
        // TuxEyes :   1.056
        // TuxFeet :   4.640
        // TuxFront :    192
        // TuxMouth :  2.944
        // TuxPupils : 1.056
        
        //          #Tux             triangles  MeshViews
        // dim  1 :    1 * 13.744 =     13.744          6
        // dim  2 :    8 * 13.744 =    109.952         48
        // dim  3 :   27 * 13.744 =    371.088        162
        // dim  4 :   64 * 13.744 =    879.616        384
        // dim  5 :  125 * 13.744 =  1.718.000        750
        // dim  6 :  216 * 13.744 =  2.968.704      1.296
        // dim  7 :  343 * 13.744 =  4.714.192      2.058
        // dim  8 :  512 * 13.744 =  7.036.928      3.072
        // dim  9 :  729 * 13.744 = 10.019.376      4.374
        // dim 10 : 1000 * 13.744 = 13.744.000      6.000
        // dim 11 : 1331 * 13.744 = 18.293.264      7.986
        // dim 12 : 1728 * 13.744 = 23.749.632     10.368

        // Clear cube
        tuxCubeCenterGroup.getChildren().clear();
                            
        // Center Tux for rotation
        final double transZ = -0.01396;
        
        final int xDist = 2;
        final int yDist = 2;
        final int zDist = 2;
        
        long delay = 4;
        final long delayIncr = 4;
        
        Group tuxCenterGroup = null;    // center Tux at (0,y,0)
        Group tuxRotationGroup = null;  // target for rotate transition
        Group tuxPositionGroup = null;  // position Tux within the cube
        
        // Appearances for body and front
        final PhongMaterial[] materials = createMaterials(dim);
        
        final int numTux = dim*dim*dim;
        final double maxTux = 10*10*10;
                
        int n = 0;
        int nx6 = 0;
        int t = materials.length-1;
        
        float xTrans = 0;
        float yTrans = 0;
        float zTrans = 0;
        
        tuxRotTransAll = new RotateTransition[numTux];
        meshViews = new MeshView[numTux * 6];
        
        final ObservableList<Node> children = tuxCubeCenterGroup.getChildren();
        
        for (int i=0; i < dim; i++) {               // z axis
            for (int j=0; j < dim; j++) {           // y axis                            
                for (int k=0; k < dim; k++) {       // x axis
                    
                    nx6 = n * 6;
                    
                    // group hierarchy from bottom to top
                    // 1.
                    tuxCenterGroup = new Group();
                    tuxCenterGroup.setTranslateZ(transZ);
                    
                    tuxCenterGroup.getChildren().addAll(
                        createMeshView(eyesMesh, eyesMat, meshViews, nx6),
                        createMeshView(feetMesh, feetMat, meshViews, nx6+1),
                        createMeshView(mouthMesh, mouthMat, meshViews, nx6+2),
                        createMeshView(pupilsMesh, pupilsMat, meshViews, nx6+3),
                        createMeshView(bodyMesh, materials[t], meshViews, nx6+4),
                        createMeshView(frontMesh, materials[t--], meshViews, nx6+5));
                    // 2.
                    tuxRotationGroup = new Group(tuxCenterGroup);    
                                      
//System.out.println("tuxCenterGroup 0 = " + tuxCenterGroup.getBoundsInLocal());
// Tux BoundsInLocal
// minX:-0.81812, minY:-1.45735, minZ:-0.28825 
// maxX: 0.81812, maxY:-0.01011, maxZ: 0.31617                    
//System.out.println("tuxCenterGroup 1 =  " + tuxCenterGroup.getBoundsInParent());
// Tux BoundsInParent
// minX:-0.81812, minY:-1.45735, minZ:-0.30221
// maxX: 0.81812, maxY:-0.01011, maxZ: 0.30221
                             
                    RotateTransition rotateTransition = new RotateTransition();
                    rotateTransition.setNode(tuxRotationGroup);
                    rotateTransition.setAxis(Rotate.Y_AXIS);
                    rotateTransition.setDelay(Duration.millis(delay));
                    rotateTransition.setDuration(Duration.millis(5000 - 2*n*(maxTux/numTux)));
                    rotateTransition.setCycleCount(Timeline.INDEFINITE);
                    rotateTransition.setAutoReverse(false); 
                    rotateTransition.setInterpolator(Interpolator.LINEAR);
                    rotateTransition.setByAngle(360);
                    tuxRotTransAll[n] = rotateTransition;
        
                    // 3.
                    tuxPositionGroup = new Group(tuxRotationGroup);
                    tuxPositionGroup.setTranslateX(xTrans);
                    tuxPositionGroup.setTranslateY(yTrans);
                    tuxPositionGroup.setTranslateZ(zTrans);
                    children.add(tuxPositionGroup);
                    
                    xTrans += xDist;
                    delay = delayIncr * n;                    
                    n++;
                }                                   // x axis
                
                xTrans = 0;
                yTrans += yDist;
            }                                       // y axis
            
            yTrans = 0;            
            zTrans += zDist;
            
            t = materials.length-1;
        }                                           // z axis
        
        tuxCubeBinL = (BoundingBox)tuxCubeCenterGroup.getBoundsInLocal();     
 //System.out.println("tuxCubeBinL " + tuxCubeBinL);       
        tuxCubeCenterGroup.setTranslateX(-(tuxCubeBinL.getMinX()+tuxCubeBinL.getMaxX())/2.0);
        tuxCubeCenterGroup.setTranslateY(-(tuxCubeBinL.getMinY()+tuxCubeBinL.getMaxY())/2.0);
        tuxCubeCenterGroup.setTranslateZ(-(tuxCubeBinL.getMinZ()+tuxCubeBinL.getMaxZ())/2.0);
        
        sceneDiameter = Math.sqrt(Math.pow(tuxCubeBinL.getWidth(), 2) + Math.pow(tuxCubeBinL.getHeight(), 2) + Math.pow(tuxCubeBinL.getDepth(), 2));

        playPauseTuxRotation(play);
        
        if (drawMode == DrawMode.LINE) {
            setDrawMode(drawMode);
        }
    }
    
    void setVantagePoint(VP vp) {
        
        Transform rotate = null;
         
        final double distance = distToSceneCenter(sceneDiameter/2);
      
        switch(vp) {
            case BOTTOM:
                rotate = new Rotate(90, Rotate.X_AXIS);
                break;
            case CORNER:
                Rotate rotateX = new Rotate(-45, Rotate.X_AXIS);
                Rotate rotateY = new Rotate(-45, new Point3D(0, 1, 1).normalize());
                rotate = rotateX.createConcatenation(rotateY);
                break;
            case FRONT:
                rotate = new Rotate();
                break;
            case TOP:
                rotate = new Rotate(-90, Rotate.X_AXIS);
               break;
        }
        
        viewingRotate.setToTransform(rotate);
                
        viewingTranslate.setX(0);
        viewingTranslate.setY(0);
        viewingTranslate.setZ(-distance);
    }
    
    void playPauseTuxRotation(boolean play) {
        if (play) {
            for (RotateTransition rot : tuxRotTransAll) {
                rot.play();
            }
        }
        else {
            for (RotateTransition rot : tuxRotTransAll) {
                rot.pause();
            }
        }
    }
    
    void stopTuxRotation() {
        for (RotateTransition rot : tuxRotTransAll) {
            rot.stop();
            rot.getNode().setRotate(0);
        }
    }
    
    void stopCubeRotation() {
        tuxCubeRotTimeline.stop();
        tuxCubeRotate.setAngle(0);
    }
    // range: [20 ... 50 ... 80] 
    void setRotationSpeed(float speed) {   
        if (speed < 49f) {
            tuxCubeRotTimeline.play();
            tuxCubeRotTimeline.setRate(1 * (49 - speed));
        }
        else if (speed > 51f) {            
            tuxCubeRotTimeline.play();
            tuxCubeRotTimeline.setRate(-1 * (speed - 51)); // negative rate works only while Timeline is playing !!
        }
        else {
            tuxCubeRotTimeline.pause();
        }     
    }
    
    void setDrawMode(DrawMode drawMode) {
        for (MeshView mv : meshViews) {
            mv.setDrawMode(drawMode);
        }
    }
    
    // TODO
    void setProjectionMode(String mode) {
        if (mode.equalsIgnoreCase("Parallel")) {
            
        }
        else {
            
        }
    }
           
    private MeshView createMeshView(Mesh mesh, Material material, MeshView[] meshViews, int index) {
        final MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        meshViews[index] = meshView;
        return meshView;
    } 
               
    private PhongMaterial[] createMaterials(int dim) {
        
        final PhongMaterial[] materials = new PhongMaterial[dim*dim];
        
        int k = 0;
        int direction = 1;
        float hue = 0;
        
        final float step = 1.0f / (dim*dim);

        for (int i=0; i < dim; i++) {
            for (int j=0; j < dim; j++) {
                
//System.out.println("hue = " + hue);
                materials[k] = new PhongMaterial();
                java.awt.Color hsbCol = new java.awt.Color(java.awt.Color.HSBtoRGB(hue, 0.85f, 0.7f));
                materials[k].setDiffuseColor(Color.rgb(hsbCol.getRed(), hsbCol.getGreen(), hsbCol.getBlue()));
                //materials[k].setDiffuseColor(Color.hsb(hue, 0.85, 0.7));
                materials[k].setSpecularColor(Color.color(0.2, 0.2, 0.2));
                materials[k].setSpecularPower(16f);
        
                hue += step * direction;
                k++;
            }

            direction *= (-1);
            if (direction < 0) {
                hue += step * (dim-1); 
            }
            else {
                hue += step * (dim+1);
            }
            
//            hue *= 360;
        }        
        
        return materials;
    }
    
    private double distToSceneCenter(double sceneRadius) {
        // Extra space
        final double borderFactor = 1.0;
        
        final double fov = perspectiveCamera.getFieldOfView();
        
        final double c3dWidth = subScene.getWidth();
        final double c3dHeight = subScene.getHeight();
        // Consider ratio of canvas' width and height
        double ratioFactor = 1.0;
        if (c3dWidth > c3dHeight) {
            ratioFactor = c3dWidth/c3dHeight;
        }
//System.out.println("sceneRadius       = " + sceneRadius);

        final double distToSceneCenter = borderFactor * ratioFactor * sceneRadius / Math.tan(Math.toRadians(fov/2));
//System.out.println("distToSceneCenter = " + distToSceneCenter);
        return distToSceneCenter;        
    }

    public PerspectiveCamera getPerspectiveCamera() {
		return perspectiveCamera;
	}
    
    public Group getTuxCubeRotGroup() {
		return tuxCubeRotGroup;
	}

	public OCCFXForm getTestCurvesXform() {
		return testCurvesXForm;
	}

	public List<BezierMesh> getBeziers() {
		return beziers;
	}
	
	public CubeViewer getCubeViewer() {
		return cubeViewer;
	}
	
}// end-of-class
