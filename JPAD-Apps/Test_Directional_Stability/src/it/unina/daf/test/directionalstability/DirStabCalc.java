package it.unina.daf.test.directionalstability;

import java.util.List;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import aircraft.components.fuselage.Fuselage;
import calculators.aerodynamics.MomentCalc;
import calculators.geometry.FusGeometryCalc;
import calculators.geometry.LSGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.DirStabEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicsDatabaseManager;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import standaloneutils.database.io.DatabaseFileReader;
import standaloneutils.database.io.DatabaseFileWriter;
import standaloneutils.database.io.DatabaseIOmanager;

public class DirStabCalc {

	public static DatabaseIOmanager<DirStabEnum> initializeInputTree() {

		DatabaseIOmanager<DirStabEnum> ioManager = new DatabaseIOmanager<DirStabEnum>();

		ioManager.addElement(DirStabEnum.Mach_number, Amount.valueOf(0., Unit.ONE), "Mach number.");

		ioManager.addElement(DirStabEnum.Reynolds_number, Amount.valueOf(0., Unit.ONE), "Reynolds number");

		ioManager.addElement(DirStabEnum.LiftCoefficient, Amount.valueOf(0., Unit.ONE), "Lift coefficient");

		ioManager.addElement(DirStabEnum.Wing_Aspect_Ratio, Amount.valueOf(0., Unit.ONE), 
				"Wing Aspect Ratio. Accepts values in [6,14] range.");

		ioManager.addElement(DirStabEnum.Wing_span, Amount.valueOf(0., SI.METER), "Wing span");

		ioManager.addElement(DirStabEnum.Wing_position, Amount.valueOf(0., Unit.ONE),
				"Between -1 and +1. Low wing = -1; high wing = 1");

		ioManager.addElement(DirStabEnum.Vertical_Tail_Aspect_Ratio, Amount.valueOf(0., Unit.ONE), 
				"Vertical Tail Aspect Ratio. Accepts values in [1,2] range.");

		ioManager.addElement(DirStabEnum.Vertical_Tail_span, Amount.valueOf(0., SI.METER), "Vertical tail span");

		ioManager.addElement(DirStabEnum.Vertical_Tail_Arm, Amount.valueOf(0., SI.METER),
				"Distance of the AC of the vertical tail MAC from the MAC/4 point of the wing");

		ioManager.addElement(DirStabEnum.Vertical_Tail_Sweep_at_half_chord, Amount.valueOf(0., NonSI.DEGREE_ANGLE), 
				"Vertical Tail sweep at half chord.");

		ioManager.addElement(DirStabEnum.Vertical_tail_airfoil_lift_curve_slope, Amount.valueOf(0., SI.RADIAN.inverse()),
				"Vertical tail airfoil lift curve slope.");

		ioManager.addElement(DirStabEnum.Horizontal_position_over_vertical, Amount.valueOf(0., Unit.ONE), 
				"Relative position of the horizontal tail over the vertical tail span, "
						+ "computed from a reference line. Must be in [0,1] range"); 

		ioManager.addElement(DirStabEnum.Diameter_at_vertical_MAC, Amount.valueOf(0., SI.METER),
				"Fuselage diameter at vertical MAC");

		ioManager.addElement(DirStabEnum.Tailcone_shape, Amount.valueOf(0., Unit.ONE), "Fuselage tailcone shape");

		ioManager.addElement(DirStabEnum.FuselageDiameter, Amount.valueOf(0., SI.METER),"Fuselage diameter");

		ioManager.addElement(DirStabEnum.FuselageLength, Amount.valueOf(0., SI.METER),"Fuselage length");

		ioManager.addElement(DirStabEnum.NoseFinenessRatio, Amount.valueOf(0., Unit.ONE), 
				"Nose fineness ratio is the ratio between nose fuselage length and maximum fuselage diameter. It varies between [1.2-1.7].");

		ioManager.addElement(DirStabEnum.FinenessRatio, Amount.valueOf(0., Unit.ONE), 
				"Fuselage fineness ratio is the ratio between fuselage length and diameter. It varies between [7-12].");

		ioManager.addElement(DirStabEnum.TailFinenessRatio, Amount.valueOf(0., Unit.ONE), 
				"Tailcone fineness ratio is the ratio between tailcone fuselage length and maximum fuselage diameter. It varies between [2.3-3.0].");

		ioManager.addElement(DirStabEnum.WindshieldAngle, Amount.valueOf(0., NonSI.DEGREE_ANGLE),
				"Windshield angle is the angle of the front window. It varies between [35 deg - 50 deg].");

		ioManager.addElement(DirStabEnum.UpsweepAngle, Amount.valueOf(0., NonSI.DEGREE_ANGLE), 
				"Upsweep angle is the upward curvature angle of the aft fuselage. It varies between [10 deg - 18 deg].");

		ioManager.addElement(DirStabEnum.xPercentPositionPole, Amount.valueOf(0., Unit.ONE),
				"xPositionPole is the position along x-axis of the reference moment point. It can be equal to 0.35, 0.50 or 0.60 which corresponds"
						+ " to 35%, 50% or 60% of fuselage length respectively.");

		ioManager.addElement(DirStabEnum.WingSweepAngle, Amount.valueOf(0., NonSI.DEGREE_ANGLE), "Sweep angle of the wing computed with respect to c/4 line");

		ioManager.addElement(DirStabEnum.xACwMACratio, Amount.valueOf(0., Unit.ONE), "Ratio between the x-position of the wing aerodynamic center and the mean aerodynamic chord");

		ioManager.addElement(DirStabEnum.xCGMACratio, Amount.valueOf(0., Unit.ONE), "Ratio between the x-position of the center of gravity and the mean aerodynamic chord");

		return ioManager;
	}


