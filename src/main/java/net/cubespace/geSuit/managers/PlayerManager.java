package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.events.NewPlayerJoinEvent;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.Ban;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Track;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class PlayerManager {
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");

	public static HashMap<UUID, GSPlayer> cachedPlayers = new HashMap<>();
	public static HashMap<String, GSPlayer> onlinePlayers = new HashMap<>();
	public static ArrayList<ProxiedPlayer> kickedPlayers = new ArrayList<>();

	public static boolean playerExists(ProxiedPlayer player) {
		return getPlayer(player.getName()) != null
				|| DatabaseManager.players.playerExists(player.getUUID());
	}

	public static boolean playerExists(UUID uuid) {
		return playerExists(uuid.toString().replaceAll("-", ""));
	}

	public static boolean playerExists(String uuid) {
		return DatabaseManager.players.playerExists(uuid);
	}

	public static boolean playerUserNameExists(String player) {
		return DatabaseManager.players.playerNameExists(player);
	}

	public static boolean playerNicknameExists(String nickname) {
		return DatabaseManager.players.nickNameExists(nickname);
	}

	public static void initPlayer(final PendingConnection connection, final LoginEvent event) {
		ProxyServer.getInstance().getScheduler().runAsync(geSuit.instance, new Runnable() {
			@Override
			public void run() {
				// Do ban check
				if (DatabaseManager.bans.isPlayerBanned(connection.getName(), connection.getUUID(), connection.getAddress().getHostString())) {
					Ban b = DatabaseManager.bans.getBanInfo(connection.getName(), connection.getUUID(), connection.getAddress().getHostString());

					boolean banned = true;
					if (b != null) {
						if (b.getType().equals("tempban")) {
							if (BansManager.checkTempBan(b)) {
								event.setCancelled(true);

								Date then = b.getBannedUntil();
								Date now = new Date();
								long timeDiff = then.getTime() - now.getTime();

								event.setCancelReason(Utilities.colorize(ConfigManager.messages.TEMP_BAN_MESSAGE.replace("{sender}", b.getBannedBy()).replace("{time}", sdf.format(then)).replace("{left}", Utilities.buildTimeDiffString(timeDiff, 2)).replace("{shortleft}", Utilities.buildShortTimeDiffString(timeDiff, 10)).replace("{message}", b.getReason())));
								LoggingManager.log(ChatColor.RED + connection.getName() + "'s connection refused due to being banned!");
							} else {
								banned = false;
							}
						} else {
							event.setCancelled(true);

							event.setCancelReason(Utilities.colorize(ConfigManager.messages.BAN_PLAYER_MESSAGE.replace("{sender}", b.getBannedBy()).replace("{message}", b.getReason())));
							LoggingManager.log(ChatColor.RED + connection.getName() + "'s connection refused due to being banned!");
						}

						if (banned) {
							// Dont load this player as they wont be joining
							event.completeIntent(geSuit.instance);
							return;
						}
					}
				}
				// Load the GSPlayer object for use
				GSPlayer gsPlayer;
				if (playerExists(connection.getUniqueId())) {
					gsPlayer = getPlayer(connection.getName());
					if (gsPlayer == null) {
						gsPlayer = DatabaseManager.players.loadPlayer(connection.getUUID());
						gsPlayer.setName(connection.getName());
						HomesManager.loadPlayersHomes(gsPlayer);
						LoggingManager.log(ConfigManager.messages.PLAYER_LOAD.replace("{player}", gsPlayer.getName()).replace("{uuid}", connection.getUniqueId().toString()));
					} else {
						LoggingManager.log(ConfigManager.messages.PLAYER_LOAD_CACHED.replace("{player}", gsPlayer.getName()).replace("{uuid}", connection.getUniqueId().toString()));
					}
				} else {
					gsPlayer = new GSPlayer(connection.getUUID(), connection.getName());
					gsPlayer.setFirstJoin(true);
				}

				gsPlayer.setIp(connection.getAddress().getHostString());

				Track history = DatabaseManager.tracking.checkNameChange(connection.getUniqueId(), connection.getName());
				if (history != null) {
					gsPlayer.setLastName(history);
				}

				cachedPlayers.put(connection.getUniqueId(), gsPlayer);

				// TODO: All database retrieval must be done here for this player
				event.completeIntent(geSuit.instance);
			}
		});
	}

	/**
	 * If this is the first connection in this session, this will do any needed final loading or setting up
	 * This also completes the creation process for new players
	 *
	 * @param player The joining player
	 * @return The GSPlayer instance for efficiency
	 */
	public static GSPlayer confirmJoin(final ProxiedPlayer player) {
		final GSPlayer gsPlayer = cachedPlayers.get(player.getUniqueId());
		if (gsPlayer.firstConnect()) {
			// Do new player stuff
			if (gsPlayer.isFirstJoin()) {
				DatabaseManager.players.insertPlayer(gsPlayer, player.getAddress().getHostString());

				LoggingManager.log(ConfigManager.messages.PLAYER_CREATE.replace("{player}", player.getName()).replace("{uuid}", player.getUniqueId().toString()));

				if (ConfigManager.main.NewPlayerBroadcast) {
					String welcomeMsg = null;
					sendBroadcast(welcomeMsg = ConfigManager.messages.NEW_PLAYER_BROADCAST.replace("{player}", player.getName()), player.getName());
					// Firing custom event
					ProxyServer.getInstance().getPluginManager().callEvent(new NewPlayerJoinEvent(player.getName(), welcomeMsg));
				}

				if (ConfigManager.spawn.SpawnNewPlayerAtNewspawn && SpawnManager.NewPlayerSpawn != null) {
					SpawnManager.newPlayers.add(player);

					ProxyServer.getInstance().getScheduler().schedule(geSuit.instance, new Runnable() {

						@Override
						public void run() {
							SpawnManager.sendPlayerToNewPlayerSpawn(gsPlayer);
							SpawnManager.newPlayers.remove(player);
						}

					}, 300, TimeUnit.MILLISECONDS);
				}
			}

			onlinePlayers.put(player.getName().toLowerCase(), gsPlayer);
		}

		return gsPlayer;
	}

	public static void unloadPlayer(String player) {
		if (onlinePlayers.containsKey(player.toLowerCase())) {
			onlinePlayers.remove(player.toLowerCase());

			LoggingManager.log(ConfigManager.messages.PLAYER_UNLOAD.replace("{player}", player));
		}
	}

	public static void sendMessageToTarget(CommandSender target, String message) {
		// Shouldnt need it. But let's be cautious.
		if (target == null) {
			LoggingManager.log("WARNING: sendMessageToTarget(CommandSender, String): Target is null!");
			return;
		}

		// Not exactly sure where we use the new line besides in the soon-to-be-removed MOTD...
		for (String line : Utilities.colorize(message).split("\n")) {
			if (geSuit.instance.isDebugEnabled()) {
				geSuit.instance.getLogger().info("DEBUG: [SendMessage] " + target.getName() + ": " + Utilities.colorize(line));
			}
			target.sendMessage(TextComponent.fromLegacyText(Utilities.colorize(line)));
		}
	}

	public static void sendMessageToTarget(GSPlayer target, String message) {
		if (target == null) {
			LoggingManager.log("WARNING: sendMessageToTarget(GSPlayer, String): Target is null!");
			return;
		}
		sendMessageToTarget(target.getProxiedPlayer(), message);
	}

	public static void sendMessageToTarget(String target, String message) {
		if ((target == null) || (target.isEmpty())) {
			LoggingManager.log("WARNING: sendMessageToTarget(String, String): Target is null or empty!");
			return;
		}
		sendMessageToTarget(getPlayer(target) != null ? getPlayer(target).getProxiedPlayer() : ProxyServer.getInstance().getConsole(), message);
	}

	public static void sendBroadcast(String message) {
		sendBroadcast(message, null);
	}

	public static void sendBroadcast(String message, String excludedPlayer) {
		for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
			if ((excludedPlayer != null) && (excludedPlayer.equals(p.getName()))) continue;
			sendMessageToTarget(p.getName(), message);
		}
		LoggingManager.log(message);
	}

	public static String getLastSeeninfos(String player, boolean full, boolean seeVanished) {
		GSPlayer p = getPlayer(player);
		LinkedHashMap<String, String> items = new LinkedHashMap<String, String>();

		boolean online = (p != null && p.getProxiedPlayer() != null);

		if (p == null) {
			// Player is offline, load data
			p = DatabaseManager.players.loadPlayer(Utilities.getUUID(player));
		}

		if (p == null) { // Unknown player
			return ConfigManager.messages.PLAYER_DOES_NOT_EXIST;
		}

		// Do a ban check
		Ban b = DatabaseManager.bans.getBanInfo(p.getName(), p.getUuid(), null);
		if (b != null) {
			if (b.getType().equals("tempban")) {
				if (b.getBannedUntil().getTime() > System.currentTimeMillis()) {
					items.put("Temp Banned", Utilities.buildShortTimeDiffString(b.getBannedUntil().getTime() - System.currentTimeMillis(), 3) + " remaining");
					items.put("Ban Reason", b.getReason());
					if (full) {
						items.put("Banned By", b.getBannedBy());
					}
				}
			} else {
				items.put("Banned", b.getReason());
				if (full) {
					items.put("Banned By", b.getBannedBy());
				}
			}
		}

		if (full) {
			if (online && p.getProxiedPlayer().getServer() != null) {
				items.put("Server", p.getProxiedPlayer().getServer().getInfo().getName());
			}
			items.put("IP", p.getIp());

			// Do GeoIP lookup
			String location = null;
			try {
				InetAddress address = InetAddress.getByName(p.getIp());
				location = GeoIPManager.lookup(address);
			} catch (UnknownHostException e) {
			}

			if (location != null) {
				items.put("Location", location);
			}
		}

		String message = (online ? ConfigManager.messages.PLAYER_SEEN_ONLINE : ConfigManager.messages.PLAYER_SEEN_OFFLINE);
		message = message.replace("{player}", p.getName());

		if (online) {
			String fullDate = String.format("%s @ %s",
					DateFormat.getDateInstance(DateFormat.MEDIUM).format(p.getLoginTime()),
					DateFormat.getTimeInstance(DateFormat.MEDIUM).format(p.getLoginTime()));
			String diff = Utilities.buildTimeDiffString(System.currentTimeMillis() - p.getLoginTime(), 2);
			message = message.replace("{timediff}", diff);
			message = message.replace("{date}", fullDate);
		} else {
			if (p.getLastOnline() != null) {
				String fullDate = String.format("%s @ %s",
						DateFormat.getDateInstance(DateFormat.MEDIUM).format(p.getLastOnline()),
						DateFormat.getTimeInstance(DateFormat.MEDIUM).format(p.getLastOnline()));
				String diff = Utilities.buildTimeDiffString(System.currentTimeMillis() - p.getLastOnline().getTime(), 2);
				message = message.replace("{timediff}", diff);
				message = message.replace("{date}", fullDate);
			} else {
				message = message.replace("{timediff}", "Never");
				message = message.replace("{date}", "Never");
			}
		}

		StringBuilder builder = new StringBuilder();
		builder.append(message);
		for (Entry<String, String> item : items.entrySet()) {
			builder.append('\n');
			builder.append(ConfigManager.messages.PLAYER_SEEN_ITEM_FORMAT
					.replace("{name}", item.getKey())
					.replace("{value}", item.getValue()));
		}

		return builder.toString();
	}

	public static GSPlayer matchOnlinePlayer(String player) {
		// Try exact match first (real name, not display name)
		GSPlayer match = getPlayer(player);
		if (match != null)
			return match;

		// Try fuzzy match (including display name)
		GSPlayer fuzzymatch = null;
		for (GSPlayer p : onlinePlayers.values()) {
			ProxiedPlayer pp = p.getProxiedPlayer();
			// Match exact display name (full match)
			if ((pp != null) && (pp.getDisplayName() != null) && pp.getDisplayName().equalsIgnoreCase(player))
				return p;

			// Match exact UUID if one was given
			if ((p.getUuid() != null) && (p.getUuid().equals(player)))
				return p;

			// Remember this "beginning" match in case we don't find a full match
			// (it's important to check displayname + name, incase their name was changed during this session)
			if ((p.getName().toLowerCase().startsWith(player)) ||
					((pp != null) && (pp.getDisplayName() != null) && (pp.getDisplayName().toLowerCase().startsWith(player))))
				match = p;

			// Remember this "fuzzy" match in case we don't find a full match or a "beginning" match)
			// (it's important to check displayname + name, incase their name was changed during this session)
			if ((p.getName().toLowerCase().contains(player)) ||
					((pp != null) && (pp.getDisplayName() != null) && (pp.getDisplayName().toLowerCase().contains(player))))
				fuzzymatch = p;
		}

		// Always return a "beginning" match first if we have one
		if (match != null) {
			return match;
		} else {
			return fuzzymatch;
		}
	}

	public static List<GSPlayer> getPlayersByIP(String ip) {
		List<GSPlayer> matchingPlayers = new ArrayList<GSPlayer>();
		if (ip == null) {
			Exception exception = new Exception("test");
			exception.printStackTrace();
			geSuit.instance.getLogger().severe("getPlayersByIP() ip is null");
			return null;
		}

		for (GSPlayer p : onlinePlayers.values()) {
			if (p.getProxiedPlayer().getAddress().getHostString().equalsIgnoreCase(ip)) {
				matchingPlayers.add(p);
			}
		}

		return matchingPlayers;
	}

	public static Collection<GSPlayer> getPlayers() {
		return onlinePlayers.values();
	}

	public static Collection<GSPlayer> cachedPlayers() {
		return cachedPlayers.values();
	}

	public static GSPlayer getPlayer(String player) {
		return onlinePlayers.get(player.toLowerCase());
	}

	public static GSPlayer getPlayer(String player, boolean ExpectOnline) {
		GSPlayer p = getPlayer(player);
		if ((p == null) && (ExpectOnline)) {
			geSuit.instance.getLogger().warning("Unable to find player named \"" + player + "\" in onlinePlayers list!");
		}
		return p;
	}

	public static GSPlayer getPlayer(UUID id) {
		return cachedPlayers.get(id);
	}

	public static GSPlayer getPlayer(ProxiedPlayer player) {
		return cachedPlayers.get(player.getUniqueId());
	}

	public static void updateTracking(GSPlayer player) {
		DatabaseManager.tracking.insertTracking(player.getName(), player.getUuid(), player.getIp());
	}

	public static void sendPrivateMessageToPlayer(GSPlayer from, String receiver, String message) {
		GSPlayer rec = matchOnlinePlayer(receiver);
		if (from.isMuted() && ConfigManager.chat.mutePrivateMessages) {
			from.sendMessage(ConfigManager.messages.MUTED);
			return;
		}
		if (rec == null) {
			from.sendMessage(ConfigManager.messages.PLAYER_NOT_ONLINE);
			return;
		}

		if (rec.isIgnoring(from.getName())) {
			from.sendMessage(ConfigManager.messages.PLAYER_IGNORING.replace("{player}", rec.getName()));
			return;
		}
		from.sendMessage(ConfigManager.messages.PRIVATE_MESSAGE_OTHER_PLAYER.replace("{player}", rec.getName()).replace("{message}", message));
		rec.sendMessage(ConfigManager.messages.PRIVATE_MESSAGE_RECEIVE.replace("{player}", from.getName()).replace("{message}", message));
		rec.setReplyPlayer(from.getName());
		sendPrivateMessageToSpies(from, rec, message);
	}

	public static void setPlayerAFK(String player, boolean sendGlobal, boolean hasDisplayPerm) {
		GSPlayer p = getPlayer(player);
		if (!p.isAFK()) {
			p.setAFK(true);
			if (sendGlobal) {
				sendBroadcast(ConfigManager.messages.PLAYER_AFK.replace("{player}", p.getDisplayingName()));
			} else {
				sendServerMessage(geSuit.proxy.getServerInfo(p.getServer()), ConfigManager.messages.PLAYER_AFK.replace("{player}", p.getDisplayingName()));
			}
			if (hasDisplayPerm) {
				p.setTempName(ConfigManager.messages.AFK_DISPLAY + p.getDisplayingName());
			}
		} else {
			p.setAFK(false);
			if (hasDisplayPerm) {
				p.revertName();
			}
			if (sendGlobal) {
				sendBroadcast(ConfigManager.messages.PLAYER_NOT_AFK.replace("{player}", p.getDisplayingName()));
			} else {
				sendServerMessage(geSuit.proxy.getServerInfo(p.getServer()), ConfigManager.messages.PLAYER_NOT_AFK.replace("{player}", p.getDisplayingName()));
			}
		}
	}

	private static void sendServerMessage(ServerInfo server, String message) {
		for (ProxiedPlayer p : server.getPlayers()) {
			for (String line : message.split("\n")) {
				p.sendMessage(line);
			}
		}
	}

	public static ArrayList<GSPlayer> getChatSpies() {
		ArrayList<GSPlayer> spies = new ArrayList<>();
		for (GSPlayer p : onlinePlayers.values()) {
			if (p.isChatSpying()) {
				spies.add(p);
			}
		}
		return spies;
	}

	public static void sendPrivateMessageToSpies(GSPlayer sender, GSPlayer receiver, String message) {
		for (GSPlayer p : getChatSpies()) {
			if (!(p.equals(sender) || p.equals(receiver))) {
				p.sendMessage(ConfigManager.messages.PRIVATE_MESSAGE_SPY.replace("{sender}", sender.getName()).replace("{player}", receiver.getName()).replace("{message}", message));
			}
		}
	}

	public static void sendMessageToSpies(ServerInfo server, String message) {
		for (GSPlayer p : getChatSpies()) {
			if (!p.getServer().equals(server.getName())) {
				p.sendMessage(message);
			}
		}
	}

	public static void setPlayerChatSpy(GSPlayer p) throws SQLException {
		if (p.isChatSpying()) {
			p.setChatSpying(false);
			p.sendMessage(ConfigManager.messages.CHATSPY_DISABLED);
		} else {
			p.setChatSpying(true);
			p.sendMessage(ConfigManager.messages.CHATSPY_ENABLED);
		}
		DatabaseManager.players.setPlayerChatSpy(p.getUuid(), p.isChatSpying());
	}

	public static boolean nickNameExists(String nick) {
		return DatabaseManager.players.nickNameExists(nick);
	}

	public static void setPlayersNickname(String player, String nick) throws SQLException {
		GSPlayer p = matchOnlinePlayer(player);
		if (p != null) {
			p.setNickname(nick);
			p.updateDisplayName();
			p.updatePlayer();
			DatabaseManager.players.setPlayerNickname(p.getUuid(), nick);
		}
	}

	public static boolean isPlayerMuted(String target) {
		GSPlayer player = matchOnlinePlayer(target);
		if (player != null) {
			return player.isMuted();
		} else {
			return DatabaseManager.players.isPlayerMuted(player.getUuid());
		}

	}

	public static void mutePlayer(String target) throws SQLException {
		GSPlayer p = matchOnlinePlayer(target);
		boolean isMuted = isPlayerMuted(target);
		if (p != null) {
			if (isMuted) {
				p.setMute(false);
				p.sendMessage(ConfigManager.messages.UNMUTED);
			} else {
				p.setMute(true);
				p.sendMessage(ConfigManager.messages.MUTED);
			}
		}
		DatabaseManager.players.mutePlayer(p.getUuid(), isMuted);
	}

	public static void tempMutePlayer(final GSPlayer t, int minutes) throws SQLException {
		mutePlayer(t.getName());
		geSuit.proxy.getScheduler().schedule(geSuit.instance, new Runnable() {
			@Override
			public void run() {
				if (t.isMuted()) {
					try {
						mutePlayer(t.getName());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

			}
		}, minutes, TimeUnit.MINUTES);
	}

	public static boolean playerUsingNickname(String string) {
		return DatabaseManager.players.playerUsingNickname(string) != null;
	}

	public static void removeNickname(String target) throws SQLException {
		setPlayersNickname(target, null);
	}

	public static boolean isPlayerOnline(String player) {
		return onlinePlayers.containsKey(player);
	}
}
