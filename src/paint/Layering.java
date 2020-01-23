/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;

import java.util.ArrayList;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;

/**
 *
 * @author zdude
 */
public class Layering {
    private static ArrayList<Canvas> canvasList = new ArrayList<>();
    
    /**
     * Create a new canvas based on information from an existing one. Keeps
     * things consistent. Makes sure that you do not have to reselect each color,
     * readjust line width, etc.
     * @param original - Existing canvas to base new one off of
     * @return - New canvas
     */
    public static Canvas initializeNewCanvas(Canvas original){
        Canvas canvas = new Canvas();
        canvas.setWidth(original.getWidth());
        canvas.setHeight(original.getHeight());
        canvas.setLayoutX(original.getLayoutX());
        canvas.setLayoutY(original.getLayoutY());
        CanvasEventHandlers.initializeEventHandlers(canvas);
        canvas.getGraphicsContext2D().setStroke(original.getGraphicsContext2D().getStroke());
        canvas.getGraphicsContext2D().setLineWidth(original.getGraphicsContext2D().getLineWidth());
        canvas.getGraphicsContext2D().setFill(original.getGraphicsContext2D().getFill());
        canvas.getGraphicsContext2D().setLineCap(original.getGraphicsContext2D().getLineCap());
        canvas.getGraphicsContext2D().setLineDashes(original.getGraphicsContext2D().getLineDashes());
        canvas.getGraphicsContext2D().setLineDashOffset(original.getGraphicsContext2D().getLineDashOffset());
        canvas.getGraphicsContext2D().setFont(original.getGraphicsContext2D().getFont());
        
        // Add the new canvas to the layer ArrayList
        addLayer(canvas);
        return canvas;
    }
    
    /**
     * Add a canvas to the layer arraylist
     * @param canvas - Canvas to add
     */
    public static void addLayer(Canvas canvas){
        canvasList.add(canvas);
    }
    
    /**
     * Swap 2 layers
     * @param initialSpot - Spot of one layer
     * @param endSpot - Spot of second layer
     */
    public static void swapLayers(int initialSpot, int endSpot){
        // Simple swapping algorithm
        Canvas temp = canvasList.get(endSpot);
        canvasList.remove(temp);
        canvasList.add(endSpot, canvasList.get(initialSpot));
        canvasList.remove(canvasList.get(initialSpot));
        canvasList.add(initialSpot, temp);
    }
    
    /**
     * Remove a layer
     * @param canvas - Canvas to remove
     */
    public static void removeLayer(Canvas canvas){
        // Remove canvas from the list
        canvasList.remove(canvas);
    }
    
    /**
     * Invert a canvas' visibility
     * @param index - Index within arraylist of the canvas to change
     */
    public void setLayerVisibility(int index){
        // Set the visibility to the opposite of what it currently is
        canvasList.get(index).setVisible(!canvasList.get(index).isVisible());
    }
    
    /**
     * Get the list of canvases
     * @return - Array List of canvases
     */
    public static ArrayList<Canvas> getCanvasList(){
        return canvasList;
    }
    
    /**
     * Combine all of the layers into a single image
     * @return - WritableImage with all layers
     */
    public static WritableImage combineLayers(){
        // List of images to draw on top of the base
        ArrayList<WritableImage> imageList = new ArrayList<>();
        for (Canvas canvas : canvasList) {
            if(canvas.isVisible()){ // Only add a canvas if it is visible.
                // Add a new image to the list of images.
                imageList.add(PaintClass.canvasSnapshotNoStackUpdate(canvas));
            }
        }
        
        // Image of the original canvas
        WritableImage baseOriginal = imageList.get(0);
        for (int i = 1; i < imageList.size(); i++) {
            // Draw each image onto the base
            canvasList.get(0).getGraphicsContext2D().drawImage(imageList.get(i), 0, 0);
        }
        // Snapshot the base with all of the images
        WritableImage base = PaintClass.canvasSnapshotNoStackUpdate(canvasList.get(0));
        // Return the base to its original state
        canvasList.get(0).getGraphicsContext2D().drawImage(baseOriginal, 0, 0);
        return base;
    }
}
