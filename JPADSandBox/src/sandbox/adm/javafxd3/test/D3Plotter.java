package sandbox.adm.javafxd3.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.IntStream;

import javax.xml.transform.TransformerException;

import org.treez.javafxd3.d3.D3;
import org.treez.javafxd3.d3.coords.Coords;
import org.treez.javafxd3.d3.core.Selection;
import org.treez.javafxd3.d3.scales.LinearScale;
import org.treez.javafxd3.d3.svg.Axis;
import org.treez.javafxd3.d3.svg.InterpolationMode;
import org.treez.javafxd3.d3.svg.Line;
import org.treez.javafxd3.d3.wrapper.JavaScriptObject;
import org.treez.javafxd3.d3.svg.Axis.Orientation;
import org.treez.javafxd3.javafx.JavaFxD3Browser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.io.CharStreams;

import javafx.scene.web.WebEngine;

public class D3Plotter {

	// The d3 wrapper
	protected D3 d3;

	// Controls the browser
	protected WebEngine webEngine;

	private JavaFxD3Browser browser;

	private Selection svg;

	private Axis xAxis;
	private Axis yAxis;

	// set margins
	final Margin margin = new Margin(20, 20, 30, 40);

	final int widthDefault = 700 - margin.left - margin.right;
	final int heightDefault = 500 - margin.top - margin.bottom;

	private int widthSVG;
	private int heightSVG;

	private Selection scaleLabel;
	private Selection translateLabel;

	private String colorFillChart = "#c0d2dd";
	private String colorStrokeChart = "black";

	public class Margin {
		public final int top;
		public final int right;
		public final int bottom;
		public final int left;
		/**
		 * Constructor
		 *
		 * @param top
		 * @param right
		 * @param bottom
		 * @param left
		 */
		public Margin(final int top, final int right, final int bottom,
				final int left) {
			super();
			this.top = top;
			this.right = right;
			this.bottom = bottom;
			this.left = left;
		}
	}

	/*
	 *  Constructor
	 */
	public D3Plotter(int wSVG, int hSVG) {
		this.widthSVG = wSVG;
		this.heightSVG = hSVG;
	}


