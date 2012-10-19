package ar.protocols;

import ar.POPXY;
import ar.sessions.utils.ConfigurationCommands;
import ar.sessions.utils.IpAndMask;

public class ConfigurationProtocol {
	
	private static final String OK_MSG = "+OK. ";
	private static final String ERR_MSG = "-ERR. ";
	private static final String WELLCOME_MSG = OK_MSG + "Conexion establecida\r\n";
	private static final String INV_COM_MSG = ERR_MSG + "Comando invalido\r\n";
	private static final String INV_SUBCOM_MSG = ERR_MSG + "Subcommando invalido\r\n";
	private static final String INV_CONV_COM_MSG = ERR_MSG + "Uso invalido de los comandos\r\n";
	private static final String INV_ARG_MSG = ERR_MSG + "Argumentos invalidos\r\n";
	private static final String INV_USR_MSG = ERR_MSG + "Usuario inexistente\r\n";
	private static final String EXT_MSG = OK_MSG + "Conexion cerrada exitosamente\r\n";

	private static final String[] commands = { "EXT", "SET", "DEL", "STA"};
	private static final String[] subCommands = {"TIMELOGIN", "CANTLOGIN", "BLACKIP", "RMFILTERDATE", "RMFILTERSENDER",
		"RMFILTERHEADER",	"RMFILTERCONTENT", "RMFILTERSIZE", "RMFILTERDISPOSITION",	"ORIGINSERVER", "ORIGINSERVERPORT",
		"CONFIGLISTENINGPORT", "WELLCOMELISTENINGPORT", "STALISTENINGPORT",	"APP"};
	
	
	public static ConfigurationCommands getCommand(String command) {
		
		String[] c = command.split("\\s+");
		if(c.length == 0){
			return null;
		}
		command = c[0];
		command = command.toUpperCase();
		for(int i=0; i < commands.length; i++){
			if(command.equals(commands[i])){
				return ConfigurationCommands.values()[i];
			}	
		}
		return null;
	}

	
	public static ConfigurationCommands getSubCommand(String subCommand) {
		
		String[] s = subCommand.split("\\s+");
		if(s.length == 0){
			return null;
		}
		subCommand = s[0];
		subCommand = subCommand.toUpperCase();
		
		for(int i=0; i < subCommands.length; i++){
			if(subCommand.equals(subCommands[i])){
				return ConfigurationCommands.values()[i+4];
			}	
		}
		return null;
	}
	
	public static String[] getParameters(String subCommandAndParameters, ConfigurationCommands subCommand) {

		String[] commandAndParam = subCommandAndParameters.split("\\s+");
		String parameters[] = null;
		
		switch(subCommand){
		case TIME_LOGIN:			parameters = getTimeLoginParameters(commandAndParam);break;
		case CANT_LOGIN:			parameters = getCantLoginParameters(commandAndParam);break;
		case BLACK_IP: 				parameters = getBlackIpParameters(commandAndParam);break;
		case RM_FILTER_DATE:		parameters = getRmFilterDateParameters(commandAndParam);break;
		case RM_FILTER_SENDER:		parameters = getRmFilterSenderParameters(commandAndParam);break;
		case RM_FILTER_HEADER:		parameters = getRmFilterHeaderParameters(commandAndParam);break;
		case RM_FILTER_CONTENT:		parameters = getRmFilterContentParameters(commandAndParam);break;
		case RM_FILTER_SIZE:		parameters = getRmFilterSizeParameters(commandAndParam);break;
		case RM_FILTER_DISPOSITION:	parameters = getRmFilterDispositionParameters(commandAndParam);break;
		case ORIGIN_SERVER:			parameters = getOriginServerParameters(commandAndParam);break;
		case ORIGIN_SERVER_PORT:	parameters = getListeningPortParameters(commandAndParam);break;
		case CONFIG_LISTENING_PORT: 	parameters = getListeningPortParameters(commandAndParam);break;
		case WELLCOME_LISTENING_PORT:	parameters = getListeningPortParameters(commandAndParam);break;
		case STA_LISTENING_PORT:		parameters = getListeningPortParameters(commandAndParam);break;
		case APP:						parameters = getAppParameters(commandAndParam);break;
		default: return null;
		}
		
		return parameters;
	}


	private static String[] getAppParameters(String[] commandAndParam) {
		if(commandAndParam.length != 2){
			return null;
		}
		String app[] = new String[1];
		app[0] = commandAndParam[1];
		return app;
	}

	private static String[] getListeningPortParameters( String[] commandAndParam) {
		
		if(commandAndParam.length != 2){
			return null;
		}
		String port[] = new String[1];
		port[0] = commandAndParam[1];
		
		if(!isValidPort(port[0])){
			return null;
		}

		return port;
	}

	private static String[] getOriginServerParameters(String[] commandAndParam) {
		
		if(commandAndParam.length != 2)
			return null;
		
		String server[] = new String[1];
		server[0] = commandAndParam[1];
		
		String[] servAndPort = server[0].split(":");
		
		if(servAndPort.length > 2 || servAndPort.length < 1)
			return null;
		
		if(servAndPort.length == 2){
			if(!isValidPort(servAndPort[1]))
				return null;
		}

		return server;
	}

