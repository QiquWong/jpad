package it.unina.daf.jpadcad.occ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import opencascade.BRepAlgoAPI_BooleanOperation;
import opencascade.BRepAlgoAPI_Common;
import opencascade.BRepAlgoAPI_Cut;
import opencascade.BRepAlgoAPI_Fuse;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_MakeSolid;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepBuilderAPI_Sewing;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.BRep_Tool;
import opencascade.IGESControl_Reader;
import opencascade.STEPControl_Reader;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp;
import opencascade.TopExp_Explorer;
import opencascade.TopTools_IndexedMapOfShape;
import opencascade.TopoDS;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Shell;
import opencascade.TopoDS_Wire;
import opencascade.gp_Pnt;
import processing.core.PVector;

/*
 * Note: this class is used only by reflection, see CADShapeFactory#factory
 * initialization.
 */
public class OCCShapeFactory extends CADShapeFactory
{
	private static final Logger logger = Logger.getLogger(OCCShapeFactory.class.getName());
	
	@Override
	public final CADShape newShape(Object o)
	{
		if (!(o instanceof TopoDS_Shape))
			throw new IllegalArgumentException();

		TopoDS_Shape ts = (TopoDS_Shape) o;
		OCCShape shape;
		switch (ts.ShapeType())
		{
			case TopAbs_COMPOUND:
				shape = new OCCCompound();
				break;
			case TopAbs_SOLID:
				shape = new OCCSolid();
				break;
			case TopAbs_SHELL:
				shape = new OCCShell();
				break;
			case TopAbs_FACE:
				shape = new OCCFace();
				break;
			case TopAbs_WIRE:
				shape = new OCCWire();
				break;
			case TopAbs_EDGE:
				shape = new OCCEdge();
				break;
			case TopAbs_VERTEX:
				shape = new OCCVertex();
				break;
			default:
				shape = new OCCShape();
				break;
		}
		shape.setShape(ts);
		return shape;
	}
	
	@Override
	public CADShape newShape (String fileName)
	{
		TopoDS_Shape brepShape;
		if (fileName.endsWith(".step") || fileName.endsWith(".stp"))
		{
			logger.fine("Read STEP file: " + fileName);
			STEPControl_Reader aReader = new STEPControl_Reader();
			aReader.ReadFile(fileName);
			logger.fine("Transfer roots into shape...");
			aReader.NbRootsForTransfer();
			aReader.TransferRoots();
			brepShape = aReader.OneShape();
			logger.fine("... done");
		}
		else if (fileName.endsWith(".igs"))
		{
			logger.fine("Read IGES file: " + fileName);
			IGESControl_Reader aReader = new IGESControl_Reader();
			aReader.ReadFile(fileName);
			logger.fine("Transfer roots into shape...");
			aReader.NbRootsForTransfer();
			aReader.TransferRoots();
			brepShape = aReader.OneShape();
			logger.fine("... done");
		}
		else
		{
			logger.fine("Read BREP file: " + fileName);
			TopoDS_Shape shape = new TopoDS_Shape();
			BRepTools.Read(shape, fileName, new BRep_Builder());
			brepShape = shape;
			logger.fine("... done");
		}
		return newShape(brepShape);
	}
	
	/**
	 * @param type 'u' = fuse, 'n' = common, '\\' = cut
	 */
	@Override
	public CADShape newShape(CADShape o1, CADShape o2, char type)
	{
		CADShape res = null;
		TopoDS_Shape s1 = ((OCCShape) o1).getShape();
		TopoDS_Shape s2 = ((OCCShape) o2).getShape();

		BRepAlgoAPI_BooleanOperation op = null;
		try
		{
			if (type == 'u')
				op = new BRepAlgoAPI_Fuse(s1, s2);
			else if (type == 'n')
				op = new BRepAlgoAPI_Common(s1, s2);
			else if (type == '\\')
				op = new BRepAlgoAPI_Cut(s1, s2);
			else
				throw new IllegalArgumentException();
			TopoDS_Shape s = op.Shape();
			if (s != null)
				res = newShape(s);
		}
		catch (RuntimeException ex)
		{
		}
		return res;
	}
	
