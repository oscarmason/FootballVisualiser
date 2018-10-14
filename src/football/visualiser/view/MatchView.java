package football.visualiser.view;

import football.visualiser.SystemData.Team;
import football.visualiser.controllers.PitchController;
import football.visualiser.interfaces.IMatchView;
import football.visualiser.interfaces.ISeekbarListener;
import football.visualiser.models.entities.Player;
import football.visualiser.view.Highlight.HighlightType;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <h1>Match View</h1>
 * Match view is responsible for displaying all graphical objects when the user is in match playing mode
 *
 * @author Dovydas Ciomenas, Irene Zeng, Oscar Mason
 */
public class MatchView implements IMatchView {
    //MatchView
    private Scene scene;
    private AnchorPane rootPane;
    private VBox mainWindow;
    private AnchorPane mainContainer;
    private Stage stage;
    private BorderPane pitchSeekbarWrapper;
    private StackPane pitchStack;

    //Menu
    private Pane dropDown;
    private Pane menu;
    private Line scoreLine;
    private ImageView triangle;
    private Image triangleDown;
    private Image triangleUp;
    private Rectangle blueGrad, redGrad;

    volatile private Slider slider;
    private double pitchRatio;
    private FXMLLoader fxmlLoader;
    private Button playButton;
    private boolean menuExpanded = false;
    private boolean menuExpanding = false;
    private int res = Toolkit.getDefaultToolkit().getScreenResolution();
    private double sliderMenuHeight = res / 3;
    private double menuMinimisedHeight = res / 3;
    private double buttonDimension = res / 3;
    private double buttonImageHeight = res / 5;
    private PlayerCard playerCard;
    private DropDownOverview overviewPane;
    private Text redScore;
    private Text blueScore;
    private Image playImage;
    private Image pauseImage;
    private ImageView playButtonImageView = new ImageView();

    //Pitch
    private Pane pitchPane;
    private Pane pitchWrapperPane;
    private HashMap<Integer, MovableGraphic> entityGraphics = new HashMap<>();
    private FootballGraphic footballGraphic = new FootballGraphic();

    //Slider
    private SeekBubble seekBubble = new SeekBubble();
    private AnchorPane timeline;
    private List<Highlight> highlights = new ArrayList<>();


    public void setPitchRatio(double pitchRatio){
        this.pitchRatio = pitchRatio;
    }

    public MatchView(Stage stage) throws IOException{
        this.stage = stage;
        initialiseView();
    }

