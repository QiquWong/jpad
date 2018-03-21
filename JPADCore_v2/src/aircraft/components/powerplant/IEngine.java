package aircraft.components.powerplant;

import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.unit.SI;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineTypeEnum;

@FreeBuilder
public interface IEngine {
	
	String getId();
	EngineTypeEnum getEngineType();
	String getEngineDatabaseName();
	Amount<Length> getLength();
	//------------------------------------------
	// only for propeller driven engines
	Amount<Length> getPropellerDiameter();
	int getNumberOfBlades();
	double getEtaPropeller();
	Amount<Power> getStaticPower();
	//------------------------------------------
	double getBpr();
	Amount<Force> getStaticThrust();
	Amount<Mass> getDryMassPublicDomain(); 
	int getNumberOfCompressorStages();
	int getNumberOfShafts(); 
	double getOverallPressureRatio();

	class Builder extends IEngine_Builder {
		public Builder () {
			
			// initializing values
			setPropellerDiameter(Amount.valueOf(0.0, SI.METER));
			setNumberOfBlades(0);
			setEtaPropeller(0.0);
			setStaticPower(Amount.valueOf(0.0, SI.WATT));
			setBpr(0.0);
			setStaticThrust(Amount.valueOf(0.0, SI.NEWTON));
			setNumberOfCompressorStages(0);
			setNumberOfShafts(0);
			setOverallPressureRatio(0.0);
		}
	}

}
