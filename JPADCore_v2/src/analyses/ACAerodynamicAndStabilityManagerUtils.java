package analyses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jcae.opencascade.jni.Geom_TrimmedCurve;
import org.jfree.chart.labels.CrosshairLabelGenerator;
import org.jscience.physics.amount.Amount;
import org.moeaframework.problem.misc.Lis;

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
import calculators.aerodynamics.SideForceCalc;
import calculators.stability.StabilityCalculators;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import javaslang.Tuple;
import javaslang.Tuple2;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import sun.print.resources.serviceui;


public class ACAerodynamicAndStabilityManagerUtils {
	
	static Double  cD0TotalAircraft = 0.0 ;
	static Double oswaldFactorTotalAircraft = 0.0;

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
			if(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getCurrentCondition().equals(ConditionEnum.TAKE_OFF)
					|| aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getCurrentCondition().equals(ConditionEnum.LANDING)) {

				CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = liftingSurfaceAerodynamicManager.new CalcHighLiftDevicesEffects();
				if(liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.TAKE_OFF))
					calcHighLiftDevicesEffects.semiempirical(
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getTakeOffFlapDefletctionList(), 
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getTakeOffSlatDefletctionList(),
							currentMachNumber
							);
				else if(liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.LANDING))
					calcHighLiftDevicesEffects.semiempirical(
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getLandingFlapDefletctionList(), 
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getLandingSlatDefletctionList(),
							currentMachNumber
							);
				aerodynamicAndStabilityManager.setDeltaCDZeroFlap(
						aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers()
						.get(ComponentEnum.WING)
						.getDeltaCD0()
						.get(MethodEnum.SEMIEMPIRICAL)
						);
				
				aerodynamicAndStabilityManager.setDeltaCLZeroFlap(
						aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers()
						.get(ComponentEnum.WING)
						.getDeltaCL0Flap()
						.get(MethodEnum.SEMIEMPIRICAL)
						);
			}
			
			aerodynamicAndStabilityManager.setDeltaCDZeroFlap(0.0);
		}

		//.........................................................................................................................
		//	CONTROL_SURFACE_LIFT_CURVE_3D
		if(liftingSurfaceAerodynamicManager.getTheLiftingSurface().getType().equals(ComponentEnum.WING)) {
			if(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getCurrentCondition().equals(ConditionEnum.TAKE_OFF)
					|| aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getCurrentCondition().equals(ConditionEnum.LANDING)) {

				CalcHighLiftCurve calcHighLiftCurve = liftingSurfaceAerodynamicManager.new CalcHighLiftCurve();
				if(liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.TAKE_OFF))
					calcHighLiftCurve.semiempirical(
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getTakeOffFlapDefletctionList(), 
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getTakeOffSlatDefletctionList(), 
							currentMachNumber 
							);
				else if(liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.LANDING))
					calcHighLiftCurve.semiempirical(
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getLandingFlapDefletctionList(), 
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getLandingSlatDefletctionList(), 
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
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getTakeOffFlapDefletctionList(), 
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getTakeOffSlatDefletctionList(), 
							currentMachNumber, 
							currentAltitude
							);
				else if(liftingSurfaceAerodynamicManager.getTheCondition().equals(ConditionEnum.LANDING))
					calcCLAtAlphaHighLift.semiempirical(
							alphaComponentCurrent,
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getLandingFlapDefletctionList(), 
							liftingSurfaceAerodynamicManager.getTheOperatingConditions().getLandingSlatDefletctionList(), 
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
	
	public static void calculateLandingGearDataSemiempirical(
			ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
			) {

		IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
		double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();
	
		//LANDING GEAR
		if(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getCurrentCondition().equals(ConditionEnum.TAKE_OFF) ||
				aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getCurrentCondition().equals(ConditionEnum.LANDING)) {
			
			if(aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers()
					.get(ComponentEnum.WING).getCLAtAlpha().get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCLAtAlpha calcCLAtAlpha = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers()
						.get(ComponentEnum.WING).new CalcCLAtAlpha();
				calcCLAtAlpha.nasaBlackwellCompleteCurve(aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers()
						.get(ComponentEnum.WING).getCurrentAlpha());
			}
			
			if(aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers()
					.get(ComponentEnum.WING).getDeltaCL0Flap().get(MethodEnum.SEMIEMPIRICAL) == null) {
				CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers()
						.get(ComponentEnum.WING).new CalcHighLiftDevicesEffects();
				calcHighLiftDevicesEffects.semiempirical(
						_theAerodynamicBuilderInterface.getTheOperatingConditions().getTakeOffFlapDefletctionList(), 
						_theAerodynamicBuilderInterface.getTheOperatingConditions().getTakeOffSlatDefletctionList(), 
						currentMachNumber
						);

			}
			
			double deltaDrag = 0.0;
			
			/* FIXME: CHECK INPUT OTHERWISE GET THE DELTA CALCULATED IN "CALCULATE LANDING GEAR SEMIEMPIRICAL DATA" */
			if(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().isCalculateLandingGearDeltaDragCoefficient() == Boolean.TRUE) {
				deltaDrag = DragCalc.calculateDeltaCD0LandingGears(
						aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface(), 
						aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getTheAircraft().getLandingGears(),
						aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getMeanAirfoil().getClAtCdMin(), 
						aerodynamicAndStabilityManager.get_deltaCLZeroFlap()
						);
			}
			else {
				deltaDrag = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getLandingGearDeltaDragCoefficient();
			}
			deltaDrag = deltaDrag * aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getLandingGearDragKFactor();
			
			cD0TotalAircraft = cD0TotalAircraft + deltaDrag;
			aerodynamicAndStabilityManager.setDeltaCDZeroLandingGear(deltaDrag);
		}
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
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getTakeOffFlapDefletctionList(), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getTakeOffSlatDefletctionList(), 
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
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getLandingFlapDefletctionList(), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getLandingSlatDefletctionList(), 
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

		/* FIXME: ADD CANARD TO EQUATION. FORTHEMORE, CREATE LOCAL MAP, SCALE VALUES EVETUALLY, THEN "PUTALL" INSIDE THE AC MANAGER FIELD  */
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
		
		if(_theAerodynamicBuilderInterface.isPerformCanardAnalyses() == false) {
			//	CD0
			CalcCD0 calcCD0 = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.CANARD).new CalcCD0();
			calcCD0.semiempirical(currentMachNumber, currentAltitude);

			//	CD_WAVE 
			CalcCDWave calcCDWave = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.CANARD).new CalcCDWave();
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
		};
		
		
		//OSWALD FACTOR
		double oswaldFactorTotalAircraft = AerodynamicCalc.calculateOswaldDLR(
				_theAerodynamicBuilderInterface.getTheAircraft(),
				currentMachNumber
				);
	
		//CD TOTAL
		
		/* FIXME: CHECK ON COMPONENTS. DEFINE CD0 AND CDw AS 0.0 THE FILL IN CASE THE RELATED FIELD IS FILLED IN THE MANAGER */
		
		if(_theAerodynamicBuilderInterface.getTheAircraft().getCanard() != null) {
		cD0TotalAircraft = DragCalc.calculateCD0Total(
				fuselageAerodynamicManagers.getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCD0().get(MethodEnum.SEMIEMPIRICAL), 
				nacelleAerodynamicManagers.getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL), 
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.CANARD).getCD0().get(MethodEnum.SEMIEMPIRICAL),
				aerodynamicAndStabilityManager.get_deltaCDZeroFlap(),
				aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getTheAircraft().getSWetTotal()
				);
		}
		else {
			cD0TotalAircraft = DragCalc.calculateCD0Total(
					fuselageAerodynamicManagers.getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
					aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCD0().get(MethodEnum.SEMIEMPIRICAL), 
					nacelleAerodynamicManagers.getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
					aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL), 
					aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),
					aerodynamicAndStabilityManager.get_deltaCDZeroFlap().doubleValue(),
					aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getTheAircraft().getSWetTotal()
					);
		}
		
		//WINGLET
		oswaldFactorTotalAircraft = oswaldFactorTotalAircraft * DragCalc.calculateOswaldFactorModificationDueToWinglet(
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getWingletHeight(),
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getSpan());
		
		
		double deltaDrag = 0.0;

		if(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().isCalculateExcrescencesDeltaDragCoefficient() == Boolean.TRUE) {
			deltaDrag = DragCalc.calculateCD0Excrescences(
					fuselageAerodynamicManagers.getCD0Total().get(MethodEnum.SEMIEMPIRICAL),
					aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCD0().get(MethodEnum.SEMIEMPIRICAL),
					nacelleAerodynamicManagers.getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
					aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),  
					aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),
					aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getTheAircraft().getSWetTotal().doubleValue(SI.SQUARE_METRE));
		}
		else {
			deltaDrag = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getExcrescencesDeltaDragCoefficient();
		}
		deltaDrag = deltaDrag * aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getExcrescencesDragKFactor();
		
		cD0TotalAircraft = cD0TotalAircraft + deltaDrag;
		aerodynamicAndStabilityManager.set_deltaCDZeroExcrescences(deltaDrag);


		deltaDrag = 0.0;

		if(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().isCalculateInterferencesDeltaDragCoefficient() == Boolean.TRUE) {
			deltaDrag = DragCalc.calculateDeltaCD0DueToWingFuselageInterference(
					aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getPositionRelativeToAttachment(),
					aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getAirfoilList().get(0).getThicknessToChordRatio(), 
					aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getSurfaceWetted(), 
					aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getPanels().get(0).getChordRoot()
					);
		}
		else {
			deltaDrag = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getInterferencesDeltaDragCoefficient();
		}
		deltaDrag = deltaDrag * aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getInterferencesDragKFactor();
		cD0TotalAircraft = cD0TotalAircraft + deltaDrag;
		aerodynamicAndStabilityManager.set_deltaCDZeroInterferences(deltaDrag);


		deltaDrag = 0.0;

		if(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().isCalculateCoolingDeltaDragCoefficient() == Boolean.TRUE) {
			deltaDrag = DragCalc.calculateCD0Cooling(
					fuselageAerodynamicManagers.getCD0Parasite().get(MethodEnum.SEMIEMPIRICAL), 
					aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCDParasite().get(MethodEnum.SEMIEMPIRICAL).get(0),
					nacelleAerodynamicManagers.getCD0Parasite().get(MethodEnum.SEMIEMPIRICAL),
					aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCDParasite().get(MethodEnum.SEMIEMPIRICAL).get(0),
					aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCDParasite().get(MethodEnum.SEMIEMPIRICAL).get(0)
					);
		}
		else {
			deltaDrag = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getCoolingDeltaDragCoefficient();
		}
		
		deltaDrag = deltaDrag * aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getCoolingDragKFactor();
		cD0TotalAircraft = cD0TotalAircraft + deltaDrag;
		aerodynamicAndStabilityManager.set_deltaCDZeroCooling(deltaDrag);
			
		//-----------polar
		Double kDragPolarAircraft = 1/(
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio()
				*Math.PI
				*oswaldFactorTotalAircraft			
				);
		
		/* FIXME: CHECK ON COMPONENTS. DEFINE CD0 AND CDw AS 0.0 THE FILL IN CASE THE RELATED FIELD IS FILLED IN THE MANAGER */
		Double cDWave = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCDWave().get(MethodEnum.LOCK_KORN_WITH_KROO)+
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCDWave().get(MethodEnum.LOCK_KORN_WITH_KROO)+
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCDWave().get(MethodEnum.LOCK_KORN_WITH_KROO)
				;
		
		/* TODO: ADD DELTA CD ELEVATOR DEFLECTION WHEN AVAILABLE */ 
		_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach(de -> {
			aerodynamicAndStabilityManager.getTotalDragCoefficient().put(
					de, 
					aerodynamicAndStabilityManager.getTotalLiftCoefficient().get(de).stream().map(cL -> 
					cD0TotalAircraft + (Math.pow(cL, 2)*kDragPolarAircraft) + cDWave)
					.collect(Collectors.toList())
					);
		});
		
		//----------- scale factors

		/* FIXME : SAME 'PUT' ISSUE AS FOR TOTAL LIFT */
		_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach( de -> {
			List<Double> temporaryDragCoefficient = new ArrayList<>();
			for(int i=0; i<aerodynamicAndStabilityManager.getAlphaBodyList().size(); i++) {
				temporaryDragCoefficient.add(
						aerodynamicAndStabilityManager.getTotalDragCoefficient().get(de).get(i)*
						_theAerodynamicBuilderInterface.getTotalDragCalibrationCDScaleFactor()
						);
			}
		aerodynamicAndStabilityManager.getTotalLiftCoefficient().put(
				de,
				temporaryDragCoefficient
				);
		});
		
		/* FIXME : SAME 'PUT' ISSUE AS FOR TOTAL LIFT */
		_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach( de -> {
		
		List<Double> temporaryCLCoefficient = new ArrayList<>();
		aerodynamicAndStabilityManager.getTotalLiftCoefficient().get(de).stream().forEach(cl ->
		temporaryCLCoefficient.add(cl * _theAerodynamicBuilderInterface.getTotalDragCalibrationCLScaleFactor()));
		
		aerodynamicAndStabilityManager.getTotalDragCoefficient().put(
				de,
				MyArrayUtils.convertDoubleArrayToListDouble(
						MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(temporaryCLCoefficient)),
						MyArrayUtils.convertToDoublePrimitive(aerodynamicAndStabilityManager.getTotalDragCoefficient().get(de)),
						MyArrayUtils.convertToDoublePrimitive(
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										aerodynamicAndStabilityManager.getTotalLiftCoefficient().get(de)))
						)
						)
				);
		});

	}
	
	//............................................................................
	// Total Moment Coefficient 
	//............................................................................

		public static void calculateTotalMomentfromAircraftComponents(
				ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
				){
			
			/* FIXME : ADD CANARD CONTRIBUTION */
			
			IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
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
								_theAerodynamicBuilderInterface.getLandingGearDragCoefficient(),
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
								aerodynamicAndStabilityManager.getCurrent3DHorizontalTailLiftCurve().get(de),
								aerodynamicAndStabilityManager.getCurrent3DHorizontalTailMomentCurve().get(de),
								_theAerodynamicBuilderInterface.getLandingGearDragCoefficient(),
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

		//............................................................................
		// Side force Coefficient 
		//............................................................................

			public static void calculateSideForceCoeffient(
					ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
					){
				
				IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
				
				aerodynamicAndStabilityManager.setCYBetaWing(
							SideForceCalc.calcCYBetaWing(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getDihedralMean())
							);
					
					// Assuming that x1 is the coordinate corresponding to the middle of the tail trunk fuselage
					Amount<Length> x1 =
							_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getNoseLength()
							.plus(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getCylinderLength())
							.plus(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageLength()).divide(2);
					Amount<Length> x0 = _theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageLength().times(0.378).plus(x1.times(0.527));
					
					Amount<Area> surfacePArrowV = Amount.valueOf(
							Math.pow(
									_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getEquivalentDiameterAtX(x0.doubleValue(SI.METER)),
									2
									)*Math.PI/4,
							SI.SQUARE_METRE
							);
					
					aerodynamicAndStabilityManager.setCYBetaFuselage(
							SideForceCalc.calcCYBetaFuselage(
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(),
									surfacePArrowV, 
									_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getSectionCylinderHeight(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().opposite()
									));
					
					aerodynamicAndStabilityManager.setCYBetaHorizontal(
							SideForceCalc.calcCYBetaHorizontalTail(
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSurfacePlanform(),
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getDihedralMean(),
									_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getSectionCylinderHeight(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().opposite()
									));
					
					aerodynamicAndStabilityManager.setCYBetaVertical(
							SideForceCalc.calcCYBetaVerticalTail(
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSurfacePlanform(),
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSpan(),
									 aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCLAlpha().get(MethodEnum.HELMBOLD_DIEDERICH),
									_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getSectionCylinderHeight(),
									Amount.valueOf(
											_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getZOutlineXZUpperAtX(
													_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes()
													.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getPanels().get(0).getChordRoot().divide(4)).doubleValue(SI.METER))
											- _theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getZOutlineXZLowerAtX(
													_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes()
													.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getPanels().get(0).getChordRoot().divide(4)).doubleValue(SI.METER)),
											SI.METER
											),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().opposite(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAeroDatabaseReader()
									));
					
					aerodynamicAndStabilityManager.setCYBetaTotal(
							SideForceCalc.calcCYBetaTotal(
									aerodynamicAndStabilityManager.getCYBetaWing(),
									aerodynamicAndStabilityManager.getCYBetaFuselage(),
									aerodynamicAndStabilityManager.getCYBetaHorizontal(),
									aerodynamicAndStabilityManager.getCYBetaVertical()
									));
					
					// Assuming that CY_delta_A = 0
					
					aerodynamicAndStabilityManager.setCYDeltaR(
							SideForceCalc.calcCYDeltaR(
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(),
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSurfacePlanform(),
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getEquivalentWing().getPanels().get(0).getTaperRatio(),
									 aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCLAlpha().get(MethodEnum.HELMBOLD_DIEDERICH),
									_theAerodynamicBuilderInterface.getVTailDynamicPressureRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSymmetricFlaps().get(0).getTheSymmetricFlapInterface().getInnerStationSpanwisePosition(),
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSymmetricFlaps().get(0).getTheSymmetricFlapInterface().getOuterStationSpanwisePosition(),
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSymmetricFlaps().get(0).getMeanChordRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAeroDatabaseReader()
									));
					
					List<Tuple2<Double, Amount<?>>> listOfCYp = new ArrayList();
					
					for (int i = 0; i < _theAerodynamicBuilderInterface.getXCGAircraft().size(); i++) {

						Amount<?> temporaryCYp = SideForceCalc.calcCYp(
								aerodynamicAndStabilityManager.getCYBetaVertical(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes()
								.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeX())
								.plus((_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChord()).times(0.25))
								.minus(
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().times(_theAerodynamicBuilderInterface.getXCGAircraft().get(i))
										.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX())
										.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes())
										),
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getZApexConstructionAxes()
								.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeZ()),
								_theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCruise()
								);

						listOfCYp.add(
								Tuple.of(
										_theAerodynamicBuilderInterface.getXCGAircraft().get(i),
										temporaryCYp
										)
								);
						
						aerodynamicAndStabilityManager.getCYp().put(
								_theAerodynamicBuilderInterface.getXCGAircraft().get(i),
								temporaryCYp
								);
					}
					
					
					List<Tuple2<Double, Amount<?>>> listOfCYr = new ArrayList();
					
					for (int i = 0; i < _theAerodynamicBuilderInterface.getXCGAircraft().size(); i++) {

						Amount<?> temporaryCYr = SideForceCalc.calcCYr(
								aerodynamicAndStabilityManager.getCYBetaVertical(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes()
								.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeX())
								.plus((_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChord()).times(0.25))
								.minus(
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().times(_theAerodynamicBuilderInterface.getXCGAircraft().get(i))
										.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX())
										.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes())
										),
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getZApexConstructionAxes()
								.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeZ()),
								_theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCruise()
								);

						listOfCYr.add(
								Tuple.of(
										_theAerodynamicBuilderInterface.getXCGAircraft().get(i),
										temporaryCYr
										)
								);
						
						aerodynamicAndStabilityManager.getCYr().put(
								_theAerodynamicBuilderInterface.getXCGAircraft().get(i),
								temporaryCYr
								);
						
					}
					
			}
				
			public static void calculateLongitudinalStaticStability(
					ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
					){
				
				/* FIXME : ADD CANARD CONTRIBUTION */
				
				IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
				double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();
				
				//=======================================================================================
				// Calculating horizontal tail equilibrium lift coefficient ... CLh_e
				//=======================================================================================

				_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(xcg -> {

					int i = _theAerodynamicBuilderInterface.getXCGAircraft().indexOf(xcg);
		
					aerodynamicAndStabilityManager.getHorizontalTailEquilibriumLiftCoefficient().put(
							xcg,
							LiftCalc.calculateHorizontalTailEquilibriumLiftCoefficient(
									Amount.valueOf((xcg*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))+
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)+
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER), SI.METER), 
									Amount.valueOf((_theAerodynamicBuilderInterface.getZCGAircraft().get(i)*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))
//											+_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeZ().doubleValue(SI.METER)+
//											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER)
											,
											SI.METER),
									aerodynamicAndStabilityManager
									.getLiftingSurfaceAerodynamicManagers()
									.get(ComponentEnum.WING)
									.getXacLRF()
									.get(_theAerodynamicBuilderInterface.getComponentTaskList()
											.get(ComponentEnum.WING)
											.get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)).plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes()),  
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes(),  
									aerodynamicAndStabilityManager
									.getLiftingSurfaceAerodynamicManagers()
									.get(ComponentEnum.HORIZONTAL_TAIL)
									.getXacLRF()
									.get(_theAerodynamicBuilderInterface.getComponentTaskList()
											.get(ComponentEnum.HORIZONTAL_TAIL)
											.get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)).plus(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getXApexConstructionAxes()), 
									_theAerodynamicBuilderInterface.getXCGFuselage(),
									_theAerodynamicBuilderInterface.getZCGFuselage(),
									_theAerodynamicBuilderInterface.getXCGLandingGear(),
									_theAerodynamicBuilderInterface.getZCGLandingGear(),
									_theAerodynamicBuilderInterface.getXCGNacelles(),
									_theAerodynamicBuilderInterface.getZCGNacelles(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord(), 
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(),
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSurfacePlanform(), 
									aerodynamicAndStabilityManager.getCurrent3DWingLiftCurve(),
									aerodynamicAndStabilityManager.getCurrent3DWingMomentCurve(),
									MyArrayUtils.convertDoubleArrayToListDouble(aerodynamicAndStabilityManager.getFuselageAerodynamicManagers()
											.get(ComponentEnum.FUSELAGE)
											.getMoment3DCurve()
											.get(_theAerodynamicBuilderInterface.getComponentTaskList()
													.get(ComponentEnum.FUSELAGE)
													.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE))),
									MyArrayUtils.convertDoubleArrayToListDouble(aerodynamicAndStabilityManager.getFuselageAerodynamicManagers()
											.get(ComponentEnum.FUSELAGE)
											.getPolar3DCurve()
											.get(_theAerodynamicBuilderInterface.getComponentTaskList()
													.get(ComponentEnum.FUSELAGE)
													.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE))),
									MyArrayUtils.convertDoubleArrayToListDouble(aerodynamicAndStabilityManager.getNacelleAerodynamicManagers()
											.get(ComponentEnum.NACELLE)
											.getMoment3DCurve()
											.get(_theAerodynamicBuilderInterface.getComponentTaskList()
													.get(ComponentEnum.NACELLE)
													.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE))),
									MyArrayUtils.convertDoubleArrayToListDouble(aerodynamicAndStabilityManager.getNacelleAerodynamicManagers()
											.get(ComponentEnum.NACELLE)
											.getPolar3DCurve()
											.get(_theAerodynamicBuilderInterface.getComponentTaskList()
													.get(ComponentEnum.NACELLE)
													.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE))),
									_theAerodynamicBuilderInterface.getLandingGearDragCoefficient(),
									_theAerodynamicBuilderInterface.getHTailDynamicPressureRatio(),
									aerodynamicAndStabilityManager.getAlphaBodyList(),
									aerodynamicAndStabilityManager.getAlphaWingList(),
									aerodynamicAndStabilityManager.getAlphaNacelleList(),
									_theAerodynamicBuilderInterface.isCalculateWingPendularStability()
									));
							
				});

				//=======================================================================================
				// Calculating total equilibrium lift coefficient ... CLtot_e
				//=======================================================================================

				
				_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(xcg -> {
				aerodynamicAndStabilityManager.getTotalEquilibriumLiftCoefficient().put(
						xcg,
						LiftCalc.calculateCLTotalCurveWithEquation(
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSurfacePlanform(),
								aerodynamicAndStabilityManager.getCurrent3DWingLiftCurve(),
								aerodynamicAndStabilityManager.getHorizontalTailEquilibriumLiftCoefficient().get(xcg),
								_theAerodynamicBuilderInterface.getHTailDynamicPressureRatio(), 
								aerodynamicAndStabilityManager.getAlphaBodyList()));		

				});
				
				//=======================================================================================
				// Calculating delta e equilibrium ... deltae_e
				//=======================================================================================

				Map<Amount<Angle>, List<Double>> liftCoefficientHorizontalTailForEquilibrium = new HashMap<>();
				
					aerodynamicAndStabilityManager.getDeltaEForEquilibrium().stream().forEach(de -> {
					List<Double> temporaryLiftHorizontalTail = new ArrayList<>();
					List<Amount<Angle>> temporaryDeList = new ArrayList<>();
					temporaryDeList.add(de);

					if(aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getAlphaZeroLift().get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
						CalcAlpha0L calcAlphaZeroLift = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).new CalcAlpha0L();
						calcAlphaZeroLift.integralMeanWithTwist();
					}
					
					if(aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCLZero().get(MethodEnum.NASA_BLACKWELL) == null) {
						CalcCL0 calcCLZero = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).new CalcCL0();
						calcCLZero.nasaBlackwell();
					}
					
					if( aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getAlphaStar().get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS) == null) {
						CalcAlphaStar calcAlphaStar =  aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).new CalcAlphaStar();
						calcAlphaStar.meanAirfoilWithInfluenceAreas();
					}
					
					if( aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCLStar().get(MethodEnum.NASA_BLACKWELL) == null) {
						CalcCLStar calcCLStar =  aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).new CalcCLStar();
						calcCLStar.nasaBlackwell();
					}
					
					if( aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCLMax().get(MethodEnum.NASA_BLACKWELL) == null) {
						CalcCLmax calcCLMax =  aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).new CalcCLmax();
						calcCLMax.nasaBlackwell();
					}
					
					if(! aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getTheLiftingSurface().getType().equals(ComponentEnum.VERTICAL_TAIL)) {
						if( aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCLAlpha().get(MethodEnum.NASA_BLACKWELL) == null) {
							CalcCLAlpha calcCLAlpha =  aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).new CalcCLAlpha();
							calcCLAlpha.nasaBlackwell();
						}
					}
					else
						if( aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCLAlpha().get(MethodEnum.HELMBOLD_DIEDERICH) == null) {
							CalcCLAlpha calcCLAlpha =  aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).new CalcCLAlpha();
							calcCLAlpha.helmboldDiederich(currentMachNumber);
						}
					
					temporaryLiftHorizontalTail = MyArrayUtils.convertDoubleArrayToListDouble(
							LiftCalc.calculateCLvsAlphaArray(
									aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCLZero().get(MethodEnum.NASA_BLACKWELL),
									aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCLMax().get(MethodEnum.NASA_BLACKWELL),
									aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getAlphaStar().get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS),
									aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getAlphaStall().get(MethodEnum.NASA_BLACKWELL),
									aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCLAlpha().get(MethodEnum.NASA_BLACKWELL),
									MyArrayUtils.convertListOfAmountToDoubleArray(
											aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArray()
											)
									)
							).stream()
							.map(cL -> cL 
									+ (_theAerodynamicBuilderInterface.getTauElevatorFunction().value(de.doubleValue(NonSI.DEGREE_ANGLE))
											*aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCLAlpha().get(MethodEnum.NASA_BLACKWELL).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
											*de.doubleValue(NonSI.DEGREE_ANGLE)
											)
									)
							.collect(Collectors.toList());

					liftCoefficientHorizontalTailForEquilibrium.put(
							de, 
							temporaryLiftHorizontalTail);
					

				});
				
				_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(xcg -> {
				aerodynamicAndStabilityManager.getDeltaEEquilibrium().put(xcg, 
						AerodynamicCalc.calculateDeltaEEquilibrium(
						liftCoefficientHorizontalTailForEquilibrium, 
						aerodynamicAndStabilityManager.getDeltaEForEquilibrium(),
						aerodynamicAndStabilityManager.getHorizontalTailEquilibriumLiftCoefficient().get(xcg),
						aerodynamicAndStabilityManager.getAlphaBodyList()
						));
				});
				
				//=======================================================================================
				// Calculating total equilibrium Drag coefficient ... CDtot_e
				//=======================================================================================

				Amount<Length> currentAltitude = aerodynamicAndStabilityManager.getCurrentAltitude();
				FuselageAerodynamicsManager fuselageAerodynamicManagers = aerodynamicAndStabilityManager.getFuselageAerodynamicManagers().get(ComponentEnum.FUSELAGE);
				NacelleAerodynamicsManager nacelleAerodynamicManagers = aerodynamicAndStabilityManager.getNacelleAerodynamicManagers().get(ComponentEnum.NACELLE);
				
				/* FIXME: CHECK ON DRAG POLAR. ELSE FOLLOW ALL FIXME IN DRAG POLAR */
				
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
				
				if(_theAerodynamicBuilderInterface.isPerformCanardAnalyses() == false) {
					//	CD0
					CalcCD0 calcCD0 = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.CANARD).new CalcCD0();
					calcCD0.semiempirical(currentMachNumber, currentAltitude);

					//	CD_WAVE 
					CalcCDWave calcCDWave = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.CANARD).new CalcCDWave();
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
				};
				
				
				//OSWALD FACTOR
				oswaldFactorTotalAircraft =  AerodynamicCalc.calculateOswaldDLR(
						_theAerodynamicBuilderInterface.getTheAircraft(),
						currentMachNumber
						);
							
				//WINGLET
				oswaldFactorTotalAircraft = oswaldFactorTotalAircraft * DragCalc.calculateOswaldFactorModificationDueToWinglet(
						aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getWingletHeight(),
						aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getSpan());
				
				
				//EXCRESCENCES 
				
					double deltaDrag = 0.0;
					
					if(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().isCalculateExcrescencesDeltaDragCoefficient() == Boolean.TRUE) {
						deltaDrag = DragCalc.calculateCD0Excrescences(
								fuselageAerodynamicManagers.getCD0Total().get(MethodEnum.SEMIEMPIRICAL),
								aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCD0().get(MethodEnum.SEMIEMPIRICAL),
								nacelleAerodynamicManagers.getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
								aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),  
								aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),
								aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getTheAircraft().getSWetTotal().doubleValue(SI.SQUARE_METRE));
					}
					else {
						deltaDrag = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getExcrescencesDeltaDragCoefficient();
					}
					aerodynamicAndStabilityManager.set_deltaCDZeroExcrescences(deltaDrag);
			

				//INTERFERENCES
				
					deltaDrag = 0.0;
					
					if(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().isCalculateInterferencesDeltaDragCoefficient() == Boolean.TRUE) {
						deltaDrag = DragCalc.calculateDeltaCD0DueToWingFuselageInterference(
								aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getPositionRelativeToAttachment(),
								aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getAirfoilList().get(0).getThicknessToChordRatio(), 
								aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getSurfaceWetted(), 
								aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getPanels().get(0).getChordRoot()
								);
					}
					else {
						deltaDrag = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getInterferencesDeltaDragCoefficient();
					}
					aerodynamicAndStabilityManager.set_deltaCDZeroInterferences(deltaDrag);
					
					
				//COOLING
				
					deltaDrag = 0.0;
					
					if(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().isCalculateCoolingDeltaDragCoefficient() == Boolean.TRUE) {
						deltaDrag = DragCalc.calculateCD0Cooling(
								fuselageAerodynamicManagers.getCD0Parasite().get(MethodEnum.SEMIEMPIRICAL), 
								aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCDParasite().get(MethodEnum.SEMIEMPIRICAL).get(0),
								nacelleAerodynamicManagers.getCD0Parasite().get(MethodEnum.SEMIEMPIRICAL),
								aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCDParasite().get(MethodEnum.SEMIEMPIRICAL).get(0),
								aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCDParasite().get(MethodEnum.SEMIEMPIRICAL).get(0)
								);
					}
					else {
						deltaDrag = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getCoolingDeltaDragCoefficient();
					}
					aerodynamicAndStabilityManager.set_deltaCDZeroCooling(deltaDrag);

				
				//CD WAWE
				Double cD0Wawe = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCDWave().get(MethodEnum.LOCK_KORN_WITH_KROO) + 
						aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCDWave().get(MethodEnum.LOCK_KORN_WITH_KROO) + 
						aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCDWave().get(MethodEnum.LOCK_KORN_WITH_KROO);

				
				if(_theAerodynamicBuilderInterface.getTheAircraft().getCanard() != null) {
				_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(xcg -> {	
							aerodynamicAndStabilityManager.getTotalEquilibriumDragCoefficient().put(
					xcg, 
					DragCalc.calculateTrimmedPolarV2(
							aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),
							aerodynamicAndStabilityManager.getFuselageAerodynamicManagers().get(ComponentEnum.FUSELAGE).getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
							aerodynamicAndStabilityManager.get_deltaCDZeroInterferences(), //cD0WingFuselageInterference,  //FIX
							aerodynamicAndStabilityManager.getNacelleAerodynamicManagers().get(ComponentEnum.NACELLE).getCD0Total().get(MethodEnum.SEMIEMPIRICAL),
							aerodynamicAndStabilityManager.get_deltaCDZeroCooling(),
							aerodynamicAndStabilityManager.get_deltaCDZeroExcrescences(),
							cD0Wawe, 
							aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.CANARD).getCD0().get(MethodEnum.SEMIEMPIRICAL),
							aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCD0().get(MethodEnum.SEMIEMPIRICAL),
							aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),
							aerodynamicAndStabilityManager.get_deltaCDZeroFlap(),
							aerodynamicAndStabilityManager.get_deltaCDZeroLandingGear(),
							aerodynamicAndStabilityManager.getTotalEquilibriumLiftCoefficient().get(xcg), 
							aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getAspectRatio(), 
							oswaldFactorTotalAircraft)
					);
				});
				}
				else {
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(xcg -> {	
						aerodynamicAndStabilityManager.getTotalEquilibriumDragCoefficient().put(
				xcg, 
				DragCalc.calculateTrimmedPolarV2(
						aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),
						aerodynamicAndStabilityManager.getFuselageAerodynamicManagers().get(ComponentEnum.FUSELAGE).getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
						aerodynamicAndStabilityManager.get_deltaCDZeroInterferences(), //cD0WingFuselageInterference,  //FIX
						aerodynamicAndStabilityManager.getNacelleAerodynamicManagers().get(ComponentEnum.NACELLE).getCD0Total().get(MethodEnum.SEMIEMPIRICAL),
						aerodynamicAndStabilityManager.get_deltaCDZeroCooling(),
						aerodynamicAndStabilityManager.get_deltaCDZeroExcrescences(),
						cD0Wawe, 
						0.0,
						aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCD0().get(MethodEnum.SEMIEMPIRICAL),
						aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),
						aerodynamicAndStabilityManager.get_deltaCDZeroFlap(),
						aerodynamicAndStabilityManager.get_deltaCDZeroLandingGear(),
						aerodynamicAndStabilityManager.getTotalEquilibriumLiftCoefficient().get(xcg), 
						aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getAspectRatio(), 
						oswaldFactorTotalAircraft)
				);
			});
				}

				//=======================================================================================
				// Calculating trimmed efficiency curves vs alpha body ... 
				//=======================================================================================

				_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(xcg -> {	
					
					aerodynamicAndStabilityManager.getTotalEquilibriumEfficiencyMap().put(
							xcg, 
							AerodynamicCalc.calculateTrimmedEfficiencyCurve(
									aerodynamicAndStabilityManager.getTotalEquilibriumLiftCoefficient().get(xcg),
									aerodynamicAndStabilityManager.getTotalDragCoefficient().get(xcg)
									)
							);
					
					aerodynamicAndStabilityManager.getTotalEquilibriumMaximumEfficiencyMap().put(
							xcg, 
							MyArrayUtils.getMax(
									MyArrayUtils.convertListOfDoubleToDoubleArray(
											aerodynamicAndStabilityManager.getTotalEquilibriumEfficiencyMap().get(xcg)
											)
									)
							);
					
				});
		
				//=======================================================================================
				// Calculating MSS position vs alpha body ...
				//=======================================================================================

				int alphaBodyZero = aerodynamicAndStabilityManager.getAlphaBodyList().indexOf(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
				
				_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(xcg -> {	

					aerodynamicAndStabilityManager.getStaticStabilityMarginMap().put(
							xcg,
							MyArrayUtils.convertDoubleArrayToListDouble(
									MyArrayUtils.convertFromDoubleToPrimitive(
											MyMathUtils.calculateArrayFirstDerivative(
													MyArrayUtils.convertToDoublePrimitive(aerodynamicAndStabilityManager.getTotalEquilibriumLiftCoefficient().get(xcg)), 
													MyArrayUtils.convertToDoublePrimitive(
															aerodynamicAndStabilityManager.getTotalMomentCoefficient().get(xcg).get(
																	Amount.valueOf(
																			0.0,
																			NonSI.DEGREE_ANGLE
																			)
																	)
															)
													)
											)
									).get(alphaBodyZero)
							);
				});
				
				//=======================================================================================
				// Calculating neutral point position vs alpha body ... 
				//=======================================================================================

				_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(xcg -> {	

					aerodynamicAndStabilityManager.getNeutralPointPositionMap().put(
							xcg,
							xcg - aerodynamicAndStabilityManager.getStaticStabilityMarginMap().get(xcg)
					);
					
				});
				
			}

			public static void calculateLateralStability(
					ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
					){
				IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
				double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();
				Amount<Length> currentAltitude = aerodynamicAndStabilityManager.getCurrentAltitude();
				
				if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.SIDE_FORCE)) {
					calculateSideForceCoeffient(aerodynamicAndStabilityManager);
				}
				
					//=======================================================================================
					// Calculating stability derivatives for each component ...
					//=======================================================================================
				
				aerodynamicAndStabilityManager.setCRollBetaWingBody(MomentCalc.calcCRollBetaWingBody(
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(), 
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(), 
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(), 
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getDihedralMean(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTwistAerodynamicAtTip(), 
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes()
						.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXLEBreakPoints().get(
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXLEBreakPoints().size()-1
								))
						.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getChordTip().divide(2)),
						Amount.valueOf(
								_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getEquivalentDiameterAtX(
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)
										+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getPanels().get(0).getChordRoot().divide(2).doubleValue(SI.METER)),
								SI.METER
								),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().opposite(), 
						LiftCalc.calculateLiftCoeff((
										_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)
										+ _theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().doubleValue(SI.KILOGRAM)
										)/2*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
								currentMachNumber*AtmosphereCalc.getSpeedOfSound(currentAltitude.doubleValue(SI.METER)),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
								currentAltitude.doubleValue(SI.METER)
								),
						currentMachNumber,
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAeroDatabaseReader() 
						));
					
				aerodynamicAndStabilityManager.setCRollBetaHorizontal(MomentCalc.calcCRollBetaHorizontalTail(
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSurfacePlanform(),
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSpan(),
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getAspectRatio(), 
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getEquivalentWing().getPanels().get(0).getTaperRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getEquivalentWing().getPanels().get(0).getSweepHalfChord(), 
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getDihedralMean(),
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getEquivalentWing().getPanels().get(0).getTwistAerodynamicAtTip(), 
									_theAerodynamicBuilderInterface.getHTailDynamicPressureRatio(),
									Amount.valueOf(
											_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getEquivalentDiameterAtX(
													_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER)
													+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getPanels().get(0).getChordRoot().divide(2).doubleValue(SI.METER)),
											SI.METER
											),
									currentMachNumber,
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAeroDatabaseReader()
									));
				
					Map<Double, Amount<?>> listOfCRollBetaVerticalTail = new HashMap<>();
					
					for (int i = 0; i < _theAerodynamicBuilderInterface.getXCGAircraft().size(); i++) {

						Amount<?> temporaryCRollBetaVerticalTail = MomentCalc.calcCRollBetaVerticalTail(
								aerodynamicAndStabilityManager.getCYBetaVertical(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes()
								.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeX())
								.plus((_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChord()).times(0.25))
								.minus(
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().times(_theAerodynamicBuilderInterface.getXCGAircraft().get(i))
										.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX())
										.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes())
										),
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getZApexConstructionAxes()
								.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeZ()),
								_theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCruise()
								);

						listOfCRollBetaVerticalTail.put(_theAerodynamicBuilderInterface.getXCGAircraft().get(i), temporaryCRollBetaVerticalTail);
	
					}
					
					aerodynamicAndStabilityManager.setCRollBetaVertical(listOfCRollBetaVerticalTail);

					
					Map<Double, Amount<?>> listOfCRollBetaTotal = new HashMap<>();
					
					for (int i = 0; i < _theAerodynamicBuilderInterface.getXCGAircraft().size(); i++) {

						Amount<?> temporaryCRollBetaTotal = MomentCalc.calcCRollBetaTotal(
								aerodynamicAndStabilityManager.get_cRollBetaWingBody(),
								aerodynamicAndStabilityManager.getCRollBetaHorizontal(),
								aerodynamicAndStabilityManager.getCRollBetaVertical().get(i)                                                           
								);

						listOfCRollBetaTotal.put(
										_theAerodynamicBuilderInterface.getXCGAircraft().get(i),
										temporaryCRollBetaTotal
										);
					}
					
					aerodynamicAndStabilityManager.setCRollBetaTotal(listOfCRollBetaTotal);

					//=======================================================================================
					// Calculating control derivatives ...
					//=======================================================================================
					
					aerodynamicAndStabilityManager.setCRollDeltaA(							MomentCalc.calcCRollDeltaA(
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
									aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCLAlpha().get(MethodEnum.NASA_BLACKWELL),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAsymmetricFlaps().get(1).getTheAsymmetricFlapInterface().getInnerStationSpanwisePosition(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAsymmetricFlaps().get(1).getTheAsymmetricFlapInterface().getOuterStationSpanwisePosition(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAsymmetricFlaps().get(1).getMeanChordRatio(),
									currentMachNumber,
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAeroDatabaseReader()
									));
					
					double etaInR  = _theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSymmetricFlaps().get(0).getTheSymmetricFlapInterface().getInnerStationSpanwisePosition();
					double etaOutR = _theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSymmetricFlaps().get(0).getTheSymmetricFlapInterface().getOuterStationSpanwisePosition();
					Amount<Length> zApplicationForceRudder = _theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSpan().times((etaOutR - etaInR)/2);

					Map<Double, Amount<?>> listOfCRollDeltaR = new HashMap<>();
					
					for (int i = 0; i < _theAerodynamicBuilderInterface.getXCGAircraft().size(); i++) {

						Amount<?> temporaryCRollDeltaR = MomentCalc.calcCRollDeltaR(
								aerodynamicAndStabilityManager.getCYDeltaR(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes()
								.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXLEAtYActual(zApplicationForceRudder.doubleValue(SI.METER)))
								.plus(
										Amount.valueOf(
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getChordAtYActual(zApplicationForceRudder.doubleValue(SI.METER))*
												(1 - 0.75*_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSymmetricFlaps().get(0).getMeanChordRatio()),
												SI.METER
												)
										)
								.minus(
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().times(_theAerodynamicBuilderInterface.getXCGAircraft().get(i))
										.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX())
										.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes())
										),
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getZApexConstructionAxes().plus(zApplicationForceRudder),
								_theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCruise()
								);

						listOfCRollDeltaR.put(
										_theAerodynamicBuilderInterface.getXCGAircraft().get(i),
										temporaryCRollDeltaR
								);
					}
					
					aerodynamicAndStabilityManager.setCRollDeltaR(listOfCRollDeltaR);


					//=======================================================================================
					// Calculating dynamic derivatives ...
					//=======================================================================================
					aerodynamicAndStabilityManager.setCRollpWingBody(MomentCalc.calcCRollpWingBody(
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
									aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCLAlpha().get(MethodEnum.NASA_BLACKWELL),
									currentMachNumber,
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAeroDatabaseReader()
									));
					
					aerodynamicAndStabilityManager.setCRollpHorizontal(
							MomentCalc.calcCRollpHorizontalTail(
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSurfacePlanform(),
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSpan(),
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getAspectRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getEquivalentWing().getPanels().get(0).getTaperRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
									aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCLAlpha().get(MethodEnum.NASA_BLACKWELL),
									currentMachNumber,
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAeroDatabaseReader()
									));
					
					aerodynamicAndStabilityManager.setCRollpVertical(
							MomentCalc.calcCRollpVerticalTail(
									aerodynamicAndStabilityManager.getCYBetaVertical(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getZApexConstructionAxes()
									.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeZ())
									));
					
					aerodynamicAndStabilityManager.set_cRollpTotal(
							MomentCalc.calcCRollpTotal(
									aerodynamicAndStabilityManager.get_cRollpWingBody(),
									aerodynamicAndStabilityManager.getCRollpHorizontal(),
									aerodynamicAndStabilityManager.getCRollpVertical()
									));
					
					aerodynamicAndStabilityManager.setCRollrWing(
							MomentCalc.calcCRollrWing(
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getDihedralMean(),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTwistAerodynamicAtTip(), 
									aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCLAlpha().get(MethodEnum.NASA_BLACKWELL),
									LiftCalc.calculateLiftCoeff(
											(
													_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)
													+ _theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().doubleValue(SI.KILOGRAM)
													)/2*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
											currentMachNumber*AtmosphereCalc.getSpeedOfSound(currentAltitude.doubleValue(SI.METER)),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
											currentAltitude.doubleValue(SI.METER)
											),
									currentMachNumber,
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAeroDatabaseReader()
									));

					Map<Double, Amount<?>> listOfCRollrVerticalTail = new HashMap<>();
					
					for (int i = 0; i < _theAerodynamicBuilderInterface.getXCGAircraft().size(); i++) {

						Amount<?> temporaryCRollrVerticalTail = MomentCalc.calcCRollrVerticalTail(
								aerodynamicAndStabilityManager.getCYBetaVertical(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes()
								.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeX())
								.plus((_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChord()).times(0.25))
								.minus(
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().times(_theAerodynamicBuilderInterface.getXCGAircraft().get(i))
										.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX())
										.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes())
										),
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getZApexConstructionAxes()
								.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeZ()),
								_theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCruise()
								);

						listOfCRollrVerticalTail.put(
										_theAerodynamicBuilderInterface.getXCGAircraft().get(i),
										temporaryCRollrVerticalTail
										);
					}
					
					aerodynamicAndStabilityManager.setCRollrVertical(
							listOfCRollrVerticalTail
							);
					
					Map<Double, Amount<?>> listOfCRollrTotal = new HashMap<>();
					
					for (int i = 0; i < _theAerodynamicBuilderInterface.getXCGAircraft().size(); i++) {

						Amount<?> temporaryCRollrTotal = MomentCalc.calcCRollrTotal(
								aerodynamicAndStabilityManager.getCRollrWing(),
								aerodynamicAndStabilityManager.getCRollrVertical().get(i)
								);

						listOfCRollrTotal.put(
										_theAerodynamicBuilderInterface.getXCGAircraft().get(i),
										temporaryCRollrTotal
										);
					}
					
					aerodynamicAndStabilityManager.setCRollrTotal(
							listOfCRollrTotal
							);
					
			}
			
				public static void calculateDirectionalStability(
						ACAerodynamicAndStabilityManager_v2 aerodynamicAndStabilityManager
						){
					IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface();
					double currentMachNumber = aerodynamicAndStabilityManager.getCurrentMachNumber();
					Amount<Length> currentAltitude = aerodynamicAndStabilityManager.getCurrentAltitude();
					
					if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.SIDE_FORCE)) {
						calculateSideForceCoeffient(aerodynamicAndStabilityManager);
					}
					
				List<Amount<Angle>> eltaRudderForEquilibrium = MyArrayUtils.convertDoubleArrayToListOfAmount((MyArrayUtils.linspaceDouble(
						5,
						25, 
						5
						)),
						NonSI.DEGREE_ANGLE);
				
				//=======================================================================================
				// Calculating stability derivatives for each component ...
				//=======================================================================================
				aerodynamicAndStabilityManager.setCNBetaFuselage(
						_theAerodynamicBuilderInterface.getXCGAircraft().stream()
							.map(
								x -> Tuple.of(
										x,
										MomentCalc.calcCNBetaFuselageVEDSC(
												_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFusDesDatabaseReader(), 
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getVeDSCDatabaseReader(),
												_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageFinenessRatio(), 
												_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getNoseFinenessRatio(), 
												_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getTailFinenessRatio(), 
												((x*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))
														+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
														+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER))
												/_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METER),
												_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getEquivalentDiameterGM(),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSpan(),
												Amount.valueOf(
														_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getEquivalentDiameterAtX(
																aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL)
																.getXacLRF().get(
																		_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																		).plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes())
																.doubleValue(SI.METER)
																),
														SI.METER
														), 
												_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getTailTipOffset().doubleValue(SI.METER)
												/(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getSectionCylinderHeight().doubleValue(SI.METER)/2),
												_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getPositionRelativeToAttachment(),
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAspectRatio(),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getPositionRelativeToAttachment()
												)
										)
								)
							.collect(Collectors.toList())
						);



				aerodynamicAndStabilityManager.setCNBetaWing(
						_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
								x -> Tuple.of(
										x,
										MomentCalc.calcCNBetaWingVEDSC(_theAerodynamicBuilderInterface.getTheAircraft().getWing()
												.getEquivalentWing().getPanels().get(0).getSweepQuarterChord())
										)
								)
						.collect(Collectors.toList())
						);


				aerodynamicAndStabilityManager.setCNBetaVertical(
						_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
								x -> Tuple.of(
										x,
										MomentCalc.calcCNbetaVerticalTailVEDSC(
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(), 
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAspectRatio(), 
												Math.abs(
														(aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL)
																.getXacLRF().get(
																		_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																		).plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes())
																.doubleValue(SI.METER))
														- ((x*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))
																+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
																+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER))
														),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
												aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getMeanAirfoil().getClAlphaLinearTrait().to(SI.RADIAN.inverse()).getEstimatedValue(),
												currentMachNumber, 
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getVeDSCDatabaseReader().get_KFv_vs_bv_over_dfv(
														_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSpan().doubleValue(SI.METER), 
														_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getEquivalentDiameterAtX(
																aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL)
																.getXacLRF().get(
																		_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																		).plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes())
																.doubleValue(SI.METER)
																), 
														_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getTailTipOffset().doubleValue(SI.METER)
														/(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getSectionCylinderHeight().doubleValue(SI.METER)/2)
														),
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getVeDSCDatabaseReader().get_KWv_vs_zw_over_rf(
														_theAerodynamicBuilderInterface.getTheAircraft().getWing().getPositionRelativeToAttachment(),
														_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
														_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getTailTipOffset().doubleValue(SI.METER)
														/(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getSectionCylinderHeight().doubleValue(SI.METER)/2)),
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getVeDSCDatabaseReader().get_KHv_vs_zh_over_bv1(
														_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getPositionRelativeToAttachment(),
														_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAspectRatio(),
														_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getTailTipOffset().doubleValue(SI.METER)
														/(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getSectionCylinderHeight().doubleValue(SI.METER)/2), 
														_theAerodynamicBuilderInterface.getTheAircraft().getWing().getPositionRelativeToAttachment())

												)
										)
								).collect(Collectors.toList())
						);


				aerodynamicAndStabilityManager.setCNBetaTotal(
						_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
								x -> Tuple.of(
										x,
										aerodynamicAndStabilityManager.getCNBetaVertical()
										.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
										._2()
										+aerodynamicAndStabilityManager.getCNBetaWing()
										.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
										._2()
										+aerodynamicAndStabilityManager.getCNBetaFuselage()
										.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
										._2()
										)
								).collect(Collectors.toList())
						);

				//=======================================================================================
				// Calculating control derivatives ...
				//=======================================================================================
				Map<Amount<Angle>, List<Tuple2<Double, Double>>> cNdrMap = new HashMap<>();

				_theAerodynamicBuilderInterface.getDeltaRudderList().stream().forEach(
						dr -> cNdrMap.put(
								dr,
								_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
										x -> Tuple.of(
												x,
												MomentCalc.calcCNdrVEDSC(
														aerodynamicAndStabilityManager.getCNBetaVertical()
														.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
														._2(),
														dr, 
														_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSymmetricFlaps().get(0).getMeanChordRatio(),
														_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getAspectRatio(),
														_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAeroDatabaseReader(), 
														_theAerodynamicBuilderInterface.getTheAircraft().getWing().getHighLiftDatabaseReader()
														)
												)
										)
								.collect(Collectors.toList())
								)
						);	

				aerodynamicAndStabilityManager.setCNDeltaR(cNdrMap);

				//=======================================================================================
				// Calculating yawing coefficient breakdown ...
				//=======================================================================================
				aerodynamicAndStabilityManager.setCNFuselage(
						_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
								x -> Tuple.of(
										x,
										MomentCalc.calcNonLinearCNFuselageVEDSC(
												aerodynamicAndStabilityManager.getCNBetaFuselage()
												.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
												._2(),
												aerodynamicAndStabilityManager.getBetaList()
												)
										)
								).collect(Collectors.toList())
						);

				aerodynamicAndStabilityManager.setCNWing(
						_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
								x -> Tuple.of(
										x,
										MomentCalc.calcCNWing(
												aerodynamicAndStabilityManager.getCNBetaWing()
												.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
												._2(),
												aerodynamicAndStabilityManager.getBetaList()
												)
										)
								).collect(Collectors.toList())
						);

				aerodynamicAndStabilityManager.setCNVertical(
						_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
								x -> Tuple.of(
										x,
										MomentCalc.calcNonLinearCNVTailVEDSC(
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAeroDatabaseReader(),
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getPanels().get(0).getSweepLeadingEdge(),
												aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL)
												.getMeanAirfoil().getThicknessToChordRatio(),
												aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL)
												.getMeanAirfoil().getFamily(),
												aerodynamicAndStabilityManager.getCNBetaVertical()
												.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
												._2(),
												aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCLMax().get(
														_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_MAX)
														)*Math.cos(
																aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getAlphaStall().get(
																		_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STALL)
																		).doubleValue(SI.RADIAN)
																),
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSurfacePlanform(),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform(),
												Amount.valueOf(
														Math.abs(
																(aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL)
																		.getXacLRF().get(
																				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																				).plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes())
																		.doubleValue(SI.METER))
																- ((x*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))
																		+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
																		+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER))
																),
														SI.METER),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
												aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getAlphaStar().get(
														_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STAR)
														),
												aerodynamicAndStabilityManager.getBetaList()
												)
										)
								).collect(Collectors.toList())
						);

				aerodynamicAndStabilityManager.setCNTotal(
						_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
								x -> Tuple.of(
										x,
										MomentCalc.calcTotalCN(
												aerodynamicAndStabilityManager.getCNFuselage()
												.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
												._2(),
												aerodynamicAndStabilityManager.getCNWing()
												.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
												._2(),
												aerodynamicAndStabilityManager.getCNVertical()
												.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
												._2()
												)
										)
								).collect(Collectors.toList())
						);

				Map<Amount<Angle>, List<Tuple2<Double, List<Double>>>> cNDueToDeltaRudderMap = new HashMap<>();

				List<Double> tauRudderList = new ArrayList<>();
				if(_theAerodynamicBuilderInterface.getTauRudderFunction() == null)
					_theAerodynamicBuilderInterface.getDeltaRudderList().stream()
					.forEach(dr -> tauRudderList.add(
							StabilityCalculators.calculateTauIndex(
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSymmetricFlaps().get(0).getMeanChordRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAspectRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAeroDatabaseReader(), 
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getHighLiftDatabaseReader(), 
									dr
									)
							));
				else
					_theAerodynamicBuilderInterface.getDeltaRudderList().stream()
					.forEach(dr -> tauRudderList.add(
							_theAerodynamicBuilderInterface.getTauRudderFunction().value(dr.doubleValue(NonSI.DEGREE_ANGLE))
							));

				_theAerodynamicBuilderInterface.getDeltaRudderList().stream().forEach(
						dr -> cNDueToDeltaRudderMap.put(
								dr,
								_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
										x -> Tuple.of(
												x,
												MomentCalc.calcCNDueToDeltaRudder(
														aerodynamicAndStabilityManager.getBetaList(),
														aerodynamicAndStabilityManager.getCNVertical().get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))._2,
														aerodynamicAndStabilityManager.getCNBetaVertical().get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))._2,
														dr, 
														tauRudderList.get(_theAerodynamicBuilderInterface.getDeltaRudderList().indexOf(dr))
														)
												)
										)
								.collect(Collectors.toList())
								)
						);	

				aerodynamicAndStabilityManager.setCNDueToDeltaRudder(cNDueToDeltaRudderMap);
				//=======================================================================================
				// Calculating dr_equilibrium for each beta ...
				//=======================================================================================
				Map<Double, List<Tuple2<Amount<Angle>, Amount<Angle>>>> betaOfEquilibriumListAtCG = new HashMap<>();
				
				//Calculating new tau
				List<Double> tauRudderListForEquilibrium = new ArrayList<>();
				if(_theAerodynamicBuilderInterface.getTauRudderFunction() == null)
					aerodynamicAndStabilityManager.getDeltaRForEquilibrium().stream()
					.forEach(dr -> tauRudderListForEquilibrium.add(
							StabilityCalculators.calculateTauIndex(
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSymmetricFlaps().get(0).getMeanChordRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAspectRatio(),
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAeroDatabaseReader(), 
									_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getHighLiftDatabaseReader(), 
									dr
									)
							));
				else
					aerodynamicAndStabilityManager.getDeltaRForEquilibrium().stream()
					.forEach(dr -> tauRudderListForEquilibrium.add(
							_theAerodynamicBuilderInterface.getTauRudderFunction().value(dr.doubleValue(NonSI.DEGREE_ANGLE))
							));
				//-----------------------------
				
				//Calcultaing new cn array
				Map<Amount<Angle>, List<Tuple2<Double, List<Double>>>> cNDueToDeltaRudderMapForEquilibrium = new HashMap<>();

				aerodynamicAndStabilityManager.getDeltaRForEquilibrium().stream().forEach(
						dr -> cNDueToDeltaRudderMapForEquilibrium.put(
								dr,
								_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
										x -> Tuple.of(
												x,
												MomentCalc.calcCNDueToDeltaRudder(
														aerodynamicAndStabilityManager.getBetaList(),
														aerodynamicAndStabilityManager.getCNVertical().get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))._2,
														aerodynamicAndStabilityManager.getCNBetaVertical().get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))._2,
														dr, 
														tauRudderListForEquilibrium.get(aerodynamicAndStabilityManager.getDeltaRForEquilibrium().indexOf(dr))
														)
												)
										)
								.collect(Collectors.toList())
								)
						);	
				//------------------------
				

				_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(
						x -> betaOfEquilibriumListAtCG.put(
								x, 
								aerodynamicAndStabilityManager.getDeltaRForEquilibrium().stream().map(
										dr -> Tuple.of( 
												Amount.valueOf(
														MyMathUtils.getIntersectionXAndY(
																MyArrayUtils.convertListOfAmountTodoubleArray(aerodynamicAndStabilityManager.getBetaList()),
																MyArrayUtils.convertToDoublePrimitive(
																		cNDueToDeltaRudderMapForEquilibrium
																		.get(dr)
																		.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
																		._2()
																		),
																MyArrayUtils.linspace(
																		0.0,
																		0.0,
																		aerodynamicAndStabilityManager.getBetaList().size()
																		)
																).get(0)._1(),
														NonSI.DEGREE_ANGLE),
												dr)
										)
								.collect(Collectors.toList())
								)
						);

				aerodynamicAndStabilityManager.setBetaOfEquilibrium(
						betaOfEquilibriumListAtCG
						);
				
				//--------------------------------
				
				aerodynamicAndStabilityManager.setCNDeltaA(
						MomentCalc.calcCNDeltaANapolitanoDatcom(
								aerodynamicAndStabilityManager.getCRollDeltaA(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAsymmetricFlaps().get(1).getTheAsymmetricFlapInterface().getInnerStationSpanwisePosition(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAsymmetricFlaps().get(1).getTheAsymmetricFlapInterface().getOuterStationSpanwisePosition(),
								LiftCalc.calculateLiftCoeff(
										(
												_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)
												+ _theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().doubleValue(SI.KILOGRAM)
												)/2*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
										currentMachNumber*AtmosphereCalc.getSpeedOfSound(currentAltitude.doubleValue(SI.METER)),
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
										currentAltitude.doubleValue(SI.METER)
										),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAeroDatabaseReader()
								).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
						);
				
			//----------------------
				
				//=======================================================================================
				// Calculating dynamic derivatives ...
				//=======================================================================================
				int indexOfAlphaBody = 
						MyArrayUtils.getIndexOfClosestValue(
								MyArrayUtils.convertArrayDoublePrimitiveToList(
										MyArrayUtils.convertListOfAmountTodoubleArray(aerodynamicAndStabilityManager.getAlphaBodyList())
										),
								aerodynamicAndStabilityManager.getAlphaBodyCurrent().getEstimatedValue()
								);

				aerodynamicAndStabilityManager.setCNpWing(
						_theAerodynamicBuilderInterface.getXCGAircraft().stream()
						.map(
								x -> Tuple.of(
										x,
										MomentCalc.calcCNpWingNapolitanoDatcom(
												aerodynamicAndStabilityManager.getCRollpWingBody(),
												aerodynamicAndStabilityManager.get_cRollpTotal(),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTwistAerodynamicAtTip(),
												aerodynamicAndStabilityManager.getStaticStabilityMarginMap().get(indexOfAlphaBody),
												_theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCruise(),
												LiftCalc.calculateLiftCoeff(
														(
																_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)
																+ _theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().doubleValue(SI.KILOGRAM)
																)/2*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
														currentMachNumber*AtmosphereCalc.getSpeedOfSound(currentAltitude.doubleValue(SI.METER)),
														_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
														currentAltitude.doubleValue(SI.METER)
														),
												currentMachNumber,
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAeroDatabaseReader()
												).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
										)
								)
						.collect(Collectors.toList())
						);

				aerodynamicAndStabilityManager.setCNpVertical(
						_theAerodynamicBuilderInterface.getXCGAircraft().stream()
						.map(
								x -> Tuple.of(
										x,
										MomentCalc.calcCNpVerticalTailNapolitanoDatcom(
												aerodynamicAndStabilityManager.getCYBetaVertical(),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes()
												.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeX())
												.plus((_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChord()).times(0.25))
												.minus(
														_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().times(x)
														.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX())
														.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes())
														),
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getZApexConstructionAxes()
												.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeZ()),
												_theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCruise()
												).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
										)
								)
						.collect(Collectors.toList())
						);

				aerodynamicAndStabilityManager.setCNpTotal(
						_theAerodynamicBuilderInterface.getXCGAircraft().stream()
						.map(
								x -> Tuple.of(
										x,
										MomentCalc.calcCNpTotalNapolitanoDatcom(
												Amount.valueOf(aerodynamicAndStabilityManager.getCNpWing().get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))._2, NonSI.DEGREE_ANGLE.inverse()),
												Amount.valueOf(aerodynamicAndStabilityManager.getCNpVertical().get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))._2, NonSI.DEGREE_ANGLE.inverse())
												).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
										)
								)
						.collect(Collectors.toList())
						);
				
