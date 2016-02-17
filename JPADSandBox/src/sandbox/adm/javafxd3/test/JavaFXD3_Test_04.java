package sandbox.adm.javafxd3.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.stream.IntStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.treez.javafxd3.javafx.SaveHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.github.javafxd3.d3.D3;
import com.github.javafxd3.d3.coords.Coords;
import com.github.javafxd3.d3.core.EnteringSelection;
import com.github.javafxd3.d3.core.Selection;
import com.github.javafxd3.d3.core.UpdateSelection;
import com.github.javafxd3.d3.core.Value;
import com.github.javafxd3.d3.functions.DatumFunction;
import com.github.javafxd3.d3.scales.IdentityScale;
import com.github.javafxd3.d3.scales.LinearScale;
import com.github.javafxd3.d3.scales.LogScale;
import com.github.javafxd3.d3.scales.OrdinalScale;
import com.github.javafxd3.d3.svg.Axis;
import com.github.javafxd3.d3.svg.Line;
import com.github.javafxd3.d3.svg.Axis.Orientation;
import com.github.javafxd3.d3.svg.Line.InterpolationMode;
import com.github.javafxd3.functionplot.FunctionPlot;
import com.github.javafxd3.functionplot.Options;
import com.github.javafxd3.javafx.JavaFxD3Browser;

import configuration.MyConfiguration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import writers.JPADStaticWriteUtils;

/**
 * Demonstrates how d3.js can be used with a JavaFx WebView
 *
 */
public class JavaFXD3_Test_04 extends Application {

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

	private Selection svg;

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

		//--------------------------------------------------
		System.out.println("\nInitializing test class...");
		String rootOutputFolderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String outputFolderPath = JPADStaticWriteUtils.createNewFolder(rootOutputFolderPath + "Test_D3" + File.separator);
		System.out.println("Output ==> " + outputFolderPath);
		
		//--------------------------------------------------
		//set state title
		stage.setTitle("javafx-d3 XXX demo");

		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {
			System.out.println("Initial loading of browser is finished");

			//do some d3 stuff
			createD3Example();
			
			//--------------------------------------------------
			
			try {
				
				// get the svg node
				
				Document doc = browser.getWebEngine().getDocument();

				String stringifiedDoc = getStringFromDoc(doc); 
				System.out.println(stringifiedDoc);
				
//				NodeList nodes = 			
//						MyXMLReaderUtils
//							.getXMLNodeListByPath(doc, "//HTML/BODY/DIV/svg");
				NodeList nodes = doc.getElementsByTagName("svg");

				System.out.println("Svg nodes, length: " + nodes.getLength());
				
				if ( nodes.getLength() > 0 ) {
					Node svgNode = nodes.item(0);
					String stringifiedSVG = getStringFromNode(svgNode);
					
//					// File chooser
//					SaveHelper saveHelper = new SaveHelper();
//					saveHelper.saveSvg(stringifiedSVG);
					
					String outputFilePath = outputFolderPath + "test2.svg";
					File file = new File(outputFilePath);
					if (file != null) {			
						try {
							PrintWriter out = new PrintWriter(file);
							out.print(stringifiedSVG);
							out.close();
						} catch (FileNotFoundException e) {
							// 
						}
					}					
				}
				
			} catch (TransformerException e) {
				e.printStackTrace();
			}

		};

		//create browser
		boolean debugMode = false;
		browser = new JavaFxD3Browser(postLoadingHook, debugMode);

