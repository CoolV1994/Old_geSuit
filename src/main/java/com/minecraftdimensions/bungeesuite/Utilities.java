package com.minecraftdimensions.bungeesuite;

import com.google.common.net.InetAddresses;

public class Utilities {
	public static boolean isIPAddress(String ip){
		return InetAddresses.isInetAddress(ip);
	}
}
