package football.visualiser.controllers;
import football.visualiser.interfaces.IFootballSensor;
import football.visualiser.interfaces.IMatchData;
import football.visualiser.interfaces.IMatchModelListener;
import football.visualiser.interfaces.IPlayerSensor;
import football.visualiser.models.*;
import football.visualiser.models.entities.Football;
import football.visualiser.models.entities.GoalKeeper;
import football.visualiser.models.entities.Player;
import football.visualiser.view.*;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static football.visualiser.SystemData.*;
import static football.visualiser.view.Highlight.HighlightType.*;

/**
 * <h1>Pitch Controller</h1>
 * Handles all communication between the model and the view when in game playback mode
 *
 * @author Oscar Mason, Irene Zeng
 */


public class PitchController implements IMatchModelListener {

    private MatchView matchView;
    private IMatchData matchData;
    private Pitch pitch;
    private MatchModel matchModel;
    private DataAnalyser dataAnalyser;
    public PitchController(){};

    /**
     * Setups all necessary components to load and display the match
     * Author: Oscar Mason
     *
     * @param matchView             Handles the GUI
     * @param matchDataFileLocation Location of the match data file on the user's computer
     * @param matchTimeStamps       Time stamps of when the first half and second half starts and ends
     * @param pitchCoordinates      X and Y coordinate of where the pitch starts and ends
     * @param footballIDs           Which entity IDs are footballs
     * @throws IOException          If the match data file fails to load or if the match view fails
     *                              to initialise
     */
    public void setupMatch(MatchView matchView, String matchDataFileLocation, String[] matchTimeStamps,
                           int[] pitchCoordinates, int[] footballIDs) throws IOException {
        this.matchData = new MatchData(matchDataFileLocation, matchTimeStamps);
        this.matchView = matchView;
        pitch = new Pitch(pitchCoordinates[PITCH_START_X], pitchCoordinates[PITCH_END_X],
                pitchCoordinates[PITCH_START_Y], pitchCoordinates[PITCH_END_Y]);
        matchView.setPitchRatio(pitch.getRatio());

        dataAnalyser = new DataAnalyser(footballIDs);

        matchModel = new MatchModel(matchData, dataAnalyser, pitch, this);
        matchModel.addMatchModelObserver(this);
        setSliderListener();

        setupEntities();
        setPerformanceButtonListener();

        matchModel.analyseMatchData(dataAnalyser, pitch);

        addHighlightTimes(dataAnalyser.getRedTeam1_2_1Times(), RED_121_PASS);
        addHighlightTimes(dataAnalyser.getBlueTeam1_2_1Times(), BLUE_121_PASS);
        addHighlightTimes(dataAnalyser.getTackleTimes(), TACKLE);
        addHighlightTimes(dataAnalyser.getBlueTeamGoalTimes(), BLUE_GOAL);
        addHighlightTimes(dataAnalyser.getRedTeamGoalTimes(), RED_GOAL);

        matchView.showAllHighlights();

        updateOverviewPane();

        matchView.setEnterHandler(EnterBarHandler);

        Platform.runLater(matchView::bindComponentsToScene);
    }


    /**
     * Takes a list of highlight times and instructs to match view to add the items to the seek bar
     * Author: Oscar Mason
     *
     * @param listOfTimes       Times at which the highlight occurred
     * @param highlightType     Type of highlight i.e. tackle goal
     */
    private void addHighlightTimes(List<Double> listOfTimes, Highlight.HighlightType highlightType){
        for(double time : listOfTimes){
            matchView.addHighlight(highlightType, time, this);
        }
    }

    /**
     * Attaches a performance view button listener to performance button on the player card
     */
    private void setPerformanceButtonListener() {
        matchView.getPlayerCard().getPerformanceButton().setOnAction(e -> {
            try {
                new PerformanceView(matchView.getPlayerCard().getPlayerID(), matchView.getPlayerCard().getPlayerTeam(),
                        dataAnalyser.getIndividualBlueGoals(), dataAnalyser.getIndividualRedGoals(),
                        dataAnalyser.getIndividualSuccPasses(),
                        dataAnalyser.getIndividualBlueTackles(), dataAnalyser.getIndividualRedTackles(),
                        getPossessionTimes(Team.BLUE), getPossessionTimes(Team.RED),
                        getDistance(matchView.getPlayerCard().getPlayerID())
                );
            } catch(IOException exc) {
                exc.printStackTrace();
            }
        });
    }

    /**
     * Provides each player with a new heat map object
     * Author: Oscar Mason
     * @param player    Player to give the object to
     */
    public void addHeatMapToPlayers(Player player){
        player.addHeatMap(new HeatMap(pitch));
    }