	@Override
	public CADExplorer newExplorer()
	{
		return new OCCExplorer();
	}
	
	@Override
	public CADWireExplorer newWireExplorer()
	{
		return new OCCWireExplorer();
	}

	@Override
	public CADIterator newIterator()
	{
		return new OCCIterator();
	}
	
	@Override
	protected Iterator<CADShapeTypes> newShapeEnumIterator(CADShapeTypes start, CADShapeTypes end)
	{
		return OCCShapeTypes.newShapeEnumIterator((OCCShapeTypes) start, (OCCShapeTypes) end);
	}

	@Override
	protected CADShapeTypes getShapeEnumInstance(String name)
	{
		return OCCShapeTypes.getSingleton(name);
	}

	@Override
	public CADGeomCurve2D newCurve2D(CADEdge E, CADFace F)
	{
		CADGeomCurve2D curve = null;
		try
		{
			curve = new OCCGeomCurve2D(E, F);
		}
		catch (RuntimeException ex)
		{
		}
		return curve;
	}
	
	@Override
	public CADGeomCurve3D newCurve3D(CADEdge E)
	{
		CADGeomCurve3D curve = null;
		try
		{
			curve = new OCCGeomCurve3D(E);
		}
		catch (RuntimeException ex)
		{
		}
		return curve;
	}

	@Override
	public CADGeomCurve3D newCurve3D(boolean isPeriodic, double[] ... pts) {
		CADGeomCurve3D curve = null;
		try
		{
			List<double[]> pointList = new ArrayList<double[]>();			
			for (double[] p : pts) {
				pointList.add(p);
			}
			if (pointList.size() > 1) {			
				curve = new OCCGeomCurve3D(pointList, isPeriodic); // can be periodic
			}
		}
		catch (RuntimeException ex)
		{
		}
		return curve;
	}

	@Override
	public CADGeomCurve3D newCurve3D(double[] pt1, double[] pt2) {
		CADGeomCurve3D curve = null;
		try
		{
			List<double[]> pointList = new ArrayList<double[]>();
			pointList.add(pt1);
			pointList.add(pt2);
			curve = new OCCGeomCurve3D(pointList, false); // a regular segment is non periodic
		}
		catch (RuntimeException ex)
		{
		}
		return curve;
	}
	
	@Override
	public CADGeomCurve3D newCurve3D(List<double[]> pointList, boolean isPeriodic) {
		CADGeomCurve3D curve = null;
		try
		{
			curve = new OCCGeomCurve3D(pointList, isPeriodic);
		}
		catch (RuntimeException ex)
		{
		}
		return curve;
	}

	@Override
	public CADGeomCurve3D newCurve3D(List<double[]> pointList, boolean isPeriodic, double[] initialTangent, double[] finalTangent, boolean doScale) {
		CADGeomCurve3D curve = null;
		try
		{
			curve = new OCCGeomCurve3D(pointList, isPeriodic, initialTangent, finalTangent, doScale);
		}
		catch (RuntimeException ex)
		{
		}
		return curve;
	}
	
	@Override
	public CADGeomCurve3D newCurve3DGP(List<gp_Pnt> pointList, boolean isPeriodic) {
		CADGeomCurve3D curve = null;
		try
		{
			curve = new OCCGeomCurve3D(
					pointList.stream()
							 .map(p -> new double[]{p.X(), p.Y(), p.Z()})
							 .collect(Collectors.toList()),
					isPeriodic);
		}
		catch (RuntimeException ex)
		{
		}
		return curve;		
	}
	
	@Override
	public CADGeomCurve3D newCurve3DP(List<PVector> pointList, boolean isPeriodic) {
		CADGeomCurve3D curve = null;
		try
		{
			curve = new OCCGeomCurve3D(
					pointList.stream()
							 .map(p -> new double[]{p.x, p.y, p.z})
							 .collect(Collectors.toList()),
					isPeriodic);
		}
		catch (RuntimeException ex)
		{
		}
		return curve;
	}
	
