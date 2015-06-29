package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.pluginmessages.SendPlayerIgnores;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IgnoresManager {

	public static void LoadPlayersIgnores(GSPlayer p) throws SQLException{
		List<String> ignores = DatabaseManager.ignores.getIgnores(p.getUuid());
		for (String ignoring : ignores){
			p.addIgnore(ignoring);
		}
	}

	public static void addIgnore(GSPlayer p, String ignore) throws SQLException {
		GSPlayer target = PlayerManager.matchOnlinePlayer(ignore);
		String ignoreUUID;
		if (target != null) {
			ignoreUUID = target.getUuid();
		} else {
			ignoreUUID = Utilities.getUUID(ignore);
		}
		if (PlayerManager.playerUserNameExists(ignore)) {
			if (!p.isIgnoring(ignore)) {
				p.addIgnore(ignore);
				DatabaseManager.ignores.addIgnore(p.getUuid(), ignoreUUID);
				p.sendMessage(ConfigManager.messages.PLAYER_IGNORED.replace("{player}", ignore));
			} else {
				removeIgnore(p, ignore);
			}
		} else {
			p.sendMessage(ConfigManager.messages.PLAYER_DOES_NOT_EXIST);
		}
		SendPlayerIgnores.execute(p, p.getProxiedPlayer().getServer().getInfo());
	}

	public static void removeIgnore(GSPlayer p, String ignore) throws SQLException{
		GSPlayer target = PlayerManager.matchOnlinePlayer(ignore);
		String ignoreUUID;
		if (target != null) {
			ignoreUUID = target.getUuid();
		} else {
			ignoreUUID = Utilities.getUUID(ignore);
		}
		if(p.isIgnoring(ignore)){
			p.removeIgnore(ignore);
			DatabaseManager.ignores.removeIgnore(p.getUuid(), ignoreUUID);
			p.sendMessage(ConfigManager.messages.PLAYER_UNIGNORED.replace("{player}", ignore));
		}else{
			p.sendMessage(ConfigManager.messages.PLAYER_NOT_IGNORED.replace("{player}", ignore));
		}
		SendPlayerIgnores.execute(p, p.getProxiedPlayer().getServer().getInfo());
	}

	public static boolean playerHasIgnores(GSPlayer p){
		return p.hasIgnores();
	}

	public static Collection<GSPlayer> getPlayersIgnorers(String player) {
		Collection<GSPlayer> players = new ArrayList<GSPlayer>();
		for(GSPlayer p: PlayerManager.getPlayers()){
			if(p.hasIgnores()){
				if(p.isIgnoring(player)){
					players.add(p);
				}
			}
		}
		return players;
	}

}