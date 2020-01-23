/*
 * Class within which all image manipulation takes place
 */
package paint;

import java.util.ArrayList;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

public class ImageManipulation {
    
    /**
     * Display an image on the Canvas
     * @param path - file path
     * @param img - Image
     * @param gc - canvas graphics context
     * @param canvas - canvas
     * @param pixels - list of pixels that are transparent
     * @param resized - Whether or not the image is being resized
     * @return - 
     */
    public Boolean setImage(String path, Image img, GraphicsContext gc, Canvas canvas, ArrayList pixels, Boolean resized){
        if(img == null && !resized){
            img = new Image("file:" + path); // Create image file
        }
        
        Boolean keepTransparency = false;
                
        
        // Handle transparent pixels
        PixelReader reader = img.getPixelReader();
        WritableImage wi = new WritableImage(reader, (int)img.getWidth(), (int)img.getHeight());
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                if(reader.getArgb(i, j) == 0){
                    keepTransparency = true;
                    wi.getPixelWriter().setColor(i, j, Color.WHITE); // Set color of transparent pixels to white. JavaFX cannot maintain transparency
                    pixels.add(i + " " + j);
                }
            }
        }
        
        if(!resized){
            // Adjust canvas to fit image
            canvas.setHeight(img.getHeight());
            canvas.setWidth(img.getWidth());
            
            // Draw image on canvas
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.drawImage(wi, 0, 0);
        }else{
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.drawImage(wi, 0, 0, canvas.getWidth(), canvas.getHeight());
        }
        return keepTransparency;
    }
    
    /**
     * Crop image to solve issue where white space was added when saved
     * @param wi - Writable Image
     * @param x - X position of new image
     * @param y - Y position of new image
     * @param width - Width of new image
     * @param height - Height of new image
     * @return - Writable Image
     */
    public static WritableImage cropBug(WritableImage wi, int x, int y, int width, int height){
        PixelReader reader = wi.getPixelReader();
        wi = new WritableImage(reader, x, y, width, height);
        return wi;
    }
    
    /**
     * Resize the image to given dimensions
     * @param canvas - Canvas
     * @param wi - WritableImage to resize
     */
    public void resizeImage(Canvas canvas, WritableImage wi){
        // Creating dialog box for input of new canvas dimensions
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Resize File");
        dialog.setHeaderText("Resize File");
        
        // Creating button to exit
        ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);
        
        // Allow user to enter custom width
        TextField x = new TextField();
        x.setPromptText("700");
        Label xLabel = new Label("Width: ");
        
        // Allow user to enter custom height
        TextField y = new TextField();
        y.setPromptText("700");
        Label yLabel = new Label("Height: ");
        
        // GridPane holding content for dialog
        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setPadding(new Insets(20, 150, 10, 10));

        pane.add(xLabel, 0, 0);
        pane.add(x, 1, 0);
        pane.add(yLabel, 0, 1);
        pane.add(y, 1, 1);

        dialog.getDialogPane().setContent(pane);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                // Test to make sure all values are numerical
                try{
                    double xTest = Double.parseDouble(x.getText());
                }catch(IllegalArgumentException e){
                    x.setText(x.getPromptText());
                }
                
                // Test to make sure all values are numerical
                try{
                    double yTest = Double.parseDouble(y.getText());
                }catch(IllegalArgumentException e){
                    y.setText(y.getPromptText());
                }
                
                // If no value is given, use prompt text
                if(x.getText().equals("")){
                    x.setText(x.getPromptText()); // Set width to prompt
                }
                if(y.getText().equals("")){
                    y.setText(y.getPromptText()); // Set height to prompt
                }
                return new Pair<>(x.getText(), y.getText());
            }
            return null;
        });
        
        // Set canvas dimensions to user-provided dimensions 
        Optional<Pair<String, String>> dimensions = dialog.showAndWait();
        
        for (Canvas currentCanvas : Layering.getCanvasList()) {
            wi = PaintClass.canvasSnapshotNoStackUpdate(currentCanvas);
            currentCanvas.setWidth(Double.parseDouble(dimensions.get().getKey()));
            currentCanvas.setHeight(Double.parseDouble(dimensions.get().getValue()));
            if(currentCanvas.equals(Layering.getCanvasList().get(0))){
                setImage(null, wi, currentCanvas.getGraphicsContext2D(), currentCanvas, null, true);
            }else{
                currentCanvas.getGraphicsContext2D().clearRect(0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());
                currentCanvas.getGraphicsContext2D().drawImage(wi, 0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());
            }
            
        }
        
    }
    
    /**
     * Convert user-selected area to an ImageView
     * @param canvas - Canvas
     * @param selectRect - User-selected area
     * @return - ImageView with image of user-selected area
     */
    public static ImageView selectedImage(Canvas canvas, Rectangle selectRect){        
        int shift = 4; // Shift 4 pixels to fix bug
        if(!canvas.equals(Layering.getCanvasList().get(0))){
            shift = 0;
        }
        WritableImage snapshot = new WritableImage((int) canvas.getWidth()+shift, (int) canvas.getHeight()+shift);
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        canvas.snapshot(sp, snapshot); // Set Writable Image contents to that of the canvas
        snapshot = cropBug(snapshot, shift, shift, (int) snapshot.getWidth()-shift, (int) snapshot.getHeight()-shift);
        WritableImage wi = new WritableImage(snapshot.getPixelReader(), (int)selectRect.getX(), (int)selectRect.getY(), (int)selectRect.getWidth(), (int)selectRect.getHeight());
        
        ImageView view = new ImageView(wi);
        return view;
    }
}
