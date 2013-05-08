package steam.dbexplorer;

import steam.dbexplorer.controller.ExplorerController;
import steam.dbexplorer.view.ExplorerView;

/**
 *  Main project file.  Either executes the Steam Database Explorer graphical user interface
 *      or the populate method to populate the database.
 *  
 *  @author Andrew Hollenbach <anh7216@rit.edu>
 *  @author Andrew DeVoe <ard5852@rit.edu>
 */
public class Main {
	
	public static final String programName = "Steam Database Explorer";
	
	/**
	 * Executes the Steam Database Explorer or Populate method
	 * 
	 * @param args  Whether to run populate or not.  Populate will
	 *     be ran only if the args has exactly one parameter which is
	 *     populate.  Otherwise the GUI is made.
	 */
	public static void main(String[] args) {
		if (args.length == 1 && args[0].toLowerCase().equals("populate")) {
			Populate.populate();
		} 
		else {
			ExplorerController controller = new ExplorerController();
			ExplorerView view = new ExplorerView(controller);
			view.start();
		}
	}
}
