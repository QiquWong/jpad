package sandbox2.vt.executableHighLift_v2;

import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.FlapTypeEnum;

public class InputTree {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	
	private Amount<Angle> alphaCurrent,
						  sweepQuarteChordEq,
						  alphaMaxClean,
						  alphaStarClean;
	private Amount<?>cLAlphaClean,
					 clAlphaMeanAirfoil;
	private Amount<Length> span,
						   meanAirfoilChord,
						   LERadiusMeanAirfoil,
						   rootChordEquivalentWing;
	private Amount<Area> surface;
	private double aspectRatio,
	               taperRatioEq,
	               cL0Clean,
	               cl0MeanAirfoil,
	               cLmaxClean,
	               cLstarClean,
	               maxthicknessMeanAirfoil;
	private int flapsNumber, slatsNumber;
	private List<FlapTypeEnum> flapType;
	private AirfoilFamilyEnum meanAirfoilFamily;
	private List<Double> cfc,
	                     csc,
	                     cExtCSlat,
	                     etaInFlap,
	                     etaOutFlap,
	                     etaInSlat,
	                     etaOutSlat;
	private List<Amount<Angle>> deltaFlap,
    							deltaSlat;
	
	//------------------------------------------------------------------------------------------
	// BUILDER:
	
	public InputTree() {

		alphaCurrent = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		sweepQuarteChordEq = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		alphaMaxClean = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		alphaStarClean = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		
		cLAlphaClean = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE.inverse());
		clAlphaMeanAirfoil = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE.inverse());
		
		span = Amount.valueOf(0.0, SI.METER);
		meanAirfoilChord = Amount.valueOf(0.0, SI.METER);
		LERadiusMeanAirfoil = Amount.valueOf(0.0, SI.METER);
		rootChordEquivalentWing = Amount.valueOf(0.0, SI.METER);
		
		surface = Amount.valueOf(0.0, SI.SQUARE_METRE);
		
		aspectRatio = 0.0;
		taperRatioEq = 0.0;
		cL0Clean = 0.0;
		cl0MeanAirfoil = 0.0;
		cLmaxClean = 0.0;
		cLstarClean = 0.0;
		maxthicknessMeanAirfoil = 0.0;
		
		flapsNumber = 0;
		slatsNumber = 0;
		
		flapType = new ArrayList<FlapTypeEnum>();
				
		cfc = new ArrayList<Double>();
		csc = new ArrayList<Double>();
		cExtCSlat = new ArrayList<Double>();
		deltaFlap = new ArrayList<Amount<Angle>>();
		deltaSlat = new ArrayList<Amount<Angle>>();
		etaInFlap = new ArrayList<Double>();
		etaOutFlap = new ArrayList<Double>();
		etaInSlat = new ArrayList<Double>();
		etaOutSlat = new ArrayList<Double>();
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
		
	public Amount<Angle> getAlphaCurrent() {
		return alphaCurrent;
	}

	public void setAlphaCurrent(Amount<Angle> alphaCurrent) {
		this.alphaCurrent = alphaCurrent;
	}

	public Amount<Angle> getSweepQuarteChordEq() {
		return sweepQuarteChordEq;
	}

	public void setSweepQuarteChordEq(Amount<Angle> sweepQuarteChordEq) {
		this.sweepQuarteChordEq = sweepQuarteChordEq;
	}

	public Amount<Angle> getAlphaMaxClean() {
		return alphaMaxClean;
	}

	public void setAlphaMaxClean(Amount<Angle> alphaMaxClean) {
		this.alphaMaxClean = alphaMaxClean;
	}

	public Amount<Angle> getAlphaStarClean() {
		return alphaStarClean;
	}

	public void setAlphaStarClean(Amount<Angle> alphaStarClean) {
		this.alphaStarClean = alphaStarClean;
	}

	public Amount<Length> getSpan() {
		return span;
	}

	public void setSpan(Amount<Length> span) {
		this.span = span;
	}

	public Amount<Area> getSurface() {
		return surface;
	}

	public void setSurface(Amount<Area> surface) {
		this.surface = surface;
	}

	public double getAspectRatio() {
		return aspectRatio;
	}

	public void setAspectRatio(double aspectRatio) {
		this.aspectRatio = aspectRatio;
	}

	public double getTaperRatioEq() {
		return taperRatioEq;
	}

	public void setTaperRatioEq(double taperRatioEq) {
		this.taperRatioEq = taperRatioEq;
	}

	public double getcL0Clean() {
		return cL0Clean;
	}

	public void setcL0Clean(double cL0Clean) {
		this.cL0Clean = cL0Clean;
	}

	public double getcLmaxClean() {
		return cLmaxClean;
	}

	public void setcLmaxClean(double cLmaxClean) {
		this.cLmaxClean = cLmaxClean;
	}

	public double getcLstarClean() {
		return cLstarClean;
	}

	public void setcLstarClean(double cLstarClean) {
		this.cLstarClean = cLstarClean;
	}

	public Amount<?> getcLAlphaClean() {
		return cLAlphaClean;
	}

	public void setcLAlphaClean(Amount<?> cLAlphaClean) {
		this.cLAlphaClean = cLAlphaClean;
	}

	public Amount<?> getClAlphaMeanAirfoil() {
		return clAlphaMeanAirfoil;
	}

	public void setClAlphaMeanAirfoil(Amount<?> clAlphaMeanAirfoil) {
		this.clAlphaMeanAirfoil = clAlphaMeanAirfoil;
	}

	public Amount<Length> getLERadiusMeanAirfoil() {
		return LERadiusMeanAirfoil;
	}

	public void setLERadiusMeanAirfoil(Amount<Length> lERadiusMeanAirfoil) {
		LERadiusMeanAirfoil = lERadiusMeanAirfoil;
	}

	public double getMaxthicknessMeanAirfoil() {
		return maxthicknessMeanAirfoil;
	}

	public void setMaxthicknessMeanAirfoil(double maxthicknessMeanAirfoil) {
		this.maxthicknessMeanAirfoil = maxthicknessMeanAirfoil;
	}

	public List<FlapTypeEnum> getFlapType() {
		return flapType;
	}

	public void setFlapType(List<FlapTypeEnum> flapType) {
		this.flapType = flapType;
	}

	public List<Double> getCfc() {
		return cfc;
	}

	public void setCfc(List<Double> cfc) {
		this.cfc = cfc;
	}

	public List<Double> getCsc() {
		return csc;
	}

	public void setCsc(List<Double> csc) {
		this.csc = csc;
	}

	public List<Double> getcExtCSlat() {
		return cExtCSlat;
	}

	public void setcExtCSlat(List<Double> cExtCSlat) {
		this.cExtCSlat = cExtCSlat;
	}

	public List<Amount<Angle>> getDeltaFlap() {
		return deltaFlap;
	}

	public void setDeltaFlap(List<Amount<Angle>> deltaFlap) {
		this.deltaFlap = deltaFlap;
	}

	public List<Amount<Angle>> getDeltaSlat() {
		return deltaSlat;
	}

	public void setDeltaSlat(List<Amount<Angle>> deltaSlat) {
		this.deltaSlat = deltaSlat;
	}

	public List<Double> getEtaInFlap() {
		return etaInFlap;
	}

	public void setEtaInFlap(List<Double> etaInFlap) {
		this.etaInFlap = etaInFlap;
	}

	public List<Double> getEtaOutFlap() {
		return etaOutFlap;
	}

	public void setEtaOutFlap(List<Double> etaOutFlap) {
		this.etaOutFlap = etaOutFlap;
	}

	public List<Double> getEtaInSlat() {
		return etaInSlat;
	}

	public void setEtaInSlat(List<Double> etaInSlat) {
		this.etaInSlat = etaInSlat;
	}

	public List<Double> getEtaOutSlat() {
		return etaOutSlat;
	}

	public void setEtaOutSlat(List<Double> etaOutSlat) {
		this.etaOutSlat = etaOutSlat;
	}

	public AirfoilFamilyEnum getMeanAirfoilFamily() {
		return meanAirfoilFamily;
	}

	public void setMeanAirfoilFamily(AirfoilFamilyEnum meanAirfoilFamily) {
		this.meanAirfoilFamily = meanAirfoilFamily;
	}

	public int getFlapsNumber() {
		return flapsNumber;
	}

	public void setFlapsNumber(int flapsNumber) {
		this.flapsNumber = flapsNumber;
	}

	public int getSlatsNumber() {
		return slatsNumber;
	}

	public void setSlatsNumber(int slatsNumber) {
		this.slatsNumber = slatsNumber;
	}

	public double getCl0MeanAirfoil() {
		return cl0MeanAirfoil;
	}

	public void setCl0MeanAirfoil(double cl0MeanAirfoil) {
		this.cl0MeanAirfoil = cl0MeanAirfoil;
	}

	public Amount<Length> getMeanAirfoilChord() {
		return meanAirfoilChord;
	}

	public void setMeanAirfoilChord(Amount<Length> meanAirfoilChord) {
		this.meanAirfoilChord = meanAirfoilChord;
	}

	public Amount<Length> getRootChordEquivalentWing() {
		return rootChordEquivalentWing;
	}

	public void setRootChordEquivalentWing(Amount<Length> rootChordEquivalentWing) {
		this.rootChordEquivalentWing = rootChordEquivalentWing;
	} 
}