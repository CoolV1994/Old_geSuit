package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.tasks.SendPluginMessage;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Vinnie on 6/28/2015.
 */
public class SendPlayerIgnores {
	public static String OUTGOING_CHANNEL = "geSuitChat";

	public static void execute(GSPlayer player, ServerInfo server) {
		if (player.hasIgnores()) {
			String ignores = "";
			for (String str : player.getIgnores()) {
				ignores += str + "%";
			}
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(bytes);
			try {
				out.writeUTF("SendPlayersIgnores");
				out.writeUTF(player.getName());
				out.writeUTF(ignores);
			} catch (IOException e) {
				e.printStackTrace();
			}
			geSuit.proxy.getScheduler().runAsync(geSuit.instance, new SendPluginMessage(OUTGOING_CHANNEL, server, bytes));
		}
	}
}
