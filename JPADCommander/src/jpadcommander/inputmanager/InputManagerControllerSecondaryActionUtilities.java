package jpadcommander.inputmanager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.Validator;

import aircraft.components.nacelles.NacelleCreator;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineTypeEnum;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import jpadcommander.Main;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyMapUtils;

public class InputManagerControllerSecondaryActionUtilities {
	
	//---------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	public InputManagerController theController;
	
	//---------------------------------------------------------------------------------
	// BUILDER
	public InputManagerControllerSecondaryActionUtilities(InputManagerController controller) {
		
		this.theController = controller;
		
	}
	
	//---------------------------------------------------------------------------------
	// METHODS
	public void removeContentOnAircraftEngineTabClose (Tab tab) {

		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {

				int indexEngine = theController.getTabPaneAircraftEngines().getTabs().indexOf(tab);

				theController.getTextFieldsAircraftEngineFileList().remove(indexEngine);
				theController.getTextFieldAircraftEngineXList().remove(indexEngine);
				theController.getTextFieldAircraftEngineYList().remove(indexEngine);
				theController.getTextFieldAircraftEngineZList().remove(indexEngine);
				theController.getTextFieldAircraftEngineTiltList().remove(indexEngine);
				theController.getChoiceBoxesAircraftEnginePositonList().remove(indexEngine);

				theController.getChoiceBoxAircraftEngineXUnitList().remove(indexEngine);
				theController.getChoiceBoxAircraftEngineYUnitList().remove(indexEngine);
				theController.getChoiceBoxAircraftEngineZUnitList().remove(indexEngine);
				theController.getChoiceBoxAircraftEngineTiltUnitList().remove(indexEngine);
				
			}
		});
	}
	
	public void removeContentOnAircraftNacelleTabClose (Tab tab) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
		
				int indexNacelle = theController.getTabPaneAircraftNacelles().getTabs().indexOf(tab);

				theController.getTextFieldsAircraftNacelleFileList().remove(indexNacelle);
				theController.getTextFieldAircraftNacelleXList().remove(indexNacelle);
				theController.getTextFieldAircraftNacelleYList().remove(indexNacelle);
				theController.getTextFieldAircraftNacelleZList().remove(indexNacelle);
				theController.getChoiceBoxesAircraftNacellePositonList().remove(indexNacelle);

				theController.getChoiceBoxAircraftNacelleXUnitList().remove(indexNacelle);
				theController.getChoiceBoxAircraftNacelleYUnitList().remove(indexNacelle);
				theController.getChoiceBoxAircraftNacelleZUnitList().remove(indexNacelle);
				
			}
		});
	}
	
	public void removeContentOnNacelleTabClose (Tab tab) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
		
				int indexNacelle = theController.getTabPaneNacelles().getTabs().indexOf(tab);

				theController.getTextFieldNacelleRoughnessList().remove(indexNacelle);
				theController.getTextFieldNacelleLengthList().remove(indexNacelle);
				theController.getTextFieldNacelleMaximumDiameterList().remove(indexNacelle);
				theController.getTextFieldNacelleRoughnessList().remove(indexNacelle);
				theController.getTextFieldNacelleKInletList().remove(indexNacelle);
				theController.getTextFieldNacelleKOutletList().remove(indexNacelle);
				theController.getTextFieldNacelleKLengthList().remove(indexNacelle);
				theController.getTextFieldNacelleKDiameterOutletList().remove(indexNacelle);
				
				theController.getChoiceBoxNacelleRoughnessUnitList().remove(indexNacelle);
				theController.getChoiceBoxNacelleLengthUnitList().remove(indexNacelle);
				theController.getChoiceBoxNacelleMaximumDiameterUnitList().remove(indexNacelle);
				
				theController.getNacelleEstimateDimesnsionButtonList().remove(indexNacelle);
				theController.getNacelleKInletInfoButtonList().remove(indexNacelle);
				theController.getNacelleKOutletInfoButtonList().remove(indexNacelle);
				theController.getNacelleKLengthInfoButtonList().remove(indexNacelle);
				theController.getNacelleKDiameterOutletInfoButtonList().remove(indexNacelle);
				
			}
		});
	}
	
	public void removeContentOnEngineTabClose (Tab tab) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
		
				int indexEngine = theController.getTabPaneEngines().getTabs().indexOf(tab);
				theController.getPowerPlantJetRadioButtonList().remove(indexEngine);
				theController.getPowerPlantTurbopropRadioButtonList().remove(indexEngine);
				theController.getPowerPlantPistonRadioButtonList().remove(indexEngine);
				theController.getPowerPlantToggleGropuList().remove(indexEngine);
				
				theController.getPowerPlantBorderPaneMap().remove(indexEngine);
				theController.getPowerPlantPaneMap().remove(indexEngine);
				
			}
		});
	}
	
	public void removeAirfoilDetailsButtonFromMapOnTabClose(Tab tab, ComponentEnum type) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				
				switch (type) {
				case WING:
					MyMapUtils.removeEntryByValue(
							theController.getWingAirfoilDetailsButtonAndTabsMap(),
							theController.getTabPaneWingViewAndAirfoils().getTabs().indexOf(tab)
							);
					break;
					
				case HORIZONTAL_TAIL:
					MyMapUtils.removeEntryByValue(
							theController.gethTailAirfoilDetailsButtonAndTabsMap(),
							theController.getTabPaneHTailViewAndAirfoils().getTabs().indexOf(tab)
							);
					break;

				case VERTICAL_TAIL:
					MyMapUtils.removeEntryByValue(
							theController.getvTailAirfoilDetailsButtonAndTabsMap(),
							theController.getTabPaneVTailViewAndAirfoils().getTabs().indexOf(tab)
							);
					break;
					
				case CANARD:
					MyMapUtils.removeEntryByValue(
							theController.getCanardAirfoilDetailsButtonAndTabsMap(),
							theController.getTabPaneCanardViewAndAirfoils().getTabs().indexOf(tab)
							);
					break;
					
				default:
					break;
				}
				
			}
		});
		
	}
	
	public void removeContentOnPanelTabClose(Tab tab, ComponentEnum type) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {
			
			@Override
			public void handle(Event event) {
				
				switch (type) {
				case WING:
					int indexWing = theController.getTabPaneWingPanels().getTabs().indexOf(tab); 
					
					if (indexWing > 0)
						theController.getCheckBoxWingLinkedToPreviousPanelList().remove(indexWing-1);
					
					theController.getTextFieldWingSpanPanelList().remove(indexWing);
					theController.getTextFieldWingSweepLEPanelList().remove(indexWing);
					theController.getTextFieldWingDihedralPanelList().remove(indexWing);
					theController.getTextFieldWingInnerChordPanelList().remove(indexWing);
					theController.getTextFieldWingInnerTwistPanelList().remove(indexWing);
					theController.getTextFieldWingInnerAirfoilPanelList().remove(indexWing);
					theController.getTextFieldWingOuterChordPanelList().remove(indexWing);
					theController.getTextFieldWingOuterTwistPanelList().remove(indexWing);
					theController.getTextFieldWingOuterAirfoilPanelList().remove(indexWing);
					
					theController.getChoiceBoxWingSpanPanelUnitList().remove(indexWing);
					theController.getChoiceBoxWingSweepLEPanelUnitList().remove(indexWing);
					theController.getChoiceBoxWingDihedralPanelUnitList().remove(indexWing);
					theController.getChoiceBoxWingInnerChordPanelUnitList().remove(indexWing);
					theController.getChoiceBoxWingInnerTwistPanelUnitList().remove(indexWing);
					theController.getChoiceBoxWingOuterChordPanelUnitList().remove(indexWing);
					theController.getChoiceBoxWingOuterTwistPanelUnitList().remove(indexWing);
					
					theController.getChooseInnerWingAirfoilFileButtonList().remove(indexWing);
					theController.getChooseOuterWingAirfoilFileButtonList().remove(indexWing);
					theController.getDetailButtonWingInnerAirfoilList().remove(indexWing);
					theController.getDetailButtonWingOuterAirfoilList().remove(indexWing);
					break;
					
				case HORIZONTAL_TAIL:
					int indexHTail = theController.getTabPaneHTailPanels().getTabs().indexOf(tab); 
					
					if (indexHTail > 0)
						theController.getCheckBoxHTailLinkedToPreviousPanelList().remove(indexHTail-1);
					
					theController.getTextFieldHTailSpanPanelList().remove(indexHTail);
					theController.getTextFieldHTailSweepLEPanelList().remove(indexHTail);
					theController.getTextFieldHTailDihedralPanelList().remove(indexHTail);
					theController.getTextFieldHTailInnerChordPanelList().remove(indexHTail);
					theController.getTextFieldHTailInnerTwistPanelList().remove(indexHTail);
					theController.getTextFieldHTailInnerAirfoilPanelList().remove(indexHTail);
					theController.getTextFieldHTailOuterChordPanelList().remove(indexHTail);
					theController.getTextFieldHTailOuterTwistPanelList().remove(indexHTail);
					theController.getTextFieldHTailOuterAirfoilPanelList().remove(indexHTail);
					
					theController.getChoiceBoxHTailSpanPanelUnitList().remove(indexHTail);
					theController.getChoiceBoxHTailSweepLEPanelUnitList().remove(indexHTail);
					theController.getChoiceBoxHTailDihedralPanelUnitList().remove(indexHTail);
					theController.getChoiceBoxHTailInnerChordPanelUnitList().remove(indexHTail);
					theController.getChoiceBoxHTailInnerTwistPanelUnitList().remove(indexHTail);
					theController.getChoiceBoxHTailOuterChordPanelUnitList().remove(indexHTail);
					theController.getChoiceBoxHTailOuterTwistPanelUnitList().remove(indexHTail);
					
					theController.getChooseInnerHTailAirfoilFileButtonList().remove(indexHTail);
					theController.getChooseOuterHTailAirfoilFileButtonList().remove(indexHTail);
					theController.getDetailButtonHTailInnerAirfoilList().remove(indexHTail);
					theController.getDetailButtonHTailOuterAirfoilList().remove(indexHTail);
					break;
					
				case VERTICAL_TAIL:
					int indexVTail = theController.getTabPaneVTailPanels().getTabs().indexOf(tab); 
					
					if (indexVTail > 0)
						theController.getCheckBoxVTailLinkedToPreviousPanelList().remove(indexVTail-1);
					
					theController.getTextFieldVTailSpanPanelList().remove(indexVTail);
					theController.getTextFieldVTailSweepLEPanelList().remove(indexVTail);
					theController.getTextFieldVTailDihedralPanelList().remove(indexVTail);
					theController.getTextFieldVTailInnerChordPanelList().remove(indexVTail);
					theController.getTextFieldVTailInnerTwistPanelList().remove(indexVTail);
					theController.getTextFieldVTailInnerAirfoilPanelList().remove(indexVTail);
					theController.getTextFieldVTailOuterChordPanelList().remove(indexVTail);
					theController.getTextFieldVTailOuterTwistPanelList().remove(indexVTail);
					theController.getTextFieldVTailOuterAirfoilPanelList().remove(indexVTail);
					
					theController.getChoiceBoxVTailSpanPanelUnitList().remove(indexVTail);
					theController.getChoiceBoxVTailSweepLEPanelUnitList().remove(indexVTail);
					theController.getChoiceBoxVTailDihedralPanelUnitList().remove(indexVTail);
					theController.getChoiceBoxVTailInnerChordPanelUnitList().remove(indexVTail);
					theController.getChoiceBoxVTailInnerTwistPanelUnitList().remove(indexVTail);
					theController.getChoiceBoxVTailOuterChordPanelUnitList().remove(indexVTail);
					theController.getChoiceBoxVTailOuterTwistPanelUnitList().remove(indexVTail);
					
					theController.getChooseInnerVTailAirfoilFileButtonList().remove(indexVTail);
					theController.getChooseOuterVTailAirfoilFileButtonList().remove(indexVTail);
					theController.getDetailButtonVTailInnerAirfoilList().remove(indexVTail);
					theController.getDetailButtonVTailOuterAirfoilList().remove(indexVTail);
					break;
					
				case CANARD:
					int indexCanard = theController.getTabPaneCanardPanels().getTabs().indexOf(tab); 
					
					if (indexCanard > 0)
						theController.getCheckBoxCanardLinkedToPreviousPanelList().remove(indexCanard-1);
					
					theController.getTextFieldCanardSpanPanelList().remove(indexCanard);
					theController.getTextFieldCanardSweepLEPanelList().remove(indexCanard);
					theController.getTextFieldCanardDihedralPanelList().remove(indexCanard);
					theController.getTextFieldCanardInnerChordPanelList().remove(indexCanard);
					theController.getTextFieldCanardInnerTwistPanelList().remove(indexCanard);
					theController.getTextFieldCanardInnerAirfoilPanelList().remove(indexCanard);
					theController.getTextFieldCanardOuterChordPanelList().remove(indexCanard);
					theController.getTextFieldCanardOuterTwistPanelList().remove(indexCanard);
					theController.getTextFieldCanardOuterAirfoilPanelList().remove(indexCanard);
					
					theController.getChoiceBoxCanardSpanPanelUnitList().remove(indexCanard);
					theController.getChoiceBoxCanardSweepLEPanelUnitList().remove(indexCanard);
					theController.getChoiceBoxCanardDihedralPanelUnitList().remove(indexCanard);
					theController.getChoiceBoxCanardInnerChordPanelUnitList().remove(indexCanard);
					theController.getChoiceBoxCanardInnerTwistPanelUnitList().remove(indexCanard);
					theController.getChoiceBoxCanardOuterChordPanelUnitList().remove(indexCanard);
					theController.getChoiceBoxCanardOuterTwistPanelUnitList().remove(indexCanard);
					
					theController.getChooseInnerCanardAirfoilFileButtonList().remove(indexCanard);
					theController.getChooseOuterCanardAirfoilFileButtonList().remove(indexCanard);
					theController.getDetailButtonCanardInnerAirfoilList().remove(indexCanard);
					theController.getDetailButtonCanardOuterAirfoilList().remove(indexCanard);
					break;
					
				default:
					break;
				}
			}
		});
		
	}

	public void removeContentOnFlapTabClose(Tab tab, ComponentEnum type) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				
				switch (type) {
				case WING:
					int indexWing = theController.getTabPaneWingFlaps().getTabs().indexOf(tab);
					
					theController.getTextFieldWingInnerPositionFlapList().remove(indexWing);
					theController.getTextFieldWingOuterPositionFlapList().remove(indexWing);
					theController.getTextFieldWingInnerChordRatioFlapList().remove(indexWing);
					theController.getTextFieldWingOuterChordRatioFlapList().remove(indexWing);
					theController.getTextFieldWingMinimumDeflectionAngleFlapList().remove(indexWing);
					theController.getTextFieldWingMaximumDeflectionAngleFlapList().remove(indexWing);
					
					theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().remove(indexWing);
					theController.getChoiceBoxWingMaximumDeflectionAngleFlapUnitList().remove(indexWing);					
					break;

				case HORIZONTAL_TAIL:
					int indexHTail = theController.getTabPaneHTailElevators().getTabs().indexOf(tab);
					
					theController.getTextFieldHTailInnerPositionElevatorList().remove(indexHTail);
					theController.getTextFieldHTailOuterPositionElevatorList().remove(indexHTail);
					theController.getTextFieldHTailInnerChordRatioElevatorList().remove(indexHTail);
					theController.getTextFieldHTailOuterChordRatioElevatorList().remove(indexHTail);
					theController.getTextFieldHTailMinimumDeflectionAngleElevatorList().remove(indexHTail);
					theController.getTextFieldHTailMaximumDeflectionAngleElevatorList().remove(indexHTail);
					
					theController.getChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList().remove(indexHTail);
					theController.getChoiceBoxHTailMaximumDeflectionAngleElevatorUnitList().remove(indexHTail);
					break;
					
				case VERTICAL_TAIL:
					int indexVTail = theController.getTabPaneVTailRudders().getTabs().indexOf(tab);
					
					theController.getTextFieldVTailInnerPositionRudderList().remove(indexVTail);
					theController.getTextFieldVTailOuterPositionRudderList().remove(indexVTail);
					theController.getTextFieldVTailInnerChordRatioRudderList().remove(indexVTail);
					theController.getTextFieldVTailOuterChordRatioRudderList().remove(indexVTail);
					theController.getTextFieldVTailMinimumDeflectionAngleRudderList().remove(indexVTail);
					theController.getTextFieldVTailMaximumDeflectionAngleRudderList().remove(indexVTail);
					
					theController.getChoiceBoxVTailMinimumDeflectionAngleRudderUnitList().remove(indexVTail);
					theController.getChoiceBoxVTailMaximumDeflectionAngleRudderUnitList().remove(indexVTail);
					break;
					
				case CANARD:
					int indexCanard = theController.getTabPaneCanardControlSurfaces().getTabs().indexOf(tab);
					
					theController.getTextFieldCanardInnerPositionControlSurfaceList().remove(indexCanard);
					theController.getTextFieldCanardOuterPositionControlSurfaceList().remove(indexCanard);
					theController.getTextFieldCanardInnerChordRatioControlSurfaceList().remove(indexCanard);
					theController.getTextFieldCanardOuterChordRatioControlSurfaceList().remove(indexCanard);
					theController.getTextFieldCanardMinimumDeflectionAngleControlSurfaceList().remove(indexCanard);
					theController.getTextFieldCanardMaximumDeflectionAngleControlSurfaceList().remove(indexCanard);
					
					theController.getChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList().remove(indexCanard);
					theController.getChoiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList().remove(indexCanard);
					break;
					
				default:
					break;
				}
			}
		});
		
	}
	
	public void removeContentOnSlatTabClose(Tab tab) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				
				int index = theController.getTabPaneWingSlats().getTabs().indexOf(tab);
				
				theController.getTextFieldWingInnerPositionSlatList().remove(index);
				theController.getTextFieldWingOuterPositionSlatList().remove(index);
				theController.getTextFieldWingInnerChordRatioSlatList().remove(index);
				theController.getTextFieldWingOuterChordRatioSlatList().remove(index);
				theController.getTextFieldWingExtensionRatioSlatList().remove(index);
				theController.getTextFieldWingMinimumDeflectionAngleSlatList().remove(index);
				theController.getTextFieldWingMaximumDeflectionAngleSlatList().remove(index);
				
				theController.getChoiceBoxWingMinimumDeflectionAngleSlatUnitList().remove(index);
				theController.getChoiceBoxWingMaximumDeflectionAngleSlatUnitList().remove(index);
				
			}
		});
		
	}
	
	public void removeContentOnSpoilerTabClose (Tab tab, ComponentEnum type) {
		
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				
				switch (type) {
				case WING:
					int indexWing = theController.getTabPaneWingSpoilers().getTabs().indexOf(tab);
					
					theController.getTextFieldWingInnerSpanwisePositionSpoilerList().remove(indexWing);
					theController.getTextFieldWingOuterSpanwisePositionSpoilerList().remove(indexWing);
					theController.getTextFieldWingInnerChordwisePositionSpoilerList().remove(indexWing);
					theController.getTextFieldWingOuterChordwisePositionSpoilerList().remove(indexWing);
					theController.getTextFieldWingMinimumDeflectionAngleSpoilerList().remove(indexWing);
					theController.getTextFieldWingMaximumDeflectionAngleSpoilerList().remove(indexWing);
					
					theController.getChoiceBoxWingMinimumDeflectionAngleSpoilerUnitList().remove(indexWing);
					theController.getChoiceBoxWingMaximumDeflectionAngleSpoilerUnitList().remove(indexWing);					
					break;
					
				case FUSELAGE:
					int indexFuselage = theController.getTabPaneFuselageSpoilers().getTabs().indexOf(tab);
					
					theController.getTextFieldFuselageInnerSpanwisePositionSpoilerList().remove(indexFuselage);
					theController.getTextFieldFuselageOuterSpanwisePositionSpoilerList().remove(indexFuselage);
					theController.getTextFieldFuselageInnerChordwisePositionSpoilerList().remove(indexFuselage);
					theController.getTextFieldFuselageOuterChordwisePositionSpoilerList().remove(indexFuselage);
					theController.getTextFieldFuselageMinimumDeflectionAngleSpoilerList().remove(indexFuselage);
					theController.getTextFieldFuselageMaximumDeflectionAngleSpoilerList().remove(indexFuselage);
					
					theController.getChoiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList().remove(indexFuselage);
					theController.getChoiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList().remove(indexFuselage);
					break;

				default:
					break;
				}
			}
		});
		
	}
	
	public void linkedToDisableCheck(ComponentEnum type) {

		switch (type) {
		case WING:
			for(int i=0; i<theController.getCheckBoxWingLinkedToPreviousPanelList().size(); i++) {
				
				theController.getTextFieldWingInnerChordPanelList().get(i+1).disableProperty().bind(
						theController.getCheckBoxWingLinkedToPreviousPanelList().get(i).selectedProperty()
						);	
				theController.getChoiceBoxWingInnerChordPanelUnitList().get(i+1).disableProperty().bind(
						theController.getCheckBoxWingLinkedToPreviousPanelList().get(i).selectedProperty()
						);	
				theController.getTextFieldWingInnerTwistPanelList().get(i+1).disableProperty().bind(
						theController.getCheckBoxWingLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getChoiceBoxWingInnerTwistPanelUnitList().get(i+1).disableProperty().bind(
						theController.getCheckBoxWingLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getTextFieldWingInnerAirfoilPanelList().get(i+1).disableProperty().bind(
						theController.getCheckBoxWingLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getDetailButtonWingInnerAirfoilList().get(i+1).disableProperty().bind(
						theController.getCheckBoxWingLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getChooseInnerWingAirfoilFileButtonList().get(i+1).disableProperty().bind(
						theController.getCheckBoxWingLinkedToPreviousPanelList().get(i).selectedProperty()
						);
			}
			break;
			
		case HORIZONTAL_TAIL:
			for(int i=0; i<theController.getCheckBoxHTailLinkedToPreviousPanelList().size(); i++) {
				
				theController.getTextFieldHTailInnerChordPanelList().get(i+1).disableProperty().bind(
						theController.getCheckBoxHTailLinkedToPreviousPanelList().get(i).selectedProperty()
						);	
				theController.getChoiceBoxHTailInnerChordPanelUnitList().get(i+1).disableProperty().bind(
						theController.getCheckBoxHTailLinkedToPreviousPanelList().get(i).selectedProperty()
						);	
				theController.getTextFieldHTailInnerTwistPanelList().get(i+1).disableProperty().bind(
						theController.getCheckBoxHTailLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getChoiceBoxHTailInnerTwistPanelUnitList().get(i+1).disableProperty().bind(
						theController.getCheckBoxHTailLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getTextFieldHTailInnerAirfoilPanelList().get(i+1).disableProperty().bind(
						theController.getCheckBoxHTailLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getDetailButtonHTailInnerAirfoilList().get(i+1).disableProperty().bind(
						theController.getCheckBoxHTailLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getChooseInnerHTailAirfoilFileButtonList().get(i+1).disableProperty().bind(
						theController.getCheckBoxHTailLinkedToPreviousPanelList().get(i).selectedProperty()
						);
			}
			break;
			
		case VERTICAL_TAIL:
			for(int i=0; i<theController.getCheckBoxVTailLinkedToPreviousPanelList().size(); i++) {
				
				theController.getTextFieldVTailInnerChordPanelList().get(i+1).disableProperty().bind(
						theController.getCheckBoxVTailLinkedToPreviousPanelList().get(i).selectedProperty()
						);	
				theController.getChoiceBoxVTailInnerChordPanelUnitList().get(i+1).disableProperty().bind(
						theController.getCheckBoxVTailLinkedToPreviousPanelList().get(i).selectedProperty()
						);	
				theController.getTextFieldVTailInnerTwistPanelList().get(i+1).disableProperty().bind(
						theController.getCheckBoxVTailLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getChoiceBoxVTailInnerTwistPanelUnitList().get(i+1).disableProperty().bind(
						theController.getCheckBoxVTailLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getTextFieldVTailInnerAirfoilPanelList().get(i+1).disableProperty().bind(
						theController.getCheckBoxVTailLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getDetailButtonVTailInnerAirfoilList().get(i+1).disableProperty().bind(
						theController.getCheckBoxVTailLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getChooseInnerVTailAirfoilFileButtonList().get(i+1).disableProperty().bind(
						theController.getCheckBoxVTailLinkedToPreviousPanelList().get(i).selectedProperty()
						);
			}
			break;

		case CANARD:
			for(int i=0; i<theController.getCheckBoxCanardLinkedToPreviousPanelList().size(); i++) {
				
				theController.getTextFieldCanardInnerChordPanelList().get(i+1).disableProperty().bind(
						theController.getCheckBoxCanardLinkedToPreviousPanelList().get(i).selectedProperty()
						);	
				theController.getChoiceBoxCanardInnerChordPanelUnitList().get(i+1).disableProperty().bind(
						theController.getCheckBoxCanardLinkedToPreviousPanelList().get(i).selectedProperty()
						);	
				theController.getTextFieldCanardInnerTwistPanelList().get(i+1).disableProperty().bind(
						theController.getCheckBoxCanardLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getChoiceBoxCanardInnerTwistPanelUnitList().get(i+1).disableProperty().bind(
						theController.getCheckBoxCanardLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getTextFieldCanardInnerAirfoilPanelList().get(i+1).disableProperty().bind(
						theController.getCheckBoxCanardLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getDetailButtonCanardInnerAirfoilList().get(i+1).disableProperty().bind(
						theController.getCheckBoxCanardLinkedToPreviousPanelList().get(i).selectedProperty()
						);
				theController.getChooseInnerCanardAirfoilFileButtonList().get(i+1).disableProperty().bind(
						theController.getCheckBoxCanardLinkedToPreviousPanelList().get(i).selectedProperty()
						);
			}
			break;
			
		default:
			break;
		}
	}

	public void setChooseAirfoilFileAction (Button chooseFileButton, TextField airfoilPathTextField) {
		
		chooseFileButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				theController.chooseAirfoilFile(airfoilPathTextField);
			}
		});
		
	}
	
	public void setChooseNacelleFileAction() {
		
		for(int i=0; i<theController.getTabPaneAircraftNacelles().getTabs().size(); i++) {
			
			int indexOfNacelle = i;
			theController.getChooseNacelleFileButtonList().get(i).setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {
					try {
						chooseAircraftNacelleFile(indexOfNacelle);
					} catch (IOException e) {
						e.printStackTrace();
					}					
				}
			});
		}
	}
	
	public void setChooseEngineFileAction() {
		
		for(int i=0; i<theController.getTabPaneAircraftEngines().getTabs().size(); i++) {
			
			int indexOfEngine = i;
			theController.getChooseEngineFileButtonList().get(i).setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					try {
						chooseAircraftEngineFile(indexOfEngine);
					} catch (IOException e) {
						e.printStackTrace();
					}					
				}
			});
		}
	}
	
	public void setAirfoilDetailsActionAndDisableCheck (Button detailsButton, TextField airfoilPathTextField, ComponentEnum type) {

		detailsButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				switch (type) {
				case WING:
					if(!theController.getWingAirfoilDetailsButtonAndTabsMap().containsKey(detailsButton)) {
						try {
							theController.getWingAirfoilDetailsButtonAndTabsMap().put(
									detailsButton, 
									theController.getTabPaneWingViewAndAirfoils().getTabs().size()
									);
							theController.showAirfoilData(
									Paths.get(airfoilPathTextField.getText()).getFileName().toString(),
									detailsButton,
									type
									);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
					
				case HORIZONTAL_TAIL:
					if(!theController.gethTailAirfoilDetailsButtonAndTabsMap().containsKey(detailsButton)) {
						try {
							theController.gethTailAirfoilDetailsButtonAndTabsMap().put(
									detailsButton, 
									theController.getTabPaneHTailViewAndAirfoils().getTabs().size()
									);
							theController.showAirfoilData(
									Paths.get(airfoilPathTextField.getText()).getFileName().toString(),
									detailsButton,
									type
									);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
					
				case VERTICAL_TAIL:
					if(!theController.getvTailAirfoilDetailsButtonAndTabsMap().containsKey(detailsButton)) {
						try {
							theController.getvTailAirfoilDetailsButtonAndTabsMap().put(
									detailsButton, 
									theController.getTabPaneVTailViewAndAirfoils().getTabs().size()
									);
							theController.showAirfoilData(
									Paths.get(airfoilPathTextField.getText()).getFileName().toString(),
									detailsButton,
									type
									);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
					
				case CANARD:
					if(!theController.getCanardAirfoilDetailsButtonAndTabsMap().containsKey(detailsButton)) {
						try {
							theController.getCanardAirfoilDetailsButtonAndTabsMap().put(
									detailsButton, 
									theController.getTabPaneCanardViewAndAirfoils().getTabs().size()
									);
							theController.showAirfoilData(
									Paths.get(airfoilPathTextField.getText()).getFileName().toString(),
									detailsButton,
									type
									);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
					
				default:
					break;
				}
			}
		});
		
		final Tooltip warning = new Tooltip("WARNING : The airfoil details are already opened !!");
		detailsButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				
				switch (type) {
				case WING:
					Point2D pW = detailsButton
					.localToScreen(
							-2.5*detailsButton.getLayoutBounds().getMaxX(),
							1.2*detailsButton.getLayoutBounds().getMaxY()
							);
					if(theController.getWingAirfoilDetailsButtonAndTabsMap().containsKey(detailsButton)) {
						warning.show(detailsButton, pW.getX(), pW.getY());
					}
					break;

				case HORIZONTAL_TAIL:
					Point2D pH = detailsButton
					.localToScreen(
							-2.5*detailsButton.getLayoutBounds().getMaxX(),
							1.2*detailsButton.getLayoutBounds().getMaxY()
							);
					if(theController.gethTailAirfoilDetailsButtonAndTabsMap().containsKey(detailsButton)) {
						warning.show(detailsButton, pH.getX(), pH.getY());
					}
					break;
					
				case VERTICAL_TAIL:
					Point2D pV = detailsButton
					.localToScreen(
							-2.5*detailsButton.getLayoutBounds().getMaxX(),
							1.2*detailsButton.getLayoutBounds().getMaxY()
							);
					if(theController.getvTailAirfoilDetailsButtonAndTabsMap().containsKey(detailsButton)) {
						warning.show(detailsButton, pV.getX(), pV.getY());
					}
					break;
					
				case CANARD:
					Point2D pC = detailsButton
					.localToScreen(
							-2.5*detailsButton.getLayoutBounds().getMaxX(),
							1.2*detailsButton.getLayoutBounds().getMaxY()
							);
					if(theController.getCanardAirfoilDetailsButtonAndTabsMap().containsKey(detailsButton)) {
						warning.show(detailsButton, pC.getX(), pC.getY());
					}
					break;

				default:
					break;
				}

			}
		});

		detailsButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				warning.hide();
			}
		});

		detailsButton.disableProperty().bind(
				Bindings.isEmpty(airfoilPathTextField.textProperty())
				);	

	}

	public void setShowEngineDataAction (RadioButton radioButton, int indexOfEngineTab, EngineTypeEnum type) {

		radioButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				switch (type) {
				case TURBOFAN:
					if (theController.getPowerPlantPaneMap().containsKey(indexOfEngineTab)) {
						if(theController.getPowerPlantEngineTypePaneMap().get(indexOfEngineTab).containsKey(EngineTypeEnum.TURBOFAN))
							theController.getPowerPlantBorderPaneMap().get(indexOfEngineTab).setCenter(
									theController.getPowerPlantEngineTypePaneMap().get(indexOfEngineTab).get(EngineTypeEnum.TURBOFAN)
									);
						else
							theController.showTurbojetTurboFanDataRadioButton(indexOfEngineTab);
					}
					else
						theController.showTurbojetTurboFanDataRadioButton(indexOfEngineTab);
					break;
				case TURBOPROP:
					if (theController.getPowerPlantPaneMap().containsKey(indexOfEngineTab)) {
						if(theController.getPowerPlantEngineTypePaneMap().get(indexOfEngineTab).containsKey(EngineTypeEnum.TURBOPROP))
							theController.getPowerPlantBorderPaneMap().get(indexOfEngineTab).setCenter(
									theController.getPowerPlantEngineTypePaneMap().get(indexOfEngineTab).get(EngineTypeEnum.TURBOPROP)
									);
						else
							theController.showTurbopropDataRadioButton(indexOfEngineTab);
					}
					else
						theController.showTurbopropDataRadioButton(indexOfEngineTab);
					break;
				case PISTON:
					if (theController.getPowerPlantPaneMap().containsKey(indexOfEngineTab)) {
						if(theController.getPowerPlantEngineTypePaneMap().get(indexOfEngineTab).containsKey(EngineTypeEnum.PISTON))
							theController.getPowerPlantBorderPaneMap().get(indexOfEngineTab).setCenter(
									theController.getPowerPlantEngineTypePaneMap().get(indexOfEngineTab).get(EngineTypeEnum.PISTON)
									);
						else
							theController.showPistonDataRadioButton(indexOfEngineTab);
					}
					else
						theController.showPistonDataRadioButton(indexOfEngineTab);
					break;
				default:
					break;
				}
			}
		});
	}
	
	public void setEstimateNacelleGeometryAction (Button estimateButton, Tab currentTab) {

		estimateButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				int tabIndex = theController.getTabPaneNacelles().getTabs().indexOf(currentTab);	

				NacelleCreator currentNacelle = Main.getTheAircraft().getNacelles().getNacellesList().get(tabIndex);

				currentNacelle.estimateDimensions(currentNacelle.getTheEngine());

				theController.getTextFieldNacelleLengthList().get(tabIndex).clear();
				theController.getTextFieldNacelleLengthList().get(tabIndex).setText(
						String.valueOf(currentNacelle.getLength().getEstimatedValue()));

				if(currentNacelle.getLength().getUnit().toString().equalsIgnoreCase("m"))
					theController.getChoiceBoxNacelleLengthUnitList().get(tabIndex).getSelectionModel().select(0);
				else if(currentNacelle.getLength().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getChoiceBoxNacelleLengthUnitList().get(tabIndex).getSelectionModel().select(1);

				theController.getTextFieldNacelleMaximumDiameterList().get(tabIndex).clear();
				theController.getTextFieldNacelleMaximumDiameterList().get(tabIndex).setText(
						String.valueOf(currentNacelle.getDiameterMax().getEstimatedValue()));

				if(currentNacelle.getDiameterMax().getUnit().toString().equalsIgnoreCase("m"))
					theController.getChoiceBoxNacelleMaximumDiameterUnitList().get(tabIndex).getSelectionModel().select(0);
				else if(currentNacelle.getDiameterMax().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getChoiceBoxNacelleMaximumDiameterUnitList().get(tabIndex).getSelectionModel().select(1);

			}
		});

	}
	
	public void aircraftLoadButtonDisableCheck () {
		
		//.......................................................................................
		// CHECK IF THE AIRCRAFT FILE TEXT FIELD IS NOT EMPTY
		theController.getLoadAircraftButton().disableProperty().bind(
				Bindings.isEmpty(theController.getTextFieldAircraftInputFile().textProperty())
				);
		
		// CHECK IF THE FILE IN TEXTFIELD IS AN AIRCRAFT
        final Tooltip warning = new Tooltip("WARNING : The selected file is not an aircraft !!");
        theController.getLoadAircraftButton().setOnMouseEntered(new EventHandler<MouseEvent>() {
        	
        	@Override
        	public void handle(MouseEvent event) {
        		Point2D p = theController.getLoadAircraftButton()
        				.localToScreen(
        						-2.5*theController.getLoadAircraftButton().getLayoutBounds().getMaxX(),
        						1.2*theController.getLoadAircraftButton().getLayoutBounds().getMaxY()
        						);
        		if(!isAircraftFile(theController.getTextFieldAircraftInputFile().getText())
        				) {
        			warning.show(theController.getLoadAircraftButton(), p.getX(), p.getY());
        		}
        	}
        });
        theController.getLoadAircraftButton().setOnMouseExited(new EventHandler<MouseEvent>() {
        	
        	@Override
        	public void handle(MouseEvent event) {
        		warning.hide();
        	}
        });
	}
	
	public void checkCabinConfigurationClassesNumber() {
		
		theController.getValidation().registerValidator(
				theController.getTextFieldClassesNumber(),
				false,
				Validator.createPredicateValidator(
						o -> {
							if(theController.getTextFieldClassesNumber().getText().equals("") 
									|| !StringUtils.isNumeric(theController.getTextFieldClassesNumber().getText())
									|| theController.getTextFieldClassesNumber().getText().length() > 1
									)
								return false;
							else
								return Integer.valueOf(theController.getTextFieldClassesNumber().getText()) <= 3;
						},
						"The maximum number of classes should be less than or equal to 3",
						Severity.WARNING
						)
				);
	}
	
	public void cabinConfigurationClassesNumberDisableCheck () {

		BooleanBinding cabinConfigurationClassesTypeChoiceBox1Binding = 
				theController.getTextFieldClassesNumber().textProperty().isNotEqualTo("1")
				.and(theController.getTextFieldClassesNumber().textProperty().isNotEqualTo("2"))
				.and(theController.getTextFieldClassesNumber().textProperty().isNotEqualTo("3"));
		BooleanBinding cabinConfigurationClassesTypeChoiceBox2Binding = 
				theController.getTextFieldClassesNumber().textProperty().isNotEqualTo("2")
				.and(theController.getTextFieldClassesNumber().textProperty().isNotEqualTo("3"));
		BooleanBinding cabinConfigurationClassesTypeChoiceBox3Binding = 
				theController.getTextFieldClassesNumber().textProperty().isNotEqualTo("3");
		
		
		theController.getCabinConfigurationClassesTypeChoiceBox1().disableProperty().bind(
				cabinConfigurationClassesTypeChoiceBox1Binding
				);
		theController.getCabinConfigurationClassesTypeChoiceBox2().disableProperty().bind(
				cabinConfigurationClassesTypeChoiceBox2Binding
				);
		theController.getCabinConfigurationClassesTypeChoiceBox3().disableProperty().bind(
				cabinConfigurationClassesTypeChoiceBox3Binding
				);
		
	}
	
	public void equivalentWingDisableCheck () {

		// disable equivalent wing if the check-box is not checked
		theController.getTextFieldEquivalentWingArea().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getTextFieldEquivalentWingAspectRatio().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getTextFieldEquivalentWingKinkPosition().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getTextFieldEquivalentWingSweepLeadingEdge().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getTextFieldEquivalentWingTwistAtTip().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getTextFieldEquivalentWingDihedral().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getTextFieldEquivalentWingTaperRatio().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getTextFieldEquivalentWingRootXOffsetLE().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getTextFieldEquivalentWingRootXOffsetTE().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getTextFieldEquivalentWingAirfoilRootPath().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getTextFieldEquivalentWingAirfoilKinkPath().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getTextFieldEquivalentWingAirfoilTipPath().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getEquivalentWingAreaUnitChoiceBox().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getEquivalentWingSweepLEUnitChoiceBox().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getEquivalentWingTwistAtTipUnitChoiceBox().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getEquivalentWingDihedralUnitChoiceBox().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getEquivalentWingAirfoilRootDetailButton().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getEquivalentWingChooseAirfoilRootButton().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getEquivalentWingAirfoilKinkDetailButton().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getEquivalentWingChooseAirfoilKinkButton().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getEquivalentWingAirfoilTipDetailButton().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getEquivalentWingChooseAirfoilTipButton().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getEquivalentWingInfoButton().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getEquivalentWingRootXOffsetLEInfoButton().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		theController.getEquivalentWingRootXOffseTLEInfoButton().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty().not());
		
		// disable the panels tab pane if the check-box is checked
		theController.getTabPaneWingPanels().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty());
		theController.getWingAddPanelButton().disableProperty().bind(theController.getEquivalentWingCheckBox().selectedProperty());
		
	}
	
	public void addAircraftEngineImplementation() {
		
		Tab newEngineTab = new Tab("Engine " + (theController.getTabPaneAircraftEngines().getTabs().size()+1));
		Pane contentPane = new Pane();
		
		Label engineFileLabel = new Label("File:");
		engineFileLabel.setFont(Font.font("System", 15));
		engineFileLabel.setLayoutX(6.0);
		engineFileLabel.setLayoutY(0.0);
		contentPane.getChildren().add(engineFileLabel);
		
		TextField engineFileTextField = new TextField();
		engineFileTextField.setLayoutX(6.0);
		engineFileTextField.setLayoutY(21);
		engineFileTextField.setPrefWidth(340);
		engineFileTextField.setPrefHeight(31);
		contentPane.getChildren().add(engineFileTextField);
		
		Button engineChooseFileButton = new Button("...");
		engineChooseFileButton.setLayoutX(348);
		engineChooseFileButton.setLayoutY(21);
		engineChooseFileButton.setPrefWidth(44);
		engineChooseFileButton.setPrefHeight(31);
		engineChooseFileButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				
				try {
					chooseAircraftEngineFile(theController.getTabPaneAircraftEngines().getTabs().size()-1);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		});
		contentPane.getChildren().add(engineChooseFileButton);
		
		Label engineXPositionLabel = new Label("X:");
		engineXPositionLabel.setFont(Font.font("System", 15));
		engineXPositionLabel.setLayoutX(6.0);
		engineXPositionLabel.setLayoutY(52.0);
		contentPane.getChildren().add(engineXPositionLabel);
		
		TextField engineXPositionTextField = new TextField();
		engineXPositionTextField.setLayoutX(6.0);
		engineXPositionTextField.setLayoutY(73);
		engineXPositionTextField.setPrefWidth(340);
		engineXPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(engineXPositionTextField);
		
		ChoiceBox<String> engineXChoiceBox = new ChoiceBox<String>();
		engineXChoiceBox.setLayoutX(348.0);
		engineXChoiceBox.setLayoutY(74);
		engineXChoiceBox.setPrefWidth(47);
		engineXChoiceBox.setPrefHeight(30);
		engineXChoiceBox.setItems(theController.getLengthUnitsList());
		contentPane.getChildren().add(engineXChoiceBox);
		
		Label engineYPositionLabel = new Label("Y:");
		engineYPositionLabel.setFont(Font.font("System", 15));
		engineYPositionLabel.setLayoutX(6.0);
		engineYPositionLabel.setLayoutY(104.0);
		contentPane.getChildren().add(engineYPositionLabel);
		
		TextField engineYPositionTextField = new TextField();
		engineYPositionTextField.setLayoutX(6.0);
		engineYPositionTextField.setLayoutY(125);
		engineYPositionTextField.setPrefWidth(340);
		engineYPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(engineYPositionTextField);
		
		ChoiceBox<String> engineYChoiceBox = new ChoiceBox<String>();
		engineYChoiceBox.setLayoutX(348.0);
		engineYChoiceBox.setLayoutY(126);
		engineYChoiceBox.setPrefWidth(47);
		engineYChoiceBox.setPrefHeight(30);
		engineYChoiceBox.setItems(theController.getLengthUnitsList());
		contentPane.getChildren().add(engineYChoiceBox);
		
		Label engineZPositionLabel = new Label("Z:");
		engineZPositionLabel.setFont(Font.font("System", 15));
		engineZPositionLabel.setLayoutX(7.0);
		engineZPositionLabel.setLayoutY(156.0);
		contentPane.getChildren().add(engineZPositionLabel);
		
		TextField engineZPositionTextField = new TextField();
		engineZPositionTextField.setLayoutX(7.0);
		engineZPositionTextField.setLayoutY(177);
		engineZPositionTextField.setPrefWidth(340);
		engineZPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(engineZPositionTextField);
		
		ChoiceBox<String> engineZChoiceBox = new ChoiceBox<String>();
		engineZChoiceBox.setLayoutX(348.0);
		engineZChoiceBox.setLayoutY(178);
		engineZChoiceBox.setPrefWidth(47);
		engineZChoiceBox.setPrefHeight(30);
		engineZChoiceBox.setItems(theController.getLengthUnitsList());
		contentPane.getChildren().add(engineZChoiceBox);
		
		Label engineTiltAngleLabel = new Label("Tilting Angle:");
		engineTiltAngleLabel.setFont(Font.font("System", 15));
		engineTiltAngleLabel.setLayoutX(7.0);
		engineTiltAngleLabel.setLayoutY(208.0);
		contentPane.getChildren().add(engineTiltAngleLabel);
		
		TextField engineTiltAngleTextField = new TextField();
		engineTiltAngleTextField.setLayoutX(7.0);
		engineTiltAngleTextField.setLayoutY(229);
		engineTiltAngleTextField.setPrefWidth(340);
		engineTiltAngleTextField.setPrefHeight(31);
		contentPane.getChildren().add(engineTiltAngleTextField);
		
		ChoiceBox<String> engineTiltAngleChoiceBox = new ChoiceBox<String>();
		engineTiltAngleChoiceBox.setLayoutX(348.0);
		engineTiltAngleChoiceBox.setLayoutY(230);
		engineTiltAngleChoiceBox.setPrefWidth(47);
		engineTiltAngleChoiceBox.setPrefHeight(30);
		engineTiltAngleChoiceBox.setItems(theController.getAngleUnitsList());
		contentPane.getChildren().add(engineTiltAngleChoiceBox);
		
		Label engineMountingPositionLabel = new Label("Position:");
		engineMountingPositionLabel.setFont(Font.font("System", 15));
		engineMountingPositionLabel.setLayoutX(7.0);
		engineMountingPositionLabel.setLayoutY(260.0);
		contentPane.getChildren().add(engineMountingPositionLabel);
		
		ChoiceBox<String> engineMountingPositionChoiceBox = new ChoiceBox<String>();
		engineMountingPositionChoiceBox.setLayoutX(6.0);
		engineMountingPositionChoiceBox.setLayoutY(282);
		engineMountingPositionChoiceBox.setPrefWidth(340);
		engineMountingPositionChoiceBox.setPrefHeight(31);
		engineMountingPositionChoiceBox.setItems(theController.getPowerPlantMountingPositionTypeList());
		contentPane.getChildren().add(engineMountingPositionChoiceBox);
		
		theController.getTextFieldsAircraftEngineFileList().add(engineFileTextField);
		theController.getTextFieldAircraftEngineXList().add(engineXPositionTextField);
		theController.getTextFieldAircraftEngineYList().add(engineYPositionTextField);
		theController.getTextFieldAircraftEngineZList().add(engineZPositionTextField);
		theController.getTextFieldAircraftEngineTiltList().add(engineTiltAngleTextField);
		theController.getChoiceBoxesAircraftEnginePositonList().add(engineMountingPositionChoiceBox);
		
		theController.getChoiceBoxAircraftEngineXUnitList().add(engineXChoiceBox);
		theController.getChoiceBoxAircraftEngineYUnitList().add(engineYChoiceBox);
		theController.getChoiceBoxAircraftEngineZUnitList().add(engineZChoiceBox);
		theController.getChoiceBoxAircraftEngineTiltUnitList().add(engineTiltAngleChoiceBox);
		
		newEngineTab.setContent(contentPane);
		theController.getTabPaneAircraftEngines().getTabs().add(newEngineTab);
		
	}
	
	public void addAircraftNacelleImplementation() {
		
		Tab newNacelleTab = new Tab("Nacelle " + (theController.getTabPaneAircraftNacelles().getTabs().size()+1));
		Pane contentPane = new Pane();
		
		Label nacelleFileLabel = new Label("File:");
		nacelleFileLabel.setFont(Font.font("System", 15));
		nacelleFileLabel.setLayoutX(6.0);
		nacelleFileLabel.setLayoutY(0.0);
		contentPane.getChildren().add(nacelleFileLabel);
		
		TextField nacelleFileTextField = new TextField();
		nacelleFileTextField.setLayoutX(6.0);
		nacelleFileTextField.setLayoutY(21);
		nacelleFileTextField.setPrefWidth(340);
		nacelleFileTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleFileTextField);
		
		Button nacelleChooseFileButton = new Button("...");
		nacelleChooseFileButton.setLayoutX(348);
		nacelleChooseFileButton.setLayoutY(21);
		nacelleChooseFileButton.setPrefWidth(44);
		nacelleChooseFileButton.setPrefHeight(31);
		nacelleChooseFileButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				
				try {
					chooseAircraftNacelleFile(theController.getTabPaneAircraftNacelles().getTabs().size()-1);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		});
		contentPane.getChildren().add(nacelleChooseFileButton);
		
		Label nacelleXPositionLabel = new Label("X:");
		nacelleXPositionLabel.setFont(Font.font("System", 15));
		nacelleXPositionLabel.setLayoutX(6.0);
		nacelleXPositionLabel.setLayoutY(52.0);
		contentPane.getChildren().add(nacelleXPositionLabel);
		
		TextField nacelleXPositionTextField = new TextField();
		nacelleXPositionTextField.setLayoutX(6.0);
		nacelleXPositionTextField.setLayoutY(73);
		nacelleXPositionTextField.setPrefWidth(340);
		nacelleXPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleXPositionTextField);
		
		ChoiceBox<String> nacelleXChoiceBox = new ChoiceBox<String>();
		nacelleXChoiceBox.setLayoutX(348.0);
		nacelleXChoiceBox.setLayoutY(74);
		nacelleXChoiceBox.setPrefWidth(47);
		nacelleXChoiceBox.setPrefHeight(30);
		nacelleXChoiceBox.setItems(theController.getLengthUnitsList());
		contentPane.getChildren().add(nacelleXChoiceBox);
		
		Label nacelleYPositionLabel = new Label("Y:");
		nacelleYPositionLabel.setFont(Font.font("System", 15));
		nacelleYPositionLabel.setLayoutX(6.0);
		nacelleYPositionLabel.setLayoutY(104.0);
		contentPane.getChildren().add(nacelleYPositionLabel);
		
		TextField nacelleYPositionTextField = new TextField();
		nacelleYPositionTextField.setLayoutX(6.0);
		nacelleYPositionTextField.setLayoutY(125);
		nacelleYPositionTextField.setPrefWidth(340);
		nacelleYPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleYPositionTextField);
		
		ChoiceBox<String> nacelleYChoiceBox = new ChoiceBox<String>();
		nacelleYChoiceBox.setLayoutX(348.0);
		nacelleYChoiceBox.setLayoutY(126);
		nacelleYChoiceBox.setPrefWidth(47);
		nacelleYChoiceBox.setPrefHeight(30);
		nacelleYChoiceBox.setItems(theController.getLengthUnitsList());
		contentPane.getChildren().add(nacelleYChoiceBox);
		
		Label nacelleZPositionLabel = new Label("Z:");
		nacelleZPositionLabel.setFont(Font.font("System", 15));
		nacelleZPositionLabel.setLayoutX(7.0);
		nacelleZPositionLabel.setLayoutY(156.0);
		contentPane.getChildren().add(nacelleZPositionLabel);
		
		TextField nacelleZPositionTextField = new TextField();
		nacelleZPositionTextField.setLayoutX(7.0);
		nacelleZPositionTextField.setLayoutY(177);
		nacelleZPositionTextField.setPrefWidth(340);
		nacelleZPositionTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleZPositionTextField);
		
		ChoiceBox<String> nacelleZChoiceBox = new ChoiceBox<String>();
		nacelleZChoiceBox.setLayoutX(348.0);
		nacelleZChoiceBox.setLayoutY(178);
		nacelleZChoiceBox.setPrefWidth(47);
		nacelleZChoiceBox.setPrefHeight(30);
		nacelleZChoiceBox.setItems(theController.getLengthUnitsList());
		contentPane.getChildren().add(nacelleZChoiceBox);
		
		Label nacelleMountingPositionLabel = new Label("Position:");
		nacelleMountingPositionLabel.setFont(Font.font("System", 15));
		nacelleMountingPositionLabel.setLayoutX(6.0);
		nacelleMountingPositionLabel.setLayoutY(208.0);
		contentPane.getChildren().add(nacelleMountingPositionLabel);
		
		ChoiceBox<String> nacelleMountingPositionChoiceBox = new ChoiceBox<String>();
		nacelleMountingPositionChoiceBox.setLayoutX(6.0);
		nacelleMountingPositionChoiceBox.setLayoutY(230);
		nacelleMountingPositionChoiceBox.setPrefWidth(340);
		nacelleMountingPositionChoiceBox.setPrefHeight(31);
		nacelleMountingPositionChoiceBox.setItems(theController.getNacelleMountingPositionTypeList());
		contentPane.getChildren().add(nacelleMountingPositionChoiceBox);
		
		theController.getTextFieldsAircraftNacelleFileList().add(nacelleFileTextField);
		theController.getTextFieldAircraftNacelleXList().add(nacelleXPositionTextField);
		theController.getTextFieldAircraftNacelleYList().add(nacelleYPositionTextField);
		theController.getTextFieldAircraftNacelleZList().add(nacelleZPositionTextField);
		theController.getChoiceBoxesAircraftNacellePositonList().add(nacelleMountingPositionChoiceBox);
		
		theController.getChoiceBoxAircraftNacelleXUnitList().add(nacelleXChoiceBox);
		theController.getChoiceBoxAircraftNacelleYUnitList().add(nacelleYChoiceBox);
		theController.getChoiceBoxAircraftNacelleZUnitList().add(nacelleZChoiceBox);
		
		newNacelleTab.setContent(contentPane);
		theController.getTabPaneAircraftNacelles().getTabs().add(newNacelleTab);
		
	}
	
	public void addNacelleImplementation() {
		
		Tab newNacelleTab = new Tab("Nacelle " + (theController.getTabPaneNacelles().getTabs().size()+1));
		Pane contentPane = new Pane();
		
		Label nacelleGlobalDataLabel = new Label("GLOBAL DATA:");
		nacelleGlobalDataLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleGlobalDataLabel.setTextFill(Paint.valueOf("#0400ff"));
		nacelleGlobalDataLabel.setLayoutX(6.0);
		nacelleGlobalDataLabel.setLayoutY(6.0);
		contentPane.getChildren().add(nacelleGlobalDataLabel);
		
		Separator globalDataLowerSeparator = new Separator();
		globalDataLowerSeparator.setLayoutX(-20);
		globalDataLowerSeparator.setLayoutY(33);
		globalDataLowerSeparator.setPrefWidth(1312);
		contentPane.getChildren().add(globalDataLowerSeparator);
		
		Label nacelleRoughnessLabel = new Label("Roughness:");
		nacelleRoughnessLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleRoughnessLabel.setLayoutX(6.0);
		nacelleRoughnessLabel.setLayoutY(34.0);
		contentPane.getChildren().add(nacelleRoughnessLabel);
		
		TextField nacelleRoughnessTextField = new TextField();
		nacelleRoughnessTextField.setLayoutX(6.0);
		nacelleRoughnessTextField.setLayoutY(55);
		nacelleRoughnessTextField.setPrefWidth(340);
		nacelleRoughnessTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleRoughnessTextField);
		
		ChoiceBox<String> nacelleRoughnessChoiceBox = new ChoiceBox<String>();
		nacelleRoughnessChoiceBox.setLayoutX(348.0);
		nacelleRoughnessChoiceBox.setLayoutY(56);
		nacelleRoughnessChoiceBox.setPrefWidth(47);
		nacelleRoughnessChoiceBox.setPrefHeight(30);
		nacelleRoughnessChoiceBox.setItems(theController.getLengthUnitsList());
		contentPane.getChildren().add(nacelleRoughnessChoiceBox);
		
		Separator roughnessLowerSeparator = new Separator();
		roughnessLowerSeparator.setLayoutX(0);
		roughnessLowerSeparator.setLayoutY(95);
		roughnessLowerSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(roughnessLowerSeparator);
		
		Label nacelleGeometryLabel = new Label("GEOMETRY:");
		nacelleGeometryLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleGeometryLabel.setTextFill(Paint.valueOf("#0400ff"));
		nacelleGeometryLabel.setLayoutX(6.0);
		nacelleGeometryLabel.setLayoutY(103.0);
		contentPane.getChildren().add(nacelleGeometryLabel);
		
		Button nacelleEstimateGeometryButton = new Button("Estimate");
		nacelleEstimateGeometryButton.setLayoutX(100);
		nacelleEstimateGeometryButton.setLayoutY(98);
		nacelleEstimateGeometryButton.setPrefWidth(77);
		nacelleEstimateGeometryButton.setPrefHeight(31);
		setEstimateNacelleGeometryAction(nacelleEstimateGeometryButton, newNacelleTab);
		contentPane.getChildren().add(nacelleEstimateGeometryButton);
		
		Separator geometryLowerSeparator = new Separator();
		geometryLowerSeparator.setLayoutX(-28);
		geometryLowerSeparator.setLayoutY(131);
		geometryLowerSeparator.setPrefWidth(1345);
		contentPane.getChildren().add(geometryLowerSeparator);
		
		Label nacelleLengthLabel = new Label("Length:");
		nacelleLengthLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleLengthLabel.setLayoutX(6.0);
		nacelleLengthLabel.setLayoutY(135.0);
		contentPane.getChildren().add(nacelleLengthLabel);
		
		TextField nacelleLengthTextField = new TextField();
		nacelleLengthTextField.setLayoutX(6.0);
		nacelleLengthTextField.setLayoutY(158);
		nacelleLengthTextField.setPrefWidth(340);
		nacelleLengthTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleLengthTextField);
		
		ChoiceBox<String> nacelleLengthChoiceBox = new ChoiceBox<String>();
		nacelleLengthChoiceBox.setLayoutX(348.0);
		nacelleLengthChoiceBox.setLayoutY(158);
		nacelleLengthChoiceBox.setPrefWidth(47);
		nacelleLengthChoiceBox.setPrefHeight(30);
		nacelleLengthChoiceBox.setItems(theController.getLengthUnitsList());
		contentPane.getChildren().add(nacelleLengthChoiceBox);
		
		Label nacelleMaximumDiameterLabel = new Label("Maximum Diameter:");
		nacelleMaximumDiameterLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleMaximumDiameterLabel.setLayoutX(6.0);
		nacelleMaximumDiameterLabel.setLayoutY(189.0);
		contentPane.getChildren().add(nacelleMaximumDiameterLabel);
		
		TextField nacelleMaximumDiameterTextField = new TextField();
		nacelleMaximumDiameterTextField.setLayoutX(6.0);
		nacelleMaximumDiameterTextField.setLayoutY(210);
		nacelleMaximumDiameterTextField.setPrefWidth(340);
		nacelleMaximumDiameterTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleMaximumDiameterTextField);
		
		ChoiceBox<String> nacelleMaximumDiameterChoiceBox = new ChoiceBox<String>();
		nacelleMaximumDiameterChoiceBox.setLayoutX(348.0);
		nacelleMaximumDiameterChoiceBox.setLayoutY(211);
		nacelleMaximumDiameterChoiceBox.setPrefWidth(47);
		nacelleMaximumDiameterChoiceBox.setPrefHeight(30);
		nacelleMaximumDiameterChoiceBox.setItems(theController.getLengthUnitsList());
		contentPane.getChildren().add(nacelleMaximumDiameterChoiceBox);
		
		Label nacelleKInletLabel = new Label("k Inlet:");
		nacelleKInletLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleKInletLabel.setLayoutX(6.0);
		nacelleKInletLabel.setLayoutY(245.0);
		contentPane.getChildren().add(nacelleKInletLabel);
		
		TextField nacelleKInletTextField = new TextField();
		nacelleKInletTextField.setLayoutX(6.0);
		nacelleKInletTextField.setLayoutY(266);
		nacelleKInletTextField.setPrefWidth(340);
		nacelleKInletTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleKInletTextField);
		
		Button nacelleKInletInfoButton = new Button("?");
		nacelleKInletInfoButton.setStyle("-fx-font-size: 8;");
		nacelleKInletInfoButton.setStyle("-fx-font-weight: bold;");
		nacelleKInletInfoButton.setLayoutX(63);
		nacelleKInletInfoButton.setLayoutY(245);
		nacelleKInletInfoButton.setPrefWidth(16);
		nacelleKInletInfoButton.setPrefHeight(18);
		nacelleKInletInfoButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				theController.showNacelleKInletInfo();
			}
		});
		contentPane.getChildren().add(nacelleKInletInfoButton);
		
		Label nacelleKOutletLabel = new Label("k Outlet:");
		nacelleKOutletLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleKOutletLabel.setLayoutX(6.0); 
		nacelleKOutletLabel.setLayoutY(304.0);
		contentPane.getChildren().add(nacelleKOutletLabel);
		
		TextField nacelleKOutletTextField = new TextField();
		nacelleKOutletTextField.setLayoutX(6.0);
		nacelleKOutletTextField.setLayoutY(327);
		nacelleKOutletTextField.setPrefWidth(340);
		nacelleKOutletTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleKOutletTextField);
		
		Button nacelleKOutletInfoButton = new Button("?");
		nacelleKOutletInfoButton.setStyle("-fx-font-size: 8;");
		nacelleKOutletInfoButton.setStyle("-fx-font-weight: bold;");
		nacelleKOutletInfoButton.setLayoutX(71);
		nacelleKOutletInfoButton.setLayoutY(304);
		nacelleKOutletInfoButton.setPrefWidth(16);
		nacelleKOutletInfoButton.setPrefHeight(18);
		nacelleKOutletInfoButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				theController.showNacelleKOutletInfo();
			}
		});
		contentPane.getChildren().add(nacelleKOutletInfoButton);
		
		Label nacelleKLengthLabel = new Label("k Length:");
		nacelleKLengthLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleKLengthLabel.setLayoutX(6.0);
		nacelleKLengthLabel.setLayoutY(364.0);
		contentPane.getChildren().add(nacelleKLengthLabel);
		
		TextField nacelleKLengthTextField = new TextField();
		nacelleKLengthTextField.setLayoutX(6.0);
		nacelleKLengthTextField.setLayoutY(389);
		nacelleKLengthTextField.setPrefWidth(340);
		nacelleKLengthTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleKLengthTextField);
		
		Button nacelleKLengthInfoButton = new Button("?");
		nacelleKLengthInfoButton.setStyle("-fx-font-size: 8;");
		nacelleKLengthInfoButton.setStyle("-fx-font-weight: bold;");
		nacelleKLengthInfoButton.setLayoutX(75);
		nacelleKLengthInfoButton.setLayoutY(365);
		nacelleKLengthInfoButton.setPrefWidth(16);
		nacelleKLengthInfoButton.setPrefHeight(18);
		nacelleKLengthInfoButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				theController.showNacelleKLengthInfo();
			}
		});
		contentPane.getChildren().add(nacelleKLengthInfoButton);
		
		Label nacelleKDiameterOutletLabel = new Label("k Diameter Outlet:");
		nacelleKDiameterOutletLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		nacelleKDiameterOutletLabel.setLayoutX(6.0);
		nacelleKDiameterOutletLabel.setLayoutY(430.0);
		contentPane.getChildren().add(nacelleKDiameterOutletLabel);
		
		TextField nacelleKDiameterOutletTextField = new TextField();
		nacelleKDiameterOutletTextField.setLayoutX(6.0);
		nacelleKDiameterOutletTextField.setLayoutY(452);
		nacelleKDiameterOutletTextField.setPrefWidth(340);
		nacelleKDiameterOutletTextField.setPrefHeight(31);
		contentPane.getChildren().add(nacelleKDiameterOutletTextField);
		
		Button nacelleKDiameterOutletInfoButton = new Button("?");
		nacelleKDiameterOutletInfoButton.setStyle("-fx-font-size: 8;");
		nacelleKDiameterOutletInfoButton.setStyle("-fx-font-weight: bold;");
		nacelleKDiameterOutletInfoButton.setLayoutX(137);
		nacelleKDiameterOutletInfoButton.setLayoutY(432);
		nacelleKDiameterOutletInfoButton.setPrefWidth(16);
		nacelleKDiameterOutletInfoButton.setPrefHeight(18);
		nacelleKDiameterOutletInfoButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				theController.showNacelleKDiameterOutletInfo();
			}
		});
		contentPane.getChildren().add(nacelleKDiameterOutletInfoButton);
		
		theController.getTextFieldNacelleRoughnessList().add(nacelleRoughnessTextField);
		theController.getTextFieldNacelleLengthList().add(nacelleLengthTextField);
		theController.getTextFieldNacelleMaximumDiameterList().add(nacelleMaximumDiameterTextField);
		theController.getTextFieldNacelleKInletList().add(nacelleKInletTextField);
		theController.getTextFieldNacelleKOutletList().add(nacelleKOutletTextField);
		theController.getTextFieldNacelleKLengthList().add(nacelleKLengthTextField);
		theController.getTextFieldNacelleKDiameterOutletList().add(nacelleKDiameterOutletTextField);
		theController.getChoiceBoxNacelleRoughnessUnitList().add(nacelleRoughnessChoiceBox);
		theController.getChoiceBoxNacelleLengthUnitList().add(nacelleLengthChoiceBox);
		theController.getChoiceBoxNacelleMaximumDiameterUnitList().add(nacelleMaximumDiameterChoiceBox);
		theController.getNacelleEstimateDimesnsionButtonList().add(nacelleEstimateGeometryButton);
		theController.getNacelleKInletInfoButtonList().add(nacelleKInletInfoButton);
		theController.getNacelleKOutletInfoButtonList().add(nacelleKOutletInfoButton);
		theController.getNacelleKLengthInfoButtonList().add(nacelleKLengthInfoButton);
		theController.getNacelleKDiameterOutletInfoButtonList().add(nacelleKDiameterOutletInfoButton);
		
		newNacelleTab.setContent(contentPane);
		theController.getTabPaneNacelles().getTabs().add(newNacelleTab);
		
	}
	
	public void addEngineImplementation() {
		
		int indexOfEngineTab = theController.getTabPaneEngines().getTabs().size();
		
		Tab newEngineTab = new Tab("Engine" + (theController.getTabPaneEngines().getTabs().size()+1));
		BorderPane contentBorderPane = new BorderPane();
		
		ToggleGroup enigneToggleGroup = new ToggleGroup();
		theController.getPowerPlantToggleGropuList().add(enigneToggleGroup);
		
		RadioButton turbojetTurbofanRadioButton = new RadioButton("Turbofan/Turbojet");
		turbojetTurbofanRadioButton.setPadding(new Insets(0, 30, 0, 0));
		turbojetTurbofanRadioButton.setToggleGroup(enigneToggleGroup);
		turbojetTurbofanRadioButton.setUserData(0);
		theController.getPowerPlantJetRadioButtonList().add(turbojetTurbofanRadioButton);
		setShowEngineDataAction(turbojetTurbofanRadioButton, indexOfEngineTab, EngineTypeEnum.TURBOFAN);
		
		RadioButton turbopropRadioButton = new RadioButton("Turboprop");
		turbopropRadioButton.setPadding(new Insets(0, 30, 0, 0));
		turbopropRadioButton.setToggleGroup(enigneToggleGroup);
		turbopropRadioButton.setUserData(1);
		theController.getPowerPlantTurbopropRadioButtonList().add(turbopropRadioButton);
		setShowEngineDataAction(turbopropRadioButton, indexOfEngineTab, EngineTypeEnum.TURBOPROP);
		
		RadioButton pistonRadioButton = new RadioButton("Piston");
		pistonRadioButton.setPadding(new Insets(0, 30, 0, 0));
		pistonRadioButton.setToggleGroup(enigneToggleGroup);
		pistonRadioButton.setUserData(2);
		theController.getPowerPlantPistonRadioButtonList().add(pistonRadioButton);
		setShowEngineDataAction(pistonRadioButton, indexOfEngineTab, EngineTypeEnum.PISTON);
		
		ToolBar engineTypeToolBar = new ToolBar();
		engineTypeToolBar.setPrefWidth(200);
		engineTypeToolBar.setPrefHeight(40);
		engineTypeToolBar.getItems().add(turbojetTurbofanRadioButton);
		engineTypeToolBar.getItems().add(turbopropRadioButton);
		engineTypeToolBar.getItems().add(pistonRadioButton);
		
		Pane engineDataPane = new Pane();
		
		contentBorderPane.setTop(engineTypeToolBar);
		contentBorderPane.setCenter(engineDataPane);
		theController.getPowerPlantBorderPaneMap().put(indexOfEngineTab, contentBorderPane);
		newEngineTab.setContent(contentBorderPane);
		theController.getTabPaneEngines().getTabs().add(newEngineTab);
		
	}
	
	public void showTurbojetTurboFanDataRadioButtonImplementation (int indexOfEngineTab) {
		
		Pane engineDataPane = new Pane();

		Label engineTypeLabel = new Label("Type:");
		engineTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineTypeLabel.setLayoutX(6.0);
		engineTypeLabel.setLayoutY(0.0);
		engineDataPane.getChildren().add(engineTypeLabel);
		
		ChoiceBox<String> engineTypeChoiceBox = new ChoiceBox<String>();
		engineTypeChoiceBox.setLayoutX(6.0);
		engineTypeChoiceBox.setLayoutY(21.0);
		engineTypeChoiceBox.setPrefWidth(340);
		engineTypeChoiceBox.setPrefHeight(31);
		engineTypeChoiceBox.setItems(theController.getJetEngineTypeList());
		engineDataPane.getChildren().add(engineTypeChoiceBox);
		theController.getEngineTurbojetTurbofanTypeChoiceBoxMap().put(indexOfEngineTab, engineTypeChoiceBox); 
		
		Label engineDatabaseLabel = new Label("Database:");
		engineDatabaseLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineDatabaseLabel.setLayoutX(6.0);
		engineDatabaseLabel.setLayoutY(52.0);
		engineDataPane.getChildren().add(engineDatabaseLabel);
		
		TextField engineDatabasePathTextField = new TextField();
		engineDatabasePathTextField.setLayoutX(6.0);
		engineDatabasePathTextField.setLayoutY(73);
		engineDatabasePathTextField.setPrefWidth(340);
		engineDatabasePathTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineDatabasePathTextField);
		theController.getEngineTurbojetTurbofanDatabaseTextFieldMap().put(indexOfEngineTab, engineDatabasePathTextField);
		
		Button engineDatabasePathChooseButton = new Button("...");
		engineDatabasePathChooseButton.setLayoutX(350.0);
		engineDatabasePathChooseButton.setLayoutY(73);
		engineDatabasePathChooseButton.setPrefWidth(24);
		engineDatabasePathChooseButton.setPrefHeight(31);
		engineDatabasePathChooseButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				theController.chooseEngineDatabase(engineDatabasePathTextField);
			}
		});
		engineDataPane.getChildren().add(engineDatabasePathChooseButton);
		
		Label engineLengthLabel = new Label("Length:");
		engineLengthLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineLengthLabel.setLayoutX(6.0);
		engineLengthLabel.setLayoutY(104.0);
		engineDataPane.getChildren().add(engineLengthLabel);
		
		TextField engineLengthTextField = new TextField();
		engineLengthTextField.setLayoutX(6.0);
		engineLengthTextField.setLayoutY(125);
		engineLengthTextField.setPrefWidth(340);
		engineLengthTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineLengthTextField);
		theController.getEngineTurbojetTurbofanLengthTextFieldMap().put(indexOfEngineTab, engineLengthTextField);
		
		ChoiceBox<String> engineLengthChoiceBox = new ChoiceBox<String>();
		engineLengthChoiceBox.setLayoutX(348.0);
		engineLengthChoiceBox.setLayoutY(125);
		engineLengthChoiceBox.setPrefWidth(47);
		engineLengthChoiceBox.setPrefHeight(30);
		engineLengthChoiceBox.setItems(theController.getLengthUnitsList());
		engineDataPane.getChildren().add(engineLengthChoiceBox);
		theController.getEngineTurbojetTurbofanLengthUnitChoiceBoxMap().put(indexOfEngineTab, engineLengthChoiceBox);
		
		Label engineStaticThrustLabel = new Label("Static Thrust:");
		engineStaticThrustLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineStaticThrustLabel.setLayoutX(6.0);
		engineStaticThrustLabel.setLayoutY(159.0);
		engineDataPane.getChildren().add(engineStaticThrustLabel);
		
		TextField engineStaticThrustTextField = new TextField();
		engineStaticThrustTextField.setLayoutX(6.0);
		engineStaticThrustTextField.setLayoutY(180);
		engineStaticThrustTextField.setPrefWidth(340);
		engineStaticThrustTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineStaticThrustTextField);
		theController.getEngineTurbojetTurbofanStaticThrustTextFieldMap().put(indexOfEngineTab, engineStaticThrustTextField);
		
		ChoiceBox<String> engineStaticThrustChoiceBox = new ChoiceBox<String>();
		engineStaticThrustChoiceBox.setLayoutX(348.0);
		engineStaticThrustChoiceBox.setLayoutY(180);
		engineStaticThrustChoiceBox.setPrefWidth(47);
		engineStaticThrustChoiceBox.setPrefHeight(30);
		engineStaticThrustChoiceBox.setItems(theController.getForceUnitsList());
		engineDataPane.getChildren().add(engineStaticThrustChoiceBox);
		theController.getEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap().put(indexOfEngineTab, engineStaticThrustChoiceBox);
		
		Label engineDryMassLabel = new Label("Dry Mass:");
		engineDryMassLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineDryMassLabel.setLayoutX(6.0);
		engineDryMassLabel.setLayoutY(213.0);
		engineDataPane.getChildren().add(engineDryMassLabel);
		
		TextField engineDryMassTextField = new TextField();
		engineDryMassTextField.setLayoutX(6.0);
		engineDryMassTextField.setLayoutY(234);
		engineDryMassTextField.setPrefWidth(340);
		engineDryMassTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineDryMassTextField);
		theController.getEngineTurbojetTurbofanDryMassTextFieldMap().put(indexOfEngineTab, engineDryMassTextField);
		
		ChoiceBox<String> engineDryMassChoiceBox = new ChoiceBox<String>();
		engineDryMassChoiceBox.setLayoutX(348.0);
		engineDryMassChoiceBox.setLayoutY(234);
		engineDryMassChoiceBox.setPrefWidth(47);
		engineDryMassChoiceBox.setPrefHeight(30);
		engineDryMassChoiceBox.setItems(theController.getMassUnitsList());
		engineDataPane.getChildren().add(engineDryMassChoiceBox);
		theController.getEngineTurbojetTurbofanDryMassUnitChoiceBoxMap().put(indexOfEngineTab, engineDryMassChoiceBox);
		
		Button engineDryMassCalculateButton = new Button("Calculate");
		engineDryMassCalculateButton.setLayoutX(397.0);
		engineDryMassCalculateButton.setLayoutY(234);
		engineDryMassCalculateButton.setPrefHeight(31);
		engineDryMassCalculateButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				theController.calculateEngineDryMass(engineDryMassTextField, engineDryMassChoiceBox, indexOfEngineTab);
			}
		});
		engineDataPane.getChildren().add(engineDryMassCalculateButton);
		
		Label engineBPRLabel = new Label("BPR:");
		engineBPRLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineBPRLabel.setLayoutX(6.0);
		engineBPRLabel.setLayoutY(268.0);
		engineDataPane.getChildren().add(engineBPRLabel);
		
		TextField engineBPRTextField = new TextField();
		engineBPRTextField.setLayoutX(6.0);
		engineBPRTextField.setLayoutY(289);
		engineBPRTextField.setPrefWidth(340);
		engineBPRTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineBPRTextField);
		theController.getEngineTurbojetTurbofanBPRTextFieldMap().put(indexOfEngineTab, engineBPRTextField);
		
		Label engineNumberOfCompressorStagesLabel = new Label("Number Of Compressor Stages:");
		engineNumberOfCompressorStagesLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineNumberOfCompressorStagesLabel.setLayoutX(6.0);
		engineNumberOfCompressorStagesLabel.setLayoutY(323.0);
		engineDataPane.getChildren().add(engineNumberOfCompressorStagesLabel);
		
		TextField engineNumberOfCompressorStagesTextField = new TextField();
		engineNumberOfCompressorStagesTextField.setLayoutX(6.0);
		engineNumberOfCompressorStagesTextField.setLayoutY(344);
		engineNumberOfCompressorStagesTextField.setPrefWidth(340);
		engineNumberOfCompressorStagesTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineNumberOfCompressorStagesTextField);
		theController.getEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap().put(indexOfEngineTab, engineNumberOfCompressorStagesTextField);
		
		Label engineNumberOfShaftsLabel = new Label("Number Of Shafts:");
		engineNumberOfShaftsLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineNumberOfShaftsLabel.setLayoutX(6.0);
		engineNumberOfShaftsLabel.setLayoutY(378.0);
		engineDataPane.getChildren().add(engineNumberOfShaftsLabel);
		
		TextField engineNumberOfShaftsTextField = new TextField();
		engineNumberOfShaftsTextField.setLayoutX(6.0);
		engineNumberOfShaftsTextField.setLayoutY(399);
		engineNumberOfShaftsTextField.setPrefWidth(340);
		engineNumberOfShaftsTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineNumberOfShaftsTextField);
		theController.getEngineTurbojetTurbofanNumberOfShaftsTextFieldMap().put(indexOfEngineTab, engineNumberOfShaftsTextField);
		
		Label engineOverallPressureRatioLabel = new Label("Overall Pressure Ratio:");
		engineOverallPressureRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineOverallPressureRatioLabel.setLayoutX(6.0);
		engineOverallPressureRatioLabel.setLayoutY(433.0);
		engineDataPane.getChildren().add(engineOverallPressureRatioLabel);
		
		TextField engineOverallPressureRatioTextField = new TextField();
		engineOverallPressureRatioTextField.setLayoutX(6.0);
		engineOverallPressureRatioTextField.setLayoutY(454);
		engineOverallPressureRatioTextField.setPrefWidth(340);
		engineOverallPressureRatioTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineOverallPressureRatioTextField);
		theController.getEngineTurbojetTurbofanOverallPressureRatioTextFieldMap().put(indexOfEngineTab, engineOverallPressureRatioTextField);
		
		theController.getPowerPlantPaneMap().put(EngineTypeEnum.TURBOFAN, engineDataPane);
		theController.getPowerPlantEngineTypePaneMap().put(indexOfEngineTab, theController.getPowerPlantPaneMap());
		theController.getPowerPlantBorderPaneMap().get(indexOfEngineTab).setCenter(engineDataPane);
		
	}
	
	public void showTurbopropDataRadioButtonImplementation (int indexOfEngineTab) {
		
		Pane engineDataPane = new Pane();
		
		Label engineTypeLabel = new Label("Type:");
		engineTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineTypeLabel.setLayoutX(6.0);
		engineTypeLabel.setLayoutY(0.0);
		engineDataPane.getChildren().add(engineTypeLabel);
		
		ChoiceBox<String> engineTypeChoiceBox = new ChoiceBox<String>();
		engineTypeChoiceBox.setLayoutX(6.0);
		engineTypeChoiceBox.setLayoutY(21.0);
		engineTypeChoiceBox.setPrefWidth(340);
		engineTypeChoiceBox.setPrefHeight(31);
		engineTypeChoiceBox.setItems(theController.getTurbopropEngineTypeList());
		engineDataPane.getChildren().add(engineTypeChoiceBox);
		theController.getEngineTurbopropTypeChoiceBoxMap().put(indexOfEngineTab, engineTypeChoiceBox); 
		
		Label engineDatabaseLabel = new Label("Database:");
		engineDatabaseLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineDatabaseLabel.setLayoutX(6.0);
		engineDatabaseLabel.setLayoutY(52.0);
		engineDataPane.getChildren().add(engineDatabaseLabel);
		
		TextField engineDatabasePathTextField = new TextField();
		engineDatabasePathTextField.setLayoutX(6.0);
		engineDatabasePathTextField.setLayoutY(73);
		engineDatabasePathTextField.setPrefWidth(340);
		engineDatabasePathTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineDatabasePathTextField);
		theController.getEngineTurbopropDatabaseTextFieldMap().put(indexOfEngineTab, engineDatabasePathTextField);
		
		Button engineDatabasePathChooseButton = new Button("...");
		engineDatabasePathChooseButton.setLayoutX(350.0);
		engineDatabasePathChooseButton.setLayoutY(73);
		engineDatabasePathChooseButton.setPrefWidth(24);
		engineDatabasePathChooseButton.setPrefHeight(31);
		engineDatabasePathChooseButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				theController.chooseEngineDatabase(engineDatabasePathTextField);
			}
		});
		engineDataPane.getChildren().add(engineDatabasePathChooseButton);
		
		Label engineLengthLabel = new Label("Length:");
		engineLengthLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineLengthLabel.setLayoutX(6.0);
		engineLengthLabel.setLayoutY(104.0);
		engineDataPane.getChildren().add(engineLengthLabel);
		
		TextField engineLengthTextField = new TextField();
		engineLengthTextField.setLayoutX(6.0);
		engineLengthTextField.setLayoutY(125);
		engineLengthTextField.setPrefWidth(340);
		engineLengthTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineLengthTextField);
		theController.getEngineTurbopropLengthTextFieldMap().put(indexOfEngineTab, engineLengthTextField);
		
		ChoiceBox<String> engineLengthChoiceBox = new ChoiceBox<String>();
		engineLengthChoiceBox.setLayoutX(348.0);
		engineLengthChoiceBox.setLayoutY(125);
		engineLengthChoiceBox.setPrefWidth(47);
		engineLengthChoiceBox.setPrefHeight(30);
		engineLengthChoiceBox.setItems(theController.getLengthUnitsList());
		engineDataPane.getChildren().add(engineLengthChoiceBox);
		theController.getEngineTurbopropLengthUnitChoiceBoxMap().put(indexOfEngineTab, engineLengthChoiceBox);
		
		Label engineStaticPowerLabel = new Label("Static Power:");
		engineStaticPowerLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineStaticPowerLabel.setLayoutX(6.0);
		engineStaticPowerLabel.setLayoutY(159.0);
		engineDataPane.getChildren().add(engineStaticPowerLabel);
		
		TextField engineStaticPowerTextField = new TextField();
		engineStaticPowerTextField.setLayoutX(6.0);
		engineStaticPowerTextField.setLayoutY(180);
		engineStaticPowerTextField.setPrefWidth(340);
		engineStaticPowerTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineStaticPowerTextField);
		theController.getEngineTurbopropStaticPowerTextFieldMap().put(indexOfEngineTab, engineStaticPowerTextField);
		
		ChoiceBox<String> engineStaticPowerChoiceBox = new ChoiceBox<String>();
		engineStaticPowerChoiceBox.setLayoutX(348.0);
		engineStaticPowerChoiceBox.setLayoutY(180);
		engineStaticPowerChoiceBox.setPrefWidth(47);
		engineStaticPowerChoiceBox.setPrefHeight(30);
		engineStaticPowerChoiceBox.setItems(theController.getPowerUnitsList());
		engineDataPane.getChildren().add(engineStaticPowerChoiceBox);
		theController.getEngineTurbopropStaticPowerUnitChoiceBoxMap().put(indexOfEngineTab, engineStaticPowerChoiceBox);
		
		Label engineDryMassLabel = new Label("Dry Mass:");
		engineDryMassLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineDryMassLabel.setLayoutX(6.0);
		engineDryMassLabel.setLayoutY(213.0);
		engineDataPane.getChildren().add(engineDryMassLabel);
		
		TextField engineDryMassTextField = new TextField();
		engineDryMassTextField.setLayoutX(6.0);
		engineDryMassTextField.setLayoutY(234);
		engineDryMassTextField.setPrefWidth(340);
		engineDryMassTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineDryMassTextField);
		theController.getEngineTurbopropDryMassTextFieldMap().put(indexOfEngineTab, engineDryMassTextField);
		
		ChoiceBox<String> engineDryMassChoiceBox = new ChoiceBox<String>();
		engineDryMassChoiceBox.setLayoutX(348.0);
		engineDryMassChoiceBox.setLayoutY(234);
		engineDryMassChoiceBox.setPrefWidth(47);
		engineDryMassChoiceBox.setPrefHeight(30);
		engineDryMassChoiceBox.setItems(theController.getMassUnitsList());
		engineDataPane.getChildren().add(engineDryMassChoiceBox);
		theController.getEngineTurbopropDryMassUnitChoiceBoxMap().put(indexOfEngineTab, engineDryMassChoiceBox);
		
		Button engineDryMassCalculateButton = new Button("Calculate");
		engineDryMassCalculateButton.setLayoutX(397.0);
		engineDryMassCalculateButton.setLayoutY(234);
		engineDryMassCalculateButton.setPrefHeight(31);
		engineDryMassCalculateButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				theController.calculateEngineDryMass(engineDryMassTextField, engineDryMassChoiceBox, indexOfEngineTab);
			}
		});
		engineDataPane.getChildren().add(engineDryMassCalculateButton);
		
		Label enginePropellerDiameterLabel = new Label("Propeller Diameter:");
		enginePropellerDiameterLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		enginePropellerDiameterLabel.setLayoutX(6.0);
		enginePropellerDiameterLabel.setLayoutY(268.0);
		engineDataPane.getChildren().add(enginePropellerDiameterLabel);
		
		TextField enginePropellerDiameterTextField = new TextField();
		enginePropellerDiameterTextField.setLayoutX(6.0);
		enginePropellerDiameterTextField.setLayoutY(289);
		enginePropellerDiameterTextField.setPrefWidth(340);
		enginePropellerDiameterTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(enginePropellerDiameterTextField);
		theController.getEngineTurbopropPropellerDiameterTextFieldMap().put(indexOfEngineTab, enginePropellerDiameterTextField);
		
		ChoiceBox<String> enginePropellerDiameterChoiceBox = new ChoiceBox<String>();
		enginePropellerDiameterChoiceBox.setLayoutX(348.0);
		enginePropellerDiameterChoiceBox.setLayoutY(289);
		enginePropellerDiameterChoiceBox.setPrefWidth(47);
		enginePropellerDiameterChoiceBox.setPrefHeight(30);
		enginePropellerDiameterChoiceBox.setItems(theController.getLengthUnitsList());
		engineDataPane.getChildren().add(enginePropellerDiameterChoiceBox);
		theController.getEngineTurbopropPropellerDiameterUnitChoiceBoxMap().put(indexOfEngineTab, enginePropellerDiameterChoiceBox);
		
		Label engineNumberOfBladesLabel = new Label("Number Of Blades:");
		engineNumberOfBladesLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineNumberOfBladesLabel.setLayoutX(6.0);
		engineNumberOfBladesLabel.setLayoutY(323.0);
		engineDataPane.getChildren().add(engineNumberOfBladesLabel);
		
		TextField engineNumberOfBladesTextField = new TextField();
		engineNumberOfBladesTextField.setLayoutX(6.0);
		engineNumberOfBladesTextField.setLayoutY(344);
		engineNumberOfBladesTextField.setPrefWidth(340);
		engineNumberOfBladesTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineNumberOfBladesTextField);
		theController.getEngineTurbopropNumberOfBladesTextFieldMap().put(indexOfEngineTab, engineNumberOfBladesTextField);
		
		Label enginePropellerEfficiencyLabel = new Label("Propeller Efficiency:");
		enginePropellerEfficiencyLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		enginePropellerEfficiencyLabel.setLayoutX(6.0);
		enginePropellerEfficiencyLabel.setLayoutY(378.0);
		engineDataPane.getChildren().add(enginePropellerEfficiencyLabel);
		
		TextField enginePropellerEfficiencyTextField = new TextField();
		enginePropellerEfficiencyTextField.setLayoutX(6.0);
		enginePropellerEfficiencyTextField.setLayoutY(399);
		enginePropellerEfficiencyTextField.setPrefWidth(340);
		enginePropellerEfficiencyTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(enginePropellerEfficiencyTextField);
		theController.getEngineTurbopropPropellerEfficiencyTextFieldMap().put(indexOfEngineTab, enginePropellerEfficiencyTextField);
		
		Label engineNumberOfCompressorStagesLabel = new Label("Number Of Compressor Stages:");
		engineNumberOfCompressorStagesLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineNumberOfCompressorStagesLabel.setLayoutX(6.0);
		engineNumberOfCompressorStagesLabel.setLayoutY(433.0);
		engineDataPane.getChildren().add(engineNumberOfCompressorStagesLabel);
		
		TextField engineNumberOfCompressorStagesTextField = new TextField();
		engineNumberOfCompressorStagesTextField.setLayoutX(6.0);
		engineNumberOfCompressorStagesTextField.setLayoutY(454);
		engineNumberOfCompressorStagesTextField.setPrefWidth(340);
		engineNumberOfCompressorStagesTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineNumberOfCompressorStagesTextField);
		theController.getEngineTurbopropNumberOfCompressorStagesTextFieldMap().put(indexOfEngineTab, engineNumberOfCompressorStagesTextField);
		
		Label engineNumberOfShaftsLabel = new Label("Number Of Shafts:");
		engineNumberOfShaftsLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineNumberOfShaftsLabel.setLayoutX(6.0);
		engineNumberOfShaftsLabel.setLayoutY(487.0);
		engineDataPane.getChildren().add(engineNumberOfShaftsLabel);
		
		TextField engineNumberOfShaftsTextField = new TextField();
		engineNumberOfShaftsTextField.setLayoutX(6.0);
		engineNumberOfShaftsTextField.setLayoutY(508);
		engineNumberOfShaftsTextField.setPrefWidth(340);
		engineNumberOfShaftsTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineNumberOfShaftsTextField);
		theController.getEngineTurbopropNumberOfShaftsTextFieldMap().put(indexOfEngineTab, engineNumberOfShaftsTextField);
		
		Label engineOverallPressureRatioLabel = new Label("Overall Pressure Ratio:");
		engineOverallPressureRatioLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineOverallPressureRatioLabel.setLayoutX(6.0);
		engineOverallPressureRatioLabel.setLayoutY(542.0);
		engineDataPane.getChildren().add(engineOverallPressureRatioLabel);
		
		TextField engineOverallPressureRatioTextField = new TextField();
		engineOverallPressureRatioTextField.setLayoutX(6.0);
		engineOverallPressureRatioTextField.setLayoutY(563);
		engineOverallPressureRatioTextField.setPrefWidth(340);
		engineOverallPressureRatioTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineOverallPressureRatioTextField);
		theController.getEngineTurbopropOverallPressureRatioTextFieldMap().put(indexOfEngineTab, engineOverallPressureRatioTextField);
		
		theController.getPowerPlantPaneMap().put(EngineTypeEnum.TURBOPROP, engineDataPane);
		theController.getPowerPlantEngineTypePaneMap().put(indexOfEngineTab, theController.getPowerPlantPaneMap());
		theController.getPowerPlantBorderPaneMap().get(indexOfEngineTab).setCenter(engineDataPane);
		
	}
	
	public void showPistonDataRadioButtonImplementation (int indexOfEngineTab) {
		
		Pane engineDataPane = new Pane();
		
		Label engineTypeLabel = new Label("Type:");
		engineTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineTypeLabel.setLayoutX(6.0);
		engineTypeLabel.setLayoutY(0.0);
		engineDataPane.getChildren().add(engineTypeLabel);
		
		ChoiceBox<String> engineTypeChoiceBox = new ChoiceBox<String>();
		engineTypeChoiceBox.setLayoutX(6.0);
		engineTypeChoiceBox.setLayoutY(21.0);
		engineTypeChoiceBox.setPrefWidth(340);
		engineTypeChoiceBox.setPrefHeight(31);
		engineTypeChoiceBox.setItems(theController.getPistonEngineTypeList());
		engineDataPane.getChildren().add(engineTypeChoiceBox);
		theController.getEnginePistonTypeChoiceBoxMap().put(indexOfEngineTab, engineTypeChoiceBox); 
		
		Label engineDatabaseLabel = new Label("Database:");
		engineDatabaseLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineDatabaseLabel.setLayoutX(6.0);
		engineDatabaseLabel.setLayoutY(52.0);
		engineDataPane.getChildren().add(engineDatabaseLabel);
		
		TextField engineDatabasePathTextField = new TextField();
		engineDatabasePathTextField.setLayoutX(6.0);
		engineDatabasePathTextField.setLayoutY(73);
		engineDatabasePathTextField.setPrefWidth(340);
		engineDatabasePathTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineDatabasePathTextField);
		theController.getEnginePistonDatabaseTextFieldMap().put(indexOfEngineTab, engineDatabasePathTextField);
		
		Button engineDatabasePathChooseButton = new Button("...");
		engineDatabasePathChooseButton.setLayoutX(350.0);
		engineDatabasePathChooseButton.setLayoutY(73);
		engineDatabasePathChooseButton.setPrefWidth(24);
		engineDatabasePathChooseButton.setPrefHeight(31);
		engineDatabasePathChooseButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				theController.chooseEngineDatabase(engineDatabasePathTextField);
			}
		});
		engineDataPane.getChildren().add(engineDatabasePathChooseButton);
		
		Label engineLengthLabel = new Label("Length:");
		engineLengthLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineLengthLabel.setLayoutX(6.0);
		engineLengthLabel.setLayoutY(104.0);
		engineDataPane.getChildren().add(engineLengthLabel);
		
		TextField engineLengthTextField = new TextField();
		engineLengthTextField.setLayoutX(6.0);
		engineLengthTextField.setLayoutY(125);
		engineLengthTextField.setPrefWidth(340);
		engineLengthTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineLengthTextField);
		theController.getEnginePistonLengthTextFieldMap().put(indexOfEngineTab, engineLengthTextField);
		
		ChoiceBox<String> engineLengthChoiceBox = new ChoiceBox<String>();
		engineLengthChoiceBox.setLayoutX(348.0);
		engineLengthChoiceBox.setLayoutY(125);
		engineLengthChoiceBox.setPrefWidth(47);
		engineLengthChoiceBox.setPrefHeight(30);
		engineLengthChoiceBox.setItems(theController.getLengthUnitsList());
		engineDataPane.getChildren().add(engineLengthChoiceBox);
		theController.getEnginePistonLengthUnitChoiceBoxMap().put(indexOfEngineTab, engineLengthChoiceBox);
		
		Label engineStaticPowerLabel = new Label("Static Power:");
		engineStaticPowerLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineStaticPowerLabel.setLayoutX(6.0);
		engineStaticPowerLabel.setLayoutY(159.0);
		engineDataPane.getChildren().add(engineStaticPowerLabel);
		
		TextField engineStaticPowerTextField = new TextField();
		engineStaticPowerTextField.setLayoutX(6.0);
		engineStaticPowerTextField.setLayoutY(180);
		engineStaticPowerTextField.setPrefWidth(340);
		engineStaticPowerTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineStaticPowerTextField);
		theController.getEnginePistonStaticPowerTextFieldMap().put(indexOfEngineTab, engineStaticPowerTextField);
		
		ChoiceBox<String> engineStaticPowerChoiceBox = new ChoiceBox<String>();
		engineStaticPowerChoiceBox.setLayoutX(348.0);
		engineStaticPowerChoiceBox.setLayoutY(180);
		engineStaticPowerChoiceBox.setPrefWidth(47);
		engineStaticPowerChoiceBox.setPrefHeight(30);
		engineStaticPowerChoiceBox.setItems(theController.getPowerUnitsList());
		engineDataPane.getChildren().add(engineStaticPowerChoiceBox);
		theController.getEnginePistonStaticPowerUnitChoiceBoxMap().put(indexOfEngineTab, engineStaticPowerChoiceBox);
		
		Label engineDryMassLabel = new Label("Dry Mass:");
		engineDryMassLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineDryMassLabel.setLayoutX(6.0);
		engineDryMassLabel.setLayoutY(213.0);
		engineDataPane.getChildren().add(engineDryMassLabel);
		
		TextField engineDryMassTextField = new TextField();
		engineDryMassTextField.setLayoutX(6.0);
		engineDryMassTextField.setLayoutY(234);
		engineDryMassTextField.setPrefWidth(340);
		engineDryMassTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineDryMassTextField);
		theController.getEnginePistonDryMassTextFieldMap().put(indexOfEngineTab, engineDryMassTextField);
		
		ChoiceBox<String> engineDryMassChoiceBox = new ChoiceBox<String>();
		engineDryMassChoiceBox.setLayoutX(348.0);
		engineDryMassChoiceBox.setLayoutY(234);
		engineDryMassChoiceBox.setPrefWidth(47);
		engineDryMassChoiceBox.setPrefHeight(30);
		engineDryMassChoiceBox.setItems(theController.getMassUnitsList());
		engineDataPane.getChildren().add(engineDryMassChoiceBox);
		theController.getEnginePistonDryMassUnitChoiceBoxMap().put(indexOfEngineTab, engineDryMassChoiceBox);
		
		Button engineDryMassCalculateButton = new Button("Calculate");
		engineDryMassCalculateButton.setLayoutX(397.0);
		engineDryMassCalculateButton.setLayoutY(234);
		engineDryMassCalculateButton.setPrefHeight(31);
		engineDryMassCalculateButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				theController.calculateEngineDryMass(engineDryMassTextField, engineDryMassChoiceBox, indexOfEngineTab);
			}
		});
		engineDataPane.getChildren().add(engineDryMassCalculateButton);
		
		Label enginePropellerDiameterLabel = new Label("Propeller Diameter:");
		enginePropellerDiameterLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		enginePropellerDiameterLabel.setLayoutX(6.0);
		enginePropellerDiameterLabel.setLayoutY(268.0);
		engineDataPane.getChildren().add(enginePropellerDiameterLabel);
		
		TextField enginePropellerDiameterTextField = new TextField();
		enginePropellerDiameterTextField.setLayoutX(6.0);
		enginePropellerDiameterTextField.setLayoutY(289);
		enginePropellerDiameterTextField.setPrefWidth(340);
		enginePropellerDiameterTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(enginePropellerDiameterTextField);
		theController.getEnginePistonPropellerDiameterTextFieldMap().put(indexOfEngineTab, enginePropellerDiameterTextField);
		
		ChoiceBox<String> enginePropellerDiameterChoiceBox = new ChoiceBox<String>();
		enginePropellerDiameterChoiceBox.setLayoutX(348.0);
		enginePropellerDiameterChoiceBox.setLayoutY(289);
		enginePropellerDiameterChoiceBox.setPrefWidth(47);
		enginePropellerDiameterChoiceBox.setPrefHeight(30);
		enginePropellerDiameterChoiceBox.setItems(theController.getLengthUnitsList());
		engineDataPane.getChildren().add(enginePropellerDiameterChoiceBox);
		theController.getEnginePistonPropellerDiameterUnitChoiceBoxMap().put(indexOfEngineTab, enginePropellerDiameterChoiceBox);
		
		Label engineNumberOfBladesLabel = new Label("Number Of Blades:");
		engineNumberOfBladesLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		engineNumberOfBladesLabel.setLayoutX(6.0);
		engineNumberOfBladesLabel.setLayoutY(323.0);
		engineDataPane.getChildren().add(engineNumberOfBladesLabel);
		
		TextField engineNumberOfBladesTextField = new TextField();
		engineNumberOfBladesTextField.setLayoutX(6.0);
		engineNumberOfBladesTextField.setLayoutY(344);
		engineNumberOfBladesTextField.setPrefWidth(340);
		engineNumberOfBladesTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(engineNumberOfBladesTextField);
		theController.getEnginePistonNumberOfBladesTextFieldMap().put(indexOfEngineTab, engineNumberOfBladesTextField);
		
		Label enginePropellerEfficiencyLabel = new Label("Propeller Efficiency:");
		enginePropellerEfficiencyLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		enginePropellerEfficiencyLabel.setLayoutX(6.0);
		enginePropellerEfficiencyLabel.setLayoutY(378.0);
		engineDataPane.getChildren().add(enginePropellerEfficiencyLabel);
		
		TextField enginePropellerEfficiencyTextField = new TextField();
		enginePropellerEfficiencyTextField.setLayoutX(6.0);
		enginePropellerEfficiencyTextField.setLayoutY(399);
		enginePropellerEfficiencyTextField.setPrefWidth(340);
		enginePropellerEfficiencyTextField.setPrefHeight(31);
		engineDataPane.getChildren().add(enginePropellerEfficiencyTextField);
		theController.getEnginePistonPropellerEfficiencyTextFieldMap().put(indexOfEngineTab, enginePropellerEfficiencyTextField);
		
		theController.getPowerPlantPaneMap().put(EngineTypeEnum.PISTON, engineDataPane);
		theController.getPowerPlantEngineTypePaneMap().put(indexOfEngineTab, theController.getPowerPlantPaneMap());
		theController.getPowerPlantBorderPaneMap().get(indexOfEngineTab).setCenter(engineDataPane);
		
	}
	
	public void chooseAircraftEngineFile(int indexOfEngine) throws IOException {

		theController.setEngineFileChooser(new FileChooser());
		theController.getEngineFileChooser().setTitle("Open File");
		theController.getEngineFileChooser().setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() 
						+ File.separator 
						+ "Template_Aircraft" 
						+ File.separator 
						+ "engines"
						)
				);
		File file = theController.getEngineFileChooser().showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			theController.getTextFieldsAircraftEngineFileList().get(indexOfEngine).setText(file.getAbsolutePath());
		}
		
	}
	
	public void chooseAircraftNacelleFile(int indexOfNacelle) throws IOException {

		theController.setNacelleFileChooser(new FileChooser());
		theController.getNacelleFileChooser().setTitle("Open File");
		theController.getNacelleFileChooser().setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() 
						+ File.separator 
						+ "Template_Aircraft" 
						+ File.separator 
						+ "nacelles"
						)
				);
		File file = theController.getNacelleFileChooser().showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			theController.getTextFieldsAircraftNacelleFileList().get(indexOfNacelle).setText(file.getAbsolutePath());
		}
		
	}

	public boolean isAircraftFile(String pathToAircraftXML) {

		boolean isAircraftFile = false;
		
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
		System.setOut(filterStream);

		if(pathToAircraftXML.endsWith(".xml")) {
			File inputFile = new File(pathToAircraftXML);
			if(inputFile.exists()) {
				JPADXmlReader reader = new JPADXmlReader(pathToAircraftXML);
				if(reader.getXmlDoc().getElementsByTagName("aircraft").getLength() > 0)
					isAircraftFile = true;
			}
		}
		// write again
		System.setOut(originalOut);
		
		return isAircraftFile;
	}
	
}
