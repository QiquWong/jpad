package it.unina.daf.jpadcad.occ;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import it.unina.daf.jpadcad.enums.FileExtension;
import opencascade.Adaptor3d_Curve;
import opencascade.Adaptor3d_HCurve;
import opencascade.BOPAlgo_PaveFiller;
import opencascade.BOPDS_DS;
import opencascade.BRepAdaptor_Curve;
import opencascade.BRepBuilderAPI_GTransform;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeVertex;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepFilletAPI_MakeFillet;
import opencascade.BRepGProp;
import opencascade.BRepMesh_IncrementalMesh;
import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.GeomAPI;
import opencascade.GeomAPI_ExtremaCurveCurve;
import opencascade.GeomAPI_IntCS;
import opencascade.GeomAPI_ProjectPointOnCurve;
import opencascade.GeomAbs_Shape;
import opencascade.GeomAdaptor_Curve;
import opencascade.GeomConvert;
import opencascade.Geom_BSplineCurve;
import opencascade.Geom_Curve;
import opencascade.Geom_Geometry;
import opencascade.Geom_TrimmedCurve;
import opencascade.IGESControl_Controller;
import opencascade.IGESControl_Writer;
import opencascade.Interface_Static;
import opencascade.STEPControl_StepModelType;
import opencascade.STEPControl_Writer;
import opencascade.ShapeAnalysis_FreeBounds;
import opencascade.StlAPI_Writer;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopTools_ListOfShape;
import opencascade.TopoDS;
import opencascade.TopoDS_CompSolid;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Shell;
import opencascade.TopoDS_Vertex;
import opencascade.gp_Ax1;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_GTrsf;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;

public final class OCCUtils {

	public static CADShapeFactory theFactory;

	static public void initCADShapeFactory() {
		if (theFactory == null) {
			CADShapeFactory.setFactory(new OCCShapeFactory());
			theFactory = CADShapeFactory.getFactory();
		}
	}
	
	// Example: write("test.brep", occshape1, occshape2, occshape3)	
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
	
	// Example: write("test.brep", occshape1, occshape2, listOfOccshapes, occshape3)
	@SuppressWarnings("unchecked")
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
	
	public static void write(String fileName, FileExtension fileExtension, List<OCCShape> shapes) {
		
		String fileNameComplete = fileName.concat("." + fileExtension.toString().toLowerCase());
		
		// Generate a compound, holding all the shapes that have been passed to the method
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound = new TopoDS_Compound();
		builder.MakeCompound(compound);
		
		// Invoke different OCCT writing utilities depending on the selected file extension
		switch (fileExtension) {
		
		case BREP:
			shapes.forEach(s -> builder.Add(compound, s.getShape()));
			BRepTools.Write(compound, fileNameComplete);
			
			break;
			
		case STEP:
			shapes.forEach(s -> builder.Add(compound, s.getShape()));
			STEPControl_Writer stepWriter = new STEPControl_Writer();
			Interface_Static.SetCVal("write.step.unit", "M");
			stepWriter.Transfer(compound, STEPControl_StepModelType.STEPControl_AsIs);
			stepWriter.Write(fileNameComplete);	
			
			break;
			 
		case IGES:
			if (IGESControl_Controller.Init() == 1) {
				IGESControl_Writer igesWriter = new IGESControl_Writer("2HM");
				shapes.forEach(s -> igesWriter.AddShape(s.getShape()));
				igesWriter.ComputeModel();
				igesWriter.Write(fileNameComplete);
			} else
				System.exit(1);
			
			break;
			
		case STL:
			StlAPI_Writer stlWriter = new StlAPI_Writer();
			BRepMesh_IncrementalMesh incrementalMesh = new BRepMesh_IncrementalMesh();
			
			shapes.forEach(s -> {
				incrementalMesh.SetShape(s.getShape());
				incrementalMesh.Perform();
				builder.Add(compound, incrementalMesh.Shape().Reversed());
			});
			
			stlWriter.Write(compound, fileNameComplete);
			
			break;
		}
	}
	
