package aircraft.components.nacelles;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.components.Aircraft;
import aircraft.components.powerPlant.Engine;
import configuration.enumerations.EngineTypeEnum;

/** 
 * The Nacelle is considered 
 * the structural part of the engine
 * 
 * @author Vittorio Trifari
 *
 */
public class NacelleCreator implements INacelleCreator {

	public enum MountingPosition {
		WING,
		FUSELAGE,
		UNDERCARRIAGE_HOUSING
	}

	private String _id;
	private MountingPosition _mountingPosition;
	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	
	private Amount<Length> _roughness;
	
	private Amount<Length> _length;
	//-----------------------------------------
	// only for jet engines
	private Amount<Length> _diameterMean;
	private Amount<Length> _diameterOutlet;
	//-----------------------------------------
	// only for propeller engines
	private Amount<Length> _width;
	private Amount<Length> _height;
	//------------------------------------------
	
	private Amount<Area> _surfaceWetted;
	private Amount<Mass> _massReference;
	
	private Aircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;
	private Engine _theEngine;
	
	private NacellesWeightsManager _theWeights;
	private NacellesBalanceManager _theBalance;
	private NacellesAerodynamicsManager _theAerodynamics;
	
	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class NacelleCreatorBuilder {
	
		// required parameters
		private String __id;
		
		// optional parameters ... defaults
		// ...
		private Amount<Length> __length = Amount.valueOf(0.0, SI.METER);
		//-----------------------------------------
		// only for jet engines
		private Amount<Length> __diameterMean = Amount.valueOf(0.0, SI.METER);
		private Amount<Length> __diameterOutlet = Amount.valueOf(0.0, SI.METER);
		//-----------------------------------------
		// only for propeller engines
		private Amount<Length> __width = Amount.valueOf(0.0, SI.METER);
		private Amount<Length> __height = Amount.valueOf(0.0, SI.METER);
		//------------------------------------------
		// TODO : CONTINUE !!!
		
	}
	
	private NacelleCreator (NacelleCreatorBuilder builder) {
		
		// TODO : PASS ALL REQUIRED VARIABLES FROM THE BUILDER TO THE CLASS FIELDS
		
		calculateSurfaceWetted();
		
		if((_theEngine.getEngineType() == EngineTypeEnum.TURBOFAN)
				|| (_theEngine.getEngineType() == EngineTypeEnum.TURBOJET)) {
			if((_length.doubleValue(SI.METER) == 0.0)
					&& (_diameterMean.doubleValue(SI.METER) == 0.0)
					&& (_diameterOutlet.doubleValue(SI.METER) == 0.0)) {
				estimateDimensions(_theEngine);
			}
		}
		else if((_theEngine.getEngineType() == EngineTypeEnum.TURBOPROP)
				|| (_theEngine.getEngineType() == EngineTypeEnum.PISTON)) {
			if((_length.doubleValue(SI.METER) == 0.0)
					&& (_height.doubleValue(SI.METER) == 0.0)
					&& (_width.doubleValue(SI.METER) == 0.0)) {
				estimateDimensions(_theEngine);
			}
		}
	}
	
	//============================================================================================
	// End of builder pattern 
	//============================================================================================
	
