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
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
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
import org.treez.javafxd3.d3.coords.Coords;
import org.treez.javafxd3.d3.core.Selection;
import org.treez.javafxd3.d3.functions.AxisScaleFirstDatumFunction;
import org.treez.javafxd3.d3.functions.AxisScaleSecondDatumFunction;
import org.treez.javafxd3.d3.functions.AxisTransformPointDatumFunction;
import org.treez.javafxd3.d3.scales.LinearScale;
import org.treez.javafxd3.d3.svg.Area;
import org.treez.javafxd3.d3.svg.InterpolationMode;
import org.treez.javafxd3.d3.svg.Line;
import org.treez.javafxd3.d3.svg.Symbol;
import org.treez.javafxd3.d3.svg.SymbolType;
import org.treez.javafxd3.javafx.JavaFxD3Browser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.io.CharStreams;

import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import sandbox.adm.D3PlotterOptions;
import standaloneutils.MyArrayUtils;

public class D3Plotter {

	// The d3 wrapper
	protected D3 d3;

	// Controls the browser
	protected WebEngine webEngine;

	private JavaFxD3Browser browser;

	private Selection svgSelection;

	// set margins
	final private Margin margin;

	final int widthGraph;
	final int heightGraph;

	private int widthPageSVG;
	private int heightPageSVG;

	private Selection scaleLabel;
	private Selection translateLabel;

	private String graphBackground = "lightblue";

	private final double xtickPadding; // = 12.0;
	private final double ytickPadding; // = 5.0;

	private int symbolSize = 64;
	private String symbolStyle = "fill:red; stroke:blue; stroke-width:2";

	private String lineStyle = "fill:none; stroke:red; stroke-width:2";

	private String areaStyle = "fill:green;";

	private boolean showSymbols = true;
	private boolean plotArea = false;
	private boolean showLegend = true;

	final private List<Double[][]> listDataArray = new ArrayList<Double[][]>();


	public static class Margin {
		public int top;
		public int right;
		public int bottom;
		public int left;
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
	public D3Plotter(
			int wSVG, int hSVG, // svg dimensions
			int marginTop, int marginRight, int marginBottom, int marginLeft,  // plot margins, t r b l
			double xtickPadding, double ytickPadding,
			boolean showSymbols, boolean showLegend, boolean plotArea,
			Double[][] dataArray
			) {
		widthPageSVG = wSVG;
		heightPageSVG = hSVG;
		margin = new Margin(marginTop, marginRight, marginBottom, marginLeft);

		widthGraph = widthPageSVG - margin.left - margin.right;
		heightGraph = heightPageSVG - margin.top - margin.bottom;

		this.xtickPadding = xtickPadding;
		this.ytickPadding = ytickPadding;

		this.showSymbols = showSymbols;
		this.showLegend = showLegend;
		this.plotArea = plotArea;

		this.listDataArray.add(dataArray);
	}

	/*
	 *  Constructor
	 */
	public D3Plotter(
			D3PlotterOptions options,
			Double[][] dataArray
			) {
		widthPageSVG = options.getWidthPageSVG();
		heightPageSVG = options.getHeightPageSVG();
		margin = options.getMargin();

		widthGraph = options.getWidthGraph();
		heightGraph = options.getHeightGraph();

		this.symbolSize = options.getSymbolSize();
		this.symbolStyle = options.getSymbolStyle();

		this.lineStyle = options.getLineStyle();

		this.areaStyle = options.getAreaStyle();

		this.xtickPadding = options.getXtickPadding();
		this.ytickPadding = options.getYtickPadding();

		this.showSymbols = options.isShowSymbols();
		this.showLegend = options.isShowLegend();
		this.plotArea = options.isPlotArea();

		this.listDataArray.add(dataArray);
	}

//	public D3Plotter() {
//		this.widthPageSVG = widthGraph;
//		this.heightPageSVG = heightGraph;
//	}

