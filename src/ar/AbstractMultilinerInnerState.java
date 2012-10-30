package ar;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import ar.sessions.ClientSession;
import ar.sessions.utils.BufferUtils;

public abstract class AbstractMultilinerInnerState extends AbstractInnerState {


	private boolean waitingLineFeedEnd = false;
	
	public AbstractMultilinerInnerState(AbstractInnerState callback) {
		super(callback);
	}

	@Override
	Response afterReadingFromServer(ClientSession session) {
		
		Response response = new Response();
		
		if(!waitingLineFeedEnd){
			response = super.afterReadingFromServer(session);
			if(BufferUtils.byteBufferToString(session.getFirstServerBuffer()).startsWith("-ERR") 
			|| BufferUtils.byteBufferToString(session.getFirstServerBuffer()).endsWith("\r\n.\r\n")) {
				waitingLineFeedEnd = false;
			} else {
				waitingLineFeedEnd = true;
			}
			
		} else {
			ByteBuffer mlsb = session.getSecondServerBuffer();			
			response.setChannel(session.getClientSocket());
			response.setOperation(SelectionKey.OP_WRITE);
			response.setState(this);
			response.setMultilineBuffer(mlsb);
			response.setMultilineResponse(true);
			this.setFlowToWriteClient();
			if(BufferUtils.byteBufferToString(mlsb).contains("\r\n.\r\n")){
				this.waitingLineFeedEnd = false;
			}
		}
		
		return response;
	}
	
	@Override
	Response afterWritingToClient(ClientSession session) {
		
		Response response;
		
		if(!waitingLineFeedEnd){
			return super.afterWritingToClient(session);
		}
		
		response = new Response();
		response.setMultilineBuffer(session.getSecondServerBuffer());
		response.setMultilineResponse(true);
		response.setState(this);
		response.setChannel(session.getOriginServerSocket());
		response.setOperation(SelectionKey.OP_READ);
		this.setFlowToReadServer();
		return response;
	}
	
	boolean isWaitingLineFeedEnd() {
		return this.waitingLineFeedEnd;
	}
	
	void setWaitingLineFeedEnd(boolean bool){
		this.waitingLineFeedEnd = bool;
	}
	
}
