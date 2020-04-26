package timertask;
import java.util.TimerTask;
import imagecomponent.ImageComponent;

public class MoveImageTask extends TimerTask{
	private ImageComponent item;
	private String command;
	private int pixel;
	
	public MoveImageTask(ImageComponent i, String s, int p) {
		item = i;
		command = s;
		pixel = p;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		item.moveTo(pixel, command);
	}
	
	
}
