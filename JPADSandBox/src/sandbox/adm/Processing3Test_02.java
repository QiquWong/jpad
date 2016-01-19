package sandbox.adm;

import java.awt.Dimension;

import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;
import processing.core.*;
import processing.javafx.PGraphicsFX2D;
import processing.javafx.PSurfaceFX;
import processing.javafx.PSurfaceFX.PApplicationFX;

public class Processing3Test_02 extends PApplicationFX{

	private PApplet applet = new MyProcessingSketch();
    private Dimension appletSize;
	
    private PerspectiveCamera camera;
    private final double sceneWidth = 600;
    private final double sceneHeight = 600;

	
    @Override
    public void start(Stage primaryStage) {

    	System.out.println("Processing/JavaFX test ...");
    	
        Group rootGroup = new Group();
        Scene scene = new Scene(
        		rootGroup, 
        		sceneWidth, sceneHeight, 
        		true, SceneAntialiasing.BALANCED
        		);
        scene.setFill(Color.WHEAT);

    	//super.start(primaryStage);
        
        primaryStage.setTitle("Agodemar - Test");
        primaryStage.setScene(scene);
        primaryStage.show();       
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }    

    /*
     * TODO: code your sketch here
     */
    public class MyProcessingSketch extends PApplet {

    	public void setup() {
    		size(200,200);
    		background(0);
    	}

    	public void draw() {
    		stroke(255);
    		if (mousePressed) {
    			line(mouseX,mouseY,pmouseX,pmouseY);
    		}
    	}
    }

}
