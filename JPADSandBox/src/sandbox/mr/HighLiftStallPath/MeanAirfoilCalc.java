package sandbox.mr.HighLiftStallPath;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;

import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class MeanAirfoilCalc {

	public static void executeStandAlone(InputOutputTree input) throws InstantiationException, IllegalAccessException{

		System.out.println("------------------------------------------------------------------------");
		System.out.println("Calculating influence areas and coefficients ... \n");

		//----------------------------------------------------------------------------------------------
		// calculation of the first influence area ...
		
		List<Amount<Area>> influenceAreas = new ArrayList<Amount<Area>>();
		influenceAreas.add(
				Amount.valueOf(
						0.5
						*input.getChordDistribution().get(0).getEstimatedValue()
						*(input.getyAdimensionalStationInput().get(1)*input.getSpan().divide(2).getEstimatedValue()
								-input.getyAdimensionalStationInput().get(0)*input.getSpan().divide(2).getEstimatedValue()),
						SI.SQUARE_METRE)
				);

		List<Double> influenceCoefficient = new ArrayList<Double>();
		
		influenceCoefficient.add(
				influenceAreas.get(0)
				.times(2)
				.divide(input.getSurface()).getEstimatedValue()
				);
		
		input.setInfluenceCoefficient(influenceCoefficient);

		//----------------------------------------------------------------------------------------------
		// calculation of the inner influence areas ... 
		
		for(int i=1; i<input.getNumberOfSections()-1; i++) {

			influenceAreas.add(
					Amount.valueOf(
							(0.5
									*input.getChordDistribution().get(i).getEstimatedValue()
									*((input.getyAdimensionalStationInput().get(i)*input.getSpan().divide(2).getEstimatedValue())
											-(input.getyAdimensionalStationInput().get(i-1)*input.getSpan().divide(2).getEstimatedValue()))
									)
							+(0.5
									*input.getChordDistribution().get(i).getEstimatedValue()
									*((input.getyAdimensionalStationInput().get(i+1)*input.getSpan().divide(2).getEstimatedValue())
											-(input.getyAdimensionalStationInput().get(i))*input.getSpan().divide(2).getEstimatedValue())),
							SI.SQUARE_METRE)
					);

			influenceCoefficient.add(
					influenceAreas.get(i)
					.times(2)
					.divide(input.getSurface()).getEstimatedValue()
					);

		}

		//----------------------------------------------------------------------------------------------
		// calculation of the last influence area ...
		
		influenceAreas.add(
				Amount.valueOf(
						0.5
						*input.getChordDistribution().get(input.getChordDistribution().size()-1).getEstimatedValue()
						*(input.getyAdimensionalStationInput().get(input.getyAdimensionalStationInput().size()-1)*input.getSpan().divide(2).getEstimatedValue()
								-input.getyAdimensionalStationInput().get(input.getyAdimensionalStationInput().size()-2)*input.getSpan().divide(2).getEstimatedValue()),
						SI.SQUARE_METRE)
				);

		influenceCoefficient.add(
				influenceAreas.get(influenceAreas.size()-1)
				.times(2)
				.divide(input.getSurface()).getEstimatedValue()
				);

		//check results:
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Checking influence areas ... \n");
		
		double totalInfluenceArea = 0; 
		for(int i=0; i<influenceAreas.size(); i++)
			totalInfluenceArea += influenceAreas.get(i).getEstimatedValue();
		
		if(input.getSurface().getEstimatedValue() - (totalInfluenceArea*2) < 0.001) {
			System.out.println("\tTotal influence area equals the semi-surface. CHECK PASSED!!\n");
			System.out.println("\tTotal inluence area = " + totalInfluenceArea);
			System.out.println("\tWing semi-surface = " + input.getSurface().divide(2).getEstimatedValue() + "\n");
		}
		else {
			System.err.println("\tTotal influence area differs from the semi-surface. CHECK NOT PASSED!!\n");
			return;
		}
		
		// t/c mean
	}
		
		
		
		public static double[] meanAirfoilFlap(double etaIn, double etaOut, InputOutputTree input) throws InstantiationException, IllegalAccessException{

			double [] influenceAreas = new double [2];
			double [] influenceFactors = new double [2];
			
			double chordIn = MyMathUtils.getInterpolatedValue1DLinear(MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertListOfAmountodoubleArray(input.getChordDistribution()), etaIn);
			

			double chordOut = MyMathUtils.getInterpolatedValue1DLinear(MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertListOfAmountodoubleArray(input.getChordDistribution()), etaOut);
			
			influenceAreas[0] = (chordIn * ((etaOut - etaIn)*input.getSemiSpan().getEstimatedValue()))/2;
			influenceAreas[1] = (chordOut * ((etaOut - etaIn)*input.getSemiSpan().getEstimatedValue()))/2;
			
			// it returns the influence coefficient
			
			influenceFactors[0] = influenceAreas[0]/(influenceAreas[0] + influenceAreas[1]);
			influenceFactors[1] = influenceAreas[1]/(influenceAreas[0] + influenceAreas[1]);
			
			return influenceFactors;
	}
}