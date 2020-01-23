/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import static paint.PaintClass.canvasSnapshot;
import static paint.PaintClass.updateServer;

/**
 *
 * @author zdude
 */
public class CanvasEventHandlers {
    
    /**
     * Apply each of the different event handlers to a canvas
     * @param canvas - Canvas to modify
     */
    public static void initializeEventHandlers(Canvas canvas){
        mouseMoved(canvas);
        mouseClicked(canvas);
        mousePressed(canvas);
        mouseDragged(canvas);
        mouseReleased(canvas);
        mouseEntered(canvas);
        mouseExited(canvas);
    }
    
    /**
     * Handle event for mouse movement. Specifically doing something if the dropper
     * tool is selected. When using dropper tool, moving across the canvas should
     * update the color pickers with the color being hovered over
     * @param canvas - Canvas being hovered over
     */
    public static void mouseMoved(Canvas canvas){
        // Handle event on mouse moved over canvas
        if(canvas.isVisible()){
            canvas.setOnMouseMoved((MouseEvent t) ->{
                if(canvas.equals(PaintClass.getSelectedCanvas())){
                    // When the dropper tool is in use
                    if(PaintClass.getDropperSelected()){
                        WritableImage wi = PaintClass.canvasSnapshot(PaintClass.getSelectedCanvas()); // Create a WritableImage containing the canvas contents

                        Color picked = wi.getPixelReader().getColor((int)t.getX(), (int)t.getY()); // Store the color of the pixel under the cursor

                        // Set the color picker values to that of the pixel that is currently being hovered over
                        PaintClass.getStrokePicker().setValue(picked);
                        PaintClass.getFillPicker().setValue(picked);

                        // Set the stroke and fill colors to the selected value
                        canvas.getGraphicsContext2D().setStroke(picked);
                        canvas.getGraphicsContext2D().setFill(picked);
                    }
                }
            });
        }
    }
    
    /**
     * Handle event on mouse click. Mostly just for the dropper tool, because
     * when you click with the dropper, it should change the selected color.
     * @param canvas - Canvas to pick color of
     */
    public static void mouseClicked(Canvas canvas){
        // Handle event on mouse click on canvas
        if(canvas.isVisible()){
            canvas.setOnMouseClicked((MouseEvent t) ->{
                if(canvas.equals(PaintClass.getSelectedCanvas())){
                    // When the dropper tool is in use
                    if(PaintClass.getDropperSelected()){
                        WritableImage wi = canvasSnapshot(canvas); // Create a WritableImage containing the canvas contents

                        Color picked = wi.getPixelReader().getColor((int)t.getX(), (int)t.getY()); // Store the color of the pixel under the cursor

                        // Set the color picker values to that of the pixel that was clicked and add it to the custom colors
                        PaintClass.getStrokePicker().setValue(picked);
                        PaintClass.getStrokePicker().getCustomColors().add(picked);
                        PaintClass.getFillPicker().setValue(picked);
                        PaintClass.getFillPicker().getCustomColors().add(picked);

                        // Set the stroke and fill colors to the selected value
                        canvas.getGraphicsContext2D().setStroke(picked);
                        canvas.getGraphicsContext2D().setFill(picked);

                        PaintClass.getDropperButton().fire(); // Turn off the dropper tool
                        PaintClass.getScene().setCursor(Cursor.CROSSHAIR); // Set cursor back to Crosshair, since mouse is currently hovering over canvas
                    }
                }else{
                    // If this canvas is not the one being interacted with, send
                    // the event to the selected canvas.
                    PaintClass.getSelectedCanvas().fireEvent(t);
                }
            });
        }
    }
    
