package standaloneutils.customdata;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.ComponentEnum;

/** 
 * Holds every useful information about center of gravity.
 * Default unit of measurement: SI meters
 * 
 * @author Lorenzo Attanasio
 */
public class CenterOfGravity {

	/* 
	 * LRF = Local Reference Frame;
	 * BRF = Body Reference Frame;
	 */

	// LRF reference coordinates (guess values)
	private Amount<Length> _xLRFref, _yLRFref, _zLRFref;

	// LRF coordinates
	private Amount<Length> 
	_xLRF = Amount.valueOf(0., SI.METER),
	_yLRF = Amount.valueOf(0., SI.METER),
	_zLRF = Amount.valueOf(0., SI.METER);

	// BRF coordinates
	private Amount<Length> _xBRF, _yBRF, _zBRF; 
	private Double _xMAC, _zMAC;

	// BRF position of the origin of component's LRF
	private Amount<Length> _x0, _y0, _z0;

	private List<Amount<Length>> _coordinatesList = new ArrayList<Amount<Length>>();

	public CenterOfGravity (
			Amount<Length> x, Amount<Length> y, Amount<Length> z,
			Amount<Length> xL, Amount<Length> yL, Amount<Length> zL,
			Amount<Length> xB, Amount<Length> yB, Amount<Length> zB) {

		_x0 = x;
		_y0 = y;
		_z0 = z;

		_xLRF = xL;
		_yLRF = yL;
		_zLRF = zL;

		_xBRF = xB;
		_yBRF = yB;
		_zBRF = zB;

		_coordinatesList.add(_x0);
		_coordinatesList.add(_y0);
		_coordinatesList.add(_z0);
		_coordinatesList.add(_xLRF);
		_coordinatesList.add(_yLRF);
		_coordinatesList.add(_zLRF);
		_coordinatesList.add(_xBRF);
		_coordinatesList.add(_yBRF);
		_coordinatesList.add(_zBRF);

	}

	public CenterOfGravity(List<Amount<Length>> coordinatesList) {
		this(coordinatesList.get(0),
				coordinatesList.get(1),
				coordinatesList.get(2),
				coordinatesList.get(3),
				coordinatesList.get(4),
				coordinatesList.get(5),
				coordinatesList.get(6),
				coordinatesList.get(7),
				coordinatesList.get(8));
	}

	public CenterOfGravity (Amount<Length> x, Amount<Length> y, Amount<Length> z) {
		setLRForigin(x, y, z);
	}

	/** 
	 * Initialize center of gravity with zeros
	 */
	public CenterOfGravity () {
		this(Amount.valueOf(0., SI.METER), Amount.valueOf(0., SI.METER), Amount.valueOf(0., SI.METER),
				Amount.valueOf(0., SI.METER),Amount.valueOf(0., SI.METER),Amount.valueOf(0., SI.METER),
				Amount.valueOf(0., SI.METER),Amount.valueOf(0., SI.METER),Amount.valueOf(0., SI.METER));
		setXLRFref(Amount.valueOf(0., SI.METER));
		setYLRFref(Amount.valueOf(0., SI.METER));
		setZLRFref(Amount.valueOf(0., SI.METER));
	}

