package aircraft.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Volume;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.componentmodel.Component;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.FuelFractionDatabaseReader;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

/** 
 * The fuel tank is supposed to be a prismoid with base dimensions 
 * a1, b1, s1, and a2, b2, s2. 
 * 
 * a1 and a2 are on y axis while b1 and b2 are on z axis; 
 * s1 and s2 are the inboard (LE) and outboard (TE) surfaces of the fuel tank.
 * 
 * The fuel tank is supposed to be contained in the wing; 
 * the class defines only half of the fuel tank (the whole tank is symmetric with respect to xz plane).
 *  
 * @author Lorenzo Attanasio
 * @see Torenbeek 1982 page 467
 */
public class FuelTank extends Component{

	private ComponentEnum _type;
	
	private FuelFractionDatabaseReader fuelFractionDatabase;
	
	private Amount<Mass> _massEstimated;
	private Amount<Volume> _volumeEstimated;

	private Amount<Length> 
	_length, _a1, _b1,
	_a2, _b2;

	private Amount<Area> _s1, _s2;
	private CenterOfGravity _cg;
	private ArrayList<MethodEnum> _methodsList;
	private Amount<Length> _xCG;
	private Amount<Length> _yCG;
	private Amount<Length> _zCG;
	private Amount<Length> _X0;
	private Amount<Length> _Y0;
	private Amount<Length> _Z0;
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
	private Map <MethodEnum, Amount<Length>> _xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	private Map <MethodEnum, Amount<Length>> _yCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	private Double[] _percentDifferenceXCG;
	private Double[] _percentDifferenceYCG;
	
	private Amount<Volume> _fuelVolume;
	private Amount<VolumetricDensity> _fuelDensity;
	private Amount<Mass> _fuelMass;
	private Amount<Force> _fuelWeight;


	public FuelTank(String name, String description, double x, double y,double z) {

		super("", name, description, x, y, z);
		_X0 = Amount.valueOf(x, SI.METER);
		_Y0 = Amount.valueOf(y, SI.METER);
		_Z0 = Amount.valueOf(z, SI.METER);
		
		_type = ComponentEnum.FUEL_TANK;
		_cg = new CenterOfGravity();
		
		// Jet A1 fuel density
		_fuelDensity = Amount.valueOf(0.804, MyUnits.KILOGRAM_LITER);

		_fuelMass = Amount.valueOf(3000., SI.KILOGRAM);
		_fuelVolume = Amount.valueOf(_fuelMass.divide(_fuelDensity).getEstimatedValue(), NonSI.LITER);
	}

	/**
	 * Overload of the previous builder that recognize aircraft name and sets it's data.
	 * 
	 * @author Vittorio Trifari
	 */
	public FuelTank(String aircraftName, String name, String description, double x, double y,double z) {

		super("", name, description, x, y, z);
		_X0 = Amount.valueOf(x, SI.METER);
		_Y0 = Amount.valueOf(y, SI.METER);
		_Z0 = Amount.valueOf(z, SI.METER);
		
		_type = ComponentEnum.FUEL_TANK;
		_cg = new CenterOfGravity();
		
		// Jet A1 fuel density
		_fuelDensity = Amount.valueOf(0.804, MyUnits.KILOGRAM_LITER);

		switch(aircraftName) {
		
		case "ATR-72":
			_fuelMass = Amount.valueOf(5000., SI.KILOGRAM);
			_fuelVolume = Amount.valueOf(_fuelMass.divide(_fuelDensity).getEstimatedValue(), NonSI.LITER);
			break;
			
		case "B747-100B":
			_fuelMass = Amount.valueOf(147437.52, SI.KILOGRAM);
			_fuelVolume = Amount.valueOf(_fuelMass.divide(_fuelDensity).getEstimatedValue(), NonSI.LITER);
			break;
		}
	}

	
	public void calculateGeometry(Aircraft aircraft) {
		
		estimateDimensions(aircraft);
		calculateArea();
		calculateVolume();
	}
	
