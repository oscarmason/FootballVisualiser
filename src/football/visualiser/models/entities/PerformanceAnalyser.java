package football.visualiser.models.entities;

import java.util.ArrayList;

// TODO: Testing of the class, then optimising the code
// TODO: Consider a function to get the match average
// TODO: Prevent mistakes caused by confusing actual degradation with changing tactics (position tracking may be useful)

/**
 * The PerformanceAnalyser class takes the arrays of velocity and distance values for a player and
 * returns a categorised analysis of the player's minute-to-minute performance.
 *
 * Categories include (current heuristics in parentheses):
 * - Category 0: No change (< 10%)
 * - Category 1/-1: Minor change from previous minute (+/- 10%)
 * - Category 2/-2: Moderate change from previous minute (+/- 35%)
 * - Category 3/-3: Major change from previous minute (+/- 50%)
 * - Category 4/-4: Extreme change from previous minute (+/- 65%)
 *
 * In all cases, negative numbers indicate a negative change in performance (subsequent values are lower than current values)
 */
public class PerformanceAnalyser {

    private ArrayList<Short> velocityValues = new ArrayList<>();
    private ArrayList<Short> distanceValues = new ArrayList<>();

    // The assessment ArrayLists store the categories assigned to each value in the velocityValues and distanceValues arrays
    private ArrayList<Short> velocityAssessment = new ArrayList<>();
    private ArrayList<Short> distanceAssessment = new ArrayList<>();

    /**
     * The constructor takes references to two ArrayLists from the caller and uses them to initialise its internal lists
     *
     * @param vel ArrayList of velocity values for a given player
     * @param dist ArrayList of distance values for a given player
     */
    public PerformanceAnalyser(ArrayList<Short> vel, ArrayList<Short> dist) {
        velocityValues = vel;
        distanceValues = dist;
    }


    public ArrayList<Short> getVelocityAssessment() {
        return velocityAssessment;
    }

    public ArrayList<Short> getDistanceAssessment() {
        return distanceAssessment;
    }

    /**
     * Iterate over both ArrayLists and assign a category to each minute by comparing it to the next value
     * */
    public void analysePerformanceData() {

        short current = 0;
        int i;

        if(velocityValues.size() < 1 || distanceValues.size() < 1)
            return;

        // Iterate over velocity array
        // current = velocityValues.get(0);

        // Because the first element cannot be compared to a previous element, the change is 0
        velocityAssessment.add(0, (short) 0);
        distanceAssessment.add(0, (short) 0);

        for( i = 1; i < velocityValues.size(); i++ ) {
            // compare current element to next element and assign a category according to the heuristic
            current = velocityValues.get(i);
            velocityAssessment.add(i, compareValues(current, velocityValues.get(i-1)));
        }

        // Iterate over distance array
        for( i = 1; i < distanceValues.size(); i++ ) {
            // compare current element to next element and assign a category according to the heuristic
            current = distanceValues.get(i);
            distanceAssessment.add(i, compareValues(current, distanceValues.get(i-1)));
        }
    }


    /**
     * Compare two short values and assign a numerical category based on the difference
     *
     * @param current The value stored at the current index in the ArrayList
     * @param previous The value stored at the adjacent index in the ArrayList
     * @return A numerical assessment of the difference between the two values according to the previously defined heuristic
     */
    private short compareValues(short current, short previous) {

        if(previous == 0) return 4;

        // calculate the percentage difference between the current value and the next value in the ArrayList
        short difference = (short) ((current / previous) * 100);

        if (difference > 165) {
            return 4; // difference is greater than 10%, next value is greater than the current value - minor improvement
        } else if (difference > 150) {
            return 3; // difference is greater than 35% and greater - moderate improvement
        }
        else if (difference > 135) {
            return 2; // difference is greater than 50% and greater - major improvement
        }

        else if (difference > 110 && difference > 100) {
            return 1; // difference is greater than 65% and greater - extreme improvement
        }

        else if (difference < 45) {
            return -4; // difference is greater than 10%, next value is smaller than the current value - minor degradation
        }

        else if (difference < 60) {
            return -3; // difference is greater than 35% and smaller - moderate degradation
        }
        else if (difference < 75) {
            return -2; // difference is greater than 50% and smaller - major degradation
        }

        else if (difference < 90 && difference < 100) {
            return -1; // difference is greater than 65% and smaller - extreme degradation
        }

        // return 0 if value has not changed
        return 0;

    }

}
