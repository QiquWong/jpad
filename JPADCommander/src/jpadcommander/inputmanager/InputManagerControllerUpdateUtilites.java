package jpadcommander.inputmanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import aircraft.components.FuelTank;
import aircraft.components.ILandingGear;
import aircraft.components.ISystems;
import aircraft.components.LandingGears;
import aircraft.components.cabinconfiguration.CabinConfiguration;
import aircraft.components.cabinconfiguration.ICabinConfiguration;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.fuselage.IFuselage;
import aircraft.components.liftingSurface.ILiftingSurface;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import aircraft.components.liftingSurface.creator.IEquivalentWing;
import aircraft.components.liftingSurface.creator.ILiftingSurfacePanelCreator;
import aircraft.components.liftingSurface.creator.ISlatCreator;
import aircraft.components.liftingSurface.creator.ISpoilerCreator;
import aircraft.components.liftingSurface.creator.ISymmetricFlapCreator;
import aircraft.components.liftingSurface.creator.LiftingSurfacePanelCreator;
import aircraft.components.liftingSurface.creator.SlatCreator;
import aircraft.components.liftingSurface.creator.SpoilerCreator;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import aircraft.components.nacelles.INacelleCreator;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.nacelles.Nacelles;
import aircraft.components.powerplant.Engine;
import aircraft.components.powerplant.IEngine;
import aircraft.components.powerplant.PowerPlant;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.ClassTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.LandingGearsMountingPositionEnum;
import configuration.enumerations.NacelleMountingPositionEnum;
import configuration.enumerations.PrimaryElectricSystemsEnum;
import configuration.enumerations.RegulationsEnum;
import configuration.enumerations.WindshieldTypeEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jpadcommander.Main;

public class InputManagerControllerUpdateUtilites {

	//---------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	public InputManagerController theController;
	
	//---------------------------------------------------------------------------------
	// BUILDER
	public InputManagerControllerUpdateUtilites(InputManagerController controller) {
		
		this.theController = controller;
		
	}
	
	//---------------------------------------------------------------------------------
	// METHODS
	
