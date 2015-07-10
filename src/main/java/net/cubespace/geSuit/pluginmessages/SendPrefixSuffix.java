package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.tasks.SendPluginMessage;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Vinnie on 6/27/2015.
 */
public class SendPrefixSuffix {
	public static String OUTGOING_CHANNEL = "geSuitChat";

	private static void send(boolean prefix, String group, String affix, ServerInfo server) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);

		out.writeUTF("PrefixesAndSuffixes");
		out.writeBoolean(prefix);
		out.writeUTF(group);
		out.writeUTF(affix);

		geSuit.proxy.getScheduler().runAsync(geSuit.instance, new SendPluginMessage(OUTGOING_CHANNEL, server, bytes));
	}

	public static void execute(ServerInfo server) throws IOException {
		for (Map.Entry<String, String> prefix : ConfigManager.chat.prefixes.entrySet()) {
			send(true, prefix.getKey(), prefix.getValue(), server);
		}
		for (Map.Entry<String, String> suffix : ConfigManager.chat.suffixes.entrySet()) {
			send(false, suffix.getKey(), suffix.getValue(), server);
		}
	}
}
