package analyses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

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
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.LiftCalc;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

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
	
	public static void initializeDataForDownwashCanard(ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager) {
		
	}
	
	public static void initializeDataForDownwash(ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager) {
		
		Map<ComponentEnum, LiftingSurfaceAerodynamicsManager> _liftingSurfaceAerodynamicManagers = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers();
		IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
		double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();
		Amount<Length> currentAltitude = aerodynamicAndStabilityManager.getCurrentAltitude();
		
		// for downwash estimation
		Amount<Length> _zACRootWing;
		Amount<Length> _horizontalDistanceQuarterChordWingHTail;
		Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailPARTIAL = null;
		Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE = null;
		Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE;

		
		//...................................................................................
		// PRELIMINARY CHECKS
		//...................................................................................
		/*
		 * Check on ALPHA ZERO LIFT in the LSAerodynamicsManager of the wing. If this is not present in the task list, it will be calculated
		 * using a defaul method. 
		 */
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)) {

			CalcAlpha0L calcAlpha0L = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcAlpha0L();
			calcAlpha0L.integralMeanWithTwist();

			_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
					AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT, 
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
					);
		}
		

		//...................................................................................
		// DISTANCE BETWEEN WING VORTEX PLANE AND THE AERODYNAMIC CENTER OF THE HTAIL
		//...................................................................................
		aerodynamicAndStabilityManager.setZACRootWing(Amount.valueOf(
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) 
				- (
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXACAirfoilVsY().get(0)*
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getChordsBreakPoints().get(0).doubleValue(SI.METER)*
						Math.tan(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(SI.RADIAN))
						),
				SI.METER
				));

		//Horizontal and vertical distance
		
		//--------------------new 
		aerodynamicAndStabilityManager.set_horizontalDistanceSlingerland(Amount.valueOf(
				_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER) -
				(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER) + 
						(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER) *
								Math.cos(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(SI.RADIAN)))
						),
				SI.METER));
		
		if ( (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) > 0 
				&& _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) > 0 ) 
				|| (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) < 0 
						&& _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) < 0 ) ) {

			aerodynamicAndStabilityManager.set_verticalDistanceSlingerland(Amount.valueOf(
					_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) 
					- aerodynamicAndStabilityManager.getZACRootWing().doubleValue(SI.METER),
					SI.METER
					));

		}

		if ( (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) > 0 
				&& _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) < 0 ) 
				|| (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) < 0 
						&& _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) > 0 ) ) { // different sides

			if(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) < 0 ){

				aerodynamicAndStabilityManager.set_verticalDistanceSlingerland(Amount.valueOf(
						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) 
						+ Math.abs(aerodynamicAndStabilityManager.getZACRootWing().doubleValue(SI.METER)),
						SI.METER
						));

			}

			if(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) > 0 ){
				aerodynamicAndStabilityManager.set_verticalDistanceSlingerland(Amount.valueOf(
						-( Math.abs(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER)) 
								+ aerodynamicAndStabilityManager.getZACRootWing().doubleValue(SI.METER)
								),
						SI.METER
						));	
			}
		}
		
		
		//----------------------------------------------------------------
		
		_horizontalDistanceQuarterChordWingHTail = Amount.valueOf(
				(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER)
						+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getChordsBreakPoints().get(0).doubleValue(SI.METER)/4) - 
				(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)
						+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getChordsBreakPoints().get(0).doubleValue(SI.METER)/4),
				SI.METER
				);

		if ( (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) > 0 
				&& _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) > 0 ) 
				|| (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) < 0 
						&& _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) < 0 ) ) {

			_verticalDistanceZeroLiftDirectionWingHTailPARTIAL = Amount.valueOf(
					_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) 
					- aerodynamicAndStabilityManager.getZACRootWing().doubleValue(SI.METER),
					SI.METER
					);

		}

		if ( (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) > 0 
				&& _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) < 0 ) 
				|| (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) < 0 
						&& _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) > 0 ) ) { // different sides

			if(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) < 0 ){

				_verticalDistanceZeroLiftDirectionWingHTailPARTIAL = Amount.valueOf(
						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) 
						+ Math.abs(aerodynamicAndStabilityManager.getZACRootWing().doubleValue(SI.METER)),
						SI.METER
						);

			}

			if(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) > 0 ){
				_verticalDistanceZeroLiftDirectionWingHTailPARTIAL = Amount.valueOf(
						-( Math.abs(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER)) 
								+ aerodynamicAndStabilityManager.getZACRootWing().doubleValue(SI.METER)
								),
						SI.METER
						);	
			}
		}

		// the horizontal distance is always the same, the vertical changes in function of the angle of attack.

		if (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) 
				< _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER)
				){

			
			_verticalDistanceZeroLiftDirectionWingHTailCOMPLETE = 
					Amount.valueOf(
							_verticalDistanceZeroLiftDirectionWingHTailPARTIAL.doubleValue(SI.METER) + (
									(_horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER) *
											Math.tan(- _theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(SI.RADIAN) +
													_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING)
													.getAlphaZeroLift().get(
															MethodEnum.INTEGRAL_MEAN_TWIST
															)
													.doubleValue(SI.RADIAN)
													)
											)
									),
							SI.METER
							);
		}

		if (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) 
				> _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) 
				) {

			_verticalDistanceZeroLiftDirectionWingHTailCOMPLETE = Amount.valueOf(
					_verticalDistanceZeroLiftDirectionWingHTailPARTIAL.doubleValue(SI.METER) - (
							(_horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER) *
									Math.tan(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(SI.RADIAN) -
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING)
											.getAlphaZeroLift().get(
													MethodEnum.INTEGRAL_MEAN_TWIST
													)
											.doubleValue(SI.RADIAN)
											)
									)
							),
					SI.METER);
		}


		_verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE = Amount.valueOf(
				_verticalDistanceZeroLiftDirectionWingHTailCOMPLETE.doubleValue(SI.METER) * 
				Math.cos(- _theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(SI.RADIAN) +
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING)
						.getAlphaZeroLift().get(
								MethodEnum.INTEGRAL_MEAN_TWIST
								)
						.doubleValue(SI.RADIAN)
						),
				SI.METER);
		
		aerodynamicAndStabilityManager.setDeltaEForEquilibrium(MyArrayUtils.convertDoubleArrayToListOfAmount((MyArrayUtils.linspaceDouble(
				_theAerodynamicBuilderInterface.getMaximumElevatorDeflection().doubleValue(NonSI.DEGREE_ANGLE),
				5, 
				10
				)),
				NonSI.DEGREE_ANGLE)); 
	}

	
	
	// TODO : CONTINUE HERE
	// passa le variabili di input necessarie e sostituisci le variabili. poi richiama il metodo sa AC v2, dopo calculate canard data.
	public static void calculateDownwashDueToCanard(
			ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
			) {
		
	}
	
	public static void calculateDownwashDueToWing(
			ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
			) {
//
//		Map<ComponentEnum, LiftingSurfaceAerodynamicsManager> _liftingSurfaceAerodynamicManagers = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers();
//		IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
//		double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();
//		Amount<Length> currentAltitude = aerodynamicAndStabilityManager.getCurrentAltitude();
//		
//		//...................................................................................
//		// PRELIMINARY CHECKS
//		//...................................................................................
//		Amount<?> cLAlphaWingCurrent = null;
//		Amount<Angle> alphaZeroLiftWingCurrent = null;
//		Double cL0WingCurrent = null;
//		Double[] liftCurveWingCurrent = null;
//		
//		/*
//		 * Check on CL ALPHA in the LSAerodynamicsManager of the wing. If this is not present in the task list, it will be calculated
//		 * using a defaul method. 
//		 */
//		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_ALPHA)) {
//			
//			CalcCLAlpha calcCLAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCLAlpha();
//			calcCLAlpha.nasaBlackwell();
//			cLAlphaWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().get(MethodEnum.NASA_BLACKWELL);
//			
//		}
//		else
//			cLAlphaWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().get(
//					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_ALPHA)
//					);
//		
//		/*
//		 * Check on ALPHA ZERO LIFT in the LSAerodynamicsManager of the wing. If this is not present in the task list, it will be calculated
//		 * using a defaul method. 
//		 */
//		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)) {
//			
//			CalcAlpha0L calcAlpha0L = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcAlpha0L();
//			calcAlpha0L.integralMeanWithTwist();
//			alphaZeroLiftWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLift().get(MethodEnum.INTEGRAL_MEAN_TWIST);
//			
//		}
//		else {
//			alphaZeroLiftWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLift().get(
//					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
//					);
//			alphaZeroLiftWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLift().get(MethodEnum.INTEGRAL_MEAN_TWIST);
//		}
//		/*
//		 * Check on CL0 in the LSAerodynamicsManager of the wing. If this is not present in the task list, it will be calculated
//		 * using a defaul method. 
//		 */
//		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_ZERO)) {
//			
//			CalcCL0 calcCL0 = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCL0();
//			calcCL0.nasaBlackwell();
//			cL0WingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero().get(MethodEnum.NASA_BLACKWELL);
//			
//		}
//		else
//			cL0WingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero().get(
//					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_ZERO)
//					);
//		
//		/*
//		 * Check on the LIFT CURVE in the LSAerodynamicsManager of the wing. If this is not present in the task list, it will be calculated
//		 * using a defaul method. 
//		 */
//		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {
//			
//			CalcLiftCurve calcLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcLiftCurve();
//			calcLiftCurve.nasaBlackwell(currentMachNumber);
//			liftCurveWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(MethodEnum.NASA_BLACKWELL);
//			
//		}
//		else
//			liftCurveWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(
//					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
//					);
//		
//		/////////////////////////////////////////////////////////////////////////////////////
//		// DOWNWASH ARRAY 
//		//...................................................................................
//		
//		//Initializing Maps
//		
//		Map<Boolean, List<Amount<Angle>>> downwashAngleConstant = new HashMap<>();
//		Map<Boolean, List<Double>> downwashGradientConstant = new HashMap<>();
//		
//		Map<MethodEnum, Map<Boolean, List<Amount<Angle>>>> downwashAngleMethod = new HashMap<>();
//		Map<MethodEnum, Map<Boolean, List<Double>>> downwashGradientMethod = new HashMap<>();
//		
//		//...................................................................................		
//		// ROSKAM (constant gradient)
//		//...................................................................................		
//		// calculate cl alpha at M=0
//		double cLAlphaMachZero = LiftCalc.calculateCLAlphaAtMachNasaBlackwell(
//				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSemiSpan(),
//				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(),
//				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution(),
//				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getChordDistribution(),
//				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getXLEDistribution(), 
//				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDihedralDistribution(),
//				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getTwistDistribution(),
//				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLiftDistribution(),
//				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getVortexSemiSpanToSemiSpanRatio(),
//				0.0,
//				currentAltitude
//				);
//
//		// Roskam method
//		List<Double> downwashGradientConstantList = new ArrayList<>();
//		for(int i=0; i<aerodynamicAndStabilityManager.getAlphaBodyList().size(); i++)
//			downwashGradientConstantList.add(
//					AerodynamicCalc.calculateDownwashRoskamWithMachEffect(
//							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(), 
//							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(), 
//							_horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER) 
//							/ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER), 
//							_verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE.doubleValue(SI.METER) 
//							/ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER), 
//							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
//							cLAlphaMachZero, 
//							cLAlphaWingCurrent.to(SI.RADIAN.inverse()).getEstimatedValue()
//							)
//					);
//
//		
//		downwashGradientConstant.put(Boolean.TRUE, downwashGradientConstantList);
//
//
//
//		double epsilonZeroRoskam = - downwashGradientConstant.get(Boolean.TRUE).get(0)
//				* alphaZeroLiftWingCurrent.doubleValue(NonSI.DEGREE_ANGLE);
//
//		List<Amount<Angle>> downwashAngleConstantList = new ArrayList<>();
//		for (int i=0; i<_theAerodynamicBuilderInterface.getNumberOfAlphasBody(); i++)
//			downwashAngleConstantList.add(
//					Amount.valueOf(
//							epsilonZeroRoskam 
//							+ downwashGradientConstant.get(Boolean.TRUE).get(0)
//							* aerodynamicAndStabilityManager.getAlphaWingList().get(i).doubleValue(NonSI.DEGREE_ANGLE),
//							NonSI.DEGREE_ANGLE
//							)	
//					);
//
//		downwashAngleConstant.put(
//				Boolean.TRUE,
//				downwashAngleConstantList
//				);
//
//
//		//...................................................................................
//		// ROSKAM (non linear gradient)
//		//...................................................................................		
//
////		downwashAngleMethod = new HashMap<>();
////		downwashGradientMethod = new HashMap<>();
////		
////		downwashAngleConstant = new HashMap<>();
////		downwashGradientConstant = new HashMap<>();
//
//		/* FIXME */
////		downwashGradientNonLinear.put(
////				MethodEnum.ROSKAM,
////				AerodynamicCalc.calculateVariableDownwashGradientRoskamWithMachEffect(
////						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
////						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getTaperRatioEquivalent(),
////						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
////						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes(),
////						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes(),
////						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
////						alphaZeroLiftWingCurrent,
////						_horizontalDistanceQuarterChordWingHTail,
////						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
////						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSweepQuarterChordEquivalent(),
////						cLAlphaMachZero,
////						cLAlphaWingCurrent.to(SI.RADIAN.inverse()).getEstimatedValue(), 
////						_alphaBodyList
////						)
////				);
//
//
////		downwashAngleNonLinear.put(
////				MethodEnum.ROSKAM,
////				AerodynamicCalc.calculateDownwashAngleFromDownwashGradient(
////						downwashGradientNonLinear.get(MethodEnum.ROSKAM),
////						_alphaBodyList,
////						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
////						alphaZeroLiftWingCurrent
////						)
////				);
////
////		_verticalDistanceZeroLiftDirectionWingHTailVariable.put(
////				MethodEnum.ROSKAM,
////				AerodynamicCalc.calculateVortexPlaneHorizontalTailVerticalDistance(
////						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
////						alphaZeroLiftWingCurrent,
////						_horizontalDistanceQuarterChordWingHTail,
////						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
////						_alphaBodyList, 
////						downwashAngleNonLinear.get(MethodEnum.ROSKAM)
////						)	
////				);
//
//		//------------- filling global for Roskam
//		downwashGradientMethod.put(MethodEnum.ROSKAM, downwashGradientConstant);	
//		downwashAngleMethod.put(MethodEnum.ROSKAM, downwashAngleConstant);
//		
//		
//		//...................................................................................
//		// SLINGERLAND (constant gradient)
//		//...................................................................................
//		
//		downwashAngleConstant = new HashMap<>();
//		downwashGradientConstant = new HashMap<>();
//		downwashAngleConstantList = new ArrayList<>();
//		downwashGradientConstantList = new ArrayList<>();
//
//		for (int i=0; i<_theAerodynamicBuilderInterface.getNumberOfAlphasBody(); i++){
//			double cl = 
//					cLAlphaWingCurrent.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue() 
//					* aerodynamicAndStabilityManager.getAlphaWingList().get(i).doubleValue(NonSI.DEGREE_ANGLE) 
//					+ cL0WingCurrent;
//
//			downwashAngleConstantList.add(
//					AerodynamicCalc.calculateDownwashAngleLinearSlingerland(
//							_horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER), 
//							_verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE.doubleValue(SI.METER), 
//							cl, 
//							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
//							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(), 
//							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSemiSpan()
//							).to(NonSI.DEGREE_ANGLE)
//					);
//		}
//
//		downwashAngleConstant.put(Boolean.TRUE, downwashAngleConstantList);
//		
//
//		downwashGradientConstant.put(
//				Boolean.TRUE,
//				MyArrayUtils.convertDoubleArrayToListDouble(
//						MyArrayUtils.convertFromDoubleToPrimitive(
//								MyMathUtils.calculateArrayFirstDerivative(
//										MyArrayUtils.convertListOfAmountTodoubleArray(aerodynamicAndStabilityManager.getAlphaBodyList()),
//										MyArrayUtils.convertListOfAmountTodoubleArray(
//												downwashAngleConstant
//												.get(Boolean.TRUE)
//												)
//										)
//								)
//						)
//				);
//
//		//...................................................................................
//		// SLINGERLAND (non linear gradient)
//		//...................................................................................
//		downwashAngleConstant.put(
//				Boolean.FALSE,
//				AerodynamicCalc.calculateDownwashAngleNonLinearSlingerland(
//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes(),
//						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes(),
//						alphaZeroLiftWingCurrent,
//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSemiSpan(), 
//						_horizontalDistanceQuarterChordWingHTail,
//						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
//						MyArrayUtils.convertToDoublePrimitive(liftCurveWingCurrent),
//						MyArrayUtils.convertListOfAmountTodoubleArray(
//								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean().stream()
//								.map(x -> x.to(NonSI.DEGREE_ANGLE))
//								.collect(Collectors.toList())
//								),
//						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(
//								aerodynamicAndStabilityManager.getAlphaBodyList().stream()
//								.map(x -> x.to(NonSI.DEGREE_ANGLE))
//								.collect(Collectors.toList())
//								))
//						)
//				);
//
////		downwashAngleNonLinear.put(
////				MethodEnum.SLINGERLAND,
////				AerodynamicCalc.calculateDownwashAngleNonLinearSlingerland(
////						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
////						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes(),
////						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes(),
////						alphaZeroLiftWingCurrent,
////						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSweepQuarterChordEquivalent(),
////						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
////						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSemiSpan(), 
////						_horizontalDistanceSlingerland,
////						_verticalDistanceSlingerland, 
////						MyArrayUtils.convertToDoublePrimitive(liftCurveWingCurrent),
////						MyArrayUtils.convertListOfAmountTodoubleArray(
////								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean().stream()
////								.map(x -> x.to(NonSI.DEGREE_ANGLE))
////								.collect(Collectors.toList())
////								),
////						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(
////								_alphaBodyList.stream()
////								.map(x -> x.to(NonSI.DEGREE_ANGLE))
////								.collect(Collectors.toList())
////								))
////						)
////				);
//
//		downwashGradientConstant.put(
//				Boolean.FALSE,
//				MyArrayUtils.convertDoubleArrayToListDouble(
//						MyArrayUtils.convertFromDoubleToPrimitive(
//								MyMathUtils.calculateArrayFirstDerivative(
//										MyArrayUtils.convertListOfAmountTodoubleArray(aerodynamicAndStabilityManager.getAlphaBodyList()),
//										MyArrayUtils.convertListOfAmountTodoubleArray(
//												downwashAngleConstant
//												.get(Boolean.FALSE)
//												)
//										)
//								)
//						)
//				);
//
//		
//		_verticalDistanceZeroLiftDirectionWingHTailVariable.put(
//				MethodEnum.SLINGERLAND,
//				AerodynamicCalc.calculateVortexPlaneHorizontalTailVerticalDistance(
//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
//						alphaZeroLiftWingCurrent,
//						_horizontalDistanceQuarterChordWingHTail,
//						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
//						aerodynamicAndStabilityManager.getAlphaBodyList(), 
//						downwashAngleConstant.get(Boolean.FALSE)
//						)	
//				);
//		
//		
//		//----------filling slingerland maps
//		
//		downwashAngleMethod.put(MethodEnum.SLINGERLAND, downwashAngleConstant);
//		downwashGradientMethod.put(MethodEnum.SLINGERLAND, downwashGradientConstant);
//		
//		//...................................................................................
//		// FROM INPUT (non linear downwash gradient assigned by the user)
//		//...................................................................................
//		
//		downwashAngleConstant = new HashMap<>();
//		downwashGradientConstant = new HashMap<>();
//		
//		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.WING_DOWNWASH))
//			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).equals(MethodEnum.INPUT)) {
//				
//				downwashGradientConstant.remove(Boolean.TRUE);
//				downwashAngleConstant.remove(Boolean.TRUE);
//				
//				downwashGradientConstant.put(
//						Boolean.FALSE,
//						_alphaBodyList.stream()
//						.map(ab -> _theAerodynamicBuilderInterface.getAircraftDownwashGradientFunction().value(ab.doubleValue(NonSI.DEGREE_ANGLE)))
//						.collect(Collectors.toList())
//						);
//				
//				downwashAngleConstant.put(
//						Boolean.FALSE,
//						AerodynamicCalc.calculateDownwashAngleFromDownwashGradient(
//								downwashGradientConstant.get(Boolean.FALSE),
//								aerodynamicAndStabilityManager.getAlphaBodyList(),
//								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
//								alphaZeroLiftWingCurrent
//								)
//						);
//				
//				downwashAngleMethod.put(MethodEnum.INPUT, downwashAngleConstant);
//				downwashGradientMethod.put(MethodEnum.INPUT, downwashGradientConstant);
//				
//			}
//		
//		// Filling the global maps ...
//		_downwashGradientMap.put(ComponentEnum.WING, downwashGradientMethod);
//		_downwashAngleMap.put(ComponentEnum.WING, downwashAngleMethod);
//		
	}
	

}
