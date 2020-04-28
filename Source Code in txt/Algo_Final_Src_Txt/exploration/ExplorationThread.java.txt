package exploration;

import config.Constant;
import connection.ConnectionSocket;
import robot.Robot;
import robot.SimulatorRobot;

import java.util.concurrent.atomic.AtomicBoolean;

public class ExplorationThread extends Thread{
	private Robot r;
	private int time;
	private int percentage;
	private int speed;
	private boolean image_recognition;

	private static final AtomicBoolean running = new AtomicBoolean(false);
	private static final AtomicBoolean completed = new AtomicBoolean(false);
	private static ExplorationThread thread = null;

	private ExplorationThread(Robot r, int time, int percentage, int speed, boolean image_recognition) {
		super("ExplorationThread");
		this.r = r;
		this.time = time;
		this.percentage = percentage;
		this.speed = speed;
		this.image_recognition = image_recognition;
		start();
	}
	
	public void run() {
		running.set(true);
		
		// Check if it is the simulator mode
		boolean isSimulated = r.getClass().equals(SimulatorRobot.class);
		Exploration e = new Exploration();
		e.Exploration(r, time, percentage, speed, image_recognition);
		if (running.get()) {
			completed.set(true);
		}
		else {
			completed.set(false);
		}
		stopThread();
		if (ConnectionSocket.checkConnection()) {
			// Send the MDF String at the end when it is completed
			String[] arr2 = r.getMDFString();
			ConnectionSocket.getInstance().sendMessage("M{\"map\":[{\"explored\": \"" + arr2[0] + "\",\"length\":" + arr2[1] + ",\"obstacle\":\"" + arr2[2] +
					"\"}]}");
			ConnectionSocket.getInstance().sendMessage(Constant.END_TOUR);
		}
		else {
			// Display onto the User Interface, it is completed when it is the simulator
			if (isSimulated) {
				SimulatorRobot sr = (SimulatorRobot) r;
				sr.displayMessage("Exploration Completed", 1);
			}
		}
	}

	public static ExplorationThread getInstance(Robot r, int time, int percentage, int speed, boolean image_recognition) {
		if (thread == null) {
			thread = new ExplorationThread(r, time, percentage, speed, image_recognition);
		}
		return thread;
	}

	public static boolean getRunning() {
		return running.get();
	}

	public static void stopThread() {
		running.set(false);
		thread = null;
	}

	public static boolean getCompleted() {
		return completed.get();
	}
}
