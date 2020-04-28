package connection;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import config.Constant;

public class ConnectionServer {
    private Socket          socket   = null; 
    private ServerSocket    server   = null; 
    private DataInputStream input    =  null; 
    private DataOutputStream output = null;
    private static ConnectionServer cs = null;
    
	private ConnectionServer() {
		
	}
	
	public static ConnectionServer getInstance() {
		if (cs == null) {
			cs = new ConnectionServer();
			cs.startServer();
		}
		return cs;
	}
	
	public void startServer() {
        // starts server and waits for a connection 
        try
        { 
            server = new ServerSocket(Constant.PORT); 
            System.out.println("Server started"); 
  
            System.out.println("Waiting for a client ..."); 
            
            socket = server.accept(); 
            System.out.println("Client accepted"); 
            
            input = new DataInputStream( 
                    new BufferedInputStream(socket.getInputStream())); 
            output = new DataOutputStream(socket.getOutputStream());
        }
        catch(UnknownHostException UHEx) { 
    		System.out.println("UnknownHostException in ConnectionServer startServer Function"); 
        } 
    	catch (IOException IOEx) {
    		System.out.println("IOException in ConnectionServer startServer Function");
    	}
	}
	
    public void sendMessage(String message) {
    	try {
    		output.write(message.getBytes());
//    		output.writeUTF(message);
    		output.flush();
    		System.out.println('"' + message + '"' + " sent successfully");
    	}
    	catch (IOException IOEx) {
    		System.out.println("IOException in ConnectionServer sendMessage Function");
    	}
    }
    
    public String receiveMessage() {
    	String message = "";
    	byte[] byteData = new byte[Constant.BUFFER_SIZE];
    	try {
    		int size = 0;
    		input.read(byteData);
    		
    		// This is to get rid of junk bytes
    		while (size < Constant.BUFFER_SIZE) {
    			if (byteData[size] == 0) {
    				break;
    			}
    			size++;
    		}
    		message = new String(byteData, 0, size, "UTF-8");
    	}
    	catch (IOException IOEx) {
    		System.out.println("IOException in ConnectionServer receiveMessage Function");
    	}
    	return message;
    }
    
    public void closeConnection() {
    	if (socket != null) {
    		try {
    			socket.close();
    			input.close();
    			output.close();
    			socket = null;
    			input = null;
    			output = null;
    		}
    		catch (IOException IOEx) {
        		System.out.println("IOException in ConnectionServer closeConnection Function");
        	}
    	}
    }
}
