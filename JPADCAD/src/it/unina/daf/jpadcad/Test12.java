package it.unina.daf.jpadcad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.CADVertex;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMapUtils;

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
		
		Test12.theAircraft = AircraftUtils.importAircraft(args);

		System.out.println("Getting the fuselage ...");
		
		Fuselage fuselage = theAircraft.getFuselage();
		
		Amount<Length> noseLength = fuselage.getFuselageCreator().getLengthNoseTrunk();
		System.out.println("Nose length: " + noseLength);
		Amount<Length> noseCapStation = fuselage.getFuselageCreator().getDxNoseCap();
		System.out.println("Nose cap x-station: " + noseCapStation);
		Double xbarNoseCap = fuselage.getNoseDxCapPercent();
		System.out.println("Nose cap x-station normalized: " + xbarNoseCap);
		Amount<Length> zNoseTip = Amount.valueOf( 
				fuselage.getFuselageCreator().getZOutlineXZLowerAtX(0.0),
				SI.METER);
		System.out.println("Nose tip z: " + zNoseTip);
		
		System.out.println("Getting selected sections ...");

		// selected sections of the nose trunk
		
		List<Double> xbars = Arrays.asList(new Double[] {xbarNoseCap, 0.2, 0.4, 0.6, 0.8, 1.0});

		System.out.println("Mose trunk selected sections, normalized x-stations: " + xbars.toString());
		
		List<List<PVector>> sections = new ArrayList<List<PVector>>();
		
		xbars.stream()
			.forEach(x -> sections.add(
				fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength.times(x)))
			);

		System.out.println("========== Initialize CAD shape factory");
		OCCUtils.initCADShapeFactory();
		
		// nose cap
		CADVertex vertexNoseTip = OCCUtils.theFactory.newVertex(0, 0, zNoseTip.doubleValue(SI.METER));
		List<Double> xx = Arrays.asList(
				MyArrayUtils.halfCosine1SpaceDouble(
						0.0, noseCapStation.doubleValue(SI.METER), 
						15) // n. points
				);
		// points z's on nose outline curve, XZ, upper
		List<double[]> pointsCapXZUpper = xx.stream()
				.map(x -> new double[]{
						x,
						0.0,
						fuselage.getFuselageCreator().getZOutlineXZUpperAtX(x)
						})
				.collect(Collectors.toList());
		// points z's on nose outline curve, XZ, lower
		List<double[]> pointsCapXZLower = xx.stream()
				.map(x -> new double[]{
						x,
						0.0,
						fuselage.getFuselageCreator().getZOutlineXZLowerAtX(x)
						})
				.collect(Collectors.toList());
		// points y's on nose outline curve, XY, right
		List<double[]> pointsCapSideRight = xx.stream()
				.map(x -> new double[]{
						x,
						fuselage.getFuselageCreator().getYOutlineXYSideRAtX(x),
						fuselage.getFuselageCreator().getCamberZAtX(x)
						})
				.collect(Collectors.toList());
				
		CADGeomCurve3D cadCrvCapXZUpper = OCCUtils.theFactory
				.newCurve3D(pointsCapXZUpper, false);
		CADGeomCurve3D cadCrvCapXZLower = OCCUtils.theFactory
				.newCurve3D(pointsCapXZLower, false);
		CADGeomCurve3D cadCrvCapXYRight = OCCUtils.theFactory
				.newCurve3D(pointsCapSideRight, false);

		
		
		System.out.println("========== Construct a fuselage nose patch");
		OCCShape patch1 = Test12.makeFuselageNosePatch(sections);

		// Write to a file
		String fileName = "test12.brep";

		if (
			OCCUtils.write(fileName,
				(OCCShape)patch1,
				// nose cap stuff
				(OCCShape)vertexNoseTip,
				(OCCEdge)((OCCGeomCurve3D)cadCrvCapXZUpper).edge(),
				(OCCEdge)((OCCGeomCurve3D)cadCrvCapXZLower).edge(),
				(OCCEdge)((OCCGeomCurve3D)cadCrvCapXYRight).edge()
				)
			)
			System.out.println("========== Output written on file: " + fileName);

	}

}
