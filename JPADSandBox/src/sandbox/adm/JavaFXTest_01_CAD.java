package sandbox.adm;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import org.fxyz.cameras.CameraTransformer;
import org.fxyz.geometry.Point3D;
import org.fxyz.shapes.primitives.BezierMesh;
import org.fxyz.shapes.primitives.PrismMesh;
import org.fxyz.shapes.primitives.helper.InterpolateBezier;
import org.jcae.opencascade.jni.BRep_Builder;
import org.jcae.opencascade.jni.TopoDS_Compound;
import org.jcae.opencascade.jni.TopoDS_Edge;
import org.jcae.opencascade.jni.TopoDS_Face;
import org.jcae.opencascade.jni.TopoDS_Shape;
import org.jcae.opencascade.jni.TopoDS_Vertex;
import org.jcae.opencascade.jni.TopoDS_Wire;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;

import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import cad.aircraft.MyAircraftBuilder;
import cad.aircraft.MyLiftingSurfaceBuilder;
import cad.occ.OCCFXMeshExtractor;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;

public class JavaFXTest_01_CAD extends Application {
	    private PerspectiveCamera camera;
	    private final double sceneWidth = 600;
	    private final double sceneHeight = 600;
	    private final CameraTransformer cameraTransform = new CameraTransformer();
	    
	    private double mousePosX;
	    private double mousePosY;
	    private double mouseOldX;
	    private double mouseOldY;
	    private double mouseDeltaX;
	    private double mouseDeltaY;
	    private ArrayList<BezierMesh> beziers;
	    private Rotate rotateY;
	    private Function<Point3D, Number> dens = p->p.f;
	    private Function<Number, Number> func = t->t;
	    private long lastEffect;
	    
