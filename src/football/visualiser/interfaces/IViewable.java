package football.visualiser.interfaces;

import javafx.scene.Scene;

/**
 * <h1>Viewable interface</h1>
 * A viewable interface should should be a class which manages the way the scene is displayed
 * and should provide access to the scene via the get method
 *
 * @author Oscar Mason
 */
public interface IViewable {
    void initialiseView() throws Exception;
    Scene getScene();
}
