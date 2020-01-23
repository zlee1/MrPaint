/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;

/**
 *
 * @author zdude
 */
public class RossClient implements Runnable{
    private Socket clientSocket; // Current client socket
    private ObjectInputStream objectInput;
    
    
    /**
     * Create a new instance of the RossClient
     * @param serverAddress - Address of the server to connect to
     * @param serverPort - Port of the server to connect to
     * @throws IOException - Exception handling
     */
    public RossClient(String serverAddress, int serverPort) throws IOException{
        this.clientSocket = new Socket(serverAddress, serverPort);
    }
    
    /**
     * Run the class
     */
    @Override
    public void run() {
        try {
            // Create ObjectInputStream to read images from
            objectInput = new ObjectInputStream(clientSocket.getInputStream());
            
            // Infinite loop. Always checking for new messages from server
            while(true){
                try{
                    // Read image and update the client stage with that image
                    BufferedImage image = ImageIO.read(ImageIO.createImageInputStream(objectInput));
                    PaintClass.updateClientStage(SwingFXUtils.toFXImage(image, null));
                }catch(NullPointerException e){
                    try {
                        // Sleep for .1 seconds if there is nothing in the ObjectInputStream currently
                        // This prevents complete overhaul of machine
                        Thread.sleep(100L);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RossClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(RossClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
