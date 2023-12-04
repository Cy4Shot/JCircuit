package jcircuit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class App {
	
	public static MainFrame frame;

	public static void main(String[] args) {
		// Set Look and Feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		// Create mainframe
		frame = new MainFrame();
		frame.setTitle("JCircuit");
		frame.setBackground(new Color(39, 174, 96));
		frame.pack();

		// put the frame in the middle of the display
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		// Show
		frame.setVisible(true);
	}

}