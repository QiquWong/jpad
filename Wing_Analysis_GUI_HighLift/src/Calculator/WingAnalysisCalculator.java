package Calculator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.RootPaneContainer;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jscience.physics.amount.Amount;
import org.treez.javafxd3.javafx.JavaFxD3Browser;

import com.ibm.icu.text.AlphabeticIndex;

import GUI.Main;
import GUI.Views.VaraiblesAnalyses;
import aircraft.components.liftingSurface.creator.SlatCreator;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import calculators.aerodynamics.LiftCalc;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.HighLiftDeviceEffectEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.DatabaseManager;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.MyArray;
import thredds.wcs.v1_0_0_1.GetCapabilities;
import writers.JPADStaticWriteUtils;

public class WingAnalysisCalculator {
	static NasaBlackwell theNasaBlackwellCalculator;
	static TextArea textOutputLift;
	static List<Double>  _deltaCl0FlapList = new ArrayList<>();
	static List<Double>  _clAlphaFlapList = new ArrayList<>();
	static double[] newChordDistributionWithFlapMeter;
	static List<Double> yAdimensionalArrayHighLiftModified = new ArrayList<>();
	static double[] yTemp;
	static List<Double> yArrayForDeltaFlap = new ArrayList<>();
	static List<Double> yArrayForDeltaSlat = new ArrayList<>();
	static Double[] yAappNew = new Double[yAdimensionalArrayHighLiftModified.size()];
	static calculators.aerodynamics.NasaBlackwell theNasaBlackwellHIGHLIFTCalculator;
	static List<Double>  _deltaClmaxFlapList = new ArrayList<>();
	static List<Double> _deltaClmaxSlatList = new ArrayList<>();
	
	// CREATE SORTED ARRAY FOR Y STATIONS, CHORD, ALPHA ZERO LIFT AND XLE

	static double[] chordDistributionHighLiftFinal;
	static double[] alphaZeroLiftDistributionHighLiftFinal;
	static double[] xleDistributionHighLiftFinal;
	static double[] dihedralxleDistributionHighLiftFinal;
	static double[] twistDistributionHighLiftFinal;
	static double[] yMeterDimensionalDistributionHighLiftFinal;
	
	static double[] clMaxDistributionHighLiftFinal;
	static double[] alphaStallDistributionHighLiftFinal;
	
