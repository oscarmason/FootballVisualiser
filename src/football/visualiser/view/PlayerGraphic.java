package football.visualiser.view;

import football.visualiser.SystemData.Team;
import javafx.scene.CacheHint;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * <h1>Player Graphic</h1>
 * Player graphic is the graphical object which represents each football player when in match playback mode
 *
 * @author Oscar Mason
 */
public class PlayerGraphic extends MovableGraphic {

    private int id;
    private Team team;

    public PlayerGraphic(Team team, int id, boolean isGoalKeeper){
        super();
        this.id = id;
        this.team = team;

        Color color;
        Color redTeamColor = Color.MAROON;
        Color blueTeamColor = Color.BLUE;
        Color strokeColor = Color.WHITE;
        Color fontColor = Color.WHITE;

        int strokeWidth = 1;
        int fontSize = 10;

        String fontFamily = "Arial";

        color = team == Team.RED ? redTeamColor : blueTeamColor;

        if(isGoalKeeper) strokeWidth = 2;

        circle = new Circle();

        circle.setFill(color);

        circle.setStroke(strokeColor);
        circle.setStrokeWidth(strokeWidth);
        circle.setStrokeType(StrokeType.INSIDE);

        Text playerText = new Text(Integer.toString(id));

        playerText.setFill(fontColor);
        playerText.setFont(new Font(fontFamily, fontSize));

        getChildren().addAll(circle, playerText);

        setBlendMode(BlendMode.SRC_ATOP);

        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }

    public int getPlayerID(){
        return id;
    }
}
