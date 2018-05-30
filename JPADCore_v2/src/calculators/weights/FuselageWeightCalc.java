package calculators.weights;

import static java.lang.Math.pow;
import static java.lang.Math.tan;

import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import analyses.OperatingConditions;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.LandingGearsMountingPositionEnum;
import configuration.enumerations.NacelleMountingPositionEnum;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;

public class FuselageWeightCalc {

	/* 
	 * page 150 Jenkinson - Civil Jet Aircraft Design
	 */
	public static Amount<Mass> calculateFuselageMassJenkinson (
			Aircraft aircraft
			) {
		
		double k = 0.;

		if (aircraft.getFuselage().getPressurized() == true) {
			k = k + 0.08;
		}

		// NACELLE ON THE H-TAIL IS ASSUMED TO INCREASE THE AIRCRAFT WEIGHT AS THEY ARE MOUNTED ON THE FUSELAGE
		if (aircraft.getNacelles().getNacellesList().get(0).getMountingPosition() == NacelleMountingPositionEnum.FUSELAGE
				|| aircraft.getNacelles().getNacellesList().get(0).getMountingPosition() == NacelleMountingPositionEnum.HTAIL) {
			k = k + 0.04;
		}

		if (aircraft.getLandingGears().getMountingPosition() == LandingGearsMountingPositionEnum.FUSELAGE) {
			k = k + 0.07;
		}

		return Amount.valueOf(0.039
				* (1 + k) 
				* Math.pow( 2
						* aircraft.getFuselage().getFuselageLength().doubleValue(SI.METER)
						* aircraft.getFuselage().getEquivalentDiameterCylinderGM().doubleValue(SI.METER)
						* Math.pow(
								aircraft.getTheAnalysisManager().getVDiveEAS().doubleValue(SI.METERS_PER_SECOND),
								0.5
								),
						1.5),
				SI.KILOGRAM
				);
	}
	
