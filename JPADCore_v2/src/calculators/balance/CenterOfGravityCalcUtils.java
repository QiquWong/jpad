package calculators.balance;

import java.util.List;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.cabinconfiguration.SeatsBlock;
import configuration.enumerations.RelativePositionEnum;

public class CenterOfGravityCalcUtils {

	/** 
	 * Evaluate center of gravity variation during boarding procedure
	 * 
	 * @param seatsBlocks
	 * @return
	 * 
	 */
	public static void calculateCGBoarding(List<SeatsBlock> seatsBlocks, Aircraft aircraft) {

		double sumFtoR = aircraft.getTheAnalysisManager().getTheBalance().getCGOEM().getXBRF().doubleValue(SI.METER)*
				aircraft.getTheAnalysisManager().getTheBalance().getOperatingEmptyMass().doubleValue(SI.KILOGRAM),

				sumRtoF = aircraft.getTheAnalysisManager().getTheBalance().getCGOEM().getXBRF().doubleValue(SI.METER)*
				aircraft.getTheAnalysisManager().getTheBalance().getOperatingEmptyMass().doubleValue(SI.KILOGRAM);

		double currentMass, mult, emptyColumns = 0.;
		boolean window = false, aisle = false, other = false;

		SeatsBlock x = null;
		currentMass = aircraft.getTheAnalysisManager().getTheBalance().getOperatingEmptyMass().doubleValue(SI.KILOGRAM);
		window = false; aisle = false; other = false;

		///////////////////
		// Front to rear
		///////////////////
		for (int k = 0; k < seatsBlocks.size(); k++) {

			x = seatsBlocks.get(k);
			emptyColumns = 0.;

			for (int j=0; j < x.getColumnsNumber(); j++) {

				// Check if the seat is near the window
				if ((window == false && aisle == false && other == false) 
						&& ((x.getPosition().equals(RelativePositionEnum.RIGHT) 
								&& x.getColumnList().get(j).equals(x.getColumnsNumber()-1)) |
								(x.getPosition().equals(RelativePositionEnum.LEFT) 
										&& x.getColumnList().get(j).equals(0)))) {

					for (int i = 0; i < x.getRowsNumber()-1; i++) {

						sumFtoR += (x.getXList().get(i) 
								+ x.getXStart().doubleValue(SI.METER) 
								+ x.getPitch().doubleValue(SI.METER)/2
								)*
								2*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().doubleValue(SI.KILOGRAM);

						currentMass += 2*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().doubleValue(SI.KILOGRAM);
						aircraft.getCabinConfiguration().getCurrentMassList().add(
								Amount.valueOf(
										currentMass, 
										SI.KILOGRAM
										)
								);
						aircraft.getCabinConfiguration().getCurrentXCoGfrontToRearWindow().add(
								Amount.valueOf(
										(sumFtoR/currentMass),
										SI.METER
										)
								);
					}
					window = true;
					break;
				}

				// Check if the seat is near the aisle
				if ((window == true && other == false) 
						&& ((x.getPosition() == RelativePositionEnum.RIGHT && x.getColumnList().get(j).equals(0)) 
								||	(x.getPosition() == RelativePositionEnum.LEFT && x.getColumnList().get(j).equals(x.getColumnsNumber()-1)) 
								||  (x.getPosition() == RelativePositionEnum.CENTER && x.getColumnList().get(j).equals(0)) 
								||	(x.getPosition() == RelativePositionEnum.CENTER && x.getColumnList().get(j).equals(x.getColumnsNumber()-1)))) {

					// If there are two aisles the loop has to fill 4 columns,
					// otherwise it has to fill 2 columns
					if (aircraft.getCabinConfiguration().getAislesNumber() > 1) {
						mult = 4.;
					} else {
						mult = 2.;
					}

					for (int i = 0; i < x.getRowsNumber()-1; i++) {

						sumFtoR += (x.getXList().get(i) + x.getXStart().doubleValue(SI.METER) + x.getPitch().doubleValue(SI.METER)/2)*
								mult*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().doubleValue(SI.KILOGRAM);

						currentMass += mult*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().doubleValue(SI.KILOGRAM);
						
						aircraft.getCabinConfiguration().getCurrentMassList().add(
								Amount.valueOf(
										currentMass,
										SI.KILOGRAM
										)
								);
						aircraft.getCabinConfiguration().getCurrentXCoGfrontToRearAisle().add(
								Amount.valueOf(
										(sumFtoR/currentMass),
										SI.METER
										)
								);
					}
					aisle = true;
					break;
				}
			}
		}


		for(int k = 0; k < seatsBlocks.size(); k++) {

			x = seatsBlocks.get(k);
			emptyColumns = 0.;

			if (x.getColumnsNumber() > 2) {

				/*
				 * Total number of columns still empty for a single block. 
				 * There are two possible cases:
				 * 1) the block is on the side, so two columns have been
				 * taken (window and aisle);
				 * 2) the block is a central one. Still two columns have
				 * been taken which are the ones near the aisle
				 */
				emptyColumns = emptyColumns + x.getColumnsNumber() - 2;

				for (int i = 0; i < x.getRowsNumber()-1; i++) {

					sumFtoR += (x.getXList().get(i) + x.getXStart().doubleValue(SI.METER) + x.getPitch().doubleValue(SI.METER)/2)*
							emptyColumns*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().doubleValue(SI.KILOGRAM);

					currentMass += emptyColumns*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().doubleValue(SI.KILOGRAM);
					
					aircraft.getCabinConfiguration().getCurrentMassList().add(
							Amount.valueOf(
									currentMass,
									SI.KILOGRAM
									)
							);
					aircraft.getCabinConfiguration().getCurrentXCoGfrontToRearOther().add(
							Amount.valueOf(
									(sumFtoR/currentMass),
									SI.METER
									)
							);
				}
				break;
			}
		}

		currentMass = aircraft.getTheAnalysisManager().getTheBalance().getOperatingEmptyMass().doubleValue(SI.KILOGRAM);
		window = false; aisle = false; other = false;

		///////////////////
		// Rear to front
		///////////////////
		for (int k = seatsBlocks.size()-1; k >= 0; k--) {

			x = seatsBlocks.get(k);
			emptyColumns = 0.;

			for (int j=0; j < x.getColumnsNumber(); j++) {

				// Check if the seat is near the window
				if ((window == false && aisle == false && other == false) 
						&& ((x.getPosition().equals(RelativePositionEnum.RIGHT) && x.getColumnList().get(j).equals(x.getColumnsNumber()-1)) 
								|| (x.getPosition().equals(RelativePositionEnum.LEFT) && x.getColumnList().get(j).equals(0)))) {

					for (int i = x.getRowsNumber()-2; i >= 0; i--) {

						sumRtoF += (x.getXList().get(i) + x.getXStart().doubleValue(SI.METER) + x.getPitch().doubleValue(SI.METER)/2)*
								2*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().doubleValue(SI.KILOGRAM);

						currentMass += 2*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().doubleValue(SI.KILOGRAM);

						aircraft.getCabinConfiguration().getCurrentXCoGrearToFrontWindow().add(
								Amount.valueOf(
										(sumRtoF/currentMass),
										SI.METER
										)
								);
					}
					window = true;
					break;
				}

				// Check if the seat is near the aisle
				if ((window == true && other == false) 
						&& ((x.getPosition() == RelativePositionEnum.RIGHT && x.getColumnList().get(j).equals(0)) 
								|| (x.getPosition() == RelativePositionEnum.LEFT && x.getColumnList().get(j).equals(x.getColumnsNumber()-1)) 
								|| (x.getPosition() == RelativePositionEnum.CENTER && x.getColumnList().get(j).equals(0))
								|| (x.getPosition() == RelativePositionEnum.CENTER && x.getColumnList().get(j).equals(x.getColumnsNumber()-1)))) {

					// If there are the aisles the loop has to fill 4 columns,
					// otherwise it has to fill 2 columns
					if (aircraft.getCabinConfiguration().getAislesNumber() > 1) {
						mult = 4.;
					} else {
						mult = 2.;
					}

					for (int i = x.getRowsNumber()-2; i >= 0; i--){

						sumRtoF += ((x.getXList().get(i) + x.getXStart().doubleValue(SI.METER) + x.getPitch().doubleValue(SI.METER)/2)*
								mult*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().doubleValue(SI.KILOGRAM));

						currentMass += mult*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().doubleValue(SI.KILOGRAM);

						aircraft.getCabinConfiguration().getCurrentXCoGrearToFrontAisle().add(
								Amount.valueOf(
										(sumRtoF/currentMass),
										SI.METER
										)
								);
					}
					aisle = true;
					break;
				}
			}
		}

		for (int k = seatsBlocks.size()-1; k >= 0; k--) {

			x = seatsBlocks.get(k);
			emptyColumns = 0.;

			if (x.getColumnsNumber() > 2) {

				/*
				 * Total number of columns still empty for a single block. 
				 * There are two possible cases:
				 * 1) the block is on the side, so two columns have been
				 * taken (window and aisle);
				 * 2) the block is a central one. Still two columns have
				 * been taken which are the ones near the aisle
				 */
				emptyColumns = x.getColumnsNumber() - 2;

				for (int i = x.getRowsNumber() - 2; i >= 0 ; i--) {

					sumRtoF += (x.getXList().get(i) + x.getXStart().doubleValue(SI.METER) + x.getPitch().doubleValue(SI.METER)/2)*
							emptyColumns*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().doubleValue(SI.KILOGRAM);

					currentMass += emptyColumns*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().doubleValue(SI.KILOGRAM);

					aircraft.getCabinConfiguration().getCurrentXCoGrearToFrontOther().add(
							Amount.valueOf(
									(sumRtoF/currentMass),
									SI.METER
									)
							);
				}
				break;
			}
		}

	}
	
}
