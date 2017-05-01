package Calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.eclipse.ui.internal.keys.AlphabeticModifierKeyComparator;
import org.jscience.physics.amount.Amount;

import GUI.Main;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FlapTypeEnum;
import javaslang.Tuple;
import javaslang.Tuple2;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.MyArray;

public class InputOutputTree {
	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:

	// INPUT

	private Amount<Length> altitude;
	private double machNumber;
	
	private Amount<Area> surface;
	private double aspectRatio;
	private int numberOfPointSemispan;
	private double adimensionalKinkStation;
	private AirfoilFamilyEnum meanAirfoilFamily;
	private double  meanThickness;
	
	private int numberOfSections;
	private List<Double> yAdimensionalStationInput;
	private List<Amount<Length>> chordDistribution;
	private List<Amount<Length>> xLEDistribution;
	private List<Amount<Angle>> twistDistribution,
								alphaZeroLiftDistribution,
								alphaStarDistribution;
	private List<Double> maximumliftCoefficientDistribution;
	
	
	//--------------analyses input
	List<Amount<Angle>> alphaArrayLiftDistribution, alphaArrayLiftCurve;
	
	
	// DERIVED INPUT
	private List<Amount<Length>> yDimensionalDistributionInput;
	private List<Amount<Angle>> dihedralDistribution;
	
	//-------------distributions
	private List<Double> yAdimensionalDistributionSemiSpan;
	private List<Amount<Length>> yDimensionalDistributionSemiSpan, 
							     chordDistributionSemiSpan,
							     xLEDistributionSemiSpan;
	private List<Amount<Angle>> twistDistributionSemiSpan,
								alphaZeroLiftDistributionSemiSpan,
								alphaStarDistributionSemiSpan,
								dihedralDistributionSemiSpan;
	private List<Double> maximumliftCoefficientDistributionSemiSpan;


	//------------wing Data
	private Amount<Length> span;
	private Amount<Length> semiSpan;
	
	// OUTPUT
	List<List<Double>> clDistributionCurves;
	
	Double cLAlphaDeg, cLAlphaRad;
	Amount<Angle> alphaZeroLift, alphaStar, alphaStall, alphaMaxLinear;
	Double cLZero, cLMax, cLStall, cLStar;
	
	List<Double> liftCoefficientCurve;
	
	List<Double> clMaxAirfoils;
	List<Double> clMaxStallPath;
	
	//------------------------------------------------------------------------------------------
	// BUILDER:

	public InputOutputTree() {


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
		yDimensionalDistributionInput = new ArrayList<>();
		
		yAdimensionalDistributionSemiSpan = new ArrayList<>();
		yDimensionalDistributionSemiSpan = new ArrayList<>(); 
	    chordDistributionSemiSpan = new ArrayList<>(); 
	    xLEDistributionSemiSpan = new ArrayList<>(); 
	    twistDistributionSemiSpan = new ArrayList<>(); 
		alphaZeroLiftDistributionSemiSpan = new ArrayList<>(); 
		alphaStarDistributionSemiSpan = new ArrayList<>(); 
		dihedralDistributionSemiSpan = new ArrayList<>(); 
		maximumliftCoefficientDistributionSemiSpan = new ArrayList<>(); 
		
		machNumber = 0.0;
		aspectRatio = 0.0;
		adimensionalKinkStation = 0.0;
		meanThickness = 0.0;

		numberOfPointSemispan = 0;
		numberOfSections = 0;
		
		clDistributionCurves = new ArrayList<>();
		
		
		cLAlphaDeg = 0.0;
		cLAlphaRad = 0.0;
		alphaZeroLift = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		alphaStar = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		alphaStall = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		alphaMaxLinear = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		
		cLZero = 0.0;
		cLStar = 0.0;
		cLMax = 0.0;
		cLStall = 0.0;
		
		liftCoefficientCurve = new ArrayList<>();

	}

