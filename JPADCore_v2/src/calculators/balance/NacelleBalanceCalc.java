package calculators.balance;

import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;

public class NacelleBalanceCalc {

	public static Amount<Length> calculateNacelleXCGTorenbeek1976 (Aircraft aircraft) {
		
		return aircraft.getNacelles().getNacellesList().get(0).getLength().times(0.4);
		
	}

	// TODO: ADD OTHER IF NEEDED
	
}
