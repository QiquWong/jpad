package it.unina.daf.jpadcad.occ;

import java.util.Arrays;
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
	private double area = 0.0;

	public OCCShell() {
	}	
	
	public OCCShell(List<CADGeomCurve3D> cadGeomCurveList) {
		myShape = OCCShellImpl(cadGeomCurveList, 0, 0, 1.0e-06);
	}

	public OCCShell(List<CADGeomCurve3D> cadGeomCurveList, long isSolid, long ruled, double pres3d) {
		myShape = OCCShellImpl(cadGeomCurveList, isSolid, ruled, pres3d);
	}

	public OCCShell(List<CADGeomCurve3D> cadGeomCurveList, long isSolid, long ruled) {
		myShape = OCCShellImpl(cadGeomCurveList, isSolid, ruled, 1.0e-06);
	}
	
	private TopoDS_Shape OCCShellImpl(List<CADGeomCurve3D> cadGeomCurveList, long isSolid, long ruled, double pres3d) {
		
		TopoDS_Shape _theShape = null;
		
		if (cadGeomCurveList.size() > 0) {
			
			// NOTE: _<var-name> means a low-level OCCT object
			
			BRepOffsetAPI_ThruSections _loft = new BRepOffsetAPI_ThruSections(isSolid, ruled, pres3d);

			cadGeomCurveList.stream()
				.map(c -> (OCCShape)c.edge())
				.map(_e -> {
					BRepBuilderAPI_MakeWire _wire = new BRepBuilderAPI_MakeWire();
					_wire.Add(TopoDS.ToEdge(_e.getShape()));
					return _wire;
				})
				.forEach(_w -> _loft.AddWire(_w.Wire()));
			
			_loft.Build();
			
			CADShapeFactory factory = CADShapeFactory.getFactory();
			if (factory != null) {
				CADShape cadShape = factory.newShape(_loft.Shape());
				
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
					_theShape = ((OCCShape)cadFace).getShape();

					// area of the face
					GProp_GProps property = new GProp_GProps();
					BRepGProp.SurfaceProperties(_theShape, property);
					area = property.Mass();
					//System.out.println("OCCShell :: Surface area = " + area);

				} else {
					Logger LOGGER=Logger.getLogger(CADShapeFactory.class.getName());
					LOGGER.log(Level.WARNING, "Class CADShapeFactory: factory object not found.");
					LOGGER.log(Level.WARNING, "Class OCCShell: low-level TopoDS_Shape object set to null.");
					_theShape = null;
				}
			} else {
				Logger LOGGER=Logger.getLogger(CADShapeFactory.class.getName());
				LOGGER.log(Level.WARNING, "Class CADShapeFactory: factory object not found.");
				LOGGER.log(Level.WARNING, "Class OCCShell: low-level TopoDS_Shape object set to null.");
				_theShape = null;
			}
		}
		return _theShape;
	}

	@Override
	public double getArea() {
		return area;
	}
}
