package it.unina.daf.jpadcad.occ;

import java.util.List;

import opencascade.BRepBuilderAPI_MakeVertex;
import opencascade.BRep_Tool;
import opencascade.TopoDS;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Vertex;
import opencascade.gp_Pnt;
import opencascade.gp_Pnt2d;

public class OCCVertex extends OCCShape implements CADVertex
{

	public OCCVertex() 
	{
	}	
	
	public OCCVertex(double x, double y, double z) {
		myShape = TopoDS.ToVertex(
				new BRepBuilderAPI_MakeVertex(new gp_Pnt(x, y, z)).Vertex() 
				);
	}	
	
	@Override
	public final TopoDS_Vertex getShape()
	{
		//return (TopoDS_Vertex) myShape;
		return TopoDS.ToVertex(myShape);
	}

	public double [] parameters(CADFace o)
	{
		OCCFace face = (OCCFace) o;
		TopoDS_Face F = face.getShape();
		TopoDS_Vertex V = getShape();
		gp_Pnt2d p = BRep_Tool.Parameters(V, F);
		return new double[]{p.X(), p.Y()};
	}
	
	public double [] pnt()
	{
		gp_Pnt p = BRep_Tool.Pnt(getShape());
		return new double[]{p.X(), p.Y(), p.Z()};
	}
	
}
