package simulator;

import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import astarpathfinder.AStarPathFinder;
import config.Constant;
import connection.ConnectionSocket;
import exploration.ExplorationThread;
import robot.SimulatorRobot;
import map.Map;
import timertask.EnableButtonTask;
import astarpathfinder.FastestPathThread;

public class AddJButtonActionListener implements ActionListener{
	private JFrame frame;
	private int x = 1000;
	private int y = 200;
	private SimulatorRobot r;
	private ArrayList<JComponent> Buttons = new ArrayList<JComponent>();
	private HashMap<String, JLabel> Labels = new HashMap<String, JLabel>();
	private HashMap  <String, String> filePath= new HashMap<String, String>();
	private Timer t = new Timer();
	private int step = 1;
	private Map loadedMap;
	private int[] waypoint_chosen = {-1,-1};
	private int time_chosen = -1;
	private int percentage_chosen = 100;
	private String[] MDFString = {null, null};
	private int speed_chosen = 1;
	private boolean image_recognition_chosen = false;
	private JTextArea message = null;
	private JScrollPane scrollPane = null;


	// This constructor initialises all the buttons
	public AddJButtonActionListener(JFrame frame, SimulatorRobot r) {
		this.frame = frame;
		this.r = r;
		String[] arr = getArenaMapFileNames();
		String[] waypoint_x_pos = create_seq_array(1, Constant.BOARDWIDTH-1);
		String[] waypoint_y_pos = create_seq_array(1, Constant.BOARDHEIGHT-1);
		String[] time_arr = create_seq_array(0, 121);
		String[] percentage_arr = create_seq_array(0, 101);
		String[] speed_arr = create_seq_array(1, 6);
		String[] image_rec_arr = new String[] {"No Image Recognition", "With Image Recognition"};

		// Create the UI Component
		JLabel mcLabel = new JLabel("Manual Control:");
		JLabel loadMapLabel = new JLabel("Select the map you wish to load:");
		JButton right = new JButton();
		JButton left = new JButton();
		JButton up = new JButton();
		JButton update = new JButton();
		JButton checkMap = new JButton();
		JButton toggleMap = new JButton();
		JButton resetRobot = new JButton();
		JLabel robotView = new JLabel("Robot's View");
		JLabel simulatedMap = new JLabel("Simulated Map");
		JButton exploration = new JButton();
		JButton fastestPath = new JButton();
		JComboBox <String> arenaMap = new JComboBox<String>(arr);
		JComboBox <String> waypoint_x = new JComboBox<String>(waypoint_x_pos);
		JComboBox <String> waypoint_y = new JComboBox<String>(waypoint_y_pos);
		JLabel set_waypoint_label = new JLabel("Set Waypoint: ");
		JLabel invalid_waypoint = new JLabel("Invalid Waypoint!!");
		JComboBox <String> time = new JComboBox<>(time_arr);
		JLabel time_label = new JLabel("Set exploration time limit (secs): ");
		JComboBox <String> percentage = new JComboBox<>(percentage_arr);
		JLabel percentage_label = new JLabel("Set exploration coverage limit (%): ");
		JButton returnToStart = new JButton();
		JButton printMDF = new JButton();
		JLabel speed_label = new JLabel("Set speed (secs/step): ");
		JComboBox <String> speed = new JComboBox<>(speed_arr);
		JComboBox <String> image_rec = new JComboBox<>(image_rec_arr);
		JLabel status = new JLabel("Status");
		message = new JTextArea("\n".repeat(7) + "Initialising the User Interface...");
		scrollPane = new JScrollPane(message);
		
//		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
//	        public void adjustmentValueChanged(AdjustmentEvent e) {  
//	            e.getAdjustable().setValue(e.getAdjustable().getMaximum());
//	            
//	        }
//	    });
//		
		scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// Set Icon or Image to the UI Component
		right.setIcon(new ImageIcon(new ImageIcon(".\\images\\right.png").getImage().getScaledInstance(Constant.GRIDWIDTH, Constant.GRIDHEIGHT, Image.SCALE_DEFAULT)));
		left.setIcon(new ImageIcon(new ImageIcon(".\\images\\left.png").getImage().getScaledInstance(Constant.GRIDWIDTH, Constant.GRIDHEIGHT, Image.SCALE_DEFAULT)));
		up.setIcon(new ImageIcon(new ImageIcon(".\\images\\up.png").getImage().getScaledInstance(Constant.GRIDWIDTH, Constant.GRIDHEIGHT, Image.SCALE_DEFAULT)));
		update.setText("Update");
		checkMap.setText("Check Map");
		toggleMap.setText("Toggle Map");
		resetRobot.setText("Restart");
		exploration.setText("Exploration");
		fastestPath.setText("Fastest Path");
		printMDF.setText("MDF String");
		returnToStart.setText("Return To Start");

		// For the Button to do something, you need to add the button to this Action Listener and set the command for the ActionListener to receive
		right.addActionListener(this);
		right.setActionCommand("Right");
		left.addActionListener(this);
		left.setActionCommand("Left");
		up.addActionListener(this);
		up.setActionCommand("Up");
		update.addActionListener(this);
		update.setActionCommand("Update");
		checkMap.addActionListener(this);
		checkMap.setActionCommand("Check Map");
		toggleMap.addActionListener(this);
		toggleMap.setActionCommand("Toggle Map");
		resetRobot.addActionListener(this);
		resetRobot.setActionCommand("Restart");
		exploration.addActionListener(this);
		exploration.setActionCommand("Exploration");
		fastestPath.addActionListener(this);
		fastestPath.setActionCommand("Fastest Path");
		arenaMap.addActionListener(this);
		arenaMap.setActionCommand("Load Map");
		arenaMap.setSelectedIndex(-1); //  This line will print null in console
		waypoint_x.addActionListener(this);
		waypoint_x.setActionCommand("Set x coordinate");
		waypoint_x.setSelectedIndex(-1); //  This line will print null in console
		waypoint_y.addActionListener(this);
		waypoint_y.setActionCommand("Set y coordinate");
		waypoint_y.setSelectedIndex(-1); //  This line will print null in console
		time.addActionListener(this);
		time.setActionCommand("Set time limit");
		time.setSelectedIndex(-1); //  This line will print null in console
		percentage.addActionListener(this);
		percentage.setActionCommand("Set % limit");
		percentage.setSelectedIndex(101);
		returnToStart.addActionListener(this);
		returnToStart.setActionCommand("Return");
		printMDF.addActionListener(this);
		printMDF.setActionCommand("MDF String");
		speed.addActionListener(this);
		speed.setActionCommand("Set speed");
		speed.setSelectedIndex(0);
		image_rec.addActionListener(this);
		image_rec.setActionCommand("Set image recognition");
		image_rec.setSelectedIndex(0);


		// Set the size (x, y, width, height) of the UI label

		mcLabel.setBounds(x, y - 100, 100, 50);
		loadMapLabel.setBounds(x, y + 165, 300, 50);
		left.setBounds(x + 100, y - 100, 50, 50);
		right.setBounds(x + 200, y - 100, 50, 50);
		up.setBounds(x + 150, y - 100, 50, 50);
		update.setBounds(x + 275, y - 100, 100, 50);
		checkMap.setBounds(x, y - 25, 100, 50);
		toggleMap.setBounds(x + 150, y - 25, 110, 50);
		resetRobot.setBounds(x + 300, y - 25, 100, 50 );
		robotView.setBounds(x - 600, y - 185, 200, 50);
		simulatedMap.setBounds(x + 100, y - 100, 300, 50);
		exploration.setBounds(x, y + 50, 110, 50);
		fastestPath.setBounds(x + 150, y + 50, 110, 50);
		arenaMap.setBounds(x + 200, y + 175, 120, 30);
		waypoint_x.setBounds(x + 85 , y + 125, 50, 30);
		waypoint_y.setBounds(x + 150, y + 125, 50, 30);
		set_waypoint_label.setBounds(x, y + 125, 300, 30);
		invalid_waypoint.setBounds(x + 150, y + 125, 300, 30);
		time.setBounds(x + 200, y + 225, 50, 30);
		time_label.setBounds(x, y + 225, 300, 30);
		percentage.setBounds(x + 200 , y + 275, 50, 30);
		percentage_label.setBounds(x, y + 275, 300, 30);
		speed.setBounds(x + 150, y + 325, 50, 30);
		speed_label.setBounds(x, y + 325, 200, 30);
		returnToStart.setBounds(x + 300, y + 50, 140, 50);
		printMDF.setBounds(x + 275, y + 305, 100, 50);
		image_rec.setBounds(x + 275, y + 225, 175, 30);
		status.setBounds(x, y + 360, 50, 30);
		scrollPane.setBounds(x, y + 390, 500, 163);
		

		// Set fonts for the labels
		mcLabel.setFont(new Font(mcLabel.getFont().getName(), Font.ITALIC, 13));
		robotView.setFont(new Font(robotView.getFont().getName(), Font.BOLD, 30));
		simulatedMap.setFont(new Font(simulatedMap.getFont().getName(), Font.BOLD, 30));
		status.setFont(new Font(simulatedMap.getFont().getName(), Font.BOLD, 15));
		message.setFont(new Font(simulatedMap.getFont().getName(), Font.ITALIC, 15));
		
		// Set background colour of components
		message.setBackground(Color.WHITE);
		
		// Set edittable of components
		message.setEditable(false);
		
		// Set max row of message
		message.setLineWrap(true);
		message.setWrapStyleWord(true);

		// Set location of the UI component
		mcLabel.setLocation(x, y - 100);
		loadMapLabel.setLocation(x, y + 165);
		left.setLocation(x + 100, y - 100);
		right.setLocation(x + 200, y - 100);
		up.setLocation(x + 150, y - 100);
		update.setLocation(x + 275, y - 100);
		checkMap.setLocation(x, y - 25);
		toggleMap.setLocation(x + 150, y - 25);
		resetRobot.setLocation(x + 300, y - 25);
		robotView.setLocation(x - 600, y - 185);
		simulatedMap.setLocation(x - 600, y - 185);
		exploration.setLocation(x, y + 50);
		fastestPath.setLocation(x + 150, y + 50);
		arenaMap.setLocation(x + 200, y + 175);
		waypoint_x.setLocation(x + 85, y + 125);
		waypoint_y.setLocation(x + 150, y + 125);
		set_waypoint_label.setLocation(x, y + 125);
		invalid_waypoint.setLocation(x + 250, y + 125);
		time.setLocation(x + 200 , y + 225);
		time_label.setLocation(x, y + 225);
		percentage.setLocation(x + 200 , y + 275);
		percentage_label.setLocation(x, y + 275);
		speed.setLocation(x + 150, y + 325);
		speed_label.setLocation(x, y + 325);
		returnToStart.setLocation(x + 300, y + 50);
		printMDF.setLocation(x + 275, y + 305);
		image_rec.setLocation(x + 275, y + 225);
		status.setLocation(x, y + 360);
		scrollPane.setLocation(x, y + 390);

		// Add the UI component to the frame
		frame.add(mcLabel);
		frame.add(loadMapLabel);
		frame.add(right);
		frame.add(left);
		frame.add(up);
		frame.add(update);
		frame.add(checkMap);
		frame.add(toggleMap);
		frame.add(resetRobot);
		frame.add(robotView);
		frame.add(simulatedMap);
		frame.add(exploration);
		frame.add(fastestPath);
		frame.add(arenaMap);
		frame.add(waypoint_x);
		frame.add(waypoint_y);
		frame.add(set_waypoint_label);
		frame.add(invalid_waypoint);
		frame.add(time);
		frame.add(time_label);
		frame.add(percentage);
		frame.add(percentage_label);
		frame.add(returnToStart);
		frame.add(printMDF);
		frame.add(speed);
		frame.add(speed_label);
		frame.add(image_rec);
		frame.add(status);
		frame.add(scrollPane);

		// Set Visibility of UI Component
		mcLabel.setVisible(true);
		loadMapLabel.setVisible(true);
		right.setVisible(true);
		left.setVisible(true);
		up.setVisible(true);
		update.setVisible(true);
		checkMap.setVisible(true);
		toggleMap.setVisible(true);
		resetRobot.setVisible(true);
		robotView.setVisible(true);
		simulatedMap.setVisible(false);
		exploration.setVisible(true);
		fastestPath.setVisible(true);
		arenaMap.setVisible(true);
		waypoint_x.setVisible(true);
		waypoint_y.setVisible(true);
		set_waypoint_label.setVisible(true);
		invalid_waypoint.setVisible(false);
		time.setVisible(true);
		time_label.setVisible(true);
		percentage.setVisible(true);
		percentage_label.setVisible(true);
		returnToStart.setVisible(true);
		printMDF.setVisible(true);
		speed.setVisible(true);
		speed_label.setVisible(true);
		image_rec.setVisible(true);
		status.setVisible(true);
		message.setVisible(true);
		scrollPane.setVisible(true);

		// Add button to the list of buttons
		Buttons.add(right);
		Buttons.add(left);
		Buttons.add(up);
		Buttons.add(update);
		Buttons.add(checkMap);
		Buttons.add(toggleMap);
		Buttons.add(resetRobot);
		Buttons.add(exploration);
		Buttons.add(fastestPath);
		Buttons.add(arenaMap);
		Buttons.add(waypoint_x);
		Buttons.add(waypoint_y);
		Buttons.add(time);
		Buttons.add(percentage);
		Buttons.add(returnToStart);
		Buttons.add(printMDF);
		Buttons.add(speed);
		Buttons.add(image_rec);

		// Add label to the hashmap
		Labels.put("mcLabel", mcLabel);
		Labels.put("loadMapLabel", loadMapLabel);
		Labels.put("robotView", robotView);
		Labels.put("simulatedMap", simulatedMap);
		Labels.put("set_waypoint_label", set_waypoint_label);
		Labels.put("invalid_waypoint", invalid_waypoint);
		Labels.put("time_label", time_label);
		Labels.put("percentage_label", percentage_label);
		Labels.put("speed_label", speed_label);
//		Labels.put("image_cap", image_cap);
//		Labels.put("calibrating", calibrating);
		
		// Disable all button during the real runs as they are not meant to work for the Real Run
		if (ConnectionSocket.checkConnection()) {
			this.disableButtons();
		}
	}
	