	public static OCCShape makePatchThruSections(
			CADVertex v0, List<CADWire> cadWires, CADVertex v1) {

		// the global factory variable must be non-null
		if (OCCUtils.theFactory == null)
			return null;
		if (cadWires.size() < 2)
			return null;
		
		// The CADShell object
		CADShell cadShell = OCCUtils.theFactory
				                    .newShell(
				                        v0, // initial vertex
				                    	cadWires.stream() // purge the null objects
									      		.filter(Objects::nonNull)
									      		.collect(Collectors.toList()),
									    v1 // final vertex
									);
		return (OCCShape) cadShell;		
	}
	
	public static OCCShape makePatchThruSections(
			CADVertex v0, List<CADWire> cadWires) {
		return makePatchThruSections(v0, cadWires, null);
	}

	public static OCCShape makePatchThruSections(
			List<CADWire> cadWires, CADVertex v1) {
		return makePatchThruSections(null, cadWires, v1);
	}
	
	public static OCCShape makePatchThruSections(List<CADWire> cadWires) {
		return makePatchThruSections(null, cadWires, null);
	}
	
	public static OCCShape makePatchThruSections(CADWire ... cadWires) {
		if (cadWires.length < 2)
			return null;
		return makePatchThruSections(
				null, 
				Arrays.stream(cadWires).collect(Collectors.toList()), // from array of CADWire to a List<>
				null);
	}
	
	public static OCCShape makePatchThruCurveSections(
			CADVertex v0, List<CADGeomCurve3D> cadCurves, CADVertex v1) {
		
		// The global factory variable must be non-null
		if (OCCUtils.theFactory == null)
			return null;
		if (cadCurves.size() < 2)
			return null;
		
		// Generate a wire for each single curve and add them to a list
		List<CADWire> cadWires = new ArrayList<>();
		cadWires.addAll(cadCurves.stream()
			     .map(crv -> crv.edge())
			     .map(e -> theFactory.newWireFromAdjacentEdges(e))
			     .collect(Collectors.toList()));
		
		// Generate the CADShell object
		CADShell cadShell = OCCUtils.theFactory
				                    .newShell(
				                        v0, // initial vertex
				                    	cadWires.stream() // purge the null objects
									      		.filter(Objects::nonNull)
									      		.collect(Collectors.toList()),
									    v1 // final vertex
									);
		return (OCCShape) cadShell;		
	}
	
	public static OCCShape makePatchThruCurveSections(
			CADVertex v0, List<CADGeomCurve3D> cadCurves) {
		return makePatchThruCurveSections(v0, cadCurves, null);
	}
	
	public static OCCShape makePatchThruCurveSections(
			List<CADGeomCurve3D> cadCurves, CADVertex v1) {
		return makePatchThruCurveSections(null, cadCurves, v1);
	}
	
	public static OCCShape makePatchThruCurveSections(List<CADGeomCurve3D> cadCurves) {
		return makePatchThruCurveSections(null, cadCurves, null);
	}
	
	public static OCCShape makePatchThruCurveSections(CADGeomCurve3D ... cadCurves) {
		if (cadCurves.length < 2)
			return null;
		return makePatchThruCurveSections(
				null, 
				Arrays.stream(cadCurves).collect(Collectors.toList()),
				null);
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

		List<CADWire> cadWireList = new ArrayList<>();
		
		cadWireList.addAll(sections.stream()
				.map(pvs -> theFactory.newCurve3DP(pvs, false))
				.map(crv -> crv.edge())
				.map(edg -> theFactory.newWireFromAdjacentEdges(edg))
				.collect(Collectors.toList()));
		
		return OCCUtils.makePatchThruSections(v0, cadWireList, v1);
	}
	
	public static OCCShape makePatchThruSectionsP(PVector p0, List<List<PVector>> sections) {
		return makePatchThruSectionsP(p0, sections, null);
	}
	
	public static OCCShape makePatchThruSectionsP(List<List<PVector>> sections, PVector p1) {
		return makePatchThruSectionsP(null, sections, p1);
	}
	
