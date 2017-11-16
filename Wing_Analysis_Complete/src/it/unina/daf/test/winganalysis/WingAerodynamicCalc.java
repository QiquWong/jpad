package it.unina.daf.test.winganalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.AirfoilCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.MomentCalc;
import calculators.aerodynamics.NasaBlackwell;
import configuration.MyConfiguration;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;


public class WingAerodynamicCalc {
	
	public static void calculateAll(InputOutputTree input) throws InstantiationException, IllegalAccessException{

		input.setSpan(Amount.valueOf(Math.sqrt(input.getAspectRatio()*input.getSurface().doubleValue(SI.SQUARE_METRE)), SI.METER));
		input.setSemiSpan(input.getSpan().divide(2));
		
		// DISTRIBUTIONS
		input.setyAdimensionalStationActual(MyArrayUtils.convertDoubleArrayToListDouble(
				MyArrayUtils.convertFromDoubleToPrimitive(MyArrayUtils.linspace(0., 1., input.getNumberOfPointSemispan()))));
		input.setyDimensionaStationActual(MyArrayUtils.convertDoubleArrayToListOfAmount(MyArrayUtils.linspace(
						0.*input.getSemiSpan().doubleValue(SI.METER), 
						1.*input.getSemiSpan().doubleValue(SI.METER), 
						input.getNumberOfPointSemispan()),SI.METER));
		
		input.setChordDistributionActual(MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationInput())), 
						        MyArrayUtils.convertListOfAmountTodoubleArray(input.getChordDistributionInput()),
						        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()))
						),
				SI.METER));
		
		input.setxLEDistributionActual(MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationInput())), 
				        MyArrayUtils.convertListOfAmountTodoubleArray(input.getxLEDistributionInput()),
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()))
				),
		SI.METER));
		
		input.setTwistDistributionActual(MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationInput())), 
				        MyArrayUtils.convertListOfAmountTodoubleArray(input.getTwistDistributionInput()),
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()))
				),
		NonSI.DEGREE_ANGLE));
		
		input.setAlphaStarDistributionActual(MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationInput())), 
				        MyArrayUtils.convertListOfAmountTodoubleArray(input.getAlphaStarDistributionInput()),
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()))
				),
				NonSI.DEGREE_ANGLE));
		
		input.setAlphaStarDistributionActual(MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationInput())), 
				        MyArrayUtils.convertListOfAmountTodoubleArray(input.getAlphaStallDistributionInput()),
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()))
				),
				NonSI.DEGREE_ANGLE));
		
		input.setAlphaZeroLiftDistributionActual(MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationInput())), 
				        MyArrayUtils.convertListOfAmountTodoubleArray(input.getAlphaZeroLiftDistributionInput()),
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()))
				),
				NonSI.DEGREE_ANGLE));
		
		input.setAlphaStallDistributionActual(MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationInput())), 
				        MyArrayUtils.convertListOfAmountTodoubleArray(input.getAlphaStallDistributionInput()),
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()))
				),
				NonSI.DEGREE_ANGLE));
		
		input.setMaximumliftCoefficientDistributionActual(MyArrayUtils.convertDoubleArrayToListDouble(
						MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationInput())), 
						        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getMaximumliftCoefficientDistributionInput())),
						        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()))
						)));
		
		input.setClZeroDistributionActual(MyArrayUtils.convertDoubleArrayToListDouble(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationInput())), 
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getClZeroDistributionInput())),
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()))
				)));
		
		input.setClalphaDEGDistributionActual(MyArrayUtils.convertDoubleArrayToListDouble(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationInput())), 
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getClalphaDEGDistributionInput())),
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()))
				)));
		
		input.setCdMinDistributionActual(MyArrayUtils.convertDoubleArrayToListDouble(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationInput())), 
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getCdMinDistributionInput())),
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()))
				)));
		
		input.setClIdealDistributionActual(MyArrayUtils.convertDoubleArrayToListDouble(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationInput())), 
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getClIdealDistributionInput())),
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()))
				)));
		
		input.setkDistributionActual(MyArrayUtils.convertDoubleArrayToListDouble(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationInput())), 
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getkDistributionInput())),
				        MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()))
				)));
		
		input.setCmc4DistributionActual(MyArrayUtils.convertDoubleArrayToListDouble(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationInput())), 
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getCmc4DistributionInput())),
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()))
						)));

		//

		Double mac = MyMathUtils.integrate1DSimpsonSpline(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						input.getyDimensionaStationActual()), // y
				MyArrayUtils.convertListOfAmountTodoubleArray(
						input.getChordDistributionActual().stream()
						.map(c -> c.pow(2))
						.collect(Collectors.toList())
						) // c^2
				);
		mac = 2.0 * mac / input.getSurface().to(SI.SQUARE_METRE).getEstimatedValue();

		input.setMac(Amount.valueOf(mac, SI.METER));


