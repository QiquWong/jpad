package aircraft.components.nacelles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.powerplant.Engine;
import analyses.nacelles.NacelleBalanceManager;
import analyses.nacelles.NacelleWeightManager;
import configuration.MyConfiguration;

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
	
	private double _kExcr = 0.0;
	
	// calculators
	private NacelleWeightManager _theWeights;
	private NacelleBalanceManager _theBalance;
	
	//--------------------------------------------------------------------------------------------------
	// BUILDER
	public Nacelles(List<NacelleCreator> theNacelleCreatorList) {
		
		this._nacellesList = theNacelleCreatorList;
		this._nacellesNumber = theNacelleCreatorList.size();
		
		this._theWeights = new NacelleWeightManager();
		this._theBalance = new NacelleBalanceManager();
		
		this._nacelleEngineMap = new HashMap<>();
		
		this._distanceBetweenInboardNacellesY = _nacellesList.get(0).getYApexConstructionAxes().times(2);
		if (_nacellesNumber>2)
			this._distanceBetweenOutboardNacellesY = _nacellesList.get(_nacellesList.size()-1).getYApexConstructionAxes().times(2);
		
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

	public NacelleWeightManager getTheWeights() {
		return _theWeights;
	}

	public void setTheWeights(NacelleWeightManager _theWeights) {
		this._theWeights = _theWeights;
	}

	public NacelleBalanceManager getTheBalance() {
		return _theBalance;
	}

	public void setTheBalance(NacelleBalanceManager _theBalance) {
		this._theBalance = _theBalance;
	}
}