	public void displayMessage(String s, int mode) {
		// Mode 0 only print the message, Mode 1 display onto the console, Mode 2 does both
		JScrollBar vbar = scrollPane.getVerticalScrollBar();
		switch(mode) {
			case 0:
				System.out.println(s);
				break;
			case 1:
				message.append("\n" + s);
				try {
					String messageText = message.getDocument().getText(0, message.getDocument().getLength());
					if (messageText.charAt(0) == '\n') {
						message.setText(message.getDocument().getText(0, message.getDocument().getLength()).replaceFirst("\n", ""));
					}
					String [] arr = messageText.split("\n");
					if (arr.length > 100) {
						message.setText(message.getDocument().getText(0, message.getDocument().getLength()).replaceFirst(arr[0] + "\n", ""));
					}
				}
				catch (Exception e) {
					System.out.println("Error in displayMessage");
				}
				vbar.addAdjustmentListener( new AdjustmentListener() {
			        @Override
			        public void adjustmentValueChanged(AdjustmentEvent e) {
			            Adjustable adjustable = e.getAdjustable();
			            adjustable.setValue(adjustable.getMaximum());
			            // This is so that the user can scroll down afterwards
			            vbar.removeAdjustmentListener(this);
			        }
			    });
		        break;
			case 2:
				System.out.println(s);
				message.append("\n" + s);
				try {
					String messageText = message.getDocument().getText(0, message.getDocument().getLength());
					if (messageText.charAt(0) == '\n') {
						message.setText(message.getDocument().getText(0, message.getDocument().getLength()).replaceFirst("\n", ""));
					}
					String [] arr = messageText.split("\n");
					if (arr.length > 100) {
						message.setText(message.getDocument().getText(0, message.getDocument().getLength()).replaceFirst(arr[0] + "\n", ""));
					}
				}
				catch (Exception e) {
					System.out.println("Error in displayMessage");
				}
				vbar.addAdjustmentListener( new AdjustmentListener() {
			        @Override
			        public void adjustmentValueChanged(AdjustmentEvent e) {
			            Adjustable adjustable = e.getAdjustable();
			            adjustable.setValue(adjustable.getMaximum());
			            // This is so that the user can scroll down afterwards
			            vbar.removeAdjustmentListener(this);
			        }
			    });
		        break;
		}	
	}

