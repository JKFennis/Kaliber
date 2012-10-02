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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.text.DecimalFormat;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import static javax.swing.UIManager.getCrossPlatformLookAndFeelClassName;
import static javax.swing.UIManager.getSystemLookAndFeelClassName;
import static javax.swing.UIManager.setLookAndFeel;
import net.miginfocom.swing.MigLayout;

public class Kaliber extends JFrame implements ActionListener, MouseListener {
	private static final long serialVersionUID = 3186649664045658397L;
	static int DELAY = 100;

	public static boolean startSending = false;
	public static boolean sendingMode = false;
	// the currently selected device in controllerTable
	Device curDevice = null;

	// osc instance, will be singleton
	static OSCmachine osc;

	/** Creates new form OpenMOve */
	public Kaliber() {
		setJMenuBar(createMenuBar());
		setTitle("Kaliber 1.0.0");
		initComponents();
		// setup osc
		osc = OSCmachine.getInstance();
		ControllerTable.setModel(new ControllerTableModel());
		TableColumn column = null;
		for (int i = 0; i < 4; i++) {
			column = ControllerTable.getColumnModel().getColumn(i);
			switch (i) {
			case 0:
				column.setPreferredWidth(25);
				column.setMaxWidth(25);
				break;
			case 1:
				column.setPreferredWidth(225);
				break;
			case 2:
				column.setPreferredWidth(125);
				column.setMaxWidth(125);
				break;
			case 3:
				column.setPreferredWidth(125);
				column.setMaxWidth(125);
				break;
			}
			column = null;
		}
		ComponentTable.setModel(new ComponentTableModel());
		for (int i = 0; i < 6; i++) {
			column = ComponentTable.getColumnModel().getColumn(i);
			switch (i) {
			case 0:
				column.setPreferredWidth(25);
				column.setMaxWidth(25);
				break;
			case 1:
				column.setPreferredWidth(225);
				break;
			case 2:
				column.setPreferredWidth(62);
				column.setMaxWidth(62);
				break;
			case 3:
				column.setPreferredWidth(62);
				column.setMaxWidth(62);
				break;
			case 4:
				column.setPreferredWidth(63);
				column.setMaxWidth(63);
				break;
			case 5:
				column.setPreferredWidth(63);
				column.setMaxWidth(63);
				break;
			}
		}

		ControllerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ComponentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// TODO: fix actionlisteners
		/*
		 * Register Action and MouseListeners
		 */
		startButton.addActionListener(this);
		stopButton.addActionListener(this);
		openButton.addActionListener(this);
		saveButton.addActionListener(this);
		newButton.addActionListener(this);
		modeButton.addActionListener(this);

		startButton.addMouseListener(this);
		stopButton.addMouseListener(this);
		openButton.addMouseListener(this);
		saveButton.addMouseListener(this);
		newButton.addMouseListener(this);
		modeButton.addMouseListener(this);

		outputPort.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				int start = 0;
				int end = outputPort.getText().length();
				outputPort.setSelectionStart(start);
				outputPort.setSelectionEnd(end);
			}

