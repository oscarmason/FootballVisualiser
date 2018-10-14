package football.visualiser.models;

import football.visualiser.SystemData;
import football.visualiser.interfaces.IMatchData;

import static football.visualiser.SystemData.*;

import java.io.*;

/**
 * <H1>Match data</H1>
 * Provides methods to traverse a match data input file including sequential access and the ability to
 * go to a particular timestamp
 *
 * @author Oscar Mason
 */
public class MatchData implements IMatchData{

    private RandomAccessFile randomAccessFile;
    private BufferedReader currentLine;
    private final long FILE_SIZE;
    private String[] matchTimeStamps;
    private int[] startEndTimeStampsMilliseconds = new int[4];
    private int totalMatchTime;
    private int analyticalStrength = 3;

    /**
     * Opens the match data file and creates a buffered reader which points to the start of the file.
     * Gets the size of the file which is used by {@link #goToTimeStamp(String)} method
     *
     * Author: Oscar Mason
     *
     * @param fileLocation  Location of the data file to open
     * @throws IOException  If the file fails to open, an IO exception will be thrown
     */
    public MatchData(String fileLocation, String[] matchTimeStamps) throws IOException{
        this.matchTimeStamps = matchTimeStamps;

        for(int i = 0; i < matchTimeStamps.length; i++){
            startEndTimeStampsMilliseconds[i] = convertTimeStampToMilliseconds(matchTimeStamps[i]);
        }

        randomAccessFile = new RandomAccessFile(fileLocation, "r");
        FILE_SIZE = randomAccessFile.length();

        currentLine = new BufferedReader(new InputStreamReader(new FileInputStream(randomAccessFile.getFD())));
        goToFirstHalf();

        setTotalMatchLength();
    }

    /**
     * Goes to the requested timestamp in the match data file
     *
     * Uses a variant of the binary search algorithm to efficiently find the timestamp.
     * Because BufferedReader does not provide a method for random file access, the quickest
     * way of traversing a file is to read each line one at a time, which is extremely time
     * consuming.
     *
     * Therefore, it is required to first seek to a position using the RandomAccessFile, and
     * using that to object create a new BufferedReader.
     *
     * RandomAccessFile goes to a specified byte in the file, which means that the buffered
     * reader will almost always be half way through reading a line. Because of this, the
     * time requested would be missed using a traditional binary search.
     *
     * To solve this, upon converging to the approximate location, goToTimeStamp backtracks
     * to the lowest most byte value from the variables first, middle, and last, and from
     * that point, reads each line individually until the requested time stamp is found.
     *
     * NOTE: While RandomAccessFile does contain a read line method for sequential access,
     * it does not use a buffer which makes it extremely slow compared to buffered reader
     * which becomes very noticeable on large files.
     *
     * Author: Oscar Mason
     *
     * @param timeStamp     The time to progress to
     */
    @Override
    public void goToTimeStamp(String timeStamp){
        long first = 0;
        long last = FILE_SIZE - 1;
        long middle = (first + last) / 2;
        String[] line;

        while(first <= last){
            seek(middle);

            line = getNextLineAsStringNoTimeChecks();

            // If we have reached the end of the data file
            if(line == null) break;

            String time = line[dataTimeStamp];

            if(time.compareTo(timeStamp) < 0){
                first = middle + 1;
            }else if(time.compareTo(timeStamp) > 0){
                last = middle - 1;
            }else{
                break;
            }
            middle = (first + last) / 2;
        }

        // Backtrack to lowest byte position and read each line to find timestamp requested
        seek(Math.max(0, Math.min(Math.min(first, last), middle)));

        // Read each line to search for the timestamp requested
        String currentTime;
        String currentLine[];
        do{
            currentLine = getNextLineAsStringNoTimeChecks();
            if(currentLine == null) {
                currentTime = null;
            }else{
                currentTime = currentLine[dataTimeStamp];
            }

        } while(currentTime != null && currentTime.compareTo(timeStamp) < 0);
    }

    @Override
    public void goToFirstHalf() {
        goToTimeStamp(matchTimeStamps[SystemData.FIRST_HALF_START_TIME]);
    }

    /**
     * Generates an array of tokens for a line of text
     *
     * @param line      The line of text to extract data from
     * @return          String array containing the data
     */
    private String[] generateTokens(String line){
        if(line == null) return null;
        return line.split(",");
    }

