/*
The MIT License (MIT)

Copyright (c) 2012 Adam Henriksson, Jules Fennis

Permission is hereby granted, free of charge, to any person obtaining a copy of this software
and associated documentation files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions: The above copyright
notice and this permission notice shall be included in all copies or substantial portions of
the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import javax.swing.table.AbstractTableModel;
import net.java.games.input.Component;

public class ComponentTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 3262338900905263942L;
	// The first column with checkboxes
	private static final int CHECK_COL = 0;
	private Device controller = null;
	private Component[] components = null;
	private float[] minValues = null;
	private float[] maxValues = null;
	private String[] plugs = null;
	private boolean[] compsSelected = null;

	public ComponentTableModel() {
	}

	/*
	 * Retrieve all the values from the currently selected JInputDevice
	 */
	public ComponentTableModel(Device controller) {
		this.controller = controller;
		this.components = controller.getComponents();
		this.minValues = controller.getMinValues();
		this.maxValues = controller.getMaxValues();
		this.plugs = controller.getPlugNames();
		this.compsSelected = controller.getSelectedComponents();
	}

	private String columnNames[] = { "I/0", "Plug", "Type", "Min",
			"Max", "Data" };

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return controller != null ? controller.getNumComponents() : 0;
	}

	/*
	 * Iterates over the table and calls a set of get() functions in
	 * MoveController to receive values
	 */
	public Object getValueAt(int row, int col) {
		if ((components == null) || (components.length <= 0))
			return null;
		Component comp = components[row];
		float minVal = minValues[row];
		float maxVal = maxValues[row];
		controller.setMapValue(row, minVal, maxVal);
		String plug = plugs[row];
		boolean select = compsSelected[row];

		switch (col) {
		case 0:
			return select;
		case 1:
			return plug;
		case 2:
			if(comp.isAnalog())
				return "Analog";
			else
				return "Digital";
			//return comp.getName();
		case 3:
			return minVal;
		case 4:
			return maxVal;
		case 5:
			return controller.getMapValue(row);
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
		} else {
			return true;
		}
	}

	/*
	 * Calls a set of set() functions in JInputDevice to update values when
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
			if (compSelected) {
				// Component is deselected, it's value should not be send over
				controller.setcompSelected(row, false);
			} else {
				// Component is selected, it's value should be send over
				controller.setcompSelected(row, true);
			}
			super.setValueAt(value, row, col);
			break;
		case 1:
			newPlug = value.toString();
			plugs[row] = newPlug;
			controller.setPlugName(row, newPlug);
			break;
		case 3:
			minVal = Float.parseFloat(value.toString());
			minValues[row] = minVal;
			controller.setMinValue(row, minVal);
			break;
		case 4:
			maxVal = Float.parseFloat(value.toString());
			maxValues[row] = maxVal;
			controller.setMaxValue(row, maxVal);
			break;

		}
		// Update the table
		fireTableCellUpdated(row, col);
	} // end of setValueAt()

}
