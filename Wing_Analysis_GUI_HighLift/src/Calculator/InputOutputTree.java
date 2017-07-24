package Calculator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
import graphics.D3Plotter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javaslang.Tuple;
import javaslang.Tuple2;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.MyArray;

public class InputOutputTree {
	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:

	// INPUT

	@FXML 
	Button saveButton;
	
	private Amount<Length> altitude;
	private double machNumber;
	
	private Amount<Area> surface;
	private double aspectRatio;
	private int numberOfPointSemispan;
	private double adimensionalKinkStation;
	private AirfoilFamilyEnum meanAirfoilFamily;
	private double  meanThickness;
	
	private int numberOfSections;
	private List<Double> yAdimensionalStationInput,
							thicknessDistribution,
							cLZeroDistribution;
	private List<Amount<Length>> chordDistribution;
	private List<Amount<Length>> xLEDistribution,
								leRadiusDistribution;
	private List<Amount<Angle>> twistDistribution,
								alphaZeroLiftDistribution,
								alphaStarDistribution;
	private List<Amount<?>> clAlphaDistribution;
	private List<Double> maximumliftCoefficientDistribution;
	
	
	//--------------analyses input
	List<Amount<Angle>> alphaArrayLiftDistribution, alphaArrayLiftCurve;
	List<Amount<Angle>> alphaArrayHighLiftDistribution, alphaArrayHighLiftCurve;
	
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
	private List<Double> maximumliftCoefficientDistributionSemiSpan,
	clZeroDistributionSemispan, 
	clAlphaDegDistributionSemiSpan;



	//------------wing Data
	private Amount<Length> span;
	private Amount<Length> semiSpan;
	
	//------------equivalentingData
	private Amount<Length> rootChordEquivalentWing;
	private Double taperRatioEquivalentWing;
	private Amount<Angle> sweepQuarterChordEquivalent;
	
	// OUTPUT
	List<List<Double>> clDistributionCurves;
	
	Double cLAlphaDeg, cLAlphaRad;
	Amount<?> cLAlpha;
	Amount<Angle> alphaZeroLift, alphaStar, alphaStall, alphaMaxLinear;
	Double cLZero, cLMax, cLStall, cLStar;
	
	List<Double> liftCoefficientCurve;
	
	List<Double> clMaxAirfoils;
	List<Double> clMaxStallPath;
	
	boolean outputTreeIsEmpty;
	
	D3Plotter d3Plotter;
	File inputFile;
	
	// analyses to perform
	
	Boolean performLoadAnalysis;
	Boolean performLiftAnalysis;
	Boolean performStallPathAnalysis;
	Boolean performHighLiftAnalysis;
	
	// flap and slat
	
	int numberOfFlaps;
	int numberOfSlats;
	List<FlapTypeEnum> flapTypes = new ArrayList<>();
	List<Amount<Angle>> flapDeflection = new ArrayList<>();
	List<Double> flapChordRatio = new ArrayList<>();
	List<Double> flapInnerStation = new ArrayList<>();
	List<Double> flapOuterStation = new ArrayList<>();
	
	List<Amount<Angle>> slatDeflection = new ArrayList<>();
	List<Double> slatChordRatio = new ArrayList<>();
	List<Double> slatExtensionRatio = new ArrayList<>();
	List<Double> slatInnerStation = new ArrayList<>();
	List<Double> slatOuterStation = new ArrayList<>();
	
	boolean highLiftInputTreeIsFilled = false;
	
	
	//high lift
	Double cLZeroHighLift, cLMaxHighLift, clStarHighLift;
	Amount<Angle> alphaZeroLiftHighLift, alphaStallHighLift, alphaStarHighLift;
	Double deltaCL0Flap, deltaCLMaxFlap, deltaCLMaxSlat;
	Amount<?> clAlphaFlap;
	
	List<Double> liftCoefficient3DCurveHighLift;
	
	List<List<Double>> clDistributionCurvesHighLift = new ArrayList<>();
	
	List<Double> flapTypeIndex = new ArrayList<>();
	List<Double> deltaFlapRef = new ArrayList<>();
	
	
	
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
		outputTreeIsEmpty = true;
		
