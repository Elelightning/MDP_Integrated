package main;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import config.Constant;
import connection.ConnectionManager;
import connection.ConnectionSocket;
import simulator.Simulator;

public class MainSystem {
	
	private static boolean realRun = false;
	private static boolean simulate = false;
	
	public static void main(String[] args) {
		ImageIcon icon = new ImageIcon(new ImageIcon(Constant.DIALOGICONIMAGEPATH).getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT));
		int result = JOptionPane.CLOSED_OPTION;
		int debug = JOptionPane.CLOSED_OPTION;
		int simulator = JOptionPane.CLOSED_OPTION;
		while (result == JOptionPane.CLOSED_OPTION) {
			result = JOptionPane.showConfirmDialog(null, "Is this the real run?", "Real Run", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
			if (result == JOptionPane.YES_OPTION) {
				realRun = true;
				debug = JOptionPane.showConfirmDialog(null, "Print debug?", "Debug", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
				while (debug == JOptionPane.CLOSED_OPTION) {
					debug = JOptionPane.showConfirmDialog(null, "Print debug?", "Debug", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
				}
				simulator = JOptionPane.showConfirmDialog(null, "Show Simulator?", "Simulator", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
				while (simulator == JOptionPane.CLOSED_OPTION) {
					simulator = JOptionPane.showConfirmDialog(null, "Show Simulator?", "Simulator", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
				}
				if (simulator == JOptionPane.YES_OPTION) {
					simulate = true;
				}
			}
			if (result == JOptionPane.NO_OPTION) {
				realRun = false;
			}
		}
		
		if (realRun) {
			ConnectionManager connectionManager = ConnectionManager.getInstance();
			if (debug == JOptionPane.YES_OPTION) {
				ConnectionSocket.setDebugTrue();
				System.out.println("Debug is " + ConnectionSocket.getDebug());
			}
			
			boolean connected = false;
			while (!connected) {
				connected = connectionManager.connectToRPi(simulate);
			}
			try {
				connectionManager.start();
			}
			catch (Exception e) {
				connectionManager.stopCM();
				System.out.println("ConnectionManager is stopped");
			}
			
		}
		else {
			Simulator s = new Simulator();
		}
	}
}
