package net.cubespace.geSuit.configs.SubConfig;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Config;

/**
 * Created by Vinnie on 6/28/2015.
 */
public class ServerChannel extends Config {
	@Comment("The Server Name")
	public String serverName;
	@Comment("One letter ID for server")
	public String shortName;

	@Comment("Format for server channel")
	public String serverFormat;
	@Comment("Format for local channel")
	public String localForamt;

	@Comment("Force chat channel?")
	public boolean forceChannel;
	@Comment("Channel to force")
	public String forcedChannel;
	@Comment("Radius for local chat")
	public int localDistance;
	@Comment("Display login/logout messages")
	public boolean connectionMessages;

	public boolean usingFactionChannels;
	public boolean usingTowny;

	public String getForcedChannel() {
		if ( forcedChannel.equalsIgnoreCase( "server" ) ) {
			return serverName;
		} else if ( forcedChannel.equalsIgnoreCase( "global" ) ) {
			return "Global";
		} else if ( forcedChannel.equalsIgnoreCase( "local" ) ) {
			return serverName + " Local";
		} else {
			return forcedChannel;
		}
	}
}
