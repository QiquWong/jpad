package it.unina.daf.jpadcad.occ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.GeomAbs_Shape;
import opencascade.TopoDS_CompSolid;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Shape;
import processing.core.PVector;

public final class OCCUtils {

	public static CADShapeFactory theFactory;

	static public void initCADShapeFactory() {
		if (theFactory == null) {
			CADShapeFactory.setFactory(new OCCShapeFactory());
			theFactory = CADShapeFactory.getFactory();
		}
	}
	
	// Example: write("test.brep", occshape1, occsape2, occshape3)	
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
	
	// Example: write("test.brep", occshape1, occsape2, listOfOccshapes, occshape3)
	public static boolean write(String fileName, Object ... objects) {
		
		if (objects.length == 0)
			return false;

		Object[] nonNullObjects = Arrays.stream(objects)
                .filter(o -> o != null)
                .toArray(size -> new Object[size]);
		
		OCCShape[] nonNullShapes = Arrays.stream(nonNullObjects)
                .filter(o -> o != null)
                .filter(o -> (o instanceof  OCCShape))
                .map(o -> (OCCShape)o)
                .toArray(size -> new OCCShape[size]);
		
		List<OCCShape> listShapes = new ArrayList<>();
		Arrays.stream(nonNullObjects)
		      .filter(o -> (o instanceof List<?>))
		      .filter(o -> (((List<?>) o).get(0) instanceof OCCShape))
		      .map(o -> (List<OCCShape>) o)
		      .forEach(l -> l.stream().filter(s -> s != null).forEach(s -> listShapes.add(s)));
		
		// Put everything in a compound
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound = new TopoDS_Compound();
		builder.MakeCompound(compound);
		// loop through the shapes and add to compound
		for(int k = 0; k < nonNullShapes.length; k++)
			builder.Add(compound, nonNullShapes[k].getShape());
		// loop through the shapes grabbed from lists and add to compound
		listShapes.stream()
			.forEach(s -> builder.Add(compound, s.getShape()));
		
//		TopoDS_CompSolid compsolid = new TopoDS_CompSolid();
//		builder.MakeCompSolid(compsolid);
//		
//		// === Experimental, trying to write solids, TODO: fixme 
//		listShapes.stream()
//			.filter(s -> s instanceof OCCSolid)
//			.forEach(s -> {
//				System.out.println(">>>>>> Solid");
//				builder.Add(compsolid, s.getShape());
//			});
		
		
//		String fileNameSolids = fileName.replace(".brep", "_solids.brep");
//		long resultSolids = BRepTools.Write(compsolid, fileNameSolids);
//		if (resultSolids == 1)
//			System.out.println("========== [OCCUtils::write] Solids written on file: " + fileNameSolids);

		// ====================
		// write on file
		long result = BRepTools.Write(compound, fileName);		
		return (result == 1);
	}
	
	public static OCCShape makePatchThruSections(
			CADVertex v0, List<CADGeomCurve3D> geomcurves, CADVertex v1) {

		// the global factory variable must be non-null
		if (OCCUtils.theFactory == null)
			return null;
		if (geomcurves.size() < 2)
			return null;
		
		// The CADShell object
		System.out.println("OCCUtils.makePatchThruSections.Surfacing ...");
		CADShell cadShell = OCCUtils.theFactory
				                    .newShell(
				                    	(OCCVertex) v0, // initial vertex
				                    	geomcurves.stream() // purge the null objects
									      				.filter(Objects::nonNull)
									      				.collect(Collectors.toList()),
									    (OCCVertex) v1 // final vertex
									);
		return (OCCShape)cadShell;		
	}
	
	public static OCCShape makePatchThruSections(
			CADVertex v0, List<CADGeomCurve3D> geomcurves) {
		return makePatchThruSections(v0, geomcurves, null);
	}

	public static OCCShape makePatchThruSections(
			List<CADGeomCurve3D> geomcurves, CADVertex v1) {
		return makePatchThruSections(null, geomcurves, v1);
	}
	
	public static OCCShape makePatchThruSections(List<CADGeomCurve3D> geomcurves) {
		return makePatchThruSections(null, geomcurves, null);
	}
	
	public static OCCShape makePatchThruSections(CADGeomCurve3D ... geomcurvesArray) {
		if (geomcurvesArray.length < 2)
			return null;
		return makePatchThruSections(null, 
				Arrays.stream(geomcurvesArray).collect(Collectors.toList()), // from array of CADGeomCurve3D to a List<>
				null);
	}
	
	public static OCCShape makePatchThruSectionsP(CADVertex v0, List<CADGeomCurve3D> geomcurves) {
		return makePatchThruSections(v0, geomcurves, null);
	}
	
	public static OCCShape makePatchThruSectionsP(List<CADGeomCurve3D> geomcurves, CADVertex v1) {
		return makePatchThruSections(null, geomcurves, v1);
	}
	
	public static OCCShape makePatchThruSectionsP(
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

		List<CADGeomCurve3D> cadGeomCurveList = new ArrayList<CADGeomCurve3D>();

		boolean isPeriodic = false;
		for(List<PVector> sectionPoints : sections) {
			cadGeomCurveList.add(
					OCCUtils.theFactory.newCurve3D(
							sectionPoints.stream()
							.map(p -> new double[]{p.x, p.y, p.z})
							.collect(Collectors.toList()), // convert from List<PVector> to List<double[]>
							isPeriodic)
					);
		}
		return OCCUtils.makePatchThruSections(v0, cadGeomCurveList, v1);
	}
	
	public static OCCShape makePatchThruSectionsP(List<List<PVector>> sections) {
		return makePatchThruSectionsP(null, sections, null);
	}
	
	public static OCCShape makePatchThruSectionsP(PVector p0, List<List<PVector>> sections) {
		return makePatchThruSectionsP(p0, sections, null);
	}
	
	public static OCCShape makePatchThruSectionsP(List<List<PVector>> sections, PVector p1) {
		return makePatchThruSectionsP(null, sections, p1);
	}
	
	public static CADShape makeFilledFace(TopoDS_Shape ... shapesArray) {
		// TODO: add checks
		
		return makeFilledFace(Arrays.stream(shapesArray).collect(Collectors.toList()));
	}

	public static CADShape makeFilledFace(List<TopoDS_Shape> shapes) {
		// TODO: add checks
		
		BRepOffsetAPI_MakeFilling filled = new BRepOffsetAPI_MakeFilling();
		shapes.stream()
			.forEach(s -> filled.Add((TopoDS_Edge) s, GeomAbs_Shape.GeomAbs_C0));
		filled.Build();
		System.out.println("[OCCUtils::makeFilledFace] Filled surface is done? = " + filled.IsDone());

		CADShape face = OCCUtils.theFactory.newShape(filled.Shape());		
		return face;
	}
	
	public static CADShape makeFilledFace(CADGeomCurve3D ... crvs) {
		List<TopoDS_Shape> shapes = new ArrayList<>();
		Arrays.stream(crvs).collect(Collectors.toList()).stream()
			.map(crv -> crv.edge())
			.forEach(cadedge -> shapes.add(
					((OCCEdge)cadedge).getShape())
			);
		return makeFilledFace(shapes);
	}	
}
