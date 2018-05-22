package aircraft.components.fuselage;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.jscience.physics.amount.Amount;

import processing.core.PVector;

public class FuselageCurvesSection {

	// NOTE: this is the front view, i.e. looking from -X towards +X
	// NOTE: x ==> Y+, left side of fuselage section when seen from X- to X+
	// NOTE: y ==> Z+, top side of fuselage section when seen from X- to X+
	// NOTE: origin (0,0) is located right in the middle of the cylindrical section, 
	//       i.e. half way the fuselage max-width and half way the max height	


	private Double _x = -999.; // the X-station
	public void set_x(Double val){_x = val;}
	public Double get_x(){return _x;}
	
	private Double _h_f, _a, _rho_Upper, _rho_Lower, _w_f;
	
	private List<PVector> _cSectionUpperLeftPoints  = new ArrayList<PVector>();
	private List<PVector> _cSectionLowerLeftPoints = new ArrayList<PVector>();

	private List<PVector> _cSectionUpperRightPoints = new ArrayList<PVector>();
	private List<PVector> _cSectionLowerRightPoints = new ArrayList<PVector>();

	private int _nPointsUpperSection = 20;
	private float _deltaUUpperSection = (float) (1.0/(_nPointsUpperSection-1));

	private int _nPointsLowerSection = 20;
	private float _deltaULowerSection = (float) (1.0/(_nPointsLowerSection-1));

	private List<PVector> _pSectionUpperLeftPoints = new ArrayList<PVector>();
	private List<PVector> _pSectionLowerLeftPoints = new ArrayList<PVector>();

	private List<PVector> _pSectionUpperRightPoints = new ArrayList<PVector>();
	private List<PVector> _pSectionLowerRightPoints = new ArrayList<PVector>();

	public List<PVector> getSectionUpperLeftPoints(){
		return _pSectionUpperLeftPoints;
	}

	public List<PVector> getSectionUpperRightPoints(){
		return _pSectionUpperRightPoints;
	}

	public List<PVector> getSectionLowerLeftPoints(){
		return _pSectionLowerLeftPoints;
	}

	public List<PVector> getSectionLowerRightPoints(){
		return _pSectionLowerRightPoints;
	}

	public FuselageCurvesSection(
			Double w_B, Double h_B, Double a, Double rhoUpper, Double rhoLower, // lengths & parameters
			int nUpper, int nLower                                              // num. points
			)
	{

		setSectionParameters(
				w_B, h_B, a, rhoUpper, rhoLower, // lengths & parameters
				nUpper, nLower                   // num. points
				);

	}

	public FuselageCurvesSection(Double w_B, Double h_B, Double a, Double rhoUpper, Double rhoLower)
	{
		this(
				w_B, h_B, a, rhoUpper, rhoLower,
				10, 10 // default no. points
				);
	}

	public void bezier() {

		upperSectionBezier();
		simmetryUpperSectionBezier();
		lowerSectionBezier();
		simmetryLowerSectionBezier();

	}

	private void upperSectionBezier() {
		int n = _cSectionUpperLeftPoints.size()-1 ;
		PVector pk = new PVector();
		for (float u = 0; u <= 1; u +=_deltaUUpperSection) {
			PVector pu = new PVector(0.0f,0.0f,0.0f);
			for (int k = 0; k <= n; k++) {
				pk.set(_cSectionUpperLeftPoints.get(k).x,_cSectionUpperLeftPoints.get(k).y);
				pk.mult(BEZ(k, n, u));
				pu.add(pk);		
			}		
			_pSectionUpperLeftPoints.add(pu);
		}
	}


	private void simmetryUpperSectionBezier() {
		int n = _cSectionUpperRightPoints.size()-1 ;
		PVector pk = new PVector();
		for (float u = 0; u <= 1; u +=_deltaUUpperSection) {
			PVector pu = new PVector(0.0f,0.0f,0.0f);
			for (int k = 0; k <= n; k++) {
				pk.set(_cSectionUpperRightPoints.get(k).x,_cSectionUpperRightPoints.get(k).y);
				pk.mult(BEZ(k, n, u));
				pu.add(pk);		
			}		
			_pSectionUpperRightPoints.add(pu);
		}
	}



	private void lowerSectionBezier() {
		int n = _cSectionLowerRightPoints.size()-1 ;
		PVector pk = new PVector();
		for (float u = 0; u <= 1; u +=_deltaULowerSection) {
			PVector pu = new PVector(0.0f,0.0f,0.0f);
			for (int k = 0; k <= n; k++) {
				pk.set(_cSectionLowerRightPoints.get(k).x,_cSectionLowerRightPoints.get(k).y);
				pk.mult(BEZ(k, n, u));
				pu.add(pk);		
			}		
			_pSectionLowerRightPoints.add(pu);
		}
	}

