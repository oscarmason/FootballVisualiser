package football.visualiser.models;

import football.visualiser.interfaces.IMatchModelListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;
import java.lang.Thread;

import football.visualiser.SystemData;
import football.visualiser.interfaces.*;
import football.visualiser.models.entities.Football;
import football.visualiser.models.entities.Player;

import static football.visualiser.SystemData.*;
import static java.lang.Math.abs;

/**
 * <h1>Match Model</h1>
 * Responsible for playing the match back and iterating through the whole data file in order to analyse
 * the match data itself
 *
 * @author Simrandeep Kaur, Oscar Mason, Benjamin Petek
 *
 */
public class MatchModel {
    private HashMap<Integer, IPlayerSensor<Player>> playerSensors;
    private HashMap<Integer, IFootballSensor<Football>> footballSensors;
    private HashMap<Integer, Player> players;
    private HashMap<Integer, Football> footballs;
    private HashMap<Integer, ArrayList<Integer>> playerDistances;
    private final int SECOND = 1000;
    private final int TWO_SECONDS = 2000;

    private IMatchData matchData;
    private IEntityObserver matchController;
    private DataAnalyser dataAnalyser;
    private Pitch pitch;
    private IMatchModelListener matchModelListener;

    private double blueTeamPassAccuracy = 0.0;
    private double redTeamPassAccuracy = 0.0;

    private AtomicBoolean isPlaying = new AtomicBoolean(false);

    public MatchModel(IMatchData matchData, DataAnalyser dataAnalyser, Pitch pitch,
                      IMatchModelListener matchModelListener) {
        this.matchData = matchData;
        this.dataAnalyser = dataAnalyser;
        this.pitch = pitch;
        this.matchModelListener = matchModelListener;

        playerDistances = new HashMap<>();
    }

    public void setEntityHashMaps(HashMap<Integer, IPlayerSensor<Player>> playerSensors,
                                  HashMap<Integer, IFootballSensor<Football>> footballSensors,
                                  HashMap<Integer, Player> players, HashMap<Integer, Football> footballs){
        this.playerSensors = playerSensors;
        this.footballSensors = footballSensors;
        this.players = players;
        this.footballs = footballs;
    }

