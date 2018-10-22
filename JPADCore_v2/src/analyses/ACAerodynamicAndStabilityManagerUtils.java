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

import org.jcae.opencascade.jni.Geom_TrimmedCurve;
import org.jscience.physics.amount.Amount;

import analyses.ACAerodynamicAndStabilityManager_v2;
import analyses.ACAerodynamicAndStabilityManager.CalcTotalLiftCoefficient;
import analyses.fuselage.FuselageAerodynamicsManager;
import analyses.fuselage.FuselageAerodynamicsManager.CalcCD0Base;
import analyses.fuselage.FuselageAerodynamicsManager.CalcCD0Parasite;
import analyses.fuselage.FuselageAerodynamicsManager.CalcCD0Total;
import analyses.fuselage.FuselageAerodynamicsManager.CalcCD0Upsweep;
import analyses.fuselage.FuselageAerodynamicsManager.CalcCD0Windshield;
import analyses.fuselage.FuselageAerodynamicsManager.CalcCDAtAlpha;
import analyses.fuselage.FuselageAerodynamicsManager.CalcCDInduced;
import analyses.fuselage.FuselageAerodynamicsManager.CalcCM0;
import analyses.fuselage.FuselageAerodynamicsManager.CalcMomentCurve;
import analyses.fuselage.FuselageAerodynamicsManager.CalcPolar;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcAlpha0L;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcAlphaStall;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcAlphaStar;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCD0;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCDParasite;
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
import analyses.nacelles.NacelleAerodynamicsManager;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.MomentCalc;
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
		liftingSurfaceAerodynamicManager.setMomentumPole(
				liftingSurfaceAerodynamicManager.getXacLRF().get(MethodEnum.NASA_BLACKWELL )
				);

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


	public static void calculateFuselageDataSemiempirical(
			ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
			) {

		Amount<Angle> alphaComponentCurrent =  aerodynamicAndStabilityManager.getAlphaBodyCurrent();
		double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();
		
		FuselageAerodynamicsManager fuselageAerodynamicManagers = aerodynamicAndStabilityManager.getFuselageAerodynamicManagers().get(ComponentEnum.FUSELAGE);
		//.........................................................................................................................

		//.........................................................................................................................
		//	CD0_TOTAL
		analyses.fuselage.FuselageAerodynamicsManager.CalcCD0Total calcCD0Total = fuselageAerodynamicManagers.new CalcCD0Total();
		calcCD0Total.semiempirical();

		//.........................................................................................................................
		//	CD_INDUCED

		analyses.fuselage.FuselageAerodynamicsManager.CalcCDInduced calcCDInduced = fuselageAerodynamicManagers.new CalcCDInduced();
		calcCDInduced.semiempirical(alphaComponentCurrent, currentMachNumber);

		//.........................................................................................................................
		//	POLAR_CURVE_3D
		analyses.fuselage.FuselageAerodynamicsManager.CalcPolar calcPolar = fuselageAerodynamicManagers.new CalcPolar();
		calcPolar.semiempirical(currentMachNumber);

		//.........................................................................................................................
		//	CD_AT_ALPHA 
		analyses.fuselage.FuselageAerodynamicsManager.CalcCDAtAlpha calcCDAtAlpha = fuselageAerodynamicManagers.new CalcCDAtAlpha();
		calcCDAtAlpha.semiempirical(alphaComponentCurrent, currentMachNumber);
		
		//.........................................................................................................................
		//	CM0
		analyses.fuselage.FuselageAerodynamicsManager.CalcCM0 calcCM0 = fuselageAerodynamicManagers.new CalcCM0();
		calcCM0.fusDes();

		//.........................................................................................................................
		//	CM_ALPHA
		analyses.fuselage.FuselageAerodynamicsManager.CalcCMAlpha calcCMAlpha = fuselageAerodynamicManagers.new CalcCMAlpha();
		calcCMAlpha.fusDes();

		//.........................................................................................................................
		//	MOMENT_CURVE_3D

		analyses.fuselage.FuselageAerodynamicsManager.CalcMomentCurve calcMomentCurve = fuselageAerodynamicManagers.new CalcMomentCurve();
		calcMomentCurve.fusDes();
		//.........................................................................................................................
		//	CM_AT_ALPHA 

		analyses.fuselage.FuselageAerodynamicsManager.CalcCMAtAlpha calcCMAtAlpha = fuselageAerodynamicManagers.new CalcCMAtAlpha();
		calcCMAtAlpha.fusDes(alphaComponentCurrent);
	}

	public static void calculateNacellesDataSemiempirical(
			ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
			) {

		IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
		NacelleAerodynamicsManager nacelleAerodynamicManagers = aerodynamicAndStabilityManager.getNacelleAerodynamicManagers().get(ComponentEnum.NACELLE);
		double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();

		List<Amount<Angle>> alphaNacelleList = new ArrayList<>();
		// alpha nacelle current

		Amount<Angle> alphaComponentCurrent = aerodynamicAndStabilityManager.getAlphaNacelleCurrent();

		switch (_theAerodynamicBuilderInterface.getTheAircraft().getNacelles().getNacellesList().get(0).getMountingPosition()) {
		case WING:
			alphaNacelleList = (aerodynamicAndStabilityManager.getAlphaWingList());
			break;
		case FUSELAGE:
			alphaNacelleList = (aerodynamicAndStabilityManager.getAlphaBodyList());
			break;
		case HTAIL:
			alphaNacelleList = (aerodynamicAndStabilityManager.getAlphaHTailList());
			break;
		default:
			break;
		}

		//.........................................................................................................................
		//	CD0_TOTAL
		analyses.nacelles.NacelleAerodynamicsManager.CalcCD0Total calcCD0Total = nacelleAerodynamicManagers.new CalcCD0Total();
		calcCD0Total.semiempirical();

		//.........................................................................................................................
		//	CD_INDUCED

		analyses.nacelles.NacelleAerodynamicsManager.CalcCDInduced calcCDInduced = nacelleAerodynamicManagers.new CalcCDInduced();
		calcCDInduced.semiempirical(alphaComponentCurrent, currentMachNumber);

		//.........................................................................................................................
		//	POLAR_CURVE_3D
		analyses.nacelles.NacelleAerodynamicsManager.CalcPolar calcPolar = nacelleAerodynamicManagers.new CalcPolar();
		calcPolar.semiempirical(currentMachNumber);

		//.........................................................................................................................
		//	CD_AT_ALPHA 
		analyses.nacelles.NacelleAerodynamicsManager.CalcCDAtAlpha calcCDAtAlpha = nacelleAerodynamicManagers.new CalcCDAtAlpha();
		calcCDAtAlpha.semiempirical(alphaComponentCurrent, currentMachNumber);

		//.........................................................................................................................
		//	CM0

		analyses.nacelles.NacelleAerodynamicsManager.CalcCM0 calcCM0 = nacelleAerodynamicManagers.new CalcCM0();
		calcCM0.multhopp();

		//.........................................................................................................................
		//	CM_ALPHA

		analyses.nacelles.NacelleAerodynamicsManager.CalcCMAlpha calcCMAlpha = nacelleAerodynamicManagers.new CalcCMAlpha();
		calcCMAlpha.multhopp(
				_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getXApexConstructionAxes().to(SI.METER)
				.plus(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getMeanAerodynamicChord().to(SI.METER).divide(4))
				.minus(
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
						.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getPanels().get(0).getChordRoot().to(SI.METER))
						),
				aerodynamicAndStabilityManager.getDownwashGradientMap().get(ComponentEnum.WING)
				.get(MethodEnum.ROSKAM)
				.get(Boolean.TRUE)
				.get(0)
				);

		//.........................................................................................................................
		//	MOMENT_CURVE_3D

		analyses.nacelles.NacelleAerodynamicsManager.CalcMomentCurve calcMomentCurve = nacelleAerodynamicManagers.new CalcMomentCurve();
		calcMomentCurve.multhopp(
				_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getXApexConstructionAxes().to(SI.METER)
				.plus(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getMeanAerodynamicChord().to(SI.METER).divide(4))
				.minus(
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
						.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getPanels().get(0).getChordRoot().to(SI.METER))
						),
				aerodynamicAndStabilityManager.getDownwashGradientMap().get(ComponentEnum.WING).get(MethodEnum.ROSKAM).get(Boolean.TRUE).get(0)
				);

		//.........................................................................................................................
		//	CM_AT_ALPHA 
		analyses.nacelles.NacelleAerodynamicsManager.CalcCMAtAlpha calcCMAtAlpha = nacelleAerodynamicManagers.new CalcCMAtAlpha();
		calcCMAtAlpha.multhopp(
				alphaComponentCurrent.to(NonSI.DEGREE_ANGLE), 
				_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getXApexConstructionAxes().to(SI.METER)
				.plus(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getMeanAerodynamicChord().to(SI.METER).divide(4))
				.minus(
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
						.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getPanels().get(0).getChordRoot().to(SI.METER))
						),
				aerodynamicAndStabilityManager.getDownwashGradientMap().get(ComponentEnum.WING).get(MethodEnum.ROSKAM).get(Boolean.TRUE).get(0)
				);
	}

	public static void initializeDataForDownwashCanard(ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager) {

	}

	public static void initializeDataForDownwash(ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager) {

		Map<ComponentEnum, LiftingSurfaceAerodynamicsManager> _liftingSurfaceAerodynamicManagers = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers();
		IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
		double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();
		Amount<Length> currentAltitude = aerodynamicAndStabilityManager.getCurrentAltitude();

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

		aerodynamicAndStabilityManager.set_horizontalDistanceQuarterChordWingHTail(Amount.valueOf(
				(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER)
						+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getChordsBreakPoints().get(0).doubleValue(SI.METER)/4) - 
				(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)
						+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getChordsBreakPoints().get(0).doubleValue(SI.METER)/4),
				SI.METER
				));

		if ( (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) > 0 
				&& _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) > 0 ) 
				|| (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) < 0 
						&& _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) < 0 ) ) {

			aerodynamicAndStabilityManager.set_verticalDistanceZeroLiftDirectionWingHTailPARTIAL(Amount.valueOf(
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

				aerodynamicAndStabilityManager.set_verticalDistanceZeroLiftDirectionWingHTailPARTIAL(Amount.valueOf(
						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) 
						+ Math.abs(aerodynamicAndStabilityManager.getZACRootWing().doubleValue(SI.METER)),
						SI.METER
						));

			}

			if(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) > 0 ){
				aerodynamicAndStabilityManager.set_verticalDistanceZeroLiftDirectionWingHTailPARTIAL(Amount.valueOf(
						-( Math.abs(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER)) 
								+ aerodynamicAndStabilityManager.getZACRootWing().doubleValue(SI.METER)
								),
						SI.METER
						));	
			}
		}

		// the horizontal distance is always the same, the vertical changes in function of the angle of attack.

		if (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) 
				< _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER)
				){


			aerodynamicAndStabilityManager.set_verticalDistanceZeroLiftDirectionWingHTailCOMPLETE( 
					Amount.valueOf(
							aerodynamicAndStabilityManager.get_verticalDistanceZeroLiftDirectionWingHTailPARTIAL().doubleValue(SI.METER) + (
									(aerodynamicAndStabilityManager.get_horizontalDistanceQuarterChordWingHTail().doubleValue(SI.METER) *
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
							));
		}

		if (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) 
				> _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) 
				) {

			aerodynamicAndStabilityManager.set_verticalDistanceZeroLiftDirectionWingHTailCOMPLETE(Amount.valueOf(
					aerodynamicAndStabilityManager.get_verticalDistanceZeroLiftDirectionWingHTailPARTIAL().doubleValue(SI.METER) - (
							(aerodynamicAndStabilityManager.get_horizontalDistanceQuarterChordWingHTail().doubleValue(SI.METER) *
									Math.tan(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(SI.RADIAN) -
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING)
											.getAlphaZeroLift().get(
													MethodEnum.INTEGRAL_MEAN_TWIST
													)
											.doubleValue(SI.RADIAN)
											)
									)
							),
					SI.METER));
		}


		aerodynamicAndStabilityManager.set_verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE(Amount.valueOf(
				aerodynamicAndStabilityManager.get_verticalDistanceZeroLiftDirectionWingHTailCOMPLETE().doubleValue(SI.METER) * 
				Math.cos(- _theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(SI.RADIAN) +
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING)
				.getAlphaZeroLift().get(
						MethodEnum.INTEGRAL_MEAN_TWIST
						)
				.doubleValue(SI.RADIAN)
						),
				SI.METER));

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

		Map<ComponentEnum, LiftingSurfaceAerodynamicsManager> _liftingSurfaceAerodynamicManagers = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers();
		IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
		double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();
		Amount<Length> currentAltitude = aerodynamicAndStabilityManager.getCurrentAltitude();

		//...................................................................................
		// PRELIMINARY CHECKS
		//...................................................................................
		Amount<?> cLAlphaWingCurrent = null;
		Amount<Angle> alphaZeroLiftWingCurrent = null;
		Double cL0WingCurrent = null;
		Double[] liftCurveWingCurrent = null;

		/*
		 * Check on CL ALPHA in the LSAerodynamicsManager of the wing. If this is not present in the task list, it will be calculated
		 * using a defaul method. 
		 */
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_ALPHA)) {

			CalcCLAlpha calcCLAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCLAlpha();
			calcCLAlpha.nasaBlackwell();
			cLAlphaWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().get(MethodEnum.NASA_BLACKWELL);

		}
		else
			cLAlphaWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_ALPHA)
					);

		/*
		 * Check on ALPHA ZERO LIFT in the LSAerodynamicsManager of the wing. If this is not present in the task list, it will be calculated
		 * using a defaul method. 
		 */
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)) {

			CalcAlpha0L calcAlpha0L = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcAlpha0L();
			calcAlpha0L.integralMeanWithTwist();
			alphaZeroLiftWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLift().get(MethodEnum.INTEGRAL_MEAN_TWIST);

		}
		else {
			alphaZeroLiftWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLift().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
					);
			alphaZeroLiftWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLift().get(MethodEnum.INTEGRAL_MEAN_TWIST);
		}
		/*
		 * Check on CL0 in the LSAerodynamicsManager of the wing. If this is not present in the task list, it will be calculated
		 * using a defaul method. 
		 */
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_ZERO)) {

			CalcCL0 calcCL0 = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCL0();
			calcCL0.nasaBlackwell();
			cL0WingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero().get(MethodEnum.NASA_BLACKWELL);

		}
		else
			cL0WingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_ZERO)
					);

		/*
		 * Check on the LIFT CURVE in the LSAerodynamicsManager of the wing. If this is not present in the task list, it will be calculated
		 * using a defaul method. 
		 */
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

			CalcLiftCurve calcLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcLiftCurve();
			calcLiftCurve.nasaBlackwell(currentMachNumber);
			liftCurveWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(MethodEnum.NASA_BLACKWELL);

		}
		else
			liftCurveWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
					);

		/////////////////////////////////////////////////////////////////////////////////////
		// DOWNWASH ARRAY 
		//...................................................................................

		//Initializing Maps

		Map<Boolean, List<Amount<Angle>>> downwashAngleConstant = new HashMap<>();
		Map<Boolean, List<Double>> downwashGradientConstant = new HashMap<>();

		Map<MethodEnum, Map<Boolean, List<Amount<Angle>>>> downwashAngleMethod = new HashMap<>();
		Map<MethodEnum, Map<Boolean, List<Double>>> downwashGradientMethod = new HashMap<>();

		//...................................................................................		
		// ROSKAM (constant gradient)
		//...................................................................................		
		// calculate cl alpha at M=0
		double cLAlphaMachZero = LiftCalc.calculateCLAlphaAtMachNasaBlackwell(
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSemiSpan(),
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(),
				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution(),
				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getChordDistribution(),
				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getXLEDistribution(), 
				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDihedralDistribution(),
				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getTwistDistribution(),
				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLiftDistribution(),
				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getVortexSemiSpanToSemiSpanRatio(),
				0.0,
				currentAltitude
				);

		// Roskam method
		List<Double> downwashGradientConstantList = new ArrayList<>();
		for(int i=0; i<aerodynamicAndStabilityManager.getAlphaBodyList().size(); i++)
			downwashGradientConstantList.add(
					AerodynamicCalc.calculateDownwashRoskamWithMachEffect(
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(), 
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(), 
							aerodynamicAndStabilityManager.get_horizontalDistanceQuarterChordWingHTail().doubleValue(SI.METER) 
							/ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER), 
							aerodynamicAndStabilityManager.get_verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE().doubleValue(SI.METER) 
							/ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER), 
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
							cLAlphaMachZero, 
							cLAlphaWingCurrent.to(SI.RADIAN.inverse()).getEstimatedValue()
							)
					);


		downwashGradientConstant.put(Boolean.TRUE, downwashGradientConstantList);



		double epsilonZeroRoskam = - downwashGradientConstant.get(Boolean.TRUE).get(0)
				* alphaZeroLiftWingCurrent.doubleValue(NonSI.DEGREE_ANGLE);

		List<Amount<Angle>> downwashAngleConstantList = new ArrayList<>();
		for (int i=0; i<_theAerodynamicBuilderInterface.getNumberOfAlphasBody(); i++)
			downwashAngleConstantList.add(
					Amount.valueOf(
							epsilonZeroRoskam 
							+ downwashGradientConstant.get(Boolean.TRUE).get(0)
							* aerodynamicAndStabilityManager.getAlphaWingList().get(i).doubleValue(NonSI.DEGREE_ANGLE),
							NonSI.DEGREE_ANGLE
							)	
					);

		downwashAngleConstant.put(
				Boolean.TRUE,
				downwashAngleConstantList
				);


		//...................................................................................
		// ROSKAM (non linear gradient)
		//...................................................................................		

		//		downwashAngleMethod = new HashMap<>();
		//		downwashGradientMethod = new HashMap<>();
		//		
		//		downwashAngleConstant = new HashMap<>();
		//		downwashGradientConstant = new HashMap<>();

		/* FIXME */
		//		downwashGradientNonLinear.put(
		//				MethodEnum.ROSKAM,
		//				AerodynamicCalc.calculateVariableDownwashGradientRoskamWithMachEffect(
		//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
		//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getTaperRatioEquivalent(),
		//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
		//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes(),
		//						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes(),
		//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
		//						alphaZeroLiftWingCurrent,
		//						_horizontalDistanceQuarterChordWingHTail,
		//						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
		//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSweepQuarterChordEquivalent(),
		//						cLAlphaMachZero,
		//						cLAlphaWingCurrent.to(SI.RADIAN.inverse()).getEstimatedValue(), 
		//						_alphaBodyList
		//						)
		//				);


		//		downwashAngleNonLinear.put(
		//				MethodEnum.ROSKAM,
		//				AerodynamicCalc.calculateDownwashAngleFromDownwashGradient(
		//						downwashGradientNonLinear.get(MethodEnum.ROSKAM),
		//						_alphaBodyList,
		//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
		//						alphaZeroLiftWingCurrent
		//						)
		//				);
		//
		//		_verticalDistanceZeroLiftDirectionWingHTailVariable.put(
		//				MethodEnum.ROSKAM,
		//				AerodynamicCalc.calculateVortexPlaneHorizontalTailVerticalDistance(
		//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
		//						alphaZeroLiftWingCurrent,
		//						_horizontalDistanceQuarterChordWingHTail,
		//						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
		//						_alphaBodyList, 
		//						downwashAngleNonLinear.get(MethodEnum.ROSKAM)
		//						)	
		//				);

		//------------- filling global for Roskam
		downwashGradientMethod.put(MethodEnum.ROSKAM, downwashGradientConstant);	
		downwashAngleMethod.put(MethodEnum.ROSKAM, downwashAngleConstant);


		//...................................................................................
		// SLINGERLAND (constant gradient)
		//...................................................................................

		downwashAngleConstant = new HashMap<>();
		downwashGradientConstant = new HashMap<>();
		downwashAngleConstantList = new ArrayList<>();
		downwashGradientConstantList = new ArrayList<>();

		for (int i=0; i<_theAerodynamicBuilderInterface.getNumberOfAlphasBody(); i++){
			double cl = 
					cLAlphaWingCurrent.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue() 
					* aerodynamicAndStabilityManager.getAlphaWingList().get(i).doubleValue(NonSI.DEGREE_ANGLE) 
					+ cL0WingCurrent;

			downwashAngleConstantList.add(
					AerodynamicCalc.calculateDownwashAngleLinearSlingerland(
							aerodynamicAndStabilityManager.get_horizontalDistanceQuarterChordWingHTail().doubleValue(SI.METER), 
							aerodynamicAndStabilityManager.get_verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE().doubleValue(SI.METER), 
							cl, 
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(), 
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSemiSpan()
							).to(NonSI.DEGREE_ANGLE)
					);
		}

		downwashAngleConstant.put(Boolean.TRUE, downwashAngleConstantList);


		downwashGradientConstant.put(
				Boolean.TRUE,
				MyArrayUtils.convertDoubleArrayToListDouble(
						MyArrayUtils.convertFromDoubleToPrimitive(
								MyMathUtils.calculateArrayFirstDerivative(
										MyArrayUtils.convertListOfAmountTodoubleArray(aerodynamicAndStabilityManager.getAlphaBodyList()),
										MyArrayUtils.convertListOfAmountTodoubleArray(
												downwashAngleConstant
												.get(Boolean.TRUE)
												)
										)
								)
						)
				);

		//...................................................................................
		// SLINGERLAND (non linear gradient)
		//...................................................................................
		downwashAngleConstant.put(
				Boolean.FALSE,
				AerodynamicCalc.calculateDownwashAngleNonLinearSlingerland(
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes(),
						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes(),
						alphaZeroLiftWingCurrent,
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSemiSpan(), 
						aerodynamicAndStabilityManager.get_horizontalDistanceQuarterChordWingHTail(),
						aerodynamicAndStabilityManager.get_verticalDistanceZeroLiftDirectionWingHTailPARTIAL(), 
						MyArrayUtils.convertToDoublePrimitive(liftCurveWingCurrent),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean().stream()
								.map(x -> x.to(NonSI.DEGREE_ANGLE))
								.collect(Collectors.toList())
								),
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(
								aerodynamicAndStabilityManager.getAlphaBodyList().stream()
								.map(x -> x.to(NonSI.DEGREE_ANGLE))
								.collect(Collectors.toList())
								))
						)
				);

		//		downwashAngleNonLinear.put(
		//				MethodEnum.SLINGERLAND,
		//				AerodynamicCalc.calculateDownwashAngleNonLinearSlingerland(
		//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
		//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes(),
		//						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes(),
		//						alphaZeroLiftWingCurrent,
		//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSweepQuarterChordEquivalent(),
		//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
		//						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSemiSpan(), 
		//						_horizontalDistanceSlingerland,
		//						_verticalDistanceSlingerland, 
		//						MyArrayUtils.convertToDoublePrimitive(liftCurveWingCurrent),
		//						MyArrayUtils.convertListOfAmountTodoubleArray(
		//								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean().stream()
		//								.map(x -> x.to(NonSI.DEGREE_ANGLE))
		//								.collect(Collectors.toList())
		//								),
		//						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(
		//								_alphaBodyList.stream()
		//								.map(x -> x.to(NonSI.DEGREE_ANGLE))
		//								.collect(Collectors.toList())
		//								))
		//						)
		//				);

		downwashGradientConstant.put(
				Boolean.FALSE,
				MyArrayUtils.convertDoubleArrayToListDouble(
						MyArrayUtils.convertFromDoubleToPrimitive(
								MyMathUtils.calculateArrayFirstDerivative(
										MyArrayUtils.convertListOfAmountTodoubleArray(aerodynamicAndStabilityManager.getAlphaBodyList()),
										MyArrayUtils.convertListOfAmountTodoubleArray(
												downwashAngleConstant
												.get(Boolean.FALSE)
												)
										)
								)
						)
				);


		aerodynamicAndStabilityManager.get_verticalDistanceZeroLiftDirectionWingHTailVariable().put(
				MethodEnum.SLINGERLAND,
				AerodynamicCalc.calculateVortexPlaneHorizontalTailVerticalDistance(
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
						alphaZeroLiftWingCurrent,
						aerodynamicAndStabilityManager.get_horizontalDistanceQuarterChordWingHTail(),
						aerodynamicAndStabilityManager.get_verticalDistanceZeroLiftDirectionWingHTailPARTIAL(), 
						aerodynamicAndStabilityManager.getAlphaBodyList(), 
						downwashAngleConstant.get(Boolean.FALSE)
						)	
				);


		//----------filling slingerland maps

		downwashAngleMethod.put(MethodEnum.SLINGERLAND, downwashAngleConstant);
		downwashGradientMethod.put(MethodEnum.SLINGERLAND, downwashGradientConstant);

		//...................................................................................
		// FROM INPUT (non linear downwash gradient assigned by the user)
		//...................................................................................

		downwashAngleConstant = new HashMap<>();
		downwashGradientConstant = new HashMap<>();

		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.WING_DOWNWASH))
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).equals(MethodEnum.INPUT)) {

				downwashGradientConstant.remove(Boolean.TRUE);
				downwashAngleConstant.remove(Boolean.TRUE);

				downwashGradientConstant.put(
						Boolean.FALSE,
						aerodynamicAndStabilityManager.getAlphaBodyList().stream()
						.map(ab -> _theAerodynamicBuilderInterface.getAircraftDownwashGradientFunction().value(ab.doubleValue(NonSI.DEGREE_ANGLE)))
						.collect(Collectors.toList())
						);

				downwashAngleConstant.put(
						Boolean.FALSE,
						AerodynamicCalc.calculateDownwashAngleFromDownwashGradient(
								downwashGradientConstant.get(Boolean.FALSE),
								aerodynamicAndStabilityManager.getAlphaBodyList(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
								alphaZeroLiftWingCurrent
								)
						);

				downwashAngleMethod.put(MethodEnum.INPUT, downwashAngleConstant);
				downwashGradientMethod.put(MethodEnum.INPUT, downwashGradientConstant);

			}

		// Filling the global maps ...
		aerodynamicAndStabilityManager.getDownwashGradientMap().put(ComponentEnum.WING, downwashGradientMethod);
		aerodynamicAndStabilityManager.getDownwashAngleMap().put(ComponentEnum.WING, downwashAngleMethod);
		//		
	}

	public void calculateDownwashAngleCurrent(ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager) {
		Amount<Angle> currentDownwashAngle = null;
		IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();


		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.WING_DOWNWASH))

			currentDownwashAngle = Amount.valueOf(
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									aerodynamicAndStabilityManager.getAlphaBodyList()),
							MyArrayUtils.convertListOfAmountTodoubleArray(
									aerodynamicAndStabilityManager.getDownwashAngleMap()
									.get(ComponentEnum.WING)
									.get(_theAerodynamicBuilderInterface.getComponentTaskList()
											.get(ComponentEnum.AIRCRAFT)
											.get(AerodynamicAndStabilityEnum.WING_DOWNWASH))
									.get(_theAerodynamicBuilderInterface.getDownwashConstant())
									),
							aerodynamicAndStabilityManager.getAlphaBodyCurrent().doubleValue(NonSI.DEGREE_ANGLE)
							),
					NonSI.DEGREE_ANGLE
					);	

		else /* ...a default method will be used (SLINGERLAND NON LINEAR) */

			currentDownwashAngle = Amount.valueOf(
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									aerodynamicAndStabilityManager.getAlphaBodyList()),
							MyArrayUtils.convertListOfAmountTodoubleArray(
									aerodynamicAndStabilityManager.getDownwashAngleMap()
									.get(ComponentEnum.WING)
									.get(MethodEnum.SLINGERLAND)
									.get(Boolean.FALSE)
									),
							aerodynamicAndStabilityManager.getAlphaBodyCurrent().doubleValue(NonSI.DEGREE_ANGLE)
							),
					NonSI.DEGREE_ANGLE
					);

		aerodynamicAndStabilityManager.setCurrentDownwashAngle(currentDownwashAngle);

	}
	public static void calculateCurrentWingLiftCurve(
			ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
			) {

		IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
		double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();
		LiftingSurfaceAerodynamicsManager liftingSurfaceAerodynamicManager = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING);

		//.........................................................................................................................
		//	WING LIFT_CURVE_3D (EVENTUALLY WITH HIGH LIFT DEVICES AND WITH FUSELAGE EFFECTS)
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CL_TOTAL) ||
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

			List<Double> temporaryLiftCurve = new ArrayList<>();

			if(_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.TAKE_OFF || _theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.LANDING) {
				if(_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.TAKE_OFF) {

					CalcHighLiftCurve calcHighLiftCurveTakeOff = liftingSurfaceAerodynamicManager.new CalcHighLiftCurve();
					calcHighLiftCurveTakeOff.semiempirical(
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
							currentMachNumber
							);

					temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
							liftingSurfaceAerodynamicManager
							.getLiftCoefficient3DCurveHighLift()
							.get(MethodEnum.SEMIEMPIRICAL));
				}
				if( _theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.LANDING) {

					CalcHighLiftCurve calcHighLiftCurveLanding = liftingSurfaceAerodynamicManager.new CalcHighLiftCurve();
					calcHighLiftCurveLanding.semiempirical(
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(), 
							currentMachNumber
							);

					temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
							liftingSurfaceAerodynamicManager
							.getLiftCoefficient3DCurveHighLift()
							.get(MethodEnum.SEMIEMPIRICAL));
				}


				if(_theAerodynamicBuilderInterface.isFuselageEffectOnWingLiftCurve()){
					//CL ALPHA
					aerodynamicAndStabilityManager.set_clAlphaWingFuselage(
							LiftCalc.calculateCLAlphaFuselage(
									liftingSurfaceAerodynamicManager.getCLAlphaHighLift().get(
											MethodEnum.SEMIEMPIRICAL),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(), 
									Amount.valueOf(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getEquivalentDiameterAtX(
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)),
											SI.METER)
									));

					//CL ZERO
					aerodynamicAndStabilityManager.set_clZeroWingFuselage(
							-aerodynamicAndStabilityManager.get_clAlphaWingFuselage().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()*
							liftingSurfaceAerodynamicManager.getAlphaZeroLiftHighLift()
							.get(MethodEnum.SEMIEMPIRICAL).doubleValue(NonSI.DEGREE_ANGLE));

					//CL MAX
					aerodynamicAndStabilityManager.set_clMaxWingFuselage(
							liftingSurfaceAerodynamicManager.getCLMaxHighLift().get(MethodEnum.SEMIEMPIRICAL));

					//CL STAR
					aerodynamicAndStabilityManager.set_clStarWingFuselage(
							liftingSurfaceAerodynamicManager.getCLStarHighLift()
							.get(MethodEnum.SEMIEMPIRICAL));

					//ALPHA STAR
					aerodynamicAndStabilityManager.set_alphaStarWingFuselage(
							Amount.valueOf(
									(aerodynamicAndStabilityManager.get_clStarWingFuselage()-
											aerodynamicAndStabilityManager.get_clZeroWingFuselage())/
									aerodynamicAndStabilityManager.get_clAlphaWingFuselage().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(), 
									NonSI.DEGREE_ANGLE));

					//ALPHA stall
					double deltaAlphaStarDeg = 	aerodynamicAndStabilityManager.get_alphaStarWingFuselage().doubleValue(NonSI.DEGREE_ANGLE) - 
							liftingSurfaceAerodynamicManager.getAlphaStarHighLift()
							.get(MethodEnum.SEMIEMPIRICAL)
							.doubleValue(NonSI.DEGREE_ANGLE);

					aerodynamicAndStabilityManager.set_alphaStallWingFuselage(Amount.valueOf(
							liftingSurfaceAerodynamicManager.getAlphaStallHighLift()
							.get(MethodEnum.SEMIEMPIRICAL).doubleValue(NonSI.DEGREE_ANGLE) - deltaAlphaStarDeg, 
							NonSI.DEGREE_ANGLE));

					//CURVE

					temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(LiftCalc.calculateCLvsAlphaArray(
							aerodynamicAndStabilityManager.get_clZeroWingFuselage(),
							aerodynamicAndStabilityManager.get_clMaxWingFuselage(),
							aerodynamicAndStabilityManager.get_alphaStarWingFuselage(),
							aerodynamicAndStabilityManager.get_alphaStallWingFuselage(),
							aerodynamicAndStabilityManager.get_clAlphaWingFuselage(),
							MyArrayUtils.convertListOfAmountToDoubleArray(
									liftingSurfaceAerodynamicManager.getAlphaArray()
									)
							));
				}
			}

			if(_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.CLIMB || _theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.CRUISE) {

				temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
						liftingSurfaceAerodynamicManager
						.getLiftCoefficient3DCurve().get(MethodEnum.SEMIEMPIRICAL)
						);



				if(_theAerodynamicBuilderInterface.isFuselageEffectOnWingLiftCurve()){

					//.............................................................................
					//CL ALPHA
					aerodynamicAndStabilityManager.set_clAlphaWingFuselage(
							LiftCalc.calculateCLAlphaFuselage(
									liftingSurfaceAerodynamicManager.getCLAlpha().get(
											MethodEnum.NASA_BLACKWELL),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(), 
									Amount.valueOf(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getEquivalentDiameterAtX(
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)),
											SI.METER)
									));

					//CL ZERO
					aerodynamicAndStabilityManager.set_clZeroWingFuselage(
							-aerodynamicAndStabilityManager.get_clAlphaWingFuselage().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()*
							liftingSurfaceAerodynamicManager.getAlphaZeroLift()
							.get(MethodEnum.INTEGRAL_MEAN_TWIST).doubleValue(NonSI.DEGREE_ANGLE));

					//CL MAX
					aerodynamicAndStabilityManager.set_clMaxWingFuselage(
							liftingSurfaceAerodynamicManager.getCLMax().get(MethodEnum.NASA_BLACKWELL));

					//CL STAR
					aerodynamicAndStabilityManager.set_clStarWingFuselage(
							liftingSurfaceAerodynamicManager.getCLStar()
							.get(MethodEnum.NASA_BLACKWELL));

					//ALPHA STAR
					aerodynamicAndStabilityManager.set_alphaStarWingFuselage(
							Amount.valueOf(
									(aerodynamicAndStabilityManager.get_clStarWingFuselage()-
											aerodynamicAndStabilityManager.get_clZeroWingFuselage())/
									aerodynamicAndStabilityManager.get_clAlphaWingFuselage().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(), 
									NonSI.DEGREE_ANGLE));

					//ALPHA stall

					double deltaAlphaStarDeg = 	aerodynamicAndStabilityManager.get_alphaStarWingFuselage().doubleValue(NonSI.DEGREE_ANGLE) - 
							liftingSurfaceAerodynamicManager.getAlphaStar()
							.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS)
							.doubleValue(NonSI.DEGREE_ANGLE);

					aerodynamicAndStabilityManager.set_alphaStallWingFuselage(Amount.valueOf(
							liftingSurfaceAerodynamicManager.getAlphaStall()
							.get(MethodEnum.NASA_BLACKWELL).doubleValue(NonSI.DEGREE_ANGLE) - deltaAlphaStarDeg, 
							NonSI.DEGREE_ANGLE)); 

					//CURVE

					temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(LiftCalc.calculateCLvsAlphaArray(
							aerodynamicAndStabilityManager.get_clZeroWingFuselage(),
							aerodynamicAndStabilityManager.get_clMaxWingFuselage(),
							aerodynamicAndStabilityManager.get_alphaStarWingFuselage(),
							aerodynamicAndStabilityManager.get_alphaStallWingFuselage(),
							aerodynamicAndStabilityManager.get_clAlphaWingFuselage(),
							MyArrayUtils.convertListOfAmountToDoubleArray(
									liftingSurfaceAerodynamicManager.getAlphaArray()
									)
							));

				}
			}

			aerodynamicAndStabilityManager.setCurrent3DWingLiftCurve(temporaryLiftCurve);

		}
	}
	
	public static void calculateCurrentWingMomentCurve(
			ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
			) {

		IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
		double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();
		LiftingSurfaceAerodynamicsManager liftingSurfaceAerodynamicManager = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING);
		
		List<Double> temporaryMomentCurve = new ArrayList<>();
		
		if(_theAerodynamicBuilderInterface.isPerformWingAnalyses() == false) {
		analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcMomentCurve calcMomentCurve = liftingSurfaceAerodynamicManager.new CalcMomentCurve();
		calcMomentCurve.fromAirfoilDistribution();
		}
		
		if ( _theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.CLIMB ||  _theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.CRUISE) {
		temporaryMomentCurve = MyArrayUtils.convertDoubleArrayToListDouble(liftingSurfaceAerodynamicManager.getMoment3DCurve().get(MethodEnum.AIRFOIL_DISTRIBUTION));
		}
		
		if ( _theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.TAKE_OFF ||  _theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.LANDING) {
		
			for(int i=0; i<liftingSurfaceAerodynamicManager.getMoment3DCurve().get(MethodEnum.SEMIEMPIRICAL).length; i++) {
				temporaryMomentCurve.add(
						liftingSurfaceAerodynamicManager.getMoment3DCurve().get(MethodEnum.AIRFOIL_DISTRIBUTION)[i] +
						liftingSurfaceAerodynamicManager.getDeltaCMc4().get(MethodEnum.SEMIEMPIRICAL)
						);
			}
			
		}
			
		aerodynamicAndStabilityManager.setCurrent3DWingMomentCurve(temporaryMomentCurve);
	}
	
	public static void calculateHorizontalTailLiftCurveWithElevatorDeflection(
			ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
			) {
	
		IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
		LiftingSurfaceAerodynamicsManager liftingSurfaceAerodynamicManager = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL);
		
			_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach(de -> {

				List<Double> temporaryLiftHorizontalTail = new ArrayList<>();
				List<Amount<Angle>> temporaryDeList = new ArrayList<>();
				temporaryDeList.add(de);


				temporaryLiftHorizontalTail = MyArrayUtils.convertDoubleArrayToListDouble(
						LiftCalc.calculateCLvsAlphaArray(
								liftingSurfaceAerodynamicManager.getCLZero().get(MethodEnum.NASA_BLACKWELL),
								liftingSurfaceAerodynamicManager.getCLMax().get(MethodEnum.NASA_BLACKWELL),
								liftingSurfaceAerodynamicManager.getAlphaStar().get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS),
								liftingSurfaceAerodynamicManager.getAlphaStall().get(MethodEnum.NASA_BLACKWELL),
								liftingSurfaceAerodynamicManager.getCLAlpha().get(MethodEnum.NASA_BLACKWELL),
								MyArrayUtils.convertListOfAmountToDoubleArray(
										liftingSurfaceAerodynamicManager.getAlphaArray()
										)
								)
						).stream()
						.map(cL -> cL 
								+ (_theAerodynamicBuilderInterface.getTauElevatorFunction().value(de.doubleValue(NonSI.DEGREE_ANGLE))
										*liftingSurfaceAerodynamicManager.getCLAlpha().get(MethodEnum.NASA_BLACKWELL).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
										*de.doubleValue(NonSI.DEGREE_ANGLE)
										)
								)
						.collect(Collectors.toList());

				double tau;
				for(int i=0; i< _theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++) {
					tau = _theAerodynamicBuilderInterface.getTauElevatorFunction().value(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i).doubleValue(NonSI.DEGREE_ANGLE));
				}

				aerodynamicAndStabilityManager.getCurrent3DHorizontalTailLiftCurve().put(
						de, 
						temporaryLiftHorizontalTail);

			});

		}
	
	public static void calculateVerticalTailLiftCurveWithRudderDeflection(
			ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
			) {

		IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
		_theAerodynamicBuilderInterface.getDeltaRudderList().stream().forEach(dr -> {
			LiftingSurfaceAerodynamicsManager liftingSurfaceAerodynamicManager = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL);

			
			List<Double> temporaryLiftVerticalTail = new ArrayList<>();
			List<Amount<Angle>> temporaryDrList = new ArrayList<>();
			temporaryDrList.add(dr);
			
			temporaryLiftVerticalTail = MyArrayUtils.convertDoubleArrayToListDouble(
					LiftCalc.calculateCLvsAlphaArray(
							liftingSurfaceAerodynamicManager.getCLZero().get(MethodEnum.NASA_BLACKWELL),
							liftingSurfaceAerodynamicManager.getCLMax().get(MethodEnum.NASA_BLACKWELL),
							liftingSurfaceAerodynamicManager.getAlphaStar().get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS),
							liftingSurfaceAerodynamicManager.getAlphaStall().get(MethodEnum.NASA_BLACKWELL),
							liftingSurfaceAerodynamicManager.getCLAlpha().get(MethodEnum.HELMBOLD_DIEDERICH),
							MyArrayUtils.convertListOfAmountToDoubleArray(
									liftingSurfaceAerodynamicManager.getAlphaArray()
									)
							)
					).stream()
					.map(cL -> cL 
							+ (_theAerodynamicBuilderInterface.getTauRudderFunction().value(dr.doubleValue(NonSI.DEGREE_ANGLE))
									*liftingSurfaceAerodynamicManager.getCLAlpha().get(MethodEnum.HELMBOLD_DIEDERICH).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
									*dr.doubleValue(NonSI.DEGREE_ANGLE)
									)
							)
					.collect(Collectors.toList());

			aerodynamicAndStabilityManager.get_current3DVerticalTailLiftCurve().put(
					dr, 
					temporaryLiftVerticalTail);

		});

	}
		
	public static void calculateTotalLiftCoefficientFromAircraftComponents(
			ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
			) {

		IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();

		_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach( de -> 
		aerodynamicAndStabilityManager.getTotalLiftCoefficient().put(
				de,
				LiftCalc.calculateCLTotalCurveWithEquation(
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(), 
						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSurfacePlanform(),  
						aerodynamicAndStabilityManager.getCurrent3DWingLiftCurve(),
						aerodynamicAndStabilityManager.getCurrent3DHorizontalTailLiftCurve().get(de),
						_theAerodynamicBuilderInterface.getHTailDynamicPressureRatio(), 
						aerodynamicAndStabilityManager.getAlphaBodyList()
						)

				)
				);

		//----------- scale factors

		List<Amount<Angle>> alphaBodyTemporary = new ArrayList<>();
		aerodynamicAndStabilityManager.getAlphaBodyList().stream().forEach(a ->
		alphaBodyTemporary.add(a.times(_theAerodynamicBuilderInterface.getTotalLiftCalibrationAlphaScaleFactor()))
				);

		_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach( de -> 
		aerodynamicAndStabilityManager.getTotalLiftCoefficient().put(
				de,
				MyArrayUtils.convertDoubleArrayToListDouble(
						MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(alphaBodyTemporary),
						MyArrayUtils.convertToDoublePrimitive(aerodynamicAndStabilityManager.getTotalLiftCoefficient().get(de)),
						MyArrayUtils.convertListOfAmountTodoubleArray(aerodynamicAndStabilityManager.getAlphaBodyList())
						)
						)
				)
				);

		
		_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach( de -> {
			List<Double> temporaryLiftCoefficient = new ArrayList<>();
			for(int i=0; i<aerodynamicAndStabilityManager.getAlphaBodyList().size(); i++) {
				temporaryLiftCoefficient.add(
						aerodynamicAndStabilityManager.getTotalLiftCoefficient().get(de).get(i)*
						_theAerodynamicBuilderInterface.getTotalLiftCalibrationCLScaleFactor()
						);
			}
		aerodynamicAndStabilityManager.getTotalLiftCoefficient().put(
				de,
				temporaryLiftCoefficient
				);
		});



	}
	
	
	public static void calculateTotalPolarSemiempirical(
			ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
			){
		
		IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
		double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();
		Amount<Length> currentAltitude = aerodynamicAndStabilityManager.getCurrentAltitude();
		FuselageAerodynamicsManager fuselageAerodynamicManagers = aerodynamicAndStabilityManager.getFuselageAerodynamicManagers().get(ComponentEnum.FUSELAGE);
		NacelleAerodynamicsManager nacelleAerodynamicManagers = aerodynamicAndStabilityManager.getNacelleAerodynamicManagers().get(ComponentEnum.NACELLE);
		
		if(aerodynamicAndStabilityManager.getTotalLiftCoefficient().isEmpty()){
			calculateTotalLiftCoefficientFromAircraftComponents(aerodynamicAndStabilityManager);
		}
		
		if(_theAerodynamicBuilderInterface.isPerformWingAnalyses() == false) {
			//	CD0
			CalcCD0 calcCD0 = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).new CalcCD0();
			calcCD0.semiempirical(currentMachNumber, currentAltitude);

			//	CD_WAVE 
			CalcCDWave calcCDWave = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).new CalcCDWave();
			calcCDWave.lockKornWithKroo();
		}
		
		if(_theAerodynamicBuilderInterface.isPerformHTailAnalyses() == false) {
			//	CD0
			CalcCD0 calcCD0 = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).new CalcCD0();
			calcCD0.semiempirical(currentMachNumber, currentAltitude);

			//	CD_WAVE 
			CalcCDWave calcCDWave = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).new CalcCDWave();
			calcCDWave.lockKornWithKroo();
		}
		
		if(_theAerodynamicBuilderInterface.isPerformVTailAnalyses() == false) {
			//	CD0
			CalcCD0 calcCD0 = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).new CalcCD0();
			calcCD0.semiempirical(currentMachNumber, currentAltitude);

			//	CD_WAVE 
			CalcCDWave calcCDWave = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).new CalcCDWave();
			calcCDWave.lockKornWithKroo();
		}
		
		if(_theAerodynamicBuilderInterface.isPerformFuselageAnalyses() == false) {

			//	CD0_TOTAL
			analyses.fuselage.FuselageAerodynamicsManager.CalcCD0Total calcCD0Total = fuselageAerodynamicManagers.new CalcCD0Total();
			calcCD0Total.semiempirical();
		}
		
		if(_theAerodynamicBuilderInterface.isPerformNacelleAnalyses() == false) {
			
		//	CD0_TOTAL
		analyses.nacelles.NacelleAerodynamicsManager.CalcCD0Total calcCD0Total = nacelleAerodynamicManagers.new CalcCD0Total();
		calcCD0Total.semiempirical();
		}

	
		Double cD0TotalAircraft = DragCalc.calculateCD0Total(
				fuselageAerodynamicManagers.getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
				fuselageAerodynamicManagers.getCD0Parasite().get(MethodEnum.SEMIEMPIRICAL), 
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCD0().get(MethodEnum.SEMIEMPIRICAL), 
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCDParasite().get(MethodEnum.SEMIEMPIRICAL).get(0),
				nacelleAerodynamicManagers.getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
				nacelleAerodynamicManagers.getCD0Parasite().get(MethodEnum.SEMIEMPIRICAL),
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL), 
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCDParasite().get(MethodEnum.SEMIEMPIRICAL).get(0),
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCDParasite().get(MethodEnum.SEMIEMPIRICAL).get(0)
				);
		
		Double oswaldFactorTotalAircraft = AerodynamicCalc.calculateOswaldHowe(
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(),
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getMeanAirfoil().getThicknessToChordRatio(),
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN),
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getNumberOfEngineOverTheWing(),
				currentMachNumber
				);
		
		Double kDragPolarAircraft = 1/(
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio()
				*Math.PI
				*oswaldFactorTotalAircraft			
				);
		
		Double cDWave = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCDWave().get(MethodEnum.LOCK_KORN_WITH_KROO)+
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCDWave().get(MethodEnum.LOCK_KORN_WITH_KROO)+
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCDWave().get(MethodEnum.LOCK_KORN_WITH_KROO)
				;
		
		_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach(de -> {
			aerodynamicAndStabilityManager.getTotalDragCoefficient().put(
					de, 
					aerodynamicAndStabilityManager.getTotalLiftCoefficient().get(de).stream().map(cL -> 
					cD0TotalAircraft + (Math.pow(cL, 2)*kDragPolarAircraft) + cDWave)
					.collect(Collectors.toList())
					);
		});
	}
	
	//............................................................................
	// Total Moment Coefficient INNER CLASS
	//............................................................................

		public void calculateTotalMomentfromAircraftComponents(
				ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
				){
			
			IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
			double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();
			Amount<Length> currentAltitude = aerodynamicAndStabilityManager.getCurrentAltitude();
			FuselageAerodynamicsManager fuselageAerodynamicManagers = aerodynamicAndStabilityManager.getFuselageAerodynamicManagers().get(ComponentEnum.FUSELAGE);
			NacelleAerodynamicsManager nacelleAerodynamicManagers = aerodynamicAndStabilityManager.getNacelleAerodynamicManagers().get(ComponentEnum.NACELLE);
			
			_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(xcg ->{
				int i = _theAerodynamicBuilderInterface.getXCGAircraft().indexOf(xcg);
				
				// Component breakdown
				
				Map<ComponentEnum, List<Double>> _momentTemporaryMap = new HashMap<>();
				//wing
				_momentTemporaryMap.put(
						ComponentEnum.WING, 
						MomentCalc.calculateCMWingCurveWithBalanceEquation(
								Amount.valueOf((xcg*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER), SI.METER),
								Amount.valueOf((_theAerodynamicBuilderInterface.getZCGAircraft().get(i)*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))
//										+_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeZ().doubleValue(SI.METER)+
//										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER)
										,
										SI.METER),
								aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers()
								.get(ComponentEnum.WING)
								.getXacLRF()
								.get(_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.WING)
										.get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)).plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes()), 
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(), 
								aerodynamicAndStabilityManager.getCurrent3DWingLiftCurve(),
								aerodynamicAndStabilityManager.getCurrent3DWingMomentCurve(),
								aerodynamicAndStabilityManager.getAlphaWingList(),
								true)
						);
				// htail
				_momentTemporaryMap.put(
						ComponentEnum.HORIZONTAL_TAIL, 
						MomentCalc.calculateCMHTailCurveWithBalanceEquation(
								Amount.valueOf((xcg*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER), SI.METER),
								Amount.valueOf((_theAerodynamicBuilderInterface.getZCGAircraft().get(i)*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))
//										+_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeZ().doubleValue(SI.METER)+
//										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER)
										,
										SI.METER),
								aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.getXacLRF()
								.get(_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.HORIZONTAL_TAIL)
										.get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)).plus(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getXApexConstructionAxes()), 
								_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getMeanAerodynamicChord(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSurfacePlanform(), 
								MyArrayUtils.convertDoubleArrayToListDouble(aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(
										ComponentEnum.HORIZONTAL_TAIL)
								.getLiftCoefficient3DCurve()
								.get(_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D))),
								MyArrayUtils.convertDoubleArrayToListDouble(aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(
										ComponentEnum.HORIZONTAL_TAIL)
								.getMoment3DCurve()
								.get(_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE))),
								_theAerodynamicBuilderInterface.getHTailDynamicPressureRatio(), 
								aerodynamicAndStabilityManager.getAlphaHTailList(),
								_theAerodynamicBuilderInterface.isCalculateWingPendularStability()
								)
						);
		
				// fuselage
				_momentTemporaryMap.put(
						ComponentEnum.FUSELAGE, 
						MomentCalc.calculateCMFuselageCurveWithBalanceEquation(
								Amount.valueOf((xcg*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER), SI.METER),
								Amount.valueOf((_theAerodynamicBuilderInterface.getZCGAircraft().get(i)*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))
//										+_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeZ().doubleValue(SI.METER)+
//										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER)
										,
										SI.METER),
								_theAerodynamicBuilderInterface.getXCGFuselage(),
								_theAerodynamicBuilderInterface.getZCGFuselage(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(),
								MyArrayUtils.convertDoubleArrayToListDouble(fuselageAerodynamicManagers
										.getMoment3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.FUSELAGE)
												.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE))),
								MyArrayUtils.convertDoubleArrayToListDouble(fuselageAerodynamicManagers
										.getPolar3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.FUSELAGE)
												.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE))),
								aerodynamicAndStabilityManager.getAlphaBodyList(),
								_theAerodynamicBuilderInterface.isCalculateWingPendularStability()
								)
						);
				
				// nacelle
				_momentTemporaryMap.put(
						ComponentEnum.NACELLE, 
						MomentCalc.calculateCMNacelleCurveWithBalanceEquation(
								Amount.valueOf((xcg*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER), SI.METER),
								Amount.valueOf((_theAerodynamicBuilderInterface.getZCGAircraft().get(i)*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))
//										+_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeZ().doubleValue(SI.METER)+
//										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER)
										,
										SI.METER),
								_theAerodynamicBuilderInterface.getXCGNacelles(),
								_theAerodynamicBuilderInterface.getZCGNacelles(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(),
								MyArrayUtils.convertDoubleArrayToListDouble(nacelleAerodynamicManagers
										.getMoment3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.NACELLE)
												.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE))),
								MyArrayUtils.convertDoubleArrayToListDouble(nacelleAerodynamicManagers
										.getPolar3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.NACELLE)
												.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE))),
								aerodynamicAndStabilityManager.getAlphaNacelleList(),
								_theAerodynamicBuilderInterface.isCalculateWingPendularStability()
								)
						);
				
				// landing gear
				_momentTemporaryMap.put(
						ComponentEnum.LANDING_GEAR,
						MomentCalc.calculateCMLandingGearCurveWithBalanceEquation(
								Amount.valueOf((xcg*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER), SI.METER),
								Amount.valueOf((_theAerodynamicBuilderInterface.getZCGAircraft().get(i)*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))
//										+_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeZ().doubleValue(SI.METER)+
//										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER)
										,
										SI.METER),
								_theAerodynamicBuilderInterface.getXCGLandingGear(),
								_theAerodynamicBuilderInterface.getZCGLandingGear(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord(),  
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(), 
								_landingGearUsedDrag,
								aerodynamicAndStabilityManager.getAlphaBodyList(),
								_theAerodynamicBuilderInterface.isCalculateWingPendularStability()
								)
						);
				
				aerodynamicAndStabilityManager.get_totalMomentCoefficientBreakDown().put(
						xcg,
						_momentTemporaryMap
						);
				
				
				Map<Amount<Angle>, List<Double>> momentMap = new HashMap<>();
				_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach( de -> 
				momentMap.put(
						de,
						MomentCalc.calculateCMTotalCurveWithBalanceEquation(
								Amount.valueOf((xcg*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER), SI.METER),
								Amount.valueOf((_theAerodynamicBuilderInterface.getZCGAircraft().get(i)*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))
//										+_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeZ().doubleValue(SI.METER)+
//										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER)
										,
										SI.METER),
								aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers()
								.get(ComponentEnum.WING)
								.getXacLRF()
								.get(_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.WING)
										.get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)).plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes()), 
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes(), 
								aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.getXacLRF()
								.get(_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.HORIZONTAL_TAIL)
										.get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)).plus(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getXApexConstructionAxes()), 
								_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes(),
								_theAerodynamicBuilderInterface.getXCGFuselage(),
								_theAerodynamicBuilderInterface.getZCGFuselage(),
								_theAerodynamicBuilderInterface.getXCGLandingGear(),
								_theAerodynamicBuilderInterface.getZCGLandingGear(),
								_theAerodynamicBuilderInterface.getXCGNacelles(),
								_theAerodynamicBuilderInterface.getZCGNacelles(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getMeanAerodynamicChord(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSurfacePlanform(), 
								aerodynamicAndStabilityManager.getCurrent3DWingLiftCurve(),
								aerodynamicAndStabilityManager.getCurrent3DWingMomentCurve(),
								MyArrayUtils.convertDoubleArrayToListDouble(fuselageAerodynamicManagers
										.getMoment3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.FUSELAGE)
												.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE))),
								MyArrayUtils.convertDoubleArrayToListDouble(fuselageAerodynamicManagers
										.getPolar3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.FUSELAGE)
												.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE))),
								MyArrayUtils.convertDoubleArrayToListDouble(nacelleAerodynamicManagers
										.getMoment3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.NACELLE)
												.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE))),
								MyArrayUtils.convertDoubleArrayToListDouble(nacelleAerodynamicManagers
										.getPolar3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.NACELLE)
												.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE))),
								_current3DHorizontalTailLiftCurve.get(de),
								_current3DHorizontalTailMomentCurve.get(de),
								_landingGearUsedDrag,
								_theAerodynamicBuilderInterface.getHTailDynamicPressureRatio(), 
								aerodynamicAndStabilityManager.getAlphaBodyList(),
								_theAerodynamicBuilderInterface.isCalculateWingPendularStability())						
						)
						);
				aerodynamicAndStabilityManager.getTotalMomentCoefficient().put(
						xcg,
						momentMap
						);

			});
		}

}
