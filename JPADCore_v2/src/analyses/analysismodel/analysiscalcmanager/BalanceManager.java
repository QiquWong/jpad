package analyses.analysismodel.analysiscalcmanager;

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


	public CenterOfGravity getCG() {
		return _cg;
	}
}