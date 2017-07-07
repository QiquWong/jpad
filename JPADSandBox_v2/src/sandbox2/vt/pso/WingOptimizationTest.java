package sandbox2.vt.pso;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
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
import analyses.OperatingConditions;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.OptimizationEnum;
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
import optimization.CostFunctions;
import optimization.ParticleSwarmOptimizer;
import sandbox2.javafx.D3Plotter;
import sandbox2.javafx.D3PlotterOptions;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import writers.JPADStaticWriteUtils;

class MyArgumentWingOptimizationTest {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	@Option(name = "-ioc", aliases = { "--input-operating-condition" }, required = true,
			usage = "operating conditions input file")
	private File _inputFileOperatingCondition;
	
	@Option(name = "-da", aliases = { "--dir-airfoils" }, required = true,
			usage = "airfoil directory path")
	private File _airfoilDirectory;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}

	public File getOperatingConditionsInputFile() {
		return _inputFileOperatingCondition;
	}
	
	public File getAirfoilDirectory() {
		return _airfoilDirectory;
	}

}

public class WingOptimizationTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;
	public static String folderPath; 
	public static String subfolderPath;
	//-------------------------------------------------------------

	public static LiftingSurface baselineWing;
	public static LiftingSurface optimizedWing;
	public static List<LiftingSurface> theWings = new ArrayList<>();

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

		LiftingSurface wing1 = WingOptimizationTest.baselineWing;
		if (wing1 == null) {
			System.out.println("wing1 object null, returning.");
			return;
		}
		LiftingSurface wing2 = WingOptimizationTest.optimizedWing;
		if (wing2 == null) {
			System.out.println("wing1 object null, returning.");
			return;
		}

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
		System.out.println("Output ==> " + subfolderPath);

		//--------------------------------------------------
		//set state title
		primaryStage.setTitle("treez/javafx-d3 - D3Plotter demo");

		List<Double[][]> listDataArray1 = new ArrayList<Double[][]>();

		listDataArray1.add(dataTopView1);

		int nSec1 = wing1.getLiftingSurfaceCreator().getDiscretizedXle().size();
		int nPanels1 = wing1.getLiftingSurfaceCreator().getPanels().size();

		Double[][] eqPts1 = new Double[4][2];
		eqPts1[0][0] = 0.0;
		eqPts1[0][1] = wing1.getLiftingSurfaceCreator().getEquivalentWing().getXOffsetEquivalentWingRootLE();
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
				- wing1.getLiftingSurfaceCreator().getEquivalentWing().getXOffsetEquivalentWingRootTE();

		listDataArray1.add(eqPts1);

		Double[][] xyMAC1 = new Double[2][2];
		xyMAC1[0][0] = wing1.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE);
		xyMAC1[0][1] = wing1.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE);
		xyMAC1[1][0] = xyMAC1[0][0];
		xyMAC1[1][1] = xyMAC1[0][1] + wing1.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METRE);

		listDataArray1.add(xyMAC1);

		double xMax1 = 1.05*wing1.getSemiSpan().doubleValue(SI.METRE);
		double xMin1 = -0.05*wing1.getSemiSpan().doubleValue(SI.METRE);
		double yMax1 = xMax1;
		double yMin1 = xMin1;

		List<Double[][]> listDataArray2 = new ArrayList<Double[][]>();

		listDataArray2.add(dataTopView2);

		int nSec2 = wing2.getLiftingSurfaceCreator().getDiscretizedXle().size();
		int nPanels2 = wing2.getLiftingSurfaceCreator().getPanels().size();

		Double[][] eqPts2 = new Double[4][2];
		eqPts2[0][0] = 0.0;
		eqPts2[0][1] = wing2.getLiftingSurfaceCreator().getEquivalentWing().getXOffsetEquivalentWingRootLE();
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
				- wing2.getLiftingSurfaceCreator().getEquivalentWing().getXOffsetEquivalentWingRootTE();

		listDataArray2.add(eqPts2);

		Double[][] xyMAC2 = new Double[2][2];
		xyMAC2[0][0] = wing2.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE);
		xyMAC2[0][1] = wing2.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE);
		xyMAC2[1][0] = xyMAC2[0][0];
		xyMAC2[1][1] = xyMAC2[0][1] + wing2.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METRE);

		listDataArray2.add(xyMAC2);

		double xMax2 = 1.05*wing2.getSemiSpan().doubleValue(SI.METRE);
		double xMin2 = -0.05*wing2.getSemiSpan().doubleValue(SI.METRE);
		double yMax2 = xMax2;
		double yMin2 = xMin2;
		
		D3PlotterOptions options1 = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(Math.min(xMin1, xMin2), Math.max(xMax1, xMax2))
				.yRange(Math.max(yMax1, yMax2), Math.min(yMin1, yMin2))	
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.1)
				.title("Baseline Wing")
				.xLabel("x (m)")
				.yLabel("y (m)")
				.showXGrid(true)
				.showYGrid(true)
				.showLegend(false)
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
				.title("Optimized Wing")
				.xLabel("x (m)")
				.yLabel("y (m)")
				.showXGrid(true)
				.showLegend(false)
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
			String outputFilePath1 = subfolderPath + "Baseline_Wing.svg";
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
			String outputFilePath2 = subfolderPath + "Optimized_Wing.svg";
			d3Plotter2.saveSVG(outputFilePath2);


		}; // end-of-Runnable
		
		// create the Browser/D3
		//create browser
		JavaFxD3Browser browser1 = d3Plotter1.getBrowser(postLoadingHook1, false);
		JavaFxD3Browser browser2 = d3Plotter2.getBrowser(postLoadingHook2, false);

		//create the scene
		TabPane tabPane = new TabPane(
				new Tab("Baseline Wing"),
				new Tab("Optimized Wing")
				);
		tabPane.getTabs().get(0).setContent(browser1);
		tabPane.getTabs().get(1).setContent(browser2);
		Scene scene = new Scene(tabPane, WIDTH+10, HEIGHT+10, Color.web("#666970"));
		
		primaryStage.setScene(scene);

		// SHOW THE SCEN FINALLY
		primaryStage.show();

	}
	
	/* Main
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();     
		
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
		    public void write(int b) {
		         // write nothing
		    }
		});
		
		System.out.println("\t--------------");
		System.out.println("\tWing Optimization Test");
		System.out.println("\t--------------");
		
		MyArgumentWingOptimizationTest va = new MyArgumentWingOptimizationTest();
		WingOptimizationTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			WingOptimizationTest.theCmdLineParser.parseArgument(args);
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("\tINPUT ===> " + pathToXML);

			String pathToOperatingConditionsXML = va.getOperatingConditionsInputFile().getAbsolutePath();
			System.out.println("OPERATING CONDITIONS INPUT ===> " + pathToOperatingConditionsXML);
			
			String dirAirfoil = va.getAirfoilDirectory().getCanonicalPath();
			System.out.println("\tAIRFOILS ===> " + dirAirfoil);

			System.out.println("\t--------------");

			//------------------------------------------------------------------------------------
			// Setup database(s)
			System.out.println("\t------------------------------------");
			System.out.println("\tSetting up databases and folders ...  ");
			System.out.println("\t------------------------------------\n");
			
			MyConfiguration.initWorkingDirectoryTree();
			folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
			subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + File.separator + "Wing_Optimization_Test" + File.separator);
			
			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
			String vedscDatabaseFilename = "VeDSC_database.h5";
			AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
			HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
			VeDSCDatabaseReader veDSCDatabaseReader = new VeDSCDatabaseReader(databaseFolderPath, vedscDatabaseFilename);
			
			//------------------------------------------------------------------------------------
			// Reading Operating Conditions from xml ...
			System.setOut(originalOut);
			System.out.println("\n\n\tDefining the operating conditions ... \n\n");
			System.setOut(filterStream);
			OperatingConditions theOperatingConditions = OperatingConditions.importFromXML(pathToOperatingConditionsXML);
			System.setOut(originalOut);
			System.out.println(theOperatingConditions.toString());
			System.setOut(filterStream);
			
			//------------------------------------------------------------------------------------
			// Reading LiftingSurface from xml  (WING 0 = BASELINE) ...
			System.setOut(filterStream);
			baselineWing = new LiftingSurfaceBuilder(
					"Wing_0",
					ComponentEnum.WING,
					aeroDatabaseReader,
					highLiftDatabaseReader,
					veDSCDatabaseReader
					)
					.liftingSurfaceCreator(
							LiftingSurfaceCreator.importFromXML(ComponentEnum.WING, pathToXML, dirAirfoil)
							)
					.build();
			theWings.add(baselineWing);
			baselineWing.calculateGeometry(
					40,
					baselineWing.getType(),
					baselineWing.getLiftingSurfaceCreator().isMirrored()
					);
			baselineWing.populateAirfoilList(
					aeroDatabaseReader, 
					Boolean.FALSE
					); 
			System.setOut(originalOut);
			//================================================================================
			// INPUTS
			int numberOfDesignVariables = 3;
			int numberOfVar1 = 5; // AR
			int numberOfVar2 = 5; // Taper Ratio
			int numberOfVar3 = 5; // Sweep LE (Eq.)
			WingAdjustCriteriaEnum wingAdjustCriterion = WingAdjustCriteriaEnum.AR_SPAN_TAPER;
			OptimizationEnum costFunctionType = OptimizationEnum.CL_MAX_WING;
			ConditionEnum theFlightCondition = ConditionEnum.CRUISE;
			double objectiveWeight1 = 0.5;
			double objectiveWeight2 = 0.5;
			int numberOfPointsSemiSpanwise = 50;
			
			double[] var1Array = MyArrayUtils.linspace(
					baselineWing.getLiftingSurfaceCreator().getAspectRatio()*0.7,
					baselineWing.getLiftingSurfaceCreator().getAspectRatio()*1.3,
					numberOfVar1
					);
			double[] var2Array = MyArrayUtils.linspace(
					baselineWing.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio()*0.7,
					1.0,
					numberOfVar2
					);
			double[] var3Array = MyArrayUtils.linspace(
					0.0,
					baselineWing.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().doubleValue(NonSI.DEGREE_ANGLE)*1.3,
					numberOfVar3
					);
			
			Double[] designVariablesLowerBound = new Double[] {var1Array[0], var2Array[0], var3Array[0]};
			Double[] designVariablesUpperBound = new Double[] {var1Array[var1Array.length-1], var2Array[var2Array.length-1], var3Array[var3Array.length-1]};
			Double convergenceThreshold = 1e-10; // threshold used to compare particles position during each iteration
			int particlesNumber = 100000;
			Double kappa = 1.0;
			Double phi1 = 2.05;
			Double phi2 = 2.05;

			//................................................................................................
			// DERIVED DATA
			Double mach = null;
			Double machTransonicThreshold = theOperatingConditions.getMachTransonicThreshold();
			Amount<Length> altitude = null;
			
			switch (theFlightCondition) {
			case TAKE_OFF:
				mach = theOperatingConditions.getMachTakeOff();
				altitude = theOperatingConditions.getAltitudeTakeOff();
				break;
			case CLIMB:
				mach = theOperatingConditions.getMachClimb();
				altitude = theOperatingConditions.getAltitudeClimb();
				break;
			case CRUISE:
				mach = theOperatingConditions.getMachCruise();
				altitude = theOperatingConditions.getAltitudeCruise();
				break;
			case LANDING:
				mach = theOperatingConditions.getMachLanding();
				altitude = theOperatingConditions.getAltitudeLanding();
				break;
			default:
				break;
			}
			
			//................................................................................................
			
			System.out.println("\t------------------------------------");
			System.out.println("\tINPUT: ");
			System.out.println("\tNumber of Design Variable : " + numberOfDesignVariables);
			System.out.println("\tDesign Variable 1 : " + Arrays.toString(var1Array));
			System.out.println("\tDesign Variable 2 : " + Arrays.toString(var2Array));
			System.out.println("\tDesign Variable 3 : " + Arrays.toString(var3Array));
			System.out.println("\n\tMach : " + mach);
			System.out.println("\tAltitude : " + altitude);
			System.out.println("\tNumber of points semi-spanwise : " + numberOfPointsSemiSpanwise);
			System.out.println("\n\tConvergence Threshold : " + convergenceThreshold);
			System.out.println("\tParticles Number : " + particlesNumber);
			System.out.println("\n\tConstriction Coefficient");
			System.out.println("\t\tKappa : " + kappa);
			System.out.println("\t\tPhi 1 : " + phi1);
			System.out.println("\t\tPhi 2 : " + phi2);
			System.out.println("\t------------------------------------\n");
			
			//================================================================================
			
			System.out.println("\t------------------------------------");
			System.out.println("\tGenerating wings population ...  ");
			System.out.println("\t------------------------------------\n");
			
			/*
			 * THIS 2D ARRAY HAS TO BE PASSED TO THE OPTIMIZER IN ORDER TO 
			 * EVALUATE THE COST FUNCTION VALUE 
			 * (EACH LINE IS RELATED TO A DESIGN VARIABLE)   
			 */
			double[][] xArraysCostFunction = new double[numberOfDesignVariables][];
			xArraysCostFunction[0] = var1Array; 
			xArraysCostFunction[1] = var2Array;
			xArraysCostFunction[2] = var3Array;
			
			/*
			 * THIS 3D ARRAY HAS TO BE PASSED TO THE OPTIMIZER IN ORDER TO 
			 * EVALUATE THE COST FUNCTION VALUE 
			 * (IT MUST HAVE A NUMBER OF DIMENSION EQUAL TO THE NUMBER OF 
			 *  DESIGN VARIABLES)   
			 */
			double[][][] costFunctionValues = new double[var1Array.length][var2Array.length][var3Array.length];
			
			for(int i = 0; i < numberOfVar1; i++) {
				for(int j = 0; j < numberOfVar2; j++) {
					for(int k = 0; k < numberOfVar3; k++) {
						
						System.setOut(originalOut);
						System.out.println("\t\tWing_" + (i+1) + "_" + (j+1) + "_" + (k+1));
						System.setOut(filterStream);
						
						LiftingSurface currentWing = new LiftingSurfaceBuilder(
								"Wing_" + (i+1) + "_" + (j+1) + "_" + (k+1),
								ComponentEnum.WING,
								aeroDatabaseReader,
								highLiftDatabaseReader,
								veDSCDatabaseReader
								)
								.liftingSurfaceCreator(
										LiftingSurfaceCreator.importFromXML(ComponentEnum.WING, pathToXML, dirAirfoil)
										)
								.build();
						
						currentWing.calculateGeometry(
								40,
								currentWing.getType(),
								currentWing.getLiftingSurfaceCreator().isMirrored());
						currentWing.populateAirfoilList(
								aeroDatabaseReader,
								Boolean.FALSE
								);
						currentWing.setExposedWing(currentWing);
						currentWing.getLiftingSurfaceCreator().setSurfaceWettedExposed(
								currentWing.getLiftingSurfaceCreator().getSurfacePlanform()
								);
						double compressibilityFactor = 1.
								/ Math.sqrt(
										1 - Math.pow(mach, 2)
										* (Math.pow(Math.cos(
												currentWing.getSweepQuarterChordEquivalent()
													.doubleValue(SI.RADIAN)),2)
												)
										);
						currentWing.calculateThicknessMean();
						currentWing.calculateFormFactor(compressibilityFactor);
						
						currentWing.getLiftingSurfaceCreator().adjustDimensions(
								var1Array[i],
								baselineWing.getLiftingSurfaceCreator().getSpan().doubleValue(SI.METER),
								var2Array[j],
								Amount.valueOf(var3Array[k], NonSI.DEGREE_ANGLE),
								baselineWing.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getDihedral(),
								baselineWing.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(), 
								wingAdjustCriterion
								);
						
						theWings.add(currentWing);
						
						switch (costFunctionType) {
						case CL_MAX_WING:
							// sign (-) for a minimization problem
							costFunctionValues[i][j][k] = -CostFunctions.cLmaxWing(
									currentWing,
									numberOfPointsSemiSpanwise, 
									mach,
									altitude
									);
							break;
						case CL_MAX_VS_CD0_WING:
							costFunctionValues[i][j][k] = CostFunctions.cLmaxVsCD0Wing(
									currentWing, 
									numberOfPointsSemiSpanwise, 
									mach, 
									machTransonicThreshold, 
									altitude, 
									objectiveWeight1, 
									objectiveWeight2
									);
							break;
						case CD0_WING:
							costFunctionValues[i][j][k] = CostFunctions.cD0Wing(
									currentWing, 
									mach, 
									machTransonicThreshold, 
									altitude
									);
							break;
						default:
							break;
						}
					}
				}
			}
			System.setOut(originalOut);
			
			System.out.println("\t------------------------------------");
			System.out.println("\tParticle Swarm Optimization :: START ");
			System.out.println("\t------------------------------------\n");
			
			// CALLING THE PSO OPTIMIZER ...
			ParticleSwarmOptimizer pso = new ParticleSwarmOptimizer(
					numberOfDesignVariables,
					designVariablesUpperBound,
					designVariablesLowerBound,
					convergenceThreshold,
					particlesNumber, 
					kappa, 
					phi1, 
					phi2,
					subfolderPath,
					xArraysCostFunction,
					costFunctionValues
					);
			
			pso.optimize();
			
			//-------------------------------------------------------------------------------
			// DEFINING THE OPTIMIZED WING ...
			System.setOut(filterStream);
			optimizedWing = new LiftingSurfaceBuilder(
					"Optimized Wing",
					ComponentEnum.WING,
					aeroDatabaseReader,
					highLiftDatabaseReader,
					veDSCDatabaseReader
					)
					.liftingSurfaceCreator(
							LiftingSurfaceCreator.importFromXML(ComponentEnum.WING, pathToXML, dirAirfoil)
							)
					.build();
			
			optimizedWing.calculateGeometry(
					40,
					optimizedWing.getType(),
					optimizedWing.getLiftingSurfaceCreator().isMirrored());
			optimizedWing.populateAirfoilList(
					aeroDatabaseReader,
					Boolean.FALSE
					);
			optimizedWing.getLiftingSurfaceCreator().adjustDimensions(
					pso.getBestPosition()[0],
					baselineWing.getLiftingSurfaceCreator().getSpan().doubleValue(SI.METER),
					pso.getBestPosition()[1],
					Amount.valueOf(pso.getBestPosition()[2], NonSI.DEGREE_ANGLE),
					baselineWing.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getDihedral(),
					baselineWing.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(), 
					wingAdjustCriterion
					);
			System.setOut(originalOut);
			
			//-------------------------------------------------------------------------------
			System.out.println("\n\n\t------------------------------------");
			System.out.println("\tParticle Swarm Optimization :: END ");
			System.out.println("\t------------------------------------\n");
			
			long estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println("\t TIME ESTIMATED = " + (estimatedTime/1000) + " seconds");
			

		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			WingOptimizationTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
		}
		
		// JavaFX ...
		System.setOut(filterStream);
		launch(args);
	}
}