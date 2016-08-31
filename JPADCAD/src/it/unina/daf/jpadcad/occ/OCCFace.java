package it.unina.daf.jpadcad.occ;

import opencascade.BRepBndLib;
import opencascade.BRep_Tool;
import opencascade.Bnd_Box;
import opencascade.TopoDS;
import opencascade.TopoDS_Face;

public class OCCFace extends OCCShape implements CADFace
{
	@Override
	public final TopoDS_Face getShape()
	{
		//return (TopoDS_Face) myShape;
		return TopoDS.ToFace(myShape);
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

	public OCCGeomSurface getGeomSurface()
	{
		OCCGeomSurface surface = new OCCGeomSurface();
		surface.setSurface(BRep_Tool.Surface(getShape()));
		return surface;
	}

}
