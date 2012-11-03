package ar.protocols;

import java.util.Set;

import org.joda.time.DateTime;

import ar.POPXY;
import ar.elements.IntervalTime;
import ar.elements.User;
import ar.elements.UserConfiguration;
import ar.sessions.utils.ConfigurationCommands;
import ar.sessions.utils.IpAndMask;

public class ConfigurationProtocol {

	public static final String OK_MSG = "+OK ";
	public static final String ERR_MSG = "-ERR ";
	public static final String FEED_TERMINATION = "\r\n.\r\n";
	private static final String WELLCOME_MSG = OK_MSG+ "Conexion establecida" + FEED_TERMINATION;
	private static final String INV_COM_MSG = ERR_MSG + "Comando invalido"  + FEED_TERMINATION;
	private static final String INV_SUBCOM_MSG = ERR_MSG
			+ "Subcommando invalido"  + FEED_TERMINATION;
	private static final String INV_CONV_COM_MSG = ERR_MSG
			+ "Uso invalido de los comandos"  + FEED_TERMINATION;
	private static final String INV_ARG_MSG = ERR_MSG
			+ "Argumentos invalidos"  + FEED_TERMINATION;
	private static final String INV_USR_MSG = ERR_MSG
			+ "Usuario inexistente"  + FEED_TERMINATION;
	private static final String EXT_MSG = OK_MSG
			+ "Conexion cerrada exitosamente"  + FEED_TERMINATION;

	private static final String[] commands = { "EXT", "SET", "DEL", "STA",
			"RST" };
	private static final String[] subCommands = { "TIMELOGIN", "CANTLOGIN",
			"BLACKIP", "RMFILTERDATE", "RMFILTERSENDER", "RMFILTERHEADER",
			"RMFILTERCONTENT", "RMFILTERSIZE", "RMFILTERDISPOSITION",
			"ORIGINSERVER", "ORIGINSERVERPORT", "CONFIGPORT",
			"WELCOMEPORT", "STATSPORT", "APP" };

	public static ConfigurationCommands getCommand(String command) {

		String[] c = command.split("\\s+");
		if (c.length == 0) {
			return null;
		}
		command = c[0];
		command = command.toUpperCase();
		for (int i = 0; i < commands.length; i++) {
			if (command.equals(commands[i])) {
				return ConfigurationCommands.values()[i];
			}
		}
		return null;
	}

	public static ConfigurationCommands getSubCommand(String subCommand) {

		String[] s = subCommand.split("\\s+");
		if (s.length == 0) {
			return null;
		}
		subCommand = s[0];
		subCommand = subCommand.toUpperCase();

		for (int i = 0; i < subCommands.length; i++) {
			if (subCommand.equals(subCommands[i])) {
				return ConfigurationCommands.values()[i + 5];
			}
		}
		return null;
	}

	public static String[] getParameters(String subCommandAndParameters,
			ConfigurationCommands subCommand) {

		String[] commandAndParam = subCommandAndParameters.split("\\s+");
		String parameters[] = null;

		switch (subCommand) {
		case TIME_LOGIN:
			parameters = getTimeLoginParameters(commandAndParam);
			break;
		case CANT_LOGIN:
			parameters = getCantLoginParameters(commandAndParam);
			break;
		case BLACK_IP:
			parameters = getBlackIpParameters(commandAndParam);
			break;
		case RM_FILTER_DATE:
			parameters = getRmFilterDateParameters(commandAndParam);
			break;
		case RM_FILTER_SENDER:
			parameters = getRmFilterSenderParameters(commandAndParam);
			break;
		case RM_FILTER_HEADER:
			parameters = getRmFilterHeaderParameters(commandAndParam);
			break;
		case RM_FILTER_CONTENT:
			parameters = getRmFilterContentParameters(commandAndParam);
			break;
		case RM_FILTER_SIZE:
			parameters = getRmFilterSizeParameters(commandAndParam);
			break;
		case RM_FILTER_DISPOSITION:
			parameters = getRmFilterDispositionParameters(commandAndParam);
			break;
		case ORIGIN_SERVER:
			parameters = getOriginServerParameters(commandAndParam);
			break;
		case ORIGIN_SERVER_PORT:
			parameters = getListeningPortParameters(commandAndParam);
			break;
		case CONFIG_LISTENING_PORT:
			parameters = getListeningPortParameters(commandAndParam);
			break;
		case WELLCOME_LISTENING_PORT:
			parameters = getListeningPortParameters(commandAndParam);
			break;
		case STA_LISTENING_PORT:
			parameters = getListeningPortParameters(commandAndParam);
			break;
		case APP:
			parameters = getAppParameters(commandAndParam);
			break;
		default:
			return null;
		}

		return parameters;
	}

