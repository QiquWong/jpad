package standaloneutils;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jscience.physics.amount.Amount;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class MyChartToFileUtils {

	private PrintWriter writer;
	private XYSeriesCollection datasetLineChart = new XYSeriesCollection( );
	private int styleIdx = 0;
	private int colorIdx = 0;
	private double xMin, xMax, yMin, yMax;

	private double legendPositionAlongCurve = 0.1;
	private int traceCounter = 0;
	private String symbolicCoords;
	private Array2DRowRealMatrix xArrays, yArrays;
	private int numberOfXarrays;
	private int numberOfYarrays;
	private int width, height;
	
	// TODO: elaborate on this
	List<ArrayRealVector> xVectors, yVectors;

	private double[] legendValue;
	private String[] legendValueString = null;

	private String path, fileName, 
	xLabel = "", yLabel = "", xUnit = "", yUnit="", 
	legendName = "", legendUnit = "";
	private boolean swapXY = false;
	private boolean stripTrailingZeros = false;
	
	public static final List<String> colorList = new ArrayList<String>();
	public static final List<String> styleList = new ArrayList<String>();

	static {

		MyChartToFileUtils.styleList.add("solid");
		MyChartToFileUtils.styleList.add("densely dashed");
		MyChartToFileUtils.styleList.add("dashdotted");
		MyChartToFileUtils.styleList.add("dashed");
		MyChartToFileUtils.styleList.add("densely dashdotted");
		MyChartToFileUtils.styleList.add("densely dotted");
		MyChartToFileUtils.styleList.add("dashdotdotted");
		MyChartToFileUtils.styleList.add("densely dashdotdetted");
		MyChartToFileUtils.styleList.add("loosely dashed");
		MyChartToFileUtils.styleList.add("loosely dashdotted");
		
//		MyChartToFileUtils.colorList.add("red");
//		MyChartToFileUtils.colorList.add("darkred");
//		MyChartToFileUtils.colorList.add("blue");
//		MyChartToFileUtils.colorList.add("darkblue");
//		MyChartToFileUtils.colorList.add("darkgrey");
//		MyChartToFileUtils.colorList.add("black");
//		MyChartToFileUtils.colorList.add("darkyellow");
//		MyChartToFileUtils.colorList.add("orange");
//		MyChartToFileUtils.colorList.add("pink");
//		MyChartToFileUtils.colorList.add("magenta"); 
//		MyChartToFileUtils.colorList.add("cyan");
//		MyChartToFileUtils.colorList.add("darkcyan");
		
		MyChartToFileUtils.colorList.add("black");
		MyChartToFileUtils.colorList.add("blue");
		MyChartToFileUtils.colorList.add("red");
		MyChartToFileUtils.colorList.add("darkgreen");
		MyChartToFileUtils.colorList.add("darkyellow");
		MyChartToFileUtils.colorList.add("brown");
		MyChartToFileUtils.colorList.add("magenta");
		MyChartToFileUtils.colorList.add("orange");
		MyChartToFileUtils.colorList.add("cyan");
		MyChartToFileUtils.colorList.add("darkcyan");

	}
	

	public MyChartToFileUtils() {
		styleIdx = 0;
		colorIdx = 0;
		set1152x768();
	}

	/**
	 * 
	 * @param xLabel
	 * @param yLabel
	 * @param xUnit
	 * @param yUnit
	 * @param legendName
	 * @param legendValue
	 * @param legendUnit
	 * @param path
	 * @param fileName
	 */
	public MyChartToFileUtils(
			String xLabel,
			String yLabel,
			String xUnit, String yUnit,
			String legendName, double[] legendValue,
			String legendUnit, String path,
			String fileName) {
		this();
		this.path = path;
		this.fileName = fileName;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		this.xUnit = xUnit;
		this.yUnit = yUnit;
		this.legendName = legendName;
		this.legendUnit = legendUnit;
		this.legendValue = legendValue;
	}
	
	// Creates plot with String array as legend value
	// @author Manuela Ruocco	
	
	public MyChartToFileUtils(
			String xLabel,
			String yLabel,
			String xUnit, String yUnit,
			String[] legendValue, String path,
			String fileName) {
		this();
		this.path = path;
		this.fileName = fileName;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		this.xUnit = xUnit;
		this.yUnit = yUnit;
		this.legendValueString = legendValue;
	}



	// Creates plot whit no legend. 
		// @author Manuela Ruocco
		
	
	public MyChartToFileUtils(
			String xLabel,
			String yLabel,
			String xUnit, String yUnit,double[] legend,
			String path,
			String fileName) {
		this();
		this.path = path;
		this.fileName = fileName;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		this.xUnit = xUnit;
		this.yUnit = yUnit;
		this.legendValue = legend;
	}
	/**
	 * 
	 * @param xLabel
	 * @param yLabel
	 * @param xUnit
	 * @param yUnit
	 * @param legendName
	 * @param legendValue
	 * @param legendUnit
	 * @param path
	 * @param fileName
	 * @return
	 */
	public static MyChartToFileUtils ChartFactory(
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String legendName, double[] legendValue,
			String legendUnit, 
			String path, String fileName) {
		return new MyChartToFileUtils(xLabel, yLabel, 
				xUnit, yUnit, legendName, legendValue, 
				legendUnit, path, fileName);
	}

	// Creates plot with String array as legend value
	// @author Manuela Ruocco
	
	public static MyChartToFileUtils ChartFactory(
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String[] legendValue, 
			String path, String fileName) {
		return new MyChartToFileUtils(xLabel, yLabel, 
				xUnit, yUnit, legendValue,  path, fileName);
	}
	
	// Creates plot whit no legend. Second
		// @author Manuela Ruocco
	
	public static MyChartToFileUtils ChartFactory(
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			double[] legend,
			String path, String fileName) {
		return new MyChartToFileUtils(xLabel, yLabel, 
				xUnit, yUnit, legend, 
				path, fileName);
	}
	/**
	 * A function to handle the creation of plots in a 
	 * very simple way. It's possible to use xArrays or yArrays as 1D arrays
	 * 
	 * 
	 * @author Lorenzo Attanasio
	 * @param xArrays
	 * @param yArrays
	 * @param xMin set xMin. Pass null to get the default value (minimum of xArrays)
	 * @param xMax set xMax. Pass null to get the default value (maximum of xArrays)
	 * @param yMin set yMin. Pass null to get the default value (minimum of yArrays)
	 * @param yMax set yMax. Pass null to get the default value (maximum of yArrays)
	 * @param xLabel
	 * @param yLabel
	 * @param xUnit
	 * @param yUnit
	 * @param legendName
	 * @param legendValue
	 * @param legendUnit
	 * @param path
	 * @param fileName
	 */
	public static void plot(
			double[][] xArrays, double[][] yArrays,
			Double xMin, Double xMax,
			Double yMin, Double yMax,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String legendName, double[] legendValue,
			String legendUnit, String path,
			String fileName) {

		MyChartToFileUtils chartFactory = ChartFactory(xLabel, yLabel, 
				xUnit, yUnit, legendName, legendValue, legendUnit, path, fileName);

		chartFactory.setXarrays(xArrays);
		chartFactory.setYarrays(yArrays);

		if (xMin != null) chartFactory.setxMin(xMin);
		if (xMax != null) chartFactory.setxMax(xMax);
		if (yMin != null) chartFactory.setyMin(yMin);
		if (yMax != null) chartFactory.setyMax(yMax);

		chartFactory.createMultiTraceChart();
		
		List<String> legend = new ArrayList<>();
		List<String> xListName = new ArrayList<>();
		List<String> yListName = new ArrayList<>();
		for(int i=0; i<legendValue.length; i++) {
			legend.add(legendName + " " + legendValue[i] + " " + legendUnit);
			xListName.add(xLabel);
			yListName.add(yLabel);
		}
		
		JPADStaticWriteUtils.exportToCSV(
				MyArrayUtils.convertTwoDimensionArrayToListDoubleArray(xArrays),
				MyArrayUtils.convertTwoDimensionArrayToListDoubleArray(yArrays),
				legend, 
				xListName, yListName, 
				path + fileName
				);
		
	}

	// TODO: document me!
	public static void plot(
			List<double[]> xArrays, List<double[]> yArrays,
			Double xMin, Double xMax,
			Double yMin, Double yMax,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String legendName, double[] legendValue,
			String legendUnit, String path,
			String fileName) {

		MyChartToFileUtils chartFactory = ChartFactory(xLabel, yLabel, 
				xUnit, yUnit, legendName, legendValue, legendUnit, path, fileName);

		for (double[] vx : xArrays )
			chartFactory.setXvectors(vx);
		
		for (double[] vy : yArrays )
			chartFactory.setYvectors(vy);

		if (xMin != null) chartFactory.setxMin(xMin);
		if (xMax != null) chartFactory.setxMax(xMax);
		if (yMin != null) chartFactory.setyMin(yMin);
		if (yMax != null) chartFactory.setyMax(yMax);

		chartFactory.createMultiTraceChart();
	}
	
	
	
	 
	public static void plot( //xArrays is 1D
			double[] xArray, double[][] yArrays,
			Double xMin, Double xMax,
			Double yMin, Double yMax,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String legendName, double[] legendValue,
			String legendUnit, String path,
			String fileName) {

		double[][] xArrays = new double[1][xArray.length];
		for (int i=0; i < xArray.length; i++)
			xArrays[0][i] = xArray[i];

		plot(xArrays, yArrays, xMin, xMax, yMin, yMax, xLabel, yLabel, xUnit, yUnit, 
				legendName, legendValue, legendUnit, path, fileName);
	}

	public static void plot(
			double[] xArray, double[] yArray,
			Double xMin, Double xMax,
			Double yMin, Double yMax,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String legendName, double[] legendValue,
			String legendUnit, String path,
			String fileName) {

		double[][] yArrays = new double[1][yArray.length];
		for (int i=0; i < yArray.length; i++)
			yArrays[0][i] = yArray[i];

		plot(xArray, yArrays, xMin, xMax, yMin, yMax, xLabel, yLabel, xUnit, yUnit, 
				legendName, legendValue, legendUnit, path, fileName);
	}

	// This function creates plot using an array of String as legendValue 
	//@author Manuela Ruocco
	
	/**
	 * A function to handle the creation of plots in a 
	 * very simple way using a String Array as legendValue. 
	 * It's possible to use xArrays or yArrays as 1D arrays.
	 * 
	 * 
	 * @author Manuela Ruocco
	 * @param xArrays
	 * @param yArrays
	 * @param xMin set xMin. Pass null to get the default value (minimum of xArrays)
	 * @param xMax set xMax. Pass null to get the default value (maximum of xArrays)
	 * @param yMin set yMin. Pass null to get the default value (minimum of yArrays)
	 * @param yMax set yMax. Pass null to get the default value (maximum of yArrays)
	 * @param xLabel
	 * @param yLabel
	 * @param xUnit
	 * @param yUnit
	 * @param legendValue
	 * @param path
	 * @param fileName
	 */
	public static void plot(
			double[][] xArrays, double[][] yArrays,
			Double xMin, Double xMax,
			Double yMin, Double yMax,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String[] legendValue, String path,
			String fileName) {

		MyChartToFileUtils chartFactory = ChartFactory(xLabel, yLabel, 
				xUnit, yUnit,legendValue,path, fileName);

		chartFactory.setXarrays(xArrays);
		chartFactory.setYarrays(yArrays);

		if (xMin != null) chartFactory.setxMin(xMin);
		if (xMax != null) chartFactory.setxMax(xMax);
		if (yMin != null) chartFactory.setyMin(yMin);
		if (yMax != null) chartFactory.setyMax(yMax);

		chartFactory.createMultiTraceChart();
	}
	
	public static void plot( //xArrays is 1D
			double[] xArray, double[][] yArrays,
			Double xMin, Double xMax,
			Double yMin, Double yMax,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String[] legendValue, String path,
			String fileName) {

		double[][] xArrays = new double[1][xArray.length];
		for (int i=0; i < xArray.length; i++)
			xArrays[0][i] = xArray[i];

		plot(xArrays, yArrays, xMin, xMax, yMin, yMax, xLabel, yLabel, xUnit, yUnit, 
				legendValue, path, fileName);
	}

	public static void plot( //xArrays is 1D
			double[] xArray, Double[][] yArrays,
			Double xMin, Double xMax,
			Double yMin, Double yMax,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String[] legendValue, String path,
			String fileName) {

		double[][] xArrays = new double[1][xArray.length];
		for (int i=0; i < xArray.length; i++)
			xArrays[0][i] = xArray[i];

		RealMatrix m = new Array2DRowRealMatrix(yArrays.length,yArrays[0].length);
		for (int i=0; i < yArrays.length; i++)
			m.setRow(i, ArrayUtils.toPrimitive(yArrays[i]));
		
		plot(xArrays, m.transpose().getData(), xMin, xMax, yMin, yMax, xLabel, yLabel, xUnit, yUnit, 
				legendValue, path, fileName);
	}
	

	
	
	
	public static void plot(
			double[] xArray, double[] yArray,
			Double xMin, Double xMax,
			Double yMin, Double yMax,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String[] legendValue, String path,
			String fileName) {

		double[][] yArrays = new double[1][yArray.length];
		for (int i=0; i < yArray.length; i++)
			yArrays[0][i] = yArray[i];

		plot(xArray, yArrays, xMin, xMax, yMin, yMax, xLabel, yLabel, xUnit, yUnit, 
				legendValue,  path, fileName);
	}
	
	// This function creates plot whit no legend
			//@author Manuela Ruocco	
	
	/**
	 * A function to handle the creation of plots with no legend in a 
	 * very simple way. It's possible to use xArrays or yArrays as 1D arrays.
	 * 
	 * @author Manuela Ruocco
	 * @param xArrays
	 * @param yArrays
	 * @param xMin set xMin. Pass null to get the default value (minimum of xArrays)
	 * @param xMax set xMax. Pass null to get the default value (maximum of xArrays)
	 * @param yMin set yMin. Pass null to get the default value (minimum of yArrays)
	 * @param yMax set yMax. Pass null to get the default value (maximum of yArrays)
	 * @param xLabel
	 * @param yLabel
	 * @param xUnit
	 * @param yUnit
	 * @param path
	 * @param fileName
	 */
	public static void plotNoLegend(
			double[][] xArrays, double[][] yArrays,
			Double xMin, Double xMax,
			Double yMin, Double yMax,
			String xLabel, String yLabel,
			String xUnit, String yUnit, String path,
			String fileName) {

		double [] legend = new double [xArrays.length];

		for ( int i=0; i<legend.length; i++){
			legend[i]=(double) i;
		}

		MyChartToFileUtils chartFactory = ChartFactory(xLabel, yLabel, 
				xUnit, yUnit,legend,path, fileName);

		chartFactory.setXarrays(xArrays);
		chartFactory.setYarrays(yArrays);

		if (xMin != null) chartFactory.setxMin(xMin);
		if (xMax != null) chartFactory.setxMax(xMax);
		if (yMin != null) chartFactory.setyMin(yMin);
		if (yMax != null) chartFactory.setyMax(yMax);

		chartFactory.createMultiTraceChartNoLegend();
	}

	public static void plotNoLegend( //xArrays is 1D
			double[] xArray, Double[][] yArrays,
			Double xMin, Double xMax,
			Double yMin, Double yMax,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
		     String path,
			String fileName) {

		RealMatrix m = new Array2DRowRealMatrix();
		for (int i=0; i < yArrays.length; i++)
			m.setRow(i, ArrayUtils.toPrimitive(yArrays[i]));

		plotNoLegend(xArray, m.transpose().getData(), xMin, xMax, yMin, yMax, xLabel, yLabel, xUnit, yUnit, 
				 path, fileName);
	}

	
	public static void plotNoLegend( //xArrays is 1D
			double[] xArray, double[][] yArrays,
			Double xMin, Double xMax,
			Double yMin, Double yMax,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
		     String path,
			String fileName) {

		double[][] xArrays = new double[1][xArray.length];
		for (int i=0; i < xArray.length; i++)
			xArrays[0][i] = xArray[i];

		plotNoLegend(xArrays, yArrays, xMin, xMax, yMin, yMax, xLabel, yLabel, xUnit, yUnit, 
				 path, fileName);
	}

	public static void plotNoLegend(
			double[] xArray, double[] yArray,
			Double xMin, Double xMax,
			Double yMin, Double yMax,
			String xLabel, String yLabel,
			String xUnit, String yUnit, String path,
			String fileName) {

		double[][] yArrays = new double[1][yArray.length];
		for (int i=0; i < yArray.length; i++)
			yArrays[0][i] = yArray[i];

		plotNoLegend(xArray, yArrays, xMin, xMax, yMin, yMax, xLabel, yLabel, xUnit, yUnit, 
				 path, fileName);
	}
	
	/**
	 * This static method allows user to create charts with a smart usage of the JFreeChart
	 * library. Giving in input, in fact, two List of double arrays (of any length) the
	 * method recognize, at first, if the corresponding elements have the same length and
	 * then creates an XYSeries object adding every element of the generic xList and yList
	 * component to it. Every series created this way is then added to an XYSeriesCollection
	 * that stores them before plotting.
	 * Finally the chart is created using JFreeChart chartFactory.
	 * 
	 * @author Vittorio Trifari
	 * @param xList a List of double[] which elements represent abscissas of the single curve 
	 * @param yList a List of double[] which elements represent ordinates of the single curve
	 * @param chartName a String containing the name that the user wants to display on top
	 * @param xLabelName 
	 * @param yLabelName
	 * @param legend a List of String which elements are the name of each plotted line
	 * @param folderPathName
	 * @param fileName
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void plot(
			List<Double[]> xList, List<Double[]> yList,
			String chartName, String xLabelName, String yLabelName,
			Double xMin, Double xMax, Double yMin, Double yMax,
			String xUnit, String yUnit,
			boolean showLegend, List<String> legend,
			String folderPathName, String fileName) throws InstantiationException, IllegalAccessException {
				
		//----------------------------------------------------------------------------------
		// Creating XY series from List and adding to a dataset
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		for (int i=0; i<xList.size(); i++) {
			// check if xList[i] and yList[i] have the same length
			if(xList.get(i).length == yList.get(i).length){
				XYSeries series = new XYSeries(legend.get(i), false);
				for (int j=0; j<xList.get(i).length; j++) {
					series.add(xList.get(i)[j], yList.get(i)[j]);
				}
				dataset.addSeries(series);					
			}
			else
				System.err.println("X AND Y LISTS CORRESPONGING ELEMENTS MUST HAVE SAME LENGTH");
		}
		
		//----------------------------------------------------------------------------------
		// Generating the .png graph
		if (!xUnit.equals("")) xUnit = "(" + xUnit + ")"; 
		if (!yUnit.equals("")) yUnit = "(" + yUnit + ")";
		
//		if (legend == null){
//			legend = new ArrayList<String>();
//			for ( int i = 0; i<xList.size() ; i++)
//				legend.add("default");
//		}
			
		// minimum and maximum of Lists research
		if(xMin == null) {
			xMin = 0.0;
			Double[] xMinArray = new Double[xList.size()];
			for(int i=0; i<xList.size(); i++) 
				xMinArray[i] = MyArrayUtils.getMin(xList.get(i));
			xMin = MyArrayUtils.getMin(xMinArray);
		}
		
		if(xMax == null) {
			xMax = 0.0;
			Double[] xMaxArray = new Double[xList.size()];
			for(int i=0; i<xList.size(); i++) 
				xMaxArray[i] = MyArrayUtils.getMax(xList.get(i));			
			xMax = MyArrayUtils.getMax(xMaxArray);
		}
		
		if(yMin == null) {
			yMin = 0.0;
			Double[] yMinArray = new Double[yList.size()];
			for(int i=0; i<xList.size(); i++) 
				yMinArray[i] = MyArrayUtils.getMin(yList.get(i));			
			yMin = MyArrayUtils.getMin(yMinArray);
		}
		
		if(yMax == null) {
			yMax = 0.0;
			Double[] yMaxArray = new Double[yList.size()];
			for(int i=0; i<xList.size(); i++) 
				yMaxArray[i] = MyArrayUtils.getMax(yList.get(i));			
			yMax = MyArrayUtils.getMax(yMaxArray);
		}
				
		JFreeChart chart = ChartFactory.createXYLineChart(
				chartName,	 					// Title
				xLabelName + " " + xUnit,		// x-axis Label
				yLabelName + " " + yUnit,		// y-axis Label
				dataset, 						// Dataset
				PlotOrientation.VERTICAL, 		// Plot Orientation
				showLegend, 					// Show Legend
				true, 							// Use tooltips
				false						 	// Configure chart to generate URLs?
				);
		chart.setBackgroundPaint(Color.WHITE);
		chart.setBackgroundImageAlpha(0.0f);
		chart.setAntiAlias(true);
		chart.getPlot().setBackgroundPaint(Color.WHITE);
		chart.getPlot().setBackgroundAlpha(0.0f);
		chart.getXYPlot().setDomainGridlinesVisible(true);
		chart.getXYPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);
		chart.getXYPlot().setRangeGridlinesVisible(true);
		chart.getXYPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);
		chart.getXYPlot().getDomainAxis().setRange(xMin - Math.abs(0.1*xMin), xMax + Math.abs(0.1*xMax));
		chart.getXYPlot().getRangeAxis().setRange(yMin - Math.abs(0.1*yMin), yMax + Math.abs(0.1*yMax));
		
		final Paint[] paintArray;
		// create default colors but modify some colors that are hard to see
		paintArray = ChartColor.createDefaultPaintArray();
		paintArray[0] = ChartColor.BLACK;
		paintArray[1] = ChartColor.BLUE;
		paintArray[2] = ChartColor.RED;
		paintArray[3] = ChartColor.DARK_GREEN;
		paintArray[4] = ChartColor.DARK_YELLOW;
		paintArray[5] = ChartColor.DARK_GRAY;
		paintArray[6] = ChartColor.DARK_BLUE;
		paintArray[7] = ChartColor.DARK_RED;
		paintArray[8] = ChartColor.VERY_DARK_GREEN;
		paintArray[9] = ChartColor.ORANGE;
		
		XYPlot plot = (XYPlot) chart.getPlot(); 
		plot.setDrawingSupplier(new DefaultDrawingSupplier(
                paintArray,
                DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
		
		// creation of the file .png
		File xyChart = new File(folderPathName + fileName + ".png"); 

		try {
			ChartUtilities.saveChartAsPNG(xyChart, chart, 1920, 1080);
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
		
		//----------------------------------------------------------------------------------
		// Generating the .tikz graph
		MyChartToFileUtils chartFactory = new MyChartToFileUtils();
		chartFactory.initializeTikz(
				folderPathName + fileName,
				xMin, xMax,
				yMin, yMax,
				xLabelName, yLabelName,
				xUnit, yUnit,
				"west", "black",
				"white", "left");

		if (xList.size() == 1)
			for (int i=0; i < yList.size(); i++) {
				try {
					chartFactory.addTraceToTikz(xList.get(0), yList.get(i), legend.get(i));
				} catch (ArrayIndexOutOfBoundsException e) {
					System.err.println("ARRAY INDEX OUT OF BOUNDS !!!");
				}
			}
		else if(xList.size() != 1 && yList.size() == 1)
			for (int i=0; i < yList.size(); i++)
				chartFactory.addTraceToTikz(xList.get(i), yList.get(0), legend.get(i));
		else
			for (int i=0; i < yList.size(); i++)
				chartFactory.addTraceToTikz(xList.get(i), yList.get(i), legend.get(i));

		chartFactory.closeTikz();
	}
	
	/** 
	 * Set up a tikz file.
	 * The writer needs to be closed
	 * 
	 * @param fileNameWithPath
	 * @param x
	 * @param y
	 * @param xmin
	 * @param xmax
	 * @param ymin
	 * @param ymax
	 * @param xlabel
	 * @param ylabel
	 * @param color
	 * @param style
	 * @param anchor
	 * @param draw
	 * @param fill
	 * @param align
	 */
	public void initializeTikz(
			String fileNameWithPath,
			double xmin, double xmax,
			double ymin, double ymax,
			String xlabel, String ylabel,
			String xUnit, String yUnit,
			String anchor, String draw,
			String fill, String align) {

		File f = new File(fileNameWithPath + ".tikz");
		if(f.exists()) {
			f.delete();
		}

		try {
			writer = new PrintWriter(fileNameWithPath + ".tikz", "UTF-8");
			writer.println("%" + fileNameWithPath
					.substring(
							fileNameWithPath.lastIndexOf(File.separator) + 1, 
							fileNameWithPath.length()));

			writer.println("\\begin{tikzpicture}\n");

			setTikzAxis(
					xmin, xmax,
					ymin, ymax,
					xlabel, ylabel,
					xUnit, yUnit,
					anchor, draw,
					fill, align);

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void initializeTikz(
			String directoryPlusFileName,
			double xmin, double xmax,
			double ymin, double ymax,
			String xlabel, String ylabel,
			String xUnit, String yUnit) {

		initializeTikz(
				directoryPlusFileName,
				xmin, xmax,
				ymin, ymax,
				xlabel, ylabel,
				xUnit, yUnit,
				"west", "black",
				"white", "left");

	}

	/**
	 * 
	 * @param fileNameWithPath
	 * @param symbolicCoords
	 */
	public void initializeBarGraphTikz(
			String fileNameWithPath, 
			String ... symbolicCoords) {

		File f = new File(fileNameWithPath + ".tikz");
		if(f.exists()) {
			f.delete();
		}

		try {
			writer = new PrintWriter(fileNameWithPath + ".tikz", "UTF-8");
			writer.println("%" + fileNameWithPath
					.substring(
							fileNameWithPath.lastIndexOf(File.separator) + 1, 
							fileNameWithPath.length()));

			writer.println("\\begin{tikzpicture}\n");
			setSymbolicCoords(symbolicCoords);
			setAxisBarGraph(this.symbolicCoords);
			//	            \addplot[ybar,fill=blue] coordinates {
			//	                (a small bar,   42)
			//	                (a medium bar,  50)
			//	                (a large bar,   80)
			//	            };
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * 
	 * @param s
	 */
	public void setSymbolicCoords(String[] s) {
		for (int i = 0; i < s.length-1; i++){
			symbolicCoords = s[i] + ",";
		}
		symbolicCoords = symbolicCoords + s[s.length-1];
	}

	public void setBarGraphValues(double[] values) {

	}

	/**
	 * Make a tikz file with one trace only.
	 * Some values are set by default. In this case
	 * the writer is automatically closed.
	 * 
	 * @param x
	 * @param y
	 * @param directory
	 * @param fileName
	 * @param xlabel
	 * @param ylabel
	 * @param style
	 */
	public void createTikz(
			double[] x, double[] y, 
			String directory, String fileName, 
			String xlabel, String ylabel, 
			String xUnit, String yUnit) {

		initializeTikz(
				directory + File.separator + fileName,
				setMin(MyArrayUtils.getMin(x)), setMax(MyArrayUtils.getMax(x)),
				setMin(MyArrayUtils.getMin(y)), setMax(MyArrayUtils.getMax(y)),
				xlabel, ylabel,
				xUnit, yUnit,
				"west", "black",
				"white", "left");

		addTraceToTikz(x, y, null, 0.,"");

		writer.println("\\end{axis}" + 
				"\n\\end{tikzpicture}%");
		writer.close();

	}

	public void createBarGraphTikz() {

		writer.println("\\end{axis}" + 
				"\n\\end{tikzpicture}%");
		writer.close();
	}

	public void createTikz(
			MyArray x, MyArray y, 
			String directory, String fileName,
			String xlabel, String ylabel,
			String xUnit, String yUnit) {

		createTikz(
				x.toArray(), y.toArray(), 
				directory, fileName,
				xlabel, ylabel, 
				xUnit, yUnit);
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @param path
	 * @param fileName
	 * @param xlabel
	 * @param ylabel
	 * @param xUnit
	 * @param yUnit
	 */
	public void createMultiTraceTikz(
			double[] x1, double[] y1, 
			double[] x2, double[] y2,
			double[] x3, double[] y3,
			String path, String fileName,
			String legendName, double[] legendValue,
			String legendUnit,
			String xlabel, String ylabel,
			String xUnit, String yUnit) {

		if (legendName == null || legendName == "")
			legendValue = new double[]{0., 0., 0.};

		initializeTikz(
				path + File.separator + fileName,
				setMin(MyArrayUtils.getMin(x1,x2,x3)), setMax(MyArrayUtils.getMax(x1,x2,x3)),
				setMin(MyArrayUtils.getMin(y1,y2,y3)), setMax(MyArrayUtils.getMax(y1,y2,y3)),
				xlabel, ylabel,
				xUnit, yUnit,
				"west", "black",
				"white", "left");

		addTraceToTikz(x1, y1, legendName, legendValue[0], legendUnit);
		addTraceToTikz(x2, y2, legendName, legendValue[1], legendUnit);

		if(x3 != null && x3.length != 0)
			addTraceToTikz(x3, y3, legendName, legendValue[2], legendUnit);

		closeTikz();
	}

	public void createMultiTraceTikz(
			double[] x1, double[] y1, 
			double[] x2, double[] y2,
			double[] x3, double[] y3,
			String path, String fileName,
			String xlabel, String ylabel,
			String xUnit, String yUnit) {
		createMultiTraceTikz(x1, y1, x2, y2, x3, y3, 
				path, fileName, "", null, "", xlabel, ylabel, xUnit, yUnit);
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param path
	 * @param fileName
	 * @param legendName
	 * @param legendValue
	 * @param legendUnit
	 * @param xLabel
	 * @param yLabel
	 * @param xUnit
	 * @param yUnit
	 */
	public void createMultiTraceTikz(
			String path, String fileName,
			String legendName, double[] legendValue,
			String legendUnit,
			String xLabel, String yLabel,
			String xUnit, String yUnit) {

		initializeTikz(
				path + File.separator + fileName,
				xMin, xMax,
				yMin, yMax,
				xLabel, yLabel,
				xUnit, yUnit,
				"west", "black",
				"white", "left");

		if (numberOfXarrays == 1)
			for (int i=0; i < numberOfYarrays; i++) {
				try {
					addTraceToTikz(xArrays.getRow(0), yArrays.getRow(i), legendName, legendValue[i], legendUnit);
				} catch (ArrayIndexOutOfBoundsException e) {
					addTraceToTikz(xArrays.getRow(0), yArrays.getRow(i), legendName, 999., legendUnit);
				}
			}
		else if(numberOfXarrays != 1 && numberOfYarrays == 1)
			for (int i=0; i < numberOfYarrays; i++)
				addTraceToTikz(xArrays.getRow(i), yArrays.getRow(0), legendName, legendValue[i], legendUnit);
		else
			for (int i=0; i < numberOfYarrays; i++)
				addTraceToTikz(xArrays.getRow(i), yArrays.getRow(i), legendName, legendValue[i], legendUnit);

		closeTikz();
	}

	public void createMultiTraceTikz(
			String path, String fileName,
		    String[] legendValue,
			String xLabel, String yLabel,
			String xUnit, String yUnit) {

		initializeTikz(
				path + File.separator + fileName,
				xMin, xMax,
				yMin, yMax,
				xLabel, yLabel,
				xUnit, yUnit,
				"west", "black",
				"white", "left");

		if (numberOfXarrays == 1)
			for (int i=0; i < numberOfYarrays; i++) {
				try {
					addTraceToTikz(xArrays.getRow(0), yArrays.getRow(i), legendValue[i]);
				} catch (ArrayIndexOutOfBoundsException e) {
					addTraceToTikz(xArrays.getRow(0), yArrays.getRow(i), legendName, 999., legendUnit);
				}
			}
		else if(numberOfXarrays != 1 && numberOfYarrays == 1)
			for (int i=0; i < numberOfYarrays; i++)
				addTraceToTikz(xArrays.getRow(i), yArrays.getRow(0),legendValue[i]);
		else
			for (int i=0; i < numberOfYarrays; i++)
				addTraceToTikz(xArrays.getRow(i), yArrays.getRow(i), legendValue[i]);

		closeTikz();
	}
	
	public void createSingleTraceTikz(
			String path, String fileName,
			String xLabel, String yLabel,
			String xUnit, String yUnit) {

		initializeTikz(
				path + File.separator + fileName,
				xMin, xMax,
				yMin, yMax,
				xLabel, yLabel,
				xUnit, yUnit,
				"west", "black",
				"white", "left");

		if (numberOfXarrays == 1)
			for (int i=0; i < numberOfYarrays; i++) {
				try {
					addTraceToTikz(xArrays.getRow(0), yArrays.getRow(i), " ");
				} catch (ArrayIndexOutOfBoundsException e) {
					addTraceToTikz(xArrays.getRow(0), yArrays.getRow(i), legendName, 999., legendUnit);
				}
			}
		else if(numberOfXarrays != 1 && numberOfYarrays == 1)
			for (int i=0; i < numberOfYarrays; i++)
				addTraceToTikz(xArrays.getRow(i), yArrays.getRow(0)," ");
		else
			for (int i=0; i < numberOfYarrays; i++)
				addTraceToTikz(xArrays.getRow(i), yArrays.getRow(i), " ");

		closeTikz();
	}
	/**
	 * @deprecated
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @param path
	 * @param fileName
	 * @param legendName
	 * @param legendValue
	 * @param legendUnit
	 * @param xLabel
	 * @param yLabel
	 * @param xUnit
	 * @param yUnit
	 */
	public void createMultiTracePng(
			double[] x1, double[] y1, 
			double[] x2, double[] y2,
			double[] x3, double[] y3,
			String path, String fileName,
			String legendName, double[] legendValue,
			String legendUnit,
			String xLabel, String yLabel,
			String xUnit, String yUnit) {

		addTraceToPng(x1, y1,
				legendName + " = " 
						+ BigDecimal.valueOf(
								(legendValue[0])).round(new MathContext(3)));

		addTraceToPng(x2, y2,
				legendName + " = " 
						+ BigDecimal.valueOf(
								(legendValue[1])).round(new MathContext(3)));

		addTraceToPng(x3, y3,
				legendName + " = " 
						+ BigDecimal.valueOf(
								(legendValue[2])).round(new MathContext(3)));

		createPNG(
				path + fileName,
				" ",
				xLabel, yLabel,
				xUnit, yUnit,
				width, height);
	}

	/**
	 * 
	 * @param path
	 * @param fileName
	 * @param legendName
	 * @param legendValue
	 * @param legendUnit
	 * @param xLabel
	 * @param yLabel
	 * @param xUnit
	 * @param yUnit
	 * @param stripTrailingZeros TODO
	 */
	
// Creates plot with double array as legend value	
	public void createMultiTracePng(
			String path, String fileName,
			String legendName, double[] legendValue,
			String legendUnit,
			String xLabel, String yLabel,
			String xUnit, String yUnit, 
			boolean stripTrailingZeros) {

		addArraysToPng(numberOfXarrays, numberOfYarrays, xArrays, yArrays, stripTrailingZeros);

		createPNG(
				path + fileName,
				" ",
				xLabel, yLabel,
				xUnit, yUnit,
				width, height);
	}

	// Creates plot with String array as legend value
	// @author Manuela Ruocco
	
	public void createMultiTracePng(
			String path, String fileName,
		    String[] legendValue,
			String xLabel, String yLabel,
			String xUnit, String yUnit, 
			boolean stripTrailingZeros) {

		addArraysToPng(numberOfXarrays, numberOfYarrays, xArrays, yArrays, stripTrailingZeros);

		//addArraysToPng(xVectors.size(), yVectors.size(), xVectors, yVectors, stripTrailingZeros);
		
		createPNG(
				path + fileName,
				" ",
				xLabel, yLabel,
				xUnit, yUnit,
				width, height);
	}
	

	// Creates plot whit no legend
	// @author Manuela Ruocco
	
	public void createSingleTracePngNoLegend(
			String path, String fileName, double[] legendValue,
			String xLabel, String yLabel,
			String xUnit, String yUnit, 
			boolean stripTrailingZeros) {

		addArraysToPng(numberOfXarrays, numberOfYarrays, xArrays, yArrays, stripTrailingZeros);

		createPNGNoLegend(
				path + fileName,
				" ",
				xLabel, yLabel,
				xUnit, yUnit,
				width, height);
	}

	private void addArraysToPng(int numberOfXarrays, int numberOfYarrays, 
			Array2DRowRealMatrix xArrays, Array2DRowRealMatrix yArrays, 
			boolean stripTrailingZeros) {


		if (legendValueString != null){  // if the legend value is a String array

			if (numberOfXarrays == 1)
				for (int i=0; i < numberOfYarrays; i++) {
					if (stripTrailingZeros)
						addTraceToPng(
								MyArrayUtils.stripTrailingZeros(xArrays.getRow(0)), 
								MyArrayUtils.stripTrailingZeros(yArrays.getRow(i)),
								" " +  legendValueString[i] + " ");

					else
						addTraceToPng(xArrays.getRow(0), yArrays.getRow(i),
								" " +  legendValueString[i]+ " ");

				}
			else if(numberOfXarrays != 1 && numberOfYarrays == 1)
				for (int i=0; i < numberOfXarrays; i++) {
					if (stripTrailingZeros)
						addTraceToPng(
								MyArrayUtils.stripTrailingZeros(xArrays.getRow(0)), 
								MyArrayUtils.stripTrailingZeros(yArrays.getRow(i)),
								" " +  legendValueString[i]+ " ");

					else
						addTraceToPng(xArrays.getRow(i), yArrays.getRow(0),
								" " + legendValueString[i]+ " ");
				}
			else // legend value is double array
				for (int i=0; i < numberOfYarrays; i++) {
					if (stripTrailingZeros){
						addTraceToPng(
								MyArrayUtils.stripTrailingZeros(xArrays.getRow(0)), 
								MyArrayUtils.stripTrailingZeros(yArrays.getRow(i)),
								" " + legendValueString[i]+ " ");
					}
					else{

						addTraceToPng(xArrays.getRow(i), yArrays.getRow(i),
								" " + legendValueString[i]+ " ");

					}}}

		else if(legendValue != null) {// if the legend value is a double array. In this case thhe method need also legend name and legend unit
			if (numberOfXarrays == 1)
				for (int i=0; i < numberOfYarrays; i++) {
					if (stripTrailingZeros)
						addTraceToPng(
								MyArrayUtils.stripTrailingZeros(xArrays.getRow(0)), 
								MyArrayUtils.stripTrailingZeros(yArrays.getRow(i)),
								legendName + " " + datasetLineChart.getSeriesCount() + " = " 
										+ BigDecimal.valueOf(
												(legendValue[i])).round(new MathContext(3)).toPlainString()
										+ " " + legendUnit);
					else
						addTraceToPng(xArrays.getRow(0), yArrays.getRow(i),
								legendName + " " + datasetLineChart.getSeriesCount() + " = " 
										+ BigDecimal.valueOf(
												(legendValue[i])).round(new MathContext(3)).toPlainString()
										+ " " + legendUnit);
				}
			else if(numberOfXarrays != 1 && numberOfYarrays == 1)
				for (int i=0; i < numberOfXarrays; i++) {
					if (stripTrailingZeros)
						addTraceToPng(
								MyArrayUtils.stripTrailingZeros(xArrays.getRow(0)), 
								MyArrayUtils.stripTrailingZeros(yArrays.getRow(i)),
								legendName + " " + datasetLineChart.getSeriesCount() + " = " 
										+ BigDecimal.valueOf(
												(legendValue[i])).round(new MathContext(3)).toPlainString()
										+ " " + legendUnit);
					else
						addTraceToPng(xArrays.getRow(i), yArrays.getRow(0),
								legendName + " " + datasetLineChart.getSeriesCount() + " = " 
										+ BigDecimal.valueOf(
												(legendValue[i])).round(new MathContext(3)).toPlainString()
										+ " " + legendUnit);
				}
			else{
				for (int i=0; i < numberOfYarrays; i++) {
					if (stripTrailingZeros){
						addTraceToPng(
								MyArrayUtils.stripTrailingZeros(xArrays.getRow(0)), 
								MyArrayUtils.stripTrailingZeros(yArrays.getRow(i)),
								legendName + " " + datasetLineChart.getSeriesCount() + " = " 
										+ BigDecimal.valueOf(
												(legendValue[i])).round(new MathContext(3)).toPlainString()
										+ " " + legendUnit);
					}
					else{
						addTraceToPng(xArrays.getRow(i), yArrays.getRow(i),
								legendName + " " + datasetLineChart.getSeriesCount() + " = " 
										+ BigDecimal.valueOf(
												(legendValue[i])).round(new MathContext(3)).toPlainString()
										+ " " + legendUnit);
					}
				}

			}}}

	
	// TODO: with lists
	private void addArraysToPng(int numberOfXarrays, int numberOfYarrays, 
			List<ArrayRealVector> xArrays, List<ArrayRealVector> yArrays, 
			boolean stripTrailingZeros) {


		if (legendValueString != null){  // if the legend value is a String array

			if (numberOfXarrays == 1)
				for (int i=0; i < numberOfYarrays; i++) {
					if (stripTrailingZeros)
						addTraceToPng(
								MyArrayUtils.stripTrailingZeros(xArrays.get(0).toArray()), 
								MyArrayUtils.stripTrailingZeros(yArrays.get(i).toArray()),
								" " +  legendValueString[i] + " ");

					else
						addTraceToPng(xArrays.get(0).toArray(), yArrays.get(i).toArray(),
								" " +  legendValueString[i]+ " ");

				}
			else if(numberOfXarrays != 1 && numberOfYarrays == 1)
				for (int i=0; i < numberOfXarrays; i++) {
					if (stripTrailingZeros)
						addTraceToPng(
								MyArrayUtils.stripTrailingZeros(xArrays.get(0).toArray()), 
								MyArrayUtils.stripTrailingZeros(yArrays.get(i).toArray()),
								" " +  legendValueString[i]+ " ");

					else
						addTraceToPng(xArrays.get(i).toArray(), yArrays.get(0).toArray(),
								" " + legendValueString[i]+ " ");
				}
			else // legend value is double array
				for (int i=0; i < numberOfYarrays; i++) {
					if (stripTrailingZeros){
						addTraceToPng(
								MyArrayUtils.stripTrailingZeros(xArrays.get(0).toArray()), 
								MyArrayUtils.stripTrailingZeros(yArrays.get(i).toArray()),
								" " + legendValueString[i]+ " ");
					}
					else{

						addTraceToPng(xArrays.get(i).toArray(), yArrays.get(i).toArray(),
								" " + legendValueString[i]+ " ");

					}}}

		else if(legendValue != null) {// if the legend value is a double array. In this case thhe method need also legend name and legend unit
			if (numberOfXarrays == 1)
				for (int i=0; i < numberOfYarrays; i++) {
					if (stripTrailingZeros)
						addTraceToPng(
								MyArrayUtils.stripTrailingZeros(xArrays.get(0).toArray()), 
								MyArrayUtils.stripTrailingZeros(yArrays.get(i).toArray()),
								legendName + " " + datasetLineChart.getSeriesCount() + " = " 
										+ BigDecimal.valueOf(
												(legendValue[i])).round(new MathContext(3)).toPlainString()
										+ " " + legendUnit);
					else
						addTraceToPng(xArrays.get(0).toArray(), yArrays.get(i).toArray(),
								legendName + " " + datasetLineChart.getSeriesCount() + " = " 
										+ BigDecimal.valueOf(
												(legendValue[i])).round(new MathContext(3)).toPlainString()
										+ " " + legendUnit);
				}
			else if(numberOfXarrays != 1 && numberOfYarrays == 1)
				for (int i=0; i < numberOfXarrays; i++) {
					if (stripTrailingZeros)
						addTraceToPng(
								MyArrayUtils.stripTrailingZeros(xArrays.get(0).toArray()), 
								MyArrayUtils.stripTrailingZeros(yArrays.get(i).toArray()),
								legendName + " " + datasetLineChart.getSeriesCount() + " = " 
										+ BigDecimal.valueOf(
												(legendValue[i])).round(new MathContext(3)).toPlainString()
										+ " " + legendUnit);
					else
						addTraceToPng(xArrays.get(i).toArray(), yArrays.get(0).toArray(),
								legendName + " " + datasetLineChart.getSeriesCount() + " = " 
										+ BigDecimal.valueOf(
												(legendValue[i])).round(new MathContext(3)).toPlainString()
										+ " " + legendUnit);
				}
			else{
				for (int i=0; i < numberOfYarrays; i++) {
					if (stripTrailingZeros){
						addTraceToPng(
								MyArrayUtils.stripTrailingZeros(xArrays.get(0).toArray()), 
								MyArrayUtils.stripTrailingZeros(yArrays.get(i).toArray()),
								legendName + " " + datasetLineChart.getSeriesCount() + " = " 
										+ BigDecimal.valueOf(
												(legendValue[i])).round(new MathContext(3)).toPlainString()
										+ " " + legendUnit);
					}
					else{
						addTraceToPng(xArrays.get(i).toArray(), yArrays.get(i).toArray(),
								legendName + " " + datasetLineChart.getSeriesCount() + " = " 
										+ BigDecimal.valueOf(
												(legendValue[i])).round(new MathContext(3)).toPlainString()
										+ " " + legendUnit);
					}
				}

			}}}
	

	public void closeTikz() {
		writer.println("\\end{axis}" + 
				"\n\\end{tikzpicture}%");
		writer.close();
	}

	/**
	 * 
	 * @param xmin
	 * @param xmax
	 * @param ymin
	 * @param ymax
	 * @param xlabel
	 * @param ylabel
	 * @param anchor
	 * @param draw
	 * @param fill
	 * @param align
	 */
	public void setTikzAxis(
			double xmin, double xmax,
			double ymin, double ymax,
			String xlabel, String ylabel,
			String xUnit, String yUnit,
			String anchor, String draw,
			String fill, String align) {

		if (!xUnit.equals("")) xUnit = "(" + xUnit + ")"; 
		if (!yUnit.equals("")) yUnit = "(" + yUnit + ")"; 

		xMin = xmin; xMax = xmax;
		yMin = ymin; yMax = ymax;
		
		writer.println("\\begin{axis}[");

		writer.println("width=\\figurewidth,"
				+ "\nheight=\\figureheight,"
				+ "\nscaled ticks=false, tick label style={/pgf/number format/fixed},");

		writer.println("xmin=" + xmin 
				+ ",\nxmax=" + xmax
				+ ",\nxlabel={" + xlabel + " " + xUnit + "}"
				+ ",\nxmajorgrids"
				+ ",\nymin=" + ymin 
				+ ",\nymax=" + ymax
				+ ",\nylabel={" + ylabel + " " + yUnit + "}"
				+ ",\nymajorgrids"
				+ ",\nlegend style={" 
				+ "at={(1.03,0.5)},"
				+ "anchor=" + anchor + ","
				+ "draw=" + draw + ","
				+ "fill=" + fill + ","
				+ "legend cell align=" + align + "}"
				+ "\n]"
				);
	}

	/**
	 * 
	 * @param xmin
	 * @param xmax
	 * @param ymin
	 * @param ymax
	 * @param xlabel
	 * @param ylabel
	 */
	public void setAxisDefault(
			double xmin, double xmax,
			double ymin, double ymax,
			String xlabel, String ylabel,
			String xUnit, String yUnit) {

		setTikzAxis(
				xmin,  xmax,
				ymin,  ymax,
				xlabel,  ylabel,
				xUnit, yUnit,
				"west", "black",
				"white", "left");
	}

	public void setAxisBarGraph(String symbolicCoords) {

		writer.println("\\begin{axis}[");
		writer.println("width=\\figurewidth,"
				+ "\nheight=\\figureheight,"
				+ "\nsymbolic x coords={"
				+ symbolicCoords + "}"
				+ "\nxtick=data"
				+ "\n]");
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param color
	 * @param style
	 */
	public void addTraceToTikz(double[] x, double[] y,
			String legendName, double legendValue,
			String legendUnit) {

		addTraceToTikz(x, y,
				legendName, legendValue,
				legendUnit, null, null, 
				false);
	}

	public void addTraceToTikz(Double[] x, Double[] y,
			String legendName, Double legendValue,
			String legendUnit) {

		addTraceToTikz(ArrayUtils.toPrimitive(x), ArrayUtils.toPrimitive(y),
				legendName, legendValue.doubleValue(),
				legendUnit, null, null, 
				false);
	}
	
	// to use String Array as legend value 
	
	public void addTraceToTikz(double[] x, double[] y,
			 String legendValue) {

		addTraceToTikz(x, y,legendValue,
				null, null, 
				false);
	}

	public void addTraceToTikz(Double[] x, Double[] y,
			 String legendValue) {

		addTraceToTikz(ArrayUtils.toPrimitive(x), ArrayUtils.toPrimitive(y),
				legendValue, null, null, 
				false);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param legendName
	 * @param legendValue
	 * @param legendUnit
	 * @param style
	 * @param color
	 * @param reduceDataPoints
	 */
	public void addTraceToTikz(double[] x, double[] y,
			String legendName, double legendValue,
			String legendUnit, String style, String color, 
			boolean reduceDataPoints) {

		if (Math.abs(legendValue) < 1e-8) legendValue = 0.0;

		// Manage arrays of different length. The method will make the plot
		// until the shortest array length is reached
		int len;
		double[] newX, newY;
		if (x.length < y.length) {
			len = x.length;
			newX = x;
			newY = ArrayUtils.subarray(y, 0, x.length);
		}
		else if (x.length > y.length){
			len = y.length;
			newX = ArrayUtils.subarray(x, 0, y.length);
			newY = y;
		} else {
			len = x.length;
			newX = x; newY = y;
		}

		// Reduce array size to ease the managing of the output file
		int nmax = 51;
		MyArray xx = new MyArray(newX), yy = new MyArray(newY);

		if(len > nmax && reduceDataPoints == true) {
			xx = new MyArray(MyMathUtils.getInterpolatedValue1DLinear(
					x, x, MyArrayUtils.linspace(MyArrayUtils.getMin(x), MyArrayUtils.getMax(x), nmax)));

			yy = new MyArray(MyMathUtils.getInterpolatedValue1DLinear(
					x, y, MyArrayUtils.linspace(MyArrayUtils.getMin(x), MyArrayUtils.getMax(x), nmax)));
		}

		// Set color and line style
		writer.println("\n\\addplot [");

		if (color != null && !color.equals("")) {
			writer.println("color=" + color + ",");
		} else {
			writer.println("color=" + MyChartToFileUtils.colorList.get(colorIdx) + ",");
		}

		if (style != null && !style.equals(""))
			writer.println(style);
		else{
			writer.println(MyChartToFileUtils.styleList.get(styleIdx));
			styleIdx++;
		}

		writer.println("]");

		// Print the data
		writer.println("table[row sep=crcr]{");

		for (int i=0; i < xx.size(); i++) {
			writer.println(xx.get(i) + "	" + yy.get(i) + "\\\\");
		}

		// Manage legends above curves
		String legendRelativePosition = "above";
		if (legendPositionAlongCurve > 0.95) legendPositionAlongCurve = 0.1;

		if (legendName != null && legendName != ""){

			String legend = "";

			if (traceCounter == 0) { 
				legend = "{" + legendName + " = " 
						+ BigDecimal.valueOf(legendValue).round(new MathContext(3)).stripTrailingZeros().toPlainString() 
						+ " " + legendUnit + "};";

			} else {
				legend = "{" + BigDecimal.valueOf(legendValue).round(new MathContext(3)).stripTrailingZeros().toPlainString()
						+ " " + legendUnit + "};";
			}

			writer.println("} node[" 
					+ legendRelativePosition + "," 
					+ "pos=" + BigDecimal.valueOf(legendPositionAlongCurve).round(new MathContext(3)).toPlainString()
					+ "] " + legend);

			//			writer.println("\\addlegendentry{" + legendName + " = " + legendValue + "};");
		} else {
			writer.println("};");
		}

		legendPositionAlongCurve += 0.06;
		traceCounter++;	
	}
	
	public void addTraceToTikz(double[] x, double[] y, String legendValue,
			String style, String color, 
			boolean reduceDataPoints) {

		// Manage arrays of different length. The method will make the plot
		// until the shortest array length is reached
		int len;
		double[] newX, newY;
		if (x.length < y.length) {
			len = x.length;
			newX = x;
			newY = ArrayUtils.subarray(y, 0, x.length);
		}
		else if (x.length > y.length){
			len = y.length;
			newX = ArrayUtils.subarray(x, 0, y.length);
			newY = y;
		} else {
			len = x.length;
			newX = x; newY = y;
		}

		// Reduce array size to ease the managing of the output file
		int nmax = 51;
		MyArray xx = new MyArray(newX), yy = new MyArray(newY);

		if(len > nmax && reduceDataPoints == true) {
			xx = new MyArray(MyMathUtils.getInterpolatedValue1DLinear(
					x, x, MyArrayUtils.linspace(MyArrayUtils.getMin(x), MyArrayUtils.getMax(x), nmax)));

			yy = new MyArray(MyMathUtils.getInterpolatedValue1DLinear(
					x, y, MyArrayUtils.linspace(MyArrayUtils.getMin(x), MyArrayUtils.getMax(x), nmax)));
		}

		// Set color and line style
		writer.println("\n\\addplot [");

		if (color != null && !color.equals("")) {
			writer.println("color=" + color + ",");
		} else {
			writer.println("color=" + MyChartToFileUtils.colorList.get(colorIdx) + ",");
		}

		if (style != null && !style.equals(""))
			writer.println(style);
		else{
			writer.println(MyChartToFileUtils.styleList.get(styleIdx));
			styleIdx++;
		}

		writer.println("]");

		// Print the data
		writer.println("table[row sep=crcr]{");

		for (int i=0; i < xx.size(); i++) {
			writer.println(xx.get(i) + "	" + yy.get(i) + "\\\\");
		}

		// Manage legends above curves
		String legendRelativePosition = "above";
		if (legendPositionAlongCurve > 0.95) legendPositionAlongCurve = 0.1;

		if (legendName != null && legendName != ""){

			String legend = "";
			 
			legend = "{" + legendUnit + "};";
			
			writer.println("} node[" 
					+ legendRelativePosition + "," 
					+ "pos=" + BigDecimal.valueOf(legendPositionAlongCurve).round(new MathContext(3)).toPlainString()
					+ "] " + legend);

			//			writer.println("\\addlegendentry{" + legendName + " = " + legendValue + "};");
		} else {
			writer.println("};");
		}

		legendPositionAlongCurve += 0.06;
		traceCounter++;	
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param legendName
	 * @param legendValue
	 * @param color
	 * @param style
	 */
	public void addAllTracesToTikz(List<Double[]> x, List<Double[]> y,
			String legendName, List<Double> legendValue,
			String legendUnit) {

		for (int i=0; i < x.size(); i++)  {
			addTraceToTikz(ArrayUtils.toPrimitive(x.get(i)), 
					ArrayUtils.toPrimitive(y.get(i)),
					legendName, legendValue.get(i),
					legendUnit); 
		}

		writer.close();
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param legend
	 */
	public void addTraceToPng(
			double[] x, double[] y,
			String legend) {

		int len;
		if (x.length < y.length) len = x.length;
		else len = y.length;

		XYSeries series = new XYSeries(legend, false);
		for (int i = 0; i < len; i++) {
			series.add( x[i] , y[i] );
		}

		datasetLineChart.addSeries( series );
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param legend
	 */
	public void addAllTracesToPng(
			List<Double[]> x, List<Double[]> y,
			List<String> legend) {

		for (int j=0; j< x.size(); j++) {
			addTraceToPng(
					ArrayUtils.toPrimitive(x.get(j)), 
					ArrayUtils.toPrimitive(y.get(j)), 
					legend.get(j));
		}
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param filenameWithPath
	 * @param title
	 * @param legend
	 * @param xLabel
	 * @param yLabel
	 * @param width
	 * @param height
	 */
	public void createPNG(
			String filenameWithPath,
			String title,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			int width, int height) {

		if (!xUnit.equals("")) xUnit = "(" + xUnit + ")"; 
		if (!yUnit.equals("")) yUnit = "(" + yUnit + ")";

		PlotOrientation orientation;
		if (swapXY) orientation = PlotOrientation.HORIZONTAL;
		else orientation = PlotOrientation.VERTICAL;

		JFreeChart xylineChart = ChartFactory.createXYLineChart(
				title, 
				xLabel + " " + xUnit,
				yLabel + " " + yUnit, 
				datasetLineChart,
				orientation, 
				true, true, false);

		xylineChart.setBackgroundPaint(Color.WHITE);
		xylineChart.setBackgroundImageAlpha(0.0f);
		xylineChart.setAntiAlias(true);

		xylineChart.getPlot().setBackgroundPaint(Color.WHITE);
		xylineChart.getPlot().setBackgroundAlpha(0.0f);
		//		xylineChart.getXYPlot().getRenderer().setSeriesPaint(seriesIndex, color);
		xylineChart.getXYPlot().setDomainGridlinesVisible(true);
		xylineChart.getXYPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);
		xylineChart.getXYPlot().setRangeGridlinesVisible(true);
		xylineChart.getXYPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);
		//xylineChart.removeLegend(); 

		final Paint[] paintArray;
		// create default colors but modify some colors that are hard to see
		paintArray = ChartColor.createDefaultPaintArray();
		paintArray[0] = ChartColor.BLACK;
		paintArray[1] = ChartColor.BLUE;
		paintArray[2] = ChartColor.RED;
		paintArray[3] = ChartColor.DARK_GREEN;
		paintArray[4] = ChartColor.DARK_YELLOW;
		paintArray[5] = ChartColor.DARK_GRAY;
		paintArray[6] = ChartColor.DARK_BLUE;
		paintArray[7] = ChartColor.DARK_RED;
		paintArray[8] = ChartColor.VERY_DARK_GREEN;
		paintArray[9] = ChartColor.ORANGE;
		
		XYPlot plot = (XYPlot) xylineChart.getPlot(); 
		plot.setDrawingSupplier(new DefaultDrawingSupplier(
                paintArray,
                DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
		
		NumberAxis domain = (NumberAxis) xylineChart.getXYPlot().getDomainAxis();
		domain.setRange(xMin, xMax);
		//        domain.setTickUnit(new NumberTickUnit(0.1));
		//        domain.setVerticalTickLabels(true);
		NumberAxis range = (NumberAxis) xylineChart.getXYPlot().getRangeAxis();
		range.setRange(yMin, yMax);
		//        range.setTickUnit(new NumberTickUnit(0.1));

		File xyChart = new File(filenameWithPath + ".png"); 

		try {
			File f = new File(filenameWithPath + ".png");
			if(f.exists()) f.delete();

			ChartUtilities.saveChartAsPNG(xyChart, xylineChart, width, height);
			datasetLineChart.removeAllSeries();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	/**
	 * This method allows to create plots whit no legend. 
	 * 
	 * @param x
	 * @param y
	 * @param filenameWithPath
	 * @param title
	 * @param legend
	 * @param xLabel
	 * @param yLabel
	 * @param width
	 * @param height
	 */
	
	public void createPNGNoLegend(
			String filenameWithPath,
			String title,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			int width, int height) {

		if (!xUnit.equals("")) xUnit = "(" + xUnit + ")"; 
		if (!yUnit.equals("")) yUnit = "(" + yUnit + ")";

		PlotOrientation orientation;
		if (swapXY) orientation = PlotOrientation.HORIZONTAL;
		else orientation = PlotOrientation.VERTICAL;

		JFreeChart xylineChart = ChartFactory.createXYLineChart(
				title, 
				xLabel + " " + xUnit,
				yLabel + " " + yUnit, 
				datasetLineChart,
				orientation, 
				true, true, false);

		xylineChart.setBackgroundPaint(Color.WHITE);
		xylineChart.setBackgroundImageAlpha(0.0f);
		xylineChart.setAntiAlias(true);

		xylineChart.getPlot().setBackgroundPaint(Color.WHITE);
		xylineChart.getPlot().setBackgroundAlpha(0.0f);
		//		xylineChart.getXYPlot().getRenderer().setSeriesPaint(seriesIndex, color);
		xylineChart.getXYPlot().setDomainGridlinesVisible(true);
		xylineChart.getXYPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);
		xylineChart.getXYPlot().setRangeGridlinesVisible(true);
		xylineChart.getXYPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);
		xylineChart.removeLegend(); 

		final Paint[] paintArray;
		// create default colors but modify some colors that are hard to see
		paintArray = ChartColor.createDefaultPaintArray();
		paintArray[0] = ChartColor.BLACK;
		paintArray[1] = ChartColor.BLUE;
		paintArray[2] = ChartColor.RED;
		paintArray[3] = ChartColor.DARK_GREEN;
		paintArray[4] = ChartColor.DARK_YELLOW;
		paintArray[5] = ChartColor.DARK_GRAY;
		paintArray[6] = ChartColor.DARK_BLUE;
		paintArray[7] = ChartColor.DARK_RED;
		paintArray[8] = ChartColor.VERY_DARK_GREEN;
		paintArray[9] = ChartColor.ORANGE;
		
		XYPlot plot = (XYPlot) xylineChart.getPlot(); 
		plot.setDrawingSupplier(new DefaultDrawingSupplier(
                paintArray,
                DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
		
		NumberAxis domain = (NumberAxis) xylineChart.getXYPlot().getDomainAxis();
		domain.setRange(xMin, xMax);
		//        domain.setTickUnit(new NumberTickUnit(0.1));
		//        domain.setVerticalTickLabels(true);
		NumberAxis range = (NumberAxis) xylineChart.getXYPlot().getRangeAxis();
		range.setRange(yMin, yMax);
		//        range.setTickUnit(new NumberTickUnit(0.1));

		File XYChart = new File(filenameWithPath + ".png"); 

		try {
			File f = new File(filenameWithPath + ".png");
			if(f.exists()) f.delete();

			ChartUtilities.saveChartAsPNG(XYChart, xylineChart, width, height);
			datasetLineChart.removeAllSeries();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param filenameWithPath
	 * @param title
	 * @param dataset
	 * @param width
	 * @param height
	 */
	public void createPieChartPNG(
			String filenameWithPath,
			String title,
			DefaultPieDataset dataset,
			int width, int height) {

		JFreeChart chart = ChartFactory.createPieChart(
				title,  // chart title
				dataset,             // data
				false,               // include legend
				true,
				false
				);

		PiePlot plot = (PiePlot) chart.getPlot();
		//		        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		plot.setNoDataMessage("No data available");
		plot.setCircular(true);
		plot.setLabelGap(0.02);

		chart.setBackgroundPaint(Color.WHITE);
		chart.setBackgroundImageAlpha(0.0f);
		chart.setAntiAlias(true);

		chart.getPlot().setBackgroundPaint(Color.WHITE);
		chart.getPlot().setBackgroundAlpha(0.0f);

		final Paint[] paintArray;
		// create default colors but modify some colors that are hard to see
		paintArray = ChartColor.createDefaultPaintArray();
		paintArray[0] = ChartColor.BLACK;
		paintArray[1] = ChartColor.BLUE;
		paintArray[2] = ChartColor.RED;
		paintArray[3] = ChartColor.DARK_GREEN;
		paintArray[4] = ChartColor.DARK_YELLOW;
		paintArray[5] = ChartColor.DARK_GRAY;
		paintArray[6] = ChartColor.DARK_BLUE;
		paintArray[7] = ChartColor.DARK_RED;
		paintArray[8] = ChartColor.VERY_DARK_GREEN;
		paintArray[9] = ChartColor.ORANGE;
		
		chart.getPlot().setDrawingSupplier(new DefaultDrawingSupplier(
                paintArray,
                DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
		
		File chartFile = new File(filenameWithPath + ".png"); 

		try {
			File f = new File(filenameWithPath + ".png");
			if(f.exists()) f.delete();
			ChartUtilities.saveChartAsPNG(chartFile, chart, width, height);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a graph from a table object.
	 * This method creates both the tikz file and
	 * the png file.
	 * 
	 * @param xValues
	 * @param entries
	 * @param fileName
	 * @param ymin
	 * @param ymax
	 * @param xLabel
	 * @param yLabel
	 * @param legendName
	 */
	public void createGraphFromTable(
			double[] xValues,
			Object entries,
			String path,
			String fileName,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String legendName,
			boolean png) {

		createGraphFromTable(
				xValues,
				entries,
				path,
				fileName,
				xLabel, yLabel,
				xUnit, yUnit,
				legendName,
				"", "",
				png);

	}

	/**
	 * @author Lorenzo Attanasio
	 * @param xValues
	 * @param entries
	 * @param path
	 * @param fileName
	 * @param xLabel
	 * @param yLabel
	 * @param xUnit
	 * @param yUnit
	 * @param legendName
	 * @param style
	 * @param color
	 * @param png
	 */
	public void createGraphFromTable(
			double[] xValues,
			Object entries,
			String path,
			String fileName,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String legendName,
			String style, String color,
			boolean png) {

		Table<Object, Object, Object> map;

		if (entries instanceof TreeBasedTable) {
			map = (TreeBasedTable<Object, Object, Object>) entries;
		} else {
			map = (HashBasedTable<Object, Object, Object>) entries;
		}

		// Loop over methods
		for (Entry<Object, Map<Object, Object>> m : map.rowMap().entrySet()) {

			traceCounter = 0;
			styleIdx = 0;
			colorIdx = 0;
			
			Map<Object, Object> innerMap = m.getValue();
			if (m.getKey() instanceof MethodEnum) {
				initializeTikz(
						path + fileName + 
						WordUtils.capitalizeFully(m.getKey().toString()),
						setMin(MyArrayUtils.getMin(xValues)), setMax(MyArrayUtils.getMax(xValues)),
						setMin(MyMapUtils.getAbsoluteMin(map)), setMax(MyMapUtils.getAbsoluteMax(map)),
						xLabel, yLabel,
						xUnit, yUnit);
			}

			// Loop over alphas
			for (Entry<Object, Object > mm : innerMap.entrySet()){
				if (mm.getKey() instanceof Amount
						&& mm.getValue() instanceof MyArray) {
					
					System.out.println("Alpha map: "+ mm.getKey().toString());
					
					Amount<?> amount = (Amount<?>) mm.getKey();
					MyArray myArray = (MyArray) mm.getValue();

					if (amount.getUnit().equals(SI.RADIAN))
						amount = amount.to(NonSI.DEGREE_ANGLE).copy();

					addTraceToTikz(
							xValues,
							myArray.toArray(),
							legendName, amount.getEstimatedValue(),
							amount.getUnit().toString(), style, color, true);

					if (png == true)
						addTraceToPng(xValues,
								myArray.toArray(),
								legendName + " = " 
										+ BigDecimal.valueOf(
												(amount.getEstimatedValue())).round(new MathContext(6)));
				} 
			}

			closeTikz();

			if (png == true)
				createPNG(
						path + fileName + 
						WordUtils.capitalizeFully(m.getKey().toString()),
						" ",
						xLabel, yLabel,
						xUnit, yUnit,
						width, height);
		}
	}


	/**
	 * @author Lorenzo Attanasio
	 * @param xValues
	 * @param map
	 * @param path
	 * @param fileName
	 * @param xLabel
	 * @param yLabel
	 * @param xUnit
	 * @param yUnit
	 * @param legendName
	 * @param swapXY
	 */
	public void createTikzFromMap(
			Double[] xValues,
			Map map,
			String path,
			String fileName,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String legendName,
			boolean swapXY) {

		traceCounter = 0;
		styleIdx = 0;
		colorIdx = 0;

		Map<Object, Object> entries = (Map<Object, Object>) map;

		if (swapXY)
			initializeTikz(
					path + fileName,
					setMin(MyMapUtils.getAbsoluteMin(entries)), setMax(MyMapUtils.getAbsoluteMax(entries)),
					setMin(MyArrayUtils.getMin(xValues)), setMax(MyArrayUtils.getMax(xValues)),
					xLabel, yLabel,
					xUnit, yUnit);
		else
			initializeTikz(
					path + fileName,
					setMin(MyArrayUtils.getMin(xValues)), setMax(MyArrayUtils.getMax(xValues)),
					setMin(MyMapUtils.getAbsoluteMin(entries)), setMax(MyMapUtils.getAbsoluteMax(entries)),
					xLabel, yLabel,
					xUnit, yUnit);

		for (Map.Entry<Object, Object> entry : entries.entrySet()){

			if (entry.getKey() instanceof Amount
					&& entry.getValue() instanceof MyArray) {

				Amount<?> amount = (Amount<?>) entry.getKey();
				MyArray myArray = (MyArray) entry.getValue();

				if (amount.getUnit().equals(SI.RADIAN))
					amount = amount.to(NonSI.DEGREE_ANGLE).copy();

				addTraceToTikz(
						ArrayUtils.toPrimitive(xValues),
						myArray.toArray(),
						legendName, amount.getEstimatedValue(),
						amount.getUnit().toString());

			} else if (entry.getValue() instanceof Double[]) { 

				if(swapXY)
					addTraceToTikz(
							(Double[]) entry.getValue(),
							xValues,
							legendName, (Double) entry.getKey(),
							"");
				else
					addTraceToTikz(
							xValues,
							(Double[]) entry.getValue(),
							legendName, (Double) entry.getKey(),
							"");
			}

		}

		closeTikz();
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param xValues
	 * @param entries
	 * @param path
	 * @param fileName
	 * @param xLabel
	 * @param yLabel
	 * @param xUnit
	 * @param yUnit
	 * @param legendName
	 */
	public void createTikzFromTable(
			double[] xValues,
			Object entries,
			String path,
			String fileName,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String legendName) {

		Table<Object, Object, Object> map;

		if (entries instanceof TreeBasedTable) {
			map = (TreeBasedTable<Object, Object, Object>) entries;
		} else {
			map = (HashBasedTable<Object, Object, Object>) entries;
		}

		// Loop over methods
		for (Entry<Object, Map<Object, Object>> m : map.rowMap().entrySet()) {

			traceCounter = 0;
			styleIdx = 0;
			colorIdx = 0;

			Map<Object, Object> innerMap = m.getValue();
			if (m.getKey() instanceof MethodEnum) {
				initializeTikz(
						path + fileName + 
						WordUtils.capitalizeFully(m.getKey().toString()),
						setMin(MyArrayUtils.getMin(xValues)), setMax(MyArrayUtils.getMax(xValues)),
						setMin(MyMapUtils.getAbsoluteMin(map)), setMax(MyMapUtils.getAbsoluteMax(map)),
						xLabel, yLabel,
						xUnit, yUnit);
			}

			// Loop over alphas
			for (Entry<Object, Object > mm : innerMap.entrySet()){
				if (mm.getKey() instanceof Amount
						&& mm.getValue() instanceof MyArray) {

					Amount<?> amount = (Amount<?>) mm.getKey();
					MyArray myArray = (MyArray) mm.getValue();

					if (amount.getUnit().equals(SI.RADIAN))
						amount = amount.to(NonSI.DEGREE_ANGLE).copy();

					addTraceToTikz(
							xValues,
							myArray.toArray(),
							legendName, amount.getEstimatedValue(),
							amount.getUnit().toString());
				} 
			}

			closeTikz();
		}
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param xValues
	 * @param entries
	 * @param path
	 * @param fileName
	 * @param xLabel
	 * @param yLabel
	 * @param xUnit
	 * @param yUnit
	 * @param legendName
	 */
	public void createPngFromTable(
			double[] xValues,
			Object entries,
			String path,
			String fileName,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String legendName) {

		Table<Object, Object, Object> map;

		if (entries instanceof TreeBasedTable) {
			map = (TreeBasedTable<Object, Object, Object>) entries;
		} else {
			map = (HashBasedTable<Object, Object, Object>) entries;
		}

		// Loop over methods
		for (Entry<Object, Map<Object, Object>> m : map.rowMap().entrySet()) {

			traceCounter = 0;
			styleIdx = 0;
			colorIdx = 0;

			Map<Object, Object> innerMap = m.getValue();

			// Loop over alphas
			for (Entry<Object, Object > mm : innerMap.entrySet()){
				if (mm.getKey() instanceof Amount
						&& mm.getValue() instanceof MyArray) {

					Amount<?> amount = (Amount<?>) mm.getKey();
					MyArray myArray = (MyArray) mm.getValue();

					if (amount.getUnit().equals(SI.RADIAN))
						amount = amount.to(NonSI.DEGREE_ANGLE).copy();

					addTraceToPng(xValues,
							myArray.toArray(),
							legendName + " = " 
									+ BigDecimal.valueOf(
											(amount.getEstimatedValue())).round(new MathContext(3)));
				} 
			}

			createPNG(
					path + fileName + 
					WordUtils.capitalizeFully(m.getKey().toString()),
					" ",
					xLabel, yLabel,
					xUnit, yUnit,
					width, height);
		}
	}

	/**
	 * Create a chart representing three different functions
	 * 
	 * @author Lorenzo Attanasio
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @param path
	 * @param fileName
	 * @param xLabel
	 * @param yLabel
	 * @param xUnit
	 * @param yUnit
	 * @param legendName
	 * @param legendValue
	 * @param legendUnit
	 * @param swapXY
	 */
	public void createMultiTraceChart(
			double[] x1, double[] y1,
			double[] x2, double[] y2,
			double[] x3, double[] y3,
			String path,
			String fileName,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String legendName, double[] legendValue,
			String legendUnit,
			boolean swapXY) {

//		createMultiTraceTikz(x1, y1, x2, y2, x3, y3,
//				path, fileName, 
//				legendName, legendValue, legendUnit, 
//				xLabel, yLabel, xUnit, yUnit);

		createMultiTracePng(x1, y1, x2, y2, x3, y3, 
				path, fileName,
				legendName, legendValue, legendUnit, 
				xLabel, yLabel, xUnit, yUnit);
	}

	/**
	 * Create a chart representing n different functions.
	 * x and y arrays MUST be initialized through their setters
	 * 
	 * @param path
	 * @param fileName
	 * @param xLabel
	 * @param yLabel
	 * @param xUnit
	 * @param yUnit
	 * @param legendName
	 * @param legendValue
	 * @param legendUnit
	 * @param stripTrailingZeros TODO
	 */
	
	// Creates plot with double array as legend value
	public void createMultiTraceChart(
			String path, String fileName,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String legendName, double[] legendValue,
			String legendUnit,
			boolean stripTrailingZeros) {

//		createMultiTraceTikz(
//				path, fileName, 
//				legendName, legendValue, legendUnit, 
//				xLabel, yLabel, xUnit, yUnit);

		createMultiTracePng(
				path, fileName,
				legendName, legendValue, legendUnit, 
				xLabel, yLabel, xUnit, yUnit, stripTrailingZeros);
	}

	// Creates plot with String array as legend value
	// @author Manuela Ruocco
	
	public void createMultiTraceChart(
			String path, String fileName,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			String[] legendValue,
			boolean stripTrailingZeros) {

//		createMultiTraceTikz(
//				path, fileName, 
//				legendValue, 
//				xLabel, yLabel, xUnit, yUnit);

		createMultiTracePng(
				path, fileName,
				legendValue,
				xLabel, yLabel, xUnit, yUnit, stripTrailingZeros);
	}
	


	public void createMultiTraceChart(boolean stripTrailingZeros) {
		createMultiTraceChart(path, fileName, 
				xLabel, yLabel, xUnit, yUnit, 
				legendName, legendValue, legendUnit, stripTrailingZeros);
	}

	// Creates plot whit no legend. 
		// @author Manuela Ruocco
		
	public void createMultiTraceChartNoLegend(
			String path, String fileName,
			String xLabel, String yLabel,
			String xUnit, String yUnit,
			double[] legend,
			boolean stripTrailingZeros) {


//		createSingleTraceTikz(
//				path, fileName, 
//				xLabel, yLabel, xUnit, yUnit);

		createSingleTracePngNoLegend(
				path, fileName,legend,
				xLabel, yLabel, xUnit, yUnit, stripTrailingZeros);
	}

	public void createMultiTraceChart() {

		if (legendValueString != null){   // legend as Sring array 
			createMultiTraceChart(path, fileName, 
					xLabel, yLabel, xUnit, yUnit, legendValueString,
					stripTrailingZeros);
		}
		else if ((legendValue != null)){	// legend as double array
			createMultiTraceChart(path, fileName, 
					xLabel, yLabel, xUnit, yUnit,legendName, 
					legendValue, legendUnit, stripTrailingZeros);
		}}

	
	public void createMultiTraceChartNoLegend() {
		createMultiTraceChartNoLegend(path, fileName, 
				xLabel, yLabel, xUnit, yUnit, 
				legendValue, stripTrailingZeros);
	}
	/**
	 * Set the arrays that contain the x values
	 * 
	 * @param x
	 */
	public void setXarrays(double[] ... x) {
		if (xArrays == null)
			xArrays = new Array2DRowRealMatrix(x);
		else 
			xArrays = new Array2DRowRealMatrix(MyArrayUtils.concatMatricesVertically(xArrays.getData(), x));
		numberOfXarrays = xArrays.getRowDimension();
		xMin = setMin(MyArrayUtils.getMin(xArrays.getData()));
		xMax = setMax(MyArrayUtils.getMax(xArrays.getData()));
	}

	/**
	 * Set the arrays that contain the y values
	 * 
	 * @param y
	 */
	public void setYarrays(double[] ... y) {
		if (yArrays == null)
			yArrays = new Array2DRowRealMatrix(y);
		else 
			yArrays = new Array2DRowRealMatrix(MyArrayUtils.concatMatricesVertically(yArrays.getData(), y));
		numberOfYarrays = yArrays.getRowDimension();
		yMin = setMin(MyArrayUtils.getMin(yArrays.getData()));
		yMax = setMax(MyArrayUtils.getMax(yArrays.getData()));
	}

	// TODO: document me
	public void setXvectors(double[] x) {
		if (xVectors == null) {
			xVectors = new ArrayList<ArrayRealVector>();
			xVectors.add(new ArrayRealVector(x));
		}
		else 
			xVectors.add(new ArrayRealVector(x));
		
		xMin = setMin(MyArrayUtils.getMin(x));
		xMax = setMax(MyArrayUtils.getMax(x));
		
//		System.out.println("\n\n---------- xVectors");
//		System.out.println(xVectors.get(xVectors.size()-1));
//		
//		System.out.println("\n\n----------");
		
	}

	// TODO: document me
	public void setYvectors(double[] y) {
		if (yVectors == null) {
			yVectors = new ArrayList<ArrayRealVector>();
			yVectors.add(new ArrayRealVector(y));
		}
		else 
			yVectors.add(new ArrayRealVector(y));
		
		yMin = setMin(MyArrayUtils.getMin(y));
		yMax = setMax(MyArrayUtils.getMax(y));
		
//		System.out.println("\n\n---------- yVectors");
//		System.out.println(yVectors.get(yVectors.size()-1));
//		
//		System.out.println("\n\n----------");
		
		
	}
	
	
	
	private static double setMinRatio(double min, double ratio) {
		if (min<0) return min*(1+ratio);
		if (Math.abs(min) < 1e-1) return 0.0;
		return min*(1-ratio);
	}

	private static double setMaxRatio(double max, double ratio) {
		if (max<0) return max*(1-ratio);
		if (Math.abs(max) < 1e-2) return 0.0;
		return max*(1+ratio);
	}

	public static double setMin(double min) {
		return setMinRatio(min, 0.08);
	}

	public static double setMax(double max) {
		return setMaxRatio(max, 0.08);
	}

	public void set1440x960() {
		width = 1440;
		height = 960;
	}
	
	public void set1440x900() {
		width = 1440;
		height = 900;
	}
	
	public void set1600x900() {
		width = 1600;
		height = 900;
	}
	
	public void set1280x960() {
		width = 1280;
		height = 960;
	}
	
	public void set1280x720() {
		width = 1280;
		height = 720;
	}
	
	public void set1152x768() {
		width = 1152;
		height = 768;
	}

	public void set1920x1080() {
		width = 1920;
		height = 1080;
	}

	public void set1366x768() {
		width = 1366;
		height = 768;
	}

	public void set800x600() {
		width = 800;
		height = 600;
	}

	public PrintWriter getWriter() {
		return writer;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public double getxMin() {
		return xMin;
	}

	public void setxMin(double xMin) {
		this.xMin = xMin;
	}

	public double getxMax() {
		return xMax;
	}

	public void setxMax(double xMax) {
		this.xMax = xMax;
	}

	public double getyMin() {
		return yMin;
	}

	public void setyMin(double yMin) {
		this.yMin = yMin;
	}

	public double getyMax() {
		return yMax;
	}

	public void setyMax(double yMax) {
		this.yMax = yMax;
	}

	public boolean isSwapXY() {
		return swapXY;
	}

	public void setSwapXY(boolean swapXY) {
		this.swapXY = swapXY;
	}

	public boolean isStripTrailingZeros() {
		return stripTrailingZeros;
	}

	public void setStripTrailingZeros(boolean stripTrailingZeros) {
		this.stripTrailingZeros = stripTrailingZeros;
	}

	//
	//	public static void writeSvg(
	//			String format,
	//			int width, int height, 
	//			JFreeChart lineChartObject,
	//			File lineChart
	//			) {
	//
	//		/* Define the data range for SVG Pie Chart */
	//		//        DefaultPieDataset mySvgPieChartData = new DefaultPieDataset();                
	//		//        mySvgPieChartData.setValue("JFreeChart", 77);
	//		//        mySvgPieChartData.setValue("Batik", 80);
	//		//        mySvgPieChartData.setValue("Chart", 55);
	//		//        mySvgPieChartData.setValue("Apache", 67);
	//		//        mySvgPieChartData.setValue("Java", 80);
	//		//        /* This method returns a JFreeChart object back to us */                                
	//		//        JFreeChart myPieChart=ChartFactory.createPieChart("JFreeChart - SVG Pie Chart Example",mySvgPieChartData,true,true,false);
	//		//        /* Our logical Pie chart is ready at this step. We can now write the chart as SVG using Batik */
	//		//        /* Get DOM Implementation */
	//		//        DOMImplementation mySVGDOM= GenericDOMImplementation.getDOMImplementation();
	//		//        /* create Document object */
	//		//        Document document = mySVGDOM.createDocument(null, "svg", null);
	//		//        /* Create SVG Generator */
	//		//        SVGGraphics2D my_svg_generator = new SVGGraphics2D(document);
	//		//        /* Render chart as SVG 2D Graphics object */
	//		//        myPieChart.draw(my_svg_generator, new Rectangle2D.Double(0, 0, 640, 480), null);
	//		//        /* Write output to file */
	//		//        my_svg_generator..stream("output_pie_chart.svg");    
	//
	//		try {
	//			ChartUtilities.saveChartAsJPEG(lineChart ,lineChartObject, width ,height);
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//
	//	}
	//
}
