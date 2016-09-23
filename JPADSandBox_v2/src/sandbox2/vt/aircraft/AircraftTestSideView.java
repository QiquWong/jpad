package sandbox2.vt.aircraft;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
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
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sandbox2.javafx.D3Plotter;
import sandbox2.javafx.D3PlotterOptions;
import standaloneutils.JPADXmlReader;
import writers.JPADStaticWriteUtils;

public class AircraftTestSideView extends Application {

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

		LiftingSurface wing = AircraftTestSideView.theAircraft.getExposedWing();
		if (wing == null) {
			System.out.println("wing object null, returning.");
			return;
		}

		LiftingSurface hTail = AircraftTestSideView.theAircraft.getHTail();
		if (hTail == null) {
			System.out.println("horizontal tail object null, returning.");
			return;
		}
		
		LiftingSurface vTail = AircraftTestSideView.theAircraft.getVTail();
		if (vTail == null) {
			System.out.println("vertical tail object null, returning.");
			return;
		}
		
		Fuselage fuselage = AircraftTestSideView.theAircraft.getFuselage();
		if (fuselage == null) {
			System.out.println("fuselage object null, returning.");
			return;
		}

		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// upper curve, sideview
		List<Amount<Length>> vX1Upper = fuselage.getFuselageCreator().getOutlineXZUpperCurveAmountX();
		int nX1Upper = vX1Upper.size();
		List<Amount<Length>> vZ1Upper = fuselage.getFuselageCreator().getOutlineXZUpperCurveAmountZ();

		Double[][] dataOutlineXZUpperCurve = new Double[nX1Upper][2];
		IntStream.range(0, nX1Upper)
		.forEach(i -> {
			dataOutlineXZUpperCurve[i][0] = vX1Upper.get(i).doubleValue(SI.METRE);
			dataOutlineXZUpperCurve[i][1] = vZ1Upper.get(i).doubleValue(SI.METRE);
		});

		// lower curve, sideview
		List<Amount<Length>> vX2Lower = fuselage.getFuselageCreator().getOutlineXZLowerCurveAmountX();
		int nX2Lower = vX2Lower.size();
		List<Amount<Length>> vZ2Lower = fuselage.getFuselageCreator().getOutlineXZLowerCurveAmountZ();

		Double[][] dataOutlineXZLowerCurve = new Double[nX2Lower][2];
		IntStream.range(0, nX2Lower)
		.forEach(i -> {
			dataOutlineXZLowerCurve[i][0] = vX2Lower.get(i).doubleValue(SI.METRE);
			dataOutlineXZLowerCurve[i][1] = vZ2Lower.get(i).doubleValue(SI.METRE);
		});
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		Double[] wingRootXCoordinates = wing.getAirfoilList().get(0).getAirfoilCreator().getXCoords();
		Double[] wingRootZCoordinates = wing.getAirfoilList().get(0).getAirfoilCreator().getZCoords();
		Double[][] wingRootAirfoilPoints = new Double[wingRootXCoordinates.length][2];
		for (int i=0; i<wingRootAirfoilPoints.length; i++) {
			wingRootAirfoilPoints[i][0] = (wingRootXCoordinates[i]*wing.getChordRoot().getEstimatedValue()) 
										  + wing.getXApexConstructionAxes().getEstimatedValue(); 
			wingRootAirfoilPoints[i][1] = (wingRootZCoordinates[i]*wing.getChordRoot().getEstimatedValue())
										  + wing.getZApexConstructionAxes().getEstimatedValue();
		}
		
