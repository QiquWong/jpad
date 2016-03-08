package aircraft.components.nacelles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Area;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.powerPlant.Engine;
import aircraft.components.powerPlant.PowerPlant;
import configuration.enumerations.AircraftEnum;
import standaloneutils.customdata.CenterOfGravity;

/** 
 * Manage all the nacelles of the aircraft
 * and the calculations associated with them
 * 
 * @author Lorenzo Attanasio
 *
 */
public class NacellesManager {

	private int _nacellesNumber = 1;
	private Amount<Mass> _totalMass;
	private CenterOfGravity _totalCG;
	private Boolean _nacellesEqual = false;
	private Double _cd0Total, _cd0Parasite, _cd0Base;
	private Double _percentTotalDifference;
	private Amount<Mass> _massReference;
	private Aircraft _theAircraft;
	private Double _distanceBetweenInboardNacellesY, _distanceBetweenOutboardNacellesY;
	private Amount<Area> _surfaceWetted;

	private List<Nacelle> _nacellesList = new ArrayList<Nacelle>();
	private List<CenterOfGravity> _cgList = new ArrayList<CenterOfGravity>();
	private List<Amount<Mass>> _massList = new ArrayList<Amount<Mass>>();
	private Map<Nacelle, Engine> _nacelleEngineMap = new HashMap<Nacelle, Engine>();

	public NacellesManager(Aircraft aircraft) {
		_theAircraft = aircraft;
		_nacellesNumber = 2;
		_massReference = Amount.valueOf(0., SI.KILOGRAM);
	}

	/**
	 * Overload of the NacellesManager that recognize aircraft name and sets the relative values.
	 * 
	 * @author Vittorio Trifari
	 */
	public NacellesManager(AircraftEnum aircraftName, Aircraft aircraft) {
		
		switch(aircraftName) {
		
		case ATR72:
			_theAircraft = aircraft;
			_nacellesNumber = 2;
			_massReference = Amount.valueOf(409.4000, SI.KILOGRAM);
			break;
		
		case B747_100B:
			_theAircraft = aircraft;
			_nacellesNumber = 4;
			_massReference = Amount.valueOf(1184.2500, SI.KILOGRAM);
			break;
			
		case AGILE_DC1:
			_theAircraft = aircraft;
			_nacellesNumber = 2;
			_massReference = Amount.valueOf(380., SI.KILOGRAM); // ADAS
			break;
		}
	}

	public void initializeNacelles() {

		for(int i=0; i < _nacellesNumber; i++) {
			_nacellesList.add(new Nacelle("Nacelle_" + i, "", 0.0, 0.0, 0.0, _theAircraft));
		}

		_distanceBetweenInboardNacellesY = 2*_nacellesList.get(0).get_Y0().getEstimatedValue();
		if (_nacellesNumber>2)
			_distanceBetweenOutboardNacellesY = 2*_nacellesList.get(2).get_Y0().getEstimatedValue();
	}

	/**
	 * Overload of the default initializator that recognize aircraft name and initialize it's nacelles.
	 * 
	 * @author Vittorio Trifari
	 */
	public void initializeNacelles(AircraftEnum aircraftName) {

		switch(aircraftName) {
		
		case ATR72:
			_nacellesNumber = 2;
				_nacellesList.add(new Nacelle(aircraftName, "Nacelle_1", "", 8.6100, 4.0500, 1.3255, _theAircraft));
				_nacellesList.add(new Nacelle(aircraftName, "Nacelle_2", "", 8.6100, 4.0500, 1.3255, _theAircraft));
			break;
			
		case B747_100B:
			_nacellesNumber = 4;
			for(int i=0; i < _nacellesNumber; i++) {
				_nacellesList.add(new Nacelle(aircraftName, "Nacelle_" + i, "", 0.0, 0.0, 0.0, _theAircraft));
			}
			break;
			
		case AGILE_DC1:
			_nacellesNumber = 2;
//			for(int i=0; i < _nacellesNumber; i++) {
//				_nacellesList.add(new Nacelle(aircraftName, "Nacelle_" + i, "", 0.0, 0.0, 0.0, _theAircraft));
				_nacellesList.add(new Nacelle(aircraftName, "Nacelle_1", "", 12.891, 4.968, -1.782, _theAircraft));
				_nacellesList.add(new Nacelle(aircraftName, "Nacelle_2", "", 12.891, -4.968, -1.782, _theAircraft));
//			}
			break;
		}

		_distanceBetweenInboardNacellesY = 2*_nacellesList.get(0).get_Y0().getEstimatedValue();
		if (_nacellesNumber>2)
			_distanceBetweenOutboardNacellesY = 2*_nacellesList.get(2).get_Y0().getEstimatedValue();
	}
	
	public void setEngines() {
		for(int i=0; i < _nacellesNumber; i++) {
			if (PowerPlant.engineList.get(i) != null) {
				_nacellesList.get(i).set_theEngine(PowerPlant.engineList.get(i));
				_nacelleEngineMap.put(_nacellesList.get(i), PowerPlant.engineList.get(i));

			} else {
				System.out.println("There is no engine to associate to the " + i + "-th nacelle");
			}
		}
	}

