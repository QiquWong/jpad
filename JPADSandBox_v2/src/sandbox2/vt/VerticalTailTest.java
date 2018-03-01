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
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
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

class MyArgumentVTail {
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

public class VerticalTailTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static LiftingSurface theVerticalTail;

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
		System.out.println("function start :: getting the vertical tail object ...");

		LiftingSurface vTail = VerticalTailTest.theVerticalTail;
		if (vTail == null) {
			System.out.println("vertical tail object null, returning.");
			return;
		}

		System.out.println("The vertical tail ...");
		System.out.println(vTail);
		System.out.println("Details on panel discretization ...");
		vTail.getLiftingSurfaceCreator().reportPanelsToSpanwiseDiscretizedVariables();

		//--------------------------------------------------
		// get data vectors from wing discretization
		List<Amount<Length>> vY = vTail.getLiftingSurfaceCreator().getDiscretizedYs();
		int nY = vY.size();
		List<Amount<Length>> vChords = vTail.getLiftingSurfaceCreator().getDiscretizedChords();
		List<Amount<Length>> vXle = vTail.getLiftingSurfaceCreator().getDiscretizedXle();

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

		Double[][] dataTopView = vTail.getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);

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

		Double[][] xyMAC = new Double[2][2];
		xyMAC[0][0] = vTail.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE);
		xyMAC[0][1] = vTail.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE);
		xyMAC[1][0] = xyMAC[0][0] + vTail.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METRE);
		xyMAC[1][1] = xyMAC[0][1] ;

		listDataArray.add(xyMAC);

		double yMax = 1.25*vTail.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METRE);
		double yMin = -0.05*vTail.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METRE);
		double xMax = yMax;
		double xMin = yMin;

		D3PlotterOptions options = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(xMin, xMax)
				.yRange(yMin, yMax)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.1)
				.title("Vertical tail data representation")
				.xLabel("x (m)")
				.yLabel("z (m)")
				.showXGrid(true)
				.showYGrid(true)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				.symbolSizes(20,20)
				.showSymbols(true,true) // NOTE: overloaded function
				.symbolStyles(
						"fill:blue; stroke:red; stroke-width:2",
						"fill:cyan; stroke:green; stroke-width:2"
						)
				.lineStyles(
						"fill:none; stroke:darkblue; stroke-width:3",
						"fill:none; stroke:darkblue; stroke-width:2"
						)
				.plotAreas(true,false)
				.areaStyles("fill:orange;","fill:yellow;")
				.areaOpacities(1.0,0.50)
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
			String outputFilePath = outputFolderPath + "VerticalTail.svg";
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
		System.out.println("Vertical Tail test / D3");
		System.out.println("--------------");

		MyArgumentVTail va = new MyArgumentVTail();
		VerticalTailTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			VerticalTailTest.theCmdLineParser.parseArgument(args);
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
			
			// read LiftingSurface from xml ...
			theVerticalTail = new LiftingSurface(LiftingSurfaceCreator.importFromXML(ComponentEnum.VERTICAL_TAIL, pathToXML, dirAirfoil));
			theVerticalTail.setAeroDatabaseReader(aeroDatabaseReader);
			theVerticalTail.setHighLiftDatabaseReader(highLiftDatabaseReader);
			theVerticalTail.setVeDSCDatabaseReader(veDSCDatabaseReader);

			VerticalTailTest.theVerticalTail.getLiftingSurfaceCreator().calculateGeometry(
					40,
					theVerticalTail.getLiftingSurfaceCreator().getType(),
					theVerticalTail.getLiftingSurfaceCreator().isMirrored()
					);

			System.out.println("The vertical tail ...");
			System.out.println(VerticalTailTest.theVerticalTail.getLiftingSurfaceCreator().toString());
			System.out.println("Details on panel discretization ...");
			VerticalTailTest.theVerticalTail.getLiftingSurfaceCreator().reportPanelsToSpanwiseDiscretizedVariables();

		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			HorizontalTailTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ...
		launch(args);
	}

}