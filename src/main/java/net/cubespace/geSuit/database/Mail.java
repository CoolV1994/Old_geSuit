package net.cubespace.geSuit.database;

import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vinnie on 6/28/2015.
 */
public class Mail implements IRepository {
	public List<String> getMail(String player, int start) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();
		List<String> mail = new ArrayList<>();

		try {
			PreparedStatement getMail = connectionHandler.getPreparedStatement("getMail");
			getMail.setString(1, player);
			getMail.setInt(2, start);

			ResultSet res = getMail.executeQuery();
			while (res.next()) {
				mail.add(ConfigManager.messages.MAIL_FORMAT
						.replace("{sender}", res.getString("playername"))
						.replace("{message}", res.getString("message")));
			}
			res.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}

		return mail;
	}

	public void addMessage(String sender, String receiver, String message) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement addMessage = connectionHandler.getPreparedStatement("addMessage");
			addMessage.setString(1, sender);
			addMessage.setString(2, receiver);
			addMessage.setString(3, message);

			addMessage.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}
	}

	public void clearInbox(String player) {
		ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

		try {
			PreparedStatement clearInbox = connectionHandler.getPreparedStatement("clearInbox");
			clearInbox.setString(1, player);

			clearInbox.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionHandler.release();
		}
	}

	@Override
	public String[] getTable() {
		return new String[]{ConfigManager.main.Table_Mail,
				"time timestamp, " +
				"sender VARCHAR(100), " +
				"receiver VARCHAR(100), " +
				"message VARCHAR(255), " +
				"CONSTRAINT fk_sender FOREIGN KEY (sender) REFERENCES " + ConfigManager.main.Table_Players + " (uuid) ON UPDATE CASCADE ON DELETE CASCADE, " +
				"CONSTRAINT fk_receiver FOREIGN KEY (receiver) REFERENCES " + ConfigManager.main.Table_Players + " (uuid) ON UPDATE CASCADE ON DELETE CASCADE"
		};
	}

	@Override
	public void registerPreparedStatements(ConnectionHandler connection) {
		connection.addPreparedStatement("getMail",
				"SELECT " + ConfigManager.main.Table_Mail + ".message, " + ConfigManager.main.Table_Players + ".playername" +
				" FROM " + ConfigManager.main.Table_Mail +
				" LEFT JOIN " + ConfigManager.main.Table_Players +
				" ON " + ConfigManager.main.Table_Mail + ".receiver=" + ConfigManager.main.Table_Players + ".uuid" +
				" WHERE receiver = ? " +
				" ORDER BY " + ConfigManager.main.Table_Mail + ".time DESC" +
				" LIMIT ?, 9;");
		connection.addPreparedStatement("addMessage", "INSERT INTO " + ConfigManager.main.Table_Mail + " (time, sender, receiver, message) VALUES (NOW(), ?, ?, ?)");
		connection.addPreparedStatement("clearInbox", "DELETE FROM " + ConfigManager.main.Table_Mail + " WHERE receiver = ?");
	}

	@Override
	public void checkUpdate() {

	}
}
