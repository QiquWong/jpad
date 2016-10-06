package jpadcommander.inputmanager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.treez.javafxd3.d3.svg.SymbolType;
import org.treez.javafxd3.javafx.JavaFxD3Browser;

import aircraft.components.Aircraft;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.RegulationsEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import graphics.D3Plotter;
import graphics.D3PlotterOptions;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import jpadcommander.Main;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;

public class InputManagerAircraftDefaultController {

	ObservableList<String> defaultAircraftList = FXCollections.observableArrayList(
			"ATR-72",
			"B747-100B",
			"AGILE-DC1"
			);
	
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox defaultAircraftChioseBox;
	
	@FXML
	@SuppressWarnings("unchecked")
	private void initialize() {
		defaultAircraftChioseBox.setItems(defaultAircraftList);
	}
	
	@FXML
	private void loadAircraftFile() throws IOException {

		Main.setStatus(State.RUNNING);
		Main.checkStatus(Main.getStatus());

		String databaseFolderPath = Main.getDatabaseDirectoryPath();
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		String highLiftDatabaseFileName = "HighLiftDatabase.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);

		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
		System.setOut(filterStream);

		if(defaultAircraftChioseBox
				.getSelectionModel()
					.getSelectedItem()
						.equals("ATR-72")) {
			Main.setChoiseBoxSelectionDefaultAircraft(
					defaultAircraftChioseBox
						.getSelectionModel()
							.getSelectedItem()
							);
			Main.setInputFileAbsolutePath("");
			Main.setTheAircraft(new Aircraft.AircraftBuilder(
					"ATR-72",
					AircraftEnum.ATR72,
					aeroDatabaseReader,
					highLiftDatabaseReader
					)
					.build()
					);
			logAircraftDefaultToInterface();
		}
		else if(defaultAircraftChioseBox
				.getSelectionModel()
				.getSelectedItem()
					.equals("B747-100B")) {
//			Main.setInputFileAbsolutePath("");
			Main.setChoiseBoxSelectionDefaultAircraft(
					defaultAircraftChioseBox
						.getSelectionModel()
							.getSelectedItem()
							);
			Alert alert = new Alert(
					AlertType.INFORMATION, 
					"Hello from DAF!\nThis action is still unimplemented.", 
					ButtonType.OK);
			alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			alert.show();
		}
		else if(defaultAircraftChioseBox
				.getSelectionModel()
				.getSelectedItem()
					.equals("AGILE-DC1")) {
//			Main.setInputFileAbsolutePath("");
			Main.setChoiseBoxSelectionDefaultAircraft(
					defaultAircraftChioseBox
						.getSelectionModel()
							.getSelectedItem()
							);
			Alert alert = new Alert(
					AlertType.INFORMATION, 
					"Hello from DAF!\nThis action is still unimplemented.", 
					ButtonType.OK);
			alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			alert.show();
		}

		createAircraftTopView();
		createAircraftSideView();
		createAircraftFrontView();
		
		// write again
		System.setOut(originalOut);
		
		//////////////////////////////////////////////////////////////////////////////////
		Main.setStatus(State.READY);
		Main.checkStatus(Main.getStatus());

	}	

	public static void createAircraftTopView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// left curve, upperview
		List<Amount<Length>> vX1Left = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountX();
		int nX1Left = vX1Left.size();
		List<Amount<Length>> vY1Left = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountY();

		Double[][] dataOutlineXYLeftCurve = new Double[nX1Left][2];
		IntStream.range(0, nX1Left)
		.forEach(i -> {
			dataOutlineXYLeftCurve[i][1] = vX1Left.get(i).doubleValue(SI.METRE);
			dataOutlineXYLeftCurve[i][0] = vY1Left.get(i).doubleValue(SI.METRE);
		});

		// right curve, upperview
		List<Amount<Length>> vX2Right = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountX();
		int nX2Right = vX2Right.size();
		List<Amount<Length>> vY2Right = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountY();

		Double[][] dataOutlineXYRightCurve = new Double[nX2Right][2];
		IntStream.range(0, nX2Right)
		.forEach(i -> {
			dataOutlineXYRightCurve[i][1] = vX2Right.get(i).doubleValue(SI.METRE);
			dataOutlineXYRightCurve[i][0] = vY2Right.get(i).doubleValue(SI.METRE);
		});

		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		List<Amount<Length>> vY = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs();
		int nY = vY.size();
		List<Amount<Length>> vChords = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedChords();
		List<Amount<Length>> vXle = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle();
		
		Double[][] dataChordsVsY = new Double[nY][2];
		Double[][] dataXleVsY = new Double[nY][2];
		IntStream.range(0, nY)
		.forEach(i -> {
			dataChordsVsY[i][0] = vY.get(i).doubleValue(SI.METRE);
			dataChordsVsY[i][1] = vChords.get(i).doubleValue(SI.METRE);
			dataXleVsY[i][0] = vY.get(i).doubleValue(SI.METRE);
			dataXleVsY[i][1] = vXle.get(i).doubleValue(SI.METRE);
		});

