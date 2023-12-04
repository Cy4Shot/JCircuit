package jcircuit.el;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class EditableList extends JTable {
	private static final long serialVersionUID = 1718225737306167870L;
	private DefaultCellEditor cellEditor;
	private final DefaultTableModel model;
	public JTextField textField;

	public EditableList() {
		super(1, 1);
		this.model = (DefaultTableModel) getModel();
		init();
	}

	private void init() {
		initTable();
		initEditorComponent();
		initSelectionListener();
		initKeyListeners();
	}

	private void initTable() {
		setTableHeader(null);
	}

	private void initEditorComponent() {
		TableColumn column = getColumnModel().getColumn(0);
		textField = new JTextField();
		this.cellEditor = new DefaultCellEditor(textField);
		cellEditor.setClickCountToStart(1);
		column.setCellEditor(cellEditor);
		textField.setBorder(null);
		textField.setForeground(new Color(0, 100, 250));
	}

	private void initSelectionListener() {
		getSelectionModel().addListSelectionListener(e -> {
			int selectedRow = getSelectedRow();
			if (selectedRow == -1) {
				return;
			}
			startEditingAtRow(selectedRow);
		});
	}

	private void initKeyListeners() {
		// enter key will insert a new row at next row index
		Action insertRowAfterCurrentRowAction = createEnterKeyAction();
		bindKeyAction(insertRowAfterCurrentRowAction, KeyEvent.VK_ENTER, 0, this, textField);
		// enter key will insert a new row a prev row index
		Action insertRowBeforeCurrentRowAction = createAltEnterKeyAction();
		bindKeyAction(insertRowBeforeCurrentRowAction, KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK, this, textField);
		// del and backspace keys will remove the empty rows
		textField.addKeyListener(createDelKeyListener());
	}

	private void bindKeyAction(Action action, int keyCode, int acceleratorKey, JComponent... components) {
		for (JComponent component : components) {
			KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, acceleratorKey);
			component.registerKeyboardAction(action, keyStroke, JComponent.WHEN_FOCUSED);
		}
	}

	private Action createEnterKeyAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = -2868571187560143562L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final int selectedRow = getSelectedRow();
				if (selectedRow == -1) {
					return;
				}
				insertNewRow(selectedRow + 1);
				selectRow(selectedRow + 1);
			}
		};
	}

	private Action createAltEnterKeyAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = 2607035917414499200L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRow = getSelectedRow();
				if (selectedRow == -1) {
					return;
				}
				cellEditor.stopCellEditing();
				removeEditor();
				insertNewRow(selectedRow);
			}
		};
	}

	private KeyListener createDelKeyListener() {
		return new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int selectedRow = getSelectedRow();
				if (selectedRow == -1 || getRowCount() == 1) {
					return;
				}
				boolean emptyField = textField.getText().trim().length() == 0;
				boolean del = e.getKeyCode() == KeyEvent.VK_DELETE;
				boolean bsp = e.getKeyCode() == KeyEvent.VK_BACK_SPACE;
				if ((del || bsp) && emptyField) {
					removeRow(selectedRow);
					int newSel = del ? selectedRow : selectedRow - 1;
					if (newSel == -1) {
						newSel = 0;
						del = true;
					}
					if (newSel > getRowCount() - 1) {
						newSel = getRowCount() - 1;
						del = false;
					}
					selectRow(newSel);
					e.consume();
					if (del) {
						textField.setCaretPosition(0);
					} else {
						textField.setCaretPosition(textField.getDocument().getLength());
					}
				}
			}
		};
	}

	private void selectRow(int selectedRow) {
		setRowSelectionInterval(selectedRow, selectedRow);
	}

	private void startEditingAtRow(int row) {
		if (isCellEditable(row, 0)) {
			editCellAt(row, 0);
			changeSelection(row, 0, false, false);
			textField.requestFocusInWindow();
		}
	}

	private void insertNewRow(int newRowIndex) {
		model.insertRow(newRowIndex, new String[] { "" });
	}

	private void removeRow(int selectedRow) {
		cellEditor.stopCellEditing();
		removeEditor();
		model.removeRow(selectedRow);
	}
	
	public void finishEditing() {
		cellEditor.stopCellEditing();
	}

	public Object[][] getTableData() {
		int nRow = model.getRowCount(), nCol = model.getColumnCount();
		Object[][] tableData = new Object[nRow][nCol];
		for (int i = 0; i < nRow; i++)
			for (int j = 0; j < nCol; j++)
				tableData[i][j] = model.getValueAt(i, j);
		return tableData;
	}

	public List<String> getDataAsStringList() {
		return Arrays.stream(getTableData()).flatMap(Arrays::stream).filter(a -> a != null).map(Object::toString)
				.collect(Collectors.toList());
	}
}