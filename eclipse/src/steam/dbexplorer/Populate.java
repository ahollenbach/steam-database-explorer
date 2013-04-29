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
	
	public static Set<Pair> populatePlayers(int recursive, Connection con, Long longId, List<Integer> appsAdded) {
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
				
				String insertAchievementString = "insert into OwnedAchievement values (?, ?, ?);";
				PreparedStatement insertAchievementStatement = con.prepareStatement(insertAchievementString);
				
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
								if (appsAdded.contains(owned.getKey())) {
									
									
									try {			
							            Map<String, Object> param = new HashMap<String,Object>();
										param.put("appid", owned.getKey());
										param.put("steamid", id.getSteamId64());
										param.put("format", "json");
							            JSONObject jsonData = new JSONObject( WebApi.getJSON("ISteamUserStats", "GetPlayerAchievements", 1, param));
							            JSONArray achievement = jsonData.getJSONObject("playerstats").getJSONArray("achievements");
							            
							            
							            for (int i = 0; i < achievement.length(); i ++) {
							            	try {
								                JSONObject ach = achievement.getJSONObject(i);
								                if (ach.getInt("achieved") == 1) {
								                	insertAchievementStatement.setLong(1, owned.getKey());
								                	insertAchievementStatement.setString(2, ach.getString("apiname"));
								                	insertAchievementStatement.setLong(3, id.getSteamId64());
								                	insertAchievementStatement.execute();
								                }
							                } catch (SQLException e) {
							                	e.printStackTrace();
							                }
							            }
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						for (SteamId temp:friends) {
							try {
								friendsToAdd.addAll((populatePlayers(recursive+1, con, temp.getSteamId64(), appsAdded)));	
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
	
	public static List<Long> populateApps(Connection con) {
		List<Long> appsAdded = new ArrayList<Long>();
		try {			
            JSONObject jsonData = new JSONObject( WebApi.getJSON("ISteamApps", "GetAppList"));
            JSONArray appsData = jsonData.getJSONObject("applist").getJSONObject("apps").getJSONArray("app");
            
            String insertString = "insert into application values ( ?, ?);";
            PreparedStatement insertStatement = con.prepareStatement(insertString);
            
            for (int i = 0; i < appsData.length(); i ++) {
                JSONObject app = appsData.getJSONObject(i);
                
                insertStatement.setLong(1, Long.parseLong(app.getString("appid")));
                insertStatement.setString(2, app.getString("name"));

                try {
                	insertStatement.execute();
                	appsAdded.add(Long.parseLong(app.getString("appid")));
                } catch (SQLException e) {
                	e.printStackTrace();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
		return appsAdded;
	}
	
	public static void populateAchievements(Connection con, List<Integer> apps) {
		try {			
			for (int currentApp = 0; currentApp < apps.size(); currentApp++) {
				Map<String, Object> param = new HashMap<String,Object>();
				param.put("gameid", apps.get(currentApp));
				param.put("format", "json");
	            JSONObject jsonData = new JSONObject( WebApi.getJSON("ISteamUserStats", "GetGlobalAchievementPercentagesForApp", 1, param));
	            JSONArray achievement = jsonData.getJSONObject("achievementpercentages").getJSONObject("achievements").getJSONArray("achievement");
	            
	            String insertString = "insert into achievement values ( ?, ?);";
	            PreparedStatement insertAchivement = con.prepareStatement(insertString);
	            
	            for (int i = 0; i < achievement.length(); i ++) {
	                try {
		                JSONObject ach = achievement.getJSONObject(i);
		                insertAchivement.setLong(1, apps.get(currentApp));
		                insertAchivement.setString(2, ach.getString("name"));

	                	insertAchivement.execute();
	                } catch (Exception e) {
	                	e.printStackTrace();
	                }
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
			String url = Credentials.DATABASEURL;
			Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection(url, Credentials.DATABASEUSERNAME ,Credentials.DATABASEPASSWORD);
			
			List<Integer> appsAdded = new ArrayList<Integer>();
			
			//appsAdded.addAll(populateApps(con));  Is what we would do if we wanted to get all of the achievements on Steam
			populateApps(con);
			appsAdded.add(70);    //Half Life (has no achievements)
			appsAdded.add(220);   //Half Life 2
			appsAdded.add(240);   //Counter Strike: Source
			appsAdded.add(260);   //Counter Strike: Source Beta
			appsAdded.add(300);   //Day of Defeat: Source
			appsAdded.add(440);   //Team Fortress 2
			appsAdded.add(550);   //Left 4 Dead 2
			appsAdded.add(570);   //Dota 2  (has no achievements)
			appsAdded.add(1250);  //Killing Floor
			appsAdded.add(221380);//Age of Empires II: HD Edition 
			
			populateAchievements(con, appsAdded);
			
			populatePlayers(0,con,76561197988083973L, appsAdded);
			//76561197988083973  -  Tonbo
			//76561197988128323  -  WispingWinds
			//76561198018660341  -  DROCK
			
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
	}
}
