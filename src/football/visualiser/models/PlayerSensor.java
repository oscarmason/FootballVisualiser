package football.visualiser.models;

import football.visualiser.interfaces.IPlayerSensor;
import football.visualiser.models.entities.Entity;
import football.visualiser.models.entities.Player;

/**
 * <H1>Player Sensor</H1>
 * Sensor attached to a player
 *
 * @param <T>   Owner, which should be of type Player, that the sensor is attached to
 *
 * @author Oscar Mason
 */
public class PlayerSensor<T extends Player> extends Sensor<T> implements IPlayerSensor<T> {

    public PlayerSensor(int id, T owner, boolean isPrimarySensor) {
        super(id, owner, isPrimarySensor);
    }
}

