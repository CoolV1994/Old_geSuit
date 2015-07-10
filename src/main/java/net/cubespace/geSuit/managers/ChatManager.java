package net.cubespace.geSuit.managers;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.configs.SubConfig.ServerChannel;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.Channel;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.pluginmessages.SendChannel;
import net.cubespace.geSuit.pluginmessages.SendChatPlayer;
import net.cubespace.geSuit.pluginmessages.SendPrefixSuffix;
import net.cubespace.geSuit.pluginmessages.SendServerChannelData;
import net.cubespace.geSuit.tasks.SendPluginMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatManager {

	public static ArrayList<Channel> channels = new ArrayList();
	public static HashMap<String, ServerChannel> serverData = new HashMap();
	public static boolean MuteAll;

	public static void defaultPrefixes() {
		if (ConfigManager.chat.prefixes.isEmpty()) {
			ConfigManager.chat.prefixes.put("Default", "");
			ConfigManager.chat.suffixes.put("Default", "");
			try {
				ConfigManager.chat.save();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
	}

	public static void loadChannels() {
		LoggingManager.log(ChatColor.GOLD + "Loading channels");
		String server = ProxyServer.getInstance().getConsole().getName();
		//Load Global
		loadChannel(server, "Global", ConfigManager.channels.Global, true, true);
		//Load Admin Channel
		loadChannel(server, "Admin", ConfigManager.channels.Admin, true, true);
		//Load Faction Channel
		loadChannel(server, "Faction", ConfigManager.channels.Faction, true, true);
		//Load Faction Ally Channel
		loadChannel(server, "FactionAlly", ConfigManager.channels.FactionAlly, true, true);
		//Load Towny Channels
		loadChannel(server, "Town", ConfigManager.channels.TownyTown, true, true);
		loadChannel(server, "Nation", ConfigManager.channels.TownyNation, true, true);
		//Load Server Channels
		for (String serverName : ProxyServer.getInstance().getServers().keySet()) {
			ServerChannel chan = ConfigManager.channels.Servers.get(serverName);
			if (chan == null) {
				chan = new ServerChannel();
				chan.serverName = serverName;
				chan.shortName = serverName.substring(0, 1);
				chan.serverFormat = ConfigManager.messages.CHANNEL_DEFAULT_SERVER;
				chan.localForamt = ConfigManager.messages.CHANNEL_DEFAULT_LOCAL;
				chan.forceChannel = false;
				chan.forcedChannel = ConfigManager.chat.defaultChannel;
				chan.localDistance = 50;
				chan.connectionMessages = true;
				ConfigManager.channels.Servers.put(serverName, chan);
			}
			loadChannel(server, serverName, chan.serverFormat, true, true);
			loadChannel(server, serverName + " Local", chan.localForamt, true, true);
			serverData.put(serverName, chan);
		}
		try {
			ConfigManager.channels.save();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		//Load custom channels from db

		LoggingManager.log(ChatColor.GOLD + "Channels loaded - " + ChatColor.DARK_GREEN + channels.size());
	}

	public static void loadChannel(String owner, String name, String format, boolean isDefault, boolean open) {
		Channel c = new Channel(name, format, owner, false, isDefault, open);
		channels.add(c);
	}

	//    public static boolean usingFactions( Server server ) {
	//        return serverData.get( server.getInfo().getName() ).usingFactions();
	//    }
	//
	//    public static boolean usingTowny( Server server ) {
	//        return serverData.get( server.getInfo().getName() ).usingTowny();
	//    }

	public static void sendDefaultChannelsToServer(ServerInfo s) {
		ArrayList<Channel> chans = getDefaultChannels(s.getName());
		for (Channel c : chans) {
			SendChannel.execute(s, c);
		}
	}

	public static void sendFactionChannelsToServer(ServerInfo s) {
		serverData.get(s.getName()).usingFactionChannels = true;
		SendChannel.execute(s, getChannel("Faction"));
		SendChannel.execute(s, getChannel("FactionAlly"));
	}

	public static void sendTownyChannelsToServer(ServerInfo s) {
		serverData.get(s.getName()).usingTowny = true;
		SendChannel.execute(s, getChannel("Town"));
		SendChannel.execute(s, getChannel("Nation"));
	}

	/*public static void checkForPlugins( ServerInfo server ) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream( b );
		try {
			out.writeUTF( "PluginCheck" );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		sendPluginMessageTaskChat( server, b );
	}*/

	public static ArrayList<Channel> getDefaultChannels(String server) {
		ArrayList<Channel> chans = new ArrayList();
		for (Channel c : channels) {
			if (c.getName().equals("Global") || c.getName().equals("Admin") || c.getName().equals(server) || c.getName().equals(server + " Local")) {
				chans.add(c);
			}
		}
		return chans;
	}


	/*public void createNewCustomChannel( String owner, String name, String format, boolean open ) throws SQLException {
		SQLManager.standardQuery( "INSERT INTO BungeeCustomChannels VALUES('" + name + "','" + owner + "','" + format + "'," + open + ",)" );
		Channel c = new Channel( owner, name, format, false, false, false );
		channels.add( c );
	}*/

	public static boolean channelExists(String name) {
		for (Channel c : channels) {
			if (c.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public static boolean channelSimilarExists(String name) {
		for (Channel c : channels) {
			if (c.getName().toLowerCase().contains(name.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static ArrayList<Channel> getPlayersChannels(GSPlayer p) {
		return p.getPlayersChannels();
	}

	//getPlayersChannels    public static void loadPlayersChannels( ProxiedPlayer player, Server server ) throws SQLException {
	//        ResultSet res = SQLManager.sqlQuery( "SELECT channel FROM BungeeChannelMembers WHERE player = '" + player.getName() + "'" );
	//        while ( res.next() ) {
	//            getChannel( res.getString( "channel" ) ).addMember( player.getName() );
	//        }
	//        res.close();
	//        sendPlayersChannels( PlayerManager.getPlayer( player ), server );
	//    }

	//    private static void sendPlayersChannels( GSPlayer p, Server server ) {
	//        for ( Channel c : p.getPlayersChannels() ) {
	//            if ( !channelsSentToServers.get( server.getInfo().getName() ).contains( c ) ) {
	//                SendChannel.execute( server, c );
	//            }
	//       }
	//    }


	public static Channel getChannel(String name) {
		for (Channel chan : channels) {
			if (chan.getName().equals(name)) {
				return chan;
			}
		}
		return null;
	}

	public static Channel getSimilarChannel(String name) {
		for (Channel chan : channels) {
			if (chan.getName().toLowerCase().contains(name.toLowerCase())) {
				return chan;
			}
		}
		return null;
	}

	public static void setPlayerToForcedChannel(GSPlayer p, ServerInfo server) throws SQLException {

		Channel c = getChannel(p.getChannel());

		ServerChannel sd = serverData.get(server.getName());
		if (sd.forceChannel) {
			c = getChannel(sd.getForcedChannel());
			setPlayersChannel(p, c, false);
			return;
		}
		if (c == null) {
			c = getChannel(getServersDefaultChannel(sd));
			setPlayersChannel(p, c, false);
			return;
		}
		if (!c.isDefault()) {
			return;
		}
		if (isFactionChannel(c) && sd.usingFactionChannels) {
			return;
		}
		if (isTownyChannel(c) && sd.usingTowny) {
			return;
		}

		if (isServerChannel(c)) {
			c = getChannel(sd.serverName);
			setPlayersChannel(p, c, false);
		} else if (isLocalChannel(c)) {
			c = getChannel(sd.serverName + " Local");
			setPlayersChannel(p, c, false);
		} else if (c.getName().equals("Global")) {
			return;
		} else {
			c = getChannel(getServersDefaultChannel(sd));
			setPlayersChannel(p, c, false);
			return;
		}


	}

	private static boolean isLocalChannel(Channel c) {
		if (c.isDefault() && geSuit.proxy.getServers().containsKey(c.getName().split(" ")[0])) {
			return true;
		}
		return false;
	}

	private static boolean isServerChannel(Channel c) {
		if (c.isDefault() && geSuit.proxy.getServers().containsKey(c.getName())) {
			return true;
		}
		return false;
	}

	public static boolean isFactionChannel(Channel c) {
		return c.getName().equals("Faction") || c.getName().equals("FactionAlly");
	}

	public static boolean isTownyChannel(Channel c) {
		return c.getName().equals("Town") || c.getName().equals("Nation");
	}

	public static void setPlayerAFK(String player, boolean sendGlobal, boolean hasDisplayPerm) {
		PlayerManager.setPlayerAFK(player, sendGlobal, hasDisplayPerm);
	}

	public static void setChatSpy(String player) throws SQLException {
		GSPlayer p = PlayerManager.getPlayer(player);
		PlayerManager.setPlayerChatSpy(p);

	}

	public static void muteAll(String string) {
		if (MuteAll) {
			MuteAll = false;
			PlayerManager.sendBroadcast(ConfigManager.messages.MUTE_ALL_DISABLED.replace("{sender}", string));
		} else {
			MuteAll = true;
			PlayerManager.sendBroadcast(ConfigManager.messages.MUTE_ALL_ENABLED.replace("{sender}", string));
		}

	}

	public static void nickNamePlayer(String sender, String target, String nickname, boolean on) throws SQLException {
		GSPlayer s = PlayerManager.getPlayer(sender);
		GSPlayer t;
		nickname = Utilities.colorize(nickname);
		if (nickname.length() > ConfigManager.chat.nickNameLimit) {
			s.sendMessage(ConfigManager.messages.NICKNAME_TOO_LONG);
			return;
		}
		if (!sender.equals(target)) {
			if (!PlayerManager.playerUserNameExists(target)) {
				s.sendMessage(ConfigManager.messages.PLAYER_DOES_NOT_EXIST);
				return;
			}
			t = PlayerManager.matchOnlinePlayer(target);
			if (t != null) {
				target = t.getName();
			}
		} else {
			t = s;
		}
		// Are we removing the nick?
		if (on == false) {
			PlayerManager.removeNickname(target);
			if (s.getName().equals(target)) {
				s.sendMessage(ConfigManager.messages.NICKNAME_REMOVED);
			} else {
				s.sendMessage(ConfigManager.messages.NICKNAME_REMOVED_PLAYER.replace("{player}", target));
				if (t != null) {
					t.sendMessage(ConfigManager.messages.NICKNAME_REMOVED);
				}
			}
			return;
		}
		// Has it been taken already?
		if (PlayerManager.nickNameExists(nickname)) {
			s.sendMessage(ConfigManager.messages.NICKNAME_TAKEN);
			return;
		}
		// TODO: Remove this if - does it even get used?
		// Has it been taken already and is the targets username the same as the nickname?
		if (PlayerManager.nickNameExists(nickname) && !t.getName().equals(nickname)) {
			s.sendMessage(ConfigManager.messages.NICKNAME_TAKEN);
			return;
		}
		// TODO: Add in impersonation prevention
		// Assign the nickname
		PlayerManager.setPlayersNickname(target, nickname);
		// Send out messages
		if (t != null && !t.equals(s)) {
			s.sendMessage(ConfigManager.messages.NICKNAMED_PLAYER.replace("{player}", target).replace("{name}", nickname));
			if (t != null) {
				t.sendMessage(ConfigManager.messages.NICKNAME_CHANGED.replace("{name}", nickname));
			}
		} else {
			if (target.equals(s.getName())) {
				s.sendMessage(ConfigManager.messages.NICKNAME_CHANGED.replace("{name}", Utilities.colorize(nickname)));
			} else {
				s.sendMessage(ConfigManager.messages.NICKNAMED_PLAYER.replace("{name}", Utilities.colorize(nickname)).replace("{player}", target));
			}
		}
	}

	public static void replyToPlayer(String sender, String message) {
		GSPlayer p = PlayerManager.getPlayer(sender);
		String reply = p.getReplyPlayer();
		if (p.isMuted() && ConfigManager.chat.mutePrivateMessages) {
			p.sendMessage(ConfigManager.messages.MUTED);
			return;
		}
		if (reply == null) {
			p.sendMessage(ConfigManager.messages.NO_ONE_TO_REPLY);
			return;
		}
		PlayerManager.sendPrivateMessageToPlayer(p, reply, message);
	}

	public static void MutePlayer(String sender, String target, boolean command) throws SQLException {
		GSPlayer p = PlayerManager.getPlayer(sender);
		if (!PlayerManager.playerUserNameExists(target)) {
			p.sendMessage(ConfigManager.messages.PLAYER_DOES_NOT_EXIST);
			return;
		}
		GSPlayer t = PlayerManager.matchOnlinePlayer(target);
		if (t != null) {
			target = t.getName();
		}
		if (command) {
			command = !PlayerManager.isPlayerMuted(target);
		} else {
			if (!PlayerManager.isPlayerMuted(target)) {
				p.sendMessage(ConfigManager.messages.PLAYER_NOT_MUTE);
				return;
			}
		}
		PlayerManager.mutePlayer(target);
		if (command) {
			p.sendMessage(ConfigManager.messages.PLAYER_MUTED.replace("{player}", target));
			return;
		} else {
			p.sendMessage(ConfigManager.messages.PLAYER_UNMUTED.replace("{player}", target));
		}

	}

	public static void tempMutePlayer(String sender, String target, int minutes) throws SQLException {
		GSPlayer p = PlayerManager.getPlayer(sender);
		GSPlayer t = PlayerManager.matchOnlinePlayer(target);
		if (t == null) {
			p.sendMessage(ConfigManager.messages.PLAYER_NOT_ONLINE);
			return;
		}
		PlayerManager.tempMutePlayer(t, minutes);
		p.sendMessage(ConfigManager.messages.PLAYER_MUTED.replace("{player}", t.getDisplayingName()));
	}

	public static void reloadChat(String readUTF) throws SQLException, IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("Reload");
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (ServerInfo s : geSuit.proxy.getServers().values()) {
			geSuit.proxy.getScheduler().runAsync(geSuit.instance, new SendPluginMessage("geSuitChat", s, b));
		}
		channels.clear();
		serverData.clear();
		try {
			ConfigManager.chat.reload();
			ConfigManager.channels.reload();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		//Channels.reload();
		loadChannels();
		for (ServerInfo s : geSuit.proxy.getServers().values()) {
			SendServerChannelData.execute(s);
			ChatManager.sendDefaultChannelsToServer(s);
			SendPrefixSuffix.execute(s);
		}
		for (ProxiedPlayer p : geSuit.proxy.getPlayers()) {
			SendChatPlayer.execute(p.getName(), p.getServer().getInfo(), true);
		}
	}

	public static Channel getPlayersChannel(GSPlayer p) {
		return getChannel(p.getChannel());
	}

	public static Channel getPlayersNextChannel(GSPlayer p, boolean factionAccess, boolean townyAccess, boolean inNation, boolean bypass) {
		Channel current = p.getPlayersChannel();
		String c = current.getName();
		ServerChannel sd = p.getServerData();
		if (!bypass && sd.forceChannel) {
			String forced = sd.getForcedChannel();
			if (sd.usingFactionChannels && factionAccess) {
				if (c.equals(forced)) {
					return getChannel("Faction");
				}
				if (c.equals("Faction")) {
					return getChannel("FactionAlly");
				}
			}
			if (sd.usingTowny && townyAccess) {
				if (c.equals(forced) || c.equals("FactionAlly")) {
					return getChannel("Town");
				}
				if (c.equals("Town") && inNation) {
					return getChannel("Nation");
				}
			}
			return getChannel(forced);
		}
		if (sd.usingFactionChannels && factionAccess) {
			if (c.equals(p.getServerData().serverName + " Local")) {
				return getChannel("Faction");
			}
			if (c.equals("Faction")) {
				return getChannel("FactionAlly");
			}
		}
		if (sd.usingTowny && townyAccess) {
			if (c.equals(p.getServerData().serverName + " Local") || c.equals("FactionAlly")) {
				return getChannel("Town");
			}
			if (c.equals("Town") && inNation) {
				return getChannel("Nation");
			}
		}
		if (c.equals("Global")) {
			return getChannel(p.getServerData().serverName);
		}
		if (c.equals(p.getServer())) {
			return getChannel(p.getServerData().serverName + " Local");
		}
		return getChannel("Global");
	}

	public static void setPlayersChannel(GSPlayer p, Channel channel, boolean message) throws SQLException {
		p.setChannel(channel.getName());
		p.updatePlayer();
		DatabaseManager.players.setPlayerChannel(p.getUuid(), channel.getName());
		if (message) {
			p.sendMessage(ConfigManager.messages.CHANNEL_TOGGLE.replace("{channel}", channel.getName()));
		}
	}

	public static ServerChannel getServerData(String server) {
		return serverData.get(server);
	}

	public static boolean isPlayerChannelMember(GSPlayer p, Channel channel) {
		return channel.getMembers().contains(p);
	}

	public static boolean canPlayerToggleToChannel(GSPlayer p, Channel channel) {
		if (channel.isDefault()) {
			if (p.getServerData().forceChannel) {
				String forcedChannel = p.getServerData().getForcedChannel();
				if (channel.getName().equals(forcedChannel)) {
					return true;
				} else {
					return false;
				}
			}
			return true;
		}
		return true;
	}

	public static void togglePlayersChannel(String player, boolean factionAccess, boolean townyAccess, boolean inNation, boolean bypass) throws SQLException {
		GSPlayer p = PlayerManager.getPlayer(player);
		setPlayersChannel(p, getPlayersNextChannel(p, factionAccess, townyAccess, inNation, bypass), true);
	}

	public static void togglePlayerToChannel(String sender, String channel, boolean factionAccess, boolean townyAccess, boolean inNation, boolean bypass) throws SQLException {
		GSPlayer p = PlayerManager.getPlayer(sender);
		if (channel.equalsIgnoreCase("Local")) {
			channel = p.getServer() + " Local";
		} else if (channel.equalsIgnoreCase("Server")) {
			channel = p.getServer();
		} else if (channel.equalsIgnoreCase("Global")) {
			channel = "Global";
		}
		Channel c = getSimilarChannel(channel);
		if (c == null) {
			p.sendMessage(ConfigManager.messages.CHANNEL_DOES_NOT_EXIST);
			return;
		}
		if (isFactionChannel(c) && !factionAccess) {
			p.sendMessage(ConfigManager.messages.CHANNEL_UNTOGGLABLE.replace("{channel}", c.getName()));
			return;
		}
		if (isTownyChannel(c) && !townyAccess || (c.getName().equals("Nation") && !inNation)) {
			p.sendMessage(ConfigManager.messages.CHANNEL_UNTOGGLABLE.replace("{channel}", c.getName()));
			return;
		}
		if (!bypass) {
			if (c.isDefault() || isPlayerChannelMember(p, c)) {
				if (canPlayerToggleToChannel(p, c)) {
					setPlayersChannel(p, c, true);
					return;
				} else {
					p.sendMessage(ConfigManager.messages.CHANNEL_UNTOGGLABLE.replace("{channel}", c.getName()));
					return;
				}
			} else {
				p.sendMessage(ConfigManager.messages.CHANNEL_NOT_A_MEMBER);
				return;
			}
		} else {
			setPlayersChannel(p, c, true);
		}

	}

	public static String getServersDefaultChannel(ServerChannel server) {
		return server.getForcedChannel();
	}

	public static void togglePlayersFactionsChannel(String player, Boolean inFaction) throws SQLException {
		GSPlayer p = PlayerManager.getPlayer(player);
		String channel = p.getChannel();
		String newchannel;
		if (!inFaction) {
			p.sendMessage(ConfigManager.messages.FACTION_NONE);
			return;
		}
		if (channel.equals("Faction")) {
			newchannel = "FactionAlly";
			p.sendMessage(ConfigManager.messages.FACTION_ALLY_TOGGLE);
		} else if (channel.equals("FactionAlly")) {
			newchannel = getServersDefaultChannel(p.getServerData());
			p.sendMessage(ConfigManager.messages.CHANNEL_TOGGLE.replace("{channel}", newchannel));
		} else {
			newchannel = "Faction";
			p.sendMessage(ConfigManager.messages.FACTION_TOGGLE);
		}
		Channel c = getChannel(newchannel);
		setPlayersChannel(p, c, false);
	}

	public static void togglePlayersTownyChannel(String player, Boolean inTown, Boolean inNation) throws SQLException {
		GSPlayer p = PlayerManager.getPlayer(player);
		String channel = p.getChannel();
		String newchannel;
		if (!inTown) {
			p.sendMessage(ConfigManager.messages.TOWNY_NONE);
			return;
		}
		if (channel.equals("Town") && inNation) {
			newchannel = "Nation";
			p.sendMessage(ConfigManager.messages.TOWNY_NATION_TOGGLE);
		} else if (channel.equals("Nation") || channel.equals("Town")) {
			newchannel = getServersDefaultChannel(p.getServerData());
			p.sendMessage(ConfigManager.messages.CHANNEL_TOGGLE.replace("{channel}", newchannel));
		} else {
			newchannel = "Town";
			p.sendMessage(ConfigManager.messages.TOWNY_TOGGLE);
		}
		Channel c = getChannel(newchannel);
		setPlayersChannel(p, c, false);
	}

	public static void toggleToPlayersFactionChannel(String sender, String channel, boolean hasFaction) throws SQLException {
		GSPlayer p = PlayerManager.getPlayer(sender);
		if (!hasFaction) {
			p.sendMessage(ConfigManager.messages.FACTION_NONE);
			return;
		}
		if (p.getChannel().equals(channel)) {
			channel = getServersDefaultChannel(p.getServerData());
			p.sendMessage(ConfigManager.messages.FACTION_OFF_TOGGLE);
		} else if (channel.equals("Faction")) {
			p.sendMessage(ConfigManager.messages.FACTION_TOGGLE);
		} else {
			p.sendMessage(ConfigManager.messages.FACTION_ALLY_TOGGLE);
		}
		Channel c = getChannel(channel);
		setPlayersChannel(p, c, false);
	}

	public static void toggleToPlayersTownyChannel(String sender, String channel, boolean hasTown, Boolean hasNation) throws SQLException {
		GSPlayer p = PlayerManager.getPlayer(sender);
		if (!hasTown) {
			p.sendMessage(ConfigManager.messages.TOWNY_NONE);
			return;
		}
		if (p.getChannel().equals(channel)) {
			channel = getServersDefaultChannel(p.getServerData());
			p.sendMessage(ConfigManager.messages.TOWNY_OFF_TOGGLE);
		} else if (channel.equals("Town")) {
			p.sendMessage(ConfigManager.messages.TOWNY_TOGGLE);
		} else if (channel.equals("Nation")) {
			if (hasNation) {
				p.sendMessage(ConfigManager.messages.TOWNY_NATION_TOGGLE);
			} else {
				p.sendMessage(ConfigManager.messages.TOWNY_NATION_NONE);
				return;
			}
		}
		Channel c = getChannel(channel);
		setPlayersChannel(p, c, false);
	}

	public static void sendPlayerChannelInformation(String sender, String channel, boolean perm) {
		Channel c = getSimilarChannel(channel);
		GSPlayer p = PlayerManager.getPlayer(sender);
		if (c == null) {
			p.sendMessage(ConfigManager.messages.CHANNEL_DOES_NOT_EXIST);
			return;
		}
		p.sendMessage(ChatColor.DARK_AQUA + "---------" + ChatColor.GOLD + "Channel Info" + ChatColor.DARK_AQUA + "---------");
		p.sendMessage(" ");
		p.sendMessage(ChatColor.GOLD + "Channel name: " + ChatColor.AQUA + c.getName());
		if (!c.isDefault()) {
			p.sendMessage(ChatColor.GOLD + "Channel status: " + ChatColor.AQUA + c.getStatus());
		}
		if (!c.isDefault()) {
			p.sendMessage(ChatColor.GOLD + "Channel owner: " + ChatColor.AQUA + c.getOwner());
			ArrayList<GSPlayer> members = c.getMembers();
			String players = ChatColor.GOLD + "Members: " + ChatColor.AQUA + "";
			for (int i = 0; i < members.size() && i < 10; i++) {
				players += members.get(i) + ", ";
			}
			players = players.substring(0, players.length() - 2);
			if (members.size() >= 10) {
				players += "...";
			}
			p.sendMessage(players);
			if (p.getName().equals(c.getOwner()) || perm) {
				p.sendMessage(ChatColor.GOLD + "Format: " + ChatColor.AQUA + c.format());
			}
		} else {
			p.sendMessage(ChatColor.GOLD + "Channel type: " + ChatColor.AQUA + "Server");
			if (perm) {
				p.sendMessage(ChatColor.GOLD + "Format: " + ChatColor.AQUA + c.format());
			}
		}
	}

	public static void setChannelsFormat(String readUTF, String readUTF2, boolean readBoolean) {
		// TODO Auto-generated method stub
		//update channel
		//sql update
		//resend to servers

	}
}