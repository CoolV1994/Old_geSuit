package net.cubespace.geSuit.configs;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.geSuit.geSuit;

import java.io.File;
import java.util.HashMap;

public class ChatConfig extends Config {
	public ChatConfig() {
		CONFIG_FILE = new File(geSuit.instance.getDataFolder(), "chat.yml");
	}

	public boolean logChat = true;
	public boolean stripChat = true;
	public boolean mutePrivateMessages = true;
	public int nickNameLimit = 16;
	public boolean updateNicknamesOnTab = true;
	public String globalChatRegex = "\\{(factions_.*?)\\}";
	public String defaultChannel = "Global";

	public HashMap<String, String> prefixes = new HashMap<String, String>()
	{{
			put("default", "");
	}};
	public HashMap<String, String> suffixes = new HashMap<String, String>()
	{{
			put("default", "");
	}};
}