	static double[] clZeroDistributionHighLiftFinal;
	static double[] clAlphaDistributionHighLiftFinal;

	
	public static void calculateLoadDistributions(InputOutputTree theInputOutputTree, VaraiblesAnalyses theController){

		double vortexSemiSpanToSemiSpanRatio = (1./(2*theInputOutputTree.getNumberOfPointSemispan()));

		double [] twistDistributionRadians = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] alphaZeroLiftDistributionRadians = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] dihedralDistributionRadians = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] yDistributionMeter = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] chordDistributionMeter = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] xleDistributionMeter = new double[theInputOutputTree.getNumberOfPointSemispan()];

		for (int i=0; i< theInputOutputTree.getNumberOfPointSemispan(); i++) {
			twistDistributionRadians[i] = theInputOutputTree.getTwistDistributionSemiSpan().get(i).doubleValue(SI.RADIAN);
			alphaZeroLiftDistributionRadians[i] = theInputOutputTree.getAlphaZeroLiftDistributionSemiSpan().get(i).doubleValue(SI.RADIAN);
			dihedralDistributionRadians [i] = theInputOutputTree.getDihedralDistributionSemiSpan().get(i).doubleValue(SI.RADIAN);
			yDistributionMeter [i] = theInputOutputTree.getyDimensionalDistributionSemiSpan().get(i).doubleValue(SI.METER);
			chordDistributionMeter [i] = theInputOutputTree.getChordDistributionSemiSpan().get(i).doubleValue(SI.METER);
			xleDistributionMeter [i] = theInputOutputTree.getxLEDistributionSemiSpan().get(i).doubleValue(SI.METER);
		}

		theInputOutputTree.initializeData();

		calculators.aerodynamics.NasaBlackwell theNasaBlackwellCalculator = new  calculators.aerodynamics.NasaBlackwell(
				theInputOutputTree.getSemiSpan().doubleValue(SI.METER), 
				theInputOutputTree.getSurface().doubleValue(SI.SQUARE_METRE),
				yDistributionMeter,
				chordDistributionMeter,
				xleDistributionMeter,
				dihedralDistributionRadians,
				twistDistributionRadians,
				alphaZeroLiftDistributionRadians,
				vortexSemiSpanToSemiSpanRatio,
				0.0,
				theInputOutputTree.getMachNumber(),
				theInputOutputTree.getAltitude().doubleValue(SI.METER));

		for (int i=0; i<theInputOutputTree.getAlphaArrayLiftDistribution().size(); i++){

			theNasaBlackwellCalculator.calculate(theInputOutputTree.getAlphaArrayLiftDistribution().get(i));
			List<Double> clList = new ArrayList<>();
			clList = Main.convertDoubleArrayToListDouble(
					Main.convertFromDoubleToPrimitive(
							theNasaBlackwellCalculator.getClTotalDistribution().toArray()));
			if (clList.get(i).isNaN()){
				for (int ii=0; ii< clList.size(); ii++){
					clList.set(ii, 0.0);
				}
			}
			theInputOutputTree.getClDistributionCurves().add(i, 
					clList);
		}

		theController.setRunLoad(theController.getRunLoad()+1);

		// PRINT OUTPUT

		// JFREECHART

		//			final XYSeriesCollection dataset = new XYSeriesCollection( );          
		//		      final XYSeries firefox = new XYSeries( "Firefox" );          
		//		      firefox.add( 1.0 , 1.0 );          
		//		      firefox.add( 2.0 , 4.0 );          
		//		      firefox.add( 3.0 , 3.0 );          
		//		      
		//		      final XYSeries chrome = new XYSeries( "Chrome" );          
		//		      chrome.add( 1.0 , 4.0 );          
		//		      chrome.add( 2.0 , 5.0 );          
		//		      chrome.add( 3.0 , 6.0 );          
		//		      
		//		      final XYSeries iexplorer = new XYSeries( "InternetExplorer" );          
		//		      iexplorer.add( 3.0 , 4.0 );          
		//		      iexplorer.add( 4.0 , 5.0 );          
		//		      iexplorer.add( 5.0 , 4.0 );          
		//		      
		//		      dataset.addSeries( firefox );          
		//		      dataset.addSeries( chrome );          
		//		      dataset.addSeries( iexplorer );
		//		      
		//		  	JFreeChart chart = ChartFactory.createXYLineChart(
		//					"Lift Coefficient distribution", 
		//					"eta",
		//					"Cl",
		//					dataset);

		//		    final SwingNode chartSwingNode = new SwingNode();
		//
		//		    chartSwingNode.setContent(
		//		      new ChartPanel(
		//		    		  chart,
		//		    		  1500,
		//		    		  1500,
		//		    		  1000, 
		//		    		  1000,
		//		    		  1000,
		//		    		  1000,
		//		    		  true, 
		//		    		  true, 
		//		    		  false,
		//		    		  true,
		//		    		  true, 
		//		    		  true
		//		      )      
		//		    );
		//		  	
		//		  	theController.getOutputPaneFinalLOAD().getChildren().add(chartSwingNode);


		//LINECHART javafx

		List<String> legendLiftCoefficientDistribution = new ArrayList<>();

		for(int i=0; i<theInputOutputTree.getAlphaArrayLiftDistribution().size(); i++){
			legendLiftCoefficientDistribution.add(
					"alpha = " + 
							theInputOutputTree.getAlphaArrayLiftDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) +
							" deg"
					);
		}

		List<List<Double>> xList = new ArrayList<>();
		List<List<Double>> yList = new ArrayList<>();

		for(int i=0; i<theInputOutputTree.getClDistributionCurves().size(); i++){
			xList.add(theInputOutputTree.getyAdimensionalDistributionSemiSpan());
			yList.add(theInputOutputTree.getClDistributionCurves().get(i));

		}

		displayChart(
				"Lift Coefficient Distribution",
				"eta",
				"Cl", 
				150, 
				750,
				true,
				Side.RIGHT,
				legendLiftCoefficientDistribution, 
				xList, 
				yList, 
				theController.getOutputPaneFinalLOAD().getChildren()
				);

		GridPane gridText = theController.getOutputPaneTextLOAD();
		gridText.add(new Label("Text Output"), 0, 0);
		TextArea textOutput = theController.getOutputTextLOAD();

		textOutput.appendText("--------ANALYSIS NUMBER " + theController.getRunLoad() + "------------\n");
		for(int i=0; i<theInputOutputTree.getAlphaArrayLiftDistribution().size(); i++){
			textOutput.appendText("Cl distribution at alpha = " + 
					theInputOutputTree.getAlphaArrayLiftDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + 
					" deg ---> " + theInputOutputTree.getClDistributionCurves().get(i) + "\n");
		}

		textOutput.appendText("--------End of " + theController.getRunLoad() + "st Run------------\n\n");
		//	theController.getOutputPaneTextLOAD().getChildren().add(gridText);
	}

	public static void calculateLiftCurve(InputOutputTree theInputOutputTree, VaraiblesAnalyses theController){

		// Setup database(s)
		AerodynamicDatabaseReader aeroDatabaseReader;

		// Setup database(s)

		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";

		aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);

		theController.setRunLift(theController.getRunLift()+1);

		double vortexSemiSpanToSemiSpanRatio = (1./(2*theInputOutputTree.getNumberOfPointSemispan()));

		double [] twistDistributionRadians = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] alphaZeroLiftDistributionRadians = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] dihedralDistributionRadians = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] yDistributionMeter = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] chordDistributionMeter = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] xleDistributionMeter = new double[theInputOutputTree.getNumberOfPointSemispan()];

		for (int i=0; i< theInputOutputTree.getNumberOfPointSemispan(); i++) {
			twistDistributionRadians[i] = theInputOutputTree.getTwistDistributionSemiSpan().get(i).doubleValue(SI.RADIAN);
			alphaZeroLiftDistributionRadians[i] = theInputOutputTree.getAlphaZeroLiftDistributionSemiSpan().get(i).doubleValue(SI.RADIAN);
			dihedralDistributionRadians [i] = theInputOutputTree.getDihedralDistributionSemiSpan().get(i).doubleValue(SI.RADIAN);
			yDistributionMeter [i] = theInputOutputTree.getyDimensionalDistributionSemiSpan().get(i).doubleValue(SI.METER);
			chordDistributionMeter [i] = theInputOutputTree.getChordDistributionSemiSpan().get(i).doubleValue(SI.METER);
			xleDistributionMeter [i] = theInputOutputTree.getxLEDistributionSemiSpan().get(i).doubleValue(SI.METER);
		}

		//		theInputOutputTree.initializeData();

		theNasaBlackwellCalculator = new  NasaBlackwell(
				theInputOutputTree.getSemiSpan().doubleValue(SI.METER), 
				theInputOutputTree.getSurface().doubleValue(SI.SQUARE_METRE),
				yDistributionMeter,
				chordDistributionMeter,
				xleDistributionMeter,
				dihedralDistributionRadians,
				twistDistributionRadians,
				alphaZeroLiftDistributionRadians,
				vortexSemiSpanToSemiSpanRatio,
				0.0,
				theInputOutputTree.getMachNumber(),
				theInputOutputTree.getAltitude().doubleValue(SI.METER));
		
		// CL ALPHA

		Amount<Angle> alphaFirst = Amount.valueOf(Math.toRadians(2.0), SI.RADIAN);
		Amount<Angle> alphaSecond = Amount.valueOf(Math.toRadians(4.0), SI.RADIAN);

		theNasaBlackwellCalculator.calculate(alphaFirst);
		double [] clDistribution = theNasaBlackwellCalculator.getClTotalDistribution().toArray();
		double cLFirst = theNasaBlackwellCalculator.get_cLEvaluated();

		theNasaBlackwellCalculator.calculate(alphaSecond);
		double cLSecond = theNasaBlackwellCalculator.get_cLEvaluated();


		double cLAlpha = (cLSecond - cLFirst)/(alphaSecond.getEstimatedValue()-alphaFirst.getEstimatedValue()); // 1/rad
		theInputOutputTree.setcLAlphaRad(cLAlpha);
		theInputOutputTree.setcLAlphaDeg(Math.toRadians(cLAlpha));
		Amount<?> clAlpha = Amount.valueOf(theInputOutputTree.getcLAlphaDeg(), NonSI.DEGREE_ANGLE.inverse());
		theInputOutputTree.setcLAlpha(clAlpha);

		Amount<?> wingclAlpha = Amount.valueOf( theInputOutputTree.getcLAlphaRad() , SI.RADIAN.inverse());

		// CL ZERO

		Amount<Angle> alphaZero = Amount.valueOf(0.0, SI.RADIAN);
		theNasaBlackwellCalculator.calculate(alphaZero);
		double cLZero = theNasaBlackwellCalculator.get_cLEvaluated();
		theInputOutputTree.setcLZero(cLZero);

		//ALPHA ZERO LIFT

		double alphaZeroLift = -(theInputOutputTree.getcLZero())/theInputOutputTree.getcLAlphaDeg();
		theInputOutputTree.setAlphaZeroLift(Amount.valueOf(alphaZeroLift, NonSI.DEGREE_ANGLE));


		//ALPHA STAR

		List<Amount<Area>> influenceAreas = new ArrayList<>();
		List<Double> kFactors = new ArrayList<>();

		// First value
		influenceAreas.add(0,
				Amount.valueOf(
						(theInputOutputTree.getChordDistribution().get(0).doubleValue(SI.METER) * 
								(theInputOutputTree.getyDimensionalDistributionInput().get(1).doubleValue(SI.METER) - 
										theInputOutputTree.getyDimensionalDistributionInput().get(0).doubleValue(SI.METER))/2),
						SI.SQUARE_METRE)
				);

		kFactors.add(0, 
				(2*influenceAreas.get(0).doubleValue(SI.SQUARE_METRE)/
						theInputOutputTree.getSurface().doubleValue(SI.SQUARE_METRE)));

		if(theInputOutputTree.getNumberOfSections() == 2 ){
			influenceAreas.add(1,
					Amount.valueOf(
							(theInputOutputTree.getChordDistribution().get(1).doubleValue(SI.METER) * 
									(theInputOutputTree.getyDimensionalDistributionInput().get(1).doubleValue(SI.METER) - 
											theInputOutputTree.getyDimensionalDistributionInput().get(0).doubleValue(SI.METER))/2),
							SI.SQUARE_METRE)
					);
			kFactors.add(1, 
					(2*influenceAreas.get(1).doubleValue(SI.SQUARE_METRE)/
							theInputOutputTree.getSurface().doubleValue(SI.SQUARE_METRE)));
		}

		if(theInputOutputTree.getNumberOfSections() >=3){
			for (int i=1; i<theInputOutputTree.getNumberOfSections()-1;i++){
				influenceAreas.add(i,
						Amount.valueOf(
								(theInputOutputTree.getChordDistribution().get(i).doubleValue(SI.METER) * 
										(theInputOutputTree.getyDimensionalDistributionInput().get(i).doubleValue(SI.METER) - 
												theInputOutputTree.getyDimensionalDistributionInput().get(i-1).doubleValue(SI.METER))/2) + 
								((theInputOutputTree.getChordDistribution().get(i).doubleValue(SI.METER) * 
										(theInputOutputTree.getyDimensionalDistributionInput().get(i+1).doubleValue(SI.METER) - 
												theInputOutputTree.getyDimensionalDistributionInput().get(i).doubleValue(SI.METER)))/2),
								SI.SQUARE_METRE)
						);

				kFactors.add(i, 
						(2*influenceAreas.get(i).doubleValue(SI.SQUARE_METRE)/
								theInputOutputTree.getSurface().doubleValue(SI.SQUARE_METRE)));

			}

			influenceAreas.add(theInputOutputTree.getNumberOfSections()-1,
					Amount.valueOf(
							(theInputOutputTree.getChordDistribution().get(theInputOutputTree.getNumberOfSections()-1).doubleValue(SI.METER) * 
									(theInputOutputTree.getyDimensionalDistributionInput().get(theInputOutputTree.getNumberOfSections()-1).doubleValue(SI.METER) - 
											theInputOutputTree.getyDimensionalDistributionInput().get(theInputOutputTree.getNumberOfSections()-2).doubleValue(SI.METER))/2),
							SI.SQUARE_METRE)
					);
			kFactors.add(theInputOutputTree.getNumberOfSections()-1, 
					(2*influenceAreas.get(theInputOutputTree.getNumberOfSections()-1).doubleValue(SI.SQUARE_METRE)/
							theInputOutputTree.getSurface().doubleValue(SI.SQUARE_METRE)));

		}


		Double alphaStarDeg = 0.0;
		Double meanThickness = 0.0;
		for (int i=0; i<kFactors.size(); i++){
			alphaStarDeg = alphaStarDeg + (kFactors.get(i)*theInputOutputTree.getAlphaStarDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE));
			meanThickness = meanThickness + (kFactors.get(i)*theInputOutputTree.getThicknessDistribution().get(i));
		}
		Amount<Angle> alphaStar =  Amount.valueOf(
				alphaStarDeg,
				NonSI.DEGREE_ANGLE);

		theInputOutputTree.setAlphaStar(alphaStar);
		theInputOutputTree.setMeanThickness(meanThickness);

		//CL STAR

		theNasaBlackwellCalculator.calculate(alphaStar);
		double cLStar = theNasaBlackwellCalculator.get_cLEvaluated();
		theInputOutputTree.setcLStar(cLStar);

		// CL MAX


		double cLMax = LiftCalc.calculateCLMax(
				MyArrayUtils.convertToDoublePrimitive(theInputOutputTree.getMaximumliftCoefficientDistributionSemiSpan()),
				theInputOutputTree.getSemiSpan().doubleValue(SI.METER),
				theInputOutputTree.getSurface().doubleValue(SI.SQUARE_METRE), 
				yDistributionMeter,
				chordDistributionMeter,
				xleDistributionMeter,
				dihedralDistributionRadians, 
				twistDistributionRadians, 
				alphaZeroLiftDistributionRadians,
				vortexSemiSpanToSemiSpanRatio,
				0.0,
				theInputOutputTree.getMachNumber(), 
				theInputOutputTree.getAltitude().doubleValue(SI.METER)
				);

		theInputOutputTree.setcLMax(cLMax);

		// ALPHA MAX LINEAR
		theInputOutputTree.setAlphaMaxLinear(
				Amount.valueOf(
						(cLMax-cLZero)/theInputOutputTree.getcLAlphaDeg(), 
						NonSI.DEGREE_ANGLE)
				);

		// ALPHA STALL

		// delta alpha	

		double sweepLEDeg = Math.toDegrees(
				Math.atan(
						theInputOutputTree.getxLEDistribution().get(theInputOutputTree.getNumberOfSections()-1).getEstimatedValue()/
						theInputOutputTree.getSemiSpan().getEstimatedValue()
						)
				);

		double deltaYPercent =  aeroDatabaseReader
				.getDeltaYvsThickness(
						theInputOutputTree.getMeanThickness(),
						theInputOutputTree.getMeanAirfoilFamily()
						);

		Amount<Angle> deltaAlpha = Amount.valueOf(
				aeroDatabaseReader
				.getDAlphaVsLambdaLEVsDy(
						sweepLEDeg,
						deltaYPercent
						),
				NonSI.DEGREE_ANGLE);

		theInputOutputTree.setAlphaStall(theInputOutputTree.getAlphaMaxLinear()
				.plus(deltaAlpha));

		theInputOutputTree.setLiftCoefficientCurve(new ArrayList<>());

		theInputOutputTree.setLiftCoefficientCurve(
				Main.convertDoubleArrayToListDouble(
						calculateCLvsAlphaArray(
								theInputOutputTree.getcLZero(),
								theInputOutputTree.getcLMax(),
								theInputOutputTree.getAlphaStar(),
								theInputOutputTree.getAlphaStall(),
								wingclAlpha,
								MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutputTree.getAlphaArrayLiftCurve())
								)));



		//LINECHART javafx

		List<List<Double>> xList = new ArrayList<>();
		List<List<Double>> yList = new ArrayList<>();
		List<String> legend = new ArrayList<>();

		xList.add(
				Main.convertDoubleArrayToListDouble(
						MyArrayUtils.convertListOfAmountToDoubleArray(
								theInputOutputTree.getAlphaArrayLiftCurve()
								)));

		yList.add(theInputOutputTree.getLiftCoefficientCurve());

		legend.add("curve");

		TabPane newOutputCharts = new TabPane();
		//lift
		Tab Lift = new Tab();
		Lift.setText("Lift Curve");
		Pane LiftPane = new Pane();		
		Lift.setContent(LiftPane);


		//stall path 
		Tab stallPath = new Tab();
		stallPath.setText("Stall Path");
		Pane stallPathPane = new Pane();

		stallPath.setContent(stallPathPane);

		newOutputCharts.getTabs().add(0, Lift);
		newOutputCharts.getTabs().add(1, stallPath);


		if(theController.getYesStallPath().isSelected()){
			theController.getOutputPaneFinalLIFT().getChildren().clear();

			theController.getOutputPaneFinalLIFT().getChildren().add(newOutputCharts);

			Node chart = displayChartNode(
					"Lift Curve",
					"alpha",
					"CL", 
					150, 
					750,
					false,
					Side.RIGHT,
					legend, 
					xList, 
					yList, 
					newOutputCharts.getTabs().get(0).getContent()
					);

			LiftPane.getChildren().clear();
			LiftPane.getChildren().add(chart);
		}

		if(theController.getNoStallPath().isSelected()){
			displayChart(
					"Lift Curve",
					"alpha",
					"CL", 
					150, 
					750,
					false,
					Side.RIGHT,
					legend, 
					xList, 
					yList, 
					theController.getOutputPaneFinalLIFT().getChildren()
					);
		}

		GridPane gridText = theController.getOutputPaneTextLIFT();
		gridText.add(new Label("Text Output"), 0, 0);

		textOutputLift = theController.getOutputTextLIFT();

		textOutputLift.appendText("--------ANALYSIS NUMBER " + theController.getRunLift() + "------------\n");

		textOutputLift.appendText("Alpha Array --> (deg)  " + Arrays.toString(MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutputTree.getAlphaArrayLiftCurve())));
		textOutputLift.appendText("\nCL curve --> " + theInputOutputTree.getLiftCoefficientCurve());

		textOutputLift.appendText("\nAlpha zero Lift (deg) --> " + theInputOutputTree.getAlphaZeroLift().doubleValue(NonSI.DEGREE_ANGLE));
		textOutputLift.appendText("\nCL zero --> " + theInputOutputTree.getcLZero());
		textOutputLift.appendText("\nCL alpha (1/deg) --> " + theInputOutputTree.getcLAlphaDeg());
		textOutputLift.appendText("\nCL alpha (1/rad) --> " + theInputOutputTree.getcLAlphaRad());
		textOutputLift.appendText("\nAlpha star (deg) --> " + theInputOutputTree.getAlphaStar().doubleValue(NonSI.DEGREE_ANGLE));
		textOutputLift.appendText("\nCL star --> " + theInputOutputTree.getcLStar());
		textOutputLift.appendText("\nAlpha max Linear (deg) --> " + theInputOutputTree.getAlphaMaxLinear().doubleValue(NonSI.DEGREE_ANGLE));
		textOutputLift.appendText("\nCL max --> " + theInputOutputTree.getcLMax());
		textOutputLift.appendText("\nAlpha stall (deg) --> " + theInputOutputTree.getAlphaStall().doubleValue(NonSI.DEGREE_ANGLE));

		if(theController.getNoStallPath().isSelected()){
			textOutputLift.appendText("\n\n--------End of " + theController.getRunLift() + "st Run------------\n\n");

		}

		if ( theController.getYesStallPath().isSelected() ){
			theInputOutputTree.setPerformStallPathAnalysis(true);
			WingAnalysisCalculator.performStallPath(theInputOutputTree, theController, stallPathPane, newOutputCharts);
		}

		if ( theController.getNoStallPath().isSelected() ){
			theInputOutputTree.setPerformStallPathAnalysis(false);
		}
	}

	public static void calculateHighLiftCurve(
			InputOutputTree theInputOutpuTree,
			VaraiblesAnalyses theController) {

		// Setup database(s)
		AerodynamicDatabaseReader aeroDatabaseReader;
		HighLiftDatabaseReader highLiftDatabaseReader;
		// Setup database(s)

		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		String highLiftDatabaseFileName = "HighLiftDatabase.h5";

		aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);

		theInputOutpuTree.setPerformHighLiftAnalysis(true);
		theController.setRunLift(theController.getRunHighLift()+1);

		List<SymmetricFlapCreator> theFlapList = new ArrayList<>();
		List<SlatCreator> theSlatList = new ArrayList<>();

		for(int i=0; i<theInputOutpuTree.getNumberOfFlaps(); i++) {
			theFlapList.add(
					new SymmetricFlapCreator.SymmetricFlapBuilder(
							"flap", 
							theInputOutpuTree.getFlapTypes().get(i),
							theInputOutpuTree.getFlapInnerStation().get(i), 
							theInputOutpuTree.getFlapOuterStation().get(i), 
							theInputOutpuTree.getFlapChordRatio().get(i), 
							theInputOutpuTree.getFlapChordRatio().get(i), 
							Amount.valueOf(-5.0, NonSI.DEGREE_ANGLE),
							Amount.valueOf(40.0, NonSI.DEGREE_ANGLE)
							).build()
					);
		}

		for(int i=0; i<theInputOutpuTree.getNumberOfSlats(); i++) {
			theSlatList.add(
					new SlatCreator.SlatBuilder(
							"flap",
							theInputOutpuTree.getSlatInnerStation().get(i), 
							theInputOutpuTree.getSlatOuterStation().get(i),
							theInputOutpuTree.getSlatChordRatio().get(i), 
							theInputOutpuTree.getSlatChordRatio().get(i), 
							theInputOutpuTree.getSlatExtensionRatio().get(i), 
							Amount.valueOf(0.0, NonSI.DEGREE_ANGLE),
							Amount.valueOf(20.0, NonSI.DEGREE_ANGLE)
							).build()
					);
		}

		if(!theInputOutpuTree.performLiftAnalysis) {
			Amount<Angle> alphaInitialAmount = Amount.valueOf(0, NonSI.DEGREE_ANGLE);

			Amount<Angle> alphaFinalAmount = Amount.valueOf(20.0,
					NonSI.DEGREE_ANGLE);

			List<Amount<Angle>>alphaLiftArray = MyArrayUtils.convertDoubleArrayToListOfAmount(
					MyArrayUtils.linspace(
							alphaInitialAmount.doubleValue(NonSI.DEGREE_ANGLE),
							alphaFinalAmount.doubleValue(NonSI.DEGREE_ANGLE),
							21),
					NonSI.DEGREE_ANGLE);

			theInputOutpuTree.setAlphaArrayLiftCurve(alphaLiftArray);
			calculateLiftCurve(theInputOutpuTree, theController);
		}

		Map<HighLiftDeviceEffectEnum, Object> highLiftDevicesEffectsMap = 
				LiftCalc.calculateHighLiftDevicesEffects(
						aeroDatabaseReader,
						highLiftDatabaseReader, 
						theFlapList, 
						theSlatList,
						theInputOutpuTree.getyAdimensionalStationInput(),
						theInputOutpuTree.getClAlphaDistribution(),
						theInputOutpuTree.getcLZeroDistribution(), 
						theInputOutpuTree.getThicknessDistribution(),
						theInputOutpuTree.getLeRadiusDistribution(), 
						theInputOutpuTree.getChordDistribution(), 
						theInputOutpuTree.getFlapDeflection(), 
						theInputOutpuTree.getSlatDeflection(), 
						Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), 
						theInputOutpuTree.getcLAlpha(), 
						theInputOutpuTree.getSweepQuarterChordEquivalent(), 
						theInputOutpuTree.getTaperRatioEquivalentWing(), 
						theInputOutpuTree.getRootChordEquivalentWing(),
						theInputOutpuTree.getAspectRatio(), 
						theInputOutpuTree.getSurface(), 
						theInputOutpuTree.getMeanThickness(), 
						theInputOutpuTree.getMeanAirfoilFamily(), 
						theInputOutpuTree.getcLZero(), 
						theInputOutpuTree.getcLMax(), 
						theInputOutpuTree.getAlphaStar(),
						theInputOutpuTree.getAlphaStall()
						);

		 _deltaCl0FlapList =  
				(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl0_FLAP_LIST);

		List<Double>  _deltaCL0FlapList =  
				(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CL0_FLAP_LIST)
				;
		_deltaClmaxFlapList = 
				(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_FLAP_LIST);

		List<Double> _deltaCLmaxFlapList =  
				(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_FLAP_LIST);

		_deltaClmaxSlatList =
				(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_SLAT_LIST);

		List<Double> _deltaCLmaxSlatList = 
				(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_SLAT_LIST);

		List<Double> _deltaCD0List =
				(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CD_LIST);

		List<Double> _deltaCMc4List =
				(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CM_c4_LIST);

		Double _deltaCl0Flap = 
				(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl0_FLAP);

		Double _deltaCL0Flap = 
				(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CL0_FLAP);

		theInputOutpuTree.setDeltaCL0Flap(_deltaCL0Flap);

		Double  _deltaClmaxFlap = 
				(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_FLAP);

		Double  _deltaCLmaxFlap =
				(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_FLAP);

		theInputOutpuTree.setDeltaCLMaxFlap(_deltaCLmaxFlap);

		Double  _deltaClmaxSlat = 
				(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_SLAT);

		Double _deltaCLmaxSlat = 
				(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_SLAT);

		theInputOutpuTree.setDeltaCLMaxSlat(_deltaCLmaxSlat);

		Double _deltaCD0 =
				(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CD);

		Double _deltaCMc4 =
				(Double) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CM_c4);

		Amount<?> _cLAlphaHighLift = 
				(Amount<?>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.CL_ALPHA_HIGH_LIFT);

		theInputOutpuTree.setClAlphaFlap(_cLAlphaHighLift);
		
		_clAlphaFlapList = 
				(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.CL_ALPHA_HIGH_LIFT_LIST);

		//------------------------------------------------------
		// CL ZERO HIGH LIFT
		theInputOutpuTree.setcLZeroHighLift(theInputOutpuTree.getcLZero() + _deltaCL0Flap);

		//------------------------------------------------------
		// ALPHA ZERO LIFT HIGH LIFT

		theInputOutpuTree.setAlphaZeroLiftHighLift(Amount.valueOf(
				-(theInputOutpuTree.getcLZero()/_cLAlphaHighLift.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()), 
				NonSI.DEGREE_ANGLE));

		//------------------------------------------------------
		// CL MAX HIGH LIFT
		if(theInputOutpuTree.getNumberOfSlats()==0) {
			theInputOutpuTree.setcLMaxHighLift(theInputOutpuTree.getcLMax()+_deltaCLmaxFlap);
		}
		else 
			theInputOutpuTree.setcLMaxHighLift(theInputOutpuTree.getcLMax()+_deltaCLmaxFlap+_deltaCLmaxSlat);

		//------------------------------------------------------
		// ALPHA STALL HIGH LIFT
		double deltaYPercent = aeroDatabaseReader
				.getDeltaYvsThickness(
						theInputOutpuTree.getMeanThickness(),
						theInputOutpuTree.getMeanAirfoilFamily()
						);

		Amount<Angle> deltaAlpha = Amount.valueOf(
				aeroDatabaseReader
				.getDAlphaVsLambdaLEVsDy(
						theInputOutpuTree.getSweepQuarterChordEquivalent().doubleValue(NonSI.DEGREE_ANGLE),
						deltaYPercent
						),
				NonSI.DEGREE_ANGLE);

		theInputOutpuTree.setAlphaStallHighLift(
				Amount.valueOf(
						((theInputOutpuTree.getcLMaxHighLift()
								- theInputOutpuTree.getcLZeroHighLift())
								/_cLAlphaHighLift
								.to(NonSI.DEGREE_ANGLE.inverse())
								.getEstimatedValue()

								+ deltaAlpha.doubleValue(NonSI.DEGREE_ANGLE)),
						NonSI.DEGREE_ANGLE)
				);

		//------------------------------------------------------
		// ALPHA STAR HIGH LIFT
		theInputOutpuTree.setAlphaStarHighLift(
				Amount.valueOf(
						theInputOutpuTree.getAlphaStallHighLift().doubleValue(NonSI.DEGREE_ANGLE)
						-(theInputOutpuTree.getAlphaStall().doubleValue(NonSI.DEGREE_ANGLE)
								- theInputOutpuTree.getAlphaStar().doubleValue(NonSI.DEGREE_ANGLE)),
						NonSI.DEGREE_ANGLE)
				);
		//------------------------------------------------------
		// ALPHA STAR HIGH LIFT
		theInputOutpuTree.setClStarHighLift(
				(_cLAlphaHighLift.to(NonSI.DEGREE_ANGLE.inverse())
						.getEstimatedValue()
						* theInputOutpuTree.getAlphaStarHighLift()
						.doubleValue(NonSI.DEGREE_ANGLE))
				+theInputOutpuTree.getcLZeroHighLift()
				);



		theInputOutpuTree.setLiftCoefficient3DCurveHighLift(
				MyArrayUtils.convertDoubleArrayToListDouble(
						LiftCalc.calculateCLvsAlphaArray(
								theInputOutpuTree.getcLZeroHighLift(),
								theInputOutpuTree.getcLMaxHighLift(),
								theInputOutpuTree.getAlphaStarHighLift(),
								theInputOutpuTree.getAlphaStallHighLift(),
								_cLAlphaHighLift,
								MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getAlphaArrayHighLiftCurve())
								))
				);	


		//LINECHART javafx

		List<List<Double>> xList = new ArrayList<>();
		List<List<Double>> yList = new ArrayList<>();
		List<String> legend = new ArrayList<>();

		xList.add(
				Main.convertDoubleArrayToListDouble(
						MyArrayUtils.convertListOfAmountToDoubleArray(
								theInputOutpuTree.alphaArrayLiftCurve
								)));
		xList.add(
				Main.convertDoubleArrayToListDouble(
						MyArrayUtils.convertListOfAmountToDoubleArray(
								theInputOutpuTree.alphaArrayHighLiftCurve
								)));

		yList.add(theInputOutpuTree.getLiftCoefficientCurve());
		yList.add(theInputOutpuTree.getLiftCoefficient3DCurveHighLift());

		legend.add("Clean configuration");
		legend.add("With high lift devices");

		TabPane newOutputCharts = new TabPane();

		//lift
		Tab Lift = new Tab();
		Lift.setText("Lift Curve");
		Pane LiftPane = new Pane();		
		Lift.setContent(LiftPane);


		//lift distribution 
		Tab liftDistribution = new Tab();
		liftDistribution.setText("Lift disrtibution");
		Pane liftDistributionPane = new Pane();
		
		liftDistribution.setContent(liftDistributionPane);

		// chord distribution
		Tab chordDistribution = new Tab();
		chordDistribution.setText("Chord disrtibution");
		Pane chordDistributionPane = new Pane();
		
		chordDistribution.setContent(chordDistributionPane);
		
		// xle distribution
		Tab xleDistribution = new Tab();
		xleDistribution.setText("Xle disrtibution");
		Pane xleDistributionPane = new Pane();
		
		xleDistribution.setContent(xleDistributionPane);
		
		// alpha zero lift distribution
		Tab alpaZeroLiftDistribution = new Tab();
		alpaZeroLiftDistribution.setText("Alpha zero lift disrtibution");
		Pane alpaZeroLiftDistributionPane = new Pane();
		
		alpaZeroLiftDistribution.setContent(alpaZeroLiftDistributionPane);
		
		newOutputCharts.getTabs().add(0, Lift);
		newOutputCharts.getTabs().add(1, chordDistribution);
		newOutputCharts.getTabs().add(2, xleDistribution);
		newOutputCharts.getTabs().add(3, alpaZeroLiftDistribution);
	    newOutputCharts.getTabs().add(4, liftDistribution);


				if(theController.getYesHighLift().isSelected()){
					theController.getOutputPaneFinalHIGHLIFT().getChildren().clear();
					
					theController.getOutputPaneFinalHIGHLIFT().getChildren().add(newOutputCharts);
		
					Node chart = displayChartNode(
							"Lift Curve",
							"alpha",
							"CL", 
							150, 
							750,
							false,
							Side.RIGHT,
							legend, 
							xList, 
							yList, 
							newOutputCharts.getTabs().get(0).getContent()
							);
		
					LiftPane.getChildren().clear();
					LiftPane.getChildren().add(chart);
				}
		
				if(theController.getNoHighLift().isSelected()){
		displayChart(
				"Lift Curve",
				"alpha",
				"CL", 
				150, 
				750,
				true,
				Side.RIGHT,
				legend, 
				xList, 
				yList, 
				theController.getOutputPaneFinalHIGHLIFT().getChildren()
				);
			}

		GridPane gridText = theController.getOutputPaneTextHIGHLIFT();
		gridText.add(new Label("Text Output"), 0, 0);

		textOutputLift = theController.getOutputTextHIGHLIFT();

		textOutputLift.appendText("--------ANALYSIS NUMBER " + theController.getRunHighLift() + "------------\n");

		textOutputLift.appendText("\n\n--------CLEAN RESULTS ------------\n");
		textOutputLift.appendText("Alpha Array --> (deg)  " + Arrays.toString(MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutpuTree.getAlphaArrayLiftCurve())));
		textOutputLift.appendText("\nCL curve --> " + theInputOutpuTree.getLiftCoefficientCurve());

		textOutputLift.appendText("\nAlpha zero Lift (deg) --> " + theInputOutpuTree.getAlphaZeroLift().doubleValue(NonSI.DEGREE_ANGLE));
		textOutputLift.appendText("\nCL zero --> " + theInputOutpuTree.getcLZero());
		textOutputLift.appendText("\nCL alpha (1/deg) --> " + theInputOutpuTree.getcLAlphaDeg());
		textOutputLift.appendText("\nCL alpha (1/rad) --> " + theInputOutpuTree.getcLAlphaRad());
		textOutputLift.appendText("\nAlpha star (deg) --> " + theInputOutpuTree.getAlphaStar().doubleValue(NonSI.DEGREE_ANGLE));
		textOutputLift.appendText("\nCL star --> " + theInputOutpuTree.getcLStar());
		textOutputLift.appendText("\nAlpha max Linear (deg) --> " + theInputOutpuTree.getAlphaMaxLinear().doubleValue(NonSI.DEGREE_ANGLE));
		textOutputLift.appendText("\nCL max --> " + theInputOutpuTree.getcLMax());
		textOutputLift.appendText("\nAlpha stall (deg) --> " + theInputOutpuTree.getAlphaStall().doubleValue(NonSI.DEGREE_ANGLE));

		textOutputLift.appendText("\n\n--------HIGH LIFT RESULTS ------------\n");
		textOutputLift.appendText("Alpha Array --> (deg)  " + Arrays.toString(MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutpuTree.getAlphaArrayHighLiftCurve())));
		textOutputLift.appendText("\nCL curve --> " + theInputOutpuTree.getLiftCoefficient3DCurveHighLift());

		textOutputLift.appendText("\nDelta CL0 --> " + theInputOutpuTree.getDeltaCL0Flap());
		textOutputLift.appendText("\nDelta CL max flap--> " + theInputOutpuTree.getDeltaCLMaxFlap());
		textOutputLift.appendText("\nDelta CL max slat--> " + theInputOutpuTree.getDeltaCLMaxSlat());
		textOutputLift.appendText("\nAlpha zero Lift (deg) --> " + theInputOutpuTree.getAlphaZeroLiftHighLift().doubleValue(NonSI.DEGREE_ANGLE));
		textOutputLift.appendText("\nCL zero --> " + theInputOutpuTree.getcLZeroHighLift());
		textOutputLift.appendText("\nCL alpha (1/deg) --> " + theInputOutpuTree.getClAlphaFlap().to(NonSI.DEGREE_ANGLE.inverse()));
		textOutputLift.appendText("\nCL alpha (1/rad) --> " + theInputOutpuTree.getClAlphaFlap().to(SI.RADIAN.inverse()));
		textOutputLift.appendText("\nAlpha star (deg) --> " + theInputOutpuTree.getAlphaStarHighLift().doubleValue(NonSI.DEGREE_ANGLE));
		textOutputLift.appendText("\nCL star --> " + theInputOutpuTree.getClStarHighLift());
		textOutputLift.appendText("\nCL max --> " + theInputOutpuTree.getcLMaxHighLift());
		textOutputLift.appendText("\nAlpha stall (deg) --> " + theInputOutpuTree.getAlphaStallHighLift().doubleValue(NonSI.DEGREE_ANGLE));
			
			if(theController.getNoHighLift().isSelected()){
			textOutputLift.appendText("\n\n--------End of " + theController.getRunLift() + "st Run------------\n\n");
		
			}

			if ( theController.getYesHighLift().isSelected() ){
				theInputOutpuTree.setPerformHighLiftDistributionAnalysis(true);
				
				WingAnalysisCalculator.calculateHighLiftDistribution(
						theInputOutpuTree,
						theController, 
						chordDistributionPane,
						xleDistributionPane,
						alpaZeroLiftDistributionPane,
						liftDistributionPane,
						newOutputCharts);
				
				//stall path
				Tab stalPathHighLift = new Tab();
				stalPathHighLift.setText("Stall Path High Lift");
				Pane stalPathHighLiftPane = new Pane();
				
				stalPathHighLift.setContent(stalPathHighLiftPane);
			    newOutputCharts.getTabs().add(5, stalPathHighLift);
				
				WingAnalysisCalculator.performHighLiftStallPath(
						theInputOutpuTree, 
						theController, 
						stalPathHighLiftPane, 
						newOutputCharts
						);
				
				WingAnalysisCalculator.performHighLiftStallPathNewMethod(
						theInputOutpuTree, 
						theController, 
						stalPathHighLiftPane, 
						newOutputCharts
						);
			}

	}

	public static void calculateHighLiftDistribution(
			InputOutputTree theInputOutpuTree,
			VaraiblesAnalyses theController,
			Pane chordPane,
			Pane xlePane,
			Pane a0lPane,
			Pane liftPane,
			TabPane newOutputCharts
			) {

		//Y ARRAY ----------------------------------------

		yAdimensionalArrayHighLiftModified = new ArrayList<>();
		Map<Double, Amount<Length>> mapDeltaChordFlap = new HashMap<>();
		Map<Double, Amount<Angle>> mapAdimensionalStationAlphaZeroLiftFlap = new HashMap<>(); // map aol
		Map<Double, Amount<Length>> mapDeltaChordSlat = new HashMap<>(); // map chord
		Map<Double, Amount<Length>> mapDimensionalStationXleSlat = new HashMap<>(); // map xle
		
		Map<Double, Double> mapDeltaClMaxFlap = new HashMap<>(); // map xle
		Map<Double, Double> mapDeltaClMaxSlat = new HashMap<>(); // map xle

		Map<Double, Double> mapClZeroFlap = new HashMap<>(); // map xle
		Map<Double, Double> mapClAlphaFlap = new HashMap<>(); // map xle

		// Setup database(s)
		HighLiftDatabaseReader highLiftDatabaseReader;
		AerodynamicDatabaseReader aeroDatabaseReader = null;
		// Setup database(s)

		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String highLiftDatabaseFileName = "HighLiftDatabase.h5";
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";

		highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);

		//----------------------------------------------------------------------
		// FILL Y VALUES -------------------------------------------------------
		// ---------------------------------------------------------------------

		//fill y array with input values----------------------------------------------

		theInputOutpuTree.getyAdimensionalStationInput().stream().forEach(yAd ->
		yAdimensionalArrayHighLiftModified.add(yAd)
				);

		// fill y array with flap values----------------------------------------------
		theInputOutpuTree.getFlapInnerStation().stream().forEach(yFlapAd ->{
			int i = theInputOutpuTree.getFlapInnerStation().indexOf(yFlapAd);

			if(yAdimensionalArrayHighLiftModified.contains(yFlapAd))
				yFlapAd=yFlapAd + 0.001;

			// calc delta flap
			Double flapDiscontinuity = 0.0;
			Double deltaCLFlap = 0.0;
			Double deltaCCfFlap = 0.0;
			Double cFirstC = 0.0;
			Double newClAlpha = 0.0;


			flapDiscontinuity = yFlapAd - 0.002;

			deltaCCfFlap = 
					highLiftDatabaseReader
					.getDeltaCCfVsDeltaFlap(
							theInputOutpuTree.getFlapDeflection().get(i).doubleValue(NonSI.DEGREE_ANGLE),
							theInputOutpuTree.getFlapTypeIndex().get(i)
							);


			cFirstC = 
					(1+(deltaCCfFlap*theInputOutpuTree.getFlapChordRatio().get(i)));

			newClAlpha = 
//					_clAlphaFlapList.get(i);
					(MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
					MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getClAlphaDegDistributionSemiSpan()),
					yFlapAd))* (
							cFirstC * (
									1-theInputOutpuTree.getFlapChordRatio().get(i) * Math.pow(
											Math.sin(theInputOutpuTree.getFlapDeflection().get(i).doubleValue(SI.RADIAN)),
											2
											)));


			yAdimensionalArrayHighLiftModified.add(flapDiscontinuity);
			yArrayForDeltaFlap.add(flapDiscontinuity);

			mapDeltaChordFlap.put(
					flapDiscontinuity,
					Amount.valueOf(
							0.0,
							SI.METER)
					);

			yArrayForDeltaFlap.add(yFlapAd);
			yAdimensionalArrayHighLiftModified.add(yFlapAd);

			mapDeltaChordFlap.put(
					yFlapAd,
					Amount.valueOf(
							(deltaCCfFlap*theInputOutpuTree.getFlapChordRatio().get(i)*
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
											MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
											yFlapAd)),
							SI.METER)
					);

			mapAdimensionalStationAlphaZeroLiftFlap.put(flapDiscontinuity, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getAlphaZeroLiftDistributionSemiSpan())),
									yFlapAd),
							NonSI.DEGREE_ANGLE)
					);

			mapAdimensionalStationAlphaZeroLiftFlap.put(yFlapAd, 
					Amount.valueOf(
							-((MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getClZeroDistributionSemispan()),
									yFlapAd) + _deltaCl0FlapList.get(i))/
									newClAlpha),
							NonSI.DEGREE_ANGLE)
					);
			
			mapDeltaClMaxFlap.put(
					flapDiscontinuity,
							0.0
					);
		
			mapDeltaClMaxFlap.put(
					yFlapAd,
							_deltaClmaxFlapList.get(i)
					);
			
			mapClZeroFlap.put(
					flapDiscontinuity,
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getClZeroDistributionSemispan()),
									yFlapAd)
					);
		
			mapClZeroFlap.put(
					yFlapAd,
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
							MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getClZeroDistributionSemispan()),
							yFlapAd) + 
					_deltaCl0FlapList.get(i)
					);
			
			mapClAlphaFlap.put(
					flapDiscontinuity,
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getClAlphaDegDistributionSemiSpan()),
									yFlapAd)
					);
		
			mapClAlphaFlap.put(
					yFlapAd,
					newClAlpha
					);
			
			
			
			
		}	
				);


		// fill y array with flap outer values----------------------------------------------
		theInputOutpuTree.getFlapOuterStation().stream().forEach(yFlapAd ->{
			int i = theInputOutpuTree.getFlapOuterStation().indexOf(yFlapAd);

			if(yAdimensionalArrayHighLiftModified.contains(yFlapAd))
				yFlapAd=yFlapAd-0.001;

			// calc delta flap
			Double flapDiscontinuity = 0.0;
			Double deltaCLFlap = 0.0;
			Double deltaCCfFlap = 0.0;
			Double cFirstC = 0.0;
			Double newClAlpha = 0.0;

			flapDiscontinuity = yFlapAd + 0.002;

			deltaCCfFlap = 
					highLiftDatabaseReader
					.getDeltaCCfVsDeltaFlap(
							theInputOutpuTree.getFlapDeflection().get(i).doubleValue(NonSI.DEGREE_ANGLE),
							theInputOutpuTree.getFlapTypeIndex().get(i)
							);


			cFirstC = 
					(1+(deltaCCfFlap*theInputOutpuTree.getFlapChordRatio().get(i)));

			newClAlpha = 
//					_clAlphaFlapList.get(i);
					(MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
					MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getClAlphaDegDistributionSemiSpan()),
					yFlapAd))* (
							cFirstC * (
									1-theInputOutpuTree.getFlapChordRatio().get(i) * Math.pow(
											Math.sin(theInputOutpuTree.getFlapDeflection().get(i).doubleValue(SI.RADIAN)),
											2
											)));

			yArrayForDeltaFlap.add(flapDiscontinuity);
			yAdimensionalArrayHighLiftModified.add(flapDiscontinuity);

			mapDeltaChordFlap.put(
					flapDiscontinuity,
					Amount.valueOf(
							0.0,
							SI.METER)
					);

			yArrayForDeltaFlap.add(yFlapAd);
			yAdimensionalArrayHighLiftModified.add(yFlapAd);

			mapDeltaChordFlap.put(
					yFlapAd,
					Amount.valueOf(
							(deltaCCfFlap*theInputOutpuTree.getFlapChordRatio().get(i)*
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
											MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
											yFlapAd)),
							SI.METER)
					);

			mapAdimensionalStationAlphaZeroLiftFlap.put(flapDiscontinuity, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getAlphaZeroLiftDistributionSemiSpan())),
									yFlapAd),
							NonSI.DEGREE_ANGLE)
					);

			mapAdimensionalStationAlphaZeroLiftFlap.put(yFlapAd, 
					Amount.valueOf(
							-((MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getClZeroDistributionSemispan()),
									yFlapAd) + _deltaCl0FlapList.get(i))/
									newClAlpha),
							NonSI.DEGREE_ANGLE)
					);

			mapDeltaClMaxFlap.put(
					flapDiscontinuity,
							0.0
					);
		
			mapDeltaClMaxFlap.put(
					yFlapAd,
							_deltaClmaxFlapList.get(i)
					);
			
			mapClZeroFlap.put(
					flapDiscontinuity,
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getClZeroDistributionSemispan()),
									yFlapAd)
					);
		
			mapClZeroFlap.put(
					yFlapAd,
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
							MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getClZeroDistributionSemispan()),
							yFlapAd) + 
					_deltaCl0FlapList.get(i)
					);
			
			mapClAlphaFlap.put(
					flapDiscontinuity,
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getClAlphaDegDistributionSemiSpan()),
									yFlapAd)
					);
		
			mapClAlphaFlap.put(
					yFlapAd,
					newClAlpha
					);
		}	
				);

		// fill y array with slat inner values----------------------------------------------
		yArrayForDeltaSlat.add(0.0);
		

		mapDeltaChordSlat.put(0.0,
				Amount.valueOf(
						0.0,
						SI.METER)
				);
		
		mapDimensionalStationXleSlat.put(0.0,
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
								MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getxLEDistributionSemiSpan())),
								0.0),
						SI.METER));
		
		mapDeltaClMaxSlat.put(0.0,
						0.0
				);
		
		theInputOutpuTree.getSlatInnerStation().stream().forEach(ySlatAd ->{
			int i = theInputOutpuTree.getSlatInnerStation().indexOf(ySlatAd);

			Double slatDiscontinuity = 0.0;

			if(yAdimensionalArrayHighLiftModified.contains(ySlatAd))
				ySlatAd=ySlatAd+0.001;

			slatDiscontinuity = ySlatAd - 0.002;


			mapDeltaChordSlat.put(slatDiscontinuity,
					Amount.valueOf(
							0.0,
							SI.METER)
					);

			mapDeltaChordSlat.put(ySlatAd,
					Amount.valueOf(
							((theInputOutpuTree.getSlatExtensionRatio().get(i)*
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
											MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
											ySlatAd)) - 
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
											MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
											ySlatAd)),
							SI.METER)
					);


			yAdimensionalArrayHighLiftModified.add(ySlatAd);
			yAdimensionalArrayHighLiftModified.add(slatDiscontinuity);
			yArrayForDeltaSlat.add(ySlatAd);
			yArrayForDeltaSlat.add(slatDiscontinuity);




			mapDimensionalStationXleSlat.put(slatDiscontinuity,
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getxLEDistributionSemiSpan())),
									slatDiscontinuity),
							SI.METER));


			mapDimensionalStationXleSlat.put(ySlatAd,
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getxLEDistributionSemiSpan())),
									ySlatAd) - 
							((theInputOutpuTree.getSlatExtensionRatio().get(i)*
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
											MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
											ySlatAd)) - 
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
											MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
											ySlatAd)),
							SI.METER));
			
			mapDeltaClMaxSlat.put(
					slatDiscontinuity,
							0.0
					);
		
			mapDeltaClMaxSlat.put(
					ySlatAd,
							_deltaClmaxSlatList.get(i)
					);

		}
				);


		// fill y array with slat outer values----------------------------------------------
		theInputOutpuTree.getSlatOuterStation().stream().forEach(ySlatAd ->{
			int i = theInputOutpuTree.getSlatOuterStation().indexOf(ySlatAd);

			Double slatDiscontinuity = 0.0;

			if(yAdimensionalArrayHighLiftModified.contains(ySlatAd))
				ySlatAd=ySlatAd-0.001;


			slatDiscontinuity = ySlatAd + 0.002;

			mapDeltaChordSlat.put(slatDiscontinuity,
					Amount.valueOf(
							0.0,
							SI.METER)
					);

			mapDeltaChordSlat.put(ySlatAd,
					Amount.valueOf(
							((theInputOutpuTree.getSlatExtensionRatio().get(i)*
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
											MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
											ySlatAd)) - 
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
											MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
											ySlatAd)),
							SI.METER)
					);


			yAdimensionalArrayHighLiftModified.add(ySlatAd);
			yAdimensionalArrayHighLiftModified.add(slatDiscontinuity);
			yArrayForDeltaSlat.add(ySlatAd);
			yArrayForDeltaSlat.add(slatDiscontinuity);




			mapDimensionalStationXleSlat.put(slatDiscontinuity,
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getxLEDistributionSemiSpan())),
									slatDiscontinuity),
							SI.METER));


			mapDimensionalStationXleSlat.put(ySlatAd,
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getxLEDistributionSemiSpan())),
									ySlatAd) - 
							((theInputOutpuTree.getSlatExtensionRatio().get(i)*
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
											MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
											ySlatAd)) - 
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
											MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
											ySlatAd)),
							SI.METER));
			
			mapDeltaClMaxSlat.put(
					slatDiscontinuity,
							0.0
					);
		
			mapDeltaClMaxSlat.put(
					ySlatAd,
							_deltaClmaxSlatList.get(i)
					);

		}
				);

		yArrayForDeltaSlat.add(theInputOutpuTree.getyAdimensionalStationInput().get(theInputOutpuTree.getNumberOfSections()-1));

		mapDeltaChordSlat.put(theInputOutpuTree.getyAdimensionalStationInput().get(theInputOutpuTree.getNumberOfSections()-1),
				Amount.valueOf(
						0.0,
						SI.METER)
				);
		
		mapDimensionalStationXleSlat.put(theInputOutpuTree.getyAdimensionalStationInput().get(theInputOutpuTree.getNumberOfSections()-1),
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
								MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getxLEDistributionSemiSpan())),
								theInputOutpuTree.getyAdimensionalStationInput().get(theInputOutpuTree.getNumberOfSections()-1)),
						SI.METER));
		
		mapDeltaClMaxSlat.put(theInputOutpuTree.getyAdimensionalStationInput().get(theInputOutpuTree.getNumberOfSections()-1),
						0.0
				);
		
		// sort y Array

		yAappNew = new Double[yAdimensionalArrayHighLiftModified.size()];

		yAdimensionalArrayHighLiftModified.stream().forEach(y ->{
			int i = yAdimensionalArrayHighLiftModified.indexOf(y);
			yAappNew[i] = y;
		});
		Arrays.sort(yAappNew);
		yAdimensionalArrayHighLiftModified = new ArrayList<>();
		for(int i=0; i<yAappNew.length; i++) {
			yAdimensionalArrayHighLiftModified.add(i, yAappNew[i]);
		} 

		//-----------------------------------------------------------------------
		// Interpolating values for new array

		Double [] chordBaseLine = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()), 
				MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutpuTree.getChordDistributionSemiSpan()), 
				MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified)
				);

		Double [] clMaxBaseLine = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()), 
				MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getMaximumliftCoefficientDistributionSemiSpan()), 
				MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified)
				);

		// flap (chord, alpha zero lift)----------

		yAappNew = new Double[yArrayForDeltaFlap.size()];

		yArrayForDeltaFlap.stream().forEach(y ->{
			int i = yArrayForDeltaFlap.indexOf(y);
			yAappNew[i] = y;
		});
		Arrays.sort(yAappNew);
		yArrayForDeltaFlap = new ArrayList<>();
		for(int i=0; i<yAappNew.length; i++) {
			yArrayForDeltaFlap.add(i, yAappNew[i]);
		} 

		yAappNew = new Double[yArrayForDeltaSlat.size()];

		yArrayForDeltaSlat.stream().forEach(y ->{
			int i = yArrayForDeltaSlat.indexOf(y);
			yAappNew[i] = y;
		});
		Arrays.sort(yAappNew);
		yArrayForDeltaSlat = new ArrayList<>();
		for(int i=0; i<yAappNew.length; i++) {
			yArrayForDeltaSlat.add(i, yAappNew[i]);
		} 


		Double [] deltaChordFlapSorted = new Double [yArrayForDeltaFlap.size()];
		Double [] alphaZeroLiftSorted = new Double [yArrayForDeltaFlap.size()];
		Double [] clMaxSorted = new Double [yArrayForDeltaFlap.size()];
		Double [] clZeroSorted = new Double [yArrayForDeltaFlap.size()];
		Double [] clAlphaSorted = new Double [yArrayForDeltaFlap.size()];

		yArrayForDeltaFlap.stream().forEach(y ->{
			int i = yArrayForDeltaFlap.indexOf(y);
			deltaChordFlapSorted[i] = mapDeltaChordFlap.get(y).doubleValue(SI.METER);
			alphaZeroLiftSorted[i] = mapAdimensionalStationAlphaZeroLiftFlap.get(y).doubleValue(SI.RADIAN);
			clMaxSorted[i] = mapDeltaClMaxFlap.get(y);
			clAlphaSorted[i] = mapClAlphaFlap.get(y);
			clZeroSorted[i] = mapClZeroFlap.get(y);
		});

		Double [] deltaChordsFlapInterpolated = 
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(yArrayForDeltaFlap), 
						MyArrayUtils.convertToDoublePrimitive(deltaChordFlapSorted), 
						MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified)
						);

		Double [] alphaZeroLiftInterpolated = 
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(yArrayForDeltaFlap), 
						MyArrayUtils.convertToDoublePrimitive(alphaZeroLiftSorted), 
						MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified)
						);

		Double [] deltaClMaxFlapInterpolated = 
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(yArrayForDeltaFlap), 
						MyArrayUtils.convertToDoublePrimitive(clMaxSorted), 
						MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified)
						);
		
		Double [] clZeroFlapInterpolated = 
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(yArrayForDeltaFlap), 
						MyArrayUtils.convertToDoublePrimitive(clZeroSorted), 
						MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified)
						);
		
		Double [] clAlphaFlapInterpolated = 
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(yArrayForDeltaFlap), 
						MyArrayUtils.convertToDoublePrimitive(clAlphaSorted), 
						MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified)
						);
		
		// slat (chord, xle)------------
		Double [] deltaChordSlatSorted = new Double [yArrayForDeltaSlat.size()];
		Double [] xleSlatSorted = new Double [yArrayForDeltaSlat.size()];
		Double [] deltaClMaxSlatSorted = new Double [yArrayForDeltaSlat.size()];

		yArrayForDeltaSlat.stream().forEach(y ->{
			int i = yArrayForDeltaSlat.indexOf(y);
			deltaChordSlatSorted[i] = mapDeltaChordSlat.get(y).doubleValue(SI.METER);
			xleSlatSorted[i] = mapDimensionalStationXleSlat.get(y).doubleValue(SI.METER);
			deltaClMaxSlatSorted[i] = mapDeltaClMaxSlat.get(y);
		}
				);

		Double [] deltaChordsSlatInterpolated = 
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(yArrayForDeltaSlat), 
						MyArrayUtils.convertToDoublePrimitive(deltaChordSlatSorted), 
						MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified)
						);

		Double [] xleInterpolated = 
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(yArrayForDeltaSlat), 
						MyArrayUtils.convertToDoublePrimitive(xleSlatSorted), 
						MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified)
						);
		

		Double [] deltaClMaxSlatInterpolated = 
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(yArrayForDeltaSlat), 
						MyArrayUtils.convertToDoublePrimitive(deltaClMaxSlatSorted), 
						MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified)
						);

		Double [] chordArrayFinal = new Double [yAdimensionalArrayHighLiftModified.size()];
		Double [] clMaxArrayFinal = new Double [yAdimensionalArrayHighLiftModified.size()];
		// final Chord Array
		yAdimensionalArrayHighLiftModified.stream().forEach(y ->{
			int i = yAdimensionalArrayHighLiftModified.indexOf(y);
			chordArrayFinal [i] = chordBaseLine[i] + deltaChordsFlapInterpolated[i]+ deltaChordsSlatInterpolated[i];
		});
		//final Cl max array
		yAdimensionalArrayHighLiftModified.stream().forEach(y ->{
			int i = yAdimensionalArrayHighLiftModified.indexOf(y);
			clMaxArrayFinal [i] = clMaxBaseLine[i] + deltaClMaxFlapInterpolated[i]+ deltaClMaxSlatInterpolated[i];
		});
		
		Double [] twist = 
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()), 
					    MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutpuTree.getTwistDistributionSemiSpan()), 
						MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified)
						);
		
		chordDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		alphaZeroLiftDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		xleDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		dihedralxleDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		twistDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		yMeterDimensionalDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		
		clMaxDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		alphaStallDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		
		clZeroDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		clAlphaDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];

		//		System.out.println(" chord baseline " + Arrays.toString(chordBaseLine) );
