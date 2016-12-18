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
import org.eclipse.ui.internal.themes.ThemesExtension;
import org.jscience.physics.amount.Amount;


import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.AerodynamicAndStabilityPlotEnum;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
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
			StabilityExecutableManager theStabilityCalculator
			) throws ParserConfigurationException {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading input file data ...\n");

		//------------------------------------------------------------------------------------
		// Setup database(s)

		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		String highLiftDatabaseFileName = "HighLiftDatabase.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);

		//-------------------------------------------------------------------------------
		// DOWNWASH

		String DownwashString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@downwash");
		if(DownwashString.equalsIgnoreCase("CONSTANT"))
			theStabilityCalculator.setDownwashConstant(Boolean.TRUE);
		else if (DownwashString.equalsIgnoreCase("variable"))
			theStabilityCalculator.setDownwashConstant(Boolean.FALSE);


		//---------------------------------------------------------------------------------
		// OPERATING CONDITION:
		
		theStabilityCalculator.setAircraftName(reader.getXMLPropertyByPath("//aircraft_name"));
		theStabilityCalculator.setXCGAircraft((Amount<Length>) reader.getXMLAmountWithUnitByPath("//operating_conditions/x_cg"));
		theStabilityCalculator.setYCGAircraft((Amount<Length>) reader.getXMLAmountWithUnitByPath("//operating_conditions/y_cg"));
		theStabilityCalculator.setZCGAircraft((Amount<Length>) reader.getXMLAmountWithUnitByPath("//operating_conditions/z_cg"));
		theStabilityCalculator.setAltitude((Amount<Length>) reader.getXMLAmountWithUnitByPath("//operating_conditions/altitude"));
		theStabilityCalculator.setMachCurrent(Double.valueOf(reader.getXMLPropertyByPath("//operating_conditions/mach_number")));
		theStabilityCalculator.setReynoldsCurrent(Double.valueOf(reader.getXMLPropertyByPath("//operating_conditions/Reynolds_number")));

		theStabilityCalculator.setWingFinalMomentumPole(0.25);
		theStabilityCalculator.setHTailFinalMomentumPole(0.25);
		theStabilityCalculator.setAlphaWingForDistribution(reader.readArrayofAmountFromXML("//operating_conditions/alpha_wing_array_for_distribution"));
		theStabilityCalculator.setAlphaHorizontalTailForDistribution(reader.readArrayofAmountFromXML("//operating_conditions/alpha_horizontal_tail_array_for_distribution"));
		
		String condition = reader.getXMLPropertyByPath("//operating_conditions/condition");

		if ( condition.equals("TAKE_OFF") || condition.equals("take_off") )
			theStabilityCalculator.setTheCondition(ConditionEnum.TAKE_OFF);
		if ( condition.equals("CRUISE") || condition.equals("cruise") )
			theStabilityCalculator.setTheCondition(ConditionEnum.CRUISE);
		if ( condition.equals("LANDING") ||condition.equals("landing"))
			theStabilityCalculator.setTheCondition(ConditionEnum.LANDING);

		theStabilityCalculator.setAlphaBodyInitial((Amount<Angle>) reader.getXMLAmountWithUnitByPath("//operating_conditions/alpha_body_initial"));
		theStabilityCalculator.setAlphaBodyFinal((Amount<Angle>) reader.getXMLAmountWithUnitByPath("//operating_conditions/alpha_body_final"));
		theStabilityCalculator.setNumberOfAlphasBody((int)Double.parseDouble((reader.getXMLPropertyByPath("//operating_conditions/number_of_alphas_body"))));
		theStabilityCalculator.setWingMomentumPole(reader.readArrayDoubleFromXMLSplit("//operating_conditions/wing_momentum_pole"));
		theStabilityCalculator.setHTailMomentumPole(reader.readArrayDoubleFromXMLSplit("//operating_conditions/horizontal_tail_momentum_pole"));
		//---------------------------------------------------------------------------------
		// WING:

		theStabilityCalculator.setXApexWing((Amount<Length>) reader.getXMLAmountWithUnitByPath("//wing/position/x"));
		theStabilityCalculator.setYApexWing((Amount<Length>) reader.getXMLAmountWithUnitByPath("//wing/position/y"));
		theStabilityCalculator.setZApexWing((Amount<Length>) reader.getXMLAmountWithUnitByPath("//wing/position/z"));

		theStabilityCalculator.setWingSurface((Amount<Area>) reader.getXMLAmountWithUnitByPath("//wing/global/surface"));
		theStabilityCalculator.setWingAngleOfIncidence((Amount<Angle>) reader.getXMLAmountWithUnitByPath("//wing/global/angle_of_incidence"));
		theStabilityCalculator.setWingAspectRatio(Double.valueOf(reader.getXMLPropertyByPath("//wing/global/aspect_ratio")));
		theStabilityCalculator.setWingNumberOfPointSemiSpanWise((int)Double.parseDouble((reader.getXMLPropertyByPath("//wing/global/number_of_point_semispan"))));
		theStabilityCalculator.setWingAdimentionalKinkStation(Double.parseDouble((reader.getXMLPropertyByPath("//wing/global/adimensional_kink_station"))));
		theStabilityCalculator.setWingNumberOfGivenSections((int)Double.parseDouble((reader.getXMLPropertyByPath("//wing/global/number_of_given_sections"))));
		theStabilityCalculator.setWingSweepQuarterChord((Amount<Angle>) reader.getXMLAmountWithUnitByPath("//wing/global/sweep_quarter_chord"));
		theStabilityCalculator.setWingSweepLE((Amount<Angle>) reader.getXMLAmountWithUnitByPath("//wing/global/sweep_LE"));
		
		
		
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

		//distributions

		theStabilityCalculator.setWingYAdimensionalBreakPoints(reader.readArrayDoubleFromXMLSplit("//wing/distribution/geometry/y_adimensional_stations"));

		theStabilityCalculator.setWingChordsBreakPoints(reader.readArrayofAmountFromXML("//wing/distribution/geometry/chord_distribution"));
		theStabilityCalculator.setWingXleBreakPoints(reader.readArrayofAmountFromXML("//wing/distribution/geometry/x_le_distribution"));
		theStabilityCalculator.setWingTwistBreakPoints(reader.readArrayofAmountFromXML("//wing/distribution/geometry/twist_distribution"));
		theStabilityCalculator.setWingDihedralBreakPoints(reader.readArrayofAmountFromXML("//wing/distribution/geometry/dihedral_distribution"));
		theStabilityCalculator.setWingAlphaZeroLiftBreakPoints(reader.readArrayofAmountFromXML("//wing/distribution/aerodynamics/alpha_zero_lift_distribution"));
		theStabilityCalculator.setWingAlphaStarBreakPoints(reader.readArrayofAmountFromXML("//wing/distribution/aerodynamics/alpha_star_distribution"));
		theStabilityCalculator.setWingClMaxBreakPoints(reader.readArrayDoubleFromXMLSplit("//wing/distribution/aerodynamics/maximum_lift_coefficient_distribution"));
		theStabilityCalculator.setWingClAlphaBreakPointsDeg(reader.readArrayDoubleFromXMLSplit("//wing/distribution/aerodynamics/cl_alpha_distribution"));
		theStabilityCalculator.setWingMaxThicknessBreakPoints(reader.readArrayDoubleFromXMLSplit("//wing/distribution/geometry/max_thickness_airfoil"));
		theStabilityCalculator.setWingLERadiusBreakPoints(reader.readArrayofAmountFromXML("//wing/distribution/geometry/radius_leading_edge"));
		
		theStabilityCalculator.setWingXACBreakPoints(reader.readArrayDoubleFromXMLSplit("//wing/distribution/aerodynamics/x_ac_referred_to_chord"));
		theStabilityCalculator.setWingCmACBreakPoints(reader.readArrayDoubleFromXMLSplit("//wing/distribution/aerodynamics/c_m_ac"));
		//---------------------------------------------------------------------------------
		// HIGH LIFT DEVICES:

		if (theStabilityCalculator.getTheCondition() == ConditionEnum.TAKE_OFF || theStabilityCalculator.getTheCondition() == ConditionEnum.LANDING) {

			//flaps
			theStabilityCalculator.setWingNumberOfFlaps(reader.getXMLPropertiesByPath("//wing/high_lift_devices/flaps/flap/flap_type").size());

			//initialize array
			theStabilityCalculator.setWingFlapType(new ArrayList<>());
			theStabilityCalculator.setWingFlapCfC(new ArrayList<>());
			theStabilityCalculator.setWingDeltaFlap(new ArrayList<>());
			theStabilityCalculator.setWingEtaInFlap(new ArrayList<>());
			theStabilityCalculator.setWingEtaOutFlap(new ArrayList<>());

			//reading flap type
			List<String> flapsType = reader.getXMLPropertiesByPath("//wing/high_lift_devices/flaps/flap/flap_type");
			for(int i=0; i<flapsType.size(); i++) {
				if(flapsType.get(i).equals("SINGLE_SLOTTED"))
					theStabilityCalculator.getWingFlapType().add(FlapTypeEnum.SINGLE_SLOTTED);
				else if(flapsType.get(i).equals("DOUBLE_SLOTTED"))
					theStabilityCalculator.getWingFlapType().add(FlapTypeEnum.DOUBLE_SLOTTED);
				else if(flapsType.get(i).equals("PLAIN"))
					theStabilityCalculator.getWingFlapType().add(FlapTypeEnum.PLAIN);
				else if(flapsType.get(i).equals("FOWLER"))
					theStabilityCalculator.getWingFlapType().add(FlapTypeEnum.FOWLER);
				else if(flapsType.get(i).equals("TRIPLE_SLOTTED"))
					theStabilityCalculator.getWingFlapType().add(FlapTypeEnum.TRIPLE_SLOTTED);
				else {
					System.err.println("NO VALID FLAP TYPE!!");
					return;
				}	
			}

			for(int i=0; i<flapsType.size(); i++) {
				theStabilityCalculator.getWingFlapCfC().add(Double.parseDouble((reader.getXMLPropertiesByPath("//wing/high_lift_devices/flaps/flap/flap_chord_ratio")).get(i)));
			}

			for(int i=0; i<flapsType.size(); i++) {
				theStabilityCalculator.getWingDeltaFlap().add(
						Amount.valueOf(
								Double.parseDouble(reader.getXMLPropertiesByPath("//wing/high_lift_devices/flaps/flap/flap_deflection").get(i))
								,NonSI.DEGREE_ANGLE)
						);
			}

			for(int i=0; i<flapsType.size(); i++) {
				theStabilityCalculator.getWingEtaInFlap().add(Double.parseDouble((reader.getXMLPropertiesByPath("//wing/high_lift_devices/flaps/flap/flap_non_dimensional_inner_station")).get(i)));
			}

			for(int i=0; i<flapsType.size(); i++) {
				theStabilityCalculator.getWingEtaOutFlap().add(Double.parseDouble((reader.getXMLPropertiesByPath("//wing/high_lift_devices/flaps/flap/flap_non_dimensional_outer_station")).get(i)));
			}


			//Slats
			theStabilityCalculator.setWingNumberOfSlats(reader.getXMLPropertiesByPath("//wing/high_lift_devices/slats/slat/slat_deflection").size());

			//initialize array
			theStabilityCalculator.setWingSlatCsC(new ArrayList<>());
			theStabilityCalculator.setWingCExtCSlat(new ArrayList<>());
			theStabilityCalculator.setWingEtaInSlat(new ArrayList<>());
			theStabilityCalculator.setWingEtaOutSlat(new ArrayList<>());
			theStabilityCalculator.setWingDeltaSlat(new ArrayList<>());

			// Values
			for(int i=0; i<theStabilityCalculator.getWingNumberOfSlats(); i++) {
				theStabilityCalculator.getWingSlatCsC().add(Double.parseDouble((reader.getXMLPropertiesByPath("//wing/high_lift_devices/slats/slat/slat_chord_ratio")).get(i)));
			}

			for(int i=0; i<theStabilityCalculator.getWingNumberOfSlats(); i++) {
				theStabilityCalculator.getWingCExtCSlat().add(Double.parseDouble((reader.getXMLPropertiesByPath("//wing/high_lift_devices/slats/slat/slat_extension_ratio")).get(i)));
			}

			for(int i=0; i<theStabilityCalculator.getWingNumberOfSlats(); i++) {
				theStabilityCalculator.getWingEtaInSlat().add(Double.parseDouble((reader.getXMLPropertiesByPath("//wing/high_lift_devices/slats/slat/slat_non_dimensional_inner_station")).get(i)));
			}

			for(int i=0; i<theStabilityCalculator.getWingNumberOfSlats(); i++) {
				theStabilityCalculator.getWingEtaOutSlat().add(Double.parseDouble((reader.getXMLPropertiesByPath("//wing/high_lift_devices/slats/slat/slat_non_dimensional_outer_station")).get(i)));
			}

			for(int i=0; i<theStabilityCalculator.getWingNumberOfSlats(); i++) {
				theStabilityCalculator.getWingDeltaSlat().add(
						Amount.valueOf(
								Double.parseDouble(reader.getXMLPropertiesByPath("//wing/high_lift_devices/slats/slat/slat_deflection").get(i))
								,NonSI.DEGREE_ANGLE)
						);
			}

		}

		//---------------------------------------------------------------------------------
		// FUSELAGE:

		theStabilityCalculator.setFuselageDiameter((Amount<Length>) reader.getXMLAmountWithUnitByPath("//fuselage/fuselageDiameter"));
		theStabilityCalculator.setFuselageLength((Amount<Length>) reader.getXMLAmountWithUnitByPath("//fuselage/fuselageLength"));
		theStabilityCalculator.setFuselageNoseFinessRatio(Double.parseDouble((reader.getXMLPropertyByPath("//fuselage/noseFinenessRatio"))));
		theStabilityCalculator.setFuselageFinessRatio(Double.parseDouble((reader.getXMLPropertyByPath("//fuselage/finenessRatio"))));
		theStabilityCalculator.setFuselageTailFinessRatio(Double.parseDouble((reader.getXMLPropertyByPath("//fuselage/tailFinenessRatio"))));
		theStabilityCalculator.setFuselageWindshieldAngle((Amount<Angle>)reader.getXMLAmountWithUnitByPath("//fuselage/windshieldAngle"));
		theStabilityCalculator.setFuselageUpSweepAngle((Amount<Angle>)reader.getXMLAmountWithUnitByPath("//fuselage/upsweepAngle"));
		theStabilityCalculator.setFuselageXPercentPositionPole(Double.parseDouble((reader.getXMLPropertyByPath("//fuselage/xPercentPositionPole"))));

		//---------------------------------------------------------------------------------
		// HORIZONTAL TAIL:

		theStabilityCalculator.setXApexHTail((Amount<Length>) reader.getXMLAmountWithUnitByPath("//horizontal_tail/position/x"));
		theStabilityCalculator.setYApexHTail((Amount<Length>) reader.getXMLAmountWithUnitByPath("//horizontal_tail/position/y"));
		theStabilityCalculator.setZApexHTail((Amount<Length>) reader.getXMLAmountWithUnitByPath("//horizontal_tail/position/z"));
		
		theStabilityCalculator.setVerticalTailSpan((Amount<Length>) reader.getXMLAmountWithUnitByPath("//horizontal_tail/global/vertical_tail_span"));

		theStabilityCalculator.setHTailSurface((Amount<Area>) reader.getXMLAmountWithUnitByPath("//horizontal_tail/global/surface"));
		theStabilityCalculator.setHTailAngleOfIncidence((Amount<Angle>) reader.getXMLAmountWithUnitByPath("//horizontal_tail/global/angle_of_incidence"));
		theStabilityCalculator.setHTailAspectRatio(Double.valueOf(reader.getXMLPropertyByPath("//horizontal_tail/global/aspect_ratio")));
		theStabilityCalculator.setHTailNumberOfPointSemiSpanWise((int)Double.parseDouble((reader.getXMLPropertyByPath("//horizontal_tail/global/number_of_point_semispan"))));
		theStabilityCalculator.setHTailnumberOfGivenSections((int)Double.parseDouble((reader.getXMLPropertyByPath("//horizontal_tail/global/number_of_given_sections"))));

		List<String> airfoilHtailFamilyProperty = reader.getXMLPropertiesByPath("//horizontal_tail/global/mean_airfoil_family");
		//airfoil type
		if(airfoilHtailFamilyProperty.get(0).equals("NACA_4_DIGIT"))
			theStabilityCalculator.setHTailMeanAirfoilFamily(AirfoilFamilyEnum.NACA_4_Digit);
		else if(airfoilHtailFamilyProperty.get(0).equals("NACA_5_DIGIT"))
			theStabilityCalculator.setHTailMeanAirfoilFamily(AirfoilFamilyEnum.NACA_5_Digit);
		else if(airfoilHtailFamilyProperty.get(0).equals("NACA_63_SERIES"))
			theStabilityCalculator.setHTailMeanAirfoilFamily(AirfoilFamilyEnum.NACA_63_Series);
		else if(airfoilHtailFamilyProperty.get(0).equals("NACA_64_SERIES"))
			theStabilityCalculator.setHTailMeanAirfoilFamily(AirfoilFamilyEnum.NACA_64_Series);
		else if(airfoilHtailFamilyProperty.get(0).equals("NACA_65_SERIES"))
			theStabilityCalculator.setHTailMeanAirfoilFamily(AirfoilFamilyEnum.NACA_65_Series);
		else if(airfoilHtailFamilyProperty.get(0).equals("NACA_66_SERIES"))
			theStabilityCalculator.setHTailMeanAirfoilFamily(AirfoilFamilyEnum.NACA_66_Series);
		else if(airfoilHtailFamilyProperty.get(0).equals("BICONVEX"))
			theStabilityCalculator.setHTailMeanAirfoilFamily(AirfoilFamilyEnum.BICONVEX);
		else if(airfoilHtailFamilyProperty.get(0).equals("DOUBLE_WEDGE"))
			theStabilityCalculator.setHTailMeanAirfoilFamily(AirfoilFamilyEnum.DOUBLE_WEDGE);
		else {
			System.err.println("NO VALID FAMILY TYPE!!");
			return;
		}
		//recognizing airfoil family
		airfoilFamilyIndex = 0;
		if(theStabilityCalculator.getHTailMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_4_Digit) 
			airfoilFamilyIndex = 1;
		else if(theStabilityCalculator.getHTailMeanAirfoilFamily()  == AirfoilFamilyEnum.NACA_5_Digit)
			airfoilFamilyIndex = 2;
		else if(theStabilityCalculator.getHTailMeanAirfoilFamily()  == AirfoilFamilyEnum.NACA_63_Series)
			airfoilFamilyIndex = 3;
		else if(theStabilityCalculator.getHTailMeanAirfoilFamily()  == AirfoilFamilyEnum.NACA_64_Series)
			airfoilFamilyIndex = 4;
		else if(theStabilityCalculator.getHTailMeanAirfoilFamily()  == AirfoilFamilyEnum.NACA_65_Series)
			airfoilFamilyIndex = 5;
		else if(theStabilityCalculator.getHTailMeanAirfoilFamily()  == AirfoilFamilyEnum.NACA_66_Series)
			airfoilFamilyIndex = 6;
		else if(theStabilityCalculator.getHTailMeanAirfoilFamily()  == AirfoilFamilyEnum.BICONVEX)
			airfoilFamilyIndex = 7;
		else if(theStabilityCalculator.getHTailMeanAirfoilFamily()  == AirfoilFamilyEnum.DOUBLE_WEDGE)
			airfoilFamilyIndex = 8;

		theStabilityCalculator.setHTailMaxThicknessBreakPoints(reader.readArrayDoubleFromXMLSplit("//horizontal_tail/distribution/geometry/max_thickness_airfoil"));
		
		//distribution
		theStabilityCalculator.setHTailYAdimensionalBreakPoints(reader.readArrayDoubleFromXML("//horizontal_tail/distribution/geometry/y_adimensional_stations"));
		theStabilityCalculator.setHTailChordsBreakPoints(reader.readArrayofAmountFromXML("//horizontal_tail/distribution/geometry/chord_distribution"));
		theStabilityCalculator.setHTailXleBreakPoints(reader.readArrayofAmountFromXML("//horizontal_tail/distribution/geometry/x_le_distribution"));
		theStabilityCalculator.setHTailTwistBreakPoints(reader.readArrayofAmountFromXML("//horizontal_tail/distribution/geometry/twist_distribution"));
		theStabilityCalculator.setHTailDihedralBreakPoints(reader.readArrayofAmountFromXML("//horizontal_tail/distribution/geometry/dihedral_distribution"));
		theStabilityCalculator.setHTailAlphaZeroLiftBreakPoints(reader.readArrayofAmountFromXML("//horizontal_tail/distribution/aerodynamics/alpha_zero_lift_distribution"));
		theStabilityCalculator.setHTailAlphaStarBreakPoints(reader.readArrayofAmountFromXML("//horizontal_tail/distribution/aerodynamics/alpha_star_distribution"));
		theStabilityCalculator.setHTailClMaxBreakPoints(reader.readArrayDoubleFromXML("//horizontal_tail/distribution/aerodynamics/maximum_lift_coefficient_distribution"));
		theStabilityCalculator.setHTailClAlphaBreakPointsDeg(reader.readArrayDoubleFromXML("//horizontal_tail/distribution/aerodynamics/linear_slope_coefficient"));
		theStabilityCalculator.setHTailXACBreakPoints(reader.readArrayDoubleFromXMLSplit("//horizontal_tail/distribution/aerodynamics/x_ac_referred_to_chord"));
		theStabilityCalculator.setHTailCmACBreakPoints(reader.readArrayDoubleFromXMLSplit("//horizontal_tail/distribution/aerodynamics/c_m_ac"));
	
		//---------------------------------------------------------------------------------
		// ELEVATOR:
		theStabilityCalculator.setAnglesOfElevatorDeflection(reader.readArrayofAmountFromXML("//horizontal_tail/elevator/angles_of_elevator_deflection"));
		theStabilityCalculator.setElevatorCfC(Double.valueOf(reader.getXMLPropertyByPath("//horizontal_tail/elevator/elevator_chord_ratio")));
		theStabilityCalculator.setElevatorEtaIn(Double.valueOf(reader.getXMLPropertyByPath("//horizontal_tail/elevator/elevator_non_dimensional_inner_station")));
		theStabilityCalculator.setElevatorEtaOut(Double.valueOf(reader.getXMLPropertyByPath("//horizontal_tail/elevator/elevator_non_dimensional_outer_station")));

		//---------------------------------------------------------------------------------
		// ENGINE:

		//---------------------------------------------------------------------------------
		// LIFT CURVE OF AIRFOIL:
		
		//wing
		String wingLiftAirfoilMethod = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@WingAirfoilLiftCurve");
		if(wingLiftAirfoilMethod.equalsIgnoreCase("INPUT"))
			theStabilityCalculator.setWingairfoilLiftCoefficientCurve(MethodEnum.INPUT);
		if(wingLiftAirfoilMethod.equalsIgnoreCase("CALCULATED"))
			theStabilityCalculator.setWingairfoilLiftCoefficientCurve(MethodEnum.CLASSIC);
		
		if(theStabilityCalculator.getWingairfoilLiftCoefficientCurve()==MethodEnum.INPUT){
			for (int i=0; i<theStabilityCalculator.getWingNumberOfGivenSections(); i++){
				List<String> alphasWing = new ArrayList<>();
				List<Amount<Angle>> alphasWingAmount = new ArrayList<>();
				alphasWing =JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//wing/airfoil_lift_curve/alpha").get(i));
				List<String> clWing = new ArrayList<>();
				List<Double> clWingAmount = new ArrayList<>();
				clWing =JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//wing/airfoil_lift_curve/lift_coefficient").get(i));
				for (int ii=0; ii<clWing.size(); ii++){
					clWingAmount.add(Double.valueOf(clWing.get(ii)));
					alphasWingAmount.add(Amount.valueOf(Double.valueOf(alphasWing.get(ii)),NonSI.DEGREE_ANGLE));
				}
				theStabilityCalculator.getClDistributionAirfoilsWing().add(i,clWingAmount);
				theStabilityCalculator.getAlphaAirfoilsWing().add(i,alphasWingAmount);
			}
		}
		
		//horizontal tail
		String hTailLiftAirfoilMethod = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@HtailAirfoilLiftCurve");
		if(hTailLiftAirfoilMethod.equalsIgnoreCase("INPUT"))
			theStabilityCalculator.setHTailairfoilLiftCoefficientCurve(MethodEnum.INPUT);
		if(hTailLiftAirfoilMethod.equalsIgnoreCase("CALCULATED"))
			theStabilityCalculator.setHTailairfoilLiftCoefficientCurve(MethodEnum.CLASSIC);
		
		if(theStabilityCalculator.getHTailairfoilLiftCoefficientCurve()==MethodEnum.INPUT){
			for (int i=0; i<theStabilityCalculator.getHTailnumberOfGivenSections(); i++){
				List<String> alphasHTail = new ArrayList<>();
				List<Amount<Angle>> alphasHTailAmount = new ArrayList<>();
				alphasHTail =JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//horizontal_tail/airfoil_lift_curve/alpha").get(i));
				List<String> clHTail = new ArrayList<>();
				List<Double> clHTailAmount = new ArrayList<>();
				clHTail =JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//horizontal_tail/airfoil_lift_curve/lift_coefficient").get(i));
				for (int ii=0; ii<clHTail.size(); ii++){
					clHTailAmount.add(Double.valueOf(clHTail.get(ii)));
					alphasHTailAmount.add(Amount.valueOf(Double.valueOf(alphasHTail.get(ii)),NonSI.DEGREE_ANGLE));
				}
				theStabilityCalculator.getClDistributionAirfoilsHTail().add(i,clHTailAmount);
				theStabilityCalculator.getAlphaAirfoilsHTail().add(i,alphasHTailAmount);
			}
		}
		//---------------------------------------------------------------------------------
		// DRAG POLAR:
		
		//wing
		String wingDragMethod = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@wingPolar");
		if(wingDragMethod.equalsIgnoreCase("INPUT"))
			theStabilityCalculator.setwingDragMethod(MethodEnum.INPUT);
		if(wingDragMethod.equalsIgnoreCase("CALCULATED_PARABOLIC"))
			theStabilityCalculator.setwingDragMethod(MethodEnum.CLASSIC);
		if(wingDragMethod.equalsIgnoreCase("CALCULATED_INPUT_AIRFOIL"))
			theStabilityCalculator.setwingDragMethod(MethodEnum.AIRFOIL_INPUT);
		if(wingDragMethod.equalsIgnoreCase("CALCULATED_DATABASE_AIRFOIL"))
			theStabilityCalculator.setwingDragMethod(MethodEnum.AIRFOIL_DISTRIBUTION);
		
		
		if(theStabilityCalculator.getwingDragMethod()==MethodEnum.INPUT){
			theStabilityCalculator.setclWingDragPolar(reader.readArrayDoubleFromXML("//wing/polar/cl_polar_curve"));
			theStabilityCalculator.setcDPolarWing(reader.readArrayDoubleFromXML("//wing/polar/cd_polar_curve"));
		}
		

		if(theStabilityCalculator.getwingDragMethod()==MethodEnum.AIRFOIL_INPUT){
			for (int i=0; i<theStabilityCalculator.getWingNumberOfGivenSections(); i++){
				List<String> clWing = new ArrayList<>();
				List<Double> clWingAmount = new ArrayList<>();
				clWing =JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//wing/polar/cl_polar_curve").get(i));
				List<String> cdWing = new ArrayList<>();
				List<Double> cdWingAmount = new ArrayList<>();
				cdWing =JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//wing/polar/cd_polar_curve").get(i));
				for (int ii=0; ii<clWing.size(); ii++){
					clWingAmount.add(Double.valueOf(clWing.get(ii)));
					cdWingAmount.add(Double.valueOf(cdWing.get(ii)));
				}
				theStabilityCalculator.getClPolarAirfoilWingDragPolar().add(i,clWingAmount);
				theStabilityCalculator.getcDPolarAirfoilsWing().add(i,cdWingAmount);
			}
		}
		
		//h tail
		String hTailDragMethod = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@hTailPolar");
		if(hTailDragMethod.equalsIgnoreCase("INPUT"))
			theStabilityCalculator.setHTailDragMethod(MethodEnum.INPUT);
		if(hTailDragMethod.equalsIgnoreCase("CALCULATED_PARABOLIC"))
			theStabilityCalculator.setHTailDragMethod(MethodEnum.CLASSIC);
		if(hTailDragMethod.equalsIgnoreCase("CALCULATED_INPUT_AIRFOIL"))
			theStabilityCalculator.setHTailDragMethod(MethodEnum.AIRFOIL_INPUT);
		if(hTailDragMethod.equalsIgnoreCase("CALCULATED_DATABASE_AIRFOIL"))
			theStabilityCalculator.setHTailDragMethod(MethodEnum.AIRFOIL_DISTRIBUTION);
		
		if(theStabilityCalculator.getHTailDragMethod()==MethodEnum.INPUT){
			theStabilityCalculator.setcLhTailDragPolar(reader.readArrayDoubleFromXML("//horizontal_tail/polar/cl_polar_curve"));
			theStabilityCalculator.setcDPolarhTail(reader.readArrayDoubleFromXML("//horizontal_tail/polar/cd_polar_curve"));
		}
		if(theStabilityCalculator.getHTailDragMethod()==MethodEnum.AIRFOIL_INPUT){
			for (int i=0; i<theStabilityCalculator.getHTailnumberOfGivenSections(); i++){
				List<String> clTail = new ArrayList<>();
				List<Double> clTailAmount = new ArrayList<>();
				clTail =JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//horizontal_tail/polar/cl_polar_curve").get(i));
				List<String> cdTail = new ArrayList<>();
				List<Double> cdTailAmount = new ArrayList<>();
				cdTail =JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//horizontal_tail/polar/cd_polar_curve").get(i));
				for (int ii=0; ii<clTail.size(); ii++){
					clTailAmount.add(Double.valueOf(clTail.get(ii)));
					cdTailAmount.add(Double.valueOf(cdTail.get(ii)));
				}
				theStabilityCalculator.getClPolarAirfoilHTailDragPolar().add(i,clTailAmount);
				theStabilityCalculator.getcDPolarAirfoilsHTail().add(i,cdTailAmount);
			}
		}
		
		//---------------------------------------------------------------------------------
		// MOMENT CURVE:
		
		//wing
		String wingMomentAirfoilMethod = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@ACmoment");
		if(wingMomentAirfoilMethod.equalsIgnoreCase("CURVE"))
			theStabilityCalculator.setWingairfoilMomentCoefficientCurve(MethodEnum.INPUTCURVE);
		if(wingMomentAirfoilMethod.equalsIgnoreCase("CONSTANT"))
			theStabilityCalculator.setWingairfoilMomentCoefficientCurve(MethodEnum.CONSTANT);
		
		if(theStabilityCalculator.getWingairfoilMomentCoefficientCurve()==MethodEnum.INPUTCURVE){
			for (int i=0; i<theStabilityCalculator.getWingNumberOfGivenSections(); i++){
				List<String> clWing = new ArrayList<>();
				List<Double> clWingAmount = new ArrayList<>();
				clWing =JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//wing/moment/cl_moment_curve").get(i));
				List<String> cmWing = new ArrayList<>();
				List<Double> cmWingAmount = new ArrayList<>();
				cmWing =JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//wing/moment/cm_curve").get(i));
				for (int ii=0; ii<clWing.size(); ii++){
					clWingAmount.add(Double.valueOf(clWing.get(ii)));
					cmWingAmount.add(Double.valueOf(cmWing.get(ii)));
				}
				theStabilityCalculator.getWingCLMomentAirfoilInput().add(i,clWingAmount);
				theStabilityCalculator.getWingCMMomentAirfoilInput().add(i,cmWingAmount);
			}
		}
		if(theStabilityCalculator.getWingairfoilMomentCoefficientCurve()==MethodEnum.CONSTANT){
		theStabilityCalculator.setWingCmACBreakPoints(reader.readArrayDoubleFromXMLSplit("//wing/distribution/aerodynamics/c_m_ac"));
		}

		//---------------------------------------------------------------------------------
		// PLOT:

		// plot flag
		boolean plotFlag = false;
		String plotString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@plot");
		if(plotString.equalsIgnoreCase("true"))
			plotFlag = Boolean.TRUE;

		theStabilityCalculator.setPlotCheck(plotFlag);


		if( theStabilityCalculator.getPlotCheck() == Boolean.TRUE ) {

			//LIFT
			// CL vs Alpha wing
			String wingLift = reader.getXMLPropertyByPath("//plot/lift/wing/CL_clean_curve");
			if (wingLift != null) {
				if(wingLift.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.WING_CL_CURVE_CLEAN);
			}

			// CL vs Alpha wing high lift 
			String wingLiftHighLift = reader.getXMLPropertyByPath("//plot/lift/wing/CL_high_lift_curve");
			if (wingLiftHighLift != null) {
				if(wingLiftHighLift.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.WING_CL_CURVE_HIGH_LIFT);
			}

			// CL vs Alpha horizontal tail
			String hTailLift = reader.getXMLPropertyByPath("//plot/lift/horizontal_tail/CL_clean_curve");
			if (hTailLift != null) {
				if(hTailLift.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.HTAIL_CL_CURVE_CLEAN);
			}

			// CL vs Alpha horizontal tail high lift
			String hTailLiftHighLift = reader.getXMLPropertyByPath("//plot/lift/horizontal_tail/CL_curve_with_elevator_deflections");
			if (hTailLiftHighLift != null) {
				if(hTailLiftHighLift.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.HTAIL_CL_CURVE_ELEVATOR);
			}			


			//DRAG
			// CD vs Alpha wing
			String wingPolarDrag = reader.getXMLPropertyByPath("//plot/drag/wing/polar_drag");
			if (wingPolarDrag != null) {
				if(wingPolarDrag.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.WING_POLAR_CURVE_CLEAN);
			}

			// CD vs Alpha wing highlift
			String wingPolarDragHighLift = reader.getXMLPropertyByPath("//plot/drag/wing/polar_drag_with_high_lift_devices");
			if (wingPolarDragHighLift != null) {
				if(wingPolarDragHighLift.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.WING_POLAR_CURVE_HIGHLIFT);
			}

			// CD vs Alpha horizontal tail
			String horizontalTailPolarDrag = reader.getXMLPropertyByPath("//plot/drag/horizontal_tail/polar_drag");
			if (horizontalTailPolarDrag != null) {
				if(horizontalTailPolarDrag.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.HTAIL_POLAR_CURVE_CLEAN);
			}

			//MOMENT
			// CM wing respect to c4
			String wingMomentQuarter = reader.getXMLPropertyByPath("//plot/moment/wing/moment_coefficient_respect_to_other_poles");
			if (wingMomentQuarter != null) {
				if(wingMomentQuarter.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.WING_CM_QUARTER_CHORD);
			}

			// CM wing respect to ac
			String wingMomentAC = reader.getXMLPropertyByPath("//plot/moment/wing/moment_coefficient_respect_to_aerodynamic_center");
			if (wingMomentAC != null) {
				if(wingMomentAC.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.WING_CM_AERODYNAMIC_CENTER);
			}

			// CM horizontal tail respect to c4
			String horizoMomentQuarter = reader.getXMLPropertyByPath("//plot/moment/horizontal_tail/moment_coefficient_respect_to_quarter_chord");
			if (horizoMomentQuarter != null) {
				if(horizoMomentQuarter.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.HTAIL_CM_QUARTER_CHORD);
			}

			// CM horizontal tail respect to ac
			String hTailMomentAC = reader.getXMLPropertyByPath("//plot/moment/horizontal_tail/moment_coefficient_respect_to_aerodynamic_center");
			if ( hTailMomentAC != null) {
				if( hTailMomentAC.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.HTAIL_CM_AERODYNAMIC_CENTER);
			}

			// CM fuselage
			String fuselageMoment = reader.getXMLPropertyByPath("//plot/moment/fuselage/moment_coefficient");
			if ( fuselageMoment != null) {
				if( fuselageMoment.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.FUSELAGE_CM_PLOT);
			}

			// CM engine direct
			String engineDirect = reader.getXMLPropertyByPath("//plot/moment/engine/propulsive_System_CM_direct_effects");
			if ( engineDirect != null) {
				if( engineDirect.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.PROPULSIVE_SYSTEM_CM_DIRECT_EFFECTS);
			}

			// CM engine non direct
			String engineNonDirect = reader.getXMLPropertyByPath("//plot/moment/engine/propulsive_System_CM_indirect_effects");
			if ( engineNonDirect != null) {
				if( engineNonDirect.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.PROPULSIVE_SYSTEM_CM_NON_DIRECT_EFFECTS);
			}

			//STABILITY
			// CM alpha component
			String cmAlphaComponent = reader.getXMLPropertyByPath("//plot/stability/CM_cg_vs_alpha_body_components");
			if ( cmAlphaComponent != null) {
				if( cmAlphaComponent.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.AIRCRAFT_CM_VS_ALPHA_BODY_COMPONENTS);
			}

			// CM alpha aircraft
			String cmAlphaAircraft = reader.getXMLPropertyByPath("//plot/stability/CM_cg_vs_alpha_body_aircraft");
			if ( cmAlphaAircraft != null) {
				if( cmAlphaAircraft.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.AIRCRAFT_CM_VS_ALPHA_BODY);	
			}

			// CM alpha aircraft delta e
			String cmAlphaVarDeltaE = reader.getXMLPropertyByPath("//plot/stability/CM_cg_vs_alpha_body_aircraft_var_deltae");
			if ( cmAlphaVarDeltaE != null) {
				if( cmAlphaVarDeltaE.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.AIRCRAFT_CM_VS_ALPHA_BODY_DELTAE);	
			}

			// Cl alpha aircraft delta e
			String cmlTotVarDeltaE = reader.getXMLPropertyByPath("//plot/stability/CL_tot_vs_alpha_body_aircraft_var_deltae");
			if ( cmlTotVarDeltaE != null) {
				if( cmlTotVarDeltaE.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.AIRCRAFT_CL_VS_ALPHA_BODY_DELTAE);	
			}

			// Cl alpha aircraft delta e
			String cmCLDeltaE = reader.getXMLPropertyByPath("//plot/stability/CM_cg_vs_CL_tot_var_deltae");
			if ( cmCLDeltaE != null) {
				if( cmCLDeltaE.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.AIRCRAFT_CM_VS_CL_DELTAE);	
			}

			// Downwash angle
			String downwashAngle = reader.getXMLPropertyByPath("//plot/stability/Downwash_angle");
			if ( downwashAngle != null) {
				if( downwashAngle.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.DOWNWASH_ANGLE);	
			}

			// Downwash gradient
			String downwashGrdient = reader.getXMLPropertyByPath("//plot/stability/Downwash_gradient");
			if ( downwashGrdient != null) {
				if( downwashGrdient.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.DOWNWASH_GRADIENT);	
			}

			// delta e equilibrium
			String deltaEEquilibrium = reader.getXMLPropertyByPath("//plot/stability/delta_e_equilibrium");
			if ( deltaEEquilibrium != null) {
				if( deltaEEquilibrium.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.DELTA_E_EQUILIBRIUM);	
			}

			// neutral point stick fixed
			String neutralPoint = reader.getXMLPropertyByPath("//plot/stability/neutral_point_stick_fixed");
			if ( neutralPoint != null) {
				if( neutralPoint.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.NEUTRAL_POINT);	
			}

			//DISTRIBUTION
			 //wing
			String clWing = reader.getXMLPropertyByPath("//plot/distribution/cl_wing");
			if ( clWing != null) {
				if( clWing.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.CL_DISTRIBUTION_WING);	
			}
			
			String inducedAngleOfAttackWing = reader.getXMLPropertyByPath("//plot/distribution/induced_angle_of_attack_wing");
			if ( inducedAngleOfAttackWing != null) {
				if( inducedAngleOfAttackWing.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.INDUCED_ALPHA_DISTRIBUTION_WING);	
			}
			
			String centerOfPressureWing = reader.getXMLPropertyByPath("//plot/distribution/center_of_pressure_wing");
			if ( centerOfPressureWing != null) {
				if( centerOfPressureWing.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.CENTER_OF_PRESSURE_DISTRIBUTION_WING);	
			}
			
			String cmWing = reader.getXMLPropertyByPath("//plot/distribution/cm_wing");
			if ( cmWing != null) {
				if( cmWing.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.CM_DISTRIBUTION_WING);	
			}
			
			//horizontal tail
			String clHTail = reader.getXMLPropertyByPath("//plot/distribution/cl_horizontal_tail");
			if ( clHTail != null) {
				if( clHTail.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.CL_DISTRIBUTION_HORIZONTAL_TAIL);	
			}
			
			String inducedAngleOfAttackHTail = reader.getXMLPropertyByPath("//plot/distribution/induced_angle_of_attack_horizontal_tail");
			if ( inducedAngleOfAttackHTail != null) {
				if( inducedAngleOfAttackHTail.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.INDUCED_ALPHA_DISTRIBUTION_HORIZONTAL_TAIL);	
			}
			
			String centerOfPressureHTail = reader.getXMLPropertyByPath("//plot/distribution/center_of_pressure_horizontal_tail");
			if ( centerOfPressureHTail != null) {
				if( centerOfPressureHTail.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.CENTER_OF_PRESSURE_DISTRIBUTION_HORIZONTAL_TAIL);	
			}
			
			String cmHTail = reader.getXMLPropertyByPath("//plot/distribution/cm_horizontal_tail");
			if ( cmHTail != null) {
				if( cmHTail.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.CM_DISTRIBUTION_HORIZONTAL_TAIL);	
			}
			
			String clTotal = reader.getXMLPropertyByPath("//plot/stability/CL_Total");
			if ( clTotal != null) {
				if( clTotal.equalsIgnoreCase("TRUE")) 
					theStabilityCalculator.getPlotList().add(AerodynamicAndStabilityPlotEnum.CL_TOTAL);	
			}
		}


	}
}