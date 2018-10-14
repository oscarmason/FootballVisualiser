package football.visualiser;

import football.visualiser.view.StartView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;

/**
 * <h1>Football Visualiser Main</h1>
 * Launches JavaFX application and initialises the Start View
 */
public class FootballVisualiser extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Football Visualiser");

        // initialise startView
        StartView startView = new StartView();
        primaryStage.setScene(startView.getScene());
        primaryStage.setResizable(false);
        primaryStage.show();

    }

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Football Visualiser");

        launch(args);

    }
}
