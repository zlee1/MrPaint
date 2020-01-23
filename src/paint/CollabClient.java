/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;

/**
 *
 * @author zdude
 */
public class CollabClient implements Runnable{
    private Socket client; // Current client socket
    private CollabListener listener;
    private Thread listenThread;
    
    
    /**
     * Create a new instance of the RossClient
     * @param serverAddress - Address of the server to connect to
     * @param serverPort - Port of the server to connect to
     * @throws IOException - Exception handling
     */
    public CollabClient(String serverAddress, int serverPort) throws IOException{
        this.client = new Socket(serverAddress, serverPort);
    }
    
    /**
     * Update all of the clients with an updated version of the host's canvas
     * @param wi - WritableImage containing canvas contents
     * @throws IOException - Handling exceptions with input/output
     */
    public void updateServer(WritableImage wi) throws IOException{
        // Creating a new BufferedImage from the WritableImage
        // Allows image to be passed through Image Input/Output stream
        BufferedImage image = SwingFXUtils.fromFXImage(wi, null);
        
        // Iterate through all of the clients and update them individually
        ImageIO.write(image, "png", client.getOutputStream()); // Send image to client
        client.getOutputStream().flush();
    }
    
    /**
     * Run the class
     */
    @Override
    public void run() {
        // Create a new listener to always be reading for a message from server
        listener = new CollabListener(client, null);
        listenThread = new Thread(listener);
        listenThread.start();
    }
}
