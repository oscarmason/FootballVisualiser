package football.visualiser.models;

import org.junit.Test;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Created by BPetek on 31/01/2017.
 */
public class PerformanceTrackerTest {

    private PerformanceTracker pt;

    private int playerID;
    private String filepath = "/testData.txt";

    /**
     * Tests whether the program can import the data set successfully
     * */
    @Test
    public void initialise() {

        pt = new PerformanceTracker(60, 0);

    }


    @Test
    public void initThrowsException() {

        boolean exceptionThrown = false;

        try {
            pt = new PerformanceTracker(-20, 0);
        }
        catch(IllegalArgumentException e) {
            System.out.println("Initialisation method has thrown an exception (as expected)");
            exceptionThrown = true;
        }

        assert(exceptionThrown);

    }


    /**
     * Tests whether the program can correctly calculate the average speed up to a certain margin of error
     */
    @Test
    public void calculateAverageSpeed(){

        PerformanceTracker pt = new PerformanceTracker(60, 0);

        pt.updateData(1, (short) 11);
        pt.updateData(4, (short) 15);
        pt.updateData(21, (short) 19);
        pt.updateData(55, (short) 15);
        // average of above values is 15
        pt.updateData(64, (short) 14);
        pt.updateData(68, (short) 18);
        // average is 16
        pt.updateData(123, (short) 8);
        pt.updateData(131, (short) 12);
        // average is 10

        long testValue1 = pt.getAverageVelocity().get(0);
        long testValue2 = pt.getAverageVelocity().get(1);
        long testValue3 = pt.getAverageVelocity().get(2);

        assertEquals(15, testValue1);
        assertEquals(16, testValue2);
        assertEquals(10, testValue3);

    }


    /**
     * Tests whether the program can correctly calculate the average distance travelled by an entity
     */
    @Test
    public void calculateAverageDistance() {
        PerformanceTracker pt = new PerformanceTracker(60, 0);

        pt.updateData(1, (short) 11);
        pt.updateData(4, (short) 15);
        pt.updateData(21, (short) 19);
        pt.updateData(55, (short) 15);
        // sum of above values for one minute is 900 ((11*60 + 15*60 + 19*60 + 15 *60)/4)

        pt.updateData(64, (short) 14);
        pt.updateData(68, (short) 18);
        // sum is 960

        pt.updateData(123, (short) 8);
        pt.updateData(131, (short) 12);
        // sum is 600

        long testValue1 = pt.getAverageDistance().get(0);
        long testValue2 = pt.getAverageDistance().get(1);
        long testValue3 = pt.getAverageDistance().get(2);

        assertEquals(900, testValue1);
        assertEquals(960, testValue2);
        assertEquals(600, testValue3);
    }


    /**
     * Tests whether the program can detect and account for anomalies in sensor data with regards to speed
     */
    @Test
    public void averageSpeedAnomalyDetection() {

        PerformanceTracker pt = new PerformanceTracker(60, 0);

        pt.updateData(1, (short) 11);
        pt.updateData(4, (short) 15);
        pt.updateData(21, (short) 19);
        pt.updateData(55, (short) 15);
        // average of above values is 15
        pt.updateData(64, (short) 14);
        pt.updateData(68, (short) 172);
        // average is 200, an increase of more than 80% compared to the previous value (15)
        pt.updateData(123, (short) 8);
        pt.updateData(131, (short) 12);
        // average is 10
        pt.updateData(185, (short) 9);
        pt.updateData(192, (short) -15);
        // average is -3, a decrease of more than 80% compared to the previous value (10)

        long testValue1 = pt.getAverageVelocity().get(0);
        long testValue2 = pt.getAverageVelocity().get(1);
        long testValue3 = pt.getAverageVelocity().get(2);
        long testValue4 = pt.getAverageVelocity().get(3);

        assertEquals(15, testValue1);
        assertEquals(14, testValue2);
        assertEquals(10, testValue3);
        assertEquals(9, testValue4);

    }


