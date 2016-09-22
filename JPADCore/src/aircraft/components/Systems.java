package aircraft.components;

import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.componentmodel.Component;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.atmosphere.AtmosphereCalc;
import writers.JPADStaticWriteUtils;


public class Systems extends Component{

	private Amount<Mass> _massCS;
	private Amount<Length> _lenght;

	private Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	private List<MethodEnum> _methodsList = new ArrayList<MethodEnum>();
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
	private Double[] _percentDifference;
	private Amount<Mass> _massReference;
	private Amount<Mass> _massMean;
	private Amount<Mass> _mass;


	public Systems(String name, String description, double x, double y,
			double z) {
		super("", name, description, x, y, z);

		_massReference = Amount.valueOf(2118, SI.KILOGRAM);

	}

	/**
	 * Overload of the previous builder that recognize aircraft name and sets it's 
	 * systems data.
	 * 
	 * @author Vittorio Trifari
	 */
	public Systems(AircraftEnum aircraftName, String name, String description, double x, double y,
			double z) {
		super("", name, description, x, y, z);

		switch(aircraftName) {
		
		case ATR72:
			_massReference = Amount.valueOf(2118, SI.KILOGRAM);
			break;
			
		case B747_100B:
			_massReference = Amount.valueOf(15949.0, SI.KILOGRAM);
			break;
			
		case AGILE_DC1:
			_massReference = Amount.valueOf(5087., SI.KILOGRAM);
			break;
			
		case IRON:
			_massReference = Amount.valueOf(4283., SI.KILOGRAM);
			break;
		}
	}

	@Override
	public void calculateMass(Aircraft aircraft, OperatingConditions conditions, MethodEnum method) {

		// The user can estimate the overall systems mass (control surfaces systems, apu, de-icing ecc...)
		// or estimate each component mass separately
		calculateOverallMass(aircraft, method);
//		} else {
//			calculateControlSurfaceMass(aircraft, method);
			//			_mass = _massCS; // + ...
//		}

	}


	public void calculateControlSurfaceMass(Aircraft aircraft, MethodEnum method) {

		switch (method) {
		case JENKINSON : {
			_methodsList.add(method);
			_massCS = Amount.valueOf(
					Math.pow(aircraft.get_weights().get_MTOM().times(0.04).getEstimatedValue(), 0.684),
					SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_massCS.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case TORENBEEK_1982 : {
			_massCS = Amount.valueOf(
					0.4915*Math.pow(aircraft.get_weights().get_MTOM().getEstimatedValue(), 2/3), 
					SI.KILOGRAM);
			_methodsList.add(method);
			_massMap.put(method, Amount.valueOf(round(_massCS.getEstimatedValue()), SI.KILOGRAM));
		} break;

		default : {} break;
		}

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_percentDifference =  new Double[_massMap.size()]; 

	}


	
	/**
	 * Evaluate mass of all the systems on board (APU, air conditioning, hydraulic systems ...)
	 * 
	 * @param aircraft
	 * @param method
	 */
	public void calculateOverallMass(Aircraft aircraft, MethodEnum method) {

		switch(method) {
		case TORENBEEK_2013 : {

			_mass = Amount.valueOf((
					270*aircraft.get_fuselage().get_len_F().getEstimatedValue()*
					aircraft.get_fuselage().get_equivalentDiameterCylinderGM().getEstimatedValue() +
					150*aircraft.get_fuselage().get_len_F().getEstimatedValue())/
					AtmosphereCalc.g0.getEstimatedValue(), 
					SI.KILOGRAM);
			_methodsList.add(method);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));

		} break;
		default : break;
		}	
		
		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_percentDifference =  new Double[_massMap.size()]; 

		_massMean = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_massReference, 
				_massMap,
				_percentDifference,
				30.).getFilteredMean(), SI.KILOGRAM);
		
	}


	public void calculateAPUMass() {

	}


	public void calculateInstrumentationMass() {

	}


	public void calculateElectricalMass() {

	}


	public void calculateAntiIceAirCond() {

	}


	public void calculateElectronicsMass() {

	}


	public void calculateHydraulicPneumaticMass() {

	}

	public Amount<Mass> get_massCS() {
		return _massCS;
	}


	public void set_massCS(Amount<Mass> _mass) {
		this._massCS = _mass;
	}


	public Amount<Length> get_lenght() {
		return _lenght;
	}


	public void set_lenght(Amount<Length> _lenght) {
		this._lenght = _lenght;
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


	public Amount<Mass> get_massMean() {
		return _massMean;
	}


	public Amount<Mass> get_mass() {
		return _mass;
	}


	public void set_mass(Amount<Mass> _mass) {
		this._mass = _mass;
	}

	public static String getId() {
		return "9";
	}

}
