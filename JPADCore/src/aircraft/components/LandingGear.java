package aircraft.components;

import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.componentmodel.Component;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;


public class LandingGear extends Component{

	private Amount<Length> _X0, _Y0, _Z0;

	private Amount<Mass> _mass, _massMain, _massNose;
	private Amount<Length> _lenght;
	private MountingPosition _mounting;

	private Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	private Map <MethodEnum, Amount<Length>> _xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	private Map <MethodEnum, Amount<Length>> _yCGMap = new TreeMap<MethodEnum, Amount<Length>>();

	private List<MethodEnum> _methodsList = new ArrayList<MethodEnum>();
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
	private Double[] _percentDifference;
	private Amount<Mass> _massReference, _massEstimated;
	private CenterOfGravity _cg;

	private Amount<Length> _xCG;

	private Double[] _percentDifferenceXCG;

	private double _deltaCD0;


	public LandingGear(String name, String description, double x, double y,
			double z) {
		super("", name, description, x, y, z);

		_X0 = Amount.valueOf(x, SI.METER);
		_Y0 = Amount.valueOf(y, SI.METER);
		_Z0 = Amount.valueOf(z, SI.METER);
		_cg = new CenterOfGravity(_X0, _Y0, _Z0);

		_mounting = MountingPosition.FUSELAGE;
		_massReference = Amount.valueOf(675.8, SI.KILOGRAM);

	}
	
	/**
	 * Overload of the previous builder that recognize aircraft name and sets 
	 * it's landing gear data.
	 * 
	 * @author Vittorio Trifari
	 */
	public LandingGear(AircraftEnum aircraftName, String name, String description, double x, double y,
			double z) {
		super("", name, description, x, y, z);

		_X0 = Amount.valueOf(x, SI.METER);
		_Y0 = Amount.valueOf(y, SI.METER);
		_Z0 = Amount.valueOf(z, SI.METER);
		_cg = new CenterOfGravity(_X0, _Y0, _Z0);

		switch(aircraftName) {
		
		case ATR72:
			_mounting = MountingPosition.FUSELAGE;
			_massReference = Amount.valueOf(675.8, SI.KILOGRAM);
			set_deltaCD0(0.008);
			break;
			
		case B747_100B:
			_mounting = MountingPosition.FUSELAGE;
			_massReference = Amount.valueOf(13900.0, SI.KILOGRAM);
			set_deltaCD0(0.020);
			break;
			
		case AGILE_DC1:
			_mounting = MountingPosition.FUSELAGE;
			_massReference = Amount.valueOf(1501.6, SI.KILOGRAM);
			set_deltaCD0(0.018);
			break;
		}
	}

	public enum MountingPosition {
		FUSELAGE,
		WING,
		NACELLE,
	}

