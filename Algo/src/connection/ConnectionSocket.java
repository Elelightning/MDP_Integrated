package connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import config.Constant;

public class ConnectionSocket {
    // initialize socket and input output streams 
    private Socket socket              = null; 
    private InputStream  din     = null; 
    private PrintStream dout    = null;
    private static ConnectionSocket cs = null;
    private static AtomicBoolean connected = new AtomicBoolean(false);
    private static final AtomicBoolean debug = new AtomicBoolean(false);
    
    private ConnectionSocket() {
    	
    }
    
    public static ConnectionSocket getInstance() {
    	if (cs == null) {
    		cs = new ConnectionSocket();
    		cs.connectToRPI();
    	}
    	return cs;
    }
    
    public static boolean checkConnection() {
    	if (cs == null) {
    		return false;
    	}
    	return true;
    }
    
    public boolean connectToRPI() {
    	boolean result = true;
    	if (socket == null) {
	    	try {
	    		socket = new Socket(Constant.IP_ADDRESS, Constant.PORT);	
	    		System.out.println("Connected to " + Constant.IP_ADDRESS + ":" + Integer.toString(Constant.PORT));
	    		din  = socket.getInputStream(); 
	    		dout = new PrintStream(socket.getOutputStream()); 
	    		connected.set(true);
	    		
	    	}
	    	catch(UnknownHostException UHEx) { 
	    		System.out.println("UnknownHostException in ConnectionSocket connectToRPI Function"); 
	    		result = false;
	        } 
	    	catch (IOException IOEx) {
	    		System.out.println("IOException in ConnectionSocket connectToRPI Function");
	    		result = false;
	    	}
    	}
    	return result;
    }
    
    public void sendMessage(String message) {
    	try {
    		dout.write(message.getBytes());
    		dout.flush();
    		
    		if (debug.get()) {
    			System.out.println('"' + message + '"' + " sent successfully");
    		}
    	}
    	catch (IOException IOEx) {
    		System.out.println("IOException in ConnectionSocket sendMessage Function");
    	}
    }
    
    // Get message from buffer
    public String receiveMessage() {

    	byte[] byteData = new byte[Constant.BUFFER_SIZE];
    	try {
    		int size = 0;
    		while (din.available() == 0 && connected.get()) {
    			try {
    				ConnectionManager.getInstance().join(1);
    			}
    			catch(Exception e) {
    				System.out.println("Error in receive message");
    			}
    		}
    		din.read(byteData);
    		
    		// This is to get rid of junk bytes
    		while (size < Constant.BUFFER_SIZE) {
    			if (byteData[size] == 0) {
    				break;
    			}
    			size++;
    		}
    		String message = new String(byteData, 0, size, "UTF-8");

    		return message;
    	}
    	catch (IOException IOEx) {
    		System.out.println("IOException in ConnectionSocket receiveMessage Function");
    	}
    	return "Error";
    }
    
    public void closeConnection() {
    	if (socket != null) {
    		try {
    			dout.close();
    			socket.close();
    			din.close();
    			dout = null;
    			socket = null;
    			din = null;
    			connected.set(false);
    			System.out.println("Successfully closed the ConnectionSocket.");
    		}
    		catch (IOException IOEx) {
        		System.out.println("IOException in ConnectionSocket closeConnection Function");
        	}
    	}
    }
    
    public static void setDebugTrue() {
    	debug.set(true);
    }
    
    public static boolean getDebug() {
    	return debug.get();
    }
}