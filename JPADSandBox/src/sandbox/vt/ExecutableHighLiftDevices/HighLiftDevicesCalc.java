package sandbox.vt.ExecutableHighLiftDevices;

import java.util.ArrayList;
import java.util.List;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import org.jscience.physics.amount.Amount;
import configuration.enumerations.HighLiftExexutableEnum;
import standaloneutils.database.io.DatabaseIOmanager;
import standaloneutils.database.io.InputFileReader;

public class HighLiftDevicesCalc {
	
	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	
	static DatabaseIOmanager<HighLiftExexutableEnum> inputManager = new DatabaseIOmanager<HighLiftExexutableEnum>();
	static DatabaseIOmanager<HighLiftExexutableEnum> outputManager = new DatabaseIOmanager<HighLiftExexutableEnum>();
	
	/**********************************************************************************************************************************************
	 * This method initializes all required input data.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @return inputManager the object which contains all input data initialized
	 */
	public static DatabaseIOmanager<HighLiftExexutableEnum> initializeInputTree() {

		// FLIGHT CONDITION:
		inputManager.addElement(HighLiftExexutableEnum.AlphaCurrent, Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), "Wing current angle of attack");
		
		// WING:
		// Geometry
		inputManager.addElement(HighLiftExexutableEnum.AspectRatio, Amount.valueOf(0.0, Unit.ONE), "Wing aspect ratio");
		inputManager.addElement(HighLiftExexutableEnum.Span, Amount.valueOf(0.0, SI.METER), "Wing span");
		inputManager.addElement(HighLiftExexutableEnum.Surface, Amount.valueOf(0.0, SI.SQUARE_METRE), "Wing area");
		inputManager.addElement(HighLiftExexutableEnum.SweepQuarterChordEq, Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), "Sweep angle of the equivalent wing at c/4");
		inputManager.addElement(HighLiftExexutableEnum.TaperRatioEq, Amount.valueOf(0.0, Unit.ONE), "Taper ratio of the equivalent wing");
		inputManager.addElement(HighLiftExexutableEnum.DeltaYPercent, Amount.valueOf(0.0, Unit.ONE), "LE sharpness parameter of the mean airfoil");
		
		// Clean configuration parameters
		inputManager.addElement(HighLiftExexutableEnum.AlphaMaxClean, Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), "Stall angle of attack of the wing in clean configuration");
		inputManager.addElement(HighLiftExexutableEnum.AlphaStarClean, Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), "Angle of attack related to the beginning of the non-linear trait of the wing lift curve in clean configuration");
		inputManager.addElement(HighLiftExexutableEnum.CL0Clean, Amount.valueOf(0.0, Unit.ONE), "CL at alpha 0 deg related to the wing in clean configuration");
		inputManager.addElement(HighLiftExexutableEnum.CLAlphaClean, Amount.valueOf(0.0, NonSI.DEGREE_ANGLE.inverse()), "Slope of the lift curve of the wing in clean configuration");
		inputManager.addElement(HighLiftExexutableEnum.CLmaxClean, Amount.valueOf(0.0, Unit.ONE), "Maximum lift coefficient of the wing in clean configuration");
		inputManager.addElement(HighLiftExexutableEnum.CLStarClean, Amount.valueOf(0.0, Unit.ONE), "Lift coefficient related to end of the linear trait of the lift curve in clean configuration");
		
		// Mean airfoil
		inputManager.addElement(HighLiftExexutableEnum.ClAlphaMeanAirfoil, Amount.valueOf(0.0, NonSI.DEGREE_ANGLE.inverse()), "Slope of the lift curve of the mean airfoil");
		inputManager.addElement(HighLiftExexutableEnum.LERadiusMeanAirfoil, Amount.valueOf(0.0, Unit.ONE), "LE radius of the mean airfoil");
		inputManager.addElement(HighLiftExexutableEnum.MaxThicknessMeanAirfoil, Amount.valueOf(0.0, Unit.ONE), "Maximum thickness of the mean airfoil");
		
		// FLAPS DATA:	
		inputManager.addElementStringList(HighLiftExexutableEnum.FlapType, new ArrayList<String>(), "List of flaps types");
		inputManager.addElement(HighLiftExexutableEnum.Cfc, new ArrayList<Double>(), "List of flap chord ratios");
		inputManager.addElementDoubleArray(HighLiftExexutableEnum.DeltaFlap, new ArrayList<Double[]>(), "List of array of flaps deflections");
		inputManager.addElement(HighLiftExexutableEnum.EtaInFlap, new ArrayList<Double>(), "List of flap inboard stations");
		inputManager.addElement(HighLiftExexutableEnum.EtaOutFlap, new ArrayList<Double>(), "List of flap outboard stations");
		
		// SLATS DATA:
		inputManager.addElement(HighLiftExexutableEnum.DeltaSlat, new ArrayList<Double>(), "List of slats deflections");
		inputManager.addElement(HighLiftExexutableEnum.Csc, new ArrayList<Double>(), "List of slat chord ratios");
		inputManager.addElement(HighLiftExexutableEnum.CextCSlat, new ArrayList<Double>(), "List of ratios between the airfoil chord with active slat and the clean airfoil chord");
		inputManager.addElement(HighLiftExexutableEnum.EtaInSlat, new ArrayList<Double>(), "List of slat inboard stations");
		inputManager.addElement(HighLiftExexutableEnum.EtaOutSlat, new ArrayList<Double>(), "List of slat outboard stations");

		return inputManager;
	}
	
	/**********************************************************************************************************************************************
	 * This method populates the outputManager with all output data 
	 * obtained from the calculation method.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param alphaMaxFlapSlat
	 * @param alphaStarFlapSlat
	 * @param cLAlphaFlapList
	 * @param cLAlphaFlap
	 * @param cLmaxFlapSlat
	 * @param cLStarFlapSlat
	 * @param deltaCD0List
	 * @param deltaCD0
	 * @param deltaCl0FlapList
	 * @param deltaCl0Flap
	 * @param deltaCL0FlapList
	 * @param deltaCL0Flap
	 * @param deltaClmaxFlapList
	 * @param deltaClmaxFlap
	 * @param deltaCLmaxFlapList
	 * @param deltaCLmaxFlap
	 * @param deltaClmaxSlatList
	 * @param deltaClmaxSlat
	 * @param deltaCLmaxSlatList
	 * @param deltaCLmaxSlat
	 * @param deltaCMc4List
	 * @param deltaCMc4
	 * @return outputManager the object which contains all output data
	 */
	public static DatabaseIOmanager<HighLiftExexutableEnum> initializeOutputTree(
			double alphaMaxFlapSlat,
			double alphaStarFlapSlat,
			List<Double> cLAlphaFlapList,
			double cLAlphaFlap,
			double cLmaxFlapSlat,
			double cLStarFlapSlat,
			List<Double> deltaCD0List,
			double deltaCD0,
			List<Double> deltaCl0FlapList,
			double deltaCl0Flap,
			List<Double> deltaCL0FlapList,
			double deltaCL0Flap,
			List<Double> deltaClmaxFlapList,
			double deltaClmaxFlap,
			List<Double> deltaCLmaxFlapList,
			double deltaCLmaxFlap,
			List<Double> deltaClmaxSlatList,
			double deltaClmaxSlat,
			List<Double> deltaCLmaxSlatList,
			double deltaCLmaxSlat,
			List<Double> deltaCMc4List,
			double deltaCMc4
			) {

		outputManager.addElement(HighLiftExexutableEnum.AlphaMaxFlapSlat, Amount.valueOf(alphaMaxFlapSlat, NonSI.DEGREE_ANGLE), "Stall angle of attack with flaps and slats deflected"); 
		outputManager.addElement(HighLiftExexutableEnum.AlphaStarFlapSlat, Amount.valueOf(alphaStarFlapSlat, NonSI.DEGREE_ANGLE), "Angle of attack related to the beginning of the non-linear trait of the wing lift curve with flaps and slats deflected");
		outputManager.addElement(HighLiftExexutableEnum.CLAlphaFlapList, cLAlphaFlapList, "List of slopes of the lift curve of the wing taking into account the effect of each flap");
		outputManager.addElement(HighLiftExexutableEnum.CLAlphaFlap, Amount.valueOf(cLAlphaFlap, NonSI.DEGREE_ANGLE.inverse()), "Slope of the lift curve of the wing with flaps and slats deflected");
		outputManager.addElement(HighLiftExexutableEnum.CLmaxFlapSlat, Amount.valueOf(cLmaxFlapSlat, Unit.ONE), "Maximum lift coefficient of the wing with flaps and slats deflected");
		outputManager.addElement(HighLiftExexutableEnum.CLStarFlapSlat, Amount.valueOf(cLStarFlapSlat, Unit.ONE), "Lift coefficient related to end of the linear trait of the lift curve with flaps and slats deflected");
		outputManager.addElement(HighLiftExexutableEnum.DeltaCD0List, deltaCD0List, "List of contributes of each flap to the CD0");
		outputManager.addElement(HighLiftExexutableEnum.DeltaCD0, Amount.valueOf(deltaCD0, Unit.ONE), "Total contribute of flaps to the CD0");
		outputManager.addElement(HighLiftExexutableEnum.DeltaCl0FlapList, deltaCl0FlapList, "List of contributes of each flap to the Cl0 (2D)");
		outputManager.addElement(HighLiftExexutableEnum.DeltaCl0Flap, Amount.valueOf(deltaCl0Flap, Unit.ONE), "Total contribute of flaps to the Cl0 (2D)");
		outputManager.addElement(HighLiftExexutableEnum.DeltaCL0FlapList, deltaCL0FlapList, "List of contributes of each flap to the CL0 (3D)");
		outputManager.addElement(HighLiftExexutableEnum.DeltaCL0Flap, Amount.valueOf(deltaCL0Flap, Unit.ONE), "Total contribute of flaps to the CL0 (3D)");
		outputManager.addElement(HighLiftExexutableEnum.DeltaClmaxFlapList, deltaClmaxFlapList, "List of contributes of each flap to the Clmax (2D)");
		outputManager.addElement(HighLiftExexutableEnum.DeltaClmaxFlap, Amount.valueOf(deltaClmaxFlap, Unit.ONE), "Total contribute of flaps to the Clmax (2D)");
		outputManager.addElement(HighLiftExexutableEnum.DeltaCLmaxFlapList, deltaCLmaxFlapList, "List of contributes of each flap to the CLmax (3D)");
		outputManager.addElement(HighLiftExexutableEnum.DeltaCLmaxFlap, Amount.valueOf(deltaCLmaxFlap, Unit.ONE), "Total contribute of flaps to the CLmax (3D)");
		outputManager.addElement(HighLiftExexutableEnum.DeltaClmaxSlatList, deltaClmaxSlatList, "List of contributes of each slat to the Clmax (2D)");
		outputManager.addElement(HighLiftExexutableEnum.DeltaClmaxSlat, Amount.valueOf(deltaClmaxSlat, Unit.ONE), "Total contribute of slats to the Clmax (2D)");
		outputManager.addElement(HighLiftExexutableEnum.DeltaCLmaxSlatList, deltaCLmaxSlatList, "List of contributes of each slat to the CLmax (3D)");
		outputManager.addElement(HighLiftExexutableEnum.DeltaCLmaxSlat, Amount.valueOf(deltaCLmaxSlat, Unit.ONE), "Total contribute of slats to the CLmax (3D)");
		outputManager.addElement(HighLiftExexutableEnum.DeltaCMc4List, deltaCMc4List, "List of contributes of each flap to the CM_c/4 (3D)");
		outputManager.addElement(HighLiftExexutableEnum.DeltaCMc4, Amount.valueOf(deltaCMc4, Unit.ONE), "Total contribute of flaps to the CM_c/4 (3D)");
		
		return outputManager;  
	} 
	
	/**********************************************************************************************************************************************
	 * This method is in charge of reading data from a given XML input file and 
	 * put them inside the initialized object of the DatabaseIOManager created before.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param filenamewithPathAndExt
	 * @return
	 */
	public static DatabaseIOmanager<HighLiftExexutableEnum> readFromFile(String filenamewithPathAndExt) {
		
		inputManager = initializeInputTree();
		
		InputFileReader<HighLiftExexutableEnum> amountsInputFileReader = 
				new InputFileReader<HighLiftExexutableEnum>(
						filenamewithPathAndExt,
						inputManager.getTagList()
						);
		InputFileReader<HighLiftExexutableEnum> stringInputFileReader = 
				new InputFileReader<HighLiftExexutableEnum>(
						filenamewithPathAndExt,
						inputManager.getTagListString()
						);
		
		InputFileReader<HighLiftExexutableEnum> listStringInputFileReader = 
				new InputFileReader<HighLiftExexutableEnum>(
						filenamewithPathAndExt,
						inputManager.getTagListListString()
						);
		
		InputFileReader<HighLiftExexutableEnum> listDoubleInputFileReader = 
				new InputFileReader<HighLiftExexutableEnum>(
						filenamewithPathAndExt,
						inputManager.getTagListListDouble()
						);
		
		InputFileReader<HighLiftExexutableEnum> listDoubleVecInputFileReader = 
				new InputFileReader<HighLiftExexutableEnum>(
						filenamewithPathAndExt,
						inputManager.getTagVecListListDouble()
						);
		
		List<Amount> valueList = amountsInputFileReader.readAmounts();
		inputManager.setValueList(valueList);
		
		List<String> stringList = stringInputFileReader.readStrings();
		inputManager.setStringList(stringList);
		
		List<List<String>> stringListList = listStringInputFileReader.readStringLists();
		inputManager.setStringListList(stringListList);
		
		List<List<Double>> doubleListList = listDoubleInputFileReader.readDoubleLists();
		inputManager.setDoubleListList(doubleListList);
		
		List<List<Double[]>> doubleVecListList = listDoubleVecInputFileReader.readDoubleVecLists();
		inputManager.setDoubleVecListList(doubleVecListList);

		return inputManager;
	}
}
