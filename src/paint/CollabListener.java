/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;

/**
 *
 * @author zdude
 */
public class CollabListener implements Runnable{
    private Socket client;
    private CollabServer server;
    
    /**
     * Initialize class
     * @param client - Client to listen for
     * @param server - Server to listen for
     */
    public CollabListener(Socket client, CollabServer server){
        this.client = client;
        this.server = server;
    }
    
    @Override
    public void run() {
        // Infinite loop. Always checking for new messages from server
        while(true){
            try{
                // Read image and update the client stage with that image
                BufferedImage image = ImageIO.read(ImageIO.createImageInputStream(client.getInputStream()));
                PaintClass.updateCanvas(SwingFXUtils.toFXImage(image, null));
            }catch(NullPointerException e){
                try {
                    // Sleep for .1 seconds if there is nothing in the ObjectInputStream currently
                    // This prevents complete overhaul of machine
                    Thread.sleep(100L);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CollabListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                Logger.getLogger(CollabListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
