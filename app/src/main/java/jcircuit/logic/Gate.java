package jcircuit.logic;

import java.awt.Image;
import java.awt.Toolkit;

import jcircuit.App;

public enum Gate {
	AND(367, 271, Vector2i.of(0, 67), Vector2i.of(0, 201), Vector2i.of(367, 134)),
	NAND(367, 271, Vector2i.of(0, 67), Vector2i.of(0, 201), Vector2i.of(367, 134)),
	OR(367, 271, Vector2i.of(0, 67), Vector2i.of(0, 201), Vector2i.of(367, 134)),
	NOR(367, 271, Vector2i.of(0, 67), Vector2i.of(0, 201), Vector2i.of(367, 134)),
	XOR(367, 271, Vector2i.of(0, 67), Vector2i.of(0, 201), Vector2i.of(367, 134)),
	XNOR(367, 271, Vector2i.of(0, 67), Vector2i.of(0, 201), Vector2i.of(367, 134)),
	BUFFER(334, 337, Vector2i.of(0, 168), Vector2i.of(334, 168)),
	NOT(334, 337, Vector2i.of(0, 168), Vector2i.of(334, 168));

	final int width, height;
	final Vector2i[] conns;

	Gate(int width, int height, Vector2i... conns) {
		this.width = width;
		this.height = height;
		this.conns = conns;
	}
	
	public Image image() {
		return Toolkit.getDefaultToolkit()
				.getImage(App.class.getResource("/gate/" + name().toLowerCase() + ".png"));
	}

	public int w() {
		return this.width;
	}

	public int h() {
		return this.height;
	}
	
	public int w(double scale) {
		return (int) (this.width * scale);
	}

	public int h(double scale) {
		return (int) (this.height * scale);
	}

	public Vector2i conn(int connId) {
		return conns[connId];
	}
	
	public Vector2i[] conns() {
		return this.conns;
	}

	public boolean isOutput(int connId) {
		return connId == this.conns.length - 1;
	}
	
	public boolean isBinary() {
		return conns.length == 3;
	}
}