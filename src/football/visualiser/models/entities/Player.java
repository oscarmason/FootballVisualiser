package football.visualiser.models.entities;

import football.visualiser.SystemData.Team;
import football.visualiser.interfaces.IPlayerSensor;
import football.visualiser.models.HeatMap;
import football.visualiser.models.PerformanceTracker;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * <h1>Player</h1>
 * Stores data related to a player including its id and heat map, as well as methods provided by the base class
 * {@link Entity}
 *
 * @author Oscar Mason, Benjamin Petek
 */

public class Player extends Entity {

    private Team team;
    private HashMap<Integer, IPlayerSensor<Player>> sensors = new HashMap<>();
    private ArrayList<Short> avgSpeed;
    private ArrayList<Short> avgDistance;
    private HeatMap heatMap;

    private double possessionTime;
    private int x;
    private int y;
    private double successfulPasses;
    private int prevX;
    private int prevY;

    // PerformanceTracker and PerformanceAnalyser object for each player
    private PerformanceTracker pt;
    private PerformanceAnalyser pa;

    public Player(int id){
        super(id);
    }

    public void addHeatMap(HeatMap heatMap){
        this.heatMap = heatMap;
    }

    public HeatMap getHeatMap(){
        return heatMap;
    }

    public void setTeam(Team team){
        this.team = team;
    }

    public Team getTeam(){
        return team;
    }

    public void setSuccessfulPasses(double successfulPasses){
        this.successfulPasses = successfulPasses;
    }

    public double getSuccessfulPasses(){
        return successfulPasses;
    }

    public void addSensor(IPlayerSensor<Player> playerSensor){
        sensors.put(playerSensor.getID(), playerSensor);
    }

    public void addToFootballPossessionTime(int time){
        possessionTime += time;
    }

    public double getPossessionTime(){
        return possessionTime;
    }

    public void addPerformanceTracker(int interval, int initTime) {
        pt = new PerformanceTracker(interval, initTime);
    }

    public void addPerformanceAnalyser() {
        for(short s : avgDistance){
            System.out.println(s);
        }
        pa = new PerformanceAnalyser(avgSpeed, avgDistance);
        pa.analysePerformanceData();
    }

    public void updatePerformanceTracker(int time, short vel) {
        pt.updateData(time, vel);
        avgSpeed = pt.getAverageVelocity();
        avgDistance = pt.getAverageDistance();
    }

    /**
     * This method is overridden as we want to compute the average position of the player based on all the sensors
     * attached to them for the purpose of the graphic displayed to the user
     * @return  Average position of all sensor's X positions
     */
    @Override
    public int getX(){

        x = 0;
        for(IPlayerSensor<Player> playerSensor : sensors.values()){
            x += playerSensor.getX();
        }

        x = x / sensors.size();

        return x;
    }

    /**
     * This method is overridden as we want to compute the average position of the player based on all the sensors
     * attached to them for the purpose of the graphic displayed to the user
     * @return  Average position of all sensor's Y positions
     */
    @Override
    public int getY(){
        y = 0;
        for(IPlayerSensor<Player> playerSensor : sensors.values()){
            y += playerSensor.getY();
        }

        y = y / sensors.size();

        return y;
    }

    public ArrayList<Short> getAvgSpeed() {
        return avgSpeed;
    }

    public ArrayList<Short> getAvgDistance() {
        return avgDistance;
    }

    public PerformanceTracker getPerformanceTracker() {
        return pt;
    }

    public PerformanceAnalyser getPerformanceAnalyser() {
        return pa;
    }

    public ArrayList<Short> getPlayerVelocityAssessment() {
        return pa.getVelocityAssessment();
    }

    public ArrayList<Short> getPlayerDistanceAssessment() {
        return pa.getDistanceAssessment();
    }

    public void setPrevPosition(int x, int y){
        this.prevX = x;
        this.prevY = y;
    }

    public int getPrevX(){
        return prevX;
    }

    public int getPrevY(){
        return prevY;
    }
}
