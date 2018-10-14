package football.visualiser.models;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The performance tracker calculates the performance (average speed, distance covered, etc.) for each player and stores the data in Player objects
 * Each second, the main function calls the PerformanceTracker object with the sensor ID, timestamp and velocity data
 * The performance tracker is responsible for calculating the results
 *
 * @author Benjamin Petek
 */
public class PerformanceTracker {

    /*
    TODO: Tidy up code
    TODO: Figure out solution to currentArrayIndex and currentTime variables being separate
    TODO: Add exception handling as appropriate
    */

    // private member variables that are used for calculations
    private int timeInterval;
    private int timestamp;
    private short velocity;
    private int currentTimeV, currentTimeD;


    private int currentVelocityArrayIndex = 0; // use this counter to index into the avgVelocityArray and replace values when a new average is calculated
    private int currentDistanceArrayIndex = 0; // use this counter to index into the avgDistanceArray and replace values when a new average is calculated


    private ArrayList<Short> velocityArray = new ArrayList<Short>();
    private ArrayList<Short> distanceArray = new ArrayList<Short>();

    private HashMap<Integer, ArrayList<Short>> velocityMap = new HashMap<>();

    // ArrayLists of values used to store results of calculations
    private ArrayList<Short> avgVelocity = new ArrayList<Short>();
    private ArrayList<Short> avgDistance = new ArrayList<Short>();


    /**
     * Constructor function for the PerformanceTracker class
     */
    public PerformanceTracker(int interval, int initTime) throws IllegalArgumentException {


        if(interval <= 0 || initTime < 0) {
            throw new IllegalArgumentException();
        }

        timeInterval = interval;

        timestamp = initTime;
        velocity = 0;

        currentTimeV = initTime;
        currentTimeD = initTime;

    }

    // accessor functions

    /**
     * @return Reference to the ArrayList containing average velocity values
     * */
    public ArrayList<Short> getAverageVelocity() {
        return avgVelocity;
    }


    /**
     * @return Reference to the ArrayList containing average distance travelled values
     * */
    public ArrayList<Short> getAverageDistance() {
        return avgDistance;
    }

    // public-facing function that allows the main class to pass data to the performance tracker
    /**
     * Function that allows other classes in the system to update the data values stored in the
     * */
    public void updateData(int time, short vel) {

       timestamp = time;
       velocity = vel;
       calculateAverageVelocity();
       calculateAverageDistance();
    }

    // calculation functions

    /**
     * Calculates a per-minute average velocity for a given player
     * */

    private void calculateAverageVelocity() {

        /*
        Take data given by the main function and insert them into an ArrayList
        When one minute's worth of data has been collected, calculate the average and store it in the avgVelocity ArrayList
        * */

        int i;
        short sum = 0;
        short result = 0;

        // if the velocity associated with the time stamp is within the specified interval (i.e. 60 seconds), add the value to the temporary array
        // if it is outside the interval, clear the array and increment the index and currentTime variables
        if(timestamp - currentTimeV > timeInterval ) {

            // purge temporary array and increment the base time and index
            velocityArray.clear();
            currentVelocityArrayIndex++;
            currentTimeV += timeInterval;

        }

        velocityArray.add(velocity);

        for (i = 0; i < velocityArray.size(); i++) {
            sum += velocityArray.get(i);
        }


        // conditional statement to prevent division by 0 errors
        if(velocityArray.size() > 0) {

            result = (short) (sum / velocityArray.size());

            if(currentVelocityArrayIndex > avgVelocity.size()-1) {
                avgVelocity.add(currentVelocityArrayIndex, result);
            }
            else {
                avgVelocity.set(currentVelocityArrayIndex, result);
            }


        }
        else {
            // if the velocity array contains 0 elements, add 0 to average velocity array
            avgVelocity.add(currentVelocityArrayIndex, (short) 0);
        }
    }

    /**
     * Calculates the average distance travelled by a given player for each minute of the match
     * */

    private void calculateAverageDistance() {


        int distance;
        int i;

        int sum = 0;

        if(timestamp - currentTimeD > timeInterval ) {
            // purge temporary array, increment values
            distanceArray.clear();
            currentDistanceArrayIndex++;
            currentTimeD += timeInterval;
        }

        distance = velocity * 60;
        distance /= 100;
        short dist = (short) distance;
        distanceArray.add(dist);

        for(i = 0; i<distanceArray.size(); i++) {
            sum += distanceArray.get(i);
        }

        if (currentDistanceArrayIndex > avgDistance.size() - 1) {
            // if no value has yet been added for the current interval, add a new value
            avgDistance.add(currentDistanceArrayIndex, (short) (sum/distanceArray.size()));
        } else {
            // otherwise update the old value
            avgDistance.set(currentDistanceArrayIndex, (short) (sum/distanceArray.size()));
        }

    }
}
