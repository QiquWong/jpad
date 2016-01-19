package sandbox.adm;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.jfree.fx.FXGraphics2D;
import org.scilab.forge.jlatexmath.Box;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import com.emxsys.chart.EnhancedLineChart;
import com.emxsys.chart.extension.XYAnnotations.Layer;
import com.emxsys.chart.extension.XYLineAnnotation;
import com.emxsys.chart.extension.XYTextAnnotation;
import com.emxsys.chart.extension.SubtitleExtension;
import com.emxsys.chart.extension.AnnotationExtension;
import com.emxsys.chart.extension.LogarithmicAxis;
import com.emxsys.chart.extension.MarkerExtension;
import com.emxsys.chart.extension.SubtitleExtension;
import com.emxsys.chart.extension.ValueMarker;
import com.emxsys.chart.extension.XYAnnotations.Layer;
import com.emxsys.chart.extension.XYImageAnnotation;
import com.emxsys.chart.extension.XYLineAnnotation;
import com.emxsys.chart.extension.XYPolygonAnnotation;
import com.emxsys.chart.extension.XYTextAnnotation;


import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import jmatrix.Matrix;

public class JavaFXTest_03_LineChart extends Application {

	@Override
	public void start(Stage stage) throws Exception {

		stage.setTitle("Agodemar: Line Chart Sample");
		
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("X-axis");
        yAxis.setLabel("Y-axis");
        
        //creating the chart
        // final LineChart lineChart = new LineChart(xAxis,yAxis);
        final EnhancedLineChart lineChart = new EnhancedLineChart(xAxis,yAxis);
        
		// produce data with a custom function
		ObservableList<XYChart.Series<Double, Double>> chartData = getChartData();

		// assign data to the chart object
		lineChart.setData(chartData);
		
		// Extracting min, max on data set 
		Double minX = chartData.get(0).getData().stream()
			.map(data -> data.getXValue())
			.min(Comparator.comparing(num -> num)).get();

		Double maxX = chartData.get(0).getData().stream()
				.map(data -> data.getXValue())
				.max(Comparator.comparing(num -> num)).get();
		
		System.out.println("TestFXLineChartPane >>>>>> min X: " + minX);
		System.out.println("TestFXLineChartPane >>>>>> max X: " + maxX);
		
//		// axis settings
//		xAxis.setAutoRanging(false);
//		xAxis.setLowerBound(minX);
//		xAxis.setUpperBound(maxX);
//		xAxis.setTickUnit(0.5);
		
		// title
		lineChart.setTitle("Example of curve (Double -> Double)");
		lineChart.setLegendSide(Side.RIGHT);

		//----------------
		// Experimental
		
		((SubtitleExtension) lineChart).addSubtitle("First Subtitle");
		((SubtitleExtension) lineChart).addSubtitle("Another Subtitle");
		// or
		// ((SubtitleExtension) lineChart).clearSubtitles();

		// not working
        ValueAxis xAxis0 = (ValueAxis) lineChart.getXAxis();
        ValueAxis yAxis0 = (ValueAxis) lineChart.getYAxis();

        ((AnnotationExtension) lineChart).getAnnotations().add(new XYTextAnnotation(
                "Text on Background", xAxis0.getLowerBound(), yAxis0.getUpperBound(), Pos.TOP_LEFT),
                Layer.BACKGROUND);

        ((AnnotationExtension) lineChart).getAnnotations().add(new XYTextAnnotation(
                "Text on Foreground", xAxis0.getUpperBound(), yAxis0.getLowerBound(), Pos.BOTTOM_RIGHT),
                Layer.FOREGROUND);

        lineChart.requestLayout(); // resolve layout pass
		

		
		
		// not working
		ValueAxis xAxis1 = (ValueAxis) lineChart.getXAxis();
        ValueAxis yAxis1 = (ValueAxis) lineChart.getYAxis();
        ((AnnotationExtension) lineChart).getAnnotations().add(new XYLineAnnotation(
                xAxis1.getUpperBound(), yAxis1.getLowerBound(),
                xAxis1.getLowerBound(), yAxis1.getUpperBound(),
                2.0,
                Color.RED),
                Layer.BACKGROUND);
        lineChart.requestLayout(); // resolve layout pass

        
        
        XYChart.Series series1 = (XYChart.Series) lineChart.getData().get(0);
        ObservableList data = series1.getData();
        double minX0 = Double.MAX_VALUE;
        double minY0 = Double.MAX_VALUE;
        double maxX0 = Double.MIN_VALUE;
        double maxY0 = Double.MIN_VALUE;
        double avgX = 0;
        double avgY = 0;
        double totalX = 0;
        double totalY = 0;
        int numItems = 0;

        for (Iterator it = data.iterator(); it.hasNext();) {
            XYChart.Data xy = (XYChart.Data) it.next();
            double x = (double) xy.getXValue();
            double y = (double) xy.getYValue();
            minX0 = Math.min(x, minX0);
            minY0 = Math.min(y, minY0);
            maxX0 = Math.max(x, maxX0);
            maxY0 = Math.max(y, maxY0);
            totalX += x;
            totalY += y;
            numItems++;
        }
        avgX = totalX / numItems;
        avgY = totalY / numItems;

        ((MarkerExtension) lineChart).getMarkers().addRangeMarker(new ValueMarker(maxY0, String.format("Series 1 Max: %1$.1f", maxY0), Pos.TOP_RIGHT));
        ((MarkerExtension) lineChart).getMarkers().addRangeMarker(new ValueMarker(avgY, String.format("Series 1 Avg: %1$.1f", avgY), Pos.TOP_CENTER));
        ((MarkerExtension) lineChart).getMarkers().addRangeMarker(new ValueMarker(minY0, String.format("Series 1 Min: %1$.1f", minY0), Pos.BOTTOM_LEFT));
        ((MarkerExtension) lineChart).getMarkers().addDomainMarker(new ValueMarker(3, "Fixed", Pos.BOTTOM_RIGHT));        
        
        
		//----------------
		
		
		final StackPane stackPaneWithChart = new StackPane();
		
		// Load the right font for LaTeX formulas
		// TODO: try FXGraphics2D
		javafx.scene.text.Font.loadFont(TeXFormula.class.getResourceAsStream("/org/scilab/forge/jlatexmath/fonts/base/jlm_cmmi10.ttf"), 1);
		javafx.scene.text.Font.loadFont(TeXFormula.class.getResourceAsStream("/org/scilab/forge/jlatexmath/fonts/maths/jlm_cmsy10.ttf"), 1);
		javafx.scene.text.Font.loadFont(TeXFormula.class.getResourceAsStream("/org/scilab/forge/jlatexmath/fonts/latin/jlm_cmr10.ttf"), 1);
		
		MyCanvas labelA = new MyCanvas(
				"x=\\frac{-b \\pm \\sqrt {b^2-4ac}}{2a}",
				25 // dimension
				);
		
		// a container for the chart object
		VBox vBoxWithChart = new VBox();
        vBoxWithChart.getChildren().add(lineChart);
        VBox.setVgrow(lineChart, Priority.ALWAYS);
		stackPaneWithChart.getChildren().add(vBoxWithChart);
		stackPaneWithChart.getChildren().add(labelA);

        // Bind canvas size to stack pane size. 
        labelA.widthProperty().bind( stackPaneWithChart.widthProperty() ); 
        labelA.heightProperty().bind( stackPaneWithChart.heightProperty() );  
	
        // ---------------
        // experimental
        
		// create a formula
//		TeXFormula formula = new TeXFormula("\\sqrt{x}");
//		TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);

        
        // ---------------
        
		final Group rootGroup = new Group();

        // add the main container to the root node
        rootGroup.getChildren().add(stackPaneWithChart);

        
        Scene scene  = new Scene(rootGroup,800,600);
        
		//---------------------------------------------------------
		// Chart initial dimensions
		lineChart.setPrefWidth(
				scene.getWidth()*1.0
				);
		lineChart.setPrefHeight(
				scene.getHeight()*1.0
				);

		
		//---------------------------------------------------------
		// SCENE LISTENERS
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                // System.out.println("Width: " + newSceneWidth);
                lineChart.setPrefWidth(
                		scene.getWidth()*1.0
                		);
                lineChart.setMaxWidth(
                		scene.getWidth()*1.0
                		);                	
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
            	// System.out.println("Height: " + newSceneHeight);
        		lineChart.setPrefHeight(
        				scene.getHeight()*1.0
        				);
        		lineChart.setMaxHeight(
        				scene.getHeight()*1.0
        				);
            }
        });        
		//---------------------------------------------------------
        
        stage.setScene(scene);
        stage.show();
        
        // TODO: see
        // https://bitbucket.org/emxsys/javafx-chart-extensions/wiki/XYAnnotation%20Extension
        
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	private ObservableList<XYChart.Series<Double, Double>> getChartData() {
		
		double value0 = 17.56;
		double value1 = 17.06;
		double value2 = 8.25;

		ObservableList<XYChart.Series<Double, Double>> result = FXCollections.observableArrayList();
		Series<Double, Double> series0 = new Series<>();
		series0.setName("Curve 0");
		Series<Double, Double> series1 = new Series<>();
		series1.setName("Curve 1");
		Series<Double, Double> series2 = new Series<>();
		series2.setName("Curve 2");
		
		// generate x-values
		List<Double> xList = Arrays.asList(ArrayUtils.toObject(
				Matrix.linspace(
						0.0, 2.5, 
						25
						).data
				));
		// Generate Data in objects of type XYChart and populate series
		for (Double x : xList) {
			series0.getData().add(new XYChart.Data(x, value0));
			value0 = value0 + 4 * Math.random() - 2;
			series1.getData().add(new XYChart.Data(x, value1));
			value1 = value1 + Math.random() - .5;
			series2.getData().add(new XYChart.Data(x, value2));
			value2 = value2 + 4 * Math.random() - 2;
		}
		// populate result
		result.addAll(series0, series1, series2);
		// and return
		return result;
	}
	
	// TODO: code from FXGraphics2D
	// http://stackoverflow.com/questions/25027060/running-swing-application-in-javafx/25037747#25037747

	static class MyCanvas extends Canvas { 

		private FXGraphics2D g2;
		private Box box;
		private String latexMathString = "x"; 

		public MyCanvas(String str, int dim) {
			this.latexMathString = str;
			this.g2 = new FXGraphics2D(getGraphicsContext2D());
			this.g2.scale(dim, dim); // dimensions of the box

			// create a formula
			TeXFormula formula = new TeXFormula(latexMathString);
			TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);

			// the 'Box' seems to be the thing we can draw directly to Graphics2D
			this.box = icon.getBox();

			// Redraw canvas when size changes. 
			widthProperty().addListener(evt -> draw()); 
			heightProperty().addListener(evt -> draw()); 
		}  

		private void draw() { 
			double width = getWidth(); 
			double height = getHeight();
			getGraphicsContext2D().clearRect(0, 0, width, height);
			this.box.draw(g2, 1, 5);
		} 

		@Override 
		public boolean isResizable() { 
			return true;
		}  

		@Override 
		public double prefWidth(double height) { return getWidth(); }  

		@Override 
		public double prefHeight(double width) { return getHeight(); } 
		
	}// end-of-class MyCanvas

}
