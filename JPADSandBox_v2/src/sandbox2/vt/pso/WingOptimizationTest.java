package sandbox2.vt.pso;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LiftingSurface.LiftingSurfaceBuilder;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import calculators.aerodynamics.LiftCalc;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.WingAdjustCriteriaEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import optimization.ParticleSwarmOptimizer;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import writers.JPADStaticWriteUtils;

class MyArgumentWingOptimizationTest {
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

public class WingOptimizationTest {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	public static List<LiftingSurface> theWings = new ArrayList<>();

	/* Main
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO: check out this as an alternative
		// https://blog.codecentric.de/en/2015/09/javafx-how-to-easily-implement-application-preloader-2/

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

			String dirAirfoil = va.getAirfoilDirectory().getCanonicalPath();
			System.out.println("\tAIRFOILS ===> " + dirAirfoil);

			System.out.println("\t--------------");

			// This wing static object is available in the scope of
			// the Application.start method
			
			//------------------------------------------------------------------------------------
			// Setup database(s)
			System.out.println("\t------------------------------------");
			System.out.println("\tSetting up databases and folders ...  ");
			System.out.println("\t------------------------------------\n");
			
			MyConfiguration.initWorkingDirectoryTree();
			
			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
			String vedscDatabaseFilename = "VeDSC_database.h5";
			AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
			HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
			VeDSCDatabaseReader veDSCDatabaseReader = new VeDSCDatabaseReader(databaseFolderPath, vedscDatabaseFilename);
			String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
			String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "Wing_Optimization_Test" + File.separator);
			
			// read LiftingSurface from xml  (WING 0 = BASELINE) ...
			System.setOut(filterStream);
			LiftingSurface wing0 = new LiftingSurfaceBuilder(
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
			theWings.add(wing0);
			wing0.calculateGeometry(
					40,
					wing0.getType(),
					wing0.getLiftingSurfaceCreator().isMirrored()
					);
			wing0.populateAirfoilList(
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
			
			Double mach = 0.4;
			Amount<Length> altitude = Amount.valueOf(6000, SI.METER);
			int numberOfPointsSemiSpanwise = 50;
			
			double[] var1Array = MyArrayUtils.linspace(
					wing0.getLiftingSurfaceCreator().getAspectRatio()*0.7,
					wing0.getLiftingSurfaceCreator().getAspectRatio()*1.3,
					numberOfVar1
					);
			double[] var2Array = MyArrayUtils.linspace(
					wing0.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio()*0.7,
					1.0,
					numberOfVar2
					);
			double[] var3Array = MyArrayUtils.linspace(
					0.0,
					wing0.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().doubleValue(NonSI.DEGREE_ANGLE)*1.3,
					numberOfVar3
					);
			
			Double[] designVariablesLowerBound = new Double[] {var1Array[0], var2Array[0], var3Array[0]};
			Double[] designVariablesUpperBound = new Double[] {var1Array[var1Array.length-1], var2Array[var2Array.length-1], var3Array[var3Array.length-1]};
			Double convergenceThreshold = 1e-3; // threshold used to compare particles position during each iteration
			int particlesNumber = 10;
			Double kappa = 1.0;
			Double phi1 = 2.05;
			Double phi2 = 2.05;
			
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
						
						currentWing.getLiftingSurfaceCreator().adjustDimensions(
								var1Array[i],
								wing0.getLiftingSurfaceCreator().getSpan().doubleValue(SI.METER),
								var2Array[k],
								Amount.valueOf(var3Array[k], NonSI.DEGREE_ANGLE),
								wing0.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getDihedral(),
								wing0.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(), 
								wingAdjustCriterion
								);
						
						theWings.add(currentWing);
						
						currentWing.calculateGeometry(
								40,
								currentWing.getType(),
								currentWing.getLiftingSurfaceCreator().isMirrored());
						currentWing.populateAirfoilList(
								aeroDatabaseReader,
								Boolean.FALSE
								);
						
						// sign (-) for a minimization problem
						costFunctionValues[i][j][k] = -WingOptimizationTest.calculateCLmaxWing(
								currentWing,
								numberOfPointsSemiSpanwise, 
								mach,
								altitude.doubleValue(SI.METER)
								);
							
					}
				}
			}
			System.setOut(originalOut);
			
			System.out.println("\t------------------------------------");
			System.out.println("\tEvaluating response surface ... ");
			System.out.println("\t------------------------------------\n");
			MyInterpolatingFunction costFunction = new MyInterpolatingFunction();
			costFunction.interpolateTrilinear(
					var1Array,
					var2Array,
					var3Array,
					costFunctionValues
					);
			
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
					null
					);
			
			pso.optimize();
			
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
			return;
		}	    

	}

	public static Double calculateCLmaxWing (
			LiftingSurface w,
			int numberOfPointsSemiSpanwise,
			double mach,
			double altitude
			) {
		
		List<Amount<Length>> _yStationDistribution = new ArrayList<Amount<Length>>();
		List<Amount<Angle>> _alphaZeroLiftDistribution = new ArrayList<Amount<Angle>>();
		List<Amount<Angle>> _twistDistribution = new ArrayList<Amount<Angle>>();
		List<Amount<Length>> _chordDistribution = new ArrayList<Amount<Length>>();
		List<Amount<Angle>> _dihedralDistribution = new ArrayList<Amount<Angle>>();
		List<Amount<Length>> _xLEDistribution = new ArrayList<Amount<Length>>();
		List<Double> _clMaxDistribution = new ArrayList<Double>();
		
		//----------------------------------------------------------------------------------------------------------------------
		// Calculating airfoil parameter distributions
		//......................................................................................................................
		// ETA STATIONS AND Y STATIONS
		double[] _yStationDistributionArray = MyArrayUtils.linspace(
				0,
				w.getSemiSpan().doubleValue(SI.METER),
				numberOfPointsSemiSpanwise
				);
		for(int i=0; i<_yStationDistributionArray.length; i++)
			_yStationDistribution.add(Amount.valueOf(_yStationDistributionArray[i], SI.METER));
		//......................................................................................................................
		// ALPHA ZERO LIFT
		Double[] _alphaZeroLiftDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_alphaZeroLiftDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getAlpha0VsY()),
				_yStationDistributionArray
				);
		for(int i=0; i<_alphaZeroLiftDistributionArray.length; i++)
			_alphaZeroLiftDistribution.add(Amount.valueOf(_alphaZeroLiftDistributionArray[i], NonSI.DEGREE_ANGLE));
		//......................................................................................................................
		// TWIST 
		Double[] _twistDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_twistDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getTwistsBreakPoints()),
				_yStationDistributionArray
				);
		for(int i=0; i<_twistDistributionArray.length; i++)
			_twistDistribution.add(Amount.valueOf(_twistDistributionArray[i], NonSI.DEGREE_ANGLE));
		//......................................................................................................................
		// CHORDS
		Double[] _chordDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_chordDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getChordsBreakPoints()),
				_yStationDistributionArray
				);
		for(int i=0; i<_chordDistributionArray.length; i++)
			_chordDistribution.add(Amount.valueOf(_chordDistributionArray[i], SI.METER));
		//......................................................................................................................
		// DIHEDRAL
		Double[] _dihedralDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_dihedralDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getDihedralsBreakPoints()),
				_yStationDistributionArray
				);
		for(int i=0; i<_dihedralDistributionArray.length; i++)
			_dihedralDistribution.add(Amount.valueOf(_dihedralDistributionArray[i], NonSI.DEGREE_ANGLE));
		//......................................................................................................................
		// XLE DISTRIBUTION
		Double[] _xLEDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_xLEDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getXLEBreakPoints()),
				_yStationDistributionArray
				);
		for(int i=0; i<_xLEDistributionArray.length; i++)
			_xLEDistribution.add(Amount.valueOf(_xLEDistributionArray[i], SI.METER));
		//......................................................................................................................
		// Clmax DISTRIBUTION
		Double[] _clMaxDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_clMaxDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertToDoublePrimitive(w.getClMaxVsY()),
				_yStationDistributionArray
				);
		for(int i=0; i<_clMaxDistributionArray.length; i++)
			_clMaxDistribution.add(_clMaxDistributionArray[i]);
		
		return LiftCalc.calculateCLMax(
				MyArrayUtils.convertToDoublePrimitive(_clMaxDistribution),
				w.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER),
				w.getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(_yStationDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_chordDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_xLEDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_dihedralDistribution.stream().map(x -> x.to(SI.RADIAN)).collect(Collectors.toList())),
				MyArrayUtils.convertListOfAmountTodoubleArray(_twistDistribution.stream().map(x -> x.to(SI.RADIAN)).collect(Collectors.toList())),
				MyArrayUtils.convertListOfAmountTodoubleArray(_alphaZeroLiftDistribution.stream().map(x -> x.to(SI.RADIAN)).collect(Collectors.toList())), 
				(1./(2*numberOfPointsSemiSpanwise)), 
				0.0,
				mach,
				altitude
				);
		
	}
	
}