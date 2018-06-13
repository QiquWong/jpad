package calculators.balance;

import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.cabinconfiguration.SeatsBlock;
import configuration.enumerations.RelativePositionEnum;

public class BoardingCenterOfGravityCalc {

	/** 
	 * Evaluate center of gravity variation during boarding procedure
	 * 
	 * @param seatsBlocks
	 * @return
	 * 
	 */
	public static void calculateCGBoarding (List<SeatsBlock> seatsBlockList, Aircraft aircraft) {
		
		int numberOfWindowColumns = 0;
		int numberOfAisleColumns = 0;
		int numberOfOtherColumns = 0;
		boolean isWindow = false;
		
		SeatsBlock seatBlock;
		
		for (int i=0; i<seatsBlockList.size(); i++) {
			
			seatBlock = seatsBlockList.get(i);
			isWindow = false;
			
			for (int j=0; j<seatBlock.getColumnsNumber(); j++) {
				
				// Check if the seat is near the window
				if ( (isWindow == false) && 
						( (seatBlock.getPosition().equals(RelativePositionEnum.RIGHT) 
								&& seatBlock.getColumnList().get(j*seatBlock.getRowsNumber()).equals(seatBlock.getColumnsNumber()-1)) ||
								(seatBlock.getPosition().equals(RelativePositionEnum.LEFT) 
										&& seatBlock.getColumnList().get(j*seatBlock.getRowsNumber()).equals(0)))) {
					
					numberOfWindowColumns += 1;
					isWindow = true;
				}
				
				// Check if the seat is near the aisle
				else if ( (seatBlock.getPosition().equals(RelativePositionEnum.RIGHT) && seatBlock.getColumnList().get(j*seatBlock.getRowsNumber()).equals(0)) 
								||	(seatBlock.getPosition().equals(RelativePositionEnum.LEFT) && seatBlock.getColumnList().get(j*seatBlock.getRowsNumber()).equals(seatBlock.getColumnsNumber()-1)) 
								||  (seatBlock.getPosition().equals(RelativePositionEnum.CENTER) && seatBlock.getColumnList().get(j*seatBlock.getRowsNumber()).equals(0)) 
								||	(seatBlock.getPosition().equals(RelativePositionEnum.CENTER) && seatBlock.getColumnList().get(j*seatBlock.getRowsNumber()).equals(seatBlock.getColumnsNumber()-1))
								) {
			
					numberOfAisleColumns += 1;
				}
				
				else {
					
					numberOfOtherColumns += 1;
					
				}
				
			}
			
		}
		
		////////////////////
		//  Front to rear //
		////////////////////

		/*
		 * Starting point -> OEW Conditions
		 */
		double sumFrontToRear = aircraft.getTheAnalysisManager().getTheBalance().getCGOperatingEmptyMass().getXBRF().doubleValue(SI.METER)*
				aircraft.getTheAnalysisManager().getTheBalance().getTheBalanceManagerInterface().getOperatingEmptyMass().doubleValue(SI.KILOGRAM);
		double currentMass = aircraft.getTheAnalysisManager().getTheBalance().getTheBalanceManagerInterface().getOperatingEmptyMass().doubleValue(SI.KILOGRAM);
		
		/*
		 *  In the three case of window, aisle of other, a loop over the number of rows has to be carried out
		 *  to calculate the cg excursion in both front-to-rear and rear-to-front cases.
		 */
		int mult = 0;
		List<Double> massList = new ArrayList<>();
		List<Double> xCGListFrontToRear = new ArrayList<>();
		for (int i=0; i<3; i++) { // i=0 -> window, i=1 -> aisle, i=2 -> other
			
			if (i==0) 
				mult = numberOfWindowColumns;
			else if (i==1) 
				mult = numberOfAisleColumns;
			else 
				mult = numberOfOtherColumns;
			
			for (int j=0; j<seatsBlockList.get(0).getRowsNumber(); j++) {

				sumFrontToRear += (
						seatsBlockList.get(0).getXList().get(j) 
						+ seatsBlockList.get(0).getXStart().doubleValue(SI.METER) 
						+ (seatsBlockList.get(0).getPitch().doubleValue(SI.METER)/2)
						)
						*mult
						*aircraft.getTheAnalysisManager().getTheBalance().getTheBalanceManagerInterface().getSinglePassengerMass().doubleValue(SI.KILOGRAM);

				currentMass += mult*aircraft.getTheAnalysisManager().getTheBalance().getTheBalanceManagerInterface().getSinglePassengerMass().doubleValue(SI.KILOGRAM);

				xCGListFrontToRear.add(sumFrontToRear/currentMass);
				massList.add(currentMass);
				
				aircraft.getCabinConfiguration().getCurrentMassList().add(
						Amount.valueOf(
								currentMass, 
								SI.KILOGRAM
								)
						);

				aircraft.getCabinConfiguration().getSeatsCoGFrontToRear().add(
						Amount.valueOf(
								(sumFrontToRear/currentMass),
								SI.METER
								)
						);
			}
		}
		
		////////////////////
		//  Rear to rear //
		////////////////////

		/*
		 * Starting point -> OEW Conditions
		 */
		double sumRearToFront = aircraft.getTheAnalysisManager().getTheBalance().getCGOperatingEmptyMass().getXBRF().doubleValue(SI.METER)*
				aircraft.getTheAnalysisManager().getTheBalance().getTheBalanceManagerInterface().getOperatingEmptyMass().doubleValue(SI.KILOGRAM);
		currentMass = aircraft.getTheAnalysisManager().getTheBalance().getTheBalanceManagerInterface().getOperatingEmptyMass().doubleValue(SI.KILOGRAM);
		
		/*
		 *  In the three case of window, aisle of other, a loop over the number of rows has to be carried out
		 *  to calculate the cg excursion in both front-to-rear and rear-to-front cases.
		 */
		mult = 0;
		List<Double> xCGListRearToFront = new ArrayList<>();
		for (int i=0; i<3; i++) { // i=0 -> window, i=1 -> aisle, i=2 -> other
			
			if (i==0) 
				mult = numberOfWindowColumns;
			else if (i==1) 
				mult = numberOfAisleColumns;
			else 
				mult = numberOfOtherColumns;
			
			for (int j=seatsBlockList.get(0).getRowsNumber()-1; j>=0; j--) {

				sumRearToFront += (
						seatsBlockList.get(0).getXList().get(j) 
						+ seatsBlockList.get(0).getXStart().doubleValue(SI.METER) 
						+ (seatsBlockList.get(0).getPitch().doubleValue(SI.METER)/2)
						)
						*mult
						*aircraft.getTheAnalysisManager().getTheBalance().getTheBalanceManagerInterface().getSinglePassengerMass().doubleValue(SI.KILOGRAM);

				currentMass += mult*aircraft.getTheAnalysisManager().getTheBalance().getTheBalanceManagerInterface().getSinglePassengerMass().doubleValue(SI.KILOGRAM);

				xCGListRearToFront.add(sumRearToFront/currentMass);
				
				aircraft.getCabinConfiguration().getSeatsCoGRearToFront().add(
						Amount.valueOf(
								(sumRearToFront/currentMass),
								SI.METER
								)
						);
			}
		}
	}
}
