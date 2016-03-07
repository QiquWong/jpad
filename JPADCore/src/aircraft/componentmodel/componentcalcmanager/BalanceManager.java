package aircraft.componentmodel.componentcalcmanager;

import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;

public abstract class BalanceManager extends ComponentCalculator{

	protected CenterOfGravity _cg;

	protected Amount<Length> _xCGReference;
	protected Amount<Length> _xCGEstimated;

	protected Amount<Length> _xCG, _yCG, _zCG;

	protected Double[] _percentDifferenceXCG, _percentDifferenceYCG;
	protected Amount<Length> _yCGReference, _yCGEstimated;

	protected Map <MethodEnum, Amount<Length>> _xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	protected Map <MethodEnum, Amount<Length>> _yCGMap = new TreeMap<MethodEnum, Amount<Length>>();

	public BalanceManager() {
		_cg = new CenterOfGravity();	
	}

	@Override
	public void calculateAll() {
		// TODO Auto-generated method stub
	}


	public CenterOfGravity get_cg() {
		return _cg;
	}
	
	public void set_cg(CenterOfGravity _cg) {
		this._cg = _cg;
	}

	public Amount<Length> get_xCG() {
		return _xCG;
	}

	public void set_xCG(Amount<Length> _xCG) {
		this._xCG = _xCG;
	}

	public Amount<Length> get_yCG() {
		return _yCG;
	}

	public void set_yCG(Amount<Length> _yCG) {
		this._yCG = _yCG;
	}

	public Amount<Length> get_zCG() {
		return _zCG;
	}

	public void set_zCG(Amount<Length> _zCG) {
		this._zCG = _zCG;
	}

	
}