package football.visualiser.models;

import football.visualiser.SystemData.Team;
import football.visualiser.interfaces.IFootballSensor;
import football.visualiser.interfaces.IMatchData;
import football.visualiser.interfaces.IPlayerSensor;
import football.visualiser.models.entities.*;
import football.visualiser.SystemData.EntityType;

import static football.visualiser.SystemData.*;

import java.util.*;


/**
 * <h1>Data Analyser</h1>
 * The data analyser class analyses the input match data in order to:
 * <ul>
 *     <li>Assign players to their teams automatically</li>
 *     <li>Calculate the distance between entities</li>
 *     <li>Check which player and team is in possession</li>
 *     <li>Create player, football, and goal keep entities</li>
 *     <li>Detect tackles</li>
 *     <li>Detect 1-2-1 passes</li>
 *     <li>Detect goals</li>
 *     <li>Update player heat maps</li>
 *     <li>Detect when a football is out of bounds</li>
 *     <li>Detect interaction with the football</li>
 *     <li>Switch sides at half time</li>
 * </ul>
 *
 *
 * @author Oscar Mason, Simrandeep Kaur
 */

public class DataAnalyser {
    private int dataX = 3;
    private int dataY = 2;
    private int numberOfPlayersOnTeam = 8;
    private int oneMetre = 1000;
    private int ballInPossessionStartTime;
    private int ballInPossessionEndTime;
    private int tackleStartTime;

    private double totalPassCountBlue = 0;
    private double succPassCountBlue =0;
    private double totalPassCountRed = 0;
    private double succPassCountRed =0;
    private double blueTeamShotAccuracy = 0;
    private double redTeamShotAccuracy = 0 ;

    private volatile static Player playerInPossession = null;
    private volatile static Player lastNonGoalieInPossession = null;
    private int[] footballIDs;
    private int[] referees = { 105, 106 };

    private boolean checkForGoal = true;
    private boolean tackleInProgress;
    private boolean tackleRecentlyRegistered;

    private List<Double> tackleTimes = new ArrayList<>();
    private List<Double> redTeamGoalTimes = new ArrayList<>();
    private List<Double> blueTeamGoalTimes = new ArrayList<>();
    private List<Double> blueTeam1_2_1Times = new ArrayList<>();
    private List<Double> redTeam1_2_1Times = new ArrayList<>();

    private Map<Integer, Team> goals = new TreeMap<>();
    private Map<Integer, Integer> individualRedGoals = new LinkedHashMap<>();
    private Map<Integer, Integer> individualBlueGoals = new LinkedHashMap<>();
    private Map<Integer, Integer> individualSuccPasses = new LinkedHashMap<>();
    private Map<Integer, Integer> individualRedTackles = new LinkedHashMap<>();
    private Map<Integer, Integer> individualBlueTackles = new LinkedHashMap<>();

    private Pair<Integer, Integer> goalCount = new Pair<>();

    //Required for successful pass and 1-2-1 pass functions
    private Player currentPlayerInPossession = null;
    private Player lastPlayerInPossession = currentPlayerInPossession;
    private Player secToLastPlayerInPossession = lastPlayerInPossession;
    private int secToLastPlayerXPosition;

    private final int PRE_HIGHLIGHT_MILLISECONDS = 5000;
    private final int PRE_121_MILLISECONDS = 10000;
    private final int POSSESSION_TRIGGER_DISTANCE = 1000;
    private final int POSSESSION_STILL_HELD_DISTANCE = 2000;
    private final int AVERAGE_ARRAY_COUNTER = 0;
    private final int AVERAGE_ARRAY_Y = 1;
    private final int AVERAGE_ARRAY_X = 2;

    private final double BALL_KICK_VELOCITY_THRESHOLD = 1.2;
    private final double BALL_STOPPED_VELOCITY_THRESHOLD = 0.4;

    private boolean sidesSwitched = false;

    public DataAnalyser(int[] footballIDs) {
        this.footballIDs = footballIDs;
    }