	private void simmetryLowerSectionBezier() {
		int n = _cSectionLowerLeftPoints.size()-1 ;
		PVector pk = new PVector();
		for (float u = 0; u <= 1; u +=_deltaULowerSection) {
			PVector pu = new PVector(0.0f,0.0f,0.0f);
			for (int k = 0; k <= n; k++) {
				pk.set(_cSectionLowerLeftPoints.get(k).x,_cSectionLowerLeftPoints.get(k).y);
				pk.mult(BEZ(k, n, u));
				pu.add(pk);		
			}		
			_pSectionLowerLeftPoints.add(pu);
		}
	}


	public static float BEZ(int k, int n, float u) {
		if (k == n) {
			return (float) Math.pow(u, k);
		} else if (k == 0) {
			return (float) Math.pow((1 - u), (n));
		} else {
			// float bez = ArithmeticUtils.binomialCoefficient(n, k); // deprecated
			float bez = CombinatoricsUtils.binomialCoefficient(n, k);
			bez = (float) (bez * Math.pow(u, k));
			bez = (float) (bez * Math.pow((1 - u), (n - k)));
			return bez;
		}
	}


	public void setSectionNPoints(int nUpper,int nLower){
		if (nUpper>0){
			_nPointsUpperSection=nUpper;	
			_deltaUUpperSection= (float) (1.0/(_nPointsUpperSection-1));
			_pSectionUpperLeftPoints.clear();
			_pSectionUpperRightPoints.clear();
		}

		if(nLower>0){
			_nPointsLowerSection = nLower;
			_deltaULowerSection= (float) (1.0/(_nPointsLowerSection-1));
			_pSectionLowerLeftPoints.clear();
			_pSectionLowerRightPoints.clear();
		}
		bezier();
	}

	// Utility function
	public FuselageCurvesSection translateZ(double dZ)
	{
		// NOTE: x ==> Y+, left side of fuselage section when seen from X- to X+
		// NOTE: y ==> Z+, top side of fuselage section when seen from X- to X+
		
//		System.out.println(_cSectionUpperLeftPoints);
		for ( PVector p : _cSectionUpperLeftPoints )
			p.add( (float)0., (float)dZ, (float)0.);
//		System.out.println(_cSectionUpperLeftPoints);
		for ( PVector p : _cSectionLowerLeftPoints )
			p.add( (float)0., (float)dZ, (float)0.);
		for ( PVector p : _cSectionUpperRightPoints )
			p.add( (float)0., (float)dZ, (float)0.);
		for ( PVector p : _cSectionLowerRightPoints )
			p.add( (float)0., (float)dZ, (float)0.);

		for ( PVector p : _pSectionUpperLeftPoints )
			p.add( (float)0., (float)dZ, (float)0.);
		for ( PVector p : _pSectionLowerLeftPoints )
			p.add( (float)0., (float)dZ, (float)0.);
		for ( PVector p : _pSectionUpperRightPoints )
			p.add( (float)0., (float)dZ, (float)0.);
		for ( PVector p : _pSectionLowerRightPoints )
			p.add( (float)0., (float)dZ, (float)0.);

		return this;
	}

	public void setSectionParameters(
			Double w_B, Double h_B, Double a, Double rhoUpper, Double rhoLower, // lengths & parameters
			int nUpper, int nLower                                              // num. points
			)
	{

		// clear control points
		_cSectionUpperRightPoints.clear();
		_cSectionUpperLeftPoints.clear();
		_cSectionLowerRightPoints.clear();
		_cSectionLowerLeftPoints.clear();
		
		// clear curve points
		_pSectionUpperRightPoints.clear();
		_pSectionUpperLeftPoints.clear();
		_pSectionLowerRightPoints.clear();
		_pSectionLowerLeftPoints.clear();
		
		_h_f       = (double) Math.round(h_B*1000)/1000;		
		_w_f       = (double) Math.round(w_B*1000)/1000;
		_a         = (double) Math.round(a*1000)/1000;
		_rho_Upper = (double) Math.round(rhoUpper*1000)/1000;
		_rho_Lower = (double) Math.round(rhoLower*1000)/1000;

		// counter-clockwise from (-width/2, 0)

		// Upper Right Section Control Points

		PVector p0ur = new PVector((float)               -(_w_f/2),(float)                    (0.0));
		PVector p1ur = new PVector((float)               -(_w_f/2),(float) (_rho_Upper*(1-_a)*_h_f));
		PVector p2ur = new PVector((float)               -(_w_f/2),(float)            ((1-_a)*_h_f));
		PVector p3ur = new PVector((float)  -(_rho_Upper*(_w_f/2)),(float)            ((1-_a)*_h_f));
		PVector p4ur = new PVector((float)                   (0.0),(float)            ((1-_a)*_h_f));

//		// adjust origin, translate downwards
		p0ur.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p1ur.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p2ur.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p3ur.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p4ur.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		
		_cSectionUpperRightPoints.add(p0ur);
		_cSectionUpperRightPoints.add(p1ur);
		_cSectionUpperRightPoints.add(p2ur);
		_cSectionUpperRightPoints.add(p3ur);
		_cSectionUpperRightPoints.add(p4ur);

		// Upper Left Section (Symmetric) Control Points

		PVector p0ul = new PVector((float)         (0.0*(_w_f/2)),(float)            ((1-_a)*_h_f));
		PVector p1ul = new PVector((float)  (_rho_Upper*(_w_f/2)),(float)            ((1-_a)*_h_f));
		PVector p2ul = new PVector((float)         (1.0*(_w_f/2)),(float)            ((1-_a)*_h_f));
		PVector p3ul = new PVector((float)         (1.0*(_w_f/2)),(float) (_rho_Upper*(1-_a)*_h_f));
		PVector p4ul = new PVector((float)         (1.0*(_w_f/2)),(float)                    (0.0));
//
//		// adjust origin, translate downwards
		p0ul.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p1ul.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p2ul.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p3ul.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p4ul.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );

