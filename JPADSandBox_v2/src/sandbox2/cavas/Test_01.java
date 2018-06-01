package sandbox2.cavas;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

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
import igeo.IVec2R.Len;
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
		AircraftUtils.performAnalyses(aircraft, operatingConditions, AircraftUtils.pathToAnalysesXML, "IRON_CANARD_cavas");
		
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
				databaseReader.getClbetaWBClbetaOverCLift1Lc2VsLc2ARlambda(taperRatioWing, aspectRatioWing, sweepAngleC2Wing), // var0, var1, var2
				NonSI.DEGREE_ANGLE.inverse()
				);

		// K_M_Lambda_W
		double aspectRatioOverCosSweepAngleC2Wing = aspectRatioWing/Math.cos(sweepAngleC2Wing.doubleValue(SI.RADIAN));
		double mach = operatingConditions.getMachCruise();
		double machTimesCosSweepAngleC2Wing = mach * Math.cos(sweepAngleC2Wing.doubleValue(SI.RADIAN));
		
		System.out.println(">> Mach: " + mach);
		System.out.println(">> AR/cos(Lambda_c/2) wing: " + aspectRatioOverCosSweepAngleC2Wing);
		System.out.println(">> Mach cos(Lambda_c/2) wing: " + machTimesCosSweepAngleC2Wing);
		
		double cRollKappaMachLambdaW = databaseReader.getClbetaWBKMLVsMachTimesCosLc2AROverCosLc2(
				aspectRatioOverCosSweepAngleC2Wing, // var0
				machTimesCosSweepAngleC2Wing // var1
				);
		
		// K_f_W
		Amount<Length> spanWing = wing.getSpan();
		int idxBreakPointTipWing = wing.getXLEBreakPoints().size()-1;
		Amount<Length> xLETipBFRWing = wing.getXApexConstructionAxes().plus(wing.getXLEBreakPoints().get(idxBreakPointTipWing));				
		double aOverSpanWing = xLETipBFRWing.doubleValue(SI.METER)/spanWing.doubleValue(SI.METER);
		
		System.out.println(">> A wing: " + xLETipBFRWing);
		System.out.println(">> span wing: " + spanWing);
		System.out.println(">> A/b wing: " + aOverSpanWing);
		
		double cRollKappafW = databaseReader.getClbetaWBKfVsAOverBAROverCosLc2(aspectRatioOverCosSweepAngleC2Wing, aOverSpanWing); // var0, var1
		
		// Cl_beta/CL1_AR_W
		Amount<?> cRollBetaOverCL1WingBodyAR = Amount.valueOf(
				databaseReader.getClbetaWBClbetaOverCLift1ARVsARlambda(taperRatioWing, aspectRatioWing), // var0, var1
				NonSI.DEGREE_ANGLE.inverse()
				);
		
		// Cl_beta/Gamma_W
		Amount<?> cRollBetaOverGammaW = Amount.valueOf(
				databaseReader.getClbetaWBClbetaOverGammaWVsARLc2lambda(taperRatioWing, sweepAngleC2Wing, aspectRatioWing), // var0, var1, var2
				NonSI.DEGREE_ANGLE.pow(2).inverse()
				);
		
		// K_M_Gamma_W
		double cRollKappaMachGammaW = databaseReader.getClbetaWBKMGammaVsMachTimesCosLc2AROverCosLc2(
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
				databaseReader.getClbetaWBDClbetaOverEpsWTimesTanLc4VsARlambda(taperRatioWing, aspectRatioWing), // var0, var1
				NonSI.DEGREE_ANGLE.pow(2).inverse()
				);
		
		// Cl_beta_WB
		Amount<Velocity> velocityTAS = operatingConditions.getTASCruise();
		Amount<Mass> maxTakeOffWeight = Amount.valueOf(54500.0, SI.KILOGRAM); // IRON MTOW TODO do the weight analysis
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
				databaseReader.getClbetaWBClbetaOverGammaWVsARLc2lambda(taperRatioHTail, sweepAngleC2HTail, aspectRatioHTail), // var0, var1, var2
				NonSI.DEGREE_ANGLE.pow(2).inverse()
				);

		// K_M_Gamma_H
		double aspectRatioOverCosSweepAngleC2HTail = aspectRatioHTail/Math.cos(sweepAngleC2HTail.doubleValue(SI.RADIAN));
		double machTimesCosSweepAngleC2HTail = mach * Math.cos(sweepAngleC2HTail.doubleValue(SI.RADIAN));
		
		System.out.println(">> Mach: " + mach);
		System.out.println(">> AR/cos(Lambda_c/2) horizontal tail: " + aspectRatioOverCosSweepAngleC2HTail);
		System.out.println(">> Mach cos(Lambda_c/2) horizontal tail: " + machTimesCosSweepAngleC2HTail);
		
		double cRollKappaMachGammaH = databaseReader.getClbetaWBKMGammaVsMachTimesCosLc2AROverCosLc2(
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
				databaseReader.getClbetaWBDClbetaOverEpsWTimesTanLc4VsARlambda(taperRatioHTail, aspectRatioHTail), // var0, var1
				NonSI.DEGREE_ANGLE.pow(2).inverse()
				);
		
		// Cl_beta_H
		Amount<Angle> dihedralHTail = hTail.getDihedralMean();
		Amount<Angle> aerodynamicTwistHTail = hTail.getEquivalentWing().getPanels().get(0).getTwistAerodynamicAtTip();
		Amount<Angle> sweepAngleC4HTail = hTail.getEquivalentWing().getPanels().get(0).getSweepQuarterChord().to(NonSI.DEGREE_ANGLE);
		double etaH = 0.9; // TODO take the degradation factor of dynamic pressure horizontal tail
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
		// Calculation of Cl_beta_V
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_beta_V");
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
		double bVOver2TimesR1 = spanVTail.doubleValue(SI.METER)/r1.doubleValue(SI.METER);
		
		System.out.println(">> span vertical tail: " + spanVTail);
		System.out.println(">> r_1: " + r1);

		double cYawKappaYV = databaseReader.getCybetaVKYVVsBVOver2TimesR1(bVOver2TimesR1);
		
		// CY_beta_V
		double taperRatioVTail = vTail.getEquivalentWing().getPanels().get(0).getTaperRatio();
		double aspectRatioVTail = vTail.getAspectRatio();
		Amount<Angle> sweepAngleLEVTail = vTail.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE);

		System.out.println(">> taper ratio vertical tail: " + taperRatioVTail);
		System.out.println(">> aspect ratio vertical tail: " + aspectRatioVTail);
		System.out.println(">> sweep angle @ LE vertical tail: " + sweepAngleLEVTail);

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

		Amount<?> cYawBetaV =
				cLAlphaV.abs().times(surfaceVTail).divide(surfaceWing)
				.times(-cYawKappaYV*etaVTimes1MinusdSigmaOverdBeta)
				.to(SI.RADIAN.inverse());
		
		// Cl_beta_V
		Amount<Angle> angleOfAttack = operatingConditions.getAlphaCurrentCruise().to(NonSI.DEGREE_ANGLE);
		Amount<Length> xV = vTail.getXApexConstructionAxes()
				.plus(vTail.getMeanAerodynamicChordLeadingEdgeX())
				.plus(vTail.getMeanAerodynamicChord()).times(0.25);
		Amount<Length> zV = vTail.getZApexConstructionAxes()
				.plus(vTail.getMeanAerodynamicChordLeadingEdgeZ());
		
		System.out.println(">> alpha_1: " + angleOfAttack);
		System.out.println(">> x_V: " + xV);
		System.out.println(">> z_V: " + zV);

		Amount<?> cRollBetaV =
				cYawBetaV
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
		
		System.out.println(">>>> K_Y_V: " + cYawKappaYV);
		System.out.println(">>>>>> CY_beta_V: " + cYawBetaV);
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
		
		double innerRME = databaseReader.getCldeltaARMEVsEtaLambdaBetaBetaTimesAROverKLambda(
				taperRatioWing,
				betaFactorTimesAspectRatioWingOverKWing,
				lambdaBetaFactorWing,
				etaInboardAileron
				); 
		double outerRME = databaseReader.getCldeltaARMEVsEtaLambdaBetaBetaTimesAROverKLambda(
				taperRatioWing,
				betaFactorTimesAspectRatioWingOverKWing,
				lambdaBetaFactorWing,
				etaOutboardAileron
				);
		double deltaRME = outerRME - innerRME;

		// Cl_delta_A
		Amount<?> cRollDeltaAFullChord = Amount.valueOf(
				deltaRME*kWing/betaFactor,
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
		double etaInboardRudder = vTail.getSymmetricFlaps().get(0).getTheSymmetricFlapInterface().getInnerStationSpanwisePosition();
		double etaOutboardRudder = vTail.getSymmetricFlaps().get(0).getTheSymmetricFlapInterface().getOuterStationSpanwisePosition();
		double innerKR = 0.5; // TODO take K_R_inner
		double outerKR = 0.8; // TODO take K_R_outer
		double deltaKR = outerKR - innerKR;
				
		// CY_delta_r
		double etaV = 0.9; // TODO take the degradation factor of dynamic pressure for the vertical tail
		Amount <?> cYawDeltaR = cLAlphaV.abs().times(surfaceVTail).divide(surfaceWing).times(etaV*deltaKR*tauR);
		
		System.out.println(">> degradation factor of dynamic pressure vertical tail: " + etaV);
		System.out.println(">>>> tau_R: " + tauR);
		System.out.println(">>>>>> CY_delta_r: " + cYawDeltaR);
		
		// --------------------------------------------------
		// Calculation of Cl_delta_r
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_delta_r");
		System.out.println("-------------------------");
		
		// CY_delta_r
		Amount<Length> xR = Amount.valueOf(
				15,
				SI.METER
				);
		
		//Amount <?> cRollDeltaR = cYawDeltaR
		
		
		
		// --------------------------------------------------
		// Calculation of Cl_p_WB
		// --------------------------------------------------
		System.out.println("-------------------------");
		System.out.println("Calculation of Cl_p_WB");
		System.out.println("-------------------------");

		// RDP_W
		Amount<?> rollingDampingParametersWing = Amount.valueOf(
				databaseReader.getClpWRDPVsLambdaBetaBetaTimesAROverKLambda(
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
				databaseReader.getClpWRDPVsLambdaBetaBetaTimesAROverKLambda(taperRatioHTail, betaFactorTimesAspectRatioHTailOverKHTail, lambdaBetaFactorHTail), // var0, var1, var2
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
		Amount<?> cRollpV = cYawBetaV.times((zV.divide(spanWing)).pow(2)).times(2);
		
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
				databaseReader.getClrWClrOverCLift1VsARLambdaLc4(taperRatioWing, aspectRatioWing, sweepAngleC2Wing),
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
				databaseReader.getClrWDClrOverEpsWVsARLambda(taperRatioWing, aspectRatioWing), // var0, var1
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
				cYawBetaV
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
		
	}

}