    /**
     * Calculates the medium positions of all entities at the start of the match.
     * These average positions are then used to decipher which team a player is on and whether
     * they are goal keepers or not
     *
     * Author: Oscar Mason
     *
     * @param matchData Match data which provides access to the file itself
     */
    public HashMap<Integer, int[]> calculateEntityAverageStartPosition(IMatchData matchData) {
        int[] tokens;
        int id;

        HashMap<Integer, int[]> startPosition = new HashMap<>();
        int[] entityPositions;

        // Use 4000 iterations of the data to calculate their start positions
        for (int i = 0; i < 4000; i++) {
            tokens = matchData.getNextLineAsInt();
            id = tokens[dataID];

            if (!startPosition.containsKey(id)) startPosition.put(id, new int[]{0, 0, 0});

            entityPositions = startPosition.get(id);

            entityPositions[AVERAGE_ARRAY_COUNTER]++;
            entityPositions[AVERAGE_ARRAY_X] = entityPositions[AVERAGE_ARRAY_X] + tokens[dataX];
            entityPositions[AVERAGE_ARRAY_Y] = entityPositions[AVERAGE_ARRAY_Y] + tokens[dataY];
        }

        // Divide totals by counter to get the average
        for (Integer key : startPosition.keySet()) {
            startPosition.get(key)[AVERAGE_ARRAY_X] = startPosition.get(key)[AVERAGE_ARRAY_X]
                    /= startPosition.get(key)[AVERAGE_ARRAY_COUNTER];
            startPosition.get(key)[AVERAGE_ARRAY_Y] = startPosition.get(key)[AVERAGE_ARRAY_Y]
                    /= startPosition.get(key)[AVERAGE_ARRAY_COUNTER];
        }

        matchData.goToFirstHalf();
        return startPosition;
    }

    /**
     * Cycles through the IDs in the list of start positions and uses them to decipher, partially from the users input,
     * whether the entity is a football or a player and calls the corresponding method for each
     *
     * Author: Oscar Mason
     *
     * @param startPositions    The starting positions of each entity
     * @param playerSensors     Sensors attached to players
     * @param footballSensors   Sensors attached to the footballs
     * @param players           Player entities
     * @param footballs         Football entities
     */
    public void createEntities(HashMap<Integer, int[]> startPositions,
                               HashMap<Integer, IPlayerSensor<Player>> playerSensors,
                               HashMap<Integer, IFootballSensor<Football>> footballSensors,
                               HashMap<Integer, Player> players, HashMap<Integer, Football> footballs) {
        EntityType entityType;
        int xPosition;
        int yPosition;

        for (Integer entityID : startPositions.keySet()) {
            entityType = getEntityType(entityID);
            xPosition = startPositions.get(entityID)[AVERAGE_ARRAY_X];
            yPosition = startPositions.get(entityID)[AVERAGE_ARRAY_Y];

            if (entityType == EntityType.PLAYER) {
                createPlayer(players, playerSensors, xPosition, yPosition, entityID);
            }
        }

        for (Integer footballID : footballIDs) {
            createFootball(footballs, footballSensors, -10000000, -10000000, footballID);
        }
    }

    /**
     * Analyses the distance between sensors to decipher sensors which are attached to the same player thus preventing
     * multiple player entities being created for a single player.
     *
     * Sensor belong to the same player if they are within one metre of each other at the start of the match
     *
     * Author: Oscar Mason
     *
     * @param players           Stores the player entities
     * @param playerSensors     List of player sensors of which
     * @param xPosition         Starting x position
     * @param yPosition         Starting y position
     * @param sensorID          ID of the sensor
     */

    private void createPlayer(HashMap<Integer, Player> players, HashMap<Integer, IPlayerSensor<Player>> playerSensors,
                              int xPosition, int yPosition, int sensorID) {
        boolean addPlayer = true;
        int samePlayerID = 0;

        for (Integer key : players.keySet()) {
            // If two sensors are within one metre of each other at the start, the sensors will be attached to
            // the same player
            if (Math.abs(xPosition - players.get(key).getX()) < oneMetre
                    && Math.abs(yPosition - players.get(key).getY()) < oneMetre) {
                addPlayer = false;
                samePlayerID = key;
                break;
            }
        }

        IPlayerSensor<Player> sensor;
        Player player;

        // If it has been calculated that there is a new player, create a new one and add it to the players hash map.
        // The player sensor in question should be attached to the player whether the player is new or not.
        // If the player is new, the current sensor should be set as the primary sensor for that player (true)
        if (addPlayer) {
            player = new Player(sensorID);
            sensor = new PlayerSensor<>(sensorID, player, true);
            players.put(sensorID, player);

        } else {
            player = players.get(samePlayerID);
            sensor = new PlayerSensor<>(sensorID, player, false);
        }

        player.addSensor(sensor);
        sensor.setX(xPosition);
        sensor.setY(yPosition);
        playerSensors.put(sensorID, sensor);
    }


    /**
     * Creates the footballs
     *
     * Author: Oscar Mason
     *
     * @param footballs       Hash map which stores the footballs
     * @param footballSensors Hash map which stores the football sensors
     * @param xPosition       Starting x position of the ball
     * @param yPosition       Starting y position of the ball
     * @param sensorID        ID of the ball
     */
    private void createFootball(HashMap<Integer, Football> footballs, HashMap<Integer,
            IFootballSensor<Football>> footballSensors, int xPosition, int yPosition, int sensorID) {

        Football football = new Football(sensorID);
        IFootballSensor<Football> sensor = new FootballSensor<>(sensorID, football, true);
        sensor.setX(xPosition);
        sensor.setY(yPosition);
        footballs.put(sensorID, football);
        footballSensors.put(sensorID, sensor);
    }

