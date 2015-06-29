package net.cubespace.geSuit.database;

import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.objects.Warp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vinnie on 6/28/2015.
 */
public class Ignores implements IRepository {
    public List<String> getIgnores(String player) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();
        List<String> ignores = new ArrayList<>();

        try {
            PreparedStatement getIgnores = connectionHandler.getPreparedStatement("getIgnores");
            getIgnores.setString(1, player);

            ResultSet res = getIgnores.executeQuery();
            while (res.next()) {
                ignores.add(res.getString("ignoring"));
            }
            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }

        return ignores;
    }

    public void addIgnore(String player, String ignore) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement addIgnore = connectionHandler.getPreparedStatement("addIgnore");
            addIgnore.setString(1, player);
            addIgnore.setString(2, ignore);

            addIgnore.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    public void removeIgnore(String player, String ignore) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement removeIgnore = connectionHandler.getPreparedStatement("deleteWarp");
            removeIgnore.setString(1, player);
            removeIgnore.setString(2, ignore);

            removeIgnore.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    @Override
    public String[] getTable() {
        return new String[]{ConfigManager.main.Table_Ignores,
                "player VARCHAR(100), " +
                "ignoring VARCHAR(100), " +
                "CONSTRAINT pk_ignored PRIMARY KEY (player,ignoring), " +
                "CONSTRAINT fk_player FOREIGN KEY (player) REFERENCES "+ ConfigManager.main.Table_Players +" (uuid) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "CONSTRAINT fk_ignored FOREIGN KEY (ignoring) REFERENCES "+ ConfigManager.main.Table_Players +" (uuid) ON UPDATE CASCADE ON DELETE CASCADE"
        };
    }

    @Override
    public void registerPreparedStatements(ConnectionHandler connection) {
        connection.addPreparedStatement("getIgnores", "SELECT ignoring FROM "+ ConfigManager.main.Table_Ignores +" WHERE player = ?");
        connection.addPreparedStatement("addIgnore", "INSERT INTO "+ ConfigManager.main.Table_Warps +" (player, ignoring) VALUES (?, ?)");
        connection.addPreparedStatement("removeIgnore", "DELETE FROM "+ ConfigManager.main.Table_Warps +" WHERE player = ? AND ignoring = ?");
    }

    @Override
    public void checkUpdate() {

    }
}
