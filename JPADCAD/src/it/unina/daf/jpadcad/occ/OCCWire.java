package it.unina.daf.jpadcad.occ;

import opencascade.BRepGProp;
import opencascade.GProp_GProps;
import opencascade.TopoDS_Wire;

public class OCCWire extends OCCShape implements CADWire
{
	@Override
	public final TopoDS_Wire getShape()
	{
		return (TopoDS_Wire) myShape;
	}

	public double length()
	{
		GProp_GProps myProps = new GProp_GProps();
		BRepGProp.LinearProperties (myShape, myProps);
		return myProps.Mass();
	}
}
