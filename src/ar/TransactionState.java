package ar;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import ar.elements.MailParser;
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
			String command = BufferUtils.byteBufferToString(session.getClientBuffer()).trim();
			if(command.length() >= 5){
				command = command.substring(0, 5);
			}
			POPHeadCommands cmd = POPHeadCommands.getLiteralByString(command);

			String args = BufferUtils.byteBufferToString(session.getClientBuffer()).substring(4);
	
			AbstractInnerState tmpState = null;

			response = super.afterReadingFromClient(session);
			switch(cmd) {
			case STAT:
				tmpState = new StatState();
				break;
			case LIST:
				tmpState = new ListState(args);
				break;
			case RETR:
				tmpState = new RetrState();
				break;
			case DELE:
				tmpState = new DeleState(args);
				break;
			case NOOP:
				tmpState = new NoopState();
				break;
			case RSET:
				tmpState = new RsetState();
				break;
			case TOP:
				tmpState = new TopState();
				break;
			case UIDL:
				tmpState = new UidlState(args);
				break;
			case QUIT:
				tmpState = new QuitState();
				break;
			default:
				response = super.afterReadingFromClient(session);
				break;
			}
			tmpState.setFlowToWriteServer();
			response.setState(tmpState);
			
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
		
		private String tmpMailPart;
		private boolean statusIssued;
		
		@Override
		public Response eval(ClientSession session) {
			
			/* Look up for the last action done */
			switch(this.getFlowDirection()){	
			case READ_FILE:	return afterReadingFromFile(session);
			case WRITE_FILE:return afterWritingToFile(session);
			default: return super.eval(session);
			
			}
		}

		private Response afterReadingFromFile(ClientSession session) {
			Response response = new Response();
			ByteBuffer mlsb = session.getSecondServerBuffer();			
			response.setChannel(session.getClientSocket());
			response.setOperation(SelectionKey.OP_WRITE);
			response.setState(this);
			response.setMultilineBuffer(mlsb);
			response.setMultilineResponse(true);
			this.setFlowToWriteClient();
			if(BufferUtils.byteBufferToString(mlsb).endsWith("\r\n.\r\n")){
				this.setWaitingLineFeedEnd(false);
			}
			return response;
		}

		private Response afterWritingToFile(ClientSession session) {

			if(this.isWaitingLineFeedEnd()){
				Response response = super.afterWritingToClient(session);
				return response;
			}
			
			if(session.getClient().hasExternalApps()){
				session.getClient().getExternalProcessChain().process(session.getFile1(), session.getClient().getUser(), ".mail");
			}

			try {
				session.getFile1().seek(0);
				if(session.getClient().hasTransformations()){
					MailParser parser = new MailParser(session.getFile1(), session.getFile2(), session.getClient());
					parser.parseMessage();
					session.getFile2().seek(0);
				} else {
					session.setFile2(session.getFile1());
					session.getFile2().seek(0);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Response response = new Response();
			response.setOperation(SelectionKey.OP_READ);
			response.setState(this);
			response.setChannel(session.getFile2Channel());
			response.setMultilineBuffer(session.getSecondServerBuffer());
			response.setMultilineResponse(true);
			this.setFlowToReadFile();
			this.setWaitingLineFeedEnd(true);
			return response;
		}

		@Override
		Response afterReadingFromServer(ClientSession session){
			
			Response response = null;

			if(!this.isWaitingLineFeedEnd()){
				response = super.afterReadingFromServer(session);
				if(this.isWaitingLineFeedEnd()) {
					String responseToClient = BufferUtils.byteBufferToString(response.getBuffers()).split("\\r\\n")[0];
					tmpMailPart = BufferUtils.byteBufferToString(response.getBuffers()).substring(responseToClient.length()+2);
					response.getBuffers().clear();
					response.getBuffers().put((responseToClient+"\r\n").getBytes());
					response.getBuffers().flip();
					this.statusIssued = false;
				}
//				this.setFlowToWriteFile();
				return response;
			} 
			
			response = new Response();

			if(tmpMailPart != null) {
				session.getSecondServerBuffer().clear();
				session.getSecondServerBuffer().put(tmpMailPart.getBytes());
				session.getSecondServerBuffer().flip();
				tmpMailPart = null;
			}
			
			response.setChannel(session.getFile1Channel());
			
			ByteBuffer mlsb = session.getSecondServerBuffer();			
			
			response.setOperation(SelectionKey.OP_WRITE);
			response.setState(this);
			response.setMultilineBuffer(mlsb);
			response.setMultilineResponse(true);
			this.setFlowToWriteFile();
			
			if(BufferUtils.byteBufferToString(mlsb).contains("\r\n.\r\n")){
				this.setWaitingLineFeedEnd(false);
			}
			
			return response;
		}
		

		@Override
		Response afterWritingToClient(ClientSession session){
			Response response = super.afterWritingToClient(session);
			if(!this.isWaitingLineFeedEnd()){
				AbstractInnerState tmpState = new NoneState();
				tmpState.setFlowToReadClient();
				response.setState(tmpState);
				return response;
			}
			
			if(!this.statusIssued) {
				this.statusIssued = true;
				response.setChannel(null);
				return response;
			}
			
			response.setChannel(session.getFile2Channel());
			this.setFlowToReadFile();
			
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
		
		@Override
		Response afterWritingToClient(ClientSession session){
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
