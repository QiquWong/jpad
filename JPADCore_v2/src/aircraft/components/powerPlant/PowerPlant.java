package aircraft.components.powerPlant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Force;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.powerPlant.Engine.EngineBuilder;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;

/** 
 * The Propulsion System includes engines, engine exhaust, 
 * reverser, starting, controls, lubricating, and fuel systems.
 * The output of this class is the entire propulsion system (that
 * is, all engines are included) 
 */

//TODO: this class should not extend MyComponent
public class PowerPlant {

	//---------------------------------------------------------------------------
	// ALREADY DONE :
	public  String _id ;
	private Integer _engineNumber;
	private EngineMountingPositionEnum _mountingPoint;
	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	private Amount<Angle> _muT;
	
	/** Check if engines are all the same */
	private Boolean _engineEqual = false;
	
	public List<Engine> _engineList;
	private Amount<Force> _T0Total;
	private Amount<Power> _P0Total;
	
	private List<CenterOfGravity> _cgList;
	private Map <MethodEnum, Amount<Mass>> _massMap;
	private Map <MethodEnum, Amount<Length>> _xCGMap;
	private Map <MethodEnum, Amount<Length>> _yCGMap;
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap;
	private Double[] _percentDifference;
	
	private Aircraft _theAircraft;
	private PowerPlantWeightsManager _theWeights;
	private PowerPlantBalanceManager _theBalance;
	
	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class PowerPlantBuilder {
	
		// required parameters
		private String __id;
		private Integer __engineNumber;
		public List<Engine> __engineList = new ArrayList<Engine>();
		
		// optional parameters ... defaults
		// ...	
		private List<CenterOfGravity> __cgList = new ArrayList<CenterOfGravity>();
		private Map <MethodEnum, Amount<Mass>> __massMap = new TreeMap<MethodEnum, Amount<Mass>>();
		private Map <MethodEnum, Amount<Length>> __xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
		private Map <MethodEnum, Amount<Length>> __yCGMap = new TreeMap<MethodEnum, Amount<Length>>();
		private Map <AnalysisTypeEnum, List<MethodEnum>> __methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
		private Double[] __percentDifference;
		
		public PowerPlantBuilder (String id, Engine engine, Integer nEngine) {
			this.__id = id;
			this.__engineNumber = nEngine;
			for(int i=0; i<__engineNumber; i++)
				__engineList.add(engine);
		}
		
	}
	
	//---------------------------------------------------------------------------

	private Amount<Mass> 
	_massDryEngine, _massDryEngineEstimated,
	_massDryEngineActual, _massDryEngineActualTotal, 
	_totalMass, _dryMassPublicDomain;

	private Double _percentTotalDifference;
	private CenterOfGravity _cg;

	private Double[] _percentDifferenceXCG;
	private CenterOfGravity _totalCG;

	private double etaEfficiency;
	private double nBlade;
	Amount<Length> fanDiameter;


	public void calculateDerivedVariables() {

		_massDryEngineActualTotal = _massDryEngineActual.times(_engineNumber);
		_T0Total = Amount.valueOf(0., SI.NEWTON);
		_P0Total = Amount.valueOf(0., SI.WATT);

		//		if (_engineType==MyEngineTypeEnum.TURBOPROP || _engineType==MyEngineTypeEnum.PISTON) {
		//			_rpm = Amount.valueOf(2100, MyUnits.RPM);
		//			_rps = _rpm.to(SI.HERTZ);
		//			_diameter = Amount.valueOf(3.93, SI.METER);
		//			_J = _v0.divide(_diameter.times(_rps)).getEstimatedValue();
		//
		//			_eta = 0.75;
		//			_cP = _P0.divide((conditions.get_densityCurrent().times(_rps.pow(3)).times(_diameter.pow(5)))).getEstimatedValue();
		//			
		//			//TODO: need for a more accurate estimation of Ct for v--->0
		//			//		_cT = _eta*_cP/_J;
		//			_cT = 0.12;
		//
		//			_T0 = Amount.valueOf(conditions.get_densityCurrent().times(_cT).times(_rps.pow(2)).times(_diameter.pow(4)).getEstimatedValue(),
		//					SI.NEWTON);
		//		}

		for(int i=0; i < _engineNumber; i++) {
			_T0Total = _T0Total.plus(engineList.get(i).getT0());
			_P0Total = _P0Total.plus(engineList.get(i).getP0());
		}

	}


	@Override
	public void calculateMass() {

		_totalMass = Amount.valueOf(0., SI.KILOGRAM);
		_dryMassPublicDomain = Amount.valueOf(0., SI.KILOGRAM);

		for(int i=0; i < _engineNumber; i++) {
			_totalMass = _totalMass.plus(engineList.get(i).getmasTotalMass());
			_dryMassPublicDomain = _dryMassPublicDomain.plus(engineList.get(i).getWeights().getDryMassPublicDomain());
		}

		_percentTotalDifference = _totalMass.
				minus(_dryMassPublicDomain).
				divide(_dryMassPublicDomain).
				getEstimatedValue()*100.;
	}


	public CenterOfGravity calculateCG() {

		_totalCG = new CenterOfGravity();		
		for(int i=0; i < _engineNumber; i++) {
			engineList.get(i).getBalance().calculateAll();
			_cgList.add(engineList.get(i).getBalance().get_cg());
			_totalCG = _totalCG.plus(engineList.get(i).getBalance().get_cg()
					.times(engineList.get(i).getTotalMass().doubleValue(SI.KILOGRAM)));
		}
		
		_totalCG = _totalCG.divide(_totalMass.doubleValue(SI.KILOGRAM));
		return _totalCG;
	}

