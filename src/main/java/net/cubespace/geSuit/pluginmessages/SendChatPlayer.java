package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ChatManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.tasks.SendPluginMessage;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Vinnie on 6/27/2015.
 */
public class SendChatPlayer {
	public static String OUTGOING_CHANNEL = "geSuitChat";

	public static void execute(String player, ServerInfo server, boolean serverConnect) throws SQLException {
		GSPlayer p = PlayerManager.getPlayer(player);
		if (serverConnect) {
			ChatManager.setPlayerToForcedChannel(p, server);
		}
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeUTF("SendPlayer");
			out.writeUTF(p.getUuid());
			out.writeUTF(p.getName());
			out.writeUTF(p.getChannel());
			out.writeBoolean(p.isMuted());
			out.writeUTF(p.getNickname());
			out.writeUTF(p.getTempName());
			out.writeBoolean(p.isChatSpying());
			out.writeBoolean(p.isDND());
			out.writeBoolean(p.isAFK());

		} catch (IOException e) {
			e.printStackTrace();
		}

		geSuit.proxy.getScheduler().runAsync(geSuit.instance, new SendPluginMessage(OUTGOING_CHANNEL, server, bytes));
	}
}
