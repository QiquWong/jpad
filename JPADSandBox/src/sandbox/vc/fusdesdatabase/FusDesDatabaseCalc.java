package sandbox.vc.fusdesdatabase;

import java.io.File;
import java.util.List;

import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import com.sun.javafx.geom.Area;
import com.sun.pisces.Surface;

import aircraft.components.fuselage.Fuselage;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.MomentCalc;
import calculators.geometry.FusGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.DirStabEnum;
import configuration.enumerations.FusDesDatabaseEnum;
import database.databasefunctions.DatabaseReader;
import database.databasefunctions.aerodynamics.AerodynamicsDatabaseManager;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import standaloneutils.database.io.InputFileReader;
import standaloneutils.database.io.DatabaseFileWriter;
import standaloneutils.database.io.DatabaseIOmanager;

public class FusDesDatabaseCalc {


	public static DatabaseIOmanager<FusDesDatabaseEnum> initializeInputTree() {

		DatabaseIOmanager<FusDesDatabaseEnum> fusIOManager = new DatabaseIOmanager<FusDesDatabaseEnum>();

		fusIOManager.addElement(FusDesDatabaseEnum.MachNumber,Amount.valueOf(0., Unit.ONE), "Mach number");

		fusIOManager.addElement(FusDesDatabaseEnum.ReynoldsNumber,Amount.valueOf(0., Unit.ONE), "Reynolds number");

		fusIOManager.addElement(FusDesDatabaseEnum.MAC,Amount.valueOf(0., SI.METER), "Mean aerodynamic chord");

		fusIOManager.addElement(FusDesDatabaseEnum.WingSpan, Amount.valueOf(0., SI.METER), "Wing span");

		fusIOManager.addElement(FusDesDatabaseEnum.WingSurface, Amount.valueOf(0., SI.SQUARE_METRE), "Wing surface");

		fusIOManager.addElement(FusDesDatabaseEnum.FuselageDiameter, Amount.valueOf(0., SI.METER),"Fuselage diameter");

		fusIOManager.addElement(FusDesDatabaseEnum.FuselageLength, Amount.valueOf(0., SI.METER),"Fuselage length");

		fusIOManager.addElement(FusDesDatabaseEnum.NoseFinenessRatio, Amount.valueOf(0., Unit.ONE), 
				"Nose fineness ratio is the ratio between nose fuselage length and maximum fuselage diameter. It varies between [1.2-1.7].");

		fusIOManager.addElement(FusDesDatabaseEnum.FinenessRatio, Amount.valueOf(0., Unit.ONE), 
				"Fuselage fineness ratio is the ratio between fuselage length and diameter. It varies between [7-12].");

		fusIOManager.addElement(FusDesDatabaseEnum.TailFinenessRatio, Amount.valueOf(0., Unit.ONE), 
				"Tailcone fineness ratio is the ratio between tailcone fuselage length and maximum fuselage diameter. It varies between [2.3-3.0].");

		fusIOManager.addElement(FusDesDatabaseEnum.WindshieldAngle, Amount.valueOf(0., NonSI.DEGREE_ANGLE),
				"Windshield angle is the angle of the front window. It varies between [35 deg - 50 deg].");

		fusIOManager.addElement(FusDesDatabaseEnum.UpsweepAngle, Amount.valueOf(0., NonSI.DEGREE_ANGLE), 
				"Upsweep angle is the upward curvature angle of the aft fuselage. It varies between [10 deg - 18 deg].");

		fusIOManager.addElement(FusDesDatabaseEnum.xPercentPositionPole, Amount.valueOf(0., Unit.ONE),
				"xPositionPole is the position along x-axis of the reference moment point. It can be equal to 0.35, 0.50 or 0.60 which corresponds"
						+ " to 35%, 50% or 60% of fuselage length respectively.");

		return fusIOManager;  
	}

