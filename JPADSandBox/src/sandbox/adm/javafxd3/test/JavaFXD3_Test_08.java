package sandbox.adm.javafxd3.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.treez.javafxd3.d3.svg.SymbolType;
import org.treez.javafxd3.javafx.JavaFxD3Browser;

import aircraft.components.liftingSurface.adm.LiftingSurface;
import configuration.MyConfiguration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sandbox.adm.D3PlotterOptions;
import standaloneutils.JPADXmlReader;
import writers.JPADStaticWriteUtils;

public class JavaFXD3_Test_08 extends Application {

	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	@Option(name = "-da", aliases = { "--dir-airfoils" }, required = true,
			usage = "airfoil directory path")
	private File _airfoilDirectory;

	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	//-------------------------------------------------------------
	private LiftingSurface theWing;
	
	public LiftingSurface getTheWing() {
		return theWing;
	}

	public void setTheWing(LiftingSurface wing) {
		this.theWing = wing;
	}
	//-------------------------------------------------------------

	private D3Plotter d3Plotter;

	private final int WIDTH = 700;
	private final int HEIGHT = 600;
	
	private static final double DELTA = 0.001d;

	@Override
	public void start(Stage primaryStage) throws Exception {

		//--------------------------------------------------
		// get the wing object
		System.out.println("\n\n##################");
		System.out.println("function start :: getting the wing object ...");

		LiftingSurface wing = this.getTheWing();
		if (wing == null) {
			System.out.println("wing object null, returning.");
			return;
		}
		
		System.out.println("The wing ...");
		System.out.println(wing);
		System.out.println("Details on panel discretization ...");
		wing.reportPanelsToSpanwiseDiscretizedVariables();
		
		System.out.println("##################\n\n");
		
		
		//--------------------------------------------------
		System.out.println("Initializing test class...");
		String rootOutputFolderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String outputFolderPath = JPADStaticWriteUtils.createNewFolder(rootOutputFolderPath + "Test_D3" + File.separator);
		System.out.println("Output ==> " + outputFolderPath);

		//--------------------------------------------------
		//set state title
		primaryStage.setTitle("treez/javafx-d3 - D3Plotter demo");

		List<Double[][]> listDataArray = new ArrayList<Double[][]>();
		
		// the data we want to plot [x,y]
		Double[][] dataArray1 = {
				{ 0.0, 0.0 },
				{ 20.0, 15.5 },
				{ 50.0, 10.0 },
				{ 40.0, -10.0 },
				{ 35.0, 18.0 }
				};

		Double[][] dataArray2 = {
				{ 5.0, 0.0 },
				{ 10.0, -5.5 },
				{ 50.0, -10.0 },
				{ 40.0, 0.0 },
				{ 35.0, -8.0 }
				};
		
		listDataArray.add(dataArray1);
		listDataArray.add(dataArray2);
		
		D3PlotterOptions options = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(-1.0, 60.0).yRange(-50.0, 60.0)
				.axisLineColor("magenta").axisLineStrokeWidth("5px")
				.graphBackgroundColor("yellow").graphBackgroundOpacity(0.2)
//				.symbolType(SymbolType.TRIANGLE_UP)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.TRIANGLE_UP
						)
//				.symbolStyle("fill:yellow; stroke:green; stroke-width:2")
				.symbolStyles(
						"fill:cyan; stroke:green; stroke-width:2",
						"fill:blue; stroke:red; stroke-width:2"
						)
//				.lineStyle(
//						"fill:none; stroke:orange; stroke-dasharray: 15px, 8px; stroke-width:2"
//						)
				.lineStyles(
						"fill:none; stroke:red; stroke-width:2",
						"fill:none; stroke:magenta; stroke-dasharray: 15px, 2px; stroke-width:2"
						)
//				.plotArea(true)
				.plotAreas(false,true)
				.areaStyle("fill:orange;")
				// TODO
//				.areaStyles("fill:orange;","fill:yellow;")
				.areaOpacity(0.7)
				// TODO
//				.areaOpacities(0.7,0.5)
				//.legendItems("Pippo1", "agodemar2", "crocco3")
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
	
	public JavaFXD3_Test_08() {
		theCmdLineParser = new CmdLineParser(this);
	}
	public File getInputFile() {
		return _inputFile;
	}

	public File getAirfoilDirectory() {
		return _airfoilDirectory;
	}

	/**
	 * Main
	 *
	 * @param args
	 */
	public static void main(String[] args) throws CmdLineException, IOException {
		
		System.out.println("--------------");
		System.out.println("Wing test / D3");
		System.out.println("--------------");

		JavaFXD3_Test_08 theTestObject = new JavaFXD3_Test_08();

		theTestObject.theCmdLineParser.parseArgument(args);

		String pathToXML = theTestObject.getInputFile().getAbsolutePath();
		System.out.println("INPUT ===> " + pathToXML);

		String dirAirfoil = theTestObject.getAirfoilDirectory().getCanonicalPath();
		System.out.println("AIRFOILS ===> " + dirAirfoil);

		System.out.println("--------------");

		LiftingSurface wing = LiftingSurface.importFromXML(pathToXML, dirAirfoil);
		wing.calculateGeometry(30);
		
		// pass the wing to the test object
		theTestObject.setTheWing(wing);

//		System.out.println("The wing ...");
//		System.out.println(wing);
//		System.out.println("Details on panel discretization ...");
//		wing.reportPanelsToSpanwiseDiscretizedVariables();
		
		// JavaFX ...
		launch(args);
	}
	
}
