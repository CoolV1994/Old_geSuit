package net.cubespace.geSuit.configs;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.geSuit.configs.SubConfig.GeoIPSettings;
import net.cubespace.geSuit.geSuit;

import java.io.File;
import java.util.HashMap;

public class BansConfig extends Config {
	public BansConfig() {
		CONFIG_FILE = new File(geSuit.instance.getDataFolder(), "bans.yml");
	}

	public Boolean Enabled = true;
	public Boolean BroadcastBans = true;
	public Boolean BroadcastUnbans = false;
	public Boolean BroadcastWarns = true;
	public Boolean BroadcastKicks = true;
	public Boolean ShowAltAccounts = true;
	public Boolean ShowBannedAltAccounts = true;
	public Boolean TrackOnTime = true;
	public int NameChangeNotifyTime = 20;

	public int WarningExpiryDays = 180;

	public HashMap<Integer, String> Actions = new HashMap<Integer, String>();

	public GeoIPSettings GeoIP = new GeoIPSettings();
}