    /**
     * In order to keep the user interface responsive whilst the game is being played back,
     * the game loop is put in a background thread
     * Author: Oscar Mason
     */
    public void gameLoopStart() {
        // To prevent multiple threads, and in turn game loops, from running at the same time,
        // such as when seeking to a new position in the game, a new thread should only be started
        // when the match is playing (when seeking the match is paused)
        // The status of the whether the match is playing is store in an AtomicBoolean to ensure
        // only one thread can modify the boolean at a time

        if (getIsPlaying()) {
            Thread thread = new Thread(this::gameLoop);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Iterate through all players in the HashMap and assign a new PerformanceTracker to each object
     * The PerformanceTracker constructor takes the SECOND constant as the interval variable (i.e. 1000 units is a second)
     * @param initTime The initial time value of the match, i.e. the first timestamp in the data stream. Used to determine when a minute has passed
     */
    public void initialisePerformanceTrackers(int initTime){
        /* NOTE: The Performance Tracker returns the (hopefully) workable set of data
           Use getAvgSpeed() and getAvgDistance() on each player
           to retrieve a reference to the array list, which contains short integers (type is Short, may cause problems if any code expects an int)
        * */
        for(int player : players.keySet()) {
            players.get(player).addPerformanceTracker(SECOND, initTime);
        }
    }

    public HashMap<Integer, Player> getPlayers(){
        return players;
    }

    /**
     * Iterates through the whole match data file and carries out all analysing tasks including:
     *      Detecting goals
     *      Detecting tackles
     *      Analysing player performance
     *      Finding the closest player to the ball
     *      Checking which player is in possession of the ball
     *      Detecting 1-2-1 passes
     *
     * Authors: Oscar Mason, Simrandeep Kaur, Benjamin Petek
     *
     * @param dataAnalyser      Data analyser which handles the actual analytical tasks
     * @param pitch             Contains the information about the pitch relating to the data file being analysed
     */
    public void analyseMatchData(DataAnalyser dataAnalyser, Pitch pitch){
        matchData.goToFirstHalf();
        Football footballInPlay = footballs.get(footballs.keySet().iterator().next());
        IPlayerSensor<Player> playerSensorClosestToBall;
        int currentMillisecond;
        int prevMillisecond = 0;
        int twoSeconds = 0;
        int currentID;
        boolean performanceTrackersInitialised = false;
        int currentX;
        int currentY;
        int distanceX;
        int distanceY;
        int prevX;
        int prevY;
        int hypotenuse;

        matchData.setAnalyticalStrength(3);

        for(Player player : players.values()){
            playerDistances.put(player.getID(), new ArrayList<Integer>());
        }


        for(int[] data = matchData.getNextLineAsInt(); data != null; data = matchData.getNextLineAsInt()){

            currentID = data[dataID];
            currentMillisecond = data[dataTimeStamp];

            // If the performance trackers for all players are not created yet, do that now
            if (!performanceTrackersInitialised) {
                initialisePerformanceTrackers(currentMillisecond);
                performanceTrackersInitialised = true;
            }

            // Stores which football is currently in player
            footballInPlay = dataAnalyser.getFootBallInPlay(pitch, footballs, footballInPlay);

            playerSensorClosestToBall = dataAnalyser.playerClosestToFootball(playerSensors, footballInPlay);

            dataAnalyser.checkIfPlayerIsInPossession(playerSensorClosestToBall, footballInPlay,
                    data[dataTimeStamp]);

            dataAnalyser.detectTackle(currentMillisecond, playerSensorClosestToBall, playerSensors, matchData);

            if (players.containsKey(data[dataID])) {
                players.get(currentID).setX(data[dataXPosition]);
                players.get(currentID).setY(data[dataYPosition]);
            }

            if(playerSensors.containsKey(currentID)){
                playerSensors.get(currentID).setX(data[dataXPosition]);
                playerSensors.get(currentID).setY(data[dataYPosition]);
            }

            if(footballs.containsKey(currentID)){
                footballs.get(currentID).setX(data[dataXPosition]);
                footballs.get(currentID).setY(data[dataYPosition]);
                footballs.get(currentID).setZ(data[dataZPosition]);
                footballs.get(currentID).setCurrentVelocity(data[velocityPosition]);
            }

            // Performs these operations once every two seconds
            if(currentMillisecond - twoSeconds > TWO_SECONDS){
                dataAnalyser.interactionWithFootballOccurred(footballInPlay);
                twoSeconds = currentMillisecond;
                blueTeamPassAccuracy = dataAnalyser.detectSuccessfulPassBlueTeam();
                redTeamPassAccuracy = dataAnalyser.detectSuccessfulPassRedTeam();
            }

            // Performs these operations once every second
            if(currentMillisecond - prevMillisecond > SECOND){
                dataAnalyser.switchSides(currentMillisecond, matchData, pitch);

                prevMillisecond = currentMillisecond;
                dataAnalyser.detect1_2_1PassTeam(currentMillisecond, matchData, pitch.getLeftGoal(), pitch.getRightGoal());
                for(Player player : players.values()){

                    currentX = player.getX();
                    currentY = player.getY();
                    prevX = player.getPrevX();
                    prevY = player.getPrevY();
                    distanceX = abs(currentX - prevX);
                    distanceY = abs(currentY - prevY);

                    player.setPrevPosition(currentX, currentY);

                    hypotenuse = (int) Math.sqrt(distanceX * distanceX + distanceY * distanceY);

                    playerDistances.get(player.getID()).add(hypotenuse);
                }
            }

            dataAnalyser.updatePlayerHeatMaps(pitch, players);

            // Check whether the game has switched sides
            dataAnalyser.switchSides(currentMillisecond, matchData, pitch);

            dataAnalyser.detectGoal(currentMillisecond, matchData, pitch, footballInPlay, pitch.getLeftGoal(), true);
            dataAnalyser.detectGoal(currentMillisecond, matchData, pitch, footballInPlay, pitch.getRightGoal(), false);
        }

        // Go back to the start of the match so that the match can be played straight away
        matchData.goToFirstHalf();

        matchData.setAnalyticalStrength(1);

        for(Player player : players.values()){
            ArrayList<Integer> distance = new ArrayList<>();
            int counter = 0;
            int sum = 0;
            playerDistances.remove(0);
            for(Integer dis : playerDistances.get(player.getID())){
                sum+= dis;
                counter++;

                if(counter == 60){
                    distance.add(sum);
                    sum = 0;
                    counter = 0;
                }
            }
            playerDistances.put(player.getID(), distance);
        }
    }

    /**
     * Tracks the passage of time to control the rate gameplay in the match
     * updates the position of entities in a loop depending on how many entities
     * Authors: Simrandeep Kaur
     */
    public void gameLoop() {
        int data[];
        int currentMillisecond;

        int prevMillisecond = 0;
        int prevSecond = 0;
        long currentTime;
        long lastUpdate = 0;
        Football footballInPlay = footballs.get(footballs.keySet().iterator().next());
        IPlayerSensor<Player> playerSensorClosestToBall;
        int twoSeconds = 0;
        int frameRate = 20;

        for (data = matchData.getNextLineAsInt(); data != null; data = matchData.getNextLineAsInt()) {
            if(!getIsPlaying()){
                break;
            }

            int currentID = data[dataID];
            currentMillisecond = data[dataTimeStamp];


            footballInPlay = dataAnalyser.getFootBallInPlay(pitch, footballs, footballInPlay);

            playerSensorClosestToBall = dataAnalyser.playerClosestToFootball(playerSensors, footballInPlay);
            dataAnalyser.checkIfPlayerIsInPossession(playerSensorClosestToBall, footballInPlay, data[dataTimeStamp]);

            dataAnalyser.detectTackle(currentMillisecond, playerSensorClosestToBall, playerSensors, matchData);

            if (players.containsKey(data[dataID])) {
                players.get(currentID).setX(data[dataXPosition]);
                players.get(currentID).setY(data[dataYPosition]);
            }

            if(playerSensors.containsKey(currentID)){
                playerSensors.get(currentID).setX(data[dataXPosition]);
                playerSensors.get(currentID).setY(data[dataYPosition]);

            }

            if(footballs.containsKey(currentID)){
                footballs.get(currentID).setX(data[dataXPosition]);
                footballs.get(currentID).setY(data[dataYPosition]);
                footballs.get(currentID).setCurrentVelocity(data[velocityPosition]);
            }

            if(currentMillisecond - twoSeconds > TWO_SECONDS){

                dataAnalyser.interactionWithFootballOccurred(footballInPlay);
                twoSeconds = currentMillisecond;

                if (getIsPlaying()) {
                    matchModelListener.updateCursorPosition(currentMillisecond);
                }
                matchModelListener.updateGoalCount(currentMillisecond);
            }

            currentMillisecond = data[dataTimeStamp];

            if(currentMillisecond - prevMillisecond > frameRate || currentMillisecond - prevMillisecond < 0){
                currentTime = System.nanoTime();
                long diff = (long) Math.ceil((currentTime - lastUpdate) / 1000000);
                // Update the position of the player graphics
                for(Player player : players.values()){
                    matchController.updatePosition(player.getID(), player.getX(), player.getY());
                }
                // Update the position of the football graphic
                matchController.updatePosition(footballInPlay.getID(), footballInPlay.getX(), footballInPlay.getY());
                long sleepLength = frameRate - diff;
                lastUpdate = currentTime;
                if(sleepLength > 0){
                    try {
                        Thread.sleep(sleepLength);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                prevMillisecond = currentMillisecond;
            }
        }
    }

    public void setIsPlaying(boolean playing){
        isPlaying.set(playing);
    }

    public boolean getIsPlaying(){
        return isPlaying.get();
    }

    /**
     * Seeks to the new position in the match
     *
     * Author: Oscar Mason
     *
     * @param sliderValue           The current value (position) of the seekbar's slider
     * @param startEndTimeStamps    List of start and end times for the first and second half
     * @param matchTotalTime        Total time in milliseconds of the match
     */
    public void seek(Number sliderValue, int[] startEndTimeStamps, int matchTotalTime){
        int seekInMilliseconds = convertSeekValueToMilliseconds((double) sliderValue, matchTotalTime);
        int seekTo = convertToMatchTimeStamp(startEndTimeStamps, seekInMilliseconds);
        long timeStamp = (long) (seekTo * Math.pow(10, timeOffset));
        matchData.goToTimeStamp(String.valueOf(timeStamp));
        setIsPlaying(true);
        gameLoopStart();
    }

    /**
     * If by adding the requested seekInMilliseconds time to the first half start time, it does not go past the first
     * half end time, it will return that value, otherwise it will return the appropriate time in the second half
     *
     * Author: Oscar Mason
     *
     * @param startEndTimeStamps    List of start and end times for the first and second half
     * @param seekInMilliseconds    Place in the match in milliseconds to seek to
     * @return                      Seek time in milliseconds
     */
    public int convertToMatchTimeStamp(int[] startEndTimeStamps, int seekInMilliseconds){
        int seekTo = startEndTimeStamps[FIRST_HALF_START_TIME] + seekInMilliseconds;
        if(seekTo < startEndTimeStamps[FIRST_HALF_END_TIME]){
            return seekTo;
        }else{
            return startEndTimeStamps[SECOND_HALF_START_TIME] + (seekTo - startEndTimeStamps[FIRST_HALF_END_TIME]);
        }
    }

    /**
     * Converts an input seek value into milliseconds
     *
     * @param seekValue         Value of the seekbar's slider
     * @param matchTotalTime    Total time of the match
     * @return                  Time in the match in milliseconds corresponding to the value of the seekbar
     */
    public int convertSeekValueToMilliseconds(double seekValue, int matchTotalTime){
        return (int) (matchTotalTime * seekValue);
    }

    public void addMatchModelObserver(IEntityObserver matchController){
        this.matchController = matchController;
    }

    public HashMap<Integer, ArrayList<Integer>> getPlayerDistances(){
        return playerDistances;
    }
}