		Double[][] dataTopViewIsolated = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		
		Double[][] dataTopView = new Double[dataTopViewIsolated.length][dataTopViewIsolated[0].length];
		for (int i=0; i<dataTopViewIsolated.length; i++) { 
			dataTopView[i][0] = dataTopViewIsolated[i][0] + Main.getTheAircraft().getWing().getYApexConstructionAxes().doubleValue(SI.METER);
			dataTopView[i][1] = dataTopViewIsolated[i][1] + Main.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER);
		}
		
		Double[][] dataTopViewMirrored = new Double[dataTopView.length][dataTopView[0].length];
		for (int i=0; i<dataTopView.length; i++) { 
				dataTopViewMirrored[i][0] = -dataTopView[i][0];
				dataTopViewMirrored[i][1] = dataTopView[i][1];
		}

		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		List<Amount<Length>> vYHTail = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedYs();
		int nYHTail = vYHTail.size();
		List<Amount<Length>> vChordsHTail = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedChords();
		List<Amount<Length>> vXleHTail = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedXle();

		Double[][] dataChordsVsYHTail = new Double[nYHTail][2];
		Double[][] dataXleVsYHTail = new Double[nYHTail][2];
		IntStream.range(0, nYHTail)
		.forEach(i -> {
			dataChordsVsYHTail[i][0] = vYHTail.get(i).doubleValue(SI.METRE);
			dataChordsVsYHTail[i][1] = vChordsHTail.get(i).doubleValue(SI.METRE);
			dataXleVsYHTail[i][0] = vYHTail.get(i).doubleValue(SI.METRE);
			dataXleVsYHTail[i][1] = vXleHTail.get(i).doubleValue(SI.METRE);
		});

		Double[][] dataTopViewIsolatedHTail = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.HORIZONTAL_TAIL);

		Double[][] dataTopViewHTail = new Double[dataTopViewIsolatedHTail.length][dataTopViewIsolatedHTail[0].length];
		for (int i=0; i<dataTopViewIsolatedHTail.length; i++) { 
			dataTopViewHTail[i][0] = dataTopViewIsolatedHTail[i][0];
			dataTopViewHTail[i][1] = dataTopViewIsolatedHTail[i][1] + Main.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER);
		}

		Double[][] dataTopViewMirroredHTail = new Double[dataTopViewHTail.length][dataTopViewHTail[0].length];
		for (int i=0; i<dataTopViewHTail.length; i++) { 
			dataTopViewMirroredHTail[i][0] = -dataTopViewHTail[i][0];
			dataTopViewMirroredHTail[i][1] = dataTopViewHTail[i][1];
		}

		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		Double[] vTailRootXCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(0).getAirfoilCreator().getXCoords();
		Double[] vTailRootYCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(0).getAirfoilCreator().getZCoords();
		Double[][] vTailRootAirfoilPoints = new Double[vTailRootXCoordinates.length][2];
		for (int i=0; i<vTailRootAirfoilPoints.length; i++) {
			vTailRootAirfoilPoints[i][1] = (vTailRootXCoordinates[i]*Main.getTheAircraft().getVTail().getChordRoot().getEstimatedValue()) + Main.getTheAircraft().getVTail().getXApexConstructionAxes().getEstimatedValue(); 
			vTailRootAirfoilPoints[i][0] = (vTailRootYCoordinates[i]*Main.getTheAircraft().getVTail().getChordRoot().getEstimatedValue());
		}
		
		int nPointsVTail = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedXle().size();
		Double[] vTailTipXCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(Main.getTheAircraft().getVTail().getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
		Double[] vTailTipYCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(Main.getTheAircraft().getVTail().getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
		Double[][] vTailTipAirfoilPoints = new Double[vTailTipXCoordinates.length][2];
		for (int i=0; i<vTailTipAirfoilPoints.length; i++) {
			vTailTipAirfoilPoints[i][1] = (vTailTipXCoordinates[i]*Main.getTheAircraft().getVTail().getChordTip().getEstimatedValue()) 
										  + Main.getTheAircraft().getVTail().getXApexConstructionAxes().getEstimatedValue()
										  + Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsVTail-1).getEstimatedValue(); 
			vTailTipAirfoilPoints[i][0] = (vTailTipYCoordinates[i]*Main.getTheAircraft().getVTail().getChordTip().getEstimatedValue());
		}
		
		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<Double[][]> nacellePointsList = new ArrayList<Double[][]>();
		
		for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
			
			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline();
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperY = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYRight();
			
			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerY = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYLeft();

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
			
			
			Double[][] dataOutlineXZCurveNacelle = new Double[dataOutlineXZCurveNacelleX.size()][2];
			for(int j=0; j<dataOutlineXZCurveNacelleX.size(); j++) {
				dataOutlineXZCurveNacelle[j][1] = dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER);
				dataOutlineXZCurveNacelle[j][0] = dataOutlineXZCurveNacelleY.get(j).doubleValue(SI.METER);
			}
			
			nacellePointsList.add(dataOutlineXZCurveNacelle);
			
		}
		
		List<Double[][]> listDataArrayTopView = new ArrayList<Double[][]>();

		// wing
		listDataArrayTopView.add(dataTopView);
		listDataArrayTopView.add(dataTopViewMirrored);
		// hTail
		listDataArrayTopView.add(dataTopViewHTail);
		listDataArrayTopView.add(dataTopViewMirroredHTail);
		// fuselage
		listDataArrayTopView.add(dataOutlineXYLeftCurve);
		listDataArrayTopView.add(dataOutlineXYRightCurve);
		// vTail
		listDataArrayTopView.add(vTailRootAirfoilPoints);
		listDataArrayTopView.add(vTailTipAirfoilPoints);
		// nacelles
		for (int i=0; i<nacellePointsList.size(); i++)
			listDataArrayTopView.add(nacellePointsList.get(i));

		double xMaxTopView = 1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = 1.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMinTopView = -0.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
			
		// TODO : SEE HOW TO FIT THE IMAGE TO PARENT
		int WIDTH = 700;
		int HEIGHT = 600;
		
		D3PlotterOptions optionsTopView = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(xMinTopView, xMaxTopView)
				.yRange(yMaxTopView, yMinTopView)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
				.title("Aircraft data representation - Top View")
				.xLabel("y (m)")
				.yLabel("x (m)")
				.showXGrid(true)
				.showYGrid(true)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				.symbolSizes(2,2,2,2,2,2,2,2,2,2,2,2)
				.showSymbols(false,false,false,false,false,false,false,false,false,false,false,false) // NOTE: overloaded function
				.symbolStyles(
						"fill:blue; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2"
						)
				.lineStyles(
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2"
						)
				.plotAreas(true,true,true,true,true,true,true,true,true,true,true)
				.areaStyles("fill:lightblue;","fill:lightblue;","fill:blue;","fill:blue;","fill:white;","fill:white;",
						"fill:yellow;","fill:yellow;","fill:orange;","fill:orange;")
				.areaOpacities(1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0)
				.showLegend(false)
				.build();
		
		D3Plotter d3Plotter = new D3Plotter(
				optionsTopView,
				listDataArrayTopView
				);
		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter.createD3Content();
			
			//--------------------------------------------------
			// output
			String outputFilePathTopView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "AircraftTopView.svg";
			d3Plotter.saveSVG(outputFilePathTopView);

		}; // end-of-Runnable

		// create the Browser/D3
		//create browser
		JavaFxD3Browser browserTopView = d3Plotter.getBrowser(postLoadingHook, false);
		Scene sceneTopView = new Scene(
				browserTopView,
				WIDTH,
				HEIGHT,
				Color.web("#666970")
				);
		Main.getAircraftTopViewPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public static void createAircraftSideView() {
	
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// upper curve, sideview
		List<Amount<Length>> vX1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountX();
		int nX1Upper = vX1Upper.size();
		List<Amount<Length>> vZ1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountZ();

		Double[][] dataOutlineXZUpperCurve = new Double[nX1Upper][2];
		IntStream.range(0, nX1Upper)
		.forEach(i -> {
			dataOutlineXZUpperCurve[i][0] = vX1Upper.get(i).doubleValue(SI.METRE);
			dataOutlineXZUpperCurve[i][1] = vZ1Upper.get(i).doubleValue(SI.METRE);
		});

		// lower curve, sideview
		List<Amount<Length>> vX2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountX();
		int nX2Lower = vX2Lower.size();
		List<Amount<Length>> vZ2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountZ();

		Double[][] dataOutlineXZLowerCurve = new Double[nX2Lower][2];
		IntStream.range(0, nX2Lower)
		.forEach(i -> {
			dataOutlineXZLowerCurve[i][0] = vX2Lower.get(i).doubleValue(SI.METRE);
			dataOutlineXZLowerCurve[i][1] = vZ2Lower.get(i).doubleValue(SI.METRE);
		});
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		Double[] wingRootXCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(0).getAirfoilCreator().getXCoords();
		Double[] wingRootZCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(0).getAirfoilCreator().getZCoords();
		Double[][] wingRootAirfoilPoints = new Double[wingRootXCoordinates.length][2];
		for (int i=0; i<wingRootAirfoilPoints.length; i++) {
			wingRootAirfoilPoints[i][0] = (wingRootXCoordinates[i]*Main.getTheAircraft().getWing().getChordRoot().getEstimatedValue()) 
										  + Main.getTheAircraft().getWing().getXApexConstructionAxes().getEstimatedValue(); 
			wingRootAirfoilPoints[i][1] = (wingRootZCoordinates[i]*Main.getTheAircraft().getWing().getChordRoot().getEstimatedValue())
										  + Main.getTheAircraft().getWing().getZApexConstructionAxes().getEstimatedValue();
		}
		
		int nPointsWing = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().size();
		Double[] wingTipXCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(Main.getTheAircraft().getWing().getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
		Double[] wingTipZCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(Main.getTheAircraft().getWing().getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
		Double[][] wingTipAirfoilPoints = new Double[wingTipXCoordinates.length][2];
		for (int i=0; i<wingTipAirfoilPoints.length; i++) {
			wingTipAirfoilPoints[i][0] = (wingTipXCoordinates[i]*Main.getTheAircraft().getWing().getChordTip().getEstimatedValue()) 
										  + Main.getTheAircraft().getWing().getXApexConstructionAxes().getEstimatedValue()
										  + Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsWing-1).getEstimatedValue(); 
			wingTipAirfoilPoints[i][1] = (wingTipZCoordinates[i]*Main.getTheAircraft().getWing().getChordTip().getEstimatedValue())
										  + Main.getTheAircraft().getWing().getZApexConstructionAxes().getEstimatedValue();
		}
		
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		Double[] hTailRootXCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(0).getAirfoilCreator().getXCoords();
		Double[] hTailRootZCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(0).getAirfoilCreator().getZCoords();
		Double[][] hTailRootAirfoilPoints = new Double[hTailRootXCoordinates.length][2];
		for (int i=0; i<hTailRootAirfoilPoints.length; i++) {
			hTailRootAirfoilPoints[i][0] = (hTailRootXCoordinates[i]*Main.getTheAircraft().getHTail().getChordRoot().getEstimatedValue())
										   + Main.getTheAircraft().getHTail().getXApexConstructionAxes().getEstimatedValue(); 
			hTailRootAirfoilPoints[i][1] = (hTailRootZCoordinates[i]*Main.getTheAircraft().getHTail().getChordRoot().getEstimatedValue())
										   + Main.getTheAircraft().getHTail().getZApexConstructionAxes().getEstimatedValue();
		}
		
		int nPointsHTail = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedXle().size();
		Double[] hTailTipXCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(Main.getTheAircraft().getHTail().getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
		Double[] hTailTipZCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(Main.getTheAircraft().getHTail().getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
		Double[][] hTailTipAirfoilPoints = new Double[hTailTipXCoordinates.length][2];
		for (int i=0; i<hTailTipAirfoilPoints.length; i++) {
			hTailTipAirfoilPoints[i][0] = (hTailTipXCoordinates[i]*Main.getTheAircraft().getHTail().getChordTip().getEstimatedValue()) 
										  + Main.getTheAircraft().getHTail().getXApexConstructionAxes().getEstimatedValue()
										  + Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(); 
			hTailTipAirfoilPoints[i][1] = (hTailTipZCoordinates[i]*Main.getTheAircraft().getHTail().getChordTip().getEstimatedValue())
										  + Main.getTheAircraft().getHTail().getZApexConstructionAxes().getEstimatedValue();
		}
		
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		Double[][] dataTopViewVTail = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);
		for(int i=0; i<dataTopViewVTail.length; i++){
			dataTopViewVTail[i][0] += Main.getTheAircraft().getVTail().getXApexConstructionAxes().getEstimatedValue();
			dataTopViewVTail[i][1] += Main.getTheAircraft().getVTail().getZApexConstructionAxes().getEstimatedValue();
		}
		
		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<Double[][]> nacellePointsList = new ArrayList<Double[][]>();
		
		for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
			
			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline();
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperZ = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZUpper();
			
			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerZ = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZLower();

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
			
			
			Double[][] dataOutlineXZCurveNacelle = new Double[dataOutlineXZCurveNacelleX.size()][2];
			for(int j=0; j<dataOutlineXZCurveNacelleX.size(); j++) {
				dataOutlineXZCurveNacelle[j][0] = dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER);
				dataOutlineXZCurveNacelle[j][1] = dataOutlineXZCurveNacelleZ.get(j).doubleValue(SI.METER);
			}
			
			nacellePointsList.add(dataOutlineXZCurveNacelle);
			
		}
		
		List<Double[][]> listDataArraySideView = new ArrayList<Double[][]>();

		// vTail
		listDataArraySideView.add(dataTopViewVTail);
		// fuselage
		listDataArraySideView.add(dataOutlineXZUpperCurve);
		listDataArraySideView.add(dataOutlineXZLowerCurve);
		// wing
		listDataArraySideView.add(wingRootAirfoilPoints);
		listDataArraySideView.add(wingTipAirfoilPoints);
		// hTail
		listDataArraySideView.add(hTailRootAirfoilPoints);
		listDataArraySideView.add(hTailTipAirfoilPoints);
		// nacelles
		for (int i=0; i<nacellePointsList.size(); i++)
			listDataArraySideView.add(nacellePointsList.get(i));
		
		double xMaxSideView = 1.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double xMinSideView = -0.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMaxSideView = 1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double yMinSideView = -1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		D3PlotterOptions optionsSideView = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(xMinSideView, xMaxSideView)
				.yRange(yMinSideView, yMaxSideView)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
				.title("Aircraft data representation - Side View")
				.xLabel("x (m)")
				.yLabel("z (m)")
				.showXGrid(true)
				.showYGrid(true)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				.symbolSizes(2,2,2,2,2,2,2,2,2,2,2,2,2)
				.showSymbols(false,false,false,false,false,false,false,false,false,false,false,false,false) // NOTE: overloaded function
				.symbolStyles(
						"fill:blue; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2"
						)
				.lineStyles(
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2"
						)
				.plotAreas(true,true,true,true,true,true,true,true,true,true,true,true,true)
				.areaStyles("fill:yellow;","fill:white;","fill:white;","fill:lightblue;","fill:lightblue;","fill:blue;","fill:blue;",
						"fill:orange;","fill:orange;","fill:orange;","fill:orange;","fill:orange;","fill:orange;")
				.areaOpacities(1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0)
				.showLegend(false)
				.build();
		
		D3Plotter d3Plotter = new D3Plotter(
				optionsSideView,
				listDataArraySideView
				);
		
		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter.createD3Content();
			
			//--------------------------------------------------
			// output
			String outputFilePathSideView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "AircraftSideView.svg";
			d3Plotter.saveSVG(outputFilePathSideView);

		}; // end-of-Runnable

		// create the Browser/D3
		//create browser
		JavaFxD3Browser browserSideView = d3Plotter.getBrowser(postLoadingHook, false);
		Scene sceneSideView = new Scene(browserSideView, WIDTH+10, HEIGHT+10, Color.web("#666970"));
		Main.getAircraftSideViewPane().getChildren().add(sceneSideView.getRoot());		
		
	}
	
	public static void createAircraftFrontView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// section upper curve
		List<Amount<Length>> vY1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionUpperCurveAmountY();
		int nY1Upper = vY1Upper.size();
		List<Amount<Length>> vZ1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionUpperCurveAmountZ();

		Double[][] dataSectionYZUpperCurve = new Double[nY1Upper][2];
		IntStream.range(0, nY1Upper)
		.forEach(i -> {
			dataSectionYZUpperCurve[i][0] = vY1Upper.get(i).doubleValue(SI.METRE);
			dataSectionYZUpperCurve[i][1] = vZ1Upper.get(i).doubleValue(SI.METRE);
		});

		// section lower curve
		List<Amount<Length>> vY2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionLowerCurveAmountY();
		int nY2Lower = vY2Lower.size();
		List<Amount<Length>> vZ2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionLowerCurveAmountZ();

		Double[][] dataSectionYZLowerCurve = new Double[nY2Lower][2];
		IntStream.range(0, nY2Lower)
		.forEach(i -> {
			dataSectionYZLowerCurve[i][0] = vY2Lower.get(i).doubleValue(SI.METRE);
			dataSectionYZLowerCurve[i][1] = vZ2Lower.get(i).doubleValue(SI.METRE);
		});
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		List<Amount<Length>> wingBreakPointsYCoordinates = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getYBreakPoints();
		int nYPointsWingTemp = wingBreakPointsYCoordinates.size();
		for(int i=0; i<nYPointsWingTemp; i++)
			wingBreakPointsYCoordinates.add(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getYBreakPoints().get(nYPointsWingTemp-i-1));
		int nYPointsWing = wingBreakPointsYCoordinates.size();
		
		List<Amount<Length>> wingThicknessZCoordinates = new ArrayList<Amount<Length>>();
		for(int i=0; i<Main.getTheAircraft().getWing().getAirfoilList().size(); i++)
			wingThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
							MyArrayUtils.getMax(Main.getTheAircraft().getWing().getAirfoilList().get(i).getGeometry().getZCoords()),
							SI.METER
							)
					);
		for(int i=0; i<nYPointsWingTemp; i++) {
			wingThicknessZCoordinates.add(
					Amount.valueOf(
							(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getChordsBreakPoints().get(nYPointsWingTemp-i-1).doubleValue(SI.METER)*
									MyArrayUtils.getMin(Main.getTheAircraft().getWing().getAirfoilList().get(nYPointsWingTemp-i-1).getGeometry().getZCoords())),
							SI.METER
							)
					);
		}
		
		List<Amount<Angle>> dihedralList = new ArrayList<>();
		for (int i = 0; i < Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().size(); i++) {
			dihedralList.add(
					Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().get(i)
					);
		}
		for (int i = 0; i < Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().size(); i++) {
			dihedralList.add(
					Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().get(
							Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().size()-1-i)
					);
		}
		
		Double[][] dataFrontViewWing = new Double[nYPointsWing][2];
		IntStream.range(0, nYPointsWing)
		.forEach(i -> {
			dataFrontViewWing[i][0] = wingBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getWing().getYApexConstructionAxes()).doubleValue(SI.METRE);
			dataFrontViewWing[i][1] = wingThicknessZCoordinates.get(i)
					.plus(Main.getTheAircraft().getWing().getZApexConstructionAxes())
						.plus(wingBreakPointsYCoordinates.get(i)
										.times(Math.sin(dihedralList.get(i)
												.doubleValue(SI.RADIAN))
												)
										)
							.doubleValue(SI.METRE);
		});
		
		Double[][] dataFrontViewWingMirrored = new Double[nYPointsWing][2];
		IntStream.range(0, nYPointsWing)
		.forEach(i -> {
			dataFrontViewWingMirrored[i][0] = -dataFrontViewWing[i][0];
			dataFrontViewWingMirrored[i][1] = dataFrontViewWing[i][1];
		});
		
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		List<Amount<Length>> hTailBreakPointsYCoordinates = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getYBreakPoints();
		int nYPointsHTailTemp = hTailBreakPointsYCoordinates.size();
		for(int i=0; i<nYPointsHTailTemp; i++)
			hTailBreakPointsYCoordinates.add(Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getYBreakPoints().get(nYPointsHTailTemp-i-1));
		int nYPointsHTail = hTailBreakPointsYCoordinates.size();
		
		List<Amount<Length>> hTailThicknessZCoordinates = new ArrayList<Amount<Length>>();
		for(int i=0; i<Main.getTheAircraft().getHTail().getAirfoilList().size(); i++)
			hTailThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
							MyArrayUtils.getMax(Main.getTheAircraft().getHTail().getAirfoilList().get(i).getGeometry().getZCoords()),
							SI.METER
							)
					);
		for(int i=0; i<nYPointsHTailTemp; i++)
			hTailThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getChordsBreakPoints().get(nYPointsHTailTemp-i-1).doubleValue(SI.METER)*
							MyArrayUtils.getMin(Main.getTheAircraft().getHTail().getAirfoilList().get(nYPointsHTailTemp-i-1).getGeometry().getZCoords()),
							SI.METER
							)
					);
		
		List<Amount<Angle>> dihedralListHTail = new ArrayList<>();
		for (int i = 0; i < Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().size(); i++) {
			dihedralListHTail.add(
					Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().get(i)
					);
		}
		for (int i = 0; i < Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().size(); i++) {
			dihedralListHTail.add(
					Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().get(
							Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().size()-1-i)
					);
		}
		
		Double[][] dataFrontViewHTail = new Double[nYPointsHTail][2];
		IntStream.range(0, nYPointsHTail)
		.forEach(i -> {
			dataFrontViewHTail[i][0] = hTailBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getHTail().getYApexConstructionAxes()).doubleValue(SI.METRE);
			dataFrontViewHTail[i][1] = hTailThicknessZCoordinates.get(i)
					.plus(Main.getTheAircraft().getHTail().getZApexConstructionAxes())
					.plus(hTailBreakPointsYCoordinates.get(i)
									.times(Math.sin(dihedralListHTail.get(i)
											.doubleValue(SI.RADIAN))
											)
									)
						.doubleValue(SI.METRE);
		});
		
		Double[][] dataFrontViewHTailMirrored = new Double[nYPointsHTail][2];
		IntStream.range(0, nYPointsHTail)
		.forEach(i -> {
			dataFrontViewHTailMirrored[i][0] = -dataFrontViewHTail[i][0];
			dataFrontViewHTailMirrored[i][1] = dataFrontViewHTail[i][1];
		});
		
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		List<Amount<Length>> vTailBreakPointsYCoordinates = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getYBreakPoints();
		int nYPointsVTailTemp = vTailBreakPointsYCoordinates.size();
		for(int i=0; i<nYPointsVTailTemp; i++)
			vTailBreakPointsYCoordinates.add(Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getYBreakPoints().get(nYPointsVTailTemp-i-1));
		int nYPointsVTail = vTailBreakPointsYCoordinates.size();
		
		List<Amount<Length>> vTailThicknessZCoordinates = new ArrayList<Amount<Length>>();
		for(int i=0; i<Main.getTheAircraft().getVTail().getAirfoilList().size(); i++)
			vTailThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
							MyArrayUtils.getMax(Main.getTheAircraft().getVTail().getAirfoilList().get(i).getGeometry().getZCoords()),
							SI.METER
							)
					);
		for(int i=0; i<nYPointsVTailTemp; i++)
			vTailThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getChordsBreakPoints().get(nYPointsVTailTemp-i-1).doubleValue(SI.METER)*
							MyArrayUtils.getMin(Main.getTheAircraft().getVTail().getAirfoilList().get(nYPointsVTailTemp-i-1).getGeometry().getZCoords()),
							SI.METER
							)
					);
		
		Double[][] dataFrontViewVTail = new Double[nYPointsVTail][2];
		IntStream.range(0, nYPointsVTail)
		.forEach(i -> {
			dataFrontViewVTail[i][0] = vTailThicknessZCoordinates.get(i).plus(Main.getTheAircraft().getVTail().getYApexConstructionAxes()).doubleValue(SI.METRE);
			dataFrontViewVTail[i][1] = vTailBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getVTail().getZApexConstructionAxes()).doubleValue(SI.METRE);
		});
		
		//--------------------------------------------------
		// get data vectors from engine discretization
		//--------------------------------------------------
		List<Double[][]> nacellePointsList = new ArrayList<Double[][]>();

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

			Double[][] nacellePoints = new Double[yCoordinate.length][2];
			for (int j=0; j<yCoordinate.length; j++) {
				nacellePoints[j][0] = yCoordinate[j] + y0;
				nacellePoints[j][1] = zCoordinate[j] + z0;
			}

			nacellePointsList.add(nacellePoints);
		}
		
		List<Double[][]> listDataArrayFrontView = new ArrayList<Double[][]>();

		// hTail
		listDataArrayFrontView.add(dataFrontViewHTail);
		listDataArrayFrontView.add(dataFrontViewHTailMirrored);
		// vTail
		listDataArrayFrontView.add(dataFrontViewVTail);
		// wing
		listDataArrayFrontView.add(dataFrontViewWing);
		listDataArrayFrontView.add(dataFrontViewWingMirrored);
		// fuselage
		listDataArrayFrontView.add(dataSectionYZUpperCurve);
		listDataArrayFrontView.add(dataSectionYZLowerCurve);
		// nacelles
		for (int i=0; i<nacellePointsList.size(); i++)
			listDataArrayFrontView.add(nacellePointsList.get(i));
		
		double yMaxFrontView = 1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double yMinFrontView = -1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
		double zMaxFrontView = yMaxFrontView; 
		double zMinFrontView = yMinFrontView;
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		D3PlotterOptions optionsFrontView = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(yMinFrontView, yMaxFrontView)
				.yRange(zMinFrontView, zMaxFrontView)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
				.title("Aircraft data representation - Front View")
				.xLabel("y (m)")
				.yLabel("z (m)")
				.showXGrid(true)
				.showYGrid(true)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				.symbolSizes(2,2,2,2,2,2,2,2,2,2,2)
				.showSymbols(false,false,false,false,false,false,false,false,false,false) // NOTE: overloaded function
				.symbolStyles(
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2"
						)
				.lineStyles(
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2"
						)
				.plotAreas(true,true,true,true,true,true,true,true,true,true)
				.areaStyles("fill:blue;","fill:blue;","fill:yellow;","fill:lightblue;","fill:lightblue;","fill:white;","fill:white;",
						"fill:orange;","fill:orange;")
				.areaOpacities(1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0)
				.showLegend(false)
				.build();
		
		D3Plotter d3Plotter = new D3Plotter(
				optionsFrontView,
				listDataArrayFrontView
				);

		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter.createD3Content();
			
			//--------------------------------------------------
			// output
			String outputFilePathFrontView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "AircraftFrontView.svg";
			d3Plotter.saveSVG(outputFilePathFrontView);


		}; // end-of-Runnable

		// create the Browser/D3
		//create browser
		JavaFxD3Browser browserFrontView = d3Plotter.getBrowser(postLoadingHook, false);

		//create the scene
		Scene sceneFrontView = new Scene(browserFrontView, WIDTH+10, HEIGHT+10, Color.web("#666970"));
		Main.getAircraftFrontViewPane().getChildren().add(sceneFrontView.getRoot());
	}
	
	@SuppressWarnings("unchecked")
	public static void logAircraftDefaultToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		Main.setTextAreaAircraftConsoleOutput(
				(TextArea) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#output")
				);
		Main.getTextAreaAircraftConsoleOutput().setText(
				Main.getTheAircraft().toString()
				);

		// clear all the file path text fields: 
		if(Main.getChoiceBoxAircraftType() != null)
			Main.getChoiceBoxAircraftType().getSelectionModel().clearSelection();
		if(Main.getChoiceBoxRegulationsType() != null)
			Main.getChoiceBoxRegulationsType().getSelectionModel().clearSelection();
		if(Main.getTextFieldAircraftCabinConfiguration() != null)
			Main.getTextFieldAircraftCabinConfiguration().clear();
		if(Main.getTextFieldAircraftFuselageFile() != null)
			Main.getTextFieldAircraftFuselageFile().clear();
		if(Main.getTextFieldAircraftWingFile() != null)
			Main.getTextFieldAircraftWingFile().clear();
		if(Main.getTextFieldAircraftHorizontalTailFile() != null)
			Main.getTextFieldAircraftHorizontalTailFile().clear();
		if(Main.getTextFieldAircraftVerticalTailFile() != null)
			Main.getTextFieldAircraftVerticalTailFile().clear();
		if(Main.getTextFieldAircraftCanardFile() != null)
			Main.getTextFieldAircraftCanardFile().clear();
		if(Main.getTextFieldAircraftEngineFileList() != null)
			Main.getTextFieldAircraftEngineFileList().clear();
		if(Main.getTextFieldAircraftNacelleFileList() != null)
			Main.getTextFieldAircraftNacelleFileList().clear();
		if(Main.getTextFieldAircraftLandingGearsFile() != null)
			Main.getTextFieldAircraftLandingGearsFile().clear();
		if(Main.getTextFieldAircraftSystemsFile() != null)
			Main.getTextFieldAircraftSystemsFile().clear();
		
		//---------------------------------------------------------------------------------
		// AIRCRAFT TYPE:
		Main.setChoiceBoxAircraftType(
				(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftType")
				);
		
		AircraftTypeEnum aircraftTypeFileName = Main.getTheAircraft().getTypeVehicle();
		
		if(aircraftTypeFileName != null) { 
			if(Main.getChoiceBoxAircraftType() != null) {
				if(aircraftTypeFileName.toString().equalsIgnoreCase("JET"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(0);
				else if(aircraftTypeFileName.toString().equalsIgnoreCase("FIGHTER"))		
					Main.getChoiceBoxAircraftType().getSelectionModel().select(1);
				else if(aircraftTypeFileName.toString().equalsIgnoreCase("BUSINESS_JET"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(2);
				else if(aircraftTypeFileName.toString().equalsIgnoreCase("TURBOPROP"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(3);
				else if(aircraftTypeFileName.toString().equalsIgnoreCase("GENERAL_AVIATION"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(4);
				else if(aircraftTypeFileName.toString().equalsIgnoreCase("COMMUTER"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(5);
				else if(aircraftTypeFileName.toString().equalsIgnoreCase("ACROBATIC"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(6);
			}
		}
		
		//---------------------------------------------------------------------------------
		// REGULATIONS TYPE:
		Main.setChoiceBoxRegulationsType(
				(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxRegulationsType")
				);
		
		RegulationsEnum regulationsTypeFileName = Main.getTheAircraft().getRegulations();
		
		if(regulationsTypeFileName != null) { 
			if(Main.getChoiceBoxRegulationsType() != null) {
				if(regulationsTypeFileName.toString().equalsIgnoreCase("FAR_23"))
					Main.getChoiceBoxRegulationsType().getSelectionModel().select(0);
				else if(regulationsTypeFileName.toString().equalsIgnoreCase("FAR_25"))		
					Main.getChoiceBoxRegulationsType().getSelectionModel().select(1);
			}
		}
		
		//---------------------------------------------------------------------------------
		// FUSELAGE:
		Main.setTextFieldAircraftFuselageX(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftFuselageX")
				);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldAircraftFuselageX().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftFuselageX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftFuselageY(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftFuselageY")
				);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldAircraftFuselageY().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftFuselageY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftFuselageZ(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftFuselageZ")
				);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldAircraftFuselageZ().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftFuselageZ().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// WING:
		Main.setTextFieldAircraftWingX(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingX")
				);
		if(Main.getTheAircraft().getWing() != null)
			Main.getTextFieldAircraftWingX().setText(
					Main.getTheAircraft()
					.getWing()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftWingX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftWingY(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingY")
				);
		if(Main.getTheAircraft().getWing() != null)
			Main.getTextFieldAircraftWingY().setText(
					Main.getTheAircraft()
					.getWing()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftWingY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftWingZ(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingZ")
				);
		if(Main.getTheAircraft().getWing() != null)
			Main.getTextFieldAircraftWingZ().setText(
					Main.getTheAircraft()
					.getWing()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftWingZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftWingRiggingAngle(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingRiggingAngle")
				);
		if(Main.getTheAircraft().getWing() != null)
			Main.getTextFieldAircraftWingRiggingAngle().setText(
					Main.getTheAircraft()
					.getWing()
					.getRiggingAngle()
					.toString()
					);
		else
			Main.getTextFieldAircraftWingRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// HORIZONTAL TAIL:
		Main.setTextFieldAircraftHorizontalTailX(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailX")
				);
		if(Main.getTheAircraft().getHTail() != null)
			Main.getTextFieldAircraftHorizontalTailX().setText(
					Main.getTheAircraft()
					.getHTail()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftHorizontalTailX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftHorizontalTailY(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailY")
				);
		if(Main.getTheAircraft().getHTail() != null)
			Main.getTextFieldAircraftHorizontalTailY().setText(
					Main.getTheAircraft()
					.getHTail()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftHorizontalTailY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftHorizontalTailZ(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailZ")
				);
		if(Main.getTheAircraft().getHTail() != null)
			Main.getTextFieldAircraftHorizontalTailZ().setText(
					Main.getTheAircraft()
					.getHTail()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftHorizontalTailZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftHorizontalTailRiggingAngle(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailRiggingAngle")
				);
		if(Main.getTheAircraft().getHTail() != null)
			Main.getTextFieldAircraftHorizontalTailRiggingAngle().setText(
					Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle()
					.toString()
					);
		else
			Main.getTextFieldAircraftHorizontalTailRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// VERTICAL TAIL:
		Main.setTextFieldAircraftVerticalTailX(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailX")
				);
		if(Main.getTheAircraft().getVTail() != null)
			Main.getTextFieldAircraftVerticalTailX().setText(
					Main.getTheAircraft()
					.getVTail()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftVerticalTailX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftVerticalTailY(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailY")
				);
		if(Main.getTheAircraft().getVTail() != null)
			Main.getTextFieldAircraftVerticalTailY().setText(
					Main.getTheAircraft()
					.getVTail()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftVerticalTailY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftVerticalTailZ(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailZ")
				);
		if(Main.getTheAircraft().getVTail() != null)
			Main.getTextFieldAircraftVerticalTailZ().setText(
					Main.getTheAircraft()
					.getVTail()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftVerticalTailZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftVerticalTailRiggingAngle(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailRiggingAngle")
				);
		if(Main.getTheAircraft().getVTail() != null)
			Main.getTextFieldAircraftVerticalTailRiggingAngle().setText(
					Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle()
					.toString()
					);
		else
			Main.getTextFieldAircraftHorizontalTailRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// CANARD:
		//		if(Main.getTextFieldAircraftCanardX() == null)
		Main.setTextFieldAircraftCanardX(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardX")
				);
		if(Main.getTheAircraft().getCanard() != null)
			Main.getTextFieldAircraftCanardX().setText(
					Main.getTheAircraft()
					.getCanard()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftCanardX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftCanardY(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardY")
				);
		if(Main.getTheAircraft().getCanard() != null)
			Main.getTextFieldAircraftCanardY().setText(
					Main.getTheAircraft()
					.getCanard()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftCanardY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftCanardZ(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardZ")
				);
		if(Main.getTheAircraft().getCanard() != null)
			Main.getTextFieldAircraftCanardZ().setText(
					Main.getTheAircraft()
					.getCanard()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftCanardZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftCanardRiggingAngle(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardRiggingAngle")
				);
		if(Main.getTheAircraft().getCanard() != null)
			Main.getTextFieldAircraftCanardRiggingAngle().setText(
					Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle()
					.toString()
					);
		else
			Main.getTextFieldAircraftCanardRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// POWER PLANT:
		Main.getTextFieldAircraftEngineFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile1")
				);
		Main.getTextFieldAircraftEngineFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile2")
				);
		Main.getTextFieldAircraftEngineFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile3")
				);
		Main.getTextFieldAircraftEngineFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile4")
				);
		Main.getTextFieldAircraftEngineFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile5")
				);
		Main.getTextFieldAircraftEngineFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftEngineXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX1")
				);
		Main.getTextFieldAircraftEngineXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX2")
				);
		Main.getTextFieldAircraftEngineXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX3")
				);
		Main.getTextFieldAircraftEngineXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX4")
				);
		Main.getTextFieldAircraftEngineXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX5")
				);
		Main.getTextFieldAircraftEngineXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftEngineYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY1")
				);
		Main.getTextFieldAircraftEngineYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY2")
				);
		Main.getTextFieldAircraftEngineYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY3")
				);
		Main.getTextFieldAircraftEngineYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY4")
				);
		Main.getTextFieldAircraftEngineYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY5")
				);
		Main.getTextFieldAircraftEngineYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftEngineZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ1")
				);
		Main.getTextFieldAircraftEngineZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ2")
				);
		Main.getTextFieldAircraftEngineZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ3")
				);
		Main.getTextFieldAircraftEngineZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ4")
				);
		Main.getTextFieldAircraftEngineZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ5")
				);
		Main.getTextFieldAircraftEngineZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftEnginePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEnginePosition1")
				);
		Main.getTextFieldAircraftEnginePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEnginePosition2")
				);
		Main.getTextFieldAircraftEnginePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEnginePosition3")
				);
		Main.getTextFieldAircraftEnginePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEnginePosition4")
				);
		Main.getTextFieldAircraftEnginePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEnginePosition5")
				);
		Main.getTextFieldAircraftEnginePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEnginePosition6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftEngineTiltList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt1")
				);
		Main.getTextFieldAircraftEngineTiltList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt2")
				);
		Main.getTextFieldAircraftEngineTiltList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt3")
				);
		Main.getTextFieldAircraftEngineTiltList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt4")
				);
		Main.getTextFieldAircraftEngineTiltList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt5")
				);
		Main.getTextFieldAircraftEngineTiltList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt6")
				);
		//..........................................................................................................
		if(Main.getTheAircraft().getPowerPlant() != null) {
			for (int i = 0; i < Main.getTheAircraft().getPowerPlant().getEngineNumber(); i++) {
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineXList().get(i).setText(
							Main.getTheAircraft()
							.getPowerPlant()
							.getEngineList()
							.get(i)
							.getXApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftEngineXList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineYList().get(i).setText(
							Main.getTheAircraft()
							.getPowerPlant()
							.getEngineList()
							.get(i)
							.getYApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftEngineYList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineZList().get(i).setText(
							Main.getTheAircraft()
							.getPowerPlant()
							.getEngineList()
							.get(i)
							.getZApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftEngineZList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEnginePositonList().get(i).setText(
							Main.getTheAircraft()
							.getPowerPlant()
							.getEngineList()
							.get(i)
							.getMountingPosition()
							.toString()
							);
				else
					Main.getTextFieldAircraftEnginePositonList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineTiltList().get(i).setText(
							Main.getTheAircraft()
							.getPowerPlant()
							.getEngineList()
							.get(i)
							.getTiltingAngle()
							.toString()
							);
				else
					Main.getTextFieldAircraftEngineTiltList().get(i).setText(
							"NOT INITIALIZED"
							);
			}
		}
		//---------------------------------------------------------------------------------
		// NACELLES:
		Main.getTextFieldAircraftNacelleFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile1")
				);
		Main.getTextFieldAircraftNacelleFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile2")
				);
		Main.getTextFieldAircraftNacelleFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile3")
				);
		Main.getTextFieldAircraftNacelleFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile4")
				);
		Main.getTextFieldAircraftNacelleFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile5")
				);
		Main.getTextFieldAircraftNacelleFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftNacelleXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX1")
				);
		Main.getTextFieldAircraftNacelleXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX2")
				);
		Main.getTextFieldAircraftNacelleXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX3")
				);
		Main.getTextFieldAircraftNacelleXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX4")
				);
		Main.getTextFieldAircraftNacelleXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX5")
				);
		Main.getTextFieldAircraftNacelleXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftNacelleYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY1")
				);
		Main.getTextFieldAircraftNacelleYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY2")
				);
		Main.getTextFieldAircraftNacelleYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY3")
				);
		Main.getTextFieldAircraftNacelleYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY4")
				);
		Main.getTextFieldAircraftNacelleYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY5")
				);
		Main.getTextFieldAircraftNacelleYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftNacelleZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ1")
				);
		Main.getTextFieldAircraftNacelleZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ2")
				);
		Main.getTextFieldAircraftNacelleZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ3")
				);
		Main.getTextFieldAircraftNacelleZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ4")
				);
		Main.getTextFieldAircraftNacelleZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ5")
				);
		Main.getTextFieldAircraftNacelleZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftNacellePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacellePosition1")
				);
		Main.getTextFieldAircraftNacellePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacellePosition2")
				);
		Main.getTextFieldAircraftNacellePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacellePosition3")
				);
		Main.getTextFieldAircraftNacellePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacellePosition4")
				);
		Main.getTextFieldAircraftNacellePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacellePosition5")
				);
		Main.getTextFieldAircraftNacellePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacellePosition6")
				);
		//..........................................................................................................
		if(Main.getTheAircraft().getNacelles() != null) {
			for (int i = 0; i < Main.getTheAircraft().getNacelles().getNacellesNumber(); i++) {
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleXList().get(i).setText(
							Main.getTheAircraft()
							.getNacelles()
							.getNacellesList()
							.get(i)
							.getXApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftNacelleXList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleYList().get(i).setText(
							Main.getTheAircraft()
							.getNacelles()
							.getNacellesList()
							.get(i)
							.getYApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftNacelleYList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleZList().get(i).setText(
							Main.getTheAircraft()
							.getNacelles()
							.getNacellesList()
							.get(i)
							.getZApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftNacelleZList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacellePositonList().get(i).setText(
							Main.getTheAircraft()
							.getNacelles()
							.getNacellesList()
							.get(i)
							.getMountingPosition()
							.toString()
							);
				else
					Main.getTextFieldAircraftNacellePositonList().get(i).setText(
							"NOT INITIALIZED"
							);
			}
		}
		//---------------------------------------------------------------------------------
		// LANDING GEARS:
		Main.setTextFieldAircraftLandingGearsX(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftLandingGearsX")
				);
		if(Main.getTheAircraft().getLandingGears() != null)
			Main.getTextFieldAircraftLandingGearsX().setText(
					Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftLandingGearsX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftLandingGearsY(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftLandingGearsY")
				);
		if(Main.getTheAircraft().getLandingGears() != null)
			Main.getTextFieldAircraftLandingGearsY().setText(
					Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftLandingGearsY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftLandingGearsZ(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftLandingGearsZ")
				);
		if(Main.getTheAircraft().getLandingGears() != null)
			Main.getTextFieldAircraftLandingGearsZ().setText(
					Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftLandingGearsZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftLandingGearsPosition(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftLandingGearsPosition")
				);
		if(Main.getTheAircraft().getLandingGears() != null)
			Main.getTextFieldAircraftLandingGearsPosition().setText(
					Main.getTheAircraft()
					.getLandingGears()
					.getMountingPosition()
					.toString()
					);
		else
			Main.getTextFieldAircraftLandingGearsPosition().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// SYSTEMS:
		Main.setTextFieldAircraftSystemsX(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftSystemsX")
				);
		if(Main.getTheAircraft().getSystems() != null)
			Main.getTextFieldAircraftSystemsX().setText(
					Main.getTheAircraft()
					.getSystems()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftSystemsX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftSystemsY(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftSystemsY")
				);
		if(Main.getTheAircraft().getSystems() != null)
			Main.getTextFieldAircraftSystemsY().setText(
					Main.getTheAircraft()
					.getSystems()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftSystemsY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftSystemsZ(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftSystemsZ")
				);
		if(Main.getTheAircraft().getSystems() != null)
			Main.getTextFieldAircraftSystemsZ().setText(
					Main.getTheAircraft()
					.getSystems()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftSystemsZ().setText(
					"NOT INITIALIZED"
					);
	}
	
}