    /**
     * Analyse the X starting coordinates of the players to decipher which team each player is on such that the first
     * half of the total number of players on the left will make up the red team, and the other half will make up the
     * blue team
     *
     * Author: Oscar Mason
     *
     * @param pitchStartX       The x coordinate of where the football pitch starts.
     * @param players           Contains all the player entities
     * @param playerSensors     Contains all the player sensors
     */
    public void assignPlayersToTeams(int pitchStartX, HashMap<Integer, Player> players, HashMap<Integer,
            IPlayerSensor<Player>> playerSensors){
        int teamAllocationCounter = 0;
        Player player;
        int leftestPlayer;
        int key = 0;
        Player redGoalKeeper = null;
        Player blueGoalKeeper = null;

        for(int i = 0; i < numberOfPlayersOnTeam * 2; i++){
            leftestPlayer = pitchStartX;
            // Search for the player furthest to the left which has NOT already been assigned to a team (its team
            // value is set to null)
            for(Integer currentKey : players.keySet()){
                player = players.get(currentKey);
                if(player.getX() < leftestPlayer && player.getTeam() == null) {
                    leftestPlayer = player.getX();
                    key = currentKey;
                }
            }
            player = players.get(key);

            // If the player is the furthest player to the left or right, make them the goal keepers
            if(teamAllocationCounter == 0) redGoalKeeper = player;
            else if(teamAllocationCounter == numberOfPlayersOnTeam * 2 - 1) blueGoalKeeper = player;

            // Assign the players a team; the furthest players to the left will be set to red, the rest to blue
            if(player != null){
                player.setTeam(teamAllocationCounter < numberOfPlayersOnTeam ? Team.RED : Team.BLUE);
                teamAllocationCounter++;
            }
        }

        createGoalKeepers(redGoalKeeper, players, playerSensors);
        createGoalKeepers(blueGoalKeeper, players, playerSensors);
    }

    /**
     * Creates the goal keepers
     *
     * Author: Oscar Mason
     *
     * @param player        Player to base the goalkeeper on
     * @param players       Hash map which stores the goal keepers
     * @param playerSensors Hash map of sensors attached to players
     */
    private void createGoalKeepers(Player player, HashMap<Integer, Player> players,
                                   HashMap<Integer, IPlayerSensor<Player>> playerSensors) {
        Player goalKeeper = new GoalKeeper(player.getID());

        for (IPlayerSensor<Player> playerSensor : playerSensors.values()) {
            if (playerSensor.getOwner().getID() == player.getID()) {
                goalKeeper.addSensor(playerSensor);
                playerSensor.setOwner(goalKeeper);

                // Revalidate positions in order to update owner position
                playerSensor.setX(playerSensor.getX());
                playerSensor.setY(playerSensor.getY());
            }
        }

        goalKeeper.setTeam(player.getTeam());
        players.put(goalKeeper.getID(), goalKeeper);
    }