	/*
	 * called in Runnable object
	 */
	public void createD3Content() {

		System.out.println("D3Plotter :: createD3Content");

//		int widthSVG = 1200;
//		int heightSVG = 800;

		d3 = browser.getD3();
//		System.out.println("D3 version " + d3.version());
		webEngine = d3.getWebEngine();

//		injectStuffInHeadNode();
//		injectMathJax();

		// apply CSS
		loadCssForThisClass();

		svg = d3.select("svg")
				.attr("width", widthSVG)
				.attr("height", heightSVG);

		// Inject basic style attributes into svg node
		try {

			injectStyleInSVG();

		} catch (TransformerException e) {
			e.printStackTrace();
		}


		double xMin = 0;
		double xMax = 50;
		double yMin = -25;
		double yMax = 25;

		LinearScale x = d3.scale() //
				.linear() //
				.domain(xMin, xMax) //
				.range(0.0, widthDefault);

		LinearScale y = d3.scale() //
				.linear() //
				.domain(yMin, yMax) //
				.range(heightDefault, 0.0);

		// set the x axis
		xAxis = d3.svg() //
				.axis() //
				.scale(x) //
				.orient(Orientation.BOTTOM) //
				.tickSize(-heightDefault)
				// .outerTickSize(10) // agodemar
				.tickPadding(12) // agodemar
				;

		// set the y axis
		yAxis = d3.svg() //
				.axis() //
				.scale(y) //
				.orient(Orientation.LEFT) //
				.ticks(5) //
				.tickSize(-widthDefault)
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
				.attr("width", widthDefault + margin.left + margin.right) //
				.attr("height", heightDefault + margin.top + margin.bottom) //
				.append("g") //
				.attr("transform", "translate(" + margin.left + "," + margin.top + ")")
				;

//		// create zoom behavior
//		Zoom zoom = d3.behavior() //
//				.zoom() //
//				.x(x)
//				.y(y)
//				.on(ZoomEventType.ZOOM, new ZoomDatumFunction(d3, scaleLabel, translateLabel, svg, xAxis, yAxis));
//
//		svg.call(zoom);

		svg.append("rect") //
				.attr("width", widthDefault) //
				.attr("height", heightDefault)
				;

		svg.append("g") //
				.attr("class", "x axis") //
				.attr("transform", "translate(0," + heightDefault + ")") //
				.call(xAxis)
				;

		svg.append("g") //
				.attr("class", "y axis") //
				.call(yAxis);

		// agodemar

		// data that you want to plot, I"ve used separate arrays for x and y values
		double[] xData = {5, 10, 25, 32, 40, 40, 15, 7};
		double[] yData = {3, 17, 4, 10, 6, -20, -20.0, 0};

		// the chart object, includes all margins
		Selection chart = d3.select("svg") //
			.attr("width", widthDefault + margin.right + margin.left) //
			.attr("height", heightDefault + margin.top + margin.bottom) //
			.attr("class", "chart")
			.attr("fill", colorFillChart)
			.attr("stroke", colorStrokeChart)
			;

		// the main object where the chart and axis will be drawn
		Selection main = chart.append("g") //
			.attr("transform", "translate(" + margin.left + "," + margin.top + ")") //
			.attr("width", widthDefault) //
			.attr("height", heightDefault) //
			.attr("class", "main")
			;

		// draw the graph object
		Selection g = main.append("svg:g");

		g.selectAll("scatter-dots")
		  .data(yData)  // using the values in the ydata array
		  .enter().append("svg:circle")  // create a new circle for each value
		      .attr("cy", new YAxisDatumFunction(webEngine, y, yData) ) // translate y value to a pixel
		      .attr("cx", new XAxisDatumFunction(webEngine, x, xData)) // translate x value
		      .attr("r", 5) // radius of circle
		      .attr("fill", "darkgreen")
		      .style("opacity", 1.0); // opacity of circle


		// Line, the path generator
		Line line;

		InterpolationMode mode = InterpolationMode.LINEAR;

		line = d3.svg().line()
				.x(new XAxisDatumFunction(webEngine, x, xData))
				.y(new YAxisDatumFunction(webEngine, y, yData))
				;

		Selection g2 = g.append("svg:g")
				.classed("Pippo-line-group", true);

		String cssClassName = "Agodemar-Test-Line";
		Selection pathLine = g2.append("path").classed(cssClassName, true);
		pathLine
			.attr("fill","none")
			.attr("stroke","red")
			.attr("stroke-width","5")
			.attr("stroke-linecap","square") // "butt", "round", "square"
			.attr("stroke-dasharray","15,10")
			;

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
			.attr("fill", "gray")
			;

		// ###########################################################à

//		Selection symbolSelection = xySelection //
//				.append("g") //
//				.attr("id", "symbols") //
//				.attr("class", "symbols");
//
//		Selection symbols = symbolSelection
//				.selectAll("path") //
//				.data(dataArray) //
//				.enter() //
//				.append("path") //
//				.attr("transform", new AxisTransformPointDatumFunction(xScale, yScale)) //
//				//.attrExpression("transform", "function(d, i) { return 'translate(' + d[0] + ',' + d[1] + ')'; }") //
//				.attr("d", symbolDString) //
//				.attr("style", symbolStyle);



	}


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

	private void loadCss(URL cssUrl){

		String command = "var head  = document.getElementsByTagName('head')[0];" //
		+ "    var link  = document.createElement('link');" //
		+ "    link.rel  = 'stylesheet';" //
		+ "    link.type = 'text/css';" //
		+ "    link.href = '"+ cssUrl + "';" //
		+ "    link.media = 'all';" //
		+ "    head.appendChild(link);";
		webEngine.executeScript(command);
	}

	private void injectStyleInSVG() throws TransformerException {

		String className = getClass().getName();
//		System.out.println("injectStyleInSVG :: class name: " + className);
		String cssPath = className.replace(".", "/") + ".css";
//		System.out.println("injectStyleInSVG :: css path: " + cssPath);

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

	private String getFileContent(URL url){
		try {
			// System.out.println("CSS url: " + url);
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

	public JavaFxD3Browser getBrowser(Runnable postLoadingHook, boolean debugMode) {
		this.browser = new JavaFxD3Browser(postLoadingHook, debugMode);
		return this.browser;
	}
}
