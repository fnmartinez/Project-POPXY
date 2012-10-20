package ar;

import ar.sessions.ClientSession;

public class AuthState implements State {
	
	State currentState;

	public AuthState(){
		super();
		this.currentState = new NoneState();
	}
	
	
	private class NoneState extends AbstractInnerState{

		@Override
		Response readFromClient(ClientSession session) {
			return null;
		}
	}


	public Response eval(ClientSession session) {
		// TODO Auto-generated method stub
		return null;
	}

}
