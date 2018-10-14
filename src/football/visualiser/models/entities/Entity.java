package football.visualiser.models.entities;

/**
 * <h1>Entity</h1>
 * This class should be inherited by entities which move around the field such as players,
 * footballs, and goal keepers.
 *
 * It provides methods for storing and retrieving the location and id of the entity
 *
 * @author Oscar Mason
 *
 */
public abstract class Entity{
    private int x;
    private int y;
    protected int id;

    public Entity(int id){
        this.id = id;
    }

    public int getID(){
        return id;
    };

    public void setX(int x){
        this.x = x;
    }

    public void setY(int y){
        this.y = y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }
}