	public static OCCShape makePatchThruSectionsP(List<List<PVector>> sections) {
		return makePatchThruSectionsP(null, sections, null);
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
	
	public static CADFace makeFilledFace(List<CADEdge> closedWire, List<double[]> controlPts) {
		CADFace face = null;
		
		// Initialize the filler
		BRepOffsetAPI_MakeFilling filler = new BRepOffsetAPI_MakeFilling();
		
		filler.SetApproxParam(3, 20);
		filler.SetConstrParam(0.00001, 0.0001, 0.5, 1.0);
		filler.SetResolParam(3, 20, 20, 0);
		
		// Add each edge and each control point to the filling maker
		closedWire.forEach(e -> filler.Add(((OCCEdge) e).getShape(), GeomAbs_Shape.GeomAbs_C0));
		
		if (controlPts.size() > 0)
			controlPts.forEach(pt -> filler.Add(new gp_Pnt(pt[0], pt[1], pt[2])));
		
		// Generate the face
		filler.Build();	
		face = (CADFace) theFactory.newShape(filler.Shape());
		
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
		
		Arrays.asList(prepends).stream()
			.forEach(s -> sb.append("\t" + s + "\n")); // user additional log messages
				
		sb
			.append("\tTopoDS_Shape report\n")
			.append("\t-------------------------------------\n");
		
		sb
			.append("\tShapes of type TopAbs_SHAPE: " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_SHAPE) + "\n")
			.append("\tShapes of type TopAbs_VERTEX: " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_VERTEX) + "\n")
			.append("\tShapes of type TopAbs_EDGE: " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_EDGE) + "\n")
			.append("\tShapes of type TopAbs_WIRE: " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_WIRE) + "\n")
			.append("\tShapes of type TopAbs_FACE: " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_FACE) + "\n")
			.append("\tShapes of type TopAbs_SHELL: " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_SHELL) + "\n")
			.append("\tShapes of type TopAbs_SOLID: " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_SOLID) + "\n")
			.append("\tShapes of type TopAbs_COMPSOLID: " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_COMPSOLID) + "\n")
			.append("\tShapes of type TopAbs_COMPOUND: " + OCCUtils.numberOfShape(shape, TopAbs_ShapeEnum.TopAbs_COMPOUND) + "\n")
			.append("\t-------------------------------------");
		
		return sb.toString();
	}
	
	public static CADVertex pointProjectionOnCurve0(CADGeomCurve3D cadCurve, double[] pnt) {
		CADVertex result = null;		
		GeomAPI_ProjectPointOnCurve poc = new GeomAPI_ProjectPointOnCurve();
		gp_Pnt gpPnt = new gp_Pnt(pnt[0], pnt[1], pnt[2]);
		poc.Init(gpPnt, ((OCCGeomCurve3D)cadCurve).getAdaptorCurve().Curve());
		poc.Perform(gpPnt);
//		System.out.println("[OCCUtils.pointProjectionOnCurve]>> Projecting point (" + gpPnt.X() +", "+ gpPnt.Y() +", "+ gpPnt.Z());
//		System.out.println("[OCCUtils.pointProjectionOnCurve]>> N. projections: " + poc.NbPoints());
		gp_Pnt gpPntP = null;
		if(poc.NbPoints() > 0) {
			gpPntP = poc.NearestPoint();
			CADVertex projection = OCCUtils.theFactory.newVertex(gpPntP.X(), gpPntP.Y(), gpPntP.Z());
//			System.out.println("[OCCUtils.pointProjectionOnCurve]>> Projected point (" + Arrays.toString(projection.pnt()));
			result = projection;
		}	
		return result;
	}
	
	public static CADVertex pointProjectionOnCurve(CADGeomCurve3D cadCurve, double[] pnt) {
		CADVertex vertex = null;
		
		double param = pointProjectionOnCurveParam(cadCurve, pnt);	
		vertex = theFactory.newVertex(cadCurve.value(param));
		
		return vertex;
	}
	
	public static double pointProjectionOnCurveParam(CADGeomCurve3D cadCurve, double[] pnt) {
		double param = 0;
		
		GeomAPI_ProjectPointOnCurve projector = new GeomAPI_ProjectPointOnCurve();
		gp_Pnt gpPnt = new gp_Pnt(pnt[0], pnt[1], pnt[2]);
		
		Geom_Curve curve = ((OCCGeomCurve3D) cadCurve).getAdaptorCurve().Curve();
		double uMin = curve.FirstParameter();
		double uMax = curve.LastParameter();
		
		projector.Init(curve, uMin, uMax);
		projector.Perform(gpPnt);
		
		if (projector.NbPoints() > 0) {
			param = projector.LowerDistanceParameter();
		} else {
			System.out.println("[OCCUtils::pointProjectionOnCurveParam] No projection found!");
		}
		
		return param;
	}
	
	public static List<OCCEdge> splitCADCurve(CADGeomCurve3D cadCurve, double[] pnt) {
		List<OCCEdge> result = new ArrayList<>();
		
		double[] range = cadCurve.getRange();
		double projParam = pointProjectionOnCurveParam(cadCurve, pnt);
		
		Geom_BSplineCurve basisCurve = ((OCCGeomCurve3D) cadCurve).getAdaptorCurve().BSpline();
		
		Geom_BSplineCurve curve1 = GeomConvert.SplitBSplineCurve(basisCurve, range[0], projParam, 1e-6, 1);
		Geom_BSplineCurve curve2 = GeomConvert.SplitBSplineCurve(basisCurve, projParam, range[1], 1e-6, 1);
		
		result.add(((OCCEdge) theFactory.newShape(new BRepBuilderAPI_MakeEdge(curve1).Edge())));
		result.add(((OCCEdge) theFactory.newShape(new BRepBuilderAPI_MakeEdge(curve2).Edge())));
			
		return result;	
	}
	
	public static List<OCCEdge> splitCADCurve(CADEdge cadEdge, double[] pnt) {
		return splitCADCurve(theFactory.newCurve3D(cadEdge), pnt);
	}
	
	public static List<OCCEdge> splitEdge(CADGeomCurve3D cadCurve, double[] pnt) {
		List<OCCEdge> result = new ArrayList<>();
		
		CADVertex projection = OCCUtils.pointProjectionOnCurve0(cadCurve, pnt);
		
		TopoDS_Vertex vtx_1 = null;
		
		gp_Pnt gpPnt_1 = new gp_Pnt(projection.pnt()[0], projection.pnt()[1], projection.pnt()[2]);
		
		// check if at least one projection occurred
		if(!projection.equals(null)) {
		
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
//			System.out.println("[OCCUtils.splitEdge]>> BOPDS_DS is null? " + (bopds_ds == null));
//			System.out.println("[OCCUtils.splitEdge]>> BOPDS_DS NShapes " + bopds_ds.NbShapes());
			for (int k = 0; k < bopds_ds.NbShapes(); k++) {
//				System.out.println("[OCCUtils.splitEdge]>> BOPDS_DS shape " + k + " type: " + bopds_ds.ShapeInfo(k).ShapeType());
				if (k > 1) {
					if (bopds_ds.ShapeInfo(k).ShapeType() == TopAbs_ShapeEnum.TopAbs_EDGE) {
						result.add((OCCEdge) OCCUtils.theFactory.newShape(bopds_ds.Shape(k)));
//						System.out.println(
//								"[OCCUtils.splitEdge]>> Paves -> edge, has BRep? " + bopds_ds.ShapeInfo(k).HasBRep()
//						);
					}
				}
			}
		}
		return result;
	}
	
