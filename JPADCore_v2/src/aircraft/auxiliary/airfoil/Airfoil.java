package aircraft.auxiliary.airfoil;

import java.util.HashMap;
import java.util.Map;

import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AirfoilEnum;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.AirfoilStationEnum;
import configuration.enumerations.AirfoilTypeEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;

public class Airfoil {

	// NOTE: airfoil shape, means _unit chord_
	// NOTE: all X's and Z's are non-dimensional
	// NOTE: curvilinear coordinate oriented as follows: TE->upper->LE->lower

	private String _name;
	private AirfoilFamilyEnum _family;
	private AirfoilTypeEnum _type;
	private AirfoilCreator _theAirfoilCreator;

	/**
	 * This constructor creates an Airfoil object from the AirfoilCreator class
	 */
	public Airfoil(
			AirfoilCreator airfoilCreator,
			AerodynamicDatabaseReader aerodynamicDatabaseReader
			) {
		
		this._theAirfoilCreator = airfoilCreator;
		this._name = airfoilCreator.getName();
		this._type = airfoilCreator.getType();
		this._family = airfoilCreator.getFamily();
		
		this.aerodynamics = new Aerodynamics(airfoilCreator);
		this.geometry = new Geometry(airfoilCreator, aerodynamicDatabaseReader);
		
	}
	
	/**
	 * Builder that recognize the aircraft name and the station of airfoil.
	 * 
	 * @author Manuela Ruocco
	 */
	public Airfoil(AircraftEnum aircraftName, AirfoilStationEnum station, LiftingSurface ls, Double yLoc) {

		_id = ls.getId() + idCounter + "99";
		idCounter++;
		
		switch(aircraftName) {
		case ATR72:
			_type = AirfoilTypeEnum.CONVENTIONAL;

			_theLiftingSurface = ls;
			geometry = new Geometry(this, yLoc);
			aerodynamics = new Aerodynamics(this, aircraftName, station);
			
			break;
		
		// TODO: put inside Geometry and Aerodynamics B747-100B correct data (actually there are the same data in both ATR-72 and B747-100B
		case B747_100B:
			_type = AirfoilTypeEnum.MODERN_SUPERCRITICAL;

			_theLiftingSurface = ls;
			geometry = new Geometry(this, yLoc);
			aerodynamics = new Aerodynamics(this, aircraftName, station);
			break;
			
		case AGILE_DC1:
			_type = AirfoilTypeEnum.MODERN_SUPERCRITICAL; //TODO: have to check

			_theLiftingSurface = ls;
			geometry = new Geometry(this, yLoc);
			aerodynamics = new Aerodynamics(this,aircraftName, station);
			break;
		}
	}
	
	public String getName() {
		return _name;
	}

	public void setName(String _name) {
		this._name = _name;
	}

	public AirfoilFamilyEnum getFamily() {
		return _family;
	}

	public void setFamily(AirfoilFamilyEnum _family) {
		this._family = _family;
	}
	
	public AirfoilTypeEnum getType() {
		return _type;
	}

	public void setType(AirfoilTypeEnum _type) {
		this._type = _type;
	}
	
	public AirfoilCreator getAirfoilCreator() {
		return _theAirfoilCreator;
	}

	public void setAirfoilCreator(AirfoilCreator _theAirfoilCreator) {
		this._theAirfoilCreator = _theAirfoilCreator;
	}

} // end-of-class
