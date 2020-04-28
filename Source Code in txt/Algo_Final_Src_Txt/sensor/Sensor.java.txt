package sensor;
import config.Constant;
import map.Map;

public abstract class Sensor {
	// More like sensor interface class to arduino

	// Far sensor range = 80-150
	// Short sensor range = 10-50
	// FR, FC, FL, RB, RF, LF

	public static int[][] sensorLocation = new int[6][2];
	public static int[][] sensorDirection = new int[3][2]; // Assume NORTH is the direction, Represent front, left and right sensor direction

	// Calculates the sensor direction and the position of all my sensors based on offset from my robot position 
	public static void updateSensorDirection(int direction) {
		int i = direction;
		sensorDirection[0] = Constant.SENSORDIRECTION[i];

		sensorLocation[0] = new int[] {sensorDirection[0][0]+ Constant.SENSORDIRECTION[(i+1) % Constant.SENSORDIRECTION.length][0],
				sensorDirection[0][1]+ Constant.SENSORDIRECTION[(i+1) % Constant.SENSORDIRECTION.length][1]};
		sensorLocation[1] = sensorDirection[0];
		sensorLocation[2] = new int[] {sensorDirection[0][0] + Constant.SENSORDIRECTION[((i-1) % Constant.SENSORDIRECTION.length + Constant.SENSORDIRECTION.length) % Constant.SENSORDIRECTION.length][0],
				sensorDirection[0][1] + Constant.SENSORDIRECTION[((i-1) % Constant.SENSORDIRECTION.length + Constant.SENSORDIRECTION.length) % Constant.SENSORDIRECTION.length][1]};
		sensorLocation[3] = new int[] {Constant.SENSORDIRECTION[(i+1) % Constant.SENSORDIRECTION.length][0] +
				Constant.SENSORDIRECTION[(i+2) % Constant.SENSORDIRECTION.length][0],
				Constant.SENSORDIRECTION[(i+1) % Constant.SENSORDIRECTION.length][1] +
						Constant.SENSORDIRECTION[(i+2) % Constant.SENSORDIRECTION.length][1]};
		sensorLocation[4] = new int[] {sensorLocation[0][0], sensorLocation[0][1]};
		sensorLocation[5] = new int[] {sensorLocation[2][0], sensorLocation[2][1]};

		sensorDirection[1] = new int[] {Constant.SENSORDIRECTION[(i+1) % Constant.SENSORDIRECTION.length][0], Constant.SENSORDIRECTION[(i+1) % Constant.SENSORDIRECTION.length][1]};
		sensorDirection[2] = new int[] {Constant.SENSORDIRECTION[((i-1) % Constant.SENSORDIRECTION.length + Constant.SENSORDIRECTION.length) % Constant.SENSORDIRECTION.length][0],
				Constant.SENSORDIRECTION[((i-1) % Constant.SENSORDIRECTION.length + Constant.SENSORDIRECTION.length) % Constant.SENSORDIRECTION.length][1]};
	}

	public abstract String[] getAllSensorsValue(int x, int y, int direction); 
	public abstract Map getTrueMap(); 	// Only for simulator to display the true map
	public abstract void setTrueMap(Map map); // Only for simulator to load map
}