package football.visualiser.view;

import football.visualiser.SystemData;
import javafx.geometry.Pos;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

/**<h1>Performance View</h1>
 * Created by Dovydas Ciomenas on 03/04/2017.
 * A Performance view for a separate stage to display player's performance values.
 * @author Dovydas Ciomenas, Irene Zeng
 */
public class PerformanceView{
    private FXMLLoader fxmlLoader;
    private Node perfViewNode;
    private Stage stage;
    private Scene scene;
    private AnchorPane root;
    private double width;
    private double height;
    private int playerID;
    private SystemData.Team team;
    private Text goals;
    private Text corners;
    private Text speed;
    private Text possessions;
    private Text tackle;
    private Text penalties;
    private Text passes;
    private Map<Integer, Integer> individualRedGoals;
    private Map<Integer, Integer> individualBlueGoals;
    private Map<Integer, Integer> individualSuccPasses;
    private Map<Integer, Integer> individualBlueTackles;
    private Map<Integer, Integer> individualRedTackles;
    private Map<Integer, Double> individualBluePossessionTimes;
    private Map<Integer, Double> individualRedPossessionTimes;
    private ArrayList<Integer> individualDistance;

    public PerformanceView(int playerID, SystemData.Team team,
                           Map<Integer, Integer> individualBlueGoals, Map<Integer, Integer> individualRedGoals,
                           Map<Integer, Integer> individualSuccPasses,
                           Map<Integer, Integer> individualBlueTackles, Map<Integer, Integer> individualRedTackles,
                           Map<Integer, Double> individualBluePossessionTimes, Map<Integer, Double> individualRedPossessionTimes,
                           ArrayList<Integer> individualDistance
    ) throws IOException {
        this.playerID = playerID;
        this.team = team;
        this.individualRedGoals = individualRedGoals;
        this.individualBlueGoals = individualBlueGoals;
        this.individualSuccPasses = individualSuccPasses;
        this.individualBluePossessionTimes = individualBluePossessionTimes;
        this.individualRedPossessionTimes = individualRedPossessionTimes;
        this.individualDistance = individualDistance;

        //Initialise View
        stage = new Stage();
        stage.setResizable(false);
        fxmlLoader = new FXMLLoader(getClass().getResource("PerformanceView.fxml"));
        Parent fxmlRoot = fxmlLoader.load();
        scene = new Scene(fxmlRoot);
        stage.setScene(scene);
        stage.show();
        perfViewNode = fxmlLoader.getRoot();

        //Lookup
        FlowPane chartContainer = (FlowPane) scene.lookup("#chartContainer");
        Pane playerPane = (Pane) scene.lookup("#playerPane");
        HBox gradient = (HBox) scene.lookup("#gradient");
        Text idText = (Text) scene.lookup("#id");
        Text idText2 = (Text) scene.lookup("#id2");
        Text teamText = (Text) scene.lookup("#teamText");
        Circle circle = (Circle) scene.lookup("#circle");
        GridPane playerStatContainer = (GridPane) scene.lookup("#playerStatContainer");

        goals = (Text) scene.lookup("#goals");
        corners = (Text) scene.lookup("#corners");
        speed = (Text) scene.lookup("#speed");
        possessions = (Text) scene.lookup("#possessions");
        tackle = (Text) scene.lookup("#tackle");
        penalties = (Text) scene.lookup("#penalties");
        passes = (Text) scene.lookup("#passes");

        //Initialise Objects
        Rectangle gradRec = new Rectangle(0, 0, playerPane.getWidth(), playerPane.getHeight());
        Stop[] stopsBlue = new Stop[] { new Stop(0, Color.DODGERBLUE), new Stop(1, Color.rgb(22, 22, 32))};
        LinearGradient lgBlue = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stopsBlue);
        Stop[] stopsRed = new Stop[] { new Stop(0, Color.rgb(255,40,40)), new Stop(1, Color.rgb(22, 22, 32))};
        LinearGradient lgRed = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stopsRed);

        LineChart<Number, Number>  lineChartForDistance = createLineChart("Distance over time", "Distance (mm)", convertArrayListToArray(individualDistance));

        //Set Node Properties
        chartContainer.setOrientation(Orientation.HORIZONTAL);
        gradient.setOpacity(0.1);
        idText.setText(Integer.toString(playerID));
        idText2.setText(Integer.toString(playerID));

        //Set Node Properties for Different Teams
        if (team == SystemData.Team.BLUE) {
            gradRec.setFill(lgBlue);
            circle.setFill(Color.DODGERBLUE);
            teamText.setText("Team Blue");

            //Update goals for each player
            goals.setText(Integer.toString(getGoals(individualBlueGoals)));

            //Update tackles for each player
            tackle.setText(Integer.toString(getTackles(individualBlueTackles)));

            //Update possession times for each player
            possessions.setText(Double.toString(getPossessionTimes(individualBluePossessionTimes)));

        } else {
            gradRec.setFill(lgRed);
            circle.setFill(Color.rgb(255,40,40));
            teamText.setText("Team Red");

            //Update goals for each player
            goals.setText(Integer.toString(getGoals(individualRedGoals)));

            //Update tackles for each player
            tackle.setText(Integer.toString(getTackles(individualRedTackles)));

            //Update possession times for each player
            possessions.setText(Double.toString(getPossessionTimes(individualRedPossessionTimes)));
        }

        //Update successful passes for each player
        passes.setText(Integer.toString(getSuccPasses(individualSuccPasses)));

        //Populating the Player Statistics Container
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                Pane pane = new Pane();
                playerStatContainer.add(pane, i, j);
                pane.setOpacity(0.25);
                if((j % 2.) == 0) {
                    pane.setId("even");
                    if((i % 2.) == 0) {
                        pane.setId("evenVal");
                    }
                } else {
                    pane.setId("odd");
                    if((i % 2.) != 0) {
                        pane.setId("oddVal");
                    }
                }
            }
        }

        //Add Components
        gradient.getChildren().add(gradRec);
        chartContainer.setAlignment(Pos.CENTER);
        chartContainer.getChildren().addAll(lineChartForDistance);

    }

    public LineChart<Number, Number> createLineChart(String title, String yLabel, Integer[] data){

        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Time (Minute)");
        yAxis.setLabel(yLabel);

        //creating the chart
        LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);

        lineChart.setTitle(title);

        //defining series
        XYChart.Series series = new XYChart.Series();

        lineChart.setLegendVisible(false);

        //populating the series with data

        int[] time = new int[data.length];
        for(Short i = 0; i < data.length; i++){
            time[i] = i;
        }

        for(int i = 0; i < time.length; i++){
            series.getData().add(new XYChart.Data(time[i], data[i]));
        }

        lineChart.getData().add(series);
        lineChart.setPadding(new Insets(40, 40, 0, 0));
        lineChart.setCenterShape(true);

        return lineChart;
    }

    public int getGoals(Map<Integer, Integer> individualGoals){

        int goals = 0;

        for (Map.Entry<Integer, Integer> entry : individualGoals.entrySet()) {

            Integer key = entry.getKey();

            if(key == playerID){
                goals = entry.getValue();
                break;
            }
        }
        return goals;
    }

    public int getSuccPasses(Map<Integer, Integer> individualSuccPasses){

        int passes = 0;

        for (Map.Entry<Integer, Integer> entry : individualSuccPasses.entrySet()) {

            Integer key = entry.getKey();

            if(key == playerID){
                passes = entry.getValue();
                break;
            }
        }
        return passes;
    }

    public int getTackles(Map<Integer, Integer> individualTackles){

        int tackles = 0;

        for (Map.Entry<Integer, Integer> entry : individualTackles.entrySet()) {

            Integer key = entry.getKey();

            if(key == playerID){
                tackles = entry.getValue();
                break;
            }
        }
        return tackles;
    }

    public double getPossessionTimes(Map<Integer, Double> individualPossessionTimes){

        double possessionTimes = 0;

        for (Map.Entry<Integer, Double> entry : individualPossessionTimes.entrySet()) {

            Integer key = entry.getKey();

            if(key == playerID){
                possessionTimes = entry.getValue();
                break;
            }
        }
        return possessionTimes;
    }

    public Integer[] convertArrayListToArray(ArrayList<Integer> data){

        Integer[] arrayData = data.toArray(new Integer[data.size()]);
        return arrayData;
    }
}