	private String[] create_seq_array(int min, int max){
		String[] arr = new String[max-min+1];
		int count = 1;
		for (int i=min; i<max; i++) {
			arr[count] = Integer.toString(i);
			count ++;
		}
		return arr;
	}
	
	// Reads all the txt file from the directory sample arena. Returns the array of the file name in the directory.
	public String[] getArenaMapFileNames() {
		File folder = new File("./sample arena");
		filePath= new HashMap<String, String>();
		for (File file: folder.listFiles()) {
			if (file.getName().endsWith(".txt")) {
				filePath.put(file.getName().substring(0, file.getName().lastIndexOf(".txt")), file.getAbsolutePath());
			}
		}
		String[] fileName = new String[filePath.size()];
		int i = filePath.size()-1;

		for (String key: filePath.keySet()) {
			fileName[i] = key;
			i--;
		}
		Arrays.sort(fileName);
		return fileName;
	}
	
	// Convert the sample arena text file into map and return the map
	public Map getGridfromFile(String path, String fileName, String[][] grid) throws Exception, FileNotFoundException, IOException{
		FileReader fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		int heightCount = 0;
		
		while (line != null) {
			line = line.strip().toUpperCase();
			
			// Check for invalid map
			if (line.length() != Constant.BOARDWIDTH) {
				displayMessage("The format of the " + fileName + " does not match the board format.", 1);
				throw new Exception("The format of the " + fileName + " does not match the board format.");
			}
			for (int i = 0; i < line.length(); i++) {
				switch(line.charAt(i)) {
					case 'S': // Represent start grid
						grid[i][heightCount] = Constant.POSSIBLEGRIDLABELS[4];
						break;
						
					case 'U': // Represent empty grid as end grid already used 'E'
						// Here, we set to explored instead of Unexplored
						grid[i][heightCount] = Constant.POSSIBLEGRIDLABELS[1];
						break;
						
//					case 'W':
						/* We do not allow waypoint setting as there may be ambiguous situation 
						 * 
						 * For example, if user accidentally set wrongly, what should be the grid string after it resets as one
						 * map should only have one waypoint?
						 * 
						 * This might affect the correctness of our mdf string.*/
//						grid[i][heightCount] = Constant.POSSIBLEGRIDLABELS[3];
//						break;
						
					case 'E': // Represent end grid
						grid[i][heightCount] = Constant.POSSIBLEGRIDLABELS[5];
						break;
						
					case 'O': // Represent obstacle grid
						grid[i][heightCount] = Constant.POSSIBLEGRIDLABELS[2];
						break;
						
					default:
						displayMessage("There is unrecognised character symbol in " + fileName + ".\n" +
					fileName + " failed to load into the program.", 1);
						throw new Exception("There is unrecognised character symbol in " + fileName + ".\n" +
					fileName + " failed to load into the program.");
				}
			}

			heightCount++;
			line = br.readLine();
		}
		if (heightCount != Constant.BOARDHEIGHT) {
			throw new Exception("The format of the " + fileName + " does not match the board format.");
		}
		br.close();
		loadedMap = new Map(grid);
		displayMessage(fileName + " has loaded successfully.", 2);
		return loadedMap;
	}
	
