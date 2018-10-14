package football.visualiser.models;

import football.visualiser.SystemData.Team;

/**
 * <h1>Goal</h1>
 * Used to store goal related information such as the bounds of the goal and the team who,
 * upon a ball entering the goal, will receive the score
 *
 * @author Oscar Mason
 */

public class Goal {
    private Team team;
    private int x;
    private int y1;
    private int y2;
    private int z;

    public Goal(int x, int y1, int y2, int z){
        this.x = x;
        this.y1 = y1;
        this.y2 = y2;
        this.z = z;
    }

    public int getX(){
        return x;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }

    public int getZ() {
        return z;
    }

    public void setTeam(Team team){
        this.team = team;
    }

    public Team getTeam(){
        return team;
    }
}
