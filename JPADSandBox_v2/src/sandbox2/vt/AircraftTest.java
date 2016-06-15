package sandbox2.vt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.treez.javafxd3.d3.svg.SymbolType;
import org.treez.javafxd3.javafx.JavaFxD3Browser;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LiftingSurface.LiftingSurfaceBuilder;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sandbox2.javafx.D3Plotter;
import sandbox2.javafx.D3PlotterOptions;
import standaloneutils.JPADXmlReader;
import writers.JPADStaticWriteUtils;

class MyArgumentsAircraft {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	@Option(name = "-da", aliases = { "--dir-airfoils" }, required = true,
			usage = "airfoil directory path")
	private File _airfoilDirectory;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}

	public File getAirfoilDirectory() {
		return _airfoilDirectory;
	}

}

public class AircraftTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static Aircraft theAircraft;

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
		System.out.println("function start :: getting the aircaft object ...");

		LiftingSurface wing = AircraftTest.theAircraft.getWing();
		if (wing == null) {
			System.out.println("aircraft object null, returning.");
			return;
		}

		Fuselage fuselage = AircraftTest.theAircraft.getFuselage();
		if (fuselage == null) {
			System.out.println("fuselage object null, returning.");
			return;
		}

		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------

		// left curve, upperview
		List<Amount<Length>> vX1 = fuselage.getFuselageCreator().getOutlineXYSideLCurveAmountX();
		int nX1 = vX1.size();
		List<Amount<Length>> vY1 = fuselage.getFuselageCreator().getOutlineXYSideLCurveAmountY();

		Double[][] dataOutlineXYLeftCurve = new Double[nX1][2];
		IntStream.range(0, nX1)
		.forEach(i -> {
			dataOutlineXYLeftCurve[i][1] = vX1.get(i).doubleValue(SI.METRE);
			dataOutlineXYLeftCurve[i][0] = vY1.get(i).doubleValue(SI.METRE);
		});

		// right curve, upperview
		List<Amount<Length>> vX2 = fuselage.getFuselageCreator().getOutlineXYSideRCurveAmountX();
		int nX2 = vX2.size();
		List<Amount<Length>> vY2 = fuselage.getFuselageCreator().getOutlineXYSideRCurveAmountY();

		Double[][] dataOutlineXYRightCurve = new Double[nX2][2];
		IntStream.range(0, nX2)
		.forEach(i -> {
			dataOutlineXYRightCurve[i][1] = vX2.get(i).doubleValue(SI.METRE);
			dataOutlineXYRightCurve[i][0] = vY2.get(i).doubleValue(SI.METRE);
		});

		System.out.println("\n\n##################\n\n");

		System.out.println("No. points in curves: " + nX1 +", "+ nX2);
		
		System.out.println("\n\n##################\n\n");

		//--------------------------------------------------
		// get data vectors from wing discretization
		List<Amount<Length>> vY = wing.getLiftingSurfaceCreator().getDiscretizedYs();
		int nY = vY.size();
		List<Amount<Length>> vChords = wing.getLiftingSurfaceCreator().getDiscretizedChords();
		List<Amount<Length>> vXle = wing.getLiftingSurfaceCreator().getDiscretizedXle();

//		List<Amount<Length>> vXleBRF = new ArrayList<Amount<Length>>();
//		for(Amount<Length> x : vXle)
//			vXleBRF.add(x.plus(wing.getXApexConstructionAxes()));
		
		Double[][] dataChordsVsY = new Double[nY][2];
		Double[][] dataXleVsY = new Double[nY][2];
		IntStream.range(0, nY)
		.forEach(i -> {
			dataChordsVsY[i][0] = vY.get(i).doubleValue(SI.METRE);
			dataChordsVsY[i][1] = vChords.get(i).doubleValue(SI.METRE);
			dataXleVsY[i][0] = vY.get(i).doubleValue(SI.METRE);
//			dataXleVsY[i][1] = vXleBRF.get(i).doubleValue(SI.METRE);
			dataXleVsY[i][1] = vXle.get(i).doubleValue(SI.METRE);
		});

		System.out.println("##################\n\n");

		Double[][] dataTopView = wing.getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.WING);

		//--------------------------------------------------
		System.out.println("Initializing test class...");
		String rootOutputFolderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String outputFolderPath = JPADStaticWriteUtils.createNewFolder(rootOutputFolderPath + "Test_D3_WingBody" + File.separator);
		System.out.println("Output ==> " + outputFolderPath);

		//--------------------------------------------------
		//set state title
		primaryStage.setTitle("treez/javafx-d3 - D3Plotter demo");

		List<Double[][]> listDataArray = new ArrayList<Double[][]>();

		listDataArray.add(dataOutlineXYLeftCurve);
		listDataArray.add(dataOutlineXYRightCurve);
		listDataArray.add(dataTopView);

