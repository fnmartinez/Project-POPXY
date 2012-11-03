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

public abstract class AbstractMailFetcherInnerState extends AbstractMultilinerInnerState {

	private File incomingMail;
	private File outcomingMail;
	private RandomAccessFile incomingMailRAF;
	private FileChannel incomingFileChannel;
	private RandomAccessFile outcomingMailRAF;
	private FileChannel outcomingFileChannel;
	private boolean statusIssued;
	private Boolean directToClient = null;
	
	public AbstractMailFetcherInnerState(AbstractInnerState callback) {
		super(callback);
		
	}
	
	@Override
	public Action eval(ClientSession session, Action a) {
		
		Action r = null;
		/* Look up for the last action done */
		switch(this.getFlowDirection()){	
		case READ_FILE:	r = afterReadingFromFile(session); break;
		case WRITE_FILE:r = afterWritingToFile(session); break;
		
		}
		r = super.eval(session, a);
		
		return r;
	}

	Action afterReadingFromFile(ClientSession session) {
		Action response = new Action();
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

	Action afterWritingToFile(ClientSession session) {

		if(this.isWaitingLineFeedEnd()){
			Action response = super.afterWritingToClient(session);
			return response;
		}
		if(session.getClient().hasExternalApps()){
			ExternalProcessChain epc = session.getClient().getExternalProcessChain();
			try {
				this.incomingMail = epc.process(this.incomingMail, session.getClient().getUser(), ".mail");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
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
		response.setMultilineBuffer(session.getSecondServerBuffer());
		response.setMultilineResponse(true);
		this.setFlowToReadFile();
		this.setWaitingLineFeedEnd(true);
		return response;
	}

	@Override
	Action afterReadingFromServer(ClientSession session){
		
		Action response = null;
		
		if(directToClient == null){
			directToClient = !session.getClient().hasTransformations();
		}
		if(directToClient){
			return super.afterReadingFromServer(session);
		}
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
				
				response.getBuffers().clear();
				response.getBuffers().put((responseToClient+"\r\n").getBytes());
				response.getBuffers().flip();
				this.statusIssued = false;
			}
//			this.setFlowToWriteFile();
			return response;
		} 
		
		response = new Action();

		
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
	Action afterWritingToClient(ClientSession session){
		Action response = super.afterWritingToClient(session);
		if(!this.isWaitingLineFeedEnd()){
			AbstractInnerState tmpState;
			tmpState = this.getCallbackState();
			tmpState.setFlowToReadClient();
			response.setState(tmpState);
			return response;
		}

		if(directToClient){
			return response;
		}
		
		if(!this.statusIssued) {
			this.statusIssued = true;
//			response.setChannel(null);
			return response;
		}
		
		response.setChannel(this.outcomingFileChannel);
		this.setFlowToReadFile();
		
		return response;
	}
	
	public File getIncomingMail() {
		return incomingMail;
	}
	
	public void setIncomingMail(File incomingMail) {
		this.incomingMail = incomingMail;
	}
	
	public File getOutcomingMail() {
		return outcomingMail;
	}
	
	public void setOutcomingMail(File outcomingMail) {
		this.outcomingMail = outcomingMail;
	}
	
	public RandomAccessFile getIncomingMailRAF() {
		return incomingMailRAF;
	}
	
	public void setIncomingMailRAF(RandomAccessFile incomingMailRAF) {
		this.incomingMailRAF = incomingMailRAF;
	}
	
	public FileChannel getIncomingFileChannel() {
		return incomingFileChannel;
	}
	
	public void setIncomingFileChannel(FileChannel incomingFileChannel) {
		this.incomingFileChannel = incomingFileChannel;
	}
	
	public RandomAccessFile getOutcomingMailRAF() {
		return outcomingMailRAF;
	}
	
	public void setOutcomingMailRAF(RandomAccessFile outcomingMailRAF) {
		this.outcomingMailRAF = outcomingMailRAF;
	}
	
	public FileChannel getOutcomingFileChannel() {
		return outcomingFileChannel;
	}
	
	public void setOutcomingFileChannel(FileChannel outcomingFileChannel) {
		this.outcomingFileChannel = outcomingFileChannel;
	}
	
	public boolean isStatusIssued() {
		return statusIssued;
	}
	
	public void setStatusIssued(boolean statusIssued) {
		this.statusIssued = statusIssued;
	}
	
	public Boolean getDirectToClient() {
		return directToClient;
	}
	
	public void setDirectToClient(Boolean directToClient) {
		this.directToClient = directToClient;
	}

}