    /**
     * Creates all the hashmaps for the players, goal keepers, and footballs
     * Author: Oscar Mason
     */
    public void setupEntities(){
        HashMap<Integer, int[]> startPositions;
        HashMap<Integer, IPlayerSensor<Player>> playerSensors = new HashMap<>();
        HashMap<Integer, IFootballSensor<Football>> footballSensors = new HashMap<>();
        HashMap<Integer, Player> players = new HashMap<>();
        HashMap<Integer, Football> footballs  = new HashMap<>();

        startPositions = dataAnalyser.calculateEntityAverageStartPosition(matchData);

        dataAnalyser.createEntities(startPositions, playerSensors, footballSensors, players, footballs);
        dataAnalyser.assignPlayersToTeams(pitch.getX2(), players, playerSensors);
        matchModel.setEntityHashMaps(playerSensors, footballSensors, players, footballs);

        // For each player create a new player graphic and add a heat map
        for(Player player : players.values()){
            boolean isGoalKeeper = player instanceof GoalKeeper;

            matchView.addPlayer(player.getTeam(), player.getID(), isGoalKeeper, handlePlayerClick);

            addHeatMapToPlayers(player);
        }

        for(Football football : footballs.values()){
            matchView.addFootball(football.getID());
        }
    }

    /**
     * Handler for when the user clicks on a player to display the player card
     * Author: Oscar Mason
     */
    private EventHandler<MouseEvent> handlePlayerClick = event -> {
        Object source = event.getSource();
        if(source instanceof PlayerGraphic){
            PlayerGraphic playerGraphic = (PlayerGraphic) source;
            matchView.showPlayerInfoPane(
                    event.getSceneX(), event.getSceneY(),
                    matchModel.getPlayers().get(playerGraphic.getPlayerID())
            );
        }
    };

    /**
     * Handler to remove the player card when the user clicks on another any part of the scene
     * @param e     Object which was clicked
     */
    @FXML
    public void handleEmptyAreaClick(MouseEvent e){
        Object source = e.getSource();
        if(!(source instanceof PlayerGraphic)){
            matchView.removePlayerCard();
        }
    }

    /**
     * Handler to expand and shrink toolbar at the top
     */
    @FXML
    public void handleToolbarClick(){
        matchModel.setIsPlaying(false);
        matchView.setPlayButtonImage(true);
        if (!matchView.expandMenu()) {
            matchView.getOverviewPane().printTwoTeamGoals();
        }
    }

    /**
     * Listens for when the user drags and releases the slider along the seek bar
     * When the user drags the slider, it converts the current position on the seek bar to milliseconds and
     * updates the time along the top
     *
     * When the user releases their mouse, the game is progressed to the relevant position in the match and
     * the game resumes. The seek time bubble which displays the time is also removed
     *
     * Author: Oscar Mason
     */
    private void setSliderListener(){
        matchView.getSeekSlider().setOnMouseReleased((e) -> {
            matchModel.seek(matchView.getSeekSlider().getValue(),
                    matchData.getStartEndTimeStamps(), matchData.getTotalMatchTimeInMilliseconds());
            matchView.removeSeekTime();
            matchView.setPlayButtonImage(true);
        });

        matchView.getSeekSlider().setOnMouseDragged((e) -> {
                int seekTimeInSeconds = matchModel.convertSeekValueToMilliseconds(matchView.getSeekSlider().getValue(),
                        matchData.getTotalMatchTimeInMilliseconds());
                matchView.setSeekTimePosition(e);
                matchView.setSeekTimeText(seekTimeInSeconds);
        });
    }

    /**
     * Handles the mouse event for when the user clicks on the seek bar
     * When this event is fired the game must be paused to prevent the program from constantly attempting
     * to seek to the point in the file where the user has dragged to unnecessarily.
     * It also displays the small bubble which displays the current seek time
     *
     * Author: Oscar Mason
     *
     * @param event     The mouse event fired
     */
    @FXML
    public void handleSeekDown(MouseEvent event){
        matchModel.setIsPlaying(false);

        matchView.setSeekTimePosition(event);
        int seekTimeInSeconds = matchModel.convertSeekValueToMilliseconds(matchView.getSeekSlider().getValue(),
                matchData.getTotalMatchTimeInMilliseconds());
        matchView.setSeekTimeText(seekTimeInSeconds);
        matchView.showSeekTime();
    }


    /**
     * Handler for the playback button
     * Pauses and resumes the game and sets the relevant icon for the button
     *
     * Author: Oscar Mason
     */
    @FXML
    public void handlePlayButtonClick(){
        if(matchModel.getIsPlaying()){
            matchModel.setIsPlaying(false);
            matchView.setPlayButtonImage(true);
        }else{
            matchModel.setIsPlaying(true);
            matchView.setPlayButtonImage(false);
            matchModel.gameLoopStart();
        }
    }