	//Ej: set app /bin/bash -c cat -users kkenny mmesa 
	private static String[] getAppParameters(String[] commandAndParam) {
		if (commandAndParam.length < 2) {
			return null;
		}
		String[] app = new String[1];
		String path = commandAndParam[1];
		int i;
		for (i = 2; i < commandAndParam.length; i++) {
			if (!commandAndParam[i].equals("-users")) {
				path =path.concat(" " + commandAndParam[i]);
			} else {
				break;
			}
		}
		if(i < commandAndParam.length){
			app = new String[commandAndParam.length-i];
			for (int j = 1; j+i < commandAndParam.length; j++) {
				app[j] = commandAndParam[j+i];
			}
		}
		app[0] = path;
		return app;
	}

	private static String[] getListeningPortParameters(String[] commandAndParam) {

		if (commandAndParam.length != 2) {
			return null;
		}
		String port[] = new String[1];
		port[0] = commandAndParam[1];

		if (!isValidPort(port[0])) {
			return null;
		}

		return port;
	}

	private static String[] getOriginServerParameters(String[] commandAndParam) {

		if (commandAndParam.length != 2)
			return null;

		String server[] = new String[1];
		server[0] = commandAndParam[1];

		String[] servAndPort = server[0].split(":");

		if (servAndPort.length > 2 || servAndPort.length < 1)
			return null;

		if (servAndPort.length == 2) {
			if (!isValidPort(servAndPort[1]))
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

		if (commandAndParam.length < 2) {
			return null;
		}
		String parameters[] = new String[1];
		String size = commandAndParam[1];

		Integer c = Integer.parseInt(size);
		if (c == null) {
			return null;
		}

		if (commandAndParam.length > 2) {
			parameters = new String[commandAndParam.length - 1];
			for (int i = 2; i < commandAndParam.length; i++) {
				parameters[i - 1] = commandAndParam[i];
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

		if (commandAndParam.length < 2) {
			return null;
		}
		String parameters[] = new String[1];
		String sender = commandAndParam[1];

		if (!isValidSender(sender)) {
			return null;
		}

		if (commandAndParam.length > 2) {
			parameters = new String[commandAndParam.length - 1];
			for (int i = 2; i < commandAndParam.length; i++) {
				parameters[i - 1] = commandAndParam[i];
			}
		}

		parameters[0] = sender;

		return parameters;

	}

	private static String[] getRmFilterDateParameters(String[] commandAndParam) {

		if (commandAndParam.length < 2) {
			return null;
		}
		String parameters[] = new String[1];
		String date = commandAndParam[1];

		if (!isValidDate(date)) {
			return null;
		}

		if (commandAndParam.length > 2) {
			parameters = new String[commandAndParam.length - 1];
			for (int i = 2; i < commandAndParam.length; i++) {
				parameters[i - 1] = commandAndParam[i];
			}
		}

		parameters[0] = date;

		return parameters;

	}

	private static boolean isValidDate(String date) {
		String[] aux = date.split("-");
		if(aux.length != 3)
			return false;
		int day, month, year;
		try{
			day = Integer.parseInt(aux[0]);
			month = Integer.parseInt(aux[1]);
			year = Integer.parseInt(aux[2]);
		} catch (NumberFormatException e){
			return false;
		}
		DateTime d = new DateTime(year, month, day, 0, 0, 0, 0);
		if(d != null)
			return true;
		return false;
	}

	private static String[] getBlackIpParameters(String[] commandAndParam) {

		if (commandAndParam.length < 2 || commandAndParam.length > 3) {
			return null;
		}
		String parameters[] = new String[1];
		String ip = commandAndParam[1];

		if (!IpAndMask.isValidIp(ip)) {
			return null;
		}

		if (commandAndParam.length == 3) {
			parameters = new String[2];
			String mask = commandAndParam[2];
			if (!IpAndMask.isValidIp(mask)) {
				return null;
			}
			parameters[1] = mask;
		}

		parameters[0] = ip;

		return parameters;

	}

	private static String[] getCantLoginParameters(String[] commandAndParam) {

		if (commandAndParam.length < 2) {
			return null;
		}
		String parameters[] = new String[1];
		String cant = commandAndParam[1];

		Integer c = Integer.parseInt(cant);
		if (c == null) {
			return null;
		}

		if (commandAndParam.length > 2) {
			parameters = new String[commandAndParam.length - 1];
			for (int i = 2; i < commandAndParam.length; i++) {
				parameters[i - 1] = commandAndParam[i];
			}
		}

		parameters[0] = cant;

		return parameters;

	}

	private static String[] getTimeLoginParameters(String[] commandAndParam) {

		if (commandAndParam.length < 3) {
			return null;
		}
		String parameters[] = new String[2];
		String fromTime = commandAndParam[1];
		String toTime = commandAndParam[2];

		if (!isValidTime(fromTime) || !isValidTime(toTime)) {
			return null;
		}

		if (commandAndParam.length > 3) {
			parameters = new String[commandAndParam.length - 1];
			for (int i = 3; i < commandAndParam.length; i++) {
				parameters[i - 1] = commandAndParam[i];
			}
		}

		parameters[0] = timeToMinutes(fromTime);
		parameters[1] = timeToMinutes(toTime);

		return parameters;

	}

	private static String timeToMinutes(String time) {
		Integer hour = Integer.parseInt(time.substring(0, 2));
		Integer min = Integer.parseInt(time.substring(2, 4));

		min = min + (hour * 60);

		return min.toString();
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
		return OK_MSG + FEED_TERMINATION;
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

	private static boolean isValidTime(String time) {
		if (time.length() != 4)
			return false;

		Integer hour, min;

		try {
			hour = Integer.parseInt(time.substring(0, 2));
			min = Integer.parseInt(time.substring(2, 4));
		} catch (Exception e) {
			return false;
		}

		if (hour == null || min == null) {
			return false;
		}

		if (min < 0 || min > 59)
			return false;

		if (hour < 0 || hour > 23)
			return false;

		return true;
	}

	private static boolean isValidPort(String string) {
		Integer port;
		try {
			port = Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return false;
		}
		return port != null;
	}

	private static boolean isValidSender(String sender) {
		return true;
	}

	public static String getStatusMsg(String params) {
		
		POPXY popxy = POPXY.getInstance();
		String ret = OK_MSG + "\n";

		if(params.length() == 0){
		
			ret = ret + "- ORIGINSERVER\t\t" + User.getGlobalServerAddress() + ":"+ User.getGlobalServerPort() + "\n";
			ret = ret + "- CONFIGPORT\t" + popxy.getAdminPort() + "\n";
			ret = ret + "- WELCOMEPORT\t" + popxy.getWelcomeSocketPort()+ "\n";
			ret = ret + "- STATSPORT\t" + popxy.getStatsPort() + "\n";
			
			for(IpAndMask ip: popxy.getBlackIps()){
				ret = ret + "- BLACKIP\t" + ip.getIp() +"\t"+ ip.getMask()+"\n";
			}

			UserConfiguration conf = User.getGlobalConfiguration();
			ret = ret + getStatusMsg(conf);
			
			if(popxy.getUsernames().size() != 0){
				ret = ret + "- Users:\r\n";
			}
			for(String username: popxy.getUsernames()){
				ret = ret + username+"\t";
			}
			if(popxy.getUsernames().size() > 0){
				ret = ret+ "\n";
			}
			
		}
		else{
			String username = params.split("\\s")[0];
			if(!popxy.existingUser(username)){
				ret = "-ERR. Usuario inexistente\n";
				return ret;
			}
			User user = popxy.getUser(username);
			ret = ret + getStatusMsg(user.getUserConfig());
		}
		ret = ret + FEED_TERMINATION;
		return ret;
	}

	private static String getStatusMsg(UserConfiguration conf) {
		String ret = "";
		if(conf.getLoginMax() != -1){
			ret = ret + "- CANTLOGIN\t"+conf.getLoginMax()+"\n";
		}
		for(IntervalTime i: conf.getTimeConfiguration().getIntervals()){
			ret = ret + "- TIMELOGIN\t";
			Integer hF = i.getMinFrom()/60;
			Integer hT = i.getMinTo()/60;
			Integer mF = i.getMinFrom()%60;
			Integer mT = i.getMinTo()%60;
			ret = ret + ((hF.toString().length()==2)? hF: "0"+hF);
			ret = ret + ((mF.toString().length()==2)? mF: "0"+mF);
			ret = ret + "\t" +((hT.toString().length()==2)? hT: "0"+hT);
			ret = ret + ((mT.toString().length()==2)? mT: "0"+mT) + "\n";
		}
				
		
		if(conf.hasExternalApps()){
			if(conf.getLeet()) ret = ret + "- l33t\n";
			if(conf.getRotate()) ret = ret + "- Rotate\n";
			if(conf.getAnonymous()) ret = ret + "- Anonymous\n";
			for(String path: conf.getExternalApps()){
				ret = ret + "- path\n";
			}			
		}
		return ret;
	}
}
