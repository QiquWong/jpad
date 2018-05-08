package calculators.stability;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import calculators.aerodynamics.MomentCalc;
import calculators.geometry.LSGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.DirStabEnum;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;

public class StabilityDerivatives {


	private Double 
			wingAspectRatio, 
			verticalTailAspectRatio,
			wingPosition,
			cNb;
	
	private Amount<Length> vTArm, wingSpan, verticalTailSpan, diameterAtVerticalMAC;
	
	private String veDSCDatabaseFileName, fusDesDatabaseFileName;
	private VeDSCDatabaseReader veDSCDatabaseReader;
	private FusDesDatabaseReader fusDesDatabaseReader;

	
	
	// -------------------------  Directional Stability ------------------------- 
	
	public Double DirectionalStabilityDerivative(Aircraft AC,  
			Double horizontalPositionOverVertical, Double tailconeShape){

	wingAspectRatio = AC.getWing().getAspectRatio();
	verticalTailAspectRatio = AC.getVTail().getAspectRatio();
	verticalTailSpan = AC.getVTail().getSpan();
	wingPosition = AC.getWing().getPositionRelativeToAttachment();
	
	veDSCDatabaseReader = DatabaseManager.initializeVeDSC(new VeDSCDatabaseReader(
			MyConfiguration.getDir(FoldersEnum.DATABASE_DIR), veDSCDatabaseFileName));

	fusDesDatabaseReader = DatabaseManager.initializeFusDes(new FusDesDatabaseReader(
			MyConfiguration.getDir(FoldersEnum.DATABASE_DIR), fusDesDatabaseFileName));

	veDSCDatabaseReader.runAnalysis(wingAspectRatio,
									wingPosition,
									verticalTailAspectRatio,
									verticalTailSpan.doubleValue(SI.METER), 
									horizontalPositionOverVertical,
									diameterAtVerticalMAC.doubleValue(SI.METER),
									tailconeShape);
	
	//	fusDesDatabaseReader.runAnalysis(
	//			inputManager.getValue(DirStabEnum.NoseFinenessRatio).getEstimatedValue(),
	//			inputManager.getValue(DirStabEnum.WindshieldAngle).doubleValue(NonSI.DEGREE_ANGLE),
	//			inputManager.getValue(DirStabEnum.FinenessRatio).getEstimatedValue(),
	//			inputManager.getValue(DirStabEnum.TailFinenessRatio).getEstimatedValue(),
	//			inputManager.getValue(DirStabEnum.UpsweepAngle).doubleValue(NonSI.DEGREE_ANGLE),
	//			inputManager.getValue(DirStabEnum.xPercentPositionPole).getEstimatedValue());

	// ---------------------------------------------------------------------------------------------------------------------------------------------
	// TODO: geometric parameters 
	// ---------------------------------------------------------------------------------------------------------------------------------------------
	//	double cNbVertical = MomentCalc.calcCNbetaVerticalTail(Amount wingAspectRatio, Amount verticalTailAspectRatio, Amount<Length> verticalTailArm,
	//												Amount<Length> wingSpan, Double wingSurface, Double verticalSurface, Double verticalSweepAngleAtHalfChord, 
	//												Double verticalTailAirfoilLiftCurveSlope,   
	//			inputManager.getValue(DirStabEnum.Mach_number).getEstimatedValue(), 
	//			veDSCDatabaseReader.getkFv(),
	//			veDSCDatabaseReader.getkWv(),
	//			veDSCDatabaseReader.getkHv())/(180/Math.PI)
	
	
	return cNb;
	}

}
