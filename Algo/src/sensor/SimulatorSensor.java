package sensor;
import config.Constant;
import map.Map;

public class SimulatorSensor extends Sensor{
	// Simulate the Ardurino sensor
	private Map trueMap;
	private String sensorValue;
	private String[] sensorArray;
	
	// Randomly generate a true map for simulated environment
	public SimulatorSensor() {
		trueMap = new Map();
		trueMap.generateMap(Constant.RANDOMMAP);
	}
	
	// Set a map for the simulated environment
	public SimulatorSensor(Map map) {
		trueMap = map;
	}
	
	// Based on the true simulated map, the sensor will get the valid sensor value for the robot
	private void updateSensorsValue(int x, int y, int direction) {
		// sensorValue will have this: FR, FC, FL, RB, RF, LF

		String [] sensorValue = new String[6];

		// "North", "East", "South", "West"
		for (int i = 1; i <= Constant.SHORTSENSORMAXRANGE; i ++ ) {
			if (sensorValue[0] == null && trueMap.getGrid(x+sensorLocation[0][0] + i * sensorDirection[0][0], y+sensorLocation[0][1] + i * sensorDirection[0][1]).compareTo(Constant.POSSIBLEGRIDLABELS[2]) == 0
					&& i <= Constant.SENSOR_RANGES[0].length){
				sensorValue[0] = getSensorValue(0, i, "SHORT");
			}
			if (sensorValue[1] == null && trueMap.getGrid(x+sensorLocation[1][0] + i * sensorDirection[0][0], y+sensorLocation[1][1] + i * sensorDirection[0][1]).compareTo(Constant.POSSIBLEGRIDLABELS[2]) == 0
					&& i <= Constant.SENSOR_RANGES[1].length) {
				sensorValue[1] = getSensorValue(1, i, "SHORT");
			}
			if (sensorValue[2] == null && trueMap.getGrid(x+sensorLocation[2][0] + i * sensorDirection[0][0], y+sensorLocation[2][1] + i * sensorDirection[0][1]).compareTo(Constant.POSSIBLEGRIDLABELS[2]) == 0
					&& i <= Constant.SENSOR_RANGES[2].length){
				sensorValue[2] = getSensorValue(2, i, "SHORT");
			}
			if (sensorValue[3] == null && trueMap.getGrid(x+sensorLocation[3][0] + i * sensorDirection[1][0], y+sensorLocation[3][1] + i * sensorDirection[1][1]).compareTo(Constant.POSSIBLEGRIDLABELS[2]) == 0
					&& i <= Constant.SENSOR_RANGES[3].length) {
				sensorValue[3] = getSensorValue(3, i, "SHORT");
			}
			if (sensorValue[4] == null && trueMap.getGrid(x+sensorLocation[4][0] + i * sensorDirection[1][0], y+sensorLocation[4][1] + i * sensorDirection[1][1]).compareTo(Constant.POSSIBLEGRIDLABELS[2]) == 0
					&& i <= Constant.SENSOR_RANGES[4].length) {
				sensorValue[4] = getSensorValue(4, i, "SHORT");
			}
		}

		for (int i = 1; i <= Constant.FARSENSORMAXRANGE; i ++ ) {
			if (sensorValue[5] == null && trueMap.getGrid(x+sensorLocation[5][0] + i * sensorDirection[2][0], y+sensorLocation[5][1] + i * sensorDirection[2][1]).compareTo(Constant.POSSIBLEGRIDLABELS[2]) == 0
					&& i <= Constant.SENSOR_RANGES[5].length) {
				sensorValue[5] = getSensorValue(5, i, "FAR");
			}
		}

		sensorValue = padSensorValue(sensorValue);
		this.sensorValue = String.join(" ", sensorValue);
	}
	
	// Depends on the sensors, it will return the valid values based on the grid offset from the sensor
	private String getSensorValue(int s, int i, String mode) {
		return "" + (Constant.SENSOR_RANGES[s][i-1]-1);
	}

	public String[] getAllSensorsValue(int x, int y, int direction) {
		// This is to simulate getting values from Arduino
		updateSensorsValue(x, y, direction);
		sensorArray = sensorValue.split(" ");
		return sensorArray;
	}

	private String[] padSensorValue(String [] arr) {
		// Pad whatever value that has no obstacle
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == null) {
				// As long as they are greater than FARSENSORMAXRANGE THEY CAN BE DEEMED AS NO OBSTACLE
				arr[i] = "" + (Constant.FARSENSORMAXRANGE * 10 + Constant.FARSENSOROFFSET + 1) + ".0";
			}
		}
		return arr;
	}

	public Map getTrueMap() {
		return trueMap;
	}

	public void setTrueMap(Map map) {
		trueMap = map;
	}

}