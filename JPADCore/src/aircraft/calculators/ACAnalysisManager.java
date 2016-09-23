package aircraft.calculators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.components.Aircraft;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;

/**
 * All the computations are managed by this class.
 * Do not use directly the methods contained in each component; instead, invoke always the methods contained in this class
 * in order to be sure that each quantity is evaluated correctly. 
 * 
 * @author Lorenzo Attanasio
 *
 */
public class ACAnalysisManager {

	private final String id = "23";

	// private MyAircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;

	private ACPerformanceManager _thePerformances;
	
	private AerodynamicDatabaseReader _aerodynamicDatabaseReader;

	private List<MethodEnum> _methodsList = new ArrayList<MethodEnum>(); 
	private Map <ComponentEnum, List<MethodEnum>> _methodsMap = 
			new HashMap<ComponentEnum, List<MethodEnum>>();
	private Map <AnalysisTypeEnum, Boolean> _executedAnalysesMap = 
			new HashMap<AnalysisTypeEnum, Boolean>();

	private ACAerodynamicsManager _theAerodynamics;

	private List<ACCalculatorManager> _theCalculatorsList = new ArrayList<ACCalculatorManager>();

	private String _name = "";

	public ACAnalysisManager () {
		_name = "ANALYSIS";
	}

	public ACAnalysisManager (OperatingConditions conditions) {
		_name = "ANALYSIS";
		_theOperatingConditions = conditions;
	}
	
	public ACAnalysisManager(OperatingConditions conditions, Aircraft aircraft, AnalysisTypeEnum ... type) {
		_name = "ANALYSIS";
		_theOperatingConditions = conditions;
		//		doAnalysis(aircraft, type);
	}

	public ACAnalysisManager(Aircraft aircraft, AnalysisTypeEnum ... type) {

		_name = "ANALYSIS";
		_theOperatingConditions = new OperatingConditions();
		//		doAnalysis(aircraft, type);
	}


	public void doAnalysis(Aircraft aircraft, AnalysisTypeEnum ... type) {

		if (aircraft == null) return;

		_theOperatingConditions.calculate();
		_thePerformances = new ACPerformanceManager();

		//it's possible to define a method setDatabase
		if ( aircraft.get_wing().getAerodynamics().get_AerodynamicDatabaseReader() != null){
				aircraft.get_theAerodynamics().set_aerodynamicDatabaseReader(
						aircraft
						.get_wing()
						.getAerodynamics()
						.get_AerodynamicDatabaseReader());}
		
		if ( aircraft.get_wing().getAerodynamics().getHighLiftDatabaseReader() != null){
				aircraft.get_theAerodynamics().set_highLiftDatabaseReader(
						aircraft
						.get_wing()
						.getAerodynamics()
						.getHighLiftDatabaseReader());}
				
		updateGeometry(aircraft);

		if (Arrays.asList(type).contains(AnalysisTypeEnum.WEIGHTS)) {
			calculateWeights(aircraft); 
			_executedAnalysesMap.put(AnalysisTypeEnum.WEIGHTS, true);
		}

		if (Arrays.asList(type).contains(AnalysisTypeEnum.BALANCE)) {
			calculateBalance(aircraft);
			_executedAnalysesMap.put(AnalysisTypeEnum.BALANCE, true);
		}

		if (Arrays.asList(type).contains(AnalysisTypeEnum.AERODYNAMIC)) {
			calculateAerodynamics(aircraft);
			_executedAnalysesMap.put(AnalysisTypeEnum.AERODYNAMIC, true);
		}
		
		if (Arrays.asList(type).contains(AnalysisTypeEnum.PERFORMANCE)) {
			calculatePerformances(aircraft);
			_executedAnalysesMap.put(AnalysisTypeEnum.PERFORMANCE, true);
		}
		
		if (Arrays.asList(type).contains(AnalysisTypeEnum.COSTS)) {
			calculateCosts(aircraft);
			_executedAnalysesMap.put(AnalysisTypeEnum.COSTS, true);
		}
		
	} // end of constructor

