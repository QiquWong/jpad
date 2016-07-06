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

import com.sun.org.apache.xml.internal.utils.ThreadControllerWrapper;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.EngineTypeEnum;
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
import standaloneutils.MyArrayUtils;
import writers.JPADStaticWriteUtils;

public class AircraftTestFrontView extends Application {

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

		LiftingSurface wing = AircraftTestFrontView.theAircraft.getExposedWing();
		if (wing == null) {
			System.out.println("wing object null, returning.");
			return;
		}

		LiftingSurface hTail = AircraftTestFrontView.theAircraft.getHTail();
		if (hTail == null) {
			System.out.println("horizontal tail object null, returning.");
			return;
		}
		
		LiftingSurface vTail = AircraftTestFrontView.theAircraft.getVTail();
		if (vTail == null) {
			System.out.println("vertical tail object null, returning.");
			return;
		}
		
		Fuselage fuselage = AircraftTestFrontView.theAircraft.getFuselage();
		if (fuselage == null) {
			System.out.println("fuselage object null, returning.");
			return;
		}

		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// section upper curve
		List<Amount<Length>> vY1Upper = fuselage.getFuselageCreator().getSectionUpperCurveAmountY();
		int nY1Upper = vY1Upper.size();
		List<Amount<Length>> vZ1Upper = fuselage.getFuselageCreator().getSectionUpperCurveAmountZ();

		Double[][] dataSectionYZUpperCurve = new Double[nY1Upper][2];
		IntStream.range(0, nY1Upper)
		.forEach(i -> {
			dataSectionYZUpperCurve[i][0] = vY1Upper.get(i).doubleValue(SI.METRE);
			dataSectionYZUpperCurve[i][1] = vZ1Upper.get(i).doubleValue(SI.METRE);
		});

		// section lower curve
		List<Amount<Length>> vY2Lower = fuselage.getFuselageCreator().getSectionLowerCurveAmountY();
		int nY2Lower = vY2Lower.size();
		List<Amount<Length>> vZ2Lower = fuselage.getFuselageCreator().getSectionLowerCurveAmountZ();

		Double[][] dataSectionYZLowerCurve = new Double[nY2Lower][2];
		IntStream.range(0, nY2Lower)
		.forEach(i -> {
			dataSectionYZLowerCurve[i][0] = vY2Lower.get(i).doubleValue(SI.METRE);
			dataSectionYZLowerCurve[i][1] = vZ2Lower.get(i).doubleValue(SI.METRE);
		});
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		List<Amount<Length>> wingBreakPointsYCoordinates = wing.getLiftingSurfaceCreator().getYBreakPoints();
		int nYPointsWingTemp = wingBreakPointsYCoordinates.size();
		for(int i=0; i<nYPointsWingTemp; i++)
			wingBreakPointsYCoordinates.add(wing.getLiftingSurfaceCreator().getYBreakPoints().get(nYPointsWingTemp-i-1));
		int nYPointsWing = wingBreakPointsYCoordinates.size();
		
		List<Amount<Length>> wingThicknessZCoordinates = new ArrayList<Amount<Length>>();
		for(int i=0; i<wing.getAirfoilList().size(); i++)
			wingThicknessZCoordinates.add(
					Amount.valueOf(
							wing.getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
							MyArrayUtils.getMax(wing.getAirfoilList().get(i).getGeometry().get_zCoords()),
							SI.METER
							)
					);
		for(int i=0; i<nYPointsWingTemp; i++)
			wingThicknessZCoordinates.add(
					Amount.valueOf(
							wing.getLiftingSurfaceCreator().getChordsBreakPoints().get(nYPointsWingTemp-i-1).doubleValue(SI.METER)*
							MyArrayUtils.getMin(wing.getAirfoilList().get(nYPointsWingTemp-i-1).getGeometry().get_zCoords()),
							SI.METER
							)
					);
		
		Double[][] dataFrontViewWing = new Double[nYPointsWing][2];
		IntStream.range(0, nYPointsWing)
		.forEach(i -> {
			dataFrontViewWing[i][0] = wingBreakPointsYCoordinates.get(i).plus(wing.getYApexConstructionAxes()).doubleValue(SI.METRE);
			dataFrontViewWing[i][1] = wingThicknessZCoordinates.get(i).plus(wing.getZApexConstructionAxes()).doubleValue(SI.METRE);
		});
		
