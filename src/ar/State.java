package ar;

import ar.sessions.ClientSession;

public interface State {
	
	public Response eval(ClientSession session);
	
	public boolean isEndState();


}
