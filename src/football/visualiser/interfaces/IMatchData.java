package football.visualiser.interfaces;

/**
 * <H1>Match data interface</H1>
 * Classes which implement this interface should provide implementations which follow
 * methods to get individual lines and go to a particular time stamp
 *
 * @author Oscar Mason
 */
public interface IMatchData {
    /**
     * Goes to the requested time stamp in the match. If the time stamp does not exact,
     * go to the next available time stamp
     *
     * @param timeStamp The time to progress to
     */
    void goToTimeStamp(String timeStamp);

    /**
     * Method which navigates back to the start of the match
     */
    void goToFirstHalf();

    /**
     * Reads the next line in a match data file ready for processing
     *
     * @return Array of strings where all the information is separated and placed
     * in a separate position in the array
     */
    String[] getNextLineAsString();

    /**
     * Gets the next line from the data file as a list of integers
     *
     * @return List containing all the information in a single line of the data file
     */
    int[] getNextLineAsInt();

    /**
     * Get the timestamps for when the first half of the match starts and ends
     * and when the second half of the match starts and ends
     *
     * @return A list of timestamps
     */
    int[] getStartEndTimeStamps();

    /**
     * Get the total playing time of the match
     *
     * @return Return the total match playing time in milliseconds
     */
    int getTotalMatchTimeInMilliseconds();

    /**
     * Allows the strength of the analysation process to be altered. The minimum value should be 1.
     * The higher the value, the less accurate the analysation process will be
     *
     * @param analyticalStrength    Strength of the analysation process
     */
    void setAnalyticalStrength(int analyticalStrength);

}