	public void calculateFuel() {
		_fuelMass = Amount.valueOf(_fuelDensity.times(_fuelVolume).getEstimatedValue(), SI.KILOGRAM);
		_fuelWeight = _fuelMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
	}
	
	public void calculateVolume() {

		_volumeEstimated = Amount.valueOf(
				1/3 * _length.getEstimatedValue() * 
				(_s1.getEstimatedValue() 
						+ Math.sqrt((_s1.times(_s2)).getEstimatedValue()) 
						+ _s2.getEstimatedValue()) 
						, Volume.UNIT);
	}

	/** 
	 * Calculate areas from base size
	 * 
	 * @param theAircraft
	 */
	@SuppressWarnings("unchecked")
	public void calculateArea() {

		_s1 = (Amount<Area>) _a1.times(_b1);
		_s2 = (Amount<Area>) _a2.times(_b2);
	}

	/** 
	 * Estimate dimensions of the fuel tank;
	 * use this method only if the user doesn't give
	 * any information on fuel tank size
	 */
	public void estimateDimensions(Aircraft aircraft) {

		// Approximate length of fuel tank in half wing
		// The user can also override it
		_a1 = (aircraft.get_wing().get_span().divide(2)).times(0.75);
		_a2 = (aircraft.get_wing().get_span().divide(2)).times(0.75);
		_b1 = (aircraft.get_wing().get_meanAerodChordActual().times(aircraft.get_wing().get_thicknessMean()));
		_b2 = (aircraft.get_wing().get_meanAerodChordActual().times(aircraft.get_wing().get_thicknessMean()));
		_length = aircraft.get_wing().get_meanAerodChordActual().times(0.75);
	}

