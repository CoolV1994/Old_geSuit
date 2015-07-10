package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.configs.SubConfig.ServerChannel;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ChatManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.tasks.SendPluginMessage;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Vinnie on 6/28/2015.
 */
public class SendServerChannelData {
	public static String OUTGOING_CHANNEL = "geSuitChat";

	public static void execute(ServerInfo server) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("SendServerData");
			ServerChannel sd = ChatManager.serverData.get(server.getName());
			out.writeUTF(sd.serverName);
			out.writeUTF(sd.shortName);
			out.writeInt(sd.localDistance);
			out.writeBoolean(sd.connectionMessages);
			out.writeUTF(ConfigManager.chat.globalChatRegex);
		} catch (IOException e) {
			e.printStackTrace();
		}

		geSuit.proxy.getScheduler().runAsync(geSuit.instance, new SendPluginMessage(OUTGOING_CHANNEL, server, bytes));
	}
}