	private void disableButtons() {
		for (int i = 0; i < Buttons.size(); i++){
			Buttons.get(i).setEnabled(false);
		}
	}
	
	public void enableButtons() {
		for (int i = 0; i < Buttons.size(); i++){
			Buttons.get(i).setEnabled(true);
		}
	}

	public void disableLabel(String label) {
		Labels.get(label).setVisible(false);
	}

	public void enableLabel(String label) {
		Labels.get(label).setVisible(true);
	}
	
	// Define all the action of all the button
	@Override
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("Right")) {
			displayMessage("Rotate right button clicked", 2);
			disableButtons();
			if (!Labels.get("robotView").isVisible()) {
				enableLabel("robotView");
				disableLabel("simulatedMap");
			}
			r.rotateRight();
			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
		}

		if (action.equals("Left")) {
			displayMessage("Rotate left button clicked", 2);
			disableButtons();
			if (!Labels.get("robotView").isVisible()) {
				enableLabel("robotView");
				disableLabel("simulatedMap");
			}
			r.rotateLeft();
			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
		}

		if (action.equals("Up")) {
			displayMessage("Forward button clicked", 2);
			disableButtons();
			if (!Labels.get("robotView").isVisible()) {
				enableLabel("robotView");
				disableLabel("simulatedMap");
				r.toggleMap();
			}
			r.forward(1);
			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
		}

		if (action.contentEquals("Update")) {
			disableButtons();
			if (!Labels.get("robotView").isVisible()) {
				enableLabel("robotView");
				disableLabel("simulatedMap");
			}
			int[] isObstacle = r.updateMap();
			for (int i = 0; i < isObstacle.length; i++) {
				System.out.print(isObstacle[i] + " ");
			}
			System.out.println();
			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
		}

		if (action.contentEquals("Check Map")) {
			disableButtons();
			JOptionPane.showMessageDialog(null, r.checkMap(), "Result of checking map", JOptionPane.INFORMATION_MESSAGE);
			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
		}

		if (action.contentEquals("Toggle Map")) {
			disableButtons();
			if (r.toggleMap().compareTo("robot") == 0) {
				if (r.checkMap().equals("The maps are the same!") && Labels.get("robotView").isVisible()){
					disableLabel("robotView");
					enableLabel("simulatedMap");
				}
				else {
					enableLabel("robotView");
					disableLabel("simulatedMap");
				}
			}
			else {
				disableLabel("robotView");
				enableLabel("simulatedMap");
			}
			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
		}

		if (action.contentEquals("Restart")) {
			disableButtons();
			enableLabel("robotView");
			disableLabel("simulatedMap");
			ExplorationThread.stopThread();
			FastestPathThread.stopThread();
			r.restartRobotUI();
			try {
				TimeUnit.MILLISECONDS.sleep(500);
			}
			catch (Exception z) {

			}
			r.restartRobot();
			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
			displayMessage("Restarted the Robot", 2);
		}

		if (action.contentEquals("Load Map")) {
			JComboBox <String> arenaMap = (JComboBox <String>)e.getSource();
			String selectedFile = (String) arenaMap.getSelectedItem();
			if (selectedFile == null || selectedFile.compareTo("Choose a map to load") == 0) {
				return;
			}
			String[][] grid = new String[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];
			disableButtons();
			try {
				Map map = getGridfromFile(filePath.get(selectedFile), selectedFile, grid);
				r.setTrueMap(map);
				if (Labels.get("simulatedMap").isVisible()) {
					enableLabel("robotView");
					disableLabel("simulatedMap");
					r.toggleMap();
					r.toggleMap();
				}
			}
			catch (FileNotFoundException f){
				System.out.println("File not found");
			}
			catch (IOException IO) {
				System.out.println("IOException when reading" + selectedFile);
			}
			catch (Exception eX) {
				System.out.println(eX.getMessage());
			}

			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
			
		}

		if (action.contentEquals("Fastest Path")) {
			
			if (Arrays.equals(waypoint_chosen, new int[] {-1, -1})) {
				r.setWaypoint(waypoint_chosen[0], waypoint_chosen[1]);
			}
			int [] waypoint = r.getWaypoint();
			if (!FastestPathThread.getRunning() && ExplorationThread.getCompleted()) {
				displayMessage("Fastest Path Started", 1);
				FastestPathThread.getInstance(r, waypoint, speed_chosen);
				disableButtons();
				t.schedule(new EnableButtonTask(this), 1);
			}
			else if (!ExplorationThread.getCompleted()) {
				displayMessage("You need to run exploration first.", 2);
			}
		}

		if (action.contentEquals("Exploration")) {
			if (!ExplorationThread.getRunning()) {
				displayMessage("Exploration Started", 1);
				ExplorationThread.getInstance(r, time_chosen, percentage_chosen, speed_chosen, image_recognition_chosen);
				disableButtons();
				t.schedule(new EnableButtonTask(this), 1);
			}
		}

		if (action.contentEquals("Set x coordinate")) {
			AStarPathFinder astar = new AStarPathFinder();
			JComboBox <String> waypoint_x = (JComboBox <String>)e.getSource();
			String selected_waypoint_x = (String) waypoint_x.getSelectedItem();
			if (selected_waypoint_x == null) {
				waypoint_chosen[0] = -1;
			}
			else {
				waypoint_chosen[0] = Integer.parseInt(selected_waypoint_x);
			}
			int[] old_waypoint = r.getWaypoint();
			r.setWaypoint(waypoint_chosen[0], waypoint_chosen[1]);
			if (!Arrays.equals(old_waypoint, r.getWaypoint())){
				enableLabel("robotView");
				disableLabel("simulatedMap");
			}

		}

		if (action.contentEquals("Set y coordinate")) {
			AStarPathFinder astar = new AStarPathFinder();
			JComboBox <String> waypoint_y = (JComboBox <String>)e.getSource();
			String selected_waypoint_y = (String) waypoint_y.getSelectedItem();
			if (selected_waypoint_y == null) {
				waypoint_chosen[1] = -1;
			}
			else {
				waypoint_chosen[1] = Integer.parseInt(selected_waypoint_y);
			}
			int[] old_waypoint = r.getWaypoint();
			r.setWaypoint(waypoint_chosen[0], waypoint_chosen[1]);
			if (!Arrays.equals(old_waypoint, r.getWaypoint())){
				enableLabel("robotView");
				disableLabel("simulatedMap");
			}
		}

		if (action.contentEquals("Set time limit")) {
			JComboBox <String> secs = (JComboBox <String>)e.getSource();
			String secs_chosen = (String) secs.getSelectedItem();
			if (secs_chosen == null) {
				return;
			}
			else {
				time_chosen = Integer.parseInt(secs_chosen);
			}
		}

		if (action.contentEquals("Set % limit")) {
			JComboBox <String> percentage = (JComboBox <String>)e.getSource();
			String perc_chosen = (String) percentage.getSelectedItem();
			if (perc_chosen == null) {
				return;
			}
			else {
				percentage_chosen = Integer.parseInt(perc_chosen);
			}
		}

		if (action.contentEquals("Return")) {
			r.resetRobotPositionOnUI();
		}

		if (action.contentEquals("MDF String")) {
			MDFString = r.getMDFString();
			displayMessage("The MDF String is: ", 2);
			displayMessage("Part 1: "  + MDFString[0], 2);
			displayMessage("Part 2: " + MDFString[2], 2);
		}

		if (action.contentEquals("Set speed")) {
			JComboBox<String> s = (JComboBox<String>) e.getSource();
			String sp = (String) s.getSelectedItem();
			if (sp != null) {
				speed_chosen = Integer.parseInt(sp);
			}
		}

		if (action.contentEquals("Set image recognition")) {
			JComboBox<String> ir = (JComboBox<String>) e.getSource();
			String irc = (String) ir.getSelectedItem();
			if (irc != null) {
				image_recognition_chosen = irc.equals("With Image Recognition");
				System.out.println("Image Rec: " + image_recognition_chosen);
			}
		}
	}
}
