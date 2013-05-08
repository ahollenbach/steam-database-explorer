/**
 *  Main project file.  Either executes the Steam Database Explorer graphical user interface
 *      or the populate method to populate the database.
 *  
 *  @author Andrew Hollenbach <anh7216@rit.edu>
 *  @author Andrew DeVoe <ard5852@rit.edu>
 */

package steam.dbexplorer;

import steam.dbexplorer.controller.ExplorerController;
import steam.dbexplorer.view.ExplorerView;

public class Main {
	
	public static final String programName = "Steam Database Explorer";
	
	/**
	 * Executes the Steam Database Explorer or Populate method
	 * 
	 * @param args  No args are expected
	 */
	public static void main(String[] args) {
		ExplorerController controller = new ExplorerController();
		ExplorerView view = new ExplorerView(controller);
		view.start();
		//Populate.populate();
	}
}
