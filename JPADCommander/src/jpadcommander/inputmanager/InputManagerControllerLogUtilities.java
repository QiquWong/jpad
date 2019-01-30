package jpadcommander.inputmanager;

import java.io.File;
import java.util.Arrays;

import javax.measure.unit.SI;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.components.liftingSurface.creator.AsymmetricFlapCreator;
import aircraft.components.liftingSurface.creator.LiftingSurfacePanelCreator;
import aircraft.components.liftingSurface.creator.SlatCreator;
import aircraft.components.liftingSurface.creator.SpoilerCreator;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.powerplant.Engine;
import configuration.enumerations.ClassTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.WindshieldTypeEnum;
import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.WingTipType;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.XSpacingType;
import jpadcommander.Main;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class InputManagerControllerLogUtilities {

	//---------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	public InputManagerController theController;
	
	//---------------------------------------------------------------------------------
	// BUILDER
	public InputManagerControllerLogUtilities(InputManagerController controller) {
		
		this.theController = controller;
		
	}
	
	//---------------------------------------------------------------------------------
	// METHODS
	
	public void logAircraftFromFileToInterface() {

		String dirLiftingSurfaces = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces";
		String dirFuselages = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "fuselages";
		String dirEngines = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "engines";
		String dirNacelles = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "nacelles";
		String dirLandingGears = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "landing_gears";
		String dirCabinConfiguration = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "cabin_configurations";
		String pathToXML = theController.getTextFieldAircraftInputFile().getText();

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaAircraftConsoleOutput().setText(
				Main.getTheAircraft().toString()
				);

		// get the text field for aircraft input data
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		//---------------------------------------------------------------------------------
		// AIRCRAFT TYPE:
		String aircraftTypeFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@type");
		
		if(aircraftTypeFileName != null) { 
			if(theController.getAircraftTypeChoiceBox() != null) {
				if(aircraftTypeFileName.equalsIgnoreCase("JET"))
					theController.getAircraftTypeChoiceBox().getSelectionModel().select(0);
				else if(aircraftTypeFileName.equalsIgnoreCase("FIGHTER"))		
					theController.getAircraftTypeChoiceBox().getSelectionModel().select(1);
				else if(aircraftTypeFileName.equalsIgnoreCase("BUSINESS_JET"))
					theController.getAircraftTypeChoiceBox().getSelectionModel().select(2);
				else if(aircraftTypeFileName.equalsIgnoreCase("TURBOPROP"))
					theController.getAircraftTypeChoiceBox().getSelectionModel().select(3);
				else if(aircraftTypeFileName.equalsIgnoreCase("GENERAL_AVIATION"))
					theController.getAircraftTypeChoiceBox().getSelectionModel().select(4);
				else if(aircraftTypeFileName.equalsIgnoreCase("COMMUTER"))
					theController.getAircraftTypeChoiceBox().getSelectionModel().select(5);
				else if(aircraftTypeFileName.equalsIgnoreCase("ACROBATIC"))
					theController.getAircraftTypeChoiceBox().getSelectionModel().select(6);
			}
		}
		
		//---------------------------------------------------------------------------------
		// REGULATIONS TYPE:
		String regulationsTypeFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@regulations");
		
		if(regulationsTypeFileName != null) { 
			if(theController.getRegulationsTypeChoiceBox() != null) {
				if(regulationsTypeFileName.equalsIgnoreCase("FAR_23"))
					theController.getRegulationsTypeChoiceBox().getSelectionModel().select(0);
				else if(regulationsTypeFileName.equalsIgnoreCase("FAR_25"))		
					theController.getRegulationsTypeChoiceBox().getSelectionModel().select(1);
			}
		}
		
		//---------------------------------------------------------------------------------
		// CABIN CONFIGURATION:
		String cabinConfigrationFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//global_data/cabin_configuration/@file");

		if(theController.getTextFieldAircraftCabinConfigurationFile() != null) 
			theController.getTextFieldAircraftCabinConfigurationFile().setText(
					dirCabinConfiguration 
					+ File.separator
					+ cabinConfigrationFileName
					);
		else
			theController.getTextFieldAircraftCabinConfigurationFile().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// FUSELAGE:
		String fuselageFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselages/fuselage/@file");
		if(fuselageFileName != null) 
			theController.getTextFieldAircraftFuselageFile().setText(
					dirFuselages 
					+ File.separator
					+ fuselageFileName
					);
		else
			theController.getTextFieldAircraftFuselageFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getFuselage() != null) {
			theController.getTextFieldAircraftFuselageX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getFuselage()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageXUnitChoiceBox().getSelectionModel().select(1);

		}

		else {
			theController.getTextFieldAircraftFuselageX().setText("0.0");
			theController.getFuselageXUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getFuselage() != null) {
			theController.getTextFieldAircraftFuselageY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getFuselage()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageYUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftFuselageY().setText("0.0");
			theController.getFuselageYUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getFuselage() != null) {
			theController.getTextFieldAircraftFuselageZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getFuselage()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageZUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftFuselageZ().setText("0.0");
			theController.getFuselageZUnitChoiceBox().getSelectionModel().select(0);
		}
		//---------------------------------------------------------------------------------
		// WING:
		String wingFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/wing/@file");
		if(wingFileName != null)
			theController.getTextFieldAircraftWingFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ wingFileName
					);
		else 
			theController.getTextFieldAircraftWingFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getWing() != null) {

			theController.getTextFieldAircraftWingX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getWing()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.getWingXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getWingXUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftWingX().setText("0.0");
			theController.getWingXUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getWing() != null) {

			theController.getTextFieldAircraftWingY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getWing()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.getWingYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getWingYUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftWingY().setText("0.0");
			theController.getWingYUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getWing() != null) {

			theController.getTextFieldAircraftWingZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getWing()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.getWingZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getWingZUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftWingZ().setText("0.0");
			theController.getWingZUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getWing() != null) {

			theController.getTextFieldAircraftWingRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getWing()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("�")
					|| Main.getTheAircraft()
					.getWing()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				theController.getWingRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				theController.getWingRiggingAngleUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftWingRiggingAngle().setText("0.0");
			theController.getWingRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
		}
		//---------------------------------------------------------------------------------
		// HORIZONTAL TAIL:
		String hTailFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/horizontal_tail/@file");
		if(hTailFileName != null)
			theController.getTextFieldAircraftHTailFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ hTailFileName
					);
		else
			theController.getTextFieldAircraftHTailFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getHTail() != null) {

			theController.getTextFieldAircraftHTailX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getHTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.gethTailXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.gethTailXUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftHTailX().setText("0.0");
			theController.gethTailXUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getHTail() != null) {

			theController.getTextFieldAircraftHTailY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getHTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.gethTailYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.gethTailYUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftHTailY().setText("0.0");
			theController.gethTailYUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getHTail() != null) {

			theController.getTextFieldAircraftHTailZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getHTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.getHtailZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getHtailZUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftHTailZ().setText("0.0");
			theController.getHtailZUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getHTail() != null) {

			theController.getTextFieldAircraftHTailRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("�")
					|| Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				theController.gethTailRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				theController.gethTailRiggingAngleUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftHTailRiggingAngle().setText("0.0");
			theController.gethTailRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
		}
		//---------------------------------------------------------------------------------
		// VERTICAL TAIL:
		String vTailFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/vertical_tail/@file");
		if(vTailFileName != null)
			theController.getTextFieldAircraftVTailFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ vTailFileName
					);
		else
			theController.getTextFieldAircraftVTailFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getVTail() != null) {

			theController.getTextFieldAircraftVTailX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getVTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.getvTailXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getvTailXUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftVTailX().setText("0.0");
			theController.getvTailXUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getVTail() != null) {

			theController.getTextFieldAircraftVTailY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getVTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.getvTailYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getvTailYUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftVTailY().setText("0.0");
			theController.getvTailYUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getVTail() != null) {

			theController.getTextFieldAircraftVTailZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getVTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.getvTailZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getvTailZUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftVTailZ().setText("0.0");
			theController.getvTailZUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getVTail() != null) {

			theController.getTextFieldAircraftVTailRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("�")
					|| Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				theController.getvTailRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				theController.getvTailRiggingAngleUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftVTailRiggingAngle().setText("0.0");
			theController.getvTailRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
		}
		//---------------------------------------------------------------------------------
		// CANARD:
		String canardFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/canard/@file");
		if(canardFileName != null)
			theController.getTextFieldAircraftCanardFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ canardFileName
					);
		else
			theController.getTextFieldAircraftCanardFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getCanard() != null) {

			theController.getTextFieldAircraftCanardX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getCanard()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.getCanardXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getCanardXUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftCanardX().setText("0.0");
			theController.getCanardXUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getCanard() != null) {

			theController.getTextFieldAircraftCanardY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getCanard()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.getCanardYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getCanardYUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftCanardY().setText("0.0");
			theController.getCanardYUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getCanard() != null) {

			theController.getTextFieldAircraftCanardZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getCanard()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				theController.getCanardZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getCanardZUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftCanardZ().setText("0.0");
			theController.getCanardZUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getCanard() != null) {

			theController.getTextFieldAircraftCanardRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("�")
					|| Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				theController.getCanardRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				theController.getCanardRiggingAngleUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftCanardRiggingAngle().setText("0.0");
			theController.getCanardRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
		}
		
		//---------------------------------------------------------------------------------
		// ENGINES NUMBER CHECK:
		if (Main.getTheAircraft().getPowerPlant().getEngineList().size() >= 
				theController.getTabPaneAircraftEngines().getTabs().size()) {
			
			int iStart = theController.getTabPaneAircraftEngines().getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++)
				theController.getInputManagerControllerUtilities().addAircraftEngineImplementation();
			
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER ENGINES:
		//---------------------------------------------------------------------------------
		NodeList nodelistEngines = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//power_plant/engine");
		if(nodelistEngines != null) {
			for (int i = 0; i < nodelistEngines.getLength(); i++) {
				//..........................................................................................................
				Node nodeEngine  = nodelistEngines.item(i); 
				Element elementEngine = (Element) nodeEngine;
				if(elementEngine.getAttribute("file") != null)
					theController.getTextFieldsAircraftEngineFileList().get(i).setText(
							dirEngines 
							+ File.separator
							+ elementEngine.getAttribute("file")	
							);
				else
					theController.getTextFieldsAircraftEngineFileList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) {
					theController.getTextFieldAircraftEngineXList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getXApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxAircraftEngineXUnitList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxAircraftEngineXUnitList().get(i).getSelectionModel().select(1);
				}
				else {
					theController.getTextFieldAircraftEngineXList().get(i).setText("0.0");
					theController.getChoiceBoxAircraftEngineXUnitList().get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) {
					theController.getTextFieldAircraftEngineYList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getYApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxAircraftEngineYUnitList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxAircraftEngineYUnitList().get(i).getSelectionModel().select(1);
				}
				else {
					theController.getTextFieldAircraftEngineYList().get(i).setText("0.0");
					theController.getChoiceBoxAircraftEngineYUnitList().get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) {
					theController.getTextFieldAircraftEngineZList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getZApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxAircraftEngineZUnitList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxAircraftEngineZUnitList().get(i).getSelectionModel().select(1);
				}
				else {
					theController.getTextFieldAircraftEngineZList().get(i).setText("0.0");
					theController.getChoiceBoxAircraftEngineZUnitList().get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)

					if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("BURIED")
							)
						theController.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("WING")
							)
						theController.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(1);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("AFT_FUSELAGE")
							)
						theController.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(2);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("HTAIL")
							)
						theController.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(3);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("REAR_FUSELAGE")
							)
						theController.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(4);

				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) {
					theController.getTextFieldAircraftEngineTiltList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getTiltingAngle()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getTiltingAngle().getUnit().toString().equalsIgnoreCase("�")
							|| Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getTiltingAngle().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxAircraftEngineTiltUnitList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getTiltingAngle().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxAircraftEngineTiltUnitList().get(i).getSelectionModel().select(1);
				}
				else {
					theController.getTextFieldAircraftEngineTiltList().get(i).setText("0.0");
					theController.getChoiceBoxAircraftEngineTiltUnitList().get(i).getSelectionModel().select(0);
				}
			}
		}

		//---------------------------------------------------------------------------------
		// NACELLE NUMBER CHECK:
		if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
				theController.getTabPaneAircraftNacelles().getTabs().size()) {
			
			int iStart = theController.getTabPaneAircraftNacelles().getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++)
				theController.getInputManagerControllerUtilities().addAircraftNacelleImplementation();
			
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER ENGINES:
		//---------------------------------------------------------------------------------
		NodeList nodelistNacelles = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//nacelles/nacelle");
		if(nodelistNacelles != null) {
			for (int i = 0; i < nodelistNacelles.getLength(); i++) {
				//..........................................................................................................
				Node nodeNacelle  = nodelistNacelles.item(i); 
				Element elementNacelle = (Element) nodeNacelle;
				if(elementNacelle.getAttribute("file") != null)
					theController.getTextFieldsAircraftNacelleFileList().get(i).setText(
							dirNacelles
							+ File.separator
							+ elementNacelle.getAttribute("file")	
							);
				else
					theController.getTextFieldsAircraftNacelleFileList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null) {
					theController.getTextFieldAircraftNacelleXList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getXApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxAircraftNacelleXUnitList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxAircraftNacelleXUnitList().get(i).getSelectionModel().select(1);
				}
				else {
					theController.getTextFieldAircraftNacelleXList().get(i).setText("0.0");
					theController.getChoiceBoxAircraftNacelleXUnitList().get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null) {
					theController.getTextFieldAircraftNacelleYList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getYApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxAircraftNacelleYUnitList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxAircraftNacelleYUnitList().get(i).getSelectionModel().select(1);
				}
				else {
					theController.getTextFieldAircraftNacelleYList().get(i).setText("0.0");
					theController.getChoiceBoxAircraftNacelleYUnitList().get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null) {
					theController.getTextFieldAircraftNacelleZList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getZApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxAircraftNacelleZUnitList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxAircraftNacelleZUnitList().get(i).getSelectionModel().select(1);
				}
				else {
					theController.getTextFieldAircraftNacelleZList().get(i).setText("0.0");
					theController.getChoiceBoxAircraftNacelleZUnitList().get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)

					if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("WING")
							)
						theController.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("FUSELAGE")
							)
						theController.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(1);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("HTAIL")
							)
						theController.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(2);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("UNDERCARRIAGE_HOUSING")
							)
						theController.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(3);

			}
		}

		//---------------------------------------------------------------------------------
		// LANDING GEARS:
		String landingGearsFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//landing_gears/@file");
		if(landingGearsFileName != null) 
			theController.getTextFieldAircraftLandingGearsFile().setText(
					dirLandingGears 
					+ File.separator
					+ landingGearsFileName
					);
		else
			theController.getTextFieldAircraftLandingGearsFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getLandingGears() != null) {

			theController.getTextFieldAircraftNoseLandingGearsX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getXApexConstructionAxesNoseGear()
							.getEstimatedValue()
							)
					);
			theController.getTextFieldAircraftMainLandingGearsX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getXApexConstructionAxesMainGear()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxesNoseGear().getUnit().toString().equalsIgnoreCase("m"))
				theController.getNoseLandingGearsXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxesNoseGear().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getNoseLandingGearsXUnitChoiceBox().getSelectionModel().select(1);
			
			if(Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxesMainGear().getUnit().toString().equalsIgnoreCase("m"))
				theController.getMainLandingGearsXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxesMainGear().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getMainLandingGearsXUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftNoseLandingGearsX().setText("0.0");
			theController.getNoseLandingGearsXUnitChoiceBox().getSelectionModel().select(0);
			theController.getTextFieldAircraftMainLandingGearsX().setText("0.0");
			theController.getMainLandingGearsXUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getLandingGears() != null) {

			theController.getTextFieldAircraftNoseLandingGearsY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getYApexConstructionAxesNoseGear()
							.getEstimatedValue()
							)
					);
			theController.getTextFieldAircraftMainLandingGearsY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getYApexConstructionAxesMainGear()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxesNoseGear().getUnit().toString().equalsIgnoreCase("m"))
				theController.getMainLandingGearsYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxesNoseGear().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getNoseLandingGearsYUnitChoiceBox().getSelectionModel().select(1);
			
			if(Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxesMainGear().getUnit().toString().equalsIgnoreCase("m"))
				theController.getMainLandingGearsYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxesMainGear().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getMainLandingGearsYUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftNoseLandingGearsY().setText("0.0");
			theController.getNoseLandingGearsYUnitChoiceBox().getSelectionModel().select(0);
			theController.getTextFieldAircraftMainLandingGearsY().setText("0.0");
			theController.getMainLandingGearsYUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getLandingGears() != null) {

			theController.getTextFieldAircraftNoseLandingGearsZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getZApexConstructionAxesNoseGear()
							.getEstimatedValue()
							)
					);
			theController.getTextFieldAircraftMainLandingGearsZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getZApexConstructionAxesMainGear()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxesNoseGear().getUnit().toString().equalsIgnoreCase("m"))
				theController.getNoseLandingGearsZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxesNoseGear().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getNoseLandingGearsZUnitChoiceBox().getSelectionModel().select(1);
			
			if(Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxesMainGear().getUnit().toString().equalsIgnoreCase("m"))
				theController.getMainLandingGearsZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxesMainGear().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getMainLandingGearsZUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftNoseLandingGearsZ().setText("0.0");
			theController.getNoseLandingGearsZUnitChoiceBox().getSelectionModel().select(0);
			theController.getTextFieldAircraftMainLandingGearsZ().setText("0.0");
			theController.getMainLandingGearsZUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getLandingGears() != null)

			if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("FUSELAGE")
					)
				theController.getLandingGearsMountingPositionTypeChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("WING")
					)
				theController.getLandingGearsMountingPositionTypeChoiceBox().getSelectionModel().select(1);
			else if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("NACELLE")
					)
				theController.getLandingGearsMountingPositionTypeChoiceBox().getSelectionModel().select(2);

		//---------------------------------------------------------------------------------
		// SYSTEMS:
		if(Main.getTheAircraft().getSystems() != null) {
		
			if(Main.getTheAircraft().getSystems()
					.getTheSystemsInterface()
					.getPrimaryElectricSystemsType()
					.toString()
					.equalsIgnoreCase("AC")
					)
				theController.getSystemsPrimaryElectricalTypeChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getSystems()
					.getTheSystemsInterface()
					.getPrimaryElectricSystemsType()
					.toString()
					.equalsIgnoreCase("DC")
					)
				theController.getSystemsPrimaryElectricalTypeChoiceBox().getSelectionModel().select(1);
			
		}
	}
	

	
	public void logFuselageFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaFuselageConsoleOutput().setText(
				Main.getTheAircraft().getFuselage().toString()
				);

		//---------------------------------------------------------------------------------
		// ADJUST CRITERION CHOICE BOX:
		if(Main.getTheAircraft() != null)
			theController.getFuselageAdjustCriterionChoiceBox().setDisable(false);
		
		//---------------------------------------------------------------------------------
		// PRESSURIZED FLAG: 
		if(Main.getTheAircraft().getFuselage().getPressurized().equals(Boolean.TRUE))
			theController.getFuselagePressurizedCheckBox().setSelected(true);
			
		//---------------------------------------------------------------------------------
		// DECK NUMBER:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageDeckNumber().setText(
					Integer.toString(
							Main.getTheAircraft()
							.getFuselage()
							
							.getDeckNumber()
							)
					);
		else
			theController.getTextFieldFuselageDeckNumber().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// LENGTH:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageLength().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getFuselageLength()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getFuselageLength().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageLengthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getFuselageLength().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageLengthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageLength().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// SURFACE ROUGHNESS:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageSurfaceRoughness().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getRoughness()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageRoughnessUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageRoughnessUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageSurfaceRoughness().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// LENGTH RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageNoseLengthRatio().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getNoseLengthRatio()
					.toString()
					);
		else
			theController.getTextFieldFuselageNoseLengthRatio().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE TIP OFFSET RATIO:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageNoseTipOffset().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getNoseTipOffset()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getNoseTipOffset().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageNoseTipOffsetZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getNoseTipOffset().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageNoseTipOffsetZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageNoseTipOffset().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE DX CAP PERCENT:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageNoseDxCap().setText(
					Double.toString(
							Main.getTheAircraft()
							.getFuselage()
							
							.getNoseCapOffsetPercent()
							)
					);
		else
			theController.getTextFieldFuselageNoseDxCap().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// WINDSHIELD TYPE:
		if(Main.getTheAircraft().getFuselage() != null) { 
			if(theController.getWindshieldTypeChoiceBox() != null) {
				if(Main.getTheAircraft().getFuselage().getWindshieldType() == WindshieldTypeEnum.DOUBLE)
					theController.getWindshieldTypeChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft().getFuselage().getWindshieldType() == WindshieldTypeEnum.FLAT_FLUSH)		
					theController.getWindshieldTypeChoiceBox().getSelectionModel().select(1);
				else if(Main.getTheAircraft().getFuselage().getWindshieldType() == WindshieldTypeEnum.FLAT_PROTRUDING)
					theController.getWindshieldTypeChoiceBox().getSelectionModel().select(2);
				else if(Main.getTheAircraft().getFuselage().getWindshieldType() == WindshieldTypeEnum.SINGLE_ROUND)
					theController.getWindshieldTypeChoiceBox().getSelectionModel().select(3);
				else if(Main.getTheAircraft().getFuselage().getWindshieldType() == WindshieldTypeEnum.SINGLE_SHARP)
					theController.getWindshieldTypeChoiceBox().getSelectionModel().select(4);
			}
		}
		
		//---------------------------------------------------------------------------------
		// WINDSHIELD WIDTH:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageNoseWindshieldWidth().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getWindshieldWidth()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getWindshieldWidth().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageWindshieldWidthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getWindshieldWidth().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageWindshieldWidthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageNoseWindshieldWidth().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// WINDSHIELD HEIGHT:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageNoseWindshieldHeight().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getWindshieldHeight()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getWindshieldHeight().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageWindshieldHeightUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getWindshieldHeight().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageWindshieldHeightUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageNoseWindshieldHeight().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE MID-SECTION HEIGHT TO TOTAL SECTION HEIGHT RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageNoseMidSectionHeight().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionNoseMidLowerToTotalHeightRatio()
					.toString()
					);
		else
			theController.getTextFieldFuselageNoseMidSectionHeight().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE MID-SECTION RHO UPPER:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageNoseMidSectionRhoUpper().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionMidNoseRhoUpper()
					.toString()
					);
		else
			theController.getTextFieldFuselageNoseMidSectionRhoUpper().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE MID-SECTION RHO LOWER:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageNoseMidSectionRhoLower().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionMidNoseRhoLower()
					.toString()
					);
		else
			theController.getTextFieldFuselageNoseMidSectionRhoLower().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER LENGTH RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageCylinderLengthRatio().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getCylinderLengthRatio()
					.toString()
					);
		else
			theController.getTextFieldFuselageCylinderLengthRatio().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION WIDTH:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageCylinderSectionWidth().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getSectionCylinderWidth()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getSectionCylinderWidth().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageCylinderSectionWidthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getSectionCylinderWidth().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageCylinderSectionWidthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageCylinderSectionWidth().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION HEIGHT:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageCylinderSectionHeight().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getSectionCylinderHeight()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getSectionCylinderHeight().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageCylinderSectionHeightUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getSectionCylinderHeight().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageCylinderSectionHeightUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageCylinderSectionHeight().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION HEIGHT FROM GROUND:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageCylinderHeightFromGround().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getHeightFromGround()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getHeightFromGround().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageHeightFromGroundUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getHeightFromGround().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageHeightFromGroundUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageCylinderHeightFromGround().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION HEIGHT TO TOTAL HEIGH RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageCylinderSectionHeightRatio().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionCylinderLowerToTotalHeightRatio()
					.toString()
					);
		else
			theController.getTextFieldFuselageCylinderSectionHeightRatio().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION RHO UPPER:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageCylinderSectionRhoUpper().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionCylinderRhoUpper()
					.toString()
					);
		else
			theController.getTextFieldFuselageCylinderSectionRhoUpper().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION RHO LOWER:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageCylinderSectionRhoLower().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionCylinderRhoLower()
					.toString()
					);
		else
			theController.getTextFieldFuselageCylinderSectionRhoLower().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// TAIL CONE TIP OFFSET:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageTailTipOffset().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getTailTipOffset()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getTailTipOffset().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageTailTipOffsetZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getTailTipOffset().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageTailTipOffsetZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageTailTipOffset().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// TAIL CONE DX CAP PERCENT:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageTailDxCap().setText(
					Double.toString(
							Main.getTheAircraft()
							.getFuselage()
							
							.getTailCapOffsetPercent()
							)
					);
		else
			theController.getTextFieldFuselageTailDxCap().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// TAIL CONE MID SECTION HEIGHT TO TOTAL HEIGHT RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageTailMidSectionHeight().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionTailMidLowerToTotalHeightRatio()
					.toString()
					);
		else
			theController.getTextFieldFuselageTailMidSectionHeight().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// TAIL CONE MID SECTION RHO UPPER:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageTailMidRhoUpper().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionMidTailRhoUpper()
					.toString()
					);
		else
			theController.getTextFieldFuselageTailMidRhoUpper().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// TAIL CONE MID SECTION RHO LOWER:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageTailMidRhoLower().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionMidTailRhoLower()
					.toString()
					);
		else
			theController.getTextFieldFuselageTailMidRhoLower().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// SPOILERS NUMBER CHECK:
		if (Main.getTheAircraft().getFuselage().getSpoilers().size() >= 
				theController.getTabPaneFuselageSpoilers().getTabs().size()) {
			
			int iStart = theController.getTabPaneFuselageSpoilers().getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getFuselage().getSpoilers().size(); i++)
				theController.addFuselageSpoiler();
			
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER SPOILERS:
		for (int i=0; i<Main.getTheAircraft().getFuselage().getSpoilers().size(); i++) {
			
			SpoilerCreator currentSpoiler = Main.getTheAircraft().getFuselage().getSpoilers().get(i);
			
			//---------------------------------------------------------------------------------
			// INNER SPANWISE POSITION:
			if(Double.valueOf(currentSpoiler.getInnerStationSpanwisePosition()) != null) {
				theController.getTextFieldFuselageInnerSpanwisePositionSpoilerList().get(i).setText(
						String.valueOf(currentSpoiler.getInnerStationSpanwisePosition())
						);
			}
			else
				theController.getTextFieldFuselageInnerSpanwisePositionSpoilerList().get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// OUTER SPANWISE POSITION:
			if(Double.valueOf(currentSpoiler.getOuterStationSpanwisePosition()) != null) {
				theController.getTextFieldFuselageOuterSpanwisePositionSpoilerList().get(i).setText(
						String.valueOf(currentSpoiler.getOuterStationSpanwisePosition())
						);
			}
			else
				theController.getTextFieldFuselageOuterSpanwisePositionSpoilerList().get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// INNER CHORDWISE POSITION:
			if(Double.valueOf(currentSpoiler.getInnerStationChordwisePosition()) != null) {
				theController.getTextFieldFuselageInnerChordwisePositionSpoilerList().get(i).setText(
						String.valueOf(currentSpoiler.getInnerStationChordwisePosition())
						);
			}
			else
				theController.getTextFieldFuselageInnerChordwisePositionSpoilerList().get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// OUTER CHORDWISE POSITION:
			if(Double.valueOf(currentSpoiler.getOuterStationChordwisePosition()) != null) {
				theController.getTextFieldFuselageOuterChordwisePositionSpoilerList().get(i).setText(
						String.valueOf(currentSpoiler.getOuterStationChordwisePosition())
						);
			}
			else
				theController.getTextFieldFuselageOuterChordwisePositionSpoilerList().get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// MINIMUM DEFLECTION:
			if(currentSpoiler.getMinimumDeflection() != null) {
				
				theController.getTextFieldFuselageMinimumDeflectionAngleSpoilerList().get(i).setText(
						String.valueOf(currentSpoiler.getMinimumDeflection().getEstimatedValue())
						);
				
				if(currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("�")
						|| currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getChoiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(0);
				else if(currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getChoiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldFuselageMinimumDeflectionAngleSpoilerList().get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// MAXIMUM DEFLECTION:
			if(currentSpoiler.getMaximumDeflection() != null) {
				
				theController.getTextFieldFuselageMaximumDeflectionAngleSpoilerList().get(i).setText(
						String.valueOf(currentSpoiler.getMaximumDeflection().getEstimatedValue())
						);
				
				if(currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("�")
						|| currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getChoiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(0);
				else if(currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getChoiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldFuselageMaximumDeflectionAngleSpoilerList().get(i).setText(
						"NOT INITIALIZED"
						);
			
		}
	}


	
	public void logCabinConfigutionFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaCabinConfigurationConsoleOutput().setText(
				Main.getTheAircraft().getCabinConfiguration().toString()
				);

		if(Main.getTheAircraft().getCabinConfiguration() != null) {

			//---------------------------------------------------------------------------------
			// ACTUAL PASSENGERS NUMBER:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getDesignPassengerNumber()) != null) 
				theController.getTextFieldActualPassengersNumber().setText(
					Integer.toString(
							Main.getTheAircraft()
							.getCabinConfiguration()
							.getDesignPassengerNumber()
							)
					);
			else
				theController.getTextFieldActualPassengersNumber().setText("0");
			//---------------------------------------------------------------------------------
			// FLIGHT CREW NUMBER:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getFlightCrewNumber()) != null)
				theController.getTextFieldFlightCrewNumber().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getFlightCrewNumber()
								)
						);
			else
				theController.getTextFieldFlightCrewNumber().setText("0");
			//---------------------------------------------------------------------------------
			// CLASSES NUMBER:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getClassesNumber()) != null) 
				theController.getTextFieldClassesNumber().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getClassesNumber()
								)
						);
			else
				theController.getTextFieldFlightCrewNumber().setText("0");
			//---------------------------------------------------------------------------------
			// CLASSES TYPE:
			if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType() != null) {
				
				for(int i=0; i<Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().size(); i++) {
					
					// CLASS 1
					if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.ECONOMY))
						theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(0);
					if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.BUSINESS))
						theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(1);
					if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.FIRST))
						theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(2);
					
					if (i==1) {
					
						// CLASS 1
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.ECONOMY))
							theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.BUSINESS))
							theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.FIRST))
							theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(2);

						// CLASS 2
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(1).equals(ClassTypeEnum.ECONOMY))
							theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(1).equals(ClassTypeEnum.BUSINESS))
							theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(1).equals(ClassTypeEnum.FIRST))
							theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().select(2);
						
					}
					if (i==2) {
						
						// CLASS 1
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.ECONOMY))
							theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.BUSINESS))
							theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.FIRST))
							theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(2);

						// CLASS 2
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(1).equals(ClassTypeEnum.ECONOMY))
							theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(1).equals(ClassTypeEnum.BUSINESS))
							theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(1).equals(ClassTypeEnum.FIRST))
							theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().select(2);
						
						// CLASS 3
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(2).equals(ClassTypeEnum.ECONOMY))
							theController.getCabinConfigurationClassesTypeChoiceBox3().getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(2).equals(ClassTypeEnum.BUSINESS))
							theController.getCabinConfigurationClassesTypeChoiceBox3().getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(2).equals(ClassTypeEnum.FIRST))
							theController.getCabinConfigurationClassesTypeChoiceBox3().getSelectionModel().select(2);
						
					}
				}
			}
			
			//---------------------------------------------------------------------------------
			// AISLES NUMBER:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getAislesNumber()) != null)
				theController.getTextFieldAislesNumber().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getAislesNumber()
								)
						);
			else
				theController.getTextFieldAislesNumber().setText("0");
			//---------------------------------------------------------------------------------
			// X COORDINATE FIRST ROW:
			if(Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow() != null) {
				
				theController.getTextFieldXCoordinateFirstRow().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getXCoordinatesFirstRow()
								.doubleValue(SI.METER)
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getXCoordinatesFirstRow().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationXCoordinateFirstRowUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getXCoordinatesFirstRow().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationXCoordinateFirstRowUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldXCoordinateFirstRow().setText("0.0");
				theController.getCabinConfigurationXCoordinateFirstRowUnitChoiceBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// NUMBER OF BRAKES ECONOMY:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksEconomyClass()) != null)
				theController.getTextFieldNumberOfBrakesEconomy().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfBreaksEconomyClass()
								)
						);
			else
				theController.getTextFieldNumberOfBrakesEconomy().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF BRAKES BUSINESS:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksBusinessClass()) != null)
				theController.getTextFieldNumberOfBrakesBusiness().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfBreaksBusinessClass()
								)
						);
			else
				theController.getTextFieldNumberOfBrakesBusiness().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF BRAKES FIRST:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksFirstClass()) != null)
				theController.getTextFieldNumberOfBrakesFirst().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfBreaksFirstClass()
								)
						);
			else
				theController.getTextFieldNumberOfBrakesFirst().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF ROWS ECONOMY:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsEconomyClass()) != null)
				theController.getTextFieldNumberOfRowsEconomy().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfRowsEconomyClass()
								)
						);
			else
				theController.getTextFieldNumberOfRowsEconomy().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF ROWS BUSINESS:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsBusinessClass()) != null)
				theController.getTextFieldNumberOfRowsBusiness().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfRowsBusinessClass()
								)
						);
			else
				theController.getTextFieldNumberOfRowsBusiness().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF ROWS FIRST:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsFirstClass()) != null)
				theController.getTextFieldNumberOfRowsFirst().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfRowsFirstClass()
								)
						);
			else
				theController.getTextFieldNumberOfRowsFirst().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF COLUMNS ECONOMY:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsEconomyClass() != null)
				theController.getTextFieldNumberOfColumnsEconomy().setText(
						String.valueOf(
								Arrays.toString(
										Main.getTheAircraft()
										.getCabinConfiguration()
										.getNumberOfColumnsEconomyClass()
										)
								)
						);
			else
				theController.getTextFieldNumberOfColumnsEconomy().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF COLUMNS BUSINESS:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsBusinessClass() != null)
				theController.getTextFieldNumberOfColumnsBusiness().setText(
						String.valueOf(
								Arrays.toString(
										Main.getTheAircraft()
										.getCabinConfiguration()
										.getNumberOfColumnsBusinessClass()
										)
								)
						);
			else
				theController.getTextFieldNumberOfColumnsBusiness().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF COLUMNS FIRST:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsFirstClass() != null)
				theController.getTextFieldNumberOfColumnsFirst().setText(
						String.valueOf(
								Arrays.toString(
										Main.getTheAircraft()
										.getCabinConfiguration()
										.getNumberOfColumnsFirstClass()
										)
								)
						);
			else
				theController.getTextFieldNumberOfColumnsFirst().setText("0");
			//---------------------------------------------------------------------------------
			// SEATS PITCH ECONOMY:
			if(Main.getTheAircraft().getCabinConfiguration().getPitchEconomyClass() != null) {
				
				theController.getTextFieldSeatsPitchEconomy().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getPitchEconomyClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchEconomyClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationSeatsPitchEconomyUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchEconomyClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationSeatsPitchEconomyUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldSeatsPitchEconomy().setText("0.0");
				theController.getCabinConfigurationSeatsPitchEconomyUnitChoiceBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// SEATS PITCH BUSINESS:
			if(Main.getTheAircraft().getCabinConfiguration().getPitchBusinessClass() != null) {
				
				theController.getTextFieldSeatsPitchBusiness().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getPitchBusinessClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchBusinessClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationSeatsPitchBusinessUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchBusinessClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationSeatsPitchBusinessUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldSeatsPitchBusiness().setText("0.0");
				theController.getCabinConfigurationSeatsPitchBusinessUnitChoiceBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// SEATS PITCH FIRST:
			if(Main.getTheAircraft().getCabinConfiguration().getPitchFirstClass() != null) {
				
				theController.getTextFieldSeatsPitchFirst().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getPitchFirstClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchFirstClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationSeatsPitchFirstUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchFirstClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationSeatsPitchFirstUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldSeatsPitchFirst().setText("0.0");
				theController.getCabinConfigurationSeatsPitchFirstUnitChoiceBox().getSelectionModel().select(0);
			}
			//---------------------------------------------------------------------------------
			// SEATS WIDTH ECONOMY:
			if(Main.getTheAircraft().getCabinConfiguration().getWidthEconomyClass() != null) {
				
				theController.getTextFieldSeatsWidthEconomy().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getWidthEconomyClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthEconomyClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationSeatsWidthEconomyUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthEconomyClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationSeatsWidthEconomyUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldSeatsWidthEconomy().setText("0.0");
				theController.getCabinConfigurationSeatsWidthEconomyUnitChoiceBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// SEATS WIDTH BUSINESS:
			if(Main.getTheAircraft().getCabinConfiguration().getWidthBusinessClass() != null) {
				
				theController.getTextFieldSeatsWidthBusiness().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getWidthBusinessClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthBusinessClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationSeatsWidthBusinessUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthBusinessClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationSeatsWidthBusinessUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldSeatsWidthBusiness().setText("0.0");
				theController.getCabinConfigurationSeatsWidthBusinessUnitChoiceBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// SEATS WIDTH FIRST:
			if(Main.getTheAircraft().getCabinConfiguration().getWidthFirstClass() != null) {
				
				theController.getTextFieldSeatsWidthFirst().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getWidthFirstClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthFirstClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationSeatsWidthFirstUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthFirstClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationSeatsWidthFirstUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldSeatsWidthFirst().setText("0.0");
				theController.getCabinConfigurationSeatsWidthFirstUnitChoiceBox().getSelectionModel().select(0);
			}
			//---------------------------------------------------------------------------------
			// DISTANCE FROM WALL ECONOMY:
			if(Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallEconomyClass() != null) {
				
				theController.getTextFieldDistanceFromWallEconomy().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getDistanceFromWallEconomyClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallEconomyClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationDistanceFromWallEconomyUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallEconomyClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationDistanceFromWallEconomyUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldDistanceFromWallEconomy().setText("0.0");
				theController.getCabinConfigurationDistanceFromWallEconomyUnitChoiceBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// DISTANCE FROM WALL BUSINESS:
			if(Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallBusinessClass() != null) {
				
				theController.getTextFieldDistanceFromWallBusiness().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getDistanceFromWallBusinessClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallBusinessClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationDistanceFromWallBusinessUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallBusinessClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationDistanceFromWallBusinessUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldDistanceFromWallBusiness().setText("0.0");
				theController.getCabinConfigurationDistanceFromWallBusinessUnitChoiceBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// DISTANCE FROM WALL FIRST:
			if(Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallFirstClass() != null) {
				
				theController.getTextFieldDistanceFromWallFirst().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getDistanceFromWallFirstClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallFirstClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationDistanceFromWallFirstUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallFirstClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationDistanceFromWallFirstUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldDistanceFromWallFirst().setText("0.0");
				theController.getCabinConfigurationDistanceFromWallFirstUnitChoiceBox().getSelectionModel().select(0);
			}
		}
	}
	

	
	public void logWingFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaWingConsoleOutput().setText(
				Main.getTheAircraft().getWing().toString()
				+ "\n\n\n" + Main.getTheAircraft().getFuelTank().toString()
				);

		if(Main.getTheAircraft().getWing() != null) {

			//---------------------------------------------------------------------------------
			// ADJUST CRITERION CHOICE BOX:
			if(Main.getTheAircraft() != null)
				theController.getWingAdjustCriterionChoiceBox().setDisable(false);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING FLAG: 
			if(Main.getTheAircraft().getWing().getEquivalentWingFlag() == true)
				theController.getEquivalentWingCheckBox().setSelected(true);

			//---------------------------------------------------------------------------------
			// MAIN SPAR LOCATION:
			if(Double.valueOf(Main.getTheAircraft().getWing().getMainSparDimensionlessPosition()) != null) 
				theController.getTextFieldWingMainSparAdimensionalPosition().setText(
						Double.toString(
								Main.getTheAircraft()
								.getWing()
								
								.getMainSparDimensionlessPosition()
								)
						);
			else
				theController.getTextFieldWingMainSparAdimensionalPosition().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// SECONDARY SPAR LOCATION:
			if(Double.valueOf(Main.getTheAircraft().getWing().getSecondarySparDimensionlessPosition()) != null) 
				theController.getTextFieldWingSecondarySparAdimensionalPosition().setText(
						Double.toString(
								Main.getTheAircraft()
								.getWing()
								
								.getSecondarySparDimensionlessPosition()
								)
						);
			else
				theController.getTextFieldWingSecondarySparAdimensionalPosition().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// ROUGHNESS:
			if(Main.getTheAircraft().getWing().getRoughness() != null) {
				
				theController.getTextFieldWingRoughness().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getRoughness()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing()
						.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
					theController.getWingRoughnessUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing()
						.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getWingRoughnessUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldWingRoughness().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// WINGLET HEIGHT:
			if(Main.getTheAircraft().getWing().getWingletHeight() != null) {
				
				theController.getTextFieldWingWingletHeight().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getWingletHeight()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing()
						.getWingletHeight().getUnit().toString().equalsIgnoreCase("m"))
					theController.getWingWingletHeightUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing()
						.getWingletHeight().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getWingWingletHeightUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldWingWingletHeight().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING AREA:
			if(Main.getTheAircraft().getWing().getSurfacePlanform() != null) {
				
				theController.getTextFieldEquivalentWingArea().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getSurfacePlanform()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing()
						.getSurfacePlanform().getUnit().toString().equalsIgnoreCase("m�"))
					theController.getEquivalentWingAreaUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing()
						.getSurfacePlanform().getUnit().toString().equalsIgnoreCase("ft�"))
					theController.getEquivalentWingAreaUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldEquivalentWingArea().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING ASPECT RATIO:
			if(Main.getTheAircraft().getWing().getAspectRatio() != null) {
				
				theController.getTextFieldEquivalentWingAspectRatio().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getAspectRatio()
								)
						);
			}
			else
				theController.getTextFieldEquivalentWingAspectRatio().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING KINK POSITION:
			if(Double.valueOf(Main.getTheAircraft().getWing().getEquivalentWing().getRealWingDimensionlessKinkPosition()) != null) {
				
				theController.getTextFieldEquivalentWingKinkPosition().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getRealWingDimensionlessKinkPosition()
								)
						);
			}
			else 
				theController.getTextFieldEquivalentWingKinkPosition().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING SWEEP LE:
			if(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge() != null) {
				
				theController.getTextFieldEquivalentWingSweepLeadingEdge().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getPanels().get(0)
								.getSweepLeadingEdge()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("�")
						|| Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getEquivalentWingSweepLEUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getEquivalentWingSweepLEUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldEquivalentWingSweepLeadingEdge().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING TWIST AT TIP:
			if(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip() != null) {
				
				theController.getTextFieldEquivalentWingTwistAtTip().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getPanels().get(0)
								.getTwistGeometricAtTip()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getTwistAerodynamicAtTip().getUnit().toString().equalsIgnoreCase("�")
						|| Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getTwistAerodynamicAtTip().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getEquivalentWingTwistAtTipUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getTwistAerodynamicAtTip().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getEquivalentWingTwistAtTipUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldEquivalentWingTwistAtTip().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING DIHEDRAL:
			if(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getDihedral() != null) {
				
				theController.getTextFieldEquivalentWingDihedral().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getPanels().get(0)
								.getDihedral()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getDihedral().getUnit().toString().equalsIgnoreCase("�")
						|| Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getEquivalentWingDihedralUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getEquivalentWingDihedralUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldEquivalentWingDihedral().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING TAPER RATIO:
			if(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTaperRatio() != null) {
				
				theController.getTextFieldEquivalentWingTaperRatio().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getPanels().get(0)
								.getTaperRatio()
								)
						);
			}
			else
				theController.getTextFieldEquivalentWingTaperRatio().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING X OFFSET LE:
			if(Double.valueOf(Main.getTheAircraft().getWing().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordLE()) != null) {
				
				theController.getTextFieldEquivalentWingRootXOffsetLE().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getRealWingDimensionlessXOffsetRootChordLE()
								)
						);
			}
			else
				theController.getTextFieldEquivalentWingRootXOffsetLE().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// EQUIVALENT WING X OFFSET TE:
			if(Double.valueOf(Main.getTheAircraft().getWing().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordTE()) != null) {
				
				theController.getTextFieldEquivalentWingRootXOffsetTE().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getRealWingDimensionlessXOffsetRootChordTE()
								)
						);
			}
			else
				theController.getTextFieldEquivalentWingRootXOffsetTE().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING AIRFOIL ROOT PATH:
			if(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getAirfoilRoot() != null) {
				
				theController.getTextFieldEquivalentWingAirfoilRootPath().setText(
						String.valueOf(
								Main.getInputDirectoryPath()
								+ File.separator
								+ "Template_Aircraft"
								+ File.separator
								+ "lifting_surfaces"
								+ File.separator
								+ "airfoils"
								+ File.separator
								+ Main.getTheAircraft()
								.getWing()
								.getEquivalentWing()
								.getPanels().get(0)
								.getAirfoilRoot()
								.getName()
								+ ".xml"
								)
						);
			}
			else
				theController.getTextFieldEquivalentWingAirfoilRootPath().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING AIRFOIL KINK PATH:
			if(Main.getTheAircraft().getWing().getEquivalentWing().getEquivalentWingAirfoilKink() != null) {
				
				theController.getTextFieldEquivalentWingAirfoilKinkPath().setText(
						String.valueOf(
								Main.getInputDirectoryPath()
								+ File.separator
								+ "Template_Aircraft"
								+ File.separator
								+ "lifting_surfaces"
								+ File.separator
								+ "airfoils"
								+ File.separator
								+ Main.getTheAircraft()
								.getWing()
								.getEquivalentWing()
								.getEquivalentWingAirfoilKink()
								.getName()
								+ ".xml"
								)
						);
			}
			else
				theController.getTextFieldEquivalentWingAirfoilKinkPath().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING AIRFOIL TIP PATH:
			if(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getAirfoilTip() != null) {
				
				theController.getTextFieldEquivalentWingAirfoilTipPath().setText(
						String.valueOf(
								Main.getInputDirectoryPath()
								+ File.separator
								+ "Template_Aircraft"
								+ File.separator
								+ "lifting_surfaces"
								+ File.separator
								+ "airfoils"
								+ File.separator
								+ Main.getTheAircraft()
								.getWing()
								.getEquivalentWing()
								.getPanels().get(0)
								.getAirfoilTip()
								.getName()
								+ ".xml"
								)
						);
			}
			else
				theController.getTextFieldEquivalentWingAirfoilTipPath().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// PANELS NUMBER CHECK:
			if (Main.getTheAircraft().getWing().getPanels().size() >= 
					theController.getTabPaneWingPanels().getTabs().size()) {
				
				int iStart = theController.getTabPaneWingPanels().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getWing().getPanels().size(); i++)
					theController.addWingPanel();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER PANELS:
			for (int i=0; i<Main.getTheAircraft().getWing().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getWing().getPanels().get(i);
				
				//---------------------------------------------------------------------------------
				// LINKED TO (from the second panel on):
				if(i>0)
					if(currentPanel.isLinked())
						theController.getCheckBoxWingLinkedToPreviousPanelList().get(i-1).setSelected(true);
				
				//---------------------------------------------------------------------------------
				// PANEL SPAN:
				if(currentPanel.getSpan() != null) {
					
					theController.getTextFieldWingSpanPanelList().get(i).setText(
							String.valueOf(currentPanel.getSpan().getEstimatedValue())
							);
					
					if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxWingSpanPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxWingSpanPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingSpanPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// SWEEP LE:
				if(currentPanel.getSweepLeadingEdge() != null) {
					
					theController.getTextFieldWingSweepLEPanelList().get(i).setText(
							String.valueOf(currentPanel.getSweepLeadingEdge().getEstimatedValue())
							);
					
					if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingSweepLEPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingSweepLEPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingSweepLEPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// DIHEDRAL:
				if(currentPanel.getDihedral() != null) {
					
					theController.getTextFieldWingDihedralPanelList().get(i).setText(
							String.valueOf(currentPanel.getDihedral().getEstimatedValue())
							);
					
					if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingDihedralPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingDihedralPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingDihedralPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD (ROOT):
				if(currentPanel.getChordRoot() != null) {
					
					theController.getTextFieldWingInnerChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordRoot().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxWingInnerChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxWingInnerChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingInnerChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER TWIST (ROOT):
				if(currentPanel.getTwistGeometricRoot() != null) {
					
					theController.getTextFieldWingInnerTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricRoot().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingInnerTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingInnerTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingInnerTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER AIRFOIL PATH (ROOT):
				if(currentPanel.getAirfoilRoot().getName() != null) {

					theController.getTextFieldWingInnerAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getWing().getPanels().get(i).getAirfoilRootPath()
							);
				}
				else
					theController.getTextFieldWingInnerAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD (TIP):
				if(currentPanel.getChordTip() != null) {
					
					theController.getTextFieldWingOuterChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordTip().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxWingOuterChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxWingOuterChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingOuterChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER TWIST (TIP):
				if(currentPanel.getTwistGeometricAtTip() != null) {
					
					theController.getTextFieldWingOuterTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricAtTip().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingOuterTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingOuterTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingOuterTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER AIRFOIL PATH (TIP):
				if(currentPanel.getAirfoilTip().getName() != null) {

					theController.getTextFieldWingOuterAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getWing().getPanels().get(i).getAirfoilTipPath()
							);
				}
				else
					theController.getTextFieldWingOuterAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// FLAPS NUMBER CHECK:
			if (Main.getTheAircraft().getWing().getSymmetricFlaps().size() >= 
					theController.getTabPaneWingFlaps().getTabs().size()) {
				
				int iStart = theController.getTabPaneWingFlaps().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getWing().getSymmetricFlaps().size(); i++)
					theController.addFlap();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER FLAPS:
			for (int i=0; i<Main.getTheAircraft().getWing().getSymmetricFlaps().size(); i++) {
				
				SymmetricFlapCreator currentFlap = Main.getTheAircraft().getWing().getSymmetricFlaps().get(i);
				
				//---------------------------------------------------------------------------------
				// TYPE:
				if(currentFlap.getType() != null) {
					if(currentFlap.getType().equals(FlapTypeEnum.SINGLE_SLOTTED))
						theController.getChoiceBoxWingFlapTypeList().get(i).getSelectionModel().select(0);
					else if(currentFlap.getType().equals(FlapTypeEnum.DOUBLE_SLOTTED))
						theController.getChoiceBoxWingFlapTypeList().get(i).getSelectionModel().select(1);
					else if(currentFlap.getType().equals(FlapTypeEnum.PLAIN))
						theController.getChoiceBoxWingFlapTypeList().get(i).getSelectionModel().select(2);
					else if(currentFlap.getType().equals(FlapTypeEnum.FOWLER))
						theController.getChoiceBoxWingFlapTypeList().get(i).getSelectionModel().select(3);
					else if(currentFlap.getType().equals(FlapTypeEnum.OPTIMIZED_FOWLER))
						theController.getChoiceBoxWingFlapTypeList().get(i).getSelectionModel().select(4);
					else if(currentFlap.getType().equals(FlapTypeEnum.TRIPLE_SLOTTED))
						theController.getChoiceBoxWingFlapTypeList().get(i).getSelectionModel().select(5);
				}
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(Double.valueOf(currentFlap.getInnerStationSpanwisePosition()) != null) {
					theController.getTextFieldWingInnerPositionFlapList().get(i).setText(
							String.valueOf(currentFlap.getInnerStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldWingInnerPositionFlapList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(Double.valueOf(currentFlap.getOuterStationSpanwisePosition()) != null) {
					theController.getTextFieldWingOuterPositionFlapList().get(i).setText(
							String.valueOf(currentFlap.getOuterStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldWingOuterPositionFlapList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(Double.valueOf(currentFlap.getInnerChordRatio()) != null) {
					theController.getTextFieldWingInnerChordRatioFlapList().get(i).setText(
							String.valueOf(currentFlap.getInnerChordRatio())
							);
				}
				else
					theController.getTextFieldWingInnerChordRatioFlapList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(Double.valueOf(currentFlap.getOuterChordRatio())!= null) {
					theController.getTextFieldWingOuterChordRatioFlapList().get(i).setText(
							String.valueOf(currentFlap.getOuterChordRatio())
							);
				}
				else
					theController.getTextFieldWingOuterChordRatioFlapList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentFlap.getMinimumDeflection() != null) {
					
					theController.getTextFieldWingMinimumDeflectionAngleFlapList().get(i).setText(
							String.valueOf(currentFlap.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentFlap.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("�")
							|| currentFlap.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().get(i).getSelectionModel().select(0);
					else if(currentFlap.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingMinimumDeflectionAngleFlapList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentFlap.getMaximumDeflection() != null) {
					
					theController.getTextFieldWingMaximumDeflectionAngleFlapList().get(i).setText(
							String.valueOf(currentFlap.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentFlap.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("�")
							|| currentFlap.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingMaximumDeflectionAngleFlapUnitList().get(i).getSelectionModel().select(0);
					else if(currentFlap.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingMaximumDeflectionAngleFlapUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingMaximumDeflectionAngleFlapList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// SLATS NUMBER CHECK:
			if (Main.getTheAircraft().getWing().getSlats().size() >= 
					theController.getTabPaneWingSlats().getTabs().size()) {
				
				int iStart = theController.getTabPaneWingSlats().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getWing().getSlats().size(); i++)
					theController.addSlat();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER SLATS:
			for (int i=0; i<Main.getTheAircraft().getWing().getSlats().size(); i++) {
				
				SlatCreator currentSlat = Main.getTheAircraft().getWing().getSlats().get(i);
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(Double.valueOf(currentSlat.getInnerStationSpanwisePosition()) != null) {
					theController.getTextFieldWingInnerPositionSlatList().get(i).setText(
							String.valueOf(currentSlat.getInnerStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldWingInnerPositionSlatList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(Double.valueOf(currentSlat.getOuterStationSpanwisePosition()) != null) {
					theController.getTextFieldWingOuterPositionSlatList().get(i).setText(
							String.valueOf(currentSlat.getOuterStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldWingOuterPositionSlatList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(Double.valueOf(currentSlat.getInnerChordRatio()) != null) {
					theController.getTextFieldWingInnerChordRatioSlatList().get(i).setText(
							String.valueOf(currentSlat.getInnerChordRatio())
							);
				}
				else
					theController.getTextFieldWingInnerChordRatioSlatList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(Double.valueOf(currentSlat.getOuterChordRatio()) != null) {
					theController.getTextFieldWingOuterChordRatioSlatList().get(i).setText(
							String.valueOf(currentSlat.getOuterChordRatio())
							);
				}
				else
					theController.getTextFieldWingOuterChordRatioSlatList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// EXTENSION RATIO:
				if(Double.valueOf(currentSlat.getExtensionRatio()) != null) {
					theController.getTextFieldWingExtensionRatioSlatList().get(i).setText(
							String.valueOf(currentSlat.getExtensionRatio())
							);
				}
				else
					theController.getTextFieldWingExtensionRatioSlatList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentSlat.getMinimumDeflection() != null) {
					
					theController.getTextFieldWingMinimumDeflectionAngleSlatList().get(i).setText(
							String.valueOf(currentSlat.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentSlat.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("�")
							|| currentSlat.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingMinimumDeflectionAngleSlatUnitList().get(i).getSelectionModel().select(0);
					else if(currentSlat.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingMinimumDeflectionAngleSlatUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingMinimumDeflectionAngleSlatList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentSlat.getMaximumDeflection() != null) {
					
					theController.getTextFieldWingMaximumDeflectionAngleSlatList().get(i).setText(
							String.valueOf(currentSlat.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentSlat.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("�")
							|| currentSlat.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingMaximumDeflectionAngleSlatUnitList().get(i).getSelectionModel().select(0);
					else if(currentSlat.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingMaximumDeflectionAngleSlatUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingMaximumDeflectionAngleSlatList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
//			//---------------------------------------------------------------------------------
//			// LEFT AILERONS:
//
//			AsymmetricFlapCreator leftAileron = Main.getTheAircraft().getWing().getAsymmetricFlaps().get(0);
//
//			//---------------------------------------------------------------------------------
//			// TYPE:
//			if(leftAileron.getType() != null) {
//				if(leftAileron.getType().equals(FlapTypeEnum.PLAIN))
//					theController.getWingAileron1TypeChoiceBox().getSelectionModel().select(0);
//			}
//			
//			//---------------------------------------------------------------------------------
//			// INNER POSITION:
//			if(Double.valueOf(leftAileron.getInnerStationSpanwisePosition()) != null) {
//				theController.getTextFieldWingInnerPositionAileron1().setText(
//						String.valueOf(leftAileron.getInnerStationSpanwisePosition())
//						);
//			}
//			else
//				theController.getTextFieldWingInnerPositionAileron1().setText(
//						"NOT INITIALIZED"
//						);
//
//			//---------------------------------------------------------------------------------
//			// OUTER POSITION:
//			if(Double.valueOf(leftAileron.getOuterStationSpanwisePosition()) != null) {
//				theController.getTextFieldWingOuterPositionAileron1().setText(
//						String.valueOf(leftAileron.getOuterStationSpanwisePosition())
//						);
//			}
//			else
//				theController.getTextFieldWingOuterPositionAileron1().setText(
//						"NOT INITIALIZED"
//						);
//
//			//---------------------------------------------------------------------------------
//			// INNER CHORD RATIO:
//			if(Double.valueOf(leftAileron.getInnerChordRatio()) != null) {
//				theController.getTextFieldWingInnerChordRatioAileron1().setText(
//						String.valueOf(leftAileron.getInnerChordRatio())
//						);
//			}
//			else
//				theController.getTextFieldWingInnerChordRatioAileron1().setText(
//						"NOT INITIALIZED"
//						);
//
//			//---------------------------------------------------------------------------------
//			// OUTER CHORD RATIO:
//			if(Double.valueOf(leftAileron.getOuterChordRatio()) != null) {
//				theController.getTextFieldWingOuterChordRatioAileron1().setText(
//						String.valueOf(leftAileron.getOuterChordRatio())
//						);
//			}
//			else
//				theController.getTextFieldWingOuterChordRatioAileron1().setText(
//						"NOT INITIALIZED"
//						);
//
//			//---------------------------------------------------------------------------------
//			// MINIMUM DEFLECTION:
//			if(leftAileron.getMinimumDeflection() != null) {
//
//				theController.getTextFieldWingMinimumDeflectionAngleAileron1().setText(
//						String.valueOf(leftAileron.getMinimumDeflection().getEstimatedValue())
//						);
//
//				if(leftAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("�")
//						|| leftAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
//					theController.getWingMinimumDeflectionAngleAileron1UnitChoiceBox().getSelectionModel().select(0);
//				else if(leftAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
//					theController.getWingMinimumDeflectionAngleAileron1UnitChoiceBox().getSelectionModel().select(1);
//
//			}
//			else
//				theController.getTextFieldWingMinimumDeflectionAngleAileron1().setText(
//						"NOT INITIALIZED"
//						);
//
//			//---------------------------------------------------------------------------------
//			// MAXIMUM DEFLECTION:
//			if(leftAileron.getMaximumDeflection() != null) {
//
//				theController.getTextFieldWingMaximumDeflectionAngleAileron1().setText(
//						String.valueOf(leftAileron.getMaximumDeflection().getEstimatedValue())
//						);
//
//				if(leftAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("�")
//						|| leftAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
//					theController.getWingMaximumDeflectionAngleAileron1UnitChoiceBox().getSelectionModel().select(0);
//				else if(leftAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
//					theController.getWingMaximumDeflectionAngleAileron1UnitChoiceBox().getSelectionModel().select(1);
//
//			}
//			else
//				theController.getTextFieldWingMaximumDeflectionAngleAileron1().setText(
//						"NOT INITIALIZED"
//						);
//
//			//---------------------------------------------------------------------------------
//			// RIGHT AILERONS:
//
//			AsymmetricFlapCreator rightAileron = Main.getTheAircraft().getWing().getAsymmetricFlaps().get(1);
//
//			//---------------------------------------------------------------------------------
//			// TYPE:
//			if(rightAileron.getType() != null) {
//				if(rightAileron.getType().equals(FlapTypeEnum.PLAIN))
//					theController.getWingRightAileronTypeChoichBox().getSelectionModel().select(0);
//			}
//			
//			//---------------------------------------------------------------------------------
//			// INNER POSITION:
//			if(Double.valueOf(rightAileron.getInnerStationSpanwisePosition()) != null) {
//				theController.getTextFieldWingInnerPositionAileronRight().setText(
//						String.valueOf(rightAileron.getInnerStationSpanwisePosition())
//						);
//			}
//			else
//				theController.getTextFieldWingInnerPositionAileronRight().setText(
//						"NOT INITIALIZED"
//						);
//
//			//---------------------------------------------------------------------------------
//			// OUTER POSITION:
//			if(Double.valueOf(rightAileron.getOuterStationSpanwisePosition()) != null) {
//				theController.getTextFieldWingOuterPositionAileronRight().setText(
//						String.valueOf(rightAileron.getOuterStationSpanwisePosition())
//						);
//			}
//			else
//				theController.getTextFieldWingOuterPositionAileronRight().setText(
//						"NOT INITIALIZED"
//						);
//
//			//---------------------------------------------------------------------------------
//			// INNER CHORD RATIO:
//			if(Double.valueOf(rightAileron.getInnerChordRatio()) != null) {
//				theController.getTextFieldWingInnerChordRatioAileronRight().setText(
//						String.valueOf(rightAileron.getInnerChordRatio())
//						);
//			}
//			else
//				theController.getTextFieldWingInnerChordRatioAileronRight().setText(
//						"NOT INITIALIZED"
//						);
//
//			//---------------------------------------------------------------------------------
//			// OUTER CHORD RATIO:
//			if(Double.valueOf(rightAileron.getOuterChordRatio()) != null) {
//				theController.getTextFieldWingOuterChordRatioAileronRight().setText(
//						String.valueOf(rightAileron.getOuterChordRatio())
//						);
//			}
//			else
//				theController.getTextFieldWingOuterChordRatioAileronRight().setText(
//						"NOT INITIALIZED"
//						);
//
//			//---------------------------------------------------------------------------------
//			// MINIMUM DEFLECTION:
//			if(rightAileron.getMinimumDeflection() != null) {
//
//				theController.getTextFieldWingMinimumDeflectionAngleAileronRight().setText(
//						String.valueOf(rightAileron.getMinimumDeflection().getEstimatedValue())
//						);
//
//				if(rightAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("�")
//						|| rightAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
//					theController.getWingMinimumDeflectionAngleAileronRigthUnitChoiceBox().getSelectionModel().select(0);
//				else if(rightAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
//					theController.getWingMinimumDeflectionAngleAileronRigthUnitChoiceBox().getSelectionModel().select(1);
//
//			}
//			else
//				theController.getTextFieldWingMinimumDeflectionAngleAileronRight().setText(
//						"NOT INITIALIZED"
//						);
//
//			//---------------------------------------------------------------------------------
//			// MAXIMUM DEFLECTION:
//			if(rightAileron.getMaximumDeflection() != null) {
//
//				theController.getTextFieldWingMaximumDeflectionAngleAileronRight().setText(
//						String.valueOf(rightAileron.getMaximumDeflection().getEstimatedValue())
//						);
//
//				if(rightAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("�")
//						|| rightAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
//					theController.getWingMaximumDeflectionAngleAileronRightUnitChoiceBox().getSelectionModel().select(0);
//				else if(rightAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
//					theController.getWingMaximumDeflectionAngleAileronRightUnitChoiceBox().getSelectionModel().select(1);
//
//			}
//			else
//				theController.getTextFieldWingMaximumDeflectionAngleAileronRight().setText(
//						"NOT INITIALIZED"
//						);
			
			//---------------------------------------------------------------------------------
			// AILERONS NUMBER CHECK:
			if (Main.getTheAircraft().getWing().getAsymmetricFlaps().size() >= 
					theController.getTabPaneWingAilerons().getTabs().size()) {
				
				int iStart = theController.getTabPaneWingAilerons().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getWing().getAsymmetricFlaps().size(); i++)
					theController.addAileron();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER AILERONS:
			for (int i=0; i<Main.getTheAircraft().getWing().getAsymmetricFlaps().size(); i++) {
				
				AsymmetricFlapCreator currentAileron = Main.getTheAircraft().getWing().getAsymmetricFlaps().get(i);
				
				//---------------------------------------------------------------------------------
				// TYPE:
				if(currentAileron.getType() != null) {					
					if(currentAileron.getType().equals(FlapTypeEnum.PLAIN))
						theController.getChoiceBoxWingAileronTypeList().get(i).getSelectionModel().select(0);					
				}
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(Double.valueOf(currentAileron.getInnerStationSpanwisePosition()) != null) {
					theController.getTextFieldWingInnerPositionAileronList().get(i).setText(
							String.valueOf(currentAileron.getInnerStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldWingInnerPositionAileronList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(Double.valueOf(currentAileron.getOuterStationSpanwisePosition()) != null) {
					theController.getTextFieldWingOuterPositionAileronList().get(i).setText(
							String.valueOf(currentAileron.getOuterStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldWingOuterPositionAileronList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(Double.valueOf(currentAileron.getInnerChordRatio()) != null) {
					theController.getTextFieldWingInnerChordRatioAileronList().get(i).setText(
							String.valueOf(currentAileron.getInnerChordRatio())
							);
				}
				else
					theController.getTextFieldWingInnerChordRatioAileronList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(Double.valueOf(currentAileron.getOuterChordRatio())!= null) {
					theController.getTextFieldWingOuterChordRatioAileronList().get(i).setText(
							String.valueOf(currentAileron.getOuterChordRatio())
							);
				}
				else
					theController.getTextFieldWingOuterChordRatioAileronList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentAileron.getMinimumDeflection() != null) {
					
					theController.getTextFieldWingMinimumDeflectionAngleAileronList().get(i).setText(
							String.valueOf(currentAileron.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("�")
							|| currentAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingMinimumDeflectionAngleAileronUnitList().get(i).getSelectionModel().select(0);
					else if(currentAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingMinimumDeflectionAngleAileronUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingMinimumDeflectionAngleAileronList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentAileron.getMaximumDeflection() != null) {
					
					theController.getTextFieldWingMaximumDeflectionAngleAileronList().get(i).setText(
							String.valueOf(currentAileron.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("�")
							|| currentAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingMaximumDeflectionAngleAileronUnitList().get(i).getSelectionModel().select(0);
					else if(currentAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingMaximumDeflectionAngleAileronUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingMaximumDeflectionAngleAileronList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// SPOILERS NUMBER CHECK:
			if (Main.getTheAircraft().getWing().getSpoilers().size() >= 
					theController.getTabPaneWingSpoilers().getTabs().size()) {
				
				int iStart = theController.getTabPaneWingSpoilers().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getWing().getSpoilers().size(); i++)
					theController.addSpoiler();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER SPOILERS:
			for (int i=0; i<Main.getTheAircraft().getWing().getSpoilers().size(); i++) {
				
				SpoilerCreator currentSpoiler = Main.getTheAircraft().getWing().getSpoilers().get(i);
				
				//---------------------------------------------------------------------------------
				// INNER SPANWISE POSITION:
				if(Double.valueOf(currentSpoiler.getInnerStationSpanwisePosition()) != null) {
					theController.getTextFieldWingInnerSpanwisePositionSpoilerList().get(i).setText(
							String.valueOf(currentSpoiler.getInnerStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldWingInnerSpanwisePositionSpoilerList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER SPANWISE POSITION:
				if(Double.valueOf(currentSpoiler.getOuterStationSpanwisePosition()) != null) {
					theController.getTextFieldWingOuterSpanwisePositionSpoilerList().get(i).setText(
							String.valueOf(currentSpoiler.getOuterStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldWingOuterSpanwisePositionSpoilerList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORDWISE POSITION:
				if(Double.valueOf(currentSpoiler.getInnerStationChordwisePosition()) != null) {
					theController.getTextFieldWingInnerChordwisePositionSpoilerList().get(i).setText(
							String.valueOf(currentSpoiler.getInnerStationChordwisePosition())
							);
				}
				else
					theController.getTextFieldWingInnerChordwisePositionSpoilerList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORDWISE POSITION:
				if(Double.valueOf(currentSpoiler.getOuterStationChordwisePosition()) != null) {
					theController.getTextFieldWingOuterChordwisePositionSpoilerList().get(i).setText(
							String.valueOf(currentSpoiler.getOuterStationChordwisePosition())
							);
				}
				else
					theController.getTextFieldWingOuterChordwisePositionSpoilerList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentSpoiler.getMinimumDeflection() != null) {
					
					theController.getTextFieldWingMinimumDeflectionAngleSpoilerList().get(i).setText(
							String.valueOf(currentSpoiler.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("�")
							|| currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingMinimumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(0);
					else if(currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingMinimumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingMinimumDeflectionAngleSpoilerList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentSpoiler.getMaximumDeflection() != null) {
					
					theController.getTextFieldWingMaximumDeflectionAngleSpoilerList().get(i).setText(
							String.valueOf(currentSpoiler.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("�")
							|| currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingMaximumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(0);
					else if(currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingMaximumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingMaximumDeflectionAngleSpoilerList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
		}
	}
	

	
	public void logHTailFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaHTailConsoleOutput().setText(
				Main.getTheAircraft().getHTail().toString()
				);

		if(Main.getTheAircraft().getHTail() != null) {

			//---------------------------------------------------------------------------------
			// ADJUST CRITERION CHOICE BOX:
			if(Main.getTheAircraft() != null)
				theController.gethTailAdjustCriterionChoiceBox().setDisable(false);
			
			//---------------------------------------------------------------------------------
			// MAIN SPAR LOCATION:
			if(Double.valueOf(Main.getTheAircraft().getHTail().getMainSparDimensionlessPosition()) != null) 
				theController.getTextFieldHTailMainSparAdimensionalPosition().setText(
						Double.toString(
								Main.getTheAircraft()
								.getHTail()
								.getMainSparDimensionlessPosition()
								)
						);
			else
				theController.getTextFieldHTailMainSparAdimensionalPosition().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// SECONDARY SPAR LOCATION:
			if(Double.valueOf(Main.getTheAircraft().getHTail().getSecondarySparDimensionlessPosition()) != null) 
				theController.getTextFieldHTailSecondarySparAdimensionalPosition().setText(
						Double.toString(
								Main.getTheAircraft()
								.getHTail()
								.getSecondarySparDimensionlessPosition()
								)
						);
			else
				theController.getTextFieldHTailSecondarySparAdimensionalPosition().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// ROUGHNESS:
			if(Main.getTheAircraft().getHTail().getRoughness() != null) {
				
				theController.getTextFieldHTailRoughness().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getHTail()
								
								.getRoughness()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getHTail()
						.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
					theController.gethTailRoughnessUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getHTail()
						.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
					theController.gethTailRoughnessUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldHTailRoughness().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// PANELS NUMBER CHECK:
			if (Main.getTheAircraft().getHTail().getPanels().size() >= 
					theController.getTabPaneHTailPanels().getTabs().size()) {
				
				int iStart = theController.getTabPaneHTailPanels().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getHTail().getPanels().size(); i++)
					theController.addHTailPanel();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER PANELS:
			for (int i=0; i<Main.getTheAircraft().getHTail().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getHTail().getPanels().get(i);
				
				//---------------------------------------------------------------------------------
				// LINKED TO (from the second panel on):
				if(i>0)
					if(currentPanel.isLinked())
						theController.getCheckBoxHTailLinkedToPreviousPanelList().get(i-1).setSelected(true);
				
				//---------------------------------------------------------------------------------
				// PANEL SPAN:
				if(currentPanel.getSpan() != null) {
					
					theController.getTextFieldHTailSpanPanelList().get(i).setText(
							String.valueOf(currentPanel.getSpan().getEstimatedValue())
							);
					
					if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxHTailSpanPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxHTailSpanPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldHTailSpanPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// SWEEP LE:
				if(currentPanel.getSweepLeadingEdge() != null) {
					
					theController.getTextFieldHTailSweepLEPanelList().get(i).setText(
							String.valueOf(currentPanel.getSweepLeadingEdge().getEstimatedValue())
							);
					
					if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxHTailSweepLEPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxHTailSweepLEPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldHTailSweepLEPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// DIHEDRAL:
				if(currentPanel.getDihedral() != null) {
					
					theController.getTextFieldHTailDihedralPanelList().get(i).setText(
							String.valueOf(currentPanel.getDihedral().getEstimatedValue())
							);
					
					if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxHTailDihedralPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxHTailDihedralPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldHTailDihedralPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD (ROOT):
				if(currentPanel.getChordRoot() != null) {
					
					theController.getTextFieldHTailInnerChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordRoot().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxHTailInnerChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxHTailInnerChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldHTailInnerChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER TWIST (ROOT):
				if(currentPanel.getTwistGeometricRoot() != null) {
					
					theController.getTextFieldHTailInnerTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricRoot().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxHTailInnerTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxHTailInnerTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldHTailInnerTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER AIRFOIL PATH (ROOT):
				if(currentPanel.getAirfoilRoot().getName() != null) {

					theController.getTextFieldHTailInnerAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getHTail().getPanels().get(i).getAirfoilRootPath()
							);
				}
				else
					theController.getTextFieldHTailInnerAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD (TIP):
				if(currentPanel.getChordTip() != null) {
					
					theController.getTextFieldHTailOuterChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordTip().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxHTailOuterChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxHTailOuterChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldHTailOuterChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER TWIST (TIP):
				if(currentPanel.getTwistGeometricAtTip() != null) {
					
					theController.getTextFieldHTailOuterTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricAtTip().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxHTailOuterTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxHTailOuterTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldHTailOuterTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER AIRFOIL PATH (TIP):
				if(currentPanel.getAirfoilTip().getName() != null) {

					theController.getTextFieldHTailOuterAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getHTail().getPanels().get(i).getAirfoilTipPath()
							);
				}
				else
					theController.getTextFieldHTailOuterAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// ELEVATORS NUMBER CHECK:
			if (Main.getTheAircraft().getHTail().getSymmetricFlaps().size() >= 
					theController.getTabPaneHTailElevators().getTabs().size()) {
				
				int iStart = theController.getTabPaneHTailElevators().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getHTail().getSymmetricFlaps().size(); i++)
					theController.addElevator();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER ELEVATORS:
			for (int i=0; i<Main.getTheAircraft().getHTail().getSymmetricFlaps().size(); i++) {
				
				SymmetricFlapCreator currentElevator = Main.getTheAircraft().getHTail().getSymmetricFlaps().get(i);
				
				//---------------------------------------------------------------------------------
				// TYPE:
				if(currentElevator.getType() != null) {
					if(currentElevator.getType().equals(FlapTypeEnum.PLAIN))
						theController.getChoiceBoxHTailElevatorTypeList().get(i).getSelectionModel().select(0);
				}
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(Double.valueOf(currentElevator.getInnerStationSpanwisePosition()) != null) {
					theController.getTextFieldHTailInnerPositionElevatorList().get(i).setText(
							String.valueOf(currentElevator.getInnerStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldHTailInnerPositionElevatorList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(Double.valueOf(currentElevator.getOuterStationSpanwisePosition()) != null) {
					theController.getTextFieldHTailOuterPositionElevatorList().get(i).setText(
							String.valueOf(currentElevator.getOuterStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldHTailOuterPositionElevatorList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(Double.valueOf(currentElevator.getInnerChordRatio()) != null) {
					theController.getTextFieldHTailInnerChordRatioElevatorList().get(i).setText(
							String.valueOf(currentElevator.getInnerChordRatio())
							);
				}
				else
					theController.getTextFieldHTailInnerChordRatioElevatorList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(Double.valueOf(currentElevator.getOuterChordRatio()) != null) {
					theController.getTextFieldHTailOuterChordRatioElevatorList().get(i).setText(
							String.valueOf(currentElevator.getOuterChordRatio())
							);
				}
				else
					theController.getTextFieldHTailOuterChordRatioElevatorList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentElevator.getMinimumDeflection() != null) {
					
					theController.getTextFieldHTailMinimumDeflectionAngleElevatorList().get(i).setText(
							String.valueOf(currentElevator.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentElevator.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("�")
							|| currentElevator.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList().get(i).getSelectionModel().select(0);
					else if(currentElevator.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldHTailMinimumDeflectionAngleElevatorList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentElevator.getMaximumDeflection() != null) {
					
					theController.getTextFieldHTailMaximumDeflectionAngleElevatorList().get(i).setText(
							String.valueOf(currentElevator.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentElevator.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("�")
							|| currentElevator.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxHTailMaximumDeflectionAngleElevatorUnitList().get(i).getSelectionModel().select(0);
					else if(currentElevator.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxHTailMaximumDeflectionAngleElevatorUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldHTailMaximumDeflectionAngleElevatorList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
		}
	}
	

	
	public void logVTailFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaVTailConsoleOutput().setText(
				Main.getTheAircraft().getVTail().toString()
				);

		if(Main.getTheAircraft().getVTail() != null) {

			//---------------------------------------------------------------------------------
			// ADJUST CRITERION CHOICE BOX:
			if(Main.getTheAircraft() != null)
				theController.getvTailAdjustCriterionChoiceBox().setDisable(false);
			
			//---------------------------------------------------------------------------------
			// MAIN SPAR LOCATION:
			if(Double.valueOf(Main.getTheAircraft().getVTail().getMainSparDimensionlessPosition()) != null) 
				theController.getTextFieldVTailMainSparAdimensionalPosition().setText(
						Double.toString(
								Main.getTheAircraft()
								.getVTail()
								.getMainSparDimensionlessPosition()
								)
						);
			else
				theController.getTextFieldVTailMainSparAdimensionalPosition().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// SECONDARY SPAR LOCATION:
			if(Double.valueOf(Main.getTheAircraft().getVTail().getSecondarySparDimensionlessPosition()) != null) 
				theController.getTextFieldVTailSecondarySparAdimensionalPosition().setText(
						Double.toString(
								Main.getTheAircraft()
								.getVTail()
								.getSecondarySparDimensionlessPosition()
								)
						);
			else
				theController.getTextFieldVTailSecondarySparAdimensionalPosition().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// ROUGHNESS:
			if(Main.getTheAircraft().getVTail().getRoughness() != null) {
				
				theController.getTextFieldVTailRoughness().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getVTail()
								
								.getRoughness()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getVTail()
						.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
					theController.getvTailRoughnessUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getVTail()
						.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getvTailRoughnessUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldVTailRoughness().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// PANELS NUMBER CHECK:
			if (Main.getTheAircraft().getVTail().getPanels().size() >= 
					theController.getTabPaneVTailPanels().getTabs().size()) {
				
				int iStart = theController.getTabPaneVTailPanels().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getVTail().getPanels().size(); i++)
					theController.addVTailPanel();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER PANELS:
			for (int i=0; i<Main.getTheAircraft().getVTail().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getVTail().getPanels().get(i);
				
				//---------------------------------------------------------------------------------
				// LINKED TO (from the second panel on):
				if(i>0)
					if(currentPanel.isLinked())
						theController.getCheckBoxVTailLinkedToPreviousPanelList().get(i-1).setSelected(true);
				
				//---------------------------------------------------------------------------------
				// PANEL SPAN:
				if(currentPanel.getSpan() != null) {
					
					theController.getTextFieldVTailSpanPanelList().get(i).setText(
							String.valueOf(currentPanel.getSpan().getEstimatedValue())
							);
					
					if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxVTailSpanPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxVTailSpanPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldVTailSpanPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// SWEEP LE:
				if(currentPanel.getSweepLeadingEdge() != null) {
					
					theController.getTextFieldVTailSweepLEPanelList().get(i).setText(
							String.valueOf(currentPanel.getSweepLeadingEdge().getEstimatedValue())
							);
					
					if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxVTailSweepLEPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxVTailSweepLEPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldVTailSweepLEPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// DIHEDRAL:
				if(currentPanel.getDihedral() != null) {
					
					theController.getTextFieldVTailDihedralPanelList().get(i).setText(
							String.valueOf(currentPanel.getDihedral().getEstimatedValue())
							);
					
					if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxVTailDihedralPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxVTailDihedralPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldVTailDihedralPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD (ROOT):
				if(currentPanel.getChordRoot() != null) {
					
					theController.getTextFieldVTailInnerChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordRoot().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxVTailInnerChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxVTailInnerChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldVTailInnerChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER TWIST (ROOT):
				if(currentPanel.getTwistGeometricRoot() != null) {
					
					theController.getTextFieldVTailInnerTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricRoot().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxVTailInnerTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxVTailInnerTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldVTailInnerTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER AIRFOIL PATH (ROOT):
				if(currentPanel.getAirfoilRoot().getName() != null) {

					theController.getTextFieldVTailInnerAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getVTail().getPanels().get(i).getAirfoilRootPath()
							);
				}
				else
					theController.getTextFieldVTailInnerAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD (TIP):
				if(currentPanel.getChordTip() != null) {
					
					theController.getTextFieldVTailOuterChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordTip().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxVTailOuterChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxVTailOuterChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldVTailOuterChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER TWIST (TIP):
				if(currentPanel.getTwistGeometricAtTip() != null) {
					
					theController.getTextFieldVTailOuterTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricAtTip().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxVTailOuterTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxVTailOuterTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldVTailOuterTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER AIRFOIL PATH (TIP):
				if(currentPanel.getAirfoilTip().getName() != null) {

					theController.getTextFieldVTailOuterAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getVTail().getPanels().get(i).getAirfoilTipPath()
							);
				}
				else
					theController.getTextFieldVTailOuterAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// RudderS NUMBER CHECK:
			if (Main.getTheAircraft().getVTail().getSymmetricFlaps().size() >= 
					theController.getTabPaneVTailRudders().getTabs().size()) {
				
				int iStart = theController.getTabPaneVTailRudders().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getVTail().getSymmetricFlaps().size(); i++)
					theController.addRudder();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER RudderS:
			for (int i=0; i<Main.getTheAircraft().getVTail().getSymmetricFlaps().size(); i++) {
				
				SymmetricFlapCreator currentRudder = Main.getTheAircraft().getVTail().getSymmetricFlaps().get(i);
				
				//---------------------------------------------------------------------------------
				// TYPE:
				if(currentRudder.getType() != null) {
					if(currentRudder.getType().equals(FlapTypeEnum.PLAIN))
						theController.getChoiceBoxVTailRudderTypeList().get(i).getSelectionModel().select(0);
				}
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(Double.valueOf(currentRudder.getInnerStationSpanwisePosition()) != null) {
					theController.getTextFieldVTailInnerPositionRudderList().get(i).setText(
							String.valueOf(currentRudder.getInnerStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldVTailInnerPositionRudderList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(Double.valueOf(currentRudder.getOuterStationSpanwisePosition()) != null) {
					theController.getTextFieldVTailOuterPositionRudderList().get(i).setText(
							String.valueOf(currentRudder.getOuterStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldVTailOuterPositionRudderList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(Double.valueOf(currentRudder.getInnerChordRatio()) != null) {
					theController.getTextFieldVTailInnerChordRatioRudderList().get(i).setText(
							String.valueOf(currentRudder.getInnerChordRatio())
							);
				}
				else
					theController.getTextFieldVTailInnerChordRatioRudderList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(Double.valueOf(currentRudder.getOuterChordRatio()) != null) {
					theController.getTextFieldVTailOuterChordRatioRudderList().get(i).setText(
							String.valueOf(currentRudder.getOuterChordRatio())
							);
				}
				else
					theController.getTextFieldVTailOuterChordRatioRudderList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentRudder.getMinimumDeflection() != null) {
					
					theController.getTextFieldVTailMinimumDeflectionAngleRudderList().get(i).setText(
							String.valueOf(currentRudder.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentRudder.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("�")
							|| currentRudder.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxVTailMinimumDeflectionAngleRudderUnitList().get(i).getSelectionModel().select(0);
					else if(currentRudder.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxVTailMinimumDeflectionAngleRudderUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldVTailMinimumDeflectionAngleRudderList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentRudder.getMaximumDeflection() != null) {
					
					theController.getTextFieldVTailMaximumDeflectionAngleRudderList().get(i).setText(
							String.valueOf(currentRudder.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentRudder.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("�")
							|| currentRudder.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxVTailMaximumDeflectionAngleRudderUnitList().get(i).getSelectionModel().select(0);
					else if(currentRudder.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxVTailMaximumDeflectionAngleRudderUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldVTailMaximumDeflectionAngleRudderList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
		}
	}
	

	
	public void logCanardFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaCanardConsoleOutput().setText(
				Main.getTheAircraft().getCanard().toString()
				);

		if(Main.getTheAircraft().getCanard() != null) {

			//---------------------------------------------------------------------------------
			// ADJUST CRITERION CHOICE BOX:
			if(Main.getTheAircraft() != null)
				theController.getCanardAdjustCriterionChoiceBox().setDisable(false);
			
			//---------------------------------------------------------------------------------
			// MAIN SPAR LOCATION:
			if(Double.valueOf(Main.getTheAircraft().getCanard().getMainSparDimensionlessPosition()) != null) 
				theController.getTextFieldCanardMainSparAdimensionalPosition().setText(
						Double.toString(
								Main.getTheAircraft()
								.getCanard()
								.getMainSparDimensionlessPosition()
								)
						);
			else
				theController.getTextFieldCanardMainSparAdimensionalPosition().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// SECONDARY SPAR LOCATION:
			if(Double.valueOf(Main.getTheAircraft().getCanard().getSecondarySparDimensionlessPosition()) != null) 
				theController.getTextFieldCanardSecondarySparAdimensionalPosition().setText(
						Double.toString(
								Main.getTheAircraft()
								.getCanard()
								.getSecondarySparDimensionlessPosition()
								)
						);
			else
				theController.getTextFieldCanardSecondarySparAdimensionalPosition().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// ROUGHNESS:
			if(Main.getTheAircraft().getCanard().getRoughness() != null) {
				
				theController.getTextFieldCanardRoughness().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCanard()
								
								.getRoughness()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCanard()
						.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCanardRoughnessUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCanard()
						.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCanardRoughnessUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldCanardRoughness().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// PANELS NUMBER CHECK:
			if (Main.getTheAircraft().getCanard().getPanels().size() >= 
					theController.getTabPaneCanardPanels().getTabs().size()) {
				
				int iStart = theController.getTabPaneCanardPanels().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getCanard().getPanels().size(); i++)
					theController.addCanardPanel();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER PANELS:
			for (int i=0; i<Main.getTheAircraft().getCanard().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getCanard().getPanels().get(i);
				
				//---------------------------------------------------------------------------------
				// LINKED TO (from the second panel on):
				if(i>0)
					if(currentPanel.isLinked())
						theController.getCheckBoxCanardLinkedToPreviousPanelList().get(i-1).setSelected(true);
				
				//---------------------------------------------------------------------------------
				// PANEL SPAN:
				if(currentPanel.getSpan() != null) {
					
					theController.getTextFieldCanardSpanPanelList().get(i).setText(
							String.valueOf(currentPanel.getSpan().getEstimatedValue())
							);
					
					if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxCanardSpanPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxCanardSpanPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldCanardSpanPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// SWEEP LE:
				if(currentPanel.getSweepLeadingEdge() != null) {
					
					theController.getTextFieldCanardSweepLEPanelList().get(i).setText(
							String.valueOf(currentPanel.getSweepLeadingEdge().getEstimatedValue())
							);
					
					if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxCanardSweepLEPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxCanardSweepLEPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldCanardSweepLEPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// DIHEDRAL:
				if(currentPanel.getDihedral() != null) {
					
					theController.getTextFieldCanardDihedralPanelList().get(i).setText(
							String.valueOf(currentPanel.getDihedral().getEstimatedValue())
							);
					
					if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxCanardDihedralPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxCanardDihedralPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldCanardDihedralPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD (ROOT):
				if(currentPanel.getChordRoot() != null) {
					
					theController.getTextFieldCanardInnerChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordRoot().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxCanardInnerChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxCanardInnerChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldCanardInnerChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER TWIST (ROOT):
				if(currentPanel.getTwistGeometricRoot() != null) {
					
					theController.getTextFieldCanardInnerTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricRoot().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxCanardInnerTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxCanardInnerTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldCanardInnerTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER AIRFOIL PATH (ROOT):
				if(currentPanel.getAirfoilRoot().getName() != null) {

					theController.getTextFieldCanardInnerAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getCanard().getPanels().get(i).getAirfoilRootPath()
							);
				}
				else
					theController.getTextFieldCanardInnerAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD (TIP):
				if(currentPanel.getChordTip() != null) {
					
					theController.getTextFieldCanardOuterChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordTip().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxCanardOuterChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxCanardOuterChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldCanardOuterChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER TWIST (TIP):
				if(currentPanel.getTwistGeometricAtTip() != null) {
					
					theController.getTextFieldCanardOuterTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricAtTip().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("�")
							|| currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxCanardOuterTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxCanardOuterTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldCanardOuterTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER AIRFOIL PATH (TIP):
				if(currentPanel.getAirfoilTip().getName() != null) {

					theController.getTextFieldCanardOuterAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getCanard().getPanels().get(i).getAirfoilTipPath()
							);
				}
				else
					theController.getTextFieldCanardOuterAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// ControlSurfaceS NUMBER CHECK:
			if (Main.getTheAircraft().getCanard().getSymmetricFlaps().size() >= 
					theController.getTabPaneCanardControlSurfaces().getTabs().size()) {
				
				int iStart = theController.getTabPaneCanardControlSurfaces().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getCanard().getSymmetricFlaps().size(); i++)
					theController.addControlSurface();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER ControlSurfaces:
			for (int i=0; i<Main.getTheAircraft().getCanard().getSymmetricFlaps().size(); i++) {
				
				SymmetricFlapCreator currentControlSurface = Main.getTheAircraft().getCanard().getSymmetricFlaps().get(i);
				
				//---------------------------------------------------------------------------------
				// TYPE:
				if(currentControlSurface.getType() != null) {
					if(currentControlSurface.getType().equals(FlapTypeEnum.PLAIN))
						theController.getChoiceBoxCanardControlSurfaceTypeList().get(i).getSelectionModel().select(0);
				}
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(Double.valueOf(currentControlSurface.getInnerStationSpanwisePosition()) != null) {
					theController.getTextFieldCanardInnerPositionControlSurfaceList().get(i).setText(
							String.valueOf(currentControlSurface.getInnerStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldCanardInnerPositionControlSurfaceList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(Double.valueOf(currentControlSurface.getOuterStationSpanwisePosition()) != null) {
					theController.getTextFieldCanardOuterPositionControlSurfaceList().get(i).setText(
							String.valueOf(currentControlSurface.getOuterStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldCanardOuterPositionControlSurfaceList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(Double.valueOf(currentControlSurface.getInnerChordRatio()) != null) {
					theController.getTextFieldCanardInnerChordRatioControlSurfaceList().get(i).setText(
							String.valueOf(currentControlSurface.getInnerChordRatio())
							);
				}
				else
					theController.getTextFieldCanardInnerChordRatioControlSurfaceList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(Double.valueOf(currentControlSurface.getOuterChordRatio()) != null) {
					theController.getTextFieldCanardOuterChordRatioControlSurfaceList().get(i).setText(
							String.valueOf(currentControlSurface.getOuterChordRatio())
							);
				}
				else
					theController.getTextFieldCanardOuterChordRatioControlSurfaceList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentControlSurface.getMinimumDeflection() != null) {
					
					theController.getTextFieldCanardMinimumDeflectionAngleControlSurfaceList().get(i).setText(
							String.valueOf(currentControlSurface.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentControlSurface.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("�")
							|| currentControlSurface.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList().get(i).getSelectionModel().select(0);
					else if(currentControlSurface.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldCanardMinimumDeflectionAngleControlSurfaceList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentControlSurface.getMaximumDeflection() != null) {
					
					theController.getTextFieldCanardMaximumDeflectionAngleControlSurfaceList().get(i).setText(
							String.valueOf(currentControlSurface.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentControlSurface.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("�")
							|| currentControlSurface.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList().get(i).getSelectionModel().select(0);
					else if(currentControlSurface.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList().get(i).getSelectionModel().select(1);

				}
				else
					theController.getTextFieldCanardMaximumDeflectionAngleControlSurfaceList().get(i).setText(
							"NOT INITIALIZED"
							);

			}
		}
	}


	
	public void logNacelleFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaNacelleConsoleOutput().setText(
				Main.getTheAircraft().getNacelles().toString()
				);

		if(Main.getTheAircraft().getNacelles() != null) {

			//---------------------------------------------------------------------------------
			// NACELLES NUMBER CHECK:
			if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
					theController.getTabPaneNacelles().getTabs().size()) {

				int iStart = theController.getTabPaneNacelles().getTabs().size();

				for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++)
					theController.getInputManagerControllerUtilities().addNacelleImplementation();

			}

			//---------------------------------------------------------------------------------
			// LOOP OVER NACELLES:
			for (int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

				NacelleCreator currentNacelle = Main.getTheAircraft().getNacelles().getNacellesList().get(i);

				//---------------------------------------------------------------------------------
				// ROUGHNESS:
				if(currentNacelle.getRoughness() != null) {
					theController.getTextFieldNacelleRoughnessList().get(i).setText(
							String.valueOf(currentNacelle.getRoughness().getEstimatedValue()));

					if(currentNacelle.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxNacelleRoughnessUnitList().get(i).getSelectionModel().select(0);
					else if(currentNacelle.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxNacelleRoughnessUnitList().get(i).getSelectionModel().select(1);

				}
				else
					theController.getTextFieldNacelleRoughnessList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// LENGTH:
				if(currentNacelle.getLength() != null) {
					theController.getTextFieldNacelleLengthList().get(i).setText(
							String.valueOf(currentNacelle.getLength().getEstimatedValue()));

					if(currentNacelle.getLength().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxNacelleLengthUnitList().get(i).getSelectionModel().select(0);
					else if(currentNacelle.getLength().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxNacelleLengthUnitList().get(i).getSelectionModel().select(1);

				}
				else
					theController.getTextFieldNacelleLengthList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DIAMETER:
				if(currentNacelle.getDiameterMax() != null) {
					theController.getTextFieldNacelleMaximumDiameterList().get(i).setText(
							String.valueOf(currentNacelle.getDiameterMax().getEstimatedValue()));

					if(currentNacelle.getDiameterMax().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxNacelleMaximumDiameterUnitList().get(i).getSelectionModel().select(0);
					else if(currentNacelle.getDiameterMax().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxNacelleMaximumDiameterUnitList().get(i).getSelectionModel().select(1);

				}
				else
					theController.getTextFieldNacelleLengthList().get(i).setText(
							"NOT INITIALIZED"
							);

				//---------------------------------------------------------------------------------
				// K INLET:
				if(Double.valueOf(currentNacelle.getKInlet()) != null) {
					theController.getTextFieldNacelleKInletList().get(i).setText(
							String.valueOf(currentNacelle.getKInlet()));
				}
				else
					theController.getTextFieldNacelleKInletList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// K OULET:
				if(Double.valueOf(currentNacelle.getKOutlet()) != null) {
					theController.getTextFieldNacelleKOutletList().get(i).setText(
							String.valueOf(currentNacelle.getKOutlet()));
				}
				else
					theController.getTextFieldNacelleKOutletList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// K LENGTH:
				if(Double.valueOf(currentNacelle.getKLength()) != null) {
					theController.getTextFieldNacelleKLengthList().get(i).setText(
							String.valueOf(currentNacelle.getKLength()));
				}
				else
					theController.getTextFieldNacelleKLengthList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// K DIAMETER OUTLET:
				if(Double.valueOf(currentNacelle.getKDiameterOutlet()) != null) {
					theController.getTextFieldNacelleKDiameterOutletList().get(i).setText(
							String.valueOf(currentNacelle.getKDiameterOutlet()));
				}
				else
					theController.getTextFieldNacelleKDiameterOutletList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
		}
	}
	
	@SuppressWarnings("unused")
	public void logPowerPlantFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaPowerPlantConsoleOutput().setText(
				Main.getTheAircraft().getPowerPlant().toString()
				);

		if(Main.getTheAircraft().getPowerPlant() != null) {

			//---------------------------------------------------------------------------------
			// ENGINES NUMBER CHECK:
			if (Main.getTheAircraft().getPowerPlant().getEngineList().size() >= 
					theController.getTabPaneEngines().getTabs().size()) {

				int iStart = theController.getTabPaneEngines().getTabs().size();

				for(int i=iStart; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++)
					theController.getInputManagerControllerUtilities().addEngineImplementation();

			}

			//---------------------------------------------------------------------------------
			// LOOP OVER ENGINES:
			for (int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				Engine currentEngine = Main.getTheAircraft().getPowerPlant().getEngineList().get(i);
				
				switch (currentEngine.getEngineType()) {
				case TURBOFAN:
					
					theController.getPowerPlantJetRadioButtonList().get(i).setSelected(true);
					theController.getInputManagerControllerUtilities().showTurbojetTurboFanDataRadioButtonImplementation(i);
					
					//---------------------------------------------------------------------------------
					// ENGINE TYPE:
					if(currentEngine.getEngineType() != null) 
						theController.getEngineTurbojetTurbofanTypeChoiceBoxMap().get(i).getSelectionModel().select(0);
					
					//---------------------------------------------------------------------------------
					// ENGINE DATABASE:
					if(currentEngine.getEngineDatabaseName() != null) {
						theController.getEngineTurbojetTurbofanDatabaseTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getEngineDatabaseName())
								);
					}
					else
						theController.getEngineTurbojetTurbofanDatabaseTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE LENGTH:
					if(currentEngine.getLength() != null) {
						theController.getEngineTurbojetTurbofanLengthTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getLength().getEstimatedValue())
								);
						
						if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("m"))
							theController.getEngineTurbojetTurbofanLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("ft"))
							theController.getEngineTurbojetTurbofanLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbojetTurbofanLengthTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE DRY MASS:
					if(currentEngine.getDryMassPublicDomain() != null) {
						theController.getEngineTurbojetTurbofanDryMassTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getDryMassPublicDomain().getEstimatedValue())
								);
						
						if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
							theController.getEngineTurbojetTurbofanDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
							theController.getEngineTurbojetTurbofanDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbojetTurbofanDryMassTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE STATIC THRUST:
					if(currentEngine.getT0() != null) {
						theController.getEngineTurbojetTurbofanStaticThrustTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getT0().getEstimatedValue())
								);
						
						if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("N"))
							theController.getEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("lbf"))
							theController.getEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbojetTurbofanStaticThrustTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE BPR:
					if(Double.valueOf(currentEngine.getBPR()) != null) 
						theController.getEngineTurbojetTurbofanBPRTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getBPR())
								);
					else
						theController.getEngineTurbojetTurbofanBPRTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF COMPRESSOR STAGES:
					if((Integer) currentEngine.getNumberOfCompressorStages() != null) 
						theController.getEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfCompressorStages())
								);
					else
						theController.getEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF SHAFTS:
					if((Integer) currentEngine.getNumberOfShafts() != null) 
						theController.getEngineTurbojetTurbofanNumberOfShaftsTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfShafts())
								);
					else
						theController.getEngineTurbojetTurbofanNumberOfShaftsTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE OVERALL PRESSURE RATIO:
					if((Double) currentEngine.getOverallPressureRatio() != null) 
						theController.getEngineTurbojetTurbofanOverallPressureRatioTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getOverallPressureRatio())
								);
					else
						theController.getEngineTurbojetTurbofanOverallPressureRatioTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					break;
				case TURBOJET:
					
					theController.getPowerPlantJetRadioButtonList().get(i).setSelected(true);
					theController.getInputManagerControllerUtilities().showTurbojetTurboFanDataRadioButtonImplementation(i);
					
					//---------------------------------------------------------------------------------
					// ENGINE TYPE:
					if(currentEngine.getEngineType() != null) 
						theController.getEngineTurbojetTurbofanTypeChoiceBoxMap().get(i).getSelectionModel().select(1);
					
					//---------------------------------------------------------------------------------
					// ENGINE DATABASE:
					if(currentEngine.getEngineDatabaseName() != null) {
						theController.getEngineTurbojetTurbofanDatabaseTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getEngineDatabaseName())
								);
					}
					else
						theController.getEngineTurbojetTurbofanDatabaseTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE LENGTH:
					if(currentEngine.getLength() != null) {
						theController.getEngineTurbojetTurbofanLengthTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getLength().getEstimatedValue())
								);
						
						if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("m"))
							theController.getEngineTurbojetTurbofanLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("ft"))
							theController.getEngineTurbojetTurbofanLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbojetTurbofanLengthTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE DRY MASS:
					if(currentEngine.getDryMassPublicDomain() != null) {
						theController.getEngineTurbojetTurbofanDryMassTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getDryMassPublicDomain().getEstimatedValue())
								);
						
						if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
							theController.getEngineTurbojetTurbofanDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
							theController.getEngineTurbojetTurbofanDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbojetTurbofanDryMassTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE STATIC THRUST:
					if(currentEngine.getT0() != null) {
						theController.getEngineTurbojetTurbofanStaticThrustTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getT0().getEstimatedValue())
								);
						
						if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("N"))
							theController.getEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("lbf"))
							theController.getEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbojetTurbofanStaticThrustTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE BPR:
					if(Double.valueOf(currentEngine.getBPR()) != null) 
						theController.getEngineTurbojetTurbofanBPRTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getBPR())
								);
					else
						theController.getEngineTurbojetTurbofanBPRTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF COMPRESSOR STAGES:
					if((Integer) currentEngine.getNumberOfCompressorStages() != null) 
						theController.getEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfCompressorStages())
								);
					else
						theController.getEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF SHAFTS:
					if((Integer) currentEngine.getNumberOfShafts() != null) 
						theController.getEngineTurbojetTurbofanNumberOfShaftsTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfShafts())
								);
					else
						theController.getEngineTurbojetTurbofanNumberOfShaftsTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE OVERALL PRESSURE RATIO:
					if((Double) currentEngine.getOverallPressureRatio() != null) 
						theController.getEngineTurbojetTurbofanOverallPressureRatioTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getOverallPressureRatio())
								);
					else
						theController.getEngineTurbojetTurbofanOverallPressureRatioTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					break;
				case TURBOPROP:
					
					theController.getPowerPlantTurbopropRadioButtonList().get(i).setSelected(true);
					theController.getInputManagerControllerUtilities().showTurbopropDataRadioButtonImplementation(i);
					
					//---------------------------------------------------------------------------------
					// ENGINE TYPE:
					if(currentEngine.getEngineType() != null) 
						theController.getEngineTurbopropTypeChoiceBoxMap().get(i).getSelectionModel().select(0);
					
					//---------------------------------------------------------------------------------
					// ENGINE DATABASE:
					if(currentEngine.getEngineDatabaseName() != null) {
						theController.getEngineTurbopropDatabaseTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getEngineDatabaseName())
								);
					}
					else
						theController.getEngineTurbopropDatabaseTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE LENGTH:
					if(currentEngine.getLength() != null) {
						theController.getEngineTurbopropLengthTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getLength().getEstimatedValue())
								);
						
						if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("m"))
							theController.getEngineTurbopropLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("ft"))
							theController.getEngineTurbopropLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbopropLengthTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE DRY MASS:
					if(currentEngine.getDryMassPublicDomain() != null) {
						theController.getEngineTurbopropDryMassTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getDryMassPublicDomain().getEstimatedValue())
								);
						
						if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
							theController.getEngineTurbopropDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
							theController.getEngineTurbopropDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbopropDryMassTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE STATIC POWER:
					if(currentEngine.getP0() != null) {
						theController.getEngineTurbopropStaticPowerTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getP0().getEstimatedValue())
								);
						
						if(currentEngine.getP0().getUnit().toString().equalsIgnoreCase("W"))
							theController.getEngineTurbopropStaticPowerUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getP0().getUnit().toString().equalsIgnoreCase("hp"))
							theController.getEngineTurbopropStaticPowerUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbopropStaticPowerTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE PROPELLER DIAMETER:
					if(currentEngine.getPropellerDiameter() != null) {
						theController.getEngineTurbopropPropellerDiameterTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getPropellerDiameter().getEstimatedValue())
								);
						
						if(currentEngine.getPropellerDiameter().getUnit().toString().equalsIgnoreCase("m"))
							theController.getEngineTurbopropPropellerDiameterUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getPropellerDiameter().getUnit().toString().equalsIgnoreCase("ft"))
							theController.getEngineTurbopropPropellerDiameterUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbopropPropellerDiameterTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF BLADES:
					if((Integer) currentEngine.getNumberOfBlades() != null) 
						theController.getEngineTurbopropNumberOfBladesTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfBlades())
								);
					else
						theController.getEngineTurbopropNumberOfBladesTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE PROPELLER EFFICIENCY:
					if((Double) currentEngine.getEtaPropeller() != null) 
						theController.getEngineTurbopropPropellerEfficiencyTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getEtaPropeller())
								);
					else
						theController.getEngineTurbopropPropellerEfficiencyTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF COMPRESSOR STAGES:
					if((Integer) currentEngine.getNumberOfCompressorStages() != null) 
						theController.getEngineTurbopropNumberOfCompressorStagesTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfCompressorStages())
								);
					else
						theController.getEngineTurbopropNumberOfCompressorStagesTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF SHAFTS:
					if((Integer) currentEngine.getNumberOfShafts() != null) 
						theController.getEngineTurbopropNumberOfShaftsTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfShafts())
								);
					else
						theController.getEngineTurbopropNumberOfShaftsTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE OVERALL PRESSURE RATIO:
					if((Double) currentEngine.getOverallPressureRatio() != null) 
						theController.getEngineTurbopropOverallPressureRatioTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getOverallPressureRatio())
								);
					else
						theController.getEngineTurbopropOverallPressureRatioTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
										
					break;
				case PISTON:
					
					theController.getPowerPlantPistonRadioButtonList().get(i).setSelected(true);
					theController.getInputManagerControllerUtilities().showPistonDataRadioButtonImplementation(i);
					
					//---------------------------------------------------------------------------------
					// ENGINE TYPE:
					if(currentEngine.getEngineType() != null) 
						theController.getEnginePistonTypeChoiceBoxMap().get(i).getSelectionModel().select(0);
					
					//---------------------------------------------------------------------------------
					// ENGINE DATABASE:
					if(currentEngine.getEngineDatabaseName() != null) {
						theController.getEnginePistonDatabaseTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getEngineDatabaseName())
								);
					}
					else
						theController.getEnginePistonDatabaseTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE LENGTH:
					if(currentEngine.getLength() != null) {
						theController.getEnginePistonLengthTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getLength().getEstimatedValue())
								);
						
						if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("m"))
							theController.getEnginePistonLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("ft"))
							theController.getEnginePistonLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEnginePistonLengthTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE DRY MASS:
					if(currentEngine.getDryMassPublicDomain() != null) {
						theController.getEnginePistonDryMassTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getDryMassPublicDomain().getEstimatedValue())
								);
						
						if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
							theController.getEnginePistonDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
							theController.getEnginePistonDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEnginePistonDryMassTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE STATIC POWER:
					if(currentEngine.getP0() != null) {
						theController.getEnginePistonStaticPowerTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getP0().getEstimatedValue())
								);
						
						if(currentEngine.getP0().getUnit().toString().equalsIgnoreCase("W"))
							theController.getEnginePistonStaticPowerUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getP0().getUnit().toString().equalsIgnoreCase("hp"))
							theController.getEnginePistonStaticPowerUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEnginePistonStaticPowerTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE PROPELLER DIAMETER:
					if(currentEngine.getPropellerDiameter() != null) {
						theController.getEnginePistonPropellerDiameterTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getPropellerDiameter().getEstimatedValue())
								);
						
						if(currentEngine.getPropellerDiameter().getUnit().toString().equalsIgnoreCase("m"))
							theController.getEnginePistonPropellerDiameterUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getPropellerDiameter().getUnit().toString().equalsIgnoreCase("ft"))
							theController.getEnginePistonPropellerDiameterUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEnginePistonPropellerDiameterTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF BLADES:
					if((Integer) currentEngine.getNumberOfBlades() != null) 
						theController.getEnginePistonNumberOfBladesTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfBlades())
								);
					else
						theController.getEnginePistonNumberOfBladesTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE PROPELLER EFFICIENCY:
					if((Double) currentEngine.getEtaPropeller() != null) 
						theController.getEnginePistonPropellerEfficiencyTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getEtaPropeller())
								);
					else
						theController.getEnginePistonPropellerEfficiencyTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					break;
				default:
					break;
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	public void logLandingGearsFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaLandingGearsConsoleOutput().setText(
				Main.getTheAircraft().getLandingGears().toString()
				);

		//---------------------------------------------------------------------------------
		// MAIN LEG LENGTH
		if(Main.getTheAircraft().getLandingGears().getMainLegsLenght() != null) {
			
			theController.getTextFieldLandingGearsMainLegLength().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getMainLegsLenght()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getMainLegsLenght()
					.getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsMainLegLengthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getMainLegsLenght()
					.getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsMainLegLengthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldLandingGearsMainLegLength().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// DISTANCE BETWEEN WHEELS
		if(Main.getTheAircraft().getLandingGears().getDistanceBetweenWheels() != null) {
			
			theController.getTextFieldLandingGearsDistanceBetweenWheels().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getDistanceBetweenWheels()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getDistanceBetweenWheels()
					.getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsDistanceBetweenWheelsUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getDistanceBetweenWheels()
					.getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsDistanceBetweenWheelsUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldLandingGearsDistanceBetweenWheels().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NUMBER OF FRONTAL WHEELS 
		if((Integer) Main.getTheAircraft().getLandingGears().getNumberOfFrontalWheels() != null) {
			
			theController.getTextFieldLandingGearsNumberOfFrontalWheels().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getNumberOfFrontalWheels()
							)
					);
		}
		else
			theController.getTextFieldLandingGearsNumberOfFrontalWheels().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NUMBER OF REAR WHEELS 
		if((Integer) Main.getTheAircraft().getLandingGears().getNumberOfRearWheels() != null) {
			
			theController.getTextFieldLandingGearsNumberOfRearWheels().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getNumberOfRearWheels()
							)
					);
		}
		else
			theController.getTextFieldLandingGearsNumberOfRearWheels().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// FRONTAL WHEELS HEIGHT
		if(Main.getTheAircraft().getLandingGears().getFrontalWheelsHeight() != null) {
			
			theController.getTextFieldLandingGearsFrontalWheelsHeight().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getFrontalWheelsHeight()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getFrontalWheelsHeight()
					.getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsFrontalWheelsHeigthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getFrontalWheelsHeight()
					.getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsFrontalWheelsHeigthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldLandingGearsFrontalWheelsHeight().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// FRONTAL WHEELS WIDTH
		if(Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth() != null) {
			
			theController.getTextFieldLandingGearsFrontalWheelsWidth().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getFrontalWheelsWidth()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth()
					.getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsFrontalWheelsWidthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth()
					.getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsFrontalWheelsWidthUnitChoiceBox().getSelectionModel().select(1);

		}
		else
			theController.getTextFieldLandingGearsFrontalWheelsWidth().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// Rear WHEELS HEIGHT
		if(Main.getTheAircraft().getLandingGears().getRearWheelsHeight() != null) {

			theController.getTextFieldLandingGearsRearWheelsHeight().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getRearWheelsHeight()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getRearWheelsHeight()
					.getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsRearWheelsHeigthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getRearWheelsHeight()
					.getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsRearWheelsHeigthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldLandingGearsRearWheelsHeight().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// Rear WHEELS WIDTH
		if(Main.getTheAircraft().getLandingGears().getRearWheelsWidth() != null) {
			
			theController.getTextFieldLandingGearsRearWheelsWidth().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getRearWheelsWidth()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getRearWheelsWidth()
					.getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsRearWheelsWidthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getRearWheelsWidth()
					.getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsRearWheelsWidthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldLandingGearsRearWheelsWidth().setText(
					"NOT INITIALIZED"
					);
		
	}
	
	public void logCADConfigurationFromFileToInterface() {
		
		// Print the CADManager toString() method inside the text area of the GUI ...
		theController.getTextAreaCAD3DViewConsoleOutput().setText(
				Main.getTheCADManager().toString()
				);
		
		//-------------------------------------------------------------------------
		// CAD EXPORT OPTIONS
		if (theController.getExportCADWireframeCheckBox().isDisabled())
			theController.getExportCADWireframeCheckBox().setDisable(false);
		theController.getExportCADWireframeCheckBox().setSelected(
				Main.getTheCADManager().getTheCADBuilderInterface().getExportWireframe());
		
		if (theController.getFileExtensionCADChoiceBox().isDisabled())
			theController.getFileExtensionCADChoiceBox().setDisable(false);
		if (Main.getTheCADManager().getTheCADBuilderInterface().getFileExtension().equals(FileExtension.BREP))
			theController.getFileExtensionCADChoiceBox().getSelectionModel().select(0);
		if (Main.getTheCADManager().getTheCADBuilderInterface().getFileExtension().equals(FileExtension.STEP))
			theController.getFileExtensionCADChoiceBox().getSelectionModel().select(1);
		if (Main.getTheCADManager().getTheCADBuilderInterface().getFileExtension().equals(FileExtension.IGES))
			theController.getFileExtensionCADChoiceBox().getSelectionModel().select(2);
		if (Main.getTheCADManager().getTheCADBuilderInterface().getFileExtension().equals(FileExtension.STL))
			theController.getFileExtensionCADChoiceBox().getSelectionModel().select(3);
		
		//-------------------------------------------------------------------------
		// FUSELAGE OPTIONS
		if (Main.getTheAircraft().getFuselage() != null) {
			
			if (theController.getGenerateFuselageCADCheckBox().isDisabled())
				theController.getGenerateFuselageCADCheckBox().setDisable(false);
			theController.getGenerateFuselageCADCheckBox().setSelected(
					Main.getTheCADManager().getTheCADBuilderInterface().getGenerateFuselage());

			if (theController.getFuselageCADNumberNoseSectionsTextField().isDisabled())
				theController.getFuselageCADNumberNoseSectionsTextField().setDisable(false);
			theController.getFuselageCADNumberNoseSectionsTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getNumberNoseTrunkSections()));

			if (theController.getFuselageCADNoseSpacingChoiceBox().isDisabled())
				theController.getFuselageCADNoseSpacingChoiceBox().setDisable(false);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getSpacingTypeNoseTrunk().equals(XSpacingType.UNIFORM))
				theController.getFuselageCADNoseSpacingChoiceBox().getSelectionModel().select(0);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getSpacingTypeNoseTrunk().equals(XSpacingType.COSINUS))
				theController.getFuselageCADNoseSpacingChoiceBox().getSelectionModel().select(1);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getSpacingTypeNoseTrunk().equals(XSpacingType.HALFCOSINUS1))
				theController.getFuselageCADNoseSpacingChoiceBox().getSelectionModel().select(2);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getSpacingTypeNoseTrunk().equals(XSpacingType.HALFCOSINUS2))
				theController.getFuselageCADNoseSpacingChoiceBox().getSelectionModel().select(3);

			if (theController.getFuselageCADNumberTailSectionsTextField().isDisabled())
				theController.getFuselageCADNumberTailSectionsTextField().setDisable(false);
			theController.getFuselageCADNumberTailSectionsTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getNumberTailTrunkSections()));

			if (theController.getFuselageCADTailSpacingChoiceBox().isDisabled())
				theController.getFuselageCADTailSpacingChoiceBox().setDisable(false);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getSpacingTypeTailTrunk().equals(XSpacingType.UNIFORM))
				theController.getFuselageCADTailSpacingChoiceBox().getSelectionModel().select(0);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getSpacingTypeTailTrunk().equals(XSpacingType.COSINUS))
				theController.getFuselageCADTailSpacingChoiceBox().getSelectionModel().select(1);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getSpacingTypeTailTrunk().equals(XSpacingType.HALFCOSINUS1))
				theController.getFuselageCADTailSpacingChoiceBox().getSelectionModel().select(2);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getSpacingTypeTailTrunk().equals(XSpacingType.HALFCOSINUS2))
				theController.getFuselageCADTailSpacingChoiceBox().getSelectionModel().select(3);

		} else {
			
			theController.getGenerateFuselageCADCheckBox().setDisable(true);
			theController.getFuselageCADNumberNoseSectionsTextField().setDisable(true);
			theController.getFuselageCADNoseSpacingChoiceBox().setDisable(true);
			theController.getFuselageCADNumberTailSectionsTextField().setDisable(true);
			theController.getFuselageCADTailSpacingChoiceBox().setDisable(true);
			
		}
		
		//-------------------------------------------------------------------------
		// WING OPTIONS
		if (Main.getTheAircraft().getWing() != null) {
			
			if (theController.getGenerateWingCADCheckBox().isDisabled())
				theController.getGenerateWingCADCheckBox().setDisable(false);
			theController.getGenerateWingCADCheckBox().setSelected(
					Main.getTheCADManager().getTheCADBuilderInterface().getGenerateWing());
			
			if (theController.getWingCADTipTypeChoiceBox().isDisabled())
				theController.getWingCADTipTypeChoiceBox().setDisable(false);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getWingTipType().equals(WingTipType.CUTOFF))
				theController.getWingCADTipTypeChoiceBox().getSelectionModel().select(0);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getWingTipType().equals(WingTipType.ROUNDED))
				theController.getWingCADTipTypeChoiceBox().getSelectionModel().select(1);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getWingTipType().equals(WingTipType.WINGLET))
				theController.getWingCADTipTypeChoiceBox().getSelectionModel().select(2);
						
			if (theController.getWingletCADYOffsetFactorTextField().isDisabled())
				theController.getWingletCADYOffsetFactorTextField().setDisable(false);
			theController.getWingletCADYOffsetFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getWingletYOffsetFactor()));

			if (theController.getWingletCADXOffsetFactorTextField().isDisabled())
				theController.getWingletCADXOffsetFactorTextField().setDisable(false);
			theController.getWingletCADXOffsetFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getWingletXOffsetFactor()));

			if (theController.getWingletCADTaperRatioTextField().isDisabled())
				theController.getWingletCADTaperRatioTextField().setDisable(false);
			theController.getWingletCADTaperRatioTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getWingletTaperRatio()));
		
		} else {
			
			theController.getGenerateWingCADCheckBox().setDisable(true);
			theController.getWingCADTipTypeChoiceBox().setDisable(true);
			theController.getWingletCADYOffsetFactorTextField().setDisable(true);
			theController.getWingletCADXOffsetFactorTextField().setDisable(true);
			theController.getWingletCADTaperRatioTextField().setDisable(true);
			
		}
		
		//-------------------------------------------------------------------------
		// HTAIL OPTIONS
		if (Main.getTheAircraft().getHTail() != null) {
			
			if (theController.getGenerateHTailCADCheckBox().isDisabled())
				theController.getGenerateHTailCADCheckBox().setDisable(false);
			theController.getGenerateHTailCADCheckBox().setSelected(
					Main.getTheCADManager().getTheCADBuilderInterface().getGenerateHTail());
			
			if (theController.getHTailCADTipTypeChoiceBox().isDisabled())
				theController.getHTailCADTipTypeChoiceBox().setDisable(false);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getHTailTipType().equals(WingTipType.CUTOFF))
				theController.getHTailCADTipTypeChoiceBox().getSelectionModel().select(0);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getHTailTipType().equals(WingTipType.ROUNDED))
				theController.getHTailCADTipTypeChoiceBox().getSelectionModel().select(1);
		
		} else {
			
			theController.getGenerateHTailCADCheckBox().setDisable(true);
			theController.getHTailCADTipTypeChoiceBox().setDisable(true);		
			
		}
		
		//-------------------------------------------------------------------------
		// VTAIL OPTIONS
		if (Main.getTheAircraft().getVTail() != null) {
			
			if (theController.getGenerateVTailCADCheckBox().isDisabled())
				theController.getGenerateVTailCADCheckBox().setDisable(false);
			theController.getGenerateVTailCADCheckBox().setSelected(
					Main.getTheCADManager().getTheCADBuilderInterface().getGenerateVTail());
			
			if (theController.getVTailCADTipTypeChoiceBox().isDisabled())
				theController.getVTailCADTipTypeChoiceBox().setDisable(false);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getVTailTipType().equals(WingTipType.CUTOFF))
				theController.getVTailCADTipTypeChoiceBox().getSelectionModel().select(0);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getVTailTipType().equals(WingTipType.ROUNDED))
				theController.getVTailCADTipTypeChoiceBox().getSelectionModel().select(1);
		
		} else {
			
			theController.getGenerateVTailCADCheckBox().setDisable(true);
			theController.getVTailCADTipTypeChoiceBox().setDisable(true);
			
		}
		
		//-------------------------------------------------------------------------
		// CANARD OPTIONS
		if (Main.getTheAircraft().getCanard() != null) {
			
			if (theController.getGenerateCanardCADCheckBox().isDisabled())
				theController.getGenerateCanardCADCheckBox().setDisable(false);
			theController.getGenerateCanardCADCheckBox().setSelected(
					Main.getTheCADManager().getTheCADBuilderInterface().getGenerateCanard());
			
			if (theController.getCanardCADTipTypeChoiceBox().isDisabled())
				theController.getCanardCADTipTypeChoiceBox().setDisable(false);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getCanardTipType().equals(WingTipType.CUTOFF))
				theController.getCanardCADTipTypeChoiceBox().getSelectionModel().select(0);
			if (Main.getTheCADManager().getTheCADBuilderInterface().getCanardTipType().equals(WingTipType.ROUNDED))
				theController.getCanardCADTipTypeChoiceBox().getSelectionModel().select(1);
		
		} else {
			
			theController.getGenerateCanardCADCheckBox().setDisable(true);
			theController.getCanardCADTipTypeChoiceBox().setDisable(true);
			
		}
		
		//-------------------------------------------------------------------------
		// WING FAIRING OPTIONS
		if (Main.getTheAircraft().getWing() != null && Main.getTheAircraft().getFuselage() != null) {
			
			if (theController.getGenerateWingFairingCADCheckBox().isDisabled())
				theController.getGenerateWingFairingCADCheckBox().setDisable(false);
			theController.getGenerateWingFairingCADCheckBox().setSelected(
					Main.getTheCADManager().getTheCADBuilderInterface().getGenerateWingFairing());
			
			if (theController.getWingFairingCADFrontLengthFactorTextField().isDisabled())
				theController.getWingFairingCADFrontLengthFactorTextField().setDisable(false);
			theController.getWingFairingCADFrontLengthFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getWingFairingFrontLengthFactor()));
			
			if (theController.getWingFairingCADBackLengthFactorTextField().isDisabled())
				theController.getWingFairingCADBackLengthFactorTextField().setDisable(false);
			theController.getWingFairingCADBackLengthFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getWingFairingBackLengthFactor()));
			
			if (theController.getWingFairingCADWidthFactorTextField().isDisabled())
				theController.getWingFairingCADWidthFactorTextField().setDisable(false);
			theController.getWingFairingCADWidthFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getWingFairingWidthFactor()));
			
			if (theController.getWingFairingCADHeightFactorTextField().isDisabled())
				theController.getWingFairingCADHeightFactorTextField().setDisable(false);
			theController.getWingFairingCADHeightFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getWingFairingHeightFactor()));
			
			if (theController.getWingFairingCADHeightBelowReferenceFactorTextField().isDisabled())
				theController.getWingFairingCADHeightBelowReferenceFactorTextField().setDisable(false);
			theController.getWingFairingCADHeightBelowReferenceFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getWingFairingHeightBelowReferenceFactor()));
			
			if (theController.getWingFairingCADHeightAboveReferenceFactorTextField().isDisabled())
				theController.getWingFairingCADHeightAboveReferenceFactorTextField().setDisable(false);
			theController.getWingFairingCADHeightAboveReferenceFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getWingFairingHeightAboveReferenceFactor()));
			
			if (theController.getWingFairingCADFilletRadiusFactorTextField().isDisabled())
				theController.getWingFairingCADFilletRadiusFactorTextField().setDisable(false);
			theController.getWingFairingCADFilletRadiusFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getWingFairingFilletRadiusFactor()));
			
		} else {
			
			theController.getGenerateWingFairingCADCheckBox().setDisable(true);
			theController.getWingFairingCADFrontLengthFactorTextField().setDisable(true);
			theController.getWingFairingCADBackLengthFactorTextField().setDisable(true);
			theController.getWingFairingCADWidthFactorTextField().setDisable(true);
			theController.getWingFairingCADHeightFactorTextField().setDisable(true);
			theController.getWingFairingCADHeightBelowReferenceFactorTextField().setDisable(true);
			theController.getWingFairingCADHeightAboveReferenceFactorTextField().setDisable(true);
			theController.getWingFairingCADFilletRadiusFactorTextField().setDisable(true);
			
		}
		
		//-------------------------------------------------------------------------
		// CANARD FAIRING OPTIONS
		if (Main.getTheAircraft().getCanard() != null && Main.getTheAircraft().getFuselage() != null) {
			
			if (theController.getGenerateCanardFairingCADCheckBox().isDisabled())
				theController.getGenerateCanardFairingCADCheckBox().setDisable(false);
			theController.getGenerateCanardFairingCADCheckBox().setSelected(
					Main.getTheCADManager().getTheCADBuilderInterface().getGenerateCanardFairing());
			
			if (theController.getCanardFairingCADFrontLengthFactorTextField().isDisabled())
				theController.getCanardFairingCADFrontLengthFactorTextField().setDisable(false);
			theController.getCanardFairingCADFrontLengthFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getCanardFairingFrontLengthFactor()));
			
			if (theController.getCanardFairingCADBackLengthFactorTextField().isDisabled())
				theController.getCanardFairingCADBackLengthFactorTextField().setDisable(false);
			theController.getCanardFairingCADBackLengthFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getCanardFairingBackLengthFactor()));
			
			if (theController.getCanardFairingCADWidthFactorTextField().isDisabled())
				theController.getCanardFairingCADWidthFactorTextField().setDisable(false);
			theController.getCanardFairingCADWidthFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getCanardFairingWidthFactor()));
			
			if (theController.getCanardFairingCADHeightFactorTextField().isDisabled())
				theController.getCanardFairingCADHeightFactorTextField().setDisable(false);
			theController.getCanardFairingCADHeightFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getCanardFairingHeightFactor()));
			
			if (theController.getCanardFairingCADHeightBelowReferenceFactorTextField().isDisabled())
				theController.getCanardFairingCADHeightBelowReferenceFactorTextField().setDisable(false);
			theController.getCanardFairingCADHeightBelowReferenceFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getCanardFairingHeightBelowReferenceFactor()));
			
			if (theController.getCanardFairingCADHeightAboveReferenceFactorTextField().isDisabled())
				theController.getCanardFairingCADHeightAboveReferenceFactorTextField().setDisable(false);
			theController.getCanardFairingCADHeightAboveReferenceFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getCanardFairingHeightAboveReferenceFactor()));
			
			if (theController.getCanardFairingCADFilletRadiusFactorTextField().isDisabled())
				theController.getCanardFairingCADFilletRadiusFactorTextField().setDisable(false);
			theController.getCanardFairingCADFilletRadiusFactorTextField().setText(
					String.valueOf(Main.getTheCADManager().getTheCADBuilderInterface().getCanardFairingFilletRadiusFactor()));
			
		} else {
			
			theController.getGenerateCanardFairingCADCheckBox().setDisable(true);
			theController.getCanardFairingCADFrontLengthFactorTextField().setDisable(true);
			theController.getCanardFairingCADBackLengthFactorTextField().setDisable(true);
			theController.getCanardFairingCADWidthFactorTextField().setDisable(true);
			theController.getCanardFairingCADHeightFactorTextField().setDisable(true);
			theController.getCanardFairingCADHeightBelowReferenceFactorTextField().setDisable(true);
			theController.getCanardFairingCADHeightAboveReferenceFactorTextField().setDisable(true);
			theController.getCanardFairingCADFilletRadiusFactorTextField().setDisable(true);
			
		}
		
	}

}
