package analyses.analysismodel.analysiscalcmanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import analyses.OperatingConditions;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;

public abstract class AerodynamicsManager extends ComponentCalculator{

	protected Double _machTransonicThreshold = null;

	protected Double 
	_alpha, _cD0,
	_cD0Parasite, 
	_kExcr, _cDTotalCurrent,
	_cDRoughness;
	
	protected Double 
	_mach, _altitude, _cF, _reynolds, 
	_cDwave = 0.,
	_cdGap,
	_cL = 0., _cLCurrent = 0.45, 
	_compressibilityFactor, _formFactor,
	_cD0Total, _CeCt;
	
	protected Double
	_cLAlpha, _cMacAdditional, _cMacBasic, 
	_cMacTotal, _betaPG, _cLAlphaMean2D;
	
	protected Amount<Angle> _alpha0L = null;

	protected Double _liftCoefficientGradient = null;

	protected Amount<Length> _aerodynamicCenterX = null;
	protected Amount<Length> _aerodynamicCenterY = Amount.valueOf(1.0, SI.METRE);
	protected Amount<Length> _aerodynamicCenterZ = Amount.valueOf(1.0, SI.METRE);
	protected Double _pitchCoefficientAC = null;

	protected Amount<Angle> _alphaStar = null;
	protected Amount<Angle> _alphaStall = null;
	protected Double _liftCoefficientMax = null;

	protected Double _rollCoefficientGradient = null;
	protected Double _pitchCoefficientGradient = null;
	protected Double _yawCoefficientGradient = null;

	protected OperatingConditions _theOperatingConditions;
	protected ComponentEnum _type;
	protected GeometryManager geometry;

	protected Double[] _percentDifference;
	
	Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();

	public Double get_mach() {
		return _mach;
	}

	public void set_mach(Double _mach) {
		this._mach = _mach;
	}

	public Double get_altitude() {
		return _altitude;
	}

	public void set_altitude(Double _altitude) {
		this._altitude = _altitude;
	}

	public OperatingConditions get_theOperatingConditions() {
		return _theOperatingConditions;
	}

	public void set_theOperatingConditions(OperatingConditions _theOperatingConditions) {
		this._theOperatingConditions = _theOperatingConditions;
	}

	

}

