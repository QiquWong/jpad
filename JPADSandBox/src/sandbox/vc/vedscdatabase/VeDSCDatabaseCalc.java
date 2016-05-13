package sandbox.vc.vedscdatabase;

import java.util.List;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import calculators.aerodynamics.MomentCalc;
import calculators.geometry.LSGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.FusDesDatabaseEnum;
import configuration.enumerations.VeDSCDatabaseEnum;
import database.databasefunctions.aerodynamics.DatabaseManager;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import standaloneutils.database.io.InputFileReader;
import standaloneutils.database.io.DatabaseFileWriter;
import standaloneutils.database.io.DatabaseIOmanager;

public class VeDSCDatabaseCalc {
	
	public static DatabaseIOmanager<VeDSCDatabaseEnum> initializeInputTree() {
		
		DatabaseIOmanager<VeDSCDatabaseEnum> ioManager = new DatabaseIOmanager<VeDSCDatabaseEnum>();
		
		ioManager.addElement(VeDSCDatabaseEnum.Mach_number, Amount.valueOf(0., Unit.ONE), "Mach number.");
		
		ioManager.addElement(VeDSCDatabaseEnum.Wing_Aspect_Ratio, Amount.valueOf(0., Unit.ONE), 
				"Wing Aspect Ratio. Accepts values in [6,14] range.");
		
		ioManager.addElement(VeDSCDatabaseEnum.Wing_span, Amount.valueOf(0., SI.METER), "Wing span");
		
		ioManager.addElement(VeDSCDatabaseEnum.Wing_position, Amount.valueOf(0., Unit.ONE),
				"Between -1 and +1. Low wing = -1; high wing = 1");
		
		ioManager.addElement(VeDSCDatabaseEnum.Vertical_Tail_Aspect_Ratio, Amount.valueOf(0., Unit.ONE), 
				"Vertical Tail Aspect Ratio. Accepts values in [1,2] range.");
		
		ioManager.addElement(VeDSCDatabaseEnum.Vertical_Tail_span, Amount.valueOf(0., SI.METER), "Vertical tail span");
		
		ioManager.addElement(VeDSCDatabaseEnum.Vertical_Tail_Arm, Amount.valueOf(0., SI.METER),
				"Distance of the AC of the vertical tail MAC from the MAC/4 point of the wing");
		
		ioManager.addElement(VeDSCDatabaseEnum.Vertical_Tail_Sweep_at_half_chord, Amount.valueOf(0., NonSI.DEGREE_ANGLE), 
				"Vertical Tail sweep at half chord.");
		
		ioManager.addElement(VeDSCDatabaseEnum.Vertical_tail_airfoil_lift_curve_slope, Amount.valueOf(0., SI.RADIAN.inverse()),
				"Vertical tail airfoil lift curve slope.");
		
		ioManager.addElement(VeDSCDatabaseEnum.Horizontal_position_over_vertical, Amount.valueOf(0., Unit.ONE), 
				"Relative position of the horizontal tail over the vertical tail span, "
				+ "computed from a reference line. Must be in [0,1] range"); //TODO
		
		ioManager.addElement(VeDSCDatabaseEnum.Diameter_at_vertical_MAC, Amount.valueOf(0., SI.METER),
				"Fuselage diameter at vertical MAC");
		
		ioManager.addElement(VeDSCDatabaseEnum.Tailcone_shape, Amount.valueOf(0., Unit.ONE), "Fuselage tailcone shape");

		return ioManager;
	}

	/**
	 * 
	 * @param kFv
	 * @param kVf
	 * @param kWv
	 * @param kWf
	 * @param kHv
	 * @param kHf
	 * @param cNbetaVertical
	 * @return
	 */
	public static DatabaseIOmanager<VeDSCDatabaseEnum> initializeOutputTree(
			double kFv, double kVf, double kWv, double kWf, double kHv, double kHf, double cNbetaVertical) {
		
		DatabaseIOmanager<VeDSCDatabaseEnum> ioManager = new DatabaseIOmanager<VeDSCDatabaseEnum>();
		
		ioManager.addElement(VeDSCDatabaseEnum.KFv_vs_bv_over_dfv, Amount.valueOf(kFv, Unit.ONE), 
				"Aerodynamic interference factor of the fuselage on the vertical tail "
				+ "as a function of the ratio of the vertical tail span over the fuselage diameter "
				+ "taken at the aerodynamic center on the MAC of the vertical tail");
		
		ioManager.addElement(VeDSCDatabaseEnum.KHf_vs_zh_over_bv1, Amount.valueOf(kHf, Unit.ONE),
				"Interference factor of the horizontal tail on the fuselage");
		
		ioManager.addElement(VeDSCDatabaseEnum.KHv_vs_zh_over_bv1, Amount.valueOf(kHv, Unit.ONE),
				"Interference factor of the horizontal tail on the vertical tail");
		
		ioManager.addElement(VeDSCDatabaseEnum.KVf_vs_bv_over_dfv, Amount.valueOf(kVf, Unit.ONE), 
				"Interference factor of the vertical tail on the fuselage");
		
		ioManager.addElement(VeDSCDatabaseEnum.KWf_vs_zw_over_rf, Amount.valueOf(kWf, Unit.ONE),
				"Interference factor of the wing on the fuselage");
		
		ioManager.addElement(VeDSCDatabaseEnum.KWv_vs_zw_over_rf, Amount.valueOf(kWv, Unit.ONE),
				"Interference factor of the wing on the vertical tail");
		
		ioManager.addElement(VeDSCDatabaseEnum.CN_beta_vertical, Amount.valueOf(cNbetaVertical, SI.RADIAN.inverse()),
				"Yawing moment coefficient derivative, vertical tail contribution");
		
		return ioManager;
	}