		Double[][] dataFrontViewWingMirrored = new Double[nYPointsWing][2];
		IntStream.range(0, nYPointsWing)
		.forEach(i -> {
			dataFrontViewWingMirrored[i][0] = -dataFrontViewWing[i][0];
			dataFrontViewWingMirrored[i][1] = dataFrontViewWing[i][1];
		});
		
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		List<Amount<Length>> hTailBreakPointsYCoordinates = hTail.getLiftingSurfaceCreator().getYBreakPoints();
		int nYPointsHTailTemp = hTailBreakPointsYCoordinates.size();
		for(int i=0; i<nYPointsHTailTemp; i++)
			hTailBreakPointsYCoordinates.add(hTail.getLiftingSurfaceCreator().getYBreakPoints().get(nYPointsHTailTemp-i-1));
		int nYPointsHTail = hTailBreakPointsYCoordinates.size();
		
		List<Amount<Length>> hTailThicknessZCoordinates = new ArrayList<Amount<Length>>();
		for(int i=0; i<vTail.getAirfoilList().size(); i++)
			hTailThicknessZCoordinates.add(
					Amount.valueOf(
							hTail.getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
							MyArrayUtils.getMax(hTail.getAirfoilList().get(i).getGeometry().get_zCoords()),
							SI.METER
							)
					);
		for(int i=0; i<nYPointsHTailTemp; i++)
			hTailThicknessZCoordinates.add(
					Amount.valueOf(
							hTail.getLiftingSurfaceCreator().getChordsBreakPoints().get(nYPointsHTailTemp-i-1).doubleValue(SI.METER)*
							MyArrayUtils.getMin(hTail.getAirfoilList().get(nYPointsHTailTemp-i-1).getGeometry().get_zCoords()),
							SI.METER
							)
					);
		
		Double[][] dataFrontViewHTail = new Double[nYPointsHTail][2];
		IntStream.range(0, nYPointsHTail)
		.forEach(i -> {
			dataFrontViewHTail[i][0] = hTailBreakPointsYCoordinates.get(i).plus(hTail.getYApexConstructionAxes()).doubleValue(SI.METRE);
			dataFrontViewHTail[i][1] = hTailThicknessZCoordinates.get(i).plus(hTail.getZApexConstructionAxes()).doubleValue(SI.METRE);
		});
		
		Double[][] dataFrontViewHTailMirrored = new Double[nYPointsHTail][2];
		IntStream.range(0, nYPointsHTail)
		.forEach(i -> {
			dataFrontViewHTailMirrored[i][0] = -dataFrontViewHTail[i][0];
			dataFrontViewHTailMirrored[i][1] = dataFrontViewHTail[i][1];
		});
		
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		List<Amount<Length>> vTailBreakPointsYCoordinates = vTail.getLiftingSurfaceCreator().getYBreakPoints();
		int nYPointsVTailTemp = vTailBreakPointsYCoordinates.size();
		for(int i=0; i<nYPointsVTailTemp; i++)
			vTailBreakPointsYCoordinates.add(vTail.getLiftingSurfaceCreator().getYBreakPoints().get(nYPointsVTailTemp-i-1));
		int nYPointsVTail = vTailBreakPointsYCoordinates.size();
		
		List<Amount<Length>> vTailThicknessZCoordinates = new ArrayList<Amount<Length>>();
		for(int i=0; i<vTail.getAirfoilList().size(); i++)
			vTailThicknessZCoordinates.add(
					Amount.valueOf(
							vTail.getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
							MyArrayUtils.getMax(vTail.getAirfoilList().get(i).getGeometry().get_zCoords()),
							SI.METER
							)
					);
		for(int i=0; i<nYPointsVTailTemp; i++)
			vTailThicknessZCoordinates.add(
					Amount.valueOf(
							vTail.getLiftingSurfaceCreator().getChordsBreakPoints().get(nYPointsVTailTemp-i-1).doubleValue(SI.METER)*
							MyArrayUtils.getMin(vTail.getAirfoilList().get(nYPointsVTailTemp-i-1).getGeometry().get_zCoords()),
							SI.METER
							)
					);
		
		Double[][] dataFrontViewVTail = new Double[nYPointsVTail][2];
		IntStream.range(0, nYPointsVTail)
		.forEach(i -> {
			dataFrontViewVTail[i][0] = vTailThicknessZCoordinates.get(i).plus(vTail.getYApexConstructionAxes()).doubleValue(SI.METRE);
			dataFrontViewVTail[i][1] = vTailBreakPointsYCoordinates.get(i).plus(vTail.getZApexConstructionAxes()).doubleValue(SI.METRE);
		});
		
