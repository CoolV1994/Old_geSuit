package net.cubespace.geSuit.objects;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.configs.SubConfig.ServerChannel;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ChatManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.pluginmessages.SendChatPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class GSPlayer {
	// General - Init
	private String uuid;
	private String playername;
	private String ip;
	private Timestamp lastOnline;
	private Timestamp firstOnline;
	// Teleports - Init
	private boolean acceptingTeleports;
	// Chat - Init
	private String nickname;
	private String channel;
	private boolean muted;
	private boolean chatspying;
	private boolean dnd;

	// General
	private String server = null;
	private long loginTime;
	private boolean firstConnect = true;
	private boolean joinAnnounced = false;
	private boolean isFirstJoin = false;
	private Track previousName;
	// Homes
	private HashMap<String, ArrayList<Home>> homes = new HashMap<>();
	// Teleport
	private Location deathBackLocation;
	private Location teleportBackLocation;
	private boolean backToDeath;
	// Spawn
	private boolean newSpawn = false;
	// Chat
	private ArrayList<String> ignores = new ArrayList<>();
	private ArrayList<Channel> channels = new ArrayList<>();
	private String replyPlayer;
	private String tempName;
	private boolean afk;

	public GSPlayer(String uuid, String name) {
		this(uuid, name, null);
	}

	public GSPlayer(String uuid, String name, String ip) {
		this(
				uuid,
				name,
				null,
				ip,
				new Timestamp(new Date().getTime()),
				new Timestamp(new Date().getTime()),
				true,
				true,
				ConfigManager.chat.defaultChannel,
				false,
				false,
				false
		);
	}

	public GSPlayer(
			String uuid,
			String name,
			String nickname,
			String ip,
			Timestamp lastOnline,
			Timestamp firstOnline,
			boolean tps,
			boolean newspawn,
			String channel,
			boolean muted,
			boolean chatspying,
			boolean dnd
	) {
		//ProxyServer.getInstance().getLogger().info("LOADED DATA: "+name+" "+uuid+" "+tps+" "+ip+" "+lastOnline);
		this.uuid = uuid;
		this.playername = name;
		this.nickname = nickname;
		this.ip = ip;
		this.lastOnline = lastOnline;
		this.firstOnline = firstOnline;
		this.loginTime = new Date().getTime();
		this.acceptingTeleports = tps;
		this.newSpawn = newspawn;
		this.channel = channel;
		this.muted = muted;
		this.chatspying = chatspying;
		this.dnd = dnd;
	}

	public String getName() {
		return playername;
	}

	public void setName(String newPlayerName) {
		playername = newPlayerName;
	}

	public ProxiedPlayer getProxiedPlayer() {
		return ProxyServer.getInstance().getPlayer(playername);
	}

	public void sendMessage(String message) {
		// Allow messages to be "silenced" by providing an empty string
		// (if you really must send a blank line for some reason, use a formatting code on its own, eg. "&f")
		if (message == null || message.isEmpty())
			return;

		for (String line : message.split("\n|\\{N\\}")) {
			getProxiedPlayer().sendMessage(TextComponent.fromLegacyText(Utilities.colorize(line)));
		}
	}

	public boolean acceptingTeleports() {
		return this.acceptingTeleports;
	}

	public void setAcceptingTeleports(boolean tp) {
		this.acceptingTeleports = tp;
	}

	public void setDeathBackLocation(Location loc) {
		deathBackLocation = loc;
		backToDeath = true;
	}

	public boolean hasDeathBackLocation() {
		return deathBackLocation != null;
	}

	public void setTeleportBackLocation(Location loc) {
		teleportBackLocation = loc;
		backToDeath = false;
	}

	public Location getLastBackLocation() {
		if (backToDeath) {
			return deathBackLocation;
		} else {
			return teleportBackLocation;
		}
	}

	public boolean hasTeleportBackLocation() {
		return teleportBackLocation != null;
	}

	public Location getDeathBackLocation() {
		return deathBackLocation;
	}

	public Location getTeleportBackLocation() {
		return teleportBackLocation;
	}

	public String getServer() {
		if ((getProxiedPlayer() == null) || (getProxiedPlayer().getServer() == null)) {
			return server;
		}

		return getProxiedPlayer().getServer().getInfo().getName();
	}

	public HashMap<String, ArrayList<Home>> getHomes() {
		return homes;
	}

	/**
	 * Will the next server connect be the first server to be joined in this session
	 */
	public boolean firstConnect() {
		return firstConnect;
	}

	/**
	 * Called in ServerConnectedEvent to signify that it has connected to a server
	 */
	public void connected() {
		firstConnect = false;
	}

	public void connectTo(ServerInfo s) {
		getProxiedPlayer().connect(s);
	}

	public String getUuid() {
		return uuid;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ipAddress) {
		ip = ipAddress;
	}

	public Timestamp getLastOnline() {
		return lastOnline;
	}

	public void setLastOnline(Timestamp value) {
		lastOnline = value;
	}

	public Timestamp getFirstOnline() {
		return firstOnline;
	}

	public void setFirstOnline(Timestamp value) {
		firstOnline = value;
	}

	/**
	 * Is this player a new player (as in the first time they have ever joined the proxy)
	 */
	public boolean isFirstJoin() {
		return isFirstJoin;
	}

	public void setFirstJoin(boolean value) {
		isFirstJoin = value;
	}

	public boolean hasJoinAnnounced() {
		return joinAnnounced;
	}

	public void setJoinAnnounced(boolean joinAnnounced) {
		this.joinAnnounced = joinAnnounced;
	}

	/**
	 * Signifies that this player must be taken to the new player spawn upon first connect
	 */
	public boolean isNewSpawn() {
		return newSpawn;
	}

	public void setNewSpawn(boolean newSpawn) {
		this.newSpawn = newSpawn;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	public void setLastName(Track track) {
		previousName = track;
	}

	public Track getLastName() {
		return previousName;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public boolean isMuted() {
		return muted;
	}

	public void setMute(boolean mute) {
		this.muted = mute;
		//updatePlayer();
	}

	public boolean hasNickname() {
		return nickname != null;
	}

	public String getNickname() {
		if (nickname == null) {
			return "";
		}
		return nickname;
	}

	public void setNickname(String nick) {
		this.nickname = nick;
	}

	public boolean isChatSpying() {
		return chatspying;
	}

	public void setChatSpying(boolean spy) {
		this.chatspying = spy;
		updatePlayer();
	}

	public boolean isDND() {
		return dnd;
	}

	public void setDND(boolean dnd) {
		this.dnd = dnd;
	}

	public void addIgnore(String player) {
		this.ignores.add(player);
	}

	public void removeIgnore(String player) {
		this.ignores.remove(player);
	}

	public boolean ignoringPlayer(String player) {
		return ignores.contains(player);
	}

	public void joinChannel(Channel channel) {
		this.channels.add(channel);
	}

	public void leaveChannel(Channel channel) {
		this.channels.remove(channel);
	}

	public boolean isInChannel(Channel channel) {
		return this.channels.contains(channel);
	}

	public void joinChannel(String channel) {
		this.channels.add(ChatManager.getChannel(channel));
	}

	public void leaveChannel(String channel) {
		this.channels.remove(ChatManager.getChannel(channel));
	}

	public boolean isInChannel(String channel) {
		return this.channels.contains(this.channels.add(ChatManager.getChannel(channel)));
	}

	public Channel getPlayersChannel() {
		return ChatManager.getChannel(channel);
	}

	public ArrayList<Channel> getPlayersChannels() {
		return channels;
	}

	public Channel getPlayersSimilarChannel(String channel) {
		for (Channel chan : channels) {
			if (chan.getName().contains(channel)) {
				return chan;
			}
		}
		return null;
	}

	public boolean isIgnoring(String ignore) {
		return ignores.contains(ignore);
	}

	public ArrayList<String> getIgnores() {
		return ignores;
	}

	public boolean hasIgnores() {
		return !ignores.isEmpty();
	}

	public void setReplyPlayer(String name) {
		replyPlayer = name;
	}

	public ServerChannel getServerData() {
		return ChatManager.getServerData(getServer());
	}

	public boolean hasReply() {
		return replyPlayer != null;
	}

	public String getReplyPlayer() {
		return replyPlayer;
	}

	public boolean isAFK() {
		return afk;
	}

	public void setAFK(boolean afk) {
		this.afk = afk;
	}

	public void updateDisplayName() {
		String name = getDisplayingName();
		if (name.length() > 16) {
			name = name.substring(0, 16);
		}
		if (ConfigManager.chat.updateNicknamesOnTab) {
			ProxiedPlayer p = ProxyServer.getInstance().getPlayer(playername);
			if (p != null && name != null) {
				p.setDisplayName(name);
			}
		}
	}

	public String getDisplayingName() {
		if (tempName != null) {
			return tempName;
		} else if (nickname != null) {
			return nickname;
		} else {
			return playername;
		}
	}

	public void setTempName(String name) {
		tempName = name;
		updatePlayer();
		updateDisplayName();
	}

	public void revertName() {
		tempName = null;
		updatePlayer();
		updateDisplayName();
	}

	public String getTempName() {
		if (tempName == null) {
			return "";
		}
		return tempName;
	}

	public void updatePlayer() {
		try {
			SendChatPlayer.execute(playername, geSuit.proxy.getServerInfo(getServer()), false);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