	private static String[] getRmFilterDispositionParameters(
			String[] commandAndParam) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String[] getRmFilterSizeParameters(String[] commandAndParam) {
		
		if(commandAndParam.length < 2){
			return null;
		}
		String parameters[] = new String[1];
		String size = commandAndParam[1];
		
		Integer c = Integer.parseInt(size);
		if(c == null){
			return null;
		}
		
		if(commandAndParam.length > 2){
			parameters = new String[commandAndParam.length-1];
			for(int i = 2; i < commandAndParam.length; i++){
				parameters[i-1] = commandAndParam[i];
			}
		}
		
		parameters[0] = size;
		
		return parameters;
	}

	private static String[] getRmFilterContentParameters(
			String[] commandAndParam) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String[] getRmFilterHeaderParameters(String[] commandAndParam) {
		// TODO Auto-generated method stub
		return null;
		
	}

	private static String[] getRmFilterSenderParameters(String[] commandAndParam) {
		
		if(commandAndParam.length < 2){
			return null;
		}
		String parameters[] = new String[1];
		String sender = commandAndParam[1];
		
		if(!isValidSender(sender)){
			return null;
		}
		
		if(commandAndParam.length > 2){
			parameters = new String[commandAndParam.length-1];
			for(int i = 2; i < commandAndParam.length; i++){
				parameters[i-1] = commandAndParam[i];
			}
		}
		
		parameters[0] = sender;
		
		return parameters;
		
	}


	private static String[] getRmFilterDateParameters(String[] commandAndParam) {
		
		if(commandAndParam.length < 2){
			return null;
		}
		String parameters[] = new String[1];
		String date = commandAndParam[1];
		
		if(!isValidDate(date)){
			return null;
		}
		
		if(commandAndParam.length > 2){
			parameters = new String[commandAndParam.length-1];
			for(int i = 2; i < commandAndParam.length; i++){
				parameters[i-1] = commandAndParam[i];
			}
		}
		
		parameters[0] = date;
		
		return parameters;
		
	}

	private static String[] getBlackIpParameters(String[] commandAndParam) {
		
		if(commandAndParam.length < 2 || commandAndParam.length > 3){
			return null;
		}
		String parameters[] = new String[1];
		String ip = commandAndParam[1];
		
		if(! IpAndMask.isValidIp(ip)){
			return null;
		}
		
		if(commandAndParam.length == 3){
			parameters = new String[2];
			String mask = commandAndParam[2];
			if(! IpAndMask.isValidIp(mask)){
				return null;
			}
			parameters[1] = mask;
		}
		
		parameters[0] = ip;
		
		return parameters;
		
	}

	private static String[] getCantLoginParameters(String[] commandAndParam) {
		
		if(commandAndParam.length < 2){
			return null;
		}
		String parameters[] = new String[1];
		String cant = commandAndParam[1];
		
		Integer c = Integer.parseInt(cant);
		if(c == null){
			return null;
		}
		
		if(commandAndParam.length > 2){
			parameters = new String[commandAndParam.length-1];
			for(int i = 2; i < commandAndParam.length; i++){
				parameters[i-1] = commandAndParam[i];
			}
		}
		
		parameters[0] = cant;
		
		return parameters;
		
	}

	private static String[] getTimeLoginParameters(String[] commandAndParam) {
		
		if(commandAndParam.length < 3){
			return null;
		}
		String parameters[] = new String[2];
		String fromTime = commandAndParam[1];
		String toTime = commandAndParam[2];
		
		if(!isValidDate(fromTime) || !isValidDate(toTime)){
			return null;
		}
		
		if(commandAndParam.length > 3){
			parameters = new String[commandAndParam.length-1];
			for(int i = 3; i < commandAndParam.length; i++){
				parameters[i-1] = commandAndParam[i];
			}
		}
		
		parameters[0] = fromTime;
		parameters[1] = toTime;
		
		return parameters;
		
	}


	public static String getWellcomeMsg() {		
		return WELLCOME_MSG;
	}

	public static String getInvalidCommandMsg() {
		return INV_COM_MSG;
	}
	
	
	public static String getInvalidSubCommandMsg() {
		return INV_SUBCOM_MSG;
	}

	public static String getInvalidUserMsg() {
		return INV_USR_MSG;
	}

	public static String getOkMsg() {
		return OK_MSG+"\r\n";
	}

	public static String getInvalidArgumentMsg() {
		return INV_ARG_MSG;
	}

	public static String getInvalidCommandsMsg() {
		return INV_CONV_COM_MSG;
	}

	public static String getExitMsg() {
		return EXT_MSG;
	}
	
	private static boolean isValidDate(String fromTime) {
		// TODO Auto-generated method stub
		return false;
	}

	
	private static boolean isValidPort(String string) {
		Integer port;
		try{
			port = Integer.parseInt(string);
		}
		catch(NumberFormatException e){
			return false;
		}
		return port != null;
	}

	private static boolean isValidSender(String sender) {
		// TODO Auto-generated method stub
		return false;
	}


	public static String getStatusMsg(POPXY popxy) {
		String ret = OK_MSG + "\n";
		ret = ret + " originserver \t " + popxy.getDefaultOriginServer()+":"+popxy.getDefaultOriginServerPort()+"\n";
		ret = ret + " configListeningPort \t " + popxy.getAdminPort()+"\n";
		ret = ret + " wellcomeListeningPort \t " + popxy.getWelcomeSocketPort()+"\n";
		ret = ret + " statsListeningPort \t " + popxy.getStatsPort()+"\n";
		
		
		return ret;
	}
}
