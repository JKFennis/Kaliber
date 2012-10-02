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

import io.thp.psmove.Button;
import java.util.ArrayList;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class JInputDevice extends Device {
	private static ArrayList<JInputDevice> jInputDevices = null;

	/*
	 * Get all the avaliable JInputDevices
	 */
	public static ArrayList<JInputDevice> getDevices() {
		if (jInputDevices != null) {
			return jInputDevices;
		}
		jInputDevices = new ArrayList<JInputDevice>();

		ControllerEnvironment ce = ControllerEnvironment
				.getDefaultEnvironment();
		Controller[] cs = ce.getControllers();

		if ((cs == null) || (cs.length == 0))
			return jInputDevices;

		for (Controller c : cs)
			if (!c.getName().equals("Motion Controller"))
				jInputDevices.add(new JInputDevice(c));

		return jInputDevices;
	}

	public static int getNumDevices() {
		getDevices();
		return jInputDevices.size();
	}

	public static JInputDevice getDevice(int index) {
		getDevices();
		return jInputDevices.get(index);
	}

	private Controller controller;
	private Component[] components;
	private float[] minValues = null;
	private float[] maxValues = null;
	private float[] mapValues = null;
	private String[] plugs = null;
	private boolean[] compsSelected = null;
	private int deviceIndex;

	/*
	 * Creates a new instance of JInputDevice
	 */
	public JInputDevice(Controller controller) {
		super();
		this.controller = controller;
		this.components = controller.getComponents();
		this.minValues = new float[getNumComponents()];
		this.minValues = initMinValues(getNumComponents());
		this.maxValues = new float[getNumComponents()];
		this.maxValues = initMaxValues(getNumComponents());
		this.plugs = new String[getNumComponents()];
		// this.plugs = initPlugNames(getNumComponents());
		this.compsSelected = new boolean[getNumComponents()];
		this.mapValues = new float[getNumComponents()];
		this.deviceIndex = 0;
	}

	public boolean getIsSelected() {
		return isSelected;
	}

	// Method called from controllerTable
	public void setIsSelected(Boolean value) {
		isSelected = value;
	}

	public float[] initMaxValues(int numOfComponents) {
		for (int i = 0; i < numOfComponents; i++)
			maxValues[i] = 1;
		return maxValues;
	}

	public float[] initMinValues(int numOfComponents) {
		for (int i = 0; i < numOfComponents; i++)
			if (components[i].isAnalog())
				minValues[i] = -1;
			else
				minValues[i] = 0;
		return minValues;
	}

	/*
	 * Only used to reset names when New is pressed
	 */
	public void initPlugNames(int numOfComponents) {
		for (int i = 0; i < this.plugs.length; i++) {
			//if (this.plugs[i] == null) {
				// remove all possible whitespace
				String compName = components[i].getName().replaceAll("\\s", "");
				compName = compName.trim();
				this.plugs[i] = "/device/" + this.deviceIndex + "/component/"
						+ compName;
			//}
		}
	}
	
	

	public String getName() {
		return controller.getName();
	}

	public String getType() {
		return controller.getType().toString();
	}

	public int getNumComponents() {
		return controller.getComponents().length;
	}

	public Component[] getComponents() {
		return this.components;
	}

	public void poll() {
		controller.poll();
		sendOSCMessage();

	}

	@SuppressWarnings("static-access")
	public void sendOSCMessage() {
		for (int i = 0; i < getNumComponents(); i++) {
			if (getIsSelected()) {
				if (compsSelected[i]) {
					float oldValue = getMapValue(i);
					setMapValue(i, getMinValue(i), getMaxValue(i));
					if (!Kaliber.sendingMode) {
						if (mapValues[i] != oldValue)
							osc.sendMessage(osc.createMessage(new String(
									plugs[i]), mapValues[i]));
					} else {
						osc.sendMessage(osc.createMessage(new String(plugs[i]),
								mapValues[i]));
					}
				}
			}
		}
	}

	public void setMinValue(int row, float minVal) {
		this.minValues[row] = minVal;
	}

	public float[] getMinValues() {
		return this.minValues;
	}

	public float getMinValue(int i) {
		return this.minValues[i];
	}

	public void setMaxValue(int row, float maxVal) {
		this.maxValues[row] = maxVal;
	}

	public float[] getMaxValues() {
		return this.maxValues;
	}

	public float getMaxValue(int i) {
		return this.maxValues[i];
	}

	void setcompSelected(int row, boolean select) {
		this.compsSelected[row] = select;
	}

	public boolean[] getSelectedComponents() {
		return this.compsSelected;
	}

	public boolean getSelectedComponent(int i) {
		return this.compsSelected[i];
	}

	public void setPlugName(int row, String plug) {
		this.plugs[row] = plug;
	}

	public String[] getPlugNames() {
		/*
		 * for (int i = 0; i < this.plugs.length; i++) { if (this.plugs[i] ==
		 * null) { // remove all possible whitespace String compName =
		 * components[i].getName().replaceAll("\\s", ""); compName =
		 * compName.trim(); this.plugs[i] = "/device/" + this.deviceIndex +
		 * "/component/" + compName; } }
		 */
		return this.plugs;
	}

	public String getPlugName(int i) {
		// remove all possible whitespace
		/*
		 * String compName = components[i].getName().replaceAll("\\s", "");
		 * compName = compName.trim(); this.plugs[i] = "/device/" +
		 * this.deviceIndex + "/component/" + compName;
		 */
		return this.plugs[i];
	}

	@Override
	/*
	 * The data from the controller components are mapped according to
	 * boundaries specified by the user (minValue & maxValue). The mapped value
	 * is send over OSC
	 */
	public void setMapValue(int row, float minVal, float maxVal) {
		int valLow;
		int valHigh;

		if (components[row].isAnalog()) {
			valLow = -1;
			valHigh = 1;
			this.mapValues[row] = minVal
					+ ((components[row].getPollData() - valLow) * (maxVal - minVal))
					/ (valHigh - valLow);
		} else {
			valLow = 0;
			valHigh = 1;
			this.mapValues[row] = minVal
					+ ((components[row].getPollData() - valLow) * (maxVal - minVal))
					/ (valHigh - valLow);
		}
	}

	float getMapValue(int row) {
		return mapValues[row];
	}

	public float[] getMapValues() {
		return this.mapValues;
	}

	public void setDeviceIndex(int row) {
		this.deviceIndex = row;
	}

	public int getDeviceIndex() {
		return this.deviceIndex;
	}

	@Override
	int getCurrentPressed() {
		/* movecontroller function, not implemented */
		return 0;
	}

	@Override
	int getTrigger() {
		/* movecontroller function, not implemented */
		return 0;
	}

	@Override
	Button[] getFilteredButtons() {
		/* movecontroller function, not implemented */
		return null;
	}

	@Override
	float[] getSensorData() {
		/* movecontroller function, not implemented */
		return null;
	}

	@Override
	String[] getSensorDataNames() {
		/* movecontroller function, not implemented */
		return null;
	}

	@Override
	void setRumbleValue(int rumbleValue) {
		/* movecontroller function, not implemented */
	}

	@Override
	void setLed(int red, int green, int blue) {
		/* movecontroller function, not implemented */
	}

} // End of class
