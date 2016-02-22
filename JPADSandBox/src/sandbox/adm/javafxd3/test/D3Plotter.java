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

import org.treez.core.atom.graphics.length.Length;
import org.treez.javafxd3.d3.D3;
import org.treez.javafxd3.d3.coords.Coords;
import org.treez.javafxd3.d3.core.Selection;
import org.treez.javafxd3.d3.functions.AxisScaleFirstDatumFunction;
import org.treez.javafxd3.d3.functions.AxisScaleSecondDatumFunction;
import org.treez.javafxd3.d3.functions.AxisTransformPointDatumFunction;
import org.treez.javafxd3.d3.scales.LinearScale;
import org.treez.javafxd3.d3.scales.QuantitativeScale;
import org.treez.javafxd3.d3.svg.Area;
import org.treez.javafxd3.d3.svg.Axis;
import org.treez.javafxd3.d3.svg.InterpolationMode;
import org.treez.javafxd3.d3.svg.Line;
import org.treez.javafxd3.d3.svg.Symbol;
import org.treez.javafxd3.d3.svg.SymbolType;
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

	private Selection svgSelection;

	// set margins
	final Margin margin = new Margin(20, 20, 10, 40); // t r b l

	final int widthGraph = 700 - margin.left - margin.right;
	final int heightGraph = 500 - margin.top - margin.bottom;

	private int widthPageSVG;
	private int heightPageSVG;

	private Selection scaleLabel;
	private Selection translateLabel;

	private String graphBackground = "lightblue";

	private final double xtickPadding = 12.0;
	private final double ytickPadding = 5.0;

	private final int symbolSquareSize = 64;
	private String symbolStyle = "fill:red; stroke:blue; stroke-width:2";

	private String lineStyle = "fill:none; stroke:red; stroke-width:2";

	private String areaStyle = "fill:green;";


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
		this.widthPageSVG = wSVG;
		this.heightPageSVG = hSVG;
	}

	public D3Plotter() {
		this.widthPageSVG = widthGraph;
		this.heightPageSVG = heightGraph;
	}

	/*
	 * called in Runnable object
	 */
	public void createD3Content() {


		Double[][] dataArray = { { 1.0, 0.0 }, { 20.0, 15.5 }, { 50.0, 10.0 } };

		// data that you want to plot, I"ve used separate arrays for x and y values
		double[] xData1 = {5, 10, 25, 32, 40, 40, 15, 7};
		double[] yData1 = {3, 17, 4, 10, 6, -20, -20.0, 0};

		double xMin = 0;
		double xMax = 50;
		double yMin = -30.0;
		double yMax = 30.0;

		System.out.println("D3Plotter :: createD3Content");

//		int widthSVG = 1200;
//		int heightSVG = 800;

		d3 = browser.getD3();
//		System.out.println("D3 version " + d3.version());
		webEngine = d3.getWebEngine();

//		injectStuffInHeadNode();
//		injectMathJax();

		// apply CSS
//		loadCssForThisClass();

		// svg
		svgSelection = d3.select("svg")
				.attr("width", widthPageSVG)
				.attr("height", heightPageSVG);

		//page
		Selection pageSelection = svgSelection //
				.append("g") //
				.attr("id", "page")
				.attr("width", widthPageSVG) //
				.attr("height", widthPageSVG);

		// Inject basic style attributes into svg node
//		try {
//
//			injectStyleInSVG();
//
//		} catch (TransformerException e) {
//			e.printStackTrace();
//		}

		//graph
		Selection graphSelection = pageSelection //
				.append("g") //
				.attr("id", "graph") //
				.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

		Selection graphRectSelection = graphSelection //
				.append("rect") //
				.attr("width", widthGraph) //
				.attr("height", heightGraph) //
				.attr("fill", graphBackground);

		//x axis
		Selection xAxisSelection = graphSelection //
				.append("g") //
				.attr("id", "" + "xAxis") //
				.attr("class", "axis") //
				.attr("transform", "translate(0," + heightGraph + ")");

		// QuantitativeScale<?> xScale;

		LinearScale xScale = d3.scale() //
				.linear() //
				.domain(xMin, xMax) //
				.range(0.0, widthGraph);

		// set the x axis
		org.treez.javafxd3.d3.svg.Axis xAxis = d3.svg() //
				.axis() //
				.scale(xScale) //
				.orient(org.treez.javafxd3.d3.svg.Axis.Orientation.BOTTOM) //
				.tickSize(-heightGraph)
				// .outerTickSize(10) // agodemar
				.tickPadding(xtickPadding) // agodemar
				;

		xAxis.apply(xAxisSelection);

		xAxisSelection //
				.selectAll("path, line") //
				.style("fill", "none") //
				.style("stroke", "#000")
				.style("stroke-width", "1.2px") //
				.style("shape-rendering", "geometricPrecision"); // "crispEdges"

//		if (logXScale) {
//			//major ticks
//			xAxisSelection //
//			.selectAll(".tick:nth-child(1)") //
//			.classed("major", true);
//
//			xAxisSelection //
//			.selectAll(".tick:nth-child(9n+1)") //
//			.classed("major", true);
//
//			Selection majorTickLines = xAxisSelection //
//					.selectAll(".major") //
//					.selectAll("line");
//
//			Selection minorTickLines = xAxisSelection //
//					.selectAll(".tick:not(.major)") //
//					.selectAll("line");
//
//			majorTickLines //
//			.style("stroke", "blue") //
//			.attr("y2", "+" + 20);
//
//			minorTickLines //
//			.style("stroke", "red")
//			.attr("y2", "+" + 10);
//
//		}

		//y axis
		Selection yAxisSelection = graphSelection //
				.append("g") //
				.attr("id", "" + "yAxis") //
				.attr("class", "axis");

		LinearScale yScale = d3 //
				.scale() //
				.linear() //
				.domain(yMin, yMax) //
				.range(heightGraph, 0.0);

		org.treez.javafxd3.d3.svg.Axis yAxis = d3 //
				.svg() //
				.axis() //
				.scale(yScale)
				.orient(org.treez.javafxd3.d3.svg.Axis.Orientation.LEFT)
				.tickPadding(ytickPadding)
				.tickSize(-widthGraph)
				// .outerTickSize(10) // agodemar
				// .tickPadding(5) // agodemar
				;

		yAxis.apply(yAxisSelection);

		yAxisSelection //
			.selectAll("path, line") //
			.style("fill", "none") //
			.style("stroke", "#000")
			// .style("stroke-dasharray","15,10")
			.style("stroke-width", "1.2px") //
			.style("shape-rendering", "geometricPrecision"); // "crispEdges"


		//xy plot
		Selection xySelection = graphSelection //
			.append("g") //
			.attr("id", "xy") //
			.attr("class", "xy");


		//plot line
		org.treez.javafxd3.d3.svg.Line linePathGenerator = d3 //
			.svg()//
			.line()
			.x(new AxisScaleFirstDatumFunction(xScale))
			.y(new AxisScaleSecondDatumFunction(yScale));

		Selection line = xySelection //
				.append("path") //
				.attr("id", "line") //
				.attr("d", linePathGenerator.generate(dataArray))
				.attr("class", "line")
				.attr("style", lineStyle);


		//plot area beneath line
		double yMin1 = yScale.apply(0.0).asDouble();
		Area areaPathGenerator = d3 //
			.svg() //
			.area() //
			.x(new AxisScaleFirstDatumFunction(xScale))
			.y0(yMin1)
			.y1(new AxisScaleSecondDatumFunction(yScale));
		String areaPath = areaPathGenerator.generate(dataArray);

		@SuppressWarnings("unused")
		Selection area = xySelection //
			.append("path") //
			.attr("id", "area") //
			.attr("d", areaPath)
			.attr("class", "area")
			.attr("style", areaStyle)
			.attr("opacity", "0.5")
			;

		//plot symbols
		Symbol symbol = d3 //
				.svg() //
				.symbol();
		symbol = symbol //
				.size(symbolSquareSize) //
				.type(SymbolType.CIRCLE);

		String symbolDString = symbol.generate();

		Selection symbolSelection = xySelection //
				.append("g") //
				.attr("id", "symbols") //
				.attr("class", "symbols");

		@SuppressWarnings("unused")
		Selection symbols = symbolSelection
				.selectAll("path") //
				.data(dataArray) //
				.enter() //
				.append("path") //
				.attr("transform", new AxisTransformPointDatumFunction(xScale, yScale)) //
				//.attrExpression("transform", "function(d, i) { return 'translate(' + d[0] + ',' + d[1] + ')'; }") //
				.attr("d", symbolDString) //
				.attr("style", symbolStyle);

		//################################

		// draw the graph object
		Selection points1Selection = xySelection.append("svg:g");
		points1Selection.selectAll("scatter-dots")
		  .data(yData1)  // using the values in the ydata array
		  .enter().append("svg:circle")  // create a new circle for each value
		      .attr("cy", new YAxisDatumFunction(webEngine, yScale, yData1) ) // translate y value to a pixel
		      .attr("cx", new XAxisDatumFunction(webEngine, xScale, xData1)) // translate x value
		      .attr("r", 5) // radius of circle
		      .attr("fill", "darkgreen")
		      .style("opacity", 1.0); // opacity of circle

		// Line, the path generator
		Line line1 = d3.svg().line()
				.x(new XAxisDatumFunction(webEngine, xScale, xData1))
				.y(new YAxisDatumFunction(webEngine, yScale, yData1))
				;

		Selection line1Selection = xySelection.append("svg:g")
				.classed("Pippo-line-group", true);
		String cssClassName = "Agodemar-Test-Line";
		Selection pathLine = line1Selection
				.append("path")
				.classed(cssClassName, true)
				;
		pathLine
			.attr("fill","none")
			.attr("stroke","red")
			.attr("stroke-width","5")
			.attr("stroke-linecap","square") // "butt", "round", "square"
			.attr("stroke-dasharray","15,10")
			;

		final Stack<Coords> points = new Stack<>();

		IntStream.range(0, xData1.length)
			.forEach(i ->
					points.push(new Coords(webEngine, xData1[i], yData1[i]))
					);

//		System.out.println("points:");
//		points.stream()
//			.forEach(p -> System.out.println(p.x()+", "+p.y()));

		InterpolationMode mode1 = InterpolationMode.MONOTONE;
		line1 = line1.interpolate(mode1);
//		System.out.println("Interpolation mode: " + line.interpolate());

		double tension = 0.1;
		line1 = line1.tension(tension);
//		System.out.println("tension: " + line.tension());

		List<Coords> coordsList = new ArrayList<>(points);

//		System.out.println("coordsList:");
//		coordsList.stream()
//			.forEach(c -> System.out.println(c.x()+", "+c.y()));

		String coordinates = line1.generate(coordsList);

//		System.out.println("coordinates: " + coordinates);

		pathLine.attr("d", coordinates)
			.attr("fill", "gray")
			;

		// ###########################################################

		// ###########################################################
		// this is my playground ...



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