		_cSectionUpperLeftPoints.add(p0ul);
		_cSectionUpperLeftPoints.add(p1ul);
		_cSectionUpperLeftPoints.add(p2ul);
		_cSectionUpperLeftPoints.add(p3ul);
		_cSectionUpperLeftPoints.add(p4ul);

		// Lower Left Section Control Points

		PVector p0ll = new PVector((float)        (1.0*(_w_f/2)),(float)                 (0.0));
		PVector p1ll = new PVector((float)        (1.0*(_w_f/2)),(float) -(_rho_Lower*_a*_h_f));
		PVector p2ll = new PVector((float)        (1.0*(_w_f/2)),(float)            -(_a*_h_f));
		PVector p3ll = new PVector((float) (_rho_Lower*(_w_f/2)),(float)            -(_a*_h_f));
		PVector p4ll = new PVector((float)                 (0.0),(float)            -(_a*_h_f));

//		// adjust origin, translate downwards
		p0ll.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p1ll.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p2ll.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p3ll.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p4ll.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		
		_cSectionLowerLeftPoints.add(p0ll);
		_cSectionLowerLeftPoints.add(p1ll);
		_cSectionLowerLeftPoints.add(p2ll);
		_cSectionLowerLeftPoints.add(p3ll);
		_cSectionLowerLeftPoints.add(p4ll);

		// Lower Right Section (Symmetric) Control Points

		PVector p0lr = new PVector((float)                  (0.0),(float)            -(_a*_h_f));
		PVector p1lr = new PVector((float) -(_rho_Lower*(_w_f/2)),(float)            -(_a*_h_f));
		PVector p2lr = new PVector((float)        -(1.0*(_w_f/2)),(float)            -(_a*_h_f));
		PVector p3lr = new PVector((float)        -(1.0*(_w_f/2)),(float) -(_rho_Lower*_a*_h_f));
		PVector p4lr = new PVector((float)        -(1.0*(_w_f/2)),(float)                 (0.0));
//
//		// adjust origin, translate downwards
		p0lr.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p1lr.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p2lr.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p3lr.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		p4lr.add(new PVector( (float)0.0, (float)(-(0.5-_a)*_h_f)) );
		
		_cSectionLowerRightPoints.add(p0lr);
		_cSectionLowerRightPoints.add(p1lr);
		_cSectionLowerRightPoints.add(p2lr);
		_cSectionLowerRightPoints.add(p3lr);
		_cSectionLowerRightPoints.add(p4lr);

		// generate points
		bezier();

	}

	public Double get_h_f() {return _h_f;}
	public Amount<Length> get_Len_Height() {return Amount.valueOf(_h_f, SI.METRE);}
	
	public Double get_Width() {return _w_f;}
	public Amount<Length> get_w_f() {return Amount.valueOf(_w_f, SI.METRE);}
	
	public Double get_LowerToTotalHeightRatio() {return _a;}
	public Double get_RhoUpper() {return _rho_Upper;}
	public Double get_RhoLower() {return _rho_Lower;}
	
	public List<PVector> getSectionLeftPoints(){
		List<PVector> pLeftPoints = new ArrayList<PVector>();
		// add all points from upper-left
		pLeftPoints.addAll(_pSectionUpperLeftPoints);
		// add points from lower-left, starting from 2nd
		pLeftPoints
			.addAll(
					_pSectionLowerLeftPoints
					.subList(
							1, _pSectionLowerLeftPoints.size()
					)
			);
		return pLeftPoints;
	}
	

} // end of MyFuselageCurvesSection class

