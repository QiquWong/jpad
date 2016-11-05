package sandbox2.mr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import configuration.MyConfiguration;
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
		
		//TODO continue here 
	}
}