package sandbox.vt.ExecutableHighLiftDevices;

import java.util.ArrayList;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.HighLiftExexutableEnum;
import standaloneutils.JPADGlobalData;
import standaloneutils.customdata.MyXmlTree;

public class HighLiftDevicesCalc {

	public static void initializeInputTree() {
				
		JPADGlobalData.setTheXmlTree(new MyXmlTree());
		
		JPADGlobalData.getTheXmlTree().add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), HighLiftExexutableEnum.AlphaCurrent, 1, "Wing current angle of attack");
		JPADGlobalData.getTheXmlTree().add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), HighLiftExexutableEnum.AlphaMaxClean, 1, "Stall angle of attack of the wing in clean configuration");
		JPADGlobalData.getTheXmlTree().add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), HighLiftExexutableEnum.AlphaStarClean, 1, "Angle of attack related to the beginning of the non-linear trait of the wing lift curve in clean configuration");
		JPADGlobalData.getTheXmlTree().add(0.0, HighLiftExexutableEnum.AspectRatio, 1, "Wing aspect ratio");
		JPADGlobalData.getTheXmlTree().add(0.0, HighLiftExexutableEnum.AspectRatio, 1, "Wing aspect ratio");
		JPADGlobalData.getTheXmlTree().add(new ArrayList<Double>(), HighLiftExexutableEnum.CextCSlat, 1, "List of ratios between the airfoil chord with active slat and the clean airfoil chord");
		JPADGlobalData.getTheXmlTree().add(new ArrayList<Double>(), HighLiftExexutableEnum.Cfc, 1, "List of flap chord ratios");
		JPADGlobalData.getTheXmlTree().add(0.0, HighLiftExexutableEnum.ClAlphaMeanAirfoil, 1, "Slope of the lift curve of the mean airfoil (1/deg)");
		JPADGlobalData.getTheXmlTree().add(0.0, HighLiftExexutableEnum.CL0Clean, 1, "CL at alpha 0 deg related to the wing in clean configuration");
		JPADGlobalData.getTheXmlTree().add(0.0, HighLiftExexutableEnum.CLAlphaClean, 1, "Slope of the lift curve of the wing in clean configuration (1/rad)");
		JPADGlobalData.getTheXmlTree().add(0.0, HighLiftExexutableEnum.CLmaxClean, 1, "Maximum lift coefficient of the wing in clean configuration");
		JPADGlobalData.getTheXmlTree().add(0.0, HighLiftExexutableEnum.CLStarClean, 1, "Lift coefficient related to end of the linear trait of the lift curve in clean configuration");
		JPADGlobalData.getTheXmlTree().add(new ArrayList<Double>(), HighLiftExexutableEnum.Csc, 1, "Lidt of slat chord ratios");
		JPADGlobalData.getTheXmlTree().add(0.0, HighLiftExexutableEnum.DeltaYPercent, 1, "LE sharpness parameter of the mean airfoil");
		JPADGlobalData.getTheXmlTree().add(new ArrayList<Double>(), HighLiftExexutableEnum.EtaInFlap, 1, "List of flap inboard stations");
		JPADGlobalData.getTheXmlTree().add(new ArrayList<Double>(), HighLiftExexutableEnum.EtaOutFlap, 1, "List of flap outboard stations");
		JPADGlobalData.getTheXmlTree().add(new ArrayList<Double>(), HighLiftExexutableEnum.EtaInSlat, 1, "List of slat inboard stations");
		JPADGlobalData.getTheXmlTree().add(new ArrayList<Double>(), HighLiftExexutableEnum.EtaOutSlat, 1, "List of slat outboard stations");
		JPADGlobalData.getTheXmlTree().add(new ArrayList<FlapTypeEnum>(), HighLiftExexutableEnum.FlapType, 1, "List of flaps types");
		JPADGlobalData.getTheXmlTree().add(new ArrayList<Double[]>(), HighLiftExexutableEnum.DeltaFlap, 1, "List of array of flaps deflections");
		JPADGlobalData.getTheXmlTree().add(new ArrayList<Double>(), HighLiftExexutableEnum.DeltaSlat, 1, "List of slats deflections");
		JPADGlobalData.getTheXmlTree().add(0.0, HighLiftExexutableEnum.LERadiusMeanAirfoil, 1, "Leading edge radius of the mean airfoil");
		JPADGlobalData.getTheXmlTree().add(new ArrayList<Double[]>(), HighLiftExexutableEnum.MaxThicknessMeanAirfoil, 1, "Maximum thickness of the mean airfoil");
		JPADGlobalData.getTheXmlTree().add(Amount.valueOf(0.0, SI.METER), HighLiftExexutableEnum.Span, 1, "Wing span");
		JPADGlobalData.getTheXmlTree().add(Amount.valueOf(0.0, SI.SQUARE_METRE), HighLiftExexutableEnum.Surface, 1, "Wing area");
		JPADGlobalData.getTheXmlTree().add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), HighLiftExexutableEnum.SweepQuarterChordEq, 1, "Sweep angle of the equivalent wing at c/4");
		JPADGlobalData.getTheXmlTree().add(0.0, HighLiftExexutableEnum.TaperRatioEq, 1, "Taper ratio of the equivalent wing");
	}
	
	
	
}
