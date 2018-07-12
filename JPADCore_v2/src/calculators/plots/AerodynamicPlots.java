package calculators.plots;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;

import analyses.IACAerodynamicAndStabilityManager;
import analyses.fuselage.FuselageAerodynamicsManager;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager;
import analyses.nacelles.NacelleAerodynamicsManager;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.AerodynamicAndStabilityPlotEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import javaslang.Tuple2;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;

public class AerodynamicPlots {

	public void plotAllCharts (IACAerodynamicAndStabilityManager _theAerodynamicBuilderInterface, 
			Map<ComponentEnum, LiftingSurfaceAerodynamicsManager> _liftingSurfaceAerodynamicManagers,
			String wingPlotFolderPath,
			String horizontalTailPlotFolderPath,
			String canardPlotFolderPath,
			String verticalTailPlotFolderPath,
			String fuselagePlotFolderPath,
			String nacellePlotFolderPath,
			String aircraftPlotFolderPath,
			Map<Amount<Angle>, List<Double>> _current3DHorizontalTailLiftCurve,
			Map<Amount<Angle>, List<Double>> _current3DHorizontalTailPolarCurve,
			Map<Amount<Angle>, List<Double>> _current3DHorizontalTailMomentCurve,
			Map<Amount<Angle>, List<Double>> _current3DVerticalTailLiftCurve,
			List<Amount<Angle>> _alphaBodyList,
			Map<ComponentEnum, NacelleAerodynamicsManager>_nacelleAerodynamicManagers,
			Map<ComponentEnum, FuselageAerodynamicsManager> _fuselageAerodynamicManagers,
			Map<ComponentEnum, Map<MethodEnum, Map<Boolean, List<Amount<Angle>>>>> _downwashAngleMap,
			Map<ComponentEnum, Map<MethodEnum, Map<Boolean, List<Double>>>> _downwashGradientMap,
			Map<Double, Map<Amount<Angle>, List<Double>>> _totalMomentCoefficient,
			Map<Amount<Angle>, List<Double>> _totalLiftCoefficient,
			Map<Amount<Angle>, List<Double>> _totalDragCoefficient,
			Map<Double, List<Double>> _horizontalTailEquilibriumLiftCoefficient,
			Map<Double, List<Double>> _horizontalTailEquilibriumDragCoefficient,
			Map<Double, List<Double>> _totalEquilibriumLiftCoefficient,
			Map<Double, List<Double>> _totalEquilibriumDragCoefficient,
			Map<Double, List<Amount<Angle>>> _deltaEEquilibrium,
			Map<Double, Map<ComponentEnum, List<Double>>> _momentCoefficientBreakDown,
			Map<Double, List<Double>> _totalEquilibriumEfficiencyMap ,
			Map<Double, Double> _totalEquilibriumMaximumEfficiencyMap,
			Map<Double, List<Double>> _neutralPointPositionMap,
			Map<Double, List<Double>> _staticStabilityMarginMap,
			List<Amount<Angle>> _deltaEForEquilibrium,
			List<Amount<Angle>> _betaList,
			Map<MethodEnum, Map<Double, List<Tuple2<Amount<Angle>, Amount<Angle>>>>> _betaOfEquilibrium,
			Map<MethodEnum, Map<Amount<Angle>, List<Tuple2<Double, List<Double>>>>> _cNDueToDeltaRudder,
			Map<MethodEnum, List<Tuple2<Double, List<Double>>>> _cNTotal,
			Map<MethodEnum, List<Tuple2<Double, List<Double>>>> _cNFuselage,
			Map<MethodEnum, List<Tuple2<Double, List<Double>>>> _cNVertical,
			Map<MethodEnum, List<Tuple2<Double, List<Double>>>> _cNWing) {
		
		List<Double[]> xVectorMatrix = new ArrayList<Double[]>();
		List<Double[]> yVectorMatrix = new ArrayList<Double[]>();
		List<String> legend  = new ArrayList<>(); 
		double[][] xMatrix;
		double[][] yMatrix;
		String[] legendString;

		List<Double> xVector = new ArrayList<Double>();
		List<Double> yVector = new ArrayList<Double>();

		// wing
		if (_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.CRUISE || 
				_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.CLIMB ) {

			//-----------------------------------------------------------------------------------------------------------------------
			// WING
			if(_theAerodynamicBuilderInterface.getTheAircraft().getWing() != null) {

				//-----------------------------------------------------------------------------------------------------------------------
				// LIFT CURVE
				try {

					if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_LIFT_CURVE_CLEAN)) {

						if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

							xVector = new ArrayList<Double>();
							yVector = new ArrayList<Double>();

							xVector = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertListOfAmountToDoubleArray(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean()
									));
							yVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));


