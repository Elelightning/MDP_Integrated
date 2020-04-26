package timertask;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import astarpathfinder.FastestPathThread;
import exploration.ExplorationThread;
import simulator.AddJButtonActionListener;

public class EnableButtonTask extends TimerTask{
	private AddJButtonActionListener AL;
	// This class basically enables all the button after the movement
	public EnableButtonTask(AddJButtonActionListener AL) {
		this.AL = AL;
	}
	public void run() {
		while (ExplorationThread.getRunning() || FastestPathThread.getRunning()) {
			try {
				TimeUnit.SECONDS.sleep(1);
			}
			catch(Exception e) {
				System.out.println("Exception in EnableButtonTask.java");
			}
		}
		AL.enableButtons();
	}
}
