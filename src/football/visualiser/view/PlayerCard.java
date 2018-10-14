package football.visualiser.view;

import football.visualiser.SystemData;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 * <h1>Player Card</h1>
 * Graphic displayed when the user clicks on a player to find out more player information
 *
 * @author Oscar Mason, Dovydas Ciomenas
 */
public class PlayerCard {
    private Node playerCardNode;
    private Text playerIDText;
    private Circle circle;
    private Button perfButton;
    private Pane heatMapPane;
    private int playerID;
    private double width;
    private double height;
    private double heatMapWidth = 400;
    private SystemData.Team team;

    public PlayerCard() throws IOException{

        FXMLLoader playerInfoFxmlLoader = new FXMLLoader(getClass().getResource("/football/visualiser/view/PlayerCard.fxml"));
        Parent fxmlRoot = playerInfoFxmlLoader.load();
        fxmlRoot.getStylesheets().add(getClass().getResource("PlayerCardStyle.css").toExternalForm());

        playerCardNode = playerInfoFxmlLoader.getRoot();
        playerIDText = (Text) playerCardNode.lookup("#playerID");
        circle = (Circle) playerCardNode.lookup("#circle");
        perfButton = (Button) playerCardNode.lookup("#perfButton");
        heatMapPane = (Pane) playerCardNode.lookup("#heatMap");
        heatMapPane.setBackground(new Background(
                new BackgroundFill(Color.hsb(25, 0.9, 0.9), CornerRadii.EMPTY, Insets.EMPTY)));
        width = 418;
        height = 480;
    }

    public int getPlayerID() {
        return playerID;
    }

    public Button getPerformanceButton() {
        return perfButton;
    }

    public Node getPlayerCardNode(){
        return playerCardNode;
    }

    public void setTranslate(double x, double y) {

        if(x >= playerCardNode.getScene().getWindow().getWidth() - width) {
            x -= width;
        }

        if(y >= playerCardNode.getScene().getWindow().getHeight() - height) {
            y -= height;
        }

        y = playerCardNode.getScene().getWindow().getHeight() / 2  - height / 2;

        playerCardNode.setLayoutX(x);
        playerCardNode.setLayoutY(y);
    }

    public void setPlayerIDText(int playerID){
        this.playerID = playerID;
        playerIDText.setText(Integer.toString(playerID));
    }

    public void setPlayerTeamColor(SystemData.Team team) {
        this.team = team;
        if(team == SystemData.Team.RED) {
            circle.setId("teamRed");
        } else {
            circle.setId("teamBlue");
        }
    }

    /**
     * Draws the graphic for the heat map on the player card
     *
     * Author: Oscar Mason
     *
     * @param heatMap   Heat map to draw
     */
    public void drawHeapMap(int[][] heatMap){
        double sideLength = (heatMapWidth) / heatMap.length;
        int hueRange = 80;
        int minHue = 25;
        int hue;
        double transparency = 1.0;
        Color color;
        int min = 1;
        int max = 0;

        // Finds the largest value in the heat map to prevent the min and max hue from overflowing
        for(int c = 0; c < heatMap.length; c++) {
            for (int r = 0; r < heatMap[c].length; r++) {
                if(heatMap[c][r] > max) max = heatMap[c][r];
            }
        }

        for(int r = 0; r < heatMap.length; r++) {
            for (int c = 0; c < heatMap[c].length; c++) {
                Rectangle rectangle = new Rectangle(sideLength + 1, sideLength + 1);
                rectangle.setTranslateX(sideLength * r);
                rectangle.setTranslateY(sideLength * c);

                // Calculates the hue based on the value in the heat map array
                hue = (hueRange + minHue) - (int) ((((double) heatMap[r][c] - min)
                        /(double) (max-min)) * hueRange + minHue);

                color = Color.hsb(hue, 0.8, 0.9, transparency);

                rectangle.setFill(color);

                heatMapPane.getChildren().add(rectangle);
            }
        }
    }

    public SystemData.Team getPlayerTeam() {
        return team;
    }

}
