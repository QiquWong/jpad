package sandbox.adm.javafxd3.test;

import org.treez.javafxd3.javafx.FunctionPlotter;

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
		functionPlotter.setXDomain(0,7);	
		functionPlotter.setYDomain(-1.2,1.2);
		functionPlotter.plot("cos(2*x)*exp(-0.2*x^2)");

		//create the scene
		Scene scene = new Scene(functionPlotter, 750, 500, Color.web("#666970"));
		stage.setScene(scene);
		stage.show();	
	}	

	//#end region

}