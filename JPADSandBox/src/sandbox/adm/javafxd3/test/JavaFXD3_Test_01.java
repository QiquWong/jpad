package sandbox.adm.javafxd3.test;

import com.github.javafxd3.javafx.FunctionPlotter;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JavaFXD3_Test_01 extends Application {

	//#region METHODS

	/**
	 * Main
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {

		//set stage title
		stage.setTitle("Functionplotter demo");		
		
		//create function plotter
		FunctionPlotter functionPlotter = new FunctionPlotter();
		functionPlotter.setXDomain(-2,2);	
		functionPlotter.setYDomain(-0.2,1.2);
		functionPlotter.plot("[{fn: 'exp(-x^2)'}]");

		//create the scene
		Scene scene = new Scene(functionPlotter, 750, 500, Color.web("#666970"));
		stage.setScene(scene);
		stage.show();	
	}	

	//#end region

}
