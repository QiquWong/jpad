package it.unina.daf.jpadcad.occ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import opencascade.BOPAlgo_PaveFiller;
import opencascade.BOPDS_DS;
import opencascade.BRepBuilderAPI_MakeVertex;
import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.BRep_Tool;
import opencascade.GeomAPI_ProjectPointOnCurve;
import opencascade.GeomAbs_Shape;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopTools_ListOfShape;
import opencascade.TopoDS;
import opencascade.TopoDS_CompSolid;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Vertex;
import opencascade.gp_Pnt;
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
		// loop through the shapes (not passed in a list) and add to compound
		for(int k = 0; k < nonNullShapes.length; k++)
			builder.Add(compound, nonNullShapes[k].getShape());
		// loop through the shapes grabbed from lists and add to compound
		listShapes.stream()
			.forEach(s -> builder.Add(compound, s.getShape()));
		
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
		// TODO: ??? check if consecutive edges in the list of shapes share the same vertex
		
		BRepOffsetAPI_MakeFilling filled = new BRepOffsetAPI_MakeFilling();

		filled.SetApproxParam(3, 20);
		/* 
		 * Sets the parameters used to approximate the filling surface.
		 * DEFAULTS
		 * int MaxDeg = 8, the highest degree which the polynomial defining the filling surface can have. 
		 * MaxSegments = 9, the greatest number of segments which the filling surface can have. 
		 */
		
		filled.SetConstrParam(0.00001, 0.0001, 0.5, 1.0);
		/*
		 * Sets the values of Tolerances used to control the constraint. 
		 * DEFAULTS
		 * double Tol2d = 0.00001, 
		 * double Tol3d = 0.0001, 	is the maximum distance allowed between the support surface and the constraints.
		 * double TolAng = 0.01, 	is the maximum angle allowed between the normal of the surface and the constraints.
		 * double TolCurv = 0.1, 	is the maximum difference of curvature allowed between the surface and the constraint. 
		 */

		filled.SetResolParam(3, 20, 20, 0);
		/* 
		 * Sets the parameters used for resolution. 
		 * DEFAULTS
		 * int Degree = 3, 			is the order of energy criterion to minimize for computing the deformation of the surface.
		 * 							The recommanded value is i+2 where i is the maximum order of the constraints. 
		 * int NbPtsOnCur = 15,		is the average number of points for discretisation of the edges. 
		 * int NbIter = 2,			is the maximum number of iterations of the process. For each iteration the number of 
		 * 							discretisation points is increased.
		 * long	Anisotropie = 0 
		 * 
		 */
		
		shapes.stream()
			.map(s -> (TopoDS_Edge)s)
			.filter(e -> BRep_Tool.Degenerated(e) != 1)
			.forEach(e -> filled.Add(e, GeomAbs_Shape.GeomAbs_C0));

		System.out.println("[OCCUtils::makeFilledFace] Filling ...");
		
		filled.Build();
		System.out.println("[OCCUtils::makeFilledFace] Filled surface is done? " + (filled.IsDone() == 1));

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
	
	/** Return the number of shapes in one shape */
	public static int numberOfShape(TopoDS_Shape shape, TopAbs_ShapeEnum type)
	{
		int n=0;
		TopExp_Explorer exp = new TopExp_Explorer(shape, type);
		while (exp.More() > 0) {
			n++;
			exp.Next();
		}
		return n;
	}
	
	public static String reportOnShape(TopoDS_Shape shape, String ... prepends) {
		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n");
		
		java.util.Arrays.asList(prepends).stream()
			.forEach(s -> sb.append("\t" +s + "\n")); // user additional log messages
				
		sb
			.append("\tTopoDS_Shape report\n")
			//.append("\t-------------------------------------\n")
			//.append("\tTypes: ")
			//.append( java.util.Arrays.asList( TopAbs_ShapeEnum.class.getEnumConstants()) + "\n")
			.append("\t-------------------------------------\n");
		
		sb
			.append("\tShapes of type TopAbs_SHAPE: " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_SHAPE) + "\n")
			.append("\tShapes of type TopAbs_VERTEX " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_VERTEX) + "\n")
			.append("\tShapes of type TopAbs_EDGE " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_EDGE) + "\n")
			.append("\tShapes of type TopAbs_WIRE " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_WIRE) + "\n")
			.append("\tShapes of type TopAbs_FACE " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_FACE) + "\n")
			.append("\tShapes of type TopAbs_SHELL " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_SHELL) + "\n")
			.append("\tShapes of type TopAbs_SOLID " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_SOLID) + "\n")
			.append("\tShapes of type TopAbs_COMPSOLID " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_COMPSOLID) + "\n")
			.append("\tShapes of type TopAbs_COMPOUND " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_COMPOUND) + "\n")
			.append("\t-------------------------------------");
		
		return sb.toString();
	}
	
	public static List<OCCEdge> splitEdge(CADGeomCurve3D cadCurve, double[] pnt) {
		List<OCCEdge> result = new ArrayList<>();
		
		GeomAPI_ProjectPointOnCurve poc = new GeomAPI_ProjectPointOnCurve();
		gp_Pnt gpPnt = new gp_Pnt(pnt[0], pnt[1], pnt[2]);
		poc.Init(gpPnt, ((OCCGeomCurve3D)cadCurve).getAdaptorCurve().Curve());
		poc.Perform(gpPnt);
		System.out.println("[OCCUtils.splitEdge]>> Projecting point (" + gpPnt.X() +", "+ gpPnt.Y() +", "+ gpPnt.Z() + ") onto Guide-Curve-1");
		System.out.println("[OCCUtils.splitEdge]>> N. projections: " + poc.NbPoints());
		gp_Pnt gpPnt_1 = null;
		double par_1;
		TopoDS_Edge e1 = null;
		TopoDS_Edge e2 = null;
		TopoDS_Vertex vtx_1 = null;
		// check if at least one projection occurred
		if (poc.NbPoints() > 0) {			
			gpPnt_1 = poc.Point(1);
			System.out.println("[OCCUtils.splitEdge]>> Projected point (" + gpPnt_1.X() +", "+ gpPnt_1.Y() +", "+ gpPnt_1.Z() + ")" );
			par_1 = poc.Parameter(1);
			System.out.println("[OCCUtils.splitEdge]>> Projected point parameter: " + par_1);
			
			// https://www.opencascade.com/doc/occt-7.0.0/overview/html/occt_user_guides__boolean_operations.html
			// https://github.com/DLR-SC/tigl/src/boolean_operations/CCutShape.cpp
			// http://www.algotopia.com/contents/opencascade/opencascade_basic
			
			// prepare filler
			TopTools_ListOfShape listOfArguments = new TopTools_ListOfShape();
			
			BRepBuilderAPI_MakeVertex vertexBuilder = new BRepBuilderAPI_MakeVertex(gpPnt_1);
			vtx_1 = vertexBuilder.Vertex();
			
			listOfArguments.Append(vtx_1);
			
			TopoDS_Shape tds_edge =
					((OCCEdge)
							((OCCGeomCurve3D)cadCurve).edge()
							).getShape();

			listOfArguments.Append(tds_edge);
			
			BOPAlgo_PaveFiller paveFiller = new BOPAlgo_PaveFiller();
			paveFiller.SetArguments(listOfArguments);
			paveFiller.Perform();
			
			BOPDS_DS bopds_ds = paveFiller.DS();
			System.out.println("[OCCUtils.splitEdge]>> BOPDS_DS is null? " + (bopds_ds == null));
			System.out.println("[OCCUtils.splitEdge]>> BOPDS_DS NShapes " + bopds_ds.NbShapes());
			for (int k = 0; k < bopds_ds.NbShapes(); k++) {
				System.out.println("[OCCUtils.splitEdge]>> BOPDS_DS shape " + k + " type: " + bopds_ds.ShapeInfo(k).ShapeType());
				if (k > 1) {
					if (bopds_ds.ShapeInfo(k).ShapeType() == TopAbs_ShapeEnum.TopAbs_EDGE) {
						result.add((OCCEdge) OCCUtils.theFactory.newShape(bopds_ds.Shape(k)));
						System.out.println(
								"[OCCUtils.splitEdge]>> Paves -> edge, has BRep? " + bopds_ds.ShapeInfo(k).HasBRep()
						);
					}
				}
			}
		}
		return result;
	}
	
