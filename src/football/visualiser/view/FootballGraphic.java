package football.visualiser.view;

import javafx.scene.CacheHint;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * <h1>Football Graphic</h1>
 * Graphic used for displaying the football on the GUI
 *
 * @author Oscar Mason
 */

public class FootballGraphic extends MovableGraphic {


    public FootballGraphic(){
        super();

        Color color = Color.WHITE;
        Color strokeColor = Color.BLACK;
        int strokeWidth = 1;

        circle = new Circle();

        circle.setFill(color);

        circle.setStroke(strokeColor);
        circle.setStrokeWidth(strokeWidth);

        getChildren().addAll(circle);

        setBlendMode(BlendMode.SRC_ATOP);

        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }
}
