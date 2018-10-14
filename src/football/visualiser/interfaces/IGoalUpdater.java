package football.visualiser.interfaces;

/**
 * <H1>Goal Update Interface</H1>
 * Classes which implement IGoalUpdater should check the number of goals that have
 * occurred up to the current millisecond in the match
 *
 * @author Oscar Mason
 */
public interface IGoalUpdater {
    void updateGoalCount(int currentMillisecond);
}
