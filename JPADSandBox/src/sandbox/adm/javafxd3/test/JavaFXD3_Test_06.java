package sandbox.adm.javafxd3.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
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
	final int height = 400 - margin.top - margin.bottom;

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
//		System.out.println("D3 version " + d3.version());
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
		
		initialize();
		
		
	}

	private void initialize() {

		LinearScale x = d3.scale() //
				.linear() //
				.domain(-width / 2, width / 2) //
				.range(0.0, width);

		LinearScale y = d3.scale() //
				.linear() //
				.domain(-height / 2, height / 2) //
				.range(height, 0.0);

		// set the x axis
		xAxis = d3.svg() //
				.axis() //
				.scale(x) //
				.orient(Orientation.BOTTOM) //
				.tickSize(-height);

		// set the y axis
		yAxis = d3.svg() //
				.axis() //
				.scale(y) //
				.orient(Orientation.LEFT) //
				.ticks(5) //
				.tickSize(-width);

		Selection selection = d3.select("#root");

		// create info text boxes
		scaleLabel = selection.append("div") //
				.text("scale:");

		translateLabel = selection //
				.append("div") //
				.text("translate:");

		//.x(x) //
		//.y(y) //

		svg = d3.select("#svg") //
				.attr("width", width + margin.left + margin.right) //
				.attr("height", height + margin.top + margin.bottom) //
				.append("g") //
				.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

		// create zoom behavior
		Zoom zoom = d3.behavior() //
				.zoom() //	
				.x(x)
				.y(y)
				.on(ZoomEventType.ZOOM, new ZoomDatumFunction(d3, scaleLabel, translateLabel, svg, xAxis, yAxis));

		svg.call(zoom);

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

			// System.out.println("SVG:\n" + getStringFromNode(svgNode));

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