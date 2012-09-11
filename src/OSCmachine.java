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
			OpenMove9.printConsole("Failed to set up listener " + e.toString(),
					false);
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

						try {

							if (msg.getName().contains("/Rumble")) {
								int rumble = 0;
								if (msg.getArg(0).equals("OFF")) {
									d.setRumbleValue(0);
								} else if (msg.getArg(0).equals("ON")) {
									d.setRumbleValue(255);
								} else {
									if (msg.getArg(0).getClass()
											.equals(Float.class)) {
										rumble = (int) Math.round((Float) msg
												.getArg(0));
									} else {
										if (msg.getArg(0).getClass()
												.equals(Integer.class))
											rumble = (Integer) msg.getArg(0);
									}
									d.setRumbleValue(rumble);
								}
								OpenMove9.printConsole("Message reveived "
										+ msg.getName() + " "
										+ msg.getArg(0).toString(), false);
							}
							if (msg.getName().contains("/Led")) {
								if (msg.getArg(0).equals("OFF")) {
									d.setLed(0, 0, 0);
									OpenMove9.printConsole(
											"Message reveived " + msg.getName()
													+ " "
													+ msg.getArg(0).toString(),
											false);
								} else if (msg.getArg(0).equals("ON")) {
									d.setLed(255, 255, 255);
									OpenMove9.printConsole(
											"Message reveived " + msg.getName()
													+ " "
													+ msg.getArg(0).toString(),
											false);
								} else {
									int red = 0;
									int green = 0;
									int blue = 0;
									if (msg.getArg(0).getClass()
											.equals(Float.class))
										red = (int) Math.round((Float) msg
												.getArg(0));
									else if (msg.getArg(0).getClass()
											.equals(Integer.class))
										red = (Integer) msg.getArg(0);

									if (msg.getArg(1).getClass()
											.equals(Float.class))
										green = (int) Math.round((Float) msg
												.getArg(1));
									else if (msg.getArg(1).getClass()
											.equals(Integer.class))
										green = (Integer) msg.getArg(1);

									if (msg.getArg(1).getClass()
											.equals(Float.class))
										blue = (int) Math.round((Float) msg
												.getArg(2));
									else if (msg.getArg(2).getClass()
											.equals(Integer.class))
										blue = (Integer) msg.getArg(2);
									d.setLed(red, green, blue);
									OpenMove9.printConsole(
											"Message reveived " + msg.getName()
													+ " "
													+ msg.getArg(0).toString()
													+ " "
													+ msg.getArg(1).toString()
													+ " "
													+ msg.getArg(2).toString(),
											false);
								}

							}
						} catch (ArrayIndexOutOfBoundsException ex) {
							OpenMove9
									.printConsole(
											"Failed to receive message: wrong set of arguments",
											false);
							return;
						}
					}
				}
			});
		} catch (Exception e) {
			OpenMove9.printConsole("Failed to receive message " + e.toString(),
					false);
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
			OpenMove9.printConsole("Failed to set up OSC " + e.toString(),
					false);
		}
		return msg;
	}

	public static void sendMessage(OSCMessage m) {
		if (OpenMove9.startSending) {
			try {
				OpenMove9.printConsole(
						"Sending " + m.getName() + " " + m.getArg(0), true);
				c.send(m);
			} catch (Exception e) {
				OpenMove9.printConsole("sendMessage error " + e.toString(),
						true);
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
