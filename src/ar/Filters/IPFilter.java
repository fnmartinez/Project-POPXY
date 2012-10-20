package ar.Filters;

import java.nio.channels.Channel;

import ar.POPXY;

public class IPFilter extends NetFilter{

	private POPXY proxy;
	
	public IPFilter(){
		this.proxy = POPXY.getInstance();
	}
	
	public boolean filter(Channel ch) {
		
		return false;
	}
	
	
}
