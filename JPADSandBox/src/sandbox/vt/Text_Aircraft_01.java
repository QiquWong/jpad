package sandbox.vt;

import java.io.File;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import standaloneutils.JPADGlobalData;
import writers.JPADDataWriter;
import writers.JPADWriteUtils;

public class Text_Aircraft_01 {

	public static void main(String[] args) {

		// Default operating conditions
		OperatingConditions theOperatingCondition = new OperatingConditions();
		
		theOperatingCondition.set_machCurrent(0.53);
		theOperatingCondition.set_altitude(Amount.valueOf(7000, NonSI.FOOT));
		//theOperatingConditions.set_alphaCurrent(Amount.valueOf(2.0, NonSI.DEGREE_ANGLE));
		
		System.out.println("Operating condition");
		System.out.println("\tMach: " + theOperatingCondition.get_machCurrent());
		System.out.println("----------------------");
		

		// Initialize Aircraft with default parameters
//		Aircraft theAircraft = new Aircraft(
//				ComponentEnum.FUSELAGE, 
//				ComponentEnum.WING,
//				ComponentEnum.HORIZONTAL_TAIL,
//				ComponentEnum.VERTICAL_TAIL,
//				ComponentEnum.POWER_PLANT,
//				ComponentEnum.NACELLE,
//				ComponentEnum.LANDING_GEAR
//				);
		Aircraft theAircraft = Aircraft.createDefaultAircraft();
		theAircraft.set_name("Test Aircraft");

		System.out.println("\nAircraft");
		System.out.println("\tName of fuselage: " + theAircraft.get_fuselage().get_name());
		System.out.println("----------------------\n");
		
		// Gotta create this object
		ACAnalysisManager theAnalysis = new ACAnalysisManager(theOperatingCondition);
		// Gotta call this method first
		theAnalysis.updateGeometry(theAircraft);
		theAnalysis.doAnalysis(theAircraft, 
				AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY, 
				AnalysisTypeEnum.BALANCE,
				AnalysisTypeEnum.WEIGHTS,
				AnalysisTypeEnum.PERFORMANCE,
				AnalysisTypeEnum.COSTS);
		// do pass these parameters in this sequence
		
		
		System.out.println("----------------------");
		
		// TODO: fix this ??
		JPADGlobalData.setTheCurrentAircraft(theAircraft);
		JPADGlobalData.setTheCurrentAnalysis(theAnalysis);
		JPADWriteUtils.buildXmlTree(theAircraft, theOperatingCondition);

		JPADDataWriter theWriter = new JPADDataWriter(
				theOperatingCondition, theAircraft, theAnalysis);

		String xmlFileFolderPath = 
				MyConfiguration.currentDirectoryString
				+ File.separator + "out";		
		String xmlFilePath = xmlFileFolderPath + File.separator + "pippo.xml";
		
		System.out.println("Exporting to file: " + xmlFilePath);
		theWriter.exportToXMLfile(xmlFilePath);
		
	}

}
