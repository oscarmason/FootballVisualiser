package football.visualiser.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

/**<h1>Data Item</h1>
 * Created by Dovydas Ciomenas on 07/03/2017.
 * A Data Item view class that displays the stored match property as a selectable pane in the start view.
 * @author Dovydas Ciomenas
 */
public class DataItem extends Pane {
    String name;
    String filePath;
    Text displayName;
    Text displayFilePath;
    Button removeItemButton;

    public DataItem(int id, String name, String filePath) {
        super();

        this.name = name;
        this.filePath = filePath;

        if(name.length() > 20) {
            displayName = new Text(name.substring(0, 20) + "...");
        } else {
            displayName = new Text(name);
        }
        this.displayName.setId("dataName");
        if(filePath.length() > 33) {

            displayFilePath = new Text("..." + filePath.substring(filePath.length()-30, filePath.length()));
        } else {
            displayFilePath = new Text(filePath);
        }
        this.displayFilePath.setId("dataFilePath");

        setMinSize(USE_COMPUTED_SIZE, 70);
        setPrefSize(USE_PREF_SIZE, 70);
        setMaxSize(USE_COMPUTED_SIZE, 70);

        if((id % 2.) == 0) {
            setId("dataItemEven");
        } else {
            setId("dataItemOdd");
        }

        BorderPane anchor = new BorderPane();
        HBox hbox = new HBox();
        GridPane gridPane = new GridPane();
        gridPane.add(displayName, 0, 0);
        gridPane.add(displayFilePath, 0, 1);

        gridPane.setMargin(this.displayName, new Insets(12, 0, 0, 10));
        gridPane.setMargin(this.displayFilePath, new Insets(0, 0, 0, 10));

        removeItemButton = new Button();
        removeItemButton.setText("x");
        removeItemButton.setId("removeButton");

        removeItemButton.setPadding(new Insets(8, 12, 8, 12));

        anchor.setPrefSize(262.0, 70);
        anchor.setLeft(gridPane);
        anchor.setRight(removeItemButton);
        getChildren().addAll(anchor);

    }

    public String getName() {
        return name;
    }

    public Button getRemoveButton() {
        return removeItemButton;
    }
}
