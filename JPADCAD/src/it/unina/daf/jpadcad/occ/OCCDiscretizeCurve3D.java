package it.unina.daf.jpadcad.occ;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import gnu.trove.list.array.TIntArrayList;
import opencascade.Adaptor3d_Curve;
import opencascade.gp_Pnt;

public class OCCDiscretizeCurve3D
{
	private static final Logger logger=Logger.getLogger(OCCDiscretizeCurve3D.class.getName());
	private final Adaptor3d_Curve curve;
	// Number of points
	private int nr = 0;
	private double length = -1.0;
	private double [] a;
	private final double start;
	private final double end;
	
	private	void curveArrayValues(Adaptor3d_Curve curve, int size, double u[])
	{
		for (int i = 0; i < size; i++)
		{
			gp_Pnt gp = curve.Value(u[3*i]);
			u[3*i]    = gp.X();
			u[3*i+1]  = gp.Y();
			u[3*i+2]  = gp.Z();
		}	
	}
	
	public OCCDiscretizeCurve3D(Adaptor3d_Curve myCurve, double s, double e)
	{
		curve = myCurve;
		start = s;
		end = e;
	}
	
	public final void discretizeMaxLength(double len)
	{
		logger.fine("Discretize with max length: "+len);
		int nsegments = 10;
		double [] xyz;
		while (true)
		{
			nsegments *= 10;
			a = new double[nsegments+1];
			double delta = (end - start) / nsegments;
			xyz = new double[3*(nsegments+1)];
			for (int i = 0; i < nsegments; i++)
				xyz[3*i] = start + i * delta;
			//  Avoid rounding errors
			xyz[3*nsegments] = end;
			// curve.arrayValues(nsegments + 1, xyz);
			curveArrayValues(curve, nsegments + 1, xyz);
			
			double abscissa, dist;
			nr = 1;
			a[0] = start;
			for (int ns = 1; ns < nsegments; ns++)
			{
				abscissa = start + ns * delta;
				dist = Math.sqrt(
				  (xyz[3*nr-3] - xyz[3*ns  ]) * (xyz[3*nr-3] - xyz[3*ns  ]) +
				  (xyz[3*nr-2] - xyz[3*ns+1]) * (xyz[3*nr-2] - xyz[3*ns+1]) +
				  (xyz[3*nr-1] - xyz[3*ns+2]) * (xyz[3*nr-1] - xyz[3*ns+2]));
				if (dist > len)
				{
					a[nr] = abscissa;
					if (nr < ns)
					{
						xyz[3*nr]   = xyz[3*ns];
						xyz[3*nr+1] = xyz[3*ns+1];
						xyz[3*nr+2] = xyz[3*ns+2];
					}
					nr++;
				}
			}
			a[nr] = end;
			if (nr < nsegments)
			{
				xyz[3*nr]   = xyz[3*nsegments];
				xyz[3*nr+1] = xyz[3*nsegments+1];
				xyz[3*nr+2] = xyz[3*nsegments+2];
			}
			nr++;
			//  Stop when there are at least 10 points per segments
			if (nr * 10 < nsegments)
				break;
		}
		logger.fine("(length) Number of points: "+nr);
		length = -1.0;
		adjustAbscissas(xyz, new CheckRatioLength());
	}
	
	public final void setDiscretization(double [] param)
	{
		nr = param.length;
		a = new double[nr];
		System.arraycopy(param, 0, a, 0, nr);
		length = -1.0;
	}
	
	private void split(int n)
	{
		nr = n + 1;
		a = new double[nr];
		double delta = (end - start) / n;
		for (int i = 0; i < n; i++)
			a[i] = start + i * delta;
		
		//  Avoid rounding errors
		a[n] = end;
		length = -1.0;
	}
	
	public final void splitSubsegment(int numseg, int nrsub)
	{
		if (numseg < 0 || numseg >= nr)
			return;
		OCCDiscretizeCurve3D ref = new OCCDiscretizeCurve3D(curve, a[numseg], a[numseg+1]);
		ref.split(nrsub);
		double [] newA = new double[nr+ref.nr-2];
		if (numseg > 0)
			System.arraycopy(a, 0, newA, 0, numseg);
		if (ref.nr > 0)
			System.arraycopy(ref.a, 0, newA, numseg, ref.nr);
		if (nr-numseg-2 > 0)
			System.arraycopy(a, numseg+2, newA, numseg+ref.nr, nr-numseg-2);
		a = newA;
		nr += ref.nr - 2;
	}
	