    /**
     * Looks at the distance between the football and players to calculate which player is in possession of the ball
     * if any.
     *
     * A player is classed as taking possession if they are within the possession distance threshold set at
     * one metre
     *
     * A player remains in possession if they are within the possession held threshold set at two metres
     *
     * The last player and second to last player in possession is also stored for the purpose of detecting 1-2-1 passes
     * in a separate function
     *
     * The function also updates the possession time for each player
     *
     * Author: Oscar Mason, Simrandeep Kaur
     *
     * @param sensorClosestToFootball   Player sensor which is the closest to the football as calculated by another
     *                                  function
     * @param football                  Football currently in player
     * @param currentTime               Current time in the match
     * @return                          If there is a player on possession, that player will be returned, otherwise it
     *                                  null will be returned
     */
    public Player checkIfPlayerIsInPossession(IPlayerSensor<Player> sensorClosestToFootball, Football football,
                                              int currentTime){


        // Find the closest player to the ball
        int distance = calculateDistance(sensorClosestToFootball.getX(), sensorClosestToFootball.getY(),
                football.getX(), football.getY());
        // Check whether the closest player was in close proximity of the ball and a kick occurred. If so, set them
        // as having possession of the ball
        if(sensorClosestToFootball.getOwner() != playerInPossession && distance <= POSSESSION_TRIGGER_DISTANCE){
            // Update the amount of time the previous player was in possession of the ball for
            if (currentPlayerInPossession != null && ballInPossessionEndTime != 0) {
                updatePlayerPossessionTime(currentPlayerInPossession, ballInPossessionEndTime - ballInPossessionStartTime);
            }
            ballInPossessionStartTime = currentTime;

            secToLastPlayerInPossession = lastPlayerInPossession;
            secToLastPlayerXPosition = sensorClosestToFootball.getX();
            lastPlayerInPossession = currentPlayerInPossession;

            setPlayerInPossession(sensorClosestToFootball.getOwner());
            currentPlayerInPossession = playerInPossession;
            ballInPossessionStartTime = currentTime;

            return getPlayerInPossession();

        }

        if(playerInPossession != null){

            // If the player who was in possession on the last check is still within close proximity of the ball
            // keep that player in possession
            try{
                distance = calculateDistance(playerInPossession.getX(), playerInPossession.getY(),
                        football.getX(), football.getY());

                if(distance <= POSSESSION_STILL_HELD_DISTANCE){
                    ballInPossessionEndTime = currentTime;
                    return getPlayerInPossession();
                }

                if(ballInPossessionEndTime != 0){
                    updatePlayerPossessionTime(currentPlayerInPossession, ballInPossessionEndTime - ballInPossessionStartTime);
                }

            }catch (NullPointerException e){
                // No player in possession
            }


            ballInPossessionStartTime = 0;
            ballInPossessionEndTime = 0;
        }

        // If there is no player in close proximity of the ball, set it to null
        setPlayerInPossession(null);
        return playerInPossession;
    }

    /**
     * Sets the current player in possession of the ball
     * Method is synchronized to prevent threading issues when a new thread is created on restarting the match from
     * a different point
     *
     * Author: Oscar Mason
     *
     * @param player    The player who is currently in possession
     */
    private synchronized void setPlayerInPossession(Player player){
        playerInPossession = player;
        if(!(playerInPossession instanceof GoalKeeper) && playerInPossession != null){
            lastNonGoalieInPossession = playerInPossession;
        }
    }

    private synchronized Player getPlayerInPossession(){
        return playerInPossession;
    }

    private void updatePlayerPossessionTime(Player player, int time){
        player.addToFootballPossessionTime(time);
    }

    /**
     * Checks which football is in play depending on which one is closest to the centre and whether the current
     * football in play has gone out of bounds
     *
     * Author: Oscar Mason
     *
     * @param pitch             The pitch used to get the field bounds
     * @param footballs         List of footballs to check
     * @param currentFootball   Football currently in play
     * @return                  Football now in play
     */
    public Football getFootBallInPlay(Pitch pitch, HashMap<Integer, Football> footballs, Football currentFootball){
        int centreY = pitch.getY1() + pitch.getHeight() / 2;
        int closestDistanceToCentre = Math.abs(centreY - currentFootball.getY());
        int footballDistanceToCentre;
        for(Football football : footballs.values()){
            footballDistanceToCentre = Math.abs(centreY - football.getY());
            if(footballDistanceToCentre < closestDistanceToCentre &&
                    !outOfBounds(pitch, football.getX(), football.getY()) &&
                    outOfBounds(pitch, currentFootball.getX(), currentFootball.getY())){
                currentFootball = football;
                closestDistanceToCentre = footballDistanceToCentre;
            }
        }
        return currentFootball;
    }