		performLoadAnalysis = false;
		performLiftAnalysis= false;
		performStallPathAnalysis = false;
		performHighLiftAnalysis = false;
		highLiftInputTreeIsFilled = false;
	}

	public void cleanCleanConfigurationData() {
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
		outputTreeIsEmpty = true;
		
		performLoadAnalysis = false;
		performLiftAnalysis= false;
		performStallPathAnalysis = false;
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
			
			List<Double> clAlphaDeg = new ArrayList<>();
			for (int i =0; i<numberOfSections; i++) {
				clAlphaDeg.add(clAlphaDistribution.get(i).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue());
			}
			xLEDistributionSemiSpan = calculateDiscretizedListAlongSemiSpanAmountLength(xLEDistribution);
			chordDistributionSemiSpan = calculateDiscretizedListAlongSemiSpanAmountLength(chordDistribution);
			twistDistributionSemiSpan = calculateDiscretizedListAlongSemiSpanAmountAngle(twistDistribution);;
			alphaStarDistributionSemiSpan = calculateDiscretizedListAlongSemiSpanAmountAngle(alphaStarDistribution);
			alphaZeroLiftDistributionSemiSpan = calculateDiscretizedListAlongSemiSpanAmountAngle(alphaZeroLiftDistribution);
			maximumliftCoefficientDistributionSemiSpan = calculateDiscretizedListAlongSemiSpanListDouble(maximumliftCoefficientDistribution);
			clZeroDistributionSemispan = calculateDiscretizedListAlongSemiSpanListDouble(cLZeroDistribution);
			clAlphaDegDistributionSemiSpan = calculateDiscretizedListAlongSemiSpanListDouble(clAlphaDeg);
			
			double[] dihedral = new double [numberOfPointSemispan];
			for(int i=0; i<numberOfPointSemispan; i++)
			 dihedral[i] = 0.0;
			
			dihedralDistributionSemiSpan = MyArrayUtils.convertDoubleArrayToListOfAmount(
					dihedral,
					NonSI.DEGREE_ANGLE);
			
			// equivalent wing
			
			Double integral1 = MyMathUtils.integrate1DSimpsonSpline(
					MyArrayUtils.convertListOfAmountTodoubleArray(
						this.yDimensionalDistributionInput), // y
					MyArrayUtils.convertListOfAmountTodoubleArray(
						this.xLEDistribution) // xle(y)
				);
			int nSec = this.xLEDistribution.size();
			Double xleAtTip = this.xLEDistribution.get(nSec - 1).doubleValue(SI.METER);
			Double area1 = (semiSpan.doubleValue(SI.METER) * xleAtTip) - integral1;
			
			Double xOffsetEquivalentWingRootLE = xleAtTip - (area1 / (0.5*semiSpan.doubleValue(SI.METER)));
			
			//======================================================
			// integral_2 = ((b/2)*(xte(0)-xte(b/2)) - [ (xte(0)*b/2) - (int_0^(b/2) [xle(y) + c(y)] dy)]
			// _xOffsetEquivalentWingRootTE = chord_root - xte(b/2) - (A_2/((b/2*)(1/2)))

			Tuple2<
				List<Amount<Length>>, // Xle
				List<Amount<Length>>  // c
				> xlePlusCTuple = Tuple.of(this.xLEDistribution, this.chordDistribution);

			List<Double> xlePlusC = IntStream.range(0, this.yDimensionalDistributionInput.size())
					.mapToObj(i -> 
						xlePlusCTuple._1.get(i).doubleValue(SI.METRE)
						+ xlePlusCTuple._2.get(i).doubleValue(SI.METRE)) // xle + c
					.collect(Collectors.toList());

			Double integral2 = MyMathUtils.integrate1DSimpsonSpline(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							this.yDimensionalDistributionInput), // y
					MyArrayUtils.convertToDoublePrimitive(xlePlusC) // xle + c
					);
			Double xteAtRoot = chordDistribution.get(0).doubleValue(SI.METER);
			int nPan = this.numberOfSections-1;
			Double xteAtTip = xleAtTip + chordDistribution.get(numberOfSections-1).doubleValue(SI.METER);
			
			Double area2a = (xteAtRoot*semiSpan.doubleValue(SI.METER)) - integral2;
			Double area2 = semiSpan.doubleValue(SI.METER) * ( xteAtRoot - xteAtTip ) - area2a;
			
			Double xOffsetEquivalentWingRootTE = 
					chordDistribution.get(0).doubleValue(SI.METER)
					- xteAtTip - ((area2 / (0.5*semiSpan.doubleValue(SI.METER))));
			
			rootChordEquivalentWing = Amount.valueOf(
					chordDistribution.get(0).doubleValue(SI.METER)
					- xOffsetEquivalentWingRootLE 
					- xOffsetEquivalentWingRootTE,
					SI.METER
					);
			
			// taper ratio equivalent wing
			
			taperRatioEquivalentWing = chordDistribution.get(numberOfSections-1).doubleValue(SI.METER)/rootChordEquivalentWing.doubleValue(SI.METER);
			
			// sweep quarter chord
			
			sweepQuarterChordEquivalent = Amount.valueOf(
					Math.atan(
							(xLEDistribution.get(numberOfSections-1).doubleValue(SI.METER)+
							(chordDistribution.get(numberOfSections-1).doubleValue(SI.METER)/4) -
							(xOffsetEquivalentWingRootLE + chordDistribution.get(0).doubleValue(SI.METER)/4)
							)/ semiSpan.doubleValue(SI.METER)
							), 
					SI.RADIAN
					);
			
			
			if (flapTypes.get(0) != null) {
				
				for(int i=0; i<flapChordRatio.size(); i++) {
					if(flapTypes.get(i) == FlapTypeEnum.SINGLE_SLOTTED) {
						flapTypeIndex.add(1.0);
						deltaFlapRef.add(45.0);
					}
					else if(flapTypes.get(i) == FlapTypeEnum.DOUBLE_SLOTTED) {
						flapTypeIndex.add(2.0);
						deltaFlapRef.add(50.0);
					}
					else if(flapTypes.get(i) == FlapTypeEnum.PLAIN) {
						flapTypeIndex.add(3.0);
						deltaFlapRef.add(60.0);
					}
					else if(flapTypes.get(i) == FlapTypeEnum.FOWLER) {
						flapTypeIndex.add(4.0);
						deltaFlapRef.add(40.0);
					}
					else if(flapTypes.get(i) == FlapTypeEnum.TRIPLE_SLOTTED) {
						flapTypeIndex.add(5.0);
						deltaFlapRef.add(50.0);
					}
					else if(flapTypes.get(i) == FlapTypeEnum.OPTIMIZED_FOWLER) {
						flapTypeIndex.add(6.0);
						deltaFlapRef.add(40.0);
					}
				}
			}
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
//		if (highLiftInputTreeIsFilled) {
//			array[array.length-1][0] = yDimensionalDistributionInput.get(0).doubleValue(SI.METER);
//			array[array.length-1][1] =  xLEDistribution.get(0).doubleValue(SI.METER);
//		}

		
		return array;
	}
	
	public Double[][] getFlapsAsArray(
			int flapNumber) {

		Double[][] array = new Double[5][2];
			array[0][0] = flapInnerStation.get(flapNumber)*semiSpan.doubleValue(SI.METER);
			array[1][0] = flapInnerStation.get(flapNumber)*semiSpan.doubleValue(SI.METER);
			array[2][0] = flapOuterStation.get(flapNumber)*semiSpan.doubleValue(SI.METER);
			array[3][0] = flapOuterStation.get(flapNumber)*semiSpan.doubleValue(SI.METER);
			array[4][0] = array[0][0];
			array[0][1] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalDistributionSemiSpan), 
					MyArrayUtils.convertListOfAmountTodoubleArray(xLEDistributionSemiSpan),  
					flapInnerStation.get(flapNumber)
					) +
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(yAdimensionalDistributionSemiSpan), 
							MyArrayUtils.convertListOfAmountTodoubleArray(chordDistributionSemiSpan),  
							flapInnerStation.get(flapNumber)
							);
			array[1][1] = array[0][1] - flapChordRatio.get(flapNumber)*MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalDistributionSemiSpan), 
					MyArrayUtils.convertListOfAmountTodoubleArray(chordDistributionSemiSpan),  
					flapInnerStation.get(flapNumber)
					);
			array[2][1] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalDistributionSemiSpan), 
					MyArrayUtils.convertListOfAmountTodoubleArray(xLEDistributionSemiSpan),  
					flapOuterStation.get(flapNumber)
					) +
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(yAdimensionalDistributionSemiSpan), 
							MyArrayUtils.convertListOfAmountTodoubleArray(chordDistributionSemiSpan),  
							flapOuterStation.get(flapNumber)
							) -
					flapChordRatio.get(flapNumber)*MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(yAdimensionalDistributionSemiSpan), 
							MyArrayUtils.convertListOfAmountTodoubleArray(chordDistributionSemiSpan),  
							flapOuterStation.get(flapNumber)
							);
			array[3][1] =  MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalDistributionSemiSpan), 
					MyArrayUtils.convertListOfAmountTodoubleArray(xLEDistributionSemiSpan),  
					flapOuterStation.get(flapNumber)
					) +
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(yAdimensionalDistributionSemiSpan), 
							MyArrayUtils.convertListOfAmountTodoubleArray(chordDistributionSemiSpan),  
							flapOuterStation.get(flapNumber)
							);
			array[4][1] = array[0][1];
	
			
