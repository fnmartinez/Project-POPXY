package ar;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import ar.sessions.ClientSession;
import ar.sessions.utils.BufferUtils;
import ar.sessions.utils.POPHeadCommands;

public class TransactionState implements State {
	
	private State currentState;
	
	public TransactionState(){
		super();
		this.currentState = new NoneState();
	}
	
	private class NoneState extends AbstractInnerState {
		

		@Override
		Response afterReadingFromClient(ClientSession session) {
			
			Response response = new Response();
			
			POPHeadCommands cmd = POPHeadCommands.getLiteralByString(BufferUtils.byteBufferToString(session.getClientBuffer()[0]));

			String firstCaracter = BufferUtils.byteBufferToString(session.getClientBuffer()[1]);
			boolean validArgument = firstCaracter.startsWith(" ") || firstCaracter.startsWith("\n");
	
			AbstractInnerState tmpState;

			switch(cmd) {
			case STAT:
				
				if(validArgument){
					response = super.afterReadingFromClient(session);
					tmpState = new StatState();
					tmpState.setFlowToWriteServer();
					response.setState(tmpState);
				} else {
					response = invalidArgumentResponse(session);
				}
				
				break;
			case LIST:
				
				if(validArgument){
					response = super.afterReadingFromClient(session);
					tmpState = new ListState();
					tmpState.setFlowToWriteServer();
					response.setState(tmpState);
				} else {
					response = invalidArgumentResponse(session);
				}
				
				break;
			case RETR:
				
				if(validArgument){
					response = super.afterReadingFromClient(session);
					tmpState = new RetrState();
					tmpState.setFlowToWriteServer();
					response.setState(tmpState);
				} else {
					response = invalidArgumentResponse(session);
				}

				break;
			case DELE:
				
				if(validArgument){
					response = super.afterReadingFromClient(session);
					tmpState = new DeleState();
					tmpState.setFlowToWriteServer();
					response.setState(tmpState);
				} else {
					response = invalidArgumentResponse(session);
				}
				
				break;
			case NOOP:
				
				if(validArgument){
					response = super.afterReadingFromClient(session);
					tmpState = new NoopState();
					tmpState.setFlowToWriteServer();
					response.setState(tmpState);
				} else {
					response = invalidArgumentResponse(session);
				}
				
				break;
			case RSET:
				
				if(validArgument){
					response = super.afterReadingFromClient(session);
					tmpState = new RsetState();
					tmpState.setFlowToWriteServer();
					response.setState(tmpState);
				} else {
					response = invalidArgumentResponse(session);
				}
				
				break;
			case TOP:
				response = super.afterReadingFromClient(session);
				tmpState = new TopState();
				tmpState.setFlowToWriteServer();
				response.setState(tmpState);
				break;
			case UIDL:
				if(validArgument){
					response = super.afterReadingFromClient(session);
					tmpState = new UidlState();
					tmpState.setFlowToWriteServer();
					response.setState(tmpState);
				} else {
					response = invalidArgumentResponse(session);
				}
				break;
			case QUIT:
				if(validArgument){
					response = super.afterReadingFromClient(session);
					tmpState = new QuitState();
					tmpState.setFlowToWriteServer();
					response.setState(tmpState);
				} else {
					response = invalidArgumentResponse(session);
				}
				break;
			default:
				response = super.afterReadingFromClient(session);
				break;
			}
			
			return response;
		}
		
		
		private Response invalidArgumentResponse(ClientSession session) {
			Response response = new Response();
			ByteBuffer[] bufferToUse = session.getFirstServerBuffer();
			for(ByteBuffer bf: bufferToUse) {
				bf.clear();
			}
			bufferToUse = session.getFirstServerBuffer();
			bufferToUse[0].clear();
			bufferToUse[1].clear();
			
			bufferToUse[0].put("-ERR".getBytes());
			bufferToUse[1].put(" Invalid command.\r\n".getBytes());
			
			bufferToUse[0].flip();
			bufferToUse[1].flip();
			
			response.setBuffers(bufferToUse);
			response.setChannel(session.getClientSocket());
			response.setOperation(SelectionKey.OP_WRITE);
			response.setState(this);
			this.setFlowToWriteClient();
			
			return response;
		}


		public boolean isEndState() {
			return false;
		}
		
	}
	
	private class QuitState extends AbstractInnerState implements EndState{
		
		@Override
		public boolean isEndState() {
			return true;
		}

		public State getNextState() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private class StatState extends AbstractInnerState{
		
		@Override
		Response afterWritingToClient(ClientSession session) {
			Response response = super.afterWritingToClient(session);
			AbstractInnerState tmpState = new NoneState();
			tmpState.setFlowToReadClient();
			response.setState(tmpState);
			return response;
		}

	}

	private class ListState extends AbstractMultilinerInnerState{
		@Override
		Response afterWritingToClient(ClientSession session){
			Response response = super.afterWritingToClient(session);
			if(response.isEndOfChainResponse()){
				AbstractInnerState tmpState = new NoneState();
				tmpState.setFlowToReadClient();
				response.setState(tmpState);
			}
			return response;
		}
	}
	
	private class RetrState extends AbstractMultilinerInnerState{
		@Override
		Response afterWritingToClient(ClientSession session){
			Response response = super.afterWritingToClient(session);
			if(response.isEndOfChainResponse()){
				AbstractInnerState tmpState = new NoneState();
				tmpState.setFlowToReadClient();
				response.setState(tmpState);
			}
			return response;
		}
		
	}
	
	private class DeleState extends AbstractInnerState{

		@Override
		Response afterWritingToClient(ClientSession session) {
			Response response = super.afterWritingToClient(session);
			AbstractInnerState tmpState = new NoneState();
			tmpState.setFlowToReadClient();
			response.setState(tmpState);
			return response;
		}
	
	}
	
	private class NoopState extends AbstractInnerState{
		
		@Override
		Response afterWritingToClient(ClientSession session) {
			Response response = new Response();
			response = super.afterWritingToClient(session);
			AbstractInnerState tmpState = new NoneState();
			tmpState.setFlowToReadClient();
			response.setState(tmpState);
			return response;
		}

	}
	
	private class UidlState extends AbstractMultilinerInnerState{

		@Override
		Response afterWritingToClient(ClientSession session){
			Response response = super.afterWritingToClient(session);
			if(response.isEndOfChainResponse()){
				AbstractInnerState tmpState = new NoneState();
				tmpState.setFlowToReadClient();
				response.setState(tmpState);
			}
			return response;
		}
		
	}
	
	private class TopState extends AbstractMultilinerInnerState{
		
		@Override
		Response afterWritingToClient(ClientSession session){
			Response response = super.afterWritingToClient(session);
			if(response.isEndOfChainResponse()){
				AbstractInnerState tmpState = new NoneState();
				tmpState.setFlowToReadClient();
				response.setState(tmpState);
			}
			return response;
		}

	}
	
	private class RsetState extends AbstractInnerState{
		
		@Override
		Response afterWritingToClient(ClientSession session) {
			Response response = new Response();
			response = super.afterWritingToClient(session);
			AbstractInnerState tmpState = new NoneState();
			tmpState.setFlowToReadClient();
			response.setState(tmpState);
			return response;
		}

	}
	
	public Response eval(ClientSession session) {
		Response response = this.currentState.eval(session);
		this.currentState = response.getState();
		
		if(this.currentState.isEndState()){
			response.setState(((EndState)this.currentState).getNextState());
		} else {
			response.setState(this);
		}

		return response;
	}

	public boolean isEndState() {
		return false;
	}



}
