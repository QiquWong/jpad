package calculators.balance;

import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;

public class EngineBalanceCalc {

	public static Amount<Length> calculateEngineXCGSforza (Aircraft aircraft) {

		return aircraft.getPowerPlant().getEngineList().get(0).getLength().times(0.5);

	}

	// TODO: ADD OTHER IF NEEDED

}
