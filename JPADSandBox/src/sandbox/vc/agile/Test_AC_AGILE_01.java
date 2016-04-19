package sandbox.vc.agile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.google.common.io.Files;

import aircraft.OperatingConditions;
import aircraft.components.Aircraft;
import calculators.performance.PayloadRangeCalc;
import calculators.performance.SpecificRangeCalc;
import calculators.performance.ThrustCalc;
import calculators.performance.customdata.ThrustMap;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.cNbetaContributionsEnum;
import sandbox.vc.dirstab.DirStabCalc;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class Test_AC_AGILE_01 {
	
	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;
	
	@Option(name = "-d", aliases = { "--database-path" }, required = true,
			usage = "path for database files")
	private File _databasePath;

	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	public Test_AC_AGILE_01(){
		theCmdLineParser = new CmdLineParser(this);
	}
	
	public static void main(String[] args) throws CmdLineException {
		
		// Allocate the main object
		Test_AC_AGILE_01 theTestObject = new Test_AC_AGILE_01();
		
		String folderPathName = MyConfiguration.outputDirectory + File.separator + 
				"Test_AC_AGILE_VC_sandbox" + File.separator ;
		String fileName = "thrustVSspeed";
		theTestObject.theCmdLineParser.parseArgument(args);
		
		String fileNameWithPathAndExt = theTestObject.get_inputFile().getAbsolutePath();
		String databaseDirectoryAbsolutePath = theTestObject.get_databasePath().getAbsolutePath();
		
		// Set database name	
		String veDSCDatabaseFileName = "VeDSC_database.h5";
		String fusDesDatabaseFileName = "FusDes_database.h5";
		databaseDirectoryAbsolutePath = theTestObject.get_databasePath().getAbsolutePath();
		String outputFileNameWithPathAndExt = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) + File.separator + 
				Files.getNameWithoutExtension(fileNameWithPathAndExt) + "_Out.xml";
		//------------------------------------------------------------------------------------
		// Initialize Aircraft with default parameters
		//------------------------------------------------------------------------------------
		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.ATR72); 
				
		OperatingConditions operatingConditions = new OperatingConditions();
		operatingConditions.set_altitude(Amount.valueOf(0.000, SI.METER));
		operatingConditions.set_machCurrent(0.78);
		// Mass - Weight
		Amount<Mass> MTOM = Amount.valueOf(25330, NonSI.POUND); // 
		aircraft.get_weights().set_MTOM(MTOM);
		Amount<Force> _MLW = MTOM.times(0.9).times(AtmosphereCalc.g0).to(SI.NEWTON);
		aircraft.get_weights().set_MLW(_MLW);

//		// Geometry		
		Amount<Area> _surface = Amount.valueOf(76.33,SI.SQUARE_METRE);
		aircraft.get_exposedWing().set_surface(_surface);
		Amount<Length> _span = Amount.valueOf(27.6, SI.METER);
		aircraft.get_exposedWing().set_span(_span);
		//------------------------------------------------------------------------------------
		
		//------------------------------------------------------------------
		// Engine Moment calculation
		//------------------------------------------------------------------
		Amount<Force> _T0Total = Amount.valueOf(13217.65, NonSI.POUND_FORCE);
		aircraft.get_powerPlant().set_T0Total(_T0Total);
		
		double le = 5; // engine arm [m]
		double byPassRatio = 8;
		double engineNumber = 1;
		double[] speed = MyArrayUtils.linspace(
					SpeedCalc.calculateTAS(
							0.05,
							operatingConditions.get_altitude().getEstimatedValue()
							),
					SpeedCalc.calculateTAS(
							1.0,
							operatingConditions.get_altitude().getEstimatedValue()
							),
					250
					);
		double[] machVector = MyArrayUtils.linspace(0.05,1,250);
					
		double[] thrust = ThrustCalc.calculateThrustVsSpeed(
									_T0Total.to(SI.NEWTON).getEstimatedValue(),
									1, // phi
									operatingConditions.get_altitude().getEstimatedValue(),
									EngineOperatingConditionEnum.TAKE_OFF,
									EngineTypeEnum.TURBOFAN,
									byPassRatio,
									engineNumber,
									speed
									);
		
		double[] thrustMoment = new double[thrust.length]; 
		for(int i=0; i < thrust.length; i++){
			thrustMoment[i] = thrust[i]*le;
		}
		
//		System.out.println("\n T0: " + _T0Total.getEstimatedValue()+ " " + _T0Total.getUnit());
//		System.out.println("\n h: " + operatingConditions.get_altitude().getEstimatedValue());
//		System.out.println("\n BPR: " + byPassRatio);
//		System.out.println("\n nun engine: " + engineNumber);
		
		//------------------------------------------------------------------
		// Aerodynamic Moment calculation
		//------------------------------------------------------------------
		System.out.println("\n engine arm: " + le);
		
		HashMap<cNbetaContributionsEnum, Double> cNbMap = 
				DirStabCalc.executeStandaloneDirStabMap(veDSCDatabaseFileName,  
				fusDesDatabaseFileName,
				databaseDirectoryAbsolutePath,
				fileNameWithPathAndExt, 
				outputFileNameWithPathAndExt);
		
		double tau = 0.5;
		double dr  = 30;
		double _density = operatingConditions.get_densityCurrent().getEstimatedValue();
		double[] yawingMoment = new double[thrustMoment.length];
		
		for(int i=0; i < thrust.length; i++){
		yawingMoment[i] = cNbMap.get(cNbetaContributionsEnum.cNbVertical).doubleValue()*
				tau*
				dr*
//				operatingConditions.get_dynamicPressure().getEstimatedValue()*
				0.5*
				_density*
				Math.pow(speed[i],2)*
				_surface.getEstimatedValue()*
				_span.getEstimatedValue();
		}
		
		//-------------------------------------------------
		// PLOTTING:
		double[][] thrustPlotVector = new double [2][speed.length];
//		System.out.println("speed: " + speed.length);
		
		for(int i=0; i < speed.length; i++){
		thrustPlotVector[0][i] = thrustMoment[i];
		thrustPlotVector[1][i] = yawingMoment[i];
		}
		String[] legendValue = new String[2];
		legendValue[0] = "Thrust Moment";
		legendValue[1] = "Yawning Moment";
		
//		System.out.println("thrustPlotVector length: " + thrustPlotVector.length);
			MyChartToFileUtils.plotNoLegend(speed,thrust,
					null,null,null,null,
					"TAS", "Thrust",
					"m/s", "N",
					folderPathName, fileName);
			
			MyChartToFileUtils.plotNoLegend(speed,thrustMoment,
					null,null,null,null,
					"TAS", "Thrust Moment",
					"m/s", "N m",
					folderPathName, "Engine moment");
			
			MyChartToFileUtils.plot(machVector,thrustPlotVector,
//					null,100.0,null,yawingMoment[yawingMoment.length/3] ,	// axis with limits
					null,0.5,null,yawingMoment[yawingMoment.length/3] ,
//					"TAS", "Thrust - Yawing Moment",	
					"M", "Thrust - Yawing Moment",
//					"m/s", "N m",legendValue,
					"", "N m",legendValue,
					folderPathName, "VMC");

	}
	
	public File get_inputFile() {
		return _inputFile;
	}

	public File get_databasePath() {
		return _databasePath;
	}

}
