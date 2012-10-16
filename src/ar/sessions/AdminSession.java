package ar.sessions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;

import ar.parsers.ConfigurationProtocolParser;
import ar.sessions.utils.HeadCommands;

public class AdminSession implements Session {


    private static final int HEAD_COMMAND_SIZE = 3;
    private static final int bufSize = 512;
    private static final int ANSWER_BUF_SIZE = 100;
    private static final String WELLCOME_MSG = "+ok. Conexion establecida.\n";
    private static final String INV_COM_MSG = "-ERR. Comando invalido.\n";
    private static final String OK_MSG = "+OK.\n";
    
	private SocketChannel adminChannel;
	private Selector selector;
	private ByteBuffer commandBuf;
	private ByteBuffer parametersBuf;
	private ByteBuffer answerBuf;
	private SelectionKey key;

	
	public AdminSession(SelectionKey key) throws IOException {
		
		this.commandBuf = ByteBuffer.allocate(HEAD_COMMAND_SIZE);
		this.parametersBuf = ByteBuffer.allocate(bufSize);
		this.answerBuf = ByteBuffer.allocate(ANSWER_BUF_SIZE);
		
		this.selector = key.selector();		
		this.adminChannel = ((ServerSocketChannel) key.channel()).accept();
		adminChannel.configureBlocking(false); 
        
		writeStringIntoBuffer(answerBuf, WELLCOME_MSG);
		
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
			// TODO Se cerro la conexion antes de q le pueda responder!!!
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void writeStringIntoBuffer(ByteBuffer buf, String msg){
		for(int i=0; i < msg.length();i++){
			buf.putChar(msg.charAt(i));
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
        	
        	HeadCommands headCommand = ConfigurationProtocolParser.parseHeadCommand(commandBuf);
        	
        	if(headCommand == HeadCommands.INVALID_COMMAND){ 
        		writeStringIntoBuffer(answerBuf, INV_COM_MSG);
        		this.key.interestOps(SelectionKey.OP_WRITE);
        		return;
        	}

    		String[] parameters = ConfigurationProtocolParser.parseParameters(parametersBuf);
    		
        	if(headCommand == HeadCommands.ADD){        		
//TODO!!!
        		
        		this.key.interestOps(SelectionKey.OP_WRITE);
        		return;
        	}
        	
        	for(int i=0; i < OK_MSG.length();i++){
    			answerBuf.putChar(OK_MSG.charAt(i));
    		}
    		this.key.interestOps(SelectionKey.OP_WRITE);
    		return;
            // Indicate via key that reading/writing are both of interest now.
        }

	}

	private void handleEndConection(){
		//TODO
		try {
			adminChannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


}
