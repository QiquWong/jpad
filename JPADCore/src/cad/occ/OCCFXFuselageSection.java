package cad.occ;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.unit.SI;

import org.fxyz.shapes.primitives.helper.InterpolateBezier;

import aircraft.components.fuselage.Fuselage;
import aircraft.components.fuselage.MyFuselageCurvesSection;

public class OCCFXFuselageSection {

	private List<org.fxyz.geometry.Point3D> _knots = new ArrayList<org.fxyz.geometry.Point3D>();
	// the interpolating bezier
	private InterpolateBezier _interpolate;

	private Double _x;
	private int _idxX = 0;
	public OCCFXFuselageSection(List<org.fxyz.geometry.Point3D> pts) { 
		_knots = pts;
		_interpolate = new InterpolateBezier(_knots);
	}
	public OCCFXFuselageSection(Fuselage fuselage, int idx) {
		if (fuselage != null) {
			_idxX = idx; 
			// check range
			if (
					(_idxX >= fuselage.NUM_SECTIONS_YZ) ||
					(_idxX < 0)
					) {
				_idxX = fuselage.IDX_SECTION_YZ_CYLINDER_1;
			}
			_x = fuselage.get_sectionsYZStations().get(_idxX).doubleValue(SI.METER);
			MyFuselageCurvesSection section = fuselage.get_sectionsYZ().get(_idxX);
			_knots = section.getSectionLeftPoints() // Gives PVectors
					.stream()
					.map(pVector -> { // <===== LAMBDA
						org.fxyz.geometry.Point3D p3D = new org.fxyz.geometry.Point3D(0f, 0f, 0f);
						p3D.x = _x.floatValue();
						p3D.y = pVector.x;
						p3D.z = pVector.y;
						return p3D;
					}) // want org.fxyz.geometry.Point3D
					.collect(Collectors.toList());
			_interpolate = new InterpolateBezier(_knots);
		}
	}
	public InterpolateBezier getInterpolateBezier() {
		return _interpolate;
	}
	public List<org.fxyz.geometry.Point3D> getKnots() {
		return _knots;
	}
}
