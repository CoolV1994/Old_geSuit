package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.Channel;
import net.cubespace.geSuit.tasks.SendPluginMessage;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Vinnie on 6/28/2015.
 */
public class SendChannel {
	public static String OUTGOING_CHANNEL = "geSuitChat";

	public static void execute(ServerInfo server, Channel channel) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("SendChannel");
			out.writeUTF(channel.serialise());
		} catch (IOException e) {
			e.printStackTrace();
		}
		geSuit.proxy.getScheduler().runAsync(geSuit.instance, new SendPluginMessage(OUTGOING_CHANNEL, server, bytes));
	}
}
