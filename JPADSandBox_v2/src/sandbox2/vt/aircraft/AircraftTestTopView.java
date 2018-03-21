package sandbox2.vt.aircraft;

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

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
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

class MyArgumentsAircraft {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	@Option(name = "-da", aliases = { "--dir-airfoils" }, required = true,
			usage = "airfoil directory path")
	private File _airfoilDirectory;

	@Option(name = "-df", aliases = { "--dir-fuselages" }, required = true,
			usage = "fuselages directory path")
	private File _fuselagesDirectory;
	
	@Option(name = "-dls", aliases = { "--dir-lifting-surfaces" }, required = true,
			usage = "lifting surfaces directory path")
	private File _liftingSurfacesDirectory;
	
	@Option(name = "-de", aliases = { "--dir-engines" }, required = true,
			usage = "engines directory path")
	private File _enginesDirectory;
	
	@Option(name = "-dn", aliases = { "--dir-nacelles" }, required = true,
			usage = "nacelles directory path")
	private File _nacellesDirectory;
	
	@Option(name = "-dlg", aliases = { "--dir-landing-gears" }, required = true,
			usage = "landing gears directory path")
	private File _landingGearsDirectory;
	
	@Option(name = "-dcc", aliases = { "--dir-cabin-configurations" }, required = true,
			usage = "cabin configurations directory path")
	private File _cabinConfigurationsDirectory;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}

	public File getAirfoilDirectory() {
		return _airfoilDirectory;
	}

	public File getFuselagesDirectory() {
		return _fuselagesDirectory;
	}
	
	public File getLiftingSurfacesDirectory() {
		return _liftingSurfacesDirectory;
	}

	public File getEnginesDirectory() {
		return _enginesDirectory;
	}
	
	public File getNacellesDirectory() {
		return _nacellesDirectory;
	}
	
	public File getLandingGearsDirectory() {
		return _landingGearsDirectory;
	}

	public File getCabinConfigurationDirectory() {
		return _cabinConfigurationsDirectory;
	}
}
	
public class AircraftTestTopView extends Application {

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

		Fuselage fuselage = AircraftTestTopView.theAircraft.getFuselage();
		if (fuselage == null) {
			System.out.println("fuselage object null, returning.");
			return;
		}
		
		LiftingSurface wing = AircraftTestTopView.theAircraft.getExposedWing();
		if (wing == null) {
			System.out.println("wing object null, returning.");
			return;
		}

		LiftingSurface hTail = AircraftTestTopView.theAircraft.getHTail();
		if (hTail == null) {
			System.out.println("horizontal tail object null, returning.");
			return;
		}
		
		LiftingSurface vTail = AircraftTestTopView.theAircraft.getVTail();
		if (vTail == null) {
			System.out.println("vertical tail object null, returning.");
			return;
		}

		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// left curve, upperview
		List<Amount<Length>> vX1Left = fuselage.getFuselageCreator().getOutlineXYSideLCurveAmountX();
		int nX1Left = vX1Left.size();
		List<Amount<Length>> vY1Left = fuselage.getFuselageCreator().getOutlineXYSideLCurveAmountY();

		Double[][] dataOutlineXYLeftCurve = new Double[nX1Left][2];
		IntStream.range(0, nX1Left)
		.forEach(i -> {
			dataOutlineXYLeftCurve[i][1] = vX1Left.get(i).doubleValue(SI.METRE);
			dataOutlineXYLeftCurve[i][0] = vY1Left.get(i).doubleValue(SI.METRE);
		});

		// right curve, upperview
		List<Amount<Length>> vX2Right = fuselage.getFuselageCreator().getOutlineXYSideRCurveAmountX();
		int nX2Right = vX2Right.size();
		List<Amount<Length>> vY2Right = fuselage.getFuselageCreator().getOutlineXYSideRCurveAmountY();

		Double[][] dataOutlineXYRightCurve = new Double[nX2Right][2];
		IntStream.range(0, nX2Right)
		.forEach(i -> {
			dataOutlineXYRightCurve[i][1] = vX2Right.get(i).doubleValue(SI.METRE);
			dataOutlineXYRightCurve[i][0] = vY2Right.get(i).doubleValue(SI.METRE);
		});

		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
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

		Double[][] dataTopViewIsolated = wing.getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		
		Double[][] dataTopView = new Double[dataTopViewIsolated.length][dataTopViewIsolated[0].length];
		for (int i=0; i<dataTopViewIsolated.length; i++) { 
			dataTopView[i][0] = dataTopViewIsolated[i][0] + wing.getYApexConstructionAxes().doubleValue(SI.METER);
			dataTopView[i][1] = dataTopViewIsolated[i][1] + wing.getXApexConstructionAxes().doubleValue(SI.METER);
		}
		
		Double[][] dataTopViewMirrored = new Double[dataTopView.length][dataTopView[0].length];
		for (int i=0; i<dataTopView.length; i++) { 
				dataTopViewMirrored[i][0] = -dataTopView[i][0];
				dataTopViewMirrored[i][1] = dataTopView[i][1];
		}

		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
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

