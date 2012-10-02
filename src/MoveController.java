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
import net.java.games.input.Component;
import io.thp.psmove.*;

import io.thp.psmove.Frame;

public class MoveController extends Device {

	private static ArrayList<MoveController> MoveControllers = null;

	private static Button Buttons;
	private static Button[] allButtons;
	private static float[] sensorData;
	private static String[] sensorDataNames;
	public static Button[] filteredButtons;

	public static ArrayList<MoveController> getMoves() {
		if (MoveControllers != null) {
			return MoveControllers;
		}

		// Arraylist that holds the number of MoveControllers found
		MoveControllers = new ArrayList<MoveController>();

		// retrieve num of move connected
		int numOfMoves = getNumConnected();
		PSMove[] ms = new PSMove[numOfMoves];
		if ((ms == null) || (ms.length == 0))
			return MoveControllers;
		// Populated array with the found controllers
		if (numOfMoves >= 1)
			for (int i = 0; i < numOfMoves; i++) {
				ms[i] = new PSMove(i);
			}
		for (PSMove m : ms)
			// Copy the array to the arraylist
			MoveControllers.add(new MoveController(m));
		return MoveControllers;
	}

	public static int getNumConnected() {
		return psmoveapi.count_connected();
	}

	// return a move controller for MoveControllers arraylist
	public static MoveController getMove(int mIndex) {
		getMoves();
		return MoveControllers.get(mIndex);
	}

	private PSMove move;
	private float[] minValues = null;
	private float[] maxValues = null;
	private float[] mapValues = null;
	private String[] plugs = null;
	private boolean[] compsSelected = null;
	private int deviceIndex;
	private int rumbleValue, red, green, blue;
	private float gx, gy, gz;
	private float ax, ay, az;

	/*
	 * Creates a new instance of MoveController
	 */
	public MoveController(PSMove move) {
		super();
		this.move = move;
		@SuppressWarnings("static-access")
		// store all move (including ps3) buttons in array Btns
		Button[] Btns = Buttons.values();
		allButtons = new Button[Btns.length];
		// Create an array with move-specific buttons
		allButtons = findMoveBtns(Btns);
		// trim down the array
		filteredButtons = filterMoveButtons(allButtons);
		sensorData = new float[19];
		sensorData = initSensorData(sensorData.length);
		sensorDataNames = new String[19];
		sensorDataNames = initSensorDataNames(sensorDataNames.length);
		this.minValues = new float[getNumComponents()];
		this.minValues = initMinValues(getNumComponents());
		// On startup the column min-values in ComponentTable should reflect the
		// proper values
		this.maxValues = new float[getNumComponents()];
		this.maxValues = initMaxValues(getNumComponents());
		// On startup the column max-values in ComponentTable should reflect the
		// proper values
		this.mapValues = new float[getNumComponents()];
		this.plugs = new String[getNumComponents()];
		// this.plugs = initPlugNames(getNumComponents());
		this.compsSelected = new boolean[getNumComponents()];
		this.deviceIndex = 0;
		this.rumbleValue = 0;
		this.red = 0;
		this.green = 0;
		this.blue = 0;
		this.gx = 0;
		this.gy = 0;
		this.gz = 0;
		this.ax = 0;
		this.ay = 0;
		this.ay= 0;
		
	}

	public boolean getIsSelected() {
		return isSelected;
	}

	// Method called from controllerTable
	public void setIsSelected(Boolean value) {
		isSelected = value;
	}

	public float[] initMaxValues(int numOfComponents) {
		for (int i = 0; i < numOfComponents; i++) {
			if (i < 9) {
				if (filteredButtons[i].swigValue() == 1048576)
					maxValues[i] = 255;
				else
					maxValues[i] = 1;
			}
			if (i > 8 && i < 18) {
				maxValues[i] = 900;
			} if (i > 17 && i < 24) {
				maxValues[i] = 10;
			} else if (i > 23) {
				maxValues[i] = 255;
			}
		}
		return maxValues;
	}

	public float[] initMinValues(int numOfComponents) {
		for (int i = 0; i < numOfComponents; i++) {
			if (i < 9)
				minValues[i] = 0;
			if (i > 8 && i < 18)
				minValues[i] = -900;
			if (i > 17 && i < 24)
				minValues[i] = 0;
			else if (i > 23) {
				minValues[i] = 0;
			}
		}
		return minValues;
	}
	
	
	
	/*
	 if (row > 8 && row < 18) {
			valLow = -900;
			valHigh = 900;
			float theVal = getSensorData(row - filteredButtons.length);
			this.mapValues[row] = minVal
					+ ((theVal - valLow) * (maxVal - minVal))
					/ (valHigh - valLow);
		}
		if (row > 17 && row < 24) {
				valLow = 0;
				valHigh = 10;
				float theVal = getSensorData(row - filteredButtons.length);
		
		} else if (row > 23) {
	
	 
	 */
	
	
	
	
	
	
	
	

