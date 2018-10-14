package football.visualiser.interfaces;

import football.visualiser.models.entities.Player;

/**
 * <H1>Player sensor interface</H1>
 * Interface which concrete player sensor classes should implement
 * @param <T>   Owner, which should be of type Player, that the sensor is attached to
 *
 * @author Oscar Mason
 */

public interface IPlayerSensor<T extends Player> extends ISensor<T> {

}