//	// BRepAlgoAPI_Cut cutter(_source->Shape(), _tool->Shape(), *_dsfiller, Standard_True);
//	BRepAlgoAPI_Cut cutter = new BRepAlgoAPI_Cut(tds_edgeS1, vtxS1Ap_1, paveFiller, 1);
//	cutter.Build();
//	System.out.println(">> BRepAlgoAPI_Cut build error status: " + cutter.ErrorStatus());
//	
//	TopoDS_Shape tds_shape = cutter.Shape();
//	System.out.println(">> BRepAlgoAPI_Cut shape is null? " + tds_shape.IsNull());

	public static OCCVertex getVertexFromEdge(OCCEdge occEdge, int idx) {
		OCCVertex result = null;
		if ((idx == 0) || (idx == 1)) {
			TopoDS_Edge e = TopoDS.ToEdge(occEdge.getShape());
			List<OCCVertex> listVtx = new ArrayList<>();
			TopExp_Explorer exp = new TopExp_Explorer(e, TopAbs_ShapeEnum.TopAbs_VERTEX);
			while (exp.More() > 0) {
				listVtx.add(
						(OCCVertex) OCCUtils.theFactory.newShape(exp.Current())
						);
				exp.Next();
			}
			result = listVtx.get(idx);
		}
		return result;
	}
	
}
