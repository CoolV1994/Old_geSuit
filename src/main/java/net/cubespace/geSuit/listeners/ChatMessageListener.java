package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.managers.*;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.pluginmessages.*;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class ChatMessageListener implements Listener {

	@EventHandler
	public void receivePluginMessage( PluginMessageEvent event ) throws IOException, SQLException {
		if ( event.isCancelled() ) {
			return;
		}
		if ( !( event.getSender() instanceof Server ) )
			return;
		if ( !event.getTag().equalsIgnoreCase( "geSuitChat" ) ) {
			return;
		}
		event.setCancelled( true );
		Server s = ( Server ) event.getSender();
		DataInputStream in = new DataInputStream( new ByteArrayInputStream( event.getData() ) );
		String task = in.readUTF();
		if ( task.equals( "LogChat" ) ) {
			String message = in.readUTF();
			if ( ConfigManager.chat.logChat ) {
				LoggingManager.log(message);
			}
			PlayerManager.sendMessageToSpies(s.getInfo(), message);

			return;
		}
		if ( task.equals( "GlobalChat" ) ) {
			String sender = in.readUTF();
			String message = in.readUTF();
			SendGlobalChat.execute(sender, message, s.getInfo());
			return;
		}
		if ( task.equals( "GetServerChannels" ) ) {
			SendServerChannelData.execute(s.getInfo());
			ChatManager.sendDefaultChannelsToServer( s.getInfo() );
			SendPrefixSuffix.execute(s.getInfo());
			return;
		}
		if ( task.equals( "GetFactionChannels" ) ) {
			ChatManager.sendFactionChannelsToServer( s.getInfo() );
			return;
		}
		if ( task.equals( "GetTownyChannels" ) ) {
			ChatManager.sendTownyChannelsToServer( s.getInfo() );
			return;
		}
		if ( task.equals( "AdminChat" ) ) {
			String message = in.readUTF();
			SendAdminChat.execute(message, s.getInfo());
			return;
		}
		if ( task.equals( "GetPlayer" ) ) {
			String player = in.readUTF();
			SendChatPlayer.execute(player, s.getInfo(), true);
			SendPlayerIgnores.execute(PlayerManager.getPlayer(player), s.getInfo());
			return;
		}
		if ( task.equals( "AFKPlayer" ) ) {
			ChatManager.setPlayerAFK( in.readUTF(), in.readBoolean(), in.readBoolean() );
			return;
		}
		if ( task.equals( "ReplyToPlayer" ) ) {
			ChatManager.replyToPlayer( in.readUTF(), in.readUTF() );
			return;
		}
		if ( task.equals( "PrivateMessage" ) ) {
			GSPlayer p = PlayerManager.getPlayer( in.readUTF() );
			PlayerManager.sendPrivateMessageToPlayer( p, in.readUTF(), in.readUTF() );
			return;
		}
		if ( task.equals( "SetChatSpy" ) ) {
			ChatManager.setChatSpy( in.readUTF() );
			return;
		}
		if ( task.equals( "IgnorePlayer" ) ) {
			GSPlayer p = PlayerManager.getPlayer( in.readUTF() );
			IgnoresManager.addIgnore( p, in.readUTF() );
			return;
		}
		if ( task.equals( "UnIgnorePlayer" ) ) {
			GSPlayer p = PlayerManager.getPlayer( in.readUTF() );
			IgnoresManager.removeIgnore( p, in.readUTF() );
			return;
		}
		if ( task.equals( "MuteAll" ) ) {
			ChatManager.muteAll( in.readUTF() );
			return;
		}
		if ( task.equals( "MutePlayer" ) ) {
			ChatManager.MutePlayer( in.readUTF(), in.readUTF(), in.readBoolean() );
			return;
		}
		if ( task.equals( "NickNamePlayer" ) ) {
			ChatManager.nickNamePlayer( in.readUTF(), in.readUTF(), in.readUTF(), in.readBoolean() );
			return;
		}
		if ( task.equals( "TempMutePlayer" ) ) {
			ChatManager.tempMutePlayer( in.readUTF(), in.readUTF(), in.readInt() );
			return;
		}
		if ( task.equals( "ReloadChat" ) ) {
			ChatManager.reloadChat( in.readUTF() );
			return;
		}
		if ( task.equals( "TogglePlayersChannel" ) ) {
			ChatManager.togglePlayersChannel( in.readUTF(), in.readBoolean(), in.readBoolean(), in.readBoolean(), in.readBoolean() );
			return;
		}
		if ( task.equals( "TogglePlayersFactionsChannel" ) ) {
			ChatManager.togglePlayersFactionsChannel( in.readUTF(), in.readBoolean() );
			return;
		}
		if ( task.equals( "ToggleToPlayersFactionChannel" ) ) {
			ChatManager.toggleToPlayersFactionChannel( in.readUTF(), in.readUTF(), in.readBoolean() );
			return;
		}
		if ( task.equals( "TogglePlayersTownyChannel" ) ) {
			ChatManager.togglePlayersTownyChannel( in.readUTF(), in.readBoolean(), in.readBoolean() );
			return;
		}
		if ( task.equals( "ToggleToPlayersTownyChannel" ) ) {
			ChatManager.toggleToPlayersTownyChannel( in.readUTF(), in.readUTF(), in.readBoolean(), in.readBoolean() );
			return;
		}
		if ( task.equals( "TogglePlayerToChannel" ) ) {
			ChatManager.togglePlayerToChannel( in.readUTF(), in.readUTF(), in.readBoolean(), in.readBoolean(), in.readBoolean(), in.readBoolean() );
			return;
		}
		if ( task.equals( "GetChannelInfo" ) ) {
			ChatManager.sendPlayerChannelInformation( in.readUTF(), in.readUTF(), in.readBoolean() );
			return;
		}
		if ( task.equals( "SetChannelFormat" ) ) {
			ChatManager.setChannelsFormat( in.readUTF(), in.readUTF(), in.readBoolean() );
			return;
		}
		if ( task.equals( "SendVersion" ) ) {
			LoggingManager.log( in.readUTF() );
			return;
		}
	}

}