	/*
	 * Only used to reset names when New is pressed
	 */
	public void initPlugNames(int numOfComponents) {
		for (int i = 0; i < this.plugs.length; i++) {
			// if (this.plugs[i] == null) {
			if (i < 9) {
				// remove all possible whitespace
				String compName = filteredButtons[i].toString().replaceAll(
						"\\s", "");
				compName = compName.trim();
				this.plugs[i] = "/device/" + this.deviceIndex + "/component/"
						+ compName;
			} else if (i > 24) {
				this.plugs[i] = "/device/" + this.deviceIndex
						+ "/component/Led";

			} else {
				String compName = sensorDataNames[i - filteredButtons.length];
				this.plugs[i] = "/device/" + this.deviceIndex + "/component/"
						+ compName;
			}
			// }
		}

	}

	public void getOrientation() {
		float [] ax = { 0.f };
        float [] ay = { 0.f };
        float [] az = { 0.f };

        float [] gx = { 0.f };
        float [] gy = { 0.f };
        float [] gz = { 0.f };
		move.get_accelerometer_frame(Frame.Frame_SecondHalf, ax, ay, az);
		move.get_gyroscope_frame(Frame.Frame_SecondHalf, gx, gy, gz);
        
		/*
		System.out.format("ax: %.2f ay: %.2f az: %.2f ",
                ax[0], ay[0], az[0]);
        System.out.format("gx: %.2f gy: %.2f gz: %.2f\n",
                gx[0], gy[0], gz[0]);
        */        
		this.gx = gx[0];
		this.gy = gy[0];
		this.gz = gz[0];
		this.ax = ax[0];
		this.ay = ay[0];
		this.az = az[0];
        
	}

	private Button[] findMoveBtns(Button[] Btns2) {
		/*
		 * Compare the button array from the move controllers, then copy only
		 * the buttons that we want to use
		 */
		for (int i = 0; i < Btns2.length; i++) {
			int theBtnNumber = Btns2[i].swigValue();
			switch (theBtnNumber) {
			case 16:
			case 32:
			case 64:
			case 128:
			case 256:
			case 2048:
			case 65536:
			case 524288:
			case 1048576:
				allButtons[i] = Btns2[i];
				break;
			}
		}
		return allButtons;
	} // end of findMoveBtns

	private Button[] filterMoveButtons(Button[] allButtons2) {
		/*
		 * Trim down the empty places in allButtons array
		 */
		if (allButtons2 == null) {
			// Double check if the array we pass is not null
			return null;
		}
		/*
		 * Copy the array to a temp arraylist, then only copy non-zero elements
		 * back into the new array
		 */
		ArrayList<Button> filteredButtons = new ArrayList<Button>();
		for (int i = 0, length = allButtons2.length; i < length; i++) {
			if (allButtons2[i] != null) {
				filteredButtons.add(allButtons2[i]);
			}
		}
		return (Button[]) filteredButtons.toArray(new Button[filteredButtons
				.size()]);
	} // end of filterMoveButtons()

	private float[] initSensorData(int num) {
		for (int i = 0; i < num; i++) {
			switch (i) {
			case 0:
				sensorData[i] = move.getAx();
				break;
			case 1:
				sensorData[i] = move.getAy();
				break;
			case 2:
				sensorData[i] = move.getAz();
				break;		
			case 3:
				sensorData[i] = move.getGx();
				break;
			case 4:
				sensorData[i] = move.getGy();
				break;
			case 5:
				sensorData[i] = move.getGz();
				break;		
			case 6:
				sensorData[i] = move.getMx();
				break;
			case 7:
				sensorData[i] = move.getMy();
				break;
			case 8:
				sensorData[i] = move.getMz();
				break;
			case 9:
				sensorData[i] = this.ax;
				break;
			case 10:
				sensorData[i] = this.ay;
				break;
			case 11:
				sensorData[i] = this.az;
				break;
			case 12:
				sensorData[i] = this.gx;
				break;
			case 13:
				sensorData[i] = this.gy;
				break;
			case 14:
				sensorData[i] = this.gz;
				break;
			case 15:
				sensorData[i] = this.rumbleValue;
				break;
			case 16:
				sensorData[i] = this.red;
				break;
			case 17:
				sensorData[i] = this.green;
				break;
			case 18:
				sensorData[i] = this.blue;
				break;
			}
		}
		return sensorData;
	}

	private float getSensorData(int dataNum) {
		switch (dataNum) {
		case 0:
			return move.getAx();
		case 1:
			return move.getAy();
		case 2:
			return move.getAz();
		case 3:
			return move.getGx();
		case 4:
			return move.getGy();
		case 5:
			return move.getGz();
		case 6:
			return move.getMx();
		case 7:
			return move.getMy();
		case 8:
			return move.getMz();
		case 9:
			return this.ax;
		case 10:
			return this.ay;
		case 11:
			return this.az;
		case 12:
			return this.gx;
		case 13:
			return this.gy;
		case 14:
			return this.gz;
		case 15:
			return this.rumbleValue;
		case 16:
			return this.red;
		case 17:
			return this.green;
		case 18:
			return this.blue;
		}
		return 0;
	}