			public void focusLost(FocusEvent e) {
				String newOutPort = outputPort.getText();
				if (outputPort.getText().length() != 0)
					try {
						osc.setOutPort(newOutPort);
					} catch (NumberFormatException ex) {
					}
			}

		});

		startPolling();
		/*
		 * Listener that calls function valueChanged each time a different
		 * controller is selected Valuechanged() updates the tablemodel of
		 * componentTable
		 */
		ControllerTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting())
							return;
						ListSelectionModel lsm = ControllerTable
								.getSelectionModel();
						int fi = e.getFirstIndex();
						int li = e.getLastIndex();
						int index = lsm.isSelectedIndex(fi) ? fi : li;

						try {
							curDevice = Device.getDevice(index);
							/*
							 * Check if abstract object Device is of subclass
							 * controller or move
							 */
							if (curDevice instanceof MoveController) {
								ComponentTable
										.setModel(new MoveComponentTableModel(
												curDevice));
							} else if (curDevice instanceof JInputDevice) {
								ComponentTable
										.setModel(new ComponentTableModel(
												curDevice));
							}
							TableColumn column = null;
							for (int i = 0; i < 6; i++) {
								column = ComponentTable.getColumnModel()
										.getColumn(i);
								switch (i) {
								case 0:
									column.setPreferredWidth(25);
									column.setMaxWidth(25);
									break;
								case 1:
									column.setPreferredWidth(225);
									break;
								case 2:
									column.setPreferredWidth(62);
									column.setMaxWidth(62);
									break;
								case 3:
									column.setPreferredWidth(62);
									column.setMaxWidth(62);
									break;
								case 4:
									column.setPreferredWidth(63);
									column.setMaxWidth(63);
									break;
								case 5:
									column.setPreferredWidth(63);
									column.setMaxWidth(63);
									break;
								}
								column = null;
							}
						} catch (IndexOutOfBoundsException ex) {
							printConsole("Failed to set table model", false);
						}
					}
				});
	}

	// TODO: fix listener functions
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == startButton) {
			if (!startSending) {
				startSending = true;
				startButton
						.setIcon(new ImageIcon(startSelected));
			}
		}
		if (e.getSource() == stopButton) {
			if (startSending) {
				startSending = false;
				startButton.setIcon(new ImageIcon(start));
			}
		}
		if (e.getSource() == openButton || e.getSource() == Open) {
			theChooser.setDialogType(JFileChooser.OPEN_DIALOG);
			theChooser.showOpenDialog(getParent());
		}
		if (e.getSource() == saveButton || e.getSource() == Save) {
			theChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			theChooser.showSaveDialog(getParent());
		}
		if (e.getSource() == newButton || e.getSource() == New) {
			for (Device d : Device.Devices) {
				d.initMinValues(d.getNumComponents());
				d.initMaxValues(d.getNumComponents());
				d.initPlugNames(d.getNumComponents());
				d.setIsSelected(false);
				for (int row = 0; row < ControllerTable.getRowCount(); row++) {
					if (d.getIsSelected())
						ControllerTable.getModel().setValueAt(false, row, 0);
				}
				for (int c = 0; c < d.getNumComponents(); c++) {
					d.setcompSelected(c, false);
				}
			}
			ControllerTable.repaint();
		}
		if (e.getSource() == modeButton || e.getSource() == change
				|| e.getSource() == cont) {
			if (sendingMode) {
				sendingMode = false;
				modeButton.setIcon(new ImageIcon(changeImg));
				modeButton.setPressedIcon(new ImageIcon(
						changeSelected));
				modeButton.setRolloverIcon(new ImageIcon(
						changeRollOver));
				actionLabel.setText("Send On Action");
				change.setSelected(true);
			} else if (!sendingMode) {
				sendingMode = true;
				modeButton.setIcon(new ImageIcon(contImg));
				modeButton.setPressedIcon(new ImageIcon(
						contSelected));
				modeButton.setRolloverIcon(new ImageIcon(
						contRollOver));
				actionLabel.setText("Send Continuous");
				cont.setSelected(true);
			}
		}
	}

	public void mouseEntered(java.awt.event.MouseEvent evt) {
		if (evt.getSource() == startButton) {
			actionLabel.setText("Start");
		}
		if (evt.getSource() == stopButton) {
			actionLabel.setText("Stop");
		}
		if (evt.getSource() == openButton) {
			actionLabel.setText("Open");
		}
		if (evt.getSource() == saveButton) {
			actionLabel.setText("Save");
		}
		if (evt.getSource() == newButton) {
			actionLabel.setText("New");
		}
		if (evt.getSource() == modeButton) {
			if (sendingMode)
				actionLabel.setText("Send Continuous");
			if (!sendingMode)
				actionLabel.setText("Send On Action");
		}
	}

	public void mouseExited(java.awt.event.MouseEvent evt) {
		actionLabel.setText("");
	}

	public void mouseClicked(java.awt.event.MouseEvent evt) {/* not implemented */
	}

	public void mousePressed(MouseEvent e) {/* not implemented */
	}

	public void mouseReleased(MouseEvent e) {/* not implemented */
	}

	public JMenuBar createMenuBar() {
		// Create the menu bar.
		menuBar = new JMenuBar();

		// Build the first menu.
		menu = new JMenu("File");
		menu.getAccessibleContext().setAccessibleDescription(
				"The only menu in this program that has menu items");
		menuBar.add(menu);

		// a group of JMenuItems
		New = new JMenuItem("New", KeyEvent.VK_N);

		New.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.META_MASK));
		New.getAccessibleContext().setAccessibleDescription(
				"This doesn't really do anything");
		menu.add(New);
		New.addActionListener(this);

		Open = new JMenuItem("Open", KeyEvent.VK_O);
		Open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.META_MASK));
		Open.getAccessibleContext().setAccessibleDescription(
				"This doesn't really do anything");
		menu.add(Open);
		Open.addActionListener(this);
		Save = new JMenuItem("Save", KeyEvent.VK_S);
		Save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.META_MASK));
		Save.getAccessibleContext().setAccessibleDescription(
				"This doesn't really do anything");
		menu.add(Save);
		Save.addActionListener(this);

		menu = new JMenu("Mode");
		menu.getAccessibleContext().setAccessibleDescription(
				"The only menu in this program that has menu items");
		menuBar.add(menu);

		// a group of radio button menu items
		ButtonGroup group = new ButtonGroup();

		change = new JRadioButtonMenuItem("On Change");
		change.setSelected(true);
		group.add(change);
		menu.add(change);
		cont = new JRadioButtonMenuItem("Continuous");
		group.add(cont);
		menu.add(cont);
		change.addActionListener(this);
		cont.addActionListener(this);
		return menuBar;
	}

	public void startPolling() {
		(new Timer(DELAY, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Update the table to see 'live' values
				ComponentTable.repaint();
				if (Device.getNumDevices() >= 1) {
					for (Device d : Device.Devices)
						d.poll();
					console.setCaretPosition(console.getDocument().getLength());
				}
			}
		})).start();
	}

	public static void printConsole(String event, boolean directionOut) {
		timeStamp = new Date();
		console.append(" ");
		console.append("[" + DateFormat.getTimeInstance().format(timeStamp)
				+ "]");
		console.append(" ");
		if (directionOut)
			console.append(">");
		if (!directionOut)
			console.append("<");
		console.append(" ");
		console.append(event);
		console.append("\n");
	}

	/*
	 * Initializing GUI components
	 */
	private void initComponents() {
		controllerScrollPanel = new JScrollPane();
		ControllerTable = new JTable();
		componentScrollPanel = new JScrollPane();
		ComponentTable = new JTable();
		console = new JTextArea();
		console.setEditable(false);
		console.setBackground(Color.BLACK);
		console.setFont(new Font("Sans Serif", Font.PLAIN, 11));
		console.setForeground(Color.WHITE);
		console.setLineWrap(true);

		consolePane = new JScrollPane(console);
		SplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
				componentScrollPanel, consolePane);

		Border border = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		controllerScrollPanel.setBorder(border);
		componentScrollPanel.setBorder(border);
		consolePane.setBorder(border);
		setBackground(new Color(170, 170, 170));

		SplitPane.setOpaque(true);
		SplitPane.setBorder(null);

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setMinimumSize(new java.awt.Dimension(200, 300));
		// setPreferredSize(new java.awt.Dimension(500, 400));
		// setMaximumSize(new java.awt.Dimension(500, 800));

		ControllerTable.setAutoCreateRowSorter(false);
		ControllerTable.setModel(new DefaultTableModel(new Object[][] {
				{ null, null, null, null }, { null, null, null, null },
				{ null, null, null, null }, { null, null, null, null } },

		new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
		// ControllerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		controllerScrollPanel.setViewportView(ControllerTable);

		ComponentTable.setModel(new DefaultTableModel(new Object[][] {
				{ null, null, null, null }, { null, null, null, null },
				{ null, null, null, null }, { null, null, null, null } },
				new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
		// ComponentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		componentScrollPanel.setViewportView(ComponentTable);

		try {
			// Eclispe: images must reside in package folder
			start = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_start.png"));
			startRollOver = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_start_rollover.png"));
			startSelected = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_start_selected.png"));
			stop = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_stop.png"));
			stopRollOver = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_stop_rollover.png"));
			stopSelected = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_stop_selected.png"));
			
			saveImg = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_save.png"));
			saveRollOver = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_save_rollover.png"));
			saveSelected = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_save_selected.png"));
			
			openImg = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_open.png"));
			openRollOver = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_open_rollover.png"));
			openSelected = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_open_selected.png"));
			
			newImg = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_new.png"));
			newRollOver = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_new_rollover.png"));
			newSelected = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_new_selected.png"));
			
			changeImg = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_onchange.png"));
			changeRollOver = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_onchange_rollover.png"));
			changeSelected = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_onchange_selected.png"));
			
			contImg = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_continuous.png"));
			contRollOver = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_continuous_rollover.png"));
			contSelected = javax.imageio.ImageIO.read(ClassLoader
					.getSystemResource("images/JButtons_continuous_selected.png"));
			// Eclispe: works if placed in root project folder
			// image = javax.imageio.ImageIO.read(new
			// java.io.File("JButtons_start.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// javax.swing.ImageIcon icon = new javax.swing.ImageIcon(start);

		startButton = new ImageButton(new ImageIcon(start));
		// startButton = new ImageButton(new ImageIcon("JButtons_start.png"));
		stopButton = new ImageButton(new ImageIcon(stop));
		openButton = new ImageButton(new ImageIcon(openImg));
		saveButton = new ImageButton(new ImageIcon(saveImg));
		newButton = new ImageButton(new ImageIcon(newImg));
		modeButton = new ImageButton(new ImageIcon(changeImg));
		actionLabel = new JLabel("");
		actionLabel.setFont(new Font("Sans Serif", Font.PLAIN, 12));

		startButton.setPressedIcon(new ImageIcon(startSelected));
		startButton
				.setRolloverIcon(new ImageIcon(startRollOver));
		startButton
				.setSelectedIcon(new ImageIcon(startRollOver));
		startButton
				.setDisabledIcon(new ImageIcon(startRollOver));

		stopButton.setPressedIcon(new ImageIcon(stopSelected));
		stopButton.setRolloverIcon(new ImageIcon(stopRollOver));
		stopButton.setSelectedIcon(new ImageIcon(stopRollOver));
		stopButton.setDisabledIcon(new ImageIcon(stopRollOver));

		openButton.setPressedIcon(new ImageIcon(openSelected));
		openButton.setRolloverIcon(new ImageIcon(openRollOver));
		openButton.setSelectedIcon(new ImageIcon(openRollOver));
		openButton.setDisabledIcon(new ImageIcon(openRollOver));

		saveButton.setPressedIcon(new ImageIcon(saveSelected));
		saveButton.setRolloverIcon(new ImageIcon(saveRollOver));
		saveButton.setSelectedIcon(new ImageIcon(saveRollOver));
		saveButton.setDisabledIcon(new ImageIcon(saveRollOver));

		newButton.setPressedIcon(new ImageIcon(newSelected));
		newButton.setRolloverIcon(new ImageIcon(newRollOver));
		newButton.setSelectedIcon(new ImageIcon(newRollOver));
		newButton.setDisabledIcon(new ImageIcon(newRollOver));
		modeButton.setPressedIcon(new ImageIcon(
				changeSelected));
		modeButton.setRolloverIcon(new ImageIcon(
				changeRollOver));

		statusbar = new JPanel();
		JLabel statusLabel = new JLabel(
				"OSC Address 127.0.0.1 | Listening @ port 8000");
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setFont(new Font("Sans Serif", Font.PLAIN, 11));
		statusbar.add(statusLabel);
		outPortLabel = new JLabel(" Output port");
		outPortLabel.setHorizontalAlignment(SwingConstants.CENTER);
		outPortLabel.setFont(new Font("Sans Serif", Font.PLAIN, 11));
		outputPort = new JFormattedTextField(new DecimalFormat("##"));
		outputPort.setText("12000");
		outputPort.setHorizontalAlignment(JTextField.CENTER);
		outputPort.setColumns(4);

		// The layout has 1 extra column, allowing easy spacing between elements
		getContentPane().setLayout(
				new MigLayout("insets 0 0 0 0",
						"0[]-10[]-10[]5[]-8[]-8[][]push[][]0",
						"0[]0[]0[grow]0[]0"));

		// Row 0
		getContentPane().add(startButton, "cell 0 0");
		getContentPane().add(stopButton, "cell 1 0");
		getContentPane().add(modeButton, "cell 2 0");
		getContentPane().add(newButton, "cell 3 0");
		getContentPane().add(openButton, "cell 4 0");
		getContentPane().add(saveButton, "cell 5 0");
		getContentPane().add(actionLabel, "cell 6 0");
		getContentPane().add(outPortLabel, "cell 7 0");
		getContentPane().add(outputPort, "cell 8 0, gapright 5");
		// Row 2
		// controllerScrollPanel.setPreferredSize(new
		// Dimension(WIDTH,(Device.Devices.size()*50)));
		getContentPane().add(controllerScrollPanel,
				"cell 0 1 9 1, grow, height 120:120:120");
		// Row 3
		// getContentPane().add(componentScrollPanel,
		// "cell 0 2 9 1, width 200:500:500, height 0:600:1600");

		// Row 4
		getContentPane().add(SplitPane, "cell 0 2 9 0, grow");
		// Row 5
		getContentPane().add(statusbar, "cell 0 3 9 1, grow, height 24:24:24");
		//SplitPane.setDividerLocation(0.5);
		SplitPane.setResizeWeight(1);
		theChooser = new JFileChooser();
		FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter(
				"xml files (*.xml)", "xml");
		theChooser.setFileFilter(xmlfilter);

		theChooser.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 2733533319904047170L;

			public void actionPerformed(ActionEvent evt) {
				JFileChooser chooser = (JFileChooser) evt.getSource();
				// Ok was clicked
				if (JFileChooser.APPROVE_SELECTION.equals(evt
						.getActionCommand())) {
					if (chooser.getDialogType() == 0) {

						File theFile = chooser.getSelectedFile();
						openPreset(theFile);
					} else if (chooser.getDialogType() == 1) {
						File theFile = chooser.getSelectedFile();
						String nameOfFile = "";
						nameOfFile = theFile.getPath();
						savePreset(theFile, nameOfFile);
					}
				} else if (JFileChooser.CANCEL_SELECTION.equals(evt
						.getActionCommand())) {
					// Cancel was clicked
					printConsole("Canceled", false);
				}
			}
		});
		pack();
	}

	protected void savePreset(File theFile, String FileName) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("controllerList");
			doc.appendChild(rootElement);
			for (Device d : Device.Devices) {
				// controller elements
				Element XMLDevice = doc.createElement("Device");
				rootElement.appendChild(XMLDevice);

				// set controller id
				Attr deviceType = doc.createAttribute("id");
				deviceType.setValue(d.getName()
						+ Integer.toString(d.getDeviceIndex()).trim());
				XMLDevice.setAttributeNode(deviceType);

				// pass in number of components
				Attr numofComp = doc.createAttribute("NoC");
				numofComp.setValue(Integer.toString(d.getNumComponents()));
				XMLDevice.setAttributeNode(numofComp);

				// save if controller is selected
				Attr selected = doc.createAttribute("selected");
				selected.setValue(new Boolean(d.getIsSelected()).toString()
						.trim());
				XMLDevice.setAttributeNode(selected);

				for (int i = 0; i < d.getNumComponents(); i++) {
					// isComponent selected elements
					Element compSelected = doc.createElement("compSelected");
					compSelected.appendChild(doc.createTextNode(new Boolean(d
							.getSelectedComponent(i)).toString().trim()));
					XMLDevice.appendChild(compSelected);

					// minVal elements
					Element minVal = doc.createElement("minVal");
					minVal.appendChild(doc.createTextNode(Float.toString(d
							.getMinValue(i))));
					XMLDevice.appendChild(minVal);

					// maxVal elements
					Element maxVal = doc.createElement("maxVal");
					maxVal.appendChild(doc.createTextNode(Float.toString(d
							.getMaxValue(i))));
					XMLDevice.appendChild(maxVal);

					// plug elements
					Element plug = doc.createElement("plug");
					plug.appendChild(doc.createTextNode(d.getPlugName(i)));
					XMLDevice.appendChild(plug);
				}
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(FileName));
			if (FileName.endsWith(".xml")) {
				transformer.transform(source, result);
			} else {
				result = new StreamResult(new File(FileName + ".xml"));
				transformer.transform(source, result);
			}
			printConsole("File: " + FileName + " saved!", true);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	protected void openPreset(File theFile) {
		try {
			File fXmlFile = theFile;
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			NodeList DeviceList = doc.getElementsByTagName("Device");

			for (int temp = 0; temp < DeviceList.getLength(); temp++) {
				Node xmlDevice = DeviceList.item(temp);
				if (xmlDevice.getNodeType() == Node.ELEMENT_NODE) {
					for (Device d : Device.Devices) {
						Element eElement = (Element) xmlDevice;
						String deviceName = eElement.getAttribute("id").trim();
						int numOfComponents = Integer.parseInt(eElement
								.getAttribute("NoC").trim());
						boolean isSelected = Boolean.parseBoolean(eElement
								.getAttribute("selected").trim());
						if (deviceName.equals(d.getName()
								+ Integer.toString(d.getDeviceIndex()).trim())) {
							d.setIsSelected(isSelected);
							for (int i = 0; i < numOfComponents; i++) {
								boolean compSelected = getBooleanValue(
										"compSelected", eElement, i);
								d.setcompSelected(i, compSelected);
								float minval = getFloatValue("minVal",
										eElement, i);
								d.setMinValue(i, minval);
								float maxval = getFloatValue("maxVal",
										eElement, i);
								d.setMaxValue(i, maxval);
								String plug = getPlugValue("plug", eElement, i);
								d.setPlugName(i, plug);
							}
						}
					}
				}
			}
			printConsole("File: " + theFile.getName() + " opened", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean getBooleanValue(String sTag, Element eElement, int i) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(i)
				.getChildNodes();
		Node nValue = (Node) nlList.item(0);
		return Boolean.parseBoolean(nValue.getNodeValue().trim());
	}

	private static int getIntValue(String sTag, Element eElement, int i) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(i)
				.getChildNodes();
		Node nValue = (Node) nlList.item(0);
		return Integer.parseInt(nValue.getNodeValue().trim());
	}

	private static float getFloatValue(String sTag, Element eElement, int i) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(i)
				.getChildNodes();
		Node nValue = (Node) nlList.item(0);
		return Float.parseFloat(nValue.getNodeValue().trim());
	}

	private static String getPlugValue(String sTag, Element eElement, int i) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(i)
				.getChildNodes();
		Node nValue = (Node) nlList.item(0);
		return nValue.getNodeValue().trim();
	}

	private static boolean isPlatform(String platform) {
		return System.getProperties().getProperty(OS_NAME).toLowerCase()
				.contains(platform.toLowerCase());
	}

	private static String getLookAndFeel() {
		// if (true) return "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
		// Linux has issues with the gtk look and feel themes.
		if (isPlatform(LINUX))
			return getCrossPlatformLookAndFeelClassName();
		return getSystemLookAndFeelClassName();
	}

	public static void main(String args[]) {
		try {
			if (isPlatform(MAC_OS_X)) {
				System.setProperty("apple.awt.rendering", "true");
				System.setProperty("apple.awt.brushMetalLook", "true");
				System.setProperty("apple.laf.useScreenMenuBar", "true");
				System.setProperty(
						"apple.awt.window.position.forceSafeCreation", "true");
				System.setProperty("apple.laf.useScreenMenuBar", "true");
			}
			setLookAndFeel(getLookAndFeel());
			// make sure we have nice window decorations.
			setDefaultLookAndFeelDecorated(true);

		} catch (Exception ex) {
		}
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new Kaliber().setVisible(true);
			}
		});
	} // End of Main()

	// Variables declaration
	private JSplitPane ControllerSplitPane, SplitPane;
	private JTable ControllerTable, ComponentTable;
	private JScrollPane controllerScrollPanel, componentScrollPanel,
			consolePane;
	private JFileChooser theChooser;
	private JFormattedTextField outputPort;
	private JLabel outPortLabel, actionLabel;
	private JPanel statusbar;
	private static JTextArea console;
	private ImageButton startButton, stopButton, newButton, openButton,
			saveButton, modeButton;
	private JMenuBar menuBar;
	private JMenu menu;
	private JMenuItem New, Open, Save;
	private JRadioButtonMenuItem change, cont;
	private static Date timeStamp;

	private BufferedImage start, startRollOver, startSelected, stop,
			stopRollOver, stopSelected, contImg, contRollOver, contSelected,
			changeImg, changeRollOver, changeSelected, openImg, openRollOver,
			openSelected, saveImg, saveRollOver, saveSelected, newImg, newRollOver, newSelected;

	private static final String OS_NAME = "os.name";
	private static final String MAC_OS_X = "Mac OS X";
	private static final String LINUX = "Linux";

	// End of variables declaration

} // End of openMove class
