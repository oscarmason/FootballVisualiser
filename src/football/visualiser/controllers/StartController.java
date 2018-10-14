package football.visualiser.controllers;

import football.visualiser.models.DataProperty;
import football.visualiser.view.AnalyseView;
import football.visualiser.view.DataItem;
import football.visualiser.view.MatchView;
import football.visualiser.view.StartView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**<h1>Start Controller</h1>
 * Created by Dovydas Ciomenas on 21/02/2017.
 * <p>
 * Controller for StartView.
 * @author Dovydas Ciomenas, Oscar Mason, Irene Zeng
 */
public class StartController {
    private Stage stage;
    private StartView startView;
    private AnalyseView analyseView;
    private File selectedFile;
    private Stage fileChooserStage; // Stage for FileChooser
    private boolean analysisStarted;
    private Stage progressStage;
    private Thread analyseThread;

    // Buttons for StartView
    @FXML
    private Button newMatchButton;

    @FXML
    private VBox dataItemBox;

    /**
     * Passes StartView object to Controller.
     *
     * @param startView StartView
     */
    public void setStartView(StartView startView) {
        this.startView = startView;
    }

    @FXML
    public void initialize() {
        loadDataItems();
    }

    /**
     * Because analysing the match can take some time, this needs to be put in a background thread.
     * Only when the thread has completed its task of analysing the data should it display the match scene
     *
     * Author: Oscar Mason, Dovydas Ciomenas, Irene Zeng
     * @param matchProp     Match property
     */
    private void loadMatch(DataProperty matchProp) throws Exception{
        if(analysisStarted) return;

        analysisStarted = true;

        stage = (Stage) dataItemBox.getScene().getWindow();

        MatchView matchView = new MatchView(stage);
        PitchController pitchController = matchView.getController();

        String path = matchProp.getProperty("path");
        String[] matchTimeStamps = matchProp.getProperty("matchTimeStamps").trim().split(",");
        String[] tempCoordinates = matchProp.getProperty("pitchCoordinates").trim().split(",");
        String[] tempIDs = matchProp.getProperty("footballIDs").trim().split(",");

        int[] coordinates = new int[4];
        for(int i = 0; i < 4; i++) {
            coordinates[i] = Integer.parseInt(tempCoordinates[i]);
        }

        int[] IDs = new int[tempIDs.length];
        for (int i = 0; i < tempIDs.length; i++) {
            IDs[i] = Integer.parseInt(tempIDs[i]);
        }

        Task<Void> analyseMatchTask = new Task<Void>(){
            @Override
            protected Void call() throws Exception {
                pitchController.setupMatch(matchView, path, matchTimeStamps, coordinates, IDs);
                return null;
            }
        };

        analyseMatchTask.setOnSucceeded(e-> {
            stage.setScene(matchView.getScene());
            stage.centerOnScreen();
            stage.setResizable(true);
            stage.sizeToScene();
            progressStage.close();
            stage.show();
            analysisStarted = false;
        });



        analyseMatchTask.setOnRunning(e-> {
            //stage.hide();
            progressStage = new Stage();
            progressStage.setOnCloseRequest(event-> {
                analyseThread.interrupt();
                if(analyseThread.isInterrupted()) {
                    System.exit(1);
                }

            });
            startView.showProgress(progressStage);

        });

        analyseMatchTask.setOnFailed(e->{
            progressStage.close();
            startView.showLoadingError();
            analysisStarted = false;
        });

        analyseThread = new Thread(analyseMatchTask);
        analyseThread.start();
    }

    private void loadDataItems() {
        dataItemBox.getChildren().clear();
        try {
            File folder = new File("resources/properties/");
            if(!folder.exists()) {
                new File("resources/properties/").mkdir();
            }

            File[] listOfFiles = folder.listFiles();

            int i = 0;
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    DataProperty prop = new DataProperty(file.getName().replace(".properties", ""));

                    DataItem item = new DataItem(
                            i, prop.getProperty("name", "Null"), prop.getProperty("path", "Null")
                    );

                    item.setOnMouseClicked(handleItemClick);
                    item.getRemoveButton().setOnAction(e -> handleRemoveButton(item));

                    dataItemBox.getChildren().add(item);
                }
                i++;
            }

            if(dataItemBox.getChildren().isEmpty()) {
                Text emptyText = new Text("No Recent Matches");
                emptyText.setId("emptyText");
                emptyText.setTextAlignment(TextAlignment.CENTER);
                dataItemBox.setAlignment(Pos.CENTER);
                dataItemBox.getChildren().add(emptyText);
                dataItemBox.setCursor(Cursor.DEFAULT);
            } else {
                dataItemBox.setCursor(Cursor.HAND);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRemoveButton(DataItem item) {
        try {
            File folder = new File("resources/properties/");
            File[] listOfFiles = folder.listFiles();

            for(File file : listOfFiles) {
                if(file.isFile()) {
                    if(file.getName().replace(".properties", "").equals(item.getName())) {
                        if(!file.delete()) {
                            System.out.println("Could not remove " + item.getName() + " property");
                        } else {
                            loadDataItems();
                        }
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public EventHandler<MouseEvent> handleItemClick = event -> {
        Object source = event.getSource();
        if(source instanceof DataItem){
            DataItem dataItem = (DataItem) source;
            try {
                loadMatch(new DataProperty(dataItem.getName()));
            } catch(Exception e) {
            }
        }
    };

    /**
     * Handles "New Match" Button in StartView.
     * User selects file through FileChooser;
     * If the file has been selected, pass it to the AnalyseView and initialise it.
     *
     * @throws Exception
     * @author Dovydas Ciomenas
     */
    @FXML
    public void handleNewMatchButton() throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Match Data File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        selectedFile = fileChooser.showOpenDialog(fileChooserStage);
        if (selectedFile != null) {

            // Switches the scene to AnalyseView
            stage = (Stage) newMatchButton.getScene().getWindow();
            analyseView = new AnalyseView(selectedFile);
            stage.setScene(analyseView.getScene());
            stage.centerOnScreen();
            stage.setResizable(false);
            stage.show();
        }
    }

    /**
     * Handles "About" Button.
     *
     * @author Dovydas Ciomenas
     */
    @FXML
    public void handleAboutButton() {
        startView.showAbout();
    }


}
