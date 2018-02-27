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
import configuration.enumerations.FuselageAdjustCriteriaEnum;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
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

class MyArgumentsFuselageAdjustTest {
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

public class FuselageAdjustTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static Fuselage theFuselage1;
	public static Fuselage theFuselage2;

	//-------------------------------------------------------------

	// lateral view
	private D3Plotter d3Plotter1;
	private D3Plotter d3Plotter2;

	// front view
	private D3Plotter d3Plotter3;
	private D3Plotter d3Plotter4;
	
	private final int WIDTH = 700;
	private final int HEIGHT = 600;

	@SuppressWarnings("unused")
	private static final double DELTA = 0.001d;

	@Override
	public void start(Stage primaryStage) throws Exception {

		//--------------------------------------------------
		// get the wing object
		System.out.println("\n\n##################");
		System.out.println("function start :: getting the fuselage objects ...");

		Fuselage fuselage1 = FuselageAdjustTest.theFuselage1;
		if (fuselage1 == null) {
			System.out.println("fuselage1 object null, returning.");
			return;
		}
		Fuselage fuselage2 = FuselageAdjustTest.theFuselage2;
		if (fuselage2 == null) {
			System.out.println("fuselage2 object null, returning.");
			return;
		}
		// ADJUSTING SIDE VIEW
		fuselage2.getFuselageCreator().adjustDimensions(
				fuselage2.getFuselageCreator().getTailLength().times(1.2),
				FuselageAdjustCriteriaEnum.ADJ_TAILCONE_LENGTH_CONST_FINENESS_RATIOS_VAR_LENGTHS
				);
		// ADJUSTING FRONT VIEW
		fuselage2.getFuselageCreator().adjustSectionShapeParameters(
				fuselage2.getFuselageCreator().IDX_SECTION_YZ_CYLINDER_1,
				0.8,
				0.0, 
				0.0
				);
		
		//--------------------------------------------------
		// get data vectors from fuselage 1 side view discretization
		//--------------------------------------------------

		// upper curve, sideview
		List<Amount<Length>> vX1Fus1 = fuselage1.getFuselageCreator().getOutlineXZUpperCurveAmountX();
		int nX1Fus1 = vX1Fus1.size();
		List<Amount<Length>> vZ1Fus1 = fuselage1.getFuselageCreator().getOutlineXZUpperCurveAmountZ();

		Double[][] dataOutlineXZUpperCurveFus1 = new Double[nX1Fus1][2];
		IntStream.range(0, nX1Fus1)
		.forEach(i -> {
			dataOutlineXZUpperCurveFus1[i][0] = vX1Fus1.get(i).doubleValue(SI.METRE);
			dataOutlineXZUpperCurveFus1[i][1] = vZ1Fus1.get(i).doubleValue(SI.METRE);
		});

		// lower curve, sideview
		List<Amount<Length>> vX2Fus1 = fuselage1.getFuselageCreator().getOutlineXZLowerCurveAmountX();
		int nX2Fus1 = vX2Fus1.size();
		List<Amount<Length>> vZ2Fus1 = fuselage1.getFuselageCreator().getOutlineXZLowerCurveAmountZ();

		Double[][] dataOutlineXZLowerCurveFus1 = new Double[nX2Fus1][2];
		IntStream.range(0, nX2Fus1)
		.forEach(i -> {
			dataOutlineXZLowerCurveFus1[i][0] = vX2Fus1.get(i).doubleValue(SI.METRE);
			dataOutlineXZLowerCurveFus1[i][1] = vZ2Fus1.get(i).doubleValue(SI.METRE);
		});

		// camberline, sideview
		List<Amount<Length>> vX3Fus1 = fuselage1.getFuselageCreator().getOutlineXZCamberLineAmountX();
		int nX3Fus1 = vX3Fus1.size();
		List<Amount<Length>> vZ3Fus1 = fuselage1.getFuselageCreator().getOutlineXZCamberLineAmountZ();

		Double[][] dataOutlineXZCamberLineFus1 = new Double[nX3Fus1][2];
		IntStream.range(0, nX3Fus1)
		.forEach(i -> {
			dataOutlineXZCamberLineFus1[i][0] = vX3Fus1.get(i).doubleValue(SI.METRE);
			dataOutlineXZCamberLineFus1[i][1] = vZ3Fus1.get(i).doubleValue(SI.METRE);
		});

		//--------------------------------------------------
		// get data vectors from fuselage 2 front view discretization
		//--------------------------------------------------
		// section upper curve
		List<Amount<Length>> vY1UpperFus1 = fuselage1.getFuselageCreator().getSectionUpperCurveAmountY();
		int nY1UpperFus1 = vY1UpperFus1.size();
		List<Amount<Length>> vZ1UpperFus1 = fuselage1.getFuselageCreator().getSectionUpperCurveAmountZ();

		Double[][] dataSectionYZUpperCurveFus1 = new Double[nY1UpperFus1][2];
		IntStream.range(0, nY1UpperFus1)
		.forEach(i -> {
			dataSectionYZUpperCurveFus1[i][0] = vY1UpperFus1.get(i).doubleValue(SI.METRE);
			dataSectionYZUpperCurveFus1[i][1] = vZ1UpperFus1.get(i).doubleValue(SI.METRE);
		});

		// section lower curve
		List<Amount<Length>> vY2LowerFus1 = fuselage1.getFuselageCreator().getSectionLowerCurveAmountY();
		int nY2LowerFus1 = vY2LowerFus1.size();
		List<Amount<Length>> vZ2LowerFus1 = fuselage1.getFuselageCreator().getSectionLowerCurveAmountZ();

		Double[][] dataSectionYZLowerCurveFus1 = new Double[nY2LowerFus1][2];
		IntStream.range(0, nY2LowerFus1)
		.forEach(i -> {
			dataSectionYZLowerCurveFus1[i][0] = vY2LowerFus1.get(i).doubleValue(SI.METRE);
			dataSectionYZLowerCurveFus1[i][1] = vZ2LowerFus1.get(i).doubleValue(SI.METRE);
		});
		
		//--------------------------------------------------
		// get data vectors from fuselage 2 side view discretization
		//--------------------------------------------------

		// upper curve, sideview
		List<Amount<Length>> vX1Fus2 = fuselage2.getFuselageCreator().getOutlineXZUpperCurveAmountX();
		int nX1Fus2 = vX1Fus2.size();
		List<Amount<Length>> vZ1Fus2 = fuselage2.getFuselageCreator().getOutlineXZUpperCurveAmountZ();

		Double[][] dataOutlineXZUpperCurveFus2 = new Double[nX1Fus2][2];
		IntStream.range(0, nX1Fus2)
		.forEach(i -> {
			dataOutlineXZUpperCurveFus2[i][0] = vX1Fus2.get(i).doubleValue(SI.METRE);
			dataOutlineXZUpperCurveFus2[i][1] = vZ1Fus2.get(i).doubleValue(SI.METRE);
		});

		// lower curve, sideview
		List<Amount<Length>> vX2Fus2 = fuselage2.getFuselageCreator().getOutlineXZLowerCurveAmountX();
		int nX2Fus2 = vX2Fus2.size();
		List<Amount<Length>> vZ2Fus2 = fuselage2.getFuselageCreator().getOutlineXZLowerCurveAmountZ();

		Double[][] dataOutlineXZLowerCurveFus2 = new Double[nX2Fus2][2];
		IntStream.range(0, nX2Fus2)
		.forEach(i -> {
			dataOutlineXZLowerCurveFus2[i][0] = vX2Fus2.get(i).doubleValue(SI.METRE);
			dataOutlineXZLowerCurveFus2[i][1] = vZ2Fus2.get(i).doubleValue(SI.METRE);
		});

		// camberline, sideview
		List<Amount<Length>> vX3Fus2 = fuselage2.getFuselageCreator().getOutlineXZCamberLineAmountX();
		int nX3Fus2 = vX3Fus2.size();
		List<Amount<Length>> vZ3Fus2 = fuselage2.getFuselageCreator().getOutlineXZCamberLineAmountZ();

		Double[][] dataOutlineXZCamberLineFus2 = new Double[nX3Fus2][2];
		IntStream.range(0, nX3Fus2)
		.forEach(i -> {
			dataOutlineXZCamberLineFus2[i][0] = vX3Fus2.get(i).doubleValue(SI.METRE);
			dataOutlineXZCamberLineFus2[i][1] = vZ3Fus2.get(i).doubleValue(SI.METRE);
		});
		
		//--------------------------------------------------
		// get data vectors from fuselage 2 front view discretization
		//--------------------------------------------------
		// section upper curve
		List<Amount<Length>> vY1UpperFus2 = fuselage2.getFuselageCreator().getSectionUpperCurveAmountY();
		int nY1UpperFus2 = vY1UpperFus2.size();
		List<Amount<Length>> vZ1UpperFus2 = fuselage2.getFuselageCreator().getSectionUpperCurveAmountZ();

		Double[][] dataSectionYZUpperCurveFus2 = new Double[nY1UpperFus2][2];
		IntStream.range(0, nY1UpperFus2)
		.forEach(i -> {
			dataSectionYZUpperCurveFus2[i][0] = vY1UpperFus2.get(i).doubleValue(SI.METRE);
			dataSectionYZUpperCurveFus2[i][1] = vZ1UpperFus2.get(i).doubleValue(SI.METRE);
		});

		// section lower curve
		List<Amount<Length>> vY2LowerFus2 = fuselage2.getFuselageCreator().getSectionLowerCurveAmountY();
		int nY2LowerFus2 = vY2LowerFus2.size();
		List<Amount<Length>> vZ2LowerFus2 = fuselage2.getFuselageCreator().getSectionLowerCurveAmountZ();

		Double[][] dataSectionYZLowerCurveFus2 = new Double[nY2LowerFus1][2];
		IntStream.range(0, nY2LowerFus2)
		.forEach(i -> {
			dataSectionYZLowerCurveFus2[i][0] = vY2LowerFus2.get(i).doubleValue(SI.METRE);
			dataSectionYZLowerCurveFus2[i][1] = vZ2LowerFus2.get(i).doubleValue(SI.METRE);
		});
		
		//--------------------------------------------------
		System.out.println("Initializing test class...");
		String rootOutputFolderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String outputFolderPath = JPADStaticWriteUtils.createNewFolder(rootOutputFolderPath + "Tests_Aircraft" + File.separator);
		System.out.println("Output ==> " + outputFolderPath);

		//--------------------------------------------------
		//set state title
		primaryStage.setTitle("treez/javafx-d3 - D3Plotter demo");

		List<Double[][]> listDataArray1 = new ArrayList<Double[][]>();
		List<Double[][]> listDataArray2 = new ArrayList<Double[][]>();
		List<Double[][]> listDataArray3 = new ArrayList<Double[][]>();
		List<Double[][]> listDataArray4 = new ArrayList<Double[][]>();

		listDataArray1.add(dataOutlineXZUpperCurveFus1);
		listDataArray1.add(dataOutlineXZLowerCurveFus1);
		listDataArray1.add(dataOutlineXZCamberLineFus1);

		listDataArray2.add(dataOutlineXZUpperCurveFus2);
		listDataArray2.add(dataOutlineXZLowerCurveFus2);
		listDataArray2.add(dataOutlineXZCamberLineFus2);
		
		listDataArray3.add(dataSectionYZUpperCurveFus1);
		listDataArray3.add(dataSectionYZLowerCurveFus1);
		
		listDataArray4.add(dataSectionYZUpperCurveFus2);
		listDataArray4.add(dataSectionYZLowerCurveFus2);
		
		double xMax1 = 1.20*fuselage1.getFuselageCreator().getFuselageLength().doubleValue(SI.METRE);
		double xMin1 = -0.20*fuselage1.getFuselageCreator().getFuselageLength().doubleValue(SI.METRE);
		double yMax1 = 15;
		double yMin1 = -15;
		
		double xMax2 = 1.20*fuselage2.getFuselageCreator().getFuselageLength().doubleValue(SI.METRE);
		double xMin2 = -0.20*fuselage2.getFuselageCreator().getFuselageLength().doubleValue(SI.METRE);
		double yMax2 = 15;
		double yMin2 = -15;

		double xMax3 = 1.20*fuselage1.getFuselageCreator().getSectionCylinderWidth().doubleValue(SI.METER);
		double xMin3 = -1.20*fuselage1.getFuselageCreator().getSectionCylinderWidth().doubleValue(SI.METER);
		double yMax3 = 1.20*fuselage1.getFuselageCreator().getSectionCylinderWidth().doubleValue(SI.METER); 
		double yMin3 = -1.20*fuselage1.getFuselageCreator().getSectionCylinderWidth().doubleValue(SI.METER);
		
		double xMax4 = 1.20*fuselage2.getFuselageCreator().getSectionCylinderWidth().doubleValue(SI.METER);
		double xMin4 = -1.20*fuselage2.getFuselageCreator().getSectionCylinderWidth().doubleValue(SI.METER);
		double yMax4 = 1.20*fuselage2.getFuselageCreator().getSectionCylinderWidth().doubleValue(SI.METER); 
		double yMin4 = -1.20*fuselage2.getFuselageCreator().getSectionCylinderWidth().doubleValue(SI.METER);
		
		D3PlotterOptions options = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthSVG(1*WIDTH).heightSVG(1*HEIGHT)
				.xRange(Math.min(xMin1, xMin2), Math.max(xMax1, xMax2))
				.yRange(Math.min(yMin1, yMin2), Math.max(yMax1, yMax2))
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

		D3PlotterOptions options1 = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthSVG(1*WIDTH).heightSVG(1*HEIGHT)
				.xRange(Math.min(xMin3, xMin4), Math.max(xMax3, xMax4))
				.yRange(Math.min(yMin3, yMin4), Math.max(yMax3, yMax4))
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
		
		d3Plotter1 = new D3Plotter(
				options,
				listDataArray1
				);
		d3Plotter2 = new D3Plotter(
				options,
				listDataArray2
				);
		d3Plotter3 = new D3Plotter(
				options1,
				listDataArray3
				);
		d3Plotter4 = new D3Plotter(
				options1,
				listDataArray4
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
			String outputFilePath1 = outputFolderPath + "Fuselage_Adjust_Test_1_Side.svg";
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
			String outputFilePath2 = outputFolderPath + "Fuselage_Adjust_Test_2_Side.svg";
			d3Plotter2.saveSVG(outputFilePath2);


		}; // end-of-Runnable
		
		//define d3 content as post loading hook
		Runnable postLoadingHook3 = () -> {
			System.out.println("Runnable :: Initial loading of browser is finished");

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter3.createD3Content();

			//--------------------------------------------------
			// output
			String outputFilePath3 = outputFolderPath + "Fuselage_Adjust_Test_1_Front.svg";
			d3Plotter3.saveSVG(outputFilePath3);


		}; // end-of-Runnable
		
		//define d3 content as post loading hook
		Runnable postLoadingHook4 = () -> {
			System.out.println("Runnable :: Initial loading of browser is finished");

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter4.createD3Content();

			//--------------------------------------------------
			// output
			String outputFilePath4 = outputFolderPath + "Fuselage_Adjust_Test_2_Front.svg";
			d3Plotter4.saveSVG(outputFilePath4);


		}; // end-of-Runnable
		
		// create the Browser/D3
		//create browser
		JavaFxD3Browser browser1 = d3Plotter1.getBrowser(postLoadingHook1, false);
		JavaFxD3Browser browser2 = d3Plotter2.getBrowser(postLoadingHook2, false);
		JavaFxD3Browser browser3 = d3Plotter3.getBrowser(postLoadingHook3, false);
		JavaFxD3Browser browser4 = d3Plotter4.getBrowser(postLoadingHook4, false);

		//create the scene
		TabPane tabPane = new TabPane(
				new Tab("Fuselage_Side_1"),
				new Tab("Fuselage_Front_1"),
				new Tab("Fuselage_Side_2"),
				new Tab("Fuselage_Front_2")
				);
		tabPane.getTabs().get(0).setContent(browser1);
		tabPane.getTabs().get(1).setContent(browser3);
		tabPane.getTabs().get(2).setContent(browser2);
		tabPane.getTabs().get(3).setContent(browser4);
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

		System.out.println("----------------------");
		System.out.println("Fuselage test / D3");
		System.out.println("----------------------");

		MyArgumentsFuselageAdjustTest va = new MyArgumentsFuselageAdjustTest();
		FuselageAdjustTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			FuselageAdjustTest.theCmdLineParser.parseArgument(args);
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToXML);

			System.out.println("--------------");

			MyConfiguration.initWorkingDirectoryTree();
			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String fusDesDatabaseFilename = "FusDes_database.h5";
			FusDesDatabaseReader fusDesDatabaseReader = new FusDesDatabaseReader(databaseFolderPath, fusDesDatabaseFilename);
			
			// default Fuselage ...
			theFuselage1 = null;
			theFuselage2 = null;

			System.out.println("The fuselage 1 ...");
			System.out.println(FuselageAdjustTest.theFuselage1.getFuselageCreator().toString());
			System.out.println("Details on discretization ...");
			
			System.out.println("The fuselage 2 ...");
			System.out.println(FuselageAdjustTest.theFuselage2.getFuselageCreator().toString());
			System.out.println("Details on discretization ...");

		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			FuselageAdjustTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ...
		launch(args);
	}

}