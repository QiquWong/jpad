package sandbox.adm.javafxd3.test;

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class MyApplication extends Application {

    private static final double WIDTH = 800;
    private static final double HEIGHT = 600;

    // Just a counter to create some delay while showing preloader.
    private static final int COUNT_LIMIT = 500000;

    private static int stepCount = 1;

    // Used to demonstrate step couns.
    public static String STEP() {
        return stepCount++ + ". ";
    }

    private Stage applicationStage;

    public static void main(String[] args) {
        LauncherImpl.launchApplication(MyApplication.class, MyPreloader.class, args);
    }

    public MyApplication() {
        // Constructor is called after BEFORE_LOAD.
        System.out.println(MyApplication.STEP() + "MyApplication constructor called, thread: " + Thread.currentThread().getName());
    }

    @Override
    public void init() throws Exception {
        System.out.println(MyApplication.STEP() + "MyApplication#init (doing some heavy lifting), thread: " + Thread.currentThread().getName());

        // Perform some heavy lifting (i.e. database start, check for application updates, etc. )
        for (int i = 0; i < COUNT_LIMIT; i++) {
            double progress = (100 * i) / COUNT_LIMIT;
            LauncherImpl.notifyPreloader(this, new Preloader.ProgressNotification(progress));
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println(MyApplication.STEP() + "MyApplication#start (initialize and show primary application stage), thread: " + Thread.currentThread().getName());

        applicationStage = primaryStage;

        Label title = new Label("This is your application!");
        title.setTextAlignment(TextAlignment.CENTER);

        VBox root = new VBox(title);
        root.setAlignment(Pos.CENTER);

        // Create scene and show application stage.
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        applicationStage.setScene(scene);
        applicationStage.show();
    }

}
