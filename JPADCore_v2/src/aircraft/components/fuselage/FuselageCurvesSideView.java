package aircraft.components.fuselage;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.util.ArithmeticUtils;
import org.jscience.physics.amount.Amount;

//import org.apache.commons.math3.linear.RealMatrix;
import processing.core.PVector;


public class FuselageCurvesSideView {

	private double _l_T, _l_F, _l_N, _l_C, _h_N,_h_T, _d_C, _half_d_C;//_a;
	
	// TO DO: https://github.com/jhonqco/Grafica122/blob/master/Curvas/Bezier.java

	private List<PVector> _cNoseUpperPoints     = new ArrayList<PVector>();
	private List<PVector> _cNoseLowerPoints     = new ArrayList<PVector>();
	private List<PVector> _cCylinderLowerPoints = new ArrayList<PVector>();
	private List<PVector> _cCylinderUpperPoints = new ArrayList<PVector>();
	private List<PVector> _cTailLowerPoints     = new ArrayList<PVector>();
	private List<PVector> _cTailUpperPoints     = new ArrayList<PVector>();

	private Amount<Angle> _sweepConstructionAngle = null;
	
//	int _nPoints = 20;
//	float deltaU = (float) (1.0/(_nPoints-1));

	private int _nPointsNose = 20;
	private float _deltaUNose = (float) (1.0/(_nPointsNose-1));

	private int _nPointsCylinder = 4;
	private float _deltaUCylinder = (float) (1.0/(_nPointsCylinder-1));

	private int _nPointsTail = 20;
	private float _deltaUTail = (float) (1.0/(_nPointsTail-1));

	private List<PVector> _pNoseUpperPoints     = new ArrayList<PVector>();
	private List<PVector> _pNoseLowerPoints     = new ArrayList<PVector>();
	private List<PVector> _pCylinderUpperPoints = new ArrayList<PVector>();
	private List<PVector> _pCylinderLowerPoints = new ArrayList<PVector>();
	private List<PVector> _pTailUpperPoints     = new ArrayList<PVector>();
	private List<PVector> _pTailLowerPoints     = new ArrayList<PVector>();

	private List<PVector> _pNoseCamberlinePoints     = new ArrayList<PVector>();
	private List<PVector> _pCylinderCamberlinePoints = new ArrayList<PVector>();
	private List<PVector> _pTailCamberlinePoints     = new ArrayList<PVector>();

	public List<PVector> getNoseUpperPoints(){
		return _pNoseUpperPoints;
	}

	public List<PVector> getNoseLowerPoints(){
		return _pNoseLowerPoints;
	}

	public List<PVector> getNoseCamberlinePoints(){
		return _pNoseCamberlinePoints;
	}
	
	public List<PVector> getCylinderUpperPoints(){
		return _pCylinderUpperPoints;
	}

	public List<PVector> getCylinderLowerPoints(){
		return _pCylinderLowerPoints;
	}

	public List<PVector> getCylinderCamberlinePoints(){
		return _pCylinderCamberlinePoints;
	}

	public List<PVector> getTailUpperPoints(){
		return _pTailUpperPoints;
	}

	public List<PVector> getTailLowerPoints(){
		return _pTailLowerPoints;
	}
	
	public List<PVector> getTailCamberlinePoints(){
		return _pTailCamberlinePoints;
	}
	

