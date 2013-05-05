/**
 * The explorer controller is used to interface between the model and the view.
 * The controller is in charge of checking that all input taken from the view
 * makes sense, and converting information from the model into a type preferred
 * by the view.
 * 
 * @author Andrew Hollenbach <ahollenbach>
 */

package steam.dbexplorer.controller;

import java.util.HashMap;

import steam.dbexplorer.SystemCode;
import steam.dbexplorer.model.ExplorerModel;

public class ExplorerController {
	private static HashMap<String, String[]> tableLabels;
	
	public static final String[] tableNames = {"Achievements", 
										  	   "Applications", 
										  	   "Friends", 
										  	   "Owned Achievements", 
										  	   "Owned Applications", 
										  	   "Players" };
	
	public static final String[] supportedClauses = {"where",
													 "sort by"};
	
	public static final String[] operators = {"less than",
											  "less than or equal to",
											  "equal to",
											  "not equal to",
											  "greater than or equal to",
											  "greater than"};

	public static final String[] opEquivs = {"<",
											 "<=",
											 "==",
											 "<>",
											 ">=",
											 ">"};
	
	/** 
	 * A string value of the last entity type to be fetched. Might be 
	 * deprecated.
	 */
	private String currentTable;
	
	public ExplorerController() {
		// populate tableLabels
		tableLabels = new HashMap<String, String[]>();
		String[][] labels = {{"Application ID", "Application Name", "Achievement Name"}, //Achievements
							 {"Application ID", "Application Name"}, //Applications
							 {"Steam ID #1", "Steam ID #2"}, //Friends
							 {"Application ID", "Application Name", "Steam ID", "Persona Name", "Achievement Name"}, //Owned achievements
							 {"Application ID", "Application Name", "Steam ID", "Persona Name"}, //Owned applications
							 {"Steam ID", "Persona Name", "Profile URL", "Real Name", "Date Joined"}}; //player
		for(int i=0;i<tableNames.length;i++) {
			tableLabels.put(tableNames[i], labels[i]);
		}
		
		
	}
	
	public Object[][] getData(String tableName, String[] options) {
		this.currentTable = tableName;
		ExplorerModel.setUp();
		Object[][] data = {};
		if(tableName == null) {
		} else if(tableName.equals("Achievements")) {
			data = ExplorerModel.retrieveAchievements(options);
		} else if(tableName.equals("Applications")) {
			data = ExplorerModel.retrieveApplications(options);
		} else if(tableName.equals("Friends")) {
			data = ExplorerModel.retrieveFriends(options);
		} else if(tableName.equals("Owned Achievements")) {
			data = ExplorerModel.retrieveOwnedAchievements(76561198049281288L, options);
		} else if(tableName.equals("Owned Applications")) {
			data = ExplorerModel.retrieveOwnedApplications(76561198049281288L, options);
		} else if(tableName.equals("Players")) {
			data = ExplorerModel.retrievePlayers(options);
		}
		return data;
	}

	public String[] getLabels(String tableName) {
		String[] labels = tableLabels.get(tableName);
		return labels;
	}
	
	/**
	 * Creates an entry using the given string values. Verifies if the 
	 * values are correct. If any of the values are not parsable, returns
	 * false. 
	 * @param values The values to insert
	 * @param entityName The name of the entity to create.
	 * @return
	 */
	public SystemCode createEntry(String entityName, String[] values) {
		int numAttr = tableLabels.get(entityName).length;
		if(numAttr > values.length) {
			String[] valsWithNullStrings = new String[numAttr];
			for(int i=0;i<values.length;i++) {
				valsWithNullStrings[i] = values[i]; 
			}
		}
		return ExplorerModel.createEntity(entityName,values);
	}
	
	public SystemCode deleteEntity(String entityName, String[] values) {
		// should not matter if you send all the values or just the primary
		// keys.
		return ExplorerModel.deleteEntity(entityName, values);
	}
	
	public SystemCode updateEntity(String entityName, String[] values) {
		// should not matter if you send all the values or just the primary
		// keys.
		return ExplorerModel.updateEntity(entityName, values);
	}
}
