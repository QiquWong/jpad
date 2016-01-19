/* jCAE stand for Java Computer Aided Engineering. Features are : Small CAD
   modeler, Finite element mesher, Plugin architecture.

    Copyright (C) 2004,2005, by EADS CRC

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package cad.occ;

import org.jcae.opencascade.jni.BRep_Tool;
import org.jcae.opencascade.jni.TopExp;
import org.jcae.opencascade.jni.TopoDS_Edge;
import org.jcae.opencascade.jni.TopoDS_Vertex;

import cad.jcae.CADEdge;
import cad.jcae.CADVertex;

public class OCCEdge extends OCCShape implements CADEdge
{
	@Override
	public final TopoDS_Edge getShape()
	{
		return (TopoDS_Edge) myShape;
	}

	public boolean isDegenerated()
	{
		return BRep_Tool.degenerated(getShape());
	}
	
	public double [] range()
	{
		return BRep_Tool.range(getShape());
	}
	
	public CADVertex [] vertices()
	{
		OCCVertex[] V = new OCCVertex[2];
		//  A CADExplorer must not be used here because it
		//  does not necessarily return vertices in the same order.
		//  TopExp.vertices takes care of edge orientation.
		TopoDS_Vertex [] tv = TopExp.vertices(getShape());
		for (int i = 0; i < 2; i++)
		{
			OCCVertex occVertex=new OCCVertex();
			occVertex.setShape(tv[i]);
			V[i] = occVertex;			
		}
		return V;
	}
	
}
