/**
 * This is a reference class to the DDL. Every attribute is in the order that
 * it is in the database. A Disp ending means that the attributes are in the
 * format they are displayed in to the user.
 * 
 * @author Andrew Hollenbach <ahollenbach>
 */

package steam.dbexplorer.dbobject;

public class DBReference {
	public static final String[] PlayerDisp = {"Steam ID", "Persona Name", "Profile URL", "Real Name", "Date Joined"};
	public static final String[] FriendDisp = {"Steam ID #1", "Steam ID #2"};
	public static final String[] ApplicationDisp = {"Application ID", "Application Name"};
	public static final String[] AchievementDisp = {"Application ID", "Achievement Name"};
	public static final String[] OwnedAchievementDisp = {"Application ID", "Achievement Name", "Steam ID"};
	public static final String[] OwnedApplicationDisp = {"Application ID", "Steam ID"};
	
	//for a "using" clause
	public static final String PlayerTables = "";
	public static final String FriendTables = "Player";
	public static final String ApplicationTables = "";
	public static final String AchievementTables = "Application";
	public static final String OwnedAchievementTables = "Application,Player";
	public static final String OwnedApplicationTables = "Application,Player";
}