		Double[][] dataTopViewIsolatedHTail = hTail.getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.HORIZONTAL_TAIL);

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
		// get data vectors from vTail discretization
		//--------------------------------------------------
		double[] vTailRootXCoordinates = vTail.getLiftingSurfaceCreator().getAirfoilList().get(0).getXCoords();
		double[] vTailRootYCoordinates = vTail.getLiftingSurfaceCreator().getAirfoilList().get(0).getZCoords();
		Double[][] vTailRootAirfoilPoints = new Double[vTailRootXCoordinates.length][2];
		for (int i=0; i<vTailRootAirfoilPoints.length; i++) {
			vTailRootAirfoilPoints[i][1] = (vTailRootXCoordinates[i]*vTail.getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().getEstimatedValue()) + vTail.getXApexConstructionAxes().getEstimatedValue(); 
			vTailRootAirfoilPoints[i][0] = (vTailRootYCoordinates[i]*vTail.getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().getEstimatedValue());
		}
		
		int nPointsVTail = vTail.getLiftingSurfaceCreator().getDiscretizedXle().size();
		double[] vTailTipXCoordinates = vTail.getLiftingSurfaceCreator().getAirfoilList().get(vTail.getLiftingSurfaceCreator().getAirfoilList().size()-1).getXCoords();
		double[] vTailTipYCoordinates = vTail.getLiftingSurfaceCreator().getAirfoilList().get(vTail.getLiftingSurfaceCreator().getAirfoilList().size()-1).getZCoords();
		Double[][] vTailTipAirfoilPoints = new Double[vTailTipXCoordinates.length][2];
		for (int i=0; i<vTailTipAirfoilPoints.length; i++) {
			vTailTipAirfoilPoints[i][1] = (vTailTipXCoordinates[i]*vTail.getLiftingSurfaceCreator().getPanels()
					.get(vTail.getLiftingSurfaceCreator().getPanels().size()-1).getChordTip().getEstimatedValue()) 
										  + vTail.getXApexConstructionAxes().getEstimatedValue()
										  + vTail.getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsVTail-1).getEstimatedValue(); 
			vTailTipAirfoilPoints[i][0] = (vTailTipYCoordinates[i]*vTail.getLiftingSurfaceCreator().getPanels()
					.get(vTail.getLiftingSurfaceCreator().getPanels().size()-1).getChordTip().getEstimatedValue());
		}
		
		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<Double[][]> nacellePointsList = new ArrayList<Double[][]>();
		
		for(int i=0; i<theAircraft.getNacelles().getNacellesList().size(); i++) {
			
			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = theAircraft.getNacelles().getNacellesList().get(i).getXCoordinatesOutline();
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperY = theAircraft.getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYRight();
			
			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerY = theAircraft.getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYLeft();

			List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
			List<Amount<Length>> dataOutlineXZCurveNacelleY = new ArrayList<>();
			
			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j)
						.plus(theAircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(j)
						.plus(theAircraft.getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));
			}
			
			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1) 
						.plus(theAircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleY.add(nacelleCurveLowerY.get(nacelleCurveXPoints-j-1)
						.plus(theAircraft.getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));
			}
			
			dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0)
					.plus(theAircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
			dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(0)
					.plus(theAircraft.getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));
			
			
			Double[][] dataOutlineXZCurveNacelle = new Double[dataOutlineXZCurveNacelleX.size()][2];
			for(int j=0; j<dataOutlineXZCurveNacelleX.size(); j++) {
				dataOutlineXZCurveNacelle[j][1] = dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER);
				dataOutlineXZCurveNacelle[j][0] = dataOutlineXZCurveNacelleY.get(j).doubleValue(SI.METER);
			}
			
			nacellePointsList.add(dataOutlineXZCurveNacelle);
			
		}
		
		//--------------------------------------------------
		System.out.println("Initializing test class...");
		String rootOutputFolderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String outputFolderPath = JPADStaticWriteUtils.createNewFolder(rootOutputFolderPath + "Tests_Aircraft" + File.separator);
		System.out.println("Output ==> " + outputFolderPath);

		//--------------------------------------------------
		//set state title
		primaryStage.setTitle("treez/javafx-d3 - D3Plotter demo");

		List<Double[][]> listDataArrayTopView = new ArrayList<Double[][]>();

		// wing
		listDataArrayTopView.add(dataTopView);
		listDataArrayTopView.add(dataTopViewMirrored);
		// hTail
		listDataArrayTopView.add(dataTopViewHTail);
		listDataArrayTopView.add(dataTopViewMirroredHTail);
		// fuselage
		listDataArrayTopView.add(dataOutlineXYLeftCurve);
		listDataArrayTopView.add(dataOutlineXYRightCurve);
		// vTail
		listDataArrayTopView.add(vTailRootAirfoilPoints);
		listDataArrayTopView.add(vTailTipAirfoilPoints);
		// nacelles
		for (int i=0; i<nacellePointsList.size(); i++)
			listDataArrayTopView.add(nacellePointsList.get(i));

		double xMaxTopView = 1.40*fuselage.getFuselageCreator().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -1.40*fuselage.getFuselageCreator().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = 1.20*fuselage.getFuselageCreator().getFuselageLength().doubleValue(SI.METRE);
		double yMinTopView = -0.20*fuselage.getFuselageCreator().getFuselageLength().doubleValue(SI.METRE);
			
		D3PlotterOptions optionsTopView = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(xMinTopView, xMaxTopView)
				.yRange(yMaxTopView, yMinTopView)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
				.title("Aircraft data representation - Top View")
				.xLabel("y (m)")
				.yLabel("x (m)")
				.showXGrid(true)
				.showYGrid(true)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				.symbolSizes(2,2,2,2,2,2,2,2,2,2,2,2)
				.showSymbols(false,false,false,false,false,false,false,false,false,false,false,false) // NOTE: overloaded function
				.symbolStyles(
						"fill:blue; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2"
						)
				.lineStyles(
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2"
						)
				.plotAreas(true,true,true,true,true,true,true,true,true,true,true)
				.areaStyles("fill:lightblue;","fill:lightblue;","fill:blue;","fill:blue;","fill:white;","fill:white;",
						"fill:yellow;","fill:yellow;","fill:orange;","fill:orange;")
				.areaOpacities(1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0)
				.showLegend(false)
				.build();

