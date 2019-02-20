package it.unina.daf.jpadcad.occ;

import java.util.Arrays;

import opencascade.BRep_Builder;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;

public class OCCCompound extends OCCShape implements CADCompound {
	
	public OCCCompound(CADShape ... cadShapes) {
		TopoDS_Compound comp = new TopoDS_Compound();
		BRep_Builder builder = new BRep_Builder();
		builder.MakeCompound(comp);
		Arrays.asList(cadShapes).forEach(s -> builder.Add(comp, ((OCCShape) s).getShape()));		
		myShape = comp;
	}
	
	@Override
	public final TopoDS_Compound getShape() {		
		return TopoDS.ToCompound(myShape);
	}
	
	public boolean add(CADShape o) {
		OCCShape s = (OCCShape) o;
		BRep_Builder builder = new BRep_Builder();
		builder.Add(getShape(), s.getShape());
		return true;
	}
}
