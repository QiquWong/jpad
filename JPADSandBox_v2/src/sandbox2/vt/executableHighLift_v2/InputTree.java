package sandbox2.vt.executableHighLift_v2;

import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.FlapTypeEnum;

public class InputTree {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	private boolean plotCharts;
	
	private Amount<Angle> currentAngleOfAttack;
	
	private Double aspectRatio;
	private Amount<Area> surface;
	private Double taperRatioEq;
	private Amount<Angle> sweepQuarteChordEq;
	private Amount<Length> rootChordEquivalentWing;
	
	private Amount<Angle> alphaStallClean;
	private Amount<Angle> alphaStarClean;
	private Amount<?> cLAlphaClean;
	private Double cL0Clean;
	private Double cLstarClean;
	private Double cLmaxClean;
	
	private List<Double> etaStations;
	private List<Amount<Length>> airfoilsChordDistribution;
	private List<Double> maxThicknessAirfoilsDistribution;
	private List<Double> leadingEdgeRadiusAirfoilsDistribution;
	private List<Amount<?>> clAlphaAirfoilsDistribution;
	private List<Double> cl0AirfoilsDistribution;
	private AirfoilFamilyEnum airfoilsFamily;
	
	private Integer flapsNumber;
	private Integer slatsNumber;
	private List<FlapTypeEnum> flapType;
	private List<Double> cfc;
	private List<Double> csc;
	private List<Double> cExtCSlat;
	private List<Double> etaInFlap;
	private List<Double> etaOutFlap;
	private List<Double> etaInSlat;
	private List<Double> etaOutSlat;
	private List<Amount<Angle>> deltaFlap;
	private List<Amount<Angle>> deltaSlat;
	private List<SymmetricFlapCreator> symmetricFlapCreatorList;
	//------------------------------------------------------------------------------------------
	// BUILDER:
	
	public InputTree() {

		plotCharts = false;
		
		currentAngleOfAttack = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		
		aspectRatio = 0.0;
		surface = Amount.valueOf(0.0, SI.SQUARE_METRE);
		sweepQuarteChordEq = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		taperRatioEq = 0.0;
		rootChordEquivalentWing = Amount.valueOf(0.0, SI.METER);
		
		alphaStallClean = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		alphaStarClean = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		cLAlphaClean = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE.inverse());
		cL0Clean = 0.0;
		cLstarClean = 0.0;
		cLmaxClean = 0.0;
		
		airfoilsChordDistribution = new ArrayList<>();
		maxThicknessAirfoilsDistribution = new ArrayList<>();
		leadingEdgeRadiusAirfoilsDistribution = new ArrayList<>();
		clAlphaAirfoilsDistribution = new ArrayList<>();
		cl0AirfoilsDistribution = new ArrayList<>();
		airfoilsFamily = null;
		
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
	
	public Amount<Angle> getCurrentAngleOfAttack() {
		return currentAngleOfAttack;
	}

	public void setCurrentAngleOfAttack(Amount<Angle> currentAngleOfAttack) {
		this.currentAngleOfAttack = currentAngleOfAttack;
	}

	public Double getAspectRatio() {
		return aspectRatio;
	}

	public void setAspectRatio(Double aspectRatio) {
		this.aspectRatio = aspectRatio;
	}

	public Amount<Area> getSurface() {
		return surface;
	}

	public void setSurface(Amount<Area> surface) {
		this.surface = surface;
	}

	public Double getTaperRatioEq() {
		return taperRatioEq;
	}

	public void setTaperRatioEq(Double taperRatioEq) {
		this.taperRatioEq = taperRatioEq;
	}

	public Amount<Angle> getSweepQuarteChordEq() {
		return sweepQuarteChordEq;
	}

	public void setSweepQuarteChordEq(Amount<Angle> sweepQuarteChordEq) {
		this.sweepQuarteChordEq = sweepQuarteChordEq;
	}

	public Amount<Length> getRootChordEquivalentWing() {
		return rootChordEquivalentWing;
	}

	public void setRootChordEquivalentWing(Amount<Length> rootChordEquivalentWing) {
		this.rootChordEquivalentWing = rootChordEquivalentWing;
	}

	public Amount<Angle> getAlphaStallClean() {
		return alphaStallClean;
	}

