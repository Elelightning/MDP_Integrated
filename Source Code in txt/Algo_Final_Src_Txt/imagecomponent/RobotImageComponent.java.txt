package imagecomponent;
import config.Constant;

public class RobotImageComponent extends ImageComponent{

	private static final long serialVersionUID = 5944902400245422120L;
	
	// Create the image component for the robot
	public RobotImageComponent(String path, int width, int height) {
		super(path, width, height);
	}
	
	// Move in the direction by how many pixel, the smaller pixel will result in smoother movement
	public void moveRight(int pixel) {
		int x = (int) super.getLocation().getX();
		int y = (int) super.getLocation().getY();
		if (x + pixel < Constant.WIDTH - (Constant.GRIDWIDTH * 3 - Constant.ROBOTWIDTH)/2) {
			setLocation(x + pixel, y);
		}
	}
	
	public void moveLeft(int pixel) {
		int x = (int) super.getLocation().getX();
		int y = (int) super.getLocation().getY();
		if (x - pixel >= Constant.MARGINLEFT + (Constant.GRIDWIDTH * 3 - Constant.ROBOTWIDTH)/2) {
			setLocation(x - pixel, y);
		}
	}
	
	public void moveUp(int pixel) {
		int x = (int) super.getLocation().getX();
		int y = (int) super.getLocation().getY();
		if (y - pixel >= Constant.MARGINTOP + (Constant.GRIDHEIGHT * 3 - Constant.ROBOTHEIGHT)/2) {
			setLocation(x, y - pixel);
		}
	}
	
	public void moveDown(int pixel) {
		int x = (int) super.getLocation().getX();
		int y = (int) super.getLocation().getY();
		if (y  + pixel < Constant.HEIGHT - (Constant.GRIDHEIGHT * 3 - Constant.ROBOTHEIGHT)/2) {
			setLocation(x, y + pixel);
		}
	}

}