		//--------------------------------------------------
		// get data vectors from engine discretization
		//--------------------------------------------------
		List<Double[][]> enginePointsList = new ArrayList<Double[][]>();
		
		if((theAircraft.getPowerPlant().getEngineType() == EngineTypeEnum.TURBOJET)
				|| (theAircraft.getPowerPlant().getEngineType() == EngineTypeEnum.TURBOFAN)) {
			
			for(int i=0; i<theAircraft.getPowerPlant().getEngineList().size(); i++) {
				
				double[] angleArray = MyArrayUtils.linspace(0.0, 2*Math.PI, 20);
				double[] yCoordinate = new double[angleArray.length];
				double[] zCoordinate = new double[angleArray.length];
				
				double radius = theAircraft.getPowerPlant()
							.getEngineList().get(i)
								.getDiameter()
									.divide(2)
										.doubleValue(SI.METER);
				double y0 = theAircraft.getPowerPlant()
							.getEngineList().get(i)
								.getYApexConstructionAxes()
									.doubleValue(SI.METER);
				
				double z0 = theAircraft.getPowerPlant()
						.getEngineList().get(i)
							.getZApexConstructionAxes()
								.doubleValue(SI.METER);
				
				for(int j=0; j<angleArray.length; j++) {
					yCoordinate[j] = radius*Math.cos(angleArray[j]);
					zCoordinate[j] = radius*Math.sin(angleArray[j]);
				}
				
				Double[][] enginePoints = new Double[yCoordinate.length][2];
				for (int j=0; j<yCoordinate.length; j++) {
					enginePoints[j][0] = yCoordinate[j] + y0;
					enginePoints[j][1] = zCoordinate[j] + z0;
				}
				
				enginePointsList.add(enginePoints);
				
			}
		}
		else if((theAircraft.getPowerPlant().getEngineType() == EngineTypeEnum.TURBOPROP)
				|| (theAircraft.getPowerPlant().getEngineType() == EngineTypeEnum.PISTON)) {
			
			for(int i=0; i<theAircraft.getPowerPlant().getEngineList().size(); i++) {
				Double[][] enginePoints = new Double[5][2];
				enginePoints[0][0] = theAircraft.getPowerPlant().getEngineList().get(i).getYApexConstructionAxes()
						.plus(theAircraft.getPowerPlant().getEngineList().get(i).getWidth().divide(2)).getEstimatedValue();
				enginePoints[0][1] = theAircraft.getPowerPlant().getEngineList().get(i).getZApexConstructionAxes()
						.minus(theAircraft.getPowerPlant().getEngineList().get(i).getHeight().divide(2)).getEstimatedValue();
				enginePoints[1][0] = theAircraft.getPowerPlant().getEngineList().get(i).getYApexConstructionAxes()
						.plus(theAircraft.getPowerPlant().getEngineList().get(i).getWidth().divide(2)).getEstimatedValue();
				enginePoints[1][1] = theAircraft.getPowerPlant().getEngineList().get(i).getZApexConstructionAxes()
						.plus(theAircraft.getPowerPlant().getEngineList().get(i).getHeight().divide(2)).getEstimatedValue();
				enginePoints[2][0] = theAircraft.getPowerPlant().getEngineList().get(i).getYApexConstructionAxes()
						.minus(theAircraft.getPowerPlant().getEngineList().get(i).getWidth().divide(2)).getEstimatedValue();
				enginePoints[2][1] = theAircraft.getPowerPlant().getEngineList().get(i).getZApexConstructionAxes()
						.plus(theAircraft.getPowerPlant().getEngineList().get(i).getHeight().divide(2)).getEstimatedValue();
				enginePoints[3][0] = theAircraft.getPowerPlant().getEngineList().get(i).getYApexConstructionAxes()
						.minus(theAircraft.getPowerPlant().getEngineList().get(i).getWidth().divide(2)).getEstimatedValue();
				enginePoints[3][1] = theAircraft.getPowerPlant().getEngineList().get(i).getZApexConstructionAxes()
						.minus(theAircraft.getPowerPlant().getEngineList().get(i).getHeight().divide(2)).getEstimatedValue();
				enginePoints[4][0] = theAircraft.getPowerPlant().getEngineList().get(i).getYApexConstructionAxes()
						.plus(theAircraft.getPowerPlant().getEngineList().get(i).getWidth().divide(2)).getEstimatedValue();
				enginePoints[4][1] = theAircraft.getPowerPlant().getEngineList().get(i).getZApexConstructionAxes()
						.minus(theAircraft.getPowerPlant().getEngineList().get(i).getHeight().divide(2)).getEstimatedValue();
				
				enginePointsList.add(enginePoints);
			}
		}
		
