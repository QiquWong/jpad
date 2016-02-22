package sandbox.vt.SpecificRange_Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.ThrustMap;
import configuration.MyConfiguration;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.engine.EngineDatabaseManager;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;
import writers.JPADStaticWriteUtils;

/**
 * Class that allows user to evaluate the specific range for different mach numbers.
 * The guideline is to assign an array of Mach values, starting from the one realitve
 * to cruise stalling speed, and then evaluate the SFC, from engine database, for each 
 * Mach number of a given array; from here the A.F. is built by evaluating the aerodynamic
 * efficinecy for each value of the same Mach array. Finally the specific range is
 * calculated, in [nmi]/[lbs], dividing the A.F. by the max take off mass.
 * Furthermore the class allow to plot the Specific Range v.s. Mach chart parameterized
 * at different weight conditions. 
 * 
 * @author Vittorio Trifari
 */

public class SpecificRangeCalc {
	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	
	private double[][] specificRange, sfc, efficiency;
	
	//-------------------------------------------------------------------------------------
	// METHODS
	
	/**************************************************************************************
	 * A static method that allows users to evaluate the aerodynamic efficiency value for each 
	 * Mach number of a given array; this through the evaluation of the CL and the relative
	 * CD from the total drag polar. 
	 * 
	 * @author Vittorio Trifari
	 * @param maxTakeOffMass
	 * @param mach, an array of mach number
	 * @param surface
	 * @param altitude
	 * @param ar
	 * @param oswald
	 * @param cd0
	 * @param tcMax
	 * @param sweepHalfChord
	 * @param airfoilType
	 * @return efficiency, an array of the aerodynamic efficiency for each given mach number
	 */
	public static Double[] calculateEfficiencyVsMach(
			Amount<Mass> maxTakeOffMass,
			Double[] mach,
			double surface,
			double altitude,
			double ar,
			double oswald,
			double cd0,
			double tcMax,
			Amount<Angle> sweepHalfChord,
			AirfoilTypeEnum airfoilType
			) {
		
		Double[] cL = new Double[mach.length];
		Double[] cD = new Double[mach.length];
		Double[] efficiency = new Double[mach.length];
		
		for(int i=0; i<mach.length; i++) {
			cL[i] = LiftCalc.calculateLiftCoeff(
					maxTakeOffMass.times(AtmosphereCalc.g0).getEstimatedValue(),
					SpeedCalc.calculateTAS(mach[i], altitude),
					surface,
					altitude);
			
			cD[i] = DragCalc.calculateCDTotal(
					cd0,
					cL[i],
					ar,
					oswald,
					mach[i],
					sweepHalfChord.getEstimatedValue(),
					tcMax,
					airfoilType);
			
			efficiency[i] = cL[i]/cD[i];
		}
		return efficiency;
	}
	
