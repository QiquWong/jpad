package it.unina.daf.jpadcad.occ;

import java.util.ArrayList;
import java.util.List;

import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepGProp;
import opencascade.BRepTools_WireExplorer;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.Geom_Curve;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Wire;
import opencascade.gp_Pnt;

public class OCCWire extends OCCShape implements CADWire
{	
	@Override
	public final TopoDS_Wire getShape()
	{
		return TopoDS.ToWire(myShape);
	}

	public double length()
	{
		GProp_GProps myProps = new GProp_GProps();
		BRepGProp.LinearProperties (myShape, myProps);
		return myProps.Mass();
	}
	
	public List<CADVertex> vertices() {
		List<CADVertex> vertices = new ArrayList<>();
		
		BRepTools_WireExplorer wireExplorer = new BRepTools_WireExplorer(TopoDS.ToWire(myShape));
		
		double[] firstP0 = new double[1];
		double[] lastP0 = new double[1];
		
		TopoDS_Edge edge0 = wireExplorer.Current();
		Geom_Curve curve0 = BRep_Tool.Curve(edge0, firstP0, lastP0);
		
		gp_Pnt firstPnt0 = curve0.Value(firstP0[0]);
		gp_Pnt lastPnt0 = curve0.Value(lastP0[0]);
		
		vertices.add(new OCCVertex(firstPnt0.X(), firstPnt0.Y(), firstPnt0.Z()));
		vertices.add(new OCCVertex(lastPnt0.X(), lastPnt0.Y(), lastPnt0.Z()));
		
		wireExplorer.Next();
		
		while (wireExplorer.More() > 0) {
			
			double[] lastP = new double[1];
			
			TopoDS_Edge edge = wireExplorer.Current();
			Geom_Curve curve = BRep_Tool.Curve(edge, new double[1], lastP);
			
			gp_Pnt lastPnt = curve.Value(lastP[0]);
			
			vertices.add(new OCCVertex(lastPnt.X(), lastPnt.Y(), lastPnt.Z()));
			
			wireExplorer.Next();
		}
		
		return vertices;
	}
	
	public List<CADEdge> edges() {
		List<CADEdge> edges = new ArrayList<>();
		
		BRepTools_WireExplorer wireExplorer = new BRepTools_WireExplorer(TopoDS.ToWire(myShape));
		
		CADShapeFactory factory = CADShapeFactory.getFactory();
		
		while (wireExplorer.More() > 0) {
			edges.add((CADEdge) factory.newShape(wireExplorer.Current()));
			
			wireExplorer.Next();
		}
		
		return edges;
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
