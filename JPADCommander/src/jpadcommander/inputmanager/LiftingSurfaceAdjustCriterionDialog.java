package jpadcommander.inputmanager;

import java.util.Locale;

import javax.measure.unit.SI;

import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jpadcommander.Main;

public class LiftingSurfaceAdjustCriterionDialog extends Stage {
	
	private TextField liftingSurfaceAspectRatioTextField;
	private TextField liftingSurfaceSpanTextField;
	private TextField liftingSurfaceAreaTextField;
	private TextField liftingSurfaceRootChordTextField;
	private TextField liftingSurfaceTipChordTextField;
	private TextField liftingSurfaceTaperRatioTextField;
	
    public LiftingSurfaceAdjustCriterionDialog(Stage owner, String adjustCriterion, ComponentEnum type) {
    	
        super();
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        Group root = new Group();
        Scene scene = new Scene(root, owner.getWidth()*0.5, owner.getHeight()*0.2, Color.LIGHTBLUE);
        setScene(scene);

        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(20));
        gridpane.setHgap(5);
        gridpane.setVgap(5);
        root.getChildren().add(gridpane);
        
        Label instructionLabel = new Label(); 
        instructionLabel.setText("Please provide the following data:");
        instructionLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        instructionLabel.setAlignment(Pos.CENTER);
        gridpane.add(instructionLabel, 1, 0);

        Button continueButton = new Button("Continue");
        continueButton.setAlignment(Pos.CENTER);
        gridpane.add(continueButton, 1, 2);
        
//        "MODIFY AR, SPAN AND ROOT CHORD",
//		"MODIFY AR, SPAN AND TIP CHORD",
//		"MODIFY AR, SPAN AND TAPER-RATIO",
//		"MODIFY AR, AREA AND ROOT CHORD",
//		"MODIFY AR, AREA AND TIP CHORD",
//		"MODIFY AR, AREA AND TAPER-RATIO", 
//		"MODIFY AR, ROOT CHORD AND TIP CHORD",
//		"MODIFY AR, ROOT CHORD AND TAPER-RATIO",
//		"MODIFY AR, TIP CHORD AND TAPER-RATIO",
//		"MODIFY SPAN, AREA AND ROOT CHORD",
//		"MODIFY SPAN, AREA AND TIP CHORD",
//		"MODIFY SPAN, AREA AND TAPER-RATIO",
//		"MODIFY SPAN, ROOT CHORD AND TIP CHORD", 
//		"MODIFY SPAN, ROOT CHORD AND TAPER-RATIO",
//		"MODIFY SPAN, TIP CHORD AND TAPER-RATIO",
//		"MODIFY AREA, ROOT CHORD AND TIP CHORD",
//		"MODIFY AREA, ROOT CHORD AND TAPER-RATIO",
//		"MODIFY AREA, TIP CHORD AND TAPER-RATIO"
        
        LiftingSurface currentLiftingSurface = null;
        
        switch (type) {
		case WING:
			currentLiftingSurface = Main.getTheAircraft().getWing();
			break;
		case HORIZONTAL_TAIL:
			currentLiftingSurface = Main.getTheAircraft().getHTail();
			break;
		case VERTICAL_TAIL:
			currentLiftingSurface = Main.getTheAircraft().getVTail();
			break;
		case CANARD:
			currentLiftingSurface = Main.getTheAircraft().getCanard();
			break;
		default:
			break;
		}
		
        if (adjustCriterion.equalsIgnoreCase("MODIFY AR, SPAN AND ROOT CHORD")) {
        	
        	//.......................................................................................
        	Label liftingSurfaceAspectRatioLabel = new Label("Aspect Ratio:");
        	gridpane.add(liftingSurfaceAspectRatioLabel, 0, 1);
        	liftingSurfaceAspectRatioTextField = new TextField();
        	gridpane.add(liftingSurfaceAspectRatioTextField, 1, 1);
        	Label currentLiftingSurfaceAspectRatioLabel = new Label(
        			"Current value: " 
        					+ String.format(
        							Locale.ROOT,
        							"%.02f",
        							currentLiftingSurface.getLiftingSurfaceCreator().getAspectRatio()
        							)
        					+ " m"
        			);
        	gridpane.add(currentLiftingSurfaceAspectRatioLabel, 2, 1);
        	//.......................................................................................
        	Label liftingSurfaceSpanLabel = new Label("Span:");
        	gridpane.add(liftingSurfaceSpanLabel, 0, 1);
        	liftingSurfaceSpanTextField = new TextField();
        	gridpane.add(liftingSurfaceSpanTextField, 1, 1);
        	Label currentLiftingSurfaceSpanLabel = new Label(
        			"Current value: " 
        					+ String.format(
        							Locale.ROOT,
        							"%.02f",
        							currentLiftingSurface.getLiftingSurfaceCreator().getSpan().doubleValue(SI.METER)
        							)
        					+ " m"
        			);
        	gridpane.add(currentLiftingSurfaceSpanLabel, 2, 1);
        	//.......................................................................................
        	Label liftingSurfaceRootChordLabel = new Label("Root Chord:");
        	gridpane.add(liftingSurfaceRootChordLabel, 0, 1);
        	liftingSurfaceRootChordTextField = new TextField();
        	gridpane.add(liftingSurfaceRootChordTextField, 1, 1);
        	Label currentLiftingRootChordLabel = new Label(
        			"Current value: " 
        					+ String.format(
        							Locale.ROOT,
        							"%.02f",
        							currentLiftingSurface.getLiftingSurfaceCreator().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
        							)
        					+ " m"
        			);
        	gridpane.add(currentLiftingRootChordLabel, 2, 1);
        	//.......................................................................................
        	continueButton.disableProperty().bind(
        			Bindings.isEmpty(liftingSurfaceAspectRatioTextField.textProperty())
            	    .or(Bindings.isEmpty(liftingSurfaceSpanTextField.textProperty()))
            	    .or(Bindings.isEmpty(liftingSurfaceRootChordTextField.textProperty()))
        			);
        }
        else if (adjustCriterion.equalsIgnoreCase("") ) {

        }
        
