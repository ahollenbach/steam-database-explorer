/**
 * The explorer model is a model of the database tables in Java. This class 
 * can be used to interface with the psql database for all the CRUD operations.
 * 
 * @author Andrew Hollenbach <ahollenbach>
 */
package steam.dbexplorer.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.github.koraktor.steamcondenser.steam.community.WebApi;

import steam.dbexplorer.Credentials;
import steam.dbexplorer.SystemCode;

public class ExplorerModel {
	
	static Connection con;
	
    public static void setUp() {
		try {
			WebApi.setApiKey(Credentials.APIKEY);

			String url = Credentials.DATABASEURL;
			Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection(url, Credentials.DATABASEUSERNAME ,Credentials.DATABASEPASSWORD);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static void tearDown() {
    	try {
		    con.close();
    	} catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    
	/**
	 * A map of the primary keys in the table. This is used in the generic
	 * update/delete entity methods.
	 */
	private static final HashMap<String, String[]> tableKeys;
	static
    {
		tableKeys = new HashMap<String, String[]>();
		//TODO Add all the tables and their primary keys here
    }
	
	/**
	 * Creates a new entry in the database in the supplied table with the 
	 * supplied values.
	 * 
	 * @param entityName The type of object (Player, Friend, etc.)
	 * @param values The values to insert into the table.
	 * @return A system code describing the success or failure of the operation.
	 */
	public static SystemCode createEntity(String entityName, Object[] values) {
		//TODO Double-check this code for functionality
		String createString = "insert into " + entityName + " values (";
		for(int i = 0; i < values.length; i++) {
			createString += values[i].toString();
			if ( (i+1) < values.length ) {
				createString += ", ";
			}
		}
		createString += ");";
		
		try {
			PreparedStatement createStatement = con.prepareStatement(createString);
			createStatement.execute();
			return SystemCode.SUCCESS;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return SystemCode.FAILURE;
		}
	}
	
	/**
	 * Grabs players from the database and converts values from the resultSet 
	 * into an array of objects. Parameters subject to change in the future.
	 *  
	 * @param options A list of parameters used to filter/sort the results
	 * @return An array of an array of values pertaining to player entries
	 * in the database that match the search terms provided in the options
	 * array. If no entries are found, it will return an empty array. If there
	 * was an error processing the request, it will return null.
	 */
	public static Object[][] retrievePlayers(Object[] options) {
		String commandString = "select * from player";
		String[][] stringsToReturn = null;
		if (options.length > 0) {
			commandString += " where";	
			for (int i = 0; i < options.length; i++) {
				commandString += options[i].toString();
				if ( (i+1) < options.length ) {
					commandString += " and ";
				}
			}
		}
		commandString += ";";
		try {
			PreparedStatement commandStatement = con.prepareStatement(commandString);
			commandStatement.execute();
			ResultSet rs = commandStatement.getResultSet();           //TODO::Refactor this into a resultSet to Ovject[][] method
			rs.last();
			int rsRows = rs.getRow() - 1;
			int rsCols = rs.getMetaData().getColumnCount();
			stringsToReturn = new String[rsRows][rsCols];
			
			rs.beforeFirst();
			for (int currentRow = 0; currentRow < rsRows; currentRow++) {
				rs.next();
				for (int currentColumn = 0; currentColumn < rsCols; currentColumn++) {
					stringsToReturn[currentRow][currentColumn] = rs.getString(currentColumn); 
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return stringsToReturn;
	}
	
	/**
	 * Grabs players from the database and converts values from the resultSet 
	 * into an array of objects. Parameters subject to change in the future.
	 *  
	 * @param options A list of parameters used to filter/sort the results
	 * @return An array of an array of values pertaining to player entries
	 * in the database that match the search terms provided in the options
	 * array. If no entries are found, it will return an empty array. If there
	 * was an error processing the request, it will return null.
	 */
	public static Object[][] retrieveFriends(Object[] options) {
		String command = "select * from friend";
		if (options.length > 0) {
			command += " where";
			
			for (int i = 0; i < options.length; i++) {
				command += options[i].toString();
				if ( (i+1) < options.length ) {
					command += " and ";
				}
			}
		}
		command += ";";
		return null;
	}
	
	/**
	 * Grabs players from the database and converts values from the resultSet 
	 * into an array of objects. Parameters subject to change in the future.
	 *  
	 * @param options A list of parameters used to filter/sort the results
	 * @return An array of an array of values pertaining to player entries
	 * in the database that match the search terms provided in the options
	 * array. If no entries are found, it will return an empty array. If there
	 * was an error processing the request, it will return null.
	 */
	public static Object[][] retrieveApplications(Object[] options) {
		return null;
	}
	
	/**
	 * Grabs players from the database and converts values from the resultSet 
	 * into an array of objects. Parameters subject to change in the future.
	 *  
	 * @param options A list of parameters used to filter/sort the results
	 * @return An array of an array of values pertaining to player entries
	 * in the database that match the search terms provided in the options
	 * array. If no entries are found, it will return an empty array. If there
	 * was an error processing the request, it will return null.
	 */
	public static Object[][] retrieveAchievements(Object[] options) {
		return null;
	}
	
	/**
	 * Grabs players from the database and converts values from the resultSet 
	 * into an array of objects. Parameters subject to change in the future.
	 *  
	 * @param steamID The steamID of a player whose applications to retrieve
	 * @param options A list of parameters used to filter/sort the results
	 * @return An array of an array of values pertaining to player entries
	 * in the database that match the search terms provided in the options
	 * array. If no entries are found, it will return an empty array. If there
	 * was an error processing the request, it will return null.
	 */
	public static Object[][] retrieveOwnedAchievements
								(long steamID, Object[] options) {
		return null;
	}
	
	/**
	 * Grabs the owned applications for the specified steamID from the database 
	 * and converts values from the resultSet into an array of objects.
	 * Parameters subject to change in the future.
	 *  
	 * @param steamID The steamID of a player whose applications to retrieve
	 * @param options A list of parameters used to filter/sort the results
	 * @return An array of an array of values pertaining to player entries
	 * in the database that match the search terms provided in the options
	 * array. If no entries are found, it will return an empty array. If there
	 * was an error processing the request, it will return null.
	 */
	public static Object[][] retrieveOwnedApplications
								(long steamID, Object[] options) {
		return null;
	}
	
	/**
	 * Updates the entry in the given table for the given entity. All primary
	 * keys must be included in the values array in order to figure out what
	 * entity to update.
	 * 
	 * @param entityName The type of object (Player, Friend, etc.)
	 * @param values The values to update the table with.
	 * @return A system code describing the success or failure of the operation.
	 */
	public static SystemCode updateEntity(String entityName, Object[] values) {
		return SystemCode.FAILURE;
	}
	
	/**
	 * Deletes the entry in the given table for the given entity. All primary
	 * keys must be included in the values array in order to figure out what
	 * entity to delete.
	 * 
	 * @param entityName The type of object (Player, Friend, etc.)
	 * @param primaryKeys The primary keys used to uniquely identify an object.
	 * @return A system code describing the success or failure of the operation.
	 */
	public static SystemCode deleteEntity
								(String entityName, Object[] primaryKeys) {
		return SystemCode.FAILURE;
	}

	

}
