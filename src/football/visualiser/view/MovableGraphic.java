package football.visualiser.view;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

/**
 * <h1>Movable Graphic</h1>
 * A movable graphic is a graphic object which can move around the screen
 *
 * @author Oscar Mason
 */

public abstract class MovableGraphic extends StackPane {
    protected Circle circle;

    public void updateGraphicPosition(int x, int y) {
        final double updateX = x - circle.getRadius();
        final double updateY = y - circle.getRadius();
        y -= circle.getRadius();

        Platform.runLater(() -> {
                setTranslateX(updateX);
                setTranslateY(updateY);
        });
    }

    public void setSize(double size){
        circle.setRadius(size);
    }
}
