package sandbox2.adm.toxiclibs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import standaloneutils.JPADXmlReader;
import toxi.geom.Line3D;
import toxi.geom.Vec3D;
import toxi.geom.mesh.BezierPatch;
import toxi.geom.mesh.Mesh3D;
import toxi.geom.mesh.STLReader;
import toxi.geom.mesh.TriangleMesh;
import toxi.geom.mesh.WETriangleMesh;
import toxi.geom.mesh.WingedEdge;
import toxi.math.noise.SimplexNoise;

class MyArgumentTest03 {
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

public class Test03 extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	private static File _meshFile;

	private static final double MODEL_SCALE_FACTOR = 10;
	private static final double MODEL_X_OFFSET = 0; // standard
	private static final double MODEL_Y_OFFSET = 0; // standard

	private static final int VIEWPORT_SIZE = 600;

	private static final Color lightColor = Color.rgb(244, 255, 250);
	private static final Color jewelColor = Color.rgb(0, 190, 222);

	private Group root;
	private PointLight pointLight;

	//--------------------------------------------------------------

	public static File getMeshFile() {
		return Test03._meshFile;
	}

	public static void setMeshFile(File _meshFile) {
		Test03._meshFile = _meshFile;
	}

	static MeshView[] loadMeshViews(File meshFile) {
		StlMeshImporter importer = new StlMeshImporter();
		importer.read(meshFile);
		Mesh mesh = importer.getImport();
		importer.close();

		return new MeshView[] { new MeshView(mesh) };
	}	  

	private Group buildScene() {
		MeshView[] meshViews = loadMeshViews(Test03.getMeshFile());
		for (int i = 0; i < meshViews.length; i++) {
			meshViews[i].setTranslateX(VIEWPORT_SIZE / 2 + MODEL_X_OFFSET);
			meshViews[i].setTranslateY(VIEWPORT_SIZE / 2 + MODEL_Y_OFFSET);
			meshViews[i].setTranslateZ(VIEWPORT_SIZE / 2);
			meshViews[i].setScaleX(MODEL_SCALE_FACTOR);
			meshViews[i].setScaleY(MODEL_SCALE_FACTOR);
			meshViews[i].setScaleZ(MODEL_SCALE_FACTOR);

			PhongMaterial sample = new PhongMaterial(jewelColor);
			sample.setSpecularColor(lightColor);
			sample.setSpecularPower(16);
			meshViews[i].setMaterial(sample);

			meshViews[i].getTransforms().setAll(new Rotate(38, Rotate.Z_AXIS), new Rotate(20, Rotate.X_AXIS));
		}

		pointLight = new PointLight(lightColor);
		pointLight.setTranslateX(VIEWPORT_SIZE*3/4);
		pointLight.setTranslateY(VIEWPORT_SIZE/2);
		pointLight.setTranslateZ(VIEWPORT_SIZE/2);
		PointLight pointLight2 = new PointLight(lightColor);
		pointLight2.setTranslateX(VIEWPORT_SIZE*1/4);
		pointLight2.setTranslateY(VIEWPORT_SIZE*3/4);
		pointLight2.setTranslateZ(VIEWPORT_SIZE*3/4);
		PointLight pointLight3 = new PointLight(lightColor);
		pointLight3.setTranslateX(VIEWPORT_SIZE*5/8);
		pointLight3.setTranslateY(VIEWPORT_SIZE/2);
		pointLight3.setTranslateZ(0);

		Color ambientColor = Color.rgb(80, 80, 80, 0);
		AmbientLight ambient = new AmbientLight(ambientColor);

		root = new Group(meshViews);
		root.getChildren().add(pointLight);
		root.getChildren().add(pointLight2);
		root.getChildren().add(pointLight3);
		root.getChildren().add(ambient);

		return root;
	}

	private PerspectiveCamera addCamera(Scene scene) {
		PerspectiveCamera perspectiveCamera = new PerspectiveCamera();
		System.out.println("Near Clip: " + perspectiveCamera.getNearClip());
		System.out.println("Far Clip:  " + perspectiveCamera.getFarClip());
		System.out.println("FOV:       " + perspectiveCamera.getFieldOfView());

		scene.setCamera(perspectiveCamera);
		return perspectiveCamera;
	}

	private class ZoomingPane extends Pane {
		Node content;
		private DoubleProperty zoomFactor = new SimpleDoubleProperty(1);