	@SuppressWarnings("unchecked")
	public void updateAircraftTabData() {
		
		//.................................................................................................
		// DATA INITIALIZATION
		//.................................................................................................
		String aircraftType = "";
		String aircraftRegulation = "";
		//.................................................................................................
		String cabinConfigurationFilePath = "";
		//.................................................................................................
		String fuselageFilePath = "";
		theController.setFuselageXPositionUnit("");
		theController.setFuselageXPositionUnit("");
		theController.setFuselageYPositionValue("");
		theController.setFuselageYPositionUnit("");
		theController.setFuselageZPositionValue("");
		theController.setFuselageZPositionUnit("");
		//.................................................................................................
		String wingFilePath = "";
		theController.setWingXPositionValue("");
		theController.setWingXPositionUnit("");
		theController.setWingYPositionValue("");
		theController.setWingYPositionUnit("");
		theController.setWingZPositionValue("");
		theController.setWingZPositionUnit("");
		theController.setWingRiggingAngleValue("");
		theController.setWingRiggingAngleUnit("");
		//.................................................................................................
		String hTailFilePath = "";
		theController.sethTailXPositionValue("");
		theController.sethTailXPositionUnit("");
		theController.sethTailYPositionValue("");
		theController.sethTailYPositionUnit("");
		theController.sethTailZPositionValue("");
		theController.sethTailZPositionUnit("");
		theController.sethTailRiggingAngleValue("");
		theController.sethTailRiggingAngleUnit("");
		//.................................................................................................
		String vTailFilePath = "";
		theController.setvTailXPositionValue("");
		theController.setvTailXPositionUnit("");
		theController.setvTailYPositionValue("");
		theController.setvTailYPositionUnit("");
		theController.setvTailZPositionValue("");
		theController.setvTailZPositionUnit("");
		theController.setvTailRiggingAngleValue("");
		theController.setvTailRiggingAngleUnit("");
		//.................................................................................................
		String canardFilePath = "";
		theController.setCanardXPositionValue("");
		theController.setCanardXPositionUnit("");
		theController.setCanardYPositionValue("");
		theController.setCanardYPositionUnit("");
		theController.setCanardZPositionValue("");
		theController.setCanardZPositionUnit("");
		theController.setCanardRiggingAngleValue("");
		theController.setCanardRiggingAngleUnit("");
		//.................................................................................................
		List<String> engineFilePathList = new ArrayList<>();
		theController.setEngineXPositionValueList(new ArrayList<>());
		theController.setEngineXPositionUnitList(new ArrayList<>());
		theController.setEngineYPositionValueList(new ArrayList<>());
		theController.setEngineYPositionUnitList(new ArrayList<>());
		theController.setEngineZPositionValueList(new ArrayList<>());
		theController.setEngineZPositionUnitList(new ArrayList<>());
		theController.setEngineTiltAngleValueList(new ArrayList<>());
		theController.setEngineTiltAngleUnitList(new ArrayList<>());
		theController.setEngineMountinPositionValueList(new ArrayList<>());
		//.................................................................................................
		List<String> nacelleFilePathList = new ArrayList<>();
		theController.setNacelleXPositionValueList(new ArrayList<>());
		theController.setNacelleXPositionUnitList(new ArrayList<>());
		theController.setNacelleYPositionValueList(new ArrayList<>());
		theController.setNacelleYPositionUnitList(new ArrayList<>());
		theController.setNacelleZPositionValueList(new ArrayList<>());
		theController.setNacelleZPositionUnitList(new ArrayList<>());
		theController.setNacelleMountinPositionValueList(new ArrayList<>());
		//.................................................................................................
		String landingGearsFilePath = ("");
		theController.setNoseLandingGearsXPositionUnit("");
		theController.setNoseLandingGearsXPositionUnit("");
		theController.setNoseLandingGearsYPositionValue("");
		theController.setNoseLandingGearsYPositionUnit("");
		theController.setNoseLandingGearsZPositionValue("");
		theController.setNoseLandingGearsZPositionUnit("");
		theController.setMainLandingGearsXPositionUnit("");
		theController.setMainLandingGearsXPositionUnit("");
		theController.setMainLandingGearsYPositionValue("");
		theController.setMainLandingGearsYPositionUnit("");
		theController.setMainLandingGearsZPositionValue("");
		theController.setMainLandingGearsZPositionUnit("");
		theController.setLandingGearsMountinPositionValue("");
		//.................................................................................................
		theController.setSystemsPrimaryElectricalTypeValue("");  
		
		//.................................................................................................
		// FETCHING DATA FROM GUI FIELDS ...
		//.................................................................................................
		if(!theController.getAircraftTypeChoiceBox().getSelectionModel().isEmpty())
			aircraftType = theController.getAircraftTypeChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(!theController.getRegulationsTypeChoiceBox().getSelectionModel().isEmpty())
			aircraftRegulation = theController.getRegulationsTypeChoiceBox().getSelectionModel().getSelectedItem().toString().replace('-', '_');
		//.................................................................................................
		if(theController.getTextFieldAircraftCabinConfigurationFile().getText() != null)
			cabinConfigurationFilePath = theController.getTextFieldAircraftCabinConfigurationFile().getText();
		//.................................................................................................
		if(theController.getTextFieldAircraftFuselageFile().getText() != null)
			fuselageFilePath = theController.getTextFieldAircraftFuselageFile().getText();
		if(theController.getTextFieldAircraftFuselageX().getText() != null)
			theController.setFuselageXPositionValue(theController.getTextFieldAircraftFuselageX().getText());
		if(!theController.getFuselageXUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setFuselageXPositionUnit(theController.getFuselageXUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftFuselageY().getText() != null)
			theController.setFuselageYPositionValue(theController.getTextFieldAircraftFuselageY().getText());
		if(!theController.getFuselageYUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setFuselageYPositionUnit(theController.getFuselageYUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftFuselageZ().getText() != null)
			theController.setFuselageZPositionValue(theController.getTextFieldAircraftFuselageZ().getText());
		if(!theController.getFuselageZUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setFuselageZPositionUnit(theController.getFuselageZUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		//.................................................................................................
		if(theController.getTextFieldAircraftWingFile().getText() != null)
			wingFilePath = theController.getTextFieldAircraftWingFile().getText();
		if(theController.getTextFieldAircraftWingX().getText() != null)
			theController.setWingXPositionValue(theController.getTextFieldAircraftWingX().getText());
		if(!theController.getWingXUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setWingXPositionUnit(theController.getWingXUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftWingY().getText() != null)
			theController.setWingYPositionValue(theController.getTextFieldAircraftWingY().getText());
		if(!theController.getWingYUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setWingYPositionUnit(theController.getWingYUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftWingZ().getText() != null)
			theController.setWingZPositionValue(theController.getTextFieldAircraftWingZ().getText());
		if(!theController.getWingZUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setWingZPositionUnit(theController.getWingZUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftWingRiggingAngle().getText() != null)
			theController.setWingRiggingAngleValue(theController.getTextFieldAircraftWingRiggingAngle().getText());
		if(!theController.getWingRiggingAngleUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setWingRiggingAngleUnit(theController.getWingRiggingAngleUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		//.................................................................................................
		if(theController.getTextFieldAircraftHTailFile().getText() != null)
			hTailFilePath = theController.getTextFieldAircraftHTailFile().getText();
		if(theController.getTextFieldAircraftHTailX().getText() != null)
			theController.sethTailXPositionValue(theController.getTextFieldAircraftHTailX().getText());
		if(!theController.gethTailXUnitChoiceBox().getSelectionModel().isEmpty())
			theController.sethTailXPositionUnit(theController.gethTailXUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftHTailY().getText() != null)
			theController.sethTailYPositionValue(theController.getTextFieldAircraftHTailY().getText());
		if(!theController.gethTailYUnitChoiceBox().getSelectionModel().isEmpty())
			theController.sethTailYPositionUnit(theController.gethTailYUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftHTailZ().getText() != null)
			theController.sethTailZPositionValue(theController.getTextFieldAircraftHTailZ().getText());
		if(!theController.getHtailZUnitChoiceBox().getSelectionModel().isEmpty())
			theController.sethTailZPositionUnit(theController.getHtailZUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftHTailRiggingAngle().getText() != null)
			theController.sethTailRiggingAngleValue(theController.getTextFieldAircraftHTailRiggingAngle().getText());
		if(!theController.gethTailRiggingAngleUnitChoiceBox().getSelectionModel().isEmpty())
			theController.sethTailRiggingAngleUnit(theController.gethTailRiggingAngleUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		//.................................................................................................
		if(theController.getTextFieldAircraftVTailFile().getText() != null)
			vTailFilePath = theController.getTextFieldAircraftVTailFile().getText();
		if(theController.getTextFieldAircraftVTailX().getText() != null)
			theController.setvTailXPositionValue(theController.getTextFieldAircraftVTailX().getText());
		if(!theController.getvTailXUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setvTailXPositionUnit(theController.getvTailXUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftVTailY().getText() != null)
			theController.setvTailYPositionValue(theController.getTextFieldAircraftVTailY().getText());
		if(!theController.getvTailYUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setvTailYPositionUnit(theController.getvTailYUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftVTailZ().getText() != null)
			theController.setvTailZPositionValue(theController.getTextFieldAircraftVTailZ().getText());
		if(!theController.getvTailZUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setvTailZPositionUnit(theController.getvTailZUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftVTailRiggingAngle().getText() != null)
			theController.setvTailRiggingAngleValue(theController.getTextFieldAircraftVTailRiggingAngle().getText());
		if(!theController.getvTailRiggingAngleUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setvTailRiggingAngleUnit(theController.getvTailRiggingAngleUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		//.................................................................................................
		if(theController.getTextFieldAircraftCanardFile().getText() != null)
			canardFilePath = theController.getTextFieldAircraftCanardFile().getText();
		if(theController.getTextFieldAircraftCanardX().getText() != null)
			theController.setCanardXPositionValue(theController.getTextFieldAircraftCanardX().getText());
		if(!theController.getCanardXUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setCanardXPositionUnit(theController.getCanardXUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftCanardY().getText() != null)
			theController.setCanardYPositionValue(theController.getTextFieldAircraftCanardY().getText());
		if(!theController.getCanardYUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setCanardYPositionUnit(theController.getCanardYUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftCanardZ().getText() != null)
			theController.setCanardZPositionValue(theController.getTextFieldAircraftCanardZ().getText());
		if(!theController.getCanardZUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setCanardZPositionUnit(theController.getCanardZUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftCanardRiggingAngle().getText() != null)
			theController.setCanardRiggingAngleValue(theController.getTextFieldAircraftCanardRiggingAngle().getText());
		if(!theController.getCanardRiggingAngleUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setCanardRiggingAngleUnit(theController.getCanardRiggingAngleUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		//.................................................................................................
		if(!theController.getTextFieldsAircraftEngineFileList().isEmpty())
			theController.getTextFieldsAircraftEngineFileList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> engineFilePathList.add(tf.getText()));
		if(!theController.getTextFieldAircraftEngineXList().isEmpty())
			theController.getTextFieldAircraftEngineXList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> theController.getEngineXPositionValueList().add(tf.getText()));
		if(!theController.getChoiceBoxAircraftEngineXUnitList().isEmpty())
			theController.getChoiceBoxAircraftEngineXUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> theController.getEngineXPositionUnitList().add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldAircraftEngineYList().isEmpty())
			theController.getTextFieldAircraftEngineYList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> theController.getEngineYPositionValueList().add(tf.getText()));
		if(!theController.getChoiceBoxAircraftEngineYUnitList().isEmpty())
			theController.getChoiceBoxAircraftEngineYUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> theController.getEngineYPositionUnitList().add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldAircraftEngineZList().isEmpty())
			theController.getTextFieldAircraftEngineZList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> theController.getEngineZPositionValueList().add(tf.getText()));
		if(!theController.getChoiceBoxAircraftEngineZUnitList().isEmpty())
			theController.getChoiceBoxAircraftEngineZUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> theController.getEngineZPositionUnitList().add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldAircraftEngineTiltList().isEmpty())
			theController.getTextFieldAircraftEngineTiltList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> theController.getEngineTiltAngleValueList().add(tf.getText()));
		if(!theController.getChoiceBoxAircraftEngineTiltUnitList().isEmpty())
			theController.getChoiceBoxAircraftEngineTiltUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> theController.getEngineTiltAngleUnitList().add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getChoiceBoxesAircraftEnginePositonList().isEmpty())
			theController.getChoiceBoxesAircraftEnginePositonList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> theController.getEngineMountinPositionValueList().add(cb.getSelectionModel().getSelectedItem()));
		//.................................................................................................
		if(!theController.getTextFieldsAircraftNacelleFileList().isEmpty())
			theController.getTextFieldsAircraftNacelleFileList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> nacelleFilePathList.add(tf.getText()));
		if(!theController.getTextFieldAircraftNacelleXList().isEmpty())
			theController.getTextFieldAircraftNacelleXList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> theController.getNacelleXPositionValueList().add(tf.getText()));
		if(!theController.getChoiceBoxAircraftNacelleXUnitList().isEmpty())
			theController.getChoiceBoxAircraftNacelleXUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> theController.getNacelleXPositionUnitList().add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldAircraftNacelleYList().isEmpty())
			theController.getTextFieldAircraftNacelleYList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> theController.getNacelleYPositionValueList().add(tf.getText()));
		if(!theController.getChoiceBoxAircraftNacelleYUnitList().isEmpty())
			theController.getChoiceBoxAircraftNacelleYUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> theController.getNacelleYPositionUnitList().add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldAircraftNacelleZList().isEmpty())
			theController.getTextFieldAircraftNacelleZList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> theController.getNacelleZPositionValueList().add(tf.getText()));
		if(!theController.getChoiceBoxAircraftNacelleZUnitList().isEmpty())
			theController.getChoiceBoxAircraftNacelleZUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> theController.getNacelleZPositionUnitList().add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getChoiceBoxesAircraftNacellePositonList().isEmpty())
			theController.getChoiceBoxesAircraftNacellePositonList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> theController.getNacelleMountinPositionValueList().add(cb.getSelectionModel().getSelectedItem()));
		//.................................................................................................
		if(theController.getTextFieldAircraftLandingGearsFile().getText() != null)
			landingGearsFilePath = theController.getTextFieldAircraftLandingGearsFile().getText();
		if(theController.getTextFieldAircraftNoseLandingGearsX().getText() != null)
			theController.setNoseLandingGearsXPositionValue(theController.getTextFieldAircraftNoseLandingGearsX().getText());
		if(!theController.getNoseLandingGearsXUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setNoseLandingGearsXPositionUnit(theController.getNoseLandingGearsXUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftNoseLandingGearsY().getText() != null)
			theController.setNoseLandingGearsYPositionValue(theController.getTextFieldAircraftNoseLandingGearsY().getText());
		if(!theController.getNoseLandingGearsYUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setNoseLandingGearsYPositionUnit(theController.getNoseLandingGearsYUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftNoseLandingGearsZ().getText() != null)
			theController.setNoseLandingGearsZPositionValue(theController.getTextFieldAircraftNoseLandingGearsZ().getText());
		if(!theController.getNoseLandingGearsZUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setNoseLandingGearsZPositionUnit(theController.getNoseLandingGearsZUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftMainLandingGearsX().getText() != null)
			theController.setMainLandingGearsXPositionValue(theController.getTextFieldAircraftMainLandingGearsX().getText());
		if(!theController.getMainLandingGearsXUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setMainLandingGearsXPositionUnit(theController.getMainLandingGearsXUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftMainLandingGearsY().getText() != null)
			theController.setMainLandingGearsYPositionValue(theController.getTextFieldAircraftMainLandingGearsY().getText());
		if(!theController.getMainLandingGearsYUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setMainLandingGearsYPositionUnit(theController.getMainLandingGearsYUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAircraftMainLandingGearsZ().getText() != null)
			theController.setMainLandingGearsZPositionValue(theController.getTextFieldAircraftMainLandingGearsZ().getText());
		if(!theController.getMainLandingGearsZUnitChoiceBox().getSelectionModel().isEmpty())
			theController.setMainLandingGearsZPositionUnit(theController.getMainLandingGearsZUnitChoiceBox().getSelectionModel().getSelectedItem().toString());
		if(!theController.getLandingGearsMountingPositionTypeChoiceBox().getSelectionModel().isEmpty())
			theController.setLandingGearsMountinPositionValue(theController.getLandingGearsMountingPositionTypeChoiceBox().getSelectionModel().getSelectedItem().toString());
		//.................................................................................................
		if(!theController.getSystemsPrimaryElectricalTypeChoiceBox().getSelectionModel().isEmpty())
			theController.setSystemsPrimaryElectricalTypeValue(theController.getSystemsPrimaryElectricalTypeChoiceBox().getSelectionModel().getSelectedItem().toString());
		
		//.................................................................................................
		// FILTERING FILLED NACELLE AND ENGINES TABS ...
		//.................................................................................................
		int numberOfFilledNacelleTabs = Arrays.asList(
				nacelleFilePathList.size(),
				theController.getNacelleXPositionValueList().size(),
				theController.getNacelleXPositionUnitList().size(),
				theController.getNacelleYPositionValueList().size(),
				theController.getNacelleYPositionUnitList().size(),
				theController.getNacelleZPositionValueList().size(),
				theController.getNacelleZPositionUnitList().size(),
				theController.getNacelleMountinPositionValueList().size()
				).stream()
				.mapToInt(size -> size)
				.min()
				.getAsInt();
		
		int numberOfFilledEngineTabs = Arrays.asList(
				engineFilePathList.size(),
				theController.getEngineXPositionValueList().size(),
				theController.getEngineXPositionUnitList().size(),
				theController.getEngineYPositionValueList().size(),
				theController.getEngineYPositionUnitList().size(),
				theController.getEngineZPositionValueList().size(),
				theController.getEngineZPositionUnitList().size(),
				theController.getEngineTiltAngleValueList().size(),
				theController.getEngineTiltAngleUnitList().size(),
				theController.getEngineMountinPositionValueList().size()
				).stream()
				.mapToInt(size -> size)
				.min()
				.getAsInt();
		
		if (theController.getTabPaneAircraftNacelles().getTabs().size() > numberOfFilledNacelleTabs) {
			
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					
					//..................................................................................
					// NACELLE UPDATE WARNING
					Stage nacelleUpdateWarning = new Stage();
					
					nacelleUpdateWarning.setTitle("New Nacelle Warning");
					nacelleUpdateWarning.initModality(Modality.WINDOW_MODAL);
					nacelleUpdateWarning.initStyle(StageStyle.UNDECORATED);
					nacelleUpdateWarning.initOwner(Main.getPrimaryStage());

					FXMLLoader loader = new FXMLLoader();
					loader.setLocation(Main.class.getResource("inputmanager/UpdateNacelleWarning.fxml"));
					BorderPane nacelleUpdateWarningBorderPane = null;
					try {
						nacelleUpdateWarningBorderPane = loader.load();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					Button continueButton = (Button) nacelleUpdateWarningBorderPane.lookup("#warningContinueButton");
					continueButton.setOnAction(new EventHandler<ActionEvent>() {
						
						@Override
						public void handle(ActionEvent arg0) {
							nacelleUpdateWarning.close();
						}
						
					});
					
					Scene scene = new Scene(nacelleUpdateWarningBorderPane);
					nacelleUpdateWarning.setScene(scene);
					nacelleUpdateWarning.sizeToScene();
					nacelleUpdateWarning.show();
					
				}
			});
			
		}
		
		if (theController.getTabPaneAircraftEngines().getTabs().size() > numberOfFilledEngineTabs) {
			
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					
					//..................................................................................
					// ENGINE UPDATE WARNING
					Stage engineUpdateWarning = new Stage();
					
					engineUpdateWarning.setTitle("New Engine Warning");
					engineUpdateWarning.initModality(Modality.WINDOW_MODAL);
					engineUpdateWarning.initStyle(StageStyle.UNDECORATED);
					engineUpdateWarning.initOwner(Main.getPrimaryStage());

					FXMLLoader loader = new FXMLLoader();
					loader.setLocation(Main.class.getResource("inputmanager/UpdateEngineWarning.fxml"));
					BorderPane engineUpdateWarningBorderPane = null;
					try {
						engineUpdateWarningBorderPane = loader.load();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					Button continueButton = (Button) engineUpdateWarningBorderPane.lookup("#warningContinueButton");
					continueButton.setOnAction(new EventHandler<ActionEvent>() {
						
						@Override
						public void handle(ActionEvent arg0) {
							engineUpdateWarning.close();
						}
						
					});
					
					Scene scene = new Scene(engineUpdateWarningBorderPane);
					engineUpdateWarning.setScene(scene);
					engineUpdateWarning.sizeToScene();
					engineUpdateWarning.show();
					
				}
			});
			
		}
		
		//.................................................................................................
		// SETTING ALL DATA INSIDE THE AIRCRAFT OBJECT ...
		//.................................................................................................
		Main.getTheAircraft().setTypeVehicle(AircraftTypeEnum.valueOf(aircraftType));
		Main.getTheAircraft().setRegulations(RegulationsEnum.valueOf(aircraftRegulation));
		//.................................................................................................
		Main.getTheAircraft().setCabinConfigurationFilePath(cabinConfigurationFilePath);
		//.................................................................................................
		if(Main.getTheAircraft().getFuselage() != null) {
			Main.getTheAircraft().setFuselageFilePath(fuselageFilePath);
			Main.getTheAircraft().getFuselage().setXApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getFuselageXPositionValue()), Unit.valueOf(theController.getFuselageXPositionUnit()))
					);
			Main.getTheAircraft().getFuselage().setYApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getFuselageYPositionValue()), Unit.valueOf(theController.getFuselageYPositionUnit()))
					);
			Main.getTheAircraft().getFuselage().setZApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getFuselageZPositionValue()), Unit.valueOf(theController.getFuselageZPositionUnit()))
					);
		}
		//.................................................................................................
		if(Main.getTheAircraft().getWing() != null) {
			Main.getTheAircraft().setWingFilePath(wingFilePath);
			Main.getTheAircraft().getWing().setXApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getWingXPositionValue()), Unit.valueOf(theController.getWingXPositionUnit()))
					);
			Main.getTheAircraft().getWing().setYApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getWingYPositionValue()), Unit.valueOf(theController.getWingYPositionUnit()))
					);
			Main.getTheAircraft().getWing().setZApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getWingZPositionValue()), Unit.valueOf(theController.getWingZPositionUnit()))
					);
			Main.getTheAircraft().getWing().setRiggingAngle(
					(Amount<Angle>) Amount.valueOf(Double.valueOf(theController.getWingRiggingAngleValue()), Unit.valueOf(theController.getWingRiggingAngleUnit()))
					);
			
			Main.getTheAircraft().getFuelTank().setXApexConstructionAxes(
					Main.getTheAircraft().getWing().getXApexConstructionAxes()
					.plus(Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot()
							.times(Main.getTheAircraft().getWing().getMainSparDimensionlessPosition()
									)
							)
					);
			Main.getTheAircraft().getFuelTank().setYApexConstructionAxes(
					Main.getTheAircraft().getWing().getYApexConstructionAxes()
					);
			Main.getTheAircraft().getFuelTank().setZApexConstructionAxes(
					Main.getTheAircraft().getWing().getZApexConstructionAxes()
					);
			
		}
		//.................................................................................................
		if(Main.getTheAircraft().getHTail() != null) {
			Main.getTheAircraft().setHTailFilePath(hTailFilePath);
			Main.getTheAircraft().getHTail().setXApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.gethTailXPositionValue()), Unit.valueOf(theController.gethTailXPositionUnit()))
					);
			Main.getTheAircraft().getHTail().setYApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.gethTailYPositionValue()), Unit.valueOf(theController.gethTailYPositionUnit()))
					);
			Main.getTheAircraft().getHTail().setZApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.gethTailZPositionValue()), Unit.valueOf(theController.gethTailZPositionUnit()))
					);
			Main.getTheAircraft().getHTail().setRiggingAngle(
					(Amount<Angle>) Amount.valueOf(Double.valueOf(theController.gethTailRiggingAngleValue()), Unit.valueOf(theController.gethTailRiggingAngleUnit()))
					);
		}
		//.................................................................................................
		if(Main.getTheAircraft().getVTail() != null) {
			Main.getTheAircraft().setVTailFilePath(vTailFilePath);
			Main.getTheAircraft().getVTail().setXApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getvTailXPositionValue()), Unit.valueOf(theController.getvTailXPositionUnit()))
					);
			Main.getTheAircraft().getVTail().setYApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getvTailYPositionValue()), Unit.valueOf(theController.getvTailYPositionUnit()))
					);
			Main.getTheAircraft().getVTail().setZApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getvTailZPositionValue()), Unit.valueOf(theController.getvTailZPositionUnit()))
					);
			Main.getTheAircraft().getVTail().setRiggingAngle(
					(Amount<Angle>) Amount.valueOf(Double.valueOf(theController.getvTailRiggingAngleValue()), Unit.valueOf(theController.getvTailRiggingAngleUnit()))
					);
		}
		//.................................................................................................
		if(Main.getTheAircraft().getCanard() != null) {
			
			Main.getTheAircraft().setCanardFilePath(canardFilePath);
			Main.getTheAircraft().getCanard().setXApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getCanardXPositionValue()), Unit.valueOf(theController.getCanardXPositionUnit()))
					);
			Main.getTheAircraft().getCanard().setYApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getCanardYPositionValue()), Unit.valueOf(theController.getCanardYPositionUnit()))
					);
			Main.getTheAircraft().getCanard().setZApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getCanardZPositionValue()), Unit.valueOf(theController.getCanardZPositionUnit()))
					);
			Main.getTheAircraft().getCanard().setRiggingAngle(
					(Amount<Angle>) Amount.valueOf(Double.valueOf(theController.getCanardRiggingAngleValue()), Unit.valueOf(theController.getCanardRiggingAngleUnit()))
					);
		}
		//.................................................................................................
		if(Main.getTheAircraft().getPowerPlant() != null) {
			Main.getTheAircraft().setEngineFilePathList(new ArrayList<>());
			Main.getTheAircraft().getEngineFilePathList().addAll(engineFilePathList);
			for(int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				Engine currentEngine = Main.getTheAircraft().getPowerPlant().getEngineList().get(i);

				currentEngine.setXApexConstructionAxes(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(theController.getEngineXPositionValueList().get(i)), 
								Unit.valueOf(theController.getEngineXPositionUnitList().get(i))
								)
						);
				currentEngine.setYApexConstructionAxes(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(theController.getEngineYPositionValueList().get(i)), 
								Unit.valueOf(theController.getEngineYPositionUnitList().get(i))
								)
						);
				currentEngine.setZApexConstructionAxes(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(theController.getEngineZPositionValueList().get(i)), 
								Unit.valueOf(theController.getEngineZPositionUnitList().get(i))
								)
						);
				currentEngine.setTiltingAngle(
						(Amount<Angle>) Amount.valueOf(
								Double.valueOf(theController.getEngineTiltAngleValueList().get(i)), 
								Unit.valueOf(theController.getEngineTiltAngleUnitList().get(i))
								)
						);
				currentEngine.setMountingPosition(
						EngineMountingPositionEnum.valueOf(theController.getEngineMountinPositionValueList().get(i))
						);
			}
		}
		//.................................................................................................
		if(Main.getTheAircraft().getNacelles() != null) {
			Main.getTheAircraft().setNacelleFilePathList(new ArrayList<>());
			Main.getTheAircraft().getNacelleFilePathList().addAll(nacelleFilePathList);
			for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

				NacelleCreator currentNacelle = Main.getTheAircraft().getNacelles().getNacellesList().get(i);

				currentNacelle.setXApexConstructionAxes(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(theController.getNacelleXPositionValueList().get(i)), 
								Unit.valueOf(theController.getNacelleXPositionUnitList().get(i))
								)
						);
				currentNacelle.setYApexConstructionAxes(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(theController.getNacelleYPositionValueList().get(i)), 
								Unit.valueOf(theController.getNacelleYPositionUnitList().get(i))
								)
						);
				currentNacelle.setZApexConstructionAxes(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(theController.getNacelleZPositionValueList().get(i)), 
								Unit.valueOf(theController.getNacelleZPositionUnitList().get(i))
								)
						);
				currentNacelle.setMountingPosition(
						NacelleMountingPositionEnum.valueOf(theController.getNacelleMountinPositionValueList().get(i))
						);
			}
		}
		//.................................................................................................
		if(Main.getTheAircraft().getLandingGears() != null) {
			Main.getTheAircraft().setLandingGearsFilePath(landingGearsFilePath);
			Main.getTheAircraft().getLandingGears().setXApexConstructionAxesNoseGear(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getNoseLandingGearsXPositionValue()), Unit.valueOf(theController.getNoseLandingGearsXPositionUnit()))
					);
			Main.getTheAircraft().getLandingGears().setYApexConstructionAxesNoseGear(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getNoseLandingGearsYPositionValue()), Unit.valueOf(theController.getNoseLandingGearsYPositionUnit()))
					);
			Main.getTheAircraft().getLandingGears().setZApexConstructionAxesNoseGear(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getNoseLandingGearsZPositionValue()), Unit.valueOf(theController.getNoseLandingGearsZPositionUnit()))
					);
			Main.getTheAircraft().getLandingGears().setXApexConstructionAxesMainGear(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getMainLandingGearsXPositionValue()), Unit.valueOf(theController.getMainLandingGearsXPositionUnit()))
					);
			Main.getTheAircraft().getLandingGears().setYApexConstructionAxesMainGear(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getMainLandingGearsYPositionValue()), Unit.valueOf(theController.getMainLandingGearsYPositionUnit()))
					);
			Main.getTheAircraft().getLandingGears().setZApexConstructionAxesMainGear(
					(Amount<Length>) Amount.valueOf(Double.valueOf(theController.getMainLandingGearsZPositionValue()), Unit.valueOf(theController.getMainLandingGearsZPositionUnit()))
					);
			Main.getTheAircraft().getLandingGears().setMountingPosition(LandingGearsMountingPositionEnum.valueOf(theController.getLandingGearsMountinPositionValue()));
		}
		//.................................................................................................
		if(Main.getTheAircraft().getSystems() != null) {
			Main.getTheAircraft().getSystems().setTheSystemsInterface(
					// this is the solution to the mergeFrom() issue within service ...
					new ISystems.Builder().setPrimaryElectricSystemsType(
							PrimaryElectricSystemsEnum.valueOf(theController.getSystemsPrimaryElectricalTypeValue())
							)
					.buildPartial()
					);
			// FIXME: mergeFrom() of the FreeBuilder does not work inside a service
//			Main.getTheAircraft().getSystems().setTheSystemsInterface(
//					ISystems.Builder.from(Main.getTheAircraft().getSystems().getTheSystemsInterface())
//					.setPrimaryElectricSystemsType(PrimaryElectricSystemsEnum.valueOf(systemsPrimaryElectricalTypeValue))
//					.buildPartial()
//					);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void updateFuselageTabData() {
		
		FusDesDatabaseReader fusDesDatabaseReader = Main.getTheAircraft().getFuselage().getFusDesDatabaseReader();
		
		//.................................................................................................
		// DATA INITIALIZATION
		//.................................................................................................
		boolean fuselagePressurizedFlag = false;
		String fuselageDeckNumber = "";
		String fuselageLength = "";
		String fuselageLengthUnit = "";
		String fuselageRoughness = "";
		String fuselageRoughnessUnit = "";
		//.................................................................................................
		String fuselageNoseLengthRatio = "";
		String fuselageNoseTipOffset = "";
		String fuselageNoseTipOffsetUnit = "";
		String fuselageNoseDxCapPercent = "";
		String fuselageNoseWindshieldType = "";
		String fuselageNoseWindshieldWidth = "";
		String fuselageNoseWindshieldWidthUnit = "";
		String fuselageNoseWindshieldHeigth = "";
		String fuselageNoseWindshieldHeightUnit = "";
		String fuselageNoseMidSectionToTotalSectionHeightRatio = "";
		String fuselageNoseSectionRhoUpper = "";
		String fuselageNoseSectionRhoLower = "";
		//.................................................................................................
		String fuselageCylinderLengthRatio = "";
		String fuselageCylinderSectionWidth = "";
		String fuselageCylinderSectionWidthUnit = "";
		String fuselageCylinderSectionHeight = "";
		String fuselageCylinderSectionHeigthUnit = "";
		String fuselageCylinderHeigthFromGround = "";
		String fuselageCylinderHeigthFromGroundUnit = "";
		String fuselageCylinderMidSectionToTotalSectionHeightRatio = "";
		String fuselageCylinderSectionRhoUpper = "";
		String fuselageCylinderSectionRhoLower = "";
		//.................................................................................................
		String fuselageTailTipOffset = "";
		String fuselageTailTipOffsetUnit = "";
		String fuselageTailDxCapPercent = "";
		String fuselageTailMidSectionToTotalSectionHeightRatio = "";
		String fuselageTailSectionRhoUpper = "";
		String fuselageTailSectionRhoLower = "";
		//.................................................................................................
		List<String> fuselageSpoilersInnerSpanwisePositionList = new ArrayList<>();
		List<String> fuselageSpoilersOuterSpanwisePositionList = new ArrayList<>();
		List<String> fuselageSpoilersInnerChordwisePositionList = new ArrayList<>();
		List<String> fuselageSpoilersOuterChordwisePositionList = new ArrayList<>();
		List<String> fuselageSpoilersMaximumDeflectionAngleList = new ArrayList<>();
		List<String> fuselageSpoilersMaximumDeflectionAngleUnitList = new ArrayList<>();
		List<String> fuselageSpoilersMinimumDeflectionAngleList = new ArrayList<>();
		List<String> fuselageSpoilersMinimumDeflectionAngleUnitList = new ArrayList<>();
		
		//.................................................................................................
		// FETCHING DATA FROM GUI FIELDS ...
		//.................................................................................................
		if(theController.getFuselagePressurizedCheckBox().isSelected())
			fuselagePressurizedFlag = true;
		if(theController.getTextFieldFuselageDeckNumber().getText() != null)
			fuselageDeckNumber = theController.getTextFieldFuselageDeckNumber().getText();
		if(theController.getTextFieldFuselageLength().getText() != null)
			fuselageLength = theController.getTextFieldFuselageLength().getText();
		if(!theController.getFuselageLengthUnitChoiceBox().getSelectionModel().isEmpty())
			fuselageLengthUnit = theController.getFuselageLengthUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldFuselageSurfaceRoughness().getText() != null)
			fuselageRoughness = theController.getTextFieldFuselageSurfaceRoughness().getText();
		if(!theController.getFuselageRoughnessUnitChoiceBox().getSelectionModel().isEmpty())
			fuselageRoughnessUnit = theController.getFuselageRoughnessUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		//.................................................................................................
		if(theController.getTextFieldFuselageNoseLengthRatio().getText() != null)
			fuselageNoseLengthRatio = theController.getTextFieldFuselageNoseLengthRatio().getText();
		if(theController.getTextFieldFuselageNoseTipOffset().getText() != null)
			fuselageNoseTipOffset = theController.getTextFieldFuselageNoseTipOffset().getText();
		if(!theController.getFuselageNoseTipOffsetZUnitChoiceBox().getSelectionModel().isEmpty())
			fuselageNoseTipOffsetUnit = theController.getFuselageNoseTipOffsetZUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldFuselageNoseDxCap().getText() != null)
			fuselageNoseDxCapPercent = theController.getTextFieldFuselageNoseDxCap().getText();
		if(!theController.getWindshieldTypeChoiceBox().getSelectionModel().isEmpty())
			fuselageNoseWindshieldType = theController.getWindshieldTypeChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldFuselageNoseWindshieldWidth().getText() != null)
			fuselageNoseWindshieldWidth = theController.getTextFieldFuselageNoseWindshieldWidth().getText();
		if(!theController.getFuselageWindshieldWidthUnitChoiceBox().getSelectionModel().isEmpty())
			fuselageNoseWindshieldWidthUnit = theController.getFuselageWindshieldWidthUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldFuselageNoseWindshieldWidth().getText() != null)
			fuselageNoseWindshieldWidth = theController.getTextFieldFuselageNoseWindshieldWidth().getText();
		if(!theController.getFuselageWindshieldWidthUnitChoiceBox().getSelectionModel().isEmpty())
			fuselageNoseWindshieldWidthUnit = theController.getFuselageWindshieldWidthUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldFuselageNoseWindshieldHeight().getText() != null)
			fuselageNoseWindshieldHeigth = theController.getTextFieldFuselageNoseWindshieldHeight().getText();
		if(!theController.getFuselageWindshieldHeightUnitChoiceBox().getSelectionModel().isEmpty())
			fuselageNoseWindshieldHeightUnit = theController.getFuselageWindshieldHeightUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldFuselageNoseMidSectionHeight().getText() != null)
			fuselageNoseMidSectionToTotalSectionHeightRatio = theController.getTextFieldFuselageNoseMidSectionHeight().getText();
		if(theController.getTextFieldFuselageNoseMidSectionRhoUpper().getText() != null)
			fuselageNoseSectionRhoUpper = theController.getTextFieldFuselageNoseMidSectionRhoUpper().getText();
		if(theController.getTextFieldFuselageNoseMidSectionRhoLower().getText() != null)
			fuselageNoseSectionRhoLower = theController.getTextFieldFuselageNoseMidSectionRhoLower().getText();
		//.................................................................................................
		if(theController.getTextFieldFuselageCylinderLengthRatio().getText() != null)
			fuselageCylinderLengthRatio = theController.getTextFieldFuselageCylinderLengthRatio().getText();
		if(theController.getTextFieldFuselageCylinderSectionWidth().getText() != null)
			fuselageCylinderSectionWidth = theController.getTextFieldFuselageCylinderSectionWidth().getText();
		if(!theController.getFuselageCylinderSectionWidthUnitChoiceBox().getSelectionModel().isEmpty())
			fuselageCylinderSectionWidthUnit = theController.getFuselageCylinderSectionWidthUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldFuselageCylinderSectionHeight().getText() != null)
			fuselageCylinderSectionHeight = theController.getTextFieldFuselageCylinderSectionHeight().getText();
		if(!theController.getFuselageCylinderSectionHeightUnitChoiceBox().getSelectionModel().isEmpty())
			fuselageCylinderSectionHeigthUnit = theController.getFuselageCylinderSectionHeightUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldFuselageCylinderHeightFromGround().getText() != null)
			fuselageCylinderHeigthFromGround = theController.getTextFieldFuselageCylinderHeightFromGround().getText();
		if(!theController.getFuselageHeightFromGroundUnitChoiceBox().getSelectionModel().isEmpty())
			fuselageCylinderHeigthFromGroundUnit = theController.getFuselageHeightFromGroundUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldFuselageCylinderSectionHeightRatio().getText() != null)
			fuselageCylinderMidSectionToTotalSectionHeightRatio = theController.getTextFieldFuselageCylinderSectionHeightRatio().getText();
		if(theController.getTextFieldFuselageCylinderSectionRhoUpper().getText() != null)
			fuselageCylinderSectionRhoUpper = theController.getTextFieldFuselageCylinderSectionRhoUpper().getText();
		if(theController.getTextFieldFuselageCylinderSectionRhoLower().getText() != null)
			fuselageCylinderSectionRhoLower = theController.getTextFieldFuselageCylinderSectionRhoLower().getText();
		//.................................................................................................
		if(theController.getTextFieldFuselageTailTipOffset().getText() != null)
			fuselageTailTipOffset = theController.getTextFieldFuselageTailTipOffset().getText();
		if(!theController.getFuselageTailTipOffsetZUnitChoiceBox().getSelectionModel().isEmpty())
			fuselageTailTipOffsetUnit = theController.getFuselageTailTipOffsetZUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldFuselageTailDxCap().getText() != null)
			fuselageTailDxCapPercent = theController.getTextFieldFuselageTailDxCap().getText();
		if(theController.getTextFieldFuselageTailMidSectionHeight().getText() != null)
			fuselageTailMidSectionToTotalSectionHeightRatio = theController.getTextFieldFuselageTailMidSectionHeight().getText();
		if(theController.getTextFieldFuselageTailMidRhoLower().getText() != null)
			fuselageTailSectionRhoUpper = theController.getTextFieldFuselageTailMidRhoUpper().getText();
		if(theController.getTextFieldFuselageTailMidRhoLower().getText() != null)
			fuselageTailSectionRhoLower = theController.getTextFieldFuselageTailMidRhoLower().getText();
		//.................................................................................................
		if(!theController.getTextFieldFuselageInnerSpanwisePositionSpoilerList().isEmpty())
			theController.getTextFieldFuselageInnerSpanwisePositionSpoilerList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> fuselageSpoilersInnerSpanwisePositionList.add(tf.getText()));
		if(!theController.getTextFieldFuselageOuterSpanwisePositionSpoilerList().isEmpty())
			theController.getTextFieldFuselageOuterSpanwisePositionSpoilerList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> fuselageSpoilersOuterSpanwisePositionList.add(tf.getText()));
		if(!theController.getTextFieldFuselageInnerChordwisePositionSpoilerList().isEmpty())
			theController.getTextFieldFuselageInnerChordwisePositionSpoilerList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> fuselageSpoilersInnerChordwisePositionList.add(tf.getText()));
		if(!theController.getTextFieldFuselageOuterChordwisePositionSpoilerList().isEmpty())
			theController.getTextFieldFuselageOuterChordwisePositionSpoilerList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> fuselageSpoilersOuterChordwisePositionList.add(tf.getText()));
		if(!theController.getTextFieldFuselageMaximumDeflectionAngleSpoilerList().isEmpty())
			theController.getTextFieldFuselageMaximumDeflectionAngleSpoilerList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> fuselageSpoilersMaximumDeflectionAngleList.add(tf.getText()));
		if(!theController.getChoiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList().isEmpty())
			theController.getChoiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> fuselageSpoilersMaximumDeflectionAngleUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldFuselageMinimumDeflectionAngleSpoilerList().isEmpty())
			theController.getTextFieldFuselageMinimumDeflectionAngleSpoilerList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> fuselageSpoilersMinimumDeflectionAngleList.add(tf.getText()));
		if(!theController.getChoiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList().isEmpty())
			theController.getChoiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> fuselageSpoilersMinimumDeflectionAngleUnitList.add(cb.getSelectionModel().getSelectedItem()));
		
		//.................................................................................................
		// FILTERING FILLED SPOILERS TABS ...
		//.................................................................................................
		int numberOfFilledFuselageSpoilerTabs = Arrays.asList(
				fuselageSpoilersInnerSpanwisePositionList.size(),
				fuselageSpoilersOuterSpanwisePositionList.size(),
				fuselageSpoilersInnerChordwisePositionList.size(),
				fuselageSpoilersOuterChordwisePositionList.size(),
				fuselageSpoilersMaximumDeflectionAngleList.size(),
				fuselageSpoilersMaximumDeflectionAngleUnitList.size(),
				fuselageSpoilersMinimumDeflectionAngleList.size(),
				fuselageSpoilersMinimumDeflectionAngleUnitList.size()
				).stream()
				.mapToInt(size -> size)
				.min()
				.getAsInt();

		if (numberOfFilledFuselageSpoilerTabs > 0) {
			if (theController.getTabPaneFuselageSpoilers().getTabs().size() > numberOfFilledFuselageSpoilerTabs) {

				Platform.runLater(new Runnable() {

					@Override
					public void run() {

						//..................................................................................
						// FUSELAGE SPOILERS UPDATE WARNING
						Stage fuselageSpoilersUpdateWarning = new Stage();

						fuselageSpoilersUpdateWarning.setTitle("Fuselage Spoiler Update Warning");
						fuselageSpoilersUpdateWarning.initModality(Modality.WINDOW_MODAL);
						fuselageSpoilersUpdateWarning.initStyle(StageStyle.UNDECORATED);
						fuselageSpoilersUpdateWarning.initOwner(Main.getPrimaryStage());

						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(Main.class.getResource("inputmanager/UpdateFuselageSpoilersWarning.fxml"));
						BorderPane fuselageSpoilersUpdateWarningBorderPane = null;
						try {
							fuselageSpoilersUpdateWarningBorderPane = loader.load();
						} catch (IOException e) {
							e.printStackTrace();
						}

						Button continueButton = (Button) fuselageSpoilersUpdateWarningBorderPane.lookup("#warningContinueButton");
						continueButton.setOnAction(new EventHandler<ActionEvent>() {

							@Override
							public void handle(ActionEvent arg0) {
								fuselageSpoilersUpdateWarning.close();
							}

						});

						Scene scene = new Scene(fuselageSpoilersUpdateWarningBorderPane);
						fuselageSpoilersUpdateWarning.setScene(scene);
						fuselageSpoilersUpdateWarning.sizeToScene();
						fuselageSpoilersUpdateWarning.show();

					}
				});

			}
		}
		//.................................................................................................
		// SETTING ALL DATA INSIDE THE AIRCRAFT OBJECT ...
		//.................................................................................................
		List<SpoilerCreator> spoilersList = new ArrayList<>();
		
		for (int i=0; i<numberOfFilledFuselageSpoilerTabs; i++) {
		
			spoilersList.add(
					new SpoilerCreator(
							new ISpoilerCreator.Builder()
							.setId("Fuselage Spoiler " + (i+1) + " - " + Main.getTheAircraft().getId())
							.setInnerStationSpanwisePosition(Double.valueOf(fuselageSpoilersInnerSpanwisePositionList.get(i))) 
							.setOuterStationSpanwisePosition(Double.valueOf(fuselageSpoilersOuterSpanwisePositionList.get(i)))
							.setInnerStationChordwisePosition(Double.valueOf(fuselageSpoilersInnerChordwisePositionList.get(i)))
							.setOuterStationChordwisePosition(Double.valueOf(fuselageSpoilersOuterChordwisePositionList.get(i))) 
							.setMinimumDeflection(
									(Amount<Angle>) Amount.valueOf(
											Double.valueOf(fuselageSpoilersMinimumDeflectionAngleList.get(i)),
											Unit.valueOf(fuselageSpoilersMinimumDeflectionAngleUnitList.get(i))
											)
									)
							.setMaximumDeflection(
									(Amount<Angle>) Amount.valueOf(
											Double.valueOf(fuselageSpoilersMaximumDeflectionAngleList.get(i)), 
											Unit.valueOf(fuselageSpoilersMaximumDeflectionAngleUnitList.get(i))
											)
									)
							.build()
							)
					);
					
		}
				
		IFuselage.Builder.from(
				Main.getTheAircraft().getFuselage().getTheFuselageCreatorInterface()
				)
		// GLOBAL DATA
		.setId("Fuselage Creator - " + Main.getTheAircraft().getId())
		.setPressurized(fuselagePressurizedFlag)
		.setFuselageLength(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(fuselageLength),
						Unit.valueOf(fuselageLengthUnit)
						)
				)
		.setDeckNumber(Integer.valueOf(fuselageDeckNumber))
		.setRoughness(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(fuselageRoughness),
						Unit.valueOf(fuselageRoughnessUnit)
						)
				)
		// NOSE TRUNK
		.setNoseCapOffsetPercent(Double.valueOf(fuselageNoseDxCapPercent))
		.setNoseTipOffset(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(fuselageNoseTipOffset),
						Unit.valueOf(fuselageNoseTipOffsetUnit)
						)
				)
		.setNoseLengthRatio(Double.valueOf(fuselageNoseLengthRatio))
		.setSectionMidNoseRhoLower(Double.valueOf(fuselageNoseSectionRhoUpper))
		.setSectionMidNoseRhoUpper(Double.valueOf(fuselageNoseSectionRhoLower))
		.setSectionNoseMidLowerToTotalHeightRatio(Double.valueOf(fuselageNoseMidSectionToTotalSectionHeightRatio))
		.setWindshieldType(WindshieldTypeEnum.valueOf(fuselageNoseWindshieldType))
		.setWindshieldHeight(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(fuselageNoseWindshieldHeigth),
						Unit.valueOf(fuselageNoseWindshieldHeightUnit)
						)
				)
		.setWindshieldWidth(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(fuselageNoseWindshieldWidth),
						Unit.valueOf(fuselageNoseWindshieldWidthUnit)
						)
				)
		// CYLINDRICAL TRUNK
		.setCylinderLengthRatio(Double.valueOf(fuselageCylinderLengthRatio))
		.setSectionCylinderHeight(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(fuselageCylinderSectionHeight),
						Unit.valueOf(fuselageCylinderSectionHeigthUnit)
						)
				)
		.setSectionCylinderWidth(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(fuselageCylinderSectionWidth),
						Unit.valueOf(fuselageCylinderSectionWidthUnit)
						)
				)
		.setHeightFromGround(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(fuselageCylinderHeigthFromGround),
						Unit.valueOf(fuselageCylinderHeigthFromGroundUnit)
						)
				)
		.setSectionCylinderLowerToTotalHeightRatio(Double.valueOf(fuselageCylinderMidSectionToTotalSectionHeightRatio))
		.setSectionCylinderRhoLower(Double.valueOf(fuselageCylinderSectionRhoLower))
		.setSectionCylinderRhoUpper(Double.valueOf(fuselageCylinderSectionRhoUpper))
		// TAIL TRUNK
		.setTailTipOffest(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(fuselageTailTipOffset),
						Unit.valueOf(fuselageTailTipOffsetUnit)
						)
				)
		.setTailCapOffsetPercent(Double.valueOf(fuselageTailDxCapPercent))
		.setSectionMidTailRhoLower(Double.valueOf(fuselageTailSectionRhoLower))
		.setSectionMidTailRhoUpper(Double.valueOf(fuselageTailSectionRhoUpper))
		.setSectionTailMidLowerToTotalHeightRatio(Double.valueOf(fuselageTailMidSectionToTotalSectionHeightRatio))
		.clearSpoilers()
		.addAllSpoilers(spoilersList)
		.build();
		
		Main.getTheAircraft().getFuselage().setFusDesDatabaseReader(fusDesDatabaseReader);
		Main.getTheAircraft().getFuselage().calculateGeometry();
	}
	
	@SuppressWarnings({ "unchecked" })
	public void updateCabinConfigurationTabData() {
		
		//.................................................................................................
		// DATA INITIALIZATION
		//.................................................................................................
		String actualPassengerNumber = "";
		String maximumPassengerNumber = "";
		String flightCrewNumber = "";
		String classesNumber = "";
		List<String> classesType = new ArrayList<>();
		String aislesNumber = "";
		String xCoordinateFirstRow = "";
		String xCoordinateFirstRowUnit = "";
		//.................................................................................................
		String numberOfBrakesEconomyClass = "";
		String numberOfBrakesBusinessClass = "";
		String numberOfBrakesFirstClass = "";
		String numberOfRowsEconomyClass = "";
		String numberOfRowsBusinessClass = "";
		String numberOfRowsFirstClass = "";
		String numberOfColumnsEconomyClass = "";
		String numberOfColumnsBusinessClass = "";
		String numberOfColumnsFirstClass = "";
		String seatsPitchEconomyClass = "";
		String seatsPitchBusinessClass = "";
		String seatsPitchFirstClass = "";
		String seatsPitchEconomyClassUnit = "";
		String seatsPitchBusinessClassUnit = "";
		String seatsPitchFirstClassUnit = "";
		String seatsWidthEconomyClass = "";
		String seatsWidthBusinessClass = "";
		String seatsWidthFirstClass = "";
		String seatsWidthEconomyClassUnit = "";
		String seatsWidthBusinessClassUnit = "";
		String seatsWidthFirstClassUnit = "";
		String distanceFromWallEconomyClass = "";
		String distanceFromWallBusinessClass = "";
		String distanceFromWallFirstClass = "";
		String distanceFromWallEconomyClassUnit = "";
		String distanceFromWallBusinessClassUnit = "";
		String distanceFromWallFirstClassUnit = "";
		
		//.................................................................................................
		// FETCHING DATA FROM GUI FIELDS ...
		//.................................................................................................
		if(theController.getTextFieldActualPassengersNumber().getText() != null)
			actualPassengerNumber = theController.getTextFieldActualPassengersNumber().getText();
		if(theController.getTextFieldMaximumPassengersNumber().getText() != null)
			maximumPassengerNumber = theController.getTextFieldMaximumPassengersNumber().getText();
		if(theController.getTextFieldFlightCrewNumber().getText() != null)
			flightCrewNumber = theController.getTextFieldFlightCrewNumber().getText();
		if(theController.getTextFieldClassesNumber().getText() != null)
			classesNumber = theController.getTextFieldClassesNumber().getText();
		if(!theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().isEmpty())
			classesType.add(theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().getSelectedItem().toString());
		if(!theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().isEmpty())
			classesType.add(theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().getSelectedItem().toString());
		if(!theController.getCabinConfigurationClassesTypeChoiceBox3().getSelectionModel().isEmpty())
			classesType.add(theController.getCabinConfigurationClassesTypeChoiceBox3().getSelectionModel().getSelectedItem().toString());
		if(theController.getTextFieldAislesNumber().getText() != null)
			aislesNumber = theController.getTextFieldAislesNumber().getText();
		if(theController.getTextFieldXCoordinateFirstRow().getText() != null)
			xCoordinateFirstRow = theController.getTextFieldXCoordinateFirstRow().getText();
		if(!theController.getCabinConfigurationXCoordinateFirstRowUnitChoiceBox().getSelectionModel().isEmpty())
			xCoordinateFirstRowUnit = theController.getCabinConfigurationXCoordinateFirstRowUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		//.................................................................................................
		if(theController.getTextFieldNumberOfBrakesEconomy().getText() != null)
			numberOfBrakesEconomyClass = theController.getTextFieldNumberOfBrakesEconomy().getText();
		if(theController.getTextFieldNumberOfBrakesBusiness().getText() != null)
			numberOfBrakesBusinessClass = theController.getTextFieldNumberOfBrakesBusiness().getText();
		if(theController.getTextFieldNumberOfBrakesFirst().getText() != null)
			numberOfBrakesFirstClass = theController.getTextFieldNumberOfBrakesFirst().getText();
		if(theController.getTextFieldNumberOfRowsEconomy().getText() != null)
			numberOfRowsEconomyClass = theController.getTextFieldNumberOfRowsEconomy().getText();
		if(theController.getTextFieldNumberOfRowsBusiness().getText() != null)
			numberOfRowsBusinessClass = theController.getTextFieldNumberOfRowsBusiness().getText();
		if(theController.getTextFieldNumberOfRowsFirst().getText() != null)
			numberOfRowsFirstClass = theController.getTextFieldNumberOfRowsFirst().getText();
		if(theController.getTextFieldNumberOfColumnsEconomy().getText() != null)
			numberOfColumnsEconomyClass = theController.getTextFieldNumberOfColumnsEconomy().getText();
		if(theController.getTextFieldNumberOfColumnsBusiness().getText() != null)
			numberOfColumnsBusinessClass = theController.getTextFieldNumberOfColumnsBusiness().getText();
		if(theController.getTextFieldNumberOfColumnsFirst().getText() != null)
			numberOfColumnsFirstClass = theController.getTextFieldNumberOfColumnsFirst().getText();
		if(theController.getTextFieldSeatsPitchEconomy().getText() != null)
			seatsPitchEconomyClass = theController.getTextFieldSeatsPitchEconomy().getText();
		if(theController.getTextFieldSeatsPitchBusiness().getText() != null)
			seatsPitchBusinessClass = theController.getTextFieldSeatsPitchBusiness().getText();
		if(theController.getTextFieldSeatsPitchFirst().getText() != null)
			seatsPitchFirstClass = theController.getTextFieldSeatsPitchFirst().getText();
		if(!theController.getCabinConfigurationSeatsPitchEconomyUnitChoiceBox().getSelectionModel().isEmpty())
			seatsPitchEconomyClassUnit = theController.getCabinConfigurationSeatsPitchEconomyUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(!theController.getCabinConfigurationSeatsPitchBusinessUnitChoiceBox().getSelectionModel().isEmpty())
			seatsPitchBusinessClassUnit = theController.getCabinConfigurationSeatsPitchBusinessUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(!theController.getCabinConfigurationSeatsPitchFirstUnitChoiceBox().getSelectionModel().isEmpty())
			seatsPitchFirstClassUnit = theController.getCabinConfigurationSeatsPitchFirstUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldSeatsWidthEconomy().getText() != null)
			seatsWidthEconomyClass = theController.getTextFieldSeatsWidthEconomy().getText();
		if(theController.getTextFieldSeatsWidthBusiness().getText() != null)
			seatsWidthBusinessClass = theController.getTextFieldSeatsWidthBusiness().getText();
		if(theController.getTextFieldSeatsWidthFirst().getText() != null)
			seatsWidthFirstClass = theController.getTextFieldSeatsWidthFirst().getText();
		if(!theController.getCabinConfigurationSeatsWidthEconomyUnitChoiceBox().getSelectionModel().isEmpty())
			seatsWidthEconomyClassUnit = theController.getCabinConfigurationSeatsWidthEconomyUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(!theController.getCabinConfigurationSeatsWidthBusinessUnitChoiceBox().getSelectionModel().isEmpty())
			seatsWidthBusinessClassUnit = theController.getCabinConfigurationSeatsWidthBusinessUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(!theController.getCabinConfigurationSeatsWidthFirstUnitChoiceBox().getSelectionModel().isEmpty())
			seatsWidthFirstClassUnit = theController.getCabinConfigurationSeatsWidthFirstUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldDistanceFromWallEconomy().getText() != null)
			distanceFromWallEconomyClass = theController.getTextFieldDistanceFromWallEconomy().getText();
		if(theController.getTextFieldDistanceFromWallBusiness().getText() != null)
			distanceFromWallBusinessClass = theController.getTextFieldDistanceFromWallBusiness().getText();
		if(theController.getTextFieldDistanceFromWallFirst().getText() != null)
			distanceFromWallFirstClass = theController.getTextFieldDistanceFromWallFirst().getText();
		if(!theController.getCabinConfigurationDistanceFromWallEconomyUnitChoiceBox().getSelectionModel().isEmpty())
			distanceFromWallEconomyClassUnit = theController.getCabinConfigurationDistanceFromWallEconomyUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(!theController.getCabinConfigurationDistanceFromWallBusinessUnitChoiceBox().getSelectionModel().isEmpty())
			distanceFromWallBusinessClassUnit = theController.getCabinConfigurationDistanceFromWallBusinessUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(!theController.getCabinConfigurationDistanceFromWallFirstUnitChoiceBox().getSelectionModel().isEmpty())
			distanceFromWallFirstClassUnit = theController.getCabinConfigurationDistanceFromWallFirstUnitChoiceBox().getSelectionModel().getSelectedItem().toString();

		//.................................................................................................
		// SETTING ALL DATA INSIDE THE AIRCRAFT OBJECT ...
		//.................................................................................................
		List<String> numberOfColumnsEconomyClassSplitList = new ArrayList<>(); 
		String numberOfColumnsEconomyClassString = numberOfColumnsEconomyClass.trim();
		numberOfColumnsEconomyClassString = numberOfColumnsEconomyClassString.replace("[", "");
		numberOfColumnsEconomyClassString = numberOfColumnsEconomyClassString.replace("]", "");
		numberOfColumnsEconomyClassString = numberOfColumnsEconomyClassString.replace(",", ";");
		numberOfColumnsEconomyClassSplitList = Arrays.asList(numberOfColumnsEconomyClassString.split(";"));
		Integer[] numberOfColumnsEconomyClassArray = new Integer[numberOfColumnsEconomyClassSplitList.size()];
		for (int i=0; i<numberOfColumnsEconomyClassSplitList.size(); i++)
			numberOfColumnsEconomyClassArray[i] = Integer.valueOf(numberOfColumnsEconomyClassSplitList.get(i).trim());
		
		List<String> numberOfColumnsBusinessClassSplitList = new ArrayList<>();
		String numberOfColumnsBusinessClassString = numberOfColumnsBusinessClass.trim();
		numberOfColumnsBusinessClassString = numberOfColumnsBusinessClassString.replace("[", "");
		numberOfColumnsBusinessClassString = numberOfColumnsBusinessClassString.replace("]", "");
		numberOfColumnsBusinessClassString = numberOfColumnsBusinessClassString.replace(",", ";");
		numberOfColumnsBusinessClassSplitList = Arrays.asList(numberOfColumnsBusinessClassString.split(";"));
		Integer[] numberOfColumnsBusinessClassArray = new Integer[numberOfColumnsBusinessClassSplitList.size()];
		for (int i=0; i<numberOfColumnsBusinessClassSplitList.size(); i++)
			numberOfColumnsBusinessClassArray[i] = Integer.valueOf(numberOfColumnsBusinessClassSplitList.get(i).trim());
		
		List<String> numberOfColumnsFirstClassSplitList = new ArrayList<>();
		String numberOfColumnsFirstClassString = numberOfColumnsFirstClass.trim();
		numberOfColumnsFirstClassString = numberOfColumnsFirstClassString.replace("[", "");
		numberOfColumnsFirstClassString = numberOfColumnsFirstClassString.replace("]", "");
		numberOfColumnsFirstClassString = numberOfColumnsFirstClassString.replace(",", ";");
		numberOfColumnsFirstClassSplitList = Arrays.asList(numberOfColumnsFirstClassString.split(";"));
		Integer[] numberOfColumnsFirstClassArray = new Integer[numberOfColumnsFirstClassSplitList.size()];
		for (int i=0; i<numberOfColumnsFirstClassSplitList.size(); i++)
			numberOfColumnsFirstClassArray[i] = Integer.valueOf(numberOfColumnsFirstClassSplitList.get(i).trim());
		
		CabinConfiguration aircraftCabinConfiguration = new CabinConfiguration(
				new ICabinConfiguration.Builder()
				.setId("Cabin Configuration - " + Main.getTheAircraft().getId())
				.setActualPassengerNumber(Integer.valueOf(actualPassengerNumber))
				.setMaximumPassengerNumber(Integer.valueOf(maximumPassengerNumber))
				.setFlightCrewNumber(Integer.valueOf(flightCrewNumber))
				.setClassesNumber(Integer.valueOf(classesNumber))
				.addAllClassesType(classesType.stream()
						.map(string -> ClassTypeEnum.valueOf(string))
						.collect(Collectors.toList())
						)
				.setAislesNumber(Integer.valueOf(aislesNumber))
				.setXCoordinatesFirstRow(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(xCoordinateFirstRow),
								Unit.valueOf(xCoordinateFirstRowUnit)
								)
						)
				.setNumberOfBreaksEconomyClass(Integer.valueOf(numberOfBrakesEconomyClass))
				.setNumberOfBreaksBusinessClass(Integer.valueOf(numberOfBrakesBusinessClass))
				.setNumberOfBreaksFirstClass(Integer.valueOf(numberOfBrakesFirstClass))
				.setNumberOfRowsEconomyClass(Integer.valueOf(numberOfRowsEconomyClass))
				.setNumberOfRowsBusinessClass(Integer.valueOf(numberOfRowsBusinessClass))
				.setNumberOfRowsFirstClass(Integer.valueOf(numberOfRowsFirstClass))
				.setNumberOfColumnsEconomyClass(numberOfColumnsEconomyClassArray)
				.setNumberOfColumnsBusinessClass(numberOfColumnsBusinessClassArray)
				.setNumberOfColumnsFirstClass(numberOfColumnsFirstClassArray)
				.setPitchEconomyClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(seatsPitchEconomyClass),
								Unit.valueOf(seatsPitchEconomyClassUnit)
								)
						)
				.setPitchBusinessClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(seatsPitchBusinessClass),
								Unit.valueOf(seatsPitchBusinessClassUnit)
								)
						)
				.setPitchFirstClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(seatsPitchFirstClass),
								Unit.valueOf(seatsPitchFirstClassUnit)
								)
						)
				.setWidthEconomyClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(seatsWidthEconomyClass),
								Unit.valueOf(seatsWidthEconomyClassUnit)
								)
						)
				.setWidthBusinessClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(seatsWidthBusinessClass),
								Unit.valueOf(seatsWidthBusinessClassUnit)
								)
						)
				.setWidthFirstClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(seatsWidthFirstClass),
								Unit.valueOf(seatsWidthFirstClassUnit)
								)
						)
				.setDistanceFromWallEconomyClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(distanceFromWallEconomyClass),
								Unit.valueOf(distanceFromWallEconomyClassUnit)
								)
						)
				.setDistanceFromWallBusinessClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(distanceFromWallBusinessClass),
								Unit.valueOf(distanceFromWallBusinessClassUnit)
								)
						)
				.setDistanceFromWallFirstClass(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(distanceFromWallFirstClass),
								Unit.valueOf(distanceFromWallFirstClassUnit)
								)
						)
				.build()
				);
				
		Main.getTheAircraft().setCabinConfiguration(aircraftCabinConfiguration);

	}

	@SuppressWarnings("unchecked")
	public void updateWingTabData() {
		
		//.................................................................................................
		// DATA INITIALIZATION
		//.................................................................................................
		boolean wingEquivalentFlag = false;
		String wingMainSparLoacation = "";
		String wingSecondarySparLocation = "";
		String wingRoughness = "";
		String wingRoughnessUnit = "";
		String wingWingletHeigth = "";
		String wingWingletHeightUnit = "";
		//.................................................................................................
		String wingEquivalentArea = "";
		String wingEquivalentAreaUnit = "";
		String wingEquivalentAspectRatio = "";
		String wingEquivalentKinkEtaStation = "";
		String wingEquivalentSweepLE = "";
		String wingEquivalentSweepLEUnit = "";
		String wingEquivalentTwistAtTip = "";
		String wingEquivalentTwistAtTipUnit = "";
		String wingEquivalentDihedralAngle = "";
		String wingEquivalentDihedralAngleUnit = "";
		String wingEquivalentTaperRatio = "";
		String wingEquivalentXOffsetRootLE = "";
		String wingEquivalentXOffsetRootTE = "";
		String wingEquivalentAirfoilRootPath = "";
		String wingEquivalentAirfoilKinkPath = "";
		String wingEquivalentAirfoilTipPath = "";
		//.................................................................................................
		List<String> wingPanelsSpanList = new ArrayList<>();
		List<String> wingPanelsSpanUnitList = new ArrayList<>();
		List<String> wingPanelsSweepLEList = new ArrayList<>();
		List<String> wingPanelsSweepLEUnitList = new ArrayList<>();
		List<String> wingPanelsDihedralList = new ArrayList<>();
		List<String> wingPanelsDihedralUnitList = new ArrayList<>();
		List<String> wingPanelsInnerChordList = new ArrayList<>();
		List<String> wingPanelsInnerChordUnitList = new ArrayList<>();
		List<String> wingPanelsInnerTwistList = new ArrayList<>();
		List<String> wingPanelsInnerTwistUnitList = new ArrayList<>();
		List<String> wingPanelsInnerAirfoilPathList = new ArrayList<>();
		List<String> wingPanelsOuterChordList = new ArrayList<>();
		List<String> wingPanelsOuterChordUnitList = new ArrayList<>();
		List<String> wingPanelsOuterTwistList = new ArrayList<>();
		List<String> wingPanelsOuterTwistUnitList = new ArrayList<>();
		List<String> wingPanelsOuterAirfoilPathList = new ArrayList<>();
		//.................................................................................................
		List<String> wingFlapsTypeList = new ArrayList<>();
		List<String> wingFlapsInnerPositionList = new ArrayList<>();
		List<String> wingFlapsOuterPositionList = new ArrayList<>();
		List<String> wingFlapsInnerChordRatioList = new ArrayList<>();
		List<String> wingFlapsOuterChordRatioList = new ArrayList<>();
		List<String> wingFlapsMinimumDeflectionList = new ArrayList<>();
		List<String> wingFlapsMinimumDeflectionUnitList = new ArrayList<>();
		List<String> wingFlapsMaximumDeflectionList = new ArrayList<>();
		List<String> wingFlapsMaximumDeflectionUnitList = new ArrayList<>();
		//.................................................................................................
		List<String> wingSlatsInnerPositionList = new ArrayList<>();
		List<String> wingSlatsOuterPositionList = new ArrayList<>();
		List<String> wingSlatsInnerChordRatioList = new ArrayList<>();
		List<String> wingSlatsOuterChordRatioList = new ArrayList<>();
		List<String> wingSlatsExtensionRatioList = new ArrayList<>();
		List<String> wingSlatsMinimumDeflectionList = new ArrayList<>();
		List<String> wingSlatsMinimumDeflectionUnitList = new ArrayList<>();
		List<String> wingSlatsMaximumDeflectionList = new ArrayList<>();
		List<String> wingSlatsMaximumDeflectionUnitList = new ArrayList<>();
		//.................................................................................................
		String wingLeftAileronType = "";
		String wingLeftAileronInnerPosition = "";
		String wingLeftAileronOuterPosition = "";
		String wingLeftAileronInnerChordRatio = "";
		String wingLeftAileronOuterChordRatio = "";
		String wingLeftAileronMinimumDeflection = "";
		String wingLeftAileronMinimumDeflectionUnit = "";
		String wingLeftAileronMaximumDeflection = "";
		String wingLeftAileronMaximumDeflectionUnit = "";
		//.................................................................................................
		String wingRightAileronType = "";
		String wingRightAileronInnerPosition = "";
		String wingRightAileronOuterPosition = "";
		String wingRightAileronInnerChordRatio = "";
		String wingRightAileronOuterChordRatio = "";
		String wingRightAileronMinimumDeflection = "";
		String wingRightAileronMinimumDeflectionUnit = "";
		String wingRightAileronMaximumDeflection = "";
		String wingRightAileronMaximumDeflectionUnit = "";
		//.................................................................................................
		List<String> wingSpoilersInnerSpanwisePositionList = new ArrayList<>();
		List<String> wingSpoilersOuterSpanwisePositionList = new ArrayList<>();
		List<String> wingSpoilersInnerChordwisePositionList = new ArrayList<>();
		List<String> wingSpoilersOuterChordwisePositionList = new ArrayList<>();
		List<String> wingSpoilersMinimumDeflectionList = new ArrayList<>();
		List<String> wingSpoilersMinimumDeflectionUnitList = new ArrayList<>();
		List<String> wingSpoilersMaximumDeflectionList = new ArrayList<>();
		List<String> wingSpoilersMaximumDeflectionUnitList = new ArrayList<>();
		
		//.................................................................................................
		// FETCHING DATA FROM GUI FIELDS ...
		//.................................................................................................
		if(theController.getEquivalentWingCheckBox().isSelected())
			wingEquivalentFlag = true;
		if(theController.getTextFieldWingMainSparAdimensionalPosition().getText() != null)
			wingMainSparLoacation = theController.getTextFieldWingMainSparAdimensionalPosition().getText();
		if(theController.getTextFieldWingSecondarySparAdimensionalPosition().getText() != null)
			wingSecondarySparLocation = theController.getTextFieldWingSecondarySparAdimensionalPosition().getText();
		if(theController.getTextFieldWingRoughness().getText() != null)
			wingRoughness = theController.getTextFieldWingRoughness().getText();
		if(!theController.getWingRoughnessUnitChoiceBox().getSelectionModel().isEmpty())
			wingRoughnessUnit = theController.getWingRoughnessUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldWingWingletHeight().getText() != null)
			wingWingletHeigth = theController.getTextFieldWingWingletHeight().getText();
		if(!theController.getWingWingletHeightUnitChoiceBox().getSelectionModel().isEmpty())
			wingWingletHeightUnit = theController.getWingWingletHeightUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		//.................................................................................................
		if(theController.getTextFieldEquivalentWingArea().getText() != null)
			wingEquivalentArea = theController.getTextFieldEquivalentWingArea().getText();
		if(!theController.getEquivalentWingAreaUnitChoiceBox().getSelectionModel().isEmpty())
			wingEquivalentAreaUnit = theController.getEquivalentWingAreaUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldEquivalentWingAspectRatio().getText() != null)
			wingEquivalentAspectRatio = theController.getTextFieldEquivalentWingAspectRatio().getText();
		if(theController.getTextFieldEquivalentWingKinkPosition().getText() != null)
			wingEquivalentKinkEtaStation = theController.getTextFieldEquivalentWingKinkPosition().getText();
		if(theController.getTextFieldEquivalentWingSweepLeadingEdge().getText() != null)
			wingEquivalentSweepLE = theController.getTextFieldEquivalentWingSweepLeadingEdge().getText();
		if(!theController.getEquivalentWingSweepLEUnitChoiceBox().getSelectionModel().isEmpty())
			wingEquivalentSweepLEUnit = theController.getEquivalentWingSweepLEUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldEquivalentWingTwistAtTip().getText() != null)
			wingEquivalentTwistAtTip = theController.getTextFieldEquivalentWingTwistAtTip().getText();
		if(!theController.getEquivalentWingTwistAtTipUnitChoiceBox().getSelectionModel().isEmpty())
			wingEquivalentTwistAtTipUnit = theController.getEquivalentWingTwistAtTipUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldEquivalentWingDihedral().getText() != null)
			wingEquivalentDihedralAngle = theController.getTextFieldEquivalentWingDihedral().getText();
		if(!theController.getEquivalentWingDihedralUnitChoiceBox().getSelectionModel().isEmpty())
			wingEquivalentDihedralAngleUnit = theController.getEquivalentWingDihedralUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldEquivalentWingTaperRatio().getText() != null)
			wingEquivalentTaperRatio = theController.getTextFieldEquivalentWingTaperRatio().getText();
		if(theController.getTextFieldEquivalentWingRootXOffsetLE().getText() != null)
			wingEquivalentXOffsetRootLE = theController.getTextFieldEquivalentWingRootXOffsetLE().getText();
		if(theController.getTextFieldEquivalentWingRootXOffsetTE().getText() != null)
			wingEquivalentXOffsetRootTE = theController.getTextFieldEquivalentWingRootXOffsetTE().getText();
		if(theController.getTextFieldEquivalentWingAirfoilRootPath().getText() != null)
			wingEquivalentAirfoilRootPath = theController.getTextFieldEquivalentWingAirfoilRootPath().getText();
		if(theController.getTextFieldEquivalentWingAirfoilKinkPath().getText() != null)
			wingEquivalentAirfoilKinkPath = theController.getTextFieldEquivalentWingAirfoilKinkPath().getText();
		if(theController.getTextFieldEquivalentWingAirfoilTipPath().getText() != null)
			wingEquivalentAirfoilTipPath = theController.getTextFieldEquivalentWingAirfoilTipPath().getText();
		//.................................................................................................
		if(!theController.getTextFieldWingSpanPanelList().isEmpty())
			theController.getTextFieldWingSpanPanelList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingPanelsSpanList.add(tf.getText()));
		if(!theController.getChoiceBoxWingSpanPanelUnitList().isEmpty())
			theController.getChoiceBoxWingSpanPanelUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> wingPanelsSpanUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldWingSweepLEPanelList().isEmpty())
			theController.getTextFieldWingSweepLEPanelList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingPanelsSweepLEList.add(tf.getText()));
		if(!theController.getChoiceBoxWingSweepLEPanelUnitList().isEmpty())
			theController.getChoiceBoxWingSweepLEPanelUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> wingPanelsSweepLEUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldWingDihedralPanelList().isEmpty())
			theController.getTextFieldWingDihedralPanelList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingPanelsDihedralList.add(tf.getText()));
		if(!theController.getChoiceBoxWingDihedralPanelUnitList().isEmpty())
			theController.getChoiceBoxWingDihedralPanelUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> wingPanelsDihedralUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldWingInnerChordPanelList().isEmpty())
			theController.getTextFieldWingInnerChordPanelList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingPanelsInnerChordList.add(tf.getText()));
		if(!theController.getChoiceBoxWingInnerChordPanelUnitList().isEmpty())
			theController.getChoiceBoxWingInnerChordPanelUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> wingPanelsInnerChordUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldWingInnerTwistPanelList().isEmpty())
			theController.getTextFieldWingInnerTwistPanelList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingPanelsInnerTwistList.add(tf.getText()));
		if(!theController.getChoiceBoxWingInnerTwistPanelUnitList().isEmpty())
			theController.getChoiceBoxWingInnerTwistPanelUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> wingPanelsInnerTwistUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldWingInnerAirfoilPanelList().isEmpty())
			theController.getTextFieldWingInnerAirfoilPanelList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingPanelsInnerAirfoilPathList.add(tf.getText()));
		if(!theController.getTextFieldWingOuterChordPanelList().isEmpty())
			theController.getTextFieldWingOuterChordPanelList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingPanelsOuterChordList.add(tf.getText()));
		if(!theController.getChoiceBoxWingOuterChordPanelUnitList().isEmpty())
			theController.getChoiceBoxWingOuterChordPanelUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> wingPanelsOuterChordUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldWingOuterTwistPanelList().isEmpty())
			theController.getTextFieldWingOuterTwistPanelList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingPanelsOuterTwistList.add(tf.getText()));
		if(!theController.getChoiceBoxWingOuterTwistPanelUnitList().isEmpty())
			theController.getChoiceBoxWingOuterTwistPanelUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> wingPanelsOuterTwistUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldWingOuterAirfoilPanelList().isEmpty())
			theController.getTextFieldWingOuterAirfoilPanelList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingPanelsOuterAirfoilPathList.add(tf.getText()));
		//.................................................................................................
		if(!theController.getChoiceBoxWingFlapTypeList().isEmpty())
			theController.getChoiceBoxWingFlapTypeList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> wingFlapsTypeList.add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldWingInnerPositionFlapList().isEmpty())
			theController.getTextFieldWingInnerPositionFlapList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingFlapsInnerPositionList.add(tf.getText()));
		if(!theController.getTextFieldWingOuterPositionFlapList().isEmpty())
			theController.getTextFieldWingOuterPositionFlapList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingFlapsOuterPositionList.add(tf.getText()));
		if(!theController.getTextFieldWingInnerChordRatioFlapList().isEmpty())
			theController.getTextFieldWingInnerChordRatioFlapList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingFlapsInnerChordRatioList.add(tf.getText()));
		if(!theController.getTextFieldWingOuterChordRatioFlapList().isEmpty())
			theController.getTextFieldWingOuterChordRatioFlapList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingFlapsOuterChordRatioList.add(tf.getText()));
		if(!theController.getTextFieldWingMaximumDeflectionAngleFlapList().isEmpty())
			theController.getTextFieldWingMaximumDeflectionAngleFlapList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingFlapsMaximumDeflectionList.add(tf.getText()));
		if(!theController.getChoiceBoxWingMaximumDeflectionAngleFlapUnitList().isEmpty())
			theController.getChoiceBoxWingMaximumDeflectionAngleFlapUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> wingFlapsMaximumDeflectionUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldWingMinimumDeflectionAngleFlapList().isEmpty())
			theController.getTextFieldWingMinimumDeflectionAngleFlapList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingFlapsMinimumDeflectionList.add(tf.getText()));
		if(!theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().isEmpty())
			theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> wingFlapsMinimumDeflectionUnitList.add(cb.getSelectionModel().getSelectedItem()));
		//.................................................................................................
		if(!theController.getTextFieldWingInnerPositionSlatList().isEmpty())
			theController.getTextFieldWingInnerPositionSlatList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingSlatsInnerPositionList.add(tf.getText()));
		if(!theController.getTextFieldWingOuterPositionSlatList().isEmpty())
			theController.getTextFieldWingOuterPositionSlatList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingSlatsOuterPositionList.add(tf.getText()));
		if(!theController.getTextFieldWingInnerChordRatioSlatList().isEmpty())
			theController.getTextFieldWingInnerChordRatioSlatList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingSlatsInnerChordRatioList.add(tf.getText()));
		if(!theController.getTextFieldWingOuterChordRatioSlatList().isEmpty())
			theController.getTextFieldWingOuterChordRatioSlatList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingSlatsOuterChordRatioList.add(tf.getText()));
		if(!theController.getTextFieldWingExtensionRatioSlatList().isEmpty())
			theController.getTextFieldWingExtensionRatioSlatList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingSlatsExtensionRatioList.add(tf.getText()));
		if(!theController.getTextFieldWingMaximumDeflectionAngleSlatList().isEmpty())
			theController.getTextFieldWingMaximumDeflectionAngleSlatList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingSlatsMaximumDeflectionList.add(tf.getText()));
		if(!theController.getChoiceBoxWingMaximumDeflectionAngleSlatUnitList().isEmpty())
			theController.getChoiceBoxWingMaximumDeflectionAngleSlatUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> wingSlatsMaximumDeflectionUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldWingMinimumDeflectionAngleSlatList().isEmpty())
			theController.getTextFieldWingMinimumDeflectionAngleSlatList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingSlatsMinimumDeflectionList.add(tf.getText()));
		if(!theController.getChoiceBoxWingMinimumDeflectionAngleSlatUnitList().isEmpty())
			theController.getChoiceBoxWingMinimumDeflectionAngleSlatUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> wingSlatsMinimumDeflectionUnitList.add(cb.getSelectionModel().getSelectedItem()));
		//.................................................................................................
		if(!theController.getWingLeftAileronTypeChoichBox().getSelectionModel().isEmpty())
			wingLeftAileronType = theController.getWingLeftAileronTypeChoichBox().getSelectionModel().getSelectedItem();
		if(theController.getTextFieldWingInnerPositionAileronLeft().getText() != null)
			wingLeftAileronInnerPosition = theController.getTextFieldWingInnerPositionAileronLeft().getText();
		if(theController.getTextFieldWingOuterPositionAileronLeft().getText() != null)
			wingLeftAileronOuterPosition = theController.getTextFieldWingOuterPositionAileronLeft().getText();
		if(theController.getTextFieldWingInnerChordRatioAileronLeft().getText() != null)
			wingLeftAileronInnerChordRatio = theController.getTextFieldWingInnerChordRatioAileronLeft().getText();
		if(theController.getTextFieldWingOuterChordRatioAileronLeft().getText() != null)
			wingLeftAileronOuterChordRatio = theController.getTextFieldWingOuterChordRatioAileronLeft().getText();
		if(theController.getTextFieldWingMaximumDeflectionAngleAileronLeft().getText() != null)
			wingLeftAileronMaximumDeflection = theController.getTextFieldWingMaximumDeflectionAngleAileronLeft().getText();
		if(!theController.getWingMaximumDeflectionAngleAileronLeftUnitChoiceBox().getSelectionModel().isEmpty())
			wingLeftAileronMaximumDeflectionUnit = theController.getWingMaximumDeflectionAngleAileronLeftUnitChoiceBox().getSelectionModel().getSelectedItem();
		if(theController.getTextFieldWingMinimumDeflectionAngleAileronLeft().getText() != null)
			wingLeftAileronMinimumDeflection = theController.getTextFieldWingMinimumDeflectionAngleAileronLeft().getText();
		if(!theController.getWingMinimumDeflectionAngleAileronLeftUnitChoiceBox().getSelectionModel().isEmpty())
			wingLeftAileronMinimumDeflectionUnit = theController.getWingMinimumDeflectionAngleAileronLeftUnitChoiceBox().getSelectionModel().getSelectedItem();
		//.................................................................................................
		if(!theController.getWingRightAileronTypeChoichBox().getSelectionModel().isEmpty())
			wingRightAileronType = theController.getWingRightAileronTypeChoichBox().getSelectionModel().getSelectedItem();
		if(theController.getTextFieldWingInnerPositionAileronRight().getText() != null)
			wingRightAileronInnerPosition = theController.getTextFieldWingInnerPositionAileronRight().getText();
		if(theController.getTextFieldWingOuterPositionAileronRight().getText() != null)
			wingRightAileronOuterPosition = theController.getTextFieldWingOuterPositionAileronRight().getText();
		if(theController.getTextFieldWingInnerChordRatioAileronRight().getText() != null)
			wingRightAileronInnerChordRatio = theController.getTextFieldWingInnerChordRatioAileronRight().getText();
		if(theController.getTextFieldWingOuterChordRatioAileronRight().getText() != null)
			wingRightAileronOuterChordRatio = theController.getTextFieldWingOuterChordRatioAileronRight().getText();
		if(theController.getTextFieldWingMaximumDeflectionAngleAileronRight().getText() != null)
			wingRightAileronMaximumDeflection = theController.getTextFieldWingMaximumDeflectionAngleAileronRight().getText();
		if(!theController.getWingMaximumDeflectionAngleAileronRightUnitChoiceBox().getSelectionModel().isEmpty())
			wingRightAileronMaximumDeflectionUnit = theController.getWingMaximumDeflectionAngleAileronRightUnitChoiceBox().getSelectionModel().getSelectedItem();
		if(theController.getTextFieldWingMinimumDeflectionAngleAileronRight().getText() != null)
			wingRightAileronMinimumDeflection = theController.getTextFieldWingMinimumDeflectionAngleAileronRight().getText();
		if(!theController.getWingMinimumDeflectionAngleAileronRigthUnitChoiceBox().getSelectionModel().isEmpty())
			wingRightAileronMinimumDeflectionUnit = theController.getWingMinimumDeflectionAngleAileronRigthUnitChoiceBox().getSelectionModel().getSelectedItem();
		//.................................................................................................
		if(!theController.getTextFieldWingInnerSpanwisePositionSpoilerList().isEmpty())
			theController.getTextFieldWingInnerSpanwisePositionSpoilerList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingSpoilersInnerSpanwisePositionList.add(tf.getText()));
		if(!theController.getTextFieldWingOuterSpanwisePositionSpoilerList().isEmpty())
			theController.getTextFieldWingOuterSpanwisePositionSpoilerList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingSpoilersOuterSpanwisePositionList.add(tf.getText()));
		if(!theController.getTextFieldWingInnerChordwisePositionSpoilerList().isEmpty())
			theController.getTextFieldWingInnerChordwisePositionSpoilerList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingSpoilersInnerChordwisePositionList.add(tf.getText()));
		if(!theController.getTextFieldWingOuterChordwisePositionSpoilerList().isEmpty())
			theController.getTextFieldWingOuterChordwisePositionSpoilerList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingSpoilersOuterChordwisePositionList.add(tf.getText()));
		if(!theController.getTextFieldWingMaximumDeflectionAngleSpoilerList().isEmpty())
			theController.getTextFieldWingMaximumDeflectionAngleSpoilerList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingSpoilersMaximumDeflectionList.add(tf.getText()));
		if(!theController.getChoiceBoxWingMaximumDeflectionAngleSpoilerUnitList().isEmpty())
			theController.getChoiceBoxWingMaximumDeflectionAngleSpoilerUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> wingSpoilersMaximumDeflectionUnitList.add(cb.getSelectionModel().getSelectedItem()));
		if(!theController.getTextFieldWingMinimumDeflectionAngleSpoilerList().isEmpty())
			theController.getTextFieldWingMinimumDeflectionAngleSpoilerList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(tf -> wingSpoilersMinimumDeflectionList.add(tf.getText()));
		if(!theController.getChoiceBoxWingMinimumDeflectionAngleSpoilerUnitList().isEmpty())
			theController.getChoiceBoxWingMinimumDeflectionAngleSpoilerUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(cb -> wingSpoilersMinimumDeflectionUnitList.add(cb.getSelectionModel().getSelectedItem()));
		
		//.................................................................................................
		// FILTERING FILLED FLAPS TABS ...
		//.................................................................................................
		int numberOfFilledWingFlapsTabs = Arrays.asList(
				wingFlapsTypeList.size(),
				wingFlapsInnerPositionList.size(),
				wingFlapsOuterPositionList.size(),
				wingFlapsInnerChordRatioList.size(),
				wingFlapsOuterChordRatioList.size(),
				wingFlapsMaximumDeflectionList.size(),
				wingFlapsMaximumDeflectionUnitList.size(),
				wingFlapsMinimumDeflectionList.size(),
				wingFlapsMinimumDeflectionUnitList.size()
				).stream()
				.mapToInt(size -> size)
				.min()
				.getAsInt();

		if (numberOfFilledWingFlapsTabs > 0) {
			if (theController.getTabPaneWingFlaps().getTabs().size() > numberOfFilledWingFlapsTabs) {

				Platform.runLater(new Runnable() {

					@Override
					public void run() {

						//..................................................................................
						// WING FLAPS UPDATE WARNING
						Stage wingFlapsUpdateWarning = new Stage();

						wingFlapsUpdateWarning.setTitle("Wing Flaps Update Warning");
						wingFlapsUpdateWarning.initModality(Modality.WINDOW_MODAL);
						wingFlapsUpdateWarning.initStyle(StageStyle.UNDECORATED);
						wingFlapsUpdateWarning.initOwner(Main.getPrimaryStage());

						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(Main.class.getResource("inputmanager/UpdateWingFlapsWarning.fxml"));
						BorderPane wingFlapsUpdateWarningBorderPane = null;
						try {
							wingFlapsUpdateWarningBorderPane = loader.load();
						} catch (IOException e) {
							e.printStackTrace();
						}

						Button continueButton = (Button) wingFlapsUpdateWarningBorderPane.lookup("#warningContinueButton");
						continueButton.setOnAction(new EventHandler<ActionEvent>() {

							@Override
							public void handle(ActionEvent arg0) {
								wingFlapsUpdateWarning.close();
							}

						});

						Scene scene = new Scene(wingFlapsUpdateWarningBorderPane);
						wingFlapsUpdateWarning.setScene(scene);
						wingFlapsUpdateWarning.sizeToScene();
						wingFlapsUpdateWarning.show();

					}
				});

			}
		}
		
		List<SymmetricFlapCreator> flapList = new ArrayList<>();
		
		for (int i=0; i<numberOfFilledWingFlapsTabs; i++) {
		
			flapList.add(
					new SymmetricFlapCreator(new ISymmetricFlapCreator.Builder()
							.setId("Wing Flap " + (i+1) + " - " + Main.getTheAircraft().getId())
							.setType(FlapTypeEnum.valueOf(wingFlapsTypeList.get(i)))
							.setInnerStationSpanwisePosition(Double.valueOf(wingFlapsInnerPositionList.get(i))) 
							.setOuterStationSpanwisePosition(Double.valueOf(wingFlapsOuterPositionList.get(i))) 
							.setInnerChordRatio(Double.valueOf(wingFlapsInnerChordRatioList.get(i))) 
							.setOuterChordRatio(Double.valueOf(wingFlapsOuterChordRatioList.get(i)))
							.setMinimumDeflection((Amount<Angle>) Amount.valueOf(
									Double.valueOf(wingFlapsMinimumDeflectionList.get(i)),
									Unit.valueOf(wingFlapsMinimumDeflectionUnitList.get(i))
									))
							.setMaximumDeflection((Amount<Angle>) Amount.valueOf(
									Double.valueOf(wingFlapsMaximumDeflectionList.get(i)),
									Unit.valueOf(wingFlapsMaximumDeflectionUnitList.get(i))
									))
							.build()
							)
					);
		}
		
		//.................................................................................................
		// FILTERING FILLED SLATS TABS ...
		//.................................................................................................
		int numberOfFilledWingSlatsTabs = Arrays.asList(
				wingSlatsInnerPositionList.size(),
				wingSlatsOuterPositionList.size(),
				wingSlatsInnerChordRatioList.size(),
				wingSlatsOuterChordRatioList.size(),
				wingSlatsExtensionRatioList.size(),
				wingSlatsMaximumDeflectionList.size(),
				wingSlatsMaximumDeflectionUnitList.size(),
				wingSlatsMinimumDeflectionList.size(),
				wingSlatsMinimumDeflectionUnitList.size()
				).stream()
				.mapToInt(size -> size)
				.min()
				.getAsInt();

		if (numberOfFilledWingSlatsTabs > 0) {
			if (theController.getTabPaneWingSlats().getTabs().size() > numberOfFilledWingSlatsTabs) {

				Platform.runLater(new Runnable() {

					@Override
					public void run() {

						//..................................................................................
						// WING SLATS UPDATE WARNING
						Stage wingSlatsUpdateWarning = new Stage();

						wingSlatsUpdateWarning.setTitle("Wing Slats Update Warning");
						wingSlatsUpdateWarning.initModality(Modality.WINDOW_MODAL);
						wingSlatsUpdateWarning.initStyle(StageStyle.UNDECORATED);
						wingSlatsUpdateWarning.initOwner(Main.getPrimaryStage());

						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(Main.class.getResource("inputmanager/UpdateWingSlatsWarning.fxml"));
						BorderPane wingSlatsUpdateWarningBorderPane = null;
						try {
							wingSlatsUpdateWarningBorderPane = loader.load();
						} catch (IOException e) {
							e.printStackTrace();
						}

						Button continueButton = (Button) wingSlatsUpdateWarningBorderPane.lookup("#warningContinueButton");
						continueButton.setOnAction(new EventHandler<ActionEvent>() {

							@Override
							public void handle(ActionEvent arg0) {
								wingSlatsUpdateWarning.close();
							}

						});

						Scene scene = new Scene(wingSlatsUpdateWarningBorderPane);
						wingSlatsUpdateWarning.setScene(scene);
						wingSlatsUpdateWarning.sizeToScene();
						wingSlatsUpdateWarning.show();

					}
				});

			}
		}
		
		List<SlatCreator> slatList = new ArrayList<>();
		
		for (int i=0; i<numberOfFilledWingSlatsTabs; i++) {
		
			slatList.add(
					new SlatCreator(new ISlatCreator.Builder()
							.setId("Wing Slat " + (i+1) + " - " + Main.getTheAircraft().getId()) 
							.setInnerStationSpanwisePosition(Double.valueOf(wingSlatsInnerPositionList.get(i))) 
							.setOuterStationSpanwisePosition(Double.valueOf(wingSlatsOuterPositionList.get(i))) 
							.setInnerChordRatio(Double.valueOf(wingSlatsInnerChordRatioList.get(i))) 
							.setOuterChordRatio(Double.valueOf(wingSlatsOuterChordRatioList.get(i)))
							.setExtensionRatio(Double.valueOf(wingSlatsExtensionRatioList.get(i)))
							.setMinimumDeflection((Amount<Angle>) Amount.valueOf(
									Double.valueOf(wingSlatsMinimumDeflectionList.get(i)),
									Unit.valueOf(wingSlatsMinimumDeflectionUnitList.get(i))
									))
							.setMaximumDeflection((Amount<Angle>) Amount.valueOf(
									Double.valueOf(wingSlatsMaximumDeflectionList.get(i)), 
									Unit.valueOf(wingSlatsMaximumDeflectionUnitList.get(i))
									))
							.build()
							)
					);
		}
		
		//.................................................................................................
		// FILTERING FILLED AILERONS TABS ... /*TODO*/
		//.................................................................................................
