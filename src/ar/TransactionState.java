package ar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import ar.elements.Mail;
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
		Action afterReadingFromClient(ClientSession session) {
			
			Action response = new Action();
			String command = BufferUtils.byteBufferToString(session.getClientBuffer()).trim();
			if(command.length() >= 5){
				command = command.substring(0, 4);
			}
			POPHeadCommands cmd = POPHeadCommands.getLiteralByString(command);
			
			String args = BufferUtils.byteBufferToString(session.getClientBuffer());
			if(args.length() > 5){
				args = args.substring(5);
			} else {
				args = "";
			}
	
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
				session.getClient().readMail();
				tmpState = new RetrState(this);
				break;
			case DELE:
				tmpState = new DeleState(this, args);
				tmpState.setFlowToReadClient();
				response.setState(tmpState);
				response.setChannel(null);
				response.setOperation(SelectionKey.OP_READ);
				return response;
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
		
		private Action invalidCommand(ClientSession session) {
			Action response = new Action();
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

	}
	
	private class QuitState extends AbstractInnerState implements EndState{
		
		public QuitState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}
		private boolean isFinalState = false;
		
		@Override
		Action afterWritingToClient(ClientSession session) {
			Action response = new Action();
			
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
		
		public StatState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}
		@Override
		Action afterWritingToClient(ClientSession session) {
			Action response = super.afterWritingToClient(session);
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
	}

	private class ListState extends AbstractMultilinerInnerState{
		
		private String args;
		
		public ListState(AbstractInnerState callback, String args) {
			super(callback);
			this.args = args.trim();
		}
		
		@Override
		Action afterWritingToClient(ClientSession session){
			if(args.length() > 0){
				this.setWaitingLineFeedEnd(false);
			}
			Action response = super.afterWritingToClient(session);
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

	}
	
	private class RetrState extends AbstractMailFetcherInnerClass{
		
		public RetrState(AbstractInnerState callback) {
			super(callback);
		}

		public String toString(){
			return "Retr";
		}

	}
	
	private class DeleState extends AbstractInnerState{

		private String args;
		private boolean hasRestrictions;
		private boolean checkOnlyHeaders;
		private boolean alreadyChecked;
		private File inputFile;
		private File outputFile;
		private boolean statusNotified;
		private Mail mailToCheck = null;
		private boolean retrievalError;
		
		public DeleState(AbstractInnerState callback, String args) {
			super(callback);
			this.args = args.trim();
			this.hasRestrictions = false;
			this.alreadyChecked = false;
		}
		
		@Override
		Action afterReadingFromClient(ClientSession session) {
			Action response = super.afterReadingFromClient(session);
			
			if(this.alreadyChecked) {
				return response;
			}
			
			this.hasRestrictions = session.getClient().hasDeletionRestriction();
			if(!this.hasRestrictions) {
				return response;
			}
			this.statusNotified = false;
			response.getBuffers().clear();
			AbstractInnerState ais;
//			if(!session.getClient().getDeletionConfiguration().hasContentTypeRestriction() &&
//					!session.getClient().getDeletionConfiguration().hasStructureRestriction()){
//				//TODO: hacer top n 0
//				response.getBuffers().put(("TOP "+args+" 0\r\n").getBytes());
//				ais = new TopState(this);
//				((AbstractMailFetcherInnerClass)ais).setDirectToClient(false);
//				this.checkOnlyHeaders = true;
//			} else {
				//TODO: hacer retr n
				response.getBuffers().put(("RETR "+args+"\r\n").getBytes());
				ais = new RetrState(this);
				((AbstractMailFetcherInnerClass)ais).setDirectToClient(false);
				this.checkOnlyHeaders = false;
//			}
			response.getBuffers().flip();
			ais.setFlowToWriteServer();
			response.setState(ais);
			
			return response;
		}
		
		@Override
		Action afterReadingFromServer(ClientSession session) {
			
			if(!this.hasRestrictions) {
				Action r = super.afterReadingFromServer(session);
				if(BufferUtils.byteBufferToString(r.getBuffers()).startsWith("+OK")) {
					session.getClient().dropMail();
				}
				return r;
			}
			
			if(this.retrievalError) {
				this.hasRestrictions = false;
				this.alreadyChecked = true;
				Action r = this.afterReadingFromClient(session);
				r.getBuffers().clear();
				r.getBuffers().put(("DELE "+args+"\r\n").getBytes());
				r.getBuffers().flip();
				return r;
			}
			
			if(this.mailToCheck == null) {
				MailParser parser = null;
				try {
					parser = new MailParser(new RandomAccessFile(inputFile, "rw"), new RandomAccessFile(outputFile, "rw"), session.getClient());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(this.checkOnlyHeaders) {
					try {
						parser.parseOnlyHeadersMessage();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				} else {
					try {
						parser.parseMessage();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				this.mailToCheck = parser.getMail();
				
			}
			
			if(session.getClient().passDeletionFilters(mailToCheck)) {
				this.hasRestrictions = false;
				this.alreadyChecked = true;
				Action r = this.afterReadingFromClient(session);
				r.getBuffers().clear();
				r.getBuffers().put(("DELE "+args+"\r\n").getBytes());
				r.getBuffers().flip();
				session.getClient().dropMail();
				return r;
			}
			
			Action r = new Action();
			
			r.setBuffers(session.getFirstServerBuffer());
			r.getBuffers().clear();
			r.getBuffers().put(("+OK You cannot delete that message\r\n").getBytes());
			r.getBuffers().flip();
			r.setState(this);
			r.setChannel(session.getClientSocket());
			r.setOperation(SelectionKey.OP_WRITE);
			this.setFlowToWriteClient();
			
			return r;
		}
		
		@Override
		Action afterWritingToClient(ClientSession session) {
			Action response = super.afterWritingToClient(session);
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
		public InnerStateAction callbackEval(AbstractInnerState s, Action a) {
			
			if(s.getFlowDirection() != FlowDirection.WRITE_CLIENT) {
				return super.callbackEval(s, a);
			}
			
			if(!this.statusNotified){
				this.statusNotified = true;
				String resp = BufferUtils.byteBufferToString(a.getBuffers());
				if(resp.startsWith("+OK")){
					InnerStateAction r = super.callbackEval(s, a);
					r.setChannel(null);
					return r;
				} else if(resp.startsWith("-ERR")){
					this.retrievalError = true;
				}
			} 
			
//			return super.callbackEval(s, a);
			InnerStateAction r = new InnerStateAction();
			if(s.getClass() == RetrState.class || s.getClass() == TopState.class) {
				AbstractMailFetcherInnerClass amfis = (AbstractMailFetcherInnerClass)s;
				this.mailToCheck = amfis.getMail();
				this.inputFile = amfis.getIncomingMail();
				this.outputFile = amfis.getOutcomingMail();
			}
			
			r.setState(this);
			r.setOperation(-1);
//			this.setFlowToWriteFile();
			this.setFlowToReadServer();
			return r;
		}
	
	}
	
	private class NoopState extends AbstractInnerState{
		
		public NoopState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}
		@Override
		Action afterWritingToClient(ClientSession session) {
			Action response = new Action();
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

	}
	
	private class UidlState extends AbstractMultilinerInnerState{

		private String args;
		
		public UidlState(AbstractInnerState callback, String args) {
			super(callback);
			this.args = args.trim();
		}
		
		@Override
		Action afterWritingToClient(ClientSession session){
			if(args.length() > 0){
				this.setWaitingLineFeedEnd(false);
			}
			Action response = super.afterWritingToClient(session);
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

	}
	
	private class TopState extends AbstractMailFetcherInnerClass{
		
		public TopState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		Action afterWritingToFile(ClientSession session) {

			if(this.isWaitingLineFeedEnd()){
				Action response = super.afterWritingToClient(session);
				return response;
			}
			
			try {
				this.incomingMailRAF = new RandomAccessFile(incomingMail, "rw");
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			if(session.getClient().hasExternalApps()){
				ExternalProcessChain epc = session.getClient().getExternalProcessChain();
				this.incomingMailRAF = epc.process(this.incomingMailRAF, session.getClient().getUser(), ".mail");
			}

			try {
				this.incomingMailRAF.seek(0);
				this.outcomingMail = File.createTempFile(session.getClient().getUser(), ".mail");
				this.outcomingMailRAF = new RandomAccessFile(this.outcomingMail, "rw");
				this.outcomingMailRAF.seek(0);
				if(session.getClient().hasTransformations()){
					MailParser parser = new MailParser(this.incomingMailRAF, this.outcomingMailRAF, session.getClient());
					parser.parseOnlyHeadersMessage();
				//	parser.parseMessage();
					this.setMail(parser.getMail());
				} else {
					this.outcomingMailRAF = this.incomingMailRAF;
				}
				this.outcomingMailRAF.seek(0);
				this.outcomingFileChannel = this.outcomingMailRAF.getChannel();
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Action response = new Action();
			response.setOperation(SelectionKey.OP_READ);
			response.setState(this);
			response.setChannel(this.outcomingFileChannel);
			response.setBuffers(session.getSecondServerBuffer());
			this.setFlowToReadFile();
			this.setWaitingLineFeedEnd(true);
			return response;
		}
		
		public String toString(){
			return "Top";
		}

	}
	
	private class RsetState extends AbstractInnerState{
		
		public RsetState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}

		@Override
		Action afterWritingToClient(ClientSession session) {
			Action response = new Action();
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


	}
	
	public Action eval(ClientSession session, Action a) {
		Action response = this.currentState.eval(session, a);
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
