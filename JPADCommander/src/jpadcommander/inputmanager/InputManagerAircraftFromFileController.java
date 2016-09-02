package jpadcommander.inputmanager;

import java.io.File;
import java.io.IOException;

import aircraft.components.Aircraft;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import jpadcommander.Main;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class InputManagerAircraftFromFileController {

	@FXML
	private void chooseAircraftFile() throws IOException {
		
		// get the text field for aircraft input file name
		Main.setTextFieldAircraftInputFile(
				(TextField) Main.getMainInputManagerAircraftFromFileLayout().lookup("#textFieldAircraftInputFile")
		);

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Open File");
		chooser.setInitialDirectory(new File(Main.getInputDirectoryPath()));
		File file = chooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			Main.getTextFieldAircraftInputFile().setText(file.getAbsolutePath());
		}
	}

	@FXML
	private void loadAircraftFile() throws IOException {
//		Alert alert = new Alert(
//				AlertType.INFORMATION, 
//				"Hello from DAF!\nThis action is still unimplemented.", 
//				ButtonType.OK);
//		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
//		alert.show();
		
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
		
		// get the text field for aircraft input data
		JPADXmlReader reader = new JPADXmlReader(pathToXML);
		
		// CABIN CONFIGURATION:
		Main.setTextFieldAircraftCabinConfiguration(
				(TextField) Main.getMainInputManagerAircraftFromFileLayout().lookup("#textFieldAircraftCabinConfiguration")
				);
		Main.getTextFieldAircraftCabinConfiguration().setText(
				dirCabinConfiguration 
				+ File.separator
				+ MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//global_data/cabin_configuration/@file")
				);
		
		// FUSELAGE:
		Main.setTextFieldAircraftFuselageFile(
				(TextField) Main.getMainInputManagerAircraftFromFileLayout().lookup("#textFieldAircraftFuselageFile")
				);
		Main.getTextFieldAircraftFuselageFile().setText(
				dirFuselages 
				+ File.separator
				+ MyXMLReaderUtils.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselages/fuselage/@file")
				);
		
		Main.setTextFieldAircraftFuselageFile(
				(TextField) Main.getMainInputManagerAircraftFromFileLayout().lookup("#textFieldAircraftFuselageX")
				);
		Main.getTextFieldAircraftFuselageFile().setText(
				Main.getTheAircraft()
					.getFuselage()
						.getXApexConstructionAxes()
							.toString()
						);
		
	}	
}
