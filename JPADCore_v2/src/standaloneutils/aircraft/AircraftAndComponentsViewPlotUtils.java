package standaloneutils.aircraft;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.cabinconfiguration.ISeatBlock;
import aircraft.components.cabinconfiguration.SeatsBlock;
import calculators.geometry.FusNacGeometryCalc;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.RelativePositionEnum;
import javaslang.Tuple;
import javaslang.Tuple2;
import standaloneutils.GeometryCalc;
import standaloneutils.MyArrayUtils;
import standaloneutils.customdata.CenterOfGravity;

public class AircraftAndComponentsViewPlotUtils {

	public static final int LABEL_SIZE = 30;
	public static final int TICK_LABEL_SIZE = 25;
	public static final int LEGEND_FONT_SIZE = 20;
	public static final int WIDTH = 1000;
	public static final int HEIGHT = 1000;
	
	public static void createAircraftTopView(Aircraft aircraft, String outputDirectoryPath) {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		
		if(aircraft.getFuselage() != null) {
			// left curve, upperview
			List<Amount<Length>> vX1Left = new ArrayList<>(); 
			aircraft.getFuselage().getOutlineXYSideLCurveAmountX().stream().forEach(x -> vX1Left.add(x));
			int nX1Left = vX1Left.size();
			List<Amount<Length>> vY1Left = new ArrayList<>(); 
			aircraft.getFuselage().getOutlineXYSideLCurveAmountY().stream().forEach(y -> vY1Left.add(y));

			// right curve, upperview
			List<Amount<Length>> vX2Right = new ArrayList<>(); 
			aircraft.getFuselage().getOutlineXYSideRCurveAmountX().stream().forEach(x -> vX2Right.add(x));;
			int nX2Right = vX2Right.size();
			List<Amount<Length>> vY2Right = new ArrayList<>(); 
			aircraft.getFuselage().getOutlineXYSideRCurveAmountY().stream().forEach(y -> vY2Right.add(y));;

			IntStream.range(0, nX1Left)
			.forEach(i -> {
				seriesFuselageCurve.add(vX1Left.get(i).doubleValue(SI.METRE), vY1Left.get(i).doubleValue(SI.METRE));
			});
			IntStream.range(0, nX2Right)
			.forEach(i -> {
				seriesFuselageCurve.add(vX2Right.get(vX2Right.size()-1-i).doubleValue(SI.METRE), vY2Right.get(vY2Right.size()-1-i).doubleValue(SI.METRE));
			});
		}
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		XYSeries seriesWingTopView = new XYSeries("Wing - Top View", false);
		
		if (aircraft.getWing() != null) {
			Double[][] dataTopViewIsolated = aircraft.getWing().getDiscretizedTopViewAsArray(ComponentEnum.WING);

			IntStream.range(0, dataTopViewIsolated.length)
			.forEach(i -> {
				seriesWingTopView.add(
						dataTopViewIsolated[i][1] + aircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewIsolated[i][0] + aircraft.getWing().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
			IntStream.range(0, dataTopViewIsolated.length)
			.forEach(i -> {
				seriesWingTopView.add(
						dataTopViewIsolated[dataTopViewIsolated.length-1-i][1] + aircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER),
						-dataTopViewIsolated[dataTopViewIsolated.length-1-i][0] + aircraft.getWing().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		XYSeries seriesHTailTopView = new XYSeries("HTail - Top View", false);
		
		if (aircraft.getHTail() != null) {
			Double[][] dataTopViewIsolatedHTail = aircraft.getHTail().getDiscretizedTopViewAsArray(ComponentEnum.HORIZONTAL_TAIL);

			IntStream.range(0, dataTopViewIsolatedHTail.length)
			.forEach(i -> {
				seriesHTailTopView.add(
						dataTopViewIsolatedHTail[i][1] + aircraft.getHTail().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewIsolatedHTail[i][0] + aircraft.getHTail().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
			IntStream.range(0, dataTopViewIsolatedHTail.length)
			.forEach(i -> {
				seriesHTailTopView.add(
						dataTopViewIsolatedHTail[dataTopViewIsolatedHTail.length-1-i][1] + aircraft.getHTail().getXApexConstructionAxes().doubleValue(SI.METER),
						- dataTopViewIsolatedHTail[dataTopViewIsolatedHTail.length-1-i][0] + aircraft.getHTail().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}
		
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		XYSeries seriesVTailRootAirfoilTopView = new XYSeries("VTail Root - Top View", false);
		XYSeries seriesVTailTipAirfoilTopView = new XYSeries("VTail Tip - Top View", false);
		
		if (aircraft.getVTail() != null) {

			double[] vTailRootXCoordinates = aircraft.getVTail().getAirfoilList().get(0).getXCoords();
			double[] vTailRootYCoordinates = aircraft.getVTail().getAirfoilList().get(0).getZCoords();
			double[] vTailTipXCoordinates = aircraft.getVTail().getAirfoilList().get(aircraft.getVTail().getAirfoilList().size()-1).getXCoords();
			double[] vTailTipYCoordinates = aircraft.getVTail().getAirfoilList().get(aircraft.getVTail().getAirfoilList().size()-1).getZCoords();
			int nPointsVTail = aircraft.getVTail().getDiscretizedXle().size();

			IntStream.range(0, vTailRootXCoordinates.length)
			.forEach(i -> {
				seriesVTailRootAirfoilTopView.add(
						(vTailRootXCoordinates[i]*aircraft.getVTail().getPanels()
								.get(aircraft.getVTail().getPanels().size()-1).getChordRoot().doubleValue(SI.METER))
						+ aircraft.getVTail().getXApexConstructionAxes().doubleValue(SI.METER),
						(vTailRootYCoordinates[i]*aircraft.getVTail().getPanels().get(0).getChordRoot().getEstimatedValue())
						);
			});

			IntStream.range(0, vTailTipXCoordinates.length)
			.forEach(i -> {
				seriesVTailTipAirfoilTopView.add(
						(vTailTipXCoordinates[i]*aircraft.getVTail().getPanels()
								.get(aircraft.getVTail().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ aircraft.getVTail().getXApexConstructionAxes().getEstimatedValue()
						+ aircraft.getVTail().getDiscretizedXle().get(nPointsVTail-1).getEstimatedValue(),
						(vTailTipYCoordinates[i]*aircraft.getVTail().getPanels()
								.get(aircraft.getVTail().getPanels().size()-1).getChordTip().getEstimatedValue())
						);
			});
		}
		
		//--------------------------------------------------
		// get data vectors from canard discretization
		//--------------------------------------------------
		XYSeries seriesCanardTopView = new XYSeries("Canard - Top View", false);
		
		if (aircraft.getCanard() != null) {
			Double[][] dataTopViewIsolatedCanard = aircraft.getCanard().getDiscretizedTopViewAsArray(ComponentEnum.CANARD);

			IntStream.range(0, dataTopViewIsolatedCanard.length)
			.forEach(i -> {
				seriesCanardTopView.add(
						dataTopViewIsolatedCanard[i][1] + aircraft.getCanard().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewIsolatedCanard[i][0] + aircraft.getCanard().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
			IntStream.range(0, dataTopViewIsolatedCanard.length)
			.forEach(i -> {
				seriesCanardTopView.add(
						dataTopViewIsolatedCanard[dataTopViewIsolatedCanard.length-1-i][1] + aircraft.getCanard().getXApexConstructionAxes().doubleValue(SI.METER),
						- dataTopViewIsolatedCanard[dataTopViewIsolatedCanard.length-1-i][0] + aircraft.getCanard().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}
		
		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesTopViewList = new ArrayList<>();

		if(aircraft.getNacelles() != null) {
			for(int i=0; i<aircraft.getNacelles().getNacellesList().size(); i++) {

				// upper curve, sideview
				List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
				aircraft.getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
				int nacelleCurveXPoints = nacelleCurveX.size();
				List<Amount<Length>> nacelleCurveUpperY = new ArrayList<>(); 
				aircraft.getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYRight().stream().forEach(y -> nacelleCurveUpperY.add(y));

				// lower curve, sideview
				List<Amount<Length>> nacelleCurveLowerY = new ArrayList<>(); 
				aircraft.getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYLeft().stream().forEach(y -> nacelleCurveLowerY.add(y));;

				List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
				List<Amount<Length>> dataOutlineXZCurveNacelleY = new ArrayList<>();

				for(int j=0; j<nacelleCurveXPoints; j++) {
					dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j)
							.plus(aircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
					dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(j)
							.plus(aircraft.getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));
				}

				for(int j=0; j<nacelleCurveXPoints; j++) {
					dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1) 
							.plus(aircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
					dataOutlineXZCurveNacelleY.add(nacelleCurveLowerY.get(nacelleCurveXPoints-j-1)
							.plus(aircraft.getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));
				}

				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0)
						.plus(aircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(0)
						.plus(aircraft.getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));

				XYSeries seriesNacelleCruvesTopView = new XYSeries("Nacelle " + i + " XZ Curve - Top View", false);
				IntStream.range(0, dataOutlineXZCurveNacelleX.size())
				.forEach(j -> {
					seriesNacelleCruvesTopView.add(
							dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER),
							dataOutlineXZCurveNacelleY.get(j).doubleValue(SI.METER)
							);
				});
				
				seriesNacelleCruvesTopViewList.add(seriesNacelleCruvesTopView);

			}
		}

		//-------------------------------------------------------------------------------
		// get data vectors from power plant propellers (only for PISTON and TURBOPROP)
		//-------------------------------------------------------------------------------
		List<XYSeries> seriesPropellerTopViewList = new ArrayList<>();
		
		if(aircraft.getPowerPlant() != null) {
			for(int i=0; i<aircraft.getPowerPlant().getEngineList().size(); i++) {

				if (aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
						|| aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {

					XYSeries seriesPropellerCruvesTopView = new XYSeries("Propeller " + i, false);
					seriesPropellerCruvesTopView.add(
							aircraft.getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							aircraft.getPowerPlant().getEngineList().get(i).getYApexConstructionAxes().doubleValue(SI.METER)
							+ aircraft.getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);
					seriesPropellerCruvesTopView.add(
							aircraft.getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							aircraft.getPowerPlant().getEngineList().get(i).getYApexConstructionAxes().doubleValue(SI.METER)
							- aircraft.getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);

					seriesPropellerTopViewList.add(seriesPropellerCruvesTopView);
				}
			}
		}
		
		double xMaxTopView = 1.40*aircraft.getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -1.40*aircraft.getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = 1.20*aircraft.getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMinTopView = -0.20*aircraft.getFuselage().getFuselageLength().doubleValue(SI.METRE);
			
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		Map<Double, Tuple2<XYSeries, Color>> componentZList = new HashMap<>();
		if (aircraft.getFuselage() != null) 
			componentZList.put(
					aircraft.getFuselage().getZApexConstructionAxes().doubleValue(SI.METER)
					+ aircraft.getFuselage().getSectionCylinderHeight().divide(2).doubleValue(SI.METER),
					Tuple.of(seriesFuselageCurve, Color.WHITE) 
					);
		if (aircraft.getWing() != null) 
			componentZList.put(
					aircraft.getWing().getZApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesWingTopView, Color.decode("#87CEFA"))
					); 
		if (aircraft.getHTail() != null) 
			componentZList.put(
					aircraft.getHTail().getZApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesHTailTopView, Color.decode("#00008B"))
					);
		if (aircraft.getCanard() != null) 
			componentZList.put(
					aircraft.getCanard().getZApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesCanardTopView, Color.decode("#228B22"))
					);
		if (aircraft.getVTail() != null)
			if (aircraft.getFuselage() != null)
				componentZList.put(
						aircraft.getFuselage().getZApexConstructionAxes().doubleValue(SI.METER)
						+ aircraft.getFuselage().getSectionCylinderHeight().divide(2).doubleValue(SI.METER)
						+ 0.0001,
						Tuple.of(seriesVTailRootAirfoilTopView, Color.decode("#FFD700"))
						);
		if (aircraft.getVTail() != null)
			componentZList.put(
					aircraft.getVTail().getZApexConstructionAxes().doubleValue(SI.METER) 
					+ aircraft.getVTail().getSpan().doubleValue(SI.METER), 
					Tuple.of(seriesVTailTipAirfoilTopView, Color.decode("#FFD700"))
					);
		if (aircraft.getNacelles() != null) 
			seriesNacelleCruvesTopViewList.stream().forEach(
					nac -> componentZList.put(
							aircraft.getNacelles().getNacellesList().get(
									seriesNacelleCruvesTopViewList.indexOf(nac)
									).getZApexConstructionAxes().doubleValue(SI.METER)
							+ 0.005
							+ seriesNacelleCruvesTopViewList.indexOf(nac)*0.005, 
							Tuple.of(nac, Color.decode("#FF7F50"))
							)
					);
		if (aircraft.getPowerPlant() != null) 
			seriesPropellerTopViewList.stream().forEach(
					prop -> componentZList.put(
							aircraft.getPowerPlant().getEngineList().get(
									seriesPropellerTopViewList.indexOf(prop)
									).getZApexConstructionAxes().doubleValue(SI.METER)
							+ 0.0015
							+ seriesPropellerTopViewList.indexOf(prop)*0.001, 
							Tuple.of(prop, Color.BLACK)
							)
					);
		
		Map<Double, Tuple2<XYSeries, Color>> componentZListSorted = 
				componentZList.entrySet().stream()
			    .sorted(Entry.comparingByKey(Comparator.reverseOrder()))
			    .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
			                              (e1, e2) -> e1, LinkedHashMap::new));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		componentZListSorted.values().stream().forEach(t -> dataset.addSeries(t._1()));
		
		List<Color> colorList = new ArrayList<>();
		componentZListSorted.values().stream().forEach(t -> colorList.add(t._2()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Aircraft data representation - Top View", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
                false, // legend
                true,  // tooltips
                false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);
		range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					colorList.get(i)
					);
		}
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
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopViewSVG = outputDirectoryPath 
				+ File.separator 
				+ "AircraftTopView.svg";
		File outputFileSVG = new File(outputFilePathTopViewSVG);
		if(outputFileSVG.exists()) outputFileSVG.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO PNG
		String outputFilePathTopViewPNG = outputDirectoryPath 
				+ File.separator 
				+ "AircraftTopView.png";
		File outputFilePNG = new File(outputFilePathTopViewPNG);
		if(outputFilePNG.exists()) outputFilePNG.delete();
		
		try {
			ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static void createAircraftSideView(Aircraft aircraft, String outputDirectoryPath) {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		
		if (aircraft.getFuselage() != null) {
			// upper curve, sideview
			List<Amount<Length>> vX1Upper = new ArrayList<>(); 
			aircraft.getFuselage().getOutlineXZUpperCurveAmountX().stream().forEach(x -> vX1Upper.add(x));
			int nX1Upper = vX1Upper.size();
			List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
			aircraft.getFuselage().getOutlineXZUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

			// lower curve, sideview
			List<Amount<Length>> vX2Lower = new ArrayList<>(); 
			aircraft.getFuselage().getOutlineXZLowerCurveAmountX().stream().forEach(x -> vX2Lower.add(x));
			int nX2Lower = vX2Lower.size();
			List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
			aircraft.getFuselage().getOutlineXZLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));

			IntStream.range(0, nX1Upper)
			.forEach(i -> {
				seriesFuselageCurve.add(vX1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
			});
			IntStream.range(0, nX2Lower)
			.forEach(i -> {
				seriesFuselageCurve.add(vX2Lower.get(vX2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
			});
		}
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		XYSeries seriesWingRootAirfoil = new XYSeries("Wing Root - Side View", false);
		XYSeries seriesWingTipAirfoil = new XYSeries("Wing Tip - Side View", false);

		if (aircraft.getWing() != null) {
			double[] wingRootXCoordinates = aircraft.getWing().getAirfoilList().get(0).getXCoords();
			double[] wingRootZCoordinates = aircraft.getWing().getAirfoilList().get(0).getZCoords();
			double[] wingTipXCoordinates = aircraft.getWing().getAirfoilList()
					.get(aircraft.getWing().getAirfoilList().size()-1).getXCoords();
			double[] wingTipZCoordinates = aircraft.getWing().getAirfoilList()
					.get(aircraft.getWing().getAirfoilList().size()-1).getZCoords();
			int nPointsWing = aircraft.getWing().getDiscretizedXle().size();

			IntStream.range(0, wingRootXCoordinates.length)
			.forEach(i -> {
				seriesWingRootAirfoil.add(
						(wingRootXCoordinates[i]*aircraft.getWing().getPanels().get(0).getChordRoot().getEstimatedValue()) 
						+ aircraft.getWing().getXApexConstructionAxes().getEstimatedValue(),
						(wingRootZCoordinates[i]*aircraft.getWing().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getWing().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, wingTipXCoordinates.length)
			.forEach(i -> {
				seriesWingTipAirfoil.add(
						(wingTipXCoordinates[i]*aircraft.getWing().getPanels()
								.get(aircraft.getWing().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ aircraft.getWing().getXApexConstructionAxes().getEstimatedValue()
						+ aircraft.getWing().getDiscretizedXle().get(nPointsWing-1).getEstimatedValue(),
						(wingTipZCoordinates[i]*aircraft.getWing().getPanels()
								.get(aircraft.getWing().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ aircraft.getWing().getZApexConstructionAxes().getEstimatedValue()
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		XYSeries seriesHTailRootAirfoil = new XYSeries("HTail Root - Side View", false);
		XYSeries seriesHTailTipAirfoil = new XYSeries("HTail Tip - Side View", false);

		if (aircraft.getHTail() != null) {
			double[] hTailRootXCoordinates = aircraft.getHTail().getAirfoilList().get(0).getXCoords();
			double[] hTailRootZCoordinates = aircraft.getHTail().getAirfoilList().get(0).getZCoords();
			double[] hTailTipXCoordinates = aircraft.getHTail().getAirfoilList()
					.get(aircraft.getHTail().getAirfoilList().size()-1).getXCoords();
			double[] hTailTipZCoordinates = aircraft.getHTail().getAirfoilList()
					.get(aircraft.getHTail().getAirfoilList().size()-1).getZCoords();
			int nPointsHTail = aircraft.getHTail().getDiscretizedXle().size();

			IntStream.range(0, hTailRootXCoordinates.length)
			.forEach(i -> {
				seriesHTailRootAirfoil.add(
						(hTailRootXCoordinates[i]*aircraft.getHTail().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getHTail().getXApexConstructionAxes().getEstimatedValue(),
						(hTailRootZCoordinates[i]*aircraft.getHTail().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getHTail().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, hTailTipXCoordinates.length)
			.forEach(i -> {
				seriesHTailTipAirfoil.add(
						(hTailTipXCoordinates[i]*aircraft.getHTail().getPanels()
								.get(aircraft.getHTail().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ aircraft.getHTail().getXApexConstructionAxes().getEstimatedValue()
						+ aircraft.getHTail().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(),
						(hTailTipZCoordinates[i]*aircraft.getHTail().getPanels()
								.get(aircraft.getHTail().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ aircraft.getHTail().getZApexConstructionAxes().getEstimatedValue()
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from canard discretization
		//--------------------------------------------------
		XYSeries seriesCanardRootAirfoil = new XYSeries("Canard Root - Side View", false);
		XYSeries seriesCanardTipAirfoil = new XYSeries("Canard Tip - Side View", false);

		if (aircraft.getCanard() != null) {
			double[] canardRootXCoordinates = aircraft.getCanard().getAirfoilList().get(0).getXCoords();
			double[] canardRootZCoordinates = aircraft.getCanard().getAirfoilList().get(0).getZCoords();
			double[] canardTipXCoordinates = aircraft.getCanard().getAirfoilList()
					.get(aircraft.getCanard().getAirfoilList().size()-1).getXCoords();
			double[] canardTipZCoordinates = aircraft.getCanard().getAirfoilList()
					.get(aircraft.getCanard().getAirfoilList().size()-1).getZCoords();
			int nPointsHTail = aircraft.getCanard().getDiscretizedXle().size();

			IntStream.range(0, canardRootXCoordinates.length)
			.forEach(i -> {
				seriesCanardRootAirfoil.add(
						(canardRootXCoordinates[i]*aircraft.getCanard().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getCanard().getXApexConstructionAxes().getEstimatedValue(),
						(canardRootZCoordinates[i]*aircraft.getCanard().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getCanard().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, canardTipXCoordinates.length)
			.forEach(i -> {
				seriesCanardTipAirfoil.add(
						(canardTipXCoordinates[i]*aircraft.getCanard().getPanels()
								.get(aircraft.getCanard().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ aircraft.getCanard().getXApexConstructionAxes().getEstimatedValue()
						+ aircraft.getCanard().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(),
						(canardTipZCoordinates[i]*aircraft.getCanard().getPanels()
								.get(aircraft.getCanard().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ aircraft.getCanard().getZApexConstructionAxes().getEstimatedValue()
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		XYSeries seriesVTailSideView = new XYSeries("VTail - Side View", false);

		if (aircraft.getVTail() != null) {
			Double[][] dataTopViewVTail = aircraft.getVTail().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);

			IntStream.range(0, dataTopViewVTail.length)
			.forEach(i -> {
				seriesVTailSideView.add(
						dataTopViewVTail[i][0] + aircraft.getVTail().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewVTail[i][1] + aircraft.getVTail().getZApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}

		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesSideViewList = new ArrayList<>();

		if (aircraft.getNacelles() != null) {
			for(int i=0; i<aircraft.getNacelles().getNacellesList().size(); i++) {

				// upper curve, sideview
				List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
				aircraft.getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
				int nacelleCurveXPoints = nacelleCurveX.size();
				List<Amount<Length>> nacelleCurveUpperZ = new ArrayList<>(); 
				aircraft.getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZUpper().stream().forEach(z -> nacelleCurveUpperZ.add(z));

				// lower curve, sideview
				List<Amount<Length>> nacelleCurveLowerZ = new ArrayList<>(); 
				aircraft.getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZLower().stream().forEach(z -> nacelleCurveLowerZ.add(z));;

				List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
				List<Amount<Length>> dataOutlineXZCurveNacelleZ = new ArrayList<>();

				for(int j=0; j<nacelleCurveXPoints; j++) {
					dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j)
							.plus(aircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
					dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(j)
							.plus(aircraft.getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
				}

				for(int j=0; j<nacelleCurveXPoints; j++) {
					dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1) 
							.plus(aircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
					dataOutlineXZCurveNacelleZ.add(nacelleCurveLowerZ.get(nacelleCurveXPoints-j-1)
							.plus(aircraft.getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
				}

				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0)
						.plus(aircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(0)
						.plus(aircraft.getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));


				XYSeries seriesNacelleCruvesSideView = new XYSeries("Nacelle " + i + " XY Curve - Side View", false);
				IntStream.range(0, dataOutlineXZCurveNacelleX.size())
				.forEach(j -> {
					seriesNacelleCruvesSideView.add(
							dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER),
							dataOutlineXZCurveNacelleZ.get(j).doubleValue(SI.METER)
							);
				});

				seriesNacelleCruvesSideViewList.add(seriesNacelleCruvesSideView);

			}
		}
		//-------------------------------------------------------------------------------
		// get data vectors from power plant propellers (only for PISTON and TURBOPROP)
		//-------------------------------------------------------------------------------
		List<XYSeries> seriesPropellerSideViewList = new ArrayList<>();
		
		if(aircraft.getPowerPlant() != null) {
			for(int i=0; i<aircraft.getPowerPlant().getEngineList().size(); i++) {

				if (aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
						|| aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {

					XYSeries seriesPropellerCruvesSideView = new XYSeries("Propeller " + i, false);
					seriesPropellerCruvesSideView.add(
							aircraft.getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							1.0015*aircraft.getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
							+ aircraft.getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);
					seriesPropellerCruvesSideView.add(
							aircraft.getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							1.0015*aircraft.getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
							- aircraft.getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);

					seriesPropellerSideViewList.add(seriesPropellerCruvesSideView);
				}
			}
		}		
		//--------------------------------------------------
		// get data vectors from landing gears 
		//--------------------------------------------------
		XYSeries seriesMainLandingGearSideView = new XYSeries("Main Landing Gears - Side View", false);
		XYSeries seriesNoseLandingGearSideView = new XYSeries("Front Landing Gears - Side View", false);
		
		if (aircraft.getLandingGears() != null) {
			Amount<Length> radiusNose = aircraft.getLandingGears().getFrontalWheelsHeight().divide(2);
			Amount<Length> radiusMain = aircraft.getLandingGears().getRearWheelsHeight().divide(2);
			Double[] frontWheelCenterPosition = new Double[] {
					aircraft.getLandingGears().getXApexConstructionAxesNoseGear().doubleValue(SI.METER),
					aircraft.getLandingGears().getZApexConstructionAxesNoseGear().doubleValue(SI.METER)
					- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					- radiusNose.doubleValue(SI.METER)
			};
			Double[] mainWheelCenterPosition = new Double[] {
					aircraft.getLandingGears().getXApexConstructionAxesMainGear().doubleValue(SI.METER),
					aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
					- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					- radiusMain.doubleValue(SI.METER)
			};
			Double[] thetaArray = MyArrayUtils.linspaceDouble(0, 2*Math.PI, 360);

			IntStream.range(0, thetaArray.length)
			.forEach(i -> {
				seriesNoseLandingGearSideView.add(
						radiusNose.doubleValue(SI.METER)*Math.cos(thetaArray[i]) + frontWheelCenterPosition[0],
						radiusNose.doubleValue(SI.METER)*Math.sin(thetaArray[i]) + frontWheelCenterPosition[1]
						);
				seriesMainLandingGearSideView.add(
						radiusMain.doubleValue(SI.METER)*Math.cos(thetaArray[i]) + mainWheelCenterPosition[0],
						radiusMain.doubleValue(SI.METER)*Math.sin(thetaArray[i]) + mainWheelCenterPosition[1]
						);
			});
		}
		
		double xMaxSideView = 1.20*aircraft.getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double xMinSideView = -0.20*aircraft.getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMaxSideView = 1.40*aircraft.getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMinSideView = -1.40*aircraft.getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		if (aircraft.getPowerPlant() != null)
			seriesPropellerSideViewList.stream().forEach(
					prop -> seriesAndColorList.add(Tuple.of(prop, Color.BLACK))
					);
		if (aircraft.getNacelles() != null)
			seriesNacelleCruvesSideViewList.stream().forEach(
					nac -> seriesAndColorList.add(Tuple.of(nac, Color.decode("#FF7F50")))
					);
		if (aircraft.getWing() != null) {
			seriesAndColorList.add(Tuple.of(seriesWingRootAirfoil, Color.decode("#87CEFA")));
			seriesAndColorList.add(Tuple.of(seriesWingTipAirfoil, Color.decode("#87CEFA")));
		}
		if (aircraft.getHTail() != null) {
			seriesAndColorList.add(Tuple.of(seriesHTailRootAirfoil, Color.decode("#00008B")));
			seriesAndColorList.add(Tuple.of(seriesHTailTipAirfoil, Color.decode("#00008B")));
		}
		if (aircraft.getCanard() != null) {
			seriesAndColorList.add(Tuple.of(seriesCanardRootAirfoil, Color.decode("#228B22")));
			seriesAndColorList.add(Tuple.of(seriesCanardTipAirfoil, Color.decode("#228B22")));
		}
		if (aircraft.getLandingGears() != null) {
			seriesAndColorList.add(Tuple.of(seriesNoseLandingGearSideView, Color.decode("#404040")));
			seriesAndColorList.add(Tuple.of(seriesMainLandingGearSideView, Color.decode("#404040")));
		}
		if (aircraft.getFuselage() != null)
			seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));
		if (aircraft.getVTail() != null)
			seriesAndColorList.add(Tuple.of(seriesVTailSideView, Color.decode("#FFD700")));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Aircraft data representation - Side View", 
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
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(xMinSideView, xMaxSideView);
		domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(yMinSideView, yMaxSideView);
		range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
		XYLineAndShapeRenderer xyLineAndShapePropRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapePropRenderer.setDefaultShapesVisible(false);
		xyLineAndShapePropRenderer.setDefaultLinesVisible(true);
		xyLineAndShapePropRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if (i < aircraft.getPowerPlant().getEngineNumber()) {
				xyLineAndShapePropRenderer.setSeriesVisible(i, true);
				xyLineAndShapePropRenderer.setSeriesPaint(i, Color.BLACK);
				xyLineAndShapePropRenderer.setSeriesStroke(
						i, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}
			else
				xyLineAndShapePropRenderer.setSeriesVisible(i, false);
		}
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

		if (aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
				|| aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {
			plot.setRenderer(2, xyLineAndShapeRenderer);
			plot.setDataset(2, dataset);
			plot.setRenderer(1, xyAreaRenderer);
			plot.setDataset(1, dataset);
			plot.setRenderer(0, xyLineAndShapePropRenderer);
			plot.setDataset(0, dataset);
		}
		else {
			plot.setRenderer(1, xyLineAndShapeRenderer);
			plot.setDataset(1, dataset);
			plot.setRenderer(0, xyAreaRenderer);
			plot.setDataset(0, dataset);
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopViewSVG = outputDirectoryPath 
				+ File.separator 
				+ "AircraftSideView.svg";
		File outputFileSVG = new File(outputFilePathTopViewSVG);
		if(outputFileSVG.exists()) outputFileSVG.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO PNG
		String outputFilePathTopViewPNG = outputDirectoryPath 
				+ File.separator 
				+ "AircraftSideView.png";
		File outputFilePNG = new File(outputFilePathTopViewPNG);
		if(outputFilePNG.exists()) outputFilePNG.delete();
		
		try {
			ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void createAircraftFrontView(Aircraft aircraft, String outputDirectoryPath) {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Front View", false);
		
		if (aircraft.getFuselage() != null) {
			// section upper curve
			List<Amount<Length>> vY1Upper = new ArrayList<>(); 
			aircraft.getFuselage().getSectionUpperCurveAmountY().stream().forEach(y -> vY1Upper.add(y));
			int nY1Upper = vY1Upper.size();
			List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
			aircraft.getFuselage().getSectionUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

			// section lower curve
			List<Amount<Length>> vY2Lower = new ArrayList<>(); 
			aircraft.getFuselage().getSectionLowerCurveAmountY().stream().forEach(y -> vY2Lower.add(y));
			int nY2Lower = vY2Lower.size();
			List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
			aircraft.getFuselage().getSectionLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));

			IntStream.range(0, nY1Upper)
			.forEach(i -> {
				seriesFuselageCurve.add(vY1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
			});
			IntStream.range(0, nY2Lower)
			.forEach(i -> {
				seriesFuselageCurve.add(vY2Lower.get(vY2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
			});
		}
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		XYSeries seriesWingFrontView = new XYSeries("Wing - Front View", false);

		if (aircraft.getWing() != null) {
			List<Amount<Length>> wingBreakPointsYCoordinates = new ArrayList<>(); 
			aircraft.getWing().getYBreakPoints().stream().forEach(y -> wingBreakPointsYCoordinates.add(y));
			int nYPointsWingTemp = wingBreakPointsYCoordinates.size();
			for(int i=0; i<nYPointsWingTemp; i++)
				wingBreakPointsYCoordinates.add(aircraft.getWing().getYBreakPoints().get(nYPointsWingTemp-i-1));
			int nYPointsWing = wingBreakPointsYCoordinates.size();

			List<Amount<Length>> wingThicknessZCoordinates = new ArrayList<Amount<Length>>();
			for(int i=0; i<aircraft.getWing().getAirfoilList().size(); i++)
				wingThicknessZCoordinates.add(
						Amount.valueOf(
								aircraft.getWing().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
								MyArrayUtils.getMax(aircraft.getWing().getAirfoilList().get(i).getZCoords()),
								SI.METER
								)
						);
			for(int i=0; i<nYPointsWingTemp; i++) {
				wingThicknessZCoordinates.add(
						Amount.valueOf(
								(aircraft.getWing().getChordsBreakPoints().get(nYPointsWingTemp-i-1).doubleValue(SI.METER)*
										MyArrayUtils.getMin(aircraft.getWing().getAirfoilList().get(nYPointsWingTemp-i-1).getZCoords())),
								SI.METER
								)
						);
			}

			List<Amount<Angle>> dihedralList = new ArrayList<>();
			for (int i = 0; i < aircraft.getWing().getDihedralsBreakPoints().size(); i++) {
				dihedralList.add(
						aircraft.getWing().getDihedralsBreakPoints().get(i)
						);
			}
			for (int i = 0; i < aircraft.getWing().getDihedralsBreakPoints().size(); i++) {
				dihedralList.add(
						aircraft.getWing().getDihedralsBreakPoints().get(
								aircraft.getWing().getDihedralsBreakPoints().size()-1-i)
						);
			}

			IntStream.range(0, nYPointsWing)
			.forEach(i -> {
				seriesWingFrontView.add(
						wingBreakPointsYCoordinates.get(i).plus(aircraft.getWing().getYApexConstructionAxes()).doubleValue(SI.METRE),
						wingThicknessZCoordinates.get(i)
						.plus(aircraft.getWing().getZApexConstructionAxes())
						.plus(wingBreakPointsYCoordinates.get(i)
								.times(Math.sin(dihedralList.get(i)
										.doubleValue(SI.RADIAN))
										)
								)
						.doubleValue(SI.METRE)
						);
			});
			IntStream.range(0, nYPointsWing)
			.forEach(i -> {
				seriesWingFrontView.add(
						-wingBreakPointsYCoordinates.get(i).plus(aircraft.getWing().getYApexConstructionAxes()).doubleValue(SI.METRE),
						wingThicknessZCoordinates.get(i)
						.plus(aircraft.getWing().getZApexConstructionAxes())
						.plus(wingBreakPointsYCoordinates.get(i)
								.times(Math.sin(dihedralList.get(i)
										.doubleValue(SI.RADIAN))
										)
								)
						.doubleValue(SI.METRE)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		XYSeries seriesHTailFrontView = new XYSeries("HTail - Front View", false);

		if (aircraft.getHTail() != null) {
			List<Amount<Length>> hTailBreakPointsYCoordinates = new ArrayList<>(); 
			aircraft.getHTail().getYBreakPoints().stream().forEach(y -> hTailBreakPointsYCoordinates.add(y));
			int nYPointsHTailTemp = hTailBreakPointsYCoordinates.size();
			for(int i=0; i<nYPointsHTailTemp; i++)
				hTailBreakPointsYCoordinates.add(aircraft.getHTail().getYBreakPoints().get(nYPointsHTailTemp-i-1));
			int nYPointsHTail = hTailBreakPointsYCoordinates.size();

			List<Amount<Length>> hTailThicknessZCoordinates = new ArrayList<Amount<Length>>();
			for(int i=0; i<aircraft.getHTail().getAirfoilList().size(); i++)
				hTailThicknessZCoordinates.add(
						Amount.valueOf(
								aircraft.getHTail().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
								MyArrayUtils.getMax(aircraft.getHTail().getAirfoilList().get(i).getZCoords()),
								SI.METER
								)
						);
			for(int i=0; i<nYPointsHTailTemp; i++)
				hTailThicknessZCoordinates.add(
						Amount.valueOf(
								aircraft.getHTail().getChordsBreakPoints().get(nYPointsHTailTemp-i-1).doubleValue(SI.METER)*
								MyArrayUtils.getMin(aircraft.getHTail().getAirfoilList().get(nYPointsHTailTemp-i-1).getZCoords()),
								SI.METER
								)
						);

			List<Amount<Angle>> dihedralListHTail = new ArrayList<>();
			for (int i = 0; i < aircraft.getHTail().getDihedralsBreakPoints().size(); i++) {
				dihedralListHTail.add(
						aircraft.getHTail().getDihedralsBreakPoints().get(i)
						);
			}
			for (int i = 0; i < aircraft.getHTail().getDihedralsBreakPoints().size(); i++) {
				dihedralListHTail.add(
						aircraft.getHTail().getDihedralsBreakPoints().get(
								aircraft.getHTail().getDihedralsBreakPoints().size()-1-i)
						);
			}

			IntStream.range(0, nYPointsHTail)
			.forEach(i -> {
				seriesHTailFrontView.add(
						hTailBreakPointsYCoordinates.get(i).plus(aircraft.getHTail().getYApexConstructionAxes()).doubleValue(SI.METRE),
						hTailThicknessZCoordinates.get(i)
						.plus(aircraft.getHTail().getZApexConstructionAxes())
						.plus(hTailBreakPointsYCoordinates.get(i)
								.times(Math.sin(dihedralListHTail.get(i)
										.doubleValue(SI.RADIAN))
										)
								)
						.doubleValue(SI.METRE)
						);
			});
			IntStream.range(0, nYPointsHTail)
			.forEach(i -> {
				seriesHTailFrontView.add(
						-hTailBreakPointsYCoordinates.get(i).plus(aircraft.getHTail().getYApexConstructionAxes()).doubleValue(SI.METRE),
						hTailThicknessZCoordinates.get(i)
						.plus(aircraft.getHTail().getZApexConstructionAxes())
						.plus(hTailBreakPointsYCoordinates.get(i)
								.times(Math.sin(dihedralListHTail.get(i)
										.doubleValue(SI.RADIAN))
										)
								)
						.doubleValue(SI.METRE)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from canard discretization
		//--------------------------------------------------
		XYSeries seriesCanardFrontView = new XYSeries("Canard - Front View", false);

		if (aircraft.getCanard() != null) {
			List<Amount<Length>> canardBreakPointsYCoordinates = new ArrayList<>(); 
			aircraft.getCanard().getYBreakPoints().stream().forEach(y -> canardBreakPointsYCoordinates.add(y));
			int nYPointsCanardTemp = canardBreakPointsYCoordinates.size();
			for(int i=0; i<nYPointsCanardTemp; i++)
				canardBreakPointsYCoordinates.add(aircraft.getCanard().getYBreakPoints().get(nYPointsCanardTemp-i-1));
			int nYPointsCanard = canardBreakPointsYCoordinates.size();

			List<Amount<Length>> canardThicknessZCoordinates = new ArrayList<Amount<Length>>();
			for(int i=0; i<aircraft.getCanard().getAirfoilList().size(); i++)
				canardThicknessZCoordinates.add(
						Amount.valueOf(
								aircraft.getCanard().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
								MyArrayUtils.getMax(aircraft.getCanard().getAirfoilList().get(i).getZCoords()),
								SI.METER
								)
						);
			for(int i=0; i<nYPointsCanardTemp; i++)
				canardThicknessZCoordinates.add(
						Amount.valueOf(
								aircraft.getCanard().getChordsBreakPoints().get(nYPointsCanardTemp-i-1).doubleValue(SI.METER)*
								MyArrayUtils.getMin(aircraft.getCanard().getAirfoilList().get(nYPointsCanardTemp-i-1).getZCoords()),
								SI.METER
								)
						);

			List<Amount<Angle>> dihedralListCanard = new ArrayList<>();
			for (int i = 0; i < aircraft.getCanard().getDihedralsBreakPoints().size(); i++) {
				dihedralListCanard.add(
						aircraft.getCanard().getDihedralsBreakPoints().get(i)
						);
			}
			for (int i = 0; i < aircraft.getCanard().getDihedralsBreakPoints().size(); i++) {
				dihedralListCanard.add(
						aircraft.getCanard().getDihedralsBreakPoints().get(
								aircraft.getCanard().getDihedralsBreakPoints().size()-1-i)
						);
			}

			IntStream.range(0, nYPointsCanard)
			.forEach(i -> {
				seriesCanardFrontView.add(
						canardBreakPointsYCoordinates.get(i).plus(aircraft.getCanard().getYApexConstructionAxes()).doubleValue(SI.METRE),
						canardThicknessZCoordinates.get(i)
						.plus(aircraft.getCanard().getZApexConstructionAxes())
						.plus(canardBreakPointsYCoordinates.get(i)
								.times(Math.sin(dihedralListCanard.get(i)
										.doubleValue(SI.RADIAN))
										)
								)
						.doubleValue(SI.METRE)
						);
			});
			IntStream.range(0, nYPointsCanard)
			.forEach(i -> {
				seriesCanardFrontView.add(
						-canardBreakPointsYCoordinates.get(i).plus(aircraft.getCanard().getYApexConstructionAxes()).doubleValue(SI.METRE),
						canardThicknessZCoordinates.get(i)
						.plus(aircraft.getCanard().getZApexConstructionAxes())
						.plus(canardBreakPointsYCoordinates.get(i)
								.times(Math.sin(dihedralListCanard.get(i)
										.doubleValue(SI.RADIAN))
										)
								)
						.doubleValue(SI.METRE)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		XYSeries seriesVTailFrontView = new XYSeries("VTail - Front View", false);

		if (aircraft.getVTail() != null) {
			List<Amount<Length>> vTailBreakPointsYCoordinates = new ArrayList<>(); 
			aircraft.getVTail().getYBreakPoints().stream().forEach(y -> vTailBreakPointsYCoordinates.add(y));
			int nYPointsVTailTemp = vTailBreakPointsYCoordinates.size();
			for(int i=0; i<nYPointsVTailTemp; i++)
				vTailBreakPointsYCoordinates.add(aircraft.getVTail().getYBreakPoints().get(nYPointsVTailTemp-i-1));
			int nYPointsVTail = vTailBreakPointsYCoordinates.size();

			List<Amount<Length>> vTailThicknessZCoordinates = new ArrayList<Amount<Length>>();
			for(int i=0; i<aircraft.getVTail().getAirfoilList().size(); i++)
				vTailThicknessZCoordinates.add(
						Amount.valueOf(
								aircraft.getVTail().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
								MyArrayUtils.getMax(aircraft.getVTail().getAirfoilList().get(i).getZCoords()),
								SI.METER
								)
						);
			for(int i=0; i<nYPointsVTailTemp; i++)
				vTailThicknessZCoordinates.add(
						Amount.valueOf(
								aircraft.getVTail().getChordsBreakPoints().get(nYPointsVTailTemp-i-1).doubleValue(SI.METER)*
								MyArrayUtils.getMin(aircraft.getVTail().getAirfoilList().get(nYPointsVTailTemp-i-1).getZCoords()),
								SI.METER
								)
						);

			IntStream.range(0, nYPointsVTail)
			.forEach(i -> {
				seriesVTailFrontView.add(
						vTailThicknessZCoordinates.get(i).plus(aircraft.getVTail().getYApexConstructionAxes()).doubleValue(SI.METRE),
						vTailBreakPointsYCoordinates.get(i).plus(aircraft.getVTail().getZApexConstructionAxes()).doubleValue(SI.METRE)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from nacelles discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesFrontViewList = new ArrayList<>();

		if (aircraft.getNacelles() != null) {
			for(int i=0; i<aircraft.getNacelles().getNacellesList().size(); i++) {

				double[] angleArray = MyArrayUtils.linspace(0.0, 2*Math.PI, 20);
				double[] yCoordinate = new double[angleArray.length];
				double[] zCoordinate = new double[angleArray.length];

				double radius = aircraft.getNacelles()
						.getNacellesList().get(i)
						.getDiameterMax()
						.divide(2)
						.doubleValue(SI.METER);
				double y0 = aircraft.getNacelles()
						.getNacellesList().get(i)
						.getYApexConstructionAxes()
						.doubleValue(SI.METER);

				double z0 = aircraft.getNacelles()
						.getNacellesList().get(i)
						.getZApexConstructionAxes()
						.doubleValue(SI.METER);

				for(int j=0; j<angleArray.length; j++) {
					yCoordinate[j] = radius*Math.cos(angleArray[j]);
					zCoordinate[j] = radius*Math.sin(angleArray[j]);
				}

				XYSeries seriesNacelleCruvesFrontView = new XYSeries("Nacelle " + i + " - Front View", false);
				IntStream.range(0, yCoordinate.length)
				.forEach(j -> {
					seriesNacelleCruvesFrontView.add(
							yCoordinate[j] + y0,
							zCoordinate[j] + z0
							);
				});

				seriesNacelleCruvesFrontViewList.add(seriesNacelleCruvesFrontView);
			}
		}
		//-------------------------------------------------------------------------------
		// get data vectors from power plant propellers (only for PISTON and TURBOPROP)
		//-------------------------------------------------------------------------------
		List<XYSeries> seriesPropellerFrontViewList = new ArrayList<>();
		
		if(aircraft.getPowerPlant() != null) {
			for(int i=0; i<aircraft.getPowerPlant().getEngineList().size(); i++) {

				if (aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
						|| aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {
					
					Amount<Length> radius = aircraft.getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2);
					Double[] centerPosition = new Double[] {
							aircraft.getPowerPlant().getEngineList().get(i).getYApexConstructionAxes().doubleValue(SI.METER),
							aircraft.getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
					};
					Double[] thetaArray = MyArrayUtils.linspaceDouble(0, 2*Math.PI, 360);
					
					XYSeries seriesPropellerCruvesFrontView = new XYSeries("Propeller " + i, false);
					IntStream.range(0, thetaArray.length)
					.forEach(j -> {
						seriesPropellerCruvesFrontView.add(
								radius.doubleValue(SI.METER)*Math.cos(thetaArray[j]) + centerPosition[0],
								radius.doubleValue(SI.METER)*Math.sin(thetaArray[j]) + centerPosition[1]
								);
					});

					seriesPropellerFrontViewList.add(seriesPropellerCruvesFrontView);
					
				}
			}
		}
		//--------------------------------------------------
		// get data vectors from landing gears
		//--------------------------------------------------
		List<XYSeries> serieNoseLandingGearsCruvesFrontViewList = new ArrayList<>();
		List<XYSeries> serieMainLandingGearsCruvesFrontViewList = new ArrayList<>();

		if (aircraft.getLandingGears() != null) {
			for(int i=0; i<aircraft.getLandingGears().getNumberOfRearWheels()/2; i++) {

				XYSeries seriesLeftMainLandingGearCruvesFrontView = new XYSeries("Left Main Landing Gear " + i + " - Front View", false);
				seriesLeftMainLandingGearCruvesFrontView.add(
						aircraft.getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ (i*1.1*aircraft.getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesLeftMainLandingGearCruvesFrontView.add(
						aircraft.getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ aircraft.getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*aircraft.getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesLeftMainLandingGearCruvesFrontView.add(
						aircraft.getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ aircraft.getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*aircraft.getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- aircraft.getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
						);
				seriesLeftMainLandingGearCruvesFrontView.add(
						aircraft.getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ (i*1.1*aircraft.getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- aircraft.getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
						);
				seriesLeftMainLandingGearCruvesFrontView.add(
						aircraft.getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ (i*1.1*aircraft.getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);

				XYSeries seriesRightMainLandingGearCruvesFrontView = new XYSeries("Right Main Landing Gear " + i + " - Front View", false);
				seriesRightMainLandingGearCruvesFrontView.add(
						- (aircraft.getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ (i*1.1*aircraft.getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesRightMainLandingGearCruvesFrontView.add(
						- (aircraft.getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ aircraft.getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
								+ (i*1.1*aircraft.getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesRightMainLandingGearCruvesFrontView.add(
						- (aircraft.getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ aircraft.getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
								+ (i*1.1*aircraft.getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- aircraft.getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
						);
				seriesRightMainLandingGearCruvesFrontView.add(
						- (aircraft.getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ (i*1.1*aircraft.getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- aircraft.getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
						);
				seriesRightMainLandingGearCruvesFrontView.add(
						- (aircraft.getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ (i*1.1*aircraft.getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);

				serieMainLandingGearsCruvesFrontViewList.add(seriesLeftMainLandingGearCruvesFrontView);
				serieMainLandingGearsCruvesFrontViewList.add(seriesRightMainLandingGearCruvesFrontView);
			}
			
			for(int i=0; i<aircraft.getLandingGears().getNumberOfFrontalWheels(); i++) {

				XYSeries seriesLeftNoseLandingGearCruvesFrontView = new XYSeries("Left Nose Landing Gear " + i + " - Front View", false);
				seriesLeftNoseLandingGearCruvesFrontView.add(
						-aircraft.getLandingGears()
						.getFrontalWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*aircraft.getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesLeftNoseLandingGearCruvesFrontView.add(
						-aircraft.getLandingGears()
						.getFrontalWheelsWidth().doubleValue(SI.METER)
						+ aircraft.getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*aircraft.getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesLeftNoseLandingGearCruvesFrontView.add(
						-aircraft.getLandingGears()
						.getFrontalWheelsWidth().doubleValue(SI.METER)
						+ aircraft.getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*aircraft.getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- aircraft.getLandingGears().getFrontalWheelsHeight().doubleValue(SI.METER)
						);
				seriesLeftNoseLandingGearCruvesFrontView.add(
						-aircraft.getLandingGears()
						.getFrontalWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*aircraft.getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- aircraft.getLandingGears().getFrontalWheelsHeight().doubleValue(SI.METER)
						);
				seriesLeftNoseLandingGearCruvesFrontView.add(
						-aircraft.getLandingGears()
						.getFrontalWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*aircraft.getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);

				XYSeries seriesRightNoseLandingGearCruvesFrontView = new XYSeries("Right Nose Landing Gear " + i + " - Front View", false);
				seriesRightNoseLandingGearCruvesFrontView.add(
						- (i*1.1*aircraft.getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesRightNoseLandingGearCruvesFrontView.add(
						- (i*1.1*aircraft.getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesRightNoseLandingGearCruvesFrontView.add(
						- (i*1.1*aircraft.getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- aircraft.getLandingGears().getFrontalWheelsHeight().doubleValue(SI.METER)
						);
				seriesRightNoseLandingGearCruvesFrontView.add(
						- (i*1.1*aircraft.getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- aircraft.getLandingGears().getFrontalWheelsHeight().doubleValue(SI.METER)
						);
				seriesRightNoseLandingGearCruvesFrontView.add(
						- (i*1.1*aircraft.getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);

				serieNoseLandingGearsCruvesFrontViewList.add(seriesLeftNoseLandingGearCruvesFrontView);
				serieNoseLandingGearsCruvesFrontViewList.add(seriesRightNoseLandingGearCruvesFrontView);
			}
		}
		
		double yMaxFrontView = 1.20*aircraft.getWing().getSemiSpan().doubleValue(SI.METER);
		double yMinFrontView = -1.20*aircraft.getWing().getSemiSpan().doubleValue(SI.METRE);
		double zMaxFrontView = yMaxFrontView; 
		double zMinFrontView = yMinFrontView;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		Map<Double, Tuple2<XYSeries, Color>> componentXList = new HashMap<>();
		if (aircraft.getFuselage() != null) 
			componentXList.put(
					aircraft.getFuselage().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesFuselageCurve, Color.WHITE) 
					);
		if (aircraft.getWing() != null) 
			componentXList.put(
					aircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesWingFrontView, Color.decode("#87CEFA"))
					); 
		if (aircraft.getHTail() != null) 
			componentXList.put(
					aircraft.getHTail().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesHTailFrontView, Color.decode("#00008B"))
					);
		if (aircraft.getCanard() != null) 
			componentXList.put(
					aircraft.getCanard().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesCanardFrontView, Color.decode("#228B22"))
					);
		if (aircraft.getVTail() != null)
			componentXList.put(
					aircraft.getVTail().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesVTailFrontView, Color.decode("#FFD700"))
					);
		if (aircraft.getNacelles() != null) 
			seriesNacelleCruvesFrontViewList.stream().forEach(
					nac -> componentXList.put(
							aircraft.getNacelles().getNacellesList().get(
									seriesNacelleCruvesFrontViewList.indexOf(nac)
									).getXApexConstructionAxes().doubleValue(SI.METER)
							+ 0.005
							+ seriesNacelleCruvesFrontViewList.indexOf(nac)*0.005, 
							Tuple.of(nac, Color.decode("#FF7F50"))
							)
					);
		if (aircraft.getPowerPlant() != null) 
			seriesPropellerFrontViewList.stream().forEach(
					prop -> componentXList.put(
							aircraft.getPowerPlant().getEngineList().get(
									seriesPropellerFrontViewList.indexOf(prop)
									).getXApexConstructionAxes().doubleValue(SI.METER)
							+ 0.0015
							+ seriesPropellerFrontViewList.indexOf(prop)*0.001, 
							Tuple.of(prop, new Color(0f, 0f, 0f, 0.25f))
							)
					);
		if (aircraft.getLandingGears() != null) { 
			serieMainLandingGearsCruvesFrontViewList.stream().forEach(
					lg -> componentXList.put(
							aircraft.getLandingGears().getXApexConstructionAxesMainGear().doubleValue(SI.METER)
							+ serieMainLandingGearsCruvesFrontViewList.indexOf(lg)*0.001, 
							Tuple.of(lg, Color.decode("#404040"))
							)
					);
			serieNoseLandingGearsCruvesFrontViewList.stream().forEach(
					lg -> componentXList.put(
							aircraft.getLandingGears().getXApexConstructionAxesNoseGear().doubleValue(SI.METER)
							+ serieNoseLandingGearsCruvesFrontViewList.indexOf(lg)*0.001, 
							Tuple.of(lg, Color.decode("#404040"))
							)
					);
		}
		
		Map<Double, Tuple2<XYSeries, Color>> componentXListSorted = 
				componentXList.entrySet().stream()
			    .sorted(Entry.comparingByKey(Comparator.naturalOrder()))
			    .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
			                              (e1, e2) -> e1, LinkedHashMap::new));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		componentXListSorted.values().stream().forEach(t -> dataset.addSeries(t._1()));
		
		List<Color> colorList = new ArrayList<>();
		componentXListSorted.values().stream().forEach(t -> colorList.add(t._2()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Aircraft data representation - Front View", 
				"y (m)", 
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
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(yMinFrontView, yMaxFrontView);
		domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(zMinFrontView, zMaxFrontView);
		range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					colorList.get(i)
					);
		}
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
		
		plot.setRenderer(0, xyAreaRenderer);
		plot.setDataset(0, dataset);
		plot.setRenderer(1, xyLineAndShapeRenderer);
		plot.setDataset(1, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopViewSVG = outputDirectoryPath 
				+ File.separator 
				+ "AircraftFrontView.svg";
		File outputFileSVG = new File(outputFilePathTopViewSVG);
		if(outputFileSVG.exists()) outputFileSVG.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO PNG
		String outputFilePathTopViewPNG = outputDirectoryPath 
				+ File.separator 
				+ "AircraftFrontView.png";
		File outputFilePNG = new File(outputFilePathTopViewPNG);
		if(outputFilePNG.exists()) outputFilePNG.delete();
		
		try {
			ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void createFuselageTopView(Aircraft aircraft, String outputDirectoryPath) {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// left curve, upperview
		List<Amount<Length>> vX1Left = new ArrayList<>(); 
		aircraft.getFuselage().getOutlineXYSideLCurveAmountX().stream().forEach(x -> vX1Left.add(x));
		int nX1Left = vX1Left.size();
		List<Amount<Length>> vY1Left = new ArrayList<>(); 
		aircraft.getFuselage().getOutlineXYSideLCurveAmountY().stream().forEach(y -> vY1Left.add(y));

		// right curve, upperview
		List<Amount<Length>> vX2Right = new ArrayList<>(); 
		aircraft.getFuselage().getOutlineXYSideRCurveAmountX().stream().forEach(x -> vX2Right.add(x));
		int nX2Right = vX2Right.size();
		List<Amount<Length>> vY2Right = new ArrayList<>();
		aircraft.getFuselage().getOutlineXYSideRCurveAmountY().stream().forEach(y -> vY2Right.add(y));

		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		IntStream.range(0, nX1Left)
		.forEach(i -> {
			seriesFuselageCurve.add(vX1Left.get(i).doubleValue(SI.METRE), vY1Left.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nX2Right)
		.forEach(i -> {
			seriesFuselageCurve.add(vX2Right.get(vX2Right.size()-1-i).doubleValue(SI.METRE), vY2Right.get(vY2Right.size()-1-i).doubleValue(SI.METRE));
		});

		double xMaxTopView = 1.40*aircraft.getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -1.40*aircraft.getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = 1.20*aircraft.getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMinTopView = -0.20*aircraft.getFuselage().getFuselageLength().doubleValue(SI.METRE);

		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Fuselage data representation - Top View", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
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
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);
		range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
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

		plot.setRenderer(0, xyAreaRenderer);
		plot.setDataset(0, dataset);
		plot.setRenderer(1, xyLineAndShapeRenderer);
		plot.setDataset(1, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopViewSVG = outputDirectoryPath 
				+ File.separator 
				+ "FuselageTopView.svg";
		File outputFileSVG = new File(outputFilePathTopViewSVG);
		if(outputFileSVG.exists()) outputFileSVG.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO PNG
		String outputFilePathTopViewPNG = outputDirectoryPath 
				+ File.separator 
				+ "FuselageTopView.png";
		File outputFilePNG = new File(outputFilePathTopViewPNG);
		if(outputFilePNG.exists()) outputFilePNG.delete();
		
		try {
			ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void createFuselageSideView(Aircraft aircraft, String outputDirectoryPath) {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// upper curve, sideview
		List<Amount<Length>> vX1Upper = new ArrayList<>(); 
		aircraft.getFuselage().getOutlineXZUpperCurveAmountX().stream().forEach(x -> vX1Upper.add(x));
		int nX1Upper = vX1Upper.size();
		List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
		aircraft.getFuselage().getOutlineXZUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

		// lower curve, sideview
		List<Amount<Length>> vX2Lower = new ArrayList<>(); 
		aircraft.getFuselage().getOutlineXZLowerCurveAmountX().stream().forEach(x -> vX2Lower.add(x));
		int nX2Lower = vX2Lower.size();
		List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
		aircraft.getFuselage().getOutlineXZLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));

		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		IntStream.range(0, nX1Upper)
		.forEach(i -> {
			seriesFuselageCurve.add(vX1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nX2Lower)
		.forEach(i -> {
			seriesFuselageCurve.add(vX2Lower.get(vX2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
		});
		
		double xMaxSideView = 1.20*aircraft.getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double xMinSideView = -0.20*aircraft.getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMaxSideView = 1.40*aircraft.getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMinSideView = -1.40*aircraft.getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Fuselage data representation - Side View", 
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
		domain.setRange(xMinSideView, xMaxSideView);
		domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(yMinSideView, yMaxSideView);
		range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
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
		
		plot.setRenderer(0, xyAreaRenderer);
		plot.setDataset(0, dataset);
		plot.setRenderer(1, xyLineAndShapeRenderer);
		plot.setDataset(1, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopViewSVG = outputDirectoryPath 
				+ File.separator 
				+ "FuselageSideView.svg";
		File outputFileSVG = new File(outputFilePathTopViewSVG);
		if(outputFileSVG.exists()) outputFileSVG.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO PNG
		String outputFilePathTopViewPNG = outputDirectoryPath 
				+ File.separator 
				+ "FuselageSideView.png";
		File outputFilePNG = new File(outputFilePathTopViewPNG);
		if(outputFilePNG.exists()) outputFilePNG.delete();
		
		try {
			ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
	}
	
	public static void createFuselageFrontView(Aircraft aircraft, String outputDirectoryPath) {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// section upper curve
		List<Amount<Length>> vY1Upper = new ArrayList<>(); 
		aircraft.getFuselage().getSectionUpperCurveAmountY().stream().forEach(y -> vY1Upper.add(y));
		int nY1Upper = vY1Upper.size();
		List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
		aircraft.getFuselage().getSectionUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

		// section lower curve
		List<Amount<Length>> vY2Lower = new ArrayList<>(); 
		aircraft.getFuselage().getSectionLowerCurveAmountY().stream().forEach(y -> vY2Lower.add(y));
		int nY2Lower = vY2Lower.size();
		List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
		aircraft.getFuselage().getSectionLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));
		
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Front View", false);
		IntStream.range(0, nY1Upper)
		.forEach(i -> {
			seriesFuselageCurve.add(vY1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nY2Lower)
		.forEach(i -> {
			seriesFuselageCurve.add(vY2Lower.get(vY2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
		});
		
		double yMaxFrontView = 1.20*aircraft.getWing().getSemiSpan().doubleValue(SI.METER);
		double yMinFrontView = -1.20*aircraft.getWing().getSemiSpan().doubleValue(SI.METRE);
		double zMaxFrontView = yMaxFrontView; 
		double zMinFrontView = yMinFrontView;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Fuselage data representation - Front View", 
				"y (m)", 
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
		domain.setRange(yMinFrontView, yMaxFrontView);
		domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(zMinFrontView, zMaxFrontView);
		range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
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
		
		plot.setRenderer(0, xyAreaRenderer);
		plot.setDataset(0, dataset);
		plot.setRenderer(1, xyLineAndShapeRenderer);
		plot.setDataset(1, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopViewSVG = outputDirectoryPath 
				+ File.separator 
				+ "FuselageFrontView.svg";
		File outputFileSVG = new File(outputFilePathTopViewSVG);
		if(outputFileSVG.exists()) outputFileSVG.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO PNG
		String outputFilePathTopViewPNG = outputDirectoryPath 
				+ File.separator 
				+ "FuselageFrontView.png";
		File outputFilePNG = new File(outputFilePathTopViewPNG);
		if(outputFilePNG.exists()) outputFilePNG.delete();
		
		try {
			ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void createSeatMap(Aircraft aircraft, String outputDirectoryPath) {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// left curve, upperview
		List<Amount<Length>> vX1Left = new ArrayList<>(); 
		aircraft.getFuselage().getOutlineXYSideLCurveAmountX().stream().forEach(x -> vX1Left.add(x));
		int nX1Left = vX1Left.size();
		List<Amount<Length>> vY1Left = new ArrayList<>();
		aircraft.getFuselage().getOutlineXYSideLCurveAmountY().stream().forEach(y -> vY1Left.add(y));

		// right curve, upperview
		List<Amount<Length>> vX2Right = new ArrayList<>(); 
		aircraft.getFuselage().getOutlineXYSideRCurveAmountX().stream().forEach(x -> vX2Right.add(x));
		int nX2Right = vX2Right.size();
		List<Amount<Length>> vY2Right = new ArrayList<>(); 
		aircraft.getFuselage().getOutlineXYSideRCurveAmountY().stream().forEach(y -> vY2Right.add(y));

		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		IntStream.range(0, nX1Left)
		.forEach(i -> {
			seriesFuselageCurve.add(vX1Left.get(i).doubleValue(SI.METRE), vY1Left.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nX2Right)
		.forEach(i -> {
			seriesFuselageCurve.add(vX2Right.get(vX2Right.size()-1-i).doubleValue(SI.METRE), vY2Right.get(vY2Right.size()-1-i).doubleValue(SI.METRE));
		});

		//--------------------------------------------------
		// creating seat blocks outlines and seats (starting from FIRST class)
		//--------------------------------------------------
		List<SeatsBlock> seatBlockList = new ArrayList<>();
		List<XYSeries> seatBlockSeriesList = new ArrayList<>();
		List<XYSeries> seatsSeriesList = new ArrayList<>();
		
		Amount<Length> length = Amount.valueOf(0., SI.METER);
		Map<Integer, Amount<Length>> breaksMap = new HashMap<>();
		List<Map<Integer, Amount<Length>>> breaksMapList = new ArrayList<>();
		int classNumber = aircraft.getCabinConfiguration().getClassesNumber()-1;
		
		for (int i = 0; i < aircraft.getCabinConfiguration().getClassesNumber(); i++) {

			breaksMap.put(
					aircraft.getCabinConfiguration().getNumberOfBreaksList().get(classNumber-i), 
					aircraft.getCabinConfiguration().getWidthList().get(i)
					);
			breaksMapList.add(breaksMap);
			
			SeatsBlock seatsBlock = new SeatsBlock(
					new ISeatBlock.Builder()
					.setPosition(RelativePositionEnum.RIGHT)
					.setXStart(aircraft.getCabinConfiguration().getXCoordinatesFirstRow().plus(length))
					.setPitch(aircraft.getCabinConfiguration().getPitchList().get(classNumber-i))
					.setWidth(aircraft.getCabinConfiguration().getWidthList().get(classNumber-i))
					.setDistanceFromWall(aircraft.getCabinConfiguration().getDistanceFromWallList().get(classNumber-i))
					.putAllBreaksMap(breaksMapList.get(i))
					.setRowsNumber(aircraft.getCabinConfiguration().getNumberOfRowsList().get(classNumber-i))
					.setColumnsNumber(aircraft.getCabinConfiguration().getNumberOfColumnsList().get(classNumber-i)[0])
					.setType(aircraft.getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(classNumber-i))					
					.build()
					);

			seatBlockList.add(seatsBlock);
			//........................................................................................................
			XYSeries seriesSeatBlock = new XYSeries("Seat Block " + i + " start", false);
			seriesSeatBlock.add(
					aircraft.getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
					+ length.doubleValue(SI.METER),
					- FusNacGeometryCalc.getWidthAtX(
							aircraft.getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
							+ length.doubleValue(SI.METER),
							aircraft.getFuselage().getOutlineXYSideRCurveX(),
							aircraft.getFuselage().getOutlineXYSideRCurveY()
							)/2
					+ aircraft.getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
					);
			seriesSeatBlock.add(
					aircraft.getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
					+ length.doubleValue(SI.METER),
					FusNacGeometryCalc.getWidthAtX(
							aircraft.getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
							+ length.doubleValue(SI.METER),
							aircraft.getFuselage().getOutlineXYSideRCurveX(),
							aircraft.getFuselage().getOutlineXYSideRCurveY()
							)/2
					- aircraft.getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
					);
			//........................................................................................................
			Amount<Length> aisleWidth = Amount.valueOf(0.0, SI.METER);
			Amount<Length> currentYPosition = Amount.valueOf(0.0, SI.METER);
			Double breakLengthPitchFraction = 0.25;
			List<Integer> breakesPositionsIndexList = new ArrayList<>();
			for (int iBrake=0; iBrake<aircraft.getCabinConfiguration().getNumberOfBreaksList().get(classNumber-i); iBrake++) {
				Integer brekesInteval = Math.round(
						(aircraft.getCabinConfiguration().getNumberOfRowsList().get(classNumber-i)
						+ aircraft.getCabinConfiguration().getNumberOfBreaksList().get(classNumber-i))
						/ (aircraft.getCabinConfiguration().getNumberOfBreaksList().get(classNumber-i) + 1)
						);
				breakesPositionsIndexList.add((iBrake+1)*brekesInteval);
			}
			
			for(int j=0; j<aircraft.getCabinConfiguration().getNumberOfColumnsList().get(classNumber-i).length; j++) {
				if(j>0) {
					aisleWidth = Amount.valueOf( 
							(aircraft.getFuselage().getSectionCylinderWidth().doubleValue(SI.METER) 
									- (MyArrayUtils.sumArrayElements(aircraft.getCabinConfiguration().getNumberOfColumnsList().get(classNumber-i))
											* aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER))
									- 2*aircraft.getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER))
							/aircraft.getCabinConfiguration().getAislesNumber(),
							SI.METER
							);
					currentYPosition = Amount.valueOf(
							seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getYValue(),
							SI.METER
							);
				}
				for (int k = 0; k <aircraft.getCabinConfiguration().getNumberOfColumnsList().get(classNumber-i)[j]; k++) {
					
					int indexOfCurrentBrake = 10000;
					
					for (int r = 0;
							 r < aircraft.getCabinConfiguration().getNumberOfRowsList().get(classNumber-i); 
							 r++) {
						
						XYSeries seriesSeats = new XYSeries("Column " + i + j + k + r, false);
						if(j>0) {
							int rowIndex = r;
							if(breakesPositionsIndexList.stream().anyMatch(x -> x == rowIndex)) {
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ breakLengthPitchFraction * aircraft.getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER)
										+ aircraft.getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										currentYPosition.doubleValue(SI.METER)
										- aisleWidth.doubleValue(SI.METER)
										- aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										- k * aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
								indexOfCurrentBrake = r;
							}
							else if (r > indexOfCurrentBrake)
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ aircraft.getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										currentYPosition.doubleValue(SI.METER)
										- aisleWidth.doubleValue(SI.METER)
										- aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										- k * aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
							else
								seriesSeats.add(
										aircraft.getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METER)
										+ length.doubleValue(SI.METER)
										+ aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										+ r * aircraft.getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										currentYPosition.doubleValue(SI.METER)
										- aisleWidth.doubleValue(SI.METER)
										- aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										- k * aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
						}
						else {
							int columnIndex = r;
							if(breakesPositionsIndexList.stream().anyMatch(x -> x == columnIndex)) {
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ breakLengthPitchFraction * aircraft.getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER)
										+ aircraft.getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										aircraft.getFuselage().getSectionCylinderWidth().divide(2).doubleValue(SI.METER)
										- aircraft.getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
										- aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).divide(2).doubleValue(SI.METER)
										- k * aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
								indexOfCurrentBrake = r;
							}
							else if (r > indexOfCurrentBrake)
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ aircraft.getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										aircraft.getFuselage().getSectionCylinderWidth().divide(2).doubleValue(SI.METER)
										- aircraft.getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
										- aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).divide(2).doubleValue(SI.METER)
										- k * aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
							else
								seriesSeats.add(
										aircraft.getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METER)
										+ length.doubleValue(SI.METER)
										+ aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										+ r * aircraft.getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										aircraft.getFuselage().getSectionCylinderWidth().divide(2).doubleValue(SI.METER)
										- aircraft.getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
										- aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).divide(2).doubleValue(SI.METER)
										- k * aircraft.getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
								
						}
						seatsSeriesList.add(seriesSeats);
						
					}
				}				
			}
		
			length = length.plus(seatsBlock.getLenghtOverall());
			seatBlockSeriesList.add(seriesSeatBlock);
			
		}
		
		XYSeries seriesSeatBlock = new XYSeries("Seat Block " + classNumber + " ending", false);
		seriesSeatBlock.add(
				aircraft.getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
				+ length.doubleValue(SI.METER),
				- FusNacGeometryCalc.getWidthAtX(
						aircraft.getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
						+ length.doubleValue(SI.METER),
						aircraft.getFuselage().getOutlineXYSideRCurveX(),
						aircraft.getFuselage().getOutlineXYSideRCurveY()
						)/2
				+ aircraft.getCabinConfiguration().getDistanceFromWallList().get(classNumber).doubleValue(SI.METER)
				);
		seriesSeatBlock.add(
				aircraft.getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
				+ length.doubleValue(SI.METER),
				FusNacGeometryCalc.getWidthAtX(
						aircraft.getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
						+ length.doubleValue(SI.METER),
						aircraft.getFuselage().getOutlineXYSideRCurveX(),
						aircraft.getFuselage().getOutlineXYSideRCurveY()
						)/2
				- aircraft.getCabinConfiguration().getDistanceFromWallList().get(classNumber).doubleValue(SI.METER)
				);
		seatBlockSeriesList.add(seriesSeatBlock);
		
		double xMaxTopView = aircraft.getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -aircraft.getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = aircraft.getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMinTopView = 0.0;

		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seatsSeriesList.stream().forEach(
				s -> seriesAndColorList.add(Tuple.of(s, Color.WHITE))
				);
		seatBlockSeriesList.stream().forEach(
				sb -> seriesAndColorList.add(Tuple.of(sb, Color.WHITE))
				);
		seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Seat Map representation", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
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
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);
		range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
		
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if(i<seatsSeriesList.size()) {
				xyLineAndShapeRenderer.setSeriesLinesVisible(i, false);
				xyLineAndShapeRenderer.setSeriesShapesVisible(i, true);
				xyLineAndShapeRenderer.setSeriesShape(
						i,
						ShapeUtils.createDiamond(2.5f).getBounds()
						);
			}
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopViewSVG = outputDirectoryPath 
				+ File.separator 
				+ "SeatMap.svg";
		File outputFileSVG = new File(outputFilePathTopViewSVG);
		if(outputFileSVG.exists()) outputFileSVG.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO PNG
		String outputFilePathTopViewPNG = outputDirectoryPath 
				+ File.separator 
				+ "SeatMap.png";
		File outputFilePNG = new File(outputFilePathTopViewPNG);
		if(outputFilePNG.exists()) outputFilePNG.delete();
		
		try {
			ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void createWingPlanformView(Aircraft aircraft, String outputDirectoryPath) {
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = aircraft.getWing().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		
		XYSeries seriesWingTopView = new XYSeries("Wing Planform", false);
		IntStream.range(0, dataTopViewIsolated.length)
		.forEach(i -> {
			seriesWingTopView.add(
					dataTopViewIsolated[i][1],
					dataTopViewIsolated[i][0]
					);
		});
		seriesWingTopView.add(
				dataTopViewIsolated[0][1],
				dataTopViewIsolated[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from flaps
		//--------------------------------------------------
		List<XYSeries> seriesFlapsTopViewList = new ArrayList<>();
		if (!aircraft.getWing().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<aircraft.getWing().getSymmetricFlaps().size(); i++) {
				
				double yIn = aircraft.getWing().getSemiSpan().times(
						aircraft.getWing().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = aircraft.getWing().getSemiSpan().times(
						aircraft.getWing().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = aircraft.getWing().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = aircraft.getWing().getSymmetricFlaps().get(i).getOuterChordRatio();
				
				XYSeries seriesFlapsTopView = new XYSeries("Flap" + i, false);
				seriesFlapsTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
				seriesFlapsTopView.add(
						xLELocalInnerChord + (localChordInner),
						yIn
						);
				seriesFlapsTopView.add(
						xLELocalOuterChord + (localChordOuter),
						yOut
						);
				seriesFlapsTopView.add(
						xLELocalOuterChord + (localChordOuter*(1-outerChordRatio)),
						yOut
						);
				seriesFlapsTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
			
				seriesFlapsTopViewList.add(seriesFlapsTopView);
			}
		}
		//--------------------------------------------------
		// get data vectors from slats
		//--------------------------------------------------
		List<XYSeries> seriesSlatsTopViewList = new ArrayList<>();
		if (!aircraft.getWing().getSlats().isEmpty()) {
			for(int i=0; i<aircraft.getWing().getSlats().size(); i++) {
				
				double yIn = aircraft.getWing().getSemiSpan().times(
						aircraft.getWing().getSlats().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = aircraft.getWing().getSemiSpan().times(
						aircraft.getWing().getSlats().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = aircraft.getWing().getSlats().get(i).getInnerChordRatio();
				double outerChordRatio = aircraft.getWing().getSlats().get(i).getOuterChordRatio();
				
				XYSeries seriesSlatTopView = new XYSeries("Slat" + i, false);
				seriesSlatTopView.add(
						xLELocalInnerChord,
						yIn
						);
				seriesSlatTopView.add(
						xLELocalInnerChord + (innerChordRatio*localChordInner),
						yIn
						);
				seriesSlatTopView.add(
						xLELocalOuterChord + (outerChordRatio*localChordOuter),
						yOut
						);
				seriesSlatTopView.add(
						xLELocalOuterChord,
						yOut
						);
				seriesSlatTopView.add(
						xLELocalInnerChord,
						yIn
						);

				seriesSlatsTopViewList.add(seriesSlatTopView);
			}
		}
		//--------------------------------------------------
		// get data vectors from ailerons
		//--------------------------------------------------
		XYSeries seriesAileronTopView = new XYSeries("Right Slat", false);
		
		if (!aircraft.getWing().getAsymmetricFlaps().isEmpty()) {
			
			double yIn = aircraft.getWing().getSemiSpan().times(
					aircraft.getWing().getAsymmetricFlaps().get(0).getInnerStationSpanwisePosition()
					).doubleValue(SI.METER);
			double yOut = aircraft.getWing().getSemiSpan().times(
					aircraft.getWing().getAsymmetricFlaps().get(0).getOuterStationSpanwisePosition()
					).doubleValue(SI.METER);

			double localChordInner = GeometryCalc.getChordAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedChords()),
					yIn);
			double localChordOuter = GeometryCalc.getChordAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedChords()),
					yOut);

			double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedXle()),
					yIn);
			double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedXle()),
					yOut);

			double innerChordRatio = aircraft.getWing().getAsymmetricFlaps().get(0).getInnerChordRatio();
			double outerChordRatio = aircraft.getWing().getAsymmetricFlaps().get(0).getOuterChordRatio();

			seriesAileronTopView.add(
					xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
					yIn
					);
			seriesAileronTopView.add(
					xLELocalInnerChord + (localChordInner),
					yIn
					);
			seriesAileronTopView.add(
					xLELocalOuterChord + (localChordOuter),
					yOut
					);
			seriesAileronTopView.add(
					xLELocalOuterChord + (localChordOuter*(1-outerChordRatio)),
					yOut
					);
			seriesAileronTopView.add(
					xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
					yIn
					);

		}
		//--------------------------------------------------
		// get data vectors from spoilers
		//--------------------------------------------------
		List<XYSeries> seriesSpoilersTopViewList = new ArrayList<>();
		if (!aircraft.getWing().getSpoilers().isEmpty()) {
			for(int i=0; i<aircraft.getWing().getSpoilers().size(); i++) {
				
				double yIn = aircraft.getWing().getSemiSpan().times(
						aircraft.getWing().getSpoilers().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = aircraft.getWing().getSemiSpan().times(
						aircraft.getWing().getSpoilers().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getWing().getDiscretizedXle()),
						yOut);
				
				double innerChordwisePosition = aircraft.getWing().getSpoilers().get(i).getInnerStationChordwisePosition();
				double outerChordwisePosition = aircraft.getWing().getSpoilers().get(i).getOuterStationChordwisePosition();
				
				XYSeries seriesSpoilerTopView = new XYSeries("Spoiler" + i, false);
				seriesSpoilerTopView.add(
						xLELocalInnerChord + (localChordInner*innerChordwisePosition),
						yIn
						);
				seriesSpoilerTopView.add(
						xLELocalInnerChord + (localChordInner*outerChordwisePosition),
						yIn
						);
				seriesSpoilerTopView.add(
						xLELocalOuterChord + (localChordOuter*outerChordwisePosition),
						yOut
						);
				seriesSpoilerTopView.add(
						xLELocalOuterChord + (localChordOuter*innerChordwisePosition),
						yOut
						);
				seriesSpoilerTopView.add(
						xLELocalInnerChord + (localChordInner*innerChordwisePosition),
						yIn
						);

				seriesSpoilersTopViewList.add(seriesSpoilerTopView);
			}
		}
		
		double semispan = aircraft.getWing().getSemiSpan().doubleValue(SI.METER);
		double rootChord = aircraft.getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = aircraft.getWing().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = aircraft.getWing().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = aircraft.getWing().getDiscretizedXle().get(
				aircraft.getWing().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*aircraft.getWing().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*aircraft.getWing().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*aircraft.getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*aircraft.getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*aircraft.getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesFlapsTopViewList.stream().forEach(
				flap -> seriesAndColorList.add(Tuple.of(flap, Color.decode("#778899")))
				);
		seriesSlatsTopViewList.stream().forEach(
				slat -> seriesAndColorList.add(Tuple.of(slat, Color.decode("#6495ED")))
				);
		seriesAndColorList.add(Tuple.of(seriesAileronTopView, Color.decode("#1E90FF")));
		seriesSpoilersTopViewList.stream().forEach(
				spoiler -> seriesAndColorList.add(Tuple.of(spoiler, Color.decode("#ADD8E6")))
				);
		seriesAndColorList.add(Tuple.of(seriesWingTopView, Color.decode("#87CEFA")));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Wing Planform representation", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
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
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);
		range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
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
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopViewSVG = outputDirectoryPath 
				+ File.separator 
				+ "WingPlanform.svg";
		File outputFileSVG = new File(outputFilePathTopViewSVG);
		if(outputFileSVG.exists()) outputFileSVG.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO PNG
		String outputFilePathTopViewPNG = outputDirectoryPath 
				+ File.separator 
				+ "WingPlanform.png";
		File outputFilePNG = new File(outputFilePathTopViewPNG);
		if(outputFilePNG.exists()) outputFilePNG.delete();
		
		try {
			ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void createEquivalentWingView(Aircraft aircraft, String outputDirectoryPath) {
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = aircraft.getWing().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		
		XYSeries seriesWingTopView = new XYSeries("Wing Planform", false);
		IntStream.range(0, dataTopViewIsolated.length)
		.forEach(i -> {
			seriesWingTopView.add(
					dataTopViewIsolated[i][1],
					dataTopViewIsolated[i][0]
					);
		});
		seriesWingTopView.add(
				dataTopViewIsolated[0][1],
				dataTopViewIsolated[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from equivalent wing discretization
		//--------------------------------------------------
		int nSec = aircraft.getWing().getDiscretizedXle().size();
		int nPanels = aircraft.getWing().getPanels().size();

		XYSeries seriesEquivalentWingTopView = new XYSeries("Equivalent Wing", false);
		seriesEquivalentWingTopView.add(
				aircraft.getWing().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordLE(),
				0.0
				);
		seriesEquivalentWingTopView.add(
				aircraft.getWing().getDiscretizedXle().get(nSec - 1).doubleValue(SI.METER),
				aircraft.getWing().getSemiSpan().doubleValue(SI.METER)
				);
		seriesEquivalentWingTopView.add(
				aircraft.getWing().getDiscretizedXle().get(nSec - 1).plus(
						aircraft.getWing().getPanels().get(nPanels - 1).getChordTip()
						).doubleValue(SI.METER),
				aircraft.getWing().getSemiSpan().doubleValue(SI.METER)
				);
		seriesEquivalentWingTopView.add(
				aircraft.getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
				- aircraft.getWing().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordTE(),
				0.0
				);
		seriesEquivalentWingTopView.add(
				aircraft.getWing().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordLE(),
				0.0
				);
		
		XYSeries seriesMeanAerodinamicChordView = new XYSeries("M.A.C.", false);
		seriesMeanAerodinamicChordView.add(
				aircraft.getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE),
				aircraft.getWing().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				aircraft.getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ aircraft.getWing().getMeanAerodynamicChord().doubleValue(SI.METRE),
				aircraft.getWing().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		
		double semispan = aircraft.getWing().getSemiSpan().doubleValue(SI.METER);
		double rootChord = aircraft.getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = aircraft.getWing().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = aircraft.getWing().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = aircraft.getWing().getDiscretizedXle().get(
				aircraft.getWing().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*aircraft.getWing().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*aircraft.getWing().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*aircraft.getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*aircraft.getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*aircraft.getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
			
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesEquivalentWingTopView, Color.decode("#87CEFA")));
		seriesAndColorList.add(Tuple.of(seriesWingTopView, Color.decode("#87CEFA")));
		seriesAndColorList.add(Tuple.of(seriesMeanAerodinamicChordView, Color.BLACK));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Wing Planform representation", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
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
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);
		range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		xyAreaRenderer.setDefaultSeriesVisible(false);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if(i==0)
				xyAreaRenderer.setSeriesVisible(i, true);
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		xyLineAndShapeRenderer.setDrawSeriesLineAsPath(true);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if (i==1 || i==2) {
				xyLineAndShapeRenderer.setSeriesStroke(
						i,
						new BasicStroke(
								2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
								1.0f, new float[] {5.0f}, 1.0f));
			}
			else {
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
			}
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopViewSVG = outputDirectoryPath 
				+ File.separator 
				+ "EquivalentWing.svg";
		File outputFileSVG = new File(outputFilePathTopViewSVG);
		if(outputFileSVG.exists()) outputFileSVG.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO PNG
		String outputFilePathTopViewPNG = outputDirectoryPath 
				+ File.separator 
				+ "EquivalentWing.png";
		File outputFilePNG = new File(outputFilePathTopViewPNG);
		if(outputFilePNG.exists()) outputFilePNG.delete();
		
		try {
			ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void createHTailPlanformView(Aircraft aircraft, String outputDirectoryPath) {
		
		//--------------------------------------------------
		// get data vectors from HTail discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = aircraft.getHTail().getDiscretizedTopViewAsArray(ComponentEnum.HORIZONTAL_TAIL);
		
		XYSeries seriesHTailTopView = new XYSeries("HTail Planform", false);
		IntStream.range(0, dataTopViewIsolated.length)
		.forEach(i -> {
			seriesHTailTopView.add(
					dataTopViewIsolated[i][1],
					dataTopViewIsolated[i][0]
					);
		});
		seriesHTailTopView.add(
				dataTopViewIsolated[0][1],
				dataTopViewIsolated[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from flaps
		//--------------------------------------------------
		List<XYSeries> seriesElevatorsTopViewList = new ArrayList<>();
		if (!aircraft.getHTail().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<aircraft.getHTail().getSymmetricFlaps().size(); i++) {
				
				double yIn = aircraft.getHTail().getSemiSpan().times(
						aircraft.getHTail().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = aircraft.getHTail().getSemiSpan().times(
						aircraft.getHTail().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getHTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getHTail().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getHTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getHTail().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getHTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getHTail().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getHTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getHTail().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = aircraft.getHTail().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = aircraft.getHTail().getSymmetricFlaps().get(i).getOuterChordRatio();
				
				XYSeries seriesElevatorTopView = new XYSeries("Elevator" + i, false);
				seriesElevatorTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
				seriesElevatorTopView.add(
						xLELocalInnerChord + (localChordInner),
						yIn
						);
				seriesElevatorTopView.add(
						xLELocalOuterChord + (localChordOuter),
						yOut
						);
				seriesElevatorTopView.add(
						xLELocalOuterChord + (localChordOuter*(1-outerChordRatio)),
						yOut
						);
				seriesElevatorTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
			
				seriesElevatorsTopViewList.add(seriesElevatorTopView);
			}
		}
		
		XYSeries seriesMeanAerodinamicChordView = new XYSeries("M.A.C.", false);
		seriesMeanAerodinamicChordView.add(
				aircraft.getHTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE),
				aircraft.getHTail().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				aircraft.getHTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ aircraft.getHTail().getMeanAerodynamicChord().doubleValue(SI.METRE),
				aircraft.getHTail().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		
		double semispan = aircraft.getHTail().getSemiSpan().doubleValue(SI.METER);
		double rootChord = aircraft.getHTail().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = aircraft.getHTail().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = aircraft.getHTail().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = aircraft.getHTail().getDiscretizedXle().get(
				aircraft.getHTail().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*aircraft.getHTail().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*aircraft.getHTail().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*aircraft.getHTail().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*aircraft.getHTail().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*aircraft.getHTail().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesMeanAerodinamicChordView, Color.BLACK));
		seriesElevatorsTopViewList.stream().forEach(
				flap -> seriesAndColorList.add(Tuple.of(flap, Color.decode("#0066CC")))
				);
		seriesAndColorList.add(Tuple.of(seriesHTailTopView, Color.decode("#00008B")));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Horizontal Tail Planform representation", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
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
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);
		range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if(i==0)
				xyAreaRenderer.setSeriesVisible(i, false);
			else
				xyAreaRenderer.setSeriesPaint(
						i,
						seriesAndColorList.get(i)._2()
						);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if (i==0) {
				xyLineAndShapeRenderer.setSeriesStroke(
						i,
						new BasicStroke(
								2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
								1.0f, new float[] {5.0f}, 1.0f));
			}
			else {
				xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						i, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopViewSVG = outputDirectoryPath 
				+ File.separator 
				+ "HorizontalTailPlanform.svg";
		File outputFileSVG = new File(outputFilePathTopViewSVG);
		if(outputFileSVG.exists()) outputFileSVG.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO PNG
		String outputFilePathTopViewPNG = outputDirectoryPath 
				+ File.separator 
				+ "HorizontalTailPlanform.png";
		File outputFilePNG = new File(outputFilePathTopViewPNG);
		if(outputFilePNG.exists()) outputFilePNG.delete();
		
		try {
			ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void createVTailPlanformView(Aircraft aircraft, String outputDirectoryPath) {
		
		//--------------------------------------------------
		// get data vectors from VTail discretization
		//--------------------------------------------------
		Double[][] dataTopViewVTail = aircraft.getVTail().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);
		
		XYSeries seriesVTailTopView = new XYSeries("VTail", false);
		IntStream.range(0, dataTopViewVTail.length)
		.forEach(i -> {
			seriesVTailTopView.add(
					dataTopViewVTail[i][1],
					dataTopViewVTail[i][0]
					);
		});
		seriesVTailTopView.add(
				dataTopViewVTail[0][1],
				dataTopViewVTail[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from rudders
		//--------------------------------------------------
		List<XYSeries> seriesRudderTopViewList = new ArrayList<>();
		if (!aircraft.getVTail().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<aircraft.getVTail().getSymmetricFlaps().size(); i++) {
				
				double yIn = aircraft.getVTail().getSemiSpan().times(
						aircraft.getVTail().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = aircraft.getVTail().getSemiSpan().times(
						aircraft.getVTail().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getVTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getVTail().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getVTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getVTail().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getVTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getVTail().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getVTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getVTail().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = aircraft.getVTail().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = aircraft.getVTail().getSymmetricFlaps().get(i).getOuterChordRatio();
				
				XYSeries seriesRudderTopView = new XYSeries("Rudder" + i, false);
				seriesRudderTopView.add(
						yIn,
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio))
						);
				seriesRudderTopView.add(
						yIn,
						xLELocalInnerChord + (localChordInner)
						);
				seriesRudderTopView.add(
						yOut,
						xLELocalOuterChord + (localChordOuter)
						);
				seriesRudderTopView.add(
						yOut,
						xLELocalOuterChord + (localChordOuter*(1-outerChordRatio))
						);
				seriesRudderTopView.add(
						yIn,
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio))
						);
			
				seriesRudderTopViewList.add(seriesRudderTopView);
			}
		}
		
		XYSeries seriesMeanAerodinamicChordView = new XYSeries("M.A.C.", false);
		seriesMeanAerodinamicChordView.add(
				aircraft.getVTail().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE),
				aircraft.getVTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				aircraft.getVTail().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE),
				aircraft.getVTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ aircraft.getVTail().getMeanAerodynamicChord().doubleValue(SI.METRE)
				);
		
		double span = aircraft.getVTail().getSpan().doubleValue(SI.METER);
		double rootChord = aircraft.getVTail().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = aircraft.getVTail().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = aircraft.getVTail().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = aircraft.getVTail().getDiscretizedXle().get(
				aircraft.getVTail().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - span/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*span) {
			
			xMaxTopView = 1.05*aircraft.getVTail().getSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*aircraft.getVTail().getSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*aircraft.getVTail().getSpan().doubleValue(SI.METRE) 
					+ 0.5*aircraft.getVTail().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*aircraft.getVTail().getSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesMeanAerodinamicChordView, Color.BLACK));
		seriesRudderTopViewList.stream().forEach(
				flap -> seriesAndColorList.add(Tuple.of(flap, Color.decode("#FF8C00")))
				);
		seriesAndColorList.add(Tuple.of(seriesVTailTopView, Color.decode("#FFD700")));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Vertical Tail Planform representation", 
				"z (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
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
		domain.setRange(xMinTopView, xMaxTopView);
		domain.setInverted(Boolean.FALSE);
		domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(yMinTopView, yMaxTopView);
		range.setInverted(Boolean.FALSE);
		range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if(i==0)
				xyAreaRenderer.setSeriesVisible(i, false);
			else
				xyAreaRenderer.setSeriesPaint(
						i,
						seriesAndColorList.get(i)._2()
						);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if (i==0) {
				xyLineAndShapeRenderer.setSeriesStroke(
						i,
						new BasicStroke(
								2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
								1.0f, new float[] {5.0f}, 1.0f));
			}
			else {
				xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						i, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopViewSVG = outputDirectoryPath 
				+ File.separator 
				+ "VerticalTailPlanform.svg";
		File outputFileSVG = new File(outputFilePathTopViewSVG);
		if(outputFileSVG.exists()) outputFileSVG.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO PNG
		String outputFilePathTopViewPNG = outputDirectoryPath 
				+ File.separator 
				+ "VerticalTailPlanform.png";
		File outputFilePNG = new File(outputFilePathTopViewPNG);
		if(outputFilePNG.exists()) outputFilePNG.delete();
		
		try {
			ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void createCanardPlanformView(Aircraft aircraft, String outputDirectoryPath) {
		
		//--------------------------------------------------
		// get data vectors from Canard discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = aircraft.getCanard().getDiscretizedTopViewAsArray(ComponentEnum.CANARD);
		
		XYSeries seriesCanardTopView = new XYSeries("Canard Planform", false);
		IntStream.range(0, dataTopViewIsolated.length)
		.forEach(i -> {
			seriesCanardTopView.add(
					dataTopViewIsolated[i][1],
					dataTopViewIsolated[i][0]
					);
		});
		seriesCanardTopView.add(
				dataTopViewIsolated[0][1],
				dataTopViewIsolated[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from control surface
		//--------------------------------------------------
		List<XYSeries> seriesControlSurfacesTopViewList = new ArrayList<>();
		if (!aircraft.getCanard().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<aircraft.getCanard().getSymmetricFlaps().size(); i++) {
				
				double yIn = aircraft.getCanard().getSemiSpan().times(
						aircraft.getCanard().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = aircraft.getCanard().getSemiSpan().times(
						aircraft.getCanard().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getCanard().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getCanard().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getCanard().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getCanard().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getCanard().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getCanard().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getCanard().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(aircraft.getCanard().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = aircraft.getCanard().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = aircraft.getCanard().getSymmetricFlaps().get(i).getOuterChordRatio();
				
				XYSeries seriesControlSurfaceTopView = new XYSeries("Control Surface" + i, false);
				seriesControlSurfaceTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
				seriesControlSurfaceTopView.add(
						xLELocalInnerChord + (localChordInner),
						yIn
						);
				seriesControlSurfaceTopView.add(
						xLELocalOuterChord + (localChordOuter),
						yOut
						);
				seriesControlSurfaceTopView.add(
						xLELocalOuterChord + (localChordOuter*(1-outerChordRatio)),
						yOut
						);
				seriesControlSurfaceTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
			
				seriesControlSurfacesTopViewList.add(seriesControlSurfaceTopView);
			}
		}
		
		XYSeries seriesMeanAerodinamicChordView = new XYSeries("M.A.C.", false);
		seriesMeanAerodinamicChordView.add(
				aircraft.getCanard().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE),
				aircraft.getCanard().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				aircraft.getCanard().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ aircraft.getCanard().getMeanAerodynamicChord().doubleValue(SI.METRE),
				aircraft.getCanard().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		
		double semispan = aircraft.getCanard().getSemiSpan().doubleValue(SI.METER);
		double rootChord = aircraft.getCanard().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = aircraft.getCanard().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = aircraft.getCanard().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = aircraft.getCanard().getDiscretizedXle().get(
				aircraft.getCanard().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*aircraft.getCanard().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*aircraft.getCanard().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*aircraft.getCanard().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*aircraft.getCanard().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*aircraft.getCanard().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesMeanAerodinamicChordView, Color.BLACK));
		seriesControlSurfacesTopViewList.stream().forEach(
				flap -> seriesAndColorList.add(Tuple.of(flap, Color.decode("#32CD32")))
				);
		seriesAndColorList.add(Tuple.of(seriesCanardTopView, Color.decode("#228B22")));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Canard Planform representation", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
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
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);
		range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if(i==0)
				xyAreaRenderer.setSeriesVisible(i, false);
			else
				xyAreaRenderer.setSeriesPaint(
						i,
						seriesAndColorList.get(i)._2()
						);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if (i==0) {
				xyLineAndShapeRenderer.setSeriesStroke(
						i,
						new BasicStroke(
								2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
								1.0f, new float[] {5.0f}, 1.0f));
			}
			else {
				xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						i, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopViewSVG = outputDirectoryPath 
				+ File.separator 
				+ "CanardPlanform.svg";
		File outputFileSVG = new File(outputFilePathTopViewSVG);
		if(outputFileSVG.exists()) outputFileSVG.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO PNG
		String outputFilePathTopViewPNG = outputDirectoryPath 
				+ File.separator 
				+ "CanardPlanform.png";
		File outputFilePNG = new File(outputFilePathTopViewPNG);
		if(outputFilePNG.exists()) outputFilePNG.delete();
		
		try {
			ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void createNacelleTopView(Aircraft aircraft, String outputDirectoryPath) {

		//---------------------------------------------------------------------------------
		// LOOP OVER NACELLES:
		for (int i=0; i<aircraft.getNacelles().getNacellesList().size(); i++) {

			//--------------------------------------------------
			// get data vectors from nacelle discretization
			//--------------------------------------------------

			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
			aircraft.getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperY = new ArrayList<>(); 
			aircraft.getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYRight().stream().forEach(y -> nacelleCurveUpperY.add(y));

			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerY = new ArrayList<>(); 
			aircraft.getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYLeft().stream().forEach(y -> nacelleCurveLowerY.add(y));

			List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
			List<Amount<Length>> dataOutlineXZCurveNacelleY = new ArrayList<>();

			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j));
				dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(j));
			}

			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1) );
				dataOutlineXZCurveNacelleY.add(nacelleCurveLowerY.get(nacelleCurveXPoints-j-1));
			}

			dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0));
			dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(0));

			XYSeries seriesNacelleCruvesTopView = new XYSeries("Nacelle " + i + " XZ Curve - Top View", false);
			IntStream.range(0, dataOutlineXZCurveNacelleX.size())
			.forEach(k -> {
				seriesNacelleCruvesTopView.add(
						dataOutlineXZCurveNacelleX.get(k).doubleValue(SI.METER),
						dataOutlineXZCurveNacelleY.get(k).doubleValue(SI.METER)
						);
			});

			double xMaxTopView = 1.40*aircraft.getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double xMinTopView = -1.40*aircraft.getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double yMaxTopView = 1.20*aircraft.getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);
			double yMinTopView = -0.20*aircraft.getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);

			//-------------------------------------------------------------------------------------
			// DATASET CRATION
			List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
			seriesAndColorList.add(Tuple.of(seriesNacelleCruvesTopView, Color.decode("#FF7F50")));

			XYSeriesCollection dataset = new XYSeriesCollection();
			seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

			//-------------------------------------------------------------------------------------
			// CHART CRATION
			JFreeChart chart = ChartFactory.createXYAreaChart(
					"Nacelle data representation - Top View", 
					"y (m)", 
					"x (m)",
					(XYDataset) dataset,
					PlotOrientation.HORIZONTAL,
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
			domain.setRange(yMinTopView, yMaxTopView);
			domain.setInverted(Boolean.TRUE);
			domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
			domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
			NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
			range.setRange(xMinTopView, xMaxTopView);
			range.setInverted(Boolean.FALSE);
			range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
			range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

			XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
			xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
			xyAreaRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyAreaRenderer.setSeriesPaint(
						j,
						seriesAndColorList.get(j)._2()
						);
			}
			XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
			xyLineAndShapeRenderer.setDefaultShapesVisible(false);
			xyLineAndShapeRenderer.setDefaultLinesVisible(true);
			xyLineAndShapeRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyLineAndShapeRenderer.setSeriesPaint(j, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						j, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}

			plot.setRenderer(0, xyAreaRenderer);
			plot.setDataset(0, dataset);
			plot.setRenderer(1, xyLineAndShapeRenderer);
			plot.setDataset(1, dataset);

			//-------------------------------------------------------------------------------------
			// EXPORT TO SVG
			String outputFilePathTopViewSVG = outputDirectoryPath 
					+ File.separator 
					+ "NacelleTopView_" + i + ".svg";
			File outputFileSVG = new File(outputFilePathTopViewSVG);
			if(outputFileSVG.exists()) outputFileSVG.delete();
			SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
			Rectangle r = new Rectangle(WIDTH, HEIGHT);
			chart.draw(g2, r);
			try {
				SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
			} catch (IOException e) {
				e.printStackTrace();
			}

			//-------------------------------------------------------------------------------------
			// EXPORT TO PNG
			String outputFilePathTopViewPNG = outputDirectoryPath 
					+ File.separator 
					+ "NacelleTopView_" + i + ".png";
			File outputFilePNG = new File(outputFilePathTopViewPNG);
			if(outputFilePNG.exists()) outputFilePNG.delete();
			
			try {
				ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void createNacelleSideView(Aircraft aircraft, String outputDirectoryPath) {

		//---------------------------------------------------------------------------------
		// LOOP OVER NACELLES:
		for (int i=0; i<aircraft.getNacelles().getNacellesList().size(); i++) {

			//--------------------------------------------------
			// get data vectors from nacelle discretization
			//--------------------------------------------------

			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
			aircraft.getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperZ = new ArrayList<>(); 
			aircraft.getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZUpper().stream().forEach(z -> nacelleCurveUpperZ.add(z));

			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerZ = new ArrayList<>(); 
			aircraft.getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZLower().stream().forEach(z -> nacelleCurveLowerZ.add(z));;

			List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
			List<Amount<Length>> dataOutlineXZCurveNacelleZ = new ArrayList<>();

			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(j));
			}

			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveLowerZ.get(nacelleCurveXPoints-j-1));
			}

			dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0));
			dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(0));

			XYSeries seriesNacelleCruvesSideView = new XYSeries("Nacelle " + i + " XY Curve - Side View", false);
			IntStream.range(0, dataOutlineXZCurveNacelleX.size())
			.forEach(j -> {
				seriesNacelleCruvesSideView.add(
						dataOutlineXZCurveNacelleZ.get(j).doubleValue(SI.METER),
						dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER)
						);
			});

			double xMaxSideView = 1.40*aircraft.getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double xMinSideView = -1.40*aircraft.getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double yMaxSideView = 1.20*aircraft.getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);
			double yMinSideView = -0.20*aircraft.getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);

			//-------------------------------------------------------------------------------------
			// DATASET CRATION
			List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
			seriesAndColorList.add(Tuple.of(seriesNacelleCruvesSideView, Color.decode("#FF7F50")));

			XYSeriesCollection dataset = new XYSeriesCollection();
			seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

			//-------------------------------------------------------------------------------------
			// CHART CRATION
			JFreeChart chart = ChartFactory.createXYAreaChart(
					"Nacelle data representation - Side View", 
					"z (m)", 
					"x (m)",
					(XYDataset) dataset,
					PlotOrientation.HORIZONTAL,
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
			domain.setRange(xMinSideView, xMaxSideView);
			domain.setInverted(Boolean.FALSE);
			domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
			domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
			NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
			range.setRange(yMinSideView, yMaxSideView);
			range.setInverted(Boolean.FALSE);
			range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
			range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

			XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
			xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
			xyAreaRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyAreaRenderer.setSeriesPaint(
						j,
						seriesAndColorList.get(j)._2()
						);
			}
			XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
			xyLineAndShapeRenderer.setDefaultShapesVisible(false);
			xyLineAndShapeRenderer.setDefaultLinesVisible(true);
			xyLineAndShapeRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyLineAndShapeRenderer.setSeriesPaint(j, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						j, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}

			plot.setRenderer(0, xyAreaRenderer);
			plot.setDataset(0, dataset);
			plot.setRenderer(1, xyLineAndShapeRenderer);
			plot.setDataset(1, dataset);

			//-------------------------------------------------------------------------------------
			// EXPORT TO SVG
			String outputFilePathTopViewSVG = outputDirectoryPath 
					+ File.separator 
					+ "NacelleSideView_" + i + ".svg";
			File outputFileSVG = new File(outputFilePathTopViewSVG);
			if(outputFileSVG.exists()) outputFileSVG.delete();
			SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
			Rectangle r = new Rectangle(WIDTH, HEIGHT);
			chart.draw(g2, r);
			try {
				SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
			} catch (IOException e) {
				e.printStackTrace();
			}

			//-------------------------------------------------------------------------------------
			// EXPORT TO PNG
			String outputFilePathTopViewPNG = outputDirectoryPath 
					+ File.separator 
					+ "NacelleSideView_" + i + ".png";
			File outputFilePNG = new File(outputFilePathTopViewPNG);
			if(outputFilePNG.exists()) outputFilePNG.delete();
			
			try {
				ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void createNacelleFrontView(Aircraft aircraft, String outputDirectoryPath) {

		//---------------------------------------------------------------------------------
		// LOOP OVER NACELLES:
		for (int i=0; i<aircraft.getNacelles().getNacellesList().size(); i++) {

			//--------------------------------------------------
			// get data vectors from nacelle discretization
			//--------------------------------------------------
			double[] angleArray = MyArrayUtils.linspace(0.0, 2*Math.PI, 100);
			double[] yCoordinate = new double[angleArray.length];
			double[] zCoordinate = new double[angleArray.length];

			double radius = aircraft.getNacelles()
					.getNacellesList().get(i)
					.getDiameterMax()
					.divide(2)
					.doubleValue(SI.METER);
			double y0 = 0.0;
			double z0 = 0.0;

			for(int j=0; j<angleArray.length; j++) {
				yCoordinate[j] = radius*Math.cos(angleArray[j]);
				zCoordinate[j] = radius*Math.sin(angleArray[j]);
			}

			XYSeries seriesNacelleCruvesFrontView = new XYSeries("Nacelle " + i + " - Front View", false);
			IntStream.range(0, yCoordinate.length)
			.forEach(j -> {
				seriesNacelleCruvesFrontView.add(
						yCoordinate[j] + y0,
						zCoordinate[j] + z0
						);
			});

			double yMaxFrontView = 1.20*aircraft.getNacelles().getNacellesList().get(i).getDiameterMax().doubleValue(SI.METRE);
			double yMinFrontView = -1.20*aircraft.getNacelles().getNacellesList().get(i).getDiameterMax().doubleValue(SI.METRE);
			double xMaxFrontView = yMaxFrontView;
			double xMinFrontView = yMinFrontView;

			//-------------------------------------------------------------------------------------
			// DATASET CRATION
			List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
			seriesAndColorList.add(Tuple.of(seriesNacelleCruvesFrontView, Color.decode("#FF7F50")));

			XYSeriesCollection dataset = new XYSeriesCollection();
			seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

			//-------------------------------------------------------------------------------------
			// CHART CRATION
			JFreeChart chart = ChartFactory.createXYAreaChart(
					"Nacelle data representation - Front View", 
					"x (m)", 
					"z (m)",
					(XYDataset) dataset,
					PlotOrientation.HORIZONTAL,
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
			domain.setRange(xMinFrontView, xMaxFrontView);
			domain.setInverted(Boolean.TRUE);
			domain.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
			domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
			NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
			range.setRange(yMinFrontView, yMaxFrontView);
			range.setInverted(Boolean.FALSE);
			range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
			range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

			XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
			xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
			xyAreaRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyAreaRenderer.setSeriesPaint(
						j,
						seriesAndColorList.get(j)._2()
						);
			}
			XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
			xyLineAndShapeRenderer.setDefaultShapesVisible(false);
			xyLineAndShapeRenderer.setDefaultLinesVisible(true);
			xyLineAndShapeRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyLineAndShapeRenderer.setSeriesPaint(j, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						j, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}

			plot.setRenderer(0, xyAreaRenderer);
			plot.setDataset(0, dataset);
			plot.setRenderer(1, xyLineAndShapeRenderer);
			plot.setDataset(1, dataset);

			//-------------------------------------------------------------------------------------
			// EXPORT TO SVG
			String outputFilePathTopViewSVG = outputDirectoryPath 
					+ File.separator 
					+ "NacelleFrontView_" + i + ".svg";
			File outputFileSVG = new File(outputFilePathTopViewSVG);
			if(outputFileSVG.exists()) outputFileSVG.delete();
			SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
			Rectangle r = new Rectangle(WIDTH, HEIGHT);
			chart.draw(g2, r);
			try {
				SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
			} catch (IOException e) {
				e.printStackTrace();
			}

			//-------------------------------------------------------------------------------------
			// EXPORT TO PNG
			String outputFilePathTopViewPNG = outputDirectoryPath 
					+ File.separator 
					+ "NacelleFrontView_" + i + ".png";
			File outputFilePNG = new File(outputFilePathTopViewPNG);
			if(outputFilePNG.exists()) outputFilePNG.delete();
			
			try {
				ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void createAircraftSideViewWithCGPositions(
			Aircraft aircraft, 
			String outputDirectoryPath,
			Map<ComponentEnum, CenterOfGravity> cgMap,
			Map<ComponentEnum, Amount<Mass>> massMap
			) {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		
		if (aircraft.getFuselage() != null) {
			// upper curve, sideview
			List<Amount<Length>> vX1Upper = new ArrayList<>(); 
			aircraft.getFuselage().getOutlineXZUpperCurveAmountX().stream().forEach(x -> vX1Upper.add(x));
			int nX1Upper = vX1Upper.size();
			List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
			aircraft.getFuselage().getOutlineXZUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

			// lower curve, sideview
			List<Amount<Length>> vX2Lower = new ArrayList<>(); 
			aircraft.getFuselage().getOutlineXZLowerCurveAmountX().stream().forEach(x -> vX2Lower.add(x));
			int nX2Lower = vX2Lower.size();
			List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
			aircraft.getFuselage().getOutlineXZLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));

			IntStream.range(0, nX1Upper)
			.forEach(i -> {
				seriesFuselageCurve.add(vX1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
			});
			IntStream.range(0, nX2Lower)
			.forEach(i -> {
				seriesFuselageCurve.add(vX2Lower.get(vX2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
			});
		}
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		XYSeries seriesWingRootAirfoil = new XYSeries("Wing Root - Side View", false);
		XYSeries seriesWingTipAirfoil = new XYSeries("Wing Tip - Side View", false);

		if (aircraft.getWing() != null) {
			double[] wingRootXCoordinates = aircraft.getWing().getAirfoilList().get(0).getXCoords();
			double[] wingRootZCoordinates = aircraft.getWing().getAirfoilList().get(0).getZCoords();
			double[] wingTipXCoordinates = aircraft.getWing().getAirfoilList()
					.get(aircraft.getWing().getAirfoilList().size()-1).getXCoords();
			double[] wingTipZCoordinates = aircraft.getWing().getAirfoilList()
					.get(aircraft.getWing().getAirfoilList().size()-1).getZCoords();
			int nPointsWing = aircraft.getWing().getDiscretizedXle().size();

			IntStream.range(0, wingRootXCoordinates.length)
			.forEach(i -> {
				seriesWingRootAirfoil.add(
						(wingRootXCoordinates[i]*aircraft.getWing().getPanels().get(0).getChordRoot().getEstimatedValue()) 
						+ aircraft.getWing().getXApexConstructionAxes().getEstimatedValue(),
						(wingRootZCoordinates[i]*aircraft.getWing().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getWing().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, wingTipXCoordinates.length)
			.forEach(i -> {
				seriesWingTipAirfoil.add(
						(wingTipXCoordinates[i]*aircraft.getWing().getPanels()
								.get(aircraft.getWing().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ aircraft.getWing().getXApexConstructionAxes().getEstimatedValue()
						+ aircraft.getWing().getDiscretizedXle().get(nPointsWing-1).getEstimatedValue(),
						(wingTipZCoordinates[i]*aircraft.getWing().getPanels()
								.get(aircraft.getWing().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ aircraft.getWing().getZApexConstructionAxes().getEstimatedValue()
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		XYSeries seriesHTailRootAirfoil = new XYSeries("HTail Root - Side View", false);
		XYSeries seriesHTailTipAirfoil = new XYSeries("HTail Tip - Side View", false);

		if (aircraft.getHTail() != null) {
			double[] hTailRootXCoordinates = aircraft.getHTail().getAirfoilList().get(0).getXCoords();
			double[] hTailRootZCoordinates = aircraft.getHTail().getAirfoilList().get(0).getZCoords();
			double[] hTailTipXCoordinates = aircraft.getHTail().getAirfoilList()
					.get(aircraft.getHTail().getAirfoilList().size()-1).getXCoords();
			double[] hTailTipZCoordinates = aircraft.getHTail().getAirfoilList()
					.get(aircraft.getHTail().getAirfoilList().size()-1).getZCoords();
			int nPointsHTail = aircraft.getHTail().getDiscretizedXle().size();

			IntStream.range(0, hTailRootXCoordinates.length)
			.forEach(i -> {
				seriesHTailRootAirfoil.add(
						(hTailRootXCoordinates[i]*aircraft.getHTail().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getHTail().getXApexConstructionAxes().getEstimatedValue(),
						(hTailRootZCoordinates[i]*aircraft.getHTail().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getHTail().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, hTailTipXCoordinates.length)
			.forEach(i -> {
				seriesHTailTipAirfoil.add(
						(hTailTipXCoordinates[i]*aircraft.getHTail().getPanels()
								.get(aircraft.getHTail().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ aircraft.getHTail().getXApexConstructionAxes().getEstimatedValue()
						+ aircraft.getHTail().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(),
						(hTailTipZCoordinates[i]*aircraft.getHTail().getPanels()
								.get(aircraft.getHTail().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ aircraft.getHTail().getZApexConstructionAxes().getEstimatedValue()
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from canard discretization
		//--------------------------------------------------
		XYSeries seriesCanardRootAirfoil = new XYSeries("Canard Root - Side View", false);
		XYSeries seriesCanardTipAirfoil = new XYSeries("Canard Tip - Side View", false);

		if (aircraft.getCanard() != null) {
			double[] canardRootXCoordinates = aircraft.getCanard().getAirfoilList().get(0).getXCoords();
			double[] canardRootZCoordinates = aircraft.getCanard().getAirfoilList().get(0).getZCoords();
			double[] canardTipXCoordinates = aircraft.getCanard().getAirfoilList()
					.get(aircraft.getCanard().getAirfoilList().size()-1).getXCoords();
			double[] canardTipZCoordinates = aircraft.getCanard().getAirfoilList()
					.get(aircraft.getCanard().getAirfoilList().size()-1).getZCoords();
			int nPointsHTail = aircraft.getCanard().getDiscretizedXle().size();

			IntStream.range(0, canardRootXCoordinates.length)
			.forEach(i -> {
				seriesCanardRootAirfoil.add(
						(canardRootXCoordinates[i]*aircraft.getCanard().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getCanard().getXApexConstructionAxes().getEstimatedValue(),
						(canardRootZCoordinates[i]*aircraft.getCanard().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getCanard().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, canardTipXCoordinates.length)
			.forEach(i -> {
				seriesCanardTipAirfoil.add(
						(canardTipXCoordinates[i]*aircraft.getCanard().getPanels()
								.get(aircraft.getCanard().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ aircraft.getCanard().getXApexConstructionAxes().getEstimatedValue()
						+ aircraft.getCanard().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(),
						(canardTipZCoordinates[i]*aircraft.getCanard().getPanels()
								.get(aircraft.getCanard().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ aircraft.getCanard().getZApexConstructionAxes().getEstimatedValue()
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		XYSeries seriesVTailSideView = new XYSeries("VTail - Side View", false);

		if (aircraft.getVTail() != null) {
			Double[][] dataTopViewVTail = aircraft.getVTail().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);

			IntStream.range(0, dataTopViewVTail.length)
			.forEach(i -> {
				seriesVTailSideView.add(
						dataTopViewVTail[i][0] + aircraft.getVTail().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewVTail[i][1] + aircraft.getVTail().getZApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}

		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesSideViewList = new ArrayList<>();

		if (aircraft.getNacelles() != null) {
			for(int i=0; i<aircraft.getNacelles().getNacellesList().size(); i++) {

				// upper curve, sideview
				List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
				aircraft.getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
				int nacelleCurveXPoints = nacelleCurveX.size();
				List<Amount<Length>> nacelleCurveUpperZ = new ArrayList<>(); 
				aircraft.getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZUpper().stream().forEach(z -> nacelleCurveUpperZ.add(z));

				// lower curve, sideview
				List<Amount<Length>> nacelleCurveLowerZ = new ArrayList<>(); 
				aircraft.getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZLower().stream().forEach(z -> nacelleCurveLowerZ.add(z));;

				List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
				List<Amount<Length>> dataOutlineXZCurveNacelleZ = new ArrayList<>();

				for(int j=0; j<nacelleCurveXPoints; j++) {
					dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j)
							.plus(aircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
					dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(j)
							.plus(aircraft.getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
				}

				for(int j=0; j<nacelleCurveXPoints; j++) {
					dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1) 
							.plus(aircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
					dataOutlineXZCurveNacelleZ.add(nacelleCurveLowerZ.get(nacelleCurveXPoints-j-1)
							.plus(aircraft.getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
				}

				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0)
						.plus(aircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(0)
						.plus(aircraft.getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));


				XYSeries seriesNacelleCruvesSideView = new XYSeries("Nacelle " + i + " XY Curve - Side View", false);
				IntStream.range(0, dataOutlineXZCurveNacelleX.size())
				.forEach(j -> {
					seriesNacelleCruvesSideView.add(
							dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER),
							dataOutlineXZCurveNacelleZ.get(j).doubleValue(SI.METER)
							);
				});

				seriesNacelleCruvesSideViewList.add(seriesNacelleCruvesSideView);

			}
		}
		//-------------------------------------------------------------------------------
		// get data vectors from power plant propellers (only for PISTON and TURBOPROP)
		//-------------------------------------------------------------------------------
		List<XYSeries> seriesPropellerSideViewList = new ArrayList<>();
		
		if(aircraft.getPowerPlant() != null) {
			for(int i=0; i<aircraft.getPowerPlant().getEngineList().size(); i++) {

				if (aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
						|| aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {

					XYSeries seriesPropellerCruvesSideView = new XYSeries("Propeller " + i, false);
					seriesPropellerCruvesSideView.add(
							aircraft.getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							1.0015*aircraft.getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
							+ aircraft.getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);
					seriesPropellerCruvesSideView.add(
							aircraft.getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							1.0015*aircraft.getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
							- aircraft.getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);

					seriesPropellerSideViewList.add(seriesPropellerCruvesSideView);
				}
			}
		}		
		//--------------------------------------------------
		// get data vectors from landing gears 
		//--------------------------------------------------
		XYSeries seriesMainLandingGearSideView = new XYSeries("Main Landing Gears - Side View", false);
		XYSeries seriesNoseLandingGearSideView = new XYSeries("Front Landing Gears - Side View", false);
		
		if (aircraft.getLandingGears() != null) {
			Amount<Length> radiusNose = aircraft.getLandingGears().getFrontalWheelsHeight().divide(2);
			Amount<Length> radiusMain = aircraft.getLandingGears().getRearWheelsHeight().divide(2);
			Double[] frontWheelCenterPosition = new Double[] {
					aircraft.getLandingGears().getXApexConstructionAxesNoseGear().doubleValue(SI.METER),
					aircraft.getLandingGears().getZApexConstructionAxesNoseGear().doubleValue(SI.METER)
					- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					- radiusNose.doubleValue(SI.METER)
			};
			Double[] mainWheelCenterPosition = new Double[] {
					aircraft.getLandingGears().getXApexConstructionAxesMainGear().doubleValue(SI.METER),
					aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
					- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					- radiusMain.doubleValue(SI.METER)
			};
			Double[] thetaArray = MyArrayUtils.linspaceDouble(0, 2*Math.PI, 360);

			IntStream.range(0, thetaArray.length)
			.forEach(i -> {
				seriesNoseLandingGearSideView.add(
						radiusNose.doubleValue(SI.METER)*Math.cos(thetaArray[i]) + frontWheelCenterPosition[0],
						radiusNose.doubleValue(SI.METER)*Math.sin(thetaArray[i]) + frontWheelCenterPosition[1]
						);
				seriesMainLandingGearSideView.add(
						radiusMain.doubleValue(SI.METER)*Math.cos(thetaArray[i]) + mainWheelCenterPosition[0],
						radiusMain.doubleValue(SI.METER)*Math.sin(thetaArray[i]) + mainWheelCenterPosition[1]
						);
			});
		}
		
		//-------------------------------------------------------------------------------------
		// XCG and ZCG SERIES CREATION
		List<XYSeries> cgPositionsSeriesList = new ArrayList<>();
		for(int iSeries=0; iSeries<cgMap.size(); iSeries++) {
			XYSeries currentSeries = new XYSeries(cgMap.keySet().toArray()[iSeries].toString(), false);
			CenterOfGravity currentCG = (CenterOfGravity) cgMap.values().toArray()[iSeries];
			Amount<Length> currentXCG = currentCG.getXBRF(); 
			Amount<Length> currentZCG = currentCG.getZBRF();
			Amount<Mass> currentMass = (Amount<Mass>) massMap.values().toArray()[iSeries];
			if(currentMass.doubleValue(SI.KILOGRAM) != 0.0) {
				currentSeries.add(
						currentXCG.doubleValue(SI.METER),
						currentZCG.doubleValue(SI.METER)
						);
				cgPositionsSeriesList.add(currentSeries);
			}
		}
		
		double xMaxSideView = 1.20*aircraft.getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double xMinSideView = -0.20*aircraft.getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMaxSideView = 1.40*aircraft.getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMinSideView = -1.40*aircraft.getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		
		int WIDTH = 1000;
		int HEIGHT = 1000;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		List<Tuple2<XYSeries, Color>> seriesAndColorListCGPositions = new ArrayList<>();
		if (aircraft.getPowerPlant() != null)
			seriesPropellerSideViewList.stream().forEach(
					prop -> seriesAndColorList.add(Tuple.of(prop, Color.BLACK))
					);
		if (aircraft.getNacelles() != null)
			seriesNacelleCruvesSideViewList.stream().forEach(
					nac -> seriesAndColorList.add(Tuple.of(nac, Color.decode("#FF7F50")))
					);
		if (aircraft.getWing() != null) {
			seriesAndColorList.add(Tuple.of(seriesWingRootAirfoil, Color.decode("#87CEFA")));
			seriesAndColorList.add(Tuple.of(seriesWingTipAirfoil, Color.decode("#87CEFA")));
		}
		if (aircraft.getHTail() != null) {
			seriesAndColorList.add(Tuple.of(seriesHTailRootAirfoil, Color.decode("#00008B")));
			seriesAndColorList.add(Tuple.of(seriesHTailTipAirfoil, Color.decode("#00008B")));
		}
		if (aircraft.getCanard() != null) {
			seriesAndColorList.add(Tuple.of(seriesCanardRootAirfoil, Color.decode("#228B22")));
			seriesAndColorList.add(Tuple.of(seriesCanardTipAirfoil, Color.decode("#228B22")));
		}
		if (aircraft.getLandingGears() != null) {
			seriesAndColorList.add(Tuple.of(seriesNoseLandingGearSideView, Color.decode("#404040")));
			seriesAndColorList.add(Tuple.of(seriesMainLandingGearSideView, Color.decode("#404040")));
		}
		if (aircraft.getFuselage() != null)
			seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));
		if (aircraft.getVTail() != null)
			seriesAndColorList.add(Tuple.of(seriesVTailSideView, Color.decode("#FFD700")));
		
		Color[] colorArray = new Color[17];
		for(int i=0; i<colorArray.length; i++) {
			int color = (int) (Math.random() * 256);
			colorArray[i] = new Color(color, color, color); 
		}
		for(int i=0; i<cgPositionsSeriesList.size(); i++)
			seriesAndColorListCGPositions.add(Tuple.of(cgPositionsSeriesList.get(i), colorArray[i]));
		
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		seriesAndColorListCGPositions.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Aircraft data representation - Side View", 
				"x (m)", 
				"z (m)",
				(XYDataset) dataset,
				PlotOrientation.VERTICAL,
                true, // legend
                true,  // tooltips
                false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);
		chart.removeLegend();
		
		// TODO: ADD CUSTOMIZED LEGEND... 
//		LegendTitle legend = new LegendTitle();
//		legend.setPosition(RectangleEdge.BOTTOM);
//		legend.setBackgroundPaint((Paint)ChartColor.WHITE);
//		chart.addLegend(legend);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(xMinSideView, xMaxSideView);
		domain.setLabelFont(new Font("Dialog", Font.PLAIN, LABEL_SIZE));
		domain.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(yMinSideView, yMaxSideView);
		range.setLabelFont(new Font("Sans-serif", Font.PLAIN, LABEL_SIZE));
		range.setTickLabelFont(new Font("Sans-serif", Font.PLAIN, TICK_LABEL_SIZE));

		XYLineAndShapeRenderer xyLineAndShapePropRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapePropRenderer.setDefaultShapesVisible(false);
		xyLineAndShapePropRenderer.setDefaultLinesVisible(true);
		xyLineAndShapePropRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if (i < aircraft.getPowerPlant().getEngineNumber()) {
				xyLineAndShapePropRenderer.setSeriesVisible(i, true);
				xyLineAndShapePropRenderer.setSeriesPaint(i, Color.BLACK);
				xyLineAndShapePropRenderer.setSeriesStroke(
						i, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}
			else
				xyLineAndShapePropRenderer.setSeriesVisible(i, false);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			for(int j=0; j<seriesAndColorListCGPositions.size(); j++)
				if(dataset.getSeries().get(i).equals(seriesAndColorListCGPositions.get(j)._1()))
					xyLineAndShapeRenderer.setSeriesVisible(i, false);
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
		}

		XYLineAndShapeRenderer xyLineAndShapeRendererCGPositions = new XYLineAndShapeRenderer();
		xyLineAndShapeRendererCGPositions.setDefaultShapesVisible(true);
		xyLineAndShapeRendererCGPositions.setDefaultLinesVisible(false);
		xyLineAndShapeRendererCGPositions.setUseOutlinePaint(true);
		xyLineAndShapeRendererCGPositions.setDefaultOutlinePaint(Color.BLACK);
		xyLineAndShapeRendererCGPositions.setDefaultOutlineStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		xyLineAndShapeRendererCGPositions.setDefaultEntityRadius(25);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyLineAndShapeRendererCGPositions.setSeriesShape(i, ShapeUtils.createDiamond(5.0f));
			for(int j=0; j<seriesAndColorList.size(); j++)
				if(dataset.getSeries().get(i).equals(seriesAndColorList.get(j)._1()))
					xyLineAndShapeRendererCGPositions.setSeriesVisible(i, false);
		}
		
		if (aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
				|| aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {
			plot.setRenderer(2, xyLineAndShapeRendererCGPositions);
			plot.setDataset(2, dataset);
			plot.setRenderer(1, xyLineAndShapeRenderer);
			plot.setDataset(1, dataset);
			plot.setRenderer(0, xyLineAndShapePropRenderer);
			plot.setDataset(0, dataset);
		}
		else {
			plot.setRenderer(1, xyLineAndShapeRendererCGPositions);
			plot.setDataset(1, dataset);
			plot.setRenderer(0, xyLineAndShapeRenderer);
			plot.setDataset(0, dataset);
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopViewSVG = outputDirectoryPath 
				+ File.separator 
				+ "AircraftSideViewWithCGPositions.svg";
		File outputFileSVG = new File(outputFilePathTopViewSVG);
		if(outputFileSVG.exists()) outputFileSVG.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFileSVG, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO PNG
		String outputFilePathTopViewPNG = outputDirectoryPath 
				+ File.separator 
				+ "AircraftSideViewWithCGPositions.png";
		File outputFilePNG = new File(outputFilePathTopViewPNG);
		if(outputFilePNG.exists()) outputFilePNG.delete();
		
		try {
			ChartUtilities.saveChartAsPNG(outputFilePNG, chart, WIDTH, HEIGHT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
}
