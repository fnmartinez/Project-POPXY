package ar.sessions.utils;

import java.util.HashMap;
import java.util.Map;

public enum POPHeadCommands {
	USER,
	PASS,
	QUIT,
	STAT,
	LIST,
	RETR,
	DELE,
	NOOP,
	RSET,
	TOP,
	UIDL,
	NONE,
	UKWN;
	
	private static Map<String, POPHeadCommands> reverseStringSearchMap;
	private static boolean firstCall = false;
	
	
	public static POPHeadCommands getLiteralByString(String str) {
		if(firstCall) {
			for(POPHeadCommands phc: POPHeadCommands.values()){
				reverseStringSearchMap.put(phc.toString(), phc);
			}
		}
		
		return reverseStringSearchMap.get(str.toUpperCase());
	}
}