//-------TODO : continue here 			
				


if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CD_TOTAL)) {
	
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
	
	if(_theAerodynamicBuilderInterface.isPerformCanardAnalyses() == false) {
		//	CD0
		CalcCD0 calcCD0 = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.CANARD).new CalcCD0();
		calcCD0.semiempirical(currentMachNumber, currentAltitude);

		//	CD_WAVE 
		CalcCDWave calcCDWave = aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.CANARD).new CalcCDWave();
		calcCDWave.lockKornWithKroo();
	}
	
	if(_theAerodynamicBuilderInterface.isPerformFuselageAnalyses() == false) {

		//	CD0_TOTAL
		analyses.fuselage.FuselageAerodynamicsManager.CalcCD0Total calcCD0Total = aerodynamicAndStabilityManager.getFuselageAerodynamicManagers().get(ComponentEnum.FUSELAGE).new CalcCD0Total();
		calcCD0Total.semiempirical();
	}
	
	if(_theAerodynamicBuilderInterface.isPerformNacelleAnalyses() == false) {
		
		//	CD0_TOTAL
		analyses.nacelles.NacelleAerodynamicsManager.CalcCD0Total calcCD0Total = aerodynamicAndStabilityManager.getNacelleAerodynamicManagers().get(ComponentEnum.NACELLE).new CalcCD0Total();
		calcCD0Total.semiempirical();
	};
	
	
	//OSWALD FACTOR
	double oswaldFactorTotalAircraft = AerodynamicCalc.calculateOswaldDLR(
			_theAerodynamicBuilderInterface.getTheAircraft(),
			currentMachNumber
			);

	//CD TOTAL
	
	/* FIXME: CHECK ON COMPONENTS. DEFINE CD0 AND CDw AS 0.0 THE FILL IN CASE THE RELATED FIELD IS FILLED IN THE MANAGER */
	
	if(_theAerodynamicBuilderInterface.getTheAircraft().getCanard() != null) {
	cD0TotalAircraft = DragCalc.calculateCD0Total(
			aerodynamicAndStabilityManager.getFuselageAerodynamicManagers().get(ComponentEnum.FUSELAGE).getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
			aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCD0().get(MethodEnum.SEMIEMPIRICAL), 
			aerodynamicAndStabilityManager.getNacelleAerodynamicManagers().get(ComponentEnum.NACELLE).getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
			aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL), 
			aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),
			aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.CANARD).getCD0().get(MethodEnum.SEMIEMPIRICAL),
			aerodynamicAndStabilityManager.get_deltaCDZeroFlap(),
			aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getTheAircraft().getSWetTotal()
			);
	}
	else {
		cD0TotalAircraft = DragCalc.calculateCD0Total(
				aerodynamicAndStabilityManager.getFuselageAerodynamicManagers().get(ComponentEnum.FUSELAGE).getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCD0().get(MethodEnum.SEMIEMPIRICAL), 
				aerodynamicAndStabilityManager.getNacelleAerodynamicManagers().get(ComponentEnum.NACELLE).getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL), 
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),
				aerodynamicAndStabilityManager.get_deltaCDZeroFlap().doubleValue(),
				aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getTheAircraft().getSWetTotal()
				);
	}
	
	//WINGLET
	oswaldFactorTotalAircraft = oswaldFactorTotalAircraft * DragCalc.calculateOswaldFactorModificationDueToWinglet(
			aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getWingletHeight(),
			aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getSpan());
	
	
	double deltaDrag = 0.0;

	if(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().isCalculateExcrescencesDeltaDragCoefficient() == Boolean.TRUE) {
		deltaDrag = DragCalc.calculateCD0Excrescences(
				aerodynamicAndStabilityManager.getFuselageAerodynamicManagers().get(ComponentEnum.FUSELAGE).getCD0Total().get(MethodEnum.SEMIEMPIRICAL),
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCD0().get(MethodEnum.SEMIEMPIRICAL),
				aerodynamicAndStabilityManager.getNacelleAerodynamicManagers().get(ComponentEnum.NACELLE).getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),  
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL),
				aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getTheAircraft().getSWetTotal().doubleValue(SI.SQUARE_METRE));
	}
	else {
		deltaDrag = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getExcrescencesDeltaDragCoefficient();
	}
	deltaDrag = deltaDrag * aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getExcrescencesDragKFactor();
	
	cD0TotalAircraft = cD0TotalAircraft + deltaDrag;
	aerodynamicAndStabilityManager.set_deltaCDZeroExcrescences(deltaDrag);


	deltaDrag = 0.0;

	if(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().isCalculateInterferencesDeltaDragCoefficient() == Boolean.TRUE) {
		deltaDrag = DragCalc.calculateDeltaCD0DueToWingFuselageInterference(
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getPositionRelativeToAttachment(),
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getAirfoilList().get(0).getThicknessToChordRatio(), 
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getSurfaceWetted(), 
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getTheLiftingSurface().getPanels().get(0).getChordRoot()
				);
	}
	else {
		deltaDrag = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getInterferencesDeltaDragCoefficient();
	}
	deltaDrag = deltaDrag * aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getInterferencesDragKFactor();
	cD0TotalAircraft = cD0TotalAircraft + deltaDrag;
	aerodynamicAndStabilityManager.set_deltaCDZeroInterferences(deltaDrag);


	deltaDrag = 0.0;

	if(aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().isCalculateCoolingDeltaDragCoefficient() == Boolean.TRUE) {
		deltaDrag = DragCalc.calculateCD0Cooling(
				aerodynamicAndStabilityManager.getFuselageAerodynamicManagers().get(ComponentEnum.FUSELAGE).getCD0Parasite().get(MethodEnum.SEMIEMPIRICAL), 
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.WING).getCDParasite().get(MethodEnum.SEMIEMPIRICAL).get(0),
				aerodynamicAndStabilityManager.getNacelleAerodynamicManagers().get(ComponentEnum.NACELLE).getCD0Parasite().get(MethodEnum.SEMIEMPIRICAL),
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.HORIZONTAL_TAIL).getCDParasite().get(MethodEnum.SEMIEMPIRICAL).get(0),
				aerodynamicAndStabilityManager.getLiftingSurfaceAerodynamicManagers().get(ComponentEnum.VERTICAL_TAIL).getCDParasite().get(MethodEnum.SEMIEMPIRICAL).get(0)
				);
	}
	else {
		deltaDrag = aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getCoolingDeltaDragCoefficient();
	}
	
	deltaDrag = deltaDrag * aerodynamicAndStabilityManager.getTheAerodynamicBuilderInterface().getCoolingDragKFactor();
	cD0TotalAircraft = cD0TotalAircraft + deltaDrag;
	aerodynamicAndStabilityManager.set_deltaCDZeroCooling(deltaDrag);
}

