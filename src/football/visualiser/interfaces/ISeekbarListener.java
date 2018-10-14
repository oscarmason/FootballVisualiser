package football.visualiser.interfaces;

import football.visualiser.view.Highlight;
import javafx.scene.input.MouseEvent;

/**
 * Classes which implement this interface should provide methods for handling
 * user interactions such as when they click on the seek bar or a highlight
 * @author Oscar Mason
 */
public interface ISeekbarListener {
    void handleMouseHover(Highlight highlight, MouseEvent event);
    void handleMousePressed(double timeRatio);
    void handleMouseReleased(double timeRatio);
    void handleMouseExited();
    void updateCursorPosition(int currentTimeMilliseconds);
}
