package connection;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import config.Constant;

public class Server {
	// This server basically test communication for the main system
	public static void main(String args[]) {
		ConnectionServer server = ConnectionServer.getInstance();
		String message = "";
		String sensorMessage = "";
		boolean acknowledge = true;
		Scanner sc = new Scanner (System.in);
		boolean exploring = false, completed = false, fastestpath = false;
		int pos[] = new int[] {1,1};
		int direction = 2, count = 0;
		while (!completed) {
//			message = "ES|";
			System.out.print("Enter your command: ");
			message = sc.nextLine();
			server.sendMessage(message);
			if (message.equals(Constant.START_EXPLORATION)) {
				exploring = true;
			}
			if (message.equals(Constant.FASTEST_PATH)) {
				fastestpath = true;
			}
			if (message.equals(Constant.SEND_ARENA)) {
				completed = true;
			}
			while (exploring || fastestpath) {
				message = server.receiveMessage();
				acknowledge = true;
				System.out.println("Message received: " + message);
				Pattern p = Pattern.compile("W\\d+[|]");

				if (p.matcher(message).matches()) {
					pos[0] = pos[0] + Constant.SENSORDIRECTION[direction][0];
					pos[1] = pos[1] + Constant.SENSORDIRECTION[direction][1];
					System.out.println(pos[0] + ", " + pos[1]);
					
//						System.out.println("Enter sensor value:");
//						message = sc.nextLine();
				}
				else if (message.equals(Constant.TURN_LEFT)) {
					direction = (direction + 3)%4;
				}
				else if (message.equals(Constant.TURN_RIGHT)) {
					direction = (direction + 1)%4;
				}
				else if(message.equals(Constant.SENSE_ALL) || message.equals(Constant.CALIBRATE) || message.equals(Constant.RIGHTALIGN)) {
					System.out.println(pos[0] + ", " + pos[1]);
				}
 				else if (message.equals(Constant.END_TOUR) || message.contains("N")) {

					exploring = false;
					fastestpath = false;
					acknowledge = false;
				}
				else {
					acknowledge = false;
					System.out.println("Error.");
				}
				
				if (acknowledge) {
					if ((pos[0] == 1 && pos[1] == 12) || (pos[0] == 17 && pos[1] == 13) || (pos[0] == 18 && pos[1] == 2) || (pos[0] == 2 && pos[1] == 1)){
						sensorMessage = "" + Constant.SENSOR_RANGES[0][1] + "|" + Constant.SENSOR_RANGES[1][1] + "|" + Constant.SENSOR_RANGES[2][1] + 
								"|" + Constant.SENSOR_RANGES[3][1] + "|" + Constant.SENSOR_RANGES[4][1] + "|" + Constant.SENSOR_RANGES[5][1] + "|1";
					}
					else if (((pos[0] == 1 && pos[1] == 13) || (pos[0] == 18 && pos[1] == 13) || (pos[0] == 18 && pos[1] == 1)) && count < 1){
						sensorMessage = "" + Constant.SENSOR_RANGES[0][0] + "|" + Constant.SENSOR_RANGES[1][0] + "|" + Constant.SENSOR_RANGES[2][0]
								+ "|" + Constant.SENSOR_RANGES[3][0] + "|" + Constant.SENSOR_RANGES[4][0] + "|" + Constant.SENSOR_RANGES[5][0] + "|1";
						count++;
					}
					else {
						sensorMessage = "84.0|84.0|84.0|3.0|3.0|84.0|1";
						count = 0;
					}
					try {
						TimeUnit.MILLISECONDS.sleep(1000);
					}
					catch (Exception e) {
						System.out.println(e.getMessage());
					}
					server.sendMessage(sensorMessage);
				}
			}
		}
		System.out.println(server.receiveMessage());
	}
}
