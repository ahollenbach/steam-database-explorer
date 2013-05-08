/**
 * The populate class creates and populates the tables with all of the required data
 * values.  The populate method is coded to be able to get a variable amount of data.
 * 
 *  @author Andrew Hollenbach <anh7216@rit.edu>
 *  @author Andrew DeVoe <ard5852@rit.edu>
 *  
 */

package steam.dbexplorer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.koraktor.steamcondenser.steam.community.SteamGame;
import com.github.koraktor.steamcondenser.steam.community.SteamId;
import com.github.koraktor.steamcondenser.steam.community.WebApi;

public class Populate {
	
	private final static int BRANCH = 2; //The produced friendship graph has a radius of BRANCH-1;
	//Thus a BRANCH of 1 gets exactly one person (The center).
	//  2 gets the center and the centers friends
	//	3 gets the center, their friends, and their friends of friends
	
	private final static int VISIBLE = 3; //community visiblity state constants
	private final static int PRIVATE = 1;

	/**
	 * Pair is used to insert friendships into the database.
	 * It is set up such that a Pair object is equal to another
	 * Pair object if its two contained Id's are equal or
	 * if they are the reverse.  Thus the follow sets are the
	 * same.
	 * (1 , 2) = ( 2 , 1 )
	 * This class was made for use with a HashedSet
	 */
	public static class Pair {
		long steamId1;
		long steamId2;
		
		/**
		 * Constructs a pair with the given values.
		 * 
		 * @param l1 The first steamId
		 * @param l2 The second steamId
		 */
		public Pair (long l1, long l2) {
			steamId1 = l1;
			steamId2 = l2;
		}
		
		/**
		 * Returns a hashed code for the pair.  It is based on
		 * the two values of the pair contains.  It is made
		 * so that a pair which is equal also has the same has
		 * code (as required by Java Standards)
		 * 
		 * @return The hashed code for the pair
		 */
		@Override
		public int hashCode() {
			return (int) (this.steamId1 * this.steamId2);
		}

		/**
		 * Returns whether an object is equal to this Pair object.
		 * Two Pair objects are equal if they contain the same
		 * two values in any order.
		 * 
		 * @param obj The object to compare to
		 * @return Whether this object and obj are equal
		 */
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
	