	/*
	 * Nicolai, L. M., and Charichner, G. E., Fundamentals of Aircraft and Airship Design, AIAA Education Series, AIAA, Reston, VA, 2010, pp. 20-1–20-25.
	 */
	public static Amount<Mass> calculateFuselageMassNicolai (Aircraft aircraft) {
		
		return Amount.valueOf(
				0.0737*
				pow(2*aircraft.getFuselage().getEquivalentDiameterCylinderGM().doubleValue(SI.METER)
						* pow(aircraft.getTheAnalysisManager().getVDiveEAS().doubleValue(SI.METERS_PER_SECOND), 0.338)  
						* pow(aircraft.getFuselage().getFuselageLength().doubleValue(SI.METER), 0.857)
						* pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)
								* aircraft.getTheAnalysisManager().getNUltimate(),
								0.286),
						1.1),
				SI.KILOGRAM);
		
	}
	
	/*
	 * page 92 Roskam part V page 92 (pdf) (Nicolai 2013 is the same pag 555)
	 */
	public static Amount<Mass> calculateFuselageMassRoskam (Aircraft aircraft) {
		
		double Kinlet = 1.0;
		
		return Amount.valueOf(
				2*10.43
				* pow(Kinlet, 1.42)
				* pow(aircraft.getTheAnalysisManager().getMaxDynamicPressure().to(MyUnits.LB_FT2).getEstimatedValue()/100,
						0.283)
				* pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND)/1000,
						0.95)
				* pow(aircraft.getFuselage().getFuselageLength().doubleValue(SI.METER)/
						aircraft.getFuselage().getSectionCylinderHeight().doubleValue(SI.METER),
						0.71), 
				NonSI.POUND
				)
				.to(SI.KILOGRAM);
		
	}
	
	/*
	 * page 403 Raymer - Aircraft Design a conceptual approach
	 */
	public static Amount<Mass> calculateFuselageMassRaymer (Aircraft aircraft) {
		
		double kdoor = 1.0;
		double klg = 1.12;
		double kws = 0.75
				* ((1+2*aircraft.getWing().getEquivalentWing().getPanels().get(0).getTaperRatio())
						/ (1+aircraft.getWing().getEquivalentWing().getPanels().get(0).getTaperRatio()))
				* aircraft.getWing().getSpan().doubleValue(NonSI.FOOT)
				* tan(aircraft.getWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN))
				/ aircraft.getFuselage().getFuselageLength().doubleValue(NonSI.FOOT);

		return Amount.valueOf(
				0.328 * kdoor * klg
				* pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND)
						* (aircraft.getTheAnalysisManager().getNUltimate()), 
						0.5)
				* pow(aircraft.getFuselage().getFuselageLength().doubleValue(NonSI.FOOT),
						0.25)
				* pow(aircraft.getFuselage().getSWetTotal().doubleValue(MyUnits.FOOT2),
						0.302)
				* pow(1 + kws, 0.04)
				* pow(aircraft.getFuselage().getFuselageLength().doubleValue(NonSI.FOOT)
						/ aircraft.getFuselage().getEquivalentDiameterCylinderGM().doubleValue(NonSI.FOOT),
						0.1), 
				NonSI.POUND)
				.to(SI.KILOGRAM);
	}

	/*
	 * pag 236 of Advanced Aircraft Design
	 */
	public static Amount<Mass> calculateFuselageMassTorenbeek2013 (Aircraft aircraft) {
		return Amount.valueOf(
				(60*pow(aircraft.getFuselage().getEquivalentDiameterCylinderGM().doubleValue(SI.METER),2)
						* (aircraft.getFuselage().getFuselageLength().doubleValue(SI.METER) + 1.5)
				+ 160*pow(aircraft.getTheAnalysisManager().getNUltimate(), 0.5)
						* aircraft.getFuselage().getEquivalentDiameterCylinderGM().doubleValue(SI.METER)
						* aircraft.getFuselage().getFuselageLength().doubleValue(SI.METER)
						),
				SI.NEWTON)
				.divide(AtmosphereCalc.g0)
				.to(SI.KILOGRAM);

	}
	
	/*
	 * page 302 of Synthesis of subsonic airplane design 1976
	 */
	public static Amount<Mass> calculateFuselageMassTorenbeek1976 (Aircraft aircraft) {
		double k = 0.;
		if (aircraft.getFuselage().getPressurized()) {k = k + 0.08;}
		if (aircraft.getLandingGears().getMountingPosition().equals(LandingGearsMountingPositionEnum.FUSELAGE)) {
			k = k + 0.07;
		}

		return Amount.valueOf((1 + k) * 0.23 * 
				Math.sqrt(
						aircraft.getTheAnalysisManager().getVDiveEAS().doubleValue(SI.METERS_PER_SECOND) 
						* aircraft.getHTail().getLiftingSurfaceACToWingACdistance().doubleValue(SI.METER)
						/ (2*aircraft.getFuselage().getEquivalentDiameterCylinderGM().doubleValue(SI.METER))
						) 
				* Math.pow(aircraft.getFuselage().getSWetTotal().doubleValue(SI.SQUARE_METRE), 1.2),
				SI.KILOGRAM
				);
	}
	
	/*
	 * page 585 Sadray Aircraft Design System Engineering Approach
	 * 18 % average difference from actual value
	 */
	public static Amount<Mass> calculateFuselageMassSadray (Aircraft aircraft) {

		double Kinlet = 1.;
		double kRho = 0.0032;
		return Amount.valueOf(
				aircraft.getFuselage().getFuselageLength().doubleValue(SI.METER)
				* pow(aircraft.getFuselage().getEquivalentDiameterCylinderGM().doubleValue(SI.METER),2)
				* aircraft.getTheAnalysisManager().getTheWeights().getMaterialDensity().getEstimatedValue()
				* kRho
				* pow(aircraft.getTheAnalysisManager().getNUltimate(),0.25)
				* Kinlet,
				SI.KILOGRAM
				);
		
	}
	
	/*
	 * page 432 Stanford University pdf
	 */
	public static Amount<Mass> calculateFuselageMassKroo (Aircraft aircraft, OperatingConditions operatingConditions) {
		
		double ifuse = 0.0;
		double ip = 1.5e-3 
				* operatingConditions.getMaxDeltaPressureCruise().doubleValue(MyUnits.LB_FT2)
				* aircraft.getFuselage().getSectionCylinderWidth().doubleValue(NonSI.FOOT);

		double ib = 0.0;
		if(aircraft.getPowerPlant().getMountingPosition().equals(EngineMountingPositionEnum.WING))
			ib = 1.91e-4 
			* aircraft.getTheAnalysisManager().getPositiveLimitLoadFactor() 
			* (aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().doubleValue(NonSI.POUND)
					- aircraft.getTheAnalysisManager().getTheWeights().getWingMass().doubleValue(NonSI.POUND)
					- aircraft.getTheAnalysisManager().getTheWeights().getNacellesMass().doubleValue(NonSI.POUND)
					- aircraft.getTheAnalysisManager().getTheWeights().getPowerPlantMass().doubleValue(NonSI.POUND)
					)
			* aircraft.getFuselage().getFuselageLength().doubleValue(NonSI.FOOT) 
			/ pow(aircraft.getFuselage().getSectionCylinderHeight().doubleValue(NonSI.FOOT), 2);
		else
			ib = 1.91e-4 
			* aircraft.getTheAnalysisManager().getPositiveLimitLoadFactor() 
			* (aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().doubleValue(NonSI.POUND)
					- aircraft.getTheAnalysisManager().getTheWeights().getWingMass().doubleValue(NonSI.POUND)
					)
			* aircraft.getFuselage().getFuselageLength().doubleValue(NonSI.FOOT) 
			/ pow(aircraft.getFuselage().getSectionCylinderHeight().doubleValue(NonSI.FOOT),2);

		if (ip > ib) {
			ifuse = ip;
		} else {
			ifuse = (Math.pow(ip,2) + Math.pow(ib,2))/(2*ib); 
		}

		return Amount.valueOf(
				(1.051 + 0.102*ifuse)
				* aircraft.getFuselage().getSWetTotal().doubleValue(MyUnits.FOOT2),
				NonSI.POUND)
				.to(SI.KILOGRAM);
		
	}
	
}