//		System.out.println("Plot options:\n" + optionsTopView);

		d3Plotter = new D3Plotter(
				optionsTopView,
				listDataArrayTopView
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
			String outputFilePathTopView = outputFolderPath + "AircraftTopView.svg";
			d3Plotter.saveSVG(outputFilePathTopView);


		}; // end-of-Runnable

		// create the Browser/D3
		//create browser
		JavaFxD3Browser browserTopView = d3Plotter.getBrowser(postLoadingHook, false);

		//create the scene
		Scene sceneTopView = new Scene(browserTopView, WIDTH+10, HEIGHT+10, Color.web("#666970"));
		primaryStage.setScene(sceneTopView);

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

		System.out.println("-------------------");
		System.out.println("Aircraft test / D3 ");
		System.out.println("-------------------");

		MyArgumentsAircraft va = new MyArgumentsAircraft();
		AircraftTestTopView.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			AircraftTestTopView.theCmdLineParser.parseArgument(args);
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToXML);

			String dirAirfoil = va.getAirfoilDirectory().getCanonicalPath();
			System.out.println("AIRFOILS ===> " + dirAirfoil);

			String dirFuselages = va.getFuselagesDirectory().getCanonicalPath();
			System.out.println("FUSELAGES ===> " + dirFuselages);
			
			String dirLiftingSurfaces = va.getLiftingSurfacesDirectory().getCanonicalPath();
			System.out.println("LIFTING SURFACES ===> " + dirLiftingSurfaces);
			
			String dirEngines = va.getEnginesDirectory().getCanonicalPath();
			System.out.println("ENGINES ===> " + dirEngines);
			
			String dirNacelles = va.getNacellesDirectory().getCanonicalPath();
			System.out.println("NACELLES ===> " + dirNacelles);
			
			String dirLandingGears = va.getLandingGearsDirectory().getCanonicalPath();
			System.out.println("LANDING GEARS ===> " + dirLandingGears);
			
			String dirCabinConfiguration = va.getCabinConfigurationDirectory().getCanonicalPath();
			System.out.println("CABIN CONFIGURATIONS ===> " + dirCabinConfiguration);
			
			System.out.println("--------------");

			//------------------------------------------------------------------------------------
			// Setup database(s)
			MyConfiguration.initWorkingDirectoryTree();
			
			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
			String fusDesDatabaseFilename = "FusDes_database.h5";
			String vedscDatabaseFilename = "VeDSC_database.h5";
			AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
			HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
			FusDesDatabaseReader fusDesDatabaseReader = new FusDesDatabaseReader(databaseFolderPath, fusDesDatabaseFilename);
			VeDSCDatabaseReader veDSCDatabaseReader = new VeDSCDatabaseReader(databaseFolderPath, vedscDatabaseFilename);
			
			// default Aircraft ATR-72 ...
//			theAircraft = new Aircraft.AircraftBuilder(
//					"ATR-72",
//					AircraftEnum.ATR72,
//					aeroDatabaseReader,
//					highLiftDatabaseReader
//					).build();

			// reading aircraft from xml ...
			theAircraft = Aircraft.importFromXML(
					pathToXML,
					dirLiftingSurfaces,
					dirFuselages,
					dirEngines,
					dirNacelles,
					dirLandingGears,
					dirCabinConfiguration,
					dirAirfoil,
					aeroDatabaseReader,
					highLiftDatabaseReader,
					fusDesDatabaseReader,
					veDSCDatabaseReader);
			
			System.out.println("The Aircaraft ...");
			System.out.println(AircraftTestTopView.theAircraft.toString());
			
		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			AircraftTestTopView.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ...
		launch(args);
	}

}