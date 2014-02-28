package net.cubespace.getSuit.configs;

import net.cubespace.getSuit.BungeeSuite;
import net.cubespace.Yamler.Config.Config;

import java.io.File;

public class SpawnConfig extends Config {
    public SpawnConfig() {
        CONFIG_FILE = new File(BungeeSuite.instance.getDataFolder(), "spawns.yml");
    }

	public Boolean SpawnNewPlayerAtNewspawn = false;
	public Boolean ForceAllPlayersToProxySpawn = false;
}