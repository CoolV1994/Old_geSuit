package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.tasks.SendPluginMessage;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Vinnie on 6/28/2015.
 */
public class SendAdminChat {
	public static String OUTGOING_CHANNEL = "geSuitChat";

	public static void execute( String message, ServerInfo server ) {
		if ( ConfigManager.chat.logChat ) {
			LoggingManager.log(message);
		}
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream( bytes );
		try {
			out.writeUTF( "SendAdminChat" );
			out.writeUTF( message );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		for ( ServerInfo proxyServer : geSuit.proxy.getServers().values() ) {
			if ( !proxyServer.getName().equals( server.getName() ) && proxyServer.getPlayers().size() > 0 ) {
				geSuit.proxy.getScheduler().runAsync(geSuit.instance, new SendPluginMessage(OUTGOING_CHANNEL, proxyServer, bytes));
			}
		}
	}
}
