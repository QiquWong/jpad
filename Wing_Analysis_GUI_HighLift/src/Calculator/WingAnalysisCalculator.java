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
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class WingAnalysisCalculator {
	static NasaBlackwell theNasaBlackwellCalculator;
	static TextArea textOutputLift;
	static List<Double>  _deltaCL0FlapList = new ArrayList<>();
	static double[] newChordDistributionWithFlapMeter;

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
		
		NasaBlackwell theNasaBlackwellCalculator = new  NasaBlackwell(
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
				
		List<Double> _deltaCl0FlapList =  
				(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl0_FLAP_LIST);
		
		_deltaCL0FlapList =  
				(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CL0_FLAP_LIST)
				;
		List<Double>  _deltaClmaxFlapList = 
				(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_FLAP_LIST);
		
		List<Double> _deltaCLmaxFlapList =  
				(List<Double>) highLiftDevicesEffectsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_FLAP_LIST);
				
		List<Double> _deltaClmaxSlatList =
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


		//stall path 
//		Tab stallPath = new Tab();
//		stallPath.setText("Stall Path");
//		Pane stallPathPane = new Pane();
//
//		stallPath.setContent(stallPathPane);

		newOutputCharts.getTabs().add(0, Lift);
//		newOutputCharts.getTabs().add(1, stallPath);


//		if(theController.getYesStallPath().isSelected()){
//			theController.getOutputPaneFinalHIGHLIFT().getChildren().clear();
//			
//			theController.getOutputPaneFinalHIGHLIFT().getChildren().add(newOutputCharts);
//
//			Node chart = displayChartNode(
//					"Lift Curve",
//					"alpha",
//					"CL", 
//					150, 
//					750,
//					false,
//					Side.RIGHT,
//					legend, 
//					xList, 
//					yList, 
//					newOutputCharts.getTabs().get(0).getContent()
//					);
//
//			LiftPane.getChildren().clear();
//			LiftPane.getChildren().add(chart);
//		}
//
//		if(theController.getNoStallPath().isSelected()){
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
//		}
		
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
//	
//	if(theController.getNoStallPath().isSelected()){
//	textOutputLift.appendText("\n\n--------End of " + theController.getRunLift() + "st Run------------\n\n");
//
//	}
	

	}
	
	public static void calculateHighLiftDistribution(
			InputOutputTree theInputOutpuTree,
			VaraiblesAnalyses theController) {
		
		//Y ARRAY ----------------------------------------

		List<Double> yAdimensionalArrayHighLiftModified = new ArrayList<>();

		Map<Double, Amount<Length>> mapAdimensionalStationChord = new HashMap<>(); // map chord
		Map<Double, Amount<Length>> mapAdimensionalStationXle = new HashMap<>(); // map xle
		Map<Double, Amount<Angle>> mapAdimensionalStationAlphaZeroLift = new HashMap<>(); // map aol

		// Setup database(s)
		HighLiftDatabaseReader highLiftDatabaseReader;
		// Setup database(s)

		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String highLiftDatabaseFileName = "HighLiftDatabase.h5";

		highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);


		//fill y array with input values----------------------------------------------
		theInputOutpuTree.getyAdimensionalStationInput().stream().forEach(yAd ->{
			int i = theInputOutpuTree.getyAdimensionalStationInput().indexOf(yAd);
			yAdimensionalArrayHighLiftModified.add(yAd);
			mapAdimensionalStationChord.put(yAd, theInputOutpuTree.getChordDistribution().get(i));
			mapAdimensionalStationXle.put(yAd, theInputOutpuTree.getxLEDistribution().get(i));
			mapAdimensionalStationAlphaZeroLift.put(yAd, theInputOutpuTree.getAlphaZeroLiftDistribution().get(i));
		}	
				);

		// fill y array with flap values----------------------------------------------
		theInputOutpuTree.getFlapInnerStation().stream().forEach(yFlapAd ->{
			int i = theInputOutpuTree.getFlapInnerStation().indexOf(yFlapAd);

			if(yAdimensionalArrayHighLiftModified.contains(yFlapAd))
				yFlapAd=yFlapAd - 0.001;

			// calc delta flap
			Double deltaCCfFlap = 0.0;
			Double cFirstC = 0.0;
			Double newClAlpha = 0.0;

			deltaCCfFlap = 
					highLiftDatabaseReader
					.getDeltaCCfVsDeltaFlap(
							theInputOutpuTree.getFlapDeflection().get(i).doubleValue(NonSI.DEGREE_ANGLE),
							theInputOutpuTree.getFlapTypeIndex().get(i)
							);

			cFirstC = 
					(1+(deltaCCfFlap*theInputOutpuTree.getFlapChordRatio().get(i)));

			newClAlpha = (MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
					MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getClAlphaDegDistributionSemiSpan()),
					yFlapAd))* (
							cFirstC * (
									1-theInputOutpuTree.getFlapChordRatio().get(i) * Math.pow(
											Math.sin(theInputOutpuTree.getFlapDeflection().get(i).doubleValue(SI.RADIAN)),
											2
											)));


			yAdimensionalArrayHighLiftModified.add(yFlapAd);
			mapAdimensionalStationChord.put(yFlapAd, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
									yFlapAd) + 
							(deltaCCfFlap*theInputOutpuTree.getFlapChordRatio().get(i)*
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
											MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
											yFlapAd)
									),
							SI.METER));

			mapAdimensionalStationXle.put(yFlapAd, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getxLEDistributionSemiSpan())),
									yFlapAd),
							SI.METER)
					);

			mapAdimensionalStationAlphaZeroLift.put(yFlapAd, 
					Amount.valueOf(
							-((MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getClZeroDistributionSemispan()),
									yFlapAd) + _deltaCL0FlapList.get(i))/
									newClAlpha),
							NonSI.DEGREE_ANGLE)
					);
		}	
				);

		// fill y array with flap outer values----------------------------------------------
		theInputOutpuTree.getFlapOuterStation().stream().forEach(yFlapAd ->{
			int i = theInputOutpuTree.getFlapOuterStation().indexOf(yFlapAd);

			if(yAdimensionalArrayHighLiftModified.contains(yFlapAd))
				yFlapAd=yFlapAd+0.001;

			// calc delta flap
			Double deltaCCfFlap = 0.0;
			Double cFirstC = 0.0;
			Double newClAlpha = 0.0;

			deltaCCfFlap = 
					highLiftDatabaseReader
					.getDeltaCCfVsDeltaFlap(
							theInputOutpuTree.getFlapDeflection().get(i).doubleValue(NonSI.DEGREE_ANGLE),
							theInputOutpuTree.getFlapTypeIndex().get(i)
							);

			cFirstC = 
					(1+(deltaCCfFlap*theInputOutpuTree.getFlapChordRatio().get(i)));

			newClAlpha = (MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
					MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getClAlphaDegDistributionSemiSpan()),
					yFlapAd))* (
							cFirstC * (
									1-theInputOutpuTree.getFlapChordRatio().get(i) * Math.pow(
											Math.sin(theInputOutpuTree.getFlapDeflection().get(i).doubleValue(SI.RADIAN)),
											2
											)));


			yAdimensionalArrayHighLiftModified.add(yFlapAd);
			mapAdimensionalStationChord.put(yFlapAd, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
									yFlapAd) + 
							(deltaCCfFlap*theInputOutpuTree.getFlapChordRatio().get(i)*
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
											MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
											yFlapAd)
									),
							SI.METER));

			mapAdimensionalStationXle.put(yFlapAd, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getxLEDistributionSemiSpan())),
									yFlapAd),
							SI.METER)
					);

			mapAdimensionalStationAlphaZeroLift.put(yFlapAd, 
					Amount.valueOf(
							-((MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getClZeroDistributionSemispan()),
									yFlapAd) + _deltaCL0FlapList.get(i))/
									newClAlpha),
							NonSI.DEGREE_ANGLE)
					);
		}	
				);



		// create new chord array----------------------------------------------

		newChordDistributionWithFlapMeter = new double[yAdimensionalArrayHighLiftModified.size()];
		Arrays.sort(MyArrayUtils.convertToDoublePrimitive(
				MyArrayUtils.convertListOfDoubleToDoubleArray(yAdimensionalArrayHighLiftModified)));
		yAdimensionalArrayHighLiftModified.stream().forEach(yADnew ->{
			int i = yAdimensionalArrayHighLiftModified.indexOf(yADnew);
			newChordDistributionWithFlapMeter [i] = mapAdimensionalStationChord.get(yADnew).doubleValue(SI.METER);
		});


		// fill y array with slat inner values----------------------------------------------
		theInputOutpuTree.getSlatInnerStation().stream().forEach(ySlatAd ->{
			int i = theInputOutpuTree.getSlatInnerStation().indexOf(ySlatAd);

			if(yAdimensionalArrayHighLiftModified.contains(ySlatAd))
				ySlatAd=ySlatAd-0.001;

			yAdimensionalArrayHighLiftModified.add(ySlatAd);
			
			mapAdimensionalStationChord.put(ySlatAd, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									newChordDistributionWithFlapMeter,
									ySlatAd) + 
							(theInputOutpuTree.getSlatExtensionRatio().get(i)*
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
											MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
											ySlatAd)
									),
							SI.METER));

			mapAdimensionalStationXle.put(ySlatAd,
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getxLEDistributionSemiSpan())),
									ySlatAd) - 
							(theInputOutpuTree.getSlatExtensionRatio().get(i)*
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
											MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getChordDistributionSemiSpan())),
											ySlatAd)
									)
							,
							SI.METER));

			mapAdimensionalStationAlphaZeroLift.put(
					ySlatAd, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
									MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getAlphaZeroLiftDistributionSemiSpan())),
									ySlatAd),
							NonSI.DEGREE_ANGLE));

		}
				);
			
