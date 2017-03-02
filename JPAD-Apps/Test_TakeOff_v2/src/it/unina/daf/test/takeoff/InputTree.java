package it.unina.daf.test.takeoff;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;

import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyUnits;


public class InputTree {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	
	// field conditions:
	private Amount<Velocity> vWind;
	private Amount<Angle> alphaGround;
	private Amount<Length> altitude;
	private MyInterpolatingFunction muFunction;
	private List<Double> muList;
	private List<Amount<Velocity>> muListSpeed;	
	private MyInterpolatingFunction muBrakeFunction;
	private List<Double> muBrakeList;
	private List<Amount<Velocity>> muBrakeListSpeed;
	
	// take-off maneuver data:
	private Amount<Duration> dtRotation;
	private Amount<Duration> dtHold;
	private Amount<Length> obstacleTakeOff;
	private Double kRotation;
	private Amount<?> alphaDotRotation;
	private Double kCLmax;
	private Double dragDueToEnigneFailure;
	private Amount<?> kAlphaDot;
	
	// aircraft, wing and engine data:
	private Amount<Mass> takeOffMass;
	private Amount<Area> wingSurface;
	private double aspectRatio;
	private Amount<Angle> iw;
	private Amount<Length> wingToGroundDistance;
	private double cD0Clean, deltaCD0Flap, deltaCD0LandingGear;
	private double oswald;
	private double cLmaxTO, cL0TO;
	private Amount<?> cLalphaFlap;
	private Amount<Force> t0;
	private int nEngine;
	private MyInterpolatingFunction netThrust;
	private List<Amount<Force>> netThrustList;
	private List<Double> netThrustMachList;
	private MyInterpolatingFunction throttleGroundIdle;
	private List<Double> throttleGroundIdleList;
	private List<Amount<Velocity>> throttleGroundIdleListSpeed;
	
	
	// boolean flag concerning charts and engine model:
	private boolean charts, engineModel, balancedFieldLength;
	
	//------------------------------------------------------------------------------------------
	// BUILDER:
	@SuppressWarnings("unchecked")
	public InputTree() {
		
		vWind = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		altitude = Amount.valueOf(0.0, SI.METER);
		muFunction = new MyInterpolatingFunction();
		muBrakeFunction = new MyInterpolatingFunction();
		throttleGroundIdle = new MyInterpolatingFunction();
		
		dtRotation = Amount.valueOf(3, SI.SECOND);
		dtHold = Amount.valueOf(0.5, SI.SECOND);
		obstacleTakeOff = Amount.valueOf(35, NonSI.FOOT).to(SI.METER);
		kRotation = 1.05;
		alphaDotRotation = Amount.valueOf(35, MyUnits.DEG_PER_SECOND);
		kCLmax = 0.9;
		dragDueToEnigneFailure = 0.0;
		kAlphaDot = Amount.valueOf(0.04, NonSI.DEGREE_ANGLE.inverse());		
		
		takeOffMass = Amount.valueOf(0.0, SI.KILOGRAM);
		wingSurface = Amount.valueOf(0.0, SI.SQUARE_METRE);
		aspectRatio = 0.0;
		iw = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		wingToGroundDistance = Amount.valueOf(0.0, SI.METER);
		cD0Clean = 0.0;
		deltaCD0Flap = 0.0;
		deltaCD0LandingGear = 0.0;
		oswald = 0.0;
		cLmaxTO = 0.0;
		cL0TO = 0.0;
		cLalphaFlap = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE.inverse());
		t0 = Amount.valueOf(0.0, SI.NEWTON);
		nEngine = 0;
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	
	public Amount<Velocity> getvWind() {
		return vWind;
	}

	public void setvWind(Amount<Velocity> vWind) {
		this.vWind = vWind;
	}

	public Amount<Angle> getAlphaGround() {
		return alphaGround;
	}

