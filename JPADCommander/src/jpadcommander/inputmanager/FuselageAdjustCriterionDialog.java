package jpadcommander.inputmanager;

import java.util.Locale;

import javax.measure.unit.SI;

import configuration.enumerations.FuselageAdjustCriteriaEnum;
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

public class FuselageAdjustCriterionDialog extends Stage {
	
    public FuselageAdjustCriterionDialog(Stage owner, String adjustCriterion) {
    	
        super();
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        Group root = new Group();
        Scene scene = new Scene(root, owner.getWidth()*0.33, owner.getHeight()*0.15, Color.LIGHTBLUE);
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

        if (adjustCriterion.equalsIgnoreCase("MODIFY TOTAL LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS")
        		|| adjustCriterion.equalsIgnoreCase("MODIFY TOTAL LENGTH, CONSTANT FINENESS-RATIOS") ) {
        	Label fuselageLengthLabel = new Label("Fuselage Length (m):");
        	gridpane.add(fuselageLengthLabel, 0, 1);
        	final TextField fuselageLengthField = new TextField();
        	gridpane.add(fuselageLengthField, 1, 1);
        	Label currentFuselageLengthLabel = new Label(
        			"Current value: " 
        					+ String.format(
        							Locale.ROOT,
        							"%.02f",
        							Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METER)
        							)
        					+ " m"
        			);
        	gridpane.add(currentFuselageLengthLabel, 2, 1);
        }
        else if (adjustCriterion.equalsIgnoreCase("MODIFY CYLINDER LENGTH (streching)") ) {
        	Label fuselageCylenderLengthLabel = new Label("Cylinder Section Length (m):");
            gridpane.add(fuselageCylenderLengthLabel, 0, 1);
            final TextField fuselageCylinderLengthField = new TextField();
            gridpane.add(fuselageCylinderLengthField, 1, 1);
            Label currentFuselageCylinderLengthLabel = new Label(
            		"Current value: " 
            				+ String.format(
            						Locale.ROOT,
            						"%.02f",
            						Main.getTheAircraft().getFuselage().getFuselageCreator().getLenC().doubleValue(SI.METER)
            						)
            				+ " m"
            		);
        	gridpane.add(currentFuselageCylinderLengthLabel, 2, 1);
        }
        else if (adjustCriterion.equalsIgnoreCase("MODIFY NOSE LENGTH, CONSTANT TOTAL LENGTH AND DIAMETERS")
        		|| adjustCriterion.equalsIgnoreCase("MODIFY NOSE LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS")
        		|| adjustCriterion.equalsIgnoreCase("MODIFY NOSE LENGTH, CONSTANT FINENESS-RATIOS") ) {
           	Label fuselageNoseLengthLabel = new Label("Nose Section Length (m):");
            gridpane.add(fuselageNoseLengthLabel, 0, 1);
            final TextField fuselageNoseLengthField = new TextField();
            gridpane.add(fuselageNoseLengthField, 1, 1);
            Label currentFuselageNoseLengthLabel = new Label(
            		"Current value: " 
            				+ String.format(
            						Locale.ROOT,
            						"%.02f",
            						Main.getTheAircraft().getFuselage().getFuselageCreator().getLenN().doubleValue(SI.METER)
            						)
            				+ " m"
            		);
        	gridpane.add(currentFuselageNoseLengthLabel, 2, 1);
        }
        else if (adjustCriterion.equalsIgnoreCase("MODIFY TAILCONE LENGTH, CONSTANT TOTAL LENGTH, DIAMETERS AND NOSE LENGTH RATIO")
        		|| adjustCriterion.equalsIgnoreCase("MODIFY TAILCONE LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS")
        		|| adjustCriterion.equalsIgnoreCase("MODIFY TAILCONE LENGTH, CONSTANT FINENESS-RATIOS") ) {
           	Label fuselageTailLengthLabel = new Label("Tail Section Length (m):");
            gridpane.add(fuselageTailLengthLabel, 0, 1);
            final TextField fuselageTailLengthField = new TextField();
            gridpane.add(fuselageTailLengthField, 1, 1);
            Label currentFuselageTailLengthLabel = new Label(
            		"Current value: " 
            		+ String.format(
            						Locale.ROOT, 
            						"%.02f",
            						Main.getTheAircraft().getFuselage().getFuselageCreator().getLenT().doubleValue(SI.METER)
            						)
            		+ " m"
            		);
        	gridpane.add(currentFuselageTailLengthLabel, 2, 1);
        }
        else if (adjustCriterion.equalsIgnoreCase("MODIFY FUSELAGE DIAMETER, CONSTANT FINENESS-RATIOS")) {
           	Label fuselageDiameterLabel = new Label("Fuselage Equivalent Diameter (m):");
            gridpane.add(fuselageDiameterLabel, 0, 1);
            final TextField fuselageDiameterField = new TextField();
            gridpane.add(fuselageDiameterField, 1, 1);
            Label currentFuselageDiameterLabel = new Label(
            		"Current value: "  
            				+ String.format(
            						Locale.ROOT, 
            						"%.02f", Main.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterCylinderGM().doubleValue(SI.METER)
            						)
            				+ " m"
            		);
        	gridpane.add(currentFuselageDiameterLabel, 2, 1);
        }
        
        Button continueButton = new Button("Continue");
        continueButton.setAlignment(Pos.CENTER);
        gridpane.add(continueButton, 1, 2);
        continueButton.setOnAction(new EventHandler<ActionEvent>() {
    		
        	public void handle(ActionEvent action) {
        	
        		if (adjustCriterion.equalsIgnoreCase("MODIFY TOTAL LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS"))
        				Main.getTheAircraft().getFuselage().getFuselageCreator().adjustDimensions(
        						Main.getTheAircraft().getFuselage().getFuselageCreator().getLenT().times(1.2),
        						FuselageAdjustCriteriaEnum.ADJ_TAILCONE_LENGTH_CONST_FINENESS_RATIOS_VAR_LENGTHS
        						);
        		 /* else if (TODO: CONTINUE WITH OTHER CRITERIA) */
        		
        		close();
        		
        	}
        	
        });
        
    }
}