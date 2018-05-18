package sandbox2.cavas;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import database.databasefunctions.DatabaseReader;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import standaloneutils.database.hdf.MyHDFReader;

public class Test_01 {

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("CAVAS Test");
		System.out.println("-------------------");
		
		// Import the aircraft
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		System.out.println("-------------------");
		System.out.println("Aircraft read");
		System.out.println("-------------------");
		
		LiftingSurface wing = aircraft.getWing();
		
		System.out.println(wing);
		
		System.out.println("-------------------");
		System.out.println("Main wing read");
		System.out.println("-------------------");
		
		AerodynamicDatabaseReader databaseReader =  wing.getAeroDatabaseReader();

		double taperRatioWing = wing.getEquivalentWing().getPanels().get(0).getTaperRatio();
		double aspectRatioWing = wing.getAspectRatio();
		Amount<Angle> sweepAngleC2Wing = wing.getEquivalentWing().getPanels().get(0).getSweepHalfChord();
		
		System.out.println(">> taper ratio: " + taperRatioWing);
		System.out.println(">> aspect ratio: " + aspectRatioWing);
		System.out.println(">> sweep angle @ c/2: " + sweepAngleC2Wing);
		
		double cRollBetaOverCL1WingBody = databaseReader.getClbetaWBClbetaOverCLift1Lc2VsLc2ARlambda(taperRatioWing, aspectRatioWing, sweepAngleC2Wing); // var0, var1, var2
		
		System.out.println(">> Cl_beta/CL1 : " + cRollBetaOverCL1WingBody);
		
	}

}
