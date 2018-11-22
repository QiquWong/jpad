package it.unina.daf.jpadcad.occ;

import java.util.List;

import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepGProp;
import opencascade.GProp_GProps;
import opencascade.TopoDS_Shape;
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
	
	public OCCWire() {	
	}
	
	public OCCWire(List<CADEdge> cadEdges) {
		myShape = occWireFromEdges(cadEdges);
	}
	
	private TopoDS_Shape occWireFromEdges(List<CADEdge> cadEdges) {
		
		TopoDS_Shape occWire = null;
		
		BRepBuilderAPI_MakeWire wireMaker = new BRepBuilderAPI_MakeWire();	
		for (int i = 0; i < cadEdges.size(); i++) {			
			wireMaker.Add(((OCCEdge) cadEdges.get(i)).getShape());
		}
		
		occWire = wireMaker.Wire();
		
		return occWire;
	}
}