	/**
	 * 
	 * @param kn
	 * @param kc
	 * @param kt
	 * @param CD0
	 * @param CM0FR
	 * @param dCMn
	 * @param dCMt
	 * @param CM0
	 * @param CMaFR
	 * @param dCMan
	 * @param dCMat
	 * @param CMa
	 * @param CNbFR
	 * @param dCNbn
	 * @param dCNbt
	 * @param CNb
	 * @return
	 * 
	 * @author Vincenzo Cusati
	 */
	public static DatabaseIOmanager<FusDesDatabaseEnum> initializeOutputTree(
			double kn, double kc, double kt, double CD0, double CM0FR, double dCMn, double dCMt, double CM0, double CMaFR,
			double dCMan, double dCMat,double CMa, double CNbFR, double dCNbn, double dCNbt, double CNb) {

		DatabaseIOmanager<FusDesDatabaseEnum> fusIOManager = new DatabaseIOmanager<FusDesDatabaseEnum>();

		fusIOManager.addElement(FusDesDatabaseEnum.kn_vs_FRn, Amount.valueOf(kn, Unit.ONE), 
				"The nose shape factor Kn represents the contribution of the nose to the global drag coefficient"
						+" and it takes into account the effect of the nose fineness ratio and of the windshield geometric angle");

		fusIOManager.addElement(FusDesDatabaseEnum.kc_vs_FR, Amount.valueOf(kc, Unit.ONE), 
				"The shape factor Kc which represents the contribution of the cabin to the global drag coefficient and it" 
						+ "takes into account the effect of the cabin length (or cabin fineness ratio)");

		fusIOManager.addElement(FusDesDatabaseEnum.kt_vs_FRt, Amount.valueOf(kt, Unit.ONE), 
				"The tail shape factor Kt which represents the contribution of the tail to the global drag coefficient and it takes"
						+ " into account the effect of the upsweep angle");

		fusIOManager.addElement(FusDesDatabaseEnum.CD0_fuselage, Amount.valueOf(CD0, Unit.ONE), 
				"Fuselage Drag Coefficient estimated with FusDes method");

		fusIOManager.addElement(FusDesDatabaseEnum.CM0_FR_vs_FR, Amount.valueOf(CM0FR,  Unit.ONE), 
				"Pitching moment coefficient as function of fuselage fineness ratio");

		fusIOManager.addElement(FusDesDatabaseEnum.dCM_nose_vs_wshield, Amount.valueOf(dCMn, Unit.ONE),
				"Pitching moment nose correction factor. It depends on windshield angle and on the nose fineness ratio");

		fusIOManager.addElement(FusDesDatabaseEnum.dCM_tail_vs_upsweep, Amount.valueOf(dCMt, Unit.ONE), 
				"Pitching moment tail correction factor. It depends on upsweep angle and on the tail fineness ratio");

		fusIOManager.addElement(FusDesDatabaseEnum.CM0_fuselage, Amount.valueOf(CM0, Unit.ONE), 
				"Pitching moment coefficient estimated with FusDes method");

		fusIOManager.addElement(FusDesDatabaseEnum.CMa_FR_vs_FR, Amount.valueOf(CMaFR, NonSI.DEGREE_ANGLE.inverse()), 
				"Pitching moment derivative as function of fuselage fineness ratio");

		fusIOManager.addElement(FusDesDatabaseEnum.dCMa_nose_vs_wshield, Amount.valueOf(dCMan, NonSI.DEGREE_ANGLE.inverse()), 
				"Pitching moment nose derivative correction factor. It depends on windshield angle and on the nose fineness ratio");

		fusIOManager.addElement(FusDesDatabaseEnum.dCMa_tail_vs_upsweep, Amount.valueOf(dCMat, NonSI.DEGREE_ANGLE.inverse()), 
				"Pitching moment tail derivative correction factor. It depends on upsweep angle and on the tailcone fineness ratio");

		fusIOManager.addElement(FusDesDatabaseEnum.CMa_fuselage, Amount.valueOf(CMa, NonSI.DEGREE_ANGLE.inverse()), 
				"Pitching moment coefficient derivative estimated with FusDes method");

		fusIOManager.addElement(FusDesDatabaseEnum.CNb_FR_vs_FR, Amount.valueOf(CNbFR, NonSI.DEGREE_ANGLE.inverse()), 
				"Yawing moment derivative coefficient as function of fuselage fineness ratio");

		fusIOManager.addElement(FusDesDatabaseEnum.dCNb_nose_vs_FRn, Amount.valueOf(dCNbn, NonSI.DEGREE_ANGLE.inverse()), 
				"Yawing moment nose correction factor. It depends on the nose fineness ratio");

		fusIOManager.addElement(FusDesDatabaseEnum.dCNb_tail_vs_FRt, Amount.valueOf(dCNbt, NonSI.DEGREE_ANGLE.inverse()), 
				"Yawing moment tail correction factor. It depends on the tailcone fineness ratio.");

		fusIOManager.addElement(FusDesDatabaseEnum.CNb_fuselage, Amount.valueOf(CNb, NonSI.DEGREE_ANGLE.inverse()), 
				"Yawing moment coefficient derivative estimated with FusDes method");

		return fusIOManager;  
	} 


