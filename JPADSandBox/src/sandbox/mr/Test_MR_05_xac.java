package sandbox.mr;

import aircraft.OperatingConditions;

import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface2Panels;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;


// This class test the static method calcXacFromNapolitanoDatcom that calculates the 
//center (AC) from the MAC leading edge using the Napolitano-Datcom method.

public class Test_MR_05_xac {

	public static void main(String[] args) {
		
	
		System.out.println("TEST START");
		System.out.println("-------------------------------------------------------- \n");
	
		// Assign all default folders
		MyConfiguration.initWorkingDirectoryTree();

		// Setup database(s)	
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String databaseFileName = "Aerodynamic_Database_Ultimate.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath, databaseFileName);
		
		// Operating Condition / Aircraft / AnalysisManager (geometry calculations)
		OperatingConditions theCondition = new OperatingConditions();
		Aircraft aircraft = Aircraft.createDefaultAircraft();
		LiftingSurface2Panels theWing = aircraft.get_wing();

		aircraft.get_theAerodynamics().set_aerodynamicDatabaseReader(aeroDatabaseReader);
		
		ACAnalysisManager theAnalysis = new ACAnalysisManager(theCondition);	
		theAnalysis.updateGeometry(aircraft);
		
		LSAerodynamicsManager theWingAnalysis = new LSAerodynamicsManager(theCondition, theWing, aircraft);
		LSAerodynamicsManager.CalcXAC theXACAnalysis = theWingAnalysis.new CalcXAC(); // because CalcXAC is a neasted class

		
		double xacQuarter= theXACAnalysis.atQuarterMAC();
		double xacSforza= theXACAnalysis.deYoungHarper();
		double xacNapolitano= theXACAnalysis.datcomNapolitano();
		
		System.out.println(" The value of dimensional XAC evaluated by Napolitano Method is -->" + xacQuarter);
		System.out.println("\n The value of dimensional XAC evaluated by Napolitano Method is -->" + xacSforza);
		System.out.println("\n The value of dimensional XAC evaluated by Napolitano Method is -->" + xacNapolitano);
	//TODO: check the results
	

	}

}
