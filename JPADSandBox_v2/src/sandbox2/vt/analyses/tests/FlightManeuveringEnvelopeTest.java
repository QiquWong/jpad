package sandbox2.vt.analyses.tests;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import calculators.performance.FlightManeuveringEnvelopeCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.RegulationsEnum;

public class FlightManeuveringEnvelopeTest {

	// EXAMPLE FROM M. Sadraey, Aircraft Performance Analysis, VDM Verlag Dr. Müller, 2009 PAG.9
	
	public static void main(String[] args) {
		
		System.out.println("-----------------------------------------------------------------------");
		System.out.println(" JPAD TEST :: FLIGHT MANEUVERING ENVELOPE");
		System.out.println("-----------------------------------------------------------------------\n");
		
		// DATA:
		Amount<Mass> maxTakeOffMass = Amount.valueOf(22303, SI.KILOGRAM);	
		Amount<Mass> maxLandingMass = Amount.valueOf(20073, SI.KILOGRAM);
		Amount<Area> wingSurface = Amount.valueOf(64.07, SI.SQUARE_METRE);
		double cLMaxClean = 1.675;
		double cLMaxFullFlap = 2.545;
		double cLMaxInverted = -1;
		Amount<Length> meanAerodynamicChord = Amount.valueOf(2.37, SI.METER);
		Amount<?> cLAlpha = Amount.valueOf(5.5, SI.RADIAN).inverse();
		double positiveLimitLoadFactor = 2.5;
		double negativeLimitLoadFactor = -1;
		Amount<Velocity> cruisingSpeed = Amount.valueOf(151.6, SI.METERS_PER_SECOND);
		Amount<Velocity> diveSpeed = Amount.valueOf(189.5, SI.METERS_PER_SECOND);
		Amount<Length> altitude = Amount.valueOf(6096, SI.METER);
		RegulationsEnum regulation = RegulationsEnum.FAR_25;
		AircraftTypeEnum aircraftType = AircraftTypeEnum.TURBOPROP;
		
		// CALCULATOR OBJECT:
		FlightManeuveringEnvelopeCalc theCalculator = new FlightManeuveringEnvelopeCalc(
				regulation,
				aircraftType,
				cLMaxClean,
				cLMaxFullFlap,
				cLMaxInverted,
				positiveLimitLoadFactor,
				negativeLimitLoadFactor,
				cruisingSpeed,
				diveSpeed,
				cLAlpha,
				meanAerodynamicChord,
				altitude,
				maxTakeOffMass,
				maxLandingMass,
				wingSurface
				);
		theCalculator.calculateManeuveringEnvelope();
		
		// PRINT RESULTS:
		MyConfiguration.customizeAmountOutput();
		
		System.out.println("-----------------------------------------------------------------------");
		System.out.println(" REGULATION : " + regulation);
		System.out.println(" AIRCRAFT TYPE : " + aircraftType);
		System.out.println("-----------------------------------------------------------------------");
		
		System.out.println("\n-----------------------------------------------------------------------");
		System.out.println(" BASIC MANEUVERING DIAGRAM");
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("Stall speed clean = " + theCalculator.getStallSpeedClean());
		System.out.println("Stall speed inverted = " + theCalculator.getStallSpeedInverted());
		System.out.println(".......................................................................");
		System.out.println("Maneuvering speed = " + theCalculator.getManeuveringSpeed());
		System.out.println("Positive limit load factor maneuvering speed = " + theCalculator.getPositiveLimitLoadFactor());
		System.out.println(".......................................................................");
		System.out.println("Cruising speed = " + theCalculator.getCruisingSpeed());
		System.out.println("Positive limit load factor cruising speed = " + theCalculator.getPositiveLimitLoadFactor());
		System.out.println(".......................................................................");
		System.out.println("Dive speed = " + theCalculator.getDiveSpeed());
		System.out.println("Positive limit load factor dive speed = " + theCalculator.getPositiveLimitLoadFactor());
		System.out.println(".......................................................................");
		System.out.println("Dive speed = " + theCalculator.getDiveSpeed());
		System.out.println("Negative limit load factor dive speed = " + theCalculator.getNegativeLimitLoadFactor());
		System.out.println(".......................................................................");
		System.out.println("Cruising speed = " + theCalculator.getCruisingSpeed());
		System.out.println("Negative limit load factor cruising speed = " + theCalculator.getNegativeLimitLoadFactor());
		System.out.println(".......................................................................");
		System.out.println("Maneuvering speed = " + theCalculator.getManeuveringSpeed());
		System.out.println("Negative limit load factor maneuvering speed = " + theCalculator.getNegativeLimitLoadFactor());
		System.out.println("\n-----------------------------------------------------------------------");
		System.out.println(" FLAP CURVE ");
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("Stall speed full flap = " + theCalculator.getStallSpeedFullFlap());
		System.out.println(".......................................................................");
		System.out.println("Maneuvering speed full flap = " + theCalculator.getManeuveringFlapSpeed());
		System.out.println("Positive limit load factor full flap maneuvering speed = " + theCalculator.getPositiveLoadFactorDesignFlapSpeed());
		System.out.println(".......................................................................");
		System.out.println("Design speed full flap = " + theCalculator.getDesignFlapSpeed());
		System.out.println("Positive limit load factor full flap maneuvering speed = " + theCalculator.getPositiveLoadFactorDesignFlapSpeed());
		System.out.println("\n-----------------------------------------------------------------------");
		System.out.println(" GUST MODIFICATION ");
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("Maneuvering speed = " + theCalculator.getManeuveringSpeed());
		System.out.println("Positive limit load factor maneuvering speed (GUST) = " + theCalculator.getPositiveLoadFactorManeuveringSpeed());
		System.out.println(".......................................................................");
		System.out.println("Cruising speed = " + theCalculator.getCruisingSpeed());
		System.out.println("Positive limit load factor cruising speed (GUST) = " + theCalculator.getPositiveLoadFactorCruisingSpeed());
		System.out.println(".......................................................................");
		System.out.println("Dive speed = " + theCalculator.getDiveSpeed());
		System.out.println("Positive limit load factor dive speed (GUST) = " + theCalculator.getPositiveLoadFactorDiveSpeed());
		System.out.println(".......................................................................");
		System.out.println("Dive speed = " + theCalculator.getDiveSpeed());
		System.out.println("Negative limit load factor dive speed (GUST) = " + theCalculator.getNegativeLoadFactorDiveSpeed());
		System.out.println(".......................................................................");
		System.out.println("Cruising speed = " + theCalculator.getCruisingSpeed());
		System.out.println("Negative limit load factor cruising speed (GUST) = " + theCalculator.getNegativeLoadFactorCruisingSpeed());
		System.out.println(".......................................................................");
		System.out.println("Maneuvering speed = " + theCalculator.getManeuveringSpeed());
		System.out.println("Negative limit load factor maneuvering speed (GUST) = " + theCalculator.getNegativeLoadFactorManeuveringSpeed());
		System.out.println("\n-----------------------------------------------------------------------");
		System.out.println(" DONE !! ");
		System.out.println("-----------------------------------------------------------------------");
	}
	
}