	/**
	 * 
	 * @param cNbetaVertical
	 * @param kFv
	 * @param kHv
	 * @param kWv
	 * @param cNbFuselage
	 * @param kHf
	 * @param kWf
	 * @param kVf
	 * @param cNbWing
	 * @param CNbAC
	 * @return
	 * 
	 * @author Vincenzo Cusati
	 */
	public static DatabaseIOmanager<DirStabEnum> initializeOutputTree(double cNbetaVertical,
			double kFv, double kHv, double kWv, double cNbFuselage, double kHf, double kWf, double kVf, 
			 double cNbWing, double CNbAC) {

		DatabaseIOmanager<DirStabEnum> ioManager = new DatabaseIOmanager<DirStabEnum>();

		ioManager.addElement(DirStabEnum.CN_beta_vertical, Amount.valueOf(cNbetaVertical, NonSI.DEGREE_ANGLE.inverse()),
				"Yawing moment coefficient derivative, vertical tail contribution");

		ioManager.addElement(DirStabEnum.KFv_vs_bv_over_dfv, Amount.valueOf(kFv, Unit.ONE), 
				"Aerodynamic interference factor of the fuselage on the vertical tail "
						+ "as a function of the ratio of the vertical tail span over the fuselage diameter "
						+ "taken at the aerodynamic center on the MAC of the vertical tail");

		ioManager.addElement(DirStabEnum.KHv_vs_zh_over_bv1, Amount.valueOf(kHv, Unit.ONE),
				"Interference factor of the horizontal tail on the vertical tail");

		ioManager.addElement(DirStabEnum.KWv_vs_zw_over_rf, Amount.valueOf(kWv, Unit.ONE),
				"Interference factor of the wing on the vertical tail");


		ioManager.addElement(DirStabEnum.CNb_fuselage, Amount.valueOf(cNbFuselage, NonSI.DEGREE_ANGLE.inverse()), 
				"Fusealge yawing moment coefficient derivative");

		ioManager.addElement(DirStabEnum.KHf_vs_zh_over_bv1, Amount.valueOf(kHf, Unit.ONE),
				"Interference factor of the horizontal tail on the fuselage");

		ioManager.addElement(DirStabEnum.KWf_vs_zw_over_rf, Amount.valueOf(kWf, Unit.ONE),
				"Interference factor of the wing on the fuselage");

		ioManager.addElement(DirStabEnum.KVf_vs_bv_over_dfv, Amount.valueOf(kVf, Unit.ONE), 
				"Interference factor of the vertical tail on the fuselage");

		ioManager.addElement(DirStabEnum.CNb_wing, Amount.valueOf(cNbWing, NonSI.DEGREE_ANGLE.inverse()), 
				"Wing yawing moment coefficient derivative");

		ioManager.addElement(DirStabEnum.CNb_AC, Amount.valueOf(CNbAC, NonSI.DEGREE_ANGLE.inverse()), 
				"Aircraft yawing moment coefficient derivative");

		return ioManager;  
	} 


