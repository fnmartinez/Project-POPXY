package ar.Filters;

import java.nio.channels.Channel;

import ar.POPXY;

public class NetFilter{

	private POPXY proxy;
	
	public NetFilter(){
		this.proxy = POPXY.getInstance();
	}
	
//    public boolean ipIsDenied(String ip) {
//    	List<String> ipBlackList = proxy.getBlack
//        if (ipBlackList != null && ip != null && !ip.equals("")) {
//                return ipBlackList.contains(ip);
//        }
//        return false;
//}
	
}
