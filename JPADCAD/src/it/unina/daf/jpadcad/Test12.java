package it.unina.daf.jpadcad;

import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.fuselage.LineMeshYZ;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

public class Test12 {

	//-------------------------------------------------------------

	public static Aircraft theAircraft;

	//-------------------------------------------------------------

	public static void main(String[] args) throws InvalidFormatException, HDF5LibraryException {


		long startTime = System.currentTimeMillis();        
		
		System.out.println("-------------------");
		System.out.println("JPADCAD Test");
		System.out.println("-------------------");
		
		Test12.theAircraft = AircraftUtils.createAircraft(args);
		
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("\nTIME ESTIMATED = " + (estimatedTime/1000) + " seconds");

		System.out.println("Getting the fuselage ...");
		
		Fuselage fuselage = theAircraft.getFuselage();
		
		LineMeshYZ lineMesh = new LineMeshYZ(
				fuselage, 
				fuselage.getFuselageCreator().getSectionsYZ().get(3),  // the section XY
				fuselage.getFuselageCreator().getLenN(), // the X-coordinate
				20                                     // number of mesh intervals
				);

		// List<PVector> = lineMesh.get_meshPoints()
		
		// TODO: implement a function getMeshPoints returning a list of JPADPVector
		
		// System.out.println("lineMesh: \n" + lineMesh);
	}

}