	public void setAlphaGround(Amount<Angle> alphaGround) {
		this.alphaGround = alphaGround;
	}

	public Amount<Length> getAltitude() {
		return altitude;
	}

	public void setAltitude(Amount<Length> altitude) {
		this.altitude = altitude;
	}

	public Amount<Mass> getTakeOffMass() {
		return takeOffMass;
	}

	public void setTakeOffMass(Amount<Mass> takeOffMass) {
		this.takeOffMass = takeOffMass;
	}

	public Amount<Area> getWingSurface() {
		return wingSurface;
	}

	public void setWingSurface(Amount<Area> wingSurface) {
		this.wingSurface = wingSurface;
	}

	public double getAspectRatio() {
		return aspectRatio;
	}

	public void setAspectRatio(double aspectRatio) {
		this.aspectRatio = aspectRatio;
	}

	public Amount<Angle> getIw() {
		return iw;
	}

	public void setIw(Amount<Angle> iw) {
		this.iw = iw;
	}

	public Amount<Length> getWingToGroundDistance() {
		return wingToGroundDistance;
	}

	public void setWingToGroundDistance(Amount<Length> wingToGroundDistance) {
		this.wingToGroundDistance = wingToGroundDistance;
	}

	public double getcD0Clean() {
		return cD0Clean;
	}

	public void setcD0Clean(double cD0Clean) {
		this.cD0Clean = cD0Clean;
	}

	public double getDeltaCD0Flap() {
		return deltaCD0Flap;
	}

	public void setDeltaCD0Flap(double deltaCD0Flap) {
		this.deltaCD0Flap = deltaCD0Flap;
	}

	public double getDeltaCD0LandingGear() {
		return deltaCD0LandingGear;
	}

	public void setDeltaCD0LandingGear(double deltaCD0LandingGear) {
		this.deltaCD0LandingGear = deltaCD0LandingGear;
	}

	public double getOswald() {
		return oswald;
	}

	public void setOswald(double oswald) {
		this.oswald = oswald;
	}

	public double getcLmaxTO() {
		return cLmaxTO;
	}

	public void setcLmaxTO(double cLmaxTO) {
		this.cLmaxTO = cLmaxTO;
	}

	public Amount<?> getcLalphaFlap() {
		return cLalphaFlap;
	}

	public double getcL0TO() {
		return cL0TO;
	}

	public void setcL0TO(double cL0TO) {
		this.cL0TO = cL0TO;
	}

	public void setcLalphaFlap(Amount<?> cLalphaFlap) {
		this.cLalphaFlap = cLalphaFlap;
	}

	public Amount<Force> getT0() {
		return t0;
	}

	public void setT0(Amount<Force> t0) {
		this.t0 = t0;
	}

	public int getnEngine() {
		return nEngine;
	}

	public void setnEngine(int nEngine) {
		this.nEngine = nEngine;
	}

	public boolean isCharts() {
		return charts;
	}

	public void setCharts(boolean charts) {
		this.charts = charts;
	}

	public boolean isEngineModel() {
		return engineModel;
	}

	public void setEngineModel(boolean engineModel) {
		this.engineModel = engineModel;
	}

	public boolean isBalancedFieldLength() {
		return balancedFieldLength;
	}

	public void setBalancedFieldLength(boolean balancedFieldLength) {
		this.balancedFieldLength = balancedFieldLength;
	}

	public MyInterpolatingFunction getMuFunction() {
		return muFunction;
	}

	public void setMuFunction(MyInterpolatingFunction muFunction) {
		this.muFunction = muFunction;
	}

	public MyInterpolatingFunction getMuBrakeFunction() {
		return muBrakeFunction;
	}

	public void setMuBrakeFunction(MyInterpolatingFunction muBrakeFunction) {
		this.muBrakeFunction = muBrakeFunction;
	}

	public Amount<Duration> getDtRotation() {
		return dtRotation;
	}

