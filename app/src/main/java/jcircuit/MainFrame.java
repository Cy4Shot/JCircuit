package jcircuit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import jcircuit.el.CoordBar;
import jcircuit.el.MainPanel;
import jcircuit.el.SelectBar;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 3994401211578660556L;

	private JPanel contentPane;
	private MainPanel mainPanel;
	private SelectBar selectBar;
	private CoordBar coordinateBar;
	private JScrollPane sp;

	private final int CONTENT_PANE_WIDTH = 1300;
	private final int CONTENT_PANE_HEIGHT = 700;

	private int mainPanelWidth;
	private int mainPanelHeight;
	private final Color background = Color.GRAY;

	// ContentPane > Toolbar, cc, mainPanel,
	public MainFrame() {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		mainPanelWidth = dim.width - 150;
		mainPanelHeight = dim.height - 160;
		contentPane = new JPanel();
		SpringLayout layout = new SpringLayout();
		contentPane.setLayout(layout);

		// create a tool bar
		selectBar = new SelectBar(this);

		// create coordinate bar at the bottom
		coordinateBar = new CoordBar();

		// construct the panels needed. (mainPanel COMES LAST)
		mainPanel = new MainPanel(this);

		sp = new JScrollPane();
		sp.setLocation(10, 10);
		sp.setViewportView(mainPanel);
		sp.setSize(mainPanelWidth, mainPanelHeight);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		contentPane.add(sp);
		contentPane.add(coordinateBar);
		contentPane.setBackground(background);

		layout.putConstraint(SpringLayout.SOUTH, coordinateBar, 0, SpringLayout.SOUTH, contentPane);
		layout.putConstraint(SpringLayout.EAST, contentPane, 0, SpringLayout.EAST, sp);
		layout.putConstraint(SpringLayout.SOUTH, sp, 0, SpringLayout.NORTH, coordinateBar);
		layout.putConstraint(SpringLayout.NORTH, sp, 0, SpringLayout.NORTH, contentPane);

		// add listeners to buttons
		this.addWindowListener(new WindowCloser());

		// set components into the contentPane
		this.add(contentPane);
		this.add(selectBar, BorderLayout.LINE_START);

		this.setSize(CONTENT_PANE_WIDTH, CONTENT_PANE_HEIGHT);
		this.setPreferredSize(new Dimension(CONTENT_PANE_WIDTH, CONTENT_PANE_HEIGHT));

	}

	private class WindowCloser extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent event) {
			System.exit(0);
		}
	}

	public SelectBar getSelectBar() {
		return this.selectBar;
	}

	public CoordBar getCoordinateBar() {
		return this.coordinateBar;
	}

	public MainPanel getMainPanel() {
		return this.mainPanel;
	}

	public JScrollPane getSP() {
		return this.sp;
	}
}