	public void calculateDerivedData(){
		
		
		span = Amount.valueOf(
				Math.sqrt(aspectRatio*surface.doubleValue(SI.SQUARE_METRE)),
				SI.METER);
		
		semiSpan = span.divide(2);
		
		for (int i=0; i<numberOfSections; i++){
			yDimensionalDistributionInput.add(i,
					Amount.valueOf(yAdimensionalStationInput.get(i)*span.divide(2).doubleValue(SI.METER),
							SI.METER));
		}
			
			// distributions
			
			yAdimensionalDistributionSemiSpan = Main.convertDoubleArrayToListDouble(MyArrayUtils.linspaceDouble(
					0,
					1.,
					numberOfPointSemispan
					));
			
			yDimensionalDistributionSemiSpan = MyArrayUtils.convertDoubleArrayToListOfAmount(
					MyArrayUtils.linspaceDouble(
							0.*span.divide(2.).doubleValue(SI.METER),
							1.*span.divide(2.).doubleValue(SI.METER),
							numberOfPointSemispan
							),
					SI.METER);
			
			
			xLEDistributionSemiSpan = calculateDiscretizedListAlongSemiSpanAmountLength(xLEDistribution);
			chordDistributionSemiSpan = calculateDiscretizedListAlongSemiSpanAmountLength(chordDistribution);
			twistDistributionSemiSpan = calculateDiscretizedListAlongSemiSpanAmountAngle(twistDistribution);;
			alphaStarDistributionSemiSpan = calculateDiscretizedListAlongSemiSpanAmountAngle(alphaStarDistribution);
			alphaZeroLiftDistributionSemiSpan = calculateDiscretizedListAlongSemiSpanAmountAngle(alphaZeroLiftDistribution);
			maximumliftCoefficientDistributionSemiSpan = calculateDiscretizedListAlongSemiSpanListDouble(maximumliftCoefficientDistribution);
			
			
			
			double[] dihedral = new double [numberOfPointSemispan];
			for(int i=0; i<numberOfPointSemispan; i++)
			 dihedral[i] = 0.0;
			
			dihedralDistributionSemiSpan = MyArrayUtils.convertDoubleArrayToListOfAmount(
					dihedral,
					NonSI.DEGREE_ANGLE);
			
	}

//	public void buildOutput(){
//		
//		yStationsAdimensional =  new double [numberOfPointSemispan];
//		clVsEtaVectors = new ArrayList<Double[]>();
//		alphaDistributionArray = new double [numberOfAlpha];
//	}
//	
	public Double[][] getDiscretizedTopViewAsArray() {

		Double[][] array = new Double[numberOfSections*2][2];
		for(int i=0; i<numberOfSections; i++){
			array[i][0] = yDimensionalDistributionInput.get(i).doubleValue(SI.METER);
			array[2*numberOfSections-1-i][0]= yDimensionalDistributionInput.get(i).doubleValue(SI.METER); 
			array[i][1] = xLEDistribution.get(i).doubleValue(SI.METER);
			array[2*numberOfSections-1-i][1] = xLEDistribution.get(i).doubleValue(SI.METER)+chordDistribution.get(i).doubleValue(SI.METER);
		}

		
		return array;
	}

