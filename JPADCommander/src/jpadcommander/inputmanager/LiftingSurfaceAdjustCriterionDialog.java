package jpadcommander.inputmanager;

import java.util.Locale;

import javax.measure.unit.SI;

import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.WingAdjustCriteriaEnum;
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
	private LiftingSurfaceCreator currentLiftingSurface;

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
		gridpane.add(continueButton, 1, 4);

		switch (type) {
		case WING:
			// TODO: FIX EQUIVALENT WING ISSUE 
			currentLiftingSurface = Main.getTheAircraft().getWing().getLiftingSurfaceCreator();
			break;
		case HORIZONTAL_TAIL:
			currentLiftingSurface = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator();
			break;
		case VERTICAL_TAIL:
			currentLiftingSurface = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator();
			break;
		case CANARD:
			currentLiftingSurface = Main.getTheAircraft().getCanard().getLiftingSurfaceCreator();
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
			Label currentLiftingSurfaceAspectRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				 currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getAspectRatio()
										)
						);
			else
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getAspectRatio()
										)
						);
				
			gridpane.add(currentLiftingSurfaceAspectRatioLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceSpanLabel = new Label("Span:");
			gridpane.add(liftingSurfaceSpanLabel, 0, 2);
			liftingSurfaceSpanTextField = new TextField();
			gridpane.add(liftingSurfaceSpanTextField, 1, 2);
			Label currentLiftingSurfaceSpanLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceSpanLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceRootChordLabel = new Label("Root Chord:");
			gridpane.add(liftingSurfaceRootChordLabel, 0, 3);
			liftingSurfaceRootChordTextField = new TextField();
			gridpane.add(liftingSurfaceRootChordTextField, 1, 3);
			Label currentLiftingSurfaceRootChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getChordRoot().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceRootChordLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceAspectRatioTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceSpanTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceRootChordTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_SPAN_ROOTCHORD
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_SPAN_ROOTCHORD
								);
					close();

				}
			});
		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY AR, SPAN AND TIP CHORD") ) {

			//.......................................................................................
			Label liftingSurfaceAspectRatioLabel = new Label("Aspect Ratio:");
			gridpane.add(liftingSurfaceAspectRatioLabel, 0, 1);
			liftingSurfaceAspectRatioTextField = new TextField();
			gridpane.add(liftingSurfaceAspectRatioTextField, 1, 1);
			Label currentLiftingSurfaceAspectRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getAspectRatio()
										)
						);
			else
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getAspectRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceAspectRatioLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceSpanLabel = new Label("Span:");
			gridpane.add(liftingSurfaceSpanLabel, 0, 2);
			liftingSurfaceSpanTextField = new TextField();
			gridpane.add(liftingSurfaceSpanTextField, 1, 2);
			Label currentLiftingSurfaceSpanLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceSpanLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTipChordLabel = new Label("Tip Chord:");
			gridpane.add(liftingSurfaceTipChordLabel, 0, 3);
			liftingSurfaceTipChordTextField = new TextField();
			gridpane.add(liftingSurfaceTipChordTextField, 1, 3);
			Label currentLiftingSurfaceTipChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceTipChordLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceAspectRatioTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceSpanTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTipChordTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_SPAN_TIPCHORD
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_SPAN_TIPCHORD
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY AR, SPAN AND TAPER-RATIO") ) {

			//.......................................................................................
			Label liftingSurfaceAspectRatioLabel = new Label("Aspect Ratio:");
			gridpane.add(liftingSurfaceAspectRatioLabel, 0, 1);
			liftingSurfaceAspectRatioTextField = new TextField();
			gridpane.add(liftingSurfaceAspectRatioTextField, 1, 1);
			Label currentLiftingSurfaceAspectRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getAspectRatio()
										)
						);
			else
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getAspectRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceAspectRatioLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceSpanLabel = new Label("Span:");
			gridpane.add(liftingSurfaceSpanLabel, 0, 2);
			liftingSurfaceSpanTextField = new TextField();
			gridpane.add(liftingSurfaceSpanTextField, 1, 2);
			Label currentLiftingSurfaceSpanLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceSpanLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTaperRatioLabel = new Label("Taper Ratio:");
			gridpane.add(liftingSurfaceTaperRatioLabel, 0, 3);
			liftingSurfaceTaperRatioTextField= new TextField();
			gridpane.add(liftingSurfaceTaperRatioTextField, 1, 3);
			Label currentLiftingSurfaceTaperRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTaperRatio()
										)
						);
			else
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getTaperRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceTaperRatioLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceAspectRatioTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceSpanTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTaperRatioTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_SPAN_TAPER
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_SPAN_TAPER
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY AR, AREA AND ROOT CHORD") ) {

			//.......................................................................................
			Label liftingSurfaceAspectRatioLabel = new Label("Aspect Ratio:");
			gridpane.add(liftingSurfaceAspectRatioLabel, 0, 1);
			liftingSurfaceAspectRatioTextField = new TextField();
			gridpane.add(liftingSurfaceAspectRatioTextField, 1, 1);
			Label currentLiftingSurfaceAspectRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getAspectRatio()
										)
						);
			else
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getAspectRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceAspectRatioLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceAreaLabel = new Label("Area:");
			gridpane.add(liftingSurfaceAreaLabel, 0, 2);
			liftingSurfaceAreaTextField = new TextField();
			gridpane.add(liftingSurfaceAreaTextField, 1, 2);
			Label currentLiftingSurfaceAreaLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			else
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			gridpane.add(currentLiftingSurfaceAreaLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceRootChordLabel = new Label("Root Chord:");
			gridpane.add(liftingSurfaceRootChordLabel, 0, 3);
			liftingSurfaceRootChordTextField = new TextField();
			gridpane.add(liftingSurfaceRootChordTextField, 1, 3);
			Label currentLiftingSurfaceRootChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getChordRoot().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceRootChordLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceAspectRatioTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceAreaTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceRootChordTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_AREA_ROOTCHORD
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_AREA_ROOTCHORD
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY AR, AREA AND TIP CHORD") ) {

			//.......................................................................................
			Label liftingSurfaceAspectRatioLabel = new Label("Aspect Ratio:");
			gridpane.add(liftingSurfaceAspectRatioLabel, 0, 1);
			liftingSurfaceAspectRatioTextField = new TextField();
			gridpane.add(liftingSurfaceAspectRatioTextField, 1, 1);
			Label currentLiftingSurfaceAspectRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getAspectRatio()
										)
						);
			else
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getAspectRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceAspectRatioLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceAreaLabel = new Label("Area:");
			gridpane.add(liftingSurfaceAreaLabel, 0, 2);
			liftingSurfaceAreaTextField = new TextField();
			gridpane.add(liftingSurfaceAreaTextField, 1, 2);
			Label currentLiftingSurfaceAreaLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			else
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			gridpane.add(currentLiftingSurfaceAreaLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTipChordLabel = new Label("Tip Chord:");
			gridpane.add(liftingSurfaceTipChordLabel, 0, 3);
			liftingSurfaceTipChordTextField = new TextField();
			gridpane.add(liftingSurfaceTipChordTextField, 1, 3);
			Label currentLiftingSurfaceTipChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceTipChordLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceAspectRatioTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceAreaTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTipChordTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_AREA_TIPCHORD
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_AREA_TIPCHORD
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY AR, AREA AND TAPER-RATIO") ) {

			//.......................................................................................
			Label liftingSurfaceAspectRatioLabel = new Label("Aspect Ratio:");
			gridpane.add(liftingSurfaceAspectRatioLabel, 0, 1);
			liftingSurfaceAspectRatioTextField = new TextField();
			gridpane.add(liftingSurfaceAspectRatioTextField, 1, 1);
			Label currentLiftingSurfaceAspectRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getAspectRatio()
										)
						);
			else
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getAspectRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceAspectRatioLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceAreaLabel = new Label("Area:");
			gridpane.add(liftingSurfaceAreaLabel, 0, 2);
			liftingSurfaceAreaTextField = new TextField();
			gridpane.add(liftingSurfaceAreaTextField, 1, 2);
			Label currentLiftingSurfaceAreaLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			else
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			gridpane.add(currentLiftingSurfaceAreaLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTaperRatioLabel = new Label("Taper Ratio:");
			gridpane.add(liftingSurfaceTaperRatioLabel, 0, 3);
			liftingSurfaceTaperRatioTextField= new TextField();
			gridpane.add(liftingSurfaceTaperRatioTextField, 1, 3);
			Label currentLiftingSurfaceTaperRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTaperRatio()
										)
						);
			else
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getTaperRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceTaperRatioLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceAspectRatioTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceAreaTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTaperRatioTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_AREA_TAPER
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_AREA_TAPER
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY AR, ROOT CHORD AND TIP CHORD") ) {

			//.......................................................................................
			Label liftingSurfaceAspectRatioLabel = new Label("Aspect Ratio:");
			gridpane.add(liftingSurfaceAspectRatioLabel, 0, 1);
			liftingSurfaceAspectRatioTextField = new TextField();
			gridpane.add(liftingSurfaceAspectRatioTextField, 1, 1);
			Label currentLiftingSurfaceAspectRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getAspectRatio()
										)
						);
			else
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getAspectRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceAspectRatioLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceRootChordLabel = new Label("Root Chord:");
			gridpane.add(liftingSurfaceRootChordLabel, 0, 2);
			liftingSurfaceRootChordTextField = new TextField();
			gridpane.add(liftingSurfaceRootChordTextField, 1, 2);
			Label currentLiftingSurfaceRootChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getChordRoot().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceRootChordLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTipChordLabel = new Label("Tip Chord:");
			gridpane.add(liftingSurfaceTipChordLabel, 0, 3);
			liftingSurfaceTipChordTextField= new TextField();
			gridpane.add(liftingSurfaceTipChordTextField, 1, 3);
			Label currentLiftingSurfaceTipChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceTipChordLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceAspectRatioTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceRootChordTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTipChordTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_ROOTCHORD_TIPCHORD
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_ROOTCHORD_TIPCHORD
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY AR, ROOT CHORD AND TAPER-RATIO") ) {

			//.......................................................................................
			Label liftingSurfaceAspectRatioLabel = new Label("Aspect Ratio:");
			gridpane.add(liftingSurfaceAspectRatioLabel, 0, 1);
			liftingSurfaceAspectRatioTextField = new TextField();
			gridpane.add(liftingSurfaceAspectRatioTextField, 1, 1);
			Label currentLiftingSurfaceAspectRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getAspectRatio()
										)
						);
			else
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getAspectRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceAspectRatioLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceRootChordLabel = new Label("Root Chord:");
			gridpane.add(liftingSurfaceRootChordLabel, 0, 2);
			liftingSurfaceRootChordTextField = new TextField();
			gridpane.add(liftingSurfaceRootChordTextField, 1, 2);
			Label currentLiftingSurfaceRootChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getChordRoot().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceRootChordLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTaperRatioLabel = new Label("Taper Ratio:");
			gridpane.add(liftingSurfaceTaperRatioLabel, 0, 3);
			liftingSurfaceTaperRatioTextField= new TextField();
			gridpane.add(liftingSurfaceTaperRatioTextField, 1, 3);
			Label currentLiftingSurfaceTaperRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTaperRatio()
										)
						);
			else
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getTaperRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceTaperRatioLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceAspectRatioTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceRootChordTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTaperRatioTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioLabel.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_ROOTCHORD_TAPER
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_ROOTCHORD_TIPCHORD
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY AR, TIP CHORD AND TAPER-RATIO") ) {

			//.......................................................................................
			Label liftingSurfaceAspectRatioLabel = new Label("Aspect Ratio:");
			gridpane.add(liftingSurfaceAspectRatioLabel, 0, 1);
			liftingSurfaceAspectRatioTextField = new TextField();
			gridpane.add(liftingSurfaceAspectRatioTextField, 1, 1);
			Label currentLiftingSurfaceAspectRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getAspectRatio()
										)
						);
			else
				currentLiftingSurfaceAspectRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getAspectRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceAspectRatioLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceTipChordLabel = new Label("Tip Chord:");
			gridpane.add(liftingSurfaceTipChordLabel, 0, 2);
			liftingSurfaceTipChordTextField = new TextField();
			gridpane.add(liftingSurfaceTipChordTextField, 1, 2);
			Label currentLiftingSurfaceTipChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceTipChordLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTaperRatioLabel = new Label("Taper Ratio:");
			gridpane.add(liftingSurfaceTaperRatioLabel, 0, 3);
			liftingSurfaceTaperRatioTextField= new TextField();
			gridpane.add(liftingSurfaceTaperRatioTextField, 1, 3);
			Label currentLiftingSurfaceTaperRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTaperRatio()
										)
						);
			else
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getTaperRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceTaperRatioLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceAspectRatioTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceTipChordTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTaperRatioTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_TIPCHORD_TAPER
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAspectRatioTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AR_ROOTCHORD_TIPCHORD
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY SPAN, AREA AND ROOT CHORD") ) {

			//.......................................................................................
			Label liftingSurfaceSpanLabel = new Label("Span:");
			gridpane.add(liftingSurfaceSpanLabel, 0, 1);
			liftingSurfaceSpanTextField = new TextField();
			gridpane.add(liftingSurfaceSpanTextField, 1, 1);
			Label currentLiftingSurfaceSpanLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceSpanLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceAreaLabel = new Label("Area:");
			gridpane.add(liftingSurfaceAreaLabel, 0, 2);
			liftingSurfaceAreaTextField = new TextField();
			gridpane.add(liftingSurfaceAreaTextField, 1, 2);
			Label currentLiftingSurfaceAreaLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			else
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			gridpane.add(currentLiftingSurfaceAreaLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceRootChordLabel = new Label("Root Chord:");
			gridpane.add(liftingSurfaceRootChordLabel, 0, 3);
			liftingSurfaceRootChordTextField= new TextField();
			gridpane.add(liftingSurfaceRootChordTextField, 1, 3);
			Label currentLiftingSurfaceRootChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getChordRoot().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceRootChordLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceSpanTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceAreaTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceRootChordTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.SPAN_AREA_ROOTCHORD
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.SPAN_AREA_ROOTCHORD
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY SPAN, AREA AND TIP CHORD") ) {

			//.......................................................................................
			Label liftingSurfaceSpanLabel = new Label("Span:");
			gridpane.add(liftingSurfaceSpanLabel, 0, 1);
			liftingSurfaceSpanTextField = new TextField();
			gridpane.add(liftingSurfaceSpanTextField, 1, 1);
			Label currentLiftingSurfaceSpanLabel = null;
			if(type.equals(ComponentEnum.WING))
			currentLiftingSurfaceSpanLabel = new Label(
					"Current value: " 
							+ String.format(
									Locale.ROOT,
									"%.02f",
									2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSpan().doubleValue(SI.METER)
									)
							+ " m"
					);
			else
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceSpanLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceAreaLabel = new Label("Area:");
			gridpane.add(liftingSurfaceAreaLabel, 0, 2);
			liftingSurfaceAreaTextField = new TextField();
			gridpane.add(liftingSurfaceAreaTextField, 1, 2);
			Label currentLiftingSurfaceAreaLabel = null;
			if(type.equals(ComponentEnum.WING))
			currentLiftingSurfaceAreaLabel = new Label(
					"Current value: " 
							+ String.format(
									Locale.ROOT,
									"%.02f",
									2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
									)
							+ " m²"
					);
			else
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			gridpane.add(currentLiftingSurfaceAreaLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTipChordLabel = new Label("Tip Chord:");
			gridpane.add(liftingSurfaceTipChordLabel, 0, 3);
			liftingSurfaceTipChordTextField= new TextField();
			gridpane.add(liftingSurfaceTipChordTextField, 1, 3);
			Label currentLiftingSurfaceTipChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceTipChordLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceSpanTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceAreaTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTipChordTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.SPAN_AREA_TIPCHORD
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.SPAN_AREA_TIPCHORD
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY SPAN, AREA AND TAPER-RATIO") ) {

			//.......................................................................................
			Label liftingSurfaceSpanLabel = new Label("Span:");
			gridpane.add(liftingSurfaceSpanLabel, 0, 1);
			liftingSurfaceSpanTextField = new TextField();
			gridpane.add(liftingSurfaceSpanTextField, 1, 1);
			Label currentLiftingSurfaceSpanLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceSpanLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceAreaLabel = new Label("Area:");
			gridpane.add(liftingSurfaceAreaLabel, 0, 2);
			liftingSurfaceAreaTextField = new TextField();
			gridpane.add(liftingSurfaceAreaTextField, 1, 2);
			Label currentLiftingSurfaceAreaLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			else
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			gridpane.add(currentLiftingSurfaceAreaLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTaperRatioLabel = new Label("Taper Ratio:");
			gridpane.add(liftingSurfaceTaperRatioLabel, 0, 3);
			liftingSurfaceTaperRatioTextField= new TextField();
			gridpane.add(liftingSurfaceTaperRatioTextField, 1, 3);
			Label currentLiftingSurfaceTaperRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTaperRatio()
										)
						);
			else
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getTaperRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceTaperRatioLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceSpanTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceAreaTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTaperRatioTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.SPAN_AREA_TAPER
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.SPAN_AREA_TAPER
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY SPAN, ROOT CHORD AND TIP CHORD") ) {

			//.......................................................................................
			Label liftingSurfaceSpanLabel = new Label("Span:");
			gridpane.add(liftingSurfaceSpanLabel, 0, 1);
			liftingSurfaceSpanTextField = new TextField();
			gridpane.add(liftingSurfaceSpanTextField, 1, 1);
			Label currentLiftingSurfaceSpanLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceSpanLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceRootChordLabel = new Label("Root Chord:");
			gridpane.add(liftingSurfaceRootChordLabel, 0, 2);
			liftingSurfaceRootChordTextField = new TextField();
			gridpane.add(liftingSurfaceRootChordTextField, 1, 2);
			Label currentLiftingSurfaceRootChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METRE)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getChordRoot().doubleValue(SI.METRE)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceRootChordLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTipChordLabel = new Label("Tip Chord:");
			gridpane.add(liftingSurfaceTipChordLabel, 0, 3);
			liftingSurfaceTipChordTextField= new TextField();
			gridpane.add(liftingSurfaceTipChordTextField, 1, 3);
			Label currentLiftingSurfaceTipChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceTipChordLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceSpanTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceRootChordTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTipChordTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.SPAN_ROOTCHORD_TIPCHORD
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.SPAN_AREA_TAPER
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY SPAN, ROOT CHORD AND TAPER-RATIO") ) {

			//.......................................................................................
			Label liftingSurfaceSpanLabel = new Label("Span:");
			gridpane.add(liftingSurfaceSpanLabel, 0, 1);
			liftingSurfaceSpanTextField = new TextField();
			gridpane.add(liftingSurfaceSpanTextField, 1, 1);
			Label currentLiftingSurfaceSpanLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceSpanLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceRootChordLabel = new Label("Root Chord:");
			gridpane.add(liftingSurfaceRootChordLabel, 0, 2);
			liftingSurfaceRootChordTextField = new TextField();
			gridpane.add(liftingSurfaceRootChordTextField, 1, 2);
			Label currentLiftingSurfaceRootChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METRE)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getChordRoot().doubleValue(SI.METRE)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceRootChordLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTaperRatioLabel = new Label("Taper Ratio:");
			gridpane.add(liftingSurfaceTaperRatioLabel, 0, 3);
			liftingSurfaceTaperRatioTextField= new TextField();
			gridpane.add(liftingSurfaceTaperRatioTextField, 1, 3);
			Label currentLiftingSurfaceTaperRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTaperRatio()
										)
						);
			else
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getTaperRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceTaperRatioLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceSpanTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceRootChordTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTaperRatioTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.SPAN_ROOTCHORD_TAPER
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.SPAN_ROOTCHORD_TAPER
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY SPAN, TIP CHORD AND TAPER-RATIO") ) {

			//.......................................................................................
			Label liftingSurfaceSpanLabel = new Label("Span:");
			gridpane.add(liftingSurfaceSpanLabel, 0, 1);
			liftingSurfaceSpanTextField = new TextField();
			gridpane.add(liftingSurfaceSpanTextField, 1, 1);
			Label currentLiftingSurfaceSpanLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceSpanLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSpan().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceSpanLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceTipChordLabel = new Label("Tip Chord:");
			gridpane.add(liftingSurfaceTipChordLabel, 0, 2);
			liftingSurfaceTipChordTextField = new TextField();
			gridpane.add(liftingSurfaceTipChordTextField, 1, 2);
			Label currentLiftingSurfaceTipChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METRE)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METRE)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceTipChordLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTaperRatioLabel = new Label("Taper Ratio:");
			gridpane.add(liftingSurfaceTaperRatioLabel, 0, 3);
			liftingSurfaceTaperRatioTextField= new TextField();
			gridpane.add(liftingSurfaceTaperRatioTextField, 1, 3);
			Label currentLiftingSurfaceTaperRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTaperRatio()
										)
						);
			else
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getTaperRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceTaperRatioLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceSpanTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceTipChordTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTaperRatioTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.SPAN_TIPCHORD_TAPER
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceSpanTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.SPAN_TIPCHORD_TAPER
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY AREA, ROOT CHORD AND TIP CHORD") ) {

			//.......................................................................................
			Label liftingSurfaceAreaLabel = new Label("Area:");
			gridpane.add(liftingSurfaceAreaLabel, 0, 1);
			liftingSurfaceAreaTextField = new TextField();
			gridpane.add(liftingSurfaceAreaTextField, 1, 1);
			Label currentLiftingSurfaceAreaLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			else
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			gridpane.add(currentLiftingSurfaceAreaLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceRootChordLabel = new Label("Root Chord:");
			gridpane.add(liftingSurfaceRootChordLabel, 0, 2);
			liftingSurfaceRootChordTextField = new TextField();
			gridpane.add(liftingSurfaceRootChordTextField, 1, 2);
			Label currentLiftingSurfaceRootChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METRE)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getChordRoot().doubleValue(SI.METRE)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceRootChordLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTipChordLabel = new Label("Tip Chord:");
			gridpane.add(liftingSurfaceTipChordLabel, 0, 3);
			liftingSurfaceTipChordTextField= new TextField();
			gridpane.add(liftingSurfaceTipChordTextField, 1, 3);
			Label currentLiftingSurfaceTipChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METER)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METER)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceTipChordLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceAreaTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceRootChordTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTipChordTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AREA_ROOTCHORD_TIPCHORD
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AREA_ROOTCHORD_TIPCHORD
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY AREA, ROOT CHORD AND TAPER-RATIO") ) {

			//.......................................................................................
			Label liftingSurfaceAreaLabel = new Label("Area:");
			gridpane.add(liftingSurfaceAreaLabel, 0, 1);
			liftingSurfaceAreaTextField = new TextField();
			gridpane.add(liftingSurfaceAreaTextField, 1, 1);
			Label currentLiftingSurfaceAreaLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			else
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			gridpane.add(currentLiftingSurfaceAreaLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceRootChordLabel = new Label("Root Chord:");
			gridpane.add(liftingSurfaceRootChordLabel, 0, 2);
			liftingSurfaceRootChordTextField = new TextField();
			gridpane.add(liftingSurfaceRootChordTextField, 1, 2);
			Label currentLiftingSurfaceRootChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METRE)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceRootChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getChordRoot().doubleValue(SI.METRE)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceRootChordLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTaperRatioLabel = new Label("Taper Ratio:");
			gridpane.add(liftingSurfaceTaperRatioLabel, 0, 3);
			liftingSurfaceTaperRatioTextField= new TextField();
			gridpane.add(liftingSurfaceTaperRatioTextField, 1, 3);
			Label currentLiftingSurfaceTaperRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTaperRatio()
										)
						);
			else
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getTaperRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceTaperRatioLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceAreaTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceRootChordTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTaperRatioTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AREA_ROOTCHORD_TAPER
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceRootChordTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AREA_ROOTCHORD_TAPER
								);
					close();

				}
			});

		}
		else if (adjustCriterion.equalsIgnoreCase("MODIFY AREA, TIP CHORD AND TAPER-RATIO") ) {

			//.......................................................................................
			Label liftingSurfaceAreaLabel = new Label("Area:");
			gridpane.add(liftingSurfaceAreaLabel, 0, 1);
			liftingSurfaceAreaTextField = new TextField();
			gridpane.add(liftingSurfaceAreaTextField, 1, 1);
			Label currentLiftingSurfaceAreaLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			else
				currentLiftingSurfaceAreaLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										2*currentLiftingSurface.getPanels().get(0).getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
										)
								+ " m²"
						);
			gridpane.add(currentLiftingSurfaceAreaLabel, 2, 1);
			//.......................................................................................
			Label liftingSurfaceTipChordLabel = new Label("Tip Chord:");
			gridpane.add(liftingSurfaceTipChordLabel, 0, 2);
			liftingSurfaceTipChordTextField = new TextField();
			gridpane.add(liftingSurfaceTipChordTextField, 1, 2);
			Label currentLiftingSurfaceTipChordLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METRE)
										)
								+ " m"
						);
			else
				currentLiftingSurfaceTipChordLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels()
										.get(currentLiftingSurface.getPanels().size()-1)
										.getChordTip().doubleValue(SI.METRE)
										)
								+ " m"
						);
			gridpane.add(currentLiftingSurfaceTipChordLabel, 2, 2);
			//.......................................................................................
			Label liftingSurfaceTaperRatioLabel = new Label("Taper Ratio:");
			gridpane.add(liftingSurfaceTaperRatioLabel, 0, 3);
			liftingSurfaceTaperRatioTextField= new TextField();
			gridpane.add(liftingSurfaceTaperRatioTextField, 1, 3);
			Label currentLiftingSurfaceTaperRatioLabel = null;
			if(type.equals(ComponentEnum.WING))
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTaperRatio()
										)
						);
			else
				currentLiftingSurfaceTaperRatioLabel = new Label(
						"Current value: " 
								+ String.format(
										Locale.ROOT,
										"%.02f",
										currentLiftingSurface.getPanels().get(0).getTaperRatio()
										)
						);
			gridpane.add(currentLiftingSurfaceTaperRatioLabel, 2, 3);
			//.......................................................................................
			continueButton.disableProperty().bind(
					Bindings.isEmpty(liftingSurfaceAreaTextField.textProperty())
					.or(Bindings.isEmpty(liftingSurfaceTipChordTextField.textProperty()))
					.or(Bindings.isEmpty(liftingSurfaceTaperRatioTextField.textProperty()))
					);
			continueButton.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent action) {

					if(type.equals(ComponentEnum.WING))
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AREA_TIPCHORD_TAPER
								);
					else
						currentLiftingSurface.adjustDimensions(
								Double.valueOf(liftingSurfaceAreaTextField.getText()),
								Double.valueOf(liftingSurfaceTipChordTextField.getText()),
								Double.valueOf(liftingSurfaceTaperRatioTextField.getText()),
								currentLiftingSurface.getPanels().get(0).getSweepLeadingEdge(),
								currentLiftingSurface.getPanels().get(0).getDihedral(), 
								currentLiftingSurface.getPanels().get(0).getTwistGeometricAtTip(),
								WingAdjustCriteriaEnum.AREA_TIPCHORD_TAPER
								);
					close();

				}
			});
		}
	}
}