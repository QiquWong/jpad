package it.unina.daf.jpadcad;

import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

public class Test14 {

	//-------------------------------------------------------------

	public static Aircraft theAircraft;

	//-------------------------------------------------------------

	public static void main(String[] args) throws InvalidFormatException, HDF5LibraryException {
		System.out.println("-------------------");
		System.out.println("JPADCAD Test");
		System.out.println("-------------------");

		Test14.theAircraft = AircraftUtils.importAircraft(args);

		System.out.println("-------------------");
		System.out.println("Getting the fuselage ...");

		Fuselage fuselage = theAircraft.getFuselage();
		
		System.out.println("========== Initialize CAD shape factory");
		OCCUtils.initCADShapeFactory();
		
		boolean supportShapes = true;
		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, supportShapes);
		
		// Write to a file
		String fileName = "test14.brep";
		
		if (OCCUtils.write(fileName, fuselageShapes))
			System.out.println("========== Output written on file: " + fileName);

	}	
}
