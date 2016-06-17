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

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
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
			System.out.println("wing object null, returning.");
			return;
		}

		LiftingSurface hTail = AircraftTest.theAircraft.getHTail();
		if (hTail == null) {
			System.out.println("horizontal tail object null, returning.");
			return;
		}
		
		LiftingSurface vTail = AircraftTest.theAircraft.getVTail();
		if (vTail == null) {
			System.out.println("vertical tail object null, returning.");
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

		Double[][] dataTopViewIsolated = wing.getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		
		Double[][] dataTopView = new Double[dataTopViewIsolated.length][dataTopViewIsolated[0].length];
		for (int i=0; i<dataTopViewIsolated.length; i++) { 
			dataTopView[i][0] = dataTopViewIsolated[i][0];
			dataTopView[i][1] = dataTopViewIsolated[i][1] + wing.getXApexConstructionAxes().doubleValue(SI.METER);
	}
		
		Double[][] dataTopViewMirrored = new Double[dataTopView.length][dataTopView[0].length];
		for (int i=0; i<dataTopView.length; i++) { 
				dataTopViewMirrored[i][0] = -dataTopView[i][0];
				dataTopViewMirrored[i][1] = dataTopView[i][1];
		}

		//--------------------------------------------------
		// get data vectors from hTail discretization
		List<Amount<Length>> vYHTail = hTail.getLiftingSurfaceCreator().getDiscretizedYs();
		int nYHTail = vYHTail.size();
		List<Amount<Length>> vChordsHTail = hTail.getLiftingSurfaceCreator().getDiscretizedChords();
		List<Amount<Length>> vXleHTail = hTail.getLiftingSurfaceCreator().getDiscretizedXle();

		Double[][] dataChordsVsYHTail = new Double[nYHTail][2];
		Double[][] dataXleVsYHTail = new Double[nYHTail][2];
		IntStream.range(0, nYHTail)
		.forEach(i -> {
			dataChordsVsYHTail[i][0] = vYHTail.get(i).doubleValue(SI.METRE);
			dataChordsVsYHTail[i][1] = vChordsHTail.get(i).doubleValue(SI.METRE);
			dataXleVsYHTail[i][0] = vYHTail.get(i).doubleValue(SI.METRE);
			dataXleVsYHTail[i][1] = vXleHTail.get(i).doubleValue(SI.METRE);
		});

		System.out.println("##################\n\n");

		Double[][] dataTopViewIsolatedHTail = hTail.getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.WING);

		Double[][] dataTopViewHTail = new Double[dataTopViewIsolatedHTail.length][dataTopViewIsolatedHTail[0].length];
		for (int i=0; i<dataTopViewIsolatedHTail.length; i++) { 
			dataTopViewHTail[i][0] = dataTopViewIsolatedHTail[i][0];
			dataTopViewHTail[i][1] = dataTopViewIsolatedHTail[i][1] + hTail.getXApexConstructionAxes().doubleValue(SI.METER);
		}

		Double[][] dataTopViewMirroredHTail = new Double[dataTopViewHTail.length][dataTopViewHTail[0].length];
		for (int i=0; i<dataTopViewHTail.length; i++) { 
			dataTopViewMirroredHTail[i][0] = -dataTopViewHTail[i][0];
			dataTopViewMirroredHTail[i][1] = dataTopViewHTail[i][1];
		}

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
		listDataArray.add(dataTopViewMirrored);
		listDataArray.add(dataTopViewHTail);
		listDataArray.add(dataTopViewMirroredHTail);

		Double[][] xyMAC = new Double[2][2];
		xyMAC[0][0] = wing.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE);
		xyMAC[0][1] = wing.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE);
		xyMAC[1][0] = xyMAC[0][0];
		xyMAC[1][1] = xyMAC[0][1] + wing.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METRE);

		listDataArray.add(xyMAC);
		
		double xMax = 1.20*wing.getSemiSpan().doubleValue(SI.METER);
		double xMin = -1.20*wing.getSemiSpan().doubleValue(SI.METER);;
		double yMax = 1.20*fuselage.getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMin = -0.20*fuselage.getFuselageCreator().getLenF().doubleValue(SI.METRE);
		
		System.out.println("The aircraft ...");
		System.out.println(wing);
		System.out.println("Details on panel discretization ...");
		wing.getLiftingSurfaceCreator().reportPanelsToSpanwiseDiscretizedVariables();

		D3PlotterOptions options = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(xMin, xMax)
				.yRange(yMax, yMin)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
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
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				.symbolSizes(5,5,5,5,5,5,5)
				.showSymbols(true,true,true,true,true,true,true) // NOTE: overloaded function
				.symbolStyles(
						"fill:blue; stroke:darkblue; stroke-width:3",
						"fill:cyan; stroke:darkblue; stroke-width:3",
						"fill:cyan; stroke:darkblue; stroke-width:3",
						"fill:cyan; stroke:darkblue; stroke-width:3",
						"fill:cyan; stroke:darkblue; stroke-width:3",
						"fill:cyan; stroke:darkblue; stroke-width:3",
						"fill:cyan; stroke:darkblue; stroke-width:3"
						)
				.lineStyles(
						"fill:none; stroke:darkblue; stroke-width:3",
						"fill:none; stroke:darkblue; stroke-width:2",
						"fill:none; stroke:darkblue; stroke-width:2",
						"fill:none; stroke:darkblue; stroke-width:2",
						"fill:none; stroke:darkblue; stroke-width:2",
						"fill:none; stroke:darkblue; stroke-width:2",
						"fill:none; stroke:darkblue; stroke-width:2"
						)
				.plotAreas(true,true,true,true,true,true)
				.areaStyles("fill:white;","fill:white;","fill:lightblue;","fill:lightblue;","fill:lightblue;","fill:lightblue;","fill:orange;")
				.areaOpacities(1.0,1.0,1.0,1.0,1.0,1.0,1.0)
				.showLegend(false)
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
			theAircraft = new Aircraft.AircraftBuilder("ATR-72", AircraftEnum.ATR72, aeroDatabaseReader).build();

			System.out.println("The Aircaraft ...");
			System.out.println(AircraftTest.theAircraft.toString());
			
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