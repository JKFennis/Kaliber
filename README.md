KALIBER

Kaliber is an open source software built to handle data between computers and devices. The desktop application communicates through the OSC protocol and can be used together with different programming environments.

Mail to:
kaliber@interactiondesign.se

Created by:
Adam Henriksson <www.adamhenriksson.com>
Jules Fennis <www.julesfennis.nl>

Funded by:
HUMlab <http://www.humlab.umu.se>

Built at:
HUMlab X <http://blog.humlab.umu.se>
Umeå Institute of Design <http://www.dh.umu.se>

Special thanks to:
Thomas Perl
Camille Moussette
Douglas Wilson

Related projects:
OSCulator <http://www.osculator.net>
GlovePIE <https://sites.google.com/site/carlkenner/glovepie>
PS Move API <http://thp.io/2010/psmove>
UniMove <http://www.copenhagengamecollective.org/projects/unimove>

————————————————————

V. 1.0.0 — 09092012

APIs added:
- JInput <http://java.net/projects/jinput>
- PS Move API <http://thp.io/2010/psmove>
- NetUtil <http://www.sciss.de/netutil>
- MigLayout <http://www.miglayout.com>

Devices tested:
- Mouse
- Keyboard
- Wacom tablets
- Playstation Dualshock 3 Sixaxis
- Playstation Move
- Xbox 360 Gamepad for PC (USB)
- 3Dconnexion Spacenavigator
- Dance Dance pad (USB)
- Arduino Leonardo <http://arduino.cc/en/Reference/MouseKeyboard>


Known bugs:
- OSX creates an extra non usable mouse.
- PS Move LED and Rumble creates a background error if wrong values (floats) are received.
- PS Move does not allow simultaneous button presses.
- PS Move acceleration, gyro and magnetometer values does not cap at min and max.
- Mouse X, Y and scroll values does not cap at min and max.
- Wacom tables crashes the application on start.
- Plug names are not saved on change.

Tested on OSX 10.6 and 10.7, fixes needed for 10.5 and 10.8
Fix loading of Java Native/Dynamic libraries (both Windows and OS X)
————————————————————

Screenshot
![Screenshot](https://github.com/JKFennis/Kaliber/tree/master/img/Kaliber_app.png)\
