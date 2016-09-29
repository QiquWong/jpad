package sandbox.vt;

import java.io.File;

import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;

public class TestAerodynamicDatabaseReaderKOmega {

	/**
	 * TEST THE FUNCTIONS.
	 * 
	 * @param args
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String args[]) throws InstantiationException, IllegalAccessException {

		AerodynamicDatabaseReader reader = new AerodynamicDatabaseReader(
				"data"
				+ File.separator,
				"Aerodynamic_Database_Ultimate.h5"
				);
		
		double kOmegaPhillipsAndAlley = reader.getKOmegePhillipsAndAlley(
				5,
				0.064,
				1.6,
				0.5,
				4
				);
		System.out.println("kOmegaPhillipsAndAlley = "  + kOmegaPhillipsAndAlley);
		
	}
	
}
