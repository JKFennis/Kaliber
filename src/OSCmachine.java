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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import de.sciss.net.OSCClient;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

class OSCmachine {
	// singleton pattern
	private static OSCmachine object;

	public static OSCClient c;
	public static OSCServer s;
	private String target = "127.0.0.1";
	private int outport = 12000;
	private int inport = 8000;

	// private for singleton
	public void changeOutPort(int newOut) {
		if (newOut == 0)
			outport = 12000;
		else
			outport = newOut;
		try {
			c.stop();
			c.setTarget(new InetSocketAddress(target, outport));
			c.start();
		} catch (IOException e) {
			Kaliber.printConsole("Failed to set up listener " + e.toString(), false);
		}
	}

	private OSCmachine() {
		try {
			// Create UDP client with any free port number
			c = OSCClient.newUsing(OSCClient.UDP);
			// Talk to scsynth on the same machine
			c.setTarget(new InetSocketAddress(target, outport));
			// Open channel and (in the case of TCP) connect, then start
			// listening for replies
			c.start();
			s = OSCServer.newUsing(OSCServer.UDP, inport, true);
			s.start();
			s.addOSCListener(new OSCListener() {
				public void messageReceived(OSCMessage msg,
						SocketAddress sender, long time) {
					int moveNum = 0;
					try {
						moveNum = Character.getNumericValue(msg.getName()
								.charAt(8));
					} catch (StringIndexOutOfBoundsException ex) {

					}
					Device d = Device.Devices.get(moveNum - 1);
					if (d instanceof MoveController) {

						if (msg.getName().contains("/Rumble")) {
							if (msg.getArg(0).equals("OFF")) {
								d.setRumbleValue(0);
							}else if (msg.getArg(0).equals("ON")) {
									d.setRumbleValue(255);
							} else {
								int rumble = (Integer) msg.getArg(0);
								d.setRumbleValue(rumble);
							}
						}
						if (msg.getName().contains("/Led")) {
							if (msg.getArg(0).equals("OFF")) {
								d.setLed(0, 0, 0);
							}else if (msg.getArg(0).equals("ON")) {
								d.setLed(255, 255, 255);
							} else {
								int red = (Integer) msg.getArg(0);
								int green = (Integer) msg.getArg(1);
								int blue = (Integer) msg.getArg(2);
								d.setLed(red, green, blue);
							}
						}
					}
				}
			});
		} catch (Exception e) {
			Kaliber.printConsole("Failed to receive message " + e.toString(), false);
			return;
		}
	}

	public static OSCmachine getInstance() {
		if (object == null) {
			// Create the object for the first and last time
			object = new OSCmachine();
		}
		return object;
	}

	// Mehod called from Move and JInputDevice
	public static OSCMessage createMessage(String addrPattern, float data) {
		OSCMessage msg = null;
		try {
			Object argsout[] = new Object[1];
			argsout[0] = new Float(data);
			msg = new OSCMessage(addrPattern, argsout);
			return msg;
		} catch (Exception e) {
			Kaliber.printConsole("Failed to set up OSC " + e.toString(), false);
		}
		return msg;
	}

	public static void sendMessage(OSCMessage m) {
		if (Kaliber.startSending) {
			try {
				Kaliber.printConsole("Sending " + m.getName()+" "+m.getArg(0), true);
				c.send(m);
			} catch (Exception e) {
				Kaliber.printConsole("sendMessage error " + e.toString(), true);
			}
		}
	}

	public int getInPort() {
		return inport;
	}

	public void setOutPort(String newOutPort) {
		outport = Integer.parseInt(newOutPort.trim());
		changeOutPort(outport);
	}

	public int getOutPort() {
		return outport;
	}

}
