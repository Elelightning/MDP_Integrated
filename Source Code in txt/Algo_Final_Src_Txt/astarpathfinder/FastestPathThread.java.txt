package astarpathfinder;

import exploration.ExplorationThread;
import robot.Robot;
import robot.SimulatorRobot;

import java.util.concurrent.atomic.AtomicBoolean;

import config.Constant;
import connection.ConnectionSocket;

public class FastestPathThread extends Thread {
    private Robot r;
    private int[] waypoint;
    private int speed;

    private static final AtomicBoolean running = new AtomicBoolean(false);
    private static final AtomicBoolean completed = new AtomicBoolean(false);
    private static FastestPathThread thread = null;

    private FastestPathThread(Robot r, int[] waypoint, int speed) {
        super("FastestPathThread");
        this.r = r;
        this.speed = speed;
        this.waypoint = waypoint;
        start();
    }

    public void run() {
    	running.set(true);
    	
    	// Check if it is the simulator mode
    	boolean isSimulated = r.getClass().equals(SimulatorRobot.class);
        FastestPath fp = new FastestPath();
        fp.FastestPath(r, waypoint, Constant.END, speed, true, true);
        if (running.get()) {
        	completed.set(true);
        }
        else {
        	completed.set(false);
        }
        stopThread();
        
		if (ConnectionSocket.checkConnection()) {
			ConnectionSocket.getInstance().sendMessage(Constant.END_TOUR);
			r.displayMessage("Sent message: " + Constant.END_TOUR, 1);
		}
		else {
			// Display onto the User Interface, it is completed when it is the simulator
			if (isSimulated) {
				SimulatorRobot sr = (SimulatorRobot) r;
				sr.displayMessage("Fastest Path Completed", 1);
				sr.setDirection(Constant.NORTH);
			}
		}
    }

    public static FastestPathThread getInstance(Robot r, int[] waypoint, int speed) {
        if (thread == null) {
            thread = new FastestPathThread(r, waypoint, speed);
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