    /**
     * Instructs the match view to update the position of the entity on screen to its new position
     * The offset of the pitch's x and y start coordinates has to be taken into account as the
     * pitch view itself starts from zero, but the match data provided may not
     *
     * Author: Oscar Mason
     *
     * @param ID    ID of the entity to update
     * @param x     updated x position of the entity
     * @param y     updated y position of the entity
     */
    @Override
    public void updatePosition(int ID, int x, int y) {
        double pitchWidthRatio = matchView.getPitchPane().getPrefWidth() / (double) pitch.getWidth();
        double pitchHeightRatio = matchView.getPitchPane().getPrefHeight() / (double) pitch.getHeight();
        int xFromZero = x - pitch.getX1();
        int yFromZero = y - pitch.getY1();
        xFromZero *= pitchWidthRatio;
        yFromZero *= pitchHeightRatio;
        matchView.updatePosition(ID, xFromZero, yFromZero);
    }

    /**
     * Handler to display a small popup with a description of the highlight type when the user hovers
     * their mouse over a highlight in the seek bar
     *
     * Author: Oscar Mason
     *
     * @param highlight     The highlight to assign the listener to
     * @param event         The mouse event that fires
     */
    @Override
    public void handleMouseHover(Highlight highlight, MouseEvent event) {
        matchView.setSeekTimeText(highlight.getHighlightTypeText());
        matchView.setSeekTimePosition(event);
        matchView.showSeekTime();
    }

    /**
     * Moves the thumb on the seek bar to the position of the highlight the user pressed on
     * @param timeRatio     The position of the seek bar to move to. Works as a ratio of its entire
     *                      width so will have a value between 0.0 and 1.0
     *
     * Author: Oscar Mason
     */
    @Override
    public void handleMousePressed(double timeRatio) {
        matchModel.setIsPlaying(false);
        matchView.moveSeekBarThumb(timeRatio);
    }

    /**
     * Handler for when the user releases their mouse from the highlight
     *
     * Author: Oscar Mason
     * @param timeRatio The position on the seek bar the user release from
     */
    @Override
    public void handleMouseReleased(double timeRatio){
        matchModel.seek(timeRatio,
                matchData.getStartEndTimeStamps(), matchData.getTotalMatchTimeInMilliseconds());
        matchView.removeSeekTime();
    }

    /**
     * Moves the cursor position depending on the current time as the match is played back
     *
     * Author: Oscar Mason
     * @param currentTime       Time to move the cursor to
     */
    @Override
    public void updateCursorPosition(int currentTime) {
        double timeRatio = dataAnalyser.convertMatchTimeToSeekbarRatio(matchData, currentTime);
        matchView.moveSeekBarThumb(timeRatio);
    }

    /**
     * When the user releases their mouse from a highlight, remove the small bubble
     */
    @Override
    public void handleMouseExited() {
        matchView.removeSeekTime();
    }

    /**
     * Updates the goal count in the toolbar as the game is progressed
     * Author: Oscar Mason
     * @param currentMillisecond    The current point in the match during playback
     */
    @Override
    public void updateGoalCount(int currentMillisecond) {
        Pair <Integer, Integer> goalCount = dataAnalyser.getGoalCount(currentMillisecond);
        int numberOfRedGoals = goalCount.getFirstValue();
        int numberOfBlueGoals = goalCount.getSecondValue();
        matchView.updateGoalCount(numberOfRedGoals, numberOfBlueGoals);
    }

    /**
     * Sets the individual goals to be displayed in the match over pane drop-down
     * Author: Irene Zeng
     */
    public void updateOverviewPane(){
        matchView.getOverviewPane().setIndividualGoals(dataAnalyser.getIndividualRedGoals(),
                dataAnalyser.getIndividualBlueGoals());
    }

    /**
     * Plays and pause the match when the user clicks the enter key on the keyboard
     * Author: Irene Zeng
     */
    EventHandler EnterBarHandler = new EventHandler<KeyEvent>() {
        public void handle(KeyEvent event) {
            if(event.getCode() == KeyCode.ENTER){
                handlePlayButtonClick();
            }
        }
    };

    public Map<Integer, Double> getPossessionTimes(Team team){

        Map<Integer, Double> possessionTimes = new HashMap<>();

        for(Player player : matchModel.getPlayers().values()){
            if(player.getTeam() == team){
                possessionTimes.put(player.getID(), player.getPossessionTime());
            }
        }

        return possessionTimes;
    }

    public ArrayList<Integer> getDistance(int id){

        return matchModel.getPlayerDistances().get(id);
    }
}