//		int numberOfFilledWingAileronTabs = Arrays.asList(
//				wingSpoilersInnerSpanwisePositionList.size(),
//				wingSpoilersOuterSpanwisePositionList.size(),
//				wingSpoilersInnerChordwisePositionList.size(),
//				wingSpoilersOuterChordwisePositionList.size(),
//				wingSpoilersMaximumDeflectionList.size(),
//				wingSpoilersMaximumDeflectionUnitList.size(),
//				wingSpoilersMinimumDeflectionList.size(),
//				wingSpoilersMinimumDeflectionUnitList.size()
//				).stream()
//				.mapToInt(size -> size)
//				.min()
//				.getAsInt();
//
//		if (numberOfFilledWingSpoilerTabs > 0) {
//			if (tabPaneWingSpoilers.getTabs().size() > numberOfFilledWingSpoilerTabs) {
//
//				Platform.runLater(new Runnable() {
//
//					@Override
//					public void run() {
//
//						//..................................................................................
//						// WING SPOILERS UPDATE WARNING
//						Stage wingSpoilersUpdateWarning = new Stage();
//
//						wingSpoilersUpdateWarning.setTitle("Wing Spoiler Update Warning");
//						wingSpoilersUpdateWarning.initModality(Modality.WINDOW_MODAL);
//						wingSpoilersUpdateWarning.initStyle(StageStyle.UNDECORATED);
//						wingSpoilersUpdateWarning.initOwner(Main.getPrimaryStage());
//
//						FXMLLoader loader = new FXMLLoader();
//						loader.setLocation(Main.class.getResource("inputmanager/UpdateWingSpoilersWarning.fxml"));
//						BorderPane wingSpoilersUpdateWarningBorderPane = null;
//						try {
//							wingSpoilersUpdateWarningBorderPane = loader.load();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//
//						Button continueButton = (Button) wingSpoilersUpdateWarningBorderPane.lookup("#warningContinueButton");
//						continueButton.setOnAction(new EventHandler<ActionEvent>() {
//
//							@Override
//							public void handle(ActionEvent arg0) {
//								wingSpoilersUpdateWarning.close();
//							}
//
//						});
//
//						Scene scene = new Scene(wingSpoilersUpdateWarningBorderPane);
//						wingSpoilersUpdateWarning.setScene(scene);
//						wingSpoilersUpdateWarning.sizeToScene();
//						wingSpoilersUpdateWarning.show();
//
//					}
//				});
//
//			}
//		}
//		
//		List<AsymmetricFlapCreator> aileronList = new ArrayList<>();
//		
//		for (int i=0; i<numberOfFilledWingSpoilerTabs; i++) {
//		
//			spoilersList.add(
//					new SpoilerCreator.SpoilerBuilder(
//							"Wing Spoiler " + (i+1) + " - " + Main.getTheAircraft().getId(), 
//							Double.valueOf(wingSpoilersInnerSpanwisePositionList.get(i)), 
//							Double.valueOf(wingSpoilersOuterSpanwisePositionList.get(i)), 
//							Double.valueOf(wingSpoilersInnerChordwisePositionList.get(i)), 
//							Double.valueOf(wingSpoilersOuterChordwisePositionList.get(i)), 
//							(Amount<Angle>) Amount.valueOf(
//									Double.valueOf(wingSpoilersMinimumDeflectionList.get(i)),
//									Unit.valueOf(wingSpoilersMinimumDeflectionUnitList.get(i))
//									),
//							(Amount<Angle>) Amount.valueOf(
//									Double.valueOf(wingSpoilersMaximumDeflectionList.get(i)), 
//									Unit.valueOf(wingSpoilersMaximumDeflectionUnitList.get(i))
//									)
//							).build()
//					);
//		}
		
		//.................................................................................................
		// FILTERING FILLED SPOILERS TABS ...
		//.................................................................................................
		int numberOfFilledWingSpoilerTabs = Arrays.asList(
				wingSpoilersInnerSpanwisePositionList.size(),
				wingSpoilersOuterSpanwisePositionList.size(),
				wingSpoilersInnerChordwisePositionList.size(),
				wingSpoilersOuterChordwisePositionList.size(),
				wingSpoilersMaximumDeflectionList.size(),
				wingSpoilersMaximumDeflectionUnitList.size(),
				wingSpoilersMinimumDeflectionList.size(),
				wingSpoilersMinimumDeflectionUnitList.size()
				).stream()
				.mapToInt(size -> size)
				.min()
				.getAsInt();

		if (numberOfFilledWingSpoilerTabs > 0) {
			if (theController.getTabPaneWingSpoilers().getTabs().size() > numberOfFilledWingSpoilerTabs) {

				Platform.runLater(new Runnable() {

					@Override
					public void run() {

						//..................................................................................
						// WING SPOILERS UPDATE WARNING
						Stage wingSpoilersUpdateWarning = new Stage();

						wingSpoilersUpdateWarning.setTitle("Wing Spoiler Update Warning");
						wingSpoilersUpdateWarning.initModality(Modality.WINDOW_MODAL);
						wingSpoilersUpdateWarning.initStyle(StageStyle.UNDECORATED);
						wingSpoilersUpdateWarning.initOwner(Main.getPrimaryStage());

						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(Main.class.getResource("inputmanager/UpdateWingSpoilersWarning.fxml"));
						BorderPane wingSpoilersUpdateWarningBorderPane = null;
						try {
							wingSpoilersUpdateWarningBorderPane = loader.load();
						} catch (IOException e) {
							e.printStackTrace();
						}

						Button continueButton = (Button) wingSpoilersUpdateWarningBorderPane.lookup("#warningContinueButton");
						continueButton.setOnAction(new EventHandler<ActionEvent>() {

							@Override
							public void handle(ActionEvent arg0) {
								wingSpoilersUpdateWarning.close();
							}

						});

						Scene scene = new Scene(wingSpoilersUpdateWarningBorderPane);
						wingSpoilersUpdateWarning.setScene(scene);
						wingSpoilersUpdateWarning.sizeToScene();
						wingSpoilersUpdateWarning.show();

					}
				});

			}
		}
		
		List<SpoilerCreator> spoilersList = new ArrayList<>();
		
		for (int i=0; i<numberOfFilledWingSpoilerTabs; i++) {
		
			spoilersList.add(
					new SpoilerCreator(new ISpoilerCreator.Builder()
							.setId("Wing Spoiler " + (i+1) + " - " + Main.getTheAircraft().getId()) 
							.setInnerStationSpanwisePosition(Double.valueOf(wingSpoilersInnerSpanwisePositionList.get(i))) 
							.setOuterStationSpanwisePosition(Double.valueOf(wingSpoilersOuterSpanwisePositionList.get(i))) 
							.setInnerStationChordwisePosition(Double.valueOf(wingSpoilersInnerChordwisePositionList.get(i))) 
							.setOuterStationChordwisePosition(Double.valueOf(wingSpoilersOuterChordwisePositionList.get(i))) 
							.setMinimumDeflection((Amount<Angle>) Amount.valueOf(
									Double.valueOf(wingSpoilersMinimumDeflectionList.get(i)),
									Unit.valueOf(wingSpoilersMinimumDeflectionUnitList.get(i))
									))
							.setMaximumDeflection((Amount<Angle>) Amount.valueOf(
									Double.valueOf(wingSpoilersMaximumDeflectionList.get(i)), 
									Unit.valueOf(wingSpoilersMaximumDeflectionUnitList.get(i))
									))
							.build()
							)
					);
		}
		
		//.................................................................................................
		// SETTING ALL DATA INSIDE THE AIRCRAFT OBJECT ...
		//.................................................................................................
		Main.getTheAircraft().getWing().setEquivalentWingFlag(wingEquivalentFlag);
		Main.getTheAircraft().getWing().setTheLiftingSurfaceInterface(ILiftingSurface.Builder.from(
				Main.getTheAircraft().getWing().getTheLiftingSurfaceInterface()
				)
				.setMainSparDimensionlessPosition(Double.valueOf(wingMainSparLoacation))
				.setSecondarySparDimensionlessPosition(Double.valueOf(wingSecondarySparLocation))
				.build()
				);
		Main.getTheAircraft().getWing().setRoughness(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(wingRoughness),
						Unit.valueOf(wingRoughnessUnit)
						)
				);
		Main.getTheAircraft().getWing().setWingletHeight(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(wingWingletHeigth),
						Unit.valueOf(wingWingletHeightUnit)
						)
				);
		//.................................................................................................
		if(wingEquivalentFlag == true) {
			
			double aspectRatio = Double.valueOf(wingEquivalentAspectRatio);
			
			Amount<Area> area = (Amount<Area>) Amount.valueOf(
					Double.valueOf(wingEquivalentArea),
					Unit.valueOf(wingEquivalentAreaUnit)
					);
			
			double taperRatio = Double.valueOf(wingEquivalentTaperRatio);
			
			Amount<Length> span = 
					Amount.valueOf(
							Math.sqrt(
									aspectRatio*
									area.doubleValue(SI.SQUARE_METRE)),
							SI.METER
							);
			
			Amount<Length> chordRootEquivalentWing = 
					Amount.valueOf(
							(2*area.doubleValue(SI.SQUARE_METRE))
							/(span.doubleValue(SI.METER)*(1+taperRatio)),
							SI.METER
							);
			
			Amount<Length> chordTipEquivalentWing = 
					Amount.valueOf(
							taperRatio*chordRootEquivalentWing.doubleValue(SI.METER),
							SI.METER
							);

			Airfoil airfoilRoot = null;
			if(wingEquivalentAirfoilRootPath != null) {
				airfoilRoot = Airfoil.importFromXML(wingEquivalentAirfoilRootPath);
			}

			Airfoil airfoilKink = null;
			if(wingEquivalentAirfoilKinkPath != null) {
				airfoilKink = Airfoil.importFromXML(wingEquivalentAirfoilKinkPath);
			}

			Airfoil airfoilTip = null;
			if(wingEquivalentAirfoilTipPath != null) {
				airfoilTip = Airfoil.importFromXML(wingEquivalentAirfoilTipPath);
			}

			LiftingSurfacePanelCreator equivalentWingPanel = new 
					LiftingSurfacePanelCreator( new ILiftingSurfacePanelCreator.Builder()
							.setId("Equivalent wing")
							.setLinkedTo(false)
							.setChordRoot(chordRootEquivalentWing.to(SI.METER))
							.setChordTip(chordTipEquivalentWing.to(SI.METER))
							.setAirfoilRoot(airfoilRoot)
							.setAirfoilTip(airfoilTip)
							.setSpan(span.divide(2).to(SI.METER))
							.setSweepLeadingEdge(
									(Amount<Angle>) Amount.valueOf(
											Double.valueOf(wingEquivalentSweepLE),
											Unit.valueOf(wingEquivalentSweepLEUnit)
											).to(SI.RADIAN)
									)
							.setDihedral(
									(Amount<Angle>) Amount.valueOf(
											Double.valueOf(wingEquivalentDihedralAngle),
											Unit.valueOf(wingEquivalentDihedralAngleUnit)
											).to(SI.RADIAN)
									)
							.setTwistGeometricAtRoot(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE))
							.setTwistGeometricAtTip(
									(Amount<Angle>) Amount.valueOf(
											Double.valueOf(wingEquivalentTwistAtTip),
											Unit.valueOf(wingEquivalentTwistAtTipUnit)
											)
									)
							.build()
							);
			
			Main.getTheAircraft().getWing().setEquivalentWing(
					new IEquivalentWing.Builder()
					.addPanels(equivalentWingPanel)
					.setRealWingDimensionlessKinkPosition(Double.valueOf(wingEquivalentKinkEtaStation))
					.setEquivalentWingAirfoilKink(airfoilKink)
					.setRealWingDimensionlessXOffsetRootChordLE(Double.valueOf(wingEquivalentXOffsetRootLE))
					.setRealWingDimensionlessXOffsetRootChordTE(Double.valueOf(wingEquivalentXOffsetRootTE))
					.build()
					);
			
			Main.getTheAircraft().getWing().setEquivalentWingFlag(true);
			
		}
		//.................................................................................................
		else {
			
			//.................................................................................................
			// FILTERING FILLED WING PANELS TABS ...
			//.................................................................................................
			int numberOfFilledWingPanelsTabs = Arrays.asList(
					wingPanelsSpanList.size(),
					wingPanelsSpanUnitList.size(),
					wingPanelsSweepLEList.size(),
					wingPanelsSweepLEUnitList.size(),
					wingPanelsDihedralList.size(),
					wingPanelsDihedralUnitList.size(),
					wingPanelsInnerChordList.size(),
					wingPanelsInnerChordUnitList.size(),
					wingPanelsInnerTwistList.size(),
					wingPanelsInnerTwistUnitList.size(),
					wingPanelsInnerAirfoilPathList.size(),
					wingPanelsOuterChordList.size(),
					wingPanelsOuterChordUnitList.size(),
					wingPanelsOuterTwistList.size(),
					wingPanelsOuterTwistUnitList.size(),
					wingPanelsOuterAirfoilPathList.size()
					).stream()
					.mapToInt(size -> size)
					.min()
					.getAsInt();

			if (numberOfFilledWingPanelsTabs > 0) {
				if (theController.getTabPaneWingPanels().getTabs().size() > numberOfFilledWingPanelsTabs) {

					Platform.runLater(new Runnable() {

						@Override
						public void run() {

							//..................................................................................
							// WING SPOILERS UPDATE WARNING
							Stage wingPanelsUpdateWarning = new Stage();

							wingPanelsUpdateWarning.setTitle("Wing Panels Update Warning");
							wingPanelsUpdateWarning.initModality(Modality.WINDOW_MODAL);
							wingPanelsUpdateWarning.initStyle(StageStyle.UNDECORATED);
							wingPanelsUpdateWarning.initOwner(Main.getPrimaryStage());

							FXMLLoader loader = new FXMLLoader();
							loader.setLocation(Main.class.getResource("inputmanager/UpdateWingPanelsWarning.fxml"));
							BorderPane wingPanelsUpdateWarningBorderPane = null;
							try {
								wingPanelsUpdateWarningBorderPane = loader.load();
							} catch (IOException e) {
								e.printStackTrace();
							}

							Button continueButton = (Button) wingPanelsUpdateWarningBorderPane.lookup("#warningContinueButton");
							continueButton.setOnAction(new EventHandler<ActionEvent>() {

								@Override
								public void handle(ActionEvent arg0) {
									wingPanelsUpdateWarning.close();
								}

							});

							Scene scene = new Scene(wingPanelsUpdateWarningBorderPane);
							wingPanelsUpdateWarning.setScene(scene);
							wingPanelsUpdateWarning.sizeToScene();
							wingPanelsUpdateWarning.show();

						}
					});

				}
			}
			
			List<LiftingSurfacePanelCreator> panelsList = new ArrayList<>();
			
			for (int i=0; i<numberOfFilledWingPanelsTabs; i++) {
			
				panelsList.add(
						new LiftingSurfacePanelCreator(
								new ILiftingSurfacePanelCreator.Builder()
								.setId("Wing Panels " + (i+1) + " - " + Main.getTheAircraft().getId())
								.setLinkedTo(false)
								.setChordRoot(
										(Amount<Length>) Amount.valueOf(
												Double.valueOf(wingPanelsInnerChordList.get(i)),
												Unit.valueOf(wingPanelsInnerChordUnitList.get(i))
												)
										)
								.setChordTip(
										(Amount<Length>) Amount.valueOf(
												Double.valueOf(wingPanelsOuterChordList.get(i)),
												Unit.valueOf(wingPanelsOuterChordUnitList.get(i))
												)
										)
								.setAirfoilRoot(Airfoil.importFromXML(wingPanelsInnerAirfoilPathList.get(i)))
								.setAirfoilTip(Airfoil.importFromXML(wingPanelsOuterAirfoilPathList.get(i)))
								.setTwistGeometricAtRoot(
										(Amount<Angle>) Amount.valueOf(
												Double.valueOf(wingPanelsInnerTwistList.get(i)),
												Unit.valueOf(wingPanelsInnerTwistUnitList.get(i))
												)
										)
								.setTwistGeometricAtTip(
										(Amount<Angle>) Amount.valueOf(
												Double.valueOf(wingPanelsOuterTwistList.get(i)),
												Unit.valueOf(wingPanelsOuterTwistUnitList.get(i))
												)
										)
								.setSpan(
										(Amount<Length>) Amount.valueOf(
												Double.valueOf(wingPanelsSpanList.get(i)),
												Unit.valueOf(wingPanelsSpanUnitList.get(i))
												)
										)
								.setSweepLeadingEdge(
										(Amount<Angle>) Amount.valueOf(
												Double.valueOf(wingPanelsSweepLEList.get(i)),
												Unit.valueOf(wingPanelsSweepLEUnitList.get(i))
												)
										)
								.setDihedral(
										(Amount<Angle>) Amount.valueOf(
												Double.valueOf(wingPanelsDihedralList.get(i)),
												Unit.valueOf(wingPanelsDihedralUnitList.get(i))
												)
										)
								.setAirfoilRootFilePath(
										wingPanelsInnerAirfoilPathList.get(i)
										)
								.setAirfoilTipFilePath(
										wingPanelsOuterAirfoilPathList.get(i)
										)
								.build()
								)
						);
								
			}
			
			Main.getTheAircraft().getWing().getPanels().clear();
			Main.getTheAircraft().getWing().setPanels(panelsList);
			
		}
		//.................................................................................................
		
		Main.getTheAircraft().getWing().getSymmetricFlaps().clear();
		Main.getTheAircraft().getWing().getAsymmetricFlaps().clear();
		Main.getTheAircraft().getWing().getSlats().clear();
		Main.getTheAircraft().getWing().getSpoilers().clear();
		
		Main.getTheAircraft().getWing().getSymmetricFlaps().addAll(flapList);
