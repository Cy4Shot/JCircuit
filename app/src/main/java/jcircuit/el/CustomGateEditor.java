package jcircuit.el;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.function.TriConsumer;

import jcircuit.App;

public class CustomGateEditor {

	private static final int border = 20;

	public CustomGateEditor(TriConsumer<List<String>, List<String>, String> create) {

		EditableList inputs = new EditableList();
		EditableList outputs = new EditableList();

		JFrame frame = new JFrame();

		JPanel top = new JPanel();
		top.setBorder(new EmptyBorder(border, border, border, border));
		top.setLayout(new GridLayout(1, 2));

		JPanel leftT = new JPanel();
		leftT.setLayout(new BoxLayout(leftT, BoxLayout.Y_AXIS));
		leftT.add(new JLabel("Inputs:"));
		leftT.add(new JScrollPane(inputs));
		top.add(leftT);

		JPanel leftB = new JPanel();
		leftB.setLayout(new BoxLayout(leftB, BoxLayout.Y_AXIS));
		leftB.add(new JLabel("Outputs:"));
		leftB.add(new JScrollPane(outputs));
		top.add(leftB);

		JButton confirm = new JButton("Next");
		confirm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				String t = (String) JOptionPane.showInputDialog(App.frame, "Gate name:", "Create Custom Gate", -1, null,
						null, "");
				if (t == null) {
					return;
				}

				if (t.length() == 0) {
					JOptionPane.showMessageDialog(App.frame, "No name inputted, aborting.");
					return;
				}
				
				inputs.finishEditing();
				outputs.finishEditing();
				create.accept(inputs.getDataAsStringList(), outputs.getDataAsStringList(), t);
			}
		});

		frame.add(top);
		frame.add(confirm, BorderLayout.SOUTH);
		frame.setLocationRelativeTo(null);
		frame.setTitle("Create Custom Gate");
		frame.pack();
		frame.setVisible(true);
	}
}
