package ar;

import ar.sessions.ClientSession;

public interface State {
	
	public Action eval(ClientSession session);
	
	public boolean isEndState();


}
