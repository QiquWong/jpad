package it.unina.daf.jpadcad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADVertex;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCVertex;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;

public class Test13 {
	
	//-------------------------------------------------------------

	public static Aircraft theAircraft;

	//-------------------------------------------------------------

	public static void main(String[] args) throws InvalidFormatException, HDF5LibraryException {

		System.out.println("-------------------");
		System.out.println("JPADCAD Test");
		System.out.println("-------------------");

		Test13.theAircraft = AircraftUtils.createAircraft(args);

		System.out.println("Getting the fuselage ...");

		Fuselage fuselage = theAircraft.getFuselage();

		Amount<Length> noseLength = fuselage.getFuselageCreator().getLengthNoseTrunk();
		System.out.println("Nose length: " + noseLength);
		Amount<Length> noseCapStation = fuselage.getFuselageCreator().getDxNoseCap();
		System.out.println("Nose cap x-station: " + noseCapStation);
		Double xbarNoseCap = fuselage.getNoseDxCapPercent(); // normalized with noseLength
		System.out.println("Nose cap x-station normalized: " + xbarNoseCap);
		Amount<Length> zNoseTip = Amount.valueOf( 
				fuselage.getFuselageCreator().getZOutlineXZLowerAtX(0.0),
				SI.METER);
		System.out.println("Nose tip z: " + zNoseTip);

		System.out.println("========== Initialize CAD shape factory");
		OCCUtils.initCADShapeFactory();
		
		System.out.println("========== Patch 1: nose cap, from nose tip to x=" + noseCapStation);
		
		System.out.println("Getting selected sections ...");
		// all xbar's are normalized with noseLength
		List<Double> xbars1 = Arrays.asList(
//				new Double[] {
//						0.2*xbarNoseCap, 0.4*xbarNoseCap, 0.8*xbarNoseCap, 1.0*xbarNoseCap}
				MyArrayUtils
					// .linspaceDouble(
					.halfCosine2SpaceDouble(
					// .cosineSpaceDouble(
						0.2*xbarNoseCap, xbarNoseCap, 
						4) // n. points
				);
		
		
		System.out.println("Nose-cap trunk selected sections, Patch-1, normalized x-stations: " + xbars1.toString());

		List<List<PVector>> sections1 = new ArrayList<List<PVector>>();
		xbars1.stream()
			  .forEach(x -> sections1.add(
					  fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength.times(x)))
			  );

		System.out.println("Constructing the nose-cap patch, Patch-1");
		OCCShape patch1 = 
				OCCUtils.makePatchThruSections(
						new PVector(0.0f, 0.0f, (float) zNoseTip.doubleValue(SI.METER)), // Nose tip vertex
						sections1
				);
		
		List<OCCShape> extraShapesCap = new ArrayList<>();
		sections1.stream()
		         .map(sec -> OCCUtils.theFactory
							         .newCurve3D(
									   sec.stream()
									      .map(p -> new double[]{p.x, p.y, p.z})
									      .collect(Collectors.toList()),
									   false)
		         )
		         .map(crv -> (OCCEdge)((OCCGeomCurve3D)crv).edge())
		         .forEach(e -> extraShapesCap.add(e));
		         
		// other nose cap entities (curves, vertices)
		CADVertex vertexNoseTip = OCCUtils.theFactory.newVertex(0, 0, zNoseTip.doubleValue(SI.METER));
		// nose cap terminal section
		List<PVector> sectionCapTerminal = fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseCapStation);
		CADGeomCurve3D cadCrvCapTerminalSection = OCCUtils.theFactory
				.newCurve3D(
					sectionCapTerminal.stream()
						.map(p -> new double[]{p.x, p.y, p.z})
						.collect(Collectors.toList()),
					false);
		// x stations defining outlines
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
		
		System.out.println("========== Patch 2: x=" + noseCapStation + "to x=" + noseLength);
		
		System.out.println("Getting selected sections ...");

		// all xbar's are normalized with noseLength
		List<Double> xbars2 = Arrays.asList(
//				new Double[] {
//						xbarNoseCap, 0.2, 0.4, 0.6, 0.8, 1.0}
				MyArrayUtils
				// .linspaceDouble(
				// .halfCosine1SpaceDouble(
				.cosineSpaceDouble(
					xbarNoseCap, 1.0, 
					13) // n. points
				);

		System.out.println("Nose trunk selected sections, Patch-2, normalized x-stations: " + xbars2.toString());

		List<List<PVector>> sections2 = new ArrayList<List<PVector>>();
		xbars2.stream()
			  .forEach(x -> sections2.add(
					  fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength.times(x)))
			  );

		System.out.println("Constructing the nose patch, Patch-2");
		OCCShape patch2 = OCCUtils.makePatchThruSections(sections2);

		System.out.println("========== Construct a fuselage nose patch (Sewing Patch-1/Patch-2");
				
		// Write to a file
		String fileName = "test13.brep";

		extraShapesCap.add((OCCVertex)vertexNoseTip);
		extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvCapXZUpper).edge());
		extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvCapXZLower).edge());
		extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvCapXYRight).edge());
		extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvCapTerminalSection).edge());

		if (
				OCCUtils.write(fileName,
						(OCCShape)patch1,
						(OCCShape)patch2,
//						// nose cap stuff
//						(OCCVertex)vertexNoseTip,
//						(OCCEdge)((OCCGeomCurve3D)cadCrvCapXZUpper).edge(),
//						(OCCEdge)((OCCGeomCurve3D)cadCrvCapXZLower).edge(),
//						(OCCEdge)((OCCGeomCurve3D)cadCrvCapXYRight).edge(),
//						(OCCEdge)((OCCGeomCurve3D)cadCrvCapTerminalSection).edge()
						extraShapesCap
						)
				)
			System.out.println("========== Output written on file: " + fileName);

	}

}