	/**
	 * 
	 * @param filenamewithPathAndExt
	 * @return
	 */
	public static DatabaseIOmanager<DirStabEnum> readFromFile(String filenamewithPathAndExt) {

		DatabaseIOmanager<DirStabEnum> inputManager = initializeInputTree();

		DatabaseFileReader<DirStabEnum> _dirStabDatabaseFileReader = 
				new DatabaseFileReader<DirStabEnum>(
						filenamewithPathAndExt, inputManager.getTagList());

		// System.out.println("--> File: " + filenamewithPathAndExt);

		List<Amount> valueList = _dirStabDatabaseFileReader.readDatabase();
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
			DatabaseIOmanager<DirStabEnum> inputManager,
			DatabaseIOmanager<DirStabEnum> outputManager) {

		DatabaseFileWriter<DirStabEnum> databaseWriter = new DatabaseFileWriter<DirStabEnum>(
//				databaseFileName, 
				"DirectionalStability", // This string is that written in the <rootElement> in the output file for Directional Stability xml file 
				filenamewithPathAndExt, inputManager, outputManager);

		databaseWriter.writeDocument();
	}


	public static void executeStandaloneDirStab(String veDSCDatabaseFileName, String fusDesDatabaseFileName, 
			String inputFileNamewithPathAndExt, String outputFileWithPathAndExt){

		DatabaseIOmanager<DirStabEnum> inputManager = readFromFile(inputFileNamewithPathAndExt); 

		VeDSCDatabaseReader veDSCDatabaseReader = AerodynamicsDatabaseManager.initializeVeDSC(new VeDSCDatabaseReader(
				MyConfiguration.getDir(FoldersEnum.DATABASE_DIR), veDSCDatabaseFileName));

		FusDesDatabaseReader fusDesDatabaseReader = AerodynamicsDatabaseManager.initializeFusDes(new FusDesDatabaseReader(
				MyConfiguration.getDir(FoldersEnum.DATABASE_DIR), fusDesDatabaseFileName));

		veDSCDatabaseReader.runAnalysis(
				inputManager.getValue(DirStabEnum.Wing_Aspect_Ratio).getEstimatedValue(), 
				inputManager.getValue(DirStabEnum.Wing_position).getEstimatedValue(), 
				inputManager.getValue(DirStabEnum.Vertical_Tail_Aspect_Ratio).getEstimatedValue(), 
				inputManager.getValue(DirStabEnum.Vertical_Tail_span).doubleValue(SI.METER), 
				inputManager.getValue(DirStabEnum.Horizontal_position_over_vertical).getEstimatedValue(),
				inputManager.getValue(DirStabEnum.Diameter_at_vertical_MAC).doubleValue(SI.METER), 
				inputManager.getValue(DirStabEnum.Tailcone_shape).getEstimatedValue());


		fusDesDatabaseReader.runAnalysis(
				inputManager.getValue(DirStabEnum.NoseFinenessRatio).getEstimatedValue(),
				inputManager.getValue(DirStabEnum.WindshieldAngle).doubleValue(NonSI.DEGREE_ANGLE),
				inputManager.getValue(DirStabEnum.FinenessRatio).getEstimatedValue(),
				inputManager.getValue(DirStabEnum.TailFinenessRatio).getEstimatedValue(),
				inputManager.getValue(DirStabEnum.UpsweepAngle).doubleValue(NonSI.DEGREE_ANGLE),
				inputManager.getValue(DirStabEnum.xPercentPositionPole).getEstimatedValue());



		//-------------  Start - Calculation vertical geoemtry parameters --------------------------------------
		double surfaceVertical = LSGeometryCalc.calculateSurface(
				inputManager.getValue(DirStabEnum.Vertical_Tail_span).doubleValue(SI.METER), 
				inputManager.getValue(DirStabEnum.Vertical_Tail_Aspect_Ratio).getEstimatedValue());

		double surfaceWing = LSGeometryCalc.calculateSurface(
				inputManager.getValue(DirStabEnum.Wing_span).doubleValue(SI.METER), 
				inputManager.getValue(DirStabEnum.Wing_Aspect_Ratio).getEstimatedValue());
		//-------------  End - Calculation vertical geoemtry parameters --------------------------------------



		//-------------  Start - Calculation fusealge geoemtry parameters --------------------------------------
		double noseLength = FusGeometryCalc.calculateFuselageNoseLength(
				inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER),
				inputManager.getValue(DirStabEnum.NoseFinenessRatio).getEstimatedValue());

