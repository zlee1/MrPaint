/**
 * Class within which general objects are created and whatnot
 */
package paint;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.application.*;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.util.Pair;

public class PaintClass extends Application{
    
    private static String filePath; // The filepath of the currently opened file
    private static String drawFilePath; // The filepath of the image to draw
    private static Boolean saved = true; // Whether or not the current file is saved
    private static String shape; // What shape is currently selected
    
    private static Canvas canvas = new Canvas(700, 700);
    
    private static Canvas selectedCanvas = canvas;
    
    private FileManaging fManager = new FileManaging(); 
    private PanesAndLayout pAL = new PanesAndLayout();
    private static ImageManipulation iManipulation = new ImageManipulation();
    private MouseEvents mouseEvents = new MouseEvents();
    
    private static double xClickPoint; // X value of mouse click location
    private static double yClickPoint; // Y value of mouse click location
    
    private static Stack undoStack = new Stack(); // Hold images used to restore canvas to version before last change
    private static Stack redoStack = new Stack(); // Hold images used to restore canvas to version before last undo
    
    private static ArrayList<Stack> undoStacks = new ArrayList<>();
    private static ArrayList<Stack> redoStacks = new ArrayList<>();
    
    private static Line tempLine; // Line preview when user drawing line
    private static Rectangle tempRect; // Rectangle preview when user drawing rectangle
    private static Ellipse tempEllipse; // Ellipse preview when user drawing ellipse
    private static Rectangle tempSquare; // Square preview when user drawing square
    private static Ellipse tempCircle; // Circle preview when user drawing circle
    private static ImageView tempImageView; // Image preview when user drawing image
    private static Text tempText; // Text preview when user draws text
    private static Polygon tempPolygon; // Regular polygon preview when user draws regular polygon;
    private static Rectangle selectRect; // Rectangle showing user's selected area
    private static ImageView selectView = new ImageView(); // ImageView displaying user's selected area
    
    private Color fillColor = Color.BLACK; // Used to maintain fill color when user creates a new file
    
    private Boolean toolBarShown = false; // Tells whether tool bar is shown or not
    
    private Boolean shadow = true; // Tells whether shadow is displayed or not
    
    private static double arcWidth; // Width of rounded rectangle arc
    private static double arcHeight; // Height of rounded rectangle arc
    
    private static ArrayList<Double> polygonXPoints; // Stores X values of polygon vertices
    private static ArrayList<Double> polygonYPoints; // Stores Y values of polygon vertices
    
    private Color background = Color.LIGHTGRAY; // Stores background color
    private Color shadowColor = Color.GRAY; // Stores shadow color
    
    private double xScale = 1; // X Scale used for zoom
    private double yScale = 1; // Y Scale used for zoom
    
    // These allow the selected area to be dragged from any part of the selection
    private static int xDist; // Distance from xClickPoint to X location of selectView and selectRect
    private static int yDist; // Distance from yClickPoint to Y location of selectView and selectRect
    
    private static Boolean dropperSelected = false; // Tells whether or not the user is using color dropper
    private static ColorPicker strokePicker;
    private static ColorPicker fillPicker;
    
    private static ImageCursor dropperCursor = new ImageCursor(new Image("file:src/Images/DropperIcon.png"), 18, 238); // Dropper cursor to use when user is using dropper tool
    private static ImageCursor fillCursor = new ImageCursor(new Image("file:src/Images/FillIcon.png"), 25, 445);
    