	public void calculateDistanceBetweenNacelles() {
		//		_distanceBetweenInboardNacellesY = 2*_Y0.getEstimatedValue();
		//		_distanceBetweenOutboardNacellesY = 0.;		
	}

	public void initializeWeights() {
		for(int i=0; i < _nacellesNumber; i++) {
			_nacellesList.get(i).initializeWeights();
		}
	}

	public void calculateSurfaceWetted() {

		_surfaceWetted = Amount.valueOf(0., SI.SQUARE_METRE);
		for(int i=0; i < _nacellesNumber; i++) {
			_nacellesList.get(i).calculateSurfaceWetted();
			_surfaceWetted = _surfaceWetted.plus(_nacellesList.get(i).get_surfaceWetted()); 
		}

	}

	/**
	 * @author Lorenzo Attanasio
	 */
	public void calculateMass() {

		_totalMass = Amount.valueOf(0., SI.KILOGRAM);
		initializeWeights();

		for(int i=0; i < _nacellesNumber; i++) {
			_nacellesList.get(i).getWeights().calculateAll();
			_massList.add(_nacellesList.get(i).getWeights().get_massEstimated());
			_totalMass = _totalMass.plus(_nacellesList.get(i).getWeights().get_massEstimated());
			_massReference = _massReference.plus(_nacellesList.get(i).getWeights().get_massReference());
		}
		

		_percentTotalDifference = _totalMass.
				minus(_massReference).
				divide(_massReference).
				getEstimatedValue()*100.;
	}

	public CenterOfGravity calculateCG() {

		_totalCG = new CenterOfGravity();
		for(int i=0; i < _nacellesNumber; i++) {
			_nacellesList.get(i).getBalance().calculateAll();
//			_cgList.add(_nacellesList.get(i).get_cg());
			_cgList.add(_nacellesList.get(i).getBalance().get_cg());
			_totalCG = _totalCG.plus(_nacellesList.get(i).getBalance().get_cg()
					.times(_nacellesList.get(i).getWeights().get_massEstimated().doubleValue(SI.KILOGRAM)));
		}

		_totalCG = _totalCG.divide(_totalMass.doubleValue(SI.KILOGRAM));
		return _totalCG;
	}

	/**
	 * @author Lorenzo Attanasio
	 */
	public void calculateAerodynamics() {
		_cd0Parasite = 0.;
		_cd0Base = 0.;
		_cd0Total = 0.;

		for(int i=0; i < _nacellesNumber; i++) {
			_nacellesList.get(i).getAerodynamics().calculateAll();
			_cd0Parasite = _cd0Parasite + _nacellesList.get(i).getAerodynamics().get_cd0Parasite();
			_cd0Base = _cd0Base + _nacellesList.get(i).getAerodynamics().get_cd0Base();
			_cd0Total = _cd0Total + _nacellesList.get(i).getAerodynamics().get_cd0Total();
		}
	}

	public int get_nacellesNumber() {
		return _nacellesNumber;
	}

	public void set_nacellesNumber(int _nacellesNumber) {
		this._nacellesNumber = _nacellesNumber;
	}

	public Amount<Mass> get_totalMass() {
		return _totalMass;
	}

	public List<Nacelle> get_nacellesList() {
		return _nacellesList;
	}

	public Map<Nacelle, Engine> get_nacelleEngineMap() {
		return _nacelleEngineMap;
	}

	public Double get_cD0Total() {
		return _cd0Total;
	}

	public Boolean get_nacellesEqual() {
		return _nacellesEqual;
	}

	public void set_nacellesEqual(Boolean _nacellesEqual) {
		this._nacellesEqual = _nacellesEqual;
	}

	public void set_totalMass(Amount<Mass> _totalMass) {
		this._totalMass = _totalMass;
	}

	public Double get_cD0Parasite() {
		return _cd0Parasite;
	}

	public Double get_cd0Base() {
		return _cd0Base;
	}

	public Double get_distanceBetweenInboardNacellesY() {
		return _distanceBetweenInboardNacellesY;
	}

	public void set_distanceBetweenInboardNacellesY(Double _distanceBetweenNacellesY) {
		this._distanceBetweenInboardNacellesY = _distanceBetweenNacellesY;
	}

	public Double get_distanceBetweenOutboardNacellesY() {
		return _distanceBetweenOutboardNacellesY;
	}

	public void set_distanceBetweenOutboardNacellesY(
			Double _distanceBetweenOutboardNacellesY) {
		this._distanceBetweenOutboardNacellesY = _distanceBetweenOutboardNacellesY;
	}


	public List<CenterOfGravity> get_cgList() {
		return _cgList;
	}


	public Amount<Area> get_surfaceWetted() {
		return _surfaceWetted;
	}


	public static String getId() {
		return "7";
	}


	public List<Amount<Mass>> get_massList() {
		return _massList;
	}

	public Amount<Mass> get_massReference() {
		return _massReference;
	}

	public void set_massReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}

}
