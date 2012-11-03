package ar;

import ar.sessions.ClientSession;

public interface State {
	
	public Action eval(ClientSession session, Action a);
	
	public boolean isEndState();


}
