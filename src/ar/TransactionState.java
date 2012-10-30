package ar;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;

import ar.elements.MailParser;
import ar.sessions.ClientSession;
import ar.sessions.utils.BufferUtils;
import ar.sessions.utils.POPHeadCommands;

public class TransactionState implements State {
	
	private State currentState;
	
	public TransactionState(){
		super();
		this.currentState = new NoneState(null);
	}
	
	private class NoneState extends AbstractInnerState {
		

		public NoneState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}

		@Override
		Response afterReadingFromClient(ClientSession session) {
			
			Response response = new Response();
			String command = BufferUtils.byteBufferToString(session.getClientBuffer()).trim();
			if(command.length() >= 5){
				command = command.substring(0, 4);
			}
			POPHeadCommands cmd = POPHeadCommands.getLiteralByString(command);

			String args = BufferUtils.byteBufferToString(session.getClientBuffer()).substring(4);
	
			AbstractInnerState tmpState = null;

			response = super.afterReadingFromClient(session);
			switch(cmd) {
			case STAT:
				tmpState = new StatState(this);
				break;
			case LIST:
				tmpState = new ListState(this, args);
				break;
			case RETR:
				tmpState = new RetrState(this);
				break;
			case DELE:
				tmpState = new DeleState(this, args);
				break;
			case NOOP:
				tmpState = new NoopState(this);
				break;
			case RSET:
				tmpState = new RsetState(this);
				break;
			case TOP:
				tmpState = new TopState(this);
				break;
			case UIDL:
				tmpState = new UidlState(this, args);
				break;
			case QUIT:
				tmpState = new QuitState(this);
				break;
			case UKWN:
			default:
				return response = invalidCommand(session);
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
		
		private Response invalidCommand(ClientSession session) {
			Response response = new Response();
			ByteBuffer bufferToUse = session.getFirstServerBuffer();
			bufferToUse.clear();
			
			bufferToUse.put("-ERR Invalid command.\r\n".getBytes());
			
			bufferToUse.flip();
			
			response.setBuffers(bufferToUse);
			response.setChannel(session.getClientSocket());
			response.setOperation(SelectionKey.OP_WRITE);
			response.setState(this);
			this.setFlowToWriteClient();
			return response;
		}

		@Override
		public void callbackFunction() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class QuitState extends AbstractInnerState implements EndState{
		
		public QuitState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}
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

		@Override
		public void callbackFunction() {
			// TODO Auto-generated method stub
			
		}
		


	}

	private class StatState extends AbstractInnerState{
		
		public StatState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}
		@Override
		Response afterWritingToClient(ClientSession session) {
			Response response = super.afterWritingToClient(session);
			AbstractInnerState tmpState;
			if(this.getCallbackState() == null) {
				tmpState = new NoneState(null);
			} else {
				tmpState = this.getCallbackState();
			}
			tmpState.setFlowToReadClient();
			response.setState(tmpState);
			return response;
		}
		public String toString(){
			return "Stat";
		}
		@Override
		public void callbackFunction() {
			// TODO Auto-generated method stub
			
		}
	}

	private class ListState extends AbstractMultilinerInnerState{
		
		private String args;
		
		public ListState(AbstractInnerState callback, String args) {
			super(callback);
			this.args = args.trim();
		}
		
		@Override
		Response afterWritingToClient(ClientSession session){
			if(args.length() > 0){
				this.setWaitingLineFeedEnd(false);
			}
			Response response = super.afterWritingToClient(session);
			if(!this.isWaitingLineFeedEnd()){
				AbstractInnerState tmpState;
				if(this.getCallbackState() == null) {
					tmpState = new NoneState(null);
				} else {
					tmpState = this.getCallbackState();
				}
				tmpState.setFlowToReadClient();
				response.setState(tmpState);
			}
			return response;
		}
		public String toString(){
			return "List";
		}

		@Override
		public void callbackFunction() {
			// TODO Auto-generated method stub
			
		}
	}
	
	private class RetrState extends AbstractMultilinerInnerState{
		
		private File incomingMail;
		private File outcomingMail;
		private RandomAccessFile incomingMailRAF;
		private FileChannel incomingFileChannel;
		private RandomAccessFile outcomingMailRAF;
		private FileChannel outcomingFileChannel;
//		private String tmpMailPart;
		private boolean statusIssued;
		