//		System.out.println(" y station " + yAdimensionalArrayHighLiftModified.toString());
//		System.out.println(" chord distribution " + Arrays.toString(chordArrayFinal));
//		System.out.println(" alpha zero lift distribution " + Arrays.toString(alphaZeroLiftInterpolated));
//		System.out.println(" twist distribution " + Arrays.toString(twist));
//		System.out.println(" xle distribution " + Arrays.toString(xleInterpolated));



		theInputOutpuTree.getyAdimensionalDistributionSemiSpan().stream().forEach(yADold ->{	
			int i = theInputOutpuTree.getyAdimensionalDistributionSemiSpan().indexOf(yADold);

			chordDistributionHighLiftFinal[i] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified), 
					MyArrayUtils.convertToDoublePrimitive(chordArrayFinal),
					yADold);

			alphaZeroLiftDistributionHighLiftFinal[i] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified), 
					MyArrayUtils.convertToDoublePrimitive(alphaZeroLiftInterpolated),
					yADold);

			xleDistributionHighLiftFinal[i] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified), 
					MyArrayUtils.convertToDoublePrimitive(xleInterpolated),
					yADold);

			dihedralxleDistributionHighLiftFinal[i] = theInputOutpuTree.getDihedralDistributionSemiSpan().get(i).doubleValue(SI.RADIAN);

			twistDistributionHighLiftFinal[i] = theInputOutpuTree.getTwistDistributionSemiSpan().get(i).doubleValue(SI.RADIAN);

			clMaxDistributionHighLiftFinal[i] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified), 
					MyArrayUtils.convertToDoublePrimitive(clMaxArrayFinal),
					yADold);
			
			clZeroDistributionHighLiftFinal[i] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified), 
					MyArrayUtils.convertToDoublePrimitive(clZeroFlapInterpolated),
					yADold);
					
			clAlphaDistributionHighLiftFinal[i] =  MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified), 
					MyArrayUtils.convertToDoublePrimitive(clAlphaFlapInterpolated),
					yADold);
			
			alphaStallDistributionHighLiftFinal[i] =
					((clMaxDistributionHighLiftFinal[i] - clZeroDistributionHighLiftFinal[i]) / 
					clAlphaDistributionHighLiftFinal[i]) + 
					theInputOutpuTree.getDeltaAlphaStallaSemispan().get(i).doubleValue(NonSI.DEGREE_ANGLE)
					;

		});

		System.out.println(" y station " + yAdimensionalArrayHighLiftModified.toString());
		System.out.println(" cl max final " + Arrays.toString(clMaxDistributionHighLiftFinal));
		
		// creating new array for dihedral and twist with new y stations.		
		double vortexSemiSpanToSemiSpanRatio = (1./(2*theInputOutpuTree.getNumberOfPointSemispan()));

		// new surface 
		
		Double flapSurfaceMeter = 0.0;
		
		for (int i=0; i<theInputOutpuTree.getFlapChordRatio().size(); i++) {
			flapSurfaceMeter = flapSurfaceMeter + 
					( ( 
							(theInputOutpuTree.getFlapOuterStation().get(i) - theInputOutpuTree.getFlapInnerStation().get(i)) *
							theInputOutpuTree.getSemiSpan().doubleValue(SI.METER)) *
							mapDeltaChordFlap.get(theInputOutpuTree.getFlapInnerStation().get(i)).doubleValue(SI.METER));
		}
		
		Double slatSurfaceMeter = 0.0;
		
		for (int i=0; i<theInputOutpuTree.getSlatChordRatio().size(); i++) {
			slatSurfaceMeter = slatSurfaceMeter + 
					( ( 
							(theInputOutpuTree.getSlatOuterStation().get(i) - theInputOutpuTree.getSlatInnerStation().get(i)) *
							theInputOutpuTree.getSemiSpan().doubleValue(SI.METER)) *
							mapDeltaChordSlat.get(theInputOutpuTree.getSlatInnerStation().get(i)).doubleValue(SI.METER));
		}
		
		Double totalSurfaceMeter = theInputOutpuTree.getSurface().doubleValue(SI.SQUARE_METRE) + flapSurfaceMeter + slatSurfaceMeter;
		 
		theInputOutpuTree.setNewChordDistributionMeter(chordDistributionHighLiftFinal);
		double[] alphaZeroLiftDistributionDeg = new double[alphaZeroLiftDistributionHighLiftFinal.length];
		for(int i=0; i<alphaZeroLiftDistributionHighLiftFinal.length; i++) {
			alphaZeroLiftDistributionDeg[i] = alphaZeroLiftDistributionHighLiftFinal[i]*57.3;
		}
		theInputOutpuTree.setNewAlphaZeroLiftDistributionDeg(alphaZeroLiftDistributionDeg);
		theInputOutpuTree.setNewXLEDisributionMeter(xleDistributionHighLiftFinal);
		
		theNasaBlackwellHIGHLIFTCalculator = new  calculators.aerodynamics.NasaBlackwell(
				theInputOutpuTree.getSemiSpan().doubleValue(SI.METER),
				theInputOutpuTree.getSurface().doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutpuTree.getyDimensionalDistributionSemiSpan()),
				chordDistributionHighLiftFinal,
				xleDistributionHighLiftFinal, 
				dihedralxleDistributionHighLiftFinal, 
				twistDistributionHighLiftFinal,
				alphaZeroLiftDistributionHighLiftFinal, 
				vortexSemiSpanToSemiSpanRatio,
				0.0,
				theInputOutpuTree.getMachNumber(),
				theInputOutpuTree.getAltitude().doubleValue(SI.METER));
		
