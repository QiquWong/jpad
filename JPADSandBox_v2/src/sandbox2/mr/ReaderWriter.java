package sandbox2.mr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.SystemOutLogger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jscience.physics.amount.Amount;


import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.PerformanceEnum;
import configuration.enumerations.PerformancePlotEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.DatabaseManager;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXLSUtils;
import standaloneutils.MyXMLReaderUtils;
import sun.misc.Perf;

/*************************************************************************************************************************
 * This class uses the method of JPADXmlReader to read and write data         				    						*
 * @author Manuela Ruocco																								*
 ***********************************************************************************************************************/
public class ReaderWriter{


	@SuppressWarnings("unchecked")
	public void importFromXML(
			String pathToXML,
			StabilityCalculator theStabilityCalculator
			) throws ParserConfigurationException {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading input file data ...\n");

		//------------------------------------------------------------------------------------
		// Setup database(s)

		//		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		//		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		//		String highLiftDatabaseFileName = "HighLiftDatabase.h5";
		//		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		//		HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);

		//---------------------------------------------------------------------------------
		// OPERATING CONDITION:

		theStabilityCalculator.setXCGAircraft((Amount<Length>) reader.getXMLAmountWithUnitByPath("//operating_conditions/x_cg"));
		theStabilityCalculator.setYCGAircraft((Amount<Length>) reader.getXMLAmountWithUnitByPath("//operating_conditions/y_cg"));
		theStabilityCalculator.setZCGAircraft((Amount<Length>) reader.getXMLAmountWithUnitByPath("//operating_conditions/z_cg"));
		theStabilityCalculator.setAltitude((Amount<Length>) reader.getXMLAmountWithUnitByPath("//operating_conditions/altitude"));
		theStabilityCalculator.setMachCurrent(Double.valueOf(reader.getXMLPropertyByPath("//operating_conditions/mach_number")));
		theStabilityCalculator.setReynoldsCurrent(Double.valueOf(reader.getXMLPropertyByPath("//operating_conditions/Reynolds_number")));

		String condition = reader.getXMLPropertyByPath("//operating_conditions/condition");
		System.out.println(" condition " + condition);
		if ( condition.equals("TAKE_OFF") || condition.equals("take_off") )
			theStabilityCalculator.setTheCondition(ConditionEnum.TAKE_OFF);
		if ( condition.equals("CRUISE") || condition.equals("cruise") )
			theStabilityCalculator.setTheCondition(ConditionEnum.CRUISE);
		if ( condition.equals("LANDING") ||condition.equals("landing"))
			theStabilityCalculator.setTheCondition(ConditionEnum.LANDING);

		theStabilityCalculator.setAlphaBodyInitial((Amount<Angle>) reader.getXMLAmountWithUnitByPath("//operating_conditions/alpha_body_initial"));
		theStabilityCalculator.setAlphaBodyFinal((Amount<Angle>) reader.getXMLAmountWithUnitByPath("//operating_conditions/alpha_body_final"));
		theStabilityCalculator.setNumberOfAlphasBody((int)Double.parseDouble((reader.getXMLPropertyByPath("//operating_conditions/number_of_alphas_body"))));

		//---------------------------------------------------------------------------------
		// WING:

		theStabilityCalculator.setXApexWing((Amount<Length>) reader.getXMLAmountWithUnitByPath("//wing/position/x"));
		theStabilityCalculator.setYApexWing((Amount<Length>) reader.getXMLAmountWithUnitByPath("//wing/position/y"));
		theStabilityCalculator.setZApexWing((Amount<Length>) reader.getXMLAmountWithUnitByPath("//wing/position/z"));

		theStabilityCalculator.setWingSurface((Amount<Area>) reader.getXMLAmountWithUnitByPath("//wing/global/surface"));
		theStabilityCalculator.setWingAspectRatio(Double.valueOf(reader.getXMLPropertyByPath("//wing/global/aspect_ratio")));
		theStabilityCalculator.setHTailNumberOfPointSemiSpanWise((int)Double.parseDouble((reader.getXMLPropertyByPath("//wing/global/number_of_point_semispan"))));
		theStabilityCalculator.setWingAdimentionalKinkStation(Double.parseDouble((reader.getXMLPropertyByPath("//wing/global/adimensional_kink_station"))));
		theStabilityCalculator.setWingNumberOfGivenSections((int)Double.parseDouble((reader.getXMLPropertyByPath("//wing/global/number_of_given_sections"))));

		List<String> airfoilFamilyProperty = reader.getXMLPropertiesByPath("//wing/global/mean_airfoil_family");

		//airfoil type
		if(airfoilFamilyProperty.get(0).equals("NACA_4_DIGIT"))
			theStabilityCalculator.setWingMeanAirfoilFamily(AirfoilFamilyEnum.NACA_4_Digit);
		else if(airfoilFamilyProperty.get(0).equals("NACA_5_DIGIT"))
			theStabilityCalculator.setWingMeanAirfoilFamily(AirfoilFamilyEnum.NACA_5_Digit);
		else if(airfoilFamilyProperty.get(0).equals("NACA_63_SERIES"))
			theStabilityCalculator.setWingMeanAirfoilFamily(AirfoilFamilyEnum.NACA_63_Series);
		else if(airfoilFamilyProperty.get(0).equals("NACA_64_SERIES"))
			theStabilityCalculator.setWingMeanAirfoilFamily(AirfoilFamilyEnum.NACA_64_Series);
		else if(airfoilFamilyProperty.get(0).equals("NACA_65_SERIES"))
			theStabilityCalculator.setWingMeanAirfoilFamily(AirfoilFamilyEnum.NACA_65_Series);
		else if(airfoilFamilyProperty.get(0).equals("NACA_66_SERIES"))
			theStabilityCalculator.setWingMeanAirfoilFamily(AirfoilFamilyEnum.NACA_66_Series);
		else if(airfoilFamilyProperty.get(0).equals("BICONVEX"))
			theStabilityCalculator.setWingMeanAirfoilFamily(AirfoilFamilyEnum.BICONVEX);
		else if(airfoilFamilyProperty.get(0).equals("DOUBLE_WEDGE"))
			theStabilityCalculator.setWingMeanAirfoilFamily(AirfoilFamilyEnum.DOUBLE_WEDGE);
		else {
			System.err.println("NO VALID FAMILY TYPE!!");
			return;
		}
		//recognizing airfoil family
		int airfoilFamilyIndex = 0;
		if(theStabilityCalculator.getWingMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_4_Digit) 
			airfoilFamilyIndex = 1;
		else if(theStabilityCalculator.getWingMeanAirfoilFamily()  == AirfoilFamilyEnum.NACA_5_Digit)
			airfoilFamilyIndex = 2;
		else if(theStabilityCalculator.getWingMeanAirfoilFamily()  == AirfoilFamilyEnum.NACA_63_Series)
			airfoilFamilyIndex = 3;
		else if(theStabilityCalculator.getWingMeanAirfoilFamily()  == AirfoilFamilyEnum.NACA_64_Series)
			airfoilFamilyIndex = 4;
		else if(theStabilityCalculator.getWingMeanAirfoilFamily()  == AirfoilFamilyEnum.NACA_65_Series)
			airfoilFamilyIndex = 5;
		else if(theStabilityCalculator.getWingMeanAirfoilFamily()  == AirfoilFamilyEnum.NACA_66_Series)
			airfoilFamilyIndex = 6;
		else if(theStabilityCalculator.getWingMeanAirfoilFamily()  == AirfoilFamilyEnum.BICONVEX)
			airfoilFamilyIndex = 7;
		else if(theStabilityCalculator.getWingMeanAirfoilFamily()  == AirfoilFamilyEnum.DOUBLE_WEDGE)
			airfoilFamilyIndex = 8;

		theStabilityCalculator.setWingMaxThicknessMeanAirfoil(Double.parseDouble((reader.getXMLPropertyByPath("//wing/global/max_thickness_mean_airfoil"))));

		//distributions
		
		theStabilityCalculator.setWingYAdimensionalBreakPoints(reader.readArrayDoubleFromXML("//wing/distribution/geometry/y_adimensional_stations"));
		
		theStabilityCalculator.setWingChordsBreakPoints(reader.readArrayofAmountFromXML("//wing/distribution/geometry/chord_distribution"));
		theStabilityCalculator.setWingXleBreakPoints(reader.readArrayofAmountFromXML("//wing/distribution/geometry/x_le_distribution"));
		theStabilityCalculator.setWingTwistBreakPoints(reader.readArrayofAmountFromXML("//wing/distribution/geometry/twist_distribution"));
		theStabilityCalculator.setWingDihedralBreakPoints(reader.readArrayofAmountFromXML("//wing/distribution/geometry/dihedral_distribution"));
		
		theStabilityCalculator.setWingAlphaZeroLiftBreakPoints(reader.readArrayofAmountFromXML("//wing/distribution/aerodynamics/alpha_zero_lift_distribution"));
		theStabilityCalculator.setWingAlphaStarBreakPoints(reader.readArrayofAmountFromXML("//wing/distribution/aerodynamics/alpha_star_distribution"));
		
		theStabilityCalculator.setWingClMaxBreakPoints(reader.readArrayDoubleFromXML("//wing/distribution/aerodynamics/maximum_lift_coefficient_distribution"));

		//---------------------------------------------------------------------------------
		// HIGH LIFT DEVICES:
		
		if (theStabilityCalculator.)
		
		
		//---------------------------------------------------------------------------------
		// FUSELAGE:

		//---------------------------------------------------------------------------------
		// HORIZONTAL TAIL:

		//---------------------------------------------------------------------------------
		// ELEVATOR:

		//---------------------------------------------------------------------------------
		// ENGINE:



	}
}