	/**************************************************************************************
	 * This static method allows users to evaluate the SFC of a turboprop or a turbofan
	 * aircraft for each Mach number of a given array; this by reading from the relative
	 * engine database. 
	 * 
	 * @author Vittorio Trifari
	 * @param mach, an array of mach number
	 * @param altitude
	 * @param bpr
	 * @param engineType
	 * @return
	 */
	public static Double[] calculateSfcVsMach(
			Double[] mach,
			double altitude,
			double bpr,
			EngineTypeEnum engineType) {
		
		Double sfcMach[] = new Double[mach.length];

		for (int i=0; i<mach.length; i++) {
			sfcMach[i] = EngineDatabaseManager.getSFC(
					mach[i],
					altitude,
					EngineDatabaseManager.getThrustRatio(
							mach[i],
							altitude,
							bpr,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					bpr,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);
		}
		return sfcMach;
	}
	
	/**************************************************************************************
	 * Static method that allows users to calculate the specific range of a turboprop or a turbofan 
	 * aircraft for each Mach number of a given array. Moreover it allows to parameterize the
	 * specific range with maximum take off mass.
	 * 
	 * @author Vittorio Trifari
	 * @param maxTakeOffMass
	 * @param mach, an array of mach number
	 * @param sfc, an array of SFC values
	 * @param efficiency, an array of aerodynamic efficiency values
	 * @param altitude
	 * @param bpr
	 * @param eta
	 * @param engineType
	 * @return specificRange, an array of the specific range, in [nmi/lb], v.s. Mach at that MTOM 
	 */
	public static Double[] calculateSpecificRangeVsMach(
			Amount<Mass> maxTakeOffMass,
			Double[] mach,
			Double[] sfc,
			Double[] efficiency,
			double altitude,
			double bpr,
			double eta,
			EngineTypeEnum engineType) {
		
		Double specificRange[] = new Double[mach.length];
		
		if (engineType == EngineTypeEnum.TURBOFAN) {
			
			Double speed[] = new Double [mach.length];
			for (int i=0; i<mach.length; i++) {
				speed[i] = SpeedCalc.calculateTAS(mach[i], altitude);
				speed[i] = Amount.valueOf(speed[i],SI.METERS_PER_SECOND).to(NonSI.KNOT).getEstimatedValue();
			}
			for (int i=0; i<sfc.length; i++)
				specificRange[i] = ((speed[i]*efficiency[i])/sfc[i])/(maxTakeOffMass.to(NonSI.POUND).getEstimatedValue());
		}
		else if(engineType == EngineTypeEnum.TURBOPROP) {
			
			for (int i=0; i<sfc.length; i++) 
				// the constant is needed in order to use sfc in lb/(hp*h) and obtain [nmi]/[lbs]
				specificRange[i] = 325.8640495*(((eta*efficiency[i])/sfc[i])/(maxTakeOffMass.to(NonSI.POUND).getEstimatedValue()));
		}
		
		return specificRange;
	}

	/**************************************************************************************
	 * Static method that allows users to create a chart for representing the speific range v.s. 
	 * Mach number parameterized in maximum take off mass.
	 *  
	 * @author Vittorio Trifari
	 * @param specificRange, a matrix of double values representing the specific range which rows are parameterized in MTOM
	 * @param machArray, a matrix of mach number which rows are parameterized to different MTOM
	 * @param maxTakeOffMassArray, an array of maxTakeOffMass double values
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void createSpecificRangeChart(
			List<Double[]> specificRange,
			List<Double[]> mach,
			List<String> legend
			) throws InstantiationException, IllegalAccessException{

		System.out.println("\n------WRITING SPECIFIC RANGE v.s. MACH CHART TO FILE-------");

		String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "SpecificRange" + File.separator);
		
		// creating and adding maximum values curve to Lists
		Double[] maxArray = new Double[specificRange.size()];
		Double[] machMaxArray = new Double[specificRange.size()];
		for(int i=0; i<specificRange.size(); i++) {
			maxArray[i] = MyArrayUtils.getMax(specificRange.get(i));
			machMaxArray[i] = mach.get(i)[MyArrayUtils.getIndexOfMax(specificRange.get(i))];
		}
		mach.add(machMaxArray);
		specificRange.add(maxArray);
		legend.add("Best Range");
		
		// creating and adding 1% penalty condition to Lists
		Double[] longRangeArray = new Double[maxArray.length];
		Double[] machLongRangeArray = new Double[maxArray.length];
		
		double[] curve1 = new double[specificRange.get(0).length];
		double[] curve2 = new double[specificRange.get(0).length];
		List<double[]> intersection = new ArrayList<double[]>();
		
		for(int i=0; i<maxArray.length; i++) {
			for(int j=0; j<curve1.length; j++)
				curve1[j] = maxArray[i] - (0.01*maxArray[i]);
			for(int k=0; k<specificRange.get(i).length; k++)
				curve2[k] = specificRange.get(i)[k].doubleValue();
			intersection.add(MyArrayUtils.intersectArraysSimple(curve1, curve2));
		}
		for(int i=0; i<intersection.size(); i++)
			for(int j=0; j<intersection.get(i).length; j++)
				if(intersection.get(i)[j] != 0.0) {
					longRangeArray[i] = intersection.get(i)[j];
					machLongRangeArray[i] = mach.get(i)[j];
				}
		mach.add(machLongRangeArray);
		specificRange.add(longRangeArray);
		legend.add("Long Range");
		
		MyChartToFileUtils.plotJFreeChart(
				mach, specificRange,									// List to be plotted
				"Specific Range v.s. Mach","Mach", "Specific Range",	// Title and labels
				null, null, null, null,									// Axis
				"", "nmi/lbs",											// Units			
				true, legend,											// Legend visibility and values
				subfolderPath, "SpecificRange"							// output information
				);
	}
	
	/**************************************************************************************
	 * Static method that allows users to create a chart for representing the specific fuel
	 * consumption (SFC) v.s. Mach number parameterized in maximum take off mass.
	 * 
	 * @author Vittorio Trifari
	 * @param sfc, a matrix of double values representing SFC which rows are parameterized in MTOM
	 * @param machMatrix, a matrix of mach number which rows are parameterized to different MTOM
	 * @param maxTakeOffMassArray, an array of maxTakeOffMass double values
	 * @param engineType
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void createSfcChart(
			List<Double[]> sfc,
			List<Double[]> mach,
			List<String> legend,
			EngineTypeEnum engineType) throws InstantiationException, IllegalAccessException {
		
		System.out.println("\n-----------WRITING SFC v.s. MACH CHART TO FILE-------------");
		
		String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "SpecificRange" + File.separator);
	
		// check on mach list elements
		if(mach.size() == sfc.size()+2) {
			mach.remove(mach.size()-1);
			mach.remove(mach.size()-1);
		}
		
		if(engineType == EngineTypeEnum.TURBOFAN )
			MyChartToFileUtils.plotJFreeChart(
					mach, sfc,
					"SFC v.s. Mach", "Mach", "SFC",
					null, null, null, null,
					"", "lb/(lb*h)",
					true, legend,
					subfolderPath, "SFC"
					);
		else if(engineType == EngineTypeEnum.TURBOPROP)
			MyChartToFileUtils.plotJFreeChart(
					mach, sfc,
					"SFC v.s. Mach", "Mach", "SFC",
					null, null, null, null,
					"", "lb/(hp*h)",
					true, legend,
					subfolderPath, "SFC"
					);  
	}
	
	public static void createEfficiencyChart(
			List<Double[]> efficiency,
			List<Double[]> mach,
			List<String> legend) throws InstantiationException, IllegalAccessException {
		
		System.out.println("\n--------WRITING EFFICINECY v.s. MACH CHART TO FILE---------");

		String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "SpecificRange" + File.separator);
	
		// check on mach list elements
				if(mach.size() == efficiency.size()+2) {
					mach.remove(mach.size()-1);
					mach.remove(mach.size()-1);
				}
		
		MyChartToFileUtils.plotJFreeChart(
				mach, efficiency,
				"Efficiency v.s. Mach", "Mach", "Efficiency",
				null, null, null, null,
				"", "",	
				true, legend,
				subfolderPath, "Efficiency"
				); 
	}
	
	/***************************************************************************************
	 * This method plots drag and thrust available at different weight conditions as 
	 * function of Mach number.
	 * 
	 * @author Vittorio Trifari
	 * @param altitude [m]
	 * @param maxTakeOffMassArray array of maximum take off mass
	 * @param listDrag a list of DragMap elements
	 * @param listThrust a list of ThrustMap elements
	 * @param speed the abscissa array
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void createThrustDragIntersectionChart(
			double altitude,
			double[] maxTakeOffMassArray,
			List<DragMap> listDrag,
			List<ThrustMap> listThrust,
			double[] speed
			) throws InstantiationException, IllegalAccessException {
		
		System.out.println("\n------WRITING SPECIFIC RANGE v.s. MACH CHART TO FILE-------");
		String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "SpecificRange" + File.separator);

		List<Double[]> yList = new ArrayList<Double[]>();
		Double[][] dragMatrix = new Double[listDrag.size()][listDrag.get(0).getDrag().length];
		for(int i=0; i<listDrag.size(); i++)
			for(int j=0; j<listDrag.get(i).getDrag().length; j++)
				dragMatrix[i][j] = Double.valueOf(listDrag.get(i).getDrag()[j]);
		for(int i=0; i<dragMatrix.length; i++)
			yList.add(dragMatrix[i]);
		Double[][] thrustMatrix = new Double[listThrust.size()][listThrust.get(0).getThrust().length];
		for(int i=0; i<listThrust.size(); i++)
			for(int j=0; j<listThrust.get(i).getThrust().length; j++)
				thrustMatrix[i][j] = Double.valueOf(listThrust.get(i).getThrust()[j]);
		yList.add(thrustMatrix[0]);

		List<Double[]> xList = new ArrayList<Double[]>();
		Double[][] machMatrix = new Double[maxTakeOffMassArray.length + 1][speed.length];
		for(int i=0; i<maxTakeOffMassArray.length + 1; i++)
			for(int j=0; j<speed.length; j++)
				machMatrix[i][j] = Double.valueOf(
						SpeedCalc.calculateMach(
								altitude,
								speed[j]
								)
						);
		for(int i=0; i<machMatrix.length; i++)
			xList.add(machMatrix[i]);
			
		List<String> legendList = new ArrayList<String>();
		for(int i=0; i<maxTakeOffMassArray.length; i++)
			legendList.add("Drag ar MTOM = " + maxTakeOffMassArray[i]);
		legendList.add("Thrust available");
		
		MyChartToFileUtils.plotJFreeChart(
				xList, yList,
				"Drag-Thrust v.s. Mach at " + altitude + " m", "Mach", "Drag, Thrust",
//				0.22, 0.5, null, 30000.0,      // TP axis
				0.4, 0.9, null, 400000.0,       // TF axis
//				null, null, null, null,
				"", "N",
				true, legendList,
				subfolderPath, "DragThrust"
				);
	}

	//--------------------------------------------------------------------------------------
	// GETTERS & SETTERS:

	public double[][] getSpecificRange() {
		return specificRange;
	}

	public double[][] getSfc() {
		return sfc;
	}

	public double[][] getEfficiency() {
		return efficiency;
	}
}