							MyChartToFileUtils.plotNoLegend(
									MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
									MyArrayUtils.convertToDoublePrimitive(yVector),
									null, 
									null, 
									null, 
									null, 
									"alpha",
									"CL",
									"deg", 
									"",
									wingPlotFolderPath,
									"Lift_Coefficient_Curve",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability()
									);
						}
						else 
							System.err.println("WARNING!! THE WING CLEAN LIFT CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING CLEAN LIFT CURVE");
					}
				}
				catch (NullPointerException e) {
					System.err.println("WARNING: (PLOT WING LIFT CURVE) MISSING VALUES ...");
				}
				//-----------------------------------------------------------------------------------------------------------------------
				// POLAR CURVE
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_POLAR_CURVE)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

						xVector = new ArrayList<Double>();
						yVector = new ArrayList<Double>();

						xVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
								));
						yVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));

						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
								MyArrayUtils.convertToDoublePrimitive(yVector),
								null, 
								null, 
								null, 
								null, 
								"CD",
								"CL",
								"", 
								"",
								wingPlotFolderPath,
								"Polar_Curve",
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability()
								);

					}
					else
						System.err.println("WARNING!! THE WING CLEAN DRAG POLAR CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING CLEAN DRAG POLAR CURVE");
				}
				//-----------------------------------------------------------------------------------------------------------------------
				// MOMENT CURVE
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_MOMENT_CURVE_CLEAN)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

						xVector = new ArrayList<Double>();
						yVector = new ArrayList<Double>();

						xVector = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean()
								));
						yVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMoment3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)));

						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
								MyArrayUtils.convertToDoublePrimitive(yVector),
								null, 
								null, 
								null, 
								null, 
								"alpha",
								"CM",
								"deg", 
								"",
								wingPlotFolderPath,
								"Moment_Coefficient_Curve",
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability()
								);
					}
					else
						System.err.println("WARNING!! THE WING CLEAN PITCHING MOMENT CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING CLEAN PITCHING MOMENT CURVE");
				}
				//-----------------------------------------------------------------------------------------------------------------------
				// STALL PATH
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_STALL_PATH)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
					xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));


					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getClMaxDistribution()));
					yVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistributionAtCLMax().get(MethodEnum.NASA_BLACKWELL)));

					legend.add("Cl max airfoils");
					legend.add("Cl distribution at stall");


					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Stall_Path",
								"eta", 
								"Cl",
								null, 
								null, 
								null, 
								null,
								"", 
								"", 
								true,
								legend,
								wingPlotFolderPath,
								"Stall_Path",
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				//-----------------------------------------------------------------------------------------------------------------------
				// LIFT COEFFICIENT DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_CL_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Cl distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Lift_Coefficient_Distributions",
									"eta", 
									"CL",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Lift_Coefficient_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING CL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING CL DISTRIBUTION");
				}
				//-----------------------------------------------------------------------------------------------------------------------
				// DRAG COEFFICIENT DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_CD_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDragCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Cd distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Drag_Coefficient_Distributions",
									"eta", 
									"Cd",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Drag_Coefficient_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING CD DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING CD DISTRIBUTION");
				}
				//-----------------------------------------------------------------------------------------------------------------------
				// MOMENT COEFFICIENT DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_CM_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Cm distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Moment_Coefficient_Distributions",
									"eta", 
									"Cm",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Moment_Coefficient_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING CM DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING CM DISTRIBUTION");
				}
				//-----------------------------------------------------------------------------------------------------------------------
				// CL ADDITIONAL DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_CL_ADDITIONAL_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Cl additional distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Additional_Lift_Coefficient_Distributions",
									"eta", 
									"Cl_add.",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Additional_Lift_Coefficient_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}
					}
					else
						System.err.println("WARNING!! THE WING ADDITIONAL CL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING ADDITIONAL CL DISTRIBUTION");
				}
				//-----------------------------------------------------------------------------------------------------------------------
				// CL BASIC DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_CL_BASIC_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistributionBasicLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Cl basic distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Basic_Lift_Coefficient_Distributions",
									"eta", 
									"Cl_basic",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									false,
									legend,
									wingPlotFolderPath,
									"Basic_Lift_Coefficient_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}


					}
					else
						System.err.println("WARNING!! THE WING BASIC CL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING BASIC CL DISTRIBUTION");
				}
				//-----------------------------------------------------------------------------------------------------------------------
				// cCL DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_cCL_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("cCl distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"cCl_Distributions",
									"eta", 
									"Ccl",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"cCl_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}	
					else
						System.err.println("WARNING!! THE WING cCL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING cCL DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// cCL ADDITIONAL DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_cCL_ADDITIONAL_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("cCl additional distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"cCl_Additional_Distributions",
									"eta", 
									"Ccl add.",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"cCl_Additional_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}

					}	
					else
						System.err.println("WARNING!! THE WING cCL ADDITIONAL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING cCL ADDITIONAL DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// cCL BASIC DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_cCL_BASIC_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionBasicLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("cCl basic distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"cCl_Basic_Distributions",
									"eta", 
									"Ccl basic",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									false,
									legend,
									wingPlotFolderPath,
									"cCl_Basic_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}

					}	
					else
						System.err.println("WARNING!! THE WING cCL BASIC DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING cCL BASIC DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// GAMMA DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_GAMMA_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Gamma distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Gamma_Distributions",
									"eta", 
									"gamma",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Gamma_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING GAMMA DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING GAMMA DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// GAMMA ADDITIONAL DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_GAMMA_ADDITIONAL_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Gamma additional distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Gamma_Additional_Distributions",
									"eta", 
									"gamma add.",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Gamma_Additional_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}


					}
					else
						System.err.println("WARNING!! THE WING GAMMA ADDITIONAL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING GAMMA ADDITIONAL DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// GAMMA BASIC DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_GAMMA_BASIC_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistributionBasicLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Gamma basic distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Gamma_Basic_Distributions",
									"eta", 
									"gamma basic",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									false,
									legend,
									wingPlotFolderPath,
									"Gamma_Basic_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING GAMMA BASIC DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING GAMMA BASIC DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// LOAD DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_TOTAL_LOAD_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Total load distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Total_Load_Distributions",
									"eta", 
									"total load",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Total_Load_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING LOAD DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING LOAD DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// ADDITIONAL LOAD DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_ADDITIONAL_LOAD_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAdditionalLoadDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Additional load distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Additional_Load_Distributions",
									"eta", 
									"additional load",
									null, 
									null, 
									null, 
									null,
									"", 
									"Newton", 
									true,
									legend,
									wingPlotFolderPath,
									"Additional_Load_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}

					}		
					else
						System.err.println("WARNING!! THE WING ADDITIONAL LOAD DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING ADDITIONAL LOAD DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// BASIC LOAD DISTRIBUTION 
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_BASIC_LOAD_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getBasicLoadDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Basic load distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}


						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Basic_Load_Distributions",
									"eta", 
									"basic load",
									null, 
									null, 
									null, 
									null,
									"", 
									"Newton", 
									false,
									legend,
									wingPlotFolderPath,
									"Basic_Load_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING BASIC LOAD DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING BASIC LOAD DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// MOMENT DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_MOMENT_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Moment distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Moment_Distributions",
									"eta", 
									"basic load",
									null, 
									null, 
									null, 
									null,
									"", 
									"Nm", 
									true,
									legend,
									wingPlotFolderPath,
									"Moment_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}


					}
					else
						System.err.println("WARNING!! THE WING MOMENT DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING MOMENT DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// DRAG DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_DRAG_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDragDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Drag distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Moment_Distributions",
									"eta", 
									"Cd",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Drag_Distributions",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING DRAG DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING DRAG DISTRIBUTION");
				}

			} // end climb cruise

			if (_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.TAKE_OFF || 
					_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.LANDING ) {


				int indexOfMaxHighLift = MyArrayUtils.getIndexOfMax(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList()
						.get(ComponentEnum.WING)
						.get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)));

				//-----------------------------------------------------------------------------------------------------------------------
				// HIGH LIFT CURVE
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_LIFT_CURVE_HIGH_LIFT)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
							|| _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean()
								));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));
						if(indexOfMaxHighLift+3<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArray().size()) {
							xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArray().subList(0, indexOfMaxHighLift+3)));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray((MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D))).subList(0, indexOfMaxHighLift+3))));
						}
						else if(indexOfMaxHighLift+1<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArray().size()) {
							xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArray().subList(0, indexOfMaxHighLift+1)));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray((MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D))).subList(0, indexOfMaxHighLift+1))));
						}
						else { 
							xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArray().subList(0, indexOfMaxHighLift)));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray((MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D))).subList(0, indexOfMaxHighLift))));
						}
						legend.add("Clean configuration");
						legend.add("Configuration with high lift devices");


						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}


						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Lift_Coefficient_Curve clean and high lift",
									"alpha", 
									"CL",
									null, 
									null, 
									null, 
									null,
									"deg", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Lift_Coefficient_Curve",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}
					}
					else
						System.err.println("WARNING!! THE WING HIGH LIFT AND CLEAN LIFT CURVES HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING HIGH LIFT CURVE");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// HIGH LIFT DRAG POLAR CURVE
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_POLAR_CURVE_HIGH_LIFT)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
							|| _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						xVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
								));
						xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray((MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D))).subList(0, indexOfMaxHighLift+1))));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray((MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D))).subList(0, indexOfMaxHighLift+1))));

						legend.add("Clean configuration");
						legend.add("Configuration with high lift devices");


						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Polar_Curve",
									"CD", 
									"CL",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Polar_Curve",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING HIGH LIFT AND CLEAN DRAG POLAR CURVES HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING HIGH LIFT DRAG POLAR CURVE");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// HIGH LIFT MOMENT CURVE 
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_MOMENT_CURVE_HIGH_LIFT)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
							|| _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean()
								));
						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArray()
								));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMoment3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentCoefficient3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)));

						legend.add("Clean configuration");
						legend.add("Configuration with high lift devices");


						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Moment_Coefficient_Curve",
									"alpha", 
									"CM",
									null, 
									null, 
									null, 
									null,
									"deg", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Moment_Coefficient_Curve",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}


					}
					else
						System.err.println("WARNING!! THE WING HIGH LIFT AND CLEAN MOMENT CURVES HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING HIGH LIFT MOMENT CURVE");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// STALL PATH
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_STALL_PATH)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
					xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));


					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getClMaxDistribution()));
					yVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistributionAtCLMax().get(MethodEnum.NASA_BLACKWELL)));

					legend.add("Cl max airfoils");
					legend.add("Cl distribution at stall");


					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Stall_Path_Clean_Configuration",
								"eta", 
								"Cl",
								null, 
								null, 
								null, 
								null,
								"", 
								"", 
								true,
								legend,
								wingPlotFolderPath,
								"Stall_Path_Clean_Configuration",
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				//-----------------------------------------------------------------------------------------------------------------------
				// LIFT COEFFICIENT DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_CL_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Cl distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}


						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Lift_Coefficient_Distributions_Clean_Configuration",
									"eta", 
									"Cl",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Lift_Coefficient_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING CL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING CL DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// DRAG COEFFICIENT DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_CD_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDragCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Cd distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}
						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Drag_Coefficient_Distributions_Clean_Configuration",
									"eta", 
									"Cd",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Drag_Coefficient_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING CD DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING CD DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// MOMENT COEFFICIENT DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_CM_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Cm distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Moment_Coefficient_Distributions_Clean_Configuration",
									"eta", 
									"Cm",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Moment_Coefficient_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING CM DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING CMmom DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// CL ADDITIONAL DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_CL_ADDITIONAL_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Cl additional distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Additional_Lift_Coefficient_Distributions_Clean_Configuration",
									"eta", 
									"CL add.",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									wingPlotFolderPath,
									"Additional_Lift_Coefficient_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING ADDITIONAL CL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING ADDITIONAL CL DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// CL BASIC DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_CL_BASIC_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistributionBasicLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Cl basic distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Basic_Lift_Coefficient_Distributions_Clean_Configuration",
									"eta", 
									"CL basic",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									false,
									legend,
									wingPlotFolderPath,
									"Basic_Lift_Coefficient_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING BASIC CL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING BASIC CL DISTRIBUTION");
				}
				//-----------------------------------------------------------------------------------------------------------------------
				// cCL DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_cCL_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("cCl distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"cCl_Distributions_Clean_Configuration",
									"eta", 
									"cCl",
									null, 
									null, 
									null, 
									null,
									"", 
									"m", 
									true,
									legend,
									wingPlotFolderPath,
									"cCl_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}		
					else
						System.err.println("WARNING!! THE WING cCL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING cCL DISTRIBUTION");
				}
				//-----------------------------------------------------------------------------------------------------------------------
				// cCL ADDITIONAL DISTRIBUTION

				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_cCL_ADDITIONAL_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("cCl additional distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"cCl_Additional_Distributions_Clean_Configuration",
									"eta", 
									"cCl_add.",
									null, 
									null, 
									null, 
									null,
									"", 
									"m", 
									true,
									legend,
									wingPlotFolderPath,
									"cCl_Additional_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}	
					else
						System.err.println("WARNING!! THE WING ADDITIONAL cCL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING ADDITIONAL cCL DISTRIBUTION");
				}
				//-----------------------------------------------------------------------------------------------------------------------
				// cCL BASIC DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_cCL_BASIC_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionBasicLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("cCl basic distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"cCl_Basic_Distributions_Clean_Configuration",
									"eta", 
									"cCl_basic",
									null, 
									null, 
									null, 
									null,
									"", 
									"m", 
									false,
									legend,
									wingPlotFolderPath,
									"cCl_Basic_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}


					}	
					else
						System.err.println("WARNING!! THE WING BASIC cCL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING BASIC cCL DISTRIBUTION");
				}
				//-----------------------------------------------------------------------------------------------------------------------
				// GAMMA DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_GAMMA_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Gamma distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Gamma_Distributions_Clean_Configuration",
									"eta", 
									"gamma",
									null, 
									null, 
									null, 
									null,
									"", 
									"m^2/s", 
									true,
									legend,
									wingPlotFolderPath,
									"Gamma_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING GAMMA DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING GAMMA DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// GAMMA ADDITIONAL DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_GAMMA_ADDITIONAL_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Gamma additional distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Gamma_Additional_Distributions_Clean_Configuration",
									"eta", 
									"gamma_add",
									null, 
									null, 
									null, 
									null,
									"", 
									"m^2/s", 
									true,
									legend,
									wingPlotFolderPath,
									"Gamma_Additional_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING ADDITIONAL GAMMA DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING ADDITIONAL GAMMA DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// GAMMA BASIC DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_GAMMA_BASIC_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistributionBasicLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Gamma basic distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Gamma_Basic_Distributions_Clean_Configuration",
									"eta", 
									"gamma_basic",
									null, 
									null, 
									null, 
									null,
									"", 
									"m^2/s", 
									false,
									legend,
									wingPlotFolderPath,
									"Gamma_Basic_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING BASIC GAMMA DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING BASIC GAMMA DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// LOAD DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_TOTAL_LOAD_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Total load distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Total_Load_Distributions_Clean_Configuration",
									"eta", 
									"Total_load",
									null, 
									null, 
									null, 
									null,
									"", 
									"Newton", 
									true,
									legend,
									wingPlotFolderPath,
									"Total_Load_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}


					}
					else
						System.err.println("WARNING!! THE WING LOAD DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING LOAD DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// ADDITIONAL LOAD DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_ADDITIONAL_LOAD_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAdditionalLoadDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Additional load distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Additional_Load_Distributions_Clean_Configuration",
									"eta", 
									"Add. Load",
									null, 
									null, 
									null, 
									null,
									"", 
									"Newton", 
									true,
									legend,
									wingPlotFolderPath,
									"Additional_Load_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING ADDITIONAL LOAD DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING ADDITIONAL LOAD DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// BASIC LOAD DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_BASIC_LOAD_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getBasicLoadDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Basic load distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Basic_Load_Distributions_Clean_Configuration",
									"eta", 
									"Basic_load",
									null, 
									null, 
									null, 
									null,
									"", 
									"Newton", 
									false,
									legend,
									wingPlotFolderPath,
									"Basic_Load_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING BASIC LOAD DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING BASIC LOAD DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// MOMENT DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_MOMENT_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Moment distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Moment_Distributions_Clean_Configuration",
									"eta", 
									"M",
									null, 
									null, 
									null, 
									null,
									"", 
									"Nm", 
									true,
									legend,
									wingPlotFolderPath,
									"Moment_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING MOMENT DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING MOMENT DISTRIBUTION");
				}

				//-----------------------------------------------------------------------------------------------------------------------
				// DRAG DISTRIBUTION
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.WING).contains(AerodynamicAndStabilityPlotEnum.WING_DRAG_DISTRIBUTION)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().size(); i++){
							xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()));
							yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDragDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)).get(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i))
									));
							legend.add("Drag distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaForDistribution().get(i) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Drag_Distributions_Clean_Configuration",
									"eta", 
									"D",
									null, 
									null, 
									null, 
									null,
									"", 
									"N", 
									true,
									legend,
									wingPlotFolderPath,
									"Drag_Distributions_Clean_Configuration",
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					else
						System.err.println("WARNING!! THE WING DRAG DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE WING DRAG DISTRIBUTION");
				}
			}
		}
		//-----------------------------------------------------------------------------------------------------------------------
		// HORIZONTAL TAIL
		//-----------------------------------------------------------------------------------------------------------------------
		String legendStringCondition = "";

		if(_theAerodynamicBuilderInterface.getTheAircraft().getHTail() != null) {

			if (_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.TAKE_OFF || 
					_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.LANDING ) {
				legendStringCondition = "_Clean_Configuration";
			}

			if (_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.CRUISE || 
					_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.CLIMB ) {
				legendStringCondition = "";
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// LIFT CURVE
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_LIFT_CURVE_CLEAN)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();

					xVector = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertListOfAmountToDoubleArray(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean()
							));
					yVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.HORIZONTAL_TAIL)
							.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));

					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"alpha_h",
							"CL",
							"deg", 
							"",
							horizontalTailPlotFolderPath,
							"Lift_Coefficient_Curve" + legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);
				}
				else
					System.err.println("WARNING!! THE HORIZONTAL TAIL CLEAN LIFT CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE HORIZONTAL TAIL CLEAN LIFT CURVE");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// DRAG POLAR CURVE 
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_POLAR_CURVE_CLEAN)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();

					xVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getPolar3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.HORIZONTAL_TAIL)
							.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
							));
					yVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.HORIZONTAL_TAIL)
							.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));

					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"CD",
							"CL",
							"", 
							"",
							horizontalTailPlotFolderPath,
							"Polar_Curve" + legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);

				}
				else
					System.err.println("WARNING!! THE HORIZONTAL TAIL CLEAN DRAG POLAR CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE HORIZONTAL TAIL CLEAN DRAG POLAR CURVE");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// MOMENT CURVE
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_MOMENT_CURVE_CLEAN)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();

					xVector = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertListOfAmountToDoubleArray(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean()
							));
					yVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMoment3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.HORIZONTAL_TAIL)
							.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)));


					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"alpha_h",
							"CM",
							"deg", 
							"",
							horizontalTailPlotFolderPath,
							"Moment_Coefficient_Curve" + legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);
				}
				else
					System.err.println("WARNING!! THE HORIZONTAL TAIL CLEAN PITCHING MOMENT CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE HORIZONTAL TAIL CLEAN PITCHING MOMENT CURVE");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// STALL PATH
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_STALL_PATH)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_MAX)) {
					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()));
					xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()));


					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getClMaxDistribution()));
					yVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistributionAtCLMax().get(MethodEnum.NASA_BLACKWELL)));

					legend.add("Cl max airfoils");
					legend.add("Cl distribution at stall");


					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Stall_Path",
								"eta", 
								"Cl",
								null, 
								null, 
								null, 
								null,
								"", 
								"", 
								true,
								legend,
								horizontalTailPlotFolderPath,
								"Stall_Path"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
			}
			//-----------------------------------------------------------------------------------------------------------------------
			// LIFT COEFFICIENT DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_CL_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("Cl distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Lift_Coefficient_Distributions",
								"eta", 
								"Cl",
								null, 
								null, 
								null, 
								null,
								"", 
								"", 
								true,
								legend,
								horizontalTailPlotFolderPath,
								"Lift_Coefficient_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE HORIZONTAL TAIL CL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE HORIZONTAL TAIL CL DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// DRAG COEFFICIENT DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_CD_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("Cd distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Drag_Coefficient_Distributions",
								"eta", 
								"Cd",
								null, 
								null, 
								null, 
								null,
								"", 
								"", 
								true,
								legend,
								horizontalTailPlotFolderPath,
								"Drag_Coefficient_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}
				}
				else
					System.err.println("WARNING!! THE HORIZONTAL TAIL CD DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE HORIZONTAL TAIL CD DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// MOMENT COEFFICIENT DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_CM_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("Cm distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}


					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Moment_Coefficient_Distributions",
								"eta", 
								"Cm",
								null, 
								null, 
								null, 
								null,
								"", 
								"", 
								true,
								legend,
								horizontalTailPlotFolderPath,
								"Moment_Coefficient_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE HORIZONTAL TAIL CM DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE HORIZONTAL TAIL CM DISTRIBUTION");
			}
			//-----------------------------------------------------------------------------------------------------------------------
			// cCL DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_cCL_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCclDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("cCl distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}


					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"cCl_Distributions",
								"eta", 
								"cCl",
								null, 
								null, 
								null, 
								null,
								"", 
								"m", 
								true,
								legend,
								horizontalTailPlotFolderPath,
								"cCl_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}	
				else
					System.err.println("WARNING!! THE HORIZONTAL TAIL cCL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE HORIZONTAL TAIL cCL DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// GAMMA DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_GAMMA_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getGammaDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("Gamma distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Gamma_Distributions",
								"eta", 
								"gamma",
								null, 
								null, 
								null, 
								null,
								"", 
								"m^2/s", 
								true,
								legend,
								horizontalTailPlotFolderPath,
								"Gamma_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}


				}
				else
					System.err.println("WARNING!! THE HORIZONTAL TAIL GAMMA DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE HORIZONTAL TAIL GAMMA DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// LOAD DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_TOTAL_LOAD_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("Total load distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Total_load_Distributions",
								"eta", 
								"Total_load",
								null, 
								null, 
								null, 
								null,
								"", 
								"Newton", 
								true,
								legend,
								horizontalTailPlotFolderPath,
								"Total_load_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE HORIZONTAL TAIL LOAD DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE HORIZONTAL TAIL LOAD DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// MOMENT
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_MOMENT_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("Moment distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Moment_Distributions",
								"eta", 
								"M",
								null, 
								null, 
								null, 
								null,
								"", 
								"Nm", 
								true,
								legend,
								horizontalTailPlotFolderPath,
								"Moment_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE HORIZONTAL TAIL MOMENT DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE HORIZONTAL TAIL MOMENT DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// DRAG
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_DRAG_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDragDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("Drag distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Drag_Distributions",
								"eta", 
								"D",
								null, 
								null, 
								null, 
								null,
								"", 
								"N", 
								true,
								legend,
								horizontalTailPlotFolderPath,
								"Drag_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE HORIZONTAL TAIL DRAG DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE HORIZONTAL TAIL DRAG DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// LIFT CURVE WITH ELEVATOR
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_LIFT_CURVE_ELEVATOR)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
						|| _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {

					if(!(_theAerodynamicBuilderInterface.getDeltaElevatorList().size()==1) & !(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(0).doubleValue(NonSI.DEGREE_ANGLE)== 0.0)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						legend.add("Clean configuration");

						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean()
								));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));

						for ( int i=0; i<_theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++) {
							xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean()
									));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_current3DHorizontalTailLiftCurve.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))));
							legend.add("Configuration with elevator at " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Lift_Coefficient_Curve_Elevator",
									"alpha", 
									"CL",
									null, 
									null, 
									null, 
									null,
									"deg", 
									"", 
									true,
									legend,
									horizontalTailPlotFolderPath,
									"Lift_Coefficient_Curve_Elevator"+ legendStringCondition,
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					if(_theAerodynamicBuilderInterface.getDeltaElevatorList().isEmpty() &  _theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis()!= null) {

						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean()
								));
						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArray()
								));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)));

						legend.add("Clean configuration");
						legend.add("Configuration with elevator at " + _theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis().doubleValue(NonSI.DEGREE_ANGLE) + " deg");
					}
				}
				else
					System.err.println("WARNING!! THE HORIZONTAL TAIL LIFT CURVES (WITH AND WITHOUT ELEVATOR) HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE HORIZONTAL TAIL LIFT CURVE WITH ELEVATOR");
			}


			//-----------------------------------------------------------------------------------------------------------------------
			// DRAG POLAR CURVE WITH ELEVATOR
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_POLAR_CURVE_ELEVATOR)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
						|| _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)) {

					if(!(_theAerodynamicBuilderInterface.getDeltaElevatorList().size()==1) & !(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(0).doubleValue(NonSI.DEGREE_ANGLE)== 0.0)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						legend.add("Clean configuration");

						xVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getPolar3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
								));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));

						for ( int i=0; i<_theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++) {
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_current3DHorizontalTailLiftCurve.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))));
							xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_current3DHorizontalTailPolarCurve.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))));
							legend.add("Configuration with elevator at " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}


						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Polar_Curve_Elevator",
									"CD", 
									"CL",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									horizontalTailPlotFolderPath,
									"Polar_Curve_Elevator"+ legendStringCondition,
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					if(_theAerodynamicBuilderInterface.getDeltaElevatorList().isEmpty() &  _theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis()!= null) {

						xVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getPolar3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
								));
						xVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getPolar3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)
								));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)));

						legend.add("Clean configuration");
						legend.add("Configuration with elevator at " + _theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis().doubleValue(NonSI.DEGREE_ANGLE) + " deg");
					}
				}
				else
					System.err.println("WARNING!! THE HORIZONTAL TAIL DRAG POLAR CURVES (WITH AND WITHOUT ELEVATOR) HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE HORIZONTAL TAIL DRAG POLAR CURVE WITH ELEVATOR");
			}


			//-----------------------------------------------------------------------------------------------------------------------
			// MOMENT CURVE WITH ELEVATOR 
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.HORIZONTAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.HTAIL_MOMENT_CURVE_ELEVATOR)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
						|| _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)) {

					if(!(_theAerodynamicBuilderInterface.getDeltaElevatorList().size()==1) & !(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(0).doubleValue(NonSI.DEGREE_ANGLE)== 0.0)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						legend.add("Clean configuration");

						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean()
								));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMoment3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)));

						for ( int i=0; i<_theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++) {
							xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean()
									));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_current3DHorizontalTailMomentCurve.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))));
							legend.add("Configuration with elevator at " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg");
						}


						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Moment_Coefficient_Curve_Elevator",
									"alpha", 
									"CM",
									null, 
									null, 
									null, 
									null,
									"deg", 
									"", 
									true,
									legend,
									horizontalTailPlotFolderPath,
									"Moment_Coefficient_Curve_Elevator"+ legendStringCondition,
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}
						if(_theAerodynamicBuilderInterface.getDeltaElevatorList().isEmpty() &  _theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis()!= null) {

							xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean()
									));
							xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArray()
									));
							yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMoment3DCurve().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.HORIZONTAL_TAIL)
									.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)));
							yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentCoefficient3DCurveHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.HORIZONTAL_TAIL)
									.get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)));

							legend.add("Clean configuration");
							legend.add("Configuration with elevator at " + _theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis().doubleValue(NonSI.DEGREE_ANGLE) + " deg");
						}
					}
				}
				else
					System.err.println("WARNING!! THE HORIZONTAL TAIL PITCHING MOMENT CURVES (WITH AND WITHOUT ELEVATOR) HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE HORIZONTAL TAIL PITCHING MOMENT CURVE WITH ELEVATOR");
			}
		}
		//-----------------------------------------------------------------------------------------------------------------------
		// CANARD
		//-----------------------------------------------------------------------------------------------------------------------
		
		if(_theAerodynamicBuilderInterface.getTheAircraft().getCanard() != null) {

			if (_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.TAKE_OFF || 
					_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.LANDING ) {
				legendStringCondition = "_Clean_Configuration";
			}

			if (_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.CRUISE || 
					_theAerodynamicBuilderInterface.getCurrentCondition() == ConditionEnum.CLIMB ) {
				legendStringCondition = "";
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// LIFT CURVE
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_LIFT_CURVE_CLEAN)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();

					xVector = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertListOfAmountToDoubleArray(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaArrayClean()
							));
					yVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficient3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.CANARD)
							.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));

					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"alpha_h",
							"CL",
							"deg", 
							"",
							canardPlotFolderPath,
							"Lift_Coefficient_Curve" + legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);
				}
				else
					System.err.println("WARNING!! THE CANARD CLEAN LIFT CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CANARD CLEAN LIFT CURVE");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// DRAG POLAR CURVE 
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_POLAR_CURVE_CLEAN)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();

					xVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getPolar3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.CANARD)
							.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
							));
					yVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficient3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.CANARD)
							.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));

					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"CD",
							"CL",
							"", 
							"",
							canardPlotFolderPath,
							"Polar_Curve" + legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);

				}
				else
					System.err.println("WARNING!! THE CANARD CLEAN DRAG POLAR CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CANARD CLEAN DRAG POLAR CURVE");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// MOMENT CURVE
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_MOMENT_CURVE_CLEAN)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();

					xVector = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertListOfAmountToDoubleArray(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaArrayClean()
							));
					yVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getMoment3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.CANARD)
							.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)));


					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"alpha_h",
							"CM",
							"deg", 
							"",
							canardPlotFolderPath,
							"Moment_Coefficient_Curve" + legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);
				}
				else
					System.err.println("WARNING!! THE CANARD CLEAN PITCHING MOMENT CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CANARD CLEAN PITCHING MOMENT CURVE");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// STALL PATH
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_STALL_PATH)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.CL_MAX)) {
					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getEtaStationDistribution()));
					xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getEtaStationDistribution()));


					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getClMaxDistribution()));
					yVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficientDistributionAtCLMax().get(MethodEnum.NASA_BLACKWELL)));

					legend.add("Cl max airfoils");
					legend.add("Cl distribution at stall");


					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Stall_Path",
								"eta", 
								"Cl",
								null, 
								null, 
								null, 
								null,
								"", 
								"", 
								true,
								legend,
								canardPlotFolderPath,
								"Stall_Path"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
			}
			//-----------------------------------------------------------------------------------------------------------------------
			// LIFT COEFFICIENT DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_CL_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i))
								));
						legend.add("Cl distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Lift_Coefficient_Distributions",
								"eta", 
								"Cl",
								null, 
								null, 
								null, 
								null,
								"", 
								"", 
								true,
								legend,
								canardPlotFolderPath,
								"Lift_Coefficient_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE CANARD CL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CANARD CL DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// DRAG COEFFICIENT DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_CD_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i))
								));
						legend.add("Cd distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Drag_Coefficient_Distributions",
								"eta", 
								"Cd",
								null, 
								null, 
								null, 
								null,
								"", 
								"", 
								true,
								legend,
								canardPlotFolderPath,
								"Drag_Coefficient_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}
				}
				else
					System.err.println("WARNING!! THE CANARD CD DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CANARD CD DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// MOMENT COEFFICIENT DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_CM_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getMomentCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i))
								));
						legend.add("Cm distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}


					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Moment_Coefficient_Distributions",
								"eta", 
								"Cm",
								null, 
								null, 
								null, 
								null,
								"", 
								"", 
								true,
								legend,
								canardPlotFolderPath,
								"Moment_Coefficient_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE CANARD CM DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CANARD CM DISTRIBUTION");
			}
			//-----------------------------------------------------------------------------------------------------------------------
			// cCL DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_cCL_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCclDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i))
								));
						legend.add("cCl distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}


					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"cCl_Distributions",
								"eta", 
								"cCl",
								null, 
								null, 
								null, 
								null,
								"", 
								"m", 
								true,
								legend,
								canardPlotFolderPath,
								"cCl_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}	
				else
					System.err.println("WARNING!! THE CANARD cCL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CANARD cCL DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// GAMMA DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_GAMMA_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getGammaDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i))
								));
						legend.add("Gamma distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Gamma_Distributions",
								"eta", 
								"gamma",
								null, 
								null, 
								null, 
								null,
								"", 
								"m^2/s", 
								true,
								legend,
								canardPlotFolderPath,
								"Gamma_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}


				}
				else
					System.err.println("WARNING!! THE CANARD GAMMA DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CANARD GAMMA DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// LOAD DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_TOTAL_LOAD_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i))
								));
						legend.add("Total load distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Total_load_Distributions",
								"eta", 
								"Total_load",
								null, 
								null, 
								null, 
								null,
								"", 
								"Newton", 
								true,
								legend,
								canardPlotFolderPath,
								"Total_load_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE CANARD LOAD DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CANARD LOAD DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// MOMENT
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_MOMENT_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getMomentDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i))
								));
						legend.add("Moment distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Moment_Distributions",
								"eta", 
								"M",
								null, 
								null, 
								null, 
								null,
								"", 
								"Nm", 
								true,
								legend,
								canardPlotFolderPath,
								"Moment_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE CANARD MOMENT DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CANARD MOMENT DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// DRAG
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_DRAG_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDragDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i))
								));
						legend.add("Drag distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Drag_Distributions",
								"eta", 
								"D",
								null, 
								null, 
								null, 
								null,
								"", 
								"N", 
								true,
								legend,
								canardPlotFolderPath,
								"Drag_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE CANARD DRAG DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CANARD DRAG DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// LIFT CURVE WITH ELEVATOR
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_LIFT_CURVE_ELEVATOR)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
						|| _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {

					if(!(_theAerodynamicBuilderInterface.getDeltaElevatorList().size()==1) & !(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(0).doubleValue(NonSI.DEGREE_ANGLE)== 0.0)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						legend.add("Clean configuration");

						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaArrayClean()
								));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));

						for ( int i=0; i<_theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++) {
							xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaArrayClean()
									));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_current3DHorizontalTailLiftCurve.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))));
							legend.add("Configuration with elevator at " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Lift_Coefficient_Curve_Elevator",
									"alpha", 
									"CL",
									null, 
									null, 
									null, 
									null,
									"deg", 
									"", 
									true,
									legend,
									canardPlotFolderPath,
									"Lift_Coefficient_Curve_Elevator"+ legendStringCondition,
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					if(_theAerodynamicBuilderInterface.getDeltaElevatorList().isEmpty() &  _theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis()!= null) {

						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaArrayClean()
								));
						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaArray()
								));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficient3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)));

						legend.add("Clean configuration");
						legend.add("Configuration with elevator at " + _theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis().doubleValue(NonSI.DEGREE_ANGLE) + " deg");
					}
				}
				else
					System.err.println("WARNING!! THE CANARD LIFT CURVES (WITH AND WITHOUT ELEVATOR) HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CANARD LIFT CURVE WITH ELEVATOR");
			}


			//-----------------------------------------------------------------------------------------------------------------------
			// DRAG POLAR CURVE WITH ELEVATOR
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_POLAR_CURVE_ELEVATOR)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
						|| _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)) {

					if(!(_theAerodynamicBuilderInterface.getDeltaElevatorList().size()==1) & !(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(0).doubleValue(NonSI.DEGREE_ANGLE)== 0.0)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						legend.add("Clean configuration");

						xVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getPolar3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
								));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));

						for ( int i=0; i<_theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++) {
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_current3DHorizontalTailLiftCurve.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))));
							xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_current3DHorizontalTailPolarCurve.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))));
							legend.add("Configuration with elevator at " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg");
						}

						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}


						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Polar_Curve_Elevator",
									"CD", 
									"CL",
									null, 
									null, 
									null, 
									null,
									"", 
									"", 
									true,
									legend,
									canardPlotFolderPath,
									"Polar_Curve_Elevator"+ legendStringCondition,
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					if(_theAerodynamicBuilderInterface.getDeltaElevatorList().isEmpty() &  _theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis()!= null) {

						xVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getPolar3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
								));
						xVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getPolar3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)
								));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficient3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)));

						legend.add("Clean configuration");
						legend.add("Configuration with elevator at " + _theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis().doubleValue(NonSI.DEGREE_ANGLE) + " deg");
					}
				}
				else
					System.err.println("WARNING!! THE CANARD DRAG POLAR CURVES (WITH AND WITHOUT ELEVATOR) HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CANARD DRAG POLAR CURVE WITH ELEVATOR");
			}


			//-----------------------------------------------------------------------------------------------------------------------
			// MOMENT CURVE WITH ELEVATOR 
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.CANARD).contains(AerodynamicAndStabilityPlotEnum.CANARD_MOMENT_CURVE_ELEVATOR)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
						|| _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)) {

					if(!(_theAerodynamicBuilderInterface.getDeltaElevatorList().size()==1) & !(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(0).doubleValue(NonSI.DEGREE_ANGLE)== 0.0)) {

						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						legend.add("Clean configuration");

						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaArrayClean()
								));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getMoment3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.CANARD)
								.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)));

						for ( int i=0; i<_theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++) {
							xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaArrayClean()
									));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_current3DHorizontalTailMomentCurve.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))));
							legend.add("Configuration with elevator at " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg");
						}


						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Moment_Coefficient_Curve_Elevator",
									"alpha", 
									"CM",
									null, 
									null, 
									null, 
									null,
									"deg", 
									"", 
									true,
									legend,
									canardPlotFolderPath,
									"Moment_Coefficient_Curve_Elevator"+ legendStringCondition,
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}
						if(_theAerodynamicBuilderInterface.getDeltaElevatorList().isEmpty() &  _theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis()!= null) {

							xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaArrayClean()
									));
							xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaArray()
									));
							yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getMoment3DCurve().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.CANARD)
									.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)));
							yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getMomentCoefficient3DCurveHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.CANARD)
									.get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)));

							legend.add("Clean configuration");
							legend.add("Configuration with elevator at " + _theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis().doubleValue(NonSI.DEGREE_ANGLE) + " deg");
						}
					}
				}
				else
					System.err.println("WARNING!! THE CANARD PITCHING MOMENT CURVES (WITH AND WITHOUT ELEVATOR) HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CANARD PITCHING MOMENT CURVE WITH ELEVATOR");
			}
		}
		//-----------------------------------------------------------------------------------------------------------------------
		// VERTICAL TAIL
		//-----------------------------------------------------------------------------------------------------------------------

		if(_theAerodynamicBuilderInterface.getTheAircraft().getVTail() != null) {

			// LIFT CURVE
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.VERTICAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.VTAIL_LIFT_CURVE_CLEAN)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();

					xVector = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertListOfAmountToDoubleArray(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayClean()
							));
					yVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.VERTICAL_TAIL)
							.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));


					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"alpha",
							"CL",
							"deg", 
							"",
							verticalTailPlotFolderPath,
							"Lift_Coefficient_Curve" + legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);

				}
				else
					System.err.println("WARNING!! THE VERTICAL TAIL CLEAN LIFT CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE VERTICAL TAIL CLEAN LIFT CURVE");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// DRAG POLAR CURVE
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.VERTICAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.VTAIL_POLAR_CURVE_CLEAN_BREAKDOWN)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();

					xVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getPolar3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.VERTICAL_TAIL)
							.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
							));
					yVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.VERTICAL_TAIL)
							.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));


					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"CD",
							"CL",
							"", 
							"",
							verticalTailPlotFolderPath,
							"Polar_Curve" + legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);


				}
				else
					System.err.println("WARNING!! THE VERTICAL TAIL CLEAN DRAG POLAR CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE VERTICAL TAIL CLEAN DRAG POLAR CURVE");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// MOMENT CURVE
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.VERTICAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.VTAIL_MOMENT_CURVE_CLEAN)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();

					xVector = MyArrayUtils.convertDoubleArrayToListDouble(
							MyArrayUtils.convertListOfAmountToDoubleArray(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayClean()
									)
							);
					yVector = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMoment3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.VERTICAL_TAIL)
							.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)));


					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"alpha",
							"Cm",
							"deg", 
							"",
							verticalTailPlotFolderPath,
							"Moment Curve" + legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);

				}
				else
					System.err.println("WARNING!! THE VERTICAL TAIL CLEAN PITCHING MOMENT CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE VERTICAL TAIL CLEAN PITCHING MOMENT CURVE");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// LIFT COEFFICIENT DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.VERTICAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.VTAIL_CL_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.VERTICAL_TAIL)
								.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("Cl distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Lift_Coefficient_Distributions",
								"eta", 
								"Cl",
								null, 
								null, 
								null, 
								null,
								"", 
								"", 
								true,
								legend,
								verticalTailPlotFolderPath,
								"Lift_Coefficient_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE VERTICAL TAIL CL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE VERTICAL TAIL CL DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// DRAG COEFFICIENT DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.VERTICAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.VTAIL_CD_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.VERTICAL_TAIL)
								.get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("Cd distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Drag_Coefficient_Distributions",
								"eta", 
								"Cd",
								null, 
								null, 
								null, 
								null,
								"", 
								"", 
								true,
								legend,
								verticalTailPlotFolderPath,
								"Drag_Coefficient_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE VERTICAL TAIL CD DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE VERTICAL TAIL CD DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// MOMENT COEFFICIENT DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.VERTICAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.VTAIL_CM_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMomentCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.VERTICAL_TAIL)
								.get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("Cm distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Moment_Coefficient_Distributions",
								"eta", 
								"Cm",
								null, 
								null, 
								null, 
								null,
								"", 
								"", 
								true,
								legend,
								verticalTailPlotFolderPath,
								"Moment_Coefficient_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE VERTICAL TAIL CM DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE VERTICAL TAIL CM DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// cCL DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.VERTICAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.VTAIL_cCL_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCclDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.VERTICAL_TAIL)
								.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("cCl distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"cCl_Distributions",
								"eta", 
								"cCl",
								null, 
								null, 
								null, 
								null,
								"", 
								"m", 
								true,
								legend,
								verticalTailPlotFolderPath,
								"cCl_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}	
				else
					System.err.println("WARNING!! THE VERTICAL TAIL cCL DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE VERTICAL TAIL cCL DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// GAMMA DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.VERTICAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.VTAIL_GAMMA_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getGammaDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.VERTICAL_TAIL)
								.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("Gamma distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Gamma_Distributions",
								"eta", 
								"Gamma",
								null, 
								null, 
								null, 
								null,
								"", 
								"m^2/s", 
								true,
								legend,
								verticalTailPlotFolderPath,
								"Gamma_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE VERTICAL TAIL GAMMA DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE VERTICAL TAIL GAMMA DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// LOAD DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.VERTICAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.VTAIL_TOTAL_LOAD_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.VERTICAL_TAIL)
								.get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("Total load distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Total_load_Distributions",
								"eta", 
								"Total_Load",
								null, 
								null, 
								null, 
								null,
								"", 
								"Newton", 
								true,
								legend,
								verticalTailPlotFolderPath,
								"Total_load_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE VERTICAL TAIL LOAD DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE VERTICAL TAIL LOAD DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// DRAG DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.VERTICAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.VTAIL_DRAG_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getDragDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("Drag distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Drag_Distributions",
								"eta", 
								"D",
								null, 
								null, 
								null, 
								null,
								"", 
								"N", 
								true,
								legend,
								verticalTailPlotFolderPath,
								"Drag_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}


				}
				else
					System.err.println("WARNING!! THE VERTICAL TAIL DRAG DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE VERTICAL TAIL DRAG DISTRIBUTION");
			}


			//-----------------------------------------------------------------------------------------------------------------------
			// MOMENT DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.VERTICAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.VTAIL_MOMENT_DISTRIBUTION)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertFromDoubleToPrimitive(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution()));
						yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMomentDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)).get(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i))
								));
						legend.add("Moment distribution at alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaForDistribution().get(i) + " deg");
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Moment_Distributions",
								"eta", 
								"M",
								null, 
								null, 
								null, 
								null,
								"", 
								"Nm", 
								true,
								legend,
								verticalTailPlotFolderPath,
								"Moment_Distributions"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE VERTICAL TAIL MOMENT DISTRIBUTION HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE VERTICAL TAIL MOMENT DISTRIBUTION");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// LIFT CURVE WITH RUDDER
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.VERTICAL_TAIL).contains(AerodynamicAndStabilityPlotEnum.VTAIL_LIFT_CURVE_RUDDER)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
						|| _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					if(!(_theAerodynamicBuilderInterface.getDeltaRudderList().size()==1) & !(_theAerodynamicBuilderInterface.getDeltaRudderList().get(0).doubleValue(NonSI.DEGREE_ANGLE)== 0.0)) {


						xVectorMatrix = new ArrayList<Double[]>();
						yVectorMatrix = new ArrayList<Double[]>();
						legend  = new ArrayList<>(); 

						legend.add("Clean configuration");

						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayClean()
								));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.VERTICAL_TAIL)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));

						for ( int i=0; i<_theAerodynamicBuilderInterface.getDeltaRudderList().size(); i++) {
							xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayClean()
									));
							yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_current3DVerticalTailLiftCurve.get(_theAerodynamicBuilderInterface.getDeltaRudderList().get(i))));
							legend.add("Configuration with rudder at " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg");
						}


						xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
						legendString = new String[xVectorMatrix.size()];

						for(int i=0; i <xVectorMatrix.size(); i++){
							xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
							yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
							legendString [i] = legend.get(i);
						}

						try {
							MyChartToFileUtils.plot(
									xVectorMatrix,
									yVectorMatrix,
									"Lift_Coefficient_Curve_Rudder",
									"alpha",
									"CL", 
									null, 
									null, 
									null, 
									null,
									"deg", 
									"", 
									true,
									legend,
									verticalTailPlotFolderPath,
									"Lift_Coefficient_Curve_Rudder"+ legendStringCondition,
									_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
									);
						} catch (InstantiationException e) {

							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

					}
					if(_theAerodynamicBuilderInterface.getDeltaRudderList().isEmpty() &  _theAerodynamicBuilderInterface.getRudderDeflectionForAnalysis()!= null) {

						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayClean()
								));
						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArray()
								));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.VERTICAL_TAIL)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));
						yVectorMatrix.add(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.VERTICAL_TAIL)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)));

						legend.add("Clean configuration");
						legend.add("Configuration with elevator at " + _theAerodynamicBuilderInterface.getRudderDeflectionForAnalysis().doubleValue(NonSI.DEGREE_ANGLE) + " deg");
					}
				}
				else
					System.err.println("WARNING!! THE VERTICAL TAIL LIFT CURVES (WITH AND WITHOUT RUDDER) HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE VERTICAL TAIL LIFT CURVE WITH RUDDER");
			}

		}	
		//-----------------------------------------------------------------------------------------------------------------------
		// FUSELAGE
		if(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage() != null) {

			//-----------------------------------------------------------------------------------------------------------------------
			// DRAG POLAR
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.FUSELAGE).contains(AerodynamicAndStabilityPlotEnum.FUSELAGE_POLAR_CURVE)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE)) {

					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();

					xVector = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					yVector = MyArrayUtils.convertDoubleArrayToListDouble(_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getPolar3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE)
							));

					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"alpha_b", 
							"CD",
							"deg", 
							"",
							fuselagePlotFolderPath,
							"Polar_Curve"+ legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);
				}
				else
					System.err.println("WARNING!! THE FUSELAGE DRAG POLAR CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE FUSELAGE DRAG POLAR CURVE");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// MOMENT CURVE
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.FUSELAGE).contains(AerodynamicAndStabilityPlotEnum.FUSELAGE_MOMENT_CURVE)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE)) {

					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();

					xVector = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					yVector = MyArrayUtils.convertDoubleArrayToListDouble(_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getMoment3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE)
							));

					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"alpha_b", 
							"CM",
							"deg", 
							"",
							fuselagePlotFolderPath,
							"Moment_Curve"+ legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);
				}
				else
					System.err.println("WARNING!! THE FUSELAGE PITCHING MOMENT CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE FUSELAGE PITCHING MOMENT CURVE");
			}
		}

		//-----------------------------------------------------------------------------------------------------------------------
		// NACELLE
		if(_theAerodynamicBuilderInterface.getTheAircraft().getNacelles() != null) {

			//-----------------------------------------------------------------------------------------------------------------------
			// DRAG POLAR
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.NACELLE).contains(AerodynamicAndStabilityPlotEnum.NACELLE_POLAR_CURVE)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE)) {

					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();

					xVector = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					yVector = MyArrayUtils.convertDoubleArrayToListDouble(_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getPolar3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE)
							));

					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"alpha_b", 
							"CD",
							"deg", 
							"",
							nacellePlotFolderPath,
							"Polar_Curve"+ legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);
				}
				else
					System.err.println("WARNING!! THE NACELLE DRAG POLAR CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE NACELLE DRAG POLAR CURVE");
			}

			//-----------------------------------------------------------------------------------------------------------------------
			// MOMENT CURVE
			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.NACELLE).contains(AerodynamicAndStabilityPlotEnum.NACELLE_MOMENT_CURVE)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE)) {

					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();

					xVector = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					yVector = MyArrayUtils.convertDoubleArrayToListDouble(_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getMoment3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE)
							));

					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"alpha_b", 
							"CM",
							"deg", 
							"",
							nacellePlotFolderPath,
							"Moment_Curve"+ legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);
				}
				else
					System.err.println("WARNING!! THE NACELLE PITCHING MOMENT CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE NACELLE PITCHING MOMENT CURVE");
			}
		}
		//-----------------------------------------------------------------------------------------------------------------------
		// AIRCRAFT
		//-----------------------------------------------------------------------------------------------------------------------
		// WING DOWNWASH
		if(_theAerodynamicBuilderInterface.getTheAircraft().getHTail() != null) {
			if(_theAerodynamicBuilderInterface.getDownwashConstant()) {
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.DOWNWASH_ANGLE)) {
					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();


					xVector = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					yVector = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertListOfAmountToDoubleArray(_downwashAngleMap.get(ComponentEnum.WING).get(Boolean.TRUE).get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH))));

					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"alpha body", 
							"epsilon",
							"deg", 
							"deg", 
							aircraftPlotFolderPath,
							"Downwash_Angle",
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);
				}

				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.DOWNWASH_GRADIENT)) {

					xVector = new ArrayList<Double>();
					yVector = new ArrayList<Double>();


					xVector = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					yVector = _downwashGradientMap.get(ComponentEnum.WING).get(Boolean.TRUE).get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH));

					MyChartToFileUtils.plotNoLegend(
							MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(xVector)),
							MyArrayUtils.convertToDoublePrimitive(yVector),
							null, 
							null, 
							null, 
							null, 
							"alpha body",  
							"d_epsilon/d_alpha",
							"deg", 
							"", 
							aircraftPlotFolderPath,
							"Downwash_Gradient",
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);
				}

			}

			if(!_theAerodynamicBuilderInterface.getDownwashConstant()) {
				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.DOWNWASH_ANGLE)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_downwashAngleMap.get(ComponentEnum.WING).get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH))
							.get(Boolean.TRUE)
							));
					yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_downwashAngleMap.get(ComponentEnum.WING).get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH))
							.get(Boolean.FALSE)
							));
					legend.add("Constant downwash gradient");
					legend.add("Linear downwash gradient");


					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}
					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Downwash_Angle",
								"alpha body",
								"Epsilon", 
								null, 
								null, 
								null, 
								null,
								"deg", 
								"deg", 
								true,
								legend,
								aircraftPlotFolderPath,
								"Downwash_Angle"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {
						
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						
						e.printStackTrace();
					}

				}

				if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.DOWNWASH_GRADIENT)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_downwashGradientMap.get(ComponentEnum.WING).get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
							).get(Boolean.TRUE)));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_downwashGradientMap.get(ComponentEnum.WING).get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
							).get(Boolean.FALSE)));
					legend.add("Constant downwash gradient");
					legend.add("Linear downwash gradient");


					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Downwash_Gradient",
								"alpha body",
								"d_epsilon/d_alpha", 
								null, 
								null, 
								null, 
								null,
								"deg", 
								"deg", 
								true,
								legend,
								aircraftPlotFolderPath,
								"Downwash_Gradient"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {
						
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						
						e.printStackTrace();
					}
				}
			}
		}
		else
			System.err.println("WARNING!! IMPOSSIBLE TO PLOT DOWNWASH ANGLE AND DOWNWASH GRADIENT. THERE IS NO HORIZONTAL TAIL ...");

		//-----------------------------------------------------------------------------------------------------------------------
		// CL TOTAL 
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.TOTAL_LIFT_CURVE)) {

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CL_TOTAL)) {

				xVectorMatrix = new ArrayList<Double[]>();
				yVectorMatrix = new ArrayList<Double[]>();
				legend  = new ArrayList<>(); 

				for(int i=0; i<_theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++){
					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_totalLiftCoefficient.get(
									_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))));
					legend.add("delta e = " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i));
				}

				xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				legendString = new String[xVectorMatrix.size()];

				for(int i=0; i <xVectorMatrix.size(); i++){
					xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
					yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
					legendString [i] = legend.get(i);
				}

				try {
					MyChartToFileUtils.plot(
							xVectorMatrix,
							yVectorMatrix,
							"Total_Lift_Coefficient",
							"alpha body",
							"CL", 
							null, 
							null, 
							null, 
							null,
							"deg", 
							"", 
							true,
							legend,
							aircraftPlotFolderPath,
							"Total_Lift_Coefficient"+ legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);
				} catch (InstantiationException e) {
					
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					
					e.printStackTrace();
				}

			}
			else
				System.err.println("WARNING!! THE TOTAL LIFT CURVE, AT ALL THE ASSIGNED DELTA ELEVATOR DEFLECTIONS, HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE TOTAL LIFT CURVES");
		}

		//-----------------------------------------------------------------------------------------------------------------------
		// CM BREAKDOWN
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.TOTAL_CM_BREAKDOWN)) {

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL)) {

				for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){
					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_momentCoefficientBreakDown.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(ComponentEnum.WING)));
					legend.add("Wing");

					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_momentCoefficientBreakDown.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(ComponentEnum.HORIZONTAL_TAIL)));
					legend.add("Horizontal_Tail");

					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_momentCoefficientBreakDown.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(ComponentEnum.FUSELAGE)));
					legend.add("Fuselage");

					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_momentCoefficientBreakDown.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(ComponentEnum.NACELLE)));
					legend.add("Nacelles");
					
					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_momentCoefficientBreakDown.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(ComponentEnum.LANDING_GEAR)));
					legend.add("Landing_Gear");

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int ii=0; ii <xVectorMatrix.size(); ii++){
						xMatrix[ii] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(ii));
						yMatrix[ii] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(ii));
						legendString [ii] = legend.get(ii);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Total_Moment_Coefficient_Breakdown_at_CG",
								"alpha body",
								"CM", 
								null, 
								null, 
								null, 
								null,
								"deg", 
								"", 
								true,
								legend,
								aircraftPlotFolderPath,
								"Total_Moment_Coefficient_Breakdown_at_CG"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {
						
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						
						e.printStackTrace();
					}

				}
			}
			else
				System.err.println("WARNING!! THE PITCHING MOMENT CURVES OF EACH COMPONENT HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CM BREAKDOWN");

		}

		//-----------------------------------------------------------------------------------------------------------------------
		// CD TOTAL 
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.TOTAL_POLAR_CURVE)) {

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CD_TOTAL)) {

				xVectorMatrix = new ArrayList<Double[]>();
				yVectorMatrix = new ArrayList<Double[]>();
				legend  = new ArrayList<>(); 

				for(int i=0; i<_theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++){
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_totalLiftCoefficient.get(
									_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))));
					xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_totalDragCoefficient.get(
									_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))));
					legend.add("delta e = " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i));
				}

				xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				legendString = new String[xVectorMatrix.size()];

				for(int i=0; i <xVectorMatrix.size(); i++){
					xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
					yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
					legendString [i] = legend.get(i);
				}

				try {
					MyChartToFileUtils.plot(
							xVectorMatrix,
							yVectorMatrix,
							"Total_Drag_Polar",
							"CD",
							"CL", 
							null, 
							null, 
							null, 
							null,
							"", 
							"", 
							true,
							legend,
							aircraftPlotFolderPath,
							"Total_Drag_Polar"+ legendStringCondition,
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
							);
				} catch (InstantiationException e) {
					
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					
					e.printStackTrace();
				}
			
			}
			else
				System.err.println("WARNING!! THE TOTAL DRAG POLAR CURVE, AT ALL THE ASSIGNED DELTA ELEVATOR DEFLECTIONS, HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE TOTAL DRAG POLAR CURVES");

		}
			
		//-----------------------------------------------------------------------------------------------------------------------
		// CM TOTAL (vs alpha)
		for (int j = 0; j<_theAerodynamicBuilderInterface.getXCGAircraft().size(); j++){

			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.TOTAL_MOMENT_CURVE_VS_ALPHA)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
								_totalMomentCoefficient.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(j)).get(
										_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))));
						legend.add("delta e = " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i));
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Total_Moment_Coefficient_with_respect_to_alpha_at_CG",
								"alpha body",
								"CM", 
								null, 
								null, 
								null, 
								null,
								"deg", 
								"", 
								true,
								legend,
								aircraftPlotFolderPath,
								"Total_Moment_Coefficient_with_respect_to_alpha_at_CG"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {
						
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						
						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE TOTAL PITCHING MOMENT CURVE AT XCG/C = " + 
							_theAerodynamicBuilderInterface.getXCGAircraft().get(j)
							+ ", AT ALL THE ASSIGNED DELTA ELEVATOR DEFLECTIONS,  HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE TOTAL PITCHING MOMENT CURVES (ALPHA)");

			}
		}

		//-----------------------------------------------------------------------------------------------------------------------
		// CM TOTAL (vs CL)
		for (int j = 0; j<_theAerodynamicBuilderInterface.getXCGAircraft().size(); j++){

			if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.TOTAL_MOMENT_CURVE_VS_CL)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++){
						xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
								_totalLiftCoefficient.get(
										_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
								_totalMomentCoefficient.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(j)).get(
										_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))));
						legend.add("delta e = " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i));
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix,
								yVectorMatrix,
								"Total_Moment_Coefficient_with_respect_to_CL_at_CG",
								"CL",
								"CM", 
								null, 
								null, 
								null, 
								null,
								"", 
								"", 
								true,
								legend,
								aircraftPlotFolderPath,
								"Total_Moment_Coefficient_with_respect_to_CL_at_CG"+ legendStringCondition,
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability() 
								);
					} catch (InstantiationException e) {
						
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						
						e.printStackTrace();
					}

				}
				else
					System.err.println("WARNING!! THE TOTAL PITCHING MOMENT CURVE AT XCG/C = " + 
							_theAerodynamicBuilderInterface.getXCGAircraft().get(j)
							+ ", AT ALL THE ASSIGNED DELTA ELEVATOR DEFLECTIONS,  HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE TOTAL PITCHING MOMENT CURVES (CL)");

			}
		}

		//----------------INDEX OF MAX ELEVATOR
		
		List<Integer> indexOfFirstMaximumDeltaElevatorOfEquilibrium = new ArrayList<>();
		
		for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){
			indexOfFirstMaximumDeltaElevatorOfEquilibrium.add(i, _deltaEEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).size());
			for(int j=0; j<_deltaEEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).size(); j++)
				if(_deltaEEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(j)
						.equals(_deltaEForEquilibrium.get(0))
						) {
					indexOfFirstMaximumDeltaElevatorOfEquilibrium.set(i, j+1);
					break;
				}
		}
		//-----------------------------------------------------------------------------------------------------------------------
		// CL TOTAL EQUILIBRIUM
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.TRIMMED_LIFT_CURVE)) {			try {	
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {

				xVectorMatrix = new ArrayList<Double[]>();
				yVectorMatrix = new ArrayList<Double[]>();
				legend  = new ArrayList<>(); 

				for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){

					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList.subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_totalEquilibriumLiftCoefficient.get(
									_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
									).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
							));
					legend.add("Xcg = " + 
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i));
				}

				xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				legendString = new String[xVectorMatrix.size()];

				for(int i=0; i <xVectorMatrix.size(); i++){
					xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
					yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
					legendString [i] = legend.get(i);
				}

				try {
					
					MyChartToFileUtils.plot(
							xVectorMatrix, 
							yVectorMatrix, 
							"Total Equilibrium Lift Coefficient", 
							"alpha body",  
							"CL_e", 
							null, 
							null, 
							null, 
							null, 
							"deg",
							"", 
							true,
							legend,
							aircraftPlotFolderPath,
							"Total_Equilibrium_Lift_Coefficient", 
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability()
							);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			else
				System.err.println("WARNING!! THE TRIMMED LIFT CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE TRIMMED LIFT CURVE");
		}
		
		catch (NullPointerException e) {
			System.err.println("WARNING: (PLOT AIRCRAFT LIFT CURVE) MISSING VALUES ...");
		}
		}

		//-----------------------------------------------------------------------------------------------------------------------
		// CD TOTAL EQUILIBRIUM

		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.TRIMMED_POLAR_CURVE)) {

			try {
				
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {

				xVectorMatrix = new ArrayList<Double[]>();
				yVectorMatrix = new ArrayList<Double[]>();
				legend  = new ArrayList<>(); 

				for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){

					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_totalEquilibriumLiftCoefficient.get(
									_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
									).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
							));
					xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_totalEquilibriumDragCoefficient.get(
									_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
									).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
							));
					legend.add("Xcg = " + 
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i));
				}

				xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				legendString = new String[xVectorMatrix.size()];

				for(int i=0; i <xVectorMatrix.size(); i++){
					xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
					yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
					legendString [i] = legend.get(i);
				}

				try {
					MyChartToFileUtils.plot(
							xVectorMatrix, 
							yVectorMatrix, 
							"Total Equilibrium Polar Curve", 
							"CL_e", 
							"CD_e",  
							null, 
							null, 
							null, 
							null, 
							"",
							"", 
							true,
							legend,
							aircraftPlotFolderPath,
							"Total_Equilibrium_Polar_Curve", 
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability()
							);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			else
				System.err.println("WARNING!! THE TRIMMED DRAG POLAR CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE TRIMMED DRAG POLAR CURVE");
		}

		catch (NoSuchElementException e) {
			System.err.println("WARNING: (PLOT WING POLAR CURVE) MISSING VALUES ...");
		}
		}

		//-----------------------------------------------------------------------------------------------------------------------
		// CL HTAIL EQUILIBRIUM
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.TRIMMED_LIFT_CURVE_HTAIL)) {

			try {	
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {

				xVectorMatrix = new ArrayList<Double[]>();
				yVectorMatrix = new ArrayList<Double[]>();
				legend  = new ArrayList<>(); 

				for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){

					
					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList.subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_horizontalTailEquilibriumLiftCoefficient.get(
									_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
									).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
							));
					legend.add("Xcg = " + 
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i));
				}

				xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				legendString = new String[xVectorMatrix.size()];

				for(int i=0; i <xVectorMatrix.size(); i++){
					xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
					yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
					legendString [i] = legend.get(i);
				}

				try {
					MyChartToFileUtils.plot(
							xVectorMatrix, 
							yVectorMatrix, 
							"Horizontal Tail Equilibrium Lift Coefficient", 
							"alpha body", 
							"CLh_e",
							null, 
							null, 
							null, 
							null, 
							"deg",
							"", 
							true,
							legend,
							aircraftPlotFolderPath,
							"Htail_Equilibrium_Lift_Coefficient", 
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability()
							);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			else
				System.err.println("WARNING!! THE TRIMMED HORIZONTAL TAIL LIFT CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE TRIMMED HORIZONTAL TAIL LIFT CURVE");
		
			}
			catch (NullPointerException e) {
				System.err.println("WARNING: (PLOT HTAIL EQUILIBRIUM LIFT CURVE) MISSING VALUES ...");
			}
			}

		//-----------------------------------------------------------------------------------------------------------------------
		// CD HTAIL EQUILIBRIUM
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.TRIMMED_POLAR_CURVE_HTAIL)) {

			try {
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {

				xVectorMatrix = new ArrayList<Double[]>();
				yVectorMatrix = new ArrayList<Double[]>();
				legend  = new ArrayList<>(); 

				for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){
					
					
					xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_horizontalTailEquilibriumLiftCoefficient.get(
									_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
									).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
							));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_horizontalTailEquilibriumDragCoefficient.get(
									_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
									).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
							));
					legend.add("Xcg = " + 
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i));
				}

				xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				legendString = new String[xVectorMatrix.size()];

				for(int i=0; i <xVectorMatrix.size(); i++){
					xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
					yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
					legendString [i] = legend.get(i);
				}

				try {
					MyChartToFileUtils.plot(
							xVectorMatrix, 
							yVectorMatrix, 
							"Horizontal Tail Equilibrium Drag Coefficient", 
							"CLh_e", 
							"CDh_e",
							null, 
							null, 
							null, 
							null, 
							"",
							"", 
							true,
							legend,
							aircraftPlotFolderPath,
							"Htail_Equilibrium_Drag_Coefficient", 
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability()
							);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
				
			}
			else
				System.err.println("WARNING!! THE TRIMMED HORIZONTAL TAIL DRAG POLAR CURVE HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE TRIMMED HORIZONTAL TAIL DRAG POLAR CURVE");
			}
			catch (NoSuchElementException e) {
				System.err.println("WARNING: (PLOT HTAIL EQUILIBRIUM POLAR CURVE) MISSING VALUES ...");
			}
			}
		
		//-----------------------------------------------------------------------------------------------------------------------
		// TOTAL TRIMMED EFFICIENCY CURVES VS ALPHA
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.TRIMMED_EFFICIENCY_CURVE_VS_ALPHA)) {

			try {
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {

				xVectorMatrix = new ArrayList<Double[]>();
				yVectorMatrix = new ArrayList<Double[]>();
				legend  = new ArrayList<>(); 

				for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){

					
					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList.subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_totalEquilibriumEfficiencyMap.get(
									_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
									).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
							));
					legend.add("Xcg = " + 
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i));
				}

				xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				legendString = new String[xVectorMatrix.size()];

				for(int i=0; i <xVectorMatrix.size(); i++){
					xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
					yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
					legendString [i] = legend.get(i);
				}

				try {
					MyChartToFileUtils.plot(
							xVectorMatrix, 
							yVectorMatrix, 
							"Total Equilibrium Efficiency vs Alpha Body", 
							"alpha body", 
							"Efficiency",
							null, 
							null, 
							null, 
							null, 
							"deg",
							"", 
							true,
							legend,
							aircraftPlotFolderPath,
							"Total_Equilibrium_Efficiency_vs_Alpha_Body", 
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability()
							);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			else
				System.err.println("WARNING!! THE TOTAL TRIMMED AIRCRAFT EFFICIENCY CURVES HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE TOTAL TRIMMED AIRCRAFT EFFICIENCY CURVES VS ALPHA");
			}
			catch (NullPointerException e) {
				System.err.println("WARNING: (PLOT EFFICIENCY CURVE) MISSING VALUES ...");
			}		
			}
		
		//-----------------------------------------------------------------------------------------------------------------------
		// TOTAL TRIMMED EFFICIENCY CURVES VS CLe
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.TRIMMED_EFFICIENCY_CURVE_VS_ALPHA)) {

			try {
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {

				xVectorMatrix = new ArrayList<Double[]>();
				yVectorMatrix = new ArrayList<Double[]>();
				legend  = new ArrayList<>(); 

				for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){
					
					xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_totalEquilibriumLiftCoefficient.get(
									_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
									).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
							));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_totalEquilibriumEfficiencyMap.get(
									_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
									).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
							));
					legend.add("Xcg = " + 
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i));
				}

				xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				legendString = new String[xVectorMatrix.size()];

				for(int i=0; i <xVectorMatrix.size(); i++){
					xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
					yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
					legendString [i] = legend.get(i);
				}

				try {
					MyChartToFileUtils.plot(
							xVectorMatrix, 
							yVectorMatrix, 
							"Total Equilibrium Efficiency vs CLe", 
							"Cl", 
							"Efficiency",
							null, 
							null, 
							null, 
							null, 
							"",
							"", 
							true,
							legend,
							aircraftPlotFolderPath,
							"Total_Equilibrium_Efficiency_vs_CLe", 
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability()
							);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			else
				System.err.println("WARNING!! THE TOTAL TRIMMED AIRCRAFT EFFICIENCY CURVES HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE TOTAL TRIMMED AIRCRAFT EFFICIENCY CURVES VS CLe");
			}
			catch (NoSuchElementException e) {
				System.err.println("WARNING: (PLOT EFFICIENCY VS CL CURVE) MISSING VALUES ...");
			}
			}
		
		//-----------------------------------------------------------------------------------------------------------------------
		// DELTA ELEVATOR EQUILIBRIUM
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.DELTA_ELEVATOR_EQUILIBRIUM)) {

			try {
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {

				xVectorMatrix = new ArrayList<Double[]>();
				yVectorMatrix = new ArrayList<Double[]>();
				legend  = new ArrayList<>(); 

				for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){

					xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_totalEquilibriumLiftCoefficient.get(
									_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
									).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
							));
					yVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(
							_deltaEEquilibrium.get(
									_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
									).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
							));
					legend.add("Xcg = " + 
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i));
				}

				xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				legendString = new String[xVectorMatrix.size()];

				for(int i=0; i <xVectorMatrix.size(); i++){
					xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
					yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
					legendString [i] = legend.get(i);
				}

				try {
					MyChartToFileUtils.plot(
							xVectorMatrix, 
							yVectorMatrix, 
							"Delta Elevator Equilibrium", 
							"CL_e", 
							"delta_e_eq",
							null, 
							null, 
							null, 
							null, 
							"",
							"deg", 
							true,
							legend,
							aircraftPlotFolderPath,
							"Delta_Elevator_Equilibrium", 
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability()
							);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			else
				System.err.println("WARNING!! THE EQUILIBRIUM DELTA ELEVATOR ARRAY HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE EQULIBRIUM DELTA ELEVATOR CURVE");
			}
			catch (NullPointerException e) {
				System.err.println("WARNING: (PLOT DELTA E EQUILIBRIUM CURVE) MISSING VALUES ...");
			}
			}
		
		//-----------------------------------------------------------------------------------------------------------------------
		// NEUTRAL POINT VS ALPHA
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.NEUTRAL_POINT_VS_ALPHA)) {

			try {
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {

				xVectorMatrix = new ArrayList<Double[]>();
				yVectorMatrix = new ArrayList<Double[]>();
				legend  = new ArrayList<>(); 

				for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){
					
					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList.subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_neutralPointPositionMap.get(
									_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
									).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
							));
					legend.add("Xcg = " + 
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i));
				}

				xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				legendString = new String[xVectorMatrix.size()];

				for(int i=0; i <xVectorMatrix.size(); i++){
					xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
					yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
					legendString [i] = legend.get(i);
				}

				try {
					MyChartToFileUtils.plot(
							xVectorMatrix, 
							yVectorMatrix, 
							"Neutral Point vs Alpha", 
							"alpha body", 
							"Neutral Point",
							null, 
							null, 
							null, 
							null, 
							"deg",
							"", 
							true,
							legend,
							aircraftPlotFolderPath,
							"Neutral_Point_vs_Alpha", 
							_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability()
							);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			else
				System.err.println("WARNING!! THE NEUTRAL POINT ARRAY HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE NEUTRAL POINT CURVE VS ALPHA");
			}
			catch (NullPointerException e) {
				System.err.println("WARNING: (PLOT NEUTRAL POINT CURVE) MISSING VALUES ...");
			}
			}
		
		//-----------------------------------------------------------------------------------------------------------------------
		// NEUTRAL POINT VS CLe
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.NEUTRAL_POINT_VS_CLe)) {

			try {
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){

						xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
								_totalEquilibriumLiftCoefficient.get(
										_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
										).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
								_neutralPointPositionMap.get(
										_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
										).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
								));
						legend.add("Xcg = " + 
								_theAerodynamicBuilderInterface.getXCGAircraft().get(i));
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix, 
								yVectorMatrix, 
								"Neutral Point vs CLe", 
								"CLe", 
								"Neutral Point",
								null, 
								null, 
								null, 
								null, 
								"",
								"", 
								true,
								legend,
								aircraftPlotFolderPath,
								"Neutral_Point_vs_CLe", 
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability()
								);
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				else
					System.err.println("WARNING!! THE NEUTRAL POINT ARRAY HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE NEUTRAL POINT CURVE VS CLe");
			}
			catch (NoSuchElementException e) {
				System.err.println("WARNING: (PLOT NEUTRAL POINT VS CL CURVE) MISSING VALUES ...");
			}
		}
		
		//-----------------------------------------------------------------------------------------------------------------------
		// STATIC STABILITY MARGIN VS ALPHA
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.STATIC_STABILITY_MARGIN_VS_ALPHA)) {

			try {
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){

						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaBodyList.subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
								_staticStabilityMarginMap.get(
										_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
										).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
								));
						legend.add("Xcg = " + 
								_theAerodynamicBuilderInterface.getXCGAircraft().get(i));
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix, 
								yVectorMatrix, 
								"Static Stability Margin vs Alpha", 
								"alpha body", 
								"Static Stability Margin",
								null, 
								null, 
								null, 
								null, 
								"deg",
								"", 
								true,
								legend,
								aircraftPlotFolderPath,
								"Static_Stability_Margin_vs_Alpha", 
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability()
								);
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				else
					System.err.println("WARNING!! THE STATIC STABILITY MARGIN ARRAY HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE STATIC STABILITY MARGIN CURVE VS ALPHA");
			}
			catch (NullPointerException e) {
				System.err.println("WARNING: (PLOT STATIC STABILITY MARGIN CURVE) MISSING VALUES ...");
			}
		}
		
		//-----------------------------------------------------------------------------------------------------------------------
		// STATIC STABILITY MARGIN VS CLe
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.STATIC_STABILITY_MARGIN_VS_CLe)) {

			try {
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {

					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){


						xVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
								_totalEquilibriumLiftCoefficient.get(
										_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
										).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
								_staticStabilityMarginMap.get(
										_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
										).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium.get(i))
								));
						legend.add("Xcg = " + 
								_theAerodynamicBuilderInterface.getXCGAircraft().get(i));
					}

					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int i=0; i <xVectorMatrix.size(); i++){
						xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
						yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
						legendString [i] = legend.get(i);
					}

					try {
						MyChartToFileUtils.plot(
								xVectorMatrix, 
								yVectorMatrix, 
								"Static Stability Margin vs CLe", 
								"CLe", 
								"Static Stability Margin",
								null, 
								null, 
								null, 
								null, 
								"",
								"", 
								true,
								legend,
								aircraftPlotFolderPath,
								"Static_Stability_Margin_vs_CLe", 
								_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVAerodynamicAndStability()
								);
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				else
					System.err.println("WARNING!! THE STATIC STABILITY MARGIN ARRAY HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE STATIC STABILITY MARGIN CURVE VS CLe");
			}
			catch (NoSuchElementException e) {
				System.err.println("WARNING: (PLOT STATIC STABILITY MARGIN VS CL CURVE) MISSING VALUES ...");
			}
		}
		
		//-----------------------------------------------------------------------------------------------------------------------
		// CN BREAKDOWN
		
		// TODO: check the type of method used, e.g. VEDESC or NAPOLITANO_DATCOM
		//       and use arrays accordingly

		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.TOTAL_CN_BREAKDOWN)) {

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY)) {

				for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){
					xVectorMatrix = new ArrayList<Double[]>();
					yVectorMatrix = new ArrayList<Double[]>();
					legend  = new ArrayList<>(); 

					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_betaList));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_cNVertical
							.get(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY))
							.get(i)._2()));
					legend.add("Vertical Tail");

					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_betaList));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_cNFuselage
							.get(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY))
							.get(i)._2()));
					legend.add("Fuselage");

					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_betaList));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_cNWing
							.get(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY))
							.get(i)._2()));
					legend.add("Wing");

					xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_betaList));
					yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
							_cNTotal
							.get(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY))
							.get(i)._2()));
					legend.add("Total Aircraft");
					
					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int ii=0; ii <xVectorMatrix.size(); ii++){
						xMatrix[ii] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(ii));
						yMatrix[ii] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(ii));
						legendString [ii] = legend.get(ii);
					}

					MyChartToFileUtils.plotNOCSV(
							xMatrix,
							yMatrix, 
							null, 
							null, 
							null, 
							null,
							"beta", 
							"CN",
							"deg", 
							"", 
							legendString, 
							aircraftPlotFolderPath,
							"Total_Yawing_Coefficient_Breakdown_at_CG" + _theAerodynamicBuilderInterface.getXCGAircraft().get(i));
				}
			}
			else
				System.err.println("WARNING!! THE CN CURVES FOR EACH COMPONENT HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CN BREAKDOWN");
		}
		
		//-----------------------------------------------------------------------------------------------------------------------
		// CN DELTA RUDDER EFFECT
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.TOTAL_CN_VS_BETA_VS_DELTA_RUDDER)) {

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY)) {
				for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){

				xVectorMatrix = new ArrayList<Double[]>();
				yVectorMatrix = new ArrayList<Double[]>();
				legend  = new ArrayList<>(); 

					for(int j=0; j<_theAerodynamicBuilderInterface.getDeltaRudderList().size(); j++){
						xVectorMatrix.add(MyArrayUtils.convertListOfAmountToDoubleArray(_betaList));
						yVectorMatrix.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
								_cNDueToDeltaRudder
								.get(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY))
								.get(_theAerodynamicBuilderInterface.getDeltaRudderList().get(j))
								.get(i)
								._2()));
						legend.add("delta_r = " + 
								_theAerodynamicBuilderInterface.getDeltaRudderList().get(j));
					}
 
					xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
					legendString = new String[xVectorMatrix.size()];

					for(int k=0; k <xVectorMatrix.size(); k++){
						xMatrix[k] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(k));
						yMatrix[k] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(k));
						legendString [k] = legend.get(k);
					}

					MyChartToFileUtils.plotNOCSV(
							xMatrix,
							yMatrix, 
							null, 
							null, 
							null, 
							null,
							"beta",  
							"CN_total",
							"deg", 
							"", 
							legendString, 
							aircraftPlotFolderPath,
							"Total_Yawing_Coefficient_with respect_to_beta_at_CG" + _theAerodynamicBuilderInterface.getXCGAircraft().get(i));
				}
			}
			else
				System.err.println("WARNING!! THE CN CURVES FOR EACH CHOSEN DELTA RUDDER HAVE NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE CN CURVES");
		}
		
		//-----------------------------------------------------------------------------------------------------------------------
		// BETA - CN EQUILIBRIUM
		if(_theAerodynamicBuilderInterface.getPlotList().get(ComponentEnum.AIRCRAFT).contains(AerodynamicAndStabilityPlotEnum.DELTA_RUDDER_EQUILIBRIUM)) {

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY)) {

				xVectorMatrix = new ArrayList<Double[]>();
				yVectorMatrix = new ArrayList<Double[]>();
				legend  = new ArrayList<>(); 

				for(int i=0; i<_theAerodynamicBuilderInterface.getXCGAircraft().size(); i++){
					
					MyInterpolatingFunction betaEquilibriumFunction = new MyInterpolatingFunction();
					betaEquilibriumFunction.interpolate(
							MyArrayUtils.convertToDoublePrimitive(
									_betaOfEquilibrium
									.get(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY))
									.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i))
									.stream()
									.map(tpl -> tpl._2.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList())
									),
							MyArrayUtils.convertToDoublePrimitive(
									_betaOfEquilibrium
									.get(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY))
									.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i))
									.stream()
									.map(tpl -> tpl._1.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList())
									)
							);
					Double[] deltaRudderEquilibriumFitted = MyArrayUtils.linspaceDouble(
							MyArrayUtils.convertToDoublePrimitive(
									_betaOfEquilibrium
									.get(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY))
									.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i))
									.stream()
									.map(tpl -> tpl._2.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList())
									)[0], 
							MyArrayUtils.convertToDoublePrimitive(
									_betaOfEquilibrium
									.get(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY))
									.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i))
									.stream()
									.map(tpl -> tpl._2.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList())
									)[MyArrayUtils.convertToDoublePrimitive(
											_betaOfEquilibrium
											.get(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY))
											.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i))
											.stream()
											.map(tpl -> tpl._2.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList())
											).length-1],
							100
							);
					
					Double[] betaEquilibriumFitted = new Double[deltaRudderEquilibriumFitted.length];
					for (int j=0; j<deltaRudderEquilibriumFitted.length; j++) 
						betaEquilibriumFitted[j] = betaEquilibriumFunction.value(deltaRudderEquilibriumFitted[j]);
					
					xVectorMatrix.add(betaEquilibriumFitted);
					yVectorMatrix.add(deltaRudderEquilibriumFitted);
					legend.add("Xcg = " + 
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i));
				}

				xMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				yMatrix = new double[xVectorMatrix.size()][xVectorMatrix.get(0).length];
				legendString = new String[xVectorMatrix.size()];

				for(int i=0; i <xVectorMatrix.size(); i++){
					xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVectorMatrix.get(i));
					yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVectorMatrix.get(i));
					legendString [i] = legend.get(i);
				}

				MyChartToFileUtils.plotNOCSV(
						xMatrix,
						yMatrix, 
						null, 
						null, 
						null, 
						null,
						"beta", 
						"delta_r",
						"deg", 
						"deg", 
						legendString, 
						aircraftPlotFolderPath,
						"Delta_Rudder_Equilibrium");
				
			}
		}
			else
				System.err.println("WARNING!! THE EQUILIBRIUM DELTA RUDDER ARRAY HAS NOT BEEN CALCULATED ... IMPOSSIBLE TO PLOT THE EQUILIBRIUM DELTA RUDDER");	
		
	}

}
