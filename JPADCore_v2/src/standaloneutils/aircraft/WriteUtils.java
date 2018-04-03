package standaloneutils.aircraft;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
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

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineTypeEnum;
import javaslang.Tuple;
import javaslang.Tuple2;
import standaloneutils.MyArrayUtils;

public final class WriteUtils {
	
	private WriteUtils() {}

	public static boolean writeAirfoilToSVG(String pathToSVG, String title, Airfoil airfoil) {
		return writeAirfoilsToSVG(pathToSVG, title, new Airfoil[] {airfoil});
	}

	public static boolean writeAirfoilToSVG(String pathToSVG, String title,
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

	public static boolean writeAircraftTopViewToSVG(String pathToSVG, String title,  
			Aircraft aircraft) {
		int WIDTH = 650;
		int HEIGHT = 650;
		return writeAircraftTopViewToSVG(pathToSVG, title, WIDTH, HEIGHT, aircraft);
	}

	public static boolean writeAircraftTopViewToSVG(String pathToSVG, String title, 
			int WIDTH, int HEIGHT, 
			Aircraft aircraft) {
		
		boolean status = false;
		
		if (aircraft == null) return status;
		
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
			Double[][] dataTopViewIsolated = aircraft.getWing().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.WING);

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
			Double[][] dataTopViewIsolatedHTail = aircraft.getHTail().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.HORIZONTAL_TAIL);

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

			double[] vTailRootXCoordinates = aircraft.getVTail().getLiftingSurfaceCreator().getAirfoilList().get(0).getXCoords();
			double[] vTailRootYCoordinates = aircraft.getVTail().getLiftingSurfaceCreator().getAirfoilList().get(0).getZCoords();
			double[] vTailTipXCoordinates = aircraft.getVTail().getLiftingSurfaceCreator().getAirfoilList().get(aircraft.getVTail().getLiftingSurfaceCreator().getAirfoilList().size()-1).getXCoords();
			double[] vTailTipYCoordinates = aircraft.getVTail().getLiftingSurfaceCreator().getAirfoilList().get(aircraft.getVTail().getLiftingSurfaceCreator().getAirfoilList().size()-1).getZCoords();
			int nPointsVTail = aircraft.getVTail().getLiftingSurfaceCreator().getDiscretizedXle().size();