//		calculators.aerodynamics.NasaBlackwell theNasaBlackwellCalculator = new  calculators.aerodynamics.NasaBlackwell(
//				theInputOutpuTree.getSemiSpan().doubleValue(SI.METER),
//				totalSurfaceMeter,
//				MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutpuTree.getyDimensionalDistributionSemiSpan()),
//				chordDistributionHighLiftFinal,
//				xleDistributionHighLiftFinal, 
//				dihedralxleDistributionHighLiftFinal, 
//				twistDistributionHighLiftFinal,
//				alphaZeroLiftDistributionHighLiftFinal, 
//				vortexSemiSpanToSemiSpanRatio,
//				0.0,
//				theInputOutpuTree.getMachNumber(),
//				theInputOutpuTree.getAltitude().doubleValue(SI.METER));



		// calculating curves

		for (int i=0; i<theInputOutpuTree.getAlphaArrayHighLiftDistribution().size(); i++){

			theNasaBlackwellHIGHLIFTCalculator.calculate(theInputOutpuTree.getAlphaArrayHighLiftDistribution().get(i));
			List<Double> clList = new ArrayList<>();
			// chords new
//			clList = Main.convertDoubleArrayToListDouble(
//					Main.convertFromDoubleToPrimitive(
//							theNasaBlackwellCalculator.getClTotalDistribution().toArray()));
			
			// chords old
			clList = Main.convertDoubleArrayToListDouble(
					Main.convertFromDoubleToPrimitive(
							theNasaBlackwellHIGHLIFTCalculator.get_ccLDistribution().toArray()));
			for(int j =0; j<clList.size(); j++) {
				clList.set(j, clList.get(j)/theInputOutpuTree.getChordDistributionSemiSpan().get(j).doubleValue(SI.METER));
			}
			
			
			
			if (clList.get(i).isNaN()){
				for (int ii=0; ii< clList.size(); ii++){
					clList.set(ii, 0.0);
				}
			}

			theInputOutpuTree.getClDistributionCurvesHighLift().add(i, 
					clList);

			
//			System.out.println(" alpha = " + theInputOutpuTree.getAlphaArrayHighLiftDistribution().get(i) + " cl " + clList);
		}
		
		// 3D curves
		
		List<Double> cL3DHighLiftModified = new ArrayList<>();
		
		theInputOutpuTree.getAlphaArrayHighLiftDistribution().stream().forEach( a -> {
			int i = theInputOutpuTree.getAlphaArrayHighLiftDistribution().indexOf(a);
			double [] integral = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
			
			for(int j=0; j<theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size(); j++) {
				integral[j] = theInputOutpuTree.getClDistributionCurvesHighLift().get(i).get(j) * theInputOutpuTree.getChordDistributionSemiSpan().get(j).doubleValue(SI.METER);
			}
			cL3DHighLiftModified.add(			
					(2/theInputOutpuTree.getSurface().doubleValue(SI.SQUARE_METRE)) * 
					(MyMathUtils.integrate1DTrapezoidLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutpuTree.getyDimensionalDistributionSemiSpan()), 
							integral, 
							0.0,
							theInputOutpuTree.getSemiSpan().doubleValue(SI.METER)-0.001
							)) 
					);
		});
		
		theInputOutpuTree.setLiftCoefficient3DCurveHighLiftModified(cL3DHighLiftModified);
		
		// chart
		
		//chord
		List<List<Double>> xList = new ArrayList<>();
		List<List<Double>> yList = new ArrayList<>();
		List<String> legend = new ArrayList<>();
		
		xList.add(theInputOutpuTree.getyAdimensionalDistributionSemiSpan());
		xList.add(theInputOutpuTree.getyAdimensionalDistributionSemiSpan());
		
		yList.add(MyArrayUtils.convertDoubleArrayToListDouble(
				MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())));
		
		yList.add(MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertFromDoubleToPrimitive(chordDistributionHighLiftFinal)));
		
		
		legend.add("Clean configuration");
		legend.add("With High Lift Devices");
		
		
		Node chartChord = displayChartNode(
				"Chord distribution",
				"eta",
				"Chord (m)", 
				150, 
				750,
				true,
				Side.BOTTOM,
				legend, 
				xList, 
				yList, 
				newOutputCharts.getTabs().get(1).getContent()
				);
		
		chordPane.getChildren().clear();
		chordPane.getChildren().add(chartChord);
		
		
		//xle
		xList = new ArrayList<>();
		yList = new ArrayList<>();
		legend = new ArrayList<>();
		
		xList.add(theInputOutpuTree.getyAdimensionalDistributionSemiSpan());
		xList.add(theInputOutpuTree.getyAdimensionalDistributionSemiSpan());
		
		yList.add(MyArrayUtils.convertDoubleArrayToListDouble(
				MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getxLEDistributionSemiSpan())));
		yList.add(MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertFromDoubleToPrimitive(xleDistributionHighLiftFinal)));
		
		legend.add("Clean configuration");
		legend.add("With High Lift Devices");
		
		Node xleChart = displayChartNode(
				"Xle distribution",
				"eta",
				"Xle (m)", 
				150, 
				750,
				true,
				Side.BOTTOM,
				legend, 
				xList, 
				yList, 
				newOutputCharts.getTabs().get(2).getContent()
				);
		
		
		xlePane.getChildren().clear();
		xlePane.getChildren().add(xleChart);
		
		//a0l
		

		double[] alphazeroLiftdeg = new double [alphaZeroLiftDistributionHighLiftFinal.length];
		for(int i=0; i<alphazeroLiftdeg.length; i++) {
			alphazeroLiftdeg [i] = Math.toDegrees(alphaZeroLiftDistributionHighLiftFinal[i]);
		}
		
		xList = new ArrayList<>();
		yList = new ArrayList<>();
		legend = new ArrayList<>();
		
		xList.add(theInputOutpuTree.getyAdimensionalDistributionSemiSpan());
		xList.add(theInputOutpuTree.getyAdimensionalDistributionSemiSpan());
		
		yList.add(MyArrayUtils.convertDoubleArrayToListDouble(
				MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getAlphaZeroLiftDistributionSemiSpan())));
		yList.add(MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertFromDoubleToPrimitive(alphazeroLiftdeg)));
		
		legend.add("Clean configuration");
		legend.add("With High Lift Devices");
		
		Node alphaZeroLiftChart = displayChartNode(
				"Alpha Zero lift distribution",
				"eta",
				"a0l (°)", 
				150, 
				750,
				true,
				Side.BOTTOM,
				legend, 
				xList, 
				yList, 
				newOutputCharts.getTabs().get(2).getContent()
				);
		
		a0lPane.getChildren().clear();
		a0lPane.getChildren().add(alphaZeroLiftChart);
		//lift
		xList = new ArrayList<>();
		yList = new ArrayList<>();
		legend = new ArrayList<>();
		

		for (int i =0; i<theInputOutpuTree.getAlphaArrayHighLiftDistribution().size(); i++) {
		xList.add(theInputOutpuTree.getyAdimensionalDistributionSemiSpan());
		yList.add(theInputOutpuTree.getClDistributionCurvesHighLift().get(i));
		legend.add("cl distribution at alpha (deg) " + theInputOutpuTree.getAlphaArrayHighLiftDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE));
		}

		Node chart = displayChartNode(
				"Lift distribution",
				"alpha",
				"Cl", 
				150, 
				750,
				true,
				Side.BOTTOM,
				legend, 
				xList, 
				yList, 
				newOutputCharts.getTabs().get(3).getContent()
				);


		liftPane.getChildren().clear();
		liftPane.getChildren().add(chart);
		
		textOutputLift.appendText("\n\nHIGH LIFT DISTRIBUTION");
		textOutputLift.appendText("\nEta Stations (deg) --> " + theInputOutpuTree.getyAdimensionalDistributionSemiSpan().toString());
		textOutputLift.appendText("\nOld chord distribution (m) --> " + Arrays.toString(MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())));
		textOutputLift.appendText("\nNew chord distribution (m) --> " + Arrays.toString(chordDistributionHighLiftFinal));
		textOutputLift.appendText("\nOld xle distribution (m) --> " + Arrays.toString(MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutpuTree.getxLEDistributionSemiSpan())));
		textOutputLift.appendText("\nNew xle distribution (m) --> " + Arrays.toString(xleDistributionHighLiftFinal));
		textOutputLift.appendText("\nOld alpha zero lift distribution (°) --> " + Arrays.toString(MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutpuTree.getAlphaZeroLiftDistributionSemiSpan())));
		textOutputLift.appendText("\nNew alpha zero lift distribution (°) --> " + Arrays.toString(alphazeroLiftdeg));
		textOutputLift.appendText("\nOld Cl max distribution --> " + theInputOutpuTree.getMaximumliftCoefficientDistributionSemiSpan().toString());
		textOutputLift.appendText("\nNew Cl max distribution --> " + Arrays.toString(clMaxDistributionHighLiftFinal));
		textOutputLift.appendText("\nNew Alpha stall distribution --> " + Arrays.toString(alphaStallDistributionHighLiftFinal));
		for(int i=0; i<theInputOutpuTree.getAlphaArrayHighLiftDistribution().size(); i++){
			textOutputLift.appendText("\nCl distribution at alpha = " + 
					theInputOutpuTree.getAlphaArrayHighLiftDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + 
					" deg ---> " + theInputOutpuTree.getClDistributionCurvesHighLift().get(i) + "\n");
		}
		
		textOutputLift.appendText("\nAlpha array (°) --> " + Arrays.toString(MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutpuTree.getAlphaArrayHighLiftDistribution())));
		textOutputLift.appendText("\nCL high lift modified --> " + Arrays.toString(MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getLiftCoefficient3DCurveHighLiftModified())));
	}


	public static void displayChart(
			String chartTitle, 
			String xAxisName,
			String yAxisName,
			double minHeight,
			double minWidth,
			boolean legendVisible,
			Side legendSide,
			List<String> legendName,
			List<List<Double>> xValues,
			List<List<Double>> yValues,
			ObservableList<Node> chartPosition) {

		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel(xAxisName);
		yAxis.setLabel(yAxisName);

		final LineChart<Number,Number> lineChart = 
				new LineChart<Number,Number>(xAxis,yAxis);

		lineChart.setTitle(chartTitle);

		lineChart.setMinHeight(minHeight);
		lineChart.setMinWidth(minWidth);
		lineChart.setLegendSide(legendSide);
		lineChart.setLegendVisible(legendVisible);

		int numberOfSeries = xValues.size();

		for (int i=0; i<numberOfSeries; i++){
			XYChart.Series series = new XYChart.Series();
			series.setName(legendName.get(i));

			int numberOfValues = xValues.get(i).size();

			for (int ii=0; ii<numberOfValues; ii++){
				series.getData().add(new XYChart.Data(xValues.get(i).get(ii), yValues.get(i).get(ii)));
			}
			lineChart.getData().add(series);
		}

		chartPosition.clear();
		chartPosition.add(lineChart);
	}

	public static Node displayChartNode(
			String chartTitle, 
			String xAxisName,
			String yAxisName,
			double minHeight,
			double minWidth,
			boolean legendVisible,
			Side legendSide,
			List<String> legendName,
			List<List<Double>> xValues,
			List<List<Double>> yValues,
			Node chartPosition) {

		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel(xAxisName);
		yAxis.setLabel(yAxisName);

		final LineChart<Number,Number> lineChart = 
				new LineChart<Number,Number>(xAxis,yAxis);

		lineChart.setTitle(chartTitle);

		lineChart.setMinHeight(minHeight);
		lineChart.setMinWidth(minWidth);
		lineChart.setLegendSide(legendSide);
		lineChart.setLegendVisible(legendVisible);


		int numberOfSeries = xValues.size();

		for (int i=0; i<numberOfSeries; i++){
			XYChart.Series series = new XYChart.Series();
			series.setName(legendName.get(i));

			int numberOfValues = xValues.get(i).size();

			for (int ii=0; ii<numberOfValues; ii++){
				series.getData().add(new XYChart.Data(xValues.get(i).get(ii), yValues.get(i).get(ii)));
			}
			lineChart.getData().add(series);
		}

		chartPosition = lineChart;
		return chartPosition;

	}
	public static Double[] calculateCLvsAlphaArray(
			double cL0,
			double cLmax,
			Amount<Angle> alphaStar,
			Amount<Angle> alphaStall,
			Amount<?> cLAlpha,
			Double[] alphaArray
			) {

		Double[] cLArray = new Double[alphaArray.length];

		double a = 0.0;
		double b = 0.0;
		double c = 0.0;
		double d = 0.0;
		double e = 0.0;


		double cLStar = (cLAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
				* alphaStar.doubleValue(NonSI.DEGREE_ANGLE))
				+ cL0;
		for(int i=0; i<alphaArray.length; i++) {
			if(alphaArray[i] <= alphaStar.doubleValue(NonSI.DEGREE_ANGLE)) {
				cLArray[i] = (cLAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
						* alphaArray[i])
						+ cL0;
			}
			else {
				double[][] matrixData = { 
						{Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 4),
							Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 3),
							Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
							alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
							1.0},
						{4* Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 3),
								3* Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
								2*alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
								1.0,
								0.0},
						{Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 4),
									Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 3),
									Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
									alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
									1.0},
						{4* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 3),
										3* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
										2*alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
										1.0,
										0.0},
						{12* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
											6*alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
											2.0,
											0.0,
											0.0},
				};

				RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
				double [] vector = {
						cLmax,
						0,
						cLStar,
						cLAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(),
						0
				};

				double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

				a = solSystem[0];
				b = solSystem[1];
				c = solSystem[2];
				d = solSystem[3];
				e = solSystem[4];

				cLArray[i] = a * Math.pow(alphaArray[i], 4) + 
						b * Math.pow(alphaArray[i], 3) + 
						c* Math.pow(alphaArray[i], 2) +
						d * alphaArray[i]+
						e;
			}
		}

		return cLArray;
	}

	public static void performStallPath(
			InputOutputTree theInputOutputTree, 
			VaraiblesAnalyses theController,
			Pane stallPathPane,
			TabPane newOutputCharts){

		theInputOutputTree.setClMaxAirfoils(theInputOutputTree.getMaximumliftCoefficientDistributionSemiSpan());

		theNasaBlackwellCalculator.calculate(theInputOutputTree.getAlphaMaxLinear());

		theInputOutputTree.setClMaxStallPath(
				MyArrayUtils.convertDoubleArrayToListDouble(
						MyArrayUtils.convertFromDoubleToPrimitive(
								theNasaBlackwellCalculator.getClTotalDistribution().toArray())));



		List<List<Double>> xList = new ArrayList<>();
		List<List<Double>> yList = new ArrayList<>();
		List<String> legend = new ArrayList<>();

		xList.add(theInputOutputTree.getyAdimensionalDistributionSemiSpan());
		xList.add(theInputOutputTree.getyAdimensionalDistributionSemiSpan());

		yList.add(theInputOutputTree.getClMaxAirfoils());
		yList.add(theInputOutputTree.getClMaxStallPath());

		legend.add("cl max airfoils");
		legend.add("cl distribution at max alpha linear = " +  theInputOutputTree.getAlphaMaxLinear().doubleValue(NonSI.DEGREE_ANGLE) + " deg");

		Node chart = displayChartNode(
				"Lift Curve",
				"alpha",
				"CL", 
				150, 
				750,
				true,
				Side.BOTTOM,
				legend, 
				xList, 
				yList, 
				newOutputCharts.getTabs().get(0).getContent()
				);


		stallPathPane.getChildren().clear();
		stallPathPane.getChildren().add(chart);


		textOutputLift.appendText("\n\nSTALL PATH");
		textOutputLift.appendText("\nEta Stations (deg) --> " + theInputOutputTree.getyAdimensionalDistributionSemiSpan().toString());
		textOutputLift.appendText("\nCl max airfoils (deg) --> " + theInputOutputTree.getClMaxAirfoils().toString());
		textOutputLift.appendText("\nEta Stations (deg) --> " + theInputOutputTree.getClMaxStallPath().toString());
		textOutputLift.appendText("\n\n--------End of " + theController.getRunLift() + "st Run------------\n\n");


	}

	public static void performHighLiftStallPath(
			InputOutputTree theInputOutputTree, 
			VaraiblesAnalyses theController,
			Pane stallPathPane,
			TabPane newOutputCharts){
		// Setup database(s)
		AerodynamicDatabaseReader aeroDatabaseReader;
		
		// Setup database(s)

		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";

		aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		
		List<Amount<Angle>> alphaArrayForCalculation =
				MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(0, 25, 26),
				NonSI.DEGREE_ANGLE
				);

		//NASA BLACKWELL CLEAN
		double vortexSemiSpanToSemiSpanRatio = (1./(2*theInputOutputTree.getNumberOfPointSemispan()));

		double [] twistDistributionRadians = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] alphaZeroLiftDistributionRadians = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] dihedralDistributionRadians = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] yDistributionMeter = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] chordDistributionMeter = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] xleDistributionMeter = new double[theInputOutputTree.getNumberOfPointSemispan()];

		for (int i=0; i< theInputOutputTree.getNumberOfPointSemispan(); i++) {
			twistDistributionRadians[i] = theInputOutputTree.getTwistDistributionSemiSpan().get(i).doubleValue(SI.RADIAN);
			alphaZeroLiftDistributionRadians[i] = theInputOutputTree.getAlphaZeroLiftDistributionSemiSpan().get(i).doubleValue(SI.RADIAN);
			dihedralDistributionRadians [i] = theInputOutputTree.getDihedralDistributionSemiSpan().get(i).doubleValue(SI.RADIAN);
			yDistributionMeter [i] = theInputOutputTree.getyDimensionalDistributionSemiSpan().get(i).doubleValue(SI.METER);
			chordDistributionMeter [i] = theInputOutputTree.getChordDistributionSemiSpan().get(i).doubleValue(SI.METER);
			xleDistributionMeter [i] = theInputOutputTree.getxLEDistributionSemiSpan().get(i).doubleValue(SI.METER);
		}
		calculators.aerodynamics.NasaBlackwell theNasaBlackwellCleanCalculator = new  calculators.aerodynamics.NasaBlackwell(
				theInputOutputTree.getSemiSpan().doubleValue(SI.METER), 
				theInputOutputTree.getSurface().doubleValue(SI.SQUARE_METRE),
				yDistributionMeter,
				chordDistributionMeter,
				xleDistributionMeter,
				dihedralDistributionRadians,
				twistDistributionRadians,
				alphaZeroLiftDistributionRadians,
				vortexSemiSpanToSemiSpanRatio,
				0.0,
				theInputOutputTree.getMachNumber(),
				theInputOutputTree.getAltitude().doubleValue(SI.METER));
		
