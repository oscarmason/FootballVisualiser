package football.visualiser.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

/**
 * <h1>Pitch Graphic</h1>
 *
 * Pitch graphic is used to represent the pitch when in playback mode
 *
 * @author Oscar Mason
 *
 */
public class PitchGraphic extends Pane {

    private Circle centreCircle;
    private Rectangle pitchEdge;
    private Line halfwayLine;
    private Line centreSpot;
    private Rectangle leftPenaltyBox;
    private Rectangle rightPenaltyBox;
    private Rectangle leftGoalBox;
    private Rectangle rightGoalBox;
    private Arc topLeftCorner;
    private Arc topRightCorner;
    private Arc bottomLeftCorner;
    private Arc bottomRightCorner;
    private final int EDGE_DISTANCE = 10;

    public PitchGraphic(){
        StackPane pitchContainer = new StackPane();
        pitchContainer.prefWidthProperty().bind(widthProperty());
        pitchContainer.prefHeightProperty().bind(heightProperty());

        getChildren().add(pitchContainer);
        setBackground(new Background(new BackgroundFill(Color.rgb(0, 170, 50), CornerRadii.EMPTY, Insets.EMPTY)));

        centreCircle = new Circle(100);
        pitchContainer.getChildren().add(centreCircle);

        pitchEdge = new Rectangle();
        pitchContainer.getChildren().add(pitchEdge);

        halfwayLine = new Line(0, 0, 0, 20);
        pitchContainer.getChildren().add(halfwayLine);

        centreSpot = new Line();
        pitchContainer.getChildren().add(centreSpot);

        leftPenaltyBox = new Rectangle();
        pitchContainer.getChildren().add(leftPenaltyBox);
        pitchContainer.setAlignment(leftPenaltyBox, Pos.CENTER_LEFT);
        leftPenaltyBox.setTranslateX(EDGE_DISTANCE / 2);

        rightPenaltyBox = new Rectangle();
        pitchContainer.getChildren().add(rightPenaltyBox);
        pitchContainer.setAlignment(rightPenaltyBox, Pos.CENTER_RIGHT);
        rightPenaltyBox.setTranslateX(-EDGE_DISTANCE / 2);

        rightGoalBox = new Rectangle();
        pitchContainer.getChildren().add(rightGoalBox);
        pitchContainer.setAlignment(rightGoalBox, Pos.CENTER_RIGHT);
        rightGoalBox.setTranslateX(-EDGE_DISTANCE / 2);

        leftGoalBox = new Rectangle();
        pitchContainer.getChildren().add(leftGoalBox);
        pitchContainer.setAlignment(leftGoalBox, Pos.CENTER_LEFT);
        leftGoalBox.setTranslateX(EDGE_DISTANCE / 2);

        topLeftCorner = new Arc();
        pitchContainer.getChildren().add(topLeftCorner);
        topLeftCorner.setStartAngle(270);
        topLeftCorner.setLength(90);

        topRightCorner = new Arc();
        pitchContainer.getChildren().add(topRightCorner);
        topRightCorner.setStartAngle(180);
        topRightCorner.setLength(90);

        bottomLeftCorner = new Arc();
        pitchContainer.getChildren().add(bottomLeftCorner);
        bottomLeftCorner.setStartAngle(0);
        bottomLeftCorner.setLength(90);

        bottomRightCorner = new Arc();
        pitchContainer.getChildren().add(bottomRightCorner);
        bottomRightCorner.setStartAngle(90);
        bottomRightCorner.setLength(90);

        drawCorner(topLeftCorner, pitchContainer, Pos.TOP_LEFT, EDGE_DISTANCE / 2, EDGE_DISTANCE / 2);
        drawCorner(topRightCorner, pitchContainer, Pos.TOP_RIGHT, -EDGE_DISTANCE / 2, EDGE_DISTANCE / 2);
        drawCorner(bottomLeftCorner, pitchContainer, Pos.BOTTOM_LEFT, EDGE_DISTANCE / 2, -EDGE_DISTANCE / 2);
        drawCorner(bottomRightCorner, pitchContainer, Pos.BOTTOM_RIGHT, -EDGE_DISTANCE / 2, -EDGE_DISTANCE / 2);

        for(Node node : pitchContainer.getChildren()){
            if (node instanceof Shape){
                Shape shape = (Shape) node;
                shape.setFill(Color.TRANSPARENT);
                shape.setStroke(Color.WHITE);
                shape.setStrokeWidth(2);
            }
        }

        centreSpot.setStrokeWidth(6);

        bindMarkings();
    }

    public void drawCorner(Arc corner, StackPane pitchContainer, Pos pos, double x, double y){
        pitchContainer.setAlignment(corner, pos);
        corner.setTranslateX(x);
        corner.setTranslateY(y);
    }

    private void bindMarkings(){
        centreCircle.radiusProperty().bind(heightProperty().multiply(0.15));

        pitchEdge.widthProperty().bind(widthProperty().subtract(EDGE_DISTANCE));
        pitchEdge.heightProperty().bind(heightProperty().subtract(EDGE_DISTANCE));

        halfwayLine.endYProperty().bind(heightProperty().subtract(EDGE_DISTANCE));

        leftPenaltyBox.heightProperty().bind(heightProperty().multiply(0.5));
        leftPenaltyBox.widthProperty().bind(widthProperty().multiply(0.12));

        rightPenaltyBox.heightProperty().bind(heightProperty().multiply(0.5));
        rightPenaltyBox.widthProperty().bind(widthProperty().multiply(0.12));

        leftGoalBox.heightProperty().bind(leftPenaltyBox.heightProperty().multiply(0.5));
        leftGoalBox.widthProperty().bind((leftPenaltyBox.widthProperty().multiply(0.5)));

        rightGoalBox.heightProperty().bind(rightPenaltyBox.heightProperty().multiply(0.5));
        rightGoalBox.widthProperty().bind((rightPenaltyBox.widthProperty().multiply(0.5)));

        topLeftCorner.radiusXProperty().bind(widthProperty().multiply(0.02));
        topLeftCorner.radiusYProperty().bind(topLeftCorner.radiusXProperty());

        topRightCorner.radiusXProperty().bind(widthProperty().multiply(0.02));
        topRightCorner.radiusYProperty().bind(topRightCorner.radiusXProperty());

        bottomLeftCorner.radiusXProperty().bind(widthProperty().multiply(0.02));
        bottomLeftCorner.radiusYProperty().bind(bottomLeftCorner.radiusXProperty());

        bottomRightCorner.radiusXProperty().bind(widthProperty().multiply(0.02));
        bottomRightCorner.radiusYProperty().bind(bottomRightCorner.radiusXProperty());

    }
}