//		 System.out.println(" flap array " );
//		 System.out.println(array[0][0] + " " +  array[0][1]);
//		 System.out.println(array[1][0]  + " " +  array[1][1]);
//		 System.out.println(array[2][0]  + " " +  array[2][1]);
//		 System.out.println(array[3][0]  + " " +  array[3][1]);
		return array;
	}

	public Double[][] getSlatsAsArray(
			int slatNumber) {

		Double[][] array = new Double[5][2];
			array[0][0] = slatInnerStation.get(slatNumber)*semiSpan.doubleValue(SI.METER);
			array[1][0] = slatInnerStation.get(slatNumber)*semiSpan.doubleValue(SI.METER);
			array[2][0] = slatOuterStation.get(slatNumber)*semiSpan.doubleValue(SI.METER);
			array[3][0] = slatOuterStation.get(slatNumber)*semiSpan.doubleValue(SI.METER);
			array[4][0] = array[0][0];
			array[0][1] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalDistributionSemiSpan), 
					MyArrayUtils.convertListOfAmountTodoubleArray(xLEDistributionSemiSpan),  
					slatInnerStation.get(slatNumber)
					);
			array[1][1] = array[0][1] + slatChordRatio.get(slatNumber)*MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalDistributionSemiSpan), 
					MyArrayUtils.convertListOfAmountTodoubleArray(chordDistributionSemiSpan),  
					slatInnerStation.get(slatNumber)
					);
			array[2][1] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalDistributionSemiSpan), 
					MyArrayUtils.convertListOfAmountTodoubleArray(xLEDistributionSemiSpan),  
					slatOuterStation.get(slatNumber)
					) + slatChordRatio.get(slatNumber)*MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(yAdimensionalDistributionSemiSpan), 
							MyArrayUtils.convertListOfAmountTodoubleArray(chordDistributionSemiSpan),  
							slatOuterStation.get(slatNumber)
							) ;
			array[3][1] =  MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(yAdimensionalDistributionSemiSpan), 
					MyArrayUtils.convertListOfAmountTodoubleArray(xLEDistributionSemiSpan),  
					slatOuterStation.get(slatNumber)
					) ;
			array[4][1] = array[0][1];
			
