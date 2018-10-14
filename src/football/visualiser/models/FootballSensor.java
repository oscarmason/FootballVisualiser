package football.visualiser.models;

import football.visualiser.interfaces.IFootballSensor;
import football.visualiser.models.entities.Football;

/**
 * <H1>Football sensor</H1>
 * Sensor which is attached to a football
 * @param <T>   Owner, which should be of type Football, that the sensor is attached to
 *
 * @author Oscar Mason
 */
public class FootballSensor<T extends Football> extends Sensor<T> implements IFootballSensor<T> {
    public FootballSensor(int id, T owner, boolean isPrimarySensor){
        super(id, owner, isPrimarySensor);
    }
}
