package it.unina.daf.jpadcad.occ;

import java.util.Arrays;

import opencascade.BRepBndLib;
import opencascade.BRep_Builder;
import opencascade.Bnd_Box;
import opencascade.TopoDS;
import opencascade.TopoDS_CompSolid;

public class OCCCompSolid extends OCCShape implements CADCompSolid
{
	public OCCCompSolid(CADShape ... cadShapes) {
		TopoDS_CompSolid comp = new TopoDS_CompSolid();
		BRep_Builder builder = new BRep_Builder();
		builder.MakeCompSolid(comp);
		Arrays.asList(cadShapes).stream()
			.filter(s -> s instanceof CADSolid)
			.forEach(s -> builder.Add(comp, ((OCCShape) s).getShape()));	
		myShape = comp;
	}
	
	@Override
	public final TopoDS_CompSolid getShape() {
		return TopoDS.ToCompSolid(myShape);
	}

	@Override
	public double [] boundingBox() {
		Bnd_Box box = new Bnd_Box();
		BRepBndLib.Add(myShape, box);
		double[] ret = new double[6];
		ret[0] = box.CornerMin().X();
		ret[1] = box.CornerMin().Y();
		ret[2] = box.CornerMin().Z();
		ret[3] = box.CornerMax().X();
		ret[4] = box.CornerMax().Y();
		ret[5] = box.CornerMax().Z();
		return ret;
	}
	
	@Override
	public boolean add(CADShape o) {		
		OCCShape s = (OCCShape) o;
		BRep_Builder builder = new BRep_Builder();
		if (s instanceof CADSolid) {
			builder.Add(getShape(), s.getShape());
			return true;
		}	
		return false;
	}

	
}
