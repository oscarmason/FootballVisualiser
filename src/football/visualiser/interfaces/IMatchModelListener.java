package football.visualiser.interfaces;

/**
 * <h1>Match Model Listener Interface</h1>
 * Classes which implement the IMatchModelListener should be able to handle
 * seek bar events, check for and goals, and observe all entity positions
 * @author Oscar Mason
 */
public interface IMatchModelListener extends ISeekbarListener, IGoalUpdater, IEntityObserver {
}
