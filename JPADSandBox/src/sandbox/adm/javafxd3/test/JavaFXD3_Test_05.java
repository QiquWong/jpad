package sandbox.adm.javafxd3.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import org.treez.javafxd3.d3.D3;
import org.treez.javafxd3.d3.coords.Coords;
import org.treez.javafxd3.d3.core.Selection;
import org.treez.javafxd3.d3.scales.LinearScale;
import org.treez.javafxd3.d3.svg.Axis;
import org.treez.javafxd3.d3.svg.Axis.Orientation;
import org.treez.javafxd3.d3.svg.InterpolationMode;
import org.treez.javafxd3.d3.svg.Line;
import org.treez.javafxd3.d3.wrapper.Inspector;
import org.treez.javafxd3.javafx.JavaFxD3Browser;
import org.treez.javafxd3.javafx.SaveHelper;
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
import javafx.stage.Stage;
import writers.JPADStaticWriteUtils;

/**
 * Demonstrates how d3.js can be used with a JavaFx WebView
 *
 */
public class JavaFXD3_Test_05 extends Application {

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
				// System.out.println(stringifiedDoc);

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

					String outputFilePath = outputFolderPath + "test3.svg";
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



		// create initial d3 content

		// data that you want to plot, I"ve used separate arrays for x and y values
		double[] xData = {5, 10, 25, 32, 40, 40, 15, 7};
		double[] yData = {3, 17, 4, 10, 6, -20, -20.0, 0};

		// size and margins for the chart

		double totalWidth = 550;
		double totalHeight = 550;

		double marginLeft = 60;
		double marginRight = 15;

		double marginTop = 20;
		double marginBottom = 60;

		double width = totalWidth - marginLeft - marginRight;
		double height = totalHeight - marginTop - marginBottom;

		// x and y scales, I've used linear here but there are other options
		// the scales translate data values to pixel values for you
		double xMin = 0;
		double xMax = 50;
		LinearScale x = d3.scale().linear() //
		          .domain(new double[]{xMin, xMax})  // the range of the values to plot
		          .range(new double[]{0, width});        // the pixel range of the x-axis

		double yMin = -25;
		double yMax = 25;
		LinearScale y = d3.scale().linear() //
		          .domain(new double[]{yMin, yMax}) //
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
		      .attr("cy", new YAxisDatumFunction(webEngine, y, yData) ) // translate y value to a pixel
		      .attr("cx", new XAxisDatumFunction(webEngine, x, xData)) // translate x value
		      .attr("r", 5) // radius of circle
		      .style("opacity", 1.0); // opacity of circle


		// Line, the path generator
		Line line;

		InterpolationMode mode = InterpolationMode.LINEAR;

		// line = d3.svg().line().x(xAccessor).y(yAcccessor).defined(isDefinedAccessor);
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

		System.out.println("points:");
		points.stream()
			.forEach(p -> System.out.println(p.x()+", "+p.y()));

		mode = InterpolationMode.MONOTONE;
		line = line.interpolate(mode);
		System.out.println("Interpolation mode: " + line.interpolate());

		double tension = 0.1;
		line = line.tension(tension);
		System.out.println("tension: " + line.tension());

		List<Coords> coordsList = new ArrayList<>(points);

		System.out.println("coordsList:");
		coordsList.stream()
			.forEach(c -> System.out.println(c.x()+", "+c.y()));

		// Double[] values = new Double[]{20.0,20.0};

		String coordinates = line.generate(coordsList);
		System.out.println("coordinates: " + coordinates);

		pathLine.attr("d", coordinates);

//		// make a paragraph <p> in the html
//		d3.select("body").append("p").text("Agodemar :: Hi there!");

//		Inspector.inspect(
//				svg.select("g")
//				.attr("class", "main")
//				.getJsObject());

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

	public String getCssContent(URL cssUrl){
		try {
			FileInputStream  fis = new FileInputStream(cssUrl.getFile());
			String stringFromStream = CharStreams.toString(new InputStreamReader(fis, "UTF-8"));
			return stringFromStream;
		} catch (IOException e) {
			System.err.println("###############################");
			System.err.println("getCssContent !!!");
			e.printStackTrace();
			System.err.println("###############################");
			return null;
		}
	}

	public void injectStyleInSVG() throws TransformerException {

		String className = getClass().getName();
		String cssPath = className.replace(".", "/") + ".css";
		URL cssUrl = getClass().getClassLoader().getResource(cssPath);
		String stringCSSContent = getCssContent(cssUrl);

		stringCSSContent.replace("\n\r", "");
		stringCSSContent.replace("\n", "");

		Node svgNode = getSVGNode();

		if (browser != null) {
			Document document = browser.getWebEngine().getDocument();
			Element style = browser.getWebEngine().getDocument().createElementNS("http://www.w3.org/2000/svg", "style");

			style.appendChild(document.createTextNode(stringCSSContent));
			svgNode.appendChild(style);

			System.out.println("SVG:\n" + getStringFromNode(svgNode).replace("\n", ""));

		}


		//stringFromStream = "Pippo agodemar!";

		/*
		 * FIXME : try to get
		 *
<style>
.chart {

}

.main {

}

.main text {
    font: 10px sans-serif;
}

.axis line, .axis path {
    shape-rendering: crispEdges;
    stroke: black;
    fill: none;
    stroke-width:3;
    stroke-miterlimit:4;
    stroke-dasharray:none;
    stroke-opacity:1;
}

circle {
    fill: steelblue;
}
</style>
		* PROBLEM are the dots .main .axis etc
		*/

//		String command = "var svg  = document.getElementsByTagName('svg')[0];" //
//		+ "    var style  = document.createElementNS(\"http://www.w3.org/2000/svg\", 'style');" //
//		+ "    style.appendChild(document.createTextNode('"+stringFromStream+"'));" // stringFromStream
//		+ "    svg.appendChild(style);";
//
//		System.out.println("----------------\n" + command + "\n------------------");
		//webEngine.executeScript(command);

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
}
