package ar.sessions;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ar.POPXY;
import ar.elements.User;
import ar.sessions.utils.SessionStates;

public class ClientSession implements Session {

	private final static int CLIENT_BUFFER_SIZE = 50;
	private final static int SERVER_INCOMMING_BUFFER_SIZE = 4096;
	private final static int MOCK_SERVER_BUFFER_SIZE = 10;
	private SocketChannel clientSocket;
	private SocketChannel originServerSocket;
	private Selector selector;
	private POPXY proxy;
	
	private ByteBuffer clientBuffer = ByteBuffer.allocate(CLIENT_BUFFER_SIZE);	
	private ByteBuffer serverIncommingBuffer = ByteBuffer.allocate(SERVER_INCOMMING_BUFFER_SIZE);
	
	private ByteBuffer mockServerBuffer = ByteBuffer.allocate(MOCK_SERVER_BUFFER_SIZE);
	
	private User client;
	
	private SessionStates state;
	
	private boolean firstContact = false;
	
	private boolean clientUsernameGiven = false;
	
	private boolean terminateConnection = false;
	
	private String username;
	
	private ByteBuffer bufferToWrite;
	private ByteBuffer bufferToRead;
	private SocketChannel channelToWrite;
	private SocketChannel channelToRead;
	
	public ClientSession(SelectionKey key) throws IOException {
		this.selector = key.selector();
		this.proxy = POPXY.getInstance();
		this.clientSocket = ((ServerSocketChannel)key.channel()).accept();
		this.state = SessionStates.WAITING_CONNECTION_TO_SERVER;
		this.channelToWrite = clientSocket;
		this.firstContact = true;
		clientSocket.register(selector, SelectionKey.OP_WRITE, this);
	}

	public void handleConnection() {
		// TODO Auto-generated method stub

	}

	public void handleWrite() {
		SocketChannel toSuscribe = null;
		int suscriptionMode = SelectionKey.OP_CONNECT;
		switch (state) {
		case WAITING_CONNECTION_TO_SERVER:
			try {
				//TODO: Consultar bloqueo de IP
				/*if(IPisBlocked()){
				 	mockServerBuffer.clear();
				 	mockserverBuffer.put("-ERR/r/n".getBytes());
				 	mockServerBuffer.flip();
					toWrite = mockServerBuffer;
					terminateConnection = true;
				} else */if(firstContact) {
					mockServerBuffer.clear();
					mockServerBuffer.put("+OK/r/n".getBytes());
					mockServerBuffer.flip();
					bufferToWrite = mockServerBuffer;
					bufferToRead = clientBuffer;
					firstContact = false;
				}
				
				channelToWrite.write(bufferToWrite);
				toSuscribe = clientSocket;
				suscriptionMode = SelectionKey.OP_READ;
			} catch (IOException e) {
				// TODO: handle exception
			}
			break;
		default:
			//TODO: Should never happen, and rise an exception or terminate the client
			break;
		}

		try {
			toSuscribe.register(selector, suscriptionMode, this);
		} catch (ClosedChannelException e) {
			//TODO: Should close client gracefully.
			e.printStackTrace();
		}
	}

	public void handleRead() {
		SocketChannel toSuscribe = clientSocket;
		int suscriptionMode = SelectionKey.OP_CONNECT;
		String incommingMsg;
		String command;

		switch (this.state) {
		case WAITING_CONNECTION_TO_SERVER:
			try {
				
				clientSocket.read(clientBuffer);

				incommingMsg = clientBuffer.toString().trim();
				command = incommingMsg.substring(0, incommingMsg.indexOf(' '));
				mockServerBuffer.clear();
				
				username = incommingMsg.substring(incommingMsg.indexOf(' '));
				
				if(command.equalsIgnoreCase("USER")) {
					//TODO: hacete esta... función
					if(proxy.userIsBlocked(username)) {
						mockServerBuffer.put("-ERR/r/n".getBytes());
						terminateConnection = true;
						mockServerBuffer.flip();
						bufferToWrite = mockServerBuffer;
						channelToWrite = toSuscribe = clientSocket;
						suscriptionMode = SelectionKey.OP_WRITE;
					} else {
						if((client = proxy.getUser(username)) == null) {
							channelToWrite = toSuscribe = originServerSocket = (new Socket(proxy.getDefaultOriginServer(), proxy.getDefaultOriginServerPort())).getChannel();
						} else {
							channelToWrite = toSuscribe = originServerSocket = (new Socket(client.getServerAddress(), client.getServerPort())).getChannel();
						}
					}
				} else {
					mockServerBuffer.put("-ERR/r/n".getBytes());
					mockServerBuffer.flip();
					bufferToWrite = mockServerBuffer;
					channelToWrite = toSuscribe = clientSocket;
					suscriptionMode = SelectionKey.OP_WRITE;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
			
		case WAITING_CLIENT_TO_READ:
			break;
		
		case WAITING_SERVER_TO_READ:
			break;

		default:
			//TODO: should never happen, and should rise an exception or terminate the client.
			break;
		}
		
		try {
			toSuscribe.register(selector, suscriptionMode, this);
		} catch (ClosedChannelException e) {
			//TODO: Should close client gracefully.
			e.printStackTrace();
		}

	}
	
	private void sendErrorToClient() {
		
		// TODO Auto-generated method stub
		
	}

	private boolean checkClient() throws IOException {
		//TODO:
		return false;
	}
}