//-------------------------------------------------------
		Double[][] deltaClLocal = new Double [26][theInputOutputTree.getNumberOfPointSemispan()];
		List<Double> temporaryClList = new ArrayList<>();
		
		List<List<Double>> deltaClStationWithRespectToAlpha = new ArrayList<>();
		
		alphaArrayForCalculation.stream().forEach( a ->{
			int i = alphaArrayForCalculation.indexOf(a);
			
			List<Double> clDistributionHighLift = new ArrayList<>();
			List<Double> clDistributonClean = new ArrayList<>();
		
            theNasaBlackwellCleanCalculator.calculate(alphaArrayForCalculation.get(i));
			theNasaBlackwellHIGHLIFTCalculator.calculate(alphaArrayForCalculation.get(i));
			
			clDistributonClean = Main.convertDoubleArrayToListDouble(
					Main.convertFromDoubleToPrimitive(
							theNasaBlackwellCleanCalculator.getClTotalDistribution().toArray()));
			//---------
//			clDistributionHighLift = Main.convertDoubleArrayToListDouble(
//					Main.convertFromDoubleToPrimitive(
//							theNasaBlackwellHIGHLIFTCalculator.getClTotalDistribution().toArray()));
			//-----------

			
			// chords old
			clDistributionHighLift = Main.convertDoubleArrayToListDouble(
					Main.convertFromDoubleToPrimitive(
							theNasaBlackwellHIGHLIFTCalculator.get_ccLDistribution().toArray()));
			for(int j =0; j<clDistributionHighLift.size(); j++) {
				clDistributionHighLift.set(j, clDistributionHighLift.get(j)/theInputOutputTree.getChordDistributionSemiSpan().get(j).doubleValue(SI.METER));
			}
			

			if (clDistributionHighLift.get(i).isNaN()){
				for (int ii=0; ii< clDistributionHighLift.size(); ii++){
					clDistributionHighLift.set(ii, 0.0);
				}
			}
			//---
			for(int ii=0; ii<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); ii++) {
				deltaClLocal[i][ii] =  
						clDistributionHighLift.get(ii)-clDistributonClean.get(ii);
			}
			
		}
				);
			for(int i=0; i<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); i++) {
				for(int ii=0; ii<alphaArrayForCalculation.size(); ii++) {		
					temporaryClList.add(deltaClLocal[ii][i]);
				}
				deltaClStationWithRespectToAlpha.add(temporaryClList);
				temporaryClList = new ArrayList<>();
			}
		
