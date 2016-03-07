package writers;

import java.io.File;

import javax.measure.unit.SI;

import org.apache.commons.lang3.text.WordUtils;

import aircraft.calculators.ACAerodynamicsManager;
import aircraft.calculators.ACPerformanceManager;
import aircraft.components.Aircraft;
import aircraft.components.Configuration;
import aircraft.components.liftingSurface.LiftingSurface;
import calculators.performance.PerformanceCalcManager;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import writers.JPADStaticWriteUtils;

public class JPADChartWriter {

	private Aircraft theAircraft;

	public JPADChartWriter(Aircraft aircraft) {
		theAircraft = aircraft;
	}

	public void createCharts() {
		createAllCharts(theAircraft);	
	}

	public void createAllCharts(Aircraft aircraft) {

		System.out.println();
		System.out.println("--------- WRITING ALL CHARTS TO FILE ----------");
		
		createBalanceCharts(aircraft.get_configuration());
		createAircraftAerodynamicsCharts(aircraft.get_theAerodynamics());
		createLiftingSurfaceCharts(aircraft.get_wing(), true);
		createLiftingSurfaceCharts(aircraft.get_HTail(), true);
		createLiftingSurfaceCharts(aircraft.get_VTail(), true);
		createLiftingSurfaceCharts(aircraft.get_Canard(), true);
		createPerformanceCharts(aircraft.get_performances(), aircraft.get_performances().getPerformanceManager(), "AEO");
		createPerformanceCharts(aircraft.get_performances(), aircraft.get_performances().getPerformanceManagerOEI(), "OEI");
		
		System.out.println("--------- DONE WRITING CHARTS TO FILE ----------");
	}

	public void createBalanceCharts(Configuration configuration) {

		new MyChartToFileUtils().createMultiTraceTikz(
				MyArrayUtils.convertListOfAmountTodoubleArray(configuration.get_seatsCoGFrontToRear()),
				MyArrayUtils.convertListOfAmountTodoubleArray(configuration.get_currentMassList()),
				MyArrayUtils.convertListOfAmountTodoubleArray(configuration.get_seatsCoGRearToFront()),
				MyArrayUtils.convertListOfAmountTodoubleArray(configuration.get_currentMassList()),
				null, null,
				JPADWriteUtils.createImagesFolder("Balance"),
				"loadingCycle",
				"$X_{cg}$", "Mass",
				"m","kg");
	}

	public void createAircraftAerodynamicsCharts(ACAerodynamicsManager aeroCalc) {

		new MyChartToFileUtils().createTikzFromMap(
				aeroCalc.get_cL(),
				aeroCalc.get_cDMap(),
				JPADWriteUtils.createImagesFolder("Aerodynamics"),
				"drag_polar",
				"$C_D$", "$C_L$",
				"","", 
				"M", true);
	}

	/** 
	 * Lifting surface graphs 
	 * 
	 * @author LA
	 * @param liftingSurface
	 */
	public void createLiftingSurfaceCharts(LiftingSurface liftingSurface, boolean makeGraphs){

		try {
			// TODO: modify nasa Blackwell method to support vertical tail
			if (!liftingSurface.get_type().equals(ComponentEnum.VERTICAL_TAIL)) {

				new MyChartToFileUtils().createGraphFromTable(
						liftingSurface.getAerodynamics().get_yStationsND(),
						liftingSurface.getAerodynamics().getcLMap().getCxyVsAlphaTable(),
						JPADWriteUtils.createImagesFolder("Aerodynamics"),
						"cl_distribution" 
								+ WordUtils.capitalizeFully(liftingSurface.get_type().name()),
								"$Y\\;(\\mathrm{m})$", "$C_l$",
								"","", 
								"$\\alpha$",
								"solid", "black",
								makeGraphs);

				new MyChartToFileUtils().createGraphFromTable(
						liftingSurface.getAerodynamics().get_yStationsND(),
						liftingSurface.getAerodynamics().getcLMap().getCcxyVsAlphaTable(),
						JPADWriteUtils.createImagesFolder("Aerodynamics"),
						"cCl_distribution" 
								+ WordUtils.capitalizeFully(liftingSurface.get_type().name()),
								"$Y\\;(\\mathrm{m})$", "$c C_l$",
								"","",
								"solid", "black",
								"$\\alpha$", 
								makeGraphs);		
			}

		} catch(NullPointerException e) { }
	}

	public static void createPerformanceCharts(
			ACPerformanceManager performance, 
			PerformanceCalcManager perfManager,
			String engineCondition) {

		System.out.println();
		System.out.println("--------- WRITING PERFORMANCE CHARTS TO FILE ----------");
		
		MyChartToFileUtils chartFactory;
		String folderPath = JPADWriteUtils.createImagesFolder("Performances") + File.separator + engineCondition + File.separator;
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath);
		
