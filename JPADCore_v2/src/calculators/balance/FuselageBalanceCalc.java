package calculators.balance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;

public class FuselageBalanceCalc {

	/*
	 * page 359 Sforza (2014) - Aircraft Design
	 */
	public static Amount<Length> calculateFuselageXCGSforza (Aircraft aircraft) {

		return Amount.valueOf(
				aircraft.getFuselage().getFuselageLength().doubleValue(SI.METER)
				* (
						(aircraft.getFuselage().getNoseFinenessRatio()/aircraft.getFuselage().getFuselageFinenessRatio())
						+ ( (aircraft.getFuselage().getFuselageFinenessRatio() - 5) / (1.8*aircraft.getFuselage().getFuselageFinenessRatio()))
						), 
				SI.METER
				);
	}
	
	public static Amount<Length> calculateFuselageXCGTorenbeek (Aircraft aircraft) {
		
		Amount<Length> _xCG = Amount.valueOf(0.0, SI.METER);
		
		if (aircraft.getPowerPlant().getEngineNumber() == 1) {
			if (aircraft.getPowerPlant().getEngineType().get(0) == EngineTypeEnum.PISTON ||
					aircraft.getPowerPlant().getEngineType().get(0) == EngineTypeEnum.TURBOPROP) {

				_xCG = aircraft.getFuselage().getFuselageLength().to(SI.METER).times(0.335);
			}
		}
		else {

			List<Amount<Mass>> engineMassList = new ArrayList<>();
			List<Double> xCGPercentageList = new ArrayList<>();

			for(int i=0; i<aircraft.getPowerPlant().getEngineNumber(); i++) {

				engineMassList.add(aircraft.getPowerPlant().getTheWeights().getMassEstimatedList().get(i));
				
				if (aircraft.getPowerPlant().getMountingPosition().get(i) == EngineMountingPositionEnum.WING) {
					if (aircraft.getPowerPlant().getEngineType().get(i) == EngineTypeEnum.PISTON
							|| aircraft.getPowerPlant().getEngineType().get(i) == EngineTypeEnum.TURBOPROP) {
						xCGPercentageList.add(0.39); 
					} else {
						xCGPercentageList.add(0.435);
					}
				}
				else if (aircraft.getPowerPlant().getMountingPosition().get(i) == EngineMountingPositionEnum.REAR_FUSELAGE
						|| aircraft.getPowerPlant().getMountingPosition().get(i) == EngineMountingPositionEnum.HTAIL) {
					xCGPercentageList.add(0.47);
				}
				else if (aircraft.getPowerPlant().getMountingPosition().get(i) == EngineMountingPositionEnum.REAR_FUSELAGE) {
					xCGPercentageList.add(0.45);
				}
			}
			
			/* 
			 * The XCG position is the average value of percentages with respect to each engine mass
			 */
			double sum = engineMassList.stream().mapToDouble(mass -> mass.doubleValue(SI.KILOGRAM)).sum();
			double prod = 0.0;
			for(int i=0; i<aircraft.getPowerPlant().getEngineNumber(); i++) {
				prod += engineMassList.get(i).doubleValue(SI.KILOGRAM)* xCGPercentageList.get(i);
			}
			
			double overallPercentage = prod/sum;
			_xCG = aircraft.getFuselage().getFuselageLength().to(SI.METER).times(overallPercentage);
			
		}
		
		return _xCG;
		
	}
	
}
