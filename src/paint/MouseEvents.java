/*
 * Class within which mouse events are handled
 */
package paint;

import java.util.ArrayList;
import java.util.Optional;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import java.util.Random;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MouseEvents {
    
    // Image and WritableImage for each SprayPaint image
    // Multiple images needed to add variety to spray paint. Removes obvious trail of drawing the same image over and over again
    private static Image sprayPaint = new Image("file:src/Images/SprayPaint.png");
    private static WritableImage wiSprayPaint = new WritableImage(sprayPaint.getPixelReader(), (int)sprayPaint.getWidth(), (int)sprayPaint.getHeight());
    private static Image sprayPaint2 = new Image("file:src/Images/SprayPaint2.png");
    private static WritableImage wiSprayPaint2 = new WritableImage(sprayPaint2.getPixelReader(), (int)sprayPaint2.getWidth(), (int)sprayPaint2.getHeight());
    private static Image sprayPaint3 = new Image("file:src/Images/SprayPaint3.png");
    private static WritableImage wiSprayPaint3 = new WritableImage(sprayPaint3.getPixelReader(), (int)sprayPaint3.getWidth(), (int)sprayPaint3.getHeight());
    
    private ImageManipulation iManipulation = new ImageManipulation();
    
    // Random number generator for picking an image
    private static Random rand = new Random();
    
    public Double calcWidth(double startX, double endX){
        return endX-startX;
    }
    
    public Double calcHeight(double startY, double endY){
        return endY-startY;
    }
    
    /**
     * Call proper method based on what shape is being made
     * @param shape - Type of shape to draw
     * @param canvas - Canvas
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is released
     * @param fill - Decide whether or not to fill the shape
     * @param rounded - Decide whether or not to round the rectangle
     * @param arcHeight - Arc Height of rounded rectangle
     * @param arcWidth - Arc Width of rounded rectangle
     * @param strokeType - Different stroke types for freeform tool
     * @return - Return false so that file is marked as not saved
     */
    public static Boolean shape(String shape, Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t, Boolean fill, Boolean rounded, double arcWidth, double arcHeight, String strokeType){
        switch(shape){
            case "Line":
                drawLine(canvas, xClickPoint, yClickPoint, t);
                break;
            case "Freeform":
                drawBrush(canvas, xClickPoint, yClickPoint, t, strokeType);
                break;
            case "Erase":
                erase(canvas, xClickPoint, yClickPoint, t);
                break;
            case "Rectangle":
                drawRectangle(canvas, xClickPoint, yClickPoint, t, fill, rounded, arcWidth, arcHeight);
                break;
            case "Square":
                drawSquare(canvas, xClickPoint, yClickPoint, t, fill, rounded, arcWidth, arcHeight);
                break;
            case "Ellipse":
                drawEllipse(canvas, xClickPoint, yClickPoint, t, fill);
                break;
            case "Circle":
                drawCircle(canvas, xClickPoint, yClickPoint, t, fill);
                break;
        }   
        return false;
    }
    
    /**
     * Draw a rectangle to show selected area of screen
     * @param canvas - Canvas
     * @param xClickPoint - x coordinate of click point
     * @param yClickPoint - y coordinate of click point
     * @param t - MouseEvent to find current coordinates of mouse
     * @return - Rectangle to display
     */
    public static Rectangle selectRect(Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t){
        Rectangle rect;
        double x, y, width, height;
        // Different arguments needed based on what direction the rectangle is drawn in
        if(t.getX() < xClickPoint && t.getY() < yClickPoint){ // If user drags up and to the left
            x = t.getX();
            y = t.getY();
            
            // Stop user from dragging rectangle off the canvas
            if(x < 0){
                x = 0;
                width = Math.abs(xClickPoint-x);
            }else{
                width = Math.abs(xClickPoint-t.getX());
            }
            if(y < 0){
                y = 0;
                height = Math.abs(yClickPoint-y);
            }else{
                height = Math.abs(yClickPoint-t.getY());
            }
            
        }else if(t.getX() < xClickPoint && t.getY() > yClickPoint){ // If user drags down and to the left
            x = t.getX();
            y = yClickPoint;
            height = Math.abs(t.getY()-yClickPoint);
            
            // Stop user from dragging rectangle off the canvas
            if(x < 0){
                x = 0;
                width = Math.abs(xClickPoint-x);
            }else{
                width = Math.abs(xClickPoint-t.getX());
            }
            
            if(y+height > canvas.getHeight()){
                height = Math.abs(canvas.getHeight()-y);
            }
        }else if(t.getX() > xClickPoint && t.getY() < yClickPoint){ // If user drags up and to the right
            x = xClickPoint;
            y = t.getY();
            width = Math.abs(t.getX()-xClickPoint);
            
            // Stop user from dragging rectangle off the canvas
            if(x+width > canvas.getWidth()){
                width = Math.abs(canvas.getWidth()-x);
            }
            
            if(y < 0){
                y = 0;
                height = Math.abs(yClickPoint-y);
            }else{
                height = Math.abs(yClickPoint-t.getY());
            }
        }else{ // If user drags down and to the right
            x = xClickPoint;
            y = yClickPoint;
            width = Math.abs(t.getX()-xClickPoint);
            height = Math.abs(t.getY()-yClickPoint);
            
            // Stop user from dragging rectangle off the canvas
            if(x+width > canvas.getWidth()){
                width = Math.abs(canvas.getWidth()-x);
            }
            
            if(y+height > canvas.getHeight()){
                height = Math.abs(canvas.getHeight()-y);
            }
        }
        
        rect = new Rectangle(x, y, width, height);
        
        rect.setStrokeWidth(2); // Thin stroke
        rect.setStroke(Color.LIGHTGRAY); // Light gray color
        rect.setFill(null);
        rect.getStrokeDashArray().addAll(5d); // Some small dashes
        return rect;
    }
    
    /**
     * Draw text on canvas
     * @param canvas - Canvas
     * @param t - MouseEvent to find coordinates of mouse
     * @param text - Text to be displayed
     * @param fontSize - Font size for text to be displayed with
     */
    public static void drawText(Canvas canvas, MouseEvent t, String text, int fontSize){
        canvas.getGraphicsContext2D().setFont(Font.font(fontSize));
        canvas.getGraphicsContext2D().fillText(text, t.getX(), t.getY());
    }
    
    /**
     * Create a temporary text object to display while user dragging with text tool
     * @param canvas - Canvas
     * @param t - MouseEvent to find coordinates of mouse
     * @param text - Text to be displayed
     * @param fontSize - Font size for text to be displayed with
     * @return - Text object
     */
    public static Text tempText(Canvas canvas, MouseEvent t, String text, int fontSize){
        Text newText = new Text(t.getX(), t.getY(), text);
        newText.setFont(Font.font(fontSize));
        newText.setFill(canvas.getGraphicsContext2D().getFill());
        return newText;
    }
    
    /**
     * Create a dialog box allowing user to input a string and return their answer
     * @param text - Original text to display when initially opened
     * @return - String that user entered
     */
    public Optional<String> setText(String text){
        // Creating dialog box for input of new canvas dimensions
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Choose Text");
        dialog.setHeaderText("Enter the text for the text drawing tool");
        
        // Creating button to exit
        ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);
        
        // Allow user to enter custom width
        TextField textField = new TextField();
        textField.setText(text);
        Label textLabel = new Label("Text: ");
        
        // GridPane holding content for dialog
        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setPadding(new Insets(20, 150, 10, 10));

        pane.add(textLabel, 0, 0);
        pane.add(textField, 1, 0);

        dialog.getDialogPane().setContent(pane);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return (textField.getText());
            }
            return text;
        });
        
        // Set canvas dimensions to user-provided dimensions 
        Optional<String> newText = dialog.showAndWait();
        return newText;
    }
    
    /**
     * Recolor the non-transparent pixels in SprayPaint images to the same color as the stroke
     * @param canvas - Canvas
     * @param strokeType - Type of stroke to use
     */
    public static void colorBrushTypes(Canvas canvas, String strokeType){
        switch (strokeType){
            case "Spray Paint":
                // Recolor all 3 different SprayPaint images
                recolorLoops(canvas, wiSprayPaint);
                recolorLoops(canvas, wiSprayPaint2);
                recolorLoops(canvas, wiSprayPaint3);
                break;
        }
    }
    
    /**
     * Loops that will recolor images to match the stroke color. Specifically for different freeform stroke images
     * @param canvas - canvas
     * @param wi - WritableImage to change colors of
     */
    public static void recolorLoops(Canvas canvas, WritableImage wi){
        // If the pixel at a given location is not transparent, recolor it to match stroke color
        PixelReader reader = wi.getPixelReader();
        PixelWriter writer = wi.getPixelWriter();
        for (int i = 0; i < wi.getWidth(); i++) {
            for (int j = 0; j < wi.getHeight(); j++) {
                if(reader.getArgb(i, j) != 0){
                    writer.setColor(i, j, (Color) canvas.getGraphicsContext2D().getStroke());
                }
            }
        }
    }
    
    /**
     * Draw Line from mouse pressed point to mouse released point
     * @param canvas - Canvas
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is released
     */
    public static void drawLine(Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t){
        canvas.getGraphicsContext2D().strokeLine(xClickPoint, yClickPoint, t.getX(), t.getY());
    }
    
    /**
     * Draw Freely with various different stroke types
     * @param canvas - Canvas 
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is currently
     * @param strokeType - Type of stroke to use
     */
    public static void drawBrush(Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t, String strokeType){
        switch (strokeType) {
            case "Spray Paint":
                int choice = rand.nextInt(3);
                // Draw different images because it removes the repetition of having the same image drawn over and over again
                switch (choice) {
                    case 0:
                        canvas.getGraphicsContext2D().drawImage(wiSprayPaint, xClickPoint-canvas.getGraphicsContext2D().getLineWidth()/2, yClickPoint-canvas.getGraphicsContext2D().getLineWidth()/2, canvas.getGraphicsContext2D().getLineWidth(), canvas.getGraphicsContext2D().getLineWidth());
                        break;
                    case 1:
                        canvas.getGraphicsContext2D().drawImage(wiSprayPaint2, xClickPoint-canvas.getGraphicsContext2D().getLineWidth()/2, yClickPoint-canvas.getGraphicsContext2D().getLineWidth()/2, canvas.getGraphicsContext2D().getLineWidth(), canvas.getGraphicsContext2D().getLineWidth());
                        break;
                    default:
                        canvas.getGraphicsContext2D().drawImage(wiSprayPaint3, xClickPoint-canvas.getGraphicsContext2D().getLineWidth()/2, yClickPoint-canvas.getGraphicsContext2D().getLineWidth()/2, canvas.getGraphicsContext2D().getLineWidth(), canvas.getGraphicsContext2D().getLineWidth());
                        break;
                }   break;
            case "Air Brush":
                String original = canvas.getGraphicsContext2D().getStroke().toString().substring(2); // Save the original color to reapply
                canvas.getGraphicsContext2D().setStroke(Color.web(original.substring(0, 6) + "0d")); // Set the stroke to the same color, but more transparent
                drawLine(canvas, xClickPoint, yClickPoint, t); // Draw line with newly transparent color
                canvas.getGraphicsContext2D().setStroke(Color.web(original)); // Restore the color from before
                break;
            default:
                drawLine(canvas, xClickPoint, yClickPoint, t);
                break;
        }
    }
    
    /**
     * Draw white from mouse pressed point to mouse released point and restore 
     * @param canvas - Canvas
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is released
     */
    public static void erase(Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t){
        javafx.scene.paint.Paint original = canvas.getGraphicsContext2D().getStroke(); // Save original paint
        canvas.getGraphicsContext2D().setStroke(Color.WHITE);
        canvas.getGraphicsContext2D().strokeLine(xClickPoint, yClickPoint, t.getX(), t.getY());
        canvas.getGraphicsContext2D().setStroke(original); // Restore original paint
    }
    
    /**
     * Draw Rectangle from mouse pressed point to mouse released point
     * @param canvas - Canvas
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is released
     * @param fill - Decide whether or not to fill the Rectangle
     * @param rounded - Decide whether or not to round the rectangle
     * @param arcHeight - Arc Height of rounded rectangle provided by user through ComboBox
     * @param arcWidth - Arc Width of rounded rectangle provided by user through ComboBox
     */
    public static void drawRectangle(Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t, Boolean fill, Boolean rounded, double arcWidth, double arcHeight){
        double x, y, width, height;
        // Different arguments needed based on what direction the rectangle is drawn in
        if(t.getX() < xClickPoint && t.getY() < yClickPoint){ // If user draws up and to the left
            x = t.getX();
            y = t.getY();
            width = Math.abs(xClickPoint-t.getX());
            height = Math.abs(yClickPoint-t.getY());
        }else if(t.getX() < xClickPoint && t.getY() > yClickPoint){ // If user draws down and to the left
            x = t.getX();
            y = yClickPoint;
            width = Math.abs(xClickPoint-t.getX());
            height = Math.abs(t.getY()-yClickPoint);
        }else if(t.getX() > xClickPoint && t.getY() < yClickPoint){ // If user draws up and to the right
            x = xClickPoint;
            y = t.getY();
            width = Math.abs(t.getX()-xClickPoint);
            height = Math.abs(yClickPoint-t.getY());
        }else{ // If user draws down and to the right
            x = xClickPoint;
            y = yClickPoint;
            width = Math.abs(t.getX()-xClickPoint);
            height = Math.abs(t.getY()-yClickPoint);
        }
        
        // Checking if rectangle should be filled and/or rounded
        if(!rounded){
            if(fill){
                canvas.getGraphicsContext2D().fillRect(x, y, width, height);
            }
            canvas.getGraphicsContext2D().strokeRect(x, y, width, height);
        }else{
            if(fill){
                canvas.getGraphicsContext2D().fillRoundRect(x, y, width, height, arcWidth, arcHeight);
            }
            canvas.getGraphicsContext2D().strokeRoundRect(x, y, width, height, arcWidth, arcHeight);
        }
    }
    
    /**
     * Draw Square from mouse pressed point to mouse released point
     * @param canvas - Canvas
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is released
     * @param fill - Decide whether or not to fill the square
     * @param rounded - Decide whether or not to round the square
     * @param arcHeight - Arc Height of rounded square provided by user through ComboBox
     * @param arcWidth - Arc Width of rounded square provided by user through ComboBox
     */
    public static void drawSquare(Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t, Boolean fill, Boolean rounded, double arcWidth, double arcHeight){
        double x, y, width, height;
        // Change start location based on drag direction
        if(t.getX() < xClickPoint){
            if(Math.abs(xClickPoint-t.getX()) > Math.abs(yClickPoint-t.getY())){
                x = t.getX();
            }else{
                x = (xClickPoint-Math.abs(yClickPoint-t.getY()));
            }
        }else{
            x = xClickPoint;
        }
        if(t.getY() < yClickPoint){
            if(Math.abs(yClickPoint-t.getY()) > Math.abs(xClickPoint-t.getX())){
                y = t.getY();
            }else{
                y = (yClickPoint-Math.abs(xClickPoint-t.getX()));
            }
        }else{
            y = yClickPoint;
        }
        
        // Set width and height
        if(Math.abs(xClickPoint-t.getX()) > Math.abs(yClickPoint-t.getY())){
            width = Math.abs(t.getX()-xClickPoint);
            height = width;
        }else{
            width = Math.abs(t.getY()-yClickPoint);
            height = width;
        }
        
        // Checking if square should be filled and/or rounded
        if(!rounded){
            if(fill){
                canvas.getGraphicsContext2D().fillRect(x, y, width, height);
            }
            canvas.getGraphicsContext2D().strokeRect(x, y, width, height);
        }else{
            if(fill){
                canvas.getGraphicsContext2D().fillRoundRect(x, y, width, height, arcWidth, arcHeight);
            }
            canvas.getGraphicsContext2D().strokeRoundRect(x, y, width, height, arcWidth, arcHeight);
        }
    }
    
    /**
     * Draw an Ellipse from mouse pressed point to mouse released point
     * @param canvas - Canvas
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is released
     * @param fill - Decide whether or not to fill the Ellipse
     */
    public static void drawEllipse(Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t, Boolean fill){
        double x, y, width, height;
        // Different arguments needed based on what direction the Ellipse is drawn in
        if(t.getX() < xClickPoint && t.getY() < yClickPoint){ // If user drags up and to the left
            x = t.getX();
            y = t.getY();
            width = Math.abs(xClickPoint-t.getX());
            height = Math.abs(yClickPoint-t.getY());
        }else if(t.getX() < xClickPoint && t.getY() > yClickPoint){ // If user drags down and to the left
            x = t.getX();
            y = yClickPoint;
            width = Math.abs(xClickPoint-t.getX());
            height = Math.abs(t.getY()-yClickPoint);
        }else if(t.getX() > xClickPoint && t.getY() < yClickPoint){ // If user drags up and to the right
            x = xClickPoint;
            y = t.getY();
            width = Math.abs(t.getX()-xClickPoint);
            height = Math.abs(yClickPoint-t.getY());
        }else{ // If user drags down and to the right
            x = xClickPoint;
            y = yClickPoint;
            width = Math.abs(t.getX()-xClickPoint);
            height = Math.abs(t.getY()-yClickPoint);
        }
        
        // Checking if ellipse should be filled
        if(fill){
            canvas.getGraphicsContext2D().fillOval(x, y, width, height);
        }
        canvas.getGraphicsContext2D().strokeOval(x, y, width, height);
    }
    
    /**
     * Draw a circle with a start point at the center and radius being mouse distance from center
     * @param canvas - Canvas
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is released
     * @param fill - Decide whether or not to fill the Circle
     */
    public static void drawCircle(Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t, Boolean fill){
        double width, height;
        
        width = Math.abs(xClickPoint-t.getX());
        height = width;
        
        // Checking if circle should be filled
        if(fill){
            canvas.getGraphicsContext2D().fillOval(xClickPoint-(width/2), yClickPoint-(width/2), width, height);
        }
        canvas.getGraphicsContext2D().strokeOval(xClickPoint-(width/2), yClickPoint-(width/2), width, height);
    }
    
    /**
     * Draw a regular polygon with a starting point at the user's mouse location and a user-defined number of sides
     * @param canvas - Canvas to draw polygon on
     * @param numPoints - Number of sides/vertices for the polygon
     * @param xClickPoint - X coordinate of the center of the polygon
     * @param yClickPoint - Y coordinate of the center of the polygon
     * @param t - MouseEvent
     * @param fill - Whether or not to fill the polygon
     */
    public static void drawPolygon(Canvas canvas, int numPoints, double xClickPoint, double yClickPoint, MouseEvent t, Boolean fill){
        // Arrays containing x and y coordinates for each of the points in the polygon
        double[] xPoints = new double[numPoints+1];
        double[] yPoints = new double[numPoints+1];
        
        // Set starting point to the current mouse location
        xPoints[0] = t.getX();
        yPoints[0] = t.getY();
        
        final double angleStep = Math.PI * 2 / numPoints; // Amount of radians to rotate after placing each point
        double radius = Math.sqrt(((t.getX()-xClickPoint)*(t.getX()-xClickPoint)) + ((t.getY()-yClickPoint)*(t.getY()-yClickPoint))); // Distance from current mouse point to mouse click point
        double angle = Math.atan2(t.getY()-yClickPoint, t.getX()-xClickPoint); // Starting angle. Allows user to rotate polygons as needed
        
        // Add x and y coordinates to their respective arrays
        for (int i = 0; i < numPoints+1; i++, angle += angleStep) {
            xPoints[i] = Math.cos(angle) * radius + xClickPoint;
            yPoints[i] = Math.sin(angle) * radius + yClickPoint;
        }
        
        // Fill polygon when necessary
        if(fill){
            canvas.getGraphicsContext2D().fillPolygon(xPoints, yPoints, numPoints+1);
        }
        
        // Stroke polygon
        canvas.getGraphicsContext2D().strokePolygon(xPoints, yPoints, numPoints+1); 
    }
    
    /**
     * Create temporary line to show user what theirs will look like when mouse released
     * @param canvas - Canvas
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is released
     * @return - Line to display
     */
    public static Line tempLine(Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t){
        Line line = new Line(xClickPoint, yClickPoint, t.getX(), t.getY());
        line.setStroke(canvas.getGraphicsContext2D().getStroke());
        line.setStrokeWidth(canvas.getGraphicsContext2D().getLineWidth());
        line.setStrokeLineCap(canvas.getGraphicsContext2D().getLineCap());
        return line;
    }
    
    /**
     * Create a temporary Rectangle to show user what theirs will look like when mouse released
     * @param canvas - Canvas
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is released
     * @param fill - Decide whether or not to fill the rectangle
     * @param rounded - Decide whether or not to round the rectangle
     * @param arcHeight - Arc Height of rounded rectangle
     * @param arcWidth - Arc Width of rounded rectangle
     * @return - Rectangle to display
     */
    public static Rectangle tempRect(Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t, Boolean fill, Boolean rounded, double arcWidth, double arcHeight){
        Rectangle rect;
        double x, y, width, height;
        // Different arguments needed based on what direction the rectangle is drawn in
        if(t.getX() < xClickPoint && t.getY() < yClickPoint){ // If user drags up and to the left
            x = t.getX();
            y = t.getY();
            width = Math.abs(xClickPoint-t.getX());
            height = Math.abs(yClickPoint-t.getY());
        }else if(t.getX() < xClickPoint && t.getY() > yClickPoint){ // If user drags down and to the left
            x = t.getX();
            y = yClickPoint;
            width = Math.abs(xClickPoint-t.getX());
            height = Math.abs(t.getY()-yClickPoint);
        }else if(t.getX() > xClickPoint && t.getY() < yClickPoint){ // If user drags up and to the right
            x = xClickPoint;
            y = t.getY();
            width = Math.abs(t.getX()-xClickPoint);
            height = Math.abs(yClickPoint-t.getY());
        }else{ // If user drags down and to the right
            x = xClickPoint;
            y = yClickPoint;
            width = Math.abs(t.getX()-xClickPoint);
            height = Math.abs(t.getY()-yClickPoint);
        }
        
        rect = new Rectangle(x, y, width, height);
        
        // Fill when necessary
        if(fill){
            rect.setFill(canvas.getGraphicsContext2D().getFill());
        }else{
            rect.setFill(null);
        }
        
        // Round corners when necessary
        if(rounded){
            rect.setArcWidth(arcWidth);
            rect.setArcHeight(arcHeight);
        }
        
        rect.setStroke(canvas.getGraphicsContext2D().getStroke());
        rect.setStrokeWidth(canvas.getGraphicsContext2D().getLineWidth());
        return rect;
    }
    
    /**
     * Draw Square from mouse pressed point to mouse released point
     * @param canvas - Canvas
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is released
     * @param fill - Decide whether or not to fill the square
     * @param rounded - Decide whether or not to round the square
     * @param arcHeight - Arc Height of rounded square
     * @param arcWidth - Arc Width of rounded square
     * @return - Square to display
     */
    public static Rectangle tempSquare(Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t, Boolean fill, Boolean rounded, double arcWidth, double arcHeight){
        Rectangle square;
        double x, y, width, height;
        // Change start location based on drag direction
        if(t.getX() < xClickPoint){
            if(Math.abs(xClickPoint-t.getX()) > Math.abs(yClickPoint-t.getY())){
                x = t.getX();
            }else{
                x = (xClickPoint-Math.abs(yClickPoint-t.getY()));
            }
        }else{
            x = xClickPoint;
        }
        if(t.getY() < yClickPoint){
            if(Math.abs(yClickPoint-t.getY()) > Math.abs(xClickPoint-t.getX())){
                y = t.getY();
            }else{
                y = (yClickPoint-Math.abs(xClickPoint-t.getX()));
            }
        }else{
            y = yClickPoint;
        }
        
        // Set width and height
        if(Math.abs(xClickPoint-t.getX()) > Math.abs(yClickPoint-t.getY())){
            width = Math.abs(t.getX()-xClickPoint);
            height = width;
        }else{
            width = Math.abs(t.getY()-yClickPoint);
            height = width;
        }

        square = new Rectangle(x, y, width, height);
        
        // Fill when necessary
        if(fill){
            square.setFill(canvas.getGraphicsContext2D().getFill());
        }else{
            square.setFill(null);
        }
                
        // Round corners when necessary
        if(rounded){
            square.setArcWidth(arcWidth);
            square.setArcHeight(arcHeight);
        }
        
        square.setStroke(canvas.getGraphicsContext2D().getStroke());
        square.setStrokeWidth(canvas.getGraphicsContext2D().getLineWidth());
        return square;
    }
    
    /**
     * Create a temporary Ellipse to show user what theirs will look like when mouse released
     * @param canvas - Canvas
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is released
     * @param fill - Decide whether or not to fill the temporary Ellipse
     * @return - Ellipse to display
     */
    public static Ellipse tempEllipse(Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t, Boolean fill){
        Ellipse e;
        double x, y, width, height;
        
        // Different arguments needed based on what direction the Ellipse is drawn in
        if(t.getX() < xClickPoint && t.getY() < yClickPoint){ // If user drags up and to the left
            x = t.getX();
            y = t.getY();
            width = Math.abs(xClickPoint-t.getX())/2;
            height = Math.abs(yClickPoint-t.getY())/2;
        }else if(t.getX() < xClickPoint && t.getY() > yClickPoint){ // If user drags down and to the left
            x = t.getX();
            y = yClickPoint;
            width = Math.abs(xClickPoint-t.getX())/2;
            height = Math.abs(t.getY()-yClickPoint)/2;
        }else if(t.getX() > xClickPoint && t.getY() < yClickPoint){ // If user drags up and to the right
            x = xClickPoint;
            y = t.getY();
            width = Math.abs(t.getX()-xClickPoint)/2;
            height = Math.abs(yClickPoint-t.getY())/2;
        }else{ // If user drags down and to the right
            x = xClickPoint;
            y = yClickPoint;
            width = Math.abs(t.getX()-xClickPoint)/2;
            height = Math.abs(t.getY()-yClickPoint)/2;
        }
        
        e = new Ellipse(x, y, width, height);
        
        e.setCenterX((e.getCenterX()+e.getRadiusX()));
        e.setCenterY((e.getCenterY()+e.getRadiusY()));
        
        // Fill when necessary
        if(fill){
            e.setFill(canvas.getGraphicsContext2D().getFill());
        }else{
            e.setFill(null);
        }
        
        e.setStroke(canvas.getGraphicsContext2D().getStroke());
        e.setStrokeWidth(canvas.getGraphicsContext2D().getLineWidth());
        
        return e;
    }
    
    /**
     * Create a temporary Circle to show user what theirs will look like when mouse released
     * @param canvas - Canvas
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is released
     * @param fill - Decide whether or not to fill the temporary Circle
     * @return - Circle to display
     */
    public static Ellipse tempCircle(Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t, Boolean fill){
        Ellipse circle;
        double width, height;
        
        width = Math.abs(xClickPoint-t.getX());
        height = width; // Height and width should be the same
        
        width = width/2;
        height = height/2;
        
        circle = new Ellipse(xClickPoint, yClickPoint, width, height);
        
        // Fill when necessary
        if(fill){
            circle.setFill(canvas.getGraphicsContext2D().getFill());
        }else{
            circle.setFill(null);
        }
        
        circle.setStroke(canvas.getGraphicsContext2D().getStroke());
        circle.setStrokeWidth(canvas.getGraphicsContext2D().getLineWidth());
        
        return circle;
    }
    
    /**
     * Draw a rectangular image from click point to release point
     * @param filePath - Path of image file to draw
     * @param canvas - canvas
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is released
     */
    public static void drawImage(String filePath, Canvas canvas, double xClickPoint, double yClickPoint, MouseEvent t){
        Image img = new Image("file:" + filePath);
        
        double x, y, width, height;
        // Different arguments needed based on what direction the rectangle is drawn in
        if(t.getX() < xClickPoint && t.getY() < yClickPoint){ // If user drags up and to the left
            x = t.getX();
            y = t.getY();
            width = Math.abs(xClickPoint-t.getX());
            height = Math.abs(yClickPoint-t.getY());
        }else if(t.getX() < xClickPoint && t.getY() > yClickPoint){ // If user drags down and to the left
            x = t.getX();
            y = yClickPoint;
            width = Math.abs(xClickPoint-t.getX());
            height = Math.abs(t.getY()-yClickPoint);
        }else if(t.getX() > xClickPoint && t.getY() < yClickPoint){ // If user drags up and to the right
            x = xClickPoint;
            y = t.getY();
            width = Math.abs(t.getX()-xClickPoint);
            height = Math.abs(yClickPoint-t.getY());
        }else{ // If user drags down and to the right
            x = xClickPoint;
            y = yClickPoint;
            width = Math.abs(t.getX()-xClickPoint);
            height = Math.abs(t.getY()-yClickPoint);
        }
        
        canvas.getGraphicsContext2D().drawImage(img, x, y, width, height);
    }
    
    /**
     * Draw a preview of a rectangular image from click point to release point
     * @param filePath - Path of image file to draw
     * @param xClickPoint - X value where mouse was pressed
     * @param yClickPoint - Y value where mouse was pressed
     * @param t - Event to find coordinates of where mouse is released
     * @return - Temporary image contained within ImageView
     */
    public static ImageView tempImage(String filePath, double xClickPoint, double yClickPoint, MouseEvent t){
        Image img;
        double x, y, width, height;
        // Different arguments needed based on what direction the rectangle is drawn in
        if(t.getX() < xClickPoint && t.getY() < yClickPoint){ // If user drags up and to the left
            x = t.getX();
            y = t.getY();
            width = Math.abs(xClickPoint-t.getX());
            height = Math.abs(yClickPoint-t.getY());
        }else if(t.getX() < xClickPoint && t.getY() > yClickPoint){ // If user drags down and to the left
            x = t.getX();
            y = yClickPoint;
            width = Math.abs(xClickPoint-t.getX());
            height = Math.abs(t.getY()-yClickPoint);
        }else if(t.getX() > xClickPoint && t.getY() < yClickPoint){ // If user drags up and to the right
            x = xClickPoint;
            y = t.getY();
            width = Math.abs(t.getX()-xClickPoint);
            height = Math.abs(yClickPoint-t.getY());
        }else{ // If user drags down and to the right
            x = xClickPoint;
            y = yClickPoint;
            width = Math.abs(t.getX()-xClickPoint);
            height = Math.abs(t.getY()-yClickPoint);
        }
        
        img = new Image("file:" + filePath, width, height, false, false);
        ImageView view = new ImageView(img);
        view.setLayoutX(x); // Set position to user-defined spot
        view.setLayoutY(y);
        return view;
    }
    
    /**
     * Create a temporary regular polygon to display while user is drawing a regular polygon
     * @param canvas - Canvas 
     * @param numPoints - Number of sides/vertices for the polygon
     * @param xClickPoint - X coordinate of the center of the polygon
     * @param yClickPoint - Y coordinate of the center of the polygon
     * @param t - MouseEvent
     * @param fill - Whether or not to fill the polygon
     * @return - Temporary polygon to display
     */
    public static Polygon tempPolygon(Canvas canvas, int numPoints, double xClickPoint, double yClickPoint, MouseEvent t, Boolean fill){
        Polygon tempPolygon = new Polygon();
        
        // Set starting point to the current mouse location
        tempPolygon.getPoints().addAll(t.getX(), t.getY());
        
        
        final double angleStep = Math.PI * 2 / numPoints; // Amount of radians to rotate after placing each point
        double radius = Math.sqrt(((t.getX()-xClickPoint)*(t.getX()-xClickPoint)) + ((t.getY()-yClickPoint)*(t.getY()-yClickPoint))); // Distance from current mouse point to mouse click point
        double angle = Math.atan2(t.getY()-yClickPoint, t.getX()-xClickPoint); // Starting angle. Allows user to rotate polygons as needed
        
        // Add x and y coordinates to their respective arrays
        for (int i = 0; i < numPoints; i++, angle += angleStep) {
            tempPolygon.getPoints().addAll(Math.cos(angle) * radius + xClickPoint, Math.sin(angle) * radius + yClickPoint);
        }
        
        // Keep stroke color and width consistent with the user's selection
        tempPolygon.setStroke(canvas.getGraphicsContext2D().getStroke());
        tempPolygon.setStrokeWidth(canvas.getGraphicsContext2D().getLineWidth());
        
        // Fill when necessary
        if(fill){
            tempPolygon.setFill(canvas.getGraphicsContext2D().getFill());
        }else{
            tempPolygon.setFill(null);
        }
        
        return tempPolygon;
    }
    
    /**
     * Fill a space when user clicks with the fill tool
     * @param image - Snapshot of canvas before fill
     * @param t - MouseEvent to keep track of original location of mouse click
     * @param canvas - Canvas to draw fill on
     */
    public static void fill(WritableImage image, MouseEvent t, Canvas canvas){
        ArrayList<String> list = new ArrayList<>(); // ArrayList that will contain coordinates of pixels that will be changed
        
        // Pixels are stored as strings in format: "x y" 
        
        list.add(t.getX() + " " + t.getY()); // Add click point to ArrayList
        int x, y;
        int originalColor = image.getPixelReader().getArgb((int)t.getX(), (int)t.getY()); // Store the Argb of the original color where user clicked
        
        for(int i = 0; i < list.size(); i++){ // Iterate through ArrayList of pixels
            
            x = (int)Double.parseDouble(list.get(i).split(" ")[0]); // Store x coordinate of next pixel in ArrayList in x variable
            y = (int)Double.parseDouble(list.get(i).split(" ")[1]); // Store y coordinate of next pixel in ArrayList in y variable
            
            for(int j = 0; j < 9; j++){
                // newX and newY are used to check the pixels surrounding the start point.
                // Start point is the pixel from the ArrayList that is currently being used
                int newX = x-1+j%3;
                int newY = y-1+j/3;
                
                // If statement prevents fill from flowing over the edge of the canvas and causing errors
                if((newX >= 0 && newY >= 0) && (newX < canvas.getWidth() && newY < canvas.getHeight())){
                    // Check pixel to see if it matches the color of the pixel at the original click point
                    if(image.getPixelReader().getArgb(newX, newY) == originalColor){
                        // Recolor the pixel in both the canvas and the WritableImage
                        // This prevents the recoloring of already colored pixels
                        canvas.getGraphicsContext2D().fillRect(newX, newY, 1, 1);
                        image.getPixelWriter().setArgb(newX, newY, originalColor-1);
                        
                        // Store newX and newY in ArrayList
                        list.add(newX + " " + newY);
                    }
                }
            }
        }
    }
}
