package sandbox.vc.CompleteAC;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.DirStabEnum;
import standaloneutils.database.io.DatabaseIOmanager;

public class InitializeGeometryFromXml {
	
	public static void fuselageDataFromXml() {

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

}
