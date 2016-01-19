package sandbox.vt.SpecificRange_Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.geometry.LSGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.GeometryCalc;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class SpecificRange_Test_TF_SA {
	
	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION: 
	@Option(name = "-i", aliases = { "--input" }, required = false,
			usage = "my input file")
	private File _inputFile;
	
	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;
	
	@Argument
    private List<String> arguments = new ArrayList<String>();
	
	//------------------------------------------------------------------------------------------
	//BUILDER:
	public SpecificRange_Test_TF_SA() {
		theCmdLineParser = new CmdLineParser(this);
	}

	//------------------------------------------------------------------------------------------
	// MAIN:
	public static void main(String[] args) throws CmdLineException, InstantiationException, IllegalAccessException {
		//--------------------------------------------------------------------------------------
		// Arguments check and initial activities
		if (args.length == 0){
			System.err.println("NO INPUT FILE GIVEN --> TERMINATING");
			return;
		}
		
		System.out.println("-----------------------------------------------------------");
		System.out.println("SpecificRangeCalc_Test :: TURBOFAN");
		System.out.println("-----------------------------------------------------------\n");
		
		SpecificRange_Test_TF_SA main = new SpecificRange_Test_TF_SA();
		System.out.println("Input variable usage:");
		main.theCmdLineParser.printUsage(System.out);
		System.out.println("-----------------------------------------------------------");
		main.theCmdLineParser.parseArgument(args); 
		
		//----------------------------------------------------------------------------------
		// Default folders creation:
		MyConfiguration.initWorkingDirectoryTree();

		//----------------------------------------------------------------------------------
		// Variables:
		String path = main.get_inputFile().getAbsolutePath();
		JPADXmlReader reader = new JPADXmlReader(path);
		
		System.out.println("-----------------------------------------------------------");
		System.out.println("XML File Path : " + path);
		System.out.println("-----------------------------------------------------------");
		System.out.println("Initialize reading \n");
		
		List<String> maxTakeOffMass_property = reader.getXMLPropertiesByPath("//B747/Maximum_take_off_mass");	
		double maxTakeOffMass = Double.valueOf(maxTakeOffMass_property.get(0));
		List<String> altitude_property = reader.getXMLPropertiesByPath("//B747/Altitude");	
		double altitude = Double.valueOf(altitude_property.get(0));
		List<String> surface_property = reader.getXMLPropertiesByPath("//B747/Planform_surface");
		double surface = Double.valueOf(surface_property.get(0));
		List<String> cLmax_property = reader.getXMLPropertiesByPath("//B747/CLmax");
		double cLmax = Double.valueOf(cLmax_property.get(0));
		List<String> byPassRatio_property = reader.getXMLPropertiesByPath("//B747/ByPassRatio");
		double byPassRatio = Double.valueOf(byPassRatio_property.get(0));
		List<String> eta_property = reader.getXMLPropertiesByPath("//B747/Propeller_efficiency");
		double eta = Double.valueOf(eta_property.get(0));
		List<String> tcMax_property = reader.getXMLPropertiesByPath("//B747/Mean_maximum_thickness");
		double tcMax = Double.valueOf(tcMax_property.get(0));
		List<String> ar_property = reader.getXMLPropertiesByPath("//B747/AspectRatio");
		double ar = Double.valueOf(ar_property.get(0));	
		List<String> cd0_property = reader.getXMLPropertiesByPath("//B747/CD0");
		double cd0 = Double.valueOf(cd0_property.get(0));
		List<String> t0_property = reader.getXMLPropertiesByPath("//B747/Total_Thrust_Single_Engine");
		double t0 = Double.valueOf(t0_property.get(0));
		List<String> engineNumber_property = reader.getXMLPropertiesByPath("//B747/Engine_number");
		double engineNumber = Double.valueOf(engineNumber_property.get(0));
		List<String> taperRatioEquivalent_property = reader.getXMLPropertiesByPath("//B747/TaperRatio");
		double taperRatioEquivalent = Double.valueOf(taperRatioEquivalent_property.get(0));
		Amount<Angle> sweepLE = (Amount<Angle>)reader.getXMLAmountWithUnitByPath("//B747/SweepLE");
		Amount<Angle> sweepHalfChord = LSGeometryCalc.calculateSweep(
				ar,
				taperRatioEquivalent,
				sweepLE.to(SI.RADIAN).getEstimatedValue(),
				0.5,
				0.0
				);

		// generating variation of mass of 10% until -40% of maxTakeOffMass
		double[] maxTakeOffMassArray = new double[5];
		for (int i=0; i<5; i++)
			maxTakeOffMassArray[i] = maxTakeOffMass*(1-0.1*(4-i));

		Double[] vStall = new Double[maxTakeOffMassArray.length];
		for(int i=0; i<vStall.length; i++)
			vStall[i] = SpeedCalc.calculateSpeedStall(
					altitude,
					Amount.valueOf(maxTakeOffMassArray[i], SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					surface,
					cLmax);  

		//----------------------------------------------------------------------------------
		// Definition of a Mach array for each maxTakeOffMass
		List<Double[]> machList = new ArrayList<Double[]>();

		for(int i=0; i<maxTakeOffMassArray.length; i++) 
			machList.add(MyArrayUtils.linspaceDouble(
					SpeedCalc.calculateMach(
							altitude,
							vStall[i]),
					1.0,
					250));

		// Drag Thrust Intersection
		double oswald = AerodynamicCalc.calculateOswaldRaymer(
				sweepLE.to(SI.RADIAN).getEstimatedValue(),
				ar
				);

		double[][] intersection = SpecificRangeCalc.ThrustDragIntersection(
				machList,
				t0,
				engineNumber,
				0.9, // phi
				byPassRatio,
				EngineTypeEnum.TURBOFAN,
				EngineOperatingConditionEnum.CRUISE,
				altitude,
				maxTakeOffMassArray,
				surface,
				cd0,
				oswald,
				ar,
				sweepHalfChord.getEstimatedValue(),
				tcMax,
				AirfoilTypeEnum.MODERN_SUPERCRITICAL
				);
		System.out.println("Intersection Matrix\n");
		for(int i=0; i<intersection.length; i++) {
			for(int j=0; j<intersection[i].length; j++) {
				System.out.print(intersection[i][j] + " ");
			}
			System.out.println(" ");
		}

		System.out.println("-----------------------------------------------------------");

		System.out.println("\n-----------------------------------------------------------");
		System.out.println("Mach Matrix\n");
		for (int i=0; i<machList.size(); i++)
			System.out.println(Arrays.toString(machList.get(i)));

		System.out.println("-----------------------------------------------------------");

		//-----------------------------------------------------------------------------------
		// Calculation of the SFC for each Mach array
		List<Double[]> sfcList = new ArrayList<Double[]>();
		for(int i=0; i<maxTakeOffMassArray.length; i++)
			sfcList.add(SpecificRangeCalc.calculateSfcVsMach(
					machList.get(i),
					altitude,
					byPassRatio,
					EngineTypeEnum.TURBOFAN
					));

		System.out.println("SFC Matrix\n");
		for (int i=0; i<sfcList.size(); i++)
			System.out.println(Arrays.toString(sfcList.get(i)));

		System.out.println("-----------------------------------------------------------");

		//----------------------------------------------------------------------------------
		// Calculation of the Efficiency for each Mach array
		List<Double[]> efficiencyList = new ArrayList<Double[]>();
		for(int i=0; i<maxTakeOffMassArray.length; i++)
			efficiencyList.add(SpecificRangeCalc.calculateEfficiencyVsMach(
					Amount.valueOf(maxTakeOffMassArray[i],SI.KILOGRAM),
					machList.get(i),
					surface,
					altitude,
					ar,
					oswald,
					cd0,
					tcMax,
					sweepHalfChord,
					AirfoilTypeEnum.MODERN_SUPERCRITICAL));

		System.out.println("Efficiency Matrix\n");
		for (int i=0; i<efficiencyList.size(); i++)
			System.out.println(Arrays.toString(efficiencyList.get(i)));

		System.out.println("-----------------------------------------------------------");

		//-----------------------------------------------------------------------------------
		// Specific range test:
		List<Double[]> specificRange = new ArrayList<Double[]>();
		for (int i=0; i<maxTakeOffMassArray.length; i++)
			specificRange.add(SpecificRangeCalc.calculateSpecificRangeVsMach(
					Amount.valueOf(maxTakeOffMassArray[i], SI.KILOGRAM),
					machList.get(i),
					sfcList.get(i),
					efficiencyList.get(i),
					altitude,
					byPassRatio,
					0.85,
					EngineTypeEnum.TURBOFAN));

		System.out.println("SPECIFIC RANGE MATRIX [nmi/lb]\n");
		for (int i=0; i<specificRange.size(); i++)
			System.out.println(Arrays.toString(specificRange.get(i)));

		System.out.println("-----------------------------------------------------------");

		//-----------------------------------------------------------------------------------
		// PLOTTING:

		// Mass in lbs
		for (int i=0; i<maxTakeOffMassArray.length; i++)
			maxTakeOffMassArray[i] = Amount.valueOf(maxTakeOffMassArray[i], SI.KILOGRAM).to(NonSI.POUND).getEstimatedValue();

		// building legend
		List<String> legend = new ArrayList<String>();
		for(int i=0; i<maxTakeOffMassArray.length; i++)
			legend.add("MTOM = " + maxTakeOffMassArray[i] + " lbs ");

		SpecificRangeCalc.createSpecificRangeChart(specificRange, machList, legend);
		SpecificRangeCalc.createSfcChart(sfcList, machList, legend, EngineTypeEnum.TURBOFAN);
		SpecificRangeCalc.createEfficiencyChart(efficiencyList, machList, legend);
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public File get_inputFile() {
		return _inputFile;
	}
}