	public void setAlphaStallClean(Amount<Angle> alphaStallClean) {
		this.alphaStallClean = alphaStallClean;
	}

	public Amount<Angle> getAlphaStarClean() {
		return alphaStarClean;
	}

	public void setAlphaStarClean(Amount<Angle> alphaStarClean) {
		this.alphaStarClean = alphaStarClean;
	}

	public Amount<?> getCLAlphaClean() {
		return cLAlphaClean;
	}

	public void setCLAlphaClean(Amount<?> cLAlphaClean) {
		this.cLAlphaClean = cLAlphaClean;
	}

	public Double getCL0Clean() {
		return cL0Clean;
	}

	public void setCL0Clean(Double cL0Clean) {
		this.cL0Clean = cL0Clean;
	}

	public Double getCLstarClean() {
		return cLstarClean;
	}

	public void setCLstarClean(Double cLstarClean) {
		this.cLstarClean = cLstarClean;
	}

	public Double getCLmaxClean() {
		return cLmaxClean;
	}

	public void setCLmaxClean(Double cLmaxClean) {
		this.cLmaxClean = cLmaxClean;
	}

	public List<Amount<Length>> getAirfoilsChordDistribution() {
		return airfoilsChordDistribution;
	}

	public void setAirfoilsChordDistribution(List<Amount<Length>> airfoilsChordDistribution) {
		this.airfoilsChordDistribution = airfoilsChordDistribution;
	}

	public List<Double> getMaxThicknessAirfoilsDistribution() {
		return maxThicknessAirfoilsDistribution;
	}

	public void setMaxThicknessAirfoilsDistribution(List<Double> maxThicknessAirfoilsDistribution) {
		this.maxThicknessAirfoilsDistribution = maxThicknessAirfoilsDistribution;
	}

	public List<Double> getLeadingEdgeRadiusAirfoilsDistribution() {
		return leadingEdgeRadiusAirfoilsDistribution;
	}

	public void setLeadingEdgeRadiusAirfoilsDistribution(List<Double> leadingEdgeRadiusAirfoilsDistribution) {
		this.leadingEdgeRadiusAirfoilsDistribution = leadingEdgeRadiusAirfoilsDistribution;
	}

	public List<Amount<?>> getClAlphaAirfoilsDistribution() {
		return clAlphaAirfoilsDistribution;
	}

	public void setClAlphaAirfoilsDistribution(List<Amount<?>> clAlphaAirfoilsDistribution) {
		this.clAlphaAirfoilsDistribution = clAlphaAirfoilsDistribution;
	}

	public List<Double> getCl0AirfoilsDistribution() {
		return cl0AirfoilsDistribution;
	}

	public void setCl0AirfoilsDistribution(List<Double> cl0AirfoilsDistribution) {
		this.cl0AirfoilsDistribution = cl0AirfoilsDistribution;
	}

	public AirfoilFamilyEnum getAirfoilsFamily() {
		return airfoilsFamily;
	}

	public void setAirfoilsFamily(AirfoilFamilyEnum airfoilsFamilyDistribution) {
		this.airfoilsFamily = airfoilsFamilyDistribution;
	}

	public Integer getFlapsNumber() {
		return flapsNumber;
	}

	public void setFlapsNumber(Integer flapsNumber) {
		this.flapsNumber = flapsNumber;
	}

	public Integer getSlatsNumber() {
		return slatsNumber;
	}

	public void setSlatsNumber(Integer slatsNumber) {
		this.slatsNumber = slatsNumber;
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

	public List<Double> getEtaStations() {
		return etaStations;
	}

	public void setEtaStations(List<Double> etaStations) {
		this.etaStations = etaStations;
	}

	public List<SymmetricFlapCreator> getSymmetricFlapCreatorList() {
		return symmetricFlapCreatorList;
	}

	public void setSymmetricFlapCreatorList(List<SymmetricFlapCreator> symmetricFlapCreatorList) {
		this.symmetricFlapCreatorList = symmetricFlapCreatorList;
	}

	public boolean isPlotCharts() {
		return plotCharts;
	}

	public void setPlotCharts(boolean plotCharts) {
		this.plotCharts = plotCharts;
	}

}