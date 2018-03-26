package aircraft.components.fuselage;

import java.util.ArrayList;
import java.util.List;

import processing.core.PVector;

// Only half body is meshed. The rest is to be taken by symmetry w.r.t. XZ plane
// Mesh index 'i' runs from nose to tail
// Mesh index 'j' runs azimutally along the generic YZ section (X = const.)
//    from top to right-side to bottom

public class FuselageSurfaceMesh {

	private Fuselage _theFuselage;
	
	private List<List<PVector>> _surfaceMesh = new ArrayList<List<PVector>>();

	// 'ni' stands for number of intervals
	private int _ni_N = 10, _ni_C = 4, _ni_T = 10, _ni_Sec = 10;
	private int _ni_F = _ni_N + _ni_N + _ni_N; 
	
	public FuselageSurfaceMesh(Fuselage fuselage) {

		_theFuselage = fuselage;
		
		// EXPERIMENTAL
		LineMeshYZ lineMesh = new LineMeshYZ(
				_theFuselage,                          // the fuselage object 
				_theFuselage.getSectionsYZ().get(3),  // the section XY
				_theFuselage.getNoseLength(),              // the X-coordinate
				20                                     // number of mesh intervals
				);

	}

	void makeMeshYZ(Double x, int nsec)
	{
		
		// EXPERIMENTAL

	}

	public int get_ni_N() {
		return _ni_N;
	}

	public void set_ni_N(int _ni_N) {
		this._ni_N = _ni_N;
	}
	
	public int get_ni_C() {
		return _ni_C;
	}

	public void set_ni_C(int _ni_C) {
		this._ni_C = _ni_C;
	}

	public int get_ni_T() {
		return _ni_T;
	}

	public void set_ni_T(int _ni_T) {
		this._ni_T = _ni_T;
	}

	public int get_ni_Sec() {
		return _ni_Sec;
	}

	public void set_ni_Sec(int _ni_Sec) {
		this._ni_Sec = _ni_Sec;
	}

	public int get_ni_F() {
		return _ni_F;
	}


}
