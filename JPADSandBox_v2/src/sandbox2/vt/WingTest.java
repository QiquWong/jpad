package sandbox2.vt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LiftingSurface.LiftingSurfaceBuilder;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import configuration.MyConfiguration;
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

class MyArgumentWing {
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

public class WingTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static LiftingSurface theWing;

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

		LiftingSurface wing = WingTest.theWing;
		if (wing == null) {
			System.out.println("wing object null, returning.");
			return;
		}

		System.out.println("The wing ...");
		System.out.println(wing);
		System.out.println("Details on panel discretization ...");
		wing.getLiftingSurfaceCreator().reportPanelsToSpanwiseDiscretizedVariables();

		//--------------------------------------------------
		// get data vectors from wing discretization
		List<Amount<Length>> vY = wing.getLiftingSurfaceCreator().getDiscretizedYs();
		int nY = vY.size();
		List<Amount<Length>> vChords = wing.getLiftingSurfaceCreator().getDiscretizedChords();
		List<Amount<Length>> vXle = wing.getLiftingSurfaceCreator().getDiscretizedXle();

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

		Double[][] dataTopView = wing.getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.WING);

		//--------------------------------------------------
		System.out.println("Initializing test class...");
		String rootOutputFolderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String outputFolderPath = JPADStaticWriteUtils.createNewFolder(rootOutputFolderPath + "Test_D3" + File.separator);
		System.out.println("Output ==> " + outputFolderPath);

		//--------------------------------------------------
		//set state title
		primaryStage.setTitle("treez/javafx-d3 - D3Plotter demo");

		List<Double[][]> listDataArray = new ArrayList<Double[][]>();

		listDataArray.add(dataTopView);

		int nSec = theWing.getLiftingSurfaceCreator().getDiscretizedXle().size();
		int nPanels = theWing.getLiftingSurfaceCreator().getPanels().size();

		Double[][] eqPts = new Double[4][2];
		eqPts[0][0] = 0.0;
		eqPts[0][1] = theWing.getLiftingSurfaceCreator().getXOffsetEquivalentWingRootLE().doubleValue(SI.METER);
		eqPts[1][0] = theWing.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER);
		eqPts[1][1] = theWing.getLiftingSurfaceCreator().getDiscretizedXle().get(nSec - 1).doubleValue(SI.METER);
		eqPts[2][0] = theWing.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER);
		eqPts[2][1] = theWing.getLiftingSurfaceCreator().getDiscretizedXle().get(nSec - 1)
				.plus(
						theWing.getLiftingSurfaceCreator().getPanels().get(nPanels - 1).getChordTip()
						)
				.doubleValue(SI.METER);
		eqPts[3][0] = 0.0;
		eqPts[3][1] = theWing.getLiftingSurfaceCreator().getPanels().get(0).getChordRoot()
				.minus(theWing.getLiftingSurfaceCreator().getXOffsetEquivalentWingRootTE())
				.doubleValue(SI.METER);

		listDataArray.add(eqPts);

		Double[][] xyMAC = new Double[2][2];
		xyMAC[0][0] = wing.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE);
		xyMAC[0][1] = wing.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE);
		xyMAC[1][0] = xyMAC[0][0];
		xyMAC[1][1] = xyMAC[0][1] + wing.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METRE);

		listDataArray.add(xyMAC);

		double yMax = 1.05*wing.getSemiSpan().doubleValue(SI.METRE);
		double yMin = -0.05*wing.getSemiSpan().doubleValue(SI.METRE);
		double xMax = yMax;
		double xMin = yMin;

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
						SymbolType.CIRCLE
						)
				.symbolSizes(20,20,20)
				.showSymbols(true,true,true) // NOTE: overloaded function
				.symbolStyles(
						"fill:blue; stroke:red; stroke-width:2",
						"fill:cyan; stroke:green; stroke-width:2",
						"fill:cyan; stroke:black; stroke-width:3"
						)
				.lineStyles(
						"fill:none; stroke:darkblue; stroke-width:3",
						"fill:none; stroke:darkblue; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2"
						)
				.plotAreas(true,true,false,false)
				.areaStyles("fill:orange;","fill:yellow;","fill:yellow;")
				.areaOpacities(1.0,0.50,0.70)
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
		System.out.println("Wing test / D3");
		System.out.println("--------------");

		MyArgumentWing va = new MyArgumentWing();
		WingTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			WingTest.theCmdLineParser.parseArgument(args);
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
			theWing = new LiftingSurfaceBuilder("MyWing", ComponentEnum.WING, aeroDatabaseReader)
					.liftingSurfaceCreator(
							LiftingSurfaceCreator.importFromXML(ComponentEnum.WING, pathToXML, dirAirfoil)
							)
					.build();
//			
//	  TODO: //////////////////////////////////////////////////////////////////
//			// EQUIVALENT WING ON ATR-72 DO NOT CALCULATE THE TWO INTEGRALS	//
//			// BECAUSE DISCRETIZED y STATIONS AND xLE ARE NOT STRICTLY 		//
//			// MONOTONIC													//
//			//////////////////////////////////////////////////////////////////
//			
//			// default LiftingSurface from xml ...
//			theWing = new LiftingSurfaceBuilder("MyWing", ComponentEnum.WING, aeroDatabaseReader)
//					.liftingSurfaceCreator(
//							new LiftingSurfaceCreator
//							.LiftingSurfaceCreatorBuilder(
//									"MyWing",
//									Boolean.TRUE,
//									AircraftEnum.ATR72,
//									ComponentEnum.WING
//									)
//							.build()
//							)
//					.build();

			WingTest.theWing.calculateGeometry(
					40,
					theWing.getType(),
					theWing.getLiftingSurfaceCreator().isMirrored());

			System.out.println("The wing ...");
			System.out.println(WingTest.theWing.getLiftingSurfaceCreator().toString());
			System.out.println("Details on panel discretization ...");
			WingTest.theWing.getLiftingSurfaceCreator().reportPanelsToSpanwiseDiscretizedVariables();

		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			WingTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ...
		launch(args);
	}

}