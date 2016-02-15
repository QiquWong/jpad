package sandbox.adm.javafxd3.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.treez.javafxd3.javafx.SaveHelper;

import com.github.javafxd3.d3.D3;
import com.github.javafxd3.d3.coords.Coords;
import com.github.javafxd3.d3.core.EnteringSelection;
import com.github.javafxd3.d3.core.Selection;
import com.github.javafxd3.d3.core.UpdateSelection;
import com.github.javafxd3.d3.functions.DatumFunction;
import com.github.javafxd3.d3.scales.LinearScale;
import com.github.javafxd3.d3.svg.Line;
import com.github.javafxd3.d3.svg.Line.InterpolationMode;
import com.github.javafxd3.javafx.JavaFxD3Browser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;

/**
 * Demonstrates how d3.js can be used with a JavaFx WebView
 *
 */
public class JavaFXD3_Test_03 extends Application {

	//#region ATTRIBUTES

	/**
	 * The JavaFx scene
	 */
	private Scene scene;

	/**
	 * The d3 wrapper
	 */
	protected D3 d3;

	/**
	 * Controls the browser
	 */
	protected WebEngine webEngine;
	
	private JavaFxD3Browser browser;

	private boolean showPoints = true;

	private final Stack<Coords> points = new Stack<>();

	private Selection svg;

	private Selection path;

	private Line line;

	protected InterpolationMode mode = InterpolationMode.LINEAR;

	protected int widthSVG = 1200;
	protected int heightSVG = 800;

	protected double tension;
	
	
	
	//#end region

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

		//set state title
		stage.setTitle("javafx-d3 line demo");

		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {
			System.out.println("Initial loading of browser is finished");

			//do some d3 stuff
			createD3Example();
			
			//--------------------------------------------------
			
			System.out.println(
					"SVG:\n\n" + 
					getSvg()
					);
			
			// SaveHelper saveHelper = new SaveHelper();
			// saveHelper.saveSvg(svg.html());
			

		};

		//create browser
		boolean debugMode = false;
		browser = new JavaFxD3Browser(postLoadingHook, debugMode);

		//create the scene
		scene = new Scene(browser, 750, 500, Color.web("#666970"));
		stage.setScene(scene);
		stage.show();	
	}

	private void createD3Example() {

		System.out.println("createD3Example");

		d3 = browser.getD3();
		webEngine = d3.getWebEngine();
		
		// create initial d3 content

		DatumFunction<Double> xAccessor = CustomCoords.xAccessor(webEngine);
		DatumFunction<Double> yAcccessor = CustomCoords.yAccessor(webEngine);
		DatumFunction<Boolean> isDefinedAccessor = CustomCoords.definedAccessor(webEngine);
		line = d3.svg().line().x(xAccessor).y(yAcccessor).defined(isDefinedAccessor);

		svg = d3.select("svg")
				.attr("width", widthSVG)
				.attr("height", heightSVG)
				.append("g");

		String cssClassName = "LineDemo";
		path = svg.append("path").classed(cssClassName, true);
		path
			.attr("fill","none")
			.attr("stroke","red")
			.attr("stroke-width","5")
			.attr("stroke-linecap","square") // "butt", "round", "square"
			.attr("stroke-dasharray","15,10");
		
		addPoint(true);
		addPoint(true);
		addPoint(true);
		addPoint(true);
		
		d3.select("body").append("p").text("Agodemar :: Hi there!");

	}

	
	protected void addPoint(boolean defined) {

		System.out.println("Adding point");

		Random random = new Random();
		double x = random.nextInt(widthSVG/2);
		double y = random.nextInt(heightSVG/2);
		CustomCoords coords = new CustomCoords(webEngine, x, y, defined);
		points.push(coords);

		updateD3Content();
	}

	/**
	 * 
	 */
	public void updateD3Content() {

		System.out.println("Updating content");

		mode = InterpolationMode.BASIS;
		line = line.interpolate(mode);
		System.out.println("Interpolation mode: " + line.interpolate());

		tension = 2.0;
		line = line.tension(tension);
		System.out.println("tension: " + line.tension());

		List<Coords> coordsList = new ArrayList<>(points);

		// Double[] values = new Double[]{20.0,20.0};

		String coordinates = line.generate(coordsList);
		System.out.println("coordinates: " + coordinates);

		path.attr("d", coordinates);

		ArrayList<Coords> data;
		if (showPoints) {
			data = new ArrayList<>(points);
		} else {
			data = new ArrayList<>();
		}

		UpdateSelection updateSelection = getSvg().selectAll("circle").data(data);

		DatumFunction<Double> cxFunction = new CxDatumFunction(webEngine);

		DatumFunction<Double> cyFunction = new CyDatumFunction(webEngine);

		EnteringSelection enter = updateSelection.enter();
		if (enter != null) {
			Selection result = 
				enter
					.append("circle")
					.attr("cx", cxFunction)
					.attr("cy", cyFunction)
					.attr("r", 5); // radius
						
			//Inspector.inspect(result);
			updateSelection.exit().remove();
		}

	}
	
	/**
	 * @return
	 */
	public Selection getSvg() {
		Selection svg = d3.select("#svg");
		return svg;
	}

	
	//#end region

	
}
