/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;

/**
 *
 * @author zdude
 */
public class RossServer implements Runnable{
    private ServerSocket server; // Server
    private ArrayList<Socket> clientList = new ArrayList<>(); // List of all connected clients
    private ArrayList<ObjectOutputStream> streamList = new ArrayList<>(); // List of all clients' ObjectOutputStreams
    private ListenServer listener; // Listener instance
    private Thread listenThread; // Listen thread
    
    /**
     * Create a new instance of RossServer
     * @throws UnknownHostException - Handle exception for unknown hose
     * @throws IOException - Input/output exception
     */
    public RossServer() throws UnknownHostException, IOException{
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
        for (int i = 0; i < clientList.size(); i++) {
            ImageIO.write(image, "png", streamList.get(i)); // Send image to client
            streamList.get(i).flush();
        }
    }
    
    /**
     * Add client objects to their respective ArrayLists
     * @param client - Client to add
     * @throws IOException - Handling exceptions with input/output
     */
    public void addToList(Socket client) throws IOException{
        clientList.add(client); // Add client to list of clients
        streamList.add(new ObjectOutputStream(client.getOutputStream())); // Add client output stream to list of streams
        
        listener = new ListenServer(this); // Create a new listener based off of current class
        listenThread = new Thread(listener); // Create listener thread
        listenThread.start(); // Start thread
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
        Socket client;
        client = this.server.accept(); // Client object is the next client to connect
        clientList.add(client); // Add client to list of clients
        streamList.add(new ObjectOutputStream(client.getOutputStream())); // Add client output stream to list of streams
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
            RossServer bobRoss = this; // Create a new server
            
            bobRoss.listen(); // Listen for a new client
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(RossServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
