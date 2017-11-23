package it.unina.daf.jpadcad.occ;

import opencascade.TopoDS_Iterator;

public class OCCIterator implements CADIterator
{
	private final TopoDS_Iterator occIt;
	public OCCIterator ()
	{
		occIt = new TopoDS_Iterator();
	}
	
	public void init(CADShape s)
	{
		OCCShape shape = (OCCShape) s;
		occIt.Initialize(shape.getShape());
	}
	
	public boolean more()
	{
		return (occIt.More() == 1);
	}
	
	public void next()
	{
		occIt.Next();
	}
	
	public CADShape value()
	{
		return CADShapeFactory.getFactory().newShape(occIt.Value());
	}
}
