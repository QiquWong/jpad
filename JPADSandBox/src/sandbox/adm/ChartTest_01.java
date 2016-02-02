package sandbox.adm;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.fx.FXGraphics2D;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import jmatrix.Matrix;
import sandbox.adm.FXGraphics2DDemo1.ChartCanvas;

public class ChartTest_01 extends Application {

	static class ChartCanvas extends Canvas {

		JFreeChart chart;

		private FXGraphics2D g2;

		public ChartCanvas(JFreeChart chart) {
			this.chart = chart;
			this.g2 = new FXGraphics2D(getGraphicsContext2D());
			// Redraw canvas when size changes.
			widthProperty().addListener(e -> draw());
			heightProperty().addListener(e -> draw());
		}

		private void draw() {
			double width = getWidth();
			double height = getHeight();
			getGraphicsContext2D().clearRect(0, 0, width, height);
			this.chart.draw(this.g2, new Rectangle2D.Double(0, 0, width,
					height));
		}

		@Override
		public boolean isResizable() {
			return true;
		}

		@Override
		public double prefWidth(double height) { return getWidth(); }

		@Override
		public double prefHeight(double width) { return getHeight(); }
	}

	/**
	 * Creates a chart.
	 *
	 * @param dataset  a dataset.
	 *
	 * @return A chart.
	 */
	private static JFreeChart createChart(XYDataset dataset) {

		JFreeChart chart = ChartFactory.createXYLineChart(
				"JPAD, chart test",    // title
				"x", // x-axis label
				"y", // y-axis label
				dataset);

		String fontName = "Arial";
		chart.getTitle().setFont(new Font(fontName, Font.BOLD, 18));
		chart.addSubtitle(new TextTitle("See JPAD project on Github https://github.com/Aircraft-Design-UniNa/jpad/wiki", new Font(fontName, Font.PLAIN, 14)));

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setDomainPannable(true);
		plot.setRangePannable(false);
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		plot.getDomainAxis().setLowerMargin(0.0);
		plot.getDomainAxis().setLabelFont(new Font(fontName, Font.BOLD, 14));
		plot.getDomainAxis().setTickLabelFont(new Font(fontName, Font.PLAIN, 12));
		plot.getRangeAxis().setLabelFont(new Font(fontName, Font.BOLD, 14));
		plot.getRangeAxis().setTickLabelFont(new Font(fontName, Font.PLAIN, 12));
		chart.getLegend().setItemFont(new Font(fontName, Font.PLAIN, 14));
		chart.getLegend().setFrame(BlockBorder.NONE);
		//chart.getLegend().setHorizontalAlignment(HorizontalAlignment.CENTER);


		// Set min/max
		NumberAxis rangeX = (NumberAxis) plot.getDomainAxis();
        rangeX.setRange(-0.50, 1.50);
		NumberAxis rangeY = (NumberAxis) chart.getXYPlot().getRangeAxis();
		rangeY.setRange(-1, 2);
		// rangeY.setTickUnit(new NumberTickUnit(0.1));

		System.out.println("---> " + dataset.getItemCount(0));



		XYItemRenderer r = plot.getRenderer();
		if (r instanceof XYLineAndShapeRenderer) {

			System.out.println("---> XYLineAndShapeRenderer");


			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setBaseShapesVisible(false);
			renderer.setDrawSeriesLineAsPath(true);
			// set the default stroke for all series
			renderer.setAutoPopulateSeriesStroke(false);
			renderer.setBaseStroke(new BasicStroke(3.0f,
					BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL), false);

			renderer.setSeriesPaint(0, Color.RED);
			// TODO ??
			//            renderer.setSeriesPaint(1, new Color(24, 123, 58));
			//            renderer.setSeriesPaint(2, new Color(149, 201, 136));
			//            renderer.setSeriesPaint(3, new Color(1, 62, 29));
			//            renderer.setSeriesPaint(4, new Color(81, 176, 86));
			//            renderer.setSeriesPaint(5, new Color(0, 55, 122));
			//            renderer.setSeriesPaint(6, new Color(0, 92, 165));
			renderer.setBaseLegendTextFont(new Font(fontName, Font.PLAIN, 14));
		}

		return chart;

	}


	/**
	 * Creates a dataset, consisting of two series of monthly data.
	 *
	 * @return the dataset.
	 */
	private static XYDataset createDataset() {

		// generate x-values
		List<Double> x0 = Arrays.asList(ArrayUtils.toObject(
				Matrix.linspace(0.0, 1, 20).data
				));

		List<Pair<Double,Double>> xy0 =
				x0.stream()
				.map(x -> new Pair(x, x*x))
				.collect(Collectors.toList());
		XYSeries xyS0 = new XYSeries("xy0");
		for(Pair<Double,Double> xy : xy0) {
			xyS0.add(xy.getKey(),xy.getValue());
		}

		List<Pair<Double,Double>> xy1 =
				x0.stream()
				.map(x -> new Pair(x, Math.pow(x, 3)))
				.collect(Collectors.toList())
				;
		XYSeries xyS1 = new XYSeries("xy1");
		for(Pair<Double,Double> xy : xy1) {
			xyS1.add(xy.getKey(),xy.getValue());
		}

		List<Pair<Double,Double>> xy2 =
				x0.stream()
				.map(x -> new Pair(x, Math.pow(x, 4)))
				.collect(Collectors.toList())
				;
		XYSeries xyS2 = new XYSeries("xy2");
		for(Pair<Double,Double> xy : xy2) {
			xyS2.add(xy.getKey(),xy.getValue());
		}

		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(xyS0);
		dataset.addSeries(xyS1);
		dataset.addSeries(xyS2);

		return dataset;
	}

	@Override
	public void start(Stage stage) throws Exception {
		XYDataset dataset = createDataset();
		JFreeChart chart = createChart(dataset);
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane();
		stackPane.getChildren().add(canvas);
		// Bind canvas size to stack pane size.
		canvas.widthProperty().bind( stackPane.widthProperty());
		canvas.heightProperty().bind( stackPane.heightProperty());
		stage.setScene(new Scene(stackPane));
		stage.setTitle("JPAD: Chart demo");
		stage.setWidth(700);
		stage.setHeight(390);
		stage.show();
	}




	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