	/***************************************************************************								
	 * This method estimates the nacelle dimensions in inches as function of 									
	 * the engine type. If is a jet engine it uses the static thrust in lbs; 									
	 * otherwise it uses the shaft-horsepower (hp).
	 * 
	 * @see: Behind ADAS - Nacelle Sizing
	 */
	@Override
	public void estimateDimensions (Engine theEngine) {
				
		if((theEngine.getEngineType() == EngineTypeEnum.TURBOFAN) || (theEngine.getEngineType() == EngineTypeEnum.TURBOJET)) {
			_length = Amount.valueOf(
					40 + (0.59 * Math.sqrt(theEngine.getT0().doubleValue(NonSI.POUND_FORCE))),
					NonSI.INCH)
					.to(SI.METER);
			_diameterMean = Amount.valueOf(
					5 + (0.39 * Math.sqrt(theEngine.getT0().doubleValue(NonSI.POUND_FORCE))),
					NonSI.INCH)
					.to(SI.METER); 
		}
		
		else if(theEngine.getEngineType() == EngineTypeEnum.PISTON) {
			_length = Amount.valueOf(
					4*(10^-10)*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),4)
					- 6*(10^-7)*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),3)
					+ 8*(10^-5)*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),2)
					- 0.2193*theEngine.getP0().doubleValue(NonSI.HORSEPOWER)
					+ 54.097,
					NonSI.INCH)
					.to(SI.METER);
			
			if(theEngine.getP0().doubleValue(NonSI.HORSEPOWER) <= 410)
				_width = Amount.valueOf(
						- 3*(10^-7)*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),3)
						- 0.0003*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),2)
						+ 0.2196*theEngine.getP0().doubleValue(NonSI.HORSEPOWER)
						+ 7.3966,
						NonSI.INCH)
						.to(SI.METER); 
			else 
				_width = Amount.valueOf(
						- 4.6563*Math.log(theEngine.getP0().doubleValue(NonSI.HORSEPOWER))
						+ 57.943,
						NonSI.INCH)
						.to(SI.METER);
			
			_height = Amount.valueOf(
					12.595*Math.log(theEngine.getP0().doubleValue(NonSI.HORSEPOWER))
					- 43.932,
					NonSI.INCH)
					.to(SI.METER);
			}
		
		else if(theEngine.getEngineType() == EngineTypeEnum.TURBOPROP) {
			_length = Amount.valueOf(
					- 1.28*(10^-5)*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),2)
					+ 9.273*(10^-2)*theEngine.getP0().doubleValue(NonSI.HORSEPOWER)
					- 8.3456,
					NonSI.INCH)
					.to(SI.METER);
			
			_width = Amount.valueOf(
					- 0.95*(10^-6)*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),2)
					+ 0.0073*theEngine.getP0().doubleValue(NonSI.HORSEPOWER)
					+ 25.3,
					NonSI.INCH)
					.to(SI.METER);
			
			_height = Amount.valueOf(
					0.67*(10^-11)*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),3)
					- 3.35*(10^-6)*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),2)
					+ 0.029*theEngine.getP0().doubleValue(NonSI.HORSEPOWER)
					- 5.58425,
					NonSI.INCH)
					.to(SI.METER); 
		}
	}
	
	/**
	 * Wetted surface is considered as two times the external surface
	 * (the air flows both outside and inside)
	 * 
	 * BE CAREFUL! Only the external area of the nacelle is counted as wetted surface [Roskam] 
	 */
	private void calculateSurfaceWetted() {
		
		// TODO : IF TURBOPROP OR PISTON, THE Swet IS (2*HEIGHT*LENGTH)+(2*WIDTH*LENGTH) ??
		
		_surfaceWetted = _length.times(_diameterMean.times(Math.PI)).to(SI.SQUARE_METRE); 
	}

	private void initializeWeights() {
		if (_theWeights == null) 
			_theWeights = new NacellesWeightsManager(this);
	}

	private void initializeAerodynamics() {
		if (_theAerodynamics == null) 
			_theAerodynamics = new NacellesAerodynamicsManager(_theAircraft, this, _theOperatingConditions);
	}
	
	private void initializeBalance() {
		if (_theBalance == null)
			_theBalance = new NacellesBalanceManager(this);
	}

	/**
	 * Invoke all the methods to evaluate 
	 * nacelle related quantities
	 * 
	 * @author Lorenzo Attanasio
	 */
	@Override
	public void calculateAll() {
		initializeWeights();
		initializeBalance();
		initializeAerodynamics();
		
		_theWeights.calculateAll();
		_theBalance.calculateAll();
		_theAerodynamics.calculateAll();
	}

	@Override
	public Double calculateFormFactor(){
		//matlab file ATR72
		return (1 + 0.165 
				+ 0.91/(_length.getEstimatedValue()/_diameterMean.getEstimatedValue())); 	
	}
	
	@Override
	public Amount<Length> getLength() {
		return _length;
	}

	@Override
	public void setLength(Amount<Length> _lenght) {
		this._length = _lenght;
	}

	@Override
	public Amount<Area> getSurfaceWetted() {
		return _surfaceWetted;
	}

	@Override
	public void setSurfaceWetted(Amount<Area> _sWet) {
		this._surfaceWetted = _sWet;
	}

	@Override
	public Amount<Length> getDiameterMean() {
		return _diameterMean;
	}

	@Override
	public void setDiameterMean(Amount<Length> _diameter) {
		this._diameterMean = _diameter;
	}

	@Override
	public Amount<Length> getDiameterOutlet() {
		return _diameterOutlet;
	}

	@Override
	public void setDiameterOutlet(Amount<Length> _exitDiameter) {
		this._diameterOutlet = _exitDiameter;
	}

	@Override
	public Amount<Length> getWidth() {
		return _width;
	}

	@Override
	public void setWidth(Amount<Length> _width) {
		this._width = _width;
	}

	@Override
	public Amount<Length> getHeight() {
		return _height;
	}

	@Override
	public void setHeight(Amount<Length> _height) {
		this._height = _height;
	}

	@Override
	public Amount<Length> getRoughness() {
		return _roughness;
	}

	@Override
	public void setRoughness(Amount<Length> _roughness) {
		this._roughness = _roughness;
	}

	@Override
	public MountingPosition getMountingPosition() {
		return _mountingPosition;
	}

	@Override
	public void setMountingPosition(MountingPosition _mounting) {
		this._mountingPosition = _mounting;
	}

	@Override
	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	@Override
	public void setXApexConstructionAxes(Amount<Length> _X0) {
		this._xApexConstructionAxes = _X0;
	}

	@Override
	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	@Override
	public void setYApexConstructionAxes(Amount<Length> _Y0) {
		this._yApexConstructionAxes = _Y0;
	}

	@Override
	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	@Override
	public void setZApexConstructionAxes(Amount<Length> _Z0) {
		this._zApexConstructionAxes = _Z0;
	}

	@Override
	public Engine getTheEngine() {
		return _theEngine;
	}

	@Override
	public void setTheEngine(Engine _theEngine) {
		this._theEngine = _theEngine;
	}

	@Override
	public NacellesWeightsManager getWeights() {
		initializeWeights();
		return _theWeights;
	}

	@Override
	public NacellesAerodynamicsManager getAerodynamics() {
		initializeAerodynamics();
		return _theAerodynamics;
	}

	@Override
	public NacellesBalanceManager getBalance() {
		initializeBalance();
		return _theBalance;
	}
	
	@Override
	public String getId() {
		return _id;
	}

	@Override
	public void setId(String id) {
		this._id = id;
	}

	@Override
	public Amount<Mass> getMassReference() {
		return _massReference;
	}

	@Override
	public void setMassReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}
	
}
