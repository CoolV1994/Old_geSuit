package net.cubespace.geSuit.configs;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.configs.SubConfig.ServerChannel;

import java.io.File;
import java.util.HashMap;

public class Channels  extends Config {
	public Channels() {
		CONFIG_FILE = new File(geSuit.instance.getDataFolder(), "channels.yml");
	}

	public String Global = "&e[{server}]&r{permprefix}{player}{permsuffix}&f: {message}";
	public String Admin = "&c[Admin]&r{player}&f: {message}";
	public String Faction = "&a{factions_roleprefix}{factions_title} {player}:&r {message}";
	public String FactionAlly = "&d{factions_roleprefix}{factions_name} {player}:&r {message}";
	public String TownyTown = "&f[&3TC&f]{townytitle}{player} &f{townysurname}{permsuffix}&f: {message}";
	public String TownyNation = "&f[&6NC&f]{townytown}{townytitle}{player}&f{townysurname}{permsuffix}&f: {message}";

	public HashMap<String, ServerChannel> Servers = new HashMap<>();
}