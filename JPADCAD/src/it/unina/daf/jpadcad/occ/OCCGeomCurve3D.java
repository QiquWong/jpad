package it.unina.daf.jpadcad.occ;

import java.util.Arrays;
import java.util.List;

import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepGProp;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.GeomAPI_Interpolate;
import opencascade.GeomAdaptor_Curve;
import opencascade.Geom_BSplineCurve;
import opencascade.Geom_Curve;
import opencascade.Precision;
import opencascade.TColgp_HArray1OfPnt;
import opencascade.TopoDS_Edge;
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
	
	public OCCGeomCurve3D(List<double[]> pointList, boolean isPeriodic) {
		
		//-----------------------------------------------------------
		// Create a low-level object using OCCT JavaWrapper classes
		
		// curve
		int nPoints = pointList.size();
		if (nPoints > 0) {			
			TColgp_HArray1OfPnt points = new TColgp_HArray1OfPnt(1, nPoints);
			for(int i=0; i < nPoints-1; i++) {
				points.SetValue(i+1, new gp_Pnt(pointList.get(i)[0],  pointList.get(i)[1], pointList.get(i)[2]));
			}
			long longIsPeriodic = 0;
			if (isPeriodic)
				longIsPeriodic = 1;
			
			for(int i=1; i < nPoints; i++) {
				System.out.println(points.Value(i));
			}
			
			GeomAPI_Interpolate anInterpolate = 
					new GeomAPI_Interpolate(points, longIsPeriodic, Precision.Confusion());
			anInterpolate.Perform();
			Geom_BSplineCurve anInterpolationCurve = anInterpolate.Curve();
			TopoDS_Edge spline = new BRepBuilderAPI_MakeEdge(anInterpolationCurve).Edge();
			
			double[] first = new double[1];
			double[] last  = new double[1];
			Geom_Curve curve = BRep_Tool.
					Curve(spline, first, last);
			if (curve == null)
				throw new RuntimeException();
			range[0] = first[0];
			range[1] = last[0];
			myCurve = new GeomAdaptor_Curve(curve);
			GProp_GProps myProps = new GProp_GProps();
			BRepGProp.LinearProperties (spline, myProps);
			len = myProps.Mass();
			// agodemar
			//System.out.println("OCCGeomCurve3D - len: " + len);
			//System.out.println("OCCGeomCurve3D - range: " + Arrays.toString(range));		
		}
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