	public void setDtRotation(Amount<Duration> dtRotation) {
		this.dtRotation = dtRotation;
	}

	public Amount<Duration> getDtHold() {
		return dtHold;
	}

	public void setDtHold(Amount<Duration> dtHold) {
		this.dtHold = dtHold;
	}

	public Amount<Length> getObstacleTakeOff() {
		return obstacleTakeOff;
	}

	public void setObstacleTakeOff(Amount<Length> obstacleTakeOff) {
		this.obstacleTakeOff = obstacleTakeOff;
	}

	public Double getkRotation() {
		return kRotation;
	}

	public void setkRotation(Double kRotation) {
		this.kRotation = kRotation;
	}

	public Amount<?> getAlphaDotRotation() {
		return alphaDotRotation;
	}

	public void setAlphaDotRotation(Amount<?> alphaDotRotation) {
		this.alphaDotRotation = alphaDotRotation;
	}

	public Double getkCLmax() {
		return kCLmax;
	}

	public void setkCLmax(Double kCLmax) {
		this.kCLmax = kCLmax;
	}

	public Double getDragDueToEnigneFailure() {
		return dragDueToEnigneFailure;
	}

	public void setDragDueToEnigneFailure(Double dragDueToEnigneFailure) {
		this.dragDueToEnigneFailure = dragDueToEnigneFailure;
	}

	public Amount<?> getkAlphaDot() {
		return kAlphaDot;
	}

	public void setkAlphaDot(Amount<?> kAlphaDot) {
		this.kAlphaDot = kAlphaDot;
	}

	public MyInterpolatingFunction getThrottleGroundIdle() {
		return throttleGroundIdle;
	}

	public void setThrottleGroundIdle(MyInterpolatingFunction throttleGroundIdle) {
		this.throttleGroundIdle = throttleGroundIdle;
	}

	public MyInterpolatingFunction getNetThrust() {
		return netThrust;
	}

	public void setNetThrust(MyInterpolatingFunction netThrust) {
		this.netThrust = netThrust;
	}

	public List<Amount<Force>> getNetThrustList() {
		return netThrustList;
	}

	public void setNetThrustList(List<Amount<Force>> netThrustList) {
		this.netThrustList = netThrustList;
	}

	public List<Double> getNetThrustMachList() {
		return netThrustMachList;
	}

	public void setNetThrustMachList(List<Double> netThrustMachList) {
		this.netThrustMachList = netThrustMachList;
	}

	public List<Double> getMuList() {
		return muList;
	}

	public void setMuList(List<Double> muList) {
		this.muList = muList;
	}

	public List<Amount<Velocity>> getMuListSpeed() {
		return muListSpeed;
	}

	public void setMuListSpeed(List<Amount<Velocity>> muListSpeed) {
		this.muListSpeed = muListSpeed;
	}

	public List<Double> getMuBrakeList() {
		return muBrakeList;
	}

	public void setMuBrakeList(List<Double> muBrakeList) {
		this.muBrakeList = muBrakeList;
	}

	public List<Amount<Velocity>> getMuBrakeListSpeed() {
		return muBrakeListSpeed;
	}

	public void setMuBrakeListSpeed(List<Amount<Velocity>> muBrakeListSpeed) {
		this.muBrakeListSpeed = muBrakeListSpeed;
	}

	public List<Double> getThrottleGroundIdleList() {
		return throttleGroundIdleList;
	}

	public void setThrottleGroundIdleList(List<Double> throttleGroundIdleList) {
		this.throttleGroundIdleList = throttleGroundIdleList;
	}

	public List<Amount<Velocity>> getThrottleGroundIdleListSpeed() {
		return throttleGroundIdleListSpeed;
	}

	public void setThrottleGroundIdleListSpeed(List<Amount<Velocity>> throttleGroundIdleListSpeed) {
		this.throttleGroundIdleListSpeed = throttleGroundIdleListSpeed;
	}
}
