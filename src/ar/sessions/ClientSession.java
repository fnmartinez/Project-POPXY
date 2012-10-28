package ar.sessions;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import ar.AuthState;
import ar.POPXY;
import ar.Response;
import ar.State;
import ar.elements.User;
import ar.sessions.utils.BufferUtils;

public class ClientSession implements Runnable {

	private final static int CLIENT_BUFFER_COMMAND_SIZE = 4;
	private final static int CLIENT_BUFFER_ARGUMENT_SIZE = 100;
	private final static int FIRST_SERVER_BUFFER_COMM_SIZE = 4;
	private final static int FIRST_SERVER_BUFFER_ARG_SIZE = 512 - FIRST_SERVER_BUFFER_COMM_SIZE;
	private final static int SECOND_SERVER_BUFFER_SIZE = 4096;
	private final static int MOCK_SERVER_BUFFER_COMM_SIZE = 10;
	private final static int MOCK_SERVER_BUFFER_ARG_SIZE = 512 - MOCK_SERVER_BUFFER_COMM_SIZE;
	private SocketChannel clientSocket;
	private SocketChannel originServerSocket;
	private Selector selector;

	private ByteBuffer[] clientBuffer = {
			ByteBuffer.allocate(CLIENT_BUFFER_COMMAND_SIZE),
			ByteBuffer.allocate(CLIENT_BUFFER_ARGUMENT_SIZE) };

	private ByteBuffer[] firstServerBuffer = {
			ByteBuffer.allocate(FIRST_SERVER_BUFFER_COMM_SIZE),
			ByteBuffer.allocate(FIRST_SERVER_BUFFER_ARG_SIZE) };

	private ByteBuffer secondServerBuffer = ByteBuffer
			.allocate(SECOND_SERVER_BUFFER_SIZE);

	private ByteBuffer[] mockServerBuffer = {
			ByteBuffer.allocate(MOCK_SERVER_BUFFER_COMM_SIZE),
			ByteBuffer.allocate(MOCK_SERVER_BUFFER_ARG_SIZE) };

	private User client;



	private State state;

	private ByteBuffer[] bufferToWrite;
	private ByteBuffer[] bufferToRead;
	private SocketChannel channelToWrite;
	private SocketChannel channelToRead;
	private FileChannel file1;
	private FileChannel file2;
	private boolean firstContact;
	private boolean useSecondServerBuffer;
	private SocketChannel toSuscribe;
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
		String ctw = channelToWrite == originServerSocket ? "S":"C"; 
		if(msg.compareTo("") == 0 || msg.compareTo(" ") == 0) {
			System.out.println("changos!");
		}
		POPXY.getLogger().info("["+this.state+"] P->"+ctw+" : "+msg);
	}

	private void write() {
		//TODO: try to put this in the automaton
		if (firstContact) {
			try {
				clearBuffer(mockServerBuffer);

				mockServerBuffer[0].put("+OK\r\n".getBytes());

				flipBuffer(mockServerBuffer);

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
		String ctw = channelToRead == originServerSocket ? "S":"C";
		if(msg.compareTo("") == 0 || msg.compareTo(" ") == 0) {
			System.out.println("changos!");
		}
		POPXY.getLogger().info("["+this.state+"] "+ctw+"->P : "+msg);
	}
	
	private void read() {
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
			clearBuffer(bufferToWrite);
			try {
				channelToRead.read(bufferToWrite);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			flipBuffer(bufferToWrite);
			logRead(BufferUtils.byteBufferToString(bufferToWrite));
		}

		evaluateState();
		
	}
	
	private void evaluateState() {
		Response r = state.eval(this);
		this.state = r.getState();
		if(state == null){
			handleEndConection();
			toSuscribe = null;
			return;
		}
		
		//Me suscribo a escritura de los dos canales, para desuscribirme de escritura en caso de q no aplique.
//		try {
//			this.clientSocket.register(selector, 0);
//			if(this.originServerSocket!= null) this.originServerSocket.register(selector, 0);
//		} catch (ClosedChannelException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		this.toSuscribe = (SocketChannel) r.getChannel();
		this.suscriptionMode = r.getOperation();
		this.useSecondServerBuffer = r.isMultilineResponse();
		switch(suscriptionMode) {
		case SelectionKey.OP_READ:
			this.bufferToWrite = r.getBuffers();
			this.channelToRead = (SocketChannel)r.getChannel();
			break;
		case SelectionKey.OP_WRITE:
			this.bufferToRead = r.getBuffers();
			this.channelToWrite = (SocketChannel)r.getChannel();
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

	private void clearBuffer(Buffer[] b) {
		for (Buffer b2 : b) {
			b2.clear();
		}
	}

	private void flipBuffer(Buffer[] b) {
		for (Buffer b2 : b) {
			b2.flip();
		}
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

	public ByteBuffer[] getClientBuffer() {
		return clientBuffer;
	}

	public void setClientBuffer(ByteBuffer[] clientBuffer) {
		this.clientBuffer = clientBuffer;
	}

	public ByteBuffer[] getFirstServerBuffer() {
		return firstServerBuffer;
	}

	public void setFirstServerBuffer(ByteBuffer[] firstServerBuffer) {
		this.firstServerBuffer = firstServerBuffer;
	}

	public ByteBuffer getSecondServerBuffer() {
		return secondServerBuffer;
	}

	public void setSecondServerBuffer(ByteBuffer secondServerBuffer) {
		this.secondServerBuffer = secondServerBuffer;
	}


	public ByteBuffer[] getMockServerBuffer() {
		return mockServerBuffer;
	}

	public void setMockServerBuffer(ByteBuffer[] mockServerBuffer) {
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
}