		double weightPercentMTOW = 0.87;
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Speed", "Total Thrust", 
				"m/s", "N", "altitude", perfManager.getAltitudeShort().toArray(), "m", 
				subfolderPath, "ThrustVsSpeed_Phi100_CRUISE");
		chartFactory.setXarrays(perfManager.getSpeed().toArray());
		chartFactory.setYarrays(perfManager.getThrustvsAltitude(1., EngineOperatingConditionEnum.CRUISE));
		chartFactory.setxMax(performance.get_vMaxCruise().doubleValue(SI.METERS_PER_SECOND));
		chartFactory.setyMin(0.);
		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Speed", "Total Thrust", 
				"m/s", "N", "altitude", perfManager.getAltitudeShort().toArray(), "m", 
				subfolderPath, "ThrustVsSpeed_Phi100_CLIMB");
		chartFactory.setXarrays(perfManager.getSpeed().toArray());
		chartFactory.setYarrays(perfManager.getThrustvsAltitude(1., EngineOperatingConditionEnum.CLIMB));
		chartFactory.setxMax(performance.get_vMaxCruise().doubleValue(SI.METERS_PER_SECOND));
		chartFactory.setyMin(0.);
		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Speed", "Drag", 
				"m/s", "N", "altitude", perfManager.getAltitudeShort().toArray(), "m", 
				subfolderPath, "DragVsSpeedMZFW");
		chartFactory.setXarrays(perfManager.getSpeed().toArray());
		chartFactory.setYarrays(perfManager.getDragMinWeight());
		chartFactory.setxMax(performance.get_vMaxCruise().doubleValue(SI.METERS_PER_SECOND));
		chartFactory.setyMin(0.);
//		TODO
//		chartFactory.setyMax(theAircraft.get_powerPlant().get_T0Total().doubleValue(SI.NEWTON));
		chartFactory.setyMax(62000*2.);
		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Speed", "Total Thrust, Drag", 
				"m/s", "N", "altitude", MyArrayUtils.duplicateArray(perfManager.getAltitudeShort().toArray()), "m", 
				subfolderPath, "TotalThrustAndDragVsSpeed_Phi075_CRUISE");
		chartFactory.setXarrays(perfManager.getSpeed().toArray());
		chartFactory.setYarrays(perfManager.getDragMinWeight());
		chartFactory.setYarrays(perfManager.getThrustvsAltitude(0.75, EngineOperatingConditionEnum.CRUISE));
		chartFactory.setxMax(performance.get_vMaxCruise().doubleValue(SI.METERS_PER_SECOND));
		chartFactory.setyMin(0.);
//		TODO
//		chartFactory.setyMax(theAircraft.get_powerPlant().get_T0Total().doubleValue(SI.NEWTON));
		chartFactory.setyMax(62000*2.);
		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Speed", "Total Thrust, Drag", 
				"m/s", "N", "altitude", MyArrayUtils.duplicateArray(perfManager.getAltitudeShort().toArray()), "m", 
				subfolderPath, "TotalThrustAndDragVsSpeed_Phi100_CRUISE");
		chartFactory.setXarrays(perfManager.getSpeed().toArray());
		chartFactory.setYarrays(perfManager.getDragMinWeight());
		chartFactory.setYarrays(perfManager.getThrustvsAltitude(1., EngineOperatingConditionEnum.CRUISE));
		chartFactory.setxMax(performance.get_vMaxCruise().doubleValue(SI.METERS_PER_SECOND));
		chartFactory.setyMin(0.);
