package it.unina.daf.test.winganalysis;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.quantity.Length;
import javax.measure.quantity.Area;

import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.creator.SlatCreator;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;


public class InputOutputTree {
	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:

	// INPUT------------------------------------------------------------------------------------

	private Amount<Length> altitude;
	private double machNumber;	
	private int numberOfAlpha;
	private Amount<Angle> alphaInitial,
	alphaFinal;
	
	private Amount<Area> surface;
	private double aspectRatio,
	taperRatio,
	momentumPole;
	private int numberOfPointSemispan;
	
	private int numberOfSections;
	private AirfoilFamilyEnum meanAirfoilFamily;
	private AirfoilTypeEnum meanAirfoilType;
	private double  meanThickness;
	
	private List<Double> yAdimensionalStationInput;
	private List<Amount<Length>> chordDistributionInput,
	xLEDistributionInput, leRadiusDistribution;;
	
	
	private List<Amount<Angle>> alphaZeroLiftDistributionInput,
	alphaStarDistributionInput, alphaStallDistributionInput, twistDistributionInput;
	private List<Double> maximumliftCoefficientDistributionInput,
	cdMinDistributionInput,
	clZeroDistributionInput,
	clalphaDEGDistributionInput,
	clIdealDistributionInput,
	kDistributionInput,
	cmc4DistributionInput,
    maxThicknessAirfoilsDistribution;
	
	private boolean plotFlag;
	private boolean verbosityFlag;
	private boolean highLiftFlag;
	
	//high lift
	int flapsNumber, slatsNumber;
	
	private List<FlapTypeEnum> flapType;
	private List<Double> cfcInner;
	private List<Double> cfcOuter;
	private List<Double> csc;
	private List<Double> cExtCSlat;
	private List<Double> etaInFlap;
	private List<Double> etaOutFlap;
	private List<Double> etaInSlat;
	private List<Double> etaOutSlat;
	private List<Amount<Angle>> deltaFlap;
	private List<Amount<Angle>> deltaSlat;	
	private List<SymmetricFlapCreator> symmetricFlapCreatorList;
	private List<SlatCreator> slatCreatorList;
	
	private Double  _deltaCMc4List,
	_deltaCL0Flap, 
	_deltaCLmaxFlap, 
	_deltaCLmaxSlat,
	_deltaCD0, 
	_deltaCMc4;
	private Amount<?> _cLAlphaHighLift;


	// CALCULATED------------------------------------------------------------------------------------
	List<Double> yAdimensionalStationActual;
	private List<Amount<Length>> yDimensionaStationActual, chordDistributionActual,
	xLEDistributionActual, yDimensionalInput, leRadiusDistributionActual;;
	
	private List<Amount<Angle>> alphaZeroLiftDistributionActual,
	alphaStarDistributionActual,
	alphaStallDistributionActual,
	twistDistributionActual;
	
	private List<Double> maximumliftCoefficientDistributionActual,
	cdMinDistributionActual,
	clZeroDistributionActual,
	clalphaDEGDistributionActual,
	clIdealDistributionActual,
	kDistributionActual,
	cmc4DistributionActual,
	maxThicknessAirfoilsDistributionActual;
	
	private Amount<Angle> sweepLE;
	private Amount<Angle> sweepHalfChord;
	private Amount<Angle> sweepQuarterChord;
	private Amount<Length> span, semiSpan;
	int numberOfAlphaCL = 50;
	
	List<List<Double>> _discretizedAirfoilsCl, _discretizedAirfoilsCd, _discretizedAirfoilsCm;
	
	AerodynamicDatabaseReader theAerodatabaseReader;
	HighLiftDatabaseReader theHighLiftDatabaseReader;
	
	// OUTPUT------------------------------------------------------------------------------------
	private Amount<Angle> alphaZeroLift,
	alphaStar,
	alphaStall,
	alphaMaxLinear;

	private double cLZero,
	cLAlpha,
	cLStar,
	cLMax,
	deltaAlpha;
	
	private Amount<Length> mac;
	
	//CL
	private List<Amount<Angle>> alphaVector, alphaDistributionArray;
	private List<Double> yStationsAdimensional, clDistributionAtStall, cLVsAlphaVector; 
	private List<List<Double>> clVsEtaVectors;
	
