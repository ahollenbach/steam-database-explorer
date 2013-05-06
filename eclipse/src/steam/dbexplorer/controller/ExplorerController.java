/**
 * The explorer controller is used to interface between the model and the view.
 * The controller is in charge of checking that all input taken from the view
 * makes sense, and converting information from the model into a type preferred
 * by the view.
 * 
 * @author Andrew Hollenbach <ahollenbach>
 */

package steam.dbexplorer.controller;

import java.io.StringWriter;
import java.io.ObjectInputStream.GetField;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import steam.dbexplorer.SystemCode;
import steam.dbexplorer.dbobject.DBReference;
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
											 "=",
											 "<>",
											 ">=",
											 ">"};
	
	public static final String[] stringOps = {"contains","equals"};
	
	public static final String[] dateOps = {"before","on","after"};
	
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
	
	public SystemCode deleteEntity(String entityName, JSONObject json) {
		/*
		try {
			String[] values = new String[json.length()];
			String[] names = JSONObject.getNames(json);
			
			for(int i=0; i<json.length();i++) {
				String val = json.getString(names[i]);
				values[i] = convertToDbAttr(names[i]) + "=" + val;
			}
			return ExplorerModel.deleteEntity(entityName, values);
		} catch (JSONException e) {
		}
		return SystemCode.FAILURE;*/
		entityName = entityName.substring(0, entityName.length()-1); //remove s
		entityName = entityName.replace(" ", ""); //remove space
		String[] attr = DBReference.primaryKeys.get(entityName);
		String usingTables = DBReference.usingTables.get(entityName);
		
		try {
			int numAttr = attr.length;
			String[] values = new String[attr.length];
			for(int i=0;i<numAttr;i++) {
				String val = json.getString(attr[i]);
				if("string".equals(getAttrType(attr[i]))){
            		val = "\'" + val + "\'";
            	}
				values[i] = convertToDbAttr(attr[i]) + "=" + val;
			}
			return ExplorerModel.deleteEntity(entityName, values,usingTables);
		} catch(JSONException ex) {
			return SystemCode.FAILURE;
		}
	}

	/**
	 * Sends a command to the explorer model to delete the entity. Ensures
	 * the values are in the proper order.
	 * 
	 * @param json A JSONObject containing all the achievement values
	 * @return Whether the operation was successful or not
	 */
	@Deprecated
	public SystemCode deleteEntity(String entityName, String[] attr, JSONObject json, String usingTables) {
		try {
			int numAttr = attr.length;
			String[] values = new String[attr.length];
			for(int i=0;i<numAttr;i++) {
				String val = json.getString(attr[i]);
				if("string".equals(getAttrType(attr[i]))){
            		val = "\'" + val + "\'";
            	}
				values[i] = convertToDbAttr(attr[i]) + "=" + val;
			}
			return ExplorerModel.deleteEntity(entityName, values,usingTables);
		} catch(JSONException ex) {
			return SystemCode.FAILURE;
		}
	}
	
	public SystemCode updateEntity(String entityName, JSONObject json) {
		entityName = entityName.substring(0, entityName.length()-1); //remove s
		entityName = entityName.replace(" ", ""); //remove space
		String[] attr = DBReference.editableValues.get(entityName);
		String[] pKeys = DBReference.primaryKeys.get(entityName);
		String usingTables = DBReference.usingTables.get(entityName);
		
		try {
			int numAttr = attr.length;
			String[] values = new String[attr.length];
			for(int i=0;i<numAttr;i++) {
				String val = json.getString(attr[i]);
				if("string".equals(getAttrType(attr[i]))){
            		val = "\'" + val + "\'";
            	}
				values[i] = dbAttrNoPrefix(attr[i]) + "=" + val;
			}
			String[] keys = new String[attr.length];
			for(int i=0;i<pKeys.length;i++) {
				String val = json.getString(pKeys[i]);
				if("string".equals(getAttrType(pKeys[i]))){
            		val = "\'" + val + "\'";
            	}
				keys[i] = dbAttrNoPrefix(pKeys[i]) + "=" + val;
			}
			return ExplorerModel.updateEntity(entityName, values, keys);
		} catch(JSONException ex) {
			return SystemCode.FAILURE;
		}
	}
	
	public static String convertToDbAttr(String orig) {
		//SO YUCKY GET RID OF THIS
		HashMap<String, String> values = new HashMap<String, String>();
		values.put("Steam ID", "player.steamId");
		values.put("Persona Name", "player.personaName");
		values.put("Profile URL", "player.profileUrl");
		values.put("Real Name", "player.realName");
		values.put("Application ID", "application.appId");
		values.put("Date Joined", "player.timeCreated");
		values.put("Steam ID #1", "friend.steamId1");
		values.put("Steam ID #2", "friend.steamId2");
		values.put("Application Name", "application.appName");
		values.put("Achievement Name", "achievement.achievementName");
		
		return values.get(orig);
	}
	
	public static String dbAttrNoPrefix(String orig) {
		//SO YUCKY GET RID OF THIS
		HashMap<String, String> values = new HashMap<String, String>();
		values.put("Steam ID", "steamId");
		values.put("Persona Name", "personaName");
		values.put("Profile URL", "profileUrl");
		values.put("Real Name", "realName");
		values.put("Application ID", "appId");
		values.put("Date Joined", "timeCreated");
		values.put("Steam ID #1", "steamId1");
		values.put("Steam ID #2", "steamId2");
		values.put("Application Name", "appName");
		values.put("Achievement Name", "achievementName");
		
		return values.get(orig);
	}
	
	public static String getAttrType(String orig) {
		//SO YUCKY GET RID OF THIS TOO
		HashMap<String, String> values = new HashMap<String, String>();
		values.put("Steam ID", "long");
		values.put("Persona Name", "string");
		values.put("Profile URL", "string");
		values.put("Real Name", "string");
		values.put("Application ID", "long");
		values.put("Date Joined", "time");
		values.put("Steam ID #1", "long");
		values.put("Steam ID #2", "long");
		values.put("Application Name", "string");
		values.put("Achievement Name", "string");
		
		return values.get(orig);
	}
	
	public String getCurrentTable() {
		return currentTable;
	}
}
