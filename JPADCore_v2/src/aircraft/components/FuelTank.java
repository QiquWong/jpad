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
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.FuelFractionDatabaseReader;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.CenterOfGravity;

/** 
 * The fuel tank is supposed to be make up of a series of prismoids from the root station to the 85% of the
 * wing semispan. The separation of each prismoid from the other is make by the kink airfoil stations (more
 * than one on a multi-panel wing). 
 * 
 * Each prismoid is defined from the the inner spanwise section, the outer spanwise section and the 
 * distance between the two airfoil stations. Furthermore, each section of the prismoid is defined by:
 * 
 *    - the airfoil thickness related to the main spar x station
 *    - the airfoil thickness related to the second spar x station
 *    - the distance between the two spar stations. 
 * 
 * The spar stations can be set for a default aircraft (for example 25% - 55%) 
 * or can be read from the wing file.
 * 
 * The fuel tank is supposed to be contained in the wing; 
 * the class defines only half of the fuel tank (the whole tank is symmetric with respect
 * to xz plane).
 *  
 * @author Vittorio Trifari
 * 
 */
public class FuelTank implements IFuelTank {

	private String _id;
	
	private Amount<Length> _xApexConstructionAxes;
	private Amount<Length> _yApexConstructionAxes;
	private Amount<Length> _zApexConstructionAxes;
	
	private FuelFractionDatabaseReader fuelFractionDatabase;
	
	private Amount<Mass> _massEstimated;
	private Amount<Volume> _volumeEstimated;

	private Double _mainSparNormalizedStation;
	private Double _secondarySparNormalizedStation;
	List<Amount<Length>> _thicknessAtMainSpar;
	List<Amount<Length>> _thicknessAtSecondarySpar;
	List<Amount<Length>> _distanceBetweenSpars;
	List<Amount<Length>> _prismoidsLength;
	List<Amount<Area>> _prismoidsSectionsAreas;
	
	private CenterOfGravity _cg;
	private Amount<Length> _xCG;
	private Amount<Length> _yCG;
	private Amount<Length> _zCG;
	private ArrayList<MethodEnum> _methodsList;
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap;
	private Map <MethodEnum, Amount<Length>> _xCGMap;
	private Map <MethodEnum, Amount<Length>> _yCGMap;
	private Double[] _percentDifferenceXCG;
	private Double[] _percentDifferenceYCG;
	
	// Jet A1 fuel density : the user can set this parameter when necessary
	private Amount<VolumetricDensity> _fuelDensity = Amount.valueOf(0.804, MyUnits.KILOGRAM_LITER);
	private Amount<Volume> _fuelVolume;
	private Amount<Mass> _fuelMass;
	private Amount<Force> _fuelWeight;

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class FuelTankBuilder {
	
		// required parameters
		private String __id;

		// optional parameters ... defaults
		// ...
		private Double __mainSparNormalizedStation;
		private Double __secondarySparNormalizedStation;
		
		private ArrayList<MethodEnum> __methodsList = new ArrayList<MethodEnum>();
		private Map <AnalysisTypeEnum, List<MethodEnum>> __methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
		private Map <MethodEnum, Amount<Length>> __xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
		private Map <MethodEnum, Amount<Length>> __yCGMap = new TreeMap<MethodEnum, Amount<Length>>();
		List<Amount<Length>> __thicknessAtMainSpar = new ArrayList<Amount<Length>>();
		List<Amount<Length>> __thicknessAtSecondarySpar = new ArrayList<Amount<Length>>();
		List<Amount<Length>> __distanceBetweenSpars = new ArrayList<Amount<Length>>();
		List<Amount<Length>> __prismoidsLength = new ArrayList<Amount<Length>>();
		List<Amount<Area>> __prismoidsSectionsAreas = new ArrayList<Amount<Area>>();
		
		public FuelTankBuilder (String id) {
			this.__id = id;
			this.initializeDefaultVariables(AircraftEnum.ATR72);
		}
		
		public FuelTankBuilder (String id, AircraftEnum aircraftName) {
			this.__id = id;
			this.initializeDefaultVariables(aircraftName);
		}
		
		public FuelTankBuilder mainSparPosition (Double xMainSpar) {
			this.__mainSparNormalizedStation = xMainSpar;
			return this;
		}
		
		public FuelTankBuilder secondarySparPosition (Double xSecondarySpar) {
			this.__secondarySparNormalizedStation = xSecondarySpar;
			return this;
		}
		
		public FuelTank build() {
			return new FuelTank (this);
		}
		
		/************************************************************************
		 * method that recognize aircraft name and sets its 
		 * fuel tank data.
		 * 
		 * @author Vittorio Trifari
		 */
		private void initializeDefaultVariables (AircraftEnum aircraftName) {

			switch(aircraftName) {

			// TODO : CHECK THESE VALUES
			
			case ATR72:
				__mainSparNormalizedStation = 0.25;
				__secondarySparNormalizedStation = 0.55;
				break;

			case B747_100B:
				__mainSparNormalizedStation = 0.25;
				__secondarySparNormalizedStation = 0.55;
				break;

			case AGILE_DC1:
				__mainSparNormalizedStation = 0.25;
				__secondarySparNormalizedStation = 0.55;
				break;
			}
		}
	}
	
