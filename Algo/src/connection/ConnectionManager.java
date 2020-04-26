package connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import astarpathfinder.FastestPathThread;
import config.Constant;
import exploration.ExplorationThread;
import robot.RealRobot;

// This is the connection manager thread that communicates with the Rpi
public class ConnectionManager extends Thread{
	private static RealRobot robot;
	private static ConnectionManager connectionManager = null;
	private ConnectionSocket connectionSocket = ConnectionSocket.getInstance();
	private static Thread thread = null;
	private static ArrayList<String> buffer = new ArrayList<String>();
	private static AtomicBoolean running = new AtomicBoolean(false);
	private static String[] bufferableCommand = new String[] {Constant.IMAGE_ACK};
	private ConnectionManager() {

	}
	// Singleton
	public static ConnectionManager getInstance() {
		if (connectionManager == null) {
			connectionManager = new ConnectionManager();
		}
		return connectionManager;
	}
	
	// Initialise the realrobot here and connect to RPI
	public boolean connectToRPi(boolean simulate) {
		if (robot == null){
			robot = RealRobot.getInstance(simulate);
		}
			
		return connectionSocket.connectToRPI();
	}
	
	// Stop all thread and close the connection
	public void disconnectFromRPI() {
		if (ExplorationThread.getRunning()) {
			ExplorationThread.stopThread();
		}
		if (FastestPathThread.getRunning()) {
			FastestPathThread.stopThread();
		}
		connectionSocket.closeConnection();
		
	}
	
	// Start this thread and poll for the message from RPi
	public void start() {
		running.set(true);
		while(running.get()) {
			if (ExplorationThread.getRunning() || FastestPathThread.getRunning()) {
				try {
					thread.join();
				}
				catch (Exception e) {
					System.out.println("Error in start ConnectionManager");
				}
			}
			else {
				waitingForMessage();
			}
		}
	}
	
	// Stop this thread with this function
	public void stopCM() {
		running.set(false);
	}
	
	public static ArrayList<String> getBuffer(){
		return buffer;
	}
	
