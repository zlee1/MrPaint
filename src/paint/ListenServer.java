/*
 * ListenServer is always listening for new client connections to the RossServer
 * it does this by existing in a separate thread and essentially creating a new
 * server based off of the currently running server from the RossServer class.
 * Whenever the ListenServer receives a client connection, it updates a list in
 * the RossServer, which then recreates the ListenServer, causing an endless loop
 * of new connections being added.
 */
package paint;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zdude
 */
public class ListenServer implements Runnable{
    private ServerSocket server; // Currently running server
    public RossServer rServer; // Instance of server's overarching class
    
    /**
     * Constructor that takes RossServer as an argument
     * @param rServer 
     */
    public ListenServer(RossServer rServer){
        this.rServer = rServer; // Store RossServer for later use
        this.server = rServer.getServer(); // Store ServerSocket of RossServer
    }
    
    /**
     * Listen for a new client connection
     * @throws IOException - Input/output exception
     */
    private void listen() throws IOException, InterruptedException{
        Socket client;
        client = this.server.accept(); // Client object is the next client to connect
        rServer.addToList(client); // Add client to list
    }
    
    @Override
    public void run() {
        try {
            listen(); // Listen for a new client connection
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ListenServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
