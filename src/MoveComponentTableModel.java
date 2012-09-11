import io.thp.psmove.Button;
import javax.swing.table.AbstractTableModel;

public class MoveComponentTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 7239385446255678700L;
	// The first column with checkboxes
	private static final int CHECK_COL = 0;
	private Device move = null;
	private Button[] moveButtons = null;
	private float[] minValues = null;
	private float[] maxValues = null;
	private String[] plugs = null;
	private boolean[] compsSelected = null;
	private String[] sensorDataNames = null;

	public MoveComponentTableModel() {
	}

	/*
	 * Retrieve all the values from the currently selected MoveController
	 */
	public MoveComponentTableModel(Device move) {
		this.move = move;
		this.moveButtons = move.getFilteredButtons();
		this.minValues = move.getMinValues();
		this.maxValues = move.getMaxValues();
		this.plugs = move.getPlugNames();
		this.compsSelected = move.getSelectedComponents();
		this.sensorDataNames = move.getSensorDataNames();
	}

	private String columnNames[] = { "I/0", "Plug", "Type", "Min", "Max",
			"Data" };

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return move != null ? move.getNumComponents() : 0;

	}

	@SuppressWarnings("static-access")
	/*
	 * Iterates over the table and calls a set of get() functions in
	 * MoveController to receive values
	 */
	public Object getValueAt(int row, int col) {
		if ((moveButtons == null) || (moveButtons.length <= 0))
			return null;
		float minVal = minValues[row];
		float maxVal = maxValues[row];
		String plug = plugs[row];
		move.setMapValue(row, minVal, maxVal);
		boolean select = compsSelected[row];
		// Get button from the array corresponding to the current row
		/*
		 * Button btn = null; if (row < 9) { Button btn = moveButtons[row]; } //
		 * Store the buttons swigvalue int swigNum = btn.swigValue();
		 */
		// store the currently pressed button
		int curBtn = move.getCurrentPressed();
		switch (col) {
		case 0:
			return select;
		case 1:
			return plug;
		case 2:
			
			if (row < 8) {
				/*
				Button btn = moveButtons[row];
				int swigNum = btn.swigValue();
				return btn.swigToEnum(swigNum);
				*/
				return "Digital";
			} else {
				//return sensorDataNames[row - moveButtons.length];
				return "Analog";
			}
			
		case 3:
			return minVal;
		case 4:
			return maxVal;
		case 5:
			if (row < 9) {
				// If the currently pressed button is a move button (filter PS3)
				Button btn = moveButtons[row];
				int swigNum = btn.swigValue();
				if (swigNum == curBtn)
					switch (swigNum) {
					case 16:
					case 32:
					case 64:
					case 128:
					case 256:
					case 2048:
					case 65536:
					case 524288:
					case 1048576:
						// Return Pressed
						return move.getMapValue(row);
					}
				else {
					// Return not pressed
					return minVal;
				}
			} else {
				return move.getMapValue(row);
			}
		}
		return null;
	} // End of getValueAt()

	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Class<?> getColumnClass(int col) {
		if (col == CHECK_COL) {
			return getValueAt(0, CHECK_COL).getClass();
		}
		return super.getColumnClass(col);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (col == 2 || col == 5) {
			return false;
		} if ((col == 1 && row > 17) || (col == 3 && row > 17) || (col == 4 && row > 17)) {
			return false;
		} else {
			return true;
		}
	}

	/*
	 * Calls a set of set() functions in MoveController to update values when
	 * changes are made in the table
	 */
	public void setValueAt(Object value, int row, int col) {
		float minVal;
		float maxVal;
		String newPlug;
		boolean compSelected = compsSelected[row];
		switch (col) {
		case 0:
			for (int r = 0; r < getRowCount(); r++) {
				super.setValueAt(false, r, CHECK_COL);
			}
			// Component is deselected, it's value should not be send over
			if (compSelected) {
				 if (row > 18 ) {
					move.setcompSelected(19, false);
					move.setcompSelected(20, false);
					move.setcompSelected(21, false);
				 }
				move.setcompSelected(row, false);
			} else {
				 if (row > 18 ) {
						move.setcompSelected(19, true);
						move.setcompSelected(20, true);
						move.setcompSelected(21, true);
					 }
				// Component is selected, it's value should be send over
				move.setcompSelected(row, true);
			}
			super.setValueAt(value, row, col);
			break;
		case 1:
			newPlug = value.toString();
			plugs[row] = newPlug;
			move.setPlugName(row, newPlug);
			break;
		case 3:
			minVal = Float.parseFloat(value.toString());
			minValues[row] = minVal;
			move.setMinValue(row, minVal);
			break;
		case 4:
			maxVal = Float.parseFloat(value.toString());
			maxValues[row] = maxVal;
			move.setMaxValue(row, maxVal);
			break;
		} // Update the table
		fireTableCellUpdated(row, col);
	} // end of setValueAt()

}