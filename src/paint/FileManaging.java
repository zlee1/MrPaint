/*
 * Class within which all file actions take place
 */
package paint;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Pair;
import javax.imageio.ImageIO;

public class FileManaging {
    private final ImageManipulation iManipulation = new ImageManipulation();
    private final ExtensionFilter images = new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif");
    private final ExtensionFilter png = new ExtensionFilter("PNG Files", "*.png");
    private final ExtensionFilter jpg = new ExtensionFilter("JPG Files", "*.jpg");
    private final ExtensionFilter gif = new ExtensionFilter("GIF Files", "*.gif");
    private final FileChooser chooser = new FileChooser();
    private Boolean keepTransparency;
    private WritableImage wi;
    
    /**
     * Set keepTransparency variable
     * @param keepTransparency - whether or not to keep transparency of an image
     */
    public void setKeepTransparency(Boolean keepTransparency){
        this.keepTransparency = keepTransparency;
    }
    
    /**
     * Get keepTransparency variable
     * @return - whether or not to keep transparency of an image
     */
    public Boolean getKeepTransparency(){
        return keepTransparency;
    }
    
    /**
     * Create a new canvas with given dimensions
     * @param canvas - Canvas
     */
    public void handleNew(Canvas canvas){
        // Creating dialog box for input of new canvas dimensions
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("New File");
        dialog.setHeaderText("Create a New File");
        
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
        canvas.setWidth(Double.parseDouble(dimensions.get().getKey()));
        canvas.setHeight(Double.parseDouble(dimensions.get().getValue()));
        canvas.getGraphicsContext2D().setFill(Color.WHITE);
        canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    /**
     * Handle the event of the open buttons 
     * @param filePath - String containing current file path, to be replaced by new path
     * @param primaryStage - stage
     * @param gc - canvas graphics context
     * @param canvas - canvas 
     * @param pixels - list of transparent pixels
     * @return - returns ArrayList with String to keep file path stored within PaintClass and with Boolean to decide whether to keep transparency or not
     */
    public ArrayList handleOpen(String filePath, Stage primaryStage, GraphicsContext gc, Canvas canvas, ArrayList pixels){
        // Allow user to pick image file
        chooser.setTitle("Select an Image File");
        chooser.getExtensionFilters().clear();
        chooser.getExtensionFilters().add(images);
        
        ArrayList list = new ArrayList();
        
        // Resolve error message displayed when no file is selected
        try{
            File file = chooser.showOpenDialog(primaryStage);
            if(file != null){
                keepTransparency = iManipulation.setImage(file.getPath(), null, gc, canvas, pixels, false); // Display user's image
                list.add(file.getPath());
                list.add(keepTransparency);
                return list;
            }
        }catch(NullPointerException e){
        }
        
        if(filePath == null){
            return null; // Only when no file is selected
        }else{
            list.add(filePath);
            list.add(false);
            return list;
        }
    }
    
    /**
     * Choose the image for image drawing
     * @param filePath - File path
     * @param primaryStage - Stage
     * @return - Changed file path
     */
    public String chooseDrawImage(String filePath, Stage primaryStage){
        // Allow user to pick image file
        chooser.setTitle("Select an Image File");
        chooser.getExtensionFilters().clear();
        chooser.getExtensionFilters().add(images);
        
        try{
            File file = chooser.showOpenDialog(primaryStage);
            filePath = file.getPath();
        }catch(NullPointerException e){
            return filePath;
        }
        return filePath;
    }
        
    /**
     * Handle the event of the save buttons 
     * @param filePath - file path
     * @param canvas - canvas
     * @param pixels - list of pixels that are transparent
     * @param keepTransparency - whether or not to maintain transparency when saving
     */
    public void handleSave(String filePath, Canvas canvas, ArrayList pixels, Boolean keepTransparency){
        File file = new File(filePath);
        try {
            wi = Layering.combineLayers();
            ArrayList tempPixels = pixels; // Allows removing of items from ArrayList without affecting original
            
            // Loop through all pixels
            if(keepTransparency){
                wi = saveLoops(wi, tempPixels);
            }
            
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(wi, null);
            ImageIO.write(renderedImage, "png", file); // Save image with default extension as .png
        } catch (IOException ex) {
            Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Loops that convert pixels to transparent where necessary
     * @param wi - WritableImage
     * @param pixels - ArrayList containing pixels marked for transparency
     * @return - Edited WritableImage
     */
    public static WritableImage saveLoops(WritableImage wi, ArrayList pixels){
        PixelReader reader = wi.getPixelReader();
        PixelWriter writer = wi.getPixelWriter();
        for (int x = 0; x < wi.getWidth(); x++) {
            for (int y = 0; y < wi.getHeight(); y++) {
                try{
                    if (reader.getArgb(x, y) == -1) {
                        writer.setArgb(x, y, 0);
                    }
                }catch(IndexOutOfBoundsException e){
                }
            }
        }
        return wi;
    }
    
    /**
     * Handle the event of save as buttons
     * @param filePath - file path
     * @param primaryStage - stage
     * @param canvas - canvas
     * @param pixels - list of transparent pixels
     * @param keepTransparency - whether or not to maintain transparency
     * @return - returns string to keep file path stored within PaintClass
     */
    public String handleSaveAs(String filePath, Stage primaryStage, Canvas canvas, ArrayList pixels, Boolean keepTransparency){
        try{
            chooser.setTitle("Save the File");
            chooser.getExtensionFilters().clear(); // Remove previous filters
            chooser.getExtensionFilters().addAll(png, jpg, gif); // Image can only be saved as .png, .jpg or .gif file
            File file = chooser.showSaveDialog(primaryStage);
            
            if(keepTransparency){
                if(file.getPath().endsWith(".jpg")){
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Mr. Paint - Lossy Data Warning");
                    alert.setHeaderText("Potential Loss of Data");
                    alert.setContentText("Saving as a JPG file will cause transparency to be lost. Save image as a PNG to restore transparency.");
                    alert.setGraphic(new ImageView(new Image("file:AlertIcon.png", 50, 50, true, false)));
                    alert.showAndWait();
                }
            }
            
            handleSave(file.getPath(), canvas, pixels, keepTransparency);
            
            return file.getPath(); // Return new path if a file is selected
        }catch(NullPointerException e){
        }
        return null; // Return null if no file is selected.
    }
    
    /**
     * Creates an alert for when the user has unsaved work
     * @return - Alert to display
     */
    public Alert exitAlert(){
        Alert alert = new Alert(AlertType.CONFIRMATION);
        
        alert.setTitle("Unsaved Work");
        alert.setHeaderText("You have unsaved work.");
        alert.setContentText("Do you wish to save?");
        
        alert.setGraphic(new ImageView(new Image("file:AlertIcon.png", 50, 50, true, false)));

        ButtonType buttonTypeSave = new ButtonType("Yes");
        ButtonType buttonTypeNoSave = new ButtonType("No");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeSave, buttonTypeNoSave, buttonTypeCancel);
        return alert;
    }
}