	private FuelTank (FuelTankBuilder builder) {
	
		this._id = builder.__id;
		this._mainSparNormalizedStation = builder.__mainSparNormalizedStation;
		this._secondarySparNormalizedStation = builder.__secondarySparNormalizedStation;
		this._methodsList = builder.__methodsList;
		this._methodsMap = builder.__methodsMap;
		this._xCGMap = builder.__xCGMap;
		this._yCGMap = builder.__yCGMap;
		this._thicknessAtMainSpar = builder.__thicknessAtMainSpar;
		this._thicknessAtSecondarySpar = builder.__thicknessAtSecondarySpar;
		this._distanceBetweenSpars = builder.__distanceBetweenSpars;
		this._prismoidsLength = builder.__prismoidsLength;
		this._prismoidsSectionsAreas = builder.__prismoidsSectionsAreas;
		
	}
	//===================================================================================================
	// End of builder pattern
	//===================================================================================================
	
	@Override
	public void calculateGeometry(LiftingSurface theWing) {
		
		estimateDimensions(theWing);
		calculateArea();
		calculateVolume();
	}
	
	
	private void calculateVolume() {

		// TODO
		
	}

	/** 
	 * Calculate areas from base size
	 * 
	 * @param theAircraft
	 */
	private void calculateArea() {

		// TODO
		
	}

	/** 
	 * Estimate dimensions of the fuel tank.
	 */
	private void estimateDimensions(LiftingSurface theWing) {

		// TODO
		
	}
	
	@Override
	public void calculateFuelMass() {
		_fuelMass = Amount.valueOf(_fuelDensity.times(_fuelVolume).getEstimatedValue(), SI.KILOGRAM);
		_fuelWeight = _fuelMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
	}

	@Override
	public void calculateCG() {

		// TODO

	}

	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tFuel tank\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + _id + "'\n")
				.append("\t·····································\n")
				;
		
		// TODO : APPEND REMAINING DATA
		
		return sb.toString();
		
	}
	
	@Override
	public FuelFractionDatabaseReader getFuelFractionDatabase() {
		return fuelFractionDatabase;
	}
	
	@Override
	public void setFuelFractionDatabase(FuelFractionDatabaseReader fuelFractionDatabase) {
		this.fuelFractionDatabase = fuelFractionDatabase;
	}

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public void setId(String _id) {
		this._id = _id;
	}

	@Override
	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	@Override
	public void setXApexConstructionAxes(Amount<Length> _xApexConstructionAxes) {
		this._xApexConstructionAxes = _xApexConstructionAxes;
	}

	@Override
	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	@Override
	public void setYApexConstructionAxes(Amount<Length> _yApexConstructionAxes) {
		this._yApexConstructionAxes = _yApexConstructionAxes;
	}

	@Override
	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	@Override
	public void setZApexConstructionAxes(Amount<Length> _zApexConstructionAxes) {
		this._zApexConstructionAxes = _zApexConstructionAxes;
	}

	@Override
	public Double getMainSparNormalizedStation() {
		return _mainSparNormalizedStation;
	}

	@Override
	public void setMainSparNormalizedStation(Double _mainSparNormalizedStation) {
		this._mainSparNormalizedStation = _mainSparNormalizedStation;
	}

	@Override
	public Double getSecondarySparNormalizedStation() {
		return _secondarySparNormalizedStation;
	}

	@Override
	public void setSecondarySparNormalizedStation(Double _secondarySparNormalizedStation) {
		this._secondarySparNormalizedStation = _secondarySparNormalizedStation;
	}

	@Override
	public List<Amount<Length>> getDistanceBetweenSpars() {
		return _distanceBetweenSpars;
	}

	@Override
	public void setDistanceBetweenSpars(List<Amount<Length>> _distanceBetweenSpars) {
		this._distanceBetweenSpars = _distanceBetweenSpars;
	}

	@Override
	public List<Amount<Length>> getPrismoidsLength() {
		return _prismoidsLength;
	}

	@Override
	public void setPrismoidsLength(List<Amount<Length>> _prismoidsLength) {
		this._prismoidsLength = _prismoidsLength;
	}

	@Override
	public Amount<Length> getXCG() {
		return _xCG;
	}

	@Override
	public void setXCG(Amount<Length> _xCG) {
		this._xCG = _xCG;
	}

	@Override
	public Amount<Length> getYCG() {
		return _yCG;
	}

	@Override
	public void setYCG(Amount<Length> _yCG) {
		this._yCG = _yCG;
	}

	@Override
	public Amount<Length> getZCG() {
		return _zCG;
	}

	@Override
	public void setZCG(Amount<Length> _zCG) {
		this._zCG = _zCG;
	}

	@Override
	public Amount<VolumetricDensity> getFuelDensity() {
		return _fuelDensity;
	}

	@Override
	public void setFuelDensity(Amount<VolumetricDensity> _fuelDensity) {
		this._fuelDensity = _fuelDensity;
	}

	@Override
	public Amount<Mass> getMassEstimated() {
		return _massEstimated;
	}

	@Override
	public Amount<Volume> getVolumeEstimated() {
		return _volumeEstimated;
	}

	@Override
	public List<Amount<Length>> getThicknessAtMainSpar() {
		return _thicknessAtMainSpar;
	}

	@Override
	public List<Amount<Length>> getThicknessAtSecondarySpar() {
		return _thicknessAtSecondarySpar;
	}

	@Override
	public List<Amount<Area>> getPrismoidsSectionsAreas() {
		return _prismoidsSectionsAreas;
	}

	@Override
	public CenterOfGravity getCG() {
		return _cg;
	}

	@Override
	public Amount<Volume> getFuelVolume() {
		return _fuelVolume;
	}

	@Override
	public Amount<Mass> getFuelMass() {
		return _fuelMass;
	}

	@Override
	public Amount<Force> getFuelWeight() {
		return _fuelWeight;
	}
}