		double tailLength = FusGeometryCalc.calculateFuselageTailLength(
				inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER),
				inputManager.getValue(DirStabEnum.TailFinenessRatio).getEstimatedValue());

		double cabinLength = FusGeometryCalc.calculateFuselageCabinLength(
				inputManager.getValue(DirStabEnum.FuselageLength).doubleValue(SI.METER),
				noseLength,tailLength);

		double 	sWetNose = FusGeometryCalc.calcFuselageNoseWetSurface(
				inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER), 
				noseLength);

		double 	sWetCabin = FusGeometryCalc.calcFuselageCabinWetSurface(
				inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER), 
				cabinLength);

		double 	sWetTail = FusGeometryCalc.calcFuselageTailWetSurface(
				inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER), 
				tailLength);

		double wetSurface = FusGeometryCalc.calcFuselageWetSurface(
				inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER),
				inputManager.getValue(DirStabEnum.FuselageLength).doubleValue(SI.METER), 
				noseLength,cabinLength, tailLength);

		double frontSurface = Fuselage.calculateSfront(
				inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER));

		double wingSurface = Math.pow(inputManager.getValue(DirStabEnum.Wing_span).doubleValue(SI.METER),2)/
				inputManager.getValue(DirStabEnum.Wing_Aspect_Ratio).doubleValue(Unit.ONE);

		double surfaceRatio = frontSurface/wingSurface;
		//-------------  End - Calculation fusealge geoemtry parameters --------------------------------------


		// cNb vertical [1/deg]
		double cNbVertical = MomentCalc.calcCNbetaVerticalTail(
				inputManager.getValue(DirStabEnum.Wing_Aspect_Ratio).getEstimatedValue(), 
				inputManager.getValue(DirStabEnum.Vertical_Tail_Aspect_Ratio).getEstimatedValue(),
				inputManager.getValue(DirStabEnum.Vertical_Tail_Arm).doubleValue(SI.METER),
				inputManager.getValue(DirStabEnum.Wing_span).doubleValue(SI.METER),
				surfaceWing, surfaceVertical, 
				inputManager.getValue(DirStabEnum.Vertical_Tail_Sweep_at_half_chord).doubleValue(SI.RADIAN),
				inputManager.getValue(DirStabEnum.Vertical_tail_airfoil_lift_curve_slope).getEstimatedValue(),
				inputManager.getValue(DirStabEnum.Mach_number).getEstimatedValue(), 
				veDSCDatabaseReader.getkFv(),
				veDSCDatabaseReader.getkWv(),
				veDSCDatabaseReader.getkHv())/(180/Math.PI);



		// cNbFuselage referred to wing surface and span [1/deg] estimated with FusDes method
		double cNbFuselageFusDes = MomentCalc.calcCNBetaFusalage(
				fusDesDatabaseReader.getCNbFR(),
				fusDesDatabaseReader.getdCNbn(),
				fusDesDatabaseReader.getdCNbt())* surfaceRatio * inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER)/
																 inputManager.getValue(DirStabEnum.Wing_span).doubleValue(SI.METER);

		// cNbFuselage referred to wing surface and span [1/deg] estimated with VeDSC method 
		double cNbFuselage = cNbFuselageFusDes * veDSCDatabaseReader.getkVf()*
												 veDSCDatabaseReader.getkHf()*
												 veDSCDatabaseReader.getkWf();

		// cNbWing referred to wing surface and span [1/deg] 
		double cNbWing = MomentCalc.calcCNBetaWing(inputManager.getValue(DirStabEnum.LiftCoefficient).doubleValue(Unit.ONE),
												   inputManager.getValue(DirStabEnum.WingSweepAngle).doubleValue(NonSI.DEGREE_ANGLE),
												   inputManager.getValue(DirStabEnum.Wing_Aspect_Ratio).doubleValue(Unit.ONE),
												   inputManager.getValue(DirStabEnum.xACwMACratio).doubleValue(Unit.ONE),
												   inputManager.getValue(DirStabEnum.xCGMACratio).doubleValue(Unit.ONE)
												  );

		double cNbAC= MomentCalc.calcCNBetaAC(cNbVertical, cNbFuselage, cNbWing);

		DatabaseIOmanager<DirStabEnum> outputManager = initializeOutputTree(
				cNbVertical,
				veDSCDatabaseReader.getkFv(),
				veDSCDatabaseReader.getkHv(),
				veDSCDatabaseReader.getkWv(),
				cNbFuselage,
				veDSCDatabaseReader.getkHf(),
				veDSCDatabaseReader.getkWf(),
				veDSCDatabaseReader.getkVf(),
				cNbWing,
				cNbAC);

		writeToFile(outputFileWithPathAndExt, veDSCDatabaseFileName, inputManager, outputManager);
		writeToFile(outputFileWithPathAndExt, fusDesDatabaseFileName, inputManager, outputManager);

	}
	

	/**
	 * @overload
	 * 
	 * This method allows to define an arbitrary directory folder
	 * for databases. 
	 * 
	 * @param veDSCDatabaseFileName
	 * @param fusDesDatabaseFileName
	 * @param databaseDirectory is the directory where the databases are. 
	 * @param inputFileNamewithPathAndExt
	 * @param outputFileWithPathAndExt
	 * 
	 * @author Vincenzo Cusati
	 */
	
	public static void executeStandaloneDirStab(String veDSCDatabaseFileName, String fusDesDatabaseFileName, String databaseDirectory, 
			String inputFileNamewithPathAndExt, String outputFileWithPathAndExt){

		DatabaseIOmanager<DirStabEnum> inputManager = readFromFile(inputFileNamewithPathAndExt); 

		System.out.println("---> dir: " + databaseDirectory);
		System.out.println("---> name: " + veDSCDatabaseFileName);
		
		VeDSCDatabaseReader veDSCDatabaseReader = AerodynamicsDatabaseManager.initializeVeDSC(new VeDSCDatabaseReader(
				databaseDirectory, veDSCDatabaseFileName),
				databaseDirectory);

		FusDesDatabaseReader fusDesDatabaseReader = AerodynamicsDatabaseManager.initializeFusDes(new FusDesDatabaseReader(
				databaseDirectory, fusDesDatabaseFileName), 
				databaseDirectory);

		veDSCDatabaseReader.runAnalysis(
				inputManager.getValue(DirStabEnum.Wing_Aspect_Ratio).getEstimatedValue(), 
				inputManager.getValue(DirStabEnum.Wing_position).getEstimatedValue(), 
				inputManager.getValue(DirStabEnum.Vertical_Tail_Aspect_Ratio).getEstimatedValue(), 
				inputManager.getValue(DirStabEnum.Vertical_Tail_span).doubleValue(SI.METER), 
				inputManager.getValue(DirStabEnum.Horizontal_position_over_vertical).getEstimatedValue(),
				inputManager.getValue(DirStabEnum.Diameter_at_vertical_MAC).doubleValue(SI.METER), 
				inputManager.getValue(DirStabEnum.Tailcone_shape).getEstimatedValue());


		fusDesDatabaseReader.runAnalysis(
				inputManager.getValue(DirStabEnum.NoseFinenessRatio).getEstimatedValue(),
				inputManager.getValue(DirStabEnum.WindshieldAngle).doubleValue(NonSI.DEGREE_ANGLE),
				inputManager.getValue(DirStabEnum.FinenessRatio).getEstimatedValue(),
				inputManager.getValue(DirStabEnum.TailFinenessRatio).getEstimatedValue(),
				inputManager.getValue(DirStabEnum.UpsweepAngle).doubleValue(NonSI.DEGREE_ANGLE),
				inputManager.getValue(DirStabEnum.xPercentPositionPole).getEstimatedValue());



		//-------------  Start - Calculation vertical geoemtry parameters --------------------------------------
		double surfaceVertical = LSGeometryCalc.calculateSurface(
				inputManager.getValue(DirStabEnum.Vertical_Tail_span).doubleValue(SI.METER), 
				inputManager.getValue(DirStabEnum.Vertical_Tail_Aspect_Ratio).getEstimatedValue());

		double surfaceWing = LSGeometryCalc.calculateSurface(
				inputManager.getValue(DirStabEnum.Wing_span).doubleValue(SI.METER), 
				inputManager.getValue(DirStabEnum.Wing_Aspect_Ratio).getEstimatedValue());
		//-------------  End - Calculation vertical geoemtry parameters --------------------------------------



		//-------------  Start - Calculation fusealge geoemtry parameters --------------------------------------
		double noseLength = FusGeometryCalc.calculateFuselageNoseLength(
				inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER),
				inputManager.getValue(DirStabEnum.NoseFinenessRatio).getEstimatedValue());

		double tailLength = FusGeometryCalc.calculateFuselageTailLength(
				inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER),
				inputManager.getValue(DirStabEnum.TailFinenessRatio).getEstimatedValue());

		double cabinLength = FusGeometryCalc.calculateFuselageCabinLength(
				inputManager.getValue(DirStabEnum.FuselageLength).doubleValue(SI.METER),
				noseLength,tailLength);

		double 	sWetNose = FusGeometryCalc.calcFuselageNoseWetSurface(
				inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER), 
				noseLength);

		double 	sWetCabin = FusGeometryCalc.calcFuselageCabinWetSurface(
				inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER), 
				cabinLength);

		double 	sWetTail = FusGeometryCalc.calcFuselageTailWetSurface(
				inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER), 
				tailLength);

		double wetSurface = FusGeometryCalc.calcFuselageWetSurface(
				inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER),
				inputManager.getValue(DirStabEnum.FuselageLength).doubleValue(SI.METER), 
				noseLength,cabinLength, tailLength);

		double frontSurface = Fuselage.calculateSfront(
				inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER));

		double wingSurface = Math.pow(inputManager.getValue(DirStabEnum.Wing_span).doubleValue(SI.METER),2)/
				inputManager.getValue(DirStabEnum.Wing_Aspect_Ratio).doubleValue(Unit.ONE);

		double surfaceRatio = frontSurface/wingSurface;
		//-------------  End - Calculation fusealge geoemtry parameters --------------------------------------


		// cNb vertical [1/deg]
		double cNbVertical = MomentCalc.calcCNbetaVerticalTail(
				inputManager.getValue(DirStabEnum.Wing_Aspect_Ratio).getEstimatedValue(), 
				inputManager.getValue(DirStabEnum.Vertical_Tail_Aspect_Ratio).getEstimatedValue(),
				inputManager.getValue(DirStabEnum.Vertical_Tail_Arm).doubleValue(SI.METER),
				inputManager.getValue(DirStabEnum.Wing_span).doubleValue(SI.METER),
				surfaceWing, surfaceVertical, 
				inputManager.getValue(DirStabEnum.Vertical_Tail_Sweep_at_half_chord).doubleValue(SI.RADIAN),
				inputManager.getValue(DirStabEnum.Vertical_tail_airfoil_lift_curve_slope).getEstimatedValue(),
				inputManager.getValue(DirStabEnum.Mach_number).getEstimatedValue(), 
				veDSCDatabaseReader.getkFv(),
				veDSCDatabaseReader.getkWv(),
				veDSCDatabaseReader.getkHv())/(180/Math.PI);



		// cNbFuselage referred to wing surface and span [1/deg] estimated with FusDes method
		double cNbFuselageFusDes = MomentCalc.calcCNBetaFusalage(
				fusDesDatabaseReader.getCNbFR(),
				fusDesDatabaseReader.getdCNbn(),
				fusDesDatabaseReader.getdCNbt())* surfaceRatio * inputManager.getValue(DirStabEnum.FuselageDiameter).doubleValue(SI.METER)/
																 inputManager.getValue(DirStabEnum.Wing_span).doubleValue(SI.METER);

		// cNbFuselage referred to wing surface and span [1/deg] estimated with VeDSC method 
		double cNbFuselage = cNbFuselageFusDes * veDSCDatabaseReader.getkVf()*
												 veDSCDatabaseReader.getkHf()*
												 veDSCDatabaseReader.getkWf();

		// cNbWing referred to wing surface and span [1/deg] 
		double cNbWing = MomentCalc.calcCNBetaWing(inputManager.getValue(DirStabEnum.LiftCoefficient).doubleValue(Unit.ONE),
												   inputManager.getValue(DirStabEnum.WingSweepAngle).doubleValue(NonSI.DEGREE_ANGLE),
												   inputManager.getValue(DirStabEnum.Wing_Aspect_Ratio).doubleValue(Unit.ONE),
												   inputManager.getValue(DirStabEnum.xACwMACratio).doubleValue(Unit.ONE),
												   inputManager.getValue(DirStabEnum.xCGMACratio).doubleValue(Unit.ONE)
												  );

		double cNbAC= MomentCalc.calcCNBetaAC(cNbVertical, cNbFuselage, cNbWing);

		DatabaseIOmanager<DirStabEnum> outputManager = initializeOutputTree(
				cNbVertical,
				veDSCDatabaseReader.getkFv(),
				veDSCDatabaseReader.getkHv(),
				veDSCDatabaseReader.getkWv(),
				cNbFuselage,
				veDSCDatabaseReader.getkHf(),
				veDSCDatabaseReader.getkWf(),
				veDSCDatabaseReader.getkVf(),
				cNbWing,
				cNbAC);

		writeToFile(outputFileWithPathAndExt, veDSCDatabaseFileName, inputManager, outputManager);
		writeToFile(outputFileWithPathAndExt, fusDesDatabaseFileName, inputManager, outputManager);

	}
}