		private ZoomingPane(Node content) {
			this.content = content;
			getChildren().add(content);
			Scale scale = new Scale(1, 1);
			content.getTransforms().add(scale);

			zoomFactor.addListener(new ChangeListener<Number>() {
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					scale.setX(newValue.doubleValue());
					scale.setY(newValue.doubleValue());
					requestLayout();
				}
			});
		}	
		protected void layoutChildren() {
			Pos pos = Pos.TOP_LEFT;
			double width = getWidth();
			double height = getHeight();
			double top = getInsets().getTop();
			double right = getInsets().getRight();
			double left = getInsets().getLeft();
			double bottom = getInsets().getBottom();
			double contentWidth = (width - left - right)/zoomFactor.get();
			double contentHeight = (height - top - bottom)/zoomFactor.get();
			layoutInArea(content, left, top,
					contentWidth, contentHeight,
					0, null,
					pos.getHpos(),
					pos.getVpos());
		}

		public final Double getZoomFactor() {
			return zoomFactor.get();
		}
		public final void setZoomFactor(Double zoomFactor) {
			this.zoomFactor.set(zoomFactor);
		}
		public final DoubleProperty zoomFactorProperty() {
			return zoomFactor;
		}
	}

	@Override
	public void start(Stage primaryStage) {
		Group group = buildScene();
		group.setScaleX(2);
		group.setScaleY(2);
		group.setScaleZ(2);
		group.setTranslateX(50);
		group.setTranslateY(50);

		//		Scene scene = 
		//				new Scene(
		//						group, VIEWPORT_SIZE, VIEWPORT_SIZE, true
		//						);

		Slider slider = new Slider(0.5,2,1);
		ZoomingPane zoomingPane = new ZoomingPane(group);
		zoomingPane.zoomFactorProperty().bind(slider.valueProperty());
		
		Scene scene = new Scene(
				new BorderPane(zoomingPane, null, null, slider, null),
				VIEWPORT_SIZE, VIEWPORT_SIZE, false
				);

		scene.setFill(Color.rgb(10, 10, 40));
		addCamera(scene);
		primaryStage.setTitle("STL Viewer");
		primaryStage.setScene(scene);
		primaryStage.show();
	}


	public static void main(String[] args) {

		System.out.println("----------------------");
		System.out.println("Testing toxiclibs");
		System.out.println("----------------------");

		MyArgumentTest03 va = new MyArgumentTest03();
		Test03.theCmdLineParser = new CmdLineParser(va);

		try {
			Test03.theCmdLineParser.parseArgument(args);
			String pathToSTL = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToSTL);
			String pathToOutDir = va.getOutputDir().getAbsolutePath();
			System.out.println("OUTPUT DIR ===> " + pathToOutDir);
			System.out.println("--------------");

			//
			//			System.out.println("importing the binary STL into a TriangleMesh...");
			//
			//			toxi.geom.mesh.TriangleMesh mesh =
			//					(toxi.geom.mesh.TriangleMesh)new STLReader().loadBinary(
			//							pathToSTL,STLReader.TRIANGLEMESH);
			//			System.out.println("Report - " + mesh.toString());
			//
			//			System.out.println("Faces:");
			//			mesh.getFaces().stream()
			//			.forEach(f -> System.out.println(f));
			//
			//			System.out.println("Building the WETriangleMesh...");
			//
			//			WETriangleMesh wemesh = new WETriangleMesh("Agodemar Winged-Edge mesh");
			//			wemesh.addMesh(mesh);
			//			System.out.println("Report - " + wemesh.toString());
			//
			//			System.out.println("Edges:");
			//			// a lambda function taking 2 arguments
			//			BiConsumer<? super Line3D, ? super WingedEdge> actionEdge = (line, we) -> {
			//				//System.out.println(line);
			//				System.out.println(we);
			//				System.out.println("  Faces: "); 
			//				we.faces.forEach(f -> {
			//					StringBuilder sb = new StringBuilder();
			//					sb.append("  Face: ");
			//					sb.append("  "+ f.a +", "+ f.b +", "+ f.c);
			//					System.out.println(sb.toString());
			//				});
			//			};
			//			wemesh.edges.forEach(actionEdge);

			Test03.setMeshFile(new File(pathToSTL));

			System.setProperty("prism.dirtyopts", "false");
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