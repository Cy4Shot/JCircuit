package jcircuit.logic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;

import org.w3c.dom.Document;

import jcircuit.App;
import net.sourceforge.jeuclid.MathMLParserSupport;
import net.sourceforge.jeuclid.MutableLayoutContext;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.context.Parameter;
import net.sourceforge.jeuclid.converter.Converter;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;

public class TexImageGen extends JProgressBar {

	private static final long serialVersionUID = -1895595446308650019L;

	static SnuggleEngine engine = null;

	public TexImageGen() {
		setMinimum(0);
		setMaximum(5);
		setValue(0);
		setPreferredSize(new Dimension(400, 100));
		setLocation(0, 0);
		setString("Loading LaTeX...");
		setStringPainted(true);
		setUI(new BasicProgressBarUI() {
			@Override
			protected Color getSelectionForeground() {
				return Color.BLACK;
			}

			@Override
			protected Color getSelectionBackground() {
				return Color.BLACK;
			}
		});
	}

	@Override
	public void setValue(int n) {
		super.setValue(n);
		paint(getGraphics());
	}

	public static Image create(String tex, float size) {
		JDialog d = new JDialog(App.frame, "Loading TeX...");
		d.setLayout(new GridBagLayout());
		TexImageGen gen = new TexImageGen();
		d.add(gen);
		d.pack();
		d.setSize(d.getWidth() + 20, d.getHeight() + 20);
		d.setLocationRelativeTo(null);
		d.setVisible(true);
		Image i = gen.gen(tex, size);
		d.dispose();
		return i;
	}

	public static Image create(String tex) {
		return create(tex, 75f);
	}

	public Image gen(String tex, float size) {
		try {
			if (engine == null)
				engine = new SnuggleEngine();
			setValue(1);
			SnuggleSession session = engine.createSession();
			SnuggleInput input = new SnuggleInput("$$ " + tex + " $$");
			session.parseInput(input);
			String xmlString = session.buildXMLString();
			setValue(2);
			Document c = MathMLParserSupport.parseString(xmlString);
			setValue(3);
			MutableLayoutContext params = new LayoutContextImpl(LayoutContextImpl.getDefaultLayoutContext());
			params.setParameter(Parameter.MATHSIZE, size);
			params.setParameter(Parameter.ANTIALIAS, true);
			setValue(4);
			return Converter.getInstance().render(c, params);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
