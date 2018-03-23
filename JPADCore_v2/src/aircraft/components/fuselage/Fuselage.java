package aircraft.components.fuselage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.fuselage.creator.FuselageCreator;
import analyses.fuselage.FuselageBalanceManager;
import analyses.fuselage.FuselageWeightsManager;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

/**
 * The Fuselage class is in charge of handling the component position in BRF (body reference frame) as well as
 * the component mass and center of gravity. Furthermore, it contains the FuselageCreator object which manages 
 * all the geometrical parameters. 
 * 
 * @author Vittorio Trifari
 *
 */
public class Fuselage {

	//----------------------------------------------------------------------
	// VARIABLE DECLARATIONS
	//----------------------------------------------------------------------
	private FusDesDatabaseReader _fusDesDatabaseReader;
	
	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	
	private FuselageCreator _fuselageCreator;
	private FuselageWeightsManager _theWeight;
	private FuselageBalanceManager _theBalance;
	
	// TODO: REMOVE THESE
	private Amount<Mass> _mass, _massEstimated, _massReference;
	private Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>(); 
	private List<MethodEnum> _methodsList = new ArrayList<MethodEnum>();  
	private Double[] _percentDifference;       
	private Double _massCorrectionFactor = 1.; 
	
	private Map <MethodEnum, Amount<Length>> _xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	private Amount<Length> _xCG, _xCGReference, _xCGEstimated, _zCGEstimated;
	private CenterOfGravity _cg;
	private double[] _percentDifferenceXCG;

	//------------------------------------------------------------------------------------
	// BUILDER
	//------------------------------------------------------------------------------------
	public Fuselage(FuselageCreator theFuselageCreator) {
		
		this._fuselageCreator = theFuselageCreator;
		this._theWeight = new FuselageWeightsManager();
		
	}
	
