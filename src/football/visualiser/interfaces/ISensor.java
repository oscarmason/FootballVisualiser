package football.visualiser.interfaces;

import football.visualiser.models.entities.Entity;

/**
 * <H1>Sensor interface</H1>
 * Interface which should be extended by well defined sensors i.e. those that are attached to a player
 * @param <T>   Owner, which should be of type entity, that the sensor is attached to
 *
 * @author Oscar Mason
 */
public interface ISensor<T extends Entity> {

    void setX(int x);
    void setY(int y);
    void setOwner(T owner);

    int getX();
    int getY();
    int getID();
    T getOwner();
}
