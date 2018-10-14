package football.visualiser.models;

/**
 * <h1>Pair</h1>
 *
 * Generic data structure which stores two values of any type
 *
 * @author Oscar Mason
 *
 * @param <T>   Type of the first value
 * @param <U>   Type of the second value
 */
public class Pair<T, U>
{
    private T firstValue;
    private U secondValue;

    public void setFirstValue(T firstValue){
        this.firstValue = firstValue;
    }

    public T getFirstValue(){
        return firstValue;
    }

    public void setSecondValue(U secondValue){
        this.secondValue = secondValue;
    }

    public U getSecondValue(){
        return secondValue;
    }
}
