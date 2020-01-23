/*
 * Class within which panes and other layouts are created
 */
package paint;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class PanesAndLayout {
    
    /**
     * Create the Main Pane
     * @return - GridPane
     */
    public GridPane mainPane(){
        GridPane pane = new GridPane();
        pane.setStyle("-fx-background-color: #e9e9e9;");
        pane.setMinSize(10, 10);
        pane.setPadding(new Insets(0, 0, 0, 0));
        pane.setAlignment(Pos.TOP_LEFT);
        return pane;
    }
    
    /**
     * Create a new GridPane with default values
     * @return - GridPane
     */
    public static GridPane newGridPane(){
        GridPane pane = new GridPane();
        pane.setMinSize(10, 10);
        pane.setPadding(new Insets(0, 0, 0, 0));
        pane.setAlignment(Pos.TOP_LEFT); 
        return pane;
    }
    
    /**
     * Create DropShadow object with offset and color
     * @return - DropShadow
     */
    public static DropShadow newShadow(){
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(5);
        shadow.setOffsetY(5);
        shadow.setColor(Color.GRAY);
        return shadow;
    }
}