	//------------------------------------------------------------------------------------
	// METHODS
	//------------------------------------------------------------------------------------
	public void calculateCG(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMap) {
		calculateCG(aircraft, MethodEnum.SFORZA);
		calculateCG(aircraft, MethodEnum.TORENBEEK_1982);
		
		if(!methodsMap.get(ComponentEnum.FUSELAGE).equals(MethodEnum.AVERAGE)) 
			_cg.setXLRF(_xCGMap.get(methodsMap.get(ComponentEnum.FUSELAGE)));
		else {
			_percentDifferenceXCG = new double[_xCGMap.size()];
			_cg.setXLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_cg.getXLRFref(), 
					_xCGMap,
					_percentDifferenceXCG,
					30.).getFilteredMean(), SI.METER));
		}
		_cg.calculateCGinBRF(ComponentEnum.FUSELAGE);
	}
	
	public void calculateCG(
			Aircraft aircraft, 
			MethodEnum method) {

		_cg = new CenterOfGravity();
		
		_cg.setLRForigin(_xApexConstructionAxes, _yApexConstructionAxes, _zApexConstructionAxes);
		_cg.set_xLRFref(_fuselageCreator.getFuselageLength().times(0.45));
		_cg.set_yLRFref(Amount.valueOf(0., SI.METER));
		_cg.set_zLRFref(Amount.valueOf(_zApexConstructionAxes.getEstimatedValue(), SI.METER));

		// Initialize _methodsList again to clear it
		// from old entries
		_methodsList = new ArrayList<MethodEnum>();

		switch(method) {

		// page 359 Sforza (2014) - Aircraft Design
		case SFORZA : { 
			_methodsList.add(method);

			_xCG = Amount.valueOf(
					_fuselageCreator.getFuselageLength().divide(_fuselageCreator.getFuselageFinenessRatio()).getEstimatedValue()*
					(_fuselageCreator.getNoseFinenessRatio()+ (_fuselageCreator.getFuselageFinenessRatio() - 5.)/1.8)
					, SI.METER);
			_xCGMap.put(method, _xCG);
		} break;

		// page 313 Torenbeek (1982)
		case TORENBEEK_1982 : { 
			_methodsList.add(method);

			if (aircraft.getPowerPlant().getEngineNumber() == 1 && 
					(aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.PISTON |
					aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.TURBOPROP)) {

				_xCG = _fuselageCreator.getFuselageLength().times(0.335);
			}

			if (aircraft.getPowerPlant().getMountingPosition() == EngineMountingPositionEnum.WING) {
				if ((aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.PISTON |
						aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.TURBOPROP)) {
					_xCG = _fuselageCreator.getFuselageLength().times(0.39); 
				} else {
					_xCG = _fuselageCreator.getFuselageLength().times(0.435);
				}
			}

			if (aircraft.getPowerPlant().getMountingPosition() == EngineMountingPositionEnum.REAR_FUSELAGE) {
				_xCG = _fuselageCreator.getFuselageLength().times(0.47);
			}

			if (aircraft.getPowerPlant().getMountingPosition() == EngineMountingPositionEnum.BURIED) {
				_xCG = _fuselageCreator.getFuselageLength().times(0.45);
			}

			_xCGMap.put(method, _xCG);
		} break;

		default : break;

		}

		_methodsMap.put(AnalysisTypeEnum.BALANCE, _methodsList);

	}
	
	public FuselageWeightsManager getTheWeight() {
		return _theWeight;
	}

	public void setTheWeight(FuselageWeightsManager _theWeight) {
		this._theWeight = _theWeight;
	}

	public FuselageBalanceManager getTheBalance() {
		return _theBalance;
	}

	public void setTheBalance(FuselageBalanceManager _theBalance) {
		this._theBalance = _theBalance;
	}

	//------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	//------------------------------------------------------------------------------------
	public FusDesDatabaseReader getFusDesDatabaseReader() {
		return _fusDesDatabaseReader;
	}

	public void setFusDesDatabaseReader(FusDesDatabaseReader _fusDesDatabaseReader) {
		this._fusDesDatabaseReader = _fusDesDatabaseReader;
	}

	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	public void setXApexConstructionAxes(Amount<Length> _xApexConstructionAxes) {
		this._xApexConstructionAxes = _xApexConstructionAxes;
	}

	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	public void setYApexConstructionAxes(Amount<Length> _yApexConstructionAxes) {
		this._yApexConstructionAxes = _yApexConstructionAxes;
	}

	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	public void setZApexConstructionAxes(Amount<Length> _zApexConstructionAxes) {
		this._zApexConstructionAxes = _zApexConstructionAxes;
	}

	public FuselageCreator getFuselageCreator() {
		return _fuselageCreator;
	}

	public void setFuselageCreator(FuselageCreator _fuselageCreator) {
		this._fuselageCreator = _fuselageCreator;
	}

	public Amount<Mass> getMass() {
		return _mass;
	}

	public void setMass(Amount<Mass> _mass) {
		this._mass = _mass;
	}

	public Amount<Mass> getMassEstimated() {
		return _massEstimated;
	}

	public void setMassEstimated(Amount<Mass> _massEstimated) {
		this._massEstimated = _massEstimated;
	}

	public Amount<Mass> getMassReference() {
		return _massReference;
	}

	public void setMassReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}

	public Map<MethodEnum, Amount<Mass>> getMassMap() {
		return _massMap;
	}

	public void setMassMap(Map<MethodEnum, Amount<Mass>> _massMap) {
		this._massMap = _massMap;
	}

	public Map<MethodEnum, Amount<Length>> getXCGMap() {
		return _xCGMap;
	}

	public void setXCGMap(Map<MethodEnum, Amount<Length>> _xCGMap) {
		this._xCGMap = _xCGMap;
	}

	public Map<AnalysisTypeEnum, List<MethodEnum>> getMethodsMap() {
		return _methodsMap;
	}

	public void setMethodsMap(Map<AnalysisTypeEnum, List<MethodEnum>> _methodsMap) {
		this._methodsMap = _methodsMap;
	}

	public List<MethodEnum> getMethodsList() {
		return _methodsList;
	}

	public void setMethodsList(List<MethodEnum> _methodsList) {
		this._methodsList = _methodsList;
	}

	public Double[] getPercentDifference() {
		return _percentDifference;
	}

	public void setPercentDifference(Double[] _percentDifference) {
		this._percentDifference = _percentDifference;
	}

	public Double getMassCorrectionFactor() {
		return _massCorrectionFactor;
	}

	public void setMassCorrectionFactor(Double _massCorrectionFactor) {
		this._massCorrectionFactor = _massCorrectionFactor;
	}

	public Amount<Length> getXCG() {
		return _xCG;
	}

	public void setXCG(Amount<Length> _xCG) {
		this._xCG = _xCG;
	}

	public Amount<Length> getXCGReference() {
		return _xCGReference;
	}

	public void setXCGReference(Amount<Length> _xCGReference) {
		this._xCGReference = _xCGReference;
	}

	public Amount<Length> getXCGEstimated() {
		return _xCGEstimated;
	}

	public void setXCGEstimated(Amount<Length> _xCGEstimated) {
		this._xCGEstimated = _xCGEstimated;
	}

	public Amount<Length> getZCGEstimated() {
		return _zCGEstimated;
	}

	public void setZCGEstimated(Amount<Length> _zCGEstimated) {
		this._zCGEstimated = _zCGEstimated;
	}

	public CenterOfGravity getCG() {
		return _cg;
	}

	public void setCG(CenterOfGravity _cg) {
		this._cg = _cg;
	}

	public double[] getPercentDifferenceXCG() {
		return _percentDifferenceXCG;
	}

	public void setPercentDifferenceXCG(double[] _percentDifferenceXCG) {
		this._percentDifferenceXCG = _percentDifferenceXCG;
	}
	
} 
