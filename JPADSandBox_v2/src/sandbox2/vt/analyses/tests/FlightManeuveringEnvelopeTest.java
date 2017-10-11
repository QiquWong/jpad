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
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.RegulationsEnum;

public class FlightManeuveringEnvelopeTest {

	// EXAMPLE FROM M. Sadraey, Aircraft Performance Analysis, VDM Verlag Dr. Müller, 2009 PAG.9
	
	public static void main(String[] args) {
		
		MyConfiguration.initWorkingDirectoryTree();
		
		System.out.println("-----------------------------------------------------------------------");
		System.out.println(" JPAD TEST :: FLIGHT MANEUVERING ENVELOPE");
		System.out.println("-----------------------------------------------------------------------\n");
		
		// DATA FAR-25:
		Amount<Mass> maxTakeOffMass = Amount.valueOf(22303, SI.KILOGRAM);	
		Amount<Mass> maxLandingMass = Amount.valueOf(20073, SI.KILOGRAM);
		Amount<Area> wingSurface = Amount.valueOf(64.07, SI.SQUARE_METRE);
		double cLMaxClean = 1.675;
		double cLMaxFullFlap = 2.545;
		double cLMaxInverted = -1;
		Amount<Length> meanAerodynamicChord = Amount.valueOf(2.37, SI.METER);
		Amount<?> cLAlpha = Amount.valueOf(5.5, SI.RADIAN.inverse());
		double positiveLimitLoadFactor = 2.5;
		double negativeLimitLoadFactor = -1;
		Amount<Velocity> cruisingSpeed = Amount.valueOf(151.6, SI.METERS_PER_SECOND);
		Amount<Velocity> diveSpeed = Amount.valueOf(189.5, SI.METERS_PER_SECOND);
		Amount<Length> altitude = Amount.valueOf(6000, SI.METER);
		RegulationsEnum regulation = RegulationsEnum.FAR_25;
		AircraftTypeEnum aircraftType = AircraftTypeEnum.TURBOPROP;
		boolean createCSV = Boolean.TRUE;
		
		// DATA FAR-23:
//		Amount<Mass> maxTakeOffMass = Amount.valueOf(3290, SI.KILOGRAM);	
//		Amount<Mass> maxLandingMass = Amount.valueOf(2961, SI.KILOGRAM);
//		Amount<Area> wingSurface = Amount.valueOf(25.4, SI.SQUARE_METRE);
//		double cLMaxClean = 1.47;
//		double cLMaxFullFlap = 2.0;
//		double cLMaxInverted = -0.8;
//		Amount<Length> meanAerodynamicChord = Amount.valueOf(1.87, SI.METER);
//		Amount<?> cLAlpha = Amount.valueOf(4.985, SI.RADIAN).inverse();
//		double positiveLimitLoadFactor = 3.491;
//		double negativeLimitLoadFactor = -1.396;
//		Amount<Velocity> cruisingSpeed = null;
//		Amount<Velocity> diveSpeed = null;
//		Amount<Length> altitude = Amount.valueOf(6096, SI.METER);
//		RegulationsEnum regulation = RegulationsEnum.FAR_23;
//		AircraftTypeEnum aircraftType = AircraftTypeEnum.GENERAL_AVIATION;
		
		// CALCULATOR OBJECT:
		FlightManeuveringEnvelopeCalc theCalculator = new FlightManeuveringEnvelopeCalc(
				regulation,
				aircraftType,
				createCSV,
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
		theCalculator.plotManeuveringEnvelope(MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR));
		
		// PRINT RESULTS:
		System.out.println(theCalculator.toString());
		
		System.out.println("-----------------------------------------------------------------------");
		System.out.println(" DONE !! ");
		System.out.println("-----------------------------------------------------------------------");
	}
	
}
