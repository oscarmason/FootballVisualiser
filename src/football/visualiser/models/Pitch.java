package football.visualiser.models;

import football.visualiser.SystemData;

/**
 * Created by dovydas on 06/02/2017.
 */

public class Pitch {

    // The (x1, y1) (x2, y2) coordinates for the pitch
    private int x1, x2, y1, y2;

    private int width;
    private int height;
    private Goal leftGoal;
    private Goal rightGoal;

    /** Constructor for the PitchGraphic class.
     * Gets coordinates of the pitch and calculates the length and height.
    /** Constructor for the PitchGraphic class.
     * Gets coordinates of the pitch and calculates the width and height.
     *
     * @author Irene, Dovydas
     */
    public Pitch(int x1, int x2, int y1, int y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        width = Math.abs(x1) + Math.abs(x2);
        height = Math.abs(y1) + Math.abs(y2);

        leftGoal = new Goal(x1, 22560, 29880, 2440);
        leftGoal.setTeam(SystemData.Team.BLUE);
        rightGoal = new Goal(x2, 22578, 29898, 2440);
        rightGoal.setTeam(SystemData.Team.RED);
    }

    public Goal getLeftGoal(){
        return leftGoal;
    }

    public Goal getRightGoal(){
        return rightGoal;
    }

    public void switchSides(){
        leftGoal.setTeam(SystemData.Team.RED);
        rightGoal.setTeam(SystemData.Team.BLUE);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getRatio() {return (double) height / (double) width; }

    public int getX1() {
        return x1;
    }

    public int getX2() {
        return x2;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }
}