	public final void discretizeSubsegmentMaxLength(int numseg, double len)
	{
		if (numseg < 0 || numseg >= nr)
			return;
		OCCDiscretizeCurve3D ref = new OCCDiscretizeCurve3D(curve, a[numseg], a[numseg+1]);
		ref.discretizeMaxLength(len);
		double [] newA = new double[nr+ref.nr-2];
		if (numseg > 0)
			System.arraycopy(a, 0, newA, 0, numseg);
		if (ref.nr > 0)
			System.arraycopy(ref.a, 0, newA, numseg, ref.nr);
		if (nr-numseg-2 > 0)
			System.arraycopy(a, numseg+2, newA, numseg+ref.nr, nr-numseg-2);
		a = newA;
		nr += ref.nr - 2;
	}
	
	public final void discretizeNrPoints(int n)
	{
		nr = n;
		int nsegments = n;
		double [] xyz;
		TIntArrayList abscissa = new TIntArrayList(nsegments);
		while (true)
		{
			nsegments *= 10;
			a = new double[nsegments+1];
			double delta = (end - start) / nsegments;
			xyz = new double[3*(nsegments+1)];
			for (int i = 0; i < nsegments; i++)
				xyz[3*i] = start + i * delta;
			//  Avoid rounding errors
			xyz[3*nsegments] = end;
			//curve.arrayValues(nsegments + 1, xyz);
			curveArrayValues(curve, nsegments + 1, xyz);
			
			a[0] = start;
			//  Compute length, a[] and xyz[]
			double len = 0.0;
			for (int ns = 1; ns <= nsegments; ns++)
			{
				a[ns] = start + ns * delta;
				len += Math.sqrt(
				  (xyz[3*ns-3] - xyz[3*ns  ]) * (xyz[3*ns-3] - xyz[3*ns  ]) +
				  (xyz[3*ns-2] - xyz[3*ns+1]) * (xyz[3*ns-2] - xyz[3*ns+1]) +
				  (xyz[3*ns-1] - xyz[3*ns+2]) * (xyz[3*ns-1] - xyz[3*ns+2]));
			}
			double lmax = 2.0 * len / nr;
			double lmin = 0.0;
			double maxlen, dist;
			while (true)
			{
				maxlen = 0.5 * (lmin + lmax);
				int lastIndex = 0;
				abscissa.clear();
				abscissa.add(0);
				nr = 1;
				for (int ns = 1; ns < nsegments; ns++)
				{
					dist = Math.sqrt(
				  		(xyz[3*ns  ] - xyz[3*lastIndex  ]) * (xyz[3*ns  ] - xyz[3*lastIndex  ]) +
				  		(xyz[3*ns+1] - xyz[3*lastIndex+1]) * (xyz[3*ns+1] - xyz[3*lastIndex+1]) +
				  		(xyz[3*ns+2] - xyz[3*lastIndex+2]) * (xyz[3*ns+2] - xyz[3*lastIndex+2]));
					if (dist > maxlen)
					{
						lastIndex = ns;
						nr++;
						abscissa.add(ns);
					}
				}
				nr++;
				abscissa.add(nsegments);
				if (n == nr)
					break;
				else if (nr < n)
					lmax = lmax - 0.5 * (lmax - lmin);
				else
					lmin = lmin + 0.5 * (lmax - lmin);
				if (lmax - lmin < 0.5 * delta)
					break;
			}
			if (n == nr)
				break;
		}
		for (int i = 0; i < nr; i++)
		{
			int ind = abscissa.get(i);
			if (ind != i)
			{
				a[i] = a[ind];
				xyz[3*i]   = xyz[3*ind];
				xyz[3*i+1] = xyz[3*ind+1];
				xyz[3*i+2] = xyz[3*ind+2];
			}
		}
		length = -1.0;
		adjustAbscissas(xyz, new CheckRatioLength());
	}
	