    /**
     * Tests whether the program can detect and account for anomalies in sensor data with regards to distance
     */
    @Test
    public void averageDistanceTravelledAnomalyDetection() {

        PerformanceTracker pt = new PerformanceTracker(60, 0);

        pt.updateData(1, (short) 11);
        pt.updateData(4, (short) 15);
        pt.updateData(21, (short) 19);
        pt.updateData(55, (short) 15);
        // sum of above values over an entire minute is 900

        pt.updateData(64, (short) 14);
        pt.updateData(68, (short) 120);
        // sum is 4020

        pt.updateData(123, (short) 8);
        pt.updateData(131, (short) 12);
        // sum is 600

        pt.updateData(195, (short) -23);
        pt.updateData(199, (short) 12);
        // sum is -660

        long testValue1 = pt.getAverageDistance().get(0);
        long testValue2 = pt.getAverageDistance().get(1);
        long testValue3 = pt.getAverageDistance().get(2);
        long testValue4 = pt.getAverageDistance().get(3);

        assertEquals(900, testValue1);
        assertEquals(840, testValue2); // because 14*60 = 840; if first value is not anomalous, distanceArray takes precedence over avgDistance
        assertEquals(600, testValue3);
        assertEquals(600, testValue4);

    }

    /**
     * Tests whether the program can correctly calculate the average speed for a specified time interval
     */
    @Test
    public void calculateAverageSpeedCustomIntervalLarger(){

        PerformanceTracker pt = new PerformanceTracker(100000, 0);

        pt.updateData(11700, (short) 11);
        pt.updateData(37546, (short) 15);
        pt.updateData(47125, (short) 19);
        pt.updateData(96173, (short) 15);
        // average of above values is 15
        pt.updateData(110236, (short) 14);
        pt.updateData(168623, (short) 18);
        // average is 16
        pt.updateData(201622, (short) 8);
        pt.updateData(256289, (short) 12);
        // average is 10

        long testValue1 = pt.getAverageVelocity().get(0);
        long testValue2 = pt.getAverageVelocity().get(1);
        long testValue3 = pt.getAverageVelocity().get(2);

        assertEquals(15, testValue1);
        assertEquals(16, testValue2);
        assertEquals(10, testValue3);

    }

    @Test
    public void calculateAverageSpeedCustomIntervalSmaller(){

        PerformanceTracker pt = new PerformanceTracker(30, 0);

        pt.updateData(12, (short) 11);
        pt.updateData(15, (short) 15);
        pt.updateData(22, (short) 19);
        pt.updateData(27, (short) 15);
        // average of above values is 15
        pt.updateData(37, (short) 14);
        pt.updateData(40, (short) 18);
        // average is 16
        pt.updateData(71, (short) 8);
        pt.updateData(89, (short) 12);
        // average is 10

        long testValue1 = pt.getAverageVelocity().get(0);
        long testValue2 = pt.getAverageVelocity().get(1);
        long testValue3 = pt.getAverageVelocity().get(2);

        assertEquals(15, testValue1);
        assertEquals(16, testValue2);
        assertEquals(10, testValue3);

    }


    /**
     * Tests whether the program can correctly calculate the average distance when given a specific time interval
     */
    @Test
    public void calculateAverageDistanceCustomIntervalLarger() {
        PerformanceTracker pt = new PerformanceTracker(100000, 0);

        pt.updateData(11700, (short) 11);
        pt.updateData(37546, (short) 15);
        pt.updateData(47125, (short) 19);
        pt.updateData(96173, (short) 15);
        // sum of above values is 900

        pt.updateData(110236, (short) 14);
        pt.updateData(168623, (short) 18);
        // sum is 960

        pt.updateData(201622, (short) 8);
        pt.updateData(256289, (short) 12);
        // sum is 600

        long testValue1 = pt.getAverageDistance().get(0);
        long testValue2 = pt.getAverageDistance().get(1);
        long testValue3 = pt.getAverageDistance().get(2);

        assertEquals(900, testValue1);
        assertEquals(960, testValue2);
        assertEquals(600, testValue3);
    }

    @Test
    public void calculateAverageDistanceCustomIntervalSmaller() {
        PerformanceTracker pt = new PerformanceTracker(30, 0);

        pt.updateData(12, (short) 11);
        pt.updateData(15, (short) 15);
        pt.updateData(22, (short) 19);
        pt.updateData(27, (short) 15);
        // sum of above values is 900

        pt.updateData(37, (short) 14);
        pt.updateData(40, (short) 18);
        // sum is 960

        pt.updateData(71, (short) 8);
        pt.updateData(89, (short) 12);
        // sum is 600

        long testValue1 = pt.getAverageDistance().get(0);
        long testValue2 = pt.getAverageDistance().get(1);
        long testValue3 = pt.getAverageDistance().get(2);

        assertEquals(900, testValue1);
        assertEquals(960, testValue2);
        assertEquals(600, testValue3);
    }

}