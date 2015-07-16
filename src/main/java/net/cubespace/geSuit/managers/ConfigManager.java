package net.cubespace.geSuit.managers;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.configs.*;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class ConfigManager {
	public static Messages messages = new Messages();
	public static Announcements announcements = new Announcements();
	public static BansConfig bans = new BansConfig();
	public static Channels channels = new Channels();
	public static ChatConfig chat = new ChatConfig();
	public static MainConfig main = new MainConfig();
	public static SpawnConfig spawn = new SpawnConfig();
	public static TeleportConfig teleport = new TeleportConfig();
	public static MOTDFile motd = new MOTDFile("motd.txt");
	public static MOTDFile motdNew = new MOTDFile("motd-new.txt");

	static {
		try {
			messages.init();
			announcements.init();
			bans.init();
			channels.init();
			chat.init();
			main.init();
			spawn.init();
			teleport.init();
			motd.init();
			motdNew.init();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
}
