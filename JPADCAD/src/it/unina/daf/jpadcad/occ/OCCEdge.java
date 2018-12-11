package it.unina.daf.jpadcad.occ;

import opencascade.BRep_Tool;
import opencascade.TopExp;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Vertex;

public class OCCEdge extends OCCShape implements CADEdge
{
	@Override
	public final TopoDS_Edge getShape()
	{
		return TopoDS.ToEdge(myShape);
	}

	public boolean isDegenerated()
	{
		return (BRep_Tool.Degenerated(getShape()) == 1);
	}
	
	public double [] range()
	{
		double[] first = new double[1];
		double[] last  = new double[1];
		BRep_Tool.Range(getShape(), first, last);
		return new double[]{first[0], last[0]};
		//return BRep_Tool.range(getShape());
	}
	
	public CADVertex [] vertices()
	{
		OCCVertex[] V = new OCCVertex[2];
		//  A CADExplorer must not be used here because it
		//  does not necessarily return vertices in the same order.
		//  TopExp.vertices takes care of edge orientation.
		// TopoDS_Vertex[] tv = TopExp.vertices(getShape());

		TopoDS_Vertex tv1 = new TopoDS_Vertex();
		TopoDS_Vertex tv2 = new TopoDS_Vertex();
		TopExp.Vertices(getShape(), tv1, tv2);
		
		TopoDS_Vertex [] tv = new TopoDS_Vertex[]{tv1, tv2};
		
		for (int i = 0; i < 2; i++)
		{
			OCCVertex occVertex=new OCCVertex();
			occVertex.setShape(tv[i]);
			V[i] = occVertex;			
		}
		return V;
	}
	
}