	/*
	 * called in Runnable object
	 */
	public void createD3Content() {

		if (listDataArray.size() == 0)
			return;

		System.out.println("D3Plotter :: createD3Content");

		Double[][] dataArray = listDataArray.get(0);

		// TODO
		// see http://stackoverflow.com/questions/26050530/filling-a-multidimensional-array-using-a-stream
		// to populate a longer list of 2D arrays


//		System.out.println(dataArray.length);

		//--------------------------------------------------------
		// Find X- and Y- min/max values

//		List<List<Double>> dataList = MyArrayUtils.convert2DArrayToList(dataArray);
//		System.out.println(dataList);

//		System.out.println(MyArrayUtils.extractColumnOf2DArrayToList(dataArray, 0));

		DoubleSummaryStatistics summaryStatisticsX = MyArrayUtils.extractColumnOf2DArrayToList(dataArray, 0).stream()
				.mapToDouble(v -> v)
		        .summaryStatistics();

		double xMax = summaryStatisticsX.getMax();
		double xMin = summaryStatisticsX.getMin();

//		System.out.println(MyArrayUtils.extractColumnOf2DArrayToList(dataArray, 1));

		DoubleSummaryStatistics summaryStatisticsY = MyArrayUtils.extractColumnOf2DArrayToList(dataArray, 1).stream()
				.mapToDouble(v -> v)
		        .summaryStatistics();

		double yMax = summaryStatisticsY.getMax();
		double yMin = summaryStatisticsY.getMin();


		// Get the D3 object
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
				.style("font", "10px sans-serif")
				.style("shape-rendering", "geometricPrecision"); // "crispEdges" // "geometricPrecision"

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


		// X-axis Label
		svgSelection.append("text")
		    .attr("class", "x label")
		    .attr("text-anchor", "middle")
		    .attr("x", margin.left + widthGraph/2)
		    .attr("y", margin.top + heightGraph + 45) // NB: offset
		    .text("income per capita, inflation-adjusted (dollars)");

		// Y-Axis label
		svgSelection.append("text")
		    .attr("class", "y label")
		    .attr("text-anchor", "middle")
		    .attr("x", -margin.top - heightGraph/2)
		    .attr("y", margin.left/2 - 15) // NB: offset
		    .attr("dy", ".75em")
		    .attr("transform", "rotate(-90)")
		    .text("life expectancy (years)");

		// Title
		svgSelection.append("text")
	      .attr("class", "title")
	      .attr("text-anchor", "middle")
	      .attr("x", margin.left + widthGraph/2)
	      .attr("y", (margin.top / 2) - 0)  // NB: offset
	      .text("This is the title");


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


		if ( plotArea ) {
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
		}


		if (showSymbols) {
			//plot symbols
			Symbol symbol = d3 //
					.svg() //
					.symbol();
			symbol = symbol //
					.size(symbolSize) //
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
		}

		if (showLegend)
			putLegend();


		//################################
if (false) {
		// data that you want to plot, I've used separate arrays for x and y values
		double[] xData1 = {5, 10, 25, 32, 40, 40, 15, 7};
		double[] yData1 = {3, 17, 4, 10, 6, -20, -20.0, 0};

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
		Selection pathLine = line1Selection
				.append("path")
				.classed("Agodemar-Test-Line", true)
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
			.attr("fill-opacity", "0.2")
			;
}
		// ###########################################################

		// ###########################################################
		// this is my playground ...

//		injectAdditionalJavascripts();

	}

	private void putLegend() {

		System.out.println("D3Plotter :: putLegend");

		// TODO

		Selection legend = svgSelection.append("g")
				  .attr("class", "legend")
//				  .attr("x", widthGraph - 65)
//				  .attr("y", 25)
//				  .attr("height", 100)
//				  .attr("width", 100)
				.attr("transform", "translate(" + 0.9*widthGraph + "," + 0.1*heightGraph + ")")
				;

		String commandFill = getLegendCommandFill(
				new ColorLegendDatumFunction(
						webEngine, Arrays.asList("red", "blue", "green")));
		// System.out.println(commandFill);

//		String commandText = getLegendCommandText(
//				new TextLegendDatumFunction(
//						webEngine, Arrays.asList("Ago", "dem", "ar")));

		legend.selectAll("rect")
	      .data(new int[]{1, 2, 3}) // legend item counter
	      .enter()
	      .append("rect")
	      .attrExpression("y", "function(d, i){ return i *  20;}")
	      .attr("width", 10)
	      .attr("height", 10)
	      //.style("fill","green")
	      .evalForJsObject(commandFill) // .style("fill", new ColorLegendDatumFunction(webEngine))
	      ;

		legend.selectAll("text")
	      .data(new int[]{1, 2, 3}) // legend item counter
	      .enter()
	      .append("text")
		  .attr("text-anchor", "start")
	      .attr("x", 12)
	      .attrExpression("y", "function(d, i){ return 10 + i *  20;}")
	      //.text("pippo")
	      .text(new TextLegendDatumFunction(webEngine, Arrays.asList("Ago", "dem", "ar")))
	      ;

	}


