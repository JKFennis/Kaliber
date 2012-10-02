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

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

public class ControllerTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -1261946759231065024L;
	private static final int CHECK_COL = 0;
	ArrayList<Device> allDevices;

	public ControllerTableModel() {
		allDevices = Device.getAllDevices();
	}

	private String columnNames[] = { "I/0", "Controller Name", "Type",
			"#Components"};

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return allDevices.size();
	}

	public final Object[] longValues = { Boolean.TRUE, "Controller Name",
			"Type", new Integer(20), };

	public Object getValueAt(int row, int col) {
		Device jid = allDevices.get(row);
		jid.setDeviceIndex(row + 1);
		jid.initPlugNames(jid.getNumComponents());
		switch (col) {
		case 0:
			return jid.getIsSelected();
		case 1:
			return jid.getName();
		case 2:
			return jid.getType();
		case 3:
			return jid.getNumComponents();
		}
		return null;
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public void setValueAt(Object aValue, int row, int col) {
		Device jid = allDevices.get(row);
		if (col == CHECK_COL) {
			for (int r = 0; r < getRowCount(); r++) {
				super.setValueAt(false, r, CHECK_COL);
			}
		}
		if (jid.getIsSelected()) {
			jid.setIsSelected(false);
		} else {
			jid.setIsSelected(true);
		}
		super.setValueAt(aValue, row, col);
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
		if (col == 0) {
			return true;
		} else {
			return false;
		}
	}
}
