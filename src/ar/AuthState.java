package ar;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import ar.sessions.ClientSession;
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
			
			String cmd = new String(session.getClientBuffer()[0].array());
			cmd = cmd.trim();
			
			boolean validArgument = (session.getClientBuffer()[1].hasRemaining() && session.getClientBuffer()[1].get(0) == ' '); 
			String[] args = session.getClientBuffer()[1].toString().trim().split("\\s");
			
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
					bufferToUse[1].put(" You cannot login right now./r/n".getBytes());
					
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
						session.setOriginServerSocket((new Socket(
								session.getClient().getServerAddress(),
								session.getClient().getServerPort()))
								.getChannel());
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					this.setFlowToWriteServer();
					response.setChannel(session.getOriginServerSocket());
					response.setOperation(SelectionKey.OP_CONNECT);
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
				bufferToUse[1].put("Farewell./r/n".getBytes());
				
				bufferToUse[0].flip();
				bufferToUse[1].flip();
				
				response.setBuffers(bufferToUse);
				response.setChannel(session.getClientSocket());
				response.setOperation(SelectionKey.OP_WRITE);
				response.setState(new QuitState());
			} else {
				bufferToUse = session.getFirstServerBuffer();
				bufferToUse[0].clear();
				bufferToUse[1].clear();
				
				bufferToUse[0].put("-ERR".getBytes());
				bufferToUse[1].put(" Invalid opcode./r/n".getBytes());
				
				bufferToUse[0].flip();
				bufferToUse[1].flip();
				
				response.setBuffers(bufferToUse);
				response.setChannel(session.getClientSocket());
				response.setOperation(SelectionKey.OP_WRITE);
				response.setState(this);
			}
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
			
			
			// If I get any '-ERR', dispite being the first or second iteration, I should send it to 
			// the user. Else, if it's the first time, I should send the command 'USER <username>/r/n'
			// to the server, using the ClientBuffer.
			if(session.getFirstServerBuffer()[0].toString().trim().equalsIgnoreCase("+OK")) {
				
				// If it's the first time, it means I might need to send the user name to the server.
				// Hence, I'll write with the ClientBuffer.
				if(this.usernameSend) {
					bufferToUse = session.getFirstServerBuffer();
				} else {
					bufferToUse = session.getClientBuffer();
					bufferToUse[0].clear();
					bufferToUse[1].clear();
					bufferToUse[0].put("USER".getBytes());
					bufferToUse[1].put((session.getClient().getUser() + "/r/n").getBytes());
					bufferToUse[0].flip();
					bufferToUse[1].flip();
				}
			} else {
				errorRecieved = true;
				bufferToUse = session.getFirstServerBuffer();
			}
			
			
			response.setBuffers(bufferToUse);
			response.setChannel(((usernameSend)?session.getClientSocket():session.getOriginServerSocket()));
			response.setOperation(SelectionKey.OP_WRITE);
			
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
			this.setFlowToWriteServer();
			
			if(!usernameSend) {
				usernameSend = true;
			}
			
			return response;
		}
		
		@Override
		Response afterReadingFromClient(ClientSession session) {
			
			Response response = new Response();
			
			POPHeadCommands cmd = POPHeadCommands.getLiteralByString(session.getClientBuffer()[0].toString().trim());
			
			boolean validArgument = (session.getClientBuffer()[1].hasRemaining() && session.getClientBuffer()[1].get(0) == ' '); 
			String[] args = session.getClientBuffer()[1].toString().trim().split("\\s");
			
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
				ByteBuffer[] bufferToUse = session.getFirstServerBuffer();
				bufferToUse[0].clear();
				bufferToUse[1].clear();
				
				bufferToUse[0].put("-ERR".getBytes());
				bufferToUse[1].put(" Invalid opcode./r/n".getBytes());
				
				bufferToUse[0].flip();
				bufferToUse[1].flip();
				
				response.setBuffers(bufferToUse);
				response.setChannel(session.getClientSocket());
				response.setOperation(SelectionKey.OP_WRITE);
				response.setState(new NoneState());
				break;
			}
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
			bufferToUse[1].put(" Invalid command./r/n".getBytes());
			
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
			

			if(session.getFirstServerBuffer()[0].toString().trim().equalsIgnoreCase("+OK")) {
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