// CREATE SORTED ARRAY FOR Y STATIONS, CHORD, ALPHA ZERO LIFT AND XLE
	
		double[] chordDistributionHighLift = new double [yAdimensionalArrayHighLiftModified.size()];
		double[] alphaZeroLiftDistributionHighLift = new double [yAdimensionalArrayHighLiftModified.size()];
		double[] xleDistributionHighLift = new double [yAdimensionalArrayHighLiftModified.size()];
		double[] dihedralxleDistributionHighLift = new double [yAdimensionalArrayHighLiftModified.size()];
		double[] twistDistributionHighLift = new double [yAdimensionalArrayHighLiftModified.size()];
		double[] yMeterDimensionalDistributionHighLift = new double [yAdimensionalArrayHighLiftModified.size()];
		
		Arrays.sort(MyArrayUtils.convertToDoublePrimitive(
				MyArrayUtils.convertListOfDoubleToDoubleArray(yAdimensionalArrayHighLiftModified)));
		
		yAdimensionalArrayHighLiftModified.stream().forEach(yADnew ->{
			int i = yAdimensionalArrayHighLiftModified.indexOf(yADnew);
			chordDistributionHighLift [i] = mapAdimensionalStationChord.get(yADnew).doubleValue(SI.METER);
			xleDistributionHighLift [i] = mapAdimensionalStationXle.get(yADnew).doubleValue(SI.METER);
			alphaZeroLiftDistributionHighLift[i] = mapAdimensionalStationAlphaZeroLift.get(yADnew).doubleValue(SI.RADIAN);
			dihedralxleDistributionHighLift[i] = 0.0;
			twistDistributionHighLift[i] =
					Math.toRadians(MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(theInputOutpuTree.getyAdimensionalDistributionSemiSpan()),
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(theInputOutpuTree.getTwistDistributionSemiSpan())),
							yADnew));
			yMeterDimensionalDistributionHighLift[i]=yADnew*theInputOutpuTree.getSemiSpan().doubleValue(SI.METER);
			
		});
	
		double[] chordDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		double[] alphaZeroLiftDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		double[] xleDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		double[] dihedralxleDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		double[] twistDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		double[] yMeterDimensionalDistributionHighLiftFinal = new double [theInputOutpuTree.getyAdimensionalDistributionSemiSpan().size()];
		
		theInputOutpuTree.getyAdimensionalDistributionSemiSpan().stream().forEach(yADold ->{	
			int i = theInputOutpuTree.getyAdimensionalDistributionSemiSpan().indexOf(yADold);
			
			chordDistributionHighLiftFinal[i] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified), 
					chordDistributionHighLift,
					yADold);
			
			alphaZeroLiftDistributionHighLiftFinal[i] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified), 
					alphaZeroLiftDistributionHighLift,
					yADold);
			
			xleDistributionHighLiftFinal[i] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified), 
					xleDistributionHighLift,
					yADold);
			
			dihedralxleDistributionHighLiftFinal[i] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified), 
					dihedralxleDistributionHighLift,
					yADold);
			
			twistDistributionHighLiftFinal[i] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalArrayHighLiftModified), 
					twistDistributionHighLift,
					yADold);
			
			
		});
			
		
// creating new array for dihedral and twist with new y stations.		
		double vortexSemiSpanToSemiSpanRatio = (1./(2*theInputOutpuTree.getNumberOfPointSemispan()));
		
		NasaBlackwell theNasaBlackwellCalculator = new  NasaBlackwell(
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
		
		
// calculating curves
		
		for (int i=0; i<theInputOutpuTree.getAlphaArrayHighLiftDistribution().size(); i++){

			theNasaBlackwellCalculator.calculate(theInputOutpuTree.getAlphaArrayHighLiftDistribution().get(i));
			List<Double> clList = new ArrayList<>();
			clList = Main.convertDoubleArrayToListDouble(
					Main.convertFromDoubleToPrimitive(
							theNasaBlackwellCalculator.getClTotalDistribution().toArray()));
			if (clList.get(i).isNaN()){
				for (int ii=0; ii< clList.size(); ii++){
					clList.set(ii, 0.0);
				}
			}
			
			theInputOutpuTree.getClDistributionCurvesHighLift().add(i, 
					clList);
			
			System.out.println(" alpha = " + theInputOutpuTree.getAlphaArrayHighLiftDistribution().get(i) + " cl " + clList);
			}
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

}
