package ar;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import ar.sessions.ClientSession;
import ar.sessions.utils.BufferUtils;
import ar.Action;

public abstract class AbstractMultilinerInnerState extends AbstractInnerState {


	private boolean waitingLineFeedEnd = false;
	
	public AbstractMultilinerInnerState(AbstractInnerState callback) {
		super(callback);
	}

	@Override
	Action afterReadingFromServer(ClientSession session) {
		
		Action response = new Action();
		
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
			response.setBuffers(session.getSecondServerBuffer());
			this.setFlowToWriteClient();
			if(BufferUtils.byteBufferToString(mlsb).contains("\r\n.\r\n")){
				this.waitingLineFeedEnd = false;
			}
		}
		
		return response;
	}
	
	@Override
	Action afterWritingToClient(ClientSession session) {
		
		Action response;
		
		if(!waitingLineFeedEnd){
			return super.afterWritingToClient(session);
		}
		
		response = new Action();
		response.setBuffers(session.getSecondServerBuffer());
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
