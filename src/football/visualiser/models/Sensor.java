package football.visualiser.models;

import football.visualiser.interfaces.ISensor;
import football.visualiser.models.entities.Entity;

/**
 * <h1>Sensor</h1>
 *
 * Concrete sensor classes, such as those used for the footballs and players, should extend this class
 * It provides methods for storing and retrieving: the position of the sensor, and the owner of the sensor
 *
 * Each entity has a primary sensor; a sensor whose ID is also used to identify the identity itself
 *
 * @author Oscar Mason
 *
 * @param <T>
 */

public abstract class Sensor<T extends Entity> implements ISensor<T> {
    private int id;
    private T owner;
    private int x;
    private int y;
    private boolean isPrimarySensor;

    public Sensor(int id, T owner, boolean isPrimarySensor){
        this.id = id;
        this.owner = owner;
        this.isPrimarySensor = isPrimarySensor;
    }

    @Override
    public void setOwner(T owner){
        this.owner = owner;
    }

    @Override
    public void setX(int x) {
        if(isPrimarySensor) getOwner().setX(x);
        this.x = x;
    }

    @Override
    public void setY(int y) {
        if(isPrimarySensor) getOwner().setY(y);
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public T getOwner() {
        return owner;
    }

}
