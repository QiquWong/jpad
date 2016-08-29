package it.unina.daf.jpadcad.occ;

import opencascade.BRep_Builder;

public class OCCCompound extends OCCShape implements CADCompound
{
	public boolean add(CADShape o)
	{
		OCCShape s = (OCCShape) o;
		new BRep_Builder().Add(myShape, s.myShape);
		return true;
	}
}
