package football.visualiser.view;

import football.visualiser.interfaces.ISeekbarListener;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * <h1>Highlight</h1>
 * A highlight in the context of the football match is an interesting part of the match.
 * These are presented to the user in the form of a small rectangle on the seek bar which
 * the user is able to click on to jump straight to that point in the match.
 *
 * The highlight class stores the type of the highlight, the colours, and the test to display
 * when the user hovers their mouse over the highlight in the seek bar
 *
 * @author Oscar Mason
 */
public class Highlight extends Rectangle{
    public enum HighlightType { TACKLE, RED_GOAL, BLUE_GOAL, CORNER_SHOT, RED_121_PASS, BLUE_121_PASS }

    private final static Color[] highlightColors = { Color.hsb(51, 0.8, 0.9, 1.0), Color.hsb(351, 0.8, 0.9),
            Color.hsb(201, 0.8, 0.9), Color.hsb(199, 0.8, 0.60), Color.hsb(255, 0.8, 0.60), Color.hsb(255, 0.8, 0.60)};
    private final static String[] highlightText = { "Tackle", "Red goal", "Blue goal", "Corner shot", "Red 1-2-1 Pass",
            "Blue 1-2-1 Pass" };
    private final static int highlightHeight = 10;
    private final static int highlightWidth = 4;

    private Color highlightColor;
    private Color strokeColor = Color.rgb(70, 70, 70);
    private String highlightTypeText;

    private ISeekbarListener highlightMouseListener;
    private double highlightTimeRatio;

    public Highlight(HighlightType highlightType, double highlightTimeRatio, Slider slider,
                     ISeekbarListener highlightMouseListener){
        super();
        this.highlightMouseListener = highlightMouseListener;
        this.highlightTimeRatio = highlightTimeRatio;

        highlightColor = highlightColors[highlightType.ordinal()];
        highlightTypeText = highlightText[highlightType.ordinal()];

        setFill(highlightColor);
        setHeight(highlightHeight);
        setStroke(strokeColor);
        setWidth(highlightWidth);
        setY(0);
        setX(0);
        translateYProperty().bind(slider.minHeightProperty().subtract(highlightHeight));
        translateXProperty().bind(slider.prefWidthProperty().multiply(highlightTimeRatio)
                .add(slider.layoutXProperty()));

        setMouseListeners();
    }

    private void setMouseListeners(){

        setOnMouseEntered(event -> highlightMouseListener.handleMouseHover(this, event));

        setOnMousePressed(event -> highlightMouseListener.handleMousePressed(highlightTimeRatio));

        setOnMouseReleased(event -> highlightMouseListener.handleMouseReleased(highlightTimeRatio));

        setOnMouseExited(event -> highlightMouseListener.handleMouseExited());
    }

    public String getHighlightTypeText(){
        return highlightTypeText;
    }
}
