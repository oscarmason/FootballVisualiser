package football.visualiser.models.entities;


/**
 * <h1>Football</h1>
 *
 * Keeps track of football related data during the match including:
 * <ul>
 *     <li>Position</li>
 *     <li>Velocity</li>
 *     <li>Whether it was recently kicked</li>
 *     <li>Whether it was recently stopped</li>
 *     <li>The footballs ID</li>
 * </ul>
 *
 * @author Oscar Mason
 */
public class Football extends Entity {
    private int previousBallVelocity;
    private int currentBallVelocity;
    private boolean ballRecentlyKicked;
    private boolean ballRecentlyStopped;
    private int z;
    private boolean checkForGoal = false;

    public Football(int id){
        super(id);
    }

    public void setZ(int z){
        this.z = z;
    }

    public void setCheckForGoal(boolean checkForGoal){
        this.checkForGoal = checkForGoal;
    }

    public boolean getCheckForGoal(){
        return checkForGoal;
    }

    public int getZ(){
        return z;
    }

    public void setPreviousBallVelocity(int previousBallVelocity){
        this.previousBallVelocity = previousBallVelocity;
    }

    public int getPreviousBallVelocity(){
        return previousBallVelocity;
    }

    public void setCurrentVelocity(int currentBallVelocity){
        this.currentBallVelocity = currentBallVelocity;
    }

    public int getCurrentBallVelocity(){
        return currentBallVelocity;
    }

    public void setBallRecentlyKicked(boolean ballRecentlyKicked){
        this.ballRecentlyKicked = ballRecentlyKicked;
    }

    public void setBallRecentlyStopped(boolean ballRecentlyStopped){
        this.ballRecentlyStopped = ballRecentlyStopped;
    }

    public boolean wasBallRecentlyKicked(){
        return ballRecentlyKicked;
    }

    public boolean wasBallRecentlyStopped(){
        return ballRecentlyStopped;
    }
}
