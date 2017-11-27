package it.unina.daf.jpadcad.occ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.measure.unit.SI;

import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.TopoDS_Compound;
import processing.core.PVector;

public final class OCCUtils {

	public static CADShapeFactory theFactory;

	static public void initCADShapeFactory() {
		CADShapeFactory.setFactory(new OCCShapeFactory());
		theFactory = CADShapeFactory.getFactory();
	}
	
	public static boolean write(String fileName, OCCShape ... shapes) {
		
		if (shapes.length == 0)
			return false;
		
		OCCShape[] nonNullShapes = Arrays.stream(shapes)
                .filter(s -> s != null)
                .toArray(size -> new OCCShape[size]);
		
		// Put everything in a compound
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound = new TopoDS_Compound();
		builder.MakeCompound(compound);
		// loop through the shapes and add to compound
		for(int k = 0; k < nonNullShapes.length; k++)
			builder.Add(compound, nonNullShapes[k].getShape());
		// write on file
		long result = BRepTools.Write(compound, fileName);
		return (result == 1);
	}
	
	public static OCCShape makePatchThruSections(
			PVector p0, List<List<PVector>> sections, PVector p1) {
		// the global factory variable must be non-null
		if (OCCUtils.theFactory == null)
			return null;
		if (sections.size() < 2)
			return null;
		
		CADVertex v0 = null, v1 = null;
		
		if (p0 != null)
			v0 = OCCUtils.theFactory.newVertex(p0.x, p0.y, p0.z);

		if (p1 != null)
			v1 = OCCUtils.theFactory.newVertex(p1.x, p1.y, p1.z);

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
				                    	(OCCVertex) v0, // initial vertex
										cadGeomCurveList.stream() // purge the null objects
									      				.filter(Objects::nonNull)
									      				.collect(Collectors.toList()),
									    (OCCVertex) v1 // final vertex
									);
		return (OCCShape)cadShell;		
	}
	
	public static OCCShape makePatchThruSections(List<List<PVector>> sections) {
		return makePatchThruSections(null, sections, null);
	}
	
	public static OCCShape makePatchThruSections(PVector p0, List<List<PVector>> sections) {
		return makePatchThruSections(p0, sections, null);
	}
	
	public static OCCShape makePatchThruSections(List<List<PVector>> sections, PVector p1) {
		return makePatchThruSections(null, sections, p1);
	}
	
}
