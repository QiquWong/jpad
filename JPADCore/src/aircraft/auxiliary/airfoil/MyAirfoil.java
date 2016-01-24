package aircraft.auxiliary.airfoil;

import java.util.HashMap;
import java.util.Map;

import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.AirfoilTypeEnum;

public class MyAirfoil {

	// NOTE: airfoil shape, means _unit chord_
	// NOTE: all X's and Z's are non-dimensional
	// NOTE: curvilinear coordinate oriented as follows: TE->upper->LE->lower

	private String _id = "";
	public static int idCounter = 0;
	public static int nAirfoil = 0;
	private String _xmlName;
	public static final Integer _xmlLevel = 3;

	private static Map<AirfoilFamilyEnum, Double> _kWaveDragMap = new HashMap<AirfoilFamilyEnum, Double> ();

	AirfoilFamilyEnum _family;
	AirfoilTypeEnum _type;
	Geometry geometry;
	Aerodynamics aerodynamics;
	double _chordLocal;
	private LiftingSurface _theLiftingSurface;

	//	public MyAirfoil( 
	//			Double yLoc,
	//			Double thicknessOverChordunit, 
	//			Amount<Angle> alphaZeroLift,
	//			Double liftCoefficientGradient,
	//			Amount<Angle> alphaStar,
	//			Double liftCoefficientStar,
	//			Amount<Angle> alphaStall,
	//			Double liftCoefficientMax,
	//			Double dragCoefficientMin,
	//			Double liftCoefficientAtCdMin,
	//			Double kExponentDragPolar,
	//			Double aerodynamicCenterX,
	//			Double pitchingMomentCoefficientAC,
	//			Double pitchingMomentCoefficientACStall,
	//			Double reynoldsCruise,
	//			Double reynoldsNumberStall
	//			) {
	//
	//		_etaLocation = yLoc;
	//		_thicknessOverChordUnit = thicknessOverChordunit; 
	//		_alphaZeroLift = alphaZeroLift;
	//		_clAlpha = liftCoefficientGradient;
	//		_alphaStar = alphaStar;
	//		_clStar = liftCoefficientStar;
	//		_alphaStall = alphaStall;
	//		_clMax = liftCoefficientMax;
	//		_cdMin = dragCoefficientMin;
	//		_clAtCdMin = liftCoefficientAtCdMin;
	//		_mExponentDragPolar = kExponentDragPolar;
	//		_aerodynamicCenterX = aerodynamicCenterX;
	//		_cmAC = pitchingMomentCoefficientAC;
	//		_cmACStall = pitchingMomentCoefficientACStall;
	//		_reynoldsCruise = reynoldsCruise;
	//		_reynoldsNumberStall = reynoldsNumberStall;
	//
	//	}

	/** 
	 * Initialize an airfoil with default values
	 */
	public MyAirfoil(LiftingSurface ls, Double yLoc) {

		_id = ls.getObjectId() + idCounter + "99";
		idCounter++;
		_family = AirfoilFamilyEnum.NACA63_209;
		_type = AirfoilTypeEnum.CONVENTIONAL;
		
		_theLiftingSurface = ls;
		geometry = new Geometry(this, yLoc);
		aerodynamics = new Aerodynamics(this);
	}
	
	public MyAirfoil(LiftingSurface ls, Double yLoc, String name) {

		_id = ls.getObjectId() + idCounter + "99";
		idCounter++;
		_family = AirfoilFamilyEnum.NACA63_209;
		_type = AirfoilTypeEnum.CONVENTIONAL;
		
		_theLiftingSurface = ls;
		geometry = new Geometry(this, yLoc);
		aerodynamics = new Aerodynamics(this, name);
	}
	
	/**
	 * Overload of the builder that recognize the aircraft name and sets it's airfoils values.
	 * 
	 * @author Vittorio Trifari
	 */
	public MyAirfoil(String aircraftName, LiftingSurface ls, Double yLoc) {

		_id = ls.getObjectId() + idCounter + "99";
		idCounter++;
		
		switch(aircraftName) {
		case "ATR-72":
			_family = AirfoilFamilyEnum.NACA63_209;
			_type = AirfoilTypeEnum.CONVENTIONAL;

			_theLiftingSurface = ls;
			geometry = new Geometry(this, yLoc);
			aerodynamics = new Aerodynamics(this);
			break;
		
		// TODO: put inside Geometry and Aerodynamics B747-100B correct data (actually there are the same data in both ATR-72 and B747-100B
		case "B747-100B":
			_family = AirfoilFamilyEnum.NACA63_209;
			_type = AirfoilTypeEnum.MODERN_SUPERCRITICAL;

			_theLiftingSurface = ls;
			geometry = new Geometry(this, yLoc);
			aerodynamics = new Aerodynamics(this);
			break;
		}
	}
	
	
	
	public MyAirfoil() {
		geometry = new Geometry(this, 10000.0);
		aerodynamics = new Aerodynamics(this);
		_type = AirfoilTypeEnum.CONVENTIONAL;
	}
	
	public MyAirfoil(LiftingSurface ls) {
		_theLiftingSurface = ls;
		geometry = new Geometry(this, 10000.0);
		aerodynamics = new Aerodynamics(this);
		_type = AirfoilTypeEnum.CONVENTIONAL;
	}
	
	
	public void initialize(LiftingSurface ls, double yLoc) {
		_theLiftingSurface = ls;
		initializeGeometry(ls, yLoc);	
		initializeAerodynamics();
	}

	public void initializeGeometry(LiftingSurface ls, double yLoc) {
		_chordLocal = _theLiftingSurface.getChordAtYActual(yLoc);
	}

	public void initializeAerodynamics() {
		aerodynamics = new Aerodynamics(this);		
	}

	public static void populateKWaveDragMap() {

		_kWaveDragMap.put(AirfoilFamilyEnum.NACA63_209, 0.095);
		_kWaveDragMap.put(AirfoilFamilyEnum.NACA64_208, 0.080);
		_kWaveDragMap.put(AirfoilFamilyEnum.NACA65_209, 0.071);
		_kWaveDragMap.put(AirfoilFamilyEnum.NACA66_209, 0.050);
		_kWaveDragMap.put(AirfoilFamilyEnum.NACA63_412, 0.080);
		_kWaveDragMap.put(AirfoilFamilyEnum.NACA64_412, 0.068);
		_kWaveDragMap.put(AirfoilFamilyEnum.NACA65_410, 0.066);
	}

	public String get_xmlName() {
		return _xmlName;
	}

	public void set_xmlName(String _xmlDescription) {
		this._xmlName = _xmlDescription;
	}

	public Map<AirfoilFamilyEnum, Double> get_kWaveDragMap() {
		return _kWaveDragMap;
	}

	public void set_kWaveDragMap(Map<AirfoilFamilyEnum, Double> kWaveDragMap) {
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

	public AirfoilFamilyEnum get_family() {
		return _family;
	}

	public void set_family(AirfoilFamilyEnum _family) {
		this._family = _family;
	}

	public String getId() {
		return _id;
	}
	
	public String getIdNew() {
		String id = _theLiftingSurface.getObjectId() + "af" + nAirfoil;
		nAirfoil++;
		return id;
	}

	public double get_chordLocal() {
		return _chordLocal;
	}

	public LiftingSurface get_theLiftingSurface() {
		return _theLiftingSurface;
	}

} // end-of-class
