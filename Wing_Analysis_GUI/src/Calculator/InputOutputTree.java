package Calculator;

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
	private List<Amount<Length>> chordDistribution,
	xLEDistribution;
	
	private List<Amount<Angle>>
	dihedralDistribution,
	twistDistribution,
	alphaZeroLiftDistribution,
	alphaStarDistribution;


	private List<Double> maximumliftCoefficientDistribution,
	yAdimensionalStationInput;


	// OUTPUT 

	private Amount<Length> span,
	semiSpan;

	private Amount<Angle> alphaZeroLift,
	alphaStar,
	alphaStall;

	int numberOfAlphaCL = 50;

	private double
	cLZero,
	cLAlpha,
	cLStar,
	cLMax,
	deltaAlpha;

	private List<Double[]> clVsEtaVectors; 

	double [] cLVsAlphaVector, alphaVector, yStationsAdimensional;  // number of element = numberOfAlphaCL

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

		chordDistribution = new ArrayList<Amount<Length>>();
		xLEDistribution = new ArrayList<Amount<Length>>();
		dihedralDistribution = new ArrayList<Amount<Angle>>();
		twistDistribution = new ArrayList<Amount<Angle>>();

		alphaZeroLiftDistribution = new ArrayList<Amount<Angle>>();
		alphaStarDistribution = new ArrayList<Amount<Angle>>();
		maximumliftCoefficientDistribution = new ArrayList<Double>();
		yAdimensionalStationInput = new ArrayList<Double>();

		machNumber = 0.0;
		aspectRatio = 0.0;
		adimensionalKinkStation = 0.0;
		meanThickness = 0.0;

		numberOfAlpha = 0;
		numberOfPointSemispan = 0;
		numberOfSections = 0;

		cLVsAlphaVector = new double [numberOfAlphaCL];
		alphaVector = new double [numberOfAlphaCL];
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


	public List<Amount<Length>> getChordDistribution() {
		return chordDistribution;
	}


	public void setChordDistribution(List<Amount<Length>> chordDistribution) {
		this.chordDistribution = chordDistribution;
	}


	public List<Amount<Length>> getxLEDistribution() {
		return xLEDistribution;
	}


	public void setxLEDistribution(List<Amount<Length>> xLEDistribution) {
		this.xLEDistribution = xLEDistribution;
	}


	public List<Amount<Angle>> getDihedralDistribution() {
		return dihedralDistribution;
	}


	public void setDihedralDistribution(List<Amount<Angle>> dihedralDistribution) {
		this.dihedralDistribution = dihedralDistribution;
	}


	public List<Amount<Angle>> getTwistDistribution() {
		return twistDistribution;
	}


	public void setTwistDistribution(List<Amount<Angle>> twistDistribution) {
		this.twistDistribution = twistDistribution;
	}


	public List<Amount<Angle>> getAlphaZeroLiftDistribution() {
		return alphaZeroLiftDistribution;
	}

	public void setAlphaZeroLiftDistribution(List<Amount<Angle>> alphaZeroLiftDistribution) {
		this.alphaZeroLiftDistribution = alphaZeroLiftDistribution;
	}

	public List<Amount<Angle>> getAlphaStarDistribution() {
		return alphaStarDistribution;
	}

	public void setAlphaStarDistribution(List<Amount<Angle>> alphaStarDistribution) {
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
		return alphaZeroLift;
	}


	public void setAlphaZeroLift(Amount<Angle> alphaZeroLift) {
		this.alphaZeroLift = alphaZeroLift;
	}


	public Amount<Angle> getAlphaStar() {
		return alphaStar;
	}


	public void setAlphaStar(Amount<Angle> alphaStar) {
		this.alphaStar = alphaStar;
	}


	public Amount<Angle> getAlphaStall() {
		return alphaStall;
	}


	public void setAlphaStall(Amount<Angle> alphaStall) {
		this.alphaStall = alphaStall;
	}


	public int getNumberOfAlphaCL() {
		return numberOfAlphaCL;
	}


	public void setNumberOfAlphaCL(int numberOfAlphaCL) {
		this.numberOfAlphaCL = numberOfAlphaCL;
	}


	public double getClAlpha() {
		return cLAlpha;
	}


	public void setClAlpha(double clAlpha) {
		this.cLAlpha = clAlpha;
	}


	public double getClStar() {
		return cLStar;
	}


	public void setClStar(double clStar) {
		this.cLStar = clStar;
	}


	public double getClMax() {
		return cLMax;
	}


	public void setClMax(double clMax) {
		this.cLMax = clMax;
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
		return deltaAlpha;
	}


	public void setDeltaAlpha(double deltaAlpha) {
		this.deltaAlpha = deltaAlpha;
	}


	public double getcLZero() {
		return cLZero;
	}


	public void setcLZero(double cLZero) {
		this.cLZero = cLZero;
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


}