//		int nSec = wing.getLiftingSurfaceCreator().getDiscretizedXle().size();
//		int nPanels = wing.getLiftingSurfaceCreator().getPanels().size();
//
//		Double[][] eqPts = new Double[4][2];
//		eqPts[0][0] = 0.0;
//		eqPts[0][1] = wing.getLiftingSurfaceCreator().getXOffsetEquivalentWingRootLE().doubleValue(SI.METER);
//		eqPts[1][0] = wing.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER);
//		eqPts[1][1] = wing.getLiftingSurfaceCreator().getDiscretizedXle().get(nSec - 1).doubleValue(SI.METER);
//		eqPts[2][0] = wing.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER);
//		eqPts[2][1] = wing.getLiftingSurfaceCreator().getDiscretizedXle().get(nSec - 1)
//				.plus(
//						wing.getLiftingSurfaceCreator().getPanels().get(nPanels - 1).getChordTip()
//						)
//				.doubleValue(SI.METER);
//		eqPts[3][0] = 0.0;
//		eqPts[3][1] = wing.getLiftingSurfaceCreator().getPanels().get(0).getChordRoot()
//				.minus(wing.getLiftingSurfaceCreator().getXOffsetEquivalentWingRootTE())
//				.doubleValue(SI.METER);
//
//		listDataArray.add(eqPts);
		
		Double[][] xyMAC = new Double[2][2];
		xyMAC[0][0] = wing.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE);
		xyMAC[0][1] = wing.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE);
		xyMAC[1][0] = xyMAC[0][0];
		xyMAC[1][1] = xyMAC[0][1] + wing.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METRE);

		listDataArray.add(xyMAC);
		
		double xMax = 1.20*fuselage.getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double xMin = -0.20*fuselage.getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMax = xMax;
		double yMin = xMin;
		
		System.out.println("The aircraft ...");
		System.out.println(wing);
		System.out.println("Details on panel discretization ...");
		wing.getLiftingSurfaceCreator().reportPanelsToSpanwiseDiscretizedVariables();

		D3PlotterOptions options = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(xMin, xMax)
				.yRange(yMax, yMin)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.1)
				.title("Wing data representation")
				.xLabel("x (m)")
				.yLabel("y (m)")
				.showXGrid(true)
				.showYGrid(true)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE						
						)
				.symbolSizes(20,20,20,20,20)
				.showSymbols(true,true,true,true,true) // NOTE: overloaded function
				.symbolStyles(
						"fill:blue; stroke:red; stroke-width:2",
						"fill:cyan; stroke:green; stroke-width:2",
						"fill:cyan; stroke:black; stroke-width:3",
						"fill:cyan; stroke:blue; stroke-width:3",
						"fill:cyan; stroke:purple; stroke-width:3"
						)
				.lineStyles(
						"fill:none; stroke:darkblue; stroke-width:3",
						"fill:none; stroke:darkblue; stroke-width:2",
						"fill:none; stroke:darkblue; stroke-width:2",
						"fill:none; stroke:darkblue; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2"
						)
				.plotAreas(true,true,true,true,false)
				.areaStyles("fill:orange;","fill:yellow;","fill:yellow;","fill:orange;","fill:orange;")
				.areaOpacities(1.0,1.0,1.0,0.7,0.5)
				.build();

		System.out.println("Plot options:\n" + options);

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
			String outputFilePath = outputFolderPath + "test6.svg";
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
		System.out.println("Aircraft test / D3");
		System.out.println("--------------");

		MyArgumentsAircraft va = new MyArgumentsAircraft();
		AircraftTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			AircraftTest.theCmdLineParser.parseArgument(args);
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
			AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
			
			// read LiftingSurface from xml ...
			theAircraft = Aircraft.createDefaultAircraft(AircraftEnum.ATR72, aeroDatabaseReader);

			System.out.println("The Aircaraft ...");
			
//			TODO: ADD Builder pattern to Aircraft
//			System.out.println(AircraftTest.theAircraft.toString());
			
		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			AircraftTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ...
		launch(args);
	}

}