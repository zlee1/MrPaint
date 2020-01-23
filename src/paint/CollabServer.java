/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;

/**
 *
 * @author zdude
 */
public class CollabServer implements Runnable{
    private ServerSocket server; // Server
    private Socket client;
    private CollabListener listener; // Listener instance
    private Thread listenThread; // Listen thread
    
    /**
     * Create a new instance of RossServer
     * @throws UnknownHostException - Handle exception for unknown hose
     * @throws IOException - Input/output exception
     */
    public CollabServer() throws UnknownHostException, IOException{
        // Create a new ServerSocket from current IP address
        this.server = new ServerSocket(0, 1, InetAddress.getLocalHost());
    }
    
    /**
     * Update all of the clients with an updated version of the host's canvas
     * @param wi - WritableImage containing canvas contents
     * @throws IOException - Handling exceptions with input/output
     */
    public void updateClient(WritableImage wi) throws IOException{
        // Creating a new BufferedImage from the WritableImage
        // Allows image to be passed through Image Input/Output stream
        BufferedImage image = SwingFXUtils.fromFXImage(wi, null);
        
        // Iterate through all of the clients and update them individually
        ImageIO.write(image, "png", client.getOutputStream()); // Send image to client
        client.getOutputStream().flush();
    }
    
    /**
     * Return ServerSocket
     * @return - The current server as a ServerSocket
     */
    public ServerSocket getServer(){
        return server;
    }
    
    /**
     * Listen for a new client connection
     * @throws IOException - Input/output exception
     */
    private void listen() throws IOException, InterruptedException{
        client = this.server.accept(); // Client object is the next client to connect
    }
    
    /**
     * Return address of server
     * @return - InetAddress of server
     */
    public InetAddress getServerAddress(){
        return this.server.getInetAddress();
    }
    
    /**
     * Return port of server
     * @return - Server port
     */
    public int getPort(){
        return this.server.getLocalPort();
    }

    /**
     * Run the class
     */
    @Override
    public void run() {
        try {
            CollabServer s = this; // Create a new server
            
            s.listen(); // Listen for a new client
            
            // Create a listener to always be listening for a message from client
            listener = new CollabListener(client, this);
            listenThread = new Thread(listener);
            listenThread.start();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(RossServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
