package sandbox2.vt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.treez.javafxd3.d3.svg.SymbolType;
import org.treez.javafxd3.javafx.JavaFxD3Browser;

import aircraft.components.FuelTank;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sandbox2.javafx.D3Plotter;
import sandbox2.javafx.D3PlotterOptions;
import standaloneutils.JPADXmlReader;
import writers.JPADStaticWriteUtils;

public class FuelTankTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static LiftingSurface theWing;
	
	public static FuelTank theFuelTank;

	//-------------------------------------------------------------

	private D3Plotter d3Plotter;

	private final int WIDTH = 700;
	private final int HEIGHT = 600;

	@SuppressWarnings("unused")
	private static final double DELTA = 0.001d;

	@Override
	public void start(Stage primaryStage) throws Exception {

		//--------------------------------------------------
		// get the wing object
		System.out.println("\n\n##################");
		System.out.println("function start :: getting the wing object ...");

		LiftingSurface wing = FuelTankTest.theWing;
		if (wing == null) {
			System.out.println("wing object null, returning.");
			return;
		}

		System.out.println("The wing ...");
		System.out.println(wing);
		System.out.println("Details on panel discretization ...");
		wing.reportPanelsToSpanwiseDiscretizedVariables();

		System.out.println("\n\n##################");
		System.out.println("getting the fuel tank object ...");

		FuelTank fuelTank = FuelTankTest.theFuelTank;
		if (fuelTank == null) {
			System.out.println("fuel tank object null, returning.");
			return;
		}

		System.out.println("The fuel tank ...");
		System.out.println(fuelTank);
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		List<Amount<Length>> vY = wing.getDiscretizedYs();
		int nY = vY.size();
		List<Amount<Length>> vChords = wing.getDiscretizedChords();
		List<Amount<Length>> vXle = wing.getDiscretizedXle();

		Double[][] dataChordsVsY = new Double[nY][2];
		Double[][] dataXleVsY = new Double[nY][2];
		IntStream.range(0, nY)
		.forEach(i -> {
			dataChordsVsY[i][0] = vY.get(i).doubleValue(SI.METRE);
			dataChordsVsY[i][1] = vChords.get(i).doubleValue(SI.METRE);
			dataXleVsY[i][0] = vY.get(i).doubleValue(SI.METRE);
			dataXleVsY[i][1] = vXle.get(i).doubleValue(SI.METRE);
		});

		System.out.println("##################\n\n");

		Double[][] dataTopView = wing.getDiscretizedTopViewAsArray(ComponentEnum.WING);

		//--------------------------------------------------
		// get data vectors from fuel tank discretization
		List<Amount<Length>> fuelTankXCoordinates = new ArrayList<Amount<Length>>();
		int nStationFuelTank = wing.getYBreakPoints().size();
		for (int i=0; i<nStationFuelTank-1; i++) {
			fuelTankXCoordinates.add(wing.getYBreakPoints().get(i));
		}
		fuelTankXCoordinates.add(wing.getSemiSpan().times(0.85));
		fuelTankXCoordinates.add(wing.getSemiSpan().times(0.85));
		for (int i=1; i<nStationFuelTank; i++) {
			fuelTankXCoordinates.add(wing.getYBreakPoints().get(nStationFuelTank-i-1));
		}
		
		Amount<Length> xLEAt85Percent = theWing
												.getXLEAtYActual(
														theWing.getSemiSpan()
															.times(0.85)
																.doubleValue(SI.METER)
																);
		Amount<Length> chordAt85Percent = Amount.valueOf(
				theWing.getChordAtYActual(
						theWing.getSemiSpan().times(0.85).doubleValue(SI.METER)
						),
				SI.METER
				);
		
		List<Amount<Length>> fuelTankYCoordinates = new ArrayList<Amount<Length>>();
		for(int i=0; i<nStationFuelTank-1; i++) {
			fuelTankYCoordinates.add(
					theWing.getChordsBreakPoints().get(i)
						.times(theFuelTank.getTheWing().getMainSparDimensionlessPosition())
							.plus(theWing.getXLEBreakPoints().get(i))
						);
		}
		fuelTankYCoordinates.add(
				chordAt85Percent
					.times(theFuelTank.getTheWing().getMainSparDimensionlessPosition())
						.plus(xLEAt85Percent));
		fuelTankYCoordinates.add(
				chordAt85Percent
					.times(theFuelTank.getTheWing().getSecondarySparDimensionlessPosition())
						.plus(xLEAt85Percent));
		for(int i=1; i<nStationFuelTank; i++) {
			fuelTankYCoordinates.add(
					theWing.getChordsBreakPoints().get(nStationFuelTank-i-1)
						.times(theFuelTank.getTheWing().getSecondarySparDimensionlessPosition())
							.plus(theWing.getXLEBreakPoints().get(nStationFuelTank-i-1))
						);
		}
		
		Double[][] dataFuelTank = new Double[fuelTankXCoordinates.size()][2];
		IntStream.range(0, fuelTankXCoordinates.size())
		.forEach(i -> {
			dataFuelTank[i][0] = fuelTankXCoordinates.get(i).doubleValue(SI.METER);
			dataFuelTank[i][1] = fuelTankYCoordinates.get(i).doubleValue(SI.METER);
		});
		
		//--------------------------------------------------
		System.out.println("Initializing test class...");
		String rootOutputFolderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String outputFolderPath = JPADStaticWriteUtils.createNewFolder(rootOutputFolderPath + "Tests_Aircraft" + File.separator);
		System.out.println("Output ==> " + outputFolderPath);

		//--------------------------------------------------
		//set state title
		primaryStage.setTitle("treez/javafx-d3 - D3Plotter demo");

		List<Double[][]> listDataArray = new ArrayList<Double[][]>();

		listDataArray.add(dataTopView);
		listDataArray.add(dataFuelTank);
		
		double xMax = 1.05*wing.getSemiSpan().doubleValue(SI.METRE);
		double xMin = -0.05*wing.getSemiSpan().doubleValue(SI.METRE);
		double yMax = 1.30*wing.getSemiSpan().divide(2).doubleValue(SI.METRE);
		double yMin = -0.80*wing.getSemiSpan().divide(2).doubleValue(SI.METRE);

		D3PlotterOptions options = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(xMin, xMax)
				.yRange(yMax, yMin)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.1)
				.title("Fuel tank representation")
				.xLabel("y (m)")
				.yLabel("x (m)")
				.showXGrid(true)
				.showYGrid(true)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				.symbolSizes(20,20)
				.showSymbols(false,false) // NOTE: overloaded function
				.symbolStyles(
						"fill:cyan; stroke:black; stroke-width:2",
						"fill:cyan; stroke:black; stroke-width:2"
						)
				.lineStyles(
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:blue; stroke-width:2"
						)
				.plotAreas(true,true)
				.areaStyles("fill:orange;","fill:yellow;")
				.areaOpacities(0.7,1.0)
				.showLegend(false)
				.build();

