package it.unina.daf.jpadcad.occ;

import opencascade.BRep_Tool;
import opencascade.Geom2dAdaptor_Curve;
import opencascade.Geom2d_Curve;
import opencascade.gp_Pnt2d;

public class OCCGeomCurve2D implements CADGeomCurve2D
{
	private Geom2dAdaptor_Curve myCurve = null;
	private final double [] range = new double[2];
	
	public OCCGeomCurve2D(CADEdge E, CADFace F)
	{
		if (!(E instanceof OCCEdge))
			throw new IllegalArgumentException();
		if (!(F instanceof OCCFace))
			throw new IllegalArgumentException();
		OCCEdge occEdge = (OCCEdge) E;
		OCCFace occFace = (OCCFace) F;
		double[] first = new double[1];
		double[] last  = new double[1];
		Geom2d_Curve curve = BRep_Tool.CurveOnSurface(
			occEdge.getShape(),
			occFace.getShape(), first, last);
		range[0] = first[0];
		range[1] = last[0];
		if (curve == null)
			throw new RuntimeException();
		myCurve = new Geom2dAdaptor_Curve(curve);
	}
	
	public double [] value(double p)
	{
		assert myCurve != null;
		//return myCurve.Value((float) p);
		gp_Pnt2d pt = myCurve.Value((float) p);
		return new double[]{pt.X(), pt.Y()};
		
	}
	
	public double [] getRange()
	{
		return range;
	}
	
}