//			--
// NEW CL MAX DISTRIBUTION
			
			double [] deltaClAtStall = new double[theInputOutputTree.getNumberOfPointSemispan()];
			
			
//			theInputOutputTree.getyAdimensionalDistributionSemiSpan().stream().forEach(eta ->{
//				int i = theInputOutputTree.getyAdimensionalDistributionSemiSpan().indexOf(eta);
			
			for(int i=0; i<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); i++) {
				
				// new delta 
				deltaClAtStall[i] = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(alphaArrayForCalculation)),
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(deltaClStationWithRespectToAlpha.get(i))),
						theInputOutputTree.getAlphaStallDistributionSemiSpan().get(i).doubleValue(NonSI.DEGREE_ANGLE));
				
				// new cl max distribution high lift
				theInputOutputTree.getMaximumliftCoefficientDistributionSemiSpanHighLift().add(
						i, 
						theInputOutputTree.getMaximumliftCoefficientDistributionSemiSpan().get(i) + deltaClAtStall[i]);
			}
			//);
			
			System.out.println("eta station " + theInputOutputTree.getyAdimensionalDistributionSemiSpan().toString());
			System.out.println(" old cl max distribution " + theInputOutputTree.getMaximumliftCoefficientDistributionSemiSpan().toString());
			System.out.println(" new cl max distribution high lift " + theInputOutputTree.getMaximumliftCoefficientDistributionSemiSpanHighLift().toString());

			//3D CURVE----------
			
			// CL ALPHA

			Amount<Angle> alphaFirst = Amount.valueOf(Math.toRadians(2.0), SI.RADIAN);
			Amount<Angle> alphaSecond = Amount.valueOf(Math.toRadians(4.0), SI.RADIAN);

			theNasaBlackwellHIGHLIFTCalculator.calculate(alphaFirst);
			double [] clDistribution = theNasaBlackwellHIGHLIFTCalculator.getClTotalDistribution().toArray();
			double cLFirst = theNasaBlackwellHIGHLIFTCalculator.get_cLEvaluated();

			theNasaBlackwellHIGHLIFTCalculator.calculate(alphaSecond);
			double cLSecond = theNasaBlackwellHIGHLIFTCalculator.get_cLEvaluated();


			double cLAlpha = (cLSecond - cLFirst)/(alphaSecond.getEstimatedValue()-alphaFirst.getEstimatedValue()); // 1/rad
			theInputOutputTree.setcLAlphaRadHL(cLAlpha);
			theInputOutputTree.setcLAlphaDegHL(Math.toRadians(cLAlpha));
			Amount<?> clAlpha = Amount.valueOf(theInputOutputTree.getcLAlphaDegHL(), NonSI.DEGREE_ANGLE.inverse());
			theInputOutputTree.setcLAlphaHL(clAlpha);

			Amount<?> wingclAlpha = Amount.valueOf( theInputOutputTree.getcLAlphaRadHL() , SI.RADIAN.inverse());

			// CL ZERO

			Amount<Angle> alphaZero = Amount.valueOf(0.0, SI.RADIAN);
			theNasaBlackwellHIGHLIFTCalculator.calculate(alphaZero);
			double cLZero = theNasaBlackwellHIGHLIFTCalculator.get_cLEvaluated();
			theInputOutputTree.setcLZeroHL(cLZero);

			//ALPHA ZERO LIFT

			double alphaZeroLift = -(theInputOutputTree.getcLZeroHL())/theInputOutputTree.getcLAlphaDegHL();
			theInputOutputTree.setAlphaZeroLiftHL(Amount.valueOf(alphaZeroLift, NonSI.DEGREE_ANGLE));


			//ALPHA STAR

			List<Amount<Area>> influenceAreas = new ArrayList<>();
			List<Double> kFactors = new ArrayList<>();

			// First value
			influenceAreas.add(0,
					Amount.valueOf(
							(theInputOutputTree.getChordDistribution().get(0).doubleValue(SI.METER) * 
									(theInputOutputTree.getyDimensionalDistributionInput().get(1).doubleValue(SI.METER) - 
											theInputOutputTree.getyDimensionalDistributionInput().get(0).doubleValue(SI.METER))/2),
							SI.SQUARE_METRE)
					);

			kFactors.add(0, 
					(2*influenceAreas.get(0).doubleValue(SI.SQUARE_METRE)/
							theInputOutputTree.getSurface().doubleValue(SI.SQUARE_METRE)));

			if(theInputOutputTree.getNumberOfSections() == 2 ){
				influenceAreas.add(1,
						Amount.valueOf(
								(theInputOutputTree.getChordDistribution().get(1).doubleValue(SI.METER) * 
										(theInputOutputTree.getyDimensionalDistributionInput().get(1).doubleValue(SI.METER) - 
												theInputOutputTree.getyDimensionalDistributionInput().get(0).doubleValue(SI.METER))/2),
								SI.SQUARE_METRE)
						);
				kFactors.add(1, 
						(2*influenceAreas.get(1).doubleValue(SI.SQUARE_METRE)/
								theInputOutputTree.getSurface().doubleValue(SI.SQUARE_METRE)));
			}

			if(theInputOutputTree.getNumberOfSections() >=3){
				for (int i=1; i<theInputOutputTree.getNumberOfSections()-1;i++){
					influenceAreas.add(i,
							Amount.valueOf(
									(theInputOutputTree.getChordDistribution().get(i).doubleValue(SI.METER) * 
											(theInputOutputTree.getyDimensionalDistributionInput().get(i).doubleValue(SI.METER) - 
													theInputOutputTree.getyDimensionalDistributionInput().get(i-1).doubleValue(SI.METER))/2) + 
									((theInputOutputTree.getChordDistribution().get(i).doubleValue(SI.METER) * 
											(theInputOutputTree.getyDimensionalDistributionInput().get(i+1).doubleValue(SI.METER) - 
													theInputOutputTree.getyDimensionalDistributionInput().get(i).doubleValue(SI.METER)))/2),
									SI.SQUARE_METRE)
							);

					kFactors.add(i, 
							(2*influenceAreas.get(i).doubleValue(SI.SQUARE_METRE)/
									theInputOutputTree.getSurface().doubleValue(SI.SQUARE_METRE)));

				}

				influenceAreas.add(theInputOutputTree.getNumberOfSections()-1,
						Amount.valueOf(
								(theInputOutputTree.getChordDistribution().get(theInputOutputTree.getNumberOfSections()-1).doubleValue(SI.METER) * 
										(theInputOutputTree.getyDimensionalDistributionInput().get(theInputOutputTree.getNumberOfSections()-1).doubleValue(SI.METER) - 
												theInputOutputTree.getyDimensionalDistributionInput().get(theInputOutputTree.getNumberOfSections()-2).doubleValue(SI.METER))/2),
								SI.SQUARE_METRE)
						);
				kFactors.add(theInputOutputTree.getNumberOfSections()-1, 
						(2*influenceAreas.get(theInputOutputTree.getNumberOfSections()-1).doubleValue(SI.SQUARE_METRE)/
								theInputOutputTree.getSurface().doubleValue(SI.SQUARE_METRE)));

			}


			Double alphaStarDeg = 0.0;
			Double meanThickness = 0.0;
			for (int i=0; i<kFactors.size(); i++){
				alphaStarDeg = alphaStarDeg + (kFactors.get(i)*theInputOutputTree.getAlphaStarDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE));
				meanThickness = meanThickness + (kFactors.get(i)*theInputOutputTree.getThicknessDistribution().get(i));
			}
			Amount<Angle> alphaStar =  Amount.valueOf(
					alphaStarDeg,
					NonSI.DEGREE_ANGLE);


			//CL STAR

			theNasaBlackwellHIGHLIFTCalculator.calculate(alphaStar);
			double cLStar = theNasaBlackwellHIGHLIFTCalculator.get_cLEvaluated();
			theInputOutputTree.setcLStarHL(cLStar);

			// CL MAX
			
			double cLMax = LiftCalc.calculateCLMaxHIGHLIFT(
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(theInputOutputTree.getMaximumliftCoefficientDistributionSemiSpanHighLift())),
					theInputOutputTree.getSemiSpan().doubleValue(SI.METER),
					theInputOutputTree.getSurface().doubleValue(SI.SQUARE_METRE),
					MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutputTree.getyDimensionalDistributionSemiSpan()),
					chordDistributionHighLiftFinal,
					xleDistributionHighLiftFinal, 
					dihedralxleDistributionHighLiftFinal, 
					twistDistributionHighLiftFinal,
					alphaZeroLiftDistributionHighLiftFinal, 
					vortexSemiSpanToSemiSpanRatio,
					0.0,
					theInputOutputTree.getMachNumber(),
					theInputOutputTree.getAltitude().doubleValue(SI.METER),
					chordDistributionMeter
					);
				
			theInputOutputTree.setClMaxHighLiftFromStallPath(cLMax);
			theInputOutputTree.setcLMaxHL(cLMax);

			// ALPHA MAX LINEAR
			theInputOutputTree.setAlphaMaxLinearHL(
					Amount.valueOf(
							(cLMax-cLZero)/theInputOutputTree.getcLAlphaDegHL(), 
							NonSI.DEGREE_ANGLE)
					);

			// ALPHA STALL

			// delta alpha	

			double sweepLEDeg = Math.toDegrees(
					Math.atan(
							theInputOutputTree.getxLEDistribution().get(theInputOutputTree.getNumberOfSections()-1).getEstimatedValue()/
							theInputOutputTree.getSemiSpan().getEstimatedValue()
							)
					);

			double deltaYPercent =  aeroDatabaseReader
					.getDeltaYvsThickness(
							theInputOutputTree.getMeanThickness(),
							theInputOutputTree.getMeanAirfoilFamily()
							);

			Amount<Angle> deltaAlpha = 
					Amount.valueOf(
							0.0,
//					aeroDatabaseReader
//					.getDAlphaVsLambdaLEVsDy(
//							sweepLEDeg,
//							deltaYPercent
//							),
					NonSI.DEGREE_ANGLE);

			theInputOutputTree.setAlphaStallHL(theInputOutputTree.getAlphaMaxLinearHL()
					.plus(deltaAlpha));
		
			// alpha star
			
			theInputOutputTree.setAlphaStarHL(Amount.valueOf(
					alphaStar.doubleValue(NonSI.DEGREE_ANGLE) - 
					(theInputOutputTree.getAlphaStall().doubleValue(NonSI.DEGREE_ANGLE)-
							theInputOutputTree.getAlphaStallHighLift().doubleValue(NonSI.DEGREE_ANGLE)
							), 
					NonSI.DEGREE_ANGLE));
			//------------------------------
			
			
			System.out.println("cl max flapped " + cLMax);
			
			theInputOutputTree.setLiftCoefficientCurveHL(new ArrayList<>());

			theInputOutputTree.setLiftCoefficientCurveHL(
					Main.convertDoubleArrayToListDouble(
							calculateCLvsAlphaArray(
									theInputOutputTree.getcLZeroHL(),
									theInputOutputTree.getcLMaxHL(),
									theInputOutputTree.getAlphaStarHL(),
									theInputOutputTree.getAlphaStallHL(),
									wingclAlpha,
									MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutputTree.getAlphaArrayHighLiftCurve())
									)));

			System.out.println("Alpha Array --> (deg)  " + Arrays.toString(MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutputTree.getAlphaArrayHighLiftCurve())));
			System.out.println("Cl curve " + theInputOutputTree.getLiftCoefficientCurveHL().toString());
			
			// STALL PATH
			
			theNasaBlackwellHIGHLIFTCalculator.calculate(theInputOutputTree.getAlphaMaxLinearHL());

			
			List<Double> clDistributionHighLift = Main.convertDoubleArrayToListDouble(
					Main.convertFromDoubleToPrimitive(
							theNasaBlackwellHIGHLIFTCalculator.get_ccLDistribution().toArray()));
			for(int j =0; j<clDistributionHighLift.size(); j++) {
				clDistributionHighLift.set(j, clDistributionHighLift.get(j)/theInputOutputTree.getChordDistributionSemiSpan().get(j).doubleValue(SI.METER));
			}
			
			theInputOutputTree.setClMaxStallPathHL(
									clDistributionHighLift);
			
			List<List<Double>> xList = new ArrayList<>();
			List<List<Double>> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			xList.add(theInputOutputTree.getyAdimensionalDistributionSemiSpan());
			xList.add(theInputOutputTree.getyAdimensionalDistributionSemiSpan());

			yList.add(theInputOutputTree.getMaximumliftCoefficientDistributionSemiSpanHighLift());
			yList.add(theInputOutputTree.getClMaxStallPathHL());

			legend.add("cl max airfoils");
			legend.add("cl distribution at max alpha linear = " +  theInputOutputTree.getAlphaMaxLinearHL().doubleValue(NonSI.DEGREE_ANGLE) + " deg");

		    
			Node chart = displayChartNode(
					"Lift Curve",
					"alpha",
					"CL", 
					150, 
					750,
					true,
					Side.BOTTOM,
					legend, 
					xList, 
					yList, 
					newOutputCharts.getTabs().get(5).getContent()
					);


			stallPathPane.getChildren().clear();
			stallPathPane.getChildren().add(chart);


			textOutputLift.appendText("\n\nSTALL PATH HIGHL LIFT");
			textOutputLift.appendText("\nEta Stations (deg) --> " + theInputOutputTree.getyAdimensionalDistributionSemiSpan().toString());
			textOutputLift.appendText("\nCl max airfoils (deg) --> " + theInputOutputTree.getMaximumliftCoefficientDistributionSemiSpanHighLift().toString());
			textOutputLift.appendText("\nCL max distribution --> " + theInputOutputTree.getClMaxStallPathHL().toString());
			textOutputLift.appendText("\n\n--------End of " + theController.getRunLift() + "st Run------------\n\n");
			
	}
	
	//----------------------------------------------------------------------------------------
	// NEW METHOD-----------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------
	public static void performHighLiftStallPathNewMethod(
			InputOutputTree theInputOutputTree, 
			VaraiblesAnalyses theController,
			Pane stallPathPane,
			TabPane newOutputCharts){
		// Setup database(s)
		AerodynamicDatabaseReader aeroDatabaseReader;
		
		// Setup database(s)

		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";

		aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		
		List<Amount<Angle>> alphaArrayForCalculation =
				MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(0, 25, 26),
				NonSI.DEGREE_ANGLE
				);

		// OUTPUT ARRAYS
		
		List<List<Double>> deltaClStationWithRespectToAlpha = new ArrayList<>();	
		List<List<Amount<Angle>>> effectiveAngleOfAttackDistribution = new ArrayList<>();
		
		Amount<Velocity> vTAS;
		
		vTAS = Amount.valueOf(
				theInputOutputTree.getMachNumber() * AtmosphereCalc.getSpeedOfSound(theInputOutputTree.getAltitude().doubleValue(SI.METER)), 
				SI.METERS_PER_SECOND
				);
		
		//NASA BLACKWELL CLEAN
		double vortexSemiSpanToSemiSpanRatio = (1./(2*theInputOutputTree.getNumberOfPointSemispan()));

		double [] twistDistributionRadians = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] alphaZeroLiftDistributionRadians = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] dihedralDistributionRadians = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] yDistributionMeter = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] chordDistributionMeter = new double[theInputOutputTree.getNumberOfPointSemispan()];
		double [] xleDistributionMeter = new double[theInputOutputTree.getNumberOfPointSemispan()];

		for (int i=0; i< theInputOutputTree.getNumberOfPointSemispan(); i++) {
			twistDistributionRadians[i] = theInputOutputTree.getTwistDistributionSemiSpan().get(i).doubleValue(SI.RADIAN);
			alphaZeroLiftDistributionRadians[i] = theInputOutputTree.getAlphaZeroLiftDistributionSemiSpan().get(i).doubleValue(SI.RADIAN);
			dihedralDistributionRadians [i] = theInputOutputTree.getDihedralDistributionSemiSpan().get(i).doubleValue(SI.RADIAN);
			yDistributionMeter [i] = theInputOutputTree.getyDimensionalDistributionSemiSpan().get(i).doubleValue(SI.METER);
			chordDistributionMeter [i] = theInputOutputTree.getChordDistributionSemiSpan().get(i).doubleValue(SI.METER);
			xleDistributionMeter [i] = theInputOutputTree.getxLEDistributionSemiSpan().get(i).doubleValue(SI.METER);
		}
		NasaBlackwell theNasaBlackwellCleanCalculator = new  NasaBlackwell(
				theInputOutputTree.getSemiSpan().doubleValue(SI.METER), 
				theInputOutputTree.getSurface().doubleValue(SI.SQUARE_METRE),
				yDistributionMeter,
				chordDistributionMeter,
				xleDistributionMeter,
				dihedralDistributionRadians,
				twistDistributionRadians,
				alphaZeroLiftDistributionRadians,
				vortexSemiSpanToSemiSpanRatio,
				0.0,
				theInputOutputTree.getMachNumber(),
				theInputOutputTree.getAltitude().doubleValue(SI.METER));
		
