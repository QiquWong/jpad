package sandbox2.cavas;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.OperatingConditions;
import database.databasefunctions.DatabaseReader;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import igeo.IVec2R.Len;
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
		
		// Calculation of Cl_beta_WB		
		LiftingSurface wing = aircraft.getWing();
		
		System.out.println(wing);
		
		System.out.println("-------------------");
		System.out.println("Main wing read");
		System.out.println("-------------------");
		
		AerodynamicDatabaseReader databaseReader =  wing.getAeroDatabaseReader();

		// Cl_beta/CL1_Lambda_c/2
		double taperRatioWing = wing.getEquivalentWing().getPanels().get(0).getTaperRatio();
		double aspectRatioWing = wing.getAspectRatio();
		Amount<Angle> sweepAngleC2Wing = wing.getEquivalentWing().getPanels().get(0).getSweepHalfChord();
		
		System.out.println(">> taper ratio: " + taperRatioWing);
		System.out.println(">> aspect ratio: " + aspectRatioWing);
		System.out.println(">> sweep angle @ c/2 (deg): " + sweepAngleC2Wing.doubleValue(NonSI.DEGREE_ANGLE));

		Amount<?> cRollBetaOverCL1WingBodyLc2 = Amount.valueOf(databaseReader.getClbetaWBClbetaOverCLift1Lc2VsLc2ARlambda(taperRatioWing, aspectRatioWing, sweepAngleC2Wing), NonSI.DEGREE_ANGLE.inverse()); // var0, var1, var2 

		// K_M_Lambda
		double aspectRatioOverCosSweepAngleC2 = aspectRatioWing/Math.cos(sweepAngleC2Wing.doubleValue(SI.RADIAN));
		double mach = operatingConditions.getMachCruise();
		double machTimesCosSweepAngleC2 = mach * Math.cos(sweepAngleC2Wing.doubleValue(SI.RADIAN));
		
		System.out.println(">> Mach: " + mach);
		System.out.println(">> AR/cos(Lambda_c/2): " + aspectRatioOverCosSweepAngleC2);
		System.out.println(">> Mach cos(Lambda_c/2): " + machTimesCosSweepAngleC2);
		
		double cRollKappaMachLambda = databaseReader.getClbetaWBKMLVsMachTimesCosLc2AROverCosLc2(aspectRatioOverCosSweepAngleC2, machTimesCosSweepAngleC2); // var0, var1
		
		// K_f
		Amount<Length> spanWing = wing.getSpan();
		Amount<Length> a = Amount.valueOf(20.0, SI.METER); //TODO take A from aircraft
		double aOverSpanWing = a.doubleValue(SI.METER)/spanWing.doubleValue(SI.METER);
		
		System.out.println(">> A: " + a);
		System.out.println(">> b: " + spanWing);
		System.out.println(">> A/b: " + aOverSpanWing);
		
		double cRollKappaf = databaseReader.getClbetaWBKfVsAOverBAROverCosLc2(aspectRatioOverCosSweepAngleC2, aOverSpanWing); // var0, var1
		
		// Cl_beta/CL1_AR
		Amount<?> cRollBetaOverCL1WingBodyAR = Amount.valueOf(databaseReader.getClbetaWBClbetaOverCLift1ARVsARlambda(taperRatioWing, aspectRatioWing), NonSI.DEGREE_ANGLE.inverse()); // var0, var1
		
		// Cl_beta/Gamma_W
		Amount<?> cRollBetaOverGammaW = Amount.valueOf(databaseReader.getClbetaWBClbetaOverGammaWVsARLc2lambda(taperRatioWing, sweepAngleC2Wing, aspectRatioWing), NonSI.DEGREE_ANGLE.pow(2).inverse()); // var0, var1, var2
		
		// K_M_Gamma_W
		double cRollKappaMachGammaW = databaseReader.getClbetaWBKMGammaVsMachTimesCosLc2AROverCosLc2(aspectRatioOverCosSweepAngleC2, machTimesCosSweepAngleC2); // var0, var1
		
		// DeltaCl_beta/Gamma_W
		Amount<Area> SfAVG = Amount.valueOf(10.0, SI.SQUARE_METRE); //TODO take SfAVG from aircraft
		Amount<Length> dB = Amount.valueOf(Math.sqrt(4*SfAVG.doubleValue(SI.SQUARE_METRE)/Math.PI), SI.METER);
		double dBOverSpanWing = dB.doubleValue(SI.METER)/spanWing.doubleValue(SI.METER);
		
		System.out.println(">> d_B: " + dB);
		
		Amount<?> cRollDeltaClBetaOverGammaW = Amount.valueOf(-0.0005*aspectRatioWing*Math.pow(dBOverSpanWing, 2.0), NonSI.DEGREE_ANGLE.pow(2).inverse());
		
		// DCl_beta_z_W
		Amount<Length> zW = Amount.valueOf(1.0, SI.METER);
		// TODO take z_W from aircraft
		
		System.out.println(">> z_W: " + zW);
		
		Amount<?> cRollDeltaClBetaZW = Amount.valueOf((1.2*Math.sqrt(aspectRatioWing)/57.3)*(zW.doubleValue(SI.METER)/spanWing.doubleValue(SI.METER))*(2*dB.doubleValue(SI.METER)/spanWing.doubleValue(SI.METER)), NonSI.DEGREE_ANGLE.inverse());
		
		// DCl_beta/(eps_W*tan(Lambda_c/4))
		Amount<?> cRollDeltaClBetaOverEpsWTanLc4 = Amount.valueOf(databaseReader.getClbetaWBDClbetaOverEpsWTimesTanLc4VsARlambda(taperRatioWing, aspectRatioWing), NonSI.DEGREE_ANGLE.pow(2).inverse()); // var0, var1
		
		// Cl_beta_WB
		Amount<VolumetricDensity> density = operatingConditions.getDensityCruise();
		Amount<Velocity> velocityTAS = operatingConditions.getTASCruise();
		Amount<Force> weight = Amount.valueOf(1000000, SI.NEWTON);
		// TODO take the weight from aircraft. Amount<Force> weight = aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffWeight();
		Amount<Area> surfaceWing = wing.getSurfacePlanform();
		double cL1 = 2*weight.doubleValue(SI.NEWTON)/(density.doubleValue(VolumetricDensity.UNIT)*surfaceWing.doubleValue(SI.SQUARE_METRE)*Math.pow(velocityTAS.doubleValue(SI.METERS_PER_SECOND), 2));
		Amount<Angle> dihedralWing = wing.getDihedralMean();
		Amount<Angle> aerodynamicTwistWing = wing.getEquivalentWing().getPanels().get(0).getTwistAerodynamicAtTip();
		Amount<Angle> sweepAngleC4Wing = wing.getEquivalentWing().getPanels().get(0).getSweepQuarterChord();
		
		System.out.println(">> density: " + density);
		System.out.println(">> velocity: " + velocityTAS);
		System.out.println(">> weight: " + weight);
		System.out.println(">> surface wing: " + surfaceWing);
		System.out.println(">> CL_1: " + cL1);
		System.out.println(">> dihedral wing angle: " + dihedralWing);
		System.out.println(">> aerodynamic twist wing: " + aerodynamicTwistWing);
		System.out.println(">> sweep angle @ c/4: " + sweepAngleC4Wing);
		
		Amount<?> cRollBetaWB = ((cRollBetaOverCL1WingBodyLc2.times(cRollKappaMachLambda*cRollKappaf).plus(cRollBetaOverCL1WingBodyAR)).times(57.3*cL1))
				.plus((dihedralWing.times(cRollBetaOverGammaW.times(cRollKappaMachGammaW).plus(cRollDeltaClBetaOverGammaW)).plus(cRollDeltaClBetaZW)
				.plus(cRollDeltaClBetaOverEpsWTanLc4.times(aerodynamicTwistWing.times(Math.tan(sweepAngleC4Wing.doubleValue(SI.RADIAN)))))).times(57.3));
				
		System.out.println(">>>> Cl_beta/CL1_Lambda_c/2: " + cRollBetaOverCL1WingBodyLc2);
		System.out.println(">>>> K_M_Lambda: " + cRollKappaMachLambda);
		System.out.println(">>>> K_f: " + cRollKappaf);
		System.out.println(">>>> Cl_beta/CL1_AR: " + cRollBetaOverCL1WingBodyAR);
		System.out.println(">>>> Cl_beta/Gamma_W: " + cRollBetaOverGammaW);
		System.out.println(">>>> K_M_Gamma_W: " + cRollKappaMachGammaW);
		System.out.println(">>>> DCl_beta/Gamma_W: " + cRollDeltaClBetaOverGammaW);
		System.out.println(">>>> DCl_beta_z_W: " + cRollDeltaClBetaZW);
		System.out.println(">>>> DCl_beta/(eps_W tan(Lambda_c/4)): " + cRollDeltaClBetaOverEpsWTanLc4);
		System.out.println(">>>>>> Cl_beta_WB: " + cRollBetaWB);
		
		// Calculation of Cl_beta_H
		LiftingSurface hTail = aircraft.getHTail();
		
		System.out.println(hTail);
		
		System.out.println("-------------------");
		System.out.println("Horizontal tail read");
		System.out.println("-------------------");

		// Cl_beta/CL1_Lambda_c/2
		double taperRatioHTail = hTail.getEquivalentWing().getPanels().get(0).getTaperRatio();
		double aspectRatioHTail = hTail.getAspectRatio();
		Amount<Angle> sweepAngleC2HTail = hTail.getEquivalentWing().getPanels().get(0).getSweepHalfChord();
		
		System.out.println(">> taper ratio: " + taperRatioHTail);
		System.out.println(">> aspect ratio: " + aspectRatioHTail);
		System.out.println(">> sweep angle @ c/2 (deg): " + sweepAngleC2HTail.doubleValue(NonSI.DEGREE_ANGLE));

		Amount<?> cRollBetaOverCL1HTailLc2 = Amount.valueOf(databaseReader.getClbetaWBClbetaOverCLift1Lc2VsLc2ARlambda(taperRatioHTail, aspectRatioHTail, sweepAngleC2HTail), NonSI.DEGREE_ANGLE.inverse()); // var0, var1, var2 

		// K_M_Lambda
		double aspectRatioOverCosSweepAngleC2HTail = aspectRatioHTail/Math.cos(sweepAngleC2HTail.doubleValue(SI.RADIAN));
		double machTimesCosSweepAngleC2HTail = mach * Math.cos(sweepAngleC2HTail.doubleValue(SI.RADIAN));
		
		System.out.println(">> Mach: " + mach);
		System.out.println(">> AR/cos(Lambda_c/2): " + aspectRatioOverCosSweepAngleC2HTail);
		System.out.println(">> Mach cos(Lambda_c/2): " + machTimesCosSweepAngleC2HTail);
		
		double cRollKappaMachLambdaHTail = databaseReader.getClbetaWBKMLVsMachTimesCosLc2AROverCosLc2(aspectRatioOverCosSweepAngleC2HTail, machTimesCosSweepAngleC2HTail); // var0, var1
		
		// K_f
		Amount<Length> spanHTail = hTail.getSpan();
		Amount<Length> aHTail = Amount.valueOf(20.0, SI.METER); //TODO take AHTail from aircraft
		double aOverSpanHTail = a.doubleValue(SI.METER)/spanHTail.doubleValue(SI.METER);
		
		System.out.println(">> A: " + aHTail);
		System.out.println(">> b_H: " + spanHTail);
		System.out.println(">> A/b_H: " + aOverSpanHTail);
		
		double cRollKappafHTail = databaseReader.getClbetaWBKfVsAOverBAROverCosLc2(aspectRatioOverCosSweepAngleC2HTail, aOverSpanHTail); // var0, var1
		
		// Cl_beta/CL1_AR
		Amount<?> cRollBetaOverCL1HTailAR = Amount.valueOf(databaseReader.getClbetaWBClbetaOverCLift1ARVsARlambda(taperRatioHTail, aspectRatioHTail), NonSI.DEGREE_ANGLE.inverse()); // var0, var1
		
		// Cl_beta/Gamma_H
		Amount<?> cRollBetaOverGammaH = Amount.valueOf(databaseReader.getClbetaWBClbetaOverGammaWVsARLc2lambda(taperRatioHTail, sweepAngleC2HTail, aspectRatioHTail), NonSI.DEGREE_ANGLE.pow(2).inverse()); // var0, var1, var2
		
		// K_M_Gamma_H
		double cRollKappaMachGammaH = databaseReader.getClbetaWBKMGammaVsMachTimesCosLc2AROverCosLc2(aspectRatioOverCosSweepAngleC2HTail, machTimesCosSweepAngleC2HTail); // var0, var1
		
		// DeltaCl_beta/Gamma_H
		double dBOverSpanHTail = dB.doubleValue(SI.METER)/spanHTail.doubleValue(SI.METER);
		
		System.out.println(">> d_B: " + dB);
		
		Amount<?> cRollDeltaClBetaOverGammaH = Amount.valueOf(-0.0005*aspectRatioHTail*Math.pow(dBOverSpanHTail, 2.0), NonSI.DEGREE_ANGLE.pow(2).inverse());
		
		// DCl_beta_z_H
		Amount<Length> zH = Amount.valueOf(1.0, SI.METER);
		// TODO take z_H from aircraft
		
		System.out.println(">> z_H: " + zH);
		
		Amount<?> cRollDeltaClBetaZH = Amount.valueOf((1.2*Math.sqrt(aspectRatioHTail)/57.3)*(zH.doubleValue(SI.METER)/spanHTail.doubleValue(SI.METER))*(2*dB.doubleValue(SI.METER)/spanHTail.doubleValue(SI.METER)), NonSI.DEGREE_ANGLE.inverse());
		
		// DCl_beta/(eps_H*tan(Lambda_c/4))
		Amount<?> cRollDeltaClBetaOverEpsHTanLc4 = Amount.valueOf(databaseReader.getClbetaWBDClbetaOverEpsWTimesTanLc4VsARlambda(taperRatioHTail, aspectRatioHTail), NonSI.DEGREE_ANGLE.pow(2).inverse()); // var0, var1
		
		// Cl_beta_H
		Amount<Angle> dihedralHTail = hTail.getDihedralMean();
		Amount<Angle> aerodynamicTwistHTail = hTail.getEquivalentWing().getPanels().get(0).getTwistAerodynamicAtTip();
		Amount<Angle> sweepAngleC4HTail = hTail.getEquivalentWing().getPanels().get(0).getSweepQuarterChord();
		double etaH = 0.9;
		// TODO take the degradation factor of dynamic pressure
		Amount<Area> surfaceHTail = hTail.getSurfacePlanform();
		
		System.out.println(">> density: " + density);
		System.out.println(">> velocity: " + velocityTAS);
		System.out.println(">> weight: " + weight);
		System.out.println(">> surface wing: " + surfaceWing);
		System.out.println(">> CL_1: " + cL1);
		System.out.println(">> dihedral horizontal tail angle: " + dihedralHTail);
		System.out.println(">> aerodynamic twist horizontal tail: " + aerodynamicTwistHTail);
		System.out.println(">> sweep angle @ c/4: " + sweepAngleC4HTail);
		System.out.println(">> degradation factor of dynamic pressure: " + etaH);
		System.out.println(">> surface horizontal tail: " + surfaceHTail);
		
		Amount<?> cRollBetaH = (((cRollBetaOverCL1HTailLc2.times(cRollKappaMachLambdaHTail*cRollKappafHTail).plus(cRollBetaOverCL1HTailAR)).times(57.3*cL1))
				.plus((dihedralHTail.times(cRollBetaOverGammaH.times(cRollKappaMachGammaH).plus(cRollDeltaClBetaOverGammaH)).plus(cRollDeltaClBetaZH)
				.plus(cRollDeltaClBetaOverEpsHTanLc4.times(aerodynamicTwistHTail.times(Math.tan(sweepAngleC4HTail.doubleValue(SI.RADIAN)))))).times(57.3)))
				.times(surfaceHTail.divide(surfaceWing).times(spanHTail).divide(spanWing).times(etaH));
				
		System.out.println(">>>> Cl_beta/CL1_Lambda_c/2: " + cRollBetaOverCL1HTailLc2);
		System.out.println(">>>> K_M_Lambda: " + cRollKappaMachLambdaHTail);
		System.out.println(">>>> K_f: " + cRollKappafHTail);
		System.out.println(">>>> Cl_beta/CL1_AR: " + cRollBetaOverCL1HTailAR);
		System.out.println(">>>> Cl_beta/Gamma_H: " + cRollBetaOverGammaH);
		System.out.println(">>>> K_M_Gamma_H: " + cRollKappaMachGammaH);
		System.out.println(">>>> DCl_beta/Gamma_H: " + cRollDeltaClBetaOverGammaH);
		System.out.println(">>>> DCl_beta_z_H: " + cRollDeltaClBetaZH);
		System.out.println(">>>> DCl_beta/(eps_H tan(Lambda_c/4)): " + cRollDeltaClBetaOverEpsHTanLc4);
		System.out.println(">>>>>> Cl_beta_H: " + cRollBetaH);
		
		// Calculation of Cl_beta_V
		LiftingSurface vTail = aircraft.getVTail();
		
		System.out.println(vTail);
		
		System.out.println("-------------------");
		System.out.println("Vertical tail read");
		System.out.println("-------------------");
		
		Amount<Angle> angleOfAttack = operatingConditions.getAlphaCurrentCruise();
		Amount<?> cLAlphaV = Amount.valueOf(6.92, SI.RADIAN.inverse());
		double etaV = 0.9;
		// TODO take the degradation factor of dynamic pressure (Napolitano Aircraft Dynamics pag. 143)
		double dSigma_dBeta = 0.11;
		// TODO take the gradient of the sidewash angle (Napolitano Aircraft Dynamics pag. 143)
		Amount<Area> surfaceVTail = vTail.getSurfacePlanform();
		Amount<Length> xV = Amount.valueOf(10.0, SI.METER);
		Amount<Length> zV = Amount.valueOf(1.0, SI.METER);
		Amount<Length> spanVTail = vTail.getSpan();
		Amount<Length> r1 = Amount.valueOf(5.0, SI.METER);
		double bVOver2TimesR1 = spanVTail.doubleValue(SI.METER)/r1.doubleValue(SI.METER);
		double cYawKappaYV = databaseReader.getCybetaVKYVVsBVOver2TimesR1(bVOver2TimesR1);
		
		Amount<?> cRollBetaV = cLAlphaV.abs().times(surfaceVTail.divide(surfaceWing).times((zV.times(Math.cos(angleOfAttack.doubleValue(SI.RADIAN)))
				.minus(xV.times(Math.sin(angleOfAttack.doubleValue(SI.RADIAN))))).divide(spanWing)).times(-cYawKappaYV*etaV*(1 + dSigma_dBeta)));
		
		System.out.println(">> alpha_1: " + angleOfAttack);
		System.out.println(">> CL_Alpha_V: " + cLAlphaV);
		System.out.println(">> degradation factor of dynamic pressure: " + etaV);
		System.out.println(">> gradient of the sidewash angle: " + dSigma_dBeta);
		System.out.println(">> surface vertical tail: " + surfaceVTail);
		System.out.println(">> x_V: " + xV);
		System.out.println(">> z_V: " + zV);
		System.out.println(">> b_V: " + spanVTail);
		System.out.println(">> r_1: " + r1);
		System.out.println(">>>> K_Y_V: " + cYawKappaYV);
		System.out.println(">>>>>> Cl_beta_V: " + cRollBetaV);
		
		// Calculation of Cl_beta
		Amount<?> cRollBeta = cRollBetaWB.plus(cRollBetaH.plus(cRollBetaV));
		
		System.out.println(">>>>>>>> Cl_beta: " + cRollBeta);
	}

}
