package football.visualiser.interfaces;

import football.visualiser.SystemData.Team;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * <h1>Match view interface</h1>
 * Classes which implement this interface should provide methods to add a player graphic to the pitch,
 * add a football to the pitch, and update the goal count if and when required
 *
 * @author Oscar Mason
 */
public interface IMatchView extends IViewable{
    void addPlayer(Team team, int ID, boolean isGoalKeeper, EventHandler<MouseEvent> mouseEventHandler);
    void addFootball(int ID);
    void updateGoalCount(int redNumberOfGoals, int blueNumberOfGoals);
}
