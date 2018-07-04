package calculators.aerodynamics;

import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jfree.chart.util.ParamChecks;
import org.jscience.physics.amount.Amount;

import com.sun.pisces.Surface;

import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCLAlpha;
import calculators.geometry.FusNacGeometryCalc;
import calculators.stability.StabilityCalculators;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
//import databasesIO.vedscdatabase.VeDSCDatabaseCalc;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.MyArray;

/**
 * A group of static functions for evaluating aerodynamic side force/side force coefficients.
 * 
 * @author cavas
 *
 */
public class SideForceCalc {
	
	private SideForceCalc() {}
	

}