	/**
	 * Populates the Player table.  Goes about this task recursively.  
	 * Continues looking up the steamId provided as longId until the recursive reaches the
	 * branching factor then stops.  For each person, looks up the users information and stores the
	 * person, from there looks up their owned games and adds them, and then looks up the apps listed in
	 * appsAdded for the achievements to look for.  Finally looks up the players friends and returns them when
	 * done.  The intital person (recursive = 0) then adds all of the friend relationships.  This ensures that
	 * no friendships are missing due to a person having not yet been added to the database.
	 * 
	 * This function can (and will) throw exceptions, as many people whom have friendships referencing them
	 * will not be in the database.
	 * 
	 * @param recursive The recursion factor; stops when this reaches the branching factor
	 * @param con The connection to the database to add the players too
	 * @param longId The ID of the player to look up
	 * @param appsAdded The application to look up owned achievements for
	 * @return The set of friendships to attempt to add to the database.  May return an empty set
	 */
	public static Set<Pair> populatePlayers(int recursive, Connection con, Long longId, List<Integer> appsAdded) {
		if (recursive == BRANCH) {
			return new HashSet<Pair>();
		}
		else {
			try {
				String selectPlayerString = "select count(steamId) as playerExists from player where steamId = ?;";
				PreparedStatement selectPlayerStatement = con.prepareStatement(selectPlayerString);
				
				String insertPlayerString = "insert into player values (?, ?, ?, ?, ?);";
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
				} catch (SQLException e) {
		            e.printStackTrace();
				}
				
				try {
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
							//Errors here are caused due to
							//friends of "leaf nodes" in the graph of players not having their friends added
							//thus they don't exist.  However, can't simply ignore them as they may
							//have friendships to other "leafs:
							e.printStackTrace();
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
	
	/**
	 * Populates the Application table.  
	 * 
	 * @param con The connection to the database to add data too.
	 * @return The total list of applications added.  Could be used to act
	 *     As the list of applications to check for achievements.
	 */
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
	
	/**
	 * Populates the Achievement table with achievements from the list of applicationId's
	 * stored in apps.
	 * 
	 * @param con  THe connection to the database to add achievements too
	 * @param apps The list of applicationId to look up achievements for
	 */
	public static void populateAchievements(Connection con, List<Integer> apps) {
		try {		
            String insertString = "insert into achievement values ( ?, ?);";
            PreparedStatement insertAchivement = con.prepareStatement(insertString);
            
			for (int currentApp = 0; currentApp < apps.size(); currentApp++) {
				Map<String, Object> param = new HashMap<String,Object>();
				param.put("gameid", apps.get(currentApp));
				param.put("format", "json");
	            JSONObject jsonData = new JSONObject( WebApi.getJSON("ISteamUserStats", "GetGlobalAchievementPercentagesForApp", 1, param));
	            JSONArray achievement = jsonData.getJSONObject("achievementpercentages").getJSONObject("achievements").getJSONArray("achievement");
	            
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
	
	/**
	 * Populates the database connected to by con with the required tables to fill with
	 * SteamDatabaseExplorer data.  If these tables already exist, they are first dropped.
	 * 
	 * @param con
	 */
	public static void populateTables(Connection con){
		String drop = "drop table if exists Player cascade;"
		+ " drop table if exists Friend cascade;"
		+ " drop function if exists symmetric_i() cascade;"
		+ " drop function if exists symmetric_d() cascade;"
		+ " drop table if exists Application cascade;"
		+ " drop table if exists Achievement cascade;"
		+ " drop table if exists OwnedAchievement cascade;"
		+ " drop table if exists OwnedApplication cascade;";
		
		String createPlayerTable = "create table Player ("
		+ "steamId				bigint,"
		+ "personaName			varchar(32) not null,"	
		+ "profileUrl			varchar(128) not null,"
		+ "realName			varchar(64),"
		+ "timeCreated			date,"
		+ "primary key (steamId));";

		String createFriendTable = "create table Friend ("
		+ "steamId1			bigint,"
		+ "steamId2			bigint,"
		+ "primary key (steamId1, steamId2),"
		+ "foreign key (steamId1) references player(steamId)"
        	+ " on delete cascade,"
		+ "foreign key (steamId2) references player(steamId)"
			+ " on delete cascade);";
		
		String createTrigger1 = ""
		+ "create function symmetric_i() returns trigger as $symmetric_i$"
		+ " begin "
		+ " if not new.steamId1 in (select steamId2 from Friend where steamId1 = new.steamId2) "
		+ "	then "
		+ " insert into Friend (steamId1, steamId2) values (new.steamId2, new.steamId1); end if; "
		+ " return null; end; "
		+ " $symmetric_i$ Language plpgsql; "

		+ "create trigger symmetric_insert after insert on Friend"
		+ " for each row execute procedure symmetric_i();";

		String createTrigger2 = ""
		+ "create function symmetric_d() returns trigger as $symmetric_d$"
		+ " begin "
		+ "	delete from Friend "
		+ " where (steamId1 = old.steamId2) and (steamId2 = old.steamId1);"
        + " return null; end; "
		+ " $symmetric_d$ Language plpgsql;"

		+ " create trigger symmetric_delete after delete on Friend"
		+ " for each row execute procedure symmetric_d();";


		String createApplicationTable = "create table Application ("
		+ "	appId				bigint,"
		+ "	appName				varchar(1024) not null, "
		+ "	primary key (appId));";

		String createAchievementTable = "create table Achievement ("
		+ "	appId				bigint, "
		+ "	achievementName			varchar(1024),"
		+ "	primary key (appId, achievementName),"
		+ "	foreign key (appId) references Application(appId)"
			+ " on delete cascade);";

		String createOwnedAch  = "create table OwnedAchievement ("
		+ "	appId				bigint,"
		+ "	achievementName			varchar(1024),"
		+ "	steamId				bigint,"
		+ "	primary key (appId, achievementName, steamId),"
		+ "	foreign key (appId, achievementName) references Achievement(appId, achievementName)"
			+ "	on delete cascade,"
		+ " foreign key (steamId) references player(steamId)"
			+ " on delete cascade);";

		String createdOwnedApp = "create table OwnedApplication ("
		+ "	appId				bigint,"
		+ "	steamId				bigint,"
		+ "	primary key (appId, steamId),"
		+ "	foreign key (appId) references Application(appId)"
			+ "	on delete cascade,"
		+ "	foreign key (steamId) references player(steamId)"
			+ "	on delete cascade);";
		
		try {
			PreparedStatement dropState = con.prepareStatement(drop);
			PreparedStatement createPlayerTableState = con.prepareStatement(createPlayerTable);
			PreparedStatement createFriendTableState = con.prepareStatement(createFriendTable);
			PreparedStatement createTrigger1State = con.prepareStatement(createTrigger1);
			PreparedStatement createTrigger2State = con.prepareStatement(createTrigger2);
			PreparedStatement createApplicationTableState = con.prepareStatement(createApplicationTable);
			PreparedStatement createAchievementTableState = con.prepareStatement(createAchievementTable);
			PreparedStatement createdOwnedAppState = con.prepareStatement(createdOwnedApp);
			PreparedStatement createOwnedAchState = con.prepareStatement(createOwnedAch);
			
			dropState.execute();
			createPlayerTableState.execute();
			createFriendTableState.execute();
			createTrigger1State.execute();
			createTrigger2State.execute();
			createApplicationTableState.execute();
			createAchievementTableState.execute();
			createdOwnedAppState.execute();
			createOwnedAchState.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Populates the database.  Calls the various populate methods in the following order:
	 * populateTables, populateApps, populateAchievements, populatePlayers.  The order is important due to
	 * foreign key dependencies.
	 * 
	 */
	public static void populate() {
		try {
			WebApi.setApiKey(Credentials.APIKEY);
			Connection con;
			String url = Credentials.DATABASEURL;
			Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection(url, Credentials.DATABASEUSERNAME ,Credentials.DATABASEPASSWORD);
			
			populateTables(con);
			
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
			
			populatePlayers(0, con, 76561198049281288L, appsAdded);
			populatePlayers(0, con, 76561197988083973L, appsAdded);
			//Some potential seed values are:
			//76561197988083973  -  Tonbo
			//76561197988128323  -  WispingWinds
			//76561198018660341  -  DROCK
			//76561198049281288  -  Maddjak
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
