package it.unina.daf.jpadcad.occ;

import opencascade.BRepBndLib;
import opencascade.Bnd_Box;
import opencascade.TopoDS;
import opencascade.TopoDS_CompSolid;

public class OCCCompSolid extends OCCShape implements CADCompSolid
{
	@Override
	public final TopoDS_CompSolid getShape()
	{
		return TopoDS.ToCompSolid(myShape);
	}

	@Override
	public double [] boundingBox()
	{
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

	
}
