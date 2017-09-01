package sandbox2.adm.toxiclibs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.fxyz.geometry.Point3D;
import org.fxyz.shapes.primitives.TriangulatedMesh;
import org.fxyz3d.shapes.primitives.SpringMesh;
import org.fxyz3d.utils.CameraTransformer;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.stage.Stage;
import standaloneutils.JPADXmlReader;
import toxi.geom.mesh.STLReader;

class MyArgumentTest04 {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	@Option(name = "-o", aliases = { "--output-dir" }, required = true,
			usage = "output dir")
	private File _outputDir;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}
	public File getOutputDir() {
		return _outputDir;
	}

}

public class Test04 extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	public static File meshFile;

	double mousePosX;
	double mousePosY;
	double mouseOldX;
	double mouseOldY;
	double mouseDeltaX;
	double mouseDeltaY;

	static MeshView[] loadMeshViews(File meshFile) {
		StlMeshImporter importer = new StlMeshImporter();
		importer.read(meshFile);
		Mesh mesh = importer.getImport();
		importer.close();

		return new MeshView[] { new MeshView(mesh) };
	}

	private Group buildWing() {
//		MeshView[] meshViews = loadMeshViews(Test04.meshFile);

		System.out.println("Importing the binary STL via com.interactivemesh.jfx.importer into a Mesh ...");
		StlMeshImporter importer = new StlMeshImporter();
		importer.read(meshFile);
		Mesh mesh = importer.getImport();
		importer.close();
		System.out.println("Done.");
		
		//-------------------------------------------------
		// test toxiclibs triangle mesh import
		
		System.out.println("Importing the binary STL into a Toxiclib-TriangleMesh ...");
		System.out.println(meshFile.getAbsolutePath());
		toxi.geom.mesh.TriangleMesh tlMesh =
				(toxi.geom.mesh.TriangleMesh)new STLReader().loadBinary(
						meshFile.getAbsolutePath(), STLReader.TRIANGLEMESH);
//		System.out.println("Report - " + tmesh.toString());
//		System.out.println("Faces:");
//		tmesh.getFaces().stream()
//			.forEach(f -> System.out.println(f));
	
//		tlMesh.getVertices().stream()
//			.forEach(v -> System.out.println(v));
//
//		List<org.fxyz3d.geometry.Point3D> points =
//				tlMesh.getVertices().stream()
//					.map(v -> new org.fxyz3d.geometry.Point3D(v.x, v.y, v.z))
//					.collect(Collectors.toList());
//		org.fxyz3d.shapes.primitives.TriangulatedMesh trMesh 
//			= new org.fxyz3d.shapes.primitives.TriangulatedMesh(points);
		System.out.println("Done.");
//		return new Group(trMesh);
		
		//------------------------------------------------
		// test STLLoader by miho at Github
		System.out.println("Importing the binary STL via Mihosoft STLLoader ...");
		STLLoader sl = new STLLoader();
		try {
//			sl.parse(meshFile).stream()
//				.forEach(p -> System.out.println(p));
			List<eu.mihosoft.vvecmath.Vector3d> points =
					sl.parse(meshFile);
			System.out.println("Done.");
		} catch (RuntimeException | IOException e) {
			System.out.println("error reading " + meshFile.getName());
			//throw e;
		}

		return new Group(new MeshView(mesh));
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		PerspectiveCamera camera = new PerspectiveCamera(true);        
		camera.setNearClip(0.1);
		camera.setFarClip(10000.0);
		camera.setTranslateX(10);
		camera.setTranslateZ(-100);
		camera.setFieldOfView(20);

		CameraTransformer cameraTransform = new CameraTransformer();
		cameraTransform.getChildren().add(camera);
		cameraTransform.ry.setAngle(-30.0);
		cameraTransform.rx.setAngle(-15.0);

		SpringMesh spring = new SpringMesh(10, 2, 2, 8 * 2 * Math.PI, 200, 100, 0, 0);
		spring.setCullFace(CullFace.NONE);
		spring.setTextureModeVertices3D(1530, p -> p.f);

		Group wing = buildWing();
		
		Group group = new Group(
				cameraTransform, 
				spring, 
				wing);

		Scene scene = new Scene(group, 600, 400, true, SceneAntialiasing.BALANCED);
		scene.setFill(Color.BISQUE);
		scene.setCamera(camera);

		//First person shooter keyboard movement 
		scene.setOnKeyPressed(event -> {
			double change = 10.0;
			//Add shift modifier to simulate "Running Speed"
			if (event.isShiftDown()) {
				change = 50.0;
			}
			//What key did the user press?
			KeyCode keycode = event.getCode();
			//Step 2c: Add Zoom controls
			if (keycode == KeyCode.W) {
				camera.setTranslateZ(camera.getTranslateZ() + change);
			}
			if (keycode == KeyCode.S) {
				camera.setTranslateZ(camera.getTranslateZ() - change);
			}
			//Step 2d:  Add Strafe controls
			if (keycode == KeyCode.A) {
				camera.setTranslateX(camera.getTranslateX() - change);
			}
			if (keycode == KeyCode.D) {
				camera.setTranslateX(camera.getTranslateX() + change);
			}
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

		primaryStage.setScene(scene);
		primaryStage.setTitle("FXyz3D Sample");
		primaryStage.show();
	}

	public static void main(String[] args) {

		System.out.println("----------------------");
		System.out.println("Testing toxiclibs");
		System.out.println("----------------------");

		MyArgumentTest04 va = new MyArgumentTest04();
		Test04.theCmdLineParser = new CmdLineParser(va);

		try {
			Test04.theCmdLineParser.parseArgument(args);
			String pathToSTL = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToSTL);
			String pathToOutDir = va.getOutputDir().getAbsolutePath();
			System.out.println("OUTPUT DIR ===> " + pathToOutDir);
			System.out.println("--------------");

			Test04.meshFile = new File(pathToSTL);

			launch(args);

		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			Test03.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}
	}

}
