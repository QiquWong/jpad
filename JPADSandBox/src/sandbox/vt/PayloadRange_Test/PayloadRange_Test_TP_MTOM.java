package sandbox.vt.PayloadRange_Test;

import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface2Panels;
import calculators.performance.PayloadRangeCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AirplaneType;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.FuelFractionDatabaseReader;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.customdata.CenterOfGravity;

public class PayloadRange_Test_TP_MTOM{

	//---------------------------------------------------------------------------------
	// MAIN:
	public static void main(String[] args) throws HDF5LibraryException, NullPointerException{

		System.out.println("--------------------------------------------------------");
		System.out.println("PayloadRangeCalc_Test :: main");
		System.out.println("--------------------------------------------------------");
		System.out.println(" ");

		//------------------------------------------------------------------------------------
		// Assign all default folders
		MyConfiguration.initWorkingDirectoryTree();

		//------------------------------------------------------------------------------------
		// Setup database(s)
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		String fuelFractionDatabaseFileName = "FuelFractions.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		FuelFractionDatabaseReader fuelFractionReader = new FuelFractionDatabaseReader(databaseFolderPath, fuelFractionDatabaseFileName);

		//------------------------------------------------------------------------------------
		// Operating Condition / Aircraft / AnalysisManager (geometry calculations)
		OperatingConditions theCondition = new OperatingConditions();
		theCondition.set_machCurrent(0.43);
		theCondition.set_altitude(Amount.valueOf(6000.0, SI.METER));
		theCondition.calculate();
		
		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.ATR72);
		aircraft.set_name("ATR-72");
		aircraft.get_theFuelTank().setFuelFractionDatabase(fuelFractionReader);
		aircraft.get_weights().set_OEM(Amount.valueOf(12950, SI.KILOGRAM));
		aircraft.get_weights().set_MTOM(Amount.valueOf(22500, SI.KILOGRAM));
		aircraft.get_configuration().set_maxPax(77.0);

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
		theWing.setAerodynamics(theLSAnalysis);
		theAnalysis.doAnalysis(aircraft,AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY);

		//------------------------------------------------------------------------------------
		// Creating the Calculator Object

		PayloadRangeCalc test = new PayloadRangeCalc(
				// this call sets parameters to default aircraft values (ATR72)
				theCondition,
				aircraft,
				AirplaneType.TURBOPROP_REGIONAL);
		test.setPaxSingleMass(Amount.valueOf(95, SI.KILOGRAM));

		// -----------------------CRITICAL MACH NUMBER CHECK----------------------------

		boolean check = test.checkCriticalMach(theCondition.get_machCurrent());

		if (check)
			System.out.println("\n\n-----------------------------------------------------------"
					+ "\nCurrent Mach is lower then critical Mach number."
					+ "\nCurrent Mach = " + theCondition.get_machCurrent()
					+ "\nCritical Mach = " + test.getCriticalMach()
					+ "\n\n\t CHECK PASSED --> PROCEDING TO CALCULATION "
					+ "\n\n"
					+ "-----------------------------------------------------------");
		else{
			System.err.println("\n\n-----------------------------------------------------------"
					+ "\nCurrent Mach is bigger then critical Mach number."
					+ "\nCurrent Mach = " + theCondition.get_machCurrent()
					+ "\nCritical Mach = " + test.getCriticalMach()
					+ "\n\n\t CHECK NOT PASSED --> WARNING!!! "
					+ "\n\n"
					+ "-----------------------------------------------------------");
		}

		// ------------------------MTOM PARAMETERIZATION---------------------------------

		test.createPayloadRangeMatrices(
				test.getSweepHalfChordEquivalent(),
				test.getSurface(),
				test.getCd0(),
				test.getOswald(),
				test.getCl(),
				test.getAr(),
				test.getTcMax(),
				test.setByPassRatio(0.0),
				test.getEta(),
				test.getAltitude(),
				test.getCurrentMach(),
				false
				);
		// ------------------------------PLOTTING----------------------------------------
		// MTOM parameterization:

		test.createPayloadRangeCharts_MaxTakeOffMass(
				test.getRangeMatrix(),
				test.getPayloadMatrix()
				);
	}
	//------------------------------------------------------------------------------------------
	// END OF THE TEST
}