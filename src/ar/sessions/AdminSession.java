package ar.sessions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;

import javax.activity.InvalidActivityException;

import ar.POPXY;
import ar.elements.User;
import ar.protocols.ConfigurationProtocol;
import ar.sessions.utils.ConfigurationCommands;

public class AdminSession implements Session {


    private static final int HEAD_COMMAND_SIZE = 4;
    private static final int BUF_SIZE = 512;
    private static final int ANSWER_BUF_SIZE = 100;
	private static final int WELLCOME_CHANNEL = 0;
	private static final int CONGIF_CHANNEL = 1;
	private static final int ORIGIN_CHANNEL = 2;
	private static final int STATS_CHANNEL = 3;
    
	private SocketChannel adminChannel;
	private Selector selector;
	private ByteBuffer commandBuf;
	private ByteBuffer parametersBuf;
	private ByteBuffer answerBuf;
	private SelectionKey key;

	
	public AdminSession(SelectionKey key) throws IOException {
		
		this.commandBuf = ByteBuffer.allocate(HEAD_COMMAND_SIZE);
		this.parametersBuf = ByteBuffer.allocate(BUF_SIZE);
		this.answerBuf = ByteBuffer.allocate(ANSWER_BUF_SIZE);
		
		this.selector = key.selector();		
		this.adminChannel = ((ServerSocketChannel) key.channel()).accept();
		adminChannel.configureBlocking(false); 
        
		this.answer(ConfigurationProtocol.getWellcomeMsg());
		
		this.key = adminChannel.register(this.selector, SelectionKey.OP_WRITE, this);
	}

	public void handleConnection() {

	}