        continueButton.setOnAction(new EventHandler<ActionEvent>() {
    		
        	public void handle(ActionEvent action) {
        	
//        		if (adjustCriterion.equalsIgnoreCase("MODIFY TOTAL LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS"))
//        			Main.getTheAircraft().getFuselage().getFuselageCreator().adjustDimensions(
//        					Amount.valueOf(Double.valueOf(fuselageLengthField.getText()), SI.METER),
//        					FuselageAdjustCriteriaEnum.ADJ_TOT_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS
//        					);
//        		else if (adjustCriterion.equalsIgnoreCase("MODIFY TOTAL LENGTH, CONSTANT FINENESS-RATIOS"))
//        			Main.getTheAircraft().getFuselage().getFuselageCreator().adjustDimensions(
//        					Amount.valueOf(Double.valueOf(fuselageLengthField.getText()), SI.METER),
//        					FuselageAdjustCriteriaEnum.ADJ_TOT_LENGTH_CONST_FINENESS_RATIOS
//        					);
//        		else if (adjustCriterion.equalsIgnoreCase("MODIFY CYLINDER LENGTH (streching)"))
//        			Main.getTheAircraft().getFuselage().getFuselageCreator().adjustDimensions(
//        					Amount.valueOf(Double.valueOf(fuselageCylinderLengthField.getText()), SI.METER),
//        					FuselageAdjustCriteriaEnum.ADJ_CYL_LENGTH
//        					);
//        		else if (adjustCriterion.equalsIgnoreCase("MODIFY NOSE LENGTH, CONSTANT TOTAL LENGTH AND DIAMETERS"))
//        			Main.getTheAircraft().getFuselage().getFuselageCreator().adjustDimensions(
//        					Amount.valueOf(Double.valueOf(fuselageNoseLengthField.getText()), SI.METER),
//        					FuselageAdjustCriteriaEnum.ADJ_NOSE_LENGTH_CONST_TOT_LENGTH_DIAMETERS
//        					);
//        		else if (adjustCriterion.equalsIgnoreCase("MODIFY NOSE LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS"))
//        			Main.getTheAircraft().getFuselage().getFuselageCreator().adjustDimensions(
//        					Amount.valueOf(Double.valueOf(fuselageNoseLengthField.getText()), SI.METER),
//        					FuselageAdjustCriteriaEnum.ADJ_NOSE_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS
//        					);
//        		else if (adjustCriterion.equalsIgnoreCase("MODIFY NOSE LENGTH, CONSTANT FINENESS-RATIOS"))
//        			Main.getTheAircraft().getFuselage().getFuselageCreator().adjustDimensions(
//        					Amount.valueOf(Double.valueOf(fuselageNoseLengthField.getText()), SI.METER),
//        					FuselageAdjustCriteriaEnum.ADJ_NOSE_LENGTH_CONST_FINENESS_RATIOS_VAR_LENGTHS
//        					);
//        		else if (adjustCriterion.equalsIgnoreCase("MODIFY TAILCONE LENGTH, CONSTANT TOTAL LENGTH, DIAMETERS AND NOSE LENGTH RATIO"))
//        			Main.getTheAircraft().getFuselage().getFuselageCreator().adjustDimensions(
//        					Amount.valueOf(Double.valueOf(fuselageTailLengthField.getText()), SI.METER),
//        					FuselageAdjustCriteriaEnum.ADJ_TAILCONE_LENGTH_CONST_TOT_LENGTH_DIAMETERS
//        					);
//        		else if (adjustCriterion.equalsIgnoreCase("MODIFY TAILCONE LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS"))
//        			Main.getTheAircraft().getFuselage().getFuselageCreator().adjustDimensions(
//        					Amount.valueOf(Double.valueOf(fuselageTailLengthField.getText()), SI.METER),
//        					FuselageAdjustCriteriaEnum.ADJ_TAILCONE_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS
//        					);
//        		else if (adjustCriterion.equalsIgnoreCase("MODIFY TAILCONE LENGTH, CONSTANT FINENESS-RATIOS"))
//        			Main.getTheAircraft().getFuselage().getFuselageCreator().adjustDimensions(
//        					Amount.valueOf(Double.valueOf(fuselageTailLengthField.getText()), SI.METER),
//        					FuselageAdjustCriteriaEnum.ADJ_TAILCONE_LENGTH_CONST_FINENESS_RATIOS_VAR_LENGTHS
//        					);
//        		else if (adjustCriterion.equalsIgnoreCase("MODIFY FUSELAGE DIAMETER, CONSTANT FINENESS-RATIOS"))
//        			Main.getTheAircraft().getFuselage().getFuselageCreator().adjustDimensions(
//        					Amount.valueOf(Double.valueOf(fuselageDiameterTextField.getText()), SI.METER),
//        					FuselageAdjustCriteriaEnum.ADJ_FUS_LENGTH_CONST_FINENESS_RATIOS_VAR_DIAMETERS
//        					);
        		close();
        		
        	}
        });
    }
}