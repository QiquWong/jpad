package it.unina.daf.jpadcad.occ;

import java.util.List;

import opencascade.BRepBuilderAPI_MakeShell;
import opencascade.BRepBuilderAPI_MakeSolid;
import opencascade.BRepGProp;
import opencascade.GProp_GProps;
import opencascade.TopoDS_Shell;

public class OCCSolid extends OCCShape implements CADSolid
{
	private double volume = 0.0;

	public OCCSolid() {
	}	

	public OCCSolid(CADShape patch3) {
		OCCFace occf = (OCCFace)patch3;
		TopoDS_Shell sh = (TopoDS_Shell)((OCCShape)patch3).getShape();
		BRepBuilderAPI_MakeSolid mso = new BRepBuilderAPI_MakeSolid(sh);
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
