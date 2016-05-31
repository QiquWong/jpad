package sandbox.mr.HighLiftStallPath;

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

public class InputOutputTree {
	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:

	// INPUT

	private Amount<Angle> alphaCurrent,
	alphaInitial,
	alphaFinal,
	sweepLE;

	private Amount<Length> altitude;

	private Amount<Area> surface;

	private double  meanThickness,
	machNumber,
	aspectRatio,
	adimensionalKinkStation;

	private int numberOfAlpha,
	numberOfPointSemispan,
	numberOfSections;

	private AirfoilFamilyEnum meanAirfoilFamily;
	private List<Amount> chordDistribution,
	xLEDistribution,
	dihedralDistribution,
	twistDistribution,
	alphaZeroLiftDistribution,
	alphaStarDistribution,
	leadingEdgeRdiusDistribution;

	private int flapsNumber, slatsNumber;
	
	private List<Double> maximumliftCoefficientDistribution,
	maximumTicknessDistribution,
	yAdimensionalStationInput,
	clalphaDistribution,
	clZeroDistribution;

	private List<FlapTypeEnum> flapType;

	private List<Double> cfc,
	                     csc,
	                     cExtCSlat,
	                     etaInFlap,
	                     etaOutFlap,
	                     etaInSlat,
	                     etaOutSlat;
	private List<Amount<Angle>> deltaFlap,
    							deltaSlat;
	
	List<Double> influenceCoefficient;
	

	// OUTPUT 

	private Amount<Length> span,
	semiSpan;

	private Amount<Angle> alphaZeroLiftClean,
	alphaStarClean,
	alphaStallClean,
	alphaZeroLiftHL,
	alphaStarCleanHL,
	alphaStallCleanHL;

	int numberOfAlphaCL = 50;

	private double
	cLZeroClean,
	cLAlphaClean,
	cLStarClean,
	cLMaxClean,
	deltaAlphaClean,
	cLZeroHL,
	cLAlphaHL,
	cLStarHL,
	cLMaxHL,
	deltaAlphaHL;
	
	List<Double> clMaxCleanArray, clMaxHighLiftArray, clmaxairfoils, etaStations;
	
	
	private List<Double[]> clVsEtaVectors; 
	double [] cLVsAlphaVector, alphaVector, yStationsAdimensional, clMaxVector;  // number of element = numberOfAlphaCL

	double [] alphaDistributionArray;
	

	//------------------------------------------------------------------------------------------
	// BUILDER:

	public InputOutputTree() {

		alphaCurrent = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		alphaInitial = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		alphaFinal = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		sweepLE = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

		altitude = Amount.valueOf(0.0, SI.METER);

		surface = Amount.valueOf(0.0, SI.SQUARE_METRE);

		chordDistribution = new ArrayList<Amount>();
		xLEDistribution = new ArrayList<Amount>();
		dihedralDistribution = new ArrayList<Amount>();
		twistDistribution = new ArrayList<Amount>();

		alphaZeroLiftDistribution = new ArrayList<Amount>();
		alphaStarDistribution = new ArrayList<Amount>();
		maximumliftCoefficientDistribution = new ArrayList<Double>();
		yAdimensionalStationInput = new ArrayList<Double>();
		maximumTicknessDistribution = new ArrayList<Double>();
		leadingEdgeRdiusDistribution = new ArrayList<Amount>();
		
		clalphaDistribution  = new ArrayList<Double>();
		clZeroDistribution  = new ArrayList<Double>();
		
		influenceCoefficient = new ArrayList<Double>();
		
		cfc = new ArrayList<Double>();
        csc = new ArrayList<Double>();
        cExtCSlat = new ArrayList<Double>();
        etaInFlap = new ArrayList<Double>();
        etaOutFlap = new ArrayList<Double>();
        etaInSlat = new ArrayList<Double>();
        etaOutSlat = new ArrayList<Double>();
		
		machNumber = 0.0;
		aspectRatio = 0.0;
		adimensionalKinkStation = 0.0;
		meanThickness = 0.0;

		numberOfAlpha = 0;
		numberOfPointSemispan = 0;
		numberOfSections = 0;

		cLVsAlphaVector = new double [numberOfAlphaCL];
		alphaVector = new double [numberOfAlphaCL];
		
		flapsNumber = 0;
		slatsNumber = 0;
		
		deltaFlap = new ArrayList<Amount<Angle>>();
		deltaSlat = new ArrayList<Amount<Angle>>();
		
		flapType = new ArrayList<FlapTypeEnum>();
		
	}


