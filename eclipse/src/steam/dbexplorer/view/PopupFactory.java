package steam.dbexplorer.view;

import javax.swing.JOptionPane;

public class PopupFactory {
	public static void errorPopup(String title, String message) {
		JOptionPane.showMessageDialog(null,
								      message,
								      title,
								      JOptionPane.ERROR_MESSAGE);
	}
}