	public final void discretizeMaxDeflection(double defl, boolean relDefl)
	{
		if (defl <= 0.0)
			return;
		int nsegments = 10;
		double [] xyz;
		//  See org.jcae.mesh.amibe.metrics.Metric3D for an
		//  explanation about this sqrt(2).
		defl *= Math.sqrt(2);
		while (true)
		{
			nsegments *= 10;
			a = new double[nsegments+1];
			double delta = (end - start) / nsegments;
			xyz = new double[3*(nsegments+1)];
			for (int i = 0; i < nsegments; i++)
				xyz[3*i] = start + i * delta;
			//  Avoid rounding errors
			xyz[3*nsegments] = end;
			//curve.arrayValues(nsegments + 1, xyz);
			curveArrayValues(curve, nsegments + 1, xyz);
			
			double oldAbscissa, newAbscissa;
			double dist, arcLength;
			nr = 1;
			a[0] = start;
			arcLength   = 0.0;
			oldAbscissa = start;
			for (int ns = 1; ns < nsegments; ns++)
			{
				newAbscissa = start + ns * delta;
				dist = Math.sqrt(
				  (xyz[3*nr-3] - xyz[3*ns  ]) * (xyz[3*nr-3] - xyz[3*ns  ]) +
				  (xyz[3*nr-2] - xyz[3*ns+1]) * (xyz[3*nr-2] - xyz[3*ns+1]) +
				  (xyz[3*nr-1] - xyz[3*ns+2]) * (xyz[3*nr-1] - xyz[3*ns+2]));
				arcLength += length(oldAbscissa, newAbscissa, 20);
				oldAbscissa = newAbscissa;
				double dmax = defl;
				if (relDefl)
					dmax *= arcLength;
				if (arcLength - dist > dmax)
				{
					a[nr] = newAbscissa;
					arcLength   = 0.0;
					if (nr < ns)
					{
						xyz[3*nr]   = xyz[3*ns];
						xyz[3*nr+1] = xyz[3*ns+1];
						xyz[3*nr+2] = xyz[3*ns+2];
					}
					nr++;
				}
			}
			a[nr] = end;
			if (nr < nsegments)
			{
				xyz[3*nr]   = xyz[3*nsegments];
				xyz[3*nr+1] = xyz[3*nsegments+1];
				xyz[3*nr+2] = xyz[3*nsegments+2];
			}
			nr++;
			//  Stop when there are at least 10 points per segments
			if (nr * 10 < nsegments)
				break;
		}
		logger.fine("(deflection) Number of points: "+nr);
		length = -1.0;
		adjustAbscissas(xyz, new CheckRatioDeflection());
	}
	
	private void adjustAbscissas(double [] xyz, CheckRatio func)
	{
		boolean backward = false;
		int niter = 2*nr;
		while (niter > 0)
		{
			niter--;
			backward = ! backward;
			boolean redo = false;
			if (backward)
			{
				for (int i = nr - 2; i > 0; i--)
					redo |= func.move(i, xyz);
			}
			else
			{
				for (int i = 1; i < nr - 1; i++)
					redo |= func.move(i, xyz);
			}
			if (!redo)
				break;
		}
	}
	
	private interface CheckRatio
	{
		boolean move(int i, double [] xyz);
	}
	
	class CheckRatioLength implements CheckRatio
	{
		public boolean move(int i, double [] xyz)
		{
			boolean ret = false;
			double l1 = Math.sqrt(
			  (xyz[3*i  ] - xyz[3*i-3]) * (xyz[3*i  ] - xyz[3*i-3]) +
			  (xyz[3*i+1] - xyz[3*i-2]) * (xyz[3*i+1] - xyz[3*i-2]) +
			  (xyz[3*i+2] - xyz[3*i-1]) * (xyz[3*i+2] - xyz[3*i-1]));
			double l2 = Math.sqrt(
			  (xyz[3*i  ] - xyz[3*i+3]) * (xyz[3*i  ] - xyz[3*i+3]) +
			  (xyz[3*i+1] - xyz[3*i+4]) * (xyz[3*i+1] - xyz[3*i+4]) +
			  (xyz[3*i+2] - xyz[3*i+5]) * (xyz[3*i+2] - xyz[3*i+5]));
			double delta = Math.abs(l2 - l1);
			if (delta > 0.05 * (l1+l2))
			{
				double newA = a[i] + 0.8 * (a[i+1] - a[i-1]) * (l2 - l1) / (l1 + l2);
				//double [] newXYZ = curve.Value(newA);
				gp_Pnt pt = curve.Value(newA);
				double [] newXYZ = new double[]{pt.X(), pt.Y(), pt.Z()};
				
				double newl1 = Math.sqrt(
				  (newXYZ[0] - xyz[3*i-3]) * (newXYZ[0] - xyz[3*i-3]) +
				  (newXYZ[1] - xyz[3*i-2]) * (newXYZ[1] - xyz[3*i-2]) +
				  (newXYZ[2] - xyz[3*i-1]) * (newXYZ[2] - xyz[3*i-1]));
				double newl2 = Math.sqrt(
				  (newXYZ[0] - xyz[3*i+3]) * (newXYZ[0] - xyz[3*i+3]) +
				  (newXYZ[1] - xyz[3*i+4]) * (newXYZ[1] - xyz[3*i+4]) +
				  (newXYZ[2] - xyz[3*i+5]) * (newXYZ[2] - xyz[3*i+5]));
				if (Math.abs(newl2 - newl1) < delta)
				{
					ret = true;
					a[i] = newA;
					xyz[3*i]   = newXYZ[0];
					xyz[3*i+1] = newXYZ[1];
					xyz[3*i+2] = newXYZ[2];
				}
			}
			return ret;
		}
	}
	