    // Keyboard shortcuts
    private KeyCombination ctrlO = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
    private KeyCombination ctrlN = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
    private KeyCombination ctrlS = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    private KeyCombination ctrlZ = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
    private KeyCombination ctrlY = new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN);
    
    private ArrayList<Integer> pixels; // List of pixels that are transparent
    
    private Boolean keepTransparency = false; // Whether or not to maintain transparency when saving
    
    private static String text = "Sample Text"; // Text user will draw when using text draw tool
    
    // Creating image views for dragging to resize canvas
    private ImageView dragSE = new ImageView(new Image("file:src/Images/DragSEIcon.png", 25, 25, true, false));
    private ImageView dragS = new ImageView(new Image("file:src/Images/DragSIcon.png", 25, 25, true, false));
    private ImageView dragE = new ImageView(new Image("file:src/Images/DragEIcon.png", 25, 25, true, false));
    
    // Server and Client objects and threads
    private static RossServer server;
    private static Thread serverThread;
    private static RossClient client;
    private static Thread clientThread;
    
    private static CollabServer cServer;
    private static Thread cServerThread;
    private static CollabClient cClient;
    private static Thread cClientThread;
    
    private static int maxSeconds = 300; // Maximum time of the timer
    private static Label autosaveLabel = new Label(); // Label displaying time until autosave
    private Timer autosaveTimer = new Timer(); // Autosave timer
    private Thread thread = new Thread(autosaveTimer); // Thread based off of the autosave timer
    private static Button saveButton; // Save button
    private static Boolean timerVisible = true; // Whether or not the timer is visible
    private static Boolean timerEnabled = false; // Whether or not timer is activated
    
    private static ImageView clientView = new ImageView(); // Image view that will display host's screen
    private static Stage clientStage; // Stage for the client
    
    private static MenuItem watch = new MenuItem("Watch - View Only"); // Menu Item for watching with Bob Ross Mode
    
    private ScrollPane layerScrollPane = new ScrollPane();
    private GridPane mainLayerPane = PanesAndLayout.newGridPane();
    private Pane baseLayerPane = new Pane();
    
    private static Pane canvasPaneStatic;
    private static Button dropper;
    private static Scene scene;
    private static ComboBox strokeTypePicker;
    private static Button deselect;
    private static CheckBox fillBox;
    private static CheckBox roundedBox;
    private static ComboBox lineCapSelector;
    private static ComboBox lineTypePicker;
    private static ComboBox fontBox;
    private static ComboBox regPolygonSizeBox;
    
    /**
     * Update the canvas with a WritableImage. Called from the client/server to 
     * allow actual sending of information.
     * @param wi - WritableImage to draw on canvas.
     */
    public static void updateCanvas(WritableImage wi){
        iManipulation.setImage(null, wi, canvas.getGraphicsContext2D(), canvas, null, false);
    }
    
    /**
     * Return selected canvas
     * @return - Selected canvas
     */
    public static Canvas getSelectedCanvas(){
        return selectedCanvas;
    }
    
    /**
     * Return a canvas pane
     * @return - Canvas pane
     */
    public static Pane getCanvasPaneStatic(){
        return canvasPaneStatic;
    }
    
    /**
     * Return Boolean of whether or not dropper tool is in use
     * @return - dropperSelected
     */
    public static Boolean getDropperSelected(){
        return dropperSelected;
    }
    
    /**
     * Return color picker for picking stroke color
     * @return - stroke picker
     */
    public static ColorPicker getStrokePicker(){
        return strokePicker;
    }
    
    /**
     * Return color picker for picking fill color
     * @return - Fill Picker
     */
    public static ColorPicker getFillPicker(){
        return fillPicker;
    }
    
    /**
     * Return dropper button. Allows firing from other classes
     * @return - Dropper Button
     */
    public static Button getDropperButton(){
        return dropper;
    }
    
    /**
     * Return the scene
     * @return - Scene
     */
    public static Scene getScene(){
        return scene;
    }
    
    /**
     * Return selected shape
     * @return - String of shape
     */
    public static String getShape(){
        return shape;
    }
    
    /**
     * Add a point to the arraylist of polygon x and y coordinates
     * @param x - x Coordinate of polygon point
     * @param y  - y Coordinate of polygon point
     */
    public static void addPolygonPoint(double x, double y){
        polygonXPoints.add(x);
        polygonYPoints.add(y);
    }
    
    /**
     * Return ComboBox for stroke type picking
     * @return - ComboBox
     */
    public static ComboBox getStrokeTypePicker(){
        return strokeTypePicker;
    }
    
    /**
     * Return the deselect button in order to fire it from other classes
     * @return - deselect button
     */
    public static Button getDeselectButton(){
        return deselect;
    }
    
    /**
     * Return imageCursor for the dropper tool
     * @return - ImageCursor
     */
    public static ImageCursor getDropperCursor(){
        return dropperCursor;
    }
    
    /**
     * Return ImageCursor for the fill tool
     * @return - ImageCursor
     */
    public static ImageCursor getFillCursor(){
        return fillCursor;
    }
    
    /**
     * Return line preview
     * @return - tempLine
     */
    public static Line getTempLine(){
        return tempLine;
    }
    
    /**
     * Update the line preview
     * @param newLine - line object to replace preview
     */
    public static void setTempLine(Line newLine){
        tempLine = newLine;
    }
    
    /**
     * Return rectangle preview
     * @return - tempRect
     */
    public static Rectangle getTempRect(){
        return tempRect;
    }
    
    /**
     * Update rectangle preview
     * @param newRect - rectangle object to replace preview
     */
    public static void setTempRect(Rectangle newRect){
        tempRect = newRect;
    }
    
    /**
     * Return ellipse preview
     * @return - tempEllipse
     */
    public static Ellipse getTempEllipse(){
        return tempEllipse;
    }
    
    /**
     * Update ellipse preview
     * @param newEllipse - ellipse object to replace preview
     */
    public static void setTempEllipse(Ellipse newEllipse){
        tempEllipse = newEllipse;
    }
    
    /**
     * Return square preview
     * @return - tempSquare
     */
    public static Rectangle getTempSquare(){
        return tempSquare;
    }
    
    /**
     * Update square preview
     * @param newSquare - rectangle object to replace preview
     */
    public static void setTempSquare(Rectangle newSquare){
        tempSquare = newSquare;
    }
    
    /**
     * Return circle preview
     * @return - tempCircle
     */
    public static Ellipse getTempCircle(){
        return tempCircle;
    }
    
    /**
     * Update circle preview
     * @param newCircle - ellipse object to replace preview
     */
    public static void setTempCircle(Ellipse newCircle){
        tempCircle = newCircle;
    }
    
    /**
     * Return image drawing preview
     * @return - tempImageView
     */
    public static ImageView getTempImageView(){
        return tempImageView;
    }
    
    /**
     * Update image drawing preview
     * @param newImageView - ImageView to replace preview
     */
    public static void setTempImageView(ImageView newImageView){
        tempImageView = newImageView;
    }
    
    /**
     * Return text preview
     * @return - Text object to replace preview
     */
    public static Text getTempText(){
        return tempText;
    }
    
    /**
     * Update text preview
     * @param newText - Text object to replace preview
     */
    public static void setTempText(Text newText){
        tempText = newText;
    }
    
    /**
     * Return polygon preview
     * @return - tempPolygon
     */
    public static Polygon getTempPolygon(){
        return tempPolygon;
    }
    
    /**
     * Update polygon preview
     * @param newPolygon - polygon to replace preview
     */
    public static void setTempPolygon(Polygon newPolygon){
        tempPolygon = newPolygon;
    }
    
    /**
     * Return Rectangle that selects an area for select tool
     * @return - selectRect
     */
    public static Rectangle getSelectRect(){
        return selectRect;
    }
    
    /**
     * Update rectangle that selects an area for select tool
     * @param newSelectRect - Rectangle to replace preview
     */
    public static void setSelectRect(Rectangle newSelectRect){
        selectRect = newSelectRect;
    }
    
    /**
     * Return whether or not the fill box is selected
     * @return - Boolean of whether or not fill box is selected
     */
    public static Boolean getFill(){
        return fillBox.isSelected();
    }
    
    /**
     * Return whether or not the rounded box is selected
     * @return - Boolean of whether or not rounded box is selected
     */
    public static Boolean getRounded(){
        return roundedBox.isSelected();
    }
    
    /**
     * Return x value stored in click point
     * @return - xClickPoint
     */
    public static double getXClickPoint(){
        return xClickPoint;
    }
    
    /**
     * Return y value stored in click point
     * @return - yClickPoint
     */
    public static double getYClickPoint(){
        return yClickPoint;
    }
    
    /**
     * Update the x click point
     * @param point - x coordinate of mouseclick
     */
    public static void setXClickPoint(double point){
        xClickPoint = point;
    }
    
    /**
     * Update the y click point
     * @param point - y coordinate of mouseclick
     */
    public static void setYClickPoint(double point){
        yClickPoint = point;
    }
    
    /**
     * Return the user's selected arc width
     * @return - arcWidth
     */
    public static double getArcWidth(){
        return arcWidth;
    }
    
    /**
     * Return user's selected arc height
     * @return - arcHeight
     */
    public static double getArcHeight(){
        return arcHeight;
    }
    
    /**
     * Return ComboBox containing line cap selection
     * @return - lineCapSelector
     */
    public static ComboBox getLineCapSelector(){
        return lineCapSelector;
    }
    
    /**
     * Return ComboBox containing line type selection
     * @return - lineTypeSelector
     */
    public static ComboBox getLineTypePicker(){
        return lineTypePicker;
    }
    
    /**
     * Return file path of image being drawn as string
     * @return - drawFilePath
     */
    public static String getDrawFilePath(){
        return drawFilePath;
    }
    
    /**
     * Return string that will be drawn with text drawing
     * @return - text String
     */
    public static String getText(){
        return text;
    }
    
    /**
     * Return ComboBox that allows selection of different font sizes for text drawing
     * @return - fontBox
     */
    public static ComboBox getFontBox(){
        return fontBox;
    }
    
    /**
     * Return ComboBox that allows selection of number of sides for polygons
     * @return - regPolygonSizeBox
     */
    public static ComboBox getRegPolygonSizeBox(){
        return regPolygonSizeBox;
    }
    
    /**
     * Set the saved variable
     * @param s - Boolean deciding new value of saved
     */
    public static void setSaved(Boolean s){
        saved = s;
    }
    
    /**
     * Set the xDistance
     * @param newDist - new value for xDist
     */
    public static void setXDist(int newDist){
        xDist = newDist;
    }
    
    /**
     * Returns xDistance
     * @return - xDist
     */
    public static int getXDist(){
        return xDist;
    }
    
    /**
     * Set the yDistance
     * @param newDist - new value for yDist
     */
    public static void setYDist(int newDist){
        yDist = newDist;
    }
    
    /**
     * Return the yDistance
     * @return - yDist
     */
    public static int getYDist(){
        return yDist;
    }
    
    /**
     * Return user's selection from select tool as ImageView
     * @return - selectView
     */
    public static ImageView getSelectView(){
        return selectView;
    }
    
    /**
     * Update the selectView
     * @param newView - new ImageView for selectView
     */
    public static void setSelectView(ImageView newView){
        selectView = newView;
    }
    
    /**
     * Creating the stage that will display the server host's canvas contents
     */
    public static void createClientStage(){
        clientStage = new Stage();
        clientStage.setTitle("Mr. Paint - Bob Ross Mode Viewer");
        clientStage.getIcons().add(new Image("file:src/Images/MRPaint.png"));

        ScrollPane pane = new ScrollPane();

        pane.setContent(clientView); // Set content of ScrollPane to the ImageView that will contain the host's canvas contents

        Scene clientScene = new Scene(pane, 800, 800); // 800x800 is arbitrary. Allows for the default canvas size of 700x700 to be viewed without scrolling
        clientStage.setScene(clientScene);

        clientView.setEffect(PanesAndLayout.newShadow()); // Add a shadow

        clientStage.setOnCloseRequest((WindowEvent t) ->{
            watch.fire(); // If the user tries to close the window, disconnect from the host
        });

        clientStage.show();
    }
    
    /**
     * Update the client's view of the host's screen
     * @param wi - WritableImage to display on client's screen
     */
    public static void updateClientStage(WritableImage wi){
        clientView.setImage(wi); // Replace original image with updated image
    }
    
    /**
     * Return the max seconds variable
     * @return - maxSeconds
     */
    public static int getMaxSeconds(){
        return maxSeconds;
    }
    
    /**
     * Update the autosave label
     * @param seconds - Number of seconds remaining before saving
     */
    public static void setAutosaveLabel(int seconds){
        if(timerEnabled){
            Platform.runLater(() ->{
                // Autosave label should not show if no file has been selected
                if(!fileSelected()){
                    autosaveLabel.setVisible(false);
                }else{
                    autosaveLabel.setVisible(timerVisible);
                }

                // Formatting the time 
                int minutes = seconds/60;
                if(seconds%60 < 10){
                    autosaveLabel.setText(" Autosave in: " + minutes + ":0" + seconds%60);
                }else{
                    autosaveLabel.setText(" Autosave in: " + minutes + ":" + seconds%60);
                }

                // Save the file if there is no time remaining on the timer
                if(seconds == 0 && fileSelected()){
                    saveButton.fire();
                }
            });
        }else{
            autosaveLabel.setVisible(false);
        }
    }
    
    /**
     * Check whether or not a file is selected
     * @return - True if file is selected
     */
    public static Boolean fileSelected(){
        return filePath != null;
    }
        
    /**
     * Resize and align components to match scene dimensions
     * @param mBar - MenuBar
     * @param canvasPane - GridPane containing canvas
     * @param primaryStage - Stage
     * @param toolsStage - Stage for tools 
     * @param tools - Tools group
     * @param canvas - Canvas
     * @param sPane - ScrollPane
     * @param buttonPane - Pane containing all buttons and drawing tools
     * @param pane - Main pane holding everything
     * @param toolsOnlyPane - Pane with tools button
     * @param toolsButton - Tools button
     * @param canvasPaneStatic - Pane Holding canvas that will stay the same size as its contents
     */
    public void resizeAll(MenuBar mBar, Pane canvasPane, Stage primaryStage, Stage toolsStage, Group tools, Canvas canvas, ScrollPane sPane, GridPane buttonPane, GridPane pane, GridPane toolsOnlyPane, Button toolsButton, Pane canvasPaneStatic, Label zoomLabel){
        mBar.setMinWidth(primaryStage.getWidth());
        
        // Handle the toolbar
        if(primaryStage.getWidth() < buttonPane.getChildren().get(buttonPane.getChildren().size()-1).getLayoutX()+buttonPane.getChildren().get(buttonPane.getChildren().size()-1).getBoundsInLocal().getWidth()+15){
            toolsButton.setDisable(true); // Cannot remove toolbar window unless it fits
            if(!toolBarShown){ // Do not repeat this action every time. Only when toolbar not shown
                pane.getChildren().remove(buttonPane);

                if(!tools.getChildren().contains(buttonPane)){
                    tools.getChildren().add(buttonPane);
                    buttonPane.setLayoutX(0);
                    buttonPane.setLayoutY(0);
                    toolBarShown = true;
                }
                if(!toolsStage.isShowing()){
                    toolsStage.show();
                }
            }
        }else{
            toolsButton.setDisable(false);
        }
        
        // Offsets to allow visibility of ScrollBars
        sPane.setMaxWidth(primaryStage.getWidth()-216);
        sPane.setMaxHeight(primaryStage.getHeight()-138);
        sPane.setMinWidth(primaryStage.getWidth()-216);
        sPane.setMinHeight(primaryStage.getHeight()-138);
        
        // Resize canvas to keep gray space 
        // Gray space gives room for scrolling when necessary
        if(canvas.getWidth()*xScale > sPane.getWidth() || canvas.getHeight()*yScale > sPane.getHeight()){
            canvasPane.setMinWidth(canvas.getWidth()*xScale + sPane.getWidth());
            canvasPane.setMinHeight(canvas.getHeight()*yScale + sPane.getHeight());
        }else{
            canvasPane.setMinWidth(primaryStage.getWidth() - 218);
            canvasPane.setMinHeight(primaryStage.getHeight() - 140);
        }
        
        layerScrollPane.setMaxHeight(sPane.getMaxHeight());
        layerScrollPane.setMinHeight(sPane.getMinHeight());
        mainLayerPane.setMinHeight(layerScrollPane.getMinHeight()-5);
        
        dragSE.setX(canvas.getWidth()*xScale-2);
        dragSE.setY(canvas.getHeight()*yScale-2);
        dragS.setX(canvas.getWidth()*xScale/2-12);
        dragS.setY(canvas.getHeight()*yScale-2);
        dragE.setX(canvas.getWidth()*xScale-2);
        dragE.setY(canvas.getHeight()*yScale/2-12);
        
        zoomLabel.setText(" " + (int)(xScale*100) + "% "); // Set zoom label to proper zoom
    }
    
    /**
     * Set or remove the canvas shadow
     * @param canvas - Canvas
     */
    public void setShadow(Canvas canvas){
        if(shadow){
            canvas.setEffect(pAL.newShadow());
        }else{
            canvas.setEffect(null);
        }
    }
    
    /**
     * Take client's entered IP and Port, in order to create a client object
     * @return - IP Address and Port as strings
     */
    public Optional<Pair<String, String>> clientConnectionWindow(){
        // Creating dialog box for input of IP and Port
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Connect to a Host");
        dialog.setHeaderText("Connect to a Host");

        // Creating button to exit
        ButtonType okButton = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        // Allow user to enter IP
        TextField ipField = new TextField();
        ipField.setPromptText("198.164.18.10");
        Label ipLabel = new Label("IP Address: ");

        // Allow user to enter Port
        TextField portField = new TextField();
        portField.setPromptText("50123");
        Label portLabel = new Label("Port: ");

        // GridPane holding content for dialog
        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setPadding(new Insets(20, 150, 10, 10));

        // Adding labels and fields to pane
        pane.add(ipLabel, 0, 0);
        pane.add(ipField, 1, 0);
        pane.add(portLabel, 0, 1);
        pane.add(portField, 1, 1);

        // Setting the content of the dialog box
        dialog.getDialogPane().setContent(pane);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return new Pair<>(ipField.getText(), portField.getText()); // Return the user's entries
            }
            return null; // Otherwise return null
        });

        // Store the user's entries
        Optional<Pair<String, String>> hostInfo = dialog.showAndWait();
        return hostInfo; // Return user's entries
    }
    
    /**
     * Send information to the server to update the client side
     * @throws IOException - Handling exceptions with input/output
     */
    public static void updateServer() throws IOException{
        WritableImage wi;
        wi = Layering.combineLayers();
        try{
            server.updateClient(wi); // Update the client
        }catch(NullPointerException e){
        }
        try{
            cServer.updateClient(wi); // Update from collaborative server
        }catch(NullPointerException e){
        }
        try{
            cClient.updateServer(wi); // Update from collaborative client
        }catch(NullPointerException e){
        }
    }
    
    /**
     * Store snapshot of canvas in undo and redo stacks. 
     * @param snapCanvas - Canvas to take a snapshot of
     * @return - WritableImage containing snapshot of canvas
     */
    public static WritableImage canvasSnapshot(Canvas snapCanvas){
        int shift = 4; // Shift 4 pixels to fix bug
        if(!canvas.equals(snapCanvas)){
            shift = 0;
        }
        WritableImage wi = new WritableImage((int) snapCanvas.getWidth()+shift, (int) snapCanvas.getHeight()+shift);
        
        // Allows transparent snapshots
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        
        snapCanvas.snapshot(sp, wi); // Set Writable Image contents to that of the canvas
        
        wi = ImageManipulation.cropBug(wi, shift, shift, (int) wi.getWidth()-shift, (int) wi.getHeight()-shift);
        undoStacks.get(Layering.getCanvasList().indexOf(snapCanvas)).push(wi);
        redoStacks.get(Layering.getCanvasList().indexOf(snapCanvas)).clear();
        
        return wi;
    }
    
    /**
     * Take a snapshot of a canvas, but do not store image in undo/redo stacks
     * @param snapCanvas - Canvas to take a snapshot of
     * @return - WritableImage containing snapshot of canvas
     */
    public static WritableImage canvasSnapshotNoStackUpdate(Canvas snapCanvas){
        int shift = 4; // Shift 4 pixels to fix bug
        if(!canvas.equals(snapCanvas)){
            shift = 0;
        }
        WritableImage wi = new WritableImage((int) snapCanvas.getWidth()+shift, (int) snapCanvas.getHeight()+shift);
        
        // Allows transparent snapshots
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        
        snapCanvas.snapshot(sp, wi); // Set Writable Image contents to that of the canvas
        wi = ImageManipulation.cropBug(wi, shift, shift, (int) wi.getWidth()-shift, (int) wi.getHeight()-shift);
        return wi;
    }
    
    /**
     * Add a new layer to the canvas pile
     */
    public void addLayer(){
        // Add new undo/redo stacks. Different one for each of the canvases
        undoStacks.add(new Stack());
        redoStacks.add(new Stack());
        
        Pane layerPane = new Pane();
        
        // Values used for the size of layer selection menu objects. No "Magic
        // Numbers"
        layerPane.setMaxWidth(mainLayerPane.getMaxWidth());
        layerPane.setMinWidth(mainLayerPane.getMinWidth());
        layerPane.setMinHeight(100);
        layerPane.setMaxHeight(100);
        
        TextField layerField = new TextField();
        layerField.setPromptText("New Layer");
        layerField.setAlignment(Pos.CENTER);
        layerField.setMinWidth(layerPane.getMinWidth()-10);
        // "Selects" the new canvas automatically
        layerField.setStyle("-fx-background-color: #e9e9e9;");
        layerField.setLayoutX(5);
        layerField.setLayoutY(10);
        layerPane.getChildren().add(layerField);
        layerPane.setStyle("-fx-background-color: #dedede");
        
        // Select this layer when it is clicked on. 
        layerPane.setOnMouseClicked((MouseEvent t) ->{
            selectLayer(layerPane);
        });
        
        // Formatting of layer menu & layers
        
        // Add each of the layers to the layers arraylist
        ArrayList<Node> layers = new ArrayList<>();
        for (int i = 1; i < mainLayerPane.getChildren().size(); i++) {
            layers.add(mainLayerPane.getChildren().get(i));
        }
        
        // Remove all layers from the pane
        for (Node layer : layers) {
            mainLayerPane.getChildren().remove(layer);
        }
        mainLayerPane.add(layerPane, 0, 1); // Add the new layer to the top
        
        // Add all other layers back to pane
        for (Node layer : layers) {
            mainLayerPane.add(layer, 0, mainLayerPane.getChildren().size()+1);
        }
        canvasPaneStatic.getChildren().add(Layering.initializeNewCanvas(canvas));
        
        // Select the new layer that has been created.
        selectLayer(layerPane);
    }
    
    /**
     * Creating the base layer. Just like other layers, but with base canvas.
     */
    public void createBaseLayer(){
        // Add new stacks for base layer
        undoStacks.add(new Stack());
        redoStacks.add(new Stack());
        
        // Values used for the size of layer selection menu objects. No "Magic
        // Numbers"
        baseLayerPane.setMaxWidth(mainLayerPane.getMaxWidth());
        baseLayerPane.setMinWidth(mainLayerPane.getMinWidth());
        baseLayerPane.setMinHeight(100);
        baseLayerPane.setMaxHeight(100);
        
        Label baseLayerLabel = new Label("Base Layer");
        baseLayerLabel.setAlignment(Pos.CENTER);
        baseLayerLabel.setMinWidth(baseLayerPane.getMinWidth()-10);
        baseLayerLabel.setStyle("-fx-background-color: #e9e9e9");
        baseLayerLabel.setLayoutX(5);
        baseLayerLabel.setLayoutY(10);
        baseLayerPane.getChildren().add(baseLayerLabel);
        
        // Select base layer on mouse click
        baseLayerPane.setOnMouseClicked((MouseEvent t) ->{
            selectLayer(baseLayerPane);
        });
        
        // Select layer automatically
        selectLayer(baseLayerPane);
    }
    
    /**
     * Select a layer that has been clicked on or just created. 
     * @param pane - Pane that was interacted with 
     */
    public void selectLayer(Pane pane){
        // Find the pane that was previously selected, and set its color back to
        // normal.
        for (Node children : mainLayerPane.getChildren()) {
            if(children.getStyle().contains("#00baff")){
                children.setStyle("-fx-background-color: #dedede");
            }
        }
        
        // Update the color of the new selection to show it has been selected.
        pane.setStyle("-fx-background-color: #00baff");
        
        // Invert the list. Things stored in different order based on how they
        // make sense. Canvases start at the bottom and move up while panes start
        // at the top and move down. Not gonna lie to you, I did this a really dumb
        // way, and I am surprised that it works. Please do not touch it. 
        ArrayList<Canvas> tempList = Layering.getCanvasList();
        ArrayList<Canvas> list = new ArrayList<>();
        for (Canvas tempCanvas : tempList) {
            list.add(tempCanvas);
        }
        list = flipList(list);
        
        // Update the selected canvas
        selectedCanvas = list.get(mainLayerPane.getChildren().indexOf(pane)-1);
    }
    
    /**
     * Remove the layer that is currently selected.
     */
    public void removeSelectedLayer(){
        Node node = null;
        for (Node child : mainLayerPane.getChildren()) {
            // Find the layer pane that has been selected and store in node variable
            if(child.getStyle().contains("-fx-background-color: #00baff")){
                node = child;
                break;
            }
        }
        
        // Do not remove base layer pane
        if(!node.equals(baseLayerPane)){
            // Remove canvas
            Layering.removeLayer(selectedCanvas);
            canvasPaneStatic.getChildren().remove(selectedCanvas);
            mainLayerPane.getChildren().remove(node);
            selectLayer(baseLayerPane);
        }
    }
    
    // Flip an arraylist and return it
    public ArrayList flipList(ArrayList list){
        java.util.Collections.reverse(list);
        return list;
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        /**********************************Creation of all objects************************************************/
        Group root = new Group();
        scene = new Scene(root, 1200, 900, Color.LIGHTGRAY); // Create scene
        primaryStage.getIcons().add(new Image("file:src/Images/MRPaint.png")); // Set icons to Mr. Paint logo
        
        // Setting up Graphics Context for Canvas
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getHeight(), canvas.getWidth());
        
        // Create pane in order to change "Background Color" of areas where canvas does not cover. 
        Pane canvasPane = new Pane();
        canvasPane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        canvasPane.setMinWidth(scene.getWidth());
        canvasPane.setMinHeight(scene.getHeight());
        
        // Create static canvas pane to hold 
        canvasPaneStatic = new Pane();
        canvasPaneStatic.getChildren().add(canvas);
        canvasPaneStatic.setMaxWidth(canvas.getWidth());
        canvasPaneStatic.setMaxHeight(canvas.getHeight());
        
        canvasPane.getChildren().addAll(canvasPaneStatic, dragSE, dragS, dragE); // Add canvasPaneStatic to canvasPane
        
        // Create ScrollPane to hold CanvasPane
        ScrollPane sPane = new ScrollPane();
        sPane.setContent(canvasPane);
        
        Layering.addLayer(canvas);
        
        layerScrollPane.setContent(mainLayerPane);
        layerScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        layerScrollPane.setMinWidth(200);
        layerScrollPane.setMaxWidth(200);
        mainLayerPane.setStyle("-fx-background-color: #e9e9e9;");
        mainLayerPane.setMinWidth(layerScrollPane.getMinWidth()-2);
        mainLayerPane.setMaxWidth(layerScrollPane.getMaxWidth()-2);
        
        GridPane canvasAndLayers = PanesAndLayout.newGridPane();
        canvasAndLayers.add(sPane, 0, 0);
        canvasAndLayers.add(layerScrollPane, 1, 0);
        
        GridPane layeringButtons = PanesAndLayout.newGridPane();
        Button addButton = new Button();
        addButton.setGraphic(new ImageView(new Image("file:src/Images/AddIcon.png", 15, 15, false, false)));
        addButton.setMinSize(25, 25);
        addButton.setTooltip(new Tooltip("Add a new layer."));
        Button deleteButton = new Button();
        deleteButton.setGraphic(new ImageView(new Image("file:src/Images/DeleteIcon.png", 15, 15, false, false)));
        deleteButton.setMinSize(25, 25);
        deleteButton.setTooltip(new Tooltip("Delete the currently selected layer."));
        Button visibilityButton = new Button();
        visibilityButton.setGraphic(new ImageView(new Image("file:src/Images/VisibleIcon.png", 15, 15, false, false)));
        visibilityButton.setMinSize(25, 25);
        visibilityButton.setTooltip(new Tooltip("Set the currently selected layer's visibility."));
        Button moveUp = new Button();
        moveUp.setGraphic(new ImageView(new Image("file:src/Images/MoveUpIcon.png", 15, 15, false, false)));
        moveUp.setMinSize(25, 25);
        moveUp.setTooltip(new Tooltip("Move the currently selected layer up."));
        Button moveDown = new Button();
        moveDown.setGraphic(new ImageView(new Image("file:src/Images/MoveDownIcon.png", 15, 15, false, false)));
        moveDown.setMinSize(25, 25);
        moveDown.setTooltip(new Tooltip("Move the currently selected layer down."));
        layeringButtons.add(addButton, 0, 0);
        layeringButtons.add(deleteButton, 1, 0);
        layeringButtons.add(visibilityButton, 2, 0);
        layeringButtons.add(moveUp, 3, 0);
        layeringButtons.add(moveDown, 4, 0);
        mainLayerPane.add(layeringButtons, 0, 0);
        mainLayerPane.add(baseLayerPane, 0, 1);
        
        createBaseLayer();
        
        // Give the canvas shadow for extra fanciness
        DropShadow dShadow = pAL.newShadow();
        canvas.setEffect(dShadow);
        
        // Creating tools button and pane
        GridPane toolsOnlyPane = pAL.newGridPane();
        Button toolsButton = new Button();
        toolsButton.setGraphic(new ImageView(new Image("file:src/Images/ToolsIcon.png", 15, 15, false, false)));
        toolsButton.setMinSize(25, 75);
        toolsButton.setTooltip(new Tooltip("Open a new window containing the toolbar."));
        
        // GridPane used to store buttons
        GridPane buttonPane = pAL.newGridPane();
        
        // Creating pane to hold all objects
        GridPane pane = pAL.mainPane();
        
        // Create MenuItems for basic functionality
        MenuItem miNew = new MenuItem("New");
        MenuItem open = new MenuItem("Open");
        MenuItem save = new MenuItem("Save");
        MenuItem saveAs = new MenuItem("Save As");
        MenuItem exit = new MenuItem("Exit");
        MenuItem options = new MenuItem("Options");
        MenuItem about = new MenuItem("About");
        MenuItem toolHelp = new MenuItem("Tools");
        MenuItem undo = new MenuItem("Undo");
        MenuItem redo = new MenuItem("Redo");
        MenuItem resize = new MenuItem("Resize");
        MenuItem zoomIn = new MenuItem("Zoom In");
        MenuItem zoomOut = new MenuItem("Zoom Out");
        MenuItem flipH = new MenuItem("Flip Horizontally");
        MenuItem flipV = new MenuItem("Flip Vertically");
        MenuItem broadcast = new MenuItem("Broadcast - View Only");
        MenuItem collabBroadcast = new MenuItem("Broadcast - Collaborative");
        MenuItem collabWatch = new MenuItem("Watch - Collaborative");
        
        // Create Menus with items
        Menu file = new Menu("_File"); // Underscore in text allows user to press ALT + character after underscore to open menu
        file.getItems().addAll(miNew, open, save, saveAs, exit);
        Menu edit = new Menu("_Edit");
        edit.getItems().addAll(undo, redo, flipH, flipV, resize);
        Menu view = new Menu("_View");
        view.getItems().addAll(zoomIn, zoomOut);
        Menu toolsMenu = new Menu("_Tools");
        toolsMenu.getItems().add(options);
        Menu help = new Menu("_Help");
        help.getItems().addAll(toolHelp, about);
        Menu bobRossMode = new Menu("_Bob Ross Mode");
        bobRossMode.getItems().addAll(broadcast, watch, collabBroadcast, collabWatch); 
        Menu bobRossIP = new Menu();
        bobRossIP.setVisible(false);
        Menu bobRossPort = new Menu();
        bobRossPort.setVisible(false);
        
        // Create MenuBar with items
        MenuBar mBar = new MenuBar();
        mBar.getMenus().addAll(file, edit, view, toolsMenu, help, bobRossMode, bobRossIP, bobRossPort);
        mBar.setMinWidth(scene.getWidth());
        
        //Creating buttons
        Button newButton = new Button();
        newButton.setGraphic(new ImageView(new Image("file:src/Images/NewButton.png", 14, 14, false, false)));
        newButton.setMinSize(25, 25);
        newButton.setTooltip(new Tooltip("(Ctrl+N)\nCreate a new file with given dimensions."));
        Button openButton = new Button();
        openButton.setGraphic(new ImageView(new Image("file:src/Images/OpenButton.png", 15, 15, false, false)));
        openButton.setMinSize(25, 25);
        openButton.setTooltip(new Tooltip("(Ctrl+O)\nOpen an image to work with."));
        saveButton = new Button();
        saveButton.setGraphic(new ImageView(new Image("file:src/Images/SaveButton.png", 15, 15, false, false)));
        saveButton.setMinSize(25, 25);
        saveButton.setTooltip(new Tooltip("(Ctrl+S)\nSave work to the currently selected filepath."));
        Button saveAsButton = new Button();
        saveAsButton.setGraphic(new ImageView(new Image("file:src/Images/SaveAsButton.png", 15, 15, false, false)));
        saveAsButton.setMinSize(25, 25);
        saveAsButton.setTooltip(new Tooltip("Save work to a new filepath."));
        Button undoButton = new Button();
        undoButton.setGraphic(new ImageView(new Image("file:src/Images/UndoIcon.png", 15, 15, false, false)));
        undoButton.setMinSize(25, 25);
        undoButton.setTooltip(new Tooltip("(Ctrl+Z)\nUndo the most recent action."));
        Button redoButton = new Button();
        redoButton.setGraphic(new ImageView(new Image("file:src/Images/RedoIcon.png", 15, 15, false, false)));
        redoButton.setMinSize(25, 25);
        redoButton.setTooltip(new Tooltip("(Ctrl+Y)\nRedo the most recently undone action."));
        Button zoomInButton = new Button();
        zoomInButton.setGraphic(new ImageView(new Image("file:src/Images/ZoomInIcon.png", 15, 15, false, false)));
        zoomInButton.setMinSize(25, 25);
        zoomInButton.setTooltip(new Tooltip("Zoom in on the current work."));
        Button zoomOutButton = new Button();
        zoomOutButton.setGraphic(new ImageView(new Image("file:src/Images/ZoomOutIcon.png", 15, 15, false, false)));
        zoomOutButton.setMinSize(25, 25);
        zoomOutButton.setTooltip(new Tooltip("Zoom out on the current work."));
        dropper = new Button();
        dropper.setGraphic(new ImageView(new Image("file:src/Images/DropperIcon.png", 15, 15, false, false)));
        dropper.setMinSize(25, 25);
        dropper.setTooltip(new Tooltip("(D)\nSelect the dropper tool to pick a color from the work."));
        
        // ZoomPane creation and adding objects
        GridPane zoomPane = pAL.newGridPane();
        Label zoomLabel = new Label(" 100% ");
        zoomPane.add(zoomInButton, 0, 0);
        zoomPane.add(zoomLabel, 1, 0);
        zoomPane.add(zoomOutButton, 2, 0);
        
        // ArrayLists used to store coordinates of polygon vertices
        polygonXPoints = new ArrayList<>();
        polygonYPoints = new ArrayList<>();
        
        // ArrayLists used to store coordinates of pixels
        pixels = new ArrayList<>();
        
        // Creating and formatting Line Pt objects
        Label linePt = new Label(" Stroke Width: ");
        ComboBox linePtSelect = new ComboBox();
        linePtSelect.setEditable(true);
        linePtSelect.setMaxWidth(60);
        linePtSelect.getItems().addAll(1, 5, 10, 15, 20, 30, 40, 50, 75, 100);
        linePtSelect.getSelectionModel().select(1);
        GridPane linePtPane = pAL.newGridPane();
        linePtPane.add(linePt, 0, 0);
        linePtPane.add(linePtSelect, 1, 0);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineWidth(Double.parseDouble(linePtSelect.getValue().toString())); // Set line width to current value in ComboBox
        
        // Creating and formatting Stroke Color Picker objects
        Label strokePickerLabel = new Label(" Stroke Color: ");
        strokePicker = new ColorPicker();
        strokePicker.setValue(Color.BLACK);
        GridPane colorPane = pAL.newGridPane();
        colorPane.add(strokePickerLabel, 0, 0);
        colorPane.add(strokePicker, 1, 0);
        colorPane.add(dropper, 2, 0);
        gc.setStroke(strokePicker.getValue()); // Set stroke color to selected value in Stroke ColorPicker
        
        // Creating and formatting Stroke Type objects
        Label strokeTypeLabel = new Label(" Stroke Type: ");
        strokeTypePicker = new ComboBox();
        strokeTypePicker.getItems().addAll("Normal", "Spray Paint", "Air Brush");
        GridPane strokeTypePane = pAL.newGridPane();
        strokeTypePicker.getSelectionModel().select(0);
        strokeTypePane.add(strokeTypeLabel, 0, 0);
        strokeTypePane.add(strokeTypePicker, 1, 0);
        
        // Creating and formatting lineType objects
        Label lineTypeLabel = new Label(" Line Type: ");
        lineTypePicker = new ComboBox();
        lineTypePicker.getItems().addAll("Normal", "Dotted");
        lineTypePicker.getSelectionModel().select(0);
        Label dottedLabel = new Label(" Dot Length: ");
        ComboBox dottedBox = new ComboBox();
        dottedBox.setEditable(true);
        dottedBox.getItems().addAll(2, 5, 10, 25, 50);
        dottedBox.getSelectionModel().select(2);
        dottedBox.setMaxWidth(60);
        GridPane lineTypePane = pAL.newGridPane();
        lineTypePane.add(lineTypeLabel, 0, 0);
        lineTypePane.add(lineTypePicker, 1, 0);
        lineTypePane.add(dottedLabel, 2, 0);
        lineTypePane.add(dottedBox, 3, 0);
        
        // Creating and formatting Fill Color Picker objects
        Label fillLabel = new Label(" Fill Color: ");
        fillPicker = new ColorPicker();
        fillPicker.setValue(Color.BLACK);
        GridPane fillPane = pAL.newGridPane();
        fillPane.add(fillLabel, 0, 0);
        fillPane.add(fillPicker, 1, 0);
        gc.setFill(fillPicker.getValue()); // Set fill color to selected value in Fill ColorPicker
        
        // Creating and formatting shape selection objects
        Label shapeLabel = new Label(" Tool: ");
        ComboBox shapeSelector = new ComboBox();
        shapeSelector.setMaxWidth(130);
        shapeSelector.getItems().addAll("None", "Freeform", "Line", "Erase", "Rectangle", "Ellipse", "Image", "Text", "Select", "Fill", "Square", "Circle", "Custom Polygon", "Regular Polygon");
        shapeSelector.getSelectionModel().select(0);
        shape = shapeSelector.getValue().toString();
        GridPane shapePane = pAL.newGridPane();
        shapePane.add(shapeLabel, 0, 0);
        shapePane.add(shapeSelector, 1, 0);
        
        // Creating and formatting fill objects
        Label fillBoxLabel = new Label(" ");
        fillBox = new CheckBox("Fill ");
        fillBox.setMinSize(25, 25);
        fillPane.add(fillBoxLabel, 2, 0);
        fillPane.add(fillBox, 3, 0);
        
        // Creating and formatting rounded rectangle objects
        roundedBox = new CheckBox("Rounded ");
        ComboBox arcWidthSelector = new ComboBox();
        arcWidthSelector.getItems().addAll(1, 5, 10, 15, 20, 30, 40, 50, 75, 100);
        arcWidthSelector.setEditable(true);
        arcWidthSelector.setMaxWidth(60);
        arcWidthSelector.getSelectionModel().select(4);
        arcWidth = Double.parseDouble(arcWidthSelector.getSelectionModel().getSelectedItem().toString());
        ComboBox arcHeightSelector = new ComboBox();
        arcHeightSelector.getItems().addAll(1, 5, 10, 15, 20, 30, 40, 50, 75, 100);
        arcHeightSelector.setEditable(true);
        arcHeightSelector.setMaxWidth(60);
        arcHeightSelector.getSelectionModel().select(4);
        arcHeight = Double.parseDouble(arcHeightSelector.getSelectionModel().getSelectedItem().toString());
        Label arcWidthLabel = new Label(" Arc Width: ");
        Label arcHeightLabel = new Label(" Arc Height: ");
        Label spacingHeight = new Label(" ");
        GridPane arcWidthPane = pAL.newGridPane();
        GridPane arcHeightPane = pAL.newGridPane();
        arcWidthPane.add(arcWidthLabel, 0, 0);
        arcWidthPane.add(arcWidthSelector, 1, 0);
        arcHeightPane.add(arcHeightLabel, 0, 0);
        arcHeightPane.add(arcHeightSelector, 1, 0);
        arcHeightPane.add(spacingHeight, 2, 0);
        
        // Creating and formatting polygon objects for custom polygons
        GridPane polygonPane = pAL.newGridPane();
        Label polygonSpacing = new Label(" ");
        Button makePolygon = new Button("Make");
        polygonPane.add(polygonSpacing, 0, 0);
        polygonPane.add(makePolygon, 1, 0);
        
        // Creating and formatting polygon objects for regular polygons
        GridPane regPolygonPane = pAL.newGridPane();
        Label regPolygonSizeLabel = new Label(" Number of Sides: ");
        regPolygonSizeBox = new ComboBox();
        regPolygonSizeBox.getItems().addAll(3, 4, 5, 6, 7, 8, 9, 10);
        regPolygonSizeBox.setMaxWidth(60);
        regPolygonSizeBox.setEditable(true);
        regPolygonPane.add(regPolygonSizeLabel, 0, 0);
        regPolygonPane.add(regPolygonSizeBox, 1, 0);
        regPolygonSizeBox.getSelectionModel().select(2);
        
        // Creating and formatting text drawing objects
        GridPane textPane = pAL.newGridPane();
        Label fontLabel = new Label(" Font Size: ");
        Button textChoose = new Button("Choose Text");
        fontBox = new ComboBox();
        fontBox.getItems().addAll(8, 9, 10, 11, 12, 14, 18, 24, 30, 36, 48, 60, 72, 96);
        fontBox.getSelectionModel().select(4);
        textPane.add(textChoose, 0, 0);
        textPane.add(fontLabel, 2, 0);
        textPane.add(fontBox, 3, 0);
        
        // Creating and formatting line cap objects
        Label lineCapLabel = new Label(" Line Cap: ");
        lineCapSelector = new ComboBox();
        GridPane lineCapPane = pAL.newGridPane();
        lineCapPane.add(lineCapLabel, 0, 0);
        lineCapPane.add(lineCapSelector, 1, 0);
        lineCapSelector.setMaxWidth(100);
        lineCapSelector.getItems().addAll("Round", "Square");
        lineCapSelector.getSelectionModel().select(0);
        
        // Creating and formatting imagePicker
        Label imageLabel = new Label(" ");
        Button imagePicker = new Button("Pick an Image");
        imagePicker.setMinWidth(60);
        GridPane imagePane = pAL.newGridPane();
        imagePane.add(imageLabel, 0, 0);
        imagePane.add(imagePicker, 1, 0);
        
        // Creating and formatting selection tools
        Label cropSpacing = new Label(" ");
        Button cropButton = new Button("Crop");
        Label deselectSpacing = new Label(" ");
        Button deselect = new Button("Deselect");
        Label duplicateSpacing = new Label(" ");
        Button duplicate = new Button("Duplicate");
        GridPane selectPane = pAL.newGridPane();
        selectPane.add(cropSpacing, 0, 0);
        selectPane.add(cropButton, 1, 0);
        selectPane.add(deselectSpacing, 2, 0);
        selectPane.add(deselect, 3, 0);
        selectPane.add(duplicateSpacing, 4, 0);
        selectPane.add(duplicate, 5, 0);
        
        // Creating tools window 
        Group tools = new Group();
        Stage toolsStage = new Stage();
        Scene toolsScene = new Scene(tools, 1000, 75, Color.LIGHTGRAY); // Width and height of buttonPane
        toolsStage.setScene(toolsScene);
        toolsStage.setTitle("Tools - Mr. Paint");
        toolsStage.getIcons().add(new Image("file:src/Images/MRPaint.png"));
        toolsStage.setResizable(false);
        toolsStage.setX(50);
        toolsStage.setY(50);
        toolsStage.initStyle(StageStyle.UTILITY);
        
        GridPane mainFunctionalityPane = pAL.newGridPane();
        mainFunctionalityPane.add(newButton, 0, 0);
        mainFunctionalityPane.add(openButton, 1, 0);
        mainFunctionalityPane.add(saveButton, 2, 0);
        mainFunctionalityPane.add(saveAsButton, 3, 0);
        mainFunctionalityPane.add(undoButton, 4, 0);
        mainFunctionalityPane.add(redoButton, 5, 0);
        mainFunctionalityPane.add(zoomPane, 6, 0);
        mainFunctionalityPane.add(autosaveLabel, 7, 0);
        
        GridPane mainToolPane = pAL.newGridPane();
        mainToolPane.add(colorPane, 0, 0);
        mainToolPane.add(linePtPane, 1, 0);
        mainToolPane.add(fillPane, 2, 0);
        mainToolPane.add(shapePane, 3, 0);
        
        GridPane extraToolOptionsPane = pAL.newGridPane();
        extraToolOptionsPane.setMinHeight(25);
        
        // Add buttons to buttonPane
        buttonPane.add(mainFunctionalityPane, 0, 0);
        buttonPane.add(mainToolPane, 0, 1);
        buttonPane.add(extraToolOptionsPane, 0, 2);
        
        // Start the autosave thread
        thread.start();
        
        // Adding buttonPane and tools button to the toolsOnlyPane
        toolsOnlyPane.add(toolsButton, 0, 0);
        toolsOnlyPane.add(buttonPane, 1, 0);
        
        // Add items to main pane
        pane.add(mBar, 0, 0);
        pane.add(toolsOnlyPane, 0, 1);
        pane.add(canvasAndLayers, 0, 2);
        root.getChildren().addAll(pane);
        
        // Setting up Primary Stage
        primaryStage.setTitle("Mr. Paint");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        CanvasEventHandlers.initializeEventHandlers(canvas);
        
        // Resize everything, fixing bug that caused wrong dimensions upon stage showing
        resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);
                
        /************************************All Event Handlers***************************************************/
        
        /*************Additional Window Events*****************/
        
        // Handle close request for tools window
        toolsStage.setOnCloseRequest((WindowEvent t) ->{
            if(toolsButton.isDisabled()){
                t.consume(); // Do not do anything if the toolbar is not supposed to close
            }else{
                tools.getChildren().remove(buttonPane);
                toolsOnlyPane.add(buttonPane, 1, 0); // Return buttonPane to the main stage
                toolBarShown = false;
            }
        });
        
        /**************All Menu Item Actions*******************/
        
        // Handle event on options menuItem
        options.setOnAction((ActionEvent t) ->{
            // Creating options window 
            Group optionsGroup = new Group();
            Stage optionsStage = new Stage();
            Scene optionsScene = new Scene(optionsGroup, 300, 380, Color.LIGHTGRAY);
            GridPane optionsPane = pAL.newGridPane();
            optionsPane.setPadding(new Insets(10, 10, 10, 10));
            optionsPane.setHgap(10);
            optionsPane.setVgap(10);
            optionsStage.setScene(optionsScene);
            optionsStage.setTitle("Options - Mr. Paint");
            optionsStage.getIcons().add(new Image("file:src/Images/MRPaint.png"));
            optionsStage.setResizable(false);
            optionsStage.setX(50);
            optionsStage.setY(50);
            optionsPane.setMaxWidth(290);
            
            // Creating header
            Label optionsLabel = new Label("Options");
            optionsLabel.setFont(new Font(20));
            
            // Creating Shadow Box and handling event
            CheckBox shadowBox = new CheckBox("Canvas Shadow");
            shadowBox.setFont(new Font(15));
            if(shadow){
                shadowBox.setSelected(true);
            }else{
                shadowBox.setSelected(false);
            }
            shadowBox.setOnAction((ActionEvent u) ->{
                shadow = !shadow;
                if(shadow){
                    canvas.setEffect(dShadow);
                }else{
                    canvas.setEffect(null);
                }
            });
            
            // Creating Background ColorPicker and objects associated with it
            GridPane bgPane = pAL.newGridPane();
            Label bgLabel = new Label("Background Color: ");
            ColorPicker bgPicker = new ColorPicker();
            bgPane.add(bgLabel, 0, 0);
            bgPane.add(bgPicker, 1, 0);
            bgPicker.setValue(background);
            
            // Handle event on Background ColorPicker
            bgPicker.setOnAction((ActionEvent u) -> {
                background = bgPicker.getValue();
                canvasPane.setBackground(new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY)));
            });
            
            // Creating Shadow Color Picker and objects associated with it
            GridPane shadowPane = pAL.newGridPane();
            Label shadowLabel = new Label("Shadow Color:        "); // A lot of spaces to align ColorPicker properly
            ColorPicker shadowPicker = new ColorPicker();
            shadowPane.add(shadowLabel, 0, 0);
            shadowPane.add(shadowPicker, 1, 0);
            shadowPicker.setValue(shadowColor);
            
            // Handle event on Shadow ColorPicker
            shadowPicker.setOnAction((ActionEvent u) ->{
                shadowColor = shadowPicker.getValue();
                dShadow.setColor(shadowColor);
            });
            
            // Creating and formatting objects associated with transparency
            Label transparencyWarning = new Label("Choosing to save with transparency will make all white pixels transparent. PNG Files are recommended when saving with transparency.");
            transparencyWarning.setWrapText(true);
            transparencyWarning.setFont(new Font(14));
            CheckBox keepTransparencyBox = new CheckBox("Save with transparency");
            keepTransparencyBox.setFont(new Font(15));
            
            // Set the checkbox for transparency to selected if keepTransparency is true
            if(keepTransparency){
                keepTransparencyBox.setSelected(true);
            }else{
                keepTransparencyBox.setSelected(false);
            }

            // When checkbox selection changed, switch keepTransparency value
            keepTransparencyBox.setOnAction((ActionEvent u) ->{
                keepTransparency = !keepTransparency;
            });
            
            // Creating and for
            GridPane autosavePane = pAL.newGridPane();
            Label autosaveChangeLabel = new Label("Seconds until autosave: ");
            ComboBox autosaveComboBox = new ComboBox();
            CheckBox autosaveCheckBox = new CheckBox(" Timer Visible");
            CheckBox autosaveEnabledBox = new CheckBox(" Autosave Enabled");
            autosavePane.add(autosaveChangeLabel, 0, 0);
            autosavePane.add(autosaveComboBox, 1, 0);
            autosavePane.add(autosaveCheckBox, 0, 1);
            autosavePane.add(autosaveEnabledBox, 0, 2);
            autosaveComboBox.getItems().addAll(60, 120, 180, 240, 300, 600, 900, 1200);
            autosaveComboBox.setMaxWidth(60);
            autosaveComboBox.setEditable(true);
            autosaveComboBox.getItems().set(0, maxSeconds);
            autosaveComboBox.getSelectionModel().select(0);
            autosaveCheckBox.setFont(new Font(15));
            autosaveCheckBox.setSelected(timerVisible);
            autosaveEnabledBox.setFont(new Font(15));
            autosaveEnabledBox.setSelected(timerEnabled);
            
            
            // Handle action on autosaveComboBox
            autosaveComboBox.setOnAction((Event u) ->{
                // Autosave timer cannot be less than 15 seconds
                if(Integer.parseInt(autosaveComboBox.getSelectionModel().getSelectedItem().toString()) < 15){
                    autosaveComboBox.getSelectionModel().select(maxSeconds);
                }
                
                // Set max seconds to the selection
                try{
                    maxSeconds = Integer.parseInt(autosaveComboBox.getSelectionModel().getSelectedItem().toString());
                    autosaveTimer.setMaxSeconds(maxSeconds);
                    autosaveTimer.setSeconds(maxSeconds);
                }catch(IllegalArgumentException e){
                    autosaveComboBox.getSelectionModel().select(maxSeconds);
                }
            });
            
            // Switch timerVisible value when checkbox clicked
            autosaveCheckBox.setOnAction((ActionEvent u) ->{
                timerVisible = !timerVisible;
            });
            
            // Switch timerEnabled value when checkbox clicked
            autosaveEnabledBox.setOnAction((ActionEvent u) ->{
                timerEnabled = !timerEnabled;
            });
            
            // Ok button creation and formatting
            Button okButton = new Button("Ok");
            okButton.setMinWidth(50);
            okButton.setLayoutX(optionsScene.getWidth()/2 - okButton.getMinWidth()/2);
            okButton.setLayoutY(optionsScene.getHeight() - okButton.getHeight() - 30);
            
            // Handling event on okButton. Close window
            okButton.setOnAction((ActionEvent x) ->{
                optionsStage.close();
            });
            
            // Adding items to options pane
            optionsPane.add(optionsLabel, 0, 0);
            optionsPane.add(shadowBox, 0, 1);
            optionsPane.add(shadowPane, 0, 2);
            optionsPane.add(bgPane, 0, 3);
            optionsPane.add(keepTransparencyBox, 0, 4);
            optionsPane.add(transparencyWarning, 0, 5);
            optionsPane.add(autosavePane, 0, 6);
            
            
            // Adding items to group
            optionsGroup.getChildren().addAll(optionsPane, okButton);
            
            // Show options 
            optionsStage.show();
        });
        
        // Handle event on miNew MenuItem
        miNew.setOnAction((ActionEvent t) -> {
            newButton.fire(); // Call the new button's action
        });
        
        // Handle event on open MenuItem
        open.setOnAction((ActionEvent t) -> {
            openButton.fire(); // Call the open button's action
        });
        
        // Handle event on save MenuItem
        save.setOnAction((ActionEvent t) -> {
            saveButton.fire(); // Call the save button's action
        });
        
        // Handle event on saveAs MenuItem
        saveAs.setOnAction((ActionEvent t) -> {
            saveAsButton.fire(); // Call the saveAs button's action
        });
        
        // Handle event on exit MenuItem
        exit.setOnAction((ActionEvent t) -> {
            // Checking if file has been saved since last changes
            if(!saved){
                Alert alert = fManager.exitAlert();
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == alert.getButtonTypes().get(0)){
                    if(filePath == null){
                        fManager.handleSaveAs(filePath, primaryStage, canvas, pixels, keepTransparency); // Save as a file if no filepath exists
                        //End program and close window
                        Platform.exit();
                        System.exit(0);
                    }else{ 
                        fManager.handleSave(filePath, canvas, pixels, keepTransparency); // Otherwise save to current path
                        //End program and close window
                        Platform.exit();
                        System.exit(0);
                    }
                } else if (result.get() == alert.getButtonTypes().get(1)) {
                    //End program and close window
                    Platform.exit();
                    System.exit(0);
                }
            }else{
                //End program and close window
                Platform.exit();
                System.exit(0);
            }
        });
                
        // Handle event on about MenuItem
        about.setOnAction((ActionEvent t) -> {
            try {
                // Create stage for information about Mr. Paint
                Stage aboutStage = new Stage();
                aboutStage.setResizable(false);
                
                // Create panes in order to hold contents
                ScrollPane aboutPane = new ScrollPane();
                GridPane aboutGrid = pAL.newGridPane();
                
                // Store the text from the Release Notes text file into a string variable
                ClassLoader classLoader = new PaintClass().getClass().getClassLoader();
                File notesFile = new File(classLoader.getResource("TextFiles/PaintReleaseNotes.txt").getFile());
                String notesString = new String(Files.readAllBytes(notesFile.toPath()));
                
                // Creating label to display release notes
                Label notes = new Label(notesString);
                notes.setWrapText(true); // Allow text wrapping so that user does not need to scroll horizontally
                notes.setStyle("-fx-padding: 8 8 8 8"); // Add insets
                
                // Adding content to both panes
                aboutGrid.add(notes, 0, 0);
                aboutPane.setContent(aboutGrid);
                
                // Stage formatting 
                aboutStage.setScene(new Scene(aboutPane, 600, 500));
                aboutGrid.setMaxWidth(595); // GridPane width is 5 less than stage width. Keeps text from overflowing
                aboutStage.setTitle("Mr. Paint - About");
                aboutStage.getIcons().add(new Image("file:src/Images/MRPaint.png"));
                aboutStage.show(); // Show the stage
            } catch (IOException ex) {
                Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        // Handle event on toolHelp MenuItem
        toolHelp.setOnAction((ActionEvent t) ->{
            try {
                // Create stage for information about Mr. Paint Tools
                Stage aboutToolsStage = new Stage();
                aboutToolsStage.setResizable(false);
                
                // Create panes in order to hold contents
                ScrollPane aboutPane = new ScrollPane();
                GridPane aboutGrid = pAL.newGridPane();
                
                // Store the text from the Release Notes text file into a string variable
                ClassLoader classLoader = new PaintClass().getClass().getClassLoader();
                File notesFile = new File(classLoader.getResource("TextFiles/PaintToolsNotes.txt").getFile());
                String notesString = new String(Files.readAllBytes(notesFile.toPath()));
                
                // Creating label to display release notes
                Label notes = new Label(notesString);
                notes.setWrapText(true); // Allow text wrapping so that user does not need to scroll horizontally
                notes.setStyle("-fx-padding: 8 8 8 8"); // Add insets
                
                // Adding content to both panes
                aboutGrid.add(notes, 0, 0);
                aboutPane.setContent(aboutGrid);
                
                // Stage formatting 
                aboutToolsStage.setScene(new Scene(aboutPane, 600, 500));
                aboutGrid.setMaxWidth(595); // GridPane width is 5 less than stage width. Keeps text from overflowing
                aboutToolsStage.setTitle("Mr. Paint - Tools Help");
                aboutToolsStage.getIcons().add(new Image("file:src/Images/MRPaint.png"));
                aboutToolsStage.show(); // Show the stage
            } catch (IOException ex) {
                Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        // Handle event on undo MenuItem
        undo.setOnAction((ActionEvent t) ->{
            undoButton.fire(); // Call the undo button's action
        });
        
        // Handle event on redo MenuItem
        redo.setOnAction((ActionEvent t) ->{
            redoButton.fire(); // Call the redo button's action
        });
        
        // Handle event on zoomIn MenuItem
        zoomIn.setOnAction((ActionEvent t) ->{
            zoomInButton.fire(); // Call the zoom in button's action
        });
        
        // Handle event on zoomOut MenuItem
        zoomOut.setOnAction((ActionEvent t) ->{
            zoomOutButton.fire(); // Call the zoom out button's action
        });
        
        // Handle event on flipH MenuItem
        flipH.setOnAction((ActionEvent t) ->{
            WritableImage wi = canvasSnapshot(selectedCanvas); // Create a WritableImage including the canvas' contents
            
            // Flip the canvas' contents horizontally and draw them back onto the canvas
            selectedCanvas.getGraphicsContext2D().drawImage(wi, 0, 0, wi.getWidth(), wi.getHeight(), wi.getWidth(), 0, -wi.getWidth(), wi.getHeight());
            
            // Update the Bob Ross Mode server 
            try {
                updateServer();
            } catch (IOException  | NullPointerException ex) {
                Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            saved = false; // Canvas contents are no longer saved
        });
        
        // Handle event on flipV MenuItem
        flipV.setOnAction((ActionEvent t) ->{
            WritableImage wi = canvasSnapshot(selectedCanvas); // Create a WritableImage including the canvas' contents
            
            // Flip the canvas' contents vertically and draw them back onto the canvas
            selectedCanvas.getGraphicsContext2D().drawImage(wi, 0, 0, wi.getWidth(), wi.getHeight(), 0, wi.getHeight(), wi.getWidth(), -wi.getHeight());
            
            // Update the Bob Ross Mode server 
            try {
                updateServer();
            } catch (IOException  | NullPointerException ex) {
                Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
            }
            saved = false; // Canvas contents are no longer saved
        });
        
        // Handle event on resize MenuItem
        resize.setOnAction((ActionEvent t) ->{
            WritableImage wi = canvasSnapshot(selectedCanvas); // Create a WritableImage including the canvas' contents
            
            iManipulation.resizeImage(selectedCanvas, wi); // Resize the image
            
            // Update the Bob Ross Mode server 
            try {
                updateServer();
            } catch (IOException | NullPointerException ex) {
                Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
            }
            resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);
        });
        
        // Handle action on broadcast MenuItem
        broadcast.setOnAction((ActionEvent t) ->{
            try {
                if(broadcast.getText().equals("Broadcast - View Only")){
                    server = new RossServer(); // Create a new server
                    serverThread = new Thread(server); // Create a new thread based off of the runnable server
                    serverThread.start(); // Start the thread
                    broadcast.setText("Stop Broadcasting");
                    
                    // Show the IP and Port
                    bobRossIP.setText("IP: " + server.getServerAddress().getHostAddress());
                    bobRossIP.setVisible(true);
                    bobRossPort.setText("Port: " + server.getPort());
                    bobRossPort.setVisible(true);
                    
                    // Do not allow user to watch and broadcast at the same time
                    watch.setDisable(true);
                    collabBroadcast.setDisable(true);
                    collabWatch.setDisable(true);
                }else{
                    server = null; // Delete server
                    serverThread.stop(); // Stop thread
                    
                    // Remove IP and Port from screen
                    bobRossIP.setVisible(false);
                    bobRossPort.setVisible(false);
                    
                    broadcast.setText("Broadcast - View Only");
                    watch.setDisable(false); // Allow the user to watch other broadcasts
                    collabBroadcast.setDisable(false);
                    collabWatch.setDisable(false);
                }
            } catch (IOException ex) {
                Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        // Handle action on watch MenuItem
        watch.setOnAction((ActionEvent t) ->{
            if(watch.getText().equals("Watch - View Only")){
                Optional<Pair<String, String>> hostInfo = clientConnectionWindow();
                try {
                    // Attempt to create a new client based off of user's entered IP and Port
                    client = new RossClient(hostInfo.get().getKey(), Integer.parseInt(hostInfo.get().getValue()));
                    clientThread = new Thread(client); // Create a thread from the client
                    clientThread.start(); // Start the thread
                    
                    watch.setText("Stop Watching");
                    broadcast.setDisable(true); // Do not allow user to broadcast and watch at the same time
                    collabBroadcast.setDisable(true);
                    collabWatch.setDisable(true);
                    
                    createClientStage(); // Create the client stage
                    WritableImage wi = canvasSnapshot(selectedCanvas); // Take a snapshot of canvas
                    updateClientStage(wi); // Update the ImageView
                } catch (Exception e) {
                    Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, e);
                }
            }else{
                client = null; // Delete the client
                clientThread.stop(); // Stop the client thread
                watch.setText("Watch - View Only");
                broadcast.setDisable(false);
                collabBroadcast.setDisable(false);
                collabWatch.setDisable(false);
                clientStage.close(); // Close the client stage
            }
        });
        
        // Handle action on collaborative broadcast MenuItem
        collabBroadcast.setOnAction((ActionEvent t) ->{
            try {
                if(collabBroadcast.getText().equals("Broadcast - Collaborative")){
                    cServer = new CollabServer(); // Create a new server
                    cServerThread = new Thread(cServer); // Create a new thread based off of the runnable server
                    cServerThread.start(); // Start the thread
                    collabBroadcast.setText("Stop Broadcasting");
                    
                    // Show the IP and Port
                    bobRossIP.setText("IP: " + cServer.getServerAddress().getHostAddress());
                    bobRossIP.setVisible(true);
                    bobRossPort.setText("Port: " + cServer.getPort());
                    bobRossPort.setVisible(true);
                    
                    // Do not allow user to watch and broadcast at the same time
                    watch.setDisable(true);
                    broadcast.setDisable(true);
                    collabWatch.setDisable(true);
                }else{
                    cServer = null; // Delete server
                    cServerThread.stop(); // Stop thread
                    
                    // Remove IP and Port from screen
                    bobRossIP.setVisible(false);
                    bobRossPort.setVisible(false);
                    
                    collabBroadcast.setText("Broadcast - Collaborative");
                    watch.setDisable(false); // Allow the user to watch other broadcasts
                    broadcast.setDisable(false);
                    collabWatch.setDisable(false);
                }
            } catch (IOException ex) {
                Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        // Handle action on collaborative watch MenuItem
        collabWatch.setOnAction((ActionEvent t) ->{
            if(collabWatch.getText().equals("Watch - Collaborative")){
                Optional<Pair<String, String>> hostInfo = clientConnectionWindow();
                try {
                    // Attempt to create a new client based off of user's entered IP and Port
                    cClient = new CollabClient(hostInfo.get().getKey(), Integer.parseInt(hostInfo.get().getValue()));
                    cClientThread = new Thread(cClient); // Create a thread from the client
                    cClientThread.start(); // Start the thread
                    
                    collabWatch.setText("Stop Watching");
                    broadcast.setDisable(true); // Do not allow user to broadcast and watch at the same time
                    collabBroadcast.setDisable(true);
                    watch.setDisable(true);
                    
                    WritableImage wi = canvasSnapshot(selectedCanvas); // Take a snapshot of canvas
                } catch (Exception e) {
                    Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, e);
                }
            }else{
                client = null; // Delete the client
                clientThread.stop(); // Stop the client thread
                collabWatch.setText("Watch - Collaborative");
                broadcast.setDisable(false);
                collabBroadcast.setDisable(false);
                watch.setDisable(false);
            }
        });
        
        /****************All Button Actions********************/
        
        // Handle action on add layer button. Add a layer...
        addButton.setOnAction((ActionEvent t) ->{
            addLayer();
        });
        
        // Handle action on delete layer button. Delete a layer...
        deleteButton.setOnAction((ActionEvent t) ->{
            removeSelectedLayer();
        });
        
        // Handle action on layer visibility button
        visibilityButton.setOnAction((ActionEvent t) ->{
            selectedCanvas.setVisible(!selectedCanvas.isVisible());
        });
        
        // Handle action on move layer up button
        moveUp.setOnAction((ActionEvent t) ->{
            // Deselect the current selection
            deselect.fire();
            
            // Find the index of the selected layer
            int index = 0;
            for (int i = 0; i < mainLayerPane.getChildren().size(); i++) {
                if(mainLayerPane.getChildren().get(i).getStyle().contains("#00baff")){
                    index = i;
                    break;
                }
            }
            
            // Do not continue if layer is the top layer or is the base layer
            if(!selectedCanvas.equals(canvas) && index > 1 && index < mainLayerPane.getChildren().size()){
                // Swap layers within list of layers in layering class. More stupid
                // inversion of the arraylist. I'm sorry...
                Collections.reverse(Layering.getCanvasList());
                Layering.swapLayers(index-2, index-1);
                Collections.reverse(Layering.getCanvasList());
                
                // Remove all layers from the layer pane
                ArrayList<Node> list = new ArrayList<>();
                for (Node layer : mainLayerPane.getChildren()) {
                    list.add(layer);
                }
                mainLayerPane.getChildren().clear();
                
                // Add the buttons back to the main layer pane
                mainLayerPane.add(list.get(0), 0, 0);
                list.remove(0);
                
                // Swapping algorithm
                Node temp = list.get(index-1);
                list.remove(temp);
                list.add(index-1, list.get(index-2));
                list.remove(list.get(index-2));
                list.add(index-2, temp);
                
                // Add the layer panes back to the main layer pane
                int count = 1;
                for (Node layer : list){
                    mainLayerPane.add(layer, 0, count);
                    count++;
                }
                
                // Remove all canvases, and then add them back after swap.
                canvasPaneStatic.getChildren().clear();
                for (Canvas currentCanvas : Layering.getCanvasList()) {
                    canvasPaneStatic.getChildren().add(currentCanvas);
                }
            }
        });
        
        // Handle action on move layer down button
        moveDown.setOnAction((ActionEvent t) ->{
            // Deselect the current selection
            deselect.fire();
            
            // Find the index of the selected layer
            int index = 0;
            for (int i = 0; i < mainLayerPane.getChildren().size(); i++) {
                if(mainLayerPane.getChildren().get(i).getStyle().contains("#00baff")){
                    index = i;
                    break;
                }
            }
            
            // Do not continue if layer is the bottom or second to the bottom. 
            // Base layer should not be movable.
            if(!selectedCanvas.equals(canvas) && index < mainLayerPane.getChildren().size()-2){
                // Swap layers within list of layers in layering class. More stupid
                // inversion of the arraylist. I'm sorry...
                Collections.reverse(Layering.getCanvasList());
                Layering.swapLayers(index-1, index);
                Collections.reverse(Layering.getCanvasList());
                
                // Remove all layers from the layer pane
                ArrayList<Node> list = new ArrayList<>();
                for (Node layer : mainLayerPane.getChildren()) {
                    list.add(layer);
                }
                mainLayerPane.getChildren().clear();
                
                // Add the buttons back to the main layer pane
                mainLayerPane.add(list.get(0), 0, 0);
                list.remove(0);
                
                // Swapping algorithm
                Node temp = list.get(index);
                list.remove(temp);
                list.add(index, list.get(index-1));
                list.remove(list.get(index-1));
                list.add(index-1, temp);
                
                // Add the layer panes back to the main layer pane
                int count = 1;
                for (Node layer : list){
                    mainLayerPane.add(layer, 0, count);
                    count++;
                }
                
                // Remove all canvases, and then add them back after swap.
                canvasPaneStatic.getChildren().clear();
                for (Canvas currentCanvas : Layering.getCanvasList()) {
                    canvasPaneStatic.getChildren().add(currentCanvas);
                }
            }
        });
        
        // Handle event on new button
        newButton.setOnAction((ActionEvent t) -> {
            try{
                fManager.handleNew(canvas);
                filePath = null; // Clear filepath 
                
                canvasPaneStatic.getTransforms().clear(); // Clear transforms, to prevent user from starting file zoomed in or out
                xScale = 1;
                yScale = 1;
                
                resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);
                saved = true; // Blank file does not need to be saved
                gc.setFill(fillColor); // Restore fill from previous file, so that settings are not changed
                
                // Remove selection items when new canvas created
                canvasPaneStatic.getChildren().removeAll(selectView, selectRect);
                selectRect = null;
                selectView = null;
                
                // Nothing to redo or undo
                undoStack.clear();
                redoStack.clear();
            }catch(NoSuchElementException e){
            }
        });
        
        // Handle event on open button
        openButton.setOnAction((ActionEvent t) -> {
            try{
                String oldPath = filePath; // Save the old filepath
                ArrayList list;
                list = fManager.handleOpen(filePath, primaryStage, gc, canvas, pixels); // Call method to handle event
                
                try{
                    filePath = (String)list.get(0);
                    keepTransparency = (Boolean)list.get(1);
                }catch(NullPointerException e){
                }
                
                canvasPaneStatic.getTransforms().clear(); // Clear transforms, to prevent user from starting file zoomed in or out
                xScale = 1;
                yScale = 1;

                resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);

                // Remove selection items when file opened
                canvasPaneStatic.getChildren().removeAll(selectView, selectRect);
                selectRect = null;
                selectView = null;
                
                // Nothing to redo or undo
                undoStack.clear();
                redoStack.clear();
                
                try {
                    autosaveTimer.logTime(filePath, true);
                } catch (IOException ex) {
                    Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
                }

                if(!filePath.equals(oldPath)){ // If the file path is not changed, do not change the status of save. Nothing new has been opened. 
                    saved = filePath!=null;
                }
                autosaveTimer.setSeconds(maxSeconds);
                
                // Update the server
                try {
                    updateServer();
                } catch (IOException | NullPointerException ex) {
                    Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
                }
            }catch(NullPointerException e){ 
            }
        });
        
        // Handle event on save button
        saveButton.setOnAction((ActionEvent t) -> {
            deselect.fire();
            if(filePath != null){
                fManager.handleSave(filePath, canvas, pixels, keepTransparency); // Call method to handle save event
            }else{
                saveAs.fire(); // SaveAs if file does not already have a path
            }
            
            saved = true; // File has been saved
            autosaveTimer.setSeconds(maxSeconds);
        });
        
        // Handle event on saveAs button
        saveAsButton.setOnAction((ActionEvent t) -> {
            deselect.fire();
            filePath = fManager.handleSaveAs(filePath, primaryStage, canvas, pixels, keepTransparency); // Call method to handle save as event
            saved = true; // File has been saved
        });
        
        // Handle event on undo button
        undoButton.setOnAction((ActionEvent t) -> {
            try{
                if(!undoStacks.get(Layering.getCanvasList().indexOf(selectedCanvas)).isEmpty()){
                    // Remove selection items when action undone
                    canvasPaneStatic.getChildren().removeAll(selectView, selectRect);
                    selectRect = null;
                    selectView = null;
                    
                    int shift = 4; // Shift 4 pixels to fix bug
                    if(selectedCanvas.equals(canvas)){
                        shift = 4;
                    }else{
                        shift = 0;
                    }
                    WritableImage wi = new WritableImage((int) canvas.getWidth()+shift, (int) canvas.getHeight()+shift);
                    SnapshotParameters sp = new SnapshotParameters();
                    sp.setFill(Color.TRANSPARENT);
                    selectedCanvas.snapshot(sp, wi); // Set Writable Image contents to that of the canvas
                    
                    wi = iManipulation.cropBug(wi, shift, shift, (int) wi.getWidth()-shift, (int) wi.getHeight()-shift);

                    
                    redoStacks.get(Layering.getCanvasList().indexOf(selectedCanvas)).push(wi); // Allow current state to be restored
                    
                    if(selectedCanvas.equals(canvas)){
                        iManipulation.setImage(null, (Image) undoStacks.get(Layering.getCanvasList().indexOf(selectedCanvas)).pop(), selectedCanvas.getGraphicsContext2D(), selectedCanvas, pixels, false); // Undo an action
                    }else{
                        selectedCanvas.getGraphicsContext2D().clearRect(0, 0, selectedCanvas.getWidth(), selectedCanvas.getHeight());
                        selectedCanvas.getGraphicsContext2D().drawImage((Image) undoStacks.get(Layering.getCanvasList().indexOf(selectedCanvas)).pop(), 0, 0);
                    }
                    
                    // Clear polygon ArrayLists
                    polygonXPoints.clear();
                    polygonYPoints.clear();
                    saved = false; // File is no longer saved
                    resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);
                    
                    // Update the server
                    try {
                        updateServer();
                    } catch (IOException | NullPointerException ex) {
                        Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }catch(EmptyStackException e){
            }
        });
        
        // Handle event on redo button
        redoButton.setOnAction((ActionEvent t) -> {
            try{
                if(!redoStacks.get(Layering.getCanvasList().indexOf(selectedCanvas)).isEmpty()){
                    // Remove selection items when action redone
                    canvasPaneStatic.getChildren().removeAll(selectView, selectRect);
                    
                    // Clear selection
                    selectRect = null;
                    selectView = null;
                    
                    deselect.fire();
                    int shift = 4; // Shift 4 pixels to fix bug
                    if(selectedCanvas.equals(canvas)){
                        shift = 4;
                    }else{
                        shift = 0;
                    }
                    WritableImage wi = new WritableImage((int) canvas.getWidth()+shift, (int) canvas.getHeight()+shift);
                    SnapshotParameters sp = new SnapshotParameters();
                    sp.setFill(Color.TRANSPARENT);
                    selectedCanvas.snapshot(sp, wi); // Set Writable Image contents to that of the canvas
                    if(selectedCanvas.equals(canvas)){
                        wi = iManipulation.cropBug(wi, shift, shift, (int) wi.getWidth()-shift, (int) wi.getHeight()-shift);
                    }
                    
                    undoStacks.get(Layering.getCanvasList().indexOf(selectedCanvas)).push(wi); // Allow current state to be restored
                    
                    if(selectedCanvas.equals(canvas)){
                        iManipulation.setImage(null, (Image) redoStacks.get(Layering.getCanvasList().indexOf(selectedCanvas)).pop(), selectedCanvas.getGraphicsContext2D(), selectedCanvas, pixels, false); // Redo an action
                    }else{
                        selectedCanvas.getGraphicsContext2D().clearRect(0, 0, selectedCanvas.getWidth(), selectedCanvas.getHeight());
                        selectedCanvas.getGraphicsContext2D().drawImage((Image) redoStacks.get(Layering.getCanvasList().indexOf(selectedCanvas)).pop(), 0, 0);
                    }
                    
                    // Clear polygon ArrayLists
                    polygonXPoints.clear();
                    polygonYPoints.clear();
                    saved = false; // File is no longer saved
                    resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);
                    
                    // Update the server
                    try {
                        updateServer();
                    } catch (IOException | NullPointerException ex) {
                        Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }catch(EmptyStackException e){
            }
        });
        
        // Handle event on tools button
        toolsButton.setOnAction((ActionEvent t) -> {
            if(toolBarShown){
                // Restore buttonPane to primaryStage, and close tools window
                tools.getChildren().remove(buttonPane);
                toolsOnlyPane.add(buttonPane, 1, 0);
                
                toolsStage.close();
                toolBarShown = false; // The toolbar is no longer being shown
            }else{
                // Remove buttonPane from primaryStage and add it to the toolbar
                pane.getChildren().remove(buttonPane);
                try{
                    tools.getChildren().add(buttonPane);
                    buttonPane.setLayoutX(0);
                    
                }catch(IllegalArgumentException e){
                }
                toolsStage.show();
                toolBarShown = true;
            }
        });
        
        // Handle event on makePolygon button
        makePolygon.setOnAction((ActionEvent t) ->{
            // Cannot make a polygon with less than 2 sides
            if(polygonXPoints.size() >= 2 && polygonYPoints.size() >= 2){
                
                // Create new arrays that will store values from ArrayLists
                double[] xPoints = new double[polygonXPoints.size()];
                double[] yPoints = new double[polygonYPoints.size()];
                
                // Transfer values from ArrayLists to arrays
                for (int i = 0; i < polygonXPoints.size(); i++) {
                    xPoints[i] = polygonXPoints.get(i);
                    yPoints[i] = polygonYPoints.get(i);
                }
                
                // Fill when necessary
                if(fillBox.isSelected()){
                    selectedCanvas.getGraphicsContext2D().fillPolygon(xPoints, yPoints, polygonXPoints.size());
                }
                selectedCanvas.getGraphicsContext2D().strokePolygon(xPoints, yPoints, polygonXPoints.size());
                
                // Clear ArrayLists
                polygonXPoints.clear();
                polygonYPoints.clear();
                
                // Update the server
                try {
                    updateServer();
                } catch (IOException | NullPointerException ex) {
                    Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
                }
            } 
        });
        
        // Handle event on image picker button
        imagePicker.setOnAction((ActionEvent t) ->{
            // Allow the user to choose an image that they will be drawing
            drawFilePath = fManager.chooseDrawImage(drawFilePath, primaryStage);
            if(drawFilePath != null){
                imagePicker.setText("Change Image"); // If a file is selected, change text from "Pick an Image"
            }
        });
        
        // Handle event on zoomIn button
        zoomInButton.setOnAction((ActionEvent t) ->{
            // Create new scale, increasing by 5%
            Scale scale;
            try{
                scale = (Scale) canvasPaneStatic.getTransforms().get(0);
            }catch(IndexOutOfBoundsException e){
                scale = new Scale(1, 1, 0, 0);
            }
            
            // Increase scale by .05
            scale.setX(scale.getX()+.05);
            scale.setY(scale.getY()+.05);
            
            // Add to scale variables, to keep track of scale
            xScale = scale.getX();
            yScale = scale.getY();
            
            // Add scale to canvasPaneStatic
            canvasPaneStatic.getTransforms().clear();
            canvasPaneStatic.getTransforms().add(scale);
            
            resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);
        });
        
        // Handle event on zoomIn button
        zoomOutButton.setOnAction((ActionEvent t) ->{
            // Do not zoom out if scale is .01 or less
            if(xScale > .05){
                Scale scale;
                try{
                    scale = (Scale) canvasPaneStatic.getTransforms().get(0);
                }catch(IndexOutOfBoundsException e){
                    scale = new Scale(1, 1, 0, 0);
                }

                // Decrease scale by .05
                scale.setX(scale.getX()-.05);
                scale.setY(scale.getY()-.05);
                
                // Add to scale variables, to keep track of scale
                xScale = scale.getX();
                yScale = scale.getY();

                // Add scale to canvasPaneStatic
                canvasPaneStatic.getTransforms().clear();
                canvasPaneStatic.getTransforms().add(scale);

                resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);
            }
        });
        
        // Handle action on crop button. Crop to selected area
        cropButton.setOnAction((ActionEvent t) ->{
            if(canvasPaneStatic.getChildren().contains(selectRect)){
                saved = false;
                WritableImage wi = new WritableImage(selectView.getImage().getPixelReader(), (int)selectView.getImage().getWidth(), (int)selectView.getImage().getHeight());
                iManipulation.setImage(null, wi, gc, canvas, pixels, false);
                canvasPaneStatic.getChildren().removeAll(selectRect, selectView);
                
                // Clear selection
                selectRect = null;
                selectView = null;
                
                resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);
                zoomLabel.fireEvent(t);
                
                // Update the server
                try {
                    updateServer();
                } catch (IOException | NullPointerException ex) {
                    Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        // Deselect selected area when deselect button pressed
        deselect.setOnAction((ActionEvent t) ->{
            try{
                canvasPaneStatic.getChildren().remove(selectRect);
                selectedCanvas.getGraphicsContext2D().drawImage(selectView.getImage(), selectView.getX(), selectView.getY());
                canvasPaneStatic.getChildren().remove(selectView);
                
                // Clear selection
                selectRect = null;
                selectView = null;
                
                // Update the server
                try {
                    updateServer();
                } catch (IOException | NullPointerException ex) {
                    Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
                }
            }catch(NullPointerException e){
            }
        });
        
        // Duplicate the selected area
        duplicate.setOnAction((ActionEvent t) ->{
            if(canvasPaneStatic.getChildren().contains(selectRect)){
                selectedCanvas.getGraphicsContext2D().drawImage(selectView.getImage(), selectView.getX(), selectView.getY());
                selectRect.setX(5);
                selectRect.setY(5);
                selectView.setX(5);
                selectView.setY(5);
                canvasSnapshot(selectedCanvas);
                
                // Update the server
                try {
                    updateServer();
                } catch (IOException | NullPointerException ex) {
                    Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        // Handle action on color dropper button
        dropper.setOnAction((ActionEvent t) ->{
            dropperSelected = !dropperSelected;
            if(dropperSelected){
                scene.setCursor(dropperCursor);
                dropper.setGraphic(new ImageView(new Image("file:src/Images/PencilIcon.png", 15, 15, false, false)));
            }else{
                scene.setCursor(Cursor.DEFAULT);
                dropper.setGraphic(new ImageView(new Image("file:src/Images/DropperIcon.png", 15, 15, false, false)));
            }
        });
        
        // Handle action on text choose button
        textChoose.setOnAction((ActionEvent t) ->{
            text = mouseEvents.setText(text).get();
        });
        
        /******ColorPicker and ComboBox Event Handlers*********/
        
        // Handle Stroke ColorPicker selection change 
        strokePicker.setOnAction((ActionEvent t) -> {
            for (Canvas item : Layering.getCanvasList()) {
                item.getGraphicsContext2D().setStroke(strokePicker.getValue());
            }
            
            // Allow saved colors to transfer between both ColorPickers
            fillPicker.getCustomColors().clear();
            fillPicker.getCustomColors().addAll(strokePicker.getCustomColors());
        });
        
        // Handle Fill ColorPicker selection change 
        fillPicker.setOnAction((ActionEvent t) -> {
            for (Canvas item : Layering.getCanvasList()) {
                item.getGraphicsContext2D().setFill(fillPicker.getValue());
            }
            fillColor = fillPicker.getValue();
            
            // Allow saved colors to transfer between both ColorPickers
            strokePicker.getCustomColors().clear();
            strokePicker.getCustomColors().addAll(fillPicker.getCustomColors());
        });
        
        // Handle Dotted Combo Box selection change
        dottedBox.setOnAction((Event t) ->{
            // Do not allow users to set value to less than 2
            if(Double.parseDouble(dottedBox.getSelectionModel().getSelectedItem().toString()) < 2){
                dottedBox.getSelectionModel().select(2);
            }
            if(lineTypePicker.getSelectionModel().getSelectedItem().toString().equals("Dotted")){
                try{
                    for (Canvas item : Layering.getCanvasList()) {
                        item.getGraphicsContext2D().setLineDashes(Double.parseDouble(dottedBox.getSelectionModel().getSelectedItem().toString())); // Set line dashes to selected value
                    }
                }catch(IllegalArgumentException e){ // Ensures that value given is numerical
                    dottedBox.getSelectionModel().select(2);
                    for (Canvas item : Layering.getCanvasList()) {
                        item.getGraphicsContext2D().setLineDashes(Double.parseDouble(dottedBox.getSelectionModel().getSelectedItem().toString())); // Set line dashes to selected value
                    }
                }
            }else{
                for (Canvas item : Layering.getCanvasList()) {
                    item.getGraphicsContext2D().setLineDashes(0); // Set line dashes to selected value
                }
            }
        });
        
        // Handle regular polygon size ComboBox selection change
        regPolygonSizeBox.setOnAction((Event t) ->{
            // Ensure that selection is numerical
            try{
                int numSides = Integer.parseInt(regPolygonSizeBox.getSelectionModel().getSelectedItem().toString());
                if(numSides <= 2){
                    regPolygonSizeBox.getSelectionModel().select(2);
                }
            }catch(IllegalArgumentException e){
                regPolygonSizeBox.getSelectionModel().select(2);
            }
        });
        
        // Handle Line Type Combo Box selection change
        lineTypePicker.setOnAction((Event t) ->{
            if(lineTypePicker.getSelectionModel().getSelectedItem().toString().equals("Dotted")){
                for (Canvas item : Layering.getCanvasList()) {
                    item.getGraphicsContext2D().setLineDashes(Double.parseDouble(dottedBox.getSelectionModel().getSelectedItem().toString())); // Use initial value so that as soon as user switches to dotted, they can draw dotted
                }
            }else{
                for (Canvas item : Layering.getCanvasList()) {
                    item.getGraphicsContext2D().setLineDashes(0); // If user is not using dotted feature, no line dashes should be present
                }
            }
        });
        
        // Handle linePtSelector selection change
        linePtSelect.setOnAction((Event t) -> {
            try{
                for (Canvas item : Layering.getCanvasList()) {
                    item.getGraphicsContext2D().setLineWidth(Double.parseDouble(linePtSelect.getValue().toString())); // Set line width to selected width
                }
            }catch(NumberFormatException e){
                linePtSelect.getSelectionModel().select(1); // If user's entry is not numerical, switch to first option in ComboBox
            }
        });
        
        // Handle arcWidthSelector selection change
        arcWidthSelector.setOnAction((Event t) -> {
            try{
                arcWidth = Double.parseDouble(arcWidthSelector.getSelectionModel().getSelectedItem().toString());
            }catch(NumberFormatException e){
                arcWidthSelector.getSelectionModel().select(4); // If user's entry is not numerical, switch to fourth option in ComboBox
            }
        });
        
        // Handle arcHeightSelector selection change
        arcHeightSelector.setOnAction((Event t) -> {
            try{  
                arcHeight = Double.parseDouble(arcHeightSelector.getSelectionModel().getSelectedItem().toString());
            }catch(NumberFormatException e){
                arcHeightSelector.getSelectionModel().select(4); // If user's entry is not numerical, switch to fourth option in ComboBox
            }
        });
        
        // Handle lineCapSelector selection change
        lineCapSelector.setOnAction((Event t) -> {
            if(lineCapSelector.getSelectionModel().getSelectedItem().toString().equals("Round")){
                for (Canvas item : Layering.getCanvasList()) {
                    item.getGraphicsContext2D().setLineCap(StrokeLineCap.ROUND);
                }
            }else{
                for (Canvas item : Layering.getCanvasList()) {
                    item.getGraphicsContext2D().setLineCap(StrokeLineCap.SQUARE);
                }
            }
        });
        
        // Handle ComboBox selection change
        shapeSelector.setOnAction((Event t) -> {
            if(!shape.equals(shapeSelector.getValue().toString())){
                try {
                    autosaveTimer.logTime(shape, false);
                } catch (IOException ex) {
                    Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            shape = shapeSelector.getValue().toString();
            
            switch(shape){
                    case "Line":
                        extraToolOptionsPane.getChildren().removeAll(fillPane, arcWidthPane, arcHeightPane, roundedBox, polygonPane, strokeTypePane, imagePane, textPane, selectPane, regPolygonPane); // Clear out tools not relating to line drawing
                        deselect.fire(); // Fire the deselect button to avoid issues with selections
                        try{
                            extraToolOptionsPane.add(lineCapPane, 10, 0); // Line needs to have line cap options
                            extraToolOptionsPane.add(lineTypePane, 11, 0);
                        }catch(IllegalArgumentException e){
                        }
                        if(Layering.getCanvasList().get(Layering.getCanvasList().size()-1).isHover()){
                            scene.setCursor(Cursor.CROSSHAIR);
                        }
                        break;
                    case "Rectangle":
                        extraToolOptionsPane.getChildren().removeAll(lineCapPane, polygonPane, strokeTypePane, imagePane, textPane, selectPane, regPolygonPane); // Clear out objects not relating to rectangle drawing
                        deselect.fire(); // Fire the deselect button to avoid issues with selections
                        try{
                            extraToolOptionsPane.add(lineTypePane, 11, 0); 
                        }catch(IllegalArgumentException e){
                        }
                        
                        try{
                            // Rounded rectangle options
                            extraToolOptionsPane.add(arcWidthPane, 12, 0);
                            extraToolOptionsPane.add(arcHeightPane, 13, 0);
                            extraToolOptionsPane.add(roundedBox, 14, 0);
                        }catch(IllegalArgumentException e){
                        }
                        
                        if(Layering.getCanvasList().get(Layering.getCanvasList().size()-1).isHover()){
                            scene.setCursor(Cursor.CROSSHAIR);
                        }
                        break;
                    case "Square":
                        extraToolOptionsPane.getChildren().removeAll(lineCapPane, polygonPane, strokeTypePane, imagePane, textPane, selectPane, regPolygonPane); // Clear out objects not relating to square drawing
                        deselect.fire(); // Fire the deselect button to avoid issues with selections
                        try{
                            extraToolOptionsPane.add(lineTypePane, 11, 0);
                        }catch(IllegalArgumentException e){
                        }
                        
                        try{
                            // Rounded rectangle options
                            extraToolOptionsPane.add(arcWidthPane, 12, 0);
                            extraToolOptionsPane.add(arcHeightPane, 13, 0);
                            extraToolOptionsPane.add(roundedBox, 14, 0);
                        }catch(IllegalArgumentException e){
                        }
                        
                        if(Layering.getCanvasList().get(Layering.getCanvasList().size()-1).isHover()){
                            scene.setCursor(Cursor.CROSSHAIR);
                        }
                        break;
                    case "Ellipse":
                        extraToolOptionsPane.getChildren().removeAll(lineCapPane, arcWidthPane, arcHeightPane, roundedBox, polygonPane, strokeTypePane, imagePane, textPane, selectPane, regPolygonPane); // Clear out objects not relating to ellipse drawing
                        deselect.fire(); // Fire the deselect button to avoid issues with selections
                        try{
                            extraToolOptionsPane.add(lineTypePane, 11, 0);
                        }catch(IllegalArgumentException e){
                        }
                        
                        if(Layering.getCanvasList().get(Layering.getCanvasList().size()-1).isHover()){
                            scene.setCursor(Cursor.CROSSHAIR);
                        }
                        break;
                    case "Circle":
                        extraToolOptionsPane.getChildren().removeAll(lineCapPane, arcWidthPane, arcHeightPane, roundedBox, polygonPane, strokeTypePane, imagePane, textPane, selectPane, regPolygonPane); // Clear out objects not relating to circle drawing
                        deselect.fire(); // Fire the deselect button to avoid issues with selections
                        try{
                            extraToolOptionsPane.add(lineTypePane, 11, 0);
                        }catch(IllegalArgumentException e){
                        }
                        
                        if(Layering.getCanvasList().get(Layering.getCanvasList().size()-1).isHover()){
                            scene.setCursor(Cursor.CROSSHAIR);
                        }
                        break;
                    case "Custom Polygon":
                        extraToolOptionsPane.getChildren().removeAll(lineCapPane, arcWidthPane, arcHeightPane, roundedBox, strokeTypePane, imagePane, textPane, selectPane, regPolygonPane); // Clear out objects not relating to custom polygon drawing
                        deselect.fire(); // Fire the deselect button to avoid issues with selections
                        try{
                            extraToolOptionsPane.add(lineTypePane, 11, 0);
                        }catch(IllegalArgumentException e){
                        }
                        
                        // Polygon options
                        extraToolOptionsPane.add(polygonPane, 12, 0);
                        if(Layering.getCanvasList().get(Layering.getCanvasList().size()-1).isHover()){
                            scene.setCursor(Cursor.CROSSHAIR);
                        }
                        break;
                    case "Regular Polygon":
                        extraToolOptionsPane.getChildren().removeAll(lineCapPane, arcWidthPane, arcHeightPane, roundedBox, strokeTypePane, imagePane, textPane, selectPane, polygonPane); // Clear out objects not relating to regular polygon drawing
                        deselect.fire(); // Fire the deselect button to avoid issues with selections
                        try{
                            extraToolOptionsPane.add(lineTypePane, 11, 0);
                        }catch(IllegalArgumentException e){
                        }
                         
                        extraToolOptionsPane.add(regPolygonPane, 12, 0);
                        if(Layering.getCanvasList().get(Layering.getCanvasList().size()-1).isHover()){
                            scene.setCursor(Cursor.CROSSHAIR);
                        }
                        break;
                    case "Text":
                        extraToolOptionsPane.getChildren().removeAll(fillPane, arcWidthPane, arcHeightPane, roundedBox, polygonPane, strokeTypePane, lineTypePane, imagePane, selectPane, regPolygonPane, lineCapPane); // Clear out tools not relating to Text drawing
                        extraToolOptionsPane.add(textPane, 10, 0);
                        deselect.fire(); // Fire the deselect button to avoid issues with selections
                        
                        if(Layering.getCanvasList().get(Layering.getCanvasList().size()-1).isHover()){
                            scene.setCursor(Cursor.CROSSHAIR);
                        }
                        break;
                    case "Erase":
                        extraToolOptionsPane.getChildren().removeAll(fillPane, arcWidthPane, arcHeightPane, roundedBox, polygonPane, strokeTypePane, lineTypePane, imagePane, textPane, selectPane, regPolygonPane, lineCapPane); // Clear out tools not relating to erasing
                        deselect.fire();
                        
                        if(Layering.getCanvasList().get(Layering.getCanvasList().size()-1).isHover()){
                            scene.setCursor(Cursor.CROSSHAIR);
                        }
                        break;
                    case "Freeform":
                        extraToolOptionsPane.getChildren().removeAll(fillPane, arcWidthPane, arcHeightPane, roundedBox, polygonPane, lineCapPane, lineTypePane, imagePane, textPane, selectPane, regPolygonPane); // Clear out tools not relating to line drawing
                        deselect.fire();
                        try{
                            extraToolOptionsPane.add(strokeTypePane, 10, 0);
                        }catch(IllegalArgumentException e){
                        }
                        
                        if(Layering.getCanvasList().get(Layering.getCanvasList().size()-1).isHover()){
                            scene.setCursor(Cursor.CROSSHAIR);
                        }
                        break;
                    case "Image":
                        extraToolOptionsPane.getChildren().removeAll(fillPane, arcWidthPane, arcHeightPane, roundedBox, polygonPane, strokeTypePane, lineCapPane, lineTypePane, textPane, selectPane, regPolygonPane); // Clear out tools not relating to Image drawing
                        extraToolOptionsPane.add(imagePane, 10, 0);
                        deselect.fire(); // Fire the deselect button to avoid issues with selections
                        
                        if(Layering.getCanvasList().get(Layering.getCanvasList().size()-1).isHover()){
                            scene.setCursor(Cursor.CROSSHAIR);
                        }
                        break;
                    case "Select":
                        extraToolOptionsPane.getChildren().removeAll(lineCapPane, fillPane, arcWidthPane, arcHeightPane, roundedBox, polygonPane, strokeTypePane, lineTypePane, imagePane, textPane, regPolygonPane); // Remove every shape-specific tool
                        extraToolOptionsPane.add(selectPane, 10, 0);
                        
                        if(Layering.getCanvasList().get(Layering.getCanvasList().size()-1).isHover()){
                            scene.setCursor(Cursor.CROSSHAIR);
                        }
                        break;
                    case "Fill":
                        extraToolOptionsPane.getChildren().removeAll(lineCapPane, fillPane, arcWidthPane, arcHeightPane, roundedBox, polygonPane, strokeTypePane, lineTypePane, imagePane, textPane, selectPane, regPolygonPane); // Remove every shape-specific tool
                        
                        if(Layering.getCanvasList().get(Layering.getCanvasList().size()-1).isHover()){
                            scene.setCursor(fillCursor);
                        }
                        break;
                    default:
                        extraToolOptionsPane.getChildren().removeAll(lineCapPane, fillPane, arcWidthPane, arcHeightPane, roundedBox, polygonPane, strokeTypePane, lineTypePane, imagePane, textPane, selectPane, regPolygonPane); // Remove every shape-specific tool
                        deselect.fire(); // Fire the deselect button to avoid issues with selections
                        
                        if(Layering.getCanvasList().get(Layering.getCanvasList().size()-1).isHover()){
                            scene.setCursor(Cursor.CROSSHAIR);
                        }
                        break;
            }
            PauseTransition delay = new PauseTransition(Duration.seconds(.5));
            delay.setOnFinished( event -> resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel));
            delay.play(); // Delay required to fix bug causing CanvasPane taking too long to extend
            delay.play(); // Second call to fix bug causing ScrollBars to show up when they shouldn't 
        });
        
        /******************Mouse Events************************/
        
        // User can click on zoom label to restore zoom to 100%
        zoomLabel.setOnMouseClicked((MouseEvent t) ->{
            // Reset scale
            xScale = 1;
            yScale = 1;
            canvasPaneStatic.getTransforms().clear();
            
            resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);
        });
        
        // When mouse enters SE drag image view, change cursor to SE resize
        dragSE.setOnMouseEntered((MouseEvent t) ->{
            scene.setCursor(Cursor.SE_RESIZE);
        });
        
        // When mouse exits SE drag image view, change cursor to default
        dragSE.setOnMouseExited((MouseEvent t) ->{
            scene.setCursor(Cursor.DEFAULT);
        });
        
        // When mouse enters S drag image view, change cursor to S resize
        dragS.setOnMouseEntered((MouseEvent t) ->{
            scene.setCursor(Cursor.S_RESIZE);
        });
        
        // When mouse exits S drag image view, change cursor to default
        dragS.setOnMouseExited((MouseEvent t) ->{
            scene.setCursor(Cursor.DEFAULT);
        });
        
        // When mouse enters E drag image view, change cursor to E resize
        dragE.setOnMouseEntered((MouseEvent t) ->{
            scene.setCursor(Cursor.E_RESIZE);
        });
        
        // When mouse exits E drag image view, change cursor to default
        dragE.setOnMouseExited((MouseEvent t) ->{
            scene.setCursor(Cursor.DEFAULT);
        });
        
        // Handle mouse press event on ImageView 
        dragSE.setOnMousePressed((MouseEvent t) ->{
            canvasSnapshot(selectedCanvas); // Store non-resized canvas in undoStack
        });
        
        // Handle mouse drag event on ImageView
        dragSE.setOnMouseDragged((MouseEvent t) ->{
            // Draw a temporary rectangle showing the area that the canvas will be resized to
            canvasPaneStatic.getChildren().remove(tempRect);
            tempRect = mouseEvents.tempRect(canvas, 0, 0, t, false, false, 0, 0);
            tempRect.setStrokeWidth(2); // Thin stroke
            tempRect.setStroke(Color.GRAY); // Gray color
            tempRect.setFill(null);
            tempRect.getStrokeDashArray().addAll(5d); // Some small dashes
            canvasPaneStatic.getChildren().add(tempRect);
            scene.setCursor(Cursor.SE_RESIZE);
        });
        
        // Handle mouse release on ImageView
        dragSE.setOnMouseReleased((MouseEvent t) ->{
            scene.setCursor(Cursor.DEFAULT); // Cursor restored to default
            saved = false; // Work is no longer saved
            canvasPaneStatic.getChildren().remove(tempRect); // Remove the temporary rectangle
            
            // Save the original width and height of the canvas in variables
            double originalWidth = canvas.getWidth();
            double originalHeight = canvas.getHeight();
            
            // Set the new width and height
            canvas.setWidth(tempRect.getWidth());
            canvas.setHeight(tempRect.getHeight());
            
            for (Canvas currentCanvas : Layering.getCanvasList()) {
                if(!currentCanvas.equals(canvas)){
                    currentCanvas.setHeight(tempRect.getHeight());
                    currentCanvas.setWidth(tempRect.getWidth());
                    currentCanvas.getGraphicsContext2D().clearRect(originalWidth, 0, canvas.getWidth()-originalWidth, canvas.getHeight());
                    currentCanvas.getGraphicsContext2D().clearRect(0, originalHeight, canvas.getWidth(), canvas.getHeight()-originalHeight);
                }
            }
            
            Color originalFill = (Color)gc.getFill(); // Store original fill color
            
            // Fill the new area of the canvas with white
            gc.setFill(Color.WHITE);
            gc.fillRect(originalWidth, 0, canvas.getWidth()-originalWidth, canvas.getHeight());
            gc.fillRect(0, originalHeight, canvas.getWidth(), canvas.getHeight()-originalHeight);
            
            gc.setFill(originalFill); // Restore user's chosen fill color
            resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);
            
            // Update the server
            try {
                updateServer();
            } catch (IOException | NullPointerException ex) {
                Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        // Handle mouse press event on ImageView 
        dragS.setOnMousePressed((MouseEvent t) ->{
            canvasSnapshot(selectedCanvas); // Store non-resized canvas in undoStack
        });
        
        // Handle mouse drag event on ImageView
        dragS.setOnMouseDragged((MouseEvent t) ->{
            // Draw a temporary rectangle showing the area that the canvas will be resized to
            canvasPaneStatic.getChildren().remove(tempRect);
            tempRect = mouseEvents.tempRect(canvas, 0, 0, t, false, false, 0, 0);
            tempRect.setWidth(canvas.getWidth()); // User cannot change the width, since they are only dragging vertically
            tempRect.setStrokeWidth(2); // Thin stroke
            tempRect.setStroke(Color.GRAY); // Gray color
            tempRect.setFill(null);
            tempRect.getStrokeDashArray().addAll(5d); // Some small dashes
            canvasPaneStatic.getChildren().add(tempRect);
            scene.setCursor(Cursor.S_RESIZE);
        });
        
        // Handle mouse release event on ImageView
        dragS.setOnMouseReleased((MouseEvent t) ->{
            scene.setCursor(Cursor.DEFAULT); // Cursor restored to default
            saved = false; // Work is no longer saved
            canvasPaneStatic.getChildren().remove(tempRect); // Remove the temporary rectangle
            
            // Save the original height of the canvas in variable
            double originalHeight = canvas.getHeight();
            
            // Set the new height
            canvas.setHeight(tempRect.getHeight());
            
            for (Canvas currentCanvas : Layering.getCanvasList()) {
                if(!currentCanvas.equals(canvas)){
                    currentCanvas.setHeight(tempRect.getHeight());
                    currentCanvas.getGraphicsContext2D().clearRect(0, originalHeight, canvas.getWidth(), canvas.getHeight()-originalHeight);
                }
            }
            
            Color originalFill = (Color)gc.getFill(); // Store original fill color
            
            // Fill the new area of the canvas with white
            gc.setFill(Color.WHITE);
            gc.fillRect(0, originalHeight, canvas.getWidth(), canvas.getHeight()-originalHeight);
            
            gc.setFill(originalFill); // Restore user's chosen fill color
            resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);
            
            // Update the server
            try {
                updateServer();
            } catch (IOException | NullPointerException ex) {
                Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        // Handle mouse press event on ImageView 
        dragE.setOnMousePressed((MouseEvent t) ->{
            canvasSnapshot(selectedCanvas); // Store non-resized canvas in undoStack
        });
        
        // Handle mouse drag event on ImageView
        dragE.setOnMouseDragged((MouseEvent t) ->{
            // Draw a temporary rectangle showing the area that the canvas will be resized to
            canvasPaneStatic.getChildren().remove(tempRect);
            tempRect = mouseEvents.tempRect(canvas, 0, 0, t, false, false, 0, 0);
            tempRect.setHeight(canvas.getHeight()); // User cannot change the height, since they are only dragging horizontally
            tempRect.setStrokeWidth(2); // Thin stroke
            tempRect.setStroke(Color.GRAY); // Gray color
            tempRect.setFill(null);
            tempRect.getStrokeDashArray().addAll(5d); // Some small dashes
            canvasPaneStatic.getChildren().add(tempRect);
            scene.setCursor(Cursor.E_RESIZE);
        });
        
        // Handle mouse release event on ImageView
        dragE.setOnMouseReleased((MouseEvent t) ->{
            scene.setCursor(Cursor.DEFAULT); // Cursor restored to default
            saved = false; // Work is no longer saved
            canvasPaneStatic.getChildren().remove(tempRect); // Remove the temporary rectangle
            
            // Save the original width of the canvas in variable
            double originalWidth = canvas.getWidth();
            
            // Set the new width
            canvas.setWidth(tempRect.getWidth());
            
            for (Canvas currentCanvas : Layering.getCanvasList()) {
                if(!currentCanvas.equals(canvas)){
                    currentCanvas.setHeight(tempRect.getHeight());
                    currentCanvas.getGraphicsContext2D().clearRect(originalWidth, 0, canvas.getWidth()-originalWidth, canvas.getHeight());
                }
            }
            
            Color originalFill = (Color)gc.getFill(); // Store original fill color
            
            // Fill the new area of the canvas with white
            gc.setFill(Color.WHITE);
            gc.fillRect(originalWidth, 0, canvas.getWidth()-originalWidth, canvas.getHeight());
            
            gc.setFill(originalFill); // Restore user's chosen fill color
            resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);
            
            // Update the server
            try {
                updateServer();
            } catch (IOException | NullPointerException ex) {
                Logger.getLogger(PaintClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        /***************Main Window Events*********************/
        
        // Handle keyboard shortcuts
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent t) -> {
            if(ctrlO.match(t)){
                open.fire(); // Open if user presses Ctrl+O
            }else if(ctrlN.match(t)){
                newButton.fire(); // Create new file if user presses Ctrl+N
            }else if(ctrlS.match(t)){
                saveButton.fire(); // Save file if user presses Ctrl+S
            }else if(ctrlZ.match(t)){
                undoButton.fire(); // Undo if user presses Ctrl+Z
            }else if(ctrlY.match(t)){
                redoButton.fire(); // Redo if user presses Ctrl+Y
            }else if(new KeyCodeCombination(KeyCode.DELETE).match(t)){
                try{
                    // Delete the selected area by removing the selection from the pane, and setting the objects to null
                    canvasPaneStatic.getChildren().remove(selectRect);
                    canvasPaneStatic.getChildren().remove(selectView);
                    selectRect = null;
                    selectView = null;
                }catch(NullPointerException e){
                }
            }else if(new KeyCodeCombination(KeyCode.ENTER).match(t)){
                deselect.fire(); // Deselect if user presses Enter
            }else if(new KeyCodeCombination(KeyCode.R).match(t)){
                shapeSelector.getSelectionModel().select("Rectangle"); // Switch to rectangle control if user presses R
            }else if(new KeyCodeCombination(KeyCode.E).match(t)){
                shapeSelector.getSelectionModel().select("Ellipse"); // Switch to ellipse control if user presses E
            }else if(new KeyCodeCombination(KeyCode.L).match(t)){
                shapeSelector.getSelectionModel().select("Line"); // Switch to line control if user presses L
            }else if(new KeyCodeCombination(KeyCode.F).match(t)){
                shapeSelector.getSelectionModel().select("Freeform"); // Switch to freeform control if user presses F
            }else if(new KeyCodeCombination(KeyCode.P).match(t)){
                shapeSelector.getSelectionModel().select("Custom Polygon"); // Switch to custom polygon control if user presses P
            }else if(new KeyCodeCombination(KeyCode.G).match(t)){
                shapeSelector.getSelectionModel().select("Regular Polygon"); // Switch to regular polygon control if user presses G
            }else if(new KeyCodeCombination(KeyCode.A).match(t)){
                shapeSelector.getSelectionModel().select("Erase"); // Switch to eraser control if user presses A
            }else if(new KeyCodeCombination(KeyCode.S).match(t)){
                shapeSelector.getSelectionModel().select("Square"); // Switch to square control if user presses S
            }else if(new KeyCodeCombination(KeyCode.C).match(t)){
                shapeSelector.getSelectionModel().select("Circle"); // Switch to circle control if user presses C
            }else if(new KeyCodeCombination(KeyCode.I).match(t)){
                shapeSelector.getSelectionModel().select("Image"); // Switch to image control if user presses I
            }else if(new KeyCodeCombination(KeyCode.T).match(t)){
                shapeSelector.getSelectionModel().select("Text"); // Switch to text control if user presses T
            }else if(new KeyCodeCombination(KeyCode.B).match(t)){
                shapeSelector.getSelectionModel().select("Fill"); // Switch to fill tool if user presses B
            }else if(new KeyCodeCombination(KeyCode.M).match(t)){
                shapeSelector.getSelectionModel().select("Select"); // Switch to select tool if user presses M
            }else if(new KeyCodeCombination(KeyCode.D).match(t)){
                dropper.fire(); // Switch to dropper control if user presses D
            }
        });
        
        // When scene width changed, resize all components to match
        primaryStage.widthProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {
            resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);
        });
        
        // When scene height changed, resize all components to match
        primaryStage.heightProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) -> {
            resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel);
        });
        
        // Handle event of maximization
        primaryStage.maximizedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            // After .01 seconds, call resizeAll to fix bug causing scroll bars to stay visible after maximizing
            PauseTransition delay = new PauseTransition(Duration.seconds(0.01));
            delay.setOnFinished( event -> resizeAll(mBar, canvasPane, primaryStage, toolsStage, tools, canvas, sPane, buttonPane, pane, toolsOnlyPane, toolsButton, canvasPaneStatic, zoomLabel));
            delay.play();
            if(toolBarShown){
                delay.setOnFinished( event -> toolsButton.fire()); // Fire toolsButton so that when maximized, toolbar returns to primaryStage
                delay.play();
            }
        });
        
        // When close button pressed, request user save of file when necessary
        primaryStage.setOnCloseRequest((WindowEvent t) -> {
            // Checking if file has been saved since last changes
            if(!saved){
                Alert alert = fManager.exitAlert(); // Create an alert dialog
                
                Optional<ButtonType> result = alert.showAndWait(); // Wait for user to select an option from alert
                if (result.get() == alert.getButtonTypes().get(0)){ // If user chose to save
                    if(filePath == null){
                        fManager.handleSaveAs(filePath, primaryStage, canvas, pixels, keepTransparency); // Save as a file if no filepath exists
                        System.exit(0);
                        // Continue to end program
                    }else{
                        fManager.handleSave(filePath, canvas, pixels, keepTransparency); // Otherwise save to current path
                        System.exit(0);
                        // Continue to end program
                    }
                } else if (result.get() == alert.getButtonTypes().get(2)) {
                    t.consume(); // Cancel event if user selected Cancel
                }else{
                    System.exit(0); // End program if user selected No
                }
            }else{
                System.exit(0); // End program
            }
        });
    }
}