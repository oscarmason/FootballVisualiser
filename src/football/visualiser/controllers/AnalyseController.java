package football.visualiser.controllers;

import football.visualiser.view.AnalyseView;
import football.visualiser.view.MatchView;
import football.visualiser.view.StartView;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Properties;

/**<h1>Analyse Controller</h1>
 * Created by Dovydas Ciomenas on 07/03/2017.
 * <p>
 * Controller for AnalyseView.
 * Allows user to input properties for a selected match data file and proceed to the match itself.
 * @author Dovydas Ciomenas, Oscar Mason, Irene Zeng
 */
public class  AnalyseController {
    private Stage stage;
    private StartView startView;
    private AnalyseView analyseView;
    private File selectedFile;
    private int[] IDs;
    private int[] coordinates;
    private String[] matchTimeStamp;
    private boolean analysisStarted;
    private Stage progressStage;
    private Thread analyseThread;

    // Buttons for AnalyseView
    @FXML
    private Button closeButton;
    @FXML
    private Button analyseButton;

    // User input Nodes
    @FXML
    private TextField textFieldForName;
    @FXML
    private TextField textFieldForID;
    @FXML
    private TextField textFieldFor1startTime;
    @FXML
    private TextField textFieldFor1endTime;
    @FXML
    private TextField textFieldFor2startTime;
    @FXML
    private TextField textFieldFor2endTime;
    @FXML
    private TextField textFieldForStartX;
    @FXML
    private TextField textFieldForEndX;
    @FXML
    private TextField textFieldForStartY;
    @FXML
    private TextField textFieldForEndY;

    public AnalyseController(File selectedFile) {
        this.selectedFile = selectedFile;
    }


    /**
     * Passes AnalyseView to Controller.
     *
     * @param analyseView analyseView
     */
    public void setAnalyseView(AnalyseView analyseView) {
        this.analyseView = analyseView;
    }

    /**
     * Handles Close ("Back") Button.
     * Initialises the StartView.
     *
     * @throws Exception
     */
    @FXML
    public void handleCloseButton() throws Exception {
        selectedFile = null;
        stage = (Stage) closeButton.getScene().getWindow();
        StartView startView = new StartView();
        stage.setScene(startView.getScene());
        stage.centerOnScreen();
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Because analysing the match can take some time, this needs to be put in a background thread.
     * Only when the thread has completed its task of analysing the data should it display the match scene
     *
     * Author: Oscar Mason, Dovydas Ciomenas, Irene Zeng
     */
    public void loadMatch() throws Exception {
        if(analysisStarted) return;

        analysisStarted = true;

        stage = (Stage) analyseButton.getScene().getWindow();

        MatchView matchView = new MatchView(stage);
        PitchController pitchController = matchView.getController();

        Task<Void> analyseMatchTask = new Task<Void>(){
            @Override
            protected Void call() throws Exception {
                pitchController.setupMatch(
                    matchView, selectedFile.toString(), getMatchTimeStamps(), getPitchCoordinates(), getFootballIDs());
                return null;
            }
        };

        // Once the match has been analysed, switch to the match view scene
        analyseMatchTask.setOnSucceeded(e->{
            stage.setScene(matchView.getScene());
            stage.centerOnScreen();
            stage.setResizable(true);
            stage.sizeToScene();
            progressStage.close();
            stage.show();
            saveProperties();
            analysisStarted = false;
        });

        analyseMatchTask.setOnFailed(e->{
            progressStage.close();
            analyseView.showAnalyseError();
            try {
                handleCloseButton();
            } catch(Exception ev) {
                ev.printStackTrace();
            }

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
            analyseView.showProgress(progressStage);

        });

        analyseThread = new Thread(analyseMatchTask);
        analyseThread.start();
    }

    /**
     * Handles "Analyse" Button.
     * Initialises the MatchView and passes user input through MatchView.SetupMatch() to create and display new match.
     *
     * @throws Exception
     * @author Irene, Dovydas Ciomenas
     */
    @FXML
    public void handleAnalyseButton() {
        try {
            loadMatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveProperties() {
        //TODO: make it not possible to save properties with the same name.
        try {
            Properties prop = new Properties();
            prop.setProperty("name", textFieldForName.getText());
            prop.setProperty("path", selectedFile.toString());
            prop.setProperty(
                    "matchTimeStamps", textFieldFor1startTime.getText() + "," + textFieldFor1endTime.getText()
                    + "," + textFieldFor2startTime.getText() + "," + textFieldFor2endTime.getText()
            );
            prop.setProperty(
                    "pitchCoordinates", textFieldForStartX.getText() + "," + textFieldForEndX.getText() + ","
                    + textFieldForStartY.getText() + "," + textFieldForEndY.getText()
            );
            prop.setProperty("footballIDs", textFieldForID.getText().trim());
            File file = new File("resources/properties/" + textFieldForName.getText() + ".properties");
            OutputStream out = new FileOutputStream(file);
            prop.store(out, "Properties for " + textFieldForName.getText());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets Football IDs from textFields
     *
     * @return int[]
     * @author Irene
     */
    public int[] getFootballIDs() {
        String FootballsIDs = textFieldForID.getText().trim();
        String[] temp = FootballsIDs.split(",");
        IDs = new int[temp.length];

        for (int i = 0; i < temp.length; i++) {
            IDs[i] = Integer.parseInt(temp[i]);
        }
        return IDs;
    }

    /**
     * Gets Pitch Coordinates from textFields.
     *
     * @return int[]
     * @author Irene
     */
    public int[] getPitchCoordinates() {
        String startX = textFieldForStartX.getText().trim();
        String endX = textFieldForEndX.getText().trim();
        String startY = textFieldForStartY.getText().trim();
        String endY = textFieldForEndY.getText().trim();
        coordinates = new int[4];

        coordinates[0] = Integer.parseInt(startX);
        coordinates[1] = Integer.parseInt(endX);
        coordinates[2] = Integer.parseInt(startY);
        coordinates[3] = Integer.parseInt(endY);

        return coordinates;
    }

    /**
     * Gets Match Time Stamps from textFields.
     *
     * @return String[]
     * @author Irene
     */
    public String[] getMatchTimeStamps() {
        String startTimeFor1 = textFieldFor1startTime.getText().trim();
        String endTimeFor1 = textFieldFor1endTime.getText().trim();
        String startTimeFor2 = textFieldFor2startTime.getText().trim();
        String endTimeFor2 = textFieldFor2endTime.getText().trim();

        matchTimeStamp = new String[4];

        matchTimeStamp[0] = startTimeFor1;
        matchTimeStamp[1] = endTimeFor1;
        matchTimeStamp[2] = startTimeFor2;
        matchTimeStamp[3] = endTimeFor2;

        return matchTimeStamp;
    }

}
