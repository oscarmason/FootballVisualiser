package football.visualiser.view;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * <h1>Seek Time</h1>
 * SeekBubble is a graphical object used for displaying the time/highlight the user has move the slider to
 * in the seek bar
 *
 * @author Oscar Mason
 */
public class SeekBubble extends StackPane {
    private Color fontColor = Color.WHITE;
    private String fontFamily = "Arial";
    private int fontSize = 14;
    private Text timeText;
    private Color backgroundColor = Color.rgb(40, 40, 40, 0.7);

    public SeekBubble(){
        setPrefHeight(30);
        setPrefWidth(50);
        setBackground(new Background(
                new BackgroundFill(backgroundColor, new CornerRadii(6), Insets.EMPTY)));

        timeText = new Text("00:00");

        timeText.setFill(fontColor);
        timeText.setFont(new Font(fontFamily, fontSize));
        setPadding(new Insets(0, 5, 0, 5));
        getChildren().add(timeText);
    }

    /**
     * Formats the text to display the current time from the given minutes and seconds
     * @param minutes   Number of minutes into the match
     * @param seconds   Number of seconds into the match
     */
    public void setTime(int minutes, int seconds){
        String minutesString = Integer.toString(minutes);
        String secondsString = Integer.toString(seconds);
        String time = ("00" + minutesString).substring(minutesString.length()) + ":"
                + ("00" + secondsString).substring(secondsString.length());
        timeText.setText(time);
    }

    public void setText(String text){
        timeText.setText(text);
    }
}
