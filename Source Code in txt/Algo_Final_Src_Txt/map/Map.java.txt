package map;

import java.util.Random;

import config.Constant;

public class Map{

	// possibleGridLabels - 0 = unexplored, 1 = explored, 2 = obstacle, 3 = way point, 4 = start point and 5 = end point
	// Note that grid is by x, y coordinate and this is opposite of the Array position in Java
	
	private String[][] grid = new String[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];
	private double[][] dist = new double[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];
	private int[] waypoint = new int[] {-1, -1};
	private String[] MDPString = new String[3];
	private boolean changed = true;
	
	
	// Only used for simulation
	public static Random r = new Random();
	
	// Initialise an unexplored map
	public Map() {
		resetMap();
	}
	
	// Initialise map with grid String 2D array
	public Map(String[][] grid) {
		initializeMap(grid);
	}
	
	public Map(int [][] grid) {
		for (int i = 0; i < Constant.BOARDWIDTH; i++) {
			for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
				if (grid[i][j] < Constant.POSSIBLEGRIDLABELS.length) {
					setGrid(i, j, Constant.POSSIBLEGRIDLABELS[grid[i][j]]);
				}
				else {
					setGrid(i, j, Constant.POSSIBLEGRIDLABELS[0]);
				}
			}
		}
	}
	
	public Map copy() {
		Map m = new Map(this.grid);
		m.setWayPoint(this.waypoint[0], this.waypoint[1]);
		return m;
	}

	public String print() {
		String s = "";
		s += "The current map is: \n\n";
		System.out.println("The current map is: \n");

		for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
			for (int i = 0; i < Constant.BOARDWIDTH; i++) {
				if (i != Constant.BOARDWIDTH - 1) {
					if (grid[i][j] == Constant.POSSIBLEGRIDLABELS[1] || grid[i][j] == Constant.POSSIBLEGRIDLABELS[2] ||
							grid[i][j] == Constant.POSSIBLEGRIDLABELS[3] || grid[i][j] == Constant.POSSIBLEGRIDLABELS[5]) {
//						s+=grid[i][j] + "  , ";
						String temp = " ";
						if(grid[i][j] == Constant.POSSIBLEGRIDLABELS[1]) {
							temp = String.format("%3s|", " ");
						} else if(grid[i][j] == Constant.POSSIBLEGRIDLABELS[2]) {
							temp = String.format("%3s|", "X");
						} else if(grid[i][j] == Constant.POSSIBLEGRIDLABELS[3]) {
							temp = String.format("%3s|", "W");
						} else if(grid[i][j] == Constant.POSSIBLEGRIDLABELS[5]) {
							temp = String.format("%3s|", "E");
						}
						
						s += temp;
						
//						System.out.print(grid[i][j] + "  , " );
						System.out.printf("%3s", temp);
					}
					else {
//						s+=grid[i][j] + ", ";
//						System.out.print(grid[i][j] + ", " );
						String temp = " ";
						if(grid[i][j]== Constant.POSSIBLEGRIDLABELS[0]) {
							temp = String.format("%3s|", "O");
						} else if(grid[i][j] == Constant.POSSIBLEGRIDLABELS[4]) {
							temp = String.format("%3s|", "S");
						}
						s += temp;
						System.out.printf("%3s", temp);
					}
				}
				else {
					String temp = " ";
					if(grid[i][j] == Constant.POSSIBLEGRIDLABELS[1]) {
						temp = String.format("%3s|", " ");
					} else if(grid[i][j] == Constant.POSSIBLEGRIDLABELS[2]) {
						temp = String.format("%3s|", "X");
					} else if(grid[i][j] == Constant.POSSIBLEGRIDLABELS[3]) {
						temp = String.format("%3s|", "W");
					} else if(grid[i][j] == Constant.POSSIBLEGRIDLABELS[5]) {
						temp = String.format("%3s|", "E");
					} else if(grid[i][j]== Constant.POSSIBLEGRIDLABELS[0]) {
						temp = String.format("%3s|", "O");
					} else if (grid[i][j] == Constant.POSSIBLEGRIDLABELS[4]) {
						temp = String.format("%3s|", "S");
					}
					
					s += temp;
					System.out.printf("%3s", temp);
				}
			}
			s+="\n";
			System.out.println();
		}
		s+="\n";
		System.out.println("");
		return s;
	}
	
	public void initializeMap(String[][] grid) {
		for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
			for (int i = 0; i < Constant.BOARDWIDTH; i++) {
				setGrid(i, j, grid[i][j]);
			}
		}
	}
	
	// Creates an unexplored map
	public void resetMap() {
		
		/* According to the algorithm_briefing_19S1(1).pdf, 
		 * start point is always 3x3 grid at the top left corner
		 * and end point is always 3x3 grid diagonally opposite the start point*/
		
		for (int i = 0; i< Constant.BOARDWIDTH; i++) {
			for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
				// Set the start point grids
				// Set dist to 0 to ensure all values will NOT be overridden
				if (i < Constant.STARTPOINTWIDTH && j < Constant.STARTPOINTHEIGHT) {
					setGrid(i, j, Constant.POSSIBLEGRIDLABELS[4]);
					setDist(i, j, 0);
				}
				// Set the end point grids
				// Set dist to 0 to ensure all values will NOT be overridden
				else if (i >= Constant.BOARDWIDTH - Constant.ENDPOINTWIDTH && j >= Constant.BOARDHEIGHT - Constant.ENDPOINTHEIGHT) {
					setGrid(i, j, Constant.POSSIBLEGRIDLABELS[5]);
					setDist(i, j, 0);
				}
				// Set the remaining grids unexplored
				// Set dist to 999999 to ensure all values will be overridden
				else {
					setGrid(i, j, Constant.POSSIBLEGRIDLABELS[0]);
					setDist(i, j, 999999);
				}
			}
		}
	}
	
	// Set the distance of which the grid label is set
	public void setDist(int x, int y, double value) {
		if ((x >= 0) && (x < Constant.BOARDWIDTH) && (y >= 0) && (y < Constant.BOARDHEIGHT)) {
			dist[x][y] = value;
		}
	}
	
	// Set grid label 
	public void setGrid(int x, int y, String command) {
		
		if (x < 0 || x >= Constant.BOARDWIDTH || y < 0 || y >= Constant.BOARDHEIGHT) {
			return;
		}
		
		for (int i = 0; i < Constant.POSSIBLEGRIDLABELS.length; i++) {
			if (command.toUpperCase().compareTo(Constant.POSSIBLEGRIDLABELS[i].toUpperCase()) == 0) {
				changed = true;
				if (i == 3) {
					setWayPoint(x, y);
				}
				else {
					grid[x][y] = command;
				}
				
				return;
			}
		}
		System.out.println("grid label error when setting grid");
		
	}
	
	// Generate Random Map or Empty Map. Note that this does not guarantee a maneuverable map
	public void generateMap(boolean rand) {
		int k = 0;
		
		for (int i = 0; i< Constant.BOARDWIDTH; i++) {
			for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
				// Set the start point grids
				if (i < Constant.STARTPOINTWIDTH && j < Constant.STARTPOINTHEIGHT) {
					setGrid(i, j, Constant.POSSIBLEGRIDLABELS[4]);
				}
				// Set the end point grids
				else if (i >= Constant.BOARDWIDTH - Constant.ENDPOINTWIDTH && j >= Constant.BOARDHEIGHT - Constant.ENDPOINTHEIGHT) {
					setGrid(i, j, Constant.POSSIBLEGRIDLABELS[5]);
				}
				// Set the remaining grids explored
				else {
					setGrid(i, j, Constant.POSSIBLEGRIDLABELS[1]);
				}
			}
		}
		
		if (rand) {
			while (k <= Constant.MAXOBSTACLECOUNT) {
				int x = r.nextInt(Constant.BOARDWIDTH);
				int y = r.nextInt(Constant.BOARDHEIGHT);
				if (getGrid(x, y).compareTo(Constant.POSSIBLEGRIDLABELS[1]) == 0) {
					setGrid(x, y, Constant.POSSIBLEGRIDLABELS[2]);
					k++;
				}
				
			}
		}
		
	}
	
	public void setWayPoint(int x, int y) {
		boolean verbose = new Exception().getStackTrace()[1].getClassName().equals("robot.Robot");
		
		if (x >= Constant.BOARDWIDTH - 1 || x <= 0 || y >= Constant.BOARDHEIGHT - 1 || y <= 0 
			|| (getGrid(x, y) != null && getGrid(x, y).compareTo(Constant.POSSIBLEGRIDLABELS[0]) != 0 && getGrid(x, y).compareTo(Constant.POSSIBLEGRIDLABELS[1]) != 0)) {
			if (!(waypoint[0] == -1 && waypoint[1] == -1)) {
				this.waypoint[0] = -1;
				this.waypoint[1] = -1;
				if (verbose) {
					System.out.println("The current waypoint is set as: " + "-1" + "," + "-1");
				}
			}
			return;
		}
		this.waypoint[0] = x;
		this.waypoint[1] = y;
		if (verbose) {
			System.out.println("Successfully set the waypoint: " + x + "," + y);
		}
	}
	
	public int[] getWayPoint() {
		return waypoint;
	}
	
	protected String[][] getGridMap() {
		return grid;
	}

	public double getDist(int x, int y) {
		// If the x, y is outside the board, it returns an obstacle.
		if (x < 0 || x >= Constant.BOARDWIDTH || y < 0 || y >= Constant.BOARDHEIGHT) {
			return 1;
		}
		return dist[x][y];
	}
	
	public String getGrid(int x, int y) {
		
		// If the x, y is outside the board, it returns an obstacle.
		if (x < 0 || x >= Constant.BOARDWIDTH || y < 0 || y >= Constant.BOARDHEIGHT) {
			return Constant.POSSIBLEGRIDLABELS[2];
		}
		return grid[x][y];
	}
	
	// Only for simulator purposes
	public static boolean compare(Map a, Map b) {
		String [][]a_grid = a.getGridMap();
		String [][]b_grid = b.getGridMap();
		for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
			for (int i = 0; i < Constant.BOARDWIDTH; i++) {
				if (a_grid[i][j].compareTo(b_grid[i][j])!=0) {
					return false;
				}
			}
		}
		return true;
	}
	
	// Check if the MDF string stored has changed and make the mdf string if it did change
	public String[] getMDFString() {
		if (changed == false) {
			return this.MDPString;
		}
		
		changed = false;
		
		StringBuilder MDFBitStringPart1 = new StringBuilder();
		StringBuilder MDFBitStringPart2 = new StringBuilder();
		
		MDFBitStringPart1.append("11");
		String[] MDFHexString = new String[] {"","",""};
		
		for (int j = 0; j < Constant.BOARDWIDTH; j++) {
			for (int i = 0; i < Constant.BOARDHEIGHT; i++) {

				if (grid[j][i].compareTo(Constant.POSSIBLEGRIDLABELS[2])==0) { // Obstacle
					MDFBitStringPart1.append("1");
					MDFBitStringPart2.append("1");
					
				}
				else if (grid[j][i].compareTo(Constant.POSSIBLEGRIDLABELS[0]) == 0) { // Unexplored
					MDFBitStringPart1.append("0");
				}
				else {
					MDFBitStringPart1.append("1");
					MDFBitStringPart2.append("0");
				}
				
			}
		}
		MDFBitStringPart1.append("11");
		
		for (int i = 0; i < MDFBitStringPart1.length(); i += 4) {
			MDFHexString[0] += Integer.toString(Integer.parseInt(MDFBitStringPart1.substring(i, i + 4), 2), 16);
		}
		
		if ((MDFBitStringPart2.length() % 4) != 0){ // Only pad if the MDF Bit string is not a multiple of 4
			MDFBitStringPart2.insert(0, "0".repeat(4 - (MDFBitStringPart2.length() % 4)));
		}
		
		for (int i = 0; i < MDFBitStringPart2.length(); i += 4) {
			MDFHexString[2] += Integer.toString(Integer.parseInt(MDFBitStringPart2.substring(i, i + 4), 2), 16);
		}
		
		int length = 0;
		for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
			for (int i = 0; i < Constant.BOARDWIDTH; i++) {
				if (grid[i][j] != Constant.POSSIBLEGRIDLABELS[0]) {
					length++;
				}
			}
		}
		
		MDFHexString[1] = Integer.toString(length);
		
		this.MDPString = MDFHexString;
		return MDFHexString;

	}
}
