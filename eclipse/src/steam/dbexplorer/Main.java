package steam.dbexplorer;

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
		SteamId id;
		try {
			//basic steam API code, TEST
			id = SteamId.create("demomenz");
			GameStats stats = id.getGameStats("tf2");
			List<GameAchievement> achievements = stats.getAchievements();
			System.out.println("Hey!");
			int x = 0;
		} catch (SteamCondenserException e) {
			e.printStackTrace();
		}
		
	}

}
