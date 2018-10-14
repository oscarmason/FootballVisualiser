package football.visualiser.models;

import static football.visualiser.SystemData.HEAT_MAP_WIDTH;

/**
 * <h1>Heat Map</h1>
 *
 * Heat map keeps track of where on the pitch the players spent their time
 *
 * @author Oscar Mason
 */

public class HeatMap {
    private int[][] heatMap;
    private int heatMapHeight;
    private Pitch pitch;

    public HeatMap(Pitch pitch){
        this.pitch = pitch;

        // Calculate the height of the heat map data structure using the aspect ratio of the pitch itself
        heatMapHeight = (int) (HEAT_MAP_WIDTH * pitch.getRatio()) + 1;
        heatMap = new int[HEAT_MAP_WIDTH][heatMapHeight];
    }

    /**
     * Each time the function is called, it increments the players heat map by 1 for the relevant x and y
     * position in the heat map array
     *
     * Author: Oscar Mason
     *
     * @param x                 X Coordinate of the player
     * @param y                 Y Coordinate of the player
     * @param sidesSwitched     Check whether the sides have switched at half time
     */
    public void incrementHeatMap(int x, int y, boolean sidesSwitched){
        int xFromZero = x - pitch.getX1();
        int yFromZero = y - pitch.getY1();

        // Because the pitch may not always start from 0, it needs to be converted first
        xFromZero = (HEAT_MAP_WIDTH * xFromZero) / pitch.getWidth();
        yFromZero = (heatMapHeight * yFromZero) / pitch.getHeight();

        if(sidesSwitched) xFromZero = HEAT_MAP_WIDTH - xFromZero;

        // Prevent array out of bounds exceptions
        xFromZero = Math.max(0, xFromZero);
        xFromZero = Math.min(HEAT_MAP_WIDTH - 1, xFromZero);
        yFromZero = Math.max(0, yFromZero);
        yFromZero = Math.min(heatMapHeight - 1, yFromZero);

        heatMap[xFromZero][yFromZero]++;
    }

    public int[][] getHeatMap(){
        return heatMap;
    }
}
