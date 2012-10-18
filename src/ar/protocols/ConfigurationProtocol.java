package ar.protocols;

import java.nio.ByteBuffer;

import ar.sessions.utils.ConfigurationCommands;

public class ConfigurationProtocol {
	
  private static final String WELLCOME_MSG = "+OK. Conexion establecida.\n";
private static final String INV_COM_MSG = "-ERR. Comando invalido.\n";
private static final String OK_MSG = "+OK.\n";
private static final String ERR_MSG = "-ERR.\n";

	public static String getWellcomeMsg() {		
		return WELLCOME_MSG;
	}

	public static boolean isValidCommand(ByteBuffer commandBuf) {
		// TODO Auto-generated method stub
		return false;
	}

	public static String getInvalidCommandMsg() {
		// TODO Auto-generated method stub
		return null;
	}

	public static ConfigurationCommands getCommand(ByteBuffer commandBuf) {
		// TODO Auto-generated method stub 
		return null;
	}

	public static ConfigurationCommands getSubCommandAndParameters(ByteBuffer parametersBuf, String[] parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	public static String getInvalidSubCommandMsg() {
		// TODO Auto-generated method stub
		return null;
	}

	public static String getInvalidUserMsg() {
		// TODO Auto-generated method stub
		return null;
	}

	public static String getOkMsg() {
		// TODO Auto-generated method stub
		return null;
	}

	public static String getInvalidArgumentMsg() {
		// TODO Auto-generated method stub
		return null;
	}

	public static String getInvalidCommandsMsg() {
		// TODO Auto-generated method stub
		return null;
	}

}