//		Main.getTheAircraft().getWing().getAsymmetricFlaps().addAll(flapList);
		Main.getTheAircraft().getWing().getSlats().addAll(slatList);
		Main.getTheAircraft().getWing().getSpoilers().addAll(spoilersList);
		
		//.................................................................................................
		Main.getTheAircraft().getWing().calculateGeometry(
				40,
				Main.getTheAircraft().getWing().getType(),
				Main.getTheAircraft().getWing().isMirrored()
				);
		Main.getTheAircraft().getWing().populateAirfoilList(
				Main.getTheAircraft().getWing().getEquivalentWingFlag()
				);
		
	}
	
	public void updateHTailTabData() {
		
		// TODO: AFTER MATHCING ADJUST CRITERION WITH THE DATA MODEL
		
	}
	
	public void updateVTailTabData() {
		
		// TODO: AFTER MATHCING ADJUST CRITERION WITH THE DATA MODEL
		
	}
	
	public void updateCanardTabData() {
		
		// TODO: AFTER MATHCING ADJUST CRITERION WITH THE DATA MODEL
		
	}
	
	@SuppressWarnings("unchecked")
	public void updateNacelleTabData() throws IOException {
		
		//.................................................................................................
		// DATA INITIALIZATION
		//.................................................................................................
		List<String> nacelleRoughnessList = new ArrayList<>();
		List<String> nacelleRoughnessUnitList = new ArrayList<>();
		//.................................................................................................
		List<String> nacelleLengthList = new ArrayList<>();
		List<String> nacelleLengthUnitList = new ArrayList<>();
		List<String> nacelleMaximumDiameterList = new ArrayList<>();
		List<String> nacelleMaximumDiameterUnitList = new ArrayList<>();
		List<String> nacelleKInletList = new ArrayList<>();
		List<String> nacelleKOutletList = new ArrayList<>();
		List<String> nacelleKLengthList = new ArrayList<>();
		List<String> nacelleKDiameterOutletList = new ArrayList<>();
	
		//.................................................................................................
		// FETCHING DATA FROM GUI FIELDS ...
		//.................................................................................................
		if(!theController.getTextFieldNacelleRoughnessList().isEmpty())
			theController.getTextFieldNacelleRoughnessList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(
					tf -> nacelleRoughnessList.add(tf.getText())
					);
		if(!theController.getChoiceBoxNacelleRoughnessUnitList().isEmpty())
			theController.getChoiceBoxNacelleRoughnessUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(
					cb -> nacelleRoughnessUnitList.add(cb.getSelectionModel().getSelectedItem().toString())
					);
		//.................................................................................................
		if(!theController.getTextFieldNacelleLengthList().isEmpty())
			theController.getTextFieldNacelleLengthList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(
					tf -> nacelleLengthList.add(tf.getText())
					);
		if(!theController.getChoiceBoxNacelleLengthUnitList().isEmpty())
			theController.getChoiceBoxNacelleLengthUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(
					cb -> nacelleLengthUnitList.add(cb.getSelectionModel().getSelectedItem().toString())
					);
		if(!theController.getTextFieldNacelleMaximumDiameterList().isEmpty())
			theController.getTextFieldNacelleMaximumDiameterList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(
					tf -> nacelleMaximumDiameterList.add(tf.getText())
					);
		if(!theController.getChoiceBoxNacelleMaximumDiameterUnitList().isEmpty())
			theController.getChoiceBoxNacelleMaximumDiameterUnitList().stream()
			.filter(cb -> !cb.getSelectionModel().isEmpty())
			.forEach(
					cb -> nacelleMaximumDiameterUnitList.add(cb.getSelectionModel().getSelectedItem().toString())
					);
		if(!theController.getTextFieldNacelleKInletList().isEmpty())
			theController.getTextFieldNacelleKInletList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(
					tf -> nacelleKInletList.add(tf.getText())
					);
		if(!theController.getTextFieldNacelleKOutletList().isEmpty())
			theController.getTextFieldNacelleKOutletList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(
					tf -> nacelleKOutletList.add(tf.getText())
					);
		if(!theController.getTextFieldNacelleKLengthList().isEmpty())
			theController.getTextFieldNacelleKLengthList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(
					tf -> nacelleKLengthList.add(tf.getText())
					);
		if(!theController.getTextFieldNacelleKDiameterOutletList().isEmpty())
			theController.getTextFieldNacelleKDiameterOutletList().stream()
			.filter(tf -> !tf.getText().isEmpty())
			.forEach(
					tf -> nacelleKDiameterOutletList.add(tf.getText())
					);
		
		//.................................................................................................
		// FILTERING FILLED NACELLE TABS ...
		//.................................................................................................
		int numberOfFilledNacelleTabs = Arrays.asList(
				nacelleRoughnessList.size(),
				nacelleRoughnessUnitList.size(),
				nacelleLengthList.size(),
				nacelleLengthUnitList.size(),
				nacelleMaximumDiameterList.size(),
				nacelleMaximumDiameterUnitList.size(),
				nacelleKInletList.size(),
				nacelleKOutletList.size(),
				nacelleKLengthList.size(),
				nacelleKDiameterOutletList.size()
				).stream()
				.mapToInt(size -> size)
				.min()
				.getAsInt();
		
		if (theController.getTabPaneNacelles().getTabs().size() > numberOfFilledNacelleTabs) {
			
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					
					//..................................................................................
					// NACELLE UPDATE WARNING
					Stage nacelleUpdateWarning = new Stage();
					
					nacelleUpdateWarning.setTitle("New Nacelle Warning");
					nacelleUpdateWarning.initModality(Modality.WINDOW_MODAL);
					nacelleUpdateWarning.initStyle(StageStyle.UNDECORATED);
					nacelleUpdateWarning.initOwner(Main.getPrimaryStage());

					FXMLLoader loader = new FXMLLoader();
					loader.setLocation(Main.class.getResource("inputmanager/UpdateNacelleWarning.fxml"));
					BorderPane nacelleUpdateWarningBorderPane = null;
					try {
						nacelleUpdateWarningBorderPane = loader.load();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					Button continueButton = (Button) nacelleUpdateWarningBorderPane.lookup("#warningContinueButton");
					continueButton.setOnAction(new EventHandler<ActionEvent>() {
						
						@Override
						public void handle(ActionEvent arg0) {
							nacelleUpdateWarning.close();
						}
						
					});
					
					Scene scene = new Scene(nacelleUpdateWarningBorderPane);
					nacelleUpdateWarning.setScene(scene);
					nacelleUpdateWarning.sizeToScene();
					nacelleUpdateWarning.show();
					
				}
			});
			
			
		}
		
		//.................................................................................................
		// SETTING ALL DATA INSIDE THE AIRCRAFT OBJECT ...
		//.................................................................................................
		List<NacelleCreator> nacelleList = new ArrayList<>();
		for (int i=0; i<numberOfFilledNacelleTabs; i++) {
		
			nacelleList.add(
					new NacelleCreator(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i), 
							new INacelleCreator.Builder()
							.setId("Nacelle " + i + " - " + Main.getTheAircraft().getId())
							.setRoughness(
									(Amount<Length>) Amount.valueOf(
											Double.valueOf(nacelleRoughnessList.get(i)),
											Unit.valueOf(nacelleRoughnessUnitList.get(i))
											)
									)
							.setLength(
									(Amount<Length>) Amount.valueOf(
											Double.valueOf(nacelleLengthList.get(i)),
											Unit.valueOf(nacelleLengthUnitList.get(i))
											)
									)
							.setDiameterMax(
									(Amount<Length>) Amount.valueOf(
											Double.valueOf(nacelleMaximumDiameterList.get(i)),
											Unit.valueOf(nacelleMaximumDiameterUnitList.get(i))
											)
									)
							.setKInlet(Double.valueOf(nacelleKInletList.get(i)))
							.setKOutlet(Double.valueOf(nacelleKOutletList.get(i)))
							.setKLength(Double.valueOf(nacelleKLengthList.get(i)))
							.setKDiameterOutlet(Double.valueOf(nacelleKDiameterOutletList.get(i)))
							.build()
							)
					);
			
		}
		
		Main.getTheAircraft().getNacelles().getNacellesList().clear();
		Main.getTheAircraft().getNacelles().setNacellesList(nacelleList);
		
	}
	
	@SuppressWarnings("unchecked")
	public void updatePowerPlantTabData() {
		
		//.................................................................................................
		// DATA INITIALIZATION
		List<Integer> engineRadioButtonChoiceIndexList = new ArrayList<>();
		//.................................................................................................
		// TURBOFAN/TURBOJET
		//.................................................................................................
		List<String> enigneTypeTurbofanTurbojetList = new ArrayList<>();
		List<String> engineDatabasePathTurbofanTurbojetList = new ArrayList<>();
		List<String> engineLengthTurbofanTurbojetList = new ArrayList<>();
		List<String> engineLengthUnitTurbofanTurbojetList = new ArrayList<>();
		List<String> engineDryMassTurbofanTurbojetList = new ArrayList<>();
		List<String> engineDryMassUnitTurbofanTurbojetList = new ArrayList<>();
		List<String> engineStaticThrustTurbofanTurbojetList = new ArrayList<>();
		List<String> engineStaticThrustUnitTurbofanTurbojetList = new ArrayList<>();
		List<String> engineBPRTurbofanTurbojetList = new ArrayList<>();
		List<String> engineNumberOfCompressorStagesTurbofanTurbojetList = new ArrayList<>();
		List<String> engineNumberOfShaftsTurbofanTurbojetList = new ArrayList<>();
		List<String> engineOverallPressureRatioTurbofanTurbojetList = new ArrayList<>();
		//.................................................................................................
		// TURBOPROP
		//.................................................................................................
		List<String> enigneTypeTurbopropList = new ArrayList<>();
		List<String> engineDatabasePathTurbopropList = new ArrayList<>();
		List<String> engineLengthTurbopropList = new ArrayList<>();
		List<String> engineLengthUnitTurbopropList = new ArrayList<>();
		List<String> engineDryMassTurbopropList = new ArrayList<>();
		List<String> engineDryMassUnitTurbopropList = new ArrayList<>();
		List<String> engineStaticPowerTurbopropList = new ArrayList<>();
		List<String> engineStaticPowerUnitTurbopropList = new ArrayList<>();
		List<String> enginePropellerDiameterTurbopropList = new ArrayList<>();
		List<String> enginePropellerDiameterUnitTurbopropList = new ArrayList<>();
		List<String> engineNumberOfBladesTurbopropList = new ArrayList<>();
		List<String> enginePropellerEfficiencyTurbopropList = new ArrayList<>();
		List<String> engineNumberOfCompressorStagesTurbopropList = new ArrayList<>();
		List<String> engineNumberOfShaftsTurbopropList = new ArrayList<>();
		List<String> engineOverallPressureRatioTurbopropList = new ArrayList<>();
		//.................................................................................................
		// PISTON
		//.................................................................................................
		List<String> enigneTypePistonList = new ArrayList<>();
		List<String> engineDatabasePathPistonList = new ArrayList<>();
		List<String> engineLengthPistonList = new ArrayList<>();
		List<String> engineLengthUnitPistonList = new ArrayList<>();
		List<String> engineDryMassPistonList = new ArrayList<>();
		List<String> engineDryMassUnitPistonList = new ArrayList<>();
		List<String> engineStaticPowerPistonList = new ArrayList<>();
		List<String> engineStaticPowerUnitPistonList = new ArrayList<>();
		List<String> enginePropellerDiameterPistonList = new ArrayList<>();
		List<String> enginePropellerDiameterUnitPistonList = new ArrayList<>();
		List<String> engineNumberOfBladesPistonList = new ArrayList<>();
		List<String> enginePropellerEfficiencyPistonList = new ArrayList<>();
		
		//.................................................................................................
		// FETCHING DATA FROM GUI FIELDS ...
		//.................................................................................................
		for (int i=0; i<theController.getTabPaneEngines().getTabs().size(); i++) {

			if (theController.getPowerPlantToggleGropuList().get(i).getToggles().get(0) != null
							&& theController.getPowerPlantToggleGropuList().get(i).getToggles().get(1) != null
							&& theController.getPowerPlantToggleGropuList().get(i).getToggles().get(2) != null
							)
				if (theController.getPowerPlantToggleGropuList().get(i).getToggles().get(0).isSelected()
						|| theController.getPowerPlantToggleGropuList().get(i).getToggles().get(1).isSelected()
						|| theController.getPowerPlantToggleGropuList().get(i).getToggles().get(2).isSelected()
						) {
			
					engineRadioButtonChoiceIndexList.add(
							(Integer) theController.getPowerPlantToggleGropuList().get(i).getSelectedToggle().getUserData()
							);
				}
		}
		//.................................................................................................
		for(int i=0; i<engineRadioButtonChoiceIndexList.size(); i++) {

			if (engineRadioButtonChoiceIndexList.get(i) != null) {

				//........................................................................................
				// TURBOFAN - TURBOJET
				if (engineRadioButtonChoiceIndexList.get(i) == 0) {
					
					if(!theController.getEngineTurbojetTurbofanTypeChoiceBoxMap().isEmpty())
						if(!theController.getEngineTurbojetTurbofanTypeChoiceBoxMap().get(i).getSelectionModel().isEmpty())
							enigneTypeTurbofanTurbojetList.add(
									theController.getEngineTurbojetTurbofanTypeChoiceBoxMap().get(i)
									.getSelectionModel().getSelectedItem().toString()
									);
					
					if(!theController.getEngineTurbojetTurbofanDatabaseTextFieldMap().isEmpty())
						if(!theController.getEngineTurbojetTurbofanDatabaseTextFieldMap().get(i).getText().isEmpty())
							engineDatabasePathTurbofanTurbojetList.add(
									theController.getEngineTurbojetTurbofanDatabaseTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbojetTurbofanLengthTextFieldMap().isEmpty())
						if(!theController.getEngineTurbojetTurbofanLengthTextFieldMap().get(i).getText().isEmpty())
							engineLengthTurbofanTurbojetList.add(
									theController.getEngineTurbojetTurbofanLengthTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbojetTurbofanLengthUnitChoiceBoxMap().isEmpty())
						if(!theController.getEngineTurbojetTurbofanLengthUnitChoiceBoxMap().get(i).getSelectionModel().isEmpty())
							engineLengthUnitTurbofanTurbojetList.add(
									theController.getEngineTurbojetTurbofanLengthUnitChoiceBoxMap().get(i)
									.getSelectionModel().getSelectedItem().toString()
									);
					
					if(!theController.getEngineTurbojetTurbofanDryMassTextFieldMap().isEmpty())
						if(!theController.getEngineTurbojetTurbofanDryMassTextFieldMap().get(i).getText().isEmpty())
							engineDryMassTurbofanTurbojetList.add(
									theController.getEngineTurbojetTurbofanDryMassTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbojetTurbofanDryMassUnitChoiceBoxMap().isEmpty())
						if(!theController.getEngineTurbojetTurbofanDryMassUnitChoiceBoxMap().get(i).getSelectionModel().isEmpty())
							engineDryMassUnitTurbofanTurbojetList.add(
									theController.getEngineTurbojetTurbofanDryMassUnitChoiceBoxMap().get(i)
									.getSelectionModel().getSelectedItem().toString()
									);
					
					if(!theController.getEngineTurbojetTurbofanStaticThrustTextFieldMap().isEmpty())
						if(!theController.getEngineTurbojetTurbofanStaticThrustTextFieldMap().get(i).getText().isEmpty())
							engineStaticThrustTurbofanTurbojetList.add(
									theController.getEngineTurbojetTurbofanStaticThrustTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap().isEmpty())
						if(!theController.getEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap().get(i).getSelectionModel().isEmpty())
							engineStaticThrustUnitTurbofanTurbojetList.add(
									theController.getEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap().get(i)
									.getSelectionModel().getSelectedItem().toString()
									);
					
					if(!theController.getEngineTurbojetTurbofanBPRTextFieldMap().isEmpty())
						if(!theController.getEngineTurbojetTurbofanBPRTextFieldMap().get(i).getText().isEmpty())
							engineBPRTurbofanTurbojetList.add(
									theController.getEngineTurbojetTurbofanBPRTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap().isEmpty())
						if(!theController.getEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap().get(i).getText().isEmpty())
							engineNumberOfCompressorStagesTurbofanTurbojetList.add(
									theController.getEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbojetTurbofanNumberOfShaftsTextFieldMap().isEmpty())
						if(!theController.getEngineTurbojetTurbofanNumberOfShaftsTextFieldMap().get(i).getText().isEmpty())
							engineNumberOfShaftsTurbofanTurbojetList.add(
									theController.getEngineTurbojetTurbofanNumberOfShaftsTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbojetTurbofanOverallPressureRatioTextFieldMap().isEmpty())
						if(!theController.getEngineTurbojetTurbofanOverallPressureRatioTextFieldMap().get(i).getText().isEmpty())
							engineOverallPressureRatioTurbofanTurbojetList.add(
									theController.getEngineTurbojetTurbofanOverallPressureRatioTextFieldMap().get(i)
									.getText()
									);
				}
				//........................................................................................
				// TURBOPROP
				else if (engineRadioButtonChoiceIndexList.get(i) == 1) {

					if(!theController.getEngineTurbopropTypeChoiceBoxMap().isEmpty())
						if(!theController.getEngineTurbopropTypeChoiceBoxMap().get(i).getSelectionModel().isEmpty())
							enigneTypeTurbopropList.add(
									theController.getEngineTurbopropTypeChoiceBoxMap().get(i)
									.getSelectionModel().getSelectedItem().toString()
									);
					
					if(!theController.getEngineTurbopropDatabaseTextFieldMap().isEmpty())
						if(!theController.getEngineTurbopropDatabaseTextFieldMap().get(i).getText().isEmpty())
							engineDatabasePathTurbopropList.add(
									theController.getEngineTurbopropDatabaseTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbopropLengthTextFieldMap().isEmpty())
						if(!theController.getEngineTurbopropLengthTextFieldMap().get(i).getText().isEmpty())
							engineLengthTurbopropList.add(
									theController.getEngineTurbopropLengthTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbopropLengthUnitChoiceBoxMap().isEmpty())
						if(!theController.getEngineTurbopropLengthUnitChoiceBoxMap().get(i).getSelectionModel().isEmpty())
							engineLengthUnitTurbopropList.add(
									theController.getEngineTurbopropLengthUnitChoiceBoxMap().get(i)
									.getSelectionModel().getSelectedItem().toString()
									);
					
					if(!theController.getEngineTurbopropDryMassTextFieldMap().isEmpty())
						if(!theController.getEngineTurbopropDryMassTextFieldMap().get(i).getText().isEmpty())
							engineDryMassTurbopropList.add(
									theController.getEngineTurbopropDryMassTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbopropDryMassUnitChoiceBoxMap().isEmpty())
						if(!theController.getEngineTurbopropDryMassUnitChoiceBoxMap().get(i).getSelectionModel().isEmpty())
							engineDryMassUnitTurbopropList.add(
									theController.getEngineTurbopropDryMassUnitChoiceBoxMap().get(i)
									.getSelectionModel().getSelectedItem().toString()
									);
					
					if(!theController.getEngineTurbopropStaticPowerTextFieldMap().isEmpty())
						if(!theController.getEngineTurbopropStaticPowerTextFieldMap().get(i).getText().isEmpty())
							engineStaticPowerTurbopropList.add(
									theController.getEngineTurbopropStaticPowerTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbopropStaticPowerUnitChoiceBoxMap().isEmpty())
						if(!theController.getEngineTurbopropStaticPowerUnitChoiceBoxMap().get(i).getSelectionModel().isEmpty())
							engineStaticPowerUnitTurbopropList.add(
									theController.getEngineTurbopropStaticPowerUnitChoiceBoxMap().get(i)
									.getSelectionModel().getSelectedItem().toString()
									);
					
					if(!theController.getEngineTurbopropPropellerDiameterTextFieldMap().isEmpty())
						if(!theController.getEngineTurbopropPropellerDiameterTextFieldMap().get(i).getText().isEmpty())
							enginePropellerDiameterTurbopropList.add(
									theController.getEngineTurbopropPropellerDiameterTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbopropPropellerDiameterUnitChoiceBoxMap().isEmpty())
						if(!theController.getEngineTurbopropPropellerDiameterUnitChoiceBoxMap().get(i).getSelectionModel().isEmpty())
							enginePropellerDiameterUnitTurbopropList.add(
									theController.getEngineTurbopropPropellerDiameterUnitChoiceBoxMap().get(i)
									.getSelectionModel().getSelectedItem().toString()
									);
					
					if(!theController.getEngineTurbopropNumberOfBladesTextFieldMap().isEmpty())
						if(!theController.getEngineTurbopropNumberOfBladesTextFieldMap().get(i).getText().isEmpty())
							engineNumberOfBladesTurbopropList.add(
									theController.getEngineTurbopropNumberOfBladesTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbopropPropellerEfficiencyTextFieldMap().isEmpty())
						if(!theController.getEngineTurbopropPropellerEfficiencyTextFieldMap().get(i).getText().isEmpty())
							enginePropellerEfficiencyTurbopropList.add(
									theController.getEngineTurbopropPropellerEfficiencyTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbopropNumberOfCompressorStagesTextFieldMap().isEmpty())
						if(!theController.getEngineTurbopropNumberOfCompressorStagesTextFieldMap().get(i).getText().isEmpty())
							engineNumberOfCompressorStagesTurbopropList.add(
									theController.getEngineTurbopropNumberOfCompressorStagesTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbopropNumberOfShaftsTextFieldMap().isEmpty())
						if(!theController.getEngineTurbopropNumberOfShaftsTextFieldMap().get(i).getText().isEmpty())
							engineNumberOfShaftsTurbopropList.add(
									theController.getEngineTurbopropNumberOfShaftsTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEngineTurbopropOverallPressureRatioTextFieldMap().isEmpty())
						if(!theController.getEngineTurbopropOverallPressureRatioTextFieldMap().get(i).getText().isEmpty())
							engineOverallPressureRatioTurbopropList.add(
									theController.getEngineTurbopropOverallPressureRatioTextFieldMap().get(i)
									.getText()
									);
				}
				//........................................................................................
				// PISTON
				else if (engineRadioButtonChoiceIndexList.get(i) == 2) {

					if(!theController.getEnginePistonTypeChoiceBoxMap().isEmpty())
						if(!theController.getEnginePistonTypeChoiceBoxMap().get(i).getSelectionModel().isEmpty())
							enigneTypePistonList.add(
									theController.getEnginePistonTypeChoiceBoxMap().get(i)
									.getSelectionModel().getSelectedItem().toString()
									);
					
					if(!theController.getEnginePistonDatabaseTextFieldMap().isEmpty())
						if(!theController.getEnginePistonDatabaseTextFieldMap().get(i).getText().isEmpty())
							engineDatabasePathPistonList.add(
									theController.getEnginePistonDatabaseTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEnginePistonLengthTextFieldMap().isEmpty())
						if(!theController.getEnginePistonLengthTextFieldMap().get(i).getText().isEmpty())
							engineLengthPistonList.add(
									theController.getEnginePistonLengthTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEnginePistonLengthUnitChoiceBoxMap().isEmpty())
						if(!theController.getEnginePistonLengthUnitChoiceBoxMap().get(i).getSelectionModel().isEmpty())
							engineLengthUnitPistonList.add(
									theController.getEnginePistonLengthUnitChoiceBoxMap().get(i)
									.getSelectionModel().getSelectedItem().toString()
									);
					
					if(!theController.getEnginePistonDryMassTextFieldMap().isEmpty())
						if(!theController.getEnginePistonDryMassTextFieldMap().get(i).getText().isEmpty())
							engineDryMassPistonList.add(
									theController.getEnginePistonDryMassTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEnginePistonDryMassUnitChoiceBoxMap().isEmpty())
						if(!theController.getEnginePistonDryMassUnitChoiceBoxMap().get(i).getSelectionModel().isEmpty())
							engineDryMassUnitPistonList.add(
									theController.getEnginePistonDryMassUnitChoiceBoxMap().get(i)
									.getSelectionModel().getSelectedItem().toString()
									);
					
					if(!theController.getEnginePistonStaticPowerTextFieldMap().isEmpty())
						if(!theController.getEnginePistonStaticPowerTextFieldMap().get(i).getText().isEmpty())
							engineStaticPowerPistonList.add(
									theController.getEnginePistonStaticPowerTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEnginePistonStaticPowerUnitChoiceBoxMap().isEmpty())
						if(!theController.getEnginePistonStaticPowerUnitChoiceBoxMap().get(i).getSelectionModel().isEmpty())
							engineStaticPowerUnitPistonList.add(
									theController.getEnginePistonStaticPowerUnitChoiceBoxMap().get(i)
									.getSelectionModel().getSelectedItem().toString()
									);
					
					if(!theController.getEnginePistonPropellerDiameterTextFieldMap().isEmpty())
						if(!theController.getEnginePistonPropellerDiameterTextFieldMap().get(i).getText().isEmpty())
							enginePropellerDiameterPistonList.add(
									theController.getEnginePistonPropellerDiameterTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEnginePistonPropellerDiameterUnitChoiceBoxMap().isEmpty())
						if(!theController.getEnginePistonPropellerDiameterUnitChoiceBoxMap().get(i).getSelectionModel().isEmpty())
							enginePropellerDiameterUnitPistonList.add(
									theController.getEnginePistonPropellerDiameterUnitChoiceBoxMap().get(i)
									.getSelectionModel().getSelectedItem().toString()
									);
					
					if(!theController.getEnginePistonNumberOfBladesTextFieldMap().isEmpty())
						if(!theController.getEnginePistonNumberOfBladesTextFieldMap().get(i).getText().isEmpty())
							engineNumberOfBladesPistonList.add(
									theController.getEnginePistonNumberOfBladesTextFieldMap().get(i)
									.getText()
									);
					
					if(!theController.getEnginePistonPropellerEfficiencyTextFieldMap().isEmpty())
						if(!theController.getEnginePistonPropellerEfficiencyTextFieldMap().get(i).getText().isEmpty())
							enginePropellerEfficiencyPistonList.add(
									theController.getEnginePistonPropellerEfficiencyTextFieldMap().get(i)
									.getText()
									);
					
				}
			}
		}
		
		//.................................................................................................
		// FILTERING FILLED ENGINES TABS ...
		//.................................................................................................
		// TURBOFAN - TURBOJET
		//........................................................................................
		int numberOfFilledTurbofanTurbojetEngineTabs = Arrays.asList(
				enigneTypeTurbofanTurbojetList.size(),
				engineDatabasePathTurbofanTurbojetList.size(),
				engineLengthTurbofanTurbojetList.size(),
				engineLengthUnitTurbofanTurbojetList.size(),
				engineDryMassTurbofanTurbojetList.size(),
				engineDryMassUnitTurbofanTurbojetList.size(),
				engineStaticThrustTurbofanTurbojetList.size(),
				engineStaticThrustUnitTurbofanTurbojetList.size(),
				engineBPRTurbofanTurbojetList.size(),
				engineNumberOfCompressorStagesTurbofanTurbojetList.size(),
				engineNumberOfShaftsTurbofanTurbojetList.size(),
				engineOverallPressureRatioTurbofanTurbojetList.size()
				).stream()
				.mapToInt(size -> size)
				.min()
				.getAsInt();
		
		//.................................................................................................
		// TURBOPROP
		//.................................................................................................
		int numberOfFilledTurbopropEngineTabs = Arrays.asList(
				enigneTypeTurbopropList.size(),
				engineDatabasePathTurbopropList.size(),
				engineLengthTurbopropList.size(),
				engineLengthUnitTurbopropList.size(),
				engineDryMassTurbopropList.size(),
				engineDryMassUnitTurbopropList.size(),
				engineStaticPowerTurbopropList.size(),
				engineStaticPowerUnitTurbopropList.size(),
				enginePropellerDiameterTurbopropList.size(),
				enginePropellerDiameterUnitTurbopropList.size(),
				engineNumberOfBladesTurbopropList.size(),
				enginePropellerEfficiencyTurbopropList.size(),
				engineNumberOfCompressorStagesTurbopropList.size(),
				engineNumberOfShaftsTurbopropList.size(),
				engineOverallPressureRatioTurbopropList.size()
				).stream()
				.mapToInt(size -> size)
				.min()
				.getAsInt();
		
		//.................................................................................................
		// PISTON
		//.................................................................................................
		int numberOfFilledPistonEngineTabs = Arrays.asList(
				enigneTypePistonList.size(),
				engineDatabasePathPistonList.size(),
				engineLengthPistonList.size(),
				engineLengthUnitPistonList.size(),
				engineDryMassPistonList.size(),
				engineDryMassUnitPistonList.size(),
				engineStaticPowerPistonList.size(),
				engineStaticPowerUnitPistonList.size(),
				enginePropellerDiameterPistonList.size(),
				enginePropellerDiameterUnitPistonList.size(),
				engineNumberOfBladesPistonList.size(),
				enginePropellerEfficiencyPistonList.size()
				).stream()
				.mapToInt(size -> size)
				.min()
				.getAsInt();
		
		//.................................................................................................
		// SETTING ALL DATA INSIDE THE AIRCRAFT OBJECT ...
		//.................................................................................................
		List<Engine> engineList = new ArrayList<>();

		//........................................................................................
		// TURBOFAN - TURBOJET
		//........................................................................................
		for (int i=0; i<numberOfFilledTurbofanTurbojetEngineTabs; i++) {

			engineList.add(
					new Engine(
							new IEngine.Builder()
							.setId("Engine " + i + " - " + Main.getTheAircraft().getId())
							.setEngineType(EngineTypeEnum.valueOf(enigneTypeTurbofanTurbojetList.get(i)))
							.setEngineDatabaseName(engineDatabasePathTurbofanTurbojetList.get(i))
							.setLength(
									(Amount<Length>) Amount.valueOf(
											Double.valueOf(engineLengthTurbofanTurbojetList.get(i)),
											Unit.valueOf(engineLengthUnitTurbofanTurbojetList.get(i))											
											)
									)
							.setStaticThrust(
									(Amount<Force>) Amount.valueOf(
											Double.valueOf(engineStaticThrustTurbofanTurbojetList.get(i)),
											Unit.valueOf(engineStaticThrustUnitTurbofanTurbojetList.get(i))											
											)
									)
							.setBpr(Double.valueOf(engineBPRTurbofanTurbojetList.get(i)))
							.setDryMassPublicDomain(
									(Amount<Mass>) Amount.valueOf(
											Double.valueOf(engineDryMassTurbofanTurbojetList.get(i)),
											Unit.valueOf(engineDryMassUnitTurbofanTurbojetList.get(i))											
											)
									)
							.setNumberOfCompressorStages(
									Integer.valueOf(engineNumberOfCompressorStagesTurbofanTurbojetList.get(i))
									)
							.setNumberOfShafts(
									Integer.valueOf(engineNumberOfShaftsTurbofanTurbojetList.get(i))
									)
							.setOverallPressureRatio(
									Double.valueOf(engineOverallPressureRatioTurbofanTurbojetList.get(i))
									)
							.build()
							)
					);

		}

		//........................................................................................
		// TURBOPROP
		//........................................................................................
		for (int i=0; i<numberOfFilledTurbopropEngineTabs; i++) {

			engineList.add(
					new Engine(
							new IEngine.Builder()
							.setId("Engine " + i	+ " - " + Main.getTheAircraft().getId()) 
							.setEngineType(EngineTypeEnum.valueOf(enigneTypeTurbopropList.get(i)))
							.setEngineDatabaseName(engineDatabasePathTurbopropList.get(i))
							.setLength(
									(Amount<Length>) Amount.valueOf(
											Double.valueOf(engineLengthTurbopropList.get(i)),
											Unit.valueOf(engineLengthUnitTurbopropList.get(i))											
											)
									)
							.setPropellerDiameter(
									(Amount<Length>) Amount.valueOf(
											Double.valueOf(enginePropellerDiameterTurbopropList.get(i)),
											Unit.valueOf(enginePropellerDiameterUnitTurbopropList.get(i))											
											)
									)
							.setNumberOfBlades(Integer.valueOf(engineNumberOfBladesTurbopropList.get(i)))
							.setEtaPropeller(Double.valueOf(enginePropellerEfficiencyTurbopropList.get(i)))
							.setStaticPower(
									(Amount<Power>) Amount.valueOf(
											Double.valueOf(engineStaticPowerTurbopropList.get(i)),
											Unit.valueOf(engineStaticPowerUnitTurbopropList.get(i))											
											)
									)
							.setDryMassPublicDomain(
									(Amount<Mass>) Amount.valueOf(
											Double.valueOf(engineDryMassTurbopropList.get(i)),
											Unit.valueOf(engineDryMassUnitTurbopropList.get(i))											
											)
									)
							.setNumberOfCompressorStages(
									Integer.valueOf(engineNumberOfCompressorStagesTurbopropList.get(i))
									)
							.setNumberOfShafts(
									Integer.valueOf(engineNumberOfShaftsTurbopropList.get(i))
									)
							.setOverallPressureRatio(
									Double.valueOf(engineOverallPressureRatioTurbopropList.get(i))
									)
							.build()
							)
					);
			
		}
		
		//........................................................................................
		// PISTON
		//........................................................................................
		for (int i=0; i<numberOfFilledPistonEngineTabs; i++) {
			
			engineList.add(
					new Engine(
							new IEngine.Builder()
							.setId("Engine " + i	+ " - " + Main.getTheAircraft().getId())
							.setEngineType(EngineTypeEnum.valueOf(enigneTypePistonList.get(i)))
							.setEngineDatabaseName(engineDatabasePathPistonList.get(i))
							.setLength(
									(Amount<Length>) Amount.valueOf(
											Double.valueOf(engineLengthPistonList.get(i)),
											Unit.valueOf(engineLengthUnitPistonList.get(i))											
											)
									)
							.setPropellerDiameter(
									(Amount<Length>) Amount.valueOf(
											Double.valueOf(enginePropellerDiameterPistonList.get(i)),
											Unit.valueOf(enginePropellerDiameterUnitPistonList.get(i))											
											)
									)
							.setNumberOfBlades(Integer.valueOf(engineNumberOfBladesPistonList.get(i)))
							.setEtaPropeller(Double.valueOf(enginePropellerEfficiencyPistonList.get(i)))
							.setStaticPower(
									(Amount<Power>) Amount.valueOf(
											Double.valueOf(engineStaticPowerPistonList.get(i)),
											Unit.valueOf(engineStaticPowerUnitPistonList.get(i))											
											)
									)
							.setDryMassPublicDomain(
									(Amount<Mass>) Amount.valueOf(
											Double.valueOf(engineDryMassPistonList.get(i)),
											Unit.valueOf(engineDryMassUnitPistonList.get(i))											
											)
									)
							.build()
							)
					);
			
		}
		
		Main.getTheAircraft().getPowerPlant().getEngineList().clear();
		Main.getTheAircraft().getPowerPlant().setEngineList(engineList);
		
		//..................................................................................
		// ENGINE UPDATE WARNING
		if (theController.getTabPaneEngines().getTabs().size() 
				> (numberOfFilledTurbofanTurbojetEngineTabs 
				+ numberOfFilledTurbopropEngineTabs 
				+ numberOfFilledPistonEngineTabs)
				) {
			
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					
					Stage engineUpdateWarning = new Stage();
					
					engineUpdateWarning.setTitle("New Engine Warning");
					engineUpdateWarning.initModality(Modality.WINDOW_MODAL);
					engineUpdateWarning.initStyle(StageStyle.UNDECORATED);
					engineUpdateWarning.initOwner(Main.getPrimaryStage());

					FXMLLoader loader = new FXMLLoader();
					loader.setLocation(Main.class.getResource("inputmanager/UpdateEngineWarning.fxml"));
					BorderPane engineUpdateWarningBorderPane = null;
					try {
						engineUpdateWarningBorderPane = loader.load();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					Button continueButton = (Button) engineUpdateWarningBorderPane.lookup("#warningContinueButton");
					continueButton.setOnAction(new EventHandler<ActionEvent>() {
						
						@Override
						public void handle(ActionEvent arg0) {
							engineUpdateWarning.close();
						}
						
					});
					
					Scene scene = new Scene(engineUpdateWarningBorderPane);
					engineUpdateWarning.setScene(scene);
					engineUpdateWarning.sizeToScene();
					engineUpdateWarning.show();
					
				}
			});
		}
	}
	
	@SuppressWarnings("unchecked")
	public void updateLandingGearsTabData() {
		
		//.................................................................................................
		// DATA INITIALIZATION
		//.................................................................................................
		String landingGearsMainLegLength = "";
		String landingGearsMainLegLengthUnit = "";
		String landingGearsDistanceBetweenWheels = "";
		String landingGearsDistanceBetweenWheelsUnit = "";
		String landingGearsNumberOfFrontalWheels = "";
		String landingGearsNumberOfRearWheels = "";
		//.................................................................................................
		String landingGearsFrontalWheelsHeigth = "";
		String landingGearsFrontalWheelsHeigthUnit = "";
		String landingGearsFrontalWheelsWidth= "";
		String landingGearsFrontalWheelsWidthUnit = "";
		//.................................................................................................
		String landingGearsRearWheelsHeigth = "";
		String landingGearsRearWheelsHeigthUnit = "";
		String landingGearsRearWheelsWidth= "";
		String landingGearsRearWheelsWidthUnit = "";
		
		//.................................................................................................
		// FETCHING DATA FROM GUI FIELDS ...
		//.................................................................................................
		if(theController.getTextFieldLandingGearsMainLegLength().getText() != null)
			landingGearsMainLegLength = theController.getTextFieldLandingGearsMainLegLength().getText();
		if(!theController.getLandingGearsMainLegLengthUnitChoiceBox().getSelectionModel().isEmpty())
			landingGearsMainLegLengthUnit = theController.getLandingGearsMainLegLengthUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldLandingGearsDistanceBetweenWheels().getText() != null)
			landingGearsDistanceBetweenWheels = theController.getTextFieldLandingGearsDistanceBetweenWheels().getText();
		if(!theController.getLandingGearsDistanceBetweenWheelsUnitChoiceBox().getSelectionModel().isEmpty())
			landingGearsDistanceBetweenWheelsUnit = theController.getLandingGearsDistanceBetweenWheelsUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldLandingGearsNumberOfFrontalWheels().getText() != null)
			landingGearsNumberOfFrontalWheels = theController.getTextFieldLandingGearsNumberOfFrontalWheels().getText();
		if(theController.getTextFieldLandingGearsNumberOfRearWheels().getText() != null)
			landingGearsNumberOfRearWheels = theController.getTextFieldLandingGearsNumberOfRearWheels().getText();
		//.................................................................................................
		if(theController.getTextFieldLandingGearsFrontalWheelsHeight().getText() != null)
			landingGearsFrontalWheelsHeigth = theController.getTextFieldLandingGearsFrontalWheelsHeight().getText();
		if(!theController.getLandingGearsFrontalWheelsHeigthUnitChoiceBox().getSelectionModel().isEmpty())
			landingGearsFrontalWheelsHeigthUnit = theController.getLandingGearsFrontalWheelsHeigthUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldLandingGearsFrontalWheelsWidth().getText() != null)
			landingGearsFrontalWheelsWidth = theController.getTextFieldLandingGearsFrontalWheelsWidth().getText();
		if(!theController.getLandingGearsFrontalWheelsWidthUnitChoiceBox().getSelectionModel().isEmpty())
			landingGearsFrontalWheelsWidthUnit = theController.getLandingGearsFrontalWheelsWidthUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		//.................................................................................................
		if(theController.getTextFieldLandingGearsRearWheelsHeight().getText() != null)
			landingGearsRearWheelsHeigth = theController.getTextFieldLandingGearsRearWheelsHeight().getText();
		if(!theController.getLandingGearsRearWheelsHeigthUnitChoiceBox().getSelectionModel().isEmpty())
			landingGearsRearWheelsHeigthUnit = theController.getLandingGearsRearWheelsHeigthUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		if(theController.getTextFieldLandingGearsRearWheelsWidth().getText() != null)
			landingGearsRearWheelsWidth = theController.getTextFieldLandingGearsRearWheelsWidth().getText();
		if(!theController.getLandingGearsRearWheelsWidthUnitChoiceBox().getSelectionModel().isEmpty())
			landingGearsRearWheelsWidthUnit = theController.getLandingGearsRearWheelsWidthUnitChoiceBox().getSelectionModel().getSelectedItem().toString();
		
		//.................................................................................................
		// SETTING ALL DATA INSIDE THE AIRCRAFT OBJECT ...
		//.................................................................................................
		ILandingGear.Builder.from(Main.getTheAircraft().getLandingGears().getTheLandingGearsInterface())
		.setId("Landing Gears - " + Main.getTheAircraft().getId())
		.setMainLegsLenght(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(landingGearsMainLegLength),
						Unit.valueOf(landingGearsMainLegLengthUnit)
						)
				)
		.setDistanceBetweenWheels(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(landingGearsDistanceBetweenWheels),
						Unit.valueOf(landingGearsDistanceBetweenWheelsUnit)
						)
				)
		.setNumberOfFrontalWheels(Integer.valueOf(landingGearsNumberOfFrontalWheels))
		.setNumberOfFrontalWheels(Integer.valueOf(landingGearsNumberOfRearWheels))
		.setFrontalWheelsHeight(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(landingGearsFrontalWheelsHeigth),
						Unit.valueOf(landingGearsFrontalWheelsHeigthUnit)
						)
				)
		.setFrontalWheelsWidth(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(landingGearsFrontalWheelsWidth),
						Unit.valueOf(landingGearsFrontalWheelsWidthUnit)
						)
				)
		.setRearWheelsHeight(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(landingGearsRearWheelsHeigth),
						Unit.valueOf(landingGearsRearWheelsHeigthUnit)
						)
				)
		.setRearWheelsWidth(
				(Amount<Length>) Amount.valueOf(
						Double.valueOf(landingGearsRearWheelsWidth),
						Unit.valueOf(landingGearsRearWheelsWidthUnit)
						)
				)
		.build();

	}

	@SuppressWarnings("unchecked")
	public void createAircraftObjectFromData() {
		
		//....................................................................................
		// DATA INITIALIZATION
		//....................................................................................
		File cabinConfigurationFilePath = null;
		File fuselageFilePath = null;
		File wingFilePath = null;
		File hTailFilePath = null;
		File vTailFilePath = null;
		File canardFilePath = null;
		List<File> powerPlantFilePathList = new ArrayList<>();
		List<File> nacellesFilePathList = new ArrayList<>();
		File landingGearsFilePath = null;
		
		FusDesDatabaseReader fusDesDatabaseReader = Main.getTheAircraft().getFuselage().getFusDesDatabaseReader();
		AerodynamicDatabaseReader aerodynamicDatabaseReader = Main.getTheAircraft().getWing().getAeroDatabaseReader();
		HighLiftDatabaseReader highLiftDatabaseReader = Main.getTheAircraft().getWing().getHighLiftDatabaseReader();
		VeDSCDatabaseReader veDSCDatabaseReader = Main.getTheAircraft().getWing().getVeDSCDatabaseReader();
		
		if(!theController.getTextFieldAircraftCabinConfigurationFile().getText().isEmpty()) 
			cabinConfigurationFilePath = new File(theController.getTextFieldAircraftCabinConfigurationFile().getText());
		if(!theController.getTextFieldAircraftFuselageFile().getText().isEmpty()) 
			fuselageFilePath = new File(theController.getTextFieldAircraftFuselageFile().getText());
		if(!theController.getTextFieldAircraftWingFile().getText().isEmpty()) 
			wingFilePath = new File(theController.getTextFieldAircraftWingFile().getText());
		if(!theController.getTextFieldAircraftHTailFile().getText().isEmpty()) 
			hTailFilePath = new File(theController.getTextFieldAircraftHTailFile().getText());
		if(!theController.getTextFieldAircraftVTailFile().getText().isEmpty()) 
			vTailFilePath = new File(theController.getTextFieldAircraftVTailFile().getText());
		if(!theController.getTextFieldAircraftCanardFile().getText().isEmpty()) 
			canardFilePath = new File(theController.getTextFieldAircraftCanardFile().getText());
		
		theController.getTextFieldsAircraftEngineFileList().stream()
		.filter(tf -> !tf.getText().isEmpty())
		.forEach(tf -> powerPlantFilePathList.add(new File(tf.getText())));
		
		theController.getTextFieldsAircraftNacelleFileList().stream()
		.filter(tf -> !tf.getText().isEmpty())
		.forEach(tf -> nacellesFilePathList.add(new File(tf.getText())));
		
		if(!theController.getTextFieldAircraftLandingGearsFile().getText().isEmpty()) 
			landingGearsFilePath = new File(theController.getTextFieldAircraftLandingGearsFile().getText());
		
		//....................................................................................
		// CREATING AIRCRAFT COMPONENTS FROM FILE ...
		//....................................................................................
		// CABIN CONFIGURATION
		if (theController.isUpdateCabinConfigurationDataFromFile() == true)
			if (cabinConfigurationFilePath.exists())
				Main.getTheAircraft().setCabinConfiguration(
						CabinConfiguration.importFromXML(cabinConfigurationFilePath.getAbsolutePath())
						);
		//....................................................................................
		// FUSELAGE
		if (theController.isUpdateFuselageDataFromFile() == true) {
			if (fuselageFilePath.exists()) {

				Amount<Length> fuselageXApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getFuselageXPositionValue()), 
						Unit.valueOf(theController.getFuselageXPositionUnit())
						);
				Amount<Length> fuselageYApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getFuselageYPositionValue()), 
						Unit.valueOf(theController.getFuselageYPositionUnit())
						);
				Amount<Length> fuselageZApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getFuselageZPositionValue()),
						Unit.valueOf(theController.getFuselageZPositionUnit())
						);

				Main.getTheAircraft().setFuselage(Fuselage.importFromXML(fuselageFilePath.getAbsolutePath())
								);
				Main.getTheAircraft().getFuselage().calculateGeometry();
				Main.getTheAircraft().getFuselage().setFusDesDatabaseReader(fusDesDatabaseReader);
				Main.getTheAircraft().getFuselage().setXApexConstructionAxes(fuselageXApex);
				Main.getTheAircraft().getFuselage().setYApexConstructionAxes(fuselageYApex);
				Main.getTheAircraft().getFuselage().setZApexConstructionAxes(fuselageZApex);
			}
		}
		//....................................................................................
		// WING
		if (theController.isUpdateWingDataFromFile() == true) {
			if (wingFilePath.exists()) {

				Amount<Length> wingXApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getWingXPositionValue()),
						Unit.valueOf(theController.getWingXPositionUnit())
						);
				Amount<Length> wingYApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getWingYPositionValue()), 
						Unit.valueOf(theController.getWingYPositionUnit())
						);
				Amount<Length> wingZApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getWingZPositionValue()), 
						Unit.valueOf(theController.getWingZPositionUnit())
						);
				Amount<Angle> wingRiggingAngle = (Amount<Angle>) Amount.valueOf(
						Double.valueOf(theController.getWingRiggingAngleValue()),
						Unit.valueOf(theController.getWingRiggingAngleUnit())
						);

				Main.getTheAircraft().setWing(
						LiftingSurface.importFromXML(
								ComponentEnum.WING,
								wingFilePath.getAbsolutePath(),
								Main.getInputDirectoryPath() + File.separator
								+ "Template_Aircraft" + File.separator
								+ "lifting_surfaces" + File.separator
								+ "airfoils" + File.separator
								)
						);
				Main.getTheAircraft().getWing().setAeroDatabaseReader(aerodynamicDatabaseReader);
				Main.getTheAircraft().getWing().setHighLiftDatabaseReader(highLiftDatabaseReader);
				Main.getTheAircraft().getWing().setVeDSCDatabaseReader(veDSCDatabaseReader);
				Main.getTheAircraft().getWing().calculateGeometry(
						ComponentEnum.WING, 
						true
						);
				Main.getTheAircraft().getWing().populateAirfoilList(
						false
						);
				Main.getTheAircraft().getWing().setXApexConstructionAxes(wingXApex);
				Main.getTheAircraft().getWing().setYApexConstructionAxes(wingYApex);
				Main.getTheAircraft().getWing().setZApexConstructionAxes(wingZApex);
				Main.getTheAircraft().getWing().setRiggingAngle(wingRiggingAngle);

				Main.getTheAircraft().setFuelTank(
						new FuelTank(
								"Fuel Tank - " + Main.getTheAircraft().getId(),
								Main.getTheAircraft().getWing()
								)
						);
				Main.getTheAircraft().getFuelTank().setXApexConstructionAxes(
						Main.getTheAircraft().getWing().getXApexConstructionAxes()
						.plus(Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot()
								.times(Main.getTheAircraft().getWing().getMainSparDimensionlessPosition()
										)
								)
						);
				Main.getTheAircraft().getFuelTank().setYApexConstructionAxes(
						Main.getTheAircraft().getWing().getYApexConstructionAxes()
						);
				Main.getTheAircraft().getFuelTank().setZApexConstructionAxes(
						Main.getTheAircraft().getWing().getZApexConstructionAxes()
						);

			}
		}
		//....................................................................................
		// HORIZONTAL TAIL
		if (theController.isUpdateHTailDataFromFile() == true) {
			if (hTailFilePath.exists()) {

				Amount<Length> hTailXApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.gethTailXPositionValue()),
						Unit.valueOf(theController.gethTailXPositionUnit())
						);
				Amount<Length> hTailYApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.gethTailYPositionValue()), 
						Unit.valueOf(theController.gethTailYPositionUnit())
						);
				Amount<Length> hTailZApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.gethTailZPositionValue()), 
						Unit.valueOf(theController.gethTailZPositionUnit())
						);
				Amount<Angle> hTailRiggingAngle = (Amount<Angle>) Amount.valueOf(
						Double.valueOf(theController.gethTailRiggingAngleValue()),
						Unit.valueOf(theController.gethTailRiggingAngleUnit())
						);

				Main.getTheAircraft().setHTail(
						LiftingSurface.importFromXML(
								ComponentEnum.HORIZONTAL_TAIL,
								hTailFilePath.getAbsolutePath(),
								Main.getInputDirectoryPath() + File.separator
								+ "Template_Aircraft" + File.separator
								+ "lifting_surfaces" + File.separator
								+ "airfoils" + File.separator
								)
						);
				Main.getTheAircraft().getHTail().setAeroDatabaseReader(aerodynamicDatabaseReader);
				Main.getTheAircraft().getHTail().setHighLiftDatabaseReader(highLiftDatabaseReader);
				Main.getTheAircraft().getHTail().setVeDSCDatabaseReader(veDSCDatabaseReader);
				Main.getTheAircraft().getHTail().calculateGeometry(
						ComponentEnum.HORIZONTAL_TAIL, 
						true
						);
				Main.getTheAircraft().getHTail().populateAirfoilList(
						false
						);
				Main.getTheAircraft().getHTail().setXApexConstructionAxes(hTailXApex);
				Main.getTheAircraft().getHTail().setYApexConstructionAxes(hTailYApex);
				Main.getTheAircraft().getHTail().setZApexConstructionAxes(hTailZApex);
				Main.getTheAircraft().getHTail().setRiggingAngle(hTailRiggingAngle);
			}
		}
		//....................................................................................
		// VERTICAL TAIL
		if (theController.isUpdateVTailDataFromFile() == true) {
			if (vTailFilePath.exists()) {

				Amount<Length> vTailXApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getvTailXPositionValue()),
						Unit.valueOf(theController.getvTailXPositionUnit())
						);
				Amount<Length> vTailYApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getvTailYPositionValue()), 
						Unit.valueOf(theController.getvTailYPositionUnit())
						);
				Amount<Length> vTailZApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getvTailZPositionValue()), 
						Unit.valueOf(theController.getvTailZPositionUnit())
						);
				Amount<Angle> vTailRiggingAngle = (Amount<Angle>) Amount.valueOf(
						Double.valueOf(theController.getvTailRiggingAngleValue()),
						Unit.valueOf(theController.getvTailRiggingAngleUnit())
						);

				Main.getTheAircraft().setVTail(
						LiftingSurface.importFromXML(
								ComponentEnum.VERTICAL_TAIL,
								vTailFilePath.getAbsolutePath(),
								Main.getInputDirectoryPath() + File.separator
								+ "Template_Aircraft" + File.separator
								+ "lifting_surfaces" + File.separator
								+ "airfoils" + File.separator
								)
						);
				Main.getTheAircraft().getVTail().setAeroDatabaseReader(aerodynamicDatabaseReader);
				Main.getTheAircraft().getVTail().setHighLiftDatabaseReader(highLiftDatabaseReader);
				Main.getTheAircraft().getVTail().setVeDSCDatabaseReader(veDSCDatabaseReader);
				Main.getTheAircraft().getVTail().calculateGeometry(
						ComponentEnum.VERTICAL_TAIL, 
						false
						);
				Main.getTheAircraft().getVTail().populateAirfoilList(
						false
						);
				Main.getTheAircraft().getVTail().setXApexConstructionAxes(vTailXApex);
				Main.getTheAircraft().getVTail().setYApexConstructionAxes(vTailYApex);
				Main.getTheAircraft().getVTail().setZApexConstructionAxes(vTailZApex);
				Main.getTheAircraft().getVTail().setRiggingAngle(vTailRiggingAngle);
			}
		}
		//....................................................................................
		// CANARD
		if (theController.isUpdateCanardDataFromFile() == true) {
			if (canardFilePath.exists()) {

				Amount<Length> canardXApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getCanardXPositionValue()),
						Unit.valueOf(theController.getCanardXPositionUnit())
						);
				Amount<Length> canardYApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getCanardYPositionValue()), 
						Unit.valueOf(theController.getCanardYPositionUnit())
						);
				Amount<Length> canardZApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getCanardZPositionValue()), 
						Unit.valueOf(theController.getCanardZPositionUnit())
						);
				Amount<Angle> canardRiggingAngle = (Amount<Angle>) Amount.valueOf(
						Double.valueOf(theController.getCanardRiggingAngleValue()),
						Unit.valueOf(theController.getCanardRiggingAngleUnit())
						);

				Main.getTheAircraft().setCanard(
						LiftingSurface.importFromXML(
								ComponentEnum.CANARD,
								canardFilePath.getAbsolutePath(),
								Main.getInputDirectoryPath() + File.separator
								+ "Template_Aircraft" + File.separator
								+ "lifting_surfaces" + File.separator
								+ "airfoils" + File.separator
								)
						);
				Main.getTheAircraft().getCanard().setAeroDatabaseReader(aerodynamicDatabaseReader);
				Main.getTheAircraft().getCanard().setHighLiftDatabaseReader(highLiftDatabaseReader);
				Main.getTheAircraft().getCanard().setVeDSCDatabaseReader(veDSCDatabaseReader);
				Main.getTheAircraft().getCanard().calculateGeometry(
						ComponentEnum.CANARD, 
						true
						);
				Main.getTheAircraft().getCanard().populateAirfoilList(
						false
						);
				Main.getTheAircraft().getCanard().setXApexConstructionAxes(canardXApex);
				Main.getTheAircraft().getCanard().setYApexConstructionAxes(canardYApex);
				Main.getTheAircraft().getCanard().setZApexConstructionAxes(canardZApex);
				Main.getTheAircraft().getCanard().setRiggingAngle(canardRiggingAngle);
			}
		}
		//....................................................................................
		// POWER PLANT
		if (theController.isUpdatePowerPlantDataFromFile() == true) {
			
			List<Engine> engineList = new ArrayList<>();
			List<Amount<Length>> engineXList = new ArrayList<>();
			List<Amount<Length>> engineYList = new ArrayList<>();
			List<Amount<Length>> engineZList = new ArrayList<>();
			List<Amount<Angle>> engineTiltList = new ArrayList<>();		
			List<EngineMountingPositionEnum> engineMountingPositionList = new ArrayList<>();

			for (int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				engineXList.add(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(theController.getEngineXPositionValueList().get(i)), 
								Unit.valueOf(theController.getEngineXPositionUnitList().get(i))
								)
						);
				engineYList.add(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(theController.getEngineYPositionValueList().get(i)), 
								Unit.valueOf(theController.getEngineYPositionUnitList().get(i))
								)
						);
				engineZList.add(
						(Amount<Length>) Amount.valueOf(
								Double.valueOf(theController.getEngineZPositionValueList().get(i)), 
								Unit.valueOf(theController.getEngineZPositionUnitList().get(i))
								)
						);
				engineTiltList.add(
						(Amount<Angle>) Amount.valueOf(
								Double.valueOf(theController.getEngineTiltAngleValueList().get(i)), 
								Unit.valueOf(theController.getEngineTiltAngleUnitList().get(i))
								)
						);
				engineMountingPositionList.add(
						EngineMountingPositionEnum.valueOf(theController.getEngineMountinPositionValueList().get(i))
						);

			}

			powerPlantFilePathList.stream().filter(file -> file.exists()).forEach(
					file -> engineList.add(Engine.importFromXML(file.getAbsolutePath()))
					);

			for(int i=0; i<engineList.size(); i++) {

				engineList.get(i).setXApexConstructionAxes(engineXList.get(i));
				engineList.get(i).setYApexConstructionAxes(engineYList.get(i));
				engineList.get(i).setZApexConstructionAxes(engineZList.get(i));
				engineList.get(i).setTiltingAngle(engineTiltList.get(i));
				engineList.get(i).setMountingPosition(engineMountingPositionList.get(i));

			}

			Main.getTheAircraft().setPowerPlant(new PowerPlant(engineList));
		}
		//....................................................................................
		// NACELLES
		if (theController.isUpdateNacellesDataFromFile() == true) {
			
		List<NacelleCreator> nacelleList = new ArrayList<>();
		List<Amount<Length>> nacelleXList = new ArrayList<>();
		List<Amount<Length>> nacelleYList = new ArrayList<>();
		List<Amount<Length>> nacelleZList = new ArrayList<>();
		List<NacelleMountingPositionEnum> nacelleMountingPositionList = new ArrayList<>();
		
		Main.getTheAircraft().getNacelles().getNacellesList().stream().forEach(nacelle -> {
			nacelleXList.add(nacelle.getXApexConstructionAxes());
			nacelleYList.add(nacelle.getYApexConstructionAxes());
			nacelleZList.add(nacelle.getZApexConstructionAxes());
			nacelleMountingPositionList.add(nacelle.getMountingPosition());
		});

		nacellesFilePathList.stream().filter(file -> file.exists()).forEach(
				file -> nacelleList.add(
						NacelleCreator.importFromXML(
								file.getAbsolutePath(),
								Main.getInputDirectoryPath() + File.separator
								+ "Template_Aircraft" + File.separator
								+ "engines" + File.separator
								)
						)
				);
		
		for(int i=0; i<nacelleList.size(); i++) {
			
			nacelleList.get(i).setXApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(
							Double.valueOf(theController.getNacelleXPositionValueList().get(i)), 
							Unit.valueOf(theController.getNacelleXPositionUnitList().get(i))
							)
					);
			nacelleList.get(i).setYApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(
							Double.valueOf(theController.getNacelleYPositionValueList().get(i)), 
							Unit.valueOf(theController.getNacelleYPositionUnitList().get(i))
							)
					);
			nacelleList.get(i).setZApexConstructionAxes(
					(Amount<Length>) Amount.valueOf(
							Double.valueOf(theController.getNacelleZPositionValueList().get(i)), 
							Unit.valueOf(theController.getNacelleZPositionUnitList().get(i))
							)
					);
			nacelleList.get(i).setMountingPosition(
					NacelleMountingPositionEnum.valueOf(theController.getNacelleMountinPositionValueList().get(i))
					);
			
		}

		Main.getTheAircraft().setNacelles(new Nacelles(nacelleList));
		
		}
		//....................................................................................
		// LANDING GEARS
		if (theController.isUpdateLandingGearsDataFromFile() == true) {
			if (landingGearsFilePath.exists()) {

				Amount<Length> noseLandingGearsXApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getNoseLandingGearsXPositionValue()),
						Unit.valueOf(theController.getNoseLandingGearsXPositionUnit())
						);
				Amount<Length> noseLandingGearsYApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getNoseLandingGearsYPositionValue()),
						Unit.valueOf(theController.getNoseLandingGearsYPositionUnit())
						);
				Amount<Length> noseLandingGearsZApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getNoseLandingGearsZPositionValue()),
						Unit.valueOf(theController.getNoseLandingGearsZPositionUnit())
						);
				Amount<Length> mainLandingGearsXApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getMainLandingGearsXPositionValue()),
						Unit.valueOf(theController.getMainLandingGearsXPositionUnit())
						);
				Amount<Length> mainLandingGearsYApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getMainLandingGearsYPositionValue()),
						Unit.valueOf(theController.getMainLandingGearsYPositionUnit())
						);
				Amount<Length> mainLandingGearsZApex = (Amount<Length>) Amount.valueOf(
						Double.valueOf(theController.getMainLandingGearsZPositionValue()),
						Unit.valueOf(theController.getMainLandingGearsZPositionUnit())
						);
				LandingGearsMountingPositionEnum landingGearsMountingPosition =
						LandingGearsMountingPositionEnum.valueOf(theController.getLandingGearsMountinPositionValue());

				Main.getTheAircraft().setLandingGears(
						LandingGears.importFromXML(landingGearsFilePath.getAbsolutePath())
						);
				Main.getTheAircraft().getLandingGears().setXApexConstructionAxesNoseGear(noseLandingGearsXApex);
				Main.getTheAircraft().getLandingGears().setYApexConstructionAxesNoseGear(noseLandingGearsYApex);
				Main.getTheAircraft().getLandingGears().setZApexConstructionAxesNoseGear(noseLandingGearsZApex);
				Main.getTheAircraft().getLandingGears().setXApexConstructionAxesMainGear(mainLandingGearsXApex);
				Main.getTheAircraft().getLandingGears().setYApexConstructionAxesMainGear(mainLandingGearsYApex);
				Main.getTheAircraft().getLandingGears().setZApexConstructionAxesMainGear(mainLandingGearsZApex);
				Main.getTheAircraft().getLandingGears().setMountingPosition(landingGearsMountingPosition);
			}
		}
		
		//....................................................................................
		// LOGGING AIRCRAFT COMPONENTS DATA TO GUI ...
		//....................................................................................
		// COMPONENTS LOG TO INTERFACE
		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				if (theController.isUpdateFuselageDataFromFile() == true)
					if(Main.getTheAircraft().getFuselage() != null)
						theController.getInputManagerControllerLogUtilities().logFuselageFromFileToInterface();
				if (theController.isUpdateCabinConfigurationDataFromFile() == true)
					if(Main.getTheAircraft().getCabinConfiguration() != null)
						theController.getInputManagerControllerLogUtilities().logCabinConfigutionFromFileToInterface();
				if (theController.isUpdateWingDataFromFile() == true)
					if(Main.getTheAircraft().getWing() != null)
						theController.getInputManagerControllerLogUtilities().logWingFromFileToInterface();
				if (theController.isUpdateHTailDataFromFile() == true)
					if(Main.getTheAircraft().getHTail() != null)
						theController.getInputManagerControllerLogUtilities().logHTailFromFileToInterface();
				if (theController.isUpdateVTailDataFromFile() == true)
					if(Main.getTheAircraft().getVTail() != null)
						theController.getInputManagerControllerLogUtilities().logVTailFromFileToInterface();
				if (theController.isUpdateCanardDataFromFile() == true)
					if(Main.getTheAircraft().getCanard() != null)
						theController.getInputManagerControllerLogUtilities().logCanardFromFileToInterface();
				if (theController.isUpdateNacellesDataFromFile() == true)
					if(Main.getTheAircraft().getNacelles() != null)
						theController.getInputManagerControllerLogUtilities().logNacelleFromFileToInterface();
				if (theController.isUpdatePowerPlantDataFromFile() == true)
					if(Main.getTheAircraft().getPowerPlant() != null)
						theController.getInputManagerControllerLogUtilities().logPowerPlantFromFileToInterface();
				if (theController.isUpdateLandingGearsDataFromFile() == true)
					if(Main.getTheAircraft().getLandingGears() != null)
						theController.getInputManagerControllerLogUtilities().logLandingGearsFromFileToInterface();
				
			}
		});
	}
}
