package analyses.nacelles;

import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import calculators.weights.NacelleWeightCalc;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import writers.JPADStaticWriteUtils;

public class NacelleWeightManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	private List<Amount<Mass>> _massList;
	private List<Amount<Mass>> _massEstimatedList;
	private List<Amount<Mass>> _massRefereceList;
	private Map <MethodEnum, Amount<Mass>> _massMap;
	private List<MethodEnum> _methodsList;  
	private double[] _percentDifference;       
	private Amount<Mass> _totalMassReference;
	private Amount<Mass> _totalMassEstimated;
	private Map <MethodEnum, Amount<Mass>> _totalMassMap;
	private double[] _totalPercentDifference;
	
	//------------------------------------------------------------------------------
	// BUILDER:
	//------------------------------------------------------------------------------
	public NacelleWeightManager () {
		
		initializeData();
		
	}
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	private void initializeData() {
		
		this._massList = new ArrayList<>();
		this._massEstimatedList = new ArrayList<>();
		this._massRefereceList = new ArrayList<>();
		this._massMap = new HashMap<>();
		this._methodsList = new ArrayList<>();
		this._totalMassMap = new HashMap<>();
		
	}
	
	public void estimateReferenceMasses (Aircraft theAircraft) {
		
		_massRefereceList.add(
				theAircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().times(.015)
					.divide(theAircraft.getNacelles().getNacellesNumber()));
		
	}
	
	public void calculateTotalMass(Aircraft theAircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {

		_totalMassEstimated = Amount.valueOf(0., SI.KILOGRAM);
		_totalMassReference = theAircraft.getTheAnalysisManager().getTheWeights().getNacellesReferenceMass();

		theAircraft.getNacelles().getNacellesList().stream().forEach(nac -> {
			calculateMass(theAircraft, theAircraft.getPowerPlant().getEngineType(), methodsMapWeights);
		});
		
		if(!methodsMapWeights.get(ComponentEnum.NACELLE).equals(MethodEnum.AVERAGE)) {
			_totalPercentDifference =  new double[_totalMassMap.size()];
			_totalMassEstimated = _totalMassMap.get(methodsMapWeights.get(ComponentEnum.NACELLE)).to(SI.KILOGRAM);
		}
		else {
			_totalPercentDifference =  new double[_totalMassMap.size()];
			_totalMassEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_totalMassReference,
					_totalMassMap,
					_totalPercentDifference,
					100.).getMean(), SI.KILOGRAM);
		}
		
	}
	
	private void calculateMass (Aircraft theAircraft, EngineTypeEnum engineType, Map<ComponentEnum, MethodEnum> methodsMapWeights) {
		
		if (engineType.equals(EngineTypeEnum.TURBOFAN) || engineType.equals(EngineTypeEnum.TURBOJET)) {
			calculateMass(theAircraft, engineType, MethodEnum.JENKINSON);
			calculateMass(theAircraft, engineType, MethodEnum.KUNDU);
			calculateMass(theAircraft, engineType, MethodEnum.ROSKAM);
		}
		else if (engineType.equals(EngineTypeEnum.TURBOPROP)) {
			calculateMass(theAircraft, engineType, MethodEnum.KUNDU);
			calculateMass(theAircraft, engineType, MethodEnum.TORENBEEK_1976);
		}
		else if (engineType.equals(EngineTypeEnum.PISTON)) {
			calculateMass(theAircraft, engineType, MethodEnum.KUNDU);
		}
		
		if(!methodsMapWeights.get(ComponentEnum.NACELLE).equals(MethodEnum.AVERAGE)) {
			_percentDifference = new double[_massMap.size()];
			_massEstimatedList.add(_massMap.get(methodsMapWeights.get(ComponentEnum.NACELLE)));
		}
		else {
			_percentDifference = new double[_massMap.size()];
			_massEstimatedList.add(
					Amount.valueOf(JPADStaticWriteUtils.compareMethods(
							_massRefereceList.get(0),
							_massMap,
							_percentDifference,
							100.).getMean(), SI.KILOGRAM)
					);
		}
		
	}
	
	private void calculateMass (Aircraft theAircraft, EngineTypeEnum engineType, MethodEnum method) {
		
		Amount<Mass> mass = Amount.valueOf(0.0, SI.KILOGRAM);
		
		switch (engineType) {
		case TURBOFAN:
			
			switch (method) {
			case JENKINSON:
				_methodsList.add(method);
				mass = NacelleWeightCalc.calculateTurbofanNacelleMassJenkinson(theAircraft);
				_massList.add(mass);
				_massMap.put(method, Amount.valueOf(round(mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
				break;
			case KUNDU:
				_methodsList.add(method);
				mass = NacelleWeightCalc.calculateTurbofanNacelleMassKundu(theAircraft);
				_massList.add(mass);
				_massMap.put(method, Amount.valueOf(round(mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
				break;
			case ROSKAM:
				_methodsList.add(method);
				mass = NacelleWeightCalc.calculateTurbofanNacelleMassRoskam(theAircraft);
				_massList.add(mass);
				_massMap.put(method, Amount.valueOf(round(mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
				break;
			default:
				break;
			}

			break;

		case TURBOJET:
			
			switch (method) {
			case JENKINSON:
				_methodsList.add(method);
				mass = NacelleWeightCalc.calculateTurbofanNacelleMassJenkinson(theAircraft);
				_massList.add(mass);
				_massMap.put(method, Amount.valueOf(round(mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
				break;
			case KUNDU:
				_methodsList.add(method);
				mass = NacelleWeightCalc.calculateTurbofanNacelleMassKundu(theAircraft);
				_massList.add(mass);
				_massMap.put(method, Amount.valueOf(round(mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
				break;
			case ROSKAM:
				_methodsList.add(method);
				mass = NacelleWeightCalc.calculateTurbofanNacelleMassRoskam(theAircraft);
				_massList.add(mass);
				_massMap.put(method, Amount.valueOf(round(mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
				break;
			default:
				break;
			}

			break;

		case TURBOPROP:

			switch (method) {
			case KUNDU:
				_methodsList.add(method);
				mass = NacelleWeightCalc.calculateTurbopropNacelleMassKundu(theAircraft);
				_massList.add(mass);
				_massMap.put(method, Amount.valueOf(round(mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
				break;
			case TORENBEEK_1976:
				_methodsList.add(method);
				mass = NacelleWeightCalc.calculateTurbopropNacelleMassTorenbeek1976(theAircraft);
				_massList.add(mass);
				_massMap.put(method, Amount.valueOf(round(mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
				break;
			default:
				break;
			}

			break;

		case PISTON:

			switch (method) {
			case KUNDU:
				_methodsList.add(method);
				mass = NacelleWeightCalc.calculatePistonNacelleMassKundu(theAircraft);
				_massList.add(mass);
				_massMap.put(method, Amount.valueOf(round(mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
				break;
			default:
				break;
			}

			break;
		
		default:
			break;
		}

	}

	//------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	//------------------------------------------------------------------------------
	
	public List<Amount<Mass>> getMassList() {
		return _massList;
	}

	public void setMassList(List<Amount<Mass>> _massList) {
		this._massList = _massList;
	}

	public List<Amount<Mass>> getMassEstimatedList() {
		return _massEstimatedList;
	}

	public void setMassEstimatedList(List<Amount<Mass>> _massEstimatedList) {
		this._massEstimatedList = _massEstimatedList;
	}

	public List<Amount<Mass>> getMassRefereceList() {
		return _massRefereceList;
	}

	public void setMassRefereceList(List<Amount<Mass>> _massRefereceList) {
		this._massRefereceList = _massRefereceList;
	}

	public Map<MethodEnum, Amount<Mass>> getMassMap() {
		return _massMap;
	}

	public void setMassMap(Map<MethodEnum, Amount<Mass>> _massMap) {
		this._massMap = _massMap;
	}

	public List<MethodEnum> getMethodsList() {
		return _methodsList;
	}

	public void setMethodsList(List<MethodEnum> _methodsList) {
		this._methodsList = _methodsList;
	}

	public double[] getPercentDifference() {
		return _percentDifference;
	}

	public void setPercentDifference (double[] _percentDifference) {
		this._percentDifference = _percentDifference;
	}

	public Amount<Mass> getTotalMassReference() {
		return _totalMassReference;
	}

	public void setTotalMassReference(Amount<Mass> _totalMassReference) {
		this._totalMassReference = _totalMassReference;
	}

	public Amount<Mass> getTotalMassEstimated() {
		return _totalMassEstimated;
	}

	public void setTotalMassEstimated(Amount<Mass> _totalMassEstimated) {
		this._totalMassEstimated = _totalMassEstimated;
	}

	public Map<MethodEnum, Amount<Mass>> getTotalMassMap() {
		return _totalMassMap;
	}

	public void setTotalMassMap(Map<MethodEnum, Amount<Mass>> _totalMassMap) {
		this._totalMassMap = _totalMassMap;
	}

	public double[] getTotalPercentDifference() {
		return _totalPercentDifference;
	}

	public void setTotalPercentDifference (double[] _totalPercentDifference) {
		this._totalPercentDifference = _totalPercentDifference;
	}

}
