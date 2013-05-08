package steam.dbexplorer.view;

import javax.swing.JOptionPane;

/**
 * PopupFactory allows the program to create quick, personalized popups that 
 * display the message you want.
 *
 * @author Andrew Hollenbach (anh7216@rit.edu)
 */
public class PopupFactory {
	/**
	 * Creates an error popup with the given title and message
	 * @param title The title of the popup to show up in the upper left
	 * @param message The message to display to the user
	 */
	public static void errorPopup(String title, String message) {
		JOptionPane.showMessageDialog(null,
								      message,
								      title,
								      JOptionPane.ERROR_MESSAGE);
	}
}
