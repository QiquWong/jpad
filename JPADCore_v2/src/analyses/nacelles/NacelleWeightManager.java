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
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

public class NacelleWeightManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	private List<Amount<Mass>> _massList;
	private List<Amount<Mass>> _massEstimatedList;
	private List<Amount<Mass>> _massRefereceList;
	private Map <MethodEnum, Amount<Mass>> _massMap;
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap; 
	private List<MethodEnum> _methodsList;  
	private List<double[]> _percentDifferenceList;       
	private Amount<Mass> _totalMassReference;
	private Amount<Mass> _totalMassEstimated;
	private Map <MethodEnum, Amount<Mass>> _totalMassMap;
	private double[] _totalPercentDifferenceList;
	
	// to be moved
	private CenterOfGravity _totalCG;
	private List<CenterOfGravity> _cgList;
	
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
		this._methodsMap = new HashMap<>();
		this._methodsList = new ArrayList<>();
		this._percentDifferenceList = new ArrayList<>();
		this._totalMassMap = new HashMap<>();
		
	}
	
	public void calculateTotalMass(Aircraft theAircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {

		_totalMassEstimated = Amount.valueOf(0., SI.KILOGRAM);
		_totalMassReference = theAircraft.getTheAnalysisManager().getTheWeights().getNacelleReferenceMass();

		theAircraft.getNacelles().getNacellesList().stream().forEach(nac -> {
			
			_massRefereceList.add(
					theAircraft.getTheAnalysisManager().getTheWeights().getNacelleReferenceMass()
						.divide(theAircraft.getNacelles().getNacellesNumber()));
			calculateMass(theAircraft, theAircraft.getPowerPlant().getEngineType(), methodsMapWeights.get(ComponentEnum.NACELLE));
			
		});
		
		if(!methodsMapWeights.get(ComponentEnum.NACELLE).equals(MethodEnum.AVERAGE)) {
			_totalPercentDifferenceList =  new double[_totalMassMap.size()];
			_totalMassEstimated = _totalMassMap.get(methodsMapWeights.get(ComponentEnum.NACELLE)).to(SI.KILOGRAM);
		}
		else {
			_totalPercentDifferenceList =  new double[_totalMassMap.size()];
			_totalMassEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_totalMassReference,
					_totalMassMap,
					_totalPercentDifferenceList,
					100.).getMean(), SI.KILOGRAM);
		}
		
	}
	
	private void calculateMass (Aircraft theAircraft, EngineTypeEnum engineType, MethodEnum method) {
		
		switch (engineType) {
		case TURBOFAN:
			
			Amount<Mass> mass = Amount.valueOf(0.0, SI.KILOGRAM);
			
			switch (method) {
			case JENKINSON:
				_methodsList.add(method);
				mass = NacelleWeightCalc.calculateTurbofanNacelleMassJenkinson(theAircraft);
				_massList.add(mass);
				_massMap.put(method, Amount.valueOf(round(mass.getEstimatedValue()), SI.KILOGRAM));
				break;
			case KUNDU:
				_methodsList.add(method);
				mass = NacelleWeightCalc.calculateTurbofanNacelleMassKundu(theAircraft);
				_massList.add(mass);
				_massMap.put(method, Amount.valueOf(round(mass.getEstimatedValue()), SI.KILOGRAM));
				break;
			case TORENBEEK_1982:
				_methodsList.add(method);
				mass = NacelleWeightCalc.calculateTurbofanNacelleMassTorenbeek1982(theAircraft);
				_massList.add(mass);
				_massMap.put(method, Amount.valueOf(round(mass.getEstimatedValue()), SI.KILOGRAM));
				break;
			
				// TODO: CONTINUE FROM HERE
				
			default:
				break;
			}
			
			break;

		default:
			break;
		}
		
	}
	
}
