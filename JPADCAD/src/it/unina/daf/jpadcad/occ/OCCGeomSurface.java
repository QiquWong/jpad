package it.unina.daf.jpadcad.occ;

import opencascade.GeomAPI_ProjectPointOnSurf;
import opencascade.GeomLProp_SLProps;
import opencascade.Geom_Surface;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;

public class OCCGeomSurface implements CADGeomSurface
{
	private Geom_Surface mySurface = null;
	private GeomLProp_SLProps myLprop = null;
	
	public OCCGeomSurface()
	{
	}
	
	public final void setSurface(Object o)
	{
		mySurface = (Geom_Surface) o;
	}
	
	public Object getSurface()
	{
		return mySurface;
	}
	
	public double [] value(double u, double v)
	{
		gp_Pnt p = mySurface.Value(u, v);
		double[] ret = new double[3];
		ret[0] = p.X(); 
		ret[1] = p.Y(); 
		ret[2] = p.Z();
		return ret;
	}
	
	public void dinit(int degree)
	{
		myLprop = new GeomLProp_SLProps(degree, 0.0001);
		myLprop.SetSurface(mySurface);
	}
	
	public void setParameter(double u, double v)
	{
		assert null != myLprop;
		myLprop.SetParameters(u, v);
	}
	
	public double [] d1U()
	{
		assert null != myLprop;
		return new double[]{myLprop.D1U().X(), myLprop.D1U().Y(), myLprop.D1U().Z()};
	}
	
	public double [] d1V()
	{
		assert null != myLprop;
		return new double[]{myLprop.D1V().X(), myLprop.D1V().Y(), myLprop.D1V().Z()};
	}
	
	public double [] d2U()
	{
		assert null != myLprop;
		return new double[]{myLprop.D2U().X(), myLprop.D2U().Y(), myLprop.D2U().Z()};
	}
	
	public double [] d2V()
	{
		assert null != myLprop;
		return new double[]{myLprop.D2V().X(), myLprop.D2V().Y(), myLprop.D2V().Z()};
	}
	
	public double [] dUV()
	{
		assert null != myLprop;
		return new double[]{myLprop.DUV().X(), myLprop.DUV().Y(), myLprop.DUV().Z()};
	}
	
	public double [] normal()
	{
		assert null != myLprop;
		return new double[]{myLprop.Normal().X(), myLprop.Normal().Y(), myLprop.Normal().Z()};
	}
	
	public double minCurvature()
	{
		assert null != myLprop;
		return myLprop.MinCurvature();
	}
	
	public double maxCurvature()
	{
		assert null != myLprop;
		return myLprop.MaxCurvature();
	}
	
	public double meanCurvature()
	{
		assert null != myLprop;
		return myLprop.MeanCurvature();
	}
	
	public double gaussianCurvature()
	{
		assert null != myLprop;
		return myLprop.GaussianCurvature();
	}
	
	public double [] curvatureDirections()
	{
		assert null != myLprop;
		// TODO: fix this
		//return myLprop.CurvatureDirections();
		return new double[]{1, 0, 0};
		
	}
	
	public double lowerDistance(double [] p)
	{
//		GeomAPI_ProjectPointOnSurf pps = new GeomAPI_ProjectPointOnSurf(p, mySurface);
		GeomAPI_ProjectPointOnSurf pps = new GeomAPI_ProjectPointOnSurf();
		pps.Init(new gp_Pnt(p[0], p[1], p[2]), mySurface);
		return pps.LowerDistance();
	}

}