    /**
     * Initialises all graphic user objects to be displayed
     * @throws IOException  If the xml layout file cannot be loaded
     */
    @Override
    public void initialiseView() throws IOException {
        //Initialise the view
        fxmlLoader = new FXMLLoader(getClass().getResource("RootLayout.fxml"));
        Parent fxmlRoot = fxmlLoader.load();
        scene = new Scene(fxmlRoot);

        //Initialise Objects
        pitchPane = new PitchGraphic();
        playerCard = new PlayerCard();
        overviewPane = new DropDownOverview();
        blueGrad = new Rectangle(0, 0, 300, sliderMenuHeight);
        redGrad = new Rectangle(0, 0, 300, sliderMenuHeight);
        Stop[] stopsBlue = new Stop[] { new Stop(0, Color.rgb(42, 42, 52)), new Stop(1, Color.DODGERBLUE)};
        LinearGradient lgBlue = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stopsBlue);
        Stop[] stopsRed = new Stop[] { new Stop(0, Color.rgb(255,40,40)), new Stop(1, Color.rgb(42, 42, 52))};
        LinearGradient lgRed = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stopsRed);
        pitchPane = new PitchGraphic();
        playerCard = new PlayerCard();
        overviewPane = new DropDownOverview();

        //Lookup
        rootPane = (AnchorPane) scene.lookup("#root");
        pitchWrapperPane = (Pane) scene.lookup("#pitchWrapper");
        dropDown = (Pane) scene.lookup("#dropDown");
        menu = (Pane) scene.lookup("#menu");
        scoreLine = (Line) scene.lookup("#scoreLine");
        triangle = (ImageView) scene.lookup("#triangle");
        slider = (Slider) scene.lookup("#slider");
        timeline = (AnchorPane) scene.lookup("#timeline");
        mainWindow = (VBox) scene.lookup("#mainWindow");
        playButton = (Button) scene.lookup("#playButton");
        HBox gradCont = (HBox) scene.lookup("#gradContainer");
        VBox mainWindow = (VBox) scene.lookup("#mainWindow");
        redScore = (Text) scene.lookup("#redScore");
        blueScore = (Text) scene.lookup("#blueScore");
        mainContainer = (AnchorPane) scene.lookup("#mainContainer");
        pitchSeekbarWrapper = (BorderPane) scene.lookup("#pitchSeekbarWrapper");
        pitchStack = (StackPane) scene.lookup("#pitchStack");

        //Load Resources
        triangleDown = new Image(
                getClass().getClassLoader().getResource("images/triangledown.png").toExternalForm()
        );

        triangleUp = new Image(
                getClass().getClassLoader().getResource("images/triangleup.png").toExternalForm()
        );

        playImage = new Image(getClass().getClassLoader().getResource("images/playIcon.png").toExternalForm());
        pauseImage = new Image(getClass().getClassLoader().getResource("images/pauseIcon.png").toExternalForm());

        //Set Node Properties
        triangle.setImage(triangleDown);
        slider.setMin(0);
        slider.setMax(1);
        timeline.setMinHeight(10);
        dropDown.setMinHeight(0);

        mainWindow.setLayoutY(menuMinimisedHeight);

        menu.setMinHeight(sliderMenuHeight);
        slider.setMinHeight(sliderMenuHeight);
        mainWindow.setLayoutY(sliderMenuHeight);
        pitchSeekbarWrapper.setCache(true);
        pitchSeekbarWrapper.setCacheHint(CacheHint.SPEED);
        playButton = (Button) scene.lookup("#playButton");

        playButtonImageView.setImage(playImage);
        playButtonImageView.setPreserveRatio(true);
        playButtonImageView.setFitHeight(buttonImageHeight);
        playButton.setGraphic(playButtonImageView);
        playButton.setMinHeight(buttonDimension);
        playButton.setMaxHeight(buttonDimension);
        playButton.setMinWidth(buttonDimension);
        playButton.setMaxWidth(buttonDimension);


        pitchWrapperPane.getChildren().add(pitchPane);

        pitchPane.getChildren().add(footballGraphic);

        pitchPane.setPrefWidth(Screen.getPrimary().getBounds().getWidth() * 0.5);
        pitchPane.setPrefHeight(pitchPane.getPrefWidth() * 0.7);


        blueGrad.setFill(lgBlue);
        redGrad.setFill(lgRed);
        scoreLine.setEndY(sliderMenuHeight);

        gradCont.getChildren().addAll(blueGrad, redGrad);

        mainContainer.minHeightProperty().bind(pitchWrapperPane.heightProperty());
        mainContainer.minWidthProperty().bind(pitchWrapperPane.widthProperty());

        Stop[] stopsDark = new Stop[] { new Stop(0, Color.rgb(22,23,32)), new Stop(1, Color.rgb(52, 52, 62))};

        RadialGradient rg1 = new RadialGradient(0, 0, 0.5, 0.5, 0.95, true,
                CycleMethod.NO_CYCLE, stopsDark);

        Rectangle mainGrad = new Rectangle();

        mainGrad.setFill(rg1);

        pitchStack.getChildren().add(0, mainGrad);

        mainGrad.widthProperty().bind(mainContainer.widthProperty());
        mainGrad.heightProperty().bind(mainContainer.heightProperty());

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        stage.setMaxHeight(primaryScreenBounds.getHeight());
        stage.setMaxWidth(primaryScreenBounds.getWidth());
    }

    public void setEnterHandler(EventHandler<KeyEvent> eventHandler){
        scene.setOnKeyPressed(eventHandler);
    }

    /**
     * Creates a highlight graphic and adds it to the highlights list
     *
     * Author: Oscar Mason
     *
     * @param highlightType             Type of the highlight
     * @param seekbarRatio              Position in the seek bar to place the high light graphic
     * @param highlightMouseListener    Mouse listener for when the user hovers or clicks on a highlight
     */
    public void addHighlight(HighlightType highlightType, double seekbarRatio,
                             ISeekbarListener highlightMouseListener){
        highlights.add(new Highlight(highlightType, seekbarRatio, slider, highlightMouseListener));
    }

    public DropDownOverview getOverviewPane(){
        return overviewPane;
    }

    /**
     * Adds all the higlight graphics to the seekbar
     *
     * Author: Oscar Mason
     */
    public void showAllHighlights(){
        for(Highlight highlight : highlights){
            timeline.getChildren().add(highlight);
        }
    }

    /**
     * Displays the current time to where the user has dragged the seekbar slider to
     */
    public void showSeekTime(){
        if(!rootPane.getChildren().contains(seekBubble)){
            rootPane.getChildren().add(seekBubble);
        }
    }

    public void setPlayButtonImage(boolean isPlaying){
        playButtonImageView.setImage(isPlaying ? playImage : pauseImage);
        playButton.setGraphic(playButtonImageView);
    }

    public void setSeekTimePosition(MouseEvent event){
        Bounds bounds = slider.localToScene(slider.getBoundsInLocal());
        seekBubble.setTranslateX(event.getSceneX() - seekBubble.getPrefWidth() / 2);
        seekBubble.setTranslateY(bounds.getMinY() - seekBubble.getPrefHeight());
    }

    /**
     * Updates the text of the slider bubble to the relevant time in the match
     * Format of text is MM:SS
     *
     * Author: Oscar Mason
     *
     * @param seekTimeInMilliseconds    Current time in the match
     */
    public void setSeekTimeText(int seekTimeInMilliseconds){
        int toSeconds = seekTimeInMilliseconds / 1000;
        int minutes = toSeconds / 60;
        int seconds = toSeconds % 60;
        seekBubble.setTime(minutes, seconds);
    }

    public void setSeekTimeText(String text){
        seekBubble.setText(text);
    }

    public void removeSeekTime(){
        rootPane.getChildren().remove(seekBubble);
    }

    public Slider getSeekSlider(){
        return slider;
    }

    public boolean expandMenu(){
        if(!menuExpanding) {
            double startHeight;
            double finishHeight;

            if(menuExpanded){
                startHeight = dropDown.getHeight();
                finishHeight = 0;
                overviewPane.setSize(dropDown.getWidth(), 0);
                dropDown.getChildren().remove(overviewPane);
                triangle.setImage(triangleDown);
            }else{
                startHeight = 0;
                finishHeight = scene.getHeight() - sliderMenuHeight;
                triangle.setImage(triangleUp);
            }

            Transition transition = new ToolbarResizeAnimation(new Duration(500), startHeight, finishHeight);

            transition.setOnFinished(e -> {
                menuExpanding = false;
                pitchWrapperPane.setCacheHint(CacheHint.DEFAULT);

                if(!menuExpanded) {
                    if(!dropDown.getChildren().contains(overviewPane)) {
                        dropDown.getChildren().add(overviewPane);
                        //overviewPane.setSize(dropDown.getWidth(), dropDown.getHeight()-sliderMenuHeight);
                        overviewPane.prefHeightProperty().bind(pitchSeekbarWrapper.heightProperty());
                    }
                }
                menuExpanded = !menuExpanded;
            });


            menuExpanding = true;
            transition.playFromStart();
            removePlayerCard();
        }
        return menuExpanded;
    }

    public void bindComponentsToScene(){
        stage.minHeightProperty().bind(pitchWrapperPane.prefHeightProperty().add(sliderMenuHeight).add(menuMinimisedHeight).add(25));
        stage.minWidthProperty().bind(pitchWrapperPane.widthProperty());
        pitchWrapperPane.prefWidthProperty().bind(stage.widthProperty().multiply(0.7));
        pitchPane.prefWidthProperty().bind(pitchWrapperPane.prefWidthProperty());
        pitchPane.prefHeightProperty().bind(pitchPane.prefWidthProperty().multiply(pitchRatio));
        pitchWrapperPane.prefHeightProperty().bind(pitchPane.prefHeightProperty());
        timeline.minWidthProperty().bind(stage.widthProperty().subtract(playButton.widthProperty()));
        slider.prefWidthProperty().bind(timeline.minWidthProperty());
        overviewPane.prefWidthProperty().bind(scene.widthProperty());

    }

    public void updatePosition(int ID, int x, int y) {
        if(entityGraphics.containsKey(ID)){
            entityGraphics.get(ID).updateGraphicPosition(x, y);
        }
    }

    public Region getPitchPane(){
        return pitchPane;
    }

    @Override
    public void addPlayer(Team team, int ID, boolean isGoalKeeper, EventHandler<MouseEvent> mouseEventHandler) {
        PlayerGraphic playerGraphic = new PlayerGraphic(team, ID, isGoalKeeper);
        entityGraphics.put(ID, playerGraphic);
        playerGraphic.setSize(10);
        pitchWrapperPane.getChildren().add(playerGraphic);
        playerGraphic.setOnMouseClicked(mouseEventHandler);
    }

    @Override
    public void addFootball(int ID){
        entityGraphics.put(ID, footballGraphic);
        footballGraphic.setSize(10);
    }

    @Override
    public void updateGoalCount(int redNumberOfGoals, int blueNumberOfGoals) {
        Platform.runLater(() -> {
            redScore.setText(Integer.toString(redNumberOfGoals));
            blueScore.setText(Integer.toString(blueNumberOfGoals));
        });
    }

    public void moveSeekBarThumb(double position){
        Platform.runLater(() -> slider.setValue(position));
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    public PlayerCard getPlayerCard() {
        return playerCard;
    }

    public void showPlayerInfoPane(double x, double y, Player player) {
        if(!rootPane.getChildren().contains(playerCard.getPlayerCardNode())){
            rootPane.getChildren().add(playerCard.getPlayerCardNode());
        }
        playerCard.setTranslate(x, y);
        playerCard.setPlayerIDText(player.getID());
        playerCard.setPlayerTeamColor(player.getTeam());
        System.out.println("CARD");

        playerCard.drawHeapMap(player.getHeatMap().getHeatMap());
    }

    public void removePlayerCard() {
        if(rootPane.getChildren().contains(playerCard.getPlayerCardNode())){
            rootPane.getChildren().remove(playerCard.getPlayerCardNode());
        }
    }

    private class ToolbarResizeAnimation extends Transition{
        private double height;
        private double startHeight;
        private double heightDifference;
        private int i = 0;

        public ToolbarResizeAnimation(Duration duration, double startHeight, double height){
            setCycleDuration(duration);
            this.height = height;
            this.startHeight = startHeight;
            heightDifference = height - startHeight;
            pitchSeekbarWrapper.setCacheHint(CacheHint.SPEED);
        }

        @Override
        protected void interpolate(double frac) {
            dropDown.setMinHeight(startHeight + (heightDifference * frac));

        }
    }

    public PitchController getController(){
        return fxmlLoader.getController();
    }

}
