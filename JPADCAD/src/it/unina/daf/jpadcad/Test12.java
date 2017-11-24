package it.unina.daf.jpadcad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.measure.quantity.Length;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import processing.core.PVector;

public class Test12 {

	//-------------------------------------------------------------

	public static Aircraft theAircraft;

	//-------------------------------------------------------------

	public static OCCShape makeFuselageNosePatch(List<List<PVector>> sections) {
		
		// the global factory variable must be non-null
		if (OCCUtils.theFactory == null)
			return null;
		if (sections.size() < 2)
			return null;

		boolean isPeriodic = false;
		
		List<CADGeomCurve3D> cadGeomCurveList = new ArrayList<CADGeomCurve3D>();

		for(List<PVector> sectionPoints : sections) {
			
			// list of points belonging to the desired curve-1
			cadGeomCurveList.add(
				OCCUtils.theFactory.newCurve3D(
					sectionPoints.stream()
					.map(p -> new double[]{p.x, p.y, p.z})
					.collect(Collectors.toList()),
					isPeriodic)
				);
		}
	
		// The CADShell object
		System.out.println("Surfacing ...");
		CADShell cadShell = OCCUtils.theFactory
				                    .newShell(
										cadGeomCurveList.stream() // purge the null objects
									      				.filter(Objects::nonNull)
									      				.collect(Collectors.toList())
									);
		return (OCCShape)cadShell;
	}
	
	public static void main(String[] args) throws InvalidFormatException, HDF5LibraryException {

		System.out.println("-------------------");
		System.out.println("JPADCAD Test");
		System.out.println("-------------------");
		
		Test12.theAircraft = AircraftUtils.createAircraft(args);

		System.out.println("Getting the fuselage ...");
		
		Fuselage fuselage = theAircraft.getFuselage();
		
		Amount<Length> noseLength = fuselage.getFuselageCreator().getLengthNoseTrunk();
		
		System.out.println("Getting selected sections ...");

		// selected sections of the nose trunk
		
		List<Double> xs = Arrays.asList(new Double[] {0.2, 0.4, 0.6, 0.8, 1.0});

		System.out.println("Mose trunk selected sections, normalized x-stations: " + xs.toString());
		
		List<List<PVector>> sections = new ArrayList<List<PVector>>();
		
		xs.stream()
			.forEach(x -> sections.add(
				fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength.times(x)))
			);

		System.out.println("========== Initialize CAD shape factory");
		OCCUtils.initCADShapeFactory();
		
		System.out.println("========== Construct a fuselage nose patch");
		OCCShape patch1 = Test12.makeFuselageNosePatch(sections);

		// Write to a file
		String fileName = "test12.brep";

		if (OCCUtils.write(fileName, ((OCCShape)patch1)))
			System.out.println("========== Output written on file: " + fileName);

	}

}
