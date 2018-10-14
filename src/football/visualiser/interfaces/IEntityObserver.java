package football.visualiser.interfaces;

/**
 * <h1>Entity Observer Interface</h1>
 * Classes which implement the IEntityObserver should listen for changes to the position
 * of entities (players and footballs) as the game is played back. They should then instruct
 * the relevant view class to make changes to the position of the GUI elements
 *
 * @author Oscar Mason
 */
public interface IEntityObserver {
    void updatePosition(int ID, int x, int y);
}
