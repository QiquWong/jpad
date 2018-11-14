package jpadcommander.inputmanager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import javax.measure.unit.SI;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;
import org.jscience.physics.amount.Amount;

import aircraft.components.cabinconfiguration.ISeatBlock;
import aircraft.components.cabinconfiguration.SeatsBlock;
import calculators.geometry.FusNacGeometryCalc;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.RelativePositionEnum;
import graphics.ChartCanvas;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javaslang.Tuple;
import javaslang.Tuple2;
import jpadcommander.Main;
import standaloneutils.GeometryCalc;
import standaloneutils.MyArrayUtils;

public class InputManagerControllerGraphicUtilities {

	//---------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	public InputManagerController theController;
	
	//---------------------------------------------------------------------------------
	// BUILDER
	public InputManagerControllerGraphicUtilities(InputManagerController controller) {
		
		this.theController = controller;
		
	}
	
	//---------------------------------------------------------------------------------
	// METHODS

	public void createAircraftTopView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			// left curve, upperview
			List<Amount<Length>> vX1Left = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXYSideLCurveAmountX().stream().forEach(x -> vX1Left.add(x));
			int nX1Left = vX1Left.size();
			List<Amount<Length>> vY1Left = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXYSideLCurveAmountY().stream().forEach(y -> vY1Left.add(y));

			// right curve, upperview
			List<Amount<Length>> vX2Right = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveAmountX().stream().forEach(x -> vX2Right.add(x));;
			int nX2Right = vX2Right.size();
			List<Amount<Length>> vY2Right = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveAmountY().stream().forEach(y -> vY2Right.add(y));;

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
		