	public FuselageCurvesSideView(
				Double l_N, Double h_N, Double l_C, Double l_F, Double h_T, Double half_d_C, Double a,
				int nNose, int nCylinder, int nTail
				) 
	{
		
		// NOTE: origin (0,0) is located right in the middle of the foremost cylindrical section, 
		//       i.e. half way the fuselage max height	
		
		// assign no. points
		_nPointsNose = nNose;
		_deltaUNose = (float) (1.0/(_nPointsNose-1));

		_nPointsCylinder = nCylinder;
		_deltaUCylinder = (float) (1.0/(_nPointsCylinder-1));

		_nPointsTail = nTail;
		_deltaUTail = (float) (1.0/(_nPointsTail-1));

		_sweepConstructionAngle = Amount.valueOf(30.0,NonSI.DEGREE_ANGLE);
		
		
		_l_F = l_F;
		_l_N = l_N;
		_l_C = l_C;
		_l_T = _l_F - _l_N - _l_C;
		_half_d_C = half_d_C;
		_d_C = 2.0*_half_d_C;
		_h_N = h_N;
		_h_T = h_T;
		//_a   =   a;

		// fixed number of control points: 6

		// Point for upper nose curve 
		Double dxNoseUpper            = 0.20*_l_N;
		Double ratioHeightLengthUpper = (_half_d_C - _h_N)/ _l_N;
		Double z2u                    = _half_d_C - ratioHeightLengthUpper*(_l_N - dxNoseUpper);
		Double dzNoseUpper            = z2u - _h_N;

		// Parameters for lower nose curve 
		Double dxNoseLower            = 0.5*_l_N;
		Double ratioHeightLengthLower = (_half_d_C + _h_N)/ _l_N;
		Double z2l                    = -(_half_d_C - ratioHeightLengthLower*(_l_N - dxNoseLower));
		Double dzNoseLower            = _h_N - z2l;

		// Parameters for upper tail curve 
		Double dxTailUpper = 0.5*_l_T;

		// Parameters for lower tail curve 
		Double dxTailLower = 0.30*_l_T; // 0.15* 
		Double dyTailLower = _h_T;

		// Upper Nose Control Points
		
		PVector p0u = new PVector((float)(0.0), (float)(1.0*_h_N));
		PVector p1u = new PVector((float)(0.0), (float)(1.0*_h_N + 0.5*dzNoseUpper));
		PVector p2u = new PVector((float)(0.0), (float)(1.0*_h_N + 1.0*dzNoseUpper));
		
		PVector p3u = new PVector((float) (1.0*_l_N-1.0*dxNoseUpper),(float) (_half_d_C));//((1.0-_a)*_d_C));
		PVector p4u = new PVector((float) (1.0*_l_N-0.5*dxNoseUpper),(float) (_half_d_C));               // ((1.0-_a)*_d_C));
		PVector p5u = new PVector((float)                 (1.0*_l_N),(float) (_half_d_C));               //((1.0-_a)*_d_C));

		_cNoseUpperPoints.add(p0u);
		_cNoseUpperPoints.add(p1u);
		_cNoseUpperPoints.add(p2u);
		_cNoseUpperPoints.add(p3u);
		_cNoseUpperPoints.add(p4u);
		_cNoseUpperPoints.add(p5u);

		// System.out.println(_cNoseUpperPoints);

		// Lower Nose Control Points
		
		PVector p0l = new PVector((float)(0.0), (float)(1.0*_h_N));
		PVector p1l = new PVector((float)(0.0), (float)(1.0*_h_N - 0.5*dzNoseLower));
		PVector p2l = new PVector((float)(0.0), (float)(1.0*_h_N - 1.0*dzNoseLower));
		
		PVector p3l = new PVector((float) (1.0*_l_N-1.0*dxNoseLower),(float) -(_half_d_C));
		PVector p4l = new PVector((float) (1.0*_l_N-0.5*dxNoseLower),(float) -(_half_d_C));
		PVector p5l = new PVector((float)                 (1.0*_l_N),(float) -(_half_d_C));

		_cNoseLowerPoints.add(p0l);
		_cNoseLowerPoints.add(p1l);
		_cNoseLowerPoints.add(p2l);
		_cNoseLowerPoints.add(p3l);
		_cNoseLowerPoints.add(p4l);
		_cNoseLowerPoints.add(p5l);
		
		// Upper Cylinder Control Points
		PVector p6u = new PVector((float) (1.0*_l_N       ),(float) (_half_d_C));
		PVector p7u = new PVector((float) (0.5*(_l_N+_l_C)),(float) (_half_d_C));
		PVector p8u = new PVector((float) (1.0*(_l_N+_l_C)),(float) (_half_d_C));
		
		_cCylinderUpperPoints.add(p6u);
		_cCylinderUpperPoints.add(p7u);
		_cCylinderUpperPoints.add(p8u);
		
		// Lower Cylinder Control Points
		PVector p6l = new PVector((float) (1.0*_l_N       ),(float)-(_half_d_C));//  (-_a*_d_C));
		PVector p7l = new PVector((float) (0.5*(_l_N+_l_C)),(float)-(_half_d_C));//  (-_a*_d_C));
		PVector p8l = new PVector((float) (1.0*(_l_N+_l_C)),(float)-(_half_d_C));//  (-_a*_d_C));

		_cCylinderLowerPoints.add(p6l);
		_cCylinderLowerPoints.add(p7l);
		_cCylinderLowerPoints.add(p8l);

		// Upper Tail Control Points
		PVector p9u  = new PVector((float) (1.0*(_l_F-_l_T            )),(float)(_half_d_C));
		PVector p10u = new PVector((float) (1.0*(_l_F-_l_T+0.3*dxTailUpper)),(float)(_half_d_C)); // TODO: update drawings in help 
		PVector p11u = new PVector((float) (1.0*(_l_F-_l_T+dxTailUpper)),(float)(_half_d_C));
		PVector p12u = new PVector((float) (1.0*_l_F                   ),(float)(_half_d_C));
		PVector p13u = new PVector((float) (1.0*_l_F                   ),(float) (1.0*_h_T + 0.40*Math.abs(_half_d_C-_h_T))); // TODO: update drawings in help
		PVector p14u = new PVector((float) (1.0*_l_F                   ),(float) (1.0*_h_T)); // dyTailLower = _h_T 

		_cTailUpperPoints.add(p9u);
		_cTailUpperPoints.add(p10u);
		_cTailUpperPoints.add(p11u);
		_cTailUpperPoints.add(p12u);
		_cTailUpperPoints.add(p13u);
		_cTailUpperPoints.add(p14u);

		// Lower Tail Control Points ** dyTailLower = _h_T 
		PVector p9l  = new PVector((float) (1.0*(_l_F-_l_T            )),(float)-(_half_d_C));
		PVector p10l = new PVector((float) (1.0*(_l_F-_l_T+0.3*dxTailLower)),(float)-(_half_d_C)); // TODO: update drawings in help
		PVector p11l = new PVector((float) (1.0*(_l_F-_l_T+dxTailLower)),(float)-(_half_d_C));

		
		// reassign _sweepConstructionAngle
		double angle = Math.atan((_half_d_C + _h_T)/_l_T);
		_sweepConstructionAngle = Amount.valueOf(angle,SI.RADIAN);
		
		float b = (float) (_l_T - dxTailLower);
		float x12 = (float) (_l_F - 0.5*b);
		float y12 = (float) (-_half_d_C + 0.5*b*Math.tan(_sweepConstructionAngle.doubleValue(SI.RADIAN)));
		PVector p12l = new PVector(x12, y12); // TODO: update drawings in help

		float x13 = (float) _l_F;
		float y13 = (float) (-_half_d_C + b*Math.tan(_sweepConstructionAngle.doubleValue(SI.RADIAN)));
		PVector p13l = new PVector(x13, y13); // TODO: update drawings in help

		float x14 = x13;
		float y15 = (float) dyTailLower.doubleValue();
		float y14 = (float) (y15 - 0.5*Math.abs(y15 - y13));
		PVector p14l = new PVector(x14, y14); // TODO: update drawings in help
		
		float x15 = x14;
		PVector p15l = new PVector(x15, y15);

		_cTailLowerPoints.add(p9l);
		_cTailLowerPoints.add(p10l);
		_cTailLowerPoints.add(p11l);
		_cTailLowerPoints.add(p12l);
		_cTailLowerPoints.add(p13l);
		_cTailLowerPoints.add(p14l);
		_cTailLowerPoints.add(p15l);

		// generate points
		bezier(); // TODO: use Processing library for this

	}
	