	/** 
	 * Set the origin of local reference frame in the body reference frame
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setLRForigin(Amount<Length> x, Amount<Length> y, Amount<Length> z) {
		_x0 = x;
		_y0 = y;
		_z0 = z;
	}
	
	public void calculateCGinBRF(ComponentEnum type) {

		if(type == ComponentEnum.VERTICAL_TAIL) {
			if (_xBRF == null) {
				_xBRF = Amount.valueOf(_xLRF.getEstimatedValue(), SI.METER);
			} else {
				_xBRF = _x0.plus(_xLRF);
			}

			_yBRF = Amount.valueOf(0.0, SI.METER);

			if (_zBRF == null) {
				_zBRF = Amount.valueOf(_yLRF.getEstimatedValue(), SI.METER);
			} else {
				_zBRF = _z0.plus(_yLRF);
			}
		}
		else {
			if (_xBRF == null) {
				_xBRF = Amount.valueOf(_xLRF.getEstimatedValue(), SI.METER);
			} else {
				_xBRF = _x0.plus(_xLRF);
			}

			if (_yBRF == null) {
				_yBRF = Amount.valueOf(_yLRF.getEstimatedValue(), SI.METER);
			} else {
				_yBRF = _y0.plus(_yLRF);
			}

			if (_zBRF == null) {
				_zBRF = Amount.valueOf(_zLRF.getEstimatedValue(), SI.METER);
			} else {
				_zBRF = _z0.plus(_zLRF);
			}
		}
	}


	public void calculateCGinMAC(
			Amount<Length> xB, Amount<Length> yB, Amount<Length>zB, 
			Amount<Length> len) {

		_xMAC = (_xBRF.minus(xB)).divide(len).getEstimatedValue();
		_zMAC = _zBRF.divide(len).getEstimatedValue();
	}

	public CenterOfGravity plus(CenterOfGravity cg) {
		List<Amount<Length>> coordinatesList = new ArrayList<Amount<Length>>();

		for (int i=0; i < _coordinatesList.size(); i++) {
			coordinatesList.add(this._coordinatesList.get(i).plus(cg._coordinatesList.get(i)));
		}

		return new CenterOfGravity(coordinatesList);
	}

	public CenterOfGravity times(double constant) {

		List<Amount<Length>> coordinatesList = new ArrayList<Amount<Length>>();

		for (Amount<Length> x : _coordinatesList) {
			coordinatesList.add(x.times(constant));
		}

		return new CenterOfGravity(coordinatesList);
	}

	public CenterOfGravity divide(double constant) {
		return times(1./constant);
	}

	public Amount<Length> getXLRF() {
		return _xLRF;
	}

	public void setXLRF(Amount<Length> _xLRF) {
		this._xLRF = _xLRF;
	}

	public Amount<Length> getYLRF() {
		return _yLRF;
	}

	public void setYLRF(Amount<Length> _yLRF) {
		this._yLRF = _yLRF;
	}

	public Amount<Length> getZLRF() {
		return _zLRF;
	}

	public void set_zLRF(Amount<Length> _zLRF) {
		this._zLRF = _zLRF;
	}

	public Amount<Length> getXBRF() {
		return _xBRF;
	}

	public void setXBRF(Amount<Length> _xBRF) {
		this._xBRF = _xBRF;
	}

	public Amount<Length> getYBRF() {
		return _yBRF;
	}

	public void set_yBRF(Amount<Length> _yBRF) {
		this._yBRF = _yBRF;
	}

	public Amount<Length> getZBRF() {
		return _zBRF;
	}

	public void setZBRF(Amount<Length> _zBRF) {
		this._zBRF = _zBRF;
	}

	public Amount<Length> getXLRFref() {
		return _xLRFref;
	}

	public void setXLRFref(Amount<Length> _xLRFref) {
		this._xLRFref = _xLRFref;
	}

	public Amount<Length> getYLRFref() {
		return _yLRFref;
	}

	public void setYLRFref(Amount<Length> _yLRFref) {
		this._yLRFref = _yLRFref;
	}

	public Amount<Length> get_zLRFref() {
		return _zLRFref;
	}

	public void setZLRFref(Amount<Length> _zLRFref) {
		this._zLRFref = _zLRFref;
	}

	public Amount<Length> get_x0() {
		return _x0;
	}

	public void setX0(Amount<Length> _x0) {
		this._x0 = _x0;
	}

	public Amount<Length> get_y0() {
		return _y0;
	}

	public void setY0(Amount<Length> _y0) {
		this._y0 = _y0;
	}

	public Amount<Length> get_z0() {
		return _z0;
	}

	public void setZ0(Amount<Length> _z0) {
		this._z0 = _z0;
	}

	public Double getXMAC() {
		return _xMAC;
	}

	public Double getZMAC() {
		return _zMAC;
	}

	public void setZMAC(Double _zMAC) {
		this._zMAC = _zMAC;
	}

}