		if (Main.getTheAircraft().getWing() != null) {
			Double[][] dataTopViewIsolated = Main.getTheAircraft().getWing().getDiscretizedTopViewAsArray(ComponentEnum.WING);

			IntStream.range(0, dataTopViewIsolated.length)
			.forEach(i -> {
				seriesWingTopView.add(
						dataTopViewIsolated[i][1] + Main.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewIsolated[i][0] + Main.getTheAircraft().getWing().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
			IntStream.range(0, dataTopViewIsolated.length)
			.forEach(i -> {
				seriesWingTopView.add(
						dataTopViewIsolated[dataTopViewIsolated.length-1-i][1] + Main.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER),
						-dataTopViewIsolated[dataTopViewIsolated.length-1-i][0] + Main.getTheAircraft().getWing().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		XYSeries seriesHTailTopView = new XYSeries("HTail - Top View", false);
		
		if (Main.getTheAircraft().getHTail() != null) {
			Double[][] dataTopViewIsolatedHTail = Main.getTheAircraft().getHTail().getDiscretizedTopViewAsArray(ComponentEnum.HORIZONTAL_TAIL);

			IntStream.range(0, dataTopViewIsolatedHTail.length)
			.forEach(i -> {
				seriesHTailTopView.add(
						dataTopViewIsolatedHTail[i][1] + Main.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewIsolatedHTail[i][0] + Main.getTheAircraft().getHTail().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
			IntStream.range(0, dataTopViewIsolatedHTail.length)
			.forEach(i -> {
				seriesHTailTopView.add(
						dataTopViewIsolatedHTail[dataTopViewIsolatedHTail.length-1-i][1] + Main.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER),
						- dataTopViewIsolatedHTail[dataTopViewIsolatedHTail.length-1-i][0] + Main.getTheAircraft().getHTail().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		XYSeries seriesVTailRootAirfoilTopView = new XYSeries("VTail Root - Top View", false);
		XYSeries seriesVTailTipAirfoilTopView = new XYSeries("VTail Tip - Top View", false);
		
		if (Main.getTheAircraft().getVTail() != null) {

			double[] vTailRootXCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(0).getXCoords();
			double[] vTailRootYCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(0).getZCoords();
			double[] vTailTipXCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(Main.getTheAircraft().getVTail().getAirfoilList().size()-1).getXCoords();
			double[] vTailTipYCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(Main.getTheAircraft().getVTail().getAirfoilList().size()-1).getZCoords();
			int nPointsVTail = Main.getTheAircraft().getVTail().getDiscretizedXle().size();

			IntStream.range(0, vTailRootXCoordinates.length)
			.forEach(i -> {
				seriesVTailRootAirfoilTopView.add(
						(vTailRootXCoordinates[i]*Main.getTheAircraft().getVTail().getPanels()
								.get(Main.getTheAircraft().getVTail().getPanels().size()-1).getChordRoot().doubleValue(SI.METER))
						+ Main.getTheAircraft().getVTail().getXApexConstructionAxes().doubleValue(SI.METER),
						(vTailRootYCoordinates[i]*Main.getTheAircraft().getVTail().getPanels().get(0).getChordRoot().getEstimatedValue())
						);
			});

			IntStream.range(0, vTailTipXCoordinates.length)
			.forEach(i -> {
				seriesVTailTipAirfoilTopView.add(
						(vTailTipXCoordinates[i]*Main.getTheAircraft().getVTail().getPanels()
								.get(Main.getTheAircraft().getVTail().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ Main.getTheAircraft().getVTail().getXApexConstructionAxes().getEstimatedValue()
						+ Main.getTheAircraft().getVTail().getDiscretizedXle().get(nPointsVTail-1).getEstimatedValue(),
						(vTailTipYCoordinates[i]*Main.getTheAircraft().getVTail().getPanels()
								.get(Main.getTheAircraft().getVTail().getPanels().size()-1).getChordTip().getEstimatedValue())
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from canard discretization
		//--------------------------------------------------
		XYSeries seriesCanardTopView = new XYSeries("Canard - Top View", false);
		
		if (Main.getTheAircraft().getCanard() != null) {
			Double[][] dataTopViewIsolatedCanard = Main.getTheAircraft().getCanard().getDiscretizedTopViewAsArray(ComponentEnum.CANARD);

			IntStream.range(0, dataTopViewIsolatedCanard.length)
			.forEach(i -> {
				seriesCanardTopView.add(
						dataTopViewIsolatedCanard[i][1] + Main.getTheAircraft().getCanard().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewIsolatedCanard[i][0] + Main.getTheAircraft().getCanard().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
			IntStream.range(0, dataTopViewIsolatedCanard.length)
			.forEach(i -> {
				seriesCanardTopView.add(
						dataTopViewIsolatedCanard[dataTopViewIsolatedCanard.length-1-i][1] + Main.getTheAircraft().getCanard().getXApexConstructionAxes().doubleValue(SI.METER),
						- dataTopViewIsolatedCanard[dataTopViewIsolatedCanard.length-1-i][0] + Main.getTheAircraft().getCanard().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesTopViewList = new ArrayList<>();

		if(Main.getTheAircraft().getNacelles() != null) {
			for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

				// upper curve, sideview
				List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
				int nacelleCurveXPoints = nacelleCurveX.size();
				List<Amount<Length>> nacelleCurveUpperY = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYRight().stream().forEach(y -> nacelleCurveUpperY.add(y));

				// lower curve, sideview
				List<Amount<Length>> nacelleCurveLowerY = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYLeft().stream().forEach(y -> nacelleCurveLowerY.add(y));;

				List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
				List<Amount<Length>> dataOutlineXZCurveNacelleY = new ArrayList<>();

				for(int j=0; j<nacelleCurveXPoints; j++) {
					dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j)
							.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
					dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(j)
							.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));
				}

				for(int j=0; j<nacelleCurveXPoints; j++) {
					dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1) 
							.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
					dataOutlineXZCurveNacelleY.add(nacelleCurveLowerY.get(nacelleCurveXPoints-j-1)
							.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));
				}

				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(0)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));

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
		
		if(Main.getTheAircraft().getPowerPlant() != null) {
			for(int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				if (Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
						|| Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {

					XYSeries seriesPropellerCruvesTopView = new XYSeries("Propeller " + i, false);
					seriesPropellerCruvesTopView.add(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getYApexConstructionAxes().doubleValue(SI.METER)
							+ Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);
					seriesPropellerCruvesTopView.add(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getYApexConstructionAxes().doubleValue(SI.METER)
							- Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);

					seriesPropellerTopViewList.add(seriesPropellerCruvesTopView);
				}
			}
		}
		
		double xMaxTopView = 1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = 1.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMinTopView = -0.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
			
		int WIDTH = 650;
		int HEIGHT = 650;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		Map<Double, Tuple2<XYSeries, Color>> componentZList = new HashMap<>();
		if (Main.getTheAircraft().getFuselage() != null) 
			componentZList.put(
					Main.getTheAircraft().getFuselage().getZApexConstructionAxes().doubleValue(SI.METER)
					+ Main.getTheAircraft().getFuselage().getSectionCylinderHeight().divide(2).doubleValue(SI.METER),
					Tuple.of(seriesFuselageCurve, Color.WHITE) 
					);
		if (Main.getTheAircraft().getWing() != null) 
			componentZList.put(
					Main.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesWingTopView, Color.decode("#87CEFA"))
					); 
		if (Main.getTheAircraft().getHTail() != null) 
			componentZList.put(
					Main.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesHTailTopView, Color.decode("#00008B"))
					);
		if (Main.getTheAircraft().getCanard() != null) 
			componentZList.put(
					Main.getTheAircraft().getCanard().getZApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesCanardTopView, Color.decode("#228B22"))
					);
		if (Main.getTheAircraft().getVTail() != null)
			if (Main.getTheAircraft().getFuselage() != null)
				componentZList.put(
						Main.getTheAircraft().getFuselage().getZApexConstructionAxes().doubleValue(SI.METER)
						+ Main.getTheAircraft().getFuselage().getSectionCylinderHeight().divide(2).doubleValue(SI.METER)
						+ 0.0001,
						Tuple.of(seriesVTailRootAirfoilTopView, Color.decode("#FFD700"))
						);
		if (Main.getTheAircraft().getVTail() != null)
			componentZList.put(
					Main.getTheAircraft().getVTail().getZApexConstructionAxes().doubleValue(SI.METER) 
					+ Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METER), 
					Tuple.of(seriesVTailTipAirfoilTopView, Color.decode("#FFD700"))
					);
		if (Main.getTheAircraft().getNacelles() != null) 
			seriesNacelleCruvesTopViewList.stream().forEach(
					nac -> componentZList.put(
							Main.getTheAircraft().getNacelles().getNacellesList().get(
									seriesNacelleCruvesTopViewList.indexOf(nac)
									).getZApexConstructionAxes().doubleValue(SI.METER)
							+ 0.005
							+ seriesNacelleCruvesTopViewList.indexOf(nac)*0.005, 
							Tuple.of(nac, Color.decode("#FF7F50"))
							)
					);
		if (Main.getTheAircraft().getPowerPlant() != null) 
			seriesPropellerTopViewList.stream().forEach(
					prop -> componentZList.put(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(
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
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "AircraftTopView.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  
		
		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  
		
		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.getAircraftTopViewPane().getChildren().clear();
		theController.getAircraftTopViewPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public void createAircraftSideView() {
	
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		
		if (Main.getTheAircraft().getFuselage() != null) {
			// upper curve, sideview
			List<Amount<Length>> vX1Upper = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXZUpperCurveAmountX().stream().forEach(x -> vX1Upper.add(x));
			int nX1Upper = vX1Upper.size();
			List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXZUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

			// lower curve, sideview
			List<Amount<Length>> vX2Lower = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXZLowerCurveAmountX().stream().forEach(x -> vX2Lower.add(x));
			int nX2Lower = vX2Lower.size();
			List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXZLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));

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

		if (Main.getTheAircraft().getWing() != null) {
			double[] wingRootXCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(0).getXCoords();
			double[] wingRootZCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(0).getZCoords();
			double[] wingTipXCoordinates = Main.getTheAircraft().getWing().getAirfoilList()
					.get(Main.getTheAircraft().getWing().getAirfoilList().size()-1).getXCoords();
			double[] wingTipZCoordinates = Main.getTheAircraft().getWing().getAirfoilList()
					.get(Main.getTheAircraft().getWing().getAirfoilList().size()-1).getZCoords();
			int nPointsWing = Main.getTheAircraft().getWing().getDiscretizedXle().size();

			IntStream.range(0, wingRootXCoordinates.length)
			.forEach(i -> {
				seriesWingRootAirfoil.add(
						(wingRootXCoordinates[i]*Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot().getEstimatedValue()) 
						+ Main.getTheAircraft().getWing().getXApexConstructionAxes().getEstimatedValue(),
						(wingRootZCoordinates[i]*Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getWing().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, wingTipXCoordinates.length)
			.forEach(i -> {
				seriesWingTipAirfoil.add(
						(wingTipXCoordinates[i]*Main.getTheAircraft().getWing().getPanels()
								.get(Main.getTheAircraft().getWing().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ Main.getTheAircraft().getWing().getXApexConstructionAxes().getEstimatedValue()
						+ Main.getTheAircraft().getWing().getDiscretizedXle().get(nPointsWing-1).getEstimatedValue(),
						(wingTipZCoordinates[i]*Main.getTheAircraft().getWing().getPanels()
								.get(Main.getTheAircraft().getWing().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ Main.getTheAircraft().getWing().getZApexConstructionAxes().getEstimatedValue()
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		XYSeries seriesHTailRootAirfoil = new XYSeries("HTail Root - Side View", false);
		XYSeries seriesHTailTipAirfoil = new XYSeries("HTail Tip - Side View", false);

		if (Main.getTheAircraft().getHTail() != null) {
			double[] hTailRootXCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(0).getXCoords();
			double[] hTailRootZCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(0).getZCoords();
			double[] hTailTipXCoordinates = Main.getTheAircraft().getHTail().getAirfoilList()
					.get(Main.getTheAircraft().getHTail().getAirfoilList().size()-1).getXCoords();
			double[] hTailTipZCoordinates = Main.getTheAircraft().getHTail().getAirfoilList()
					.get(Main.getTheAircraft().getHTail().getAirfoilList().size()-1).getZCoords();
			int nPointsHTail = Main.getTheAircraft().getHTail().getDiscretizedXle().size();

			IntStream.range(0, hTailRootXCoordinates.length)
			.forEach(i -> {
				seriesHTailRootAirfoil.add(
						(hTailRootXCoordinates[i]*Main.getTheAircraft().getHTail().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getHTail().getXApexConstructionAxes().getEstimatedValue(),
						(hTailRootZCoordinates[i]*Main.getTheAircraft().getHTail().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getHTail().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, hTailTipXCoordinates.length)
			.forEach(i -> {
				seriesHTailTipAirfoil.add(
						(hTailTipXCoordinates[i]*Main.getTheAircraft().getHTail().getPanels()
								.get(Main.getTheAircraft().getHTail().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ Main.getTheAircraft().getHTail().getXApexConstructionAxes().getEstimatedValue()
						+ Main.getTheAircraft().getHTail().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(),
						(hTailTipZCoordinates[i]*Main.getTheAircraft().getHTail().getPanels()
								.get(Main.getTheAircraft().getHTail().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ Main.getTheAircraft().getHTail().getZApexConstructionAxes().getEstimatedValue()
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from canard discretization
		//--------------------------------------------------
		XYSeries seriesCanardRootAirfoil = new XYSeries("Canard Root - Side View", false);
		XYSeries seriesCanardTipAirfoil = new XYSeries("Canard Tip - Side View", false);

		if (Main.getTheAircraft().getCanard() != null) {
			double[] canardRootXCoordinates = Main.getTheAircraft().getCanard().getAirfoilList().get(0).getXCoords();
			double[] canardRootZCoordinates = Main.getTheAircraft().getCanard().getAirfoilList().get(0).getZCoords();
			double[] canardTipXCoordinates = Main.getTheAircraft().getCanard().getAirfoilList()
					.get(Main.getTheAircraft().getCanard().getAirfoilList().size()-1).getXCoords();
			double[] canardTipZCoordinates = Main.getTheAircraft().getCanard().getAirfoilList()
					.get(Main.getTheAircraft().getCanard().getAirfoilList().size()-1).getZCoords();
			int nPointsHTail = Main.getTheAircraft().getCanard().getDiscretizedXle().size();

			IntStream.range(0, canardRootXCoordinates.length)
			.forEach(i -> {
				seriesCanardRootAirfoil.add(
						(canardRootXCoordinates[i]*Main.getTheAircraft().getCanard().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getCanard().getXApexConstructionAxes().getEstimatedValue(),
						(canardRootZCoordinates[i]*Main.getTheAircraft().getCanard().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getCanard().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, canardTipXCoordinates.length)
			.forEach(i -> {
				seriesCanardTipAirfoil.add(
						(canardTipXCoordinates[i]*Main.getTheAircraft().getCanard().getPanels()
								.get(Main.getTheAircraft().getCanard().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ Main.getTheAircraft().getCanard().getXApexConstructionAxes().getEstimatedValue()
						+ Main.getTheAircraft().getCanard().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(),
						(canardTipZCoordinates[i]*Main.getTheAircraft().getCanard().getPanels()
								.get(Main.getTheAircraft().getCanard().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ Main.getTheAircraft().getCanard().getZApexConstructionAxes().getEstimatedValue()
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		XYSeries seriesVTailSideView = new XYSeries("VTail - Side View", false);

		if (Main.getTheAircraft().getVTail() != null) {
			Double[][] dataTopViewVTail = Main.getTheAircraft().getVTail().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);

			IntStream.range(0, dataTopViewVTail.length)
			.forEach(i -> {
				seriesVTailSideView.add(
						dataTopViewVTail[i][0] + Main.getTheAircraft().getVTail().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewVTail[i][1] + Main.getTheAircraft().getVTail().getZApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}

		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesSideViewList = new ArrayList<>();

		if (Main.getTheAircraft().getNacelles() != null) {
			for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

				// upper curve, sideview
				List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
				int nacelleCurveXPoints = nacelleCurveX.size();
				List<Amount<Length>> nacelleCurveUpperZ = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZUpper().stream().forEach(z -> nacelleCurveUpperZ.add(z));

				// lower curve, sideview
				List<Amount<Length>> nacelleCurveLowerZ = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZLower().stream().forEach(z -> nacelleCurveLowerZ.add(z));;

				List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
				List<Amount<Length>> dataOutlineXZCurveNacelleZ = new ArrayList<>();

				for(int j=0; j<nacelleCurveXPoints; j++) {
					dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j)
							.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
					dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(j)
							.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
				}

				for(int j=0; j<nacelleCurveXPoints; j++) {
					dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1) 
							.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
					dataOutlineXZCurveNacelleZ.add(nacelleCurveLowerZ.get(nacelleCurveXPoints-j-1)
							.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
				}

				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(0)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));


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
		
		if(Main.getTheAircraft().getPowerPlant() != null) {
			for(int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				if (Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
						|| Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {

					XYSeries seriesPropellerCruvesSideView = new XYSeries("Propeller " + i, false);
					seriesPropellerCruvesSideView.add(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							1.0015*Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
							+ Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);
					seriesPropellerCruvesSideView.add(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							1.0015*Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
							- Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
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
		
		if (Main.getTheAircraft().getLandingGears() != null) {
			Amount<Length> radiusNose = Main.getTheAircraft().getLandingGears().getFrontalWheelsHeight().divide(2);
			Amount<Length> radiusMain = Main.getTheAircraft().getLandingGears().getRearWheelsHeight().divide(2);
			Double[] frontWheelCenterPosition = new Double[] {
					Main.getTheAircraft().getLandingGears().getXApexConstructionAxesNoseGear().doubleValue(SI.METER),
					Main.getTheAircraft().getLandingGears().getZApexConstructionAxesNoseGear().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					- radiusNose.doubleValue(SI.METER)
			};
			Double[] mainWheelCenterPosition = new Double[] {
					Main.getTheAircraft().getLandingGears().getXApexConstructionAxesMainGear().doubleValue(SI.METER),
					Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
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
		
		double xMaxSideView = 1.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double xMinSideView = -0.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMaxSideView = 1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMinSideView = -1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		if (Main.getTheAircraft().getPowerPlant() != null)
			seriesPropellerSideViewList.stream().forEach(
					prop -> seriesAndColorList.add(Tuple.of(prop, Color.BLACK))
					);
		if (Main.getTheAircraft().getNacelles() != null)
			seriesNacelleCruvesSideViewList.stream().forEach(
					nac -> seriesAndColorList.add(Tuple.of(nac, Color.decode("#FF7F50")))
					);
		if (Main.getTheAircraft().getWing() != null) {
			seriesAndColorList.add(Tuple.of(seriesWingRootAirfoil, Color.decode("#87CEFA")));
			seriesAndColorList.add(Tuple.of(seriesWingTipAirfoil, Color.decode("#87CEFA")));
		}
		if (Main.getTheAircraft().getHTail() != null) {
			seriesAndColorList.add(Tuple.of(seriesHTailRootAirfoil, Color.decode("#00008B")));
			seriesAndColorList.add(Tuple.of(seriesHTailTipAirfoil, Color.decode("#00008B")));
		}
		if (Main.getTheAircraft().getCanard() != null) {
			seriesAndColorList.add(Tuple.of(seriesCanardRootAirfoil, Color.decode("#228B22")));
			seriesAndColorList.add(Tuple.of(seriesCanardTipAirfoil, Color.decode("#228B22")));
		}
		if (Main.getTheAircraft().getLandingGears() != null) {
			seriesAndColorList.add(Tuple.of(seriesNoseLandingGearSideView, Color.decode("#404040")));
			seriesAndColorList.add(Tuple.of(seriesMainLandingGearSideView, Color.decode("#404040")));
		}
		if (Main.getTheAircraft().getFuselage() != null)
			seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));
		if (Main.getTheAircraft().getVTail() != null)
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
			if (i < Main.getTheAircraft().getPowerPlant().getEngineNumber()) {
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

		if (Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
				|| Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {
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
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "AircraftSideView.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  
		
		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  
		
		//create browser
		Scene sceneSideView = new Scene(stackPane, WIDTH+10, HEIGHT+10, javafx.scene.paint.Color.web("#666970"));
		theController.getAircraftSideViewPane().getChildren().clear();
		theController.getAircraftSideViewPane().getChildren().add(sceneSideView.getRoot());		
		
	}
	
	public void createAircraftFrontView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Front View", false);
		
		if (Main.getTheAircraft().getFuselage() != null) {
			// section upper curve
			List<Amount<Length>> vY1Upper = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getSectionUpperCurveAmountY().stream().forEach(y -> vY1Upper.add(y));
			int nY1Upper = vY1Upper.size();
			List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getSectionUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

			// section lower curve
			List<Amount<Length>> vY2Lower = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getSectionLowerCurveAmountY().stream().forEach(y -> vY2Lower.add(y));
			int nY2Lower = vY2Lower.size();
			List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getSectionLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));

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

		if (Main.getTheAircraft().getWing() != null) {
			List<Amount<Length>> wingBreakPointsYCoordinates = new ArrayList<>(); 
			Main.getTheAircraft().getWing().getYBreakPoints().stream().forEach(y -> wingBreakPointsYCoordinates.add(y));
			int nYPointsWingTemp = wingBreakPointsYCoordinates.size();
			for(int i=0; i<nYPointsWingTemp; i++)
				wingBreakPointsYCoordinates.add(Main.getTheAircraft().getWing().getYBreakPoints().get(nYPointsWingTemp-i-1));
			int nYPointsWing = wingBreakPointsYCoordinates.size();

			List<Amount<Length>> wingThicknessZCoordinates = new ArrayList<Amount<Length>>();
			for(int i=0; i<Main.getTheAircraft().getWing().getAirfoilList().size(); i++)
				wingThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getWing().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
								MyArrayUtils.getMax(Main.getTheAircraft().getWing().getAirfoilList().get(i).getZCoords()),
								SI.METER
								)
						);
			for(int i=0; i<nYPointsWingTemp; i++) {
				wingThicknessZCoordinates.add(
						Amount.valueOf(
								(Main.getTheAircraft().getWing().getChordsBreakPoints().get(nYPointsWingTemp-i-1).doubleValue(SI.METER)*
										MyArrayUtils.getMin(Main.getTheAircraft().getWing().getAirfoilList().get(nYPointsWingTemp-i-1).getZCoords())),
								SI.METER
								)
						);
			}

			List<Amount<Angle>> dihedralList = new ArrayList<>();
			for (int i = 0; i < Main.getTheAircraft().getWing().getDihedralsBreakPoints().size(); i++) {
				dihedralList.add(
						Main.getTheAircraft().getWing().getDihedralsBreakPoints().get(i)
						);
			}
			for (int i = 0; i < Main.getTheAircraft().getWing().getDihedralsBreakPoints().size(); i++) {
				dihedralList.add(
						Main.getTheAircraft().getWing().getDihedralsBreakPoints().get(
								Main.getTheAircraft().getWing().getDihedralsBreakPoints().size()-1-i)
						);
			}

			IntStream.range(0, nYPointsWing)
			.forEach(i -> {
				seriesWingFrontView.add(
						wingBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getWing().getYApexConstructionAxes()).doubleValue(SI.METRE),
						wingThicknessZCoordinates.get(i)
						.plus(Main.getTheAircraft().getWing().getZApexConstructionAxes())
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
						-wingBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getWing().getYApexConstructionAxes()).doubleValue(SI.METRE),
						wingThicknessZCoordinates.get(i)
						.plus(Main.getTheAircraft().getWing().getZApexConstructionAxes())
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

		if (Main.getTheAircraft().getHTail() != null) {
			List<Amount<Length>> hTailBreakPointsYCoordinates = new ArrayList<>(); 
			Main.getTheAircraft().getHTail().getYBreakPoints().stream().forEach(y -> hTailBreakPointsYCoordinates.add(y));
			int nYPointsHTailTemp = hTailBreakPointsYCoordinates.size();
			for(int i=0; i<nYPointsHTailTemp; i++)
				hTailBreakPointsYCoordinates.add(Main.getTheAircraft().getHTail().getYBreakPoints().get(nYPointsHTailTemp-i-1));
			int nYPointsHTail = hTailBreakPointsYCoordinates.size();

			List<Amount<Length>> hTailThicknessZCoordinates = new ArrayList<Amount<Length>>();
			for(int i=0; i<Main.getTheAircraft().getHTail().getAirfoilList().size(); i++)
				hTailThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getHTail().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
								MyArrayUtils.getMax(Main.getTheAircraft().getHTail().getAirfoilList().get(i).getZCoords()),
								SI.METER
								)
						);
			for(int i=0; i<nYPointsHTailTemp; i++)
				hTailThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getHTail().getChordsBreakPoints().get(nYPointsHTailTemp-i-1).doubleValue(SI.METER)*
								MyArrayUtils.getMin(Main.getTheAircraft().getHTail().getAirfoilList().get(nYPointsHTailTemp-i-1).getZCoords()),
								SI.METER
								)
						);

			List<Amount<Angle>> dihedralListHTail = new ArrayList<>();
			for (int i = 0; i < Main.getTheAircraft().getHTail().getDihedralsBreakPoints().size(); i++) {
				dihedralListHTail.add(
						Main.getTheAircraft().getHTail().getDihedralsBreakPoints().get(i)
						);
			}
			for (int i = 0; i < Main.getTheAircraft().getHTail().getDihedralsBreakPoints().size(); i++) {
				dihedralListHTail.add(
						Main.getTheAircraft().getHTail().getDihedralsBreakPoints().get(
								Main.getTheAircraft().getHTail().getDihedralsBreakPoints().size()-1-i)
						);
			}

			IntStream.range(0, nYPointsHTail)
			.forEach(i -> {
				seriesHTailFrontView.add(
						hTailBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getHTail().getYApexConstructionAxes()).doubleValue(SI.METRE),
						hTailThicknessZCoordinates.get(i)
						.plus(Main.getTheAircraft().getHTail().getZApexConstructionAxes())
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
						-hTailBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getHTail().getYApexConstructionAxes()).doubleValue(SI.METRE),
						hTailThicknessZCoordinates.get(i)
						.plus(Main.getTheAircraft().getHTail().getZApexConstructionAxes())
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

		if (Main.getTheAircraft().getCanard() != null) {
			List<Amount<Length>> canardBreakPointsYCoordinates = new ArrayList<>(); 
			Main.getTheAircraft().getCanard().getYBreakPoints().stream().forEach(y -> canardBreakPointsYCoordinates.add(y));
			int nYPointsCanardTemp = canardBreakPointsYCoordinates.size();
			for(int i=0; i<nYPointsCanardTemp; i++)
				canardBreakPointsYCoordinates.add(Main.getTheAircraft().getCanard().getYBreakPoints().get(nYPointsCanardTemp-i-1));
			int nYPointsCanard = canardBreakPointsYCoordinates.size();

			List<Amount<Length>> canardThicknessZCoordinates = new ArrayList<Amount<Length>>();
			for(int i=0; i<Main.getTheAircraft().getCanard().getAirfoilList().size(); i++)
				canardThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getCanard().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
								MyArrayUtils.getMax(Main.getTheAircraft().getCanard().getAirfoilList().get(i).getZCoords()),
								SI.METER
								)
						);
			for(int i=0; i<nYPointsCanardTemp; i++)
				canardThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getCanard().getChordsBreakPoints().get(nYPointsCanardTemp-i-1).doubleValue(SI.METER)*
								MyArrayUtils.getMin(Main.getTheAircraft().getCanard().getAirfoilList().get(nYPointsCanardTemp-i-1).getZCoords()),
								SI.METER
								)
						);

			List<Amount<Angle>> dihedralListCanard = new ArrayList<>();
			for (int i = 0; i < Main.getTheAircraft().getCanard().getDihedralsBreakPoints().size(); i++) {
				dihedralListCanard.add(
						Main.getTheAircraft().getCanard().getDihedralsBreakPoints().get(i)
						);
			}
			for (int i = 0; i < Main.getTheAircraft().getCanard().getDihedralsBreakPoints().size(); i++) {
				dihedralListCanard.add(
						Main.getTheAircraft().getCanard().getDihedralsBreakPoints().get(
								Main.getTheAircraft().getCanard().getDihedralsBreakPoints().size()-1-i)
						);
			}

			IntStream.range(0, nYPointsCanard)
			.forEach(i -> {
				seriesCanardFrontView.add(
						canardBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getCanard().getYApexConstructionAxes()).doubleValue(SI.METRE),
						canardThicknessZCoordinates.get(i)
						.plus(Main.getTheAircraft().getCanard().getZApexConstructionAxes())
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
						-canardBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getCanard().getYApexConstructionAxes()).doubleValue(SI.METRE),
						canardThicknessZCoordinates.get(i)
						.plus(Main.getTheAircraft().getCanard().getZApexConstructionAxes())
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

		if (Main.getTheAircraft().getVTail() != null) {
			List<Amount<Length>> vTailBreakPointsYCoordinates = new ArrayList<>(); 
			Main.getTheAircraft().getVTail().getYBreakPoints().stream().forEach(y -> vTailBreakPointsYCoordinates.add(y));
			int nYPointsVTailTemp = vTailBreakPointsYCoordinates.size();
			for(int i=0; i<nYPointsVTailTemp; i++)
				vTailBreakPointsYCoordinates.add(Main.getTheAircraft().getVTail().getYBreakPoints().get(nYPointsVTailTemp-i-1));
			int nYPointsVTail = vTailBreakPointsYCoordinates.size();

			List<Amount<Length>> vTailThicknessZCoordinates = new ArrayList<Amount<Length>>();
			for(int i=0; i<Main.getTheAircraft().getVTail().getAirfoilList().size(); i++)
				vTailThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getVTail().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
								MyArrayUtils.getMax(Main.getTheAircraft().getVTail().getAirfoilList().get(i).getZCoords()),
								SI.METER
								)
						);
			for(int i=0; i<nYPointsVTailTemp; i++)
				vTailThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getVTail().getChordsBreakPoints().get(nYPointsVTailTemp-i-1).doubleValue(SI.METER)*
								MyArrayUtils.getMin(Main.getTheAircraft().getVTail().getAirfoilList().get(nYPointsVTailTemp-i-1).getZCoords()),
								SI.METER
								)
						);

			IntStream.range(0, nYPointsVTail)
			.forEach(i -> {
				seriesVTailFrontView.add(
						vTailThicknessZCoordinates.get(i).plus(Main.getTheAircraft().getVTail().getYApexConstructionAxes()).doubleValue(SI.METRE),
						vTailBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getVTail().getZApexConstructionAxes()).doubleValue(SI.METRE)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from nacelles discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesFrontViewList = new ArrayList<>();

		if (Main.getTheAircraft().getNacelles() != null) {
			for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

				double[] angleArray = MyArrayUtils.linspace(0.0, 2*Math.PI, 20);
				double[] yCoordinate = new double[angleArray.length];
				double[] zCoordinate = new double[angleArray.length];

				double radius = Main.getTheAircraft().getNacelles()
						.getNacellesList().get(i)
						.getDiameterMax()
						.divide(2)
						.doubleValue(SI.METER);
				double y0 = Main.getTheAircraft().getNacelles()
						.getNacellesList().get(i)
						.getYApexConstructionAxes()
						.doubleValue(SI.METER);

				double z0 = Main.getTheAircraft().getNacelles()
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
		
		if(Main.getTheAircraft().getPowerPlant() != null) {
			for(int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				if (Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
						|| Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {
					
					Amount<Length> radius = Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2);
					Double[] centerPosition = new Double[] {
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getYApexConstructionAxes().doubleValue(SI.METER),
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
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

		if (Main.getTheAircraft().getLandingGears() != null) {
			for(int i=0; i<Main.getTheAircraft().getLandingGears().getNumberOfRearWheels()/2; i++) {

				XYSeries seriesLeftMainLandingGearCruvesFrontView = new XYSeries("Left Main Landing Gear " + i + " - Front View", false);
				seriesLeftMainLandingGearCruvesFrontView.add(
						Main.getTheAircraft().getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesLeftMainLandingGearCruvesFrontView.add(
						Main.getTheAircraft().getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesLeftMainLandingGearCruvesFrontView.add(
						Main.getTheAircraft().getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
						);
				seriesLeftMainLandingGearCruvesFrontView.add(
						Main.getTheAircraft().getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
						);
				seriesLeftMainLandingGearCruvesFrontView.add(
						Main.getTheAircraft().getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);

				XYSeries seriesRightMainLandingGearCruvesFrontView = new XYSeries("Right Main Landing Gear " + i + " - Front View", false);
				seriesRightMainLandingGearCruvesFrontView.add(
						- (Main.getTheAircraft().getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesRightMainLandingGearCruvesFrontView.add(
						- (Main.getTheAircraft().getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
								+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesRightMainLandingGearCruvesFrontView.add(
						- (Main.getTheAircraft().getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
								+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
						);
				seriesRightMainLandingGearCruvesFrontView.add(
						- (Main.getTheAircraft().getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
						);
				seriesRightMainLandingGearCruvesFrontView.add(
						- (Main.getTheAircraft().getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);

				serieMainLandingGearsCruvesFrontViewList.add(seriesLeftMainLandingGearCruvesFrontView);
				serieMainLandingGearsCruvesFrontViewList.add(seriesRightMainLandingGearCruvesFrontView);
			}
			
			for(int i=0; i<Main.getTheAircraft().getLandingGears().getNumberOfFrontalWheels(); i++) {

				XYSeries seriesLeftNoseLandingGearCruvesFrontView = new XYSeries("Left Nose Landing Gear " + i + " - Front View", false);
				seriesLeftNoseLandingGearCruvesFrontView.add(
						-Main.getTheAircraft().getLandingGears()
						.getFrontalWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesLeftNoseLandingGearCruvesFrontView.add(
						-Main.getTheAircraft().getLandingGears()
						.getFrontalWheelsWidth().doubleValue(SI.METER)
						+ Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesLeftNoseLandingGearCruvesFrontView.add(
						-Main.getTheAircraft().getLandingGears()
						.getFrontalWheelsWidth().doubleValue(SI.METER)
						+ Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getFrontalWheelsHeight().doubleValue(SI.METER)
						);
				seriesLeftNoseLandingGearCruvesFrontView.add(
						-Main.getTheAircraft().getLandingGears()
						.getFrontalWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getFrontalWheelsHeight().doubleValue(SI.METER)
						);
				seriesLeftNoseLandingGearCruvesFrontView.add(
						-Main.getTheAircraft().getLandingGears()
						.getFrontalWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);

				XYSeries seriesRightNoseLandingGearCruvesFrontView = new XYSeries("Right Nose Landing Gear " + i + " - Front View", false);
				seriesRightNoseLandingGearCruvesFrontView.add(
						- (i*1.1*Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesRightNoseLandingGearCruvesFrontView.add(
						- (i*1.1*Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesRightNoseLandingGearCruvesFrontView.add(
						- (i*1.1*Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getFrontalWheelsHeight().doubleValue(SI.METER)
						);
				seriesRightNoseLandingGearCruvesFrontView.add(
						- (i*1.1*Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getFrontalWheelsHeight().doubleValue(SI.METER)
						);
				seriesRightNoseLandingGearCruvesFrontView.add(
						- (i*1.1*Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);

				serieNoseLandingGearsCruvesFrontViewList.add(seriesLeftNoseLandingGearCruvesFrontView);
				serieNoseLandingGearsCruvesFrontViewList.add(seriesRightNoseLandingGearCruvesFrontView);
			}
		}
		
		double yMaxFrontView = 1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double yMinFrontView = -1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
		double zMaxFrontView = yMaxFrontView; 
		double zMinFrontView = yMinFrontView;
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		Map<Double, Tuple2<XYSeries, Color>> componentXList = new HashMap<>();
		if (Main.getTheAircraft().getFuselage() != null) 
			componentXList.put(
					Main.getTheAircraft().getFuselage().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesFuselageCurve, Color.WHITE) 
					);
		if (Main.getTheAircraft().getWing() != null) 
			componentXList.put(
					Main.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesWingFrontView, Color.decode("#87CEFA"))
					); 
		if (Main.getTheAircraft().getHTail() != null) 
			componentXList.put(
					Main.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesHTailFrontView, Color.decode("#00008B"))
					);
		if (Main.getTheAircraft().getCanard() != null) 
			componentXList.put(
					Main.getTheAircraft().getCanard().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesCanardFrontView, Color.decode("#228B22"))
					);
		if (Main.getTheAircraft().getVTail() != null)
			componentXList.put(
					Main.getTheAircraft().getVTail().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesVTailFrontView, Color.decode("#FFD700"))
					);
		if (Main.getTheAircraft().getNacelles() != null) 
			seriesNacelleCruvesFrontViewList.stream().forEach(
					nac -> componentXList.put(
							Main.getTheAircraft().getNacelles().getNacellesList().get(
									seriesNacelleCruvesFrontViewList.indexOf(nac)
									).getXApexConstructionAxes().doubleValue(SI.METER)
							+ 0.005
							+ seriesNacelleCruvesFrontViewList.indexOf(nac)*0.005, 
							Tuple.of(nac, Color.decode("#FF7F50"))
							)
					);
		if (Main.getTheAircraft().getPowerPlant() != null) 
			seriesPropellerFrontViewList.stream().forEach(
					prop -> componentXList.put(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(
									seriesPropellerFrontViewList.indexOf(prop)
									).getXApexConstructionAxes().doubleValue(SI.METER)
							+ 0.0015
							+ seriesPropellerFrontViewList.indexOf(prop)*0.001, 
							Tuple.of(prop, new Color(0f, 0f, 0f, 0.25f))
							)
					);
		if (Main.getTheAircraft().getLandingGears() != null) { 
			serieMainLandingGearsCruvesFrontViewList.stream().forEach(
					lg -> componentXList.put(
							Main.getTheAircraft().getLandingGears().getXApexConstructionAxesMainGear().doubleValue(SI.METER)
							+ serieMainLandingGearsCruvesFrontViewList.indexOf(lg)*0.001, 
							Tuple.of(lg, Color.decode("#404040"))
							)
					);
			serieNoseLandingGearsCruvesFrontViewList.stream().forEach(
					lg -> componentXList.put(
							Main.getTheAircraft().getLandingGears().getXApexConstructionAxesNoseGear().doubleValue(SI.METER)
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
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(zMinFrontView, zMaxFrontView);

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
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "AircraftFrontView.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  
		
		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  
		
		//create browser
		Scene sceneFrontView = new Scene(stackPane, WIDTH+10, HEIGHT+10, javafx.scene.paint.Color.web("#666970"));
		theController.getAircraftFrontViewPane().getChildren().clear();
		theController.getAircraftFrontViewPane().getChildren().add(sceneFrontView.getRoot());
		
	}
	
	public void createFuselageTopView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// left curve, upperview
		List<Amount<Length>> vX1Left = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXYSideLCurveAmountX().stream().forEach(x -> vX1Left.add(x));
		int nX1Left = vX1Left.size();
		List<Amount<Length>> vY1Left = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXYSideLCurveAmountY().stream().forEach(y -> vY1Left.add(y));

		// right curve, upperview
		List<Amount<Length>> vX2Right = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveAmountX().stream().forEach(x -> vX2Right.add(x));
		int nX2Right = vX2Right.size();
		List<Amount<Length>> vY2Right = new ArrayList<>();
		Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveAmountY().stream().forEach(y -> vY2Right.add(y));

		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		IntStream.range(0, nX1Left)
		.forEach(i -> {
			seriesFuselageCurve.add(vX1Left.get(i).doubleValue(SI.METRE), vY1Left.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nX2Right)
		.forEach(i -> {
			seriesFuselageCurve.add(vX2Right.get(vX2Right.size()-1-i).doubleValue(SI.METRE), vY2Right.get(vY2Right.size()-1-i).doubleValue(SI.METRE));
		});

		double xMaxTopView = 1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = 1.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMinTopView = -0.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);

		int WIDTH = 650;
		int HEIGHT = 650;

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
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);

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
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "FuselageTopView.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  

		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.getFuselageTopViewPane().getChildren().clear();
		theController.getFuselageTopViewPane().getChildren().add(sceneTopView.getRoot());
	}

	public void createFuselageSideView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// upper curve, sideview
		List<Amount<Length>> vX1Upper = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXZUpperCurveAmountX().stream().forEach(x -> vX1Upper.add(x));
		int nX1Upper = vX1Upper.size();
		List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXZUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

		// lower curve, sideview
		List<Amount<Length>> vX2Lower = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXZLowerCurveAmountX().stream().forEach(x -> vX2Lower.add(x));
		int nX2Lower = vX2Lower.size();
		List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXZLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));

		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		IntStream.range(0, nX1Upper)
		.forEach(i -> {
			seriesFuselageCurve.add(vX1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nX2Lower)
		.forEach(i -> {
			seriesFuselageCurve.add(vX2Lower.get(vX2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
		});
		
		double xMaxSideView = 1.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double xMinSideView = -0.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMaxSideView = 1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMinSideView = -1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
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
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "FuselageSideView.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  
		
		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  
		
		//create browser
		Scene sceneSideView = new Scene(stackPane, WIDTH+10, HEIGHT+10, javafx.scene.paint.Color.web("#666970"));
		theController.getFuselageSideViewPane().getChildren().clear();
		theController.getFuselageSideViewPane().getChildren().add(sceneSideView.getRoot());		
		
	}
	
	public void createFuselageFrontView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// section upper curve
		List<Amount<Length>> vY1Upper = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getSectionUpperCurveAmountY().stream().forEach(y -> vY1Upper.add(y));
		int nY1Upper = vY1Upper.size();
		List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getSectionUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

		// section lower curve
		List<Amount<Length>> vY2Lower = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getSectionLowerCurveAmountY().stream().forEach(y -> vY2Lower.add(y));
		int nY2Lower = vY2Lower.size();
		List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getSectionLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));
		
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Front View", false);
		IntStream.range(0, nY1Upper)
		.forEach(i -> {
			seriesFuselageCurve.add(vY1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nY2Lower)
		.forEach(i -> {
			seriesFuselageCurve.add(vY2Lower.get(vY2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
		});
		
		double yMaxFrontView = 1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double yMinFrontView = -1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
		double zMaxFrontView = yMaxFrontView; 
		double zMinFrontView = yMinFrontView;
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
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
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(zMinFrontView, zMaxFrontView);

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
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "FuselageFrontView.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  
		
		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  
		
		//create browser
		Scene sceneFrontView = new Scene(stackPane, WIDTH+10, HEIGHT+10, javafx.scene.paint.Color.web("#666970"));
		theController.getFuselageFrontViewPane().getChildren().clear();
		theController.getFuselageFrontViewPane().getChildren().add(sceneFrontView.getRoot());
	}
	
	public void createSeatMap() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// left curve, upperview
		List<Amount<Length>> vX1Left = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXYSideLCurveAmountX().stream().forEach(x -> vX1Left.add(x));
		int nX1Left = vX1Left.size();
		List<Amount<Length>> vY1Left = new ArrayList<>();
		Main.getTheAircraft().getFuselage().getOutlineXYSideLCurveAmountY().stream().forEach(y -> vY1Left.add(y));

		// right curve, upperview
		List<Amount<Length>> vX2Right = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveAmountX().stream().forEach(x -> vX2Right.add(x));
		int nX2Right = vX2Right.size();
		List<Amount<Length>> vY2Right = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveAmountY().stream().forEach(y -> vY2Right.add(y));

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
		int classNumber = Main.getTheAircraft().getCabinConfiguration().getClassesNumber()-1;
		
		for (int i = 0; i < Main.getTheAircraft().getCabinConfiguration().getClassesNumber(); i++) {

			breaksMap.put(
					Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksList().get(classNumber-i), 
					Main.getTheAircraft().getCabinConfiguration().getWidthList().get(i)
					);
			breaksMapList.add(breaksMap);
			
			SeatsBlock seatsBlock = new SeatsBlock(
					new ISeatBlock.Builder()
					.setPosition(RelativePositionEnum.RIGHT)
					.setXStart(Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().plus(length))
					.setPitch(Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i))
					.setWidth(Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i))
					.setDistanceFromWall(Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber-i))
					.putAllBreaksMap(breaksMapList.get(i))
					.setRowsNumber(Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsList().get(classNumber-i))
					.setColumnsNumber(Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsList().get(classNumber-i)[0])
					.setType(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(classNumber-i))					
					.build()
					);

			seatBlockList.add(seatsBlock);
			//........................................................................................................
			XYSeries seriesSeatBlock = new XYSeries("Seat Block " + i + " start", false);
			seriesSeatBlock.add(
					Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
					+ length.doubleValue(SI.METER),
					- FusNacGeometryCalc.getWidthAtX(
							Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
							+ length.doubleValue(SI.METER),
							Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveX(),
							Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveY()
							)/2
					+ Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
					);
			seriesSeatBlock.add(
					Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
					+ length.doubleValue(SI.METER),
					FusNacGeometryCalc.getWidthAtX(
							Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
							+ length.doubleValue(SI.METER),
							Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveX(),
							Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveY()
							)/2
					- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
					);
			//........................................................................................................
			Amount<Length> aisleWidth = Amount.valueOf(0.0, SI.METER);
			Amount<Length> currentYPosition = Amount.valueOf(0.0, SI.METER);
			Double breakLengthPitchFraction = 0.25;
			List<Integer> breakesPositionsIndexList = new ArrayList<>();
			for (int iBrake=0; iBrake<Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksList().get(classNumber-i); iBrake++) {
				Integer brekesInteval = Math.round(
						(Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsList().get(classNumber-i)
						+ Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksList().get(classNumber-i))
						/ (Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksList().get(classNumber-i) + 1)
						);
				breakesPositionsIndexList.add((iBrake+1)*brekesInteval);
			}
			
			for(int j=0; j<Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsList().get(classNumber-i).length; j++) {
				if(j>0) {
					aisleWidth = Amount.valueOf( 
							(Main.getTheAircraft().getFuselage().getSectionCylinderWidth().doubleValue(SI.METER) 
									- (MyArrayUtils.sumArrayElements(Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsList().get(classNumber-i))
											* Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER))
									- 2*Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER))
							/Main.getTheAircraft().getCabinConfiguration().getAislesNumber(),
							SI.METER
							);
					currentYPosition = Amount.valueOf(
							seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getYValue(),
							SI.METER
							);
				}
				for (int k = 0; k <Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsList().get(classNumber-i)[j]; k++) {
					
					int indexOfCurrentBrake = 10000;
					
					for (int r = 0;
							 r < Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsList().get(classNumber-i); 
							 r++) {
						
						XYSeries seriesSeats = new XYSeries("Column " + i + j + k + r, false);
						if(j>0) {
							int rowIndex = r;
							if(breakesPositionsIndexList.stream().anyMatch(x -> x == rowIndex)) {
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ breakLengthPitchFraction * Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER)
										+ Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										currentYPosition.doubleValue(SI.METER)
										- aisleWidth.doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
								indexOfCurrentBrake = r;
							}
							else if (r > indexOfCurrentBrake)
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										currentYPosition.doubleValue(SI.METER)
										- aisleWidth.doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
							else
								seriesSeats.add(
										Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METER)
										+ length.doubleValue(SI.METER)
										+ Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										+ r * Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										currentYPosition.doubleValue(SI.METER)
										- aisleWidth.doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
						}
						else {
							int columnIndex = r;
							if(breakesPositionsIndexList.stream().anyMatch(x -> x == columnIndex)) {
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ breakLengthPitchFraction * Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER)
										+ Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										Main.getTheAircraft().getFuselage().getSectionCylinderWidth().divide(2).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).divide(2).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
								indexOfCurrentBrake = r;
							}
							else if (r > indexOfCurrentBrake)
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										Main.getTheAircraft().getFuselage().getSectionCylinderWidth().divide(2).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).divide(2).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
							else
								seriesSeats.add(
										Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METER)
										+ length.doubleValue(SI.METER)
										+ Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										+ r * Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										Main.getTheAircraft().getFuselage().getSectionCylinderWidth().divide(2).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).divide(2).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
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
				Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
				+ length.doubleValue(SI.METER),
				- FusNacGeometryCalc.getWidthAtX(
						Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
						+ length.doubleValue(SI.METER),
						Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveX(),
						Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveY()
						)/2
				+ Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber).doubleValue(SI.METER)
				);
		seriesSeatBlock.add(
				Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
				+ length.doubleValue(SI.METER),
				FusNacGeometryCalc.getWidthAtX(
						Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
						+ length.doubleValue(SI.METER),
						Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveX(),
						Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveY()
						)/2
				- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber).doubleValue(SI.METER)
				);
		seatBlockSeriesList.add(seriesSeatBlock);
		
		double xMaxTopView = Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMinTopView = 0.0;

		int WIDTH = 650;
		int HEIGHT = 650;

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
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);

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
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "SeatMap.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  

		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.getCabinConfigurationSeatMapPane().getChildren().clear();
		theController.getCabinConfigurationSeatMapPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public void createWingPlanformView() {
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = Main.getTheAircraft().getWing().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		
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
		if (!Main.getTheAircraft().getWing().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getWing().getSymmetricFlaps().size(); i++) {
				
				double yIn = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getWing().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getWing().getSymmetricFlaps().get(i).getOuterChordRatio();
				
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
		if (!Main.getTheAircraft().getWing().getSlats().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getWing().getSlats().size(); i++) {
				
				double yIn = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getSlats().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getSlats().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getWing().getSlats().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getWing().getSlats().get(i).getOuterChordRatio();
				
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
		
		if (!Main.getTheAircraft().getWing().getAsymmetricFlaps().isEmpty()) {
			
			double yIn = Main.getTheAircraft().getWing().getSemiSpan().times(
					Main.getTheAircraft().getWing().getAsymmetricFlaps().get(0).getInnerStationSpanwisePosition()
					).doubleValue(SI.METER);
			double yOut = Main.getTheAircraft().getWing().getSemiSpan().times(
					Main.getTheAircraft().getWing().getAsymmetricFlaps().get(0).getOuterStationSpanwisePosition()
					).doubleValue(SI.METER);

			double localChordInner = GeometryCalc.getChordAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
					yIn);
			double localChordOuter = GeometryCalc.getChordAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
					yOut);

			double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
					yIn);
			double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
					yOut);

			double innerChordRatio = Main.getTheAircraft().getWing().getAsymmetricFlaps().get(0).getInnerChordRatio();
			double outerChordRatio = Main.getTheAircraft().getWing().getAsymmetricFlaps().get(0).getOuterChordRatio();

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
		if (!Main.getTheAircraft().getWing().getSpoilers().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getWing().getSpoilers().size(); i++) {
				
				double yIn = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getSpoilers().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getSpoilers().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
						yOut);
				
				double innerChordwisePosition = Main.getTheAircraft().getWing().getSpoilers().get(i).getInnerStationChordwisePosition();
				double outerChordwisePosition = Main.getTheAircraft().getWing().getSpoilers().get(i).getOuterStationChordwisePosition();
				
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
		
		double semispan = Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getWing().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getWing().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getWing().getDiscretizedXle().get(
				Main.getTheAircraft().getWing().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
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
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);

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
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "WingPlanform.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  

		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.getWingPlanformPane().getChildren().clear();
		theController.getWingPlanformPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public void createEquivalentWingView() {
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = Main.getTheAircraft().getWing().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		
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
		int nSec = Main.getTheAircraft().getWing().getDiscretizedXle().size();
		int nPanels = Main.getTheAircraft().getWing().getPanels().size();
		
		double xOffsetLE = Main.getTheAircraft().getWing().getXOffsetEquivalentWingRootLE();
		double xOffsetTE = Main.getTheAircraft().getWing().getXOffsetEquivalentWingRootTE();
		Amount<Length> equivalentWingSemispan = Main.getTheAircraft().getWing().getDiscretizedYs().get(nSec - 1);
		Amount<Angle> equivalentWingSweepLE = Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge();
		
		XYSeries seriesEquivalentWingTopView = new XYSeries("Equivalent Wing", false);
		
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getDiscretizedXle().get(0)
					.plus(Amount.valueOf(xOffsetLE, SI.METER)).doubleValue(SI.METER),
				0.0
				);
		
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getDiscretizedXle().get(0)
					.plus(Amount.valueOf(xOffsetLE, SI.METER))
					.plus(equivalentWingSemispan
						.times(Math.tan(equivalentWingSweepLE.doubleValue(SI.RADIAN)))).doubleValue(SI.METER),
				equivalentWingSemispan.doubleValue(SI.METER)
				);
		
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getDiscretizedXle().get(0)
					.plus(Amount.valueOf(xOffsetLE, SI.METER))
					.plus(equivalentWingSemispan
						.times(Math.tan(equivalentWingSweepLE.doubleValue(SI.RADIAN))))
					.plus(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getChordTip()).doubleValue(SI.METER),
				equivalentWingSemispan.doubleValue(SI.METER)
				);
		
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getDiscretizedXle().get(0)
					.plus(Main.getTheAircraft().getWing().getDiscretizedChords().get(0))
					.plus(Amount.valueOf(xOffsetTE, SI.METER)).doubleValue(SI.METER),
				0.0
				);
		
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getDiscretizedXle().get(0)
					.plus(Amount.valueOf(xOffsetLE, SI.METER)).doubleValue(SI.METER),
				0.0
				);

//		XYSeries seriesEquivalentWingTopView = new XYSeries("Equivalent Wing", false);
//		seriesEquivalentWingTopView.add(
//				Main.getTheAircraft().getWing().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordLE(),
//				0.0
//				);
//		seriesEquivalentWingTopView.add(
//				Main.getTheAircraft().getWing().getDiscretizedXle().get(nSec - 1).doubleValue(SI.METER),
//				Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER)
//				);
//		seriesEquivalentWingTopView.add(
//				Main.getTheAircraft().getWing().getDiscretizedXle().get(nSec - 1).plus(
//						Main.getTheAircraft().getWing().getPanels().get(nPanels - 1).getChordTip()
//						).doubleValue(SI.METER),
//				Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER)
//				);
//		seriesEquivalentWingTopView.add(
//				Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
//				- Main.getTheAircraft().getWing().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordTE(),
//				0.0
//				);
//		seriesEquivalentWingTopView.add(
//				Main.getTheAircraft().getWing().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordLE(),
//				0.0
//				);
		
		XYSeries seriesMeanAerodinamicChordView = new XYSeries("M.A.C.", false);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE),
				Main.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ Main.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METRE),
				Main.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		
		double semispan = Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getWing().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getWing().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getWing().getDiscretizedXle().get(
				Main.getTheAircraft().getWing().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
			
		int WIDTH = 650;
		int HEIGHT = 650;
		
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
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);

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
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "EquivalentWing.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  

		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.getEquivalentWingPane().getChildren().clear();
		theController.getEquivalentWingPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public void createHTailPlanformView() {
		
		//--------------------------------------------------
		// get data vectors from HTail discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = Main.getTheAircraft().getHTail().getDiscretizedTopViewAsArray(ComponentEnum.HORIZONTAL_TAIL);
		
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
		if (!Main.getTheAircraft().getHTail().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getHTail().getSymmetricFlaps().size(); i++) {
				
				double yIn = Main.getTheAircraft().getHTail().getSemiSpan().times(
						Main.getTheAircraft().getHTail().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getHTail().getSemiSpan().times(
						Main.getTheAircraft().getHTail().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getHTail().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getHTail().getSymmetricFlaps().get(i).getOuterChordRatio();
				
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
				Main.getTheAircraft().getHTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE),
				Main.getTheAircraft().getHTail().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getHTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ Main.getTheAircraft().getHTail().getMeanAerodynamicChord().doubleValue(SI.METRE),
				Main.getTheAircraft().getHTail().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		
		double semispan = Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getHTail().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getHTail().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getHTail().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getHTail().getDiscretizedXle().get(
				Main.getTheAircraft().getHTail().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getHTail().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
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
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);

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
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "HTailPlanform.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  

		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.gethTailPlanformPane().getChildren().clear();
		theController.gethTailPlanformPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public void createVTailPlanformView() {
		
		//--------------------------------------------------
		// get data vectors from VTail discretization
		//--------------------------------------------------
		Double[][] dataTopViewVTail = Main.getTheAircraft().getVTail().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);
		
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
		if (!Main.getTheAircraft().getVTail().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getVTail().getSymmetricFlaps().size(); i++) {
				
				double yIn = Main.getTheAircraft().getVTail().getSemiSpan().times(
						Main.getTheAircraft().getVTail().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getVTail().getSemiSpan().times(
						Main.getTheAircraft().getVTail().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getVTail().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getVTail().getSymmetricFlaps().get(i).getOuterChordRatio();
				
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
				Main.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE),
				Main.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE),
				Main.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ Main.getTheAircraft().getVTail().getMeanAerodynamicChord().doubleValue(SI.METRE)
				);
		
		double span = Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getVTail().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getVTail().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getVTail().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getVTail().getDiscretizedXle().get(
				Main.getTheAircraft().getVTail().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - span/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*span) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getVTail().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
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
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(yMinTopView, yMaxTopView);
		range.setInverted(Boolean.FALSE);

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
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "VTailPlanform.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  

		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.getvTailPlanformPane().getChildren().clear();
		theController.getvTailPlanformPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public void createCanardPlanformView() {
		
		//--------------------------------------------------
		// get data vectors from Canard discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = Main.getTheAircraft().getCanard().getDiscretizedTopViewAsArray(ComponentEnum.CANARD);
		
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
		if (!Main.getTheAircraft().getCanard().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getCanard().getSymmetricFlaps().size(); i++) {
				
				double yIn = Main.getTheAircraft().getCanard().getSemiSpan().times(
						Main.getTheAircraft().getCanard().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getCanard().getSemiSpan().times(
						Main.getTheAircraft().getCanard().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getCanard().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getCanard().getSymmetricFlaps().get(i).getOuterChordRatio();
				
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
				Main.getTheAircraft().getCanard().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE),
				Main.getTheAircraft().getCanard().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getCanard().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ Main.getTheAircraft().getCanard().getMeanAerodynamicChord().doubleValue(SI.METRE),
				Main.getTheAircraft().getCanard().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		
		double semispan = Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getCanard().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getCanard().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getCanard().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getCanard().getDiscretizedXle().get(
				Main.getTheAircraft().getCanard().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getCanard().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
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
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);

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
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "CanardPlanform.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  

		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.getCanardPlanformPane().getChildren().clear();
		theController.getCanardPlanformPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public void createNacelleTopView() {

		//---------------------------------------------------------------------------------
		// NACELLES NUMBER CHECK:
		if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
				theController.getTabPaneNacellesTopViews().getTabs().size()) {
			
			int iStart = theController.getTabPaneNacellesTopViews().getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
				
				Tab nacelleTab = new Tab("Nacelle " + (i+1));
				Pane topView = new Pane();
				theController.getNacelleTopViewPaneList().add(topView);
				nacelleTab.setContent(topView);
				theController.getTabPaneNacellesTopViews().getTabs().add(nacelleTab);
				
			}
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER NACELLES:
		for (int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

			//--------------------------------------------------
			// get data vectors from nacelle discretization
			//--------------------------------------------------

			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperY = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYRight().stream().forEach(y -> nacelleCurveUpperY.add(y));

			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerY = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYLeft().stream().forEach(y -> nacelleCurveLowerY.add(y));

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

			double xMaxTopView = 1.40*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double xMinTopView = -1.40*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double yMaxTopView = 1.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);
			double yMinTopView = -0.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);

			int WIDTH = 650;
			int HEIGHT = 650;

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
			NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
			range.setRange(xMinTopView, xMaxTopView);
			range.setInverted(Boolean.FALSE);

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
			String outputFilePathTopView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "Nacelle_" + (i+1) + "_TopView.svg";
			File outputFile = new File(outputFilePathTopView);
			if(outputFile.exists()) outputFile.delete();
			SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
			Rectangle r = new Rectangle(WIDTH, HEIGHT);
			chart.draw(g2, r);
			try {
				SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
			} catch (IOException e) {
				e.printStackTrace();
			}

			//-------------------------------------------------------------------------------------
			// PLOT TO PANE
			ChartCanvas canvas = new ChartCanvas(chart);
			StackPane stackPane = new StackPane(); 
			stackPane.getChildren().add(canvas);  

			// Bind canvas size to stack pane size. 
			canvas.widthProperty().bind(stackPane.widthProperty()); 
			canvas.heightProperty().bind(stackPane.heightProperty());  

			Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
			theController.getNacelleTopViewPaneList().get(i).getChildren().clear();
			theController.getNacelleTopViewPaneList().get(i).getChildren().add(sceneTopView.getRoot());

		}
	}
	
	public void createNacelleSideView() {

		//---------------------------------------------------------------------------------
		// NACELLES NUMBER CHECK:
		if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
				theController.getTabPaneNacellesSideViews().getTabs().size()) {
			
			int iStart = theController.getTabPaneNacellesSideViews().getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
				
				Tab nacelleTab = new Tab("Nacelle " + (i+1));
				Pane sideView = new Pane();
				theController.getNacelleSideViewPaneList().add(sideView);
				nacelleTab.setContent(sideView);
				theController.getTabPaneNacellesSideViews().getTabs().add(nacelleTab);
				
			}
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER NACELLES:
		for (int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

			//--------------------------------------------------
			// get data vectors from nacelle discretization
			//--------------------------------------------------

			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperZ = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZUpper().stream().forEach(z -> nacelleCurveUpperZ.add(z));

			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerZ = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZLower().stream().forEach(z -> nacelleCurveLowerZ.add(z));;

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

			double xMaxSideView = 1.40*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double xMinSideView = -1.40*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double yMaxSideView = 1.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);
			double yMinSideView = -0.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);

			int WIDTH = 650;
			int HEIGHT = 650;

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
			NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
			range.setRange(yMinSideView, yMaxSideView);
			range.setInverted(Boolean.FALSE);

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
			String outputFilePathTopView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "Nacelle_" + (i+1) + "_SideView.svg";
			File outputFile = new File(outputFilePathTopView);
			if(outputFile.exists()) outputFile.delete();
			SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
			Rectangle r = new Rectangle(WIDTH, HEIGHT);
			chart.draw(g2, r);
			try {
				SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
			} catch (IOException e) {
				e.printStackTrace();
			}

			//-------------------------------------------------------------------------------------
			// PLOT TO PANE
			ChartCanvas canvas = new ChartCanvas(chart);
			StackPane stackPane = new StackPane(); 
			stackPane.getChildren().add(canvas);  

			// Bind canvas size to stack pane size. 
			canvas.widthProperty().bind(stackPane.widthProperty()); 
			canvas.heightProperty().bind(stackPane.heightProperty());  

			Scene sceneSideView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
			theController.getNacelleSideViewPaneList().get(i).getChildren().clear();
			theController.getNacelleSideViewPaneList().get(i).getChildren().add(sceneSideView.getRoot());

		}
	}
	
	public void createNacelleFrontView() {

		//---------------------------------------------------------------------------------
		// NACELLES NUMBER CHECK:
		if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
				theController.getTabPaneNacellesFrontViews().getTabs().size()) {
			
			int iStart = theController.getTabPaneNacellesFrontViews().getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
				
				Tab nacelleTab = new Tab("Nacelle " + (i+1));
				Pane frontView = new Pane();
				theController.getNacelleFrontViewPaneList().add(frontView);
				nacelleTab.setContent(frontView);
				theController.getTabPaneNacellesFrontViews().getTabs().add(nacelleTab);
				
			}
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER NACELLES:
		for (int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

			//--------------------------------------------------
			// get data vectors from nacelle discretization
			//--------------------------------------------------
			double[] angleArray = MyArrayUtils.linspace(0.0, 2*Math.PI, 100);
			double[] yCoordinate = new double[angleArray.length];
			double[] zCoordinate = new double[angleArray.length];

			double radius = Main.getTheAircraft().getNacelles()
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

			double yMaxFrontView = 1.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getDiameterMax().doubleValue(SI.METRE);
			double yMinFrontView = -1.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getDiameterMax().doubleValue(SI.METRE);
			double xMaxFrontView = yMaxFrontView;
			double xMinFrontView = yMinFrontView;

			int WIDTH = 650;
			int HEIGHT = 650;

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
			NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
			range.setRange(yMinFrontView, yMaxFrontView);
			range.setInverted(Boolean.FALSE);

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
			String outputFilePathTopView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "Nacelle_" + (i+1) + "_FrontView.svg";
			File outputFile = new File(outputFilePathTopView);
			if(outputFile.exists()) outputFile.delete();
			SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
			Rectangle r = new Rectangle(WIDTH, HEIGHT);
			chart.draw(g2, r);
			try {
				SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
			} catch (IOException e) {
				e.printStackTrace();
			}

			//-------------------------------------------------------------------------------------
			// PLOT TO PANE
			ChartCanvas canvas = new ChartCanvas(chart);
			StackPane stackPane = new StackPane(); 
			stackPane.getChildren().add(canvas);  

			// Bind canvas size to stack pane size. 
			canvas.widthProperty().bind(stackPane.widthProperty()); 
			canvas.heightProperty().bind(stackPane.heightProperty());  

			Scene sceneFrontView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
			theController.getNacelleFrontViewPaneList().get(i).getChildren().clear();
			theController.getNacelleFrontViewPaneList().get(i).getChildren().add(sceneFrontView.getRoot());

		}
	}
	
}