    /**
     * Detects whether a goal has been scored
     *
     * Author: Oscar Mason
     *
     * @param currentTimeMilliseconds   Current time in the match
     * @param matchData                 Match data which provides methods to read the file
     * @param pitch                     Football pitch
     * @param football                  Current football in player
     * @param goal                      The goal object containing the boundaries of the goal
     * @param lessThanOrEqual           Whether the function should check for goals on the left or right side of the
     *                                  pitch
     * @return                          The team who scored the goal
     */
    public Team detectGoal(int currentTimeMilliseconds, IMatchData matchData, Pitch pitch, Football football,
                           Goal goal, boolean lessThanOrEqual){
        Team goalsTeam = null;
        boolean outOfBoundsX;

        if(!outOfBounds(pitch, football.getX(), football.getY()) && playerInPossession != null){
            checkForGoal = true;
        }

        // Check if the football has gone outside the pitch on the left hand side
        if(lessThanOrEqual) outOfBoundsX = football.getX() <= goal.getX();
        // Check if the football has gone outside the pitch on the right hand side
        else outOfBoundsX = football.getX() >= goal.getX();

        // If a goal has just occurred we don't want to check for another straight away so check for goal will be
        // set to false
        // Checks whether the ball has gone out of bounds on the X coordinate, and whether it has gone outside the
        // boundaries of the goal itself
        if(checkForGoal && outOfBoundsX && football.getY() >= goal.getY1() && football.getY() <= goal.getY2()
                && football.getZ() <= goal.getZ()){
            goalsTeam = goal.getTeam();
            if(goalsTeam == Team.RED) {
                // Stores the time at which the goal occurred
                redTeamGoalTimes.add(convertMatchTimeToSeekbarRatio(matchData,
                        currentTimeMilliseconds - PRE_HIGHLIGHT_MILLISECONDS));
                goals.put(currentTimeMilliseconds, goalsTeam);

                // Stores who scored the goal
                if(!individualRedGoals.containsKey(lastNonGoalieInPossession.getID())) {
                    individualRedGoals.put(lastNonGoalieInPossession.getID(), 1);
                }
                else {
                    individualRedGoals.put(lastNonGoalieInPossession.getID(),
                            individualRedGoals.get(lastNonGoalieInPossession.getID()) + 1);
                }
            }
            else {
                // Stores the time at which the goal occurred
                blueTeamGoalTimes.add(convertMatchTimeToSeekbarRatio(matchData,
                        currentTimeMilliseconds - PRE_HIGHLIGHT_MILLISECONDS));
                goals.put(currentTimeMilliseconds, goalsTeam);

                // Stores who scored the goal
                if(!individualBlueGoals.containsKey(lastNonGoalieInPossession.getID())) {
                    individualBlueGoals.put(lastNonGoalieInPossession.getID(), 1);
                }
                else {
                    individualBlueGoals.put(lastNonGoalieInPossession.getID(),
                            individualBlueGoals.get(lastNonGoalieInPossession.getID()) + 1);
                }
            }
            checkForGoal = false;
        }

        return goalsTeam;
    }

    public Map<Integer, Integer> getIndividualRedGoals(){
        return individualRedGoals;
    }

    public Map<Integer, Integer> getIndividualBlueGoals(){
        return individualBlueGoals;
    }

    /** Switches the sides of the pitch at half time
     *
     * Author: Oscar Mason
     *
     * @param currentMilliseconds   The current time of the match
     * @param matchData             Match data class providing access ot the data file
     * @param pitch                 Football pitch
     */
    public void switchSides(int currentMilliseconds, IMatchData matchData, Pitch pitch){
        if(!sidesSwitched && currentMilliseconds > matchData.getStartEndTimeStamps()[1]){
            pitch.switchSides();
            sidesSwitched = true;
        }
    }

    /**
     * Provides the number of goals scored for each team up until the current millisecond specified
     *
     * Author: Oscar Mason
     *
     * @param currentMillisecond    The number of goals scored will only include those before the
     *                              millisecond passed in
     * @return                      Number of goals for each team as a pair, where the number of red
     *                              goals is the first value and the number of blue goals is the second
     */
    public Pair<Integer, Integer> getGoalCount(int currentMillisecond){
        goalCount.setFirstValue(0);
        goalCount.setSecondValue(0);
        for(Integer key : goals.keySet()){
            if(key > currentMillisecond) return goalCount;

            if(goals.get(key) == Team.RED) goalCount.setFirstValue(goalCount.getFirstValue() + 1);
            else goalCount.setSecondValue(goalCount.getSecondValue() + 1);
        }
        return goalCount;
    }

    /**
     * Compares the current velocity of the ball in play to its previous velocity in order to
     * decipher whether some interaction occurred with the ball such as a kick
     *
     * Author: Oscar Mason
     *
     * @param football  Current football in play
     */
    public void interactionWithFootballOccurred(Football football){
        boolean ballKicked = false;
        boolean ballStopped = false;
        if(football.getCurrentBallVelocity() == 0) football.setCurrentVelocity(1);

        // Calculate the difference between the previous velocity of the ball and the
        // current velocity, and if it is greater or small than the kicked or stopped
        // threshold, then the ball must have interacted wit something
        double ratio = ((double) football.getCurrentBallVelocity()
                / (double) football.getPreviousBallVelocity());
        if(ratio >= BALL_KICK_VELOCITY_THRESHOLD){
            ballKicked = true;
        }
        if(ratio < BALL_STOPPED_VELOCITY_THRESHOLD){
            ballStopped = true;
        }
        football.setBallRecentlyKicked(ballKicked);
        football.setBallRecentlyStopped(ballStopped);
        football.setPreviousBallVelocity(football.getCurrentBallVelocity());
    }