	public void handleWrite() {
		try {
			answerBuf.flip();
			adminChannel.write(answerBuf);			
			answerBuf.clear();
			
			this.key = adminChannel.register(this.selector, SelectionKey.OP_READ, this);
			
		} catch (ClosedChannelException e) {
			this.handleEndConection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void handleRead() {
        long bytesRead = 0;
        
        commandBuf.clear();
        parametersBuf.clear();
    	
        ByteBuffer[] buffers = {commandBuf, parametersBuf};
        
    	try {
			bytesRead = adminChannel.read(buffers);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        if (bytesRead == -1) { // El administrador cerro la conexion!
            handleEndConection();
        } else if (bytesRead > 0) {

        	commandBuf.flip();
        	parametersBuf.flip();
        	
        	ConfigurationCommands command =  ConfigurationProtocol.getCommand(commandBuf);
        	if(command == null){
        		this.answer(ConfigurationProtocol.getInvalidCommandMsg());
        		this.key.interestOps(SelectionKey.OP_WRITE);
        		return;
        	}
        	
        	String[] parameters = null;
        	ConfigurationCommands subCommand = ConfigurationProtocol.getSubCommandAndParameters(parametersBuf, parameters);
        	if(subCommand == null){
        		this.answer(ConfigurationProtocol.getInvalidSubCommandMsg());
        		this.key.interestOps(SelectionKey.OP_WRITE);
        		return;
        	}
        	
        	switch(subCommand){
        		case  TIME_LOGIN:  				this.timeLogin(command, parameters); break;
        		case  CANT_LOGIN:  				this.cantLogin(command, parameters); break;
        		case  BLACK_IP:  				this.blackIp(command, parameters); break;
        		case  RM_FILTER_DATE:  			this.removeFilterDate(command, parameters); break;
        		case  RM_FILTER_SENDER:  		this.removeFilterSender(command, parameters); break;
        		case  RM_FILTER_HEADER:  		this.removeFilterHeader(command, parameters); break;
        		case  RM_FILTER_CONTENT:  		this.removeFilterContent(command, parameters); break;
        		case  RM_FILTER_SIZE:  			this.removeFilterSize(command, parameters); break;
        		case  RM_FILTER_DISPOSITION:	this.removeFilterDisposition(command, parameters); break;
        		case  ORIGIN_SERVER:  			this.setOriginServer(command, parameters); break;
        		case  ORIGIN_SERVER_PORT:  		this.setListeningPort(ORIGIN_CHANNEL, command, parameters); break;
        		case  CONFIG_LISTENING_PORT:  	this.setListeningPort(CONGIF_CHANNEL, command, parameters); break;
        		case  WELLCOME_LISTENING_PORT:  this.setListeningPort(WELLCOME_CHANNEL, command, parameters); break;
        		case  STA_LISTENING_PORT:  		this.setListeningPort(STATS_CHANNEL, command, parameters); break;
        		case  APP:  					this.setApp(command, parameters); break;
        		default:						System.out.println("ERRORRRR"); return;
        	};

    		this.key.interestOps(SelectionKey.OP_WRITE);
    		return;
        }

	}
	
	private void setListeningPort(int channel, ConfigurationCommands command, String[] parameters) {

		if(command == ConfigurationCommands.SET){
			POPXY p = POPXY.getInstance();
			//El primer parametro es el puerto: SET PORT xxxxx
			Integer port = Integer.getInteger(parameters[0]);
			if(port != null){
				
				switch(channel){
					case WELLCOME_CHANNEL: p.setWellcomePort(port);break;
					case CONGIF_CHANNEL: p.setAdminPort(port);break;
					case ORIGIN_CHANNEL: p.setOriginPort(port);break;
					case STATS_CHANNEL: p.setStatsPort(port);break;
					default: System.out.println("ERROR!!!");
				}
				
				
				this.answer(ConfigurationProtocol.getOkMsg());
				return;
			}
			this.answer(ConfigurationProtocol.getInvalidArgumentMsg());
			return;
		}
		this.answer(ConfigurationProtocol.getInvalidCommandsMsg());
		return;
	}	

	private void setOriginServer(ConfigurationCommands command, String[] parameters) {
		
		POPXY popxy = POPXY.getInstance();
		User user = null;
		if(parameters.length == 2){
			user = popxy.getUser(parameters[1]);
			//Si el usuario no existe
			if(user == null){
				answer(ConfigurationProtocol.getInvalidUserMsg());
				return;
			}
		}
		
	}

	private void timeLogin(ConfigurationCommands command, String[] parameters) {
		POPXY popxy = POPXY.getInstance();
		User user = null;
		//Si me pasa como parametro un usuario(no es global):
		if(parameters.length == 3){
			user = popxy.getUser(parameters[2]);
			//Si el usuario no existe
			if(user == null){
				answer(ConfigurationProtocol.getInvalidUserMsg());
				return;
			}
		}
		
		
		
	}
	
	private void setApp(ConfigurationCommands command, String[] parameters) {
		// TODO Auto-generated method stub
		
	}

	
	private void removeFilterDisposition(ConfigurationCommands command,	String[] parameters) {
		// TODO Auto-generated method stub
		
	}

	private void removeFilterSize(ConfigurationCommands command, String[] parameters) {
		// TODO Auto-generated method stub
		
	}

	private void removeFilterContent(ConfigurationCommands command,	String[] parameters) {
		// TODO Auto-generated method stub
		
	}

	private void removeFilterHeader(ConfigurationCommands command,
			String[] parameters) {
		// TODO Auto-generated method stub
		
	}

	private void removeFilterSender(ConfigurationCommands command,
			String[] parameters) {
		// TODO Auto-generated method stub
		
	}

	private void removeFilterDate(ConfigurationCommands command,
			String[] parameters) {
		// TODO Auto-generated method stub
		
	}

	private void blackIp(ConfigurationCommands command, String[] parameters) {
		// TODO Auto-generated method stub
		
	}

	private void cantLogin(ConfigurationCommands command, String[] parameters) {
		// TODO Auto-generated method stub
		
	}



	private void answer(String answer) {
		this.answerBuf.put(answer.getBytes());
		
	}

	private void handleEndConection(){
		//TODO
System.out.println("Se cerro la conexion del administrador!!!");
		try {
			adminChannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


}
