package sandbox.adm.javafxd3.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.IntStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.treez.javafxd3.d3.D3;
import org.treez.javafxd3.d3.behaviour.Drag;
import org.treez.javafxd3.d3.behaviour.Drag.DragEventType;
import org.treez.javafxd3.d3.behaviour.Zoom;
import org.treez.javafxd3.d3.behaviour.Zoom.ZoomEventType;
import org.treez.javafxd3.d3.coords.Coords;
import org.treez.javafxd3.d3.core.Selection;
import org.treez.javafxd3.d3.scales.LinearScale;
import org.treez.javafxd3.d3.svg.Axis;
import org.treez.javafxd3.d3.svg.InterpolationMode;
import org.treez.javafxd3.d3.svg.Line;
import org.treez.javafxd3.d3.svg.Axis.Orientation;
import org.treez.javafxd3.javafx.JavaFxD3Browser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.io.CharStreams;

import configuration.MyConfiguration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import writers.JPADStaticWriteUtils;

/**
 * Demonstrates how d3.js can be used with a JavaFx WebView
 *
 */
public class JavaFXD3_Test_06 extends Application {

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

	private Axis xAxis;
	private Axis yAxis;

	// set margins
	final Margin margin = new Margin(20, 20, 30, 40);
	final int width = 700 - margin.left - margin.right;
	final int height = 500 - margin.top - margin.bottom;

	private Selection scaleLabel;
	private Selection translateLabel;	
	
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
		stage.setTitle("treez/javafx-d3 XXX demo");

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
				//System.out.println(stringifiedDoc);

				NodeList nodes = doc.getElementsByTagName("svg");

				System.out.println("Svg nodes, length: " + nodes.getLength());

				if ( nodes.getLength() > 0 ) {
					Node svgNode = nodes.item(0);
					String stringifiedSVG = getStringFromNode(svgNode);

//					// File chooser
//					SaveHelper saveHelper = new SaveHelper();
//					saveHelper.saveSvg(stringifiedSVG);

					String outputFilePath = outputFolderPath + "test4.svg";
					File file = new File(outputFilePath);
					if (file != null) {
						try {
							PrintWriter out = new PrintWriter(file);
							out.print(
									stringifiedSVG // stringifiedDoc //
									);
							out.close();
						} catch (FileNotFoundException e) {
							//
						}
					}
				}

