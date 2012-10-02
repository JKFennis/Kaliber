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
import de.sciss.net.OSCBundle;
import net.java.games.input.Component;

/*
 * superclass of subclasses MoveController and JInputDevice
 */
abstract public class Device {
	OSCmachine osc = OSCmachine.getInstance();
	// Arraylist to store the available devices
	public static ArrayList<Device> Devices = new ArrayList<Device>();
	// Arraylist to store OSCBundles of the devices
	public static ArrayList<OSCBundle> theBundles = new ArrayList<OSCBundle>();
	// Toggled when devices is (de)selected in controllerTable
	public boolean isSelected;

	// Populate the arraylist
	public static ArrayList<Device> getAllDevices() {
		for (JInputDevice j : JInputDevice.getDevices())
			Devices.add(j);

		for (MoveController m : MoveController.getMoves())
			Devices.add(m);
		return Devices;
	}

	public static int getNumDevices() {
		return Devices.size();
	}

	public static Device getDevice(int index) {
		return Devices.get(index);
	}

	/*
	 * Methods abstracted from MoveController and JInputDevice
	 */
	abstract void setDeviceIndex(int row);

	abstract int getDeviceIndex();

	abstract boolean getIsSelected();

	abstract void setIsSelected(Boolean value);

	abstract void poll();

	abstract void setMinValue(int row, float minVal);

	abstract void setMaxValue(int row, float maxVal);

	abstract void setMapValue(int row, float minVal, float maxVal);

	abstract void setPlugName(int row, String plug);

	abstract void setcompSelected(int row, boolean select);

	abstract boolean[] getSelectedComponents();

	abstract boolean getSelectedComponent(int i);

	abstract float[] getMinValues();

	abstract float getMinValue(int i);

	abstract float[] getMaxValues();

	abstract float getMaxValue(int i);

	abstract float[] getMapValues();

	abstract float getMapValue(int row);

	abstract String[] getPlugNames();

	abstract String getPlugName(int i);

	abstract String getName();

	abstract String getType();

	abstract int getNumComponents();

	abstract Component[] getComponents();

	abstract int getCurrentPressed();

	abstract int getTrigger();

	abstract Button[] getFilteredButtons();

	abstract float[] getSensorData();

	abstract String[] getSensorDataNames();
	
	abstract void setRumbleValue(int rumbleValue);
	
	abstract void setLed(int red, int green, int blue);

	abstract void initPlugNames(int numComponents);
	
	abstract float[] initMinValues(int numComponents);
	
	abstract float[] initMaxValues(int numComponents);

}
