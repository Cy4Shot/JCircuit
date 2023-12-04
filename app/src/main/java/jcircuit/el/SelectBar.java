package jcircuit.el;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import jcircuit.App;
import jcircuit.MainFrame;
import jcircuit.logic.CustomGate;
import jcircuit.logic.Tool;

public class SelectBar extends JPanel {

	private static final long serialVersionUID = 3102205887508093912L;
	private static final int border = 20;

	public Tool selected = Tool.EMPTY;
	private File save = null;

	JLabel selectedText;
	final MainFrame frame;

	public SelectBar(MainFrame frame) {
		this.frame = frame;

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		this.setBorder(new EmptyBorder(border, border, border, border));

		// Create Title
		JLabel title = new JLabel("Tools & Logic");
		title.setFont(title.getFont().deriveFont(30f));
		title.setBorder(new EmptyBorder(0, 0, border, 0));
		this.add(title);

		// Create Selected Text
		selectedText = new JLabel();
		title.setFont(title.getFont().deriveFont(20f));
		selectedText.setBorder(new EmptyBorder(0, 0, border, 0));
		select(Tool.EMPTY);
		this.add(selectedText);

		// Clear Button
		JButton clear = new JButton("Clear Selected Tool");
		clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				select(Tool.EMPTY);
			}
		});
		this.add(clear);

		// Create Buttons
		JPanel content = new JPanel();
		content.setLayout(new GridLayout(0, 2));
		for (Tool g : Tool.values()) {
			if (g.isEmpty())
				continue;
			content.add(new SelectBarItem(g));
		}
		this.add(content);

		// More Buttons
		JPanel extras = new JPanel();
		extras.setBorder(new EmptyBorder(20, 0, 0, 0));
		extras.setLayout(new GridLayout(0, 1));
		extras.add(new ActionItem("New", (e) -> {
			int create = JOptionPane.showConfirmDialog(frame,
					"<html>Are you sure?<br/>This will clear the document, without saving changes.</html>");
			if (create == 0) {
				frame.getMainPanel().clear();
				save = null;
			}
		}));
		extras.add(new ActionItem("Open", (e) -> {
			load();
		}));
		extras.add(new JSeparator());
		extras.add(new ActionItem("Save", (e) -> {
			if (save == null)
				saveAs();
			else
				save();
		}));
		extras.add(new ActionItem("Save As", (e) -> {
			saveAs();
		}));
		extras.add(new ActionItem("Export", (e) -> {
			export();
		}));
		extras.add(new JSeparator());
		extras.add(new JLabel("JCircuit by Cy4"));
		this.add(extras);

		// Layout
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(title, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(selectedText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(clear, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(content, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(extras, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(title).addComponent(selectedText)
				.addComponent(clear).addComponent(content).addComponent(extras));

		// Mouse
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				select(Tool.EMPTY);
			}
		});
	}

	public void select(Tool tool) {

		if (tool == Tool.ADD) {
			new CustomGateEditor((i, o, n) -> {
				App.frame.getMainPanel().customGates.add(new CustomGate(i, o, n));
			});
			return;
		}

		this.selected = tool;

		if (tool == Tool.EMPTY) {
			this.selectedText.setText("No tool selected");
		} else {
			if (tool.isGate()) {
				this.selectedText.setText("Selected: Place " + tool.name());
			} else {
				this.selectedText.setText("Selected: " + StringUtils.capitalize(tool.name().toLowerCase()));
			}
		}

		if (this.frame.getMainPanel() != null)
			this.frame.getMainPanel().redraw();
	}

	public class SelectBarItem extends JButton {

		private static final long serialVersionUID = 6763158559779632135L;
		private static final int IMAGE_SIZE = 50;

		public SelectBarItem(Tool tool) {
			super();

			String folder = tool.isGate() ? "/gate/" : "/tool/";
			ImageIcon img = new ImageIcon(App.class.getResource(folder + tool.name().toLowerCase() + ".png"));
			Image scaled = img.getImage().getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH);
			setIcon(new ImageIcon(scaled));

			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					select(tool);
				}
			});
		}
	}

	public class ActionItem extends JButton {

		private static final long serialVersionUID = 2619260886884076329L;

		public ActionItem(String name, Consumer<ActionEvent> action) {
			super(name);
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					action.accept(e);
				}
			});
		}

	}

	public void load() {
		JFileChooser chooser = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("JCircuit File", new String[] { "jcs" });
		chooser.setFileFilter(filter);
		chooser.addChoosableFileFilter(filter);
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			save = chooser.getSelectedFile();
			if (!FilenameUtils.getExtension(save.getName()).equalsIgnoreCase("jcs")) {
				JOptionPane.showMessageDialog(frame, "File does not have a .jcs extension.");
				return;
			}

			try {
				frame.getMainPanel()
						.load(JsonParser.parseReader(new JsonReader(new FileReader(save))).getAsJsonObject());
				select(Tool.EMPTY);
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(frame, e.getMessage());
			}
		}
	}

	public void save() {
		try (Writer writer = new FileWriter(save)) {
			new Gson().toJson(frame.getMainPanel().save(), writer);
			JOptionPane.showMessageDialog(frame,
					"<html>Successfully saved to:<br/>" + save.getAbsolutePath() + "</html>");
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(frame, e1.getMessage());
		}
	}

	public void saveAs() {
		JFileChooser chooser = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("JCircuit File", new String[] { "jcs" });
		chooser.setFileFilter(filter);
		chooser.addChoosableFileFilter(filter);
		if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			save = chooser.getSelectedFile();
			if (!FilenameUtils.getExtension(save.getName()).equalsIgnoreCase("jcs")) {
				save = new File(save.toString() + ".jcs");
			}
			save();
		}
	}

	public void export() {
		String[] options = { "Export to LaTeX", "Export as PNG", "Cancel" };
		JPanel p = new JPanel();
		p.add(new JLabel("Select an export option:"));
		int result = JOptionPane.showOptionDialog(null, p, "Export", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, null);
		if (result == JOptionPane.YES_OPTION) {
			JPanel panel = new JPanel();
			GroupLayout layout = new GroupLayout(panel);
			panel.setLayout(layout);

			JLabel title = new JLabel("Find LaTeX below:");
			title.setBorder(new EmptyBorder(0, 0, 20, 0));

			final String latex = frame.getMainPanel().latex();

			JTextArea textarea = new JTextArea();
			textarea.setEditable(false);
			textarea.setText(latex);
			textarea.setBorder(new EmptyBorder(10, 10, 10, 10));
			JScrollPane area = new JScrollPane(textarea);
			Dimension d = area.getPreferredSize();
			area.setPreferredSize(new Dimension(Math.min(d.width, 600), Math.min(400, d.height)));

			JLabel sep = new JLabel();
			sep.setBorder(new EmptyBorder(0, 0, 10, 0));

			JPanel cp = new JPanel();
			cp.setLayout(new GridLayout());
			JButton copy = new JButton("Copy to Clipboard");
			copy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					StringSelection stringSelection = new StringSelection(latex);
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(stringSelection, null);
					copy.setText("Copied!");
				}
			});
			cp.add(copy);
			cp.setBorder(new EmptyBorder(0, 0, 10, 0));

			panel.add(title);
			panel.add(area);
			panel.add(sep);
			panel.add(cp);

			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(title, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
							GroupLayout.PREFERRED_SIZE)
					.addComponent(area, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
							GroupLayout.PREFERRED_SIZE)
					.addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
							GroupLayout.PREFERRED_SIZE)
					.addComponent(cp, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
							GroupLayout.PREFERRED_SIZE));
			layout.setHorizontalGroup(layout.createParallelGroup().addComponent(title).addComponent(area)
					.addComponent(sep).addComponent(cp));

			JOptionPane.showConfirmDialog(null, panel, "Export", -1);
		} else if (result == JOptionPane.NO_OPTION) {
			select(Tool.EMPTY);
			JFileChooser chooser = new JFileChooser();
			FileFilter filter = new FileNameExtensionFilter("Portable Network Graphics (PNG)", new String[] { "png" });
			chooser.setFileFilter(filter);
			chooser.addChoosableFileFilter(filter);
			if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
				File save = chooser.getSelectedFile();
				if (!FilenameUtils.getExtension(save.getName()).equalsIgnoreCase("png")) {
					save = new File(save.toString() + ".png");
				}

				try {
					ImageIO.write(frame.getMainPanel().image(), "png", save);
					JOptionPane.showMessageDialog(frame,
							"<html>Successfully exported to:<br/>" + save.getAbsolutePath() + "</html>");
				} catch (IOException e) {
					JOptionPane.showMessageDialog(frame, e.getMessage());
				}
			}
		}
	}
}