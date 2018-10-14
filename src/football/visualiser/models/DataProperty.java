package football.visualiser.models;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by Dovydas Ciomenas on 22/03/2017.
 * Data Property for storing and reading stored matches.
 * @author Dovydas Ciomenas
 */
public class DataProperty extends Properties {
    private String name;

    public DataProperty(String name) throws Exception {
        this.name = name;
        loadProperty();
    }

    public void loadProperty() throws Exception {
        File file = new File("resources/properties/" + name + ".properties");
        FileInputStream input = new FileInputStream(file);

        new Properties();

        load(input);
        input.close();
    }
}