//	public static List<OCCEdge> splitEdge(CADGeomCurve3D cadCurve, List<Double> crvFracs) {
//		List<OCCEdge> result = new ArrayList<>();
//		if(crvFracs.stream().anyMatch(f -> (f <= 0) || (f >= 1))) {
////			System.out.println("[OCCUtils.splitEdge] Values must be positive and less than 1");
//			return result;
//		}
//		int len = crvFracs.size();
//		double[] crvRange = cadCurve.getRange();	
//		List<double[]> pnts = new ArrayList<>();		
//		pnts = crvFracs.stream()
//				       .sorted()
//		               .map(f -> cadCurve.value(f*(crvRange[1] - crvRange[0]) + crvRange[0]))
//					   .collect(Collectors.toList());		
//		TopTools_ListOfShape listOfArguments = new TopTools_ListOfShape();
//		TopoDS_Shape tdsEdge = ((OCCEdge)((OCCGeomCurve3D)cadCurve).edge()).getShape();		
//		pnts.forEach(p -> {
//			gp_Pnt gpPnt = new gp_Pnt(p[0], p[1], p[2]);
//			BRepBuilderAPI_MakeVertex vertexBuilder = new BRepBuilderAPI_MakeVertex(gpPnt);
//			TopoDS_Vertex vtx = vertexBuilder.Vertex();
//			listOfArguments.Append(vtx);
//		});		
//		listOfArguments.Append(tdsEdge);		
//		BOPAlgo_PaveFiller paveFiller = new BOPAlgo_PaveFiller();
//		paveFiller.SetArguments(listOfArguments);
//		paveFiller.Perform();
//		
//		BOPDS_DS bopdsDS = paveFiller.DS();
////		System.out.println("[OCCUtils.splitEdge]>> BOPDS_DS is null? " + (bopdsDS == null));
////		System.out.println("[OCCUtils.splitEdge]>> BOPDS_DS NShapes " + bopdsDS.NbShapes());
//		for(int k = 0; k < bopdsDS.NbShapes(); k++) {
////			System.out.println("[OCCUtils.splitEdge]>> BOPDS_DS shape " + k + " type: " + bopdsDS.ShapeInfo(k).ShapeType());
//			if(k > (len + 1)) {
//				if(bopdsDS.ShapeInfo(k).ShapeType() == TopAbs_ShapeEnum.TopAbs_EDGE) {
//					result.add((OCCEdge) OCCUtils.theFactory.newShape(bopdsDS.Shape(k)));
////					System.out.println("[OCCUtils.splitEdge]>> Paves -> edge, has BRep? " + bopdsDS.ShapeInfo(k).HasBRep()
////					);
//				}
//			}
//		}		
//		return result;
//	}
	