	//DRAG
	private List<Double> polarClean;
	private List<Double> wawePolar;
	private List<Double> parasitePolar;
	private List<Double> inducedPolar;
	private List<List<Double>> totalDragDistribution;
	private List<List<Double>> parasiteDragDistribution;
	private List<List<Double>> inducedDragDistribution;
	private List<List<Double>> waweDragDistribution;
	
	
	//MOMENT
	private List<List<Double>> cmVsEtaVectors;
	private List<Double> momentCurveClean;
	
	//HIGH LIFT
	private List<Amount<Angle>> alphaVectorHighLift;
	private List<Double> clVsAlphaHighLift;
	private List<Double> polarHighLift;
	private List<Double> momentCurveHighLift;
	
	private Amount<Angle> alphaZeroLiftHL,
	alphaStarHL,
	alphaStallHL,
	alphaMaxLinearHL;

	private double cLZeroHL,
	cLAlphaHL,
	cLStarHL,
	cLMaxHL,
	deltaAlphaHL;
	
	private List<Amount<Angle>> alphaVectorHL;
	
	//------------------------------------------------------------------------------------------
	// BUILDER:

	public InputOutputTree() {

		alphaInitial = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		alphaFinal = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		sweepLE = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

		altitude = Amount.valueOf(0.0, SI.METER);

		surface = Amount.valueOf(0.0, SI.SQUARE_METRE);

		chordDistributionInput = new ArrayList<Amount<Length>>();
		xLEDistributionInput = new ArrayList<Amount<Length>>();

		twistDistributionInput = new ArrayList<>();
		alphaZeroLiftDistributionInput = new ArrayList<Amount<Angle>>();
		alphaStarDistributionInput = new ArrayList<Amount<Angle>>();
		alphaStallDistributionInput = new ArrayList<>();
		maximumliftCoefficientDistributionInput = new ArrayList<Double>();
		yAdimensionalStationInput = new ArrayList<Double>();
		cdMinDistributionInput = new ArrayList<Double>();
		clIdealDistributionInput = new ArrayList<Double>();
		kDistributionInput = new ArrayList<Double>();
		cmc4DistributionInput = new ArrayList<Double>();
		chordDistributionActual = new ArrayList<>();
		symmetricFlapCreatorList = new ArrayList<>();
		slatCreatorList = new ArrayList<>();
	    leRadiusDistribution = new ArrayList<>();
	    maxThicknessAirfoilsDistribution = new ArrayList<>();
	    yDimensionalInput = new ArrayList<>();
	
		machNumber = 0.0;
		aspectRatio = 0.0;
		meanThickness = 0.0;

		numberOfAlpha = 0;
		numberOfPointSemispan = 0;
		numberOfSections = 0;

		 alphaVector= new ArrayList<>();
		 alphaDistributionArray = new ArrayList<>();
		 yStationsAdimensional = new ArrayList<>(); 
		 clDistributionAtStall = new ArrayList<>();
		 cLVsAlphaVector = new ArrayList<>();
		 clVsEtaVectors = new ArrayList<>();
		 polarClean = new ArrayList<>();
		 totalDragDistribution = new ArrayList<>();
		 momentCurveClean = new ArrayList<>();
		 alphaVectorHighLift = new ArrayList<>();
	     clVsAlphaHighLift = new ArrayList<>();
		 polarHighLift = new ArrayList<>();
		 momentCurveHighLift = new ArrayList<>();
		 polarClean = new ArrayList<>();
		 wawePolar = new ArrayList<>();
		 parasitePolar = new ArrayList<>();
		 inducedPolar = new ArrayList<>();
		 totalDragDistribution = new ArrayList<>();
		 parasiteDragDistribution = new ArrayList<>();
		 inducedDragDistribution = new ArrayList<>();
		 waweDragDistribution = new ArrayList<>();
		 clZeroDistributionInput = new ArrayList<>();
		 clalphaDEGDistributionInput = new ArrayList<>();
		 cmVsEtaVectors = new ArrayList<>();
		 
		flapType = new ArrayList<>();
		cfcInner = new ArrayList<>();
		cfcOuter = new ArrayList<>();
		csc = new ArrayList<>();
		cExtCSlat = new ArrayList<>();
		etaInFlap = new ArrayList<>();
		etaOutFlap = new ArrayList<>();
		etaInSlat = new ArrayList<>();
		etaOutSlat = new ArrayList<>();
		deltaFlap = new ArrayList<>();
		deltaSlat = new ArrayList<>();	
		
		alphaVectorHL = new ArrayList<>();
		
		 _discretizedAirfoilsCl = new ArrayList<>();
		 _discretizedAirfoilsCd =  new ArrayList<>();
		 _discretizedAirfoilsCm = new ArrayList<>();
	}


	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:


	public Amount<Length> getAltitude() {
		return altitude;
	}


	public void setAltitude(Amount<Length> altitude) {
		this.altitude = altitude;
	}


	public double getMachNumber() {
		return machNumber;
	}


	public void setMachNumber(double machNumber) {
		this.machNumber = machNumber;
	}


	public int getNumberOfAlpha() {
		return numberOfAlpha;
	}


	public void setNumberOfAlpha(int numberOfAlpha) {
		this.numberOfAlpha = numberOfAlpha;
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


	public double getMeanThickness() {
		return meanThickness;
	}


	public void setMeanThickness(double meanThickness) {
		this.meanThickness = meanThickness;
	}


	public List<Double> getyAdimensionalStationInput() {
		return yAdimensionalStationInput;
	}


	public void setyAdimensionalStationInput(List<Double> yAdimensionalStationInput) {
		this.yAdimensionalStationInput = yAdimensionalStationInput;
	}


	public List<Amount<Length>> getChordDistributionInput() {
		return chordDistributionInput;
	}


	public void setChordDistributionInput(List<Amount<Length>> chordDistributionInput) {
		this.chordDistributionInput = chordDistributionInput;
	}


	public List<Amount<Length>> getxLEDistributionInput() {
		return xLEDistributionInput;
	}


	public void setxLEDistributionInput(List<Amount<Length>> xLEDistributionInput) {
		this.xLEDistributionInput = xLEDistributionInput;
	}


	public List<Amount<Angle>> getTwistDistributionInput() {
		return twistDistributionInput;
	}


	public void setTwistDistributionInput(List<Amount<Angle>> twistDistributionInput) {
		this.twistDistributionInput = twistDistributionInput;
	}


	public List<Amount<Angle>> getAlphaZeroLiftDistributionInput() {
		return alphaZeroLiftDistributionInput;
	}


	public void setAlphaZeroLiftDistributionInput(List<Amount<Angle>> alphaZeroLiftDistributionInput) {
		this.alphaZeroLiftDistributionInput = alphaZeroLiftDistributionInput;
	}


	public List<Amount<Angle>> getAlphaStarDistributionInput() {
		return alphaStarDistributionInput;
	}


	public void setAlphaStarDistributionInput(List<Amount<Angle>> alphaStarDistributionInput) {
		this.alphaStarDistributionInput = alphaStarDistributionInput;
	}


	public List<Double> getMaximumliftCoefficientDistributionInput() {
		return maximumliftCoefficientDistributionInput;
	}


	public void setMaximumliftCoefficientDistributionInput(List<Double> maximumliftCoefficientDistributionInput) {
		this.maximumliftCoefficientDistributionInput = maximumliftCoefficientDistributionInput;
	}


	public List<Double> getCdMinDistributionInput() {
		return cdMinDistributionInput;
	}


	public void setCdMinDistributionInput(List<Double> cdMinDistributionInput) {
		this.cdMinDistributionInput = cdMinDistributionInput;
	}


	public List<Double> getClIdealDistributionInput() {
		return clIdealDistributionInput;
	}


	public void setClIdealDistributionInput(List<Double> clIdealDistributionInput) {
		this.clIdealDistributionInput = clIdealDistributionInput;
	}


	public List<Double> getkDistributionInput() {
		return kDistributionInput;
	}


	public void setkDistributionInput(List<Double> kDistributionInput) {
		this.kDistributionInput = kDistributionInput;
	}


	public List<Double> getCmc4DistributionInput() {
		return cmc4DistributionInput;
	}


	public void setCmc4DistributionInput(List<Double> cmc4DistributionInput) {
		this.cmc4DistributionInput = cmc4DistributionInput;
	}


	public List<Double> getyAdimensionalStationActual() {
		return yAdimensionalStationActual;
	}


	public void setyAdimensionalStationActual(List<Double> yAdimensionalStationActual) {
		this.yAdimensionalStationActual = yAdimensionalStationActual;
	}


	public List<Amount<Length>> getyDimensionaStationActual() {
		return yDimensionaStationActual;
	}


	public void setyDimensionaStationActual(List<Amount<Length>> yDimensionaStationActual) {
		this.yDimensionaStationActual = yDimensionaStationActual;
	}


	public List<Amount<Length>> getChordDistributionActual() {
		return chordDistributionActual;
	}


	public void setChordDistributionActual(List<Amount<Length>> chordDistributionActual) {
		this.chordDistributionActual = chordDistributionActual;
	}


	public List<Amount<Length>> getxLEDistributionActual() {
		return xLEDistributionActual;
	}


	public void setxLEDistributionActual(List<Amount<Length>> xLEDistributionActual) {
		this.xLEDistributionActual = xLEDistributionActual;
	}


	public List<Amount<Angle>> getTwistDistributionActual() {
		return twistDistributionActual;
	}


	public void setTwistDistributionActual(List<Amount<Angle>> twistDistributionActual) {
		this.twistDistributionActual = twistDistributionActual;
	}


	public List<Amount<Angle>> getAlphaZeroLiftDistributionActual() {
		return alphaZeroLiftDistributionActual;
	}


	public void setAlphaZeroLiftDistributionActual(List<Amount<Angle>> alphaZeroLiftDistributionActual) {
		this.alphaZeroLiftDistributionActual = alphaZeroLiftDistributionActual;
	}


	public List<Amount<Angle>> getAlphaStarDistributionActual() {
		return alphaStarDistributionActual;
	}


	public void setAlphaStarDistributionActual(List<Amount<Angle>> alphaStarDistributionActual) {
		this.alphaStarDistributionActual = alphaStarDistributionActual;
	}


	public List<Double> getMaximumliftCoefficientDistributionActual() {
		return maximumliftCoefficientDistributionActual;
	}


	public void setMaximumliftCoefficientDistributionActual(List<Double> maximumliftCoefficientDistributionActual) {
		this.maximumliftCoefficientDistributionActual = maximumliftCoefficientDistributionActual;
	}


	public List<Double> getCdMinDistributionActual() {
		return cdMinDistributionActual;
	}


	public void setCdMinDistributionActual(List<Double> cdMinDistributionActual) {
		this.cdMinDistributionActual = cdMinDistributionActual;
	}


	public List<Double> getClIdealDistributionActual() {
		return clIdealDistributionActual;
	}


	public void setClIdealDistributionActual(List<Double> clIdealDistributionActual) {
		this.clIdealDistributionActual = clIdealDistributionActual;
	}


	public List<Double> getkDistributionActual() {
		return kDistributionActual;
	}


	public void setkDistributionActual(List<Double> kDistributionActual) {
		this.kDistributionActual = kDistributionActual;
	}


	public List<Double> getCmc4DistributionActual() {
		return cmc4DistributionActual;
	}


	public void setCmc4DistributionActual(List<Double> cmc4DistributionActual) {
		this.cmc4DistributionActual = cmc4DistributionActual;
	}


	public Amount<Angle> getSweepLE() {
		return sweepLE;
	}


	public void setSweepLE(Amount<Angle> sweepLE) {
		this.sweepLE = sweepLE;
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


	public int getNumberOfAlphaCL() {
		return numberOfAlphaCL;
	}


	public void setNumberOfAlphaCL(int numberOfAlphaCL) {
		this.numberOfAlphaCL = numberOfAlphaCL;
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


	public double getcLZero() {
		return cLZero;
	}


	public void setcLZero(double cLZero) {
		this.cLZero = cLZero;
	}


	public double getcLAlpha() {
		return cLAlpha;
	}


	public void setcLAlpha(double cLAlpha) {
		this.cLAlpha = cLAlpha;
	}


	public double getcLStar() {
		return cLStar;
	}


	public void setcLStar(double cLStar) {
		this.cLStar = cLStar;
	}


	public double getcLMax() {
		return cLMax;
	}


	public void setcLMax(double cLMax) {
		this.cLMax = cLMax;
	}


	public double getDeltaAlpha() {
		return deltaAlpha;
	}


	public void setDeltaAlpha(double deltaAlpha) {
		this.deltaAlpha = deltaAlpha;
	}


	public List<Amount<Angle>> getAlphaVector() {
		return alphaVector;
	}


	public void setAlphaVector(List<Amount<Angle>> alphaVector) {
		this.alphaVector = alphaVector;
	}


	public List<Amount<Angle>> getAlphaDistributionArray() {
		return alphaDistributionArray;
	}


	public void setAlphaDistributionArray(List<Amount<Angle>> alphaDistributionArray) {
		this.alphaDistributionArray = alphaDistributionArray;
	}


	public List<Double> getyStationsAdimensional() {
		return yStationsAdimensional;
	}


	public void setyStationsAdimensional(List<Double> yStationsAdimensional) {
		this.yStationsAdimensional = yStationsAdimensional;
	}


	public List<Double> getClDistributionAtStall() {
		return clDistributionAtStall;
	}


	public void setClDistributionAtStall(List<Double> clDistributionAtStall) {
		this.clDistributionAtStall = clDistributionAtStall;
	}


	public List<Double> getcLVsAlphaVector() {
		return cLVsAlphaVector;
	}


	public void setcLVsAlphaVector(List<Double> cLVsAlphaVector) {
		this.cLVsAlphaVector = cLVsAlphaVector;
	}


	public List<List<Double>> getClVsEtaVectors() {
		return clVsEtaVectors;
	}


	public void setClVsEtaVectors(List<List<Double>> clVsEtaVectors) {
		this.clVsEtaVectors = clVsEtaVectors;
	}


	public List<Double> getPolarClean() {
		return polarClean;
	}


	public void setPolarClean(List<Double> polarClean) {
		this.polarClean = polarClean;
	}


	public List<List<Double>> getDragDistribution() {
		return totalDragDistribution;
	}


	public void setDragDistribution(List<List<Double>> dragDistribution) {
		this.totalDragDistribution = dragDistribution;
	}


	public List<Double> getMomentCurveClean() {
		return momentCurveClean;
	}


	public void setMomentCurveClean(List<Double> momentCurveClean) {
		this.momentCurveClean = momentCurveClean;
	}


	public List<Amount<Angle>> getAlphaVectorHighLift() {
		return alphaVectorHighLift;
	}


	public void setAlphaVectorHighLift(List<Amount<Angle>> alphaVectorHighLift) {
		this.alphaVectorHighLift = alphaVectorHighLift;
	}


	public List<Double> getClVsAlphaHighLift() {
		return clVsAlphaHighLift;
	}


	public void setClVsAlphaHighLift(List<Double> clVsAlphaHighLift) {
		this.clVsAlphaHighLift = clVsAlphaHighLift;
	}


	public List<Double> getPolarHighLift() {
		return polarHighLift;
	}


	public void setPolarHighLift(List<Double> polarHighLift) {
		this.polarHighLift = polarHighLift;
	}


	public List<Double> getMomentCurveHighLift() {
		return momentCurveHighLift;
	}


	public void setMomentCurveHighLift(List<Double> momentCurveHighLift) {
		this.momentCurveHighLift = momentCurveHighLift;
	}


	public Amount<Angle> getAlphaMaxLinear() {
		return alphaMaxLinear;
	}


	public List<Double> getClZeroDistributionInput() {
		return clZeroDistributionInput;
	}


	public void setClZeroDistributionInput(List<Double> clZeroDistributionInput) {
		this.clZeroDistributionInput = clZeroDistributionInput;
	}


	public void setAlphaMaxLinear(Amount<Angle> alphaMaxLinear) {
		this.alphaMaxLinear = alphaMaxLinear;
	}


	public List<Double> getClZeroDistributionActual() {
		return clZeroDistributionActual;
	}


	public void setClZeroDistributionActual(List<Double> clZeroDistributionActual) {
		this.clZeroDistributionActual = clZeroDistributionActual;
	}


	public List<Double> getClalphaDEGDistributionInput() {
		return clalphaDEGDistributionInput;
	}


	public void setClalphaDEGDistributionInput(List<Double> clalphaDEGDistributionInput) {
		this.clalphaDEGDistributionInput = clalphaDEGDistributionInput;
	}


	public List<Double> getClalphaDEGDistributionActual() {
		return clalphaDEGDistributionActual;
	}


	public void setClalphaDEGDistributionActual(List<Double> clalphaDEGDistributionActual) {
		this.clalphaDEGDistributionActual = clalphaDEGDistributionActual;
	}


	public List<List<Double>> getTotalDragDistribution() {
		return totalDragDistribution;
	}


	public void setTotalDragDistribution(List<List<Double>> totalDragDistribution) {
		this.totalDragDistribution = totalDragDistribution;
	}


	public List<List<Double>> getParasiteDragDistribution() {
		return parasiteDragDistribution;
	}


	public void setParasiteDragDistribution(List<List<Double>> parasiteDragDistribution) {
		this.parasiteDragDistribution = parasiteDragDistribution;
	}


	public List<List<Double>> getInducedDragDistribution() {
		return inducedDragDistribution;
	}


	public void setInducedDragDistribution(List<List<Double>> inducedDragDistribution) {
		this.inducedDragDistribution = inducedDragDistribution;
	}


	public List<List<Double>> getWaweDragDistribution() {
		return waweDragDistribution;
	}


	public void setWaweDragDistribution(List<List<Double>> waweDragDistribution) {
		this.waweDragDistribution = waweDragDistribution;
	}


	public List<Double> getWawePolar() {
		return wawePolar;
	}


	public void setWawePolar(List<Double> wawePolar) {
		this.wawePolar = wawePolar;
	}


	public AirfoilTypeEnum getMeanAirfoilType() {
		return meanAirfoilType;
	}


	public void setMeanAirfoilType(AirfoilTypeEnum meanAirfoilType) {
		this.meanAirfoilType = meanAirfoilType;
	}


	public List<Double> getParasitePolar() {
		return parasitePolar;
	}


	public void setParasitePolar(List<Double> parasitePolar) {
		this.parasitePolar = parasitePolar;
	}


	public List<Double> getInducedPolar() {
		return inducedPolar;
	}


	public void setInducedPolar(List<Double> inducedPolar) {
		this.inducedPolar = inducedPolar;
	}


	public Amount<Length> getMac() {
		return mac;
	}


	public void setMac(Amount<Length> mac) {
		this.mac = mac;
	}


	public double getMomentumPole() {
		return momentumPole;
	}


	public void setMomentumPole(double momentumPole) {
		this.momentumPole = momentumPole;
	}


	public List<Amount<Angle>> getAlphaStallDistributionInput() {
		return alphaStallDistributionInput;
	}


	public void setAlphaStallDistributionInput(List<Amount<Angle>> alphaStallDistributionInput) {
		this.alphaStallDistributionInput = alphaStallDistributionInput;
	}


	public List<Amount<Angle>> getAlphaStallDistributionActual() {
		return alphaStallDistributionActual;
	}


	public void setAlphaStallDistributionActual(List<Amount<Angle>> alphaStallDistributionActual) {
		this.alphaStallDistributionActual = alphaStallDistributionActual;
	}


	public List<List<Double>> get_discretizedAirfoilsCl() {
		return _discretizedAirfoilsCl;
	}


	public void set_discretizedAirfoilsCl(List<List<Double>> _discretizedAirfoilsCl) {
		this._discretizedAirfoilsCl = _discretizedAirfoilsCl;
	}


	public List<List<Double>> get_discretizedAirfoilsCd() {
		return _discretizedAirfoilsCd;
	}


	public void set_discretizedAirfoilsCd(List<List<Double>> _discretizedAirfoilsCd) {
		this._discretizedAirfoilsCd = _discretizedAirfoilsCd;
	}


	public List<List<Double>> get_discretizedAirfoilsCm() {
		return _discretizedAirfoilsCm;
	}


	public void set_discretizedAirfoilsCm(List<List<Double>> _discretizedAirfoilsCm) {
		this._discretizedAirfoilsCm = _discretizedAirfoilsCm;
	}


	public Amount<Angle> getSweepHalfChord() {
		return sweepHalfChord;
	}


	public void setSweepHalfChord(Amount<Angle> sweepHalfChord) {
		this.sweepHalfChord = sweepHalfChord;
	}


	public List<List<Double>> getCmVsEtaVectors() {
		return cmVsEtaVectors;
	}


	public void setCmVsEtaVectors(List<List<Double>> cmVsEtaVectors) {
		this.cmVsEtaVectors = cmVsEtaVectors;
	}


	public int getFlapsNumber() {
		return flapsNumber;
	}


	public void setFlapsNumber(int flapsNumber) {
		this.flapsNumber = flapsNumber;
	}


	public List<FlapTypeEnum> getFlapType() {
		return flapType;
	}


	public void setFlapType(List<FlapTypeEnum> flapType) {
		this.flapType = flapType;
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


	public int getSlatsNumber() {
		return slatsNumber;
	}


	public void setSlatsNumber(int slatsNumber) {
		this.slatsNumber = slatsNumber;
	}


	public AerodynamicDatabaseReader getTheAerodatabaseReader() {
		return theAerodatabaseReader;
	}


	public void setTheAerodatabaseReader(AerodynamicDatabaseReader theAerodatabaseReader) {
		this.theAerodatabaseReader = theAerodatabaseReader;
	}


	public HighLiftDatabaseReader getTheHighLiftDatabaseReader() {
		return theHighLiftDatabaseReader;
	}


	public void setTheHighLiftDatabaseReader(HighLiftDatabaseReader theHighLiftDatabaseReader) {
		this.theHighLiftDatabaseReader = theHighLiftDatabaseReader;
	}


	public List<Double> getCfcInner() {
		return cfcInner;
	}


	public void setCfcInner(List<Double> cfcInner) {
		this.cfcInner = cfcInner;
	}


	public List<Double> getCfcOuter() {
		return cfcOuter;
	}


	public void setCfcOuter(List<Double> cfcOuter) {
		this.cfcOuter = cfcOuter;
	}


	public List<SymmetricFlapCreator> getSymmetricFlapCreatorList() {
		return symmetricFlapCreatorList;
	}


	public void setSymmetricFlapCreatorList(List<SymmetricFlapCreator> symmetricFlapCreatorList) {
		this.symmetricFlapCreatorList = symmetricFlapCreatorList;
	}


	public List<SlatCreator> getSlatCreatorList() {
		return slatCreatorList;
	}


	public void setSlatCreatorList(List<SlatCreator> slatCreatorList) {
		this.slatCreatorList = slatCreatorList;
	}


	public List<Double> getMaxThicknessAirfoilsDistribution() {
		return maxThicknessAirfoilsDistribution;
	}


	public void setMaxThicknessAirfoilsDistribution(List<Double> maxThicknessAirfoilsDistribution) {
		this.maxThicknessAirfoilsDistribution = maxThicknessAirfoilsDistribution;
	}


	public List<Amount<Length>> getLeRadiusDistribution() {
		return leRadiusDistribution;
	}


	public void setLeRadiusDistribution(List<Amount<Length>> leRadiusDistribution) {
		this.leRadiusDistribution = leRadiusDistribution;
	}


	public List<Double> getMaxThicknessAirfoilsDistributionActual() {
		return maxThicknessAirfoilsDistributionActual;
	}


	public void setMaxThicknessAirfoilsDistributionActual(List<Double> maxThicknessAirfoilsDistributionActual) {
		this.maxThicknessAirfoilsDistributionActual = maxThicknessAirfoilsDistributionActual;
	}


	public List<Amount<Length>> getLeRadiusDistributionActual() {
		return leRadiusDistributionActual;
	}


	public void setLeRadiusDistributionActual(List<Amount<Length>> leRadiusDistributionActual) {
		this.leRadiusDistributionActual = leRadiusDistributionActual;
	}


	public List<Amount<Length>> getyDimensionalInput() {
		return yDimensionalInput;
	}


	public void setyDimensionalInput(List<Amount<Length>> yDimensionalInput) {
		this.yDimensionalInput = yDimensionalInput;
	}


	public Amount<Angle> getSweepQuarterChord() {
		return sweepQuarterChord;
	}


	public void setSweepQuarterChord(Amount<Angle> sweepQuarterChord) {
		this.sweepQuarterChord = sweepQuarterChord;
	}


	public double getTaperRatio() {
		return taperRatio;
	}


	public void setTaperRatio(double taperRatio) {
		this.taperRatio = taperRatio;
	}


	public Double get_deltaCLmaxSlat() {
		return _deltaCLmaxSlat;
	}


	public void set_deltaCLmaxSlat(Double _deltaCLmaxSlat) {
		this._deltaCLmaxSlat = _deltaCLmaxSlat;
	}


	public Double get_deltaCD0() {
		return _deltaCD0;
	}


	public void set_deltaCD0(Double _deltaCD0) {
		this._deltaCD0 = _deltaCD0;
	}


	public Double get_deltaCMc4() {
		return _deltaCMc4;
	}


	public void set_deltaCMc4(Double _deltaCMc4) {
		this._deltaCMc4 = _deltaCMc4;
	}

	public Double get_deltaCMc4List() {
		return _deltaCMc4List;
	}


	public void set_deltaCMc4List(Double _deltaCMc4List) {
		this._deltaCMc4List = _deltaCMc4List;
	}


	public Double get_deltaCL0Flap() {
		return _deltaCL0Flap;
	}


	public void set_deltaCL0Flap(Double _deltaCL0Flap) {
		this._deltaCL0Flap = _deltaCL0Flap;
	}


	public Double get_deltaCLmaxFlap() {
		return _deltaCLmaxFlap;
	}


	public void set_deltaCLmaxFlap(Double _deltaCLmaxFlap) {
		this._deltaCLmaxFlap = _deltaCLmaxFlap;
	}
	
	public Amount<?> get_cLAlphaHighLift() {
		return _cLAlphaHighLift;
	}


	public void set_cLAlphaHighLift(Amount<?> _cLAlphaHighLift) {
		this._cLAlphaHighLift = _cLAlphaHighLift;
	}


	public Amount<Angle> getAlphaZeroLiftHL() {
		return alphaZeroLiftHL;
	}


	public void setAlphaZeroLiftHL(Amount<Angle> alphaZeroLiftHL) {
		this.alphaZeroLiftHL = alphaZeroLiftHL;
	}


	public Amount<Angle> getAlphaStarHL() {
		return alphaStarHL;
	}


	public void setAlphaStarHL(Amount<Angle> alphaStarHL) {
		this.alphaStarHL = alphaStarHL;
	}


	public Amount<Angle> getAlphaStallHL() {
		return alphaStallHL;
	}


	public void setAlphaStallHL(Amount<Angle> alphaStallHL) {
		this.alphaStallHL = alphaStallHL;
	}


	public Amount<Angle> getAlphaMaxLinearHL() {
		return alphaMaxLinearHL;
	}


	public void setAlphaMaxLinearHL(Amount<Angle> alphaMaxLinearHL) {
		this.alphaMaxLinearHL = alphaMaxLinearHL;
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


	public List<Amount<Angle>> getAlphaVectorHL() {
		return alphaVectorHL;
	}


	public void setAlphaVectorHL(List<Amount<Angle>> alphaVectorHL) {
		this.alphaVectorHL = alphaVectorHL;
	}


	public boolean isPlotFlag() {
		return plotFlag;
	}


	public void setPlotFlag(boolean plotFlag) {
		this.plotFlag = plotFlag;
	}


	public boolean isVerbosityFlag() {
		return verbosityFlag;
	}


	public void setVerbosityFlag(boolean verbosityFlag) {
		this.verbosityFlag = verbosityFlag;
	}


	public boolean isHighLiftFlag() {
		return highLiftFlag;
	}


	public void setHighLiftFlag(boolean highLiftFlag) {
		this.highLiftFlag = highLiftFlag;
	}

}
