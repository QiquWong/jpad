package sandbox2.mds.javafx;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class JavaFX_Test_01 extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("JavaFX Welcome");

		// setting the grid
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));
		
		// adding text and labels to the grid
		Text sceneTitle = new Text("Welcome");
		sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		grid.add(sceneTitle, 0, 0, 2, 1);
		
		Label userName = new Label("User Name:");
		grid.add(userName, 0, 1);
		
		TextField userTextField = new TextField();
		grid.add(userTextField, 1, 1);
		
		Label pw = new Label("Password:");
		grid.add(pw, 0, 2);
		
		PasswordField pwBox = new PasswordField();
		grid.add(pwBox, 1, 2);
		
		//grid.setGridLinesVisible(true);
		
		// adding a button
		Button btn = new Button("Sign in");
		HBox hbBtn = new HBox(10);
		hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtn.getChildren().add(btn);
		grid.add(hbBtn, 1, 4);
		
		final Text actionTarget = new Text();
		grid.add(actionTarget, 1, 6);
		
		// adding an event to the button when pressed
		btn.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				actionTarget.setFill(Color.FIREBRICK);
				actionTarget.setText("Sign in button pressed");
			}	
		});
			
		// setting the scene
		Scene scene = new Scene(grid, 300, 275);
		
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
