package jpadcommander.inputmanager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.components.Aircraft;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import jpadcommander.Main;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class InputManagerAircraftFromFileController {

	FileChooser chooser;
	
	@FXML
	private void chooseAircraftFile() throws IOException {

		chooser = new FileChooser();
		chooser.setTitle("Open File");
		chooser.setInitialDirectory(new File(Main.getInputDirectoryPath()));
		File file = chooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			Main.getTextFieldAircraftInputFile().setText(file.getAbsolutePath());
			Main.setInputFileAbsolutePath(file.getAbsolutePath());
		}
		
	}
	
	@FXML
	private void loadAircraftFile() throws IOException, InterruptedException {
	
		if(Main.getTheAircraft() != null)
			Main.setChoiseBoxSelectionDefaultAircraft(null);
		
		if(Main.isAircraftFile(Main.getTextFieldAircraftInputFile().getText()))
			try {
				loadAircraftFileImplementation();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
	}

	private void loadAircraftFileImplementation() throws IOException, InterruptedException {
		Main.setStatus(State.RUNNING);
		Main.checkStatus(Main.getStatus());
		
		String databaseFolderPath = Main.getDatabaseDirectoryPath();
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		String highLiftDatabaseFileName = "HighLiftDatabase.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);

		String dirLiftingSurfaces = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces";
		String dirFuselages = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "fuselages";
		String dirEngines = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "engines";
		String dirNacelles = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "nacelles";
		String dirLandingGears = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "landing_gears";
		String dirSystems = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "systems";
		String dirCabinConfiguration = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "cabin_configurations";
		String dirAirfoils = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces" + File.separator + "airfoils";

		String pathToXML = Main.getTextFieldAircraftInputFile().getText(); 

		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
		System.setOut(filterStream);

		Main.setTheAircraft(Aircraft.importFromXML(
				pathToXML,
				dirLiftingSurfaces,
				dirFuselages,
				dirEngines,
				dirNacelles,
				dirLandingGears,
				dirSystems,
				dirCabinConfiguration,
				dirAirfoils,
				aeroDatabaseReader,
				highLiftDatabaseReader)
				);

		logAircraftFromFileToInterface();

		// write again
		System.setOut(originalOut);
		
		//////////////////////////////////////////////////////////////////////////////////
		Main.setStatus(State.READY);
		Main.checkStatus(Main.getStatus());
	}

	public static void logAircraftFromFileToInterface() {

		String dirLiftingSurfaces = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces";
		String dirFuselages = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "fuselages";
		String dirEngines = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "engines";
		String dirLandingGears = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "landing_gears";
		String dirSystems = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "systems";
		String dirCabinConfiguration = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "cabin_configurations";
		String pathToXML = Main.getTextFieldAircraftInputFile().getText();

		// print the toString method of the aircraft inside the text area of the GUI ...
		Main.setTextAreaAircraftConsoleOutput(
				(TextArea) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#output")
				);
		Main.getTextAreaAircraftConsoleOutput().setText(
				Main.getTheAircraft().toString()
				);

		// get the text field for aircraft input data
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		//---------------------------------------------------------------------------------
		// CABIN CONFIGURATION:
		Main.setTextFieldAircraftCabinConfiguration(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCabinConfiguration")
				);

		String cabinConfigrationFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//global_data/cabin_configuration/@file");

		if(cabinConfigrationFileName != null) 
			Main.getTextFieldAircraftCabinConfiguration().setText(
					dirCabinConfiguration 
					+ File.separator
					+ cabinConfigrationFileName
					);
		else
			Main.getTextFieldAircraftCabinConfiguration().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// FUSELAGE:
		Main.setTextFieldAircraftFuselageFile(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftFuselageFile")
				);

		String fuselageFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselages/fuselage/@file");
		if(fuselageFileName != null) 
			Main.getTextFieldAircraftFuselageFile().setText(
					dirFuselages 
					+ File.separator
					+ fuselageFileName
					);
		else
			Main.getTextFieldAircraftFuselageFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftFuselageX(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftFuselageX")
				);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldAircraftFuselageX().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftFuselageX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftFuselageY(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftFuselageY")
				);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldAircraftFuselageY().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftFuselageY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftFuselageZ(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftFuselageZ")
				);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldAircraftFuselageZ().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftFuselageZ().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// WING:
		Main.setTextFieldAircraftWingFile(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingFile")
				);

		String wingFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/wing/@file");
		if(wingFileName != null)
			Main.getTextFieldAircraftWingFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ wingFileName
					);
		else
			Main.getTextFieldAircraftWingFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftWingX(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingX")
				);
		if(Main.getTheAircraft().getWing() != null)
			Main.getTextFieldAircraftWingX().setText(
					Main.getTheAircraft()
					.getWing()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftWingX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftWingY(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingY")
				);
		if(Main.getTheAircraft().getWing() != null)
			Main.getTextFieldAircraftWingY().setText(
					Main.getTheAircraft()
					.getWing()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftWingY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftWingZ(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingZ")
				);
		if(Main.getTheAircraft().getWing() != null)
			Main.getTextFieldAircraftWingZ().setText(
					Main.getTheAircraft()
					.getWing()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftWingZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftWingRiggingAngle(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftWingRiggingAngle")
				);
		if(Main.getTheAircraft().getWing() != null)
			Main.getTextFieldAircraftWingRiggingAngle().setText(
					Main.getTheAircraft()
					.getWing()
					.getRiggingAngle()
					.toString()
					);
		else
			Main.getTextFieldAircraftWingRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// HORIZONTAL TAIL:
		Main.setTextFieldAircraftHorizontalTailFile(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailFile")
				);

		String hTailFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/horizontal_tail/@file");
		if(hTailFileName != null)
			Main.getTextFieldAircraftHorizontalTailFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ hTailFileName
					);
		else
			Main.getTextFieldAircraftHorizontalTailFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftHorizontalTailX(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailX")
				);
		if(Main.getTheAircraft().getHTail() != null)
			Main.getTextFieldAircraftHorizontalTailX().setText(
					Main.getTheAircraft()
					.getHTail()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftHorizontalTailX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftHorizontalTailY(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailY")
				);
		if(Main.getTheAircraft().getHTail() != null)
			Main.getTextFieldAircraftHorizontalTailY().setText(
					Main.getTheAircraft()
					.getHTail()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftHorizontalTailY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftHorizontalTailZ(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailZ")
				);
		if(Main.getTheAircraft().getHTail() != null)
			Main.getTextFieldAircraftHorizontalTailZ().setText(
					Main.getTheAircraft()
					.getHTail()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftHorizontalTailZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftHorizontalTailRiggingAngle(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftHTailRiggingAngle")
				);
		if(Main.getTheAircraft().getHTail() != null)
			Main.getTextFieldAircraftHorizontalTailRiggingAngle().setText(
					Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle()
					.toString()
					);
		else
			Main.getTextFieldAircraftHorizontalTailRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// VERTICAL TAIL:
		Main.setTextFieldAircraftVerticalTailFile(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailFile")
				);

		String vTailFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/vertical_tail/@file");
		if(vTailFileName != null)
			Main.getTextFieldAircraftVerticalTailFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ vTailFileName
					);
		else
			Main.getTextFieldAircraftVerticalTailFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftVerticalTailX(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailX")
				);
		if(Main.getTheAircraft().getVTail() != null)
			Main.getTextFieldAircraftVerticalTailX().setText(
					Main.getTheAircraft()
					.getVTail()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftVerticalTailX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftVerticalTailY(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailY")
				);
		if(Main.getTheAircraft().getVTail() != null)
			Main.getTextFieldAircraftVerticalTailY().setText(
					Main.getTheAircraft()
					.getVTail()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftVerticalTailY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftVerticalTailZ(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailZ")
				);
		if(Main.getTheAircraft().getVTail() != null)
			Main.getTextFieldAircraftVerticalTailZ().setText(
					Main.getTheAircraft()
					.getVTail()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftVerticalTailZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftVerticalTailRiggingAngle(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftVTailRiggingAngle")
				);
		if(Main.getTheAircraft().getVTail() != null)
			Main.getTextFieldAircraftVerticalTailRiggingAngle().setText(
					Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle()
					.toString()
					);
		else
			Main.getTextFieldAircraftHorizontalTailRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// CANARD:
		Main.setTextFieldAircraftCanardFile(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardFile")
				);

		String canardFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/canard/@file");
		if(canardFileName != null)
			Main.getTextFieldAircraftCanardFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ canardFileName
					);
		else
			Main.getTextFieldAircraftCanardFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		//		if(Main.getTextFieldAircraftCanardX() == null)
		Main.setTextFieldAircraftCanardX(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardX")
				);
		if(Main.getTheAircraft().getCanard() != null)
			Main.getTextFieldAircraftCanardX().setText(
					Main.getTheAircraft()
					.getCanard()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftCanardX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftCanardY(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardY")
				);
		if(Main.getTheAircraft().getCanard() != null)
			Main.getTextFieldAircraftCanardY().setText(
					Main.getTheAircraft()
					.getCanard()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftCanardY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftCanardZ(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardZ")
				);
		if(Main.getTheAircraft().getCanard() != null)
			Main.getTextFieldAircraftCanardZ().setText(
					Main.getTheAircraft()
					.getCanard()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftCanardZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftCanardRiggingAngle(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftCanardRiggingAngle")
				);
		if(Main.getTheAircraft().getCanard() != null)
			Main.getTextFieldAircraftCanardRiggingAngle().setText(
					Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle()
					.toString()
					);
		else
			Main.getTextFieldAircraftCanardRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// POWER PLANT:
		Main.getTextFieldAircraftEngineFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile1")
				);
		Main.getTextFieldAircraftEngineFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile2")
				);
		Main.getTextFieldAircraftEngineFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile3")
				);
		Main.getTextFieldAircraftEngineFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile4")
				);
		Main.getTextFieldAircraftEngineFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile5")
				);
		Main.getTextFieldAircraftEngineFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineFile6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftEngineXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX1")
				);
		Main.getTextFieldAircraftEngineXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX2")
				);
		Main.getTextFieldAircraftEngineXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX3")
				);
		Main.getTextFieldAircraftEngineXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX4")
				);
		Main.getTextFieldAircraftEngineXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX5")
				);
		Main.getTextFieldAircraftEngineXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineX6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftEngineYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY1")
				);
		Main.getTextFieldAircraftEngineYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY2")
				);
		Main.getTextFieldAircraftEngineYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY3")
				);
		Main.getTextFieldAircraftEngineYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY4")
				);
		Main.getTextFieldAircraftEngineYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY5")
				);
		Main.getTextFieldAircraftEngineYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineY6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftEngineZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ1")
				);
		Main.getTextFieldAircraftEngineZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ2")
				);
		Main.getTextFieldAircraftEngineZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ3")
				);
		Main.getTextFieldAircraftEngineZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ4")
				);
		Main.getTextFieldAircraftEngineZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ5")
				);
		Main.getTextFieldAircraftEngineZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineZ6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftEnginePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEnginePosition1")
				);
		Main.getTextFieldAircraftEnginePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEnginePosition2")
				);
		Main.getTextFieldAircraftEnginePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEnginePosition3")
				);
		Main.getTextFieldAircraftEnginePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEnginePosition4")
				);
		Main.getTextFieldAircraftEnginePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEnginePosition5")
				);
		Main.getTextFieldAircraftEnginePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEnginePosition6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftEngineTiltList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt1")
				);
		Main.getTextFieldAircraftEngineTiltList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt2")
				);
		Main.getTextFieldAircraftEngineTiltList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt3")
				);
		Main.getTextFieldAircraftEngineTiltList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt4")
				);
		Main.getTextFieldAircraftEngineTiltList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt5")
				);
		Main.getTextFieldAircraftEngineTiltList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftEngineTilt6")
				);
		//..........................................................................................................
		NodeList nodelistEngines = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//power_plant/engine");
		if(nodelistEngines != null) {
			for (int i = 0; i < nodelistEngines.getLength(); i++) {
				//..........................................................................................................
				Node nodeEngine  = nodelistEngines.item(i); 
				Element elementEngine = (Element) nodeEngine;
				if(elementEngine.getAttribute("file") != null)
					Main.getTextFieldAircraftEngineFileList().get(i).setText(
							dirEngines 
							+ File.separator
							+ elementEngine.getAttribute("file")	
							);
				else
					Main.getTextFieldAircraftEngineFileList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineXList().get(i).setText(
							Main.getTheAircraft()
							.getPowerPlant()
							.getEngineList()
							.get(i)
							.getXApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftEngineXList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineYList().get(i).setText(
							Main.getTheAircraft()
							.getPowerPlant()
							.getEngineList()
							.get(i)
							.getYApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftEngineYList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineZList().get(i).setText(
							Main.getTheAircraft()
							.getPowerPlant()
							.getEngineList()
							.get(i)
							.getZApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftEngineZList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEnginePositonList().get(i).setText(
							Main.getTheAircraft()
							.getPowerPlant()
							.getEngineList()
							.get(i)
							.getMountingPosition()
							.toString()
							);
				else
					Main.getTextFieldAircraftEnginePositonList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineTiltList().get(i).setText(
							Main.getTheAircraft()
							.getPowerPlant()
							.getEngineList()
							.get(i)
							.getTiltingAngle()
							.toString()
							);
				else
					Main.getTextFieldAircraftEngineTiltList().get(i).setText(
							"NOT INITIALIZED"
							);
			}
		}
		//---------------------------------------------------------------------------------
		// NACELLES:
		Main.getTextFieldAircraftNacelleFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile1")
				);
		Main.getTextFieldAircraftNacelleFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile2")
				);
		Main.getTextFieldAircraftNacelleFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile3")
				);
		Main.getTextFieldAircraftNacelleFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile4")
				);
		Main.getTextFieldAircraftNacelleFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile5")
				);
		Main.getTextFieldAircraftNacelleFileList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleFile6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftNacelleXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX1")
				);
		Main.getTextFieldAircraftNacelleXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX2")
				);
		Main.getTextFieldAircraftNacelleXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX3")
				);
		Main.getTextFieldAircraftNacelleXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX4")
				);
		Main.getTextFieldAircraftNacelleXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX5")
				);
		Main.getTextFieldAircraftNacelleXList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleX6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftNacelleYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY1")
				);
		Main.getTextFieldAircraftNacelleYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY2")
				);
		Main.getTextFieldAircraftNacelleYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY3")
				);
		Main.getTextFieldAircraftNacelleYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY4")
				);
		Main.getTextFieldAircraftNacelleYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY5")
				);
		Main.getTextFieldAircraftNacelleYList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleY6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftNacelleZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ1")
				);
		Main.getTextFieldAircraftNacelleZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ2")
				);
		Main.getTextFieldAircraftNacelleZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ3")
				);
		Main.getTextFieldAircraftNacelleZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ4")
				);
		Main.getTextFieldAircraftNacelleZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ5")
				);
		Main.getTextFieldAircraftNacelleZList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacelleZ6")
				);
		//..........................................................................................................
		Main.getTextFieldAircraftNacellePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacellePosition1")
				);
		Main.getTextFieldAircraftNacellePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacellePosition2")
				);
		Main.getTextFieldAircraftNacellePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacellePosition3")
				);
		Main.getTextFieldAircraftNacellePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacellePosition4")
				);
		Main.getTextFieldAircraftNacellePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacellePosition5")
				);
		Main.getTextFieldAircraftNacellePositonList().add(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftNacellePosition6")
				);
		//..........................................................................................................
		NodeList nodelistNacelles = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//nacelles/nacelle");
		if(nodelistNacelles != null) {
			for (int i = 0; i < nodelistNacelles.getLength(); i++) {
				//..........................................................................................................
				Node nodeNacelle  = nodelistNacelles.item(i); 
				Element elementNacelle = (Element) nodeNacelle;
				if(elementNacelle.getAttribute("file") != null)
					Main.getTextFieldAircraftNacelleFileList().get(i).setText(
							dirEngines 
							+ File.separator
							+ elementNacelle.getAttribute("file")	
							);
				else
					Main.getTextFieldAircraftNacelleFileList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleXList().get(i).setText(
							Main.getTheAircraft()
							.getNacelles()
							.getNacellesList()
							.get(i)
							.getXApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftNacelleXList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleYList().get(i).setText(
							Main.getTheAircraft()
							.getNacelles()
							.getNacellesList()
							.get(i)
							.getYApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftNacelleYList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleZList().get(i).setText(
							Main.getTheAircraft()
							.getNacelles()
							.getNacellesList()
							.get(i)
							.getZApexConstructionAxes()
							.toString()
							);
				else
					Main.getTextFieldAircraftNacelleZList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacellePositonList().get(i).setText(
							Main.getTheAircraft()
							.getNacelles()
							.getNacellesList()
							.get(i)
							.getMountingPosition()
							.toString()
							);
				else
					Main.getTextFieldAircraftNacellePositonList().get(i).setText(
							"NOT INITIALIZED"
							);
			}
		}
		//---------------------------------------------------------------------------------
		// LANDING GEARS:
		Main.setTextFieldAircraftLandingGearsFile(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftLandingGearsFile")
				);

		String landingGearsFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//landing_gears/@file");
		if(landingGearsFileName != null) 
			Main.getTextFieldAircraftLandingGearsFile().setText(
					dirLandingGears 
					+ File.separator
					+ landingGearsFileName
					);
		else
			Main.getTextFieldAircraftLandingGearsFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftLandingGearsX(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftLandingGearsX")
				);
		if(Main.getTheAircraft().getLandingGears() != null)
			Main.getTextFieldAircraftLandingGearsX().setText(
					Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftLandingGearsX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftLandingGearsY(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftLandingGearsY")
				);
		if(Main.getTheAircraft().getLandingGears() != null)
			Main.getTextFieldAircraftLandingGearsY().setText(
					Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftLandingGearsY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		//		if(Main.getTextFieldAircraftLandingGearsZ() == null)
		Main.setTextFieldAircraftLandingGearsZ(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftLandingGearsZ")
				);
		if(Main.getTheAircraft().getLandingGears() != null)
			Main.getTextFieldAircraftLandingGearsZ().setText(
					Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftLandingGearsZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftLandingGearsPosition(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftLandingGearsPosition")
				);
		if(Main.getTheAircraft().getLandingGears() != null)
			Main.getTextFieldAircraftLandingGearsPosition().setText(
					Main.getTheAircraft()
					.getLandingGears()
					.getMountingPosition()
					.toString()
					);
		else
			Main.getTextFieldAircraftLandingGearsPosition().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// SYSTEMS:
		Main.setTextFieldAircraftSystemsFile(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftSystemsFile")
				);

		String systemsFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//systems/@file");
		if(systemsFileName != null) 
			Main.getTextFieldAircraftSystemsFile().setText(
					dirSystems 
					+ File.separator
					+ systemsFileName
					);
		else
			Main.getTextFieldAircraftSystemsFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftSystemsX(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftSystemsX")
				);
		if(Main.getTheAircraft().getSystems() != null)
			Main.getTextFieldAircraftSystemsX().setText(
					Main.getTheAircraft()
					.getSystems()
					.getXApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftSystemsX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftSystemsY(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftSystemsY")
				);
		if(Main.getTheAircraft().getSystems() != null)
			Main.getTextFieldAircraftSystemsY().setText(
					Main.getTheAircraft()
					.getSystems()
					.getYApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftSystemsY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		Main.setTextFieldAircraftSystemsZ(
				(TextField) Main.getMainInputManagerAircraftSubContentFieldsLayout().lookup("#textFieldAircraftSystemsZ")
				);
		if(Main.getTheAircraft().getSystems() != null)
			Main.getTextFieldAircraftSystemsZ().setText(
					Main.getTheAircraft()
					.getSystems()
					.getZApexConstructionAxes()
					.toString()
					);
		else
			Main.getTextFieldAircraftSystemsZ().setText(
					"NOT INITIALIZED"
					);
		
		///////////////////////////////////////////////////////////////////////////////
		// TODO : ADD SVG IMAGES TO THE TOP, SIDE AND FRONT VIEW PANES SAVED IN MAIN //
		///////////////////////////////////////////////////////////////////////////////
		
	}
}
