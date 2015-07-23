package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.objects.GSPlayer;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Vinnie on 7/23/2015.
 */
public class MailManager {
	public static void sendMessage(GSPlayer p, String receiver, String message) throws SQLException {
		GSPlayer target = PlayerManager.matchOnlinePlayer(receiver);
		String targetUUID;
		if (target != null) {
			targetUUID = target.getUuid();
		} else {
			targetUUID = Utilities.getUUID(receiver);
		}
		if (PlayerManager.playerExists(targetUUID)) {
			DatabaseManager.mail.addMessage(p.getUuid(), targetUUID, message);
			p.sendMessage(ConfigManager.messages.PLAYER_MAIL_SENT.replace("{player}", receiver));
		} else {
			p.sendMessage(ConfigManager.messages.PLAYER_DOES_NOT_EXIST);
		}
		// TODO: Add social spy
	}

	public static void getMail(GSPlayer p, int page) throws SQLException {
		List<String> mail = DatabaseManager.mail.getMail(p.getUuid(), page * 9);
		if (mail.isEmpty()) {
			p.sendMessage(ConfigManager.messages.PLAYER_INBOX_EMPTY);
		}
		for (String message : mail) {
			p.sendMessage(message);
		}
	}

	public static void clearInbox(GSPlayer p) throws SQLException {
		DatabaseManager.mail.clearInbox(p.getUuid());
		p.sendMessage(ConfigManager.messages.PLAYER_INBOX_CLEARED);
	}
}
