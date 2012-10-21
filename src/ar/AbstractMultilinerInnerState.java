package ar;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import ar.sessions.ClientSession;

public abstract class AbstractMultilinerInnerState extends AbstractInnerState {

	private boolean waitingLineFeedEnd = false;
	
	@Override
	Response afterReadingFromServer(ClientSession session) {
		
		Response response = new Response();
		
		if(!waitingLineFeedEnd){
			response = super.afterReadingFromServer(session);
			if(session.getFirstServerBuffer()[0].toString().trim().compareToIgnoreCase("-ERR") == 0) {
				waitingLineFeedEnd = false;
			} else {
				waitingLineFeedEnd = true;
			}
			
		} else {
			ByteBuffer mlsb = session.getSecondServerBuffer();
			response = new Response();
			response.setMultilineBuffer(mlsb);
			response.setMultilineResponse(true);
			this.setFlowToWriteClient();
			if(mlsb.toString().endsWith("/r/n./r/n")){
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
		response.setChannel(session.getOriginServerSocket());
		response.setOperation(SelectionKey.OP_READ);
		this.setFlowToReadServer();
		return response;
	}
}