    /**
     * Returns the player who is closest to the football
     *
     * Author: Oscar Mason
     *
     * @param playerSensors Sensors whose distance should be checked
     * @param football      Football currently in use
     * @return              Player closest to the football
     */
    public IPlayerSensor<Player> playerClosestToFootball (HashMap<Integer, IPlayerSensor<Player>> playerSensors,
                                                          Football football){
        int shortestDistance = Integer.MAX_VALUE;
        int footballX = football.getX();
        int footballY = football.getY();
        int playerDistance;
        IPlayerSensor<Player> closestSensor = playerSensors.values().iterator().next();
        for(IPlayerSensor<Player> playerSensor : playerSensors.values()){
            playerDistance = calculateDistance(playerSensor.getX(), playerSensor.getY(), footballX, footballY);
            if(playerDistance < shortestDistance) {
                shortestDistance = playerDistance;
                closestSensor = playerSensor;
            }
        }
        return closestSensor;
    }

    /**
     * Calculates the Euclidean distance between two entities
     *
     * Author: Oscar Mason
     *
     * @param x1    X Coordinate of the first entity
     * @param y1    Y Coordinate of the first entity
     * @param x2    X Coordinate of the second entity
     * @param y2    Y Coordinate of the second entity
     * @return      Distance between the first and second entities in centimetres
     */
    private int calculateDistance(int x1, int y1, int x2, int y2){
        double xDistanceSquare = Math.pow(Math.max(x1, x2) - Math.min(x1, x2), 2);
        double yDistanceSquare = Math.pow(Math.max(y1, y2) - Math.min(y1, y2), 2);
        double diagonal = Math.sqrt(xDistanceSquare + yDistanceSquare);
        return (int) diagonal;
    }

    /**
     * Detects when a player on the opposing team of the player who is currently closest to the ball, comes
     * within a certain distance of that player
     *
     * Author: Oscar Mason
     *
     * @param currentTime           Current time in the match in milliseconds
     * @param closestPlayerToBall   Player currently closest to the ball
     * @param playerSensors         Player sensors whose distance will be checked
     * @param matchData             Provides methods to read the data file
     */
    public void detectTackle(int currentTime, IPlayerSensor<Player> closestPlayerToBall,
                             HashMap<Integer, IPlayerSensor<Player>> playerSensors, IMatchData matchData){
        int playerID = closestPlayerToBall.getOwner().getID();
        Team teamOfClosestPlayerToBall = closestPlayerToBall.getOwner().getTeam();
        int closestPlayerToBallX = closestPlayerToBall.getX();
        int closestPlayerToBallY = closestPlayerToBall.getY();
        int tackleDistance = 1000;
        // Length of time two players are in close proximity with the ball
        int tackleDurationThreshold = 1000;
        for(IPlayerSensor<Player> playerSensor : playerSensors.values()){
            if(teamOfClosestPlayerToBall != playerSensor.getOwner().getTeam()
                    && calculateDistance(closestPlayerToBallX, closestPlayerToBallY,
                    playerSensor.getX(), playerSensor.getY()) < tackleDistance){
                // If a tackle was not already in progress, save the start time of the tackle
                if(!tackleInProgress){
                    tackleStartTime = currentTime;
                    tackleInProgress = true;
                }
                // If there wasn't a tackle recently registered and the duration of the tackle is greater than the
                // threshold, then store the time at which it occurred
                if(!tackleRecentlyRegistered && currentTime - tackleStartTime >= tackleDurationThreshold){
                    tackleRecentlyRegistered = true;

                    if(teamOfClosestPlayerToBall == Team.RED){
                        if(individualRedTackles.containsKey(playerID)){
                            individualRedTackles.put(playerID,
                                    individualRedTackles.get(playerID) + 1);
                        }else{
                            individualRedTackles.put(playerID, 1);
                        }
                    }else{
                        if(individualBlueTackles.containsKey(playerID)){
                            individualBlueTackles.put(playerID,
                                    individualBlueTackles.get(playerID) + 1);
                        }else{
                            individualBlueTackles.put(playerID, 1);
                        }
                    }
                    tackleTimes.add(convertMatchTimeToSeekbarRatio(matchData,
                            currentTime - PRE_HIGHLIGHT_MILLISECONDS));
                }
                return;
            }
        }
        tackleInProgress = false;
        tackleRecentlyRegistered = false;
    }

    /**
     * Takes the current time of the match and calculates the matches progress as a ratio of the whole match
     *
     * Author: Oscar Mason
     *
     * @param matchData     Provides methods relating to the match data file required for getting the
     *                      start and end times of the first and second half
     * @param matchTime     Current time of the match in milliseconds
     * @return              Ratio of match progress with a value between 0.0 and 1.0
     */
    public double convertMatchTimeToSeekbarRatio(IMatchData matchData, int matchTime){
        if(matchTime < matchData.getStartEndTimeStamps()[1]){
            matchTime = matchTime - matchData.getStartEndTimeStamps()[0];
        }else{
            matchTime = matchTime - matchData.getStartEndTimeStamps()[2] + matchData.getStartEndTimeStamps()[1]
                    - matchData.getStartEndTimeStamps()[0];
        }
        return (double) matchTime / (double) matchData.getTotalMatchTimeInMilliseconds();
    }

