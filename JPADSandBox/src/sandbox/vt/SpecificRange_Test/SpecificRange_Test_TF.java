package sandbox.vt.SpecificRange_Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import calculators.aerodynamics.AerodynamicCalc;
import configuration.MyConfiguration;
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

public class SpecificRange_Test_TF {
	
	//------------------------------------------------------------------------------------------
	// MAIN:
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		
		System.out.println("-----------------------------------------------------------");
		System.out.println("SpecificRangeCalc_Test :: TURBOFAN");
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
		theCondition.set_altitude(Amount.valueOf(10000.0, SI.METER));
		theCondition.set_machCurrent(0.84);
		Aircraft aircraft = Aircraft.createDefaultAircraft("B747-100B");

		aircraft.get_theAerodynamics().set_aerodynamicDatabaseReader(aeroDatabaseReader);
		aircraft.set_name("B747-100B");
		aircraft.get_wing().set_theCurrentAirfoil(
				new MyAirfoil(
						aircraft.get_wing(), 
						0.5
						)
				);	
		aircraft.get_wing().get_theCurrentAirfoil().set_type(AirfoilTypeEnum.MODERN_SUPERCRITICAL);

		ACAnalysisManager theAnalysis = new ACAnalysisManager(theCondition);
		theAnalysis.updateGeometry(aircraft);

		//--------------------------------------------------------------------------------------
		// Set the CoG(Bypass the Balance analysis allowing to perform Aerodynamic analysis only)
		CenterOfGravity cgMTOM = new CenterOfGravity();

		// x_cg in body-ref.-frame
		cgMTOM.set_xBRF(Amount.valueOf(23.1, SI.METER)); 
		cgMTOM.set_yBRF(Amount.valueOf(0.0, SI.METER));
		cgMTOM.set_zBRF(Amount.valueOf(0.0, SI.METER));

		aircraft.get_theBalance().set_cgMTOM(cgMTOM);
		aircraft.get_HTail().calculateArms(aircraft);
		aircraft.get_VTail().calculateArms(aircraft);

		theAnalysis.doAnalysis(aircraft, 
				AnalysisTypeEnum.AERODYNAMIC);
		
		// generating variation of mass of 10% until -40% of maxTakeOffMass
		double[] maxTakeOffMassArray = new double[5];
		for (int i=0; i<5; i++)
//			maxTakeOffMassArray[i] = aircraft.get_weights().get_MTOM().getEstimatedValue()*(1-0.1*(4-i));
			maxTakeOffMassArray[i] = aircraft.get_weights().get_MTOM().plus(aircraft.get_weights().get_MLM()).divide(2).getEstimatedValue()*(1-0.1*(4-i));
		
		double cLmax = 1.5; // TODO : Fix when the correct CLmax is calculated from wing
		
		Double[] vStall = new Double[maxTakeOffMassArray.length];
		for(int i=0; i<vStall.length; i++)
			vStall[i] = SpeedCalc.calculateSpeedStall(
					theCondition.get_altitude().getEstimatedValue(),
					Amount.valueOf(maxTakeOffMassArray[i], SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					aircraft.get_wing().get_surface().getEstimatedValue(),
					cLmax);  

		//----------------------------------------------------------------------------------
		// Definition of a Mach array for each maxTakeOffMass
		List<Double[]> machList = new ArrayList<Double[]>();
		
		for(int i=0; i<maxTakeOffMassArray.length; i++) 
			machList.add(MyArrayUtils.linspaceDouble(
					SpeedCalc.calculateMach(
							theCondition.get_altitude().getEstimatedValue(),
							vStall[i]),
					1.0,
					250));
		
		// Drag Thrust Intersection
		double[][] intersection = SpecificRangeCalc.ThrustDragIntersection(
				machList,
				aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
				aircraft.get_powerPlant().get_engineNumber(),
				1, // phi
				aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
				EngineTypeEnum.TURBOFAN,
				EngineOperatingConditionEnum.CRUISE,
				theCondition.get_altitude().getEstimatedValue(),
				maxTakeOffMassArray,
				aircraft.get_wing().get_surface().getEstimatedValue(),
				aircraft.get_theAerodynamics().get_cD0(),
				aircraft.get_theAerodynamics().get_oswald(),
				aircraft.get_wing().get_aspectRatio(),
				aircraft.get_wing().get_sweepHalfChordEq().getEstimatedValue(),
				aircraft.get_wing().get_maxThicknessMean(),
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
					theCondition.get_altitude().getEstimatedValue(),
					aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
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
					aircraft.get_wing().get_surface().getEstimatedValue(),
					theCondition.get_altitude().getEstimatedValue(),
					aircraft.get_wing().get_aspectRatio(),
					aircraft.get_theAerodynamics().get_oswald(),
					aircraft.get_theAerodynamics().get_cD0(),
					aircraft.get_wing().get_maxThicknessMean(),
					aircraft.get_wing().get_sweepHalfChordEq(),
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
					theCondition.get_altitude().getEstimatedValue(),
					aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
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
	// END OF THE TEST
}