List<Double> xleTimeC = new ArrayList<>();
for (int i=0; i<input.getNumberOfPointSemispan(); i++) {
		xleTimeC.add(input.getxLEDistributionActual().get(i).doubleValue(SI.METER) * input.getChordDistributionActual().get(i).doubleValue(SI.METER));
}
		Double xle = MyMathUtils.integrate1DSimpsonSpline(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						input.getyDimensionaStationActual()), // y
				MyArrayUtils.convertToDoublePrimitive(xleTimeC) // xle * c
				);

			xle = 2.0 * xle / input.getSurface().to(SI.SQUARE_METRE).getEstimatedValue();
			Amount<Length> meanAerodynamicChordLeadingEdgeX = Amount.valueOf(xle,SI.METRE);
	
		//------------------------------------------------------------------------------------
		// ALPHAS
		//------------------------------------------------------------------------------------
			
			//------------------------------------------------------------------------------------
			// CL --------------------------------------------------------------------------------
			//------------------------------------------------------------------------------------

			double [] dihedralActualZero = new double [input.getNumberOfPointSemispan()];	
			for (int i=0; i< dihedralActualZero.length; i++){
				dihedralActualZero[i] = 0.0;
			}
			double vortexSemiSpanToSemiSpanRatio = (1./(2*input.getNumberOfPointSemispan()));

			NasaBlackwell theNasaBlackwellCalculator = new  NasaBlackwell(
					input.getSemiSpan().getEstimatedValue(), 
					input.getSurface().getEstimatedValue(),
				    MyArrayUtils.convertListOfAmountTodoubleArray(input.getyDimensionaStationActual()),
				    MyArrayUtils.convertListOfAmountTodoubleArray(input.getChordDistributionActual()),
				    MyArrayUtils.convertListOfAmountTodoubleArray(input.getxLEDistributionActual()),
					dihedralActualZero,
					MyArrayUtils.convertListOfAmountTodoubleArray(input.getTwistDistributionActual()),
					MyArrayUtils.convertListOfAmountTodoubleArray(input.getAlphaZeroLiftDistributionActual()),
					vortexSemiSpanToSemiSpanRatio,
					0.0,
					input.getMachNumber(),
					input.getAltitude().getEstimatedValue());

			//cl alpha
			Amount<Angle> alphaFirst = Amount.valueOf(Math.toRadians(2.0), SI.RADIAN);
			Amount<Angle> alphaSecond = Amount.valueOf(Math.toRadians(4.0), SI.RADIAN);
			theNasaBlackwellCalculator.calculate(alphaFirst);
			double cLFirst = theNasaBlackwellCalculator.get_cLEvaluated();
			theNasaBlackwellCalculator.calculate(alphaSecond);
			double cLSecond = theNasaBlackwellCalculator.get_cLEvaluated();
			double cLAlpha = (cLSecond - cLFirst)/(alphaSecond.doubleValue(NonSI.DEGREE_ANGLE)-alphaFirst.doubleValue(NonSI.DEGREE_ANGLE)); // 1/rad
			input.setcLAlpha(cLAlpha); // 1/deg

			// alpha zero lift e cl zero
			Amount<Angle> alphaZero = Amount.valueOf(0.0, SI.RADIAN);
			theNasaBlackwellCalculator.calculate(alphaZero);
			double cLZero = theNasaBlackwellCalculator.get_cLEvaluated();
			input.setcLZero(cLZero);
			double alphaZeroLift = -(cLZero)/cLAlpha;
			input.setAlphaZeroLift(Amount.valueOf(Math.toDegrees(alphaZeroLift), NonSI.DEGREE_ANGLE));

			// alpha Star
			double rootChord = input.getChordDistributionInput().get(0).doubleValue(SI.METER);
			double kinkChord = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual())), 
					MyArrayUtils.convertListOfAmountTodoubleArray(input.getChordDistributionActual()),
					input.getAdimensionalKinkStation());
			double tipChord = input.getChordDistributionInput().get(input.getChordDistributionInput().size()-1).doubleValue(SI.METER);

			double alphaStarRoot= input.getAlphaStarDistributionInput().get(0).doubleValue(NonSI.DEGREE_ANGLE);
			double alphaStarKink = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual())), 
					MyArrayUtils.convertListOfAmountTodoubleArray(input.getAlphaStarDistributionActual()),
					input.getAdimensionalKinkStation());
			double alphaStarTip = input.getAlphaStarDistributionInput().get(input.getAlphaStarDistributionInput().size()-1).doubleValue(NonSI.DEGREE_ANGLE); 

			double dimensionalKinkStation = input.getAdimensionalKinkStation()*input.getSemiSpan().getEstimatedValue();
			double dimensionalOverKink = input.getSemiSpan().getEstimatedValue() - dimensionalKinkStation;

			double influenceAreaRoot = rootChord * dimensionalKinkStation/2;
			double influenceAreaKink = (kinkChord * dimensionalKinkStation/2) + (kinkChord * dimensionalOverKink/2);
			double influenceAreaTip = tipChord * dimensionalOverKink/2;

			double kRoot = 2*influenceAreaRoot/input.getSurface().getEstimatedValue();
			double kKink = 2*influenceAreaKink/input.getSurface().getEstimatedValue();
			double kTip = 2*influenceAreaTip/input.getSurface().getEstimatedValue();

			double alphaStar =  alphaStarRoot * kRoot + alphaStarKink * kKink + alphaStarTip * kTip;

			input.setAlphaStar(Amount.valueOf(Math.toDegrees(alphaStar), NonSI.DEGREE_ANGLE));

			Amount<Angle> alphaStarAmount = Amount.valueOf(alphaStar, SI.RADIAN);

			theNasaBlackwellCalculator.calculate(alphaStarAmount);
			double cLStar = theNasaBlackwellCalculator.get_cLEvaluated();
			input.setcLStar(cLStar);


			// cl Max
			double[] alphaArrayNasaBlackwell = MyArrayUtils.linspace(0.0, 30, 31);
			double[] clDistributionActualNasaBlackwell = new double[input.getNumberOfPointSemispan()]; 
			boolean firstIntersectionFound = false;
			int indexOfFirstIntersection = 0;
			int indexOfAlphaFirstIntersection = 0;
			double diffCLapp = 0;
			double diffCLappOld = 0;
			double diffCL = 0;
			double accuracy =0.0001;
			double deltaAlpha = 0.0;
			Amount<Angle> alphaNew = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
			double cLMax = 0.0;
			Amount<Angle> alphaMaxLinear = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);


			for (int k = indexOfFirstIntersection; k< input.getNumberOfPointSemispan(); k++) {
				diffCLapp = ( clDistributionActualNasaBlackwell[k] -  input.getMaximumliftCoefficientDistributionActual().get(k));
				diffCL = Math.max(diffCLapp, diffCLappOld);
				diffCLappOld = diffCL;
			}
			if( Math.abs(diffCL) < accuracy){
				cLMax = theNasaBlackwellCalculator.getCLCurrent();
				alphaMaxLinear = 
						Amount.valueOf(
								theNasaBlackwellCalculator.getAlphaCurrent(),
								NonSI.DEGREE_ANGLE); 
			}
			else{
				deltaAlpha = alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection] 
						- alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection-1];
				alphaNew = Amount.valueOf(
						(alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection] - (deltaAlpha/2)),
						NonSI.DEGREE_ANGLE
						).to(SI.RADIAN);
				double alphaOld = alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection]; 
				diffCLappOld = 0;
				while ( diffCL > accuracy){
					diffCL = 0;
					theNasaBlackwellCalculator.calculate(alphaNew);
					clDistributionActualNasaBlackwell = theNasaBlackwellCalculator
							.getClTotalDistribution()
							.toArray();
					for (int m =0; m< input.getNumberOfPointSemispan(); m++) {
						diffCLapp = clDistributionActualNasaBlackwell[m] - input.getMaximumliftCoefficientDistributionActual().get(m);

						if ( diffCLapp > 0 ){
							diffCL = Math.max(diffCLapp,diffCLappOld);
							diffCLappOld = diffCL;
						}

					}
					deltaAlpha = Math.abs(alphaOld - alphaNew.doubleValue(NonSI.DEGREE_ANGLE));
					alphaOld = alphaNew.doubleValue(NonSI.DEGREE_ANGLE);
					if (diffCL == 0){ //this means that diffCL would have been negative
						alphaNew = Amount.valueOf(
								alphaOld + (deltaAlpha/2),
								NonSI.DEGREE_ANGLE
								);
						diffCL = 1; // generic positive value in order to enter again in the while cycle 
						diffCLappOld = 0;
					}
					else { 
						if(deltaAlpha > 0.005){
							alphaNew = Amount.valueOf(
									alphaOld - (deltaAlpha/2),
									NonSI.DEGREE_ANGLE
									);	
							diffCLappOld = 0;
							if ( diffCL < accuracy) break;
						}
						else {
							alphaNew = Amount.valueOf(
									alphaOld - (deltaAlpha),
									NonSI.DEGREE_ANGLE
									);	
							diffCLappOld = 0;
							if ( diffCL < accuracy) 
								break;
						}
					}
				}
				theNasaBlackwellCalculator.calculate(alphaNew.to(SI.RADIAN));
				input.setClDistributionAtStall(
						MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertFromDoubleToPrimitive(
								theNasaBlackwellCalculator.getClTotalDistribution().toArray()
								)));	
				input.setcLMax(theNasaBlackwellCalculator.getCLCurrent());
				input.setAlphaMaxLinear(alphaNew);
			}

			// alpha stall
			double alphaMax = ((cLMax-cLZero)/Math.toRadians(cLAlpha));
			double alphaStall = alphaMax + input.getDeltaAlpha();
			input.setAlphaStall(Amount.valueOf(alphaStall, NonSI.DEGREE_ANGLE));
			
			Double [] alphaCleanArrayPlotDouble = MyArrayUtils.linspaceDouble(
					input.getAlphaZeroLift().doubleValue(NonSI.DEGREE_ANGLE)-5, 
					input.getAlphaStall().doubleValue(NonSI.DEGREE_ANGLE) + 2, 
					input.getNumberOfAlphaCL()
					);
			
			input.setAlphaVector(MyArrayUtils.convertDoubleArrayToListOfAmount(alphaCleanArrayPlotDouble, NonSI.DEGREE_ANGLE));
	
			
		//------------------------------------------------------------------------------------
		// CL, CD, CM MATRIX
		//------------------------------------------------------------------------------------
		
		// Creating cl cd cm curve
	
		List<List<Double>> clListInput = new ArrayList<>();
		List<List<Double>> cdListInput = new ArrayList<>();
		List<List<Double>> cmListInput = new ArrayList<>();
		List<List<Amount<Angle>>> alphaArrayInput = new ArrayList<>();
		
		for(int ii=0; ii<input.getNumberOfPointSemispan(); ii++) {

			Double[] cLArray = new Double[input.getAlphaVector().size()];

			double a = 0.0;
			double b = 0.0;
			double c = 0.0;
			double d = 0.0;
			double cLStarnew = (input.getClalphaDEGDistributionActual().get(ii))
					* input.getAlphaStarDistributionActual().get(ii).doubleValue(NonSI.DEGREE_ANGLE)
					+ input.getClZeroDistributionActual().get(ii);

			for(int i=0; i<input.getAlphaVector().size(); i++) {
				if(input.getAlphaVector().get(i).doubleValue(NonSI.DEGREE_ANGLE) <= input.getAlphaStarDistributionActual().get(ii).doubleValue(NonSI.DEGREE_ANGLE)) {

					cLArray[i] = (input.getClalphaDEGDistributionActual().get(ii))
							* input.getAlphaVector().get(i).doubleValue(NonSI.DEGREE_ANGLE)
							+ input.getClZeroDistributionActual().get(ii) ;
				}
				else {
					double[][] matrixData = { 
							{
								Math.pow(input.getAlphaStallDistributionActual().get(ii).doubleValue(NonSI.DEGREE_ANGLE), 3),
								Math.pow(input.getAlphaStallDistributionActual().get(ii).doubleValue(NonSI.DEGREE_ANGLE), 2),
								input.getAlphaStallDistributionActual().get(ii).doubleValue(NonSI.DEGREE_ANGLE),
								1.0},
							{
									3* Math.pow(input.getAlphaStallDistributionActual().get(ii).doubleValue(NonSI.DEGREE_ANGLE), 2),
									2*input.getAlphaStallDistributionActual().get(ii).doubleValue(NonSI.DEGREE_ANGLE),
									1.0,
									0.0},
							{
										Math.pow(input.getAlphaStarDistributionActual().get(ii).doubleValue(NonSI.DEGREE_ANGLE), 3),
										Math.pow(input.getAlphaStarDistributionActual().get(ii).doubleValue(NonSI.DEGREE_ANGLE), 2),
										input.getAlphaStarDistributionActual().get(ii).doubleValue(NonSI.DEGREE_ANGLE),
										1.0},
							{
											3* Math.pow(input.getAlphaStarDistributionActual().get(ii).doubleValue(NonSI.DEGREE_ANGLE), 2),
											2*input.getAlphaStarDistributionActual().get(ii).doubleValue(NonSI.DEGREE_ANGLE),
											1.0,
											0.0}

					};

					RealMatrix m = MatrixUtils.createRealMatrix(matrixData);

					double [] vector = {
							input.getMaximumliftCoefficientDistributionActual().get(ii),
							0,
							cLStarnew,
							input.getClalphaDEGDistributionActual().get(ii)
					};

					double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

					a = solSystem[0];
					b = solSystem[1];
					c = solSystem[2];
					d = solSystem[3];

					cLArray[i] = 
							a * Math.pow(input.getAlphaVector().get(i).doubleValue(NonSI.DEGREE_ANGLE), 3) + 
							b * Math.pow(input.getAlphaVector().get(i).doubleValue(NonSI.DEGREE_ANGLE), 2) +
							c * input.getAlphaVector().get(i).doubleValue(NonSI.DEGREE_ANGLE) +
							d;

				}
			}
			clListInput.add(MyArrayUtils.convertDoubleArrayToListDouble(cLArray));
			alphaArrayInput.add(input.getAlphaVector());
		}
		
		input.set_discretizedAirfoilsCl(AirfoilCalc.calculateCLMatrixAirfoils(
				input.getAlphaVector(),
				alphaArrayInput, 
				clListInput,
				input.getyAdimensionalStationInput(), 
				input.getyAdimensionalStationActual()
				));
		
		//cd
		for(int ii=0; ii<input.getNumberOfSections(); ii++) {
			Double[] cdArray = new Double[input.getAlphaVector().size()];
			
			for(int i=0; i< input.getAlphaVector().size(); i++) {
				cdArray[i] = input.getCdMinDistributionActual().get(ii)+
						input.getkDistributionActual().get(ii)*
						Math.pow(
								clListInput.get(ii).get(i)-input.getClIdealDistributionActual().get(ii),
								2
								);
			}
			
			cdListInput.add(MyArrayUtils.convertDoubleArrayToListDouble(cdArray));
		}
		
		input.set_discretizedAirfoilsCd(AirfoilCalc.calculateAerodynamicCoefficientsMatrixAirfoils(
				input.getcLVsAlphaVector(), 
				clListInput,
				cdListInput,
				input.getyAdimensionalStationInput(), 
				input.getyAdimensionalStationActual()
				));
			
		
		//cm
		for(int ii=0; ii<input.getNumberOfSections(); ii++) {
			Double[] cmArray = new Double[input.getAlphaVector().size()];
			
			for(int i=0; i< input.getAlphaVector().size(); i++) {
				cmArray[i] = input.getCmc4DistributionActual().get(ii);
			}
			
			cmListInput.add(MyArrayUtils.convertDoubleArrayToListDouble(cmArray));
		}
		
		input.set_discretizedAirfoilsCm(AirfoilCalc.calculateAerodynamicCoefficientsMatrixAirfoils(
				input.getcLVsAlphaVector(), 
				clListInput,
				cmListInput,
				input.getyAdimensionalStationInput(), 
				input.getyAdimensionalStationActual()
				));
				
				
				
		//------------------------------------------------------------------------------------
		
		
		System.out.println("\n---------ACTUAL PARAMETER DISTRIBUTION------------");	
		System.out.println("--------------------------------------------------");
		System.out.println("Y Stations  " + input.getyAdimensionalStationActual().toString());
		System.out.println("Y Dimensional Stations (unit = " + input.getyDimensionaStationActual().get(0).getUnit() + " ) = " + 
		Arrays.toString(
		MyArrayUtils.convertListOfAmountTodoubleArray((input.getyDimensionaStationActual()))
		));
		System.out.println("Chord distribution (unit = " + input.getChordDistributionActual().get(0).getUnit() + " ) = " + 
		Arrays.toString(
		MyArrayUtils.convertListOfAmountTodoubleArray((input.getChordDistributionActual()))
		));
		System.out.println("Xle distribution (unit = " + input.getxLEDistributionActual().get(0).getUnit() + " ) = " + 
		Arrays.toString(
		MyArrayUtils.convertListOfAmountTodoubleArray((input.getxLEDistributionActual()))
		));
		System.out.println("Twist distribution (unit = " + input.getTwistDistributionActual().get(0).getUnit() + " ) = " + 
		Arrays.toString(
		MyArrayUtils.convertListOfAmountTodoubleArray((input.getTwistDistributionActual()))
		));
		System.out.println("alpha star distribution (unit = " + input.getAlphaStarDistributionActual().get(0).getUnit() + " ) = " + 
		Arrays.toString(
		MyArrayUtils.convertListOfAmountTodoubleArray((input.getAlphaStarDistributionActual()))
		));
		System.out.println("alpha zero lift distribution (unit = " + input.getAlphaZeroLiftDistributionActual().get(0).getUnit() + " ) = " + 
		Arrays.toString(
		MyArrayUtils.convertListOfAmountTodoubleArray((input.getAlphaZeroLiftDistributionActual()))
		));
		System.out.println("Cl max distribution =  " + input.getMaximumliftCoefficientDistributionActual().toString());
		

		//--------------------------------------------------------------------------------------
		// BUILDING CLEAN CURVE: 
		//--------------------------------------------------------------------------------------

		input.setcLVsAlphaVector(
				MyArrayUtils.convertDoubleArrayToListDouble(
						LiftCalc.calculateCLvsAlphaArray(
								input.getcLZero(),
								input.getcLMax(),
								input.getAlphaStar(),
								input.getAlphaStall(),
								Amount.valueOf(input.getcLAlpha(), NonSI.DEGREE_ANGLE.inverse()),
								alphaCleanArrayPlotDouble
								)
						)
				);
		
		//--------------------------------------------------------------------------------------
		// DISTRIBUTIONS: 
		//--------------------------------------------------------------------------------------
		if (input.getNumberOfAlpha() !=0 ){
			
			Double[] alphaDistributionArray = new Double [input.getNumberOfAlpha()];
		
			alphaDistributionArray = MyArrayUtils.linspaceDouble(
					input.getAlphaInitial().getEstimatedValue(),
					input.getAlphaFinal().getEstimatedValue(), 
					input.getNumberOfAlpha());
			input.setAlphaDistributionArray(MyArrayUtils.convertDoubleArrayToListOfAmount(alphaDistributionArray, NonSI.DEGREE_ANGLE));
			for (int i=0; i<input.getNumberOfAlpha(); i++){	
				Amount<Angle> alphaAngle = Amount.valueOf(Math.toRadians(alphaDistributionArray[i]), SI.RADIAN);
				theNasaBlackwellCalculator.calculate(alphaAngle);
				double [] clDistributionArray = theNasaBlackwellCalculator.getClTotalDistribution().toArray();
				input.getClVsEtaVectors().set(
						i, 
						MyArrayUtils.convertDoubleArrayToListDouble(
								MyArrayUtils.convertFromDoubleToPrimitive(
										clDistributionArray
										)));
			}
		}
		
		// RESULTS
		System.out.println(" \n-----------WING CLEAN RESULTS-------------- ");
		System.out.println(" Alpha stall = " + input.getAlphaStall().getEstimatedValue() + " " + input.getAlphaStall().getUnit());
		System.out.println(" Alpha star = " + input.getAlphaStar().getEstimatedValue() + " " + input.getAlphaStar().getUnit());
		System.out.println(" Alpha max linear " + input.getAlphaMaxLinear().getEstimatedValue() + " " + input.getAlphaMaxLinear().getUnit());
		System.out.println(" CL max = " + input.getcLMax());
		System.out.println(" CL star = " + input.getcLStar());
		System.out.println(" CL alpha = " + input.getcLAlpha() + " (1/deg)");
		System.out.println(" Cl distribution at stall " + input.getClDistributionAtStall().toString());
		System.out.println(" Alpha Array for CL curve (deg) " + Arrays.toString(alphaCleanArrayPlotDouble));

		
		//------------------------------------------------------------------------------------
		// CD --------------------------------------------------------------------------------
		//------------------------------------------------------------------------------------
		
		// Distribution
		
		// DRAG COEFFICIENT DISTRIBUTION:

		input.getAlphaDistributionArray().stream().forEach( a -> {
		input.getParasiteDragDistribution().add(DragCalc.calculateParasiteDragDistributionFromAirfoil(
				input.getNumberOfPointSemispan(),
				a,
				theNasaBlackwellCalculator, 
				input.get_discretizedAirfoilsCd(),
				input.getcLVsAlphaVector()
				));
		});
		
		input.getAlphaDistributionArray().stream().forEach( a -> {
		input.getParasiteDragDistribution().add(DragCalc.calculateInducedDragDistribution(
				input.getNumberOfPointSemispan(),
				a, 
				theNasaBlackwellCalculator, 
				input.get_discretizedAirfoilsCl(), 
				input.getAlphaVector(), 
				input.getClZeroDistributionActual(),
				input.getClalphaDEGDistributionActual(),
				AerodynamicCalc.calculateInducedAngleOfAttackDistribution(
						a, 
						theNasaBlackwellCalculator, 
						input.getAltitude(),
						input.getMachNumber(),
						input.getNumberOfPointSemispan())
				));
		});

		
		input.getAlphaDistributionArray().stream().forEach( a -> {
		int indeaxOfa = input.getAlphaDistributionArray().indexOf(a);
		List<Double> totalDrag = new ArrayList<>();
		for (int i=0; i<input.getNumberOfPointSemispan(); i++){
			totalDrag.add(
					i,
					input.getParasiteDragDistribution().get(indeaxOfa).get(i) + 
					input.getInducedDragDistribution().get(indeaxOfa).get(i) 
					);
		}
			input.getTotalDragDistribution().add(totalDrag);
		}
		);
		
		
		
		// Polar Curve
			//wawe
		input.getAlphaDistributionArray().stream().forEach( a -> {
			double clActual = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							input.getAlphaVector()),
					MyArrayUtils.convertToDoublePrimitive(
							MyArrayUtils.convertListOfDoubleToDoubleArray(input.getcLVsAlphaVector())),
					a.doubleValue(NonSI.DEGREE_ANGLE)
					);
		input.getWawePolar().add(DragCalc.calculateCDWaveLockKorn(
							clActual,
							input.getMachNumber(),
							input.getSweepLE().doubleValue(SI.RADIAN),
							input.getMeanThickness(),
							input.getMeanAirfoilType()
							));
		});
		
			// cd parasite
		input.setParasitePolar(DragCalc.calculateParasiteDragLiftingSurfaceFromAirfoil(
				input.getAlphaVector(),
				theNasaBlackwellCalculator,
				input.get_discretizedAirfoilsCd(),
				input.getcLVsAlphaVector(),
				input.getChordDistributionActual(), 
				input.getSurface(), 
				input.getyDimensionaStationActual()
				));
		
		   // cd induced
		
		input.getAlphaDistributionArray().stream().forEach( a -> {
			double clActual = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							input.getAlphaVector()),
					MyArrayUtils.convertToDoublePrimitive(
							MyArrayUtils.convertListOfDoubleToDoubleArray(input.getcLVsAlphaVector())),
					a.doubleValue(NonSI.DEGREE_ANGLE)
					);
		input.getInducedPolar().add(
				Math.pow(clActual,2)
				/(Math.PI
				*input.getAspectRatio()
				*		AerodynamicCalc.calculateOswaldRaymer(
						input.getSweepLE().doubleValue(SI.RADIAN),
						input.getAspectRatio()
						))
				);
		});
		
		// total
		
		input.getAlphaDistributionArray().stream().forEach( a -> {
			int indeaxOfa = input.getAlphaDistributionArray().indexOf(a);
			input.getPolarClean().add(input.getParasitePolar().get(indeaxOfa) + input.getInducedPolar().get(indeaxOfa)+
					input.getWawePolar().get(indeaxOfa));
		});
		
		
		//------------------------------------------------------------------------------------
		// CM --------------------------------------------------------------------------------
		//------------------------------------------------------------------------------------

		
		input.setMomentCurveClean(
				MomentCalc.calcCMLiftingSurfaceWithIntegral(
				theNasaBlackwellCalculator, 
				input.getAlphaVector(), 
				input.getMac(),
				input.getyDimensionaStationActual(),
				input.getClZeroDistributionActual(),
				input.getClalphaDEGDistributionActual(), 
				input.getCmc4DistributionActual(),
				input.getChordDistributionActual(),
				input.getxLEDistributionActual(),
				input.get_discretizedAirfoilsCl(),
				input.getAlphaVector(), 
				input.getSurface(), 
				Amount.valueOf(
						input.getMomentumPole()*input.getMac().doubleValue(SI.METER) + meanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER),
						SI.METER
						)
				));
		
		
		
		//--------------------------------------------------------------------------------------
		// PLOT: 
		//--------------------------------------------------------------------------------------
		//LIFT CURVE
		String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
		System.out.println(" \n-----------WRITING CHART TO FILE. CL vs Alpha-------------- ");
		List<Double> xVector = new ArrayList<Double>();
		List<Double> yVector = new ArrayList<Double>();

		xVector = new ArrayList<Double>();
		yVector = new ArrayList<Double>();

		xVector = MyArrayUtils.convertDoubleArrayToListDouble(alphaCleanArrayPlotDouble);
		yVector = input.getcLVsAlphaVector();

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
				MyArrayUtils.convertToDoublePrimitive(yVector),
				null, 
				null, 
				null, 
				null, 
				"alpha",
				"CL",
				"deg", 
				"",
				folderPath,
				"Lift_Coefficient_Curve",
				false
				);

		System.out.println(" \n-------------------DONE----------------------- ");


		//LIFT DISTRIBUTIONS
		System.out.println(" \n-----------WRITING CHART TO FILE. Lift Coefficient Distribution-------------- ");
		List<Double[]> xVectorMatrix = new ArrayList<Double[]>();
		List<Double[]> yVectorMatrix = new ArrayList<Double[]>();
		List<String> legendList;
		double[][] xMatrix;
		double[][] yMatrix;
		String[] legendString;

		xVectorMatrix = new ArrayList<Double[]>();
		yVectorMatrix = new ArrayList<Double[]>();
		legendList  = new ArrayList<>(); 

		for(int i=0; i<input.getNumberOfAlpha(); i++){
			xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()));
			yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getClVsEtaVectors().get(i)));
			legendList.add("Cl distribution at alpha = " + input.getAlphaDistributionArray().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg");
		}

		xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
		yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
		legendString = new String[xVectorMatrix.size()];

		for(int i=0; i <xVectorMatrix.size(); i++){
			xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
			yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
			legendString [i] = legendList.get(i);
		}

		MyChartToFileUtils.plotNOCSV(
				xMatrix,
				yMatrix, 
				0.0, 
				null, 
				null, 
				null,
				"eta", 
				"Cl",
				"", 
				"", 
				legendString, 
				folderPath,
				"Lift_Coefficient_Distributions");

		System.out.println(" \n-------------------DONE----------------------- ");			

		//STALL PATH
		System.out.println(" \n-----------WRITING CHART TO FILE. Stall Path-------------- ");
		xVectorMatrix = new ArrayList<Double[]>();
		yVectorMatrix = new ArrayList<Double[]>();
		legendList  = new ArrayList<>(); 
		
			xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()));
			xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()));
			yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getMaximumliftCoefficientDistributionActual()));
			yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getClDistributionAtStall()));
			legendList.add("Cl max airofil ");
			legendList.add("Cl distribution at stall ");
	

		xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
		yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
		legendString = new String[xVectorMatrix.size()];

		for(int i=0; i <xVectorMatrix.size(); i++){
			xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
			yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
			legendString [i] = legendList.get(i);
		}

		MyChartToFileUtils.plotNOCSV(
				xMatrix,
				yMatrix, 
				0.0, 
				null, 
				null, 
				null,
				"eta", 
				"Cl",
				"", 
				"", 
				legendString, 
				folderPath,
				"Stall_Path");

		System.out.println(" \n-------------------DONE----------------------- ");	
		
		//DRAG DISTRIBUTIONS
		System.out.println(" \n-----------WRITING CHART TO FILE. Drag Coefficient Distributions-------------- ");
		xVectorMatrix = new ArrayList<Double[]>();
		yVectorMatrix = new ArrayList<Double[]>();

		legendList  = new ArrayList<>(); 

		for(int i=0; i<input.getNumberOfAlpha(); i++){
			xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getyAdimensionalStationActual()));
			yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getDragDistribution().get(i)));
			legendList.add("Cd distribution at alpha = " + input.getAlphaDistributionArray().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg");
		}

		xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
		yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
		legendString = new String[xVectorMatrix.size()];

		for(int i=0; i <xVectorMatrix.size(); i++){
			xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
			yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
			legendString [i] = legendList.get(i);
		}

		MyChartToFileUtils.plotNOCSV(
				xMatrix,
				yMatrix, 
				0.0, 
				null, 
				null, 
				null,
				"eta", 
				"Cd",
				"", 
				"", 
				legendString, 
				folderPath,
				"Drag_Coefficient_Distributions");

		System.out.println(" \n-------------------DONE----------------------- ");	
		
		//DRAG POLAR
		System.out.println(" \n-----------WRITING CHART TO FILE. Drag Polar Breakdown-------------- ");
		xVectorMatrix = new ArrayList<Double[]>();
		yVectorMatrix = new ArrayList<Double[]>();

		legendList  = new ArrayList<>(); 

			yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getcLVsAlphaVector()));
			xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getParasitePolar()));
			legendList.add("Parasite Drag");
			
			yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getcLVsAlphaVector()));
			xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getInducedPolar()));
			legendList.add("Induced Drag");
			
			yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getcLVsAlphaVector()));
			xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getWawePolar()));
			legendList.add("Wawe Drag");
			
			yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getcLVsAlphaVector()));
			xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(input.getPolarClean()));
			legendList.add("Polar Drag");
		

		xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
		yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
		legendString = new String[xVectorMatrix.size()];

		for(int i=0; i <xVectorMatrix.size(); i++){
			xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
			yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
			legendString [i] = legendList.get(i);
		}

		MyChartToFileUtils.plotNOCSV(
				xMatrix,
				yMatrix, 
				0.0, 
				null, 
				null, 
				null,
				"CD", 
				"CL",
				"", 
				"", 
				legendString, 
				folderPath,
				"Polar Breakdown");

		System.out.println(" \n-------------------DONE----------------------- ");	

		
		//MOMENT CURVE
		System.out.println(" \n-----------WRITING CHART TO FILE. Moment Curve-------------- ");
		xVector = new ArrayList<Double>();
		yVector = new ArrayList<Double>();

		xVector = new ArrayList<Double>();
		yVector = new ArrayList<Double>();

		xVector = MyArrayUtils.convertDoubleArrayToListDouble(alphaCleanArrayPlotDouble);
		yVector = input.getMomentCurveClean();

		System.out.println(" \n-----------WRITING CHART TO FILE . Cl distribution-------------- ");

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
				MyArrayUtils.convertToDoublePrimitive(yVector),
				null, 
				null, 
				null, 
				null, 
				"alpha",
				"CM",
				"deg", 
				"",
				folderPath,
				"Moment_Coefficient_Curve",
				false
				);

		System.out.println(" \n-------------------DONE----------------------- ");

			}
		}
		