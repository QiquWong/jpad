// This is the Test class for Longitudinal Stability. The object of analysis is an aircraft.
//
// The reference angle of attack is alphaBody, that is the angle between the direction of asimptotic 
// velocity and the reference line of fuselage. So, for each component it is necessary to evaluate the
// aerodynamic characteristics, such as lift, drag and moment, having alphaBody as input.
// Moreover for each component are drawn the aerodynamic curves in function of local angle of attack.

// alphaWing = alphaBody + iWing
// alphaHorizontal = alphaBody - downwashAngle +iHorizontal

// So alphaBody is the input data, iWing and iHorizontal are geometry data and downwashAngle must be calculated


package sandbox.mr;

import static java.lang.Math.toRadians;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import configuration.enumerations.DatabaseReaderEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import javafx.util.Pair;

public class Test_MR_07_LongitudinalStability {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) {

		System.out.println("------------------------------------");
		System.out.println("\n Longitudinal Stability Test ");
		System.out.println("\n------------------------------------");

		// -----------------------------------------------------------------------
		// INITIALIZE TEST CLASS
		// -----------------------------------------------------------------------
		
		
		//----------------------------------------------------------------------------------
		// Default folders creation:
		MyConfiguration.initWorkingDirectoryTree();

		
		//------------------------------------------------------------------------------------
		// Operating Condition 
		OperatingConditions theConditions = new OperatingConditions();
		theConditions.set_alphaCurrent(Amount.valueOf(toRadians(2.), SI.RADIAN));
		
		
		//------------------------------------------------------------------------------------
		// Default Aircraft 
		Aircraft aircraft = Aircraft.createDefaultAircraft("ATR-72");
		aircraft.set_name("ATR-72");
		
		//------------------------------------------------------------------------------------
	    // Wing
		LiftingSurface theWing = aircraft.get_wing();

		//--------------------------------------------------------------------------------------
		// Aerodynamic analysis
		ACAnalysisManager theAnalysis = new ACAnalysisManager(theConditions);
		theAnalysis.updateGeometry(aircraft);

		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager(
				theConditions, 
				theWing,
				aircraft
				); 

		aircraft.get_wing().setAerodynamics(theLSAnalysis);

		theLSAnalysis.setDatabaseReaders(
				new Pair(DatabaseReaderEnum.AERODYNAMIC, 
						"Aerodynamic_Database_Ultimate.h5"),
				new Pair(DatabaseReaderEnum.HIGHLIFT, "HighLiftDatabase.h5")
				);
	}

}
