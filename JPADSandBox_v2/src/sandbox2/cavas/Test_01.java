package sandbox2.cavas;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.OperatingConditions;
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
		OperatingConditions operatingConditions = AircraftUtils.importOperatingCondition(args);
		
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
		System.out.println(">> sweep angle @ c/2 (deg): " + sweepAngleC2Wing.doubleValue(NonSI.DEGREE_ANGLE));
		
		double cRollBetaOverCL1WingBody = databaseReader.getClbetaWBClbetaOverCLift1Lc2VsLc2ARlambda(
				taperRatioWing, // var0, var1, var2 
				aspectRatioWing, 
				sweepAngleC2Wing
		);

		double cRollBetaOverGammaW = databaseReader.getClbetaWBClbetaOverGammaWVsARLc2lambda(
				taperRatioWing, // var0, var1, var2 
				sweepAngleC2Wing,
				aspectRatioWing
		);

		double aspectRatioOverCosSweepAngleC2 = aspectRatioWing/Math.cos(sweepAngleC2Wing.doubleValue(SI.RADIAN));
		double mach = operatingConditions.getMachCruise();
		double machTimesCosSweepAngleC2 = mach * Math.cos(sweepAngleC2Wing.doubleValue(SI.RADIAN));
		
		System.out.println(">> Mach: " + mach);
		System.out.println(">> AR cos(Lambda_c/2): " + aspectRatioOverCosSweepAngleC2);
		System.out.println(">> Mach cos(Lambda_c/2): " + machTimesCosSweepAngleC2);
		
		double cRollKappaMachLambda = databaseReader.getClbetaWBKMLVsMachTimesCosLc2AROverCosLc2(
				aspectRatioOverCosSweepAngleC2, // var0, var1
				machTimesCosSweepAngleC2
				);
		
		double twistDeg = wing.getEquivalentWing().getPanels().get(0).getTwistAerodynamicAtTip().doubleValue(NonSI.DEGREE_ANGLE);
		double twistRad = wing.getEquivalentWing().getPanels().get(0).getTwistAerodynamicAtTip().doubleValue(SI.RADIAN);
		
		System.out.println(">> Cl_beta/CL1:    " + cRollBetaOverCL1WingBody);
		System.out.println(">> Cl_beta/GammaW: " + cRollBetaOverGammaW);
		System.out.println(">> K_M_Lambda:     " + cRollKappaMachLambda);
		
		
	}

}
