package ar.parsers;

import java.nio.ByteBuffer;

import ar.sessions.utils.HeadCommands;

public class ConfigurationProtocolParser {
	
	private static final String[] headCommands = {"ADD", "DEL", "STA"};


	public static HeadCommands parseHeadCommand(ByteBuffer commandBuf) {
		
		for(int i=0; i < headCommands.length;i++){
			for(int j=0; j< commandBuf.array().length; j++){
				if((commandBuf.array())[j] != (headCommands[i].getBytes()[j])){
					break;
				}
			if( (j+1) == commandBuf.array().length)
				return HeadCommands.valueOf(headCommands[i]);
			}
			
		}
		return HeadCommands.values()[0];		
	}


	public static String[] parseParameters(ByteBuffer parametersBuf) {
		// TODO Auto-generated method stub
		return null;
	}

}
