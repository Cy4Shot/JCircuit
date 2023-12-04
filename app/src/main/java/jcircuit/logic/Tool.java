package jcircuit.logic;

import java.awt.Image;
import java.util.function.Supplier;

import javax.swing.JOptionPane;

import jcircuit.App;
import jcircuit.el.MainPanel;

public enum Tool {
	EMPTY(null), SELECT(null), WIRE(null), NODE(null), TEXT(() -> {
		String t = (String) JOptionPane.showInputDialog(App.frame, "Input TeX:", "Text Tool", -1, null, null, "");
		if (t == null || t.length() == 0)
			return null;
		MainPanel p = App.frame.getMainPanel();
		Image im = TexImageGen.create(t);
		int w = im.getWidth(null);
		int h = im.getHeight(null);
		int h1 = (int) (p.grid * 2 * (Math.round(h / 4 / p.grid)));
		return new Gate("", w, h, t, im, Vector2i.of(0, h1), Vector2i.of(w, h1));
	}),
	AND(() -> Gate.AND),
	NAND(() -> Gate.NAND),
	OR(() -> Gate.OR),
	NOR(() -> Gate.NOR),
	XOR(() -> Gate.XOR),
	XNOR(() -> Gate.XNOR),
	BUFFER(() -> Gate.BUFFER),
	NOT(() -> Gate.NOT),
	ADD(null),
	CUSTOM(() -> {
		String[] options = App.frame.getMainPanel().customGates.stream().map(c -> c.name).toArray(String[]::new);
		if (options.length == 0) {
			JOptionPane.showMessageDialog(App.frame, "You need to create a custom gate first.");
			return null;
		}
		Object selectionObject = JOptionPane.showInputDialog(App.frame, "Choose", "Menu", JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		if (selectionObject == null)
			return null;
		String selectionString = selectionObject.toString();
		return App.frame.getMainPanel().customGates.stream().filter(p -> p.name == selectionString)
				.toArray(CustomGate[]::new)[0];
	}),
	DELETE(null);

	Supplier<Gate> gate;

	Tool(Supplier<Gate> gate) {
		this.gate = gate;
	}

	public boolean isEmpty() {
		return equals(EMPTY);
	}

	public boolean isGate() {
		return gate != null;
	}

	public Gate getGate() {
		if (gate == null)
			return null;

		return gate.get();
	}
}