		//create the scene
		scene = new Scene(browser, 750, 500, Color.web("#666970"));
		stage.setScene(scene);
		stage.show();
		
	}

	/*
	 * called in Runnable object
	 */
	private void createD3Example() {

		System.out.println("createD3Example");

		boolean showPoints = true;

		int widthSVG = 1200;
		int heightSVG = 800;

		d3 = browser.getD3();
		webEngine = d3.getWebEngine();
		loadCssForThisClass();
		
		svg = d3.select("svg")
				.attr("width", widthSVG)
				.attr("height", heightSVG)
				.append("g");

		// create initial d3 content

		// data that you want to plot, I"ve used separate arrays for x and y values
		double[] xData = {5, 10, 15, 20};
		double[] yData = {3, 17, 4, 6};

		// size and margins for the chart		
		
		double totalWidth = 500;
		double totalHeight = 500;
		
		double marginLeft = 60;
		double marginRight = 15;
		
		double marginTop = 20;
		double marginBottom = 60;
		
		double width = totalWidth - marginLeft - marginRight;
		double height = totalHeight - marginTop - marginBottom;
		
		// x and y scales, I've used linear here but there are other options
		// the scales translate data values to pixel values for you
		double xMax = 22;
		LinearScale x = d3.scale().linear() //
		          .domain(new double[]{0, xMax})  // the range of the values to plot
		          .range(new double[]{0, width});        // the pixel range of the x-axis

		double yMax = 22;
		LinearScale y = d3.scale().linear() //
		          .domain(new double[]{0, yMax}) //
		          .range(new double[]{height, 0});

		// the chart object, includes all margins
		Selection chart = d3.select("svg") //
		.attr("width", width + marginRight + marginLeft) //
		.attr("height", height + marginTop + marginBottom) //
		.attr("class", "chart");

		// the main object where the chart and axis will be drawn
		Selection main = chart.append("g") //
		.attr("transform", "translate(" + marginLeft + "," + marginTop + ")") //
		.attr("width", width) //
		.attr("height", height) //
		.attr("class", "main");   

		// draw the x axis
		Axis xAxis = d3.svg().axis().scale(x).orient(Orientation.BOTTOM);
		
		//xAxis.innerTickSize(10);
		
		
		main.append("g") //
		.attr("transform", "translate(0," + height + ")") //
		.attr("class", "main axis date").call(xAxis);

		// draw the y axis
		Axis yAxis = d3.svg().axis() //
		.scale(y) //
		.orient(Orientation.LEFT);

		main.append("g") //
		.attr("transform", "translate(0,0)") //
		.attr("class", "main axis date") //
		.call(yAxis);

		// draw the graph object
		Selection g = main.append("svg:g"); 

		g.selectAll("scatter-dots")
		  .data(yData)  // using the values in the ydata array
		  .enter().append("svg:circle")  // create a new circle for each value
		      .attr("cy", new YAxisDatumFunction(webEngine, y) ) // translate y value to a pixel
		      .attr("cx", new XAxisDatumFunction(webEngine, x, xData)) // translate x value
		      .attr("r", 5) // radius of circle
		      .style("opacity", 0.6); // opacity of circle


		
		
		
		
		Line line;
		InterpolationMode mode = InterpolationMode.LINEAR;
		DatumFunction<Double> xAccessor = CustomCoords.xAccessor(webEngine);
		DatumFunction<Double> yAcccessor = CustomCoords.yAccessor(webEngine);
		DatumFunction<Boolean> isDefinedAccessor = CustomCoords.definedAccessor(webEngine);
		line = d3.svg().line().x(xAccessor).y(yAcccessor).defined(isDefinedAccessor);

		String cssClassName = "Agodemar-Test-Line";
		Selection pathLine = svg.append("path").classed(cssClassName, true);
		pathLine
			.attr("fill","none")
			.attr("stroke","red")
			.attr("stroke-width","5")
			.attr("stroke-linecap","square") // "butt", "round", "square"
			.attr("stroke-dasharray","15,10");
		
		final Stack<Coords> points = new Stack<>();

//		double [] x = {50.0, 120.0, 400.0, 700};
//		double [] y = {100.0, 30.0, 20.0, 200};
		
		IntStream.range(0, xData.length)
			.forEach(i ->
					points.push(new CustomCoords(webEngine, xData[i], yData[i], true))
					);

//		System.out.println("Updating content");

		mode = InterpolationMode.BASIS;
		line = line.interpolate(mode);
		System.out.println("Interpolation mode: " + line.interpolate());

		double tension = 2.0;
		line = line.tension(tension);
		System.out.println("tension: " + line.tension());

		List<Coords> coordsList = new ArrayList<>(points);

		// Double[] values = new Double[]{20.0,20.0};

		String coordinates = line.generate(coordsList);
		System.out.println("coordinates: " + coordinates);

		pathLine.attr("d", coordinates);

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
		
		
//		// make a paragraph <p> in the html
//		d3.select("body").append("p").text("Agodemar :: Hi there!");

	}

	
	/**
	 * @return
	 */
	public Selection getSvg() {
		Selection svg = d3.select("#svg");
		return svg;
	}

	
	//#end region

	
	/**
	 * If a css file exists that has the same name as the java/class file and
	 * is located next to that file, the css file is loaded with this method. 
	 */
	private void loadCssForThisClass() {
		String className = getClass().getName();
		String cssPath = className.replace(".", "/") + ".css";		
		URL cssUrl = getClass().getClassLoader().getResource(cssPath);		
		loadCss(cssUrl);
	}
	public void loadCss(URL cssUrl){
		
		String command = "var head  = document.getElementsByTagName('head')[0];" //
		+ "    var link  = document.createElement('link');" //		
		+ "    link.rel  = 'stylesheet';" //
		+ "    link.type = 'text/css';" //
		+ "    link.href = '"+ cssUrl + "';" //
		+ "    link.media = 'all';" //
		+ "    head.appendChild(link);";		
		webEngine.executeScript(command);		
	}
	
	
	//-------------------------------------------------------------------------
	// Converting a org.w3c.dom.Document in Java to String using Transformer
	// http://stackoverflow.com/questions/22539158/converting-a-org-w3c-dom-document-in-java-to-string-using-transformer
	public static String getStringFromDoc(Document doc) throws TransformerException {

        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(domSource, result);
        writer.flush();
        return writer.toString();
    }	

	public static String getStringFromNode(Node node) throws TransformerException {

        DOMSource domSource = new DOMSource(node);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(domSource, result);
        writer.flush();
        return writer.toString();
    }	
	
	
}
