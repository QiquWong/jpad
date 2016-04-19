package sandbox.vt.SpecificRange_Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.measure.quantity.Angle;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import calculators.aerodynamics.DragCalc;
import calculators.geometry.LSGeometryCalc;
import calculators.performance.PerformanceCalcUtils;
import calculators.performance.SpecificRangeCalc;
import calculators.performance.ThrustCalc;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.DragThrustIntersectionMap;
import calculators.performance.customdata.ThrustMap;
import configuration.MyConfiguration;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class SpecificRange_Test_TP_SA {
	
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
	public SpecificRange_Test_TP_SA() {
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
		System.out.println("SpecificRangeCalc_Test :: TURBOPROP");
		System.out.println("-----------------------------------------------------------\n");
		
		SpecificRange_Test_TP_SA main = new SpecificRange_Test_TP_SA();
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
		
		List<String> maxTakeOffMass_property = reader.getXMLPropertiesByPath("//ATR72/Maximum_take_off_mass");	
		double maxTakeOffMass = Double.valueOf(maxTakeOffMass_property.get(0));
		List<String> maxLandingMass_property = reader.getXMLPropertiesByPath("//ATR72/Maximum_landing_mass");	
		double maxLandingMass = Double.valueOf(maxLandingMass_property.get(0));
		List<String> altitude_property = reader.getXMLPropertiesByPath("//ATR72/Altitude");	
		double altitude = Double.valueOf(altitude_property.get(0));
		List<String> surface_property = reader.getXMLPropertiesByPath("//ATR72/Planform_surface");
		double surface = Double.valueOf(surface_property.get(0));
		List<String> cLmax_property = reader.getXMLPropertiesByPath("//ATR72/CLmax");
		double cLmax = Double.valueOf(cLmax_property.get(0));
		List<String> byPassRatio_property = reader.getXMLPropertiesByPath("//ATR72/ByPassRatio");
		double byPassRatio = Double.valueOf(byPassRatio_property.get(0));
		List<String> tcMax_property = reader.getXMLPropertiesByPath("//ATR72/Mean_maximum_thickness");
		double tcMax = Double.valueOf(tcMax_property.get(0));
		List<String> ar_property = reader.getXMLPropertiesByPath("//ATR72/AspectRatio");
		double ar = Double.valueOf(ar_property.get(0));	
		List<String> cd0_property = reader.getXMLPropertiesByPath("//ATR72/CD0");
		double cd0 = Double.valueOf(cd0_property.get(0));
		List<String> oswald_property = reader.getXMLPropertiesByPath("//ATR72/Oswald");
		double oswald = Double.valueOf(oswald_property.get(0));
		List<String> t0_property = reader.getXMLPropertiesByPath("//ATR72/Total_Thrust_Single_Engine");
		double t0 = Double.valueOf(t0_property.get(0));
		List<String> engineNumber_property = reader.getXMLPropertiesByPath("//ATR72/Engine_number");
		double engineNumber = Double.valueOf(engineNumber_property.get(0));
		List<String> taperRatioEquivalent_property = reader.getXMLPropertiesByPath("//ATR72/TaperRatio");
		double taperRatioEquivalent = Double.valueOf(taperRatioEquivalent_property.get(0));
		Amount<Angle> sweepLE = (Amount<Angle>)reader.getXMLAmountWithUnitByPath("//ATR72/SweepLE");
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
//			maxTakeOffMassArray[i] = maxTakeOffMass*(1-0.1*(4-i));
			maxTakeOffMassArray[i] = ((maxTakeOffMass + maxLandingMass)/2)*(1-0.1*(4-i));
		
		double[] weight = new double[maxTakeOffMassArray.length];
		for(int i=0; i<weight.length; i++)
			weight[i] = maxTakeOffMassArray[i]*AtmosphereCalc.g0.getEstimatedValue();

		//----------------------------------------------------------------------------------
		// Drag Thrust Intersection		
		double[] speed = MyArrayUtils.linspace(
				SpeedCalc.calculateTAS(
						0.05,
						altitude
						),
				SpeedCalc.calculateTAS(
						1.0,
						altitude
						),
				250
				);

		List<DragMap> listDrag = new ArrayList<DragMap>();
		for(int i=0; i<maxTakeOffMassArray.length; i++)
			listDrag.add(
					new DragMap(
							weight[i],
							altitude,
							DragCalc.calculateDragVsSpeed(
									weight[i],
									altitude,
									surface,
									cd0,
									ar,
									oswald,
									speed,
									sweepHalfChord.getEstimatedValue(),
									tcMax,
									AirfoilTypeEnum.CONVENTIONAL
									),
							speed
							)
					);

		List<ThrustMap> listThrust = new ArrayList<ThrustMap>();
		for(int i=0; i<maxTakeOffMassArray.length; i++)
			listThrust.add(
					new ThrustMap(
							altitude,
							1.0, // phi
							ThrustCalc.calculateThrustVsSpeed(
									t0,
									1.0, // phi
									altitude,
									EngineOperatingConditionEnum.CRUISE,
									EngineTypeEnum.TURBOPROP,
									byPassRatio,
									engineNumber,
									speed
									),
							speed,
							byPassRatio,
							EngineOperatingConditionEnum.CRUISE
							)
					);

		List<DragThrustIntersectionMap> intersectionList = PerformanceCalcUtils
				.calculateDragThrustIntersection(
						new double[] {altitude},
						speed,
						weight,
						new double[] {1.0},
						new EngineOperatingConditionEnum[] {EngineOperatingConditionEnum.CRUISE},
						byPassRatio,
						surface,
						cLmax,
						listDrag,
						listThrust
						);
		
		// Definition of a Mach array for each maxTakeOffMass
		List<Double[]> machList = new ArrayList<Double[]>();
		for(int i=0; i<maxTakeOffMassArray.length; i++) 
			machList.add(MyArrayUtils.linspaceDouble(
					intersectionList.get(i).getMinMach(),
					intersectionList.get(i).getMaxMach(),
					250));
		
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
					EngineTypeEnum.TURBOPROP
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
					AirfoilTypeEnum.CONVENTIONAL));

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
					EngineTypeEnum.TURBOPROP));

		System.out.println("SPECIFIC RANGE MATRIX [nmi/lb]\n");
		for (int i=0; i<specificRange.size(); i++)
			System.out.println(Arrays.toString(specificRange.get(i)));

		System.out.println("-----------------------------------------------------------");

		//-----------------------------------------------------------------------------------
		// PLOTTING:
		
		// building legend
		List<String> legend = new ArrayList<String>();
		for(int i=0; i<maxTakeOffMassArray.length; i++)
			legend.add("MTOM = " + maxTakeOffMassArray[i] + " kg ");

		SpecificRangeCalc.createSpecificRangeChart(specificRange, machList, legend);
		SpecificRangeCalc.createSfcChart(sfcList, machList, legend, EngineTypeEnum.TURBOPROP);
		SpecificRangeCalc.createEfficiencyChart(efficiencyList, machList, legend);
		SpecificRangeCalc.createThrustDragIntersectionChart(
				altitude,
				maxTakeOffMassArray,
				listDrag,
				listThrust,
				speed
				);
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public File get_inputFile() {
		return _inputFile;
	}
}