package sandbox.adm.javafxd3.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

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
import com.github.javafxd3.d3.functions.DatumFunction;
import com.github.javafxd3.d3.scales.LinearScale;
import com.github.javafxd3.d3.svg.Line;
import com.github.javafxd3.d3.svg.Line.InterpolationMode;
import com.github.javafxd3.javafx.JavaFxD3Browser;

import configuration.MyConfiguration;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

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

		System.out.println("\nInitializing test class...");
		String rootOutputFolderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String outputFolderPath = JPADStaticWriteUtils.createNewFolder(rootOutputFolderPath + "Test_D3" + File.separator);
		System.out.println("Output ==> " + outputFolderPath);
		
		//set state title
		stage.setTitle("javafx-d3 line demo");

		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {
			System.out.println("Initial loading of browser is finished");

			//do some d3 stuff
			createD3Example();
			
			//--------------------------------------------------
			
			System.out.println(
					"\nSVG:\n" + 
					getSvg()
					);
			
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
					
					String outputFilePath = outputFolderPath + "test.svg";
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

	private void createD3Example() {

		System.out.println("createD3Example");

		d3 = browser.getD3();
		webEngine = d3.getWebEngine();
		
		
//		// get the contents from the webview
//		// http://stackoverflow.com/questions/14273450/get-the-contents-from-the-webview-using-javafx
//		webEngine.getLoadWorker().stateProperty().addListener(
//				new ChangeListener<State>() {
//
//					@Override
//					public void changed(ObservableValue ov, State oldState, State newState) {
//						if (newState == Worker.State.SUCCEEDED) {
//							Document doc = webEngine.getDocument();
//							try {
//								Transformer transformer = TransformerFactory.newInstance().newTransformer();
//								transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
//								transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//								transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//								transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//								transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
//
//								transformer.transform(new DOMSource(doc),
//										new StreamResult(new OutputStreamWriter(System.out, "UTF-8")));
//							} catch (Exception ex) {
//								ex.printStackTrace();
//							}
//						}
//					}
//				});		
		
		
		
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
		
		// make a paragrapf <p> in the html
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
