package ar;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import ar.sessions.ClientSession;
import ar.sessions.utils.BufferUtils;
import ar.sessions.utils.POPHeadCommands;

public class AuthState implements State {
	
	State currentState;

	public AuthState(){
		super();
		this.currentState = new NoneState(null);
		((AbstractInnerState)this.currentState).setFlowToWriteClient();
	}
	
	
	private class NoneState extends AbstractInnerState{

		public NoneState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}

		@Override
		Response afterReadingFromClient(ClientSession session) {
			
			Response response = new Response();
			
			POPHeadCommands cmd = POPHeadCommands.getLiteralByString(BufferUtils.byteBufferToString(session.getClientBuffer()).substring(0, 5).trim());

			String[] args = BufferUtils.byteBufferToString(session.getClientBuffer()).substring(4).trim().split("\\s");
	
			ByteBuffer bufferToUse = null;
			
			switch(cmd) {
			case USER:
				if(args != null	&& args[0] != null	&& args[0].compareTo("") != 0) {
					if (POPXY.getInstance().userIsBlocked(args[0])) {
						bufferToUse = session.getFirstServerBuffer();
						bufferToUse.clear();
						
						bufferToUse.put("-ERR You cannot login right now.\r\n".getBytes());
						
						bufferToUse.flip();
						
						response.setBuffers(bufferToUse);
						response.setChannel(session.getClientSocket());
						response.setOperation(SelectionKey.OP_WRITE);
						response.setState(new QuitState(this));
					} else {
						if(session.getOriginServerSocket() != null) {
							try {
								session.getOriginServerSocket().close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						session.setClient(POPXY.getInstance().getUser(args[0]));
						try {
							
							String host = session.getClient().getServerAddress();
							int port = session.getClient().getServerPort();

							SocketChannel socketChannel = SocketChannel.open();
							socketChannel.connect(new InetSocketAddress(host, port));
							POPXY.getLogger().info("Conecting to "+ host);
							while(! socketChannel.finishConnect() ){
							    System.out.println(".");    
							}
							POPXY.getLogger().info("Connected to server "+ host);
							session.setOriginServerSocket(socketChannel);
							
						} catch (UnknownHostException e) {
							e.printStackTrace();
//							response.setState(null);
//							return response;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						this.setFlowToWriteServer();
						response.setChannel(session.getOriginServerSocket());
						response.setBuffers(session.getFirstServerBuffer());
						response.setOperation(SelectionKey.OP_READ);
						response.setState(new UserState(this));
						((AbstractInnerState)response.getState()).setFlowToReadServer();
					}
				}
				break;
			case QUIT:
				// TODO:
				bufferToUse = session.getFirstServerBuffer();
				bufferToUse.clear();
				
				bufferToUse.put("+OK Farewell.\r\n".getBytes());
				
				bufferToUse.flip();
				
				response.setBuffers(bufferToUse);
				response.setChannel(session.getClientSocket());
				response.setOperation(SelectionKey.OP_WRITE);
				response.setState(new QuitState(this));
				break;
			default:
				response = invalidCommand(session);
				break;
			}

			return response;
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
	
		
		public boolean isEndState() {
			return false;
		}
		
		public String toString(){
			return "None";
		}

		@Override
		public void callbackFunction() {
			// TODO Auto-generated method stub
			
		}
	}

	private class UserState extends AbstractInnerState{
		
		private boolean usernameSend = false;
		
		public UserState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		Response afterReadingFromServer(ClientSession session) {
			Response response = new Response();
			
			boolean errorRecieved = false;
			ByteBuffer bufferToUse = session.getFirstServerBuffer();
			
			String cmd = BufferUtils.byteBufferToString(session.getFirstServerBuffer()).substring(0, 4);
			cmd = cmd.trim();
			// If I get any '-ERR', dispite being the first or second iteration, I should send it to 
			// the user. Else, if it's the first time, I should send the command 'USER <username>/r/n'
			// to the server, using the ClientBuffer.
			if(cmd.equalsIgnoreCase("+OK")) {
				
				// If it's the first time, it means I might need to send the user name to the server.
				// Hence, I'll write with the ClientBuffer.
				if(this.usernameSend) {
					bufferToUse = session.getFirstServerBuffer();
					this.setFlowToWriteClient();
					response.setOperation(SelectionKey.OP_WRITE);

				} else {

					this.setFlowToWriteServer();
					bufferToUse = session.getClientBuffer();
					bufferToUse.clear();
					bufferToUse.put(("USER "+session.getClient().getUser() + "\r\n").getBytes());
					bufferToUse.flip();
					response.setOperation(SelectionKey.OP_WRITE);
				}
			} else {
				this.setFlowToWriteClient();
				errorRecieved = true;
				bufferToUse = session.getFirstServerBuffer();
				response.setOperation(SelectionKey.OP_WRITE);
			}
			
			
			response.setBuffers(bufferToUse);
			response.setChannel(((usernameSend)?session.getClientSocket():session.getOriginServerSocket()));
			
			State state = null;
			
			if(errorRecieved && !this.usernameSend) {
				// The server is not a POP3 enabled server.
				state = null;
			} else if (this.usernameSend && errorRecieved){
				// The username is not valid
				if(this.getCallbackState() == null) {
					state = new NoneState(null);
				} else {
					state = this.getCallbackState();
				}
			} else if (this.usernameSend && !errorRecieved){
				// The username is valid
				if(this.getCallbackState() == null) {
					state = new UserState(new NoneState(null));
				} else {
					state = new UserState(this.getCallbackState());
				}
			} else if (!this.usernameSend && !errorRecieved) {
				// I must send the user name the next time
				state = this;
			} else {
				// This is bad.
				state = null;
			}
			response.setState(state);
			
			if(!usernameSend) {
				usernameSend = true;
			}
			
			return response;
		}
		
		@Override
		Response afterReadingFromClient(ClientSession session) {
			
			Response response = new Response();
			
			String command = BufferUtils.byteBufferToString(session.getClientBuffer()).trim();
			if(command.length() >= 5){
				command = command.substring(0, 5);
			}
			POPHeadCommands cmd = POPHeadCommands.getLiteralByString(command);

			AbstractInnerState tmpState = null;
			
			response = super.afterReadingFromClient(session);
			switch(cmd){
			case PASS:
					tmpState = new PassState(this);
				break;
			case QUIT:
					tmpState = new QuitState(this);

				break;
			default:
				tmpState = this;
				response = invalidCommand(session);
				break;
			}
			tmpState.setFlowToWriteServer();
			response.setState(tmpState);

			return response;
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
			AbstractInnerState tmpState;
			if(this.getCallbackState() == null) {
				tmpState = new NoneState(null);
			} else {
				tmpState = this.getCallbackState();
			}
			response.setState(tmpState);
			this.setFlowToWriteClient();
			return response;
		}
		
		public String toString(){
			return "User";
		}

		@Override
		public void callbackFunction() {
			// TODO Auto-generated method stub
			
		}
	
	}
	
	private class PassState extends AbstractInnerState implements EndState {
		
		private boolean isFinalState = false;
		
		public PassState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		Response afterReadingFromServer(ClientSession session) {
			Response response = new Response();
			
			response = super.afterReadingFromServer(session);
			

			if(BufferUtils.byteBufferToString(session.getFirstServerBuffer()).startsWith("+OK")) {
				this.isFinalState = true;
				session.getClient().login();
			} else {
				AbstractInnerState tmpState;
				if(this.getCallbackState() == null) {
					tmpState = new NoneState(null);
				} else {
					tmpState = this.getCallbackState();
				}
				response.setState(tmpState);
			}

			
			return response;
		
		}

		public boolean isEndState() {
			return this.isFinalState;
		}

		public State getNextState() {
			return new TransactionState();
		}
		
		public String toString(){
			return "Pass";
		}

		@Override
		public void callbackFunction() {
			// TODO Auto-generated method stub
			
		}
	}
	
	private class QuitState extends AbstractInnerState implements EndState {
		
		boolean isFinalState = false;

		public QuitState(AbstractInnerState callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		Response afterWritingToClient(ClientSession session) {
			Response response = new Response();
			
			response = super.afterWritingToClient(session);
			this.isFinalState = true;
			response.setState(this);
			return response;
		
		}
		
		
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
		return "Auth("+this.currentState+")";
	}

}
