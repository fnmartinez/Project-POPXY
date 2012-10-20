package ar.sessions;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ar.POPXY;
import ar.elements.User;
import ar.sessions.utils.POPHeadCommands;
import ar.sessions.utils.SessionState;

public class ClientSession implements Session {

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

	private POPXY proxy;

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

	private SessionState state;

	private POPHeadCommands lastCommand;

	private boolean clientUsernameGiven = false;

	private boolean terminateConnection = false;

	private String username;

	private ByteBuffer[] bufferToWrite;
	private ByteBuffer[] bufferToRead;
	private SocketChannel channelToWrite;
	private SocketChannel channelToRead;
	private boolean firstContact;
	private boolean mockingServer;
	private boolean verifyServerStatus;
	private boolean loginRetry;
	private boolean recentlyConnected;

	public ClientSession(SelectionKey key) throws IOException {
		this.selector = key.selector();
		this.proxy = POPXY.getInstance();
		this.clientSocket = ((ServerSocketChannel) key.channel()).accept();
		this.state = SessionState.AUTH_STATE;
		this.lastCommand = POPHeadCommands.NONE;
		this.channelToWrite = clientSocket;
		this.firstContact = true;
		this.mockingServer = true;
		clientSocket.register(selector, SelectionKey.OP_WRITE, this);
	}

