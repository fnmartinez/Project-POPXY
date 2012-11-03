package ar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
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
	
	private class RetrState extends AbstractMailFetcherInnerState{
		
//		private File incomingMail;
//		private File outcomingMail;
//		private RandomAccessFile incomingMailRAF;
//		private FileChannel incomingFileChannel;
//		private RandomAccessFile outcomingMailRAF;
//		private FileChannel outcomingFileChannel;
//		private boolean statusIssued;
//		private Boolean directToClient = null;
//		
		public RetrState(AbstractInnerState callback) {
			super(callback);
		}
//
//		@Override
//		public Action eval(ClientSession session, Action a) {
//			
//			Action r = null;
//			/* Look up for the last action done */
//			switch(this.getFlowDirection()){	
//			case READ_FILE:	r = afterReadingFromFile(session); break;
//			case WRITE_FILE:r = afterWritingToFile(session); break;
//			
//			}
//			r = super.eval(session, a);
//			
//			return r;
//		}
//
//		private Action afterReadingFromFile(ClientSession session) {
//			Action response = new Action();
//			ByteBuffer mlsb = session.getSecondServerBuffer();			
//			response.setChannel(session.getClientSocket());
//			response.setOperation(SelectionKey.OP_WRITE);
//			response.setState(this);
//			response.setMultilineBuffer(mlsb);
//			response.setMultilineResponse(true);
//			this.setFlowToWriteClient();
//			if(BufferUtils.byteBufferToString(mlsb).endsWith("\r\n.\r\n")){
//				this.setWaitingLineFeedEnd(false);
//			}
//			return response;
//		}
//
//		private Action afterWritingToFile(ClientSession session) {
//
//			if(this.isWaitingLineFeedEnd()){
//				Action response = super.afterWritingToClient(session);
//				return response;
//			}
//			if(session.getClient().hasExternalApps()){
//				ExternalProcessChain epc = session.getClient().getExternalProcessChain();
//				try {
//					this.incomingMail = epc.process(this.incomingMail, session.getClient().getUser(), ".mail");
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//
//			
//			try {
//				this.incomingMailRAF = new RandomAccessFile(incomingMail, "rw");
//				this.incomingMailRAF.seek(0);
//				this.outcomingMail = File.createTempFile(session.getClient().getUser(), ".mail");
//				this.outcomingMailRAF = new RandomAccessFile(this.outcomingMail, "rw");
//				this.outcomingMailRAF.seek(0);
//				if(session.getClient().hasTransformations()){
//					MailParser parser = new MailParser(this.incomingMailRAF, this.outcomingMailRAF, session.getClient());
//					parser.parseMessage();
//				} else {
//					this.outcomingMailRAF = this.incomingMailRAF;
//				}
//				this.outcomingMailRAF.seek(0);
//				this.outcomingFileChannel = this.outcomingMailRAF.getChannel();
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			Action response = new Action();
//			response.setOperation(SelectionKey.OP_READ);
//			response.setState(this);
//			response.setChannel(this.outcomingFileChannel);
//			response.setMultilineBuffer(session.getSecondServerBuffer());
//			response.setMultilineResponse(true);
//			this.setFlowToReadFile();
//			this.setWaitingLineFeedEnd(true);
//			return response;
//		}
//
//		@Override
//		Action afterReadingFromServer(ClientSession session){
//			
//			Action response = null;
//			
//			if(directToClient == null){
//				directToClient = !session.getClient().hasTransformations();
//			}
//			if(directToClient){
//				return super.afterReadingFromServer(session);
//			}
//			if(!this.isWaitingLineFeedEnd()){
//				response = super.afterReadingFromServer(session);
//				if(this.isWaitingLineFeedEnd()) {
//					String responseToClient = BufferUtils.byteBufferToString(response.getBuffers()).split("\\r\\n")[0];
//					try {
//						this.incomingMail = File.createTempFile(session.getClient().getUser(), ".mail");
//						this.incomingMailRAF = new RandomAccessFile(incomingMail, "rw");
//						this.incomingMailRAF.seek(0);
//						this.incomingMailRAF.write(BufferUtils.byteBufferToString(response.getBuffers()).substring(responseToClient.length()+2).getBytes());
//						this.incomingFileChannel = this.incomingMailRAF.getChannel();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//					response.getBuffers().clear();
//					response.getBuffers().put((responseToClient+"\r\n").getBytes());
//					response.getBuffers().flip();
//					this.statusIssued = false;
//				}
////				this.setFlowToWriteFile();
//				return response;
//			} 
//			
//			response = new Action();
//
//			
//			response.setChannel(this.incomingFileChannel);
//			
//			ByteBuffer mlsb = session.getSecondServerBuffer();			
//			
//			response.setOperation(SelectionKey.OP_WRITE);
//			response.setState(this);
//			response.setMultilineBuffer(mlsb);
//			response.setMultilineResponse(true);
//			this.setFlowToWriteFile();
//			
//			if(BufferUtils.byteBufferToString(mlsb).contains("\r\n.\r\n")){
//				this.setWaitingLineFeedEnd(false);
//			}
//			
//			return response;
//		}
//		
//
//		@Override
//		Action afterWritingToClient(ClientSession session){
//			Action response = super.afterWritingToClient(session);
//			if(!this.isWaitingLineFeedEnd()){
//				AbstractInnerState tmpState;
//				if(this.getCallbackState() == null) {
//					tmpState = new NoneState(null);
//				} else {
//					tmpState = this.getCallbackState();
//				}
//				tmpState.setFlowToReadClient();
//				response.setState(tmpState);
//				return response;
//			}
//
//			if(directToClient){
//				return response;
//			}
//			
//			if(!this.statusIssued) {
//				this.statusIssued = true;
////				response.setChannel(null);
//				return response;
//			}
//			
//			response.setChannel(this.outcomingFileChannel);
//			this.setFlowToReadFile();
//			
//			return response;
//		}
		public String toString(){
			return "Retr";
		}

	}
	
	private class DeleState extends AbstractInnerState{

		private String args;
		private File testMail;
		private boolean testHeadersOnly;
		private boolean testMessage;
		private boolean messagedTested;
		
		public DeleState(AbstractInnerState callback, String args) {
			super(callback);
			this.args = args.trim();
			this.testMail = null;
//			this.testHeader = null;
		}
		
		@Override
		Action afterReadingFromClient(ClientSession session) {
			Action response = super.afterReadingFromClient(session);
			AbstractInnerState ais;
			

			if(session.getClient().hasDeletionRestriction() && !this.messagedTested){
				this.testMessage = true;
				this.messagedTested = false;
				response.getBuffers().clear();
				if(!session.getClient().getDeletionConfiguration().hasContentTypeRestriction() &&
						!session.getClient().getDeletionConfiguration().hasStructureRestriction()) {
					response.getBuffers().put(("TOP "+this.args+" 0\r\n").getBytes());
					ais = new TopState(this);
				} else {
					response.getBuffers().put(("RETR "+this.args+"\r\n").getBytes());
					ais = new RetrState(this);
				}
				response.getBuffers().flip();
			} else {
				//TODO: do deletion normally
				this.testMessage = false;
				this.messagedTested = true;
				ais = this;
			}
			
			ais.setFlowToWriteServer();
			response.setState(ais);
			
			return response;
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
		Action afterReadingFromServer(ClientSession session) {
			
			File fin = this.testMail;
			File fout = null;
			RandomAccessFile finRAF = null;
			RandomAccessFile foutRAF = null;
			
			if(!this.testMessage) {
				return super.afterReadingFromServer(session);
			}

			try {
				finRAF = new RandomAccessFile(fin, "rw");
				finRAF.seek(0);
				fout = File.createTempFile(session.getClient().getUser(), ".mail");
				foutRAF = new RandomAccessFile(fout, "rw");
				foutRAF.seek(0);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			MailParser parser = new MailParser(finRAF, foutRAF, session.getClient());

			if(this.testHeadersOnly) {
				try {
					parser.parseOnlyHeadersMessage();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} else {
				try {
					parser.parseMessage();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
			
			

			Action r = null;
			if (session.getClient().passDeletionFilters(parser.getMail())) {
				// poner para borrar
				this.messagedTested = true;
				r = this.afterReadingFromClient(session);
			} else {
				// devolver OK al cliente
				ByteBuffer b = session.getFirstServerBuffer();
				b.clear();
				b.put("+OK Your message wasn't deleted\r\n".getBytes());
				b.flip();
				return super.afterReadingFromServer(session);
			}
			
			return r;
		}


		@Override
		public InnerStateAction callbackEval(AbstractInnerState s, Action a) {
			// TODO Auto-generated method stub
			InnerStateAction r = new InnerStateAction(a);
			AbstractMailFetcherInnerState rs = (AbstractMailFetcherInnerState)s;
			
			if(rs.getFlowDirection() == FlowDirection.WRITE_CLIENT) {
				r.setOperation(-1);
				r.setState(this);
				this.setFlowToWriteClient();
				this.testMail = rs.getIncomingMail();
			}
			
			if(s.getClass() == RetrState.class) {
				this.testHeadersOnly = false;
			}
			if(s.getClass() == TopState.class) {
				this.testHeadersOnly = true;
			}
			
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
	
	private class TopState extends AbstractMailFetcherInnerState{
		
		public TopState(AbstractInnerState callback) {
			super(callback);
			this.setDirectToClient(false);
		}
		
		@Override
		Action afterWritingToFile(ClientSession session) {

			if(this.isWaitingLineFeedEnd()){
				Action response = super.afterWritingToClient(session);
				return response;
			}
//			if(session.getClient().hasExternalApps()){
//				ExternalProcessChain epc = session.getClient().getExternalProcessChain();
//				try {
//					this.setIncomingMail(epc.process(this.getIncomingMail(), session.getClient().getUser(), ".mail"));
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}

			
			try {
				this.setIncomingMailRAF(new RandomAccessFile(this.getIncomingMail(), "rw"));
				this.getIncomingMailRAF().seek(0);
				this.setOutcomingMail(File.createTempFile(session.getClient().getUser(), ".mail"));
				this.setOutcomingMailRAF(new RandomAccessFile(this.getOutcomingMail(), "rw"));
				this.getOutcomingMailRAF().seek(0);
				if(session.getClient().hasTransformations()){
					MailParser parser = new MailParser(this.getIncomingMailRAF(), this.getOutcomingMailRAF(), session.getClient());
					parser.parseOnlyHeadersMessage();
				} else {
					this.setOutcomingMailRAF(this.getIncomingMailRAF());
				}
				this.getOutcomingMailRAF().seek(0);
				this.setOutcomingFileChannel(this.getOutcomingMailRAF().getChannel());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Action response = new Action();
			response.setOperation(SelectionKey.OP_READ);
			response.setState(this);
			response.setChannel(this.getOutcomingFileChannel());
			response.setMultilineBuffer(session.getSecondServerBuffer());
			response.setMultilineResponse(true);
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