	private String[] initSensorDataNames(int num) {
		for (int i = 0; i < num; i++) {
			switch (i) {
			case 0:
				sensorDataNames[i] = "RawAccX";
				break;
			case 1:
				sensorDataNames[i] = "RawAccY";
				break;
			case 2:
				sensorDataNames[i] = "RawAccZ";
				break;
			case 3:
				sensorDataNames[i] = "RawGyroX";
				break;
			case 4:
				sensorDataNames[i] = "RawGyroY";
				break;
			case 5:
				sensorDataNames[i] = "RawGyroZ";
				break;
			case 6:
				sensorDataNames[i] = "MagnoX";
				break;
			case 7:
				sensorDataNames[i] = "MagnoY";
				break;
			case 8:
				sensorDataNames[i] = "MagnoZ";
				break;
			case 9:
				sensorDataNames[i] = "AccX";
				break;
			case 10:
				sensorDataNames[i] = "AccY";
				break;
			case 11:
				sensorDataNames[i] = "AccZ";
				break;
			case 12:
				sensorDataNames[i] = "GyroX";
				break;
			case 13:
				sensorDataNames[i] = "GyroY";
				break;
			case 14:
				sensorDataNames[i] = "GyroZ";
				break;
			case 15:
				sensorDataNames[i] = "Rumble";
				break;
			case 16:
				sensorDataNames[i] = "LedRed";
				break;
			case 17:
				sensorDataNames[i] = "LedGreen";
				break;
			case 18:
				sensorDataNames[i] = "LedBlue";
				break;
			}
		}
		return sensorDataNames;
	}

	public void poll() {
		move.poll();
		getOrientation();
		if (compsSelected[25])
			move.set_leds(this.red, this.green, this.blue);
		else
			move.set_leds(0, 0, 0);

		if (compsSelected[24])
			move.set_rumble(this.rumbleValue);
		else
			move.set_rumble(0);
		move.update_leds();
		// Update the array of messages, storing the latest component value in a
		// message
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

	public String getName() {
		return "PSMove";
	}

	public String getType() {
		return "Motion Controller";
	}

	public int getNumComponents() {
		int allComponents = filteredButtons.length + sensorData.length;
		return allComponents;
	}

	Button[] getFilteredButtons() {
		return filteredButtons;
	}

	float[] getSensorData() {
		return sensorData;
	}

	String[] getSensorDataNames() {
		return sensorDataNames;
	}

	public int getCurrentPressed() {
		return move.get_buttons();
	}

	public int getTrigger() {
		return move.get_trigger();
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

	/*
	 * By default the osc plugname (addresspattern) follows
	 * /devicename/i/componentname
	 */
	public String[] getPlugNames() {
		return this.plugs;
	}

	public String getPlugName(int i) {
		return this.plugs[i];
	}

	/*
	 * The data from the controller components are mapped according to
	 * boundaries specified by the user (minValue & maxValue). The mapped value
	 * is send over OSC
	 */
	public void setMapValue(int row, float minVal, float maxVal) {
		int valLow;
		int valHigh;
		if (row < 9) {
			switch (filteredButtons[row].swigValue()) {
			case 1048576:
				valLow = 0;
				valHigh = 255;
				this.mapValues[row] = minVal
						+ ((move.get_trigger() - valLow) * (maxVal - minVal))
						/ (valHigh - valLow);
				break;
			case 16:
			case 32:
			case 64:
			case 128:
			case 256:
			case 2048:
			case 65536:
			case 524288:
				valLow = 0;
				valHigh = 1;
				if (getCurrentPressed() == filteredButtons[row].swigValue()) {
					this.mapValues[row] = minVal
							+ ((1 - valLow) * (maxVal - minVal))
							/ (valHigh - valLow);
				} else {
					this.mapValues[row] = minVal
							+ ((0 - valLow) * (maxVal - minVal))
							/ (valHigh - valLow);
				}
				break;
			}
		}
		if (row > 8 && row < 18) {
			valLow = -900;
			valHigh = 900;
			float theVal = getSensorData(row - filteredButtons.length);
			this.mapValues[row] = minVal
					+ ((theVal - valLow) * (maxVal - minVal))
					/ (valHigh - valLow);
		}
		if (row > 17 && row < 24) {
				valLow = 0;
				valHigh = 10;
				float theVal = getSensorData(row - filteredButtons.length);
				/*
				this.mapValues[row] = minVal
						+ ((theVal - valLow) * (maxVal - minVal))
						/ (valHigh - valLow);
				*/
				this.mapValues[row] = Math.round(theVal*100.0);	
		
		} else if (row > 23) {
			valLow = 0;
			valHigh = 255;
			float theVal = getSensorData(row - filteredButtons.length);
			this.mapValues[row] = minVal
					+ ((theVal - valLow) * (maxVal - minVal))
					/ (valHigh - valLow);
		}
	}

	public float getMapValue(int row) {
		return this.mapValues[row];
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

	public void setRumbleValue(int rumbleVal) {
		this.rumbleValue = rumbleVal;
	}

	public void setLed(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	Component[] getComponents() {
		/* JInputdevice function, not implemented */
		return null;
	}

}
