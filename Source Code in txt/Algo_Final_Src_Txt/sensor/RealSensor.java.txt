package sensor;

import map.Map;

public class RealSensor extends Sensor{
	
	/* This class basically instantiate the sensor class for Real Run.*/
	private int x;
	private int y;
	private int direction;
	private String arr[] = new String[6];

	public RealSensor(){
		super();
	}
	
	public RealSensor(int x, int y, int direction){
		this.x = x;
		this.y = y;
		this.direction = direction;
	}

	@Override
	public String[] getAllSensorsValue(int x, int y, int direction) {
		if (this.x == x && this.y == y && this.direction == direction){
			this.x = x;
			this.y = y;
			this.direction = y;
			return arr;
		}
		return null;
	}//

	
	@Override
	// In the real run, the robot cannot know what is the true map.
	public Map getTrueMap() {
		return null;
	}

	@Override
	// This is not valid action for the robot
	public void setTrueMap(Map map) {

	}

}