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
		if ( aircraft.getWing().getAerodynamics().get_AerodynamicDatabaseReader() != null){
				aircraft.getTheAerodynamics().set_aerodynamicDatabaseReader(
						aircraft
						.getWing()
						.getAerodynamics()
						.get_AerodynamicDatabaseReader());}
		
		if ( aircraft.getWing().getAerodynamics().getHighLiftDatabaseReader() != null){
				aircraft.getTheAerodynamics().set_highLiftDatabaseReader(
						aircraft
						.getWing()
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
		if(aircraft.getFuselage() != null){
			System.out.println("Updating fuselage geometry ...");
			aircraft.getFuselage().getFuselageCreator().calculateGeometry();
			aircraft.getFuselage().getFuselageCreator().checkGeometry();
			aircraft.setSWetTotal(aircraft.getFuselage().getsWet());
		}

		// Wing
		if(aircraft.getWing() != null){
			System.out.println("Updating wing geometry ...");
			aircraft.getWing().getLiftingSurfaceCreator().calculateGeometry(ComponentEnum.WING, Boolean.TRUE);
			//			aircraft.get_wing().updateAirfoilsGeometry();
			aircraft.getWing().getGeometry().calculateAll();
			aircraft.setSWetTotal(aircraft.getExposedWing().getLiftingSurfaceCreator().getSurfaceWetted());
		}
		//ExposedWing
		//TODO: eventually continue here
		if(aircraft.getExposedWing() != null){
		aircraft.getExposedWing().setSurface(aircraft.getExposedWing().getSurface());
		aircraft.getExposedWing().setSpan(Amount.valueOf(aircraft.getWing().getSpan().getEstimatedValue()-
				aircraft.getFuselage().getWidthAtX(aircraft.getWing()
						.get_xLEMacActualBRF().getEstimatedValue()), SI.METER));
		aircraft.getExposedWing().set_semispan(
				Amount.valueOf(
						(aircraft.getExposedWing().get_span().getEstimatedValue()/2),SI.METER)
				);
		aircraft.getExposedWing().set_aspectRatio(
				(Math.pow(aircraft.getExposedWing().get_span().getEstimatedValue(),2))
				/(aircraft.getExposedWing().get_surface().getEstimatedValue()));
		//aircraft.get_exposedWing().updateAirfoilsGeometryEquivalentWing(aircraft);
		}

		// Htail
		if(aircraft.getHTail() != null){
			System.out.println("Updating HTail geometry ...");
			aircraft.getHTail().calculateGeometry();
			//			aircraft.get_HTail().updateAirfoilsGeometry();
			aircraft.getHTail().getGeometry().calculateAll();
			aircraft.setSWetTotal(aircraft.getHTail().get_surfaceWettedExposed().getEstimatedValue());
		}

		// Vtail
		if(aircraft.getVTail() != null){
			System.out.println("Updating VTail geometry ...");
			aircraft.getVTail().calculateGeometry();
			//			aircraft.get_VTail().updateAirfoilsGeometry();
			aircraft.getVTail().getGeometry().calculateAll();
			aircraft.setSWetTotal(aircraft.getVTail().get_surfaceWettedExposed().getEstimatedValue());
		}

		// Canard
		if(aircraft.getCanard() != null){
			System.out.println("Updating Canard geometry ...");
			aircraft.getCanard().calculateGeometry();
			//			aircraft.get_Canard().updateAirfoilsGeometry();
			aircraft.getCanard().getGeometry().calculateAll();
			aircraft.setSWetTotal(aircraft.getCanard().get_surfaceWettedExposed().getEstimatedValue());
		}

		// Nacelle
		if(aircraft.getNacelles() != null){
			aircraft.getNacelles().calculateSurfaceWetted();
			aircraft.setSWetTotal(aircraft.getNacelles().getSurfaceWetted().getEstimatedValue());
		}

		// Fuel tank
		if(aircraft.getFuelTank() != null){
			aircraft.getFuelTank().calculateGeometry(aircraft);
		}

		// Evaluate thrust output
		if(aircraft.getPowerPlant() != null){
			aircraft.getPowerPlant().calculateDerivedVariables();
		}

		if(aircraft!= null && _theOperatingConditions!=null){

			aircraft.getThePerformance().calculateSpeeds();

			aircraft.getTheWeights().calculateDependentVariables(aircraft);
			aircraft.getTheBalance().calculateBalance(aircraft);

			if(aircraft.getHTail() != null){
				aircraft.getHTail().calculateACwACdistance(aircraft);
			}

			if(aircraft.getVTail() != null){
				aircraft.getVTail().calculateACwACdistance(aircraft);
			}

			// Calculate dependent variables
			aircraft.getCabinConfiguration().calculateDependentVariables();
		}

		_executedAnalysesMap.put(AnalysisTypeEnum.GEOMETRY, true);
	}
	
	public void calculateAerodynamics(Aircraft aircraft) {
		calculateAerodynamics(_theOperatingConditions, aircraft);
	}

	public void calculateAerodynamics(OperatingConditions conditions, Aircraft aircraft) {

		aircraft.getTheAerodynamics().initialize(_theOperatingConditions);
		aircraft.getTheAerodynamics().calculateAll(_theOperatingConditions);

		// populate _theCalculatorsList
		_theCalculatorsList.add(aircraft.getTheAerodynamics());

	}

	public void calculateBalance(Aircraft aircraft) {

		// Build cabin layout
		aircraft.getCabinConfiguration().buildSimpleLayout(aircraft);

		// Estimate center of gravity location
		aircraft.getTheBalance().calculateBalance(aircraft, _theOperatingConditions, _methodsMap);

		// Evaluate arms again with the new CG estimate
		aircraft.getHTail().calculateArms(aircraft);
		aircraft.getVTail().calculateArms(aircraft);

		_theCalculatorsList.add(aircraft.getTheBalance());
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

		aircraft.getTheWeights().calculateDependentVariables(aircraft);
		aircraft.getCabinConfiguration().calculateDependentVariables();

		// Evaluate aircraft masses
		aircraft.getTheWeights().calculateAllMasses(aircraft, _theOperatingConditions, _methodsMap);

		// populate _theCalculatorsList
		_theCalculatorsList.add(aircraft.getTheWeights());

	}

	public void calculatePerformances(Aircraft aircraft) {
		aircraft.getThePerformance().calculateAllPerformance();
	}
	
	public void calculateCosts(Aircraft aircraft) {
		aircraft.getTheCosts().calculateAll();
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

