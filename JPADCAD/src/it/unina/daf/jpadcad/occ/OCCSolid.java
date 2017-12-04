package it.unina.daf.jpadcad.occ;

import java.util.List;

import opencascade.BRepBuilderAPI_MakeShell;
import opencascade.BRepBuilderAPI_MakeSolid;
import opencascade.BRepGProp;
import opencascade.GProp_GProps;
import opencascade.Geom_Surface;
import opencascade.TopoDS_Shell;

public class OCCSolid extends OCCShape implements CADSolid
{
	private double volume = 0.0;

	public OCCSolid() {
	}	

	public OCCSolid(CADShape shell) {
		OCCFace occf = new OCCFace();
		occf.setShape(((OCCShape)shell).getShape());
		BRepBuilderAPI_MakeShell msh = new BRepBuilderAPI_MakeShell(
				(Geom_Surface)occf.getGeomSurface().getSurface());
		BRepBuilderAPI_MakeSolid mso = new BRepBuilderAPI_MakeSolid(
				msh.Shell());
		myShape = mso.Shape();
		
		// volume of the solid
		GProp_GProps property = new GProp_GProps();
		BRepGProp.SurfaceProperties(myShape, property);
		volume = property.Mass();
		//System.out.println("OCCSolid :: Solid volume = " + volume);
		
	}
	
	@Override
	public double getVolume() {
		return volume;
	}
}

// see  https://www.opencascade.com/doc/occt-7.0.0/refman/html/class_b_rep_offset_a_p_i___thru_sections.html
