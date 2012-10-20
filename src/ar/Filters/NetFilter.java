package ar.Filters;

import java.nio.channels.Channel;

import ar.POPXY;

public class NetFilter {

	private POPXY proxy;

	public NetFilter() {
		this.proxy = POPXY.getInstance();
	}
	
	public boolean filter(String ip){
		return proxy.isOnTheBlackList(ip);
	}

}