	@Override
	public CADShell newShell(List<CADWire> cadWireList) {
		CADShell shell = null;
		try
		{
			shell = new OCCShell(cadWireList); // defaults: isSolid = 0, ruled = 0, pres3d = 1.0e-06
		}
		catch (RuntimeException ex)
		{
		}
		
		return shell;
	}

	@Override
	public CADShell newShell(List<CADWire> cadWireList, long isSolid, long ruled, double pres3d) {
		CADShell shell = null;
		try
		{
			shell = new OCCShell(cadWireList, isSolid, ruled, pres3d);
		}
		catch (RuntimeException ex)
		{
		}
		
		return shell;
	}
	
	@Override
	public CADShell newShell(List<CADWire> cadWireList, long isSolid, long ruled) {
		CADShell shell = null;
		try
		{
			shell = new OCCShell(cadWireList, isSolid, ruled);
		}
		catch (RuntimeException ex)
		{
		}
		
		return shell;
	}
	
	@Override
	public CADShell newShell(CADVertex v0, List<CADWire> cadWireList, CADVertex v1) {
		CADShell shell = null;
		try
		{
			shell = new OCCShell(v0, cadWireList, v1); // defaults: isSolid = 0, ruled = 0, pres3d = 1.0e-06
		}
		catch (RuntimeException ex)
		{
		}
		
		return shell;
	}
	
	@Override
	public CADShell newShell(CADVertex v0, List<CADWire> cadWireList, CADVertex v1, long isSolid, long ruled, double pres3d) {
		CADShell shell = null;
		try
		{
			shell = new OCCShell(v0, cadWireList, v1, isSolid, ruled, pres3d);
		}
		catch (RuntimeException ex)
		{
		}
		
		return shell;
	}
	
	@Override
	public CADShell newShell(CADVertex v0, List<CADWire> cadWireList, CADVertex v1, long isSolid, long ruled) {
		CADShell shell = null;
		try
		{
			shell = new OCCShell(v0, cadWireList, v1, isSolid, ruled);
		}
		catch (RuntimeException ex)
		{
		}
		
		return shell;
	}

	@Override
	public CADFace newFacePlanar(double[] v0, double[] v1, double[] v2) {
		BRepBuilderAPI_MakeEdge em0 = new BRepBuilderAPI_MakeEdge(
				 new gp_Pnt(v0[0], v0[1], v0[2]),
				 new gp_Pnt(v1[0], v1[1], v1[2])
		);
		em0.Build();
		BRepBuilderAPI_MakeEdge em1 = new BRepBuilderAPI_MakeEdge(
				 new gp_Pnt(v1[0], v1[1], v1[2]),
				 new gp_Pnt(v2[0], v2[1], v2[2])
		);
		em1.Build();
		BRepBuilderAPI_MakeEdge em2 = new BRepBuilderAPI_MakeEdge(
				 new gp_Pnt(v2[0], v2[1], v2[2]),
				 new gp_Pnt(v0[0], v0[1], v0[2])
		);
		em2.Build();
		BRepBuilderAPI_MakeWire wm = new BRepBuilderAPI_MakeWire();
		wm.Add(em0.Edge());
		wm.Add(em1.Edge());
		wm.Add(em2.Edge());
		wm.Build();
		BRepBuilderAPI_MakeFace mf = new BRepBuilderAPI_MakeFace(wm.Wire(), 1);
		mf.Build();
		TopoDS_Face tds_f = mf.Face();

		return (CADFace) newShape(tds_f);
	}
	
	@Override
	public CADFace newFacePlanar(CADVertex v0, CADVertex v1, CADVertex v2) {
		return newFacePlanar(v0.pnt(), v1.pnt(), v2.pnt());		
	}
	
	@Override
	public CADFace newFacePlanar(CADWire wire) {
		TopoDS_Wire tdsWire = TopoDS.ToWire(((OCCShape) wire).getShape());
		
		BRepBuilderAPI_MakeFace faceMaker = new BRepBuilderAPI_MakeFace(tdsWire, 1);
		
		return (CADFace) newShape(faceMaker.Face());
	}
	
	@Override
	public CADShell newShellFromAdjacentFaces(CADFace ... cadFaces) {
		return newShellFromAdjacentFaces(Arrays.asList(cadFaces));
	}

