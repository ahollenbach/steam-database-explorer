/**
 *  Main project file
 *  
 *  @author Andrew Hollenbach <anh7216@rit.edu>
 *  @author Andrew DeVoe <ard5852@rit.edu>
 * 
 * 
 * 
 * 
 */

package steam.dbexplorer;

import java.sql.DriverManager;
import java.sql.*;
import java.util.List;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.steam.community.GameAchievement;
import com.github.koraktor.steamcondenser.steam.community.GameStats;
import com.github.koraktor.steamcondenser.steam.community.SteamId;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*SteamId id;
		try {
			//basic steam API code, TEST
			id = SteamId.create("demomenz");
			GameStats stats = id.getGameStats("tf2");
			List<GameAchievement> achievements = stats.getAchievements();
			System.out.println("Hey!");
			int x = 0;
		} catch (SteamCondenserException e) {
			e.printStackTrace();
		} */
		try {
			Connection con;
			Statement stmt;
			String url = "jdbc:postgresql://reddwarf.cs.rit.edu/p48501c";
			Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection(url, "p48501c","moovaeheequeingo");
			
			stmt = con.createStatement();
			
			stmt.execute("select * from player;");
			System.out.println(stmt.getResultSet().getMetaData().getColumnCount());
			
			
			stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
		System.out.println("Test complete");
		
		
	}

}