//		TODO
//		chartFactory.setyMax(theAircraft.get_powerPlant().get_T0Total().doubleValue(SI.NEWTON));
		chartFactory.setyMax(62000*2.);
		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Speed", "Rate of Climb", 
				"m/s", "m/s", "altitude", perfManager.getAltitudeShort().toArray(), "m", 
				subfolderPath, "RateOfClimb_Phi100_MZFW");
		chartFactory.setXarrays(perfManager.getSpeed().toArray());
		chartFactory.setYarrays(perfManager.getRCvsAltitudeMinWeight(1.));
		chartFactory.setxMax(performance.get_vMaxCruise().doubleValue(SI.METERS_PER_SECOND));
		chartFactory.setyMin(0.);
		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Speed", "Rate of Climb", 
				"m/s", "m/s", "altitude", perfManager.getAltitudeShort().toArray(), "m", 
				subfolderPath, "RateOfClimb_Phi100_" + weightPercentMTOW + "MTOW");
		chartFactory.setXarrays(perfManager.getSpeed().toArray());
		chartFactory.setYarrays(perfManager.getRCvsAltitudeMinWeight(1.));
		chartFactory.setxMax(performance.get_vMaxCruise().doubleValue(SI.METERS_PER_SECOND));
		chartFactory.setyMin(0.);
		chartFactory.createMultiTraceChart();

		chartFactory = MyChartToFileUtils.ChartFactory(
				"Speed", "Rate of Climb", 
				"m/s", "m/s", "altitude", perfManager.getAltitudeShort().toArray(), "m", 
				subfolderPath, "RateOfClimb_Phi100_MTOW");
		chartFactory.setXarrays(perfManager.getSpeed().toArray());
		chartFactory.setYarrays(perfManager.getRCvsAltitudeMaxWeight(1.));
		chartFactory.setxMax(performance.get_vMaxCruise().doubleValue(SI.METERS_PER_SECOND));
		chartFactory.setyMin(0.);
		chartFactory.createMultiTraceChart();

		chartFactory = MyChartToFileUtils.ChartFactory(
				"Speed", "Climb Angle", 
				"m/s", "°", "altitude", perfManager.getAltitudeShort().toArray(), "m", 
				subfolderPath, "ClimbAngle_Phi100_MZFW");
		chartFactory.setXarrays(perfManager.getSpeed().toArray());
		chartFactory.setYarrays(perfManager.getGammaDegVsAltitudeMinWeight(1.));
		chartFactory.setxMax(performance.get_vMaxCruise().doubleValue(SI.METERS_PER_SECOND));
		chartFactory.setyMin(0.); //TODO: check max and min angles
		chartFactory.setyMax(20.);
		chartFactory.createMultiTraceChart();

		chartFactory = MyChartToFileUtils.ChartFactory(
				"Speed", "Climb Angle", 
				"m/s", "°", "altitude", perfManager.getAltitudeShort().toArray(), "m", 
				subfolderPath, "ClimbAngle_Phi100_MTOW");
		chartFactory.setXarrays(perfManager.getSpeed().toArray());
		chartFactory.setYarrays(perfManager.getGammaDegVsAltitudeMaxWeight(1.));
		chartFactory.setxMax(performance.get_vMaxCruise().doubleValue(SI.METERS_PER_SECOND));
		chartFactory.setyMin(0.);
		chartFactory.setyMax(20.);
		chartFactory.createMultiTraceChart();

		chartFactory = MyChartToFileUtils.ChartFactory(
				"Altitude", "Rate of Climb", "m", "m/s", 
				"altitude", perfManager.getAltitudeShort().toArray(), "m", 
				subfolderPath, "RateOfClimbMax_Phi100_MZFW");
		chartFactory.setXarrays(perfManager.getAltitude().toArray());
		chartFactory.setYarrays(perfManager.getRCmaxMinWeight(1.));
		chartFactory.setyMin(0.);
		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Altitude", "Rate of Climb", "m", "m/s", 
				"altitude", perfManager.getAltitudeShort().toArray(), "m", 
				subfolderPath, "RateOfClimbMax_Phi100_" + weightPercentMTOW + "MTOW");
		chartFactory.setXarrays(perfManager.getAltitude().toArray());
		chartFactory.setYarrays(perfManager.getRCmax(weightPercentMTOW, 1., EngineOperatingConditionEnum.CLIMB));
		chartFactory.setyMin(0.);
		chartFactory.createMultiTraceChart();

//		chartFactory = MyChartToFileUtils.ChartFactory(
//				"Altitude", "RC", "m", "m/s", 
//				"altitude", perfManager.getAltitudeShort().toArray(), "m", 
//				subfolderPath, "RateOfClimbMaxMTOW");
//		chartFactory.setXarrays(perfManager.getAltitude().toArray());
//		chartFactory.setYarrays(perfManager.getRCmaxMaxWeight(1.));
//		chartFactory.setyMin(0.);
//		chartFactory.createMultiTraceChart();

		MyChartToFileUtils.plot(perfManager.getAltitude().toArray(), 
				perfManager.getRCmaxMaxWeight(1.), 
				null, null, 0., null, "Altitude", "Rate of Climb", "m", "m/s", 
				"altitude", perfManager.getAltitudeShort().toArray(), "m", 
				subfolderPath, "RateOfClimbMax_Phi100_MTOW");
		