		int nPointsWing = wing.getLiftingSurfaceCreator().getDiscretizedXle().size();
		Double[] wingTipXCoordinates = wing.getAirfoilList().get(wing.getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
		Double[] wingTipZCoordinates = wing.getAirfoilList().get(wing.getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
		Double[][] wingTipAirfoilPoints = new Double[wingTipXCoordinates.length][2];
		for (int i=0; i<wingTipAirfoilPoints.length; i++) {
			wingTipAirfoilPoints[i][0] = (wingTipXCoordinates[i]*wing.getChordTip().getEstimatedValue()) 
										  + wing.getXApexConstructionAxes().getEstimatedValue()
										  + wing.getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsWing-1).getEstimatedValue(); 
			wingTipAirfoilPoints[i][1] = (wingTipZCoordinates[i]*wing.getChordTip().getEstimatedValue())
										  + wing.getZApexConstructionAxes().getEstimatedValue();
		}
		
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		Double[] hTailRootXCoordinates = hTail.getAirfoilList().get(0).getAirfoilCreator().getXCoords();
		Double[] hTailRootZCoordinates = hTail.getAirfoilList().get(0).getAirfoilCreator().getZCoords();
		Double[][] hTailRootAirfoilPoints = new Double[hTailRootXCoordinates.length][2];
		for (int i=0; i<hTailRootAirfoilPoints.length; i++) {
			hTailRootAirfoilPoints[i][0] = (hTailRootXCoordinates[i]*hTail.getChordRoot().getEstimatedValue())
										   + hTail.getXApexConstructionAxes().getEstimatedValue(); 
			hTailRootAirfoilPoints[i][1] = (hTailRootZCoordinates[i]*hTail.getChordRoot().getEstimatedValue())
										   + hTail.getZApexConstructionAxes().getEstimatedValue();
		}
		
		int nPointsHTail = hTail.getLiftingSurfaceCreator().getDiscretizedXle().size();
		Double[] hTailTipXCoordinates = hTail.getAirfoilList().get(hTail.getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
		Double[] hTailTipZCoordinates = hTail.getAirfoilList().get(hTail.getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
		Double[][] hTailTipAirfoilPoints = new Double[hTailTipXCoordinates.length][2];
		for (int i=0; i<hTailTipAirfoilPoints.length; i++) {
			hTailTipAirfoilPoints[i][0] = (hTailTipXCoordinates[i]*hTail.getChordTip().getEstimatedValue()) 
										  + hTail.getXApexConstructionAxes().getEstimatedValue()
										  + hTail.getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(); 
			hTailTipAirfoilPoints[i][1] = (hTailTipZCoordinates[i]*hTail.getChordTip().getEstimatedValue())
										  + hTail.getZApexConstructionAxes().getEstimatedValue();
		}
		
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		List<Amount<Length>> vYVTail = vTail.getLiftingSurfaceCreator().getDiscretizedYs();
		int nYVTail = vYVTail.size();
		List<Amount<Length>> vChordsVTail = vTail.getLiftingSurfaceCreator().getDiscretizedChords();
		List<Amount<Length>> vXleVTail = vTail.getLiftingSurfaceCreator().getDiscretizedXle();

		Double[][] dataChordsVsYVTail = new Double[nYVTail][2];
		Double[][] dataXleVsYVTail = new Double[nYVTail][2];
		IntStream.range(0, nYVTail)
		.forEach(i -> {
			dataChordsVsYVTail[i][0] = vYVTail.get(i).doubleValue(SI.METRE);
			dataChordsVsYVTail[i][1] = vChordsVTail.get(i).doubleValue(SI.METRE);
			dataXleVsYVTail[i][0] = vYVTail.get(i).doubleValue(SI.METRE);
			dataXleVsYVTail[i][1] = vXleVTail.get(i).doubleValue(SI.METRE);
		});

		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<Double[][]> nacellePointsList = new ArrayList<Double[][]>();
		
		for(int i=0; i<theAircraft.getNacelles().getNacellesList().size(); i++) {
			
			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = theAircraft.getNacelles().getNacellesList().get(i).getXCoordinatesOutline();
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperZ = theAircraft.getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZUpper();
			
			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerZ = theAircraft.getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZLower();

			List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
			List<Amount<Length>> dataOutlineXZCurveNacelleZ = new ArrayList<>();
			
			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j)
						.plus(theAircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(j)
						.plus(theAircraft.getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
			}
			
			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1) 
						.plus(theAircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveLowerZ.get(nacelleCurveXPoints-j-1)
						.plus(theAircraft.getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
			}
			
			dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0)
					.plus(theAircraft.getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
			dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(0)
					.plus(theAircraft.getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
			
			
			Double[][] dataOutlineXZCurveNacelle = new Double[dataOutlineXZCurveNacelleX.size()][2];
			for(int j=0; j<dataOutlineXZCurveNacelleX.size(); j++) {
				dataOutlineXZCurveNacelle[j][0] = dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER);
				dataOutlineXZCurveNacelle[j][1] = dataOutlineXZCurveNacelleZ.get(j).doubleValue(SI.METER);
			}
			
			nacellePointsList.add(dataOutlineXZCurveNacelle);
			
		}
		
		System.out.println("##################\n\n");

		Double[][] dataTopViewVTail = vTail.getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);
		for(int i=0; i<dataTopViewVTail.length; i++){
			dataTopViewVTail[i][0] += vTail.getXApexConstructionAxes().getEstimatedValue();
			dataTopViewVTail[i][1] += vTail.getZApexConstructionAxes().getEstimatedValue();
		}
		
		//--------------------------------------------------
		System.out.println("Initializing test class...");
		String rootOutputFolderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String outputFolderPath = JPADStaticWriteUtils.createNewFolder(rootOutputFolderPath + "Tests_Aircraft" + File.separator);
		System.out.println("Output ==> " + outputFolderPath);

		//--------------------------------------------------
		//set state title
		primaryStage.setTitle("treez/javafx-d3 - D3Plotter demo");

		List<Double[][]> listDataArraySideView = new ArrayList<Double[][]>();

		// vTail
		listDataArraySideView.add(dataTopViewVTail);
		// fuselage
		listDataArraySideView.add(dataOutlineXZUpperCurve);
		listDataArraySideView.add(dataOutlineXZLowerCurve);
		// wing
		listDataArraySideView.add(wingRootAirfoilPoints);
		listDataArraySideView.add(wingTipAirfoilPoints);
		// hTail
		listDataArraySideView.add(hTailRootAirfoilPoints);
		listDataArraySideView.add(hTailTipAirfoilPoints);
		// nacelles
		for (int i=0; i<nacellePointsList.size(); i++)
			listDataArraySideView.add(nacellePointsList.get(i));
		
		double xMaxSideView = 1.20*fuselage.getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double xMinSideView = -0.20*fuselage.getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMaxSideView = 1.40*fuselage.getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double yMinSideView = -1.40*fuselage.getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		
		D3PlotterOptions optionsSideView = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(xMinSideView, xMaxSideView)
				.yRange(yMinSideView, yMaxSideView)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
				.title("Aircraft data representation - Side View")
				.xLabel("x (m)")
				.yLabel("z (m)")
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
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				.symbolSizes(2,2,2,2,2,2,2,2,2,2,2,2,2)
				.showSymbols(false,false,false,false,false,false,false,false,false,false,false,false,false) // NOTE: overloaded function
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
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2"
						)
				.plotAreas(true,true,true,true,true,true,true,true,true,true,true,true,true)
				.areaStyles("fill:yellow;","fill:white;","fill:white;","fill:lightblue;","fill:lightblue;","fill:blue;","fill:blue;",
						"fill:orange;","fill:orange;","fill:orange;","fill:orange;","fill:orange;","fill:orange;")
				.areaOpacities(1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0)
				.showLegend(false)
				.build();

//		System.out.println("Plot options:\n" + optionsSideView);

		d3Plotter = new D3Plotter(
				optionsSideView,
				listDataArraySideView
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
			String outputFilePathSideView = outputFolderPath + "AircraftSideView.svg";
			d3Plotter.saveSVG(outputFilePathSideView);

		}; // end-of-Runnable

		// create the Browser/D3
		//create browser
		JavaFxD3Browser browserSideView = d3Plotter.getBrowser(postLoadingHook, false);

		//create the scene
		Scene sceneSideView = new Scene(browserSideView, WIDTH+10, HEIGHT+10, Color.web("#666970"));
		primaryStage.setScene(sceneSideView);
		
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
		AircraftTestSideView.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			AircraftTestSideView.theCmdLineParser.parseArgument(args);
			
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
			
			String dirSystems = va.getSystemsDirectory().getCanonicalPath();
			System.out.println("SYSTEMS ===> " + dirSystems);
			
			String dirCabinConfiguration = va.getCabinConfigurationDirectory().getCanonicalPath();
			System.out.println("CABIN CONFIGURATIONS ===> " + dirCabinConfiguration);
			
			System.out.println("--------------");

			//------------------------------------------------------------------------------------
			// Setup database(s)
			MyConfiguration.initWorkingDirectoryTree();
			
			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
			AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
			HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
			
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
					dirSystems,
					dirCabinConfiguration,
					dirAirfoil,
					aeroDatabaseReader,
					highLiftDatabaseReader);
			
			System.out.println("The Aircaraft ...");
			System.out.println(AircraftTestSideView.theAircraft.toString());
			
		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			AircraftTestSideView.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ...
		launch(args);
	}

}
