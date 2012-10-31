package ar.sessions;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import ar.AuthState;
import ar.POPXY;
import ar.Response;
import ar.State;
import ar.elements.User;
import ar.sessions.utils.BufferUtils;

public class ClientSession implements Runnable {

	private final static int CLIENT_BUFFER_SIZE = 104;
	private final static int FIRST_SERVER_BUFFER_SIZE = 512;
	private final static int SECOND_SERVER_BUFFER_SIZE = 4096;
	private final static int MOCK_SERVER_BUFFER_SIZE = 512;
	private SocketChannel clientSocket;
	private SocketChannel originServerSocket;

	private ByteBuffer clientBuffer = ByteBuffer.allocate(CLIENT_BUFFER_SIZE);

	private ByteBuffer firstServerBuffer = ByteBuffer.allocate(FIRST_SERVER_BUFFER_SIZE);
	private ByteBuffer secondServerBuffer = ByteBuffer.allocate(SECOND_SERVER_BUFFER_SIZE);

	private ByteBuffer mockServerBuffer = ByteBuffer.allocate(MOCK_SERVER_BUFFER_SIZE);

	private User client;



	private State state;

	private ByteBuffer bufferToWrite;
	private ByteBuffer bufferToRead;
	private ByteChannel channelToWrite;
	private ByteChannel channelToRead;
//	private RandomAccessFile file1;
//	private File filename1;
//	private RandomAccessFile file2;
//	private File filename2;
	private boolean firstContact;
	private boolean useSecondServerBuffer;
	private int suscriptionMode;
	private boolean conectionEstablished;
	
	public ClientSession(SelectionKey key) throws IOException {
	}

	public ClientSession(SocketChannel s) {
		this.clientSocket = s;
		this.state = new AuthState();
		this.channelToWrite = clientSocket;
		this.firstContact = true;
		this.useSecondServerBuffer = false;
		this.conectionEstablished = true;
	}

	public void handleConnection() {

		this.bufferToRead = firstServerBuffer;
		this.channelToRead = originServerSocket;

		this.read();
	}
	
	private void logWrite(String msg) {
		String ctw = channelToWrite == originServerSocket ? "S":(channelToWrite == clientSocket ? "C" : "F"); 
		if(msg.compareTo("") == 0 || msg.compareTo(" ") == 0) {
			System.out.println("changos!");
		}
		POPXY.getLogger().info("["+this.state+"] P->"+ctw+" : "+msg);
	}