//		System.out.println("Plot options:\n" + options);

		d3Plotter = new D3Plotter(
				options,
				listDataArray
				);


		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {
			System.out.println("Runnable :: Initial loading of browser is finished");

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter.createD3Content();

			//--------------------------------------------------
			// output
			String outputFilePath = outputFolderPath + "FuelTank.svg";
			d3Plotter.saveSVG(outputFilePath);


		}; // end-of-Runnable

		// create the Browser/D3
		//create browser
		JavaFxD3Browser browser = d3Plotter.getBrowser(postLoadingHook, false);

		//create the scene
		Scene scene = new Scene(browser, WIDTH+10, HEIGHT+10, Color.web("#666970"));
		primaryStage.setScene(scene);

		// SHOW THE SCEN FINALLY
		primaryStage.show();

	}

	/**
	 * Main
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO: check out this as an alternative
		// https://blog.codecentric.de/en/2015/09/javafx-how-to-easily-implement-application-preloader-2/

		System.out.println("--------------");
		System.out.println("Fuel tank test / D3");
		System.out.println("--------------");

		MyArgumentWing va = new MyArgumentWing();
		FuelTankTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			FuelTankTest.theCmdLineParser.parseArgument(args);
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToXML);

			String dirAirfoil = va.getAirfoilDirectory().getCanonicalPath();
			System.out.println("AIRFOILS ===> " + dirAirfoil);

			System.out.println("--------------");

			// This wing static object is available in the scope of
			// the Application.start method
			
			//------------------------------------------------------------------------------------
			// Setup database(s)
			MyConfiguration.initWorkingDirectoryTree();
			
			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
			String vedscDatabaseFilename = "VeDSC_database.h5";
			AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
			HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
			VeDSCDatabaseReader veDSCDatabaseReader = new VeDSCDatabaseReader(databaseFolderPath, vedscDatabaseFilename);
			
			// imported wing from xml ...
			theWing = LiftingSurface.importFromXML(ComponentEnum.WING, pathToXML, dirAirfoil);
			theWing.setAeroDatabaseReader(aeroDatabaseReader);
			theWing.setHighLiftDatabaseReader(highLiftDatabaseReader);
			theWing.setVeDSCDatabaseReader(veDSCDatabaseReader);
			theWing.calculateGeometry(ComponentEnum.WING, true);
			theWing.populateAirfoilList(false);
			
			theFuelTank = new FuelTank("My Fuel Tank", theWing);
										
			theWing.setXApexConstructionAxes(Amount.valueOf(11.0, SI.METER));
			theWing.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
			theWing.setZApexConstructionAxes(Amount.valueOf(1.6, SI.METER));
			theFuelTank.calculateCG();
			
			System.out.println("The wing ...");
			System.out.println(FuelTankTest.theWing.toString());
			System.out.println("Details on panel discretization ...");
			FuelTankTest.theWing.reportPanelsToSpanwiseDiscretizedVariables();
			
			System.out.println("The fuel tank ...");
			System.out.println(FuelTankTest.theFuelTank.toString());
			
		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			FuelTankTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ...
		launch(args);
	}

}