	public List<Amount<Angle>> calculateDiscretizedListAlongSemiSpanAmountAngle (
			List<Amount<Angle>> inputList){
				
		List<Amount<Angle>> discretizedOutput = new ArrayList<>();
		
		MyArray inputArray = new MyArray(MyArrayUtils.convertListOfAmountTodoubleArray(inputList));
		
		discretizedOutput = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArray.createArray(
				inputArray.interpolate(
						MyArrayUtils.convertListOfAmountTodoubleArray(yDimensionalDistributionInput),
						MyArrayUtils.convertListOfAmountTodoubleArray(yDimensionalDistributionSemiSpan))).toArray(),
				inputList.get(0).getUnit());
		
		
		return discretizedOutput;
	}
	
	public List<Amount<Length>> calculateDiscretizedListAlongSemiSpanAmountLength (
			List<Amount<Length>> inputList){
				
		List<Amount<Length>> discretizedOutput = new ArrayList<>();
		
		MyArray inputArray = new MyArray(MyArrayUtils.convertListOfAmountTodoubleArray(inputList));
		
		discretizedOutput = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArray.createArray(
				inputArray.interpolate(
						MyArrayUtils.convertListOfAmountTodoubleArray(yDimensionalDistributionInput),
						MyArrayUtils.convertListOfAmountTodoubleArray(yDimensionalDistributionSemiSpan))).toArray(),
				inputList.get(0).getUnit());
		
		return discretizedOutput;
	}
	
	public List<Double> calculateDiscretizedListAlongSemiSpanListDouble (
			List<Double> inputList){
				
		List<Double> discretizedOutput = new ArrayList<>();
		
		double [] inputDouble = new double [numberOfSections];
		double [] outputDouble = new double [numberOfPointSemispan];
		for ( int i=0; i<numberOfSections; i++){
			inputDouble [i] = inputList.get(i);
		}
		
	MyArray inputArray = new MyArray(inputDouble);
		
		outputDouble = 
				inputArray.interpolate(
						MyArrayUtils.convertListOfAmountTodoubleArray(yDimensionalDistributionInput),
						MyArrayUtils.convertListOfAmountTodoubleArray(yDimensionalDistributionSemiSpan)).toArray();
			
		for(int i=0; i<numberOfPointSemispan; i++){
			discretizedOutput.add(outputDouble[i]);
		}
		
		return discretizedOutput;
	}
	
	public void initializeData(){
		clDistributionCurves = new ArrayList<>();
	}
	
	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	
	
	public Amount<Length> getAltitude() {
		return altitude;
	}

	public double getMachNumber() {
		return machNumber;
	}

	public Amount<Area> getSurface() {
		return surface;
	}

	public double getAspectRatio() {
		return aspectRatio;
	}

	public int getNumberOfPointSemispan() {
		return numberOfPointSemispan;
	}

	public double getAdimensionalKinkStation() {
		return adimensionalKinkStation;
	}

	public AirfoilFamilyEnum getMeanAirfoilFamily() {
		return meanAirfoilFamily;
	}

	public double getMeanThickness() {
		return meanThickness;
	}

	public int getNumberOfSections() {
		return numberOfSections;
	}

	public List<Double> getyAdimensionalStationInput() {
		return yAdimensionalStationInput;
	}

	public List<Amount<Length>> getChordDistribution() {
		return chordDistribution;
	}

	public List<Amount<Length>> getxLEDistribution() {
		return xLEDistribution;
	}

	public List<Amount<Angle>> getTwistDistribution() {
		return twistDistribution;
	}

	public List<Amount<Angle>> getAlphaZeroLiftDistribution() {
		return alphaZeroLiftDistribution;
	}

	public List<Amount<Angle>> getAlphaStarDistribution() {
		return alphaStarDistribution;
	}

	public List<Double> getMaximumliftCoefficientDistribution() {
		return maximumliftCoefficientDistribution;
	}

	public List<Amount<Angle>> getAlphaArrayLiftDistribution() {
		return alphaArrayLiftDistribution;
	}

	public List<Amount<Angle>> getAlphaArrayLiftCurve() {
		return alphaArrayLiftCurve;
	}

	public List<Amount<Length>> getyDimensionalDistributionInput() {
		return yDimensionalDistributionInput;
	}

	public List<Amount<Angle>> getDihedralDistribution() {
		return dihedralDistribution;
	}

	public List<Amount<Length>> getyDimensionalDistributionSemiSpan() {
		return yDimensionalDistributionSemiSpan;
	}

	public List<Amount<Length>> getChordDistributionSemiSpan() {
		return chordDistributionSemiSpan;
	}

	public List<Amount<Length>> getxLEDistributionSemiSpan() {
		return xLEDistributionSemiSpan;
	}

	public List<Amount<Angle>> getTwistDistributionSemiSpan() {
		return twistDistributionSemiSpan;
	}

	public List<Amount<Angle>> getAlphaZeroLiftDistributionSemiSpan() {
		return alphaZeroLiftDistributionSemiSpan;
	}

	public List<Amount<Angle>> getAlphaStarDistributionSemiSpan() {
		return alphaStarDistributionSemiSpan;
	}

	public List<Amount<Angle>> getDihedralDistributionSemiSpan() {
		return dihedralDistributionSemiSpan;
	}

	public List<Double> getMaximumliftCoefficientDistributionSemiSpan() {
		return maximumliftCoefficientDistributionSemiSpan;
	}

	public Amount<Length> getSpan() {
		return span;
	}

	public void setAltitude(Amount<Length> altitude) {
		this.altitude = altitude;
	}

	public void setMachNumber(double machNumber) {
		this.machNumber = machNumber;
	}

	public void setSurface(Amount<Area> surface) {
		this.surface = surface;
	}

	public void setAspectRatio(double aspectRatio) {
		this.aspectRatio = aspectRatio;
	}

	public void setNumberOfPointSemispan(int numberOfPointSemispan) {
		this.numberOfPointSemispan = numberOfPointSemispan;
	}

	public void setAdimensionalKinkStation(double adimensionalKinkStation) {
		this.adimensionalKinkStation = adimensionalKinkStation;
	}

	public void setMeanAirfoilFamily(AirfoilFamilyEnum meanAirfoilFamily) {
		this.meanAirfoilFamily = meanAirfoilFamily;
	}

	public void setMeanThickness(double meanThickness) {
		this.meanThickness = meanThickness;
	}

	public void setNumberOfSections(int numberOfSections) {
		this.numberOfSections = numberOfSections;
	}

	public void setyAdimensionalStationInput(List<Double> yAdimensionalStationInput) {
		this.yAdimensionalStationInput = yAdimensionalStationInput;
	}

	public void setChordDistribution(List<Amount<Length>> chordDistribution) {
		this.chordDistribution = chordDistribution;
	}

	public void setxLEDistribution(List<Amount<Length>> xLEDistribution) {
		this.xLEDistribution = xLEDistribution;
	}

	public void setTwistDistribution(List<Amount<Angle>> twistDistribution) {
		this.twistDistribution = twistDistribution;
	}

	public void setAlphaZeroLiftDistribution(List<Amount<Angle>> alphaZeroLiftDistribution) {
		this.alphaZeroLiftDistribution = alphaZeroLiftDistribution;
	}

	public void setAlphaStarDistribution(List<Amount<Angle>> alphaStarDistribution) {
		this.alphaStarDistribution = alphaStarDistribution;
	}

	public void setMaximumliftCoefficientDistribution(List<Double> maximumliftCoefficientDistribution) {
		this.maximumliftCoefficientDistribution = maximumliftCoefficientDistribution;
	}

	public void setAlphaArrayLiftDistribution(List<Amount<Angle>> alphaArrayLiftDistribution) {
		this.alphaArrayLiftDistribution = alphaArrayLiftDistribution;
	}

	public void setAlphaArrayLiftCurve(List<Amount<Angle>> alphaArrayLiftCurve) {
		this.alphaArrayLiftCurve = alphaArrayLiftCurve;
	}

	public void setyDimensionalDistributionInput(List<Amount<Length>> yDimensionalDistributionInput) {
		this.yDimensionalDistributionInput = yDimensionalDistributionInput;
	}

	public void setDihedralDistribution(List<Amount<Angle>> dihedralDistribution) {
		this.dihedralDistribution = dihedralDistribution;
	}

	public void setyDimensionalDistributionSemiSpan(List<Amount<Length>> yDimensionalDistributionSemiSpan) {
		this.yDimensionalDistributionSemiSpan = yDimensionalDistributionSemiSpan;
	}

	public void setChordDistributionSemiSpan(List<Amount<Length>> chordDistributionSemiSpan) {
		this.chordDistributionSemiSpan = chordDistributionSemiSpan;
	}

	public void setxLEDistributionSemiSpan(List<Amount<Length>> xLEDistributionSemiSpan) {
		this.xLEDistributionSemiSpan = xLEDistributionSemiSpan;
	}

	public void setTwistDistributionSemiSpan(List<Amount<Angle>> twistDistributionSemiSpan) {
		this.twistDistributionSemiSpan = twistDistributionSemiSpan;
	}

	public void setAlphaZeroLiftDistributionSemiSpan(List<Amount<Angle>> alphaZeroLiftDistributionSemiSpan) {
		this.alphaZeroLiftDistributionSemiSpan = alphaZeroLiftDistributionSemiSpan;
	}

	public void setAlphaStarDistributionSemiSpan(List<Amount<Angle>> alphaStarDistributionSemiSpan) {
		this.alphaStarDistributionSemiSpan = alphaStarDistributionSemiSpan;
	}

	public void setDihedralDistributionSemiSpan(List<Amount<Angle>> dihedralDistributionSemiSpan) {
		this.dihedralDistributionSemiSpan = dihedralDistributionSemiSpan;
	}

	public void setMaximumliftCoefficientDistributionSemiSpan(List<Double> maximumliftCoefficientDistributionSemiSpan) {
		this.maximumliftCoefficientDistributionSemiSpan = maximumliftCoefficientDistributionSemiSpan;
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

	public List<List<Double>> getClDistributionCurves() {
		return clDistributionCurves;
	}

	public void setClDistributionCurves(List<List<Double>> clDistributionCurves) {
		this.clDistributionCurves = clDistributionCurves;
	}

	public List<Double> getyAdimensionalDistributionSemiSpan() {
		return yAdimensionalDistributionSemiSpan;
	}

	public void setyAdimensionalDistributionSemiSpan(List<Double> yAdimensionalDistributionSemiSpan) {
		this.yAdimensionalDistributionSemiSpan = yAdimensionalDistributionSemiSpan;
	}

	public Double getcLAlphaDeg() {
		return cLAlphaDeg;
	}

	public Double getcLAlphaRad() {
		return cLAlphaRad;
	}

	public Amount<Angle> getAlphaZeroLift() {
		return alphaZeroLift;
	}

	public Amount<Angle> getAlphaStar() {
		return alphaStar;
	}

	public Amount<Angle> getAlphaStall() {
		return alphaStall;
	}

	public Amount<Angle> getAlphaMaxLinear() {
		return alphaMaxLinear;
	}

	public Double getcLZero() {
		return cLZero;
	}

	public Double getcLMax() {
		return cLMax;
	}

	public Double getcLStall() {
		return cLStall;
	}

	public List<Double> getLiftCoefficientCurve() {
		return liftCoefficientCurve;
	}

	public void setcLAlphaDeg(Double cLAlphaDeg) {
		this.cLAlphaDeg = cLAlphaDeg;
	}

	public void setcLAlphaRad(Double cLAlphaRad) {
		this.cLAlphaRad = cLAlphaRad;
	}

	public void setAlphaZeroLift(Amount<Angle> alphaZeroLift) {
		this.alphaZeroLift = alphaZeroLift;
	}

	public void setAlphaStar(Amount<Angle> alphaStar) {
		this.alphaStar = alphaStar;
	}

	public void setAlphaStall(Amount<Angle> alphaStall) {
		this.alphaStall = alphaStall;
	}

	public void setAlphaMaxLinear(Amount<Angle> alphaMaxLinear) {
		this.alphaMaxLinear = alphaMaxLinear;
	}

	public void setcLZero(Double cLZero) {
		this.cLZero = cLZero;
	}

	public void setcLMax(Double cLMax) {
		this.cLMax = cLMax;
	}

	public void setcLStall(Double cLStall) {
		this.cLStall = cLStall;
	}

	public void setLiftCoefficientCurve(List<Double> liftCoefficientCurve) {
		this.liftCoefficientCurve = liftCoefficientCurve;
	}

	public Double getcLStar() {
		return cLStar;
	}

	public void setcLStar(Double cLStar) {
		this.cLStar = cLStar;
	}

	public List<Double> getClMaxAirfoils() {
		return clMaxAirfoils;
	}

	public List<Double> getClMaxStallPath() {
		return clMaxStallPath;
	}

	public void setClMaxAirfoils(List<Double> clMaxAirfoils) {
		this.clMaxAirfoils = clMaxAirfoils;
	}

	public void setClMaxStallPath(List<Double> clMaxStallPath) {
		this.clMaxStallPath = clMaxStallPath;
	}
	




}
