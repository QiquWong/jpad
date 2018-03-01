package sandbox2.vt.pso;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
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
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import analyses.OperatingConditions;
import analyses.liftingsurface.LSAerodynamicsManager;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCLmax;
import analyses.liftingsurface.LSAerodynamicsManager.CalcPolar;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
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
import standaloneutils.MyUnits;
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

	public static Double cLmaxBaseline = null;
	public static Double cDMinBaseline = null;
	public static Amount<Mass> wingMassBaseline = null;
	public static Double cLmaxOptimized = null;
	public static Double cDMinOptimized = null;
	public static Amount<Mass> wingMassOptimized = null;
	
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
			baselineWing = new LiftingSurface(LiftingSurfaceCreator.importFromXML(ComponentEnum.WING, pathToXML, dirAirfoil));
			baselineWing.setAeroDatabaseReader(aeroDatabaseReader);
			baselineWing.setHighLiftDatabaseReader(highLiftDatabaseReader);
			baselineWing.setVeDSCDatabaseReader(veDSCDatabaseReader);
			theWings.add(baselineWing);
			baselineWing.getLiftingSurfaceCreator().calculateGeometry(
					40,
					baselineWing.getLiftingSurfaceCreator().getType(),
					baselineWing.getLiftingSurfaceCreator().isMirrored()
					);
			baselineWing.getLiftingSurfaceCreator().populateAirfoilList(false); 
			System.setOut(originalOut);
			//================================================================================
			// INPUTS
			int numberOfDesignVariables = 3;
			int numberOfVar1 = 5; // AR
			int numberOfVar2 = 5; // Taper Ratio
			int numberOfVar3 = 5; // Sweep LE (Eq.)
			WingAdjustCriteriaEnum wingAdjustCriterion = WingAdjustCriteriaEnum.AR_SPAN_TAPER;
			OptimizationEnum costFunctionType = OptimizationEnum.CL_MAX_VS_MASS_WING;
			ConditionEnum theFlightCondition = ConditionEnum.CRUISE;
			double objectiveWeight1 = 0.4;
			double objectiveWeight2 = 1 - objectiveWeight1;
			int numberOfPointsSemiSpanwise = 50;
			Amount<Angle> alphaInitial = Amount.valueOf(-2.0, NonSI.DEGREE_ANGLE);
			Amount<Angle> alphaFinal = Amount.valueOf(20.0, NonSI.DEGREE_ANGLE);
			int numberOfAlphas = 23;

			//............................................................................
			// input only for wing mass
			Amount<Mass> maxTakeOffMass = Amount.valueOf(53610, SI.KILOGRAM);
			Amount<Mass> maxZeroFuelMass = Amount.valueOf(49345, SI.KILOGRAM);
			double ultimateLoadFactor = 3.75; // 1.5 * limitLoadFactor = 1.5*2.5 = 3.75 
			//............................................................................
			
			double[] var1Array = MyArrayUtils.linspace(
					9.0,
					12.0,
					numberOfVar1
					);
			double[] var2Array = MyArrayUtils.linspace(
					0.3,
					0.7,
					numberOfVar2
					);
			double[] var3Array = MyArrayUtils.linspace(
					0.0,
					20,
					numberOfVar3
					);
			
			Double[] designVariablesLowerBound = new Double[] {var1Array[0], var2Array[0], var3Array[0]};
			Double[] designVariablesUpperBound = new Double[] {var1Array[var1Array.length-1], var2Array[var2Array.length-1], var3Array[var3Array.length-1]};
			Double convergenceThreshold = 1e-10; // threshold used to compare particles position during each iteration
			int particlesNumber = 100000;
			Double kappa = 1.0;
			Double phi1 = 2.05;
			Double phi2 = 2.05;

			// BASELINE
			LSAerodynamicsManager theBaselineWingAerodynamicsManager = new LSAerodynamicsManager(
					baselineWing,
					theOperatingConditions,
					theFlightCondition, 
					numberOfPointsSemiSpanwise, 
					MyArrayUtils.convertDoubleArrayToListOfAmount(
							MyArrayUtils.linspace(
									alphaInitial.doubleValue(NonSI.DEGREE_ANGLE), 
									alphaFinal.doubleValue(NonSI.DEGREE_ANGLE), 
									numberOfAlphas
									),
							NonSI.DEGREE_ANGLE),
					null, 
					null
					);
			
			CalcCLmax calcCLmaxBaseline = theBaselineWingAerodynamicsManager.new CalcCLmax();
			calcCLmaxBaseline.nasaBlackwell();
			cLmaxBaseline = theBaselineWingAerodynamicsManager.getCLMax().get(MethodEnum.NASA_BLACKWELL);
			
			CalcPolar calcPolarBaseline = theBaselineWingAerodynamicsManager.new CalcPolar();
			calcPolarBaseline.fromCdDistribution(
					theBaselineWingAerodynamicsManager.getCurrentMachNumber(),
					theBaselineWingAerodynamicsManager.getCurrentAltitude()
					);
			cDMinBaseline = MyArrayUtils.getMin(theBaselineWingAerodynamicsManager.getPolar3DCurve().get(MethodEnum.AIRFOIL_DISTRIBUTION));
			
			Amount<Area> wingSurface = theBaselineWingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getSurfacePlanform();
			Amount<Length> wingSpan = theBaselineWingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getSpan();
			Double taperRatio = theBaselineWingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio();
			Amount<Angle> sweepQuarterChord = theBaselineWingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord();
			Double thicknessMean = theBaselineWingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getThicknessMean();
			
			wingMassBaseline = Amount.valueOf(
					(4.22*wingSurface.doubleValue(MyUnits.FOOT2) 
							+ 1.642e-6
							* (ultimateLoadFactor
									* Math.pow(wingSpan.doubleValue(NonSI.FOOT),3)
									* Math.sqrt(maxTakeOffMass.doubleValue(NonSI.POUND)*maxZeroFuelMass.doubleValue(NonSI.POUND))
									* (1 + 2*taperRatio)
									)
							/(thicknessMean
									* Math.pow(Math.cos(sweepQuarterChord.doubleValue(SI.RADIAN)),2)
									* wingSurface.doubleValue(MyUnits.FOOT2)
									* (1 + taperRatio)
									)
							),
					NonSI.POUND).to(SI.KILOGRAM);
			
			//................................................................................................
			
			System.out.println("\t------------------------------------");
			System.out.println("\tINPUT: ");
			System.out.println("\tNumber of Design Variable : " + numberOfDesignVariables);
			System.out.println("\tDesign Variable 1 : " + Arrays.toString(var1Array));
			System.out.println("\tDesign Variable 2 : " + Arrays.toString(var2Array));
			System.out.println("\tDesign Variable 3 : " + Arrays.toString(var3Array));
			System.out.println("\tNumber of points semi-spanwise : " + numberOfPointsSemiSpanwise);
			System.out.println("\tAlpha Initial : " + alphaInitial);
			System.out.println("\tAlpha Final : " + alphaFinal);
			System.out.println("\tNumber of alphas : " + numberOfAlphas);

			if(costFunctionType.equals(OptimizationEnum.CL_MAX_VS_CD_MIN_WING) 
					|| costFunctionType.equals(OptimizationEnum.CL_MAX_VS_MASS_WING)) {
				System.out.println("\tObjective 1 weight : " + objectiveWeight1);
				System.out.println("\tObjective 2 weight : " + objectiveWeight2);
			
				if(costFunctionType.equals(OptimizationEnum.CL_MAX_VS_MASS_WING) 
						|| costFunctionType.equals(OptimizationEnum.WING_MASS)) {
					System.out.println("\tUltimate Load Factor : " + ultimateLoadFactor);
					System.out.println("\tMax Take-Off Mass : " + maxTakeOffMass);
					System.out.println("\tMax Zero Fuel Mass : " + maxZeroFuelMass);
				}
			}
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
						
						LiftingSurface currentWing = new LiftingSurface(LiftingSurfaceCreator.importFromXML(ComponentEnum.WING, pathToXML, dirAirfoil));
						currentWing.setAeroDatabaseReader(aeroDatabaseReader);
						currentWing.setHighLiftDatabaseReader(highLiftDatabaseReader);
						currentWing.setVeDSCDatabaseReader(veDSCDatabaseReader);
						
						currentWing.getLiftingSurfaceCreator().calculateGeometry(
								40,
								currentWing.getLiftingSurfaceCreator().getType(),
								currentWing.getLiftingSurfaceCreator().isMirrored());
						currentWing.getLiftingSurfaceCreator().populateAirfoilList(false);
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
						
						LSAerodynamicsManager theWingAerodynamicsManager = new LSAerodynamicsManager(
								currentWing,
								theOperatingConditions,
								theFlightCondition, 
								numberOfPointsSemiSpanwise, 
								MyArrayUtils.convertDoubleArrayToListOfAmount(
										MyArrayUtils.linspace(
												alphaInitial.doubleValue(NonSI.DEGREE_ANGLE), 
												alphaFinal.doubleValue(NonSI.DEGREE_ANGLE), 
												numberOfAlphas
												),
										NonSI.DEGREE_ANGLE),
								null, 
								null
								);
						
						switch (costFunctionType) {
						case CL_MAX_WING:
							// sign (-) for a minimization problem
							costFunctionValues[i][j][k] = CostFunctions.cLMaxWing(theWingAerodynamicsManager);
							break;
						case CL_MAX_VS_CD_MIN_WING:
							costFunctionValues[i][j][k] = CostFunctions.cLmaxVsCDminWing(
									objectiveWeight1,
									objectiveWeight2,
									cDMinBaseline,
									theWingAerodynamicsManager
									);
							break;
						case CD_MIN_WING:
							costFunctionValues[i][j][k] = CostFunctions.cDMinWing(theWingAerodynamicsManager);
							break;
						case CL_MAX_VS_MASS_WING:
							costFunctionValues[i][j][k] = CostFunctions.cLmaxVsMassWing(
									objectiveWeight1, 
									objectiveWeight2,
									wingMassBaseline.doubleValue(SI.KILOGRAM), 
									ultimateLoadFactor, 
									maxTakeOffMass,
									maxZeroFuelMass,
									theWingAerodynamicsManager
									);
							break;
						case WING_MASS:
							costFunctionValues[i][j][k] = CostFunctions.wingMass(
									ultimateLoadFactor,
									maxTakeOffMass, 
									maxZeroFuelMass, 
									theWingAerodynamicsManager
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
			
			// Initial Position
			System.out.println("\n\n\t------------------------------------");
			System.out.println("\tBASELINE DESIGN PARAMETERS: ");
			System.out.println("\t------------------------------------");
			System.out.println("\tAR = " + baselineWing.getLiftingSurfaceCreator().getAspectRatio());
			System.out.println("\tTaper Ratio = " + baselineWing.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio());
			System.out.println("\tSweep LE = " + baselineWing.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE));
			System.out.println("\t------------------------------------");
			// Best Position
			System.out.println("\n\n\t------------------------------------");
			System.out.println("\tBEST PARTICLE POSITION: ");
			System.out.println("\t------------------------------------");
			System.out.println("\tAR = " + pso.getBestPosition()[0]);
			System.out.println("\tTaper Ratio = " + pso.getBestPosition()[1]);
			System.out.println("\tSweep LE = " + pso.getBestPosition()[2] + "°");
			System.out.println("\t------------------------------------");
			
			//-------------------------------------------------------------------------------
			// DEFINING THE OPTIMIZED WING ...
			System.setOut(filterStream);
			optimizedWing = new LiftingSurface(LiftingSurfaceCreator.importFromXML(ComponentEnum.WING, pathToXML, dirAirfoil));
			optimizedWing.setAeroDatabaseReader(aeroDatabaseReader);
			optimizedWing.setHighLiftDatabaseReader(highLiftDatabaseReader);
			optimizedWing.setVeDSCDatabaseReader(veDSCDatabaseReader);
			optimizedWing.getLiftingSurfaceCreator().calculateGeometry(
					40,
					optimizedWing.getLiftingSurfaceCreator().getType(),
					optimizedWing.getLiftingSurfaceCreator().isMirrored());
			optimizedWing.getLiftingSurfaceCreator().populateAirfoilList(false);
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
			
			//-------------------------------------------------------------------------------
			System.out.println("\t------------------------------------");
			System.out.println("\tComparing baseline and optimized wing ... ");
			System.out.println("\t------------------------------------");
			System.setOut(filterStream);
			
			// OPTIMIZED WING OUTPUT
			LSAerodynamicsManager theOptimizedWingAerodynamicsManager = new LSAerodynamicsManager(
					optimizedWing,
					theOperatingConditions,
					theFlightCondition, 
					numberOfPointsSemiSpanwise, 
					MyArrayUtils.convertDoubleArrayToListOfAmount(
							MyArrayUtils.linspace(
									alphaInitial.doubleValue(NonSI.DEGREE_ANGLE), 
									alphaFinal.doubleValue(NonSI.DEGREE_ANGLE), 
									numberOfAlphas
									),
							NonSI.DEGREE_ANGLE),
					null, 
					null
					);
			
			if(costFunctionType.equals(OptimizationEnum.CL_MAX_WING) 
					|| costFunctionType.equals(OptimizationEnum.CL_MAX_VS_CD_MIN_WING)
					|| costFunctionType.equals(OptimizationEnum.CL_MAX_VS_MASS_WING)
					) {
				CalcCLmax calcCLmaxOptimized = theOptimizedWingAerodynamicsManager.new CalcCLmax();
				calcCLmaxOptimized.nasaBlackwell();
				cLmaxOptimized = theOptimizedWingAerodynamicsManager.getCLMax().get(MethodEnum.NASA_BLACKWELL);
			}
			if(costFunctionType.equals(OptimizationEnum.CD_MIN_WING) || costFunctionType.equals(OptimizationEnum.CL_MAX_VS_CD_MIN_WING)) {
				CalcPolar calcPolarOptimized = theOptimizedWingAerodynamicsManager.new CalcPolar();
				calcPolarOptimized.fromCdDistribution(
						theOptimizedWingAerodynamicsManager.getCurrentMachNumber(),
						theOptimizedWingAerodynamicsManager.getCurrentAltitude()
						);
				cDMinOptimized = MyArrayUtils.getMin(theOptimizedWingAerodynamicsManager.getPolar3DCurve().get(MethodEnum.AIRFOIL_DISTRIBUTION));
			}
			if(costFunctionType.equals(OptimizationEnum.CL_MAX_VS_MASS_WING) 
					|| costFunctionType.equals(OptimizationEnum.WING_MASS)) {
				Amount<Area> wingSurfaceOptimized = theOptimizedWingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getSurfacePlanform();
				Amount<Length> wingSpanOptimized = theOptimizedWingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getSpan();
				Double taperRatioOptimied = theOptimizedWingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio();
				Amount<Angle> sweepQuarterChordOptimized = theOptimizedWingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord();
				Double thicknessMeanOptimized = theOptimizedWingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getThicknessMean();
				
				wingMassOptimized = Amount.valueOf(
						(4.22*wingSurfaceOptimized.doubleValue(MyUnits.FOOT2) 
								+ 1.642e-6
								* (ultimateLoadFactor
										* Math.pow(wingSpanOptimized.doubleValue(NonSI.FOOT),3)
										* Math.sqrt(maxTakeOffMass.doubleValue(NonSI.POUND)*maxZeroFuelMass.doubleValue(NonSI.POUND))
										* (1 + 2*taperRatioOptimied)
										)
								/(thicknessMeanOptimized
										* Math.pow(Math.cos(sweepQuarterChordOptimized.doubleValue(SI.RADIAN)),2)
										* wingSurfaceOptimized.doubleValue(MyUnits.FOOT2)
										* (1 + taperRatioOptimied)
										)
								),
						NonSI.POUND).to(SI.KILOGRAM);
			}

			System.setOut(originalOut);
			
			if(cLmaxOptimized != null) {
				System.out.println("\tCLmax Baseline = " + cLmaxBaseline);
				System.out.println("\tCLmax Optimized = " + cLmaxOptimized);
			}
			if(cDMinOptimized != null) {
				System.out.println("\n\tCDmin Baseline = " + cDMinBaseline);
				System.out.println("\tCDmin Optimized = " + cDMinOptimized);
			}
			if(wingMassOptimized != null) {
				System.out.println("\n\tWing Mass Baseline = " + wingMassBaseline);
				System.out.println("\tWing Mass Optimized = " + wingMassOptimized);
			}
			System.out.println("\t------------------------------------");

			long estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println("\n\tTIME ESTIMATED = " + (estimatedTime/1000) + " seconds");
			

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