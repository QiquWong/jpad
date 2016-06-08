package aircraft.auxiliary.airfoil;

import java.util.HashMap;
import java.util.Map;

import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AirfoilEnum;
import configuration.enumerations.AirfoilStationEnum;
import configuration.enumerations.AirfoilTypeEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;

public class Airfoil {

	// NOTE: airfoil shape, means _unit chord_
	// NOTE: all X's and Z's are non-dimensional
	// NOTE: curvilinear coordinate oriented as follows: TE->upper->LE->lower

	private String _id = "";
	public static int idCounter = 0;
	public static int nAirfoil = 0;
	public static final Integer _xmlLevel = 3;

	private static Map<AirfoilEnum, Double> _kWaveDragMap = new HashMap<AirfoilEnum, Double> ();

	AirfoilEnum _family;
	AirfoilTypeEnum _type;
	Geometry geometry;
	Aerodynamics aerodynamics;
	double _chordLocal;
	private LiftingSurface _theLiftingSurface;

	/**
	 * This constructor creates an Airfoil object from the AirfoilCreator class
	 */
	public Airfoil(
			AirfoilCreator airfoilCreator,
			AerodynamicDatabaseReader aerodynamicDatabaseReader
			) {
		
		this._id = airfoilCreator.getID();
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
			_family = AirfoilEnum.DFVLR_R4;
			_type = AirfoilTypeEnum.MODERN_SUPERCRITICAL; //TODO: have to check

			_theLiftingSurface = ls;
			geometry = new Geometry(this, yLoc);
			aerodynamics = new Aerodynamics(this,aircraftName, station);
			break;
		}
	}
	
	//*********************************************************************
	// These methods initialize an airfoil from the default ones.
	public void initialize(Airfoil airfoil, AirfoilEnum airfoilName) {
		
		// FIXME : Geometry initialize always to the NACA 66(3)-418
		//		  or to the NACA 0012 in case of HTail or VTail
		
		geometry = new Geometry(airfoil);	
		aerodynamics = new Aerodynamics(airfoil, airfoilName);
		
	}
	//*********************************************************************	
	
	public static void populateKWaveDragMap() {

		_kWaveDragMap.put(AirfoilEnum.NACA63_209, 0.095);
		_kWaveDragMap.put(AirfoilEnum.NACA64_208, 0.080);
		_kWaveDragMap.put(AirfoilEnum.NACA65_209, 0.071);
		_kWaveDragMap.put(AirfoilEnum.NACA66_209, 0.050);
		_kWaveDragMap.put(AirfoilEnum.NACA63_412, 0.080);
		_kWaveDragMap.put(AirfoilEnum.NACA64_412, 0.068);
		_kWaveDragMap.put(AirfoilEnum.NACA65_410, 0.066);
	}

	public Map<AirfoilEnum, Double> get_kWaveDragMap() {
		return _kWaveDragMap;
	}

	public void set_kWaveDragMap(Map<AirfoilEnum, Double> kWaveDragMap) {
		_kWaveDragMap = kWaveDragMap;
	}

	public AirfoilTypeEnum get_type() {
		return _type;
	}

	public void set_type(AirfoilTypeEnum _type) {
		this._type = _type;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public Aerodynamics getAerodynamics() {
		return aerodynamics;
	}

	public AirfoilEnum get_family() {
		return _family;
	}

	public void setAerodynamics(Aerodynamics aerodynamics) {
		this.aerodynamics = aerodynamics;
	}

	public void set_family(AirfoilEnum _family) {
		this._family = _family;
	}

	public String getId() {
		return _id;
	}
	
	public String getIdNew() {
		@SuppressWarnings("static-access")
		String id = _theLiftingSurface.getId() + "af" + nAirfoil;
		nAirfoil++;
		return id;
	}

	public double get_chordLocal() {
		return _chordLocal;
	}

	public void set_chordLocal(double _chordLocal) {
		this._chordLocal = _chordLocal;
	}

	public LiftingSurface get_theLiftingSurface() {
		return _theLiftingSurface;
	}

} // end-of-class
