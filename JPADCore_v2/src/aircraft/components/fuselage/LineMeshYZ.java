package aircraft.components.fuselage;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Length;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.jscience.physics.amount.Amount;

import processing.core.PVector;

public class LineMeshYZ {

	private Fuselage _theFuselage;
	private FuselageCurvesSection _theCurvesSection;
	private Amount<Length> _x;
	
	private List<PVector> _meshPoints = new ArrayList<PVector>(); 
	
	// 'ni' stands for number of intervals
	private int _ni_Sec;
	
	public LineMeshYZ(
			Fuselage fuselage, 
			FuselageCurvesSection curvesSection, 
		    Amount<Length> x, 
			int ni_Sec) 
	{
		_theFuselage = fuselage;
		_ni_Sec = ni_Sec;
		_theCurvesSection = curvesSection; 
		
		_x = x; // _theFuselage.get_len_N();
		
		// build the mesh
		List<PVector> vPPoints0 = new ArrayList<PVector>(); 
		vPPoints0 = _theFuselage.getUniqueValuesYZSideRCurve( _x );

//		System.out.println(vCurveAbscissa);
//		System.out.println("points: "+vPPoints.size());
	
		List<Double> vCurveAbscissa0 = new ArrayList<Double>();
		Double sum = 0.0;
		vCurveAbscissa0.add( sum );
		for (int i = 1; i < vPPoints0.size(); i++)
		{
			Double ds2 = 
					Math.pow(vPPoints0.get(i).y - vPPoints0.get(i-1).y, 2) +
					Math.pow(vPPoints0.get(i).z - vPPoints0.get(i-1).z, 2);
			sum = sum + Math.sqrt(ds2); 
			vCurveAbscissa0.add( sum );
		}
		
//		System.out.println(vCurveAbscissa);
//		System.out.println("s: "+vCurveAbscissa.size());

		List<Double> vY0 = new ArrayList<Double>();
		List<Double> vZ0 = new ArrayList<Double>();
		for (int i = 0; i < vPPoints0.size(); i++)
		{
			vY0.add((double) vPPoints0.get(i).y);
			vZ0.add((double) vPPoints0.get(i).z);
		}

//		System.out.println("Y: "+vY.size());
//		System.out.println("Z: "+vZ.size());

		List<Double> vTheta0 = new ArrayList<Double>();
		vTheta0.add(0.0);
		for (int i = 1; i < vPPoints0.size(); i++)
		{
			vTheta0.add(
					Math.atan2(vY0.get(i), vZ0.get(i))
					);
		}
		
//		System.out.println("Theta1: "+vTheta1.size());
//		System.out.println(vTheta1);
		
		// Desired points
		Double dTheta = Math.PI/_ni_Sec;
		List<Double> vTheta = new ArrayList<Double>();
		for (int i = 0; i <= _ni_Sec; i++)
		{
			vTheta.add(0.0 + i*dTheta);
		}

//		System.out.println("theta: "+vTheta.size());
		
		double vaTheta1 []  = new double[vTheta0.size()];
		double vaY []  = new double[vY0.size()];
		double vaZ []  = new double[vZ0.size()];
		for (int i = 0; i < vaTheta1.length; i++) {
			vaTheta1[i] = vTheta0.get(i);
			vaY     [i] = vY0.get(i);
			vaZ     [i] = vZ0.get(i);
		}

		
		UnivariateInterpolator interpolatorY = new LinearInterpolator(); // SplineInterpolator();
		UnivariateFunction myInterpolationFunctionY = interpolatorY.interpolate(vaTheta1, vaY);
		UnivariateFunction myInterpolationFunctionZ = interpolatorY.interpolate(vaTheta1, vaZ);
		// now interpolate
		_meshPoints.add(
				new PVector(
						(float) _x.getEstimatedValue(),
						(float) vY0.get(0).doubleValue(),
						(float) vZ0.get(0).doubleValue()
					)
				);
		for (int i = 1; i < vTheta.size()-1; i++) {
			Double y = myInterpolationFunctionY.value(vTheta.get(i));			
			Double z = myInterpolationFunctionZ.value(vTheta.get(i));		
			_meshPoints.add(
					new PVector(
							(float) _x.getEstimatedValue(),
							(float) y.doubleValue(),
							(float) z.doubleValue()
						)
					);
		}
		_meshPoints.add(
				new PVector(
						(float) _x.getEstimatedValue(),
						(float) vY0.get(vY0.size()-1).doubleValue(),
						(float) vZ0.get(vZ0.size()-1).doubleValue()
					)
				);
		

	} // end-of-constructor

	public int get_ni_Sec() {
		return _ni_Sec;
	}

	public void set_ni_Sec(int _ni_Sec) {
		this._ni_Sec = _ni_Sec;
	}

	public Fuselage get_Fuselage() {
		return _theFuselage;
	}

	public void set_Fuselage(Fuselage _theFuselage) {
		this._theFuselage = _theFuselage;
	}

	public FuselageCurvesSection get_CurvesSection() {
		return _theCurvesSection;
	}

	public void set_CurvesSection(FuselageCurvesSection _theCurvesSection) {
		this._theCurvesSection = _theCurvesSection;
	}

	public List<PVector> get_meshPoints() {
		return _meshPoints;
	}

}
