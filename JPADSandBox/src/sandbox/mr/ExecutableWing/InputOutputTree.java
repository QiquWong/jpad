package sandbox.mr.ExecutableWing;

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
	
	private double machNumber,
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
						 alphaStarDistribution;


	private List<Double> maximumliftCoefficientDistribution;
	
	
	// OUTPUT 
	
	private Amount<Length> span,
						   semiSpan;
	
	private Amount<Angle> alphaZeroLift,
	                      alphaStar,
	                      alphaStall;
	
	private int numberOfAlphaCL = 50;
	
	private double clAlpha,
				   clStar,
				   clMax;
	
	private List<double[]> clVsEtaVectors; // la lunghezza del vettore dipende dal numero di punti lungo la semiapertura,
	// la lunghezza di elementi della lista è il numero di valori di alfa che si danno in input
	
	private double [] cLVsAlphaVector;  // number of element = numberOfAlphaCL
	
	
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
	
		machNumber = 0.0;
		aspectRatio = 0.0;
		adimensionalKinkStation = 0.0;
		
		numberOfAlpha = 0;
		numberOfPointSemispan = 0;
		numberOfSections = 0;
		

	}
	// Poi un metodo inizializza gli output

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
		return clAlpha;
	}


	public void setClAlpha(double clAlpha) {
		this.clAlpha = clAlpha;
	}


	public double getClStar() {
		return clStar;
	}


	public void setClStar(double clStar) {
		this.clStar = clStar;
	}


	public double getClMax() {
		return clMax;
	}


	public void setClMax(double clMax) {
		this.clMax = clMax;
	}


	public List<double[]> getClVsEtaVectors() {
		return clVsEtaVectors;
	}


	public void setClVsEtaVectors(List<double[]> clVsEtaVectors) {
		this.clVsEtaVectors = clVsEtaVectors;
	}


	public double[] getcLVsAlphaVector() {
		return cLVsAlphaVector;
	}


	public void setcLVsAlphaVector(double[] cLVsAlphaVector) {
		this.cLVsAlphaVector = cLVsAlphaVector;
	}
	

}
