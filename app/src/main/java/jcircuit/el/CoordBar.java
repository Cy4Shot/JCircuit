package jcircuit.el;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import jcircuit.App;
import jcircuit.logic.Vector2i;

public class CoordBar extends JToolBar {
	private static final long serialVersionUID = -3379603174188645836L;
	private JLabel coordinates;

	public CoordBar() {
		JLabel coordinatePic = new JLabel(new ImageIcon(App.class.getResource("/icon/coord.png")));
		coordinatePic.setBorder(new EmptyBorder(0, 10, 0, 0));
		this.add(coordinatePic);

		coordinates = new JLabel();
		coordinates.setText("  0 x 0  ");
		this.add(coordinates);

		this.setFloatable(false);
		this.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BLACK));
	}

	public void setCoord(Vector2i coord) {
		coordinates.setText(String.format("  %d x %d  ", coord.x, coord.y));
	}

	public JToolBar getCoordinateBar() {
		return this;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getParent().getWidth(), super.getPreferredSize().height);
	}
}