	class CheckRatioDeflection implements CheckRatio
	{
		public boolean move(int i, double [] xyz)
		{
			boolean ret = false;
			double l1 = Math.sqrt(
			  (xyz[3*i  ] - xyz[3*i-3]) * (xyz[3*i  ] - xyz[3*i-3]) +
			  (xyz[3*i+1] - xyz[3*i-2]) * (xyz[3*i+1] - xyz[3*i-2]) +
			  (xyz[3*i+2] - xyz[3*i-1]) * (xyz[3*i+2] - xyz[3*i-1]));
			double l2 = Math.sqrt(
			  (xyz[3*i  ] - xyz[3*i+3]) * (xyz[3*i  ] - xyz[3*i+3]) +
			  (xyz[3*i+1] - xyz[3*i+4]) * (xyz[3*i+1] - xyz[3*i+4]) +
			  (xyz[3*i+2] - xyz[3*i+5]) * (xyz[3*i+2] - xyz[3*i+5]));
			double a1 = length(a[i-1], a[i], 20);
			double a2 = length(a[i], a[i+1], 20);
			double d1 = (a1 - l1) / a1;
			double d2 = (a2 - l2) / a2;
			double d3 = (a1 + a2 - l1 - l2) / (a1 + a2);
			double delta = Math.abs(d2 - d1);
			if (delta > 0.05 * d3)
			{
				double newA = a[i] + 0.8 * (a[i+1] - a[i-1]) * (l2 - l1) / (l1 + l2);
				//double [] newXYZ = curve.Value(newA);
				gp_Pnt pt = curve.Value(newA);
				double [] newXYZ = new double[]{pt.X(), pt.Y(), pt.Z()};
				
				l1 = Math.sqrt(
				  (newXYZ[0] - xyz[3*i-3]) * (newXYZ[0] - xyz[3*i-3]) +
				  (newXYZ[1] - xyz[3*i-2]) * (newXYZ[1] - xyz[3*i-2]) +
				  (newXYZ[2] - xyz[3*i-1]) * (newXYZ[2] - xyz[3*i-1]));
				l2 = Math.sqrt(
				  (newXYZ[0] - xyz[3*i+3]) * (newXYZ[0] - xyz[3*i+3]) +
				  (newXYZ[1] - xyz[3*i+4]) * (newXYZ[1] - xyz[3*i+4]) +
				  (newXYZ[2] - xyz[3*i+5]) * (newXYZ[2] - xyz[3*i+5]));
				a1 = length(a[i-1], newA, 20);
				a2 = length(newA, a[i+1], 20);
				d1 = (a1 - l1) / a1;
				d2 = (a2 - l2) / a2;
				if (Math.abs(d2 - d1) < delta)
				{
					ret = true;
					a[i] = newA;
					xyz[3*i]   = newXYZ[0];
					xyz[3*i+1] = newXYZ[1];
					xyz[3*i+2] = newXYZ[2];
				}
			}
			return ret;
		}
	}
	
	public final int nbPoints()
	{
		return nr;
	}
	
	public final double parameter(int index)
	{
		return a[index-1];
	}
	
	final double length(double from, double to, int nrsub)
	{
		assert nr > 0;
		double delta = (to - from) / nrsub;
		double l = 0.0;
		double [] xyz = new double[3*(nrsub+1)];
		for (int i = 0; i < nrsub; i++)
			xyz[3*i] = from + i * delta;
		//  Avoid rounding errors
		xyz[3*nrsub] = to;
		//curve.arrayValues(nrsub + 1, xyz);
		curveArrayValues(curve, nrsub + 1, xyz);
		
		for (int i = 0; i < 3 * nrsub; i+=3)
		{
			l += Math.sqrt(
			  (xyz[i+3] - xyz[i  ]) * (xyz[i+3] - xyz[i  ]) +
			  (xyz[i+4] - xyz[i+1]) * (xyz[i+4] - xyz[i+1]) +
			  (xyz[i+5] - xyz[i+2]) * (xyz[i+5] - xyz[i+2]));
		}
		return l;
	}
	
	public double length()
	{
		if (length >= 0.0)
			return length;
		
		assert nr > 0;
		double [] xyz = new double[3*nr];
		for (int i = 0; i < nr; i++)
			xyz[3*i] = a[i];
		//curve.arrayValues(nr, xyz);
		curveArrayValues(curve, nr, xyz);
		
		length = 0.0;
		for (int i = 3; i < 3*nr; i+=3)
			length += Math.sqrt(
			  (xyz[i-3] - xyz[i  ]) * (xyz[i-3] - xyz[i  ]) +
			  (xyz[i-2] - xyz[i+1]) * (xyz[i-2] - xyz[i+1]) +
			  (xyz[i-1] - xyz[i+2]) * (xyz[i-1] - xyz[i+2]));
		return length;
	}

    public List<gp_Pnt> getPoints() {
    	return IntStream.range(0, nr)
    			.mapToObj(i -> curve.Value(a[i]))
    			.collect(Collectors.toList());
	}

}