	/** 
	 * Evaluate dependent geometric parameters
	 */
	public void updateGeometry(Aircraft aircraft) {

		// Fuselage
		if(aircraft.get_fuselage() != null){
			System.out.println("Updating fuselage geometry ...");
			aircraft.get_fuselage().calculateGeometry();
			aircraft.get_fuselage().checkGeometry();
			aircraft.set_sWetTotal(aircraft.get_fuselage().get_sWet().getEstimatedValue());
		}

		// Wing
		if(aircraft.get_wing() != null){
			System.out.println("Updating wing geometry ...");
			aircraft.get_wing().calculateGeometry();
			//			aircraft.get_wing().updateAirfoilsGeometry();
			aircraft.get_wing().getGeometry().calculateAll();
			aircraft.set_sWetTotal(aircraft.get_wing().get_surfaceWettedExposed().getEstimatedValue());
		}
		//ExposedWing
		//TODO: eventually continue here
		if(aircraft.get_exposedWing() != null){
		aircraft.get_exposedWing().set_surface(aircraft.get_wing().get_surfaceExposed());
		aircraft.get_exposedWing().set_span(Amount.valueOf(aircraft.get_wing().get_span().getEstimatedValue()-
				aircraft.get_fuselage().getWidthAtX(aircraft.get_wing()
						.get_xLEMacActualBRF().getEstimatedValue()), SI.METER));
		aircraft.get_exposedWing().set_semispan(
				Amount.valueOf(
						(aircraft.get_exposedWing().get_span().getEstimatedValue()/2),SI.METER)
				);
		aircraft.get_exposedWing().set_aspectRatio(
				(Math.pow(aircraft.get_exposedWing().get_span().getEstimatedValue(),2))
				/(aircraft.get_exposedWing().get_surface().getEstimatedValue()));
		//aircraft.get_exposedWing().updateAirfoilsGeometryEquivalentWing(aircraft);
		}

		// Htail
		if(aircraft.get_HTail() != null){
			System.out.println("Updating HTail geometry ...");
			aircraft.get_HTail().calculateGeometry();
			//			aircraft.get_HTail().updateAirfoilsGeometry();
			aircraft.get_HTail().getGeometry().calculateAll();
			aircraft.set_sWetTotal(aircraft.get_HTail().get_surfaceWettedExposed().getEstimatedValue());
		}

		// Vtail
		if(aircraft.get_VTail() != null){
			System.out.println("Updating VTail geometry ...");
			aircraft.get_VTail().calculateGeometry();
			//			aircraft.get_VTail().updateAirfoilsGeometry();
			aircraft.get_VTail().getGeometry().calculateAll();
			aircraft.set_sWetTotal(aircraft.get_VTail().get_surfaceWettedExposed().getEstimatedValue());
		}

		// Canard
		if(aircraft.get_Canard() != null){
			System.out.println("Updating Canard geometry ...");
			aircraft.get_Canard().calculateGeometry();
			//			aircraft.get_Canard().updateAirfoilsGeometry();
			aircraft.get_Canard().getGeometry().calculateAll();
			aircraft.set_sWetTotal(aircraft.get_Canard().get_surfaceWettedExposed().getEstimatedValue());
		}

		// Nacelle
		if(aircraft.get_theNacelles() != null){
			aircraft.get_theNacelles().calculateSurfaceWetted();
			aircraft.set_sWetTotal(aircraft.get_theNacelles().get_surfaceWetted().getEstimatedValue());
		}

		// Fuel tank
		if(aircraft.get_theFuelTank() != null){
			aircraft.get_theFuelTank().calculateGeometry(aircraft);
		}

		// Evaluate thrust output
		if(aircraft.get_powerPlant() != null){
			aircraft.get_powerPlant().calculateDerivedVariables();
		}

		if(aircraft!= null && _theOperatingConditions!=null){

			aircraft.get_performances().calculateSpeeds();

			aircraft.get_weights().calculateDependentVariables(aircraft);
			aircraft.get_theBalance().calculateBalance(aircraft);

			if(aircraft.get_HTail() != null){
				aircraft.get_HTail().calculateACwACdistance(aircraft);
			}

			if(aircraft.get_VTail() != null){
				aircraft.get_VTail().calculateACwACdistance(aircraft);
			}

			// Calculate dependent variables
			aircraft.get_configuration().calculateDependentVariables();
		}

		_executedAnalysesMap.put(AnalysisTypeEnum.GEOMETRY, true);
	}
	
	public void calculateAerodynamics(Aircraft aircraft) {
		calculateAerodynamics(_theOperatingConditions, aircraft);
	}

	public void calculateAerodynamics(OperatingConditions conditions, Aircraft aircraft) {

		aircraft.get_theAerodynamics().initialize(_theOperatingConditions);
		aircraft.get_theAerodynamics().calculateAll(_theOperatingConditions);

		// populate _theCalculatorsList
		_theCalculatorsList.add(aircraft.get_theAerodynamics());

	}