	public void handleConnection() {
		// TODO Auto-generated method stub

		this.bufferToRead = firstServerBuffer;
		this.channelToRead = originServerSocket;
		this.verifyServerStatus = true;
		this.recentlyConnected = true;

		try {
			channelToRead.register(selector, SelectionKey.OP_READ, this);
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void handleWrite() {
		SocketChannel toSuscribe = null;
		int suscriptionMode = SelectionKey.OP_CONNECT;

		if (firstContact) {
			try {
				// TODO: Consultar bloqueo de IP
				clearBuffer(mockServerBuffer);

				mockServerBuffer[0].put("+OK/r/n".getBytes());

				flipBuffer(mockServerBuffer);

				clientSocket.write(mockServerBuffer);
				setToRead(clientSocket, clientBuffer);
				toSuscribe = clientSocket;
				suscriptionMode = SelectionKey.OP_READ;
			} catch (IOException e) {
				// TODO: handle exception
			}

			this.firstContact = false;
		} else {
			switch (state) {
			case AUTH_STATE:
				switch (lastCommand) {
				case NONE:
					try {

						channelToWrite.write(bufferToWrite);
						toSuscribe = channelToWrite;
						suscriptionMode = SelectionKey.OP_READ;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case USER:

					try {
						channelToWrite.write(bufferToWrite);
						toSuscribe = channelToWrite;
						suscriptionMode = SelectionKey.OP_READ;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;
				case PASS:
					
					try {
						channelToWrite.write(bufferToWrite);
						toSuscribe = channelToWrite;
						suscriptionMode = SelectionKey.OP_READ;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				default:
					break;
				}
				break;
			case TRANSACTION_STATE:
				break;
			case UPDATE_STATE:
				break;
			default:
				// TODO: Should never happen, and rise an exception or terminate
				// the client
				break;
			}
		}

		try {
			toSuscribe.register(selector, suscriptionMode, this);
		} catch (ClosedChannelException e) {
			// TODO: Should close client gracefully.
			e.printStackTrace();
		}
	}

	public void handleRead() {
		SocketChannel toSuscribe = clientSocket;
		int suscriptionMode = SelectionKey.OP_CONNECT;
		String cmd;
		String[] args;

		switch (this.state) {
		case AUTH_STATE:
			switch (lastCommand) {
				// This state happens when Session has just started or
				// when there was no login.
			case NONE:
				
				try {
					channelToRead.read(bufferToRead);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				cmd = bufferToRead[0].toString().trim();
				args = bufferToRead[0].toString().trim().split("\\s");
				
				if (cmd.compareToIgnoreCase(POPHeadCommands.USER
						.toString()) == 0
						&& args != null
						&& args[0] != null
						&& args[0].compareTo("") != 0) {
					if (proxy.userIsBlocked(args[0])) {
						mockServerBuffer[0].put("-ERR/r/n".getBytes());
						
						setToWrite(clientSocket, mockServerBuffer);
						
						toSuscribe = clientSocket;
						suscriptionMode = SelectionKey.OP_WRITE;
					} else {
						if(originServerSocket != null) {
							try {
								originServerSocket.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if ((client = proxy.getUser(args[0])) == null) {
							try {
								toSuscribe = originServerSocket = (new Socket(
										proxy.getDefaultOriginServer(),
										proxy.getDefaultOriginServerPort()))
										.getChannel();
							} catch (UnknownHostException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							try {
								toSuscribe = originServerSocket = (new Socket(
										client.getServerAddress(),
										client.getServerPort()))
										.getChannel();
							} catch (UnknownHostException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						this.lastCommand = POPHeadCommands.USER;
						this.verifyServerStatus = true;
						suscriptionMode = SelectionKey.OP_CONNECT;
					}
				} else if (cmd.compareToIgnoreCase(POPHeadCommands.QUIT
						.toString()) == 0) {
					// TODO:
					mockServerBuffer[0].put("+OK/r/n".getBytes());
					
					this.lastCommand = POPHeadCommands.QUIT;
					setToWrite(channelToRead, mockServerBuffer);
					toSuscribe = channelToRead;
					suscriptionMode = SelectionKey.OP_WRITE;
					this.terminateConnection = true;
				} else {
					mockServerBuffer[0].put("-ERR/r/n".getBytes());
					
					setToWrite(channelToRead, mockServerBuffer);
					toSuscribe = channelToRead;
					suscriptionMode = SelectionKey.OP_WRITE;
				}
					
				break;
			case USER:
				try {
					channelToRead.read(bufferToRead);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if(this.verifyServerStatus) {
					if(this.bufferToRead[0].toString().trim()
							.compareToIgnoreCase("+OK") == 0) {
						this.verifyServerStatus = false;
						setToWrite(toSuscribe = clientSocket, bufferToRead);
					}
				} else {
					if(this.bufferToWrite[0].toString().trim().compareToIgnoreCase("PASS") == 0) {
						this.verifyServerStatus = true;
						this.lastCommand = POPHeadCommands.PASS;
					}
					setToWrite(toSuscribe = originServerSocket, bufferToRead);
					
				}
				
				suscriptionMode = SelectionKey.OP_WRITE;
					

				break;
			case PASS:
				try {
					channelToRead.read(bufferToRead);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if(this.verifyServerStatus) {
					if(this.bufferToRead[0].toString().trim()
							.compareToIgnoreCase("+OK") == 0) {
						this.verifyServerStatus = false;
						this.state = SessionState.TRANSACTION_STATE;
					} else {
						this.verifyServerStatus = true;
					}
					this.lastCommand = POPHeadCommands.NONE;
					setToWrite(toSuscribe = clientSocket, bufferToRead);
				} else {
					if(this.bufferToWrite[0].toString().trim().compareToIgnoreCase("PASS") == 0) {
						this.verifyServerStatus = true;
						this.lastCommand = POPHeadCommands.PASS;
					}
					setToWrite(toSuscribe = originServerSocket, bufferToRead);
					
				}
				
				suscriptionMode = SelectionKey.OP_WRITE;
					
				break;
			default:
				break;
			}
			break;

		case TRANSACTION_STATE:
			switch(lastCommand) {
			case NONE:
				
				try {
					channelToRead.read(bufferToRead);
					
					if(this.verifyServerStatus) {
						
					} else {
						
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case DELE:
				break;
			case LIST:
				break;
			case NOOP:
				break;
			case RETR:
				break;
			case QUIT:
				break;
			case RSET:
				break;
			case STAT:
				break;
			case TOP:
				break;
			case UIDL:
				break;
			case UKWN:
				break;
			default:
				break;
			}
			break;

		case UPDATE_STATE:
			break;

		default:
			// TODO: should never happen, and should rise an exception or
			// terminate the client.
			break;
		}

		try {
			toSuscribe.register(selector, suscriptionMode, this);
		} catch (ClosedChannelException e) {
			// TODO: Should close client gracefully.
			e.printStackTrace();
		}

	}

	private void setToWrite(SocketChannel sc, ByteBuffer[] bf) {
		this.bufferToWrite = bf;
		flipBuffer(this.bufferToWrite);
		this.channelToWrite = sc;
	}

	private void setToRead(SocketChannel sc, ByteBuffer[] bf) {
		this.bufferToRead = bf;
		clearBuffer(bufferToRead);
		this.channelToRead = sc;
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
}
