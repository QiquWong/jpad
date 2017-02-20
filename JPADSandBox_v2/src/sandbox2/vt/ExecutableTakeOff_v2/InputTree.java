package sandbox2.vt.ExecutableTakeOff_v2;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;

public class InputTree {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	
	// ground conditions:
	private Amount<Velocity> vWind;
	private Amount<Angle> alphaGround;
	private Amount<Length> altitude;
	
	// aircraft, wing and engine data:
	private Amount<Mass> takeOffMass;
	private Amount<Area> wingSurface;
	private Amount<Length> wingSpan;
	private double aspectRatio;
	private Amount<Angle> iw;
	private Amount<Length> wingToGroundDistance;
	private double cD0Clean, deltaCD0Flap, deltaCD0LandingGear;
	private double oswald;
	private double cLmaxTO, cL0TO;
	private Amount<?> cLalphaFlap;
	private Amount<Force> t0;
	private int nEngine;
	private double[] netThrust;
	private double[] machArray;
	
	// boolean flag concerning charts and engine model:
	private boolean charts, engineModel, balancedFieldLength;
	
	//------------------------------------------------------------------------------------------
	// BUILDER:
	public InputTree() {
		
		vWind = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		altitude = Amount.valueOf(0.0, SI.METER);
		
		takeOffMass = Amount.valueOf(0.0, SI.KILOGRAM);
		wingSurface = Amount.valueOf(0.0, SI.SQUARE_METRE);
		wingSpan = Amount.valueOf(0.0, SI.METER);
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

	public Amount<Length> getWingSpan() {
		return wingSpan;
	}

	public void setWingSpan(Amount<Length> wingSpan) {
		this.wingSpan = wingSpan;
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

	public double[] getNetThrust() {
		return netThrust;
	}

	public double[] getMachArray() {
		return machArray;
	}

	public void setNetThrust(double[] netThrust) {
		this.netThrust = netThrust;
	}

	public void setMachArray(double[] machArray) {
		this.machArray = machArray;
	}

	public boolean isBalancedFieldLength() {
		return balancedFieldLength;
	}

	public void setBalancedFieldLength(boolean balancedFieldLength) {
		this.balancedFieldLength = balancedFieldLength;
	}
}
