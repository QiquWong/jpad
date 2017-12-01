package it.unina.daf.jpadcad.occ;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepGProp;
import opencascade.BRepOffsetAPI_ThruSections;
import opencascade.GProp_GProps;
import opencascade.TopoDS;
import opencascade.TopoDS_Shape;

public class OCCShell extends OCCShape implements CADShell
{
	private static long defaultMakeSolid = 0;
	private static long defaultMakeRuled = 0;
	private static double defaultPrec3D = 1.0e-06;
	
	public static boolean isDefaultMakeSolid() {
		return (defaultMakeSolid == 1);
	}

	public static void setDefaultMakeSolid(boolean value) {
		OCCShell.defaultMakeSolid = (value) ? 1 : 0;
	}

	public static boolean isDefaultMakeRuled() {
		return (defaultMakeRuled == 1);
	}

	public static void setDefaultMakeRuled(boolean value) {
		OCCShell.defaultMakeRuled = (value) ? 1 : 0;
	}

	public static double getDefaultPrec3D() {
		return defaultPrec3D;
	}

	public static void setDefaultPrec3D(double defaultPrec3D) {
		OCCShell.defaultPrec3D = defaultPrec3D;
	}

	private double area = 0.0;

	public OCCShell() {
	}	
	
	public OCCShell(List<CADGeomCurve3D> cadGeomCurveList) {
		myShape = OCCShellImpl(null, cadGeomCurveList, null, defaultMakeSolid, defaultMakeRuled, defaultPrec3D);
	}

	public OCCShell(List<CADGeomCurve3D> cadGeomCurveList, long solid, long ruled, double pres3d) {
		myShape = OCCShellImpl(null, cadGeomCurveList, null, solid, ruled, pres3d);
	}

	public OCCShell(List<CADGeomCurve3D> cadGeomCurveList, long solid, long ruled) {
		myShape = OCCShellImpl(null, cadGeomCurveList, null, solid, ruled, defaultPrec3D);
	}

	// passing the initial vertex
	public OCCShell(OCCVertex v0, List<CADGeomCurve3D> cadGeomCurveList) {
		myShape = OCCShellImpl(v0, cadGeomCurveList, null, defaultMakeSolid, defaultMakeRuled, defaultPrec3D);
	}

	public OCCShell(OCCVertex v0, List<CADGeomCurve3D> cadGeomCurveList, long solid, long ruled) {
		myShape = OCCShellImpl(v0, cadGeomCurveList, null, solid, ruled, defaultPrec3D);
	}

	public OCCShell(OCCVertex v0, List<CADGeomCurve3D> cadGeomCurveList, long solid, long ruled, double pres3d) {
		myShape = OCCShellImpl(v0, cadGeomCurveList, null, solid, ruled, pres3d);
	}

	// passing the final vertex
	public OCCShell(List<CADGeomCurve3D> cadGeomCurveList, OCCVertex v1) {
		myShape = OCCShellImpl(null, cadGeomCurveList, v1, defaultMakeSolid, defaultMakeRuled, defaultPrec3D);
	}

	public OCCShell(List<CADGeomCurve3D> cadGeomCurveList, OCCVertex v1, long solid, long ruled) {
		myShape = OCCShellImpl(null, cadGeomCurveList, v1, solid, ruled, defaultPrec3D);
	}

	public OCCShell(List<CADGeomCurve3D> cadGeomCurveList, OCCVertex v1, long solid, long ruled, double pres3d) {
		myShape = OCCShellImpl(null, cadGeomCurveList, v1, solid, ruled, pres3d);
	}
	
	// passing the initial and final vertices
	public OCCShell(OCCVertex v0, List<CADGeomCurve3D> cadGeomCurveList, OCCVertex v1) {
		myShape = OCCShellImpl(v0, cadGeomCurveList, v1, defaultMakeSolid, defaultMakeRuled, defaultPrec3D);
	}

	public OCCShell(OCCVertex v0, List<CADGeomCurve3D> cadGeomCurveList, OCCVertex v1, long solid, long ruled) {
		myShape = OCCShellImpl(v0, cadGeomCurveList, v1, solid, ruled, defaultPrec3D);
	}

	public OCCShell(OCCVertex v0, List<CADGeomCurve3D> cadGeomCurveList, OCCVertex v1, long solid, long ruled, double pres3d) {
		myShape = OCCShellImpl(v0, cadGeomCurveList, v1, solid, ruled, pres3d);
	}

	// accepts a first and final vertex
	// https://www.opencascade.com/doc/occt-7.0.0/refman/html/class_b_rep_offset_a_p_i___thru_sections.html
	private TopoDS_Shape OCCShellImpl(
			OCCVertex v0, List<CADGeomCurve3D> cadGeomCurveList, OCCVertex v1,
			long solid, long ruled, double pres3d) {
		
		TopoDS_Shape aShape = null;
		
		if (cadGeomCurveList.size() > 0) {
			
			// NOTE: _<var-name> means a low-level OCCT object
			
			BRepOffsetAPI_ThruSections loft = new BRepOffsetAPI_ThruSections(solid, ruled, pres3d);
			
			if (v0 != null)
				loft.AddVertex(v0.getShape());

			cadGeomCurveList.stream()
				.map(c -> (OCCShape)c.edge())
				.map(e -> {
					BRepBuilderAPI_MakeWire wire = new BRepBuilderAPI_MakeWire();
					wire.Add(TopoDS.ToEdge(e.getShape()));
					return wire;
				})
				.forEach(w -> loft.AddWire(w.Wire()));

			if (v1 != null)
				loft.AddVertex(v1.getShape());
			
			loft.Build();
			
			CADShapeFactory factory = CADShapeFactory.getFactory();
			if (factory != null) {
				CADShape cadShape = factory.newShape(loft.Shape());
				
				//-----------------------------------------------------------
				// Explore the shell in search of faces, 
				// set first face to myShape

				// count
				CADExplorer expF = CADShapeFactory.getFactory().newExplorer();
				int faces = 0;
				for (expF.init(cadShape, CADShapeTypes.FACE); expF.more(); expF.next())
					faces++;	

				// System.out.println("Face count in cadShape: " + faces);
				
				if (faces > 0) {
					expF.init(cadShape, CADShapeTypes.FACE);
					CADFace cadFace = (CADFace) expF.current();
					aShape = ((OCCShape)cadFace).getShape();

					// area of the face
					GProp_GProps property = new GProp_GProps();
					BRepGProp.SurfaceProperties(aShape, property);
					area = property.Mass();
					//System.out.println("OCCShell :: Surface area = " + area);

				} else {
					Logger LOGGER=Logger.getLogger(CADShapeFactory.class.getName());
					LOGGER.log(Level.WARNING, "Class CADShapeFactory: factory object not found.");
					LOGGER.log(Level.WARNING, "Class OCCShell: low-level TopoDS_Shape object set to null.");
					aShape = null;
				}
			} else {
				Logger LOGGER=Logger.getLogger(CADShapeFactory.class.getName());
				LOGGER.log(Level.WARNING, "Class CADShapeFactory: factory object not found.");
				LOGGER.log(Level.WARNING, "Class OCCShell: low-level TopoDS_Shape object set to null.");
				aShape = null;
			}
		}
		return aShape;
	}
	
	@Override
	public double getArea() {
		return area;
	}
}
