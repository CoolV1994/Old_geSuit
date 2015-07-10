package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.database.*;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class DatabaseManager {
	public static ConnectionPool connectionPool;
	public static Players players;
	public static Ignores ignores;
	public static Homes homes;
	public static Bans bans;
	public static Portals portals;
	public static Spawns spawns;
	public static Warps warps;
	public static Tracking tracking;
	public static OnTime ontime;

	static {
		players = new Players();
		ignores = new Ignores();
		homes = new Homes();
		bans = new Bans();
		portals = new Portals();
		spawns = new Spawns();
		warps = new Warps();
		tracking = new Tracking();
		ontime = new OnTime();

		connectionPool = new ConnectionPool();
		connectionPool.addRepository(players);
		connectionPool.addRepository(ignores);
		connectionPool.addRepository(homes);
		connectionPool.addRepository(bans);
		connectionPool.addRepository(portals);
		connectionPool.addRepository(spawns);
		connectionPool.addRepository(warps);
		connectionPool.addRepository(tracking);
		connectionPool.addRepository(ontime);
		connectionPool.initialiseConnections(ConfigManager.main.Database);

		AnnouncementManager.loadAnnouncements();
		WarpsManager.loadWarpLocations();
		PortalManager.loadPortals();
		SpawnManager.loadSpawns();
	}
}