		public RetrState(AbstractInnerState callback) {
			super(callback);
		}

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
			System.out.println("");
			if(session.getClient().hasExternalApps()){
				ExternalProcessChain epc = session.getClient().getExternalProcessChain();
				try {
					this.incomingMail = epc.process(this.incomingMail, session.getClient().getUser(), ".mail");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				session.setFile1(epc.process(session.getFile1(), session.getClient().getUser(), ".moil"));
			}

//			HashSet<String[]> hs = new HashSet<String[]>();
//			String[] sa = {
//				"/bin/bash",
//				"-c",
//				"cat"
//			};
//			hs.add(sa);
//			ExternalProcessChain epc = new ExternalProcessChain(hs);
//			session.setFile1(epc.process(session.getFile1(), session.getClient().getUser(), ".moil"));
		
			
			try {
				this.incomingMailRAF = new RandomAccessFile(incomingMail, "rw");
				this.incomingMailRAF.seek(0);
				this.outcomingMail = File.createTempFile(session.getClient().getUser(), ".mail");
				this.outcomingMailRAF = new RandomAccessFile(this.outcomingMail, "rw");
				this.outcomingMailRAF.seek(0);
				if(session.getClient().hasTransformations()){
					MailParser parser = new MailParser(this.incomingMailRAF, this.outcomingMailRAF, session.getClient());
					parser.parseMessage();
				} else {
					this.outcomingMailRAF = this.incomingMailRAF;
//					session.setFile2(session.getFile1());
//					session.getFile2().seek(0);
				}
				this.outcomingMailRAF.seek(0);
				this.outcomingFileChannel = this.outcomingMailRAF.getChannel();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Response response = new Response();
			response.setOperation(SelectionKey.OP_READ);
			response.setState(this);
			response.setChannel(this.outcomingFileChannel);
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
					try {
						this.incomingMail = File.createTempFile(session.getClient().getUser(), ".mail");
						this.incomingMailRAF = new RandomAccessFile(incomingMail, "rw");
						this.incomingMailRAF.seek(0);
						this.incomingMailRAF.write(BufferUtils.byteBufferToString(response.getBuffers()).substring(responseToClient.length()+2).getBytes());
						this.incomingFileChannel = this.incomingMailRAF.getChannel();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
//					tmpMailPart = BufferUtils.byteBufferToString(response.getBuffers()).substring(responseToClient.length()+2);
					response.getBuffers().clear();
					response.getBuffers().put((responseToClient+"\r\n").getBytes());
					response.getBuffers().flip();
					this.statusIssued = false;
				}
//				this.setFlowToWriteFile();
				return response;
			} 
			
			response = new Response();

//			if(tmpMailPart != null) {
//				session.getSecondServerBuffer().clear();
//				session.getSecondServerBuffer().put(tmpMailPart.getBytes());
//				session.getSecondServerBuffer().flip();
//				tmpMailPart = null;
//			}
			
			response.setChannel(this.incomingFileChannel);
			
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
				AbstractInnerState tmpState;
				if(this.getCallbackState() == null) {
					tmpState = new NoneState(null);
				} else {
					tmpState = this.getCallbackState();
				}
				tmpState.setFlowToReadClient();
				response.setState(tmpState);
				return response;
			}
			
			if(!this.statusIssued) {
				this.statusIssued = true;
				response.setChannel(null);
				return response;
			}
			
			response.setChannel(this.outcomingFileChannel);
			this.setFlowToReadFile();
			
			return response;
		}
		public String toString(){
			return "Retr";
		}

		@Override
		public void callbackFunction() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class DeleState extends AbstractInnerState{

		private String args;
		
		public DeleState(AbstractInnerState callback, String args) {
			super(callback);
			this.args = args.trim();
		}
		
		@Override
		Response afterReadingFromClient(ClientSession session) {
			Response response = new Response();
			
			return response;
		}
		
		@Override
		Response afterWritingToClient(ClientSession session) {
			Response response = super.afterWritingToClient(session);
			AbstractInnerState tmpState;
			if(this.getCallbackState() == null) {
				tmpState = new NoneState(null);
			} else {
				tmpState = this.getCallbackState();
			}
			tmpState.setFlowToReadClient();
			response.setState(tmpState);
			return response;
		}
		public String toString(){
			return "Dele";
		}



		@Override
		public void callbackFunction() {
			// TODO Auto-generated method stub
			
		}
	
	}
	
	private class NoopState extends AbstractInnerState{
		
		public NoopState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}
		@Override
		Response afterWritingToClient(ClientSession session) {
			Response response = new Response();
			response = super.afterWritingToClient(session);
			AbstractInnerState tmpState;
			if(this.getCallbackState() == null) {
				tmpState = new NoneState(null);
			} else {
				tmpState = this.getCallbackState();
			}
			tmpState.setFlowToReadClient();
			response.setState(tmpState);
			return response;
		}
		public String toString(){
			return "Noop";
		}
		@Override
		public void callbackFunction() {
			// TODO Auto-generated method stub
			
		}

	}
	
	private class UidlState extends AbstractMultilinerInnerState{

		private String args;
		
		public UidlState(AbstractInnerState callback, String args) {
			super(callback);
			this.args = args.trim();
		}
		
		@Override
		Response afterWritingToClient(ClientSession session){
			if(args.length() > 0){
				this.setWaitingLineFeedEnd(false);
			}
			Response response = super.afterWritingToClient(session);
			if(!this.isWaitingLineFeedEnd()){
				AbstractInnerState tmpState;
				if(this.getCallbackState() == null) {
					tmpState = new NoneState(null);
				} else {
					tmpState = this.getCallbackState();
				}
				tmpState.setFlowToReadClient();
				response.setState(tmpState);
			}
			return response;
		}
		public String toString(){
			return "Uidl";
		}

		@Override
		public void callbackFunction() {
			// TODO Auto-generated method stub
			
		}
	}
	
	private class TopState extends AbstractMultilinerInnerState{
		
		public TopState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}
		@Override
		Response afterWritingToClient(ClientSession session){
			Response response = super.afterWritingToClient(session);
			if(!this.isWaitingLineFeedEnd()){
				AbstractInnerState tmpState;
				if(this.getCallbackState() == null) {
					tmpState = new NoneState(null);
				} else {
					tmpState = this.getCallbackState();
				}
				tmpState.setFlowToReadClient();
				response.setState(tmpState);
			}
			return response;
		}
		public String toString(){
			return "Top";
		}
		@Override
		public void callbackFunction() {
			// TODO Auto-generated method stub
			
		}

	}
	
	private class RsetState extends AbstractInnerState{
		
		public RsetState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}

		@Override
		Response afterWritingToClient(ClientSession session) {
			Response response = new Response();
			response = super.afterWritingToClient(session);
			AbstractInnerState tmpState;
			if(this.getCallbackState() == null) {
				tmpState = new NoneState(null);
			} else {
				tmpState = this.getCallbackState();
			}
			tmpState.setFlowToReadClient();
			response.setState(tmpState);
			return response;
		}
		
		public String toString(){
			return "Rset";
		}

		@Override
		public void callbackFunction() {
			// TODO Auto-generated method stub
			
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
