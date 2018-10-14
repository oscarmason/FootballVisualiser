package football.visualiser.view;


import football.visualiser.FootballVisualiser;
import football.visualiser.controllers.StartController;
import football.visualiser.interfaces.IViewable;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

/**
 * <h1>Start View</h1>
 * The landing stage view for Football Visualiser.
 * Can load a new match, display the saved properties and show 'about' dialog.
 *
 *
 * @author Dovydas Ciomenas
 */

public class StartView implements IViewable {
    private Scene scene;
    private FXMLLoader fxmlLoader;
    private AnchorPane rootPane;
    private VBox dataItemBox;
    private Image footballVisualiserLogo;
    private ImageView fbLogo;

    public StartView() throws IOException {
        initialiseView();
    }

    /**
     * Initialises the startView scene.
     * Loads StartLayout.fxml;
     * Passes itself to controller;
     * Adds images for logo and graphics for buttons;
     * Gets Version number from MANIFEST.
     *
     * @throws Exception
     * @author Dovydas Ciomenas
     */
    @Override
    public void initialiseView() throws IOException {
        fxmlLoader = new FXMLLoader(getClass().getResource("StartLayout.fxml"));
        StartController startController = new StartController();
        fxmlLoader.setController(startController);
        Parent fxmlRoot = fxmlLoader.load();
        fxmlRoot.getStylesheets().add(getClass().getResource("StartStyle.css").toExternalForm());
        scene = new Scene(fxmlRoot);
        startController.setStartView(this);
        rootPane = (AnchorPane) scene.lookup("#root");



        loadImages();
        displayVersion();

        dataItemBox = (VBox) scene.lookup("#dataItemBox");
        ScrollPane propScroll = (ScrollPane) scene.lookup("#propScroll");
        propScroll.setFitToWidth(true);



    }

    public void showProgress(Stage progressStage) {
        progressStage.initStyle(StageStyle.UNDECORATED);
        progressStage.setTitle("Please Wait");
        BorderPane progressRoot = new BorderPane();
        progressRoot.setId("progressPane");
        Scene progressScene = new Scene(progressRoot, 300, 150);
        progressStage.setScene(progressScene);
        progressStage.centerOnScreen();
        progressStage.setAlwaysOnTop(true);
        progressStage.show();
        progressStage.setResizable(false);
        progressStage.setIconified(false);

        progressRoot.setStyle("-fx-background-color: rgb(32, 33, 42);");

        ProgressIndicator progressIndic = new ProgressIndicator();
        Text loadingText = new Text("Loading Match Data");
        loadingText.setFont(Font.font("Avenir", 20));
        loadingText.setFill(Color.rgb(202, 203, 212));


        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(25);


        vbox.getChildren().addAll(progressIndic, loadingText);



        progressRoot.setCenter(vbox);
    }


    private void displayVersion() {
        Text version = (Text) scene.lookup("#versionValue");
        version.setText("1.0");
    }

    private void loadImages() {
        fbLogo = (ImageView) scene.lookup("#logo");
        footballVisualiserLogo = new Image(
                getClass().getClassLoader().getResource("images/FVlogo.png").toExternalForm()
        );
        fbLogo.setImage(footballVisualiserLogo);

        Button newMatchButton = (Button) scene.lookup("#newMatchButton");
        Image newMatchImage = new Image(
                getClass().getClassLoader().getResource("images/newMatchIcon1blue.png").toExternalForm()
        );
        newMatchButton.setGraphic(new ImageView(newMatchImage));

        Button aboutButton = (Button) scene.lookup("#aboutButton");
        Image aboutImage = new Image(
                getClass().getClassLoader().getResource("images/aboutIcon1blue.png").toExternalForm()
        );
        aboutButton.setGraphic(new ImageView(aboutImage));
    }

    public void showLoadingError() {
        Alert alert = new Alert(Alert.AlertType.ERROR,
                "The file is missing or the data is corrupted.", ButtonType.CLOSE);
        alert.setTitle("Error");
        alert.setHeaderText("Loading Match Failed");
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("DialogStyle.css").toExternalForm());

        alert.showAndWait();
    }

    public void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "An application developed by University of Nottingham students for the Software Engineering Group Project that facilitates visual analytics of football games by coaches, journalists, as well as computer game developers.",
                ButtonType.CLOSE);
        alert.setTitle("About");
        alert.setHeaderText("About Football Visualiser");
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("DialogStyle.css").toExternalForm());

        alert.showAndWait();

    }

    @Override
    public Scene getScene() {
        return scene;
    }

    public StartController getController() {
        return fxmlLoader.getController();
    }

}