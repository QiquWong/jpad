package sandbox.mr.ExecutableMeanAirfoil;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;

public class MeanAirfoilCalc {

	public static void executeStandAlone(InputOutputTree inputOutput) throws InstantiationException, IllegalAccessException{

		System.out.println("------------------------------------------------------------------------");
		System.out.println("Calculating influence areas and coefficients ... \n");

		//----------------------------------------------------------------------------------------------
		// calculation of the first influence area ...
		inputOutput.getInfluenceAreas().add(
				Amount.valueOf(
						0.5
						*inputOutput.getChordsArray().get(0).getEstimatedValue()
						*(inputOutput.getEtaStations().get(1)*inputOutput.getWingSpan().divide(2).getEstimatedValue()
								-inputOutput.getEtaStations().get(0)*inputOutput.getWingSpan().divide(2).getEstimatedValue()),
						SI.SQUARE_METRE)
				);

		inputOutput.getInfluenceCoefficients().add(
				inputOutput.getInfluenceAreas().get(0)
				.times(2)
				.divide(inputOutput.getWingSurface()).getEstimatedValue()
				);

		//----------------------------------------------------------------------------------------------
		// calculation of the inner influence areas ... 
		for(int i=1; i<inputOutput.getNumberOfSection()-1; i++) {

			inputOutput.getInfluenceAreas().add(
					Amount.valueOf(
							(0.5
									*inputOutput.getChordsArray().get(i).getEstimatedValue()
									*((inputOutput.getEtaStations().get(i)*inputOutput.getWingSpan().divide(2).getEstimatedValue())
											-(inputOutput.getEtaStations().get(i-1)*inputOutput.getWingSpan().divide(2).getEstimatedValue()))
									)
							+(0.5
									*inputOutput.getChordsArray().get(i).getEstimatedValue()
									*((inputOutput.getEtaStations().get(i+1)*inputOutput.getWingSpan().divide(2).getEstimatedValue())
											-(inputOutput.getEtaStations().get(i))*inputOutput.getWingSpan().divide(2).getEstimatedValue())),
							SI.SQUARE_METRE)
					);

			inputOutput.getInfluenceCoefficients().add(
					inputOutput.getInfluenceAreas().get(i)
					.times(2)
					.divide(inputOutput.getWingSurface()).getEstimatedValue()
					);

		}

		//----------------------------------------------------------------------------------------------
		// calculation of the last influence area ...
		inputOutput.getInfluenceAreas().add(
				Amount.valueOf(
						0.5
						*inputOutput.getChordsArray().get(inputOutput.getChordsArray().size()-1).getEstimatedValue()
						*(inputOutput.getEtaStations().get(inputOutput.getEtaStations().size()-1)*inputOutput.getWingSpan().divide(2).getEstimatedValue()
								-inputOutput.getEtaStations().get(inputOutput.getEtaStations().size()-2)*inputOutput.getWingSpan().divide(2).getEstimatedValue()),
						SI.SQUARE_METRE)
				);

		inputOutput.getInfluenceCoefficients().add(
				inputOutput.getInfluenceAreas().get(inputOutput.getInfluenceAreas().size()-1)
				.times(2)
				.divide(inputOutput.getWingSurface()).getEstimatedValue()
				);

		//check results:
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Checking influence areas ... \n");
		
		double totalInfluenceArea = 0; 
		for(int i=0; i<inputOutput.getInfluenceAreas().size(); i++)
			totalInfluenceArea += inputOutput.getInfluenceAreas().get(i).getEstimatedValue();
		
		if(inputOutput.getWingSurface().getEstimatedValue() - (totalInfluenceArea*2) < 0.001) {
			System.out.println("\tTotal influence area equals the semi-surface. CHECK PASSED!!\n");
			System.out.println("\tTotal inluence area = " + totalInfluenceArea);
			System.out.println("\tWing semi-surface = " + inputOutput.getWingSurface().divide(2).getEstimatedValue() + "\n");
		}
		else {
			System.err.println("\tTotal influence area differs from the semi-surface. CHECK NOT PASSED!!\n");
			return;
		}
		
		//----------------------------------------------------------------------------------------------
		// MEAN AIRFOIL DATA CALCULATION:
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Calculating mean airfoil data ...");

		//----------------------------------------------------------------------------------------------
		// Chord:
		double chordMeanAirfoil = 0;

		for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
			chordMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
			*inputOutput.getChordsArray().get(i).getEstimatedValue();

		inputOutput.setChords(Amount.valueOf(chordMeanAirfoil, SI.METER));

		//----------------------------------------------------------------------------------------------
		// Maximum thickness:
		if(!inputOutput.getMaximumThicknessArray().isEmpty()) {
			double maximumThicknessMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				maximumThicknessMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getMaximumThicknessArray().get(i);

			inputOutput.setMaximumThickness(maximumThicknessMeanAirfoil);
		}

		//----------------------------------------------------------------------------------------------
		// Leading edge radius thickness:
		if(!inputOutput.getRadiusLEArray().isEmpty()) {
			double leadingEdgeMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				leadingEdgeMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getRadiusLEArray().get(i).getEstimatedValue();

			inputOutput.setRadiusLE(Amount.valueOf(leadingEdgeMeanAirfoil, SI.METER));
		}

		//----------------------------------------------------------------------------------------------
		// Trailing edge angle:
		if(!inputOutput.getPhiTEArray().isEmpty()) {
			double trailingEdgeAngleMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				trailingEdgeAngleMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getPhiTEArray().get(i).getEstimatedValue();

			inputOutput.setPhiTE(Amount.valueOf(trailingEdgeAngleMeanAirfoil, NonSI.DEGREE_ANGLE));
		}

		//----------------------------------------------------------------------------------------------
		// Alpha zero lift:
		if(!inputOutput.getAlphaZeroLiftArray().isEmpty()) {
			double alphaZeroLiftMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				alphaZeroLiftMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getAlphaZeroLiftArray().get(i).getEstimatedValue();

			inputOutput.setAlphaZeroLift(Amount.valueOf(alphaZeroLiftMeanAirfoil, NonSI.DEGREE_ANGLE));
		}

		//----------------------------------------------------------------------------------------------
		// Alpha star:
		if(!inputOutput.getAlphaStarArray().isEmpty()) {
			double alphaStarMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				alphaStarMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getAlphaStarArray().get(i).getEstimatedValue();

			inputOutput.setAlphaStar(Amount.valueOf(alphaStarMeanAirfoil, NonSI.DEGREE_ANGLE));
		}

		//----------------------------------------------------------------------------------------------
		// Alpha stall:
		if(!inputOutput.getAngleOfStallArray().isEmpty()) {
			double alphaStallMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				alphaStallMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getAngleOfStallArray().get(i).getEstimatedValue();

			inputOutput.setAngleOfStall(Amount.valueOf(alphaStallMeanAirfoil, NonSI.DEGREE_ANGLE));
		}

		//----------------------------------------------------------------------------------------------
		// Cl0:
		if(!inputOutput.getCl0Array().isEmpty()) {
			double cl0MeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				cl0MeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getCl0Array().get(i);

			inputOutput.setCl0(cl0MeanAirfoil);
		}

		//----------------------------------------------------------------------------------------------
		// Cl star:
		if(!inputOutput.getClStarArray().isEmpty()) {
			double clStarMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				clStarMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getClStarArray().get(i);

			inputOutput.setClStar(clStarMeanAirfoil);
		}

		//----------------------------------------------------------------------------------------------
		// Cl max:
		if(!inputOutput.getClmaxArray().isEmpty()) {
			double clMaxMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				clMaxMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getClmaxArray().get(i);

			inputOutput.setClmax(clMaxMeanAirfoil);
		}

		//----------------------------------------------------------------------------------------------
		// Cl alpha:
		if(!inputOutput.getClAlphaArray().isEmpty()) {
			double clAlphaMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				clAlphaMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getClAlphaArray().get(i).getEstimatedValue();

			inputOutput.setClAlpha(Amount.valueOf(clAlphaMeanAirfoil, NonSI.DEGREE_ANGLE.inverse()));
		}

		//----------------------------------------------------------------------------------------------
		// Cd min:
		if(!inputOutput.getCdminArray().isEmpty()) {
			double cdMinMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				cdMinMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getCdminArray().get(i);

			inputOutput.setCdmin(cdMinMeanAirfoil);
		}

		//----------------------------------------------------------------------------------------------
		// Cl @ Cd min:
		if(!inputOutput.getClAtCdminArray().isEmpty()) {
			double clAtCdMinMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				clAtCdMinMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getClAtCdminArray().get(i);

			inputOutput.setClAtCdmin(clAtCdMinMeanAirfoil);
		}

		//----------------------------------------------------------------------------------------------
		// K factor drag polar:
		if(!inputOutput.getkFactorDragPolarArray().isEmpty()) {
			double kFactorDragPolarMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				kFactorDragPolarMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getkFactorDragPolarArray().get(i);

			inputOutput.setkFactorDragPolar(kFactorDragPolarMeanAirfoil);
		}

		//----------------------------------------------------------------------------------------------
		// Aerodynamic center:
		if(!inputOutput.getXacArray().isEmpty()) {
			double xACMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				xACMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getXacArray().get(i);

			inputOutput.setXac(xACMeanAirfoil);
		}

		//----------------------------------------------------------------------------------------------
		// Cm_ac:
		if(!inputOutput.getCmACArray().isEmpty()) {
			double cmACMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				cmACMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getCmACArray().get(i);

			inputOutput.setCmAC(cmACMeanAirfoil);
		}

		//----------------------------------------------------------------------------------------------
		// Cm_ac stall:
		if(!inputOutput.getCmACstallArray().isEmpty()) {
			double cmACStallMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				cmACStallMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getCmACstallArray().get(i);

			inputOutput.setCmACstall(cmACStallMeanAirfoil);
		}

		//----------------------------------------------------------------------------------------------
		// Other values:
		if(!inputOutput.getOtherValuesArray().isEmpty()) { 
			double otherValuesMeanAirfoil = 0;

			for(int i=0; i<inputOutput.getInfluenceCoefficients().size(); i++)
				otherValuesMeanAirfoil += inputOutput.getInfluenceCoefficients().get(i)
				*inputOutput.getOtherValuesArray().get(i);

			inputOutput.setOtherValues(otherValuesMeanAirfoil);
		}

		//----------------------------------------------------------------------------------------------
		// PRINT RESULTS:
		System.out.println("\n------------------------------------------------------------------------");
		System.out.println("INFLUENCE AREAS : \n");
		System.out.println(inputOutput.getInfluenceAreas());
		System.out.println("--------------------------------------------------------------------------");
		System.out.println("INFLUENCE COEFFICIENTS : \n");
		System.out.println(inputOutput.getInfluenceCoefficients());
		System.out.println("--------------------------------------------------------------------------");
		System.out.println("GEOMETRY : ");
		System.out.println("\tChord mean airfoil = " + inputOutput.getChords() + "\n");
		if(!inputOutput.getMaximumThicknessArray().isEmpty()) 
			System.out.println("\tt/c mean airfoil = " + inputOutput.getMaximumThickness());
		if(!inputOutput.getRadiusLEArray().isEmpty()) 
			System.out.println("\tLE radius mean airfoil = " + inputOutput.getRadiusLE());
		if(!inputOutput.getPhiTEArray().isEmpty())
			System.out.println("\tphi TE mean airfoil = " + inputOutput.getPhiTE());
		System.out.println("--------------------------------------------------------------------------");
		System.out.println("AERODYNAMIC : ");
		if(!inputOutput.getAlphaZeroLiftArray().isEmpty()) 		
			System.out.println("\tAlpha zero lift mean airfoil = " + inputOutput.getAlphaZeroLift());
		if(!inputOutput.getAlphaStarArray().isEmpty()) 
			System.out.println("\tAlpha star mean airfoil = " + inputOutput.getAlphaStar());
		if(!inputOutput.getAngleOfStallArray().isEmpty()) 
			System.out.println("\tAlpha stall mean airfoil = " + inputOutput.getAngleOfStall());
		if(!inputOutput.getCl0Array().isEmpty()) 
			System.out.println("\tCl0 mean airfoil = " + inputOutput.getCl0());
		if(!inputOutput.getClStarArray().isEmpty()) 
			System.out.println("\tCl* mean airfoil = " + inputOutput.getClStar());
		if(!inputOutput.getClmaxArray().isEmpty()) 
			System.out.println("\tClmax mean airfoil = " + inputOutput.getClmax());
		if(!inputOutput.getClAlphaArray().isEmpty()) 
			System.out.println("\tClalpha mean airfoil = " + inputOutput.getClAlpha());
		if(!inputOutput.getCdminArray().isEmpty()) 
			System.out.println("\tCdmin mean airfoil = " + inputOutput.getCdmin());
		if(!inputOutput.getClAtCdminArray().isEmpty()) 
			System.out.println("\tCl @ Cdmin mean airfoil = " + inputOutput.getClAtCdmin());
		if(!inputOutput.getkFactorDragPolarArray().isEmpty()) 
			System.out.println("\tK factors drag ploar mean airfoil = " + inputOutput.getkFactorDragPolar());
		if(!inputOutput.getXacArray().isEmpty()) 
			System.out.println("\tXac mean airfoil = " + inputOutput.getXac());
		if(!inputOutput.getCmACArray().isEmpty()) 
			System.out.println("\tCm_ac mean airfoil = " + inputOutput.getCmAC());
		if(!inputOutput.getCmACstallArray().isEmpty()) 
			System.out.println("\tCm_ac stall mean airfoil = " + inputOutput.getCmACstall());

		if(!inputOutput.getOtherValuesArray().isEmpty()) {
			System.out.println("------------------------------------------------------------------------");
			System.out.println("OTHER VALUES : ");
			System.out.println("\tOther values related to the mean airfoil = " + inputOutput.getOtherValues() + "\n");
		}

		System.out.println("--------------------------------DONE-------------------------------------\n");
	}
}