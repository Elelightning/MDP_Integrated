package robot;

import java.awt.Image;
import java.util.ArrayList;

import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import config.Constant;
import connection.ConnectionManager;
import connection.ConnectionSocket;
import map.Map;
import sensor.RealSensor;

public class RealRobot extends Robot{
	private ConnectionSocket connectionSocket = ConnectionSocket.getInstance();
	private static RealRobot r = null;
	private int numOfCount = 0;
	private SimulatorRobot sr = null;
	
	private RealRobot(boolean simulator) {
		super();
		this.map = new Map();
		initialise(1,1,Constant.SOUTH);
		this.sensor = new RealSensor();
		if (simulator) {
			JFrame frame = new JFrame("RealRun");
			frame.setIconImage(new ImageIcon(Constant.SIMULATORICONIMAGEPATH).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
			frame.setLayout(null);
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.setSize(Constant.MARGINLEFT + Constant.GRIDWIDTH * Constant.BOARDWIDTH + 100, Constant.MARGINTOP + Constant.GRIDHEIGHT * Constant.BOARDHEIGHT + 100);
			sr = new SimulatorRobot(frame, this.x, this.y, this.getDirection());
			frame.setVisible(true);
			sr.displayMessage("Disabled all buttons for the real run.", 2);
		}
	}
	
	// Singleton and accept a boolean variable to decide if the simulator UI will be displayed
	public static RealRobot getInstance(boolean simulator) {
		if (r == null) {
			r = new RealRobot(simulator);
		}
		return r;
	}
	
	// Difference between this function and acknowledge is that it returns the sensor value arr after sending sense message to Ardurino
	@Override
	protected String[] getSensorValues() {
		// FR, FM, FL, RB, RF, LF is the arrangement of our sensor values
		// Accept only all integer sensor values or all double sensor values
		Pattern sensorPattern = Pattern.compile("\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+");
		Pattern sensorPattern2 = Pattern.compile("\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+");
		String s;
		String[] arr = null;
		connectionSocket.sendMessage(Constant.SENSE_ALL);
		if (sr != null) {
			sr.displayMessage("Sent message: " + Constant.SENSE_ALL, 1);
		}
		boolean completed = false;

		while (!completed) {
			s = connectionSocket.receiveMessage().trim();
			if (sensorPattern.matcher(s).matches() || sensorPattern2.matcher(s).matches()) {
				arr = s.split("\\|");
				break;
			}
		}
		System.arraycopy(arr, 0, sensorValues, 0, 6);
		this.sensePosition[0] = x;
		this.sensePosition[1] = y;
		this.sensePosition[2] = getDirection();
		return arr;
	}
	
	// Receive sensor values to acknowledge the action had performed by the Ardurino
	private boolean acknowledge(){
		Pattern sensorPattern = Pattern.compile("\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+");
		Pattern sensorPattern2 = Pattern.compile("\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+");
		String s;
		String[] arr = null;

		boolean completed = false;

		while (!completed) {
			s = connectionSocket.receiveMessage().trim();
			if (ConnectionSocket.getDebug()) {
				System.out.println("In get sensor values");
				System.out.println(s);
			}
			if (sr != null) {
				sr.displayMessage("Received Message: " + s, 1);
			}
			if (sensorPattern.matcher(s).matches() || sensorPattern2.matcher(s).matches()) {
				arr = s.split("\\|");
				break;
			}
		}
		
		// 1 indicates the Ardurino has performed the action successfully and hence return true
		if (Integer.parseInt(arr[6]) == 1) {
			System.arraycopy(arr, 0, sensorValues, 0, 6);
			this.sensorValues = arr;
			this.sensePosition[0] = x;
			this.sensePosition[1] = y;
			this.sensePosition[2] = getDirection();
			sendMDPString();
			return true;
		}
		
		// 0 indicates the Ardurino has failed to performed the action and hence return true
		System.arraycopy(arr, 0, sensorValues, 0, 6);
		this.sensePosition[0] = x;
		this.sensePosition[1] = y;
		this.sensePosition[2] = getDirection();
		sendMDPString();
		return false;
		
	}
	
	// Send the mdf string every 4 times we receive the sensor value
	public void sendMDPString() {
		if (this.numOfCount > 3) {
			String[] arr2 = this.getMDFString();
			connectionSocket.sendMessage("M{\"map\":[{\"explored\": \"" + arr2[0] + "\",\"length\":" + arr2[1] + ",\"obstacle\":\"" + arr2[2] +
					"\"}]}");
			if (sr != null) {
				sr.displayMessage("Sent message: M{\"map\":[{\"explored\": \"" + arr2[0] + "\",\"length\":" + arr2[1] + ",\"obstacle\":\"" + arr2[2] +
					"\"}]}", 1);
			}
			this.numOfCount = 0;
		}
		else {
			this.numOfCount++;
		}
	}
	
	// Send the forward message with how many steps and update the robot position
	@Override
	public void forward(int step) {
		connectionSocket.sendMessage("W" + Integer.toString(step)+ "|");
		this.x = checkValidX(this.x + Constant.SENSORDIRECTION[this.getDirection()][0]);
		this.y = checkValidX(this.y + Constant.SENSORDIRECTION[this.getDirection()][1]);
		if (sr != null) {
			sr.forward(step);
			sr.displayMessage("Sent message: W" + Integer.toString(step)+ "|", 1);
		}
		toggleValid();
		if (!acknowledge()) {
			this.x = checkValidX(this.x - Constant.SENSORDIRECTION[this.getDirection()][0]);
			this.y = checkValidX(this.y - Constant.SENSORDIRECTION[this.getDirection()][1]);
			if (sr != null) {
				sr.backward(step);
			}
		}
	}
	
	// Send the rotate right message and update the robot direction
	@Override
	public void rotateRight() {
		connectionSocket.sendMessage(Constant.TURN_RIGHT);
		setDirection((this.getDirection() + 1) % 4);
		if (sr != null) {
			sr.rotateRight();
			sr.displayMessage("Sent message: " + Constant.TURN_RIGHT, 1);
		}
		acknowledge();
	}
	
	// Send the rotate left message and update the robot direction
	@Override
	public void rotateLeft() {
		connectionSocket.sendMessage(Constant.TURN_LEFT);
		setDirection((this.getDirection() + 3) % 4);
		if (sr != null) {
			sr.rotateLeft();
			sr.displayMessage("Sent message: " + Constant.TURN_LEFT, 1);
		}
		acknowledge();
	}
	
	// Send the image position and the command to take picture
	public boolean captureImage(int[][] image_pos) {
		connectionSocket.sendMessage("C["+ image_pos[0][1] + "," + image_pos[0][0] + "|" + image_pos[1][1] + "," + image_pos[1][0] +
				"|" + image_pos[2][1] + "," + image_pos[2][0] + "]"); // This is left middle right. x and y is inverted in Real Run.
		
		if (sr != null) {
			sr.captureImage(image_pos);
			sr.displayMessage("Sent message: C["+ image_pos[0][1] + "," + image_pos[0][0] + "|" + image_pos[1][1] + "," + image_pos[1][0] +
				"|" + image_pos[2][1] + "," + image_pos[2][0] + "]", 1);
		}
		boolean completed = false;
		String s;
		ArrayList <String> buffer = ConnectionManager.getBuffer();
		while (!completed) {
			s = connectionSocket.receiveMessage().trim();
			completed = checkImageAcknowledge(s);
			if (completed && s.equals(Constant.IMAGE_ACK)) {
				System.out.println(s);
				return false;
			}
			else if (completed && s.equals(Constant.IMAGE_STOP)){
				System.out.println(s);
				return true;
			}
			else {
				for (int i = 0; i < buffer.size(); i++) {
					completed = checkImageAcknowledge(buffer.get(i));
					if (completed) {
						buffer.remove(i);
						break;
					}
				}
			}
			System.out.println(s);
		}
		return false;
	}
	
	// Get the image acknowledgement from RPI
	private boolean checkImageAcknowledge(String s) {
		if (s.equals(Constant.IMAGE_ACK) || s.equals(Constant.IMAGE_STOP)) {
			return true;
		}
		return false;
	}
	
	// Send the calibrate command
	public void calibrate() {
		connectionSocket.sendMessage(Constant.CALIBRATE);
		if (sr != null) {
			sr.displayMessage("Sent message: " + Constant.CALIBRATE, 1);
		}
		acknowledge();
	}

	// Send the right align command
	public void right_align() {
		connectionSocket.sendMessage(Constant.RIGHTALIGN);
		if (sr != null) {
			sr.displayMessage("Sent message: " + Constant.RIGHTALIGN, 1);
		}
		acknowledge();
	}
	
	// This will override the update map from the robot class
	public int[] updateMap() {
		int[] isObstacle = super.updateMap();
		
		// If there is a simulator, this will display the robot movement on screen
		if (sr != null) {
			sr.setMap(this.getMap());
		}
		return isObstacle;
	}
	
	public void displayMessage(String s, int mode) {
		if (sr != null) {
			sr.displayMessage(s, mode);
		}
		else {
			System.out.println(s);
		}
	}
}