	public void calculateBalance(Aircraft aircraft) {

		// Build cabin layout
		aircraft.get_configuration().buildSimpleLayout(aircraft);

		// Estimate center of gravity location
		aircraft.get_theBalance().calculateBalance(aircraft, _theOperatingConditions, _methodsMap);

		// Evaluate arms again with the new CG estimate
		aircraft.get_HTail().calculateArms(aircraft);
		aircraft.get_VTail().calculateArms(aircraft);

		_theCalculatorsList.add(aircraft.get_theBalance());
	}


	public void calculateWeights(Aircraft aircraft) {

		// Choose methods to use for each component
		// All methods are used for weight estimation and for CG estimation
		_methodsList.clear();
		_methodsList.add(MethodEnum.ALL);
		_methodsMap.put(ComponentEnum.FUSELAGE, _methodsList);
		_methodsList = new ArrayList<MethodEnum>();

		_methodsList.add(MethodEnum.ALL);
		_methodsMap.put(ComponentEnum.WING, _methodsList);
		_methodsList = new ArrayList<MethodEnum>();

		_methodsList.add(MethodEnum.ALL);
		_methodsMap.put(ComponentEnum.HORIZONTAL_TAIL, _methodsList);
		_methodsList = new ArrayList<MethodEnum>();

		_methodsList.add(MethodEnum.ALL);
		_methodsMap.put(ComponentEnum.VERTICAL_TAIL, _methodsList);
		_methodsList = new ArrayList<MethodEnum>();

		_methodsList.add(MethodEnum.ALL);
		_methodsMap.put(ComponentEnum.POWER_PLANT, _methodsList);
		_methodsList = new ArrayList<MethodEnum>();

		_methodsList.add(MethodEnum.ALL);
		_methodsMap.put(ComponentEnum.FUEL_TANK, _methodsList);
		_methodsList = new ArrayList<MethodEnum>();

		_methodsList.add(MethodEnum.ALL);
		_methodsMap.put(ComponentEnum.NACELLE, _methodsList);
		_methodsList = new ArrayList<MethodEnum>();

		_methodsList.add(MethodEnum.ALL);
		_methodsMap.put(ComponentEnum.LANDING_GEAR, _methodsList);
		_methodsList = new ArrayList<MethodEnum>();

		_methodsList.add(MethodEnum.ALL);
		_methodsMap.put(ComponentEnum.SYSTEMS, _methodsList);
		_methodsList = new ArrayList<MethodEnum>();

		aircraft.get_weights().calculateDependentVariables(aircraft);
		aircraft.get_configuration().calculateDependentVariables();

		// Evaluate aircraft masses
		aircraft.get_weights().calculateAllMasses(aircraft, _theOperatingConditions, _methodsMap);

		// populate _theCalculatorsList
		_theCalculatorsList.add(aircraft.get_weights());

	}

	public void calculatePerformances(Aircraft aircraft) {
		aircraft.get_performances().calculateAllPerformance(aircraft);
	}
	
	public void calculateCosts(Aircraft aircraft) {
		aircraft.get_theCosts().calculateAll();
	}

	public OperatingConditions get_theOperatingConditions() {
		return _theOperatingConditions;
	}


	public ACPerformanceManager get_thePerformances() {
		return _thePerformances;
	}


	//	public MyAircraft get_theAircraft() {
	//		return _theAircraft;
	//	}

	public ACAerodynamicsManager get_theAerodynamics() {
		return _theAerodynamics;
	}


	public void calculate() {
		//		_theAeroCalculator.calculateDragPolar();
		//		_theWeights.calculateAerodynamic();
	}

	public Map<AnalysisTypeEnum, Boolean> get_executedAnalysesMap() {
		return _executedAnalysesMap;
	}

	public String get_name() {
		return _name;
	}

	public void setName(String _name) {
		this._name = _name;
	}

	public List<ACCalculatorManager> get_theCalculatorsList() {
		return _theCalculatorsList;
	}

	public void set_theCalculatorsList(List<ACCalculatorManager> _theCalculatorsList) {
		this._theCalculatorsList = _theCalculatorsList;
	}

	public String getId() {
		return id;
	}

	public AerodynamicDatabaseReader get_aerodynamicDatabaseReader() {
		return _aerodynamicDatabaseReader;
	}

	public void set_aerodynamicDatabaseReader(AerodynamicDatabaseReader _aerodynamicDatabaseReader) {
		this._aerodynamicDatabaseReader = _aerodynamicDatabaseReader;
	}
	public void set_theOperatingConditions(OperatingConditions _theOperatingConditions) {
		this._theOperatingConditions = _theOperatingConditions;
	}
	
}

