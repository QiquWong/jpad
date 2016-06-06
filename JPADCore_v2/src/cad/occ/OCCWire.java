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

import org.jcae.opencascade.jni.BRepGProp;
import org.jcae.opencascade.jni.GProp_GProps;
import org.jcae.opencascade.jni.TopoDS_Wire;

import cad.jcae.CADWire;

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
		BRepGProp.linearProperties (myShape, myProps);
		return myProps.mass();
	}
}