    /**
     * Returns the entity type based on their ID
     *
     * Author: Oscar Mason
     *
     * @param id    ID of the entity
     * @return      The corresponding entity type
     */
    private EntityType getEntityType(int id){
        for(int footballID : footballIDs){
            if(id == footballID) return EntityType.BALL;
        }

        for(int refID : referees){
            if (id == refID) return EntityType.REFEREE;
        }

        return EntityType.PLAYER;
    }

    /**
     * Check whether an entity has gone out of bounds of the pitch
     *
     * Author: Oscar Mason
     *
     * @param pitch     PitchGraphic whose bounds are used
     * @param x         Current x position of entity
     * @param y         Current y position of entity
     * @return          Returns true if the entity has gone out of bounds
     */
    public boolean outOfBounds(Pitch pitch, int x, int y) {
        return (x < pitch.getX1() || x > pitch.getX2() || y < pitch.getY1() || y > pitch.getY2());
    }


    /**
     * return percentage e.g. 95.3%  passing accuracy for blue team(team that starts with the ball)
     * When ball gets kicked, how often does a player from opponent team end up getting it.
     *
     * Author: Simrandeep Kaur
     *
     * @return blueTeamShotAccuracy          percentage of passing accuracy
     */
    public double detectSuccessfulPassBlueTeam() {
        //detects successful passes for the team that starts with the ball

        if (currentPlayerInPossession != null && lastPlayerInPossession != null) {

            if(currentPlayerInPossession.getTeam() == Team.BLUE){

                if(lastPlayerInPossession != currentPlayerInPossession){
                    //pass occurred but rn it hasn't determined whether its a successful pass or not

                    totalPassCountBlue++;
                    if(currentPlayerInPossession.getTeam() == lastPlayerInPossession.getTeam()){
                        //successful pass
                        succPassCountBlue++;

                        if(!individualSuccPasses.containsKey(lastPlayerInPossession.getID())) {
                            individualSuccPasses.put(lastPlayerInPossession.getID(), 1);
                        }
                        else {
                            individualSuccPasses.put(lastPlayerInPossession.getID(),
                                    individualSuccPasses.get(lastPlayerInPossession.getID()) + 1);
                        }

                    }
                    blueTeamShotAccuracy = succPassCountBlue / totalPassCountBlue * 100;
                }

            }
        }
        return blueTeamShotAccuracy;
    }

    public double getTotalPassCountBlue(){
        System.out.println(succPassCountBlue + " " + totalPassCountBlue);
        System.out.println(succPassCountBlue / totalPassCountBlue * 100);

        System.out.println(succPassCountRed + " " + totalPassCountRed);
        System.out.println(succPassCountRed / totalPassCountRed * 100);
        return succPassCountBlue / totalPassCountBlue * 100;
    }


    /**
     * return percentage e.g. 95.3%  passing accuracy for red team(team that doesn't start with the ball)
     * When ball gets kicked, how often does a player from opponent team end up getting it.
     *
     * Author: Simrandeep Kaur
     *
     * @return redTeamShotAccuracy          percentage of passing accuracy
     */
    public double detectSuccessfulPassRedTeam() {
        //detects successful passes for the team that starts with the ball
        //double redTeamPercent = 0.0;

        if (currentPlayerInPossession != null && lastPlayerInPossession != null) {

            if (currentPlayerInPossession.getTeam() == Team.RED) {
                if (lastPlayerInPossession != currentPlayerInPossession) {
                    //Pass occurred
                    totalPassCountRed++;

                    if (currentPlayerInPossession.getTeam() == lastPlayerInPossession.getTeam()) {
                        //successful pass occurred
                        succPassCountRed++;

                        if(!individualSuccPasses.containsKey(lastPlayerInPossession.getID())) {
                            individualSuccPasses.put(lastPlayerInPossession.getID(), 1);
                        }
                        else {
                            individualSuccPasses.put(lastPlayerInPossession.getID(),
                                    individualSuccPasses.get(lastPlayerInPossession.getID()) + 1);
                        }

                    }
                    redTeamShotAccuracy = succPassCountRed / totalPassCountRed * 100;
                    //System.out.println("Percentage of successful passes for team : RED " + redTeamShotAccuracy);
                }
            }
        }
        return redTeamShotAccuracy;
    }

    public Map<Integer, Integer> getIndividualSuccPasses(){
        return individualSuccPasses;
    }

