package it.unina.daf.jpadcad.occ;

import java.util.Arrays;

import opencascade.BRepGProp;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.GeomAdaptor_Curve;
import opencascade.Geom_Curve;
import opencascade.gp_Pnt;

public class OCCGeomCurve3D implements CADGeomCurve3D
{
	private GeomAdaptor_Curve myCurve = null;
	private final double [] range = new double[2];
	private OCCDiscretizeCurve3D discret = null;
	private double len = 0.0;
	
	public OCCGeomCurve3D(CADEdge E)
	{
		if (!(E instanceof OCCEdge))
			throw new IllegalArgumentException();
		OCCEdge occEdge = (OCCEdge) E;
		double[] first = new double[1];
		double[] last  = new double[1];
		Geom_Curve curve = BRep_Tool.
				Curve(occEdge.getShape(), first, last);
		if (curve == null)
			throw new RuntimeException();
		range[0] = first[0];
		range[1] = last[0];
		myCurve = new GeomAdaptor_Curve(curve);
		GProp_GProps myProps = new GProp_GProps();
		BRepGProp.LinearProperties (occEdge.getShape(), myProps);
		len = myProps.Mass();
		// agodemar
		//System.out.println("OCCGeomCurve3D - len: " + len);
		//System.out.println("OCCGeomCurve3D - range: " + Arrays.toString(range));
	}
	
	public double [] value(double p)
	{
		assert myCurve != null;
		//return myCurve.Value((float) p);
		gp_Pnt pt = myCurve.Value((float) p);
		return new double[]{pt.X(), pt.Y(), pt.Z()};
	}
	
	public double [] getRange()
	{
		return range;
	}
	
	public void discretize(double maxlen, double deflection, boolean relDefl)
	{
		if (discret == null)
			discret = new OCCDiscretizeCurve3D(myCurve, range[0], range[1]);
		discret.discretizeMaxDeflection(deflection, relDefl);
		if (maxlen > 0.0)
		{
			for (int i = 0; i < discret.nbPoints()-1; i++)
				discret.discretizeSubsegmentMaxLength(i, maxlen);
		}
	}
	
	public void discretize(double maxlen)
	{
		if (discret == null)
			discret = new OCCDiscretizeCurve3D(myCurve, range[0], range[1]);
		discret.discretizeMaxLength(maxlen);
	}
	
	public void discretize(int n)
	{
		if (discret == null)
			discret = new OCCDiscretizeCurve3D(myCurve, range[0], range[1]);
		discret.discretizeNrPoints(n);
	}
	
	public void splitSubsegment(int numseg, int nrsub)
	{
		if (discret == null)
			discret = new OCCDiscretizeCurve3D(myCurve, range[0], range[1]);
		discret.splitSubsegment(numseg, nrsub);
	}
	
	public void setDiscretization(double [] param)
	{
		if (discret == null)
			discret = new OCCDiscretizeCurve3D(myCurve, range[0], range[1]);
		discret.setDiscretization(param);
	}
	
	public int nbPoints()
	{
		assert discret != null;
		return discret.nbPoints();
	}
	
	public double parameter(int index)
	{
		assert discret != null;
		return discret.parameter(index);
	}
	
	public double length()
	{
		return len;
	}
	
}
