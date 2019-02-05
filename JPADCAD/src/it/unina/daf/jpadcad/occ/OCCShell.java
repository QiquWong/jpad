package it.unina.daf.jpadcad.occ;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepBuilderAPI_Sewing;
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

	public OCCShell(List<CADWire> cadWireList) {
		myShape = OCCShellThruSections(null, cadWireList, null, defaultMakeSolid, defaultMakeRuled, defaultPrec3D);
	}

	public OCCShell(List<CADWire> cadWireList, long solid, long ruled, double pres3d) {
		myShape = OCCShellThruSections(null, cadWireList, null, solid, ruled, pres3d);
	}

	public OCCShell(List<CADWire> cadWireList, long solid, long ruled) {
		myShape = OCCShellThruSections(null, cadWireList, null, solid, ruled, defaultPrec3D);
	}

	// passing the initial vertex
	public OCCShell(CADVertex v0, List<CADWire> cadWireList) {
		myShape = OCCShellThruSections(v0, cadWireList, null, defaultMakeSolid, defaultMakeRuled, defaultPrec3D);
	}

	public OCCShell(CADVertex v0, List<CADWire> cadWireList, long solid, long ruled) {
		myShape = OCCShellThruSections(v0, cadWireList, null, solid, ruled, defaultPrec3D);
	}

	public OCCShell(CADVertex v0, List<CADWire> cadWireList, long solid, long ruled, double pres3d) {
		myShape = OCCShellThruSections(v0, cadWireList, null, solid, ruled, pres3d);
	}

	// passing the final vertex
	public OCCShell(List<CADWire> cadWireList, CADVertex v1) {
		myShape = OCCShellThruSections(null, cadWireList, v1, defaultMakeSolid, defaultMakeRuled, defaultPrec3D);
	}

	public OCCShell(List<CADWire> cadWireList, CADVertex v1, long solid, long ruled) {
		myShape = OCCShellThruSections(null, cadWireList, v1, solid, ruled, defaultPrec3D);
	}

	public OCCShell(List<CADWire> cadWireList, CADVertex v1, long solid, long ruled, double pres3d) {
		myShape = OCCShellThruSections(null, cadWireList, v1, solid, ruled, pres3d);
	}
	
	// passing the initial and final vertices
	public OCCShell(CADVertex v0, List<CADWire> cadWireList, CADVertex v1) {
		myShape = OCCShellThruSections(v0, cadWireList, v1, defaultMakeSolid, defaultMakeRuled, defaultPrec3D);
	}

	public OCCShell(CADVertex v0, List<CADWire> cadWireList, CADVertex v1, long solid, long ruled) {
		myShape = OCCShellThruSections(v0, cadWireList, v1, solid, ruled, defaultPrec3D);
	}

	public OCCShell(CADVertex v0, List<CADWire> cadWireList, CADVertex v1, long solid, long ruled, double pres3d) {
		myShape = OCCShellThruSections(v0, cadWireList, v1, solid, ruled, pres3d);
	}

	// accepts a first and final vertex
	// https://www.opencascade.com/doc/occt-7.0.0/refman/html/class_b_rep_offset_a_p_i___thru_sections.html
