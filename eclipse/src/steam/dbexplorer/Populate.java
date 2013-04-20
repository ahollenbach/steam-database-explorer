/**
 * The populate class drops all data from the tables and inserts new test 
 * values. This class can be used for testing, because it will return the 
 * database to a specific state specified by its methods.
 * 
 * @author Andrew Hollenbach <ahollenbach>
 */

package steam.dbexplorer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.koraktor.steamcondenser.exceptions.WebApiException;
import com.github.koraktor.steamcondenser.steam.community.SteamGame;
import com.github.koraktor.steamcondenser.steam.community.SteamId;
import com.github.koraktor.steamcondenser.steam.community.WebApi;
import com.github.koraktor.steamcondenser.steam.servers.MasterServer;

public class Populate {
	
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
		
		@Override
		public int hashCode() {
			return (int) (this.steamId1 * this.steamId2);
		}

		@Override
		public boolean equals(Object obj) {
			if (!Pair.class.isInstance(obj)) {
				return false;
			}
			else {
				Pair other = (Pair) obj;
				if ((this.steamId1 == other.steamId1) && (this.steamId2 == other.steamId2)) {
					return true;
				}
				if ((this.steamId1 == other.steamId2) && (this.steamId2 == other.steamId1)) {
					return true;
				}
				return false;
			}
		}
	}
	
	public static Set<Pair> populatePlayers(int recursive, Connection con, Long longId) {
		if (recursive == BRANCH) {
			return new HashSet<Pair>();
		}
		else {
			try {
				String selectPlayerString = "select count(steamId) as playerExists from player where steamId = ?;";
				PreparedStatement selectPlayerStatement = con.prepareStatement(selectPlayerString);
				
				String insertPlayerString = "insert into player values (?, ?, ?, ?, ?, ?);";
				PreparedStatement insertPlayerStatement = con.prepareStatement(insertPlayerString);
				
				String insertAppString = "insert into OwnedApplication values (?, ?);";
				PreparedStatement insertAppStatement = con.prepareStatement(insertAppString);
				
				String insertFriendString = "insert into friend values (?, ?);";
				PreparedStatement insertFriendStatement = con.prepareStatement(insertFriendString);
				
				SteamId id;
				List<SteamId> friends = new ArrayList<SteamId>();
				Map<Integer, SteamGame> games = new HashMap<Integer, SteamGame>();
				Set<Pair> friendsToAdd = new HashSet<Pair>();
				try {
					selectPlayerStatement.setLong(1, longId);
					selectPlayerStatement.execute();
					ResultSet rs = selectPlayerStatement.getResultSet();
					rs.next();
					if (rs.getInt("PlayerExists") > 0) {
						return friendsToAdd; //We already called populatePlayer on this person
					}
					id = SteamId.create(longId, true);
					insertPlayerStatement.setLong(1, id.getSteamId64());
					insertPlayerStatement.setString(2, id.getNickname());
					if (id.getCustomUrl() != null) {
						insertPlayerStatement.setString(3, id.getCustomUrl());
					}
					else {
						insertPlayerStatement.setString(3, id.getBaseUrl());
					}
					insertPlayerStatement.setString(4, id.getRealName());
					if (id.getMemberSince() != null) {
						insertPlayerStatement.setDate(5, new Date(id.getMemberSince().getTime()));
					}
					else {
						insertPlayerStatement.setDate(5, null);
					}
					insertPlayerStatement.setDate(6,null);
					insertPlayerStatement.execute();
					
					try {
						if (id.getVisibilityState() == VISIBLE) {
							friends = id.getFriends();
							games = id.getGames();
						}
						for (Iterator<Entry<Integer,SteamGame>> it = games.entrySet().iterator(); it.hasNext();) {
							try {
								Entry<Integer,SteamGame> owned = it.next();
								insertAppStatement.setLong(1, owned.getKey());
								insertAppStatement.setLong(2, id.getSteamId64());
								insertAppStatement.execute();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						for (SteamId temp:friends) {
							try {
								friendsToAdd.addAll((populatePlayers(recursive+1, con, temp.getSteamId64())));	
								friendsToAdd.add(new Pair(id.getSteamId64(), temp.getSteamId64()));
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
							insertFriendStatement.setLong(1, p.steamId1);
							insertFriendStatement.setLong(2, p.steamId2);
							insertFriendStatement.execute();
						} catch (Exception e) {
							//e.printStackTrace();  Errors here are caused due to
							//friends of "leaf nodes" in the graph of players not having their friends added
							//thus they don't exist.  However, can't simply ignore them as they may
							//have friendships to other "leafs:
							System.err.println(e.getMessage());;
						}
					}
					return new HashSet<Pair>();
				}
				else {
					return friendsToAdd;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return new HashSet<Pair>();
			}
		}
	}
	
	public static void populateApps(Connection con) {
		try {			
            JSONObject jsonData = new JSONObject( WebApi.getJSON("ISteamApps", "GetAppList"));
            JSONArray appsData = jsonData.getJSONObject("applist").getJSONObject("apps").getJSONArray("app");
            
            String insertString = "insert into application values ( ?, ?);";
            PreparedStatement updateApp = con.prepareStatement(insertString);
            
            for (int i = 0; i < appsData.length(); i ++) {
                JSONObject app = appsData.getJSONObject(i);
                
                updateApp.setLong(1, Long.parseLong(app.getString("appid")));
                updateApp.setString(2, app.getString("name"));

                try {
                	updateApp.executeUpdate();
                } catch (SQLException e) {
                	e.printStackTrace();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void populate() {
		try {
			WebApi.setApiKey(Credentials.APIKEY);
			
			Connection con;
			Statement stmt;
			String url = Credentials.DATABASEURL;
			Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection(url, Credentials.DATABASEUSERNAME ,Credentials.DATABASEPASSWORD);
			
			stmt = con.createStatement();
			
			populateApps(con);
			
			//stmt.execute("select * from player;");
			//ResultSet rs = stmt.getResultSet();
			//rs.next();
			//Long n = rs.getLong("steamId");
			populatePlayers(0,con,76561197988083973L);
			//76561197988083973  -  Tonbo
			//76561197988128323  -  WispingWinds
			//76561198018660341  -  DROCK
			
			stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
	}
}