    /**
     * Reads a single line from the match data file
     * @return  Returns the line
     */
    private String readLine(){
        try {
            return currentLine.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Reads the next line in the data file. If the data does not contain all the required fields it
     * will continue to read the next line until it does or until the end of the data file is reached
     *
     * Author: Oscar Mason
     *
     * @return  A string array containing the data of the line
     */
    @Override
    public String[] getNextLineAsString() {
        String line = "";
        String[] tokens;
        do{
            for(int i = 0; i < analyticalStrength; i++){
                readLine();
            }

            line = readLine();
            tokens = generateTokens(line);
            if (tokens == null) break;

            if(tokens[SystemData.dataTimeStamp].compareTo(matchTimeStamps[SystemData.FIRST_HALF_START_TIME]) < 0){
                goToTimeStamp(matchTimeStamps[SystemData.FIRST_HALF_START_TIME]);
                line = readLine();
                tokens = generateTokens(line);
            }else if(tokens[SystemData.dataTimeStamp].compareTo(matchTimeStamps[SystemData.FIRST_HALF_END_TIME]) > 0 &&
                    tokens[SystemData.dataTimeStamp].compareTo(matchTimeStamps[SystemData.SECOND_HALF_START_TIME]) < 0){
                goToTimeStamp(matchTimeStamps[SystemData.SECOND_HALF_START_TIME]);
                line = readLine();
                tokens = generateTokens(line);
            }else if(tokens[SystemData.dataTimeStamp].compareTo(matchTimeStamps[SystemData.SECOND_HALF_END_TIME]) > 0){
                tokens = null;
                line = null;
            }

        }while(line != null && tokens.length != 13);

        return tokens;
    }

    /**
     * This method is required by the {@link #goToTimeStamp(String)} method in order to find a time stamp
     *
     * Author: Oscar Mason
     *
     * @return  Reads the next line from the match data file
     */
    private String[] getNextLineAsStringNoTimeChecks(){
        String line = "";
        String[] tokens;
        do{
            line = readLine();
            tokens = generateTokens(line);
            if (tokens == null) break;

        }while(tokens.length != SystemData.numberOfFields);

        return tokens;
    }

    /**
     * Returns the next line following the byte position specified
     *
     * ReadLine is required because the seek function called on randomAccessFile will usually result in half a line
     * being read
     *
     * Author: Oscar Mason
     *
     * @param position  Number of bytes from the start of the file to move to
     */
    private void seek(long position){
        try {
            // Go to the position in the file
            randomAccessFile.seek(position);

            // Create a new buffer which points to the position specified then discard the rest of the line which
            // may be incomplete
            if(randomAccessFile != null){
                FileInputStream fileInputStream = new FileInputStream(randomAccessFile.getFD());
                currentLine = new BufferedReader(new InputStreamReader(fileInputStream));
                readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts an array of tokens of type string to int. The time stamp is converted to
     * milliseconds before being inserted into the array
     *
     * Author: Oscar Mason
     *
     * @return              An array of tokens
     */
    @Override
    public int[] getNextLineAsInt(){
        String[] stringTokens = getNextLineAsString();
        if(stringTokens == null) return null;

        int[] tokens = new int[stringTokens.length];

        for (int i = 0; i < stringTokens.length; i++) {
            try{
                if(i == dataTimeStamp){
                    tokens[i] = convertTimeStampToMilliseconds(stringTokens[i]);
                }else{
                    tokens[i] = Integer.parseInt(stringTokens[i]);
                }
            }catch (NumberFormatException e){
                System.out.println("Failed to parse input token");
            }
        }
        return tokens;
    }

    /**
     * Converts a time stamp from picoseconds to milliseconds
     *
     * Author: Oscar Mason
     *
     * @param timeStamp     Time stamp to be converted
     * @return              Requested time stamp in milliseconds
     */
    private int convertTimeStampToMilliseconds(String timeStamp){
        int time = 0;
        try{
            time = Integer.parseInt(timeStamp.substring(0, timeStamp.length() - SystemData.timeOffset));
        }catch (NumberFormatException e){
            System.out.println("Failed to parse input token");
        }
        return time;
    }

    public int[] getStartEndTimeStamps(){
        return startEndTimeStampsMilliseconds;
    }


    /**
     * Sets the length of the whole match in milliseconds
     *
     * Author: Oscar Mason
     */
    private void setTotalMatchLength(){
        totalMatchTime = startEndTimeStampsMilliseconds[1] - startEndTimeStampsMilliseconds[0] +
                startEndTimeStampsMilliseconds[3] -  startEndTimeStampsMilliseconds[2];
    }

    @Override
    public int getTotalMatchTimeInMilliseconds(){
        return totalMatchTime;
    }


    public void setAnalyticalStrength(int analyticalStrength){
        this.analyticalStrength = analyticalStrength;
    }

}
