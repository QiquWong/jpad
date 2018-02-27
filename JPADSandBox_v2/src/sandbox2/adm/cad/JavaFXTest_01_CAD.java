package sandbox2.adm.cad;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
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
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.JPADXmlReader;
import writers.JPADStaticWriteUtils;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
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
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;

import aircraft.components.Aircraft;
import analyses.OperatingConditions;
import cad.aircraft.MyAircraftBuilder;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;

public class JavaFXTest_01_CAD extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static Aircraft theAircraft;

	//-------------------------------------------------------------
	
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
		
		//--------------------------------------------------
		// get the aircraft object
		System.out.println("\n\n##################");
		System.out.println("function start :: getting the aircraft object ...");

		Aircraft aircraft = JavaFXTest_01_CAD.theAircraft; // object created in function main
		
		if (aircraft == null) {
			System.out.println("aircraft object null, returning.");
			return;
		}
		System.out.println("\n\n##################\n\n");
		
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

/*
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
*/

		primaryStage.setTitle("F(X)yz - Bezier Splines");
		primaryStage.setScene(scene);
		primaryStage.show();   

//		timerEffect.start();
	}
	/**
	 * Main
	 *
	 * @param args
	 * @throws InvalidFormatException 
	 * @throws HDF5LibraryException 
	 */
	public static void main(String[] args) throws InvalidFormatException, HDF5LibraryException {
		
		// redirect console output
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
		    public void write(int b) {
		         // write nothing
		    }
		});

		long startTime = System.currentTimeMillis();        
		
		System.out.println("-------------------");
		System.out.println("CAD Test");
		System.out.println("-------------------");
		
		ArgumentsCADTests va = new ArgumentsCADTests();
		JavaFXTest_01_CAD.theCmdLineParser = new CmdLineParser(va);
		
		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			JavaFXTest_01_CAD.theCmdLineParser.parseArgument(args);
			
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("AIRCRAFT INPUT ===> " + pathToXML);

			String pathToAnalysesXML = va.getInputFileAnalyses().getAbsolutePath();
			System.out.println("ANALYSES INPUT ===> " + pathToAnalysesXML);
			
			String pathToOperatingConditionsXML = va.getOperatingConditionsInputFile().getAbsolutePath();
			System.out.println("OPERATING CONDITIONS INPUT ===> " + pathToOperatingConditionsXML);
			
			String dirAirfoil = va.getAirfoilDirectory().getCanonicalPath();
			System.out.println("AIRFOILS ===> " + dirAirfoil);

			String dirFuselages = va.getFuselagesDirectory().getCanonicalPath();
			System.out.println("FUSELAGES ===> " + dirFuselages);
			
			String dirLiftingSurfaces = va.getLiftingSurfacesDirectory().getCanonicalPath();
			System.out.println("LIFTING SURFACES ===> " + dirLiftingSurfaces);
			
			String dirEngines = va.getEnginesDirectory().getCanonicalPath();
			System.out.println("ENGINES ===> " + dirEngines);
			
			String dirNacelles = va.getNacellesDirectory().getCanonicalPath();
			System.out.println("NACELLES ===> " + dirNacelles);
			
			String dirLandingGears = va.getLandingGearsDirectory().getCanonicalPath();
			System.out.println("LANDING GEARS ===> " + dirLandingGears);
			
			String dirSystems = va.getSystemsDirectory().getCanonicalPath();
			System.out.println("SYSTEMS ===> " + dirSystems);
			
			String dirCabinConfiguration = va.getCabinConfigurationDirectory().getCanonicalPath();
			System.out.println("CABIN CONFIGURATIONS ===> " + dirCabinConfiguration);
			
			System.out.println("--------------");

			//------------------------------------------------------------------------------------
			// Setup database(s)
			MyConfiguration.initWorkingDirectoryTree(
					MyConfiguration.databaseDirectory,
					MyConfiguration.inputDirectory,
					MyConfiguration.outputDirectory
					);
			
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

			////////////////////////////////////////////////////////////////////////
			// Aircraft creation
			System.out.println("\n\n\tCreating the Aircraft ... \n\n");
			
			// deactivating system.out
			System.setOut(filterStream);
			
			// default Aircraft ATR-72 ...
//			theAircraft = new Aircraft.AircraftBuilder(
//					"ATR-72",
//					AircraftEnum.ATR72,
//					aeroDatabaseReader,
//					highLiftDatabaseReader,
//			        fusDesDatabaseReader,
//					veDSCDatabaseReader
//					)
//					.build();

//			AircraftSaveDirectives asd = new AircraftSaveDirectives
//					.Builder("_ATR72")
//					.addAllWingAirfoilFileNames(
//							theAircraft.getWing().getAirfoilList().stream()
//									.map(a -> a.getAirfoilCreator().getName() + ".xml")
//									.collect(Collectors.toList())
//						)
//					.addAllHTailAirfoilFileNames(
//							theAircraft.getHTail().getAirfoilList().stream()
//									.map(a -> a.getAirfoilCreator().getName() + ".xml")
//									.collect(Collectors.toList())
//						)
//					.addAllVTailAirfoilFileNames(
//							theAircraft.getVTail().getAirfoilList().stream()
//									.map(a -> a.getAirfoilCreator().getName() + ".xml")
//									.collect(Collectors.toList())
//						)
//					.build();
//			
//			JPADStaticWriteUtils.saveAircraftToXML(theAircraft, MyConfiguration.getDir(FoldersEnum.INPUT_DIR), "aircraft_ATR72", asd);
			
			// reading aircraft from xml ... 
			theAircraft = Aircraft.importFromXML(
					pathToXML,
					dirLiftingSurfaces,
					dirFuselages,
					dirEngines,
					dirNacelles,
					dirLandingGears,
					dirCabinConfiguration,
					dirAirfoil,
					aeroDatabaseReader,
					highLiftDatabaseReader,
					fusDesDatabaseReader,
					veDSCDatabaseReader);
			
			// activating system.out
			System.setOut(originalOut);			
			System.out.println(theAircraft.toString());
			System.setOut(filterStream);
			
			////////////////////////////////////////////////////////////////////////
			// Set the folders tree
			MyConfiguration.initWorkingDirectoryTree(
					MyConfiguration.currentDirectoryString,
					MyConfiguration.inputDirectory, 
					MyConfiguration.outputDirectory);
			String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
			String aircraftFolder = JPADStaticWriteUtils.createNewFolder(folderPath + theAircraft.getId() + File.separator);
			String subfolderPath = JPADStaticWriteUtils.createNewFolder(aircraftFolder);

			////////////////////////////////////////////////////////////////////////
			// Defining the operating conditions ...
			System.setOut(originalOut);
			System.out.println("\n\n\tDefining the operating conditions ... \n\n");
			System.setOut(filterStream);
			OperatingConditions theOperatingConditions = OperatingConditions.importFromXML(pathToOperatingConditionsXML);
			System.setOut(originalOut);
			System.out.println(theOperatingConditions.toString());
//			System.setOut(filterStream);
			
			long estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println("\n\n\t TIME ESTIMATED = " + (estimatedTime/1000) + " seconds");
			
//			System.setOut(filterStream);
			
		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	
		
//		System.exit(1);
		launch(args);
	}    
}
