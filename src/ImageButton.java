import java.awt.*;
import javax.swing.*;

public class ImageButton extends JButton {
	private static final long serialVersionUID = 7691895821277874814L;

	public ImageButton(ImageIcon icon) {

		setSize(icon.getImage().getWidth(null), icon.getImage().getHeight(null));
		setIcon(icon);
		setMargin(new Insets(0, 0, 0, 0));
		setIconTextGap(0);
		setBorderPainted(false);
		setBorder(null);
		setText(null);
	}

}
