package sandbox2.mds.javafx;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class JavaFX_Test_03 extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {	
		// creating the parent node and the main scene
		Group root = new Group();		
		Scene scene = new Scene(root, 800, 800);
		
		// setting the camera
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setTranslateZ(-1000);
		camera.setNearClip(0.1);
		camera.setFarClip(2000);
		camera.setFieldOfView(35);
		scene.setCamera(camera);
		
		// creating a new material
		PhongMaterial blueStuff = new PhongMaterial();
		blueStuff.setDiffuseColor(Color.LIGHTBLUE);
		blueStuff.setSpecularColor(Color.BLUE);
		
		// adding a cylinder
		double cyl_radius = 100;
		double cyl_height = 50;
		Cylinder cylinder = new Cylinder(cyl_radius, cyl_height);
		cylinder.setMaterial(blueStuff);
		root.getChildren().add(cylinder);
		
		// translating the cylinder
		cylinder.setTranslateX(-200);
		cylinder.setTranslateY(200);
		cylinder.setTranslateZ(200);
		
		// creating a box
		double box_depth = 100;
		double box_height = 100;
		double box_width = 100;
		Box box = new Box(box_depth, box_height, box_width);
		box.setMaterial(blueStuff);
		root.getChildren().add(box);
		
		// translating the box
		box.setTranslateX(150);
		box.setTranslateY(-100);
		box.setTranslateZ(-100);
		
		// rotating the box
		Rotate rxBox = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
		Rotate ryBox = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
		Rotate rzBox = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);
		rxBox.setAngle(30);
		ryBox.setAngle(50);
		rzBox.setAngle(30);
		box.getTransforms().addAll(rxBox, ryBox, rzBox);
		
		// adding a sphere
		double sphere_radius = 100;
		Sphere sphere = new Sphere(sphere_radius);
		sphere.setMaterial(blueStuff);
		root.getChildren().add(sphere);
		
		// translating the sphere
		sphere.setTranslateX(-180);
		sphere.setTranslateY(-100);
		sphere.setTranslateZ(100);
				
		// building a pyramid
		TriangleMesh pyramidMesh = new TriangleMesh();
		
		// populating the texture coordinates
		pyramidMesh.getTexCoords().addAll(0, 0); // adding just one couple of fake coordinates (necessary!)
		
		// populating the vertices
		float h = 150; // height
		float s = 300; // side
		pyramidMesh.getPoints().addAll(
				0,    0,   0,    // Point 0
				0,    h,   -s/2, // Point 1
				-s/2, h,   0,    // Point 2
				s/2,  h,   0,    // Point 3
				0,    h,   s/2	 // Point 4	
				);
		
		// populating the faces
		pyramidMesh.getFaces().addAll(
				0,0,  2,0,  1,0,
				0,0,  1,0,  3,0,
				0,0,  3,0,  4,0,
				0,0,  4,0,  2,0,  
				4,0,  1,0,  2,0,
				4,0,  3,0,  1,0
				);
			
		// drawing the 3D pyramid
		MeshView pyramid = new MeshView(pyramidMesh);
		pyramid.setDrawMode(DrawMode.FILL);
		pyramid.setMaterial(blueStuff);
		root.getChildren().add(pyramid);
		
		// translating the pyramid
		pyramid.setTranslateX(200);
		pyramid.setTranslateY(100);
		pyramid.setTranslateZ(200);
		
		// animating the objects
		RotateTransition rtCylinder = new RotateTransition();
		rtCylinder.setNode(cylinder);
		rtCylinder.setDuration(Duration.millis(3000));
		rtCylinder.setAxis(Rotate.X_AXIS);
		rtCylinder.setByAngle(360);
		rtCylinder.setCycleCount(Animation.INDEFINITE);
		rtCylinder.setInterpolator(Interpolator.LINEAR);
		rtCylinder.play();
		
		RotateTransition rtBox = new RotateTransition();
		rtBox.setNode(box);
		rtBox.setDuration(Duration.millis(3000));
		rtBox.setAxis(Rotate.Z_AXIS);
		rtBox.setByAngle(360);
		rtBox.setCycleCount(Animation.INDEFINITE);
		rtBox.setInterpolator(Interpolator.LINEAR);
		rtBox.play();
		
		RotateTransition rtSphere = new RotateTransition();
		rtSphere.setNode(sphere);
		rtSphere.setDuration(Duration.millis(9000));
		rtSphere.setAxis(Rotate.Y_AXIS);
		rtSphere.setByAngle(360);
		rtSphere.setCycleCount(Animation.INDEFINITE);
		rtSphere.setInterpolator(Interpolator.LINEAR);
		rtSphere.play();
		
		RotateTransition rtPyramid = new RotateTransition();
		rtPyramid.setNode(pyramid);
		rtPyramid.setDuration(Duration.millis(3000));
		rtPyramid.setAxis(Rotate.Y_AXIS);
		rtPyramid.setByAngle(360);
		rtPyramid.setCycleCount(Animation.INDEFINITE);
		rtPyramid.setInterpolator(Interpolator.LINEAR);
		rtPyramid.play();
		
		// adding a light source
		PointLight light = new PointLight(Color.WHITE);
		light.setTranslateX(-1000);
		light.setTranslateY(100);
		light.setTranslateZ(-1000);
		root.getChildren().add(light);
		
		// showing the stage
		primaryStage.setScene(scene);
		primaryStage.setTitle("3D Objects");
		primaryStage.show();
	}

}
