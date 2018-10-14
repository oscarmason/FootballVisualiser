package football.visualiser;

public class SystemData {
    public enum Team { RED, BLUE }
    public enum EntityType {PLAYER, REFEREE, GOALIE, BALL}

    public static int dataID = 0;
    public static int dataTimeStamp = 1;
    public static int dataYPosition = 2;
    public static int dataXPosition = 3;
    public static int dataZPosition = 4;
    public static int velocityPosition = 5;

    public static int numberOfFields = 13;

    public static final int HEAT_MAP_WIDTH = 70;

    // Array positions of the data stored in the arrays used for passing the user input
    public static final int PITCH_START_X = 0;
    public static final int PITCH_END_X = 1;
    public static final int PITCH_START_Y = 2;
    public static final int PITCH_END_Y = 3;

    public static final int FIRST_HALF_START_TIME = 0;
    public static final int FIRST_HALF_END_TIME = 1;
    public static final int SECOND_HALF_START_TIME = 2;
    public static final int SECOND_HALF_END_TIME = 3;

    public static int timeOffset = 9;
}