	/**
	 * 
	 * @param filenamewithPathAndExt
	 * @return
	 */
	public static DatabaseIOmanager<FusDesDatabaseEnum> readFromFile(String filenamewithPathAndExt) {

		DatabaseIOmanager<FusDesDatabaseEnum> inputManager = initializeInputTree();

		InputFileReader<FusDesDatabaseEnum> _fusDesDatabaseFileReader = 
				new InputFileReader<FusDesDatabaseEnum>(
						filenamewithPathAndExt, inputManager.getTagList());


		List<Amount> valueList = _fusDesDatabaseFileReader.readAmounts();
		inputManager.setValueList(valueList);

		return inputManager;
	}

	/**
	 * 
	 * @param filenamewithPathAndExt
	 * @param databaseFileName
	 * @param inputManager
	 * @param outputManager
	 */
	public static void writeToFile(
			String filenamewithPathAndExt,
			String databaseFileName,
			DatabaseIOmanager<FusDesDatabaseEnum> inputManager,
			DatabaseIOmanager<FusDesDatabaseEnum> outputManager) {

		DatabaseFileWriter<FusDesDatabaseEnum> databaseWriter = new DatabaseFileWriter<FusDesDatabaseEnum>(
				databaseFileName, filenamewithPathAndExt, inputManager, outputManager);

		databaseWriter.writeDocument();
	}