			IntStream.range(0, vTailRootXCoordinates.length)
			.forEach(i -> {
				seriesVTailRootAirfoilTopView.add(
						(vTailRootXCoordinates[i]*aircraft.getVTail().getLiftingSurfaceCreator().getPanels()
								.get(aircraft.getVTail().getLiftingSurfaceCreator().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ aircraft.getVTail().getXApexConstructionAxes().getEstimatedValue(),
						(vTailRootYCoordinates[i]*aircraft.getVTail().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().getEstimatedValue())
						);
			});

			IntStream.range(0, vTailTipXCoordinates.length)
			.forEach(i -> {
				seriesVTailTipAirfoilTopView.add(
						(vTailTipXCoordinates[i]*aircraft.getVTail().getLiftingSurfaceCreator().getPanels()
								.get(aircraft.getVTail().getLiftingSurfaceCreator().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ aircraft.getVTail().getXApexConstructionAxes().getEstimatedValue()
						+ aircraft.getVTail().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsVTail-1).getEstimatedValue(),
						(vTailTipYCoordinates[i]*aircraft.getVTail().getLiftingSurfaceCreator().getPanels()
								.get(aircraft.getVTail().getLiftingSurfaceCreator().getPanels().size()-1).getChordTip().getEstimatedValue())
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from canard discretization
		//--------------------------------------------------
		XYSeries seriesCanardTopView = new XYSeries("Canard - Top View", false);
		
		if (aircraft.getCanard() != null) {
			Double[][] dataTopViewIsolatedCanard = aircraft.getCanard().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.CANARD);

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
					+ aircraft.getVTail().getLiftingSurfaceCreator().getSpan().doubleValue(SI.METER), 
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
		// CHART CREATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				title, 
				"x (m)", 
				"y (m)",
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
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);

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

	public static boolean writeAircraftSideViewToSVG(String pathToSVG, String title,  
			Aircraft aircraft) {
		int WIDTH = 650;
		int HEIGHT = 650;
		return writeAircraftSideViewToSVG(pathToSVG, title, WIDTH, HEIGHT, aircraft);
	}

	public static boolean writeAircraftSideViewToSVG(String pathToSVG, String title, 
			int WIDTH, int HEIGHT, 
			Aircraft aircraft) {
		
		boolean status = false;
		
		if (aircraft == null) return status;
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Side View", false);
		
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
			double[] wingRootXCoordinates = aircraft.getWing().getLiftingSurfaceCreator().getAirfoilList().get(0).getXCoords();
			double[] wingRootZCoordinates = aircraft.getWing().getLiftingSurfaceCreator().getAirfoilList().get(0).getZCoords();
			double[] wingTipXCoordinates = aircraft.getWing().getLiftingSurfaceCreator().getAirfoilList()
					.get(aircraft.getWing().getLiftingSurfaceCreator().getAirfoilList().size()-1).getXCoords();
			double[] wingTipZCoordinates = aircraft.getWing().getLiftingSurfaceCreator().getAirfoilList()
					.get(aircraft.getWing().getLiftingSurfaceCreator().getAirfoilList().size()-1).getZCoords();
			int nPointsWing = aircraft.getWing().getLiftingSurfaceCreator().getDiscretizedXle().size();

			IntStream.range(0, wingRootXCoordinates.length)
			.forEach(i -> {
				seriesWingRootAirfoil.add(
						(wingRootXCoordinates[i]*aircraft.getWing().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().getEstimatedValue()) 
						+ aircraft.getWing().getXApexConstructionAxes().getEstimatedValue(),
						(wingRootZCoordinates[i]*aircraft.getWing().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getWing().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, wingTipXCoordinates.length)
			.forEach(i -> {
				seriesWingTipAirfoil.add(
						(wingTipXCoordinates[i]*aircraft.getWing().getLiftingSurfaceCreator().getPanels()
								.get(aircraft.getWing().getLiftingSurfaceCreator().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ aircraft.getWing().getXApexConstructionAxes().getEstimatedValue()
						+ aircraft.getWing().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsWing-1).getEstimatedValue(),
						(wingTipZCoordinates[i]*aircraft.getWing().getLiftingSurfaceCreator().getPanels()
								.get(aircraft.getWing().getLiftingSurfaceCreator().getPanels().size()-1).getChordTip().getEstimatedValue())
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
			double[] hTailRootXCoordinates = aircraft.getHTail().getLiftingSurfaceCreator().getAirfoilList().get(0).getXCoords();
			double[] hTailRootZCoordinates = aircraft.getHTail().getLiftingSurfaceCreator().getAirfoilList().get(0).getZCoords();
			double[] hTailTipXCoordinates = aircraft.getHTail().getLiftingSurfaceCreator().getAirfoilList()
					.get(aircraft.getHTail().getLiftingSurfaceCreator().getAirfoilList().size()-1).getXCoords();
			double[] hTailTipZCoordinates = aircraft.getHTail().getLiftingSurfaceCreator().getAirfoilList()
					.get(aircraft.getHTail().getLiftingSurfaceCreator().getAirfoilList().size()-1).getZCoords();
			int nPointsHTail = aircraft.getHTail().getLiftingSurfaceCreator().getDiscretizedXle().size();

			IntStream.range(0, hTailRootXCoordinates.length)
			.forEach(i -> {
				seriesHTailRootAirfoil.add(
						(hTailRootXCoordinates[i]*aircraft.getHTail().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getHTail().getXApexConstructionAxes().getEstimatedValue(),
						(hTailRootZCoordinates[i]*aircraft.getHTail().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getHTail().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, hTailTipXCoordinates.length)
			.forEach(i -> {
				seriesHTailTipAirfoil.add(
						(hTailTipXCoordinates[i]*aircraft.getHTail().getLiftingSurfaceCreator().getPanels()
								.get(aircraft.getHTail().getLiftingSurfaceCreator().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ aircraft.getHTail().getXApexConstructionAxes().getEstimatedValue()
						+ aircraft.getHTail().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(),
						(hTailTipZCoordinates[i]*aircraft.getHTail().getLiftingSurfaceCreator().getPanels()
								.get(aircraft.getHTail().getLiftingSurfaceCreator().getPanels().size()-1).getChordTip().getEstimatedValue())
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
			double[] canardRootXCoordinates = aircraft.getCanard().getLiftingSurfaceCreator().getAirfoilList().get(0).getXCoords();
			double[] canardRootZCoordinates = aircraft.getCanard().getLiftingSurfaceCreator().getAirfoilList().get(0).getZCoords();
			double[] canardTipXCoordinates = aircraft.getCanard().getLiftingSurfaceCreator().getAirfoilList()
					.get(aircraft.getCanard().getLiftingSurfaceCreator().getAirfoilList().size()-1).getXCoords();
			double[] canardTipZCoordinates = aircraft.getCanard().getLiftingSurfaceCreator().getAirfoilList()
					.get(aircraft.getCanard().getLiftingSurfaceCreator().getAirfoilList().size()-1).getZCoords();
			int nPointsHTail = aircraft.getCanard().getLiftingSurfaceCreator().getDiscretizedXle().size();

			IntStream.range(0, canardRootXCoordinates.length)
			.forEach(i -> {
				seriesCanardRootAirfoil.add(
						(canardRootXCoordinates[i]*aircraft.getCanard().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getCanard().getXApexConstructionAxes().getEstimatedValue(),
						(canardRootZCoordinates[i]*aircraft.getCanard().getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ aircraft.getCanard().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, canardTipXCoordinates.length)
			.forEach(i -> {
				seriesCanardTipAirfoil.add(
						(canardTipXCoordinates[i]*aircraft.getCanard().getLiftingSurfaceCreator().getPanels()
								.get(aircraft.getCanard().getLiftingSurfaceCreator().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ aircraft.getCanard().getXApexConstructionAxes().getEstimatedValue()
						+ aircraft.getCanard().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(),
						(canardTipZCoordinates[i]*aircraft.getCanard().getLiftingSurfaceCreator().getPanels()
								.get(aircraft.getCanard().getLiftingSurfaceCreator().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ aircraft.getCanard().getZApexConstructionAxes().getEstimatedValue()
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		XYSeries seriesVTailSideView = new XYSeries("VTail - Side View", false);

		if (aircraft.getVTail() != null) {
			Double[][] dataTopViewVTail = aircraft.getVTail().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);

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
		XYSeries seriesLandingGearSideView = new XYSeries("Landing Gears - Side View", false);
		
		if (aircraft.getLandingGears() != null) {
			Amount<Length> radius = aircraft.getLandingGears().getRearWheelsHeight().divide(2);
			Double[] wheelCenterPosition = new Double[] {
					aircraft.getLandingGears().getXApexConstructionAxesMainGear().doubleValue(SI.METER),
					aircraft.getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
					- aircraft.getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					- radius.doubleValue(SI.METER)
			};
			Double[] thetaArray = MyArrayUtils.linspaceDouble(0, 2*Math.PI, 360);

			IntStream.range(0, thetaArray.length)
			.forEach(i -> {
				seriesLandingGearSideView.add(
						radius.doubleValue(SI.METER)*Math.cos(thetaArray[i]) + wheelCenterPosition[0],
						radius.doubleValue(SI.METER)*Math.sin(thetaArray[i]) + wheelCenterPosition[1]
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
		if (aircraft.getLandingGears() != null) 
			seriesAndColorList.add(Tuple.of(seriesLandingGearSideView, Color.decode("#404040")));
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
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(yMinSideView, yMaxSideView);

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