	private void write() {
		//TODO: try to put this in the automaton
		if (firstContact) {
			try {
				this.mockServerBuffer.clear();
				mockServerBuffer.put("+OK\r\n".getBytes());
				this.mockServerBuffer.flip();

				logWrite(BufferUtils.byteBufferToString(mockServerBuffer));
				clientSocket.write(mockServerBuffer);
			} catch (IOException e) {
				// TODO: handle exception
			}

			this.firstContact = false;
		} else {
			if(useSecondServerBuffer){
				try {
					logWrite(BufferUtils.byteBufferToString(secondServerBuffer));
					channelToWrite.write(secondServerBuffer);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					logWrite(BufferUtils.byteBufferToString(bufferToRead));
					channelToWrite.write(bufferToRead);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		evaluateState();
		
	}

	private void logRead(String msg) {
		String ctw = channelToRead == originServerSocket ? "S":(channelToRead == clientSocket ? "C" : "F");
		if(msg.compareTo("") == 0 || msg.compareTo(" ") == 0) {
			System.out.println("changos!");
		}
		POPXY.getLogger().info("["+this.state+"] "+ctw+"->P : "+msg);
	}
	
	private void read() {
		if(this.channelToRead != null){
			if(useSecondServerBuffer) {
				secondServerBuffer.clear();
				try {
					channelToRead.read(secondServerBuffer);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				secondServerBuffer.flip();
				logRead(BufferUtils.byteBufferToString(secondServerBuffer));
			} else {
				bufferToWrite.clear();
				try {
					channelToRead.read(bufferToWrite);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bufferToWrite.flip();
				logRead(BufferUtils.byteBufferToString(bufferToWrite));
			}
		}
		evaluateState();
		
	}
	
	private void evaluateState() {
		Response r = state.eval(this);
		this.state = r.getState();
		if(state == null){
			handleEndConection();
			return;
		}
		
		
		this.suscriptionMode = r.getOperation();
		this.useSecondServerBuffer = r.isMultilineResponse();
		switch(suscriptionMode) {
		case SelectionKey.OP_READ:
			this.bufferToWrite = r.getBuffers();
			this.channelToRead = r.getChannel();
			break;
		case SelectionKey.OP_WRITE:
			this.bufferToRead = r.getBuffers();
			this.channelToWrite = r.getChannel();
			break;
		default:
			this.bufferToRead = this.bufferToWrite = null;
			break;
		}
		
		return;
	}

	private void handleEndConection() {
		System.out.println("Se cerro la conexion del cliente!!!");
		try {
			if(this.clientSocket != null){
				this.clientSocket.close();
			}
			if(this.originServerSocket != null){
				this.originServerSocket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.conectionEstablished = false;
		
	}

	
	public SocketChannel getClientSocket() {
		return clientSocket;
	}

	public void setClientSocket(SocketChannel clientSocket) {
		this.clientSocket = clientSocket;
	}

	public SocketChannel getOriginServerSocket() {
		return originServerSocket;
	}

	public void setOriginServerSocket(SocketChannel originServerSocket) {
		this.originServerSocket = originServerSocket;
	}

	public ByteBuffer getClientBuffer() {
		return clientBuffer;
	}

	public void setClientBuffer(ByteBuffer clientBuffer) {
		this.clientBuffer = clientBuffer;
	}

	public ByteBuffer getFirstServerBuffer() {
		return firstServerBuffer;
	}

	public void setFirstServerBuffer(ByteBuffer firstServerBuffer) {
		this.firstServerBuffer = firstServerBuffer;
	}

	public ByteBuffer getSecondServerBuffer() {
		return secondServerBuffer;
	}

	public void setSecondServerBuffer(ByteBuffer secondServerBuffer) {
		this.secondServerBuffer = secondServerBuffer;
	}


	public ByteBuffer getMockServerBuffer() {
		return mockServerBuffer;
	}

	public void setMockServerBuffer(ByteBuffer mockServerBuffer) {
		this.mockServerBuffer = mockServerBuffer;
	}

	public User getClient() {
		return client;
	}

	public void setClient(User client) {
		this.client = client;
	}

	public void run() {
		// TODO Auto-generated method stub
		this.write();
	
		while(this.conectionEstablished){
			switch(suscriptionMode) {
			case SelectionKey.OP_READ:
				this.read();
				break;
			case SelectionKey.OP_WRITE:
				this.write();
				break;
			default:
				break;
			}
		}
		
	}

//	public FileChannel getFile1Channel() {
//		return this.getFile1().getChannel();
//	}
//	
//	public RandomAccessFile getFile1(){
//		if(this.file1 == null){
//			try {
//				this.file1 = new RandomAccessFile( this.filename1 = File.createTempFile(this.client.getUser(), ".mail", null), "rw");
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		return this.file1;
//	}
//	
//	public RandomAccessFile getFile2(){
//		if(this.file2 == null){
//			try {
//				this.file2 = new RandomAccessFile( this.filename2 = File.createTempFile(this.client.getUser(), ".mail", null), "rw");
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		return this.file2;
//	}
//	
//	public FileChannel getFile2Channel() {
//		return this.getFile2().getChannel();
//	}
//	
//	public void removeFile1(){
//		if(this.file1 != null && this.filename1 != null ) {
//			this.filename1.delete();
//			this.file1 = null;
//		}
//	}
//	
//	public void removeFile2(){
//		if(this.file2 != null && this.filename2 != null) {
//			this.filename2.delete();
//			this.file2 = null;
//		}
//	}
//
//	public void setFile2(RandomAccessFile file) {
//		this.file2 = file;
//	}
//	
//	public void setFile1(RandomAccessFile file) {
//		this.file1 = file;
//	}
}

