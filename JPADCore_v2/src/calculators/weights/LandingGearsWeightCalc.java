package calculators.weights;

import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import configuration.enumerations.EngineTypeEnum;
import standaloneutils.MyMathUtils;

public class LandingGearsWeightCalc {

	/*
	 * Torenbeek: SYNTHESIS OF SUBSONIC AIRPLANE DESIGN pag. 282 - 283 
	 */
	public static Amount<Mass> calculateMainGearMassTorenbeek1976 (Aircraft aircraft) {
		
		double[] kucArray = new double[] {1.0, 1.08};
		double[] wingPositionArray = new double[] {-1.0, 1.0};
		
		double kuc = MyMathUtils.getInterpolatedValue1DLinear(
				wingPositionArray,
				kucArray,
				aircraft.getWing().getPositionRelativeToAttachment()
				);
		
		double a = 0.0;
		double b = 0.0;
		double c = 0.0;
		double d = 0.0;
		
		if(aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) 
				|| aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET)) {
		 
			a = 33.0;
			b = 0.04;
			c = 0.021;
			d = 0.0;
			
		}
		else {
			
			a = 40.0;
			b = 0.16;
			c = 0.019;
			d = 1.5e-5;
			
		}
		
		return Amount.valueOf(
				kuc*(a 
						+ (b * Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND), 0.75))
						+ (c * aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND))
						+ (d * Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND), 1.5)) 
						),
				NonSI.POUND
				).to(SI.KILOGRAM);


	}
	
	/*
	 * Torenbeek: SYNTHESIS OF SUBSONIC AIRPLANE DESIGN pag. 282 - 283 
	 */
	public static Amount<Mass> calculateFrontGearMassTorenbeek1976 (Aircraft aircraft) {
		
		double[] kucArray = new double[] {1.0, 1.08};
		double[] wingPositionArray = new double[] {-1.0, 1.0};
		
		double kuc = MyMathUtils.getInterpolatedValue1DLinear(
				wingPositionArray,
				kucArray,
				aircraft.getWing().getPositionRelativeToAttachment()
				);
		
		double a = 0.0;
		double b = 0.0;
		double c = 0.0;
		double d = 0.0;
		
		if(aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) 
				|| aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET)) {
		 
			a = 12.0;
			b = 0.06;
			c = 0.0;
			d = 0.0;
			
		}
		else {
			
			a = 20.0;
			b = 0.10;
			c = 0.0;
			d = 2e-6;
			
		}
		
		return Amount.valueOf(
				kuc*(a 
						+ (b * Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND), 0.75))
						+ (c * aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND))
						+ (d * Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND), 1.5)) 
						),
				NonSI.POUND
				).to(SI.KILOGRAM);

	}
 	
}