//		 System.out.println(" slat array " );
//		 System.out.println(array[0][0] + " " +  array[0][1]);
//		 System.out.println(array[1][0]  + " " +  array[1][1]);
//		 System.out.println(array[2][0]  + " " +  array[2][1]);
//		 System.out.println(array[3][0]  + " " +  array[3][1]);
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
	
	// if true the outputTreeIdEmpty
	public boolean outputTreeEmpty(){
		
		return outputTreeIsEmpty;
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
		outputTreeIsEmpty = false;
	}

	public void setcLAlphaRad(Double cLAlphaRad) {
		this.cLAlphaRad = cLAlphaRad;
		outputTreeIsEmpty = false;
	}

	public void setAlphaZeroLift(Amount<Angle> alphaZeroLift) {
		this.alphaZeroLift = alphaZeroLift;
		outputTreeIsEmpty = false;
	}

	public void setAlphaStar(Amount<Angle> alphaStar) {
		this.alphaStar = alphaStar;
		outputTreeIsEmpty = false;
	}

	public void setAlphaStall(Amount<Angle> alphaStall) {
		this.alphaStall = alphaStall;
		outputTreeIsEmpty = false;
	}

	public void setAlphaMaxLinear(Amount<Angle> alphaMaxLinear) {
		this.alphaMaxLinear = alphaMaxLinear;
		outputTreeIsEmpty = false;
	}

	public void setcLZero(Double cLZero) {
		this.cLZero = cLZero;
		outputTreeIsEmpty = false;
	}

	public void setcLMax(Double cLMax) {
		this.cLMax = cLMax;
		outputTreeIsEmpty = false;
	}

	public void setcLStall(Double cLStall) {
		this.cLStall = cLStall;
		outputTreeIsEmpty = false;
	}

	public void setLiftCoefficientCurve(List<Double> liftCoefficientCurve) {
		this.liftCoefficientCurve = liftCoefficientCurve;
		outputTreeIsEmpty = false;
	}

	public Double getcLStar() {
		return cLStar;
	}

	public void setcLStar(Double cLStar) {
		this.cLStar = cLStar;
		outputTreeIsEmpty = false;
	}

	public List<Double> getClMaxAirfoils() {
		return clMaxAirfoils;
		
	}

	public List<Double> getClMaxStallPath() {
		return clMaxStallPath;
	}

	public void setClMaxAirfoils(List<Double> clMaxAirfoils) {
		this.clMaxAirfoils = clMaxAirfoils;
		outputTreeIsEmpty = false;
	}

	public void setClMaxStallPath(List<Double> clMaxStallPath) {
		this.clMaxStallPath = clMaxStallPath;
		outputTreeIsEmpty = false;
	}

	public boolean isInputTreeIsEmpty() {
		return outputTreeIsEmpty;
	}

	public void setInputTreeIsEmpty(boolean inputTreeIsEmpty) {
		this.outputTreeIsEmpty = inputTreeIsEmpty;
	}

	public Button getSaveButton() {
		return saveButton;
	}

	public void setSaveButton(Button saveButton) {
		this.saveButton = saveButton;
	}

	public D3Plotter getD3Plotter() {
		return d3Plotter;
	}

	public void setD3Plotter(D3Plotter d3Plotter) {
		this.d3Plotter = d3Plotter;
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public Boolean getPerformStallPathAnalysis() {
		return performStallPathAnalysis;
	}

	public void setPerformStallPathAnalysis(Boolean performStallPathAnalysis) {
		this.performStallPathAnalysis = performStallPathAnalysis;
	}

	public Boolean getPerformLoadAnalysis() {
		return performLoadAnalysis;
	}

	public Boolean getPerformLiftAnalysis() {
		return performLiftAnalysis;
	}

	public void setPerformLoadAnalysis(Boolean performLoadAnalysis) {
		this.performLoadAnalysis = performLoadAnalysis;
	}

	public void setPerformLiftAnalysis(Boolean performLiftAnalysis) {
		this.performLiftAnalysis = performLiftAnalysis;
	}

	public boolean isOutputTreeIsEmpty() {
		return outputTreeIsEmpty;
	}

	public void setOutputTreeIsEmpty(boolean outputTreeIsEmpty) {
		this.outputTreeIsEmpty = outputTreeIsEmpty;
	}

	public int getNumberOfFlaps() {
		return numberOfFlaps;
	}

	public void setNumberOfFlaps(int numberOfFlaps) {
		this.numberOfFlaps = numberOfFlaps;
	}

	public int getNumberOfSlats() {
		return numberOfSlats;
	}

	public void setNumberOfSlats(int numberOfSlats) {
		this.numberOfSlats = numberOfSlats;
	}

	public List<FlapTypeEnum> getFlapTypes() {
		return flapTypes;
	}

	public void setFlapTypes(List<FlapTypeEnum> flapTypes) {
		this.flapTypes = flapTypes;
	}

	public List<Amount<Angle>> getFlapDeflection() {
		return flapDeflection;
	}

	public void setFlapDeflection(List<Amount<Angle>> flapDeflection) {
		this.flapDeflection = flapDeflection;
	}

	public List<Double> getFlapChordRatio() {
		return flapChordRatio;
	}

	public void setFlapChordRatio(List<Double> flapChordRatio) {
		this.flapChordRatio = flapChordRatio;
	}

	public List<Double> getFlapInnerStation() {
		return flapInnerStation;
	}

	public void setFlapInnerStation(List<Double> flapInnerStation) {
		this.flapInnerStation = flapInnerStation;
	}

	public List<Double> getFlapOuterStation() {
		return flapOuterStation;
	}

	public void setFlapOuterStation(List<Double> flapOuterStation) {
		this.flapOuterStation = flapOuterStation;
	}

	public List<Amount<Angle>> getSlatDeflection() {
		return slatDeflection;
	}

	public void setSlatDeflection(List<Amount<Angle>> slatDeflection) {
		this.slatDeflection = slatDeflection;
	}

	public List<Double> getSlatChordRatio() {
		return slatChordRatio;
	}

	public void setSlatChordRatio(List<Double> slatChordRatio) {
		this.slatChordRatio = slatChordRatio;
	}

	public List<Double> getSlatExtensionRatio() {
		return slatExtensionRatio;
	}

	public void setSlatExtensionRatio(List<Double> slatExtensionRatio) {
		this.slatExtensionRatio = slatExtensionRatio;
	}

	public List<Double> getSlatInnerStation() {
		return slatInnerStation;
	}

	public void setSlatInnerStation(List<Double> slatInnerStation) {
		this.slatInnerStation = slatInnerStation;
	}

	public List<Double> getSlatOuterStation() {
		return slatOuterStation;
	}

	public void setSlatOuterStation(List<Double> slatOuterStation) {
		this.slatOuterStation = slatOuterStation;
	}

	public boolean isHighLiftInputTreeIsFilled() {
		return highLiftInputTreeIsFilled;
	}

	public void setHighLiftInputTreeIsFilled(boolean highLiftInputTreeIsFilled) {
		this.highLiftInputTreeIsFilled = highLiftInputTreeIsFilled;
	}

	public Boolean getPerformHighLiftAnalysis() {
		return performHighLiftAnalysis;
	}

	public void setPerformHighLiftAnalysis(Boolean performHighLiftAnalysis) {
		this.performHighLiftAnalysis = performHighLiftAnalysis;
	}

	public List<Amount<Angle>> getAlphaArrayHighLiftDistribution() {
		return alphaArrayHighLiftDistribution;
	}

	public List<Amount<Angle>> getAlphaArrayHighLiftCurve() {
		return alphaArrayHighLiftCurve;
	}

	public void setAlphaArrayHighLiftDistribution(List<Amount<Angle>> alphaArrayHighLiftDistribution) {
		this.alphaArrayHighLiftDistribution = alphaArrayHighLiftDistribution;
	}

	public void setAlphaArrayHighLiftCurve(List<Amount<Angle>> alphaArrayHighLiftCurve) {
		this.alphaArrayHighLiftCurve = alphaArrayHighLiftCurve;
	}

	public Amount<?> getcLAlpha() {
		return cLAlpha;
	}

	public void setcLAlpha(Amount<?> cLAlpha) {
		this.cLAlpha = cLAlpha;
	}

	public Amount<Length> getRootChordEquivalentWing() {
		return rootChordEquivalentWing;
	}

	public void setRootChordEquivalentWing(Amount<Length> rootChordEquivalentWing) {
		this.rootChordEquivalentWing = rootChordEquivalentWing;
	}

	public Double getTaperRatioEquivalentWing() {
		return taperRatioEquivalentWing;
	}

	public Amount<Angle> getSweepQuarterChordEquivalent() {
		return sweepQuarterChordEquivalent;
	}

	public void setTaperRatioEquivalentWing(Double taperRatioEquivalentWing) {
		this.taperRatioEquivalentWing = taperRatioEquivalentWing;
	}

	public void setSweepQuarterChordEquivalent(Amount<Angle> sweepQuarterChordEquivalent) {
		this.sweepQuarterChordEquivalent = sweepQuarterChordEquivalent;
	}

	public List<Double> getThicknessDistribution() {
		return thicknessDistribution;
	}

	public void setThicknessDistribution(List<Double> thicknessDistribution) {
		this.thicknessDistribution = thicknessDistribution;
	}

	public List<Double> getcLZeroDistribution() {
		return cLZeroDistribution;
	}

	public List<Amount<Length>> getLeRadiusDistribution() {
		return leRadiusDistribution;
	}

	public void setcLZeroDistribution(List<Double> cLZeroDistribution) {
		this.cLZeroDistribution = cLZeroDistribution;
	}

	public void setLeRadiusDistribution(List<Amount<Length>> leRadiusDistribution) {
		this.leRadiusDistribution = leRadiusDistribution;
	}

	public List<Amount<?>> getClAlphaDistribution() {
		return clAlphaDistribution;
	}

	public void setClAlphaDistribution(List<Amount<?>> clAlphaDistribution) {
		this.clAlphaDistribution = clAlphaDistribution;
	}

	public Double getcLZeroHighLift() {
		return cLZeroHighLift;
	}

	public Double getcLMaxHighLift() {
		return cLMaxHighLift;
	}

	public Double getClStarHighLift() {
		return clStarHighLift;
	}

	public Amount<Angle> getAlphaZeroLiftHighLift() {
		return alphaZeroLiftHighLift;
	}

	public Amount<Angle> getAlphaStallHighLift() {
		return alphaStallHighLift;
	}

	public Amount<Angle> getAlphaStarHighLift() {
		return alphaStarHighLift;
	}

	public void setcLZeroHighLift(Double cLZeroHighLift) {
		this.cLZeroHighLift = cLZeroHighLift;
	}

	public void setcLMaxHighLift(Double cLMaxHighLift) {
		this.cLMaxHighLift = cLMaxHighLift;
	}

	public void setClStarHighLift(Double clStarHighLift) {
		this.clStarHighLift = clStarHighLift;
	}

	public void setAlphaZeroLiftHighLift(Amount<Angle> alphaZeroLiftHighLift) {
		this.alphaZeroLiftHighLift = alphaZeroLiftHighLift;
	}

	public void setAlphaStallHighLift(Amount<Angle> alphaStallHighLift) {
		this.alphaStallHighLift = alphaStallHighLift;
	}

	public void setAlphaStarHighLift(Amount<Angle> alphaStarHighLift) {
		this.alphaStarHighLift = alphaStarHighLift;
	}

	public List<Double> getLiftCoefficient3DCurveHighLift() {
		return liftCoefficient3DCurveHighLift;
	}

	public void setLiftCoefficient3DCurveHighLift(List<Double> liftCoefficient3DCurveHighLift) {
		this.liftCoefficient3DCurveHighLift = liftCoefficient3DCurveHighLift;
	}

	public Double getDeltaCL0Flap() {
		return deltaCL0Flap;
	}

	public Double getDeltaCLMaxFlap() {
		return deltaCLMaxFlap;
	}

	public Double getDeltaCLMaxSlat() {
		return deltaCLMaxSlat;
	}

	public void setDeltaCL0Flap(Double deltaCL0Flap) {
		this.deltaCL0Flap = deltaCL0Flap;
	}

	public void setDeltaCLMaxFlap(Double deltaCLMaxFlap) {
		this.deltaCLMaxFlap = deltaCLMaxFlap;
	}

	public void setDeltaCLMaxSlat(Double deltaCLMaxSlat) {
		this.deltaCLMaxSlat = deltaCLMaxSlat;
	}

	public Amount<?> getClAlphaFlap() {
		return clAlphaFlap;
	}

	public void setClAlphaFlap(Amount<?> clAlphaFlap) {
		this.clAlphaFlap = clAlphaFlap;
	}

	public List<Double> getFlapTypeIndex() {
		return flapTypeIndex;
	}

	public void setFlapTypeIndex(List<Double> flapTypeIndex) {
		this.flapTypeIndex = flapTypeIndex;
	}

	public List<Double> getClZeroDistributionSemispan() {
		return clZeroDistributionSemispan;
	}

	public void setClZeroDistributionSemispan(List<Double> clZeroDistributionSemispan) {
		this.clZeroDistributionSemispan = clZeroDistributionSemispan;
	}

	public List<Double> getClAlphaDegDistributionSemiSpan() {
		return clAlphaDegDistributionSemiSpan;
	}

	public void setClAlphaDegDistributionSemiSpan(List<Double> clAlphaDegDistributionSemiSpan) {
		this.clAlphaDegDistributionSemiSpan = clAlphaDegDistributionSemiSpan;
	}

	public List<List<Double>> getClDistributionCurvesHighLift() {
		return clDistributionCurvesHighLift;
	}

	public void setClDistributionCurvesHighLift(List<List<Double>> clDistributionCurvesHighLift) {
		this.clDistributionCurvesHighLift = clDistributionCurvesHighLift;
	}


}
