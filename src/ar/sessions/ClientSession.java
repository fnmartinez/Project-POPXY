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
import ar.Action;
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
		try {
			logWrite(BufferUtils.byteBufferToString(bufferToRead));
			channelToWrite.write(bufferToRead);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	
	private void evaluateState() {
		Action r = state.eval(this);
		this.state = r.getState();
		if(state == null){
			handleEndConection();
			return;
		}
		
		
		this.suscriptionMode = r.getOperation();
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
			evaluateState();
		}
		
	}

}

