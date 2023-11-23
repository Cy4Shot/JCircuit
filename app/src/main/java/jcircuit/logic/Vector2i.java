package jcircuit.logic;

public class Vector2i {

	public int x;
	public int y;

	public Vector2i() {
		this.x = 0;
		this.y = 0;
	}

	public Vector2i(Vector2i other) {
		this.x = other.x;
		this.y = other.y;
	}

	public Vector2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Vector2i snap(double grid) {
		this.x = (int) (Math.floor(((double) x + grid / 2) / grid) * grid);
		this.y = (int) (Math.floor(((double) y + grid / 2) / grid) * grid);
		return this;
	}

	public Vector2i snapN(double grid) {
		return new Vector2i(this).snap(grid);
	}
	
	public Vector2i sub(int v) {
		this.x -= v;
		this.y -= v;
		return this;
	}
	
	public Vector2i sub(int x, int y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	public Vector2i sub(Vector2i other) {
		this.x -= other.x;
		this.y -= other.y;
		return this;
	}

	public Vector2i subN(Vector2i other) {
		return new Vector2i(this).sub(other);
	}
	
	public Vector2i add(int v) {
		this.x += v;
		this.y += v;
		return this;
	}

	public Vector2i add(int x, int y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public Vector2i add(Vector2i other) {
		this.x += other.x;
		this.y += other.y;
		return this;
	}

	public Vector2i addN(int x, int y) {
		return new Vector2i(this.x + x, this.y + y);
	}

	public Vector2i addN(Vector2i other) {
		return new Vector2i(this).add(other);
	}

	public Vector2i addN(int v) {
		return addN(v, v);
	}

	public Vector2i mul(double scale) {
		this.x *= scale;
		this.y *= scale;
		return this;
	}

	public Vector2i mulN(double scale) {
		return new Vector2i(this).mul(scale);
	}

	public Vector2i set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Vector2i set(Vector2i other) {
		this.x = other.x;
		this.y = other.y;
		return this;
	}
	
	public Vector2i negate() {
		this.x = -this.x;
		this.y = -this.y;
		return this;
	}

	public boolean isIn(int x1, int y1, int x2, int y2) {
		return isIn(Vector2i.of(x1, y1), Vector2i.of(x2, y2));
	}

	public boolean isIn(Vector2i p1, Vector2i p2) {
		int minX = Math.min(p1.x, p2.x);
		int maxX = Math.max(p1.x, p2.x);
		int minY = Math.min(p1.y, p2.y);
		int maxY = Math.max(p1.y, p2.y);

		return x >= minX && x <= maxX && y >= minY && y <= maxY;
	}
	
	public static Vector2i of(int v) {
		return new Vector2i(v, v);
	}

	public static Vector2i of(int x, int y) {
		return new Vector2i(x, y);
	}

	@Override
	public String toString() {
		return String.format("(%d, %d)", x, y);
	}

}