	public void buildOutput(){
		
		yStationsAdimensional =  new double [numberOfPointSemispan];
		clVsEtaVectors = new ArrayList<Double[]>();
		alphaDistributionArray = new double [numberOfAlpha];
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:

	public Amount<Angle> getAlphaCurrent() {
		return alphaCurrent;
	}


	public void setAlphaCurrent(Amount<Angle> alphaCurrent) {
		this.alphaCurrent = alphaCurrent;
	}


	public Amount<Angle> getAlphaInitial() {
		return alphaInitial;
	}


	public void setAlphaInitial(Amount<Angle> alphaInitial) {
		this.alphaInitial = alphaInitial;
	}


	public Amount<Angle> getAlphaFinal() {
		return alphaFinal;
	}


	public void setAlphaFinal(Amount<Angle> alphaFinal) {
		this.alphaFinal = alphaFinal;
	}


	public Amount<Angle> getSweepLE() {
		return sweepLE;
	}


	public void setSweepLE(Amount<Angle> sweepLE) {
		this.sweepLE = sweepLE;
	}


	public Amount<Length> getAltitude() {
		return altitude;
	}


	public void setAltitude(Amount<Length> altitude) {
		this.altitude = altitude;
	}


	public Amount<Area> getSurface() {
		return surface;
	}


	public void setSurface(Amount<Area> surface) {
		this.surface = surface;
	}


	public double getMachNumber() {
		return machNumber;
	}


	public void setMachNumber(double machNumber) {
		this.machNumber = machNumber;
	}


	public double getAspectRatio() {
		return aspectRatio;
	}


	public void setAspectRatio(double aspectRatio) {
		this.aspectRatio = aspectRatio;
	}


	public double getAdimensionalKinkStation() {
		return adimensionalKinkStation;
	}


	public void setAdimensionalKinkStation(double adimensionalKinkStation) {
		this.adimensionalKinkStation = adimensionalKinkStation;
	}


	public int getNumberOfAlpha() {
		return numberOfAlpha;
	}


	public void setNumberOfAlpha(int numberOfAlpha) {
		this.numberOfAlpha = numberOfAlpha;
	}


	public int getNumberOfPointSemispan() {
		return numberOfPointSemispan;
	}


	public void setNumberOfPointSemispan(int numberOfPointSemispan) {
		this.numberOfPointSemispan = numberOfPointSemispan;
	}


	public int getNumberOfSections() {
		return numberOfSections;
	}


	public void setNumberOfSections(int numberOfSections) {
		this.numberOfSections = numberOfSections;
	}


	public AirfoilFamilyEnum getMeanAirfoilFamily() {
		return meanAirfoilFamily;
	}


	public void setMeanAirfoilFamily(AirfoilFamilyEnum meanAirfoilFamily) {
		this.meanAirfoilFamily = meanAirfoilFamily;
	}


	public List<Amount> getChordDistribution() {
		return chordDistribution;
	}


	public void setChordDistribution(List<Amount> chordDistribution) {
		this.chordDistribution = chordDistribution;
	}


	public List<Amount> getxLEDistribution() {
		return xLEDistribution;
	}


	public void setxLEDistribution(List<Amount> xLEDistribution) {
		this.xLEDistribution = xLEDistribution;
	}


	public List<Amount> getDihedralDistribution() {
		return dihedralDistribution;
	}


	public void setDihedralDistribution(List<Amount> dihedralDistribution) {
		this.dihedralDistribution = dihedralDistribution;
	}


	public List<Amount> getTwistDistribution() {
		return twistDistribution;
	}


	public void setTwistDistribution(List<Amount> twistDistribution) {
		this.twistDistribution = twistDistribution;
	}


	public List<Amount> getAlphaZeroLiftDistribution() {
		return alphaZeroLiftDistribution;
	}

	public void setAlphaZeroLiftDistribution(List<Amount> alphaZeroLiftDistribution) {
		this.alphaZeroLiftDistribution = alphaZeroLiftDistribution;
	}

	public List<Amount> getAlphaStarDistribution() {
		return alphaStarDistribution;
	}

	public void setAlphaStarDistribution(List<Amount> alphaStarDistribution) {
		this.alphaStarDistribution = alphaStarDistribution;
	}

	public List<Double> getMaximumliftCoefficientDistribution() {
		return maximumliftCoefficientDistribution;
	}

	public void setMaximumliftCoefficientDistribution(List<Double> maximumliftCoefficientDistribution) {
		this.maximumliftCoefficientDistribution = maximumliftCoefficientDistribution;
	}

	public Amount<Length> getSpan() {
		return span;
	}


	public void setSpan(Amount<Length> span) {
		this.span = span;
	}


	public Amount<Length> getSemiSpan() {
		return semiSpan;
	}


	public void setSemiSpan(Amount<Length> semiSpan) {
		this.semiSpan = semiSpan;
	}


	public Amount<Angle> getAlphaZeroLift() {
		return alphaZeroLiftClean;
	}


	public void setAlphaZeroLift(Amount<Angle> alphaZeroLift) {
		this.alphaZeroLiftClean = alphaZeroLift;
	}


	public Amount<Angle> getAlphaStar() {
		return alphaStarClean;
	}


	public void setAlphaStar(Amount<Angle> alphaStar) {
		this.alphaStarClean = alphaStar;
	}


	public Amount<Angle> getAlphaStall() {
		return alphaStallClean;
	}


	public void setAlphaStall(Amount<Angle> alphaStall) {
		this.alphaStallClean = alphaStall;
	}


	public int getNumberOfAlphaCL() {
		return numberOfAlphaCL;
	}


	public void setNumberOfAlphaCL(int numberOfAlphaCL) {
		this.numberOfAlphaCL = numberOfAlphaCL;
	}


	public double getClAlpha() {
		return cLAlphaClean;
	}


	public void setClAlpha(double clAlpha) {
		this.cLAlphaClean = clAlpha;
	}


	public double getClStar() {
		return cLStarClean;
	}


	public void setClStar(double clStar) {
		this.cLStarClean = clStar;
	}


	public double getClMax() {
		return cLMaxClean;
	}


	public void setClMax(double clMax) {
		this.cLMaxClean = clMax;
	}


	public List<Double[]> getClVsEtaVectors() {
		return clVsEtaVectors;
	}


	public void setClVsEtaVectors(List<Double[]> clVsEtaVectors) {
		this.clVsEtaVectors = clVsEtaVectors;
	}


	public double[] getcLVsAlphaVector() {
		return cLVsAlphaVector;
	}


	public List<Double> getyAdimensionalStationInput() {
		return yAdimensionalStationInput;
	}


	public void setyAdimensionalStationInput(List<Double> yAdimensionalStationInput) {
		this.yAdimensionalStationInput = yAdimensionalStationInput;
	}


	public void setcLVsAlphaVector(double[] cLVsAlphaVector) {
		this.cLVsAlphaVector = cLVsAlphaVector;
	}


	public double getMeanThickness() {
		return meanThickness;
	}


	public void setMeanThickness(double meanThickness) {
		this.meanThickness = meanThickness;
	}


	public double getDeltaAlpha() {
		return deltaAlphaClean;
	}


	public void setDeltaAlpha(double deltaAlpha) {
		this.deltaAlphaClean = deltaAlpha;
	}


	public double getcLZero() {
		return cLZeroClean;
	}


	public void setcLZero(double cLZero) {
		this.cLZeroClean = cLZero;
	}



	public double[] getAlphaVector() {
		return alphaVector;
	}



	public void setAlphaVector(double[] alphaVector) {
		this.alphaVector = alphaVector;
	}


	public double[] getyStationsAdimensional() {
		return yStationsAdimensional;
	}


	public void setyStationsAdimensional(double[] yStationsAdimensional) {
		this.yStationsAdimensional = yStationsAdimensional;
	}


	public double[] getAlphaDistributionArray() {
		return alphaDistributionArray;
	}


	public void setAlphaDistributionArray(double[] alphaDistributionArray) {
		this.alphaDistributionArray = alphaDistributionArray;
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


	public double getcLAlpha() {
		return cLAlphaClean;
	}


	public void setcLAlpha(double cLAlpha) {
		this.cLAlphaClean = cLAlpha;
	}


	public double getcLStar() {
		return cLStarClean;
	}


	public void setcLStar(double cLStar) {
		this.cLStarClean = cLStar;
	}


	public double getcLMax() {
		return cLMaxClean;
	}


	public void setcLMax(double cLMax) {
		this.cLMaxClean = cLMax;
	}


	public double[] getClMaxVector() {
		return clMaxVector;
	}


	public void setClMaxVector(double[] clMaxVector) {
		this.clMaxVector = clMaxVector;
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


	public List<Double> getClalphaDistribution() {
		return clalphaDistribution;
	}


	public void setClalphaDistribution(List<Double> clalphaDistribution) {
		this.clalphaDistribution = clalphaDistribution;
	}


	public List<Double> getClZeroDistribution() {
		return clZeroDistribution;
	}


	public void setClZeroDistribution(List<Double> clZeroDistribution) {
		this.clZeroDistribution = clZeroDistribution;
	}


	public Amount<Angle> getAlphaZeroLiftClean() {
		return alphaZeroLiftClean;
	}


	public void setAlphaZeroLiftClean(Amount<Angle> alphaZeroLiftClean) {
		this.alphaZeroLiftClean = alphaZeroLiftClean;
	}


	public Amount<Angle> getAlphaStarClean() {
		return alphaStarClean;
	}


	public void setAlphaStarClean(Amount<Angle> alphaStarClean) {
		this.alphaStarClean = alphaStarClean;
	}


	public Amount<Angle> getAlphaStallClean() {
		return alphaStallClean;
	}


	public void setAlphaStallClean(Amount<Angle> alphaStallClean) {
		this.alphaStallClean = alphaStallClean;
	}


	public double getcLZeroClean() {
		return cLZeroClean;
	}


	public void setcLZeroClean(double cLZeroClean) {
		this.cLZeroClean = cLZeroClean;
	}


	public double getcLAlphaClean() {
		return cLAlphaClean;
	}


	public void setcLAlphaClean(double cLAlphaClean) {
		this.cLAlphaClean = cLAlphaClean;
	}


	public double getcLStarClean() {
		return cLStarClean;
	}


	public void setcLStarClean(double cLStarClean) {
		this.cLStarClean = cLStarClean;
	}


	public double getcLMaxClean() {
		return cLMaxClean;
	}


	public void setcLMaxClean(double cLMaxClean) {
		this.cLMaxClean = cLMaxClean;
	}


	public double getDeltaAlphaClean() {
		return deltaAlphaClean;
	}


	public void setDeltaAlphaClean(double deltaAlphaClean) {
		this.deltaAlphaClean = deltaAlphaClean;
	}


	public double getcLZeroHL() {
		return cLZeroHL;
	}


	public void setcLZeroHL(double cLZeroHL) {
		this.cLZeroHL = cLZeroHL;
	}


	public double getcLAlphaHL() {
		return cLAlphaHL;
	}


	public void setcLAlphaHL(double cLAlphaHL) {
		this.cLAlphaHL = cLAlphaHL;
	}


	public double getcLStarHL() {
		return cLStarHL;
	}


	public void setcLStarHL(double cLStarHL) {
		this.cLStarHL = cLStarHL;
	}


	public double getcLMaxHL() {
		return cLMaxHL;
	}


	public void setcLMaxHL(double cLMaxHL) {
		this.cLMaxHL = cLMaxHL;
	}


	public double getDeltaAlphaHL() {
		return deltaAlphaHL;
	}


	public void setDeltaAlphaHL(double deltaAlphaHL) {
		this.deltaAlphaHL = deltaAlphaHL;
	}


	public Amount<Angle> getAlphaZeroLiftHL() {
		return alphaZeroLiftHL;
	}


	public void setAlphaZeroLiftHL(Amount<Angle> alphaZeroLiftHL) {
		this.alphaZeroLiftHL = alphaZeroLiftHL;
	}


	public Amount<Angle> getAlphaStarCleanHL() {
		return alphaStarCleanHL;
	}


	public void setAlphaStarCleanHL(Amount<Angle> alphaStarCleanHL) {
		this.alphaStarCleanHL = alphaStarCleanHL;
	}


	public Amount<Angle> getAlphaStallCleanHL() {
		return alphaStallCleanHL;
	}


	public void setAlphaStallCleanHL(Amount<Angle> alphaStallCleanHL) {
		this.alphaStallCleanHL = alphaStallCleanHL;
	}


	public List<Double> getClMaxCleanArray() {
		return clMaxCleanArray;
	}


	public void setClMaxCleanArray(List<Double> clMaxCleanArray) {
		this.clMaxCleanArray = clMaxCleanArray;
	}


	public List<Double> getClMaxHighLiftArray() {
		return clMaxHighLiftArray;
	}


	public void setClMaxHighLiftArray(List<Double> clMaxHighLiftArray) {
		this.clMaxHighLiftArray = clMaxHighLiftArray;
	}


	public List<Double> getClmaxairfoils() {
		return clmaxairfoils;
	}


	public void setClmaxairfoils(List<Double> clmaxairfoils) {
		this.clmaxairfoils = clmaxairfoils;
	}


	public List<Double> getEtaStations() {
		return etaStations;
	}


	public void setEtaStations(List<Double> etaStations) {
		this.etaStations = etaStations;
	}


	public List<Double> getInfluenceCoefficient() {
		return influenceCoefficient;
	}


	public void setInfluenceCoefficient(List<Double> influenceCoefficient) {
		this.influenceCoefficient = influenceCoefficient;
	}


	public List<Double> getMaximumTickness() {
		return maximumTicknessDistribution;
	}


	public void setMaximumTickness(List<Double> maximumTickness) {
		this.maximumTicknessDistribution = maximumTickness;
	}


	public List<Amount> getLeadingEdgeRdiusDistribution() {
		return leadingEdgeRdiusDistribution;
	}


	public void setLeadingEdgeRdiusDistribution(List<Amount> leadingEdgeRdiusDistribution) {
		this.leadingEdgeRdiusDistribution = leadingEdgeRdiusDistribution;
	}


}
