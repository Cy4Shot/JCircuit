package jcircuit.logic;

import java.awt.Image;

public record GateInstance(Gate gate, Vector2i pos) {
	public Image image() {
		return gate.image();
	}
	
	public Vector2i size(double scale) {
		return Vector2i.of(w(scale), h(scale));
	}

	public int w(double scale) {
		return gate.w(scale);
	}

	public int h(double scale) {
		return gate.h(scale);
	}
}
