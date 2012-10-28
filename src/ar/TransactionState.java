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

			String args = BufferUtils.byteBufferToString(session.getClientBuffer()[1]);
			boolean validArgument = args.startsWith(" ") || args.startsWith("\n") || args.startsWith("\r");
	
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
					tmpState = new ListState(args);
					tmpState.setFlowToWriteServer();
					response.setState(tmpState);
				} else {
					response = invalidArgumentResponse(session);
				}
				
				break;
			case RETR:
				
				if(validArgument){
					response = super.afterReadingFromClient(session);
					tmpState = new RetrState(args);
					tmpState.setFlowToWriteServer();
					response.setState(tmpState);
				} else {
					response = invalidArgumentResponse(session);
				}

				break;
			case DELE:
				
				if(validArgument){
					response = super.afterReadingFromClient(session);
					tmpState = new DeleState(args);
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
				tmpState = new TopState(args);
				tmpState.setFlowToWriteServer();
				response.setState(tmpState);
				break;
			case UIDL:
				if(validArgument){
					response = super.afterReadingFromClient(session);
					tmpState = new UidlState(args);
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
		
		public String toString(){
			return "None";
		}
		
	}
	
	private class QuitState extends AbstractInnerState implements EndState{
		
		private boolean isFinalState = false;
		
		@Override
		Response afterWritingToClient(ClientSession session) {
			Response response = new Response();
			
			response = super.afterWritingToClient(session);
			this.isFinalState = true;
			response.setState(this);
			return response;
		
		}
		
		@Override
		public boolean isEndState() {
			return this.isFinalState;
		}

		public State getNextState() {
			return null;
		}
		public String toString(){
			return "Quit";
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
		public String toString(){
			return "Stat";
		}
	}

	private class ListState extends AbstractMultilinerInnerState{
		
		private String args;
		
		public ListState(String args) {
			this.args = args.trim();
		}
		
		@Override
		Response afterWritingToClient(ClientSession session){
			if(args.length() > 0){
				this.setWaitingLineFeedEnd(false);
			}
			Response response = super.afterWritingToClient(session);
			if(!this.isWaitingLineFeedEnd()){
				AbstractInnerState tmpState = new NoneState();
				tmpState.setFlowToReadClient();
				response.setState(tmpState);
			}
			return response;
		}
		public String toString(){
			return "List";
		}
	}
	
	private class RetrState extends AbstractMultilinerInnerState{
		
		private String args;
		
		public RetrState(String args) {
			this.args = args.trim();
		}
		
		@Override
		Response afterWritingToClient(ClientSession session){
			if(args.length() > 0){
				this.setWaitingLineFeedEnd(false);
			}
			Response response = super.afterWritingToClient(session);
			if(!this.isWaitingLineFeedEnd()){
				AbstractInnerState tmpState = new NoneState();
				tmpState.setFlowToReadClient();
				response.setState(tmpState);
			}
			return response;
		}
		public String toString(){
			return "Retr";
		}
		
	}
	
	private class DeleState extends AbstractInnerState{

		private String args;
		
		public DeleState(String args) {
			this.args = args.trim();
		}
		
		@Override
		Response afterWritingToClient(ClientSession session) {
			Response response = super.afterWritingToClient(session);
			AbstractInnerState tmpState = new NoneState();
			tmpState.setFlowToReadClient();
			response.setState(tmpState);
			return response;
		}
		public String toString(){
			return "Dele";
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
		public String toString(){
			return "Noop";
		}

	}
	
	private class UidlState extends AbstractMultilinerInnerState{

		private String args;
		
		public UidlState(String args) {
			this.args = args.trim();
		}
		
		@Override
		Response afterWritingToClient(ClientSession session){
			if(args.length() > 0){
				this.setWaitingLineFeedEnd(false);
			}
			Response response = super.afterWritingToClient(session);
			if(!this.isWaitingLineFeedEnd()){
				AbstractInnerState tmpState = new NoneState();
				tmpState.setFlowToReadClient();
				response.setState(tmpState);
			}
			return response;
		}
		public String toString(){
			return "Uidl";
		}
	}
	
	private class TopState extends AbstractMultilinerInnerState{
		
		private String args;
		
		public TopState(String args) {
			this.args = args.trim();
		}
		
		@Override
		Response afterWritingToClient(ClientSession session){
			if(args.length() > 0){
				this.setWaitingLineFeedEnd(false);
			}
			Response response = super.afterWritingToClient(session);
			if(!this.isWaitingLineFeedEnd()){
				AbstractInnerState tmpState = new NoneState();
				tmpState.setFlowToReadClient();
				response.setState(tmpState);
			}
			return response;
		}
		public String toString(){
			return "Top";
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
		
		public String toString(){
			return "Rset";
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
	
	public String toString(){
		return "TRANS("+this.currentState+")";
	}



}
