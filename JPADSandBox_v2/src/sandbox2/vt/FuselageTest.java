package sandbox2.vt;

import java.io.File;
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

import aircraft.components.fuselage.Fuselage;
import aircraft.components.fuselage.creator.FuselageCreator;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sandbox2.javafx.D3Plotter;
import sandbox2.javafx.D3PlotterOptions;
import standaloneutils.JPADXmlReader;
import writers.JPADStaticWriteUtils;

class MyArgumentsFuselage {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}

}

public class FuselageTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static Fuselage theFuselage;

	//-------------------------------------------------------------

	private D3Plotter d3Plotter;

	private final int WIDTH = 900;
	private final int HEIGHT = 800;

	@Override
	public void start(Stage primaryStage) throws Exception {

		//--------------------------------------------------
		// get the wing object
		System.out.println("\n\n##################");
		System.out.println("function start :: getting the fuselage object ...");

		Fuselage fuselage = FuselageTest.theFuselage;
		if (fuselage == null) {
			System.out.println("fuselage object null, returning.");
			return;
		}

		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------

		// upper curve, sideview
		List<Amount<Length>> vX1 = fuselage.getFuselageCreator().getOutlineXZUpperCurveAmountX();
		int nX1 = vX1.size();
		List<Amount<Length>> vZ1 = fuselage.getFuselageCreator().getOutlineXZUpperCurveAmountZ();

		Double[][] dataOutlineXZUpperCurve = new Double[nX1][2];
		IntStream.range(0, nX1)
		.forEach(i -> {
			dataOutlineXZUpperCurve[i][0] = vX1.get(i).doubleValue(SI.METRE);
			dataOutlineXZUpperCurve[i][1] = vZ1.get(i).doubleValue(SI.METRE);
		});

		// lower curve, sideview
		List<Amount<Length>> vX2 = fuselage.getFuselageCreator().getOutlineXZLowerCurveAmountX();
		int nX2 = vX2.size();
		List<Amount<Length>> vZ2 = fuselage.getFuselageCreator().getOutlineXZLowerCurveAmountZ();

		Double[][] dataOutlineXZLowerCurve = new Double[nX2][2];
		IntStream.range(0, nX2)
		.forEach(i -> {
			dataOutlineXZLowerCurve[i][0] = vX2.get(i).doubleValue(SI.METRE);
			dataOutlineXZLowerCurve[i][1] = vZ2.get(i).doubleValue(SI.METRE);
		});

		// camberline, sideview
		List<Amount<Length>> vX3 = fuselage.getFuselageCreator().getOutlineXZCamberLineAmountX();
		int nX3 = vX3.size();
		List<Amount<Length>> vZ3 = fuselage.getFuselageCreator().getOutlineXZCamberLineAmountZ();

		Double[][] dataOutlineXZCamberLine = new Double[nX3][2];
		IntStream.range(0, nX3)
		.forEach(i -> {
			dataOutlineXZCamberLine[i][0] = vX3.get(i).doubleValue(SI.METRE);
			dataOutlineXZCamberLine[i][1] = vZ3.get(i).doubleValue(SI.METRE);
		});

		System.out.println("\n\n##################\n\n");

		System.out.println("No. points in curves: " + nX1 +", "+ nX2 + ", " + nX3);

		System.out.println("\n\n##################\n\n");

		//--------------------------------------------------
		System.out.println("Initializing test class...");
		String rootOutputFolderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String outputFolderPath = JPADStaticWriteUtils.createNewFolder(rootOutputFolderPath + "Tests_Aircraft" + File.separator);
		System.out.println("Output ==> " + outputFolderPath);

		//--------------------------------------------------
		//set state title
		primaryStage.setTitle("treez/javafx-d3 - D3Plotter demo");

		List<Double[][]> listDataArray = new ArrayList<Double[][]>();

		listDataArray.add(dataOutlineXZUpperCurve);
		listDataArray.add(dataOutlineXZLowerCurve);
		listDataArray.add(dataOutlineXZCamberLine);

		double xMax = 1.20*fuselage.getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double xMin = -0.20*fuselage.getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMax = 15;
		double yMin = -15;

		D3PlotterOptions options = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthSVG(1*WIDTH).heightSVG(1*HEIGHT)
				.xRange(xMin, xMax)
				.yRange(yMin, yMax)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
				.title("Fuselage representation")
				.xLabel("x (m)")
				.yLabel("z (m)")
				.showXGrid(true)
				.showYGrid(true)
				//				.symbolType(SymbolType.CIRCLE)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				//				.symbolSize(20)
				.symbolSizes(10,10,10)
				.showSymbols(true,true,true) // NOTE: overloaded function
				//				.symbolStyle("fill:yellow; stroke:green; stroke-width:2")
				.symbolStyles(
						"fill:blue; stroke:red; stroke-width:1",
						"fill:cyan; stroke:green; stroke-width:1",
						"fill:cyan; stroke:black; stroke-width:1"
						)
				.lineStyles(
						"fill:none; stroke:darkblue; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:magenta; stroke-dasharray: 15px, 2px; stroke-width:1"
						)
				.plotArea(false)
				.areaStyles("fill:orange;","fill:yellow;")
				.areaOpacities(0.50,0.70)
				.build();

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
			String outputFilePath = outputFolderPath + "Fuselage.svg";
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

		System.out.println("----------------------");
		System.out.println("Fuselage test / D3");
		System.out.println("----------------------");

		MyArgumentsFuselage va = new MyArgumentsFuselage();
		FuselageTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			FuselageTest.theCmdLineParser.parseArgument(args);
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToXML);

			System.out.println("--------------");

			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String fusDesDatabaseFilename = "FusDes_database.h5";
			FusDesDatabaseReader fusDesDatabaseReader = new FusDesDatabaseReader(databaseFolderPath, fusDesDatabaseFilename);
			
			// This wing static object is available in the scope of
			// the Application.start method

			//				// Read fuselage from xml ...
			//				theFuselage = new FuselageBuilder("MyFuselage", ComponentEnum.FUSELAGE)
			//						.fuselageCreator(
			//								FuselageCreator.importFromXML(pathToXML)
			//								)
			//						.build();

			// default Fuselage ...
			theFuselage = new Fuselage.FuselageBuilder("MyFuselage", fusDesDatabaseReader)
					.fuselageCreator(
							new FuselageCreator
							.FuselageBuilder("Test ATR72 fuselage", AircraftEnum.ATR72)
							.build()
							)
					.build();

			//				FuselageTest.theFuselage.getFuselageCreator().calculateGeometry(
			//						20,    // No. points in nose trunk
			//						5,     // No. points in cylindrical trunk
			//						15,    // No. points in tail trunk
			//						10, 10 // No. points in upper/lower cyl. trunk section
			//						);

			System.out.println("The fuselage ...");
			System.out.println(FuselageTest.theFuselage.getFuselageCreator().toString());
			System.out.println("Details on discretization ...");

		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			FuselageTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ...
		launch(args);
	}
}
