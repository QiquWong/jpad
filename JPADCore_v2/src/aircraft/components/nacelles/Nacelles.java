package aircraft.components.nacelles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.powerplant.Engine;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;

/** 
 * Manage all the nacelles of the aircraft
 * and the calculations associated with them
 * 
 * @author Lorenzo Attanasio
 *
 */
public class Nacelles {

	//--------------------------------------------------------------------------------------------------
	// VARIABLES DECLARATION
	private int _nacellesNumber;
	private List<NacelleCreator> _nacellesList;
	private Map<NacelleCreator, Engine> _nacelleEngineMap;
	private Amount<Area> _surfaceWetted;
	private Amount<Length> _distanceBetweenInboardNacellesY, _distanceBetweenOutboardNacellesY;
	
	private Amount<Mass> _totalMass;
	private List<Amount<Mass>> _massList;
	private CenterOfGravity _totalCG;
	private List<CenterOfGravity> _cgList;
	private double _percentTotalDifference;
	private Amount<Mass> _massReference;

	// TODO : move these ??? These should be retrieved from the NacelleAerodynamicManager
	private double _cD0Total, _cD0Parasite, _cD0Base;
	private double _kExcr = 0.0;
	
	//--------------------------------------------------------------------------------------------------
	// BUILDER
	public Nacelles(List<NacelleCreator> theNacelleCreatorList) {
		
		this._nacellesList = theNacelleCreatorList;
		this._nacellesNumber = theNacelleCreatorList.size();
		
		this._nacelleEngineMap = new HashMap<>();
		this._massList = new ArrayList<>();
		this._cgList = new ArrayList<>();
		
		this._distanceBetweenInboardNacellesY = _nacellesList.get(0).getYApexConstructionAxes().times(2);
		if (_nacellesNumber>2)
			this._distanceBetweenOutboardNacellesY = _nacellesList.get(2).getYApexConstructionAxes().times(2);
		
		populateEnginesMap();
		calculateSurfaceWetted();
		
	}
	
	//--------------------------------------------------------------------------------------------------
	// METHODS
	
	private void populateEnginesMap() {
		for(int i=0; i < _nacellesNumber; i++) {
			_nacelleEngineMap.put(_nacellesList.get(i), _nacellesList.get(i).getTheEngine());
		}
	}

	public void calculateSurfaceWetted() {
		_surfaceWetted = Amount.valueOf(0., SI.SQUARE_METRE);
		for(int i=0; i < _nacellesNumber; i++) {
			_surfaceWetted = _surfaceWetted.plus(_nacellesList.get(i).getSurfaceWetted()); 
		}
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder();
		
		sb.append("\t-------------------------------------\n")
		  .append("\tNacelles\n")
		  .append("\t-------------------------------------\n")
		  .append("\tNumber of nacelles: " + _nacellesNumber + "\n")
		  ;
		for(int i=0; i<this._nacellesList.size(); i++)
			sb.append("\t-------------------------------------\n")
			  .append("\tNacelle n° " + (i+1) + "\n")
			  .append("\t-------------------------------------\n")
			  .append(this._nacellesList.get(i).toString())
			  ;
		
		
		return sb.toString();
		
	}
	
	// TODO : move these ??
	public void initializeWeights(Aircraft theAircraft) {
		for(int i=0; i < _nacellesNumber; i++) {
			_nacellesList.get(i).initializeWeights(theAircraft);
		}
	}
	
	public void initializeBalance() {
		for(int i=0; i < _nacellesNumber; i++) {
			_nacellesList.get(i).initializeBalance();
		}
	}
	
