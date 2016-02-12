package sandbox.vc;

import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.components.Aircraft;
import sandbox.vt.PayloadRange_Test.PayloadRangeCalc;
import standaloneutils.atmosphere.AtmosphereCalc;

public class Test_AC_AGILE_01 {
	
	
	public static void main(String[] args) {
		
		Amount<Length> range = Amount.valueOf(1890, NonSI.MILE);
		Amount<Mass> paxSingleMass = Amount.valueOf(225, NonSI.POUND);
		
		
		
		// Initialize Aircraft with default parameters
		Aircraft aircraft = Aircraft.createDefaultAircraft("B747-100B"); //("ATR-72")
				
		OperatingConditions operatingConditions = new OperatingConditions();
		operatingConditions.set_altitude(Amount.valueOf(11000, SI.METER));
		operatingConditions.set_machCurrent(0.78);
		Amount<Mass> MTOM = Amount.valueOf(25330, NonSI.POUND);
		aircraft.get_weights().set_MTOM(MTOM);
		aircraft.get_performances().set_range(range);
		aircraft.get_weights().set_paxSingleMass(paxSingleMass);
		
		Amount<Force> _MLW = MTOM.times(0.9).times(AtmosphereCalc.g0).to(SI.NEWTON);
		aircraft.get_weights().set_MLW(_MLW);
		
		Amount<Length> diam_C_MAX = Amount.valueOf(3, SI.METER);
		aircraft.get_fuselage().set_diam_C_MAX(diam_C_MAX );
		
	}

}
