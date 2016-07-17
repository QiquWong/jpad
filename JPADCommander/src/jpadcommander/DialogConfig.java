package jpadcommander;

import java.io.File;

import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogConfig extends Stage {
	
    public DialogConfig(Stage owner) {
    	
        super();
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        Group root = new Group();
        Scene scene = new Scene(root, 820, 250, Color.LIGHTBLUE);
        setScene(scene);
        
        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(20));
        gridpane.setHgap(5);
        gridpane.setVgap(5);

        Label workingDirectoryLabel = new Label("Working directory: ");
        gridpane.add(workingDirectoryLabel, 0, 1);

        Label inputDirectoryLabel = new Label("Input Directory: ");
        gridpane.add(inputDirectoryLabel, 0, 2);
        
        Label outputDirectoryLabel = new Label("Output Directory: ");
        gridpane.add(outputDirectoryLabel, 0, 3);
        
        Label databaseDirectoryLabel = new Label("Database Directory: ");
        gridpane.add(databaseDirectoryLabel, 0, 4);
        
        final TextField workingDirectoryField = new TextField();
        workingDirectoryField.setPrefWidth(600);
        gridpane.add(workingDirectoryField, 1, 1);
//        ValidationSupport validationSupport = new ValidationSupport();
//        validationSupport.registerValidator(workingDirectoryField, Validator.createEmptyValidator("Working directory path is required"));
        
        final TextField inputDirectoryField = new TextField();
        gridpane.add(inputDirectoryField, 1, 2);
        
        final TextField outputDirectoryField = new TextField();
        gridpane.add(outputDirectoryField, 1, 3);
        
        final TextField databaseDirectoryField = new TextField();
        gridpane.add(databaseDirectoryField, 1, 4);      
        
        Button inputDirectoryChooseButton = new Button("...");
        gridpane.add(inputDirectoryChooseButton, 2, 2);
        inputDirectoryChooseButton.setOnAction(new EventHandler<ActionEvent>() {
    		
        	public void handle(ActionEvent action) {
        		DirectoryChooser chooser = new DirectoryChooser();
        		chooser.setTitle("Choose input directory");
        		chooser.setInitialDirectory(new File(System.getProperty("user.dir") + File.separator + "config"));
        		System.out.println(System.getProperty("user.dir"));
        		
        		File file = chooser.showDialog(Main.getPrimaryStage());
        		if (file != null) {
        			// get full path and populate the text box
        			inputDirectoryField.setStyle("-fx-text-inner-color: #000000;");
        			inputDirectoryField.setText(file.getAbsolutePath());
        			Main.setInputDirectoryPath(file.getAbsolutePath());
        		}
        	}
        });
        
        Button outputDirectoryChooseButton = new Button("...");
        gridpane.add(outputDirectoryChooseButton, 2, 3);
        outputDirectoryChooseButton.setOnAction(new EventHandler<ActionEvent>() {
    		
        	public void handle(ActionEvent action) {
        		DirectoryChooser chooser = new DirectoryChooser();
        		chooser.setTitle("Choose output directory");
        		chooser.setInitialDirectory(new File(System.getProperty("user.dir") + File.separator + "config"));
        		File file = chooser.showDialog(Main.getPrimaryStage());
        		if (file != null) {
        			// get full path and populate the text box
        			outputDirectoryField.setStyle("-fx-text-inner-color: #000000;");
        			outputDirectoryField.setText(file.getAbsolutePath());
        			Main.setOutputDirectoryPath(file.getAbsolutePath());
        		}
        	}
        });
        
        Button databaseDirectoryChooseButton = new Button("...");
        gridpane.add(databaseDirectoryChooseButton, 2, 4);
        databaseDirectoryChooseButton.setOnAction(new EventHandler<ActionEvent>() {
    		
        	public void handle(ActionEvent action) {
        		DirectoryChooser chooser = new DirectoryChooser();
        		chooser.setTitle("Choose database directory");
        		chooser.setInitialDirectory(new File(System.getProperty("user.dir") + File.separator + "config"));
        		File file = chooser.showDialog(Main.getPrimaryStage());
        		if (file != null) {
        			// get full path and populate the text box
        			databaseDirectoryField.setStyle("-fx-text-inner-color: #000000;");
        			databaseDirectoryField.setText(file.getAbsolutePath());
        			Main.setDatabaseDirectoryPath(file.getAbsolutePath());
        		}
        	}
        });
        
        Button workingDirectoryChooseButton = new Button("...");
        gridpane.add(workingDirectoryChooseButton, 2, 1);
        workingDirectoryChooseButton.setOnAction(new EventHandler<ActionEvent>() {
		
        	public void handle(ActionEvent action) {
        		DirectoryChooser chooser = new DirectoryChooser();
        		chooser.setTitle("Choose working directory");
        		chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        		File file = chooser.showDialog(Main.getPrimaryStage());
        		if (file != null) {
        			// get full path and populate the text box
        			workingDirectoryField.setStyle("-fx-text-inner-color: #000000;");
        			workingDirectoryField.setText(file.getAbsolutePath());
        		}
        		
        		File inputDirectory = new File(workingDirectoryField.getText() + File.separator + "in"); 
            	File outputDirectory = new File(workingDirectoryField.getText() + File.separator + "out");
            	File databaseDirectory = new File(workingDirectoryField.getText() + File.separator + "data");
            	
            	if(inputDirectory.exists()) {
            		inputDirectoryField.setStyle("-fx-text-inner-color: #000000;");
            		inputDirectoryField.setText(inputDirectory.getAbsolutePath());
            	}
            	else {
            		Text errorText = new Text("FOLDER NOT FOUND!   INSERT THE FOLDER MANUALLY   	--->");
            		inputDirectoryField.setStyle("-fx-text-inner-color: #FF0000;");
            		inputDirectoryField.setText(errorText.getText());
            	}
            	
            	if(outputDirectory.exists()) {
            		outputDirectoryField.setStyle("-fx-text-inner-color: #000000;");
            		outputDirectoryField.setText(outputDirectory.getAbsolutePath());
            	}
            	else {
            		Text errorText = new Text("FOLDER NOT FOUND!   INSERT THE FOLDER MANUALLY   	--->");
            		outputDirectoryField.setStyle("-fx-text-inner-color: #FF0000;");
            		outputDirectoryField.setText(errorText.getText());
            	}
            	
            	if(databaseDirectory.exists()) {
            		databaseDirectoryField.setStyle("-fx-text-inner-color: #000000;");
            		databaseDirectoryField.setText(databaseDirectory.getAbsolutePath());
            	}
            	else {
            		Text errorText = new Text("FOLDER NOT FOUND!   INSERT THE FOLDER MANUALLY   	--->");
            		databaseDirectoryField.setStyle("-fx-text-inner-color: #FF0000;");
            		databaseDirectoryField.setText(errorText.getText());
            	}
        	}
        });
        
        Button start = new Button("Start");
        
        final Tooltip warning = new Tooltip("WARNING : One of the folder selected does not exists !!");
        start.setOnMouseEntered(new EventHandler<MouseEvent>() {
        	
        	@Override
        	public void handle(MouseEvent event) {
        		Point2D p = start.localToScreen(-2.5*start.getLayoutBounds().getMaxX(), 1.2*start.getLayoutBounds().getMaxY());
        		if((!new File(workingDirectoryField.getText()).exists())
        				|| (!new File(inputDirectoryField.getText()).exists())
        				|| (!new File(outputDirectoryField.getText()).exists())
        				|| (!new File(databaseDirectoryField.getText()).exists())
        				) {
        			warning.show(start, p.getX(), p.getY());
        		}
        	}
        });
        start.setOnMouseExited(new EventHandler<MouseEvent>() {
        	
        	@Override
        	public void handle(MouseEvent event) {
        		warning.hide();
        	}
        });
        
        start.disableProperty().bind(
        	    Bindings.isEmpty(workingDirectoryField.textProperty())
        	    .or(Bindings.isEmpty(inputDirectoryField.textProperty()))
        	    .or(Bindings.isEmpty(outputDirectoryField.textProperty()))
        	    .or(Bindings.isEmpty(databaseDirectoryField.textProperty()))
        	    );
        	
        start.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
            	Main.setInputDirectoryPath(inputDirectoryField.getText());
            	Main.setOutputDirectoryPath(outputDirectoryField.getText());
            	Main.setDatabaseDirectoryPath(databaseDirectoryField.getText());
            	if((new File(workingDirectoryField.getText()).exists())
        				&& (new File(inputDirectoryField.getText()).exists())
        				&& (new File(outputDirectoryField.getText()).exists())
        				&& (new File(databaseDirectoryField.getText()).exists())
        				)
            		close();
            }
            
        });
        gridpane.add(start, 1, 5);
        GridPane.setHalignment(start, HPos.RIGHT);
        root.getChildren().add(gridpane);
    }
}