	public void calculateMass(Aircraft theAircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {

		_totalMass = Amount.valueOf(0., SI.KILOGRAM);
		_massReference = theAircraft.getTheAnalysisManager().getTheWeights().getNacelleReferenceMass();
		initializeWeights(theAircraft);

		for(int i=0; i < _nacellesNumber; i++) {
			_nacellesList.get(i).getWeights().setMassReference(
					theAircraft.getTheAnalysisManager().getTheWeights().getNacelleReferenceMass()
						.divide(_nacellesNumber));
			_nacellesList.get(i).getWeights().calculateAll();
			if(!methodsMapWeights.get(ComponentEnum.NACELLE).equals(MethodEnum.AVERAGE))
				_nacellesList.get(i).getWeights().setMassEstimated(
						_nacellesList.get(i).getWeights().getMassMap().get(
								methodsMapWeights.get(ComponentEnum.NACELLE)
								)
						);
			_massList.add(_nacellesList.get(i).getWeights().getMassEstimated());
			_totalMass = _totalMass.plus(_nacellesList.get(i).getWeights().getMassEstimated());
			_massReference = _massReference.plus(theAircraft.getTheAnalysisManager().getTheWeights().getNacelleReferenceMass());
		}
		
		_percentTotalDifference = _totalMass.
				minus(_massReference).
				divide(_massReference).
				getEstimatedValue()*100.;
	}
	
	public CenterOfGravity calculateCG() {

		_totalCG = new CenterOfGravity();
		initializeBalance();
		
		for(int i=0; i < _nacellesNumber; i++) {
			_nacellesList.get(i).initializeBalance();
			_nacellesList.get(i).getBalance().calculateAll();
			_cgList.add(_nacellesList.get(i).getBalance().getCG());
			_totalCG = _totalCG.plus(_nacellesList.get(i).getBalance().getCG()
					.times(_nacellesList.get(i).getWeights().getMassEstimated().doubleValue(SI.KILOGRAM)));
		}

		_totalCG = _totalCG.divide(_totalMass.doubleValue(SI.KILOGRAM));
		return _totalCG;
	}

	//--------------------------------------------------------------------------------------------------
	// GETTERS & SETTERS

	public int getNacellesNumber() {
		return _nacellesNumber;
	}
	
	public void setNacellesNumber(int _nacellesNumber) {
		this._nacellesNumber = _nacellesNumber;
	}
	
	public List<NacelleCreator> getNacellesList() {
		return _nacellesList;
	}
	
	public void setNacellesList(List<NacelleCreator> _nacellesList) {
		this._nacellesList = _nacellesList;
	}
	
	public Map<NacelleCreator, Engine> getNacelleEngineMap() {
		return _nacelleEngineMap;
	}
	
	public void setNacelleEngineMap(Map<NacelleCreator, Engine> _nacelleEngineMap) {
		this._nacelleEngineMap = _nacelleEngineMap;
	}

	public Amount<Mass> getTotalMass() {
		return _totalMass;
	}
	
	public void setTotalMass(Amount<Mass> _totalMass) {
		this._totalMass = _totalMass;
	}
	
	public List<Amount<Mass>> getMassList() {
		return _massList;
	}
	
	public void setMassList(List<Amount<Mass>> _massList) {
		this._massList = _massList;
	}
	
	public CenterOfGravity getTotalCG() {
		return _totalCG;
	}
	
	public void setTotalCG(CenterOfGravity _totalCG) {
		this._totalCG = _totalCG;
	}

	public List<CenterOfGravity> getCGList() {
		return _cgList;
	}
	
	public void setCGList(List<CenterOfGravity> _cgList) {
		this._cgList = _cgList;
	}
	
	public double getPercentTotalDifference() {
		return _percentTotalDifference;
	}
	
	public double getCD0Total() {
		return _cD0Total;
	}
	
	public double getCD0Parasite() {
		return _cD0Parasite;
	}
	
	public double getCD0Base() {
		return _cD0Base;
	}
	
	public Amount<Mass> getMassReference() {
		return _massReference;
	}
	
	public void setMassReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}
	
	public Amount<Area> getSurfaceWetted() {
		return _surfaceWetted;
	}
	
	public void setSurfaceWetted(Amount<Area> _surfaceWetted) {
		this._surfaceWetted = _surfaceWetted;
	}

	public Amount<Length> getDistanceBetweenInboardNacellesY() {
		return _distanceBetweenInboardNacellesY;
	}

	public void setDistanceBetweenInboardNacellesY(Amount<Length> _distanceBetweenInboardNacellesY) {
		this._distanceBetweenInboardNacellesY = _distanceBetweenInboardNacellesY;
	}

	public Amount<Length> getDistanceBetweenOutboardNacellesY() {
		return _distanceBetweenOutboardNacellesY;
	}

	public void setDistanceBetweenOutboardNacellesY(Amount<Length> _distanceBetweenOutboardNacellesY) {
		this._distanceBetweenOutboardNacellesY = _distanceBetweenOutboardNacellesY;
	}

	public double getKExcr() {
		return _kExcr;
	}

	public void setKExcr(double _kExcr) {
		this._kExcr = _kExcr;
	}
}