	/**
	 * 
	 * @param filenamewithPathAndExt
	 * @return
	 */
	
public static DatabaseIOmanager<VeDSCDatabaseEnum> readFromFile(String filenamewithPathAndExt) {
		
		DatabaseIOmanager<VeDSCDatabaseEnum> inputManager = initializeInputTree();
		
		InputFileReader<VeDSCDatabaseEnum> databaseReader = new InputFileReader<VeDSCDatabaseEnum>(
				filenamewithPathAndExt, inputManager.getTagList());

		List<Amount> valueList = databaseReader.readAmounts();
		inputManager.setValueList(valueList);
		return inputManager;
	}

	/**
	 * 
	 * @param filenamewithPathAndExt
	 * @param inputManager
	 * @param outputManager
	 */
	public static void writeToFile(
			String filenamewithPathAndExt,
			String databaseFileName,
			DatabaseIOmanager<VeDSCDatabaseEnum> inputManager,
			DatabaseIOmanager<VeDSCDatabaseEnum> outputManager) {
		
		DatabaseFileWriter<VeDSCDatabaseEnum> databaseWriter = 
				new DatabaseFileWriter<VeDSCDatabaseEnum>(
						databaseFileName, filenamewithPathAndExt, 
						inputManager, outputManager);
		
		databaseWriter.writeDocument();
	}
	
	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param inputFileWithPathAndExt
	 * @param outputFileWithPathAndExt
	 */
	public static void executeStandalone(String databaseFileName, String inputFileWithPathAndExt, String outputFileWithPathAndExt) {
		
		DatabaseIOmanager<VeDSCDatabaseEnum> inputManager = readFromFile(inputFileWithPathAndExt);
		
		VeDSCDatabaseReader veDSCDatabaseReader = DatabaseManager.initializeVeDSC(
				new VeDSCDatabaseReader(MyConfiguration.databaseDirectory, databaseFileName));
		
		veDSCDatabaseReader.runAnalysis(
				inputManager.getValue(VeDSCDatabaseEnum.Wing_Aspect_Ratio).getEstimatedValue(), 
				inputManager.getValue(VeDSCDatabaseEnum.Wing_position).getEstimatedValue(), 
				inputManager.getValue(VeDSCDatabaseEnum.Vertical_Tail_Aspect_Ratio).getEstimatedValue(), 
				inputManager.getValue(VeDSCDatabaseEnum.Vertical_Tail_span).doubleValue(SI.METER), 
				inputManager.getValue(VeDSCDatabaseEnum.Horizontal_position_over_vertical).getEstimatedValue(),
				inputManager.getValue(VeDSCDatabaseEnum.Diameter_at_vertical_MAC).doubleValue(SI.METER), 
				inputManager.getValue(VeDSCDatabaseEnum.Tailcone_shape).getEstimatedValue());
		
		double surfaceVertical = LSGeometryCalc.calculateSurface(
				inputManager.getValue(VeDSCDatabaseEnum.Vertical_Tail_span).doubleValue(SI.METER), 
				inputManager.getValue(VeDSCDatabaseEnum.Vertical_Tail_Aspect_Ratio).getEstimatedValue());
		
		double surfaceWing = LSGeometryCalc.calculateSurface(
				inputManager.getValue(VeDSCDatabaseEnum.Wing_span).doubleValue(SI.METER), 
				inputManager.getValue(VeDSCDatabaseEnum.Wing_Aspect_Ratio).getEstimatedValue());
		
		double cN_beta_v = MomentCalc.calcCNbetaVerticalTail(
				inputManager.getValue(VeDSCDatabaseEnum.Wing_Aspect_Ratio).getEstimatedValue(), 
				inputManager.getValue(VeDSCDatabaseEnum.Vertical_Tail_Aspect_Ratio).getEstimatedValue(),
				inputManager.getValue(VeDSCDatabaseEnum.Vertical_Tail_Arm).doubleValue(SI.METER),
				inputManager.getValue(VeDSCDatabaseEnum.Wing_span).doubleValue(SI.METER),
				surfaceWing, surfaceVertical, 
				inputManager.getValue(VeDSCDatabaseEnum.Vertical_Tail_Sweep_at_half_chord).doubleValue(SI.RADIAN),
				inputManager.getValue(VeDSCDatabaseEnum.Vertical_tail_airfoil_lift_curve_slope).getEstimatedValue(),
				inputManager.getValue(VeDSCDatabaseEnum.Mach_number).getEstimatedValue(), 
				veDSCDatabaseReader.getkFv(),
				veDSCDatabaseReader.getkWv(),
				veDSCDatabaseReader.getkHv());
		
		DatabaseIOmanager<VeDSCDatabaseEnum> outputManager = initializeOutputTree(
				veDSCDatabaseReader.getkFv(),
				veDSCDatabaseReader.getkVf(),
				veDSCDatabaseReader.getkWv(),
				veDSCDatabaseReader.getkWf(),
				veDSCDatabaseReader.getkHv(),
				veDSCDatabaseReader.getkHf(),
				cN_beta_v);
		
		writeToFile(outputFileWithPathAndExt, databaseFileName, inputManager, outputManager);
	}
	
	public static void writeDefaultFile(String databaseFileName, String outputFileWithPathAndExt) {
		writeToFile(
				outputFileWithPathAndExt, 
				databaseFileName, 
				initializeInputTree(),
				initializeOutputTree(0., 0., 0., 0., 0., 0., 0.));
	}
}