	public String waitingForMessage() {
		// Start Exploration/ Fastest Path/ Send_Arena/ Initializing/ Sensor Values
		String s = "";
		boolean complete = false;
		Pattern sensorPattern = Pattern.compile("\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+"); 
		
		// Check if received a valid message
		while (!complete) {
			robot.displayMessage("Waiting for orders", 2);
			s = this.connectionSocket.receiveMessage().trim();
			robot.displayMessage("Received message: " + s, 2);
			
			// Set the robot position only if the correct message is received and the program is not running exploration and fastest path
			if (!ExplorationThread.getRunning() && !FastestPathThread.getRunning() && s.contains(Constant.INITIALISING)) {
				Pattern p_s_s = Pattern.compile(Constant.INITIALISING + "[\\s]\\([1-9],[1-9],[0-3]{1}\\)");
				Pattern p_d_s = Pattern.compile(Constant.INITIALISING + "[\\s]\\([1][0-8],[1-9],[0-3]{1}\\)");
				Pattern p_s_d = Pattern.compile(Constant.INITIALISING + "[\\s]\\([1-9],[1][0-4],[0-3]{1}\\)");
				Pattern p_d_d = Pattern.compile(Constant.INITIALISING + "[\\s]\\([1][0-8],[1][0-4],[0-3]{1}\\)");
				if ((p_s_s.matcher(s).matches() || p_d_s.matcher(s).matches() || p_s_d.matcher(s).matches() || p_d_d.matcher(s).matches())){
					complete = true;
					String tmp = s.replace(Constant.INITIALISING + " (", "");
					tmp = tmp.replace(")", "");
					String[] arr = tmp.trim().split(",");
					robot.initialise(Integer.parseInt(arr[1]), Integer.parseInt(arr[0]), (Integer.parseInt(arr[2]) + 1 ) % 4);
					s = "Successfully set the robot's position: " + Integer.parseInt(arr[0]) + 
						"," + Integer.parseInt(arr[1]) + "," + Integer.parseInt(arr[2]);
					robot.displayMessage(s, 2);
				}
			}
			
			// Start exploration only if the correct message is received and the program is not running exploration and fastest path
			else if (!ExplorationThread.getRunning() && !FastestPathThread.getRunning() && s.equals(Constant.START_EXPLORATION) ) {
				s = "Exploration Started";
				thread = ExplorationThread.getInstance(robot, Constant.TIME, Constant.PERCENTAGE, Constant.SPEED, Constant.IMAGE_REC);
				thread.setPriority(Thread.MAX_PRIORITY);
				
				complete = true;
				
				try {
					thread.join();
				}
				catch(Exception e) {
					System.out.println("Error in start exploration in ConnectionManager");
				}
			}
			
			// Start fastestpath only if the correct message is received and the program is not running exploration and fastest path
			else if (!ExplorationThread.getRunning() && !FastestPathThread.getRunning() && s.equals(Constant.FASTEST_PATH) ){
				thread = FastestPathThread.getInstance(robot, robot.getWaypoint(), 1);
				thread.setPriority(Thread.MAX_PRIORITY);
				
				s = "Fastest Path started";
				try {
					thread.join();
					
				}
				catch(Exception e) {
					System.out.println("Error in fastest path in ConnectionManager");
				}
				complete = true;
			}
			
			// Set waypoint only if the correct message is received and the program is not running exploration and fastest path
			else if (!ExplorationThread.getRunning() && !FastestPathThread.getRunning() && 
					s.contains(Constant.SETWAYPOINT)) {
				Pattern wp_s_s = Pattern.compile(Constant.SETWAYPOINT + " \\([1-9],[1-9]\\)");
				Pattern wp_d_s = Pattern.compile(Constant.SETWAYPOINT + " \\([1-9],[1][0-8]\\)");
				Pattern wp_s_d = Pattern.compile(Constant.SETWAYPOINT + " \\([1][0-4],[1-9]\\)");
				Pattern wp_d_d = Pattern.compile(Constant.SETWAYPOINT + " \\([1][0-4],[1][0-8]\\)");
				if ((wp_s_s.matcher(s).matches() || wp_d_s.matcher(s).matches() || wp_s_d.matcher(s).matches() || wp_d_d.matcher(s).matches())){
					complete = true;
					String tmp = s.replace(Constant.SETWAYPOINT + " (", "");
					tmp = tmp.replace(")", "");
					String[] arr = tmp.trim().split(",");
					robot.setWaypoint(Integer.parseInt(arr[1]), Integer.parseInt(arr[0]));
					s = "Successfully received the waypoint: " + Integer.parseInt(arr[0]) + 
							"," + Integer.parseInt(arr[1]);
					robot.displayMessage(s, 2);
				}
			}
			
			// Send mdf arena only if the correct message is received and the program is not running exploration and fastest path
			else if (s.equals(Constant.SEND_ARENA)) {
				String[] arr = robot.getMDFString();
				connectionSocket.sendMessage("{\"map\":[{\"explored\": \"" + arr[0] + "\",\"length\":" + arr[1] + ",\"obstacle\":\"" + arr[2] +
						"\"}]}");
				robot.displayMessage("{\"map\":[{\"explored\": \"" + arr[0] + "\",\"length\":" + arr[1] + ",\"obstacle\":\"" + arr[2] +
						"\"}]}", 2);
				complete = true;
			}
			
			// Store valid message if the program is running exploration or fastest path
			else if (Arrays.asList(bufferableCommand).contains(s) || sensorPattern.matcher(s).matches()) {
				// If the command is an acknowledgement or sensor values, put into buffer
				buffer.add(s);
				System.out.println("Placed command" + s + " into buffer");
			}
			else {
				System.out.println("Unknown command: " + s);
			}
			
		}
		return s;
	}
}
