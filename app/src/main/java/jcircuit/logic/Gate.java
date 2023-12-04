package jcircuit.logic;

import java.awt.Image;
import java.awt.Toolkit;

import jcircuit.App;

public class Gate {
	//@formatter:off
	public static final Gate AND = new Gate("and", 367, 271, Vector2i.of(0, 67), Vector2i.of(0, 201), Vector2i.of(367, 134));
	public static final Gate NAND = new Gate("nand", 367, 271, Vector2i.of(0, 67), Vector2i.of(0, 201), Vector2i.of(367, 134));
	public static final Gate OR = new Gate("or", 367, 271, Vector2i.of(0, 67), Vector2i.of(0, 201), Vector2i.of(367, 134));
	public static final Gate NOR = new Gate("nor", 367, 271, Vector2i.of(0, 67), Vector2i.of(0, 201), Vector2i.of(367, 134));
	public static final Gate XOR = new Gate("xor", 367, 271, Vector2i.of(0, 67), Vector2i.of(0, 201), Vector2i.of(367, 134));
	public static final Gate XNOR = new Gate("xnor", 367, 271, Vector2i.of(0, 67), Vector2i.of(0, 201), Vector2i.of(367, 134));
	public static final Gate BUFFER = new Gate("buffer", 334, 337, Vector2i.of(0, 168), Vector2i.of(334, 168));
	public static final Gate NOT = new Gate("not", 334, 337, Vector2i.of(0, 168), Vector2i.of(334, 168));
	//@formatter:on

	protected int width, height;
	protected Vector2i[] conns;
	protected String name;
	protected String text;
	protected transient Image img;
	
	Gate(String name) {
		this.name = name;
		this.text = "";
	}

	Gate(String name, int width, int height, Vector2i... conns) {
		this.width = width;
		this.height = height;
		this.conns = conns;
		this.name = name;
		this.text = "";
		updateImg();
	}

	Gate(String name, int width, int height, String text, Image img, Vector2i... conns) {
		this.width = width;
		this.height = height;
		this.conns = conns;
		this.name = name;
		this.text = text;
		this.img = img;
	}

	void updateImg() {
		if (isText())
			this.img = TexImageGen.create(text);
		else
			this.img = Toolkit.getDefaultToolkit()
					.getImage(App.class.getResource("/gate/" + name.toLowerCase() + ".png"));
	}

	public Image image() {
		if (img == null) {
			this.updateImg();
		}
		return this.img;
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

	public String name() {
		return name;
	}

	public boolean isText() {
		return this.text.length() > 0;
	}

	public String text() {
		return this.text;
	}
}