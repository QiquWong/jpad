package analyses;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

@FreeBuilder
public interface IOperatingConditions {

	String getID();
	
	// CLIMB
	Amount<Angle> getAlphaClimb();
	Amount<Angle> getBetaClimb();
	double getMachClimb();
	Amount<Length> getAltitudeClimb();
	Amount<Temperature> getDeltaTemperatureClimb();
	double getThrottleClimb();
	
	// CRUISE
	Amount<Angle> getAlphaCruise();
	Amount<Angle> getBetaCruise();
	double getMachCruise();
	Amount<Length> getAltitudeCruise();
	Amount<Temperature> getDeltaTemperatureCruise();
	double getThrottleCruise();
	
	// TAKE-OFF
	Amount<Angle> getAlphaTakeOff();
	Amount<Angle> getBetaTakeOff();
	double getMachTakeOff();
	Amount<Length> getAltitudeTakeOff();
	Amount<Temperature> getDeltaTemperatureTakeOff();
	double getThrottleTakeOff();
	List<Amount<Angle>> getTakeOffFlapDefletctionList();
	List<Amount<Angle>> getTakeOffSlatDefletctionList();
	List<Amount<Angle>> getTakeOffCanardDefletction();
	
	// LANDING
	Amount<Angle> getAlphaLanding();
	Amount<Angle> getBetaLanding();
	double getMachLanding();
	Amount<Length> getAltitudeLanding();
	Amount<Temperature> getDeltaTemperatureLanding();
	double getThrottleLanding();
	List<Amount<Angle>> getLandingFlapDefletctionList();
	List<Amount<Angle>> getLandingSlatDefletctionList();
	List<Amount<Angle>> getLandingCanardDefletction();
	
	class Builder extends IOperatingConditions_Builder {
		public Builder() {
			
		}
	}
	
}
