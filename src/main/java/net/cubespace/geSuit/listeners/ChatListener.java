package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.managers.ChatManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ChatListener implements Listener {
	public static List<String> BlockedCommands = Arrays.asList( "/l", "/lc", "/localchannel", "/lchannel", "/channellocal", "/s", "/sc", "/serverchannel", "/schannel", "/channelserver", "/g", "/globalchat", "/globalchannel", "/gchannel" );

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerLogin( ServerConnectedEvent e ) throws SQLException {
		//        ChatManager.loadPlayersChannels( e.getPlayer(), e.getServer() );
		GSPlayer p = PlayerManager.getPlayer( e.getPlayer() );
		if ( p != null ) {
			p.updateDisplayName();

		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerLogin( PostLoginEvent e ) throws SQLException {

	}

	@EventHandler
	public void playerChat( ChatEvent e ) throws SQLException {
		GSPlayer p = PlayerManager.getPlayer( e.getSender().toString() );
		if ( p == null ) {
			if ( e.getSender() instanceof ProxiedPlayer ) {
				ProxiedPlayer player = ( ProxiedPlayer ) e.getSender();
				if ( player != null && player.getPendingConnection() != null ) {
					DatabaseManager.players.loadPlayer(player.getUUID());
				} else {
					return;
				}
			} else {
				return;
			}
		}
		if ( e.isCommand() ) {
			if ( BlockedCommands.contains( e.getMessage().split( " " )[0].toLowerCase() ) ) {
				if ( ChatManager.MuteAll ) {
					p.sendMessage( ConfigManager.messages.MUTED );
					e.setCancelled( true );
				}
				if ( p.isMuted() ) {
					p.sendMessage( ConfigManager.messages.MUTED );
					e.setCancelled( true );
					System.out.println( "muted" );
				}
			}
			return;
		}
		if ( ChatManager.MuteAll ) {
			p.sendMessage( ConfigManager.messages.MUTED );
			e.setCancelled( true );
		}
		if ( p.isMuted() ) {
			p.sendMessage( ConfigManager.messages.MUTED );
			e.setCancelled( true );
		}
	}

	//    @EventHandler( priority = EventPriority.HIGHEST )
	//    public void playerLogout( PlayerDisconnectEvent e ) throws SQLException {
	//
	//    }

	@EventHandler
	public void playerKicked( ServerKickEvent e ) throws SQLException {
		PlayerManager.kickedPlayers.add( e.getPlayer() );
	}

}