//		chartFactory = MyChartToFileUtils.ChartFactory(
//				"Speed", "Altitude", "m/s", "m", 
//				"phi", perfManager.getPhiShort().toArray(), "m", 
//				subfolderPath, "test");
//		chartFactory.setYarrays(perfManager.getMaximumSpeedMinWeight(FlightConditionEnum.CLIMB));
//		chartFactory.setXarrays(perfManager.getAltitude().toArray());
//		chartFactory.setyMin(0.);
//		chartFactory.setSwapXY(true);
//		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Altitude", "Speed", "m", "m/s", 
				"phi", perfManager.getPhiShort().toArray(), "", 
				subfolderPath, "FlightEnvelope_MZFW_CLIMB");
		chartFactory.setXarrays(perfManager.getAltitudeEnvelope().toArray());
		chartFactory.setYarrays(perfManager.getMinimumAndMaximumSpeedAsSingleCurveMinWeight(EngineOperatingConditionEnum.CLIMB));
		chartFactory.setyMin(0.);
		chartFactory.setSwapXY(true);
		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Altitude", "Speed", "m", "m/s", 
				"phi", perfManager.getPhiShort().toArray(), "", 
				subfolderPath, "FlightEnvelope_MTOW_CLIMB");
		chartFactory.setXarrays(perfManager.getAltitudeEnvelope().toArray());
		chartFactory.setYarrays(perfManager.getMinimumAndMaximumSpeedAsSingleCurveMaxWeight(EngineOperatingConditionEnum.CLIMB));
		chartFactory.setyMin(0.);
		chartFactory.setSwapXY(true);
		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Altitude", "Speed", "m", "m/s", 
				"phi", perfManager.getPhiShort().toArray(), "", 
				subfolderPath, "FlightEnvelope_MZFW_CRUISE");
		chartFactory.setXarrays(perfManager.getAltitudeEnvelope().toArray());
		chartFactory.setYarrays(perfManager.getMinimumAndMaximumSpeedAsSingleCurveMinWeight(EngineOperatingConditionEnum.CRUISE));
		chartFactory.setyMin(0.);
		chartFactory.setSwapXY(true);
		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Altitude", "Speed", "m", "m/s", 
				"phi", perfManager.getPhiShort().toArray(), "", 
				subfolderPath, "FlightEnvelope_MTOW_CRUISE");
		chartFactory.setXarrays(perfManager.getAltitudeEnvelope().toArray());
		chartFactory.setYarrays(perfManager.getMinimumAndMaximumSpeedAsSingleCurveMaxWeight(EngineOperatingConditionEnum.CRUISE));
		chartFactory.setyMin(0.);
		chartFactory.setSwapXY(true);
		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Altitude", "Speed", "m", "m/s", 
				"phi", perfManager.getPhiShort().toArray(), "", 
				subfolderPath, "FlightEnvelope_" + weightPercentMTOW + "MTOW_CLIMB");
		chartFactory.setXarrays(perfManager.getAltitudeEnvelope().toArray());
		chartFactory.setYarrays(perfManager.getMinimumAndMaximumSpeedAsSingleCurveMaxWeight(weightPercentMTOW, EngineOperatingConditionEnum.CLIMB));
		chartFactory.setyMin(0.);
		chartFactory.setSwapXY(true);
		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Altitude", "Speed", "m", "m/s", 
				"phi", perfManager.getPhiShort().toArray(), "", 
				subfolderPath, "FlightEnvelope_" + weightPercentMTOW + "MTOW_CRUISE");
		chartFactory.setXarrays(perfManager.getAltitudeEnvelope().toArray());
		chartFactory.setYarrays(perfManager.getMinimumAndMaximumSpeedAsSingleCurveMaxWeight(weightPercentMTOW, EngineOperatingConditionEnum.CRUISE));
		chartFactory.setyMin(0.);
		chartFactory.setSwapXY(true);
		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Speed", "Range", "m/s", "m", 
				"altitude", perfManager.getAltitudeShort().toArray(), "", 
				subfolderPath, "RangeVsSpeedVsAltitude");
		chartFactory.setXarrays(perfManager.getSpeed().toArray());
		chartFactory.setYarrays(perfManager.getRangeManager().getRangeSpeedAndAltitudeConstantM());
		chartFactory.setyMin(0.);
		chartFactory.createMultiTraceChart();
		
		chartFactory = MyChartToFileUtils.ChartFactory(
				"Speed", "Range", "m/s", "m", 
				"weight", perfManager.getWeight().toArray(), "", 
				subfolderPath, "RangeVsSpeedVsWeight");
		chartFactory.setXarrays(perfManager.getSpeed().toArray());
		chartFactory.setYarrays(perfManager.getRangeManager().getRangeSpeedAndClConstantM());
		chartFactory.setyMin(0.);
		chartFactory.createMultiTraceChart();
	
		JPADStaticWriteUtils.logToConsole("--------- DONE WRITING PERFORMANCE CHARTS TO FILE ----------");
	}

}

