package imagecomponent;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public abstract class ImageComponent extends JLabel{

	private static final long serialVersionUID = -3071360136132074019L;
	private ImageIcon imageIcon;
	private int height, width;
	
	// Take the path of the image, width and height to be displayed
	public ImageComponent(String path, int width, int height) {
		this.height = height;
		this.width = width;
		setImage(path);
	}
	
	// Get the image from the path and scale it based on the width and height specified and set it visible
	public void setImage(String path) {
		setVisible(false);
		imageIcon = new ImageIcon(path);
		setSize(width, height);
		setIcon(new ImageIcon(imageIcon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT)));
		setVisible(true);
	}
	
	// Define a general movement function to allow the Image to move
	public void moveTo(int pixel, String command) {
		if (command.toUpperCase().compareTo("RIGHT") == 0){
			moveRight(pixel);
		}
		if (command.toUpperCase().compareTo("LEFT") == 0){
			moveLeft(pixel);
		}
		if (command.toUpperCase().compareTo("UP") == 0){
			moveUp(pixel);
		}
		if (command.toUpperCase().compareTo("DOWN") == 0){
			moveDown(pixel);
		}
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public abstract void moveRight(int pixel);
	public abstract void moveLeft(int pixel);
	public abstract void moveUp(int pixel);
	public abstract void moveDown(int pixel);
		
}
