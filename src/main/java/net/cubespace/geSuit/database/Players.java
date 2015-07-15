package net.cubespace.geSuit.database;

import com.google.common.collect.Maps;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.objects.GSPlayer;

import java.sql.*;
import java.util.*;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Players implements IRepository {

	public boolean playerExists(String uuid) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement playerExists = connectionHandler.getPreparedStatement("playerExists");
			playerExists.setString(1, uuid);

			return playerExists.executeQuery().next();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}

		return true;
	}

	public boolean playerNameExists(String playerName) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement playerExists = connectionHandler.getPreparedStatement("playerNameExists");
			playerExists.setString(1, playerName);

			return playerExists.executeQuery().next();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}

		return true;
	}

	public String getPlayerIP(String uuid) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement getPlayerIP = connectionHandler.getPreparedStatement("getPlayerIP");
			getPlayerIP.setString(1, uuid);

			String ip = null;
			ResultSet res = getPlayerIP.executeQuery();
			while (res.next()) {
				ip = res.getString("ipaddress");
			}
			res.close();

			return ip;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}

		return null;
	}

	public boolean getPlayerTPS(String uuid) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement getPlayerTPS = connectionHandler.getPreparedStatement("getPlayerTPS");
			getPlayerTPS.setString(1, uuid);

			boolean tps = true;
			ResultSet res = getPlayerTPS.executeQuery();
			while (res.next()) {
				tps = res.getBoolean("tps");
			}
			res.close();

			return tps;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}

		return true;
	}

	public String[] getAltPlayer(String uuid, String ip, boolean ignoreSelf) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement getAltPlayer = connectionHandler.getPreparedStatement("getAltPlayer");
			getAltPlayer.setString(1, ip);

			String altname = "";
			String altuuid = uuid;

			ResultSet res = getAltPlayer.executeQuery();

			if (ignoreSelf) {
				// Skip the first result when a new player joins (because it's always themselves)
				res.next();
			}

			if (res.next()) {
				altname = res.getString("playername");
				altuuid = res.getString("uuid");
			}
			res.close();

			if (!uuid.equals(altuuid)) {
				return new String[]{altname, altuuid};
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}

		return null;
	}

	public void insertPlayer(GSPlayer player, String ip) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement insertPlayer = connectionHandler.getPreparedStatement("insertPlayer");
			insertPlayer.setString(1, player.getName());
			insertPlayer.setString(2, player.getUuid());
			insertPlayer.setString(3, ip);

			insertPlayer.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}
	}

	public void insertPlayerConvert(String player, String uuid, Timestamp lastonline, String ip, boolean tps) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();
		try {
			PreparedStatement insertPlayerConvert = connectionHandler.getPreparedStatement("insertPlayerConvert");
			insertPlayerConvert.setString(1, player);
			insertPlayerConvert.setString(2, uuid);
			insertPlayerConvert.setTimestamp(3, lastonline);
			insertPlayerConvert.setTimestamp(4, lastonline);
			insertPlayerConvert.setString(5, ip);
			insertPlayerConvert.setBoolean(6, tps);

			insertPlayerConvert.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}
	}

	public void updatePlayer(GSPlayer gsPlayer) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement updatePlayer = connectionHandler.getPreparedStatement("updatePlayer");
			updatePlayer.setString(1, gsPlayer.getUuid().toString());
			updatePlayer.setString(2, gsPlayer.getName());
			updatePlayer.setString(3, gsPlayer.getNickname());
			updatePlayer.setString(4, gsPlayer.getIp());
			updatePlayer.setBoolean(5, gsPlayer.acceptingTeleports());
			updatePlayer.setBoolean(6, gsPlayer.isNewSpawn());
			updatePlayer.setString(7, gsPlayer.getChannel());
			updatePlayer.setBoolean(8, gsPlayer.isMuted());
			updatePlayer.setBoolean(9, gsPlayer.isChatSpying());
			updatePlayer.setBoolean(10, gsPlayer.isDND());
			updatePlayer.setString(11, gsPlayer.getUuid().toString());

			updatePlayer.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}
	}

	public GSPlayer loadPlayer(String uuid) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		GSPlayer player1 = null;
		try {
			PreparedStatement getPlayer = connectionHandler.getPreparedStatement("getPlayer");
			getPlayer.setString(1, uuid);

			ResultSet res = getPlayer.executeQuery();
			while (res.next()) {
				player1 = new GSPlayer(
						res.getString("uuid"),
						res.getString("playername"),
						res.getString("nickname"),
						res.getString("ipaddress"),
						res.getTimestamp("lastonline"),
						res.getTimestamp("firstonline"),
						res.getBoolean("tps"),
						res.getBoolean("newspawn"),
						res.getString("channel"),
						res.getBoolean("muted"),
						res.getBoolean("chatspy"),
						res.getBoolean("dnd")
				);
			}

			res.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}

		return player1;
	}

	public List<String> matchPlayers(String player) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		List<String> players = new ArrayList<String>();
		try {
			PreparedStatement getPlayer = connectionHandler.getPreparedStatement("matchPlayers");
			getPlayer.setString(1, "%" + player + "%");
			getPlayer.setString(2, player);

			ResultSet res = getPlayer.executeQuery();
			while (res.next()) {
				players.add(res.getString("playername"));
			}

			res.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}

		return players;
	}

	public Map<String, UUID> resolvePlayerNames(Collection<String> names) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		Map<String, UUID> resolved = Maps.newHashMapWithExpectedSize(names.size());
		try {
			int maxBatch = 40;
			int count = 0;
			StringBuilder builder = new StringBuilder();
			for (String name : names) {
				++count;
				if (builder.length() != 0) {
					builder.append(",");
				}

				builder.append(name);

				if (count >= maxBatch) {
					PreparedStatement statement = connectionHandler.getPreparedStatement("resolvePlayerName");
					statement.setString(1, builder.toString());

					ResultSet results = statement.executeQuery();
					while (results.next()) {
						resolved.put(results.getString("playername"), Utilities.makeUUID(results.getString("uuid")));
					}
					results.close();

					builder.setLength(0);
					count = 0;
				}
			}

			if (count > 0) {
				PreparedStatement statement = connectionHandler.getPreparedStatement("resolvePlayerName");
				statement.setString(1, builder.toString());

				ResultSet results = statement.executeQuery();
				while (results.next()) {
					resolved.put(results.getString("playername"), Utilities.makeUUID(results.getString("uuid")));
				}
				results.close();
			}

			return resolved;
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyMap();
		} finally {
			connectionHandler.release();
		}
	}

	/**
	 * Resolves a player name using the tracking table instead of the players table. This allows it to resolve old names for players
	 *
	 * @param names
	 * @return
	 */
	public Map<String, UUID> resolvePlayerNamesHistoric(Collection<String> names) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		Map<String, UUID> resolved = Maps.newHashMapWithExpectedSize(names.size());
		try {
			int maxBatch = 40;
			int count = 0;
			StringBuilder builder = new StringBuilder();
			for (String name : names) {
				++count;
				if (builder.length() != 0) {
					builder.append(",");
				}

				builder.append(name);

				if (count >= maxBatch) {
					PreparedStatement statement = connectionHandler.getPreparedStatement("resolveOldPlayerName");
					statement.setString(1, builder.toString());

					ResultSet results = statement.executeQuery();
					while (results.next()) {
						resolved.put(results.getString("player"), Utilities.makeUUID(results.getString("uuid")));
					}
					results.close();

					builder.setLength(0);
					count = 0;
				}
			}

			if (count > 0) {
				PreparedStatement statement = connectionHandler.getPreparedStatement("resolveOldPlayerName");
				statement.setString(1, builder.toString());

				ResultSet results = statement.executeQuery();
				while (results.next()) {
					resolved.put(results.getString("player"), Utilities.makeUUID(results.getString("uuid")));
				}
				results.close();
			}

			return resolved;
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyMap();
		} finally {
			connectionHandler.release();
		}
	}

	public Map<UUID, String> resolveUUIDs(Collection<UUID> ids) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		Map<UUID, String> resolved = Maps.newHashMapWithExpectedSize(ids.size());
		try {
			int maxBatch = 40;
			int count = 0;
			StringBuilder builder = new StringBuilder();
			for (UUID id : ids) {
				++count;
				if (builder.length() != 0) {
					builder.append(",");
				}

				builder.append(id.toString().replace("-", ""));

				if (count >= maxBatch) {
					PreparedStatement statement = connectionHandler.getPreparedStatement("resolveUUID");
					statement.setString(1, builder.toString());

					ResultSet results = statement.executeQuery();
					while (results.next()) {
						resolved.put(Utilities.makeUUID(results.getString("uuid")), results.getString("playername"));
					}
					results.close();

					builder.setLength(0);
					count = 0;
				}
			}

			if (count > 0) {
				PreparedStatement statement = connectionHandler.getPreparedStatement("resolveUUID");
				statement.setString(1, builder.toString());

				ResultSet results = statement.executeQuery();
				while (results.next()) {
					resolved.put(Utilities.makeUUID(results.getString("uuid")), results.getString("playername"));
				}
				results.close();
			}

			return resolved;
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyMap();
		} finally {
			connectionHandler.release();
		}
	}

	public void setPlayerChannel(String uuid, String channel) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement statement = connectionHandler.getPreparedStatement("setPlayerChannel");
			statement.setString(1, uuid);
			statement.setString(2, channel);

			statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}
	}

	public void setPlayerChatSpy(String uuid, boolean spying) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement statement = connectionHandler.getPreparedStatement("setPlayerChatSpy");
			statement.setBoolean(1, spying);
			statement.setString(2, uuid);

			statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}
	}

	public boolean nickNameExists(String nickname) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement nickExists = connectionHandler.getPreparedStatement("nickNameExists");
			nickExists.setString(1, nickname);

			return nickExists.executeQuery().next();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}

		return true;
	}

	public void setPlayerNickname(String uuid, String nickname) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement statement = connectionHandler.getPreparedStatement("setPlayerNickname");
			statement.setString(1, uuid);
			statement.setString(2, nickname);

			statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}
	}

	public boolean isPlayerMuted(String uuid) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement getMute = connectionHandler.getPreparedStatement("isPlayerMuted");
			getMute.setString(1, uuid);

			return getMute.executeQuery().next();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}

		return false;
	}

	public void mutePlayer(String uuid, boolean muted) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement statement = connectionHandler.getPreparedStatement("mutePlayer");
			statement.setString(1, uuid);
			statement.setBoolean(2, muted);

			statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}
	}

	public String playerUsingNickname(String nickname) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement getNick = connectionHandler.getPreparedStatement("playerUsingNickname");
			getNick.setString(1, nickname);

			String result = null;
			ResultSet res = getNick.executeQuery();
			while (res.next()) {
				result = res.getString("nickname");
			}
			res.close();

			return result;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}

		return null;
	}

	@Override
	public String[] getTable() {
		return new String[]{ConfigManager.main.Table_Players,
				"uuid VARCHAR(100) NOT NULL,"
						+ "playername VARCHAR(100), "
						+ "nickname VARCHAR(100), "
						+ "ipaddress VARCHAR(100), "
						+ "lastonline DATETIME NOT NULL, "
						+ "firstonline DATETIME NOT NULL, "
						+ "tps TINYINT(1) DEFAULT 1,"
						+ "newspawn TINYINT(1) DEFAULT 0,"
						+ "channel VARCHAR(100), "
						+ "muted TINYINT(1) DEFAULT 1,"
						+ "chatspy TINYINT(1) DEFAULT 1,"
						+ "dnd TINYINT(1) DEFAULT 1,"
						+ "CONSTRAINT pk_uuid PRIMARY KEY (uuid)"};
	}

	@Override
	public void registerPreparedStatements(ConnectionHandler connection) {
		connection.addPreparedStatement("getPlayerIP", "SELECT ipaddress FROM " + ConfigManager.main.Table_Players + " WHERE uuid = ?");
		connection.addPreparedStatement("playerExists", "SELECT uuid FROM " + ConfigManager.main.Table_Players + " WHERE uuid = ?");
		connection.addPreparedStatement("playerNameExists", "SELECT playername FROM " + ConfigManager.main.Table_Players + " WHERE playername = ?");
		connection.addPreparedStatement("getPlayerTPS", "SELECT tps FROM " + ConfigManager.main.Table_Players + " WHERE uuid = ?");
		connection.addPreparedStatement("getPlayer", "SELECT * FROM " + ConfigManager.main.Table_Players + " WHERE uuid = ?");
		connection.addPreparedStatement("getAltPlayer", "SELECT playername, uuid FROM " + ConfigManager.main.Table_Players + " WHERE ipaddress = ? ORDER BY lastonline DESC LIMIT 2");
		connection.addPreparedStatement("matchPlayers", "SELECT playername, uuid FROM " + ConfigManager.main.Table_Players + " WHERE playername like ? OR uuid like ? ORDER BY lastonline LIMIT 20");
		connection.addPreparedStatement("insertPlayer", "INSERT INTO " + ConfigManager.main.Table_Players + " (playername,uuid,firstonline,lastonline,ipaddress) VALUES (?, ?, NOW(), NOW(), ?)");
		connection.addPreparedStatement("insertPlayerConvert", "INSERT INTO " + ConfigManager.main.Table_Players + " (playername,uuid,firstonline,lastonline,ipaddress,tps) VALUES (?, ?, ?, ?, ?, ?)");
		connection.addPreparedStatement("getPlayers", "SELECT * FROM " + ConfigManager.main.Table_Players);
		connection.addPreparedStatement("setUUID", "UPDATE " + ConfigManager.main.Table_Players + " SET uuid = ? WHERE playername = ?");
		connection.addPreparedStatement("updatePlayer", "UPDATE " + ConfigManager.main.Table_Players + " SET uuid = ?, playername = ?, nickname = ?, ipaddress = ?, lastonline = NOW(), tps = ?, newspawn = ?, channel = ?, muted = ?, chatspy = ?, dnd = ? WHERE uuid = ?");
		connection.addPreparedStatement("resolvePlayerName", "SELECT playername, uuid FROM " + ConfigManager.main.Table_Players + " WHERE FIND_IN_SET(playername, ?)");
		connection.addPreparedStatement("resolveOldPlayerName", "SELECT player, uuid FROM " + ConfigManager.main.Table_Tracking + " WHERE FIND_IN_SET(player, ?) GROUP BY player");
		connection.addPreparedStatement("resolveUUID", "SELECT playername,uuid FROM " + ConfigManager.main.Table_Players + " WHERE FIND_IN_SET(uuid, ?)");
		connection.addPreparedStatement("setPlayerChannel", "UPDATE " + ConfigManager.main.Table_Players + " SET channel = ? WHERE uuid = ?");
		connection.addPreparedStatement("setPlayerChatSpy", "UPDATE " + ConfigManager.main.Table_Players + " SET chatspy = ? WHERE uuid = ?");
		connection.addPreparedStatement("nickNameExists", "SELECT nickname FROM " + ConfigManager.main.Table_Players + " WHERE nickname = ?");
		connection.addPreparedStatement("setPlayerNickname", "UPDATE " + ConfigManager.main.Table_Players + " SET nickname = ? WHERE uuid = ?");
		connection.addPreparedStatement("isPlayerMuted", "SELECT muted FROM " + ConfigManager.main.Table_Players + " WHERE uuid = ? AND muted = 1");
		connection.addPreparedStatement("mutePlayer", "UPDATE " + ConfigManager.main.Table_Players + " SET muted = ? WHERE uuid = ?");
		connection.addPreparedStatement("playerUsingNickname", "SELECT playername FROM " + ConfigManager.main.Table_Players + " WHERE nickname LIKE '%?%'");
	}

	@Override
	public void checkUpdate() {
		int installedVersion = ConfigManager.main.Version_Database_Players;

		System.out.println("Current Version of the Players Database: " + installedVersion);

		if (installedVersion < 2) {
			// Version 2 adds UUIDs as Field
			ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

			try {
				connectionHandler.getConnection().createStatement().execute("ALTER TABLE `" + ConfigManager.main.Table_Players + "` ADD `uuid` VARCHAR(100) NULL AFTER `playername`, ADD UNIQUE (`uuid`) ;");
			} catch (SQLException e) {
				System.out.println("Could not update the Player Database to version 2");
				e.printStackTrace();
				return;
			} finally {
				connectionHandler.release();
			}

			connectionHandler = DatabaseManager.connectionPool.getConnection();

			// Convert all Names to UUIDs
			PreparedStatement getPlayers = connectionHandler.getPreparedStatement("getPlayers");
			try {
				ResultSet res = getPlayers.executeQuery();
				while (res.next()) {
					String playername = res.getString("playername");
					String uuid = Utilities.getUUID(playername);

					if (uuid != null) {
						ConnectionHandler connectionHandler1 = DatabaseManager.connectionPool.getConnection();

						try {
							PreparedStatement preparedStatement = connectionHandler1.getPreparedStatement("setUUID");
							preparedStatement.setString(1, uuid);
							preparedStatement.setString(2, playername);
							preparedStatement.executeUpdate();
						} catch (SQLException e) {
							System.out.println("Could not update Player for update to version 2");
							e.printStackTrace();
						} finally {
							connectionHandler1.release();
						}
					}
				}
			} catch (SQLException e) {
				System.out.println("Could not get Players for update to version 2");
				e.printStackTrace();
				return;
			} finally {
				connectionHandler.release();
			}
		}
		if (installedVersion < 3) {
			// Version 3 adds "firstonline" field
			ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();
			try {
				System.out.println("Upgrading Player Database to version 3...");
				connectionHandler.getConnection().createStatement().execute("ALTER TABLE `" + ConfigManager.main.Table_Players + "` ADD `firstonline` DATETIME NOT NULL AFTER `uuid`;");
			} catch (SQLException e) {
				System.out.println("Could not update the Player Database to version 3");
				e.printStackTrace();
				return;
			} finally {
				connectionHandler.release();
			}

			// Convert any existing "firstonline" values to the current "lastonline" values
			connectionHandler = DatabaseManager.connectionPool.getConnection();
			Statement stmt = null;
			try {
				stmt = connectionHandler.getConnection().createStatement();
				stmt.executeUpdate("UPDATE `" + ConfigManager.main.Table_Players + "` SET firstonline=lastonline");
				stmt.close();
			} catch (SQLException e) {
				System.out.println("Could not upgrade firstonline values of existing players");
				e.printStackTrace();
				return;
			} finally {
				connectionHandler.release();
			}
		}

		ConfigManager.main.Version_Database_Players = 3;
		try {
			ConfigManager.main.save();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

}
