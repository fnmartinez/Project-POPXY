package ar;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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
		this.currentState = new NoneState();
		((AbstractInnerState)this.currentState).setFlowToWriteClient();
	}
	
	
	private class NoneState extends AbstractInnerState{

		@Override
		Response afterReadingFromClient(ClientSession session) {
			
			Response response = new Response();
			
			String cmd = BufferUtils.byteBufferToString(session.getClientBuffer()[0]);
			cmd = cmd.trim();
			byte firstCaracter = session.getClientBuffer()[1].get(0);
			boolean validArgument = (session.getClientBuffer()[1].hasRemaining() && (firstCaracter == '\n' || firstCaracter == ' ')); 
			String[] args = (BufferUtils.byteBufferToString(session.getClientBuffer()[1]).trim()).split("\\s");
			
			ByteBuffer[] bufferToUse = null;
			
			if (cmd.compareToIgnoreCase(POPHeadCommands.USER
					.toString()) == 0
					&& validArgument
					&& args != null
					&& args[0] != null
					&& args[0].compareTo("") != 0) {
				if (POPXY.getInstance().userIsBlocked(args[0])) {
					bufferToUse = session.getFirstServerBuffer();
					bufferToUse[0].clear();
					bufferToUse[1].clear();
					
					bufferToUse[0].put("-ERR".getBytes());
					bufferToUse[1].put(" You cannot login right now.\r\n".getBytes());
					
					bufferToUse[0].flip();
					bufferToUse[1].flip();
					
					response.setBuffers(bufferToUse);
					response.setChannel(session.getClientSocket());
					response.setOperation(SelectionKey.OP_WRITE);
					response.setState(new QuitState());
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
						socketChannel.configureBlocking(false);
						socketChannel.connect(new InetSocketAddress(host, port));
						while(! socketChannel.finishConnect() ){
						    System.out.println(".");    
						}
						session.setOriginServerSocket(socketChannel);
						
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					this.setFlowToWriteServer();
					response.setChannel(session.getOriginServerSocket());
					response.setBuffers(session.getFirstServerBuffer());
					response.setOperation(SelectionKey.OP_READ);
					response.setState(new UserState());
					((AbstractInnerState)response.getState()).setFlowToReadServer();
				}
			} else if (cmd.compareToIgnoreCase(POPHeadCommands.QUIT
					.toString()) == 0
					&& validArgument) {
				// TODO:
				bufferToUse = session.getFirstServerBuffer();
				bufferToUse[0].clear();
				bufferToUse[1].clear();
				
				bufferToUse[0].put("+OK ".getBytes());
				bufferToUse[1].put("Farewell.\r\n".getBytes());
				
				bufferToUse[0].flip();
				bufferToUse[1].flip();
				
				response.setBuffers(bufferToUse);
				response.setChannel(session.getClientSocket());
				response.setOperation(SelectionKey.OP_WRITE);
				response.setState(new QuitState());
			} else {
				response = invalidCommand(session);
			}
			return response;
		}

		private Response invalidCommand(ClientSession session) {
			Response response = new Response();
			ByteBuffer[] bufferToUse = session.getFirstServerBuffer();
			bufferToUse[0].clear();
			bufferToUse[1].clear();
			
			bufferToUse[0].put("-ERR".getBytes());
			bufferToUse[1].put(" Invalid command.\r\n".getBytes());
			
			bufferToUse[0].flip();
			bufferToUse[1].flip();
			
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
	}

	private class UserState extends AbstractInnerState{
		
		private boolean usernameSend = false;
		
		@Override
		Response afterReadingFromServer(ClientSession session) {
			Response response = new Response();
			
			boolean errorRecieved = false;
			ByteBuffer[] bufferToUse = session.getFirstServerBuffer();
			
			String cmd = BufferUtils.byteBufferToString(session.getFirstServerBuffer()[0]);
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
					bufferToUse[0].clear();
					bufferToUse[1].clear();
					bufferToUse[0].put("USER".getBytes());
					bufferToUse[1].put((" "+session.getClient().getUser() + "\r\n").getBytes());
					bufferToUse[0].flip();
					bufferToUse[1].flip();
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
				state = new NoneState();
			} else if (this.usernameSend && !errorRecieved){
				// The username is valid
				state = new PassState();
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
			
			POPHeadCommands cmd = POPHeadCommands.getLiteralByString(BufferUtils.byteBufferToString(session.getClientBuffer()[0]));

			byte firstCaracter = session.getClientBuffer()[1].get(0);
			boolean validArgument = (session.getClientBuffer()[1].hasRemaining() && (firstCaracter == '\n' || firstCaracter == ' '));			
			AbstractInnerState tmpState;
			
			switch(cmd){
			case PASS:
				if(validArgument){
					response = super.afterReadingFromClient(session);
					tmpState = new PassState();
					tmpState.setFlowToWriteServer();
					response.setState(tmpState);
				} else {
					response = invalidArgumentResponse(session);
				}
				break;
			case QUIT:
				if(validArgument){
					response = super.afterReadingFromClient(session);
					tmpState = new QuitState();
					tmpState.setFlowToWriteServer();
					response.setState(tmpState);
				} else {
					response = invalidArgumentResponse(session);
				}

				break;
			default:
				response = invalidCommand(session);
				break;
			}
			return response;
		}
		
		private Response invalidCommand(ClientSession session) {
			Response response = new Response();
			ByteBuffer[] bufferToUse = session.getFirstServerBuffer();
			bufferToUse[0].clear();
			bufferToUse[1].clear();
			
			bufferToUse[0].put("-ERR".getBytes());
			bufferToUse[1].put(" Invalid command.\r\n".getBytes());
			
			bufferToUse[0].flip();
			bufferToUse[1].flip();
			
			response.setBuffers(bufferToUse);
			response.setChannel(session.getClientSocket());
			response.setOperation(SelectionKey.OP_WRITE);
			response.setState(new NoneState());
			this.setFlowToWriteClient();
			return response;
		}

		private Response invalidArgumentResponse(ClientSession session) {
			Response response = new Response();
			ByteBuffer[] bufferToUse = session.getFirstServerBuffer();
			for(ByteBuffer bf: bufferToUse) {
				bf.clear();
			}
			bufferToUse = session.getFirstServerBuffer();
			bufferToUse[0].clear();
			bufferToUse[1].clear();
			
			bufferToUse[0].put("-ERR".getBytes());
			bufferToUse[1].put(" Invalid command.\r\n".getBytes());
			
			bufferToUse[0].flip();
			bufferToUse[1].flip();
			
			response.setBuffers(bufferToUse);
			response.setChannel(session.getClientSocket());
			response.setOperation(SelectionKey.OP_WRITE);
			response.setState(this);
			this.setFlowToWriteClient();
			
			return response;
		}
	}
	
	private class PassState extends AbstractInnerState implements EndState {
		
		private boolean isFinalState = false;
		@Override
		Response afterReadingFromServer(ClientSession session) {
			Response response = new Response();
			
			response = super.afterReadingFromServer(session);
			

			if(BufferUtils.byteBufferToString(session.getFirstServerBuffer()[0]).trim().equalsIgnoreCase("+OK")) {
				this.isFinalState = true;
			} else {
				response.setState(new NoneState());
			}

			
			return response;
		
		}

		public boolean isEndState() {
			return this.isFinalState;
		}

		public State getNextState() {
			return new TransactionState();
		}
	}
	
	private class QuitState extends AbstractInnerState implements EndState {

		public boolean isEndState() {
			return true;
		}

		public State getNextState() {
			return null;
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

}
