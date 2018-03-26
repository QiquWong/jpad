package calculators.balance;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

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
				/ aircraft.getFuselage().getFuselageFinenessRatio()
				* aircraft.getFuselage().getNoseFinenessRatio()
				+ (aircraft.getFuselage().getFuselageFinenessRatio() - 5.)
				/ 1.8, 
				SI.METER
				);
	}
	
	public static Amount<Length> calculateFuselageXCGTorenbeek (Aircraft aircraft) {
		
		Amount<Length> _xCG = Amount.valueOf(0.0, SI.METER);
		
		if (aircraft.getPowerPlant().getEngineNumber() == 1 && 
				(aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.PISTON |
				aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.TURBOPROP)) {

			_xCG = aircraft.getFuselage().getFuselageLength().to(SI.METER).times(0.335);
		}

		if (aircraft.getPowerPlant().getMountingPosition() == EngineMountingPositionEnum.WING) {
			if ((aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.PISTON |
					aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.TURBOPROP)) {
				_xCG = aircraft.getFuselage().getFuselageLength().to(SI.METER).times(0.39); 
			} else {
				_xCG = aircraft.getFuselage().getFuselageLength().to(SI.METER).times(0.435);
			}
		}

		if (aircraft.getPowerPlant().getMountingPosition() == EngineMountingPositionEnum.REAR_FUSELAGE) {
			_xCG = aircraft.getFuselage().getFuselageLength().to(SI.METER).times(0.47);
		}

		if (aircraft.getPowerPlant().getMountingPosition() == EngineMountingPositionEnum.BURIED) {
			_xCG = aircraft.getFuselage().getFuselageLength().to(SI.METER).times(0.45);
		}
		
		return _xCG;
		
	}
	
}
