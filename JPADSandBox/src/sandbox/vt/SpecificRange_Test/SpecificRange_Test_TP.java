package sandbox.vt.SpecificRange_Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface2Panels;
import calculators.aerodynamics.DragCalc;
import calculators.performance.PerformanceCalcUtils;
import calculators.performance.SpecificRangeCalc;
import calculators.performance.ThrustCalc;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.DragThrustIntersectionMap;
import calculators.performance.customdata.ThrustMap;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;
import standaloneutils.customdata.CenterOfGravity;

public class SpecificRange_Test_TP {

	//------------------------------------------------------------------------------------------
	// MAIN:
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {

		System.out.println("-----------------------------------------------------------");
		System.out.println("SpecificRangeCalc_Test :: TURBOPROP");
		System.out.println("-----------------------------------------------------------\n");

		//----------------------------------------------------------------------------------
		// Default folders creation:
		MyConfiguration.initWorkingDirectoryTree();

		//------------------------------------------------------------------------------------
		// Setup database(s)
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);

		//------------------------------------------------------------------------------------
		// Operating Condition / Aircraft / AnalysisManager (geometry calculations)
		OperatingConditions theCondition = new OperatingConditions();
		theCondition.set_altitude(Amount.valueOf(6000.0, SI.METER));
		theCondition.set_machCurrent(0.45);
		theCondition.calculate();

		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.ATR72);
		aircraft.set_name("ATR-72");

		LiftingSurface2Panels theWing = aircraft.get_wing();

		ACAnalysisManager theAnalysis = new ACAnalysisManager(theCondition);
		theAnalysis.updateGeometry(aircraft);

		// Set the CoG(Bypass the Balance analysis allowing to perform Aerodynamic analysis only)
		CenterOfGravity cgMTOM = new CenterOfGravity();

		// x_cg in body-ref.-frame
		cgMTOM.set_xBRF(Amount.valueOf(12.0, SI.METER));
		cgMTOM.set_yBRF(Amount.valueOf(0.0, SI.METER));
		cgMTOM.set_zBRF(Amount.valueOf(2.3, SI.METER));

		aircraft.get_theBalance().set_cgMTOM(cgMTOM);
		aircraft.get_HTail().calculateArms(aircraft);
		aircraft.get_VTail().calculateArms(aircraft);

		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager(
				theCondition,
				theWing,
				aircraft
				);

		theLSAnalysis.set_AerodynamicDatabaseReader(aeroDatabaseReader);
		theAnalysis.doAnalysis(aircraft, AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY);
		theWing.setAerodynamics(theLSAnalysis);

		// generating variation of mass of 10% until -40% of maxTakeOffMass
		double[] maxTakeOffMassArray = new double[5];
		for (int i=0; i<5; i++)
//			maxTakeOffMassArray[i] = aircraft.get_weights().get_MTOM().getEstimatedValue()*(1-0.1*(4-i));
			maxTakeOffMassArray[i] = aircraft.get_weights().get_MTOM().plus(aircraft.get_weights().get_MLM()).divide(2).getEstimatedValue()*(1-0.1*(4-i));

		double[] weight = new double[maxTakeOffMassArray.length];
		for(int i=0; i<weight.length; i++)
			weight[i] = maxTakeOffMassArray[i]*AtmosphereCalc.g0.getEstimatedValue();

		System.out.println("\n\nMAX TAKE OFF MASS ARRAY:");
		for(int i=0; i<maxTakeOffMassArray.length; i++)
			System.out.print(maxTakeOffMassArray[i] + " ");
		System.out.println("\n\n");

		double cLmax = 1.6; // TODO : Fix when the correct CLmax is calculated from wing

		// Drag Thrust Intersection
		double[] speed = MyArrayUtils.linspace(
				SpeedCalc.calculateTAS(
						0.05,
						theCondition.get_altitude().getEstimatedValue()
						),
				SpeedCalc.calculateTAS(
						1.0,
						theCondition.get_altitude().getEstimatedValue()
						),
				250
				);

		List<DragMap> listDrag = new ArrayList<DragMap>();
		for(int i=0; i<maxTakeOffMassArray.length; i++)
			listDrag.add(
					new DragMap(
							weight[i],
							theCondition.get_altitude().getEstimatedValue(),
							DragCalc.calculateDragVsSpeed(
									weight[i],
									theCondition.get_altitude().getEstimatedValue(),
									aircraft.get_wing().get_surface().getEstimatedValue(),
									aircraft.get_theAerodynamics().get_cD0(),
									aircraft.get_wing().get_aspectRatio(),
									aircraft.get_theAerodynamics().get_oswald(),
									speed,
									aircraft.get_wing().get_sweepHalfChordEq().getEstimatedValue(),
									aircraft.get_wing().get_maxThicknessMean(),
									AirfoilTypeEnum.CONVENTIONAL
									),
							speed
							)
					);

		List<ThrustMap> listThrust = new ArrayList<ThrustMap>();
		for(int i=0; i<maxTakeOffMassArray.length; i++)
			listThrust.add(
					new ThrustMap(
							theCondition.get_altitude().getEstimatedValue(),
							1.0, // phi
							ThrustCalc.calculateThrustVsSpeed(
									aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
									1.0, // phi
									theCondition.get_altitude().getEstimatedValue(),
									EngineOperatingConditionEnum.CRUISE,
									EngineTypeEnum.TURBOPROP,
									aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
									aircraft.get_powerPlant().get_engineNumber(),
									speed
									),
							speed,
							aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
							EngineOperatingConditionEnum.CRUISE
							)
					);

		List<DragThrustIntersectionMap> intersectionList = PerformanceCalcUtils
				.calculateDragThrustIntersection(
						new double[] {theCondition.get_altitude().getEstimatedValue()},
						speed,
						weight,
						new double[] {1.0},
						new EngineOperatingConditionEnum[] {EngineOperatingConditionEnum.CRUISE},
						aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
						aircraft.get_wing().get_surface().getEstimatedValue(),
						cLmax,
						listDrag,
						listThrust
						);

		//----------------------------------------------------------------------------------
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
					theCondition.get_altitude().getEstimatedValue(),
					aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
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
					aircraft.get_wing().get_surface().getEstimatedValue(),
					theCondition.get_altitude().getEstimatedValue(),
					aircraft.get_wing().get_aspectRatio(),
					aircraft.get_theAerodynamics().get_oswald(),
					aircraft.get_theAerodynamics().get_cD0(),
					aircraft.get_wing().get_maxThicknessMean(),
					aircraft.get_wing().get_sweepHalfChordEq(),
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
					theCondition.get_altitude().getEstimatedValue(),
					aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
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
				theCondition.get_altitude().getEstimatedValue(),
				maxTakeOffMassArray,
				listDrag,
				listThrust,
				speed
				);
	}
	//------------------------------------------------------------------------------------------
	// END OF THE TEST
}