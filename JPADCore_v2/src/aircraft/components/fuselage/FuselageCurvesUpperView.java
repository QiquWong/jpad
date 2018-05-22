package aircraft.components.fuselage;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.CombinatoricsUtils;

import processing.core.PVector;

public class FuselageCurvesUpperView {

	private Double _l_T, _l_F, _l_N, _l_C, _half_w_B, _w_B;
	
	private List<PVector> _cNoseUpperPoints     = new ArrayList<PVector>();
	private List<PVector> _cNoseLowerPoints     = new ArrayList<PVector>();
	private List<PVector> _cCylinderLowerPoints = new ArrayList<PVector>();
	private List<PVector> _cCylinderUpperPoints = new ArrayList<PVector>();
	private List<PVector> _cTailLowerPoints     = new ArrayList<PVector>();
	private List<PVector> _cTailUpperPoints     = new ArrayList<PVector>();
	
	
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

	public List<PVector> getNoseUpperPoints(){
		return _pNoseUpperPoints;
	}

	public List<PVector> getNoseLowerPoints(){
		return _pNoseLowerPoints;
	}

	public List<PVector> getCylinderUpperPoints(){
		return _pCylinderUpperPoints;
	}

	public List<PVector> getCylinderLowerPoints(){
		return _pCylinderLowerPoints;
	}

	public List<PVector> getTailUpperPoints(){
		return _pTailUpperPoints;
	}

	public List<PVector> getTailLowerPoints(){
		return _pTailLowerPoints;
	}
	
	
	public FuselageCurvesUpperView(
				Double l_N, Double l_C, Double l_F, Double half_w_B,
				int nNose, int nCylinder, int nTail
				) 
	{
		// assign no. points
		_nPointsNose = nNose;
		_deltaUNose = (float) (1.0/(_nPointsNose-1));

		_nPointsCylinder = nCylinder;
		_deltaUCylinder = (float) (1.0/(_nPointsCylinder-1));

		_nPointsTail = nTail;
		_deltaUTail = (float) (1.0/(_nPointsTail-1));

		_l_F = l_F;
		_l_N = l_N;
		_l_C = l_C;
		_l_T = _l_F - _l_N - _l_C;
		_half_w_B = half_w_B;
		setWB(2.0*_half_w_B);

		// fixed number of control points: 6

		// Parameters nose curve 
		Double dxNose               = 0.3*_l_N;
		Double ratioWidthNoseLength = _half_w_B / _l_N;
		Double dyNose               = _half_w_B - ratioWidthNoseLength*(_l_N - dxNose);

		// Parameters tail curve 
		Double dxTail               = 0.4*_l_T; // TODO: parametrize!
		Double dyTail               = 0.30*_half_w_B; // TODO: parametrize!
		
		//  Nose Control Points, Right side

		PVector p0u = new PVector((float)                  (0.0),(float)            (0.0));
		PVector p1u = new PVector((float)                  (0.0),(float)     (0.5*dyNose));
		PVector p2u = new PVector((float)                  (0.0),(float)     (1.0*dyNose));
		PVector p3u = new PVector((float)  (1.0*_l_N-1.0*dxNose),(float)  (1.0*_half_w_B));
		PVector p4u = new PVector((float)  (1.0*_l_N-0.5*dxNose),(float)  (1.0*_half_w_B));
		PVector p5u = new PVector((float)             (1.0*_l_N),(float)  (1.0*_half_w_B));

		_cNoseUpperPoints.add(p0u);
		_cNoseUpperPoints.add(p1u);
		_cNoseUpperPoints.add(p2u);
		_cNoseUpperPoints.add(p3u);
		_cNoseUpperPoints.add(p4u);
		_cNoseUpperPoints.add(p5u);

		// Lower Nose Control Point, Left side

		PVector p0l = new PVector((float)                      (0.0),(float)            (0.0));
		PVector p1l = new PVector((float)                      (0.0),(float)    (-0.5*dyNose));
		PVector p2l = new PVector((float)                      (0.0),(float)    (-1.0*dyNose));
		PVector p3l = new PVector((float)      (1.0*_l_N-1.0*dxNose),(float) (-1.0*_half_w_B));
		PVector p4l = new PVector((float)      (1.0*_l_N-0.5*dxNose),(float) (-1.0*_half_w_B));
		PVector p5l = new PVector((float)                 (1.0*_l_N),(float) (-1.0*_half_w_B));

		_cNoseLowerPoints.add(p0l);
		_cNoseLowerPoints.add(p1l);
		_cNoseLowerPoints.add(p2l);
		_cNoseLowerPoints.add(p3l);
		_cNoseLowerPoints.add(p4l);
		_cNoseLowerPoints.add(p5l);

		// Cylinder Control Points, Right side

		PVector p6u = new PVector((float)                (1.0*(_l_N)),(float)  (1.0*_half_w_B));
		PVector p7u = new PVector((float)           (0.5*(_l_N+_l_C)),(float)  (1.0*_half_w_B));
		PVector p8u = new PVector((float)           (1.0*(_l_N+_l_C)),(float)  (1.0*_half_w_B));

		_cCylinderUpperPoints.add(p6u);
		_cCylinderUpperPoints.add(p7u);
		_cCylinderUpperPoints.add(p8u);

		// Cylinder Control Points, Left side
		
		PVector p6l = new PVector((float)                (1.0*(_l_N)),(float)  (-1.0*_half_w_B));
		PVector p7l = new PVector((float)           (0.5*(_l_N+_l_C)),(float)  (-1.0*_half_w_B));
		PVector p8l = new PVector((float)           (1.0*(_l_N+_l_C)),(float)  (-1.0*_half_w_B));

		_cCylinderLowerPoints.add(p6l);
		_cCylinderLowerPoints.add(p7l);
		_cCylinderLowerPoints.add(p8l);

		// Tail Control Points, Right side

		PVector p9u  = new PVector((float)          (1.0*(_l_F-_l_T)),(float)    (1.0*_half_w_B));                       
		PVector p10u = new PVector((float) (1.0*((_l_F-_l_T)+0.3*dxTail)),(float)    (1.0*_half_w_B)); // TODO: update drawings in help                            
		PVector p11u = new PVector((float) (1.0*((_l_F-_l_T)+dxTail)),(float)    (1.0*_half_w_B));                            
		PVector p12u = new PVector((float)                 (1.0*_l_F),(float)       (1.0*dyTail));                           
		PVector p13u = new PVector((float)                 (1.0*_l_F),(float)     (1.0*dyTail)/2);
		PVector p14u = new PVector((float)                 (1.0*_l_F),(float)              (0.0));

		_cTailUpperPoints.add(p9u);
		_cTailUpperPoints.add(p10u);
		_cTailUpperPoints.add(p11u);
		_cTailUpperPoints.add(p12u);
		_cTailUpperPoints.add(p13u);
		_cTailUpperPoints.add(p14u);

		// Lower Tail Control Points, Left side

		PVector p9l  = new PVector((float)          (1.0*(_l_F-_l_T)),(float)    (-1.0*_half_w_B));                       
		PVector p10l = new PVector((float) (1.0*((_l_F-_l_T)+0.3*dxTail)),(float)    (-1.0*_half_w_B));  // TODO: update drawings in help                           
		PVector p11l = new PVector((float) (1.0*((_l_F-_l_T)+dxTail)),(float)    (-1.0*_half_w_B));                            
		PVector p12l = new PVector((float)                 (1.0*_l_F),(float)       (-1.0*dyTail));                           
		PVector p13l = new PVector((float)                 (1.0*_l_F),(float)     (-1.0*dyTail)/2);

		_cTailLowerPoints.add(p9l);
		_cTailLowerPoints.add(p10l);
		_cTailLowerPoints.add(p11l);
		_cTailLowerPoints.add(p12l);
		_cTailLowerPoints.add(p13l);

		bezier();

	}