	public FuselageCurvesSideView(Double l_N, Double h_N, Double l_C, Double l_F,Double h_T, Double half_d_C,Double a) {
		this(
				l_N, h_N, l_C, l_F,h_T, half_d_C, a,
				20, 4, 20 // default no. points
			);
	}

	public void bezier() {
		// basic curves
		generalBezierNoseUpper();
		generalBezierNoseLower();
		generalBezierCylinderUpper();
		generalBezierCylinderLower();
		generalBezierTailLower();
		generalBezierTailUpper();
		// camber-lines
		calculateCamberlines();
		
	}
	
	private void calculateCamberlines( )
	{
		for (int i = 0; i < _pNoseUpperPoints.size(); i++) 
		{
			_pNoseCamberlinePoints.add(
					new PVector(
							_pNoseUpperPoints.get(i).x,
							(float) (0.5*(_pNoseUpperPoints.get(i).y + _pNoseLowerPoints.get(i).y))
							)
					);
		}
		for (int i = 0; i < _pCylinderUpperPoints.size(); i++) 
		{
			_pCylinderCamberlinePoints.add(
					new PVector(
							_pCylinderUpperPoints.get(i).x,
							(float) (0.5*(_pCylinderUpperPoints.get(i).y + _pCylinderLowerPoints.get(i).y))
							)
					);
		}
		for (int i = 0; i < _pTailUpperPoints.size(); i++) 
		{
			_pTailCamberlinePoints.add(
					new PVector(
							_pTailUpperPoints.get(i).x,
							(float) (0.5*(_pTailUpperPoints.get(i).y + _pTailLowerPoints.get(i).y))
							)
					);
		}

		
	}// end-of calculateCamberlines
	


