package ar;

import java.nio.channels.SelectionKey;

import ar.sessions.ClientSession;

public abstract class AbstractInnerState implements State{
	
	private FlowDirection flowDirection; 

	public Response eval(ClientSession session) {
		
		switch(flowDirection){
		case READ_CLIENT: 	return readFromClient(session);	
		case READ_SERVER:	return readFromServer(session);
		case WRITE_CLIENT:	return writeToClient(session);
		case WRITE_SERVER:	return writeToServer(session);
		
		}
		return null;
	}

	Response writeToServer(ClientSession session) {
		Response response = new Response();
		response.setBuffers(session.getFirstServerBuffer());
		response.setChannel(session.getOriginServerSocket());
		response.setOperation(SelectionKey.OP_READ);
		response.setState(this);
		this.flowDirection = FlowDirection.READ_SERVER;
		return response;
	}

	Response writeToClient(ClientSession session) {
		Response response = new Response();
		response.setBuffers(session.getClientBuffer());
		response.setChannel(session.getClientSocket());
		response.setOperation(SelectionKey.OP_READ);
		response.setState(this);
		this.flowDirection = FlowDirection.READ_CLIENT;
		return response;
	}

	Response readFromServer(ClientSession session) {
		Response response = new Response();
		response.setBuffers(session.getFirstServerBuffer());
		response.setChannel(session.getClientSocket());
		response.setOperation(SelectionKey.OP_WRITE);
		response.setState(this);
		this.flowDirection = FlowDirection.WRITE_CLIENT;
		return response;
	}

	Response readFromClient(ClientSession session) {
		Response response = new Response();
		response.setBuffers(session.getClientBuffer());
		response.setChannel(session.getOriginServerSocket());
		response.setOperation(SelectionKey.OP_WRITE);
		response.setState(this);
		this.flowDirection = FlowDirection.WRITE_SERVER;
		return response;
	}

}