	public void calculateCG(Aircraft aircraft, OperatingConditions conditions) {
		calculateCG(aircraft, conditions, MethodEnum.TORENBEEK_1982);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void calculateCG(
			Aircraft aircraft, 
			OperatingConditions conditions,
			MethodEnum method) {

		_cg.setLRForigin(_X0, _Y0, _Z0);
		_cg.set_xLRFref((_a1.plus(_a2)).divide(2));
		_cg.set_yLRFref(Amount.valueOf(0., SI.METER));
		_cg.set_zLRFref((_b1.plus(_b2)).divide(2));

		// Initialize _methodsList again to clear it
		// from old entries
		_methodsList = new ArrayList<MethodEnum>();

		_xCG = Amount.valueOf(0., SI.METER);
		_yCG = Amount.valueOf(0., SI.METER);
		_zCG = Amount.valueOf(0., SI.METER);

		switch(method) {

		// page 359 Sforza (2014) - Aircraft Design
		// page 313 Torenbeek (1982)
		case TORENBEEK_1982 : { 
			_methodsList.add(method);

			_yCG = Amount.valueOf(
					0.0 
					, SI.METER);

			_xCG = Amount.valueOf(
					_length.divide(4).getEstimatedValue()*
					(_s1.getEstimatedValue() 
							+ 3*_s2.getEstimatedValue() 
							+ 2*Math.sqrt(_s1.getEstimatedValue()*_s2.getEstimatedValue()))
							/(_s1.getEstimatedValue() 
									+ _s2.getEstimatedValue() 
									+ Math.sqrt(_s1.getEstimatedValue()*_s2.getEstimatedValue()))
									, SI.METER);

			//				System.out.println("x: " + _xCG 
			//				+ ", y: " + _yCG 
			//				+ ", xLE: " + getXLEAtYEquivalent(_yCG.getEstimatedValue()));
			_xCGMap.put(method, _xCG);
			_yCGMap.put(method, _yCG);

		} break;

		default : break;

		}

		_methodsMap.put(AnalysisTypeEnum.BALANCE, _methodsList);
		_percentDifferenceXCG = new Double[_xCGMap.size()];
		_percentDifferenceYCG = new Double[_yCGMap.size()];

		_cg.set_xLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.get_xLRFref(), 
				_xCGMap,
				_percentDifferenceXCG,
				100.).getFilteredMean(), SI.METER));

		_cg.set_yLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.get_yLRFref(), 
				_yCGMap,
				_percentDifferenceYCG,
				100.).getFilteredMean(), SI.METER));

		_cg.calculateCGinBRF();

	}


	public void calculateMass() {

	}

	public Amount<Volume> get_volumeEstimated() {
		return _volumeEstimated;
	}

	public void set_volumeEstimated(Amount<Volume> _volumeEstimated) {
		this._volumeEstimated = _volumeEstimated;
	}

	public Amount<Length> get_length() {
		return _length;
	}

	public void set_length(Amount<Length> _length) {
		this._length = _length;
	}

	public Amount<Length> get_a1() {
		return _a1;
	}

	public void set_a1(Amount<Length> _a1) {
		this._a1 = _a1;
	}

	public Amount<Length> get_b1() {
		return _b1;
	}

	public void set_b1(Amount<Length> _b1) {
		this._b1 = _b1;
	}

	public Amount<Length> get_a2() {
		return _a2;
	}

	public void set_a2(Amount<Length> _a2) {
		this._a2 = _a2;
	}

	public Amount<Length> get_b2() {
		return _b2;
	}

	public void set_b2(Amount<Length> _b2) {
		this._b2 = _b2;
	}

	public Amount<Area> get_s1() {
		return _s1;
	}

	public void set_s1(Amount<Area> _s1) {
		this._s1 = _s1;
	}

	public Amount<Area> get_s2() {
		return _s2;
	}

	public void set_s2(Amount<Area> _s2) {
		this._s2 = _s2;
	}

	public Amount<Length> get_zCG() {
		return _zCG;
	}

	public void set_zCG(Amount<Length> _zCG) {
		this._zCG = _zCG;
	}

	public Map<MethodEnum, Amount<Length>> get_xCGMap() {
		return _xCGMap;
	}

	public void set_xCGMap(Map<MethodEnum, Amount<Length>> _xCGMap) {
		this._xCGMap = _xCGMap;
	}

	public Map<MethodEnum, Amount<Length>> get_yCGMap() {
		return _yCGMap;
	}

	public void set_yCGMap(Map<MethodEnum, Amount<Length>> _yCGMap) {
		this._yCGMap = _yCGMap;
	}

	public CenterOfGravity get_cg() {
		return _cg;
	}

	public Double[] get_percentDifferenceXCG() {
		return _percentDifferenceXCG;
	}

	public void set_percentDifferenceXCG(Double[] _percentDifferenceXCG) {
		this._percentDifferenceXCG = _percentDifferenceXCG;
	}

	public Double[] get_percentDifferenceYCG() {
		return _percentDifferenceYCG;
	}

	public void set_percentDifferenceYCG(Double[] _percentDifferenceYCG) {
		this._percentDifferenceYCG = _percentDifferenceYCG;
	}

	public Amount<Volume> get_fuelVolume() {
		return _fuelVolume;
	}

	public void set_fuelVolume(Amount<Volume> _fuelVolume) {
		this._fuelVolume = _fuelVolume;
	}

	public Amount<VolumetricDensity> get_fuelDensity() {
		return _fuelDensity;
	}

	public void set_fuelDensity(Amount<VolumetricDensity> _fuelDensity) {
		this._fuelDensity = _fuelDensity;
	}

	public Amount<Mass> get_fuelMass() {
		return _fuelMass;
	}

	public void set_fuelMass(Amount<Mass> _fuelMass) {
		this._fuelMass = _fuelMass;
	}

	public static String getId() {
		return "5";
	}

	public FuelFractionDatabaseReader getFuelFractionDatabase() {
		return fuelFractionDatabase;
	}

	public void setFuelFractionDatabase(FuelFractionDatabaseReader fuelFractionDatabase) {
		this.fuelFractionDatabase = fuelFractionDatabase;
	}
}