	private void generalBezierNoseUpper() {
		int n = _cNoseUpperPoints.size()-1 ;
		PVector pk = new PVector();
//		List<PVector> pNoseUpperPointsBezier = new ArrayList<PVector>();
		for (float u = 0; u <= 1; u +=_deltaUNose) {
			PVector pu = new PVector(0.0f,0.0f,0.0f);
			for (int k = 0; k <= n; k++) {
				pk.set(_cNoseUpperPoints.get(k).x,_cNoseUpperPoints.get(k).y);
				pk.mult(BEZ(k, n, u));
				pu.add(pk);		
			}		
			_pNoseUpperPoints.add(pu);
		}
		
	
	}
	

	private void generalBezierNoseLower() {
		int n = _cNoseLowerPoints.size()-1 ;
		PVector pk = new PVector();
		for (float u = 0; u <= 1; u +=_deltaUNose) {
			PVector pu = new PVector(0.0f,0.0f,0.0f);
			for (int k = 0; k <= n; k++) {
				pk.set(_cNoseLowerPoints.get(k).x,_cNoseLowerPoints.get(k).y);
				pk.mult(BEZ(k, n, u));
				pu.add(pk);		
			}		
			_pNoseLowerPoints.add(pu);
		
		}
	}

	private void generalBezierCylinderUpper() {
		int n = _cCylinderUpperPoints.size()-1 ;
		PVector pk = new PVector();
		for (float u = 0; u <= 1; u +=_deltaUCylinder) {
			PVector pu = new PVector(0.0f,0.0f,0.0f);
			for (int k = 0; k <= n; k++) {
				pk.set(_cCylinderUpperPoints.get(k).x,_cCylinderUpperPoints.get(k).y);
				pk.mult(BEZ(k, n, u));
				pu.add(pk);		
			}		
			_pCylinderUpperPoints.add(pu);
		}
	}
	