	public static void executeStandaloneFusDes(String databaseFileName, String inputFileNamewithPathAndExt, String outputFileWithPathAndExt){

		DatabaseIOmanager<FusDesDatabaseEnum> inputManager = readFromFile(inputFileNamewithPathAndExt); 

		FusDesDatabaseReader fusDesDatabaseReader = AerodynamicsDatabaseManager.initializeFusDes(new FusDesDatabaseReader(
				MyConfiguration.databaseDirectory, databaseFileName));


		fusDesDatabaseReader.runAnalysis(
				inputManager.getValue(FusDesDatabaseEnum.NoseFinenessRatio).getEstimatedValue(),
				inputManager.getValue(FusDesDatabaseEnum.WindshieldAngle).doubleValue(NonSI.DEGREE_ANGLE),
				inputManager.getValue(FusDesDatabaseEnum.FinenessRatio).getEstimatedValue(),
				inputManager.getValue(FusDesDatabaseEnum.TailFinenessRatio).getEstimatedValue(),
				inputManager.getValue(FusDesDatabaseEnum.UpsweepAngle).doubleValue(NonSI.DEGREE_ANGLE),
				inputManager.getValue(FusDesDatabaseEnum.xPercentPositionPole).getEstimatedValue());

		double noseLength = FusGeometryCalc.calculateFuselageNoseLength(
				inputManager.getValue(FusDesDatabaseEnum.FuselageDiameter).doubleValue(SI.METER),
				inputManager.getValue(FusDesDatabaseEnum.NoseFinenessRatio).getEstimatedValue());

		double tailLength = FusGeometryCalc.calculateFuselageTailLength(
				inputManager.getValue(FusDesDatabaseEnum.FuselageDiameter).doubleValue(SI.METER),
				inputManager.getValue(FusDesDatabaseEnum.TailFinenessRatio).getEstimatedValue());

		double cabinLength = FusGeometryCalc.calculateFuselageCabinLength(
				inputManager.getValue(FusDesDatabaseEnum.FuselageLength).doubleValue(SI.METER),
				noseLength,tailLength);

		double 	sWetNose = FusGeometryCalc.calcFuselageNoseWetSurface(
				inputManager.getValue(FusDesDatabaseEnum.FuselageDiameter).doubleValue(SI.METER), 
				noseLength);

		double 	sWetCabin = FusGeometryCalc.calcFuselageCabinWetSurface(
				inputManager.getValue(FusDesDatabaseEnum.FuselageDiameter).doubleValue(SI.METER), 
				cabinLength);

		double 	sWetTail = FusGeometryCalc.calcFuselageTailWetSurface(
				inputManager.getValue(FusDesDatabaseEnum.FuselageDiameter).doubleValue(SI.METER), 
				tailLength);

		double wetSurface = FusGeometryCalc.calcFuselageWetSurface(
				inputManager.getValue(FusDesDatabaseEnum.FuselageDiameter).doubleValue(SI.METER),
				inputManager.getValue(FusDesDatabaseEnum.FuselageLength).doubleValue(SI.METER), 
				noseLength,cabinLength, tailLength);

		//		double frontSurface = FusGeometryCalc.calculateSfront(
		//				inputManager.getValue(FusDesDatabaseEnum.FuselageDiameter).doubleValue(SI.METER));

		double frontSurface = Fuselage.calculateSfront(inputManager.getValue(FusDesDatabaseEnum.FuselageDiameter).doubleValue(SI.METER));

		double surfaceRatio = frontSurface/inputManager.getValue(FusDesDatabaseEnum.WingSurface).doubleValue(SI.SQUARE_METRE);;

		double cDFlatPlate = AerodynamicCalc.calculateCfTurb(
				inputManager.getValue(FusDesDatabaseEnum.ReynoldsNumber).getEstimatedValue(),
				inputManager.getValue(FusDesDatabaseEnum.MachNumber).getEstimatedValue());

		// Referred to wing surface
		double cDFuselage = DragCalc.dragFusDesCalc(
				fusDesDatabaseReader.getKn(),
				fusDesDatabaseReader.getKc(),
				fusDesDatabaseReader.getKt(),
				wetSurface, sWetNose, sWetCabin, sWetTail,
				frontSurface, cDFlatPlate)*surfaceRatio;

		// Referred to wing surface and MAC
		double cMFuselage = MomentCalc.calcCM0Fuselage(
				fusDesDatabaseReader.getCM0FR(),
				fusDesDatabaseReader.getdCMn(),
				fusDesDatabaseReader.getdCMt()) * surfaceRatio * inputManager.getValue(FusDesDatabaseEnum.FuselageDiameter).doubleValue(SI.METER)/
				 											   	 inputManager.getValue(FusDesDatabaseEnum.MAC).doubleValue(SI.METER);

		// Referred to wing surface and MAC
		double cMaFuselage = MomentCalc.calcCMAlphaFuselage(
				fusDesDatabaseReader.getCMaFR(),
				fusDesDatabaseReader.getdCMan(),
				fusDesDatabaseReader.getdCMat()) * surfaceRatio * inputManager.getValue(FusDesDatabaseEnum.FuselageDiameter).doubleValue(SI.METER)/
			   	 												  inputManager.getValue(FusDesDatabaseEnum.MAC).doubleValue(SI.METER);;

		// Referred to wing surface and span
		double cNbFuselage = MomentCalc.calcCNBetaFuselage(
				fusDesDatabaseReader.getCNbFR(),
				fusDesDatabaseReader.getdCNbn(),
				fusDesDatabaseReader.getdCNbt()) * surfaceRatio * inputManager.getValue(FusDesDatabaseEnum.FuselageDiameter).doubleValue(SI.METER)/
			   	 											      inputManager.getValue(FusDesDatabaseEnum.WingSpan).doubleValue(SI.METER);


		DatabaseIOmanager<FusDesDatabaseEnum> outputManager = initializeOutputTree(
				fusDesDatabaseReader.getKn(),
				fusDesDatabaseReader.getKc(),
				fusDesDatabaseReader.getKt(),
				cDFuselage,
				fusDesDatabaseReader.getCM0FR(),
				fusDesDatabaseReader.getdCMn(), 
				fusDesDatabaseReader.getdCMt(),
				cMFuselage, 
				fusDesDatabaseReader.getCMaFR(),
				fusDesDatabaseReader.getdCMan(), 
				fusDesDatabaseReader.getdCMat(),
				cMaFuselage,
				fusDesDatabaseReader.getCNbFR(),
				fusDesDatabaseReader.getdCNbn(), 
				fusDesDatabaseReader.getdCNbt(),
				cNbFuselage);

		writeToFile(outputFileWithPathAndExt, databaseFileName, inputManager, outputManager);
	}

	//	public static void writeDefaultFile(String databaseFileName, String outputFileWithPathAndExt) {
	//		writeToFile(
	//				outputFileWithPathAndExt, 
	//				databaseFileName, 
	//				initializeInputTree(),
	//				initializeOutputTree(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
	//	}
}