	private String getLegendCommandFill(ColorLegendDatumFunction colorDatumFunction) {
		JSObject d3JsObject = d3.getJsObject();
		String funcName = createNewTemporaryInstanceName(); // see JavaScriptObject.java
		System.out.println("\t" + funcName);
		d3JsObject.setMember(funcName, colorDatumFunction);
		return "this.style('fill', function(d,i) {" //
				+ "return d3." + funcName + ".apply(this, {datum:d}, i);" //
				+ "})";
	}

	private String getLegendCommandText(TextLegendDatumFunction textDatumFunction) {
		JSObject d3JsObject = d3.getJsObject();
		String funcName = createNewTemporaryInstanceName(); // see JavaScriptObject.java
		System.out.println("\t" + funcName);
		d3JsObject.setMember(funcName, textDatumFunction);
		return "this.style('text', function(d,i) {" //
				+ "return d3." + funcName + ".apply(this, {datum:d}, i);" //
				+ "})";
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

	private static void applyTransformation(Selection selection, double x, double y, double rotation) {
		String transformString = "translate(" + x + "," + y + "),rotate(" + rotation + ")";
		selection.attr("transform", transformString);
	}


	public void saveSVG(String outputFilePath) {

		System.out.println("D3Plotter :: saveSVG");

		// get the svg node

		Document doc = browser.getWebEngine().getDocument();

		//String stringifiedDoc = getStringFromDoc(doc);
		//System.out.println(stringifiedDoc);

		NodeList nodes = doc.getElementsByTagName("svg");

		//System.out.println("Svg nodes, length: " + nodes.getLength());

		String stringifiedSVG = "";

		if ( nodes.getLength() > 0 ) {
			Node svgNode = nodes.item(0);
			try {
				stringifiedSVG = getStringFromNode(svgNode);
			} catch (TransformerException e1) {
				e1.printStackTrace();
			}

//			// File chooser
//			SaveHelper saveHelper = new SaveHelper();
//			saveHelper.saveSvg(stringifiedSVG);

			File file = new File(outputFilePath);
			if (file != null) {
				try {
					PrintWriter out = new PrintWriter(file);
					out.print(
							stringifiedSVG // stringifiedDoc //
							);
					out.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
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

	private void injectAdditionalJavascripts() {
		System.out.println("Injecting additional D3-related .js");

		String className = getClass().getName();
		String classPath = className.replace(".", "/");
		// System.out.println("==> " + classPath);
		int lastSlash = classPath.lastIndexOf("/");
		// System.out.println("==> " + lastSlash);
		if (lastSlash != -1) {
			List<String> jsList =
				    Arrays.asList(
				    		"d3-legend.min.js"
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
						System.out.println("\t" + sPath);
						URL sUrl = getClass().getClassLoader().getResource(sPath);
						try {
							FileInputStream  fis = new FileInputStream(sUrl.getFile());
							String jsContent = CharStreams.toString(new InputStreamReader(fis, "UTF-8"));

							// !!!
							webEngine.executeScript(jsContent);

						} catch (IOException e) {
							System.err.println("###############################");
							System.err.println("injectAdditionalJavascripts !!!");
							e.printStackTrace();
							System.err.println("###############################");
						}
					});
		}

	}// injectMathJax



	public JavaFxD3Browser getBrowser(Runnable postLoadingHook, boolean debugMode) {
		this.browser = new JavaFxD3Browser(postLoadingHook, debugMode);
		return this.browser;
	}

	protected static String createNewTemporaryInstanceName() {
		double random = Math.random();
		String randomString = ("" + random).substring(2,5);
		String name =  "temp__instance__" + System.currentTimeMillis() + "_" + randomString;
		return name;
	}

}
