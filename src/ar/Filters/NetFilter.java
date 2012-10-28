package ar.Filters;

import java.nio.channels.Channel;

import ar.POPXY;

public class NetFilter {

	public static boolean applyFilters(String ip){
		return POPXY.getInstance().isOnTheBlackList(ip);
	}

}