    /**
     * Handle event on mouse press. Mostly making sure that freeform-like tools
     * place dots when just pressed. Also fires fill and places a point for the
     * custom polygon tool.
     * @param canvas - Canvas to draw on
     */
    public static void mousePressed(Canvas canvas){
        // Handle mouse press by taking location where pressed and storing for use on release
        if(canvas.isVisible()){
            canvas.setOnMousePressed((MouseEvent t) -> {
                if(canvas.equals(PaintClass.getSelectedCanvas())){
                    if(!PaintClass.getDropperSelected()){
                        double xClickPoint = t.getX(); // Store current mouse X location
                        double yClickPoint = t.getY(); // Store current mouse Y location
                        PaintClass.setXClickPoint(xClickPoint);
                        PaintClass.setYClickPoint(yClickPoint);
                        if(!PaintClass.getShape().equals("None")){ // Only continue if a shape is selected
                            WritableImage wi = PaintClass.canvasSnapshot(canvas); // Create new WritableImage containing the contents of the canvas

                            switch(PaintClass.getShape()){
                                case "Eraser":
                                    // Ensure erasure when user presses mouse, instead of just moving
                                    MouseEvents.erase(canvas, xClickPoint, yClickPoint, t);
                                    break;
                                case "Freeform":
                                    // Recolor freeform images
                                    MouseEvents.colorBrushTypes(canvas, PaintClass.getStrokeTypePicker().getSelectionModel().getSelectedItem().toString());
                                    break;
                                case "Custom Polygon":
                                    // Add the point where user clicks to arraylists of x and y coordinates for polygon drawing
                                    PaintClass.addPolygonPoint(xClickPoint, yClickPoint);

                                    // Changing line dashes fixes bug where if dotted lines were selected, points would not be drawn
                                    double[] dotWidth = canvas.getGraphicsContext2D().getLineDashes();
                                    canvas.getGraphicsContext2D().setLineDashes(0); // Remove dashes so that point can be drawn
                                    MouseEvents.drawLine(canvas, xClickPoint, yClickPoint, t);
                                    canvas.getGraphicsContext2D().setLineDashes(dotWidth); // Restore previous selection of dashes
                                    break;
                                case "Select":
                                    try{
                                        PaintClass.getDeselectButton().fire(); // Deselect previous selection
                                    }catch(NullPointerException e){

                                    }
                                    break;
                                case "Fill":
                                    MouseEvents.fill(wi, t, canvas); // Call the fill method to fill the clicked area
                                    break;
                            }
                        }
                        // Update the server
                        try {
                            updateServer();
                        } catch (IOException | NullPointerException ex) {
                            Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }else{
                    // If this canvas is not the one being interacted with, send
                    // the event to the selected canvas.
                    PaintClass.getSelectedCanvas().fireEvent(t);
                }
            });
        }
    }
    
    /**
     * Handle event on mouse drag. Mostly just creating/updating previews for
     * shapes to be drawn. 
     * @param canvas - Canvas to take graphics context information from. (What is
     * the fill color, stroke color, etc.)
     */
    public static void mouseDragged(Canvas canvas){
        // Handle mouse dragging over canvas. Drawing shape previews
        if(canvas.isVisible()){
            canvas.setOnMouseDragged((MouseEvent t) -> {
                if(canvas.equals(PaintClass.getSelectedCanvas())){
                    if(!PaintClass.getDropperSelected() && !PaintClass.getShape().equals("Fill")){ // Ignore drag if dropper is selected and if fill tool is in use. Do not draw selected shape
                        PaintClass.getScene().setCursor(Cursor.CROSSHAIR); // Ensure that while drawing shapes, cursor stays as crosshair
                        switch(PaintClass.getShape()){
                            case "Line":
                                // Draw and display shape preview
                                PaintClass.getCanvasPaneStatic().getChildren().remove(PaintClass.getTempLine());
                                PaintClass.setTempLine(MouseEvents.tempLine(canvas, PaintClass.getXClickPoint(), PaintClass.getYClickPoint(), t));
                                PaintClass.getCanvasPaneStatic().getChildren().add(PaintClass.getTempLine());
                                break;
                            case "Rectangle":
                                // Draw and display shape preview
                                PaintClass.getCanvasPaneStatic().getChildren().remove(PaintClass.getTempRect());
                                PaintClass.setTempRect(MouseEvents.tempRect(canvas, PaintClass.getXClickPoint(), PaintClass.getYClickPoint(), t, PaintClass.getFill(), PaintClass.getRounded(), PaintClass.getArcWidth(), PaintClass.getArcHeight()));
                                PaintClass.getCanvasPaneStatic().getChildren().add(PaintClass.getTempRect());
                                break;
                            case "Square":
                                // Draw and display shape preview
                                PaintClass.getCanvasPaneStatic().getChildren().remove(PaintClass.getTempSquare());
                                PaintClass.setTempSquare(MouseEvents.tempSquare(canvas, PaintClass.getXClickPoint(), PaintClass.getYClickPoint(), t, PaintClass.getFill(), PaintClass.getRounded(), PaintClass.getArcWidth(), PaintClass.getArcHeight()));
                                PaintClass.getCanvasPaneStatic().getChildren().add(PaintClass.getTempSquare());
                                break;
                            case "Ellipse":
                                // Draw and display shape preview
                                PaintClass.getCanvasPaneStatic().getChildren().remove(PaintClass.getTempEllipse());
                                PaintClass.setTempEllipse(MouseEvents.tempEllipse(canvas, PaintClass.getXClickPoint(), PaintClass.getYClickPoint(), t, PaintClass.getFill()));
                                PaintClass.getCanvasPaneStatic().getChildren().add(PaintClass.getTempEllipse());
                                break;
                            case "Circle":
                                // Draw and display shape preview
                                PaintClass.getCanvasPaneStatic().getChildren().remove(PaintClass.getTempCircle());
                                PaintClass.setTempCircle(MouseEvents.tempCircle(canvas, PaintClass.getXClickPoint(), PaintClass.getYClickPoint(), t, PaintClass.getFill()));
                                PaintClass.getCanvasPaneStatic().getChildren().add(PaintClass.getTempCircle());
                                break;
                            case "Freeform":
                                // Freeform drawing should not have square ends or be dotted. Change combo boxes as well so that they are not misleading
                                canvas.getGraphicsContext2D().setLineCap(StrokeLineCap.ROUND);
                                PaintClass.getLineCapSelector().getSelectionModel().select(0);
                                canvas.getGraphicsContext2D().setLineDashes(0);
                                PaintClass.getLineTypePicker().getSelectionModel().select(0);

                                // Allow user to draw freely. Not a preveiw
                                MouseEvents.drawBrush(canvas, PaintClass.getXClickPoint(), PaintClass.getYClickPoint(), t, PaintClass.getStrokeTypePicker().getSelectionModel().getSelectedItem().toString());
                                PaintClass.setXClickPoint(t.getX());
                                PaintClass.setYClickPoint(t.getY());
                                break;
                            case "Erase":
                                // Erasing should not have square ends or be dotted. Change combo boxes as well so that they are not misleading
                                canvas.getGraphicsContext2D().setLineCap(StrokeLineCap.ROUND);
                                PaintClass.getLineCapSelector().getSelectionModel().select(0);
                                canvas.getGraphicsContext2D().setLineDashes(0);
                                PaintClass.getLineTypePicker().getSelectionModel().select(0);

                                // Allow user to draw freely. Not a preveiw
                                MouseEvents.erase(canvas, PaintClass.getXClickPoint(), PaintClass.getYClickPoint(), t);
                                PaintClass.setXClickPoint(t.getX());
                                PaintClass.setYClickPoint(t.getY());
                                break;
                            case "Image":
                                // Display preview of image before user releases mouse
                                PaintClass.getCanvasPaneStatic().getChildren().remove(PaintClass.getTempImageView());
                                PaintClass.setTempImageView(MouseEvents.tempImage(PaintClass.getDrawFilePath(), PaintClass.getXClickPoint(), PaintClass.getYClickPoint(), t));
                                PaintClass.getCanvasPaneStatic().getChildren().add(PaintClass.getTempImageView());
                                break;
                            case "Text":
                                // Display preview of text before user releases mouse
                                PaintClass.getCanvasPaneStatic().getChildren().remove(PaintClass.getTempText());
                                PaintClass.setTempText(MouseEvents.tempText(canvas, t, PaintClass.getText(), (int) PaintClass.getFontBox().getSelectionModel().getSelectedItem()));
                                PaintClass.getCanvasPaneStatic().getChildren().add(PaintClass.getTempText());
                                break;
                            case "Select":
                                // Display preview of selection before user releases mouse
                                PaintClass.getCanvasPaneStatic().getChildren().remove(PaintClass.getSelectRect());
                                PaintClass.setSelectRect(MouseEvents.selectRect(canvas, PaintClass.getXClickPoint(), PaintClass.getYClickPoint(), t));
                                PaintClass.getCanvasPaneStatic().getChildren().add(PaintClass.getSelectRect());
                                break;
                            case "Regular Polygon":
                                // Draw and display shape preview
                                PaintClass.getCanvasPaneStatic().getChildren().remove(PaintClass.getTempPolygon());
                                PaintClass.setTempPolygon(MouseEvents.tempPolygon(canvas, Integer.parseInt(PaintClass.getRegPolygonSizeBox().getSelectionModel().getSelectedItem().toString()), PaintClass.getXClickPoint(), PaintClass.getYClickPoint(), t, PaintClass.getFill()));
                                PaintClass.getCanvasPaneStatic().getChildren().add(PaintClass.getTempPolygon());
                                break;
                        }
                    }
                }else{
                    // If this canvas is not the one being interacted with, send
                    // the event to the selected canvas.
                    PaintClass.getSelectedCanvas().fireEvent(t);
                }
            });
        }
    }
    
    /**
     * Handle event on mouse release. Draw shapes, remove previews.
     * @param canvas - Canvas to draw on
     */
    public static void mouseReleased(Canvas canvas){
        // Handle mouse release by drawing shape where necessary
        if(canvas.isVisible()){
            canvas.setOnMouseReleased((MouseEvent t) -> {
                if(canvas.equals(PaintClass.getSelectedCanvas())){
                    PaintClass.getCanvasPaneStatic().getChildren().removeAll(PaintClass.getTempLine(), PaintClass.getTempRect(), PaintClass.getTempEllipse(), PaintClass.getTempSquare(), PaintClass.getTempCircle(), PaintClass.getTempImageView(), PaintClass.getTempText(), PaintClass.getTempPolygon()); // Ensures that all temporary shapes have been removed
                    if(!PaintClass.getDropperSelected()){
                        if(!canvas.isHover()){
                            PaintClass.getScene().setCursor(Cursor.DEFAULT); // Restore cursor to default if not hovering over canvas
                        }

                        if(!PaintClass.getShape().equals("None")){ // Do nothing if no shape is selected
                            PaintClass.setSaved(false);
                            switch (PaintClass.getShape()) {
                                case "Image":
                                    // Draw image and set saved to false
                                    MouseEvents.drawImage(PaintClass.getDrawFilePath(), canvas, PaintClass.getXClickPoint(), PaintClass.getYClickPoint(), t); // Draw an image if image drawing is selected
                                    break;
                                case "Text":
                                    // Draw text and set saved to false
                                    MouseEvents.drawText(canvas, t, PaintClass.getText(), (int) PaintClass.getFontBox().getSelectionModel().getSelectedItem());
                                    break;
                                case "Regular Polygon":
                                    MouseEvents.drawPolygon(canvas, Integer.parseInt(PaintClass.getRegPolygonSizeBox().getSelectionModel().getSelectedItem().toString()), PaintClass.getXClickPoint(), PaintClass.getYClickPoint(), t, PaintClass.getFill());
                                    break;
                                case "Select":
                                    // If something was already selected, deselect and draw contents back onto canvas
                                    if(PaintClass.getCanvasPaneStatic().getChildren().contains(PaintClass.getSelectView())){
                                        canvas.getGraphicsContext2D().drawImage(PaintClass.getSelectView().getImage(), PaintClass.getSelectView().getX(), PaintClass.getSelectView().getY());
                                        PaintClass.getCanvasPaneStatic().getChildren().remove(PaintClass.getSelectView());
                                    }

                                    // Set image view position and contents to that of the drawn rectangle and add image view to canvasPaneStatic
                                    PaintClass.setSelectView(ImageManipulation.selectedImage(canvas, PaintClass.getSelectRect()));
                                    PaintClass.getSelectView().setX(PaintClass.getSelectRect().getX());
                                    PaintClass.getSelectView().setY(PaintClass.getSelectRect().getY());
                                    PaintClass.getCanvasPaneStatic().getChildren().add(PaintClass.getSelectView());

                                    // Handle mouse enter event on image veiw
                                    PaintClass.getSelectView().setOnMouseEntered((MouseEvent u) ->{
                                        PaintClass.getScene().setCursor(Cursor.MOVE); // Change to move cursor when mouse enters image view
                                    });

                                    // Handle mouse exit event on image view
                                    PaintClass.getSelectView().setOnMouseExited((MouseEvent u) ->{
                                        try{
                                            Thread.sleep(10); // Wait .01 seconds to ensure that user is not hovering over the selected rectangle
                                        }catch(InterruptedException ex){
                                            Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                        if(canvas.isHover()){
                                            PaintClass.getScene().setCursor(Cursor.CROSSHAIR); // Change cursor back to crosshair when switching from hovering over image view to hovering over canvas
                                        }else{
                                            PaintClass.getScene().setCursor(Cursor.DEFAULT); // Change cursor back to default when no longer hovering over canvas or image view
                                        }

                                    });

                                    // When user presses mouse on image view, store the distance from their click location to the location of the image view
                                    PaintClass.getSelectView().setOnMousePressed((MouseEvent u) ->{
                                        PaintClass.setXDist((int)u.getX() - (int)PaintClass.getSelectView().getX());
                                        PaintClass.setYDist((int)u.getY() - (int)PaintClass.getSelectView().getY());
                                    });

                                    // Handle drag event on image view
                                    PaintClass.getSelectView().setOnMouseDragged((MouseEvent u) ->{
                                        PaintClass.setSaved(false); // Only changes things if user drags selected portion of screen

                                        // Move selected image view and rectangle to current mouse location
                                        // Do not allow user to drag selection completely off canvas
                                        // Set new location to mouse location - distance from mouse to current location
                                        // This allows the user to drag from anywhere on the image view
                                        if(u.getX()-PaintClass.getXDist() < canvas.getWidth()){
                                            PaintClass.getSelectRect().setX(u.getX()-PaintClass.getXDist());
                                            PaintClass.getSelectView().setX(u.getX()-PaintClass.getXDist());
                                        }
                                        if(u.getY()-PaintClass.getYDist() < canvas.getHeight()){
                                            PaintClass.getSelectRect().setY(u.getY()-PaintClass.getYDist());
                                            PaintClass.getSelectView().setY(u.getY()-PaintClass.getYDist());
                                        }

                                    });

                                    Color originalFill = (Color) canvas.getGraphicsContext2D().getFill();
                                    if(canvas.equals(Layering.getCanvasList().get(0))){
                                        canvas.getGraphicsContext2D().setFill(Color.WHITE);
                                        canvas.getGraphicsContext2D().fillRect(PaintClass.getSelectRect().getX(), PaintClass.getSelectRect().getY(), PaintClass.getSelectRect().getWidth(), PaintClass.getSelectRect().getHeight());
                                        canvas.getGraphicsContext2D().setFill(originalFill);
                                    }else{
                                        canvas.getGraphicsContext2D().clearRect(PaintClass.getSelectRect().getX(), PaintClass.getSelectRect().getY(), PaintClass.getSelectRect().getWidth(), PaintClass.getSelectRect().getHeight());
                                    }

                                    PaintClass.setSaved(true);
                                    break;
                                default:
                                    MouseEvents.shape(PaintClass.getShape(), canvas, PaintClass.getXClickPoint(), PaintClass.getYClickPoint(), t, PaintClass.getFill(), PaintClass.getRounded(), PaintClass.getArcWidth(), PaintClass.getArcHeight(), PaintClass.getStrokeTypePicker().getSelectionModel().getSelectedItem().toString());
                                    break;
                            }
                            if(!PaintClass.getShape().equals("Select")){
                                // Update the server
                                try {
                                    updateServer();
                                } catch (IOException | NullPointerException ex) {
                                    Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }

                        }
                    }
                }else{
                    // If this canvas is not the one being interacted with, send
                    // the event to the selected canvas.
                    PaintClass.getSelectedCanvas().fireEvent(t);
                }
            });
        }
    }
    
    /**
     * Handle event on mouse enter. Change cursor to either crosshair or dropper
     * @param canvas - Canvas being entered.
     */
    public static void mouseEntered(Canvas canvas){
        // Change cursor to crosshair when over canvas
        canvas.setOnMouseEntered((MouseEvent t) ->{
            if(!PaintClass.getDropperSelected()){
                if(PaintClass.getShape().equals("Fill")){
                    PaintClass.getScene().setCursor(PaintClass.getFillCursor()); // If the fill tool is in use, set the cursor to the fill cursor
                }else{
                    PaintClass.getScene().setCursor(Cursor.CROSSHAIR); // If no cursor-changing tool is in use, set the cursor to crosshair
                }
            }else{
                PaintClass.getScene().setCursor(PaintClass.getDropperCursor()); // If the dropper tool is in use, set the cursor to the dropper cursor
            }
        });
    }
    
    /**
     * Handle event on mouse exit. Set cursor back to default.
     * @param canvas - Canvas mouse is exiting
     */
    public static void mouseExited(Canvas canvas){
        // Change cursor to default when no longer over canvas
        canvas.setOnMouseExited((MouseEvent t) ->{
            PaintClass.getScene().setCursor(Cursor.DEFAULT);
        });
    }
}