		//--------------------------------------------------
		System.out.println("Initializing test class...");
		String rootOutputFolderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String outputFolderPath = JPADStaticWriteUtils.createNewFolder(rootOutputFolderPath + "Tests_Aircraft" + File.separator);
		System.out.println("Output ==> " + outputFolderPath);

		//--------------------------------------------------
		//set state title
		primaryStage.setTitle("treez/javafx-d3 - D3Plotter demo");

		List<Double[][]> listDataArrayFrontView = new ArrayList<Double[][]>();

		// hTail
		listDataArrayFrontView.add(dataFrontViewHTail);
		listDataArrayFrontView.add(dataFrontViewHTailMirrored);
		// vTail
		listDataArrayFrontView.add(dataFrontViewVTail);
		// fuselage
		listDataArrayFrontView.add(dataSectionYZUpperCurve);
		listDataArrayFrontView.add(dataSectionYZLowerCurve);
		// wing
		listDataArrayFrontView.add(dataFrontViewWing);
		listDataArrayFrontView.add(dataFrontViewWingMirrored);
		// engine
		for (int i=0; i<enginePointsList.size(); i++)
			listDataArrayFrontView.add(enginePointsList.get(i));
		
		double yMaxFrontView = 1.20*wing.getSemiSpan().doubleValue(SI.METER);
		double yMinFrontView = -1.20*wing.getSemiSpan().doubleValue(SI.METRE);
		double zMaxFrontView = yMaxFrontView; 
		double zMinFrontView = yMinFrontView;
		
		D3PlotterOptions optionsFrontView = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(yMinFrontView, yMaxFrontView)
				.yRange(zMinFrontView, zMaxFrontView)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
				.title("Aircraft data representation - Front View")
				.xLabel("y (m)")
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
						SymbolType.CIRCLE
						)
				.symbolSizes(2,2,2,2,2,2,2,2,2,2,2)
				.showSymbols(false,false,false,false,false,false,false,false,false,false) // NOTE: overloaded function
				.symbolStyles(
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
						"fill:none; stroke:black; stroke-width:2"
						)
				.plotAreas(true,true,true,true,true,true,true,true,true,true)
				.areaStyles("fill:blue;","fill:blue;","fill:yellow;","fill:white;","fill:white;","fill:lightblue;","fill:lightblue;",
						"fill:orange;","fill:orange;")
				.areaOpacities(1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0)
				.showLegend(false)
				.build();

//		System.out.println("Plot options:\n" + optionsSideView);

		d3Plotter = new D3Plotter(
				optionsFrontView,
				listDataArrayFrontView
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
			String outputFilePathSideView = outputFolderPath + "AircraftFrontView.svg";
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
			
			String dirLandingGears = va.getLandingGearsDirectory().getCanonicalPath();
			System.out.println("LANDING GEARS ===> " + dirLandingGears);
			
			String dirSystems = va.getSystemsDirectory().getCanonicalPath();
			System.out.println("SYSTEMS ===> " + dirSystems);
			
			String dirCabinConfiguration = va.getCabinConfigurationDirectory().getCanonicalPath();
			System.out.println("CABIN CONFIGURATIONS ===> " + dirCabinConfiguration);
			
			String dirCosts = va.getCostsDirectory().getCanonicalPath();
			System.out.println("COSTS ===> " + dirCosts);
			
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
//					)
//					.build();

			// reading aircraft from xml ...
			theAircraft = Aircraft.importFromXML(
					pathToXML,
					dirLiftingSurfaces,
					dirFuselages,
					dirEngines,
					dirLandingGears,
					dirSystems,
					dirCabinConfiguration,
					dirAirfoil,
					dirCosts,
					aeroDatabaseReader,
					highLiftDatabaseReader);
			
			System.out.println("The Aircaraft ...");
			System.out.println(AircraftTestFrontView.theAircraft.toString());
			
		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			AircraftTestFrontView.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ...
		launch(args);
	}

}