				String outputFilePath = outputFolderPath + "test4.html";
				File file = new File(outputFilePath);
				if (file != null) {
					try {
						PrintWriter out = new PrintWriter(file);
						out.print(
								stringifiedDoc.replace("&lt;", "<") //
								);
						out.close();
					} catch (FileNotFoundException e) {
						//
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
		scene = new Scene(browser, 700, 600, Color.web("#666970"));
		stage.setScene(scene);

//		final WebView webView = new WebView();
//	    webView.getEngine().load("http://www.mathjax.org/demos/");
//	    System.getProperties().list(System.out);
//	    stage.setScene(new Scene(webView));
		
		
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
//		System.out.println("D3 version " + d3.version());
		webEngine = d3.getWebEngine();

		injectStuffInHeadNode();
		injectMathJax();
		
		// apply CSS
		loadCssForThisClass();

		svg = d3.select("svg")
				.attr("width", widthSVG)
				.attr("height", heightSVG);

		try {

			injectStyleInSVG();

		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		initialize();
		
		
	}

	private void initialize() {

		double xMin = 0;
		double xMax = 50;
		double yMin = -25;
		double yMax = 25;
		
		LinearScale x = d3.scale() //
				.linear() //
				.domain(xMin, xMax) //
				.range(0.0, width);

		LinearScale y = d3.scale() //
				.linear() //
				.domain(yMin, yMax) //
				.range(height, 0.0);

		// set the x axis
		xAxis = d3.svg() //
				.axis() //
				.scale(x) //
				.orient(Orientation.BOTTOM) //
				.tickSize(-height)
				// .outerTickSize(10) // agodemar
				.tickPadding(12) // agodemar
				;

		// set the y axis
		yAxis = d3.svg() //
				.axis() //
				.scale(y) //
				.orient(Orientation.LEFT) //
				.ticks(5) //
				.tickSize(-width)
				// .outerTickSize(10) // agodemar
				.tickPadding(5) // agodemar
				;

		Selection selection = d3.select("#root");

		// create info text boxes
		scaleLabel = selection.append("div") //
				.text("scale:");

		translateLabel = selection //
				.append("div") //
				.text("translate:");

		//.x(x) //
		//.y(y) //

		
//		//Double[] data = Arrays.range(16).map(callback);
//		List<Coords> coordsList = new ArrayList<>();
//		for(int index=0;index<16;index++){
//			Coords coords = new Coords(webEngine, SQUARE_WIDTH / 2, SQUARE_HEIGHT / 2);
//			coordsList.add(coords);
//		}
		
		svg = d3.select("#svg") //
				.attr("width", width + margin.left + margin.right) //
				.attr("height", height + margin.top + margin.bottom) //
				.append("g") //
				.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
		
//		// create zoom behavior
//		Zoom zoom = d3.behavior() //
//				.zoom() //	
//				.x(x)
//				.y(y)
//				.on(ZoomEventType.ZOOM, new ZoomDatumFunction(d3, scaleLabel, translateLabel, svg, xAxis, yAxis));
//
//		svg.call(zoom);

		svg.append("rect") //
				.attr("width", width) //
				.attr("height", height);

		svg.append("g") //
				.attr("class", "x axis") //
				.attr("transform", "translate(0," + height + ")") //
				.call(xAxis);

		svg.append("g") //
				.attr("class", "y axis") //
				.call(yAxis);

		// agodemar
		
		// data that you want to plot, I"ve used separate arrays for x and y values
		double[] xData = {5, 10, 25, 32, 40, 40, 15, 7};
		double[] yData = {3, 17, 4, 10, 6, -20, -20.0, 0};
		
		// the chart object, includes all margins
		Selection chart = d3.select("svg") //
			.attr("width", width + margin.right + margin.left) //
			.attr("height", height + margin.top + margin.bottom) //
			.attr("class", "chart");

		// the main object where the chart and axis will be drawn
		Selection main = chart.append("g") //
			.attr("transform", "translate(" + margin.left + "," + margin.top + ")") //
			.attr("width", width) //
			.attr("height", height) //
			.attr("class", "main");

		// draw the graph object
		Selection g = main.append("svg:g");

		g.selectAll("scatter-dots")
		  .data(yData)  // using the values in the ydata array
		  .enter().append("svg:circle")  // create a new circle for each value
		      .attr("cy", new YAxisDatumFunction(webEngine, y, yData) ) // translate y value to a pixel
		      .attr("cx", new XAxisDatumFunction(webEngine, x, xData)) // translate x value
		      .attr("r", 5) // radius of circle
		      .style("opacity", 1.0); // opacity of circle
		

		// Line, the path generator
		Line line;

		InterpolationMode mode = InterpolationMode.LINEAR;

		line = d3.svg().line()
				.x(new XAxisDatumFunction(webEngine, x, xData))
				.y(new YAxisDatumFunction(webEngine, y, yData));

		Selection g2 = g.append("svg:g")
				.classed("Pippo-line-group", true);

		String cssClassName = "Agodemar-Test-Line";
		Selection pathLine = g2.append("path").classed(cssClassName, true);
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
					points.push(new Coords(webEngine, xData[i], yData[i]))
					);

//		System.out.println("points:");
//		points.stream()
//			.forEach(p -> System.out.println(p.x()+", "+p.y()));

		mode = InterpolationMode.MONOTONE;
		line = line.interpolate(mode);
		System.out.println("Interpolation mode: " + line.interpolate());

		double tension = 0.1;
		line = line.tension(tension);
//		System.out.println("tension: " + line.tension());

		List<Coords> coordsList = new ArrayList<>(points);

//		System.out.println("coordsList:");
//		coordsList.stream()
//			.forEach(c -> System.out.println(c.x()+", "+c.y()));

		String coordinates = line.generate(coordsList);

//		System.out.println("coordinates: " + coordinates);

		pathLine.attr("d", coordinates)
			.attr("fill", "gray");
		
		//-----------------------------------------------
		Selection g3 = g.append("svg:g")
				.classed("MathJax-group", true);

		Selection latex = g3.append("text")
			     .attr("x", 100)
			     .attr("y", 60)
			     .attr("width", 400)
			     .attr("height", 200)
			     .attr("font-size", 14)
			     .text("\\( x = \\sum_{i \\in A} \\frac{f_{\\mathrm{GT}}^i}{2} \\)");
		
		g3.append("foreignObject")
	     .attr("x", 200)
	     .attr("y", 30)
	     .attr("width", 400)
	     .attr("height", 200)
	     .attr("font-size", 14)
	     .text("\\( y = f_i (x) \\)");
		
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

		// System.out.println("CSS:\n" + getCssContent(cssUrl) );
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

	public String getFileContent(URL url){
		try {
			FileInputStream  fis = new FileInputStream(url.getFile());
			String stringFromStream = CharStreams.toString(new InputStreamReader(fis, "UTF-8"));
			return stringFromStream;
		} catch (IOException e) {
			System.err.println("###############################");
			System.err.println("getFileContent !!!");
			e.printStackTrace();
			System.err.println("###############################");
			return null;
		}
	}

	public void injectStyleInSVG() throws TransformerException {

		String className = getClass().getName();
		String cssPath = className.replace(".", "/") + ".css";
		URL cssUrl = getClass().getClassLoader().getResource(cssPath);
		String stringCSSContent = getFileContent(cssUrl);

		stringCSSContent.replace("\n\r", "");
		stringCSSContent.replace("\n", "");

		Node svgNode = getSVGNode();

		if (browser != null) {
			Document document = browser.getWebEngine().getDocument();
			Element style = browser.getWebEngine().getDocument().createElementNS("http://www.w3.org/2000/svg", "style");
			style.appendChild(document.createTextNode(stringCSSContent));
			svgNode.appendChild(style);

			// System.out.println("SVG:\n" + getStringFromNode(svgNode));

		}
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

        // select the Saxon processor if you want to prevent wrong escape char conversions
        // TransformerFactory tf = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl",null);

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

	public Node getSVGNode() {
		// get the svg node
		if (browser != null) {
			NodeList nodes = browser.getWebEngine().getDocument().getElementsByTagName("svg");
			if ( nodes.getLength() > 0 )
				return nodes.item(0);
			else
				return null;
		}
		else
			return null;
	}
	
	private void injectMathJax() {
		System.out.println("Injecting MathJax/D3-related .js");
		
		String className = getClass().getName();
		String classPath = className.replace(".", "/");
		// System.out.println("==> " + classPath);
		int lastSlash = classPath.lastIndexOf("/");
		// System.out.println("==> " + lastSlash);
		if (lastSlash != -1) {
			List<String> jsList =
				    Arrays.asList(
				    		"svg_mathjax.js"
				    		/* 
				    		 * 
				    		 * you might have more than one .js
				    		 * and put it here, they'll be executed
				    		 * in sequence, e.g.
				    		 * 
				    		"MathJax.js", 				    		
				    		"TeX-AMS-MML_SVG.js",
				    		"jax.js",
				    		"mathjaxlabel.js"
				    		 */
				    		);
			jsList.stream()
				.forEach(
					s -> {
						String sPath = classPath.substring(0, lastSlash) + "/" + s;
						System.out.println(sPath);
						URL sUrl = getClass().getClassLoader().getResource(sPath);
						try {
							FileInputStream  fis = new FileInputStream(sUrl.getFile());
							String mathjaxContent = CharStreams.toString(new InputStreamReader(fis, "UTF-8"));
							
							// !!!
							webEngine.executeScript(mathjaxContent);
							
						} catch (IOException e) {
							System.err.println("###############################");
							System.err.println("injectMathJax !!!");
							e.printStackTrace();
							System.err.println("###############################");
						}
					});
		} 

	}// injectMathJax
	
	private void injectStuffInHeadNode() {
		
		System.out.println("injectStuffInHeadNode");
		
		// get the head node
		if (browser != null) {
			
			Document document = browser.getWebEngine().getDocument();
			NodeList nodes = document.getElementsByTagName("head");
			Element head;
			if ( nodes.getLength() == 0 ) {
				head = document.createElement("head");
				System.out.println("Created element <head/>");
			}
			else {
				head = (Element) nodes.item(0);
				System.out.println("Element <head/> found");
			}
			
			Element script = document.createElement("script");
			script.setAttribute("type", "text/javascript");
			script.setAttribute("src", "http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_SVG");
			script.appendChild(
					document.createTextNode(" "));
			head.appendChild(script);

			// Embedding MathJax into SVG, see here
			// http://www.embeddedrelated.com/showarticle/599.php
			
			Element script2 = document.createElement("script");
			script2.setAttribute("type", "text/javascript");
			// script2.setAttribute("src", "svg_mathjax.js");
			
			String className = getClass().getName();
			String classPath = className.replace(".", "/");
			// System.out.println("==> " + classPath);
			int lastSlash = classPath.lastIndexOf("/");
			// System.out.println("==> " + lastSlash);
			if (lastSlash != -1) {
				String jsPath = classPath.substring(0, lastSlash) + "/" + "svg_mathjax.js";
				System.out.println(jsPath);
				URL jsUrl = getClass().getClassLoader().getResource(jsPath);
				String jsContent = getFileContent(jsUrl);
				jsContent.replace("\n\r", "");
				jsContent.replace("\n", "");
				jsContent.replace("&lt;", "<");
				
				script2.appendChild(
						document.createTextNode(jsContent));
			}
			head.appendChild(script2);

			Element script3 = document.createElement("script");
			script3.setAttribute("type", "text/javascript");
			script3.appendChild(
					document.createTextNode(
							"new Svg_MathJax().install();"
							));
			head.appendChild(script3);
			
		}
	}

	
}