	public FuselageCurvesUpperView(Double l_N, Double h_N, Double l_C, Double l_F, Double half_w_B) {
		this(
				l_N, l_C, l_F, half_w_B, 
				20, 4, 20 // default no. points
			);
	}
	
	
	public void bezier() {

		generalBezierNoseUpper();
		generalBezierNoseLower();
		generalBezierCylinderUpper();
		generalBezierCylinderLower();
		generalBezierTailLower();
		generalBezierTailUpper();
	}

	private void generalBezierNoseUpper() {
		int n = _cNoseUpperPoints.size()-1 ;
		PVector pk = new PVector();
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
			// float bez = ArithmeticUtils.binomialCoefficient(n, k); deprecated
			float bez = CombinatoricsUtils.binomialCoefficient(n, k);
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

		}
		if (nCylinder > 0) {
			_nPointsCylinder = nCylinder; 
			_deltaUCylinder = (float) (1.0/(_nPointsCylinder - 1));
			_pCylinderUpperPoints.clear();
			_pCylinderLowerPoints.clear();

		}
		if (nTail > 0) {
			_nPointsTail = nTail; 
			_deltaUTail = (float) (1.0/(_nPointsTail - 1));
			_pTailUpperPoints.clear();
			_pTailLowerPoints.clear();

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

	public Double getWB() {
		return _w_B;
	}

	public void setWB(Double _w_B) {
		this._w_B = _w_B;
	}

} // end-of-class MyFuselageCurvesUpperView

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


