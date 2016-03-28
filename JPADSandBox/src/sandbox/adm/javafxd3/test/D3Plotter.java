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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.ArrayUtils;
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

	private D3PlotterOptions options;

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
			D3PlotterOptions options,
			Double[][] dataArray
			) {
		this.options = options;
		this.listDataArray.add(dataArray);
	}

	/*
	 *  Constructor
	 */
	public D3Plotter(
			D3PlotterOptions options,
			List<Double[][]> listDataArray
			) {
		this.options = options;
		this.listDataArray.addAll(listDataArray);
	}	
	
	
	/*
	 * called in Runnable object
	 */
	public void createD3Content() {

		if (listDataArray.size() == 0)
			return;

		System.out.println("D3Plotter :: createD3Content");

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
				.attr("width", options.getWidthPageSVG())
				.attr("height", options.getHeightPageSVG());

		//page
		Selection pageSelection = svgSelection //
				.append("g") //
				.attr("id", "page")
				.attr("width", options.getWidthPageSVG()) //
				.attr("height", options.getHeightPageSVG());

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
				.attr("transform", "translate(" + options.getMargin().left + "," + options.getMargin().top + ")");

		Selection graphRectSelection = graphSelection // only the filling area+opacity
				.append("rect") //
				.attr("width", options.getWidthGraph()) //
				.attr("height", options.getHeightGraph()) //
				.attr("fill", options.getGraphBackgroundColor())
				.attr("opacity", options.getGraphBackgroundOpacity().toString())
				;

		String axisLineStyle = "stroke: " + options.getAxisLineColor() + "; stroke-width: " + options.getAxisLineStrokeWidth() +";";

		Selection graphRectSelection2 = graphSelection // only the outline
				.append("rect") //
				.attr("width", options.getWidthGraph()) //
				.attr("height", options.getHeightGraph()) //
				.attr("fill", "none")
				.attr("opacity", "1.0")
				.attr("style", axisLineStyle) // "stroke: black; stroke-width: 3px;"
				;

		//x axis
		Selection xAxisSelection = graphSelection //
				.append("g") //
				.attr("id", "" + "xAxis") //
				.attr("class", "axis") //
				.attr("transform", "translate(0," + options.getHeightGraph() + ")")
				;
		
		
		// Go through the list of data couples and plot
		// detect axis limits
		double xMin, xMax;
		double yMin, yMax;
		xMin = 1.0e15; xMax = -1.0e15; // initialize so that they will change
		yMin = 1.0e15; yMax = -1.0e15;

		for (Double[][] dataArray : listDataArray) {
			//--------------------------------------------------------
			// Find X- and Y- min/max values

			//		List<List<Double>> dataList = MyArrayUtils.convert2DArrayToList(dataArray);
			//		System.out.println(dataList);

			//		System.out.println(MyArrayUtils.extractColumnOf2DArrayToList(dataArray, 0));

			DoubleSummaryStatistics summaryStatisticsX = MyArrayUtils.extractColumnOf2DArrayToList(dataArray, 0).stream()
					.mapToDouble(v -> v)
					.summaryStatistics();

			// X-axis range
			if (options.isAutoRangeX()) {
				xMin = Math.min(xMin, summaryStatisticsX.getMin());
				xMax = Math.max(xMax, summaryStatisticsX.getMax());
			} else {
				xMin = options.getXMin();
				xMax = options.getXMax();
			}

			//		System.out.println(MyArrayUtils.extractColumnOf2DArrayToList(dataArray, 1));

			DoubleSummaryStatistics summaryStatisticsY = MyArrayUtils.extractColumnOf2DArrayToList(dataArray, 1).stream()
					.mapToDouble(v -> v)
					.summaryStatistics();

			// Y-axis range
			if (options.isAutoRangeY()) {
				yMin = Math.min(yMin, summaryStatisticsY.getMin());
				yMax = Math.max(yMax, summaryStatisticsY.getMax());
			} else {
				yMin = options.getYMin();
				yMax = options.getYMax();
			}
		
		}		

		// QuantitativeScale<?> xScale;

		LinearScale xScale = d3.scale() //
				.linear() //
				.domain(xMin, xMax) //
				.range(0.0, options.getWidthGraph())
				;

		// set the x axis
		org.treez.javafxd3.d3.svg.Axis xAxis = d3.svg() //
				.axis() //
				.scale(xScale) //
				.orient(org.treez.javafxd3.d3.svg.Axis.Orientation.BOTTOM) //
				.tickSize(-options.getHeightGraph())
				// .outerTickSize(10) // agodemar
				.tickPadding(options.getXtickPadding()) // agodemar
				;

		xAxis.apply(xAxisSelection);

		xAxisSelection //
		.selectAll("path, line") //
		.style("fill", "none") //
		.style("stroke", options.getXGridLineColor()) // TODO
		.style("stroke-width", options.getXGridLineStrokeWidth()) //
		.style("stroke-dasharray",options.getXGridLineDashArray())
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
				.range(options.getHeightGraph(), 0.0);

		org.treez.javafxd3.d3.svg.Axis yAxis = d3 //
				.svg() //
				.axis() //
				.scale(yScale)
				.orient(org.treez.javafxd3.d3.svg.Axis.Orientation.LEFT)
				.tickPadding(options.getYtickPadding())
				.tickSize(-options.getWidthGraph())
				// .outerTickSize(10) // agodemar
				// .tickPadding(5) // agodemar
				;

		yAxis.apply(yAxisSelection);

		yAxisSelection //
		.selectAll("path, line") //
		.style("fill", "none") //
		.style("stroke", options.getYGridLineColor()) // TODO
		.style("stroke-dasharray",options.getYGridLineDashArray())
		.style("stroke-width", options.getYGridLineStrokeWidth()) //
		.style("shape-rendering", "geometricPrecision"); // "crispEdges"


		// X-axis Label
		svgSelection.append("text")
		.attr("class", "x label")
		.attr("text-anchor", "middle")
		.attr("x", options.getMargin().left + options.getWidthGraph()/2)
		.attr("y", options.getMargin().top + options.getHeightGraph() + 45) // NB: offset
		.text("income per capita, inflation-adjusted (dollars)");

		// Y-Axis label
		svgSelection.append("text")
		.attr("class", "y label")
		.attr("text-anchor", "middle")
		.attr("x", -options.getMargin().top - options.getHeightGraph()/2)
		.attr("y", options.getMargin().left/2 - 15) // NB: offset
		.attr("dy", ".75em")
		.attr("transform", "rotate(-90)")
		.text("life expectancy (years)");

		// Title
		svgSelection.append("text")
		.attr("class", "title")
		.attr("text-anchor", "middle")
		.attr("x", options.getMargin().left + options.getWidthGraph()/2)
		.attr("y", (options.getMargin().top / 2) - 0)  // NB: offset
		.text("This is the title");
		
		
		// go through the list of data couples and plot 
		for (Double[][] dataArray : listDataArray) {

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
					.attr("style", options.getLineStyle());


			if ( options.isPlotArea() ) {
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
				.attr("style", options.getAreaStyle())
				.attr("opacity", options.getAreaOpacity())
				;
			}


			if (options.isShowSymbols()) {
				//plot symbols
				Symbol symbol = d3 //
						.svg() //
						.symbol();
				symbol = symbol //
						.size(options.getSymbolSize()) //
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
				.attr("style", options.getSymbolStyle());
			}

		}
		
		if (options.isShowLegend())
			putLegend();


		//################################
/*
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
} // if-false
*/
		// ###########################################################

		// ###########################################################
		// this is my playground ...

//		injectAdditionalJavascripts();

	}

	private void putLegend() {

		System.out.println("D3Plotter :: putLegend");

		// TODO
		
		if (options.isAutoLegend()) {
			// resize legend items list
			options.getLegendItems().clear();
			
			IntStream.range(0, listDataArray.size())
				.forEach(i -> {
					StringBuilder sb = new StringBuilder()
							.append("data set ")
							.append(i);
					options.getLegendItems().add(sb.toString());
				}
			);
			
		}
		
		// Count legend strings and arrange a counter vector accordingly
		int nLegendItems = options.getLegendItems().size();
		
		if (nLegendItems == 0) return;
		
		int[] itemCounter = ArrayUtils.toPrimitive(
			IntStream.range(0, nLegendItems)
				.boxed()
				.collect(Collectors.toList())
				.toArray(new Integer[0])
			);

		double xPercentLegendPos = 0.9; // from left
		double yPercentLegendPos = 0.1; // from top
		StringBuilder sbLegendTransform = new StringBuilder();
		sbLegendTransform
			.append("translate(")
				.append(xPercentLegendPos*options.getWidthGraph())
				.append(",")
				.append(0.1*options.getHeightGraph())
			.append(")")
			;
		
		Selection legend = svgSelection.append("g")
				  .attr("class", "legend")
//				  .attr("x", widthGraph - 65)
//				  .attr("y", 25)
//				  .attr("height", 100)
//				  .attr("width", 100)
				.attr("transform", sbLegendTransform.toString())
				;

		String commandFill = getLegendCommandFill(
				new ColorLegendDatumFunction(
						webEngine, Arrays.asList(
								"red", "blue", "green", "magenta", "blueviolet", "darkcyan", "crimson",
								"darkorchid", "orange", "orangered" 
								// add here the rest -> https://www.w3.org/TR/SVG/types.html#ColorKeywords 
								)));
		// System.out.println(commandFill);

//		String commandText = getLegendCommandText(
//				new TextLegendDatumFunction(
//						webEngine, Arrays.asList("Ago", "dem", "ar")));

		int rectHeight = 10, rectWidth = 10;
		int xOffset = 18, yOffsetMultiplier = 22, y0 = 10;
		
		legend.selectAll("rect")
	      .data(itemCounter) // new int[]{1, 2, 3} legend item counter
	      .enter()
	      .append("rect")
	      .attrExpression("y", "function(d, i){ return i * " + yOffsetMultiplier + ";}")
	      .attr("width", rectWidth)
	      .attr("height", rectHeight)
	      //.style("fill","green")
	      .evalForJsObject(commandFill) // .style("fill", new ColorLegendDatumFunction(webEngine))
	      ;

		legend.selectAll("text")
	      .data(itemCounter) // new int[]{1, 2, 3} legend item counter
	      .enter()
	      .append("text")
		  .attr("text-anchor", "start")
	      .attr("x", xOffset) // right offset wrt symbol
	      .attrExpression("y", "function(d, i){ return " + rectHeight + " + i * " + yOffsetMultiplier + ";}")
	      //.text("pippo")
	      .text(new TextLegendDatumFunction(
	    		  webEngine, 
	    		  options.getLegendItems() // Arrays.asList("Ago", "dem", "ar")
	    		  ))
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

	public D3 getD3() {
		return d3;
	}

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
