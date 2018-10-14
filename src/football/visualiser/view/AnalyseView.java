package football.visualiser.view;

import football.visualiser.controllers.AnalyseController;
import football.visualiser.interfaces.IViewable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;

/**<h1>Analyse View</h1>
 * Created by irene on 2017/2/21.
 * <p>
 * The AnalyseView scene displays a series of TextFields for user to input properties for a selectedFile's match data.
 *
 * @author Irene Zeng, Dovydas Ciomenas
 */

public class AnalyseView implements IViewable {
    private Scene scene;
    private FXMLLoader fxmlLoader;
    private AnchorPane rootPane;
    private TextField textFieldForStartX;
    private TextField textFieldForEndX;
    private TextField textFieldForStartY;
    private TextField textFieldForEndY;
    private TextField textFieldForID;
    private TextField textFieldForName;
    private TextField textFieldFor1startTime;
    private TextField textFieldFor1endTime;
    private TextField textFieldFor2startTime;
    private TextField textFieldFor2endTime;
    private Button analyseButton;
    private AnalyseController analyseController;
    private File selectedFile;

    /**
     * When initialised, gets the selectedFile from StartController initialised by StartView.
     *
     * @param selectedFile the file which was selected through FileChooser in StartController initialised by StartView.
     * @author Dovydas Ciomenas
     */
    public AnalyseView(File selectedFile) throws IOException {
        this.selectedFile = selectedFile;
        initialiseView();
    }

    /**
     * Initializes the view of AnalyseView and binds TextFields to analyseButton's disableProperty;
     * Adds constraints for the Text Fields;
     * Passes a selectedFile to a analyseController;
     * Layout defined in AnalyseLayout.fxml.
     *
     * @throws Exception
     * @author Irene, Dovydas Ciomenas
     */
    @Override
    public void initialiseView() throws IOException {
        fxmlLoader = new FXMLLoader(getClass().getResource("AnalyseLayout.fxml"));
        analyseController = new AnalyseController(selectedFile);
        fxmlLoader.setController(analyseController);
        Parent fxmlRoot = fxmlLoader.load();
        fxmlRoot.getStylesheets().add(getClass().getResource("AnalyseStyle.css").toExternalForm());
        scene = new Scene(fxmlRoot);
        analyseController.setAnalyseView(this);

        rootPane = (AnchorPane) scene.lookup("#root");
        textFieldForStartX = (TextField) scene.lookup("#textFieldForStartX");
        textFieldForEndX = (TextField) scene.lookup("#textFieldForEndX");
        textFieldForStartY = (TextField) scene.lookup("#textFieldForStartY");
        textFieldForEndY = (TextField) scene.lookup("#textFieldForEndY");
        textFieldFor1startTime = (TextField) scene.lookup("#textFieldFor1startTime");
        textFieldFor1endTime = (TextField) scene.lookup("#textFieldFor1endTime");
        textFieldFor2startTime = (TextField) scene.lookup("#textFieldFor2startTime");
        textFieldFor2endTime = (TextField) scene.lookup("#textFieldFor2endTime");
        textFieldForID = (TextField) scene.lookup("#textFieldForID");
        textFieldForName = (TextField) scene.lookup("#textFieldForName");
        analyseButton = (Button) scene.lookup("#analyseButton");

        analyseButton.disableProperty().bind(Bindings.isEmpty(
                textFieldForStartX.textProperty())
                .or(Bindings.isEmpty(textFieldForName.textProperty()))
                .or(Bindings.isEmpty(textFieldForEndX.textProperty()))
                .or(Bindings.isEmpty(textFieldForStartY.textProperty()))
                .or(Bindings.isEmpty(textFieldForEndY.textProperty()))
                .or(Bindings.isEmpty(textFieldFor1startTime.textProperty()))
                .or(Bindings.isEmpty(textFieldFor1endTime.textProperty()))
                .or(Bindings.isEmpty(textFieldFor2startTime.textProperty()))
                .or(Bindings.isEmpty(textFieldFor2endTime.textProperty()))
                .or(Bindings.isEmpty(textFieldForID.textProperty()))

        );

        constraintForStartX();
        constraintForEndX();
        constraintForStartY();
        constraintForEndY();
    }


    public void constraintForStartX() {
        textFieldForStartX.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                if (!arg2.matches("^\\-?[0-9]*$")) {
                    textFieldForStartX.setText(arg2.replaceAll(arg2, ""));
                }
            }
        });
    }

    public void constraintForEndX() {
        textFieldForEndX.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                if (!arg2.matches("^\\-?[0-9]*$")) {
                    textFieldForEndX.setText(arg2.replaceAll(arg2, ""));
                }
            }
        });
    }

    public void constraintForStartY() {
        textFieldForStartY.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                if (!arg2.matches("^\\-?[0-9]*$")) {
                    textFieldForStartY.setText(arg2.replaceAll(arg2, ""));
                }
            }
        });
    }

    public void constraintForEndY() {
        textFieldForEndY.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                if (!arg2.matches("^\\-?[0-9]*$")) {
                    textFieldForEndY.setText(arg2.replaceAll(arg2, ""));
                }
            }
        });
    }

    public void showAnalyseError() {
        Alert alert = new Alert(Alert.AlertType.ERROR,
                "Unable to analyse the data file or the provided properties do not match the data.",
                ButtonType.CLOSE);
        alert.setTitle("Failed");
        alert.setHeaderText("Analysis Failed");
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("DialogStyle.css").toExternalForm());
        alert.showAndWait();

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
        progressStage.setResizable(false);
        progressStage.setIconified(false);
        progressRoot.setStyle("-fx-background-color: rgb(32, 33, 42);");
        
        ProgressIndicator progressIndic = new ProgressIndicator();
        Text loadingText = new Text("Analysing Match Data");
        loadingText.setFont(Font.font("Avenir", 20));
        loadingText.setFill(Color.rgb(202, 203, 212));
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(25);
        vbox.getChildren().addAll(progressIndic, loadingText);
        progressRoot.setCenter(vbox);
        progressStage.show();
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    public AnalyseController getController() {
        return analyseController;
    }

}