	public Amount<Mass> get_massDryEngineActual() {
		return _massDryEngineActual;
	}

	public void set_mass(Amount<Mass> _mass) {
		this._massDryEngine = _mass;
	}

	public Amount<Power> get_P0Total() {
		return _P0Total;
	}

	public void set_P0Total(Amount<Power> _P0) {
		this._P0Total = _P0;
	}

	public Amount<Force> get_T0Total() {
		return _T0Total;
	}

	public void set_T0Total(Amount<Force> _T0Total) {
		this._T0Total = _T0Total;
	}

	public Integer get_engineNumber() {
		return _engineNumber;
	}

	public void set_engineNumber(Integer _engineNumber) {
		this._engineNumber = _engineNumber;
	}


	public EngineTypeEnum get_engineType() {
		return _engineType;
	}

	public void set_engineType(EngineTypeEnum _engineType) {
		this._engineType = _engineType;
	}

	public Map<MethodEnum, Amount<Mass>> get_massMap() {
		return _massMap;
	}


	public Map<AnalysisTypeEnum, List<MethodEnum>> get_methodsMap() {
		return _methodsMap;
	}


	public Double[] get_percentDifference() {
		return _percentDifference;
	}


	public void set_massDryEngine(Amount<Mass> _massDryEngine) {
		this._massDryEngine = _massDryEngine;
	}


	public Amount<Mass> get_totalMass() {
		return _totalMass;
	}


	public Amount<Mass> get_dryMassPublicDomain() {
		return _dryMassPublicDomain;
	}


	public void set_totalPowerPlantMassActual(
			Amount<Mass> _totalPowerPlantMassReference) {
		this._dryMassPublicDomain = _totalPowerPlantMassReference;
	}


	public Double get_percentTotalDifference() {
		return _percentTotalDifference;
	}

	//---------------------------------------------------------------------------------
	// ALREADY DONE : 
	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	public void setXApexConstructionAxes(Amount<Length> _X0) {
		this._xApexConstructionAxes = _X0;
	}

	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	public void setYApexConstructionAxes(Amount<Length> _Y0) {
		this._yApexConstructionAxes = _Y0;
	}

	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	public void setZApexConstructionAxes(Amount<Length> _Z0) {
		this._zApexConstructionAxes = _Z0;
	}

	public EngineMountingPositionEnum getMountingPoint() {
		return _mountingPoint;
	}

	public void setMountingPoint(EngineMountingPositionEnum _mountingPoint) {
		this._mountingPoint = _mountingPoint;
	}

	public Amount<Angle> getMuT() {
		return _muT;
	}

	public void setMuT(Amount<Angle> _muT) {
		this._muT = _muT;
	}
	
	//---------------------------------------------------------------------------------
	
	public Amount<Mass> get_massDryEngineActualTotal() {
		return _massDryEngineActualTotal;
	}


	public Amount<Mass> get_massDryEngine() {
		return _massDryEngine;
	}


	public Amount<Mass> get_massDryEngineEstimated() {
		return _massDryEngineEstimated;
	}


	public EngineMountingPositionEnum get_position() {
		return _position;
	}


	public void set_position(EngineMountingPositionEnum _position) {
		this._position = _position;
	}


	public String get_name() {
		return _name;
	}


	public void set_name(String _name) {
		this._name = _name;
	}


	public Amount<Length> get_length() {
		return _length;
	}


	public void set_length(Amount<Length> _length) {
		this._length = _length;
	}


	public Map<MethodEnum, Amount<Length>> get_xCGMap() {
		return _xCGMap;
	}


	public void set_xCGMap(Map<MethodEnum, Amount<Length>> _xCGMap) {
		this._xCGMap = _xCGMap;
	}


	public Map<MethodEnum, Amount<Length>> get_yCGMap() {
		return _yCGMap;
	}


	public void set_yCGMap(Map<MethodEnum, Amount<Length>> _yCGMap) {
		this._yCGMap = _yCGMap;
	}


	public CenterOfGravity get_cg() {
		return _cg;
	}


	public void set_cg(CenterOfGravity _cg) {
		this._cg = _cg;
	}


	public Double[] get_percentDifferenceXCG() {
		return _percentDifferenceXCG;
	}

	public static String getId() {
		return "8";
	}

	public List<Engine> get_engineList() {
		return engineList;
	}

	public Boolean is_engineEqual() {
		return _engineEqual;
	}

	public void set_engineEqual(Boolean _engineEqual) {
		this._engineEqual = _engineEqual;
	}

	public CenterOfGravity get_totalCG() {
		return _totalCG;
	}

	public List<CenterOfGravity> get_cgList() {
		return _cgList;
	}

	public void set_cgList(List<CenterOfGravity> _cgList) {
		this._cgList = _cgList;
	}

	public double getEtaEfficiency() {
		return etaEfficiency;
	}

	public void setEtaEfficiency(double etaEfficiency) {
		this.etaEfficiency = etaEfficiency;
	}

	public double getnBlade() {
		return nBlade;
	}

	public void setnBlade(double nBlade) {
		this.nBlade = nBlade;
	}

	public Amount<Length> getFanDiameter() {
		return fanDiameter;
	}

	public void setFanDiameter(Amount<Length> fanDiameter) {
		this.fanDiameter = fanDiameter;
	}

	
}