	private void generalBezierCylinderLower() {
		int n = _cCylinderLowerPoints.size()-1 ;
		PVector pk = new PVector();
		for (float u = 0; u <= 1; u +=_deltaUCylinder) {
			PVector pu = new PVector(0.0f,0.0f,0.0f);
			for (int k = 0; k <= n; k++) {
				pk.set(_cCylinderLowerPoints.get(k).x,_cCylinderLowerPoints.get(k).y);
				pk.mult(BEZ(k, n, u));
				pu.add(pk);		
			}		
			_pCylinderLowerPoints.add(pu);
		}
	}
	
	
	private void generalBezierTailUpper() {
		int n =  _cTailUpperPoints.size()-1 ;
		PVector pk = new PVector();
		for (float u = 0; u <= 1; u +=_deltaUTail) {
			PVector pu = new PVector(0.0f,0.0f,0.0f);
			for (int k = 0; k <= n; k++) {
				pk.set( _cTailUpperPoints.get(k).x, _cTailUpperPoints.get(k).y);
				pk.mult(BEZ(k, n, u));
				pu.add(pk);		
			}		
			_pTailUpperPoints.add(pu);
		}
	}


	private void generalBezierTailLower() {
		int n = _cTailLowerPoints.size()-1 ;
		PVector pk = new PVector();
		for (float u = 0; u <= 1; u +=_deltaUTail) {
			PVector pu = new PVector(0.0f,0.0f,0.0f);
			for (int k = 0; k <= n; k++) {
				pk.set(_cTailLowerPoints.get(k).x,_cTailLowerPoints.get(k).y);
				pk.mult(BEZ(k, n, u));
				pu.add(pk);		
			}		
			_pTailLowerPoints.add(pu);
		}
	}

	public static float BEZ(int k, int n, float u) {
		if (k == n) {
			return (float) Math.pow(u, k);
		} else if (k == 0) {
			return (float) Math.pow((1 - u), (n));
		} else {
			float bez = ArithmeticUtils.binomialCoefficient(n, k);
			bez = (float) (bez * Math.pow(u, k));
			bez = (float) (bez * Math.pow((1 - u), (n - k)));
			return bez;
		}
	}
	
	public void setNPoints (int nNose, int nCylinder, int nTail){
		if (nNose > 0) {
			_nPointsNose = nNose; 
			_deltaUNose = (float) (1.0/(_nPointsNose - 1));
			_pNoseUpperPoints.clear();
			_pNoseLowerPoints.clear();
//			bezier();
		}
		if (nCylinder > 0) {
			_nPointsCylinder = nCylinder; 
			_deltaUCylinder = (float) (1.0/(_nPointsCylinder - 1));
			_pCylinderUpperPoints.clear();
			_pCylinderLowerPoints.clear();
//			bezier();
		}
		if (nTail > 0) {
			_nPointsTail = nTail; 
			_deltaUTail = (float) (1.0/(_nPointsTail - 1));
			_pTailUpperPoints.clear();
			_pTailLowerPoints.clear();
//			bezier();
		}
		bezier();
	}
	public void setNPointsNose (int nNose){
		if (nNose > 0) {
			_nPointsNose = nNose; 
			_deltaUNose = (float) (1.0/(_nPointsNose - 1));
			_pNoseUpperPoints.clear();
			_pNoseLowerPoints.clear();
			bezier();
		}
	}
	public void setNPointsCylinder (int nCylinder){
		if (nCylinder > 0) {
			_nPointsCylinder = nCylinder; 
			_deltaUCylinder = (float) (1.0/(_nPointsCylinder - 1));
			_pCylinderUpperPoints.clear();
			_pCylinderLowerPoints.clear();
			bezier();
		}
	}
	public void setNPointsTail (int nTail){
		if (nTail > 0) {
			_nPointsTail = nTail; 
			_deltaUTail = (float) (1.0/(_nPointsTail - 1));
			_pTailUpperPoints.clear();
			_pTailLowerPoints.clear();
			bezier();
		}
	}
	
} // end-of-class MyFuselageCurvesSideView
