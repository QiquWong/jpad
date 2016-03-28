package sandbox.adm.javafxd3.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.treez.javafxd3.d3.behaviour.Zoom;
import org.treez.javafxd3.d3.behaviour.Zoom.ZoomEventType;
import org.treez.javafxd3.d3.core.Selection;
import org.treez.javafxd3.d3.functions.DatumFunction;
import org.treez.javafxd3.javafx.JavaFxD3Browser;
import org.treez.javafxd3.d3.svg.SymbolType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import configuration.MyConfiguration;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sandbox.adm.D3PlotterOptions;
import writers.JPADStaticWriteUtils;

public class JavaFXD3_Test_07  extends Application {

	private D3Plotter d3Plotter;

	private final int WIDTH = 700;
	private final int HEIGHT = 600;
	
	private static final double DELTA = 0.001d;

	public static class NoopDatumFunction implements DatumFunction<Void> {
		@Override
		public Void apply(Object context, Object d, int index) {
			return null;
		}
	}

	private final NoopDatumFunction noopListener = new NoopDatumFunction();

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

		List<Double[][]> listDataArray = new ArrayList<Double[][]>();
		
		// the data we want to plot [x,y]
		Double[][] dataArray1 = {
				{ 0.0, 0.0 },
				{ 20.0, 15.5 },
				{ 50.0, 10.0 },
				{ 40.0, -10.0 },
				{ 35.0, 18.0 }
				};

		Double[][] dataArray2 = {
				{ 5.0, 0.0 },
				{ 10.0, -5.5 },
				{ 50.0, -10.0 },
				{ 40.0, 0.0 },
				{ 35.0, -8.0 }
				};
		
		listDataArray.add(dataArray1);
		listDataArray.add(dataArray2);
		
		D3PlotterOptions options = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(-1.0, 60.0).yRange(-50.0, 60.0)
				.axisLineColor("magenta").axisLineStrokeWidth("5px")
				.graphBackgroundColor("yellow").graphBackgroundOpacity(0.2)
//				.symbolType(SymbolType.TRIANGLE_UP)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.TRIANGLE_UP
						)
//				.symbolStyle("fill:yellow; stroke:green; stroke-width:2")
				.symbolStyles(
						"fill:cyan; stroke:green; stroke-width:2",
						"fill:blue; stroke:red; stroke-width:2"
						)
//				.lineStyle(
//						"fill:none; stroke:orange; stroke-dasharray: 15px, 8px; stroke-width:2"
//						)
				.lineStyles(
						"fill:none; stroke:red; stroke-width:2",
						"fill:none; stroke:magenta; stroke-dasharray: 15px, 2px; stroke-width:2"
						)
//				.plotArea(true)
				.plotAreas(false,true)
				.areaStyle("fill:orange;")
				// TODO
//				.areaStyles("fill:orange;","fill:yellow;")
				.areaOpacity(0.7)
				// TODO
//				.areaOpacities(0.7,0.5)
				//.legendItems("Pippo1", "agodemar2", "crocco3")
				.build();

		System.out.println("Plot options:\n" + options);

		d3Plotter = new D3Plotter(
				options,
				listDataArray
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
		
		// TODO manage keyboard events
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                System.out.println("Key Pressed: " + ke.getCode());
        		////*
                
                Zoom zoom = d3Plotter.getD3().behavior().zoom();
                zoom.center(10,10);
                
                System.out.println(zoom.size().get(0, Double.class));
                System.out.println(zoom.size().get(1, Double.class));
                
                Selection body = d3Plotter.getD3().select("body");
                zoom.event(body);
        		zoom.event(body.transition());
        		
        		zoom.on(ZoomEventType.ZOOMSTART, noopListener);
        		zoom.on(ZoomEventType.ZOOM, noopListener);
        		zoom.on(ZoomEventType.ZOOMEND, noopListener);

                switch (ke.getCode()) {
				case PLUS:
//                	browser.setScaleX(
//                			browser.getScaleX()*1.2
//                			);
//                	browser.setScaleY(
//                			browser.getScaleY()*1.2
//                			);
                	
                	zoom.scale();
            		zoom.scale(5.0);

            		zoom.scaleExtent();
            		zoom.scaleExtent(new Double[] { 5.0, 4.0 });
            		zoom.translate();
            		zoom.translate(new Double[] { 5.0, 6.0 });
                	
					break;
				case MINUS:
//                	browser.setScaleX(
//                			browser.getScaleX()*0.80
//                			);
//                	browser.setScaleY(
//                			browser.getScaleY()*0.80
//                			);
					break;
				default:
					break;
				}
        		///*/
                browser.requestLayout();
            }
        });
		
		// SHOW THE SCEN FINALLY
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
