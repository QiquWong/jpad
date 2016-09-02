package it.unina.daf.jpadcad.occ;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepOffsetAPI_ThruSections;
import opencascade.TopoDS;

public class OCCShell extends OCCShape implements CADShell
{

	public OCCShell() {
	}	
	
	public OCCShell(List<CADGeomCurve3D> cadGeomCurveList) {
		
		if (cadGeomCurveList.size() > 0) {
			
			BRepOffsetAPI_ThruSections _loft = new BRepOffsetAPI_ThruSections();

			cadGeomCurveList.stream()
				.map(c -> (OCCShape)c.edge())
				.map(_e -> {
					BRepBuilderAPI_MakeWire _wire = new BRepBuilderAPI_MakeWire();
					_wire.Add(TopoDS.ToEdge(_e.getShape()));
					return _wire;
				})
				.forEach(_w -> _loft.AddWire(_w.Wire()));
				;
			
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
					myShape = ((OCCShape)cadFace).getShape();
				} else {
					Logger LOGGER=Logger.getLogger(CADShapeFactory.class.getName());
					LOGGER.log(Level.WARNING, "Class CADShapeFactory: factory object not found.");
					LOGGER.log(Level.WARNING, "Class OCCShell: low-level TopoDS_Shape object set to null.");
					myShape = null;
				}
			} else {
				Logger LOGGER=Logger.getLogger(CADShapeFactory.class.getName());
				LOGGER.log(Level.WARNING, "Class CADShapeFactory: factory object not found.");
				LOGGER.log(Level.WARNING, "Class OCCShell: low-level TopoDS_Shape object set to null.");
				myShape = null;
			}
		}
	}
}