	public void calculateMass(Aircraft aircraft, OperatingConditions conditions) {
		calculateMass(aircraft, conditions, MethodEnum.ROSKAM);
		calculateMass(aircraft, conditions, MethodEnum.STANFORD);
		calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_1982);
		calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_2013);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void calculateMass(Aircraft aircraft, OperatingConditions conditions, MethodEnum method) {
		switch (method) {
		/* Average error > 30 %
		case JENKINSON : {
			_methodsList.add(method);
			_mass = aircraft.get_weights().get_MTOM().times(0.0445);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
		 */
		case ROSKAM : { // Roskam page 97 (pdf) part V
			_methodsList.add(method);
			_mass = Amount.valueOf(
					62.21 * Math.pow(aircraft.get_weights().get_MTOM().to(NonSI.POUND).times(1e-3).getEstimatedValue(), 0.84),
					NonSI.POUND).to(SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case STANFORD : {
			_methodsList.add(method);
			_mass = aircraft.get_weights().get_MTOM().times(0.04);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case TORENBEEK_1982 : {
			_methodsList.add(method);
			_massMain = Amount.valueOf(40 + 0.16 * 
					Math.pow(aircraft.get_weights().get_MTOM().to(NonSI.POUND).getEstimatedValue(), 0.75) + 
					0.019 * aircraft.get_weights().get_MTOM().to(NonSI.POUND).getEstimatedValue() + 
					1.5 * 1e-5 * Math.pow(aircraft.get_weights().get_MTOM().to(NonSI.POUND).getEstimatedValue(), 1.5),
					NonSI.POUND).to(SI.KILOGRAM);
			_massNose = Amount.valueOf(20 + 0.1 * 
					Math.pow(aircraft.get_weights().get_MTOM().to(NonSI.POUND).getEstimatedValue(), 0.75) + 
					2 * 1e-5 * 
					Math.pow(aircraft.get_weights().get_MTOM().to(NonSI.POUND).getEstimatedValue(), 1.5),
					NonSI.POUND).to(SI.KILOGRAM);

			_mass = _massNose.plus(_massMain);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case TORENBEEK_2013 : {
			_methodsList.add(method);
			_mass = aircraft.get_weights().get_MTOM().times(0.025).
					plus(aircraft.get_weights().get_MLM().times(0.016));
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case RAYMER : {
			//TODO
		} break;
		default : {} break;
		}

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_percentDifference =  new Double[_massMap.size()]; 

		_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_massReference, 
				_massMap,
				_percentDifference,
				25.).getFilteredMean(), SI.KILOGRAM);

	}

	public void calculateCG(Aircraft aircraft, OperatingConditions conditions) {
		calculateCG(aircraft, conditions, MethodEnum.SFORZA);
	}
	
	
	/** 
	 * Evaluate CG location in LRF.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void calculateCG(
			Aircraft aircraft, 
			OperatingConditions conditions,
			MethodEnum method) {

		_cg.setLRForigin(_X0, _Y0, _Z0);
		_cg.set_xLRFref(Amount.valueOf(0., SI.METER));
		_cg.set_yLRFref(Amount.valueOf(0., SI.METER));
		_cg.set_zLRFref(Amount.valueOf(0., SI.METER));

		// Initialize _methodsList again to clear it
		// from old entries
		_methodsList = new ArrayList<MethodEnum>();

		switch(method) {

		// page 359 Sforza (2014) - Aircraft Design
		
		//TODO change this
		case SFORZA : { 
			_methodsList.add(method);
			_xCG = Amount.valueOf(0.0, SI.METER);		
			_xCGMap.put(method, _xCG);
		} break;

		default : break;

		}

		_methodsMap.put(AnalysisTypeEnum.BALANCE, _methodsList);
		_percentDifferenceXCG = new Double[_xCGMap.size()];

		_cg.set_xLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.get_xLRFref(), 
				_xCGMap,
				_percentDifferenceXCG,
				30.).getFilteredMean(), SI.METER));

		_cg.calculateCGinBRF();

	}

	public Amount<Mass> get_mass() {
		return _mass;
	}


	public void set_mass(Amount<Mass> _mass) {
		this._mass = _mass;
	}


	public Amount<Length> get_lenght() {
		return _lenght;
	}


	public void set_lenght(Amount<Length> _lenght) {
		this._lenght = _lenght;
	}


	public MountingPosition get_mounting() {
		return _mounting;
	}


	public void set_mounting(MountingPosition _mounting) {
		this._mounting = _mounting;
	}


	public Map<MethodEnum, Amount<Mass>> get_massMap() {
		return _massMap;
	}


	public void set_massMap(Map<MethodEnum, Amount<Mass>> _massMap) {
		this._massMap = _massMap;
	}


	public Map<AnalysisTypeEnum, List<MethodEnum>> get_methodsMap() {
		return _methodsMap;
	}


	public void set_methodsMap(
			Map<AnalysisTypeEnum, List<MethodEnum>> _methodsMap) {
		this._methodsMap = _methodsMap;
	}


	public Double[] get_percentDifference() {
		return _percentDifference;
	}


	public void set_percentDifference(Double[] _percentDifference) {
		this._percentDifference = _percentDifference;
	}


	public Amount<Mass> get_massReference() {
		return _massReference;
	}


	public void set_massReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}

	@Override
	public Amount<Length> get_X0() { return _X0; }

	@Override
	public void set_X0(Amount<Length> x) { _X0 = x; };

	@Override
	public Amount<Length> get_Y0() { return _Y0; }

	@Override
	public void set_Y0(Amount<Length> y) { _Y0 = y; };

	@Override
	public Amount<Length> get_Z0() { return _Z0; }

	@Override
	public void set_Z0(Amount<Length> z) { _Z0 = z; }


	public Amount<Mass> get_massEstimated() {
		return _massEstimated;
	}

	public Map<MethodEnum, Amount<Length>> get_xCGMap() {
		return _xCGMap;
	}

	public void set_xCGMap(Map<MethodEnum, Amount<Length>> _xCGMap) {
		this._xCGMap = _xCGMap;
	}

	public CenterOfGravity get_cg() {
		return _cg;
	}

	public void set_cg(CenterOfGravity _cg) {
		this._cg = _cg;
	}

	public static String getId() {
		return "6";
	}

	public double get_deltaCD0() {
		return _deltaCD0;
	}

	public void set_deltaCD0(double _deltaCD0) {
		this._deltaCD0 = _deltaCD0;
	}

}
