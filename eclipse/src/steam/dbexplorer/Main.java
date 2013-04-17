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

import java.awt.print.Printable;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.steam.community.GameAchievement;
import com.github.koraktor.steamcondenser.steam.community.GameStats;
import com.github.koraktor.steamcondenser.steam.community.SteamGame;
import com.github.koraktor.steamcondenser.steam.community.SteamId;
import com.github.koraktor.steamcondenser.steam.community.WebApi;

public class Main {
	
	private final static int BRANCH = 2; //The produced friendship tree has a radius of BRANCH-1;
	//Thus a BRANCH of 1 gets exactly one person (The center).
	//  2 gets the center and the centers friends
	//	3 gets the center, their friends, and their friends of friends
	
	private final static int VISIBLE = 3; //community visiblity state constants
	private final static int PRIVATE = 1;

	public static class Pair {
		long steamId1;
		long steamId2;
		
		public Pair (long l1, long l2) {
			steamId1 = l1;
			steamId2 = l2;
		}
	}
	
	public static Set<Pair> populatePlayers(int recursive, Statement stmt, Long longId) {
		if (recursive == BRANCH) {
			return new HashSet<Pair>();
		}
		else {
			SteamId id;
			List<SteamId> friends = new ArrayList<SteamId>();
			Set<Pair> friendsToAdd = new HashSet<Pair>();
			try {
				//basic steam API code, TEST
				stmt.execute("select count(steamId) as playerExists from player where steamId = "+longId+";");
				ResultSet rs = stmt.getResultSet();
				rs.next();
				if (rs.getInt("PlayerExists") > 0) {
					return new HashSet<Pair>(); //We already called populatePlayer on this person
				}
				id = SteamId.create(longId, true);
				stmt.executeUpdate("insert into player values ("+id.getSteamId64()+", '"+id.getNickname()+"', '"+id.getCustomUrl()+"', null, null, null);");
				try {
					//if (id.getVisibilityState() == VISIBLE) {
						friends = id.getFriends();
					//}
					for (SteamId temp:friends) {
						try {
							//if (((!friendsToAdd.containsKey(id.getSteamId64()) && !friendsToAdd.containsValue(temp.getSteamId64())) && 
							//		(!friendsToAdd.containsValue(id.getSteamId64()) && !friendsToAdd.containsKey(temp.getSteamId64()))) ) {
									friendsToAdd.add(new Pair(id.getSteamId64(), temp.getSteamId64()));
							//	}
							if ((recursive+1) != BRANCH) {
								friendsToAdd.addAll((populatePlayers(recursive+1, stmt, temp.getSteamId64())));					
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (recursive == 0) {
				for (Iterator<Pair> it = friendsToAdd.iterator(); it.hasNext();) {
					Pair p = it.next();
					try {
						stmt.executeUpdate("insert into friend values ("+p.steamId1+", "+p.steamId2+");");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return new HashSet<Pair>();
			}
			else {
				return friendsToAdd;
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			WebApi.setApiKey(Credentials.APIKEY);
			
			Connection con;
			Statement stmt;
			String url = Credentials.DATABASEURL;
			Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection(url, Credentials.DATABASEUSERNAME ,Credentials.DATABASEPASSWORD);
			
			stmt = con.createStatement();
			
			//stmt.execute("select * from player;");
			//ResultSet rs = stmt.getResultSet();
			//rs.next();
			//Long n = rs.getLong("steamId");
			populatePlayers(0,stmt,76561197988083973L);
			//76561197988083973  -  Tonbo
			//76561197988128323  -  WispingWinds
			//76561198018660341  -  DROCK
			
			stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
		System.out.println("Test complete");
	}
}
