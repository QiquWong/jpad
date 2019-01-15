package calculators.performance;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;
import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;

import aircraft.components.powerplant.PowerPlant;
import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.atmosphere.SpeedCalc;

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
	public static double[] calculateSfcVsMach(
			double[] mach,
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double phi,
			double sfcCorrectionFactor,
			PowerPlant thePowerPlant
			) {
		
		double sfcMach[] = new double[mach.length];

		for (int i=0; i<mach.length; i++) {
			
			sfcMach[i] = thePowerPlant.getEngineDatabaseReaderList().get(i).getSfc(
					mach[i],
					altitude,
					deltaTemperature,
					phi,
					EngineOperatingConditionEnum.CRUISE,
					sfcCorrectionFactor
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
	public static double[] calculateSpecificRangeVsMach(
			Amount<Mass> maxTakeOffMass,
			double[] mach,
			double[] sfc,
			double[] efficiency,
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature
//			double eta,
//			EngineTypeEnum engineType
			) {
		
		double specificRange[] = new double[mach.length];
		
//		if (engineType == EngineTypeEnum.TURBOFAN) {
			
		double speed[] = new double [mach.length];
		for (int i=0; i<mach.length; i++) 
			speed[i] = SpeedCalc.calculateTAS(mach[i], altitude, deltaTemperature).doubleValue(NonSI.KNOT);
		for (int i=0; i<sfc.length; i++)
			specificRange[i] = ((speed[i]*efficiency[i])/sfc[i])/(maxTakeOffMass.doubleValue(NonSI.POUND));
			
//		}
//		else if(engineType == EngineTypeEnum.TURBOPROP) {
//			
//			for (int i=0; i<sfc.length; i++) 
//				// the constant is needed in order to use sfc in lb/(hp*h) and obtain [nmi]/[lbs]
//				specificRange[i] = 325.8640495*(((eta*efficiency[i])/sfc[i])/(maxTakeOffMass.doubleValue(NonSI.POUND)));
//		}
		
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
			List<String> legend,
			String performanceFolderPath,
			boolean createCSV
			) throws InstantiationException, IllegalAccessException{

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
		
		MyChartToFileUtils.plot(
				mach, specificRange,									// List to be plotted
				"Specific Range v.s. Mach","Mach", "Specific Range",	// Title and labels
				null, null, null, null,									// Axis
				"", "nmi/lbs",											// Units			
				true, legend,											// Legend visibility and values
				performanceFolderPath, "Cruise_Grid",					// output information
				createCSV
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
			String performanceFolderPath,
//			EngineTypeEnum engineType,
			boolean createCSV) throws InstantiationException, IllegalAccessException {
		
		System.out.println("\n-----------WRITING SFC v.s. MACH CHART TO FILE-------------");
		
		// check on mach list elements
		if(mach.size() == sfc.size()+2) {
			mach.remove(mach.size()-1);
			mach.remove(mach.size()-1);
		}
		
//		if(engineType == EngineTypeEnum.TURBOFAN )
		MyChartToFileUtils.plot(
				mach, sfc,
				"SFC v.s. Mach", "Mach", "SFC",
				null, null, null, null,
				"", "lb/(lb*h)",
				true, legend,
				performanceFolderPath, "SFC",
				createCSV
				);
//		else if(engineType == EngineTypeEnum.TURBOPROP)
//			MyChartToFileUtils.plot(
//					mach, sfc,
//					"SFC v.s. Mach", "Mach", "SFC",
//					null, null, null, null,
//					"", "lb/(hp*h)",
//					true, legend,
//					subfolderPath, "SFC",
//					createCSV
//					);  
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