//	private TopoDS_Shape OCCShellThruSections0(
//			OCCVertex v0, List<CADGeomCurve3D> cadGeomCurveList, OCCVertex v1,
//			long solid, long ruled, double pres3d) {
//		
//		TopoDS_Shape aShape = null;
//		
//		if (cadGeomCurveList.size() > 0) {
//			
//			// NOTE: _<var-name> means a low-level OCCT object
//			
//			BRepOffsetAPI_ThruSections loft = new BRepOffsetAPI_ThruSections(solid, ruled, pres3d);
//			
//			if (v0 != null)
//				loft.AddVertex(v0.getShape());
//
//			cadGeomCurveList.stream()
//				.map(c -> (OCCShape)c.edge())
//				.map(e -> {
//					BRepBuilderAPI_MakeWire wire = new BRepBuilderAPI_MakeWire();
//					wire.Add(TopoDS.ToEdge(e.getShape()));
//					return wire;
//				})
//				.forEach(w -> loft.AddWire(w.Wire()));
//
//			if (v1 != null)
//				loft.AddVertex(v1.getShape());
//			
//			loft.Build();
//			
//			CADShapeFactory factory = CADShapeFactory.getFactory();
//			if (factory != null) {
//				CADShape cadShape = factory.newShape(loft.Shape());
//				
//				//-----------------------------------------------------------
//				// Explore the shell in search of faces, 
//				// set first face to myShape
//
//				// count
//				CADExplorer expF = CADShapeFactory.getFactory().newExplorer();
//				int faces = 0;
//				for (expF.init(cadShape, CADShapeTypes.FACE); expF.more(); expF.next())
//					faces++;	
//
//				// System.out.println("Face count in cadShape: " + faces);
//				
//				if (faces > 0) {
//					expF.init(cadShape, CADShapeTypes.FACE);
//					CADFace cadFace = (CADFace) expF.current();
//					aShape = ((OCCShape)cadFace).getShape();
//
//					// area of the face
//					GProp_GProps property = new GProp_GProps();
//					BRepGProp.SurfaceProperties(aShape, property);
//					area = property.Mass();
//					//System.out.println("OCCShell :: Surface area = " + area);
//
//				} else {
//					Logger LOGGER=Logger.getLogger(CADShapeFactory.class.getName());
//					LOGGER.log(Level.WARNING, "Class CADShapeFactory: factory object not found.");
//					LOGGER.log(Level.WARNING, "Class OCCShell: low-level TopoDS_Shape object set to null.");
//					aShape = null;
//				}
//			} else {
//				Logger LOGGER=Logger.getLogger(CADShapeFactory.class.getName());
//				LOGGER.log(Level.WARNING, "Class CADShapeFactory: factory object not found.");
//				LOGGER.log(Level.WARNING, "Class OCCShell: low-level TopoDS_Shape object set to null.");
//				aShape = null;
//			}
//		}
//		return aShape;
//	}
	
	private TopoDS_Shape OCCShellThruSections(
			CADVertex v0, List<CADWire> cadWireList, CADVertex v1, 
			long solid, long ruled, double pres3d) {
		
		TopoDS_Shape aShape = null;
		
		if (cadWireList.size() > 0) {
			
			BRepOffsetAPI_ThruSections loft = new BRepOffsetAPI_ThruSections(solid, ruled, pres3d);
			loft.CheckCompatibility(1);
			
			if (v0 != null)
				loft.AddVertex(((OCCVertex) v0).getShape());
			
			cadWireList.forEach(w -> loft.AddWire(((OCCWire) w).getShape()));
			
			if (v1 != null)
				loft.AddVertex(((OCCVertex) v1).getShape());
			
			loft.Build(); // Not strictly necessary!
			
			CADShapeFactory factory = CADShapeFactory.getFactory();
			if (factory != null) {
				CADShape cadShape = factory.newShape(loft.Shape()); 
				
				//----------------------------------------
				// Explore the shell in search of faces 
				//----------------------------------------

				// Count
				CADExplorer faceExp = factory.newExplorer();
				int nFaces = 0;
				
				List<CADFace> faces = new ArrayList<>();
				for (faceExp.init(cadShape, CADShapeTypes.FACE); faceExp.more(); faceExp.next()) {
					faces.add((CADFace) faceExp.current());
					nFaces++;
				}
									
				if (nFaces > 0) {
					
					if (nFaces == 1) 
						
						aShape = ((OCCShape) faces.get(0)).getShape();
					
					else {
						// Initialize the sew maker
						BRepBuilderAPI_Sewing sewMaker = new BRepBuilderAPI_Sewing();
						
						faces.stream()
							 .map(f -> ((OCCShape) f).getShape())
							 .forEach(sewMaker::Add);
						
						sewMaker.Perform();
						TopoDS_Shape sewedShape = sewMaker.SewedShape();
						
						// Explore the sewed object in search of shell elements and get the  
						// first occurrence, in case of multiple shells launch a warning message.
						CADExplorer shellExp = factory.newExplorer();
						int nShells = 0;
						
						List<CADShell> shells = new ArrayList<>();			
						for (shellExp.init(factory.newShape(sewedShape), CADShapeTypes.SHELL); 
							 shellExp.more(); 
							 shellExp.next()) {
							
							shells.add((CADShell) shellExp.current());
							nShells++;
						}
						
						if (nShells == 1) {
							
							aShape = ((OCCShape) shells.get(0)).getShape();
							
						} else {
							Logger LOGGER = Logger.getLogger(CADShapeFactory.class.getName());
							LOGGER.log(Level.WARNING, "Class CADShapeFactory: factory object not found.");
							LOGGER.log(Level.WARNING, "Class OCCShell: low-level TopoDS_Shape object set to null.");
							aShape = null;						
						}
						
					}

					// Calculate the area of the just generated face/shell element
					GProp_GProps property = new GProp_GProps();
					BRepGProp.SurfaceProperties(aShape, property);
					area = property.Mass();

				} else {
					Logger LOGGER = Logger.getLogger(CADShapeFactory.class.getName());
					LOGGER.log(Level.WARNING, "Class CADShapeFactory: factory object not found.");
					LOGGER.log(Level.WARNING, "Class OCCShell: low-level TopoDS_Shape object set to null.");
					aShape = null;
				}
			}
			
		} else {
			Logger LOGGER = Logger.getLogger(CADShapeFactory.class.getName());
			LOGGER.log(Level.WARNING, "Class CADShapeFactory: factory object not found.");
			LOGGER.log(Level.WARNING, "Class OCCShell: low-level TopoDS_Shape object set to null.");
			
			aShape = null;
		}
		
		return aShape;	
	}
	
	@Override
	public double getArea() {
		return area;
	}
}