	@Override
	public CADShell newShellFromAdjacentFaces(List<CADFace> cadFaces) {
		CADShell ret = null;
		BRepBuilderAPI_Sewing sewedObj = new BRepBuilderAPI_Sewing();
		cadFaces.stream()
			.forEach(f -> sewedObj.Add(((OCCFace)f).getShape()));
		sewedObj.Perform();
		TopoDS_Shape tds_shape = sewedObj.SewedShape();		

		TopExp_Explorer exp = new TopExp_Explorer(tds_shape, TopAbs_ShapeEnum.TopAbs_SHELL);
		if (exp.More() == 1) {
			System.out.println("========== [OCCShapeFactory::newShellFromAdjacentFaces] exploring shells in sewed object ...");
			ret = (CADShell) newShape(exp.Current());
		}
		return ret;
	}
	
	@Override
	public CADShell newShellFromAdjacentShells(CADShell ... cadShells) {
		return newShellFromAdjacentShells(Arrays.asList(cadShells));
	}
	
	@Override
	public CADShell newShellFromAdjacentShells(List<CADShell> cadShells) {
		CADShell ret = null;
		BRepBuilderAPI_Sewing sewer = new BRepBuilderAPI_Sewing();
		cadShells.forEach(s -> sewer.Add(((OCCShell) s).getShape()));
		sewer.Perform();		
		TopoDS_Shape sewedShape = sewer.SewedShape();
		
		TopExp_Explorer exp = new TopExp_Explorer(sewedShape, TopAbs_ShapeEnum.TopAbs_SHELL);
		if (exp.More() == 1) {
			ret = (CADShell) newShape(exp.Current());
		}	
		return ret;
	}
	
	@Override
	public CADSolid newSolidFromAdjacentFaces(CADFace ... cadFaces) {
		return newSolidFromAdjacentFaces(Arrays.asList(cadFaces));
	}
	
	@Override
	public CADSolid newSolidFromAdjacentFaces(List<CADFace> cadFaces) {
		CADSolid ret = null;
		CADShell shell = newShellFromAdjacentFaces(cadFaces);
		TopoDS_Shape tds_shape = ((OCCShell)shell).getShape();
		TopExp_Explorer exp = new TopExp_Explorer(tds_shape, TopAbs_ShapeEnum.TopAbs_SHELL);
		if (exp.More() == 1) {
			System.out.println("========== [OCCShapeFactory::newSolidFromAdjacentFaces] from shell to solid ...");
			BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
			solidMaker.Add(TopoDS.ToShell(exp.Current()));
			solidMaker.Build();
			ret = (CADSolid) newShape(solidMaker.Solid());
		}
		return ret;
	}
	
	@Override
	public CADSolid newSolidFromAdjacentShells(CADShell ... cadShells) {
		return newSolidFromAdjacentShells(Arrays.asList(cadShells));
	}
	
	@Override
	public CADSolid newSolidFromAdjacentShells(List<CADShell> cadShells) {
		CADSolid ret = null;
		BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
		
		for (int i = 0; i < cadShells.size(); i++) {
			solidMaker.Add(TopoDS.ToShell(((OCCShell) cadShells.get(i)).getShape()));
		}
		solidMaker.Build();
		ret = (CADSolid) newShape(solidMaker.Solid());
		
		return ret;
	}
	
	@Override
	public CADWire newWireFromAdjacentEdges(CADEdge ... cadEdges) {
		return new OCCWire(Arrays.asList(cadEdges));
	}
	
	@Override
	public CADWire newWireFromAdjacentEdges(List<CADEdge> cadEdges) {
		return new OCCWire(cadEdges);
	}

	@Override
	public CADVertex newVertex(double x, double y, double z) {
		CADVertex vertex = null;
		try
		{
			vertex = new OCCVertex(x, y, z);
		}
		catch (RuntimeException ex)
		{
		}
		
		return vertex;
	}

	@Override
	public CADVertex newVertex(double[] coordinates3d) {
		return newVertex(coordinates3d[0], coordinates3d[1], coordinates3d[1]);
	}

}