//-------------------------------------------------------
		Double[][] deltaClLocal = new Double [26][theInputOutputTree.getNumberOfPointSemispan()];
		List<Double> temporaryClList = new ArrayList<>();
	
		
		alphaArrayForCalculation.stream().forEach( a ->{
			int i = alphaArrayForCalculation.indexOf(a);
			
			List<Double> clDistributionHighLift = new ArrayList<>();
			List<Double> clDistributonClean = new ArrayList<>();
		
            theNasaBlackwellCleanCalculator.calculate(alphaArrayForCalculation.get(i));
			theNasaBlackwellHIGHLIFTCalculator.calculate(alphaArrayForCalculation.get(i));
			
			clDistributonClean = Main.convertDoubleArrayToListDouble(
					Main.convertFromDoubleToPrimitive(
							theNasaBlackwellCleanCalculator.getClTotalDistribution().toArray()));

			
			// chords old
			clDistributionHighLift = Main.convertDoubleArrayToListDouble(
					Main.convertFromDoubleToPrimitive(
							theNasaBlackwellHIGHLIFTCalculator.get_ccLDistribution().toArray()));
			for(int j =0; j<clDistributionHighLift.size(); j++) {
				clDistributionHighLift.set(j, clDistributionHighLift.get(j)/theInputOutputTree.getChordDistributionSemiSpan().get(j).doubleValue(SI.METER));
			}
			

			if (clDistributionHighLift.get(i).isNaN()){
				for (int ii=0; ii< clDistributionHighLift.size(); ii++){
					clDistributionHighLift.set(ii, 0.0);
				}
			}
			//---
			for(int ii=0; ii<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); ii++) {
				deltaClLocal[i][ii] =  
						clDistributionHighLift.get(ii)-clDistributonClean.get(ii);
			}
			
			//alpha effective
			effectiveAngleOfAttackDistribution.add(AlphaEffective.calculateAlphaEffective(
					theNasaBlackwellCleanCalculator,
					theInputOutputTree.getNumberOfPointSemispan(),
					a,
					vTAS, 
					theInputOutputTree.getTwistDistributionSemiSpan()
					));
			
		}
				);
			for(int i=0; i<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); i++) {
				for(int ii=0; ii<alphaArrayForCalculation.size(); ii++) {		
					temporaryClList.add(deltaClLocal[ii][i]);
				}
				deltaClStationWithRespectToAlpha.add(temporaryClList);
				temporaryClList = new ArrayList<>();
			}
			
	
			System.out.println(" eta station " + theInputOutputTree.getyAdimensionalDistributionSemiSpan().toString());
			for(int i=0; i<alphaArrayForCalculation.size(); i++) {
				System.out.print(" \n delta cl at alpha =" + alphaArrayForCalculation.get(i) + " = ");
				for(int ii=0; ii<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); ii++)
			System.out.print(" " + deltaClStationWithRespectToAlpha.get(ii).get(i));
			}
			
			for(int i=0; i<alphaArrayForCalculation.size(); i++) {
				System.out.print("\n alpha effective at alpha = " + alphaArrayForCalculation.get(i) + " = ");
				for(int ii=0; ii<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); ii++)
			System.out.print("  " + effectiveAngleOfAttackDistribution.get(i).get(ii).doubleValue(NonSI.DEGREE_ANGLE));
			}
			
	}
}
