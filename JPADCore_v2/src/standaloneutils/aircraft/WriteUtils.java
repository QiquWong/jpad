package standaloneutils.aircraft;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;

import aircraft.components.liftingSurface.airfoils.Airfoil;
import javaslang.Tuple;
import javaslang.Tuple2;

public final class WriteUtils {
	
	private WriteUtils() {}

	public static boolean writeAirfoilSVG(String pathToSVG, String title, Airfoil airfoil) {
		return writeAirfoilsToSVG(pathToSVG, title, new Airfoil[] {airfoil});
	}

	public static boolean writeAirfoilSVG(String pathToSVG, String title,
			int WIDTH, int HEIGHT, double xMin, double xMax, double yMin, double yMax,
			Airfoil airfoil) {
		return writeAirfoilsToSVG(pathToSVG, title,
				WIDTH, HEIGHT, xMin, xMax, yMin, yMax,
				new Airfoil[] {airfoil});
	}
	
	public static boolean writeAirfoilsToSVG(String pathToSVG, String title, Airfoil ... airfoils) {
		double xMin = -0.1;
		double xMax = 1.1;
		double yMin = -0.575;
		double yMax = 0.575;
		int WIDTH = 550;
		int HEIGHT = 600;
		return writeAirfoilsToSVG(pathToSVG, title, 
				WIDTH, HEIGHT, xMin, xMax, yMin, yMax,
				airfoils);
	}
	
	public static boolean writeAirfoilsToSVG(String pathToSVG, String title, 
			int WIDTH, int HEIGHT, double xMin, double xMax, double yMin, double yMax,
			Airfoil ... airfoils) {
		
		boolean status = false;
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		//Arrays.asList(airfoils).stream()
		IntStream.range(0, airfoils.length)
			.forEach(kAirfoil -> {
				
				Airfoil airfoil = airfoils[kAirfoil];
				
				List<Double> xCoordinates = Arrays.stream(airfoil.getXCoords())
						.boxed()
						.collect(Collectors.toList()); 
				List<Double> zCoordinates = Arrays.stream(airfoil.getZCoords())
						.boxed()
						.collect(Collectors.toList()); 

				XYSeries seriesAirfoil = new XYSeries("Airfoil-"+kAirfoil, false);
				IntStream.range(0, xCoordinates.size())
				.forEach(i -> {
					seriesAirfoil.add(
							xCoordinates.get(i),
							zCoordinates.get(i)
							);
				});

				//-------------------------------------------------------------------------------------
				// DATASET UPDATE
				List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
				seriesAndColorList.add(Tuple.of(seriesAirfoil, Color.decode("#87CEFA")));

				seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
			});
		//-------------------------------------------------------------------------------------
		// CHART CREATION

		JFreeChart chart = ChartFactory.createXYAreaChart(
				title, 
				"x (m)", 
				"z (m)",
				(XYDataset) dataset,
				PlotOrientation.VERTICAL,
                false, // legend
                true,  // tooltips
                false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.0f);
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(xMin, xMax);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(yMin, yMax);

		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
		}
		
		plot.setRenderer(xyLineAndShapeRenderer);
		plot.setDataset(dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG

		File outputFile = new File(pathToSVG);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
			status = true;
		} catch (IOException e) {
			e.printStackTrace();
			status = false;
		}
		return status;
	}

}
