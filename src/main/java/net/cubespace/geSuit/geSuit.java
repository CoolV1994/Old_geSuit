package net.cubespace.geSuit;

import net.cubespace.geSuit.commands.BanCommand;
import net.cubespace.geSuit.commands.DebugCommand;
import net.cubespace.geSuit.commands.MOTDCommand;
import net.cubespace.geSuit.commands.NamesCommand;
import net.cubespace.geSuit.commands.OnTimeCommand;
import net.cubespace.geSuit.commands.ReloadCommand;
import net.cubespace.geSuit.commands.SeenCommand;
import net.cubespace.geSuit.commands.TempBanCommand;
import net.cubespace.geSuit.commands.UnbanCommand;
import net.cubespace.geSuit.commands.WarnCommand;
import net.cubespace.geSuit.commands.WarnHistoryCommand;
import net.cubespace.geSuit.commands.WhereCommand;
import net.cubespace.geSuit.database.ConnectionHandler;
import net.cubespace.geSuit.listeners.*;
import net.cubespace.geSuit.managers.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class geSuit extends Plugin
{

    public static geSuit instance;
    public static ProxyServer proxy;
    private boolean DebugEnabled = false;

    public void onEnable()
    {
        instance = this;
        LoggingManager.log(ChatColor.GREEN + "Starting geSuit");
        proxy = ProxyServer.getInstance();
        LoggingManager.log(ChatColor.GREEN + "Initialising Managers");

        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();
        connectionHandler.release();

        registerListeners();
        registerCommands();
        GeoIPManager.initialize();
        ChatManager.defaultPrefixes();
        ChatManager.loadChannels();
    }

    private void registerCommands()
    {
        // A little hardcore. Prevent updating without a restart. But command squatting = bad!
        if (ConfigManager.main.MOTD_Enabled) {
            proxy.getPluginManager().registerCommand(this, new MOTDCommand());
        }
        if (ConfigManager.main.Seen_Enabled) {
            proxy.getPluginManager().registerCommand(this, new SeenCommand());
        }
        proxy.getPluginManager().registerCommand(this, new UnbanCommand());
        proxy.getPluginManager().registerCommand(this, new BanCommand());
        proxy.getPluginManager().registerCommand(this, new TempBanCommand());
        proxy.getPluginManager().registerCommand(this, new WarnCommand());
        proxy.getPluginManager().registerCommand(this, new WhereCommand());
        proxy.getPluginManager().registerCommand(this, new ReloadCommand());
        proxy.getPluginManager().registerCommand(this, new DebugCommand());
        proxy.getPluginManager().registerCommand(this, new WarnHistoryCommand());
        proxy.getPluginManager().registerCommand(this, new NamesCommand());
        if (ConfigManager.bans.TrackOnTime) {
        	proxy.getPluginManager().registerCommand(this, new OnTimeCommand());
        }
    }

    private void registerListeners()
    {
        getProxy().registerChannel("geSuitTeleport");       // Teleport out/in
        getProxy().registerChannel("geSuitSpawns");         // Spawns out/in
        getProxy().registerChannel("geSuitBans");           // Bans in
        getProxy().registerChannel("geSuitPortals");        // Portals out/in
        getProxy().registerChannel("geSuitWarps");          // Warps in
        getProxy().registerChannel("geSuitHomes");          // Homes in
        getProxy().registerChannel("geSuitChat");           // Chat out/in
        getProxy().registerChannel("geSuitAPI");            // API messages in

        proxy.getPluginManager().registerListener(this, new PlayerListener());
        proxy.getPluginManager().registerListener(this, new BansMessageListener());
        proxy.getPluginManager().registerListener(this, new TeleportsListener());
        proxy.getPluginManager().registerListener(this, new TeleportsMessageListener());
        proxy.getPluginManager().registerListener(this, new WarpsMessageListener());
        proxy.getPluginManager().registerListener(this, new HomesMessageListener());
        proxy.getPluginManager().registerListener(this, new PortalsMessageListener());
        proxy.getPluginManager().registerListener(this, new SpawnListener());
        proxy.getPluginManager().registerListener(this, new SpawnMessageListener());
        proxy.getPluginManager().registerListener(this, new ChatListener());
        proxy.getPluginManager().registerListener(this, new ChatMessageListener());
        proxy.getPluginManager().registerListener(this, new APIMessageListener());
    }

    public void onDisable()
    {
        DatabaseManager.connectionPool.closeConnections();
    }

	public boolean isDebugEnabled() {
		return DebugEnabled;
	}

	public void setDebugEnabled(boolean debugEnabled) {
		DebugEnabled = debugEnabled;
	}
	
	public void DebugMsg(String msg) {
		if (isDebugEnabled()) {
			geSuit.instance.getLogger().info("DEBUG: " + msg);
		}
	}
}