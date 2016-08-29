package it.unina.daf.jpadcad.occ;

import opencascade.BRepTools_WireExplorer;

public class OCCWireExplorer implements CADWireExplorer
{
	private final BRepTools_WireExplorer occWExp;
	public OCCWireExplorer ()
	{
		occWExp = new BRepTools_WireExplorer();
	}
	
	public void init(CADWire w, CADFace f)
	{
		OCCWire occWire = (OCCWire) w;
		OCCFace occFace = (OCCFace) f;
		occWExp.Init(occWire.getShape(), occFace.getShape());
	}
	
	public boolean more()
	{
		return (occWExp.More() == 1);
	}
	
	public void next()
	{
		occWExp.Next();
	}
	
	public CADEdge current()
	{
		return (CADEdge) CADShapeFactory.getFactory().newShape(occWExp.Current());
	}
}
