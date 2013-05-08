/**
 * This is a static reference class to the DDL. Every hashmap can be keyed 
 * using values from the tableNames array.
 * All values are in their human-readable form.
 * 
 *  @author Andrew Hollenbach <anh7216@rit.edu>
 *  @author Andrew DeVoe <ard5852@rit.edu>
 */
package steam.dbexplorer.dbobject;

import java.util.HashMap;

public class DBReference {
	/**
	 * This stores a list of the column labels for each table that are to be
	 * presented to the end user. Only includes actual table values.
	 */
	public static HashMap<String, String[]> displayNames   = new HashMap<String, String[]>();
	
	/**
	 * Stores a list of column labels for those attributes which
	 * can be edited (non-primary key elements)
	 */
	public static HashMap<String, String[]> editableValues = new HashMap<String, String[]>();
	
	/**
	 * A list of the primary key elements for each table.
	 */
	public static HashMap<String, String[]> primaryKeys    = new HashMap<String, String[]>();
	
	/**
	 * A list of the foreign tables each table uses.
	 */
	public static HashMap<String, String>   usingTables    = new HashMap<String, String>();
	
	/**
	 * This stores a list of the column labels for each table that are to be
	 * presented to the end user. May include joined information from other 
	 * tables.
	 * Use this hashmap for populate the labels in the JTable!
	 */
	public static HashMap<String, String[]> tableLabels    = new HashMap<String, String[]>();
	
	/**
	 * A list of the different table names. You must always use one of these values
	 * to properly key on any of the hashmaps in this class.
	 */
	public static final String[] tableNames = new String[] {"Player",
															"Friend",
															"Application",
															"Achievement",
															"OwnedAchievement",
															"OwnedApplication"};
	/**
	 * Addes all the values to the static hashmaps.
	 */
	static {
		displayNames.put("Player", new String[] {"Steam ID", "Persona Name", "Profile URL", "Real Name", "Date Joined"});
		displayNames.put("Friend", new String[] {"Steam ID #1", "Steam ID #2"});
		displayNames.put("Application", new String[] {"Application ID", "Application Name"});
		displayNames.put("Achievement", new String[] {"Application ID", "Achievement Name"});
		displayNames.put("OwnedAchievement", new String[] {"Application ID", "Achievement Name", "Steam ID"});
		displayNames.put("OwnedApplication", new String[] {"Application ID", "Steam ID"});
		
		editableValues.put("Player", new String[] {"Persona Name", "Profile URL", "Real Name", "Date Joined"});
		editableValues.put("Friend", new String[] {});
		editableValues.put("Application", new String[] {"Application Name"});
		editableValues.put("Achievement", new String[] {});
		editableValues.put("OwnedAchievement", new String[] {});
		editableValues.put("OwnedApplication", new String[] {});
		
		primaryKeys.put("Player", new String[] {"Steam ID"});
		primaryKeys.put("Friend", new String[] {"Steam ID #1", "Steam ID #2"});
		primaryKeys.put("Application", new String[] {"Application ID"});
		primaryKeys.put("Achievement", new String[] {"Application ID", "Achievement Name"});
		primaryKeys.put("OwnedAchievement", new String[] {"Application ID", "Achievement Name", "Steam ID"});
		primaryKeys.put("OwnedApplication", new String[] {"Application ID", "Steam ID"});
		
		usingTables.put("Player", "");
		usingTables.put("Friend", "Player");
		usingTables.put("Application", "");
		usingTables.put("Achievement", "Application");
		//usingTables.put("OwnedAchievement", "Application,Achievement,Player");
		usingTables.put("OwnedAchievement", "Application,Player");
		usingTables.put("OwnedApplication", "Application,Player");
		
		tableLabels.put("Player", new String[] {"Steam ID", "Persona Name", "Profile URL", "Real Name", "Date Joined"});
		tableLabels.put("Friend", new String[] {"Steam ID #1", "Persona Name #1", "Steam ID #2", "Persona Name #2"});
		tableLabels.put("Application", new String[] {"Application ID", "Application Name"});
		tableLabels.put("Achievement", new String[] {"Application ID", "Application Name", "Achievement Name"});
		tableLabels.put("OwnedAchievement", new String[] {"Application ID", "Application Name", "Steam ID", "Persona Name", "Achievement Name"});
		tableLabels.put("OwnedApplication", new String[] {"Application ID", "Application Name", "Steam ID", "Persona Name"});
	}
	
	/**
	 * Checks if the given attribute name is a primary key
	 * for the supplied table.
	 * 
	 * @param tableName The table to check the attribute on
	 * @param attrName The attribute to check
	 * @return True if the attribute is a primary key, otherwise
	 * false.
	 */
	public static Boolean isPrimaryKey(String tableName,String attrName) {
		if(tableName == null) {
			return false;
		} else {
			tableName = convertToDBFormat(tableName);
			return contains(primaryKeys.get(tableName), attrName);
		}
	}
	
	/**
	 * Checks if a value is contained in a list of values
	 * 
	 * @param values The values to search through
	 * @param value A value to check for
	 * @return True if values contains value,
	 * otherwise false
	 */
	private static Boolean contains(String[] values, String value) {
		for(int i=0;i<values.length;i++) {
			if(value.equals(values[i])) return true;
		}
		return false;
	}
	
	/**
	 * Given the table and a column number, it will return the display
	 * name (human-readable) label of the column.
	 * Note: this is needed for translating upwards (from database result up)
	 * 
	 * @param table The table to look at, typically in the format "achievement"
	 * @param colNum The column number
	 * @return The attribute name in human-readable format
	 */
	public static String getDisplayName(String table, int colNum) {
		String upper = table.substring(0, 1).toUpperCase() + table.substring(1);
		return displayNames.get(upper)[colNum];
	}
	
	/**
	 * Converts a table name from human-readable to database-compatible.
	 * Yay!
	 * <pre>
	 * i.e. Achievements       -> Achievement
	 *      Owned Achievements -> OwnedAchievement
	 * </pre>
	 * 
	 * @param entityName The name of the entity to convert
	 * @return The entity's name converted to database format
	 */
	public static String convertToDBFormat(String entityName) {
		if(entityName != null && entityName.length() > 0) {
			entityName = entityName.substring(0, entityName.length()-1); //remove s
			entityName = entityName.replace(" ", ""); //remove space
		}
		return entityName;
	}
}
