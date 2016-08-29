package it.unina.daf.jpadcad.occ;

import opencascade.BRepBndLib;
import opencascade.BRepTools;
import opencascade.Bnd_Box;
import opencascade.TopAbs_Orientation;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Solid;
import opencascade.TopoDS_Vertex;

public class OCCShape implements CADShape
{
	TopoDS_Shape myShape = null;
	
	OCCShape()
	{
	}
	
	final void setShape(TopoDS_Shape o)
	{
		myShape = o;
	}
	
	public TopoDS_Shape getShape()
	{
		return myShape;
	}
	
	public double [] boundingBox()
	{
		Bnd_Box box = new Bnd_Box();
		BRepBndLib.Add(myShape, box);
		double[] ret = new double[6];
		ret[0] = box.CornerMin().X();
		ret[1] = box.CornerMin().Y();
		ret[2] = box.CornerMin().Z();
		ret[3] = box.CornerMax().X();
		ret[4] = box.CornerMax().Y();
		ret[5] = box.CornerMax().Z();
		return ret;
	}
	
	public OCCShape reversed()
	{
		OCCShape s;
		if (myShape instanceof TopoDS_Vertex)
			s = new OCCVertex();
		else if (myShape instanceof TopoDS_Edge)
			s = new OCCEdge();
		else if (myShape instanceof TopoDS_Face)
			s = new OCCFace();
		else if (myShape instanceof TopoDS_Solid)
			s = new OCCSolid();
		else
			s = new OCCShape();
		s.setShape(myShape.Reversed());
		return s;
	}
	
	public int orientation()
	{
		return myShape.Orientation().swigValue();
	}
	
	public boolean isOrientationForward()
	{
		return myShape.Orientation() == TopAbs_Orientation.TopAbs_FORWARD;
	}
	
	@Override
	public final boolean equals(Object o)
	{
		if (o == null)
			return false;
		if (!(o instanceof OCCShape))
			return false;
		OCCShape that = (OCCShape) o;
		return myShape.equals(that.myShape);
	}
	
	public boolean isSame(Object o)
	{
		OCCShape that = (OCCShape) o;
		long ret = myShape.IsSame(that.myShape);
		return (ret == 1);
	}
	
	public void writeNative(String filename)
	{
  		BRepTools.Write(myShape, filename);
	}
	
	@Override
	public final int hashCode()
	{
		return myShape.hashCode();
	}
	
}
