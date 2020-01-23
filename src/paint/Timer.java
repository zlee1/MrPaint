/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zdude
 */
public class Timer implements Runnable{
    int maxSeconds = 300;
    int seconds = maxSeconds; // Number of seconds until 
    int timeElapsed = 0; // Amount of time spent on a shape
    
    /**
     * Call the runTimer method
     */
    @Override
    public void run() {
        try {
            this.runTimer();
        } catch (InterruptedException ex) {
            Logger.getLogger(Timer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Complete an action every second
     * @throws InterruptedException 
     */
    public void runTimer() throws InterruptedException{
        do{
            Thread.sleep(1000L); // Sleep the thread for 1 second
            timeElapsed++;
            if(PaintClass.fileSelected()){
                seconds--; // Remove one from seconds for each iteration
                if(seconds < 0){ // Set seconds back to max if it drops below 0
                    seconds = maxSeconds;
                }
            }else{
                seconds = maxSeconds; // If no file is selected, set seconds equal to max seconds
            }
            PaintClass.setAutosaveLabel(seconds); // Update the label in the PaintClass
        }while(seconds >= 0); // Infinite loop
    }
    
    /**
     * Log the time spent on a tool
     * @param logInfo - String to log
     * @param opened - Whether or not a file had been opened
     * @throws IOException - Potential io exceptions from file writer
     */
    public void logTime(String logInfo, Boolean opened) throws IOException{
        FileWriter writer;
        try{
            // Create a new file in the src with the name of the computer name.
            writer = new FileWriter(System.getProperty("user.dir") + "/src/logFiles/" + getComputerName() + ".txt", true);
        }catch(Exception e){
            // Try again.
            writer = new FileWriter(System.getProperty("user.dir") + "/src/logFiles/unknown.txt", true);
        }
        
        // Write filename opened when the user opens an image
        // Write the time spent on a tool if the user switched tools.
        if(opened){
            writer.append(logInfo + " Opened\n");
        }else{
            writer.append(logInfo + ": " + timeElapsed + "\n");
            // Reset the time elapsed.
            timeElapsed = 0;
        }
        
        // Close file writer.
        writer.close();
    }
    
    /**
     * Find the computer's name
     * @return - name of the computer
     */
    private String getComputerName(){
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME"))
            return env.get("COMPUTERNAME");
        else if (env.containsKey("HOSTNAME"))
            return env.get("HOSTNAME");
        else
            return "UnknownComputer";
    }

    /**
     * Change the seconds variable
     * @param seconds - New value for seconds variable
     */
    public void setSeconds(int seconds){
        this.seconds = seconds;
    }
    
    /**
     * Change the max seconds variable
     * @param maxSeconds - New value for max seconds variable
     */
    public void setMaxSeconds(int maxSeconds){
        this.maxSeconds = maxSeconds;
        seconds = maxSeconds;
    }
    
    /**
     * Return the number of seconds remaining until autosave
     * @return - seconds
     */
    public int getSeconds(){
        return seconds;
    }
    
    /**
     * Return the max seconds variable
     * @return - maxSeconds
     */
    public int getMaxSeconds(){
        return maxSeconds;
    }
    
    /**
     * Return the time elapsed.
     * @return - timeElapsed
     */
    public int getTimeElapsed(){
        return timeElapsed;
    }
}
