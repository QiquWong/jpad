package calculators.balance;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;

public class LandingGearsBalanceCalc {

	public static Amount<Length> calculateXCGLandingGears (Aircraft aircraft) {
		
		return Amount.valueOf(
				(
						(aircraft.getLandingGears().getXApexConstructionAxesMainGear().doubleValue(SI.METER)
						* aircraft.getLandingGears().getTheWeigths().getMainGearMassEstimated().doubleValue(SI.KILOGRAM))
						+ (aircraft.getLandingGears().getXApexConstructionAxesNoseGear().doubleValue(SI.METER)
						* aircraft.getLandingGears().getTheWeigths().getFrontGearMassEstimated().doubleValue(SI.KILOGRAM))
						)
				/aircraft.getLandingGears().getTheWeigths().getMassEstimated().doubleValue(SI.KILOGRAM),
				SI.METER
				);
		
	}
	
	public static Amount<Length> calculateZCGLandingGears (Aircraft aircraft) {
		
		Amount<Length> zCGNoseLeg = aircraft.getLandingGears().getMainLegsLenght().divide(2);
		Amount<Length> zCGMainLeg = aircraft.getLandingGears().getMainLegsLenght().divide(2);
		Amount<Length> zCGNoseWheel = aircraft.getLandingGears().getFrontalWheelsHeight().divide(2)
										.plus(aircraft.getLandingGears().getMainLegsLenght());
		Amount<Length> zCGMainWheel = aircraft.getLandingGears().getRearWheelsHeight().divide(2)
										.plus(aircraft.getLandingGears().getMainLegsLenght());
		Amount<Length> noseLegLength = aircraft.getLandingGears().getMainLegsLenght();
		Amount<Length> mainLegLength = aircraft.getLandingGears().getMainLegsLenght();
		Amount<Length> noseWheelHeight = aircraft.getLandingGears().getFrontalWheelsHeight();
		Amount<Length> mainWheelHeight = aircraft.getLandingGears().getRearWheelsHeight();
		
		return	aircraft.getLandingGears().getZApexConstructionAxesMainGear().minus(
				Amount.valueOf(
						((zCGNoseLeg.times(noseLegLength))
								.plus((zCGNoseWheel).times(noseWheelHeight))
								.plus((zCGMainLeg).times(mainLegLength))
								.plus((zCGMainWheel).times(mainWheelHeight)))
						.divide(noseLegLength
								.plus(mainLegLength)
								.plus(noseWheelHeight)
								.plus(mainWheelHeight)
								)
						.getEstimatedValue(),
						SI.METER
						)
				);
		
	}
	
}
