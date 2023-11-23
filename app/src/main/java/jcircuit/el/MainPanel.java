package jcircuit.el;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import jcircuit.MainFrame;
import jcircuit.logic.Gate;
import jcircuit.logic.GateInstance;
import jcircuit.logic.Rect;
import jcircuit.logic.Tool;
import jcircuit.logic.Vector2i;
import jcircuit.logic.Wiring;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = 6924426594452103926L;
	public List<GateInstance> gates = new ArrayList<GateInstance>();
	public List<Wiring> wirings = new ArrayList<Wiring>();

	volatile Vector2i draggedAt;
	Vector2i cameraPos = new Vector2i();
	double gateSize = 0.5;
	double grid = 16.75;
	Integer selected = -1;
	ImmutablePair<Integer, Integer> wiringConn = null;
	ImmutablePair<Integer, Integer> changeTurn = null;
	ImmutablePair<Integer, Integer> newTurn = null;
	Vector2i lastMouse = Vector2i.of(0, 0);

	BufferedImage canvas;
	Graphics2D graphics2D;
	boolean exporting = false;

	private MainFrame frame;

	public MainPanel(MainFrame frame) {
		this.frame = frame;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Tool tool = tool();
				Vector2i ep = Vector2i.of(e.getX(), e.getY());
				if (tool.equals(Tool.EMPTY)) {
					draggedAt = ep.subN(cameraPos);
				} else if (tool.equals(Tool.SELECT)) {
					selected = -1;
					for (int i = 0; i < wirings.size(); i++) {
						Wiring wire = wirings.get(i);
						for (int j = 0; j < wire.turns().size(); j++) {
							Vector2i p = wire.turns().get(j).addN(cameraPos);
							if (ep.isIn(p.addN(-10), p.addN(10))) {
								changeTurn = ImmutablePair.of(i, j);
								return;
							}
						}
					}
					for (int i = gates.size() - 1; i >= 0; i--) {
						GateInstance gate = gates.get(i);
						Vector2i pos = gate.pos();
						Vector2i pos1 = pos.addN(gate.w(gateSize), gate.h(gateSize));
						if (ep.subN(cameraPos).isIn(pos, pos1)) {
							selected = i;
							break;
						}
					}
					if (selected >= 0)
						draggedAt = ep.subN(gates.get(selected).pos());
					redraw();
				} else if (tool.equals(Tool.WIRE)) {
					for (int i = 0; i < gates.size(); i++) {
						GateInstance gate = gates.get(i);
						Vector2i pos = gate.pos().addN(cameraPos);
						Vector2i[] conns = gate.gate().conns();
						for (int j = 0; j < conns.length; j++) {
							Vector2i p = pos.addN(conns[j].mulN(gateSize));
							if (ep.isIn(p.addN(-10), p.addN(10))) {
								wiringConn = ImmutablePair.of(i, j);
								return;
							}
						}
					}
					for (int i = 0; i < wirings.size(); i++) {
						Wiring w = wirings.get(i);
						List<Vector2i> points = allTurns(w);
						for (int j = 0; j < points.size() - 1; j++) {
							Vector2i a = points.get(j);
							Vector2i b = points.get(j + 1);
							if (ep.isIn(a.x, a.y - 10, b.x, a.y + 10) || ep.isIn(b.x - 10, a.y, b.x + 10, b.y)) {
								newTurn = ImmutablePair.of(i, j);
								return;
							}
						}
					}
				} else if (tool.isGate) {
					Gate gate = tool.getGate();
					Vector2i offset = Vector2i.of(gate.w(), gate.h()).mul(gateSize).mul(0.5D);
					gates.add(new GateInstance(gate,
							Vector2i.of(e.getX(), e.getY()).sub(cameraPos).sub(offset).snapN(grid)));
					redraw();
				} else if (tool.equals(Tool.DELETE)) {
					for (int i = gates.size() - 1; i >= 0; i--) {
						GateInstance gate = gates.get(i);
						Vector2i pos = gate.pos().addN(cameraPos);
						Vector2i pos1 = pos.addN(gate.w(gateSize), gate.h(gateSize));
						if (ep.isIn(pos, pos1)) {
							gates.remove(i);
							List<Wiring> remove = new ArrayList<Wiring>();
							for (Wiring w : wirings) {
								if (w.input().getLeft() == i || w.output().getLeft() == i) {
									remove.add(w);
								} else {
									w.updateDelete(i);
								}
							}
							wirings.removeAll(remove);
							break;
						}
					}
					for (Wiring w : wirings) {
						List<Vector2i> remove = new ArrayList<Vector2i>();
						for (int i = 0; i < w.turns().size(); i++) {
							Vector2i p = w.turns().get(i);
							if (ep.isIn(p.addN(cameraPos).addN(-10), p.addN(cameraPos).addN(10))) {
								remove.add(p);
							}
						}
						w.turns().removeAll(remove);
					}
					redraw();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				Tool tool = tool();
				Vector2i ep = Vector2i.of(e.getX(), e.getY());
				if (tool.equals(Tool.WIRE)) {
					if (wiringConn != null) {
						for (int i = 0; i < gates.size(); i++) {
							GateInstance gate = gates.get(i);
							Vector2i pos = gate.pos().addN(cameraPos);
							Vector2i[] conns = gate.gate().conns();
							for (int j = 0; j < conns.length; j++) {
								Vector2i p = pos.addN(conns[j].mulN(gateSize));
								if (ep.isIn(p.addN(-10), p.addN(10))) {
									wirings.add(new Wiring(MutablePair.of(wiringConn.left, wiringConn.right),
											new ArrayList<>(), MutablePair.of(i, j)));
									break;
								}
							}
						}
					} else if (newTurn != null) {
						wirings.get(newTurn.getLeft()).turns().add(newTurn.getRight(), ep.subN(cameraPos).snapN(grid));
					}
					redraw();
					wiringConn = null;
					newTurn = null;
				} else if (tool.equals(Tool.SELECT)) {
					if (changeTurn != null) {
						wirings.get(changeTurn.getLeft()).turns().get(changeTurn.getRight())
								.set(ep.subN(cameraPos).snapN(grid));
					}
					redraw();
					changeTurn = null;
				}
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				frame.getCoordinateBar().setCoord(Vector2i.of(e.getX(), e.getY()).sub(cameraPos));
				lastMouse = Vector2i.of(e.getX(), e.getY());

				if (tool().isGate) {
					redraw();
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				lastMouse = Vector2i.of(e.getX(), e.getY());
				frame.getCoordinateBar().setCoord(lastMouse.subN(cameraPos));
				if (!SwingUtilities.isLeftMouseButton(e))
					return;
				if (tool().equals(Tool.EMPTY)) {
					MainPanel.this.cameraPos.set(lastMouse.sub(draggedAt));
					redraw();
				} else if (tool().equals(Tool.SELECT) && selected >= 0) {
					gates.get(selected).pos().set(lastMouse.sub(draggedAt).snapN(grid));
					redraw();
				} else if (tool().equals(Tool.SELECT) && changeTurn != null) {
					redraw();
				} else if (tool().equals(Tool.WIRE)) {
					redraw();
				}
			}
		});
		setLayout(null);
		setDoubleBuffered(true);
		setLocation(10, 10);
		setFocusable(true);
		requestFocus();
	}

	public void setCanvas(Dimension size) {
		if (canvas == null || canvas.getWidth() != size.width || canvas.getHeight() != size.height) {
			canvas = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
			graphics2D = canvas.createGraphics();
			graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics2D.setPaint(Color.white);
			graphics2D.fillRect(0, 0, size.width, size.height);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {

		// Background
		if (!exporting)
			setCanvas(getSize());
		g.drawImage(canvas, 0, 0, this);
		Graphics2D g2 = (Graphics2D) g;

		// Gates
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (GateInstance gate : gates) {
			Vector2i pos = gate.pos().addN(cameraPos);
			g2.drawImage(gate.image(), pos.x, pos.y, gate.w(gateSize), gate.h(gateSize), this);
		}

		// Wires
		g2.setStroke(new BasicStroke(2));
		for (Wiring wire : wirings) {
			List<Vector2i> points = allTurns(wire);

			for (int i = 1; i < points.size(); i++) {
				Vector2i a = points.get(i - 1);
				Vector2i b = points.get(i);

				// Horizontal -> Vertical
				g2.drawLine(a.x, a.y, b.x, a.y);
				g2.drawLine(b.x, a.y, b.x, b.y);
			}
		}
		g2.setStroke(new BasicStroke(1));

		// Tool Gizmos
		switch (tool()) {
		case EMPTY:
			break;
		case SELECT:
			for (int i = 0; i < gates.size(); i++) {
				GateInstance gate = gates.get(i);
				Vector2i pos = gate.pos().addN(cameraPos);
				g2.setColor(i == selected ? Color.MAGENTA : Color.BLUE);
				g2.drawRect(pos.x, pos.y, gate.w(gateSize), gate.h(gateSize));
				g2.setColor(Color.BLACK);
			}
			g2.setColor(Color.BLUE);
			for (Wiring wire : wirings) {
				for (Vector2i pos : wire.turns()) {
					Vector2i p = pos.addN(cameraPos);
					g2.drawRect(p.x - 10, p.y - 10, 20, 20);
				}
			}
			g2.setColor(Color.MAGENTA);
			g2.setStroke(new BasicStroke(2));
			if (changeTurn != null) {
				Wiring w = wirings.get(changeTurn.getLeft());
				List<Vector2i> points = allTurns(w);
				Function<Integer, Vector2i> getter = (i) -> changeTurn.getRight() + 1 == i ? lastMouse.snapN(grid)
						: points.get(i);

				for (int i = 1; i < points.size(); i++) {
					Vector2i a = getter.apply(i - 1);
					Vector2i b = getter.apply(i);

					// Horizontal -> Vertical
					g2.drawLine(a.x, a.y, b.x, a.y);
					g2.drawLine(b.x, a.y, b.x, b.y);
				}
			}
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(1));
			break;
		case WIRE:
			g2.setColor(Color.BLUE);
			for (GateInstance gate : gates) {
				Vector2i pos = gate.pos().addN(cameraPos);
				for (Vector2i conn : gate.gate().conns()) {
					Vector2i p = pos.addN(conn.mulN(gateSize));
					g2.drawRect(p.x - 10, p.y - 10, 20, 20);
				}
			}
			g2.setStroke(new BasicStroke(2));
			for (Wiring wire : wirings) {
				for (Vector2i pos : wire.turns()) {
					Vector2i p = pos.addN(cameraPos);
					g2.fill(new Ellipse2D.Double(p.x - 5, p.y - 5, 10, 10));
				}
			}
			g2.setColor(Color.MAGENTA);
			if (wiringConn != null) {
				Vector2i pos = pointFromIds(wiringConn).add(cameraPos);
				g2.drawLine(pos.x, pos.y, lastMouse.x, lastMouse.y);
			}
			if (newTurn != null) {
				Wiring w = wirings.get(newTurn.getLeft());
				List<Vector2i> points = allTurns(w);
				Vector2i a = points.get(newTurn.getRight());
				Vector2i b = points.get(newTurn.getRight() + 1);
				Vector2i pos = lastMouse.snapN(grid);
				g2.drawLine(a.x, a.y, pos.x, a.y);
				g2.drawLine(pos.x, a.y, pos.x, pos.y);
				g2.drawLine(pos.x, pos.y, b.x, pos.y);
				g2.drawLine(b.x, pos.y, b.x, b.y);
			}
			g2.setStroke(new BasicStroke(1));
			g2.setColor(Color.BLACK);
			break;
		case DELETE:
			g2.setColor(Color.RED);
			for (GateInstance gate : gates) {
				Vector2i pos = gate.pos().addN(cameraPos);
				g2.drawRect(pos.x, pos.y, gate.w(gateSize), gate.h(gateSize));
			}
			for (Wiring wire : wirings) {
				for (Vector2i pos : wire.turns()) {
					Vector2i p = pos.addN(cameraPos);
					g2.drawRect(p.x - 10, p.y - 10, 20, 20);
				}
			}
			g2.setColor(Color.BLACK);
			break;
		case TEXT:
			break;
		default:
			Gate gate = tool().getGate();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			Vector2i pos = lastMouse.subN(Vector2i.of(gate.w(), gate.h()).mul(gateSize).mul(0.5D)).snapN(grid);
			g2.drawImage(gate.image(), pos.x, pos.y, gate.w(gateSize), gate.h(gateSize), this);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			break;
		}
	}

	private List<Vector2i> allTurns(Wiring w) {
		List<Vector2i> points = new ArrayList<Vector2i>();
		points.add(pointFromIds(w.input()));
		points.addAll(w.turns());
		points.add(pointFromIds(w.output()));
		return points.stream().map(a -> a.addN(cameraPos)).collect(Collectors.toList());
	}

	private Vector2i pointFromIds(Pair<Integer, Integer> ids) {
		GateInstance gate = gates.get(ids.getLeft());
		return gate.pos().addN(gate.gate().conn(ids.getRight()).mulN(gateSize));
	}

	public void redraw() {
		Dimension size = getSize();
		graphics2D.setPaint(Color.white);
		graphics2D.fillRect(0, 0, size.width, size.height);
		repaint();
	}

	public void clear() {
		gates.clear();
		wirings.clear();
		redraw();
	}

	public Tool tool() {
		return this.frame.getSelectBar().selected;
	}

	public JsonObject save() {
		Gson gson = new GsonBuilder().create();
		JsonObject obj = new JsonObject();
		obj.add("gates", gson.toJsonTree(frame.getMainPanel().gates));
		obj.add("wirings", gson.toJsonTree(frame.getMainPanel().wirings));
		return obj;
	}

	public void load(JsonObject obj) {
		Gson gson = new GsonBuilder().create();
		gates = gson.fromJson(obj.getAsJsonArray("gates"), new TypeToken<ArrayList<GateInstance>>() {
		}.getType());
		wirings = gson.fromJson(obj.getAsJsonArray("wirings"), new TypeToken<ArrayList<Wiring>>() {
		}.getType());
		cameraPos = new Vector2i();
		redraw();
	}

	// ......
	final double latexScale = 43.41727D / 367D * 0.0351459803514598D * 2;

	public String latex() {
		StringBuilder builder = new StringBuilder();
		builder.append("\\usepackage{circuitikz}\n\n");
		builder.append("\\begin{center}\n");
		builder.append("\t\\begin{circuitikz} \\draw");

		for (int i = 0; i < gates.size(); i++) {
			GateInstance gate = gates.get(i);
			Vector2i pos = gate.pos().addN(gate.size(gateSize).mulN(0.5D));
			if (gate.gate().isBinary()) {
				// Yeah... what.
				// I have literally no clue why, but it solves the problem in all cases.
				pos.add(gate.w(gateSize) * 3 / 8, 0);
			}

			builder.append("\n\t(");
			builder.append((double) pos.x * latexScale);
			builder.append(", ");
			builder.append((double) pos.y * -latexScale);
			builder.append(") node[");
			builder.append(gate.gate().name().toLowerCase());
			builder.append(" port] (g");
			builder.append(i);
			builder.append(") {}");
		}

		for (int i = 0; i < wirings.size(); i++) {
			Wiring wire = wirings.get(i);
			builder.append("\n\t(g");
			builder.append(wire.input().getLeft());
			builder.append(".");
			builder.append(wire.isAOutput(gates) ? "out" : "in " + (wire.input().getRight() + 1));
			builder.append(") -| (");
			for (Vector2i pos : wire.turns()) {
				builder.append((double) pos.x * latexScale);
				builder.append(", ");
				builder.append((double) pos.y * -latexScale);
				builder.append(") -| (");
			}
			builder.append("g");
			builder.append(wire.output().getLeft());
			builder.append(".");
			builder.append(wire.isBOutput(gates) ? "out" : "in " + (wire.output().getRight() + 1));
			builder.append(")");
		}

		builder.append(";\n");
		builder.append("\t\\end{circuitikz}\n");
		builder.append("\\end{center}");
		return builder.toString();
	}

	public Rect minMaxCoord() {
		Rect rect = new Rect();
		gates.forEach(g -> rect.expand(g.pos(), g.size(gateSize)));
		wirings.forEach(w -> w.turns().forEach(t -> rect.expand(t, Vector2i.of(0))));
		return rect.border(20);
	}

	public BufferedImage image() {
		Rect rect = minMaxCoord();
		Vector2i oldPos = new Vector2i(cameraPos);

		cameraPos = rect.min().negate();
		setCanvas(new Dimension(rect.w(), rect.h()));
		exporting = true;

		BufferedImage img = new BufferedImage(rect.w(), rect.h(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		paint(g);

		g.dispose();
		cameraPos.set(oldPos);
		exporting = false;

		return img;
	}

	// Stop clipping when exporting.
	@Override
	public int getWidth() {
		if (exporting)
			return Integer.MAX_VALUE;
		return super.getWidth();
	}

	@Override
	public int getHeight() {
		if (exporting)
			return Integer.MAX_VALUE;
		return super.getHeight();
	}
}