    /**
     * Identify whether a 1-2-1 pass has occurred within two players of the same team. 1-2-1 passes : player 1
     * passes to player 2, subsequently player 2 passes back to player 1.
     *
     * Author: Simrandeep Kaur
     *
     * Function is called after a normal pass occurs
     *
     * @param leftGoal      stores which team is currently at the left goal
     * @param rightGoal     stores which team is currently at the right goal
     */
    public Team detect1_2_1PassTeam (int currentTimeMilliseconds, IMatchData matchData, Goal leftGoal, Goal rightGoal) {
        if (currentPlayerInPossession != null && secToLastPlayerInPossession != null) {

            if(leftGoal.getTeam() == Team.BLUE && rightGoal.getTeam() == Team.RED){
                //first half of match
                if(currentPlayerInPossession.getTeam() == Team.RED && lastPlayerInPossession.getTeam() == Team.RED
                        && secToLastPlayerInPossession.getTeam() == Team.RED) {
                    if (secToLastPlayerInPossession.getID() == currentPlayerInPossession.getID()) {
                        if(currentPlayerInPossession.getX() > secToLastPlayerXPosition + 6000){
                            redTeam1_2_1Times.add(convertMatchTimeToSeekbarRatio(matchData,
                                    currentTimeMilliseconds - PRE_121_MILLISECONDS));
                            return Team.RED;
                        }
                    }
                }else if(currentPlayerInPossession.getTeam() == Team.BLUE && lastPlayerInPossession.getTeam() ==
                        Team.BLUE && secToLastPlayerInPossession.getTeam() == Team.BLUE) {
                    if (secToLastPlayerInPossession.getID() == currentPlayerInPossession.getID()) {

                        if(currentPlayerInPossession.getX() < secToLastPlayerXPosition - 6000){
                            blueTeam1_2_1Times.add(convertMatchTimeToSeekbarRatio(matchData,
                                    currentTimeMilliseconds - PRE_121_MILLISECONDS));

                            return Team.BLUE;
                        }
                    }
                }

            }else if(leftGoal.getTeam() == Team.RED && rightGoal.getTeam() == Team.BLUE){
                //second half
                if(currentPlayerInPossession.getTeam() == Team.BLUE && lastPlayerInPossession.getTeam() == Team.BLUE
                        && secToLastPlayerInPossession.getTeam() == Team.BLUE) {

                    if (secToLastPlayerInPossession.getID() == currentPlayerInPossession.getID()) {
                        if (currentPlayerInPossession.getX() > secToLastPlayerXPosition + 6000) {
                            blueTeam1_2_1Times.add(convertMatchTimeToSeekbarRatio(matchData,
                                    currentTimeMilliseconds - PRE_121_MILLISECONDS));

                            return Team.BLUE;
                        }

                    }
                }else if (currentPlayerInPossession.getTeam() == Team.RED && lastPlayerInPossession.getTeam()
                        == Team.RED && secToLastPlayerInPossession.getTeam() == Team.RED) {

                    if (secToLastPlayerInPossession.getID() == currentPlayerInPossession.getID()) {
                        if (currentPlayerInPossession.getX() < secToLastPlayerXPosition - 6000) {
                            redTeam1_2_1Times.add(convertMatchTimeToSeekbarRatio(matchData,
                                    currentTimeMilliseconds - PRE_121_MILLISECONDS));

                            return Team.RED;

                        }

                    }

                }

            }
        }
        return null;
    }

    /**
     * Updates the heat map data for each player using their position on the pitch
     *
     * Author: Oscar Mason
     *
     * @param pitch     Pitch to check whether the player is out of bounds
     * @param players   Players whose heat maps are to be updated
     */

    public void updatePlayerHeatMaps(Pitch pitch, HashMap<Integer, Player> players){
        for(Player player : players.values()){
            if(!outOfBounds(pitch, player.getX(), player.getY())){
                player.getHeatMap().incrementHeatMap(player.getX(), player.getY(), sidesSwitched);
            }
        }
    }

    public Map<Integer, Integer> getIndividualRedTackles(){
        return individualRedTackles;
    }

    public Map<Integer, Integer> getIndividualBlueTackles(){
        return individualBlueTackles;
    }

    public List<Double> getTackleTimes(){
        return tackleTimes;
    }

    public List<Double> getRedTeamGoalTimes(){
        return redTeamGoalTimes;
    }

    public List<Double> getBlueTeamGoalTimes(){
        return blueTeamGoalTimes;
    }

    public List<Double> getBlueTeam1_2_1Times(){ return blueTeam1_2_1Times; }

    public List<Double> getRedTeam1_2_1Times(){ return redTeam1_2_1Times; }
}