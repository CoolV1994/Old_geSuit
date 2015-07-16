package net.cubespace.geSuit.configs;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.geSuit.configs.SubConfig.ServerChannel;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;

import java.io.File;
import java.util.HashMap;

public class Channels extends Config {
	public Channels() {
		CONFIG_FILE = new File(geSuit.instance.getDataFolder(), "channels.yml");
	}

	public String Global = ConfigManager.messages.CHANNEL_DEFAULT_GLOBAL;
	public String Admin = ConfigManager.messages.CHANNEL_DEFAULT_ADMIN;
	public String Faction = ConfigManager.messages.CHANNEL_DEFAULT_FACTION;
	public String FactionAlly = ConfigManager.messages.CHANNEL_DEFAULT_FACTION_ALLY;
	public String TownyTown = ConfigManager.messages.CHANNEL_DEFAULT_TOWN;
	public String TownyNation = ConfigManager.messages.CHANNEL_DEFAULT_NATION;

	public HashMap<String, ServerChannel> Servers = new HashMap<>();
}