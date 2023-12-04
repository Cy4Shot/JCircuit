package jcircuit.logic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import jcircuit.App;

public class CustomGate extends Gate {

	private static final double grid = App.frame.getMainPanel().grid;
	private static final int lineLength = 20;
	private static final int lineSep = (int) (grid * 4);
	private static final int padding = (int) (grid * 2);

	public List<MutablePair<String, MutablePair<Vector2i, Boolean>>> cconns;
	List<MutablePair<Vector2i, Boolean>> bconns;
	transient List<Image> inputs, outputs;
	transient Image nameImg;
	public Generator gen;

	public CustomGate(List<String> inputs, List<String> outputs, String name) {
		super(name);
		this.gen = new Generator(inputs, outputs, name);

		// Create Images
		this.nameImg = TexImageGen.create(name, 50);
		this.inputs = inputs.stream().map(s -> TexImageGen.create(s, 35)).collect(Collectors.toList());
		this.outputs = outputs.stream().map(s -> TexImageGen.create(s, 35)).collect(Collectors.toList());

		// Create Dimension
		this.width = this.nameImg.getWidth(null) + (this.inputs.isEmpty() ? 0 : lineLength)
				+ (this.outputs.isEmpty() ? 0 : lineLength) + padding * 4;
		this.height = this.nameImg.getHeight(null) + Math.max(this.inputs.size(), this.outputs.size()) * lineSep
				+ padding * 2;

		// Create Conns
		this.bconns = new ArrayList<>();
		this.cconns = new ArrayList<>();
		int firstSep = this.nameImg.getHeight(null) + padding;
		firstSep = (int) (grid * (Math.round((double) firstSep / grid))) + lineSep;
		for (int i = 0; i < this.inputs.size(); i++) {
			MutablePair<Vector2i, Boolean> m = MutablePair.of(Vector2i.of(0, firstSep + lineSep * i), false);
			this.cconns.add(MutablePair.of(inputs.get(i), m));
			this.bconns.add(m);
		}
		for (int i = 0; i < this.outputs.size(); i++) {
			MutablePair<Vector2i, Boolean> m = MutablePair.of(Vector2i.of(width, firstSep + lineSep * i), true);
			this.cconns.add(MutablePair.of(outputs.get(i), m));
			this.bconns.add(m);
		}
	}

	public record Generator(List<String> inputs, List<String> outputs, String name) {
		public CustomGate generate() {
			return new CustomGate(inputs, outputs, name);
		}
	}

	public Vector2i tl() {
		int hi = this.inputs.isEmpty() ? 0 : 1;
		return Vector2i.of(hi * lineLength, 0);
	}

	public Vector2i wh() {
		int hi = this.inputs.isEmpty() ? 0 : 1;
		int ho = this.outputs.isEmpty() ? 0 : 1;
		return Vector2i.of(width - (hi + ho) * lineLength, height);
	}

	public Vector2i tt() {
		int hi = this.inputs.isEmpty() ? 0 : 1;
		return Vector2i.of(hi * lineLength + padding * 2, padding);
	}

	public Vector2i ts() {
		return Vector2i.of(nameImg.getWidth(null), nameImg.getHeight(null));
	}

	public int fs() {
		int firstSep = this.nameImg.getHeight(null) + padding + lineSep;
		return (int) (grid * Math.round((double) firstSep / grid));
	}

	public Vector2i op(int i) {
		int h = fs() + lineSep * i;
		Image im = outputs.get(i);
		return Vector2i.of(width - lineLength - padding - im.getWidth(null), h - im.getHeight(null) / 2);
	}
	
	public Vector2i od(int i) {
		Image im = outputs.get(i);
		return Vector2i.of(im.getWidth(null), im.getHeight(null));
	}
	
	public Vector2i ip(int i) {
		int h = fs() + lineSep * i;
		Image im = inputs.get(i);
		return Vector2i.of(lineLength + padding, h - im.getHeight(null) / 2);
	}
	
	public Vector2i id(int i) {
		Image im = inputs.get(i);
		return Vector2i.of(im.getWidth(null), im.getHeight(null));
	}
	
	public Pair<Vector2i, Vector2i> ol(int i) {
		int h = fs() + lineSep * i;
		return Pair.of(Vector2i.of(width - lineLength, h), Vector2i.of(width, h));
	}
	
	public Pair<Vector2i, Vector2i> il(int i) {
		int h = fs() + lineSep * i;
		return Pair.of(Vector2i.of(0, h), Vector2i.of(lineLength, h));
	}

	@Override
	public Image image() {
		BufferedImage image = new BufferedImage(width + 3, height + 3, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width + 3, height + 3);
		g.setColor(Color.BLACK);
		int hi = this.inputs.isEmpty() ? 0 : 1;
		int ho = this.outputs.isEmpty() ? 0 : 1;
		g.translate(0.5D, 0.5D);
		g.drawRect(hi * lineLength, 0, width - (hi + ho) * lineLength, height);
		g.drawImage(nameImg, hi * lineLength + padding * 2, padding, nameImg.getWidth(null), nameImg.getHeight(null),
				null);

		int i = 0, o = 0;
		int firstSep = fs();
		g.setStroke(new BasicStroke(4));
		for (MutablePair<Vector2i, Boolean> conn : bconns) {
			if (conn.right) {
				Image im = outputs.get(o);
				int h = firstSep + lineSep * o;
				g.drawImage(im, width - lineLength - padding - im.getWidth(null), h - im.getHeight(null) / 2,
						im.getWidth(null), im.getHeight(null), null);
				h += 2;
				g.drawLine(width - lineLength, h, width, h);
				o++;
			} else {
				Image im = inputs.get(i);
				int h = firstSep + lineSep * i;
				g.drawImage(im, lineLength + padding, h - im.getHeight(null) / 2, im.getWidth(null), im.getHeight(null),
						null);
				h += 2;
				g.drawLine(0, h, lineLength, h);
				i++;
			}
		}
		return image;
	}

	@Override
	public int w() {
		return this.width;
	}

	@Override
	public int h() {
		return this.height;
	}

	@Override
	public Vector2i[] conns() {
		return this.bconns.stream().map(p -> p.left).toArray(Vector2i[]::new);
	}

	@Override
	public Vector2i conn(int connId) {
		return this.bconns.get(connId).left;
	}

	@Override
	public boolean isOutput(int connId) {
		return this.bconns.get(connId).right;
	}

	@Override
	public boolean isBinary() {
		return false;
	}
}
