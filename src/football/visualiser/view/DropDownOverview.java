package football.visualiser.view;

import com.sun.javafx.tools.ant.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.*;

/**<h1>Drop Down Overview</h1>
 * Created by Dovydas Ciomenas on 02/04/2017.
 * The Overview of the whole match displayed in the drop down view.
 * @author Dovydas Ciomenas, Irene Zeng
 */
public class DropDownOverview extends AnchorPane {
    private double width;
    private double height;
    private BorderPane bPane;
    private TableView tableView;
    private Map<Integer, Integer> individualRedGoals;
    private Map<Integer, Integer> individualBlueGoals;
    private Map<Integer, Integer> sortedRedGoals;
    private Map<Integer, Integer> sortedBlueGoals;
    private ScrollPane scrollPane;
    private Pane blueContainer;
    private Pane redContainer;
    private VBox blueOverview;
    private VBox redOverview;

    public DropDownOverview() {
        super();

        HBox teamPanesContainer = new HBox();
        blueContainer = new Pane();
        redContainer = new Pane();
        blueOverview = new VBox();
        redOverview = new VBox();

        getChildren().add(teamPanesContainer);
        teamPanesContainer.getChildren().addAll(blueContainer, redContainer);
        blueContainer.getChildren().add(blueOverview);
        redContainer.getChildren().add(redOverview);

        setTopAnchor(teamPanesContainer, 0.);
        setLeftAnchor(teamPanesContainer, 0.);
        setRightAnchor(teamPanesContainer, 0.);

        //Bind Components
        teamPanesContainer.prefWidthProperty().bind(widthProperty());
        blueContainer.minWidthProperty().bind(teamPanesContainer.widthProperty().divide(2));
        redContainer.minWidthProperty().bind(teamPanesContainer.widthProperty().divide(2));


        teamPanesContainer.setAlignment(Pos.CENTER);


        //For Testing Purposes
        blueContainer.setStyle("-fx-background-color: transparent;");
        redContainer.setStyle("-fx-background-color: transparent;");
        blueOverview.setStyle("-fx-background-color: transparent;");
        redOverview.setStyle("-fx-background-color: transparent;");
        blueOverview.setStyle("-fx-padding: 20px;");
        redOverview.setStyle("-fx-padding: 20px;");
        teamPanesContainer.setStyle("-fx-background-color: transparent;");
        setStyle("-fx-background-color: transparent;");

    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;

    }

    private void addSortedPlayerList(int colForID, int colForGoals, Map<Integer, Integer> individualGoals, Pane container) {

        GridPane gridPane = new GridPane();

        Text id = new Text("Player ID");
        id.setFill(Color.WHITE);
        Text goals = new Text("Goals");
        goals.setFill(Color.WHITE);

        gridPane.add(id, colForID, 0);
        gridPane.add(goals, colForGoals, 0);

        int r = 1;

        for(Map.Entry<Integer, Integer> entry: individualGoals.entrySet()){
            Pane paneId = new Pane();
            paneId.setPrefHeight(60);
            paneId.setPrefWidth(120);
            Pane paneGoals = new Pane();
            paneGoals.setPrefHeight(60);
            if((r % 2.) == 0) {
                paneId.setStyle("-fx-background-color: rgba(32, 33, 42, 0.15);");
                paneGoals.setStyle("-fx-background-color: rgba(62, 63, 72, 0.15);");
            } else {
                paneId.setStyle("-fx-background-color: rgba(42, 43, 52, 0.15);");
                paneGoals.setStyle("-fx-background-color: rgba(72, 73, 82, 0.15);");
            }

            gridPane.add(paneId, colForID, r);
            gridPane.add(paneGoals, colForGoals, r);

            Integer key = entry.getKey();
            Integer value = entry.getValue();

            Text playerID = new Text(Integer.toString(key));
            playerID.setFill(Color.WHITE);

            Text playerGoals = new Text(Integer.toString(value));
            playerGoals.setFill(Color.WHITE);


            gridPane.add(playerID, colForID, r);
            gridPane.add(playerGoals, colForGoals, r);

            gridPane.setAlignment(Pos.CENTER);


            playerID.setTextAlignment(TextAlignment.CENTER);
            playerGoals.setTextAlignment(TextAlignment.CENTER);

            playerID.setLayoutX(27);
            playerGoals.setLayoutX(7);

            gridPane.setHgap(1);
            gridPane.setVgap(1);



            r++;
        }


        container.getChildren().add(gridPane);
    }

    public void setIndividualGoals(Map<Integer, Integer> individualRedGoals, Map<Integer, Integer> individualBlueGoals){
        // update the visible text
        sortedBlueGoals = sortIndividualGoals(individualBlueGoals);
        sortedRedGoals = sortIndividualGoals(individualRedGoals);

        addSortedPlayerList(0,1,sortedBlueGoals, blueOverview);
        addSortedPlayerList(0,1,sortedRedGoals, redOverview);
    }

    public Map<Integer, Integer> sortIndividualGoals(Map<Integer, Integer> individualGoals){

        List<Map.Entry<Integer, Integer>> listForGoals = new LinkedList<Map.Entry<Integer, Integer>>(individualGoals.entrySet());

        Collections.sort(listForGoals, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1,
                               Map.Entry<Integer, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<Integer, Integer> sortedGoals = new LinkedHashMap<Integer, Integer>();

        for (Map.Entry<Integer, Integer> entry : listForGoals) {
            sortedGoals.put(entry.getKey(), entry.getValue());
        }

        return sortedGoals;
    }

    public void printTwoTeamGoals(){
        printGoals(sortedBlueGoals);
        printGoals(sortedRedGoals);
    }

    public void printGoals(Map<Integer, Integer> goals){
        for (Map.Entry<Integer, Integer> entry : goals.entrySet()) {
            System.out.println("ID: " + entry.getKey() + " Goals: " + entry.getValue());
        }
    }

}
