package sandbox.vt.PayloadRange_Test;

import java.util.List;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.AirplaneType;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.FuelFractionDatabaseReader;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.customdata.CenterOfGravity;

public class PayloadRange_Test_TP{
		
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
		Aircraft aircraft = Aircraft.createDefaultAircraft("ATR-72");
		
		aircraft.get_theAerodynamics().set_aerodynamicDatabaseReader(aeroDatabaseReader);
		aircraft.get_theFuelTank().setFuelFractionDatabase(fuelFractionReader);
		aircraft.set_name("ATR-72");
		aircraft.get_wing().set_theCurrentAirfoil(
				new MyAirfoil(
						aircraft.get_wing(), 
						0.5
						)
				);		
		
		ACAnalysisManager theAnalysis = new ACAnalysisManager(theCondition);
		theAnalysis.updateGeometry(aircraft);

		//--------------------------------------------------------------------------------------
		// Set the CoG(Bypass the Balance analysis allowing to perform Aerodynamic analysis only)
		CenterOfGravity cgMTOM = new CenterOfGravity();

		// x_cg in body-ref.-frame
		cgMTOM.set_xBRF(Amount.valueOf(12.0, SI.METER)); 
		cgMTOM.set_yBRF(Amount.valueOf(0.0, SI.METER));
		cgMTOM.set_zBRF(Amount.valueOf(2.3, SI.METER));

		aircraft.get_theBalance().set_cgMTOM(cgMTOM);
		aircraft.get_HTail().calculateArms(aircraft);
		aircraft.get_VTail().calculateArms(aircraft);
		
		theAnalysis.doAnalysis(aircraft, 
				AnalysisTypeEnum.AERODYNAMIC);
		
		//------------------------------------------------------------------------------------
		// Creating the Calculator Object
		
		PayloadRangeCalc test = new PayloadRangeCalc(
				// this call sets parameters to default aircraft values (ATR72)
				theCondition, 
				aircraft,
				AirplaneType.TURBOPROP_REGIONAL);
		
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
		
		// ---------------------------BEST RANGE CASE-----------------------------------			
		
		System.out.println();
		System.out.println("-------------------BEST RANGE CASE-------------------------");
		
		List<Amount<Length>> vRange_BR = test.createRangeArray(
				test.getMaxTakeOffMass(),
				test.getSweepHalfChordEquivalent(),
				test.getSurface(),
				test.getCd0(),
				test.getOswald(),
				aircraft.get_theAerodynamics().getcLE(),
				test.getAr(),
				test.getTcMax(),
				test.setByPassRatio(0.0),
				test.getEta(),
				test.getAltitude(),
				test.calculateBestRangeMach(
						EngineTypeEnum.TURBOPROP,
						test.getSurface(),
						test.getAr(),
						test.getOswald(),
						test.getCd0(),
						test.getAltitude()),
				true);
				
		// -------------------------USER CURRENT MACH------------------------------------
		
		System.out.println();
		System.out.println("-------------------CURRENT MACH CASE-------------------------");
		List<Amount<Length>> vRange_CM = test.createRangeArray(
				test.getMaxTakeOffMass(),
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
				false);
		
		// ------------------------------PLOTTING----------------------------------------		
		// Mach parameterization:
		List<Double> vPayload = test.createPayloadArray();
		test.createPayloadRangeCharts_Mach(
				vRange_BR,
				vRange_CM,
				vPayload,
				test.calculateBestRangeMach(
						EngineTypeEnum.TURBOPROP,
						test.getSurface(),
						test.getAr(),
						test.getOswald(),
						test.getCd0(),
						test.getAltitude()),
				test.getCurrentMach());
	}
	//------------------------------------------------------------------------------------------
	// END OF THE TEST
}