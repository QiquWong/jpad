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
import configuration.enumerations.WingAdjustCriteriaEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sandbox2.javafx.D3Plotter;
import sandbox2.javafx.D3PlotterOptions;
import standaloneutils.JPADXmlReader;
import writers.JPADStaticWriteUtils;

class MyArgumentWingAdjustTest {
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

public class WingAdjustTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static LiftingSurface theWing1;
	public static LiftingSurface theWing2;

	//-------------------------------------------------------------

	private D3Plotter d3Plotter1;
	private D3Plotter d3Plotter2;

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

		LiftingSurface wing1 = WingAdjustTest.theWing1;
		if (wing1 == null) {
			System.out.println("wing1 object null, returning.");
			return;
		}
		LiftingSurface wing2 = WingAdjustTest.theWing2;
		if (wing2 == null) {
			System.out.println("wing1 object null, returning.");
			return;
		}
//		wing2.getLiftingSurfaceCreator().adjustDimensions(
//				wing1.getLiftingSurfaceCreator()
//							.getAspectRatio()*1.2, // <==== ADJUST AR 
//				wing1.getLiftingSurfaceCreator()
//							.getSpan().doubleValue(SI.METER),
//				wing1.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
//							.getChordRoot().doubleValue(SI.METER),
//				wing1.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
//							.getSweepLeadingEdge().times(2),
//				wing1.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
//							.getDihedral(), 
//				wing1.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
//							.getTwistGeometricAtTip(),
//				WingAdjustCriteriaEnum.AR_SPAN_ROOTCHORD
//				);
		wing2.getLiftingSurfaceCreator().adjustDimensions(
				wing1.getLiftingSurfaceCreator()
							.getSurfacePlanform().doubleValue(SI.SQUARE_METRE)*1.2, // <==== ADJUST AR 
				wing1.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
							.getChordRoot().doubleValue(SI.METER)*1.2,
				wing1.getLiftingSurfaceCreator().getEquivalentWing().getPanels()
							.get(wing1.getLiftingSurfaceCreator().getEquivalentWing().getPanels().size()-1)
							.getChordTip().doubleValue(SI.METER),
				wing1.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
							.getSweepLeadingEdge(),
				wing1.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
							.getDihedral(), 
				wing1.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0)
							.getTwistGeometricAtTip(),
				WingAdjustCriteriaEnum.AREA_ROOTCHORD_TIPCHORD
				);
		
		System.out.println("The wing ...");
		System.out.println(wing1);
		System.out.println("Details on panel discretization ...");
		wing1.getLiftingSurfaceCreator().reportPanelsToSpanwiseDiscretizedVariables();

		//--------------------------------------------------
		// get data vectors from wing discretization (WING_1)
		List<Amount<Length>> vY1 = wing1.getLiftingSurfaceCreator().getDiscretizedYs();
		int nY1 = vY1.size();
		List<Amount<Length>> vChords1 = wing1.getLiftingSurfaceCreator().getDiscretizedChords();
		List<Amount<Length>> vXle1 = wing1.getLiftingSurfaceCreator().getDiscretizedXle();

		Double[][] dataChordsVsY1 = new Double[nY1][2];
		Double[][] dataXleVsY1 = new Double[nY1][2];
		IntStream.range(0, nY1)
		.forEach(i -> {
			dataChordsVsY1[i][0] = vY1.get(i).doubleValue(SI.METRE);
			dataChordsVsY1[i][1] = vChords1.get(i).doubleValue(SI.METRE);
			dataXleVsY1[i][0] = vY1.get(i).doubleValue(SI.METRE);
			dataXleVsY1[i][1] = vXle1.get(i).doubleValue(SI.METRE);
		});

		System.out.println("##################\n\n");

		//--------------------------------------------------
		// get data vectors from wing discretization  (WING_2)
		List<Amount<Length>> vY2 = wing2.getLiftingSurfaceCreator().getDiscretizedYs();
		int nY2 = vY2.size();
		List<Amount<Length>> vChords2 = wing2.getLiftingSurfaceCreator().getDiscretizedChords();
		List<Amount<Length>> vXle2 = wing2.getLiftingSurfaceCreator().getDiscretizedXle();

		Double[][] dataChordsVsY2 = new Double[nY2][2];
		Double[][] dataXleVsY2 = new Double[nY2][2];
		IntStream.range(0, nY2)
		.forEach(i -> {
			dataChordsVsY2[i][0] = vY2.get(i).doubleValue(SI.METRE);
			dataChordsVsY2[i][1] = vChords2.get(i).doubleValue(SI.METRE);
			dataXleVsY2[i][0] = vY2.get(i).doubleValue(SI.METRE);
			dataXleVsY2[i][1] = vXle2.get(i).doubleValue(SI.METRE);
		});

		System.out.println("##################\n\n");

		Double[][] dataTopView1 = wing1.getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		Double[][] dataTopView2 = wing2.getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		
		//--------------------------------------------------
		System.out.println("Initializing test class...");
		String rootOutputFolderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String outputFolderPath = JPADStaticWriteUtils.createNewFolder(rootOutputFolderPath + "Tests_Aircraft" + File.separator);
		System.out.println("Output ==> " + outputFolderPath);

		//--------------------------------------------------
		//set state title
		primaryStage.setTitle("treez/javafx-d3 - D3Plotter demo");

		List<Double[][]> listDataArray1 = new ArrayList<Double[][]>();

		listDataArray1.add(dataTopView1);

		int nSec1 = wing1.getLiftingSurfaceCreator().getDiscretizedXle().size();
		int nPanels1 = wing1.getLiftingSurfaceCreator().getPanels().size();

		Double[][] eqPts1 = new Double[4][2];
		eqPts1[0][0] = 0.0;
		eqPts1[0][1] = wing1.getLiftingSurfaceCreator().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordLE();
		eqPts1[1][0] = wing1.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER);
		eqPts1[1][1] = wing1.getLiftingSurfaceCreator().getDiscretizedXle().get(nSec1 - 1).doubleValue(SI.METER);
		eqPts1[2][0] = wing1.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER);
		eqPts1[2][1] = wing1.getLiftingSurfaceCreator().getDiscretizedXle().get(nSec1 - 1)
				.plus(
						wing1.getLiftingSurfaceCreator().getPanels().get(nPanels1 - 1).getChordTip()
						)
				.doubleValue(SI.METER);
		eqPts1[3][0] = 0.0;
		eqPts1[3][1] = wing1.getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
				- wing1.getLiftingSurfaceCreator().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordTE();

		listDataArray1.add(eqPts1);

		Double[][] xyMAC1 = new Double[2][2];
		xyMAC1[0][0] = wing1.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE);
		xyMAC1[0][1] = wing1.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE);
		xyMAC1[1][0] = xyMAC1[0][0];
		xyMAC1[1][1] = xyMAC1[0][1] + wing1.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METRE);

		listDataArray1.add(xyMAC1);

		double xMax1 = 1.05*wing1.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METRE);
		double xMin1 = -0.05*wing1.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METRE);
		double yMax1 = xMax1;
		double yMin1 = xMin1;

		List<Double[][]> listDataArray2 = new ArrayList<Double[][]>();

		listDataArray2.add(dataTopView2);

		int nSec2 = wing2.getLiftingSurfaceCreator().getDiscretizedXle().size();
		int nPanels2 = wing2.getLiftingSurfaceCreator().getPanels().size();

		Double[][] eqPts2 = new Double[4][2];
		eqPts2[0][0] = 0.0;
		eqPts2[0][1] = wing2.getLiftingSurfaceCreator().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordLE();
		eqPts2[1][0] = wing2.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER);
		eqPts2[1][1] = wing2.getLiftingSurfaceCreator().getDiscretizedXle().get(nSec2 - 1).doubleValue(SI.METER);
		eqPts2[2][0] = wing2.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER);
		eqPts2[2][1] = wing2.getLiftingSurfaceCreator().getDiscretizedXle().get(nSec2 - 1)
				.plus(
						wing2.getLiftingSurfaceCreator().getPanels().get(nPanels2 - 1).getChordTip()
						)
				.doubleValue(SI.METER);
		eqPts2[3][0] = 0.0;
		eqPts2[3][1] = wing2.getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
				- wing2.getLiftingSurfaceCreator().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordTE();

		listDataArray2.add(eqPts2);

		Double[][] xyMAC2 = new Double[2][2];
		xyMAC2[0][0] = wing2.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE);
		xyMAC2[0][1] = wing2.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE);
		xyMAC2[1][0] = xyMAC2[0][0];
		xyMAC2[1][1] = xyMAC2[0][1] + wing2.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METRE);

		listDataArray2.add(xyMAC2);

		double xMax2 = 1.05*wing2.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METRE);
		double xMin2 = -0.05*wing2.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METRE);
		double yMax2 = xMax2;
		double yMin2 = xMin2;
		
		D3PlotterOptions options1 = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(Math.min(xMin1, xMin2), Math.max(xMax1, xMax2))
				.yRange(Math.max(yMax1, yMax2), Math.min(yMin1, yMin2))	
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
				.areaStyles("fill:none;","fill:yellow;","fill:yellow;")
				.areaOpacities(1.0,0.50,0.70)
				.build();
		System.out.println("Plot options:\n" + options1);

		D3PlotterOptions options2 = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(Math.min(xMin1, xMin2), Math.max(xMax1, xMax2))
				.yRange(Math.max(yMax1, yMax2), Math.min(yMin1, yMin2))
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
				.areaStyles("fill:none;","fill:yellow;","fill:yellow;")
				.areaOpacities(1.0,0.50,0.70)
				.build();
		System.out.println("Plot options:\n" + options2);
		
		d3Plotter1 = new D3Plotter(
				options1,
				listDataArray1
				);
		d3Plotter2 = new D3Plotter(
				options2,
				listDataArray2
				);

		//define d3 content as post loading hook
		Runnable postLoadingHook1 = () -> {
			System.out.println("Runnable :: Initial loading of browser is finished");

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter1.createD3Content();

			//--------------------------------------------------
			// output
			String outputFilePath1 = outputFolderPath + "Wing_Adjust_Test_1.svg";
			d3Plotter1.saveSVG(outputFilePath1);


		}; // end-of-Runnable

		Runnable postLoadingHook2 = () -> {
			System.out.println("Runnable :: Initial loading of browser is finished");

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter2.createD3Content();

			//--------------------------------------------------
			// output
			String outputFilePath2 = outputFolderPath + "Wing_Adjust_Test_2.svg";
			d3Plotter2.saveSVG(outputFilePath2);


		}; // end-of-Runnable
		
		// create the Browser/D3
		//create browser
		JavaFxD3Browser browser1 = d3Plotter1.getBrowser(postLoadingHook1, false);
		JavaFxD3Browser browser2 = d3Plotter2.getBrowser(postLoadingHook2, false);

		//create the scene
		TabPane tabPane = new TabPane(
				new Tab("Wing_1"),
				new Tab("Wing_2")
				);
		tabPane.getTabs().get(0).setContent(browser1);
		tabPane.getTabs().get(1).setContent(browser2);
		Scene scene = new Scene(tabPane, WIDTH+10, HEIGHT+10, Color.web("#666970"));
		
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

		MyArgumentWingAdjustTest va = new MyArgumentWingAdjustTest();
		WingAdjustTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			WingAdjustTest.theCmdLineParser.parseArgument(args);
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
			theWing1 = new LiftingSurface(LiftingSurfaceCreator.importFromXML(ComponentEnum.WING, pathToXML, dirAirfoil));
			theWing1.setAeroDatabaseReader(aeroDatabaseReader);
			theWing1.setHighLiftDatabaseReader(highLiftDatabaseReader);
			theWing1.setVeDSCDatabaseReader(veDSCDatabaseReader);
			
			theWing2 = new LiftingSurface(LiftingSurfaceCreator.importFromXML(ComponentEnum.WING, pathToXML, dirAirfoil));
			theWing2.setAeroDatabaseReader(aeroDatabaseReader);
			theWing2.setHighLiftDatabaseReader(highLiftDatabaseReader);
			theWing2.setVeDSCDatabaseReader(veDSCDatabaseReader);
			
			WingAdjustTest.theWing1.getLiftingSurfaceCreator().calculateGeometry(
					40,
					theWing1.getLiftingSurfaceCreator().getType(),
					theWing1.getLiftingSurfaceCreator().isMirrored());
			WingAdjustTest.theWing2.getLiftingSurfaceCreator().calculateGeometry(
					40,
					theWing2.getLiftingSurfaceCreator().getType(),
					theWing2.getLiftingSurfaceCreator().isMirrored());

			WingAdjustTest.theWing1.getLiftingSurfaceCreator().populateAirfoilList(
					theWing1.getLiftingSurfaceCreator().getEquivalentWingFlag()
					);
			WingAdjustTest.theWing2.getLiftingSurfaceCreator().populateAirfoilList(
					theWing2.getLiftingSurfaceCreator().getEquivalentWingFlag()
					);
			
			System.out.println("Wing 1...");
			System.out.println(WingAdjustTest.theWing1.getLiftingSurfaceCreator().toString());
			System.out.println("Details on panel discretization ...");
			WingAdjustTest.theWing1.getLiftingSurfaceCreator().reportPanelsToSpanwiseDiscretizedVariables();
			System.out.println("\n\nWing 2 ...");
			System.out.println(WingAdjustTest.theWing2.getLiftingSurfaceCreator().toString());
			System.out.println("Details on panel discretization ...");
			WingAdjustTest.theWing2.getLiftingSurfaceCreator().reportPanelsToSpanwiseDiscretizedVariables();

		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			WingAdjustTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ...
		launch(args);
	}

}