	    @Override
	    public void start(Stage primaryStage) throws Exception {
	        Group sceneRoot = new Group();
	        Scene scene = new Scene(sceneRoot, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
	        scene.setFill(Color.BLACK);
	        camera = new PerspectiveCamera(true);        
	     
	        //setup camera transform for rotational support
	        cameraTransform.setTranslate(0, 0, 0);
	        cameraTransform.getChildren().add(camera);
	        camera.setNearClip(0.1);
	        camera.setFarClip(10000.0);
	        camera.setTranslateZ(-10);
	        cameraTransform.ry.setAngle(-45.0);
	        cameraTransform.rx.setAngle(-10.0);
	        //add a Point Light for better viewing of the grid coordinate system
	        PointLight light = new PointLight(Color.WHITE);
	        cameraTransform.getChildren().add(light);
	        cameraTransform.getChildren().add(new AmbientLight(Color.WHITE));
	        light.setTranslateX(camera.getTranslateX());
	        light.setTranslateY(camera.getTranslateY());
	        light.setTranslateZ(camera.getTranslateZ());        
	        scene.setCamera(camera);
	        
	        rotateY = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
	        Group group = new Group();
	        group.getChildren().add(cameraTransform);    
	        
	        //------------------------------------------------------------------
	        // AGODEMAR

			// Default operating conditions
			OperatingConditions theOperatingConditions = new OperatingConditions();
			
			// Initialize Aircraft with default parameters
			Aircraft theAircraft = new Aircraft(
					ComponentEnum.FUSELAGE, 
					ComponentEnum.WING,
					ComponentEnum.HORIZONTAL_TAIL,
					ComponentEnum.VERTICAL_TAIL,
					ComponentEnum.POWER_PLANT,
					ComponentEnum.NACELLE,
					ComponentEnum.LANDING_GEAR
					);
			theAircraft.set_name("TestWing");

			// Gotta create this object
			ACAnalysisManager _theAnalysis = new ACAnalysisManager(
					theOperatingConditions,
					theAircraft,
					AnalysisTypeEnum.AERODYNAMIC);
			
			_theAnalysis.updateGeometry(theAircraft);
			
			// CAD/OCC/OccJava-related stuff
			// see TestOCCMeshExtractor
			BRep_Builder theBuilder = new BRep_Builder();
			TopoDS_Compound theCompound = new TopoDS_Compound();
			theBuilder.makeCompound(theCompound);

			MyAircraftBuilder aircraftBuilder = new MyAircraftBuilder();
			aircraftBuilder.buildCAD(theAircraft);
			theBuilder.add(theCompound, aircraftBuilder.getTheCurrentCompound());

			OCCFXMeshExtractor occMeshExtractor = 
					new OCCFXMeshExtractor((TopoDS_Shape)theCompound);

			System.out.println("############################################");
			System.out.println("### CAD stats");
			System.out.println("############################################");

			Collection<TopoDS_Face> faces = occMeshExtractor.getFaces();
			System.out.println("Faces: " + faces.size());

			Collection<TopoDS_Edge> edges = occMeshExtractor.getEdges();
			System.out.println("Edges: " + edges.size());

			Collection<TopoDS_Edge> freeEdges = occMeshExtractor.getFreeEdges();
			System.out.println("Free Edges: " + freeEdges.size());

			Collection<TopoDS_Vertex> vertices = occMeshExtractor.getVertices();
			System.out.println("Vertices: " + vertices.size());

			Collection<TopoDS_Wire> wires = occMeshExtractor.getWires();
			System.out.println("Wires: " + wires.size());

			Collection<TopoDS_Compound> compounds = occMeshExtractor.getCompounds();
			System.out.println("Compounds: " + compounds.size());

			System.out.println("############################################");
	        
			//---------------------------------------------------------------------------
			// see TestOCCMeshExtractor
			
			List<TriangleMesh> theTriangleMeshList = new ArrayList<TriangleMesh>();
			
			// transform the Collection into a List
			List<TopoDS_Face> faceList = new ArrayList<TopoDS_Face>(faces);

			// collect data
			if (faceList.size() > 0) {

				System.out.println("Now take faces via OCCMeshExtractor.FaceData ...");

				// loop on all faces in CAD obj
				for(TopoDS_Face face : faceList) {

					// int kFace = 0;
					OCCFXMeshExtractor.FaceData faceData = 
							new OCCFXMeshExtractor.FaceData(
									// faceList.get(kFace),
									face,
									false
									);
					// build triangulation
					faceData.load();
					System.out.println(
							"--------- Triangulation ...\n" +
									"NbrOfPolys: " + faceData.getNbrOfPolys() + "\n" +
									"Polys length: " + faceData.getPolys().length + "\n" +
									"Polys:\n" + Ints.asList(faceData.getPolys()) + "\n" + // Guava
									"Triangles length: " + faceData.getITriangles().length + "\n" +
									"Triangles:\n" + Ints.asList(faceData.getITriangles()) + "\n" + // Guava
									"NbrOfVertices: " + faceData.getNbrOfVertices() + "\n" +
									"Vertices: " + faceData.getVertices().length + "\n" +
									"NbrOfLins: " + faceData.getNbrOfLines() + "\n" +
									"Lines: " + faceData.getLines().length + "\n" +
									"Nodes: " + faceData.getNodes().length
							);
					List<Float> nodes = Floats.asList(faceData.getNodes()); // Guava
					System.out.println(
							"Nodes :: " + nodes
							);

					// create the mesh
					TriangleMesh mesh = new TriangleMesh();

					//				Points, i.e. nodes
					//				mesh.getPoints().addAll(faceData.getNodes());
					//				
					//				//for now we'll just make an empty texCoordinate group
					//				mesh.getTexCoords().addAll(0, 0);
					//				
					//				//Add the faces "winding" the points generally counter clock wise
					//				for (int i = 0; i < faceData.getITriangles().length; )
					//				{
					//					// System.out.println("i: " + i);
					//					mesh.getFaces().addAll(
					//							faceData.getITriangles()[i++],0,
					//							faceData.getITriangles()[i++],0,
					//							faceData.getITriangles()[i++],0
					//					);
					//				}

					mesh = faceData.getTriangleMesh();
					//				System.out.println(
					//						"TriangleMesh :: FaceElementSize: " + _theTriangleMesh.getFaceElementSize()
					//				);

					theTriangleMeshList.add(mesh);

				} // end-of-for loop

			} // if faces != null
			
			// see MyFXAircraft3DView
			final PhongMaterial material = new PhongMaterial();
			material.setDiffuseColor(Color.GRAY);
			material.setSpecularColor(Color.LIGHTGREY);
			
			// use a lambda here !! (need Java 8, of course)
			List<MeshView> meshViews = 
					theTriangleMeshList // get the TriangleMesh list from the OCC extractor object
					.stream()
					.map(mesh -> {
						MeshView meshView = new MeshView(mesh); // get a MeshView for each TriangleMesh
						meshView.setMaterial(material);
						meshView.setCullFace(CullFace.NONE); // set to CullFace.NONE (remove culling) to show back lines
						meshView.setId("Aircraft-Face");
						return meshView;
					})
					.collect(Collectors.toList());
			
			
			Group aircraftGroup = new Group();
			aircraftGroup.getChildren().addAll(meshViews);
			
			
	        //------------------------------------------------------------------
	        
	        
//	        List<Point3D> knots=Arrays.asList(new Point3D(0f,0f,0f),new Point3D(3f,0f,2f),
//	                new Point3D(5f,2f,3f),new Point3D(7f,-3f,0f),new Point3D(6f,-1f,-4f));
	        List<Point3D> knots=Arrays.asList(new Point3D(3f,0f,0f),new Point3D(0.77171f,1.68981f,0.989821f),
	                new Point3D(-0.681387f,0.786363f,-0.281733f),new Point3D(-2.31757f,-0.680501f,-0.909632f),
	                new Point3D(-0.404353f,-2.81233f,0.540641f),new Point3D(1.1316f,-0.727237f,0.75575f),
	                new Point3D(1.1316f,0.727237f,-0.75575f),new Point3D(-0.404353f,2.81233f,-0.540641f),
	                new Point3D(-2.31757f,0.680501f,0.909632f),new Point3D(-0.681387f,-0.786363f,0.281733f),
	                new Point3D(0.77171f,-1.68981f,-0.989821f),new Point3D(3f,0f,0f));
	        
	        boolean showControlPoints=true;
	        boolean showKnots=true;
	        
	        InterpolateBezier interpolate = new InterpolateBezier(knots);
	        beziers=new ArrayList<>();
	        AtomicInteger sp=new AtomicInteger();
	        if(showKnots || showControlPoints){
	            interpolate.getSplines().forEach(spline->{
	                Point3D k0=spline.getPoints().get(0);
	                Point3D k1=spline.getPoints().get(1);
	                Point3D k2=spline.getPoints().get(2);
	                Point3D k3=spline.getPoints().get(3);
	                if(showKnots){
	                    Sphere s=new Sphere(0.2d);
	                    s.getTransforms().add(new Translate(k0.x, k0.y, k0.z));
	                    s.setMaterial(new PhongMaterial(Color.GREENYELLOW));
	                    group.getChildren().add(s);
	                    s=new Sphere(0.2d);
	                    s.getTransforms().add(new Translate(k3.x, k3.y, k3.z));
	                    s.setMaterial(new PhongMaterial(Color.GREENYELLOW));
	                    group.getChildren().add(s);
	                }
	                if(showControlPoints){
	                    PrismMesh c = new PrismMesh(0.03d, 1d, 1, k0, k1);
	                    c.setTextureModeNone(Color.GREEN);
	                    group.getChildren().add(c);

	                    c = new PrismMesh(0.03d, 1d, 1, k1, k2);
	                    c.setTextureModeNone(Color.GREEN);
	                    group.getChildren().add(c);

	                    c = new PrismMesh(0.03d, 1d, 1, k2, k3);
	                    c.setTextureModeNone(Color.GREEN);
	                    group.getChildren().add(c);

	                    Sphere s=new Sphere(0.1d);
	                    s.getTransforms().add(new Translate(k1.x, k1.y, k1.z));
	                    s.setMaterial(new PhongMaterial(Color.RED));
	                    group.getChildren().add(s);
	                    s=new Sphere(0.1d);
	                    s.getTransforms().add(new Translate(k2.x, k2.y, k2.z));
	                    s.setMaterial(new PhongMaterial(Color.RED));
	                    group.getChildren().add(s);
	                }
	            });
	        }
	        long time=System.currentTimeMillis();
	        interpolate.getSplines().stream().forEach(spline->{
	            BezierMesh bezier = new BezierMesh(spline,0.1d,
	                                    300,20,0,0);
//	            bezier.setDrawMode(DrawMode.LINE);
//	            bezier.setCullFace(CullFace.NONE);
//	            bezier.setSectionType(SectionType.TRIANGLE);

	        // NONE
//	            bezier.setTextureModeNone(Color.hsb(360d*sp.getAndIncrement()/interpolate.getSplines().size(), 1, 1));
	        // IMAGE
//	            bezier.setTextureModeImage(getClass().getResource("res/LaminateSteel.jpg").toExternalForm());
	        // PATTERN
//	           bezier.setTextureModePattern(3d);
	        // FUNCTION
	            bezier.setTextureModeVertices1D(1530,t->spline.getKappa(t.doubleValue()));
//	            bezier.setTextureModeVertices1D(1530,func);
	        // DENSITY
//	            bezier.setTextureModeVertices3D(256*256,dens);
	        // FACES
//	            bezier.setTextureModeFaces(256*256);

	            bezier.getTransforms().addAll(new Rotate(0,Rotate.X_AXIS),rotateY);
	            beziers.add(bezier);
	        });
	        System.out.println("time: "+(System.currentTimeMillis()-time)); //43.815->25.606->15
	        group.getChildren().addAll(beziers);
	        
	        sceneRoot.getChildren().addAll(group);
	        
	        // AGODEMAR
	        sceneRoot.getChildren().addAll(aircraftGroup);
	        
	        //First person shooter keyboard movement 
	        scene.setOnKeyPressed(event -> {
	            double change = 10.0;
	            //Add shift modifier to simulate "Running Speed"
	            if(event.isShiftDown()) { change = 50.0; }
	            //What key did the user press?
	            KeyCode keycode = event.getCode();
	            //Step 2c: Add Zoom controls
	            if(keycode == KeyCode.W) { camera.setTranslateZ(camera.getTranslateZ() + change); }
	            if(keycode == KeyCode.S) { camera.setTranslateZ(camera.getTranslateZ() - change); }
	            //Step 2d:  Add Strafe controls
	            if(keycode == KeyCode.A) { camera.setTranslateX(camera.getTranslateX() - change); }
	            if(keycode == KeyCode.D) { camera.setTranslateX(camera.getTranslateX() + change); }
	        });        
	        
	        scene.setOnMousePressed((MouseEvent me) -> {
	            mousePosX = me.getSceneX();
	            mousePosY = me.getSceneY();
	            mouseOldX = me.getSceneX();
	            mouseOldY = me.getSceneY();
	        });
	        scene.setOnMouseDragged((MouseEvent me) -> {
	            mouseOldX = mousePosX;
	            mouseOldY = mousePosY;
	            mousePosX = me.getSceneX();
	            mousePosY = me.getSceneY();
	            mouseDeltaX = (mousePosX - mouseOldX);
	            mouseDeltaY = (mousePosY - mouseOldY);
	            
	            double modifier = 10.0;
	            double modifierFactor = 0.1;
	            
	            if (me.isControlDown()) {
	                modifier = 0.1;
	            }
	            if (me.isShiftDown()) {
	                modifier = 50.0;
	            }
	            if (me.isPrimaryButtonDown()) {
	                cameraTransform.ry.setAngle(((cameraTransform.ry.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);  // +
	                cameraTransform.rx.setAngle(((cameraTransform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);  // -
	            } else if (me.isSecondaryButtonDown()) {
	                double z = camera.getTranslateZ();
	                double newZ = z + mouseDeltaX * modifierFactor * modifier;
	                camera.setTranslateZ(newZ);
	            } else if (me.isMiddleButtonDown()) {
	                cameraTransform.t.setX(cameraTransform.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
	                cameraTransform.t.setY(cameraTransform.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
	            }
	        });
	        
	        lastEffect = System.nanoTime();
	        AtomicInteger count=new AtomicInteger();
	        AnimationTimer timerEffect = new AnimationTimer() {

	            @Override
	            public void handle(long now) {
	                if (now > lastEffect + 1_000_000_000l) {
//	                    Point3D loc = knot.getPositionAt((count.get()%100)*2d*Math.PI/100d);
//	                    Point3D dir = knot.getTangentAt((count.get()%100)*2d*Math.PI/100d);
//	                    cameraTransform.t.setX(loc.x);
//	                    cameraTransform.t.setY(loc.y);
//	                    cameraTransform.t.setZ(-loc.z);
//	                    javafx.geometry.Point3D axis = cameraTransform.rx.getAxis();
//	                    javafx.geometry.Point3D cross = axis.crossProduct(-dir.x,-dir.y,-dir.z);
//	                    double angle = axis.angle(-dir.x,-dir.y,-dir.z);
//	                    cameraTransform.rx.setAngle(angle);
//	                    cameraTransform.rx.setAxis(new javafx.geometry.Point3D(cross.getX(),-cross.getY(),cross.getZ()));
//	                    dens = p->(float)(p.x*Math.cos(count.get()%100d*2d*Math.PI/50d)+p.y*Math.sin(count.get()%100d*2d*Math.PI/50d));
	                    func=t->Math.pow(t.doubleValue(),(count.get()%5d));
	                    beziers.forEach(b->b.setFunction(func));
//	                    knot.setP(1+(count.get()%5));
//	                    knot.setQ(2+(count.get()%15));
	                    
//	                    if(count.get()%100<50){
//	                        knot.setDrawMode(DrawMode.LINE);
//	                    } else {
//	                        knot.setDrawMode(DrawMode.FILL);
//	                    }
//	                    beziers.forEach(b->b.setColors((int)Math.pow(2,count.get()%16)));
//	                    beziers.forEach(b->b.setWireRadius(0.1d+(count.get()%6)/10d));
//	                    beziers.forEach(b->b.setPatternScale(1d+(count.get()%10)*3d));
//	                    beziers.forEach(b->b.setSectionType(SectionType.values()[count.get()%SectionType.values().length]));
	                    count.getAndIncrement();
	                    lastEffect = now;
	                }
	            }
	        };
	        
	        
	        primaryStage.setTitle("F(X)yz - Bezier Splines");
	        primaryStage.setScene(scene);
	        primaryStage.show();   
	        
	        timerEffect.start();
	        
	    }
	    /**
	     * @param args the command line arguments
	     */
	    public static void main(String[] args) {
	        launch(args);
	    }    
	}
