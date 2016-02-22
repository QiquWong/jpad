package sandbox.adm.javafxd3.test;

import java.io.File;

import org.treez.javafxd3.javafx.JavaFxD3Browser;

import configuration.MyConfiguration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import writers.JPADStaticWriteUtils;

public class JavaFXD3_Test_07  extends Application {

	private D3Plotter d3Plotter;

	private final int WIDTH = 700;
	private final int HEIGHT = 600;

	@Override
	public void start(Stage primaryStage) throws Exception {

		//--------------------------------------------------
		System.out.println("Initializing test class...");
		String rootOutputFolderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String outputFolderPath = JPADStaticWriteUtils.createNewFolder(rootOutputFolderPath + "Test_D3" + File.separator);
		System.out.println("Output ==> " + outputFolderPath);

		//--------------------------------------------------
		//set state title
		primaryStage.setTitle("treez/javafx-d3 - D3Plotter demo");

		d3Plotter = new D3Plotter(WIDTH, HEIGHT);

		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {
			System.out.println("Runnable :: Initial loading of browser is finished");

			//do some d3 stuff
			d3Plotter.createD3Content();

		};

		// create the Browser/D3
		//create browser
		JavaFxD3Browser browser = d3Plotter.getBrowser(postLoadingHook, false);

		//create the scene
		Scene scene = new Scene(browser, WIDTH, HEIGHT, Color.web("#666970"));
		primaryStage.setScene(scene);
		primaryStage.show();

	}


	/**
	 * Main
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
