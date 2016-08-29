package it.unina.daf.jpadcad.occ;

import java.util.ArrayList;
import java.util.List;

import opencascade.TopExp_Explorer;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Iterator;
import opencascade.TopoDS_Shape;

public class OCCExplorer implements CADExplorer
{
	private final TopExp_Explorer occExp;
	private List<TopoDS_Shape> compounds;
	private int compId;
	
	public OCCExplorer ()
	{
		occExp = new TopExp_Explorer();
	}
	
	public void init(CADShape s, CADShapeTypes t)
	{
		OCCShape shape = (OCCShape) s;
		if(t == CADShapeTypes.COMPOUND)
		{
			compId = 0;
			compounds = new ArrayList<TopoDS_Shape>();
			getCompounds(shape.getShape(), compounds);
		}
		else
		{
			compounds = null;
			OCCShapeTypes type = (OCCShapeTypes) t;
			occExp.Init(shape.getShape(), type.asType());
		}
	}
	
	public boolean more()
	{
		if(compounds == null)
			return (occExp.More() == 1);
		else
			return compId < compounds.size();
	}
	
	public void next()
	{
		if(compounds == null)
			occExp.Next();
		else
			compId ++;
	}
	
	public CADShape current()
	{
		TopoDS_Shape s;
		if(compounds == null)
			s = occExp.Current();
		else
			s = compounds.get(compId);
		return CADShapeFactory.getFactory().newShape(s);
	}

	private static void getCompounds(TopoDS_Shape root, List<TopoDS_Shape> result)
	{
		if( root instanceof TopoDS_Compound)
		{
			result.add(root);
			TopoDS_Iterator it = new TopoDS_Iterator(root);
			while(it.More() == 1)
			{
				getCompounds(it.Value(), result);
				it.Next();
			}
		}
	}
}
