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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.steam.community.GameAchievement;
import com.github.koraktor.steamcondenser.steam.community.GameStats;
import com.github.koraktor.steamcondenser.steam.community.SteamGame;
import com.github.koraktor.steamcondenser.steam.community.SteamId;
import com.github.koraktor.steamcondenser.steam.community.WebApi;

public class Main {
	
	private final static int BRANCH = 3; //The produced friendship tree has a radius of BRANCH-1;
	//Thus a BRANCH of 1 gets exactly one person (The center).
	//  2 gets the center and the centers friends
	//	3 gets the center, their friends, and their friends of friends
	
	
	public static HashMap<Long,Long> populatePlayers(int recursive, Statement stmt, Long longId) {
		if (recursive == BRANCH) {
			return new HashMap<Long, Long>();
		}
		else {
			SteamId id;
			List<SteamId> friends;
			HashMap<Long, Long> friendsToAdd = new HashMap<Long, Long> ();
			try {
				//basic steam API code, TEST
				stmt.execute("select count(steamId) as playerExists from player where steamId = "+longId+";");
				ResultSet rs = stmt.getResultSet();
				rs.next();
				if (rs.getInt("PlayerExists") > 0) {
					return new HashMap<Long, Long> (); //We already called populatePlayer on this person
				}
				id = SteamId.create(longId, true);
				stmt.executeUpdate("insert into player values ("+id.getSteamId64()+", '"+id.getNickname()+"', '"+id.getCustomUrl()+"', null, null, null);");
				//System.out.println(id.getNickname());
				//System.out.println(id.getMemberSince());
				try {
					friends = id.getFriends();
					//System.out.println("Size:"+friends.size());
					//GameStats stats = id.getGameStats("tf2");
					//List<GameAchievement> achievements = stats.getAchievements();
					for (SteamId temp:friends) {
						try {
							//System.out.println (temp.getSteamId64());
							//SteamId f = SteamId.create(temp.getSteamId64(), true) ;
							//System.out.println("insert into player values ("+f.getSteamId64()+", '"+f.getNickname()+"', '"+f.getCustomUrl()+"', null, null, null);");
							//stmt.executeUpdate("insert into player values ("+f.getSteamId64()+", '"+f.getNickname()+"', '"+f.getCustomUrl()+"', null, null, null);");
							if ((recursive+1) != BRANCH) {
								friendsToAdd.putAll(populatePlayers(recursive+1, stmt, temp.getSteamId64()));					
							}
							
							//System.out.println("insert into friend values ("+id.getSteamId64()+", "+f.getSteamId64()+");");
							if ((!(friendsToAdd.containsKey(id.getSteamId64()) && friendsToAdd.containsValue(temp.getSteamId64())) || 
								!(friendsToAdd.containsValue(id.getSteamId64()) && friendsToAdd.containsKey(temp.getSteamId64()))) ) {
								friendsToAdd.put(id.getSteamId64(), temp.getSteamId64());
							}
							//stmt.executeUpdate("insert into friend values ("+id.getSteamId64()+", "+temp.getSteamId64()+");");
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
				for (Iterator<Long> it = friendsToAdd.keySet().iterator(); it.hasNext();) {
					Long steamId1 = it.next();
					Long steamId2 = friendsToAdd.get(steamId1);
					try {
						stmt.executeUpdate("insert into friend values ("+steamId1+", "+steamId2+");");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return new HashMap<Long,Long>();
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
			populatePlayers(0,stmt,76561197988128323L);
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
