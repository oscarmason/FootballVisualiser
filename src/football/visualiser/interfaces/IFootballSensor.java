package football.visualiser.interfaces;

import football.visualiser.models.entities.Football;

/**
 * <H1>Football sensor interface</h1>
 * Interface which concrete football sensor classes should implement
 * @param <T>   Owner, which should be of type Football, that the sensor is attached to
 *
 * @author Oscar Mason
 */

public interface IFootballSensor <T extends Football> extends ISensor<T>{
}
