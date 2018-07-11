package sandbox2.cavas;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.OperatingConditions;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCLAlpha;
import calculators.aerodynamics.LiftCalc;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.atmosphere.AtmosphereCalc;

public class Test_01 {

	public static void main(String[] args) {
		System.out.println("-------------------------");
		System.out.println("CAVAS Test");
		System.out.println("-------------------------");
		
		// Import the aircraft
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		// Import operating conditions
		OperatingConditions operatingConditions = AircraftUtils.importOperatingCondition(args);
		
		// Perform analysis
		// AircraftUtils.performAnalyses(aircraft, operatingConditions, AircraftUtils.pathToAnalysesXML, "IRON_CANARD_cavas");
		
		LiftingSurface wing = aircraft.getWing();
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface hTail = aircraft.getHTail();
		LiftingSurface vTail = aircraft.getVTail();
		
		AerodynamicDatabaseReader databaseReader =  wing.getAeroDatabaseReader();
		
		List<Amount<Angle>> alphaList = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(0, 20, 30),
				NonSI.DEGREE_ANGLE
				);
		
		LiftingSurfaceAerodynamicsManager wingManager = new LiftingSurfaceAerodynamicsManager(
				wing,
				operatingConditions, 
				ConditionEnum.CRUISE,
				50,
				alphaList, 
				alphaList,
				null
				);
		
		LiftingSurfaceAerodynamicsManager hTailManager = new LiftingSurfaceAerodynamicsManager(
				hTail,
				operatingConditions, 
				ConditionEnum.CRUISE,
				50,
				alphaList, 
				alphaList,
				null
				);
		
		LiftingSurfaceAerodynamicsManager vTailManager = new LiftingSurfaceAerodynamicsManager(
				vTail,
				operatingConditions, 
				ConditionEnum.CRUISE,
				50,
				alphaList, 
				alphaList,
				null
				);
		
		// --------------------------------------------------
		// Calculation of Cl_beta_WB		
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_beta_WB");
		System.out.println("-------------------------");
		
		// Cl_beta/CL1_Lambda_c/2_W
		double taperRatioWing = wing.getEquivalentWing().getPanels().get(0).getTaperRatio();
		double aspectRatioWing = wing.getAspectRatio();
		Amount<Angle> sweepAngleC2Wing = wing.getEquivalentWing().getPanels().get(0).getSweepHalfChord().to(NonSI.DEGREE_ANGLE);
		
		System.out.println(">> taper ratio wing: " + taperRatioWing);
		System.out.println(">> aspect ratio wing: " + aspectRatioWing);
		System.out.println(">> sweep angle @ c/2 wing: " + sweepAngleC2Wing);

		Amount<?> cRollBetaOverCL1WingBodyLc2 = Amount.valueOf(
				databaseReader.getClBetaWBClBetaOverCLift1Lc2VsLc2ARLambda(taperRatioWing, aspectRatioWing, sweepAngleC2Wing), // var0, var1, var2
				NonSI.DEGREE_ANGLE.inverse()
				);

		// K_M_Lambda_W
		double aspectRatioOverCosSweepAngleC2Wing = aspectRatioWing/Math.cos(sweepAngleC2Wing.doubleValue(SI.RADIAN));
		double mach = operatingConditions.getMachCruise();
		double machTimesCosSweepAngleC2Wing = mach * Math.cos(sweepAngleC2Wing.doubleValue(SI.RADIAN));
		
		System.out.println(">> Mach: " + mach);
		System.out.println(">> AR/cos(Lambda_c/2) wing: " + aspectRatioOverCosSweepAngleC2Wing);
		System.out.println(">> Mach cos(Lambda_c/2) wing: " + machTimesCosSweepAngleC2Wing);
		
		double cRollKappaMachLambdaW = databaseReader.getClBetaWBKMLVsMachTimesCosLc2AROverCosLc2(
				aspectRatioOverCosSweepAngleC2Wing, // var0
				machTimesCosSweepAngleC2Wing // var1
				);
		
		// K_f_W
		Amount<Length> spanWing = wing.getSpan();
		Amount<Length> xC2TipBFRWing = wing.getXApexConstructionAxes()
				.plus(wing.getXLEBreakPoints().get(
						wing.getXLEBreakPoints().size()-1))
				.plus(wing.getEquivalentWing().getPanels().get(0).getChordTip().divide(2));
		double aOverSpanWing = xC2TipBFRWing.doubleValue(SI.METER)/spanWing.doubleValue(SI.METER);
		
		System.out.println(">> A wing: " + xC2TipBFRWing);
		System.out.println(">> span wing: " + spanWing);
		System.out.println(">> A/b wing: " + aOverSpanWing);

		double cRollKappafW = databaseReader.getClBetaWBKfVsAOverBAROverCosLc2(aspectRatioOverCosSweepAngleC2Wing, aOverSpanWing); // var0, var1
		
		// Cl_beta/CL1_AR_W
		Amount<?> cRollBetaOverCL1WingBodyAR = Amount.valueOf(
				databaseReader.getClBetaWBClBetaOverCLift1ARVsARLambda(taperRatioWing, aspectRatioWing), // var0, var1
				NonSI.DEGREE_ANGLE.inverse()
				);
		
		// Cl_beta/Gamma_W
		Amount<?> cRollBetaOverGammaW = Amount.valueOf(
				databaseReader.getClBetaWBClBetaOverGammaWVsARLc2Lambda(taperRatioWing, sweepAngleC2Wing, aspectRatioWing), // var0, var1, var2
				NonSI.DEGREE_ANGLE.pow(2).inverse()
				);
		
		// K_M_Gamma_W
		double cRollKappaMachGammaW = databaseReader.getClBetaWBKMGammaVsMachTimesCosLc2AROverCosLc2(
				aspectRatioOverCosSweepAngleC2Wing, // var0
				machTimesCosSweepAngleC2Wing // var1
				);
		
		// DCl_beta/Gamma_W
		Amount<Length> dBW = Amount.valueOf( 
				fuselage.getEquivalentDiameterAtX(
						wing.getXApexConstructionAxes()
						.plus(wing.getPanels().get(0).getChordRoot().divide(2)).doubleValue(SI.METER)
						),
				SI.METER
				);
//		Another solution
//		
//		Amount<Area> crossSectionFuselage = aircraft.getFuselage().getCylinderSectionArea();
//		Amount<Length> dBW = Amount.valueOf(Math.sqrt(4*crossSectionFuselage.doubleValue(SI.SQUARE_METRE)/Math.PI), SI.METER);
		double dBWOverSpanWing = dBW.doubleValue(SI.METER)/spanWing.doubleValue(SI.METER);		
		
		System.out.println(">> d_B_W: " + dBW);
		
