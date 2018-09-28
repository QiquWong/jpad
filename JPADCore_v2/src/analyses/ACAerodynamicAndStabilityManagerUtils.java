package analyses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;

import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcAlpha0L;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcAlphaStall;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcAlphaStar;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCD0;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCDWave;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCL0;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCLAlpha;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCLAtAlpha;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCLAtAlphaHighLift;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCLStar;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCLmax;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCMAlpha;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCMAtAlpha;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCMac;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcDragDistributions;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcHighLiftCurve;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcHighLiftDevicesEffects;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcLiftCurve;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcLiftDistributions;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcMachCr;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcMomentDistribution;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcXAC;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyArrayUtils;

public class ACAerodynamicAndStabilityManagerUtils {

	public static void calculateLiftingSurfaceDataSemiempirical(
			ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager,
			ComponentEnum type
			) {
		
		LiftingSurfaceAerodynamicsManager liftingSurfaceAerodynamicManager = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(type);
		Amount<Angle> alphaComponentCurrent = null;
		switch (type) {
		case WING:
			alphaComponentCurrent = aerodynamicAndStabilityManager.getAlphaWingCurrent();
			break;
		case HORIZONTAL_TAIL:
			alphaComponentCurrent = aerodynamicAndStabilityManager.getAlphaHTailCurrent();
			break;
		case VERTICAL_TAIL:
			alphaComponentCurrent = aerodynamicAndStabilityManager.getBetaVTailCurrent();
			break;
		case CANARD:
			alphaComponentCurrent = aerodynamicAndStabilityManager.getAlphaCanardCurrent();
			break;
		default:
			break;
		}
		
		double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();
		Amount<Length> currentAltitude = aerodynamicAndStabilityManager.getCurrentAltitude();
		
		//.........................................................................................................................
		//	CL_AT_ALPHA  
		CalcCLAtAlpha calcCLAtAlpha = liftingSurfaceAerodynamicManager.new CalcCLAtAlpha();
		calcCLAtAlpha.nasaBlackwellCompleteCurve(alphaComponentCurrent);
		
		//.........................................................................................................................
		//	CRITICAL_MACH 
		CalcMachCr calcMachCr = liftingSurfaceAerodynamicManager.new CalcMachCr();
		calcMachCr.kroo(liftingSurfaceAerodynamicManager.getCLAtAlpha().get(MethodEnum.NASA_BLACKWELL));

		//.........................................................................................................................
		//	AERODYNAMIC_CENTER
		CalcXAC calcXAC = liftingSurfaceAerodynamicManager.new CalcXAC();
		calcXAC.datcomNapolitano();

		//.........................................................................................................................
		//	CL_ALPHA
		CalcCLAlpha calcCLAlpha = liftingSurfaceAerodynamicManager.new CalcCLAlpha();
		if(liftingSurfaceAerodynamicManager.getTheLiftingSurface().getType().equals(ComponentEnum.VERTICAL_TAIL))
			calcCLAlpha.helmboldDiederich(currentMachNumber);
		else
			calcCLAlpha.nasaBlackwell();

		//.........................................................................................................................
		//	CL_ZERO
		CalcCL0 calcCL0 = liftingSurfaceAerodynamicManager.new CalcCL0();
		calcCL0.nasaBlackwell();

		//.........................................................................................................................
		//	CL_STAR
		CalcCLStar calcCLStar = liftingSurfaceAerodynamicManager.new CalcCLStar();
		calcCLStar.nasaBlackwell();

		//.........................................................................................................................
		//	CL_MAX
		CalcCLmax calcCLmax = liftingSurfaceAerodynamicManager.new CalcCLmax();
		calcCLmax.nasaBlackwell();

		//.........................................................................................................................
		//	ALPHA_ZERO_LIFT
		CalcAlpha0L calcAlpha0L = liftingSurfaceAerodynamicManager.new CalcAlpha0L();
		calcAlpha0L.integralMeanWithTwist();

		//.........................................................................................................................
		//	ALPHA_STAR
		CalcAlphaStar calcAlphaStar = liftingSurfaceAerodynamicManager.new CalcAlphaStar();
		calcAlphaStar.meanAirfoilWithInfluenceAreas();

		//.........................................................................................................................
		//	ALPHA_STALL
		CalcAlphaStall calcAlphaStall = liftingSurfaceAerodynamicManager.new CalcAlphaStall();
		calcAlphaStall.fromAlphaMaxLinearNasaBlackwell(currentMachNumber);

		//.........................................................................................................................
		//	LIFT_CURVE_3D 
		CalcLiftCurve calcLiftCurve = liftingSurfaceAerodynamicManager.new CalcLiftCurve();
		calcLiftCurve.nasaBlackwell(currentMachNumber);

		//.........................................................................................................................
		//	LIFT_DISTRIBUTION
		CalcLiftDistributions calcLiftDistributions = liftingSurfaceAerodynamicManager.new CalcLiftDistributions();
		calcLiftDistributions.nasaBlackwell();

		//.........................................................................................................................
		//	CD0
		CalcCD0 calcCD0 = liftingSurfaceAerodynamicManager.new CalcCD0();
		calcCD0.semiempirical(currentMachNumber, currentAltitude);

		//.........................................................................................................................
		//	CD_WAVE 
		CalcCDWave calcCDWave = liftingSurfaceAerodynamicManager.new CalcCDWave();
		calcCDWave.lockKornWithKroo();

		//.........................................................................................................................
		//	DRAG_DISTRIBUTION
		CalcDragDistributions calcDragDistributions = liftingSurfaceAerodynamicManager.new CalcDragDistributions();
		calcDragDistributions.nasaBlackwell(currentMachNumber);

		//.........................................................................................................................
		//	CM_AC
		CalcCMac calcCMac = liftingSurfaceAerodynamicManager.new CalcCMac();
		calcCMac.fromAirfoilDistribution();

		//.........................................................................................................................
		//	CM_ALPHA
		CalcCMAlpha calcCMAlpha = liftingSurfaceAerodynamicManager.new CalcCMAlpha();
		calcCMAlpha.nasaBlackwell();

		//.........................................................................................................................
		//	MOMENT_CURVE_3D
		analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcMomentCurve calcMomentCurve = liftingSurfaceAerodynamicManager.new CalcMomentCurve();
		calcMomentCurve.fromAirfoilDistribution();

		//.........................................................................................................................
		//	CM_AT_ALPHA  
		CalcCMAtAlpha calcCMAtAlpha = liftingSurfaceAerodynamicManager.new CalcCMAtAlpha();
		calcCMAtAlpha.fromAirfoilDistribution(alphaComponentCurrent);

		//.........................................................................................................................
		//	MOMENT_DISTRIBUTION
		CalcMomentDistribution calcMomentDistribution = liftingSurfaceAerodynamicManager.new CalcMomentDistribution();
		calcMomentDistribution.fromAirfoilDistribution();

		//.........................................................................................................................
		//	CONTROL SURFACE EFFECTS
		if(liftingSurfaceAerodynamicManager.getTheLiftingSurface().getType().equals(ComponentEnum.WING)) {
			if(liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.TAKE_OFF)
					|| liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.LANDING)) {

				CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = liftingSurfaceAerodynamicManager.new CalcHighLiftDevicesEffects();
				if(liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.TAKE_OFF))
					calcHighLiftDevicesEffects.semiempirical(
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getSlatDeflectionTakeOff(),
							currentMachNumber
							);
				else if(liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.LANDING))
					calcHighLiftDevicesEffects.semiempirical(
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getFlapDeflectionLanding(), 
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getSlatDeflectionLanding(),
							currentMachNumber
							);
			}
		}

		//.........................................................................................................................
		//	CONTROL_SURFACE_LIFT_CURVE_3D
		if(liftingSurfaceAerodynamicManager.getTheLiftingSurface().getType().equals(ComponentEnum.WING)) {
			if(liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.TAKE_OFF)
					|| liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.LANDING)) {
				
				CalcHighLiftCurve calcHighLiftCurve = liftingSurfaceAerodynamicManager.new CalcHighLiftCurve();
				if(liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.TAKE_OFF))
					calcHighLiftCurve.semiempirical(
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getSlatDeflectionLanding(), 
							currentMachNumber 
							);
				else if(liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.LANDING))
					calcHighLiftCurve.semiempirical(
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getFlapDeflectionLanding(), 
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getSlatDeflectionLanding(), 
							currentMachNumber 
							);
				
			}
		}
		else if(liftingSurfaceAerodynamicManager.getTheLiftingSurface().getType().equals(ComponentEnum.HORIZONTAL_TAIL)
				|| liftingSurfaceAerodynamicManager.getTheLiftingSurface().getType().equals(ComponentEnum.CANARD)) {
			
			List<Amount<Angle>> deltaDeflectionList = new ArrayList<>();
			CalcHighLiftCurve calcHighLiftCurve = liftingSurfaceAerodynamicManager.new CalcHighLiftCurve();
			
			switch (type) {
			case HORIZONTAL_TAIL:
				deltaDeflectionList.addAll(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getDeltaElevatorList());
				deltaDeflectionList.stream().forEach(delta -> delta.to(NonSI.DEGREE_ANGLE));
				deltaDeflectionList.stream().forEach(delta -> {
					calcHighLiftCurve.semiempirical(
							Arrays.asList(delta), 
							new ArrayList<>(), 
							currentMachNumber 
							);
					aerodynamicAndStabilityManager.getCurrent3DHorizontalTailLiftCurve().put(
							delta,
							MyArrayUtils.convertDoubleArrayToListDouble(
									aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers()
									.get(ComponentEnum.HORIZONTAL_TAIL)
									.getLiftCoefficient3DCurveHighLift()
									.get(MethodEnum.SEMIEMPIRICAL)
									)
							);
				});
				break;
			case CANARD:
				deltaDeflectionList.addAll(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getDeltaCanardControlSurfaceList());
				deltaDeflectionList.stream().forEach(delta -> delta.to(NonSI.DEGREE_ANGLE));
				deltaDeflectionList.stream().forEach(delta -> {
					calcHighLiftCurve.semiempirical(
							Arrays.asList(delta), 
							new ArrayList<>(), 
							currentMachNumber 
							);
					aerodynamicAndStabilityManager.getCurrent3DCanardLiftCurve().put(
							delta,
							MyArrayUtils.convertDoubleArrayToListDouble(
									aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers()
									.get(ComponentEnum.CANARD)
									.getLiftCoefficient3DCurveHighLift()
									.get(MethodEnum.SEMIEMPIRICAL)
									)
							);
				});
				break;
			default:
				break;
			}
			
		}

		//.........................................................................................................................
		//	CL_AT_ALPHA_CONTROL_SURFACE 
		if(liftingSurfaceAerodynamicManager.getTheLiftingSurface().getType().equals(ComponentEnum.WING)) {
			if(liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.TAKE_OFF)
					|| liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.LANDING)) {

				CalcCLAtAlphaHighLift calcCLAtAlphaHighLift = liftingSurfaceAerodynamicManager.new CalcCLAtAlphaHighLift();
				if(liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.TAKE_OFF))
					calcCLAtAlphaHighLift.semiempirical(
							alphaComponentCurrent,
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getSlatDeflectionLanding(), 
							currentMachNumber, 
							currentAltitude
							);
				else if(liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.LANDING))
					calcCLAtAlphaHighLift.semiempirical(
							alphaComponentCurrent,
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getFlapDeflectionLanding(), 
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getSlatDeflectionLanding(), 
							currentMachNumber, 
							currentAltitude
							);
			}
		}
	}
	
	
}