//	public static List<OCCEdge> splitEdgeByPntsList(CADGeomCurve3D cadCurve, List<double[]> pnts) {
//		List<OCCEdge> result = new ArrayList<>();
//		int len = pnts.size();
//		TopTools_ListOfShape listOfArguments = new TopTools_ListOfShape();
//		TopoDS_Shape tdsEdge = ((OCCEdge)((OCCGeomCurve3D)cadCurve).edge()).getShape();
//		pnts.forEach(p -> {
//			gp_Pnt gpPnt = new gp_Pnt(p[0], p[1], p[2]);
//			BRepBuilderAPI_MakeVertex vertexBuilder = new BRepBuilderAPI_MakeVertex(gpPnt);
//			TopoDS_Vertex vtx = vertexBuilder.Vertex();
//			listOfArguments.Append(vtx);
//		});	
//		listOfArguments.Append(tdsEdge);		
//		BOPAlgo_PaveFiller paveFiller = new BOPAlgo_PaveFiller();
//		paveFiller.SetArguments(listOfArguments);
//		paveFiller.Perform();
//		
//		BOPDS_DS bopdsDS = paveFiller.DS();
////		System.out.println("[OCCUtils.splitEdge]>> BOPDS_DS is null? " + (bopdsDS == null));
////		System.out.println("[OCCUtils.splitEdge]>> BOPDS_DS NShapes " + bopdsDS.NbShapes());
//		for(int k = 0; k < bopdsDS.NbShapes(); k++) {
////			System.out.println("[OCCUtils.splitEdge]>> BOPDS_DS shape " + k + " type: " + bopdsDS.ShapeInfo(k).ShapeType());
//			if(k > (len + 1)) {
//				if(bopdsDS.ShapeInfo(k).ShapeType() == TopAbs_ShapeEnum.TopAbs_EDGE) {
//					result.add((OCCEdge) OCCUtils.theFactory.newShape(bopdsDS.Shape(k)));
////					System.out.println("[OCCUtils.splitEdge]>> Paves -> edge, has BRep? " + bopdsDS.ShapeInfo(k).HasBRep()
////					);
//				}
//			}
//		}		
//		return result;
//	}

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
	
	public static List<OCCWire> getClosedWireFromShell(OCCShell occShell) {
		
		ShapeAnalysis_FreeBounds shapeAnalyzer = new ShapeAnalysis_FreeBounds(occShell.getShape());
		TopoDS_Compound wires = shapeAnalyzer.GetClosedWires();
		
		// Explore the compound of wires
		List<OCCWire> expWires = new ArrayList<>();
		
		TopExp_Explorer explorer = new TopExp_Explorer(wires, TopAbs_ShapeEnum.TopAbs_WIRE);
		while (explorer.More() > 0) {
			OCCWire wire = (OCCWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(explorer.Current()));
			expWires.add(wire);
			explorer.Next();			
		}
		
		if (expWires.size() < 1) 
		 	System.err.println("[OCCUtils.getClosedWireFromShell] " + 
							   "Warning: the number of found free wires is incorrect. Returning a null value ...");
	
		return expWires;
	}
	
	public static OCCShape getShapeTranslated(OCCShape shape, double[] initPnt, double[] finalPnt) {
		
		gp_Trsf translation = new gp_Trsf();
		
		translation.SetTranslation(
				new gp_Pnt(initPnt[0], initPnt[1], initPnt[2]), 
				new gp_Pnt(finalPnt[0], finalPnt[1], finalPnt[2])
				);
		
		return (OCCShape) OCCUtils.theFactory.newShape(
				new BRepBuilderAPI_Transform(shape.getShape(), translation, 1).Shape());
	}
	
	public static List<OCCShape> getShapesTranslated(List<OCCShape> shapes, double[] initPnt, double[] finalPnt) {
		
		List<OCCShape> translatedShapes = new ArrayList<>();
		shapes.forEach(s -> translatedShapes.add(getShapeTranslated(s, initPnt, finalPnt)));
		
		return translatedShapes;
	}
	
	public static OCCShape getShapeRotated(OCCShape shape, double[] rotCenter, double[] rotAxis, double rotAngle) {
		
		gp_Trsf rotation = new gp_Trsf();
		
		rotation.SetRotation(
				new gp_Ax1(
						new gp_Pnt(rotCenter[0], rotCenter[1], rotCenter[2]), 
						new gp_Dir(rotAxis[0], rotAxis[1], rotAxis[2])
						), 
				rotAngle
				);
		
		return (OCCShape) OCCUtils.theFactory.newShape(
				new BRepBuilderAPI_Transform(shape.getShape(), rotation, 1).Shape());
	}
	
	public static List<OCCShape> getShapesRotated(List<OCCShape> shapes, double[] rotCenter, double[] rotAxis, double rotAngle) {
		
		List<OCCShape> rotatedShapes = new ArrayList<>();
		shapes.forEach(s -> rotatedShapes.add(getShapeRotated(s, rotCenter, rotAxis, rotAngle)));
		
		return rotatedShapes;
	}
	
	public static OCCShape getShapeScaled(OCCShape shape, double[] centerPnt, double scaleFactor) {
		
		gp_Trsf scale = new gp_Trsf();
		
		scale.SetScale(
				new gp_Pnt(centerPnt[0], centerPnt[1], centerPnt[2]), 
				scaleFactor
				);
		
		return (OCCShape) OCCUtils.theFactory.newShape(
				new BRepBuilderAPI_Transform(shape.getShape(), scale, 1).Shape());	
	}
	
	public static List<OCCShape> getShapesScaled(List<OCCShape> shapes, double[] centerPnt, double scaleFactor) {
		
		List<OCCShape> scaledShapes = new ArrayList<>();
		shapes.forEach(s -> scaledShapes.add(getShapeScaled(s, centerPnt, scaleFactor)));
		
		return scaledShapes;
	}
	
	public static OCCShape getShapeStretched(OCCShape shape, double[] centerPnt, double[] stretchAxis, double stretchingFactor) {
		
		gp_GTrsf stretching = new gp_GTrsf();
		
		stretching.SetAffinity(
				new gp_Ax2(
						new gp_Pnt(centerPnt[0], centerPnt[1], centerPnt[2]), 
						new gp_Dir(stretchAxis[0], stretchAxis[1], stretchAxis[2])
						), 
				stretchingFactor
				);
		
		return (OCCShape) OCCUtils.theFactory.newShape(
				new BRepBuilderAPI_GTransform(shape.getShape(), stretching, 1).Shape());	
	}
	
	public static List<OCCShape> getShapesStretched(List<OCCShape> shapes, double[] centerPnt, double[] stretchAxis, double stretchingFactor) {
		
		List<OCCShape> stretchedShapes = new ArrayList<>();
		shapes.forEach(s -> stretchedShapes.add(getShapeStretched(s, centerPnt, stretchAxis, stretchingFactor)));
		
		return stretchedShapes;
	}
	
	/**
	 * Mirrors the provided shape with respect to a plane
	 * @param shape - The shape to mirror.
	 * @param mirrorPlaneOrigin - The origin of the mirror plane.
	 * @param mirrorPlaneNormal - The normal to the mirror plane.
	 * @param mirrorPlaneInDirection - A direction normal to the previous one and contained in the mirror plane.
	 * @return mirroredShape - The original shape mirrored with respect to the provided plane.
	 */
	public static OCCShape getShapeMirrored(OCCShape shape, 
			PVector mirrorPlaneOrigin, PVector mirrorPlaneNormal, PVector mirrorPlaneInDirection) {
		
		OCCShape mirroredShape = null;
		
		// Initialize the transformation
		gp_Trsf mirroring = new gp_Trsf();
		
		// Generate the symmetry plane
		gp_Ax2 mirrorPlane = new gp_Ax2(
				new gp_Pnt(mirrorPlaneOrigin.x, mirrorPlaneOrigin.y, mirrorPlaneOrigin.z), 
				new gp_Dir(mirrorPlaneNormal.x, mirrorPlaneNormal.y, mirrorPlaneNormal.z),
				new gp_Dir(mirrorPlaneInDirection.x, mirrorPlaneInDirection.y, mirrorPlaneInDirection.z)
				);
		mirroring.SetMirror(mirrorPlane);
		
		// Generate the mirrored shape
		mirroredShape = (OCCShape) OCCUtils.theFactory.newShape(
				new BRepBuilderAPI_Transform(shape.getShape(), mirroring, 1).Shape());
			
		return mirroredShape;
	}
	
	public static List<OCCShape> getShapesMirrored(List<OCCShape> shapes,
			PVector mirroPlaneOrigin, PVector mirrorPlaneNormal, PVector mirrorPlaneInDirection) {
		
		List<OCCShape> mirroredShapes = new ArrayList<>();	
		shapes.forEach(s -> mirroredShapes.add(getShapeMirrored(s, 
				mirroPlaneOrigin, mirrorPlaneNormal, mirrorPlaneInDirection)));
		
		return mirroredShapes;
	}
	
	public static List<double[]> getIntersectionPts(CADGeomCurve3D curve1, CADGeomCurve3D curve2, double tol) {
		List<double[]> intersectionPts = new ArrayList<>();
		
		Geom_Curve geomCurve1 = ((OCCGeomCurve3D) curve1).getAdaptorCurve().Curve();
		Geom_Curve geomCurve2 = ((OCCGeomCurve3D) curve2).getAdaptorCurve().Curve();
		
		List<Double> intersectionParams = getParamIntersectionPts(geomCurve1, geomCurve2, tol);
		
		intersectionParams.forEach(d -> intersectionPts.add(new double[] {
				geomCurve1.Value(d).X(),
				geomCurve1.Value(d).Y(),
				geomCurve1.Value(d).Z()}));
		
		return intersectionPts;
	}
	
	public static List<double[]> getIntersectionPts(CADGeomCurve3D curve1, CADGeomCurve3D curve2) {
		return getIntersectionPts(curve1, curve2, 1e-5);
	}
	
	// Computes intersection between two curves and returns the intersection parameter on curve1
	private static List<Double> getParamIntersectionPts(Geom_Curve geomCurve1, Geom_Curve geomCurve2, double tol) {
		GeomAPI_ExtremaCurveCurve extrema = new GeomAPI_ExtremaCurveCurve(geomCurve1, geomCurve2);
		int nExtrema = extrema.NbExtrema();
		
		List<Double> pars = new ArrayList<>();		
		if (nExtrema > 0) {		
			for (int i = 1; i <= nExtrema; i++) {	
				double[] par = new double[] {0};
				
				gp_Pnt p1 = new gp_Pnt();
				gp_Pnt p2 = new gp_Pnt();
				
				extrema.Points(i, p1, p2);
				
				if (p1.IsEqual(p2, tol) == 1) {
					extrema.Parameters(1, par, new double[] {0});
					pars.add(par[0]);
				}
			}	
			
		} else {
			System.out.println("OCCUtils::getParamIntersectionPt - "
					+ "Warning: no intersections found, returning empty list.");
		}

		return pars;		
	}
	
	public static OCCShell applyFilletOnShell(OCCShell shell, int[] edgeIndexes, double radius) {
		
		OCCShell filletShell = null;

		BRepFilletAPI_MakeFillet filletMaker = new BRepFilletAPI_MakeFillet(shell.getShape());

		List<CADEdge> shellEdges = new ArrayList<>();
		OCCExplorer exp = new OCCExplorer();
		exp.init(shell, CADShapeTypes.EDGE);
		while (exp.more()) {
			shellEdges.add((CADEdge) exp.current());
			exp.next();
		}

		Arrays.stream(edgeIndexes).forEach(i -> filletMaker.Add(radius, ((OCCEdge) shellEdges.get(i)).getShape()));

		List<TopoDS_Shell> filletShells = new ArrayList<>();
		TopExp_Explorer filletShellExplorer = new TopExp_Explorer(filletMaker.Shape(), TopAbs_ShapeEnum.TopAbs_SHELL);
		while(filletShellExplorer.More() > 0) {
			filletShells.add(TopoDS.ToShell(filletShellExplorer.Current()));
			filletShellExplorer.Next();
		}

		filletShell = (OCCShell) OCCUtils.theFactory.newShape(filletShells.get(0));

		return filletShell;
	}
	
	public static List<CADWire> revolveWireAroundGuideCurve(
			CADWire wire, double[] cgWire, CADGeomCurve3D guideCurve,
			double scaling, int nRevolvedWires) {
		
		List<CADWire> revolvedWires = new ArrayList<>();
		
		guideCurve.discretize(nRevolvedWires);
		
		double[] guideCurveParams = ((OCCGeomCurve3D) guideCurve).getDiscretizedCurve().getParams();
		List<gp_Pnt> guideCurvePts = ((OCCGeomCurve3D) guideCurve).getDiscretizedCurve().getPoints();
		
		double[] scaleFactors = MyArrayUtils.linspace(1.0, scaling, guideCurvePts.size());
		
		revolvedWires.add(wire);
		for (int i = 1; i < guideCurvePts.size(); i++) {
			
			gp_Trsf translate = new gp_Trsf();
			gp_Trsf scale = new gp_Trsf();
			gp_Trsf rotate = new gp_Trsf();
			
			// SCALING		
			scale.SetScale(new gp_Pnt(cgWire[0], cgWire[1], cgWire[2]), scaleFactors[i]);
			
			CADWire scaledWire = (CADWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(
					new BRepBuilderAPI_Transform(
							((OCCWire) wire).getShape(), scale, 0).Shape()));
			
			// ROTATION
			gp_Ax1 rotAxis = new gp_Ax1(
					new gp_Pnt(0.0, 0.0, 0.0), 
					new gp_Dir(new gp_Vec(1.0, 0.0, 0.0)));
			
			gp_Vec guideCrvTang = new gp_Vec();
			((OCCGeomCurve3D) guideCurve).getAdaptorCurve().D1(guideCurveParams[i], new gp_Pnt(), guideCrvTang);
			
			gp_Vec bNormal = new gp_Vec(0.0, 1.0, 0.0);
			gp_Vec iNormal = new gp_Vec(0.0, guideCrvTang.Y(), guideCrvTang.Z());
			
			double rotAngle = bNormal.AngleWithRef(iNormal, new gp_Vec(1.0, 0.0, 0.0));
			
			rotate.SetRotation(rotAxis, rotAngle);
			
			CADWire rotatedWire = (CADWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(
					new BRepBuilderAPI_Transform(
							((OCCWire) scaledWire).getShape(), rotate, 0).Shape()));
			
			// TRANSLATION
			translate.SetTranslation(
					BRep_Tool.Pnt(
							((OCCVertex) rotatedWire.vertices().get(0)).getShape()), 
					guideCurvePts.get(i));
			
			CADWire translatedWire = (CADWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(
					new BRepBuilderAPI_Transform(
							((OCCWire) rotatedWire).getShape(), translate, 0).Shape()));
					
			revolvedWires.add(translatedWire);		
		}
		
		return revolvedWires;
	}
	
	public static double[] getShapeCG(OCCShape shape) {
		
		GProp_GProps gProp = new GProp_GProps();
		BRepGProp.LinearProperties(shape.getShape(), gProp);	
		gp_Pnt cgGpPnt = gProp.CentreOfMass();
		
		return new double[] {cgGpPnt.X(), cgGpPnt.Y(), cgGpPnt.Z()};
	}
	
	public static List<OCCShape> filterAircraftPartSolids(List<OCCShape> shapes) {

		List<OCCShape> solid = new ArrayList<>();

		solid.addAll(shapes.stream()
				.filter(s -> s instanceof OCCSolid)
				.collect(Collectors.toList()));

		return solid;
	}
	
}

