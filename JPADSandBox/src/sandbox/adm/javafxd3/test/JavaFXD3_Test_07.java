package sandbox.adm.javafxd3.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.xml.transform.TransformerException;

import org.treez.javafxd3.javafx.JavaFxD3Browser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import configuration.MyConfiguration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sandbox.adm.D3PlotterOptions;
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

		Double[][] dataArray = {
				{ 0.0, 0.0 },
				{ 20.0, 15.5 },
				{ 50.0, 10.0 },
				{ 40.0, -10.0 },
				{ 35.0, 18.0 }
				};

		D3PlotterOptions options = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(-1.0, 100.0)
				.yRange(-50.0, 100.0)
				.plotArea(true)
				.build();

		System.out.println("Options:\n" + options);

//		d3Plotter = new D3Plotter(
//				WIDTH, HEIGHT, // svg dimensions
//				40, 20, 50, 60, // plot margins, t r b l
//				12.0, 8.0, // xtickPadding, ytickPadding
//				true, // showSymbols
//				true, // showLegend
//				true, // plotArea
//				dataArray
//				);

		d3Plotter = new D3Plotter(
				options,
				dataArray
				);


		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {
			System.out.println("Runnable :: Initial loading of browser is finished");

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter.createD3Content();

			//--------------------------------------------------
			// output
			String outputFilePath = outputFolderPath + "test5.svg";
			d3Plotter.saveSVG(outputFilePath);


		}; // end-of-Runnable

		// create the Browser/D3
		//create browser
		JavaFxD3Browser browser = d3Plotter.getBrowser(postLoadingHook, false);

		//create the scene
		Scene scene = new Scene(browser, WIDTH+10, HEIGHT+10, Color.web("#666970"));
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
