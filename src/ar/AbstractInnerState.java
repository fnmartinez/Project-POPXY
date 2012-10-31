package ar;

import java.nio.channels.SelectionKey;
import ar.Action;
import ar.sessions.ClientSession;

public abstract class AbstractInnerState implements State{
	
	private AbstractInnerState callbackState;

	private Object attachment = null;
	
	private FlowDirection flowDirection = FlowDirection.WRITE_CLIENT; 
	
	public AbstractInnerState(AbstractInnerState callback) {
		this.callbackState = callback;
	}

	public Action eval(ClientSession session) {
		
		/* Look up for the last action done */
		switch(flowDirection){
		case READ_CLIENT: 	return afterReadingFromClient(session);	
		case READ_SERVER:	return afterReadingFromServer(session);
		case WRITE_CLIENT:	return afterWritingToClient(session);
		case WRITE_SERVER:	return afterWritingToServer(session);
		
		}
		return null;
	}
	
	public void setFlowToReadClient(){
		this.flowDirection = FlowDirection.READ_CLIENT;
	}
	
	public void setFlowToReadServer(){
		this.flowDirection = FlowDirection.READ_SERVER;
	}
	
	public void setFlowToWriteClient(){
		this.flowDirection = FlowDirection.WRITE_CLIENT;
	}
	
	public void setFlowToWriteServer(){
		this.flowDirection = FlowDirection.WRITE_SERVER;
	}
	
	public void setFlowToWriteFile() {
		this.flowDirection = FlowDirection.WRITE_FILE;
		
	}
	
	public void setFlowToReadFile() {
		this.flowDirection = FlowDirection.READ_FILE;
	}

	// Once the server was written, this function is called
	// in order to set it to read from the server channel for
	// its response.
	Action afterWritingToServer(ClientSession session) {
		Action response = new Action();
		response.setBuffers(session.getFirstServerBuffer());
		response.setChannel(session.getOriginServerSocket());
		response.setOperation(SelectionKey.OP_READ);
		response.setState(this);
		this.flowDirection = FlowDirection.READ_SERVER;
		return response;
	}

	// Once the client was written, this function is called
	// to read from the client the next instruction.
	Action afterWritingToClient(ClientSession session) {
		Action response = new Action();
		response.setBuffers(session.getClientBuffer());
		response.setChannel(session.getClientSocket());
		response.setOperation(SelectionKey.OP_READ);
		response.setState(this);
		this.flowDirection = FlowDirection.READ_CLIENT;
		return response;
	}

	// Once the server was read, we change to write the 
	// client.
	Action afterReadingFromServer(ClientSession session) {
		Action response = new Action();
		response.setBuffers(session.getFirstServerBuffer());
		response.setChannel(session.getClientSocket());
		response.setOperation(SelectionKey.OP_WRITE);
		response.setState(this);
		this.flowDirection = FlowDirection.WRITE_CLIENT;
		return response;
	}

	// Once the client was read, we change to write the
	// server.
	Action afterReadingFromClient(ClientSession session) {
		Action response = new Action();
		response.setBuffers(session.getClientBuffer());
		response.setChannel(session.getOriginServerSocket());
		response.setOperation(SelectionKey.OP_WRITE);
		response.setState(this);
		this.flowDirection = FlowDirection.WRITE_SERVER;
		return response;
	}
	
	public abstract InnerStateAction callbackEval(AbstractInnerState s);
	
	public boolean isEndState() {
		return false;
	}
	public FlowDirection getFlowDirection() {
		return this.flowDirection;
	}
	
	public void setAttachment(Object o) {
		this.attachment = o;
	}
	
	public Object getAttachment() {
		return this.attachment;
	}
	
	public AbstractInnerState getCallbackState() {
		return this.callbackState;
	}
}

