package jcircuit.logic;

public class Rect {

	int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
	int maxX = -Integer.MAX_VALUE, maxY = -Integer.MAX_VALUE;

	public Rect expand(Vector2i pos, Vector2i size) {
		if (minX > pos.x) {
			minX = pos.x;
		} else if (maxX < pos.x + size.x) {
			maxX = pos.x + size.x;
		}

		if (minY > pos.y) {
			minY = pos.y;
		} else if (maxY < pos.y + size.y) {
			maxY = pos.y + size.y;
		}
		return this;
	}

	public Rect border(int b) {
		minX -= b;
		minY -= b;
		maxX += b;
		maxY += b;
		return this;
	}
	
	public int w() {
		return Math.abs(maxX - minX);
	}
	
	public int h() {
		return Math.abs(maxY - minY);
	}
	
	public Vector2i min() {
		return Vector2i.of(minX, minY);
	}
}