		Amount<?> cRollDeltaClBetaOverGammaW = Amount.valueOf(
				-0.0005*aspectRatioWing*Math.pow(dBWOverSpanWing, 2),
				NonSI.DEGREE_ANGLE.pow(2).inverse()
				);
		
		// DCl_beta_z_W
		Amount<Length> zW = wing.getZApexConstructionAxes().opposite();
		double zWOverSpanWing = zW.doubleValue(SI.METER)/spanWing.doubleValue(SI.METER);
		
		System.out.println(">> z_W: " + zW);
		
		Amount<?> cRollDeltaClBetaZW = Amount.valueOf(
				1.2*Math.sqrt(aspectRatioWing)/57.3*zWOverSpanWing*2*dBWOverSpanWing,
				NonSI.DEGREE_ANGLE.inverse()
				);
		
		// DCl_beta/(eps_W*tan(Lambda_c/4))
		Amount<?> cRollDeltaClBetaOverEpsWTanLc4 = Amount.valueOf(
				databaseReader.getClBetaWBDClBetaOverEpsWTimesTanLc4VsARLambda(taperRatioWing, aspectRatioWing), // var0, var1
				NonSI.DEGREE_ANGLE.pow(2).inverse()
				);
		
		// Cl_beta_WB
		Amount<Velocity> velocityTAS = operatingConditions.getTASCruise();
		Amount<Mass> maxTakeOffWeight = Amount.valueOf(54500.0, SI.KILOGRAM);
		Amount<Force> weight = Amount.valueOf(
				maxTakeOffWeight.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		Amount<Area> surfaceWing = wing.getSurfacePlanform();
		
		double cL1 = LiftCalc.calculateLiftCoeff(
				weight.doubleValue(SI.NEWTON),
				velocityTAS.doubleValue(SI.METERS_PER_SECOND),
				surfaceWing.doubleValue(SI.SQUARE_METRE),
				operatingConditions.getAltitudeCruise().doubleValue(SI.METER)
				);
		
		Amount<Angle> dihedralWing = wing.getDihedralMean();
		Amount<Angle> aerodynamicTwistWing = wing.getEquivalentWing().getPanels().get(0).getTwistAerodynamicAtTip();
		Amount<Angle> sweepAngleC4Wing = wing.getEquivalentWing().getPanels().get(0).getSweepQuarterChord().to(NonSI.DEGREE_ANGLE);
		
		System.out.println(">> velocity: " + velocityTAS);
		System.out.println(">> weight: " + weight);
		System.out.println(">> surface wing: " + surfaceWing);
		System.out.println(">> CL_1: " + cL1);
		System.out.println(">> dihedral angle wing: " + dihedralWing);
		System.out.println(">> aerodynamic twist wing: " + aerodynamicTwistWing);
		System.out.println(">> sweep angle @ c/4 wing: " + sweepAngleC4Wing);
		
		Amount<?> cRollBetaWB =
				(
						cRollBetaOverCL1WingBodyLc2.times(cRollKappaMachLambdaW*cRollKappafW)
						.plus(cRollBetaOverCL1WingBodyAR)
				).times(cL1)
				.plus(
						dihedralWing.times(cRollBetaOverGammaW.times(cRollKappaMachGammaW).plus(cRollDeltaClBetaOverGammaW))
						.plus(cRollDeltaClBetaZW)
						.plus(
								cRollDeltaClBetaOverEpsWTanLc4.times(
										aerodynamicTwistWing.times(
												Math.tan(sweepAngleC4Wing.doubleValue(SI.RADIAN))
										)
								)
						)
				).to(SI.RADIAN.inverse());
				
		System.out.println(">>>> Cl_beta/CL1_Lambda_c/2_W: " + cRollBetaOverCL1WingBodyLc2);
		System.out.println(">>>> K_M_Lambda_W: " + cRollKappaMachLambdaW);
		System.out.println(">>>> K_f_W: " + cRollKappafW);
		System.out.println(">>>> Cl_beta/CL1_AR_W: " + cRollBetaOverCL1WingBodyAR);
		System.out.println(">>>> Cl_beta/Gamma_W: " + cRollBetaOverGammaW);
		System.out.println(">>>> K_M_Gamma_W: " + cRollKappaMachGammaW);
		System.out.println(">>>> DCl_beta/Gamma_W: " + cRollDeltaClBetaOverGammaW);
		System.out.println(">>>> DCl_beta_z_W: " + cRollDeltaClBetaZW);
		System.out.println(">>>> DCl_beta/(eps_W tan(Lambda_c/4)): " + cRollDeltaClBetaOverEpsWTanLc4);
		System.out.println(">>>>>> Cl_beta_WB: " + cRollBetaWB);
		
		// --------------------------------------------------
		// Calculation of Cl_beta_H
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_beta_H");
		System.out.println("-------------------------");

		// NEGLECT effect of fuselage, sweep and z_H on Cl_beta_H
		
		// Cl_beta/Gamma_H
		double taperRatioHTail = hTail.getEquivalentWing().getPanels().get(0).getTaperRatio();
		double aspectRatioHTail = hTail.getAspectRatio();
		Amount<Angle> sweepAngleC2HTail = hTail.getEquivalentWing().getPanels().get(0).getSweepHalfChord().to(NonSI.DEGREE_ANGLE);
		
		System.out.println(">> taper ratio horizontal tail: " + taperRatioHTail);
		System.out.println(">> aspect ratio horizontal tail: " + aspectRatioHTail);
		System.out.println(">> sweep angle @ c/2 horizontal tail: " + sweepAngleC2HTail);

		Amount<?> cRollBetaOverGammaH = Amount.valueOf(
				databaseReader.getClBetaWBClBetaOverGammaWVsARLc2Lambda(taperRatioHTail, sweepAngleC2HTail, aspectRatioHTail), // var0, var1, var2
				NonSI.DEGREE_ANGLE.pow(2).inverse()
				);

		// K_M_Gamma_H
		double aspectRatioOverCosSweepAngleC2HTail = aspectRatioHTail/Math.cos(sweepAngleC2HTail.doubleValue(SI.RADIAN));
		double machTimesCosSweepAngleC2HTail = mach * Math.cos(sweepAngleC2HTail.doubleValue(SI.RADIAN));
		
		System.out.println(">> Mach: " + mach);
		System.out.println(">> AR/cos(Lambda_c/2) horizontal tail: " + aspectRatioOverCosSweepAngleC2HTail);
		System.out.println(">> Mach cos(Lambda_c/2) horizontal tail: " + machTimesCosSweepAngleC2HTail);
		
		double cRollKappaMachGammaH = databaseReader.getClBetaWBKMGammaVsMachTimesCosLc2AROverCosLc2(
				aspectRatioOverCosSweepAngleC2HTail, // var0
				machTimesCosSweepAngleC2HTail // var1
				);
		
		// DCl_beta/Gamma_H
		Amount<Length> spanHTail = hTail.getSpan();
		Amount<Length> dBH = Amount.valueOf( 
				fuselage.getEquivalentDiameterAtX(
						hTail.getXApexConstructionAxes()
						.plus(hTail.getPanels().get(0).getChordRoot().divide(2)).doubleValue(SI.METER)),
				SI.METER
				);
		double dBHOverSpanHTail = dBH.doubleValue(SI.METER)/spanHTail.doubleValue(SI.METER);
		
		System.out.println(">> span horizontal tail: " + spanHTail);
		System.out.println(">> d_B_H: " + dBH);
		
		Amount<?> cRollDeltaClBetaOverGammaH = Amount.valueOf(
				-0.0005*aspectRatioHTail*Math.pow(dBHOverSpanHTail, 2),
				NonSI.DEGREE_ANGLE.pow(2).inverse()
				);
		
		// DCl_beta/(eps_H*tan(Lambda_c/4))
		Amount<?> cRollDeltaClBetaOverEpsHTanLc4 = Amount.valueOf(
				databaseReader.getClBetaWBDClBetaOverEpsWTimesTanLc4VsARLambda(taperRatioHTail, aspectRatioHTail), // var0, var1
				NonSI.DEGREE_ANGLE.pow(2).inverse()
				);
		
		// Cl_beta_H
		Amount<Angle> dihedralHTail = hTail.getDihedralMean();
		Amount<Angle> aerodynamicTwistHTail = hTail.getEquivalentWing().getPanels().get(0).getTwistAerodynamicAtTip();
		Amount<Angle> sweepAngleC4HTail = hTail.getEquivalentWing().getPanels().get(0).getSweepQuarterChord().to(NonSI.DEGREE_ANGLE);
		double etaH = 0.9;
		Amount<Area> surfaceHTail = hTail.getSurfacePlanform();
		
		System.out.println(">> dihedral angle horizontal tail: " + dihedralHTail);
		System.out.println(">> aerodynamic twist horizontal tail: " + aerodynamicTwistHTail);
		System.out.println(">> sweep angle @ c/4 horizontal tail: " + sweepAngleC4HTail);
		System.out.println(">> degradation factor of dynamic pressure horizontal tail: " + etaH);
		System.out.println(">> surface horizontal tail: " + surfaceHTail);
		
		Amount<?> cRollBetaH = 
				(
						dihedralHTail.times(cRollBetaOverGammaH.times(cRollKappaMachGammaH).plus(cRollDeltaClBetaOverGammaH))
						.plus(
								cRollDeltaClBetaOverEpsHTanLc4.times(
										aerodynamicTwistHTail.times(
												Math.tan(sweepAngleC4HTail.doubleValue(SI.RADIAN))
										)
								)
						)
				).times(surfaceHTail).divide(surfaceWing).times(spanHTail).divide(spanWing).times(etaH)
				.to(SI.RADIAN.inverse());
				
		System.out.println(">>>> Cl_beta/Gamma_H: " + cRollBetaOverGammaH);
		System.out.println(">>>> K_M_Gamma_H: " + cRollKappaMachGammaH);
		System.out.println(">>>> DCl_beta/Gamma_H: " + cRollDeltaClBetaOverGammaH);
		System.out.println(">>>> DCl_beta/(eps_H tan(Lambda_c/4)): " + cRollDeltaClBetaOverEpsHTanLc4);
		System.out.println(">>>>>> Cl_beta_H: " + cRollBetaH);
		
		// --------------------------------------------------
		// Calculation of CY_beta_V
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CY_beta_V");
		System.out.println("-------------------------");
		
		// K_Y_V
		Amount<Length> spanVTail = vTail.getSpan();
		Amount<Length> r1 = Amount.valueOf(
				fuselage.getZOutlineXZUpperAtX(
						vTail.getXApexConstructionAxes()
						.plus(vTail.getPanels().get(0).getChordRoot().divide(4)).doubleValue(SI.METER))
				- fuselage.getZOutlineXZLowerAtX(
						vTail.getXApexConstructionAxes()
						.plus(vTail.getPanels().get(0).getChordRoot().divide(4)).doubleValue(SI.METER)),
				SI.METER
				);
		double bVOver2TimesR1 = spanVTail.doubleValue(SI.METER)/(2*r1.doubleValue(SI.METER));
		
		System.out.println(">> span vertical tail: " + spanVTail);
		System.out.println(">> r_1: " + r1);

		double cYKappaYV = databaseReader.getCyBetaVKYVVsBVOver2TimesR1(bVOver2TimesR1);
		
		// CY_beta_V
		CalcCLAlpha calcCLAlphaVTail = vTailManager.new CalcCLAlpha();
		calcCLAlphaVTail.helmboldDiederich(mach);
		Amount<?> cLAlphaV = vTailManager.getCLAlpha().get(MethodEnum.HELMBOLD_DIEDERICH);
		Amount<Area> surfaceVTail = vTail.getSurfacePlanform();
		Amount<Length> heightFuselage = fuselage.getSectionCylinderHeight();
		double zWOverHeightFuselage = zW.doubleValue(SI.METER)/heightFuselage.doubleValue(SI.METER);
		double etaVTimes1MinusdSigmaOverdBeta =
				0.724
				+ 3.06*(surfaceVTail.doubleValue(SI.SQUARE_METRE)/surfaceWing.doubleValue(SI.SQUARE_METRE))/(1 + Math.cos(sweepAngleC4Wing.doubleValue(SI.RADIAN)))
				+ 0.4*zWOverHeightFuselage
				+ 0.009*aspectRatioWing;

		System.out.println(">> CL_Alpha vertical tail: " + cLAlphaV);
		System.out.println(">> surface vertical tail: " + surfaceVTail);
		System.out.println(">> height fuselage: " + heightFuselage);
		System.out.println(">> degradation factor of dynamic pressure * (1 - gradient of the sidewash angle): " + etaVTimes1MinusdSigmaOverdBeta);

		Amount<?> cYBetaV =
				cLAlphaV.abs().times(surfaceVTail).divide(surfaceWing)
				.times(-cYKappaYV*etaVTimes1MinusdSigmaOverdBeta)
				.to(SI.RADIAN.inverse());
		
		System.out.println(">>>> K_Y_V: " + cYKappaYV);
		System.out.println(">>>>>> CY_beta_V: " + cYBetaV);

		// --------------------------------------------------
		// Calculation of Cl_beta_V
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_beta_V");
		System.out.println("-------------------------");
		
		// Cl_beta_V
		Amount<Angle> angleOfAttack = operatingConditions.getAlphaCurrentCruise().to(NonSI.DEGREE_ANGLE);
		Amount<Length> xCG = Amount.valueOf(15.0, SI.METER);
		Amount<Length> xV = vTail.getXApexConstructionAxes()
				.plus(vTail.getMeanAerodynamicChordLeadingEdgeX())
				.plus((vTail.getMeanAerodynamicChord()).times(0.25))
				.minus(xCG);
		Amount<Length> zV = vTail.getZApexConstructionAxes()
				.plus(vTail.getMeanAerodynamicChordLeadingEdgeZ());
		
		System.out.println(">> alpha_1: " + angleOfAttack);
		System.out.println(">> x_CG: " + xCG);
		System.out.println(">> x_V: " + xV);
		System.out.println(">> z_V: " + zV);

		Amount<?> cRollBetaV =
				cYBetaV
				.times(
						zV.times(
								Math.cos(angleOfAttack.doubleValue(SI.RADIAN))
						).minus(
								xV.times(
										Math.sin(angleOfAttack.doubleValue(SI.RADIAN))
								)
						)
				).divide(spanWing)
				.to(SI.RADIAN.inverse());
		
		System.out.println(">>>>>> Cl_beta_V: " + cRollBetaV);
		
		// --------------------------------------------------
		// Calculation of Cl_beta
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_beta");
		System.out.println("-------------------------");
		
		// Cl_beta
		Amount<?> cRollBeta = 
				cRollBetaWB.plus(cRollBetaH).plus(cRollBetaV)
				.to(SI.RADIAN.inverse());
		
		System.out.println(">>>>>>>> Cl_beta: " + cRollBeta);
		
		// --------------------------------------------------
		// Calculation of Cl_delta_A
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_delta_A");
		System.out.println("-------------------------");
		
		// tau_A
		double cAileronOverCWing = wing.getAsymmetricFlaps().get(1).getMeanChordRatio();

		System.out.println(">> mean chord aileron/mean chord wing: " + cAileronOverCWing);
		
		double tauA = databaseReader.getControlSurfaceTauEVsCControlSurfaceOverCHorizontalTail(cAileronOverCWing);
		
		// Delta RME
		double etaInboardAileron = wing.getAsymmetricFlaps().get(1).getTheAsymmetricFlapInterface().getInnerStationSpanwisePosition();
		double etaOutboardAileron = wing.getAsymmetricFlaps().get(1).getTheAsymmetricFlapInterface().getOuterStationSpanwisePosition();
		double betaFactor = Math.sqrt(1 - Math.pow(mach, 2));
		Amount<Angle> lambdaBetaFactorWing = Amount.valueOf(
				Math.atan(
						Math.tan(sweepAngleC4Wing.doubleValue(SI.RADIAN))/betaFactor
				),
				SI.RADIAN
				);
		CalcCLAlpha calcCLAlphaWing = wingManager.new CalcCLAlpha();
		calcCLAlphaWing.nasaBlackwell();
		Amount<?> cLAlphaW = wingManager.getCLAlpha().get(MethodEnum.NASA_BLACKWELL);
		double kWing = cLAlphaW.to(SI.RADIAN.inverse()).getEstimatedValue()*betaFactor/(2*Math.PI);
		double betaFactorTimesAspectRatioWingOverKWing = betaFactor*aspectRatioWing/kWing;

		System.out.println(">> eta_In_Aileron: " + etaInboardAileron);
		System.out.println(">> eta_Out_Aileron: " + etaOutboardAileron);
		System.out.println(">> lambda_beta_W: " + lambdaBetaFactorWing.to(NonSI.DEGREE_ANGLE));
		System.out.println(">> beta AR/k_W: " + betaFactorTimesAspectRatioWingOverKWing);
		
		double innerRME = databaseReader.getClDeltaARMEVsEtaLambdaBetaBetaTimesAROverKLambda(
				taperRatioWing,
				betaFactorTimesAspectRatioWingOverKWing,
				lambdaBetaFactorWing,
				etaInboardAileron
				); 
		double outerRME = databaseReader.getClDeltaARMEVsEtaLambdaBetaBetaTimesAROverKLambda(
				taperRatioWing,
				betaFactorTimesAspectRatioWingOverKWing,
				lambdaBetaFactorWing,
				etaOutboardAileron
				);
		double deltaRME = outerRME - innerRME;

		// Cl_delta_A
		Amount<?> cRollDeltaAFullChord = Amount.valueOf(
				-deltaRME*kWing/betaFactor,
				SI.RADIAN.inverse()
				);
		Amount<?> cRollDeltaA = cRollDeltaAFullChord.times(tauA);
		
		System.out.println(">>>> tau_A: " + tauA);
		System.out.println(">>>> RME_I: " + innerRME);
		System.out.println(">>>> RME_O: " + outerRME);
		System.out.println(">>>> Delta RME: " + deltaRME);
		System.out.println(">>>>>> Cl_delta_A: " + cRollDeltaA);
		
		// --------------------------------------------------
		// Calculation of CY_delta_r
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CY_delta_r");
		System.out.println("-------------------------");
		
		// tau_R
		double cRudderOverCWing = vTail.getSymmetricFlaps().get(0).getMeanChordRatio();
		
		System.out.println(">> mean chord rudder/mean chord wing: " + cRudderOverCWing);
		
		double tauR = databaseReader.getControlSurfaceTauEVsCControlSurfaceOverCHorizontalTail(cRudderOverCWing);
		
		// Delta K_R
		double taperRatioVTail = vTail.getEquivalentWing().getPanels().get(0).getTaperRatio();
		double etaInboardRudder = vTail.getSymmetricFlaps().get(0).getTheSymmetricFlapInterface().getInnerStationSpanwisePosition();
		double etaOutboardRudder = vTail.getSymmetricFlaps().get(0).getTheSymmetricFlapInterface().getOuterStationSpanwisePosition();

		System.out.println(">> taper ratio vertical tail: " + taperRatioVTail);
		System.out.println(">> eta_In_Rudder: " + etaInboardRudder);
		System.out.println(">> eta_Out_Rudder: " + etaOutboardRudder);
		
		double innerKR = databaseReader.getCYDeltaRKRVsEtaLambdaV(taperRatioVTail, etaInboardRudder);
		double outerKR = databaseReader.getCYDeltaRKRVsEtaLambdaV(taperRatioVTail, etaOutboardRudder);
		double deltaKR = outerKR - innerKR;
		
		// CY_delta_r
		double etaV = 0.9;
		Amount <?> cYDeltaR = cLAlphaV.abs().times(surfaceVTail).divide(surfaceWing).times(etaV*deltaKR*tauR);
		
		System.out.println(">> degradation factor of dynamic pressure vertical tail: " + etaV);
		System.out.println(">>>> tau_R: " + tauR);
		System.out.println(">>>> K_R_I: " + innerKR);
		System.out.println(">>>> K_R_O: " + outerKR);
		System.out.println(">>>> Delta K_R: " + deltaKR);
		System.out.println(">>>>>> CY_delta_r: " + cYDeltaR);
		
		// --------------------------------------------------
		// Calculation of Cl_delta_r
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_delta_r");
		System.out.println("-------------------------");
		
		// Cl_delta_r
		Amount<Length> zApplicationForceRudder = spanVTail.times((etaOutboardRudder-etaInboardRudder)/2);
		Amount<Length> xR = vTail.getXApexConstructionAxes()
				.plus(vTail.getXLEAtYActual(zApplicationForceRudder.doubleValue(SI.METER)))
				.plus(
						Amount.valueOf(
								vTail.getChordAtYActual(zApplicationForceRudder.doubleValue(SI.METER))*
								(1 - 0.75*vTail.getSymmetricFlaps().get(0).getMeanChordRatio()),
								SI.METER
						)
				)
				.minus(xCG);
		Amount<Length> zR = vTail.getZApexConstructionAxes()
				.plus(zApplicationForceRudder);
		
		System.out.println(">> x_R: " + xR);
		System.out.println(">> z_R: " + zR);
		
		Amount <?> cRollDeltaR = cYDeltaR
				.times(
						zR.times(
								Math.cos(angleOfAttack.doubleValue(SI.RADIAN))
						).minus(
								xR.times(
										Math.sin(angleOfAttack.doubleValue(SI.RADIAN))
								)
						)
				).divide(spanWing)
				.to(SI.RADIAN.inverse());
		
		System.out.println(">>>>>> Cl_delta_r: " + cRollDeltaR);
		
		// --------------------------------------------------
		// Calculation of Cl_p_WB
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_p_WB");
		System.out.println("-------------------------");

		// RDP_W
		Amount<?> rollingDampingParametersWing = Amount.valueOf(
				databaseReader.getClPWRDPVsLambdaBetaBetaTimesAROverKLambda(
						taperRatioWing, // var0
						betaFactorTimesAspectRatioWingOverKWing, // var1
						lambdaBetaFactorWing // var2
						),
				SI.RADIAN.inverse()
				);
				
		// Cl_p_WB
		Amount<?> cRollpWB = rollingDampingParametersWing.times(kWing/betaFactor);
		
		System.out.println(">>>> RDP_W: " + rollingDampingParametersWing);
		System.out.println(">>>>>> Cl_p_WB: " + cRollpWB);

		// --------------------------------------------------
		// Calculation of Cl_p_H
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_p_H");
		System.out.println("-------------------------");

		// RDP_H
		Amount<Angle> lambdaBetaFactorHTail = Amount.valueOf(
				Math.atan(
						Math.tan(sweepAngleC4HTail.doubleValue(SI.RADIAN))/betaFactor
				),
				SI.RADIAN);
		CalcCLAlpha calcCLAlphaHTail = hTailManager.new CalcCLAlpha();
		calcCLAlphaHTail.nasaBlackwell();
		Amount<?> cLAlphaH = hTailManager.getCLAlpha().get(MethodEnum.NASA_BLACKWELL);
		double kHTail = cLAlphaH.getEstimatedValue()*betaFactor/(2*Math.PI);
		double betaFactorTimesAspectRatioHTailOverKHTail = betaFactor*aspectRatioHTail/kHTail;

		System.out.println(">> lambda_beta_H: " + lambdaBetaFactorHTail.to(NonSI.DEGREE_ANGLE));
		System.out.println(">> beta AR/k_H: " + betaFactorTimesAspectRatioHTailOverKHTail);
		
		Amount<?> rollingDampingParametersHTail = Amount.valueOf(
				databaseReader.getClPWRDPVsLambdaBetaBetaTimesAROverKLambda(taperRatioHTail, betaFactorTimesAspectRatioHTailOverKHTail, lambdaBetaFactorHTail), // var0, var1, var2
				SI.RADIAN.inverse());
				
		// Cl_p_H
		Amount<?> cRollpH = rollingDampingParametersHTail.times(surfaceHTail).divide(surfaceWing).times((spanHTail.divide(spanWing)).pow(2)).times(0.5*kHTail/betaFactor);
		
		System.out.println(">>>> RDP_H: " + rollingDampingParametersHTail);
		System.out.println(">>>>>> Cl_p_H: " + cRollpH);

		// --------------------------------------------------
		// Calculation of Cl_p_V
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_p_V");
		System.out.println("-------------------------");

		// Cl_p_V
		Amount<?> cRollpV = cYBetaV.times((zV.divide(spanWing)).pow(2)).times(2);
		
		System.out.println(">>>>>> Cl_p_V: " + cRollpV);

		// --------------------------------------------------
		// Calculation of Cl_p
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_p");
		System.out.println("-------------------------");

		// Cl_p
		Amount<?> cRollp = cRollpWB.plus(cRollpH).plus(cRollpV);

		System.out.println(">>>>>>>> Cl_p: " + cRollp);
		
		// --------------------------------------------------
		// Calculation of Cl_r_W
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_r_W");
		System.out.println("-------------------------");

		// Cl_r/CL1
		double bFactor = 
				Math.sqrt(
						1 - Math.pow(mach*Math.cos(sweepAngleC4Wing.doubleValue(SI.RADIAN)), 2)
				);
		double dFactor =
				(
						1
						+ aspectRatioWing*(1 - Math.pow(bFactor, 2))/
								(
										2*bFactor*(aspectRatioWing*bFactor + 2*Math.cos(sweepAngleC4Wing.doubleValue(SI.RADIAN)))
								)
						+ (aspectRatioWing*bFactor + 2*Math.cos(sweepAngleC4Wing.doubleValue(SI.RADIAN)))/
								(
										(aspectRatioWing*bFactor + 4*Math.cos(sweepAngleC4Wing.doubleValue(SI.RADIAN)))		
								)*Math.pow(Math.tan(sweepAngleC4Wing.doubleValue(SI.RADIAN)), 2)/8
				)/
				(
						1 + (aspectRatioWing + 2*Math.cos(sweepAngleC4Wing.doubleValue(SI.RADIAN)))/
								(
										(aspectRatioWing + 4*Math.cos(sweepAngleC4Wing.doubleValue(SI.RADIAN)))		
								)*Math.pow(Math.tan(sweepAngleC4Wing.doubleValue(SI.RADIAN)), 2)/8
				);

		System.out.println(">> B: " + bFactor);
		System.out.println(">> D: " + dFactor);
		
		Amount<?> cRollrOverCL1AtMachZero = Amount.valueOf(
				databaseReader.getClRWClrOverCLift1VsARLambdaLc4(taperRatioWing, aspectRatioWing, sweepAngleC4Wing),
				SI.RADIAN.inverse()
				);
		Amount<?> cRollrOverCL1 = cRollrOverCL1AtMachZero.times(dFactor);
		
		// DCl_r/Gamma_W
		Amount<?> cRollDeltaClrOverGammaW = Amount.valueOf(
				(1/12)*
				(Math.PI*aspectRatioWing*Math.sin(sweepAngleC4Wing.doubleValue(SI.RADIAN)))/
				(aspectRatioWing + 4*Math.cos(sweepAngleC4Wing.doubleValue(SI.RADIAN))),
				SI.RADIAN.inverse().pow(2)
				);
		
		// DCl_r/eps_W
		Amount<?> cRollDeltaClrOverEpsW = Amount.valueOf(
				databaseReader.getClRWDClrOverEpsWVsARLambda(taperRatioWing, aspectRatioWing), // var0, var1
				SI.RADIAN.inverse().times(NonSI.DEGREE_ANGLE.inverse())
				);
		
		// Cl_r_W
		Amount<?> cRollrWing = cRollrOverCL1.times(cL1).plus(cRollDeltaClrOverGammaW.times(dihedralWing)).plus(cRollDeltaClrOverEpsW.times(aerodynamicTwistWing));
		
		System.out.println(">>>> Cl_r/CL1 @ mach = 0: " + cRollrOverCL1AtMachZero);
		System.out.println(">>>> Cl_r/CL1: " + cRollrOverCL1);
		System.out.println(">>>> DCl_r/Gamma_W: " + cRollDeltaClrOverGammaW);
		System.out.println(">>>> DCl_r/eps_W: " + cRollDeltaClrOverEpsW);
		System.out.println(">>>>>> Cl_r_W: " + cRollrWing);
		
		// --------------------------------------------------
		// Calculation of Cl_r_V
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_r_V");
		System.out.println("-------------------------");
		
		// Cl_r_V
		Amount<?> cRollrV =
				cYBetaV
				.times(
						zV.times(
								Math.cos(angleOfAttack.doubleValue(SI.RADIAN))
						).minus(
								xV.times(
										Math.sin(angleOfAttack.doubleValue(SI.RADIAN))
								)
						)
				).divide(spanWing)
				.times(
						zV.times(
								Math.sin(angleOfAttack.doubleValue(SI.RADIAN))
						).plus(
								xV.times(
										Math.cos(angleOfAttack.doubleValue(SI.RADIAN))
								)
						)
				).divide(spanWing).times(-2)
				.to(SI.RADIAN.inverse());
		
		System.out.println(">>>>>> Cl_r_V: " + cRollrV);
		
		// --------------------------------------------------
		// Calculation of Cl_r
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_r");
		System.out.println("-------------------------");
		
		// Cl_r
		Amount<?> cRollr = cRollrWing.plus(cRollrV);
		
		System.out.println(">>>>>>>> Cl_r: " + cRollr);
		
		// --------------------------------------------------
		// Calculation of CN_beta_B
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CN_beta_B");
		System.out.println("-------------------------");
		
		// K_N
		Amount<Length> lengthFuselage = fuselage.getFuselageLength();
		Amount<Area> sideSurfaceFuselage = Amount.valueOf(
				100,
				SI.SQUARE_METRE
				);
		Amount<Length> z1 = Amount.valueOf(
				fuselage.getZOutlineXZUpperAtX(0.25*lengthFuselage.doubleValue(SI.METER)) - fuselage.getZOutlineXZLowerAtX(0.25*lengthFuselage.doubleValue(SI.METER)),
				SI.METER
				);
		Amount<Length> z2 = Amount.valueOf(
				fuselage.getZOutlineXZUpperAtX(0.75*lengthFuselage.doubleValue(SI.METER)) - fuselage.getZOutlineXZLowerAtX(0.75*lengthFuselage.doubleValue(SI.METER)),
				SI.METER
				);
		// Assuming that max height fuselage and max width fuselage are the height and the width of the cylinder trunk fuselage 
		Amount<Length> zMax = fuselage.getSectionCylinderHeight();
		Amount<Length> wMax = fuselage.getSectionCylinderWidth();
		double lengthFuselageSquaredOverSideSurfaceFuselage = Math.pow(lengthFuselage.doubleValue(SI.METER), 2)/sideSurfaceFuselage.doubleValue(SI.SQUARE_METRE);
		double xCGOverLengthFuselage = xCG.doubleValue(SI.METER)/lengthFuselage.doubleValue(SI.METER);
		double squareRootZ1OverZ2 = Math.sqrt(z1.doubleValue(SI.METER)/z2.doubleValue(SI.METER));
		double maxHeightFuselageOverMaxWidthFuselage = zMax.doubleValue(SI.METER)/wMax.doubleValue(SI.METER);
		
		double cYKappaN = databaseReader.getCNBetaBKNVsXCGOverLBSquaredLBOverSBSSquareRootH1OverH2HBOverWB(
				lengthFuselageSquaredOverSideSurfaceFuselage,
				xCGOverLengthFuselage,
				squareRootZ1OverZ2,
				maxHeightFuselageOverMaxWidthFuselage
				);
		
		System.out.println(">> l_B: " + lengthFuselage);
		System.out.println(">> S_B_S: " + sideSurfaceFuselage);
		System.out.println(">> z_1: " + z1);
		System.out.println(">> z_2: " + z2);
		System.out.println(">> z_Max: " + zMax);
		System.out.println(">> w_Max: " + wMax);
		
		// K_Re_B
		double reynoldsFuselage = 1e7;
		double reynoldsFuselageTimes1eMinus6 = reynoldsFuselage*1e-6;
		
		double cYKappaReB = databaseReader.getCNBetaBKReBVsReLBTimes1eMinus6(reynoldsFuselageTimes1eMinus6);
		
		System.out.println(">> Re_B: " + reynoldsFuselage);
		System.out.println(">>>> K_N: " + cYKappaN);
		System.out.println(">>>> K_Re_B: " + cYKappaReB);
				
		// CN_beta_B
		Amount<?> cNBetaB = Amount.valueOf(
				- cYKappaN*cYKappaReB*sideSurfaceFuselage.doubleValue(SI.SQUARE_METRE)/surfaceWing.doubleValue(SI.SQUARE_METRE)*lengthFuselage.doubleValue(SI.METER)/spanWing.doubleValue(SI.METER),
				NonSI.DEGREE_ANGLE.inverse()
				).to(SI.RADIAN.inverse());
		
		System.out.println(">>>>>> CN_beta_B: " + cNBetaB);
		
		// --------------------------------------------------
		// Calculation of CN_beta_V
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CN_beta_V");
		System.out.println("-------------------------");
		
		// CN_beta_V
		Amount<?> cNBetaV =
				cYBetaV
				.times(
						zV.times(
								Math.sin(angleOfAttack.doubleValue(SI.RADIAN))
						).plus(
								xV.times(
										Math.cos(angleOfAttack.doubleValue(SI.RADIAN))
								)
						)
				).divide(spanWing).opposite();
		
		System.out.println(">>>>>> CN_beta_V: " + cNBetaV);
		
		// --------------------------------------------------
		// Calculation of CN_beta
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CN_beta");
		System.out.println("-------------------------");
		
		// CN_beta
		Amount<?> cNBeta = cNBetaB.plus(cNBetaV);
		
		System.out.println(">>>>>> CN_beta: " + cNBeta);
		
		// --------------------------------------------------
		// Calculation of CN_delta_A
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CN_delta_A");
		System.out.println("-------------------------");
		
		// Delta_K_N_A
		double innerKNA = databaseReader.getCNDeltaAKNAVsEtaARLambda(taperRatioWing, aspectRatioWing, etaInboardAileron);
		double outerKNA = databaseReader.getCNDeltaAKNAVsEtaARLambda(taperRatioWing, aspectRatioWing, etaOutboardAileron);
		double deltaKNA = outerKNA - innerKNA;
		
		// CN_delta_A
		Amount<?> cNDeltaA = cRollDeltaA.times(-deltaKNA*cL1);
		
		System.out.println(">>>> KNA_I: " + innerKNA);
		System.out.println(">>>> KNA_O: " + outerKNA);
		System.out.println(">>>> Delta KNA: " + deltaKNA);
		System.out.println(">>>>>> CN_delta_A: " + cNDeltaA);
		
		// --------------------------------------------------
		// Calculation of CN_delta_R
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CN_delta_R");
		System.out.println("-------------------------");
		
		// CN_delta_R
		Amount<?> cNDeltaR =
				cYDeltaR
				.times(
						zR.times(
								Math.sin(angleOfAttack.doubleValue(SI.RADIAN))
						).plus(
								xR.times(
										Math.cos(angleOfAttack.doubleValue(SI.RADIAN))
								)
						)
				).divide(spanWing).opposite();
		
		System.out.println(">>>>>> CN_delta_R: " + cNDeltaR);
		
		// --------------------------------------------------
		// Calculation of CN_p_W
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CN_p_W");
		System.out.println("-------------------------");
		
		// CN_p/CL1
		double cFactor = 
				(
						(
								aspectRatioWing + 4*Math.cos(
										sweepAngleC4Wing.doubleValue(SI.RADIAN)
										)
								)/(
										aspectRatioWing*bFactor + 4*Math.cos(
												sweepAngleC4Wing.doubleValue(SI.RADIAN)
												)
										)
						)*(
								aspectRatioWing*bFactor + 0.5*(aspectRatioWing*bFactor + 4*Math.cos(
										sweepAngleC4Wing.doubleValue(SI.RADIAN)
										))*Math.pow(
												Math.tan(sweepAngleC4Wing.doubleValue(SI.RADIAN)),
												2
												)
								)/(
										aspectRatioWing + 0.5*(aspectRatioWing + 4*Math.cos(
												sweepAngleC4Wing.doubleValue(SI.RADIAN)
												))*Math.pow(
														Math.tan(sweepAngleC4Wing.doubleValue(SI.RADIAN)),
														2
														)
										);
		double staticMargin = -0.2;
		Amount<?> cNpOverCL1AtMachZero = Amount.valueOf(
				-(
						aspectRatioWing + 6*(
								aspectRatioWing + Math.cos(
										sweepAngleC4Wing.doubleValue(SI.RADIAN)
										)
								)*(
										staticMargin*Math.tan(
												sweepAngleC4Wing.doubleValue(SI.RADIAN)
												)/aspectRatioWing
										+ Math.pow(
												Math.tan(sweepAngleC4Wing.doubleValue(SI.RADIAN)),
												2
												)/12
										)
						)/(
								6*(
										aspectRatioWing + Math.cos(sweepAngleC4Wing.doubleValue(SI.RADIAN))
										)
								),
				SI.RADIAN.inverse()
				);
		Amount<?> cNpOverCL1 = cNpOverCL1AtMachZero.times(cFactor);
		
		// DCN_p/eps_W
		Amount<?> cNDeltaCNpOverEpsW = Amount.valueOf(
				databaseReader.getCNPWDCNPOverEpsWVsARLambda(taperRatioWing, aspectRatioWing),
				SI.RADIAN.inverse().times(NonSI.DEGREE_ANGLE.inverse())
				);
		
		System.out.println(">>>> DCN_p/eps_W: " + cNDeltaCNpOverEpsW);
		
		// CN_p_W
		Amount<?> cNpW = cRollpWB.times(-Math.tan(angleOfAttack.doubleValue(SI.RADIAN)))
				.plus(cRollp.times(Math.tan(angleOfAttack.doubleValue(SI.RADIAN))))
				.plus(cNpOverCL1.times(cL1))
				.plus(cNDeltaCNpOverEpsW.times(aerodynamicTwistWing));
		
		System.out.println(">>>>>> CN_p_W: " + cNpW);
		
		// --------------------------------------------------
		// Calculation of CN_p_V
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CN_p_V");
		System.out.println("-------------------------");
		
		// CN_p_V
		Amount<?> cNpV =
				cYBetaV
				.times(
						zV.times(
								Math.cos(angleOfAttack.doubleValue(SI.RADIAN))
						).minus(
								xV.times(
										Math.sin(angleOfAttack.doubleValue(SI.RADIAN))
								)
						).minus(zV)
				).divide(spanWing)
				.times(
						zV.times(
								Math.sin(angleOfAttack.doubleValue(SI.RADIAN))
						).plus(
								xV.times(
										Math.cos(angleOfAttack.doubleValue(SI.RADIAN))
								)
						)
				).divide(spanWing).times(-2)
				.to(SI.RADIAN.inverse());
		
		System.out.println(">>>>>> CN_p_V: " + cNpV);
		
		// --------------------------------------------------
		// Calculation of CN_p
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CN_p");
		System.out.println("-------------------------");
		
		// CN_p
		Amount<?> cNp = cNpW.plus(cNpV);
		
		System.out.println(">>>>>>>> CN_p: " + cNp);
		
		// --------------------------------------------------
		// Calculation of CN_r_W
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CN_r_W");
		System.out.println("-------------------------");

		// CN_r/CL1^2
		Amount<?> cNrOverSquaredCL1 = Amount.valueOf(
				databaseReader.getCNRWCNROverSquaredCLift1VsARLambdaLC4XBarACMinusXBarCG(staticMargin, sweepAngleC4Wing, aspectRatioWing, taperRatioWing),
				SI.RADIAN.inverse()
				);

		System.out.println(">> MS: " + staticMargin);
		System.out.println(">>>> CN_r/CL1^2: " + cNrOverSquaredCL1);

		// CN_r/CD0
		Amount<?> cNrOverCD0 = Amount.valueOf(
				databaseReader.getCNRWCNROverCD0BarVsARLC4XBarACMinusXBarCG(staticMargin, sweepAngleC4Wing, aspectRatioWing),
				SI.RADIAN.inverse()
				);

		System.out.println(">>>> CN_r/CD0: " + cNrOverCD0);

		// CN_r_W
		double cD0 = 10;
		Amount<?> cNrW = cNrOverSquaredCL1.times(Math.pow(cL1, 2))
				.plus(cNrOverCD0.times(cD0));

		System.out.println(">>>>>> CN_r_W: " + cNrW);

		// --------------------------------------------------
		// Calculation of CN_r_V
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CN_r_V");
		System.out.println("-------------------------");

		// CN_r_V
		Amount<?> cNrV =
				cYBetaV
				.times(
						(
								zV.times(
										Math.sin(angleOfAttack.doubleValue(SI.RADIAN))
										).plus(
												xV.times(
														Math.cos(angleOfAttack.doubleValue(SI.RADIAN))
														)
												)
								).divide(spanWing)
						).pow(2).times(2)
				.to(SI.RADIAN.inverse());

		System.out.println(">>>>>> CN_r_V: " + cNrV);

		// --------------------------------------------------
		// Calculation of CN_r
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CN_r");
		System.out.println("-------------------------");

		// CN_r
		Amount<?> cNr = cNrW.plus(cNrV);

		System.out.println(">>>>>>>> CN_r: " + cNr);
		
		// --------------------------------------------------
		// Calculation of CY_beta_W
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CY_beta_W");
		System.out.println("-------------------------");
		
		// CY_beta_W
		Amount<?> cYBetaW = Amount.valueOf(
				-0.0001*dihedralWing.abs().doubleValue(NonSI.DEGREE_ANGLE),
				NonSI.DEGREE_ANGLE.inverse()
				).to(SI.RADIAN.inverse());
				
		System.out.println(">>>>>> CY_beta_W: " + cYBetaW);
		
		// --------------------------------------------------
		// Calculation of CY_beta_B
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CY_beta_B");
		System.out.println("-------------------------");
		
		// K_int
		double cYKappaInt = 0.0;
		double zWOverSemiHeightFuselage = 2*zW.doubleValue(SI.METER)/heightFuselage.doubleValue(SI.METER);
		
		if(zWOverSemiHeightFuselage > 0)
			cYKappaInt = 1.50*zWOverSemiHeightFuselage;
		else
			cYKappaInt = -1.88*zWOverSemiHeightFuselage;
		
		// S_P->V
		// Assuming that x1 is the coordinate corresponding to the middle of the tail trunk fuselage
		Amount<Length> x1 = fuselage.getNoseLength().plus(fuselage.getCylinderLength()).plus(fuselage.getFuselageLength()).divide(2);
		Amount<Length> x0 = fuselage.getFuselageLength().times(0.378).plus(x1.times(0.527));
		
		Amount<Area> surfacePArrowV = Amount.valueOf(
				Math.pow(
						fuselage.getEquivalentDiameterAtX(x0.doubleValue(SI.METER)),
						2
						)*Math.PI/4,
				SI.SQUARE_METRE
				);
		
		System.out.println(">> X_1: " + x1);
		System.out.println(">> X_0: " + x0);
		
		// CY_beta_B
		Amount<?> cYBetaB = Amount.valueOf(
				-2*cYKappaInt*surfacePArrowV.doubleValue(SI.SQUARE_METRE)/surfaceWing.doubleValue(SI.SQUARE_METRE),
				SI.RADIAN.inverse()
				);
		
		System.out.println(">>>> K_int: " + cYKappaInt);
		System.out.println(">>>> S_P->V: " + surfacePArrowV);
		System.out.println(">>>>>> CY_beta_B: " + cYBetaB);
		
		// --------------------------------------------------
		// Calculation of CY_beta_H
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CY_beta_H");
		System.out.println("-------------------------");
		
		// CY_beta_H
		double etaHTimes1MinusdSigmaOverdBeta =
				0.724
				+ 3.06*(surfaceHTail.doubleValue(SI.SQUARE_METRE)/surfaceWing.doubleValue(SI.SQUARE_METRE))/(1 + Math.cos(sweepAngleC4Wing.doubleValue(SI.RADIAN)))
				+ 0.4*zWOverHeightFuselage
				+ 0.009*aspectRatioWing;
		
		Amount<?> cYBetaH = Amount.valueOf(
				-0.0001*dihedralHTail.abs().doubleValue(NonSI.DEGREE_ANGLE)*etaHTimes1MinusdSigmaOverdBeta*surfaceHTail.doubleValue(SI.SQUARE_METRE)/surfaceWing.doubleValue(SI.SQUARE_METRE),
				NonSI.DEGREE_ANGLE.inverse()
				).to(SI.RADIAN.inverse());
		
		System.out.println(">>>>>> CY_beta_H: " + cYBetaH);
		
		// --------------------------------------------------
		// Calculation of CY_beta
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CY_beta");
		System.out.println("-------------------------");
		
		// CY_beta
		Amount<?> cYBeta = cYBetaW.plus(cYBetaB).plus(cYBetaH).plus(cYBetaV);
		
		System.out.println(">>>>>> CY_beta: " + cYBeta);
		
		// Assuming that CY_delta_A = 0
		
		// --------------------------------------------------
		// Calculation of CY_p
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CY_p");
		System.out.println("-------------------------");
		
		// CY_p
		Amount<?> cYp =
				cYBetaV
				.times(
						zV.times(
								Math.cos(angleOfAttack.doubleValue(SI.RADIAN))
						).minus(
								xV.times(
										Math.sin(angleOfAttack.doubleValue(SI.RADIAN))
								)
						)
				).divide(spanWing).times(2);
		
		System.out.println(">>>>>> CY_p: " + cYp);
		
		// --------------------------------------------------
		// Calculation of CY_r
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of CY_r");
		System.out.println("-------------------------");
		
		// CY_r
		Amount<?> cYr =
				cYBetaV
				.times(
						zV.times(
								Math.sin(angleOfAttack.doubleValue(SI.RADIAN))
						).plus(
								xV.times(
										Math.cos(angleOfAttack.doubleValue(SI.RADIAN))
								)
						)
				).divide(spanWing).times(-2);
		
		System.out.println(">>>>>> CY_r: " + cYr);
		
	}

}