aerodynamicAndStabilityManager.setCNrWing(
						_theAerodynamicBuilderInterface.getXCGAircraft().stream()
						.map(
								x -> Tuple.of(
										x,
										MomentCalc.calcCNrWingNapolitanoDatcom(
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
												aerodynamicAndStabilityManager.getStaticStabilityMarginMap().get(indexOfAlphaBody),
												cD0TotalAircraft,
												LiftCalc.calculateLiftCoeff(
														(
																_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)
																+ _theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().doubleValue(SI.KILOGRAM)
																)/2*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
														currentMachNumber*AtmosphereCalc.getSpeedOfSound(currentAltitude.doubleValue(SI.METER)),
														_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
														currentAltitude.doubleValue(SI.METER)
														),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAeroDatabaseReader()
												).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
										)
								)
						.collect(Collectors.toList())
						);

				aerodynamicAndStabilityManager.setCNrVertical(
						_theAerodynamicBuilderInterface.getXCGAircraft().stream()
						.map(
								x -> Tuple.of(
										x,
										MomentCalc.calcCNrVerticalTailNapolitanoDatcom(
												aerodynamicAndStabilityManager.getCYBetaVertical(),
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes()
												.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeX())
												.plus((_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChord()).times(0.25))
												.minus(
														_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().times(x)
														.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX())
														.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes())
														),
												_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getZApexConstructionAxes()
												.plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeZ()),
												_theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCruise()
												).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
										)
								)
						.collect(Collectors.toList())
						);

				aerodynamicAndStabilityManager.setCNrTotal(
						_theAerodynamicBuilderInterface.getXCGAircraft().stream()
						.map(
								x -> Tuple.of(
										x,
										MomentCalc.calcCNrTotalNapolitanoDatcom(
												Amount.valueOf(aerodynamicAndStabilityManager.getCNrWing().get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))._2, NonSI.DEGREE_ANGLE.inverse()),
												Amount.valueOf(aerodynamicAndStabilityManager.getCNrVertical().get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))._2, NonSI.DEGREE_ANGLE.inverse())
												).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
										)
								)
						.collect(Collectors.toList())
						);
			}

}
